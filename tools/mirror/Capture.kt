package com.hunt3r.southparktown

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import java.io.File
import javax.imageio.ImageIO

/**
 * Headless scene capture for the Windows mirror (internal QA tooling).
 *
 * Usage:
 *   Capture <outDir> [--size 720x1280] [--time 15:38] [--weather light_snow]
 *           [--tag before] scene [scene...]
 * Scene spec:
 *   LocId                exterior at the location
 *   LocId:in             the location's own interior
 *   StreetId:sec=SecId   a house-street section interior
 *   LocId:bus=school     aboard the school bus (from LocId)
 *   LocId:bus=city       aboard a city bus
 *
 * Every capture stages a FRESH game state through the real Engine
 * (spawn at homes, then advanceTime in the engine's own 15-minute steps
 * to the target minute — the same population the phone would show),
 * pins the weather, places the player, and renders the real GameView
 * into a PNG. A fix counts only when the capture that showed the
 * problem reads clean.
 */
object Capture {

    /**
     * v0.18.0c (Phase 1 — Sight): golden-harness hooks, used
     * ONLY by Golden.run. Normal captures never set these and
     * stage exactly as they always have. In golden mode the
     * staged Engine is built from [goldenSeed] (the shipped
     * default seed is the wall clock — the population would
     * differ on every run), the view's ticker is detached and
     * its tick reset to zero the moment it attaches (the shim
     * Handler runs the ticker on the Swing EDT, so a capture
     * that leaves it alive renders a scheduling-dependent
     * frame), and the finished bitmap is handed to
     * [bitmapSink] instead of being written as a QA file.
     */
    var goldenSeed: Long? = null
    var goldenMode: Boolean = false
    var bitmapSink: ((String, Bitmap) -> Unit)? = null

    fun run(args: List<String>, dataDir: File, projectDir: File) {
        if (args.contains("--trip")) {
            val name = args[args.indexOf("--trip") + 1]
            var outDir = File(".")
            var weather = "light_snow"
            var i = 0
            while (i < args.size) {
                when (args[i]) {
                    "--out" -> outDir = File(args[i + 1])
                    "--weather" -> weather = args[i + 1]
                }
                i++
            }
            outDir.mkdirs()
            MirrorMain.act = Activity()
            ensureLoreLoaded(MirrorMain.act)
            val prefs = Prefs(FileSaveStore(File(dataDir, "prefs")))
            runTrip(name, outDir, weather, prefs.getAppearance())
            println("[capture] trip done -> $outDir")
            kotlin.system.exitProcess(0)
        }
        var outDir = File(".")
        var w = 720
        var h = 1280
        var timeMin = 15 * 60 + 38
        var weather = "light_snow"
        var tag = ""
        var frames = 4
        var pan = -1f
        var crowd = listOf<String>()
        var dumpHits = false
        var fiction: String? = null
        var walk: FloatArray? = null
        val scenes = mutableListOf<String>()
        var i = 0
        while (i < args.size) {
            when (val a = args[i]) {
                "--out" -> outDir = File(args[++i])
                "--size" -> {
                    val p = args[++i].split("x"); w = p[0].toInt(); h = p[1].toInt()
                }
                "--time" -> {
                    val p = args[++i].split(":"); timeMin = p[0].toInt() * 60 + p[1].toInt()
                }
                "--weather" -> weather = args[++i]
                "--tag" -> tag = args[++i]
                "--frames" -> frames = args[++i].toInt()
                "--pan" -> pan = args[++i].toFloat()
                "--hits" -> dumpHits = true
                "--fiction" -> fiction = args[++i]
                "--walk" -> {
                    val p = args[++i].split(",")
                        .map { it.trim().toFloat() }
                    walk = floatArrayOf(p[0], p[1], p[2], p[3])
                }
                "--crowd" -> crowd = args[++i].split(",").map { it.trim() }.filter { it.isNotEmpty() }
                else -> scenes.add(a)
            }
            i++
        }
        outDir.mkdirs()

        MirrorMain.act = Activity()
        ensureLoreLoaded(MirrorMain.act)
        val prefs = Prefs(FileSaveStore(File(dataDir, "prefs")))
        val appearance = prefs.getAppearance()

        for (spec in scenes) {
            try {
                if (spec.startsWith("photo:")) {
                    renderPhoto(spec.removePrefix("photo:"), outDir, w, h,
                        timeMin, weather, tag, appearance)
                } else {
                    render(spec, outDir, w, h, timeMin, weather, tag, frames, appearance, pan, crowd, dumpHits, fiction, walk)
                }
            } catch (e: Exception) {
                println("[capture] FAILED $spec: $e")
                e.printStackTrace()
            }
        }
        println("[capture] done -> $outDir")
        kotlin.system.exitProcess(0)
    }

    /**
     * The v0.16.0c item-12 proof: stage ONE cast member's
     * real schedule trip and photograph it tick by tick.
     * The agent is settled at the origin block, the real
     * Engine advances the clock, the real dispatcher sends
     * them, Travel.maintain stamps the trip, and each tick
     * the camera stands in Travel.pose's scene — the same
     * presence the street renders. Frames: the origin
     * before departure, every en-route scene, the arrival.
     */
    private fun runTrip(name: String, outDir: File, weather: String,
                        appearance: Map<String, String>) {
        val engine = Engine()
        val st = engine.newState()
        engine.createPlayer(st, PLAYER_DEFAULT_NAME, appearance)
        val a = st.agents[name]
            ?: run { println("[trip] no agent named $name"); return }
        // the trip to stage: this agent's first agenda
        // change whose street route crosses an intermediate
        // scene (else their first change at all)
        val blocks = a.agendaBase.entries
            .sortedBy { timeStrToMinutes(it.key) }
        var origin: String? = null
        var dest: String? = null
        var departMin = -1
        for (i in 0 until blocks.size - 1) {
            val here = blocks[i].value.location
            val next = blocks[i + 1].value.location
            if (here == next) continue
            if (Walk.route(here, next).size >= 3) {
                origin = here; dest = next
                departMin = timeStrToMinutes(blocks[i + 1].key)
                break
            }
            if (origin == null) {
                origin = here; dest = next
                departMin = timeStrToMinutes(blocks[i + 1].key)
            }
        }
        if (origin == null || dest == null) {
            println("[trip] $name never changes location in their agenda")
            return
        }
        println("[trip] $name: $origin -> $dest at $departMin " +
            "via ${Walk.route(origin, dest)}")
        // settle the whole town at the departure eve: run
        // the engine from the morning to just before the
        // block change, then place our walker home-free at
        // the origin, settled, exactly as the dispatcher
        // will find them. The tick is Engine.step — the
        // game's own 15-minute tick (advanceTime + animals
        // + updatePositions + interactions): advanceTime
        // alone dispatches waypoints but never walks them,
        // as the first staging of this harness proved.
        st.timeMin = 8 * 60
        while (st.timeMin < departMin - 15) engine.step(st)
        a.location = origin; a.destination = origin
        a.waypoints.clear(); a.asleep = false
        LOCATIONS[origin]?.let { a.coords = it.gx to it.gy }
        st.lifeText.remove(Travel.keyOf(name))
        st.weatherId = weather

        val me = engine.player(st)
        fun frame(tag: String, sceneId: String, focus: Float? = null) {
            me.location = sceneId
            LOCATIONS[sceneId]?.let { me.coords = it.gx to it.gy }
            me.destination = sceneId
            st.playerInside = false
            st.weatherId = weather
            MirrorMain.refreshRenderContext(engine, st)
            val view = GameView(MirrorMain.act)
            view.engine = engine
            view.state = st
            view.season = seasonFor(st.gameMonth)
            view.holiday = displayHoliday(st)
            view.generated = MirrorMain.generatedFor(engine, st)
            view.layout(0, 0, 720, 1280)
            view.attachForMirror()
            val bmp = Bitmap.createBitmap(720, 1280,
                Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            repeat(3) {
                Thread.sleep(90)
                if (focus != null) {
                    // pin the camera on the traveler (harness-only,
                    // same math as render's --pan): a wide street's
                    // proof frame must FRAME its walker.
                    val locF = LOCATIONS[sceneId]
                    if (locF != null) {
                        val worldW = 720f * SceneScale.factor(locF, false)
                        val target = (focus * worldW - 360f)
                            .coerceIn(0f, (worldW - 720f).coerceAtLeast(0f))
                        camTargetField.setFloat(view, target)
                        camXField.setFloat(view, target)
                    }
                }
                view.draw(canvas)
            }
            val out = File(outDir, "trip_${name}_$tag.png")
            ImageIO.write(bmp.img, "png", out)
            println("[trip] $tag -> ${out.name} (${out.length()} B)")
        }

        frame("t0-origin-$origin", origin)
        var arrived = false
        for (tickN in 1..48) {
            engine.step(st)
            st.weatherId = weather
            val pose = Travel.pose(st, a)
            if (pose != null) {
                frame("t${tickN}-enroute-${pose.sceneId}" +
                    "-p${(pose.progress * 100).toInt()}", pose.sceneId,
                    pose.xFrac)
            } else if (a.waypoints.isEmpty() &&
                a.location == dest && !arrived) {
                frame("t${tickN}-arrived-$dest", dest)
                arrived = true
                break
            }
        }
        if (!arrived) println("[trip] WARNING: no arrival within 48 ticks")
    }

    /**
     * v0.16.0d: a PHOTO STILL capture — a fabricated group
     * record at [locId], rendered through the real PhotoView
     * (the album / yearbook / darkroom surface). Its figures
     * render through the PaperDoll path (stage 10), so this
     * frame is the doll path's visual proof: front poses,
     * portrait-weight painted outlines, paper ground.
     */
    private fun renderPhoto(locId: String, outDir: File, w: Int, h: Int,
                            timeMin: Int, weather: String, tag: String,
                            appearance: Map<String, String>) {
        val rec = PhotoRecord(
            id = "capture-still", dayKey = "20261024",
            dateLabel = "10/24", timeMin = timeMin, locId = locId,
            inside = false, weatherId = weather,
            appearance = appearance,
            figures = listOf(
                PhotoFigure(PLAYER_DEFAULT_NAME, "player", "happy"),
                PhotoFigure("Stan", "cast", "happy"),
                PhotoFigure("Kyle", "cast", "idle"),
                PhotoFigure("Butters", "cast", "happy")),
            caption = "capture still", seed = 1234L, absDay = 10,
            composition = "lineup")
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(),
            Paper.fillPaint(Paper.PARCHMENT))
        val view = PhotoView(MirrorMain.act)
        val frame = android.graphics.RectF(36f, 150f,
            w - 36f, h - 260f)
        view.drawInto(canvas, rec, frame)
        val name = buildString {
            append("photo_").append(locId)
            append("_").append("%02d%02d".format(timeMin / 60, timeMin % 60))
            append("_").append(weather)
            if (tag.isNotEmpty()) append("_").append(tag)
            append(".png")
        }
        val out = File(outDir, name)
        ImageIO.write(bmp.img, "png", out)
        println("[capture] photo:$locId -> ${out.path} (${out.length()} B)")
    }

    private val camXField = GameView::class.java.getDeclaredField("camX")
        .apply { isAccessible = true }
    private val camTargetField = GameView::class.java.getDeclaredField("camTargetX")
        .apply { isAccessible = true }
    private val tickField = GameView::class.java.getDeclaredField("tick")
        .apply { isAccessible = true }

    fun render(spec: String, outDir: File, w: Int, h: Int, timeMin: Int,
                       weather: String, tag: String, frames: Int,
                       appearance: Map<String, String>, pan: Float,
                       crowd: List<String>, dumpHits: Boolean = false,
                       fiction: String? = null,
                       walk: FloatArray? = null) {
        // a fiction section id itself carries a colon
        // (fiction:dungeon) — shield it through the split
        val parts = spec.replace("sec=fiction:", "sec=fiction~")
            .split(":")
        val locId = parts[0]
        var inside = false
        var section: String? = null
        var bus: String? = null
        for (p in parts.drop(1)) {
            when {
                p == "in" -> inside = true
                p.startsWith("sec=") -> { section = p.substring(4).replace("~", ":"); inside = true }
                p.startsWith("bus=") -> { bus = p.substring(4); inside = true }
            }
        }
        // A fresh world each capture, run forward by the engine itself.
        // Golden mode pins the engine seed (see the hooks above).
        val engine = if (goldenMode && goldenSeed != null) {
            Engine(goldenSeed!!)
        } else {
            Engine()
        }
        val st = engine.newState()
        engine.createPlayer(st, PLAYER_DEFAULT_NAME, appearance)
        var guard = 0
        // Engine.step, not advanceTime alone: advanceTime
        // dispatches waypoints but never walks them and
        // never stamps Travel records, so a scene staged
        // with it holds no travelers at all (runTrip's
        // staging comment records the same lesson).
        while (st.timeMin < timeMin && guard++ < 200) engine.step(st)
        st.timeMin = timeMin
        st.weatherId = weather
        st.lastConversation = emptyList()
        val me = engine.player(st)
        me.location = locId
        LOCATIONS[locId]?.let { me.coords = it.gx to it.gy }
        me.destination = locId
        // optional staged crowd: teleport named cast here (render QA —
        // the renderer under test doesn't care how the crowd arrived)
        for (name in crowd) {
            val a = st.agents[name] ?: continue
            a.location = locId
            a.waypoints.clear()
            a.asleep = false
            LOCATIONS[locId]?.let { a.coords = it.gx to it.gy }
            a.destination = locId
        }
        st.playerInside = inside
        if (section != null) {
            val resolved = when (section) {
                "first", "last" -> {
                    val secs = ResidentialStreets.housesFor(LOCATIONS[locId]!!)
                        .map { it.sectionId }.filter { it.isNotEmpty() }
                    if (section == "first") secs.firstOrNull() else secs.lastOrNull()
                }
                else -> section
            }
            if (resolved != null) st.lifeText["insideSection"] = resolved
        }
        if (bus != null) {
            val to = if (bus == "school") SchoolBus.SCHOOL else "MainStreet"
            BusRideState.board(st, bus, locId, to)
        }

        // v0.16.0e pass 2 QA: stage the costume system at a
        // chosen phase — the fiction's four save keys are
        // written directly (start = day 10, 8 AM), and the
        // clock is placed inside the requested phase. The
        // renderer under test derives everything else.
        if (fiction != null) {
            st.lifeText[Fiction.KEY_ID] = Fiction.FANTASY.id
            st.life[Fiction.KEY_START] = 10 * 96 + 32
            when (fiction) {
                "dressing" -> { st.absDay = 10; st.timeMin = 555 }
                "live" -> { st.absDay = 10; st.timeMin = 900 }
                "escalating" -> { st.absDay = 11; st.timeMin = 900 }
                "residue" -> {
                    st.life[Fiction.KEY_END] = 10 * 96 + 40
                    st.absDay = 11; st.timeMin = 600
                    Fiction.settleResidue(st)
                }
            }
        }

        MirrorMain.refreshRenderContext(engine, st)
        val view = GameView(MirrorMain.act)
        view.engine = engine
        view.state = st
        view.season = seasonFor(st.gameMonth)
        view.holiday = displayHoliday(st)
        view.generated = MirrorMain.generatedFor(engine, st)
        view.layout(0, 0, w, h)
        view.attachForMirror()
        if (goldenMode) {
            // freeze the frame clock: detach kills the ticker
            // (its EDT posts are the one async writer of tick),
            // and tick is reset in case a post already landed.
            view.detachForMirror()
            tickField.setLong(view, 0L)
        }
        // let the frame loop settle (wander phases, camera ease)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val loc = LOCATIONS[locId]
        // v0.18.0b QA: stage a WALK — one settle frame
        // first (the location key claims the spawn and
        // the crowd publishes its feet, exactly as on
        // device), then the player's PaperWalk is aimed
        // across the scene and the frame loop's own
        // ticker walks him. --frames decides how far
        // into the walk the shutter falls.
        val wk = walk
        if (wk != null) {
            view.draw(canvas)
            val pwalkField = GameView::class.java
                .getDeclaredField("pwalk")
                .apply { isAccessible = true }
            val start = PaperWalk.State(x = wk[0],
                band = wk[1])
            pwalkField.set(view, PaperWalk.tapGround(
                start, wk[2], wk[3]))
            for (a in st.agents.values) {
                if (a.isPlayer || a.waypoints.isEmpty())
                    continue
                val pose = Travel.pose(st, a) ?: continue
                val dir = if (pose.facingRight) "R"
                    else "L"
                val here = if (pose.sceneId == locId)
                    " HERE" else ""
                println("[walk] traveler ${a.name} " +
                    "in ${pose.sceneId} at x=" +
                    "${pose.xFrac} band=${Travel.BAND} " +
                    "facing=$dir$here")
            }
            println("[walk] player staged (${wk[0]}, " +
                "${wk[1]}) -> (${wk[2]}, ${wk[3]})")
        }
        repeat(frames) {
            Thread.sleep(90)
            if (pan >= 0f && loc != null) {
                // pin the camera on a world fraction (harness-only:
                // GameView's own clamp math decides the final value)
                val worldW = w * SceneScale.factor(loc, inside)
                val target = (pan * worldW - w / 2f).coerceIn(0f, (worldW - w).coerceAtLeast(0f))
                camTargetField.setFloat(view, target)
                camXField.setFloat(view, target)
            }
            view.draw(canvas)
        }
        if (wk != null) {
            val pf = GameView::class.java
                .getDeclaredField("pwalk")
                .apply { isAccessible = true }
            val end = pf.get(view) as PaperWalk.State
            println("[walk] player ended at " +
                "(${end.x}, ${end.band}) walking=" +
                "${end.walking}")
        }
        val sink = bitmapSink
        if (sink != null) {
            sink(spec, bmp)
            println("[capture] $spec -> (golden sink)")
            if (dumpHits) dumpHitList(view)
            return
        }
        val name = buildString {
            append(spec.replace(":", "_").replace("=", "-"))
            append("_").append("%02d%02d".format(timeMin / 60, timeMin % 60))
            append("_").append(weather)
            if (tag.isNotEmpty()) append("_").append(tag)
            append(".png")
        }
        val out = File(outDir, name)
        ImageIO.write(bmp.img, "png", out)
        println("[capture] $spec -> ${out.path} (${out.length()} B)")
        if (dumpHits) dumpHitList(view)
    }

    /** QA bisection aid: what did this frame register as tappable, where? */
    private fun dumpHitList(view: GameView) {
        try {
            val f = GameView::class.java.getDeclaredField("hits").apply { isAccessible = true }
            val list = f.get(view) as List<*>
            val camX = camXField.getFloat(view)
            println("[hits] ${list.size} hits (world rect -> screen x):")
            for (hit in list) {
                if (hit == null) continue
                val cls = hit.javaClass
                val kind = cls.getDeclaredField("kind").apply { isAccessible = true }.get(hit)
                val id = cls.getDeclaredField("id").apply { isAccessible = true }.get(hit)
                val rect = cls.getDeclaredField("rect").apply { isAccessible = true }.get(hit)
                        as android.graphics.RectF
                println("[hits] $kind $id [${rect.left.toInt()},${rect.top.toInt()} -> " +
                    "${rect.right.toInt()},${rect.bottom.toInt()}] screenX=${(rect.left - camX).toInt()}")
            }
        } catch (e: Exception) {
            println("[hits] dump failed: $e")
        }
    }
}

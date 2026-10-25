package com.hunt3r.southparktown

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.SwingConstants
import javax.swing.Timer

/**
 * THE MIRROR — internal QA tooling, NOT a shipped product.
 *
 * v0.16.0b: Lucky asked for "the same thing on both windows and android so
 * you can adequately bug test the android copy." v0.16.0c: the Linux run
 * is a first-class build in its own right ("Make it work on android and
 * Linux so you can test it") — out/south-park-town-v0160c-linux/, packaged
 * by tools/mirror/build_linux.sh from these same classes; the Windows
 * .exe payload is packaged from them too. ONE implementation, two
 * wrappers. This host runs THE SAME Kotlin game — the whole src/ tree,
 * the real GameView, SceneArt, KidArt, Engine, Prefs and save format —
 * on a desktop JVM, against a small android.* compatibility shim
 * (tools/mirror/shim) whose graphics layer forwards the game's own
 * android.graphics calls to Java2D. The only forced platform divergence
 * is the final rasterizer (Java2D instead of Skia) and the host chrome
 * (this file replaces GameActivity's Android widget wiring at GameView's
 * own callback seams); every scene, figure, scale and layer decision
 * rendered here is the APK's own code.
 *
 * Modes:
 *   (no args)            interactive window: walk, click, ride, save.
 *   --capture <dir> ...  headless scene captures to PNG (see Capture.kt).
 *   --selftest <dir>     launched-build proof: opens the real window,
 *                        Robot-grabs it, clicks the scene with a real
 *                        (synthesized) mouse press, grabs it again,
 *                        prints the live state, exits. Used to verify
 *                        the packaged Linux build end to end.
 *
 * Saves: the mirror reads/writes the game's own Prefs store (same keys,
 * same JSON) in its own data dir (~/.sptown-mirror), so a mirror save is
 * byte-for-byte the format the APK loads.
 */
object MirrorMain {

    lateinit var act: Activity
    lateinit var prefs: Prefs
    lateinit var engine: Engine
    lateinit var state: GameState
    lateinit var view: GameView
    var statusLabel: JLabel? = null
    var frame: JFrame? = null
    var selfTestDir: File? = null

    @JvmStatic
    fun main(args: Array<String>) {
        val home = File(System.getProperty("user.home"))
        val dataDir = File(System.getenv("SPT_MIRROR_DATA") ?: File(home, ".sptown-mirror").path)
        Context.dataDir = dataDir
        // The mirror runs from the project root (build_mirror.sh / the
        // launcher both cd there); SPT_PROJECT overrides for captures
        // launched from elsewhere.
        val projectDir = File(System.getenv("SPT_PROJECT") ?: System.getProperty("user.dir"))
        AssetManager.baseDir = File(projectDir, "assets")
        if (args.isNotEmpty() && args[0] == "--capture") {
            System.setProperty("java.awt.headless", "true")
            Capture.run(args.drop(1), dataDir, projectDir)
            return
        }
        if (args.isNotEmpty() && args[0] == "--selftest") {
            selfTestDir = File(args.getOrElse(1) { "selftest" })
            selfTestDir?.mkdirs()
        }
        SwingUtilities.invokeLater { bootInteractive(dataDir) }
    }

    /** The launched-build proof (v0.16.0c): with the real
     *  window up and the game's own tick timer running,
     *  grab the window with java.awt.Robot (a true screen
     *  grab of the launched process, not the capture
     *  harness's offscreen path), press the mouse on the
     *  scene — the press travels AWT -> MotionEvent ->
     *  GameView.onTouchEvent exactly like a hand's click —
     *  grab again, print the live state, and exit. */
    fun runSelfTest(outDir: File, dataDir: File) {
        val t = Thread {
            try {
                Thread.sleep(6000)
                val robot = java.awt.Robot()
                val f = frame ?: run {
                    println("[selftest] FAIL: no frame"); kotlin.system.exitProcess(2)
                }
                fun grab(name: String) {
                    val loc = f.locationOnScreen
                    val img = robot.createScreenCapture(
                        java.awt.Rectangle(loc.x, loc.y, f.width, f.height))
                    val out = File(outDir, name)
                    ImageIO.write(img, "png", out)
                    println("[selftest] grabbed ${out.path} (${out.length()} B)")
                }
                grab("selftest-window.png")
                val loc = f.locationOnScreen
                robot.mouseMove(loc.x + f.width / 2, loc.y + f.height / 2)
                robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
                Thread.sleep(150)
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK)
                println("[selftest] mouse press delivered at window center")
                Thread.sleep(4000)
                grab("selftest-after-click.png")
                val saveFile = File(dataDir, "prefs/sptown.properties")
                println("[selftest] state: at ${engine.player(state).location}, " +
                    "timeMin=${state.timeMin}, weather=${state.weatherId}, " +
                    "saveFile=${saveFile.path} exists=${saveFile.exists()} " +
                    "(${if (saveFile.exists()) saveFile.length() else 0} B)")
                println("[selftest] OK")
                kotlin.system.exitProcess(0)
            } catch (e: Exception) {
                println("[selftest] FAIL: $e")
                e.printStackTrace()
                kotlin.system.exitProcess(2)
            }
        }
        t.isDaemon = true
        t.start()
    }

    /** Shared boot: lore, prefs, engine, state — GameActivity's own order. */
    fun boot(dataDir: File) {
        act = Activity()
        ensureLoreLoaded(act)
        prefs = Prefs(FileSaveStore(File(dataDir, "prefs")))
        engine = Engine()
        val loaded = prefs.loadGame()
        val pName = prefs.getPlayerName() ?: PLAYER_DEFAULT_NAME
        val pApp = prefs.getAppearance()
        if (loaded == null) {
            state = engine.newState()
            engine.createPlayer(state, pName, pApp)
        } else {
            state = loaded
            val me = engine.player(state)
            me.name = pName
            me.appearance = pApp
        }
        Episodes.syncAiring(state)
        Walk.ensureFeatPass(state)
        Wardrobe.syncUnlocks(state)
        prefs.loadNpcMemory()
        VisitorSystem.favoritesProvider = {
            NpcMemoryStore.all().filterValues {
                Relationships.tierOf(it.points) >= Relationships.FRIEND
            }.keys
        }
        VisitorSystem.influenceProvider = {
            state.factionStates.mapValues { it.value.influence }
        }
        VisitorSystem.convertedProvider = { state.convertedResidents.toList() }
        engine.memoryGreetingProvider = { name ->
            NpcMemoryStore.repeatGreeting(name, engine.player(state).name)
        }
    }

    fun generatedHere(): List<ActiveNpc> = generatedFor(engine, state)

    /** VisitorSystem.presentAt with GameActivity.generatedHere's inputs. */
    fun generatedFor(eng: Engine, st: GameState): List<ActiveNpc> {
        val me = try { eng.player(st) } catch (e: Exception) { return emptyList() }
        val week = (st.gameDay + (st.gameMonth - 1) * 31) / 7
        return try {
            VisitorSystem.presentAt(me.location, st.playerInside,
                st.todayKey, st.timeMin, week, dayOfWeek(st))
        } catch (e: Exception) { emptyList() }
    }

    /**
     * GameActivity.refreshUi's render-context feed, replicated line for
     * line from the same source calls (GameActivity.kt ~571-663): the
     * living-world layers (fair, carnival, dig/fix/marks, sky, holdouts)
     * read TownInfo globals, and a capture that skips this feed would
     * silently under-dress every scene versus the phone.
     */
    fun refreshRenderContext(eng: Engine, st: GameState) {
        val me = try { eng.player(st) } catch (e: Exception) { return }
        val season = seasonFor(st.gameMonth)
        try {
            TownInfo.todayKey = st.todayKey
            TownInfo.ordinal = st.absDay
            TownInfo.timeMin = st.timeMin
            TownInfo.weatherId = st.weatherId
            TownInfo.season = season
            TownInfo.holidayId = st.holidayId
            TownInfo.occupiedHomes = HOUSE_LOCS.filter { house ->
                val owner = KID_HOUSE.entries.firstOrNull { it.value == house }?.key
                owner != null && st.agents[owner]?.location == house
            }.toSet() +
                (if (me.location == YOUR_HOUSE) setOf(YOUR_HOUSE) else emptySet())
            TownInfo.houseDecor = House.decorTokensFor(st) +
                Sleepovers.dressingTokens(st)
            TownInfo.digTokens = Mystery3.digTokensByLoc(st)
            TownInfo.fixTokens = Mystery4.fixTokensByLoc(st)
            TownInfo.factionMarkTokens =
                (FactionMissions.markTokensByLoc(st).entries +
                    Capstones.markTokensByLoc(st).entries)
                    .groupBy({ it.key }, { it.value })
                    .mapValues { (_, v) -> v.flatten() }
            TownInfo.fairTokens = Fair.fairTokensByLoc(st)
            TownInfo.carnivalTokens = Carnival.carnivalTokensByLoc(st)
            TownInfo.bedtimeOf = HOUSE_LOCS.mapNotNull { house ->
                val owner = KID_HOUSE.entries.firstOrNull { it.value == house }?.key
                    ?: return@mapNotNull null
                val agent = st.agents[owner] ?: return@mapNotNull null
                val lastKey = agent.agendaBase.keys
                    .maxByOrNull { timeStrToMinutes(it) } ?: return@mapNotNull null
                house to (timeStrToMinutes(lastKey) + 45)
            }.toMap()
            TownInfo.closedShops = eng.KEEPERS.keys
                .filter { eng.shopClosedReason(st, it) != null }.toSet()
            TownInfo.tvHouses = TownInfo.occupiedHomes
                .filter { (it.hashCode() + st.absDay) % 3 == 0 }.toSet()
            val skyNow = SkyState.now(st.absDay, st.timeMin, st.weatherId, season)
            TownInfo.auroraTonight = skyNow.special == SkyState.AURORA
            TownInfo.skySpecial = skyNow.special
            TownInfo.moonlight = skyNow.moonlight
            TownInfo.harvestMoon = skyNow.harvest
            TownInfo.auroraIntensity = skyNow.auroraIntensity
            TownInfo.mudToday = st.life["mudDay"] == st.absDay
            TownInfo.freezeThaw = st.life["snowYesterday"] == st.absDay - 1 &&
                st.weatherId in setOf("sunny", "partly_cloudy", "cloudy")
            TownInfo.budLevel = if (season == Season.SPRING) {
                ((st.gameDay + (st.gameMonth - 3).coerceAtLeast(0) * 31) / 45f)
                    .coerceIn(0f, 1f)
            } else 0f
            TownInfo.moonPhase = TownLife.moonPhase(st.absDay)
            val todayOrd = ordinalOfKey(st.todayKey)
            var bestHoldout: Holiday? = null
            var bestDiff = Int.MAX_VALUE
            for (hd in HOLIDAYS) {
                for (y in listOf(st.gameYear, st.gameYear - 1)) {
                    val diff = todayOrd - ordinalOfKey("%04d%s".format(y, hd.endMd))
                    if (diff in 1..14 && diff < bestDiff) {
                        bestDiff = diff; bestHoldout = hd
                    }
                }
            }
            TownInfo.holdoutHoliday = bestHoldout
            TownInfo.holdoutLoc = bestHoldout?.let { hd ->
                HOUSE_LOCS[((hd.id.hashCode() % HOUSE_LOCS.size) + HOUSE_LOCS.size) %
                    HOUSE_LOCS.size]
            }
            TownInfo.curtainWarm = if (st.playerInside) {
                val owner = KID_HOUSE.entries.firstOrNull { it.value == me.location }?.key
                owner != null && Relationships.tierOf(
                    st.friendship[owner] ?: 0) >= Relationships.FRIEND
            } else false
        } catch (e: Exception) {
            println("[mirror] render context feed failed: $e")
        }
    }

    fun wireView(v: GameView) {
        v.engine = engine
        v.state = state
        v.season = seasonFor(state.gameMonth)
        v.holiday = displayHoliday(state)
        refreshRenderContext(engine, state)
        v.generated = generatedHere()
        v.onTalkKid = { name -> hostTalk(name) }
        v.onTalkVisitor = { name -> hostTalk(name) }
        v.onExit = { locId -> hostMove(locId) }
        v.onCollect = { id ->
            state.collected.add(id)
            log("collect: $id")
        }
        v.onEnterInterior = {
            state.playerInside = true
            v.generated = generatedHere()
            log("inside ${engine.player(state).location}")
        }
        v.onExitInterior = {
            state.playerInside = false
            state.lifeText.remove("insideSection")
            v.generated = generatedHere()
            log("back outside")
        }
        v.onSearchTrash = { id -> log("trash searched: $id") }
        v.onPickupLitter = { id -> log("litter picked: $id") }
        v.onBubbleTap = { log("bubble tapped") }
        v.onSceneTap = { kind, id -> log("scene tap: $kind $id") }
        v.onSighting = { log("sighting!") }
        v.onBusStation = { id -> log("bus station: $id — pick a destination in the phone build; mirror logs the tap") }
        v.onSchoolBus = { log("school bus tapped") }
        v.onHouseDoor = { id -> log("house door: $id") }
        v.onSectionDoor = { id ->
            state.lifeText["insideSection"] = id
            state.playerInside = true
            log("section: $id")
        }
        v.onLivingFurniture = { id -> log("furniture: $id") }
        v.onBusRide = { log("bus ride complete") }
    }

    fun hostMove(locId: String) {
        val me = engine.player(state)
        me.location = locId
        LOCATIONS[locId]?.let { me.coords = it.gx to it.gy }
        me.destination = locId
        state.playerInside = false
        state.lifeText.remove("insideSection")
        view.generated = generatedHere()
        prefs.saveGame(state)
        log("→ $locId")
    }

    fun hostTalk(name: String) {
        val a = state.agents[name]
        val msg = if (a != null) {
            "${a.name} — ${a.activity}\n(at ${a.location})"
        } else "$name — a visitor passing through."
        log("talk: $name")
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(null, msg, name, JOptionPane.PLAIN_MESSAGE)
        }
    }

    fun log(msg: String) {
        println("[mirror] $msg")
        statusLabel?.text = msg
    }

    fun bootInteractive(dataDir: File) {
        boot(dataDir)
        val frame = JFrame("South Park Town — mirror (internal QA, v0.16.0d)")
        this.frame = frame
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        view = GameView(act)
        wireView(view)
        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val g2 = g as Graphics2D
                view.layout(0, 0, width, height)
                val bmp = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                view.draw(canvas)
                g2.drawImage(bmp.img, 0, 0, null)
            }
        }
        panel.preferredSize = Dimension(450, 800)
        panel.background = java.awt.Color.BLACK
        view.invalidateHook = Runnable { panel.repaint() }
        var downAt = 0L
        var downX = 0f
        var downY = 0f
        val mouse = object : MouseAdapter() {
            fun send(action: Int, e: MouseEvent) {
                val now = System.currentTimeMillis()
                val ev = MotionEvent.obtain(downAt, now, action, e.x.toFloat(), e.y.toFloat(), 0)
                view.onTouchEvent(ev)
                panel.repaint()
            }
            override fun mousePressed(e: MouseEvent) {
                downAt = System.currentTimeMillis(); downX = e.x.toFloat(); downY = e.y.toFloat()
                send(MotionEvent.ACTION_DOWN, e)
            }
            override fun mouseDragged(e: MouseEvent) { send(MotionEvent.ACTION_MOVE, e) }
            override fun mouseReleased(e: MouseEvent) { send(MotionEvent.ACTION_UP, e) }
        }
        panel.addMouseListener(mouse)
        panel.addMouseMotionListener(mouse)
        val status = JLabel("Mirror ready — ${engine.player(state).location}, " +
            "${state.timeMin / 60}:${"%02d".format(state.timeMin % 60)}", SwingConstants.LEFT)
        statusLabel = status
        // GameActivity parity: a 10-second world tick that advances the
        // engine one step and persists the save, exactly like the phone.
        // v0.16.0c: the tick is Engine.step — advanceTime alone dispatches
        // waypoints but never walks them (updatePositions is step's), so
        // the v0.16.0b host's town quietly never traveled; with step, the
        // mirror's cast walks its routes exactly as the phone's does.
        val tick = Timer(10_000) {
            try {
                engine.step(state)
                view.season = seasonFor(state.gameMonth)
                view.holiday = displayHoliday(state)
                refreshRenderContext(engine, state)
                view.generated = generatedHere()
                prefs.saveGame(state)
                prefs.saveNpcMemory()
                status.text = "${engine.player(state).location} — ${state.timeMin / 60}:" +
                    "%02d".format(state.timeMin % 60) + " — ${state.weatherId}"
            } catch (e: Exception) {
                println("[mirror] tick error: $e")
            }
            panel.repaint()
        }
        tick.start()
        val anim = Timer(50) { panel.repaint() }
        anim.start()
        frame.layout = BorderLayout()
        frame.add(panel, BorderLayout.CENTER)
        frame.add(status, BorderLayout.SOUTH)
        frame.pack()
        frame.isVisible = true
        view.attachForMirror()
        android.content.MirrorHooks.toaster = android.content.MirrorHooks.toaster
        android.content.MirrorHooks.activityLauncher =
            java.util.function.Consumer<Intent> { i ->
                log("startActivity → ${i.targetClass?.simpleName ?: i.action}")
            }
        log("Mirror up. Saves: ${File(dataDir, "prefs/sptown.properties")}")
        selfTestDir?.let { runSelfTest(it, dataDir) }
    }
}

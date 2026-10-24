package com.hunt3r.southparktown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

// ============================================================
// GameView — the town on screen.
//
// v0.15.0c (Lucky's visual-overhaul directive): the first-
// person POV is GONE. Every scene is a PAPER CUT-OUT stack
// (PaperScene owns the composition contract): sky → far
// plane → a street of buildings (the location's facade
// lined with PaperDressing's neighbor row — never one lone
// house) → the ground/sidewalk band → props → the y-sorted
// figures → traffic on the street strip at the very bottom
// → the near foreground. Slight parallax rides the far
// plane and the neighbor row against the camera.
//
// Movement is redone the same day: the New Kid STANDS IN
// the scene (LiveLook decides what they're wearing — day
// clothes, sleepwear on a live sleepover night, the hero
// costume in the patrol window). TAP acts by WALKING: tap
// a character, a door, a signpost, a prop, a bus station —
// the kid walks there (PaperWalk) and the interaction
// fires on arrival through the same callbacks as ever;
// tap open ground and the kid just walks there. The camera
// follows the kid; drag still pans. Interiors render as
// rooms (InteriorArt) with the same walk. Speech-bubble
// overlay + event ticker stay screen-space.
// ============================================================
class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var engine: Engine? = null
    var state: GameState? = null

    /** v0.9.0d: a bare PhotoView kept as a PAINTER — its
     *  drawInto renders a photo record into any rect, which
     *  is how the darkroom's print wall hangs the actual
     *  prints with the album's own code. Never attached. */
    private val photoRenderer by lazy { PhotoView(context) }
    private val prefs by lazy { Prefs(context) }

    /** v0.16.0d (stage 8): the current boil translation for
     *  the scene's painted backdrop — a seeded ±1px breathing
     *  on a 3-frame cycle (PaperCut owns the math). Zero when
     *  the Settings toggle is off or the cycle sits on its
     *  neutral frame. Applied to the backdrop ONLY: figures,
     *  faces and UI text never ride it. */
    private fun boilOffset(seed: Long): Pair<Float, Float> {
        if (!prefs.boilEnabled()) return 0f to 0f
        val o = PaperCut.boilOffset(seed,
            PaperCut.boilPhase(System.currentTimeMillis()))
        return o.x to o.y
    }

    /** v0.16.0d (stage 8): the diorama post pass, canvas
     *  edition. The background band's depth cue — a low-alpha
     *  grey wash over everything behind the figure pass — is
     *  the desaturation substitute for a depth-of-field blur
     *  (a real blur costs an offscreen pass per frame on a
     *  phone; the wash is two rects). The action band itself
     *  is never washed: figures paint after this and stay
     *  sharp and saturated. */
    private fun drawBackdropWash(c: Canvas, camX: Float, w: Float,
                                 h: Float, inside: Boolean) {
        val bgBot = if (inside) h * 0.64f
            else h * FacadeGeom.GROUND_LINE
        c.drawRect(camX, 0f, camX + w, bgBot,
            Paper.fillPaint("#7d838e", PaperCut.BG_WASH_ALPHA))
    }

    /** The foreground half of the post pass, drawn over the
     *  finished frame: a whisper of a wash on the near strip,
     *  the stepped vignette, and the fibrous film grain. */
    private fun drawPostPass(c: Canvas, camX: Float, w: Float,
                             h: Float, inside: Boolean, seed: Long) {
        val fgTop = if (inside) h * 0.86f else h * Depth.FG_TOP_OUT
        c.drawRect(camX, fgTop, camX + w, h,
            Paper.fillPaint("#7d838e", PaperCut.FG_WASH_ALPHA))
        Paper.drawVignette(c, camX, 0f, camX + w, h)
        Paper.drawFibreGrain(c, camX, 0f, camX + w, h, seed + 101L)
    }
    var season: Season = Season.WINTER
    var holiday: Holiday? = null
    /** Generated lorebook NPCs (visitors + residents) in this exact space. */
    var generated: List<ActiveNpc> = emptyList()

    var onTalkKid: (String) -> Unit = {}
    var onTalkVisitor: (String) -> Unit = {}
    var onExit: (String) -> Unit = {}
    var onCollect: (String) -> Unit = {}
    var onEnterInterior: () -> Unit = {}
    var onExitInterior: () -> Unit = {}
    var onSearchTrash: (String) -> Unit = {}
    var onPickupLitter: (String) -> Unit = {}
    var onBubbleTap: () -> Unit = {}
    /** v0.1.8 Phase 2: living-world props (pond, gnome, bench, …). */
    var onSceneTap: (String, String) -> Unit = { _, _ -> }
    /** v0.1.9a #98: fired when an autonomous 3:33 AM crossing finishes. */
    var onSighting: () -> Unit = {}
    /** v0.15.0c: the public bus — fired after the player
     *  walks to a station and the bus pulls up (the beat
     *  plays first; the destination picker opens after). */
    var onBusStation: (String) -> Unit = {}
    /** v0.15.0c: the school bus — fired after the player
     *  walks to the waiting school bus and it pulls up. */
    var onSchoolBus: () -> Unit = {}
    /** v0.16.0a (item 20): a named neighbor house's door. */
    var onHouseDoor: (String) -> Unit = {}
    /** v0.16.0a (item 20): a street section house's door. */
    var onSectionDoor: (String) -> Unit = {}
    /** v0.16.0a (items 7+17): usable furniture, tapped. */
    var onLivingFurniture: (String) -> Unit = {}
    /** v0.16.0e pass 2: a fabricated fiction door
     *  (cardboard, inside a claimed building) was tapped. */
    var onFictionDoor: (String) -> Unit = {}
    /** The fiction phase as of the last frame — read by
     *  the costume overlay + the fabricated doors. */
    var fictionPhaseNow: Fiction.Phase = Fiction.Phase.REAL

    /** v0.16.0e pass 2: the costume the fiction gives one
     *  figure in the current phase — FULL believers (and
     *  every kid, once LIVE) wear the cape, PARTIAL wears
     *  the towel; DRESSING costumes only the kids (they
     *  are already in character), COLLAPSING sheds the
     *  cape back to the towel. One resolver for the
     *  standing crowd and the walking cast alike. */
    fun fictionCostumeNow(def: Fiction.Def, name: String,
                          isKid: Boolean): Fiction.Costume =
        when (fictionPhaseNow) {
            Fiction.Phase.LIVE, Fiction.Phase.ESCALATING ->
                Fiction.costumeOf(def.id, name, isKid)
            Fiction.Phase.DRESSING ->
                if (isKid) Fiction.Costume.FULL
                else Fiction.Costume.NONE
            Fiction.Phase.COLLAPSING ->
                if (Fiction.costumeOf(def.id, name, isKid) ==
                    Fiction.Costume.FULL)
                    Fiction.Costume.PARTIAL
                else Fiction.Costume.NONE
            else -> Fiction.Costume.NONE
        }

    /** v0.16.0a (items 8+9): the bus route board — ride. */
    var onBusRide: () -> Unit = {}

    // Phase 2 transient visual state (not saved — pure stage dressing)
    var duckScatterUntil: Long = 0
    var lingerName: String? = null
    var lingerUntilTick: Long = 0
    // v0.4.0e: the greeting-bark bubble — one NPC's hello over
    // their head for ~2 s, set by GameActivity via showBark
    var barkName: String? = null
        private set
    var barkText: String = ""
        private set
    private var barkUntilMs: Long = 0

    fun showBark(name: String, text: String, durMs: Long) {
        barkName = name
        barkText = text
        barkUntilMs = System.currentTimeMillis() + durMs
    }
    var sitting: Boolean = false
    // ---- v0.16.0a (item 17): the player's action-animation
    // state — a current ActionAnims id playing over the
    // base figure, and the sit/stand transition clock, so
    // sitting down and getting up are ANIMATED (an eased
    // descent/ascent over the animation's ticks), never a
    // snap into the seated pose ----
    var playerAction: String = ""
        private set
    private var playerActionUntilTick: Long = -1
    private var sitAnimMode: Int = 0   // +1 sitting down, -1 standing up
    private var sitAnimStartTick: Long = -1

    fun playPlayerAction(id: String) {
        playerAction = id
        playerActionUntilTick = tick +
            (ActionAnims.byId(id)?.ticks ?: 10)
    }

    fun beginSit() {
        sitting = true
        sitAnimMode = 1
        sitAnimStartTick = tick
        playPlayerAction("sit_down")
    }

    fun beginStand() {
        sitting = false
        sitAnimMode = -1
        sitAnimStartTick = tick
        playPlayerAction("stand_up")
    }

    fun currentPlayerAction(): String =
        if (tick < playerActionUntilTick) playerAction else ""

    private val trailPrev = mutableMapOf<String, Triple<String, Float, Long>>()
    private var lastCamX = 0f
    // v0.1.9a #97 fan-club trailing state (per location visit)
    private var fanLoc: String? = null
    private var fanArriveTick: Long = 0
    private var fanScatterTick: Long = -10000
    // v0.1.9a #98 sighting state (one crossing per night)
    private var sightingDay: Int = -1
    private var sightingStartTick: Long = -1

    /** Bottom edge (px, view coords) of the Activity's top chrome.
     *  Set by GameActivity after layout; -1 = not measured yet. */
    var topChromePx: Float = -1f

    private fun chromeTop(): Float =
        if (topChromePx >= 0f) topChromePx
        else resources.displayMetrics.density * 124f

    private var tick: Long = 0
    private val ticker = object : Runnable {
        override fun run() {
            if (!isAttachedToWindow) return
            tick++
            stepPlayerWalk()
            invalidate()
            postDelayed(this, 50)
        }
    }

    // ---- v0.15.0c: the player in the scene (PaperWalk) ----
    // Transient stage state, never saved: where the kid
    // stands, where they're walking to, and what fires when
    // they get there. Reset on every location/interior flip.
    private var pwalk = PaperWalk.State()
    private var walkLocKey: String? = null
    private var lastStepMs = 0L
    // v0.16.0e (spec B3+B4): the player's gait rate for the
    // current scene — his own leg geometry at his drawn
    // size, recomputed by the figure pass each frame and
    // read by the walk loop — plus the ground truth the
    // footstep notifies fire on (snow underfoot, indoors),
    // the traveler's last phases (notify crossings are
    // deltas), and the player's live snow puffs.
    private var playerPhasePerFrac = 22f
    private var stepSnow = false
    private var stepInside = false
    private data class StepPuff(val x: Float, val feetY: Float,
                                val bornTick: Long)
    private val stepPuffs = mutableListOf<StepPuff>()
    private val travelerPhase = HashMap<String, Float>()
    private var lastStepSfxMs = 0L
    private var worldWCache = 0f
    private var viewHCache = 0f
    // the bus beat: after you arrive at a station the bus
    // pulls up (drawn sliding in on the street) before the
    // callback fires and the picker/ride opens
    private var busBeatKind: String? = null
    private var busBeatId: String = ""
    private var busBeatUntilTick = -1L
    private var busShowUntilTick = -1L
    private var busShowSchool = false

    /** GameActivity calls this after a ride lands: the bus
     *  stands at the far station a moment while you step
     *  off. */
    fun showBusArrival(school: Boolean) {
        busShowUntilTick = tick + 48
        busShowSchool = school
    }

    private fun stepPlayerWalk() {
        val nowMs = System.currentTimeMillis()
        if (lastStepMs == 0L) { lastStepMs = nowMs; return }
        val dt = ((nowMs - lastStepMs) / 1000f).coerceIn(0f, 0.2f)
        lastStepMs = nowMs
        val beat = busBeatKind
        if (beat != null) {
            if (tick >= busBeatUntilTick) {
                busBeatKind = null
                when (beat) {
                    "busstation" -> onBusStation(busBeatId)
                    "schoolbus" -> onSchoolBus()
                }
            }
            return
        }
        if (!pwalk.walking) return
        val prevPhase = pwalk.phase
        val (ns, fired) = PaperWalk.step(pwalk, dt, playerPhasePerFrac)
        pwalk = ns
        // v0.16.0e (spec B4): the foot-down NOTIFIES — the
        // only write-back animation gets. Each phase
        // crossing the controller's distance just earned
        // fires one step sound, and in snow leaves a puff
        // at the foot. No timers: stop walking and both
        // stop, mid-stride, exactly as the contract says.
        for (k in Gait.footDowns(prevPhase, ns.phase)) {
            SoundEngine.footstep(stepSnow, stepInside, k)
            if (stepSnow && worldWCache > 0f && viewHCache > 0f) {
                stepPuffs.add(StepPuff(pwalk.x * worldWCache,
                    WalkBand.feetY(pwalk.band, viewHCache, stepInside),
                    tick))
            }
        }
        if (pwalk.walking && worldWCache > 0f && width > 0) {
            val maxCam = worldWCache - width.toFloat()
            camTargetX = (pwalk.x * worldWCache - width / 2f)
                .coerceIn(0f, maxCam)
        }
        if (fired != null) firePending(fired)
    }

    /** An arrived walk's interaction, fired through the
     *  same callbacks the tap used to fire directly. The
     *  two bus kinds play their pull-up beat first. */
    private fun firePending(p: PaperWalk.Pending) {
        when (p.kind) {
            "kid" -> onTalkKid(p.id)
            "visitor" -> onTalkVisitor(p.id)
            "exit" -> onExit(p.id)
            "spawn" -> onCollect(p.id)
            "door" -> onEnterInterior()
            "exitdoor" -> onExitInterior()
            "trash" -> onSearchTrash(p.id)
            "litter" -> onPickupLitter(p.id)
            "busstation" -> {
                busBeatKind = "busstation"; busBeatId = p.id
                busBeatUntilTick = tick + 26
                busShowUntilTick = tick + 26 + 44
                busShowSchool = false
            }
            "schoolbus" -> {
                busBeatKind = "schoolbus"; busBeatId = ""
                busBeatUntilTick = tick + 26
                busShowUntilTick = tick + 26 + 44
                busShowSchool = true
            }
            "housedoor" -> onHouseDoor(p.id)
            "sectiondoor" -> onSectionDoor(p.id)
            "livingfurniture" -> onLivingFurniture(p.id)
            "fictiondoor" -> onFictionDoor(p.id)
            "busride" -> onBusRide()
            else -> onSceneTap(p.kind, p.id)
        }
    }

    /** The ground-plane band (0 = back by the buildings,
     *  1 = the street edge) for a screen/world y.
     *  v0.15.0d: WalkBand owns the band — its top is the
     *  walkable band's top line, so a tap can never aim
     *  the kid up onto a wall or a facade again. */
    private fun bandForY(y: Float, inside: Boolean): Float {
        val h = if (viewHCache > 0f) viewHCache else height.toFloat()
        return WalkBand.bandForY(y, h, inside)
    }

    private data class Hit(val kind: String, val id: String, val rect: RectF)
    private val hits = mutableListOf<Hit>()
    private var bubbleRect: RectF? = null

    // ---- camera (the world is SceneScale.factor x the
    // screen width — v0.15.0g widened every scene 2–3×;
    // the old WORLD_SCALE = 1.7 constant is retired into
    // SceneScale.OLD_FACTOR, where the composition tables
    // still read it as their unit width) ----
    private var camX = 0f
    private var camTargetX = 0f
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var dragging = false

    companion object {
        /** Where the standing bus centers at a station,
         *  as a world fraction — the station cluster's
         *  heart. FigureCompose's forbidden span derives
         *  from this same number, so the bus the painter
         *  draws and the ground the crowd avoids are one
         *  bus. The sign stands west of it (PaperDressing
         *  .SIGN_FX), the shelter at the world center. */
        const val BUS_FX = 0.55f
        private val SEAT_THEMES = setOf("classroom", "cafe", "restaurant", "bar",
            "church", "theater", "diner", "home", "bus", "mall", "loft", "hut")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) post(ticker) else removeCallbacks(ticker)
    }

    fun refresh() {
        invalidate()
    }

    /** Phase 2: send the pond ducks scattering for ~2.5 seconds. */
    fun scatterDucks() {
        duckScatterUntil = tick + 50
    }

    /** Center the camera on a world x (used when arriving somewhere new). */
    fun centerCamera() {
        camTargetX = Float.MAX_VALUE // clamped in onDraw to world center
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val st = state ?: return
        val eng = engine ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        hits.clear()
        bubbleRect = null

        val me = eng.player(st)
        val loc = LOCATIONS[me.location] ?: return
        val inside = st.playerInside
        val seed = loc.id.hashCode().toLong() * 31 + st.todayKey.hashCode()
        // v0.15.0g: the world is 2–3× wider than the town
        // was composed at — SceneScale owns the factor
        // (interiors 2×, streets 2.5×, open land 3× the
        // old 1.7-screen world); everything downstream
        // (camera, parallax, composition) is generic over
        // worldW and simply runs over the wider world.
        val worldW = w * SceneScale.factor(loc, inside)
        val maxCam = worldW - w
        if (camTargetX == Float.MAX_VALUE) camTargetX = maxCam / 2f
        camTargetX = camTargetX.coerceIn(0f, maxCam)
        camX += (camTargetX - camX) * 0.3f
        if (abs(camTargetX - camX) < 1f) camX = camTargetX
        camX = camX.coerceIn(0f, maxCam)
        worldWCache = worldW
        viewHCache = h
        // v0.15.0c: arriving somewhere new puts the kid at
        // the door, standing on the band — the walk state
        // belongs to the place, and a new place restarts it
        val locKey = loc.id + if (inside) ":in" else ":out"
        if (walkLocKey != locKey) {
            walkLocKey = locKey
            val spawnX = if (inside) {
                val dr = InteriorArt.doorRect(worldW, h)
                (dr.centerX() / worldW).coerceIn(0.05f, 0.95f)
            } else 0.5f
            pwalk = PaperWalk.State(x = spawnX, band = 0.62f,
                tx = spawnX, tband = 0.62f)
            busBeatKind = null
            // v0.16.0e: the notify bookkeeping belongs to
            // the place too — a new scene starts its
            // travelers' phases and the player's puffs fresh
            travelerPhase.clear()
            stepPuffs.clear()
        }

        // v0.1.9a #97: the fan club trails one stop behind — a new
        // location restarts their creep-in, and a fast pan reads
        // as "you turned around": they scatter.
        if (fanLoc != loc.id) {
            fanLoc = loc.id
            fanArriveTick = tick
        }
        if (abs(camX - lastCamX) > 7f && lastCamX != 0f) fanScatterTick = tick

        val night = SceneArt.timeOfDay(st.timeMin) == "night"
        // v0.16.0e pass 2: hand the painters the fiction's
        // current dressing set (base + active diffs is
        // all the renderer ever sees); the phase rides
        // along for costumes + the fabricated doors
        SceneArt.fictionDress = Fiction.dressNow(st)
        SceneArt.fictionDef = Fiction.activeDef(st)
        fictionPhaseNow = Fiction.phaseOf(st)
        val speakers = st.lastConversation.map { it.speaker }.toSet()
        // v0.1.9f accessibility: the system font scale, routed
        // through Chrome (clamped; geometry only grows). At the
        // default scale of 1.0 every overlay is byte-identical.
        val fs = Chrome.fontScaleOf(resources.configuration.fontScale)

        c.save()
        c.translate(-camX, 0f)

        // ---- background ----
        if (inside) {
            // v0.16.0a: three kinds of "inside" — a bus you
            // are riding (items 8+9), one house section of
            // a residential street (item 20), or the
            // location's own interior. The first two paint
            // their own room + layout and skip the
            // location's fixtures whole.
            val rideNow = BusRideState.read(st)
            val sectionNow = st.lifeText["insideSection"]
            val def = if (rideNow != null) interiorOf("BusStop")
                else interiorOf(loc.id)
            if (def != null) {
                // v0.16.0d (stage 8): the room's painted
                // backdrop boils with the post pass (see
                // boilOffset) — the room only, never the
                // figures who stand in it
                val boilIn = boilOffset(seed)
                c.translate(boilIn.first, boilIn.second)
                InteriorArt.drawRoom(c, worldW.toInt(), height, def, st.timeMin,
                    st.weatherId, seed, holiday)
                c.translate(-boilIn.first, -boilIn.second)
                if (rideNow != null) {
                    drawInteriorLayout(c,
                        InteriorLayouts.busLayout(rideNow.school),
                        worldW, h, rideNow)
                } else if (sectionNow != null) {
                    drawInteriorLayout(c,
                        InteriorLayouts.forSection(sectionNow,
                            ResidentialStreets.sectionName(sectionNow)),
                        worldW, h, null)
                } else {
                // #45: home interiors dress for the resident's block
                if (def.theme == "home") drawHomeLifeProps(c, st, eng, loc.id, worldW, h)
                drawLivingInterior(c, st, loc.id, worldW, h)
                // v0.2.0e: the Taco Catch cabinet stands inside
                // Freeman's Tacos, marquee lit, waiting on quarters
                // it will never get (it pays YOU).
                if (loc.id == Minigames.TACO_LOC) drawArcadeCabinet(c, worldW, h)
                // v0.3.0d: inside YOUR house, the four working
                // fixtures — bed, trophy shelf, album book,
                // mirror. Working furniture, not set dressing.
                if (loc.id == YOUR_HOUSE) drawHouseFixtures(c, st, worldW, h)
                // v0.4.0b: inside City Hall, the request board —
                // McDaniels' cork board of small town work,
                // pinned fresh every morning (Errands.boardFor).
                if (loc.id == "CityHall") {
                    drawErrandBoard(c, st, worldW, h)
                    // v0.6.0f: and under it, the ballot box —
                    // this week's town question lives here.
                    drawBallotBox(c, st, worldW, h)
                    // v0.15.0f: and on the hall's long wall,
                    // the gallery show — this IS the Community
                    // Hall (the town's hall at City Hall), and
                    // its exhibition hangs left of the board.
                    drawGalleryWall(c, st, worldW, h)
                }
                // v0.7.0c: inside the Bijou, the screen itself
                // — silver on live nights, honest when dark.
                if (loc.id == "Bijou") drawMovieScreen(c, st, worldW, h)
                // v0.7.0d: inside the Elementary, the classroom
                // board — the day's lesson, the bee on Fridays,
                // quiet days honestly quiet (School owns it).
                if (loc.id == "Elementary") drawClassroomBoard(c, st, worldW, h)
                // v0.5.0a: inside the Mall, the transit kiosk —
                // the one screen in town that sells the bus pass
                // (Walk.buyPass owns the rules; this just glows).
                if (loc.id == "Mall") drawTransitKiosk(c, st, worldW, h)
                // v0.5.0b: the wardrobe counters — the Mall's
                // clothing rack (left wall) and Jimbo's counter
                // sell their own unlockable pieces for capsule
                // coins (Wardrobe.buy owns the rules).
                if (loc.id == "Mall") drawWardrobeRack(c, st, worldW, h)
                // v0.5.0c: the capsule machine — center wall,
                // between the clothing rack and the transit
                // kiosk. It sells Pocket Pal pulls for capsule
                // coins (PocketPals.pull owns the rules).
                if (loc.id == "Mall") drawCapsuleMachine(c, st, worldW, h)
                // v0.12.0b: the Mall's two walk-to counters
                // in the ballot box's second-row band — the
                // garden centre's seed rack (left wall,
                // under the clothes rack) and the snack
                // shop's general-goods counter (right wall,
                // under the transit kiosk). Shop owns both
                // tills; the fixtures just stand there.
                if (loc.id == "Mall") drawSeedRack(c, st, worldW, h)
                if (loc.id == "Mall") drawSnackCounter(c, st, worldW, h)
                if (loc.id == "JimbosGuns") drawGunRack(c, st, worldW, h)
                // v0.16.0a (items 3–7): the location's own
                // multi-room layout — partitions, room
                // labels and the usable furniture row
                drawInteriorLayout(c,
                    InteriorLayouts.forLocation(loc.id),
                    worldW, h, null)
                }
            }
        } else {
            // v0.16.0d (stage 8): the scene's painted backdrop
            // boils (see boilOffset) — the backdrop only,
            // never the figures who stand in front of it
            val boilOut = boilOffset(seed)
            c.translate(boilOut.first, boilOut.second)
            SceneArt.drawScene(c, worldW.toInt(), height, loc, st.weatherId,
                st.timeMin, seed, season, holiday, camX)
            c.translate(-boilOut.first, -boilOut.second)
            // shenanigans scene tint
            val tint = Quests.activeDef(st)?.tintHex
            if (tint != null) c.drawRect(0f, 0f, worldW, h, Paper.fillPaint(tint, 30))
            // critters (outdoors only) — v0.16.0a (items
            // 12+14+15): they WANDER now, each at its own
            // species pace (MotionRegistry.forSpecies),
            // flipping to face its travel direction, wearing
            // the animal expression the hour suggests
            val rnd = Random(seed + 7)
            val critterMood = when {
                night -> "sleepy"
                st.timeMin in 360..540 -> "hungry"
                else -> "calm"
            }
            var ci = 0
            for (a in eng.animalsAt(st, me.location)) {
                val baseAx = worldW * (0.08f + rnd.nextFloat() * 0.84f)
                val ay = h * (0.70f + rnd.nextFloat() * 0.22f)
                val sm = MotionRegistry.forSpecies(a.species)
                val drift = kotlin.math.sin(
                    System.currentTimeMillis() / (2600f / sm.gaitHz) +
                        ci * 1.93f + sm.phase) * worldW * 0.018f
                val hop = kotlin.math.abs(kotlin.math.sin(
                    System.currentTimeMillis() / 300f + ci * 2.1f)) *
                    h * 0.006f * (sm.bobU)
                val faceLeft = kotlin.math.cos(
                    System.currentTimeMillis() / (2600f / sm.gaitHz) +
                        ci * 1.93f + sm.phase) < 0
                val ax = (baseAx + drift).coerceIn(
                    worldW * 0.04f, worldW * 0.96f)
                if (faceLeft) {
                    c.save()
                    c.translate(ax, 0f); c.scale(-1f, 1f); c.translate(-ax, 0f)
                    SceneArt.drawCritter(c, a.species, ax, ay - hop, 1.6f,
                        night, if (ci % 3 == 0) "alert" else critterMood)
                    c.restore()
                } else {
                    SceneArt.drawCritter(c, a.species, ax, ay - hop, 1.6f,
                        night, if (ci % 3 == 0) "alert" else critterMood)
                }
                ci++
            }
        }

        // ---- v0.16.0d (stage 8): the post pass's background
        // band wash — everything painted so far is behind the
        // action; the figure pass below stays unwashed ----
        drawBackdropWash(c, camX, w, h, inside)

        // ---- collectible spawns ----
        drawSpawns(c, st, worldW, h, seed)

        // ---- trash containers + litter ----
        drawTrash(c, st, loc.id, inside, worldW, h, seed)

        // ---- v0.1.8 Phase 2: the living world, outdoors ----
        if (!inside) {
            drawLivingOutdoor(c, st, eng, loc, worldW, h, seed)
            drawLivingProps(c, st, eng, loc, worldW, h, seed)
            // v0.15.0d: the dressing table's standing props,
            // at world scale, on PaperDressing's own spots
            drawStandingProps(c, loc, worldW, h, night)
            // v0.13.0d: 🎺 the parade's reviewing stand on
            // Main Street through its cycle (poster week to
            // the day itself)
            if (loc.id == "MainStreet") {
                drawParadeStand(c, st, worldW, h)
            }
            // v0.15.0c: the bus station — the show-style
            // sign stands at every BusNetwork stop (bigger
            // at the Bus Stop itself, the landmark). Tap it
            // and you walk to it; the bus does the rest.
            val dressingNow = PaperDressing.forLocation(loc)
            if (dressingNow.busStation) {
                // v0.15.0g: the sign stands at the station
                // cluster (PaperDressing.SIGN_FX), west of
                // the standing bus and clear of its body —
                // the 0.16 it stood at in the narrow world
                // is down the widened street now, far from
                // the shelter, the bus and the crowd.
                val sx = worldW * PaperDressing.SIGN_FX
                val sBase = h * 0.715f
                // v0.15.0d: sign size off the ladder — the
                // painter's board unit s stacks post (1.55s)
                // + board (1.0s), so s = SIGN_TOP / 2.55 in
                // kid units (other stations a breath smaller)
                val sSize = WorldScale.units(WorldScale.SIGN_TOP_U, h) /
                    2.55f * (if (loc.id == "BusStop") 1f else 0.85f)
                SceneArt.drawBusStopSign(c, sx, sBase, sSize, night)
                hits.add(Hit("busstation", loc.id,
                    RectF(sx - sSize * 0.95f, sBase - sSize * 2.75f,
                        sx + sSize * 0.95f, sBase + 8f)))
            }
        }

        // ---- v0.16.0a (item 20): the street's other
        // houses are real doors — every neighbor house on
        // a house scene gets a door hit on the door the
        // painter drew (ResidentialStreets owns the
        // geometry): named houses lead to their own
        // interiors, section houses to their own section --
        if (!inside && loc.kind == "house") {
            val doorH = WorldScale.doorPx(h)
            val baseY = h * FacadeGeom.GROUND_LINE
            for (sh in ResidentialStreets.doorHouses(loc)) {
                if (sh.seqIndex < 0) continue // the facade's own door is drawEntryDoor's
                val cxh = sh.centerFrac * worldW
                val kindH = if (sh.targetLocId.isNotEmpty())
                    "housedoor" else "sectiondoor"
                val idH = if (sh.targetLocId.isNotEmpty())
                    sh.targetLocId else sh.sectionId
                hits.add(Hit(kindH, idH,
                    RectF(cxh - doorH * 0.16f, baseY - doorH,
                        cxh + doorH * 0.16f, baseY + 6f)))
            }
        }

        // ---- figures: generated NPCs (background) then cast ----
        drawFigures(c, st, eng, loc, inside, worldW, h, seed, speakers, fs)

        // ---- doors ----
        if (inside) {
            val dr = InteriorArt.doorRect(worldW, h)
            val pulse = 120 + (60 * kotlin.math.sin(tick * 0.3f)).toInt()
            c.drawRect(dr, Paper.strokePaint(Paper.YELLOW, 3f, pulse.coerceIn(40, 200)))
            hits.add(Hit("exitdoor", loc.id,
                RectF(dr.left - 14, dr.top - 14, dr.right + 14, dr.bottom + 14)))
        } else {
            drawEntryDoor(c, loc, worldW, h)
        }

        // ---- v0.5.0g: the FOREGROUND band (the scene depth
        // pass, Depth.kt owns the bands): the near strip —
        // fence tops, the snowbank lip, the counter front —
        // drawn OVER the figures' feet, in the bottom strip
        // only, so depth reads without ever covering a face
        // or a tap target. The signposts stand in the same
        // band and paint after it, so their boards and hints
        // stay legible over the frieze. ----
        if (inside) {
            // v0.16.0a: aboard a bus, the foreground is the
            // bus's own seat backs, whatever station the
            // bus was boarded at
            val fgDef = if (BusRideState.active(st))
                interiorOf("BusStop") else interiorOf(loc.id)
            if (fgDef != null) InteriorArt.drawForegroundLayer(c, worldW.toInt(),
                height, fgDef, st.timeMin, st.weatherId, seed)
        } else {
            SceneArt.drawForegroundLayer(c, worldW.toInt(), height, loc,
                st.weatherId, st.timeMin, seed)
        }

        // ---- v0.15.0c: TRAFFIC + the buses (Depth's
        // traffic layer, FOREGROUND): cars ride the street
        // strip in front of the crowd; a waiting school bus
        // and a pulling-up network bus stand on the road ----
        if (!inside) drawTraffic(c, st, loc, worldW, h, seed)

        // ---- travel signposts (outside only) ----
        if (!inside) drawSigns(c, loc, worldW, h)

        // ---- v0.15.0c: the walk target — a paper ring on
        // the ground where the kid is headed ----
        if (pwalk.walking) {
            val mx = pwalk.tx * worldW
            val my = WalkBand.feetY(pwalk.tband, h, inside)
            val pulse = 1f + 0.15f * kotlin.math.sin(tick * 0.35f).toFloat()
            val rr = h * 0.016f * pulse
            c.drawOval(RectF(mx - rr, my - rr * 0.38f, mx + rr, my + rr * 0.38f),
                Paper.strokePaint(Paper.YELLOW, 3f, 210))
            c.drawCircle(mx, my, 2.6f, Paper.fillPaint("#ffffff", 220))
        }

        // ---- weather + holiday particles (outside only) ----
        if (!inside) {
            SceneArt.drawWeatherParticles(c, worldW.toInt(), height, st.weatherId, tick, night)
            SceneArt.drawHolidayParticles(c, worldW.toInt(), height, holiday, tick)
        }

        // ---- v0.16.0e (spec B4): the player's snow puffs —
        // born on foot-down notifies in the walk loop, each
        // living seven view ticks and fading as it spreads.
        // Travelers and wanderers draw their own from the
        // cycle phase directly (Gait.puffIntensity) in the
        // figure pass; these are the player's, which have a
        // real update loop to be born in. ----
        if (stepPuffs.isNotEmpty()) {
            val alive = stepPuffs.filter { tick - it.bornTick <= 7L }
            stepPuffs.clear()
            stepPuffs.addAll(alive)
            for (p in stepPuffs) {
                val age = (tick - p.bornTick) / 7f
                val rr = h * 0.020f * (0.6f + age * 0.9f)
                c.drawOval(RectF(p.x - rr, p.feetY - rr * 0.30f,
                    p.x + rr, p.feetY + rr * 0.30f),
                    Paper.fillPaint("#ffffff",
                        (95f * (1f - age)).toInt().coerceIn(0, 255)))
            }
        }

        // ---- v0.16.0d (stage 8): the post pass's foreground
        // half — near-strip wash, vignette, fibrous grain —
        // over the finished frame, under the screen-space UI ----
        drawPostPass(c, camX, w, h, inside, seed)

        c.restore()

        // ---- screen space: event strip + speech bubble, stacked
        // below the Activity chrome (disjoint regions by construction)
        drawEventStrip(c, w, st, fs)
        drawBubbleOverlay(c, w, h, st, fs)
        // Phase 2: edge-of-world life cues + the sitting vignette
        if (!inside) drawEdgeIcons(c, w, h, st, loc)
        if (sitting) {
            val sp = Paper.textPaint("#f4ecd8", 13f * fs, bold = true, casual = true)
            sp.textAlign = Paint.Align.CENTER
            c.drawText("🪑 Sitting a spell — the town breathes around you.",
                w / 2f, h * 0.965f, sp)
        }
        lastCamX = camX
    }

    /** v0.15.0c: the traffic plane. Passing cars ride the
     *  street strip on two lanes (near lane left-to-right,
     *  far lane right-to-left, a touch smaller and higher);
     *  the buses stand at the station point on the road —
     *  the school bus on its windows (tappable), a network
     *  bus while its pull-up beat plays or just after it
     *  delivers you. */
    private fun drawTraffic(c: Canvas, st: GameState, loc: Location,
                            worldW: Float, h: Float, seed: Long) {
        val dressing = PaperDressing.forLocation(loc)
        val night = SceneArt.timeOfDay(st.timeMin) == "night"
        val schoolHere = SchoolBus.busAt(st, loc.id)
        val showHere = tick < busShowUntilTick && dressing.busStation
        if (schoolHere || showHere) {
            val school = schoolHere || busShowSchool
            // v0.15.0d: the bus at WORLD SCALE — BUS_LEN kid
            // units long, BUS_H tall, standing centered on
            // the stop; its hit rect IS its drawn body, so
            // a tap anywhere on the bus boards it
            // v0.15.0g: the bus stands IN the far lane of
            // the real road (base 0.958), pulled up to the
            // curb the riders wait on — and FigureCompose
            // holds every waiting body clear of this exact
            // span (BUS_FX), so it can never cover one.
            val busW = WorldScale.units(WorldScale.BUS_LEN_U, h)
            val busH = WorldScale.units(WorldScale.BUS_H_U, h)
            val stationX = worldW * BUS_FX
            var bx = stationX
            if (busBeatKind != null) {
                // pulling up: ease in from off the left edge
                val f = 1f - ((busBeatUntilTick - tick).toFloat() / 26f)
                    .coerceIn(0f, 1f)
                val e = 1f - (1f - f) * (1f - f)
                bx = (worldW * -0.20f) + (stationX - worldW * -0.20f) * e
            }
            SceneArt.drawBusVehicle(c, bx, h * 0.958f, busW, night, school)
            if (schoolHere) {
                hits.add(Hit("schoolbus", loc.id,
                    RectF(bx - busW / 2f - 6f, h * 0.958f - busH,
                        bx + busW / 2f + 6f, h * 0.982f)))
            }
        }
        // v0.16.0a (item 2): the route pass that replaces
        // the retired #84 sweep — the school bus crosses
        // BusStop / MainStreet in its run windows, on the
        // ROAD at the standing bus's own lane line
        // (h * 0.958, inside WalkBand's road band), never
        // on grass, never in the walk band
        if (loc.id in setOf("BusStop", "MainStreet") &&
            (st.timeMin in 462..497 || st.timeMin in 887..922)) {
            val sweep = ((System.currentTimeMillis() / 55) % 140) / 140f
            val busW2 = WorldScale.units(WorldScale.BUS_LEN_U, h)
            val bx2 = -worldW * 0.18f + sweep * worldW * 1.36f
            SceneArt.drawBusVehicle(c, bx2, h * 0.958f, busW2, night, true)
        }
        if (!dressing.traffic) return
        val carCols = listOf("#c0392b", "#2a6fd4", "#e8c53a",
            "#7a4a9a", "#3a9a5a", "#d8702a")
        // v0.15.0g: the ambient rate is Traffic's pure
        // derivation now — a car now and then on a quiet
        // street (≈2.1 expected passes per scene-minute,
        // 0.30× the old inline 340-tick loop, which is
        // retired with it), driving ON the road band:
        // the far lane high in the band, the near lane
        // low, both clear of the sidewalk's edge.
        for (k in 0..1) {
            val prog = Traffic.progress(tick, k) ?: continue
            val dirRight = Traffic.dirRight(k)
            val fx = if (dirRight) -0.08f + prog * 1.16f
                else 1.08f - prog * 1.16f
            val laneY = if (k == 0) h * 0.945f else h * 0.982f
            // v0.15.0d: cars at world scale (CAR_LEN off the
            // ladder; the far lane a breath smaller, depth)
            val carW = WorldScale.units(WorldScale.CAR_LEN_U, h) *
                (if (k == 0) 0.88f else 1f)
            val slot = (tick + k * Traffic.LANE_GAP) / Traffic.PERIOD_TICKS
            val rawIdx = (seed + k * 7 + slot) % carCols.size
            val colIdx = ((rawIdx % carCols.size) + carCols.size).toInt() %
                carCols.size
            SceneArt.drawPaperCar(c, fx * worldW, laneY, carW,
                carCols[colIdx], night, if (dirRight) 1 else -1)
        }
    }

    /** v0.16.0a (items 3–9 + 20): paint one
     *  InteriorLayouts layout — the room partitions (with
     *  door gaps) across the widened interior world, a
     *  label per room, and every furniture piece at its
     *  ladder size, back pieces against the wall and floor
     *  pieces standing in the room. Every piece is a tap
     *  target: furniture is usable, not decoration. On a
     *  bus, the route board is the alight target. */
    private fun drawInteriorLayout(c: Canvas,
                                   layout: InteriorLayouts.Layout,
                                   worldW: Float, h: Float,
                                   ride: BusRideState.Ride?) {
        val wallBot = h * 0.46f
        val labelP = Paper.textPaint("#f4ead2", 13f * 1f, bold = true)
        labelP.textAlign = Paint.Align.CENTER
        // room partitions + labels
        val partP = Paper.fillPaint("#000000", 46)
        for (room in layout.rooms) {
            if (room.x0 > 0.001f) {
                val px = room.x0 * worldW
                // partition with a door gap in its lower third
                c.drawRect(px - 3f, wallBot - h * 0.16f, px + 3f, h * 0.60f, partP)
                c.drawRect(px - 3f, h * 0.78f, px + 3f, h * 0.97f, partP)
            }
            val cxm = (room.x0 + room.x1) / 2f * worldW
            c.drawText(room.label.uppercase(), cxm, h * 0.135f, labelP)
        }
        // the layout's title, small, at the room's heart
        val titleP = Paper.textPaint("#ffffff", 12f * 1f)
        titleP.textAlign = Paint.Align.CENTER
        c.drawText(layout.title, worldW * 0.5f, h * 0.165f, titleP)
        // furniture
        // v0.16.0b: the ladder IS kid units (InteriorLayouts:
        // "a chair is a chair next to a kid figure in EVERY
        // room" — CHAIR_H 0.62, COUNTER_H 0.78, SEAT_Y 0.30
        // of a kid's height). The x2.2 the growth pass
        // multiplied in here drew every piece 2.2x too
        // large in both dimensions — bank counters stood
        // 1.6x a kid's height, bus seat-backs walled off
        // the aisle. The room's growth lives in the world
        // width and the xFrac spread, never in the pieces.
        for (f in layout.furniture) {
            val fx = f.xFrac * worldW
            val fw = WorldScale.units(f.widthU, h)
            val fhgt = WorldScale.units(f.heightU, h)
            val baseY = if (f.back) h * 0.60f else h * 0.86f
            val top = baseY - fhgt
            // v0.16.0e: the piece is drawn as the thing it
            // IS — FurnitureArt's dressed language (cut
            // silhouette, paper material, top face + side
            // depth, per-kind grammar, per-room-family
            // trim). The v0.16.0b slab + lit-top-edge
            // painter is removed whole; the seat/work line
            // still lands at the ladder's seatYU inside
            // the piece's own drawing, and the hit rect is
            // the very one it always was.
            val roomKind = layout.rooms.firstOrNull {
                it.id == f.roomId }?.kind ?: "generic"
            FurnitureArt.drawPiece(c, f.kind, roomKind, fx, baseY,
                fw, fhgt, WorldScale.units(f.seatYU, h), f.back,
                (layout.key + ":" + f.id).hashCode().toLong() * 31L + 7L)
            hits.add(Hit("livingfurniture",
                "${layout.key}:${f.id}:${f.kind}:${f.action}",
                RectF(fx - fw / 2f - 6f, top - 6f, fx + fw / 2f + 6f, baseY + 8f)))
        }
        // v0.16.0e pass 2: a FABRICATED interior's door.
        // Fabrication's cheapest trick (doc 4): a real
        // door in a claimed building leads to a set that
        // isn't on any map — City Hall's basement becomes
        // the dungeon, your garage becomes the lair. The
        // door is cardboard over the real wall; the set
        // beyond it is a section, overlay state only, and
        // it vanishes with the fiction.
        val ficDoorTarget = when (layout.key) {
            "CityHall" -> "fiction:dungeon"
            "YourHouse" -> "fiction:lair"
            else -> null
        }
        if (ficDoorTarget != null && ride == null &&
            (fictionPhaseNow == Fiction.Phase.LIVE ||
                fictionPhaseNow == Fiction.Phase.ESCALATING)) {
            val dw = WorldScale.units(0.72f, h)
            val dh = WorldScale.units(2.05f, h)
            val dx = worldW * 0.925f
            val dBase = h * 0.60f
            c.drawRect(dx - dw / 2f - 4f, dBase - dh - 4f,
                dx + dw / 2f + 4f, dBase,
                Paper.fillPaint("#5a4632"))
            c.drawRect(dx - dw / 2f, dBase - dh, dx + dw / 2f,
                dBase, Paper.fillPaint("#b08d57"))
            val flute = Paper.fillPaint("#8a6c3e", 150)
            c.drawRect(dx - dw * 0.18f, dBase - dh + 6f,
                dx - dw * 0.18f + 2.5f, dBase - 6f, flute)
            c.drawRect(dx + dw * 0.18f - 2.5f, dBase - dh + 6f,
                dx + dw * 0.18f, dBase - 6f, flute)
            val signP = Paper.textPaint("#33241a",
                WorldScale.units(0.17f, h), bold = true, casual = true)
            signP.textAlign = Paint.Align.CENTER
            c.drawRect(dx - dw * 0.46f, dBase - dh * 0.78f,
                dx + dw * 0.46f, dBase - dh * 0.60f,
                Paper.fillPaint("#f2e8ce"))
            c.drawText(if (ficDoorTarget == "fiction:dungeon")
                "THE DUNGEON ↓" else "THE LAIR ↓",
                dx, dBase - dh * 0.645f, signP)
            hits.add(Hit("fictiondoor", ficDoorTarget,
                RectF(dx - dw / 2f - 6f, dBase - dh - 6f,
                    dx + dw / 2f + 6f, dBase + 8f)))
        }
        if (ride != null) {
            // the route board: where this bus is going, and
            // the tap that rides it there
            val destName = LOCATIONS[ride.to]?.name ?: ride.to
            val bw = worldW * 0.30f
            val bx = worldW * 0.5f - bw / 2f
            c.drawRoundRect(RectF(bx, h * 0.20f, bx + bw, h * 0.30f),
                8f, 8f, Paper.fillPaint("#20303e"))
            val rp = Paper.textPaint("#ffd98a", 17f * 1f, bold = true)
            rp.textAlign = Paint.Align.CENTER
            c.drawText((if (ride.school) "SCHOOL BUS" else "BUS") +
                " → $destName", worldW * 0.5f, h * 0.245f, rp)
            val rp2 = Paper.textPaint("#ffffff", 12f * 1f)
            rp2.textAlign = Paint.Align.CENTER
            c.drawText("TAP TO RIDE", worldW * 0.5f, h * 0.285f, rp2)
            hits.add(Hit("busride", ride.to,
                RectF(bx, h * 0.20f, bx + bw, h * 0.30f)))
        }
    }

    private fun drawSpawns(c: Canvas, st: GameState, worldW: Float, h: Float, seed: Long) {
        val me = engine!!.player(st)
        val spawnsHere = st.spawns.filter { it.locationId == me.location }
        val srnd = Random(seed + 99)
        for (sp in spawnsHere) {
            // #96: the meteor rock only shows itself after dark
            if (sp.id.startsWith("meteor") && st.timeMin in 300..1259) continue
            val sx = worldW * (0.10f + srnd.nextFloat() * 0.80f)
            val sy = h * (0.60f + srnd.nextFloat() * 0.22f)
            val col = collectibleById(sp.collectibleId)
            val icon = col?.icon ?: "✨"
            val br = 30f * resources.displayMetrics.density / 3f
            val badge = RectF(sx - br, sy - br, sx + br, sy + br)
            val wiggle = if (sp.wiggled) kotlin.math.sin(tick * 0.6f) * 8f else 0f
            c.drawCircle(sx + wiggle, sy, br, Paper.fillPaint("#000000", 70))
            c.drawCircle(sx + wiggle, sy, br, Paper.fillPaint(Paper.PARCHMENT))
            c.drawCircle(sx + wiggle, sy, br, Paper.strokePaint(Paper.RED, 3f))
            val tp = Paper.textPaint("#1a1a1a", br * 1.1f)
            tp.textAlign = Paint.Align.CENTER
            c.drawText(icon, sx + wiggle, sy + br * 0.4f, tp)
            val sparkle = (tick / 8 + sx.toInt()) % 20
            if (sparkle < 6) {
                c.drawCircle(sx + wiggle + br * 0.7f, sy - br * 0.7f, 3f,
                    Paper.fillPaint("#ffffff", 220))
            }
            hits.add(Hit("spawn", sp.id,
                RectF(badge.left - 20, badge.top - 20, badge.right + 20, badge.bottom + 20)))
        }
    }

    /** #45 PROPS FOLLOW THE RESIDENT: a home's dressing reads its
     *  owner's CURRENT agenda block — homework spread after school,
     *  dinner plates at dinner, TV glow on a leisure evening, a dark
     *  quiet room when they're asleep or out. */
    private fun drawHomeLifeProps(c: Canvas, st: GameState, eng: Engine,
                                  locId: String, worldW: Float, h: Float) {
        val ownerName = KID_HOUSE.entries.firstOrNull { it.value == locId }?.key ?: return
        val owner = st.agents[ownerName] ?: return
        val block = eng.effectiveAgendaBlock(st, owner, st.timeMin)
        val hour = st.timeMin / 60
        val caption: String
        when {
            owner.asleep -> {
                c.drawRect(0f, 0f, worldW, h, Paper.fillPaint("#0a1030", 70))
                caption = "${ownerName.split(" ").first()}'s room: lights out. Somebody's sawing logs."
            }
            block == null || block.location != locId -> {
                caption = "Nobody home — the room waits, tidy and quiet."
            }
            hour in 15..17 -> {
                // homework: two paper sheets on the floor-table zone
                val pw = worldW * 0.09f
                val ph = h * 0.05f
                val px = worldW * 0.42f
                val py = h * 0.78f
                c.drawRect(px, py, px + pw, py + ph, Paper.fillPaint("#f4ecd8", 235))
                c.drawRect(px + pw * 1.2f, py + ph * 0.3f, px + pw * 2.2f, py + ph * 1.3f,
                    Paper.fillPaint("#e8e0cc", 235))
                caption = "Homework spread out — school's out, books open."
            }
            hour in 17..19 -> {
                // dinner: two plates
                val pr = h * 0.022f
                for (k in 0..1) {
                    c.drawCircle(worldW * (0.44f + k * 0.12f), h * 0.80f, pr,
                        Paper.fillPaint("#f4ecd8", 240))
                    c.drawCircle(worldW * (0.44f + k * 0.12f), h * 0.80f, pr * 0.6f,
                        Paper.fillPaint("#c8b088", 240))
                }
                caption = "Dinner things out. It smells like somebody tried."
            }
            hour >= 19 -> {
                // TV glow
                val tw = worldW * 0.16f
                val th = h * 0.12f
                val tx = worldW * 0.78f
                val ty = h * 0.30f
                c.drawRect(tx, ty, tx + tw, ty + th, Paper.fillPaint("#0c0c14", 255))
                c.drawRect(tx + 4, ty + 4, tx + tw - 4, ty + th - 4,
                    Paper.fillPaint("#7ab8e8", 150 + ((tick / 6) % 40).toInt()))
                caption = "TV glow — evening leisure, South Park style."
            }
            else -> caption = "Home turf, lived-in and theirs."
        }
        val cp = Paper.textPaint("#f4ecd8", 11.5f, bold = false, casual = true)
        cp.textAlign = Paint.Align.LEFT
        c.drawText(caption, worldW * 0.03f, h * 0.055f, cp)
    }

    // ============================================================
    // v0.1.8 Phase 2 — the living world outdoors. Everything here
    // is deterministic per (location, day, weather, time) so the
    // whole town agrees on what it's doing; interactions ride the
    // normal hit system out to GameActivity.onSceneTap.
    // ============================================================
    private fun drawLivingOutdoor(c: Canvas, st: GameState, eng: Engine,
                                  loc: Location, worldW: Float, h: Float, seed: Long) {
        val tm = st.timeMin
        val hour = tm / 60
        val night = SceneArt.timeOfDay(tm) == "night"
        val day = st.absDay
        val millis = System.currentTimeMillis()
        fun glyph(txt: String, x: Float, y: Float, size: Float, color: String = "#ffffff") {
            val p = Paper.textPaint(color, size, bold = true, casual = true)
            p.textAlign = Paint.Align.CENTER
            c.drawText(txt, x, y, p)
        }
        fun scatter(salt: Int) = TownLife.scatter(loc.id, day, salt)

        // ---- #31/#32 puddles (and #74 spring paper boats) ----
        val psize = TownLife.puddleSizeAt(st, loc.id)
        if (psize > 0) {
            val frozen = loc.id in st.frozenPuddles
            val pr = Random(seed + 31)
            val puddleXs = mutableListOf<Float>()
            repeat(1 + psize / 2) { i ->
                val px = worldW * (0.16f + pr.nextFloat() * 0.68f)
                val py = h * (0.86f + pr.nextFloat() * 0.09f)
                puddleXs.add(px)
                val pw = worldW * (0.05f + psize * 0.012f)
                c.drawOval(RectF(px - pw, py - pw * 0.16f, px + pw, py + pw * 0.16f),
                    Paper.fillPaint(if (frozen) "#cfe0f2" else "#4a6a94",
                        if (frozen) 235 else 190))
                if (frozen) {
                    val cp = Paper.strokePaint("#8aa8cc", 1.6f)
                    c.drawLine(px - pw * 0.6f, py, px + pw * 0.2f, py - pw * 0.08f, cp)
                    c.drawLine(px, py + pw * 0.06f, px + pw * 0.55f, py - pw * 0.04f, cp)
                }
                if (i == 0 && season == Season.SPRING && !frozen) {
                    // #74: a paper boat rides the melt puddle
                    val started = st.life["boat:${loc.id}"]
                    val sail = if (started != null && tm >= started)
                        minOf(1f, (tm - started) / 45f) else 0f
                    val bx = px - pw * 0.7f + sail * pw * 1.4f
                    val by = py - 4f
                    c.drawPath(Path().apply {
                        moveTo(bx, by); lineTo(bx, by - 13f); lineTo(bx + 11f, by); close()
                    }, Paper.fillPaint("#f4ecd8"))
                    c.drawRect(bx - 9f, by, bx + 13f, by + 4f, Paper.fillPaint("#c8b088"))
                    hits.add(Hit("boat", loc.id,
                        RectF(px - pw - 14, py - 30, px + pw + 14, py + 16)))
                }
            }
        }

        // ---- Stark's Pond life: #1 ducks, #9 fish, #63 feeding ----
        if (loc.id == "StarksPond") {
            val pcx = worldW * 0.5f
            val pcy = h * 0.71f
            val prx = worldW * 0.33f
            val pry = h * 0.045f
            val scattering = tick < duckScatterUntil
            val fed = (st.life["ducksFedDay"] == day)
            val t = tick * 0.02f
            for (i in 0..2) {
                val ang = t * (0.6f + i * 0.13f) + i * 2.1f
                val rad = if (scattering) 1.3f else if (fed) 0.55f else 0.8f
                val dx = pcx + kotlin.math.cos(ang) * prx * rad
                val dy = pcy + kotlin.math.sin(ang) * pry * rad
                c.drawOval(RectF(dx - 13, dy - 6, dx + 13, dy + 6),
                    Paper.fillPaint("#f0ead8"))
                c.drawCircle(dx + 10, dy - 7, 5.5f, Paper.fillPaint("#f0ead8"))
                c.drawOval(RectF(dx + 13, dy - 8, dx + 19, dy - 5),
                    Paper.fillPaint("#e8a33a"))
                if (scattering) {
                    c.drawOval(RectF(dx - 20, dy - 3, dx + 20, dy + 5),
                        Paper.strokePaint("#bcd6f2", 2f))
                }
            }
            // #9: every so often a fish arcs out with a ring ripple
            val fishPhase = ((tick + seed) % 640).toInt()
            if (fishPhase < 26 && !night) {
                val fx = pcx + (scatter(9) - 0.5f) * prx * 1.4f
                val lift = (kotlin.math.sin(fishPhase / 26f * 3.14159f) * h * 0.045f).toFloat()
                glyph("🐟", fx, pcy - lift, 20f)
                val rr = fishPhase * 2.2f
                c.drawOval(RectF(fx - rr, pcy - rr * 0.25f, fx + rr, pcy + rr * 0.25f),
                    Paper.strokePaint("#dcecfc", 2f, (160 - fishPhase * 5).coerceIn(20, 160)))
                // and if Jimbo's standing here, he has notes
                if (st.agents["Jimbo"]?.location == loc.id) {
                    glyph("There she blows!", fx + 60, pcy - h * 0.09f, 12f, "#ffe9a8")
                }
            }
            hits.add(Hit("pond", loc.id, RectF(pcx - prx, pcy - pry * 2,
                pcx + prx, pcy + pry * 2)))
            // #43 STAR-GAZER: clear nights bring a telescope out
            if (night && st.weatherId in setOf("sunny", "cloudy")) {
                val tx = worldW * 0.80f
                val ty = h * 0.86f
                val lp = Paper.strokePaint("#2a2f3a", 4f)
                c.drawLine(tx, ty - 34, tx - 12, ty, lp)
                c.drawLine(tx, ty - 34, tx + 12, ty, lp)
                c.drawLine(tx, ty - 34, tx + 16, ty - 52, lp)
                glyph("✨", tx + 30, ty - 60, 15f)
                glyph("🔭", tx - 24, ty - 16, 16f)
            }
        }

        // ---- v0.2.0e: THE SHOWDOWN FORT at the snowball grounds.
        // In snow it's a proper block fort with a flag and an
        // ammo pile; the rest of the year it's three planks and
        // a sign full of confidence. Tapping it calls (or
        // explains) a Snowball Showdown. ----
        if (loc.id in Minigames.SHOWDOWN_LOCS) {
            val fx = worldW * 0.21f
            val fy = h * 0.905f
            val fw = worldW * 0.085f
            val fh = h * 0.085f
            val snowing = st.weatherId in Minigames.SNOW_WEATHERS
            val wall = if (snowing) "#eef4fa" else "#9a7448"
            val shade = if (snowing) "#c3d5e8" else "#7c5a34"
            // two blocky towers + a lower wall between, crenellated
            for ((bx, bh) in listOf((fx - fw) to fh, (fx + fw * 0.45f) to fh,
                    (fx - fw * 0.28f) to fh * 0.55f)) {
                c.drawRect(bx, fy - bh, bx + fw * 0.55f, fy, Paper.fillPaint(wall))
                c.drawRect(bx, fy - bh, bx + fw * 0.55f, fy - bh + 5f,
                    Paper.fillPaint(shade))
            }
            val flagP = Paper.strokePaint("#8a4a2a", 3f)
            c.drawLine(fx - fw + 6f, fy - fh, fx - fw + 6f, fy - fh - 22f, flagP)
            glyph("🚩", fx - fw + 14f, fy - fh - 16f, 15f)
            if (snowing) {
                // the ammo pile: three honest snowballs
                c.drawCircle(fx + fw * 1.35f, fy - 6f, 8f, Paper.fillPaint("#ffffff"))
                c.drawCircle(fx + fw * 1.35f + 13f, fy - 6f, 8f, Paper.fillPaint("#ffffff"))
                c.drawCircle(fx + fw * 1.35f + 6f, fy - 17f, 8f, Paper.fillPaint("#ffffff"))
            }
            Paper.drawSign(c, fx + fw * 0.1f, fy + 16f, fw * 1.15f, 20f,
                if (snowing) "SNOW FORT" else "FORT (NEEDS SNOW)", 10.5f)
            hits.add(Hit("snowfort", loc.id,
                RectF(fx - fw - 12, fy - fh - 30, fx + fw * 1.7f, fy + 26)))
        }

        // ---- #5 the stray cat runs its route ----
        if (TownLife.catLocAt(tm) == loc.id && !night) {
            val frac = (tm % 120) / 120f
            val cxp = worldW * (0.08f + frac * 0.84f)
            val cyp = h * (0.615f + scatter(5) * 0.02f)
            c.drawOval(RectF(cxp - 15, cyp - 6, cxp + 15, cyp + 6),
                Paper.fillPaint("#d88a3a"))
            c.drawCircle(cxp + 14, cyp - 7, 7f, Paper.fillPaint("#d88a3a"))
            c.drawPath(Path().apply {
                moveTo(cxp + 9, cyp - 12); lineTo(cxp + 12, cyp - 19); lineTo(cxp + 15, cyp - 12); close()
                moveTo(cxp + 15, cyp - 12); lineTo(cxp + 19, cyp - 18); lineTo(cxp + 21, cyp - 11); close()
            }, Paper.fillPaint("#d88a3a"))
            val tail = Paper.strokePaint("#d88a3a", 3.5f)
            c.drawLine(cxp - 14, cyp - 3, cxp - 24, cyp - 12 - kotlin.math.sin(tick * 0.2f) * 3, tail)
        }

        // ---- #3 crows on the wire ----
        run {
            val stormOff = st.weatherId in setOf("storm", "blizzard") ||
                Quests.currentStage(st) == 2
            val wireY = h * 0.16f
            val wp = Paper.strokePaint("#1c2129", 2.5f)
            val wire = Path()
            wire.moveTo(0f, wireY)
            wire.quadTo(worldW * 0.5f, wireY + h * 0.03f, worldW, wireY)
            c.drawPath(wire, wp)
            if (!stormOff) {
                val crows = 2 + (scatter(3) * 4).toInt()
                for (i in 0 until crows) {
                    val fx = worldW * (0.14f + 0.18f * i) + scatter(i + 40) * 20f
                    val fy = wireY + h * 0.03f * (1f - kotlin.math.abs(fx / worldW - 0.5f) * 2f) - 4f
                    c.drawOval(RectF(fx - 8, fy - 7, fx + 8, fy + 5), Paper.fillPaint("#14161c"))
                    c.drawCircle(fx + 7, fy - 8, 4f, Paper.fillPaint("#14161c"))
                    c.drawPath(Path().apply {
                        moveTo(fx - 7, fy); lineTo(fx - 15, fy + 4); lineTo(fx - 6, fy + 5); close()
                    }, Paper.fillPaint("#14161c"))
                }
            } else if ((tick / 30) % 9 == 0L) {
                glyph("🐦‍⬛", worldW * 0.3f, wireY - 26, 18f)
                glyph("🐦‍⬛", worldW * 0.42f, wireY - 40, 15f)
            }
        }

        // ---- #4 butterflies & bees on clear warm days ----
        if ((season == Season.SPRING || season == Season.SUMMER) &&
            st.weatherId in setOf("sunny", "partly_cloudy", "cloudy") && !night &&
            loc.id in setOf("KidsPark", "MainStreet", "BusStop", "StarksPond")) {
            for (i in 0..2) {
                val bx = worldW * (0.2f + 0.25f * i) +
                    kotlin.math.sin(millis * 0.0006f + i * 2.4f) * worldW * 0.05f
                val by = h * 0.80f + kotlin.math.cos(millis * 0.0011f + i * 1.9f) * h * 0.03f
                val flap = kotlin.math.abs(kotlin.math.sin(millis * 0.02f + i)).toFloat()
                val ww2 = 5f + flap * 6f
                c.drawOval(RectF(bx - ww2, by - 5, bx, by + 5), Paper.fillPaint("#e87ab0", 220))
                c.drawOval(RectF(bx, by - 5, bx + ww2, by + 5), Paper.fillPaint("#e87ab0", 220))
                c.drawCircle(bx, by, 2.2f, Paper.fillPaint("#2a2a2a"))
            }
            glyph("🐝", worldW * (0.55f + kotlin.math.sin(millis * 0.0016f) * 0.03f),
                h * 0.76f + kotlin.math.cos(millis * 0.002f) * 8f, 14f)
        }

        // ---- #7 leaf streams in the autumn wind ----
        if (season == Season.FALL && st.weatherId in setOf("windy", "sunny", "partly_cloudy")) {
            for (i in 0..9) {
                val speed = 90f + i * 23f
                val lx = ((millis / 1000f * speed + i * 173f) % (worldW + 120f)) - 60f
                val ly = h * (0.30f + 0.05f * i) +
                    kotlin.math.sin(millis * 0.002f + i * 1.7f) * h * 0.03f +
                    (if (lx > worldW * 0.92f) h * 0.45f else 0f) // pile at the fence line
                Paper.crooked(c, (millis / 90 + i * 40) % 360f, lx, ly) {
                    c.drawOval(RectF(lx - 6, ly - 3, lx + 6, ly + 3),
                        Paper.fillPaint(if (i % 2 == 0) "#d88a2a" else "#b85c1e", 215))
                }
            }
        }

        // ---- #78 fireflies on summer nights ----
        if (season == Season.SUMMER && night) {
            val camMoved = kotlin.math.abs(camX - lastCamX) > 3f
            for (i in 0..13) {
                val bx = worldW * (0.05f + scatter(i + 60) * 0.9f)
                val by = h * (0.72f + scatter(i + 80) * 0.2f)
                var ox = kotlin.math.sin(millis * 0.0009f + i * 2.2f) * 26f
                var oy = kotlin.math.cos(millis * 0.0013f + i * 1.6f) * 12f
                if (camMoved) { ox += 46f; oy -= 20f } // scatter from your path
                val tw = (kotlin.math.sin(millis * 0.004f + i * 3.1f) * 0.5f + 0.5f)
                c.drawCircle(bx + ox, by + oy, 2.6f,
                    Paper.fillPaint("#e8f26a", (70 + tw * 170).toInt()))
            }
        }

        // ---- #8 dusk bug clouds around the lights ----
        if (season == Season.SUMMER && hour in 18..20) {
            for ((lx, ly) in listOf(worldW * 0.06f to h * 0.42f, worldW * 0.5f to h * 0.47f)) {
                for (i in 0..6) {
                    val a = millis * 0.004f + i * 0.9f
                    c.drawCircle(lx + kotlin.math.cos(a) * 16f,
                        ly + kotlin.math.sin(a * 1.3f) * 9f, 1.8f,
                        Paper.fillPaint("#2c2c34", 190))
                }
            }
        }

        // ---- #6 animal tracks in fresh snow ----
        if (st.weatherId in setOf("light_snow", "heavy_snow")) {
            val tp = Paper.fillPaint("#b9c9de", 130)
            for (i in 0..11) {
                val tx = worldW * (0.06f + i * 0.075f)
                val ty = h * (0.90f - (i % 3) * 0.012f) + (if (i % 2 == 0) 0f else 7f)
                c.drawOval(RectF(tx, ty, tx + 7, ty + 4), tp)
            }
        }

        // ---- #40 first-flower spots in spring ----
        if (season == Season.SPRING) {
            for (i in 0..4) {
                val fx = worldW * (0.10f + scatter(i + 21) * 0.8f)
                val fy = h * (0.93f + scatter(i + 33) * 0.04f)
                glyph(if (i % 2 == 0) "🌸" else "🌼", fx, fy, 13f)
            }
        }

        // ---- #98 3:33 AM SIGHTINGS (autonomous, completed
        // v0.1.9a): on seeded nights, while you are simply OUT in
        // the small hours, something tiny crosses the yard on its
        // own — gnome-dark, fast, gone. No tap required; the
        // crossing is reported to the activity when it completes.
        if (night && tm in 200..225 && TownLife.sightingTonight(st) &&
            sightingDay != day) {
            if (sightingStartTick < 0) sightingStartTick = tick
            val p = (tick - sightingStartTick) / 170f
            if (p >= 1f) {
                sightingDay = day
                sightingStartTick = -1
                onSighting()
            } else {
                val sx = -40f + p * (worldW + 80f)
                val sy = h * 0.648f
                val sil = Paper.fillPaint("#0a0e18", 225)
                c.drawRect(sx - 5, sy - 12, sx + 5, sy, sil)
                c.drawCircle(sx, sy - 15, 4.5f, Paper.fillPaint("#f2c9a0", 235))
                c.drawPath(Path().apply {
                    moveTo(sx - 6, sy - 18); lineTo(sx + 6, sy - 18)
                    lineTo(sx, sy - 30); close()
                }, Paper.fillPaint("#8a1f1f", 235))
            }
        }
    }

    /** Phase 2 part 2: houses breathing + tappable set dressing. */
    /** v0.15.0d: PaperDressing's standing props, painted
     *  at WORLD SCALE on the dressing table's own spots
     *  (PaperDressing.propSpots — the same spots whose
     *  footprints block the walk). Flat paper shapes,
     *  solids only; each stands on the walk band's line
     *  for its depth, sized off the ladder. */
    private fun drawStandingProps(c: Canvas, loc: Location, worldW: Float,
                                  h: Float, night: Boolean) {
        fun col(hex: String): String {
            if (!night) return hex
            fun ch(s: String, i: Int) = s.substring(i, i + 2).toInt(16)
            val r = (ch(hex, 1) * 0.55f + 0x23 * 0.45f).toInt()
            val g = (ch(hex, 3) * 0.55f + 0x2f * 0.45f).toInt()
            val b = (ch(hex, 5) * 0.55f + 0x4a * 0.45f).toInt()
            return "#%02x%02x%02x".format(r, g, b)
        }
        val seedRnd = java.util.Random(loc.id.hashCode().toLong())
        // v0.16.0b: the location's own footprint — a standing
        // prop never plants in the facade/structure's lot in
        // its lower band (the Bus Stop pine stood INSIDE the
        // shelter's glass; trees in doorways are the same
        // class). Greenery rooted high behind the roofline
        // still peeks over the roofs.
        val unitW = SceneScale.unitW(worldW, loc, false)
        val structure = TownBaseline.of(loc.id)?.structure ?: "hut"
        val foot = if (loc.kind in CharacterSystem.BUILDING_KINDS)
            FacadeGeom.facadeRect(loc.kind, worldW, h, unitW)
            else FacadeGeom.structureRect(structure, worldW, h, unitW)
        // an OPEN front (shelter, pavilion) is glass and air: a
        // prop in its column reads as standing INSIDE it at any
        // band, so the whole column is clear. An opaque facade
        // occludes its own back band — only the ground-line
        // frontage (the doorway) must stay clear there.
        val openFront = loc.kind !in CharacterSystem.BUILDING_KINDS &&
            structure in setOf("shelter", "pavilion")
        val footMargin = WorldScale.units(0.30f, h)
        for ((kind, fx, band) in PaperDressing.propSpots(loc)) {
            val x = fx * worldW
            val base = WalkBand.feetY(band, h, false)
            if (x > foot.left - footMargin && x < foot.right + footMargin &&
                (if (openFront) base > foot.top
                 else base > foot.top + foot.height * 0.35f)) continue
            // v0.15.0g: ONE depth factor for everything in
            // the band — the prop breathes on the same
            // curve as the figures (the private 0.82+0.18
            // breath it kept for itself is retired), so a
            // tree at the back of the band and a kid beside
            // it agree about how deep they are standing.
            val ds = WalkBand.depthScale(band)
            fun u(units: Float) = WorldScale.units(units, h) * ds
            // the paper shadow every cut-out stands on
            c.drawOval(RectF(x - u(0.16f), base - u(0.02f),
                x + u(0.16f), base + u(0.035f)), Paper.fillPaint("#1a2436", 40))
            when (kind) {
                PaperDressing.PINE ->
                    SceneArt.drawPine(c, x, base, u(WorldScale.PINE_U), night)
                PaperDressing.TREE -> {
                    val th = u(WorldScale.TREE_U)
                    c.drawRect(x - th * 0.035f, base - th * 0.42f,
                        x + th * 0.035f, base, Paper.fillPaint(col("#5d3f24")))
                    val canopy = Paper.fillPaint(col("#3f7a44"))
                    c.drawCircle(x, base - th * 0.72f, th * 0.30f, canopy)
                    c.drawCircle(x - th * 0.22f, base - th * 0.55f, th * 0.21f, canopy)
                    c.drawCircle(x + th * 0.22f, base - th * 0.55f, th * 0.21f, canopy)
                    if (seedRnd.nextFloat() < 0.5f) {
                        // a sapling beside it — the ladder's
                        // named exception, small on purpose
                        val sx = x + th * 0.42f
                        val sh = u(WorldScale.SAPLING_U)
                        c.drawRect(sx - 2f, base - sh * 0.4f, sx + 2f, base,
                            Paper.fillPaint(col("#5d3f24")))
                        c.drawCircle(sx, base - sh * 0.68f, sh * 0.30f,
                            Paper.fillPaint(col("#4c8a50")))
                    }
                }
                PaperDressing.LAMP -> {
                    val lh = u(WorldScale.LAMP_U)
                    c.drawRect(x - 3f, base - lh, x + 3f, base,
                        Paper.fillPaint(col("#2f353e")))
                    c.drawCircle(x, base - lh, u(0.075f),
                        Paper.fillPaint(col("#ffd98a")))
                    if (night) c.drawCircle(x, base - lh, u(0.16f),
                        Paper.fillPaint("#ffd98a", 42))
                }
                PaperDressing.FENCE -> {
                    val fh = u(WorldScale.FENCE_U)
                    val fw = worldW * 0.085f
                    val wood = Paper.fillPaint(col("#8a6a48"))
                    c.drawRect(x - fw / 2, base - fh * 0.72f, x + fw / 2,
                        base - fh * 0.58f, wood)
                    c.drawRect(x - fw / 2, base - fh * 0.36f, x + fw / 2,
                        base - fh * 0.22f, wood)
                    var px = x - fw / 2
                    while (px < x + fw / 2) {
                        c.drawRect(px, base - fh, px + 4.5f, base, wood)
                        px += fw / 5f
                    }
                }
                PaperDressing.HYDRANT -> {
                    val hh = u(WorldScale.HYDRANT_U)
                    val red = Paper.fillPaint(col("#c0392b"))
                    c.drawRect(x - hh * 0.20f, base - hh * 0.82f, x + hh * 0.20f,
                        base, red)
                    c.drawCircle(x, base - hh * 0.82f, hh * 0.20f, red)
                    c.drawRect(x - hh * 0.34f, base - hh * 0.58f, x + hh * 0.34f,
                        base - hh * 0.44f, red)
                }
                PaperDressing.MAILBOX -> {
                    val mh = u(WorldScale.MAILBOX_U)
                    c.drawRect(x - 2.5f, base - mh * 0.55f, x + 2.5f, base,
                        Paper.fillPaint(col("#4a3a26")))
                    c.drawRoundRect(RectF(x - mh * 0.30f, base - mh,
                        x + mh * 0.30f, base - mh * 0.55f), 5f, 5f,
                        Paper.fillPaint(col("#2e3438")))
                }
                PaperDressing.BUSH -> {
                    val bh = u(WorldScale.BUSH_U)
                    val bush = Paper.fillPaint(col("#3f7a44"))
                    c.drawOval(RectF(x - bh * 0.55f, base - bh * 0.8f,
                        x + bh * 0.55f, base), bush)
                    c.drawOval(RectF(x - bh * 0.30f, base - bh,
                        x + bh * 0.34f, base - bh * 0.3f), bush)
                }
                PaperDressing.BENCH -> {
                    val bLen = u(WorldScale.BENCH_LEN_U)
                    val bH = u(WorldScale.BENCH_H_U)
                    val seatY = base - bH * 0.47f
                    val wood = Paper.fillPaint(col("#7a5230"))
                    c.drawRect(x - bLen / 2, base - bH, x + bLen / 2,
                        base - bH * 0.76f, wood)
                    c.drawRect(x - bLen / 2, seatY, x + bLen / 2,
                        seatY + bH * 0.10f, wood)
                    c.drawRect(x - bLen / 2 + 4f, seatY, x - bLen / 2 + 10f,
                        base, wood)
                    c.drawRect(x + bLen / 2 - 10f, seatY, x + bLen / 2 - 4f,
                        base, wood)
                }
                PaperDressing.PLANTER -> {
                    val ph = u(0.40f)
                    c.drawRect(x - ph * 0.62f, base - ph * 0.55f, x + ph * 0.62f,
                        base, Paper.fillPaint(col("#8a5a3a")))
                    c.drawOval(RectF(x - ph * 0.55f, base - ph * 1.05f,
                        x + ph * 0.55f, base - ph * 0.42f),
                        Paper.fillPaint(col("#3f7a44")))
                }
                PaperDressing.FLAG -> {
                    val fh = u(WorldScale.FLAGPOLE_U)
                    c.drawRect(x - 2.5f, base - fh, x + 2.5f, base,
                        Paper.fillPaint(col("#5a5a5a")))
                    c.drawRect(x + 2.5f, base - fh, x + fh * 0.20f,
                        base - fh * 0.86f, Paper.fillPaint(col("#d43d2a")))
                }
                PaperDressing.BIKE_RACK -> {
                    val rh = u(0.45f)
                    val rack = Paper.strokePaint(col("#6a7076"), 4.5f)
                    for (k in -1..1) {
                        val rx = x + k * rh * 0.62f
                        c.drawArc(RectF(rx - rh * 0.26f, base - rh,
                            rx + rh * 0.26f, base), 180f, 180f, false, rack)
                        c.drawLine(rx - rh * 0.26f, base - rh / 2, rx - rh * 0.26f,
                            base, rack)
                        c.drawLine(rx + rh * 0.26f, base - rh / 2, rx + rh * 0.26f,
                            base, rack)
                    }
                }
                PaperDressing.SILO -> {
                    // the landmark exception: a silo is
                    // SUPPOSED to top the barn (WorldScale)
                    val sh = u(WorldScale.SILO_U)
                    val sw = sh * 0.20f
                    c.drawRect(x - sw / 2, base - sh * 0.88f, x + sw / 2, base,
                        Paper.fillPaint(col("#b9bec6")))
                    c.drawOval(RectF(x - sw / 2, base - sh, x + sw / 2,
                        base - sh * 0.76f), Paper.fillPaint(col("#8a8f96")))
                    val band = Paper.fillPaint(col("#9a4438"))
                    c.drawRect(x - sw / 2, base - sh * 0.55f, x + sw / 2,
                        base - sh * 0.47f, band)
                }
                PaperDressing.DOCK -> {
                    val dw = worldW * 0.13f
                    val wood = Paper.fillPaint(col("#7a5c40"))
                    c.drawRect(x - dw / 2, base - u(0.05f), x + dw / 2,
                        base + u(0.02f), wood)
                    var px = x - dw / 2 + 3f
                    while (px < x + dw / 2 - 3f) {
                        c.drawLine(px, base - u(0.05f), px, base + u(0.02f),
                            Paper.strokePaint(col("#5d4630"), 2f))
                        px += dw / 6f
                    }
                    c.drawRect(x - dw / 2 + 4f, base, x - dw / 2 + 9f,
                        base + u(0.14f), wood)
                    c.drawRect(x + dw / 2 - 9f, base, x + dw / 2 - 4f,
                        base + u(0.14f), wood)
                }
            }
        }
    }

    private fun drawLivingProps(c: Canvas, st: GameState, eng: Engine,
                                loc: Location, worldW: Float, h: Float, seed: Long) {
        val tm = st.timeMin
        val hour = tm / 60
        val night = SceneArt.timeOfDay(tm) == "night"
        val day = st.absDay
        val millis = System.currentTimeMillis()
        fun glyph(txt: String, x: Float, y: Float, size: Float, color: String = "#ffffff") {
            val p = Paper.textPaint(color, size, bold = true, casual = true)
            p.textAlign = Paint.Align.CENTER
            c.drawText(txt, x, y, p)
        }
        fun scatter(salt: Int) = TownLife.scatter(loc.id, day, salt)
        val isHouse = loc.id in HOUSE_LOCS
        val baseY = h * 0.66f

        // ---- #91 the traveling gnome ----
        if (TownLife.gnomeLocFor(st) == loc.id) {
            val gx = worldW * (0.16f + scatter(91) * 0.2f)
            val gy = h * 0.90f
            c.drawPath(Path().apply {
                moveTo(gx - 9, gy - 16); lineTo(gx + 9, gy - 16); lineTo(gx, gy - 34); close()
            }, Paper.fillPaint("#c22e2e"))
            c.drawCircle(gx, gy - 14, 6f, Paper.fillPaint("#f2c9a0"))
            c.drawRect(gx - 8, gy - 12, gx + 8, gy + 2, Paper.fillPaint("#2a5a8a"))
            hits.add(Hit("gnome", loc.id, RectF(gx - 22, gy - 40, gx + 22, gy + 8)))
        }

        // ---- #99 the impossible snowman ----
        st.lifeText["snowman"]?.let { sm ->
            val parts = sm.split("|")
            if (parts.getOrNull(1) == loc.id) {
                val owner = parts.getOrNull(0) ?: ""
                val sx = worldW * 0.76f
                val sy = h * 0.93f
                c.drawCircle(sx, sy - 12, 15f, Paper.fillPaint("#f4f8ff"))
                c.drawCircle(sx, sy - 36, 10.5f, Paper.fillPaint("#f4f8ff"))
                val scarf = st.agents[owner]?.color ?: "#d43d2a"
                c.drawRect(sx - 10, sy - 29, sx + 10, sy - 24, Paper.fillPaint(scarf))
                glyph("🎩", sx, sy - 46, 15f)
            }
        }

        if (isHouse) {
            val owner = KID_HOUSE.entries.firstOrNull { it.value == loc.id }?.key
            val ownerAgent = owner?.let { st.agents[it] }
            val ownerHome = loc.id in TownInfo.occupiedHomes
            // #11 WINDOW WAVES: the resident pops up, waves, ducks away
            if (ownerHome && !night && (tick / 45 + loc.id.hashCode()) % 7 == 0L) {
                glyph("👋", worldW * 0.295f, baseY - h * 0.115f, 19f)
                glyph(owner?.split(" ")?.first() ?: "", worldW * 0.295f,
                    baseY - h * 0.085f, 10f, "#ffe9a8")
            }
            // #48 SLEEP TALKERS: tap the dark window, catch a dream
            if (night && ownerAgent?.asleep == true) {
                hits.add(Hit("window", loc.id,
                    RectF(worldW * 0.24f, baseY - h * 0.14f, worldW * 0.36f, baseY - h * 0.06f)))
                if ((tick / 60 + loc.id.hashCode()) % 5 == 0L) {
                    glyph("💭", worldW * 0.30f, baseY - h * 0.16f, 16f)
                }
            }
            // #25 CLOTHESLINE FORECASTS: dry mornings only — and
            // never the day before rain (the forecast says so)
            if (hour in 6..11 && st.weatherId !in setOf("rain", "storm") &&
                forecastWeather(st) !in setOf("rain", "storm")) {
                val ly = h * 0.585f
                val lp = Paper.strokePaint("#5a5a5a", 2.5f)
                c.drawLine(worldW * 0.66f, ly - 26, worldW * 0.66f, ly + 8, lp)
                c.drawLine(worldW * 0.94f, ly - 26, worldW * 0.94f, ly + 8, lp)
                c.drawLine(worldW * 0.66f, ly - 24, worldW * 0.94f, ly - 20, lp)
                val family = st.agents.values
                    .filter { KID_HOUSE[it.name] == loc.id }.map { it.color }
                val cols = (family + listOf("#f0e8d0", "#8aa8cc")).take(3)
                cols.forEachIndexed { i, col ->
                    val sx = worldW * (0.70f + i * 0.085f)
                    c.drawRect(sx, ly - 23 + i * 1.5f, sx + worldW * 0.055f,
                        ly + 6 + i * 1.5f, Paper.fillPaint(col, 230))
                }
            }
            // #36 RAIN BARRELS: fill with the weather, drain on dry days
            run {
                val bx = worldW * 0.845f
                val by = baseY + h * 0.02f
                c.drawRect(bx, by - 34, bx + 26, by, Paper.fillPaint("#6b4a2f"))
                val lvl = (st.life["barrel"] ?: 1).coerceIn(0, 3)
                if (lvl > 0) {
                    c.drawRect(bx + 3, by - 6 - lvl * 8, bx + 23, by - 4,
                        Paper.fillPaint("#4a6a94", 220))
                }
                if (st.weatherId == "storm") glyph("💦", bx + 13, by - 44, 14f)
            }
            // #26 GARAGE DOOR TEASE
            if (scatter(26) < 0.45 && hour in 8..19) {
                val open = (kotlin.math.sin(millis * 0.00035f + loc.id.hashCode()) * 0.5f + 0.5f)
                val gx = worldW * 0.13f
                val gw = worldW * 0.15f
                val gh = h * 0.115f
                c.drawRect(gx, baseY - gh, gx + gw, baseY, Paper.fillPaint("#3c414c"))
                c.drawRect(gx, baseY - gh, gx + gw, baseY - gh * open,
                    Paper.fillPaint("#78808e"))
                if (open > 0.45f) { // a car nose, thinking better of it
                    c.drawRect(gx + gw * 0.2f, baseY - gh * 0.42f, gx + gw * 0.8f,
                        baseY - gh * 0.08f, Paper.fillPaint("#22262e"))
                    glyph("• •", gx + gw * 0.5f, baseY - gh * 0.22f, 12f, "#ffe9a8")
                }
            }
            // #27 SPRINKLER O'CLOCK on hot clear mornings
            if (season == Season.SUMMER && st.weatherId == "sunny" && hour in 7..9) {
                val sx = worldW * 0.30f
                val sy = h * 0.90f
                val ap = Paper.strokePaint("#9fd0f2", 2.2f)
                ap.alpha = 170
                for (i in 0..4) {
                    val a = (millis * 0.001f + i * 0.35f) % 1.6f
                    val r = worldW * 0.05f
                    c.drawArc(RectF(sx - r, sy - r * 0.8f, sx + r, sy + r * 0.3f),
                        180f + a * 40f, 24f, false, ap)
                }
            }
            // #28 ROOF WORK DAY (some days, one house, gone tomorrow)
            if (day % 4 == 1 && hour in 8..16 && scatter(28) < 0.5f) {
                val rp = Paper.strokePaint("#8a6a3a", 3.5f)
                c.drawLine(worldW * 0.60f, baseY, worldW * 0.66f, baseY - h * 0.19f, rp)
                c.drawLine(worldW * 0.635f, baseY, worldW * 0.695f, baseY - h * 0.19f, rp)
                glyph("🔨", worldW * 0.44f + kotlin.math.sin(millis * 0.006f) * 3f,
                    baseY - h * 0.235f, 17f)
                glyph("👷", worldW * 0.44f, baseY - h * 0.205f, 18f)
            }
            // #29 KITCHEN SMOKE INCIDENT at meal hours
            if (hour in listOf(7, 12, 18) && scatter(29) < 0.4f && ownerHome) {
                for (i in 0..2) {
                    val ph = ((millis / 700f) + i * 0.8f) % 2.2f
                    c.drawCircle(worldW * 0.70f + ph * 14f, baseY - h * 0.16f - ph * 20f,
                        7f + ph * 6f, Paper.fillPaint("#2e2e34", (130 - ph * 45).toInt().coerceIn(20, 130)))
                }
                glyph("!!", worldW * 0.70f, baseY - h * 0.10f, 14f, "#ff9a8a")
            }
            // #38 ICICLE DRIP LINES after freeze-thaw mornings
            if (TownInfo.freezeThaw && hour in 7..13) {
                val len = ((13 - hour).coerceIn(2, 10)) * h * 0.006f
                val ip = Paper.fillPaint("#cfe4fa", 220)
                for (i in 0..5) {
                    val ix = worldW * (0.30f + i * 0.08f)
                    c.drawPath(Path().apply {
                        moveTo(ix - 4, baseY - h * 0.20f); lineTo(ix + 4, baseY - h * 0.20f)
                        lineTo(ix, baseY - h * 0.20f + len); close()
                    }, ip)
                }
            }
            // #67 SNOW SHOVELING: drifts wait on the driveway
            if (st.weatherId in setOf("light_snow", "heavy_snow", "blizzard") ||
                season == Season.WINTER) {
                if (st.lifeText["shoveled:${loc.id}"] != st.todayKey) {
                    val dx = worldW * 0.60f
                    val dy = h * 0.945f
                    c.drawOval(RectF(dx - 30, dy - 13, dx + 30, dy + 7),
                        Paper.fillPaint("#ffffff", 235))
                    c.drawOval(RectF(dx - 18, dy - 19, dx + 18, dy + 2),
                        Paper.fillPaint("#eef4ff", 235))
                    hits.add(Hit("shovel", loc.id, RectF(dx - 42, dy - 30, dx + 42, dy + 14)))
                }
            }
            // #95 FENCE FEUD dressing: dueling gnomes, taller fence
            st.lifeText["feud"]?.let { feud ->
                val sides = feud.split("|")
                if (loc.id == sides.getOrNull(0) || loc.id == sides.getOrNull(1)) {
                    val fy = h * 0.90f
                    val fp = Paper.strokePaint("#7a5230", 4f)
                    for (i in 0..4) {
                        c.drawLine(worldW * (0.86f + i * 0.03f), fy - h * 0.055f,
                            worldW * (0.86f + i * 0.03f), fy, fp)
                    }
                    glyph("🧌", worldW * 0.90f, fy - h * 0.065f, 16f)
                    glyph("💢", worldW * 0.90f, fy - h * 0.095f, 13f)
                }
            }
            // #50 THE DAWN MILK RUN: bottles at the door, just before 7
            if (tm in 390..419) {
                glyph("🥛", worldW * 0.44f, baseY + 8, 15f)
                glyph("🥛", worldW * 0.565f, baseY + 8, 15f)
            }
            // #89 ALARM CLOCK PANIC (completed v0.1.9a): just
            // before 8, today's oversleeper's alarm is STILL going
            // off at the window — shaking, furious, ignored.
            if (owner != null && owner == TownLife.alarmKidFor(st) && tm in 445..479) {
                val jx = kotlin.math.sin(tick * 2.6f) * 3f
                glyph("⏰", worldW * 0.30f + jx, baseY - h * 0.115f, 21f)
                glyph("💢", worldW * 0.30f + 17 + jx, baseY - h * 0.148f, 12f)
            }
        }

        // ---- #72 LEAF-PILE DIVES (autumn lawns + the park) ----
        if (season == Season.FALL && (isHouse || loc.id == "KidsPark")) {
            val px = worldW * (0.30f + scatter(72) * 0.3f)
            val py = h * 0.945f
            val scattered = st.life["leafScatter:${loc.id}"] == day
            c.drawOval(RectF(px - 34, py - (if (scattered) 6 else 16), px + 34, py + 6),
                Paper.fillPaint("#d88a2a", 235))
            c.drawOval(RectF(px - 20, py - (if (scattered) 9 else 24), px + 20, py),
                Paper.fillPaint("#b85c1e", 235))
            if (!scattered) {
                hits.add(Hit("leafpile", loc.id, RectF(px - 46, py - 34, px + 46, py + 12)))
            }
        }

        // ---- #39 THE LOST FLYER, stuck after a windy day ----
        if (st.lifeText["flyerLoc"] == loc.id) {
            val fx = worldW * 0.905f
            val fy = h * 0.60f
            Paper.crooked(c, 8f, fx, fy) {
                c.drawRect(fx - 13, fy - 17, fx + 13, fy + 17, Paper.fillPaint("#f4ecd8", 245))
                glyph("🐕", fx, fy + 5, 14f)
            }
            hits.add(Hit("flyer", loc.id, RectF(fx - 26, fy - 28, fx + 26, fy + 28)))
        }

        // ---- #68 KITE WEATHER at the park ----
        if (loc.id == "KidsPark" && st.weatherId == "windy") {
            val kx = worldW * 0.32f + kotlin.math.sin(millis * 0.0009f) * 26f
            val ky = h * 0.24f + kotlin.math.cos(millis * 0.0013f) * 14f
            val sp = Paper.strokePaint("#3a3f4a", 1.6f)
            c.drawLine(kx, ky + 14, worldW * 0.40f, h * 0.88f, sp)
            c.drawPath(Path().apply {
                moveTo(kx, ky - 15); lineTo(kx + 12, ky); lineTo(kx, ky + 15); lineTo(kx - 12, ky); close()
            }, Paper.fillPaint("#d43d2a"))
            glyph("🎀", kx, ky + 34 + kotlin.math.sin(millis * 0.004f) * 4f, 11f)
            hits.add(Hit("kite", loc.id, RectF(kx - 30, ky - 30, kx + 30, ky + 46)))
        }

        // ---- #75 LEMONADE STAND on hot clear days ----
        if (loc.id == "MainStreet" && season == Season.SUMMER &&
            st.weatherId == "sunny" && hour in 10..16) {
            val sx = worldW * 0.70f
            val sy = h * 0.90f
            c.drawRect(sx - 44, sy - 40, sx + 44, sy, Paper.fillPaint("#c8a24a"))
            c.drawRect(sx - 50, sy - 58, sx + 50, sy - 42, Paper.fillPaint("#f2c230"))
            Paper.drawSign(c, sx, sy - 74, 118f, 24f, "LEMONADE 25¢", 13f)
            glyph("🍋", sx - 24, sy - 18, 16f)
            glyph("🧒", sx + 26, sy - 22, 19f)
            hits.add(Hit("lemonade", loc.id, RectF(sx - 56, sy - 84, sx + 56, sy + 6)))
        }

        // ---- YOUR STAND (v0.10.0e): the player's own
        // pitch — the stand itself where it's set up
        // (table, crate, hand-painted sign), and at the
        // other permitted pitches a chalk outline marking
        // the spot a stand could go. Tapping either is a
        // fixture tap; Stand owns every rule behind it. ----
        if (Stand.locOf(st) == loc.id) {
            val sx = worldW * 0.16f
            val sy = h * 0.90f
            c.drawRect(sx - 46, sy - 38, sx + 46, sy,
                Paper.fillPaint("#8a5a30"))
            c.drawRect(sx - 46, sy - 38, sx + 46, sy - 30,
                Paper.fillPaint("#a06a38"))
            c.drawRect(sx - 40, sy - 30, sx - 34, sy + 8,
                Paper.fillPaint("#6e4520"))
            c.drawRect(sx + 34, sy - 30, sx + 40, sy + 8,
                Paper.fillPaint("#6e4520"))
            c.drawRect(sx - 58, sy - 62, sx + 58, sy - 40,
                Paper.fillPaint("#3f7a4d"))
            Paper.drawSign(c, sx, sy - 80, 132f, 24f,
                "YOUR STAND", 13f)
            glyph("🍋", sx - 26, sy - 48, 15f)
            glyph("🧾", sx + 2, sy - 48, 15f)
            glyph("📦", sx + 28, sy - 16, 16f)
            hits.add(Hit("fixture", "stand",
                RectF(sx - 62, sy - 90, sx + 62, sy + 10)))
        } else if (loc.id in Stand.STAND_LOCS) {
            val sx = worldW * 0.16f
            val sy = h * 0.90f
            val dash = Paper.strokePaint("#f4ecd8", 2.2f)
            c.drawLine(sx - 46, sy, sx + 46, sy, dash)
            c.drawLine(sx - 46, sy - 26, sx - 46, sy, dash)
            c.drawLine(sx + 46, sy - 26, sx + 46, sy, dash)
            Paper.drawSign(c, sx, sy - 44, 118f, 20f,
                "STAND PITCH", 11f)
            hits.add(Hit("fixture", "stand",
                RectF(sx - 56, sy - 56, sx + 56, sy + 8)))
        }

        // ---- YOUR GARDEN (v0.11.0b): four beds in the
        // yard at Your House — soil rows, the crop's glyph
        // by its growth stage, weeds as a few green strokes
        // when they've crept, a sparkle when ripe, a wilt
        // when the sky killed it. Garden owns every rule;
        // this only draws the record. ----
        if (loc.id == "YourHouse") {
            val gy = h * 0.90f
            for (i in 0 until Garden.BEDS) {
                val gx = worldW * (0.635f + 0.088f * i)
                val bw = worldW * 0.078f
                c.drawRect(gx - bw / 2, gy - 16, gx + bw / 2, gy,
                    Paper.fillPaint("#5a4028"))
                c.drawRect(gx - bw / 2, gy - 16, gx + bw / 2,
                    gy - 12, Paper.fillPaint("#6e5233"))
                val b = Garden.bed(st, i)
                if (b != null) {
                    val crop = Garden.cropById(b.cropId)
                    when {
                        b.dead -> glyph("🥀", gx, gy - 26, 17f)
                        b.ripe -> {
                            glyph(crop?.icon ?: "🌱", gx,
                                gy - 28, 19f)
                            glyph("✨", gx + 13, gy - 40, 10f)
                        }
                        crop != null &&
                            b.growth >= crop.growDays / 2 ->
                            glyph(crop.icon, gx, gy - 26, 17f)
                        else -> glyph("🌱", gx, gy - 24, 14f)
                    }
                    val weeds = Garden.weedLevel(st, b)
                    if (!b.dead && weeds > 0) {
                        val wp = Paper.strokePaint("#4d7a35", 2f)
                        for (k in 0 until weeds) {
                            val wx = gx - bw / 4 +
                                k * (bw / 4)
                            c.drawLine(wx, gy - 2, wx - 3,
                                gy - 10, wp)
                        }
                    }
                }
            }
            Paper.drawSign(c, worldW * 0.79f, gy - 62, 120f,
                22f, "YOUR GARDEN", 11f)
            hits.add(Hit("fixture", "garden",
                RectF(worldW * 0.58f, gy - 74,
                    worldW * 0.97f, gy + 10)))
        }

        // ---- THE PORCH COPY (v0.12.0a): this week's
        // Gazette, folded on the step beside your door —
        // drawn only while an edition stands (Newspaper
        // owns the record; this only draws it). It sits
        // there until the next edition replaces it. ----
        if (loc.id == "YourHouse" &&
            Newspaper.porchLines(st) != null) {
            val bx = worldW * 0.575f
            val by = h * 0.678f
            // the folded stack: two sheets, a red masthead
            // band on the top one, the name under the step
            c.drawRoundRect(RectF(bx - 26f, by - 6f, bx + 30f,
                by + 8f), 3f, 3f, Paper.fillPaint("#e3dac4"))
            c.drawRoundRect(RectF(bx - 30f, by - 13f, bx + 26f,
                by + 1f), 3f, 3f, Paper.fillPaint("#f4ecd8"))
            c.drawRect(bx - 30f, by - 13f, bx + 26f, by - 8f,
                Paper.fillPaint(Paper.RED))
            val gl = Paper.textPaint("#8a8272", h * 0.011f,
                bold = true, casual = true)
            gl.textAlign = Paint.Align.CENTER
            c.drawText("GAZETTE", bx - 2f, by + 15f, gl)
            hits.add(Hit("fixture", "paper",
                RectF(bx - 42f, by - 26f, bx + 42f, by + 24f)))
        }

        // ---- THE MAILBOX (v0.13.0e): your own box, out
        // front with its THE NEW KID plaque (SceneArt draws
        // the box itself) — the pen-pal program's front
        // door. A letter waiting rides it as a glyph over
        // the box, readable from the street. PenPals owns
        // every rule; this only draws the flag. ----
        if (loc.id == "YourHouse") {
            if (PenPals.unread(st)) glyph("✉️",
                worldW * 0.188f, h * 0.40f, 21f)
            hits.add(Hit("fixture", "mailbox",
                RectF(worldW * 0.10f, h * 0.42f,
                    worldW * 0.28f, h * 0.70f)))
        }

        // ---- #76 HARVEST SWAP TABLE on autumn weekends ----
        if (loc.id == "KidsPark" && season == Season.FALL && isWeekend(st) &&
            hour in 9..15) {
            val tx = worldW * 0.24f
            val ty = h * 0.90f
            c.drawRect(tx - 52, ty - 22, tx + 52, ty, Paper.fillPaint("#7a5230"))
            glyph("🎃", tx - 32, ty - 28, 17f)
            glyph("🌽", tx - 4, ty - 28, 17f)
            glyph("🍎", tx + 26, ty - 28, 17f)
            Paper.drawSign(c, tx, ty - 52, 130f, 22f, "HARVEST SWAP", 12f)
            hits.add(Hit("swap", loc.id, RectF(tx - 62, ty - 62, tx + 62, ty + 6)))
        }

        // ---- #65 benches you can actually sit on ----
        if (loc.id == "BusStop") {
            hits.add(Hit("bench", loc.id,
                RectF(worldW * 0.14f, h * 0.66f, worldW * 0.36f, h * 0.66f + 64)))
        }
        if (loc.id == "KidsPark") {
            // v0.15.0d: the park bench at world scale
            val bx = worldW * 0.60f
            val groundY = h * 0.915f
            val bLen = WorldScale.units(WorldScale.BENCH_LEN_U, h)
            val bH = WorldScale.units(WorldScale.BENCH_H_U, h)
            val seatY = groundY - bH * 0.47f
            val wood = Paper.fillPaint("#7a5230")
            c.drawRect(bx - bLen / 2, groundY - bH, bx + bLen / 2,
                groundY - bH * 0.76f, wood)
            c.drawRect(bx - bLen / 2, seatY, bx + bLen / 2,
                seatY + bH * 0.10f, wood)
            c.drawRect(bx - bLen / 2 + bH * 0.06f, seatY, bx - bLen / 2 + bH * 0.15f,
                groundY, wood)
            c.drawRect(bx + bLen / 2 - bH * 0.15f, seatY, bx + bLen / 2 - bH * 0.06f,
                groundY, wood)
            hits.add(Hit("bench", loc.id,
                RectF(bx - bLen / 2 - 10, groundY - bH - 8,
                    bx + bLen / 2 + 10, groundY + 10)))
        }

        // ---- #92 THE MYSTERY LUNCHBOX on the bus-stop bench
        // (completed v0.1.9a): sits all morning UNTIL today's kid
        // retrieves it on their way out — Engine.tickLivingWorld
        // sets life["lunchboxDay"], and the box vanishes ----
        if (loc.id == "BusStop" && tm in 420..630 &&
            st.life["lunchboxDay"] != st.absDay) {
            val bx = worldW * 0.28f
            val by = h * 0.66f + 26
            c.drawRect(bx - 13, by - 11, bx + 13, by + 3, Paper.fillPaint("#c23a54"))
            c.drawArc(RectF(bx - 7, by - 17, bx + 7, by - 6), 180f, 180f, false,
                Paper.strokePaint("#7a2436", 2.5f))
            glyph("?", bx, by + 1, 11f, "#ffffff")
        }

        // ---- #77 THE GROWING ICE SCULPTURE near Jimbo's ----
        if (loc.id == "JimbosGuns" && (st.gameMonth == 12 || st.gameMonth == 1)) {
            val stage = minOf(4, st.gameDay / 6)
            val sx = worldW * 0.80f
            val sy = h * 0.94f
            val ice = Paper.fillPaint("#bcd9f5", 235)
            when (stage) {
                0 -> c.drawRect(sx - 16, sy - 30, sx + 16, sy, ice)
                1 -> { c.drawRect(sx - 16, sy - 30, sx + 16, sy, ice)
                       c.drawCircle(sx, sy - 40, 10f, ice) }
                2 -> { c.drawCircle(sx, sy - 13, 15f, ice)
                       c.drawCircle(sx, sy - 37, 10.5f, ice) }
                3 -> { c.drawCircle(sx, sy - 13, 15f, ice)
                       c.drawCircle(sx, sy - 37, 10.5f, ice)
                       glyph("🦌", sx, sy - 56, 16f) }
                else -> { c.drawCircle(sx, sy - 13, 15f, ice)
                       c.drawCircle(sx, sy - 37, 10.5f, ice)
                       glyph("🦌", sx, sy - 56, 18f)
                       glyph("✨", sx + 22, sy - 44, 13f) }
            }
        }

        // ---- #47 THE FLICKERING STREETLIGHT (one per night) ----
        if (night) {
            val allLocs = LOCATIONS.keys.toList()
            val flickLoc = allLocs[((day % allLocs.size) + allLocs.size) % allLocs.size]
            val lx = worldW * 0.06f
            val ly = h * 0.46f
            val lp = Paper.strokePaint("#22262e", 5f)
            c.drawLine(lx, h * 0.66f, lx, ly, lp)
            c.drawLine(lx, ly, lx + 20, ly, lp)
            val fixed = st.lifeText["lampFixed:$day"] == loc.id
            val on = if (loc.id == flickLoc && !fixed) {
                Random(tick / 3).nextFloat() > 0.35f
            } else true
            if (on) {
                c.drawCircle(lx + 22, ly + 3, 15f, Paper.fillPaint("#f2c230", 60))
                c.drawCircle(lx + 22, ly + 3, 5f, Paper.fillPaint("#ffe9a8"))
            }
            if (loc.id == flickLoc && !fixed) {
                hits.add(Hit("lamp", loc.id, RectF(lx - 18, ly - 16, lx + 44, h * 0.66f)))
            }
        }

        // ---- #83 CHURCH BELL at noon ----
        if (loc.id == "Church" && tm in 714..729) {
            val bx = worldW * 0.5f
            val by = h * 0.40f
            for (i in 0..2) {
                val r = ((tick % 60) / 60f + i * 0.33f) * h * 0.10f
                c.drawCircle(bx, by, r, Paper.strokePaint("#f2c230", 2.5f,
                    (120 - r).toInt().coerceIn(15, 120)))
            }
            glyph("🔔", bx, by + 6, 18f)
        }

        // ---- v0.16.0a (item 2): the old #84 bus sweep
        // that crossed the scene at h*0.80 — through the
        // walk band and the grass — is REMOVED whole. The
        // bus now runs ONLY in Depth's traffic layer, on
        // the road (see drawTraffic's route pass). ----

        // ---- #46 COYOTE HOUR, deep night ----
        if ((hour >= 23 || hour < 4) && scatter(46) < 0.4f) {
            val sweep = ((millis / 90) % 160) / 160f
            val cxp = -60f + sweep * (worldW + 120f)
            val cyp = h * 0.545f
            val sil = Paper.fillPaint("#101623", 200)
            c.drawOval(RectF(cxp - 20, cyp - 8, cxp + 16, cyp + 5), sil)
            c.drawCircle(cxp + 19, cyp - 10, 6f, sil)
            c.drawRect(cxp - 24, cyp - 4, cxp - 16, cyp + 1, sil)
        }

        // ---- #97 THE NEW KID FAN CLUB (completed v0.1.9a):
        // they trail your route one stop behind — creeping in at
        // the screen's edge when you arrive, settling into place
        // once you've stopped, notebooks out… and they SCATTER
        // the moment you "turn around" (a fast camera pan),
        // leaving one dropped notebook behind ----
        if (TownLife.fanClubActive(st)) {
            val scattered = tick < fanScatterTick + 60
            val settled = tick - fanArriveTick > 360
            if (scattered) {
                val nx = camX + width * 0.10f
                glyph("📓", nx, h * 0.90f, 13f)
                glyph("💨", nx + 26, h * 0.885f, 12f)
            } else {
                for (i in 0..2) {
                    val fx = if (settled) worldW * (0.10f + i * 0.085f)
                        else camX + width * (0.03f + i * 0.045f)
                    val fy = h * 0.80f
                    val crouch = if (settled) 0f else h * 0.012f
                    c.drawCircle(fx, fy - 20 + crouch, 8f, Paper.fillPaint("#3a4a6a"))
                    c.drawRect(fx - 8, fy - 13 + crouch, fx + 8, fy + 8,
                        Paper.fillPaint("#2c3a5e"))
                    glyph("📝", fx + 12, fy - 12 + crouch, 12f)
                }
                if (settled) glyph("FAN CLUB — DO NOT NOTICE US",
                    worldW * 0.19f, h * 0.735f, 10f, "#ffe9a8")
            }
        }

        // ---- #62 CHALK DRAWINGS (new v0.1.9a): sidewalk slabs at
        // four spots hold the town's chalk. Yours (tap to place)
        // stays for days, fades out — and other kids ADD to it
        // while you're not looking. ----
        if (loc.id in TownLife.CHALK_SPOTS) {
            val (sx, sy) = when (loc.id) {
                "BusStop" -> worldW * 0.47f to h * 0.955f
                "Elementary" -> worldW * 0.50f to h * 0.95f
                "KidsPark" -> worldW * 0.44f to h * 0.935f
                else -> worldW * 0.34f to h * 0.95f
            }
            c.drawRoundRect(RectF(sx - worldW * 0.055f, sy - h * 0.016f,
                sx + worldW * 0.055f, sy + h * 0.016f), 8f, 8f,
                Paper.fillPaint("#9aa0a4", 200))
            TownLife.chalkShown(st, loc.id).forEachIndexed { i, doodle ->
                drawChalkDoodle(c, doodle,
                    sx + (i - 1) * worldW * 0.032f, sy, h * 0.013f)
            }
            hits.add(Hit("chalk", loc.id,
                RectF(sx - worldW * 0.06f, sy - h * 0.032f,
                    sx + worldW * 0.06f, sy + h * 0.032f)))
        }

        // ---- #93 THE LOST MITTEN TREE (new v0.1.9a): one pine at
        // the park wears every lost mitten the town hangs on it
        // (tap it holding a Lost Mitten to add yours) ----
        if (loc.id == "KidsPark") {
            val tx = worldW * 0.865f
            val ty = h * 0.905f
            SceneArt.drawPine(c, tx, ty, h * 0.30f, night)
            val hung = TownLife.mittensHung(st)
            val mcols = listOf("#c22e2e", "#2a7a8a", "#e8c53a", "#c86a9a",
                "#5aa85a", "#7a4a9a")
            for (i in 0 until minOf(hung, TownLife.MAX_MITTENS)) {
                val mx = tx + ((i / 3) - 1.5f) * h * 0.032f + (i % 2) * 7f
                val my = ty - h * (0.10f + (i % 3) * 0.048f)
                val mc = Paper.fillPaint(mcols[i % mcols.size])
                c.drawRoundRect(RectF(mx - 5, my - 7, mx + 5, my + 6), 4f, 4f, mc)
                c.drawCircle(mx + 6, my + 1, 3.2f, mc)
            }
            hits.add(Hit("mittentree", loc.id,
                RectF(tx - h * 0.05f, ty - h * 0.31f, tx + h * 0.05f, ty + 6)))
            if (hung > 0) glyph("🧤×$hung", tx, ty - h * 0.325f, 11f, "#ffe9a8")
        }
    }

    /** v0.1.9a #62: one chalk doodle, wobbly pastel strokes. */
    private fun drawChalkDoodle(c: Canvas, idx: Int, cx: Float, cy: Float, r: Float) {
        val cols = listOf("#f4f4f4", "#f2a8c0", "#a8c8f2", "#f2e2a0")
        val cp = Paper.strokePaint(cols[idx % cols.size], 2.6f)
        cp.alpha = 205
        when (idx % 8) {
            0 -> { // star
                val path = Path()
                for (k in 0..10) {
                    val a = -Math.PI / 2 + k * Math.PI / 5
                    val rr = if (k % 2 == 0) r else r * 0.45f
                    val px = cx + (rr * kotlin.math.cos(a)).toFloat()
                    val py = cy + (rr * kotlin.math.sin(a)).toFloat()
                    if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                c.drawPath(path, cp)
            }
            1 -> SceneArt.drawHeart(c, cx, cy, r * 1.1f,
                Paper.fillPaint(cols[idx % cols.size], 190))
            2 -> { // spiral
                val path = Path()
                var a = 0.0
                while (a < Math.PI * 5) {
                    val rr = (a / (Math.PI * 5)).toFloat() * r
                    val px = cx + (rr * kotlin.math.cos(a)).toFloat()
                    val py = cy + (rr * kotlin.math.sin(a)).toFloat()
                    if (a == 0.0) path.moveTo(px, py) else path.lineTo(px, py)
                    a += 0.35
                }
                c.drawPath(path, cp)
            }
            3 -> { // smiley
                c.drawCircle(cx, cy, r * 0.85f, cp)
                c.drawCircle(cx - r * 0.3f, cy - r * 0.25f, 1.8f,
                    Paper.fillPaint(cols[idx % cols.size], 200))
                c.drawCircle(cx + r * 0.3f, cy - r * 0.25f, 1.8f,
                    Paper.fillPaint(cols[idx % cols.size], 200))
                c.drawArc(RectF(cx - r * 0.45f, cy - r * 0.1f, cx + r * 0.45f,
                    cy + r * 0.55f), 15f, 150f, false, cp)
            }
            4 -> { // house
                c.drawRect(cx - r * 0.6f, cy - r * 0.1f, cx + r * 0.6f, cy + r * 0.7f, cp)
                c.drawPath(Path().apply {
                    moveTo(cx - r * 0.8f, cy - r * 0.1f); lineTo(cx, cy - r * 0.75f)
                    lineTo(cx + r * 0.8f, cy - r * 0.1f)
                }, cp)
                c.drawRect(cx - r * 0.15f, cy + r * 0.15f, cx + r * 0.15f, cy + r * 0.7f, cp)
            }
            5 -> { // flower
                for (k in 0..5) {
                    val a = k * Math.PI / 3
                    c.drawCircle(cx + (r * 0.5f * kotlin.math.cos(a)).toFloat(),
                        cy + (r * 0.5f * kotlin.math.sin(a)).toFloat(), r * 0.32f, cp)
                }
                c.drawCircle(cx, cy, r * 0.25f,
                    Paper.fillPaint("#f2e2a0", 200))
            }
            6 -> { // fish
                c.drawOval(RectF(cx - r * 0.8f, cy - r * 0.4f, cx + r * 0.3f, cy + r * 0.4f), cp)
                c.drawPath(Path().apply {
                    moveTo(cx + r * 0.3f, cy); lineTo(cx + r * 0.85f, cy - r * 0.45f)
                    lineTo(cx + r * 0.85f, cy + r * 0.45f); close()
                }, cp)
                c.drawCircle(cx - r * 0.45f, cy - r * 0.1f, 1.8f,
                    Paper.fillPaint(cols[idx % cols.size], 200))
            }
            else -> { // sun
                c.drawCircle(cx, cy, r * 0.45f, cp)
                for (k in 0..7) {
                    val a = k * Math.PI / 4
                    c.drawLine(cx + (r * 0.6f * kotlin.math.cos(a)).toFloat(),
                        cy + (r * 0.6f * kotlin.math.sin(a)).toFloat(),
                        cx + (r * 0.95f * kotlin.math.cos(a)).toFloat(),
                        cy + (r * 0.95f * kotlin.math.sin(a)).toFloat(), cp)
                }
            }
        }
        cp.alpha = 255
    }

    /** Phase 2 interiors: #85 kitchen clatter at meal hours in
     *  occupied homes, drifting up as steam-and-utensil glyphs. */
    /** v0.2.0e: the TACO CATCH cabinet inside Freeman's Tacos —
     *  a chunky upright with a lit marquee, a screen mid-game
     *  (a taco, falling forever, toward a tray that believes),
     *  and a coin slot that has never once been fed. */
    private fun drawArcadeCabinet(c: Canvas, worldW: Float, h: Float) {
        val cx = worldW * 0.865f
        val baseY = h * 0.94f
        val cw = worldW * 0.085f
        val chh = h * 0.30f
        val left = cx - cw / 2f
        val top = baseY - chh
        // body + side shade
        c.drawRoundRect(RectF(left, top, left + cw, baseY), 8f, 8f,
            Paper.fillPaint("#37455c"))
        c.drawRoundRect(RectF(left + cw * 0.78f, top, left + cw, baseY), 8f, 8f,
            Paper.fillPaint("#2a3648"))
        // marquee
        c.drawRoundRect(RectF(left + 4f, top + 4f, left + cw - 4f, top + chh * 0.16f),
            6f, 6f, Paper.fillPaint(Paper.RED))
        val mp = Paper.textPaint("#ffffff", 12.5f, bold = true, casual = true)
        mp.textAlign = Paint.Align.CENTER
        c.drawText("TACO CATCH", cx, top + chh * 0.115f, mp)
        // screen: a taco mid-fall toward a very hopeful tray
        val scr = RectF(left + cw * 0.14f, top + chh * 0.22f,
            left + cw * 0.86f, top + chh * 0.62f)
        c.drawRoundRect(scr, 5f, 5f, Paper.fillPaint("#12262b"))
        val bobY = scr.top + scr.height() *
            (0.25f + 0.18f * kotlin.math.sin(tick * 0.11f).toFloat())
        val tp = Paper.textPaint("#ffffff", 19f, bold = true, casual = true)
        tp.textAlign = Paint.Align.CENTER
        c.drawText("🌮", scr.centerX(), bobY, tp)
        c.drawRect(scr.left + scr.width() * 0.28f, scr.bottom - scr.height() * 0.16f,
            scr.right - scr.width() * 0.28f, scr.bottom - scr.height() * 0.09f,
            Paper.fillPaint("#c8b088"))
        // control deck + the slot that pays out instead of in
        c.drawRect(left + cw * 0.10f, top + chh * 0.68f, left + cw * 0.90f,
            top + chh * 0.74f, Paper.fillPaint("#c8b088"))
        c.drawCircle(cx, top + chh * 0.71f, 5f, Paper.fillPaint(Paper.RED))
        val sp = Paper.textPaint("#ffe9a8", 10.5f, bold = true, casual = true)
        sp.textAlign = Paint.Align.CENTER
        c.drawText("🪙 FREE TO PLAY · PAYS IN TACOS*", cx, top + chh * 0.85f, sp)
        c.drawText("*coins", cx, top + chh * 0.93f, sp)
        hits.add(Hit("arcade", Minigames.TACO_LOC,
            RectF(left - 14, top - 10, left + cw + 14, baseY + 8)))
    }

    /** v0.3.0d: the four working fixtures inside YOUR house.
     *  Drawn over the room (InteriorArt) and under the figures,
     *  in the same world space. Everything they show derives
     *  from the save through House — the shelf holds what you've
     *  actually collected; empty slots stay empty shelf. */
    private fun drawHouseFixtures(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val night = SceneArt.timeOfDay(st.timeMin) == "night"
        val gp = Paper.textPaint("#ffffff", h * 0.030f, bold = true, casual = true)
        gp.textAlign = Paint.Align.CENTER

        // ---- the BED (left floor): sleep until morning ----
        run {
            val l = worldW * 0.030f; val r = worldW * 0.238f
            val base = h * 0.935f; val top = h * 0.715f
            // headboard + frame
            c.drawRoundRect(RectF(l, top, l + worldW * 0.030f, base), 7f, 7f,
                Paper.fillPaint("#6b4a2f"))
            c.drawRoundRect(RectF(l + worldW * 0.012f, top + h * 0.055f, r, base),
                7f, 7f, Paper.fillPaint("#4a3423"))
            // mattress + pillow + blanket (the house accent green)
            c.drawRoundRect(RectF(l + worldW * 0.026f, top + h * 0.085f,
                r - worldW * 0.008f, base - h * 0.035f), 6f, 6f,
                Paper.fillPaint("#f0e8d0"))
            c.drawRoundRect(RectF(l + worldW * 0.034f, top + h * 0.098f,
                l + worldW * 0.082f, top + h * 0.148f), 5f, 5f,
                Paper.fillPaint("#ffffff"))
            c.drawRoundRect(RectF(l + worldW * 0.088f, top + h * 0.092f,
                r - worldW * 0.008f, base - h * 0.035f), 6f, 6f,
                Paper.fillPaint("#7ac74f"))
            c.drawRect(l + worldW * 0.088f, base - h * 0.062f,
                r - worldW * 0.008f, base - h * 0.035f,
                Paper.fillPaint("#5da23e"))
            val lp = Paper.textPaint("#f4ecd8", h * 0.016f, bold = true, casual = true)
            lp.textAlign = Paint.Align.CENTER
            c.drawText("🛏️ SLEEP", (l + r) / 2f + worldW * 0.012f, base + h * 0.026f, lp)
            if (night) {
                val zp = Paper.textPaint("#bcd2ff", h * 0.020f, bold = true, casual = true)
                zp.textAlign = Paint.Align.CENTER
                val bob = kotlin.math.sin(tick * 0.07f).toFloat() * 3f
                c.drawText("z", l + worldW * 0.055f, top + h * 0.045f + bob, zp)
                c.drawText("Z", l + worldW * 0.075f, top + h * 0.018f + bob, zp)
            }
            hits.add(Hit("fixture", "bed",
                RectF(l - 10, top - 12, r + 10, base + h * 0.04f)))
        }

        // ---- the TROPHY SHELF (wall, center-left) ----
        run {
            val l = worldW * 0.285f; val r = worldW * 0.555f
            val slotW = (r - l) / 6f
            fun plank(yTop: Float) {
                c.drawRect(l - 6f, yTop, r + 6f, yTop + h * 0.016f,
                    Paper.fillPaint("#6b4a2f"))
                c.drawRect(l - 6f, yTop + h * 0.016f, r + 6f, yTop + h * 0.020f,
                    Paper.fillPaint("#4a3423"))
            }
            // top plank: feat cups (photo feats + game feats)
            val cupY = h * 0.168f
            plank(cupY)
            val cups = House.featCups(st)
            for (i in 0 until cups) {
                c.drawText("🏆", l + slotW * (i + 0.5f), cupY - h * 0.004f, gp)
            }
            val overflow = House.featOverflow(st)
            if (overflow > 0) {
                val op = Paper.textPaint("#f2c230", h * 0.020f, bold = true, casual = true)
                op.textAlign = Paint.Align.CENTER
                c.drawText("+$overflow", l + slotW * 5.5f, cupY - h * 0.006f, op)
            }
            // two planks of collectibles: earned icons stand on
            // the wood; the rest is honest empty shelf space
            val slots = House.shelfSlots(st)
            val plankYs = listOf(h * 0.263f, h * 0.358f)
            for (row in 0..1) {
                plank(plankYs[row])
                for (i in 0..5) {
                    val icon = slots.getOrNull(row * 6 + i) ?: continue
                    c.drawText(icon, l + slotW * (i + 0.5f),
                        plankYs[row] - h * 0.004f, gp)
                }
            }
            hits.add(Hit("fixture", "shelf",
                RectF(l - 12, h * 0.125f, r + 12, h * 0.378f)))
        }

        // ---- the ATTIC (v0.12.0e: left wall, above the bed) —
        // the narrow door and the three steps up. A fixture
        // like the darkroom and the porch paper: it opens
        // Attic.pageLines (derived shelves only — the
        // overflow shelf, the family frame's outtakes, the
        // stored case boxes). Solids only — plain wood, one
        // brass knob, three steps; no pattern, no motif. ----
        run {
            val l = worldW * 0.045f; val r = worldW * 0.200f
            val t = h * 0.130f; val b = h * 0.300f
            // a soft inset panel so the wall fixture reads
            c.drawRoundRect(RectF(l - 8f, t - 8f, r + 8f, b + h * 0.032f),
                8f, 8f, Paper.fillPaint("#d9c9a3", 150))
            // the door: frame, panel, one brass knob
            c.drawRoundRect(RectF(l, t, r, b), 6f, 6f,
                Paper.fillPaint("#6b4a2f"))
            c.drawRect(l + 7f, t + 7f, r - 7f, b - 7f,
                Paper.fillPaint("#7a5230"))
            c.drawCircle(r - worldW * 0.022f, (t + b) / 2f, 4.5f,
                Paper.fillPaint("#c9a227"))
            // the three steps up: plain treads, climbing
            // the door's lower half toward the right
            val stepH = (b - t) / 9f
            for (i in 0..2) {
                val sx = l + worldW * 0.018f + i * (r - l - worldW * 0.036f) / 3f
                val sy = b - 8f - i * stepH * 1.6f
                c.drawRect(sx, sy - stepH, sx + (r - l - worldW * 0.036f) / 3f,
                    sy, Paper.fillPaint("#4a3423"))
            }
            val ap = Paper.textPaint("#f4ecd8", h * 0.0145f,
                bold = true, casual = true)
            ap.textAlign = Paint.Align.CENTER
            c.drawText("🪜 ATTIC", (l + r) / 2f, b + h * 0.024f, ap)
            hits.add(Hit("fixture", "attic",
                RectF(l - 10, t - 10, r + 10, b + h * 0.035f)))
        }

        // ---- the MIRROR (right wall strip): the creator ----
        run {
            val l = worldW * 0.895f; val r = worldW * 0.968f
            val t = h * 0.125f; val b = h * 0.365f
            c.drawRoundRect(RectF(l, t, r, b), 16f, 16f, Paper.fillPaint("#6b4a2f"))
            c.drawRoundRect(RectF(l + 6f, t + 6f, r - 6f, b - 6f), 12f, 12f,
                Paper.fillPaint(if (night) "#5a7396" else "#a8cdea"))
            // v0.14.0a: YOUR REFLECTION stands in the glass —
            // the look the moment is wearing: your pajamas
            // while a sleepover night is live at the house
            // (Sleepwear.mirrorCode — the shutter's own
            // derivation, so the glass and the album can
            // never disagree), your ordinary day look on
            // every other terms. Mirrored around its own
            // center, the way a reflection is, and clipped
            // to the glass.
            // v0.15.0a: the glass now reads LiveLook — the
            // ONE preview derivation — so on a patrol night
            // the reflection wears your hero costume too,
            // from the same code the street and the shutter
            // use. The sleep branch's code is mirrorCode's
            // own (the suite pins them equal figure-for-
            // figure); LiveLook is the door, not a second
            // derivation.
            val meRef = st.agents.values.firstOrNull { it.isPlayer }
            if (meRef != null) {
                val spec = avatarSpec(meRef.appearance)
                val look = LiveLook.playerLook(st)
                val figH = minOf((b - t) * 0.94f, (r - l) * 1.28f)
                c.save()
                c.clipRect(RectF(l + 6f, t + 6f, r - 6f, b - 6f))
                c.translate((l + r) / 2f, 0f)
                c.scale(-1f, 1f)
                c.translate(-(l + r) / 2f, 0f)
                when (look.kind) {
                    LiveLook.SLEEP ->
                        KidArt.drawSleepFigure(c, spec, look.code,
                            (l + r) / 2f, (t + b) / 2f, figH)
                    LiveLook.HERO ->
                        KidArt.drawHeroFigure(c, spec, look.code,
                            (l + r) / 2f, (t + b) / 2f, figH)
                    else ->
                        KidArt.drawFigure(c, spec,
                            (l + r) / 2f, (t + b) / 2f, figH, "idle")
                }
                c.restore()
            }
            val shine = Paper.strokePaint("#ffffff", 4f, 150)
            c.drawLine(l + (r - l) * 0.30f, b - (b - t) * 0.22f,
                l + (r - l) * 0.55f, t + (b - t) * 0.30f, shine)
            c.drawLine(l + (r - l) * 0.48f, b - (b - t) * 0.18f,
                l + (r - l) * 0.66f, t + (b - t) * 0.26f, shine)
            hits.add(Hit("fixture", "mirror",
                RectF(l - 12, t - 10, r + 12, b + 10)))
        }

        // ---- the ALBUM BOOK (on the desk, prop slot 4) ----
        run {
            val cx = worldW * 0.677f
            val base = h * 0.589f
            val bw2 = worldW * 0.042f; val bh2 = h * 0.048f
            c.drawRoundRect(RectF(cx - bw2 / 2f, base - bh2, cx + bw2 / 2f, base),
                4f, 4f, Paper.fillPaint(Paper.RED))
            c.drawRect(cx + bw2 * 0.30f, base - bh2 + 3f, cx + bw2 / 2f - 2f,
                base - 3f, Paper.fillPaint("#f4ecd8"))
            c.drawRect(cx - bw2 / 2f, base - bh2, cx - bw2 / 2f + 5f, base,
                Paper.fillPaint("#8e2432"))
            val bp = Paper.textPaint("#ffffff", h * 0.020f, bold = true, casual = true)
            bp.textAlign = Paint.Align.CENTER
            c.drawText("📸", cx - bw2 * 0.06f, base - bh2 * 0.38f, bp)
            val lp = Paper.textPaint("#f4ecd8", h * 0.0135f, bold = true, casual = true)
            lp.textAlign = Paint.Align.CENTER
            c.drawText("ALBUM", cx, base + h * 0.020f, lp)
            hits.add(Hit("fixture", "album",
                RectF(cx - bw2 / 2f - 14, base - bh2 - 12, cx + bw2 / 2f + 14,
                    base + h * 0.03f)))
        }

        // ---- the DINING TABLE (v0.6.0d: right floor) ----
        // Family evenings are hosted from here: the table is
        // where you invite a household to yours. Smooth
        // shapes only — a plain cloth, four honest plates.
        run {
            val l = worldW * 0.600f; val r = worldW * 0.868f
            val base = h * 0.935f; val top = h * 0.760f
            // two chairs tucked behind, two in front
            for (cx in listOf(l + (r - l) * 0.22f, l + (r - l) * 0.78f)) {
                c.drawRoundRect(RectF(cx - worldW * 0.026f, top - h * 0.045f,
                    cx + worldW * 0.026f, top + h * 0.030f), 6f, 6f,
                    Paper.fillPaint("#5a3d24"))
            }
            // legs + cloth top
            c.drawRect(l + worldW * 0.018f, top + h * 0.055f,
                l + worldW * 0.034f, base, Paper.fillPaint("#4a3423"))
            c.drawRect(r - worldW * 0.034f, top + h * 0.055f,
                r - worldW * 0.018f, base, Paper.fillPaint("#4a3423"))
            c.drawRoundRect(RectF(l, top, r, top + h * 0.062f), 8f, 8f,
                Paper.fillPaint("#f0e8d0"))
            c.drawRect(l, top + h * 0.040f, r, top + h * 0.062f,
                Paper.fillPaint("#e3d5ae"))
            // four place settings: plate + cup, simply drawn
            for (fx in listOf(0.16f, 0.38f, 0.62f, 0.84f)) {
                val cx = l + (r - l) * fx
                c.drawOval(RectF(cx - worldW * 0.020f, top + h * 0.014f,
                    cx + worldW * 0.020f, top + h * 0.048f),
                    Paper.fillPaint("#ffffff"))
                c.drawOval(RectF(cx - worldW * 0.011f, top + h * 0.021f,
                    cx + worldW * 0.011f, top + h * 0.041f),
                    Paper.fillPaint("#dfe9ee"))
            }
            // a serving dish in the middle — supper's promise
            val ccx = (l + r) / 2f
            c.drawOval(RectF(ccx - worldW * 0.030f, top + h * 0.010f,
                ccx + worldW * 0.030f, top + h * 0.042f),
                Paper.fillPaint(Paper.RED))
            c.drawOval(RectF(ccx - worldW * 0.020f, top + h * 0.006f,
                ccx + worldW * 0.020f, top + h * 0.030f),
                Paper.fillPaint("#f4ecd8"))
            val lp = Paper.textPaint("#f4ecd8", h * 0.016f, bold = true, casual = true)
            lp.textAlign = Paint.Align.CENTER
            c.drawText("🍽️ TABLE", (l + r) / 2f, base + h * 0.026f, lp)
            hits.add(Hit("fixture", "table",
                RectF(l - 10, top - h * 0.05f, r + 10, base + h * 0.04f)))
        }

        // ---- the DARKROOM (v0.9.0d: center wall, low): a
        // curtained corner in safelight red — one bulb, a
        // line of drying prints, a counter of trays. Smooth
        // shapes only, nothing clustered: the room's quietest
        // corner does the house's slowest work. The papers
        // on the line are honest: one per print you hold,
        // blank paper while the box is empty. ----
        run {
            val l = worldW * 0.385f; val r = worldW * 0.535f
            val t = h * 0.505f; val base = h * 0.695f
            // the alcove: near-black maroon, red within
            c.drawRoundRect(RectF(l, t, r, base), 8f, 8f,
                Paper.fillPaint("#2a1016"))
            c.drawRoundRect(RectF(l + 4f, t + 4f, r - 4f, base - 4f),
                6f, 6f, Paper.fillPaint("#571c26"))
            // the safelight: one bulb, one halo
            val bcx = (l + r) / 2f; val bcy = t + h * 0.038f
            c.drawOval(RectF(bcx - worldW * 0.030f, bcy - h * 0.026f,
                bcx + worldW * 0.030f, bcy + h * 0.026f),
                Paper.fillPaint("#a12333", 90))
            c.drawOval(RectF(bcx - worldW * 0.011f, bcy - h * 0.010f,
                bcx + worldW * 0.011f, bcy + h * 0.010f),
                Paper.fillPaint("#ff5340"))
            // the drying line + its papers
            val lineY = t + h * 0.085f
            c.drawLine(l + worldW * 0.012f, lineY, r - worldW * 0.012f,
                lineY, Paper.strokePaint("#d8cdb2", 2f))
            val papers = maxOf(1, minOf(3, Darkroom.held(st).size))
            for (i in 0 until papers) {
                val pcx = l + (r - l) * (i + 0.5f) / papers
                c.drawRect(RectF(pcx - worldW * 0.016f, lineY,
                    pcx + worldW * 0.016f, lineY + h * 0.036f),
                    Paper.fillPaint("#e8ddc4"))
                c.drawRect(pcx - worldW * 0.004f, lineY - h * 0.006f,
                    pcx + worldW * 0.004f, lineY + h * 0.002f,
                    Paper.fillPaint("#8a7350"))
            }
            // the counter of trays: three shallow baths in a
            // row, each with its sheen — developer, stop, fix
            c.drawRect(l + worldW * 0.008f, base - h * 0.052f,
                r - worldW * 0.008f, base - h * 0.006f,
                Paper.fillPaint("#3a2418"))
            val trayW = (r - l - worldW * 0.032f) / 3f
            for (i in 0..2) {
                val tx = l + worldW * 0.012f + i * (trayW + worldW * 0.004f)
                c.drawRoundRect(RectF(tx, base - h * 0.044f,
                    tx + trayW, base - h * 0.014f), 3f, 3f,
                    Paper.fillPaint("#7a2a33"))
                c.drawLine(tx + trayW * 0.15f, base - h * 0.036f,
                    tx + trayW * 0.85f, base - h * 0.036f,
                    Paper.strokePaint("#e88a7a", 2f, 160))
            }
            val lp = Paper.textPaint("#f4b8a8", h * 0.0145f,
                bold = true, casual = true)
            lp.textAlign = Paint.Align.CENTER
            c.drawText("🌑 DARKROOM", (l + r) / 2f, base + h * 0.024f, lp)
            hits.add(Hit("fixture", "darkroom",
                RectF(l - 10, t - 10, r + 10, base + h * 0.035f)))
        }

        // ---- the PRINT WALL (v0.9.0d: right of the darkroom,
        // below the calendar, beside the yearbook shelf):
        // the darkroom's harvest. A hung print is painted by
        // PhotoView's OWN renderer off the print's frozen
        // record — captioned by its chin stamp like the
        // album's — with a small brass plate under a print
        // the yearbook crowned. Empty slots stay frames. ----
        run {
            val l = worldW * 0.555f; val r = worldW * 0.775f
            val t = h * 0.530f
            c.drawRoundRect(RectF(l - 6f, t - h * 0.032f, r + 6f,
                t + h * 0.130f), 8f, 8f, Paper.fillPaint("#4a3423"))
            val hp2 = Paper.textPaint("#f2c230", h * 0.0145f,
                bold = true, casual = true)
            hp2.textAlign = Paint.Align.CENTER
            c.drawText("🖼 PRINTS", (l + r) / 2f, t - h * 0.009f, hp2)
            val wallPrints = Darkroom.wall(st)
            val slotW = (r - l) / 3f
            val pw = slotW * 0.80f; val ph = h * 0.098f
            for (i in 0 until Darkroom.WALL_CAP) {
                val cx = l + slotW * (i + 0.5f)
                val rect = RectF(cx - pw / 2f, t, cx + pw / 2f, t + ph)
                val p = wallPrints.getOrNull(i)
                if (p != null) {
                    photoRenderer.drawInto(c, p.record, rect)
                    if (Darkroom.isYearbookWinner(st, p)) {
                        c.drawRoundRect(RectF(cx - pw * 0.30f,
                            t + ph + h * 0.005f, cx + pw * 0.30f,
                            t + ph + h * 0.017f), 3f, 3f,
                            Paper.fillPaint("#c9a227"))
                        c.drawRect(cx - pw * 0.30f, t + ph + h * 0.005f,
                            cx + pw * 0.30f, t + ph + h * 0.009f,
                            Paper.fillPaint("#e8cc5e"))
                    }
                } else {
                    c.drawRoundRect(rect, 4f, 4f,
                        Paper.strokePaint("#8a7350", 2f, 130))
                    val ep = Paper.textPaint("#8a7350", h * 0.012f,
                        casual = true)
                    ep.textAlign = Paint.Align.CENTER
                    c.drawText("—", cx, t + ph * 0.55f, ep)
                }
            }
            hits.add(Hit("fixture", "printwall",
                RectF(l - 12, t - h * 0.036f, r + 12, t + h * 0.137f)))
        }

        // ---- the FAMILY CORNER (v0.10.0a: appears the day
        // your family moves in, gated on Family.arrived) —
        // their coats on hooks by the trophy shelf, their mugs
        // on the dining table, and the family frame on the
        // left wall: EMPTY until a real family photo exists,
        // then painted by PhotoView's own renderer off the
        // newest one, like the print wall paints prints. ----
        if (Family.arrived(st)) run {
            // the frame (left wall, above the bed)
            val fl = worldW * 0.045f; val fr = worldW * 0.225f
            val ft = h * 0.430f; val fb = h * 0.610f
            c.drawRoundRect(RectF(fl - 5f, ft - 5f, fr + 5f, fb + 5f),
                6f, 6f, Paper.fillPaint("#6b4a2f"))
            val fam = Family.latestFamilyPhoto(st)
            if (fam != null) {
                photoRenderer.drawInto(c, fam,
                    RectF(fl, ft, fr, fb))
            } else {
                c.drawRect(fl, ft, fr, fb, Paper.fillPaint("#e8dcc2"))
                val ep = Paper.textPaint("#8a7350", h * 0.0135f,
                    bold = true, casual = true)
                ep.textAlign = Paint.Align.CENTER
                c.drawText("📸", (fl + fr) / 2f, (ft + fb) / 2f - h * 0.008f, ep)
                c.drawText("FAMILY", (fl + fr) / 2f,
                    (ft + fb) / 2f + h * 0.020f, ep)
            }
            val cp2 = Paper.textPaint("#f4ecd8", h * 0.0135f,
                bold = true, casual = true)
            cp2.textAlign = Paint.Align.CENTER
            c.drawText(if (fam != null) "👨‍👩‍👧 FAMILY · ${fam.dateLabel}"
                else "👨‍👩‍👧 FAMILY — AWAITING ITS FIRST PHOTO",
                (fl + fr) / 2f, fb + h * 0.024f, cp2)
            hits.add(Hit("fixture", "familyframe",
                RectF(fl - 10, ft - 10, fr + 10, fb + h * 0.03f)))
            // the coats (hooks on the strip right of the bed)
            val momSpec = Family.specFor(st, Family.MOM)
            val dadSpec = Family.specFor(st, Family.DAD)
            for ((i, spec) in listOf(momSpec, dadSpec).withIndex()) {
                val cx = worldW * (0.252f + i * 0.026f)
                c.drawCircle(cx, h * 0.455f, 3.5f,
                    Paper.fillPaint("#c9a86a"))
                // shoulders + body of a hanging coat
                val path = Path().apply {
                    moveTo(cx - worldW * 0.010f, h * 0.470f)
                    quadTo(cx, h * 0.458f, cx + worldW * 0.010f,
                        h * 0.470f)
                    lineTo(cx + worldW * 0.012f, h * 0.560f)
                    lineTo(cx - worldW * 0.012f, h * 0.560f)
                    close()
                }
                c.drawPath(path, Paper.fillPaint(spec.top))
                c.drawPath(path, Paper.strokePaint(
                    darkerHex(spec.top, 0.7f), 2f, 200))
            }
            // their mugs on the dining table (beside yours)
            for ((i, spec) in listOf(momSpec, dadSpec).withIndex()) {
                val mx = worldW * (0.700f + i * 0.055f)
                val my = h * 0.630f
                c.drawRoundRect(RectF(mx, my, mx + worldW * 0.016f,
                    my + h * 0.020f), 3f, 3f, Paper.fillPaint(spec.top))
                c.drawArc(RectF(mx + worldW * 0.013f, my + h * 0.004f,
                    mx + worldW * 0.024f, my + h * 0.016f),
                    -70f, 140f, false,
                    Paper.strokePaint(spec.top, 2.5f, 220))
            }
        }
    }

    /** v0.4.0b: THE REQUEST BOARD in City Hall — a cork board
     *  on the free wall right of the minutes board (the mayor's
     *  portrait keeps the left). Its notes are the day's actual
     *  postings: Errands.boardOpen decides how many pins the
     *  cork wears, so a bare board in the room means a bare
     *  board in the dialog. Tapping it is a fixture tap, like
     *  the bed and the album at your house. */
    // ---- the GALLERY WALL (v0.15.0f: the Community
    // Hall's long wall inside City Hall, left of the
    // request board): the seasonal exhibition. A hung
    // frame is painted by PhotoView's OWN renderer off
    // the entry's frozen record — the real photograph,
    // never a stand-in — with a brass plate under the
    // standing champion. Before the lock the wall shows
    // the theme card and any of your frames already in;
    // empty slots stay honest outlines (Gallery.wallView
    // owns what the wall carries; this only paints). ----
    private fun drawGalleryWall(c: Canvas, st: GameState,
                                worldW: Float, h: Float) {
        val view = Gallery.wallView(st)
        if (view.mode == "none") return
        val l = worldW * 0.055f; val r = worldW * 0.615f
        val t = h * 0.125f
        // the wall panel
        c.drawRoundRect(RectF(l - 8f, t - h * 0.045f, r + 8f,
            h * 0.400f), 10f, 10f, Paper.fillPaint("#5d4630"))
        c.drawRoundRect(RectF(l - 8f, t - h * 0.045f, r + 8f,
            h * 0.400f), 10f, 10f,
            Paper.strokePaint("#3a2a1a", 3f, 200))
        val hp = Paper.textPaint("#f2c230", h * 0.0165f,
            bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🖼 THE GALLERY SHOW", (l + r) / 2f,
            t - h * 0.020f, hp)
        val theme = view.theme
        if (theme != null) {
            val tp = Paper.textPaint("#efe3c8", h * 0.0135f,
                casual = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText("“${theme.title}” — ${theme.season.name.lowercase()}",
                (l + r) / 2f, t + h * 0.002f, tp)
        }
        fun frame(rect: RectF, e: GalleryEntry?,
                  championPlate: Boolean) {
            c.drawRoundRect(RectF(rect.left - 4f, rect.top - 4f,
                rect.right + 4f, rect.bottom + 4f), 5f, 5f,
                Paper.fillPaint("#3a2418"))
            if (e != null) {
                photoRenderer.drawInto(c, e.record, rect)
                if (championPlate) {
                    c.drawRoundRect(RectF(
                        rect.centerX() - rect.width() * 0.32f,
                        rect.bottom + h * 0.008f,
                        rect.centerX() + rect.width() * 0.32f,
                        rect.bottom + h * 0.023f), 3f, 3f,
                        Paper.fillPaint("#c9a227"))
                    c.drawRect(
                        rect.centerX() - rect.width() * 0.32f,
                        rect.bottom + h * 0.008f,
                        rect.centerX() + rect.width() * 0.32f,
                        rect.bottom + h * 0.012f,
                        Paper.fillPaint("#e8cc5e"))
                    val pp = Paper.textPaint("#efe3c8",
                        h * 0.0115f, casual = true)
                    pp.textAlign = Paint.Align.CENTER
                    c.drawText("👑 ${e.entrant}",
                        rect.centerX(), rect.bottom + h * 0.040f, pp)
                }
            } else {
                c.drawRoundRect(rect, 4f, 4f,
                    Paper.strokePaint("#a8895e", 2f, 130))
                val ep = Paper.textPaint("#a8895e", h * 0.012f,
                    casual = true)
                ep.textAlign = Paint.Align.CENTER
                c.drawText("—", rect.centerX(),
                    rect.top + rect.height() * 0.55f, ep)
            }
        }
        when (view.mode) {
            "champion" -> {
                val cw = worldW * 0.200f; val chh = h * 0.150f
                val cx = (l + r) / 2f
                frame(RectF(cx - cw / 2f, t + h * 0.018f,
                    cx + cw / 2f, t + h * 0.018f + chh),
                    view.champion, championPlate = true)
            }
            "show", "submit" -> {
                val cols = 5
                val slotW = (r - l) / cols
                val fw = slotW * 0.78f; val fh = h * 0.088f
                val rows = if (view.mode == "show") 2 else 1
                val total = if (view.mode == "show")
                    Gallery.WALL_CAP else Gallery.PLAYER_ENTRY_CAP
                for (i in 0 until total) {
                    val row = i / cols; val col = i % cols
                    if (row >= rows) break
                    val cx = l + slotW * (col + 0.5f)
                    val top = t + h * 0.020f + row * (fh + h * 0.030f)
                    frame(RectF(cx - fw / 2f, top,
                        cx + fw / 2f, top + fh),
                        view.frames.getOrNull(i),
                        championPlate = false)
                }
            }
            else -> {
                // "card": the theme card alone, center wall
                val cw = worldW * 0.240f; val chh = h * 0.110f
                val cx = (l + r) / 2f
                val rect = RectF(cx - cw / 2f, t + h * 0.030f,
                    cx + cw / 2f, t + h * 0.030f + chh)
                c.drawRoundRect(rect, 6f, 6f,
                    Paper.fillPaint("#efe3c8"))
                val cp = Paper.textPaint("#5a4632", h * 0.0135f,
                    casual = true)
                cp.textAlign = Paint.Align.CENTER
                c.drawText("next show", cx,
                    rect.top + chh * 0.38f, cp)
                if (theme != null) {
                    val tp2 = Paper.textPaint("#3a2a1a",
                        h * 0.0165f, bold = true, casual = true)
                    tp2.textAlign = Paint.Align.CENTER
                    c.drawText("“${theme.title}”", cx,
                        rect.top + chh * 0.68f, tp2)
                }
            }
        }
        hits.add(Hit("fixture", "gallerywall",
            RectF(l - 14, t - h * 0.050f, r + 14, h * 0.405f)))
    }

    private fun drawErrandBoard(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.635f; val r = worldW * 0.875f
        val t = h * 0.125f; val b = h * 0.385f
        // errand-board: frame + cork
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#6b4a2f"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#c8a878"))
        // header strip
        c.drawRect(RectF(l, t, r, t + h * 0.048f), Paper.fillPaint(Paper.RED))
        val hp = Paper.textPaint("#ffffff", h * 0.021f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("📋 REQUEST BOARD", (l + r) / 2f, t + h * 0.036f, hp)
        val open = Errands.boardOpen(st)
        if (open.isEmpty()) {
            val ep = Paper.textPaint("#5a4632", h * 0.017f, bold = true, casual = true)
            ep.textAlign = Paint.Align.CENTER
            c.drawText("ALL TAKEN —", (l + r) / 2f, (t + b) / 2f, ep)
            c.drawText("BACK TOMORROW", (l + r) / 2f, (t + b) / 2f + h * 0.026f, ep)
        } else {
            // pinned notes: one per open posting, cork-grid,
            // each with its red pin and a scribble of lines
            val cols = 3
            val nw = (r - l) / cols
            val nh = (b - t - h * 0.055f) / 2f
            for ((i, _) in open.withIndex()) {
                val cx = l + nw * (i % cols) + nw / 2f
                val cy = t + h * 0.062f + nh * (i / cols) + nh / 2f
                c.save()
                c.rotate(if (i % 2 == 0) -2.5f else 2f, cx, cy)
                c.drawRect(RectF(cx - nw * 0.34f, cy - nh * 0.36f,
                    cx + nw * 0.34f, cy + nh * 0.36f), Paper.fillPaint("#fdfdf6"))
                c.drawCircle(cx, cy - nh * 0.30f, 4.5f, Paper.fillPaint(Paper.RED))
                val lp = Paper.strokePaint("#8a8074", 2f)
                for (k in 0..2) {
                    val ly = cy - nh * 0.10f + k * nh * 0.16f
                    c.drawLine(cx - nw * 0.24f, ly, cx + nw * (0.24f - k * 0.05f), ly, lp)
                }
                c.restore()
            }
            val bp = Paper.textPaint("#5a4632", h * 0.0155f, bold = true, casual = true)
            bp.textAlign = Paint.Align.CENTER
            c.drawText("${open.size} favor${if (open.size == 1) "" else "s"} " +
                "pinned — tap the board", (l + r) / 2f, b - h * 0.012f, bp)
        }
        hits.add(Hit("fixture", "errandboard",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    /** v0.13.0d: 🎺 THE REVIEWING STAND on Main Street —
     *  two poles, one banner, a low platform, drawn only
     *  through the parade's cycle (Parade.standOpen): the
     *  poster week it carries the date, on the day it
     *  carries the proclamation, and in a yielded year it
     *  stands furled. Smooth fills only — bunting is two
     *  plain draped panels, never a pattern (the town's
     *  standing rule, and the user's). Tapping it is a
     *  fixture tap, like the board at City Hall. */
    private fun drawParadeStand(c: Canvas, st: GameState, worldW: Float, h: Float) {
        if (!Parade.standOpen(st)) return
        val l = worldW * 0.06f; val r = worldW * 0.30f
        val t = h * 0.115f; val b = h * 0.335f
        val yielded = Parade.currentRecord(st)?.let {
            it.yieldWhy.isNotEmpty() } == true
        // the poles
        c.drawRect(RectF(l, t, l + 7f, b + h * 0.045f),
            Paper.fillPaint("#4a3423"))
        c.drawRect(RectF(r - 7f, t, r, b + h * 0.045f),
            Paper.fillPaint("#4a3423"))
        // the platform
        c.drawRoundRect(RectF(l - 10f, b, r + 10f,
            b + h * 0.038f), 5f, 5f, Paper.fillPaint("#6b4a2f"))
        // the banner: parchment, town red when the parade
        // stands, plain grey when the year yielded
        c.drawRoundRect(RectF(l, t, r, b), 6f, 6f,
            Paper.fillPaint(if (yielded) "#8a8074"
                else Paper.PARCHMENT))
        c.drawRoundRect(RectF(l, t, r, b), 6f, 6f,
            Paper.strokePaint(if (yielded) "#5a5348"
                else "#a33b5c", 3f))
        val hp = Paper.textPaint(if (yielded) "#f2ead8"
            else "#7a1f3d", h * 0.020f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🎺 FOUNDERS' DAY PARADE", (l + r) / 2f,
            t + h * 0.043f, hp)
        val lp = Paper.textPaint(if (yielded) "#f2ead8"
            else "#5a4632", h * 0.0155f, bold = true, casual = true)
        lp.textAlign = Paint.Align.CENTER
        val year = Parade.currentYear(st)
        val day = ParadeCalendar.paradeDay(year)
        val line2 = when {
            yielded -> "not held this year"
            st.absDay == day -> "TODAY — step-off 10 AM"
            st.absDay < day -> Dance.dateLabel(day) +
                " · step-off 10 AM"
            else -> "the mayor rode last"
        }
        c.drawText(line2, (l + r) / 2f, t + h * 0.082f, lp)
        // bunting: two plain draped panels off the poles —
        // solid color, one smooth curve each, no repeats
        if (!yielded) {
            val bp = Paper.fillPaint("#a33b5c", 200)
            val pathL = android.graphics.Path().apply {
                moveTo(l, t - 6f)
                quadTo((l + r) / 2f - (r - l) * 0.18f,
                    t + h * 0.030f, l + (r - l) * 0.30f, t - 6f)
                lineTo(l + (r - l) * 0.30f, t + 4f)
                lineTo(l, t + 4f)
                close()
            }
            c.drawPath(pathL, bp)
            val pathR = android.graphics.Path().apply {
                moveTo(r - (r - l) * 0.30f, t - 6f)
                quadTo((l + r) / 2f + (r - l) * 0.18f,
                    t + h * 0.030f, r, t - 6f)
                lineTo(r, t + 4f)
                lineTo(r - (r - l) * 0.30f, t + 4f)
                close()
            }
            c.drawPath(pathR, Paper.fillPaint("#2f5d8a", 200))
        }
        hits.add(Hit("fixture", "parade",
            RectF(l - 14, t - 12, r + 14, b + h * 0.05f)))
    }

    /** v0.6.0f: THE BALLOT BOX inside City Hall — a civic-red
     *  wall box on a stand under the request board. Its face
     *  reads the week through Votes: this week's question in
     *  short, and a paper corner poking from the slot once
     *  YOUR ballot is in. One smooth slot line, no patterns —
     *  democracy is plain here. Tapping it is a fixture tap,
     *  like the board above it. */
    private fun drawBallotBox(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.672f; val r = worldW * 0.838f
        val t = h * 0.408f; val b = h * 0.565f
        // the stand: two legs and a foot bar
        c.drawRect(RectF(l + (r - l) * 0.18f, b, l + (r - l) * 0.30f,
            h * 0.635f), Paper.fillPaint("#4a3423"))
        c.drawRect(RectF(r - (r - l) * 0.30f, b, r - (r - l) * 0.18f,
            h * 0.635f), Paper.fillPaint("#4a3423"))
        c.drawRect(RectF(l + (r - l) * 0.08f, h * 0.628f,
            r - (r - l) * 0.08f, h * 0.645f), Paper.fillPaint("#4a3423"))
        // the box: civic red, brass trim
        c.drawRoundRect(RectF(l - 5f, t - 5f, r + 5f, b + 5f), 6f, 6f,
            Paper.fillPaint("#c9a227"))
        c.drawRoundRect(RectF(l, t, r, b), 5f, 5f,
            Paper.fillPaint("#7a1f1f"))
        // the slot — one smooth line, and your paper in it
        val slotY = t + (b - t) * 0.30f
        c.drawRoundRect(RectF(l + (r - l) * 0.16f, slotY - 3f,
            r - (r - l) * 0.16f, slotY + 3f), 3f, 3f,
            Paper.fillPaint("#2a0f0f"))
        if (Votes.playerVote(st) != null) {
            c.save()
            c.rotate(-4f, (l + r) / 2f, slotY)
            c.drawRect(RectF((l + r) / 2f - (r - l) * 0.13f, slotY - h * 0.030f,
                (l + r) / 2f + (r - l) * 0.13f, slotY + 2f),
                Paper.fillPaint("#fdfdf6"))
            c.restore()
        }
        val hp = Paper.textPaint("#f4ecd8", h * 0.019f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🗳 BALLOT BOX", (l + r) / 2f, t + (b - t) * 0.60f, hp)
        val q = Votes.currentQuestion(st)
        val qp = Paper.textPaint("#e8d9b0", h * 0.0135f, bold = false, casual = true)
        qp.textAlign = Paint.Align.CENTER
        val short = if (q.title.length > 24) q.title.take(23) + "…" else q.title
        c.drawText(short, (l + r) / 2f, t + (b - t) * 0.82f, qp)
        hits.add(Hit("fixture", "ballotbox",
            RectF(l - 14, t - 12, r + 14, h * 0.65f)))
    }

    /** v0.7.0c: THE SCREEN inside the Bijou — a wide silver
     *  rectangle high on the back wall between two smooth
     *  curtain panels (plain drapes, no patterns). Its face
     *  reads the night through MovieNights: tonight's feature
     *  in lights on a live night, the honest dark line when
     *  the town's business books the room, and the next
     *  showing's title while the marquee is re-lettered.
     *  Tapping it is a fixture tap — the seat-taking lives
     *  behind MovieNights' sessionStatus. */
    private fun drawMovieScreen(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.155f; val r = worldW * 0.845f
        val t = h * 0.115f; val b = h * 0.430f
        val dark = MovieNights.isScreeningDay(st.absDay) &&
            MovieNights.isDarkTonight(st)
        val live = MovieNights.isScreeningDay(st.absDay) && !dark
        // the curtains: two smooth velvet panels and a valance
        c.drawRect(RectF(l - worldW * 0.055f, t - h * 0.030f,
            l + worldW * 0.030f, b + h * 0.045f), Paper.fillPaint("#6e1420"))
        c.drawRect(RectF(r - worldW * 0.030f, t - h * 0.030f,
            r + worldW * 0.055f, b + h * 0.045f), Paper.fillPaint("#6e1420"))
        c.drawRect(RectF(l - worldW * 0.055f, t - h * 0.030f,
            r + worldW * 0.055f, t + h * 0.012f), Paper.fillPaint("#57101a"))
        // the screen: lit silver on a live night, grey glass otherwise
        c.drawRoundRect(RectF(l - 6f, t - 6f, r + 6f, b + 6f), 8f, 8f,
            Paper.fillPaint("#2a2a33"))
        c.drawRect(RectF(l, t, r, b),
            Paper.fillPaint(if (live) "#e9e6da" else "#565661"))
        // the projector's cone, faint from the booth behind
        if (live) {
            val cone = android.graphics.Path().apply {
                moveTo(worldW * 0.485f, h * 0.985f)
                lineTo(worldW * 0.515f, h * 0.985f)
                lineTo(r - worldW * 0.05f, t + (b - t) * 0.18f)
                lineTo(l + worldW * 0.05f, t + (b - t) * 0.18f)
                close()
            }
            c.drawPath(cone, Paper.fillPaint("#fffbe8", 26))
        }
        val cx = (l + r) / 2f
        val hp = Paper.textPaint(if (live) "#33261a" else "#e8e4d8",
            h * 0.024f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        val lp = Paper.textPaint(if (live) "#5a4632" else "#c9c4b4",
            h * 0.0155f, bold = false, casual = true)
        lp.textAlign = Paint.Align.CENTER
        fun short(s: String, n: Int) =
            if (s.length > n) s.take(n - 1) + "…" else s
        when {
            dark -> {
                c.drawText("🎬 DARK TONIGHT", cx, t + (b - t) * 0.44f, hp)
                c.drawText(short(MovieNights.darkLine(st)
                    ?.removePrefix("🎬 Dark tonight — ") ?: "Town business.", 42),
                    cx, t + (b - t) * 0.68f, lp)
            }
            live -> {
                val f = MovieNights.featureFor(st)
                c.drawText("🎬 TONIGHT", cx, t + (b - t) * 0.32f, hp)
                c.drawText("“${short(f.title, 30)}”", cx,
                    t + (b - t) * 0.58f, hp)
                c.drawText("${f.genreLabel} — starring " +
                    short(f.stars.joinToString(", "), 40), cx,
                    t + (b - t) * 0.80f, lp)
            }
            else -> {
                val next = MovieNights.nextScreeningDay(st.absDay)
                val nf = MovieNights.featureForDay(st, next)
                c.drawText("🎬 THE BIJOU", cx, t + (b - t) * 0.38f, hp)
                c.drawText("${Jobs.WEEKDAY_NAMES[Jobs.weekdayOf(next)]}: " +
                    "“${short(nf.title, 26)}”", cx,
                    t + (b - t) * 0.64f, lp)
            }
        }
        hits.add(Hit("fixture", "moviescreen",
            RectF(l - worldW * 0.06f, t - h * 0.035f,
                r + worldW * 0.06f, b + h * 0.05f)))
    }

    /** v0.5.0a: THE TRANSIT KIOSK inside the Mall — a glowing
     *  screen on the right wall (the errand board's slot, one
     *  town over). Its face reads the save through Walk: your
     *  pass on file, the Town Fixture perk waiting, or the
     *  price. Tapping it is a fixture tap, like the board. */
    /** v0.7.0d: THE CLASSROOM BOARD inside the Elementary —
     *  a chalk board on the right wall (the hall of honor
     *  keeps the left, the cubbies the middle). Its face is
     *  read off the save through School: today's topic and
     *  teacher, the bee's setup on Friday afternoons, weekend
     *  script on weekends, quiet days plainly quiet. Tapping
     *  it is a fixture tap, like the movie screen. */
    private fun drawClassroomBoard(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.615f; val r = worldW * 0.975f
        val t = h * 0.115f; val b = h * 0.400f
        // frame + board (chalk green, one honest chalk tray)
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#6b4a2c"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#274435"))
        c.drawRect(RectF(l, b - h * 0.018f, r, b),
            Paper.fillPaint("#54381d"))
        c.drawRect(RectF(l + worldW * 0.03f, b - h * 0.030f,
            l + worldW * 0.075f, b - h * 0.020f), Paper.fillPaint("#e8e4d8"))
        val cx = (l + r) / 2f
        val hp = Paper.textPaint("#eef2e6", h * 0.023f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        val lp = Paper.textPaint("#c8d4c0", h * 0.0155f, bold = false, casual = true)
        lp.textAlign = Paint.Align.CENTER
        fun short(s: String, n: Int) =
            if (s.length > n) s.take(n - 1) + "…" else s
        val (head, sub) = School.boardLines(st)
        c.drawText(short(head, 34), cx, t + (b - t) * 0.40f, hp)
        c.drawText(short(sub, 46), cx, t + (b - t) * 0.66f, lp)
        hits.add(Hit("fixture", "classroom",
            RectF(l - worldW * 0.04f, t - h * 0.03f,
                r + worldW * 0.04f, b + h * 0.045f)))
    }

    private fun drawTransitKiosk(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.635f; val r = worldW * 0.875f
        val t = h * 0.125f; val b = h * 0.385f
        // housing + screen
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#2f3a42"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#1d2b33"))
        // header strip
        c.drawRect(RectF(l, t, r, t + h * 0.048f), Paper.fillPaint("#2a6fd4"))
        val hp = Paper.textPaint("#ffffff", h * 0.021f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🚌 TRANSIT PASS", (l + r) / 2f, t + h * 0.036f, hp)
        // the screen's one message, read off the save
        val (line1, line2) = when {
            Walk.hasPass(st) -> "PASS ON FILE ✓" to "WALKING STILL BETTER."
            Walk.townFixture(st) -> "TOWN FIXTURE?" to "YOUR PASS IS FREE — TAP"
            else -> "${Walk.BUS_PASS_PRICE} 🪙" to "UNLIMITED RIDES — TAP TO BUY"
        }
        val bp = Paper.textPaint("#9fe8c0", h * 0.020f, bold = true, casual = true)
        bp.textAlign = Paint.Align.CENTER
        c.drawText(line1, (l + r) / 2f, (t + b) / 2f - h * 0.008f, bp)
        val sp = Paper.textPaint("#9fe8c0", h * 0.0145f, bold = true, casual = true)
        sp.textAlign = Paint.Align.CENTER
        c.drawText(line2, (l + r) / 2f, (t + b) / 2f + h * 0.026f, sp)
        // the route strip: one bus, one line, total confidence
        val rp = Paper.strokePaint("#e8c53a", 3f)
        c.drawLine(l + (r - l) * 0.12f, b - h * 0.035f,
            r - (r - l) * 0.12f, b - h * 0.035f, rp)
        c.drawCircle(l + (r - l) * 0.12f, b - h * 0.035f, 4f,
            Paper.fillPaint("#e8c53a"))
        c.drawCircle(r - (r - l) * 0.12f, b - h * 0.035f, 4f,
            Paper.fillPaint("#e8c53a"))
        hits.add(Hit("fixture", "buskiosk",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    /** v0.5.0b: THE CLOTHING RACK inside the Mall (left wall,
     *  clear of the transit kiosk on the right) — two of the
     *  wardrobe's coin pieces hang here. Its tags read off the
     *  save: a price while a piece is unsold, YOURS once both
     *  hang in your own closet. */
    private fun drawWardrobeRack(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.055f; val r = worldW * 0.315f
        val t = h * 0.125f; val b = h * 0.385f
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#5a4632"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#3a2f28"))
        c.drawRect(RectF(l, t, r, t + h * 0.048f), Paper.fillPaint("#8a2a5a"))
        val hp = Paper.textPaint("#ffffff", h * 0.021f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("👕 THE GOOD SHELF", (l + r) / 2f, t + h * 0.036f, hp)
        // the rail + the two garments, in their own colors
        val railY = t + h * 0.085f
        c.drawLine(l + (r - l) * 0.08f, railY, r - (r - l) * 0.08f, railY,
            Paper.strokePaint("#c8c8c8", 3f))
        val pieces = Wardrobe.piecesAt("Mall")
        for ((i, p) in pieces.withIndex()) {
            val cxp = l + (r - l) * (0.28f + 0.44f * i)
            val col = creatorOpt(p.category, p.id).color ?: "#888888"
            c.drawRoundRect(RectF(cxp - (r - l) * 0.13f, railY + 4f,
                cxp + (r - l) * 0.13f, railY + h * 0.13f), 5f, 5f,
                Paper.fillPaint(col))
            val tag = if (Wardrobe.owned(st, p)) "YOURS ✓" else "${p.price} 🪙"
            val tp = Paper.textPaint("#f2c230", h * 0.015f, bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText(tag, cxp, railY + h * 0.165f, tp)
        }
        hits.add(Hit("fixture", "clothesrack",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    /** v0.5.0b: JIMBO'S COUNTER — the gun-shop wall where the
     *  other two coin pieces hang (a hat and a pair of pants,
     *  hunting-grade). Same read-the-save tags as the Mall rack. */
    private fun drawGunRack(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.635f; val r = worldW * 0.875f
        val t = h * 0.125f; val b = h * 0.385f
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#4a3423"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#5f4630"))
        c.drawRect(RectF(l, t, r, t + h * 0.048f), Paper.fillPaint("#b85c2a"))
        val hp = Paper.textPaint("#ffffff", h * 0.021f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🦌 JIMBO'S GOOD SHELF", (l + r) / 2f, t + h * 0.036f, hp)
        val pieces = Wardrobe.piecesAt("JimbosGuns")
        for ((i, p) in pieces.withIndex()) {
            val cxp = l + (r - l) * (0.28f + 0.44f * i)
            val col = creatorOpt(p.category, p.id).color ?: "#888888"
            c.drawRoundRect(RectF(cxp - (r - l) * 0.13f, t + h * 0.075f,
                cxp + (r - l) * 0.13f, t + h * 0.205f), 5f, 5f,
                Paper.fillPaint(col))
            val tag = if (Wardrobe.owned(st, p)) "YOURS ✓" else "${p.price} 🪙"
            val tp = Paper.textPaint("#f2c230", h * 0.015f, bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText(tag, cxp, t + h * 0.24f, tp)
        }
        val fp = Paper.textPaint("#e8dcc8", h * 0.0135f, bold = true, casual = true)
        fp.textAlign = Paint.Align.CENTER
        c.drawText("IT'S COMIN' RIGHT FOR US — THE FASHION",
            (l + r) / 2f, b - h * 0.02f, fp)
        hits.add(Hit("fixture", "gunrack",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    /** v0.5.0c: THE CAPSULE MACHINE inside the Mall — a glass
     *  dome full of big two-tone capsules (a few large solid
     *  shapes, never a cluster of dots) over a crank box. Its
     *  tag reads the save: your album count and the price.
     *  Tapping it is a fixture tap, like the kiosk beside it. */
    private fun drawCapsuleMachine(c: Canvas, st: GameState, worldW: Float, h: Float) {
        val l = worldW * 0.365f; val r = worldW * 0.60f
        val t = h * 0.125f; val b = h * 0.385f
        val cxm = (l + r) / 2f
        // housing
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#4a2a3a"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#33202c"))
        // header strip
        c.drawRect(RectF(l, t, r, t + h * 0.048f), Paper.fillPaint("#7a3fd4"))
        val hp = Paper.textPaint("#ffffff", h * 0.021f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🃏 POCKET PALS", cxm, t + h * 0.036f, hp)
        // the dome: a glass semicircle over the prize pit
        val domeR = (r - l) * 0.34f
        val domeCy = t + h * 0.155f
        c.drawArc(RectF(cxm - domeR, domeCy - domeR, cxm + domeR, domeCy + domeR),
            180f, 180f, true, Paper.fillPaint("#cfe8f7", 190))
        c.drawArc(RectF(cxm - domeR, domeCy - domeR, cxm + domeR, domeCy + domeR),
            180f, 180f, false, Paper.strokePaint("#e8e8e8", 3f))
        // the capsules: five big two-tone ovals, loosely stacked
        val caps = listOf(
            Triple(-0.16f, 0.028f, "#e33d5a"), Triple(0.10f, 0.030f, "#2a6fd4"),
            Triple(-0.03f, -0.006f, "#f2c230"), Triple(0.19f, -0.002f, "#3f9e4d"),
            Triple(-0.20f, -0.004f, "#7a3fd4"))
        for ((dx, dy, col) in caps) {
            val ccx = cxm + (r - l) * dx
            val ccy = domeCy + h * dy + domeR * 0.52f
            val cw = (r - l) * 0.115f; val chh = h * 0.021f
            c.drawRoundRect(RectF(ccx - cw, ccy - chh, ccx, ccy + chh), chh, chh,
                Paper.fillPaint(col))
            c.drawRoundRect(RectF(ccx, ccy - chh, ccx + cw, ccy + chh), chh, chh,
                Paper.fillPaint("#f4ecd8"))
        }
        // the crank box + coin slot
        val boxT = domeCy + domeR * 0.62f
        c.drawRoundRect(RectF(l + (r - l) * 0.18f, boxT, r - (r - l) * 0.18f,
            b - h * 0.045f), 5f, 5f, Paper.fillPaint("#5a3a4c"))
        c.drawCircle(cxm + (r - l) * 0.16f, boxT + h * 0.028f, h * 0.016f,
            Paper.fillPaint("#d4a017"))
        c.drawCircle(cxm + (r - l) * 0.16f, boxT + h * 0.028f, h * 0.016f,
            Paper.strokePaint("#1a1a1a", 2f))
        // the tag: price + your album so far
        val owned = PocketPals.ownedCount(st)
        val tp = Paper.textPaint("#ffd94a", h * 0.0165f, bold = true, casual = true)
        tp.textAlign = Paint.Align.CENTER
        c.drawText("${PocketPals.MACHINE_COST} 🪙 A PULL · ALBUM $owned/${PocketPals.CARDS.size}",
            cxm, b - h * 0.014f, tp)
        hits.add(Hit("fixture", "capsulemachine",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    /** v0.12.0b: THE GARDEN CENTRE inside the Mall (left
     *  wall, second row under the clothing rack — the
     *  ballot box's band) — the seed rack Garden has
     *  named since v0.11.0b, finally a counter you can
     *  stand at: five seed packets on two shelf lines,
     *  a price on every tag, and your pack count in gold
     *  on the ones you're carrying. Shop.buySeed owns
     *  the till; Garden owns the packets. */
    private fun drawSeedRack(c: Canvas, st: GameState, worldW: Float, h: Float) {
        fun glyph(txt: String, x: Float, y: Float, size: Float) {
            val p = Paper.textPaint("#ffffff", size,
                bold = true, casual = true)
            p.textAlign = Paint.Align.CENTER
            c.drawText(txt, x, y, p)
        }
        val l = worldW * 0.055f; val r = worldW * 0.315f
        val t = h * 0.408f; val b = h * 0.565f
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#3f5a2a"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#2c4423"))
        c.drawRect(RectF(l, t, r, t + h * 0.040f), Paper.fillPaint("#4d7a35"))
        val hp = Paper.textPaint("#ffffff", h * 0.018f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🌱 GARDEN CENTRE", (l + r) / 2f, t + h * 0.030f, hp)
        // two shelf lines of seed packets, crop glyph on
        // each envelope, the price beneath — and the
        // count in your pack, once you're carrying one
        val shelf1 = t + h * 0.072f
        val shelf2 = t + h * 0.118f
        val sp = Paper.strokePaint("#8a6a42", 3f)
        c.drawLine(l + 8f, shelf1 + h * 0.017f,
            r - 8f, shelf1 + h * 0.017f, sp)
        c.drawLine(l + 8f, shelf2 + h * 0.017f,
            r - 8f, shelf2 + h * 0.017f, sp)
        for ((i, crop) in Garden.CROPS.withIndex()) {
            val row = if (i < 3) 0 else 1
            val n = if (row == 0) 3 else 2
            val cxp = l + (r - l) * ((i % 3 + 0.5f) / n)
            val py = if (row == 0) shelf1 else shelf2
            glyph(crop.icon, cxp, py, h * 0.020f)
            val held = Garden.packetsHeld(st, crop.id)
            val tag = if (held > 0)
                "${crop.seedPrice} 🪙 · PACK ×$held"
            else "${crop.seedPrice} 🪙"
            val tp = Paper.textPaint(
                if (held > 0) "#ffd94a" else "#cfe0bd",
                h * 0.0115f, bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText(tag, cxp, py + h * 0.031f, tp)
        }
        hits.add(Hit("fixture", "seedrack",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    /** v0.12.0b: THE SNACK SHOP inside the Mall (right
     *  wall, second row under the transit kiosk) — the
     *  general-goods counter Stand.STOCK has named as
     *  the town's supplier all along ("the Mall snack
     *  shop"), selling its four pantry staples at the
     *  shops' own prices into your real pantry. The
     *  tags read off the save: the price always, your
     *  pantry count in gold when you're stocked.
     *  Shop.buyStaple owns the till. */
    private fun drawSnackCounter(c: Canvas, st: GameState, worldW: Float, h: Float) {
        fun glyph(txt: String, x: Float, y: Float, size: Float) {
            val p = Paper.textPaint("#ffffff", size,
                bold = true, casual = true)
            p.textAlign = Paint.Align.CENTER
            c.drawText(txt, x, y, p)
        }
        val l = worldW * 0.635f; val r = worldW * 0.875f
        val t = h * 0.408f; val b = h * 0.565f
        c.drawRoundRect(RectF(l - 7f, t - 7f, r + 7f, b + 7f), 6f, 6f,
            Paper.fillPaint("#5a4632"))
        c.drawRect(RectF(l, t, r, b), Paper.fillPaint("#3a2f28"))
        c.drawRect(RectF(l, t, r, t + h * 0.040f), Paper.fillPaint("#b85c2a"))
        val hp = Paper.textPaint("#ffffff", h * 0.018f, bold = true, casual = true)
        hp.textAlign = Paint.Align.CENTER
        c.drawText("🧺 SNACK SHOP", (l + r) / 2f, t + h * 0.030f, hp)
        // the goods on the counter: four staples, price
        // on every tag, pantry count alongside
        val gy = t + h * 0.082f
        c.drawLine(l + 8f, gy + h * 0.018f,
            r - 8f, gy + h * 0.018f, Paper.strokePaint("#8a6a42", 3f))
        for ((i, id) in Shop.STAPLE_IDS.withIndex()) {
            val item = itemById(id)
            val cxp = l + (r - l) *
                ((i + 0.5f) / Shop.STAPLE_IDS.size)
            glyph(item?.icon ?: "🧺", cxp, gy, h * 0.022f)
            val held = st.inventory[id] ?: 0
            val tag = "${Shop.staplePrice(id)} 🪙" +
                if (held > 0) " · ×$held" else ""
            val tp = Paper.textPaint(
                if (held > 0) "#ffd94a" else "#e8dcc2",
                h * 0.0115f, bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText(tag, cxp, gy + h * 0.034f, tp)
        }
        val bp = Paper.textPaint("#c8bfae", h * 0.0115f,
            bold = true, casual = true)
        bp.textAlign = Paint.Align.CENTER
        c.drawText("GENERAL GOODS — THE SHOPS' OWN PRICES",
            (l + r) / 2f, b - h * 0.012f, bp)
        hits.add(Hit("fixture", "snackcounter",
            RectF(l - 16, t - 14, r + 16, b + 14)))
    }

    private fun drawLivingInterior(c: Canvas, st: GameState, locId: String,
                                   worldW: Float, h: Float) {
        val meal = (st.timeMin / 60) in listOf(7, 8, 12, 17, 18)
        if (!meal || locId !in TownInfo.occupiedHomes) return
        val millis = System.currentTimeMillis()
        val p = Paper.textPaint("#ffffff", 15f, bold = true, casual = true)
        p.textAlign = Paint.Align.CENTER
        for (i in 0..2) {
            val ph = ((millis / 1300f) + i * 0.7f) % 2f
            val x = worldW * 0.70f + kotlin.math.sin(ph * 3f + i) * 8f
            val y = h * 0.30f - ph * h * 0.07f
            p.alpha = (190 - ph * 80).toInt().coerceIn(30, 190)
            c.drawText(if (i % 2 == 0) "♨" else "🍳", x, y, p)
        }
        p.alpha = 255
    }

    /** #81 EDGE NOISE ICONS + #82 laughter drift + #90 distant
     *  cheering: off-screen life, pointed at from the screen edge. */
    private fun drawEdgeIcons(c: Canvas, w: Float, h: Float,
                              st: GameState, loc: Location) {
        fun edge(txt: String, toLeft: Boolean, yFrac: Float, label: String) {
            val p = Paper.textPaint("#f4ecd8", 19f, bold = true, casual = true)
            p.textAlign = Paint.Align.CENTER
            val x = if (toLeft) 26f else w - 26f
            c.drawText(txt, x, h * yFrac, p)
            if (label.isNotEmpty()) {
                val lp = Paper.textPaint("#f4ecd8", 9.5f, bold = false, casual = true)
                lp.textAlign = Paint.Align.CENTER
                c.drawText(label, x, h * yFrac + 14f, lp)
            }
        }
        // the active event, wherever it is
        val def = Quests.activeDef(st)
        val clusterLoc = def?.let { Shenanigans.stageOf(it, st.timeMin).clusterLoc }
        if (def != null && clusterLoc != null && clusterLoc != loc.id) {
            val target = LOCATIONS[clusterLoc]
            if (target != null) {
                val climax = Quests.currentStage(st) == 2
                edge(if (climax) "📣" else "🎪", target.gx < loc.gx, 0.52f,
                    if (climax) "cheering!" else "event")
            }
        }
        // today's roof-work house hammers away off-screen (#28/#81)
        if (st.absDay % 4 == 1 && st.timeMin / 60 in 8..16) {
            val roofHouse = HOUSE_LOCS.firstOrNull {
                TownLife.scatter(it, st.absDay, 28) < 0.5f
            }
            if (roofHouse != null && roofHouse != loc.id) {
                val target = LOCATIONS[roofHouse]
                if (target != null) edge("🔨", target.gx < loc.gx, 0.62f, "roof work")
            }
        }
        // #82: a neighbor scene is mid-funny — laughter drifts in
        for (nId in (EXITS[loc.id] ?: emptyList())) {
            val crowd = st.agents.values.count {
                !it.isPlayer && it.location == nId && it.waypoints.isEmpty() && !it.asleep
            }
            if (crowd >= 2 &&
                TownLife.scatter(nId, st.absDay, st.timeMin / 60) < 0.30f) {
                val target = LOCATIONS[nId] ?: continue
                val left = target.gx < loc.gx
                val p = Paper.textPaint("#ffffff", 13f, bold = true, casual = true)
                p.textAlign = Paint.Align.CENTER
                val x = if (left) 30f else w - 30f
                val bob = kotlin.math.sin(System.currentTimeMillis() * 0.003f) * 5f
                c.drawText("ha!", x, h * 0.40f + bob, p)
                c.drawText("ha!", x + (if (left) 14f else -14f), h * 0.40f + 20f - bob, p)
                break
            }
        }
    }

    private fun drawTrash(c: Canvas, st: GameState, locId: String, inside: Boolean,
                          worldW: Float, h: Float, seed: Long) {
        val cs = containersAt(locId, inside)
        cs.forEachIndexed { i, cont ->
            val cx = worldW * (0.14f + 0.72f * ((i + 0.5f) / cs.size.coerceAtLeast(1)))
            val baseY = h * 0.965f
            // v0.15.0d: trash at world scale — the can
            // composes at TRASH_U kid heights (its painter
            // stands 0.74 × size tall), well under a kid
            val size = WorldScale.units(WorldScale.TRASH_U, h) / 0.74f
            val searched = !engine!!.trashAvailable(st, cont.id)
            InteriorArt.drawTrashContainer(c, cx, baseY, size, cont.kind, searched)
            hits.add(Hit("trash", cont.id,
                RectF(cx - size, baseY - size * 1.2f, cx + size, baseY + 8)))
        }
        val litterHere = st.litter.filter { it.locId == locId && it.inside == inside }
        for (drop in litterHere) {
            val lr = Random(seed + drop.id.hashCode())
            val lx = worldW * (0.06f + lr.nextFloat() * 0.88f)
            val ly = h * (0.70f + lr.nextFloat() * 0.24f)
            val item = itemById(drop.itemId)
            InteriorArt.drawLitter(c, lx, ly, h * 0.026f, item?.icon ?: "🗑️")
            val r = h * 0.032f
            hits.add(Hit("litter", drop.id, RectF(lx - r, ly - r, lx + r, ly + r)))
        }
    }

    private fun drawFigures(c: Canvas, st: GameState, eng: Engine, loc: Location,
                            inside: Boolean, worldW: Float, h: Float, seed: Long,
                            speakers: Set<String>, fs: Float = 1f) {
        val me = eng.player(st)
        // v0.16.0a: aboard a bus or inside a street-house
        // section, the room holds who it holds — a bus
        // carries its driver, a section house its own
        // quiet; the location's crowd does not follow you in
        val rideHere = BusRideState.read(st)
        val sectionHere = st.lifeText["insideSection"]
        val castHere = if (rideHere != null) {
            st.agents.values.filter {
                !it.isPlayer && it.name == SchoolBus.DRIVER &&
                    rideHere.school
            }.sortedBy { it.name }
        } else if (inside && sectionHere != null) {
            emptyList()
        } else {
            st.agents.values.filter {
                !it.isPlayer && it.location == me.location && it.waypoints.isEmpty() &&
                    CharacterSystem.npcInside(it.location) == inside
            }.sortedBy { it.name }
        }
        val genHere = if (rideHere != null || (inside && sectionHere != null))
            emptyList() else generated
        val total = castHere.size + genHere.size
        // v0.15.0c: no early return — the player ALWAYS
        // stands in this scene now (drawn below, y-sorted
        // into this same pass), even on an empty street.
        val now = System.currentTimeMillis()

        // render list: generated first (background rows), cast in front
        data class Fig(val name: String, val isCast: Boolean, val asleep: Boolean,
                       val activity: String, val gen: ActiveNpc?)
        val figs = mutableListOf<Fig>()
        for (g in genHere) figs.add(Fig(g.persona.loreName, false, false, "", g))
        for (a in castHere) figs.add(Fig(a.name, true, a.asleep, a.activity, null))
        // #13 BUS STOP HUDDLES: on school mornings the waiting kids
        // sort themselves — friends cluster, factions keep apart.
        if (loc.id == "BusStop" && st.timeMin < 9 * 60) {
            figs.sortBy { f ->
                FACTION_OF_CAST[f.name] ?: f.gen?.persona?.factionId ?: f.name
            }
        }

        // ---- GROUND-PLANE layout (v0.1.6): every figure stands ON the
        // ground band, feet on a floor line. Chunks of the list form
        // rows BACK to FRONT: back rows sit a shallow pitch UP-screen
        // with depth scale, the front row is lowest and biggest and is
        // drawn last. Pitch (0.54 x figure height) + depth shrink keep
        // any head below the face zone of the row behind — nobody
        // stands on anybody's head anymore.
        // ---- v0.5.0g: CROWD CLUSTERS — at Crowds.CLUSTER_MIN_TOTAL
        // or more present, Crowds takes the layout: friends and
        // pair-script partners gather into conversational clusters
        // instead of spreading into clutter (Lucky's correction).
        // Pure + deterministic off a chemistry snapshot of the
        // town's own social data; below the threshold it stays
        // null and the classic spread below runs untouched. ----
        val crowdLayout = if (total >= Crowds.CLUSTER_MIN_TOTAL) {
            val present = figs.map { it.name }.toSet()
            val bond = mutableMapOf<Set<String>, Int>()
            val rivals = mutableSetOf<Set<String>>()
            val castNames = castHere.map { it.name }
            for (a in castNames) for (b in castNames) {
                if (a >= b) continue
                val key = setOf(a, b)
                st.npcBonds[eng.bondKey(a, b)]?.let { bond[key] = it }
                if (relationNote(a, b) == "arch-rivals") rivals.add(key)
            }
            val factions = mutableMapOf<String, String>()
            for (f in figs) {
                FACTION_OF_CAST[f.name]?.let { factions[f.name] = it }
                f.gen?.persona?.factionId?.let { factions[f.name] = it }
            }
            Crowds.layout(figs.map { it.name }, Crowds.Chem(
                bond = bond,
                scriptPairs = PAIR_SCRIPTS.keys
                    .filter { p -> p.all { it in present } }.toSet(),
                rivalPairs = rivals,
                factionOf = factions,
                // v0.9.0b: the player's crew stands as a
                // standing cluster inside any crowd
                crew = Crew.members(st).filter { it in present }
                    .toSet(),
                // v0.9.0e: the rival's crew stands as its
                // own cluster on its own affinity — the
                // two formations can share a frame without
                // becoming one crowd
                rivalCrew = RivalCrew.crewSet(st)
                    .filter { it in present }.toSet()))
        } else null
        val rowsClassic = crowdLayout?.rows ?: when {
            total <= 5 -> 1; total <= 10 -> 2; total <= 16 -> 3; else -> 4
        }
        val perRow = (total + rowsClassic - 1) / rowsClassic
        // v0.15.0d: the crowd stands IN the walkable band
        // (WalkBand) and sizes off ONE world scale — the
        // base figure IS WorldScale's kid. A packed scene
        // narrows the slots, not the kid: fhBase dips at
        // most 8%, where the old pass could crush figures
        // to 40% just for standing in a crowd (and shrank
        // adults toward kid height with them).
        // v0.15.0g: the composition itself is FigureCompose's
        // now — ONE pure pass turns the spots below into
        // placed geometry (center x, feet line, on-screen
        // height): the crowd gathers in a window of the
        // widened world at true body pitch (no more blob —
        // the old single-screen slot pitch crushed 40 kids
        // into overlapping heads), every figure scales by
        // WalkBand.depthScale at its own feet (the old
        // rowScale step is retired into the one curve), and
        // the standing bus's span is forbidden ground, so
        // the traffic plane can never swallow a body again.
        // per-figure height multipliers (v0.1.7): CAST uses
        // their own measured heightRel — the old code sized
        // every cast member by shared BAND height, which is
        // why per-character heights never showed. Generated
        // NPCs keep the band ladder (no image to measure)
        // with a deterministic per-NPC seed jitter, and the
        // 0.96 the draw loop always applied to them.
        val figByName = figs.associateBy { it.name }
        fun heightRelOf(name: String): Float {
            val fig = figByName[name] ?: return 1f
            return if (fig.isCast) KidArt.heightFor(fig.name)
                else CharacterSystem.heightScale(fig.gen!!.persona.band) *
                    (0.94f + ((fig.name.hashCode() and 0x7fffffff) % 13) / 100f) *
                    0.96f
        }
        fun widthRatioOf(name: String): Float = when (name) {
            "Timmy" -> 1.10f   // the chair is wider than a body
            "Jimmy" -> 0.95f   // crutches out at his sides
            else -> FigureCompose.WIDTH_RATIO
        }
        // v0.15.0d: the old head-clearance clamp is GONE —
        // it was the adult-shrinker: in any crowd it cut
        // fhBase until the tallest adult's head cleared an
        // arbitrary scenery line, dragging every figure
        // (adults most) far under world scale. Rows now
        // pitch inside the band instead (above), and heads
        // stand in front of the facades like they should.

        // ---- v0.5.0g: standing spots (fraction across the
        // CROWD in layout space, frontness, mirror), computed
        // for the whole crowd BEFORE anyone draws — the
        // classic path below reproduces the pre-pass slot
        // math (chunk rows, alternate-row stagger, #41
        // wide-figure shifts); the crowd path takes Crowds'
        // clustered spots as-is, so a huddle's membership is
        // the pure function's, cover to cover. v0.15.0g:
        // the spots are FigureCompose's input — the compose
        // pass below owns the geometry they land in. ----
        val spots: List<FigureCompose.Spot> = if (crowdLayout != null) {
            val byName = crowdLayout.placed.associateBy { it.name }
            figs.map { f ->
                val p = byName.getValue(f.name)
                FigureCompose.Spot(f.name, p.xFrac, p.frontness, p.mirror)
            }
        } else {
            var ws = 0f   // #41: extra room claimed by wide figures
            var lc = -1
            figs.mapIndexed { idx, fig ->
                val chunk = idx / perRow               // 0 = back row
                if (chunk != lc) { ws = 0f; lc = chunk }
                val i = idx % perRow
                val inChunk = figs.drop(chunk * perRow).take(perRow)
                    .size.coerceAtLeast(1)
                var fx = 0.03f + 0.94f * (i + 0.5f + ws) / inChunk
                if (chunk % 2 == 1) fx += 0.42f / inChunk // stagger alternate rows
                val s = FigureCompose.Spot(fig.name, fx,
                    (rowsClassic - 1) - chunk, false)
                if (fig.name == "Timmy") ws += 0.35f
                if (fig.name == "Jimmy") ws += 0.20f
                s
            }
        }
        // ---- v0.15.0g: THE COMPOSE — spots become placed
        // figures. The standing bus's span is forbidden
        // ground whenever a bus stands (or is beating) at
        // this station, indoors never. ----
        val busForbidden: List<Pair<Float, Float>> =
            if (!inside && (SchoolBus.busAt(st, loc.id) ||
                    (tick < busShowUntilTick &&
                        PaperDressing.forLocation(loc).busStation))) {
                val (fl, fr) = FigureCompose.busSpanFrac(BUS_FX, worldW, h)
                listOf(fl * worldW to fr * worldW)
            } else emptyList()
        val composed = FigureCompose.compose(
            spots, ::heightRelOf, ::widthRatioOf,
            worldW, width.toFloat(), h, inside,
            FigureCompose.focalFrac(loc.id), busForbidden)
        val placedByName = composed.placed.associateBy { it.name }
        val fhBase = composed.fhBase
        val slotW = composed.slotW
        // ---- v0.16.0c (item 12 closed): TRAVELERS — cast
        // walking a route between schedule blocks, present
        // in THIS scene while Travel.pose puts them here.
        // They are not crowd: they enter at an edge, cross
        // the sidewalk band in profile at scene scale, and
        // leave at the far edge (or walk in to the crowd's
        // heart in their destination scene and settle).
        // Drawn interleaved with the crowd by feet line,
        // so depth reads like everyone else's.
        data class TravelerDraw(val name: String, val cx: Float,
                                val feetY: Float, val fh: Float,
                                val facing: Int)
        val travelers: List<TravelerDraw> = if (inside ||
            rideHere != null) emptyList() else {
            val feetY = WalkBand.feetY(Travel.BAND, h, false)
            val ds = WalkBand.depthScale(Travel.BAND)
            st.agents.values.mapNotNull { a ->
                if (a.isPlayer || a.waypoints.isEmpty()) null
                else Travel.pose(st, a)
                    ?.takeIf { it.sceneId == loc.id }
                    ?.let { pose ->
                        TravelerDraw(a.name, pose.xFrac * worldW,
                            feetY,
                            fhBase * KidArt.heightFor(a.name) * ds,
                            if (pose.facingRight) 1 else -1)
                    }
            }.sortedBy { it.cx }
        }
        // paint back-to-front by composed feet line (the
        // y-sort PaperScene.depthSorted states), left to
        // right within a feet line
        val drawOrder: List<Int> = figs.indices.sortedWith(
            compareBy<Int> { placedByName[figs[it].name]?.feetY ?: 0f }
                .thenBy { placedByName[figs[it].name]?.cx ?: 0f })
        // ---- v0.15.0c: YOU, IN THE SCENE — the player
        // figure, drawn in baseline order inside this same
        // pass (PaperScene.depthSorted's rule, applied to
        // the crowd): in front of everyone whose feet sit
        // higher on screen, behind everyone lower. What
        // you wear is LiveLook's call — day clothes, your
        // pajamas on a live sleepover night, your hero
        // costume in the patrol window — the same codes
        // the mirror, the previews and the shutter read.
        // The walk is PaperWalk's: bob and phase from the
        // state machine, facing flipped by direction. ----
        val playerBaseY = WalkBand.feetY(pwalk.band, h, inside)
        var playerDrawn = false
        fun drawPlayerNow() {
            if (playerDrawn) return
            playerDrawn = true
            val px = pwalk.x * worldW
            val pfh = fhBase * WalkBand.depthScale(pwalk.band)
            // the paper shadow every cut-out stands on
            c.drawOval(RectF(px - pfh * 0.30f, playerBaseY - pfh * 0.030f,
                px + pfh * 0.30f, playerBaseY + pfh * 0.048f),
                Paper.fillPaint("#1a2436", 46))
            // v0.16.0e (spec B3+B4): hand the walk loop the
            // player's own gait geometry at his drawn size,
            // and the ground truth his footstep notifies
            // fire on — recomputed here, where both are
            // actually known, every frame
            val pPlan = FigureGeometry.derive(
                avatarSpec(me.appearance)).plan
            playerPhasePerFrac = Gait.phasePerFrac(pPlan.legLen,
                pPlan.boxH, if (worldW > 0f) pfh / worldW else 0f)
            stepSnow = st.weatherId in setOf("light_snow",
                "heavy_snow", "blizzard")
            stepInside = inside
            val look = LiveLook.playerLook(st)
            // v0.16.0a (items 16–18): the player walks in
            // their own SIDE PROFILE (facing = travel
            // direction), with their own motion parameters
            // and the current action animation riding the
            // base cycle; seated includes bus rides
            val rideHere = BusRideState.active(st)
            val seatedNow = sitting || rideHere
            val pMotion = MotionRegistry.forPlayer(
                avatarSpec(me.appearance))
            var pPose = if (seatedNow) "sit" else "stand"
            var lift = 0f
            if (sitAnimMode != 0) {
                val el = (tick - sitAnimStartTick).toInt()
                val total = ActionAnims.byId("sit_down")!!.ticks
                if (el in 0 until total) {
                    val ph = ActionAnims.sitPhase(el, total,
                        sitAnimMode > 0)
                    if (sitAnimMode > 0) {
                        lift = (1f - ph) * pfh * 0.10f
                        if (ph < 0.4f) pPose = "stand"
                    } else {
                        lift = ph * pfh * 0.10f
                        if (ph > 0.6f) pPose = "sit"
                    }
                } else sitAnimMode = 0
            }
            // v0.16.0c (item 18 closed): EVERY look walks in
            // its true side profile now — the sleep look's
            // pajamas and nightcap are spec geometry the
            // profile pass draws natively, and the hero
            // look's cape is back-item geometry with a
            // profile-aware mask + emblem in KidArt's hero
            // pass. The v0.16.0a stopgap — mirror-flipping
            // the FRONT view for the sleep/hero looks when
            // walking left — is removed whole: facing flows
            // to all three looks exactly as it always has
            // to the day look.
            val anim = FigureAnim(timeMs = now, seed = me.name.hashCode(),
                walking = pwalk.walking, walkPhase = pwalk.phase,
                wind = st.weatherId == "wind",
                facing = if (pwalk.walking) pwalk.facing else 0,
                action = currentPlayerAction(),
                motionAmp = pMotion.strideDeg / 20f)
            val cy = playerBaseY - pfh / 2f - lift
            when (look.kind) {
                LiveLook.SLEEP -> KidArt.drawSleepFigure(c,
                    avatarSpec(me.appearance), look.code, px, cy, pfh,
                    "idle", "stand", anim)
                LiveLook.HERO -> KidArt.drawHeroFigure(c,
                    avatarSpec(me.appearance), look.code, px, cy, pfh,
                    "idle", "stand", anim)
                else -> KidArt.drawPlayerAvatar(c, me.appearance,
                    px, cy, pfh, anim, pPose)
            }
            // your tag, in the crowd's own tag style
            val tp = Paper.textPaint("#ffe9a8", 15f * fs,
                bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            val tw = tp.measureText("YOU")
            c.drawRoundRect(RectF(px - tw / 2 - 9, playerBaseY + 5,
                px + tw / 2 + 9, playerBaseY + 31), 8f, 8f,
                Paper.fillPaint("#000000", 140))
            c.drawText("YOU", px, playerBaseY + 24, tp)
        }
        // Phase 2: screen positions, kept for the glyph overlay pass
        data class Pos(val name: String, val isCast: Boolean, val asleep: Boolean,
                       val walking: Boolean, val dest: String?,
                       val x: Float, val baseY: Float, val fh: Float,
                       val mail: Int = 0, val alarm: Boolean = false)
        val posList = mutableListOf<Pos>()
        // ---- v0.16.0c: the traveler draw — one walker,
        // drawn with the crowd's own calls (hero costume
        // and holiday costume read as they do standing;
        // the snow lies on the walker's own crown by the
        // crowd's derived-ellipse math). A walker never
        // crosses THROUGH the player: inside body pitch
        // of the New Kid at his row, the walker steps to
        // the pitch's nearer edge, as the crowd does.
        var travelerIdx = 0
        fun drawTraveler(t: TravelerDraw) {
            val a = st.agents[t.name] ?: return
            var cx = t.cx
            run {
                val px = pwalk.x * worldW
                if (kotlin.math.abs(t.feetY - playerBaseY) <
                    t.fh * 0.34f) {
                    val pw = fhBase * WalkBand.depthScale(pwalk.band) *
                        FigureCompose.WIDTH_RATIO
                    val need = (t.fh * FigureCompose.WIDTH_RATIO +
                        pw) / 2f * FigureCompose.PITCH_SLACK
                    val dx = cx - px
                    if (kotlin.math.abs(dx) < need)
                        cx = px + (if (dx < 0f) -need else need)
                }
            }
            c.drawOval(RectF(cx - t.fh * 0.30f,
                t.feetY - t.fh * 0.030f,
                cx + t.fh * 0.30f, t.feetY + t.fh * 0.048f),
                Paper.fillPaint("#1a2436", 46))
            val motion = MotionRegistry.forName(t.name)
            // v0.16.0e (spec B3): the walker's phase is its
            // DISTANCE along the route — Travel's progress
            // over the route's ground length (each leg
            // crosses about one scene width of street)
            // through the walker's own leg geometry. The
            // shared free-running clock phase it replaces
            // ((now % 1256) / 200 — every walker in town on
            // the same wall-clock cycle, feet sliding at
            // whatever speed the grid walker happened to
            // move) is removed whole.
            val tPose = Travel.pose(st, a)
            val tPlan = FigureGeometry.derive(
                KidArt.SHEETS[t.name] ?: SheetSpec()).plan
            val legCount = ((tPose?.route?.size ?: 2) - 1)
                .coerceAtLeast(1)
            val phase = (tPose?.progress ?: 0f) * legCount * worldW *
                Gait.phasePerPx(tPlan.legLen, tPlan.boxH, t.fh)
            val anim = FigureAnim(timeMs = now,
                seed = t.name.hashCode(), walking = true,
                walkPhase = phase,
                wind = st.weatherId == "wind",
                facing = t.facing,
                motionAmp = motion.strideDeg / 20f)
            // v0.16.0e (spec B4): the walker's foot-down
            // notifies — crossings since the last frame fire
            // a step under the player's hearing (throttled
            // like the voice blips, at a traveler's gain),
            // and in snow each landing puffs at the foot
            val prevPhase = travelerPhase.put(t.name, phase)
            if (prevPhase != null) {
                val downs = Gait.footDowns(prevPhase, phase)
                if (downs.isNotEmpty()) {
                    val nowSfx = System.currentTimeMillis()
                    if (nowSfx - lastStepSfxMs > 90L) {
                        lastStepSfxMs = nowSfx
                        SoundEngine.footstep(stepSnow, false,
                            downs.first(), 0.35f)
                    }
                }
            }
            if (stepSnow) {
                val puff = Gait.puffIntensity(phase, true)
                if (puff > 0.04f) {
                    val rr = t.fh * 0.085f
                    c.drawOval(RectF(cx - rr, t.feetY - rr * 0.26f + 2f,
                        cx + rr, t.feetY + rr * 0.26f + 2f),
                        Paper.fillPaint("#ffffff", (85f * puff).toInt()))
                }
            }
            // v0.16.0e pass 2: the walking cast wears the
            // fiction too — the population channel is the
            // people you SEE on the street, and in profile
            // the cape hangs behind the torso, the towel
            // rides the same diagonal as standing
            val tFicDef = SceneArt.fictionDef
            val tFicCostume = if (tFicDef != null)
                fictionCostumeNow(tFicDef, t.name,
                    t.name in CharacterSystem.CAST_KIDS)
            else Fiction.Costume.NONE
            if (tFicCostume == Fiction.Costume.FULL &&
                tFicDef != null) {
                val capeTop = t.feetY - t.fh *
                    (ScaleContract.PROP_RELATIVE["cape"] ?: 0.62f)
                val cape = Path().apply {
                    moveTo(cx - t.fh * 0.13f, capeTop)
                    lineTo(cx + t.fh * 0.13f, capeTop)
                    lineTo(cx + t.fh * 0.23f, t.feetY)
                    lineTo(cx - t.fh * 0.23f, t.feetY)
                    close()
                }
                c.drawPath(cape, Paper.fillPaint(tFicDef.primaryHex))
                c.drawRect(cx - t.fh * 0.23f, t.feetY - t.fh * 0.035f,
                    cx + t.fh * 0.23f, t.feetY,
                    Paper.fillPaint(tFicDef.accentHex))
            }
            val costume = CharacterSystem.costumeFor(t.name, holiday)
            if (costume == null && Heroes.inCostume(st, t.name))
                KidArt.drawKidHero(c, t.name, cx,
                    t.feetY - t.fh / 2, t.fh, "idle", "stand", anim)
            else
                KidArt.drawKid(c, t.name, cx,
                    t.feetY - t.fh / 2, t.fh, "idle", costume,
                    "stand", anim)
            if (tFicCostume == Fiction.Costume.PARTIAL &&
                tFicDef != null) {
                c.save()
                c.translate(cx, t.feetY - t.fh * 0.52f)
                c.rotate(-16f)
                c.drawRoundRect(RectF(-t.fh * 0.19f, -t.fh * 0.055f,
                    t.fh * 0.19f, t.fh * 0.055f), t.fh * 0.05f,
                    t.fh * 0.05f, Paper.fillPaint("#e8e2d2"))
                c.drawRect(-t.fh * 0.19f, t.fh * 0.028f,
                    t.fh * 0.19f, t.fh * 0.055f,
                    Paper.fillPaint(tFicDef.accentHex))
                c.restore()
                c.drawCircle(cx + t.fh * 0.15f,
                    t.feetY - t.fh * 0.40f, t.fh * 0.045f,
                    Paper.fillPaint("#e8e2d2"))
            }
            if (st.weatherId in setOf("light_snow", "heavy_snow",
                    "blizzard") && !a.asleep) {
                val cover = TownLife.snowCover(t.name, st.timeMin)
                if (cover > 0.03f) {
                    val spec = KidArt.SHEETS[t.name] ?: SheetSpec()
                    val d = FigureGeometry.derive(spec)
                    val boxH = d.plan.boxH
                    val headCY = t.feetY -
                        t.fh * (0.5f - (d.hy - boxH / 2f) / boxH)
                    val rX = t.fh * d.hrx / boxH
                    val rY = t.fh * d.hry / boxH
                    c.drawArc(RectF(cx - rX, headCY - rY,
                        cx + rX, headCY + rY),
                        200f, 140f, false,
                        Paper.strokePaint("#ffffff", t.fh * 0.026f,
                            (190 * cover).toInt()))
                }
            }
            // the name tag, in the crowd's own tag style
            val tp = Paper.textPaint(a.color, 15f * fs,
                bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            val tw = tp.measureText(t.name)
            c.drawRoundRect(RectF(cx - tw / 2 - 9, t.feetY + 5,
                cx + tw / 2 + 9, t.feetY + 31), 8f, 8f,
                Paper.fillPaint("#000000", 140))
            c.drawText(t.name, cx, t.feetY + 24, tp)
            hits.add(Hit("kid", t.name,
                RectF(cx - t.fh * 0.42f, t.feetY - t.fh - 26,
                    cx + t.fh * 0.42f, t.feetY + 38)))
            posList.add(Pos(t.name, true, false, true,
                a.destination, cx, t.feetY, t.fh))
        }
        fun drawTravelersUpTo(feetLimit: Float) {
            while (travelerIdx < travelers.size &&
                travelers[travelerIdx].feetY <= feetLimit) {
                drawTraveler(travelers[travelerIdx])
                travelerIdx++
            }
        }
        for (idx in drawOrder) {
            val fig = figs[idx]
            val spot = spots[idx]
            // v0.15.0g: center, feet and height all come
            // from the compose pass (depth-scaled at the
            // figure's own feet); the classic path keeps
            // its breath of per-figure jitter on top
            val placed = placedByName.getValue(fig.name)
            var kx = placed.cx
            if (crowdLayout == null) {
                val jr = Random(seed + fig.name.hashCode())
                kx += (jr.nextFloat() - 0.5f) * composed.windowW * 0.008f
            }
            // v0.16.0b: the compose pass cannot see the player —
            // reserve his ground. A same-row figure whose center
            // lands inside body pitch of the player's center is
            // dealt to the nearer edge of that pitch, so nobody
            // stands IN the New Kid (the crowd pile over the
            // player in Lucky's Bus Stop / Main Street shots).
            run {
                val px = pwalk.x * worldW
                if (kotlin.math.abs(placed.feetY - playerBaseY) <
                    placed.fh * 0.34f) {
                    val pw = fhBase * WalkBand.depthScale(pwalk.band) *
                        FigureCompose.WIDTH_RATIO
                    val need = (placed.fh * widthRatioOf(fig.name) + pw) /
                        2f * FigureCompose.PITCH_SLACK
                    val dx = kx - px
                    if (kotlin.math.abs(dx) < need)
                        kx = px + (if (dx < 0f) -need else need)
                }
            }
            val baseY = placed.feetY
            // v0.15.0c: the player paints in baseline order
            // with the crowd — everyone whose feet sit
            // higher on screen paints first, then you, then
            // everyone lower (nearer the camera)
            // v0.16.0c: travelers join the same baseline
            // order — anyone crossing at or above your feet
            // paints before you; the rest of this row's
            // travelers paint before this row does.
            if (!playerDrawn && baseY >= playerBaseY) {
                drawTravelersUpTo(playerBaseY)
                drawPlayerNow()
            }
            drawTravelersUpTo(baseY)
            val fh = placed.fh
            var mood = when {
                fig.asleep -> "sleep"
                fig.name in speakers -> "talk"
                // #46 FACES FOLLOW FEELINGS: a co-present arch-rival
                // flashes hurt eyes now and then; your close friends
                // soften into eyelids around you; event climaxes put
                // the whole crowd on edge.
                fig.isCast && castHere.any { other ->
                    other.name != fig.name &&
                        (relationNote(fig.name, other.name) == "arch-rivals" ||
                            (st.npcBonds[eng.bondKey(fig.name, other.name)] ?: 0) <= -3)
                } && (tick / 90 + fig.name.hashCode()) % 3 == 0L -> "hurt"
                fig.isCast && (st.friendship[fig.name] ?: 0) >= 60 -> "soft"
                st.shenaniganId != null && Quests.currentStage(st) == 2 &&
                    (tick / 70 + fig.name.hashCode()) % 4 == 0L -> "hurt"
                !inside && st.weatherId == "blizzard" &&
                    (fig.name.hashCode() + (tick / 40).toInt()) % 5 == 0 -> "hurt"
                else -> "idle"
            }
            val seatTheme = inside && interiorOf(loc.id)?.theme in SEAT_THEMES
            val pose = when {
                fig.asleep -> "stand"
                seatTheme && idx % 2 == 0 -> "sit"
                !inside && loc.id == "BusStop" && idx == 0 -> "sit"
                fig.activity.contains("Sitting") || fig.activity.contains("class") -> "sit"
                else -> "stand"
            }
            // ---- procedural animation state from the figure's activity:
            // speakers stand and talk; sleepers breathe slowly (handled
            // in KidArt by mood); standing figures stroll now and then.
            val canStroll = !fig.asleep && pose == "stand" && mood != "talk" &&
                fig.name != "Timmy"
            var walking = false
            var walkPhase = 0f
            var facingNow = 0
            // v0.16.0a (items 11+12+16): continuous
            // real-time WANDER — every standing figure
            // walks its own range inside its crowd slot
            // (capped by Wander.REACH_OF_PITCH so nobody
            // can pile onto a neighbour), in its own
            // MotionRegistry gait, wearing its side
            // profile while it moves. The old strollFor
            // shuffle is retired whole.
            val motion = MotionRegistry.forName(fig.name)
            if (canStroll) {
                val slotFrac = (slotW / worldW) *
                    if (crowdLayout != null) 0.35f else 1f
                // v0.16.0e (spec B3): the wander phase is
                // distance-driven now — Wander reads the
                // figure's own leg geometry and drawn height
                // and advances the cycle by the path actually
                // walked, so the crowd's feet land too
                val figSpec = if (fig.isCast)
                    KidArt.SHEETS[fig.name] ?: SheetSpec()
                else fig.gen!!.persona.spec
                val figPlan = FigureGeometry.derive(figSpec).plan
                val wp = Wander.pose(fig.name, slotFrac,
                    placed.fh / worldW, figPlan.legLen, figPlan.boxH,
                    now, st.timeMin, st.absDay)
                kx = (kx + wp.dxFrac * worldW)
                    .coerceIn(worldW * 0.045f, worldW * 0.955f)
                walkPhase = wp.phase
                walking = wp.walking
                facingNow = wp.facing
            }
            val anim = FigureAnim(timeMs = now, seed = fig.name.hashCode(),
                walking = walking, walkPhase = walkPhase,
                wind = st.weatherId == "wind",
                facing = facingNow,
                action = if (mood == "talk") "conversation" else "",
                motionAmp = motion.strideDeg / 20f)
            // #19 RAIN DASH: outdoor walkers in rain throw their arms
            // over their heads and hurry (KidArt's "rain" pose).
            val drawPose = if (walking && !inside &&
                st.weatherId in setOf("rain", "storm")) "rain" else pose
            // ---- v0.1.9a #16 HAT GRABS: on windy days, standing
            // figures periodically SNATCH at their headwear before
            // the gust can take it (staggered per figure).
            val hatted = if (fig.isCast) {
                val sp = KidArt.SHEETS[fig.name]
                sp != null && sp.hatId != "none"
            } else fig.gen!!.persona.spec.hatId != "none"
            val drawPose2 = when {
                drawPose == "rain" -> drawPose
                !inside && st.weatherId == "wind" && drawPose == "stand" && hatted &&
                    ((tick / 45) + (fig.name.hashCode() and 0x7fffffff)) % 8 == 0L -> "hatgrab"
                else -> drawPose
            }
            // ---- v0.1.9a #12 MAIL CHECK RITUAL: at their own house
            // in their seeded morning window, a resident drifts to
            // the mailbox (its x is SceneArt's), peeks — then hops
            // (a letter!) or slumps (a bill).
            var drawBaseY = baseY
            var mailGlyph = 0
            if (!inside && fig.isCast && !fig.asleep &&
                KID_HOUSE[fig.name] == loc.id) {
                val phase = TownLife.mailPhaseAt(fig.name, st.absDay, st.timeMin)
                if (phase != null) {
                    // v0.15.0g: the mailbox stands where
                    // SceneArt paints it — at the facade's
                    // left edge, off FacadeGeom with the
                    // scene's own unit width (the 0.188
                    // world fraction it was hardcoded to in
                    // the narrow world is far down the
                    // widened street now)
                    val mfr = FacadeGeom.facadeRect("house", worldW, h,
                        SceneScale.unitW(worldW, loc, false))
                    val mailboxX = mfr.left - mfr.width * 0.10f
                    if (phase < 0.45f) {
                        kx += (mailboxX - kx) * (phase / 0.45f) * 0.85f
                        if (phase > 0.30f) mailGlyph = 1
                    } else {
                        kx = mailboxX
                        if (TownLife.mailHasLetter(fig.name, st.absDay)) {
                            drawBaseY = baseY -
                                kotlin.math.abs(kotlin.math.sin(tick * 0.45f)) * fh * 0.07f
                            mailGlyph = 2
                        } else {
                            drawBaseY = baseY + fh * 0.02f
                            mood = "hurt"
                            mailGlyph = 3
                        }
                    }
                }
            }
            // ---- v0.1.9a #33: a figure mid shake-off wiggles
            val shakingFig = !inside &&
                st.weatherId in setOf("light_snow", "heavy_snow", "blizzard") &&
                !fig.asleep && TownLife.snowShaking(fig.name, st.timeMin)
            val drawKx = if (shakingFig) kx + kotlin.math.sin(tick * 2.4f) * 3f else kx
            // #37 FOG SILHOUETTES: in fog, figures far from you are
            // soft gray shapes until you get close.
            val fogFar = !inside && st.weatherId == "fog" &&
                kotlin.math.abs(kx - (camX + width / 2f)) > worldW * 0.20f
            // v0.5.0g: a cluster member right of their group's
            // center turns INWARD toward the conversation — the
            // figure, its backpack and its weather are drawn
            // mirrored around its own center for this pass
            // (PhotoView's back-to-back trick); name tags and
            // glyphs stay unmirrored, outside the transform
            val mirrorFig = spot.mirror && !fogFar
            if (mirrorFig) {
                c.save()
                c.translate(drawKx, 0f)
                c.scale(-1f, 1f)
                c.translate(-drawKx, 0f)
            }
            // #79 NEW BACKPACK WEEK: the first school mornings of the
            // year, kids sport fresh backpacks in bright colors.
            if (!inside && loc.id == "BusStop" && st.gameMonth == 9 &&
                st.gameDay <= 5 && st.timeMin < 10 * 60 && fig.isCast &&
                !CharacterSystem.isAdult(fig.name)) {
                val bpCols = listOf("#d43d2a", "#2a9a4a", "#e8c53a", "#7a4a9a", "#2a6fd4")
                val bc = bpCols[(fig.name.hashCode() and 0x7fffffff) % bpCols.size]
                c.drawRoundRect(RectF(kx - fh * 0.30f, baseY - fh * 0.64f,
                    kx - fh * 0.13f, baseY - fh * 0.30f), 5f, 5f, Paper.fillPaint(bc))
            }
            // v0.16.0e pass 2 (costume system, population
            // channel): the fiction's costume overlay for
            // this figure — a cape behind the FULL
            // believers, a towel sash over the PARTIAL
            // ones. Presentation only: the overlay never
            // touches the civic layer (schedules, the
            // life sim) — it reads belief and draws cloth.
            val ficDef = SceneArt.fictionDef
            // (no !inside gate: the kids' game does not
            // stop at doors — a believer wears the cape
            // in the lobby too, which the fabricated
            // interior doors already assume)
            val ficCostume = if (ficDef != null)
                fictionCostumeNow(ficDef, fig.name,
                    fig.isCast &&
                        fig.name in CharacterSystem.CAST_KIDS)
            else Fiction.Costume.NONE
            if (ficCostume == Fiction.Costume.FULL && !fogFar &&
                ficDef != null) {
                val capeTop = drawBaseY - fh *
                    (ScaleContract.PROP_RELATIVE["cape"] ?: 0.62f)
                val cape = Path().apply {
                    moveTo(drawKx - fh * 0.15f, capeTop)
                    lineTo(drawKx + fh * 0.15f, capeTop)
                    lineTo(drawKx + fh * 0.27f, drawBaseY)
                    lineTo(drawKx - fh * 0.27f, drawBaseY)
                    close()
                }
                c.drawPath(cape, Paper.fillPaint(ficDef.primaryHex))
                c.drawRect(drawKx - fh * 0.27f, drawBaseY - fh * 0.035f,
                    drawKx + fh * 0.27f, drawBaseY,
                    Paper.fillPaint(ficDef.accentHex))
            }
            if (fogFar) {
                val sil = Paper.fillPaint("#9aa4ae", 205)
                c.drawCircle(drawKx, drawBaseY - fh * 0.82f, fh * 0.17f, sil)
                c.drawRoundRect(RectF(drawKx - fh * 0.19f, drawBaseY - fh * 0.68f,
                    drawKx + fh * 0.19f, drawBaseY - fh * 0.05f), fh * 0.1f, fh * 0.1f, sil)
            } else if (fig.isCast) {
                val costume = CharacterSystem.costumeFor(fig.name, holiday)
                // v0.10.0b: a roster kid standing a patrol spot
                // on a patrol night is in their hero costume —
                // cape, mask and emblem over their own colors
                // (Heroes.inCostume reads the same moment the
                // camera does, so street and album agree).
                if (costume == null && Heroes.inCostume(st, fig.name))
                    KidArt.drawKidHero(c, fig.name, drawKx,
                        drawBaseY - fh / 2, fh, mood, drawPose2, anim)
                else
                    KidArt.drawKid(c, fig.name, drawKx, drawBaseY - fh / 2, fh, mood, costume, drawPose2, anim)
            } else {
                val p = fig.gen!!.persona
                val adj = CharacterSystem.bandAdjust(p.spec, p.band)
                KidArt.drawFigure(c, adj, drawKx, drawBaseY - fh / 2, fh, mood, drawPose2, anim)
            }
            if (ficCostume == Fiction.Costume.PARTIAL &&
                !fogFar && ficDef != null) {
                // a bath towel worn as a cloak, knot at the
                // hip — partial belief, worn proudly anyway
                c.save()
                c.translate(drawKx, drawBaseY - fh * 0.52f)
                c.rotate(-16f)
                c.drawRoundRect(RectF(-fh * 0.21f, -fh * 0.055f,
                    fh * 0.21f, fh * 0.055f), fh * 0.05f, fh * 0.05f,
                    Paper.fillPaint("#e8e2d2"))
                c.drawRect(-fh * 0.21f, fh * 0.028f, fh * 0.21f,
                    fh * 0.055f, Paper.fillPaint(ficDef.accentHex))
                c.restore()
                c.drawCircle(drawKx + fh * 0.17f,
                    drawBaseY - fh * 0.40f, fh * 0.045f,
                    Paper.fillPaint("#e8e2d2"))
            }
            // v0.16.0e (spec B4): a wanderer's foot-down
            // puff in snow, read straight off its distance-
            // driven cycle phase — the notify made visible.
            // (The crowd's steps stay unvoiced by design:
            // thirty pairs of feet would drown the mix; the
            // player's and travelers' steps carry the sound.)
            if (walking && !inside && stepSnow && !fogFar) {
                val puffI = Gait.puffIntensity(walkPhase, true)
                if (puffI > 0.04f) {
                    val rr = fh * 0.085f
                    c.drawOval(RectF(drawKx - rr,
                        drawBaseY - rr * 0.26f + 2f,
                        drawKx + rr, drawBaseY + rr * 0.26f + 2f),
                        Paper.fillPaint("#ffffff", (80f * puffI).toInt()))
                }
            }
            if (!fogFar) {
                // #33 SNOW BUILDUP + SHAKE-OFF (completed v0.1.9a):
                // cover whitens over ~20 minutes of snowfall, then
                // the wearer stops and shakes it all off — wiggle
                // (drawKx), snow burst, cover drops to a dusting.
                if (!inside && st.weatherId in setOf("light_snow", "heavy_snow", "blizzard") &&
                    !fig.asleep) {
                    val cover = if (shakingFig) 0.15f
                        else TownLife.snowCover(fig.name, st.timeMin)
                    if (cover > 0.03f) {
                        // v0.16.0b: the snow lies ON the head — a
                        // thick arc stroked along the head ellipse's
                        // own crown. The old flat 0.40-wide ellipse,
                        // center-anchored for every pose, floated
                        // behind/above profile heads as a pale disc
                        // (Lucky's playtest (c)); an arc on the
                        // crown's own curve cannot float. The
                        // ellipse is the figure's OWN derived head
                        // (FigureGeometry.derive on its spec — the
                        // same numbers KidArt just drew from),
                        // mapped through the unit box KidArt scales
                        // by (h / boxH, figure centered): no class
                        // averages, so measured heads get their own
                        // arc. Inside the mirror transform, so it
                        // mirrors with the figure.
                        val spec = if (fig.isCast)
                            KidArt.SHEETS[fig.name] ?: SheetSpec()
                            else CharacterSystem.bandAdjust(
                                fig.gen!!.persona.spec, fig.gen!!.persona.band)
                        val d = FigureGeometry.derive(spec)
                        val boxH = d.plan.boxH
                        val headCY = drawBaseY -
                            fh * (0.5f - (d.hy - boxH / 2f) / boxH)
                        val rX = fh * d.hrx / boxH
                        val rY = fh * d.hry / boxH
                        c.drawArc(RectF(drawKx - rX, headCY - rY,
                            drawKx + rX, headCY + rY),
                            200f, 140f, false,
                            Paper.strokePaint("#ffffff", fh * 0.026f,
                                (190 * cover).toInt()))
                        c.drawRect(drawKx - fh * 0.15f, drawBaseY - fh * 0.63f,
                            drawKx + fh * 0.15f, drawBaseY - fh * 0.605f,
                            Paper.fillPaint("#ffffff", (115 * cover).toInt()))
                    }
                    if (shakingFig) {
                        val puff = Paper.fillPaint("#ffffff", 190)
                        c.drawCircle(drawKx - fh * 0.26f, drawBaseY - fh * 0.98f, 4.5f, puff)
                        c.drawCircle(drawKx + fh * 0.27f, drawBaseY - fh * 0.94f, 5.5f, puff)
                        c.drawCircle(drawKx + fh * 0.05f, drawBaseY - fh * 1.10f, 4f, puff)
                    }
                }
                // #59 FACTION COLORS DAY: at peak influence the whole
                // faction wears its accent for the day (an armband)
                val peak = TownLife.peakFaction(st)
                if (peak != null && fig.isCast && FACTION_OF_CAST[fig.name] == peak) {
                    val ac = TownLife.FACTION_ACCENTS[peak] ?: "#e8c53a"
                    c.drawRect(kx + fh * 0.16f, baseY - fh * 0.56f,
                        kx + fh * 0.23f, baseY - fh * 0.44f, Paper.fillPaint(ac))
                }
            }
            if (mirrorFig) c.restore()
            // #34 FOOTPRINT TRAILS: fresh snow remembers who passed
            if (walking && !inside &&
                st.weatherId in setOf("light_snow", "heavy_snow", "blizzard")) {
                val prev = trailPrev[fig.name]
                if (prev != null && prev.first == loc.id &&
                    kotlin.math.abs(prev.second - kx) > 26f) {
                    val dots = Paper.fillPaint("#aebfd8", 140)
                    for (d in 1..5) {
                        val t = d / 6f
                        val dx = prev.second + (kx - prev.second) * t
                        c.drawOval(RectF(dx - 4, baseY + 3 + (d % 2) * 5,
                            dx + 4, baseY + 7 + (d % 2) * 5), dots)
                    }
                }
                if (prev == null || kotlin.math.abs(prev.second - kx) > 26f ||
                    tick - prev.third > 60) {
                    trailPrev[fig.name] = Triple(loc.id, kx, tick)
                }
            }
            val alarmSprint = fig.isCast &&
                fig.name == TownLife.alarmKidFor(st) && st.timeMin in 470..570 &&
                (st.agents[fig.name]?.waypoints?.isNotEmpty() == true)
            posList.add(Pos(fig.name, fig.isCast, fig.asleep, walking,
                st.agents[fig.name]?.destination, kx, baseY, fh,
                mailGlyph, alarmSprint))
            // ---- name tags: front row + current speakers only (back
            // rows stay clean); long names shrink, then ellipsize to
            // the slot so tags can never mush into each other again
            if (fig.asleep) {
                val zp = Paper.textPaint("#ffffff", 22f, bold = true, casual = true)
                c.drawText("z", kx + fh * 0.22f, baseY - fh - 8, zp)
                c.drawText("z", kx + fh * 0.28f, baseY - fh - 26, zp)
            } else if (placed.frontness == 0 || fig.name in speakers) {
                val tag = if (fig.isCast) fig.name else fig.gen!!.persona.loreName
                val color = if (fig.isCast)
                    st.agents[fig.name]?.color ?: "#ffffff"
                else fig.gen!!.persona.color
                // v0.1.9f: tag text follows the system font scale;
                // the tag band only grows (geoScale), so a bigger
                // tag can never collide with the row above/below.
                val baseTagSize = (if (fig.isCast) 15f else 12.5f) * fs
                val gs = Chrome.geoScale(fs)
                val tp = Paper.textPaint(color, baseTagSize, bold = true, casual = true)
                tp.textAlign = Paint.Align.CENTER
                val maxTagW = slotW * 0.98f
                // v0.1.9b: the tag fit lives in Chrome now (same
                // shrink-then-ellipsize semantics, suite-pinned)
                val (tagSize, label) = Chrome.nameTagFit(tag, maxTagW, baseTagSize) { s, sz ->
                    tp.textSize = sz
                    tp.measureText(s)
                }
                tp.textSize = tagSize
                val tw = tp.measureText(label)
                val tr = RectF(kx - tw / 2 - 9, baseY + 5 * gs, kx + tw / 2 + 9, baseY + 31 * gs)
                c.drawRoundRect(tr, 8f * gs, 8f * gs, Paper.fillPaint("#000000", if (fig.isCast) 140 else 110))
                c.drawText(label, kx, baseY + 24 * gs, tp)
            }
            // #41 MOBILITY FITS THE ROOM: Timmy's wheelchair and
            // Jimmy's crutches get real width — in the tap target
            // and in the row spacing (wideShift, applied per chunk).
            val hw = fh * when (fig.name) {
                "Timmy" -> 0.66f
                "Jimmy" -> 0.52f
                else -> 0.42f
            }
            hits.add(Hit(if (fig.isCast) "kid" else "visitor", fig.name,
                RectF(kx - hw, baseY - fh - 26, kx + hw, baseY + 38)))
            // (#41's wide-figure row shifts now happen in the
            // standing-spots pre-pass, so the crowd's draw order
            // can differ from its slot order without losing them)
        }

        // ---- v0.15.0c: if the whole crowd stands nearer
        // the camera than you, you paint last of the
        // figures. (This is where v0.14.0a's street-costume
        // figure and v0.15.0a's street-pajamas figure used
        // to stand, alone and only in those two looks; the
        // player figure above now wears every LiveLook
        // kind, everywhere, as the scene's own citizen.)
        // v0.16.0c: travelers at your depth or beyond
        // paint before you; the nearest crossers (the
        // front band is in front of everyone) paint
        // after, and the pass ends with every walker
        // who crossed this frame on the ground. ----
        if (!playerDrawn) {
            drawTravelersUpTo(playerBaseY)
            drawPlayerNow()
        }
        drawTravelersUpTo(Float.MAX_VALUE)

        // ---- Phase 2 glyph pass: the small stuff you only notice
        // when you stop and watch the town live ----
        fun glyph(txt: String, x: Float, y: Float, size: Float, color: String = "#ffffff") {
            val p = Paper.textPaint(color, size, bold = true, casual = true)
            p.textAlign = Paint.Align.CENTER
            c.drawText(txt, x, y, p)
        }
        val millis2 = System.currentTimeMillis()
        for (p in posList) {
            if (p.asleep) continue
            val headY = p.baseY - p.fh
            val seedN = (p.name.hashCode() and 0x7fffffff)
            // #18 POCKET FIDGETS: waiting figures cycle tiny habits
            if (!p.walking) {
                val habits = listOf("♪", "…", "⚽", "🕐")
                val g = habits[(((tick / 55) + seedN) % habits.size).toInt()]
                glyph(g, p.x + p.fh * 0.30f, headY - 8, 13f, "#ffe9a8")
            }
            // #17 COLD-SNAP SNEEZES in freezing weather
            if (!inside && st.weatherId in setOf("heavy_snow", "blizzard") &&
                (tick + seedN * 7) % 460 < 14) {
                glyph("💨", p.x + p.fh * 0.22f, headY - 4, 16f)
                glyph("achoo!", p.x + p.fh * 0.30f, headY - 22, 10f, "#bcd6f2")
            }
            // #14 COFFEE STEAM TRAILS: walkers with coffee trail a
            // wisp — and Tweek's is three times anyone else's
            if (p.walking && (p.name == "Tweek" || p.dest == "TweekBros")) {
                val wisps = if (p.name == "Tweek") 3 else 1
                for (wi in 0 until wisps) {
                    val ph = ((millis2 / 700f) + wi * 0.6f) % 1.6f
                    c.drawCircle(p.x + p.fh * 0.24f + wi * 4f,
                        p.baseY - p.fh * 0.52f - ph * 15f, 3.5f + ph * 2f,
                        Paper.fillPaint("#ffffff", (130 - ph * 60).toInt().coerceIn(25, 130)))
                }
            }
            // #20 GOODBYE LINGER: a Friend+ watches you go, waving
            if (p.name == lingerName && tick <= lingerUntilTick) {
                glyph("👋", p.x + p.fh * 0.26f, headY - 12, 17f)
            }
            // v0.1.9a #12 MAIL RITUAL glyphs: peek, letter, bill
            when (p.mail) {
                1 -> glyph("📬", p.x + p.fh * 0.30f, headY - 10, 15f)
                2 -> glyph("💌", p.x + p.fh * 0.30f, headY - 10, 15f)
                3 -> glyph("🧾", p.x + p.fh * 0.30f, headY - 10, 14f)
            }
            // v0.1.9a #89 ALARM SPRINT: the oversleeper, live
            if (p.alarm) {
                glyph("💨", p.x - p.fh * 0.32f, p.baseY - p.fh * 0.5f, 14f)
                glyph("late!", p.x, headY - 14, 11f, "#ff9a8a")
            }
            // #32: slipping on a frozen puddle, mid-stride
            if (p.walking && loc.id in st.frozenPuddles &&
                TownLife.puddleSizeAt(st, loc.id) > 0 && (tick / 40 + seedN) % 5 == 0L) {
                glyph("💫", p.x, headY - 16, 14f)
            }
        }
        // #15 PHONE ZOMBIES: one walker per scene is somewhere else
        val walkers = posList.filter { it.walking && !it.asleep }
        if (walkers.isNotEmpty()) {
            val zombie = walkers[(st.absDay + loc.id.hashCode()).mod(walkers.size)]
            glyph("📱", zombie.x + zombie.fh * 0.18f, zombie.baseY - zombie.fh * 0.52f, 14f)
            if ((tick / 170 + zombie.name.hashCode()) % 3 == 0L) {
                glyph("💥", zombie.x, zombie.baseY - zombie.fh - 12, 15f)
            }
        }
        // #87 MUSIC NOTE TRAILS: one figure's radio leaks notes,
        // and the neighbors' heads bob along (in the bob already)
        val standers = posList.filter { !it.walking && !it.asleep }
        if (standers.isNotEmpty() && !inside) {
            val dj = standers[(st.absDay * 3 + loc.id.hashCode()).mod(standers.size)]
            val bob = kotlin.math.sin(millis2 * 0.004f) * 4f
            glyph("♪", dj.x + dj.fh * 0.3f, dj.baseY - dj.fh - 14 + bob, 15f, "#ffe9a8")
            glyph("♫", dj.x + dj.fh * 0.42f, dj.baseY - dj.fh - 30 - bob, 12f, "#ffe9a8")
        }
        // #88 HEARTS AND STORM GLYPHS + #86 ARGUMENT THUNDERHEADS:
        // ambient pairs leak what their bond is doing
        var pairGlyphs = 0
        for (i in posList.indices) {
            if (pairGlyphs >= 2) break
            for (j in i + 1 until posList.size) {
                if (pairGlyphs >= 2) break
                val a = posList[i]; val b = posList[j]
                if (!a.isCast || !b.isCast || a.asleep || b.asleep) continue
                if (kotlin.math.abs(a.x - b.x) > worldW * 0.15f) continue
                val bond = st.npcBonds[eng.bondKey(a.name, b.name)] ?: 0
                val rivals = relationNote(a.name, b.name) == "arch-rivals"
                val mx = (a.x + b.x) / 2f
                val my = minOf(a.baseY - a.fh, b.baseY - b.fh)
                if (bond >= 4) {
                    val bob = kotlin.math.sin(millis2 * 0.003f + i) * 3f
                    glyph("💗", mx, my - 16 + bob, 15f)
                    pairGlyphs++
                } else if (bond <= -3 || rivals) {
                    c.drawOval(RectF(mx - 20, my - 34, mx + 20, my - 18),
                        Paper.fillPaint("#2c2c34", 215))
                    glyph("⚡", mx, my - 8, 13f, "#f2c230")
                    pairGlyphs++
                }
            }
        }
        // ---- v0.4.0e: the greeting-bark bubble — one hello,
        // paper-styled, above the barker's head for ~2 s, drawn
        // last in the figure stack. Geometry is Barks.bubbleRect
        // (suite-pinned): inside the view, and never up into
        // the top chrome — the no-overlap discipline the rest of
        // the overlay lives by.
        val bName = barkName
        if (bName != null && System.currentTimeMillis() <= barkUntilMs) {
            val bp = posList.firstOrNull { it.name == bName && !it.asleep }
            if (bp != null) {
                var tSize = 13f * fs
                val tp = Paper.textPaint(Chrome.INK, tSize, bold = true, casual = true)
                tp.textAlign = Paint.Align.CENTER
                val textW = tp.measureText(barkText)
                val rect = Barks.bubbleRect(bp.x, bp.baseY - bp.fh, textW,
                    worldW, chromeTop(), fs)
                val innerW = rect.width - 2 * Barks.BUBBLE_PAD_X * fs
                if (textW > innerW && textW > 0f) {
                    tSize = (tSize * innerW / textW).coerceAtLeast(8f)
                    tp.textSize = tSize
                }
                val br = RectF(rect.left, rect.top, rect.right, rect.bottom)
                // the tail: a little paper point down toward the head
                val tailCx = bp.x.coerceIn(rect.left + 14f, rect.right - 14f)
                val tail = Path()
                tail.moveTo(tailCx - 6f, rect.bottom - 1f)
                tail.lineTo(tailCx + 6f, rect.bottom - 1f)
                tail.lineTo(bp.x, rect.bottom + Barks.BUBBLE_GAP * 0.9f)
                tail.close()
                c.drawPath(tail, Paper.fillPaint(Chrome.PARCHMENT))
                c.drawRoundRect(br, 9f * fs, 9f * fs, Paper.fillPaint(Chrome.PARCHMENT))
                c.drawRoundRect(br, 9f * fs, 9f * fs,
                    Paper.strokePaint(Chrome.PARCHMENT_EDGE, 2f))
                c.drawText(barkText, (rect.left + rect.right) / 2f,
                    rect.top + rect.height / 2f + tSize * 0.36f, tp)
            }
        }
    }


    private fun drawEntryDoor(c: Canvas, loc: Location, worldW: Float, h: Float) {
        val def = interiorOf(loc.id) ?: return
        val pulse = 120 + (60 * kotlin.math.sin(tick * 0.3f)).toInt()
        // v0.15.0g: the door rect composes against the
        // scene's unit width, exactly as the facade
        // painter composed it — one rect, still
        val unitW = SceneScale.unitW(worldW, loc, false)
        if (loc.kind in CharacterSystem.BUILDING_KINDS) {
            // v0.15.0d: the pulse hugs the very door
            // SceneArt painted on the facade — FacadeGeom's
            // one rect, filled there, outlined + hit here
            val g = FacadeGeom.doorRect(loc.kind, worldW, h, unitW)
            val dr = RectF(g.left, g.top, g.right, g.bottom)
            c.drawRect(dr, Paper.strokePaint(Paper.YELLOW, 3.5f,
                pulse.coerceIn(50, 210)))
            // #29: a keeper-run place shows its hours on the door
            // (v0.16.0b: the board composes at unit width like the
            // door it hangs on — at world width it was a billboard)
            val stC = state
            if (stC != null && engine?.shopClosedReason(stC, loc.id) != null) {
                Paper.drawSign(c, dr.centerX(), dr.top - h * 0.030f,
                    unitW * 0.22f, h * 0.032f,
                    "🔒 CLOSED", h * 0.015f)
            }
            hits.add(Hit("door", loc.id,
                RectF(dr.left - 16, dr.top - 16, dr.right + 16, dr.bottom + 10)))
        } else {
            // v0.15.0d: outdoor / hub — the entry is the
            // STRUCTURE's door, painted by SceneArt's
            // drawStructure on the structure's own front
            // (shelter, pavilion, hut, barn, loft, diner).
            // The v0.15.0c free-standing door slab that
            // stood alone in these scenes is REMOVED.
            val structure = TownBaseline.of(loc.id)?.structure ?: "hut"
            val g = FacadeGeom.structureDoorRect(structure, worldW, h, unitW)
            val dr = RectF(g.left, g.top, g.right, g.bottom)
            if (structure in setOf("shelter", "pavilion")) {
                // v0.16.0b: an open front has no door leaf to
                // outline — pulsing the whole framed opening
                // painted a giant gold rectangle floating over
                // the shelter's glass (Lucky's playtest shot).
                // The affordance is a THRESHOLD instead: a gold
                // step-line along the opening's base with two
                // short posts' feet. The hit rect is unchanged —
                // the whole opening still taps as the door.
                val pp = Paper.strokePaint(Paper.YELLOW, 3.5f,
                    pulse.coerceIn(50, 210))
                c.drawLine(dr.left, dr.bottom, dr.right, dr.bottom, pp)
                c.drawLine(dr.left, dr.bottom, dr.left, dr.bottom - h * 0.018f, pp)
                c.drawLine(dr.right, dr.bottom, dr.right, dr.bottom - h * 0.018f, pp)
            } else {
                c.drawRect(dr, Paper.strokePaint(Paper.YELLOW, 3f,
                    pulse.coerceIn(50, 210)))
            }
            hits.add(Hit("door", loc.id,
                RectF(dr.left - 14, dr.top - 14, dr.right + 14, dr.bottom + 10)))
        }
    }

    private fun drawSigns(c: Canvas, loc: Location, worldW: Float, h: Float) {
        val exits = EXITS[loc.id] ?: return
        val n = exits.size
        val boardW = minOf(worldW * 0.145f, worldW * 0.96f / n) * 0.92f
        // v0.5.0a: the live trip's next stop, computed once —
        // its signpost wears the route hint below
        val walkNext = state?.let { Walk.nextStop(it) }
        val walkDestName = state?.walkDest?.let { LOCATIONS[it]?.name }
        exits.forEachIndexed { i, id ->
            val dest = LOCATIONS[id] ?: return@forEachIndexed
            val rowB = i % 2 == 1
            val sx = worldW * (0.03f + 0.94f * (i + 0.5f) / n)
            val baseY = if (rowB) h * 0.985f else h * 0.925f
            val boardH = h * 0.036f
            val topY = baseY - boardH - h * 0.028f
            // post
            c.drawRect(sx - 3f, topY + boardH, sx + 3f, baseY, Paper.fillPaint("#5a4028"))
            // board
            Paper.crooked(c, if (i % 3 == 0) 1.2f else -1f, sx, topY + boardH / 2) {
                c.drawRoundRect(RectF(sx - boardW / 2, topY, sx + boardW / 2, topY + boardH),
                    6f, 6f, Paper.fillPaint("#7a5a36"))
                c.drawRoundRect(RectF(sx - boardW / 2, topY, sx + boardW / 2, topY + boardH),
                    6f, 6f, Paper.strokePaint("#4a3420", 2f))
            }
            val lp = Paper.textPaint("#f7ecc0",
                if (boardW > 120f) 13f else 10.5f, bold = true, casual = true)
            lp.textAlign = Paint.Align.CENTER
            var label = dest.name
            while (lp.measureText(label) > boardW - 8 && label.length > 4) {
                label = label.dropLast(2)
            }
            if (label != dest.name) label = label.trimEnd() + "…"
            c.drawText(label, sx, topY + boardH * 0.68f, lp)
            // #7 SIGNPOSTS KNOW THINGS: a live hint under each board —
            // the event is HERE, the place is closed for the night,
            // or how many people are actually there right now.
            val st = state
            val eng = engine
            if (st != null && eng != null) {
                val hint = when {
                    Quests.activeDef(st)?.let {
                        Shenanigans.stageOf(it, st.timeMin).clusterLoc
                    } == id -> "🎪 event here!"
                    // v0.5.0a: on a walking trip, the next leg's
                    // signpost says so — the route reads at a glance
                    walkNext == id ->
                        "🚶 next ▸ ${walkDestName ?: ""}"
                    eng.shopClosedReason(st, id) != null -> "🔒 closed"
                    else -> {
                        val wk = (st.gameDay + (st.gameMonth - 1) * 31) / 7
                        val heads = st.agents.values.count {
                            !it.isPlayer && it.location == id && it.waypoints.isEmpty()
                        } + VisitorSystem.visitorsFor(st.todayKey, st.timeMin)
                            .count { it.locId == id } +
                            VisitorSystem.residentsNow(st.timeMin, wk,
                                isSleepHour(st.timeMin / 60), dayOfWeek(st), st.todayKey)
                                .count { it.locId == id }
                        if (heads > 0) "👥 $heads here" else ""
                    }
                }
                if (hint.isNotEmpty()) {
                    val hp = Paper.textPaint("#2a1a0a", 10.5f, bold = true, casual = true)
                    hp.textAlign = Paint.Align.CENTER
                    c.drawText(hint, sx, baseY + 13f, hp)
                }
            }
            hits.add(Hit("exit", id, RectF(sx - boardW / 2 - 6, topY - 8,
                sx + boardW / 2 + 6, baseY + 4)))
        }
    }

    private fun drawEventStrip(c: Canvas, w: Float, st: GameState, fs: Float = 1f) {
        val def = Quests.activeDef(st) ?: return
        val stage = Shenanigans.stageOf(def, st.timeMin)
        val y0 = chromeTop()
        // v0.1.9b: themed paper band — flat theme fill, a darker
        // same-hue edge line along the bottom; fit via Chrome.fitText.
        // v0.1.9f: strip height/baseline come from Chrome's scaled
        // variants — at font scale 1.0 they are the shipped numbers.
        val stripH = Chrome.eventStripHeight(fs)
        c.drawRect(0f, y0, w, y0 + stripH,
            Paper.fillPaint(def.theme.colorHex, 215))
        c.drawRect(0f, y0 + stripH - 3f, w,
            y0 + stripH, Paper.fillPaint(Paper.darker(def.theme.colorHex)))
        val tp = Paper.textPaint("#1a1a1a", 13f * fs, bold = true, casual = true)
        tp.textAlign = Paint.Align.CENTER
        val txt = Chrome.fitText("${def.theme.icon} ${def.title} — ${stage.title}",
            w - 20) { tp.measureText(it) }
        c.drawText(txt, w / 2, y0 + Chrome.eventStripTextDy(fs), tp)
    }

    private fun drawBubbleOverlay(c: Canvas, w: Float, h: Float, st: GameState, fs: Float = 1f) {
        val convo = st.lastConversation
        if (convo.isEmpty()) return
        val show = convo.takeLast(2)
        // v0.1.9b: geometry from Chrome (identical numbers to the
        // shipped layout — the bubble still stacks below the event
        // strip with the same gap; the suite pins the offsets).
        // v0.1.9f: the scaled Chrome variants — at font scale 1.0
        // every number below is the shipped one.
        val gs = Chrome.geoScale(fs)
        val top = Chrome.bubbleTop(chromeTop(), st.shenaniganId != null, fs)
        val bw = w - Chrome.BUBBLE_MARGIN_PX * 2
        val bh = Chrome.bubbleHeight(show.size, fs)
        val rect = RectF(Chrome.BUBBLE_MARGIN_PX, top,
            Chrome.BUBBLE_MARGIN_PX + bw, top + bh)
        c.drawRoundRect(RectF(rect.left + 3, rect.top + 4, rect.right + 3, rect.bottom + 4),
            14f, 14f, Paper.fillPaint("#000000", 80))
        c.drawRoundRect(rect, 14f, 14f, Paper.fillPaint(Chrome.PAPER_WHITE))
        c.drawRoundRect(rect, 14f, 14f, Paper.strokePaint("#000000", 3f))
        val tail = Path().apply {
            moveTo(rect.left + 60, rect.bottom - 2)
            lineTo(rect.left + 80, rect.bottom + 22)
            lineTo(rect.left + 100, rect.bottom - 2)
            close()
        }
        c.drawPath(tail, Paper.fillPaint(Chrome.PAPER_WHITE))
        c.drawPath(tail, Paper.strokePaint("#000000", 3f))
        var y = top + Chrome.bubbleTopPad(fs)
        for (line in show) {
            val color = when {
                line.speaker == "🌲" -> "#5a5a5a"
                st.agents.containsKey(line.speaker) -> st.agents[line.speaker]?.color ?: "#1a1a1a"
                else -> VisitorSystem.personaByName(line.speaker)?.color ?: "#5a4a7a"
            }
            val np = Paper.textPaint(color, 14f * fs, bold = true, casual = true)
            val tp = Paper.textPaint("#1a1a1a", 14f * fs)
            val label = "${line.speaker}: "
            c.drawText(label, rect.left + 16, y, np)
            // the games' speaker treatment: a color bar under the name
            c.drawRect(rect.left + 16, y + 4 * gs, rect.left + 16 + np.measureText(label),
                y + 7 * gs, Paper.fillPaint(color))
            val lx = rect.left + 16 + np.measureText(label)
            val txt = Chrome.fitText(line.text,
                rect.width() - (lx - rect.left) - 16) { tp.measureText(it) }
            c.drawText(txt, lx, y, tp)
            y += Chrome.bubbleLineStep(fs)
        }
        bubbleRect = rect
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x; downY = ev.y; lastX = ev.x
                dragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dxTotal = ev.x - downX
                if (!dragging && abs(dxTotal) > 26f) dragging = true
                if (dragging) {
                    camTargetX -= (ev.x - lastX)
                    // v0.15.0g: the drag clamp rides the
                    // live world's own width (SceneScale's
                    // factor made it 2–3× the screen)
                    val maxCam = (worldWCache - width).coerceAtLeast(0f)
                    camTargetX = camTargetX.coerceIn(0f, maxCam)
                    lastX = ev.x
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    dragging = false
                    return true
                }
                val x = ev.x
                val y = ev.y
                bubbleRect?.let {
                    if (it.contains(x, y)) {
                        onBubbleTap()
                        return true
                    }
                }
                val wx = x + camX
                val locNow0 = state?.let { s ->
                    engine?.player(s)?.location
                }?.let { LOCATIONS[it] }
                val insideNow0 = state?.playerInside == true
                val ww = if (worldWCache > 0f) worldWCache
                    else width * (locNow0?.let {
                        SceneScale.factor(it, insideNow0)
                    } ?: SceneScale.OLD_FACTOR)
                val insideNow = insideNow0
                // v0.15.0c: taps resolve through PaperTap over
                // the frame's hit list (figures by nearest
                // center — the crowd rule — everything else
                // topmost), and the result is a WALK ORDER:
                // the kid walks to the thing and its
                // interaction fires on arrival (firePending),
                // through the very callbacks the old instant
                // tap used. A tap on the thing you're already
                // standing at arrives on the walk's first
                // step, so nothing feels taken away.
                val items = hits.map { hit ->
                    PaperTap.Item(hit.kind, hit.id,
                        hit.rect.centerX(), hit.rect.centerY(),
                        hit.rect.width(), hit.rect.height(),
                        hit.rect.bottom)
                }
                val hit = PaperTap.resolve(items, wx, y)
                // v0.15.0d: prop footprints block the ground
                // plane — a walk target that lands inside a
                // standing prop's footprint slides to its
                // near edge (you walk up BESIDE the tree,
                // the bench, the sign — never into them)
                fun avoidP(x: Float, band: Float): Pair<Float, Float> =
                    if (insideNow || locNow0 == null) x to band
                    else WalkBand.avoid(x, band,
                        PaperDressing.footprintsFor(locNow0))
                if (hit != null) {
                    var txPx = hit.cx
                    if (hit.kind == "kid" || hit.kind == "visitor") {
                        // stand beside them, on your own side
                        val off = hit.w / 2f + ww * 0.016f
                        txPx = if (pwalk.x * ww < hit.cx)
                            hit.cx - off else hit.cx + off
                    }
                    // v0.15.0g: a THING's standing spot is
                    // never on the road — things stand on the
                    // sidewalk and the floors (the bus
                    // included: you board from the curb, as
                    // always), so a hit's band clamps to the
                    // walkable band proper even though its
                    // rect's baseline dips into the road band
                    val (ax, ab) = avoidP(txPx / ww,
                        bandForY(hit.baseline, insideNow)
                            .coerceAtMost(1f))
                    pwalk = PaperWalk.tapTarget(pwalk, hit.kind, hit.id, ax, ab)
                    busBeatKind = null // a new order cancels a pull-up
                } else if (y > height * 0.55f) {
                    // tap open ground: the kid walks there
                    // v0.15.0g: THE ROAD RULE — outdoors, a
                    // ground tap that lands on the road
                    // (band past 1) is legal ONLY inside a
                    // marked crosswalk (PaperDressing's
                    // spans — the same table SceneArt
                    // zebra-stripes); anywhere else on the
                    // road the target clamps back to band 1,
                    // the sidewalk's edge, at the tapped x.
                    var gBand = bandForY(y, insideNow)
                    if (!insideNow && locNow0 != null &&
                        WalkBand.isRoad(gBand) &&
                        !WalkBand.inSpan(wx / ww,
                            PaperDressing.crosswalks(locNow0))) {
                        gBand = 1f
                    }
                    val (ax, ab0) = avoidP(wx / ww, gBand)
                    val ab = if (insideNow) ab0.coerceAtMost(1f) else ab0
                    pwalk = PaperWalk.tapGround(pwalk, ax, ab)
                    busBeatKind = null
                }
                invalidate()
                return true
            }
        }
        return true
    }
}

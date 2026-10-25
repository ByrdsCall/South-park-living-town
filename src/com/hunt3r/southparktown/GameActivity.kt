package com.hunt3r.southparktown

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// ============================================================
// GameActivity — hosts the POV GameView full-screen.
// Top bar: location / time / weather. Bottom bar: Look around,
// Talk, Who, Journal, Menu. Ambient tick every 30s + on every
// player action. Auto-save on pause.
// ============================================================
class GameActivity : Activity() {

    companion object {
        /** v0.5.0e: the phone's activity-result channel (its
         *  🗺 Map tile routes back to the picker here). */
        const val REQ_PHONE = 4105
    }

    private lateinit var engine: Engine
    private lateinit var state: GameState
    private lateinit var prefs: Prefs
    private val clock = SystemClock()
    private val bg = Executors.newSingleThreadExecutor()
    private val uiHandler = Handler(Looper.getMainLooper())

    // ---- v0.1.5 clock: one full 24h in-game cycle = 40 real minutes
    // (12 daytime hours in 20 min, 12 nighttime hours in 20 min;
    // 1 in-game hour ≈ 100 real seconds = 0.6 game-min per real second).
    // The 8 AM start is unchanged; real-time sync stays opt-in/off.
    private var lastTickMs = 0L
    private var gameMinAcc = 0.0

    private fun tickWorld() {
        if (prefs.isRealtimeSync()) {
            engine.ambientTick(state, clock)
            return
        }
        val now = System.currentTimeMillis()
        if (lastTickMs == 0L) lastTickMs = now
        val elapsedSec = (now - lastTickMs) / 1000.0
        lastTickMs = now
        gameMinAcc += elapsedSec * 0.6
        var steps = (gameMinAcc / 15).toInt()
        if (steps > 8) { steps = 8; gameMinAcc = 0.0 } // catch-up cap
        else gameMinAcc -= steps * 15
        repeat(steps) { engine.inGameTick(state) }
        if (steps > 0) visitorAmbient()
    }

    /** Player actions also nudge time forward a little (play-driven clock). */
    private fun actionTick() {
        gameMinAcc += 5.0
        tickWorld()
    }

    /** Generated NPCs litter far more than cast (Lucky's spec) and
     *  residents/visitors occasionally drop things where they stand. */
    private fun visitorAmbient() {
        val week = (state.gameDay + (state.gameMonth - 1) * 31) / 7
        val everyone = VisitorSystem.visitorsFor(state.todayKey, state.timeMin) +
            VisitorSystem.residentsNow(state.timeMin, week,
                isSleepHour(state.timeMin / 60), dayOfWeek(state), state.todayKey)
        val rnd = java.util.Random()
        for (a in everyone) {
            if (rnd.nextFloat() < 0.05f) {
                engine.dropLitter(state, a.locId, a.inside,
                    LITTER_ITEM_IDS[rnd.nextInt(LITTER_ITEM_IDS.size)], a.persona.loreName)
            }
        }
    }

    private fun currentSeason(): Season =
        if (prefs.isRealtimeSync()) seasonFor(clock.month()) else seasonFor(state.gameMonth)

    // ---------------- v0.4.0e: greeting barks ----------------
    // In-memory only (garnish, never saved): who barked when,
    // which space the player was last seen in, and who was in
    // it — the difference between an ENTRY (you walked in;
    // somebody hails you) and an ARRIVAL (you lingered; a
    // newcomer sometimes does).
    private val barkCooldowns = mutableMapOf<String, Int>()
    private var barkSpaceKey: String? = null
    private var barkPresentBefore: Set<String> = emptySet()
    // v0.13.0a: the family's own presence memory for home
    // barks (who of Mom/Dad was home at the last consult)
    private var barkFamBefore: Set<String> = emptySet()

    /** Called from refreshUi (UI thread, after every world
     *  change and every 10 s tick). All the RULES live in
     *  Barks (pure, suite-pinned); this only wires them to
     *  the world: at most one bark per space entry, a rare
     *  one per arrival, never a sleeper, never on cooldown,
     *  weighted toward your highest-relationship present. */
    private fun maybeBark() {
        try {
            val me = engine.player(state)
            val spaceKey = me.location + "|" + state.playerInside
            val present = engine.presentWithPlayer(state)
                .filter { !it.asleep && Barks.patternFor(it.name) != null }
            val presentNames = present.map { it.name }.toSet()
            val entered = spaceKey != barkSpaceKey
            val newcomers = presentNames - barkPresentBefore
            barkSpaceKey = spaceKey
            barkPresentBefore = presentNames
            val nowAbsMin = state.absDay * 1440 + state.timeMin
            // v0.4.0g: the decision itself is Barks.decide
            // (pure — the suite drives it over simulated
            // years of entries); this shell owns the
            // cooldown map, the rolls, the sound, the bubble.
            val pick = Barks.decide(prefs.barksEnabled(), state.timeMin,
                entered,
                present.map { it.name to (state.friendship[it.name] ?: 0) },
                newcomers, barkCooldowns, nowAbsMin,
                kotlin.random.Random.nextDouble(),
                kotlin.random.Random.nextDouble())
            if (pick != null) {
                barkCooldowns[pick] = nowAbsMin
                val pat = Barks.patternFor(pick) ?: return
                SoundEngine.bark(pick,
                    Barks.volumeScale(state.playerInside, state.timeMin))
                gameView.showBark(pick, pat.hello, Barks.BUBBLE_MS)
                return
            }
            // v0.13.0a: no cast bark — at home, your folks
            // hail you too. The rules live in Family.barkDecide
            // (pure, suite-pinned); this only wires presence:
            // parents count only indoors at Your House, the
            // one room they stand in. The cooldown map is the
            // cast's own ("Mom"/"Dad" can't collide with a
            // cast name — the census keeps them out of it).
            if (state.playerInside && me.location == YOUR_HOUSE) {
                val famPresent = Family.presentAt(state, YOUR_HOUSE,
                    true).map { it.persona.loreName }
                val famNew = famPresent.toSet() - barkFamBefore
                barkFamBefore = famPresent.toSet()
                val fb = Family.barkDecide(state,
                    prefs.barksEnabled(), famPresent, entered,
                    famNew, barkCooldowns, nowAbsMin,
                    kotlin.random.Random.nextDouble(),
                    kotlin.random.Random.nextDouble(),
                    kotlin.random.Random.nextDouble(),
                    nowAbsMin) ?: return
                barkCooldowns[fb.first] = nowAbsMin
                SoundEngine.bark(fb.first,
                    Barks.volumeScale(true, state.timeMin))
                gameView.showBark(fb.first, fb.second, Barks.BUBBLE_MS)
            } else barkFamBefore = emptySet()
        } catch (e: Exception) { /* garnish — a bark must never break the town */ }
    }

    private lateinit var gameView: GameView
    private lateinit var locLabel: TextView
    private lateinit var timeLabel: TextView
    private lateinit var weatherLabel: TextView
    private lateinit var questChip: TextView
    private lateinit var tickerLabel: TextView

    private val tickRunnable = object : Runnable {
        override fun run() {
            bg.execute {
                try {
                    tickWorld()
                    prefs.saveGame(state)
                    prefs.saveNpcMemory()
                    runOnUiThread { refreshUi() }
                } catch (e: Exception) {
                    // tick must never kill the app
                }
            }
            uiHandler.postDelayed(this, 10000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureLoreLoaded(this)
        prefs = Prefs(AndroidSaveStore(this))
        engine = Engine()
        // v0.2.0b: synthesized sound (SoundGen renders, SoundEngine
        // plays; rendering warms up on its own background thread)
        SoundEngine.init(this)
        SoundEngine.applySettings(prefs.audioSettings())
        engine.onStageShift = { SoundEngine.stageCue() }
        // v0.2.0g: a weather turn re-feeds the ambience/music
        // beds the moment it happens (inside advanceTime, on the
        // bg thread) instead of lagging to the next UI refresh.
        engine.onWeatherShift = {
            runOnUiThread {
                SoundEngine.updateWorld(state.weatherId, state.timeMin,
                    state.playerInside,
                    SkyState.auroraNow(state.absDay, state.timeMin,
                        state.weatherId, currentSeason()))
            }
        }

        val loaded = prefs.loadGame()
        val pName = prefs.getPlayerName() ?: PLAYER_DEFAULT_NAME
        val pApp = prefs.getAppearance()
        if (loaded == null) {
            state = engine.newState()
            engine.createPlayer(state, pName, pApp)
        } else {
            state = loaded
            // keep name/appearance in sync with prefs, no event spam
            val me = engine.player(state)
            me.name = pName
            me.appearance = pApp
        }
        // v0.1.9c: a save from before episodes existed can be
        // mid-event — synthesize its record with cards pre-shown
        // so nothing re-triggers (see Episodes.syncAiring).
        Episodes.syncAiring(state)
        // v0.5.0a: a save that earned Town Fixture before the
        // bus pass existed is owed one — Walk.ensureFeatPass
        // grants it retroactively (a ticker line says so).
        Walk.ensureFeatPass(state)
        // v0.5.0b: the wardrobe's retroactive pass — a save
        // already past a feat, a holiday beat, a case
        // retirement or a milestone is owed its pieces on load
        // (Wardrobe.syncUnlocks grants each with its ticker +
        // town-log line; in-session crossings re-sync in
        // refreshUi via syncWardrobe).
        Wardrobe.syncUnlocks(state)
        wireDialogueProvider()
        // v0.1.5: per-NPC character memory + witnessed-chatter hooks.
        prefs.loadNpcMemory()
        // v0.1.8: generated citizens read the live town — befriended
        // visitors return sooner and stay longer (#19), visitor crowds
        // tilt toward rising factions (#36), and visitors who move in
        // take up real residency (#18).
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
        engine.onWitnessedChat = { a, b ->
            for ((x, y) in listOf(a to b, b to a)) {
                val mem = NpcMemoryStore.getOrCreate(x)
                val tag = "chatting with $y"
                mem.topics.remove(tag)
                mem.topics.add(tag)
                while (mem.topics.size > 6) mem.topics.removeAt(0)
            }
        }
        bg.execute {
            if (prefs.isRealtimeSync()) engine.syncToDeviceTime(state, clock)
            prefs.saveGame(state)
            runOnUiThread { refreshUi() }
        }

        buildLayout()
        uiHandler.post { maybeShowEpisodeCards() }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun buildLayout() {
        val root = FrameLayout(this)

        gameView = GameView(this).apply {
            engine = this@GameActivity.engine
        }
        gameView.onTalkKid = { onTalkKid(it) }
        gameView.onTalkVisitor = { onTalkVisitor(it) }
        gameView.onExit = { onExit(it) }
        gameView.onCollect = { onCollect(it) }
        gameView.onEnterInterior = { onEnterInterior() }
        gameView.onExitInterior = { onExitInterior() }
        gameView.onSearchTrash = { onSearchTrash(it) }
        gameView.onPickupLitter = { onPickupLitter(it) }
        gameView.onBubbleTap = { openSaySheet() }
        gameView.onSceneTap = { kind, id -> onSceneTap(kind, id) }
        gameView.onSighting = { onSighting333() }
        gameView.onBusStation = { onBusStation(it) }
        gameView.onSchoolBus = { onSchoolBus() }
        gameView.onHouseDoor = { onHouseDoor(it) }
        gameView.onSectionDoor = { onSectionDoor(it) }
        gameView.onLivingFurniture = { onLivingFurniture(it) }
        gameView.onFictionDoor = { onFictionDoor(it) }
        gameView.onBusRide = { completeBusRide() }
        root.addView(gameView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // ---- quest chip (dynamic appearance: reskins to the event theme) ----
        // v0.1.9b: a themed paper plaque (rounded cardstock, darker
        // edge) instead of a flat color fill — position/size/ellipsis
        // unchanged, its bottom edge still defines GameView's chromeTop.
        questChip = TextView(this).apply {
            textSize = 12.5f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTextColor(Color.parseColor("#1a1a1a"))
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = UiKit.plaqueBg(this@GameActivity, Paper.PARCHMENT)
            setOnClickListener {
                SoundEngine.select()
                startActivity(Intent(this@GameActivity, QuestActivity::class.java))
            }
        }
        val chipLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        chipLp.topMargin = dp(92)
        chipLp.leftMargin = dp(8)
        root.addView(questChip, chipLp)

        // ---- ticker: latest town event, in the chrome (not in-view) ----
        // v0.1.9b: a parchment news strip with ink text (was a black
        // translucent band) — same slot, same ellipsis direction.
        tickerLabel = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.parseColor(Chrome.INK))
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.START
            background = UiKit.panelBg(this@GameActivity, Paper.PARCHMENT, 0f)
            setPadding(dp(8), dp(2), dp(8), dp(2))
        }
        val tickerLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tickerLp.topMargin = dp(4)
        root.addView(tickerLabel, tickerLp)

        // ---- top bar (below the ticker) ----
        // v0.1.9b: translucent parchment band with a cardstock edge
        // (was translucent black) — margins/padding identical, so
        // the measured chrome stacking (ticker 4dp / bar 32dp /
        // chip 92dp) is untouched. Location red, clock ink now
        // (yellow doesn't read on parchment).
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = UiKit.panelBg(this@GameActivity, Paper.PARCHMENT, 0f,
                alpha = 225)
            setPadding(dp(10), dp(4), dp(10), dp(4))
        }
        locLabel = TextView(this).apply {
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 17f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
        }
        timeLabel = TextView(this).apply {
            setTextColor(Color.parseColor(Chrome.INK))
            textSize = 14f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        weatherLabel = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER_VERTICAL
        }
        topBar.addView(locLabel, LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        topBar.addView(timeLabel, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        topBar.addView(weatherLabel, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val topLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        topLp.topMargin = dp(32)
        root.addView(topBar, topLp)

        // ---- bottom bar ----
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
            // v0.15.0g: side padding slimmed to dp(2) —
            // every dp of the bar belongs to the seven
            // labels now (BarFit's math reads these numbers)
            setPadding(dp(2), dp(6), dp(2), dp(6))
        }
        fun barButton(label: String, onClick: () -> Unit): Button {
            return Button(this).apply {
                text = label
                // v0.15.0g: ONE LINE, ALWAYS (Lucky's
                // playtest screenshots: SELFI-E, JOUR-NAL,
                // PHON-E, MEN-U wrapping mid-word). The
                // label never wraps: single-line is set,
                // and the text AUTO-SIZES down from 14sp to
                // an 8sp floor until it fits the button's
                // weighted share — the seven buttons keep
                // their names and their places; the type
                // breathes instead of breaking. Horizontal
                // padding and min width are zeroed so the
                // full share belongs to the label (Button's
                // stock padding was eating a third of it).
                setSingleLine(true)
                maxLines = 1
                setAutoSizeTextTypeUniformWithConfiguration(
                    BarFit.MIN_SP, BarFit.MAX_SP, 1,
                    android.util.TypedValue.COMPLEX_UNIT_SP)
                minWidth = 0
                minimumWidth = 0
                setPadding(0, dp(6), 0, dp(6))
                setTextColor(Color.parseColor(Chrome.INK))
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                // v0.1.9b: chunky parchment game buttons on the
                // charcoal bar (was flat dark boxes, yellow text)
                background = UiKit.buttonBg(this@GameActivity, Paper.PARCHMENT)
                setOnClickListener { SoundEngine.tap(); onClick() }
            }
        }
        val buttons = listOf(
            "👀 Look" to { onLook() },
            "💬 Talk" to { onTalkButton() },
            "📸 Selfie" to { onSelfie() },
            "🎒 Pack" to { openInventory() },
            "📓 Journal" to { openJournal() },
            "📱 Phone" to { openPhone() },
            "☰ Menu" to { openMenu() }
        )
        for ((label, fn) in buttons) {
            bottomBar.addView(barButton(label, fn), LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(1); marginEnd = dp(1)
            })
        }
        val botLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        botLp.gravity = Gravity.BOTTOM
        root.addView(bottomBar, botLp)

        setContentView(root)
        // disjoint layout regions: GameView's event strip + speech
        // bubble stack below the quest chip, never over the title
        root.post {
            if (questChip.bottom > 0) gameView.topChromePx = questChip.bottom.toFloat()
        }
    }

    // ---------------- dialogue provider: LLM or mock ----------------
    private fun wireDialogueProvider() {
        if (prefs.activeLlmConfig() == null) return // default mock brain stays
        engine.dialogueProvider = { kind, a, b, locName, playerLine ->
            llmOrMock(kind, a, b, locName, playerLine)
        }
    }

    private fun llmOrMock(kind: String, a: Agent, b: Agent?, locName: String,
                          playerLine: String?): List<Line> {
        // Runs on the bg executor — blocking network is fine here.
        val st = state
        val config = prefs.activeLlmConfig()
        val presentForMock: List<Agent>
        val prompt: String
        val validNames: List<String>
        val timeStr = minutesToTimeStr(st.timeMin)
        val weather = engine.weatherDesc(st)
        when (kind) {
            "collision" -> {
                val other = b ?: return engine.mockBatch(st, kind, a, b, locName, playerLine, emptyList())
                val locId = LOCATIONS.entries.firstOrNull { it.value.name == locName }?.key
                val critter = if (locId != null) engine.animalFlavor(st, locId) else ""
                prompt = engine.buildCollisionPrompt(a, other, locName, timeStr, weather, critter, st)
                validNames = listOf(a.name, other.name)
                presentForMock = emptyList()
            }
            "playerSay" -> {
                val present = engine.presentWithPlayer(st).filter { !it.asleep }
                if (present.isEmpty()) return engine.mockBatch(st, kind, a, b, locName, playerLine, present)
                prompt = engine.buildPlayerPrompt(st, present, playerLine ?: "", locName, timeStr, weather)
                validNames = present.map { it.name }
                presentForMock = present
            }
            "playerTalk" -> {
                prompt = engine.buildPlayerPrompt(st, listOf(a), playerLine ?: "", locName, timeStr, weather)
                validNames = listOf(a.name)
                presentForMock = emptyList()
            }
            else -> return emptyList()
        }
        if (config == null) return engine.mockBatch(st, kind, a, b, locName, playerLine, presentForMock)
        // Lorebook: ground the LLM in canon facts for who's talking + where.
        val loreNames = when (kind) {
            "collision" -> listOfNotNull(a.name, b?.name)
            "playerTalk" -> listOf(a.name)
            else -> validNames
        }
        val loreLocId = LOCATIONS.entries.firstOrNull { it.value.name == locName }?.key
        // v0.1.5: character memory + active shenanigans context join the prompt.
        val me = engine.player(st)
        val memoryBlock = when (kind) {
            "playerTalk" -> NpcMemoryStore.promptBlock(a.name, me.name)
            "playerSay" -> validNames.take(3).joinToString("") {
                NpcMemoryStore.promptBlock(it, me.name)
            }
            else -> ""
        }
        val eventBlock = Quests.activeDef(st)?.let { def ->
            val stage = Shenanigans.stageOf(def, st.timeMin)
            "\nCURRENT TOWN EVENT: '${def.title}' — ${stage.journalNote} " +
                "Everyone's behavior and talk should reflect it."
        } ?: ""
        val lines = llmLines(prompt + LoreBook.promptSuffix(loreNames, loreLocId) +
            memoryBlock + eventBlock, validNames, config)
        return if (lines.isNotEmpty()) lines
        else engine.mockBatch(st, kind, a, b, locName, playerLine, presentForMock)
    }

    private fun llmLines(prompt: String, validNames: List<String>, config: LlmConfig): List<Line> {
        var result: String? = null
        val latch = CountDownLatch(1)
        LlmClient.generate(prompt, config, 20000) { r ->
            result = r
            latch.countDown()
        }
        return try {
            latch.await(25, TimeUnit.SECONDS)
            engine.parseLlmLines(result, validNames)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ---------------- UI refresh ----------------
    /** v0.5.0b: keep the wardrobe honest — a feat crossed, a
     *  beat played, a case retired since the last refresh
     *  grants its piece here, with the grant's own ticker +
     *  town-log lines (Wardrobe.grant) and a toast so the
     *  moment isn't missed. Idempotent; runs on refresh. */
    private fun syncWardrobe() {
        val new = try { Wardrobe.syncUnlocks(state) } catch (e: Exception) { emptyList() }
        if (new.isEmpty()) return
        bg.execute { prefs.saveGame(state) }
        runOnUiThread {
            toast("👕 WARDROBE — ${new.first().name} unlocked!" +
                if (new.size > 1) " (+${new.size - 1} more — see Collection 👕)" else
                    " (see it in the creator / Collection 👕)")
        }
    }

    private fun refreshUi() {
        // v0.16.0e pass 2: the Transition Director's
        // watch — the fiction's phase is a pure function
        // of the game clock, so this only NOTICES changes:
        // it announces them, and on RESIDUE it stamps the
        // capped carry-over list (the run's only derived-
        // state write, per the costume spec).
        if (Fiction.activeDef(state) != null) {
            val ph = Fiction.phaseOf(state)
            val last = state.life["fiction:lastphase"] ?: -1
            if (ph.ordinal != last) {
                state.life["fiction:lastphase"] = ph.ordinal
                if (ph == Fiction.Phase.RESIDUE)
                    Fiction.settleResidue(state)
                if (last >= 0) {
                    toast(when (ph) {
                        Fiction.Phase.LIVE ->
                            "🎭 The game is LIVE — the town " +
                                "is dressed."
                        Fiction.Phase.ESCALATING ->
                            "🎭 The game escalates — fresh " +
                                "supplies, deeper belief."
                        Fiction.Phase.COLLAPSING ->
                            "🎭 The game is coming down."
                        Fiction.Phase.RESIDUE ->
                            "🎭 The game is over. A few " +
                                "things never got cleaned up."
                        else -> "🎭 The kids are dressing " +
                            "the town…"
                    })
                }
                prefs.saveGame(state)
            }
        }
        syncWardrobe()
        if (questChip.bottom > 0) gameView.topChromePx = questChip.bottom.toFloat()
        tickerLabel.text = state.events.lastOrNull() ?: ""
        val me = try { engine.player(state) } catch (e: Exception) { return }
        val loc = LOCATIONS[me.location] ?: return
        locLabel.text = if (state.playerInside) {
            "🏠 ${interiorOf(me.location)?.name ?: loc.name}"
        } else loc.name
        timeLabel.text = "  ${minutesToTimeStr(state.timeMin)}"
        val w = WEATHER[state.weatherId]
        // #37 A FORECAST IN THE HUD: likely next condition rides along.
        val fc = forecastWeather(state)
        val fcTxt = if (fc != state.weatherId) " → ${WEATHER[fc]?.icon ?: ""}" else ""
        weatherLabel.text = " ${w?.icon ?: ""} ${w?.label?.substringBefore(" &") ?: ""}$fcTxt"
        weatherLabel.textSize = 12f
        val md = if (state.todayKey.length >= 8) state.todayKey.substring(4) else "0101"
        gameView.state = state
        gameView.season = currentSeason()
        gameView.holiday = displayHoliday(state)
        // v0.1.8 Phase 2: refresh the render context the living
        // world draws from (occupancy, bedtimes, sky, holdouts).
        try {
            TownInfo.todayKey = state.todayKey
            TownInfo.ordinal = state.absDay
            TownInfo.timeMin = state.timeMin
            TownInfo.weatherId = state.weatherId
            TownInfo.season = currentSeason()
            TownInfo.holidayId = state.holidayId
            TownInfo.occupiedHomes = HOUSE_LOCS.filter { house ->
                val owner = KID_HOUSE.entries.firstOrNull { it.value == house }?.key
                owner != null && state.agents[owner]?.location == house
            }.toSet() +
                // v0.3.0d: your house is occupied when YOU are
                // home — its windows light for its actual resident
                (if (me.location == YOUR_HOUSE) setOf(YOUR_HOUSE)
                else emptySet())
            // v0.3.0d: the decor your house has earned (the room
            // fills as you play; InteriorArt reads the tokens)
            TownInfo.houseDecor = House.decorTokensFor(state) +
                Sleepovers.dressingTokens(state)
            TownInfo.digTokens = Mystery3.digTokensByLoc(state)
            TownInfo.fixTokens = Mystery4.fixTokensByLoc(state)
            TownInfo.factionMarkTokens = FactionMissions.markTokensByLoc(state)
            // v0.8.0b: capstone traces ride the same live map
            // as the chain marks — one layer, both registries.
            TownInfo.factionMarkTokens =
                (TownInfo.factionMarkTokens.entries +
                    Capstones.markTokensByLoc(state).entries)
                    .groupBy({ it.key }, { it.value })
                    .mapValues { (_, v) -> v.flatten() }
            // v0.9.0c: the fair's dressing, live only while
            // fair week stands (Fair.fairTokensByLoc)
            TownInfo.fairTokens = Fair.fairTokensByLoc(state)
            // v0.11.0e: the winter carnival's dressing, live
            // only while carnival week stands
            // (Carnival.carnivalTokensByLoc)
            TownInfo.carnivalTokens = Carnival.carnivalTokensByLoc(state)
            TownInfo.bedtimeOf = HOUSE_LOCS.mapNotNull { house ->
                val owner = KID_HOUSE.entries.firstOrNull { it.value == house }?.key
                    ?: return@mapNotNull null
                val agent = state.agents[owner] ?: return@mapNotNull null
                val lastKey = agent.agendaBase.keys
                    .maxByOrNull { timeStrToMinutes(it) } ?: return@mapNotNull null
                house to (timeStrToMinutes(lastKey) + 45)
            }.toMap()
            TownInfo.closedShops = engine.KEEPERS.keys
                .filter { engine.shopClosedReason(state, it) != null }.toSet()
            TownInfo.tvHouses = TownInfo.occupiedHomes
                .filter { (it.hashCode() + state.absDay) % 3 == 0 }.toSet()
            // v0.3.0e: the composed sky (SkyState) — one call,
            // every render surface reads the same answer.
            val skyNow = SkyState.now(state.absDay, state.timeMin,
                state.weatherId, currentSeason())
            TownInfo.auroraTonight = skyNow.special == SkyState.AURORA
            TownInfo.skySpecial = skyNow.special
            TownInfo.moonlight = skyNow.moonlight
            TownInfo.harvestMoon = skyNow.harvest
            TownInfo.auroraIntensity = skyNow.auroraIntensity
            TownInfo.mudToday = state.life["mudDay"] == state.absDay
            TownInfo.freezeThaw = state.life["snowYesterday"] == state.absDay - 1 &&
                state.weatherId in setOf("sunny", "partly_cloudy", "cloudy")
            TownInfo.budLevel = if (currentSeason() == Season.SPRING) {
                ((state.gameDay + (state.gameMonth - 3).coerceAtLeast(0) * 31) / 45f)
                    .coerceIn(0f, 1f)
            } else 0f
            TownInfo.moonPhase = TownLife.moonPhase(state.absDay)
            val todayOrd = ordinalOfKey(state.todayKey)
            var bestHoldout: Holiday? = null
            var bestDiff = Int.MAX_VALUE
            for (hd in HOLIDAYS) {
                for (y in listOf(state.gameYear, state.gameYear - 1)) {
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
            TownInfo.curtainWarm = if (state.playerInside) {
                val owner = KID_HOUSE.entries.firstOrNull { it.value == me.location }?.key
                owner != null && Relationships.tierOf(
                    state.friendship[owner] ?: 0) >= Relationships.FRIEND
            } else false
        } catch (e: Exception) { /* render context is flavor */ }
        // generated lorebook NPCs sharing this exact space
        gameView.generated = generatedHere()
        // quest chip: dynamic theme from the active event (or Town Board)
        try {
            val theme = Quests.themeOf(state)
            // #8: the chip pulses — objective, mood, forecast, holiday
            questChip.text = Quests.chipText(state, (state.timeMin / 15) % 4)
            // v0.1.9b: reskin = a fresh plaque in the theme color
            questChip.background = UiKit.plaqueBg(this, theme.colorHex)
        } catch (e: Exception) { /* chip is flavor */ }
        // v0.2.0b: ambience + tune follow the world (change-only)
        // v0.3.0e: + the aurora hush flag from SkyState.
        SoundEngine.updateWorld(state.weatherId, state.timeMin, state.playerInside,
            SkyState.auroraNow(state.absDay, state.timeMin,
                state.weatherId, currentSeason()))
        gameView.refresh()
        // v0.4.0e: somebody here might hail you (all rules in Barks)
        maybeBark()
        // v0.8.0c: or cry for help — an emergency's asker,
        // in this space, with their moment still open,
        // shouts once (all rules in Emergencies.entryShout)
        maybeEmergencyShout()
        // v0.8.0e: or cross a finish line — a live rival
        // race settles the moment you stand at its target
        // (all rules in Rival.raceArrival; it answers once)
        maybeRivalRace()
    }

    /** v0.8.0e: the race's finish line, wired: Rival owns
     *  the clock and the settle-once; this shell only
     *  announces the result it hands back. (Called from
     *  inside refreshUi — the settle rides this same pass;
     *  no re-entry.) */
    private fun maybeRivalRace() {
        try {
            val me = engine.player(state)
            Rival.raceArrival(state, me.location)?.let {
                prefs.saveGame(state)
                toast(it)
            }
            // v0.9.0e: the crew relay's finish line works
            // the same way — your anchor leg ends standing
            // at the target (all rules in RivalCrew)
            RivalCrew.raceArrival(state, me.location)?.let {
                prefs.saveGame(state)
                toast(it)
            }
        } catch (e: Exception) { /* garnish — a race must never break the town */ }
    }

    /** v0.8.0c: the entry shout, wired: Emergencies owns
     *  the decision (once a day, the asker present, the
     *  offer really live); this shell only speaks it. The
     *  line rides the events ticker from inside the module;
     *  the toast is the shout reaching your ears. */
    private fun maybeEmergencyShout() {
        try {
            Emergencies.entryShout(state)?.let { toast(it) }
        } catch (e: Exception) { /* garnish — a shout must never break the town */ }
    }

    override fun onResume() {
        super.onResume()
        wireDialogueProvider() // key may have changed in Settings
        SoundEngine.startLoops()
        SoundEngine.updateWorld(state.weatherId, state.timeMin, state.playerInside,
            SkyState.auroraNow(state.absDay, state.timeMin,
                state.weatherId, currentSeason()))
        lastTickMs = 0L // don't let a long pause fast-forward the town
        uiHandler.post(tickRunnable)
        uiHandler.post { maybeShowEpisodeCards() }
    }

    override fun onPause() {
        super.onPause()
        // v0.2.0f: the finale-choice dialog is back-button
        // cancelable; never let its showing-flag strand across a
        // pause (a re-present is harmless — chooseFinale no-ops
        // once a choice is locked).
        finaleChoiceShowing = false
        // v0.5.0h: same courtesy for the season finale's call
        // (a re-present is harmless — choose no-ops once locked).
        finaleVerdictShowing = false
        SoundEngine.pauseLoops()
        uiHandler.removeCallbacks(tickRunnable)
        try {
            prefs.saveGame(state)
            prefs.saveNpcMemory()
        } catch (e: Exception) {
            // never crash on save
        }
    }

    // ---------------- v0.1.9c episode cards ----------------
    // The engine queues; this presents — ONE card at a time, and
    // only at safe moments: scene entry, dialogue close, resume,
    // activity start. Never mid-dialogue (the queue just waits),
    // never stacked on a paper dialog (cards only spawn from the
    // discrete hooks below and from a card's own dismissal).
    private var episodeCardShowing = false

    private fun dialogueOpen(): Boolean =
        listOf("talk", "say", "visitor").any {
            fragmentManager.findFragmentByTag(it) != null
        }

    private fun maybeShowEpisodeCards() {
        if (episodeCardShowing || isFinishing || isDestroyed) return
        if (dialogueOpen()) return
        // yesterday's credits first, then the new title card,
        // then any queued act-break sting.
        val wrap = state.episodes.lastOrNull { it.finished && !it.wrapShown }
        if (wrap != null) { showWrapCard(wrap); return }
        val title = state.episodes.firstOrNull { !it.finished && !it.titleShown }
        if (title != null) { showTitleCard(title); return }
        val act = state.episodes.firstOrNull {
            !it.finished && it.actPending in 1..3
        }
        if (act != null) showActCard(act, act.actPending)
        // v0.2.0f: if no card took the moment, the finale's choice
        // may be waiting on it — same safe-moment discipline.
        if (!episodeCardShowing) maybeShowFinaleChoice()
        if (!episodeCardShowing) maybeShowAccusation()
        if (!episodeCardShowing) maybeShowDisposition()
        if (!episodeCardShowing) maybeShowVigil()
        if (!episodeCardShowing) maybeShowConfrontation()
        // v0.8.0f: the opinion case's town meeting — the same
        // safe-moment discipline, one rung below the
        // confrontation it queues behind.
        if (!episodeCardShowing) maybeShowMeeting()
        // v0.8.0f: and a stance the town is still waiting to
        // hear — offered after the talk that witnessed the
        // telling, never forced, never stacked on a climax.
        if (!episodeCardShowing) maybeShowVoice()
        // v0.9.0f: the backwards case's homecoming — the same
        // safe-moment discipline, one rung below the meeting
        // it queues behind.
        if (!episodeCardShowing) maybeShowRestage()
        // v0.9.0f: and a timeline line waiting to be written
        // (or struck and rewritten) — offered once the
        // evidence for a movement is in hand, never forced.
        if (!episodeCardShowing) maybeShowCommit()
        // v0.10.0f: the alibi case's reading — the same
        // safe-moment discipline, one rung below the
        // homecoming it queues behind.
        if (!episodeCardShowing) maybeShowReading()
        // v0.10.0f: and a pair waiting to be judged (or
        // struck and judged again) — offered once the
        // evidence for a pair is in hand, never forced.
        if (!episodeCardShowing) maybeShowPair()
        // v0.11.0f: the audit case's reckoning — the same
        // safe-moment discipline, one rung below the
        // reading it queues behind.
        if (!episodeCardShowing) maybeShowAudit()
        // v0.11.0f: and a site waiting to be called (or
        // struck and called again) — offered once the
        // evidence for a site is in hand, never forced.
        if (!episodeCardShowing) maybeShowSite()
        // v0.12.0f: the photo case's rehanging — the same
        // safe-moment discipline, one rung below the
        // reckoning it queues behind.
        if (!episodeCardShowing) maybeShowHanging()
        // v0.12.0f: and a frame waiting to be read (or
        // struck and read again) — offered once the
        // evidence for a frame is in hand, never forced.
        if (!episodeCardShowing) maybeShowFrame()
        // v0.13.0f: the voice case's Open Letters — the same
        // safe-moment discipline, one rung below the
        // rehanging it queues behind.
        if (!episodeCardShowing) maybeShowVoicing()
        // v0.13.0f: and a letter waiting to be read (or
        // struck and read again) — offered once the
        // evidence for a letter is in hand, never forced.
        if (!episodeCardShowing) maybeShowLetter()
        // v0.14.0f: the returns case's reading — the same
        // safe-moment discipline, one rung below the Open
        // Letters it queues behind.
        if (!episodeCardShowing) maybeShowReturns()
        // v0.14.0f: and a return waiting to be read (or
        // struck and read again) — offered once the
        // evidence for a return is in hand, never forced.
        if (!episodeCardShowing) maybeShowReturnItem()
        // v0.15.0j: the exhibition case's reading — the
        // same safe-moment discipline, one rung below the
        // reading it queues behind.
        if (!episodeCardShowing) maybeShowVerdict()
        // v0.15.0j: and a frame waiting to be read (or
        // struck and read again) — offered once the
        // evidence for a frame is in hand, never forced.
        if (!episodeCardShowing) maybeShowFrameItem()
        // v0.5.0h: and last, the season finale's closing call —
        // it presents only when no case climax is due (Finales
        // defers to the cases; two climaxes never stack).
        if (!episodeCardShowing) maybeShowFinaleVerdict()
        // v0.10.0a: and below even that, moving day — the
        // family's arrival plays at home, at a safe moment,
        // and yields to every climax in town.
        if (!episodeCardShowing) maybeShowFamilyArrival()
        // v0.10.0b: and at the very bottom, hero nights — a
        // caper movement when you're standing in its chapter
        // on a patrol night, the caper's finale at the park,
        // and the founding of your hero identity. All three
        // yield to everything above; the patrol keeps.
        if (!episodeCardShowing) maybeShowCaperBeat()
        if (!episodeCardShowing) maybeShowHeroCreation()
        // v0.10.0c: and at the very floor, opening night —
        // the school play's set piece presents at the
        // Elementary in its house hours when nothing bigger
        // owns the evening (Play defers to case climaxes,
        // finales and holiday beats, and slips a night
        // rather than fight them).
        if (!episodeCardShowing) maybeShowPlayFinale()
    }

    // ---------------- v0.10.0b: hero nights ----------------
    // The creation beat, the caper movements and the finale
    // all ride the safe-moment chain's bottom: two-step
    // authored dialogs (a name, a power; a helped/witnessed
    // choice; a stance), with the scenes themselves dealt
    // into the conversation card like every story beat.
    private var caperBeatShowing = false
    private var heroDlgShowing = false

    private fun maybeShowCaperBeat() {
        if (caperBeatShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val finale = Heroes.finaleDue(state)
        val ch = if (finale) null else Heroes.caperBeatDue(state)
        if (!finale && ch == null) return
        caperBeatShowing = true
        SoundEngine.actSting()
        if (finale) {
            state.lastConversation = Heroes.finaleLines(state)
            refreshUi()
            UiKit.showItemsDialog(this, "🦸 The Caper — The Whole Park, Six Inches Left",
                Heroes.FINALE_STANCES.map { it.second }
                    .toTypedArray()) { which ->
                caperBeatShowing = false
                val stance = Heroes.FINALE_STANCES
                    .getOrNull(which)?.first ?: "silence"
                bg.execute {
                    val text = Heroes.playFinale(state, stance)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "🦸 The Caper Closes", message = text)
                    }
                }
            }
        } else if (ch != null) {
            state.lastConversation = ch.lines
            refreshUi()
            UiKit.showItemsDialog(this,
                "🦸 The Caper — Chapter " +
                    "${Heroes.caperStage(state)}: ${ch.title}",
                arrayOf(ch.helpLabel, ch.watchLabel)) { which ->
                caperBeatShowing = false
                bg.execute {
                    val text = Heroes.playCaperBeat(state, which == 0)
                    if (text != null) {
                        state.lastConversation = ch.lines + listOf(
                            Line("🦸 Hero Work", text))
                    }
                    prefs.saveGame(state)
                    runOnUiThread { refreshUi() }
                }
            }
        }
    }

    private fun maybeShowHeroCreation() {
        if (heroDlgShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Heroes.creationDue(state)) return
        heroIdentityDialog(changing = false)
    }

    /** The two-step founding (or re-founding, at the
     *  mirror): pick a name from the authored list — or just
     *  be the New Kid, caped — then a power. The costume
     *  derives itself from your worn palette. */
    private fun heroIdentityDialog(changing: Boolean) {
        heroDlgShowing = true
        val me = try { engine.player(state) } catch (e: Exception) {
            heroDlgShowing = false; return }
        val nameOpts = Heroes.NAME_OPTIONS +
            "Just be ${me.name}, caped"
        UiKit.showItemsDialog(this,
            if (changing) "🦸 Your Hero — a New Name"
            else "🦸 Word Is, There's a Patrol",
            nameOpts.toTypedArray()) { which ->
            val chosen = if (which in Heroes.NAME_OPTIONS.indices)
                Heroes.NAME_OPTIONS[which] else me.name
            UiKit.showItemsDialog(this, "🦸 And a Power",
                Heroes.POWERS.map { it.second }
                    .toTypedArray()) { pwhich ->
                heroDlgShowing = false
                val powerId = Heroes.POWERS
                    .getOrNull(pwhich)?.first ?: "speed"
                bg.execute {
                    val ok = if (changing)
                        Heroes.changeHero(state, chosen, powerId)
                    else Heroes.createHero(state, chosen, powerId)
                    if (ok) {
                        state.lastConversation = listOf(
                            Line("🦸 Hero Log",
                                "Identity founded: $chosen — " +
                                    Heroes.powerLabel(state) + ". " +
                                    "The cape is your own colors. " +
                                    "The mask matches. Patrol " +
                                    "nights: Wednesday & Saturday, " +
                                    "19:30 sharp."))
                    }
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (ok) toast("🦸 $chosen is on the " +
                            "patrol roster.")
                    }
                }
            }
        }
    }

    // ---------------- v0.10.0c: the school play ----------------
    // Opening night rides the chain's floor: the show's
    // beats play on the conversation card, then your one
    // played beat (your line / your cue / your save — the
    // honest-input pattern), then the room's verdict,
    // judged live from the state of the show.
    private var playFinaleShowing = false

    private fun maybeShowPlayFinale() {
        if (playFinaleShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Play.finaleDue(state)) return
        playFinaleShowing = true
        SoundEngine.actSting()
        state.lastConversation = Play.finaleIntro(state)
        refreshUi()
        UiKit.showItemsDialog(this,
            "🎭 Opening Night — “${Play.TITLE}”",
            Play.finaleChoices(state).toTypedArray()) { which ->
            playFinaleShowing = false
            bg.execute {
                val text = Play.playFinale(state, which)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (text != null) UiKit.showMessageDialog(
                        this, "🎭 The Verdict of the Room",
                        message = text)
                }
            }
        }
    }

    /** Auditions: pick your track — then, if you're
     *  reading for a part, give the read. The delivery
     *  leans the casting math; the town's data decides. */
    private fun playAuditionDialog() {
        UiKit.showMessageDialog(this,
            "🎭 Auditions — “${Play.TITLE}”",
            "Victoria has a clipboard, a pencil, and the " +
                "particular calm of a principal who has " +
                "directed this show in her head since " +
                "September.\n\n${Play.PREMISE}\n\nSeven " +
                "named parts, an ensemble, a crew — and " +
                "one goat, cast from the brave.",
            positiveLabel = "🎭 I'm ready",
            onPositive = {
                UiKit.showItemsDialog(this,
                    "🎭 How do you walk in?",
                    arrayOf(
                        "🎤 AUDITION — read for a part",
                        "🔧 CREW — sets, lights, props: " +
                            "the show's other glory",
                        "🪑 AUDIENCE — a good seat and " +
                            "opening night off")) { which ->
                    when (which) {
                        0 -> UiKit.showItemsDialog(this,
                            "🎭 Your read",
                            Play.DELIVERIES.map { it.second }
                                .toTypedArray()) { dWhich ->
                            val delivery = Play.DELIVERIES
                                .getOrNull(dWhich)?.first
                                ?: "true"
                            bg.execute {
                                val text = Play.chooseAudition(
                                    state, delivery)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    refreshUi()
                                    if (text != null)
                                        UiKit.showMessageDialog(
                                            this, "🎭 The Cast List",
                                            message = text)
                                }
                            }
                        }
                        1 -> UiKit.showItemsDialog(this,
                            "🎭 The crew clipboard",
                            Play.DUTIES.map { it.second }
                                .toTypedArray()) { dWhich ->
                            val duty = Play.DUTIES
                                .getOrNull(dWhich)?.first
                                ?: "sets"
                            bg.execute {
                                val text = Play.chooseCrew(
                                    state, duty)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    refreshUi()
                                    if (text != null)
                                        UiKit.showMessageDialog(
                                            this, "🎭 The Crew Line",
                                            message = text)
                                }
                            }
                        }
                        else -> bg.execute {
                            val text = Play.chooseAudience(state)
                            prefs.saveGame(state)
                            runOnUiThread {
                                refreshUi()
                                if (text != null)
                                    UiKit.showMessageDialog(
                                        this, "🎭 The Audience",
                                        message = text)
                            }
                        }
                    }
                }
            })
    }

    // ---------------- v0.10.0a: the family arrives ----------------
    // The arrival beat: a safe-moment dialog at YOUR HOUSE on
    // the arrival morning (day 3 of a new save; the next
    // eligible morning for a pre-0.10.0a save). One stance
    // choice — how the New Kid takes the move — remembered in
    // the parents' lines for the rest of the save.
    private var familyArrivalShowing = false

    private fun maybeShowFamilyArrival() {
        if (familyArrivalShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Family.arrivalDue(state)) return
        val me = try { engine.player(state) } catch (e: Exception) { return }
        if (me.location != YOUR_HOUSE) return
        familyArrivalShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "🚚 Moving Day",
            arrayOf(
                "❤️ Take it warm — \"I'm glad you're here. All of you.\"",
                "👍 Take it fine — \"Cool. It's... fine. It's good.\"",
                "😶 Take it complicated — \"It's a lot. I need a minute.\"")) { which ->
            familyArrivalShowing = false
            val stance = listOf("warm", "fine", "complicated")
                .getOrElse(which) { "fine" }
            bg.execute {
                if (Family.chooseStance(state, stance)) {
                    state.lastConversation = listOf(
                        Line(Family.MOM,
                            Family.arrivalLine(Family.MOM, stance)),
                        Line(Family.DAD,
                            Family.arrivalLine(Family.DAD, stance)))
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🚚 Your family lives here now — Mom at the " +
                            "Bank, Dad at the Post Office, and family " +
                            "dinner at home three nights a week.")
                        refreshUi()
                    }
                }
            }
        }
    }

    /** Your parents' own talk sheet. The family's lines carry
     *  judged game state (dinner beats, chores, the allowance),
     *  so they stay on the authored deterministic brain — no LLM
     *  path — with the darkroom's print-owed line honored first,
     *  exactly like a cast Talk. */
    private fun openFamilySheet(parent: String) {
        SoundEngine.select()
        val persona = Family.persona(state, parent)
        val me = engine.player(state)
        val sheet = DialogueSheet.newInstance("Talking to $parent (family)")
        sheet.playerName = me.name
        sheet.speakerColors = mapOf(parent to persona.color) +
            state.agents.mapValues { it.value.color }
        val owed = Darkroom.owedLine(state, parent)
        sheet.initialLines = listOf(owed
            // v0.14.0d: on your birthday, a parent marks it
            // before anything else — their own line, their
            // own famPts warmth, once per parent per
            // birthday, and only when they're home to say it.
            ?: PlayerBirthday.familyLine(state, parent)
            // v0.10.0b: the morning after a patrol night, a
            // parent has OPINIONS about the cape (once a day
            // each) — before the everyday greeting.
            ?: Heroes.parentRemark(state, parent)
            ?: Line(parent, Family.greeting(state, parent)))
        sheet.onSend = { text -> sendFamilyTalk(sheet, parent, text) }
        sheet.onDismissed = { maybeShowEpisodeCards() }
        @Suppress("DEPRECATION")
        sheet.show(fragmentManager, "visitor")
    }

    private fun sendFamilyTalk(sheet: DialogueSheet, parent: String,
                                text: String) {
        bg.execute {
            runOnUiThread { sheet.setTyping(true) }
            // a family-dinner Talk beat judges first, on the
            // same rail as Dinners.judgeTalk in cast talk
            val hit = Family.judgeTalk(state, parent)
            val reply: String
            if (hit != null) {
                reply = hit.text
                if (hit.step == 0) SoundEngine.questAccept()
                if (hit.allLanded) {
                    state.events.add("🍽 Family dinner — every beat " +
                        "landed. The whole table.")
                    while (state.events.size > 40) state.events.removeAt(0)
                }
            } else if (classifyPlayerLine(text) == "bye") {
                Family.noteTalk(state, parent)
                reply = Family.persona(state, parent).byeLines[
                    kotlin.math.abs(text.hashCode() +
                        state.absDay) %
                        Family.persona(state, parent).byeLines.size]
            } else {
                val seed = text.hashCode() + state.absDay * 131 +
                    (NpcMemoryStore.get(parent)?.meets ?: 0) * 17
                reply = Family.reply(state, parent, seed)
            }
            recordTalkMemory(parent, text)
            // family warmth is the authority (it never rides
            // state.friendship) — mirror it into their memory
            NpcMemoryStore.get(parent)?.points = Family.points(state, parent)
            actionTick()
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                sheet.setTyping(false)
                sheet.addLines(listOf(Line(parent, reply)))
                refreshUi()
            }
        }
    }

    // ---------------- v0.2.0f: the finale choice ----------------
    // At the mystery finale's climax, a player holding enough of
    // the case gets ONE decision, presented at a safe moment like
    // the episode cards. No choice is ever forced mid-dialogue;
    // if the day ends unchosen, the town decides for itself.
    private var finaleChoiceShowing = false

    private fun maybeShowFinaleChoice() {
        if (finaleChoiceShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery.finaleChoiceDue(state)) return
        finaleChoiceShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "🔍 The Light Over The Pond",
            arrayOf(
                "📢 Present the truth — you know what that light is " +
                    "(${Mystery.clueCount(state)}/${Mystery.CLUES.size} clues)",
                "🤫 Say nothing — let the town have its aliens")) { which ->
            finaleChoiceShowing = false
            if (Mystery.chooseFinale(state, presentTruth = which == 0)) {
                prefs.saveGame(state)
                toast(if (which == 0)
                    "🔍 You step forward. The pond goes very quiet."
                else
                    "🔍 You say nothing. The town will write its own ending.")
                refreshUi()
            }
        }
    }

    // ---------------- v0.3.0f: the accusation ----------------
    // At the second screening's climax, a player holding enough
    // of the booth case gets ONE name to say, presented at a
    // safe moment like the pond finale's choice. Four suspects;
    // the name, once said, sticks. If the night ends unsaid,
    // the case goes cold on its own.
    private var accusationShowing = false

    private fun maybeShowAccusation() {
        if (accusationShowing || finaleChoiceShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery2.accusationDue(state)) return
        accusationShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "🔍 The Locked Booth",
            arrayOf(
                "🎞️ Accuse Mr. Mackey — the chaperone (his story keeps moving)",
                "🍿 Accuse Clyde — near the booth; he wanted the ending",
                "😐 Accuse Craig — the deadpan non-alibi",
                "🎟️ Accuse ${Mystery2.suspectLabel(state, "usher")} — the usher, the obvious suspect",
                "🤐 Say no name — let the case go cold")) { which ->
            accusationShowing = false
            if (which == 4) {
                if (Mystery2.declineAccusation(state)) {
                    prefs.saveGame(state)
                    toast("🔍 You keep your seat. The moment passes.")
                    refreshUi()
                }
                return@showItemsDialog
            }
            val key = Mystery2.SUSPECT_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery2.accuse(state, key)) {
                prefs.saveGame(state)
                toast("🔍 You say the name: ${Mystery2.suspectLabel(state, key)}. " +
                    "It echoes a little in the Bijou.")
                refreshUi()
            }
        }
    }

    // ---------------- v0.4.0f: the disposition ----------------
    // At the last pit's climax, a player holding enough of the
    // buried case gets ONE decision — not a name, a fate: what
    // happens to the box. Presented at a safe moment like the
    // other cases' moments. If the day ends undecided, the
    // county decides by default, the way counties do.
    private var dispositionShowing = false

    private fun maybeShowDisposition() {
        if (dispositionShowing || accusationShowing || finaleChoiceShowing ||
            vigilShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery3.dispositionDue(state)) return
        dispositionShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "🔍 What's Buried",
            arrayOf(
                "📜 Publish it — take the box to the town meeting " +
                    "(${Mystery3.clueCount3(state)}/${Mystery3.CLUES3.size} clues)",
                "🌙 Bury it again — hand it to Randy tonight, and tell no one",
                "✉️ Give it to Sharon — the decision should be hers")) { which ->
            dispositionShowing = false
            val choice = Mystery3.CHOICE_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery3.chooseDisposition(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "publish" -> "🔍 You pick up the box. The meeting's going to be a long one."
                    "bury" -> "🔍 You pick up the box. Nobody sees you look at Randy."
                    else -> "🔍 You pick up the box. You know whose door this ends at."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.6.0c: the vigil ----------------
    // At the last chapter's climax, after dusk, a player
    // holding enough of the gardener case gets ONE decision —
    // not a fate, a posture toward gratitude: what the town
    // does with the truth it finally holds. Presented at a
    // safe moment like the other cases' moments. If the
    // night ends undecided, the winter decides by default.
    private var vigilShowing = false

    private fun maybeShowVigil() {
        if (vigilShowing || dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            confrontationShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery4.vigilDue(state)) return
        vigilShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "🌙 The Night Gardener",
            arrayOf(
                "🌟 Name him — the town gets its thank-you, in the open " +
                    "(${Mystery4.clueCount4(state)}/${Mystery4.CLUES4.size} clues)",
                "🤫 Guard the secret — you know. Let that be enough",
                "🧤 Join in — leave a fix of your own beside his tonight")) { which ->
            vigilShowing = false
            val choice = Mystery4.CHOICE_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery4.chooseVigil(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "name" -> "🌙 You step into the lamplight. Tomorrow, a ceremony."
                    "guard" -> "🌙 You step back into the dark. The rope gets fixed."
                    else -> "🌙 Two signatures by morning. Only one of them has a name anyone knows."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.7.0f: the confrontation ----------------
    // On the confession case's last day, once the climax has
    // begun and the evidence is mostly in hand, both
    // confessors are in the room and the call is yours.
    // Presented at a safe moment like every case's climax;
    // dismissing it — or never facing them — leaves the file
    // to go cold at the wrap.
    private var confrontationShowing = false

    private fun maybeShowConfrontation() {
        if (confrontationShowing || vigilShowing || dispositionShowing ||
            accusationShowing || finaleChoiceShowing ||
            finaleVerdictShowing || episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery5.confrontationDue(state)) return
        confrontationShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "⚖️ The Two Confessions",
            arrayOf(
                "📛 Name Butters — his story fell apart, claim by claim " +
                    "(${Mystery5.clueCount5(state)}/${Mystery5.CLUES5.size} evidence)",
                "🎙️ Believe Ned — every word he said checks out",
                "⚖️ Clear them both — the evidence won't carry either confession")) { which ->
            confrontationShowing = false
            val choice = Mystery5.CHOICE_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery5.chooseConfrontation(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "name" -> "⚖️ The name goes in the file. His mother is already standing up."
                    "believe" -> "⚖️ The file closes on a true story. He accepts it like weather."
                    else -> "⚖️ The ledger goes flat on the table. The room breathes again."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.8.0f: the town meeting ----------------
    // On the opinion case's last day, once the climax has
    // begun and the evidence is mostly in hand, the room is
    // full and the call is yours: the evidence, the town,
    // your heart, or your silence. Presented at a safe moment
    // like every case's climax; dismissing it — or never
    // attending — leaves the meeting to happen without you.
    private var meetingShowing = false

    private fun maybeShowMeeting() {
        if (meetingShowing || voiceShowing || confrontationShowing ||
            vigilShowing || dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery6.meetingDue(state)) return
        meetingShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "🏛️ The Town Meeting",
            arrayOf(
                "🧩 Present the evidence — ${Mystery6.clueCount6(state)}/${Mystery6.CLUES6.size} " +
                    "proven (the score drops by its weight, ${Mystery6.presentedWeight(state)})",
                "📣 Back the town — your voice joins the story (+${Mystery6.BACK_BOOST})",
                "🖤 Speak for the accused — no facts, just her (−${Mystery6.SPEAK_RELIEF})",
                "🤐 Let it land — the town decides where it stands")) { which ->
            meetingShowing = false
            val choice = Mystery6.CHOICE_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery6.chooseMeeting(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "present" -> "🧩 The evidence goes on the table. Arithmetic enters the room."
                    "back" -> "📣 Your voice joins the story's side of the room."
                    "speak" -> "🖤 You stand up for a person. The room has to sit with that."
                    else -> "🤐 You let it land. The record keeps your silence too."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.8.0f: your voice, in conversation ----------------
    // A telling you have witnessed is still going around, and
    // the town is waiting to hear which side of your mouth it
    // comes back out of. Offered at the next safe moment after
    // the talk that witnessed it; the amounts are printed on
    // the buttons before they're pressed; dismissing it just
    // leaves the stance untaken — the telling stays pending.
    private var voiceShowing = false

    private fun maybeShowVoice() {
        if (voiceShowing || meetingShowing || confrontationShowing ||
            vigilShowing || dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val g = Mystery6.pendingStanceGossip(state) ?: return
        voiceShowing = true
        SoundEngine.select()
        UiKit.showItemsDialog(this, "🗣️ The story's going around",
            arrayOf(
                "📣 Back the story (+${Mystery6.VOICE_BACK} conviction): " +
                    "“${g.storyShort}”",
                "🧐 Question it (${Mystery6.VOICE_QUESTION} conviction): " +
                    "ask for the proof, out loud",
                "🤐 Say nothing (no change): the town notes the silence")) { which ->
            voiceShowing = false
            val stance = Mystery6.STANCE_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery6.chooseStance(state, g.id, stance)) {
                prefs.saveGame(state)
                toast(when (stance) {
                    "back" -> "📣 It goes around with your name under it now."
                    "question" -> "🧐 The telling goes around a little slower after that."
                    else -> "🤐 Noted. The story keeps your silence too."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.9.0f: the homecoming ----------------
    // On the backwards case's last day, once the climax has
    // begun and the timeline is complete, the confrontation
    // is replayed — same room, same people — with the whole
    // story known. The gated calls only appear when the
    // final state of the timeline can carry them (the module
    // re-checks at lock time; the board's forecast publishes
    // the terms in advance). Dismissing it — or never
    // attending — leaves the ceremony to rerun itself.
    private var restageShowing = false

    private fun maybeShowRestage() {
        if (restageShowing || commitShowing || readingShowing ||
            pairShowing || meetingShowing ||
            voiceShowing || confrontationShowing ||
            vigilShowing || dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery7.restageDue(state)) return
        restageShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery7.originEligible7(state)) {
            labels.add("🗣️ Say what really started it — the beginning, " +
                "as the timeline now tells it, in order")
            keys.add("origin")
        }
        labels.add("🏛️ Let the original ending stand — the taking " +
            "was the story; let it stay the story")
        keys.add("stand")
        if (Mystery7.changeEligible7(state)) {
            labels.add("🔧 Change one thing — read the ledger's " +
                "second page into the record before anyone decides")
            keys.add("change")
        }
        labels.add("🚶 Walk away — the ceremony can have the room; " +
            "it can't have you")
        keys.add("walk")
        UiKit.showItemsDialog(this, "🏫 The Homecoming",
            labels.toTypedArray()) { which ->
            restageShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery7.chooseFinale7(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "origin" -> "🗣️ You start the story at its beginning. The room hears the ending last this time."
                    "stand" -> "🏛️ The first ending stands. The plaque gets its sentence."
                    "change" -> "🔧 One thing changes: the second page enters the record, aloud."
                    else -> "🚶 You walk away. The rerun starts without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.9.0f: writing the timeline ----------------
    // A movement whose live evidence is in hand can be
    // committed as a reconstruction — one of two written
    // readings, shown whole on the buttons. Offered at a
    // safe moment whenever a line is writable (or a broken
    // line is standing struck, waiting to be rewritten);
    // dismissing it just leaves the line as it is. The
    // verdict is derived, never chosen: the past grades
    // the line, not the dialog.
    private var commitShowing = false

    private fun maybeShowCommit() {
        if (commitShowing || restageShowing || readingShowing ||
            pairShowing || meetingShowing ||
            voiceShowing || confrontationShowing ||
            vigilShowing || dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val m = Mystery7.pendingCommit7(state) ?: return
        val revising = m.id in state.arc7Commits
        commitShowing = true
        SoundEngine.select()
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${m.title} — write it again"
            else "🖊️ Commit the reconstruction: ${m.title}",
            arrayOf(
                "📜 The easy reading: “${m.easy}”",
                "🔍 The checked reading: “${m.checked}”")) { which ->
            commitShowing = false
            val pick = Mystery7.PICK_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery7.chooseCommit7(state, m.id, pick)) {
                prefs.saveGame(state)
                toast(when (Mystery7.verdict7(state, m)) {
                    "CONFIRMED" -> "🖊️ Committed — and the past agrees. The line stands confirmed."
                    "BROKEN" -> "💔 Committed — and broken on arrival. The evidence already refutes it. It can be rewritten."
                    else -> "🖊️ Committed. The line stands, untested — the past hasn't reached it yet."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.10.0f: the reading ----------------
    // At Family Night, item six: the program pauses for the
    // matter of the sign, and — if the graph is complete,
    // every pair judged — the evening is re-counted, or
    // named, or left standing, by your call. The gated
    // calls only appear when the graph's final derived
    // state can carry them (the module re-checks at lock
    // time; the board's forecast publishes the terms in
    // advance). Dismissing it — or never attending — leaves
    // the room to its column.
    private var readingShowing = false

    private fun maybeShowReading() {
        if (readingShowing || pairShowing || commitShowing ||
            auditShowing || siteShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery8.readingDue(state)) return
        readingShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery8.readEligible8(state)) {
            labels.add("🗣️ Read the graph aloud — the evening, " +
                "re-counted pair by pair, the way the evidence settled it")
            keys.add("read")
        }
        if (Mystery8.nameEligible8(state)) {
            labels.add("⚖️ Name the liar — stand by the town's " +
                "arithmetic: somebody's alibi fell; say whose it makes them")
            keys.add("name")
        }
        labels.add("🏛️ Let the count stand — seven alibis, " +
            "verified; the town keeps the evening it counted")
        keys.add("stand")
        labels.add("🚶 Walk away — the program can have the room; " +
            "it can't have the graph, and it can't have you")
        keys.add("walk")
        UiKit.showItemsDialog(this, "📖 Item Six: The Matter of the Sign",
            labels.toTypedArray()) { which ->
            readingShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery8.chooseFinale8(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "read" -> "🗣️ You read the graph aloud. The room re-counts with you."
                    "name" -> "⚖️ The room gets its name. The minutes take it down, straight."
                    "stand" -> "🏛️ The count stands. The column stays the evening's shape."
                    else -> "🚶 You walk away. Item six starts without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.10.0f: judging a pair ----------------
    // A pair whose live evidence is in hand can be judged —
    // the two tellings describe ONE event or TWO — shown
    // whole on the buttons, with the pair's shared detail.
    // Offered at a safe moment whenever a pair is judgeable
    // (or a refuted judgment is standing struck, waiting to
    // be judged again); dismissing it just leaves the pair
    // as it is. The verdict is derived, never chosen: the
    // settling piece grades the judgment, not the dialog.
    private var pairShowing = false

    private fun maybeShowPair() {
        if (pairShowing || readingShowing || commitShowing ||
            auditShowing || siteShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val p = Mystery8.pendingPair8(state) ?: return
        val revising = p.id in state.arc8Pairs
        pairShowing = true
        SoundEngine.select()
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${p.title} — judge it again"
            else "✒️ Judge the pair: ${p.title}",
            arrayOf(
                "🔗 ONE EVENT — “${p.signatureText}”",
                "✂️ TWO EVENTS — the tellings share a detail, not a moment")) { which ->
            pairShowing = false
            val judgment = Mystery8.JUDGE_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery8.choosePair8(state, p.id, judgment)) {
                prefs.saveGame(state)
                toast(when (Mystery8.pairVerdict8(state, p)) {
                    "PROVEN" -> "✒️ Judged — and proven. The settling piece backs this judgment."
                    "REFUTED" -> "💔 Judged — and refuted. The settling piece settles it the other way. It can be judged again."
                    else -> "✒️ Judged. The judgment stands, untested — its settling piece is still out there."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.11.0f: the reckoning ----------------
    // At the Reckoning, the labeled space in the program:
    // the audit closes — if the ledger is complete, every
    // site called — by a reading, a name, a filing, or a
    // walk. The gated calls only appear when the ledger's
    // final derived state can carry them (the module
    // re-checks at lock time; the board's forecast
    // publishes the terms in advance). Dismissing it — or
    // never attending — leaves the room to its board.
    private var auditShowing = false

    private fun maybeShowAudit() {
        if (auditShowing || siteShowing || hangingShowing ||
            frameShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery9.auditDue(state)) return
        auditShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery9.readEligible9(state)) {
            labels.add("🗣️ Read the audit aloud — the count, " +
                "reconciled site by site, the way the evidence settled it")
            keys.add("read")
        }
        if (Mystery9.nameEligible9(state)) {
            labels.add("⚖️ Name the adder — stand by the room's " +
                "arithmetic: something was added; say whose hand it makes")
            keys.add("name")
        }
        labels.add("🏛️ Let the books stand — four counts, " +
            "four extras; the town keeps the books it arrived with")
        keys.add("stand")
        labels.add("🚶 Walk away — the chamber can have the room; " +
            "it can't have the ledger, and it can't have you")
        keys.add("walk")
        UiKit.showItemsDialog(this, "⚖️ The Reckoning: The Count, Closed",
            labels.toTypedArray()) { which ->
            auditShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery9.chooseFinale9(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "read" -> "🗣️ You read the audit aloud. The room reconciles with you."
                    "name" -> "⚖️ The room gets its name. The minutes take it down, straight."
                    "stand" -> "🏛️ The books stand. The file goes in the drawer, stars and all."
                    else -> "🚶 You walk away. The count closes without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.11.0f: calling a site ----------------
    // A site whose live evidence is in hand can be called —
    // the count is wrong, the record is wrong, or someone
    // added one — shown whole on the buttons, with the
    // site's two numbers. Offered at a safe moment whenever
    // a site is callable (or a refuted call is standing
    // struck, waiting to be called again); dismissing it
    // just leaves the site as it is. The verdict is
    // derived, never chosen: the settling piece grades
    // the call, not the dialog.
    private var siteShowing = false

    private fun maybeShowSite() {
        if (siteShowing || auditShowing || hangingShowing ||
            frameShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val s = Mystery9.pendingSite9(state) ?: return
        val revising = s.id in state.arc9Calls
        siteShowing = true
        SoundEngine.select()
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${s.title} — call it again"
            else "✒️ Call the site: ${s.title}",
            arrayOf(
                "🔢 THE COUNT IS WRONG — counted ${s.counted} ${s.countedLabel}; the counters miscounted, and the room matches its book",
                "📖 THE RECORD IS WRONG — the book holds ${s.record} ${s.recordLabel}; the book under-wrote it, and the thing was always honestly there",
                "➕ SOMEONE ADDED ONE — a real extra thing was placed among the ${s.countedLabel}")) { which ->
            siteShowing = false
            val call = Mystery9.CALL_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery9.chooseCall9(state, s.id, call)) {
                prefs.saveGame(state)
                toast(when (Mystery9.siteVerdict9(state, s)) {
                    "PROVEN" -> "✒️ Called — and proven. The settling piece backs this call."
                    "REFUTED" -> "💔 Called — and refuted. The settling piece settles it the other way. It can be called again."
                    else -> "✒️ Called. The call stands, untested — its settling piece is still out there."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.12.0f: the rehanging ----------------
    // At the Rehanging, the labeled space in the program:
    // the wall, corrected — if the board is complete,
    // every frame read — by a reading, a name, a hanging,
    // or a walk. The gated calls only appear when the
    // board's final derived state can carry them (the
    // module re-checks at lock time; the board's forecast
    // publishes the terms in advance). Dismissing it —
    // or never attending — leaves the room to its
    // captions.
    private var hangingShowing = false

    private fun maybeShowHanging() {
        if (hangingShowing || frameShowing || voicingShowing ||
            returnsShowing || returnItemShowing ||
            letterShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery10.hangingDue(state)) return
        hangingShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery10.readEligible10(state)) {
            labels.add("🗣️ Read the frames aloud — print by " +
                "print, check by check, the way the records settled them")
            keys.add("read")
        }
        if (Mystery10.nameEligible10(state)) {
            labels.add("⚖️ Name the Bomber — stand by the town's " +
                "arithmetic: faces don't add themselves; say whose hands they make")
            keys.add("name")
        }
        labels.add("🖼️ Let it hang as loaned — four photographs, " +
            "four captions; the wall keeps the story it arrived with")
        keys.add("stand")
        labels.add("🚶 Walk away — the display can open without you; " +
            "it can't open with your reading")
        keys.add("walk")
        UiKit.showItemsDialog(this, "🖼️ The Rehanging: The Wall, Corrected",
            labels.toTypedArray()) { which ->
            hangingShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery10.chooseFinale10(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "read" -> "🗣️ You read the frames aloud. The wall holds up with you."
                    "name" -> "⚖️ The room gets its name. The program takes it down, straight."
                    "stand" -> "🖼️ It hangs as loaned. The pencil stays under the glass."
                    else -> "🚶 You walk away. The wall opens without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.12.0f: reading a frame ----------------
    // A frame whose live evidence is in hand can be read —
    // genuine, misfiled, or composite — shown whole on the
    // buttons, with the frame's three derived checks.
    // Offered at a safe moment whenever a frame is
    // readable (or a refuted reading is standing struck,
    // waiting to be read again); dismissing it just
    // leaves the frame as it is. The verdict is derived,
    // never chosen: the settling piece grades the
    // reading, not the dialog.
    private var frameShowing = false

    private fun maybeShowFrame() {
        if (frameShowing || hangingShowing || voicingShowing ||
            returnsShowing || returnItemShowing ||
            letterShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val f = Mystery10.pendingFrame10(state) ?: return
        val revising = f.id in state.arc10Calls
        frameShowing = true
        SoundEngine.select()
        fun mark(ok: Boolean) = if (ok) "✓" else "✗"
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${f.title} — read it again"
            else "✒️ Read the frame: ${f.title}",
            arrayOf(
                "📷 GENUINE — one shutter, honestly captioned (checks: place " +
                    "${mark(Mystery10.placeAgrees10(f))}, day ${mark(Mystery10.dayAgrees10(f))}, " +
                    "faces ${mark(Mystery10.facesAgrees10(f))})",
                "🏷️ MISFILED — one honest shutter wearing the wrong caption: " +
                    "the photograph is true; its label names another moment",
                "🧩 COMPOSITE — no single shutter holds it: the frame was " +
                    "made, out of more than one real moment")) { which ->
            frameShowing = false
            val call = Mystery10.CALL_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery10.chooseCall10(state, f.id, call)) {
                prefs.saveGame(state)
                toast(when (Mystery10.frameVerdict10(state, f)) {
                    "PROVEN" -> "✒️ Read — and proven. The settling piece backs this reading."
                    "REFUTED" -> "💔 Read — and refuted. The settling piece settles it the other way. It can be read again."
                    else -> "✒️ Read. The reading stands, untested — its settling piece is still out there."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.13.0f: the Open Letters ----------------
    // On the voice case's last day, once the climax has
    // begun and the ledger is complete, the letters are
    // opened in the hall — read aloud from the ledger, the
    // town's name, the signatures standing, or the folder
    // unopened. The gated calls only appear when the
    // final state of the ledger can carry them (the
    // module re-checks at lock time; the board's forecast
    // publishes the terms in advance). Dismissing it — or
    // never attending — leaves the meeting to settle the
    // mail by applause.
    private var voicingShowing = false

    private fun maybeShowVoicing() {
        if (voicingShowing || letterShowing || hangingShowing ||
            returnsShowing || returnItemShowing ||
            frameShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery11.voicingDue(state)) return
        voicingShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery11.voiceEligible11(state)) {
            labels.add("🎙️ Read the ledger aloud — letter by " +
                "letter, voice by voice, the way the evidence settled them")
            keys.add("voice")
        }
        if (Mystery11.nameEligible11(state)) {
            labels.add("⚖️ Name the borrowed hand — stand by the town's " +
                "arithmetic: voices don't study themselves; say whose stage they make")
            keys.add("name")
        }
        labels.add("📬 Let the letters stand — six letters, " +
            "six signatures; the mail keeps the story it arrived with")
        keys.add("stand")
        labels.add("🚶 Walk away — the meeting can settle the mail without you; " +
            "it can't settle it with your reading")
        keys.add("walk")
        UiKit.showItemsDialog(this, "📬 The Open Letters: The Mail, Corrected",
            labels.toTypedArray()) { which ->
            voicingShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery11.chooseFinale11(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "voice" -> "🎙️ You read the ledger aloud. The mail holds up with you."
                    "name" -> "⚖️ The room gets its name. The minutes take it down, straight."
                    "stand" -> "📬 The letters stand. The signatures stay under the words."
                    else -> "🚶 You walk away. The mail settles without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.13.0f: reading a letter ----------------
    // A letter whose live evidence is in hand can be read —
    // in voice, off voice, or borrowed voice — shown whole
    // on the buttons, with the letter's derived scan.
    // Offered at a safe moment whenever a letter is
    // readable (or a refuted reading is standing struck,
    // waiting to be read again); dismissing it just
    // leaves the letter as it is. The verdict is derived,
    // never chosen: the settling piece grades the
    // reading, not the dialog.
    private var letterShowing = false

    private fun maybeShowLetter() {
        if (letterShowing || voicingShowing || hangingShowing ||
            returnsShowing || returnItemShowing ||
            frameShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val l = Mystery11.pendingLetter11(state) ?: return
        val revising = l.id in state.arc11Calls
        letterShowing = true
        SoundEngine.select()
        val f = Mystery11.findings11(l)
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${l.title} — read it again"
            else "✒️ Read the letter: ${l.title}",
            arrayOf(
                "🎙️ IN VOICE — the signer's own: the hand may be borrowed, " +
                    "the voice never left them (scan: ${f.markersHit.size} markers, " +
                    "${f.neversHit.size} nevers, ${f.tellsHit.size} tells)",
                "✉️ OFF VOICE — a stranger's hand: the signer didn't write it, " +
                    "and nobody studied them to do it",
                "🎭 BORROWED VOICE — a studied imitation: somebody did the " +
                    "homework and wrote AS them")) { which ->
            letterShowing = false
            val call = Mystery11.CALL_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery11.chooseCall11(state, l.id, call)) {
                prefs.saveGame(state)
                toast(when (Mystery11.letterVerdict11(state, l)) {
                    "PROVEN" -> "✒️ Read — and proven. The settling piece backs this reading."
                    "REFUTED" -> "💔 Read — and refuted. The settling piece settles it the other way. It can be read again."
                    else -> "✒️ Read. The reading stands, untested — its settling piece is still out there."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.14.0f: the Reading ----------------
    // On the returns case's last day, once the climax has
    // begun and every return carries a reading, the ledger
    // is read at the school — read aloud from the ledger,
    // the town's name, the returns standing, or the box
    // unopened. The gated calls only appear when the final
    // state of the ledger can carry them (the module
    // re-checks at lock time; the board's forecast
    // publishes the terms in advance). Dismissing it — or
    // never attending — leaves the reading to settle
    // itself by applause.
    private var returnsShowing = false

    private fun maybeShowReturns() {
        if (returnsShowing || returnItemShowing ||
            letterShowing || voicingShowing || hangingShowing ||
            frameShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery12.returnsDue(state)) return
        returnsShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery12.finishEligible12(state)) {
            labels.add("📖 Read the ledger aloud — return by " +
                "return, loss, hand and fit, the way the evidence settled them")
            keys.add("read")
        }
        if (Mystery12.nameEligible12(state)) {
            labels.add("⚖️ Name the hand — stand by the town's " +
                "arithmetic: hands don't study themselves; say whose work they make")
            keys.add("name")
        }
        labels.add("📦 Let the returns stand — six things back, " +
            "six slips; the box keeps the story it arrived with")
        keys.add("stand")
        labels.add("🚶 Walk away — the school can settle the returns without you; " +
            "it can't settle them with your reading")
        keys.add("walk")
        UiKit.showItemsDialog(this, "📦 The Reading: The Returns, Read Aloud",
            labels.toTypedArray()) { which ->
            returnsShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery12.chooseFinale12(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "read" -> "📖 You read the ledger aloud. The returns hold up with you."
                    "name" -> "⚖️ The room gets its name. The record takes it down, straight."
                    "stand" -> "📦 The returns stand. The slips stay under the things."
                    else -> "🚶 You walk away. The box settles without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.14.0f: reading a return ----------------
    // A return whose live evidence is in hand can be read —
    // true return, kind mistake, or planted — shown whole
    // on the buttons, with the return's three derived
    // checks. Offered at a safe moment whenever a return
    // is readable (or a refuted reading is standing
    // struck, waiting to be read again); dismissing it
    // just leaves the return as it is. The verdict is
    // derived, never chosen: the settling piece grades
    // the reading, not the dialog.
    private var returnItemShowing = false

    private fun maybeShowReturnItem() {
        if (returnItemShowing || returnsShowing ||
            letterShowing || voicingShowing || hangingShowing ||
            frameShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val r = Mystery12.pendingItem12(state) ?: return
        val revising = r.id in state.arc12Calls
        returnItemShowing = true
        SoundEngine.select()
        fun mark(ok: Boolean) = if (ok) "✓" else "✗"
        val f = Mystery12.findings12(r)
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${r.title} — read it again"
            else "✒️ Read the return: ${r.title}",
            arrayOf(
                "📦 TRUE RETURN — the box did its one job: the thing that was " +
                    "lost is the thing that came back (checks: loss " +
                    "${mark(f.lossOk)}, fit ${mark(f.fitOk)}, hand " +
                    "${Mystery12.handShapeLabel12(Mystery12.handShape12(r))})",
                "🤲 KIND MISTAKE — an honest filing and a wrong instance: " +
                    "the card is true, and the thing is not theirs",
                "🎭 PLANTED — a studied copy: somebody did the homework " +
                    "and sent the thing on in a borrowed hand")) { which ->
            returnItemShowing = false
            val call = Mystery12.CALL_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery12.chooseCall12(state, r.id, call)) {
                prefs.saveGame(state)
                toast(when (Mystery12.letterVerdict12(state, r)) {
                    "PROVEN" -> "✒️ Read — and proven. The settling piece backs this reading."
                    "REFUTED" -> "💔 Read — and refuted. The settling piece settles it the other way. It can be read again."
                    else -> "✒️ Read. The reading stands, untested — its settling piece is still out there."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.15.0j: the wall, read back ----------------
    // The exhibition case's finale call, presented exactly
    // like the readings before it: gated options only (the
    // full read must be earned, the name call must have
    // one proven frame behind it), the ungated two always
    // on the table. The verdict is derived at the wrap,
    // never in this dialog.
    private var verdictShowing = false

    private fun maybeShowVerdict() {
        if (verdictShowing || frameItemShowing ||
            returnsShowing || returnItemShowing ||
            letterShowing || voicingShowing || hangingShowing ||
            frameShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (!Mystery13.verdictDue(state)) return
        verdictShowing = true
        SoundEngine.actSting()
        val labels = mutableListOf<String>()
        val keys = mutableListOf<String>()
        if (Mystery13.finishEligible13(state)) {
            labels.add("📖 Read the audit aloud — frame by " +
                "frame, print, chain and frame, the way the evidence settled them")
            keys.add("read")
        }
        if (Mystery13.nameEligible13(state)) {
            labels.add("⚖️ Name the hand — stand by the town's " +
                "arithmetic: prints don't replace themselves; say whose work it makes")
            keys.add("name")
        }
        labels.add("🖼 Let the wall stand — six frames hung, " +
            "six readings owed; the log keeps the story it arrived with")
        keys.add("stand")
        labels.add("🚶 Walk away — the hall can settle the wall without you; " +
            "it can't settle it with your audit")
        keys.add("walk")
        UiKit.showItemsDialog(this, "🖼 The Wall, Read Back: The Audit, Read Aloud",
            labels.toTypedArray()) { which ->
            verdictShowing = false
            val choice = keys.getOrNull(which) ?: return@showItemsDialog
            if (Mystery13.chooseFinale13(state, choice)) {
                prefs.saveGame(state)
                toast(when (choice) {
                    "read" -> "📖 You read the audit aloud. The wall holds up with you."
                    "name" -> "⚖️ The room gets its name. The record takes it down, straight."
                    "stand" -> "🖼 The wall stands. The frames stay under their labels."
                    else -> "🚶 You walk away. The log settles without you."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.15.0j: reading a frame ----------------
    // A frame whose live evidence is in hand can be read —
    // true hanging, honest cross, or swapped print — shown
    // whole on the buttons, with the frame's three derived
    // checks. Offered at a safe moment whenever a frame is
    // readable (or a refuted reading is standing struck,
    // waiting to be read again); dismissing it just leaves
    // the frame as it is. The verdict is derived, never
    // chosen: the settling piece grades the reading, not
    // the dialog.
    private var frameItemShowing = false

    private fun maybeShowFrameItem() {
        if (frameItemShowing || verdictShowing ||
            returnsShowing || returnItemShowing ||
            letterShowing || voicingShowing || hangingShowing ||
            frameShowing || auditShowing ||
            siteShowing || readingShowing ||
            pairShowing || commitShowing ||
            restageShowing || meetingShowing || voiceShowing ||
            confrontationShowing || vigilShowing ||
            dispositionShowing || accusationShowing ||
            finaleChoiceShowing || finaleVerdictShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        val fr = Mystery13.pendingFrame13(state) ?: return
        val revising = fr.id in state.arc13Calls
        frameItemShowing = true
        SoundEngine.select()
        fun mark(ok: Boolean) = if (ok) "✓" else "✗"
        val f = Mystery13.findings13(state, fr)
        UiKit.showItemsDialog(this,
            if (revising) "💔 Struck: ${Mystery13.frameLabel13(fr)} — read it again"
            else "✒️ Read the frame: ${Mystery13.frameLabel13(fr)}",
            arrayOf(
                "🖼 TRUE HANGING — the piece in the frame is the piece in " +
                    "the record (checks: print ${mark(f.printOk)}, chain " +
                    "${mark(f.chainOk)}, frame ${mark(f.frameOk)})",
                "🤲 HONEST CROSS — the records are honest and the hanging " +
                    "wasn't: the piece is another photograph's, or its road broke",
                "🎭 SWAPPED PRINT — the photograph is right in every " +
                    "record and the piece itself is a fresh printing: " +
                    "somebody replaced the original and kept the frame's face")) { which ->
            frameItemShowing = false
            val call = Mystery13.CALL_KEYS.getOrNull(which) ?: return@showItemsDialog
            if (Mystery13.chooseCall13(state, fr.id, call)) {
                prefs.saveGame(state)
                toast(when (Mystery13.frameVerdict13(state, fr)) {
                    "PROVEN" -> "✒️ Read — and proven. The settling piece backs this reading."
                    "REFUTED" -> "💔 Read — and refuted. The settling piece settles it the other way. It can be read again."
                    else -> "✒️ Read. The reading stands, untested — its settling piece is still out there."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.5.0h: the finale's closing call ----------------
    // On a season finale's last day, a player who caught at
    // least one of the week's beats is handed the closing
    // call — a verdict, a finish line, a scale, a lantern.
    // Presented at a safe moment like the cases' climaxes,
    // and it WAITS while any case climax is due: the cases
    // were here first, and two climaxes never stack. If the
    // day ends uncalled, the town makes the call itself.
    private var finaleVerdictShowing = false

    private fun maybeShowFinaleVerdict() {
        if (finaleVerdictShowing || dispositionShowing || accusationShowing ||
            finaleChoiceShowing || vigilShowing || confrontationShowing ||
            meetingShowing || voiceShowing ||
            episodeCardShowing) return
        if (isFinishing || isDestroyed || dialogueOpen()) return
        if (Finales.climaxPending(state)) return
        val def = Finales.choiceDef(state) ?: return
        finaleVerdictShowing = true
        SoundEngine.actSting()
        UiKit.showItemsDialog(this, "${def.emoji} ${def.title}",
            def.choices.map { it.label }.toTypedArray()) { which ->
            finaleVerdictShowing = false
            val choice = def.choices.getOrNull(which) ?: return@showItemsDialog
            if (Finales.choose(state, choice.key)) {
                prefs.saveGame(state)
                toast("${def.emoji} The call is yours. The town holds its breath.")
                refreshUi()
            }
        }
    }

    private fun showTitleCard(ep: EpisodeRecord) {
        episodeCardShowing = true
        SoundEngine.titleSting()
        val stage = if (ep.kind == "event")
            Quests.currentStage(state).coerceAtLeast(0) else -1
        val page = Episodes.page(ep, stage).copy(footer = "")
        UiKit.showEpisodeDialog(this, page, "▶ START THE DAY") {
            // accepting the episode = accepting the quest
            SoundEngine.questAccept()
            ep.titleShown = true
            episodeCardShowing = false
            prefs.saveGame(state)
            maybeShowEpisodeCards()
        }.show()
    }

    private fun showActCard(ep: EpisodeRecord, stageIdx: Int) {
        episodeCardShowing = true
        SoundEngine.actSting()
        val page = EpisodePage(
            titleLine = Episodes.actName(stageIdx),
            subLine = ep.stageTitles.getOrNull(stageIdx) ?: "",
            starringLine = "",
            body = listOfNotNull(ep.stageNotes.getOrNull(stageIdx)),
            footer = ""
        )
        UiKit.showEpisodeDialog(this, page, "▶ CONTINUE") {
            for (s in 0..stageIdx) ep.actsShown.add(s)
            ep.actPending = -1
            episodeCardShowing = false
            prefs.saveGame(state)
            maybeShowEpisodeCards()
        }.show()
    }

    private fun showWrapCard(ep: EpisodeRecord) {
        episodeCardShowing = true
        SoundEngine.creditsMotif()
        UiKit.showEpisodeDialog(this, Episodes.page(ep, -1),
            "📓 FILE IT IN THE JOURNAL") {
            SoundEngine.questComplete()
            ep.wrapShown = true
            episodeCardShowing = false
            prefs.saveGame(state)
            maybeShowEpisodeCards()
        }.show()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // ---------------- player actions ----------------
    private fun onLook() {
        bg.execute {
            actionTick()
            val me = engine.player(state)
            var text = engine.playerLook(state, currentSeason(), engine.activeHoliday)
            val week = (state.gameDay + (state.gameMonth - 1) * 31) / 7
            val gen = try {
                VisitorSystem.presentAt(me.location, state.playerInside,
                    state.todayKey, state.timeMin, week, dayOfWeek(state)) +
                    // v0.10.0a: the family reads in Look too
                    Family.presentAt(state, me.location, state.playerInside)
            } catch (e: Exception) { emptyList() }
            if (gen.isNotEmpty()) {
                text += "\nAlso here: " + gen.joinToString(", ") { g ->
                    "${g.persona.loreName}${if (g.persona.resident) " (local)" else " (visiting)"}"
                } + "."
            }
            val unsearched = containersAt(me.location, state.playerInside)
                .count { engine.trashAvailable(state, it.id) }
            if (unsearched > 0) text += "\n🗑️ There's trash worth searching here ($unsearched container${if (unsearched > 1) "s" else ""})."
            // v0.1.9a #62/#93: the chalk and the mitten tree read in Look too
            if (TownLife.chalkActive(state, me.location)) {
                text += "\n✏️ Chalk on the sidewalk: " + TownLife.chalkShown(state, me.location)
                    .joinToString(", ") { TownLife.CHALK_DOODLES[it] } + "."
            }
            if (me.location == "KidsPark" && TownLife.mittensHung(state) > 0) {
                text += "\n🌲 The mitten tree wears ${TownLife.mittensHung(state)} lost mittens."
            }
            // v0.2.0f: the case leaves physical evidence in a few
            // places — the first careful Look at each finds it.
            Mystery.lookClueText(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.3.0f: the booth case's evidence works the same
            // way (the Bijou gives up two pieces, one per Look).
            Mystery2.lookClueText2(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.4.0f: the buried case's finds work the same way
            // again — the dig sites (and one City Hall file) give
            // up their objects one careful Look at a time.
            Mystery3.lookClueText3(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.6.0c: the gardener case's evidence works the
            // same way — the fixes themselves, the posted
            // minutes, the parts — one careful Look at a time.
            Mystery4.lookClueText4(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.7.0f: the confession case's evidence, the same
            // way a fifth time — rivets, a dredged pond, a
            // record book, a snagged cloth, a fake appraised.
            Mystery5.lookClueText5(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.8.0f: the opinion case's evidence, the same way
            // a sixth time — spared handprints, ladder dents, a
            // box of charcoal, a tin, a patrol log in pencil.
            Mystery6.lookClueText6(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.9.0f: the backwards case's evidence, the same
            // way a seventh time — a sign-in book, an unsent
            // reply, a ledger read whole, a request slip, and
            // a day-book line that doesn't make sense yet.
            Mystery7.lookClueText7(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.10.0f: the alibi case's evidence, the same
            // way an eighth time — a faded log strip, one set
            // of tracks, a photo with a lazy clock, a smudged
            // slip, and a bicycle bell that sounds like a truck.
            Mystery8.lookClueText8(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.11.0f: the audit case's evidence, the same
            // way a ninth time — an old cup, a carbon form,
            // an invoice with a margin note, an open-backed
            // box, and a face the roll stepped over.
            Mystery9.lookClueText9(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.12.0f: the photo case's evidence, the same
            // way a tenth time — a card behind a portrait, a
            // stamp on a print's back, a marquee in a frame,
            // a two-line book entry, a mount card's weather.
            Mystery10.lookClueText10(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.13.0f: the voice case's evidence, the same
            // way an eleventh time — a draft under a fair
            // copy, a notebook of muffle marks, a form in a
            // tray, a folder of drafts, a fridge door.
            Mystery11.lookClueText11(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.14.0f: the returns case's evidence, the same
            // way a twelfth time — two cards on a spike, a
            // copybook on a shelf, a lunchbox in the pond mud,
            // an empty slot in a soft case, a spike audited
            // whole against the roster.
            Mystery12.lookClueText12(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.15.0j: the exhibition case's evidence, the
            // same way a thirteenth time — a log book's first
            // page, a hang list, a ladder rail, a sleeve in
            // a drawer, the backs of six frames.
            Mystery13.lookClueText13(state, me.location)?.let { clueText ->
                text += "\n$clueText"
            }
            // v0.8.0f: and the pressure is part of the place now —
            // at a hardened meter, the wall and the schoolyard
            // say what the town is doing, in chalk and seating.
            Mystery6.pressureLookText6(state, me.location)?.let { pressureText ->
                text += "\n$pressureText"
            }
            // v0.6.0c: and a standing fix is part of the place
            // now — the bench solid, the fountain running, the
            // rope sound — permanently, in the Look text too.
            Mystery4.fixLookText(state, me.location)?.let { fixText ->
                text += "\n$fixText"
            }
            // v0.5.0c: two Pocket Pals hide at named spots for a
            // careful Look — they never come from the machine.
            PocketPals.lookText(state, me.location)?.let { palText ->
                text += "\n$palText"
            }
            // v0.4.0b: a FIND errand's lost thing is HERE, if
            // you're standing where it was lost — a careful
            // Look recovers it and the errand settles itself.
            Errands.recoverFind(state, me.location)?.let { comp ->
                val lostName = itemById(comp.errand.itemId ?: "")?.name ?: "lost thing"
                text += "\n📋 FOUND IT — ${comp.errand.asker}'s lost $lostName, " +
                    "right where lost things end up. You'll swing it by theirs; " +
                    "consider it returned. (+${comp.reward.friendship} friendship" +
                    (if (comp.reward.coins > 0) ", +${comp.reward.coins} 🪙" else "") + ")" +
                    Errands.chainToastSuffix(comp.reward)
            }
            // v0.4.0d: a holiday beat may be hiding in this Look
            // — a porch on the trick-or-treat route, a shamrock,
            // the countdown itself. The judge speaks in the text.
            HolidayBeats.lookText(state, me.location)?.let { beatText ->
                text += "\n$beatText"
            }
            // v0.5.0h: a season finale's inspection beat may be
            // hiding in this Look too — a suspicious feed bag, a
            // fleet on a dock, a second vine going into a barn.
            Finales.lookText(state, me.location)?.let { finText ->
                text += "\n$finText"
            }
            // v0.6.0d: a family evening's participation beat
            // may be hiding in this Look — lend a hand at
            // theirs, or bring out the spread at yours.
            Dinners.lookText(state, me.location)?.let { dinText ->
                text += "\n$dinText"
            }
            // v0.10.0a: your own table's last beat hides in the
            // same Look — the wash-up, once both ends of the
            // table have talked.
            Family.lookText(state, me.location)?.let { famText ->
                text += "\n$famText"
            }
            // v0.11.0c: the sleepover's movie beat hides in
            // the same Look — the TV, the album, the couch
            // full of sleeping bags.
            Sleepovers.lookText(state, me.location)?.let { slpText ->
                text += "\n$slpText"
            }
            // v0.12.0c: a birthday party's cake beat hides in
            // the same Look — the table, the candles, the
            // cake you brought (or didn't, said honestly).
            // While the party stands but the cake isn't up,
            // the house reads as dressed for the night.
            (Birthdays.lookText(state, me.location)
                ?: Birthdays.venueText(state, me.location)
                )?.let { bdayText ->
                text += "\n$bdayText"
            }
            // v0.14.0d: YOUR birthday's cake beat hides in
            // the very same Look — your house, your table,
            // the cake you brought (or Mom's improvisation,
            // said honestly). While the party stands but
            // the cake isn't up, the house reads as dressed.
            (PlayerBirthday.lookText(state, me.location)
                ?: PlayerBirthday.venueText(state, me.location)
                )?.let { pbText ->
                text += "\n$pbText"
            }
            // v0.14.0e: the clubhouse's project beat hides
            // in the same Look — at the crew's seasonal
            // spot in hangout hours, the careful Look IS
            // the afternoon's small work. While the
            // hangout stands but the project isn't up, the
            // spot reads as claimed.
            (Clubhouse.lookText(state, me.location)
                ?: Clubhouse.venueText(state, me.location)
                )?.let { clubText ->
                text += "\n$clubText"
            }
            // v0.10.0b: a patrol spot on a patrol night may
            // hold the night's small crime — the careful Look
            // settles it (the lost-and-found discipline: the
            // Look IS the hero work).
            Heroes.lookText(state, me.location)?.let { heroText ->
                text += "\n$heroText"
            }
            // v0.6.0e: a live shift's LOOK task may be hiding
            // in this Look — a paper for the mailbox, the
            // rows, the shelves, the till.
            Jobs.lookText(state, me.location)?.let { jobText ->
                text += "\n$jobText"
            }
            // v0.15.0h: an apprenticeship's deep-work stage
            // or its judgment may be hiding in this Look —
            // the rows, the shelves, the trays — and a
            // program's site reads its standing traces here
            // (an offer waiting, your next step, the framed
            // card a master leaves behind).
            Apprenticeships.lookText(state, me.location)?.let { apprText ->
                text += "\n$apprText"
            }
            // v0.5.0d: an armed faction LOOK beat may be hiding
            // in this Look too — a census wall, a posted list,
            // a labeled box (two of them keep office hours).
            FactionMissions.lookText(state, me.location)?.let { missionText ->
                text += "\n$missionText"
            }
            // v0.8.0b: an armed capstone LOOK beat (a hymn
            // board, a bell housing, a pew at dusk) — and the
            // standing traces speak here: a finished capstone's
            // mark names itself to a careful Look.
            Capstones.lookText(state, me.location)?.let { capstoneText ->
                text += "\n$capstoneText"
            }
            // v0.6.0f: 🗳 what the town voted into being here —
            // a standing outcome's trace, or (at City Hall) the
            // week's question and your ballot's state.
            Votes.lookText(state, me.location)?.let { voteText ->
                text += "\n$voteText"
            }
            // v0.7.0b: 🔎 a town secret may be hiding in this
            // Look — the right place, the right time, once.
            // With its gate shut, the spot is ordinary scenery
            // and this simply never speaks.
            Secrets.lookText(state, me.location)?.let { secretText ->
                text += "\n$secretText"
            }
            // v0.7.0c: 🎬 the Bijou's marquee — tonight's
            // feature, the dark night's honest reason, or
            // the next showing being lettered up.
            MovieNights.lookText(state, me.location)?.let { movieText ->
                text += "\n$movieText"
            }
            // v0.7.0e: 🎬 a live climax advertises itself on
            // its turf — and a careful Look at the right spot
            // turns up the thing a FIND beat is missing.
            Climaxes.lookText(state, me.location)?.let { climaxText ->
                text += "\n$climaxText"
            }
            // v0.7.0d: 🎓 the Elementary's own business — the
            // board inside (today's lesson, the bee, quiet
            // days), the yard's recess beat outside.
            School.lookText(state, me.location)?.let { schoolText ->
                text += "\n$schoolText"
            }
            // v0.10.0c: 🎭 the play's business on the same
            // board — the poster, the cast list, rehearsal
            // in earshot, opening night, the pinned
            // playbill after (Play.lookText).
            Play.lookText(state, me.location)?.let { playText ->
                text += "\n$playText"
            }
            // v0.13.0b: 💃 the dance's business on the same
            // board — the marked wall, the poster, the
            // dressed gym on the night, the banner's
            // aftermath (Dance.lookText).
            Dance.lookText(state, me.location)?.let { danceText ->
                text += "\n$danceText"
            }
            // v0.14.0b: 🚌 the field trip's business — the
            // poster at the Elementary, the dressed site
            // on the day (FieldTrip.lookText).
            FieldTrip.lookText(state, me.location)?.let { tripText ->
                text += "\n$tripText"
            }
            // v0.13.0c: 🎤 the talent show's business at the
            // Bijou — the poster in signup week, the lit
            // stage on the night, the annotated poster's
            // echo after (Talent.lookText).
            Talent.lookText(state, me.location)?.let { talentText ->
                text += "\n$talentText"
            }
            // v0.14.0c: 🎬 the festival's business at the
            // Bijou — the second poster's program in
            // announcement week, tonight's entry on a
            // festival day (Festival.lookText). On a
            // festival day it follows the regular
            // marquee's dark line, and says why.
            Festival.lookText(state, me.location)?.let { festText ->
                text += "\n$festText"
            }
            // v0.15.0f: 🖼 the gallery show's business at
            // City Hall — the poster in the submission
            // window, hanging night, the hung wall, the
            // champion between shows (Gallery.lookText).
            Gallery.lookText(state, me.location)?.let { galText ->
                text += "\n$galText"
            }
            // v0.13.0d: 🎺 the parade's business on Main
            // Street — the poster on the reviewing stand,
            // the dressed and closed street on the day,
            // the confetti echo after (Parade.lookText).
            Parade.lookText(state, me.location)?.let { paradeText ->
                text += "\n$paradeText"
            }
            // v0.8.0e: 💢 a rival's prank may be waiting in
            // this Look — signed, cosmetic, and answered in
            // person (all rules in Rival.lookText).
            Rival.lookText(state, me.location)?.let { rivalText ->
                text += "\n$rivalText"
            }
            // v0.9.0c: 🎡 the county fair — the poster before
            // the week, the running story during it, the
            // fishing morning at the pond (Fair.lookText).
            Fair.lookText(state, me.location)?.let { fairText ->
                text += "\n$fairText"
            }
            // v0.11.0e: ❄️ the winter carnival — the poster
            // before the week, the running story during it,
            // at the pond (Carnival.lookText).
            Carnival.lookText(state, me.location)?.let { carnText ->
                text += "\n$carnText"
            }
            // v0.3.0e: 🌠 on a meteor night, Looking at the pond
            // makes a wish (once a night; the Journal counts).
            engine.pondWish(state)?.let { wishText ->
                text += "\n$wishText"
            }
            // #61 SNOWBALL STASH: a snowy day stocks your pockets
            if (state.weatherId in setOf("light_snow", "heavy_snow", "blizzard") &&
                state.life["snowDay"] != state.absDay) {
                state.life["snowDay"] = state.absDay
                state.life["snowballs"] = 3
                text += "\n❄️ You pack 3 snowballs. For emergencies. (Tap a kid to deploy one.)"
            }
            // #64 LOST & FOUND: claim today's lost thing at City Hall
            if (me.location == "CityHall" && !state.playerInside &&
                state.lifeText["lostClaimed"] != state.todayKey) {
                TownLife.lostToday(state)?.let { (owner, lostItem, _) ->
                    state.inventory[lostItem] = (state.inventory[lostItem] ?: 0) + 1
                    state.lifeText["lostClaimed"] = state.todayKey
                    text += "\n📦 You take the ${itemById(lostItem)?.name} from the box. " +
                        "Returning it to $owner would make their whole day."
                }
            }
            // #95 THE FENCE FEUD: a friend of the house can mediate
            state.lifeText["feud"]?.let { feud ->
                val sides = feud.split("|")
                if (me.location == sides.getOrNull(0) || me.location == sides.getOrNull(1)) {
                    val owner = KID_HOUSE.entries.firstOrNull { it.value == me.location }?.key
                    val pts = owner?.let { state.friendship[it] ?: 0 } ?: 0
                    if (owner != null &&
                        Relationships.tierOf(pts) >= Relationships.FRIEND) {
                        state.lifeText.remove("feud")
                        townLogLine(state, "🧱 You mediated the great fence feud")
                        text += "\n🧱 You talk both sides down over cocoa. " +
                            "The fence feud ends — $owner shakes on it."
                    } else {
                        text += "\n🧱 The fence feud rages here. " +
                            "A friend of the house could mediate…"
                    }
                }
            }
            text += LoreBook.lookSuffix(me.location)
            prefs.saveGame(state)
            runOnUiThread {
                lookDialog(text)
                // #100: the park's soft dirt holds the capsule talk
                if (me.location == "KidsPark" && !state.playerInside) capsuleDialog()
                refreshUi()
            }
        }
    }

    private fun lookDialog(text: String) {
        // v0.1.9b: paper dialog kit (was a stock AlertDialog) — the
        // scroll is height-capped so long Looks still scroll inside
        // the parchment panel instead of pushing it off-screen.
        val tv = TextView(this).apply {
            this.text = text
            setTextColor(Color.parseColor("#1a1a1a"))
            textSize = 15f
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        val scroll = ScrollView(this).apply { addView(tv) }
        scroll.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.55f).toInt())
        UiKit.showMessageDialog(this, "👀 Look around", content = scroll,
            positiveLabel = "OK")
    }

    private fun onTalkKid(name: String) {
        bg.execute {
            val kid = state.agents[name] ?: return@execute
            if (kid.asleep) {
                val line = SLEEPY_LINES[name]?.randomOrNull() ?: "Zzz..."
                runOnUiThread { sleepyDialog(name, line) }
                return@execute
            }
            // v0.2.0e SNOWBALL SHOWDOWN: at the two grounds, tapping
            // a cast kid offers the formal duel alongside everything
            // else. Snow-only — and the menu SAYS so when it isn't.
            val meLoc = engine.player(state).location
            // v0.3.0d THE DOORSTEP: nobody is ever inside your
            // house but you — a friend standing at YourHouse is
            // at your DOOR (the haunt bend brings them; the goal
            // was you, not your living room), and answering it
            // gets the knock, once per visitor per day.
            if (!state.playerInside && meLoc == YOUR_HOUSE &&
                state.life["knock:$name"] != state.absDay) {
                state.life["knock:$name"] = state.absDay
                state.events.add(House.knockLine(name))
                while (state.events.size > 40) state.events.removeAt(0)
                prefs.saveGame(state)
                runOnUiThread { toast(House.knockLine(name)) }
            }
            if (name in CharacterSystem.CAST_KIDS &&
                meLoc in Minigames.SHOWDOWN_LOCS && !state.playerInside) {
                val snowingNow = state.weatherId in Minigames.SNOW_WEATHERS
                val packed = (state.life["snowballs"] ?: 0) > 0
                val opts = mutableListOf("💬 Talk")
                // v0.9.0b: the crew ask rides this menu too —
                // the grounds are where you'd ask, snow or not
                val crewOpt = Crew.menuOption(state, name)
                if (crewOpt != null) opts.add(crewOpt)
                // v0.13.0b: the dance ask rides this menu
                // too — the grounds are where you'd ask
                val danceOpt = Dance.menuOption(state, name)
                if (danceOpt != null) opts.add(danceOpt)
                // v0.9.0c: the fair's business rides this menu
                // too — the fairground IS showdown ground, and
                // the tank/errands happen between the same kids
                val fairOpt = Fair.menuOption(state, name)
                if (fairOpt != null) opts.add(fairOpt)
                if (packed && snowingNow) {
                    opts.add("❄️ Throw a snowball (${state.life["snowballs"]} left)")
                }
                opts.add(if (snowingNow) "🏆 Snowball Showdown"
                    else "🏆 Snowball Showdown (needs snow)")
                runOnUiThread {
                    UiKit.showItemsDialog(this, name, opts.toTypedArray()) { which ->
                        when {
                            which == 0 -> maybeGiftFirst(name) { openTalkSheet(name) }
                            opts[which] == crewOpt -> crewAsk(name)
                            opts[which] == danceOpt -> danceAsk(name)
                            opts[which] == fairOpt -> fairAct(name, fairOpt)
                            opts[which].startsWith("❄️") -> throwSnowball(name)
                            snowingNow -> startShowdown(name)
                            else -> UiKit.showMessageDialog(this, "🏆 A Matter of Snow",
                                message = "A Snowball Showdown requires snowfall — " +
                                    "it's basically the whole sport. Come back when " +
                                    "the sky is doing its part, and bring a kid " +
                                    "to lose to you.")
                        }
                    }
                }
                return@execute
            }
            // #61 SNOWBALL STASH: armed and in snow country, tapping
            // a kid asks the important question first.
            if ((state.life["snowballs"] ?: 0) > 0 &&
                state.weatherId in setOf("light_snow", "heavy_snow", "blizzard")) {
                runOnUiThread {
                    UiKit.showItemsDialog(this, name,
                        arrayOf("💬 Talk",
                            "❄️ Throw a snowball (${state.life["snowballs"]} left)")) { which ->
                        if (which == 0) {
                            maybeGiftFirst(name) { openTalkSheet(name) }
                        } else {
                            throwSnowball(name)
                        }
                    }
                }
                return@execute
            }
            runOnUiThread { maybeGiftFirst(name) { openTalkSheet(name) } }
        }
    }

    /** #61: friends laugh it off; rivals declare war. */
    private fun throwSnowball(name: String) {
        SoundEngine.snowball()
        bg.execute {
            state.life["snowballs"] = (state.life["snowballs"] ?: 1) - 1
            val pts = state.friendship[name] ?: 0
            val friendly = Relationships.tierOf(pts) >= Relationships.FRIEND
            if (friendly) {
                engine.relAdjust(state, name, 2)
            } else {
                engine.relAdjust(state, name, -3)
                state.events.add("❄️ $name declares SNOW WAR on ${engine.player(state).name}!")
                while (state.events.size > 40) state.events.removeAt(0)
            }
            actionTick()
            prefs.saveGame(state)
            runOnUiThread {
                toast(if (friendly) "❄️ WHAP. $name laughs, brushing off snow. Worth it."
                    else "❄️ WHAP. $name's eyes narrow. WAR. War has been declared.")
                refreshUi()
            }
        }
    }

    /** If the player carries anything giftable, offer Talk vs Gift first. */
    private fun giftableItems(): List<ItemDef> =
        state.inventory.entries.filter { it.value > 0 }
            .mapNotNull { itemById(it.key) }

    private fun maybeGiftFirst(npcName: String, talk: () -> Unit) {
        val items = giftableItems()
        // v0.5.0c: a Pocket Pals trade joins the Talk sheet when
        // one is on the table (PocketPals.tradeOffer: their
        // weekly stash vs your dupes — never your last copy).
        val offer = PocketPals.tradeOffer(state, npcName)
        // v0.9.0b: the crew ask joins the same menu — the
        // in-talk choice, not a rail: inviting is the player's
        // initiative. The option only appears when there's
        // something to ask (Crew.menuOption owns the rules:
        // Friend tier to invite, free to leave, full and
        // left-today stated on the label itself).
        val crewOpt = Crew.menuOption(state, npcName)
        // v0.9.0c: the fair's option joins the same menu —
        // the ribbon-cutting, the booth, the tank, the fair
        // errands are all player-initiative business at the
        // fairground, like the crew ask (Fair.menuOption
        // owns the rules; labels say what they do).
        val fairOpt = Fair.menuOption(state, npcName)
        // v0.11.0e: the carnival's option joins the same
        // menu — the first hole is player-initiative
        // business at the pond, like the ribbon-cutting
        // (Carnival.menuOption owns the rules).
        val carnOpt = Carnival.menuOption(state, npcName)
        // v0.9.0d: a framed print joins the gift flow the
        // same way — an enlargement from the darkroom box,
        // giftable to this person (Darkroom.giftablePrints
        // owns the rules: enlargements only, and never a
        // photo they've already been given).
        val printGifts = Darkroom.giftablePrints(state, npcName)
        // v0.10.0b: the hero's person-menu business joins the
        // same way — on a patrol night, someone out after
        // dark can be escorted home (Heroes.menuOption owns
        // the rules; the walk is priced by the Walk graph).
        val heroOpt = Heroes.menuOption(state, npcName)
        // v0.11.0c: the sleepover plan joins the same menu —
        // asking a kid in your crew about Friday night is
        // the player's initiative, like the crew ask
        // (Sleepovers.menuOption owns who sees it: crew
        // kids only — grown-ups don't do sleepovers).
        val slpOpt = Sleepovers.menuOption(state, npcName)
        // v0.13.0b: the dance ask joins the same menu — the
        // player's initiative, like the crew ask, never a
        // rail: asking somebody to the dance is something
        // you do in person, in the asking week (Dance
        // .menuOption owns the rules: the Friend tier to
        // ask for real, a grown-up stated on the label, a
        // final answer removing the option entirely).
        val danceOpt = Dance.menuOption(state, npcName)
        // v0.14.0b: the field trip's business joins the
        // same menu — on trip day at the site, the
        // chaperone runs the beat, the photograph and
        // the bus back from her person menu, the player's
        // initiative like the crew ask (FieldTrip
        // .menuOption owns the rules and the day's order).
        val tripOpt = FieldTrip.menuOption(state, npcName)
        if (items.isEmpty() && offer == null && crewOpt == null &&
            fairOpt == null && carnOpt == null && printGifts.isEmpty() &&
            heroOpt == null && slpOpt == null && danceOpt == null &&
            tripOpt == null) {
            talk()
            return
        }
        val opts = mutableListOf("💬 Talk")
        if (items.isNotEmpty()) opts += "🎁 Give a gift…"
        if (printGifts.isNotEmpty()) opts += "🖼 Give a framed print…"
        if (offer != null) opts += "🃏 Trade Pocket Pals"
        if (crewOpt != null) opts += crewOpt
        if (fairOpt != null) opts += fairOpt
        if (carnOpt != null) opts += carnOpt
        if (heroOpt != null) opts += heroOpt
        if (slpOpt != null) opts += slpOpt
        if (danceOpt != null) opts += danceOpt
        if (tripOpt != null) opts += tripOpt
        UiKit.showItemsDialog(this, npcName, opts.toTypedArray()) { which ->
            when (opts.getOrNull(which)) {
                "🎁 Give a gift…" -> pickGift(npcName, items)
                "🖼 Give a framed print…" -> pickPrintGift(npcName, printGifts)
                "🃏 Trade Pocket Pals" -> tradeDialog(npcName)
                crewOpt -> crewAsk(npcName)
                fairOpt -> fairAct(npcName, fairOpt)
                carnOpt -> carnivalAct(npcName, carnOpt)
                heroOpt -> heroAct(npcName)
                slpOpt -> sleepoverDialog()
                danceOpt -> danceAsk(npcName)
                tripOpt -> tripAct(opts[which])
                else -> talk()
            }
        }
    }

    /** v0.14.0b: one of the field trip's chaperone-menu
     *  actions, by its label: the played beat (presented
     *  beat by beat, in the lesson's idiom), the class
     *  photograph, or the bus back. FieldTrip owns every
     *  rule; this just routes. */
    private fun tripAct(opt: String) {
        when {
            opt.startsWith(FieldTrip.OPT_PHOTO) -> {
                bg.execute {
                    val text = FieldTrip.takePhoto(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "📸 The Class Photo",
                            message = text)
                        else toast("📸 No frame to take — " +
                            "the moment's moved on.")
                    }
                }
            }
            opt.startsWith(FieldTrip.OPT_BACK) -> {
                bg.execute {
                    val text = FieldTrip.boardBack(state)
                    if (text != null) actionTick()
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "🚌 The Ride Back",
                            message = text)
                        else toast("🚌 That bus has gone — " +
                            "the road home is yours now.")
                    }
                }
            }
            else -> {
                bg.execute {
                    val ok = FieldTrip.startBeat(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        if (ok) presentTripBeat()
                        else toast("🚌 That beat's gone — " +
                            "the day's moved on.")
                    }
                }
            }
        }
    }

    /** The trip beat, presented in the lesson's idiom:
     *  text steps advance, prompt steps answer off the
     *  choice row, and the outcome reads the day back. */
    private fun presentTripBeat() {
        val beat = FieldTrip.beatBeat(state) ?: run {
            refreshUi()
            return
        }
        when (beat.kind) {
            "prompt" -> {
                UiKit.showMessageDialog(this, "🚌 ${beat.speaker}",
                    beat.text, positiveLabel = "✋ Your moment",
                    onPositive = {
                        val opts = FieldTrip.beatOptions(state)
                        UiKit.showItemsDialog(this, "🚌 ${beat.speaker}",
                            opts.map { it.second }.toTypedArray()) { which ->
                            bg.execute {
                                FieldTrip.answerBeat(state,
                                    opts[which].first)
                                actionTick()
                                prefs.saveGame(state)
                                runOnUiThread { presentTripBeat() }
                            }
                        }
                    })
            }
            else -> {
                UiKit.showMessageDialog(this, "🚌 ${beat.speaker}",
                    beat.text, positiveLabel = "▶",
                    onPositive = {
                        bg.execute {
                            FieldTrip.advanceBeat(state)
                            prefs.saveGame(state)
                            runOnUiThread {
                                if (FieldTrip.beatBeat(state) != null)
                                    presentTripBeat()
                                else refreshUi()
                            }
                        }
                    })
            }
        }
    }

    /** v0.10.0b: the escort — a hero walks someone home
     *  through the dark, on the real clock: the minutes are
     *  the Walk graph's own price for the route, spent tick
     *  by tick like every honest journey in town. */
    private fun heroAct(npcName: String) {
        bg.execute {
            val minutes = Heroes.escortMinutes(state, npcName)
            val line = Heroes.escort(state, npcName)
            if (line != null) {
                repeat(minutes / 15) { engine.advanceTime(state) }
                state.lastConversation = listOf(
                    Line("🦸 Hero Work", line))
            }
            prefs.saveGame(state)
            runOnUiThread {
                refreshUi()
                if (line == null) {
                    toast("The escort's off — the night moved on.")
                } else {
                    UiKit.showMessageDialog(this, "🦸 Escorted",
                        message = line)
                }
            }
        }
    }

    /** v0.9.0d: pick which framed enlargement this person
     *  gets. The judgment is Darkroom's (in the frame pays
     *  by the photo's real fields; not in the frame is a
     *  polite dud and the print comes home with you). */
    private fun pickPrintGift(npcName: String,
                              prints: List<DarkroomPrint>) {
        val labels = prints.map { p ->
            "${Darkroom.shortLabel(p)} — ${Darkroom.captionOf(p)}"
        }
        UiKit.showItemsDialog(this, "🖼 A framed print for $npcName",
            labels.toTypedArray()) { which ->
            prints.getOrNull(which)?.let { givePrint(npcName, it) }
        }
    }

    private fun givePrint(npcName: String, print: DarkroomPrint) {
        bg.execute {
            val res = Darkroom.givePrint(state, npcName, print.id)
            // they keep it with the day's gifts, like any
            // gift the memory store remembers
            if (res != null && res.given) {
                NpcMemoryStore.recordGift(npcName,
                    "a framed print you developed", res.loved,
                    state.friendship[npcName] ?: 0,
                    state.todayKey, state.timeMin,
                    engine.player(state).name)
            }
            prefs.saveGame(state)
            runOnUiThread {
                refreshUi()
                if (res == null) {
                    toast("That print's already spoken for.")
                } else {
                    UiKit.showMessageDialog(this,
                        "🖼 For $npcName", message = res.line)
                }
            }
        }
    }

    /** v0.9.0c: the fair's person-menu business. The ribbon
     *  is a played beat (a dialog and a mark on the fair's
     *  record); the booth is a tiny shop; the tank opens
     *  the dunk view; a fair errand accepts onto the plate
     *  through Errands' own funnels. All rules in Fair. */
    private fun fairAct(npcName: String, opt: String?) {
        if (Emergencies.momentOwned(state)) {
            UiKit.showMessageDialog(this, "🎡 The Fair Waits",
                message = "A rescue is unfolding right now — the " +
                    "fair keeps. It has waited all year; it can " +
                    "wait out an emergency.")
            return
        }
        when {
            opt == Fair.OPT_RIBBON -> bg.execute {
                val text = Fair.cutRibbon(state)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this,
                        "🎀 The ${state.gameYear} County Fair Is Open",
                        message = text.ifEmpty {
                            "The ribbon is already cut — the fair " +
                                "is open, and the Mayor is telling " +
                                "the story like she cut it herself."
                        })
                }
            }
            opt == Fair.OPT_BOOTH -> fairBoothDialog()
            opt == Fair.OPT_DUNK -> startDunkTank(npcName)
            opt != null && opt.startsWith(Fair.OPT_ERRAND_PREFIX) -> {
                val sc = Fair.errandOffer(state, npcName)
                if (sc == null) {
                    UiKit.showMessageDialog(this, "🎡 The Fair",
                        message = "That ask has already been " +
                            "taken care of — the fair moves fast.")
                    return
                }
                bg.execute {
                    val text = Fair.acceptErrand(state, sc)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        UiKit.showMessageDialog(this,
                            "🎡 Fair errand — ${sc.asker}",
                            message = if (text != null)
                                "“$text”\n\n(It's on your plate now — " +
                                    "the Journal tracks it like any " +
                                    "errand, 🎡 and all.)"
                            else "Your plate's full — finish an " +
                                "errand or two and ask again while " +
                                "the fair's still up.")
                    }
                }
            }
        }
    }

    /** v0.11.0e: the carnival's person-menu business. The
     *  first hole is a played beat (a dialog and a mark on
     *  the carnival's record). All rules in Carnival. */
    private fun carnivalAct(npcName: String, opt: String?) {
        if (Emergencies.momentOwned(state)) {
            UiKit.showMessageDialog(this, "❄️ The Carnival Waits",
                message = "A rescue is unfolding right now — the " +
                    "carnival keeps. It has waited all winter; it " +
                    "can wait out an emergency.")
            return
        }
        if (opt == Carnival.OPT_HOLE) {
            bg.execute {
                val text = Carnival.drillFirstHole(state)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this,
                        "🕳️ The ${state.gameYear} Winter Carnival Is Open",
                        message = text.ifEmpty {
                            "The first hole is already drilled — " +
                                "the carnival is open, and Jimbo " +
                                "is telling the story like the " +
                                "auger drilled itself."
                        })
                }
            }
        }
    }

    /** 🎡 The snack booth: the pantry's own food at honest
     *  fair prices, paid in capsule coins. */
    private fun fairBoothDialog() {
        val coins = Minigames.capsuleCoins(state)
        val labels = Fair.BOOTH.map { (id, price) ->
            val item = itemById(id)
            "${item?.icon ?: "🎡"} ${item?.name ?: id} — $price 🪙"
        } + "🎡 Just looking (you have $coins 🪙)"
        UiKit.showItemsDialog(this,
            "🎡 Fair Snack Booth — Liane presiding", labels.toTypedArray()) { which ->
            if (which < Fair.BOOTH.size) {
                bg.execute {
                    val line = Fair.buyBooth(state, Fair.BOOTH[which].first)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        UiKit.showMessageDialog(this, "🎡 The Booth",
                            message = line ?: "Not enough 🪙 capsule " +
                                "coins — the games pay them, and the " +
                                "booth is patient. Liane is not, " +
                                "but the booth is.")
                    }
                }
            }
        }
    }

    /** 🌱 The garden centre's seed rack (v0.12.0b): seed
     *  packets as real inventory, at the crops' exact
     *  stated prices — Shop owns the till, Garden owns
     *  the packets. One packet a tap, like the fair
     *  booth; the rack re-opens after every tap so a
     *  season's worth is a few taps, not a form. */
    private fun seedCounterDialog() {
        val coins = Minigames.capsuleCoins(state)
        val labels = Garden.CROPS.map { crop ->
            val held = Garden.packetsHeld(state, crop.id)
            "${crop.icon} ${crop.name} seed packet — " +
                "${crop.seedPrice} 🪙 (${crop.windowLabel})" +
                if (held > 0) " · pack: $held" else ""
        } + "🚶 Done (you have $coins 🪙)"
        UiKit.showItemsDialog(this,
            "🌱 The Mall Garden Centre — the seed rack",
            labels.toTypedArray()) { which ->
            val crop = Garden.CROPS.getOrNull(which)
                ?: return@showItemsDialog
            bg.execute {
                val r = Shop.buySeed(state, crop.id, "Mall")
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this,
                        "🌱 The Seed Rack", message = r.line,
                        onPositive = { seedCounterDialog() })
                }
            }
        }
    }

    /** 🧺 The snack shop's general-goods counter
     *  (v0.12.0b): the stand list's four pantry staples
     *  at the shops' own prices, into the real pantry —
     *  Shop owns the till. The title carries the till's
     *  book: your lifetime spend at this counter. */
    private fun goodsCounterDialog() {
        val coins = Minigames.capsuleCoins(state)
        val labels = Shop.STAPLE_IDS.map { id ->
            val item = itemById(id)
            val held = state.inventory[id] ?: 0
            "${item?.icon ?: "🧺"} ${item?.name ?: id} — " +
                "${Shop.staplePrice(id)} 🪙" +
                if (held > 0) " · pantry: $held" else ""
        } + "🚶 Done (you have $coins 🪙)"
        UiKit.showItemsDialog(this,
            "🧺 The Mall Snack Shop — general goods " +
                "(spent here, all told: " +
                "${Shop.goodsSpent(state)} 🪙)",
            labels.toTypedArray()) { which ->
            val id = Shop.STAPLE_IDS.getOrNull(which)
                ?: return@showItemsDialog
            bg.execute {
                val r = Shop.buyStaple(state, id, "Mall")
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this,
                        "🧺 The Snack Shop", message = r.line,
                        onPositive = { goodsCounterDialog() })
                }
            }
        }
    }

    /** 💰 Your stand (v0.10.0e): the pitch-side dialog.
     *  Every rule lives in Stand — setup and its City Hall
     *  permit, moving, opening and staffing (you, present,
     *  or a crewmate on the real Walk route), ordering
     *  stock at the shops' real prices, prices inside the
     *  band, and the books. This shell plays the answers. */
    /** 🌱 The garden plot (v0.11.0b): the four beds, the
     *  watering can and the weed fork. Every beat is
     *  priced in minutes on the real clock, spent tick by
     *  tick like every honest home beat (the hero-escort
     *  pattern); Garden owns every rule behind the lines. */
    /** v0.12.0a: the porch copy of the Gazette — the same
     *  frozen edition the Journal holds, read on your own
     *  step. Read-only: the paper is the record's, the
     *  porch is just where your copy lands. */
    /** 🪜 The attic (v0.12.0e): the upstairs fixture at
     *  Your House — three derived shelves (the overflow
     *  shelf, the family frame's outtakes, the stored
     *  case boxes) read off the house's own records by
     *  Attic.pageLines, fresh on every climb. Read-only:
     *  nothing up there is bought, sold, or stored. */
    private fun atticDialog() {
        bg.execute {
            val text = Attic.pageLines(state).joinToString("\n")
            runOnUiThread { lookDialog(text) }
        }
    }

    private fun paperDialog() {
        bg.execute {
            val lines = Newspaper.porchLines(state)
            val text = if (lines == null)
                "📰 No paper on the porch yet — the first " +
                    "edition prints at the turn of the first " +
                    "week you live in town."
            else
                "📰 On the porch, weighted down with a smooth " +
                    "stone against the mountain wind: this " +
                    "week's Gazette.\n\n" +
                    lines.joinToString("\n")
            runOnUiThread { lookDialog(text) }
        }
    }

    /** v0.13.0e: the mailbox — the pen-pal program's front
     *  door. Read the latest letter; write back in one of
     *  three tones (the letter composes in front of you
     *  from your real records, then sends on your word —
     *  15 real minutes at the box); or open the Journal's
     *  📬 page for the whole correspondence. PenPals owns
     *  every rule; this only runs the errand. */
    private var pendingPalTone: String? = null

    private fun mailboxDialog() {
        if (!PenPals.isSignedUp(state)) {
            lookDialog("📭 Your mailbox — THE NEW KID, says " +
                "the plaque your dad nailed under it the week " +
                "he took the Post Office route.\n\nNothing " +
                "personal in it today. The Elementary's " +
                "pen-pal program could change that: the " +
                "signup box sits by the classroom board " +
                "every October ${PenPals.SIGNUP_FROM}–" +
                "${PenPals.SIGNUP_TO}." +
                if (PenPals.signupOpen(state))
                    "\n\nThe box is out RIGHT NOW."
                else "")
            return
        }
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        if (pendingPalTone != null) {
            labels += "📮 Send this letter " +
                "(${PenPals.REPLY_MINUTES} min at the box)"
            actions += { sendPalReply() }
        }
        if (PenPals.unread(state)) {
            labels += "📬 Read Wren's latest letter"
            actions += {
                bg.execute {
                    val text = PenPals.latestLetter(state)
                    PenPals.markRead(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "✉️ From ${PenPals.PAL_TOWN}",
                            message = text)
                    }
                }
            }
        }
        if (PenPals.canReply(state)) {
            labels += if (PenPals.isDormant(state))
                "✍️ Write to Wren — reopen the route " +
                    "(she's waiting on YOU)"
            else "✍️ Write back — pick a tone"
            actions += { palToneDialog() }
        }
        // v0.15.0b: the return trip's hosted beats run
        // through the mailbox too — the correspondence's
        // front door, in Pinewood as at home
        val tripBeat = PenPals.tripBeatToday(state)
        if (tripBeat != null) {
            labels += "✈️ ${PenPals.PAL_TOWN}, day " +
                "${PenPals.tripDayIndex(state) + 1}: " +
                PenPals.tripBeatLabel(tripBeat)
            actions += { openTripSheet(tripBeat) }
        } else if (PenPals.tripLive(state)) {
            labels += "✈️ ${PenPals.PAL_TOWN} — talk " +
                "with ${PenPals.PAL_FIRST} (today's " +
                "beat is kept)"
            actions += { openTripSheet(null) }
        }
        labels += "📬 The correspondence so far (Journal)"
        actions += {
            prefs.saveGame(state)
            startActivity(Intent(this, JournalActivity::class.java)
                .putExtra("section", "penpals"))
        }
        labels += "🚶 Walk away"
        actions += { }
        UiKit.showItemsDialog(this,
            "📬 Your Mailbox — ${PenPals.PAL_FIRST} of " +
                PenPals.PAL_TOWN + ", " +
                PenPals.bandWord(state),
            labels.toTypedArray()) { which -> actions[which]() }
    }

    private fun palToneDialog() {
        val labels = PenPals.TONES.map { tone ->
            PenPals.toneLabel(tone) +
                if (pendingPalTone == tone) " — composed ✓ (tap to re-read)"
                else ""
        }
        UiKit.showItemsDialog(this,
            "✍️ Your letter to ${PenPals.PAL_FIRST} — " +
                "composed from your real week",
            labels.toTypedArray()) { which ->
            val tone = PenPals.TONES[which]
            pendingPalTone = tone
            val (text, _) = PenPals.composeReply(state, tone)
            UiKit.showMessageDialog(this,
                "✉️ Your letter — ${PenPals.toneLabel(tone)}",
                message = text + "\n\n— composed from your " +
                    "own records, word for word what will " +
                    "ride the mail. Send it from the mailbox.")
        }
    }

    private fun sendPalReply() {
        val tone = pendingPalTone ?: return
        bg.execute {
            val sent = PenPals.sendReply(state, tone)
            if (sent != null) engine.advanceTime(state)
            prefs.saveGame(state)
            runOnUiThread {
                pendingPalTone = null
                refreshUi()
                if (sent != null) {
                    toast("📮 Sent — via your dad's Post " +
                        "Office, to ${PenPals.PAL_TOWN}. " +
                        "Kept on the 📬 page.")
                    UiKit.showMessageDialog(this,
                        "✉️ In the mail", message = sent)
                } else toast("✉️ The route's closed for " +
                    "that letter — check the 📬 page.")
            }
        }
    }

    private fun gardenDialog() {
        val labels = mutableListOf<String>()
        for (i in 0 until Garden.BEDS)
            labels += Garden.bedLine(state, i)
        if (Garden.anyPlanted(state)) {
            labels += "💧 Water the garden — " +
                "${Garden.WATER_MINUTES} min"
            labels += "🧹 Clear the weeds — " +
                "${Garden.CLEAR_MINUTES} min"
        }
        labels += "📖 The garden page"
        labels += "🚶 Walk away"
        UiKit.showItemsDialog(this,
            "🌱 Your Garden — 🪙 " +
                "${Minigames.capsuleCoins(state)}",
            labels.toTypedArray()) { which ->
            when {
                which in 0 until Garden.BEDS ->
                    gardenBedDialog(which)
                labels.getOrNull(which) ==
                    "💧 Water the garden — " +
                        "${Garden.WATER_MINUTES} min" ->
                    bg.execute {
                        val line = Garden.water(state)
                        repeat(Garden.WATER_MINUTES / 15) {
                            engine.advanceTime(state) }
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            UiKit.showMessageDialog(this,
                                "🌱 Watering", message = line,
                                onPositive = { gardenDialog() })
                        }
                    }
                labels.getOrNull(which) ==
                    "🧹 Clear the weeds — " +
                        "${Garden.CLEAR_MINUTES} min" ->
                    bg.execute {
                        val line = Garden.clearWeeds(state)
                        repeat(Garden.CLEAR_MINUTES / 15) {
                            engine.advanceTime(state) }
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            UiKit.showMessageDialog(this,
                                "🌱 Weeding", message = line,
                                onPositive = { gardenDialog() })
                        }
                    }
                labels.getOrNull(which) ==
                    "📖 The garden page" ->
                    lookDialog(Garden.pageLines(state)
                        .joinToString("\n"))
            }
        }
    }

    /** One bed's work: plant a crop (the seed picker —
     *  a packet from your pack when you carry one, else
     *  priced from the Mall garden centre's rack), or
     *  harvest what's ripe. Out-of-window crops are
     *  listed honestly — Garden.plant itself says why
     *  not, in the packet's own voice. */
    private fun gardenBedDialog(i: Int) {
        val b = Garden.bed(state, i)
        when {
            b != null && b.ripe && !b.dead -> {
                val crop = Garden.cropById(b.cropId)
                UiKit.showItemsDialog(this,
                    "🌱 Bed ${i + 1} — ripe",
                    arrayOf(
                        "🧺 Harvest the " +
                            "${crop?.name?.lowercase() ?: b.cropId} " +
                            "— ${Garden.HARVEST_MINUTES} min",
                        "🚶 Back")) { which ->
                    if (which == 0) bg.execute {
                        val line = Garden.harvest(state, i)
                        repeat(Garden.HARVEST_MINUTES / 15) {
                            engine.advanceTime(state) }
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            UiKit.showMessageDialog(this,
                                "🌱 Harvest", message = line,
                                onPositive = { gardenDialog() })
                        }
                    } else gardenDialog()
                }
            }
            b != null && !b.dead ->
                lookDialog(Garden.bedLine(state, i) +
                    "\n\nIt's growing. Water it on dry " +
                    "days, clear its weeds weekly, and let " +
                    "the sky do its part — the garden page " +
                    "keeps the whole record.")
            else -> {
                val labels = Garden.CROPS.map { crop ->
                    val held = Garden.packetsHeld(state, crop.id)
                    if (held > 0)
                        "${crop.icon} Plant ${crop.name} — from " +
                            "your packet (pack: $held; " +
                            "${crop.windowLabel}, ${crop.growDays} " +
                            "days" +
                            (if (crop.tender) ", tender"
                             else ", hardy") +
                            ")"
                    else
                        "${crop.icon} Plant ${crop.name} — seed " +
                            "${crop.seedPrice} 🪙 (${crop.seedSupplier}; " +
                            "${crop.windowLabel}, ${crop.growDays} " +
                            "days" +
                            (if (crop.tender) ", tender" else ", hardy") +
                            ")"
                } + "🚶 Back"
                UiKit.showItemsDialog(this,
                    "🌱 Bed ${i + 1} — " +
                        (if (b?.dead == true)
                            "dead; planting clears it" else
                            "bare dirt") +
                        " — 🪙 ${Minigames.capsuleCoins(state)}",
                    labels.toTypedArray()) { which ->
                    val crop = Garden.CROPS.getOrNull(which)
                    if (crop != null) bg.execute {
                        val line = Garden.plant(state, i, crop.id)
                        val planted = Garden.bed(state, i)
                            ?.let { it.cropId == crop.id &&
                                it.plantedDay == state.absDay &&
                                !it.dead } == true
                        if (planted)
                            repeat(Garden.PLANT_MINUTES / 15) {
                                engine.advanceTime(state) }
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            UiKit.showMessageDialog(this,
                                "🌱 Planting", message = line,
                                onPositive = { gardenDialog() })
                        }
                    } else gardenDialog()
                }
            }
        }
    }

    private fun standDialog() {
        val locId = engine.player(state).location
        val has = Stand.hasStand(state)
        val here = Stand.locOf(state) == locId
        val labels = mutableListOf<String>()
        when {
            !has -> {
                labels += "🧾 Set up your stand here — permit " +
                    "${Stand.PERMIT_COST} 🪙 + kit " +
                    "${Stand.KIT_COST} 🪙"
                labels += "📖 How the stand works"
            }
            !here -> {
                labels += "🚚 Move your stand here — " +
                    "${Stand.MOVE_COST} 🪙 pitch fee"
                labels += "💰 The books"
            }
            else -> {
                if (Stand.isOpen(state))
                    labels += "🔴 Close up"
                else {
                    labels += "🟢 Open it — work it yourself"
                    labels += "👥 Open it — a crewmate works it…"
                }
                labels += "📦 Order stock…"
                labels += "🌱 Stock homegrown produce…"
                labels += "🏷 Set prices…"
                labels += "💰 The books"
            }
        }
        labels += "🚶 Walk away"
        val status = when {
            !has -> "A good pitch, chalked and permitted " +
                "for a stand. Yours, if you file the permit."
            !here -> "Your stand is at " +
                "${LOCATIONS[Stand.locOf(state)]?.name}. " +
                "This pitch is chalked and empty."
            Stand.staffedBy(state) == "you" ->
                "Open — you're working it. It sells while " +
                    "you stand here."
            Stand.staffedBy(state) != null ->
                "Open — ${Stand.staffedBy(state)} is " +
                    "working it."
            else -> Stand.closedReason(state)
        }
        UiKit.showItemsDialog(this,
            "💰 Your Stand — $status",
            labels.toTypedArray()) { which ->
            when (labels.getOrNull(which)) {
                "🧾 Set up your stand here — permit " +
                    "${Stand.PERMIT_COST} 🪙 + kit " +
                    "${Stand.KIT_COST} 🪙" -> bg.execute {
                    val line = Stand.setup(state, locId)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        UiKit.showMessageDialog(this,
                            "💰 The Stand", message = line)
                    }
                }
                "🚚 Move your stand here — " +
                    "${Stand.MOVE_COST} 🪙 pitch fee" ->
                    bg.execute {
                        val line = Stand.move(state, locId)
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            UiKit.showMessageDialog(this,
                                "💰 The Stand", message = line)
                        }
                    }
                "📖 How the stand works" ->
                    lookDialog(Stand.pageLines(state)
                        .joinToString("\n"))
                "💰 The books" ->
                    lookDialog(Stand.pageLines(state)
                        .joinToString("\n"))
                "🔴 Close up" -> bg.execute {
                    val line = Stand.closeStandLine(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        toast("💰 $line")
                    }
                }
                "🟢 Open it — work it yourself" -> bg.execute {
                    val line = Stand.openStand(state, null)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        UiKit.showMessageDialog(this,
                            "💰 Open", message = line)
                    }
                }
                "👥 Open it — a crewmate works it…" ->
                    standCrewDialog()
                "📦 Order stock…" -> standStockDialog()
                "🌱 Stock homegrown produce…" ->
                    standGardenDialog()
                "🏷 Set prices…" -> standPriceDialog()
            }
        }
    }

    /** Pick the crewmate who works the stand — the label
     *  carries their honest availability (Crew.busyReason
     *  is the oracle) and the real walk over. */
    private fun standCrewDialog() {
        val members = Crew.members(state)
        if (members.isEmpty()) {
            UiKit.showMessageDialog(this, "💰 No crew yet",
                message = "The stand is staffed by you or " +
                    "your crew — and the crew's an empty " +
                    "chair right now. Invite a friend from " +
                    "their talk menu first (Friend tier).")
            return
        }
        val labels = members.map { name ->
            val busy = Crew.busyReason(state, name)
            if (busy != null) "👥 $name — can't ($busy)"
            else {
                val from = state.agents[name]?.location
                val mins = if (from != null)
                    Walk.routeMinutes(state, from,
                        Stand.locOf(state) ?: from) else 0
                "👥 $name — walks over ($mins min), works it"
            }
        } + "🚶 Back"
        UiKit.showItemsDialog(this,
            "💰 Who works the stand?", labels.toTypedArray()) {
                which ->
            val name = members.getOrNull(which)
            if (name != null) bg.execute {
                val line = Stand.openStand(state, name)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this, "💰 Open",
                        message = line)
                }
            }
        }
    }

    /** Order stock, four at a tap, at the shops' own
     *  prices — the label shows the crate, today's order
     *  count against the shops' daily cap, and the shelf
     *  life the books will hold it to. */
    private fun standStockDialog() {
        val labels = Stand.STOCK.map { def ->
            val item = itemById(def.itemId)
            "${item?.icon ?: "📦"} +4 ${item?.name} — " +
                "${def.shopPrice * 4} 🪙 from " +
                "${def.supplier} (crate " +
                "${Stand.onHand(state, def.itemId)}, " +
                "ordered ${Stand.orderedToday(state,
                    def.itemId)}/${Stand.ORDER_CAP} today, " +
                "keeps ${def.shelfDays} days)"
        } + "🚶 Back"
        UiKit.showItemsDialog(this,
            "💰 Order stock — your 🪙: " +
                "${Minigames.capsuleCoins(state)}",
            labels.toTypedArray()) { which ->
            val def = Stand.STOCK.getOrNull(which)
            if (def != null) bg.execute {
                val line = Stand.order(state, def.itemId, 4)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this, "💰 Stock",
                        message = line,
                        onPositive = { standStockDialog() })
                }
            }
        }
    }

    /** Stock the crate from the garden (v0.11.0b): real
     *  items out of the pack, only as many as the garden
     *  provably grew — the label says both numbers, so the
     *  provenance rule is visible, not hidden. */
    private fun standGardenDialog() {
        val labels = Stand.PRODUCE.map { def ->
            val item = itemById(def.itemId)
            val grown = Garden.grownAvailable(state, def.itemId)
            val held = state.inventory[def.itemId] ?: 0
            "${item?.icon ?: "🌱"} Stock all ${item?.name} — " +
                "grown $grown · in pack $held (reference " +
                "${def.shopPrice} 🪙, keeps ${def.shelfDays} " +
                "days)"
        } + "🚶 Back"
        UiKit.showItemsDialog(this,
            "💰 From the garden — the crate takes homegrown only",
            labels.toTypedArray()) { which ->
            val def = Stand.PRODUCE.getOrNull(which)
            if (def != null) bg.execute {
                val n = Garden.grownAvailable(state, def.itemId)
                val line = Stand.stockFromGarden(state,
                    def.itemId, n)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this, "💰 Stock",
                        message = line,
                        onPositive = { standGardenDialog() })
                }
            }
        }
    }

    /** Set a price inside the stated band — the picker
     *  only offers band prices, and Stand.setPrice
     *  refuses anything outside it regardless. */
    private fun standPriceDialog() {
        val labels = Stand.ALL_DEFS.map { def ->
            val item = itemById(def.itemId)
            "${item?.icon ?: "📦"} ${item?.name} — now " +
                "${Stand.priceOf(state, def.itemId)} 🪙 " +
                "(" + (if (Stand.isProduce(def.itemId))
                    "garden reference " else "shop ") +
                "${def.shopPrice} 🪙, band " +
                "${Stand.minPrice(def.itemId)}–" +
                "${Stand.maxPrice(def.itemId)})"
        } + "🚶 Back"
        UiKit.showItemsDialog(this, "💰 Set prices",
            labels.toTypedArray()) { which ->
            val def = Stand.ALL_DEFS.getOrNull(which)
            if (def != null) {
                val item = itemById(def.itemId)
                val prices =
                    (Stand.minPrice(def.itemId)..
                        Stand.maxPrice(def.itemId)).toList()
                val plabels = prices.map { p ->
                    "🏷 ${item?.name} at $p 🪙" +
                        if (p == Stand.priceOf(state,
                                def.itemId)) " (current)" else ""
                } + "🚶 Back"
                UiKit.showItemsDialog(this,
                    "💰 ${item?.name} — " +
                        (if (Stand.isProduce(def.itemId))
                            "garden reference " else
                            "shop price ") +
                        "${def.shopPrice} 🪙",
                    plabels.toTypedArray()) { pw ->
                    val p = prices.getOrNull(pw)
                    if (p != null) bg.execute {
                        Stand.setPrice(state, def.itemId, p)
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            toast("💰 ${item?.name} now " +
                                "$p 🪙 at the stand.")
                            standPriceDialog()
                        }
                    }
                }
            }
        }
    }

    /** 🎯 The dunk tank: your three throws, really thrown —
     *  the showdown's bar against the sitter's own dodge.
     *  The bracket settles on closing night (Fair.settleDunk). */
    private fun startDunkTank(sitter: String) {
        val session = Fair.DunkSession(System.nanoTime(), sitter)
        val view = DunkTankView(this)
        view.session = session
        view.onDunkOver = { s ->
            Fair.recordDunk(state, s.hits, s.powerMilli)
            prefs.saveGame(state)
            refreshUi()
            listOf(
                if (s.hits > 0)
                    "🎯 ${s.hits} of ${Fair.DUNK_THROWS} — $sitter " +
                        "goes in the water, to enormous applause."
                else
                    "🎯 The tank wins this round — $sitter stays " +
                        "dry and completely insufferable about it.",
                "The bracket settles on closing night — the " +
                    "Journal's 🎡 COUNTY FAIR page keeps the places.")
        }
        gameDialog("🎯 Dunk Tank",
            "three throws at $sitter · release in the zones — " +
                "the better the zone, the better the throw; " +
                "the wetter the sitter, the better the fair",
            view, 400, onShow = { view.begin() }, onHide = { view.stop() })
    }

    /** 🛏️ v0.11.0c: THE PILLOW FIGHT — the sleepover's
     *  played beat, launched from the Talk rail against
     *  the guest you tapped: three swings on the showdown
     *  bar (Sleepovers.PillowSession owns the math against
     *  the guest's own dodge), and the judging lands beat
     *  2 through the module, once, when the view closes
     *  the session. The couch is the prize. */
    private fun startPillowFight(guest: String) {
        val session = Sleepovers.PillowSession(
            Sleepovers.pillowSeed(state, guest), guest)
        val view = PillowFightView(this)
        view.session = session
        view.onPillowOver = { s ->
            val hit = Sleepovers.judgePillowFight(state, s)
            prefs.saveGame(state)
            refreshUi()
            listOf(
                hit?.text ?: "🛏️ The fight happened. History " +
                    "is still deciding how it went.",
                "(${Sleepovers.beatsLanded(state)} of " +
                    "${Sleepovers.STEP_COUNT} beats of the " +
                    "night in — lights out when you're ready.)")
        }
        gameDialog("🛏️ Pillow Fight",
            "three swings at $guest · release in the zones — " +
                "take two of three and the couch is yours; " +
                "their dodge is very, very real",
            view, 400, onShow = { view.begin() }, onHide = { view.stop() })
    }

    /** 💃 v0.13.0b: THE SLOW DANCE — the dance's played
     *  beat, launched from the gym's board on the night:
     *  four phrases on the showdown bar (Dance.DanceSession
     *  owns the math — your partner's nerve is literally
     *  your margin; stag, the spotlight is the partner),
     *  and the judging lands beat 2 through the module,
     *  once, when the view closes the session. */
    private fun startSlowDance() {
        val rec = Dance.currentRecord(state) ?: return
        val session = Dance.DanceSession(
            Dance.danceSeed(state), rec.date)
        val view = SlowDanceView(this)
        view.session = session
        view.onDanceOver = { s ->
            val text = Dance.judgeSlowDance(state, s)
            prefs.saveGame(state)
            refreshUi()
            listOf(
                text ?: "🎵 The song happened. History is " +
                    "still deciding how it went.",
                "(The night's beats live on the gym's " +
                    "board — the last song is the one that " +
                    "closes it.)")
        }
        val who = if (rec.date.isEmpty())
            "alone, under the spotlight — stag, on purpose"
        else "with ${rec.date} — their nerve is your margin"
        gameDialog("🎵 The Slow Dance",
            "four phrases · tap to step, release in the " +
                "zones — steady zones, steady feet · $who",
            view, 400, onShow = { view.begin() },
            onHide = { view.stop() })
    }

    /** v0.9.0b: the crew ask itself — invite, leave, or the
     *  honest explanation of why not (full / left today /
     *  not friends yet). All rules live in Crew; this shell
     *  plays the answer and opens the talk after, the way
     *  every other person-menu action lands in conversation. */
    private fun crewAsk(npcName: String) {
        bg.execute {
            val member = Crew.isMember(state, npcName)
            val block = if (member) null else Crew.inviteBlock(state, npcName)
            val title: String
            val text: String
            when {
                member -> {
                    title = "👥 $npcName steps out"
                    text = Crew.leave(state, npcName)
                }
                block == null -> {
                    title = "👥 $npcName joins the crew!"
                    text = Crew.invite(state, npcName) +
                        "\n\n(They'll come find you when you're out — " +
                        "on foot, at walking speed, when their own " +
                        "life allows. The crew's on the Journal's " +
                        "👥 YOUR CREW page.)"
                }
                block == "full" -> {
                    title = "👥 The crew's full"
                    text = "Four's a crew; five's a parade permit. " +
                        "Ask somebody to step out first — leaving " +
                        "is always free."
                }
                block == "leftToday" -> {
                    title = "👥 Not today"
                    text = "$npcName left the crew today. Asking " +
                        "again tomorrow is the rule — crews run on " +
                        "not being weird about it."
                }
                else -> {
                    title = "👥 Not yet"
                    text = Crew.notYetLine(state, npcName)
                }
            }
            prefs.saveGame(state)
            runOnUiThread {
                refreshUi()
                UiKit.showMessageDialog(this, title,
                    message = text,
                    positiveLabel = "OK",
                    onPositive = { openTalkSheetNow(npcName) })
            }
        }
    }

    /** v0.13.0b: the dance ask itself — a yes, a kind
     *  final no, a shy "ask me again", or the honest
     *  not-yet. All rules live in Dance; this shell plays
     *  the answer and opens the talk after, the way every
     *  other person-menu action lands in conversation. */
    private fun danceAsk(npcName: String) {
        bg.execute {
            val res = Dance.ask(state, npcName)
            val title = when (res.kind) {
                "yes" -> "💃 $npcName said yes!"
                "no" -> "💃 A kind no"
                "soft" -> "💃 Not yet — ask again"
                "date" -> "💃 Already a plan"
                "stood" -> "💃 That door's closed"
                "adult" -> "💃 A grown-up, asked"
                "tier" -> "💃 Not yet"
                else -> "💃 The dance"
            }
            prefs.saveGame(state)
            runOnUiThread {
                refreshUi()
                UiKit.showMessageDialog(this, title,
                    message = res.text,
                    positiveLabel = "OK",
                    onPositive = { openTalkSheetNow(npcName) })
            }
        }
    }

    /** v0.5.0c: the trade sheet — one-for-one, the cards named,
     *  the friendship swing shown before anyone commits.
     *  Declining is free, and funny (PocketPals.declineLine). */
    private fun tradeDialog(npcName: String) {
        bg.execute {
            val offer = PocketPals.tradeOffer(state, npcName)
            runOnUiThread {
                if (offer == null) {
                    toast("🃏 No trade on the table — not this week, anyway.")
                    return@runOnUiThread
                }
                val giveN = PocketPals.count(state, offer.give)
                UiKit.showItemsDialog(this, "🃏 Pocket Pals — trading with $npcName",
                    arrayOf(
                        "🔁 Trade: your ${offer.give.name} (×$giveN) for " +
                            "their ${offer.get.name} " +
                            "(${if (offer.delta >= 0) "+" else ""}${offer.delta} 💛)",
                        "🚶 Walk away")) { which ->
                    if (which == 0) settleTrade(npcName)
                    else toast("🃏 ${PocketPals.declineLine(npcName, state)}")
                }
            }
        }
    }

    private fun settleTrade(npcName: String) {
        bg.execute {
            val done = PocketPals.executeTrade(state, npcName)
            prefs.saveGame(state)
            runOnUiThread {
                if (done == null) {
                    toast("🃏 The trade fell through — the binder math changed.")
                } else {
                    toast("🃏 TRADED! ${done.give.name} ➜ $npcName · " +
                        "${done.get.name} ➜ your binder (NEW!) " +
                        "(${if (done.delta >= 0) "+" else ""}${done.delta} 💛 $npcName)")
                }
                refreshUi()
            }
        }
    }

    private fun pickGift(npcName: String, items: List<ItemDef>) {
        val labels = items.map { "${it.icon} ${it.name} ×${state.inventory[it.id] ?: 0}" }
            .toTypedArray()
        UiKit.showItemsDialog(this, "Give $npcName…", labels) { which ->
            giveGift(npcName, items[which])
        }
    }

    private fun giveGift(npcName: String, item: ItemDef) {
        bg.execute {
            val me = engine.player(state)
            val memBefore = NpcMemoryStore.get(npcName)
            val loved: Boolean
            val reaction: String
            if (state.agents.containsKey(npcName) && npcName != me.name) {
                // v0.4.0b: if this exact item is what this exact
                // person asked you to fetch, the hand-in IS the
                // gift — the errand judge consumes the pack copy
                // and pays the errand's rewards instead.
                val handIn = Errands.handInFetch(state, npcName, item.id)
                if (handIn != null) {
                    state.lastConversation = listOf(
                        Line(me.name, "Here's the ${item.name} you needed."),
                        Line(npcName, handIn.reward.thanksLine)
                    )
                    SoundEngine.questComplete()
                    NpcMemoryStore.recordGift(npcName, item.name, true,
                        state.friendship[npcName] ?: 0, state.todayKey,
                        state.timeMin, me.name)
                    prefs.saveGame(state)
                    prefs.saveNpcMemory()
                    runOnUiThread {
                        toast("📋 Errand done for $npcName! " +
                            "+${handIn.reward.friendship} friendship" +
                            (if (handIn.reward.coins > 0)
                                ", +${handIn.reward.coins} 🪙" else "") +
                            Errands.chainToastSuffix(handIn.reward) +
                            (handIn.reward.thankItem?.let {
                                " · they pressed a ${it.name} on you in thanks"
                            } ?: "") +
                            (handIn.reward.thankCard?.let {
                                " · 🃏 they pressed a Pocket Pal on you in " +
                                    "thanks: ${it.name}! (Collection 🃏)"
                            } ?: ""))
                        refreshUi()
                    }
                    return@execute
                }
                // v0.4.0d: on Halloween, the good candy brought
                // to a route house's resident IS that house's
                // stop (the HOUSE judge) — the gift itself still
                // lands normally below; generosity, not a toll.
                val beatHit = HolidayBeats.onGive(state, npcName, item.id)
                if (beatHit != null) {
                    runOnUiThread {
                        toast("🎉 ${beatHit.beat.title} — " +
                            if (beatHit.completedNow) "played to the end!"
                            else "step ${HolidayBeats.stepsDone(state, beatHit.beat)}" +
                                " of ${beatHit.beat.steps.size} (the candy counts)")
                    }
                }
                // v0.5.0d: the same hand-off can land an armed
                // faction GIVE beat (a lookout's price, a reserve
                // cake) — the gift itself still lands below.
                val missionHits = FactionMissions.onGive(state, npcName, item.id)
                if (missionHits.isNotEmpty()) {
                    runOnUiThread {
                        for (hit in missionHits) {
                            toast("⚔️ ${hit.chain.title} — ${hit.beat.title}: " +
                                if (hit.chainCompletedNow) "CHAIN COMPLETE!"
                                else "beat ${hit.beat.beat} of 3 landed")
                        }
                    }
                }
                // v0.8.0b: and an armed capstone GIVE beat (the
                // coffee empire's tribute) — same rail.
                val capstoneHits = Capstones.onGive(state, npcName, item.id)
                if (capstoneHits.isNotEmpty()) {
                    runOnUiThread {
                        for (hit in capstoneHits) {
                            toast("🏛 ${hit.chain.title} — ${hit.beat.title}: " +
                                if (hit.capstoneCompletedNow) "CAPSTONE COMPLETE!"
                                else "beat ${hit.beat.beat} of 3 landed")
                        }
                    }
                }
                // cast: engine handles points/tier/faction + inventory
                val msg = engine.giveGift(state, npcName, item.id)
                loved = msg.contains("LOVE")
                reaction = when {
                    // v0.12.0c: on their birthday, the answer
                    // to a landed gift is their authored
                    // birthday line — the day is the occasion,
                    // whatever the gift was.
                    Birthdays.isBirthday(state, npcName) &&
                        !msg.startsWith("You don't") ->
                        Birthdays.reactionFor(npcName)
                    loved -> "No way — a ${item.name}?! " +
                        "You're the best, ${me.name}!"
                    else -> "Oh. A ${item.name}. Thanks, ${me.name}."
                }
                NpcMemoryStore.recordGift(npcName, item.name, loved,
                    state.friendship[npcName] ?: 0, state.todayKey, state.timeMin, me.name)
                // #64 LOST & FOUND payoff: walking a lost thing home
                val lost = TownLife.lostToday(state)
                if (lost != null && lost.first == npcName && lost.second == item.id &&
                    state.lifeText["lostClaimed"] == state.todayKey &&
                    state.lifeText["lostReturned"] != state.todayKey) {
                    state.lifeText["lostReturned"] = state.todayKey
                    engine.relAdjust(state, npcName, 8)
                    TownLife.repAdd(state, 2)
                    runOnUiThread {
                        toast("📦 You returned their lost ${item.name}! (+8 friendship, +rep)")
                    }
                }
                runOnUiThread { toast(msg) }
            } else if (Family.isParent(npcName)) {
                // v0.10.0a: your parent — judged like a generated
                // NPC's gift, but the warmth lands in the family's
                // own points (never state.friendship)
                val persona = Family.persona(state, npcName)
                val tasteTags = persona.traits + persona.loves
                val (pts, lv) = giftValue(item, npcName, tasteTags)
                loved = lv
                Family.adjustPoints(state, npcName, pts)
                val mem = NpcMemoryStore.getOrCreate(npcName)
                mem.points = Family.points(state, npcName)
                reaction = if (loved) "Oh wow, a ${item.name}! How did you know?!"
                    else "A ${item.name}? For me? …Thanks, kiddo."
                state.inventory[item.id] = (state.inventory[item.id] ?: 1) - 1
                if ((state.inventory[item.id] ?: 0) <= 0) state.inventory.remove(item.id)
                state.questCounters["gifts"] = (state.questCounters["gifts"] ?: 0) + 1
                NpcMemoryStore.recordGift(npcName, item.name, loved, mem.points,
                    state.todayKey, state.timeMin, me.name)
            } else if (PenPals.isPal(npcName)) {
                // v0.13.0e: your pen pal, in person — judged
                // in her voice, and the warmth lands in the
                // correspondence's own lane (never
                // state.friendship), like the family's.
                val persona = PenPals.persona(state)
                val tasteTags = persona.traits + persona.loves
                val (pts, lv) = giftValue(item, npcName, tasteTags)
                loved = lv
                PenPals.adjustPoints(state, pts)
                val mem = NpcMemoryStore.getOrCreate(npcName)
                mem.points = PenPals.points(state)
                reaction = if (loved) "A ${item.name}. For me? " +
                    "In PERSON? Five. That's a five."
                else "A ${item.name}. Noted, witnessed, and " +
                    "accepted. The Flats will hear of this."
                state.inventory[item.id] = (state.inventory[item.id] ?: 1) - 1
                if ((state.inventory[item.id] ?: 0) <= 0) state.inventory.remove(item.id)
                state.questCounters["gifts"] = (state.questCounters["gifts"] ?: 0) + 1
                NpcMemoryStore.recordGift(npcName, item.name, loved, mem.points,
                    state.todayKey, state.timeMin, me.name)
            } else {
                // generated lorebook NPC: points live in their character memory
                val persona = VisitorSystem.personaByName(npcName)
                // #23: their derived loves count as tastes, same as traits
                val tasteTags = (persona?.traits ?: emptyList()) +
                    (persona?.loves ?: emptySet())
                val (pts, lv) = giftValue(item, npcName, tasteTags)
                loved = lv
                val mem = NpcMemoryStore.getOrCreate(npcName)
                mem.points = (mem.points + pts).coerceIn(-50, 100)
                reaction = if (loved) "Oh wow, a ${item.name}! How did you know?!"
                    else "A ${item.name}? Um. Thanks, ${me.name}."
                state.inventory[item.id] = (state.inventory[item.id] ?: 1) - 1
                if ((state.inventory[item.id] ?: 0) <= 0) state.inventory.remove(item.id)
                state.questCounters["gifts"] = (state.questCounters["gifts"] ?: 0) + 1
                NpcMemoryStore.recordGift(npcName, item.name, loved, mem.points,
                    state.todayKey, state.timeMin, me.name)
            }
            state.lastConversation = listOf(
                Line(me.name, "Here — I got you a ${item.name}."),
                Line(npcName, reaction)
            )
            if (loved) SoundEngine.gift(loved = true) else SoundEngine.gift(loved = false)
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread { refreshUi() }
        }
    }

    private fun sleepyDialog(kidName: String, line: String) {
        // v0.1.9b: paper dialog kit; the panel provides the parchment
        val tv = TextView(this).apply {
            text = "\"$line\"\n\n😴 ${kidName.split(" ").firstOrNull() ?: kidName} is asleep. " +
                "Come back between 7 AM and 10 PM, dude."
            setTextColor(Color.parseColor("#1a1a1a"))
            textSize = 16f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        UiKit.showMessageDialog(this, kidName, content = tv, positiveLabel = "OK")
    }

    /** v0.4.0b: every road into a conversation passes this gate
     *  first, because Talk is an errand verb: a parcel for this
     *  person completes on contact, and a pending ask — from
     *  their current goal, or a shopkeeper at their counter —
     *  is offered before the sheet opens. Neither fires twice
     *  in a day: an offered ask is marked, taken or not. */
    private fun openTalkSheet(kidName: String) {
        // v0.4.0g: which rail this Talk rides is
        // TalkGate.gateFor's decision (pure, suite-pinned
        // under fabricated contention) — a delivery settles
        // first (it masks a beat step, as ever), then the
        // today-only holiday beat step (v0.4.0d: ahead of a
        // fresh errand ask, because the beat is today-only
        // and the errand will still be there tomorrow), then
        // the ask. This function only plays the named rail;
        // the judges fire inside bg, as before.
        val gate = TalkGate.gateFor(state, kidName,
            engine.player(state).location)
        val delivery = gate.delivery
        if (gate.rail == TalkGate.Rail.DIALOGUE) {
            // v0.15.0h: apprenticeship business rides below
            // the gate whole — an offer, a stage, or the
            // standing talk with your mentor — judged by
            // its own module when no rail claimed the talk
            // (the gate keeps its eleven rails and never
            // learns the module's name). An offer and the
            // standing talk ask their questions kindly,
            // like a job's application and notice; a stage
            // judges like a beat and shows its text.
            val apprStep = Apprenticeships.talkStepFor(state,
                kidName, engine.player(state).location)
            if (apprStep != null) {
                val prog = apprStep.prog
                when (apprStep.kind) {
                    "OFFER" -> runOnUiThread {
                        val body = TextView(this).apply {
                            text = "“${prog.offerText}”\n\n" +
                                "🎓 ${prog.chainTitle} — five " +
                                "stages over the better part of " +
                                "two weeks, one apprenticeship " +
                                "at a time. Stipends on the day; " +
                                "the ${prog.skill} skill and a " +
                                "signed mark at the end."
                            setTextColor(Color.parseColor(Chrome.INK))
                            textSize = 15f
                        }
                        UiKit.paperDialog(this, apprStep.title, body,
                            positive = "Take the apprenticeship" to {
                                bg.execute {
                                    val hit = Apprenticeships
                                        .judgeTalk(state, kidName,
                                            engine.player(state).location)
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        if (hit == null) {
                                            openTalkSheetNow(kidName)
                                        } else {
                                            UiKit.showMessageDialog(this,
                                                hit.title,
                                                message = hit.text,
                                                positiveLabel = "OK",
                                                onPositive = {
                                                    openTalkSheetNow(kidName) })
                                        }
                                    }
                                }
                            },
                            negative = "Not now" to {
                                bg.execute {
                                    val hit = Apprenticeships
                                        .judgeTalk(state, kidName,
                                            engine.player(state).location,
                                            "DECLINE")
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        if (hit == null) {
                                            openTalkSheetNow(kidName)
                                        } else {
                                            UiKit.showMessageDialog(this,
                                                hit.title,
                                                message = hit.text,
                                                positiveLabel = "OK",
                                                onPositive = {
                                                    openTalkSheetNow(kidName) })
                                        }
                                    }
                                }
                            }).show()
                    }
                    "STATUS" -> runOnUiThread {
                        val body = TextView(this).apply {
                            text = apprStep.text
                            setTextColor(Color.parseColor(Chrome.INK))
                            textSize = 15f
                        }
                        UiKit.paperDialog(this, apprStep.title, body,
                            positive = "Keep at it" to {
                                openTalkSheetNow(kidName) },
                            negative = "End the apprenticeship" to {
                                bg.execute {
                                    val hit = Apprenticeships
                                        .judgeTalk(state, kidName,
                                            engine.player(state).location,
                                            "QUIT")
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        UiKit.showMessageDialog(this,
                                            hit?.title
                                                ?: "🎓 ${prog.chainTitle} — ended",
                                            message = hit?.text
                                                ?: "The apprenticeship is ended.",
                                            positiveLabel = "OK",
                                            onPositive = {
                                                openTalkSheetNow(kidName) })
                                    }
                                }
                            }).show()
                    }
                    else -> bg.execute {
                        val hit = Apprenticeships.judgeTalk(state,
                            kidName, engine.player(state).location)
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            if (hit == null) {
                                openTalkSheetNow(kidName)
                            } else {
                                UiKit.showMessageDialog(this, hit.title,
                                    message = hit.text,
                                    positiveLabel = "OK",
                                    onPositive = {
                                        openTalkSheetNow(kidName) })
                            }
                        }
                    }
                }
                return
            }
            openTalkSheetNow(kidName)
            return
        }
        if (gate.rail == TalkGate.Rail.HOLIDAY_BEAT) {
            bg.execute {
                val hit = HolidayBeats.judgeTalk(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (hit == null) {
                        openTalkSheetNow(kidName)
                    } else {
                        val hName = HolidayBeats.holidayOf(state)?.name
                            ?: "Holiday"
                        val body = hit.text +
                            (hit.completionText?.let { "\n\n$it" }
                                ?: "\n\n(${HolidayBeats.stepsDone(state, hit.beat)}" +
                                    " of ${hit.beat.steps.size} steps of " +
                                    "“${hit.beat.title}”)")
                        UiKit.showMessageDialog(this,
                            "🎉 $hName — ${hit.beat.title}",
                            message = body,
                            positiveLabel = "OK",
                            onPositive = { openTalkSheetNow(kidName) })
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.CLIMAX) {
            // v0.7.0e: the event's climax, played as a
            // set-piece — its chain of judged beats runs
            // through whichever causer you tapped, in the
            // Climaxes module's own clothing.
            climaxDialog()
            return
        }
        if (gate.rail == TalkGate.Rail.FINALE) {
            // v0.5.0h: a season finale's live beat — judged
            // like a holiday beat step, in its own week's
            // clothing.
            bg.execute {
                val hit = Finales.judgeTalk(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (hit == null) {
                        openTalkSheetNow(kidName)
                    } else {
                        val w = Finales.windowAt(state)
                        val body = hit.text +
                            if (hit.allLanded)
                                "\n\n(Every beat of “${hit.def.title}” is caught — " +
                                    "it all comes down to the last day.)"
                            else
                                "\n\n(${Finales.stepsDone(state, hit.def, w?.instanceYear ?: state.gameYear)}" +
                                    " beats of “${hit.def.title}” caught — " +
                                    "the week isn't over.)"
                        UiKit.showMessageDialog(this,
                            "${hit.def.emoji} ${hit.def.seasonName} Finale — ${hit.def.title}",
                            message = body,
                            positiveLabel = "OK",
                            onPositive = { openTalkSheetNow(kidName) })
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.DINNER && gate.sleepHit != null) {
            // v0.11.0c: a sleepover beat rides the DINNER
            // rail — judged by its own module; the pillow
            // fight (step 2) opens its played view instead
            // of a judged text, because it is played.
            if (gate.sleepHit.stepIndex == 2) {
                startPillowFight(kidName)
            } else {
                bg.execute {
                    val hit = Sleepovers.judgeTalk(state, kidName)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (hit == null) {
                            openTalkSheetNow(kidName)
                        } else {
                            val body = hit.text +
                                if (hit.allLanded)
                                    "\n\n(Every beat of the sleepover is in — " +
                                        "whoever's still awake is lying about it.)"
                                else
                                    "\n\n(${Sleepovers.beatsLanded(state)} of " +
                                        "${Sleepovers.STEP_COUNT} beats of " +
                                        "the night in.)"
                            UiKit.showMessageDialog(this,
                                "🛌 Sleepover — " +
                                    Sleepovers.STEP_TITLES[hit.stepIndex],
                                message = body,
                                positiveLabel = "OK",
                                onPositive = { openTalkSheetNow(kidName) })
                        }
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.DINNER && gate.bdayHit != null) {
            // v0.14.0d: your OWN birthday's party beat rides
            // the DINNER rail — judged by its own module and
            // shown in the evening's own clothing, like the
            // cast party below.
            bg.execute {
                val hit = PlayerBirthday.judgeTalk(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (hit == null) {
                        openTalkSheetNow(kidName)
                    } else {
                        val body = hit.text +
                            if (hit.allLanded)
                                "\n\n(Every beat of the party is in — " +
                                    "whatever's left is cake and " +
                                    "paper crowns.)"
                            else
                                "\n\n(${PlayerBirthday.stepsDone(state)} " +
                                    "of ${PlayerBirthday.STEP_COUNT} beats " +
                                    "of the party in.)"
                        UiKit.showMessageDialog(this,
                            "🎂 Your Birthday — " +
                                PlayerBirthday.STEP_TITLES[hit.stepIndex],
                            message = body,
                            positiveLabel = "OK",
                            onPositive = { openTalkSheetNow(kidName) })
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.DINNER && gate.partyHit != null) {
            // v0.12.0c: a birthday party beat rides the
            // DINNER rail — judged by its own module and
            // shown in the evening's own clothing, like
            // the dinner beat below.
            bg.execute {
                val hit = Birthdays.judgeTalk(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (hit == null) {
                        openTalkSheetNow(kidName)
                    } else {
                        val body = hit.text +
                            if (hit.allLanded)
                                "\n\n(Every beat of the party is in — " +
                                    "whatever's left is cake and " +
                                    "paper crowns.)"
                            else
                                "\n\n(${Birthdays.stepsDone(state,
                                    hit.householdId)} " +
                                    "of ${Birthdays.STEP_COUNT} beats " +
                                    "of the party in.)"
                        UiKit.showMessageDialog(this,
                            "🎂 Birthday Party — ${hit.family}: " +
                                Birthdays.STEP_TITLES[hit.stepIndex],
                            message = body,
                            positiveLabel = "OK",
                            onPositive = { openTalkSheetNow(kidName) })
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.DINNER) {
            // v0.6.0d: a family evening's live beat — the
            // welcome or the table talk, judged like a finale
            // beat, in the evening's own clothing.
            bg.execute {
                val hit = Dinners.judgeTalk(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (hit == null) {
                        openTalkSheetNow(kidName)
                    } else {
                        val titles = if (hit.hosted) Dinners.HOSTED_TITLES
                            else Dinners.INVITED_TITLES
                        val done = if (hit.hosted) Dinners.hostedStepsDone(state)
                            else Dinners.invitedStepsDone(state)
                        val body = hit.text +
                            if (hit.allLanded)
                                "\n\n(Every beat of the evening is in — " +
                                    "whatever's left is just dessert and dishes.)"
                            else
                                "\n\n($done of ${Dinners.STEP_COUNT} beats of " +
                                    "the evening in.)"
                        UiKit.showMessageDialog(this,
                            "🍽 ${if (hit.hosted) "Hosting" else "Dinner"} " +
                                "${hit.def.family} — ${titles[hit.stepIndex]}",
                            message = body,
                            positiveLabel = "OK",
                            onPositive = { openTalkSheetNow(kidName) })
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.WORK) {
            // v0.6.0e: a town job's business. An application
            // is a hiring conversation (take it or leave it);
            // a refusal is the boss's word, shown once a day;
            // giving notice asks once, kindly; the rest —
            // clock-in, a task, clock-out — judge like a
            // dinner beat and show their text.
            val step = gate.jobStep!!
            val meLoc = engine.player(state).location
            if (step.kind == "APPLY" && !step.refused) {
                runOnUiThread {
                    val body = TextView(this).apply {
                        text = "“${step.def.applyText}”\n\n" +
                            "${step.def.emoji} ${step.def.title} — " +
                            "${step.def.daysLabel}. One job at a time; " +
                            "the week's pay lands on payday."
                        setTextColor(Color.parseColor(Chrome.INK))
                        textSize = 15f
                    }
                    UiKit.paperDialog(this,
                        "${step.def.emoji} ${step.def.title} — the offer",
                        body,
                        positive = "Take the job" to {
                            bg.execute {
                                val hit = Jobs.judgeTalk(state, kidName, meLoc)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    refreshUi()
                                    if (hit == null) {
                                        openTalkSheetNow(kidName)
                                    } else {
                                        UiKit.showMessageDialog(this,
                                            "${step.def.emoji} Hired — ${step.def.title}",
                                            message = hit.text,
                                            positiveLabel = "OK",
                                            onPositive = { openTalkSheetNow(kidName) })
                                    }
                                }
                            }
                        },
                        negative = "Not now" to { openTalkSheetNow(kidName) }).show()
                }
                return
            }
            if (step.kind == "QUIT") {
                runOnUiThread {
                    UiKit.paperDialog(this,
                        "${step.def.emoji} Give notice?",
                        TextView(this).apply {
                            text = "Give notice at the ${step.def.title}? " +
                                "The week's tab still pays out on payday. " +
                                "The door stays open — bosses in this town " +
                                "rehire without a grudge."
                            setTextColor(Color.parseColor(Chrome.INK))
                            textSize = 15f
                        },
                        positive = "Give notice" to {
                            bg.execute {
                                val hit = Jobs.judgeTalk(state, kidName, meLoc)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    refreshUi()
                                    UiKit.showMessageDialog(this,
                                        "${step.def.emoji} Notice given",
                                        message = hit?.text
                                            ?: "Notice given.",
                                        positiveLabel = "OK",
                                        onPositive = { openTalkSheetNow(kidName) })
                                }
                            }
                        },
                        negative = "Keep the job" to { openTalkSheetNow(kidName) }).show()
                }
                return
            }
            bg.execute {
                val hit = Jobs.judgeTalk(state, kidName, meLoc)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (hit == null) {
                        openTalkSheetNow(kidName)
                    } else {
                        val title = when (hit.kind) {
                            "APPLY" -> "${step.def.emoji} ${step.def.title} — not today"
                            "CLOCK_IN" -> "${step.def.emoji} ${step.def.title} — on the clock"
                            "CLOCK_OUT" -> "${step.def.emoji} ${step.def.title} — shift done"
                            else -> {
                                val tasks = Jobs.tasksFor(state, step.def)
                                "${step.def.emoji} ${step.def.title} — " +
                                    (tasks.getOrNull(hit.taskIndex)?.title ?: "Work")
                            }
                        }
                        UiKit.showMessageDialog(this, title,
                            message = hit.text,
                            positiveLabel = "OK",
                            onPositive = { openTalkSheetNow(kidName) })
                    }
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.FACTION_MISSION) {
            // v0.5.0d: faction business. An armed TALK beat for
            // this person lands through its judge, like a beat
            // step; otherwise this person fronts their faction's
            // next chain beat and offers it — take it on and it
            // arms, decline and it simply waits (no daily mark,
            // no penalty: chains don't nag and can't strand).
            val talkBeat = gate.missionTalk
            if (talkBeat != null) {
                bg.execute {
                    val hits = FactionMissions.judgeTalk(state, kidName)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        val hit = hits.firstOrNull()
                        if (hit == null) {
                            openTalkSheetNow(kidName)
                        } else {
                            val body = hit.text +
                                (hit.completionText?.let { "\n\n$it" }
                                    ?: "\n\n(⚔️ “${hit.chain.title}” — beat " +
                                        "${hit.beat.beat} of 3 landed)")
                            UiKit.showMessageDialog(this,
                                "⚔️ ${hit.chain.title} — ${hit.beat.title}",
                                message = body,
                                positiveLabel = "OK",
                                onPositive = { openTalkSheetNow(kidName) })
                        }
                    }
                }
                return
            }
            // v0.8.0b: capstone payloads on the same rail. An
            // armed capstone TALK beat (a naming, a ceremony)
            // lands through its judge, like a chain beat; a
            // capstone offer arms the same way. The two can
            // never collide with the chain payloads above: a
            // capstone exists only where its chain is done.
            val capTalk = gate.capstoneTalk
            if (capTalk != null) {
                bg.execute {
                    val hits = Capstones.judgeTalk(state, kidName)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        val hit = hits.firstOrNull()
                        if (hit == null) {
                            openTalkSheetNow(kidName)
                        } else {
                            val body = hit.text +
                                (hit.completionText?.let { "\n\n$it" }
                                    ?: "\n\n(🏛 “${hit.chain.title}” — beat " +
                                        "${hit.beat.beat} of 3 landed)")
                            UiKit.showMessageDialog(this,
                                "🏛 ${hit.chain.title} — ${hit.beat.title}",
                                message = body,
                                positiveLabel = "OK",
                                onPositive = { openTalkSheetNow(kidName) })
                        }
                    }
                }
                return
            }
            val capOffer = gate.capstoneOffer
            if (capOffer != null) {
                runOnUiThread {
                    val fName = FACTION_BY_ID[capOffer.factionId]?.name ?: "Faction"
                    val body = TextView(this).apply {
                        text = "“${capOffer.offerText}”\n\n" +
                            "🏛 ${Capstones.objective(capOffer)} — ${capOffer.hint}"
                        setTextColor(Color.parseColor(Chrome.INK))
                        textSize = 15f
                    }
                    UiKit.paperDialog(this,
                        "🏛 $fName — “${Capstones.chainById(capOffer.factionId)?.title ?: "Capstone"}”, beat ${capOffer.beat} of 3",
                        body,
                        positive = "Take it on" to {
                            bg.execute {
                                val ok = Capstones.accept(state, capOffer)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    toast(if (ok)
                                        "🏛 Taken on: ${Capstones.objective(capOffer)}"
                                    else
                                        "🏛 That one's no longer on the table.")
                                    refreshUi()
                                    openTalkSheetNow(kidName)
                                }
                            }
                        },
                        negative = "Not now" to { openTalkSheetNow(kidName) }).show()
                }
                return
            }
            val offer = gate.missionOffer!!
            runOnUiThread {
                val fName = FACTION_BY_ID[offer.factionId]?.name ?: "Faction"
                val body = TextView(this).apply {
                    text = "“${offer.offerText}”\n\n" +
                        "⚔️ ${FactionMissions.objective(offer)} — ${offer.hint}"
                    setTextColor(Color.parseColor(Chrome.INK))
                    textSize = 15f
                }
                UiKit.paperDialog(this,
                    "⚔️ $fName — “${FactionMissions.chainById(offer.factionId)?.title ?: "Mission"}”, beat ${offer.beat} of 3",
                    body,
                    positive = "Take it on" to {
                        bg.execute {
                            val ok = FactionMissions.accept(state, offer)
                            prefs.saveGame(state)
                            runOnUiThread {
                                toast(if (ok)
                                    "⚔️ Taken on: ${FactionMissions.objective(offer)}"
                                else
                                    "⚔️ That one's no longer on the table.")
                                refreshUi()
                                openTalkSheetNow(kidName)
                            }
                        }
                    },
                    negative = "Not now" to { openTalkSheetNow(kidName) }).show()
            }
            return
        }
        if (gate.rail == TalkGate.Rail.EMERG_OFFER) {
            // v0.8.0c: an emergency, offered in the moment.
            // Marked offered BEFORE the dialog, like every
            // ask, so a dismissed dialog can't re-offer it
            // today; taking it starts a clock that nothing
            // extends. The price on the dialog is the price
            // the Walk itself quoted — route, slack and all.
            val offer = gate.emergOffer!!
            bg.execute {
                Emergencies.markOffered(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    val sc = offer.scenario
                    val route = offer.stops.joinToString(" → ") {
                        LOCATIONS[it]?.name ?: it }
                    val body = TextView(this).apply {
                        text = "“${sc.offerText}”\n\n" +
                            "⏰ ${Errands.objective(offer.errand)}\n" +
                            "🧭 ${Errands.hintFor(state, offer.errand)}\n\n" +
                            "The clock: done by " +
                            "${minutesToTimeStr(offer.deadlineMin)}. The " +
                            "walk is ~${offer.routeMin} min ($route) — " +
                            "you've got slack, not forever. Taking it " +
                            "starts the clock, and the clock doesn't " +
                            "negotiate."
                        setTextColor(Color.parseColor(Chrome.INK))
                        textSize = 15f
                    }
                    UiKit.paperDialog(this,
                        "🚨 $kidName — it's an emergency", body,
                        positive = "I'm on it" to {
                            bg.execute {
                                val placed = Emergencies.accept(state, offer)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    toast(if (placed != null)
                                        "🚨 Taken on: ${Errands.objective(placed)} — " +
                                            "by ${minutesToTimeStr(placed.emergDeadline)}"
                                    else
                                        "🚨 Too late — that moment's already gone.")
                                    refreshUi()
                                    openTalkSheetNow(kidName)
                                }
                            }
                        },
                        negative = "I can't" to {
                            bg.execute {
                                Emergencies.decline(state)
                                prefs.saveGame(state)
                                runOnUiThread { openTalkSheetNow(kidName) }
                            }
                        }).show()
                }
            }
            return
        }
        if (gate.rail == TalkGate.Rail.RIVAL) {
            // v0.8.0e: the rival's business — the crowning, the
            // lock, a prank's answer, a rematch owed, or today's
            // challenge. All rules (deference, scheduling, the
            // free decline) live in Rival; this shell only plays
            // the named beat.
            val hit = gate.rivalHit!!
            val rival = Rival.rivalOf(state)
            when (hit.kind) {
                "reveal" -> {
                    UiKit.showMessageDialog(this, "🆚 $rival",
                        message = Rival.revealText(state),
                        positiveLabel = "▶",
                        onPositive = {
                            UiKit.showItemsDialog(this, "🆚 The moment",
                                Rival.revealOptions(state).toTypedArray()) { which ->
                                bg.execute {
                                    val text = Rival.playReveal(state, which)
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        UiKit.showMessageDialog(this,
                                            "🆚 THE RIVAL — $rival",
                                            message = text,
                                            positiveLabel = "🆚 It's on",
                                            onPositive = { openTalkSheetNow(kidName) })
                                    }
                                }
                            }
                        })
                }
                "lock" -> {
                    UiKit.showMessageDialog(this, "🆚 $rival",
                        message = "The scoreboard between you has " +
                            "${Rival.contestsPlayed(state)} contests on it. " +
                            "$rival is looking at the tally like it's a " +
                            "document that needs signing.",
                        positiveLabel = "▶",
                        onPositive = {
                            bg.execute {
                                val text = Rival.playLock(state)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    refreshUi()
                                    UiKit.showMessageDialog(this,
                                        "🆚 THE RIVAL — $rival",
                                        message = text,
                                        positiveLabel = "OK",
                                        onPositive = { openTalkSheetNow(kidName) })
                                }
                            }
                        })
                }
                "prank" -> {
                    UiKit.showMessageDialog(this, "💢 $rival confesses",
                        message = Rival.prankConfessText(state),
                        positiveLabel = "▶",
                        onPositive = {
                            UiKit.showItemsDialog(this, "💢 Your answer",
                                arrayOf("🥊 Prank back",
                                    "🕊 Let it go")) { which ->
                                bg.execute {
                                    val text = Rival.answerPrank(state, which == 0)
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        UiKit.showMessageDialog(this,
                                            "💢 The prank war",
                                            message = text,
                                            positiveLabel = "OK",
                                            onPositive = { openTalkSheetNow(kidName) })
                                    }
                                }
                            }
                        })
                }
                "resume" -> {
                    toast("❄️ The rematch: you vs $rival, first to 3.")
                    startShowdown(rival)
                }
                "crewresume" -> {
                    // v0.9.0e: an armed crew bracket still
                    // owes its captains' duel
                    toast("❄️ The crew bracket: captains' " +
                        "duel — you vs $rival, first to 3.")
                    startShowdown(rival)
                }
                "crewoffer" -> {
                    // v0.9.0e: a crew challenge — the rival
                    // fields their crew; all rules (fielding
                    // honesty, the no-crew word, the frenemy
                    // word, arming) live in RivalCrew; the
                    // final adds the captains' stance beat
                    val kind = hit.offerKind
                    val fin = hit.crewFinal
                    runOnUiThread {
                        val body = TextView(this).apply {
                            text = RivalCrew.offerText(state,
                                kind, fin)
                            setTextColor(Color.parseColor(Chrome.INK))
                            textSize = 15f
                        }
                        UiKit.paperDialog(this,
                            if (fin) "🏆 $rival: the crews final…"
                            else "👥 $rival's crew has a challenge…",
                            body,
                            positive = "You're on" to {
                                bg.execute {
                                    val acc = RivalCrew.accept(
                                        state, kind, fin)
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        if (fin && !acc.waited) {
                                            val stance = {
                                                UiKit.showItemsDialog(this,
                                                    "🏆 The captains",
                                                    RivalCrew.finalStanceOptions()
                                                        .toTypedArray()) { which ->
                                                    bg.execute {
                                                        val t = RivalCrew
                                                            .playFinalStance(state, which)
                                                        prefs.saveGame(state)
                                                        runOnUiThread {
                                                            UiKit.showMessageDialog(this,
                                                                "🏆 The final",
                                                                message = t,
                                                                positiveLabel = "🆚 Play it",
                                                                onPositive = {
                                                                    if (acc.startDuel)
                                                                        startShowdown(rival)
                                                                    else refreshUi()
                                                                })
                                                        }
                                                    }
                                                }
                                            }
                                            if (acc.startDuel) stance()
                                            else UiKit.showMessageDialog(this,
                                                "🏆 The final",
                                                message = acc.text,
                                                positiveLabel = "▶",
                                                onPositive = stance)
                                        } else if (acc.startDuel) {
                                            startShowdown(rival)
                                        } else {
                                            UiKit.showMessageDialog(this,
                                                "👥 Crew challenge",
                                                message = acc.text,
                                                positiveLabel = "👥 On it",
                                                onPositive = { refreshUi() })
                                        }
                                    }
                                }
                            },
                            negative = "Not today" to {
                                bg.execute {
                                    RivalCrew.decline(state)
                                    prefs.saveGame(state)
                                    runOnUiThread { openTalkSheetNow(kidName) }
                                }
                            }).show()
                    }
                }
                else -> {   // "offer"
                    val kind = hit.offerKind
                    runOnUiThread {
                        val body = TextView(this).apply {
                            text = Rival.offerText(state, kind)
                            setTextColor(Color.parseColor(Chrome.INK))
                            textSize = 15f
                        }
                        UiKit.paperDialog(this,
                            "🆚 $rival has a challenge…", body,
                            positive = "You're on" to {
                                bg.execute {
                                    val acc = Rival.accept(state, kind)
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        refreshUi()
                                        if (kind == Rival.K_SHOWDOWN) {
                                            startShowdown(rival)
                                        } else {
                                            UiKit.showMessageDialog(this,
                                                "🆚 Challenge accepted",
                                                message = acc.text,
                                                positiveLabel = "🆚 On it",
                                                onPositive = { refreshUi() })
                                        }
                                    }
                                }
                            },
                            negative = "Not today" to {
                                bg.execute {
                                    Rival.decline(state)
                                    prefs.saveGame(state)
                                    runOnUiThread { openTalkSheetNow(kidName) }
                                }
                            }).show()
                    }
                }
            }
            return
        }
        if (delivery != null) {
            bg.execute {
                val done = Errands.deliverTo(state, kidName)
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    UiKit.showMessageDialog(this, "📦 Delivered!",
                        message = (done?.reward?.thanksLine
                            ?: "$kidName takes it with a nod.") +
                            (done?.reward?.let { r ->
                                "\n\n(+${r.friendship} friendship" +
                                    (if (r.coins > 0) ", +${r.coins} 🪙" else "") + ")" +
                                    Errands.chainDialogText(r)
                            } ?: ""),
                        positiveLabel = "OK",
                        onPositive = { openTalkSheetNow(kidName) })
                }
            }
            return
        }
        // an ask is on the table: mark it offered BEFORE the
        // dialog, so a dismissed dialog can't re-offer it today
        val theAsk = gate.ask!!
        bg.execute {
            Errands.markAsked(state, kidName)
            prefs.saveGame(state)
            runOnUiThread {
                val body = TextView(this).apply {
                    text = "“${theAsk.text}”\n\n" +
                        "📋 ${Errands.objective(theAsk)} — ${Errands.hintFor(state, theAsk)}" +
                        (Errands.chainTeaser(theAsk)?.let { "\n\n$it" } ?: "")
                    setTextColor(Color.parseColor(Chrome.INK))
                    textSize = 15f
                }
                UiKit.paperDialog(this, "📋 $kidName has a favor…", body,
                    positive = "Sure — I'll do it" to {
                        bg.execute {
                            val ok = Errands.accept(state, theAsk)
                            prefs.saveGame(state)
                            runOnUiThread {
                                toast(if (ok)
                                    "📋 Errand taken: ${Errands.objective(theAsk)}"
                                else
                                    "📋 Your plate's full (${Errands.MAX_ACTIVE} errands) — " +
                                        "finish or drop one first.")
                                refreshUi()
                                openTalkSheetNow(kidName)
                            }
                        }
                    },
                    negative = "Not now" to { openTalkSheetNow(kidName) }).show()
            }
        }
    }

    private fun openTalkSheetNow(kidName: String) {
        SoundEngine.select()
        val me = engine.player(state)
        val sheet = DialogueSheet.newInstance("Talking to $kidName")
        sheet.playerName = me.name
        sheet.speakerColors = state.agents.mapValues { it.value.color }
        sheet.onSend = { text -> sendTalk(sheet, kidName, text) }
        sheet.onDismissed = { maybeShowEpisodeCards() }
        @Suppress("DEPRECATION")
        sheet.show(fragmentManager, "talk")
    }

    private fun openSaySheet() {
        SoundEngine.select()
        val me = engine.player(state)
        val loc = LOCATIONS[me.location]
        val sheet = DialogueSheet.newInstance("Yapping at ${loc?.name ?: "South Park"}")
        sheet.playerName = me.name
        sheet.speakerColors = state.agents.mapValues { it.value.color }
        sheet.initialLines = state.lastConversation
        sheet.onSend = { text -> sendSay(sheet, text) }
        sheet.onDismissed = { maybeShowEpisodeCards() }
        @Suppress("DEPRECATION")
        sheet.show(fragmentManager, "say")
    }

    private fun sendTalk(sheet: DialogueSheet, target: String, text: String) {
        bg.execute {
            runOnUiThread { sheet.setTyping(true) }
            val me = engine.player(state)
            val preLines = memoryPreLines(target)
            val agent = state.agents[target]
            val blockBefore = agent?.let {
                engine.effectiveAgendaBlock(state, it, state.timeMin)?.location
            }
            val lines = engine.playerTalk(state, target, text).toMutableList()
            // #28 CONVERSATIONS TAKE TIME: the world keeps ticking
            // while you talk (~20 game-minutes per exchange).
            gameMinAcc += 15.0
            actionTick()
            // ...and if your partner's next block pulled them away
            // mid-chat, they wrap up and GO.
            if (agent != null && blockBefore != null &&
                lines.any { it.speaker == target }) {
                val after = engine.effectiveAgendaBlock(state, agent, state.timeMin)?.location
                if (after != null && after != blockBefore && after != me.location) {
                    lines.add(Line(target,
                        "Oh crap, look at the time — I gotta be at " +
                            "${LOCATIONS[after]?.name ?: after}. Later, ${me.name}!"))
                }
            }
            recordTalkMemory(target, text)
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                sheet.setTyping(false)
                sheet.addLines(preLines + lines)
                refreshUi()
            }
        }
    }

    /** Stable string form of an appearance map (outfit-change memory). */
    private fun appearanceKey(app: Map<String, String>): String =
        app.entries.sortedBy { it.key }.joinToString(";") { "${it.key}=${it.value}" }

    /** #50 ABSENCE IS NOTICED + #48 OUTFIT CHANGES GET NOTICED:
     *  lines a friend says BEFORE your chat starts, drawn from their
     *  character memory. Read before recordTalkMemory refreshes it. */
    private fun memoryPreLines(npcName: String): List<Line> {
        // v0.4.0g: the owed-lines stack (witness tellings,
        // arc beats, beat references, errand thanks — in
        // their pinned order) is TalkGate.owedLines now;
        // what follows is the memory-driven remainder.
        val out = TalkGate.owedLines(state, npcName).toMutableList()
        // v0.6.0a: commission talk — a pending "close, but…"
        // for a near-miss photo (played once), then the
        // once-a-day reminder of an ask you're holding.
        out.addAll(Commissions.talkLines(state, npcName))
        // v0.9.0b: crew talk — a member standing with you
        // talks about the outing (the place, each other);
        // the rival has opinions about your crew; a
        // faction with three of yours in it has a line.
        // Crew.talkLines owns the stamps (once in a while,
        // never every tap).
        out.addAll(Crew.talkLines(state, npcName))
        // v0.14.0e: clubhouse talk — at the crew's seasonal
        // spot in hangout hours, a member's line IS the
        // pending Talk beat (judged as it plays, in
        // Clubhouse.talkLines) or, with the beats spent, a
        // crew-only clubhouse bank line. Non-members and
        // members elsewhere get silence from the bank.
        out.addAll(Clubhouse.talkLines(state, npcName))
        // v0.15.0h: apprenticeship remarks — a mentor's
        // standing line for their master, and the town's
        // three weeks of talk about a fresh mark, once
        // each (Apprenticeships owns the words and the
        // stamps; a person with no mark to remark on gets
        // silence, like every owed line).
        out.addAll(Apprenticeships.remarkLines(state, npcName))
        val mem = NpcMemoryStore.get(npcName) ?: return out
        if (mem.meets == 0) return out
        val me = engine.player(state)
        val pts = state.friendship[npcName] ?: mem.points
        if (mem.lastMetDay.isNotEmpty()) {
            val gap = daysBetween(mem.lastMetDay, state.todayKey)
            if (gap >= 5 && Relationships.tierOf(pts) >= Relationships.FRIEND) {
                val recap = state.townLog.lastOrNull() ?: "the usual chaos"
                val swing = state.events.lastOrNull { it.startsWith("⚔️") } ?: ""
                out.add(Line(npcName,
                    "Where've you BEEN, ${me.name}? $gap days! You missed: $recap $swing"))
            }
        }
        val curApp = appearanceKey(me.appearance)
        if (mem.seenAppearance.isNotEmpty() && mem.seenAppearance != curApp) {
            out.add(Line(npcName, outfitComment(me.appearance, mem.seenAppearance, npcName)))
        }
        // #53 THANK-YOU ECHOES: you gave their friend something they
        // loved — word reached this NPC, and they thank you for it.
        if (state.lifeText["echo:$npcName"] != state.todayKey) {
            val giftedFriend = relationsOf(npcName).firstOrNull { f ->
                NpcMemoryStore.get(f)?.likes?.isNotEmpty() == true
            }
            if (giftedFriend != null) {
                state.lifeText["echo:$npcName"] = state.todayKey
                out.add(Line(npcName,
                    "Hey — thanks for looking out for $giftedFriend, by the way. " +
                        "That was decent of you."))
            }
        }
        return out
    }

    /** The exact changed category gets the exact comment. The lines
     *  themselves live in GameData.outfitCommentLine (pure, pinned)
     *  so the deepened creator's new categories are covered too.
     *  v0.5.0b: when the changed piece is a WARDROBE unlockable,
     *  its own line goes first (Wardrobe.noticeLine — from the
     *  piece's named cast member when this NPC is that member).
     *  A wardrobe piece can only be worn once owned, so wearing
     *  one an NPC hasn't seen means it was unlocked since your
     *  last outfit change in their memory — the town notices. */
    private fun outfitComment(newApp: Map<String, String>, oldKey: String,
                              npcName: String): String {
        val old = oldKey.split(";").mapNotNull {
            val p = it.split("=", limit = 2)
            if (p.size == 2) p[0] to p[1] else null
        }.toMap()
        Wardrobe.noticeLine(newApp, old, npcName)?.let { return it }
        val changed = newApp.keys.firstOrNull { old[it] != newApp[it] } ?: return "New look, huh?"
        return outfitCommentLine(changed)
    }

    private fun sendSay(sheet: DialogueSheet, text: String) {
        bg.execute {
            runOnUiThread { sheet.setTyping(true) }
            val lines = engine.playerSay(state, text)
            actionTick()
            for (l in lines) {
                if (state.agents.containsKey(l.speaker)) recordTalkMemory(l.speaker, text)
            }
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                sheet.setTyping(false)
                sheet.addLines(lines)
                refreshUi()
            }
        }
    }

    /** Persist a talk into the NPC's character memory (cast: friendship points). */
    private fun recordTalkMemory(npcName: String, playerLine: String?) {
        val me = engine.player(state)
        // #31: holidays write into memory through the same note slot
        // as shenanigans — NPCs remember holidays spent with you.
        val eventTitle = Quests.activeDef(state)?.title
            ?: state.holidayId?.let { id ->
                HOLIDAYS.firstOrNull { it.id == id }?.let { "${it.name} with you" }
            }
        NpcMemoryStore.recordTalk(npcName, state.todayKey, state.timeMin, playerLine,
            state.friendship[npcName] ?: NpcMemoryStore.get(npcName)?.points ?: 0,
            me.name, eventTitle)
        // #48: this NPC has now SEEN your current look.
        NpcMemoryStore.getOrCreate(npcName).seenAppearance = appearanceKey(me.appearance)
    }

    // ---------------- generated lorebook NPCs (visitors + residents) ----------------
    private fun onTalkVisitor(loreName: String) {
        // v0.10.0a: your parents get the family's own sheet
        if (Family.isParent(loreName)) {
            runOnUiThread { maybeGiftFirst(loreName) { openFamilySheet(loreName) } }
            return
        }
        // v0.13.0e: your pen pal, in person at last — her
        // own sheet, her own authored voice, no brain but
        // the banks (the family precedent).
        if (PenPals.isPal(loreName)) {
            runOnUiThread { maybeGiftFirst(loreName) { openPalSheet() } }
            return
        }
        runOnUiThread { maybeGiftFirst(loreName) { openVisitorSheet(loreName) } }
    }

    /** v0.13.0e: Wren's sheet — deterministic and authored,
     *  like the family's: her greeting (the goodbye owns the
     *  last day's first hello), her talk pool in her voice,
     *  a Flats fact on "tell me more", and her own byes.
     *  Warmth moves only in her own lane (PenPals.noteTalk),
     *  never state.friendship. */
    private var palTalkCount = 0

    private fun openPalSheet() {
        SoundEngine.select()
        val persona = PenPals.persona(state)
        val me = engine.player(state)
        PenPals.noteTalk(state)
        prefs.saveGame(state)
        val sheet = DialogueSheet.newInstance(
            "Talking to ${PenPals.PAL} (your pen pal, in person)")
        sheet.playerName = me.name
        sheet.speakerColors = mapOf(PenPals.PAL to persona.color) +
            state.agents.mapValues { it.value.color }
        sheet.initialLines = listOf(
            Line(PenPals.PAL, PenPals.greeting(state)))
        sheet.onSend = { text -> sendPalTalk(sheet, text) }
        sheet.onDismissed = { maybeShowEpisodeCards() }
        @Suppress("DEPRECATION")
        sheet.show(fragmentManager, "visitor")
    }

    private fun sendPalTalk(sheet: DialogueSheet, text: String) {
        bg.execute {
            runOnUiThread { sheet.setTyping(true) }
            val persona = PenPals.persona(state)
            val reply = when {
                classifyPlayerLine(text) == "bye" ->
                    persona.byeLines[kotlin.math.abs(
                        text.hashCode() + state.absDay) %
                        persona.byeLines.size]
                text.contains("more", ignoreCase = true) ||
                    text.contains("flats", ignoreCase = true) ->
                    PenPals.VISIT_FACTS[palTalkCount %
                        PenPals.VISIT_FACTS.size]
                else -> PenPals.talkLine(palTalkCount)
            }
            palTalkCount++
            recordTalkMemory(PenPals.PAL, text)
            NpcMemoryStore.get(PenPals.PAL)?.points =
                PenPals.points(state)
            actionTick()
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                sheet.setTyping(false)
                sheet.addLines(listOf(Line(PenPals.PAL, reply)))
                refreshUi()
            }
        }
    }

    /** v0.15.0b: the return trip's hosted beats — Wren
     *  hosts, in her own voice, through the same sheet
     *  machinery as her visit's talk; PenPals owns the
     *  scripts, the warmth and the stamps. A kept beat
     *  pays once, on the sheet's close. A null beat is a
     *  free talk on a day whose beat is already kept. */
    private var tripTalkCount = 0

    private fun openTripSheet(beat: String?) {
        SoundEngine.select()
        val persona = PenPals.persona(state)
        val me = engine.player(state)
        prefs.saveGame(state)
        val sheet = DialogueSheet.newInstance(
            "✈️ ${PenPals.PAL_TOWN} — " +
                (beat?.let { PenPals.tripBeatLabel(it) }
                    ?: "with ${PenPals.PAL_FIRST}, hosting"))
        sheet.playerName = me.name
        sheet.speakerColors = mapOf(PenPals.PAL to persona.color) +
            state.agents.mapValues { it.value.color }
        val lines = mutableListOf<Line>()
        if (beat == PenPals.BEAT_ARRIVE)
            lines += Line("🚌 The road", PenPals.JOURNEY_LINE)
        if (beat != null)
            for (line in PenPals.tripScript(beat))
                lines += Line(PenPals.PAL, line)
        else
            lines += Line(PenPals.PAL,
                PenPals.hostLine(tripTalkCount))
        sheet.initialLines = lines
        sheet.onSend = { text -> sendTripTalk(sheet, text) }
        sheet.onDismissed = {
            if (beat != null &&
                PenPals.noteTripBeat(state, beat)) {
                prefs.saveGame(state)
                toast("✈️ ${PenPals.tripBeatLabel(beat)} — " +
                    "kept, in her book and yours.")
            }
            maybeShowEpisodeCards()
        }
        @Suppress("DEPRECATION")
        sheet.show(fragmentManager, "visitor")
    }

    private fun sendTripTalk(sheet: DialogueSheet, text: String) {
        bg.execute {
            runOnUiThread { sheet.setTyping(true) }
            val persona = PenPals.persona(state)
            val reply = when {
                classifyPlayerLine(text) == "bye" ->
                    persona.byeLines[kotlin.math.abs(
                        text.hashCode() + state.absDay) %
                        persona.byeLines.size]
                text.contains("more", ignoreCase = true) ||
                    text.contains("flats", ignoreCase = true) ->
                    PenPals.hostFact(tripTalkCount)
                else -> PenPals.hostLine(tripTalkCount)
            }
            tripTalkCount++
            recordTalkMemory(PenPals.PAL, text)
            NpcMemoryStore.get(PenPals.PAL)?.points =
                PenPals.points(state)
            actionTick()
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                sheet.setTyping(false)
                sheet.addLines(listOf(Line(PenPals.PAL, reply)))
                refreshUi()
            }
        }
    }

    private fun openVisitorSheet(loreName: String) {
        SoundEngine.select()
        val persona = VisitorSystem.personaByName(loreName) ?: run {
            toast("They wandered off.")
            return
        }
        val me = engine.player(state)
        val sheet = DialogueSheet.newInstance(
            "Talking to $loreName${if (persona.resident) " (local)" else " (visiting)"}")
        sheet.playerName = me.name
        sheet.speakerColors = mapOf(loreName to persona.color) +
            state.agents.mapValues { it.value.color }
        val mem = NpcMemoryStore.get(loreName)
        val greet = if (mem != null && mem.meets > 0) {
            NpcMemoryStore.repeatGreeting(loreName, me.name)
                ?: persona.greetingLines.firstOrNull() ?: "Hi."
        } else {
            persona.greetingLines[(loreName.hashCode() and 0x7fffffff) %
                persona.greetingLines.size.coerceAtLeast(1)]
        }
        // #52 TOWN REPUTATION: strangers who've never met you may
        // already know you by your reputation — good or bad.
        val repGreet = if ((mem == null || mem.meets == 0) &&
            (TownLife.rep(state) >= 10 || TownLife.rep(state) <= -10)) {
            "Oh — you're ${TownLife.repTier(state)}! Word travels fast in this town."
        } else null
        sheet.initialLines = listOf(Line(loreName, repGreet ?: greet))
        sheet.onSend = { text -> sendVisitorTalk(sheet, persona, text) }
        sheet.onDismissed = { maybeShowEpisodeCards() }
        @Suppress("DEPRECATION")
        sheet.show(fragmentManager, "visitor")
    }

    private fun sendVisitorTalk(sheet: DialogueSheet, persona: VisitorPersona, text: String) {
        bg.execute {
            runOnUiThread { sheet.setTyping(true) }
            val mem = NpcMemoryStore.getOrCreate(persona.loreName)
            val firstMeetToday = mem.lastMetDay != state.todayKey
            val preLines = memoryPreLines(persona.loreName)
            // #20 STANDING BY ASSOCIATION: a first-ever chat with a
            // faction's generated member starts from your standing
            // with their faction — doors open (or close) before you speak.
            if (mem.meets == 0 && mem.points == 0 && persona.factionId != null) {
                val start = Factions.associationStart(
                    state.factionStates[persona.factionId]?.playerStanding ?: 0)
                if (start != 0) mem.points = start
            }
            val reply = visitorReply(persona, text)
            // relationship points in their character memory (insults count too)
            val delta = when (classifyPlayerLine(text)) {
                "insult" -> -6
                "compliment" -> 5
                else -> 3
            }
            mem.points = (mem.points + delta).coerceIn(-50, 100)
            if (firstMeetToday) {
                state.questCounters["visitorsMet"] =
                    (state.questCounters["visitorsMet"] ?: 0) + 1
            }
            recordTalkMemory(persona.loreName, text)
            // recordTalk used cast friendship fallback; fix points to the visitor's own:
            NpcMemoryStore.get(persona.loreName)?.points = mem.points
            // #18 VISITORS CAN MOVE IN: met across several sessions
            // and liked — a visitor takes up residency for good.
            // v0.2.0g: conversions stop at MAX_CONVERTED (the
            // roster is forever); a save already past the cap
            // keeps everyone it has — the gate only stops NEW
            // move-ins.
            if (!persona.resident && persona.loreName !in state.convertedResidents &&
                state.convertedResidents.size < VisitorSystem.MAX_CONVERTED &&
                mem.meets >= 3 &&
                Relationships.tierOf(mem.points) >= Relationships.FRIEND) {
                state.convertedResidents.add(persona.loreName)
                state.lifeText["movedIn:${persona.loreName}"] = state.todayKey
                state.events.add("🏠 ${persona.loreName} moved to South Park for good!")
                while (state.events.size > 40) state.events.removeAt(0)
                townLogLine(state, "🏠 ${persona.loreName} moved in")
            }
            actionTick()
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                sheet.setTyping(false)
                sheet.addLines(preLines + Line(persona.loreName, reply))
                refreshUi()
            }
        }
    }

    /** Visitor reply: LLM when configured (grounded in their own lorebook
     *  entry + character memory), else their entry-derived fallback lines. */
    private fun visitorReply(persona: VisitorPersona, playerText: String): String {
        val config = prefs.activeLlmConfig()
        if (config != null) {
            val me = engine.player(state)
            val entryText = LoreBook.entry(persona.loreName)?.text ?: persona.bio
            val prompt = buildString {
                append("You are ${persona.loreName}, a minor/background character visiting South Park. ")
                append("Stay in character, 1-2 sentences, South Park voice. ")
                append("Your lorebook entry: ${LoreBook.excerpt(entryText, 500)} ")
                append(NpcMemoryStore.promptBlock(persona.loreName, me.name))
                // v0.1.9e: the visitor's current goal joins the
                // prompt (thread context rides in promptBlock).
                val g = NpcGoals.record(state, persona.loreName)
                if (!g.done) append("\nYour current goal: ${g.text} It may come up naturally.")
                append("\n${me.name} says: \"$playerText\"\n")
                append("Reply with exactly one line formatted as: ${persona.loreName}: <line>")
            }
            val lines = llmLines(prompt, listOf(persona.loreName), config)
            if (lines.isNotEmpty()) return lines.first().text
        }
        val cat = classifyPlayerLine(playerText)
        val mem = NpcMemoryStore.get(persona.loreName)
        val tier = Relationships.tierOf(mem?.points ?: 0)
        val seedRnd = java.util.Random(persona.loreName.hashCode().toLong() +
            (mem?.meets ?: 0) * 131L + playerText.hashCode())
        val pool = when {
            cat == "bye" -> persona.byeLines
            // v0.2.0d: the ambient pool (role/trait-templated
            // small talk) joins the rotation, so generated folks
            // chat about their lives, not only lorebook facts.
            tier >= Relationships.FRIEND && cat != "greeting" ->
                persona.deepLines + persona.factLines + persona.ambientLines
            cat == "greeting" -> persona.greetingLines
            else -> persona.factLines + persona.ambientLines
        }
        var line = pool[seedRnd.nextInt(pool.size)]
        // v0.1.9e THREADS + GOALS in the fallback brain too
        // (parity with the LLM prompt context above): follow-ups
        // from stored topics, and the occasional goal mention.
        if (cat == "greeting" && mem != null && seedRnd.nextFloat() < 0.35f) {
            Threads.followUp(mem, kotlin.random.Random(
                persona.loreName.hashCode().toLong() + mem.meets * 131L))?.let { line = it }
        } else if (seedRnd.nextFloat() < 0.16f) {
            val g = NpcGoals.record(state, persona.loreName)
            if (!g.done) line = NpcGoals.mention(g)
        }
        Quests.activeDef(state)?.let { def ->
            line = Shenanigans.applyTone(line,
                Shenanigans.stageOf(def, state.timeMin).tone, persona.loreName)
        }
        return line
    }

    // ---------------- interiors / trash / litter ----------------
    private fun onEnterInterior() {
        bg.execute {
            val me = engine.player(state)
            interiorOf(me.location) ?: return@execute
            // #66 DOORBELL VISITS: at a friend's house, you can ring
            // first — if they're home, they come to the door to talk
            // instead of you wandering into their house.
            val owner = KID_HOUSE.entries.firstOrNull { it.value == me.location }?.key
            val ownerAgent = owner?.let { state.agents[it] }
            if (owner != null && ownerAgent != null && !ownerAgent.asleep &&
                ownerAgent.location == me.location) {
                runOnUiThread {
                    UiKit.showItemsDialog(this,
                        LOCATIONS[me.location]?.name ?: "House",
                        arrayOf("🔔 Ring the doorbell", "🚪 Just go in")) { which ->
                        if (which == 0) openTalkSheet(owner) else enterInteriorNow()
                    }
                }
                return@execute
            }
            enterInteriorNow()
        }
    }

    private fun enterInteriorNow() {
        bg.execute {
            val me = engine.player(state)
            val def = interiorOf(me.location) ?: return@execute
            // #29 PLACES KEEP HOURS: keeper-run places lock when the
            // keeper's work block ends; the door tells the story.
            val closed = engine.shopClosedReason(state, me.location)
            if (closed != null) {
                runOnUiThread {
                    UiKit.showMessageDialog(this,
                        LOCATIONS[me.location]?.name ?: "Closed",
                        message = closed, positiveLabel = "OK")
                }
                return@execute
            }
            state.playerInside = true
            SoundEngine.door(open = true)
            SoundEngine.footsteps(snow = false, inside = true, steps = 2)
            prefs.saveGame(state)
            // #40: a place you keep clean greets you like it remembers
            val tidyNote = if ((state.life["clean:${me.location}"] ?: 0) >= 3)
                " Still tidy from your cleaning." else ""
            runOnUiThread {
                gameView.centerCamera()
                toast("🚪 You step inside — ${def.name}.$tidyNote")
                refreshUi()
                maybeShowEpisodeCards()
            }
        }
    }

    private fun onExitInterior() {
        // v0.16.0a: the door out of a bus IS the ride
        // (items 8+9), and the door out of a street-house
        // section returns you to that street (item 20)
        if (BusRideState.active(state)) {
            completeBusRide()
            return
        }
        if (state.playerInside &&
            state.lifeText.containsKey("insideSection")) {
            bg.execute {
                state.lifeText.remove("insideSection")
                state.playerInside = false
                SoundEngine.door(open = false)
                prefs.saveGame(state)
                runOnUiThread {
                    gameView.centerCamera()
                    toast("🚪 Back on the street — " +
                        "${LOCATIONS[engine.player(state).location]?.name}.")
                    refreshUi()
                }
            }
            return
        }
        bg.execute {
            val me = engine.player(state)
            state.playerInside = false
            SoundEngine.door(open = false)
            SoundEngine.footsteps(snow = state.weatherId in
                setOf("light_snow", "heavy_snow", "blizzard"), inside = false, steps = 2)
            prefs.saveGame(state)
            runOnUiThread {
                gameView.centerCamera()
                toast("🚪 Back outside — ${LOCATIONS[me.location]?.name}.")
                refreshUi()
                maybeShowEpisodeCards()
            }
        }
    }

    private fun onSearchTrash(containerId: String) {
        bg.execute {
            val cont = TRASH_CONTAINERS.firstOrNull { it.id == containerId }
                ?: return@execute
            val (foundItem, msg) = engine.searchTrash(state, cont)
            if (foundItem != null) SoundEngine.select()
            prefs.saveGame(state)
            runOnUiThread {
                toast("🗑️ $msg")
                refreshUi()
            }
        }
    }

    private fun onPickupLitter(litterId: String) {
        bg.execute {
            val (foundItem, msg) = engine.pickupLitter(state, litterId)
            if (foundItem != null) SoundEngine.tap()
            prefs.saveGame(state)
            runOnUiThread {
                // v0.16.0a (item 16): picking up trash has
                // its own animation — bend, reach, done
                gameView.playPlayerAction("pickup_trash")
                toast(msg)
                refreshUi()
            }
        }
    }

    private fun openInventory() {
        prefs.saveGame(state)
        startActivity(Intent(this, InventoryActivity::class.java))
    }

    /** Generated lorebook NPCs sharing this exact space right
     *  now — the same computation refreshUi feeds the renderer
     *  (presence base + event crowds + the welcome wagon).
     *  v0.2.0c extracted it so the selfie aims at exactly who
     *  is drawn standing with you. */
    private fun generatedHere(): List<ActiveNpc> {
        val me = try { engine.player(state) } catch (e: Exception) { return emptyList() }
        val week = (state.gameDay + (state.gameMonth - 1) * 31) / 7
        val dow = dayOfWeek(state)
        return try {
            val here = VisitorSystem.presentAt(me.location, state.playerInside,
                state.todayKey, state.timeMin, week, dow)
            // #21: generated residents join event cluster crowds
            val crowd = if (!state.playerInside) {
                VisitorSystem.eventCrowd(state.todayKey, Quests.activeDef(state),
                    Quests.currentStage(state))
                    .filter { it.locId == me.location && it.inside == state.playerInside }
            } else emptyList()
            // #60 WELCOME WAGON: on a new resident's first day, two
            // neighbors show up at their door with a casserole.
            val wagon = if (!state.playerInside && me.location in HOUSE_LOCS) {
                val newbie = VisitorSystem.residents().firstOrNull { p ->
                    p.home == me.location &&
                        state.lifeText["movedIn:${p.loreName}"] == state.todayKey
                }
                if (newbie != null) {
                    VisitorSystem.residents()
                        .filter { it.loreName != newbie.loreName }
                        .take(2)
                        .map { ActiveNpc(it, me.location, false) }
                } else emptyList()
            } else emptyList()
            // v0.3.0f: the booth case's usher is on shift at the
            // Bijou for the whole case — present, sweep in hand,
            // and talkable like any generated soul.
            val usher = if (me.location == "Bijou") {
                listOfNotNull(Mystery2.usherHere(state, state.playerInside))
            } else emptyList()
            // v0.10.0a: your family, once they've arrived —
            // household NPCs in the same generated pattern.
            val family = Family.presentAt(state, me.location,
                state.playerInside)
            // v0.13.0e: your pen pal, on her visit days — a
            // guest in the same generated pattern, present
            // only where PenPals.blockAt puts her.
            val pal = PenPals.presentAt(state, me.location,
                state.playerInside)
            (here + crowd + wagon + usher + family + pal)
                .distinctBy { it.persona.loreName }
        } catch (e: Exception) { emptyList() }
    }

    /** v0.2.0c: 📸 SELFIE — point the camera at whoever is
     *  standing with you (the same presence as Talk, cast and
     *  generated alike). Reactions, followers, feats, PHOTO
     *  goals and the once-per-event participation point all
     *  resolve in Photos (pure); here we just take the shot
     *  and show the polaroid.
     *
     *  v0.4.0c: with 2+ townsfolk around, the camera asks
     *  HOW first — the classic pile-in selfie, or a posed
     *  GROUP frame (Photos.composeGroup: up to 3 chosen by
     *  affection, arranged by chemistry, paid at the group
     *  rate). Nobody or a lone somebody keeps the old flow. */
    private fun onSelfie() {
        bg.execute {
            val castPresent = engine.presentWithPlayer(state)
                .filter { !it.asleep }.map { it.name }
            val genPresent = generatedHere().map { it.persona }
            when {
                castPresent.isEmpty() && genPresent.isEmpty() ->
                    runOnUiThread {
                        toast("Nobody here to selfie with, dude. Walk somewhere with people.")
                    }
                castPresent.size + genPresent.size >= 2 ->
                    runOnUiThread {
                        UiKit.showItemsDialog(this, "📸 Photo time — how?",
                            arrayOf("🤳 Selfie — everybody pile in",
                                "👥 Group shot — pose a proper frame")) { which ->
                            takePhoto(which == 1, castPresent, genPresent)
                        }
                    }
                else -> takePhoto(false, castPresent, genPresent)
            }
        }
    }

    /** Take the shot (classic or group), file it through
     *  Photos.applyPhoto, and show the polaroid with its
     *  summary lines. */
    private fun takePhoto(group: Boolean, castPresent: List<String>,
                          genPresent: List<VisitorPersona>) {
        bg.execute {
            val me = engine.player(state)
            val rec = if (group)
                Photos.composeGroup(state, castPresent, genPresent,
                    me.name, prefs.getAppearance())
            else Photos.compose(state, castPresent, genPresent,
                me.name, prefs.getAppearance())
            // v0.4.0b: applyPhoto settles PHOTO errands inside
            // itself — count around it to announce them here
            val errandsBefore = state.errandsDoneCount
            // v0.6.0a: same counting trick for commissions —
            // a landed ask announces itself, with its print
            val commissionsBefore = state.commissionsDone
            // v0.4.0d: same counting trick for holiday beats —
            // a portrait step settled by this shutter announces
            val beatsBefore = HolidayBeats.doneTotal(state)
            val beatsStepsBefore = HolidayBeats.activeBeat(state)
                ?.let { HolidayBeats.stepsDone(state, it) } ?: -1
            val result = Photos.applyPhoto(state, rec)
            // v0.7.0e: a photo taken on a live climax's turf
            // counts toward the chain's PHOTO beat — the
            // shutter is the verb, Climaxes just hears it.
            Climaxes.notePhoto(state, rec.locId)
            // v0.10.0a: and a family photo at home settles the
            // family's photo chore — same counting trick: the
            // chore count moves, the toast below says so.
            val famChoresBefore = state.life["famChoreDone"] ?: 0
            Family.notePhoto(state, rec)
            if ((state.life["famChoreDone"] ?: 0) > famChoresBefore) {
                state.events.add("📸 Family photo taken — chore done, " +
                    "frame no longer empty.")
                while (state.events.size > 40) state.events.removeAt(0)
            }
            prefs.saveGame(state)
            prefs.saveNpcMemory()
            runOnUiThread {
                SoundEngine.shutter()
                if (result.milestone != null) SoundEngine.questComplete()
                val bits = mutableListOf<String>()
                bits.add("📈 +${result.gained} followers " +
                    "(${state.followers} total)")
                if (rec.isGroup)
                    bits.add("👥 Group shot — ${rec.npcs.size} in frame " +
                        "(${rec.composition}).")
                if (result.chemistryBonus > 0)
                    bits.add("✨ Chemistry bonus +${result.chemistryBonus} — " +
                        "that pair is a THING in this town.")
                if (result.milestone != null)
                    bits.add("🏅 ${result.milestone} FOLLOWERS — you're A Thing now.")
                for (fid in result.newFeats)
                    bits.add("${Photos.featById(fid)?.name ?: fid} — feat complete!")
                for (g in result.goalsDone)
                    bits.add("🎯 $g finally got that fridge-door photo.")
                if (state.errandsDoneCount > errandsBefore)
                    bits.add("📋 Errand complete — " +
                        (Errands.lastNote(state)?.line ?: "someone thanks you!"))
                if (state.commissionsDone > commissionsBefore)
                    bits.add("🖨 COMMISSIONED PRINT — " +
                        (Commissions.lastNote(state)?.line ?: "the shot landed!") +
                        " It's on the wall in your Album, stamped, forever.")
                if (HolidayBeats.doneTotal(state) > beatsBefore)
                    bits.add("🎉 Holiday beat COMPLETE — that photo was the " +
                        "last step. It's in the Journal for good.")
                else if (beatsStepsBefore >= 0 &&
                    (HolidayBeats.activeBeat(state)
                        ?.let { HolidayBeats.stepsDone(state, it) } ?: -1) >
                    beatsStepsBefore)
                    bits.add("🎉 Holiday beat step — the photo counted. " +
                        "The Journal's HOLIDAYS page tracks the rest.")
                if (result.participationCounted)
                    bits.add("🎪 The selfie counts — you're part of this event now.")
                if (rec.refused.isNotEmpty())
                    bits.add("${rec.refused.joinToString(" & ")} refused. Rude.")
                UiKit.showPhotoDialog(this, rec, bits.joinToString("\n")) {
                    refreshUi()
                }.show()
                refreshUi()
            }
        }
    }

    private fun onTalkButton() {
        bg.execute {
            val present = engine.presentWithPlayer(state).filter { !it.asleep }
            runOnUiThread {
                when {
                    present.isEmpty() -> toast("Nobody here, dude. Walk somewhere with kids.")
                    present.size == 1 -> openTalkSheet(present[0].name)
                    else -> {
                        val names = present.map { it.name }.toTypedArray()
                        UiKit.showItemsDialog(this, "Talk to who?", names) { which ->
                            openTalkSheet(names[which])
                        }
                    }
                }
            }
        }
    }

    private fun onExit(id: String) {
        bg.execute {
            gameView.sitting = false
            // v0.5.0a: an exit is a WALK now — one leg of the
            // town's real geography, charged minutes by true
            // distance and lived through as real ticks (Walk
            // owns routing, shortcuts and road beats; when
            // real-time sync owns the clock, legs don't tick).
            val out = Walk.walkTo(state, id, kotlin.random.Random.Default,
                advance = !prefs.isRealtimeSync())
            val ok = out.moved
            prefs.saveGame(state)
            // v0.2.0b: your boots announce the trip — snow crunch
            // in snow weather / winter, soft steps otherwise; a
            // refused exit gets the thud.
            if (ok) {
                val snowy = state.weatherId in setOf("light_snow", "heavy_snow", "blizzard") ||
                    currentSeason() == Season.WINTER
                SoundEngine.footsteps(snow = snowy, inside = false)
            } else {
                SoundEngine.thud()
            }
            runOnUiThread {
                if (ok) gameView.centerCamera()
                toast(walkToast(out, id))
                refreshUi()
                // a road beat earns its card; episode cards
                // defer behind it on their own guards
                out.beat?.let { lookDialog(it.text) }
                if (ok) maybeShowEpisodeCards()
            }
        }
    }

    /** The stroll card, in words: the leg's landing, its
     *  flavor, and where the trip goes next (if anywhere). */
    private fun walkToast(out: LegOutcome, tappedId: String): String {
        if (!out.moved)
            return "You're already at ${LOCATIONS[tappedId]?.name ?: "here"}, dude."
        val toName = LOCATIONS[out.to]?.name ?: out.to
        val sb = StringBuilder("🚶 You walk to $toName — ${out.flavor}")
        if (out.tripDropped) sb.append(" (New plan — the old trip's off.)")
        when {
            out.arrived ->
                sb.append(" 🎯 ${LOCATIONS[out.destId]?.name ?: toName} — " +
                    "trip complete (${out.minutes} min this leg).")
            out.passThrough ->
                sb.append(" 🚶 Passing through — next stop " +
                    "${LOCATIONS[out.nextStop]?.name} toward " +
                    "${LOCATIONS[out.destId]?.name}. Tap its signpost to " +
                    "keep walking, or stay: you're really here.")
        }
        return sb.toString()
    }

    /** v0.5.0a: 🗺 GO SOMEWHERE — the destination picker.
     *  Every place in town, priced in legs and minutes off
     *  the real graph (Walk.route / routeMinutes). Picking a
     *  far place starts a walking trip and takes the first
     *  leg on the spot; with the bus pass in your wallet, a
     *  far pick offers the ride instead. */
    private fun openGoSomewhere() {
        bg.execute {
            val here = engine.player(state).location
            val dests = LOCATIONS.values.filter { it.id != here }
            val labels = dests.map { d ->
                val legs = Walk.route(here, d.id).size - 1
                val mins = Walk.routeMinutes(state, here, d.id)
                "🚶 ${d.name} — $legs leg${if (legs == 1) "" else "s"}, ~$mins min"
            }.toTypedArray()
            runOnUiThread {
                UiKit.showItemsDialog(this, "🗺 Go somewhere…", labels) { which ->
                    startGo(dests[which].id)
                }
            }
        }
    }

    private fun startGo(destId: String) {
        bg.execute {
            val here = engine.player(state).location
            val legs = Walk.route(here, destId).size - 1
            if (Walk.hasPass(state) && legs > 1) {
                val rideMin = Walk.rideCostMinutes(here, destId)
                val walkMin = Walk.routeMinutes(state, here, destId)
                val destName = LOCATIONS[destId]?.name ?: destId
                runOnUiThread {
                    UiKit.showItemsDialog(this, "🚌 $destName — how?",
                        arrayOf(
                            "🚶 Walk it — $legs legs, ~$walkMin min (the streets provide)",
                            "🚌 Ride the bus — the route at bus speed: ~$rideMin min, no stops"
                        )) { which ->
                        if (which == 1) boardBus(destId) else onExit(destId)
                    }
                }
            } else {
                // onExit runs its own bg block; call its body
                // inline here would double-hop threads — just
                // re-dispatch through it.
                runOnUiThread { onExit(destId) }
            }
        }
    }

    private fun rideBus(destId: String) {
        bg.execute {
            gameView.sitting = false
            val out = Walk.ride(state, destId,
                advance = !prefs.isRealtimeSync())
            // v0.16.0a: alighting steps you back outside
            state.playerInside = false
            prefs.saveGame(state)
            runOnUiThread {
                if (out.ok) {
                    gameView.centerCamera()
                    gameView.showBusArrival(school = false)
                    toast("🚌 The bus does its route at bus speed. " +
                        "${LOCATIONS[destId]?.name} — ${out.minutes} min, " +
                        "no stops, no stories. (The stories were the point, " +
                        "but the bus doesn't know that.)")
                } else {
                    toast("🚌 No ride for you — ${out.reason}.")
                }
                refreshUi()
                if (out.ok) maybeShowEpisodeCards()
            }
        }
    }

    /** v0.15.0c: the station flow — you walked to the sign,
     *  the bus pulled up, and now the network is yours:
     *  every other station, priced by BusNetwork's route
     *  derivation. No pass, no ride (the driver is firm). */
    private fun onBusStation(fromId: String) {
        bg.execute {
            val here = engine.player(state).location
            if (!Walk.hasPass(state)) {
                runOnUiThread {
                    UiKit.showMessageDialog(this, "🚌 The Bus",
                        message = "The bus pulls up. The driver looks " +
                            "at you, looks at your empty hands, and " +
                            "shakes his head: no pass, no ride. The " +
                            "transit kiosk inside the Mall sells one " +
                            "(${Walk.BUS_PASS_PRICE} 🪙) — and Town " +
                            "Fixtures (50 errands) ride on the house.",
                        positiveLabel = "OK")
                }
                return@execute
            }
            val dests = BusNetwork.destinationsFrom(here)
            val labels = dests.map { (id, mins) ->
                "🚌 ${LOCATIONS[id]?.name ?: id} — ~$mins min at bus speed"
            }.toTypedArray()
            runOnUiThread {
                UiKit.showItemsDialog(this,
                    "🚌 From ${LOCATIONS[here]?.name ?: here} — ride to…",
                    labels) { which ->
                    boardBus(dests[which].first)
                }
            }
        }
    }

    /** v0.16.0a (item 8): the school run — you CLIMB
     *  ABOARD first: a real school-bus interior, Crabtree
     *  at the wheel, your seat, and the ride completes
     *  when you tap the route board (or the door) to
     *  alight at the far end. */
    private fun onSchoolBus() {
        bg.execute {
            val here = engine.player(state).location
            val dest = SchoolBus.destFrom(state, here)
            if (dest == null) {
                runOnUiThread {
                    toast("🚌 The bus isn't standing there just now.")
                }
                return@execute
            }
            BusRideState.board(state, "school", here, dest)
            prefs.saveGame(state)
            runOnUiThread {
                gameView.playPlayerAction("board_bus")
                gameView.beginSit()
                gameView.centerCamera()
                toast("🚌 You climb aboard the school bus. " +
                    "Crabtree doesn't look up. Take a seat — " +
                    "tap the route board to ride to " +
                    "${LOCATIONS[dest]?.name}.")
                refreshUi()
            }
        }
    }

    /** v0.16.0a (item 9): board the public bus — the
     *  interior first, the ride when you alight. */
    private fun boardBus(destId: String) {
        bg.execute {
            val here = engine.player(state).location
            BusRideState.board(state, "public", here, destId)
            prefs.saveGame(state)
            runOnUiThread {
                gameView.playPlayerAction("board_bus")
                gameView.beginSit()
                gameView.centerCamera()
                toast("🚌 You climb aboard. The driver nods at " +
                    "your pass. Take a seat — tap the route " +
                    "board to ride to ${LOCATIONS[destId]?.name}.")
                refreshUi()
            }
        }
    }

    /** v0.16.0a (items 8+9): alighting — the ride itself
     *  runs through the very same Walk.ride /
     *  SchoolBus.ride the town has always used, so the
     *  minutes, prices and ledgers are unchanged; only
     *  the teleport is gone. */
    private fun completeBusRide() {
        val ride = BusRideState.read(state) ?: return
        BusRideState.clear(state)
        if (ride.school) completeSchoolRide(ride) else rideBus(ride.to)
    }

    private fun completeSchoolRide(ride: BusRideState.Ride) {
        bg.execute {
            gameView.sitting = false
            var out = SchoolBus.ride(state,
                advance = !prefs.isRealtimeSync())
            if (!out.ok) {
                // the window closed while you were aboard —
                // the run you boarded still lands you there
                val minutes = BusNetwork.rideMinutes(ride.from, ride.to)
                state.walkDest = ""
                engine.playerGo(state, ride.to)
                val ticks = Walk.applyMinutes(state, minutes,
                    !prefs.isRealtimeSync())
                state.life["schoolBusRides"] =
                    (state.life["schoolBusRides"] ?: 0) + 1
                out = Walk.RideOutcome(true, "", minutes, ticks)
            }
            state.playerInside = false
            prefs.saveGame(state)
            runOnUiThread {
                gameView.centerCamera()
                gameView.showBusArrival(school = true)
                toast(if (engine.player(state).location ==
                        SchoolBus.SCHOOL)
                    "🚌 The school bus rumbles you to school — " +
                        "${out.minutes} min, window seat, and " +
                        "somebody has already forgotten a lunchbox."
                else
                    "🚌 The school bus takes you back to the " +
                        "stop — ${out.minutes} min of window " +
                        "and somebody else's homework.")
                refreshUi()
                maybeShowEpisodeCards()
            }
        }
    }

    /** v0.16.0a (item 20): a named neighbor house's door —
     *  it leads to that house's own interior, as its own
     *  location has always worked. */
    private fun onHouseDoor(locId: String) {
        bg.execute {
            state.lifeText.remove("insideSection")
            engine.playerGo(state, locId)
            state.playerInside = true
            SoundEngine.door(open = true)
            prefs.saveGame(state)
            runOnUiThread {
                gameView.centerCamera()
                toast("🚪 ${LOCATIONS[locId]?.name} — inside.")
                refreshUi()
            }
        }
    }

    /** v0.16.0e pass 2: a fabricated fiction door — the
     *  cardboard door inside a claimed building, leading
     *  to a set that exists only while the game does.
     *  Rides the section machinery exactly (an overlay
     *  interior, never a base location). */
    private fun onFictionDoor(sectionId: String) {
        bg.execute {
            state.lifeText["insideSection"] = sectionId
            state.playerInside = true
            SoundEngine.door(open = true)
            prefs.saveGame(state)
            runOnUiThread {
                gameView.centerCamera()
                toast(if (sectionId == "fiction:dungeon")
                    "🚪 Beneath the Keep — a set, obviously. " +
                        "The torches are flashlights taped " +
                        "to chairs."
                else
                    "🚪 The lair — a set, obviously. Every " +
                        "gadget is a kitchen appliance in " +
                        "a costume.")
                refreshUi()
            }
        }
    }

    /** v0.16.0a (item 20): a section house's door — its
     *  own interior section of the street you're on. */
    private fun onSectionDoor(sectionId: String) {
        bg.execute {
            state.lifeText["insideSection"] = sectionId
            state.playerInside = true
            SoundEngine.door(open = true)
            prefs.saveGame(state)
            runOnUiThread {
                gameView.centerCamera()
                toast("🚪 ${ResidentialStreets.sectionName(sectionId)} — inside.")
                refreshUi()
            }
        }
    }

    /** v0.16.0a (items 7+17): usable furniture — sitting
     *  pieces seat you (animated), work/serve/read pieces
     *  play the use animation, beds are for your own
     *  house's bed flow, bus seats hold you for the ride. */
    private fun onLivingFurniture(id: String) {
        val parts = id.split(":")
        val kind = parts.getOrNull(2) ?: ""
        val action = parts.getOrNull(3) ?: "use"
        val label = InteriorLayouts.ALL.values.asSequence()
            .flatMap { it.furniture.asSequence() }
            .firstOrNull { f -> id.contains(":${f.id}:") }?.label
            ?: kind
        when (action) {
            "sit" -> {
                gameView.beginSit()
                toast("🪑 You sit down on the $label.")
            }
            "ride" -> {
                gameView.beginSit()
                toast("🚌 You take your seat.")
            }
            "sleep" -> {
                if (engine.player(state).location == YOUR_HOUSE &&
                    !state.lifeText.containsKey("insideSection")) {
                    runOnUiThread { bedDialog() }
                } else {
                    gameView.playPlayerAction("use_object")
                    toast("🛏️ Not your bed — it is, however, a " +
                        "perfectly good $label.")
                }
            }
            else -> {
                gameView.playPlayerAction(
                    ActionAnims.forTrigger(action).id)
                toast(when (action) {
                    "serve" -> "🍽️ You step up to the $label."
                    "work" -> "✏️ You get to work at the $label."
                    "read" -> "📚 You browse the $label."
                    "watch" -> "📺 You settle in by the $label."
                    else -> "👉 You use the $label."
                })
            }
        }
        refreshUi()
    }

    /** v0.5.0a: the transit kiosk inside the Mall — the one
     *  place in town that sells the bus pass (Walk.buyPass
     *  owns the transaction: price, the Town Fixture perk,
     *  the already-owns-it shrug). */
    private fun busKioskDialog() {
        bg.execute {
            val owned = Walk.hasPass(state)
            val fixture = Walk.townFixture(state)
            val coins = Minigames.capsuleCoins(state)
            runOnUiThread {
                when {
                    owned -> UiKit.showMessageDialog(this,
                        "🚌 Transit Kiosk",
                        message = "Your pass is on file, laminated and " +
                            "everything. The kiosk screen just says: " +
                            "\"WALKING STILL BETTER. — MGMT\"",
                        positiveLabel = "OK")
                    fixture -> UiKit.showItemsDialog(this,
                        "🚌 Transit Kiosk",
                        arrayOf("🎁 Claim your Town Fixture bus pass — FREE " +
                            "(50 errands earns it)")) {
                        settlePassPurchase()
                    }
                    else -> UiKit.showItemsDialog(this,
                        "🚌 Transit Kiosk",
                        arrayOf("🚌 Buy the bus pass — ${Walk.BUS_PASS_PRICE} 🪙 " +
                            "(you have $coins)")) {
                        settlePassPurchase()
                    }
                }
            }
        }
    }

    private fun settlePassPurchase() {
        bg.execute {
            val result = Walk.buyPass(state)
            prefs.saveGame(state)
            runOnUiThread {
                toast(when (result) {
                    "bought" -> "🚌 Bus pass bought! Rides now cost " +
                        "the route's minutes at bus speed — 🗺 Go " +
                        "Somewhere will offer them, and so will " +
                        "every bus stop sign in town."
                    "granted" -> "🚌 The kiosk prints your pass FREE — " +
                        "Town Fixture has its perks. Rides cost the " +
                        "route's minutes at bus speed now."
                    "poor" -> "🚌 The kiosk blinks: insufficient capsule " +
                        "coins. The pass is ${Walk.BUS_PASS_PRICE} 🪙 — " +
                        "minigames and errands pay them."
                    else -> "🚌 You already have the pass. It's in your " +
                        "wallet. It's laminated."
                })
                refreshUi()
            }
        }
    }

    /** v0.5.0c: the capsule machine inside the Mall — pull a
     *  Pocket Pal for 2 capsule coins (PocketPals.pull owns the
     *  roll: weighted rarities, dupes counted, Look exclusives
     *  never in the drum). The dialog re-opens after each pull;
     *  walking away is just not tapping it again. */
    private fun capsuleMachineDialog() {
        bg.execute {
            val coins = Minigames.capsuleCoins(state)
            val owned = PocketPals.ownedCount(state)
            val pulls = PocketPals.pulls(state)
            runOnUiThread {
                UiKit.showItemsDialog(this,
                    "🃏 Capsule Machine — album $owned/${PocketPals.CARDS.size}",
                    arrayOf(
                        "🃏 Pull a Pocket Pal — ${PocketPals.MACHINE_COST} 🪙 " +
                            "(you have $coins · $pulls pulls so far)",
                        "📖 Just look at the drum")) { which ->
                    if (which == 0) settlePalPull()
                    else toast("🃏 Twenty-two cards ride the drum. Two more " +
                        "hide somewhere in town, and the kids hold the rest " +
                        "of the economy hostage, as is tradition.")
                }
            }
        }
    }

    private fun settlePalPull() {
        bg.execute {
            val res = PocketPals.pull(state, kotlin.random.Random.Default)
            prefs.saveGame(state)
            runOnUiThread {
                if (res.status == "poor") {
                    toast("🃏 The machine blinks: a pull is " +
                        "${PocketPals.MACHINE_COST} 🪙. Minigames and " +
                        "errands pay capsule coins.")
                } else {
                    val card = res.card!!
                    val n = PocketPals.count(state, card)
                    toast("🃏 POP! ${card.name} — " +
                        "${PocketPals.schoolIcon(card.school)} ${card.school} · " +
                        (when (card.rarity) {
                            PocketPals.RARE -> "★ RARE!"
                            PocketPals.UNCOMMON -> "◆ uncommon"
                            else -> "common"
                        }) +
                        (if (n > 1) " (dupe — ×$n in the binder)" else " — NEW!") +
                        "\n${card.flavor}")
                    capsuleMachineDialog()
                }
                refreshUi()
            }
        }
    }

    /** v0.5.0b: the mirror is the wardrobe's home surface —
     *  one tap swaps between your saved preset sets (sanitized
     *  to owned pieces, like every look), and the creator is
     *  one more tap for anything deeper. Swapping changes your
     *  appearance for real, so the town's outfit-comment rail
     *  notices the new look — piece-specific lines included —
     *  the next time anyone sees you. */
    private fun mirrorDialog() {
        bg.execute {
            val presets = prefs.getCreatorPresets()
            val ownedN = Wardrobe.ownedCount(state)
            val labels = mutableListOf<String>()
            val slotOf = mutableListOf<Int>()
            for (i in presets.indices) {
                if (presets[i] != null) {
                    labels += "👕 Wear preset ${i + 1}"
                    slotOf += i
                }
            }
            labels += "🪞 Open the creator"
            // v0.10.0b: your hero identity is mirror business
            // too — the established re-open-creator route:
            // re-pick the name and power, and the costume
            // re-derives from whatever you're wearing now.
            val heroLabel = if (Heroes.heroCreated(state))
                "🦸 Hero identity — currently " +
                    Heroes.heroName(state) else null
            if (heroLabel != null) labels += heroLabel
            runOnUiThread {
                UiKit.showItemsDialog(this,
                    "🪞 Mirror — wardrobe $ownedN/${Wardrobe.PIECES.size} unlocked",
                    labels.toTypedArray()) { which ->
                    when {
                        which < slotOf.size ->
                            wearPreset(slotOf[which])
                        heroLabel != null &&
                            labels.getOrNull(which) == heroLabel ->
                            heroIdentityDialog(changing = true)
                        else -> {
                            prefs.saveGame(state)
                            toast("🪞 You regard yourself. Changes are possible. Dignity: pending.")
                            startActivity(Intent(this, CreatorActivity::class.java))
                        }
                    }
                }
            }
        }
    }

    private fun wearPreset(slot: Int) {
        bg.execute {
            val saved = prefs.getCreatorPresets().getOrNull(slot) ?: return@execute
            val look = Wardrobe.sanitizeAppearance(saved, state.collected,
                state.unlockedWardrobe.toSet())
            val me = engine.player(state)
            prefs.savePlayer(me.name, look)
            me.appearance = look
            prefs.saveGame(state)
            runOnUiThread {
                toast("👕 Preset ${slot + 1} on. The mirror approves. " +
                    "The town will have opinions.")
                refreshUi()
            }
        }
    }

    /** v0.5.0b: a wardrobe counter — the Mall's clothing rack
     *  and Jimbo's counter each sell their own pieces for
     *  Capsule Coins (Wardrobe.buy owns the transaction: the
     *  price, the wrong-counter shrug, insufficient funds). */
    private fun wardrobeShopDialog(locId: String) {
        bg.execute {
            val pieces = Wardrobe.piecesAt(locId)
            val coins = Minigames.capsuleCoins(state)
            val place = LOCATIONS[locId]?.name ?: locId
            val labels = pieces.map { p ->
                if (Wardrobe.owned(state, p)) "✅ ${p.name} — yours already"
                else "👕 ${p.name} — ${p.price} 🪙 (you have $coins)"
            }
            runOnUiThread {
                UiKit.showItemsDialog(this, "👕 $place — the good shelf",
                    labels.toTypedArray()) { which ->
                    val p = pieces.getOrNull(which) ?: return@showItemsDialog
                    settleWardrobePurchase(p.id, locId)
                }
            }
        }
    }

    private fun settleWardrobePurchase(pieceId: String, locId: String) {
        bg.execute {
            val p = Wardrobe.pieceById(pieceId)
            val result = Wardrobe.buy(state, pieceId, locId)
            prefs.saveGame(state)
            runOnUiThread {
                toast(when (result) {
                    "bought" -> "👕 ${p?.name} is YOURS. Wear it from the " +
                        "creator (or save the look and swap at your mirror). " +
                        "The town will notice."
                    "poor" -> "👕 Insufficient capsule coins — ${p?.name} is " +
                        "${p?.price} 🪙. Minigames and errands pay them."
                    "owned" -> "👕 You already own ${p?.name}. It's in the closet."
                    else -> "👕 Not sold at this counter."
                })
                refreshUi()
            }
        }
    }

    // ---------------- v0.1.8 Phase 2: living-world interactions ----------------

    private val DUCK_FOOD = setOf("cheesy_poof", "snack_cake", "hot_chocolate", "member_berry")

    /** v0.1.9a #98: an autonomous 3:33 AM crossing finished on
     *  screen — log it under ???, once a night (the dark-window
     *  tap path shares the same guard key). */
    private fun onSighting333() {
        bg.execute {
            if (state.lifeText["sighting333"] != state.todayKey) {
                state.lifeText["sighting333"] = state.todayKey
                state.life["sightings333"] = (state.life["sightings333"] ?: 0) + 1
                townLogLine(state, "??? — 3:33 AM: something small crossed the yard, all on its own")
                prefs.saveGame(state)
                runOnUiThread {
                    toast("👀 3:33 AM — something just crossed the yard. Logged under ???.")
                }
            }
        }
    }

    private fun onSceneTap(kind: String, id: String) {
        when (kind) {
            "pond" -> { gameView.scatterDucks(); pondDialog() }
            "chalk" -> {
                val locId = engine.player(state).location
                if (TownLife.chalkActive(state, locId)) {
                    val names = TownLife.chalkShown(state, locId)
                        .joinToString(", ") { TownLife.CHALK_DOODLES[it] }
                    val added = TownLife.chalkShownCount(state, locId) - 1
                    lookDialog("✏️ Chalk on the sidewalk here: $names." +
                        if (added > 0)
                            "\n\nSomebody's been adding to it while you weren't looking. " +
                                "It's basically a mural now."
                        else "\n\nStill fresh. The weather will take it within the week.")
                } else {
                    bg.execute {
                        val n = TownLife.CHALK_DOODLES.size
                        val idx = ((state.absDay * 7 + locId.hashCode()) % n + n) % n
                        state.lifeText["chalk:$locId"] = "$idx:${state.absDay}"
                        state.events.add("✏️ You chalked ${TownLife.CHALK_DOODLES[idx]} " +
                            "on the sidewalk at ${LOCATIONS[locId]?.name}.")
                        while (state.events.size > 40) state.events.removeAt(0)
                        prefs.saveGame(state)
                        runOnUiThread {
                            toast("✏️ You chalk ${TownLife.CHALK_DOODLES[idx]}. " +
                                "It'll live here for days.")
                            refreshUi()
                        }
                    }
                }
            }
            "mittentree" -> bg.execute {
                when {
                    TownLife.tryHangMitten(state) -> {
                        TownLife.repAdd(state, 1)
                        prefs.saveGame(state)
                        runOnUiThread {
                            toast("🧤 Mitten hung! The tree wears " +
                                "${TownLife.mittensHung(state)} now. (+rep)")
                            refreshUi()
                        }
                    }
                    TownLife.mittensHung(state) > 0 -> runOnUiThread {
                        lookDialog("🌲 The Lost Mitten Tree.\n\n" +
                            "${TownLife.mittensHung(state)} single mittens hang here, each " +
                            "one lost by somebody and found by somebody kinder. Bring it " +
                            "a lost mitten (they turn up as winter litter) and it will " +
                            "wear that one too.")
                    }
                    else -> runOnUiThread {
                        toast("🌲 A noble pine. It looks… incomplete. Like it's " +
                            "waiting for lost mittens.")
                    }
                }
            }
            "gnome" -> bg.execute {
                // v0.7.0b: 🔎 on the right night, the gnome has
                // a secret — the tap is its to give, once.
                val gnomeSecret = Secrets.propTap(state, "gnome")
                if (gnomeSecret != null) {
                    prefs.saveGame(state)
                    runOnUiThread {
                        lookDialog(gnomeSecret)
                        refreshUi()
                    }
                } else if (state.lifeText["gnomeDay"] == state.todayKey) {
                    runOnUiThread { toast("🧌 Still just standing there. Menacingly.") }
                } else {
                    state.lifeText["gnomeDay"] = state.todayKey
                    state.life["gnomeSeen"] = (state.life["gnomeSeen"] ?: 0) + 1
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🧌 Gnome sighting #${state.life["gnomeSeen"]}! " +
                            "Noted in your Journal. It saw you too.")
                    }
                }
            }
            "fixture" -> when (id) {
                "bed" -> bedDialog()
                "shelf" -> shelfDialog()
                "album" -> {
                    // the physical album opens the Journal's
                    // album shelf (same records PhotoView renders)
                    prefs.saveGame(state)
                    startActivity(Intent(this, JournalActivity::class.java)
                        .putExtra("section", "album"))
                }
                "table" -> hostDinnerDialog()
                "attic" -> atticDialog()
                "darkroom" -> darkroomDialog()
                "printwall" -> printWallDialog()
                "familyframe" -> {
                    val fam = Family.latestFamilyPhoto(state)
                    lookDialog(if (fam != null)
                        "👨‍👩‍👧 The family photo — ${fam.dateLabel}, " +
                            "right here at home. The frame waited for " +
                            "a real one, and got it."
                    else
                        "👨‍👩‍👧 An empty frame, hung with intent. " +
                            "Take a photo at home with Mom or Dad in " +
                            "it and this spot is theirs.")
                }
                "errandboard" -> boardDialog()
                "ballotbox" -> ballotDialog()
                // v0.15.0f: the Community Hall's show wall —
                // entries, the hanging, the hung show
                "gallerywall" -> galleryDialog()
                "stand" -> standDialog()
                "garden" -> gardenDialog()
                "paper" -> paperDialog()
                // v0.13.0e: the mailbox — the pen-pal
                // program's front door (read the letter,
                // write back, or check the route).
                "mailbox" -> mailboxDialog()
                // v0.13.0c: the screen is the talent
                // show's while the show owns it (poster
                // week, and show day itself); a screening
                // actually holding its evening keeps it.
                // v0.14.0c: on the festival's five days
                // the screen is the festival's — its own
                // session, its own jury — ahead of both.
                "moviescreen" -> when {
                    Festival.screenOpen(state) -> festivalDialog()
                    Talent.screenOpen(state) -> talentDialog()
                    else -> movieDialog()
                }
                // v0.13.0d: the reviewing stand on Main
                // Street — the parade's door through its
                // cycle (poster, the day's beats, the wrap).
                "parade" -> paradeDialog()
                "classroom" -> schoolDialog()
                "buskiosk" -> busKioskDialog()
                "capsulemachine" -> capsuleMachineDialog()
                "mirror" -> mirrorDialog()
                "clothesrack" -> wardrobeShopDialog("Mall")
                "gunrack" -> wardrobeShopDialog("JimbosGuns")
                "seedrack" -> seedCounterDialog()
                "snackcounter" -> goodsCounterDialog()
            }
            "bench" -> bg.execute {
                // v0.4.0b: a wobbly-bench errand turns this tap
                // into the repair; otherwise it's just a bench.
                val locId = engine.player(state).location
                val fix = Errands.onFixTap(state, "bench", locId)
                if (fix != null) {
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🔧 Tightened, shimmed, certified wobble-free. " +
                            "📋 Errand done for ${fix.errand.asker}! " +
                            "(+${fix.reward.friendship} friendship)" +
                            Errands.chainToastSuffix(fix.reward))
                        refreshUi()
                    }
                } else {
                    // v0.7.0b: 🔎 an accepted errand settles
                    // first; then, once, the bench itself has
                    // something to show a kid who looks under it.
                    val benchSecret = Secrets.propTap(state, "bench")
                    if (benchSecret != null) {
                        prefs.saveGame(state)
                        runOnUiThread {
                            lookDialog(benchSecret)
                            refreshUi()
                        }
                    } else {
                        runOnUiThread { benchSit() }
                    }
                }
            }
            "shovel" -> bg.execute {
                val me = engine.player(state)
                state.lifeText["shoveled:${me.location}"] = state.todayKey
                val owner = KID_HOUSE.entries.firstOrNull { it.value == me.location }?.key
                if (owner != null) engine.relAdjust(state, owner, 3)
                TownLife.repAdd(state, 1)
                actionTick()
                prefs.saveGame(state)
                runOnUiThread {
                    toast("🧹 Driveway cleared! ${owner ?: "The house"} owes you one. (+rep)")
                    refreshUi()
                }
            }
            "leafpile" -> bg.execute {
                val me = engine.player(state)
                state.life["leafScatter:${me.location}"] = state.absDay
                val diver = engine.presentWithPlayer(state).firstOrNull { !it.asleep }
                if (diver != null) {
                    state.events.add("🍂 ${diver.name} dove headfirst into the leaf pile at " +
                        "${LOCATIONS[me.location]?.name}. No hesitation. Full send.")
                    while (state.events.size > 40) state.events.removeAt(0)
                }
                prefs.saveGame(state)
                runOnUiThread {
                    toast(if (diver != null) "🍂 ${diver.name} dove straight in!"
                        else "🍂 You scatter the pile. It awaits its next victim elsewhere.")
                    refreshUi()
                }
            }
            "boat" -> bg.execute {
                val me = engine.player(state)
                val key = "boat:${me.location}"
                if (state.life[key] == null) {
                    state.life[key] = state.timeMin
                    runOnUiThread { toast("⛵ Away it goes — the great meltwater sea claims another.") }
                } else {
                    state.life.remove(key)
                    runOnUiThread { toast("⛵ You fish the soggy boat out and fold a fresh one.") }
                }
                prefs.saveGame(state)
                runOnUiThread { refreshUi() }
            }
            "kite" -> bg.execute {
                actionTick()
                prefs.saveGame(state)
                runOnUiThread {
                    toast("🪁 You fly the kite till your arms ache. Worth it.")
                    refreshUi()
                }
            }
            "lamp" -> bg.execute {
                val locId = engine.player(state).location
                state.lifeText["lampFixed:${state.absDay}"] = locId
                // v0.4.0b: the bonk is also a FIX verb — if
                // somebody's errand sent you to this lamp, the
                // repair settles it on the spot.
                val fix = Errands.onFixTap(state, "lamp", locId)
                prefs.saveGame(state)
                runOnUiThread {
                    toast(if (fix != null)
                        "💡 Bonk. Rock steady — and that's ${fix.errand.asker}'s " +
                            "errand done. 📋 (+${fix.reward.friendship} friendship)" +
                            Errands.chainToastSuffix(fix.reward)
                    else "💡 You bonk the streetlight. Rock steady — for tonight, anyway.")
                    refreshUi()
                }
            }
            "window" -> dreamDialog(id)
            "lemonade" -> bg.execute {
                val have = state.inventory["lemonade"] ?: 0
                if (have >= 3) {
                    runOnUiThread { toast("🍋 You're good on lemonade. The kid respects it.") }
                } else {
                    state.inventory["lemonade"] = have + 1
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🍋 One lemonade, 25¢ (the kid waves the fee). Sweet!")
                        refreshUi()
                    }
                }
            }
            "swap" -> swapDialog()
            // v0.2.0e: the minigame fixtures in the world
            "snowfort" -> snowfortDialog()
            "arcade" -> arcadeDialog()
            "flyer" -> {
                val notices = listOf(
                    "🐕 LOST DOG: answers to 'C'mere'. Last seen judging everyone near this fence.",
                    "🐈 LOST CAT: orange, smug, runs a whole route. If found, do NOT follow. It knows where it's going.",
                    "🎸 LOST: one (1) sense of dignity. Lost at Skeeter's. No questions asked.",
                    "🧤 LOST MITTEN: left hand, red. Its partner waits by the tree. You know the one."
                )
                val n = notices[(state.absDay + id.hashCode()) % notices.size]
                lookDialog("📌 Stuck to the fence, flapping:\n\n$n")
            }
        }
    }

    /** The pond hub: ducks, echoes, coins, snacks (#1/#63/#69/#70). */
    private fun pondDialog() {
        gameView.scatterDucks()
        val lastWord = state.lastConversation.lastOrNull()?.text
            ?.trim()?.split(Regex("\\s+"))?.lastOrNull()
            ?.trim('.', '!', '?', '"', '…') ?: "hello"
        val food = state.inventory.entries.firstOrNull {
            it.key in DUCK_FOOD && it.value > 0
        }?.key
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        options.add("📣 Shout at the pond"); actions.add {
            lookDialog("You shout across the water.\n\nSomewhere off the hills, " +
                "smaller, it comes back:\n\n“…$lastWord… $lastWord…”")
        }
        if (food != null) {
            val foodName = itemById(food)?.name ?: food
            options.add("🦆 Feed the ducks ($foodName)"); actions.add {
                bg.execute {
                    state.inventory[food] = (state.inventory[food] ?: 1) - 1
                    if ((state.inventory[food] ?: 0) <= 0) state.inventory.remove(food)
                    state.life["ducksFedDay"] = state.absDay
                    TownLife.repAdd(state, 1)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🦆 The ducks lose their minds. You are their god now.")
                        refreshUi()
                    }
                }
            }
        }
        if ((state.inventory["bottle_cap"] ?: 0) > 0) {
            options.add("🪙 Toss a bottle cap for luck"); actions.add {
                bg.execute {
                    state.inventory["bottle_cap"] = (state.inventory["bottle_cap"] ?: 1) - 1
                    if ((state.inventory["bottle_cap"] ?: 0) <= 0) {
                        state.inventory.remove("bottle_cap")
                    }
                    state.life["luckDay"] = state.absDay
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🪙 Plink. The pond keeps your luck safe — " +
                            "today's trash treasure odds just got better.")
                        refreshUi()
                    }
                }
            }
        }
        // v0.2.0e: actually fish it (the minigame — see Minigames.kt)
        options.add("🎣 Fish the pond"); actions.add { startFishing() }
        // v0.11.0e: during carnival build days, the shore
        // row takes sculpture sessions — half an hour of
        // real clock each, filed under the sky as it stands
        // (Carnival owns the rules and the record).
        if (Carnival.buildOpen(state)) {
            val done = Carnival.sessionsToday(state)
            options.add("⛄ Work on your snow sculpture " +
                "(${Carnival.SCULPT_MINUTES} min) — session " +
                "${done + 1} of ${Carnival.SCULPT_PER_DAY} today")
            actions.add {
                bg.execute {
                    val line = Carnival.buildSession(state)
                    if (line != null) {
                        repeat(Carnival.SCULPT_MINUTES / 15) {
                            engine.advanceTime(state) }
                    }
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        UiKit.showMessageDialog(this,
                            "⛄ The Shore Row",
                            message = line ?: "The row is closed " +
                                "for today — the judges take it " +
                                "as it stands.")
                    }
                }
            }
        }
        options.add("Just watch the water"); actions.add { }
        UiKit.showItemsDialog(this, "🌊 Stark's Pond",
            options.toTypedArray()) { which -> actions[which]() }
    }

    // ================= v0.2.0e: minigame launchers =================
    // The sessions are pure (Minigames.kt); the views draw and
    // take taps (MinigameViews.kt); settling happens here, once
    // per session, through the inventory/friendship systems the
    // town already runs on. Records live in the Journal 🎮 GAMES.

    /** A paper dialog holding one game view; the view's clock
     *  stops when the dialog goes away, however it goes away. */
    private fun gameDialog(title: String, sub: String, view: View, heightDp: Int,
                           onShow: () -> Unit, onHide: () -> Unit) {
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply {
            text = sub
            setTextColor(Color.parseColor(Chrome.INK))
            textSize = 13.5f
            setPadding(0, 0, 0, dp(8))
        })
        view.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp))
        col.addView(view)
        val dlg = UiKit.paperDialog(this, title, col, positive = "Done" to {})
        dlg.setOnDismissListener { onHide() }
        dlg.show()
        onShow()
    }

    /** 🎣 Fishing at Stark's Pond: tap the pond, pick the rod. */
    private fun startFishing() {
        val view = FishingView(this)
        view.playerSpec = avatarSpec(engine.player(state).appearance)
        view.sessionFactory = {
            // v0.3.0e: on a meteor night the pond's rare fish
            // rise to the streaks (Feel.METEOR_RARE_MULT).
            FishingSession(System.nanoTime(), state.weatherId, state.timeMin / 60,
                meteorNight = SkyState.meteorNow(state.absDay, state.timeMin,
                    state.weatherId, currentSeason()))
        }
        view.onCast = {
            noteCast(state)
            prefs.saveGame(state)
        }
        view.onCaught = { sess ->
            val r = applyCatch(state, sess)
            prefs.saveGame(state)
            refreshUi()
            val featTxt = r?.feats?.joinToString(" ") {
                "🏆 " + (Minigames.featById(it)?.name ?: it)
            } ?: ""
            val head = when {
                r == null -> ""
                r.species.joke ->
                    "🥾 The legendary Old Boot — ${r.cm} cm of disappointment."
                r.isNew -> "🎣 NEW CATCH! ${r.species.icon} ${r.species.name} — ${r.cm} cm!"
                else -> "🎣 ${r.species.icon} ${r.species.name} — ${r.cm} cm"
            }
            if (featTxt.isEmpty()) head else "$head · $featTxt"
        }
        // v0.2.0g: a first entry on this save carries its how-to
        val howtoF = Minigames.firstEntryHowTo(state, "fishing")
        if (howtoF != null) prefs.saveGame(state)
        gameDialog("🎣 Stark's Pond",
            "casts ${state.fishCasts} · caught ${Minigames.totalCaught(state)} · " +
                "dawn, dusk & rain bite best · a blizzard bites nobody" +
                (howtoF?.let { "\n$it" } ?: ""),
            view, 360, onShow = { view.begin() }, onHide = { view.stop() })
    }

    /** ❄️ The duel itself vs [opponent]. */
    private fun startShowdown(opponent: String) {
        val duel = ShowdownDuel(System.nanoTime(), opponent)
        val view = ShowdownView(this)
        view.duel = duel
        view.playerSpec = avatarSpec(engine.player(state).appearance)
        view.playerName = engine.player(state).name
        view.onDuelOver = { d ->
            val r = applyShowdownResult(state, d)
            prefs.saveGame(state)
            refreshUi()
            val lines = mutableListOf<String>()
            if (r != null) {
                lines.add(if (r.youWon) "🏆 YOU TAKE THE FORT!"
                    else "💀 The fort falls to ${d.opponent}.")
                lines.add("“${r.line}”")
                lines.add("🪙 +${r.coins} capsule coins · friendship " +
                    if (r.friendshipDelta >= 0) "+${r.friendshipDelta}"
                    else "${r.friendshipDelta}")
                r.feats.forEach {
                    lines.add("🏆 Feat: ${Minigames.featById(it)?.name ?: it}")
                }
            }
            lines
        }
        val rec = state.showdown[opponent]
        // v0.2.0g: a first entry on this save carries its how-to
        val howtoS = Minigames.firstEntryHowTo(state, "showdown")
        if (howtoS != null) prefs.saveGame(state)
        gameDialog("❄️ Snowball Showdown",
            "vs $opponent · first to ${Showdown.HITS_TO_WIN} hits" +
                (if (rec != null) " · your record: ${rec.wins}W–${rec.losses}L" else "") +
                " · release in the red — dodge the incoming, late" +
                (howtoS?.let { "\n$it" } ?: ""),
            view, 400, onShow = { view.begin() }, onHide = { view.stop() })
    }

    /** Tapping the fort itself: pick your opponent, or get the
     *  snow lecture, or find the fort unguarded. */
    private fun snowfortDialog() {
        if (state.weatherId !in Minigames.SNOW_WEATHERS) {
            UiKit.showMessageDialog(this, "🏆 The Fort Provides (Later)",
                message = "Three planks and a dream. A Snowball Showdown needs real " +
                    "snow — come back when it's falling and bring a kid to challenge.")
            return
        }
        val opps = Minigames.showdownOpponents(state)
        if (opps.isEmpty()) {
            UiKit.showMessageDialog(this, "❄️ The Empty Fort",
                message = "Perfect snow, and not one kid around to defend the fort " +
                    "or lose it. Even the flag looks bored.")
            return
        }
        val labels = opps.map { n ->
            val rec = state.showdown[n]
            "🏆 Challenge $n" + (if (rec != null) " (${rec.wins}W–${rec.losses}L)" else "")
        }.toTypedArray()
        UiKit.showItemsDialog(this, "❄️ Defend the Fort", labels) { which ->
            startShowdown(opps[which])
        }
    }

    /** 🕹️ The cabinet inside Freeman's Tacos. */
    private fun arcadeDialog() {
        UiKit.showItemsDialog(this, "🕹️ TACO CATCH",
            arrayOf("🌮 Play Taco Catch (60s)", "📖 How it pays")) { which ->
            if (which == 0) startTacoCatch() else UiKit.showMessageDialog(this,
                "🌮 Taco Catch — the cabinet rules",
                message = "Drag the tray. Tacos +10, burritos +15. Memberberries " +
                    "(-15) and falling TVs (-25) stun the tray — dodge them like " +
                    "they owe you money.\n\nPayout in 🪙 capsule coins: " +
                    "80+ → 2 · 180+ → 4 · 300+ → 6 · 450+ → 9 · 600+ → 12. " +
                    "High score and feats live in the Journal, under 🎮 GAMES.")
        }
    }

    /** 🌮 One 60-second run at the cabinet. */
    private fun startTacoCatch() {
        val session = TacoCatchSession(System.nanoTime())
        val view = TacoCatchView(this)
        view.session = session
        view.bestScore = state.tacoHigh
        view.onRunOver = { sess ->
            val r = applyTacoResult(state, sess)
            prefs.saveGame(state)
            refreshUi()
            val lines = mutableListOf("⏰ TIME!")
            if (r != null) {
                lines.add("🌮 Score ${r.score}" +
                    if (r.newHigh) " — NEW HIGH! 🏆" else "")
                lines.add("🪙 +${r.coins} capsule coins")
                r.feats.forEach {
                    lines.add("🏆 Feat: ${Minigames.featById(it)?.name ?: it}")
                }
            }
            lines
        }
        // v0.2.0g: a first entry on this save carries its how-to
        val howtoT = Minigames.firstEntryHowTo(state, "taco")
        if (howtoT != null) prefs.saveGame(state)
        gameDialog("🌮 Taco Catch",
            "60 seconds · high score ${state.tacoHigh} · the cabinet pays in capsule coins" +
                (howtoT?.let { "\n$it" } ?: ""),
            view, 430, onShow = { view.begin() }, onHide = { view.stop() })
    }

    // ================= v0.4.0b: the request board =================
    // Inside City Hall, under McDaniels' portrait: the town's
    // small work, pinned fresh every morning. The postings are
    // derived from the day (Errands.boardFor) — this dialog is
    // just their cork-and-paper face: take one, review your
    // plate, or drop one with nothing but a sigh.

    private fun boardDialog() {
        val open = Errands.boardOpen(state)
        val act = Errands.active(state)
        if (open.isEmpty() && act.isEmpty()) {
            UiKit.showMessageDialog(this, "📋 The Request Board",
                message = "Bare cork. Every note's been taken or run, " +
                    "and the Mayor's office has gone home. Fresh ink " +
                    "tomorrow — the town always needs something.")
            return
        }
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        for (e in open) {
            labels.add("📌 ${e.asker}: ${Errands.objective(e)}")
            actions.add { errandDetailDialog(e, fromBoard = true) }
        }
        if (act.isNotEmpty()) {
            labels.add("🗂️ Your plate (${act.size}/${Errands.MAX_ACTIVE}) — review / drop")
            actions.add { activeErrandsDialog() }
        }
        UiKit.showItemsDialog(this, "📋 City Hall Request Board",
            labels.toTypedArray()) { which -> actions[which]() }
    }

    // ================= v0.6.0f: the ballot box =================
    // Under the request board: one question a week, one
    // ballot per citizen, changeable until the count. The
    // rules live in Votes; this is their paper face.

    private fun ballotDialog() {
        val q = Votes.currentQuestion(state)
        val mine = Votes.playerVote(state)
        val mineLabel = q.options.firstOrNull { it.id == mine }?.label
        val w = Votes.playerWeight(state)
        UiKit.showMessageDialog(this, "🗳 ${q.title}",
            message = "${q.ask}\n\n" +
                (if (mineLabel != null)
                    "Your ballot is in: '$mineLabel'. You can change " +
                        "it here until the week turns (Thursday)."
                else
                    "You haven't voted yet. One ballot — changeable " +
                        "until the week turns (Thursday).") +
                (if (w > 1)
                    "\n\nYour ballot counts ×$w: the town knows a " +
                        "${Errands.titleFor(state.errandsDoneCount)} when it sees one."
                else ""),
            positiveLabel = "🗳 Vote",
            onPositive = { ballotOptionsDialog() },
            negativeLabel = "Not now")
    }

    private fun ballotOptionsDialog() {
        val q = Votes.currentQuestion(state)
        val mine = Votes.playerVote(state)
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        for (o in q.options) {
            labels.add((if (mine == o.id) "✔ " else "🗳 ") +
                "${o.label} — championed by ${o.champion}")
            val oid = o.id
            actions.add {
                bg.execute {
                    val ok = Votes.castVote(state, oid)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast(if (ok)
                            "🗳 Ballot in: '${o.label}'. The box accepts it gravely."
                        else
                            "🗳 That option isn't on this week's paper.")
                        refreshUi()
                    }
                }
            }
        }
        if (mine != null) {
            labels.add("🗑 Take my ballot back")
            actions.add {
                bg.execute {
                    Votes.retractVote(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🗳 Ballot retrieved. The box pretends not to notice.")
                        refreshUi()
                    }
                }
            }
        }
        UiKit.showItemsDialog(this, "🗳 ${q.title}",
            labels.toTypedArray()) { which -> actions[which]() }
    }

    // ================= v0.7.0c: the Bijou's screen =================
    // Friday premieres, Saturday headlines. The rules live
    // in MovieNights; this is the velvet rope. The feature is
    // eleven beats: cards, the room reacting, three prompts
    // where your timing is the buzz. Admission free — the
    // town already paid for the print.

    private fun movieDialog() {
        val status = MovieNights.sessionStatus(state)
        when (status.kind) {
            MovieNights.ST_SEATING -> {
                UiKit.showMessageDialog(this, "🎬 The Bijou",
                    message = status.text,
                    positiveLabel = "🎟 Take your seat",
                    onPositive = {
                        bg.execute {
                            val ok = MovieNights.seat(state)
                            if (ok) {
                                // trailers: the house settles,
                                // the lights go down
                                actionTick(); actionTick()
                            }
                            prefs.saveGame(state)
                            runOnUiThread {
                                if (ok) presentMovieBeat()
                                else toast("🎬 The usher shakes their head — " +
                                    "that showing's gone.")
                            }
                        }
                    },
                    negativeLabel = "Not tonight")
            }
            MovieNights.ST_RESUME -> presentMovieBeat()
            else -> UiKit.showMessageDialog(this, "🎬 The Bijou",
                status.text, positiveLabel = "OK")
        }
    }

    private fun presentMovieBeat() {
        val beat = MovieNights.sessionBeat(state) ?: run {
            refreshUi()
            return
        }
        when (beat.kind) {
            "prompt" -> {
                val labels = MovieNights.REACTIONS.map { it.second }
                    .toTypedArray()
                UiKit.showItemsDialog(this,
                    "🎬 ${MovieNights.featureFor(state).title}",
                    labels) { which ->
                    val rid = MovieNights.REACTIONS[which].first
                    bg.execute {
                        val response = MovieNights.react(state, rid)
                        actionTick()
                        prefs.saveGame(state)
                        runOnUiThread {
                            if (response != null) {
                                UiKit.showMessageDialog(this, "🎬 The Bijou",
                                    response, positiveLabel = "▶",
                                    onPositive = { presentMovieBeat() })
                            } else presentMovieBeat()
                        }
                    }
                }
            }
            "credits" -> {
                UiKit.showMessageDialog(this, "🎬 The Bijou",
                    beat.text, positiveLabel = "🍿 House lights",
                    onPositive = {
                        bg.execute {
                            MovieNights.advance(state)   // the wrap
                            actionTick()
                            prefs.saveGame(state)
                            runOnUiThread { refreshUi() }
                        }
                    })
            }
            else -> {
                UiKit.showMessageDialog(this,
                    if (beat.kind == "card") "🎬 ${MovieNights.featureFor(state).title}"
                    else "🎬 The Bijou",
                    beat.text, positiveLabel = "▶",
                    onPositive = {
                        bg.execute {
                            MovieNights.advance(state)
                            prefs.saveGame(state)
                            runOnUiThread { presentMovieBeat() }
                        }
                    })
            }
        }
    }

    // ================= v0.14.0c: the film festival =================
    // Five December nights at the Bijou. The rules live
    // in Festival; this is the same velvet rope the
    // Friday premieres use — the festival session is the
    // feature's own eleven beats, with the jury reading
    // its cards at Friday's credits and one photograph
    // while the closing-night house is still lit.

    private fun festivalDialog() {
        val status = Festival.sessionStatus(state)
        when (status.kind) {
            Festival.ST_SEATING -> {
                UiKit.showMessageDialog(this, "🎬 The Bijou",
                    message = status.text,
                    positiveLabel = "🎟 Take your seat",
                    onPositive = {
                        bg.execute {
                            val ok = Festival.seat(state)
                            if (ok) {
                                // trailers: the house settles,
                                // the lights go down
                                actionTick(); actionTick()
                            }
                            prefs.saveGame(state)
                            runOnUiThread {
                                if (ok) presentFestivalBeat()
                                else toast("🎬 The usher shakes their head — " +
                                    "that showing's gone.")
                            }
                        }
                    },
                    negativeLabel = "Not tonight")
            }
            Festival.ST_RESUME -> presentFestivalBeat()
            Festival.ST_AWARD -> {
                UiKit.showMessageDialog(this, "🎬 The Bijou",
                    message = status.text,
                    positiveLabel = "📸 Take the festival photo",
                    onPositive = {
                        bg.execute {
                            val text = Festival.takePhoto(state)
                            prefs.saveGame(state)
                            runOnUiThread {
                                refreshUi()
                                if (text != null) {
                                    UiKit.showMessageDialog(this,
                                        "📸 The Festival Photo",
                                        text, positiveLabel = "OK")
                                } else toast("📸 The moment's gone — " +
                                    "the house has drifted home.")
                            }
                        }
                    },
                    negativeLabel = "Not now")
            }
            else -> UiKit.showMessageDialog(this, "🎬 The Bijou",
                status.text, positiveLabel = "OK")
        }
    }

    // ================= v0.15.0f: the gallery show wall =================
    // The Community Hall's exhibition wall at City Hall:
    // the submission window (enter from your real album,
    // lend a darkroom print, withdraw), hanging night's
    // three beats, the hung wall, and the judged results.
    // Every rule lives in Gallery; this is its paper face.

    private fun galleryDialog() {
        val status = Gallery.hallStatus(state)
        when (status.kind) {
            Gallery.ST_SUBMIT -> {
                UiKit.showMessageDialog(this, "🖼 The Gallery Show",
                    message = status.text,
                    positiveLabel = "🖼 Enter a print",
                    onPositive = { galleryEntryDialog() },
                    negativeLabel = "Not now")
            }
            Gallery.ST_HANGING -> {
                val beat = Gallery.hangBeat(state)
                if (beat == null) {
                    UiKit.showMessageDialog(this, "🖼 The Gallery Show",
                        status.text, positiveLabel = "OK")
                } else {
                    UiKit.showMessageDialog(this, "🖼 Hanging Night",
                        message = beat,
                        positiveLabel = "🖼 Keep hanging",
                        onPositive = {
                            bg.execute {
                                Gallery.advanceHang(state)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    refreshUi()
                                    galleryDialog()
                                }
                            }
                        },
                        negativeLabel = "Step out")
                }
            }
            Gallery.ST_OPEN -> {
                bg.execute {
                    Gallery.visitShow(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        UiKit.showMessageDialog(this,
                            "🖼 The Gallery Show",
                            status.text, positiveLabel = "OK")
                    }
                }
            }
            else -> UiKit.showMessageDialog(this, "🖼 The Gallery Show",
                status.text, positiveLabel = "OK")
        }
    }

    /** The entry picker: every photograph you could
     *  enter (this season's album frames, your held
     *  darkroom prints) and every frame of yours already
     *  in, offered back for withdrawal. All eligibility
     *  is Gallery's; this lists and forwards. */
    private fun galleryEntryDialog() {
        val cal = Gallery.currentCal(state)
        val show = Gallery.recordFor(state, cal)
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        for (rec in Gallery.eligibleAlbum(state, cal, show)) {
            labels += "📷 Enter: ${Photos.captionFor(rec)}"
            val id = rec.id
            actions += {
                bg.execute {
                    val ok = Gallery.enterAlbum(state, id)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast(if (ok)
                            "🖼 In — your frame is on the list " +
                                "for the hanging."
                        else "🖼 That one's not eligible.")
                        refreshUi()
                    }
                }
            }
        }
        for (p in Gallery.eligiblePrints(state, cal, show)) {
            labels += "🖨 Lend a print: ${Photos.captionFor(p.record)}"
            val id = p.id
            actions += {
                bg.execute {
                    val ok = Gallery.enterPrint(state, id)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast(if (ok)
                            "🖼 Lent — the print hangs for the " +
                                "run and comes home after."
                        else "🖼 That print can't be lent " +
                            "right now.")
                        refreshUi()
                    }
                }
            }
        }
        for (e in Gallery.playerEntriesOf(show)) {
            labels += "↩ Withdraw: ${Photos.captionFor(e.record)}"
            val id = e.photoId
            actions += {
                bg.execute {
                    val ok = Gallery.withdraw(state, id)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast(if (ok)
                            "🖼 Withdrawn — the frame comes " +
                                "back to you."
                        else "🖼 The wall is set — that " +
                            "frame stays.")
                        refreshUi()
                    }
                }
            }
        }
        if (labels.isEmpty()) {
            UiKit.showMessageDialog(this, "🖼 The Gallery Show",
                "Nothing of yours can go up this season — " +
                    "no photographs from this season in the " +
                    "album, and no held darkroom prints to " +
                    "lend. The show hangs anyway; the " +
                    "Journal's page says so, honestly.",
                positiveLabel = "OK")
            return
        }
        UiKit.showItemsDialog(this, "🖼 Enter the Show",
            labels.toTypedArray()) { which -> actions[which]() }
    }

    private fun presentFestivalBeat() {
        val beat = Festival.sessionBeat(state) ?: run {
            refreshUi()
            return
        }
        val film = Festival.tonight(state)?.let { (y, n) ->
            Festival.filmFor(y, n) }
        when (beat.kind) {
            "prompt" -> {
                val labels = MovieNights.REACTIONS.map { it.second }
                    .toTypedArray()
                UiKit.showItemsDialog(this,
                    "🎬 ${film?.title ?: "The Festival"}",
                    labels) { which ->
                    val rid = MovieNights.REACTIONS[which].first
                    bg.execute {
                        val response = Festival.react(state, rid)
                        actionTick()
                        prefs.saveGame(state)
                        runOnUiThread {
                            if (response != null) {
                                UiKit.showMessageDialog(this, "🎬 The Bijou",
                                    response, positiveLabel = "▶",
                                    onPositive = { presentFestivalBeat() })
                            } else presentFestivalBeat()
                        }
                    }
                }
            }
            "credits" -> {
                UiKit.showMessageDialog(this, "🎬 The Bijou",
                    beat.text, positiveLabel = "🍿 House lights",
                    onPositive = {
                        bg.execute {
                            Festival.advance(state)   // the wrap
                            actionTick()
                            prefs.saveGame(state)
                            runOnUiThread { refreshUi() }
                        }
                    })
            }
            else -> {
                UiKit.showMessageDialog(this,
                    if (beat.kind == "card") "🎬 ${film?.title ?: "The Festival"}"
                    else "🎬 The Bijou",
                    beat.text, positiveLabel = "▶",
                    onPositive = {
                        bg.execute {
                            Festival.advance(state)
                            prefs.saveGame(state)
                            runOnUiThread { presentFestivalBeat() }
                        }
                    })
            }
        }
    }

    // ================= v0.13.0c: talent night =================
    // The Bijou's screen in the show's own week: the
    // poster and the signup sheet, the rehearsal, and —
    // on the night — the beat chain off the record
    // (arrival, the acts in posted order with the room's
    // reaction prompts between them, your act on the
    // showdown bar, and the results). Talent owns every
    // rule; this shell plays the answers.

    private fun talentDialog() {
        if (Talent.sessionOpen(state)) {
            presentTalentBeat()
            return
        }
        val status = Talent.screenStatus(state)
        if (status.kind == "done" ||
            status.kind == "yielded") {
            UiKit.showMessageDialog(this, "🎤 The Bijou",
                status.text, positiveLabel = "OK")
            return
        }
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        if (Talent.signupWindowOpen(state)) {
            if (!Talent.entered(state)) {
                for (act in Talent.PLAYER_ACTS) {
                    labels.add("✍️ Sign up — ${act.label}")
                    actions.add {
                        bg.execute {
                            val (_, text) = Talent.signUp(
                                state, act.id)
                            prefs.saveGame(state)
                            runOnUiThread {
                                UiKit.showMessageDialog(this,
                                    "🎤 Talent night", text,
                                    positiveLabel = "OK",
                                    onPositive = { refreshUi() })
                            }
                        }
                    }
                }
            } else {
                if (Talent.rehearseOpen(state)) {
                    labels.add("🎤 Rehearse your act " +
                        "(30 min on the Bijou's stage)")
                    actions.add {
                        bg.execute {
                            val text = Talent.rehearse(state)
                            prefs.saveGame(state)
                            runOnUiThread {
                                UiKit.showMessageDialog(this,
                                    "🎤 Rehearsal",
                                    text ?: "The stage is " +
                                        "busy right now.",
                                    positiveLabel = "OK",
                                    onPositive = { refreshUi() })
                            }
                        }
                    }
                }
                for (act in Talent.PLAYER_ACTS) {
                    labels.add("🎤 My act — ${act.label}")
                    actions.add {
                        bg.execute {
                            val (_, text) = Talent.signUp(
                                state, act.id)
                            prefs.saveGame(state)
                            runOnUiThread {
                                UiKit.showMessageDialog(this,
                                    "🎤 Talent night", text,
                                    positiveLabel = "OK",
                                    onPositive = { refreshUi() })
                            }
                        }
                    }
                }
            }
        }
        labels.add("📋 Read the poster")
        actions.add {
            UiKit.showMessageDialog(this, "🎤 The Bijou",
                Talent.posterText(state),
                positiveLabel = "OK")
        }
        UiKit.showItemsDialog(this, "🎤 The Bijou",
            labels.toTypedArray()) { which ->
            actions[which]()
        }
    }

    private fun presentTalentBeat() {
        when (Talent.nextBeatKind(state)) {
            "arrive" -> {
                val text = Talent.playArrival(state)
                if (text == null) {
                    refreshUi()
                    return
                }
                UiKit.showMessageDialog(this, "🎤 The Bijou",
                    text, positiveLabel = "▶",
                    onPositive = { presentTalentBeat() })
            }
            "act" -> {
                val text = Talent.playAct(state)
                if (text == null) {
                    presentTalentBeat()
                    return
                }
                UiKit.showMessageDialog(this, "🎤 The Bijou",
                    text, positiveLabel = "▶",
                    onPositive = { presentTalentBeat() })
            }
            "react" -> {
                val labels = MovieNights.REACTIONS
                    .map { it.second }.toTypedArray()
                UiKit.showItemsDialog(this,
                    "🎤 The Bijou — the room", labels) { which ->
                    val rid = MovieNights.REACTIONS[which].first
                    bg.execute {
                        val response = Talent.react(state, rid)
                        prefs.saveGame(state)
                        runOnUiThread {
                            if (response != null) {
                                UiKit.showMessageDialog(this,
                                    "🎤 The Bijou", response,
                                    positiveLabel = "▶",
                                    onPositive = {
                                        presentTalentBeat() })
                            } else presentTalentBeat()
                        }
                    }
                }
            }
            "youract" -> startTalentAct()
            "results" -> {
                val text = Talent.finishNight(state)
                UiKit.showMessageDialog(this, "🎤 The Bijou",
                    text ?: "🎤 The night settles.",
                    positiveLabel = "🏆 House lights",
                    onPositive = {
                        prefs.saveGame(state)
                        refreshUi()
                    })
            }
            else -> talentDialog()
        }
    }

    /** 🎤 Your act, played: the session runs on the
     *  showdown bar (its own phrases, sweep and shake per
     *  act kind), and the judging lands through Talent,
     *  once, when the view closes the session. */
    private fun startTalentAct() {
        val rec = Talent.currentRecord(state) ?: return
        val act = Talent.playerAct(rec.playerAct) ?: return
        val session = Talent.TalentSession(act,
            Talent.sessionSeed(state))
        val view = TalentActView(this)
        view.session = session
        view.onActOver = { s ->
            val lines = Talent.judgePlayerAct(state, s)
            prefs.saveGame(state)
            refreshUi()
            (lines ?: listOf("🎤 The act happened. The " +
                "meter is still deciding how it went.")) +
                "(The night goes on — the screen at the " +
                    "Bijou holds the next beat.)"
        }
        gameDialog("🎤 Your Act — ${act.label}",
            "${act.phrases} phrases · tap to perform, " +
                "release in the zones — red is steadiest · " +
                "your act: ${act.blurb}",
            view, 400, onShow = { view.begin() },
            onHide = {
                view.stop()
                // a judged act hands the night back to the
                // chain; an abandoned one just closes
                if ((Talent.currentRecord(state)
                        ?.playerScore ?: -1) >= 0) {
                    presentTalentBeat()
                }
            })
    }

    // ================= v0.13.0d: the town parade =================
    // The reviewing stand on Main Street is the parade's
    // door: the poster and the posted order outside the
    // day, the day's own beats on it — choose your part,
    // the lineup, the pass chunk by chunk, a catch session
    // for each throwing float, one photograph, and the
    // Mayor's car to close. Parade owns every rule; this
    // only presents them.

    private fun paradeDialog() {
        if (Parade.sessionOpen(state)) {
            presentParadeBeat()
            return
        }
        val status = Parade.standStatus(state)
        if (!Parade.standOpen(state) ||
            status.kind == "done" || status.kind == "yielded") {
            UiKit.showMessageDialog(this, "🎺 The reviewing stand",
                status.text, positiveLabel = "OK")
            return
        }
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        labels.add("📋 The procession order")
        actions.add {
            UiKit.showMessageDialog(this, "🎺 The stand",
                Parade.posterText(state), positiveLabel = "OK")
        }
        labels.add("🎺 Read the poster")
        actions.add {
            UiKit.showMessageDialog(this, "🎺 The reviewing stand",
                status.text, positiveLabel = "OK")
        }
        UiKit.showItemsDialog(this, "🎺 The reviewing stand",
            labels.toTypedArray()) { which -> actions[which]() }
    }

    private fun presentParadeBeat() {
        when (Parade.nextBeatKind(state)) {
            "choose" -> {
                val labels = mutableListOf<String>()
                val actions = mutableListOf<() -> Unit>()
                labels.add(if (Parade.marchEntryFor(state) ==
                    "crew") "🎺 March — with your crew"
                    else "🎺 March — walk with the school entry")
                actions.add {
                    bg.execute {
                        val text = Parade.playChoose(state,
                            "march", "")
                        prefs.saveGame(state)
                        runOnUiThread {
                            if (text != null) {
                                UiKit.showMessageDialog(this,
                                    "🎺 The parade", text,
                                    positiveLabel = "▶",
                                    onPositive = {
                                        presentParadeBeat() })
                            } else presentParadeBeat()
                        }
                    }
                }
                for (sp in Parade.SPOTS) {
                    labels.add("👀 Watch — ${sp.label}")
                    actions.add {
                        bg.execute {
                            val text = Parade.playChoose(state,
                                "watch", sp.id)
                            prefs.saveGame(state)
                            runOnUiThread {
                                if (text != null) {
                                    UiKit.showMessageDialog(this,
                                        "🎺 The parade", text,
                                        positiveLabel = "▶",
                                        onPositive = {
                                            presentParadeBeat() })
                                } else presentParadeBeat()
                            }
                        }
                    }
                }
                UiKit.showItemsDialog(this, "🎺 Take your place",
                    labels.toTypedArray()) { which ->
                    actions[which]() }
            }
            "lineup" -> {
                val text = Parade.playLineup(state)
                if (text == null) {
                    refreshUi()
                    return
                }
                UiKit.showMessageDialog(this, "🎺 The parade",
                    text, positiveLabel = "▶",
                    onPositive = { presentParadeBeat() })
            }
            "pass" -> {
                val text = Parade.playPass(state)
                if (text == null) {
                    presentParadeBeat()
                    return
                }
                UiKit.showMessageDialog(this, "🎺 The parade",
                    text, positiveLabel = "▶",
                    onPositive = { presentParadeBeat() })
            }
            "catch" -> {
                val thrower = Parade.catchOwed(state)
                if (thrower == null) {
                    presentParadeBeat()
                    return
                }
                startParadeCatch(thrower)
            }
            "photo" -> {
                val options = Parade.photoOptions(state)
                if (options.isEmpty()) {
                    presentParadeBeat()
                    return
                }
                val labels = options.map {
                    "📸 ${it.name}" }.toTypedArray()
                UiKit.showItemsDialog(this,
                    "🎺 One photograph — which float?",
                    labels) { which ->
                    bg.execute {
                        val text = Parade.takePhoto(state,
                            options[which].id)
                        prefs.saveGame(state)
                        runOnUiThread {
                            if (text != null) {
                                UiKit.showMessageDialog(this,
                                    "🎺 The parade", text,
                                    positiveLabel = "▶",
                                    onPositive = {
                                        presentParadeBeat() })
                            } else presentParadeBeat()
                        }
                    }
                }
            }
            "mayor" -> {
                val text = Parade.playMayor(state)
                UiKit.showMessageDialog(this, "🎺 The parade",
                    text ?: "🎺 The day settles.",
                    positiveLabel = "🏁 The street reopens",
                    onPositive = {
                        prefs.saveGame(state)
                        refreshUi()
                    })
            }
            else -> paradeDialog()
        }
    }

    /** 🍬 The catch, played: three throws from the float
     *  on the showdown bar; the judging lands through
     *  Parade, once, when the view closes the session. */
    private fun startParadeCatch(throwerId: String) {
        val entry = Parade.entryOf(state,
            Parade.currentYear(state), throwerId)
        val session = Parade.CatchSession(throwerId,
            Parade.catchSeed(state, throwerId))
        val view = ParadeCatchView(this)
        view.session = session
        view.floatName = entry?.name ?: "the float"
        view.onCatchOver = { s ->
            val lines = Parade.judgeCatch(state, s)
            prefs.saveGame(state)
            refreshUi()
            (lines ?: listOf("🍬 The candy happened. The " +
                "curb is still deciding how it went.")) +
                "(The stand on the street holds the " +
                    "next beat.)"
        }
        gameDialog("🍬 Candy — ${entry?.name ?: "the float"}",
            "${Parade.THROWS_PER} throws · tap to catch, " +
                "release in the zones — red is steadiest · " +
                "pockets hold ${Parade.CANDY_CAP} for the day",
            view, 400, onShow = { view.begin() },
            onHide = {
                view.stop()
                // a settled catch hands the day back to the
                // chain; an abandoned one just closes
                val rec = Parade.currentRecord(state)
                if (rec != null && throwerId in rec.catches) {
                    presentParadeBeat()
                }
            })
    }

    // ================= v0.7.0e: the climax stage =================
    // A shenanigan's playable climax, in Climaxes' keeping:
    // a chain of three judged beats through the event's
    // causers — the choice row for the Talk and Look beats
    // and for HOLD YOUR GROUND's surges, result cards between
    // beats, and a small card for the out-of-band PHOTO/FIND
    // beats (whose verbs are the shutter and the careful
    // Look, out in the world).

    private fun climaxDialog() {
        presentClimaxBeat()
    }

    private fun presentClimaxBeat() {
        val beat = Climaxes.sessionBeat(state) ?: run {
            refreshUi()
            return
        }
        val title = "🎬 ${beat.title}"
        when (beat.kind) {
            "choice", "stand" -> {
                UiKit.showItemsDialog(this, title,
                    beat.options.toTypedArray()) { which ->
                    bg.execute {
                        if (beat.kind == "choice")
                            Climaxes.choose(state, which)
                        else Climaxes.standChoose(state, which)
                        prefs.saveGame(state)
                        runOnUiThread { presentClimaxBeat() }
                    }
                }
            }
            "task" -> {
                UiKit.showItemsDialog(this, title,
                    arrayOf("✔ It's done — show them",
                        "✋ Let this one go",
                        "↩ Step away for now")) { which ->
                    bg.execute {
                        val note = Climaxes.taskAction(state, which)
                        prefs.saveGame(state)
                        runOnUiThread {
                            when {
                                note != null -> UiKit.showMessageDialog(
                                    this, title, note, positiveLabel = "OK")
                                which != 2 -> presentClimaxBeat()
                                else -> refreshUi()
                            }
                        }
                    }
                }
            }
            "result" -> {
                UiKit.showMessageDialog(this, title,
                    beat.text, positiveLabel = "▶",
                    onPositive = {
                        bg.execute {
                            Climaxes.advance(state)
                            prefs.saveGame(state)
                            runOnUiThread { presentClimaxBeat() }
                        }
                    })
            }
            "done" -> {
                UiKit.showMessageDialog(this, title,
                    beat.text, positiveLabel = "🎬 Curtain",
                    onPositive = { refreshUi() })
            }
            else -> {   // "intro"
                UiKit.showMessageDialog(this, title,
                    beat.text, positiveLabel = "▶ Step in",
                    onPositive = {
                        bg.execute {
                            Climaxes.begin(state)
                            prefs.saveGame(state)
                            runOnUiThread { presentClimaxBeat() }
                        }
                    },
                    negativeLabel = "Not now")
            }
        }
    }

    // ================= v0.7.0d: the classroom board =================
    // School days, in School.kt's keeping: the day's lesson
    // (two beats, the room reacting, one participation
    // moment), the midday recess beat, and Friday's bee —
    // letter by letter off the choice row. The board is the
    // rope; the rules are School's.

    private fun schoolDialog() {
        if (School.lessonStatus(state).kind == School.LS_IN_SESSION) {
            presentLessonBeat()
            return
        }
        if (School.beeStatus(state) == School.BS_IN_SESSION) {
            presentBeeBeat()
            return
        }
        val lesson = School.lessonStatus(state)
        val recess = School.recessStatus(state)
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        // v0.13.0b: 💃 the dance's beats ride the same
        // board on the night — arrival, the played slow
        // dance, the punch table, the last song (Dance
        // owns the order and the gates; the labels are
        // its own).
        for (label in Dance.beatLabels(state)) {
            labels += label
            actions += { danceBeat(label) }
        }
        if (lesson.kind == School.LS_AVAILABLE) {
            labels += "🎓 Sit in: “${lesson.topic.title}” — ${lesson.teacher}"
            actions += {
                bg.execute {
                    val ok = School.startLesson(state)
                    if (ok) actionTick()
                    prefs.saveGame(state)
                    runOnUiThread {
                        if (ok) presentLessonBeat()
                        else toast("🎓 That lesson's gone — the day's moved on.")
                    }
                }
            }
        }
        if (School.beeStatus(state) == School.BS_OPEN) {
            labels += "🐝 Enter the spelling bee — Victoria presiding"
            actions += {
                bg.execute {
                    val ok = School.enterBee(state)
                    if (ok) actionTick()
                    prefs.saveGame(state)
                    runOnUiThread {
                        if (ok) presentBeeBeat()
                        else toast("🐝 The chairs are full — that bee's gone.")
                    }
                }
            }
        }
        if (recess.kind == School.RS_AVAILABLE) {
            labels += "🍎 Recess: ${recess.beat!!.title} " +
                "(${recess.a} & ${recess.b})"
            actions += { recessDialog() }
        }
        // v0.10.0c: 🎭 the play's beats ride the same board:
        // auditions on the audition afternoon (pick a track;
        // auditioners then give the played read), and
        // staying for rehearsal on rehearsal afternoons.
        if (Play.auditionOpen(state)) {
            labels += "🎭 Auditions today — “${Play.TITLE}” " +
                "(this room, till 5)"
            actions += { playAuditionDialog() }
        }
        if (Play.rehearsalOpen(state)) {
            labels += Play.rehearsalLabel(state)
            actions += {
                bg.execute {
                    val text = Play.attendRehearsal(state)
                    if (text != null) actionTick()
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "🎭 Rehearsal", message = text)
                    }
                }
            }
        }
        // v0.14.0b: 🚌 the field trip's boarding rides the
        // same board on trip morning — the bus loads from
        // here, first bell to the half hour, and not one
        // minute later (FieldTrip owns the rules and the
        // arrival text).
        if (FieldTrip.boardOpen(state)) {
            labels += FieldTrip.boardLabel(state)
            actions += {
                bg.execute {
                    val text = FieldTrip.boardBus(state)
                    if (text != null) actionTick()
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "🚌 The Field Trip",
                            message = text)
                        else toast("🚌 The bus just left — " +
                            "it does that.")
                    }
                }
            }
        }
        // v0.13.0e: ✉️ the pen-pal program's signup rides
        // the same board while its October window is open —
        // the box by the board, the pairing the same
        // afternoon (PenPals owns the rules and the text).
        if (PenPals.signupOpen(state)) {
            labels += "✉️ Pen-pal program — put your name " +
                "in the box (pairings this week!)"
            actions += {
                bg.execute {
                    val text = PenPals.signUp(state)
                    prefs.saveGame(state)
                    runOnUiThread {
                        refreshUi()
                        if (text != null) UiKit.showMessageDialog(
                            this, "✉️ The Pen-Pal Program",
                            message = text)
                        else toast("✉️ The box just closed — " +
                            "next October.")
                    }
                }
            }
        }
        if (labels.isEmpty()) {
            boardInfoDialog()
            return
        }
        labels += "📋 Read the board"
        actions += { boardInfoDialog() }
        UiKit.showItemsDialog(this, "🎓 The Classroom Board",
            labels.toTypedArray()) { which -> actions[which]() }
    }

    /** v0.13.0b: one of the dance's beats, by its board
     *  label. The slow dance opens its played view; the
     *  punch table offers what's actually in your pantry
     *  (plus the cooler, forever); the rest land through
     *  Dance and read back as the night's own text. */
    private fun danceBeat(label: String) {
        when {
            label.startsWith("🎵") -> startSlowDance()
            label.startsWith("🥤") -> punchTableDialog()
            else -> bg.execute {
                val text = if (label.startsWith("💃"))
                    Dance.playArrival(state)
                else Dance.playLastSong(state)
                if (text != null) actionTick()
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (text != null) UiKit.showMessageDialog(
                        this,
                        if (label.startsWith("💃"))
                            "💃 ${Dance.NAME}"
                        else "🌙 The Last Song",
                        message = text)
                }
            }
        }
    }

    /** v0.13.0b: the punch table — your own stock, poured
     *  and shared: a lemonade becomes a punch, a snack
     *  cake is a ceremony, and the cooler is free and
     *  endless and nobody's favorite. Every serving is
     *  consumed exactly (Dance.servePunch owns the
     *  counts); what's not in your pantry isn't offered. */
    private fun punchTableDialog() {
        val opts = mutableListOf<String>()
        if (Dance.punchStock(state, "lemonade") > 0)
            opts += "🥤 Pour a punch (your lemonade, " +
                "${Dance.punchStock(state, "lemonade")} in your bag)"
        if (Dance.punchStock(state, "snack_cake") > 0)
            opts += "🧁 Break out a snack cake (yours, " +
                "${Dance.punchStock(state, "snack_cake")} in your bag)"
        opts += "💧 The water cooler (free)"
        UiKit.showItemsDialog(this, "🥤 The Punch Table",
            opts.toTypedArray()) { which ->
            val item = when {
                opts[which].startsWith("🥤") -> "lemonade"
                opts[which].startsWith("🧁") -> "snack_cake"
                else -> "water"
            }
            bg.execute {
                val text = Dance.servePunch(state, item)
                if (text != null) actionTick()
                prefs.saveGame(state)
                runOnUiThread {
                    refreshUi()
                    if (text != null) UiKit.showMessageDialog(
                        this, "🥤 The Punch Table",
                        message = text)
                    else toast("🥤 The table's moment has passed.")
                }
            }
        }
    }

    /** The board, read plain: today's business in prose. */
    private fun boardInfoDialog() {
        val lines = mutableListOf<String>()
        School.lookText(state, "Elementary")?.let { lines += it }
        Play.lookText(state, "Elementary")?.let { lines += it }
        Dance.boardLine(state)?.let { lines += it }
        FieldTrip.lookText(state, "Elementary")?.let { lines += it }
        val lesson = School.lessonStatus(state)
        lines += when (lesson.kind) {
            School.LS_DONE -> "✅ Today's lesson is sat — " +
                "“${lesson.topic.title}”, in the Journal under 🎓 SCHOOL."
            School.LS_BEFORE -> "Class starts at 9. The board's " +
                "up, the chairs are down, ${lesson.teacher} is " +
                "somewhere being ready."
            School.LS_RECESS -> "It's recess — class resumes " +
                "at 1, and the yard has the floor until then."
            School.LS_AFTER -> "School's out. The board keeps " +
                "today's lesson up for the buses: “${lesson.topic.title}”."
            School.LS_NOT_SCHOOL_DAY -> "No school today. The " +
                "board rests, and so does everyone who fears it."
            else -> ""
        }
        lines += when (School.beeStatus(state)) {
            School.BS_DONE -> "🐝 Today's bee is in the book — " +
                "the Journal's 🎓 SCHOOL page keeps the result."
            School.BS_BEFORE -> "🐝 Bee day: chairs out at 1 PM, " +
                "Victoria presiding."
            School.BS_AFTER -> "🐝 Today's bee has flown. " +
                "Fridays, 1–3 PM — the list only gets longer."
            else -> ""
        }
        UiKit.showMessageDialog(this, "🎓 The Classroom Board",
            lines.filter { it.isNotEmpty() }.joinToString("\n"),
            positiveLabel = "OK")
    }

    private fun presentLessonBeat() {
        val beat = School.lessonBeat(state) ?: run {
            refreshUi()
            return
        }
        when (beat.kind) {
            "prompt" -> {
                UiKit.showMessageDialog(this, "🎓 ${beat.speaker}",
                    beat.text, positiveLabel = "✋ Your moment",
                    onPositive = {
                        val opts = School.lessonOptions(state)
                        UiKit.showItemsDialog(this, "🎓 ${beat.speaker}",
                            opts.map { it.second }.toTypedArray()) { which ->
                            bg.execute {
                                School.answerLesson(state, opts[which].first)
                                actionTick()
                                prefs.saveGame(state)
                                runOnUiThread { presentLessonBeat() }
                            }
                        }
                    })
            }
            else -> {
                UiKit.showMessageDialog(this, "🎓 ${beat.speaker}",
                    beat.text, positiveLabel = "▶",
                    onPositive = {
                        bg.execute {
                            School.advanceLesson(state)
                            prefs.saveGame(state)
                            runOnUiThread {
                                if (School.lessonBeat(state) != null)
                                    presentLessonBeat()
                                else refreshUi()
                            }
                        }
                    })
            }
        }
    }

    private fun recessDialog() {
        val rs = School.recessStatus(state)
        if (rs.kind != School.RS_AVAILABLE) {
            toast("🍎 The yard moment's passed.")
            return
        }
        val beat = rs.beat!!; val a = rs.a!!; val b = rs.b!!
        UiKit.showMessageDialog(this, "🍎 ${beat.title}",
            School.fillBeat(beat.setup, a, b),
            positiveLabel = "▶ Referee it",
            onPositive = {
                val labels = beat.options
                    .map { School.fillBeat(it.label, a, b) }
                    .toTypedArray()
                UiKit.showItemsDialog(this, "🍎 ${beat.title}",
                    labels) { which ->
                    bg.execute {
                        val outcome = School.playRecess(state,
                            beat.options[which].id, a, b)
                        if (outcome != null) actionTick()
                        prefs.saveGame(state)
                        runOnUiThread {
                            if (outcome != null) {
                                UiKit.showMessageDialog(this,
                                    "🍎 ${beat.title}", outcome,
                                    positiveLabel = "OK",
                                    onPositive = { refreshUi() })
                            } else {
                                toast("🍎 The moment passed — recess is fickle.")
                                refreshUi()
                            }
                        }
                    }
                }
            })
    }

    private fun presentBeeBeat() {
        val beat = School.beeBeat(state) ?: run {
            refreshUi()
            return
        }
        when (beat.kind) {
            "letter" -> {
                val opts = School.letterOptions(state)
                UiKit.showItemsDialog(this,
                    "🐝 “${School.currentWord(state)}” — " +
                        School.spellingProgress(state),
                    opts.map { it.uppercaseChar().toString() }
                        .toTypedArray()) { which ->
                    bg.execute {
                        School.pickLetter(state, opts[which])
                        actionTick()
                        prefs.saveGame(state)
                        runOnUiThread { presentBeeBeat() }
                    }
                }
            }
            "done" -> {
                UiKit.showMessageDialog(this, "🐝 The Bee",
                    beat.text, positiveLabel = "🏆 Leave the gym",
                    onPositive = {
                        bg.execute {
                            School.advanceBee(state)   // clear the chair
                            actionTick()
                            prefs.saveGame(state)
                            runOnUiThread { refreshUi() }
                        }
                    })
            }
            else -> {
                UiKit.showMessageDialog(this, "🐝 The Bee",
                    beat.text, positiveLabel = "▶",
                    onPositive = {
                        bg.execute {
                            School.advanceBee(state)
                            prefs.saveGame(state)
                            runOnUiThread { presentBeeBeat() }
                        }
                    })
            }
        }
    }

    private fun activeErrandsDialog() {
        val act = Errands.active(state)
        if (act.isEmpty()) {
            toast("📋 Nothing on your plate.")
            return
        }
        val labels = act.map {
            "${if (it.source == Errands.EMERG) "🚨" else "📋"} " +
                Errands.objective(it) }.toTypedArray()
        UiKit.showItemsDialog(this, "🗂️ Your errands", labels) { which ->
            errandDetailDialog(act[which], fromBoard = false)
        }
    }

    private fun errandDetailDialog(e: Errand, fromBoard: Boolean) {
        // v0.8.0c: an emergency shows its clock and its
        // rescue rates — the base rates are only half its pay
        val isEmerg = e.source == Errands.EMERG
        val fr = (Errands.FRIENDSHIP[e.type] ?: 0) +
            if (isEmerg) Emergencies.RESCUE_FRIENDSHIP_BONUS else 0
        val coins = (Errands.COINS[e.type] ?: 0) +
            if (isEmerg) Emergencies.RESCUE_COIN_BONUS else 0
        UiKit.showMessageDialog(this,
            "${if (isEmerg) "🚨" else "📋"} ${Errands.displayAsker(e)}",
            message = "“${e.text}”\n\n🎯 ${Errands.objective(e)}\n" +
                "🧭 ${Errands.hintFor(state, e)}\n" +
                (if (isEmerg)
                    "\n⏰ By ${minutesToTimeStr(e.emergDeadline)} — " +
                        "${(e.emergDeadline - state.timeMin).coerceAtLeast(0)} " +
                        "minutes left on the clock. It does not extend.\n"
                else "") +
                "\nPays: +$fr friendship" +
                (if (coins > 0) " · +$coins 🪙 capsule coins" else "") +
                " · sometimes a thank-you gift" +
                (Errands.chainTeaser(e)?.let { "\n\n$it" } ?:
                    if (e.chainLink > 0 && e.chainLen >= 2)
                        "\n\n🧵 Link ${e.chainLink} of ${e.chainLen} in " +
                            "${e.chainFrom}'s favor chain — dropping " +
                            "drops the whole chain."
                    else ""),
            positiveLabel = if (fromBoard) "📌 Take it" else "Drop errand",
            onPositive = {
                bg.execute {
                    if (fromBoard) {
                        val ok = Errands.accept(state, e)
                        prefs.saveGame(state)
                        runOnUiThread {
                            toast(if (ok)
                                "📋 Errand taken: ${Errands.objective(e)}"
                            else
                                "📋 Your plate's full (${Errands.MAX_ACTIVE} errands) — " +
                                    "finish or drop one first.")
                            refreshUi()
                        }
                    } else {
                        val sigh = Errands.abandon(state, e.id)
                        prefs.saveGame(state)
                        runOnUiThread {
                            refreshUi()
                            UiKit.showMessageDialog(this, "📋 Dropped",
                                message = "You take the note down.\n\n" +
                                    "“${sigh ?: "Oh. Okay."}” — ${Errands.displayAsker(e)}. " +
                                    "No harm done. The board keeps turning.")
                        }
                    }
                }
            },
            negativeLabel = "Leave it")
    }

    /** #65 BENCH SITTING: stop, breathe, let the town come to you. */
    /** v0.3.0d: THE BED — sleep until morning. A dialogue-safe
     *  moment only: mid-conversation or mid-card, the bed defers
     *  politely like every other queued thing in this town. The
     *  sleep itself is the engine's natural night run forward;
     *  what you get on waking is the morning report (House). */
    private fun bedDialog() {
        if (dialogueOpen() || episodeCardShowing || finaleChoiceShowing) {
            toast("🛏️ Not mid-conversation. Even your bed has manners.")
            return
        }
        if (state.timeMin == House.WAKE_MIN) {
            toast("☀️ It's already morning. The bed releases you.")
            return
        }
        UiKit.showItemsDialog(this, "🛏️ Your bed",
            arrayOf("😴 Sleep until morning (7:00 AM)",
                "📖 Just sit on the edge a minute",
                "🛌 Sleepover plans…")) { which ->
            if (which == 1) {
                toast("🛏️ You sit on the edge of the bed. It's a good bed. " +
                    "It has never once judged you.")
                return@showItemsDialog
            }
            if (which == 2) {
                sleepoverDialog()
                return@showItemsDialog
            }
            bg.execute {
                val eventsBefore = state.events.toList()
                val arcsBefore = state.storyArcs.mapValues {
                    it.value.stage to it.value.done
                }
                val chapterBefore = state.arcChapter
                val retiredBefore = state.arcRetired
                engine.sleepUntilMorning(state)
                // the mail is the NEW morning's (seeded by the
                // day you wake into, not the one you left)
                val mailed = House.mailLine(state)
                val report = House.morningReport(state,
                    House.newEvents(eventsBefore, state.events),
                    arcsBefore, chapterBefore, retiredBefore, mailed)
                prefs.saveGame(state)
                runOnUiThread {
                    lookDialog(report.joinToString("\n\n"))
                    refreshUi()
                    maybeShowEpisodeCards()
                }
            }
        }
    }

    /** v0.9.0d: THE DARKROOM — the red-lit corner. Using it
     *  opens the album (the same records the Journal's album
     *  shelf shows, newest first): pick a photo, pick a size,
     *  and the town's own clock does the developing — the
     *  cost and the minutes are stated before you commit,
     *  the gate is Darkroom.developBlock, and the world
     *  that advances is the real one. The box manages here
     *  too: hang a print, admire the light. */
    private fun darkroomDialog() {
        if (dialogueOpen() || episodeCardShowing || finaleChoiceShowing) {
            toast("🌑 Not mid-conversation. The red light waits.")
            return
        }
        val held = Darkroom.held(state)
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        val block = Darkroom.developBlock(state, Darkroom.SIZE_PRINT)
        if (block == null) {
            labels.add("🖨 Develop a photo… " +
                "(prints ${held.size}/${Darkroom.BOX_CAP})")
            actions.add { developPicker() }
        } else {
            labels.add("🌑 The trays stay dry for now")
            actions.add { lookDialog(block) }
        }
        for (p in Darkroom.boxed(state)) {
            labels.add("🖼 Hang: ${Darkroom.shortLabel(p)}")
            actions.add { hangPrint(p.id) }
        }
        labels.add("🌑 Just watch the red light a minute")
        actions.add {
            toast("🌑 In the red dark, yesterday's light is " +
                "still becoming something. The trays hum.")
        }
        UiKit.showItemsDialog(this, "🌑 The Darkroom",
            labels.toTypedArray()) { which ->
            actions.getOrNull(which)?.invoke()
        }
    }

    /** The album, as the darkroom sees it: newest first,
     *  each photo labeled with the caption the album itself
     *  gives it. The pick is the photo; the size comes next. */
    private fun developPicker() {
        val photos = state.photos.asReversed()
        val labels = photos.map { rec ->
            val loc = LOCATIONS[rec.locId]?.name ?: rec.locId
            "📸 ${rec.dateLabel} · $loc — ${Photos.captionFor(rec)}"
        }
        UiKit.showItemsDialog(this, "📸 The Album — pick a photo",
            labels.toTypedArray()) { which ->
            photos.getOrNull(which)?.let { developSizePicker(it) }
        }
    }

    private fun developSizePicker(rec: PhotoRecord) {
        val coins = Minigames.capsuleCoins(state)
        UiKit.showItemsDialog(this, "🖨 Develop this photo?",
            arrayOf(
                "🖨 Print — ${Darkroom.PRINT_COST} 🪙 · " +
                    "${Darkroom.PRINT_MINUTES} min in the trays",
                "🖼 Enlargement, framed — ${Darkroom.ENLARGE_COST} 🪙 · " +
                    "${Darkroom.ENLARGE_MINUTES} min (giftable, " +
                    "from the gift menu)",
                "📸 Back to the album (you have $coins 🪙)")) { which ->
            when (which) {
                0 -> doDevelop(rec.id, Darkroom.SIZE_PRINT)
                1 -> doDevelop(rec.id, Darkroom.SIZE_ENLARGEMENT)
            }
        }
    }

    private fun doDevelop(photoId: String, size: String) {
        bg.execute {
            val res = Darkroom.develop(state, photoId, size)
            prefs.saveGame(state)
            runOnUiThread {
                refreshUi()
                if (res == null) {
                    toast("The tray comes up empty — the " +
                        "moment moved on.")
                } else {
                    val p = res.print
                    val forgot = res.forgotten?.let {
                        "\n\n(The box's oldest gifted print — " +
                            "${Darkroom.shortLabel(it)}, held by " +
                            "${it.owner} — is forgotten to make " +
                            "room. Twenty-four is the house's " +
                            "whole memory for these.)"
                    } ?: ""
                    UiKit.showMessageDialog(this, "🌑 Developed",
                        message = "Under the red light, the " +
                            "picture surfaces: ${Darkroom.captionOf(p)}\n\n" +
                            "${Darkroom.shortLabel(p)} — " +
                            "${res.minutes} minutes at the trays, " +
                            "${res.cost} 🪙 of paper, and the " +
                            "photo in the album is exactly as it " +
                            "was. This copy is the print's own " +
                            "now — it'll outlive the album if it " +
                            "has to." + forgot)
                }
            }
        }
    }

    private fun hangPrint(printId: String) {
        bg.execute {
            val ok = Darkroom.hang(state, printId)
            prefs.saveGame(state)
            runOnUiThread {
                refreshUi()
                if (!ok) toast("🖼 The wall holds " +
                    "${Darkroom.WALL_CAP} — take one down first.")
            }
        }
    }

    /** v0.9.0d: THE PRINT WALL — read it, swap it. Captions
     *  come off the prints' own copies (Darkroom.wallText);
     *  hanging and taking down are free, like it says. */
    private fun printWallDialog() {
        if (dialogueOpen() || episodeCardShowing || finaleChoiceShowing) {
            toast("🖼 One conversation at a time.")
            return
        }
        val labels = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()
        labels.add("📖 Read the wall")
        actions.add { lookDialog(Darkroom.wallText(state)) }
        for (p in Darkroom.wall(state)) {
            labels.add("⬇️ Take down: ${Darkroom.shortLabel(p)}")
            actions.add {
                bg.execute {
                    Darkroom.unhang(state, p.id)
                    prefs.saveGame(state)
                    runOnUiThread { refreshUi() }
                }
            }
        }
        if (Darkroom.wall(state).size < Darkroom.WALL_CAP) {
            for (p in Darkroom.boxed(state)) {
                labels.add("🖼 Hang: ${Darkroom.shortLabel(p)}")
                actions.add { hangPrint(p.id) }
            }
        }
        UiKit.showItemsDialog(this, "🖼 The Print Wall",
            labels.toTypedArray()) { which ->
            actions.getOrNull(which)?.invoke()
        }
    }

    /** v0.3.0d: the trophy shelf, read out loud — what's earned,
     *  what's still empty shelf, and what would fill it. */
    /** v0.11.0c: THE SLEEPOVER PLAN — proposed from your
     *  bed or a crew kid's menu, for FRIDAY nights only.
     *  The list is Sleepovers.planFor verbatim: free guests
     *  read as coming, claimed guests are NAMED with their
     *  reason (never silently swapped), and the crew's
     *  grown-ups carry the plain rule. Below the minimum
     *  the dialog proposes a different Friday instead. */
    private fun sleepoverDialog() {
        bg.execute {
            val plan = Sleepovers.planFor(state)
            val planned = Sleepovers.plannedTonight(state)
            val lines = mutableListOf<String>()
            val labels = mutableListOf<String>()
            val actions = mutableListOf<() -> Unit>()
            if (planned) {
                lines.add("Tonight's the night — " +
                    Sleepovers.plannedGuests(state)
                        .joinToString(", ") +
                    ", at your house, 6 PM to lights-out at 10.")
                lines.add("The evening so far: " +
                    "${Sleepovers.beatsLanded(state)} of " +
                    "${Sleepovers.STEP_COUNT} beats in " +
                    "(snacks, the movie, the pillow fight, " +
                    "lights out).")
                if (Sleepovers.beatsLanded(state) == 0) {
                    labels.add("🚪 Call it off — not tonight after all")
                    actions.add {
                        bg.execute {
                            if (Sleepovers.cancel(state)) {
                                prefs.saveGame(state)
                                runOnUiThread {
                                    toast("🛌 Called off. The bags " +
                                        "go back in the closet.")
                                    refreshUi()
                                }
                            }
                        }
                    }
                }
            } else {
                if (plan.nightReason.isNotEmpty())
                    lines.add(plan.nightReason)
                if (plan.inviteFamily != null)
                    lines.add("📩 You're invited to the " +
                        "${plan.inviteFamily} table tonight — " +
                        "a sleepover at yours is your regrets, " +
                        "sent by sleeping bag. (Their own kids " +
                        "are claimed at their table either way.)")
                for (g in plan.guests) {
                    if (g.ok) lines.add("✅ ${g.name} — ${g.reason}")
                    else {
                        labels.add("🔒 ${g.name}")
                        val reason = g.reason
                        val nm = g.name
                        actions.add {
                            UiKit.showMessageDialog(this,
                                "🛌 $nm can't make it",
                                message = reason,
                                positiveLabel = "OK",
                                onPositive = {})
                        }
                    }
                }
                for (g in plan.notGuests)
                    lines.add("🚫 ${g.name} — ${g.reason}.")
                if (plan.canPropose) {
                    labels.add("🛌 Propose tonight's sleepover")
                    actions.add {
                        bg.execute {
                            val okGo = Sleepovers.propose(state)
                            prefs.saveGame(state)
                            runOnUiThread {
                                toast(if (okGo)
                                    "🛌 It's on — the crew's " +
                                        "coming over at 6."
                                else "🛌 The plan changed under " +
                                    "you — check it again.")
                                refreshUi()
                            }
                        }
                    }
                }
            }
            val msg = lines.joinToString("\n\n")
                .ifEmpty { "🛌 Nothing to plan right now." }
            runOnUiThread {
                UiKit.showItemsDialog(this,
                    "🛌 Sleepover — Friday nights, your house",
                    (listOf(msg) + labels).toTypedArray()) { which ->
                    if (which == 0) return@showItemsDialog
                    actions.getOrNull(which - 1)?.invoke()
                }
            }
        }
    }

    /** v0.6.0d: THE DINING TABLE — family evenings are
     *  hosted from here. The list is Dinners.hostOptions
     *  verbatim: who's eligible tonight, and the town's own
     *  reason for everyone who isn't. */
    private fun hostDinnerDialog() {
        // v0.10.0a: a tap on your own dining table can first
        // settle the family's table-setting chore
        Family.noteFixture(state, "table")?.let { choreDone ->
            prefs.saveGame(state)
            toast("🍽 $choreDone")
        }
        bg.execute {
            val hosting = Dinners.hostingTonight(state)
            val decor = House.decorFor(state).size
            val pantry = Dinners.PANTRY_ITEMS.count {
                (state.inventory[it] ?: 0) > 0
            }
            runOnUiThread {
                if (hosting != null) {
                    val n = Dinners.hostedStepsDone(state)
                    val labels = mutableListOf<String>()
                    val actions = mutableListOf<() -> Unit>()
                    if (n == 0) {
                        labels.add("🚪 Call it off — not tonight after all")
                        actions.add {
                            bg.execute {
                                Dinners.cancelHosting(state)
                                prefs.saveGame(state)
                                runOnUiThread {
                                    toast("🍽 Called off. The table keeps " +
                                        "its dignity; so do they.")
                                    refreshUi()
                                }
                            }
                        }
                    }
                    labels.add("🍽 It's handled — back to the kitchen")
                    actions.add { }
                    UiKit.showItemsDialog(this,
                        "🍽️ The Dining Table — ${hosting.family} tonight " +
                            "(${Dinners.windowLabel()}), $n of " +
                            "${Dinners.STEP_COUNT} beats in · pantry $pantry",
                        labels.toTypedArray()) { which -> actions[which]() }
                } else {
                    val options = Dinners.hostOptions(state)
                    val labels = mutableListOf<String>()
                    val actions = mutableListOf<() -> Unit>()
                    for (opt in options) {
                        if (opt.eligible) {
                            labels.add("🍽 Invite ${opt.def.family} " +
                                "(${opt.def.host} leads the party)")
                            val id = opt.def.id
                            val fam = opt.def.family
                            actions.add {
                                bg.execute {
                                    val ok = Dinners.scheduleHosting(state, id)
                                    prefs.saveGame(state)
                                    runOnUiThread {
                                        if (ok) toast("🍽 $fam are coming " +
                                            "tonight (${Dinners.windowLabel()}). " +
                                            "Set the table — and mind the pantry.")
                                        refreshUi()
                                    }
                                }
                            }
                        } else {
                            labels.add("🔒 ${opt.def.family} — not tonight")
                            val reason = opt.reason
                            val fam = opt.def.family
                            actions.add {
                                UiKit.showMessageDialog(this,
                                    "🍽 $fam",
                                    message = reason,
                                    positiveLabel = "OK", onPositive = { })
                            }
                        }
                    }
                    UiKit.showItemsDialog(this,
                        "🍽️ The Dining Table — host a family evening " +
                            "(decor $decor/${Dinners.HOST_MIN_DECOR} · " +
                            "pantry $pantry)",
                        labels.toTypedArray()) { which -> actions[which]() }
                }
            }
        }
    }

    private fun shelfDialog() {
        bg.execute {
            val slots = House.shelfSlots(state)
            val found = slots.count { it != null }
            val cups = state.photoFeats.size + state.gameFeats.size
            val sb = StringBuilder()
            sb.append("🏆 THE TROPHY SHELF\n\n")
            sb.append("Collectibles: $found of ${House.SHELF_SLOTS} shelf " +
                "slots filled")
            if (found < House.SHELF_SLOTS) {
                val missing = COLLECTIBLES.take(House.SHELF_SLOTS)
                    .filter { it.id !in state.collected }
                    .joinToString(", ") { it.name }
                sb.append(" — still out there: $missing")
            }
            sb.append(".\n\nFeat trophies: $cups " +
                "(${state.photoFeats.size} photo, ${state.gameFeats.size} game).")
            val locked = House.DECOR.filter { it.id !in House.decorFor(state) }
            if (locked.isNotEmpty()) {
                sb.append("\n\nBare wall, waiting:")
                for (d in locked) sb.append("\n· ${d.name} — ${d.hint}")
            } else {
                sb.append("\n\nEvery earned piece is up. The room is " +
                    "officially proud of you.")
            }
            val text = sb.toString()
            runOnUiThread { lookDialog(text) }
        }
    }

    private fun benchSit() {
        // v0.16.0a (item 17): sitting down is animated —
        // the eased sit_down transition, not a snap
        gameView.beginSit()
        bg.execute {
            actionTick()
            actionTick()
            val present = engine.presentWithPlayer(state).filter { !it.asleep }
            val joiner = present.firstOrNull()
            val relaxed = listOf(
                "Nice spot. You get it. This is the best seat in town and it's not close.",
                "Sometimes I just sit here and let South Park happen at me.",
                "No schemes for like, ten whole minutes. A new record for this town.",
                "You ever notice the mountains look painted on? …Forget I said that."
            )
            prefs.saveGame(state)
            runOnUiThread {
                if (joiner != null) {
                    lookDialog("🪑 You sit a spell. ${joiner.name} joins you on the bench.\n\n" +
                        "\"${relaxed[(joiner.name.hashCode() and 0x7fffffff) % relaxed.size]}\"")
                } else {
                    toast("🪑 You sit a spell. The town breathes around you.")
                }
                refreshUi()
            }
        }
    }

    /** #48 SLEEP TALKERS: nonsense from a dark window. */
    private fun dreamDialog(houseId: String) {
        val owner = KID_HOUSE.entries.firstOrNull { it.value == houseId }?.key ?: "Someone"
        val dreams = listOf(
            "…no, Mr. Hat, YOU wear the salad…",
            "…the underpants go on the OUTSIDE, professor…",
            "…five more minutes… the bus is a lie…",
            "…I told you, the gnome moves when nobody's looking…",
            "…cheesy poofs… falling… forever…"
        )
        val d = dreams[(owner.hashCode() + state.absDay) % dreams.size]
        lookDialog("💭 From the dark window, $owner mumbles in their sleep:\n\n\"$d\"")
        // #98: 3:33 AM — something small crosses the yard. Logged.
        if (state.timeMin in 200..225) {
            bg.execute {
                if (state.lifeText["sighting333"] != state.todayKey) {
                    state.lifeText["sighting333"] = state.todayKey
                    townLogLine(state, "??? — 3:33 AM: something small crossed a lit yard")
                    prefs.saveGame(state)
                }
            }
        }
    }

    /** #76 HARVEST SWAP: town junk for autumn produce, 1:1. */
    private fun swapDialog() {
        val junk = state.inventory.entries
            .filter { it.value > 0 }
            .mapNotNull { itemById(it.key) }
            .filter { it.tier == "junk" }
        if (junk.isEmpty()) {
            toast("🎃 The swap table wants town junk. Your pockets are tragically classy.")
            return
        }
        val labels = junk.map { "${it.icon} ${it.name}" }.toTypedArray()
        UiKit.showItemsDialog(this, "🎃 Swap one piece of junk for produce?",
            labels) { which ->
            bg.execute {
                val give = junk[which]
                state.inventory[give.id] = (state.inventory[give.id] ?: 1) - 1
                if ((state.inventory[give.id] ?: 0) <= 0) {
                    state.inventory.remove(give.id)
                }
                val produce = listOf("member_berry", "snack_cake", "flower", "hot_chocolate")
                val get = produce[(state.absDay + give.id.hashCode()) % produce.size]
                state.inventory[get] = (state.inventory[get] ?: 0) + 1
                prefs.saveGame(state)
                runOnUiThread {
                    toast("🎃 Swapped! One ${itemById(get)?.name ?: get} for the pile.")
                    refreshUi()
                }
            }
        }
    }

    /** #100 THE TIME CAPSULE at the park: bury now, dig up in a month. */
    private fun capsuleDialog() {
        val cap = state.lifeText["capsule"]
        if (cap != null) {
            val parts = cap.split("|")
            val itemId = parts.getOrNull(0) ?: ""
            val buriedDay = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val desc = parts.getOrNull(2) ?: ""
            if (state.absDay - buriedDay >= 30) {
                UiKit.showMessageDialog(this, "🕰️ The time capsule",
                    message = "It's been a month underground. Dig it up?",
                    positiveLabel = "Dig!",
                    onPositive = {
                        bg.execute {
                            state.inventory[itemId] = (state.inventory[itemId] ?: 0) + 1
                            state.lifeText.remove("capsule")
                            prefs.saveGame(state)
                            runOnUiThread {
                                lookDialog("🕰️ Inside: ${itemById(itemId)?.icon ?: ""} " +
                                    "${itemById(itemId)?.name ?: itemId}.\n\n" +
                                    "Past-you's note, in your own handwriting:\n\"$desc\"")
                                refreshUi()
                            }
                        }
                    },
                    negativeLabel = "Leave it")
            } else {
                toast("🕰️ Something of yours is buried here. It needs more time underground " +
                    "(${30 - (state.absDay - buriedDay)} days to go).")
            }
            return
        }
        val items = giftableItems()
        if (items.isEmpty()) {
            toast("🕰️ The park's soft dirt could hold a time capsule — if you had anything to bury.")
            return
        }
        val labels = items.map { "${it.icon} ${it.name}" }.toTypedArray()
        UiKit.showItemsDialog(this, "🕰️ Bury a time capsule?",
            labels) { which ->
            bg.execute {
                val item = items[which]
                state.inventory[item.id] = (state.inventory[item.id] ?: 1) - 1
                if ((state.inventory[item.id] ?: 0) <= 0) state.inventory.remove(item.id)
                state.lifeText["capsule"] =
                    "${item.id}|${state.absDay}|${item.desc}"
                prefs.saveGame(state)
                runOnUiThread {
                    toast("🕰️ Buried. Dig it up in a month — past-you left the description as a note.")
                    refreshUi()
                }
            }
        }
    }

    private fun onCollect(spawnId: String) {
        bg.execute {
            val (got, msg) = engine.collectSpawn(state, spawnId)
            if (got != null) {
                SoundEngine.found(state.collected.size >= COLLECTIBLES.size)
            }
            prefs.saveGame(state)
            runOnUiThread {
                toast(msg)
                refreshUi()
            }
        }
    }

    private fun openJournal() {
        bg.execute {
            prefs.saveGame(state)
            runOnUiThread { startActivity(Intent(this, JournalActivity::class.java)) }
        }
    }

    private fun openMenu() {
        val items = arrayOf("🎯 Quest Tracker", "🎒 Inventory", "✨ Collection", "📖 Lorebook",
            "⚙ Settings", "🌅 Reset Town (New Day)", "🎨 Remake Kid", "🗺 Go Somewhere…",
            "📱 Phone")
        // v0.1.9b: the menu is a paper plaque list now (title on a
        // red plaque like the games' menus), same destinations
        // (v0.5.0a appended the town-wide destination picker;
        // v0.5.0e appended the phone)
        UiKit.showItemsDialog(this, "☰ Menu", items) { which ->
            when (which) {
                0 -> startActivity(Intent(this, QuestActivity::class.java))
                1 -> openInventory()
                2 -> { prefs.saveGame(state); startActivity(Intent(this, CollectionActivity::class.java)) }
                3 -> startActivity(Intent(this, LoreActivity::class.java))
                4 -> startActivity(Intent(this, SettingsActivity::class.java))
                5 -> confirmReset()
                6 -> startActivity(Intent(this, CreatorActivity::class.java))
                7 -> openGoSomewhere()
                8 -> openPhone()
            }
        }
    }

    /** v0.5.0e: the phone — a hub over the town's surfaces
     *  plus Messages. Launched for a result: its 🗺 Map tile
     *  hands the route back here, where the picker lives. */
    private fun openPhone() {
        // v0.16.0a (item 16): the phone comes UP first —
        // the phone action animation plays in the scene
        gameView.playPlayerAction("phone")
        bg.execute {
            prefs.saveGame(state)
            runOnUiThread {
                startActivityForResult(
                    Intent(this, PhoneActivity::class.java), REQ_PHONE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PHONE && resultCode == RESULT_OK &&
            data?.getStringExtra("phone_route") == "map") {
            openGoSomewhere()
        }
    }

    private fun confirmReset() {
        UiKit.showMessageDialog(this, "Reset Town?",
            message = "Start a fresh day in South Park? Your kid, friendships and collection stay. " +
                "The day just starts over. No punishments — it's cozy like that.",
            positiveLabel = "Reset",
            onPositive = {
                bg.execute {
                    val me = engine.player(state)
                    val keepName = me.name
                    val keepApp = me.appearance
                    val keepFriend = state.friendship.toMap()
                    val keepCollected = state.collected.toSet()
                    val keepInventory = state.inventory.toMap()
                    state = engine.newState()
                    engine.createPlayer(state, keepName, keepApp)
                    state.friendship.putAll(keepFriend)
                    state.collected.addAll(keepCollected)
                    state.inventory.putAll(keepInventory)
                    if (prefs.isRealtimeSync()) engine.syncToDeviceTime(state, clock)
                    prefs.saveGame(state)
                    runOnUiThread {
                        toast("🌅 A new day in South Park.")
                        refreshUi()
                    }
                }
            },
            negativeLabel = "Nah")
    }
}

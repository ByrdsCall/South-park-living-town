package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery2 — the second case (v0.3.0f, Queue #3 #6):
// "The Locked Booth".
//
// Where the pond case (Mystery.kt, v0.2.0f) sprawled across
// five weekly chapters, this one is a compressed whodunit in a
// single location. The Bijou hosts a one-night marathon: the
// kids' own home-movie — a school project — plus a donated
// prize reel. Between acts, the home-movie reel vanishes from
// the LOCKED projection booth. Eight named guests were there.
// One marathon night (an episode, on the same machinery as any
// event), three interview days, then a second screening where
// the town gathers and somebody may say a name.
//
// Structure:
//  - BEATS: two full ShenaniganDefs (the marathon night, the
//    accusation night). Like the pond chapters they live
//    OUTSIDE the random pool but resolve in SHENANIGAN_BY_ID.
//  - Gating: the case queues into the mystery slot a week
//    after the pond case retires. While the pond case is open
//    (or unplayed), this one waits — the two never overlap.
//  - Clues: 10, gathered the pond case's three ways — witness
//    interviews (Talk), physical evidence (Look), and showing
//    up (participation at the marathon). The spine of the set
//    is an alibi that will not sit still: it is re-told, a
//    little different, on each interview day, and an honest
//    man's timeline contradicts every version of it.
//  - The accusation: at the second screening's climax, a
//    player holding at least ACCUSE_MIN_CLUES of the case can
//    name one of four suspects. Naming right closes the case
//    warm. Naming wrong sticks socially — a docked bond, a
//    grudge the town's ambient talk keeps — and the case goes
//    cold anyway. Naming nobody lets it go cold clean. A week
//    after a cold close, the reel quietly turns up in the
//    Bijou lost-and-found; the town never learns, and the
//    board's final note lets a careful reader suspect forever.
//  - Retirement: every landing retires the arc permanently,
//    exactly like the pond case. The Journal's 🔍 MYSTERY
//    page gains this case as its own section below the pond's.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery2 {

    const val CASE_TITLE = "The Locked Booth"
    const val MARATHON_ID = "arc2_marathon"
    const val ACCUSATION_ID = "arc2_accusation"
    const val OPEN_DELAY_DAYS = 7      // after the pond case retires
    const val ACCUSATION_GAP_DAYS = 4  // marathon night -> second screening
    const val ACCUSE_MIN_CLUES = 6
    const val REEL_FOUND_DELAY_DAYS = 7
    const val WRONG_ACCUSE_SCAR = 15

    /** Who took the reel. Internal canon — the game resolves
     *  the case against this key and no other. */
    const val CULPRIT_KEY = "mackey"

    /** The eight named guests at the marathon (public premise —
     *  they're in the marathon def's own staging text). */
    val GUEST_LIST = listOf("Stan", "Kyle", "Cartman", "Kenny",
        "Butters", "Wendy", "Clyde", "Craig")

    /** The four names that can be said at the accusation. */
    val SUSPECT_KEYS = listOf("mackey", "clyde", "craig", "usher")

    fun suspectLabel(state: GameState, key: String): String = when (key) {
        "mackey" -> "Mr. Mackey"
        "clyde" -> "Clyde"
        "craig" -> "Craig"
        "usher" -> state.arc2Usher.ifEmpty { "the Bijou usher" }
        else -> key
    }

    /** The cast name a suspect key answers to in friendship /
     *  ambient systems (the usher answers to his own name). */
    fun suspectNpcName(state: GameState, key: String): String = when (key) {
        "mackey" -> "Mackey"
        "clyde" -> "Clyde"
        "craig" -> "Craig"
        "usher" -> state.arc2Usher
        else -> key
    }

    // ---------------- the two beats ----------------

    val BEATS: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = MARATHON_ID,
            title = "The Marathon Night",
            cause = "The Bijou is hosting a one-night marathon: the kids' own " +
                "home-movie — a school project, months in the making — screens last, " +
                "after a donated prize reel. Eight guests. One projection booth. " +
                "What could go wrong.",
            blurb = "One night only at the Bijou: a prize reel, then the premiere " +
                "of the home-movie the fourth grade has been shooting all semester.",
            theme = QuestTheme("🎞️", "#c86ae8", "MARATHON NIGHT"),
            tintHex = "#2c1e3a",
            stages = listOf(
                ShenaniganStage("Kickoff: Doors at the Bijou",
                    "🎞️ MARATHON NIGHT: The Bijou's doors are open. The prize reel " +
                        "screens first; the home-movie premieres LAST. That's the deal.",
                    "The guest list is small and fixed: Stan, Kyle, Cartman, Kenny, " +
                        "Butters, Wendy, Clyde, Craig — plus a chaperone, an usher, " +
                        "and a projectionist with a locked booth and strong opinions.",
                    "Take your seat at the Bijou.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "At the marathon. Program in hand. Seat kicked twice already.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 3,
                            note = "The Boys' Crew are filmmakers tonight. It has gone to their heads."))),
                ShenaniganStage("Escalation: Intermission",
                    "🎞️ INTERMISSION: The prize reel's done. Popcorn runs low. The booth " +
                        "stays locked between reels — booth rules, no exceptions, the " +
                        "projectionist is FIRM about it.",
                    "Twenty minutes of lobby. The usher sweeps, the chaperone supervises, " +
                        "eight kids argue about the prize reel's ending, which Clyde " +
                        "claims he could fix.",
                    "Stretch your legs in the Bijou lobby.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "In the lobby at intermission. Arguing about cinema.",
                    tone = "hype", litterBurst = 1),
                ShenaniganStage("Climax: The Empty Booth",
                    "🎞️ CLIMAX: The booth is unlocked for the premiere — and the " +
                        "home-movie's reel can is EMPTY. The booth was locked all night. " +
                        "Nothing is forced. Nothing is broken. The reel is just… gone.",
                    "A locked booth, an empty can, and a premiere with no movie. The " +
                        "manager is writing down who was here. Everybody stays calm, " +
                        "in the specific way that means nobody is calm.",
                    "Help them search the booth. Look at everything.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Searching the booth. Carefully. Twice.",
                    tone = "whisper", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = -2,
                            note = "A school project vanished on a school night. Questions will be asked."))),
                ShenaniganStage("Resolution: The Guest List",
                    "🎞️ The marathon ends without its movie. The manager's list of who " +
                        "was here tonight is short, and everybody on it knows everybody " +
                        "on it. Tomorrow, the town starts asking questions.",
                    "The premiere is postponed. The booth stays locked. In the morning " +
                        "there will be interviews — the town does those now; it has " +
                        "practice.",
                    "Head home. Tomorrow: questions.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Stan", "Kyle", "Cartman", "Kenny"),
            episodeTitles = listOf("One Night Only", "The Reel Is Gone"),
            coldOpen = listOf(
                Line("Stan", "Months of filming. MONTHS. Tonight the whole town sees it."),
                Line("Kyle", "The whole town is eight people and a chaperone, Stan."),
                Line("Stan", "A PREMIERE, Kyle. It's a premiere."))),
        ShenaniganDef(
            id = ACCUSATION_ID,
            title = "The Second Screening",
            cause = "Three days of interviews later, the Bijou opens its doors for " +
                "the screening that never happened. The reel is still missing. " +
                "Everybody who was there is coming back — and tonight, somebody " +
                "may say a name.",
            blurb = "The town regathers at the Bijou. The booth is still locked. " +
                "The case is still open. The floor is yours, if you've done the work.",
            theme = QuestTheme("🔍", "#e86a5a", "THE ACCUSATION"),
            tintHex = "#33222a",
            stages = listOf(
                ShenaniganStage("Kickoff: Back to the Bijou",
                    "🔍 THE SECOND SCREENING: The town files back into the Bijou. Same " +
                        "seats. Same eight guests. One empty reel can, out front, " +
                        "like an exhibit.",
                    "Nobody's here for a movie tonight. They're here for the part " +
                        "where somebody explains the locked booth.",
                    "Take your seat at the Bijou. Again.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Back at the Bijou. Watching the room, not the screen.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew wants their premiere back, whatever it takes."))),
                ShenaniganStage("Escalation: Theories in the Lobby",
                    "🔍 The lobby is all theories. The usher did it, says the lobby, " +
                        "mostly because ushers are who lobbies blame. The usher is " +
                        "sweeping. The usher is always sweeping.",
                    "Every guest has a version. Every version has a hole in it exactly " +
                        "the shape of its teller.",
                    "Work the lobby at the Bijou.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Listening to theories in the Bijou lobby.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: Say a Name",
                    "🔍 CLIMAX: The manager dims the lights and asks the question " +
                        "plainly: does anybody here actually KNOW something? The room " +
                        "turns. Toward whoever's done the work.",
                    "This is the moment the interviews were for. If the case you've " +
                        "built is strong enough, you can stand up and say a name — and " +
                        "names, once said in this town, stick.",
                    "Decide what you know. Decide if you'll say it.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Holding the room's attention. The floor is open.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: After the Screening",
                    "🔍 Whatever tonight decided, the Bijou turns its lights back up " +
                        "and the town walks home under them. Cases end. Rooms remember.",
                    "The second screening is over. What the town does with tonight " +
                        "is the town's business now.",
                    "Walk home under the marquee lights.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Wendy", "Stan", "Kyle"),
            episodeTitles = listOf("The Town Says a Name", "Locked Booth, Open Mic"),
            coldOpen = listOf(
                Line("Wendy", "Three days of interviews. I have notes. I have a CHART."),
                Line("Cartman", "I just want it on record that EYE was framed preemptively."),
                Line("Wendy", "Nobody's framed anyone yet, Cartman."),
                Line("Cartman", "YET.")))
    )

    val BEAT_BY_ID: Map<String, ShenaniganDef> = BEATS.associateBy { it.id }

    fun isBeat(defId: String?): Boolean = defId != null && defId in BEAT_BY_ID

    // ---------------- the clues ----------------

    data class Clue2(
        val id: String,
        val kind: Mystery.ClueKind,
        val title: String,
        val detail: String,
        /** WITNESS: the cast member who tells you. */
        val npc: String? = null,
        /** WITNESS (the usher only): told by whoever this save's
         *  usher turns out to be. */
        val byUsher: Boolean = false,
        /** WITNESS: the line they say (the telling IS the discovery). */
        val spoken: String? = null,
        /** LOOK: the location to Look at. */
        val locId: String? = null,
        /** LOOK: the sentence the Look reports. */
        val foundText: String? = null,
        /** PARTICIPATION: the beat def whose turf grants it. */
        val beatDefId: String? = null,
        /** PARTICIPATION: first stage index at which it can turn up. */
        val minStage: Int = 0
    )

    /** The alibi's three tellings, in the order they are told. */
    val ALIBI_IDS = listOf("alibi_lobby", "alibi_near", "alibi_adjacent")

    val CLUES2: List<Clue2> = listOf(
        Clue2("booth_program", Mystery.ClueKind.PARTICIPATION,
            "The marathon program",
            "The program lists every scene of the home-movie. Scene 9 is circled " +
                "in somebody's copy: 'THE COUNSELOR'S BIG NUMBER (unrehearsed!!)'. " +
                "Somebody in this town would rather that scene never screened.",
            beatDefId = MARATHON_ID, minStage = 0),
        Clue2("booth_search", Mystery.ClueKind.PARTICIPATION,
            "The booth, searched",
            "You helped search the booth after the reel vanished: door locked, " +
                "lock unmarked, everything inside tidy and squared away. Whoever " +
                "opened it used a key — and took the time to leave it neat.",
            beatDefId = MARATHON_ID, minStage = 2),
        Clue2("key_log", Mystery.ClueKind.LOOK,
            "The booth key log",
            "Two booth keys exist. The projectionist's was signed back IN at " +
                "intermission. The school's copy was signed OUT that morning — " +
                "'for the marathon' — and its IN column is still blank.",
            locId = "Bijou",
            foundText = "🔍 The key log hangs by the booth door, in pencil: two keys, " +
                "that's all there are. One came back at intermission. One — the " +
                "school's — went out the morning of the marathon and never signed " +
                "back in."),
        Clue2("thumbprint", Mystery.ClueKind.LOOK,
            "A thumbprint in butter",
            "On the booth door's inside handle: one clear thumbprint, in the good " +
                "butter — the kind from the STAFF tub behind the counter. Guests " +
                "never touch that tub. Staff and chaperones do.",
            locId = "Bijou",
            foundText = "🔍 Inside the booth, the door handle shines: one perfect " +
                "thumbprint in popcorn butter — the good butter, from the staff " +
                "tub behind the counter. Somebody let themselves out, snacking."),
        Clue2("confiscation_form", Mystery.ClueKind.LOOK,
            "A confiscation slip, half-filled",
            "In the school office tray: a pink CONFISCATED ITEMS slip. The ITEM " +
                "line is blank. The REASON line is already written, in round " +
                "careful handwriting: 'for your own good.' It was filled in before " +
                "whatever it was for.",
            locId = "Elementary",
            foundText = "🔍 In the school office's tray sits a pink confiscation " +
                "slip, started and abandoned: item — blank; reason — 'for your " +
                "own good.' Somebody at this school confiscates on instinct."),
        Clue2("tall_shadow", Mystery.ClueKind.WITNESS,
            "A tall shadow, wearing a tie",
            "Butters, refilling his soda at intermission, saw a shadow cross the " +
                "booth hall: grown-up tall, in a tie. Not a kid. Not the usher — " +
                "the usher was in the lobby, sweeping, being seen.",
            npc = "Butters",
            spoken = "I wasn't snoopin', I was gettin' a refill! And I seen a shadow " +
                "go past the booth door, fellas — a TALL one. Grown-up tall, with " +
                "a tie on. Kids don't wear ties to the movies. Well — Wendy does " +
                "sometimes. It weren't Wendy."),
        Clue2("usher_timeline", Mystery.ClueKind.WITNESS,
            "The usher's timeline",
            "The usher writes everything down. Intermission, by his notebook: " +
                "booth locked, keys on his belt, twenty minutes sweeping the " +
                "lobby — him, two moms, one spilled soda. Nobody from the school " +
                "came through that lobby. At all.",
            byUsher = true,
            spoken = "Timeline? Sure — I write it all down, it's the job. " +
                "Intermission: booth locked, keys on my belt, and me sweeping " +
                "the lobby for twenty solid minutes. Me, two moms, a spilled " +
                "soda. If anybody tells you they stood in that lobby the whole " +
                "intermission, friend — they're thinking of a different lobby."),
        Clue2("alibi_lobby", Mystery.ClueKind.WITNESS,
            "The alibi, first telling",
            "Asked where he was at intermission, the chaperone is certain: in " +
                "the lobby. The whole intermission. His words, all of them: " +
                "'the whole time.'",
            npc = "Mackey",
            spoken = "Intermission? I was in the lobby the whole time, m'kay. " +
                "Supervising. From the lobby. The whole time. Next question."),
        Clue2("alibi_near", Mystery.ClueKind.WITNESS,
            "The alibi, second telling",
            "A day later the lobby has become 'near the lobby.' Chaperones " +
                "circulate, apparently. The story moved. Stories aren't supposed " +
                "to move.",
            npc = "Mackey",
            spoken = "Well — when I said the lobby, m'kay, I meant NEAR the " +
                "lobby. The lobby area. A chaperone circulates, it's part of " +
                "the job. I circulated. Near the lobby."),
        Clue2("alibi_adjacent", Mystery.ClueKind.WITNESS,
            "The alibi, third telling",
            "Day three: 'lobby-adjacent.' He stepped toward the booth hall " +
                "'to check on things.' The lobby has officially left the " +
                "building.",
            npc = "Mackey",
            spoken = "Okay. Okay, look — 'lobby-adjacent,' m'kay? I may have " +
                "stepped toward the booth hall for one minute, to check on " +
                "things, the way a chaperone does. I was BASICALLY in the " +
                "lobby. This is a lot of questions about a lobby.")
    )

    val CLUE2_BY_ID: Map<String, Clue2> = CLUES2.associateBy { it.id }
    val CLUE2_IDS: Set<String> = CLUES2.map { it.id }.toSet()

    fun clueCount2(state: GameState): Int = state.mystery2Clues.size

    /** The interview phase: the marathon is behind us, the case
     *  is open, and the town is talking. */
    fun interviewsOpen(state: GameState): Boolean =
        state.arc2Started && !state.arc2Retired && state.absDay > state.arc2MarathonDay

    /** A clue can be found while the case is open — the
     *  interview evidence once the interviews begin, the
     *  marathon's own discoveries only on the night itself. */
    fun clueAvailable2(state: GameState, clue: Clue2): Boolean {
        if (state.arc2Retired) return false
        return when (clue.kind) {
            Mystery.ClueKind.PARTICIPATION ->
                state.shenaniganId == MARATHON_ID && state.arc2Started
            else -> interviewsOpen(state)
        }
    }

    /** Grant a clue exactly once. The ticker + the town log
     *  hear about it; the Journal board keeps it forever. */
    fun grantClue2(state: GameState, clueId: String): Boolean {
        val clue = CLUE2_BY_ID[clueId] ?: return false
        if (clueId in state.mystery2Clues) return false
        state.mystery2Clues.add(clueId)
        state.events.add("🔍 CLUE ${state.mystery2Clues.size}/${CLUES2.size}: ${clue.title}. " +
            "(Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🔍 Clue found: ${clue.title}")
        return true
    }

    /** Talk-to discovery: a witness's line — and the clue it
     *  grants. The alibi is special: it is told once per
     *  interview day, in order, each telling a little
     *  different from the last. */
    fun witnessLine2(state: GameState, npcName: String): Line? {
        if (state.arc2Retired || !interviewsOpen(state)) return null
        if (npcName == "Mackey") {
            if (state.arc2AlibiDay == state.absDay) return null
            val next = ALIBI_IDS.firstOrNull { it !in state.mystery2Clues }
                ?: return null
            val clue = CLUE2_BY_ID[next] ?: return null
            if (!grantClue2(state, next)) return null
            state.arc2AlibiDay = state.absDay
            return Line("Mackey", clue.spoken ?: "")
        }
        if (state.arc2Usher.isNotEmpty() && npcName == state.arc2Usher) {
            val clue = CLUE2_BY_ID["usher_timeline"] ?: return null
            if (!clueAvailable2(state, clue)) return null
            if (!grantClue2(state, clue.id)) return null
            return Line(npcName, clue.spoken ?: "")
        }
        val clue = CLUES2.firstOrNull {
            it.kind == Mystery.ClueKind.WITNESS && it.npc == npcName
        } ?: return null
        if (!clueAvailable2(state, clue)) return null
        if (!grantClue2(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Flavor, not evidence: the other suspects talk during
     *  the interviews — once each, and it grants nothing but
     *  character. */
    fun suspectFlavorLine(state: GameState, npcName: String): Line? {
        if (!interviewsOpen(state)) return null
        if (npcName in state.arc2FlavorSeen) return null
        val line = when (npcName) {
            "Clyde" -> "Yeah, I was near the booth. I wanted to know how the " +
                "movie ENDS. I was IN the movie and I still don't know how it " +
                "ends. That's not a crime, that's a tragedy with popcorn."
            "Craig" -> "My alibi is I sat next to Tweek all night. Ask him. " +
                "He'll panic and confirm everything and nothing, in that order."
            else -> return null
        }
        state.arc2FlavorSeen.add(npcName)
        return Line(npcName, line)
    }

    /** Look discovery: the sentence a Look at [locId] reports
     *  the first time it finds each piece of evidence there —
     *  the Bijou holds two, so it gives them up one at a time. */
    fun lookClueText2(state: GameState, locId: String): String? {
        val clue = CLUES2.firstOrNull {
            it.kind == Mystery.ClueKind.LOOK && it.locId == locId &&
                it.id !in state.mystery2Clues
        } ?: return null
        if (!clueAvailable2(state, clue)) return null
        if (!grantClue2(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during the marathon —
     *  the program comes early, the booth search at the climax. */
    fun noteParticipation2(state: GameState, defId: String) {
        if (defId != MARATHON_ID) return
        val stage = Shenanigans.stageFor(state.timeMin)
        val clue = CLUES2.firstOrNull {
            it.kind == Mystery.ClueKind.PARTICIPATION &&
                it.beatDefId == defId && stage >= it.minStage &&
                it.id !in state.mystery2Clues
        } ?: return
        if (!clueAvailable2(state, clue)) return
        if (grantClue2(state, clue.id)) {
            state.events.add("🔍 Up close, something clicks: ${clue.detail}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- the usher ----------------

    /** Screen for usher candidates out of the generated pool:
     *  a plausible grown person off the street — multiword
     *  name, no celebrity / profession-of-note / schoolkid
     *  markers in their lore text. Pure: the suite feeds it
     *  synthetic pools, the game feeds it the real one. */
    private val USHER_SCREEN = listOf("actor", "actress", "singer", "rapper",
        "president", "celebrity", "famous", "football", "basketball",
        "wrestler", "comedian", "pope", "ghost", "deceased",
        "student at south park elementary", "fourth grader", "rock star",
        "movie star", "superhero", "billionaire", "author", "artist",
        "scientist", "politician", "senator", "governor")

    fun pickUsherName(pool: List<Pair<String, String>>, marathonAbsDay: Int): String? {
        val candidates = pool.filter { (name, text) ->
            val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            words.size >= 2 && name.all { it.isLetter() || it == ' ' } &&
                USHER_SCREEN.none { marker -> marker in text.lowercase() }
        }
        if (candidates.isEmpty()) return null
        val rnd = Random(marathonAbsDay.toLong() * 1_000_003L + 77L)
        return candidates[rnd.nextInt(candidates.size)].first
    }

    /** The usher is on shift at the Bijou while the case is
     *  open — the Activities render him there (in whichever of
     *  the Bijou's spaces the player is standing in, the way
     *  VisitorSystem.presentAt matches spaces) and he can be
     *  talked to like any generated soul. Null when this save
     *  has no usher (the pool came up empty) or the case is
     *  shut. */
    fun usherHere(state: GameState, inside: Boolean): ActiveNpc? {
        if (!state.arc2Started || state.arc2Retired) return null
        val name = state.arc2Usher
        if (name.isEmpty()) return null
        val persona = VisitorSystem.personaByName(name) ?: return null
        return ActiveNpc(persona, "Bijou", inside)
    }

    // ---------------- scheduling ----------------

    /** The beat that should take today's event slot, if any.
     *  Also SCHEDULES the case: the first ask after the pond
     *  case retires books the marathon a week out. Both beats
     *  defer, day by day, while a holiday episode is on —
     *  the same discipline as the pond chapters. While the
     *  pond case is open this never fires: the mystery slot
     *  has one case at a time. */
    fun dueBeat(state: GameState): ShenaniganDef? {
        if (!state.arcRetired || state.arc2Retired) return null
        if (!state.arc2Started) {
            if (state.arc2DueAbsDay < 0) {
                state.arc2DueAbsDay = state.absDay + OPEN_DELAY_DAYS
            }
            if (state.absDay < state.arc2DueAbsDay) return null
            if (state.holidayId != null) return null // defer — holidays keep their day
            return BEAT_BY_ID[MARATHON_ID]
        }
        if (!state.arc2AccusationAired) {
            if (state.absDay < state.arc2AccuseDueAbsDay) return null
            if (state.holidayId != null) return null
            return BEAT_BY_ID[ACCUSATION_ID]
        }
        return null
    }

    /** Engine hook: a beat just took the slot. Books the
     *  case's clocks — and, on the marathon, seats the usher:
     *  one generated soul, picked from the town's own pool by
     *  the marathon's date, on shift at the Bijou for the
     *  whole case. */
    fun noteBeatStarted(state: GameState, def: ShenaniganDef) {
        when (def.id) {
            MARATHON_ID -> {
                state.arc2Started = true
                state.arc2MarathonDay = state.absDay
                state.arc2AccuseDueAbsDay = state.absDay + ACCUSATION_GAP_DAYS
                if (state.arc2Usher.isEmpty()) {
                    val pool = VisitorSystem.eligible().map { it.name to it.text }
                    pickUsherName(pool, state.absDay)?.let { state.arc2Usher = it }
                }
            }
            ACCUSATION_ID -> state.arc2AccusationAired = true
        }
    }

    // ---------------- the accusation ----------------

    /** The moment is on the table: the second screening is
     *  airing, its climax has begun, no name is locked yet,
     *  and the player holds enough of the case to say one. */
    fun accusationDue(state: GameState): Boolean {
        if (state.arc2Retired || state.arc2Accused.isNotEmpty()) return false
        if (state.arc2Declined) return false
        if (state.shenaniganId != ACCUSATION_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return clueCount2(state) >= ACCUSE_MIN_CLUES
    }

    /** Keep your seat: the moment was on the table and the
     *  player let it pass. The case will close cold at the
     *  wrap, the clean way. */
    fun declineAccusation(state: GameState): Boolean {
        if (!accusationDue(state)) return false
        state.arc2Declined = true
        state.events.add("🔍 The room waits. You look at your notes, and you " +
            "keep your seat. Some things a town has to not know.")
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    /** Say the name. It sticks. */
    fun accuse(state: GameState, suspectKey: String): Boolean {
        if (suspectKey !in SUSPECT_KEYS) return false
        if (!accusationDue(state)) return false
        state.arc2Accused = suspectKey
        state.events.add("🔍 You stand up at the Bijou. You say the name: " +
            "${suspectLabel(state, suspectKey)}. The room goes very quiet.")
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class AccusationOutcome(
        val branch: String,          // "solved" | "wrong" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the case at the accusation wrap: pick the
     *  branch, stamp the arc RETIRED forever, and hand the
     *  wrap its words. An accusation that somehow stands
     *  without the clues behind it (the dialog never offers
     *  one) degrades to a cold case rather than a free solve
     *  — and NOTHING in the wrong branch's player-facing words
     *  admits the name was wrong; doubt is the player's to
     *  keep. Only the confession is unambiguous. */
    fun resolveAccusation(state: GameState): AccusationOutcome {
        val accused = state.arc2Accused
        val backed = clueCount2(state) >= ACCUSE_MIN_CLUES
        val branch = when {
            accused == CULPRIT_KEY && backed -> "solved"
            accused.isNotEmpty() && backed -> "wrong"
            else -> "cold"
        }
        state.arc2Branch = branch
        state.arc2Retired = true
        state.arc2RetiredDay = state.absDay
        return when (branch) {
            "solved" -> AccusationOutcome(branch,
                "You said the name, and the room went quiet — then the name stood " +
                    "up. The reel is BACK. Tonight the home-movie finally screens, " +
                    "all of it, and the town watches scene 9 together. Somehow, " +
                    "that's what heals it.",
                listOf(
                    "🔍 THE CONFESSION: It was the chaperone. The home-movie caught " +
                        "a moment he would have paid to un-film, so the reel was " +
                        "'confiscated' with the school's booth key — for everyone's " +
                        "own good, m'kay. He has since apologized to the " +
                        "filmmakers, the Bijou, and the concept of cinema itself.",
                    "🔍 Scene 9 screened to a full house. It got the biggest laugh " +
                        "of the night. Its star took a bow. Growth."))
            "wrong" -> {
                val label = suspectLabel(state, accused)
                AccusationOutcome(branch,
                    "You said the name — $label — and the town believed you; that's " +
                        "what towns do. There was no confession, just a quiet that " +
                        "has followed somebody since, and a case the town now calls " +
                        "closed. The reel is still missing.",
                    listOf(
                        "🔍 THE NAME THAT STUCK: You named $label at the Bijou, in " +
                            "front of everyone, and the town wrote it down in ink. " +
                            "Cases close. Names stay said.",
                        "🔍 The reel never turned up. Neither did any other answer."))
            }
            else -> AccusationOutcome(branch,
                "Nobody said a name. The screening played to a quiet room, the " +
                    "reel stayed missing, and the case went into the Bijou's " +
                    "drawer with the other unsolved things.",
                listOf(
                    "🔍 THE COLD CASE: No accusation was ever made. The town has " +
                        "theories the way the Bijou has seats — plenty, and mostly " +
                        "empty.",
                    "🔍 Somewhere out there is a film reel with a whole movie on " +
                        "it. It is not here."))
        }
    }

    /** The branch's ripples, marks and scars — called by the
     *  wrap BEFORE the episode's credits are built, so the
     *  recap's faction-shift diff includes them. */
    fun applyAccusationEffects(state: GameState, branch: String) {
        fun ripple(factionId: String, delta: Int, note: String) {
            val fs = state.factionStates[factionId] ?: return
            fs.influence = (fs.influence + delta).coerceIn(0, 100)
            if (delta != 0) {
                fs.moodScore = (fs.moodScore + if (delta > 0) 1 else -1).coerceIn(-2, 2)
            }
            if (note.isNotEmpty()) {
                fs.mood = note
                state.events.add("⚔️ FACTION SHIFT: $note")
                while (state.events.size > 40) state.events.removeAt(0)
            }
        }
        when (branch) {
            "solved" -> {
                ripple("school_staff", 3,
                    "The school's name is clear — the case was handled in-house.")
                ripple("boys_crew", 2,
                    "The filmmakers got their movie back, and their premiere.")
                for (kid in listOf("Stan", "Kyle", "Cartman", "Kenny")) {
                    state.friendship[kid] =
                        ((state.friendship[kid] ?: 0) + 2).coerceAtLeast(0)
                }
                state.lifeText["mark:Bijou"] =
                    "A small brass plaque by the booth door: 'THE MARATHON NIGHT — " +
                        "the reel came home. Scene 9 got its laugh.'"
                townLogLine(state, "🔍 SOLVED: the booth case is closed and the " +
                    "reel came home.")
            }
            "wrong" -> {
                val accused = state.arc2Accused
                val npc = suspectNpcName(state, accused)
                if (accused == "usher") {
                    NpcMemoryStore.get(npc)?.let { mem ->
                        mem.points = (mem.points - WRONG_ACCUSE_SCAR).coerceAtLeast(0)
                    }
                } else if (npc.isNotEmpty()) {
                    state.friendship[npc] =
                        ((state.friendship[npc] ?: 0) - WRONG_ACCUSE_SCAR)
                            .coerceAtLeast(0)
                }
                state.lifeText["mark:Bijou"] =
                    "The booth door has a new sign: 'KEYS SIGNED IN & OUT — NO " +
                        "EXCEPTIONS.' Nobody talks about the marathon night."
                townLogLine(state, "🔍 CASE CLOSED: a name was named at the Bijou. " +
                    "The town has moved on. The named, mostly, has not.")
            }
            else -> {
                state.lifeText["mark:Bijou"] =
                    "A 'REEL MISSING' card still sits in the booth window, fading " +
                        "a little more every month."
                townLogLine(state, "🔍 CASE COLD: the booth case was never solved.")
            }
        }
    }

    /** The long tail of a cold close: a week after the case
     *  shut without its reel, the reel quietly turns up in the
     *  Bijou lost-and-found. Called daily by the engine;
     *  fires exactly once, and only for a case that closed
     *  cold — a solved case already has its reel home. */
    fun noteReelFound(state: GameState): Boolean {
        if (!state.arc2Retired || state.arc2ReelFound) return false
        if (state.arc2Branch == "solved" || state.arc2Branch.isEmpty()) return false
        if (state.arc2RetiredDay < 0) return false
        if (state.absDay < state.arc2RetiredDay + REEL_FOUND_DELAY_DAYS) return false
        state.arc2ReelFound = true
        state.events.add("🎞️ Bijou lost-and-found, cleaned out today: at the bottom, " +
            "one film reel. Unlabeled. Slightly dusty. The clerk logged it, shelved " +
            "it, and thought no more about it.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🎞️ A reel turned up in the Bijou lost-and-found. " +
            "Unlabeled. Unclaimed.")
        state.lifeText["mark:Bijou"] =
            "The Bijou lost-and-found shelf holds one unlabeled reel now. " +
                "It will hold it forever."
        return true
    }

    // ---------------- the Journal board ----------------

    /** The second case shows once it exists: the marathon has
     *  aired, a clue is in hand, or the case is closed. */
    fun boardVisible2(state: GameState): Boolean =
        state.arc2Started || state.mystery2Clues.isNotEmpty() || state.arc2Retired

    fun statusStamp2(state: GameState): String = when {
        state.arc2Retired && state.arc2Branch == "solved" ->
            "✅ SOLVED — the reel came home"
        state.arc2Retired && state.arc2Branch == "wrong" ->
            "🧊 CLOSED — a name was named"
        state.arc2Retired -> "🧊 COLD — unsolved"
        state.shenaniganId == ACCUSATION_ID -> "🔍 TONIGHT: THE ACCUSATION"
        state.arc2Started && state.absDay == state.arc2MarathonDay ->
            "🎞️ THE MARATHON IS TONIGHT"
        state.arc2Started -> {
            val day = state.absDay - state.arc2MarathonDay
            "🔍 INTERVIEWING — day $day of the interviews"
        }
        else -> "🔍 A CASE IS COMING"
    }

    fun currentTheory2(state: GameState): String = when {
        state.arc2Retired && state.arc2Branch == "solved" ->
            "CLOSED. The reel came home, scene 9 got its laugh, and the Bijou " +
                "has a plaque it polishes too often."
        state.arc2Retired ->
            "CLOSED, officially. The drawer at the Bijou stays shut and the " +
                "town has new things to be wrong about."
        state.shenaniganId == ACCUSATION_ID ->
            "Tonight somebody says a name, or nobody ever does."
        state.arc2Started ->
            "The smart money is on the usher. The smart money has never once " +
                "been right in this town."
        else -> "Nothing yet. The Bijou is just a theater. Probably."
    }

    /** The second case's board as plain lines (the Journal
     *  renders them below the pond case's; the suite reads
     *  them). */
    fun boardLines2(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp2(state))
        out.add("📖 THE NIGHT & THE DAYS")
        val marathonMark = when {
            state.shenaniganId == MARATHON_ID -> "▶"
            state.arc2Started -> "✓"
            else -> "·"
        }
        out.add("$marathonMark The Marathon Night — a reel vanishes from a locked booth")
        val interviewMark = when {
            state.arc2Retired -> "✓"
            interviewsOpen(state) -> "▶"
            else -> "·"
        }
        out.add("$interviewMark The Interviews — the town talks, one day at a time")
        val accusationMark = when {
            state.shenaniganId == ACCUSATION_ID -> "▶"
            state.arc2Retired -> "✓"
            else -> "·"
        }
        out.add("$accusationMark The Second Screening — a name may be said")
        out.add("🧩 CLUES — ${clueCount2(state)}/${CLUES2.size}" +
            if (!state.arc2Retired && clueCount2(state) < ACCUSE_MIN_CLUES)
                " (saying a name takes $ACCUSE_MIN_CLUES)" else "")
        for (clue in CLUES2) {
            if (clue.id in state.mystery2Clues) {
                out.add("• ${clue.title} — ${clue.detail}")
            }
        }
        val missing = CLUES2.size - clueCount2(state)
        if (missing > 0 && !state.arc2Retired) {
            out.add("• ??? — $missing still out there. Interview people. Look at " +
                "the booth, the school. Show up.")
        }
        out.add("🎭 THE SUSPECTS")
        out.add("• Mr. Mackey — the chaperone. His story keeps moving.")
        out.add("• Clyde — was near the booth. Wanted to skip to the ending.")
        out.add("• Craig — deadpan. His alibi is a shrug and Tweek.")
        out.add("• ${suspectLabel(state, "usher")} — the usher. Works the booth. " +
            "The obvious suspect.")
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory2(state)}")
        if (state.arc2Retired) {
            out.add(when (state.arc2Branch) {
                "solved" -> "🏁 The reel came home and the movie finally screened — " +
                    "all of it. The booth door has a plaque now."
                "wrong" -> "🏁 A name was named, and it stuck the way names do. The " +
                    "case is closed. The reel is still out there."
                else -> "🏁 Nobody was ever accused. The case went into the drawer, " +
                    "and the drawer stayed shut."
            })
            if (state.arc2ReelFound) {
                out.add("🎞️ Final note: a reel turned up in the Bijou lost-and-found " +
                    "a week after the case closed. Unlabeled. Unclaimed. The board " +
                    "offers no comment.")
            }
        }
        return out
    }

    // ---------------- the long tail (grudge callbacks) ----------------

    private val GRUDGES = mapOf(
        "clyde" to "Oh. Hey. It's the person who told the whole town I steal " +
            "movies. Cool. Love that for me.",
        "craig" to "…You know I didn't do it, right. Whatever. Forget it. I have.",
        "usher" to "I still usher. People tip me less now. Can't imagine why. " +
            "Oh wait — yes I can."
    )

    /** A wrongly-named suspect keeps the naming — ambient pairs
     *  that include them get their side of it, forever. Null
     *  unless the case closed on a wrong name and [p1] or [p2]
     *  is the one who was named. */
    fun grudgeCallback(state: GameState, p1: String, p2: String): Line? {
        if (!state.arc2Retired || state.arc2Branch != "wrong") return null
        val accused = suspectNpcName(state, state.arc2Accused)
        if (accused.isEmpty()) return null
        val line = GRUDGES[state.arc2Accused] ?: return null
        return if (p1 == accused || p2 == accused) Line(accused, line) else null
    }
}

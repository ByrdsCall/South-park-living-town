package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// THE SCHOOL PLAY (v0.10.0c) — once a school year, South Park
// Elementary mounts a production, and the whole town's data
// decides how it goes. The play is the town's own tradition:
//
//   "THE MOUNTAIN THAT WOULDN'T"
//   The valley votes to move the mountain six feet to the
//   left for the new road. The mountain, consulted through
//   its translator (a kid, with a shovel), declines. A goat
//   settles the matter. Written for this game; no other
//   stage's material appears here, by the standing rule.
//
// The model, in order:
//   - ONE production per school year, on the yearbook's
//     June–May rhythm: the school year named for its ending
//     spring (Yearbook.schoolYearOf) mounts its play in the
//     FALL of the prior calendar year. A fixed calendar:
//     the poster goes up on the first school day on/after
//     Oct 19; auditions two school days later; four
//     rehearsal afternoons; opening night the first Friday
//     after the last rehearsal. Pre-0.10.0c saves join at
//     the next production whose opening hasn't passed — a
//     save living through a fall this build didn't know
//     about gets no missed-play record, by the brief.
//   - DIRECTOR: Principal Victoria (authored suitability:
//     she already presides over the bee and every assembly;
//     Garrison keeps the lessons). She casts the show.
//   - CASTING is deterministic from authored suitability:
//     each role wants a mix of NERVE (an authored table,
//     Play.STAGE_NERVE — the Rival pool's eight keep their
//     Rival nerve exactly, pinned), VOICE (the bee's line-
//     handling skill, School.skillOf) and CHEMISTRY with
//     the director (Photos.chemistryBetween). Greedy by
//     role prestige; seeded hash tiebreaks. The player
//     auditions INTO the same pool with a delivery profile
//     (bold/true/deadpan) — the role they get is judged by
//     the same math, and a smaller role (ensemble,
//     understudy) is a real outcome, not a fail state.
//   - REHEARSALS are agenda claims (school afternoons,
//     15:00–17:00, right after class lets out — the claim
//     can never fight a lesson). NPC attendance is seeded
//     per member per session; the player's own attendance
//     is played and tracked, and shared sessions sprinkle
//     +1 friendship (once per session, the School sprinkle
//     discipline) and grow the cast's pair bonds a little
//     (+1 per pair per production if they shared 2+
//     sessions, settled at the wrap) — so rehearsing
//     together literally raises the cast's chemistry for
//     opening night, computed, not scripted.
//   - OPENING NIGHT is a set piece in the established
//     discipline: safe-moment gated from the chain's
//     bottom, judged LIVE from real state (cast chemistry,
//     rehearsal coverage, the crowd actually gathered,
//     and the player's ONE played beat — a line, a cue, a
//     save). It defers to case climaxes, season finales
//     and holiday beats (School.quietReason) and slips up
//     to two evenings; past that the show goes on without
//     a set piece and the record says so, kindly. Three
//     honest outcomes — TRIUMPH / SOLID / BEAUTIFUL
//     DISASTER — all of them good nights.
//   - THE RECORD: opening night mints ONE real photo via
//     the photo system's own group composition (the posed
//     finale at the school — no pixel costumes; the
//     caption and the Journal carry the production), filed
//     by Photos.applyPhoto like any photo, so the darkroom
//     can print the playbill. The morning after, a named
//     kid critic (the yearbook judges' rotation,
//     Yearbook.judgeFor) reviews it in fact-slot lines, in
//     the town log and on the Journal's 🎭 SCHOOL PLAY
//     page; past productions reduce to history lines.
//   - STATE: everything rides life/lifeText (the School
//     pattern) under "play…" keys — zero new save keys,
//     so pre-0.10.0c saves load untouched and simply join
//     the next production year.
// ============================================================

/** The production's pure calendar: a function of the
 *  Gregorian calendar alone (the FairCalendar pattern), so
 *  the renderer, the phone, the Journal and the suite all
 *  compute the same show. */
data class PlaySchedule(
    val year: Int,            // school year (ending spring)
    val announceDay: Int,     // absDay the poster goes up
    val auditionDay: Int,     // absDay of auditions
    val rehearsalDays: List<Int>, // absDays, REHEARSALS long
    val openingDay: Int       // absDay of opening night
) {
    val castDay: Int get() = auditionDay + 1
}

object PlayCalendar {

    /** [schoolYear]'s production: the poster goes up on
     *  the first school day on/after Oct 19 of the fall
     *  (calendar year = schoolYear - 1); auditions two
     *  school days after; rehearsals the next four school
     *  days; opening night the first Friday strictly after
     *  the last rehearsal. */
    fun scheduleFor(schoolYear: Int): PlaySchedule {
        val oct19 = ordinalOfKey("%04d1019".format(schoolYear - 1))
        var ord = oct19
        while (true) {
            val dow = FairCalendar.dowOfOrdinal(ord)
            if (dow in 1..5) break
            ord++
        }
        val announce = FairCalendar.absDayOfOrdinal(ord)
        val audition = School.nextSchoolDay(
            School.nextSchoolDay(announce))
        val rehearsals = mutableListOf<Int>()
        var d = audition
        repeat(Play.REHEARSALS) {
            d = School.nextSchoolDay(d)
            rehearsals += d
        }
        var opening = rehearsals.last() + 1
        while (Jobs.weekdayOf(opening) != 5) opening++
        return PlaySchedule(schoolYear, announce, audition,
            rehearsals, opening)
    }
}

/** One role in the production. [wNerve]/[wVoice]/[wChem]
 *  are the authored casting wants (-10..10; a negative
 *  voice want means FEW lines is the qualification — the
 *  Mountain speaks three times, the Goat never). */
data class PlayRole(
    val id: String, val title: String, val blurb: String,
    val wNerve: Int, val wVoice: Int, val wChem: Int,
    val comic: Boolean,
    val lines: List<String>
)

object Play {

    // ---------------- the dials (all suite-pinned) --------

    const val SCHOOL = "Elementary"
    const val TITLE = "The Mountain That Wouldn't"
    const val DIRECTOR = "Victoria"
    const val TOPIC = "the play"

    /** Rehearsals and auditions: school afternoons, right
     *  after class lets out (School.CLASS_TO is 15:00, so
     *  the claim can never collide with a lesson). */
    const val REH_FROM = 15 * 60
    const val REH_TO = 17 * 60

    /** Opening night: the house opens at 17:30; the set
     *  piece can start until 21:00. */
    const val FINAL_FROM = 17 * 60 + 30
    const val FINAL_TO = 21 * 60
    const val HOUSE_TO = 21 * 60 + 30

    /** Sessions in the rehearsal arc. */
    const val REHEARSALS = 4

    /** Named roles in the show. */
    const val ROLE_COUNT = 7

    /** Ensemble seats beyond the named roles. */
    const val ENSEMBLE_SIZE = 4

    /** Evenings opening night may slip when a bigger town
     *  moment owns the date. */
    const val GRACE_DAYS = 2

    /** The seeded share of rehearsals an NPC cast member
     *  makes, in percent — high, like real kids who
     *  auditioned on purpose, but never a guarantee. */
    const val NPC_ATTEND_PCT = 82

    /** Friendship sprinkled to castmates sharing a
     *  rehearsal with you: +1, once per session, the
     *  School discipline (LESSON_PAY's size). */
    const val REHEARSAL_PAY = 1

    // ---------------- the play ----------------

    const val PREMISE = "The valley votes to move the " +
        "mountain six feet to the left for the new road. " +
        "The mountain, consulted through its translator " +
        "(a kid, with a shovel), declines. A goat settles " +
        "the matter."

    /** Roles in CASTING order (prestige first — the
     *  Mountain is cast before the Goat because the whole
     *  show stands on who can stand still). */
    val ROLES: List<PlayRole> = listOf(
        PlayRole("mountain", "THE MOUNTAIN",
            "The lead. Three lines, all enormous. Played " +
                "very, very still.",
            wNerve = 8, wVoice = -3, wChem = 3, comic = false,
            lines = listOf(
                "I was here first. The paperwork agrees. " +
                    "The paperwork is also a mountain.",
                "Move me six feet. Go ahead. I will " +
                    "bring snacks for the attempt.",
                "I am not in the way. I am the way the " +
                    "way is.")),
        PlayRole("mayor", "THE MAYOR OF THE VALLEY",
            "Fast talk, big scissors, total confidence. " +
                "The road bond was their idea.",
            wNerve = 6, wVoice = 9, wChem = 3, comic = true,
            lines = listOf(
                "The road bond passed! Everything in the " +
                    "valley moves six feet left, starting " +
                    "with the mountain!",
                "I have a ribbon, I have scissors, and I " +
                    "have never once been wrong in public.",
                "Democracy has spoken. The mountain is " +
                    "just being a mountain about it.")),
        PlayRole("shovel", "THE KID WITH THE SHOVEL",
            "The heart of the show: the mountain's " +
                "translator, and the only one who listens.",
            wNerve = 5, wVoice = 8, wChem = 9, comic = false,
            lines = listOf(
                "The mountain says no. Politely. " +
                    "Mountains are polite.",
                "I dug the first hole for the road. The " +
                    "mountain filled it back in. At night. " +
                    "Respectfully.",
                "If the mountain moves, I move. I am the " +
                    "mountain's shovel now.")),
        PlayRole("weather", "THE WEATHER",
            "The narrator. Speaks only in forecasts, all " +
                "of them accurate.",
            wNerve = 4, wVoice = 10, wChem = 2, comic = false,
            lines = listOf(
                "Forecast: one mountain, unmoving, with a " +
                    "ninety percent chance of staying.",
                "A front of adults is moving in from the " +
                    "town hall. The mountain remains a " +
                    "mountain.",
                "Tonight: clear skies over a valley that " +
                    "has learned something. Probably.")),
        PlayRole("road", "THE NEW ROAD",
            "The case for progress. Six feet of destiny, " +
                "and it will NOT be curvy.",
            wNerve = 9, wVoice = 6, wChem = 2, comic = true,
            lines = listOf(
                "I am six feet of destiny and I will NOT " +
                    "be curvy.",
                "Every great road has a mountain it went " +
                    "around. Around is surrender with " +
                    "better PR.",
                "Straight lines! Progress! Slightly to " +
                    "the left!")),
        PlayRole("plow", "THE SNOWPLOW DRIVER",
            "Thirty winters around that mountain. The " +
                "show's steady hands.",
            wNerve = 7, wVoice = 5, wChem = 6, comic = false,
            lines = listOf(
                "I have plowed around that mountain for " +
                    "thirty winters. I can plow around " +
                    "it forever. That's the job.",
                "The mountain doesn't move for snow. Snow " +
                    "is just the mountain, visiting.",
                "Hand me the shovel, kid. Some things you " +
                    "dig WITH, not through.")),
        PlayRole("goat", "THE GOAT",
            "No lines. Pure presence. Settles the matter.",
            wNerve = 10, wVoice = -6, wChem = 4, comic = true,
            lines = listOf(
                "(The goat enters. The goat regards the " +
                    "mountain. The goat sits down beside " +
                    "the mountain.)",
                "(The goat eats the road bond. The vote " +
                    "is void. The goat is democracy now.)",
                "(Curtain. The goat remains.)"))
    )

    fun roleOf(id: String): PlayRole? =
        ROLES.firstOrNull { it.id == id }

    // ---------------- authored nerve ----------------
    // The casting table: one authored nerve number per kid
    // (1..9), in the Rival pool's own scale. The eight
    // rival-pool kids keep their Rival.nerve EXACTLY (the
    // suite pins it), so the town's nerve data is one
    // table wearing two hats, never two tables arguing.

    val STAGE_NERVE: Map<String, Int> = mapOf(
        "Cartman" to 7, "Kyle" to 8, "Wendy" to 9,
        "Craig" to 6, "Clyde" to 5, "Bebe" to 6,
        "Tweek" to 4, "Nathan" to 8,
        "Stan" to 6, "Kenny" to 7, "Butters" to 3,
        "Jimmy" to 5, "Timmy" to 6, "Tolkien" to 6,
        "Heidi" to 5, "Nichole" to 6, "Red" to 5,
        "Kevin" to 4, "Francis" to 3, "DogPoo" to 2,
        "Karen" to 4, "Scott" to 5, "Firkle" to 6,
        "Annie" to 6, "Bradley" to 4, "Ike" to 8,
        "Kevin McCormick" to 7, "Shelley" to 8,
        "Sophie" to 5, "Michael" to 7, "Pete" to 6,
        "Henrietta" to 7,
        // v0.11.0d wave 6: the table covers the grown kid cast
        // (the pin demands exactly CAST_KIDS). The newcomers
        // hold modest nerve — Pip's chin and Jenny's minutes
        // are brave in their way, but the pinned 2027 leads
        // (Ike 8, Wendy 9, Nathan 8, Shelley 8) keep their
        // casting — and the pool's ninth keeps his Rival nerve
        // exactly (Terrance Mephesto, RivalDef nerve 7)
        "Pip" to 5, "Dougie" to 4, "Jason" to 5,
        "Terrance Mephesto" to 7, "Jenny" to 5, "Flora" to 3,
        "Filmore" to 4, "Quaid" to 4, "Mimsy" to 4, "Tricia" to 5)

    fun stageNerve(name: String): Int =
        STAGE_NERVE[name] ?: 5

    // ---------------- the record ----------------

    /** A production's whole state. [track] is the player's:
     *  "" (never engaged) | "aud" (audience by choice) |
     *  "cast" | "ens" | "under" | "crew". [detail] is the
     *  role id (cast), the duty (crew) or "" . [night] is
     *  "" | "played" | "offstage". Everything serializes
     *  into one lifeText record (School's history format). */
    data class PlayRecord(
        val year: Int,
        val cast: Map<String, String>,   // roleId -> kid
        val ensemble: List<String>,
        val track: String = "",
        val detail: String = "",
        val delivery: String = "",
        val attMask: Int = 0,            // player's sessions
        val night: String = "",
        val verdict: String = "",         // triumph|solid|disaster
        val posterId: String = "",
        val critic: String = "",
        val review: String = ""
    ) {
        fun memberKids(): List<String> =
            cast.values.toList() + ensemble
        fun playerRole(): PlayRole? =
            if (track == "cast") roleOf(detail) else null
    }

    private fun recKey(year: Int) = "playRec:$year"

    fun record(state: GameState, year: Int): PlayRecord? {
        val raw = state.lifeText[recKey(year)]
            ?: return null
        return parseRecord(year, raw)
    }

    private fun parseRecord(year: Int, raw: String): PlayRecord? {
        val f = raw.split("|")
        if (f.size < 11 || f[0] != "1") return null
        val cast = LinkedHashMap<String, String>()
        if (f[1].isNotEmpty()) for (pair in f[1].split(",")) {
            val kv = pair.split(":")
            if (kv.size == 2) cast[kv[0]] = kv[1]
        }
        return PlayRecord(year, cast,
            ensemble = if (f[2].isEmpty()) emptyList()
                else f[2].split(","),
            track = f[3], detail = f[4], delivery = f[5],
            attMask = f[6].toIntOrNull() ?: 0,
            night = f[7], verdict = f[8], posterId = f[9],
            critic = f[10],
            review = if (f.size > 11) f[11] else "")
    }

    private fun saveRecord(state: GameState, rec: PlayRecord) {
        val castStr = rec.cast.entries
            .joinToString(",") { "${it.key}:${it.value}" }
        val raw = listOf("1", castStr,
            rec.ensemble.joinToString(","),
            rec.track, rec.detail, rec.delivery,
            rec.attMask.toString(), rec.night, rec.verdict,
            rec.posterId, rec.critic, rec.review)
            .joinToString("|")
        state.lifeText[recKey(rec.year)] = raw
    }

    /** Every production this save has on record, oldest
     *  first (the Journal's history lines read these). */
    fun allRecords(state: GameState): List<PlayRecord> =
        state.lifeText.keys
            .filter { it.startsWith("playRec:") }
            .mapNotNull { it.removePrefix("playRec:")
                .toIntOrNull() }
            .sorted()
            .mapNotNull { record(state, it) }

    // ---------------- which production is "this year's" ----

    /** Days after opening night a finished production
     *  stays "this year's" — the Journal keeps it as the
     *  page's subject (and its verdict stays the town's
     *  freshest theater news) before next year's cycle
     *  takes the page. */
    const val CURRENT_TAIL = 60

    /** The production this save is living through: the
     *  first whose living window — up to its opening, and
     *  CURRENT_TAIL days past it — hasn't closed. So a
     *  save's first consult mid-cycle joins that cycle
     *  (as audience if auditions already ran), and a save
     *  consulted after a fall this build didn't know
     *  about lands on NEXT year's show. */
    fun currentYear(state: GameState): Int {
        val thisYear = Yearbook.schoolYearOf(
            keyFromOrdinal(FairCalendar
                .ordinalOfAbsDay(state.absDay))) ?: 2027
        for (y in (thisYear - 1)..(thisYear + 1)) {
            val s = PlayCalendar.scheduleFor(y)
            if (s.openingDay + CURRENT_TAIL >= state.absDay)
                return y
        }
        return thisYear + 1
    }

    fun schedule(state: GameState): PlaySchedule =
        PlayCalendar.scheduleFor(currentYear(state))

    fun currentRecord(state: GameState): PlayRecord? =
        record(state, currentYear(state))

    /** The production is live for this save once its poster
     *  day has arrived (earlier, it's next term's rumor). */
    fun cycleLive(state: GameState): Boolean =
        state.absDay >= schedule(state).announceDay - 6

    // ---------------- casting ----------------

    private fun axis(stat: Int, want: Int): Double =
        if (want >= 0) want * stat / 10.0
        else -want * (10 - stat) / 10.0

    /** A kid's casting score for a role, from the town's
     *  real data: authored nerve, the bee's line-handling
     *  skill, and their actual chemistry with the director
     *  (bonds the town has really formed), plus a seeded
     *  tiebreak so equal kids don't sort by alphabet. */
    fun scoreFor(state: GameState, year: Int, kid: String,
                 role: PlayRole): Double {
        val chem = Photos.chemistryBetween(state, DIRECTOR, kid)
            .coerceIn(-2, 6) + 2          // axis value 0..8
        return axis(stageNerve(kid), role.wNerve) +
            axis(School.skillOf(kid), role.wVoice) +
            axis(chem, role.wChem) +
            tiebreak(year, role.id, kid)
    }

    private fun tiebreak(year: Int, roleId: String,
                         kid: String): Double =
        (Random(("playcast:$year:$roleId:$kid").hashCode()
            .toLong()).nextInt(100)) / 100.0

    /** The player's delivery profiles at auditions: the
     *  honest-input pattern — no secret right answer; each
     *  read leans the same casting math a different way. */
    val DELIVERIES: List<Pair<String, String>> = listOf(
        "bold" to "🎤 BIG AND BRAVE — give it everything, " +
            "dare the back row to look away",
        "true" to "🎤 SMALL AND TRUE — quiet, exact, " +
            "like you mean every word",
        "deadpan" to "🎤 DEADPAN — flat as a frozen pond; " +
            "let the room do the laughing")

    /** The player's pseudo-stats in the casting pool: an
     *  unknown kid (5s) bent by the delivery, with their
     *  REAL friendship with the director standing in for
     *  chemistry — a director casts kids she knows. */
    private fun playerScore(state: GameState, role: PlayRole,
                            delivery: String): Double {
        val nerve = 5 + if (delivery == "bold") 3 else 0
        val voice = 5 + if (delivery == "true") 3 else 0
        // the director-knowledge axis runs hotter than a
        // kid's raw pair chemistry: a director who really
        // knows you (friendship 60) hands you the heart
        // role over a marginally better stranger — the
        // suite pins the gradient (stranger: understudy;
        // known: ensemble; the director's kid: the
        // Shovel).
        val chem = ((state.friendship[DIRECTOR] ?: 0)
            .coerceIn(0, 60) / 6) +
            if (delivery == "deadpan") 2 else 0
        return axis(nerve, role.wNerve) +
            axis(voice, role.wVoice) +
            axis(chem.coerceAtMost(10), role.wChem)
    }

    /** Run casting for [year]: greedy by role prestige
     *  over the kid pool, the player inserted as a
     *  candidate iff [delivery] != null (they auditioned).
     *  Idempotent — a production casts exactly once, and
     *  the result is stored, so later friendship drift
     *  never recasts a posted list. */
    fun runCasting(state: GameState, year: Int,
                   delivery: String?): PlayRecord {
        record(state, year)?.let { return it }
        val pool = (CharacterSystem.CAST_KIDS + "You")
            .toMutableList()
        if (delivery == null) pool.remove("You")
        val cast = LinkedHashMap<String, String>()
        var playerTrack = ""
        var playerDetail = ""
        for (role in ROLES) {
            val best = pool.maxWithOrNull(
                compareBy<String> {
                    if (it == "You")
                        playerScore(state, role, delivery!!)
                    else scoreFor(state, year, it, role)
                }.thenBy { it }) ?: break
            pool.remove(best)
            if (best == "You") {
                playerTrack = "cast"; playerDetail = role.id
            } else cast[role.id] = best
        }
        // ensemble: the best remaining fits, in order
        val ensScored = pool.map { kid ->
            kid to ROLES.maxOf { r ->
                if (kid == "You")
                    playerScore(state, r, delivery!!)
                else scoreFor(state, year, kid, r) } }
            .sortedWith(compareByDescending<Pair<String, Double>>
                { it.second }.thenBy { it.first })
        val ensemble = mutableListOf<String>()
        for ((kid, _) in ensScored) {
            if (ensemble.size >= ENSEMBLE_SIZE) break
            if (kid == "You") {
                if (playerTrack.isEmpty()) playerTrack = "ens"
            } else ensemble += kid
        }
        if (delivery != null && playerTrack.isEmpty())
            playerTrack = "under"
        val rec = PlayRecord(year, cast, ensemble,
            track = playerTrack, detail = playerDetail,
            delivery = delivery ?: "")
        saveRecord(state, rec)
        state.life["playCastDay:$year"] = state.absDay
        for (kid in rec.memberKids()) plantPlayTopic(state, kid)
        return rec
    }

    /** The kid who came closest and got nothing — highest
     *  best-role score among kids neither cast nor
     *  ensembled. Their one disappointment line is owed,
     *  and it is kind (TalkGate serves it once). */
    fun passedOverKid(state: GameState,
                      rec: PlayRecord): String? {
        val taken = rec.memberKids().toSet()
        return CharacterSystem.CAST_KIDS
            .filter { it !in taken }
            .maxWithOrNull(compareBy<String> { kid ->
                ROLES.maxOf { r ->
                    scoreFor(state, rec.year, kid, r) }
            }.thenBy { it })
    }

    /** Plant "the play" in a kid's Threads topics,
     *  mirroring School's own planter exactly (the topic
     *  goes to the front; the memory keeps six). */
    fun plantPlayTopic(state: GameState, kid: String) {
        val mem = NpcMemoryStore.getOrCreate(kid)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    // ---------------- phases & the player's choices -------

    /** Who plays a role: the cast kid, or "You". */
    fun actorOf(rec: PlayRecord, roleId: String): String =
        rec.cast[roleId]
            ?: if (rec.track == "cast" && rec.detail == roleId)
                "You" else "—"

    /** Auditions are open on the audition afternoon
     *  itself, at the school, before the list is cast. */
    fun auditionOpen(state: GameState): Boolean {
        val s = schedule(state)
        return state.absDay == s.auditionDay &&
            state.timeMin in REH_FROM until REH_TO &&
            currentRecord(state) == null &&
            playerTrack(state).isEmpty()
    }

    fun playerTrack(state: GameState): String =
        currentRecord(state)?.track ?: ""

    /** The player auditions: casting runs NOW, with the
     *  player in the pool, judged by the same math as
     *  everyone. Returns the outcome text. */
    fun chooseAudition(state: GameState,
                       delivery: String): String? {
        if (!auditionOpen(state)) return null
        val rec = runCasting(state, currentYear(state), delivery)
        val role = rec.playerRole()
        return when {
            role != null ->
                "🎭 THE LIST GOES UP — Victoria pins it to " +
                    "the board, and the room reads it in the " +
                    "order rooms do: out loud. Your name is " +
                    "beside ${role.title}. ${role.blurb} " +
                    "Rehearsals start tomorrow, 3 PM. " +
                    "The mountain provides its own costume."
            rec.track == "ens" ->
                "🎭 THE LIST GOES UP — you're ENSEMBLE: " +
                    "a tree, the wind, the concept of " +
                    "Tuesday, and (act two) a second goat. " +
                    "Ensemble is where shows are actually " +
                    "held together. Rehearsals start " +
                    "tomorrow, 3 PM."
            else ->
                "🎭 THE LIST GOES UP — you're the " +
                    "UNDERSTUDY: every lead's shadow, every " +
                    "line learned twice. If anyone so much " +
                    "as sneezes wrong on opening night, the " +
                    "show is yours. Rehearsals start " +
                    "tomorrow, 3 PM — understudies attend " +
                    "them all."
        }
    }

    /** Crew duties: the show's other glory. */
    val DUTIES: List<Pair<String, String>> = listOf(
        "sets" to "🪚 SETS — the mountain is cardboard, " +
            "conviction, and one heroic coat of gray paint",
        "lights" to "🔦 LIGHTS — one borrowed spotlight, " +
            "aimed like you mean it",
        "props" to "🧹 PROPS — the shovel, the scissors, " +
            "the road bond (goat-proofing pending)")

    fun dutyLabel(duty: String): String = when (duty) {
        "sets" -> "sets"
        "lights" -> "lights"
        "props" -> "props"
        else -> duty
    }

    /** Join the crew: casting still runs (the kids fill
     *  the roles), and your name goes on the crew line of
     *  the playbill. */
    fun chooseCrew(state: GameState, duty: String): String? {
        if (!auditionOpen(state)) return null
        val rec = runCasting(state, currentYear(state), null)
        val updated = rec.copy(track = "crew", detail = duty)
        saveRecord(state, updated)
        return "🎭 CREW IT IS — ${dutyLabel(duty)}, under " +
            "Victoria's clipboard. The cast list goes up " +
            "without your name on it, and the playbill's " +
            "crew line goes up WITH it. Rehearsals start " +
            "tomorrow, 3 PM — crew attends them all; a set " +
            "doesn't build itself. (It does fall down " +
            "by itself. That's sets.)"
    }

    /** Decline a part: audience, by declared choice. The
     *  production still casts and still happens. */
    fun chooseAudience(state: GameState): String? {
        if (!auditionOpen(state)) return null
        val rec = runCasting(state, currentYear(state), null)
        saveRecord(state, rec.copy(track = "aud"))
        return "🎭 AUDIENCE, THEN — a good seat, a full " +
            "program, and opening night off. The show " +
            "goes on around you either way; that's what " +
            "shows do. The cast list's up — see the " +
            "Journal's 🎭 SCHOOL PLAY page for who's who."
    }

    // ---------------- rehearsals ----------------

    /** Today's rehearsal session index (0-based), or -1. */
    fun sessionToday(state: GameState): Int {
        val s = schedule(state)
        return s.rehearsalDays.indexOf(state.absDay)
    }

    /** Whether a given NPC makes a given session: seeded
     *  per (year, kid, session) — most kids who auditioned
     *  on purpose show up; nobody is scripted to flake. */
    fun npcAttends(year: Int, kid: String,
                   session: Int): Boolean =
        Random(("playreh:$year:$kid:$session").hashCode()
            .toLong()).nextInt(100) < NPC_ATTEND_PCT

    /** A cast/ensemble member's rehearsal coverage 0..1.
     *  The player's own coverage reads the played record. */
    fun coverageOf(state: GameState, rec: PlayRecord,
                   kid: String): Double {
        if (kid == "You")
            return Integer.bitCount(rec.attMask) /
                REHEARSALS.toDouble()
        var n = 0
        for (s in 0 until REHEARSALS)
            if (npcAttends(rec.year, kid, s)) n++
        return n / REHEARSALS.toDouble()
    }

    /** Mean rehearsal coverage across the named cast +
     *  ensemble (NPCs only — the player's own coverage is
     *  tracked separately and weighted in at the night). */
    fun castCoverage(state: GameState,
                     rec: PlayRecord): Double {
        val members = rec.memberKids()
        if (members.isEmpty()) return 0.0
        return members.sumOf { coverageOf(state, rec, it) } /
            members.size
    }

    /** The night's preparation number: the town's coverage
     *  does most of the work (0.6), and YOUR attendance is
     *  the rest (0.4) when you hold a part or a duty — so
     *  skipping your rehearsals shows on the night,
     *  honestly, and only there. Audience: the cast's
     *  coverage alone carries it. */
    fun prepScore(state: GameState, rec: PlayRecord): Double {
        val npc = castCoverage(state, rec)
        if (rec.track !in listOf("cast", "ens", "under",
                "crew")) return npc
        return 0.6 * npc + 0.4 * coverageOf(state, rec, "You")
    }

    /** The player may stay for today's rehearsal iff: a
     *  rehearsal day, in the afternoon window, they hold
     *  a part or a duty, and today isn't banked yet. */
    fun rehearsalOpen(state: GameState): Boolean {
        val rec = currentRecord(state) ?: return false
        if (rec.track !in listOf("cast", "ens", "under",
                "crew")) return false
        val idx = sessionToday(state)
        if (idx < 0) return false
        if (state.timeMin !in REH_FROM until REH_TO)
            return false
        return rec.attMask and (1 shl idx) == 0
    }

    fun rehearsalLabel(state: GameState): String {
        val idx = sessionToday(state)
        return "🎭 Stay for rehearsal " +
            "${idx + 1} of $REHEARSALS — ${sessionTheme(idx)}"
    }

    fun sessionTheme(idx: Int): String = listOf(
        "the table read", "blocking, with chalk",
        "the goat fitting (and set day)",
        "the dress rehearsal")[idx.coerceIn(0, 3)]

    /** Stay for rehearsal: banks the session, sprinkles
     *  friendship to the castmates actually in the room
     *  (seeded present, like the room really was), and
     *  returns the session's vignette. */
    fun attendRehearsal(state: GameState): String? {
        if (!rehearsalOpen(state)) return null
        val rec = currentRecord(state) ?: return null
        val idx = sessionToday(state)
        val updated = rec.copy(
            attMask = rec.attMask or (1 shl idx))
        saveRecord(state, updated)
        val present = rec.memberKids()
            .filter { npcAttends(rec.year, it, idx) }
        for (kid in present) {
            state.friendship.merge(kid, REHEARSAL_PAY,
                Int::plus)
        }
        val others = present.take(3).joinToString(", ")
        val who = if (others.isEmpty()) "a determined few"
            else others
        return when (rec.track) {
            "cast" -> {
                val role = rec.playerRole()
                val line = role?.lines?.getOrNull(idx % 3) ?: ""
                "🎭 REHEARSAL ${idx + 1} — ${sessionTheme(idx)}. " +
                    "In the room: $who. You run your lines " +
                    "as ${role?.title ?: "your part"}: " +
                    "“$line” Victoria nods the nod that " +
                    "means 'again, but that counted.' " +
                    "Lines learned: " +
                    "${Integer.bitCount(updated.attMask)} " +
                    "of $REHEARSALS."
            }
            "ens" ->
                "🎭 REHEARSAL ${idx + 1} — ${sessionTheme(idx)}. " +
                    "In the room: $who. Ensemble work: you " +
                    "are, variously, a pine tree, the wind, " +
                    "and the concept of Tuesday. The " +
                    "mountain's scenes have never stood " +
                    "straighter. Sessions: " +
                    "${Integer.bitCount(updated.attMask)} " +
                    "of $REHEARSALS."
            "under" ->
                "🎭 REHEARSAL ${idx + 1} — ${sessionTheme(idx)}. " +
                    "In the room: $who. From the wings you " +
                    "learn three people's lines cold, " +
                    "including the goat's (the goat's are " +
                    "stage directions, but you learn them). " +
                    "Sessions: " +
                    "${Integer.bitCount(updated.attMask)} " +
                    "of $REHEARSALS."
            else -> // crew
                "🎭 REHEARSAL ${idx + 1} — ${sessionTheme(idx)}. " +
                    "In the room: $who. Crew work " +
                    "(${dutyLabel(rec.detail)}): " +
                    crewWorkLine(rec.detail, idx) + " Sets " +
                    "built: " +
                    "${Integer.bitCount(updated.attMask)} " +
                    "of $REHEARSALS."
        }
    }

    private fun crewWorkLine(duty: String, idx: Int): String =
        when (duty) {
            "sets" -> listOf(
                "the mountain gets its first coat of gray " +
                    "and immediately looks more like a " +
                    "mountain than the cardboard has any " +
                    "right to.",
                "blocking chalk says the mountain must " +
                    "not wobble. You brace it like a bridge.",
                "the valley backdrop goes up. From the " +
                    "back row it reads 'valley'. From the " +
                    "front it reads 'effort', which is " +
                    "better.",
                "dress rehearsal: the mountain survives " +
                    "being leaned on by the entire plot."
                )[idx.coerceIn(0, 3)]
            "lights" -> listOf(
                "you learn which switch is the spotlight " +
                    "and which one is the fire alarm. " +
                    "Labeled, now. In pen.",
                "the Weather's entrance gets a blue wash. " +
                    "It is, technically, weather.",
                "the borrowed spotlight learns its one " +
                    "job: find the goat, follow the goat.",
                "dress rehearsal: every cue landed. The " +
                    "dark between scenes feels on purpose."
                )[idx.coerceIn(0, 3)]
            else -> listOf(
                "the shovel is sanded, painted, and given " +
                    "a name by the cast. The name is " +
                    "classified.",
                "the Mayor's scissors are checked for " +
                    "actual ribbon-cutting capability. " +
                    "They pass.",
                "the road bond is printed twice and " +
                    "goat-proofed once. (Goat-proofing " +
                    "is a hope, not a technology.)",
                "dress rehearsal: every prop is where " +
                    "the night will reach for it."
                )[idx.coerceIn(0, 3)]
        }

    // ---------------- the agenda claim ----------------

    /** The household adults of a kid, from the dinner
     *  system's households — opening night's audience is
     *  the cast's real families, not a rented crowd. */
    private fun familyAdultsOf(kid: String): List<String> =
        Dinners.HOUSEHOLDS.firstOrNull { kid in it.members }
            ?.members?.filter {
                CharacterSystem.isAdult(it) } ?: emptyList()

    /** Whether opening night can actually happen tonight:
     *  cast, unplayed, in the grace window, and no bigger
     *  town moment owns the evening. The deference list is
     *  the brief's own, read through the town's standing
     *  definitions: a case climax due, a season finale's
     *  window, or a holiday BEAT today (School.quietReason
     *  — a holiday merely existing that week does NOT own
     *  the night; Halloween's orbit covers the 2027
     *  opening, and the show plays), plus a capstone
     *  CEREMONY actually live (one authored evening for
     *  one townsperson — the Emergencies ceremony
     *  clause). A Bijou screening the same night is NOT
     *  a deferral: screenings run every Friday, the
     *  claim stack arbitrates the people, and the cast
     *  chooses its own opening night. */
    fun nightCanHappen(state: GameState): Boolean {
        val rec = currentRecord(state) ?: return false
        if (rec.night.isNotEmpty()) return false
        val s = schedule(state)
        if (state.absDay !in s.openingDay..
                (s.openingDay + GRACE_DAYS)) return false
        if (School.quietReason(state) != null) return false
        if (state.timeMin in Capstones.CEREMONY_FROM until
            Capstones.CEREMONY_TO &&
            Capstones.CHAINS.any {
                Capstones.activeBeat(state, it.factionId)
                    ?.beat == 3 }) return false
        return true
    }

    /** The play's agenda claim, seated in
     *  Engine.effectiveAgendaBlock with the set pieces
     *  (after Heroes, before the crew hook — later claims
     *  win in the stack, so this one outranks the dinner
     *  claims seated earlier): rehearsal afternoons seat
     *  the cast + director at the school; opening night
     *  seats the cast, the director and the cast's
     *  families. That means on the one night a year, the
     *  show outranks a family dinner for its own people —
     *  the stated call: the show must go on, and it is
     *  one evening. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        val rec = currentRecord(state) ?: return null
        if (rec.night.isNotEmpty()) return null
        val inShow = npcName in rec.memberKids() ||
            npcName == DIRECTOR
        // rehearsal afternoons
        val idx = sessionToday(state)
        if (idx >= 0 && timeMin in REH_FROM until REH_TO) {
            if (!inShow) return null
            val present = npcName == DIRECTOR ||
                npcAttends(rec.year, npcName, idx)
            if (present) return AgendaBlock(SCHOOL,
                "🎭 Rehearsal — “$TITLE”")
            return null
        }
        // opening night: cast, crew's audience, families
        if (nightCanHappen(state) &&
            timeMin in FINAL_FROM until HOUSE_TO) {
            val audience = rec.memberKids()
                .flatMap { familyAdultsOf(it) }.toSet()
            if (inShow || npcName in audience)
                return AgendaBlock(SCHOOL,
                    "🎭 Opening night — “$TITLE”")
        }
        return null
    }

    // ---------------- opening night ----------------

    /** The night's one seeded mishap, if any: one of the
     *  four stations can wobble; a fifth of years, nothing
     *  does and the show runs clean. */
    fun mishapFor(year: Int): String {
        val kinds = listOf("goat", "backdrop", "spotlight",
            "shovel", "")
        val idx = Random(("playmishap:$year").hashCode()
            .toLong()).nextInt(5)
        return kinds[idx]
    }

    fun mishapText(mishap: String): String = when (mishap) {
        "goat" -> "Mid-scene, the goat — unimpressed by " +
            "the second act's pacing — wanders to center " +
            "stage and sits down in the spotlight. The " +
            "audience assumes it's blocking. It is now the " +
            "most-reviewed performance of the night."
        "backdrop" -> "Behind the Mountain, the valley " +
            "backdrop gives a long, thoughtful lean to the " +
            "left. It is not falling. It is DECIDING."
        "spotlight" -> "The borrowed spotlight dies with " +
            "a sound like a small appliance giving up. " +
            "The stage goes honest-dark for one long beat."
        "shovel" -> "The Kid With the Shovel reaches for " +
            "the shovel — the shovel is not there. The " +
            "shovel is, at this moment, in the audience, " +
            "being held by someone's dad."
        else -> ""
    }

    /** The set piece presents when the night can happen,
     *  the house is up, and the player is standing in the
     *  school. Safe-moment gated from the chain's bottom
     *  in GameActivity, below every climax in town. */
    fun finaleDue(state: GameState): Boolean {
        if (!nightCanHappen(state)) return false
        if (state.timeMin !in FINAL_FROM..FINAL_TO)
            return false
        val me = state.agents.values
            .firstOrNull { it.isPlayer } ?: return false
        return me.location == SCHOOL && state.playerInside
    }

    /** The show as it plays: five beats with the real cast
     *  slotted in, quoting the production's actual lines. */
    fun finaleIntro(state: GameState): List<Line> {
        val rec = currentRecord(state) ?: return emptyList()
        fun a(roleId: String) = actorOf(rec, roleId)
        fun line(roleId: String, idx: Int) =
            roleOf(roleId)?.lines?.getOrNull(idx) ?: ""
        val out = mutableListOf<Line>()
        out += Line("🎭 Opening Night",
            "The gym is a theater tonight: folding chairs, " +
                "a borrowed spotlight, and a sign over the " +
                "door that reads “$TITLE” in letters the " +
                "art class is quietly proud of. " +
                "${DIRECTOR} takes one breath, the way " +
                "principals do before either assemblies or " +
                "history.")
        out += Line("🎭 ${a("weather")} (The Weather)",
            "“${line("weather", 0)}”")
        out += Line("🎭 ${a("mayor")} (The Mayor)",
            "“${line("mayor", 0)}” The valley holds its " +
                "breath. Somewhere, a mountain does not.")
        out += Line("🎭 ${a("mountain")} (The Mountain)",
            "“${line("mountain", 0)}” Delivered without " +
                "moving a single muscle. The room feels " +
                "it in its shoes.")
        out += Line("🎭 ${a("shovel")} (The Kid With The Shovel)",
            "“${line("shovel", 0)}”")
        out += Line("🎭 ${a("goat")} (The Goat)",
            line("goat", 0))
        return out
    }

    /** Your one played beat, by your track. The honest-
     *  input pattern: three real choices, no secret right
     *  answer — the night judges them against the state
     *  of the show, kindly. */
    fun finaleChoices(state: GameState): List<String> {
        val rec = currentRecord(state) ?: return emptyList()
        val mishap = mishapFor(rec.year)
        return when (rec.track) {
            "cast", "ens", "under" -> listOf(
                "🎤 PLAY IT BIG — commit, full voice, " +
                    "back row or bust",
                "🎤 PLAY IT TRUE — small, exact, meant",
                if (mishap.isEmpty())
                    "🙋 AD-LIB — invent a save nobody " +
                        "needs (show-off maneuver)"
                else
                    "🙋 COVER — improvise the save, hold " +
                        "the scene together")
            "crew" -> listOf(
                "🎛️ HOLD STEADY — your cue, exactly as " +
                    "rehearsed",
                "🛠️ SAVE IT — improvise the fix, now",
                "🎲 LET IT RIDE — trust the show to " +
                    "absorb it")
            else -> listOf(
                "😂 LAUGH FIRST — loud, so the room can",
                "🤫 HOLD THE QUIET — let the moment land",
                "👏 START THE APPLAUSE — bring the " +
                    "room with you")
        }
    }

    /** Judge the player's moment: returns (result, beloved)
     *  — result in nailed|landed|fumbled. */
    fun momentResult(state: GameState, rec: PlayRecord,
                     choiceIdx: Int): Pair<String, Boolean> {
        val mishap = mishapFor(rec.year)
        val prep = prepScore(state, rec)
        when (rec.track) {
            "cast", "ens" -> {
                val role = rec.playerRole()
                val fitIdx = when {
                    rec.track == "ens" -> 1
                    role?.comic == true -> 0
                    else -> 1
                }
                return when {
                    mishap.isNotEmpty() && choiceIdx == 2 ->
                        "nailed" to false
                    mishap.isNotEmpty() && choiceIdx == fitIdx ->
                        "landed" to false
                    mishap.isNotEmpty() ->
                        "fumbled" to true
                    choiceIdx == fitIdx -> "nailed" to false
                    else -> "landed" to false
                }
            }
            "under" -> {
                return when {
                    mishap.isNotEmpty() && choiceIdx == 2 ->
                        "nailed" to false
                    mishap.isNotEmpty() ->
                        "landed" to false
                    choiceIdx == 1 -> "nailed" to false
                    else -> "landed" to false
                }
            }
            "crew" -> {
                val myStation = when (rec.detail) {
                    "sets" -> "backdrop"
                    "lights" -> "spotlight"
                    else -> "shovel"
                }
                val hit = mishap == myStation
                return when {
                    hit && choiceIdx == 1 ->
                        "nailed" to false
                    hit && choiceIdx == 0 ->
                        "fumbled" to true
                    hit -> "landed" to false
                    !hit && choiceIdx == 0 ->
                        "nailed" to false
                    else -> "landed" to false
                }
            }
            else -> {  // audience: your moment moves the room
                val ov = ovationNow(state, rec)
                return when (choiceIdx) {
                    2 -> if (ov >= 0.5) "nailed" to false
                        else "landed" to false
                    1 -> if (prep >= 0.7) "nailed" to false
                        else "landed" to false
                    else -> "landed" to false
                }
            }
        }
    }

    fun momentPoints(result: String): Double = when (result) {
        "nailed" -> 1.0
        "landed" -> 0.6
        else -> 0.2
    }

    /** Cast chemistry on the night, normalized 0..1 from
     *  the named cast's real pairwise chemistry (bonds —
     *  including the ones rehearsals grew). */
    fun castChemN(state: GameState, rec: PlayRecord): Double {
        val actors = rec.cast.values.toList()
        if (actors.size < 2) return 0.25
        var sum = 0; var n = 0
        for (i in actors.indices) for (j in i + 1 until actors.size) {
            sum += Photos.chemistryBetween(state,
                actors[i], actors[j]); n++
        }
        val avg = sum.toDouble() / n
        return ((avg + 2.0) / 8.0).coerceIn(0.0, 1.0)
    }

    /** Who is actually in the room right now: the
     *  school's own presence read (awake, arrived, on the
     *  player's side of the door). */
    fun crowdNow(state: GameState): List<String> =
        School.presentAt(state)

    /** The standing-ovation read: computed from the room —
     *  how full it is, and how much the room actually
     *  loves the cast (each audience member's best real
     *  chemistry with anyone on stage). */
    fun ovationNow(state: GameState,
                   rec: PlayRecord): Double {
        val crowd = crowdNow(state)
        val onStage = (rec.memberKids() + DIRECTOR).toSet()
        val audience = crowd.filter { it !in onStage }
        val fullness = (crowd.size / 26.0).coerceIn(0.0, 1.0)
        val love = if (audience.isEmpty()) 0.15 else {
            val per = audience.map { member ->
                val best = rec.memberKids().maxOfOrNull {
                    Photos.chemistryBetween(state, member, it)
                } ?: 0
                ((best + 2.0) / 8.0).coerceIn(0.0, 1.0)
            }
            per.average()
        }
        return (0.55 * fullness + 0.45 * love)
            .coerceIn(0.0, 1.0)
    }

    /** THE JUDGE, pure: preparation, chemistry, the
     *  player's moment and the room, weighted once. Three
     *  honest outcomes, all of them good nights. */
    fun verdictFor(prep: Double, chemN: Double,
                   momentPts: Double, beloved: Boolean,
                   ovation: Double): String {
        val score = 0.45 * prep + 0.25 * chemN +
            0.20 * momentPts + 0.10 * ovation
        return when {
            score >= 0.70 -> "triumph"
            score <= 0.40 -> "disaster"
            beloved && ovation >= 0.55 -> "disaster"
            else -> "solid"
        }
    }

    fun verdictWord(verdict: String): String = when (verdict) {
        "triumph" -> "TRIUMPH"
        "disaster" -> "BEAUTIFUL DISASTER"
        else -> "SOLID"
    }

    /** Shared rehearsals settle into the cast's pair
     *  bonds: +1 for every pair that shared 2+ sessions
     *  (settled once, at the wrap — the chemistry the
     *  night was judged by already counted tonight's
     *  bonds only as they stood, so this is the NEXT
     *  show's head start, honestly labeled). */
    private fun settleBonds(state: GameState,
                            rec: PlayRecord) {
        val key = "playBonds:${rec.year}"
        if (state.life[key] != null) return
        state.life[key] = 1
        val members = rec.memberKids()
        for (i in members.indices) for (j in i + 1 until members.size) {
            var shared = 0
            for (s in 0 until REHEARSALS)
                if (npcAttends(rec.year, members[i], s) &&
                    npcAttends(rec.year, members[j], s))
                    shared++
            if (shared >= 2) {
                val pk = Threads.pairKey(members[i],
                    members[j])
                val cur = state.npcBonds[pk] ?: 0
                if (cur < 8) state.npcBonds[pk] = cur + 1
            }
        }
    }

    /** THE POSTER: one real photo record of the posed
     *  finale, composed by the photo system's own group
     *  rules from the people actually in the room, and
     *  filed by applyPhoto like any photo — the darkroom
     *  prints it, the album keeps it. No pixel costumes:
     *  the caption carries the production. Returns the
     *  record's id, or "" if the camera stayed home. */
    fun mintPoster(state: GameState,
                   rec: PlayRecord): String {
        val present = School.presentAt(state)
        if (present.isEmpty()) return ""
        val me = state.agents.values
            .firstOrNull { it.isPlayer }
        val shot = Photos.composeGroup(state, present,
            emptyList(), me?.name ?: "New Kid",
            me?.appearance ?: emptyMap())
        val castLine = ROLES.joinToString("; ") { r ->
            "${r.title.lowercase().replaceFirstChar {
                it.uppercase() }}: ${actorOf(rec, r.id)}" }
        val poster = shot.copy(caption =
            "🎭 “$TITLE” — opening night, South Park " +
                "Elementary. $castLine. Verdict: " +
                verdictWord(rec.verdict) + ".")
        Photos.applyPhoto(state, poster)
        return poster.id
    }

    /** Play the night: judge the moment, compute the
     *  verdict from the room as it stands, mint the
     *  poster, settle the bonds, and return the closing
     *  text. The review lands tomorrow morning. */
    fun playFinale(state: GameState,
                   choiceIdx: Int): String? {
        if (!finaleDue(state)) return null
        val rec = currentRecord(state) ?: return null
        val (result, beloved) = momentResult(state, rec,
            choiceIdx)
        val prep = prepScore(state, rec)
        val chemN = castChemN(state, rec)
        val ovation = ovationNow(state, rec)
        val verdict = verdictFor(prep, chemN,
            momentPoints(result), beloved, ovation)
        var updated = rec.copy(night = "played",
            verdict = verdict)
        saveRecord(state, updated)
        val posterId = mintPoster(state, updated)
        if (posterId.isNotEmpty()) {
            updated = updated.copy(posterId = posterId)
            saveRecord(state, updated)
        }
        settleBonds(state, updated)
        state.life["playNightDay:${rec.year}"] = state.absDay
        val mishap = mishapFor(rec.year)
        val momentLine = when (result) {
            "nailed" -> "Your moment lands like it was " +
                "rehearsed by fate itself. Somewhere in " +
                "the third row, a dad whispers 'that's " +
                "the new kid' with tremendous respect."
            "landed" -> "Your moment lands — not silk, " +
                "but honest canvas. The scene holds, the " +
                "room stays with you, and the show rolls " +
                "on, which is the whole job."
            else -> "Your moment goes gloriously sideways " +
                "— and the room LOVES it. It is the laugh " +
                "people will describe at breakfast. The " +
                "show doesn't survive your moment; it is " +
                "carried by it."
        }
        val ovationLine = when {
            ovation >= 0.75 -> "The ovation is a standing " +
                "one, started by the back row and finished " +
                "by everyone — computed from a room that " +
                "genuinely showed up for these kids."
            ovation >= 0.45 -> "The applause is warm and " +
                "long — folding chairs scrape as half the " +
                "room stands, then the other half decides " +
                "it meant to as well."
            else -> "The applause is fond and a little " +
                "thin — a small house, honestly counted, " +
                "clapping like family, which it mostly is."
        }
        val verdictLine = when (verdict) {
            "triumph" -> "🎭 VERDICT OF THE ROOM: TRIUMPH. " +
                "The mountain did not move. Nobody wanted " +
                "it to, by the end. That's theater."
            "disaster" -> "🎭 VERDICT OF THE ROOM: A " +
                "BEAUTIFUL DISASTER — the beloved kind, " +
                "the kind a town retells for years. " +
                "Nothing about tonight will be remembered " +
                "accurately, and all of it will be " +
                "remembered fondly."
            else -> "🎭 VERDICT OF THE ROOM: SOLID. A " +
                "real show, honestly built, honestly " +
                "played — the kind that makes next year's " +
                "auditions crowded."
        }
        return buildString {
            if (mishap.isNotEmpty()) {
                append("⚠️ THE MISHAP — ")
                append(mishapText(mishap))
                append("\n\n")
            }
            append("🎭 YOUR MOMENT — ")
            append(momentLine)
            append("\n\n")
            append(ovationLine)
            append("\n\n")
            append(verdictLine)
            if (posterId.isNotEmpty()) append("\n\n📸 The " +
                "cast takes its bow in a posed finale — " +
                "one real photo, filed in your album. The " +
                "darkroom can print it: a playbill for " +
                "the print wall.")
            append("\n\n📰 The review lands tomorrow " +
                "morning — ${criticFor(rec.year)} has the " +
                "pencil for the school paper this year.")
        }
    }

    /** The show goes on without a set piece: slipped past
     *  its grace evenings, or the player never came. The
     *  verdict is computed from the cast's own coverage
     *  and chemistry with a seeded room — the record says
     *  what happened, kindly, and no poster is minted
     *  (nobody's camera was there). */
    fun wrapOffstage(state: GameState) {
        val rec = currentRecord(state) ?: return
        if (rec.night.isNotEmpty()) return
        val prep = prepScore(state, rec)
        val chemN = castChemN(state, rec)
        val rnd = Random(("playoffstage:${rec.year}")
            .hashCode().toLong())
        val ovation = 0.35 + rnd.nextInt(30) / 100.0
        val verdict = verdictFor(prep, chemN, 0.5,
            beloved = false, ovation = ovation)
        saveRecord(state, rec.copy(night = "offstage",
            verdict = verdict))
        settleBonds(state, rec)
        // the show's night, for the review's timing, is
        // the last grace evening it could have played
        state.life["playNightDay:${rec.year}"] = minOf(
            state.absDay, PlayCalendar
                .scheduleFor(rec.year).openingDay + GRACE_DAYS)
    }

    /** The morning-after reviewer, in the yearbook
     *  judges' rotation (the "stage" desk: category 5). */
    fun criticFor(year: Int): String =
        Yearbook.judgeFor(year, 5).name

    /** Compose the review from the record's facts alone:
     *  the verdict, the lead, the player's part, the
     *  poster's existence. If the critic starred in the
     *  show, the review says so — conflict of interest,
     *  disclosed, in the school-paper tradition. */
    fun composeReview(state: GameState,
                      rec: PlayRecord): String {
        val critic = criticFor(rec.year)
        val lead = actorOf(rec, "mountain")
        val idx = Random(("playreview:${rec.year}")
            .hashCode().toLong()).nextInt(2)
        val body = when (rec.verdict) {
            "triumph" -> listOf(
                "Five stars and one of them is for the " +
                    "quiet. $lead, as the Mountain, does " +
                    "the hardest thing in theater — nothing " +
                    "— and the gym holds its breath for " +
                    "it. This is the show the school will " +
                    "measure other shows against.",
                "A triumph with folding chairs. The " +
                    "valley moved me; the Mountain ($lead) " +
                    "didn't have to. When the goat sat " +
                    "down, I understood our whole town."
                )[idx]
            "disaster" -> listOf(
                "A beautiful disaster, and I choose both " +
                    "words with care. Things leaned, " +
                    "wandered and went missing, and I " +
                    "have never heard this gym happier. " +
                    "$lead's Mountain never broke. " +
                    "Everything else did. Perfect.",
                "Five stars for the chaos alone. The " +
                    "show fell apart in the exact shape of " +
                    "our town and we loved it for that. " +
                    "The goat has a future. The backdrop " +
                    "has a past. $lead is a mountain."
                )[idx]
            else -> listOf(
                "A solid night of theater: lines landed, " +
                    "cues held, and $lead's Mountain gave " +
                    "the valley something to argue with. " +
                    "Not a legend — a foundation. Next " +
                    "year's show stands on this one.",
                "Honest work, honestly reviewed: the " +
                    "cast knew their play and the play " +
                    "knew its town. The Kid With The " +
                    "Shovel broke my heart a little. " +
                    "The goat broke nothing, which for " +
                    "the goat is growth."
                )[idx]
        }
        val playerLine = when (rec.track) {
            "cast" -> " And the new kid, as " +
                "${rec.playerRole()?.title ?: "their part"}, " +
                "was part of why it worked."
            "ens" -> " The ensemble — the new kid among " +
                "them — held every scene they stood in."
            "under" -> " Credit to the understudy bench, " +
                "the new kid included: readiness is a " +
                "performance too."
            "crew" -> " The ${dutyLabel(rec.detail)} work, " +
                "the new kid's hands in it, held when it " +
                "mattered."
            else -> ""
        }
        val conflict = if (critic in rec.memberKids() ||
            actorOf(rec, "mountain") == critic)
            " (Full disclosure: I was IN this show. " +
                "The paper has been notified. The paper " +
                "is also me.)" else ""
        return "“$body$playerLine” — $critic, the school " +
            "paper$conflict"
    }

    /** The morning after the night: the review is
     *  stamped, the town log carries it, and the cast's
     *  Threads topics turn to talk about it. */
    private fun stampReview(state: GameState,
                            rec: PlayRecord) {
        if (rec.review.isNotEmpty()) return
        val review = composeReview(state, rec)
        val updated = rec.copy(
            critic = criticFor(rec.year), review = review)
        saveRecord(state, updated)
        state.life["playReviewDay:${rec.year}"] = state.absDay
        state.townLog.add("🎭 “$TITLE” (${rec.year}) — " +
            verdictWord(rec.verdict) + ". $review")
        while (state.townLog.size > 120)
            state.townLog.removeAt(0)
        for (kid in rec.memberKids()) plantPlayTopic(state, kid)
    }

    // ---------------- mornings ----------------

    /** The play's mornings, called by Engine.onNewDay
     *  AFTER Fair and BEFORE the phone (its texts derive
     *  from these facts): the poster's ticker on
     *  announcement morning, casting for a player who
     *  never auditioned, the offstage wrap once the grace
     *  evenings pass, and the morning-after review. */
    fun dailyRollover(state: GameState) {
        if (!cycleLive(state)) return
        val year = currentYear(state)
        val s = PlayCalendar.scheduleFor(year)
        val rec = record(state, year)
        // announcement morning: the poster's ticker line
        if (state.absDay == s.announceDay &&
            state.life["playAnn:$year"] == null) {
            state.life["playAnn:$year"] = 1
            note(state,
                "🎭 POSTER UP at the Elementary: " +
                    "“$TITLE” — this year's school play. " +
                    "Auditions ${dateLabel(s.auditionDay)}, " +
                    "opening night ${dateLabel(s.openingDay)}. " +
                    "$DIRECTOR directs.")
        }
        // casting fallback: auditions came and went
        if (rec == null && state.absDay > s.auditionDay) {
            runCasting(state, year, null)
        }
        val cur = record(state, year) ?: return
        // the grace evenings passed unplayed: the show
        // went on without a set piece
        if (cur.night.isEmpty() &&
            state.absDay > s.openingDay + GRACE_DAYS) {
            wrapOffstage(state)
        }
        // the morning after the night: the review
        val after = record(state, year) ?: return
        val nightDay = state.life["playNightDay:$year"]
        if (after.night.isNotEmpty() &&
            after.review.isEmpty() && nightDay != null &&
            state.absDay > nightDay) {
            stampReview(state, after)
        }
    }

    /** The ticker note (the School/Fair pattern: the
     *  event feed, capped). */
    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40)
            state.events.removeAt(0)
    }

    /** "Fri, Nov 6"-style labels from the calendar math. */
    fun dateLabel(absDay: Int): String {
        val key = keyFromOrdinal(FairCalendar
            .ordinalOfAbsDay(absDay))
        val m = key.substring(4, 6).toIntOrNull() ?: 1
        val d = key.substring(6, 8).toIntOrNull() ?: 1
        val months = listOf("Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec")
        val dow = Jobs.WEEKDAY_NAMES[
            Jobs.weekdayOf(absDay)].take(3)
        return "$dow ${months[m - 1]} $d"
    }

    // ---------------- talk ----------------

    /** The play's owed talk line, in the TalkGate chain's
     *  tail style: post-show lines quoting the real
     *  verdict, the passed-over kid's one honest line,
     *  cast talk during the rehearsal weeks, and a cast
     *  parent's mention at their family's dinner table. */
    fun gossipLine(state: GameState,
                   npcName: String): Line? {
        if (!cycleLive(state)) return null
        val rec = currentRecord(state)
        val year = currentYear(state)
        val s = schedule(state)
        // --- after the show: the verdict, in their words ---
        if (rec != null && rec.night.isNotEmpty()) {
            val nightDay = state.life["playNightDay:$year"]
                ?: state.absDay
            if (state.absDay - nightDay in 0..10) {
                val key = "playPost:$year:$npcName"
                if (state.life[key] == null) {
                    val roleId = rec.cast.entries
                        .firstOrNull { it.value == npcName }?.key
                    val role = roleId?.let { roleOf(it) }
                    if (role != null) {
                        state.life[key] = 1
                        return Line(npcName,
                            "You were at the show? I was " +
                                "${role.title}. " +
                                "${verdictWord(rec.verdict)}" +
                                " — the paper said so and " +
                                "everything. I have decided " +
                                "to be normal about it, which " +
                                "is why I mention it first.")
                    }
                    if (npcName in rec.ensemble) {
                        state.life[key] = 1
                        return Line(npcName,
                            "Ensemble, and proud: the show " +
                                "was ${verdictWord(rec.verdict)}" +
                                ". My tree got a compliment. " +
                                "A TREE compliment. Fridge-worthy.")
                    }
                    if (npcName == DIRECTOR) {
                        state.life[key] = 1
                        return Line(npcName,
                            "The production was " +
                                "${verdictWord(rec.verdict)}" +
                                ". I direct one play a year, " +
                                "and I remember every one of " +
                                "them exactly like this one: " +
                                "entirely.")
                    }
                }
            }
            return null
        }
        if (rec == null) return null
        // --- the passed-over kid's one line (kind) ---
        val passed = passedOverKid(state, rec)
        if (npcName == passed &&
            state.life["playPass:$year"] == null &&
            state.absDay <= s.openingDay + 7) {
            state.life["playPass:$year"] = 1
            return Line(npcName,
                "I read for the Mountain. Read WELL, " +
                    "between us. ...No, it's fine. Honestly. " +
                    "Somebody has to be the best actor in " +
                    "the audience, and I have accepted the " +
                    "role. Front row. I'll be magnificent.")
        }
        // --- a cast parent, at their family's dinner ---
        if (CharacterSystem.isAdult(npcName) &&
            state.absDay <= s.openingDay + 4) {
            val hh = Dinners.HOUSEHOLDS.firstOrNull {
                npcName in it.members }
            if (hh != null) {
                val kidInShow = hh.members.firstOrNull {
                    it in rec.memberKids() }
                val tonight = Dinners.todaysInvite(state)?.id ==
                    hh.id ||
                    Dinners.hostingTonight(state)?.id == hh.id
                if (kidInShow != null && tonight &&
                    Dinners.windowOpen(state) &&
                    state.life["playDinner:$year:$npcName"]
                        == null) {
                    state.life["playDinner:$year:$npcName"] = 1
                    val roleId = rec.cast.entries
                        .firstOrNull {
                            it.value == kidInShow }?.key
                    val part = roleId?.let { roleOf(it)?.title }
                        ?: "the ensemble"
                    return Line(npcName,
                        "Our $kidInShow is $part in the " +
                            "school play this year. The " +
                            "whole family's going opening " +
                            "night. We've been rehearsing our " +
                            "applause at home. It's ready.")
                }
            }
        }
        // --- during the run-up: cast talk ---
        if (state.absDay in s.auditionDay..s.openingDay &&
            (state.absDay +
                (npcName.hashCode() and 0x7fffffff)) % 3 == 0) {
            val roleId = rec.cast.entries
                .firstOrNull { it.value == npcName }?.key
            val role = roleId?.let { roleOf(it) }
            if (role != null) {
                val idx = state.absDay % role.lines.size
                return Line(npcName,
                    "I'm ${role.title} in the play. My " +
                        "big line: “${role.lines[idx]}” " +
                        "Don't applaud yet. Save it for " +
                        "${dateLabel(s.openingDay)}.")
            }
            if (npcName in rec.ensemble) {
                return Line(npcName,
                    "Ensemble for the play. I play a tree, " +
                        "the wind, and — act two — the " +
                        "concept of being slightly in the " +
                        "way. Typecast, honestly.")
            }
            if (npcName == DIRECTOR) {
                return Line(npcName,
                    "The play is coming together the way " +
                        "plays do: terribly, then suddenly. " +
                        "Opening night is " +
                        "${dateLabel(s.openingDay)}. The goat " +
                        "has notes. The goat always has notes.")
            }
        }
        return null
    }

    // ---------------- look & board ----------------

    /** The Elementary's play business, for Look (the
     *  School.lookText seam): the poster, the cast list,
     *  rehearsals in earshot, opening night, the pinned
     *  playbill after. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != SCHOOL) return null
        if (!cycleLive(state)) return null
        val s = schedule(state)
        val rec = currentRecord(state)
        val today = state.absDay
        if (rec != null && rec.night.isNotEmpty()) {
            val nightDay = state.life["playNightDay:${rec.year}"]
                ?: today
            if (today - nightDay in 0..10) {
                return "🎭 The playbill from “$TITLE” is " +
                    "still pinned to the board: verdict — " +
                    verdictWord(rec.verdict) + "." +
                    if (rec.posterId.isNotEmpty())
                        " The posed finale is in your album."
                    else ""
            }
            return null
        }
        return when {
            today < s.announceDay ->
                "🎭 A space on the board has been cleared " +
                    "and labeled in ${DIRECTOR}'s handwriting: " +
                    "“WATCH THIS SPACE.” Underneath, smaller: " +
                    "“the goat does not get a vote this year.”"
            today < s.auditionDay ->
                "🎭 POSTER: “$TITLE” — this year's school " +
                    "play, directed by Principal $DIRECTOR. " +
                    "$PREMISE Auditions " +
                    "${dateLabel(s.auditionDay)}, 3 PM, this " +
                    "classroom. Opening night " +
                    "${dateLabel(s.openingDay)}."
            today == s.auditionDay && rec == null ->
                "🎭 AUDITIONS TODAY, 3 PM, this classroom — " +
                    "“$TITLE.” The board has a sign-up sheet " +
                    "and a pencil on a string. The pencil " +
                    "has seen things."
            rec != null && today in s.rehearsalDays &&
                state.timeMin in REH_FROM until REH_TO ->
                "🎭 From the gym: rehearsal — " +
                    "${sessionTheme(sessionToday(state))}. " +
                    "Somebody is being a mountain in there, " +
                    "professionally."
            rec != null && nightCanHappen(state) &&
                today >= s.openingDay ->
                "🎭 TONIGHT: opening night of “$TITLE” — " +
                    "house at 5:30, folding chairs, one " +
                    "borrowed spotlight. The cast list is " +
                    "on the board; the goat is unlisted, " +
                    "as is tradition."
            rec != null ->
                "🎭 The cast list for “$TITLE” is pinned " +
                    "up: " + ROLES.joinToString("; ") { r ->
                        "${r.title.lowercase()
                            .replaceFirstChar { it.uppercase() }}" +
                            " — ${actorOf(rec, r.id)}" } +
                    ". Opening night ${dateLabel(s.openingDay)}."
            else ->
                "🎭 “$TITLE”: auditions were " +
                    "${dateLabel(s.auditionDay)}. The cast " +
                    "list goes up on the board by morning."
        }
    }

    // ---------------- the Journal page ----------------

    fun trackSummary(rec: PlayRecord): String =
        when (rec.track) {
            "cast" -> "you played " +
                (rec.playerRole()?.title ?: "a named part")
            "ens" -> "you were ensemble"
            "under" -> "you were the understudy"
            "crew" -> "you crewed (${dutyLabel(rec.detail)})"
            "aud" -> "you were in the audience"
            else -> "you weren't in this one"
        }

    /** The 🎭 SCHOOL PLAY page: the current production
     *  whole while it's live (your part, your rehearsal
     *  record, the verdict, the review quoted), and past
     *  productions reduced to history lines. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val year = currentYear(state)
        val s = PlayCalendar.scheduleFor(year)
        val rec = record(state, year)
        out += "“$TITLE” — South Park Elementary's " +
            "production, school year $year"
        if (rec == null) {
            out += "Directed by Principal $DIRECTOR, who " +
                "casts it from the town's own kids — nerve, " +
                "line-handling and how they actually work " +
                "with her. $PREMISE"
            out += "📋 The poster goes up " +
                "${dateLabel(s.announceDay)}; auditions " +
                "${dateLabel(s.auditionDay)} (3 PM, the " +
                "classroom); opening night " +
                "${dateLabel(s.openingDay)}."
        } else if (rec.night.isEmpty()) {
            out += "Directed by Principal $DIRECTOR. $PREMISE"
            out += "🎭 THE CAST:"
            for (r in ROLES) {
                out += "• ${r.title} — ${actorOf(rec, r.id)}"
            }
            if (rec.ensemble.isNotEmpty())
                out += "• Ensemble — " +
                    rec.ensemble.joinToString(", ")
            out += when (rec.track) {
                "cast" -> "Your part: " +
                    (rec.playerRole()?.title ?: "") +
                    " (delivery: ${rec.delivery})."
                "ens" -> "Your part: ensemble — tree, " +
                    "wind, second goat."
                "under" -> "Your part: understudy — every " +
                    "line, learned twice."
                "crew" -> "Your part: crew — " +
                    dutyLabel(rec.detail) + "."
                "aud" -> "Your part: the audience — a good " +
                    "seat, opening night."
                else -> "Your part: none — you can still " +
                    "be there opening night."
            }
            val attended = Integer.bitCount(rec.attMask)
            if (rec.track in listOf("cast", "ens", "under"))
                out += "Your rehearsals: $attended of " +
                    "$REHEARSALS attended."
            else if (rec.track == "crew")
                out += "Your rehearsals: $attended of " +
                    "$REHEARSALS attended (sets built: " +
                    "$attended)."
            out += "🎭 Opening night: " +
                "${dateLabel(s.openingDay)}, house 5:30 PM " +
                "at the Elementary. It slips a night or " +
                "two if a bigger town moment owns the " +
                "evening — the show understands."
        } else {
            out += "🎭 VERDICT: ${verdictWord(rec.verdict)}" +
                if (rec.night == "played")
                    " — and you were there."
                else
                    " — the show went on without you in " +
                        "the room; the record is the " +
                        "cast's own."
            out += "The cast: " + ROLES.joinToString("; ") {
                r -> "${r.title.lowercase()
                    .replaceFirstChar { it.uppercase() }}: " +
                    actorOf(rec, r.id) } + "."
            out += "Your part: ${trackSummary(rec)}."
            val attended = Integer.bitCount(rec.attMask)
            if (rec.track in listOf("cast", "ens", "under",
                    "crew"))
                out += "Your rehearsal record: $attended " +
                    "of $REHEARSALS sessions."
            if (rec.posterId.isNotEmpty())
                out += "📸 The posed finale is in your " +
                    "album (opening night, at the school) " +
                    "— the darkroom can print it as the " +
                    "playbill."
            if (rec.review.isNotEmpty())
                out += "📰 THE REVIEW — ${rec.review}"
        }
        // history: elder productions, one line each
        val elder = allRecords(state)
            .filter { it.year != year && it.night.isNotEmpty() }
            .sortedByDescending { it.year }.take(3)
        for (old in elder) {
            out += "📜 ${old.year}: “$TITLE” — " +
                verdictWord(old.verdict) + "; " +
                "${actorOf(old, "mountain")} was the " +
                "Mountain; ${trackSummary(old)}."
        }
        return out
    }

    // ---------------- the phone ----------------

    /** The play's derived phone beats, in the harvest's
     *  fact discipline: the poster, the cast list, opening
     *  night, the review — one text per fact, from the
     *  person who'd send it, the morning it becomes true.
     *  Returns (sender, kind, text). */
    fun phoneBeat(state: GameState):
        Triple<String, String, String>? {
        if (!cycleLive(state)) return null
        val year = currentYear(state)
        val s = PlayCalendar.scheduleFor(year)
        val rec = record(state, year)
        val idx = year % 2
        if (state.absDay == s.announceDay) {
            val text = listOf(
                "production news, official channel: this " +
                    "year's school play is “$TITLE”. " +
                    "auditions ${dateLabel(s.auditionDay)}, " +
                    "opening ${dateLabel(s.openingDay)}. " +
                    "directed by me. the goat does not get " +
                    "a vote this year — tell the others",
                "the poster is up: “$TITLE”, this year's " +
                    "play. i direct. auditions " +
                    "${dateLabel(s.auditionDay)} after " +
                    "class, opening night " +
                    "${dateLabel(s.openingDay)}. every " +
                    "fourth-grader is welcome at auditions. " +
                    "bravery is the only prerequisite"
                )[idx]
            return Triple(DIRECTOR, "announce", text)
        }
        if (rec != null &&
            state.life["playCastDay:$year"] == state.absDay) {
            val lead = actorOf(rec, "mountain")
            return when {
                rec.track == "cast" -> Triple(DIRECTOR,
                    "cast",
                    "the list is posted. you are " +
                        (rec.playerRole()?.title ?: "cast") +
                        ". rehearsals start tomorrow, 3 pm. " +
                        "the mountain provides its own " +
                        "costume. — v")
                rec.track == "ens" -> Triple(DIRECTOR,
                    "cast",
                    "the list is posted: ENSEMBLE, you. " +
                        "tree, wind, second goat — the " +
                        "show's skeleton. rehearsals " +
                        "tomorrow, 3 pm. — v")
                rec.track == "under" -> Triple(DIRECTOR,
                    "cast",
                    "the list is posted: UNDERSTUDY, you. " +
                        "learn every line. if opening " +
                        "night so much as sneezes, the " +
                        "show is yours. rehearsals " +
                        "tomorrow, 3 pm. — v")
                rec.track == "crew" -> Triple(DIRECTOR,
                    "cast",
                    "the list is posted. the cast is set " +
                        "— and the crew line has your name " +
                        "on it (${dutyLabel(rec.detail)}). " +
                        "rehearsals tomorrow, 3 pm. — v")
                lead != "You" && lead != "—" ->
                    Triple(lead, "cast",
                        "CAST LIST IS UP. i am THE MOUNTAIN " +
                            "in “$TITLE”. three lines. i have " +
                            "already memorized them and one " +
                            "of the goat's stage directions. " +
                            "opening night " +
                            "${dateLabel(s.openingDay)}. be there")
                else -> null
            }
        }
        if (state.absDay == s.openingDay && rec != null &&
            rec.night.isEmpty()) {
            return Triple(DIRECTOR, "opening",
                "tonight. “$TITLE”, house 5:30, this " +
                    "school. the cast is ready in the way " +
                    "casts are: terrified, memorized, " +
                    "fed. come and be a room for them. — v")
        }
        if (rec != null && rec.review.isNotEmpty() &&
            state.life["playReviewDay:$year"] ==
                state.absDay) {
            return Triple(rec.critic, "review",
                "my review is in the school paper: " +
                    "“$TITLE” — " +
                    verdictWord(rec.verdict) + ". the " +
                    "journal has the whole thing (🎭 " +
                    "SCHOOL PLAY). i stand by every word, " +
                    "including the ones about the goat")
        }
        return null
    }
}

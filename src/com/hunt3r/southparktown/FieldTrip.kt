package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// FIELD TRIPS (v0.14.0b, Queue #14 item 2) — three Fridays a
// school year, the fourth grade does not meet in the
// classroom. It meets on a bus.
//
// The model, in order:
//   - THE ANCHORS are pure calendar (TripCalendar): the
//     FALL trip on the second Friday of October (Stark's
//     Pond, nature study), the WINTER trip on the first
//     Friday of February (the Park County Historical Room
//     — the town museum, kept inside City Hall), the
//     SPRING trip on the third Friday of April (Tegridy
//     Farms, agriculture day). Announced seven days ahead
//     on the classroom board and the morning ticker. The
//     three Fridays are proven disjoint from the dance
//     (2nd Fri Feb), the talent show (2nd Sat Apr), the
//     play's whole schedule, the fair and carnival weeks,
//     the parade, every finale window and every holiday
//     by a 2026–2100 sweep in ONE lived calendar space in
//     the suite — so no trip ever yields on the calendar.
//     A live case climax, a finale window or a holiday
//     beat reaching the day at runtime yields it whole,
//     in Dance's guarded pattern: the reason is stamped
//     where it is first learned and printed on the
//     poster, the page and the wrap.
//   - THE ROSTER is DERIVED, never typed: the class is
//     CharacterSystem.CAST_KIDS in the KID age band (the
//     band table is the derivation — the three young
//     teens are not in Garrison's class and do not ride),
//     and the chaperone is School.teacherFor, the same
//     rotation that teaches (Garrison; Victoria on
//     Wednesdays — trips are Fridays, so the clipboard is
//     Garrison's, and the suite pins that too).
//   - THE DAY BENDS: on a trip day the school stands
//     quiet (School.QUIET_TRIP — lessons, recess and the
//     bee all stand down, the board says why), and an
//     agenda claim seated after the parade's and before
//     the sleepovers' holds the whole roster at the site
//     from first bell (9:00) to the buses back (13:00).
//     The player rides by BOARDING at the classroom
//     board in the boarding window (9:00–9:30): the bus
//     ride is real clock (30 minutes) and lands you at
//     the site. Never boarding is a first-class outcome —
//     a quiet study day at an empty school, no credit and
//     no punishment, and the record says "missed" the way
//     the dance's record says it.
//   - AT THE SITE: free time with the classmates actually
//     there (the claim put them there; talk and Look work
//     as ever), ONE played beat run by the chaperone from
//     her tap-person menu, in the lesson's choice idiom —
//     the pond's SPECIMEN SCAN (three rounds; the intro
//     teaches the signs, the rounds test them), the
//     museum's DOCENT QUIZ (three questions off the
//     museum's own cards — two of them counted live off
//     the game's own registries), the farm's HARVEST
//     HELP (three rows; the intro teaches the reads, the
//     rows test them) — and exactly ONE class photograph
//     through Photos.composeGroup, from the classmates
//     physically at the site. The bus back rides from
//     12:30 (the second run is Crabtree's mercy, till
//     15:00); walking off is allowed and the record stays
//     honest about the day either way.
//   - FRIENDSHIP, the dance/talent discipline: the beat
//     pays only when played through, at the site — a +1
//     sprinkle to the classmates actually present (cap
//     8), +1 more to the chaperone when the work was
//     sharp (score ≥ 2 of 3). Nothing pays for merely
//     existing, and nothing anywhere else pays at all.
//   - THE RECORDS: a Journal 🚌 FIELD TRIPS page, ONE
//     TOWN NEWS line in the trip week's edition (section
//     copy — the headline ladder stays ten), and ONE
//     morning-after phone text through the harvest under
//     a FIELDTRIP trigger (fact-only; the sender set
//     stays closed). TalkGate keeps its eleven rails —
//     the trip adds none; its business rides the board,
//     the chaperone's menu and the morning.
//   - STATE: everything rides life/lifeText under
//     "trip…" keys — zero new save keys, so pre-0.14.0b
//     saves load untouched and join the next trip. The
//     day's perishables (riding, beat steps, picks, the
//     photo stamp) are day-keyed and pruned at rollover;
//     a mid-trip save/reload keeps them whole (pinned).
// ============================================================

/** The trips' pure calendar: a function of the Gregorian
 *  calendar alone (the DanceCalendar pattern). */
object TripCalendar {

    /** The nth [dow] (0 = Sunday .. 6 = Saturday) of
     *  [month] in [year], as an ordinal. */
    fun nthWeekdayOrdinal(year: Int, month: Int,
                          dow: Int, n: Int): Int {
        val first = ordinalOfKey("%04d%02d01".format(year, month))
        val toDow = (dow - FairCalendar.dowOfOrdinal(first) + 7) % 7
        return first + toDow + (n - 1) * 7
    }

    /** The fall trip: the second Friday of October. */
    fun fallOrdinal(year: Int): Int =
        nthWeekdayOrdinal(year, 10, 5, 2)

    /** The winter trip: the first Friday of February. */
    fun winterOrdinal(year: Int): Int =
        nthWeekdayOrdinal(year, 2, 5, 1)

    /** The spring trip: the third Friday of April. */
    fun springOrdinal(year: Int): Int =
        nthWeekdayOrdinal(year, 4, 5, 3)

    fun ordinalFor(season: String, year: Int): Int = when (season) {
        "fall" -> fallOrdinal(year)
        "winter" -> winterOrdinal(year)
        else -> springOrdinal(year)
    }

    fun dayFor(season: String, year: Int): Int =
        FairCalendar.absDayOfOrdinal(ordinalFor(season, year))

    /** The poster goes up a week before the trip. */
    fun announceDay(season: String, year: Int): Int =
        dayFor(season, year) - FieldTrip.ANN_BACK
}

object FieldTrip {

    // ---------------- the dials (all suite-pinned) --------

    const val SCHOOL = "Elementary"
    const val TOPIC = "field trip"

    /** The poster goes up this many days before the trip. */
    const val ANN_BACK = 7

    /** Boarding at the classroom board: first bell to the
     *  half hour. Crabtree does not wait past it. */
    const val BOARD_FROM = 9 * 60
    const val BOARD_TO = 9 * 60 + 30

    /** The bus ride, in real clock: boarding costs it,
     *  and the ride back costs it again. */
    const val RIDE_MINUTES = 30

    /** The claim's window: the class is at the site from
     *  first bell until the buses back at one. */
    const val CLAIM_FROM = 9 * 60
    const val CLAIM_TO = 13 * 60

    /** Site business (the beat, the photograph) runs
     *  while the class is at the site. */
    const val SITE_TO = CLAIM_TO

    /** The bus back: the first run loads at one with the
     *  class; a second run — Crabtree's mercy — takes a
     *  straggler until three. */
    const val BACK_FROM = 12 * 60 + 30
    const val BACK_TO = 15 * 60

    /** The played beat: three rounds at every site. */
    const val ROUNDS = 3

    /** A sharp day's score: at least this many of the
     *  three rounds right earns the chaperone's notice. */
    const val GOOD_SCORE = 2

    /** The friendship the finished beat sprinkles on each
     *  classmate actually at the site — small, once, and
     *  only for a played-through trip. */
    const val BEAT_PAY = 1
    const val SPRINKLE_CAP = 8

    /** ... and the chaperone's own bump for sharp work. */
    const val CHAPERONE_PAY = 1

    /** Journal history: past trips kept as lines. */
    const val HISTORY_CAP = 12

    /** The wrapped record stays on the board and the
     *  page's front for this many days after the trip. */
    const val TAIL_DAYS = 10

    const val OUT_WENT = "went"
    const val OUT_MISSED = "missed"
    const val OUT_YIELDED = "yielded"

    // ---------------- the sites ----------------

    data class Site(
        val id: String, val season: String, val locId: String,
        val name: String, val short: String, val emoji: String,
        val activity: String, val beatName: String
    )

    /** The rotation, in its published order: the pond in
     *  the fall, the museum in the winter (indoors, for
     *  obvious reasons), the farm in the spring. */
    val SITES: List<Site> = listOf(
        Site("pond", "fall", "StarksPond",
            "Stark's Pond — Nature Study", "Stark's Pond", "🌊",
            "Field trip — nature study at the pond, " +
                "clipboard and all.",
            "Specimen Scan"),
        Site("museum", "winter", "CityHall",
            "The Park County Historical Room", "the Historical Room",
            "🏛️",
            "Field trip — the historical room at City " +
                "Hall, whispering (mostly).",
            "Docent Quiz"),
        Site("farm", "spring", "TegridyFarms",
            "Tegridy Farms — Agriculture Day", "Tegridy Farms", "🌾",
            "Field trip — agriculture day, helping " +
                "with the rows.",
            "Harvest Help")
    )

    val SEASONS: List<String> = listOf("fall", "winter", "spring")

    fun siteFor(season: String): Site =
        SITES.first { it.season == season }

    fun seasonName(season: String): String = when (season) {
        "fall" -> "Fall"
        "winter" -> "Winter"
        else -> "Spring"
    }

    // ---------------- the instance ----------------

    data class Instance(val season: String, val year: Int) {
        val ordinal: Int get() = TripCalendar.ordinalFor(season, year)
        val day: Int get() = TripCalendar.dayFor(season, year)
        val announce: Int get() = day - ANN_BACK
        val key: String get() = "$season:$year"
        val site: Site get() = siteFor(season)
    }

    /** Every instance whose year is anywhere near
     *  [absDay], in date order. */
    fun instancesAround(absDay: Int): List<Instance> {
        val key = keyFromOrdinal(FairCalendar.ordinalOfAbsDay(absDay))
        val y = key.substring(0, 4).toIntOrNull() ?: 2026
        val out = mutableListOf<Instance>()
        for (yy in (y - 1)..(y + 1)) for (s in SEASONS)
            out += Instance(s, yy)
        return out.sortedBy { it.day }
    }

    /** The instance the town is living: the first whose
     *  tail hasn't closed (a wrapped trip stays current
     *  for TAIL_DAYS), else the next one coming. */
    fun currentInstance(state: GameState): Instance {
        val all = instancesAround(state.absDay)
        return all.firstOrNull {
            it.day + TAIL_DAYS >= state.absDay
        } ?: all.last()
    }

    /** Pure calendar: is today a trip day? (School's quiet
     *  day reads this — it must never consult outcomes.) */
    fun isTripDay(state: GameState): Boolean =
        instancesAround(state.absDay).any { it.day == state.absDay }

    // ---------------- the roster (derived) ----------------

    /** The class, derived from the cast tables on every
     *  read: every cast kid in the KID age band. The band
     *  table is the whole derivation — no typed list here
     *  or anywhere. */
    fun roster(): List<String> =
        CharacterSystem.CAST_KIDS
            .filter { CharacterSystem.CAST_BANDS[it] == AgeBand.KID }
            .sorted()

    /** The chaperone: the teacher rotation's own answer
     *  for the day (School owns the rotation). */
    fun chaperone(absDay: Int): String = School.teacherFor(absDay)

    /** Classmates physically at [locId] right now — cast
     *  standing there on the player's side of the door,
     *  the School.presentAt truthfulness. */
    fun presentAt(state: GameState, locId: String): List<String> =
        state.agents.values
            .filter {
                !it.isPlayer && it.location == locId &&
                    it.waypoints.isEmpty() && !it.asleep &&
                    CharacterSystem.npcInside(it.location) ==
                        state.playerInside &&
                    it.name in roster()
            }
            .map { it.name }
            .sorted()

    // ---------------- the record ----------------

    data class TripRecord(
        val key: String,
        val season: String = "",
        val year: Int = 0,
        val day: Int = -1,
        val outcome: String = "",     // "" | went | missed | yielded
        val yieldWhy: String = "",    // case | finale | holiday
        val beatResult: String = "",  // "" | sharp | steady | easy
        val score: Int = -1,
        val photoId: String = ""
    )

    private fun recKey(key: String) = "tripRec:$key"

    fun record(state: GameState, key: String): TripRecord? {
        val raw = state.lifeText[recKey(key)] ?: return null
        val f = raw.split("|")
        if (f.size < 8) return null
        return TripRecord(
            key = key,
            season = f.getOrNull(1) ?: "",
            year = f.getOrNull(2)?.toIntOrNull() ?: 0,
            day = f.getOrNull(3)?.toIntOrNull() ?: -1,
            outcome = f.getOrNull(4) ?: "",
            yieldWhy = f.getOrNull(5) ?: "",
            beatResult = f.getOrNull(6) ?: "",
            score = f.getOrNull(7)?.toIntOrNull() ?: -1,
            photoId = f.getOrNull(8) ?: "")
    }

    private fun saveRecord(state: GameState, rec: TripRecord) {
        state.lifeText[recKey(rec.key)] = listOf(
            "1", rec.season, rec.year.toString(),
            rec.day.toString(), rec.outcome, rec.yieldWhy,
            rec.beatResult, rec.score.toString(), rec.photoId
        ).joinToString("|")
    }

    private fun recordOrNew(state: GameState,
                            inst: Instance): TripRecord =
        record(state, inst.key) ?: TripRecord(
            key = inst.key, season = inst.season,
            year = inst.year, day = inst.day)

    fun allRecords(state: GameState): List<TripRecord> =
        state.lifeText.keys
            .filter { it.startsWith("tripRec:") }
            .mapNotNull { record(state, it.removePrefix("tripRec:")) }
            .sortedByDescending { it.day }

    // ---------------- yields ----------------

    /** Why today's trip does not run, or null when the
     *  day is free: the standing owners, read through the
     *  town's own definitions (School's quiet day minus
     *  the trip itself). The CALENDAR never yields a trip
     *  — the sweep keeps the three Fridays clear of every
     *  dated owner — so only a live owner can. */
    fun yieldReason(state: GameState): String? {
        if (!isTripDay(state)) return null
        return when (School.quietReason(state)) {
            School.QUIET_CASE -> "case"
            School.QUIET_FINALE -> "finale"
            School.QUIET_HOLIDAY -> "holiday"
            else -> null
        }
    }

    /** The yield reasons in the town's words — the
     *  poster, the page and the wrap all quote the same
     *  sentence. */
    fun yieldText(why: String): String = when (why) {
        "case" -> "there's case business in the air, and " +
            "even a field trip steps around it"
        "finale" -> "it's finale week — the whole town " +
            "is holding its breath, buses included"
        "holiday" -> "the holiday has the day, and the " +
            "bus keeps with the day"
        else -> "the town had bigger business that day"
    }

    /** The trip runs today: it's the day, and no owner
     *  holds it. The first consult on the day that finds
     *  an owner stamps the reason (the Dance pattern: the
     *  fact is recorded where it's learned) so the
     *  morning wrap can say why. */
    fun tripLive(state: GameState): Boolean {
        val inst = currentInstance(state)
        if (state.absDay != inst.day) return false
        val rec = record(state, inst.key)
        if (rec != null && rec.outcome.isNotEmpty()) return false
        val why = yieldReason(state)
        if (why != null) {
            if (state.lifeText["tripYield:${inst.key}"] == null)
                state.lifeText["tripYield:${inst.key}"] = why
            return false
        }
        return true
    }

    // ---------------- the agenda claim ----------------

    /** The trip's agenda claim, seated in Engine's stack
     *  after the parade's and before the sleepovers': on
     *  the one Friday, the site holds the whole class and
     *  its chaperone from first bell to the buses back.
     *  Self-guarded to live trip days — a yielded or
     *  wrapped day stands no claim at all. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in CLAIM_FROM until CLAIM_TO) return null
        if (!tripLive(state)) return null
        val site = currentInstance(state).site
        return when {
            npcName == chaperone(state.absDay) -> AgendaBlock(
                site.locId,
                "Chaperoning the field trip — a headcount " +
                    "at every door, a clipboard with opinions.")
            npcName in roster() -> AgendaBlock(
                site.locId, site.activity)
            else -> null
        }
    }

    // ---------------- small shared helpers ----------------

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40)
            state.events.removeAt(0)
        townLogLine(state, line)
    }

    /** "Fri, Oct 9"-style labels from the calendar math
     *  (the Dance helper's twin). */
    fun dateLabel(absDay: Int): String = Dance.dateLabel(absDay)

    private fun lcg(seed: Long): Long {
        var x = seed * 6364136223846793005L + 1442695040888963407L
        x = x xor (x ushr 33)
        return x and Long.MAX_VALUE
    }

    private fun daySeed(absDay: Int): Long =
        absDay * 1000003L + 4243L

    // ---------------- boarding ----------------

    /** You rode the bus today (the day stamp). */
    fun riding(state: GameState): Boolean =
        state.life["tripRode:${state.absDay}"] == 1

    /** Boarding is open: a live trip day, the boarding
     *  window, you not already on the bus. (The classroom
     *  board is the door — GameActivity's board dialog
     *  only exists inside the Elementary.) */
    fun boardOpen(state: GameState): Boolean =
        tripLive(state) && !riding(state) &&
            state.timeMin in BOARD_FROM until BOARD_TO

    fun boardLabel(state: GameState): String {
        val inst = currentInstance(state)
        return "🚌 Board the bus — field trip to " +
            "${inst.site.name} (departs 9:30)"
    }

    /** Board the bus: stamps the ride, moves you to the
     *  site on the real clock, and reads the arrival.
     *  Returns null when boarding isn't open. */
    fun boardBus(state: GameState): String? {
        if (!boardOpen(state)) return null
        val inst = currentInstance(state)
        val site = inst.site
        state.life["tripRode:${state.absDay}"] = 1
        saveRecord(state, recordOrNew(state, inst))
        Engine().playerGo(state, site.locId)
        state.timeMin = (state.timeMin + RIDE_MINUTES)
            .coerceAtMost(SITE_TO - RIDE_MINUTES)
        return "🚌 THE BUS — Crabtree counts heads twice, " +
            "loudly, and the bus does what buses in this " +
            "town do: it leaves exactly when it means to. " +
            "The ride is all window and somebody else's " +
            "snack smells.\n\n${site.emoji} ${site.name.uppercase()} — " +
            "the class spills out onto the ground with the " +
            "particular energy of kids who have been let " +
            "out of a building on a school day. " +
            "${chaperone(state.absDay)} has the clipboard. " +
            "The day is yours till the buses at one: look " +
            "around, talk to your class, and see " +
            "${chaperone(state.absDay)} about the " +
            "${site.beatName} when you're ready."
    }

    /** The ride back is on offer: you rode out, you're at
     *  the site, and a bus (the one, then the mercy run)
     *  is loading. */
    fun backOpen(state: GameState): Boolean {
        if (!riding(state)) return false
        if (state.life["tripBack:${state.absDay}"] == 1) return false
        if (state.timeMin !in BACK_FROM until BACK_TO) return false
        val me = state.agents.values.firstOrNull { it.isPlayer }
        return me?.location == currentInstance(state).site.locId &&
            state.absDay == currentInstance(state).day
    }

    /** Ride the bus back to the Elementary. */
    fun boardBack(state: GameState): String? {
        if (!backOpen(state)) return null
        state.life["tripBack:${state.absDay}"] = 1
        Engine().playerGo(state, SCHOOL)
        state.timeMin = (state.timeMin + RIDE_MINUTES)
            .coerceAtMost(15 * 60 + 30)
        return "🚌 THE RIDE BACK — the bus smells of pond " +
            "water, museum dust and farm, in proportions " +
            "nobody will admit to. Crabtree counts heads " +
            "twice, loudly. Everyone is accounted for. " +
            "School lets out into an ordinary afternoon, " +
            "which feels, briefly, like a rumor."
    }

    // ---------------- the played beat ----------------

    data class TripBeat(
        val kind: String, // "text" | "prompt"
        val speaker: String, val text: String)

    private fun beatStep(state: GameState): Int =
        state.life["tripStep:${state.absDay}"] ?: -1

    fun beatDone(state: GameState): Boolean =
        state.life["tripBeatDone:${state.absDay}"] == 1

    /** The beat can be offered: you rode, you're at the
     *  site while the class is, and it hasn't been played
     *  through. */
    fun beatOpen(state: GameState): Boolean {
        if (!riding(state) || beatDone(state)) return false
        if (state.timeMin >= SITE_TO) return false
        val inst = currentInstance(state)
        if (state.absDay != inst.day) return false
        val me = state.agents.values.firstOrNull { it.isPlayer }
        return me?.location == inst.site.locId
    }

    /** Start the beat (resume-safe: an open beat just
     *  continues). */
    fun startBeat(state: GameState): Boolean {
        if (beatStep(state) in 0..ROUNDS + 1) return true
        if (!beatOpen(state)) return false
        state.life["tripStep:${state.absDay}"] = 0
        state.lifeText["tripPicks:${state.absDay}"] = ""
        return true
    }

    /** The beat's current face, or null when no beat is
     *  open. Steps: 0 the intro (which teaches the game),
     *  1..ROUNDS the rounds, ROUNDS+1 the outcome. */
    fun beatBeat(state: GameState): TripBeat? {
        val step = beatStep(state)
        if (step < 0) return null
        val inst = currentInstance(state)
        val site = inst.site
        val chap = chaperone(state.absDay)
        return when (step) {
            0 -> TripBeat("text", chap, introText(state, site))
            in 1..ROUNDS -> TripBeat("prompt", chap,
                promptText(state, site, step))
            else -> TripBeat("text", chap,
                outcomeText(state, site))
        }
    }

    /** The current round's options (id to label), rotated
     *  by day and round so the right seat moves. */
    fun beatOptions(state: GameState): List<Pair<String, String>> {
        val step = beatStep(state)
        if (step !in 1..ROUNDS) return emptyList()
        val site = currentInstance(state).site
        val all = optionsFor(state, site, step)
        val rot = (state.absDay + step) % all.size
        return all.drop(rot) + all.take(rot)
    }

    /** Judge one answer and move on; the third answer
     *  judges the whole beat. */
    fun answerBeat(state: GameState, choiceId: String): Boolean {
        val step = beatStep(state)
        if (step !in 1..ROUNDS) return false
        val site = currentInstance(state).site
        val right = choiceId == correctChoice(state, site, step)
        val picks = (state.lifeText["tripPicks:${state.absDay}"]
            ?: "") + if (right) "1" else "0"
        state.lifeText["tripPicks:${state.absDay}"] = picks
        state.life["tripStep:${state.absDay}"] = step + 1
        if (step == ROUNDS) judgeBeat(state, picks)
        return true
    }

    /** Advance a text step (the intro, the outcome). */
    fun advanceBeat(state: GameState): Boolean {
        val step = beatStep(state)
        if (step < 0) return false
        if (step == 0) {
            state.life["tripStep:${state.absDay}"] = 1
            return true
        }
        if (step == ROUNDS + 1) {
            state.life.remove("tripStep:${state.absDay}")
            return true
        }
        return false
    }

    /** Settle the played beat: the record takes the
     *  result, the classmates actually at the site take
     *  the sprinkle, the chaperone takes hers on sharp
     *  work, and the thread is planted. Exactly once —
     *  the done stamp is the gate. */
    private fun judgeBeat(state: GameState, picks: String) {
        if (beatDone(state)) return
        state.life["tripBeatDone:${state.absDay}"] = 1
        val inst = currentInstance(state)
        val score = picks.count { it == '1' }
        val result = when {
            score >= ROUNDS -> "sharp"
            score >= GOOD_SCORE -> "steady"
            else -> "easy"
        }
        val rec = recordOrNew(state, inst)
        saveRecord(state, rec.copy(beatResult = result,
            score = score))
        val eng = Engine()
        val here = presentAt(state, inst.site.locId)
        for (kid in here.take(SPRINKLE_CAP)) {
            eng.relAdjust(state, kid, BEAT_PAY)
            plantTripTopic(kid)
        }
        if (score >= GOOD_SCORE) {
            eng.relAdjust(state, chaperone(state.absDay),
                CHAPERONE_PAY)
            plantTripTopic(chaperone(state.absDay))
        }
    }

    private fun plantTripTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    // ---- the beat's content (per site) ----

    /** The pond's signs, in their fixed teaching order:
     *  the sign the water makes, and the spot it means.
     *  The intro teaches all three; each round shows one
     *  and the right spot follows from the lesson. */
    val POND_SIGNS: List<Pair<String, String>> = listOf(
        "slow rings keep spreading out from one spot, " +
            "like the water is thinking" to "shallows",
        "small splashes keep popping against the wood, " +
            "one after another" to "dock",
        "the surface by the grass is flat as glass — " +
            "and something just moved under it" to "reeds")

    val POND_SPOTS: List<Pair<String, String>> = listOf(
        "shallows" to "🔍 Scan the shallows",
        "dock" to "🔍 Scan the dock posts",
        "reeds" to "🔍 Scan the reeds")

    /** The museum's authored cards (the count questions
     *  are built live in optionsFor/correctChoice from
     *  the game's own registries — a museum that counts
     *  the town should count it right). */
    data class QuizCard(val question: String,
                        val right: String,
                        val wrongA: String,
                        val wrongB: String)

    val QUIZ_CARDS: List<QuizCard> = listOf(
        QuizCard("The Founder's Bell hangs over which " +
            "doors, according to its card?",
            "The school gym doors",
            "The mayor's garage",
            "The Bijou's stage door"),
        QuizCard("The Map Table's red pin marks what?",
            "Where the town's first survey stake " +
                "was driven",
            "The mayor's parking spot",
            "Where the bell was found"),
        QuizCard("The First Snow Shovel stands in a " +
            "glass case by the door. Why?",
            "Three grades signed a petition to " +
                "retire it with honors",
            "It is too sharp to touch",
            "Nobody remembers, and the case was " +
                "already there"),
        QuizCard("The museum keeps one shelf empty, " +
            "with a card that just says SOON. " +
            "What's it for?",
            "Next year's class — history isn't " +
                "finished yet",
            "The mayor's second hat",
            "Snacks, in an emergency"))

    /** The farm's row reads, in their fixed teaching
     *  order: what the row shows, and the work it wants. */
    val FARM_READS: List<Pair<String, String>> = listOf(
        "the earth is cracked pale and the leaves are " +
            "curling in on themselves" to "water",
        "the heads are full and nodding heavy, as " +
            "ready as a row gets" to "pick",
        "a green tangle is strangling the line, " +
            "weeds shoulder to shoulder" to "clear")

    val FARM_WORKS: List<Pair<String, String>> = listOf(
        "water" to "💧 Water the row",
        "pick" to "🧺 Pick the row",
        "clear" to "✂️ Clear the row")

    /** Which quiz cards this trip asks, by round: 1 is
     *  the town-model count (-1), 2 is a seeded authored
     *  card, 3 is the portrait-wall count (-2). The two
     *  count cards always ride — they're the museum's
     *  pride, and their answers are counted live off the
     *  game's own registries when the options are built. */
    fun quizFor(absDay: Int): List<Int> {
        val pick = (lcg(daySeed(absDay) + 11) %
            QUIZ_CARDS.size).toInt()
        return listOf(-1, pick, -2)
    }

    /** The round's sign/read index, seeded by the day —
     *  a rotation through the three teachings, never the
     *  same twice in a day. */
    fun roundIndex(absDay: Int, step: Int): Int {
        val base = (lcg(daySeed(absDay) + 7) % 3).toInt()
        return (base + step - 1) % 3
    }

    /** The options for one round at one site. */
    fun optionsFor(state: GameState, site: Site,
                   step: Int): List<Pair<String, String>> =
        when (site.id) {
            "pond" -> POND_SPOTS
            "farm" -> FARM_WORKS
            else -> {
                val card = quizFor(state.absDay)[step - 1]
                when (card) {
                    -1 -> {
                        val n = LOCATIONS.size
                        listOf("right" to "🏛️ $n buildings",
                            "wa" to "🏛️ ${n + 3} buildings",
                            "wb" to "🏛️ ${n - 4} buildings")
                    }
                    -2 -> {
                        val n = CharacterSystem.CAST_KIDS.size +
                            CharacterSystem.CAST_ADULTS.size
                        listOf("right" to "🏛️ $n faces",
                            "wa" to "🏛️ ${n + 4} faces",
                            "wb" to "🏛️ ${n - 6} faces")
                    }
                    else -> {
                        val q = QUIZ_CARDS[card]
                        listOf("right" to "🏛️ ${q.right}",
                            "wa" to "🏛️ ${q.wrongA}",
                            "wb" to "🏛️ ${q.wrongB}")
                    }
                }
            }
        }

    /** The right answer for one round — a pure function
     *  of the day and the round, recomputed on every
     *  read (the suite recomputes it independently). */
    fun correctChoice(state: GameState, site: Site,
                      step: Int): String = when (site.id) {
        "pond" -> POND_SIGNS[roundIndex(state.absDay, step)].second
        "farm" -> FARM_READS[roundIndex(state.absDay, step)].second
        else -> "right"
    }

    private fun introText(state: GameState, site: Site): String {
        val chap = chaperone(state.absDay)
        return when (site.id) {
            "pond" -> "🔍 THE SPECIMEN SCAN — $chap reads " +
                "the pond rules off a card so old it has " +
                "its own specimen jar:\n\n" +
                "“Three signs, three spots. RINGS spreading " +
                "slow mean the SHALLOWS. SPLASHES against " +
                "the wood mean the DOCK POSTS. Flat GLASS " +
                "by the grass means the REEDS. Watch the " +
                "water, call the spot. Three rounds. The " +
                "pond is not graded, but it is watching " +
                "back.”"
            "museum" -> "🏛️ THE DOCENT'S QUIZ — the " +
                "docent is out with a cold, so $chap has " +
                "the question cards and the expression of " +
                "a person reading a will:\n\n" +
                "“Three questions, off the museum's own " +
                "cards. The exhibits counted themselves — " +
                "the model town downstairs has one building " +
                "for every place in town, and the portrait " +
                "wall has one face for every resident the " +
                "roll knows. The rest is on the cards. " +
                "Answer like you read the plaques, because " +
                "you're about to.”"
            else -> "🌾 HARVEST HELP — $chap reads the " +
                "farm card the way other people read " +
                "weather:\n\n" +
                "“Three rows, three reads. CRACKED earth " +
                "and curling leaves want WATER. Full heads " +
                "nodding heavy want PICKING. A green tangle " +
                "strangling the line wants CLEARING. Read " +
                "the row, do the work. The farm can tell " +
                "the difference, and so can I.”"
        }
    }

    private fun promptText(state: GameState, site: Site,
                           step: Int): String {
        val idx = roundIndex(state.absDay, step)
        return when (site.id) {
            "pond" -> "🔍 ROUND $step OF $ROUNDS — out on " +
                "the water: ${POND_SIGNS[idx].first}. " +
                "Where do you scan?"
            "museum" -> {
                val card = quizFor(state.absDay)[step - 1]
                "🏛️ QUESTION $step OF $ROUNDS — " +
                    when (card) {
                        -1 -> "The town model downstairs: " +
                            "one building for every place " +
                            "in town. How many buildings " +
                            "are on the model?"
                        -2 -> "The portrait wall: one face " +
                            "for every resident the roll " +
                            "knows. How many faces?"
                        else -> QUIZ_CARDS[card].question
                    }
            }
            else -> "🌾 ROW $step OF $ROUNDS — you walk " +
                "the line and read it: ${FARM_READS[idx].first}. " +
                "What's the work?"
        }
    }

    private fun outcomeText(state: GameState, site: Site): String {
        val inst = currentInstance(state)
        val rec = record(state, inst.key)
        val score = rec?.score ?: 0
        val chap = chaperone(state.absDay)
        val head = when (site.id) {
            "pond" -> "🔍 THE SCAN, READ BACK"
            "museum" -> "🏛️ THE QUIZ, READ BACK"
            else -> "🌾 THE ROWS, READ BACK"
        }
        val body = when {
            score >= ROUNDS -> when (site.id) {
                "pond" -> "Three for three — every sign " +
                    "read true, every spot called before " +
                    "the water finished making it. $chap " +
                    "writes something on the clipboard and " +
                    "underlines it twice."
                "museum" -> "Three for three — the cards " +
                    "go back in their box noticeably " +
                    "prouder. $chap informs the empty " +
                    "docent's chair that the class did fine."
                else -> "Three for three — every row read " +
                    "right and worked right. $chap looks " +
                    "at the rows the way teachers look at " +
                    "a test the class actually studied for."
            }
            score >= GOOD_SCORE -> when (site.id) {
                "pond" -> "Two of three — the pond kept " +
                    "one secret and gave up two, which is " +
                    "a better rate than most fishermen in " +
                    "this town manage. $chap nods, once."
                "museum" -> "Two of three — one card keeps " +
                    "its secret for the next class. $chap " +
                    "marks the card, not you, and moves on."
                else -> "Two of three — one row got the " +
                    "wrong kindness, and rows forgive " +
                    "slowly, but they forgive. $chap marks " +
                    "it down as weather."
            }
            else -> when (site.id) {
                "pond" -> "The pond kept its secrets " +
                    "today — signs misread, spots called " +
                    "late, water entirely unbothered. " +
                    "$chap writes 'the pond won' on the " +
                    "clipboard, which is, technically, a " +
                    "finding."
                "museum" -> "The cards keep their secrets " +
                    "today. $chap files the quiz under " +
                    "FIELD RESEARCH and tells the room " +
                    "the museum has beaten better classes " +
                    "than this one, which is almost a " +
                    "compliment."
                else -> "The rows got more enthusiasm " +
                    "than reading today. $chap looks at " +
                    "the clipboard for a while and writes, " +
                    "carefully, 'supervised'."
            }
        }
        return "$head — $body\n\n(The ${site.beatName} is " +
            "in the Journal — 🚌 FIELD TRIPS.)"
    }

    // ---------------- the photograph ----------------

    /** The trip's ONE photograph: the photo system's own
     *  group composition from the classmates physically
     *  at the site, captioned with the day. Returns the
     *  record id, or "" when there's no frame to take
     *  (nobody here, or the camera already fired). */
    fun takePhoto(state: GameState): String? {
        if (!riding(state)) return null
        val inst = currentInstance(state)
        if (state.absDay != inst.day) return null
        if (state.lifeText["tripPhoto:${state.absDay}"] != null)
            return null
        val present = presentAt(state, inst.site.locId)
        if (present.isEmpty()) return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
        val shot = Photos.composeGroup(state, present,
            emptyList(), me?.name ?: "New Kid",
            me?.appearance ?: emptyMap())
        val poster = shot.copy(caption =
            "🚌 ${inst.site.name} — ${dateLabel(state.absDay)}. " +
                "The whole class, out of the building on a " +
                "school day, which the photograph treats as " +
                "the historic event it is.")
        Photos.applyPhoto(state, poster)
        state.lifeText["tripPhoto:${state.absDay}"] = poster.id
        val rec = recordOrNew(state, inst)
        saveRecord(state, rec.copy(photoId = poster.id))
        return "📸 THE CLASS PHOTO — the class arranges " +
            "itself the way classes do (front row crouching, " +
            "back row on tiptoe, one kid mid-blink forever) " +
            "and the shutter takes ${inst.site.short} whole. " +
            "It's in your album."
    }

    // ---------------- the chaperone's menu ----------------

    const val OPT_BEAT_START = "🚌 Field trip: run the "
    const val OPT_BEAT_CONTINUE = "🚌 Field trip: finish the "
    const val OPT_PHOTO = "📸 Field trip: class photo"
    const val OPT_BACK = "🚌 Field trip: ride the bus back"

    /** The trip's business on the chaperone's tap-person
     *  menu — the player's initiative, like the crew ask,
     *  never a rail. One option at a time, in the day's
     *  own order: the beat, the photograph, the bus. */
    fun menuOption(state: GameState, npcName: String): String? {
        if (npcName != chaperone(state.absDay)) return null
        val inst = currentInstance(state)
        if (state.absDay != inst.day || !riding(state)) return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
        if (me?.location != inst.site.locId) return null
        val step = beatStep(state)
        if (step in 0..ROUNDS + 1)
            return OPT_BEAT_CONTINUE + inst.site.beatName
        if (!beatDone(state) && state.timeMin < SITE_TO)
            return OPT_BEAT_START + inst.site.beatName
        if (state.lifeText["tripPhoto:${state.absDay}"] == null &&
            state.timeMin < SITE_TO)
            return OPT_PHOTO
        if (backOpen(state)) return OPT_BACK
        return null
    }

    // ---------------- look & board ----------------

    /** The trip's business for Look: the poster's states
     *  at the Elementary, the dressed site on the day. */
    fun lookText(state: GameState, locId: String): String? {
        val inst = currentInstance(state)
        val site = inst.site
        if (locId == site.locId) {
            if (state.absDay != inst.day) return null
            val rec = record(state, inst.key)
            if (rec != null && rec.outcome.isNotEmpty()) {
                return "${site.emoji} The buses are gone " +
                    "and ${site.short} has its quiet back. " +
                    "A clipboard page is stuck under a " +
                    "stone by the door, in case of questions."
            }
            if (!tripLive(state)) {
                return "${site.emoji} ${site.short} is " +
                    "having an ordinary day. No bus came. " +
                    "Nobody here knows why not, and the " +
                    "not-knowing is going around."
            }
            return when (site.id) {
                "pond" -> "🌊 The pond, on a school day: " +
                    "a bus parked at the edge of the lot, " +
                    "a class spread along the shore with " +
                    "clipboards, and the water doing its " +
                    "signs for anyone taught to read them. " +
                    "(The ${site.beatName} runs through " +
                    "${chaperone(state.absDay)} — tap them.)"
                "museum" -> "🏛️ The Historical Room, on a " +
                    "school day: the class is inside among " +
                    "the cases, whispering at museum volume " +
                    "(audible two rooms away), and a bus is " +
                    "parked outside with its door open. " +
                    "(The ${site.beatName} runs through " +
                    "${chaperone(state.absDay)} — tap them.)"
                else -> "🌾 The farm, on a school day: a " +
                    "bus by the fence, a class strung along " +
                    "the rows with more opinions about " +
                    "agriculture than any of them had " +
                    "yesterday, and the rows waiting to be " +
                    "read. (The ${site.beatName} runs " +
                    "through ${chaperone(state.absDay)} — " +
                    "tap them.)"
            }
        }
        if (locId != SCHOOL) return null
        val today = state.absDay
        val rec = record(state, inst.key)
        if (rec != null && rec.outcome.isNotEmpty()) {
            if (today - inst.day in 0..TAIL_DAYS) {
                return when (rec.outcome) {
                    OUT_YIELDED -> "🚌 The field trip poster " +
                        "is still up, with the apology " +
                        "stapled to it: no trip this " +
                        "${seasonName(inst.season).lowercase()} — " +
                        yieldText(rec.yieldWhy) + ". The " +
                        "bus stayed in the lot."
                    OUT_MISSED -> "🚌 The poster is down. " +
                        "The class went to ${site.name} " +
                        "without you — ${site.short} kept " +
                        "the day, and the Journal " +
                        "(🚌 FIELD TRIPS) keeps what there " +
                        "is to keep."
                    else -> "🚌 The poster is down; a bus " +
                        "ticket stub is taped to the board " +
                        "where the route was. The trip is " +
                        "in the Journal (🚌 FIELD TRIPS)" +
                        if (rec.photoId.isNotEmpty())
                            ", and its photograph is in " +
                                "your album."
                        else "."
                }
            }
            return null
        }
        return when {
            today < inst.announce ->
                "🚌 A rectangle of board has been measured " +
                    "and marked in ${chaperone(today)}'s " +
                    "handwriting: “TRIP — WALL.” Underneath: " +
                    "“permission slips are not optional. " +
                    "they are the whole form.”"
            today < inst.day ->
                "🚌 POSTER: FIELD TRIP — ${site.name}, " +
                    "${dateLabel(inst.day)}. The whole class, " +
                    "one bus, first bell. Be at the board by " +
                    "9:30 or the bus leaves with your seat " +
                    "empty and Crabtree's blessing on it. " +
                    "Slips home and back by Thursday."
            today == inst.day && tripLive(state) ->
                "🚌 TRIP DAY — the class is at ${site.name}. " +
                    if (riding(state))
                        "You rode out with them; the building " +
                            "is holding a quiet day without " +
                            "you, and managing."
                    else if (state.timeMin < BOARD_TO)
                        "The bus is loading NOW — board " +
                            "from this board before 9:30."
                    else
                        "The bus has gone. The building is " +
                            "open and quiet; the day is a " +
                            "study day for anyone who stayed, " +
                            "and the record will say missed, " +
                            "kindly, in pencil."
            today == inst.day ->
                "🚌 The trip is OFF today — " +
                    (state.lifeText["tripYield:${inst.key}"]
                        ?.let { yieldText(it) }
                        ?: "the town had bigger business") +
                    ". The poster says so, with the apology " +
                    "stapled straight."
            else -> null
        }
    }

    // ---------------- the Journal page ----------------

    /** The 🚌 FIELD TRIPS page: the coming trip while
     *  it's ahead, the live day while it's live, the
     *  wrapped trips read back from their records. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val inst = currentInstance(state)
        val site = inst.site
        out += "FIELD TRIPS — the fourth grade, out of " +
            "the building"
        out += "Three Fridays a school year, the class " +
            "rides instead of sitting: Stark's Pond in " +
            "the fall (nature study), the Historical Room " +
            "in the winter, Tegridy Farms in the spring " +
            "(agriculture day). ${roster().size} kids, one " +
            "bus, one chaperone, no exceptions and one " +
            "mercy run."
        val rec = record(state, inst.key)
        when {
            rec != null && rec.outcome.isNotEmpty() -> {
                out += tripLine(rec)
            }
            state.absDay == inst.day && tripLive(state) -> {
                out += "🚍 TODAY: ${site.name} — the class " +
                    "is out. " +
                    if (riding(state))
                        "You rode. The ${site.beatName} " +
                            "runs through " +
                            "${chaperone(state.absDay)}; the " +
                            "buses back are at one."
                    else
                        "The bus loads at this school's " +
                            "board until 9:30."
            }
            state.absDay == inst.day -> {
                out += "🚍 TODAY was meant to be ${site.name} — " +
                    "the trip is off: " +
                    (state.lifeText["tripYield:${inst.key}"]
                        ?.let { yieldText(it) }
                        ?: "the town had bigger business") + "."
            }
            state.absDay >= inst.announce -> {
                out += "📌 POSTED: ${site.name} — " +
                    "${dateLabel(inst.day)}. Be at the " +
                    "classroom board by 9:30 that morning; " +
                    "the bus does not wait, and neither " +
                    "does Crabtree."
            }
            else -> {
                out += "📌 NEXT: ${site.name} — " +
                    "${dateLabel(inst.day)}. The poster " +
                    "goes up ${dateLabel(inst.announce)}."
            }
        }
        val past = allRecords(state)
            .filter { it.outcome.isNotEmpty() && it.key != inst.key }
            .take(HISTORY_CAP)
        if (past.isNotEmpty()) {
            out += "— past trips —"
            for (r in past) out += tripLine(r)
        }
        return out
    }

    private fun tripLine(rec: TripRecord): String {
        val site = siteFor(rec.season)
        val label = dateLabel(rec.day)
        return when (rec.outcome) {
            OUT_YIELDED -> "🚌 $label — ${site.name}: no " +
                "trip — ${yieldText(rec.yieldWhy)}. The " +
                "bus stayed in the lot."
            OUT_MISSED -> "🚌 $label — ${site.name}: the " +
                "class went; you weren't on the bus. The " +
                "day is on the school's record, not yours."
            else -> "🚌 $label — ${site.name}: you rode. " +
                when (rec.beatResult) {
                    "sharp" -> "The ${site.beatName} went " +
                        "three for three."
                    "steady" -> "The ${site.beatName} went " +
                        "two of three."
                    "easy" -> "The ${site.beatName} was " +
                        "played, and the site kept its " +
                        "secrets."
                    else -> "The ${site.beatName} never " +
                        "got played."
                } +
                if (rec.photoId.isNotEmpty())
                    " Its photograph is in your album."
                else " No photograph is on record."
        }
    }

    // ---------------- the paper ----------------

    /** The trip week's ONE TOWN NEWS line, derived whole
     *  off the wrapped record. Section copy — the
     *  headline ladder stands at ten. */
    fun paperLine(state: GameState, week: Int): String? {
        val rec = allRecords(state).firstOrNull {
            it.outcome.isNotEmpty() && it.day >= 0 &&
                Jobs.weekIndex(it.day) == week
        } ?: return null
        val site = siteFor(rec.season)
        val label = dateLabel(rec.day)
        return when (rec.outcome) {
            OUT_YIELDED -> "🚌 The ${seasonName(rec.season)} " +
                "field trip did not run — " +
                yieldText(rec.yieldWhy) + ". The Elementary " +
                "kept a quiet day; the bus kept its tank."
            OUT_MISSED -> "🚌 The ${seasonName(rec.season)} " +
                "field trip — ${site.name}, $label. The " +
                "class rode out whole; the new kid wasn't " +
                "on the bus. The day is on the school's " +
                "record."
            else -> "🚌 The ${seasonName(rec.season)} field " +
                "trip — ${site.name}, $label. The fourth " +
                "grade spent a school day out of the " +
                "building and came back with the " +
                "${site.beatName.lowercase()} on the record" +
                if (rec.photoId.isNotEmpty())
                    " and a photograph in the album."
                else "."
        }
    }

    // ---------------- the phone ----------------

    /** The trip's ONE morning-after text, in the
     *  harvest's fact discipline: from the classmate
     *  closest to you who rode (the roster rode whole),
     *  fact-only, moving nothing. Yielded trips text
     *  nobody — the poster already said it. */
    fun phoneBeat(state: GameState): Triple<String, String, String>? {
        val inst = instancesAround(state.absDay)
            .firstOrNull { it.day == state.absDay - 1 }
            ?: return null
        val rec = record(state, inst.key) ?: return null
        if (rec.outcome.isEmpty() || rec.outcome == OUT_YIELDED)
            return null
        val site = inst.site
        val sender = roster().minWithOrNull(
            compareByDescending<String> {
                state.friendship[it] ?: 0
            }.thenBy { it }) ?: return null
        val photoBit = if (rec.photoId.isNotEmpty())
            " the class photo came out — it's in your album."
        else ""
        return if (rec.outcome == OUT_MISSED) {
            Triple(sender, "missed",
                "you missed the field trip friday. we " +
                    "went to ${site.short}, the bus smelled " +
                    "like bus, ${chaperone(rec.day)} counted " +
                    "us four times. the journal has the " +
                    "day, what there was of it.")
        } else {
            val beatBit = when (rec.beatResult) {
                "sharp" -> "you cleaned up at the " +
                    "${site.beatName.lowercase()} — three " +
                    "for three."
                "steady" -> "the ${site.beatName.lowercase()} " +
                    "went two of three with your name on it."
                "easy" -> "the ${site.beatName.lowercase()} " +
                    "kept its secrets, mostly."
                else -> "you never did run the " +
                    "${site.beatName.lowercase()}."
            }
            Triple(sender, "went",
                "friday's trip, in review: ${site.short}, " +
                    "the whole class, one bus. $beatBit" +
                    "$photoBit monday, then.")
        }
    }

    // ---------------- mornings ----------------

    /** The trips' mornings, called by Engine.onNewDay
     *  beside the other set pieces, before the paper and
     *  the phone (their line and text derive from these
     *  facts): the poster's ticker on announcement
     *  morning, and the wrap on the morning after —
     *  went if you rode, yielded with its reason if an
     *  owner held the day, missed if the bus simply went
     *  without you. Yesterday's day keys are pruned. */
    fun dailyRollover(state: GameState) {
        for (inst in instancesAround(state.absDay)) {
            // announcement morning: the poster's ticker
            if (state.absDay == inst.announce &&
                state.life["tripAnn:${inst.key}"] == null) {
                state.life["tripAnn:${inst.key}"] = 1
                note(state,
                    "🚌 POSTER UP at the Elementary: " +
                        "FIELD TRIP — ${inst.site.name}, " +
                        "${dateLabel(inst.day)}. The whole " +
                        "class, one bus, first bell; be at " +
                        "the classroom board by 9:30 or " +
                        "watch it leave. Slips home and " +
                        "back by Thursday.")
            }
            // the morning after: wrap the day
            if (state.absDay == inst.day + 1) {
                val rec = record(state, inst.key)
                if (rec == null || rec.outcome.isEmpty()) {
                    val base = rec ?: TripRecord(
                        key = inst.key, season = inst.season,
                        year = inst.year, day = inst.day)
                    val yieldWhy =
                        state.lifeText["tripYield:${inst.key}"]
                            ?: ""
                    val outcome = when {
                        yieldWhy.isNotEmpty() -> OUT_YIELDED
                        state.life["tripRode:${inst.day}"] == 1 ->
                            OUT_WENT
                        else -> OUT_MISSED
                    }
                    saveRecord(state, base.copy(
                        outcome = outcome, yieldWhy = yieldWhy))
                    note(state, when (outcome) {
                        OUT_WENT -> "🚌 The ${seasonName(inst.season)} " +
                            "field trip (${inst.year}) — " +
                            "${inst.site.name}, and you were " +
                            "on the bus."
                        OUT_YIELDED -> "🚌 The ${seasonName(inst.season)} " +
                            "field trip (${inst.year}) — no " +
                            "trip: ${yieldText(yieldWhy)}."
                        else -> "🚌 The ${seasonName(inst.season)} " +
                            "field trip (${inst.year}) — the " +
                            "class went to ${inst.site.name} " +
                            "without you."
                    })
                }
            }
        }
        // prune yesterday's day keys — the records carry
        // the permanent marks
        val yday = state.absDay - 1
        for (p in listOf("tripRode:", "tripStep:",
            "tripBeatDone:", "tripBack:")) {
            state.life.remove("$p$yday")
        }
        state.lifeText.remove("tripPicks:$yday")
        state.lifeText.remove("tripPhoto:$yday")
    }
}

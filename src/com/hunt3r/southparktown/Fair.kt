package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// The County Fair — v0.9.0c (Queue #9 item 3): one week
// every summer, the fair comes to town.
//
// THE WEEK (FairCalendar, pure calendar math like Finales):
// the fair opens on the THIRD SATURDAY of August and runs
// seven days, Saturday to Friday, at Kid's Park — the open
// lawn in the middle of town takes the booths, the tank and
// the wheel, and Stark's Pond keeps the Regatta (and hosts
// the fair's own fishing morning as an excursion). A fixed
// Saturday anchor means the week MOVES against the season
// finale (the last week of August): in years where the
// fair week would touch the Regatta's week, the fair shifts
// one week earlier and SAYS SO on every surface that
// announces it. It never lands on a finale week — the
// suite proves the two windows disjoint for decades.
//
// THE CONTESTS (three, spread across the week, all judged
// on rails the town already trusts):
//   📸 PHOTO (days 0–2, judged the morning of day 3): fair
//      frames only — photos taken AT the fairground during
//      the entry days, entered by being taken — scored by
//      the yearbook's own math (Yearbook.scoreOf over Town
//      Favorite + Busiest Scene), against seeded townsfolk
//      entries on the same scale. Places 1st–3rd, ribbons,
//      and a judge from the yearbook masthead whose note
//      only ever claims facts the winning record proves.
//   🎣 FISHING (day 3, at the pond, judged next morning):
//      your pond catches that day funnel in through
//      applyCatch (your best counts); the field — Jimbo,
//      the rival when one is crowned, and seeded townsfolk —
//      each make ONE cast on the real FISH_TABLE with their
//      authored luck (RivalDefs' castLuck where it exists).
//      Ranked by Rival.fishRank, then size: rarer beats
//      bigger, and a boot is not a fish. When both you and
//      the rival hold entries, the result also lands on
//      the 🆚 ledger through Rival.recordResult — unless a
//      rival fishing duel is already armed, in which case
//      the duel keeps the ledger and the fair keeps its
//      page. Never double-counted.
//   🎯 DUNK TANK (day 5, at the fairground): a seeded
//      sitter takes the chair; five townsfolk and you each
//      get three throws. Your throws are REALLY thrown —
//      the showdown's timing bar and zone math against the
//      sitter's own dodgeFor. Their throws are simulated
//      off the same zones and the same dodge. Most dunks
//      wins; ties break on total throw power, then name —
//      a total order, no coin flips.
//
// PRIZES: ribbons (blue/red/white for 1st/2nd/3rd, kept on
// the fair's record and its Journal page), capsule coins
// (6/4/2), a 🏆 feats pair (Fair Ribbon for a first blue,
// Fair Sweep for all three blues in one fair), and ONE
// derived house piece: the ribbon display that goes up by
// the yearbook shelf the first time you take a blue
// (House derives it from these records, like every piece).
//
// FAIR LIFE: fair hours (10:00–20:00, to 21:30 on closing
// night for the fireworks watch) bend the town's agendas
// to the fairground like a gathering — a self-guarded
// claim that yields to dinners, jobs, screenings,
// ceremonies, finales and live case work, and never holds
// a crew member against the crew. Liane staffs a snack
// booth (pantry food at honest fair prices, paid in
// capsule coins — a small sink), four authored fair
// errands post across the week through the tap-person
// menu, and the opening morning offers a played beat:
// cutting the ribbon with the Mayor. Closing night ends
// with FIREWORKS — a SkyState special keyed to the fair's
// own calendar, so a photo taken that night honestly
// shows them, and the yearbook's sky judges know their
// worth (25, under the harvest moon).
//
// RECORDS: one FairRecord per year (GameState.fairs — the
// one additive save field), written at the opening,
// filled as contests judge, wrapped the morning after
// closing: attended honestly, or missed honestly ("you
// missed the fair" is a record too). The Journal page
// shows the newest fair whole and elders as one lines,
// the yearbook shelf's own cap discipline.
// ============================================================

/** One contest placing: who, with what, at what score. The
 *  detail is a fact line (a species and a size, a frame's
 *  company, a dunk count) — the page prints it verbatim. */
data class FairPlace(
    val name: String,
    val detail: String,
    val score: Int
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name, "detail" to detail, "score" to score)

    companion object {
        fun fromMap(m: Map<String, Any?>): FairPlace? {
            val name = m["name"]?.toString()?.takeIf { it.isNotEmpty() }
                ?: return null
            return FairPlace(name,
                m["detail"]?.toString() ?: "",
                (m["score"] as? Number)?.toInt() ?: 0)
        }
    }
}

/** One year's fair, whole: the calendar facts, the sitter,
 *  every contest's places, the judge's note for the photo
 *  contest, and where YOU placed (0 = unplaced/unentered). */
data class FairRecord(
    val year: Int,
    val shifted: Boolean,
    val sitter: String,
    val wrapped: Boolean = false,
    val attended: Boolean = false,
    val missed: Boolean = false,
    val photoPlaces: List<FairPlace> = emptyList(),
    val fishPlaces: List<FairPlace> = emptyList(),
    val dunkPlaces: List<FairPlace> = emptyList(),
    val photoJudge: String = "",
    val photoNote: String = "",
    val playerPhoto: Int = 0,
    val playerFish: Int = 0,
    val playerDunk: Int = 0,
    // v0.11.0b: the fourth contest — the produce weigh-in.
    // Last and defaulted: a pre-0.11.0b record reads as a
    // fair whose weigh-in table stood empty.
    val producePlaces: List<FairPlace> = emptyList(),
    val playerProduce: Int = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "year" to year, "shifted" to shifted, "sitter" to sitter,
        "wrapped" to wrapped, "attended" to attended,
        "missed" to missed,
        "photoPlaces" to photoPlaces.map { it.toMap() },
        "fishPlaces" to fishPlaces.map { it.toMap() },
        "dunkPlaces" to dunkPlaces.map { it.toMap() },
        "photoJudge" to photoJudge, "photoNote" to photoNote,
        "playerPhoto" to playerPhoto, "playerFish" to playerFish,
        "playerDunk" to playerDunk,
        "producePlaces" to producePlaces.map { it.toMap() },
        "playerProduce" to playerProduce)

    /** Lifetime blue ribbons on this record (the feats and
     *  the house piece count these). */
    fun blues(): Int =
        listOf(playerPhoto, playerFish, playerDunk,
            playerProduce).count { it == 1 }

    companion object {
        fun fromMap(m: Map<String, Any?>): FairRecord? {
            val year = (m["year"] as? Number)?.toInt() ?: return null
            fun places(key: String): List<FairPlace> =
                (m[key] as? List<Any?>)?.mapNotNull { raw ->
                    (raw as? Map<String, Any?>)?.let { FairPlace.fromMap(it) }
                } ?: emptyList()
            return FairRecord(
                year = year,
                shifted = m["shifted"] as? Boolean ?: false,
                sitter = m["sitter"]?.toString() ?: "",
                wrapped = m["wrapped"] as? Boolean ?: false,
                attended = m["attended"] as? Boolean ?: false,
                missed = m["missed"] as? Boolean ?: false,
                photoPlaces = places("photoPlaces"),
                fishPlaces = places("fishPlaces"),
                dunkPlaces = places("dunkPlaces"),
                photoJudge = m["photoJudge"]?.toString() ?: "",
                photoNote = m["photoNote"]?.toString() ?: "",
                playerPhoto = (m["playerPhoto"] as? Number)?.toInt() ?: 0,
                playerFish = (m["playerFish"] as? Number)?.toInt() ?: 0,
                playerDunk = (m["playerDunk"] as? Number)?.toInt() ?: 0,
                producePlaces = places("producePlaces"),
                playerProduce = (m["playerProduce"] as? Number)?.toInt() ?: 0)
        }
    }
}

// ============================================================
// FairCalendar — the week's pure calendar math. Everything
// here is a function of the Gregorian calendar alone (the
// Finales pattern): no save state, no randomness, so the
// renderer, the sky, the phone and the suite all compute
// the same fair.
// ============================================================
object FairCalendar {

    /** The game's first day (Engine: Day 1 = Oct 1, 2026) —
     *  absDay 0 lives here; SkyState speaks absDay, the fair
     *  speaks ordinals, this is the bridge. */
    const val EPOCH_KEY = "20261001"

    fun ordinalOfAbsDay(absDay: Int): Int =
        ordinalOfKey(EPOCH_KEY) + absDay

    fun absDayOfOrdinal(ord: Int): Int =
        ord - ordinalOfKey(EPOCH_KEY)

    /** Day of week for an ordinal: 0 = Sunday .. 6 =
     *  Saturday (Engine.dayOfWeek's anchor: the epoch day
     *  was a Thursday, 4). */
    fun dowOfOrdinal(ord: Int): Int {
        val diff = ord - ordinalOfKey(EPOCH_KEY)
        return ((4 + diff) % 7 + 7) % 7
    }

    /** The Saturday the fair opens on, before the finale
     *  check: the third Saturday of August. */
    fun thirdSaturdayOrdinal(year: Int): Int {
        val aug1 = ordinalOfKey("%04d0801".format(year))
        val toSaturday = (6 - dowOfOrdinal(aug1) + 7) % 7
        return aug1 + toSaturday + 14
    }

    /** The summer finale's window (Finales' own math: the
     *  season's last WINDOW_DAYS days, ending Aug 31). */
    fun finaleWindow(year: Int): IntRange {
        val end = ordinalOfKey("%04d0831".format(year))
        return (end - (Finales.WINDOW_DAYS - 1))..end
    }

    data class FairWindow(
        val year: Int,
        val startOrd: Int,
        val shifted: Boolean
    ) {
        val endOrd: Int get() = startOrd + 6
        val closingOrd: Int get() = endOrd
        fun dayIndex(ord: Int): Int = ord - startOrd
        fun contains(ord: Int): Boolean = ord in startOrd..endOrd
        fun startKey(): String = keyFromOrdinal(startOrd)
        fun endKey(): String = keyFromOrdinal(endOrd)
    }

    /** [year]'s fair week: the third Saturday's week,
     *  moved one week earlier iff it would touch the
     *  finale's week. One shift always suffices — the
     *  candidate starts Aug 15–21, so the shifted week
     *  ends by Aug 20, five clear days before the finale
     *  can open (Aug 25). Suite-pinned for decades. */
    fun windowFor(year: Int): FairWindow {
        val cand = thirdSaturdayOrdinal(year)
        val fin = finaleWindow(year)
        val touches = cand <= fin.last && cand + 6 >= fin.first
        return FairWindow(year, if (touches) cand - 7 else cand,
            touches)
    }

    /** The fair week containing [ord], if any. */
    fun windowAtOrdinal(ord: Int): FairWindow? {
        if (ord <= 0) return null
        val y = keyFromOrdinal(ord).substring(0, 4).toIntOrNull()
            ?: return null
        for (yy in (y - 1)..(y + 1)) {
            val w = windowFor(yy)
            if (w.contains(ord)) return w
        }
        return null
    }

    /** The next fair whose week hasn't fully passed — the
     *  one the poster and the phone count down to. */
    fun nextWindowAfter(ord: Int): FairWindow {
        val y = keyFromOrdinal(ord).substring(0, 4).toIntOrNull()
            ?: 2026
        for (yy in y..(y + 2)) {
            val w = windowFor(yy)
            if (w.endOrd >= ord) return w
        }
        return windowFor(y + 3)
    }

    /** Fireworks night: the closing date of its year's
     *  fair. SkyState asks by absDay (its own tongue);
     *  the date math stays here. */
    fun isFireworksAbsDay(absDay: Int): Boolean {
        val ord = ordinalOfAbsDay(absDay)
        val w = windowAtOrdinal(ord) ?: return false
        return ord == w.closingOrd
    }
}

object Fair {

    // ---------------- the dials (all suite-pinned) ----------------

    /** The fairground: Kid's Park. The pond keeps the
     *  Regatta and hosts the fishing morning; the park's
     *  open lawn takes the booths, the tank and the wheel. */
    const val FAIRGROUND = "KidsPark"
    const val POND = "StarksPond"

    /** Fair hours: the gates are open 10:00–20:00. */
    const val FAIR_FROM = 10 * 60
    const val FAIR_TO = 20 * 60

    /** Closing night, the town stays for the fireworks. */
    const val WATCH_TO = 21 * 60 + 30

    /** Days before opening that the poster goes up, the
     *  ticker calls it, and the Mayor texts it: three. */
    const val ANNOUNCE_AHEAD = 3

    /** The photo contest takes fair frames from the first
     *  three days (0–2) and judges on day 3's morning. */
    const val PHOTO_LAST_DAY = 2

    /** The fishing contest's one day, at the pond. */
    const val FISH_DAY = 3

    /** The dunk tank's day, at the fairground. */
    const val DUNK_DAY = 5

    /** v0.11.0b: the produce weigh-in's day — the fair's
     *  quiet day, when the giant pumpkins come to the
     *  table. Entries close with the day (the porch
     *  specimen is the entry); the weigh-in judges on the
     *  dunk morning, beside the tank's own crowd. */
    const val PRODUCE_DAY = 4

    /** Throws per dunk-tank entrant, you included. */
    const val DUNK_THROWS = 3

    /** Contest coins by place (capsule coins, the town's
     *  own economy — Minigames.awardCoins pays them). */
    val PLACE_COINS = listOf(6, 4, 2)

    /** Ribbon names by place. */
    val RIBBONS = listOf("blue", "red", "white")

    /** Seeded field sizes (the player is never in these
     *  counts — they're the town you're up against). */
    const val PHOTO_FIELD = 6
    const val FISH_FIELD = 6
    const val DUNK_FIELD = 5
    const val PRODUCE_FIELD = 5

    // ---------------- feats (rendered with the town's feat families) ---

    val FAIR_FEATS: List<ErrandFeat> = listOf(
        ErrandFeat("fair_ribbon", "🏆 Fair Ribbon",
            "Take a blue ribbon at the county fair — any " +
                "contest, any year. It goes up at home.",
            1, "Fair Ribbon"),
        ErrandFeat("fair_sweep", "🏆 Fair Sweep",
            "Take three blue ribbons at ONE fair — " +
                "photo, fishing, tank or the produce " +
                "table; any three of the four. " +
                "Almost nobody. Almost.",
            3, "Fair Sweep")
    )

    fun featById(id: String): ErrandFeat? =
        FAIR_FEATS.firstOrNull { it.id == id }

    /** Lifetime blue ribbons across every wrapped and
     *  live fair on the save. */
    fun blueRibbons(state: GameState): Int =
        state.fairs.sumOf { it.blues() }

    fun fairFeatEarned(state: GameState, f: ErrandFeat): Boolean =
        when (f.id) {
            "fair_ribbon" -> blueRibbons(state) >= 1
            "fair_sweep" -> state.fairs.any { it.blues() >= 3 }
            else -> false
        }

    fun earnedFeats(state: GameState): List<ErrandFeat> =
        FAIR_FEATS.filter { fairFeatEarned(state, it) }

    // ---------------- the calendar, from a state's chair ----------------

    fun todayOrdinal(state: GameState): Int =
        ordinalOfKey(state.todayKey)

    fun windowToday(state: GameState): FairCalendar.FairWindow? =
        FairCalendar.windowAtOrdinal(todayOrdinal(state))

    fun isFairWeek(state: GameState): Boolean =
        windowToday(state) != null

    /** 0 = opening Saturday … 6 = closing Friday, or -1
     *  outside the week. */
    fun dayIndex(state: GameState): Int =
        windowToday(state)?.dayIndex(todayOrdinal(state)) ?: -1

    fun inFairHours(state: GameState): Boolean {
        val idx = dayIndex(state)
        if (idx < 0) return false
        val to = if (idx == 6) WATCH_TO else FAIR_TO
        return state.timeMin in FAIR_FROM until to
    }

    fun recordFor(state: GameState, year: Int): FairRecord? =
        state.fairs.firstOrNull { it.year == year }

    fun liveRecord(state: GameState): FairRecord? =
        windowToday(state)?.let { recordFor(state, it.year) }

    private fun putRecord(state: GameState, rec: FairRecord) {
        val i = state.fairs.indexOfFirst { it.year == rec.year }
        if (i >= 0) state.fairs[i] = rec else state.fairs.add(rec)
    }

    private fun playerName(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.name
            ?: "New Kid"

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    private fun keyOf(y: Int, m: Int, d: Int) =
        "%04d%02d%02d".format(y, m, d)

    fun dateLabelOf(ord: Int): String {
        val k = keyFromOrdinal(ord)
        return "${k.substring(4, 6).toInt()}/${k.substring(6, 8).toInt()}"
    }

    // ---------------- the sitter & the fields ----------------

    /** Who takes the dunk tank's chair, by year: seeded off
     *  the fair's own calendar, from the town's born
     *  volunteers. */
    val SITTERS: List<String> =
        listOf("Cartman", "Randy", "Jimbo", "Butters")

    fun sitterFor(year: Int): String =
        SITTERS[Random(dailySeed(keyOf(year, 8, 1)) + 77L)
            .nextInt(SITTERS.size)]

    /** The seeded entrant field for a contest: a stable
     *  shuffle of the whole cast (kids and adults — the
     *  fair is everybody's), pinned per (year, contest). */
    fun fieldFor(year: Int, contest: String, count: Int,
                 must: List<String> = emptyList()): List<String> {
        val pool = (CharacterSystem.CAST_KIDS +
            CharacterSystem.CAST_ADULTS).sorted()
        val rnd = Random(dailySeed(keyOf(year, 8, 1)) +
            contest.hashCode().toLong() * 131L)
        val shuffled = pool.shuffled(rnd)
        val out = must.filter { it in pool }.toMutableList()
        for (name in shuffled) {
            if (out.size >= count) break
            if (name !in out) out += name
        }
        return out
    }

    /** An entrant's authored fishing luck: the rival
     *  pool's castLuck where the town wrote one, else the
     *  table's plain odds. */
    fun luckFor(name: String): Int =
        Rival.POOL.firstOrNull { it.name == name }?.castLuck ?: 0

    /** One cast on the real FISH_TABLE, weights bent by
     *  [luck] exactly the way Rival.rivalCast bends them. */
    fun castFor(name: String, seed: Long): Pair<FishSpecies, Int> {
        val rnd = Random(seed)
        val luck = luckFor(name)
        fun effW(sp: FishSpecies): Int =
            if (luck == 0) sp.weight
            else (sp.weight + luck * (100 - sp.weight) / 10)
                .coerceAtLeast(1)
        val roll = rnd.nextInt(FISH_TABLE.sumOf { effW(it) })
        var acc = 0
        var caught = FISH_TABLE.last()
        for (sp in FISH_TABLE) {
            acc += effW(sp)
            if (roll < acc) { caught = sp; break }
        }
        val cm = caught.minCm +
            rnd.nextInt(caught.maxCm - caught.minCm + 1)
        return caught to cm
    }

    // ================================================================
    // The photo contest
    // ================================================================

    /** The fair score of a real frame: the yearbook's own
     *  math — Town Favorite over Busiest Scene, star and
     *  all. Stated here once; the judges change nothing. */
    fun photoScore(rec: PhotoRecord): Int =
        Yearbook.scoreOf(Yearbook.CAT_FAVORITE, rec) +
            Yearbook.scoreOf(Yearbook.CAT_BUSY, rec)

    /** The album's fair entries: frames taken AT the
     *  fairground on the entry days of [year]'s fair. */
    fun photoEntries(state: GameState,
                     w: FairCalendar.FairWindow): List<PhotoRecord> {
        val first = keyFromOrdinal(w.startOrd)
        val last = keyFromOrdinal(w.startOrd + PHOTO_LAST_DAY)
        return state.photos.filter {
            it.locId == FAIRGROUND && it.dayKey in first..last
        }
    }

    /** The player's entered frame: their best-scoring fair
     *  frame (ties by earlier day, then id — the yearbook's
     *  total order). */
    fun playerPhotoEntry(state: GameState,
                         w: FairCalendar.FairWindow): PhotoRecord? =
        photoEntries(state, w).maxWithOrNull(
            compareBy<PhotoRecord> { photoScore(it) }
                .thenByDescending { it.dayKey }
                .thenByDescending { it.id })

    /** A townsfolk entry's score: seeded per (year,
     *  entrant) onto the same scale real frames land on —
     *  a strong fair frame beats most of them; a lone
     *  selfie beats almost none. That IS the contest. */
    fun npcPhotoScore(year: Int, name: String): Int {
        val rnd = Random(dailySeed(keyOf(year, 8, 1)) +
            name.hashCode().toLong() * 977L)
        return 25 + rnd.nextInt(140)
    }

    /** The judge's note names for a winning frame, in the
     *  yearbook's discipline: every slot is a fact the
     *  record itself proves — who is in it, where, the
     *  weather it was taken under. */
    private data class PhotoFacts(
        val winner: String, val people: String, val weather: String,
        val figures: Int, val score: Int)

    private fun fillPhoto(t: String, f: PhotoFacts): String = t
        .replace("{winner}", f.winner)
        .replace("{people}", f.people)
        .replace("{weather}", f.weather)
        .replace("{figures}", f.figures.toString())
        .replace("{score}", f.score.toString())

    /** Wendy's desk: the straight note. */
    private val PHOTO_NOTES_WENDY: List<String> = listOf(
        "Judged on the frame, not the feelings: {people}, " +
            "{figures} figures deep, under a {weather} sky. " +
            "That's the fair, caught. ({score} points.)",
        "The winner had to be this one. {people} — nobody " +
            "posed, everybody's mid-fair. {score} points, " +
            "and it wasn't close in the ways that count.")

    /** Jimmy's desk: the showman's note. */
    private val PHOTO_NOTES_JIMMY: List<String> = listOf(
        "What a terrific frame. {people}, the whole midway " +
            "in one shot — {figures} figures! Even the " +
            "{weather} cooperated. {score} points.",
        "I've announced a lot of winners, and — wow, what " +
            "a great audience — this one earned the mic: " +
            "{people}, {score} points of pure fair.")

    /** Cartman's desk: the critic endures. */
    private val PHOTO_NOTES_CARTMAN: List<String> = listOf(
        "Fine. FINE. {people} took a good picture. " +
            "{figures} figures, {weather} weather, {score} " +
            "points. I could have done better but I was busy " +
            "being the fair's most important attendee.",
        "The 'winner' is {winner} with {people}. {score} " +
            "points. My entry was disqualified for being too " +
            "powerful, probably. Whatever.")

    /** An NPC winner's note: only contest facts — the
     *  winner's name, the fairground, the field they beat.
     *  Their frame is theirs; the note invents nothing
     *  about it. */
    private val PHOTO_NOTES_NPC: List<String> = listOf(
        "{winner} takes it — the fairground frame the whole " +
            "field chased all week. {score} points, and the " +
            "ribbon table doesn't argue.",
        "The blue goes to {winner}: {score} points at Kid's " +
            "Park, top of a field of ${PHOTO_FIELD + 1}. The " +
            "town has seen the print. It's the fair, caught.")

    fun photoJudgeFor(year: Int): Yearbook.Editor =
        Yearbook.judgeFor(year, 1)

    /** Judge the photo contest for [w]'s fair. Pure over
     *  the album + the seeded field; pays the player's
     *  coins if they placed. Runs once (rollover-guarded). */
    fun judgePhoto(state: GameState, w: FairCalendar.FairWindow) {
        val rec = recordFor(state, w.year) ?: return
        val judge = photoJudgeFor(w.year)
        val field = fieldFor(w.year, "photo", PHOTO_FIELD)
        val contenders = mutableListOf<FairPlace>()
        val mine = playerPhotoEntry(state, w)
        var playerScore = -1
        if (mine != null) {
            playerScore = photoScore(mine)
            val who = if (mine.npcs.isEmpty()) "the fair, wide"
                else mine.npcs.joinToString(", ") { it.name }
            contenders += FairPlace(playerName(state),
                "your frame: $who at the fairground", playerScore)
        }
        for (name in field) {
            contenders += FairPlace(name,
                "their fairground frame", npcPhotoScore(w.year, name))
        }
        val places = contenders
            .sortedWith(compareByDescending<FairPlace> { it.score }
                .thenBy { it.name })
            .take(3)
        val playerPlace =
            if (mine == null) 0
            else places.indexOfFirst { it.name == playerName(state) } + 1
        // the judge's note over the winner — record facts
        // only, whichever side of the lens won.
        val winner = places.firstOrNull()
        val noteText: String
        if (winner != null && mine != null &&
            winner.name == playerName(state)) {
            val facts = PhotoFacts(
                winner = winner.name,
                people = if (mine.npcs.isEmpty()) "the whole fair"
                    else mine.npcs.joinToString(", ") { it.name },
                weather = mine.weatherId.replace('_', ' '),
                figures = mine.figures.size,
                score = winner.score)
            val bank = when (judge.name) {
                "Wendy" -> PHOTO_NOTES_WENDY
                "Jimmy" -> PHOTO_NOTES_JIMMY
                else -> PHOTO_NOTES_CARTMAN
            }
            noteText = fillPhoto(bank[
                (w.year + winner.score) % bank.size], facts)
        } else if (winner != null) {
            val facts = PhotoFacts(winner.name, "", "", 0, winner.score)
            noteText = fillPhoto(PHOTO_NOTES_NPC[
                (w.year + winner.score) % PHOTO_NOTES_NPC.size], facts)
        } else {
            noteText = ""
        }
        if (playerPlace in 1..3) {
            Minigames.awardCoins(state, PLACE_COINS[playerPlace - 1])
        }
        putRecord(state, rec.copy(
            photoPlaces = places,
            photoJudge = judge.name,
            photoNote = noteText,
            playerPhoto = playerPlace))
        if (mine != null) markAttended(state, w.year)
        state.life["fairPhotoJudged:${w.year}"] = 1
        note(state, "📸 County fair photo contest: " +
            (winner?.let { "${it.name} takes the blue " +
                "(${it.score})." } ?: "no entries at all — the " +
                "ribbon table keeps the ribbons.") +
            (if (playerPlace in 1..3)
                " You placed ${ordinalWord(playerPlace)} — " +
                    "a ${RIBBONS[playerPlace - 1]} ribbon!" else ""))
    }

    fun ordinalWord(n: Int): String = when (n) {
        1 -> "1st"; 2 -> "2nd"; 3 -> "3rd"; else -> "${n}th"
    }

    // ================================================================
    // The fishing contest
    // ================================================================

    private fun kFishBest(year: Int) = "fairFishBest:$year"

    /** applyCatch's funnel (Minigames calls this beside
     *  Rival.onCatch): on fishing day, a pond catch is a
     *  contest cast — your best (rank, then size) stands
     *  as your entry. Nothing else about the catch changes. */
    fun onCatch(state: GameState, sp: FishSpecies, cm: Int) {
        val w = windowToday(state) ?: return
        if (w.dayIndex(todayOrdinal(state)) != FISH_DAY) return
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return
        if (me.location != POND) return
        val key = kFishBest(w.year)
        val cur = state.lifeText[key]
        val better = if (cur == null) true else {
            val parts = cur.split(":")
            val curSp = fishById(parts.getOrNull(0) ?: "")
            val curCm = parts.getOrNull(1)?.toIntOrNull() ?: 0
            curSp == null || Rival.fishRank(sp) < Rival.fishRank(curSp) ||
                (Rival.fishRank(sp) == Rival.fishRank(curSp) && cm > curCm)
        }
        if (better) {
            state.lifeText[key] = "${sp.id}:$cm"
            note(state, "🎣 Fair fishing contest: your ${sp.name} " +
                "($cm cm) is your entry — the table has it.")
        }
    }

    fun playerFishEntry(state: GameState,
                        year: Int): Pair<FishSpecies, Int>? {
        val cur = state.lifeText[kFishBest(year)] ?: return null
        val parts = cur.split(":")
        val sp = fishById(parts.getOrNull(0) ?: "") ?: return null
        val cm = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return sp to cm
    }

    /** The fishing field: Jimbo (he'd haunt the fair if he
     *  missed it), the rival when one is crowned, and the
     *  seeded draw — each making exactly one cast. */
    fun fishField(state: GameState, year: Int): List<String> {
        val must = mutableListOf("Jimbo")
        Rival.rivalOf(state)?.let { must += it }
        return fieldFor(year, "fishing", FISH_FIELD + must.size,
            must).take(FISH_FIELD + must.size)
    }

    /** Judge the fishing contest. The field's casts are
     *  seeded per (year, entrant) — the same casts every
     *  time the question is asked, so judging is a pure
     *  read of the day the town already had. */
    fun judgeFish(state: GameState, w: FairCalendar.FairWindow) {
        val rec = recordFor(state, w.year) ?: return
        val contenders = mutableListOf<FairPlace>()
        val mine = playerFishEntry(state, w.year)
        if (mine != null) {
            contenders += FairPlace(playerName(state),
                "${mine.first.name}, ${mine.second} cm",
                fishScore(mine.first, mine.second))
        }
        val field = fishField(state, w.year)
        for (name in field) {
            val (sp, cm) = castFor(name,
                dailySeed(keyOf(w.year, 8, 1)) + name.hashCode() * 331L)
            contenders += FairPlace(name,
                "${sp.name}, $cm cm", fishScore(sp, cm))
        }
        val places = contenders
            .sortedWith(compareBy<FairPlace> { fishRankOf(it) }
                .thenByDescending { it.score }
                .thenBy { it.name })
            .take(3)
        // (ranking: fishRank asc, then size — score carries
        // the size rank inside fishScore's low digits)
        val playerPlace =
            if (mine == null) 0
            else places.indexOfFirst { it.name == playerName(state) } + 1
        if (playerPlace in 1..3) {
            Minigames.awardCoins(state, PLACE_COINS[playerPlace - 1])
        }
        putRecord(state, rec.copy(
            fishPlaces = places, playerFish = playerPlace))
        if (mine != null) markAttended(state, w.year)
        state.life["fairFishJudged:${w.year}"] = 1
        val winner = places.firstOrNull()
        note(state, "🎣 County fair fishing: " +
            (winner?.let { "${it.name} takes the blue with " +
                "${it.detail}." } ?: "the pond kept its secrets.") +
            (if (playerPlace in 1..3)
                " You placed ${ordinalWord(playerPlace)} — " +
                    "a ${RIBBONS[playerPlace - 1]} ribbon!" else ""))
        // the 🆚 ledger: a fair result is a contest result
        // when both rivals hold entries — recorded through
        // Rival's own path, unless an armed duel already
        // owns the fishing story. Never twice.
        val rival = Rival.rivalOf(state)
        if (mine != null && rival != null && rival in field &&
            Rival.pendingKind(state) != Rival.K_FISHING &&
            state.life["fairLedger:${w.year}"] != 1) {
            val (rsp, rcm) = castFor(rival,
                dailySeed(keyOf(w.year, 8, 1)) + rival.hashCode() * 331L)
            val winnerKey = when {
                Rival.fishRank(mine.first) != Rival.fishRank(rsp) ->
                    if (Rival.fishRank(mine.first) <
                        Rival.fishRank(rsp)) "you" else "them"
                mine.second != rcm ->
                    if (mine.second > rcm) "you" else "them"
                else -> "draw"
            }
            state.life["fairLedger:${w.year}"] = 1
            Rival.recordResult(state, Rival.K_FISHING, winnerKey,
                "the county fair fishing contest — your " +
                    "${mine.first.name} (${mine.second} cm) vs " +
                    "their ${rsp.name} ($rcm cm)")
        }
    }

    /** A catch's ranking score: rarity first (fishRank —
     *  lower is rarer), size as the low digits. Compared
     *  descending after rank, so [fishRankOf] does the
     *  real ordering; this packs rank+size for the page. */
    fun fishScore(sp: FishSpecies, cm: Int): Int =
        (1000 - Rival.fishRank(sp)) * 1000 + cm

    private fun fishRankOf(p: FairPlace): Int =
        1000 - (p.score / 1000)

    // ================================================================
    // The dunk tank
    // ================================================================

    /** One entrant's whole tank: three throws against the
     *  sitter. The model the view plays for the player and
     *  the simulator rolls for the town — same math, same
     *  zones, same dodge. */
    class DunkSession(seed: Long, val sitter: String) {
        private val rng = Random(seed)
        var throwsLeft: Int = DUNK_THROWS
            private set
        var hits: Int = 0
            private set
        var powerMilli: Int = 0
            private set
        var lastHit: Boolean = false
            private set
        val over: Boolean get() = throwsLeft <= 0

        /** Release the ball at [release01] across the bar.
         *  The showdown's zones price the throw; the
         *  sitter's own dodgeFor prices their squirm. */
        fun throwBall(release01: Float): Boolean {
            if (over) return false
            throwsLeft--
            val zone = Showdown.zoneOf(release01)
            val power = Showdown.zonePower(zone)
            powerMilli += (power * 1000).toInt()
            val chance = power * (1.0 - Showdown.dodgeFor(sitter))
            lastHit = rng.nextDouble() < chance
            if (lastHit) hits++
            return lastHit
        }
    }

    /** A townsfolk entrant's simulated tank: their arm is
     *  seeded per (year, entrant) — steady hands throw
     *  steadier — and every throw rolls on the player's
     *  own math against the same sitter. */
    fun npcDunkScore(year: Int, name: String,
                     sitter: String): FairPlace {
        val rnd = Random(dailySeed(keyOf(year, 8, 1)) +
            name.hashCode().toLong() * 613L)
        val arm = 0.80 + rnd.nextFloat() * 0.35
        var hits = 0
        var powerMilli = 0
        repeat(DUNK_THROWS) {
            val release = rnd.nextFloat()
            val power = Showdown.zonePower(Showdown.zoneOf(release))
            powerMilli += (power * 1000).toInt()
            val chance = (power * arm *
                (1.0 - Showdown.dodgeFor(sitter))).coerceIn(0.0, 1.0)
            if (rnd.nextDouble() < chance) hits++
        }
        return FairPlace(name,
            "$hits of $DUNK_THROWS dunks on $sitter",
            hits * 100000 + powerMilli)
    }

    private fun kDunk(year: Int) = "fairDunk:$year"

    /** The player's recorded tank (hits, powerMilli), or
     *  null until they step up. GameActivity records the
     *  finished session here, once. */
    fun recordDunk(state: GameState, hits: Int, powerMilli: Int) {
        val w = windowToday(state) ?: return
        if (state.lifeText[kDunk(w.year)] != null) return
        state.lifeText[kDunk(w.year)] = "$hits:$powerMilli"
        markAttended(state, w.year)
        note(state, "🎯 Dunk tank: you put the sitter in the " +
            "water $hits of $DUNK_THROWS times. The tank " +
            "judges itself tonight.")
    }

    fun playerDunk(state: GameState, year: Int): Pair<Int, Int>? {
        val raw = state.lifeText[kDunk(year)] ?: return null
        val parts = raw.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val p = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return h to p
    }

    /** The dunk places: the seeded town bracket plus the
     *  player's recorded tank when one stands. A total
     *  order — dunks, then throw power, then name. */
    fun dunkPlaces(state: GameState,
                   w: FairCalendar.FairWindow): List<FairPlace> {
        val rec = recordFor(state, w.year) ?: return emptyList()
        val contenders = mutableListOf<FairPlace>()
        for (name in fieldFor(w.year, "dunk", DUNK_FIELD)) {
            contenders += npcDunkScore(w.year, name, rec.sitter)
        }
        playerDunk(state, w.year)?.let { (h, p) ->
            contenders += FairPlace(playerName(state),
                "$h of $DUNK_THROWS dunks on ${rec.sitter}",
                h * 100000 + p)
        }
        return contenders
            .sortedWith(compareByDescending<FairPlace> { it.score }
                .thenBy { it.name })
            .take(3)
    }

    /** Finalize the dunk contest into the record (the
     *  wrap calls this; an early read for the page calls
     *  dunkPlaces directly and stores nothing). */
    fun settleDunk(state: GameState, w: FairCalendar.FairWindow) {
        val rec = recordFor(state, w.year) ?: return
        if (rec.dunkPlaces.isNotEmpty()) return
        val places = dunkPlaces(state, w)
        val mine = playerDunk(state, w.year)
        val playerPlace =
            if (mine == null) 0
            else places.indexOfFirst { it.name == playerName(state) } + 1
        if (playerPlace in 1..3) {
            Minigames.awardCoins(state, PLACE_COINS[playerPlace - 1])
        }
        putRecord(state, rec.copy(
            dunkPlaces = places, playerDunk = playerPlace))
        val winner = places.firstOrNull()
        note(state, "🎯 Dunk tank final: " +
            (winner?.let { "${it.name} takes the blue — " +
                "${it.detail}." } ?: "the sitter stayed dry. " +
                "Impossible. Check the tank for magnets."))
    }

    // ================================================================
    // THE PRODUCE WEIGH-IN (v0.11.0b) — the fourth contest.
    // The town's giant pumpkins come from the seeded field;
    // the player's comes from the GARDEN's record — the
    // specimen kept on the porch at harvest, judged off the
    // days it actually grew and the days it was actually
    // watered. No specimen, no entry: the table doesn't
    // weigh intentions. Ribbons under the fair's ordinary
    // rules (PLACE_COINS, blue counts in blues()); like
    // every fair ribbon, it mints no Pocket Pal card.
    // ================================================================

    /** A town grower's pumpkin weight for [year]: seeded
     *  per grower, 10–44 lb — a tended garden's honest
     *  best (46 lb off a perfect record) can beat the
     *  whole field, and a neglected one can't. */
    fun npcProduceWeight(year: Int, name: String): Int {
        val seed = year * 7919L +
            name.hashCode().toLong() * 131L + 41L
        return 10 + Random(seed).nextInt(35)
    }

    fun judgeProduce(state: GameState,
                     w: FairCalendar.FairWindow) {
        val rec = recordFor(state, w.year) ?: return
        if (state.life["fairProduceJudged:${w.year}"] == 1) return
        state.life["fairProduceJudged:${w.year}"] = 1
        val contenders = mutableListOf<FairPlace>()
        for (name in fieldFor(w.year, "produce", PRODUCE_FIELD)) {
            val lbs = npcProduceWeight(w.year, name)
            contenders += FairPlace(name,
                "$lbs lb pumpkin", lbs * 100)
        }
        val spec = Garden.fairSpecimen(state, w.year)
        if (spec != null) {
            contenders += FairPlace(playerName(state),
                "${spec.weight} lb pumpkin — ${spec.growthDays} " +
                    "days grown, ${spec.wateredDays} watered",
                spec.weight * 100 + spec.wateredDays)
            markAttended(state, w.year)
        }
        val places = contenders
            .sortedWith(compareByDescending<FairPlace> { it.score }
                .thenBy { it.name })
            .take(3)
        val playerPlace =
            if (spec == null) 0
            else places.indexOfFirst {
                it.name == playerName(state) } + 1
        if (playerPlace in 1..3) {
            Minigames.awardCoins(state, PLACE_COINS[playerPlace - 1])
        }
        putRecord(state, rec.copy(
            producePlaces = places, playerProduce = playerPlace))
        val winner = places.firstOrNull()
        note(state, "🎃 Produce weigh-in: " +
            (winner?.let { "${it.name} takes the blue — " +
                "${it.detail}." } ?: "an empty table. " +
                "The pumpkins stayed home this year.") +
            (if (spec == null) " (No pumpkin of yours on " +
                "the table — the garden's biggest of the " +
                "season is the entry, and the garden grew " +
                "none.)" else ""))
    }

    // ================================================================
    // Fair life: the gathering, the ribbon, the booth, the errands
    // ================================================================

    /** The gathering claim: during fair hours the town is
     *  AT the fair — unless something bigger holds the
     *  person. Self-guarded exactly the way Crew's claim
     *  is: dinners, jobs, screenings, ceremonies and
     *  finales all outrank a fair, a live case keeps its
     *  causers, and the crew's own hook (later in the
     *  Engine's stack, self-guarded the same way) still
     *  takes a member to the player — the fair never holds
     *  a crewmate against the crew. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        val idx = dayIndex(state)
        if (idx < 0) return null
        val to = if (idx == 6) WATCH_TO else FAIR_TO
        if (timeMin !in FAIR_FROM until to) return null
        if (Dinners.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Jobs.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (MovieNights.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Capstones.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Finales.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Climaxes.isLive(state) &&
            npcName in Climaxes.liveCausers(state)) return null
        val activity = when (idx) {
            0 -> "At the county fair — opening day, ribbon " +
                "freshly cut, midway humming."
            FISH_DAY -> "At the county fair — the fishing " +
                "crowd drifts pondward and back."
            PRODUCE_DAY -> "At the county fair — gathered " +
                "at the produce table, weighing pumpkins " +
                "by eye and lying about it."
            DUNK_DAY -> "At the county fair — gathered at " +
                "the dunk tank, merciless."
            6 -> "At the county fair — closing night, " +
                "staying for the fireworks."
            else -> "At the county fair — booths, bunting, " +
                "and somebody else's funnel smoke."
        }
        return AgendaBlock(FAIRGROUND, activity)
    }

    // ---------------- the ribbon-cutting ----------------

    private fun kCut(year: Int) = "fairCut:$year"

    fun ribbonCut(state: GameState, year: Int): Boolean =
        state.life[kCut(year)] == 1

    /** The opening played beat, judged: cut the ribbon
     *  with the Mayor on opening day. Once per fair. */
    fun cutRibbon(state: GameState): String {
        val w = windowToday(state) ?: return ""
        if (ribbonCut(state, w.year)) return ""
        state.life[kCut(w.year)] = 1
        markAttended(state, w.year)
        Engine().relAdjust(state, "McDaniels", 2)
        note(state, "🎀 The ${w.year} county fair is OPEN — " +
            "you and Mayor McDaniels cut the ribbon together, " +
            "and the town poured in behind you.")
        return "🎀 The scissors are comically large. The " +
            "ribbon is comically red. Mayor McDaniels holds " +
            "one end like a head of state, you hold the other " +
            "like a person who has never cut a ribbon and is " +
            "about to be great at it.\n\nSNIP. The fair is " +
            "open — the booths light up, the wheel creaks its " +
            "first circle, and the whole town pours past you " +
            "into its favorite week of the year."
    }

    // ---------------- the snack booth ----------------

    /** The booth's stock: the town's own pantry food at
     *  honest fair prices (capsule coins). Liane staffs
     *  it — snack royalty takes the booth every year. */
    val BOOTH_KEEPER = "Liane"
    val BOOTH: List<Pair<String, Int>> = listOf(
        "lemonade" to 1,
        "hot_chocolate" to 1,
        "cheesy_poof" to 2,
        "snack_cake" to 2)

    fun boothPrice(itemId: String): Int? =
        BOOTH.firstOrNull { it.first == itemId }?.second

    /** Buy one booth item. Pure over the inventory and
     *  the coin stack; returns the keeper's line, or null
     *  when the coins aren't there. */
    fun buyBooth(state: GameState, itemId: String): String? {
        val price = boothPrice(itemId) ?: return null
        val item = itemById(itemId) ?: return null
        if (Minigames.capsuleCoins(state) < price) return null
        val left = Minigames.capsuleCoins(state) - price
        if (left <= 0) state.inventory.remove(Minigames.COIN_ITEM)
        else state.inventory[Minigames.COIN_ITEM] = left
        state.inventory[itemId] = (state.inventory[itemId] ?: 0) + 1
        markAttended(state, windowToday(state)?.year ?: return null)
        return "${item.icon} One ${item.name.lowercase()}, " +
            "fair price: $price 🪙. Liane slides it over " +
            "with the solemnity of a state dinner."
    }

    // ---------------- the fair errands ----------------

    data class FairErrand(
        val id: String, val day: Int, val asker: String,
        val type: String, val text: String,
        val itemId: String? = null, val targetName: String? = null,
        val targetLoc: String? = null, val fixKind: String? = null
    )

    /** Four authored errands that post across the week —
     *  judged by the errand verbs themselves, paid at the
     *  errand tables, sourced FAIR like EMERG before them. */
    val ERRANDS: List<FairErrand> = listOf(
        FairErrand("ribbon_lemonade", 0, "Wendy", "DELIVER",
            "The ribbon table needs this lemonade before the " +
                "Mayor's speech dries her out entirely. Take " +
                "it to McDaniels — she's the one with the " +
                "scissors and the schedule.",
            itemId = "lemonade", targetName = "McDaniels"),
        FairErrand("lost_cap", 1, "Clyde", "FIND",
            "Okay don't laugh. My lucky bottle cap — the one " +
                "from the GOOD soda — bounced off somewhere in " +
                "the park and the fair is NOT happening for me " +
                "until it's back. It's here. It's mocking me.",
            itemId = "bottle_cap", targetLoc = FAIRGROUND),
        FairErrand("tank_bench", 2, "Jimbo", "FIX",
            "Tank bench wobbles like a newborn deer. Sitter's " +
                "gonna take a dive on DRY LAND if it lets go " +
                "mid-dunk, and then where are we? Fix the " +
                "bench at the park, would ya?",
            targetLoc = FAIRGROUND, fixKind = "bench"),
        FairErrand("poof_run", 3, "Stan", "FETCH",
            "Operation Fair Snacks has one objective: a bag " +
                "of cheesy poofs from the booth, delivered to " +
                "me, before Cartman eats the booth's entire " +
                "stock as a 'supply-chain attack'. Move, soldier.",
            itemId = "cheesy_poof")
    )

    fun errandById(id: String): FairErrand? =
        ERRANDS.firstOrNull { it.id == id }

    /** The scenario [npc] would offer the player right
     *  now: their day has come, the fair is live, you're
     *  both at the fairground in fair hours, and the
     *  errand hasn't been taken this year. */
    fun errandOffer(state: GameState, npc: String): FairErrand? {
        val w = windowToday(state) ?: return null
        val idx = w.dayIndex(todayOrdinal(state))
        if (state.timeMin !in FAIR_FROM until FAIR_TO) return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        if (me.location != FAIRGROUND) return null
        if (state.agents[npc]?.location != FAIRGROUND) return null
        val sc = ERRANDS.firstOrNull {
            it.asker == npc && idx >= it.day
        } ?: return null
        val id = "f${w.year}:${sc.id}"
        if (state.errands.any { it.id == id }) return null
        if (state.errandsRecent.any { it.id == id }) return null
        return sc
    }

    /** Build the scenario's errand record (Errands'
     *  funnels judge it like any other). */
    fun errandFor(state: GameState, sc: FairErrand): Errand {
        val w = windowToday(state)
        val e = Errand(
            id = "f${w?.year ?: state.gameYear}:${sc.id}",
            source = Errands.FAIR,
            asker = sc.asker, type = sc.type, text = sc.text,
            hint = "", itemId = sc.itemId,
            targetName = sc.targetName, targetLoc = sc.targetLoc,
            fixKind = sc.fixKind, dayKey = state.todayKey)
        return e.copy(hint = Errands.hintFor(state, e))
    }

    /** Take a fair errand onto the plate. Returns the
     *  asker's full ask (the caller shows it), or null
     *  when the plate is full. */
    fun acceptErrand(state: GameState, sc: FairErrand): String? {
        if (!Errands.accept(state, errandFor(state, sc))) return null
        windowToday(state)?.let { markAttended(state, it.year) }
        return sc.text
    }

    // ---------------- the tap-person menu ----------------

    const val OPT_RIBBON = "🎀 Cut the ribbon (opening day)"
    const val OPT_BOOTH = "🎡 Fair snack booth…"
    const val OPT_DUNK = "🎯 Dunk tank — take your 3 throws"
    const val OPT_ERRAND_PREFIX = "🎡 Fair errand: "

    /** The fair's option in the tap-person menu, if any
     *  (the Crew pattern: the option only appears when
     *  there's something to do, and the label says what). */
    fun menuOption(state: GameState, npc: String): String? {
        val w = windowToday(state) ?: return null
        val idx = w.dayIndex(todayOrdinal(state))
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        if (me.location != FAIRGROUND) return null
        if (state.agents[npc]?.location != FAIRGROUND) return null
        if (npc == "McDaniels" && idx == 0 &&
            state.timeMin in FAIR_FROM until FAIR_TO &&
            !ribbonCut(state, w.year)) return OPT_RIBBON
        val rec = recordFor(state, w.year)
        if (idx == DUNK_DAY && rec != null && npc == rec.sitter &&
            state.timeMin in FAIR_FROM until FAIR_TO &&
            playerDunk(state, w.year) == null) return OPT_DUNK
        if (npc == BOOTH_KEEPER &&
            state.timeMin in FAIR_FROM until FAIR_TO) return OPT_BOOTH
        errandOffer(state, npc)?.let {
            return OPT_ERRAND_PREFIX + when (it.type) {
                "DELIVER" -> "deliver for ${it.asker}"
                "FIND" -> "find ${it.asker}'s lost property"
                "FIX" -> "fix the dunk tank bench"
                else -> "fetch for ${it.asker}"
            }
        }
        return null
    }

    // ---------------- Look & the poster ----------------

    /** The fair in a Look: the poster before the week
     *  (dates honest, the shift confessed), the week's
     *  running story at the fairground, and the fishing
     *  morning at the pond. The fireworks get their line
     *  from SkyState, like every sky. */
    fun lookText(state: GameState, locId: String): String? {
        val today = todayOrdinal(state)
        val next = FairCalendar.nextWindowAfter(today)
        if (locId == FAIRGROUND) {
            val w = windowToday(state)
            if (w == null) {
                val away = next.startOrd - today
                if (away in 1..ANNOUNCE_AHEAD) {
                    return "🎡 A poster is pasted to the park " +
                        "fence, crooked and proud: COUNTY FAIR — " +
                        "${dateLabelOf(next.startOrd)} to " +
                        "${dateLabelOf(next.endOrd)}, right here. " +
                        "Booths, a dunk tank, a photo contest, " +
                        "and fireworks on closing night." +
                        (if (next.shifted) " (Moved up a week " +
                            "this year — the fair does NOT fight " +
                            "the Regatta for the pond's week.)"
                        else "")
                }
                return null
            }
            val idx = w.dayIndex(today)
            return when (idx) {
                0 -> "🎡 OPENING DAY at the county fair: booths " +
                    "along the paths, bunting over the midway, " +
                    "the wheel turning its first slow circles. " +
                    "The photo contest takes fair frames " +
                    "through day three — shoot the fair while " +
                    "it's loud."
                1 -> "🎡 The fair in full swing. The photo " +
                    "contest still takes fair frames (judged " +
                    "tomorrow morning); the snack booth is " +
                    "doing state business."
                2 -> "🎡 Last day for photo contest frames — " +
                    "the judges take the album at midnight. " +
                    "Tomorrow: the fishing contest at the pond."
                FISH_DAY -> "🎡 Fair day four: half the fair " +
                    "has drifted pondward for the fishing " +
                    "contest — your pond catches today are " +
                    "your entry. The booths hold the park."
                4 -> "🎡 The fair breathes out. Tomorrow " +
                    "the dunk tank takes its chair — " +
                    "${recordFor(state, w.year)?.sitter ?: "somebody"} " +
                    "has volunteered, loudly, to get wet."
                DUNK_DAY -> "🎯 DUNK TANK DAY: " +
                    "${recordFor(state, w.year)?.sitter ?: "the sitter"} " +
                    "is on the chair and the line is long. " +
                    "Talk to them at the tank to take your " +
                    "three throws."
                else -> "🎡 CLOSING DAY at the county fair: " +
                    "last rides, last snacks, ribbons coming " +
                    "down off the table — and tonight, when " +
                    "it gets dark, the whole town stays for " +
                    "the fireworks."
            }
        }
        if (locId == POND) {
            val w = windowToday(state) ?: return null
            if (w.dayIndex(today) == FISH_DAY) {
                return "🎣 The county fair's fishing contest " +
                    "runs here today: one table, one field, " +
                    "your best catch is your entry. Rarer " +
                    "beats bigger. A boot is not a fish."
            }
        }
        return null
    }

    // ---------------- the announcement & the phone ----------------

    /** The fair the town is counting down to has its
     *  announcement morning today. */
    fun announceDue(state: GameState): Boolean {
        val today = todayOrdinal(state)
        val next = FairCalendar.nextWindowAfter(today)
        if (next.startOrd - today != ANNOUNCE_AHEAD) return false
        return state.life["fairPhoned:${next.year}"] != 1
    }

    fun markPhoned(state: GameState, year: Int) {
        state.life["fairPhoned:$year"] = 1
    }

    fun announcementLine(w: FairCalendar.FairWindow): String =
        "🎡 The county fair comes to Kid's Park in three " +
            "days — ${dateLabelOf(w.startOrd)} to " +
            "${dateLabelOf(w.endOrd)}: booths, contests, a " +
            "dunk tank, and fireworks on closing night." +
            (if (w.shifted) " (Moved up a week this year " +
                "so it doesn't fight the Regatta.)" else "")

    // ---------------- attendance ----------------

    private fun kAttended(year: Int) = "fairAttended:$year"

    fun markAttended(state: GameState, year: Int) {
        state.life[kAttended(year)] = 1
    }

    fun attended(state: GameState, year: Int): Boolean =
        state.life[kAttended(year)] == 1

    // ================================================================
    // The rollover (Engine.onNewDay calls this before the phone,
    // so the fair's morning facts are textable by breakfast)
    // ================================================================

    fun dailyRollover(state: GameState) {
        val today = todayOrdinal(state)
        // ---- the announcement, three mornings out ----
        val next = FairCalendar.nextWindowAfter(today)
        if (next.startOrd - today == ANNOUNCE_AHEAD &&
            state.life["fairAnn:${next.year}"] != 1) {
            state.life["fairAnn:${next.year}"] = 1
            note(state, announcementLine(next))
            townLogLine(state, "🎡 County fair announced — " +
                "${dateLabelOf(next.startOrd)}–" +
                "${dateLabelOf(next.endOrd)} at Kid's Park")
        }
        val w = windowToday(state)
        // ---- after closing: the wrap (the first rollover
        // past the week, whenever the game next wakes —
        // a slept-through month still wraps honestly) ----
        if (w == null) {
            val y = keyFromOrdinal(today)
                .substring(0, 4).toIntOrNull() ?: return
            for (yy in y downTo (y - 1)) {
                val cand = FairCalendar.windowFor(yy)
                val rec = recordFor(state, yy)
                if (today > cand.endOrd && rec != null &&
                    !rec.wrapped) {
                    wrapFair(state, cand)
                    break
                }
            }
            return
        }
        // ---- opening morning: the record is born ----
        val idx = w.dayIndex(today)
        if (idx == 0 && recordFor(state, w.year) == null) {
            putRecord(state, FairRecord(
                year = w.year, shifted = w.shifted,
                sitter = sitterFor(w.year)))
            note(state, "🎡 THE COUNTY FAIR OPENS at Kid's " +
                "Park — a week of booths, contests and " +
                "questionable prizes. The ribbon-cutting " +
                "waits on the Mayor, at the park, today." +
                (if (w.shifted) " (A week early this year — " +
                    "the fair yields the pond's last week " +
                    "to the Regatta.)" else ""))
            townLogLine(state, "🎡 County fair (${w.year}) — " +
                "${state.gameMonth}/${state.gameDay}")
        }
        // ---- the contests judge on their mornings ----
        if (idx == FISH_DAY &&
            state.life["fairPhotoJudged:${w.year}"] != 1 &&
            recordFor(state, w.year) != null) {
            judgePhoto(state, w)
        }
        if (idx == FISH_DAY + 1 &&
            state.life["fairFishJudged:${w.year}"] != 1 &&
            recordFor(state, w.year) != null) {
            judgeFish(state, w)
        }
        // v0.11.0b: the produce weigh-in judges on the
        // dunk morning — entries (the porch specimens)
        // closed with the weigh-in day itself.
        if (idx == DUNK_DAY &&
            state.life["fairProduceJudged:${w.year}"] != 1 &&
            recordFor(state, w.year) != null) {
            judgeProduce(state, w)
        }
        // ---- the week's morning lines ----
        if (recordFor(state, w.year) != null) {
            when (idx) {
                FISH_DAY -> note(state, "🎣 Fair fishing day: " +
                    "the contest runs at Stark's Pond — your " +
                    "catches there today are your entry.")
                PRODUCE_DAY -> note(state, "🎃 Produce " +
                    "weigh-in day at the fair: the giant " +
                    "pumpkins come to the table. Your entry " +
                    "is the season's biggest from your own " +
                    "garden, kept on the porch — judged " +
                    "tomorrow off the record it grew by.")
                DUNK_DAY -> note(state, "🎯 Dunk tank day at " +
                    "the fair: " +
                    "${recordFor(state, w.year)?.sitter} takes " +
                    "the chair at Kid's Park. Three throws. " +
                    "Make them count.")
                6 -> note(state, "🎡 Closing day at the " +
                    "county fair — and tonight, fireworks " +
                    "over the park. The whole town stays.")
            }
        }
    }

    /** The morning after closing: the dunk settles, the
     *  year's record closes — attended, or honestly
     *  missed — and the dressing's strike is this line:
     *  the tokens are a pure function of the week, so the
     *  fair is gone from the park the moment the calendar
     *  turns, and the record says who was there. */
    fun wrapFair(state: GameState, w: FairCalendar.FairWindow) {
        val rec = recordFor(state, w.year) ?: return
        if (rec.wrapped) return
        // a fair the game slept through still completes:
        // any contest whose judging morning was skipped
        // judges now, off the same records and seeds, so
        // the wrapped record is whole either way.
        if (state.life["fairPhotoJudged:${w.year}"] != 1) {
            judgePhoto(state, w)
        }
        if (state.life["fairFishJudged:${w.year}"] != 1) {
            judgeFish(state, w)
        }
        // v0.11.0b: a slept-through fair still weighs its
        // pumpkins — the specimen was the entry all along.
        if (state.life["fairProduceJudged:${w.year}"] != 1) {
            judgeProduce(state, w)
        }
        settleDunk(state, w)
        val after = recordFor(state, w.year) ?: rec
        val wasThere = attended(state, w.year) ||
            after.playerPhoto > 0 || after.playerFish > 0 ||
            after.playerDunk > 0 || after.playerProduce > 0 ||
            Garden.fairSpecimen(state, w.year) != null ||
            playerPhotoEntry(state, w) != null ||
            playerFishEntry(state, w.year) != null ||
            playerDunk(state, w.year) != null
        putRecord(state, after.copy(
            wrapped = true,
            attended = wasThere,
            missed = !wasThere))
        note(state, if (wasThere)
            "🎡 The fair packs up in the dawn — booths down, " +
                "wheel still, bunting in a bin. The ribbons " +
                "stay where they were won. See the Journal's " +
                "🎡 COUNTY FAIR page."
        else
            "🎡 The fair packs up in the dawn — booths down, " +
                "wheel still. You missed the fair of " +
                "${w.year} entirely; the town ate the snacks " +
                "without you, and the Journal kept the receipts.")
        townLogLine(state, "🎡 County fair (${w.year}) — wrapped" +
            (if (wasThere) "" else " (you missed it)"))
    }

    // ================================================================
    // Dressing (the render bridge: TownInfo carries this)
    // ================================================================

    /** The fair's dressing tokens — registered in Depth at
     *  BACKGROUND with the dig and mark layers. Drawn as
     *  simple shapes only: a wheel's rim and spokes, booth
     *  boxes with striped awnings, pennant strings, the
     *  tank. No clustered patterns, ever. */
    val FAIR_TOKENS: Set<String> = setOf(
        "fair-ferris", "fair-booths", "fair-bunting", "fair-dunk-tank")

    /** locId -> tokens, live only while the week stands:
     *  the morning after closing this is empty everywhere,
     *  which IS the strike. */
    fun fairTokensByLoc(state: GameState): Map<String, List<String>> {
        if (!isFairWeek(state)) return emptyMap()
        return mapOf(FAIRGROUND to FAIR_TOKENS.sorted())
    }

    // ================================================================
    // The Journal page
    // ================================================================

    private fun placeLine(emoji: String, label: String,
                          places: List<FairPlace>,
                          playerPlace: Int): List<String> {
        if (places.isEmpty()) return listOf(
            "$emoji $label — not judged yet.")
        val out = mutableListOf("$emoji $label:")
        places.forEachIndexed { i, p ->
            out += "   ${RIBBONS[i]} ribbon — ${p.name}: ${p.detail}"
        }
        if (playerPlace in 1..3) {
            out += "   → your ${RIBBONS[playerPlace - 1]} ribbon. 🏆"
        }
        return out
    }

    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val today = todayOrdinal(state)
        val next = FairCalendar.nextWindowAfter(today)
        val live = windowToday(state)
        out += "🎡 One week every summer — third Saturday " +
            "of August, Kid's Park (a week early in years " +
            "it would fight the Regatta)."
        if (live != null) {
            val idx = live.dayIndex(today)
            out += "🎡 THE FAIR IS ON — day ${idx + 1} of 7 " +
                "(${dateLabelOf(live.startOrd)}–" +
                "${dateLabelOf(live.endOrd)}). Gates 10–8" +
                (if (idx == 6) ", fireworks tonight" else "") + "."
        } else if (next.startOrd > today) {
            out += "🎡 Next fair: ${dateLabelOf(next.startOrd)}–" +
                "${dateLabelOf(next.endOrd)}, ${next.year}" +
                (if (next.shifted) " (moved up a week — the " +
                    "Regatta keeps the pond's last week)" else "") + "."
        }
        val records = state.fairs.sortedByDescending { it.year }
        if (records.isEmpty()) {
            out += "No fair has come since you moved to town. " +
                "The poster goes up three days before opening — " +
                "watch the park fence."
            return out
        }
        val newest = records.first()
        out += "—— the fair of ${newest.year}" +
            (if (newest.shifted) " (a week early)" else "") +
            (if (newest.missed) " —— you missed it" else "") + " ——"
        if (newest.missed) {
            out += "The town went without you: the booths, " +
                "the tank, the fireworks — all of it, " +
                "unwitnessed. The results stand anyway:"
        }
        out += placeLine("📸", "Photo contest (judged by " +
            "${newest.photoJudge.ifEmpty { "the yearbook desk" }})",
            newest.photoPlaces, newest.playerPhoto)
        if (newest.photoNote.isNotEmpty()) {
            out += "   “${newest.photoNote}”"
        }
        out += placeLine("🎣", "Fishing contest (Stark's Pond)",
            newest.fishPlaces, newest.playerFish)
        out += placeLine("🎯", "Dunk tank (${newest.sitter} " +
            "in the chair)", newest.dunkPlaces, newest.playerDunk)
        // v0.11.0b: the fourth table — judged off the
        // garden's own growth record.
        out += placeLine("🎃", "Produce weigh-in (the giant " +
            "pumpkins, judged off the garden records)",
            newest.producePlaces, newest.playerProduce)
        val blues = blueRibbons(state)
        if (blues > 0) {
            out += "🏆 Your blue ribbons, lifetime: $blues — " +
                "the first one started the ribbon display " +
                "at home, by the yearbook shelf."
        }
        for (elder in records.drop(1)) {
            out += indexLine(elder)
        }
        return out
    }

    /** An elder fair, one line: the year, the contests'
     *  blues, and whether you were there. (The produce
     *  winner joins the line with v0.11.0b — a record from
     *  before it simply has no weigh-in to name.) */
    fun indexLine(rec: FairRecord): String {
        fun winner(places: List<FairPlace>): String =
            places.firstOrNull()?.name ?: "nobody"
        return "🎡 ${rec.year}: photo — ${winner(rec.photoPlaces)} · " +
            "fishing — ${winner(rec.fishPlaces)} · dunk — " +
            "${winner(rec.dunkPlaces)}" +
            (if (rec.producePlaces.isNotEmpty())
                " · produce — ${winner(rec.producePlaces)}" else "") +
            (if (rec.missed) " · (you missed it)"
             else if (rec.blues() > 0) " · ${rec.blues()} blue(s) yours"
             else "")
    }
}

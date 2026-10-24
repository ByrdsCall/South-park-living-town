package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// The Winter Carnival — v0.11.0e (Queue #11 item 5): the
// county fair's winter counterpart, one week in deep
// winter at Stark's Pond.
//
// THE WEEK (CarnivalCalendar, pure calendar math like
// FairCalendar): the carnival opens on the THIRD SATURDAY
// of January and runs seven days, Saturday to Friday, on
// the frozen pond. Deep January is the town's emptiest
// week, and the calendar proves it needs no shift: the
// suite sweeps four decades of REAL calendar dates in ONE
// ordinal space and finds the carnival week disjoint from
// every season-finale window (the winter finale owns the
// last week of February), every holiday window on the
// town's books (Christmas, New Year, Valentine's, all of
// them) and the summer fair's week — the polish-#10
// lesson, applied at design time: two rhythms computed
// in different day-spaces can share a day for years
// without anyone noticing, so this sweep compares
// everything in the one space the town actually lives.
//
// THE CONTESTS (two, on rails the town already trusts):
//   🎣 ICE FISHING (day 3, judged next morning): your
//      pond catches that day funnel in through
//      applyCatch (your best counts), exactly the fair's
//      fishing discipline — the field (Jimbo, the rival
//      when crowned, the seeded draw) each make ONE cast
//      on the real FISH_TABLE with their authored luck,
//      ranked by Rival.fishRank, then size. When both you
//      and the rival hold entries, the result lands on the
//      🆚 ledger through Rival.recordResult ONCE — unless
//      a rival fishing duel is already armed, in which
//      case the duel keeps the ledger and the carnival
//      keeps its page. Never double-counted.
//   ⛄ SNOW SCULPTURE (build days 0–2, judged the
//      morning of day 3): you build YOUR piece in played
//      sessions at the pond — each session is half an
//      hour of real clock and is filed with the weather
//      it was built under, off the sky the town actually
//      had. The stated formula: 12 a session, +10 for
//      each further day you came back, and per session
//      +6 in falling snow (it packs), +2 under a grey
//      sky, −7 in sun (it melts). The field's pieces are
//      seeded onto the same scale. The judge is the
//      yearbook masthead's own rotation, and their note
//      only ever claims facts the record proves — your
//      sessions, your days, your weather — or, for a
//      town winner, contest facts only.
//
// HOT DRINKS: the carnival's kettle crowd is real foot
// traffic, and a stand pitched at the pond (Stand's own
// pitch list) sells into it through Stand's ordinary
// tick — presence is the claim, the crate is the crate,
// prices are the prices you set. One stated synergy on
// top: hot chocolate at a pond pitch during carnival
// week buys at COCOA_RUSH × the ordinary chance. No
// invented economy anywhere: no carnival currency, no
// carnival prices, no conjured customers.
//
// CARNIVAL LIFE: carnival hours (10:00–20:00, to 21:30
// on closing night for the lantern skate) bend agendas
// to the pond as a gathering claim seated in Engine's
// stack exactly where the fair's sits — right after the
// fair — self-guarded against dinners, jobs, screenings,
// ceremonies, finales, the fair itself and live case
// work, the way the fair's claim is. Opening morning
// offers a played beat: drilling the first ice hole with
// Jimbo.
//
// RECORDS: one CarnivalRecord per year, serialized into
// lifeText (the Stand crate's discipline — no new
// GameState field, no new Prefs key), written at the
// opening, filled as contests judge, wrapped the morning
// after closing: attended honestly, or missed honestly.
// The Journal page shows the newest carnival whole and
// elders as one lines, the fair page's own pattern.
// ============================================================

/** One contest placing: who, with what, at what score —
 *  the fair's FairPlace shape, carried in lifeText. */
data class CarnPlace(
    val name: String,
    val detail: String,
    val score: Int
)

/** One year's carnival, whole: the wrap facts, both
 *  contests' places, the sculpture judge and their note,
 *  and where YOU placed (0 = unplaced/unentered). */
data class CarnivalRecord(
    val year: Int,
    val wrapped: Boolean = false,
    val attended: Boolean = false,
    val missed: Boolean = false,
    val fishPlaces: List<CarnPlace> = emptyList(),
    val sculptPlaces: List<CarnPlace> = emptyList(),
    val sculptJudge: String = "",
    val sculptNote: String = "",
    val playerFish: Int = 0,
    val playerSculpt: Int = 0
)

// ============================================================
// CarnivalCalendar — the week's pure calendar math, in the
// same ordinal tongue FairCalendar speaks, so the sweep
// can hold both weeks in one space.
// ============================================================
object CarnivalCalendar {

    data class CarnWindow(
        val year: Int,
        val startOrd: Int
    ) {
        val endOrd: Int get() = startOrd + 6
        val closingOrd: Int get() = endOrd
        fun dayIndex(ord: Int): Int = ord - startOrd
        fun contains(ord: Int): Boolean = ord in startOrd..endOrd
        fun startKey(): String = keyFromOrdinal(startOrd)
        fun endKey(): String = keyFromOrdinal(endOrd)
    }

    /** The Saturday the carnival opens on: the third
     *  Saturday of January (it falls Jan 15–21, so the
     *  week always ends by Jan 27 — deep winter, clear of
     *  everything February owns; the suite proves it). */
    fun thirdSaturdayOrdinal(year: Int): Int {
        val jan1 = ordinalOfKey("%04d0101".format(year))
        val toSaturday = (6 - FairCalendar.dowOfOrdinal(jan1) + 7) % 7
        return jan1 + toSaturday + 14
    }

    /** [year]'s carnival week. No shift rule exists —
     *  unlike the fair, this week is never within three
     *  weeks of anything it could fight, and the sweep
     *  in the suite is the standing proof. */
    fun windowFor(year: Int): CarnWindow =
        CarnWindow(year, thirdSaturdayOrdinal(year))

    /** The carnival week containing [ord], if any. */
    fun windowAtOrdinal(ord: Int): CarnWindow? {
        if (ord <= 0) return null
        val y = keyFromOrdinal(ord).substring(0, 4).toIntOrNull()
            ?: return null
        for (yy in (y - 1)..(y + 1)) {
            val w = windowFor(yy)
            if (w.contains(ord)) return w
        }
        return null
    }

    /** The next carnival whose week hasn't fully passed —
     *  the one the poster counts down to. */
    fun nextWindowAfter(ord: Int): CarnWindow {
        val y = keyFromOrdinal(ord).substring(0, 4).toIntOrNull()
            ?: 2026
        for (yy in y..(y + 2)) {
            val w = windowFor(yy)
            if (w.endOrd >= ord) return w
        }
        return windowFor(y + 3)
    }
}

object Carnival {

    // ---------------- the dials (all suite-pinned) ----------------

    /** The carnival ground: the frozen pond. The ice
     *  takes the holes, the shore takes the sculpture
     *  row, and the whole town takes the week. */
    const val GROUND = "StarksPond"

    /** Carnival hours: the lanterns are lit 10:00–20:00. */
    const val CARN_FROM = 10 * 60
    const val CARN_TO = 20 * 60

    /** Closing night, the town stays for the lantern skate. */
    const val SKATE_TO = 21 * 60 + 30

    /** Days before opening that the poster goes up, the
     *  ticker calls it, and the Mayor texts it: three. */
    const val ANNOUNCE_AHEAD = 3

    /** The ice fishing contest's one day, on the ice. */
    const val FISH_DAY = 3

    /** Sculpture build days: the week's first three
     *  (0–2). Judging lands the morning of day 3, beside
     *  the fishing crowd. */
    const val SCULPT_LAST_DAY = 2

    /** Build sessions a day allows, and the real clock
     *  each one costs (the Activity spends the minutes;
     *  the record only ever counts filed sessions). */
    const val SCULPT_PER_DAY = 2
    const val SCULPT_MINUTES = 30

    /** The sculpture formula, stated whole: points per
     *  filed session, the comeback bonus per further day
     *  worked, and the per-session weather read — snow
     *  packs (+), grey holds (+ small), sun melts (−). */
    const val SCULPT_PER_SESSION = 12
    const val SCULPT_DAY_BONUS = 10
    const val SCULPT_WX_SNOW = 6
    const val SCULPT_WX_GREY = 2
    const val SCULPT_WX_SUN = -7

    /** The seeded fields (the player is never in these
     *  counts — they're the town you're up against). */
    const val FISH_FIELD = 6
    const val SCULPT_FIELD = 6

    /** A town sculptor's seeded score band, on the
     *  player's own scale: a fully worked piece (six
     *  sessions across three snowy days, 128) can top
     *  the whole field; a one-session piece can't. */
    const val SCULPT_NPC_BASE = 20
    const val SCULPT_NPC_SPREAD = 110

    /** The hot-drinks synergy: hot chocolate at a pond
     *  pitch, carnival week, buys at this × the stand's
     *  ordinary purchase chance. Everything else about
     *  the sale is Stand's ordinary discipline. */
    const val COCOA_RUSH = 1.6
    const val COCOA_ITEM = "hot_chocolate"

    // ---------------- record storage (lifeText, the crate's way) ----

    private fun kRec(year: Int) = "carnRec:$year"

    /** Field/record separators for the serialized record.
     *  Authored text never carries them; player-supplied
     *  names are scrubbed of them on write. */
    private fun scrub(s: String): String =
        s.replace("~", "/").replace("|", "/").replace(";", ",")

    fun recordFor(state: GameState, year: Int): CarnivalRecord? {
        val raw = state.lifeText[kRec(year)] ?: return null
        val f = raw.split("~")
        if (f.size < 10) return null
        fun places(s: String): List<CarnPlace> =
            if (s.isEmpty()) emptyList()
            else s.split(";").mapNotNull { e ->
                val p = e.split("|")
                if (p.size != 3) null
                else CarnPlace(p[0], p[1],
                    p[2].toIntOrNull() ?: 0)
            }
        return CarnivalRecord(
            year = f[0].toIntOrNull() ?: return null,
            wrapped = f[1] == "1",
            attended = f[2] == "1",
            missed = f[3] == "1",
            sculptJudge = f[4],
            sculptNote = f[5],
            playerFish = f[6].toIntOrNull() ?: 0,
            playerSculpt = f[7].toIntOrNull() ?: 0,
            fishPlaces = places(f[8]),
            sculptPlaces = places(f[9]))
    }

    private fun putRecord(state: GameState, rec: CarnivalRecord) {
        fun places(l: List<CarnPlace>): String =
            l.joinToString(";") {
                "${scrub(it.name)}|${scrub(it.detail)}|${it.score}" }
        state.lifeText[kRec(rec.year)] = listOf(
            rec.year.toString(),
            if (rec.wrapped) "1" else "0",
            if (rec.attended) "1" else "0",
            if (rec.missed) "1" else "0",
            scrub(rec.sculptJudge), scrub(rec.sculptNote),
            rec.playerFish.toString(), rec.playerSculpt.toString(),
            places(rec.fishPlaces), places(rec.sculptPlaces)
        ).joinToString("~")
    }

    /** Every year's record on the save, newest first —
     *  the page reads these; years with no carnival week
     *  lived under this build simply have none. */
    fun records(state: GameState): List<CarnivalRecord> {
        val years = state.lifeText.keys
            .filter { it.startsWith("carnRec:") }
            .mapNotNull { it.removePrefix("carnRec:").toIntOrNull() }
        return years.distinct()
            .mapNotNull { recordFor(state, it) }
            .sortedByDescending { it.year }
    }

    // ---------------- the calendar, from a state's chair ------------

    fun todayOrdinal(state: GameState): Int =
        ordinalOfKey(state.todayKey)

    fun windowToday(state: GameState): CarnivalCalendar.CarnWindow? =
        CarnivalCalendar.windowAtOrdinal(todayOrdinal(state))

    fun isCarnivalWeek(state: GameState): Boolean =
        windowToday(state) != null

    /** 0 = opening Saturday … 6 = closing Friday, or -1
     *  outside the week. */
    fun dayIndex(state: GameState): Int =
        windowToday(state)?.dayIndex(todayOrdinal(state)) ?: -1

    fun inCarnivalHours(state: GameState): Boolean {
        val idx = dayIndex(state)
        if (idx < 0) return false
        val to = if (idx == 6) SKATE_TO else CARN_TO
        return state.timeMin in CARN_FROM until to
    }

    fun liveRecord(state: GameState): CarnivalRecord? =
        windowToday(state)?.let { recordFor(state, it.year) }

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

    // ================================================================
    // The ice fishing contest — the fair's discipline, on ice
    // ================================================================

    private fun kFishBest(year: Int) = "carnFishBest:$year"

    /** applyCatch's funnel (Minigames calls this beside
     *  Fair.onCatch): on ice fishing day, a pond catch is
     *  a contest cast — your best (rank, then size) stands
     *  as your entry. Nothing else about the catch changes. */
    fun onCatch(state: GameState, sp: FishSpecies, cm: Int) {
        val w = windowToday(state) ?: return
        if (w.dayIndex(todayOrdinal(state)) != FISH_DAY) return
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return
        if (me.location != GROUND) return
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
            note(state, "🎣 Carnival ice fishing: your ${sp.name} " +
                "($cm cm) comes up through the ice as your " +
                "entry — the table has it.")
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

    /** The fishing field: Jimbo (he owns the auger and
     *  the opinion), the rival when one is crowned, and
     *  the seeded draw — each making exactly one cast. */
    fun fishField(state: GameState, year: Int): List<String> {
        val must = mutableListOf("Jimbo")
        Rival.rivalOf(state)?.let { must += it }
        return Fair.fieldFor(year, "carnfish", FISH_FIELD + must.size,
            must).take(FISH_FIELD + must.size)
    }

    /** One field cast on the real FISH_TABLE, seeded off
     *  the carnival's own January base — the same cast
     *  every time the question is asked. */
    private fun fieldCast(year: Int, name: String):
            Pair<FishSpecies, Int> =
        Fair.castFor(name,
            dailySeed(keyOf(year, 1, 1)) + name.hashCode() * 331L)

    /** Judge the ice fishing contest. Pure over the filed
     *  entry + the seeded field; pays the player's coins
     *  if they placed. Runs once (rollover-guarded). */
    fun judgeFish(state: GameState, w: CarnivalCalendar.CarnWindow) {
        val rec = recordFor(state, w.year) ?: return
        if (state.life["carnFishJudged:${w.year}"] == 1) return
        state.life["carnFishJudged:${w.year}"] = 1
        val contenders = mutableListOf<CarnPlace>()
        val mine = playerFishEntry(state, w.year)
        if (mine != null) {
            contenders += CarnPlace(playerName(state),
                "${mine.first.name}, ${mine.second} cm",
                Fair.fishScore(mine.first, mine.second))
        }
        val field = fishField(state, w.year)
        for (name in field) {
            val (sp, cm) = fieldCast(w.year, name)
            contenders += CarnPlace(name,
                "${sp.name}, $cm cm", Fair.fishScore(sp, cm))
        }
        val places = contenders
            .sortedWith(compareBy<CarnPlace> {
                1000 - (it.score / 1000) }
                .thenByDescending { it.score }
                .thenBy { it.name })
            .take(3)
        val playerPlace =
            if (mine == null) 0
            else places.indexOfFirst { it.name == playerName(state) } + 1
        if (playerPlace in 1..3) {
            Minigames.awardCoins(state, Fair.PLACE_COINS[playerPlace - 1])
        }
        putRecord(state, rec.copy(
            fishPlaces = places, playerFish = playerPlace))
        if (mine != null) markAttended(state, w.year)
        val winner = places.firstOrNull()
        note(state, "🎣 Carnival ice fishing: " +
            (winner?.let { "${it.name} takes the blue with " +
                "${it.detail}." } ?: "the ice kept its secrets.") +
            (if (playerPlace in 1..3)
                " You placed ${Fair.ordinalWord(playerPlace)} — " +
                    "a ${Fair.RIBBONS[playerPlace - 1]} ribbon!" else ""))
        // the 🆚 ledger: a carnival result is a contest
        // result when both rivals hold entries — recorded
        // through Rival's own path, unless an armed duel
        // already owns the fishing story. Never twice.
        val rival = Rival.rivalOf(state)
        if (mine != null && rival != null && rival in field &&
            Rival.pendingKind(state) != Rival.K_FISHING &&
            state.life["carnLedger:${w.year}"] != 1) {
            val (rsp, rcm) = fieldCast(w.year, rival)
            val winnerKey = when {
                Rival.fishRank(mine.first) != Rival.fishRank(rsp) ->
                    if (Rival.fishRank(mine.first) <
                        Rival.fishRank(rsp)) "you" else "them"
                mine.second != rcm ->
                    if (mine.second > rcm) "you" else "them"
                else -> "draw"
            }
            state.life["carnLedger:${w.year}"] = 1
            Rival.recordResult(state, Rival.K_FISHING, winnerKey,
                "the winter carnival ice fishing — your " +
                    "${mine.first.name} (${mine.second} cm) vs " +
                    "their ${rsp.name} ($rcm cm)")
        }
    }

    // ================================================================
    // The snow-sculpture contest — built by hand, judged like
    // the yearbook: a stated formula over real records
    // ================================================================

    private fun kBuild(year: Int) = "carnBuild:$year"

    /** The filed build sessions for [year]: (day index,
     *  weather id) pairs, in the order the work happened.
     *  The weather is the sky the town actually had at the
     *  session — filed at the session, never re-read. */
    fun buildSessions(state: GameState,
                      year: Int): List<Pair<Int, String>> {
        val raw = state.lifeText[kBuild(year)] ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { e ->
            val p = e.split(":")
            val d = p.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val w = p.getOrNull(1) ?: return@mapNotNull null
            d to w
        }
    }

    fun sessionsToday(state: GameState): Int {
        val w = windowToday(state) ?: return 0
        val idx = w.dayIndex(todayOrdinal(state))
        return buildSessions(state, w.year).count { it.first == idx }
    }

    /** A build session is on the table right now: carnival
     *  week, a build day, carnival hours, you're at the
     *  pond, and today's sessions aren't spent. */
    fun buildOpen(state: GameState): Boolean {
        val idx = dayIndex(state)
        if (idx < 0 || idx > SCULPT_LAST_DAY) return false
        if (!inCarnivalHours(state)) return false
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return false
        if (me.location != GROUND || me.asleep) return false
        return sessionsToday(state) < SCULPT_PER_DAY
    }

    /** The weather's read on one session, by the stated
     *  formula: falling snow packs, a grey sky holds, sun
     *  melts, and the rest is weather. */
    fun weatherAdj(weatherId: String): Int = when (weatherId) {
        "light_snow", "heavy_snow", "blizzard" -> SCULPT_WX_SNOW
        "cloudy", "partly_cloudy", "fog", "wind" -> SCULPT_WX_GREY
        "sunny" -> SCULPT_WX_SUN
        else -> 0
    }

    /** The stated score of a filed build: sessions, the
     *  comeback bonus, and the weather the work survived —
     *  every term read off the record itself. */
    fun sculptScore(sessions: List<Pair<Int, String>>): Int {
        if (sessions.isEmpty()) return 0
        val days = sessions.map { it.first }.distinct().size
        val wx = sessions.sumOf { weatherAdj(it.second) }
        return (SCULPT_PER_SESSION * sessions.size +
            SCULPT_DAY_BONUS * (days - 1).coerceAtLeast(0) + wx)
            .coerceAtLeast(0)
    }

    fun playerSculptScore(state: GameState, year: Int): Int =
        sculptScore(buildSessions(state, year))

    /** File one build session under the sky as it stands.
     *  The Activity spends the real minutes; this only
     *  ever counts work the record proves. Returns the
     *  filed line, or null when no session is on the table. */
    fun buildSession(state: GameState): String? {
        if (!buildOpen(state)) return null
        val w = windowToday(state) ?: return null
        val idx = w.dayIndex(todayOrdinal(state))
        val sessions = buildSessions(state, w.year) +
            (idx to state.weatherId)
        state.lifeText[kBuild(w.year)] = sessions.joinToString(",") {
            "${it.first}:${it.second}" }
        markAttended(state, w.year)
        val score = sculptScore(sessions)
        val wxLine = when (weatherAdj(state.weatherId)) {
            SCULPT_WX_SNOW -> "The falling snow packs as you work — " +
                "the good stuff, the kind that holds a shape."
            SCULPT_WX_SUN -> "The sun is out and the edges are " +
                "already softening. You carve faster and meaner."
            SCULPT_WX_GREY -> "A grey, holding sky — the snow " +
                "keeps every edge you give it."
            else -> "The sky does its thing; you do yours."
        }
        return "⛄ Session ${sessions.size} on the piece is filed — " +
            "half an hour of real carving on the shore row. " +
            "$wxLine Running score by the stated formula: " +
            "$score (12 a session, +$SCULPT_DAY_BONUS a day you " +
            "come back, the weather read per session). Judging " +
            "is the morning of day four."
    }

    /** A town sculptor's seeded score: per (year, entrant),
     *  onto the player's own scale — the same every time
     *  the question is asked. */
    fun npcSculptScore(year: Int, name: String): Int {
        val rnd = Random(dailySeed(keyOf(year, 1, 1)) +
            name.hashCode().toLong() * 719L)
        return SCULPT_NPC_BASE + rnd.nextInt(SCULPT_NPC_SPREAD)
    }

    fun sculptJudgeFor(year: Int): Yearbook.Editor =
        Yearbook.judgeFor(year, 4)

    /** The judge-note facts for a winning build — every
     *  slot is a fact the filed record proves: the count
     *  of sessions, the days worked, the weather held. */
    private data class SculptFacts(
        val winner: String, val sessions: Int, val days: Int,
        val snowy: Int, val sunny: Int, val score: Int)

    private fun fillSculpt(t: String, f: SculptFacts): String = t
        .replace("{winner}", f.winner)
        .replace("{sessions}", f.sessions.toString())
        .replace("{days}", f.days.toString())
        .replace("{snowy}", f.snowy.toString())
        .replace("{sunny}", f.sunny.toString())
        .replace("{score}", f.score.toString())

    /** Wendy's desk: the straight note. */
    private val SCULPT_NOTES_WENDY: List<String> = listOf(
        "Judged on the work, not the weather report — except " +
            "the weather IS in the work: {sessions} sessions " +
            "across {days} days, {snowy} of them in falling " +
            "snow. That's a sculpture with a schedule. " +
            "({score} points.)",
        "The winner earned it in installments: {sessions} " +
            "sessions, {days} days of showing up. Snow " +
            "sculpture is a attendance sport. ({score} points.)")

    /** Jimmy's desk: the showman's note. */
    private val SCULPT_NOTES_JIMMY: List<String> = listOf(
        "What a terrific build. {sessions} sessions! {days} " +
            "days! The crowd watched this one grow like a " +
            "very cold garden. {score} points.",
        "I've announced a lot of winners, and — wow, what a " +
            "great audience — this one was carved in public, " +
            "{sessions} sessions deep. {score} points.")

    /** Cartman's desk: the critic endures. */
    private val SCULPT_NOTES_CARTMAN: List<String> = listOf(
        "Fine. FINE. {winner} carved a good snow thing. " +
            "{sessions} sessions, {days} days, {score} " +
            "points. Mine melted on purpose. It's called " +
            "ephemeral art. Look it up.",
        "The 'winner' is {winner} with {score} points. " +
            "{sunny} sunny sessions and it STILL won — the " +
            "sun was collaborating with the enemy and it " +
            "didn't matter. Whatever.")

    /** A town winner's note: only contest facts — the
     *  winner's name, the row, the field they beat. Their
     *  process is theirs; the note invents nothing. */
    private val SCULPT_NOTES_NPC: List<String> = listOf(
        "{winner} takes it — the shore-row piece the whole " +
            "field chased all week. {score} points, and the " +
            "judges' table doesn't argue.",
        "The blue goes to {winner}: {score} points on the " +
            "shore row, top of a field of ${SCULPT_FIELD + 1}. " +
            "The town has walked past it all week. It's the " +
            "carnival, standing still.")

    /** Judge the sculpture contest for [w]'s carnival.
     *  Pure over the filed build + the seeded field; pays
     *  the player's coins if they placed. Runs once. */
    fun judgeSculpt(state: GameState, w: CarnivalCalendar.CarnWindow) {
        val rec = recordFor(state, w.year) ?: return
        if (state.life["carnSculptJudged:${w.year}"] == 1) return
        state.life["carnSculptJudged:${w.year}"] = 1
        val judge = sculptJudgeFor(w.year)
        val field = Fair.fieldFor(w.year, "carnsculpt", SCULPT_FIELD)
        val contenders = mutableListOf<CarnPlace>()
        val sessions = buildSessions(state, w.year)
        val myScore = sculptScore(sessions)
        if (sessions.isNotEmpty()) {
            contenders += CarnPlace(playerName(state),
                "your piece: ${sessions.size} session(s) across " +
                    "${sessions.map { it.first }.distinct().size} " +
                    "day(s) on the shore row", myScore)
        }
        for (name in field) {
            contenders += CarnPlace(name,
                "their piece on the shore row",
                npcSculptScore(w.year, name))
        }
        val places = contenders
            .sortedWith(compareByDescending<CarnPlace> { it.score }
                .thenBy { it.name })
            .take(3)
        val playerPlace =
            if (sessions.isEmpty()) 0
            else places.indexOfFirst { it.name == playerName(state) } + 1
        val winner = places.firstOrNull()
        val noteText: String
        if (winner != null && sessions.isNotEmpty() &&
            winner.name == playerName(state)) {
            val facts = SculptFacts(
                winner = winner.name,
                sessions = sessions.size,
                days = sessions.map { it.first }.distinct().size,
                snowy = sessions.count {
                    weatherAdj(it.second) == SCULPT_WX_SNOW },
                sunny = sessions.count {
                    weatherAdj(it.second) == SCULPT_WX_SUN },
                score = winner.score)
            val bank = when (judge.name) {
                "Wendy" -> SCULPT_NOTES_WENDY
                "Jimmy" -> SCULPT_NOTES_JIMMY
                else -> SCULPT_NOTES_CARTMAN
            }
            noteText = fillSculpt(bank[
                (w.year + winner.score) % bank.size], facts)
        } else if (winner != null) {
            val facts = SculptFacts(winner.name, 0, 0, 0, 0, winner.score)
            noteText = fillSculpt(SCULPT_NOTES_NPC[
                (w.year + winner.score) % SCULPT_NOTES_NPC.size], facts)
        } else {
            noteText = ""
        }
        if (playerPlace in 1..3) {
            Minigames.awardCoins(state, Fair.PLACE_COINS[playerPlace - 1])
        }
        putRecord(state, rec.copy(
            sculptPlaces = places,
            sculptJudge = judge.name,
            sculptNote = noteText,
            playerSculpt = playerPlace))
        if (sessions.isNotEmpty()) markAttended(state, w.year)
        note(state, "⛄ Carnival snow sculptures: " +
            (winner?.let { "${it.name} takes the blue " +
                "(${it.score})." } ?: "no entries at all — the " +
                "shore row stands empty.") +
            (if (playerPlace in 1..3)
                " You placed ${Fair.ordinalWord(playerPlace)} — " +
                    "a ${Fair.RIBBONS[playerPlace - 1]} ribbon!" else ""))
    }

    // ================================================================
    // Hot drinks: the stand synergy (Stand calls this)
    // ================================================================

    /** The carnival multiplier on one stand purchase: the
     *  kettle rush is real foot traffic at a real pitch —
     *  carnival week, the pond pitch, hot chocolate. Every
     *  other sale in town prices exactly as it did. Stand
     *  multiplies its ordinary chance by this; the crate,
     *  the prices and the books never know the carnival's
     *  name. */
    fun cocoaRush(state: GameState, itemId: String): Double {
        if (itemId != COCOA_ITEM) return 1.0
        if (!isCarnivalWeek(state)) return 1.0
        if (Stand.locOf(state) != GROUND) return 1.0
        return COCOA_RUSH
    }

    /** Carnival foot traffic present at the pond right now
     *  is the gathering claim's doing — the stand sells to
     *  whoever is physically there through its own tick.
     *  This reads that truth for the page: the count of
     *  townsfolk standing at the pond in carnival hours. */
    fun crowdAtPond(state: GameState): Int {
        if (!inCarnivalHours(state)) return 0
        return state.agents.values.count {
            !it.isPlayer && !it.asleep && it.location == GROUND &&
                it.waypoints.isEmpty()
        }
    }

    // ================================================================
    // Carnival life: the gathering, the first hole, the poster
    // ================================================================

    /** The gathering claim: during carnival hours the town
     *  is AT the pond — unless something bigger holds the
     *  person. Self-guarded exactly the way the fair's
     *  claim is: dinners, jobs, screenings, ceremonies,
     *  finales, the fair itself and live case work all
     *  outrank a carnival, and the crew's own hook (later
     *  in Engine's stack, self-guarded the same way) still
     *  takes a member to the player — the carnival never
     *  holds a crewmate against the crew. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        val idx = dayIndex(state)
        if (idx < 0) return null
        val to = if (idx == 6) SKATE_TO else CARN_TO
        if (timeMin !in CARN_FROM until to) return null
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
        if (Fair.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Climaxes.isLive(state) &&
            npcName in Climaxes.liveCausers(state)) return null
        val activity = when (idx) {
            0 -> "At the winter carnival — opening day, the " +
                "first holes going in, the shore row staked."
            FISH_DAY -> "At the winter carnival — ice fishing " +
                "day, the whole town spread across the pond."
            6 -> "At the winter carnival — closing night, " +
                "staying for the lantern skate."
            else -> "At the winter carnival — lanterns, " +
                "sculptures, and somebody else's kettle steam."
        }
        return AgendaBlock(GROUND, activity)
    }

    // ---------------- the first hole ----------------

    private fun kHole(year: Int) = "carnHole:$year"

    fun holeDrilled(state: GameState, year: Int): Boolean =
        state.life[kHole(year)] == 1

    /** The opening played beat: drill the first ice hole
     *  with Jimbo on opening day. Once per carnival. */
    fun drillFirstHole(state: GameState): String {
        val w = windowToday(state) ?: return ""
        if (holeDrilled(state, w.year)) return ""
        state.life[kHole(w.year)] = 1
        markAttended(state, w.year)
        Engine().relAdjust(state, "Jimbo", 2)
        note(state, "🕳️ The ${w.year} winter carnival is OPEN — " +
            "you and Jimbo drilled the first hole together, " +
            "and the town poured out onto the ice behind you.")
        return "🕳️ The auger is older than you and louder than " +
            "both of you. Jimbo braces it like a man defusing a " +
            "bomb he respects. “She bites at the end,” he says. " +
            "“They all bite at the end.”\n\nThe ice gives with a " +
            "sound like a dinner plate dropped in a cathedral. " +
            "Black water, welling up clean. The first hole of " +
            "the carnival is drilled — and the whole town, " +
            "watching from the shore, applauds like the pond " +
            "just scored."
    }

    // ---------------- the tap-person menu ----------------

    const val OPT_HOLE = "🕳️ Drill the first hole (opening day)"

    /** The carnival's option in the tap-person menu, if
     *  any: the opening beat lives with Jimbo, at the pond,
     *  like the fair's ribbon lives with the Mayor. */
    fun menuOption(state: GameState, npc: String): String? {
        val w = windowToday(state) ?: return null
        val idx = w.dayIndex(todayOrdinal(state))
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        if (me.location != GROUND) return null
        if (state.agents[npc]?.location != GROUND) return null
        if (npc == "Jimbo" && idx == 0 &&
            state.timeMin in CARN_FROM until CARN_TO &&
            !holeDrilled(state, w.year)) return OPT_HOLE
        return null
    }

    // ---------------- Look & the poster ----------------

    /** The carnival in a Look: the poster before the week,
     *  and the week's running story at the pond. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != GROUND) return null
        val today = todayOrdinal(state)
        val next = CarnivalCalendar.nextWindowAfter(today)
        val w = windowToday(state)
        if (w == null) {
            val away = next.startOrd - today
            if (away in 1..ANNOUNCE_AHEAD) {
                return "❄️ A poster is frozen flat to the pond " +
                    "hut, crooked under its ice glaze: WINTER " +
                    "CARNIVAL — ${dateLabelOf(next.startOrd)} to " +
                    "${dateLabelOf(next.endOrd)}, right here on " +
                    "the pond. Ice fishing, snow sculptures, " +
                    "lanterns, and the whole town on the ice."
            }
            return null
        }
        val idx = w.dayIndex(today)
        return when (idx) {
            0 -> "❄️ OPENING DAY at the winter carnival: " +
                "lantern posts along the shore, the sculpture " +
                "row staked out in the snow, and Jimbo " +
                "patrolling the ice with the auger like it " +
                "owes him money. Sculpture sessions run " +
                "through day three — carve early, carve often."
            1 -> "❄️ The carnival in full swing. The " +
                "sculpture row is growing piece by piece " +
                "(sessions at the pond, judged on day four's " +
                "morning); the kettle line never quite ends."
            2 -> "❄️ Last build day for the snow sculptures " +
                "— the judges take the row at midnight. " +
                "Tomorrow: ice fishing, all day, on the real " +
                "table. Rarer beats bigger. A boot is not a fish."
            FISH_DAY -> "🎣 ICE FISHING DAY at the carnival: " +
                "holes across the whole pond, buckets, thermoses, " +
                "and total silence whenever a line twitches. " +
                "Your pond catches today are your entry."
            4 -> "❄️ The carnival breathes out. The " +
                "sculptures have their ribbons; the fishing " +
                "crowd has its stories, which grow daily in " +
                "the absence of evidence."
            5 -> "❄️ The carnival's quiet day — lantern " +
                "light on the snow, the row of sculptures " +
                "keeping their shapes for one more night. " +
                "Tomorrow is closing: the lantern skate."
            else -> "❄️ CLOSING DAY at the winter carnival: " +
                "last casts, last cocoa, the sculptures' last " +
                "night standing — and tonight the whole town " +
                "stays out on the ice for the lantern skate."
        }
    }

    // ---------------- the announcement & the phone ----------------

    /** The carnival the town is counting down to has its
     *  announcement morning today. */
    fun announceDue(state: GameState): Boolean {
        val today = todayOrdinal(state)
        val next = CarnivalCalendar.nextWindowAfter(today)
        if (next.startOrd - today != ANNOUNCE_AHEAD) return false
        return state.life["carnPhoned:${next.year}"] != 1
    }

    fun markPhoned(state: GameState, year: Int) {
        state.life["carnPhoned:$year"] = 1
    }

    fun announcementLine(w: CarnivalCalendar.CarnWindow): String =
        "❄️ The winter carnival comes to Stark's Pond in " +
            "three days — ${dateLabelOf(w.startOrd)} to " +
            "${dateLabelOf(w.endOrd)}: ice fishing, snow " +
            "sculptures, lanterns, and a lantern skate on " +
            "closing night."

    // ---------------- attendance ----------------

    private fun kAttended(year: Int) = "carnAttended:$year"

    fun markAttended(state: GameState, year: Int) {
        state.life[kAttended(year)] = 1
    }

    fun attended(state: GameState, year: Int): Boolean =
        state.life[kAttended(year)] == 1

    // ================================================================
    // The rollover (Engine.onNewDay calls this beside the fair's,
    // so the carnival's morning facts are textable by breakfast)
    // ================================================================

    fun dailyRollover(state: GameState) {
        val today = todayOrdinal(state)
        // ---- the announcement, three mornings out ----
        val next = CarnivalCalendar.nextWindowAfter(today)
        if (next.startOrd - today == ANNOUNCE_AHEAD &&
            state.life["carnAnn:${next.year}"] != 1) {
            state.life["carnAnn:${next.year}"] = 1
            note(state, announcementLine(next))
            townLogLine(state, "❄️ Winter carnival announced — " +
                "${dateLabelOf(next.startOrd)}–" +
                "${dateLabelOf(next.endOrd)} at Stark's Pond")
        }
        val w = windowToday(state)
        // ---- after closing: the wrap (the first rollover
        // past the week, whenever the game next wakes —
        // a slept-through month still wraps honestly) ----
        if (w == null) {
            val y = keyFromOrdinal(today)
                .substring(0, 4).toIntOrNull() ?: return
            for (yy in y downTo (y - 1)) {
                val cand = CarnivalCalendar.windowFor(yy)
                val rec = recordFor(state, yy)
                if (today > cand.endOrd && rec != null &&
                    !rec.wrapped) {
                    wrapCarnival(state, cand)
                    break
                }
            }
            return
        }
        // ---- opening morning: the record is born ----
        val idx = w.dayIndex(todayOrdinal(state))
        if (idx == 0 && recordFor(state, w.year) == null) {
            putRecord(state, CarnivalRecord(year = w.year))
            note(state, "❄️ THE WINTER CARNIVAL OPENS at " +
                "Stark's Pond — a week of ice, lanterns and " +
                "questionable sculpture. The first hole waits " +
                "on Jimbo and his auger, at the pond, today.")
            townLogLine(state, "❄️ Winter carnival (${w.year}) — " +
                "${state.gameMonth}/${state.gameDay}")
        }
        // ---- the contests judge on their mornings ----
        if (idx == FISH_DAY &&
            state.life["carnSculptJudged:${w.year}"] != 1 &&
            recordFor(state, w.year) != null) {
            judgeSculpt(state, w)
        }
        if (idx == FISH_DAY + 1 &&
            state.life["carnFishJudged:${w.year}"] != 1 &&
            recordFor(state, w.year) != null) {
            judgeFish(state, w)
        }
        // ---- the week's morning lines ----
        if (recordFor(state, w.year) != null) {
            when (idx) {
                1, 2 -> note(state, "⛄ Carnival sculpture days: " +
                    "build sessions at the pond (two a day, " +
                    "half an hour each) — judged on day four's " +
                    "morning off the record the work files.")
                FISH_DAY -> note(state, "🎣 Carnival ice fishing " +
                    "day: the contest runs on the ice at Stark's " +
                    "Pond — your catches there today are your entry.")
                6 -> note(state, "❄️ Closing day at the winter " +
                    "carnival — and tonight, the lantern skate " +
                    "on the pond. The whole town stays out.")
            }
        }
    }

    /** The morning after closing: the year's record
     *  closes — attended, or honestly missed — and the
     *  dressing's strike is this line: the tokens are a
     *  pure function of the week, so the carnival is gone
     *  from the pond the moment the calendar turns. */
    fun wrapCarnival(state: GameState,
                     w: CarnivalCalendar.CarnWindow) {
        val rec = recordFor(state, w.year) ?: return
        if (rec.wrapped) return
        // a carnival the game slept through still judges:
        // any contest whose morning was skipped judges
        // now, off the same records and seeds.
        if (state.life["carnSculptJudged:${w.year}"] != 1) {
            judgeSculpt(state, CarnivalCalendar.windowFor(rec.year))
        }
        if (state.life["carnFishJudged:${w.year}"] != 1) {
            judgeFish(state, CarnivalCalendar.windowFor(rec.year))
        }
        val after = recordFor(state, w.year) ?: rec
        val wasThere = attended(state, w.year) ||
            after.playerFish > 0 || after.playerSculpt > 0 ||
            playerFishEntry(state, w.year) != null ||
            buildSessions(state, w.year).isNotEmpty()
        putRecord(state, after.copy(
            wrapped = true,
            attended = wasThere,
            missed = !wasThere))
        note(state, if (wasThere)
            "❄️ The carnival packs up in the dawn — lanterns " +
                "down, holes left to skin over, the sculptures " +
                "standing their last watch. The ribbons stay " +
                "where they were won. See the Journal's " +
                "🎪 WINTER CARNIVAL page."
        else
            "❄️ The carnival packs up in the dawn — lanterns " +
                "down, holes left to skin over. You missed the " +
                "carnival of ${w.year} entirely; the town " +
                "skated without you, and the Journal kept the " +
                "receipts.")
        townLogLine(state, "❄️ Winter carnival (${w.year}) — wrapped" +
            (if (wasThere) "" else " (you missed it)"))
    }

    // ================================================================
    // Dressing (the render bridge: TownInfo carries this)
    // ================================================================

    /** The carnival's dressing tokens — registered in
     *  Depth at BACKGROUND with the fair layer. Drawn as
     *  simple shapes only: three wide-spaced ice holes
     *  (broad ellipses, never a cluster), lantern posts,
     *  the sculpture row, the banner. */
    val CARN_TOKENS: Set<String> = setOf(
        "carn-banner", "carn-holes", "carn-lanterns", "carn-sculptures")

    /** locId -> tokens, live only while the week stands:
     *  the morning after closing this is empty everywhere,
     *  which IS the strike. */
    fun carnivalTokensByLoc(state: GameState): Map<String, List<String>> {
        if (!isCarnivalWeek(state)) return emptyMap()
        return mapOf(GROUND to CARN_TOKENS.sorted())
    }

    // ================================================================
    // The Journal page
    // ================================================================

    private fun placeLine(emoji: String, label: String,
                          places: List<CarnPlace>,
                          playerPlace: Int): List<String> {
        if (places.isEmpty()) return listOf(
            "$emoji $label — not judged yet.")
        val out = mutableListOf("$emoji $label:")
        places.forEachIndexed { i, p ->
            out += "   ${Fair.RIBBONS[i]} ribbon — ${p.name}: ${p.detail}"
        }
        if (playerPlace in 1..3) {
            out += "   → your ${Fair.RIBBONS[playerPlace - 1]} ribbon. 🏆"
        }
        return out
    }

    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val today = todayOrdinal(state)
        val next = CarnivalCalendar.nextWindowAfter(today)
        val live = windowToday(state)
        out += "❄️ One week every winter — third Saturday " +
            "of January, Stark's Pond, on the ice."
        if (live != null) {
            val idx = live.dayIndex(today)
            out += "❄️ THE CARNIVAL IS ON — day ${idx + 1} of 7 " +
                "(${dateLabelOf(live.startOrd)}–" +
                "${dateLabelOf(live.endOrd)}). Lanterns 10–8" +
                (if (idx == 6) ", the lantern skate tonight" else "") + "."
        } else if (next.startOrd > today) {
            out += "❄️ Next carnival: ${dateLabelOf(next.startOrd)}–" +
                "${dateLabelOf(next.endOrd)}, ${next.year}."
        }
        // the hot-drinks synergy, stated where the town
        // reads its records: a real pitch, a real rush.
        if (Stand.hasStand(state) && Stand.locOf(state) == GROUND) {
            out += "☕ Your stand is pitched at the pond: all " +
                "carnival week, hot chocolate there buys at " +
                "the kettle rush (×$COCOA_RUSH the ordinary " +
                "chance) — the carnival crowd is real foot " +
                "traffic, and the stand's own books keep the coins."
        } else if (Stand.hasStand(state)) {
            out += "☕ Your stand is pitched at " +
                "${LOCATIONS[Stand.locOf(state)]?.name ?: "elsewhere"} — " +
                "the kettle rush (hot chocolate ×$COCOA_RUSH) " +
                "only runs at a pond pitch during carnival week."
        }
        val records = records(state)
        if (records.isEmpty()) {
            out += "No carnival has come since you moved to " +
                "town. The poster goes up three days before " +
                "opening — watch the pond hut."
            return out
        }
        val newest = records.first()
        out += "—— the carnival of ${newest.year}" +
            (if (newest.missed) " —— you missed it" else "") + " ——"
        if (newest.missed) {
            out += "The town went without you: the ice, the " +
                "lanterns, the sculptures — all of it, " +
                "unwitnessed. The results stand anyway:"
        }
        out += placeLine("⛄", "Snow sculptures (judged by " +
            "${newest.sculptJudge.ifEmpty { "the yearbook desk" }})",
            newest.sculptPlaces, newest.playerSculpt)
        if (newest.sculptNote.isNotEmpty()) {
            out += "   “${newest.sculptNote}”"
        }
        out += placeLine("🎣", "Ice fishing (Stark's Pond, " +
            "through the ice)", newest.fishPlaces, newest.playerFish)
        for (elder in records.drop(1)) {
            out += indexLine(elder)
        }
        return out
    }

    /** An elder carnival, one line: the year, the
     *  contests' blues, and whether you were there. */
    fun indexLine(rec: CarnivalRecord): String {
        fun winner(places: List<CarnPlace>): String =
            places.firstOrNull()?.name ?: "nobody"
        return "❄️ ${rec.year}: sculptures — ${winner(rec.sculptPlaces)} · " +
            "ice fishing — ${winner(rec.fishPlaces)}" +
            (if (rec.missed) " · (you missed it)" else "")
    }
}

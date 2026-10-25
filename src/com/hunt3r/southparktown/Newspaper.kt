package com.hunt3r.southparktown

/**
 * 📰 THE TOWN NEWSPAPER (v0.11.0a, Queue #11 item 1).
 *
 * A weekly edition written ENTIRELY from the record — the
 * Stand/Rival/Yearbook pattern: assembly is pure, state rides
 * life/lifeText ONLY (zero new GameState fields, zero new
 * Prefs keys), and the Journal renders what pageLines hands
 * it. Nothing is invented: a fact appears only if a record
 * somewhere in the save carries it, and a section with no
 * record in the week prints one honest line saying so.
 *
 * Every week has an identity (Jobs.weekIndex — weeks start
 * Thursday, the day the town began). At the week's turn the
 * edition for the week just lived is composed ONCE, frozen
 * into lifeText["paperLatest"], and announced on the ticker;
 * past editions reduce to their headline lines, capped.
 *
 * HEADLINE precedence — stated once, here, in docs/TUNING.md and
 * in the suite (highest tier present wins; ties within a
 * tier go to the earliest recorded day):
 *   1. CASE CLOSED — a case retired during the week, public
 *      shape only: no names, no places tied to solutions, no
 *      branch or choice words, no solution terms, ever. The
 *      closing days come from the arcs' own RetiredDay
 *      fields (arcs 2–9). Arc 1 keeps no closing day in its
 *      record — only a finished flag — so the paper never
 *      guesses which week it closed in.
 *   2. THE FAIR — a wrapped FairRecord whose fair week
 *      overlaps this week.
 *   3. SEASON FINALE — a finale instance whose wrap
 *      morning (the day after its season's last day, when
 *      Finales.dailyRollover resolves the instance) falls
 *      in the week; the town line quotes the standing mark
 *      the instance left, when the mark still stands
 *      for it. (v0.12.0a)
 *   4. OPENING NIGHT — a PlayRecord with a verdict whose
 *      scheduled opening (PlayCalendar) falls in the week.
 *   5. THE VOTE — Votes.lastResult, counted in the week.
 *   6. CAPER CLOSE — the hero caper's ending day
 *      (Heroes' caperDoneDay) falls in the week. PUBLIC
 *      SHAPE ONLY: the paper repeats the Hero Log's own
 *      public ending phrase (Heroes.caperPublicEnding)
 *      and nothing else about the caper — never who,
 *      never what, never where it hid. (v0.12.0a)
 *   7. STAND REVIEW — the rival crew's latest review of
 *      the player's stand posted during the week (Stand's
 *      review day); the verdict is the books' own.
 *      (v0.12.0a)
 *   8. THE RIVALRY — at least one Rival contest logged in
 *      the week; the headline quotes the recorded score.
 *   9. THE SKY — a special night in the paper's weather log.
 *  10. A QUIET WEEK IN SOUTH PARK — the fallback, honest.
 *
 * v0.12.0a — the paper grows up (Queue #12 item 1): the
 * three folded tiers (finale, caper, stand review) join
 * the ladder above, and the edition is DELIVERED. A copy
 * waits on the player's porch — the "paper" fixture at
 * Your House renders the frozen edition — and the phone
 * carries a one-text summary: publication stamps the
 * paperSummaryDue debt, and Phone's harvest pays it with
 * the headline verbatim, texted by a rotating cast
 * subscriber (Phone's PAPER trigger; the stamp clears
 * only when the text actually lands, the fair
 * announcement's discipline — a crowded morning defers
 * the text, never the edition).
 *
 * The weather log is the paper's own: each rollover appends
 * the ENDED day (absDay − 1) with the weather that day
 * actually had. Engine.weatherId is still the ended day's
 * weather at the module-rollover point — the new day's sky
 * only drifts later, in tickWeather — and the night's
 * special is SkyState.dateSpecial for the ended day's own
 * date and season (via Commissions.keyForAbsDay).
 *
 * Dials: HEADLINE_CAP (past editions kept as headline
 * lines), SKY_LOG_CAP (nights of sky the paper remembers —
 * two weeks, so a week's weather always survives to print).
 */
object Newspaper {

    /** Past editions kept as headline lines, latest included. */
    const val HEADLINE_CAP = 8

    /** Nights of sky kept in the paper's own weather log. */
    const val SKY_LOG_CAP = 14

    private const val K_SKY_LOG = "paperSkyLog"     // lifeText: "absDay:weatherId:special;…"
    private const val K_LATEST = "paperLatest"      // lifeText: the frozen edition, lines joined "\n"
    private const val K_WEEK = "paperWeek"          // life: the frozen edition's week
    private const val K_HEADLINES = "paperHeadlines" // lifeText: "week|headline;…"
    private const val K_LAST_WEEK = "paperLastWeek" // life: publication guard
    private const val K_SUMMARY_DUE = "paperSummaryDue" // life: v0.12.0a — the phone summary is owed

    /** One composed edition: its week, its headline, and the
     *  full ordered lines exactly as the Journal prints them. */
    data class Edition(val week: Int, val headline: String,
                       val lines: List<String>)

    /** One night in the paper's weather log. */
    data class SkyEntry(val absDay: Int, val weatherId: String,
                        val special: String)

    // ---------------- the weather log ----------------

    fun skyLog(state: GameState): List<SkyEntry> {
        val raw = state.lifeText[K_SKY_LOG] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            val p = e.split(":")
            if (p.size != 3) null
            else SkyEntry(p[0].toIntOrNull() ?: return@mapNotNull null,
                p[1], p[2])
        }
    }

    private fun skyLogInWeek(state: GameState, week: Int): List<SkyEntry> =
        skyLog(state).filter { Jobs.weekIndex(it.absDay) == week }
            .sortedBy { it.absDay }

    // ---------------- the headline history ----------------

    /** (week, headline) pairs, oldest first, capped at
     *  HEADLINE_CAP — past editions are headline lines only. */
    fun headlineHistory(state: GameState): List<Pair<Int, String>> {
        val raw = state.lifeText[K_HEADLINES] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            if (e.isEmpty()) return@mapNotNull null
            val p = e.split("|", limit = 2)
            if (p.size != 2) null
            else (p[0].toIntOrNull() ?: return@mapNotNull null) to p[1]
        }
    }

    /** The frozen edition's week, or null before the first. */
    fun publishedWeek(state: GameState): Int? = state.life[K_WEEK]

    // ---------------- rollover ----------------

    /**
     * The paper's midnight, hooked into Engine.onNewDay after
     * the stand's wrap and before the phone. Two jobs: log
     * the ended day's sky, and — when the week has turned —
     * publish the edition for the week just lived, exactly
     * once, and only if the save actually lived that week
     * (a player exists, and the paper's own log already held
     * a night from that week before tonight's entry: a save
     * teleported to a boundary has no such night, and the
     * paper does not report weeks it never saw).
     */
    fun dailyRollover(state: GameState) {
        val endedDay = state.absDay - 1
        val endedWeek = Jobs.weekIndex(endedDay)
        // lived-week evidence, read BEFORE tonight's entry lands
        val livedWeek = skyLog(state).any {
            Jobs.weekIndex(it.absDay) == endedWeek }

        // 1) the weather log: the ended day, with the weather
        // it actually had (weatherId has not re-rolled yet —
        // the new day's sky drifts later, in tickWeather) and
        // the special of its own date and season.
        if (endedDay >= 0) {
            val key = Commissions.keyForAbsDay(state, endedDay)
            val month = key.substring(4, 6).toIntOrNull()
                ?: state.gameMonth
            val special = SkyState.dateSpecial(endedDay,
                state.weatherId, seasonFor(month))
            val entries = skyLog(state)
                .filter { it.absDay != endedDay } +
                SkyEntry(endedDay, state.weatherId, special)
            state.lifeText[K_SKY_LOG] = entries
                .takeLast(SKY_LOG_CAP)
                .joinToString(";") {
                    "${it.absDay}:${it.weatherId}:${it.special}" }
        }

        // 2) publication, at the week's turn only
        if (Jobs.weekIndex(state.absDay) == endedWeek) return
        val playerExists = state.agents.values.any { it.isPlayer }
        val lastPub = state.life[K_LAST_WEEK] ?: Int.MIN_VALUE
        if (!playerExists || !livedWeek) return
        if (lastPub >= endedWeek) return
        if ((state.life[K_WEEK] ?: Int.MIN_VALUE) == endedWeek) return
        val ed = composeEdition(state, endedWeek)
        state.lifeText[K_LATEST] = ed.lines.joinToString("\n")
        state.life[K_WEEK] = endedWeek
        state.life[K_LAST_WEEK] = endedWeek
        // v0.12.0a: the phone summary is owed from this
        // morning — Phone's harvest pays the debt when a
        // morning's budget lets it, and only then.
        state.life[K_SUMMARY_DUE] = 1
        val hist = (headlineHistory(state) +
            (endedWeek to ed.headline)).takeLast(HEADLINE_CAP)
        state.lifeText[K_HEADLINES] = hist.joinToString(";") {
            "${it.first}|${it.second.replace(";", " ")
                .replace("|", " ")}" }
        state.events.add("📰 The weekly paper is out: ${ed.headline} — " +
            "see the 📰 THE PAPER page in your Journal.")
        while (state.events.size > 40) state.events.removeAt(0)
    }

    // ---------------- the record, read for one week ----------------

    /** The earliest closing day among arcs 2–13 in the week,
     *  or null. Arc 1's record carries no closing day (see
     *  the header), so it is never dated by guesswork. */
    private fun caseDayInWeek(state: GameState, week: Int): Int? =
        listOf(state.arc2RetiredDay, state.arc3RetiredDay,
            state.arc4RetiredDay, state.arc5RetiredDay,
            state.arc6RetiredDay, state.arc7RetiredDay,
            state.arc8RetiredDay, state.arc9RetiredDay,
            state.arc10RetiredDay, state.arc11RetiredDay,
            state.arc12RetiredDay, state.arc13RetiredDay)
            .filter { it >= 0 && Jobs.weekIndex(it) == week }
            .minOrNull()

    private fun fairInWeek(state: GameState, week: Int): FairRecord? =
        state.fairs.firstOrNull { rec ->
            if (!rec.wrapped) return@firstOrNull false
            val win = FairCalendar.windowFor(rec.year)
            val start = FairCalendar.absDayOfOrdinal(win.startOrd)
            val end = FairCalendar.absDayOfOrdinal(win.endOrd)
            (start..end).any { Jobs.weekIndex(it) == week }
        }

    /** The production whose opening falls in the week. The
     *  opening day is derived from the record's own year via
     *  the play calendar — the same schedule the town played
     *  by — never from a stamp the record doesn't carry. */
    private fun playInWeek(state: GameState,
        week: Int): Play.PlayRecord? =
        Play.allRecords(state).firstOrNull { rec ->
            rec.verdict.isNotEmpty() &&
                Jobs.weekIndex(PlayCalendar
                    .scheduleFor(rec.year).openingDay) == week
        }

    private fun voteInWeek(state: GameState,
        week: Int): Votes.CountResult? =
        Votes.lastResult(state)?.takeIf {
            Jobs.weekIndex(it.countedDay) == week }

    private fun contestsInWeek(state: GameState,
        week: Int): List<Rival.RivalContest> =
        Rival.contestLog(state)
            .filter { Jobs.weekIndex(it.day) == week }
            .sortedBy { it.day }

    private fun yearbookInWeek(state: GameState,
        week: Int): YearbookVolume? =
        state.yearbooks.firstOrNull {
            Jobs.weekIndex(it.publishedAbsDay) == week }

    /** A finale instance that wrapped in the week (v0.12.0a):
     *  its def, its instance year, and its wrap morning. */
    private data class FinaleWrap(val def: FinaleDef,
        val instanceYear: Int, val wrapAbsDay: Int)

    /** The morning a finale instance wraps: the day after
     *  its season's last day — the morning
     *  Finales.dailyRollover finds it pending and resolves
     *  it. Derived from the same calendar Finales runs on
     *  (seasonEndMonthDay + the shared ordinal math), never
     *  from a stamp the record doesn't carry. */
    private fun finaleWrapAbsDay(def: FinaleDef, iy: Int): Int {
        val (m, d) = Finales.seasonEndMonthDay(def.season, iy)
        val key = "%04d%02d%02d".format(iy, m, d)
        return FairCalendar.absDayOfOrdinal(ordinalOfKey(key)) + 1
    }

    /** The earliest finale wrap whose morning falls in the
     *  week, or null. The scan runs the instance years the
     *  week's own dates touch (±1 for the season edges);
     *  an instance counts only if its done-flag stands. */
    private fun finaleWrapInWeek(state: GameState,
        week: Int): FinaleWrap? {
        val years = (week * 7..week * 7 + 6).map {
            Commissions.keyForAbsDay(state, it)
                .substring(0, 4).toIntOrNull() ?: state.gameYear
        }
        val lo = (years.minOrNull() ?: state.gameYear) - 1
        val hi = (years.maxOrNull() ?: state.gameYear) + 1
        var best: FinaleWrap? = null
        for (def in Finales.DEFS) {
            for (iy in lo..hi) {
                if (!Finales.isWrapped(state, def, iy)) continue
                val wrap = finaleWrapAbsDay(def, iy)
                if (Jobs.weekIndex(wrap) != week) continue
                if (best == null || wrap < best.wrapAbsDay)
                    best = FinaleWrap(def, iy, wrap)
            }
        }
        return best
    }

    /** The hero caper's closing day, if it fell in the
     *  week (v0.12.0a). The day is Heroes' own stamp; the
     *  paper reads nothing else about the caper here. */
    private fun caperDayInWeek(state: GameState,
        week: Int): Int? {
        if (!Heroes.caperDone(state)) return null
        val day = state.life["caperDoneDay"] ?: return null
        return day.takeIf { Jobs.weekIndex(it) == week }
    }

    /** The stand's review tier (v0.12.0a): Stand keeps only
     *  the latest review standing, so the review the paper
     *  can cite is the one standing now — and it headlines
     *  only in the week its own recorded day names. */
    private fun reviewInWeek(state: GameState,
        week: Int): Boolean {
        if (Stand.reviewText(state) == null) return false
        if (Stand.reviewVerdict(state).isNullOrEmpty()) return false
        val day = Stand.reviewDay(state) ?: return false
        return Jobs.weekIndex(day) == week
    }

    private fun specialName(special: String): String = when (special) {
        SkyState.AURORA -> "the aurora"
        SkyState.METEOR -> "a meteor shower"
        SkyState.HARVEST -> "the harvest moon"
        SkyState.FIREWORKS -> "fireworks over town"
        else -> special
    }

    private fun dateLabel(state: GameState, absDay: Int): String {
        val key = Commissions.keyForAbsDay(state, absDay)
        val m = key.substring(4, 6).toIntOrNull() ?: 0
        val d = key.substring(6, 8).toIntOrNull() ?: 0
        return "$m/$d"
    }

    // ---------------- composition (pure) ----------------

    /**
     * Compose the edition for [week] from the record alone.
     * Pure: reads the state, writes nothing. Sections always
     * appear, always in this order — TOWN NEWS, SPORTS,
     * CLASSIFIEDS, WEATHER — and a section with no record in
     * the week prints one honest line saying so.
     */
    fun composeEdition(state: GameState, week: Int): Edition {
        val caseDay = caseDayInWeek(state, week)
        val fair = fairInWeek(state, week)
        val finale = finaleWrapInWeek(state, week)
        val play = playInWeek(state, week)
        val vote = voteInWeek(state, week)
        val caperDay = caperDayInWeek(state, week)
        val review = reviewInWeek(state, week)
        val contests = contestsInWeek(state, week)
        val sky = skyLogInWeek(state, week)
        val skySpecial = sky.firstOrNull {
            it.special != SkyState.NONE }

        val headline = when {
            caseDay != null -> "THE CASE CLOSES"
            fair != null -> "THE COUNTY FAIR PACKS UP"
            finale != null -> "'${finale.def.title.uppercase()}' — " +
                "THE ${finale.def.seasonName.uppercase()} " +
                "FINALE WRAPS"
            play != null -> "${Play.verdictWord(play.verdict)}: " +
                "'${Play.TITLE.uppercase()}' OPENS"
            vote != null -> "${vote.winnerLabel().uppercase()} " +
                "WINS THE VOTE"
            caperDay != null -> "THE CAPER CLOSES"
            review -> "THE STAND, REVIEWED: " +
                (Stand.reviewVerdict(state) ?: "")
            contests.isNotEmpty() -> "THE RIVALRY — " +
                Rival.scoreLine(state)
            skySpecial != null -> "SKY SHOW: " +
                "${specialName(skySpecial.special).uppercase()} " +
                "OVER SOUTH PARK"
            else -> "A QUIET WEEK IN SOUTH PARK"
        }

        val lines = mutableListOf<String>()
        lines += "📰 THE SOUTH PARK GAZETTE"
        lines += "Week $week · printed from the town's own record"
        lines += headline

        // ---- TOWN NEWS ----
        lines += "🗞 TOWN NEWS"
        val town = mutableListOf<String>()
        if (vote != null) {
            val q = vote.question()
            town += "🗳 The vote — '${q?.title ?: vote.questionId}': " +
                "${vote.winnerLabel()} takes it, ${vote.winnerVotes} " +
                "of ${vote.totalVotes} votes (margin ${vote.margin})."
        }
        if (fair != null) {
            val went = when {
                fair.attended -> "you were there"
                fair.missed -> "you missed it"
                else -> "the town turned out"
            }
            val placings = listOf(
                "photo" to fair.playerPhoto,
                "fish" to fair.playerFish,
                "dunk" to fair.playerDunk,
                // v0.11.0b: the fourth contest joins the list
                "produce" to fair.playerProduce)
                .filter { it.second > 0 }
            val placeBit = if (placings.isEmpty()) ""
                else " Your finishes: " + placings.joinToString(", ") {
                    "${it.first} ${it.second}" } + "."
            town += "🎡 The ${fair.year} county fair wrapped this " +
                "week — $went, and you brought home " +
                "${fair.blues()} blue ribbon(s).$placeBit"
        }
        if (play != null) {
            town += "🎭 Opening night — '${Play.TITLE}': the verdict " +
                "was ${Play.verdictWord(play.verdict)}" +
                (if (play.critic.isNotEmpty())
                    ", per ${play.critic}." else ".")
        }
        if (finale != null) {
            val def = finale.def
            val place = LOCATIONS[def.loc]?.name ?: def.loc
            // the standing mark is quoted only while it
            // still stands for THIS instance — a later
            // year's mark belongs to a later year's paper.
            val mark = Finales.markOf(state, def)
                ?.takeIf { it.year == finale.instanceYear }
            val markBit = if (mark != null)
                " The mark it leaves: ${mark.text} — history " +
                    "credits ${mark.winner}" +
                    (if (mark.byYou) " (your call)." else ".")
            else ""
            town += "${def.emoji} The ${def.seasonName} finale " +
                "— '${def.title}' — wrapped this week at " +
                "$place.$markBit"
        }
        if (caseDay != null) {
            town += "🔍 A long-running town case reached its " +
                "ending this week. The paper prints what the " +
                "town saw, and no more."
        }
        if (caperDay != null) {
            // PUBLIC SHAPE ONLY: the ending phrase is the
            // Hero Log's own public word for it, borrowed
            // whole — the paper adds nothing of its own
            // about the caper, ever.
            val ending = Heroes.caperPublicEnding(state)
                ?: "closed"
            town += "🦸 The caper closed this week — the hero " +
                "patrols' long night case, done. The Hero " +
                "Log's own word for the ending: $ending."
        }
        if (review) {
            val by = Stand.reviewBy(state)
            town += "💰 The rival's crew posted their review " +
                "of your stand this week — verdict: " +
                "${Stand.reviewVerdict(state)}" +
                (if (by != null) ", per $by." else ".") +
                " The books were the whole argument."
        }
        val yb = yearbookInWeek(state, week)
        if (yb != null) {
            town += "📖 The ${Yearbook.labelOf(yb.year)} yearbook " +
                "came out this week — ${yb.photoCount} photos " +
                "judged and printed."
        }
        // v0.12.0c: the week's birthdays stand in TOWN NEWS —
        // names + dates only, derived whole off the roster
        // and the week's calendar keys (Birthdays owns the
        // registry and the derivation).
        Birthdays.birthdaysLine(state, week)?.let { town += it }
        // v0.15.0i: a shared day in the week is named AS
        // shared, both names on the one line — the list
        // above carries them either way; this says the
        // day itself is doubled.
        Birthdays.sharedDayLine(state, week)?.let { town += it }
        // v0.13.0b: the week's dance stands in TOWN NEWS —
        // one line, names and the photograph's existence,
        // derived whole off the night's record (Dance owns
        // the record and the derivation). Section copy,
        // never a headline tier: the ladder stands at ten.
        Dance.paperLine(state, week)?.let { town += it }
        // v0.13.0c: the show week's talent night stands in
        // TOWN NEWS — one line, the winner and the act,
        // derived whole off the night's record (Talent
        // owns the record and the derivation). Section
        // copy, never a headline tier: the ladder stands
        // at ten.
        Talent.paperLine(state, week)?.let { town += it }
        // v0.13.0d: the parade's week stands in TOWN NEWS —
        // one line, the order's head and the Mayor's car
        // named, your entry if you marched, derived whole
        // off the day's record (Parade owns the record and
        // the derivation). Section copy, never a headline
        // tier: the ladder stands at ten.
        Parade.paperLine(state, week)?.let { town += it }
        // v0.14.0b: the trip's week stands in TOWN NEWS —
        // one line, the site and the day, your ride or
        // your absence, derived whole off the day's
        // record (FieldTrip owns the record and the
        // derivation). Section copy, never a headline
        // tier: the ladder stands at ten.
        FieldTrip.paperLine(state, week)?.let { town += it }
        // v0.14.0c: the festival stands in TOWN NEWS —
        // the announcement in its week and the winner
        // in the closing week's edition, derived whole
        // off the calendar and the year's record
        // (Festival owns both). Section copy, never a
        // headline tier: the ladder stands at ten.
        Festival.paperLine(state, week)?.let { town += it }
        // v0.14.0d: your birthday stands in TOWN NEWS —
        // one line in the edition of the week it fell
        // in, derived whole off the arrival record (and
        // the party record, when one wrapped in the
        // week). PlayerBirthday owns both. Section copy,
        // never a headline tier: the ladder stands at ten.
        PlayerBirthday.paperLine(state, week)?.let { town += it }
        // v0.15.0f: the gallery show stands in TOWN NEWS —
        // the announcement in its week and the winner in
        // the closing week's edition, derived whole off
        // the calendar and the show's record (Gallery
        // owns both). Section copy, never a headline
        // tier: the ladder stands at ten.
        Gallery.paperLine(state, week)?.let { town += it }
        // v0.15.0h: an apprenticeship's graduation stands
        // in TOWN NEWS — one line in the completion's own
        // week, derived whole off the site's record
        // (Apprenticeships owns the record and the line).
        // Section copy, never a headline tier: the ladder
        // stands at ten.
        Apprenticeships.paperLine(state, week)?.let { town += it }
        if (town.isEmpty()) town += "🗞 Nothing on the record " +
            "made town news this week."
        lines += town

        // ---- SPORTS ----
        lines += "🏅 SPORTS"
        val sports = mutableListOf<String>()
        if (Rival.revealed(state) || Rival.contestsPlayed(state) > 0 ||
            Rival.contestLog(state).isNotEmpty()) {
            sports += "🏆 ${Rival.scoreLine(state)}"
        }
        if (contests.isNotEmpty()) {
            sports += "${contests.size} contest(s) on the record " +
                "this week:"
            for (c in contests) {
                val winnerText = when (c.winner) {
                    "you" -> "you won"
                    "them" -> "${Rival.rivalOf(state)} won"
                    else -> "a draw"
                }
                sports += "🏆 ${dateLabel(state, c.day)}: " +
                    "${Rival.kindLabel(c.kind)} — $winnerText: " +
                    c.detail
            }
        }
        if (state.showdown.isNotEmpty()) {
            val w = state.showdown.values.sumOf { it.wins }
            val l = state.showdown.values.sumOf { it.losses }
            sports += "❄️ Snowball showdown — the season's card so " +
                "far: ${w}W · ${l}L over ${state.showdown.size} " +
                "opponent(s). Lifetime totals, not this week's."
        }
        if (RivalCrew.seasonPlayed(state) > 0) {
            sports += "🆚 ${RivalCrew.crewsScoreLine(state)}"
        }
        if (sports.isEmpty()) sports += "🏅 No contests on the " +
            "record this week — the field was quiet."
        lines += sports

        // ---- CLASSIFIEDS (real prices only) ----
        lines += "💰 CLASSIFIEDS"
        val hasStand = Stand.hasStand(state)
        if (hasStand) {
            lines += "💰 Your stand at " +
                "${LOCATIONS[Stand.locOf(state)]?.name ?: "town"} " +
                "— your prices next to the shops':"
        }
        for (def in Stand.STOCK) {
            val name = itemById(def.itemId)?.name ?: def.itemId
            lines += if (hasStand)
                "$name — the shops charge ${def.shopPrice} 🪙 " +
                    "(${def.supplier}); your stand asks " +
                    "${Stand.priceOf(state, def.itemId)} 🪙"
            else
                "$name — ${def.shopPrice} 🪙 at ${def.supplier}"
        }
        lines += "🪙 Starting a stand of your own: the City Hall " +
            "permit is ${Stand.PERMIT_COST} 🪙 and the stand kit " +
            "is ${Stand.KIT_COST} 🪙."

        // ---- WEATHER ----
        lines += "🌦 WEATHER"
        if (sky.isEmpty()) {
            lines += "🌦 The paper's weather log begins with " +
                "this edition — no nights were on the record " +
                "for this week."
        } else {
            for (e in sky) {
                val label = WEATHER[e.weatherId]?.label ?: e.weatherId
                lines += "${dateLabel(state, e.absDay)} — $label" +
                    (if (e.special != SkyState.NONE)
                        " · that night: ${specialName(e.special)}"
                    else "")
            }
        }

        lines += "Printed weekly, from the record — if it " +
            "didn't happen, the paper says so."
        return Edition(week, headline, lines)
    }

    // ---------------- delivery (v0.12.0a) ----------------

    /** The latest published edition's lines, exactly as
     *  frozen — what the porch copy at Your House shows.
     *  Null before the first edition: no paper on the
     *  porch yet. The porch copy persists until the next
     *  edition replaces it. */
    fun porchLines(state: GameState): List<String>? =
        state.lifeText[K_LATEST]?.split("\n")

    /** The latest edition's headline, verbatim, or null
     *  before the first edition. (Line 3 of the frozen
     *  edition — masthead and week line ride above it by
     *  construction.) */
    fun latestHeadline(state: GameState): String? =
        porchLines(state)?.getOrNull(2)

    /** The phone summary is owed: an edition published and
     *  its summary text has not landed yet. Publication
     *  stamps the debt; the phone pays it on the first
     *  morning a budget lets it — a crowded morning
     *  defers the text, never the edition. Saves from
     *  before the summary existed carry no stamp, so no
     *  old edition is ever texted about after the fact. */
    fun summaryDue(state: GameState): Boolean =
        (state.life[K_SUMMARY_DUE] ?: 0) > 0 &&
            publishedWeek(state) != null

    /** The phone paid the summary debt. */
    fun markSummarySent(state: GameState) {
        state.life.remove(K_SUMMARY_DUE)
    }

    /** The cast subscriber who texts the summary for the
     *  published week: the rotation walks the whole cast
     *  by week, so the voice changes as the weeks turn.
     *  Kenny never carries it — the headline rides
     *  verbatim, and the phone's own render muffles
     *  Kenny's texts; a muffled headline is no headline. */
    fun summarySender(state: GameState): String {
        val pool = KID_ORDER.filter { it != "Kenny" }
        val week = publishedWeek(state) ?: 0
        return pool[((week % pool.size) + pool.size) % pool.size]
    }

    // ---------------- the Journal page ----------------

    /**
     * The 📰 THE PAPER page: before the first edition, the
     * masthead and the promise; after it, the frozen latest
     * edition whole, then PAST EDITIONS — the headline
     * history, all but the latest, oldest first.
     */
    fun pageLines(state: GameState): List<String> {
        val latest = state.lifeText[K_LATEST]
            ?: return listOf(
                "📰 THE PAPER",
                "The town's weekly paper, written entirely from " +
                    "the record — votes counted, contests played, " +
                    "fairs wrapped, skies watched.",
                "The first edition prints at the turn of the " +
                    "first week you live in town. Nothing is " +
                    "invented: if it didn't happen, the paper " +
                    "says so.")
        val out = latest.split("\n").toMutableList()
        val past = headlineHistory(state).dropLast(1)
        if (past.isNotEmpty()) {
            out += "🗞 PAST EDITIONS"
            for ((w, h) in past) out += "Week $w: $h"
        }
        return out
    }
}

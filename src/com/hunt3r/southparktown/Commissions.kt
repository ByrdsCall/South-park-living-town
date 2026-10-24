package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Commissions — v0.6.0a (Queue #6 item 1): photo assignments.
//
// The town stops just posing for your album and starts
// ASKING for specific pictures. A commission is an ask built
// from parts the photo system can judge honestly, and nothing
// else:
//
//   SUBJECT   one named townsfolk, a named pair, or a named
//             trio (the asker may star in their own ask).
//   PLACE     one location — indoors or out, exactly as the
//             record's (locId, inside) pair reports it.
//   CONDITION (optional) one of:
//             comp    a posed GROUP frame (a named composition
//                     for pairs/mixed trios, "any group frame"
//                     where the composition math is chemistry-
//                     dependent — see compApplies),
//             season  the season on the photo's own day stamp,
//             weather a weather family on the record.
//
// THE BOARD IS A PURE FUNCTION OF THE WEEK, like the
// errand board is of the day: postingsForWeek(week, key,
// lastSigs) derives the same five asks on every screen,
// stored nowhere. The generator composes asks ONLY from
// schedule-proven pieces: a subject set is offered at a
// place only when the cast's own agendas (AGENT_PROFILES)
// put every subject at that place at the same minute of
// some day — co-presence is computed from the agenda
// intervals, never hoped for. Conditions are gated the
// same way (a season ask only posts when the whole
// two-week live window sits inside that season; a weather
// family only when the season's drift table can produce
// it; a composition only when Photos.compositionFor would
// yield it for exactly that set on static data alone).
// An asker never repeats their last posted ask: the
// signature of each week's postings is folded into
// state.commissionLast at the week boundary, and the
// generator re-rolls variants until the signature moves.
//
// ACCEPTED asks live on a small plate (MAX_PLATE) in
// GameState.commissions (additive save key, decode-capped),
// live for EXPIRY_DAYS from acceptance, and expire free —
// no friendship hit, ever; the errand disappointment rail
// is for work taken and dropped, and even that is only a
// sigh. Judging rides Photos.applyPhoto itself (onPhoto,
// next to Errands.onPhoto and its kin): a full match pays
// Capsule Coins scaled by the ask's difficulty + friendship
// with the asker, and the record is STAMPED a commissioned
// print — a print never fades from the album (the cap
// sheds uncommissioned photos first). A near miss (right
// people, wrong place or frame) is not silent: the asker
// says "close, but…" next time you talk, once, honestly.
// ============================================================

data class Commission(
    val id: String,              // "c<week>:<slot>"
    val asker: String,           // cast name
    val subjects: List<String>,  // 1..3 cast names (may include the asker)
    val locId: String,
    val condKind: String = "",   // "" | comp | season | weather
    val condValue: String = "",  // comp id | Season name | weather family
    val text: String,            // the ask itself, in the asker's voice
    val pay: Int = 0,            // Capsule Coins on completion
    val week: Int = 0,           // posting week index (absDay / 7)
    var acceptedDay: Int = -1,   // absDay accepted (-1 = a board posting)
    var expiresDay: Int = -1,    // last absDay the ask can still land
    var done: Boolean = false
) {
    /** The no-repeat identity: who, where, under what
     *  condition — the words may re-roll, the ask may not. */
    val signature: String
        get() = "$asker|${subjects.sorted().joinToString(",")}|$locId|" +
            "$condKind:$condValue"

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "asker" to asker, "subjects" to subjects,
        "locId" to locId, "condKind" to condKind, "condValue" to condValue,
        "text" to text, "pay" to pay, "week" to week,
        "acceptedDay" to acceptedDay, "expiresDay" to expiresDay,
        "done" to done
    )

    companion object {
        fun fromMap(m: Map<String, Any?>): Commission? {
            val id = m["id"]?.toString()?.takeIf { it.isNotEmpty() } ?: return null
            val asker = m["asker"]?.toString()
                ?.takeIf { it in KID_ORDER } ?: return null
            val subjects = (m["subjects"] as? List<Any?>)
                ?.mapNotNull { it?.toString()?.takeIf { s -> s in KID_ORDER } }
                ?.distinct() ?: return null
            if (subjects.isEmpty() || subjects.size > 3) return null
            val locId = m["locId"]?.toString()
                ?.takeIf { it in LOCATIONS } ?: return null
            val condKind = (m["condKind"]?.toString() ?: "")
                .takeIf { it in Commissions.COND_KINDS + "" } ?: ""
            return Commission(
                id = id, asker = asker, subjects = subjects, locId = locId,
                condKind = condKind,
                condValue = m["condValue"]?.toString() ?: "",
                text = m["text"]?.toString() ?: "",
                pay = ((m["pay"] as? Number)?.toInt() ?: 0).coerceIn(0, 99),
                week = ((m["week"] as? Number)?.toInt() ?: 0).coerceAtLeast(0),
                acceptedDay = (m["acceptedDay"] as? Number)?.toInt() ?: -1,
                expiresDay = (m["expiresDay"] as? Number)?.toInt() ?: -1,
                done = m["done"] as? Boolean ?: false
            )
        }
    }
}

/** One line of print history (the COMMISSIONS page's
 *  "on the wall" list). */
data class CommissionNote(val id: String, val dayKey: String, val line: String) {
    fun toMap(): Map<String, Any?> = mapOf("id" to id, "day" to dayKey, "line" to line)

    companion object {
        fun fromMap(m: Map<String, Any?>): CommissionNote? {
            val line = m["line"]?.toString()?.takeIf { it.isNotEmpty() } ?: return null
            return CommissionNote(m["id"]?.toString() ?: "",
                m["day"]?.toString() ?: "", line)
        }
    }
}

/** What a landed commission paid — the UI toasts from this. */
data class CommissionCompletion(
    val commission: Commission,
    val coins: Int,
    val friendship: Int
)

object Commissions {

    // ---------------- the dials (all suite-pinned) ----------------

    /** One game week: the board's rotation period. */
    const val WEEK_DAYS = 7

    /** Fresh commissions pinned up every week. */
    const val POSTINGS = 5

    /** Commissions on your plate at once. */
    const val MAX_PLATE = 3

    /** Days an accepted ask stays live (acceptance day
     *  counts as day one). */
    const val EXPIRY_DAYS = 7

    /** Friendship paid on a landed commission. */
    const val FRIENDSHIP = 5

    /** Coin pay by subject count, before the condition bonus. */
    const val PAY_ONE = 2
    const val PAY_PAIR = 3
    const val PAY_GROUP = 5

    /** Extra coins when the ask carries a condition. */
    const val COND_BONUS = 2

    /** Decode-side ceiling for the stored plate. */
    const val STATE_CAP = MAX_PLATE + 6

    /** Print history kept for the page. */
    const val RECENT_CAP = 8

    val COND_KINDS = setOf("comp", "season", "weather")

    /** Weather families a condition can name, as record
     *  weatherIds. Every family's members carry a positive
     *  weight in SEASON_WEATHER_MULT for every season — the
     *  suite pins that, so a weather ask is always honestly
     *  offerable (the season can produce it). */
    val WEATHER_FAMILIES: Map<String, Set<String>> = mapOf(
        "snow" to setOf("light_snow", "heavy_snow", "blizzard"),
        "sun" to setOf("sunny"),
        "rain" to setOf("rain")
    )

    private val WEATHER_PHRASES = mapOf(
        "snow" to "while the snow's coming down",
        "sun" to "in actual sunshine",
        "rain" to "in the rain"
    )

    // ---------------- calendar plumbing ----------------

    /** The date key of any absDay, from the state's own
     *  (todayKey, absDay) anchor — pure ordinal math. */
    fun keyForAbsDay(state: GameState, absDay: Int): String =
        keyFromOrdinal(ordinalOfKey(state.todayKey) - state.absDay + absDay)

    fun weekIndex(state: GameState): Int = state.absDay / WEEK_DAYS

    private fun weekStartKey(state: GameState, week: Int): String =
        keyForAbsDay(state, week * WEEK_DAYS)

    private fun dateLabel(key: String): String =
        if (key.length == 8)
            "${key.substring(4, 6).toInt()}/${key.substring(6, 8).toInt()}"
        else ""

    private fun seasonOfKey(key: String): Season =
        seasonFor(key.substring(4, 6).toIntOrNull() ?: 10)

    /** The season a whole span sits in, or null if the span
     *  [fromAbsDay, fromAbsDay + days) straddles a boundary. */
    private fun constantSeason(state: GameState, fromAbsDay: Int,
                               days: Int): Season? {
        val first = seasonOfKey(keyForAbsDay(state, fromAbsDay))
        for (d in 1 until days)
            if (seasonOfKey(keyForAbsDay(state, fromAbsDay + d)) != first)
                return null
        return first
    }

    // ---------------- the co-presence index ----------------
    // Where the cast's own schedules put them, as minute
    // intervals per person. Sleep blocks are not presence —
    // a photo needs them awake (the selfie flow filters
    // asleep figures the same way). Built once from
    // AGENT_PROFILES; the generator and the suite read the
    // same index, and the suite re-derives it independently.

    /** [name] awake at [locId] during [from, to) minutes. */
    private data class Span(val name: String, val locId: String,
                            val from: Int, val to: Int)

    private val spans: List<Span> by lazy {
        val out = mutableListOf<Span>()
        for ((name, p) in AGENT_PROFILES) {
            val blocks = p.agenda.entries
                .sortedBy { timeStrToMinutes(it.key) }
            if (blocks.isEmpty()) continue
            // the small hours belong to yesterday's last block
            val firstStart = timeStrToMinutes(blocks.first().key)
            val last = blocks.last().value
            if (firstStart > 0 && last.activity != SLEEP_ACTIVITY)
                out += Span(name, last.location, 0, firstStart)
            for (i in blocks.indices) {
                val b = blocks[i].value
                if (b.activity == SLEEP_ACTIVITY) continue
                val from = timeStrToMinutes(blocks[i].key)
                val to = if (i + 1 < blocks.size)
                    timeStrToMinutes(blocks[i + 1].key) else 1440
                if (to > from) out += Span(name, b.location, from, to)
            }
        }
        out
    }

    private fun overlaps(a: Span, b: Span): Boolean =
        a.locId == b.locId && a.from < b.to && b.from < a.to

    /** Every cast member who is ever awake at [locId]. */
    fun singlesAt(locId: String): List<String> =
        spans.filter { it.locId == locId }.map { it.name }
            .distinct().sorted()

    /** Every pair ever awake at [locId] at the same minute. */
    fun pairsAt(locId: String): List<Pair<String, String>> {
        val here = spans.filter { it.locId == locId }
        val out = mutableListOf<Pair<String, String>>()
        val names = here.map { it.name }.distinct().sorted()
        for (i in names.indices) for (j in i + 1 until names.size) {
            val a = here.filter { it.name == names[i] }
            val b = here.filter { it.name == names[j] }
            if (a.any { sa -> b.any { sb -> overlaps(sa, sb) } })
                out += names[i] to names[j]
        }
        return out
    }

    /** Every trio ever awake at [locId] at the same minute.
     *  Intervals on a line: if each pair overlaps, all three
     *  share a minute (Helly), so trios are exactly the
     *  triangles of the pair graph. */
    fun triosAt(locId: String): List<List<String>> {
        val pairs = pairsAt(locId)
        val pairSet = pairs.map { setOf(it.first, it.second) }.toSet()
        val names = singlesAt(locId)
        val out = mutableListOf<List<String>>()
        for (i in names.indices) for (j in i + 1 until names.size)
            for (k in j + 1 until names.size) {
                val t = listOf(names[i], names[j], names[k])
                if (setOf(t[0], t[1]) in pairSet &&
                    setOf(t[0], t[2]) in pairSet &&
                    setOf(t[1], t[2]) in pairSet) out += t
            }
        return out
    }

    // ---------------- text banks ----------------

    private fun nameList(names: List<String>): String = when (names.size) {
        0 -> ""
        1 -> names[0]
        2 -> "${names[0]} & ${names[1]}"
        else -> "${names.dropLast(1).joinToString(", ")} & ${names.last()}"
    }

    /** The subject phrase as the ASKER says it — a solo ask
     *  about the asker themselves is "me", not their name. */
    private fun whoPhrase(c: Commission): String =
        if (c.subjects.size == 1 && c.subjects[0] == c.asker) "me"
        else nameList(c.subjects)

    private fun condPhrase(c: Commission): String = when (c.condKind) {
        "comp" -> when (c.condValue) {
            Photos.COMP_LINEUP -> "posed in a proper line-up — a 👥 group frame"
            Photos.COMP_SQUAD -> "posed as a squad — a 👥 group frame"
            else -> "in a posed 👥 group frame"
        }
        "season" -> "while it's ${c.condValue.lowercase()}"
        "weather" -> WEATHER_PHRASES[c.condValue] ?: ""
        else -> ""
    }

    private val ASK_LINES_ONE = listOf(
        "I need a photo of {who} at {place}. {cond}It's for the fridge door, which in this house is a serious gallery.",
        "Get me {who}, at {place}. {cond}I've decided my walls have opinions and they're all wrong.",
        "One good picture of {who} at {place} — {cond}that's the whole ask. I'm framing it either way, so no pressure. (Some pressure.)"
    )
    private val ASK_LINES_PAIR = listOf(
        "Those two. {who}. At {place}. {cond}Together, in one frame — proof they can be in the same place without incident.",
        "I want {who} at {place}, together. {cond}For the scrapbook. The scrapbook is real and it has standards.",
        "{who}, {place}, one photo. {cond}People keep saying it happened. I want evidence."
    )
    private val ASK_LINES_GROUP = listOf(
        "The whole crew: {who}. At {place}. {cond}Getting them in one frame is a logistical miracle and I need it documented.",
        "One photo, {who}, all of them, at {place}. {cond}It's for a completely normal project I will not be explaining.",
        "{who} — together, at {place}. {cond}If you pull this off, you're officially the town's photographer."
    )

    /** Named voices that bend the generic banks (asker, size). */
    private val SPECIAL_LINES: Map<Pair<String, Int>, List<String>> = mapOf(
        ("Cartman" to 1) to listOf(
            "A photo of {who} at {place}. {cond}It's for a presentation. The presentation is about how great I am. This is not negotiable."),
        ("Randy" to 2) to listOf(
            "{who}. At {place}. {cond}Sharon says I never document anything. This photo is my EVIDENCE, kid. My whole case rests on it."),
        ("Butters" to 3) to listOf(
            "Oh boy — {who}, all together at {place}! {cond}I'll probably get grounded for asking, but it'll be worth it for the album!"),
        ("McDaniels" to 2) to listOf(
            "City business: {who}, photographed at {place}. {cond}There's a form for this. The form is mostly me pointing at this conversation.")
    )

    private fun askText(asker: String, subjects: List<String>,
                        locId: String, condKind: String, condValue: String,
                        rnd: Random): String {
        val probe = Commission("", asker, subjects, locId, condKind, condValue, "")
        val bank = SPECIAL_LINES[asker to subjects.size]
            ?: when (subjects.size) {
                1 -> ASK_LINES_ONE
                2 -> ASK_LINES_PAIR
                else -> ASK_LINES_GROUP
            }
        val cond = condPhrase(probe).let { if (it.isEmpty()) "" else "$it, " }
        return bank[rnd.nextInt(bank.size)]
            .replace("{who}", whoPhrase(probe))
            .replace("{place}", LOCATIONS[locId]?.name ?: locId)
            .replace("{cond}", cond)
            .replaceFirstChar { it.uppercase() }
    }

    // ---------------- generation ----------------

    /** Candidate (place, subjects) sets of one shape for one
     *  asker, best tier first: sets starring the asker, then
     *  sets touching the asker's relations (or a recognized
     *  pair), then anything the schedules allow. Tier order
     *  is preference, not a wall — the roll falls through. */
    private fun candidates(shape: Int, asker: String)
            : List<List<Pair<String, List<String>>>> {
        val tiers = listOf(mutableListOf<Pair<String, List<String>>>(),
            mutableListOf(), mutableListOf())
        val relations = relationsOf(asker).toSet()
        for (loc in LOCATIONS.keys) {
            val sets: List<List<String>> = when (shape) {
                1 -> singlesAt(loc).map { listOf(it) }
                2 -> pairsAt(loc).map { listOf(it.first, it.second) }
                else -> triosAt(loc)
            }
            for (s in sets) {
                val entry = loc to s
                when {
                    asker in s -> tiers[0] += entry
                    s.any { it in relations } ||
                        (s.size >= 2 && Photos.isRecognizedPair(s[0], s[1])) ->
                        tiers[1] += entry
                    else -> tiers[2] += entry
                }
            }
        }
        return tiers
    }

    /** The compositions honestly askable for exactly this
     *  subject set, read off Photos.compositionFor's static
     *  branches: a pair lines up; a trio of pure kids piles
     *  (chemistry's call — so only "any group frame" is
     *  promiseable); any other trio squads up. */
    fun compApplies(subjects: List<String>): List<String> = when {
        subjects.size == 2 -> listOf(Photos.COMP_LINEUP, "group")
        subjects.size == 3 -> if (subjects.all {
                val b = CharacterSystem.bandOf(it)
                b == AgeBand.KID || b == AgeBand.YOUNG_TEEN
            }) listOf("group")
        else listOf(Photos.COMP_SQUAD, "group")
        else -> emptyList()
    }

    /** A weather family the given season's drift table can
     *  actually produce (positive multiplier somewhere in
     *  SEASON_WEATHER_MULT). */
    fun weatherApplies(family: String, season: Season): Boolean {
        val ids = WEATHER_FAMILIES[family] ?: return false
        val mult = SEASON_WEATHER_MULT[season] ?: return false
        return ids.any { (mult[it] ?: 0.0) > 0.0 }
    }

    /** Build one ask for [asker] off the caller's stream —
     *  every field drawn in a fixed order, so the same
     *  stream always builds the same ask. */
    private fun buildAsk(rnd: Random, asker: String, week: Int, slot: Int,
                         seasonConstant: Season?, weekSeason: Season): Commission {
        val shape = when (rnd.nextInt(100)) {
            in 0..37 -> 1
            in 38..71 -> 2
            else -> 3
        }
        val tiers = candidates(shape, asker)
        var pick: Pair<String, List<String>>? = null
        val tierRoll = rnd.nextInt(100)
        val order = when {
            tierRoll < 45 -> listOf(0, 1, 2)
            tierRoll < 80 -> listOf(1, 0, 2)
            else -> listOf(2, 1, 0)
        }
        for (t in order) {
            if (tiers[t].isNotEmpty()) {
                pick = tiers[t][rnd.nextInt(tiers[t].size)]
                break
            }
        }
        val (locId, subjects) = pick ?: ("BusStop" to listOf(asker))
        // ---- the condition: half of asks carry one ----
        var condKind = ""
        var condValue = ""
        if (rnd.nextInt(100) < 50) {
            val options = mutableListOf<Pair<String, String>>()
            for (cv in compApplies(subjects)) options += "comp" to cv
            if (seasonConstant != null)
                options += "season" to seasonConstant.name
            for (f in WEATHER_FAMILIES.keys)
                if (weatherApplies(f, weekSeason)) options += "weather" to f
            if (options.isNotEmpty()) {
                val (k, v) = options[rnd.nextInt(options.size)]
                condKind = k; condValue = v
            }
        }
        val pay = (when (subjects.size) {
            1 -> PAY_ONE
            2 -> PAY_PAIR
            else -> PAY_GROUP
        }) + if (condKind.isNotEmpty()) COND_BONUS else 0
        val text = askText(asker, subjects, locId, condKind, condValue, rnd)
        return Commission("c$week:$slot", asker, subjects, locId,
            condKind, condValue, text, pay, week)
    }

    /** One week's postings — a PURE function of the week,
     *  its calendar, and the askers' last signatures (the
     *  no-repeat memory). Same inputs, same five asks, on
     *  every screen and every run. */
    fun postingsForWeek(week: Int, weekStartKey: String,
                        lastSig: Map<String, String>,
                        seasonConstant: Season?,
                        weekSeason: Season): List<Commission> {
        val rnd = Random(dailySeed(weekStartKey) * 977L + week * 131L + 0xCA11L)
        val askers = KID_ORDER.shuffled(rnd).take(POSTINGS)
        val out = mutableListOf<Commission>()
        for ((slot, asker) in askers.withIndex()) {
            var built: Commission? = null
            for (v in 0..5) {
                val local = Random(dailySeed(weekStartKey) * 31L +
                    asker.hashCode().toLong() * 17L + slot * 101L + v * 7919L)
                val c = buildAsk(local, asker, week, slot,
                    seasonConstant, weekSeason)
                built = c
                if (c.signature != lastSig[asker]) break
            }
            out += built!!
        }
        return out
    }

    /** Week [week]'s postings as derived from the live
     *  state's calendar + memory. The season gate spans the
     *  posting week plus the acceptance week — an ask must
     *  be landable for its whole life. postingsFor and the
     *  rollover's signature fold both come through here, so
     *  the fold replays exactly what the week showed. */
    private fun postingsForWeekOf(state: GameState,
                                  week: Int): List<Commission> =
        postingsForWeek(week, weekStartKey(state, week),
            state.commissionLast,
            constantSeason(state, week * WEEK_DAYS,
                WEEK_DAYS + EXPIRY_DAYS),
            seasonOfKey(weekStartKey(state, week)))

    /** This week's postings for the live state. */
    fun postingsFor(state: GameState): List<Commission> =
        postingsForWeekOf(state, weekIndex(state))

    /** The postings still on the cork: not taken (they live
     *  on your plate now) and not already landed this week. */
    fun boardOpen(state: GameState): List<Commission> {
        val taken = state.commissions.map { it.id }.toSet()
        val doneIds = state.commissionRecent.map { it.id }.toSet()
        return postingsFor(state).filter { it.id !in taken && it.id !in doneIds }
    }

    // ---------------- lifecycle ----------------

    fun active(state: GameState): List<Commission> =
        state.commissions.filter { !it.done }

    fun activeCount(state: GameState): Int =
        state.commissions.count { !it.done }

    fun lastNote(state: GameState): CommissionNote? =
        state.commissionRecent.lastOrNull()

    /** Put a commission on your plate. Refuses past
     *  MAX_PLATE — land one or let one lapse first. The
     *  ask lives EXPIRY_DAYS from today, whenever in its
     *  posting week you took it. */
    fun accept(state: GameState, c: Commission): Boolean {
        if (activeCount(state) >= MAX_PLATE) return false
        if (state.commissions.any { it.id == c.id }) return false
        state.commissions.add(c.copy(
            acceptedDay = state.absDay,
            expiresDay = state.absDay + EXPIRY_DAYS - 1))
        return true
    }

    private val SIGH_LINES = listOf(
        "Oh. The photo thing? Never mind, then. I'll just… remember it wrong, forever.",
        "No picture, then. Fine. My memory is basically a camera. A bad one. But fine.",
        "Alright. Scrapbook stays empty on that page. It has other pages. Probably."
    )

    /** Drop an accepted commission. The only cost is the
     *  sigh — expiry and abandonment are both free, forever. */
    fun abandon(state: GameState, commissionId: String): String? {
        val idx = state.commissions.indexOfFirst {
            it.id == commissionId && !it.done
        }
        if (idx < 0) return null
        val c = state.commissions.removeAt(idx)
        val h = (c.id.hashCode() and 0x7fffffff) % SIGH_LINES.size
        return SIGH_LINES[h]
    }

    // ---------------- judging ----------------

    enum class Verdict { MATCH, NEAR, MISS }

    /** Judge one held commission against one real record.
     *  Pure over the record's own fields — subjects by name,
     *  place by (locId, inside), condition by the record's
     *  composition / day stamp / weather. NEAR is subjects
     *  right and anything else wrong: the asker will have
     *  honest words about it, not silence. */
    fun judge(c: Commission, rec: PhotoRecord): Verdict {
        val names = rec.npcs.map { it.name }.toSet()
        if (!c.subjects.all { it in names }) return Verdict.MISS
        val placeOk = rec.locId == c.locId &&
            rec.inside == CharacterSystem.npcInside(c.locId)
        val condOk = when (c.condKind) {
            "" -> true
            "comp" -> rec.isGroup &&
                (c.condValue == "group" || rec.composition == c.condValue)
            "season" -> rec.dayKey.length == 8 &&
                seasonFor(rec.dayKey.substring(4, 6).toIntOrNull() ?: 0)
                    .name == c.condValue
            "weather" -> rec.weatherId in
                (WEATHER_FAMILIES[c.condValue] ?: emptySet())
            else -> false
        }
        return if (placeOk && condOk) Verdict.MATCH else Verdict.NEAR
    }

    private val NEAR_LINES = listOf(
        "Close, but… that's not quite the shot I asked for. Right people! Wrong everything else. The offer stands, though.",
        "Hmm. I see what you were going for. It is not what I was going for. But A for effort — the commission's still open.",
        "That's… adjacent to the photo I commissioned. Adjacent isn't the fridge door, friend. Try again?"
    )

    /** The shared completion: pay, friendship, the stamp,
     *  the records, the asker's morning text. */
    fun complete(state: GameState, c: Commission,
                 rec: PhotoRecord): CommissionCompletion? {
        val live = state.commissions.firstOrNull { it.id == c.id && !it.done }
            ?: return null
        live.done = true
        if (live.pay > 0) Minigames.awardCoins(state, live.pay)
        if (live.asker in state.friendship)
            Engine().relAdjust(state, live.asker, FRIENDSHIP)
        else {
            val mem = NpcMemoryStore.getOrCreate(live.asker)
            mem.points = (mem.points + FRIENDSHIP).coerceIn(-50, 100)
        }
        // ---- the stamp: the FIRST commission a photo lands
        // owns its print; a second ask it also satisfies
        // still pays and is still recorded (below) ----
        if (rec.commissionAsker.isEmpty()) {
            rec.commissionAsker = live.asker
            rec.commissionText = live.text
        }
        state.commissionsDone++
        Phone.noteCommission(state, live.asker)
        state.commissionRecent.add(CommissionNote(live.id, state.todayKey,
            "✔ ${objective(live)} — print for ${live.asker}"))
        while (state.commissionRecent.size > RECENT_CAP)
            state.commissionRecent.removeAt(0)
        state.events.add("📷 Commission landed — ${objective(live)} " +
            "(+${FRIENDSHIP} 💛 ${live.asker}" +
            (if (live.pay > 0) ", +${live.pay} 🪙" else "") + ")")
        while (state.events.size > 40) state.events.removeAt(0)
        state.agents[live.asker]?.memories
            ?.add("📷 The new kid shot the photo I commissioned.")
        return CommissionCompletion(live, live.pay, FRIENDSHIP)
    }

    /** Judged — called from inside Photos.applyPhoto, so a
     *  shutter click settles commissions like it settles
     *  PHOTO goals and errands. Near misses arm the asker's
     *  "close, but…" for your next talk (once a day each). */
    fun onPhoto(state: GameState, rec: PhotoRecord): List<CommissionCompletion> {
        val out = mutableListOf<CommissionCompletion>()
        for (c in active(state).toList()) {
            when (judge(c, rec)) {
                Verdict.MATCH -> complete(state, c, rec)?.let { out += it }
                Verdict.NEAR -> {
                    if (state.life["commNearDay:${c.asker}"] != state.absDay) {
                        state.life["commNearDay:${c.asker}"] = state.absDay
                        val h = (c.id.hashCode() and 0x7fffffff) %
                            NEAR_LINES.size
                        state.lifeText["commNear:${c.asker}"] = NEAR_LINES[h]
                    }
                }
                Verdict.MISS -> {}
            }
        }
        return out
    }

    // ---------------- the Talk sheet ----------------

    /** Lines [npcName] has for you about commissions, for
     *  the talk pre-lines: a pending "close, but…" plays
     *  exactly once (the removal IS the playing, the
     *  errand-thanks pattern), and a held commission earns
     *  one reminder a day. */
    fun talkLines(state: GameState, npcName: String): List<Line> {
        val out = mutableListOf<Line>()
        state.lifeText.remove("commNear:$npcName")?.let { near ->
            out.add(Line(npcName, near))
        }
        val held = active(state).firstOrNull { it.asker == npcName }
        if (held != null &&
            state.lifeText["commRemind:$npcName"] != state.todayKey) {
            state.lifeText["commRemind:$npcName"] = state.todayKey
            out.add(Line(npcName,
                "About that photo I commissioned — ${objective(held)}. " +
                    "No rush. (Some rush. ${hintFor(state, held)})"))
        }
        return out
    }

    // ---------------- daily rollover ----------------

    /** Midnight: landed commissions leave the plate (their
     *  history is the count + the recent list), expired ones
     *  lapse free with a quiet ticker obituary, and at a
     *  week boundary the outgoing weeks' posting signatures
     *  fold into the no-repeat memory — replayed in order,
     *  so the memory is exactly what the generator saw. */
    fun dailyRollover(state: GameState) {
        state.commissions.removeAll { it.done }
        val expired = active(state).filter { state.absDay > it.expiresDay }
        for (c in expired) {
            state.commissions.remove(c)
            state.events.add("📷 ${c.asker}'s commission expired " +
                "unclaimed — no hard feelings.")
            while (state.events.size > 40) state.events.removeAt(0)
        }
        val wk = weekIndex(state)
        val seen = state.life["commWeek"] ?: wk
        if (seen < wk) {
            for (w in seen until wk) {
                for (p in postingsForWeekOf(state, w))
                    state.commissionLast[p.asker] = p.signature
            }
        }
        if (seen != wk) state.life["commWeek"] = wk
    }

    // ---------------- words for the screens ----------------

    /** The one-line objective (Journal / dialogs / ticker). */
    fun objective(c: Commission): String {
        val place = LOCATIONS[c.locId]?.name ?: c.locId
        val cond = condPhrase(c).let { if (it.isEmpty()) "" else ", $it" }
        return "Photo of ${nameList(c.subjects)} at $place$cond"
    }

    /** The how-to line. Live like the errands' hints. */
    fun hintFor(state: GameState, c: Commission): String {
        val place = LOCATIONS[c.locId]?.name ?: c.locId
        val inside = CharacterSystem.npcInside(c.locId)
        val where = if (inside) "inside $place" else "at $place"
        val frame = when {
            c.condKind == "comp" && c.condValue == "group" ->
                " Pose a 👥 GROUP frame with them in it."
            c.condKind == "comp" ->
                " Pose a 👥 GROUP frame (${c.condValue})."
            c.subjects.size >= 2 ->
                " A 📸 selfie or a 👥 group frame both count."
            else -> ""
        }
        return "📸 SELFIE $where — ${nameList(c.subjects)} " +
            "are there on their rounds.$frame"
    }

    private fun expiryLabel(state: GameState, c: Commission): String =
        "through ${dateLabel(keyForAbsDay(state, c.expiresDay))}"

    /** The 📷 COMMISSIONS page, assembled pure like the
     *  errand page: your plate, this week's cork, the wall
     *  of landed prints. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "📷 COMMISSIONS — ${state.commissionsDone} print(s) on the wall"
        val act = active(state)
        if (act.isEmpty()) {
            out += "Your plate is empty. This week's asks are below — " +
                "take one and the town poses with intent."
        } else {
            out += "ON YOUR PLATE (${act.size}/$MAX_PLATE) — tap this card to manage:"
            for (c in act) {
                out += "▶ ${objective(c)} — for ${c.asker} · " +
                    "pays ${c.pay} 🪙 · ${expiryLabel(state, c)}"
                out += "   ${hintFor(state, c)}"
            }
        }
        out += "📌 THIS WEEK'S ASKS (fresh every ${WEEK_DAYS} days):"
        val open = boardOpen(state)
        if (open.isEmpty()) {
            out += "· bare — every ask's spoken for. Fresh ink next week."
        } else {
            for (c in open) {
                out += "· ${c.asker}: ${objective(c)} — pays ${c.pay} 🪙"
            }
        }
        if (state.commissionRecent.isNotEmpty()) {
            out += "🖨 ON THE WALL:"
            for (n in state.commissionRecent.takeLast(3).reversed())
                out += "· ${n.line}"
        }
        return out
    }
}

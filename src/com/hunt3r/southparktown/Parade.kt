package com.hunt3r.southparktown

import kotlin.math.abs
import kotlin.random.Random

// ============================================================
// 🎺 THE TOWN PARADE (v0.13.0d) — the Founders' Day Parade,
// the FOURTH of the town's seasonal anchors (after the
// winter carnival, the school dance and the talent show),
// and the first one that belongs to the whole town at once:
// on the third Saturday of May, every structure the town
// has built — its households, its factions, its school,
// its movie house, its city hall, and (if they stand) your
// stand and your crew — rolls down Main Street in an order
// DERIVED from those structures on every read, never
// invented here. Nobody is in two places: the procession
// is built in CLAIM order (families first — a family rides
// together or not at all; then your stand's staff and your
// crew, the player's own people; then the mayor's car, so
// no faction float can carry the mayor off; then the
// Bijou's feature stars; then the factions, whose floats
// carry a stated crew of riders while the rest of the
// faction watches from the curb), and DISPLAYED in
// PROCESSION order (the school leads, the households roll,
// the Bijou, the factions, your entries, and the Mayor's
// car brings up the rear — the mayor rides last; that is
// the town's order, and it is stated here, not discovered).
//
// THE ANCHOR: the third Saturday of May (always May
// 15–21). The pocket is the emptiest on the town's
// calendar and the suite proves it in ONE lived ordinal
// space, 2026–2100: never within three weeks of the
// dance (February), the talent show (April, always ≥31
// days clear), a play opening, the fair's week, the
// carnival's week, ANY finale window (the spring finale
// owns May 25–31; the parade is always ≥4 days clear of
// it) or ANY holiday window (May holds none at all).
// Runtime yields follow the dance/talent discipline
// anyway — the standard owners (a case climax due, a
// finale window, a holiday beat, a play opening at hand)
// take the day whole, the reason is stamped where it is
// learned and printed wherever the player would look,
// and the year is skipped, never postponed.
//
// YOUR DAY, two honest ways in: MARCH (with your crew if
// you have one — the entry is yours even if your whole
// crew was claimed by their families' floats, in which
// case you carry the banner and they wave from the
// floats; with no crew you march as a stated free-agent
// walker with the school entry) or WATCH from one of four
// named curb spots on the real Main Street frontage (the
// bank steps, the Bijou marquee, the post office corner,
// the City Hall steps), with the floats passing YOUR spot
// in procession order and the people actually standing
// near you reacting. Either way the day ends at the
// Mayor's car: McDaniels' closing line (authored here, in
// the finale-beat pattern — a closing card, no new rail)
// and ONE photograph through Photos.composeGroup (watch:
// the float of your choice in frame; march: your entry
// passing the City Hall steps — Main Street has no
// library, and City Hall's reviewing stand is the route's
// ceremonial spot, so that is where the shutter waits).
//
// CANDY, on a stated economy: three entries throw real
// pantry goods (the Cartman float throws snack cakes, the
// Bijou's float and the Mayor's car throw cheesy poofs —
// the stand's own stock items, at the stand's own
// prices). Each thrower gets ONE catch session of three
// throws on the showdown bar; caught items land in your
// real inventory, and the day's total gain is capped at
// CANDY_CAP (6) — marching throws nothing to marchers and
// pays its stated friendship instead, so the parade can
// never be a pantry farm: the maximum possible haul is
// 6 items / 12 🪙 of pantry value, once a year, and the
// suite COMPUTES it rather than trusting this comment.
//
// THE CLAIM: seats EIGHTEENTH in the agenda stack (after
// the talent show, before sleepovers — pinned in-suite
// from Engine's own source). The window is a daytime one
// — 10:00 to 13:30 — and it gathers every rider and every
// watcher to Main Street. Who is NOT in the procession or
// the route crowd is not claimed. Street traffic yields
// honestly: Walk's pure route geography is UNTOUCHED (a
// route through Main Street is still the route); what
// changes is the price of passing THROUGH the closed
// stretch mid-trip — one stated tick (+15 minutes) added
// by walkLeg when the leg enters Main Street without
// arriving there, and by routeMinutes when the path's
// intermediate stops include Main Street, so the picker
// and the charge always agree. Trips TO Main Street pay
// no delay (you are going TO the parade), trips OUT of it
// pay none, and the bus (raw-leg pricing) skips the
// closure by design.
//
// Saves: additive only — every parade fact lives under
// life/lifeText "parade…" keys; the year's record rides
// lifeText["paradeRec:<year>"] in Talent's pipe format.
// A pre-parade save simply has no record until the cycle
// touches it, and reads never create one (the rollover
// opens the record for untouched years, like the talent
// show's, so the paper and the page can carry the day).
// ============================================================

object ParadeCalendar {
    /** The parade's ordinal: the THIRD Saturday of May —
     *  the first Saturday on/after May 1, plus two weeks.
     *  Always May 15–21, always a Saturday. Pure calendar
     *  math over FairCalendar's proleptic Gregorian
     *  ordinals — the one time space every anchor shares. */
    fun paradeOrdinal(year: Int): Int {
        val may1 = ordinalOfKey("%04d0501".format(year))
        val dow = FairCalendar.dowOfOrdinal(may1)
        val toSaturday = (6 - dow + 7) % 7   // 6 = Saturday
        return may1 + toSaturday + 14
    }

    fun paradeDay(year: Int): Int =
        FairCalendar.absDayOfOrdinal(paradeOrdinal(year))

    /** The poster morning: a week ahead, like the dance
     *  and the talent show. */
    fun announceDay(year: Int): Int =
        paradeDay(year) - Parade.ANN_BACK
}

object Parade {

    const val STREET = "MainStreet"
    const val NAME = "The Founders' Day Parade"

    // ---------------- the dials (the tuning ledger) ----------------
    // One place for every parade number, pinned in-suite:
    // the window is a morning-to-early-afternoon one
    // (10:00 step-off, the street handed back at 13:30);
    // the poster rides a week ahead; the pass plays in
    // chunks of four entries; a faction float carries at
    // most six riders (its first-listed unclaimed member
    // always among them); three entries throw candy, three
    // throws each, and your pockets hold six — the cap,
    // not the throws, is the economy; marching pays your
    // walkers +1 at the wrap; passing THROUGH the closed
    // street costs one tick; a photo frame holds twelve.
    const val PARADE_FROM = 10 * 60          // 10:00
    const val PARADE_TO = 13 * 60 + 30       // 13:30
    const val ANN_BACK = 7
    const val CURRENT_TAIL = 30
    const val CHUNK = 4
    const val FACTION_CREW_CAP = 6
    const val THROWS_PER = 3
    const val CANDY_CAP = 6
    const val MARCH_PAY = 1
    const val PASS_DELAY_MIN = 15
    const val PHOTO_CREW_CAP = 12

    /** Your catching hands: the new kid's honest 5 — the
     *  spotlight number the dance and the talent show
     *  already use for you. */
    const val CATCH_NERVE = 5

    /** Who throws candy, and what: the stand's own stock
     *  items, thrown by the entries whose character calls
     *  for it — Liane's float throws snack cakes (she
     *  would), the movie house throws its own concession
     *  candy, and the Mayor's car throws cheesy poofs
     *  because civic candy is cheesy poofs. Your own stand
     *  float throws NOTHING: your own goods landing in your
     *  own pockets would be a loop, not a parade. Keys are
     *  procession entry ids. */
    val THROWERS: Map<String, String> = mapOf(
        "hh:cartman" to "snack_cake",
        "bijou" to "cheesy_poof",
        "cityhall" to "cheesy_poof")

    // ---------------- the curb spots ----------------
    // Four stated places to stand on the real Main Street
    // frontage (Walk's geography — the route runs the
    // street's own span past the bank, the Bijou, the post
    // office and City Hall). The route crowd is assigned
    // to spots by a stated seed, so who stands near you is
    // derived, not cast.
    data class Spot(val id: String, val label: String,
                    val line: String)

    val SPOTS: List<Spot> = listOf(
        Spot("bank", "🏦 The bank steps",
            "the east end of the route, on the bank's " +
                "steps — a good view down the whole street"),
        Spot("bijou", "🎬 Under the Bijou marquee",
            "mid-route, in the marquee's shade — the " +
                "floats slow here for the theater crowd"),
        Spot("postoffice", "✉️ The post office corner",
            "the corner by the post office — the route " +
                "bends here, and every float shows its best " +
                "side turning"),
        Spot("cityhall", "🏛 The City Hall steps",
            "the reviewing stand's own steps — the route " +
                "ends here, and the Mayor's car stops for " +
                "the closing word"))

    fun spot(id: String): Spot? =
        SPOTS.firstOrNull { it.id == id }

    // ---------------- the record ----------------

    /** One year's parade, pipe-serialized in lifeText
     *  under "paradeRec:<year>" — Talent's record format,
     *  worn by a daytime event. Values ride the pipes in
     *  the fixed order parseRecord reads; text fields are
     *  ids and codes only, so nothing user-shaped ever
     *  lands between the pipes. */
    data class ParadeRecord(
        val year: Int,
        var day: Int = -1,
        var mode: String = "",          // "" | march | watch
        var spot: String = "",          // curb spot id (watch)
        var marchEntry: String = "",    // entry id marched with
        var stage: Int = 0,             // 0 untouched · 1 lineup done
        var step: Int = 0,              // pass chunks completed
        // thrower entry id -> items GAINED (post-cap); the
        // key's presence is the settlement, even at 0
        var catches: Map<String, Int> = emptyMap(),
        var caughtTotal: Int = 0,
        var photoId: String = "",
        var photoEntry: String = "",    // entry id in frame
        var wrapped: String = "",       // "" | played | partial | missed | yielded
        var yieldWhy: String = ""
    )

    private fun kRec(year: Int) = "paradeRec:$year"
    private fun kAnn(year: Int) = "paradeAnn:$year"

    fun record(state: GameState, year: Int): ParadeRecord? =
        state.lifeText[kRec(year)]?.let { parseRecord(year, it) }

    fun currentRecord(state: GameState): ParadeRecord? =
        record(state, currentYear(state))

    /** The year's record, found or opened. Writers only —
     *  every read path in this module goes through
     *  [record], so looking at the parade can never create
     *  a fact. */
    fun recordOrNew(state: GameState, year: Int): ParadeRecord =
        record(state, year) ?: ParadeRecord(year).also {
            saveRecord(state, it) }

    fun saveRecord(state: GameState, rec: ParadeRecord) {
        state.lifeText[kRec(rec.year)] = listOf(
            "1", rec.day.toString(), rec.mode, rec.spot,
            rec.marchEntry, rec.stage.toString(),
            rec.step.toString(),
            rec.catches.entries.joinToString(";") {
                "${it.key}=${it.value}" },
            rec.caughtTotal.toString(), rec.photoId,
            rec.photoEntry, rec.wrapped, rec.yieldWhy)
            .joinToString("|")
    }

    private fun parseRecord(year: Int,
                            raw: String): ParadeRecord? {
        val p = raw.split("|")
        if (p.size < 13 || p[0] != "1") return null
        val catches = (p[7].takeIf { it.isNotEmpty() } ?: "")
            .split(";").filter { it.contains("=") }
            .associate {
                it.substringBefore("=") to
                    (it.substringAfter("=").toIntOrNull() ?: 0) }
        return ParadeRecord(
            year = year,
            day = p[1].toIntOrNull() ?: -1,
            mode = p[2], spot = p[3], marchEntry = p[4],
            stage = p[5].toIntOrNull() ?: 0,
            step = p[6].toIntOrNull() ?: 0,
            catches = catches,
            caughtTotal = p[8].toIntOrNull() ?: 0,
            photoId = p[9], photoEntry = p[10],
            wrapped = p[11], yieldWhy = p[12])
    }

    /** The parade year the town is currently inside:
     *  this calendar year's parade until CURRENT_TAIL
     *  days after it, then next year's (the same tail
     *  discipline as the dance and the talent show). */
    fun currentYear(state: GameState): Int {
        val y = state.gameYear
        return if (state.absDay >
            ParadeCalendar.paradeDay(y) + CURRENT_TAIL) {
            y + 1
        } else y
    }

    // ---------------- the yields ----------------

    /** Why this year's parade does not roll, or null when
     *  it does. Consulted only ON parade day (the anchor
     *  is proven disjoint from finales and holidays a
     *  priori; the guards are the standing discipline, not
     *  a calendar confession). School.quietReason is the
     *  town's own "who owns a day" line — a case climax
     *  due, a finale window, a holiday beat — and the play
     *  guard wears the dance/talent form exactly:
     *  nightCanHappen already prices its own yield, so a
     *  bare false only counts within ±2 days of an opening
     *  (the talent build's lesson, kept). Pure: reads,
     *  never stamps. */
    fun yieldReason(state: GameState): String? {
        val year = currentYear(state)
        if (state.absDay != ParadeCalendar.paradeDay(year)) {
            return null
        }
        School.quietReason(state)?.let { return it }
        val opening = PlayCalendar.scheduleFor(
            Play.currentYear(state)).openingDay
        if (!Play.nightCanHappen(state) &&
            abs(state.absDay - opening) <= 2) return "play"
        return null
    }

    fun yieldText(why: String): String = when (why) {
        School.QUIET_CASE -> "a case climax is due in town"
        School.QUIET_FINALE -> "a season finale owns the week"
        School.QUIET_HOLIDAY -> "a holiday beat owns the day"
        "play" -> "the school play's opening owns the weekend"
        else -> "the town had other business"
    }

    /** Is today parade day, live? Learns the yield where
     *  it is consulted (the stamp lands on the record the
     *  moment any surface asks), in the talent show's
     *  discipline. */
    fun dayLive(state: GameState): Boolean {
        val year = currentYear(state)
        if (state.absDay != ParadeCalendar.paradeDay(year)) {
            return false
        }
        val why = yieldReason(state)
        if (why != null) {
            val rec = recordOrNew(state, year)
            if (rec.yieldWhy.isEmpty()) {
                rec.yieldWhy = why
                saveRecord(state, rec)
            }
            return false
        }
        return true
    }

    /** The street-closure read Walk prices by: parade day
     *  today (any year's, checked in lived time) and no
     *  yield standing. Pure — Walk consults it on every
     *  priced leg and must never stamp a record. */
    fun streetClosed(state: GameState): Boolean {
        for (y in (state.gameYear - 1)..(state.gameYear + 1)) {
            if (state.absDay == ParadeCalendar.paradeDay(y)) {
                return yieldReason(state) == null
            }
        }
        return false
    }

    fun cycleLive(state: GameState): Boolean {
        val year = currentYear(state)
        return state.absDay >= ParadeCalendar.announceDay(year) &&
            state.absDay <=
                ParadeCalendar.paradeDay(year) + CURRENT_TAIL
    }

    // ---------------- the procession ----------------

    /** One entry in the procession: derived, never
     *  invented. [crew] is who is ABOARD (or marching, for
     *  the walking entries), in claim order; [act] is the
     *  agenda activity the claim speaks in. */
    data class Entry(
        val id: String,
        val name: String,
        val icon: String,
        val theme: String,
        val crew: List<String>,
        val act: String)

    /** The household floats' lines — each family's own,
     *  off its own table's character (Dinners' dishes and
     *  rituals are the source; these are the parade's
     *  words for them). */
    private val HOUSEHOLD_THEMES: Map<String, String> =
        mapOf(
            "marsh" to "a grill on wheels and a lawn-chair " +
                "throne — Randy waves like the parade was " +
                "his idea, and Sharon keeps the float (and " +
                "Randy) between the curbs",
            "broflovski" to "a float with footnotes — " +
                "Gerald toasts the crowd at every corner on " +
                "principle, and Sheila has opinions about " +
                "the other floats, delivered kindly, at volume",
            "cartman" to "one boy, one float, ten out of " +
                "ten — self-reviewed, self-applauded, and " +
                "somehow the candy budget is half gone " +
                "before step-off",
            "mccormick" to "held together with pride and " +
                "one bungee cord — the finest float on the " +
                "route, and everybody on the curb knows it",
            "stotch" to "grounded, tidy, and exactly on " +
                "schedule — the only float that files its " +
                "route plan with itself, in triplicate",
            "black" to "the nicest float on the street " +
                "and it isn't close — polished, catered, " +
                "and gracious about the rest of the field",
            "tweek" to "coffee for the whole route, " +
                "served at parade speed — the float vibrates " +
                "slightly, constantly, and the cups never spill")

    /** The faction floats' lines — from each faction's
     *  own authored character (Factions' blurbs are the
     *  source of truth for who these people are; these are
     *  the parade's words for them). The school staff's
     *  line dresses the Elementary float, since the staff
     *  faction IS the school's entry. */
    private val FACTION_THEMES: Map<String, String> =
        mapOf(
            "school_staff" to "a book the size of a car, " +
                "ridden by the whole staff — knowledge-" +
                "heavy, slow at corners, impossible to " +
                "argue with",
            "boys_crew" to "a cardboard rocket on a wagon " +
                "— the Boys' Crew, running the town's bad " +
                "ideas at walking pace, flags at full mast",
            "goth_kids" to "a black crepe hearse-float — " +
                "the Goth Kids wave at nobody, on purpose, " +
                "and the crowd respects it completely",
            "girls_circle" to "a float with a LIST of " +
                "flowers, alphabetized — the Girls' Circle, " +
                "organized even in bloom",
            "craigs_gang" to "a float so deadpan it barely " +
                "moves — Craig's Gang, flipping off the " +
                "concept of parades, gently, as agreed",
            "parents" to "a barbecue on wheels and three " +
                "kinds of sunscreen advice — the parents of " +
                "the town, waving like they mean it",
            "church_faithful" to "a banner, a hymn at " +
                "parade volume, and one very patient " +
                "procession cross — faith, rolling",
            "tegridy_crew" to "a tractor, a wok, and " +
                "commerce, Colorado style — Tegridy & " +
                "Business, the town's most fragrant float")

    private const val CREW_THEME =
        "your crew, marching — banner up, formation " +
            "loose, morale unreasonable"
    private const val SCHOOL_WALK_THEME =
        "the school's walking entry — the free-agent " +
            "walkers, you among them"
    private const val CITYHALL_THEME =
        "the Mayor's car — McDaniels rides last, waving " +
            "like the town is hers (administratively, it " +
            "is), with the law along for the shine"

    /** Your stand float's line, derived from the stand's
     *  own books: the crate's real contents, at your real
     *  prices (Stand.priceOf — the prices you set). */
    private fun standTheme(state: GameState): String {
        val crate = Stand.crate(state)
        if (crate.isEmpty()) {
            return "your stand, on wheels — the crate's " +
                "empty today, so the banner does the work; " +
                "the pitch is " +
                (Stand.locOf(state) ?: "somewhere good") +
                "'s, and the town knows the jingle anyway"
        }
        val goods = crate.groupBy { it.itemId }.entries
            .joinToString(", ") { (itemId, batches) ->
                val item = itemById(itemId)
                val n = batches.sumOf { it.count }
                "${item?.icon ?: "🧺"} ${item?.name ?: itemId} ×$n " +
                    "at ${Stand.priceOf(state, itemId)} 🪙"
            }
        return "your stand, on wheels — the real crate, " +
            "rolled out for the town to see: $goods"
    }

    /** A faction float's riders: at most FACTION_CREW_CAP
     *  of the still-unclaimed members, the first-listed
     *  always aboard (a float carries its principals),
     *  the rest seeded by (year, faction) so the same
     *  year reads the same on every surface. Returned in
     *  the faction's own list order. Who doesn't ride is
     *  route crowd — nobody vanishes. */
    private fun factionRiders(year: Int, factionId: String,
                              avail: List<String>): List<String> {
        if (avail.size <= FACTION_CREW_CAP) return avail
        val first = avail.first()
        val rest = avail.drop(1).shuffled(
            Random(("paradeRiders:$year:$factionId")
                .hashCode().toLong()))
            .take(FACTION_CREW_CAP - 1)
        val keep = setOf(first) + rest
        return avail.filter { it in keep }
    }

    /** THE PROCESSION, derived from the town's real
     *  structures on every call. Claim order (who keeps
     *  their people): households, your stand's staff, your
     *  crew, the Mayor's car, the Bijou's stars, the
     *  factions. Procession order (who rolls when): the
     *  school, the households, the Bijou, the factions,
     *  your stand, your crew, the Mayor's car last. A name
     *  lands in at most one crew — the pool is claimed,
     *  never copied. */
    fun procession(state: GameState,
                   year: Int): List<Entry> {
        val placed = HashSet<String>()
        fun claim(names: List<String>): List<String> {
            val out = names.filter {
                it in CharacterSystem.CAST && it !in placed }
            placed += out
            return out
        }
        val byId = LinkedHashMap<String, Entry>()
        // 1 — the households claim: families ride together
        for (h in Dinners.HOUSEHOLDS) {
            val crew = claim(h.members)
            byId["hh:${h.id}"] = Entry(
                "hh:${h.id}", "${h.family} float", "🏠",
                HOUSEHOLD_THEMES[h.id]
                    ?: "the family's own float, rolled " +
                        "out with pride",
                crew, "🎺 Riding ${h.family} float")
        }
        // 2 — your stand claims its staff of the day
        if (Stand.hasStand(state)) {
            val staff = Stand.assignedStaff(state)
            val crew = if (staff != null && staff != "you") {
                claim(listOf(staff))
            } else emptyList()
            byId["stand"] = Entry(
                "stand", "your stand, on wheels", "💰",
                standTheme(state), crew,
                "🎺 Riding the stand float")
        }
        // 3 — your crew claims its unclaimed members
        val members = Crew.members(state)
        if (members.isNotEmpty()) {
            val crew = claim(members)
            byId["crew"] = Entry(
                "crew", "your crew, marching", "🧢",
                CREW_THEME, crew, "🎺 Marching in the parade")
        }
        // 4 — the Mayor's car claims the mayor (before any
        // faction float can carry her off), with the law
        val hallCrew = claim(listOf("McDaniels", "Barbrady",
            "Yates"))
        byId["cityhall"] = Entry(
            "cityhall", "the Mayor's car", "🏛",
            CITYHALL_THEME, hallCrew,
            "🎺 Riding in the Mayor's car")
        // 5 — the Bijou claims its feature's stars
        val feat = MovieNights.featureForDay(state,
            ParadeCalendar.paradeDay(year))
        val bijouCrew = claim(feat.stars)
        byId["bijou"] = Entry(
            "bijou", "the Bijou's float", "🎬",
            "a silver screen on wheels, the marquee lit " +
                "at noon for “${feat.title}” — the " +
                "picture's own people aboard",
            bijouCrew, "🎺 Riding the Bijou's float")
        // 6 — the factions claim their riders; the school
        // staff's float IS the Elementary's
        for (f in FACTIONS) {
            val avail = f.members.filter {
                it in CharacterSystem.CAST && it !in placed }
            val riders = factionRiders(year, f.id, avail)
            placed += riders
            if (f.id == "school_staff") {
                byId["school"] = Entry(
                    "school", "South Park Elementary", "🏫",
                    FACTION_THEMES[f.id]
                        ?: "the school's own float",
                    riders, "🎺 Riding the school float")
            } else {
                byId["fac:${f.id}"] = Entry(
                    "fac:${f.id}", "${f.name} float",
                    f.icon,
                    FACTION_THEMES[f.id]
                        ?: "the faction's own float, in " +
                            "its own character",
                    riders, "🎺 Riding the ${f.name} float")
            }
        }
        // PROCESSION order: the school leads …
        val out = mutableListOf<Entry>()
        byId["school"]?.let { out += it }
        for (h in Dinners.HOUSEHOLDS) {
            byId["hh:${h.id}"]?.let { out += it }
        }
        byId["bijou"]?.let { out += it }
        for (f in FACTIONS) {
            if (f.id == "school_staff") continue
            byId["fac:${f.id}"]?.let { out += it }
        }
        byId["stand"]?.let { out += it }
        byId["crew"]?.let { out += it }
        // … and the mayor rides last
        byId["cityhall"]?.let { out += it }
        return out
    }

    /** Every name the procession carries this year. */
    fun placedNames(state: GameState, year: Int): Set<String> =
        procession(state, year)
            .flatMap { it.crew }.toSet()

    /** The route crowd: every cast member the procession
     *  did NOT take — the watchers. Derived, whole, and
     *  nobody's second place. */
    fun routeCrowd(state: GameState, year: Int): List<String> {
        val placed = placedNames(state, year)
        return CharacterSystem.CAST.keys
            .filter { it !in placed }.sorted()
    }

    /** Which curb spot a watcher stands at, seeded by
     *  (year, name) — the same year reads the same on
     *  every surface: the agenda claim, the reactions and
     *  the phone all agree about who stood near you. */
    fun spotOf(year: Int, name: String): String {
        val h = ("paradeSpot:$year:$name").hashCode()
        return SPOTS[(h and Int.MAX_VALUE) % SPOTS.size].id
    }

    fun crowdAt(state: GameState, year: Int,
                spotId: String): List<String> =
        routeCrowd(state, year)
            .filter { spotOf(year, it) == spotId }

    /** The procession in pass-beats: consecutive chunks
     *  of CHUNK entries, in procession order. */
    fun chunks(state: GameState, year: Int): List<List<Entry>> =
        procession(state, year).chunked(CHUNK)

    /** The entry you march with: your crew's entry when
     *  you HAVE a crew (the entry stands even if every
     *  member was claimed by a family float — you carry
     *  the banner; they wave), else the school's entry as
     *  a stated free-agent walker. */
    fun marchEntryFor(state: GameState): String =
        if (Crew.members(state).isNotEmpty()) "crew"
        else "school"

    fun entryOf(state: GameState, year: Int,
                entryId: String): Entry? =
        procession(state, year).firstOrNull { it.id == entryId }

    // ---------------- the claim ----------------

    /** The parade's agenda claim — seat EIGHTEEN, after
     *  the talent show, before sleepovers. During the
     *  window on a live parade day it holds every rider
     *  (in their entry's own words) and every watcher at
     *  Main Street; everybody else keeps their day. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in PARADE_FROM..PARADE_TO) return null
        if (!dayLive(state)) return null
        val year = currentYear(state)
        for (e in procession(state, year)) {
            if (npcName in e.crew) {
                return AgendaBlock(STREET, e.act)
            }
        }
        if (npcName in CharacterSystem.CAST) {
            return AgendaBlock(STREET, "🎺 Watching the parade")
        }
        return null
    }

    // ---------------- presence & the session ----------------

    /** Who is actually standing on Main Street right now
     *  (the MovieNights.presentCast discipline, moved to
     *  the parade's street): the photo's honesty source. */
    fun presentHere(state: GameState): List<String> {
        val me = state.agents.values.firstOrNull {
            it.isPlayer } ?: return emptyList()
        return state.agents.values
            .filter { !it.isPlayer && it.location == STREET &&
                it.location == me.location &&
                it.waypoints.isEmpty() }
            .map { it.name }
    }

    /** The beats are HERE when it is parade day, live,
     *  you are standing on Main Street, and the year is
     *  not wrapped — or when a started day is still
     *  finishing (you took your place; the parade does
     *  not abandon you at 13:30). */
    fun beatsHere(state: GameState): Boolean {
        val me = state.agents.values.firstOrNull {
            it.isPlayer } ?: return false
        if (me.location != STREET) return false
        val rec = currentRecord(state)
        if (rec != null && rec.wrapped.isNotEmpty()) {
            return false
        }
        if (rec != null &&
            (rec.mode.isNotEmpty() || rec.stage > 0)) {
            // a started day finishes on its own day, even
            // past the window's end
            return state.absDay ==
                ParadeCalendar.paradeDay(rec.year)
        }
        if (!dayLive(state)) return false
        return state.timeMin in PARADE_FROM..PARADE_TO
    }

    fun sessionOpen(state: GameState): Boolean = beatsHere(state)

    /** The stand on Main Street is the parade's door: it
     *  stands through the whole cycle (poster week to the
     *  parade's tail) whenever there is something to say. */
    fun standOpen(state: GameState): Boolean {
        if (!cycleLive(state)) return false
        val year = currentYear(state)
        val rec = record(state, year)
        if (rec != null && rec.yieldWhy.isNotEmpty() &&
            rec.wrapped.isEmpty()) return true
        if (dayLive(state)) return true
        if (rec != null && rec.wrapped.isNotEmpty()) return true
        return state.absDay <= ParadeCalendar.paradeDay(year)
    }

    data class StandStatus(val kind: String, val text: String)

    fun standStatus(state: GameState): StandStatus {
        val year = currentYear(state)
        val day = ParadeCalendar.paradeDay(year)
        val rec = record(state, year)
        val label = Dance.dateLabel(day)
        if (rec != null && rec.yieldWhy.isNotEmpty()) {
            return StandStatus("yielded",
                "🎺 The parade stand, its banner furled.\n" +
                    "This year's parade was not held — " +
                    yieldText(rec.yieldWhy) + ".")
        }
        if (rec != null && rec.wrapped.isNotEmpty()) {
            return StandStatus("done",
                "🎺 The reviewing stand, the morning " +
                    "after.\n" + wrapLine(state, rec))
        }
        if (state.absDay == day) {
            return StandStatus("live",
                "🎺 THE FOUNDERS' DAY PARADE — TODAY.\n" +
                    "Step-off was 10 AM; the street is the " +
                    "town's until 13:30. " +
                    posterText(state))
        }
        if (state.absDay < day) {
            val left = day - state.absDay
            return StandStatus("poster",
                "🎺 A parade poster, fresh on the stand: " +
                    "THE FOUNDERS' DAY PARADE — $label, " +
                    "step-off 10 AM down Main Street" +
                    (if (left == 1) " — TOMORROW."
                     else " — in $left days.") + "\n" +
                    posterText(state))
        }
        return StandStatus("quiet",
            "🎺 The reviewing stand stands empty — the " +
                "parade rolls every third Saturday of May.")
    }

    /** The procession as posted: the derived order, head
     *  to tail, with who rides what — the same derivation
     *  the day itself will roll. */
    fun posterText(state: GameState): String {
        val year = currentYear(state)
        val proc = procession(state, year)
        val sb = StringBuilder()
        sb.append("This year's order, as derived from " +
            "the town itself (${proc.size} entries):\n")
        proc.forEachIndexed { i, e ->
            sb.append("${i + 1}. ${e.icon} ${e.name}")
            if (e.crew.isNotEmpty()) {
                sb.append(" — aboard: " +
                    e.crew.joinToString(", "))
            }
            sb.append("\n")
        }
        sb.append("The Mayor's car rides last. Candy " +
            "flies from the Cartman float, the Bijou's " +
            "float and the Mayor's car — pockets hold " +
            "$CANDY_CAP.")
        return sb.toString().trimEnd()
    }

    // ---------------- the beats ----------------

    /** The next beat the stand offers, or null when the
     *  day is done (or not yours to start). The chain:
     *  choose → lineup → pass (with a catch owed after any
     *  chunk carrying a thrower, watch only) → photo
     *  (watch) → the Mayor's car. */
    fun nextBeatKind(state: GameState): String? {
        if (!beatsHere(state)) return null
        val rec = currentRecord(state)
            ?: return "choose"
        if (rec.mode.isEmpty()) return "choose"
        if (rec.stage == 0) return "lineup"
        val ch = chunks(state, rec.year)
        if (rec.mode == "watch" && rec.step in 1..ch.size) {
            val owed = ch[rec.step - 1].firstOrNull {
                it.id in THROWERS && it.id !in rec.catches }
            if (owed != null) return "catch"
        }
        if (rec.step < ch.size) return "pass"
        if (rec.mode == "watch" && rec.photoId.isEmpty() &&
            photoOptions(state).isNotEmpty()) return "photo"
        return "mayor"
    }

    fun beatLabels(state: GameState): List<String> {
        if (!beatsHere(state)) return emptyList()
        return when (nextBeatKind(state)) {
            "choose" -> listOf(
                "🎺 March in the parade",
                "👀 Watch from a curb spot")
            "lineup" -> listOf("🎺 The lineup — take your place")
            "pass" -> listOf("🎺 Here they come…")
            "catch" -> listOf("🍬 Candy incoming — catch!")
            "photo" -> listOf("📸 Take the parade photo")
            "mayor" -> listOf("🏁 The Mayor's car — the close")
            else -> emptyList()
        }
    }

    /** The thrower whose catch is currently owed, if any. */
    fun catchOwed(state: GameState): String? {
        val rec = currentRecord(state) ?: return null
        if (rec.mode != "watch") return null
        val ch = chunks(state, rec.year)
        if (rec.step !in 1..ch.size) return null
        return ch[rec.step - 1].firstOrNull {
            it.id in THROWERS && it.id !in rec.catches }?.id
    }

    fun catchSeed(state: GameState, throwerId: String): Long =
        ("paradeCatch:${state.absDay}:$throwerId")
            .hashCode().toLong()

    /** BEAT 0 — TAKE YOUR PLACE. Marching stamps your
     *  entry (derived, at the choosing); watching stamps
     *  your spot. Either way the choice is the day's: one
     *  parade, one part in it. */
    fun playChoose(state: GameState, mode: String,
                   spotId: String): String? {
        if (!beatsHere(state)) return null
        val year = currentYear(state)
        val existing = currentRecord(state)
        if (existing != null && existing.mode.isNotEmpty()) {
            return null
        }
        val rec = recordOrNew(state, year)
        rec.day = ParadeCalendar.paradeDay(year)
        return when (mode) {
            "march" -> {
                val entryId = marchEntryFor(state)
                rec.mode = "march"
                rec.marchEntry = entryId
                saveRecord(state, rec)
                val entry = entryOf(state, year, entryId)
                if (entryId == "crew") {
                    val riding = entry?.crew ?: emptyList()
                    val waved = Crew.members(state)
                        .filter { it !in riding }
                    "🎺 You take your place with your crew" +
                        (if (riding.isNotEmpty())
                            " — ${riding.joinToString(", ")} " +
                                "fall in beside you"
                         else " — the banner's yours to carry") +
                        (if (waved.isNotEmpty())
                            ". ${waved.joinToString(", ")} " +
                                "ride their families' floats " +
                                "this year and wave you past"
                         else "") +
                        ". Marching costs the afternoon " +
                        "and pays it back in the wrap: your " +
                        "walkers, +$MARCH_PAY each."
                } else {
                    "🎺 No crew of your own to march with — " +
                        "so you march as a free-agent walker " +
                        "with the school entry, " +
                        SCHOOL_WALK_THEME + ". The town " +
                        "counts showing up."
                }
            }
            "watch" -> {
                val sp = spot(spotId) ?: return null
                rec.mode = "watch"
                rec.spot = spotId
                saveRecord(state, rec)
                val near = crowdAt(state, year, spotId)
                "🎺 You take your spot: ${sp.label} — " +
                    sp.line + "." +
                    (if (near.isNotEmpty())
                        " Standing near you: " +
                            near.joinToString(", ") + "."
                     else " You have this stretch of curb " +
                        "to yourself.") +
                    " The floats will pass YOUR spot in " +
                    "order — and three of them throw candy."
            }
            else -> null
        }
    }

    /** BEAT 1 — THE LINEUP: the staging read-out, the full
     *  derived order spoken once, and your place in it. */
    fun playLineup(state: GameState): String? {
        if (!beatsHere(state)) return null
        val rec = currentRecord(state) ?: return null
        if (rec.mode.isEmpty() || rec.stage != 0) return null
        rec.stage = 1
        rec.day = ParadeCalendar.paradeDay(rec.year)
        saveRecord(state, rec)
        Engine().advanceTime(state)
        val proc = procession(state, rec.year)
        val order = proc.mapIndexed { i, e ->
            "${i + 1}. ${e.icon} ${e.name}" }.joinToString("\n")
        val yours = when (rec.mode) {
            "march" -> "You march at place " +
                (proc.indexOfFirst {
                    it.id == rec.marchEntry } + 1) +
                " of ${proc.size}."
            else -> "You watch from " +
                (spot(rec.spot)?.label ?: "your curb spot") +
                " — all ${proc.size} of them pass you."
        }
        return "🎺 THE LINEUP — the marshall reads the " +
            "order off the poster, and the town falls in:\n" +
            order + "\n" + yours + "\nThe Mayor's car " +
            "rides last. It always rides last."
    }

    // ---------------- the pass ----------------

    /** Curb reactions — the route crowd's voice, generic
     *  enough to be true of whoever says it, specific
     *  enough to be a parade. Seeded per (year, step,
     *  speaker) so every surface quotes the same remark. */
    private val REACTIONS: List<String> = listOf(
        "waves with both hands, like the float might " +
            "wave back",
        "is filming the whole thing, narrating for " +
            "posterity",
        "applauds in the wrong places and the right " +
            "spirit",
        "shouts the float's name like it's a sports team",
        "has been saving that wave since April",
        "points out a detail nobody else caught, correctly",
        "is holding a sign that just says YES with no " +
            "further context",
        "cries a little, denies it instantly, waves harder")

    private fun reactionFor(year: Int, step: Int,
                            name: String): String =
        REACTIONS[Random(("paradeReact:$year:$step:$name")
            .hashCode().toLong()).nextInt(REACTIONS.size)]

    /** BEAT — THE PASS: the next chunk of the procession
     *  goes by. Watching, it passes YOUR spot and somebody
     *  actually standing near you reacts; marching, you
     *  pass the next curb spot and its crowd reacts to
     *  YOUR stretch of the route. One chunk, one tick. */
    fun playPass(state: GameState): String? {
        if (!beatsHere(state)) return null
        val rec = currentRecord(state) ?: return null
        if (rec.mode.isEmpty() || rec.stage == 0) return null
        val ch = chunks(state, rec.year)
        if (rec.step >= ch.size) return null
        // a catch owed from the last chunk comes first
        if (catchOwed(state) != null) return null
        val chunk = ch[rec.step]
        val sb = StringBuilder()
        if (rec.mode == "march") {
            val sp = SPOTS[rec.step % SPOTS.size]
            sb.append("🎺 Your stretch of the route passes " +
                "${sp.label} —\n")
            for (e in chunk) {
                sb.append("${e.icon} ${e.name}: ${e.theme}")
                if (e.id == rec.marchEntry) {
                    sb.append(" — THAT'S YOU, mid-route, " +
                        "holding the pace")
                }
                sb.append("\n")
            }
            val watchers = crowdAt(state, rec.year, sp.id)
            if (watchers.isNotEmpty()) {
                val who = watchers[Random(
                    ("paradeMarchReact:${rec.year}:" +
                        "${rec.step}").hashCode().toLong())
                    .nextInt(watchers.size)]
                sb.append("$who, on the curb, " +
                    reactionFor(rec.year, rec.step, who) + ".")
            }
        } else {
            sb.append("🎺 Down the street they come —\n")
            for (e in chunk) {
                sb.append("${e.icon} ${e.name}: ${e.theme}")
                if (e.crew.isNotEmpty()) {
                    sb.append(" Aboard: " +
                        e.crew.joinToString(", ") + ".")
                }
                if (e.id in THROWERS) {
                    sb.append(" — and they're THROWING " +
                        "something. Hands up.")
                }
                sb.append("\n")
            }
            val near = crowdAt(state, rec.year, rec.spot)
            if (near.isNotEmpty()) {
                val who = near[Random(
                    ("paradeWatchReact:${rec.year}:" +
                        "${rec.step}").hashCode().toLong())
                    .nextInt(near.size)]
                sb.append("$who, beside you, " +
                    reactionFor(rec.year, rec.step, who) + ".")
            }
        }
        rec.step += 1
        saveRecord(state, rec)
        Engine().advanceTime(state)
        return sb.toString().trimEnd()
    }

    // ---------------- the candy ----------------

    /** One thrower's catch session: three throws on the
     *  showdown bar — the float hand's arc against your
     *  honest catching nerve. A release holds when its
     *  steadied power clears Feel's good line. The math is
     *  the dance's, re-aimed at incoming snacks. */
    class CatchSession(val throwerId: String, seed: Long) {
        private val rnd = Random(seed)
        var throws = 0; private set
        var caught = 0; private set
        val over: Boolean get() = throws >= THROWS_PER

        fun toss(release01: Double): Boolean {
            if (over) return false
            throws++
            val zone = Showdown.zoneOf(release01.toFloat())
            val jitter = (rnd.nextDouble() * 0.06 - 0.03)
                .coerceIn(-0.05, 0.05)
            val steady = Showdown.zonePower(zone) *
                (0.72 + 0.05 * CATCH_NERVE) + jitter
            val got = steady >= Feel.POWER_GOOD
            if (got) caught++
            return got
        }
    }

    /** The session's judging, once — when the view closes
     *  it. Caught candy lands in your real inventory,
     *  bounded by the day's cap: what your pockets can't
     *  hold goes to the kid next to you (stated, not
     *  vanished). The settlement is recorded against the
     *  thrower either way, so no session can be replayed
     *  for a second haul. */
    fun judgeCatch(state: GameState,
                   session: CatchSession): List<String>? {
        if (!beatsHere(state)) return null
        val rec = currentRecord(state) ?: return null
        if (rec.mode != "watch") return null
        val thrower = session.throwerId
        if (thrower in rec.catches) return null
        val itemId = THROWERS[thrower] ?: return null
        val entry = entryOf(state, rec.year, thrower)
        val item = itemById(itemId)
        val room = (CANDY_CAP - rec.caughtTotal)
            .coerceAtLeast(0)
        val gained = minOf(session.caught, room)
        val overflow = session.caught - gained
        if (gained > 0) {
            state.inventory[itemId] =
                (state.inventory[itemId] ?: 0) + gained
        }
        rec.catches = rec.catches + (thrower to gained)
        rec.caughtTotal += gained
        saveRecord(state, rec)
        val out = mutableListOf<String>()
        out += "🍬 ${entry?.name ?: "The float"} threw " +
            "$THROWS_PER — you caught ${session.caught}."
        if (gained > 0) {
            out += "${item?.icon ?: "🍬"} $gained × " +
                "${item?.name ?: itemId} into your pockets " +
                "(${rec.caughtTotal} of $CANDY_CAP for " +
                "the day)."
        }
        if (overflow > 0) {
            out += "Your pockets are full — $overflow " +
                "more bounce to the kid next to you, who " +
                "owes you nothing and knows it."
        }
        if (session.caught == 0) {
            out += "The curb provides. The floats provide " +
                "again next year."
        }
        return out
    }

    // ---------------- the photo ----------------

    /** The floats you can still put in a frame: entries
     *  with somebody aboard actually standing on Main
     *  Street right now. */
    fun photoOptions(state: GameState): List<Entry> {
        val rec = currentRecord(state) ?: return emptyList()
        val present = presentHere(state).toSet()
        return procession(state, rec.year).filter { e ->
            e.crew.any { it in present } }
    }

    /** THE PHOTO — exactly one, minted through
     *  Photos.composeGroup at your spot (watching: the
     *  float of your choice in frame; the frame holds the
     *  float's crew as they stand on the street, capped at
     *  PHOTO_CREW_CAP figures). The shutter is the day's
     *  only mint; the record keeps its id. */
    fun takePhoto(state: GameState,
                  entryId: String): String? {
        if (!beatsHere(state)) return null
        val rec = currentRecord(state) ?: return null
        if (rec.mode != "watch" || rec.photoId.isNotEmpty()) {
            return null
        }
        val entry = entryOf(state, rec.year, entryId)
            ?: return null
        val present = presentHere(state).toSet()
        val cast = entry.crew.filter { it in present }
            .take(PHOTO_CREW_CAP)
        if (cast.isEmpty()) return null
        val me = state.agents.values.first { it.isPlayer }
        val label = Dance.dateLabel(
            ParadeCalendar.paradeDay(rec.year))
        val draft = Photos.composeGroup(state, cast,
            emptyList(), me.name, me.appearance)
        val photo = draft.copy(caption = "🎺 $NAME, " +
            "$label — ${entry.name}, from " +
            (spot(rec.spot)?.label ?: "the curb") + ".")
        Photos.applyPhoto(state, photo)
        rec.photoId = photo.id
        rec.photoEntry = entryId
        saveRecord(state, rec)
        return "📸 One frame, kept: ${entry.name} rolls " +
            "past ${spot(rec.spot)?.label ?: "your spot"} " +
            "and the shutter minds its manners — " +
            "${cast.joinToString(", ")} in frame, you " +
            "behind the camera. The day's one photograph."
    }

    /** The marching shutter: your entry, gathered at the
     *  City Hall steps at the route's end — your crew
     *  whole (wherever they rode, they end the route on
     *  this street), or your entry's riders for the school
     *  walk. Called by the closing beat only. */
    private fun mintMarchPhoto(state: GameState,
                               rec: ParadeRecord) {
        val present = presentHere(state).toSet()
        val cast = if (rec.marchEntry == "crew") {
            Crew.members(state).filter { it in present }
        } else {
            entryOf(state, rec.year, rec.marchEntry)?.crew
                ?.filter { it in present } ?: emptyList()
        }.take(PHOTO_CREW_CAP)
        if (cast.isEmpty()) return
        val me = state.agents.values.first { it.isPlayer }
        val label = Dance.dateLabel(
            ParadeCalendar.paradeDay(rec.year))
        val who = if (rec.marchEntry == "crew") "your crew"
            else "the school's walkers"
        val draft = Photos.composeGroup(state, cast,
            emptyList(), me.name, me.appearance)
        val photo = draft.copy(caption = "🎺 $NAME, " +
            "$label — $who, passing the City Hall steps.")
        Photos.applyPhoto(state, photo)
        rec.photoId = photo.id
        rec.photoEntry = rec.marchEntry
    }

    // ---------------- the close ----------------

    /** The mayor's closing lines — authored, town-scale,
     *  in the finale-beat pattern: a closing card at the
     *  route's end, spoken by the mayor from her car. No
     *  rail, no gate: the day ends the way finales end,
     *  with a person summing up the town. */
    private fun mayorLine(state: GameState,
                          rec: ParadeRecord): String {
        val proc = procession(state, rec.year)
        val head = proc.firstOrNull()?.name ?: "the school"
        return when (rec.mode) {
            "march" -> "\"Another year, another street " +
                "full of us. $head led, everybody rolled, " +
                "nobody got run over by a float — that's " +
                "the whole standard, and we met it. You " +
                "marched, new kid. The town saw. I saw. " +
                "Same time next year — bring that banner " +
                "back.\" — Mayor McDaniels, from the car, " +
                "to the whole street"
            "watch" -> "\"Another year, another street " +
                "full of us. $head led, everybody rolled, " +
                "and the curbs held their own — a parade is " +
                "the watched as much as the watchers. Same " +
                "time next year.\" — Mayor McDaniels, from " +
                "the car, to the whole street"
            else -> "\"Another year, another street full " +
                "of us.\" — Mayor McDaniels, from the car"
        }
    }

    /** THE CLOSING BEAT — the Mayor's car stops at the
     *  City Hall steps: her line, the marching photo at
     *  the reviewing stand, the marching pays, and the
     *  wrap. Played once; the day is the record's after. */
    fun playMayor(state: GameState): String? {
        if (!beatsHere(state)) return null
        val rec = currentRecord(state) ?: return null
        if (rec.mode.isEmpty() || rec.stage == 0) return null
        if (rec.step < chunks(state, rec.year).size) {
            return null
        }
        if (catchOwed(state) != null) return null
        val eng = Engine()
        val sb = StringBuilder()
        sb.append("🏁 The route ends at the City Hall " +
            "steps. The Mayor's car rolls the last yards " +
            "alone, and McDaniels stands for the close.\n")
        sb.append(mayorLine(state, rec))
        if (rec.mode == "march") {
            if (rec.photoId.isEmpty()) {
                mintMarchPhoto(state, rec)
            }
            val entry = entryOf(state, rec.year,
                rec.marchEntry)
            if (rec.marchEntry == "crew" &&
                (entry?.crew?.isNotEmpty() == true)) {
                for (name in entry.crew) {
                    eng.relAdjust(state, name, MARCH_PAY)
                }
                sb.append("\n🎺 The wrap pays its walkers: " +
                    entry.crew.joinToString(", ") +
                    " +$MARCH_PAY each — marching is " +
                    "friendship at walking pace.")
            } else if (rec.marchEntry == "school") {
                eng.relAdjust(state, "Victoria", MARCH_PAY)
                sb.append("\n🎺 Principal Victoria walks " +
                    "the last stretch with the free-agent " +
                    "walkers — showing up is noted, " +
                    "+$MARCH_PAY.")
            }
            if (rec.photoId.isNotEmpty()) {
                sb.append("\n📸 The shutter catches your " +
                    "entry at the City Hall steps — the " +
                    "day's one photograph, minted.")
            }
        } else {
            sb.append("\n🍬 Your pockets hold " +
                "${rec.caughtTotal} of the day's candy " +
                "(cap $CANDY_CAP — the parade feeds the " +
                "curb, not the pantry).")
            if (rec.photoId.isNotEmpty()) {
                sb.append("\n📸 And your photograph is " +
                    "in the album.")
            }
        }
        rec.wrapped = "played"
        rec.day = ParadeCalendar.paradeDay(rec.year)
        saveRecord(state, rec)
        Engine().advanceTime(state)
        return sb.toString()
    }

    /** One factual line about a wrapped year — the paper,
     *  the stand and the Journal all quote the same
     *  sentence because they all read the same record. */
    fun wrapLine(state: GameState, rec: ParadeRecord): String {
        val proc = procession(state, rec.year)
        val head = proc.firstOrNull()?.name ?: "the school"
        val label = Dance.dateLabel(
            ParadeCalendar.paradeDay(rec.year))
        if (rec.wrapped == "yielded") {
            return "🎺 $label — the parade was not held: " +
                yieldText(rec.yieldWhy) + "."
        }
        val base = "🎺 $label — $head led the way and " +
            "the Mayor's car brought up the rear"
        return when {
            rec.wrapped == "played" && rec.mode == "march" ->
                base + "; you marched with " +
                    (entryOf(state, rec.year, rec.marchEntry)
                        ?.name ?: "your entry") +
                    if (rec.photoId.isNotEmpty()) {
                        ", and the shutter caught your " +
                            "entry at City Hall."
                    } else "."
            rec.wrapped == "played" && rec.mode == "watch" ->
                base + "; you watched from " +
                    (spot(rec.spot)?.label ?: "the curb") +
                    ", caught ${rec.caughtTotal} of the " +
                        "thrown candy" +
                    if (rec.photoId.isNotEmpty()) {
                        ", and photographed " +
                            (entryOf(state, rec.year,
                                rec.photoEntry)?.name
                                ?: "a float") + "."
                    } else "."
            rec.wrapped == "partial" ->
                base + "; you were there for part of it."
            else -> base + "; you missed it."
        }
    }

    // ---------------- the rollover ----------------

    private fun note(state: GameState, line: String) {
        state.events.add(0, line)
        while (state.events.size > 40) {
            state.events.removeAt(state.events.size - 1)
        }
    }

    private fun townNote(state: GameState, line: String) {
        if (line in state.townLog) return
        state.townLog.add(line)
        while (state.townLog.size > 30) {
            state.townLog.removeAt(0)
        }
    }

    /** The parade's mornings: the poster's ticker a week
     *  out, and the morning-after wrap — the record opens
     *  here even for a year never touched, settled from
     *  the facts (a started day wraps partial, an
     *  untouched one missed, a yielded one yielded with
     *  its reason learned from whatever still stands in
     *  the morning: a climax due yesterday is due until
     *  it's played). Runs in Engine.onNewDay beside the
     *  talent show's mornings, before the paper and the
     *  phone — so the edition line and the morning-after
     *  text derive from the wrapped record. */
    fun dailyRollover(state: GameState) {
        for (y in (state.gameYear - 1)..(state.gameYear + 1)) {
            val day = ParadeCalendar.paradeDay(y)
            if (state.absDay == ParadeCalendar.announceDay(y) &&
                state.life[kAnn(y)] == null) {
                state.life[kAnn(y)] = 1
                note(state, "🎺 POSTER UP on Main Street: " +
                    "$NAME rolls " +
                    Dance.dateLabel(day) +
                    " — step-off 10 AM. March with your " +
                    "crew (or walk with the school), or " +
                    "pick a curb spot: the bank steps, the " +
                    "Bijou marquee, the post office corner, " +
                    "the City Hall steps. The mayor rides " +
                    "last.")
            }
            if (state.absDay == day + 1) {
                val rec = recordOrNew(state, y)
                if (rec.wrapped.isNotEmpty()) continue
                if (rec.yieldWhy.isEmpty()) {
                    School.quietReason(state)?.let {
                        rec.yieldWhy = it }
                }
                rec.day = day
                when {
                    rec.yieldWhy.isNotEmpty() -> {
                        rec.wrapped = "yielded"
                        saveRecord(state, rec)
                        townNote(state, "🎺 The Founders' " +
                            "Day Parade was not held this " +
                            "year — " +
                            yieldText(rec.yieldWhy) + ".")
                    }
                    rec.mode.isNotEmpty() -> {
                        rec.wrapped = "partial"
                        saveRecord(state, rec)
                        townNote(state, wrapLine(state, rec))
                    }
                    else -> {
                        rec.wrapped = "missed"
                        saveRecord(state, rec)
                        townNote(state, wrapLine(state, rec))
                    }
                }
            }
        }
    }

    // ---------------- the surfaces ----------------

    /** The Journal's 🎺 page: this year's derived order,
     *  your part in it as played, and past years at a line
     *  each (newest three). Reads never create a record. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val year = currentYear(state)
        val day = ParadeCalendar.paradeDay(year)
        val rec = record(state, year)
        out += "The Founders' Day Parade — the third " +
            "Saturday of May, every year. The whole town " +
            "rolls down Main Street in an order derived " +
            "from the town itself; the mayor rides last."
        when {
            rec != null && rec.yieldWhy.isNotEmpty() &&
                rec.wrapped.isEmpty() -> {
                out += "📅 $year · ${Dance.dateLabel(day)}" +
                    " — will not be held"
                out += "The parade yields this year: " +
                    yieldText(rec.yieldWhy) + ". The " +
                    "stand stays furled; the order below " +
                    "is the one that would have rolled."
            }
            rec != null && rec.wrapped.isNotEmpty() -> {
                out += "📅 $year · " + wrapLine(state, rec)
                out += "The order that rolled:"
            }
            state.absDay == day -> {
                out += "📅 $year · ${Dance.dateLabel(day)}" +
                    " — TODAY, 10:00–13:30 on Main Street"
                out += "The order, as it rolls:"
            }
            state.absDay < day -> {
                out += "📅 $year · ${Dance.dateLabel(day)}" +
                    " — this year's order, as posted:"
            }
            else -> {
                out += "📅 $year · ${Dance.dateLabel(day)}"
                out += "The order, as posted:"
            }
        }
        val proc = procession(state, year)
        proc.forEachIndexed { i, e ->
            out += "${i + 1}. ${e.icon} ${e.name}" +
                (if (e.crew.isNotEmpty()) " — " +
                    e.crew.joinToString(", ") else "")
        }
        if (rec != null && rec.mode.isNotEmpty()) {
            out += when (rec.mode) {
                "march" -> "Your part: you marched with " +
                    (entryOf(state, year, rec.marchEntry)
                        ?.name ?: "your entry") + "."
                else -> "Your part: you watched from " +
                    (spot(rec.spot)?.label ?: "the curb") +
                    " — caught ${rec.caughtTotal} of the " +
                    "day's candy (cap $CANDY_CAP)."
            }
            if (rec.photoId.isNotEmpty()) {
                out += "📷 Your parade photograph is in " +
                    "the album."
            }
        }
        val past = (year - 1 downTo year - 8).mapNotNull {
            record(state, it) }
            .filter { it.wrapped.isNotEmpty() }.take(3)
        for (p in past) {
            out += wrapLine(state, p)
        }
        return out.take(40)
    }

    /** The paper's TOWN NEWS line for the parade's week —
     *  one line, the order's head and tail named, your
     *  entry if you marched. The headline ladder is not
     *  this module's business. */
    fun paperLine(state: GameState, week: Int): String? {
        for (y in (state.gameYear - 1)..(state.gameYear + 1)) {
            val rec = record(state, y) ?: continue
            if (rec.wrapped.isEmpty()) continue
            val day = ParadeCalendar.paradeDay(y)
            if (Jobs.weekIndex(day) != week) continue
            return wrapLine(state, rec)
        }
        return null
    }

    /** The morning-after phone beat: ONE text, fact-only,
     *  from a closed sender set — a crewmate who marched
     *  with you, a curb neighbor you stood with, or (a
     *  year you missed) a friend who was there. The
     *  sender set is the parade's own derived people, so
     *  the harvest's sender (first of the triple) is
     *  always somebody the day actually involved. */
    fun phoneBeat(state: GameState): Triple<String, String,
        String>? {
        val y = state.gameYear
        for (yy in (y - 1)..(y + 1)) {
            val rec = record(state, yy) ?: continue
            if (state.absDay !=
                ParadeCalendar.paradeDay(yy) + 1) continue
            if (rec.wrapped.isEmpty() ||
                rec.wrapped == "yielded") return null
            val proc = procession(state, yy)
            val head = proc.firstOrNull()?.name
                ?: "the school"
            when (rec.mode) {
                "march" -> {
                    val entry = proc.firstOrNull {
                        it.id == rec.marchEntry }
                    val sender = entry?.crew?.maxByOrNull {
                        state.friendship[it] ?: 0 }
                        ?: Crew.members(state).maxByOrNull {
                            state.friendship[it] ?: 0 }
                        ?: "Victoria"
                    return Triple(sender, "parade",
                        "Parade debrief: we marched, " +
                            "$head led, the mayor closed " +
                            "it from the car. My feet have " +
                            "filed a complaint. Worth it.")
                }
                "watch" -> {
                    val near = crowdAt(state, yy, rec.spot)
                    val sender = near.maxByOrNull {
                        state.friendship[it] ?: 0 }
                        ?.takeIf {
                            (state.friendship[it] ?: 0) >= 30 }
                        ?: return null
                    return Triple(sender, "parade",
                        "Still thinking about the parade — " +
                            "$head led it out and the " +
                            "Mayor's car closed it. You " +
                            "caught ${rec.caughtTotal} " +
                            "candies from our spot. Good " +
                            "curb. Good year.")
                }
                else -> {
                    val sender = CharacterSystem.CAST.keys
                        .filter {
                            (state.friendship[it] ?: 0) >= 30 }
                        .maxByOrNull {
                            state.friendship[it] ?: 0 }
                        ?: return null
                    return Triple(sender, "parade",
                        "You missed the parade! $head led, " +
                            "the whole town rolled, the " +
                            "mayor closed it. Third Saturday " +
                            "of May — it's not a secret, it's " +
                            "a parade.")
                }
            }
        }
        return null
    }

    /** The Look at Main Street across the cycle: the
     *  poster on the stand, the dressed street on the
     *  day, the echo after. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != STREET) return null
        val year = currentYear(state)
        val day = ParadeCalendar.paradeDay(year)
        val rec = record(state, year)
        if (rec != null && rec.yieldWhy.isNotEmpty() &&
            state.absDay in
                (day - ANN_BACK)..(day + CURRENT_TAIL)) {
            return "🎺 The reviewing stand stands furled " +
                "this year — the parade yielded: " +
                yieldText(rec.yieldWhy) + "."
        }
        return when {
            state.absDay == day && dayLive(state) &&
                state.timeMin < PARADE_FROM ->
                "🎺 Main Street is dressed and closed to " +
                    "through traffic — bunting on the " +
                    "lampposts, the reviewing stand up at " +
                    "City Hall. Step-off is 10 AM. (Trips " +
                    "THROUGH the street wait a float's " +
                    "width today: +$PASS_DELAY_MIN min. " +
                    "Trips here pay nothing extra — you're " +
                    "coming TO the parade.)"
            state.absDay == day && beatsHere(state) ->
                "🎺 THE PARADE IS ROLLING — the stand on " +
                    "the street holds the day's beats. " +
                    "Floats pass in the posted order; the " +
                    "Mayor's car rides last."
            state.absDay == day && dayLive(state) ->
                "🎺 The street is the parade's until " +
                    "13:30 — the stand holds what's left " +
                    "of the day."
            state.absDay in
                ParadeCalendar.announceDay(year)..(day - 1) -> {
                val left = day - state.absDay
                "🎺 A parade poster on the reviewing stand: " +
                    "THE FOUNDERS' DAY PARADE — " +
                    Dance.dateLabel(day) +
                    (if (left == 1) ", TOMORROW."
                     else ", in $left days.") +
                    " The order is posted: the school " +
                    "leads, the mayor rides last. Through " +
                    "traffic will wait a float's width on " +
                    "the day (+$PASS_DELAY_MIN min through " +
                    "Main Street)."
            }
            rec != null && rec.wrapped.isNotEmpty() &&
                state.absDay in (day + 1)..(day + 7) ->
                "🎺 Confetti in the gutters, chalk arrows " +
                    "still on the asphalt. " +
                    wrapLine(state, rec)
            else -> null
        }
    }

    /** The parade in other people's mouths — a seam on
     *  the talk chain, not a rail (the gate stands at
     *  eleven). Riders talk their float in the run-up;
     *  riders and watchers quote the day for a week
     *  after. One line, seeded, from the day's own
     *  derived people. */
    fun gossipLine(state: GameState,
                   npcName: String): Line? {
        val year = currentYear(state)
        val day = ParadeCalendar.paradeDay(year)
        val inRunUp = state.absDay in
            ParadeCalendar.announceDay(year)..(day - 1)
        val inEcho = state.absDay in (day + 1)..(day + 7)
        if (!inRunUp && !inEcho) return null
        if ((state.absDay + npcName.length) % 3 != 0) {
            return null
        }
        val rec = record(state, year)
        if (rec != null && rec.yieldWhy.isNotEmpty()) {
            return null
        }
        if (inRunUp) {
            val entry = procession(state, year)
                .firstOrNull { npcName in it.crew }
                ?: return null
            return Line(npcName, "Saturday's the parade — " +
                "I'm riding ${entry.name}. ${entry.theme}.")
        }
        val wrapped = rec?.wrapped ?: return null
        if (wrapped.isEmpty()) return null
        val wasIn = procession(state, year).any {
            npcName in it.crew } ||
            npcName in routeCrowd(state, year)
        if (!wasIn) return null
        // once ever each (v0.13.0g: the play's stamp —
        // the hearing is the stamping, so the day is
        // quoted once, not on every seeded day of the
        // echo week)
        val key = "paradePost:$year:$npcName"
        if (state.life[key] != null) return null
        state.life[key] = 1
        val proc = procession(state, year)
        val head = proc.firstOrNull()?.name ?: "the school"
        return Line(npcName, "The parade, though. $head " +
            "led, the mayor closed it — that's the whole " +
            "town in one street, once a year.")
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Bijou movie nights — v0.7.0c (Queue #7 item 3): on Friday
// and Saturday evenings, the theater shows a FEATURE, and
// the town turns out for it.
//
// The cadence is the Bijou's own fiction made playable.
// FRIDAY is premiere night: a feature the town made itself,
// generated whole — title, genre, stars, five title cards —
// from seeded banks, rotating like the vote questions do
// (no repeat inside a named window). SATURDAY is the
// matinee vote's night: when the town's standing pick for
// the Bijou's Saturday matinee (Votes' bijou_matinee
// question) is in force, THAT film headlines the evening
// showing — the vote visibly matters, in lights. On a
// Saturday with no standing pick, the rotation covers the
// slot like any other.
//
// The audience is authored, not rolled: the stars come;
// their friends come (the same chemistry Photos reads);
// their arch-rivals come TO HECKLE; a seeded handful of
// cinephile regulars come regardless; two souls never come
// at all. The house caps at fourteen — a crowd the Crowds
// layout renders cleanly. Between title cards the room
// reacts, and every named reaction comes from a person
// actually standing in it (the Engine routes the audience
// here with the same gathering override dinners and
// finales use; the reactions read real placement back).
//
// Playing it is three reaction prompts (after cards 1, 3
// and 4): LAUGH, GASP, APPLAUD or HECKLE, scored against
// each card's beat — the right reaction is worth 2, a
// near one 1, a whiff 0, and heckling a scene that didn't
// deserve it costs 1, while heckling the feature's one
// immortal flop scene is worth the full 2. The total is
// the night's BUZZ: a dead house, a polite house, a
// buzzing house, or — only on a perfect card — a
// LEGENDARY one, which is the feat. Buzz is remembered:
// the stars' friendship scales with it, every attendee
// carries a "movie night" thread into tomorrow, and the
// Journal's 🎬 MOVIE NIGHTS page keeps the features
// you've seen with their stars and your best house.
//
// Admission is FREE, and that is a position, not an
// omission: City Hall's arts line already bought the
// print — it is the same money the Saturday-matinee vote
// spends — so the town's ticket is showing up. Coins gate
// enough of this town's life already; the movies are the
// commons.
//
// DEFERENCE, pinned: a screening never collides with the
// town's bigger claims. A case climax (any arc's, judged
// at the evening hour — and the booth case's own nights
// at this theater above all), a season-finale evening,
// or a holiday with its own beat each put the marquee
// dark with an honest one-line reason on the Look text,
// and the cadence simply resumes at the next slot.
//
// Quiet hours respected by construction: doors at 19:00,
// last seating 20:00, the feature's nominal end 21:30 —
// ahead of the 22:00 sleep march and the 22:30 hush the
// Barks keep.
//
// State: no new save keys — the session, the records and
// the counters ride the existing `life`/`lifeText` maps,
// every read clamped against the banks below, so a
// v0.7.0b save loads never having attended and a hostile
// doc can't mint a legendary house it didn't earn.
// ============================================================

object MovieNights {

    // ---------------- the dials (all suite-pinned) ----------------

    /** Screening weekdays (Jobs.weekdayOf: 0 = Sunday …
     *  6 = Saturday): Friday premieres, Saturday headlines. */
    val SCREENING_WEEKDAYS: Set<Int> = setOf(5, 6)

    /** Doors open (19:00). */
    const val DOORS_FROM = 19 * 60

    /** Last seating (20:00) — after this the usher's rope
     *  is up; the feature is already rolling. */
    const val LAST_SEATING = 20 * 60

    /** The audience override lets go (21:30). */
    const val GATHER_TO = 21 * 60 + 30

    /** The feature's nominal end (21:30): before the 22:00
     *  sleep march and the town's 22:30 quiet hours. */
    const val FEATURE_END = 21 * 60 + 30

    /** The house cap, stars included: a crowd the Crowds
     *  layout renders in clean clusters, never a pile-up. */
    const val HOUSE_CAP = 14

    /** Generated features never repeat an identity inside
     *  this many slots of each other. */
    const val NO_REPEAT_SLOTS = 6

    /** Past screenings kept on the Journal page. */
    const val HISTORY_CAP = 12

    /** Friendship the stars take home, by tier. */
    const val PAY_DEAD = 0
    const val PAY_POLITE = 1
    const val PAY_BUZZING = 2
    const val PAY_LEGENDARY = 4

    /** Reaction points: the right one, a near one, a whiff,
     *  and the heckle surcharge (it pays in full only on
     *  the flop — everywhere else it COSTS). */
    const val SCORE_RIGHT = 2
    const val SCORE_CLOSE = 1
    const val SCORE_WRONG = 0
    const val SCORE_HECKLE_OFF = -1

    // ---------------- beats, reactions, tiers ----------------

    const val BEAT_COMEDY = "comedy"
    const val BEAT_SCARE = "scare"
    const val BEAT_HEART = "heart"
    const val BEAT_SPECTACLE = "spectacle"
    const val BEAT_DUD = "dud"

    const val REACT_LAUGH = "laugh"
    const val REACT_GASP = "gasp"
    const val REACT_APPLAUD = "applaud"
    const val REACT_HECKLE = "heckle"

    /** The reaction row, in display order: (id, label). */
    val REACTIONS: List<Pair<String, String>> = listOf(
        REACT_LAUGH to "😂 Laugh",
        REACT_GASP to "😱 Gasp",
        REACT_APPLAUD to "👏 Applaud",
        REACT_HECKLE to "🗣 Heckle")

    const val TIER_DEAD = "dead"
    const val TIER_POLITE = "polite"
    const val TIER_BUZZING = "buzzing"
    const val TIER_LEGENDARY = "legendary"

    /** Buzz totals that make each tier (max score is 6:
     *  three prompts, all landed exactly). */
    const val BUZZ_LEGENDARY = 6
    const val BUZZ_BUZZING = 4
    const val BUZZ_POLITE = 2

    /** The feat a legendary house earns, as the Journal
     *  page carries it. */
    const val FEAT_NAME = "🎬 Legendary House"

    /** The memory topic the house carries into tomorrow. */
    const val TOPIC = "movie night"

    /** The cast who never come — pinned, with their reasons
     *  carried on the Journal page. Towelie always means to
     *  come. Crabtree has seen enough movies driving the bus. */
    val NEVER_ATTENDS: Set<String> = setOf("Towelie", "Crabtree")

    /** How close a friend a star needs before the friend
     *  makes the trip (the town's shared chemistry scale). */
    const val FRIEND_CHEMISTRY = 3

    // ---------------- the cadence ----------------

    fun isScreeningDay(absDay: Int): Boolean =
        Jobs.weekdayOf(absDay) in SCREENING_WEEKDAYS

    /** A screening's slot: its index among all screening
     *  days from day 0 — the rotation's clock. Negative
     *  days (a save's prehistory) read as slot 0's kin. */
    fun slotOf(absDay: Int): Int {
        if (absDay < 0) return 0
        var count = (absDay / 7) * SCREENING_WEEKDAYS.size
        for (r in 0 until absDay % 7) {
            if (Jobs.weekdayOf(r) in SCREENING_WEEKDAYS) count++
        }
        // absDay itself, if it screens, closes its own slot
        return count
    }

    /** The next screening day strictly after [absDay]. */
    fun nextScreeningDay(absDay: Int): Int {
        var d = absDay + 1
        while (!isScreeningDay(d)) d++
        return d
    }

    // ---------------- the feature ----------------

    data class Genre(
        val id: String, val label: String,
        val noun: String,                 // the prop the middles reach for
        val titleNouns: List<String>,
        val openerBeat: String, val finaleBeat: String,
        val opener: String, val flop: String, val finale: String
    )

    val GENRES: List<Genre> = listOf(
        Genre("space", "Space Opera", "starship",
            listOf("Comet", "Signal", "Telescope", "Orbit"),
            BEAT_SPECTACLE, BEAT_SPECTACLE,
            "The Bijou's own print fades in: a cardboard {noun} over a " +
                "foil sky, and Captain {s1} takes the bridge like they " +
                "were born in a captain's chair. {s2}, as the entire " +
                "crew, salutes in four directions.",
            "The science scene. {s2} reads the star charts upside down " +
                "for nine full minutes. The charts are blank. The scene " +
                "keeps going. Somewhere in the middle, {s1} visibly " +
                "gives up on space.",
            "The {noun} comes home on fumes and string, and the whole " +
                "cardboard galaxy holds its breath — and {s1} lands it. " +
                "On the dock. In one piece. Mostly."),
        Genre("western", "Cowboy Picture", "canteen",
            listOf("Posse", "Gulch", "High Noon", "Spurs"),
            BEAT_COMEDY, BEAT_HEART,
            "{s1} rides into town at dawn — the horse is two kids in a " +
                "suit and everyone agrees not to discuss it. The town " +
                "holds its breath. The horse holds its own.",
            "The campfire scene. {s2} sings the whole ballad. All " +
                "eleven verses. The fire is a lamp with a blanket on " +
                "it and the lamp gives up at verse nine.",
            "{s1} and the stranger settle it the town way: a staring " +
                "contest at high noon. It lasts one minute of screen " +
                "time and a lifetime of town argument. The stranger blinks."),
        Genre("monster", "Monster Movie", "monster",
            listOf("Creek", "Hollow", "Maw", "Tracks"),
            BEAT_SCARE, BEAT_SPECTACLE,
            "Something has been eating the town's garden hoses, and " +
                "only {s1} believes the tracks are real. The tracks are " +
                "enormous. The tracks are also, somehow, polite.",
            "The laboratory scene. {s2} explains the {noun}'s weakness " +
                "for eleven minutes using a chalkboard, then erases the " +
                "chalkboard, then explains it again. The {noun}, " +
                "offscreen, can be heard agreeing.",
            "It was never about the teeth. {s1} leaves the gate open " +
                "and a bowl out, and the biggest shadow in Colorado sits " +
                "down like a dog who's been waiting to be asked."),
        Genre("detective", "Detective Story", "notebook",
            listOf("Suspect", "Widow", "Key", "Confession"),
            BEAT_COMEDY, BEAT_HEART,
            "Rain. A trench coat two sizes of ambition too big. " +
                "Detective {s1} lights a candy cigarette and tells the " +
                "empty office: “The town's lying about something. It " +
                "always is.”",
            "The interrogation scene. {s2} confesses to everything — " +
                "the theft, the weather, the concept of Tuesdays. {s1} " +
                "writes it all down. None of it is the case.",
            "{s1} gathers everyone in the parlor — which is the school " +
                "gym with a rug on it — and lays it out, name by name, " +
                "until the room runs out of places to look."),
        Genre("sports", "Sports Drama", "whistle",
            listOf("Comeback", "Trophy", "Underdogs", "Rematch"),
            BEAT_COMEDY, BEAT_HEART,
            "The worst team in the county gets one new player: {s1}, " +
                "who has never seen the sport played and has decided " +
                "that will not be a problem.",
            "The training montage. It's mostly {s2} running up the " +
                "same hill from different angles while the coach's " +
                "{noun} gathers dust. The montage is four minutes. The " +
                "hill is a driveway.",
            "Last play. The whole season in one throw, and {s1} — who " +
                "wasn't supposed to be the story — puts it exactly " +
                "where the story needed it. The scoreboard guy faints."),
        Genre("ghost", "Ghost Story", "lantern",
            listOf("Attic", "Staircase", "Widow", "Candle"),
            BEAT_SCARE, BEAT_HEART,
            "The house at the end of the lane has one rule, painted " +
                "over the door: nobody hums after dark. {s1} hums on " +
                "the first night. Of course {s1} hums on the first night.",
            "The séance scene. The table knocks once, twice, and " +
                "then — through {s2}, in a voice like a drawer closing " +
                "— spells out a grocery list. The ghost, apparently, " +
                "is out of eggs.",
            "{s1} finally asks the house what it wants, and the answer " +
                "is so small and so sad that the whole theater forgets " +
                "to be scared: it just wanted the porch light left on."),
        Genre("musical", "Musical", "microphone",
            listOf("Encore", "Chorus", "Spotlight", "Reprise"),
            BEAT_COMEDY, BEAT_SPECTACLE,
            "The town wakes up singing — every errand, every argument, " +
                "every weather report, in four-part harmony. Only {s1} " +
                "can't find the key. The key is the plot.",
            "The dream ballet. {s2} dances the history of the town in " +
                "a cardboard crown. It takes nine minutes and explains " +
                "nothing, and yet — and yet — nobody in the theater " +
                "checks the time.",
            "For the last number, {s1} finally sings — one note, " +
                "held, true — and the whole cast turns like sunflowers. " +
                "The {noun} weeps. Microphones don't weep. This one does."),
        Genre("sea", "Sea Adventure", "compass",
            listOf("Harbor", "Current", "Lighthouse", "Storm"),
            BEAT_SPECTACLE, BEAT_HEART,
            "The tide takes the dock, the dock takes the argument, " +
                "and {s1} — who cannot swim and refuses to learn on " +
                "principle — signs onto the last boat out anyway.",
            "The map scene. {s2} 'reads' the chart for ten full " +
                "minutes: it's a placemat. The X is a ketchup stain. " +
                "The crew nods like it's scripture.",
            "The storm spends itself by dawn, and {s1} brings what's " +
                "left of the boat home on a rope and a promise. The " +
                "harbor bell rings itself. Nobody admits to ringing it.")
    )

    fun genreById(id: String?): Genre? = GENRES.firstOrNull { it.id == id }

    /** Middle-card templates, tagged with their beats — the
     *  shared second and fourth acts of every town print. */
    val MIDDLES: List<Pair<String, String>> = listOf(
        BEAT_COMEDY to "{s2} tries to fix the {noun} with a hammer and " +
            "total confidence. It works. Nobody can say why, least of " +
            "all {s2}.",
        BEAT_SCARE to "The lights go out. In the dark, something says " +
            "{s1}'s name — in {s1}'s own voice.",
        BEAT_HEART to "{s1} finally says the thing they've been " +
            "carrying since the first scene. You could hear a pin " +
            "decline to drop.",
        BEAT_SPECTACLE to "The chase crosses the whole town — over " +
            "the fences, across the pond ice, straight through the " +
            "school play. The school play does not pause.",
        BEAT_COMEDY to "{s3} explains the entire plan using only " +
            "snacks as props. The snacks do not survive the explanation.",
        BEAT_SCARE to "Behind the door: nothing. The nothing is " +
            "worse. {s2} checks a second time, which the whole theater " +
            "agrees was a mistake.",
        BEAT_HEART to "The dog comes back. Everyone knew the dog " +
            "would come back. It doesn't matter. The dog comes back.",
        BEAT_SPECTACLE to "Everything the first scene promised would " +
            "fall apart, falls apart — all at once, and beautifully, " +
            "and right on schedule.",
        BEAT_COMEDY to "{s1} and {s2} argue in complete silence for " +
            "a full minute. It is the funniest minute of the year.",
        BEAT_HEART to "An old photograph. A held breath. {s3} looks " +
            "away first, and that's the whole scene, and somehow " +
            "it's enough.")

    private val TITLE_ADJECTIVES = listOf("Velvet", "Midnight",
        "Seventh", "Uninvited", "Patient", "Loudest", "Smallest", "Second")

    private val TITLE_PLACES = listOf("the Pond", "the Pass",
        "Main Street", "the Hollow", "the Fair", "the Ridge")

    data class Card(val beat: String, val text: String)

    data class Feature(
        val key: String,              // "s<slot>" or "v<optionId>@<day>"
        val title: String,
        val genreId: String,
        val genreLabel: String,
        val stars: List<String>,      // 2–4, in KID_ORDER's town
        val headline: Boolean,        // the vote's pick, headlining
        val cards: List<Card>         // exactly five, exactly one dud
    ) {
        /** What makes a feature itself for the rotation: the
         *  same title in the same genre with the same stars
         *  is the same picture, however the cards fell. */
        fun identity(): String =
            "${title.lowercase()}|${genreId}|${stars.joinToString(",")}"
    }

    private fun fill(template: String, genre: Genre, stars: List<String>,
                     rnd: Random): String {
        var t = template
            .replace("{s1}", stars.getOrNull(0) ?: "someone")
            .replace("{s2}", stars.getOrNull(1) ?: stars.getOrNull(0) ?: "someone")
            .replace("{s3}", stars.getOrNull(2) ?: stars.getOrNull(1)
                ?: stars.getOrNull(0) ?: "someone")
            .replace("{noun}", genre.noun)
        if ("{place}" in t) {
            t = t.replace("{place}", TITLE_PLACES[rnd.nextInt(TITLE_PLACES.size)])
        }
        return t
    }

    private fun cardsFor(genre: Genre, stars: List<String>,
                         seed: Long, dudAt: Int): List<Card> {
        val rnd = Random(seed)
        val midIdx = MIDDLES.indices.shuffled(rnd).take(2)
        val cards = arrayOfNulls<Card>(5)
        cards[0] = Card(genre.openerBeat, fill(genre.opener, genre, stars, rnd))
        cards[4] = Card(genre.finaleBeat, fill(genre.finale, genre, stars, rnd))
        cards[dudAt] = Card(BEAT_DUD, fill(genre.flop, genre, stars, rnd))
        var mi = 0
        for (i in 1..3) {
            if (cards[i] != null) continue
            val (beat, text) = MIDDLES[midIdx[mi++]]
            cards[i] = Card(beat, fill(text, genre, stars, rnd))
        }
        return cards.map { it!! }
    }

    /** The raw candidate for a slot, before the rotation's
     *  no-repeat discipline gets its say. */
    private fun rawFeature(slot: Int, attempt: Int): Feature {
        val rnd = Random(slot * 1_000_003L + attempt * 97_531L + 12_345L)
        val genre = GENRES[rnd.nextInt(GENRES.size)]
        val nStars = 2 + rnd.nextInt(3)
        // the never-come never star either: a feature cannot
        // headline someone the house rules say never attends
        val stars = KID_ORDER.filter { it !in NEVER_ATTENDS }
            .shuffled(rnd).take(nStars)
        val titleNoun = genre.titleNouns[rnd.nextInt(genre.titleNouns.size)]
        val title = when (rnd.nextInt(5)) {
            0 -> "The ${TITLE_ADJECTIVES[rnd.nextInt(TITLE_ADJECTIVES.size)]} $titleNoun"
            1 -> "${stars[0]} and the $titleNoun"
            2 -> "The $titleNoun of ${TITLE_PLACES[rnd.nextInt(TITLE_PLACES.size)]}"
            3 -> "Night of the $titleNoun"
            else -> "The Last $titleNoun in Town"
        }
        val cards = cardsFor(genre, stars,
            slot * 77_777L + attempt * 131L + 7L, dudAt = 1 + (slot % 3))
        return Feature("s$slot", title, genre.id, genre.label,
            stars, headline = false, cards = cards)
    }

    private val genMemo = mutableListOf<Feature>()
    private val genLock = Any()

    /** The rotation's feature for [slot]: a pure function of
     *  the slot (recomputed from slot 0 behind a memo, exactly
     *  like the vote rotation), distinct in identity from the
     *  NO_REPEAT_SLOTS features before it. */
    fun generatedFeature(slot: Int): Feature {
        if (slot < 0) return rawFeature(0, 0)
        synchronized(genLock) {
            while (genMemo.size <= slot) {
                val s = genMemo.size
                var attempt = 0
                var cand = rawFeature(s, attempt)
                val window = genMemo.takeLast(NO_REPEAT_SLOTS)
                    .map { it.identity() }.toSet()
                while (cand.identity() in window && attempt < 64) {
                    attempt++
                    cand = rawFeature(s, attempt)
                }
                genMemo.add(cand)
            }
            return genMemo[slot]
        }
    }

    /** The vote's matinee winners bent into headline
     *  features: the genre each winning print plays as. */
    val HEADLINE_GENRES: Map<String, String> = mapOf(
        "pancakes" to "musical",   // a breakfast epic, sung
        "moo" to "western",        // a cow's journey west
        "space" to "space")        // homework, in orbit

    /** Tonight's feature for an arbitrary day, read off the
     *  state as it stands: on a Saturday with the matinee
     *  vote standing, the winner headlines; everywhere else
     *  the rotation provides. */
    fun featureForDay(state: GameState, absDay: Int): Feature {
        if (Jobs.weekdayOf(absDay) == 6) {
            val q = Votes.questionById("bijou_matinee")
            val win = q?.let { Votes.standingOption(state, it) }
            if (q != null && win != null) {
                val genre = genreById(HEADLINE_GENRES[win.id]) ?: GENRES[0]
                val rnd = Random(absDay * 31_337L + win.id.hashCode().toLong())
                val nStars = 2 + rnd.nextInt(3)
                val stars = KID_ORDER.filter { it !in NEVER_ATTENDS }
                    .shuffled(rnd).take(nStars)
                val cards = cardsFor(genre, stars,
                    absDay * 55_511L + 3L, dudAt = 1 + (absDay % 3))
                return Feature("v${win.id}@$absDay", win.label, genre.id,
                    genre.label, stars, headline = true, cards = cards)
            }
        }
        return generatedFeature(slotOf(absDay))
    }

    /** Tonight's feature. */
    fun featureFor(state: GameState): Feature =
        featureForDay(state, state.absDay)

    // ---------------- deference ----------------

    const val DARK_CASE = "case"
    const val DARK_FINALE = "finale"
    const val DARK_HOLIDAY = "holiday"
    const val DARK_TALENT = "talent"
    const val DARK_FESTIVAL = "festival"

    /** Any case's climax judged at the evening hour: the
     *  four due-functions read on a copy of the state with
     *  the clock at doors, so a climax that lands tonight
     *  is visible this morning. Two player-facing climaxes
     *  never stack — the house rule since the finales. */
    fun anyClimaxDueTonight(state: GameState): Boolean {
        val ev = if (state.timeMin == DOORS_FROM) state
            else state.copy(timeMin = DOORS_FROM)
        return Mystery.finaleChoiceDue(ev) ||
            Mystery2.accusationDue(ev) ||
            Mystery3.dispositionDue(ev) ||
            Mystery4.vigilDue(ev) ||
            Mystery5.confrontationDue(ev) ||
            Mystery6.meetingDue(ev) ||
            Mystery7.restageDue(ev) ||
            Mystery8.readingDue(ev) ||
            Mystery9.auditDue(ev) ||
            Mystery10.hangingDue(ev) ||
            Mystery11.voicingDue(ev) ||
            Mystery12.returnsDue(ev) ||
            Mystery13.verdictDue(ev)
    }

    /** Why tonight's marquee is dark, or null if the show
     *  goes on. The booth case's own nights at this theater
     *  book it outright; then any case's climax; then a
     *  finale evening; then a holiday with its own beat.
     *  The cases outrank everything — they were here first. */
    fun darkReason(state: GameState): String? {
        val sid = state.shenaniganId
        if (!state.arc2Retired &&
            (sid == Mystery2.MARATHON_ID || sid == Mystery2.ACCUSATION_ID)) {
            return DARK_CASE
        }
        if (anyClimaxDueTonight(state)) return DARK_CASE
        if (Finales.windowAt(state) != null) return DARK_FINALE
        val hid = state.holidayId
        if (hid != null && HolidayBeats.beatFor(hid) != null) return DARK_HOLIDAY
        // v0.13.0c: talent night — the Bijou's stage is
        // the show's one Saturday a year, and the
        // screening stands down for it. It ranks below
        // case, finale and holiday on purpose: the show
        // itself yields to all three (Talent.yieldReason
        // reads the same quiet-day definition), so on a
        // night one of them owns, the marquee says THEIR
        // name, and the show's poster carries the yield.
        if (Talent.nightClaimed(state)) return DARK_TALENT
        // v0.14.0c: festival week — the Bijou's screen
        // belongs to the festival's five nights, and the
        // regular feature stands down for the week. It
        // ranks last for the same reason the talent show
        // does: the festival itself yields to case,
        // finale and holiday (Festival.nightDarkReason
        // reads this same ladder), so on a night one of
        // them owns, the marquee says THEIR name.
        if (Festival.isFestivalNight(state)) return DARK_FESTIVAL
        return null
    }

    /** The one honest line the marquee carries on a dark
     *  night — the reason, named, no mystery told. */
    fun darkLine(state: GameState): String? = when (darkReason(state)) {
        DARK_CASE -> "🎬 Dark tonight — the Bijou's booked solid. Town business."
        DARK_FINALE -> "🎬 Dark tonight — the whole town's at the season finale."
        DARK_HOLIDAY -> {
            val name = state.holidayId
                ?.let { id -> HOLIDAYS.firstOrNull { it.id == id }?.name } ?: "the holiday"
            "🎬 Dark tonight — the Bijou closes for $name."
        }
        DARK_TALENT -> "🎬 Dark tonight — the talent show has the stage."
        DARK_FESTIVAL -> "🎬 Dark tonight — the film festival has the screen."
        else -> null
    }

    fun isDarkTonight(state: GameState): Boolean = darkReason(state) != null

    // ---------------- the audience ----------------

    /** Draw strength of [name] toward a house built around
     *  [stars]: a star's friends ride the town's shared
     *  chemistry; a star's rival comes to heckle (and says
     *  so); a cinephile regular comes on the seeded roll
     *  alone. 0 = staying home. Pure in the state. */
    fun drawOf(state: GameState, name: String, stars: List<String>,
               slot: Int): Int {
        if (name in stars) return Int.MAX_VALUE
        if (name in NEVER_ATTENDS) return 0
        var best = 0
        for (star in stars) {
            if (Photos.isRivalPair(state, name, star)) {
                best = maxOf(best, 40)           // here to heckle
            } else {
                val chem = Photos.chemistryBetween(state, name, star)
                if (chem >= FRIEND_CHEMISTRY) best = maxOf(best, 30 + chem)
            }
        }
        if (best == 0) {
            // the regulars: one in six of the town keeps a
            // standing Friday-night habit, seeded by the slot
            val roll = ((name.hashCode() * 31 + slot * 7919) and 0x7fffffff) % 6
            if (roll == 0) best = 10
        }
        return best
    }

    private data class AttKey(val day: Int, val headline: String?,
                              val bondsHash: Int)
    private var attMemo: Pair<AttKey, List<String>>? = null

    /** Tonight's house, in seating order: the stars first,
     *  then the drawn — friends, hecklers, regulars — by
     *  draw strength, capped at the house cap. Deterministic
     *  in the state; memoized per (day, headline, bonds). */
    fun attendance(state: GameState): List<String> {
        val feature = featureFor(state)
        val key = AttKey(state.absDay,
            if (feature.headline) feature.key else null,
            state.npcBonds.hashCode())
        attMemo?.let { (k, v) -> if (k == key) return v }
        val slot = slotOf(state.absDay)
        val drawn = KID_ORDER
            .filter { it !in feature.stars }
            .map { it to drawOf(state, it, feature.stars, slot) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }
                .thenBy { it.first })
            .map { it.first }
        val house = (feature.stars + drawn).take(HOUSE_CAP)
        attMemo = key to house
        return house
    }

    /** The Engine's gathering hook (the dinners/finales
     *  pattern): on a live screening night, the house is AT
     *  the Bijou from doors until the feature lets out.
     *  Cheap gates first — most calls are not screening
     *  evenings at all. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in DOORS_FROM until GATHER_TO) return null
        if (!isScreeningDay(state.absDay)) return null
        if (npcName !in attendance(state)) return null
        // a dark house gathers nobody (the lite check: the
        // full climax scan is for the player-facing surfaces)
        val sid = state.shenaniganId
        if (!state.arc2Retired &&
            (sid == Mystery2.MARATHON_ID || sid == Mystery2.ACCUSATION_ID)) {
            return null
        }
        if (Finales.windowAt(state) != null) return null
        val hid = state.holidayId
        if (hid != null && HolidayBeats.beatFor(hid) != null) return null
        // v0.13.0c: talent night stands the house down —
        // the stage belongs to the living acts, and the
        // show's own claim seats its room.
        if (Talent.nightClaimed(state)) return null
        // v0.14.0c: a festival night stands the regular
        // house down — the screen belongs to the
        // festival's film, and the festival's own claim
        // (seated twentieth) seats its room.
        if (Festival.isFestivalNight(state)) return null
        return AgendaBlock("Bijou",
            "Movie night at the Bijou — in their seat before the lights go down.")
    }

    /** Who actually shares the theater with the player right
     *  now — the reaction lines' only lawful cast list. */
    fun presentCast(state: GameState): List<String> {
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return emptyList()
        return state.agents.values.filter {
            !it.isPlayer && it.location == "Bijou" && it.location == me.location &&
                it.waypoints.isEmpty() &&
                CharacterSystem.npcInside(it.location) == state.playerInside &&
                !it.asleep
        }.map { it.name }.sorted()
    }

    // ---------------- reaction scoring ----------------

    /** The (right, close) reactions for a card's beat. A dud
     *  is judged by the heckle; every other beat takes the
     *  heckle as an insult, never a near miss. */
    fun rightFor(beat: String): String = when (beat) {
        BEAT_COMEDY -> REACT_LAUGH
        BEAT_SCARE -> REACT_GASP
        BEAT_HEART -> REACT_APPLAUD
        BEAT_SPECTACLE -> REACT_GASP
        BEAT_DUD -> REACT_HECKLE
        else -> REACT_APPLAUD
    }

    fun closeFor(beat: String): String = when (beat) {
        BEAT_COMEDY -> REACT_APPLAUD
        BEAT_SCARE -> REACT_LAUGH
        BEAT_HEART -> REACT_GASP
        BEAT_SPECTACLE -> REACT_APPLAUD
        BEAT_DUD -> REACT_LAUGH
        else -> REACT_LAUGH
    }

    /** What [reaction] earns against a card of [beat]. */
    fun scoreFor(beat: String, reaction: String): Int = when {
        reaction == rightFor(beat) -> SCORE_RIGHT
        reaction == REACT_HECKLE -> SCORE_HECKLE_OFF
        reaction == closeFor(beat) -> SCORE_CLOSE
        else -> SCORE_WRONG
    }

    /** The house a buzz total made. */
    fun tierFor(buzz: Int): String = when {
        buzz >= BUZZ_LEGENDARY -> TIER_LEGENDARY
        buzz >= BUZZ_BUZZING -> TIER_BUZZING
        buzz >= BUZZ_POLITE -> TIER_POLITE
        else -> TIER_DEAD
    }

    fun tierLabel(tier: String): String = when (tier) {
        TIER_LEGENDARY -> "a LEGENDARY house"
        TIER_BUZZING -> "a buzzing house"
        TIER_POLITE -> "a polite house"
        else -> "a dead house"
    }

    /** Friendship the stars take home from a house of [tier]. */
    fun starPay(tier: String): Int = when (tier) {
        TIER_LEGENDARY -> PAY_LEGENDARY
        TIER_BUZZING -> PAY_BUZZING
        TIER_POLITE -> PAY_POLITE
        else -> PAY_DEAD
    }

    // ---------------- audience reaction lines ----------------

    private val CHEER_LINES = listOf(
        "{name} is on their feet before the card's even done. " +
            "“THAT'S {star}! I KNOW {star}!”",
        "{name} applauds like the screen owes them money. " +
            "“AGAIN! DO THE WHOLE MOVIE AGAIN!”",
        "{name}, to the whole row: “Told you. TOLD you {star} " +
            "was the star.”")

    private val HECKLE_LINES = listOf(
        "{name}, radiant with joy: “BOOO. Wonderful. BOOO.”",
        "{name} heckles in a stage whisper that carries to the " +
            "parking lot: “MY GRANDMA DIRECTS BETTER. SHE'S BLIND.”",
        "{name}: “I have waited all week to dislike this scene " +
            "and it has NOT disappointed.”")

    private val GENERIC_LINES = listOf(
        "{name} explains the plot to their neighbor, at length, " +
            "completely wrong, with total confidence.",
        "{name}'s popcorn is gone by the second card. They are " +
            "now eating the bucket's structural support.",
        "{name} laughs one beat after everyone else, every time, " +
            "like the movie's on a delay only they can hear.")

    private val KENNY_LINES = listOf(
        "(muffled) “Mmph mmph mmph — mmph!” Kenny is having the " +
            "night of his life back there.",
        "(muffled) “Mmph! Mmmph mmph!” Kenny, reviewing in real time.")

    private val NED_LINES = listOf(
        "Ned, through the voice box: “I. AM. NOT. CRYING. IT. " +
            "IS. THE. PROJECTOR.”",
        "Ned, through the voice box: “TEN. OUT. OF. TEN. NO. NOTES.”")

    private val FALLBACK_LINES = listOf(
        "Somewhere behind you, a seat creaks, popcorn goes " +
            "everywhere at the quiet part, and somebody whispers " +
            "'sorry' to the screen.",
        "The row behind you has strong feelings and no indoor " +
            "voice. The usher's flashlight makes one slow, " +
            "hopeless pass.")

    /** A reaction from the room for the gap after card
     *  [gap] (0-based). Composed ONLY from the cast actually
     *  present: a star's rival heckles first if they're in
     *  the room, then a star's friend cheers, then anyone —
     *  with Kenny muffled and Ned on the voice box, as the
     *  town renders them everywhere else. Nobody present:
     *  the room itself answers, nameless. */
    fun reactionLine(state: GameState, feature: Feature, gap: Int): String {
        val present = presentCast(state)
        if (present.isEmpty()) {
            return FALLBACK_LINES[
                ((gap * 31 + feature.key.hashCode()) and 0x7fffffff) %
                    FALLBACK_LINES.size]
        }
        val seed = feature.key.hashCode() * 131 + gap * 17
        fun pickFrom(names: List<String>, bank: List<String>,
                     star: String?): String {
            val name = names[((seed + names.size) and 0x7fffffff) % names.size]
            if (name == "Kenny") return KENNY_LINES[
                (seed and 0x7fffffff) % KENNY_LINES.size]
            if (name == "Ned") return NED_LINES[
                (seed and 0x7fffffff) % NED_LINES.size]
            var line = bank[(seed and 0x7fffffff) % bank.size]
                .replace("{name}", name)
            if (star != null) line = line.replace("{star}", star)
            return line
        }
        // a rival of any star, in the room, heckles
        for (star in feature.stars) {
            val rivals = present.filter {
                it !in feature.stars && Photos.isRivalPair(state, it, star) }
            if (rivals.isNotEmpty()) return pickFrom(rivals, HECKLE_LINES, star)
        }
        // a friend of any star cheers
        for (star in feature.stars) {
            val friends = present.filter {
                it !in feature.stars &&
                    Photos.chemistryBetween(state, it, star) >= FRIEND_CHEMISTRY }
            if (friends.isNotEmpty()) return pickFrom(friends, CHEER_LINES, star)
        }
        return pickFrom(present, GENERIC_LINES,
            feature.stars.firstOrNull())
    }

    // ---------------- state (life / lifeText, clamped) ----------------

    private fun kSeat(day: Int) = "movieSeat:$day"       // life: 1 = seated
    private fun kStep(day: Int) = "movieStep:$day"       // life: beat index
    private fun kBuzz(day: Int) = "movieBuzz:$day"       // life: running buzz
    private fun kFeat(day: Int) = "movieFeat:$day"       // lifeText: feature key
    private fun kRec(day: Int) = "movieRec:$day"         // lifeText: the night's record
    private const val K_HISTORY = "movieHistory"         // lifeText: ";" entries
    private const val K_ATTENDED = "movieAttended"       // life: lifetime attended
    private const val K_BEST = "movieBestBuzz"           // life: best buzz (can dip < 0)

    data class MovieRecord(
        val day: Int, val key: String, val title: String,
        val stars: List<String>, val tier: String, val buzz: Int
    )

    private fun encodeRecord(r: MovieRecord): String =
        listOf(r.day.toString(), r.key,
            r.title.replace("|", " ").replace(";", " "),
            r.stars.joinToString(","),
            r.tier, r.buzz.toString()).joinToString("|")

    private fun decodeRecord(raw: String?): MovieRecord? {
        raw ?: return null
        val p = raw.split("|")
        if (p.size != 6) return null
        val day = p[0].toIntOrNull() ?: return null
        if (day < 0 || p[1].isEmpty() || p[2].isBlank()) return null
        val stars = p[3].split(",").filter { it in KID_ORDER }
        if (stars.isEmpty()) return null
        if (p[4] !in setOf(TIER_DEAD, TIER_POLITE, TIER_BUZZING,
                TIER_LEGENDARY)) return null
        val buzz = p[5].toIntOrNull() ?: return null
        if (buzz !in -9..99) return null
        return MovieRecord(day, p[1], p[2], stars, p[4], buzz)
    }

    fun recordFor(state: GameState, day: Int): MovieRecord? =
        decodeRecord(state.lifeText[kRec(day)])

    fun history(state: GameState): List<MovieRecord> {
        val raw = state.lifeText[K_HISTORY] ?: return emptyList()
        return raw.split(";").mapNotNull { decodeRecord(it) }
            .take(HISTORY_CAP)
    }

    private fun writeRecord(state: GameState, r: MovieRecord) {
        state.lifeText[kRec(r.day)] = encodeRecord(r)
        val hist = (listOf(r) + history(state).filter { it.day != r.day })
            .take(HISTORY_CAP)
        state.lifeText[K_HISTORY] = hist.joinToString(";") { encodeRecord(it) }
    }

    fun attendedCount(state: GameState): Int =
        (state.life[K_ATTENDED] ?: 0).coerceAtLeast(0)

    fun bestBuzz(state: GameState): Int? =
        if (attendedCount(state) == 0) null else state.life[K_BEST]

    fun featDone(state: GameState): Boolean =
        history(state).any { it.tier == TIER_LEGENDARY } ||
            attendedCount(state) > HISTORY_CAP &&
                (state.life[K_BEST] ?: 0) >= BUZZ_LEGENDARY

    // ---------------- the session ----------------

    const val ST_IDLE = "idle"               // not a screening day
    const val ST_DARK = "dark"               // a screening day, deferred
    const val ST_BEFORE = "before"           // screening evening, doors shut yet
    const val ST_SEATING = "seating"         // doors open, not yet seated
    const val ST_RESUME = "resume"           // seated, mid-feature
    const val ST_LATE = "late"               // doors closed, never seated
    const val ST_DONE = "done"               // tonight's feature, seen

    data class Status(val kind: String, val text: String)

    fun isSeated(state: GameState): Boolean =
        (state.life[kSeat(state.absDay)] ?: 0) == 1 &&
            recordFor(state, state.absDay) == null

    /** Where tonight stands for a player looking at the
     *  screen — the dialog's whole script in one reading. */
    fun sessionStatus(state: GameState): Status {
        if (!isScreeningDay(state.absDay)) {
            val next = nextScreeningDay(state.absDay)
            val nf = featureForDay(state, next)
            val dayName = Jobs.WEEKDAY_NAMES[Jobs.weekdayOf(next)]
            return Status(ST_IDLE,
                "The screen is dark. The Bijou shows its features on " +
                    "Friday and Saturday nights — next: $dayName, " +
                    "“${nf.title}” (${nf.genreLabel}).")
        }
        darkLine(state)?.let { return Status(ST_DARK, it) }
        if (recordFor(state, state.absDay) != null) {
            val f = featureFor(state)
            return Status(ST_DONE,
                "Tonight's feature — “${f.title}” — has rolled its " +
                    "credits for you. It's on your 🎬 MOVIE NIGHTS page.")
        }
        if (isSeated(state)) {
            return Status(ST_RESUME, "Your seat is still warm. The " +
                "feature is waiting where you left it.")
        }
        val f = featureFor(state)
        val starsLine = f.stars.joinToString(", ")
        val headline = if (f.headline)
            "\n🏆 The town voted for this one — tonight it headlines."
        else ""
        return when {
            state.timeMin < DOORS_FROM -> Status(ST_BEFORE,
                "🎬 Tonight at the Bijou: “${f.title}” — a " +
                    "${f.genreLabel.lowercase()} starring $starsLine." +
                    "$headline\nDoors at 7. Admission is the town's " +
                    "treat: showing up is the ticket.")
            state.timeMin <= LAST_SEATING -> Status(ST_SEATING,
                "🎬 “${f.title}” — a ${f.genreLabel.lowercase()} " +
                    "starring $starsLine.$headline\nThe house is " +
                    "filling. Your seat is free — the town already " +
                    "paid for the print.")
            else -> Status(ST_LATE,
                "The feature — “${f.title}” — is already rolling, " +
                    "and the usher's rope is up. Some nights are " +
                    "like that. The next one is " +
                    Jobs.WEEKDAY_NAMES[Jobs.weekdayOf(
                        nextScreeningDay(state.absDay))] + ".")
        }
    }

    /** Take the seat. True only on a live screening night,
     *  inside the doors window, before the credits have
     *  rolled — and it costs nothing, by design. */
    fun seat(state: GameState): Boolean {
        if (!isScreeningDay(state.absDay)) return false
        if (isDarkTonight(state)) return false
        if (recordFor(state, state.absDay) != null) return false
        if (state.timeMin !in DOORS_FROM..LAST_SEATING) return false
        if ((state.life[kSeat(state.absDay)] ?: 0) == 1) return false
        val f = featureFor(state)
        state.life[kSeat(state.absDay)] = 1
        state.life[kStep(state.absDay)] = 0
        state.life[kBuzz(state.absDay)] = 0
        state.lifeText[kFeat(state.absDay)] = f.key
        return true
    }

    // The evening's running order: five cards, a reaction
    // from the room in the two early gaps, a prompt after
    // cards 1, 3 and 4, then the credits.
    private val BEAT_KINDS = listOf(
        "card", "reaction", "card", "prompt",
        "card", "reaction", "card", "prompt",
        "card", "prompt", "credits")
    private val BEAT_CARDS = listOf(
        0, 0, 1, 1, 2, 2, 3, 3, 4, 4, -1)
    /** Which gaps carry a prompt (by card index). */
    val PROMPT_CARDS: List<Int> = listOf(1, 3, 4)

    data class Beat(val kind: String, val text: String,
                    val cardIndex: Int = -1)

    /** The beat on screen right now for a seated player —
     *  or null when there's no live session. Reaction text
     *  is composed at presentation time, from the room as
     *  it actually stands. */
    fun sessionBeat(state: GameState): Beat? {
        if (!isSeated(state)) return null
        val step = (state.life[kStep(state.absDay)] ?: 0)
            .coerceIn(0, BEAT_KINDS.size - 1)
        val f = featureFor(state)
        val kind = BEAT_KINDS[step]
        val cardIdx = BEAT_CARDS[step]
        return when (kind) {
            "card" -> Beat("card",
                "🎞 ${cardIdx + 1} — ${f.cards[cardIdx].text}", cardIdx)
            "reaction" -> Beat("reaction",
                reactionLine(state, f, cardIdx), cardIdx)
            "prompt" -> Beat("prompt",
                "The card lands. The room holds its breath — and " +
                    "looks, somehow, at you.", cardIdx)
            else -> Beat("credits", creditsText(state, f), cardIdx)
        }
    }

    /** The room's answer to a reaction choice, for the
     *  dialog to read before the next card. */
    private fun reactionOutcomeText(score: Int, beat: String,
                                    reaction: String): String = when {
        reaction == REACT_HECKLE && beat == BEAT_DUD ->
            "The heckle lands and the whole row loses it. Even the " +
                "usher nods. Especially the usher. (+$score buzz)"
        reaction == REACT_HECKLE ->
            "You heckle straight into the scene's held breath. The " +
                "row in front turns around. All of them. Slowly. " +
                "($score buzz)"
        score >= SCORE_RIGHT ->
            "The room goes with you like you cued it. Perfect " +
                "timing — the strangers in the next row think you " +
                "planned it. (+$score buzz)"
        score == SCORE_CLOSE ->
            "A few people join in half a beat late. The room calls " +
                "it close enough, and means it kindly. (+$score buzz)"
        else ->
            "Your reaction lands in total silence. One cough, from " +
                "the very back, makes it worse. (+$score buzz)"
    }

    /** Answer the current prompt with [reactionId]. Returns
     *  the room's response, or null if no prompt is on
     *  screen. Scoring is the whole game: it lands in the
     *  running buzz the credits will read. */
    fun react(state: GameState, reactionId: String): String? {
        if (!isSeated(state)) return null
        if (reactionId !in REACTIONS.map { it.first }) return null
        val step = (state.life[kStep(state.absDay)] ?: 0)
            .coerceIn(0, BEAT_KINDS.size - 1)
        if (BEAT_KINDS[step] != "prompt") return null
        val f = featureFor(state)
        val card = f.cards[BEAT_CARDS[step]]
        val score = scoreFor(card.beat, reactionId)
        state.life[kBuzz(state.absDay)] =
            (state.life[kBuzz(state.absDay)] ?: 0) + score
        state.life[kStep(state.absDay)] = step + 1
        return reactionOutcomeText(score, card.beat, reactionId)
    }

    /** Move past a card, a reaction, or the credits. Past
     *  the credits is the wrap: the night's records are
     *  written, the buzz is paid out, and the session ends. */
    fun advance(state: GameState) {
        if (!isSeated(state)) return
        val step = (state.life[kStep(state.absDay)] ?: 0)
            .coerceIn(0, BEAT_KINDS.size - 1)
        if (BEAT_KINDS[step] == "credits") {
            finishFeature(state)
            return
        }
        if (BEAT_KINDS[step] == "prompt") return  // prompts settle via react()
        state.life[kStep(state.absDay)] = step + 1
    }

    fun buzzNow(state: GameState): Int =
        state.life[kBuzz(state.absDay)] ?: 0

    private fun creditsText(state: GameState, f: Feature): String {
        val buzz = buzzNow(state)
        val tier = tierFor(buzz)
        val starsLine = f.stars.joinToString(", ")
        val pay = starPay(tier)
        val tierLine = when (tier) {
            TIER_LEGENDARY ->
                "The house BUZZED off the scale — people are still " +
                    "arguing about the flop scene in the lobby, " +
                    "fondly, at volume."
            TIER_BUZZING ->
                "The house buzzed — a real roar at the finale, and " +
                    "the flop scene got the laugh it was owed."
            TIER_POLITE ->
                "A polite house: kind claps, warm seats, home by " +
                    "ten. The stars take it gracefully."
            else ->
                "The house sat on its hands. The projector's " +
                    "applause track played to no one."
        }
        return "🎬 CREDITS — “${f.title}”, starring $starsLine.\n" +
            "$tierLine\n" +
            "Tonight was ${tierLabel(tier)} (buzz $buzz)." +
            (if (pay > 0)
                "\nThe stars feel the room in their bones — and in " +
                    "their opinion of you. (+$pay friendship each)"
            else
                "\nThe stars file out quietly. Some houses are " +
                    "like that.")
    }

    private fun plantMovieTopic(name: String) {
        if (name !in KID_ORDER) return
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    private fun bumpFriendship(state: GameState, name: String, delta: Int) {
        if (delta == 0) return
        val cur = state.friendship[name] ?: 0
        state.friendship[name] = (cur + delta).coerceIn(-50, 100)
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** The wrap, at the credits: the record, the counters,
     *  the stars' friendship, the house's memory topic, and
     *  the ticker. Exactly once — the record is the guard. */
    fun finishFeature(state: GameState) {
        val day = state.absDay
        if (recordFor(state, day) != null) return
        val f = featureFor(state)
        val buzz = buzzNow(state)
        val tier = tierFor(buzz)
        writeRecord(state, MovieRecord(day, f.key, f.title, f.stars,
            tier, buzz))
        state.life[K_ATTENDED] = attendedCount(state) + 1
        if (attendedCount(state) == 1 ||
            buzz > (state.life[K_BEST] ?: Int.MIN_VALUE)) {
            state.life[K_BEST] = buzz
        }
        val pay = starPay(tier)
        for (star in f.stars) bumpFriendship(state, star, pay)
        for (name in attendance(state)) plantMovieTopic(name)
        note(state, "🎬 “${f.title}” played the Bijou to " +
            "${tierLabel(tier)} (buzz $buzz).")
        townLogLine(state, "🎬 Movie night: “${f.title}” — ${tierLabel(tier)}")
        state.life.remove(kSeat(day))
    }

    // ---------------- lifecycle (the Engine's rollover calls) ----------------

    /** Midnight: a session left mid-feature settles as an
     *  attended night at the buzz it had reached — the
     *  record without the credits' friendship (the stars
     *  never got their bow) — and the day's session keys
     *  are pruned, like the streetlamps' before them. Runs
     *  before the Phone in Engine.onNewDay. */
    fun dailyRollover(state: GameState) {
        val yday = state.absDay - 1
        if (yday >= 0 && (state.life[kSeat(yday)] ?: 0) == 1 &&
            recordFor(state, yday) == null) {
            val key = state.lifeText[kFeat(yday)] ?: "s${slotOf(yday)}"
            val f = featureForDay(state, yday)
            val buzz = state.life[kBuzz(yday)] ?: 0
            writeRecord(state, MovieRecord(yday, key, f.title, f.stars,
                tierFor(buzz), buzz))
            state.life[K_ATTENDED] = attendedCount(state) + 1
            if (attendedCount(state) == 1 ||
                buzz > (state.life[K_BEST] ?: Int.MIN_VALUE)) {
                state.life[K_BEST] = buzz
            }
            townLogLine(state, "🎬 Movie night: “${f.title}” — " +
                "you slipped out before the credits.")
        }
        state.life.keys.removeAll {
            (it.startsWith("movieSeat:") || it.startsWith("movieStep:") ||
                it.startsWith("movieBuzz:")) &&
                (it.substringAfter(':').toIntOrNull() ?: Int.MAX_VALUE) <
                    state.absDay
        }
        state.lifeText.keys.removeAll {
            (it.startsWith("movieFeat:") &&
                (it.substringAfter(':').toIntOrNull() ?: Int.MAX_VALUE) <
                    state.absDay) ||
                (it.startsWith("movieRec:") &&
                    (it.substringAfter(':').toIntOrNull() ?: Int.MAX_VALUE) <
                        state.absDay - 1)
        }
    }

    // ---------------- surfaces ----------------

    /** The morning report's line: tonight's announcement, or
     *  the dark reason in the report's plain words. Only on
     *  screening days — the Bijou doesn't nag all week. */
    fun morningNote(state: GameState): String? {
        if (!isScreeningDay(state.absDay)) return null
        darkLine(state)?.let { return it }
        val f = featureFor(state)
        val starsLine = f.stars.joinToString(", ")
        return "🎬 Tonight at the Bijou: “${f.title}” — a " +
            "${f.genreLabel.lowercase()} starring $starsLine. " +
            "Doors at 7, and the town already paid for the print." +
            (if (f.headline) " (The town voted for this one.)" else "")
    }

    /** The text the morning's movie text is built from — a
     *  star of tonight's feature, the natural texter, or
     *  null when there's no show to text about. */
    fun textSender(state: GameState): String? {
        if (!isScreeningDay(state.absDay) || isDarkTonight(state)) {
            return null
        }
        return featureFor(state).stars.firstOrNull()
    }

    /** The Look text at the Bijou: the marquee tonight —
     *  lit with the feature, honest when dark, and carrying
     *  the next showing when the week's shows are done. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != "Bijou") return null
        if (!isScreeningDay(state.absDay)) {
            val next = nextScreeningDay(state.absDay)
            val nf = featureForDay(state, next)
            val dayName = Jobs.WEEKDAY_NAMES[Jobs.weekdayOf(next)]
            return "🎬 The marquee is being re-lettered for $dayName: " +
                "“${nf.title}” (${nf.genreLabel})."
        }
        darkLine(state)?.let { return it }
        val f = featureFor(state)
        val starsLine = f.stars.joinToString(", ")
        return if (state.playerInside) {
            "🎬 Tonight's feature: “${f.title}” — a " +
                "${f.genreLabel.lowercase()} starring $starsLine. " +
                "The screen is the big silver rectangle. Tap it " +
                "when you're ready to take your seat."
        } else {
            "🎬 The marquee is lit: TONIGHT — “${f.title}” " +
                "(${f.genreLabel}), starring $starsLine. Doors at 7; " +
                "admission is the town's treat."
        }
    }

    /** The 🎬 MOVIE NIGHTS Journal page, assembled pure like
     *  the other boards: tonight, your houses, the features
     *  you've seen with their stars. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val attended = attendedCount(state)
        out += "🎬 MOVIE NIGHTS — $attended screening" +
            (if (attended == 1) "" else "s") + " attended"
        out += "Friday is premiere night — a feature the town made " +
            "itself. Saturday, the matinee the town voted on " +
            "headlines when the vote stands. Doors at 7, last " +
            "seating at 8, and admission is free: City Hall's arts " +
            "line bought the print, so your ticket is showing up."
        when {
            !isScreeningDay(state.absDay) -> {
                val next = nextScreeningDay(state.absDay)
                val nf = featureForDay(state, next)
                out += "🎞 Next: ${Jobs.WEEKDAY_NAMES[Jobs.weekdayOf(next)]} — " +
                    "“${nf.title}” (${nf.genreLabel}), starring " +
                    nf.stars.joinToString(", ") + "."
            }
            isDarkTonight(state) ->
                out += "🎞 Tonight: dark. ${darkLine(state)?.removePrefix("🎬 ")}"
            else -> {
                val f = featureFor(state)
                out += "🎞 Tonight: “${f.title}” (${f.genreLabel}), " +
                    "starring ${f.stars.joinToString(", ")}" +
                    (if (f.headline) " — the town's own pick, headlining."
                    else ".")
                val house = attendance(state)
                out += "The house: ${house.size} expected — " +
                    house.joinToString(", ") + "."
            }
        }
        bestBuzz(state)?.let { best ->
            out += "🏆 Your best house: ${tierLabel(tierFor(best))} " +
                "(buzz $best)."
        }
        if (featDone(state)) {
            out += "⭐ $FEAT_NAME — you once gave this town a " +
                "legendary house. The usher still tells people."
        } else {
            out += "Land every reaction — the right one, three " +
                "times running — and the house goes legendary. " +
                "That's the feat: $FEAT_NAME."
        }
        val hist = history(state)
        if (hist.isNotEmpty()) {
            out += "📽 SEEN AT THE BIJOU:"
            for (r in hist) {
                out += "· “${r.title}” — starring " +
                    r.stars.joinToString(", ") + "; " +
                    tierLabel(r.tier) + " (buzz ${r.buzz}), " +
                    "day ${r.day + 1}."
            }
        } else {
            out += "No features seen yet. The screen is patient. " +
                "Towelie means to come every week and never has; " +
                "Ms. Crabtree has seen enough movies driving the bus."
        }
        return out
    }
}

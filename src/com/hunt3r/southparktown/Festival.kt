package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// The Bijou Film Festival — v0.14.0c (Queue #14 item 3):
// one week a year, the Bijou stops showing the town's
// Friday/Saturday features and runs a FESTIVAL instead —
// five wholly original town prints, one a night, Monday
// through Friday of the December week that holds the 8th,
// before a three-person jury and whoever the room draws in.
//
// The anchor is the quiet pocket the calendar sweep
// proves empty 2026–2100: the Monday of the week
// containing December 8 (the Monday falls Dec 2–8, the
// Friday Dec 6–12) — clear of the fall finale and Black
// Friday (both done by Nov 30), clear of Christmas (Dec
// 20), and months from everything spring owns. Runtime
// deference is MovieNights' own: a night a case climax
// owns is dark with the case's name on it (darkReason
// learned DARK_FESTIVAL, ranked last — the festival
// yields to case, finale and holiday like everything
// else at this theater), that night yields, and the
// week plays on around it.
//
// The films are invented whole — titles, premises,
// program notes, scenes — and denylist-scanned against
// real pictures and franchises in the suite, the Pocket
// Pals / hero-nights discipline. The slate's ORDER is a
// seeded (year) shuffle: stable within a year, different
// across years. Stars are drawn the way MovieNights
// draws its features — from KID_ORDER, the never-come
// never star — seeded by (year, film), so a film's cast
// is the same on its poster, its night and its record.
//
// The jury is the Yearbook's pattern grown up: a plain
// rotation over an authored pool — nine townsfolk in
// three leaning trios (craft / heart / weird), one trio
// a year, consecutive years sharing NO juror. A film's
// festival score is DERIVED, never rolled at the wrap:
// its authored qualities (craft/heart/weird, weighted
// 4/4/2) + each juror's leaning dimension read at face
// value + the audience's answer (twice the night's buzz
// — the buzz YOU made if you sat the night, the house's
// own seeded buzz if you didn't). The Golden Popcorn
// goes to the derived winner; ties fall down a stated
// ladder — audience, then heart, then the title A–Z.
//
// The week rides MovieNights' machinery wherever the
// machinery reaches: the session is the feature's own
// eleven beats (five cards, the room reacting, three
// prompts scored by MovieNights.scoreFor), the audience
// is MovieNights.drawOf around the night's stars under
// MovieNights' house cap, the doors are MovieNights'
// doors, and admission is MovieNights' position — free:
// City Hall's arts line bought the festival prints the
// same way it buys the Friday ones, so the ticket is
// showing up. The one thing the week adds of its own is
// an agenda claim, seated TWENTIETH (after the field
// trips, before sleepovers): Mon–Thu there is no
// screening claim to ride, and a festival house is a
// gathering like any other.
//
// Records follow the house discipline: the paper gives
// the festival TOWN NEWS lines (the announcement in its
// week, the winner in the closing week's edition) and
// NO headline tier — the ladder stands at ten, and a
// festival, however golden, is town news like the dance,
// the show and the parade before it. The winner's stars
// take +1 friendship ONLY if you sat closing night (the
// played-through rule), paid once at the wrap. One group
// photo may be minted at closing night, in the same
// pattern. The phone carries one morning-after text
// under the FESTIVAL trigger.
//
// State: no new save keys — the year's record rides
// lifeText in Talent's pipe format, sessions ride
// life/lifeText day keys pruned at rollover, and every
// read is clamped against the banks below, so a hostile
// doc can't mint a winner the arithmetic didn't earn.
// ============================================================

object FestivalCalendar {

    /** [year]'s festival Monday: the Monday of the week
     *  containing December 8. Always Dec 2–8; the Friday
     *  always falls Dec 6–12. Pure calendar math over
     *  FairCalendar's ordinals — the one time space every
     *  anchor in town shares. */
    fun mondayOrdinal(year: Int): Int {
        val dec8 = ordinalOfKey("%04d1208".format(year))
        val dow = FairCalendar.dowOfOrdinal(dec8)   // 0 = Sun … 6 = Sat
        return dec8 - (dow + 6) % 7                  // back to Monday
    }

    /** The ordinal of festival night [night] (0 = Monday
     *  … 4 = Friday, closing night). */
    fun nightOrdinal(year: Int, night: Int): Int =
        mondayOrdinal(year) + night

    fun nightDay(year: Int, night: Int): Int =
        FairCalendar.absDayOfOrdinal(nightOrdinal(year, night))

    /** The poster morning: a week ahead of the Monday,
     *  like the dance, the show and the trips. */
    fun announceOrdinal(year: Int): Int = mondayOrdinal(year) - 7

    fun announceDay(year: Int): Int =
        FairCalendar.absDayOfOrdinal(announceOrdinal(year))

    /** The festival whose week (Mon–Fri) contains
     *  [absDay], as (year, nightIndex), or null. */
    fun festivalAt(absDay: Int): Pair<Int, Int>? {
        val ord = FairCalendar.ordinalOfAbsDay(absDay)
        val y = keyFromOrdinal(ord).substring(0, 4)
            .toIntOrNull() ?: return null
        for (yy in (y - 1)..(y + 1)) {
            val mon = mondayOrdinal(yy)
            if (ord in mon..(mon + 4)) return yy to (ord - mon)
        }
        return null
    }

    /** The next festival whose closing night hasn't
     *  passed as of [absDay] — the one the poster and
     *  the page count down to. */
    fun nextYearAfter(absDay: Int): Int {
        val ord = FairCalendar.ordinalOfAbsDay(absDay)
        val y = keyFromOrdinal(ord).substring(0, 4)
            .toIntOrNull() ?: 2026
        for (yy in (y - 1)..(y + 2)) {
            if (nightOrdinal(yy, Festival.NIGHTS - 1) >= ord)
                return yy
        }
        return y + 2
    }
}

object Festival {

    // ---------------- the dials (all suite-pinned) ----------------

    const val THEATER = "Bijou"
    const val NAME = "The Bijou Film Festival"
    const val AWARD = "the Golden Popcorn"
    const val TOPIC = "film festival"

    /** Screening nights: Monday through Friday. */
    const val NIGHTS = 5

    /** The poster goes up this many days before the
     *  Monday (the town's standing announcement lead). */
    const val ANN_BACK = 7

    /** The doors are MovieNights' doors — the festival
     *  keeps the theater's own hours, by reference. */
    val DOORS_FROM: Int get() = MovieNights.DOORS_FROM
    val LAST_SEATING: Int get() = MovieNights.LAST_SEATING
    val GATHER_TO: Int get() = MovieNights.GATHER_TO

    /** Admission is MovieNights' position, held: the
     *  arts line bought the prints; showing up is the
     *  ticket. There is no festival price to re-type. */
    const val ADMISSION_FREE = true

    /** Score weights: qualities are authored 1–10; the
     *  jury reads its leaning dimension at face value;
     *  the audience answers at twice the night's buzz. */
    const val W_CRAFT = 4
    const val W_HEART = 4
    const val W_WEIRD = 2
    const val JURY_PER_LEAN = 1
    const val AUD_PER_BUZZ = 2

    /** Friendship the winning film's stars take home —
     *  paid once, at the wrap, and only to a player who
     *  sat closing night (the played-through rule). */
    const val WINNER_STAR_PAY = 1

    /** Past festivals the Journal page keeps whole. */
    const val HISTORY_CAP = 8

    /** Days after closing the wrap can still land (a
     *  save that sleeps through the ceremony morning
     *  gets its wrap at the first morning it wakes). */
    const val WRAP_TAIL = 30

    // ---------------- the calendar, in state terms ----------------

    /** Tonight is a festival night (pure calendar — the
     *  hook MovieNights' deference reads, so it must
     *  never depend on anything but the day itself). */
    fun isFestivalDay(absDay: Int): Boolean =
        FestivalCalendar.festivalAt(absDay) != null

    fun isFestivalNight(state: GameState): Boolean =
        isFestivalDay(state.absDay)

    /** (year, nightIndex) of today, or null. */
    fun tonight(state: GameState): Pair<Int, Int>? =
        FestivalCalendar.festivalAt(state.absDay)

    private fun yearsAround(state: GameState): IntRange =
        (state.gameYear - 1)..(state.gameYear + 1)

    fun dateLabel(absDay: Int): String = Dance.dateLabel(absDay)

    // ---------------- the films (invented whole) ----------------

    data class FestFilm(
        val id: String, val title: String, val genre: String,
        val note: String, val prop: String,
        val craft: Int, val heart: Int, val weird: Int,
        val openerBeat: String, val finaleBeat: String,
        val opener: String, val flop: String, val finale: String
    ) {
        fun dim(lean: String): Int = when (lean) {
            "craft" -> craft
            "heart" -> heart
            else -> weird
        }
    }

    val FILMS: List<FestFilm> = listOf(
        FestFilm("lighthouse",
            "The Last Lighthouse on Dry Land", "Drama",
            "A landlocked lighthouse keeper keeps the lamp " +
                "lit for ships that will never come — until " +
                "a kid asks the question the whole town has " +
                "been politely not asking.",
            "lamp", 8, 10, 3,
            MovieNights.BEAT_HEART, MovieNights.BEAT_HEART,
            "Dawn. A lighthouse on a hill in a town with no " +
                "sea, and Keeper {s1} polishing a lamp the " +
                "maps say shouldn't exist. The town waves up " +
                "at it anyway. It's that kind of light.",
            "The tide scene. There is no tide. {s2} performs " +
                "the tide for six minutes using a blue " +
                "bedsheet and total commitment. The bedsheet " +
                "wins an argument nobody was having.",
            "{s1} finally lets the lamp go dark for one " +
                "night — and the whole town, without a word " +
                "passing, leaves its porch lights on instead. " +
                "Every house. All night."),
        FestFilm("homework",
            "My Homework Ate the Mayor", "Comedy",
            "A civics assignment escapes its folder and " +
                "eats its way up the town org chart. Nobody " +
                "stops it. It has a hall pass.",
            "folder", 5, 6, 9,
            MovieNights.BEAT_COMEDY, MovieNights.BEAT_COMEDY,
            "{s1}'s homework folder is empty, the mayor's " +
                "schedule is missing, and somewhere in City " +
                "Hall a stapler is being digested. {s2} is " +
                "taking minutes. The minutes are screaming.",
            "The budget-meeting scene. The homework " +
                "presents a pie chart. The pie chart is an " +
                "actual pie. The council eats the evidence " +
                "and calls it bipartisanship.",
            "Graduation day for one sheet of loose-leaf: " +
                "{s1} turns it in — completed, correct, and " +
                "only slightly mayor-flavored. It gets an A " +
                "and a formal apology from the town."),
        FestFilm("frequency",
            "The Night Frequency", "Ghost Story",
            "A late-night radio DJ takes a call from " +
                "tomorrow night's show — same caller, same " +
                "voice, one day early.",
            "microphone", 9, 5, 8,
            MovieNights.BEAT_SCARE, MovieNights.BEAT_SPECTACLE,
            "2 AM. One microphone, one county of " +
                "insomniacs. DJ {s1} takes a call — and the " +
                "caller, calm as weather, describes tonight's " +
                "show. Tomorrow's tonight.",
            "The static scene. Four minutes of genuine " +
                "static while {s2} holds an antenna made of " +
                "a coat hanger and pure faith. Something in " +
                "the static almost says a name. Almost.",
            "{s1} answers the last call on air and asks " +
                "the only question left. The answer is the " +
                "station's own sign-on — from forty years " +
                "ago, in {s1}'s own voice."),
        FestFilm("goat",
            "Goat vs. Mountain", "Sports Drama",
            "A goat and the most stubborn kid in fourth " +
                "grade race to the top of the town's " +
                "mountain. The goat has trained for this. " +
                "The kid has snacks.",
            "flag", 6, 9, 6,
            MovieNights.BEAT_COMEDY, MovieNights.BEAT_HEART,
            "The mountain has stood over the town forever, " +
                "unclimbed by goat or grade-schooler. {s1} " +
                "laces up. The goat, unimpressed, does not. " +
                "It was born ready.",
            "The training montage. The goat trains by " +
                "standing on things — a stump, a fence, a " +
                "parked car, the principal's patience. {s2} " +
                "narrates it like a title fight. It is not " +
                "a title fight.",
            "Above the clouds: one flag, two climbers, " +
                "and a photo finish the town will argue " +
                "about forever. {s1} shares the summit " +
                "snacks. The goat shares nothing. The goat " +
                "is the champion of the mountain, and of " +
                "the moment."),
        FestFilm("cartographer",
            "The Cartographer's Daughter", "Adventure",
            "A mapmaker's kid redraws the town map to fix " +
                "one wrong street — and the streets start " +
                "politely moving to match.",
            "compass", 10, 7, 7,
            MovieNights.BEAT_SPECTACLE, MovieNights.BEAT_HEART,
            "{s1} inherits two things: a brass compass " +
                "and a map of this town with one street " +
                "drawn wrong on purpose. Correcting it " +
                "takes one pencil line. The town notices " +
                "by morning.",
            "The legend scene. {s2} reads the map's " +
                "legend aloud, alphabetically, including " +
                "'scale: one inch equals one inch — do " +
                "not test.' The compass files a complaint.",
            "Every moved street holds a small parade of " +
                "lost mail, lost dogs and lost arguments " +
                "coming home at last. {s1} draws the last " +
                "line: the way back. It was the way back " +
                "all along.")
    )

    fun filmById(id: String?): FestFilm? =
        FILMS.firstOrNull { it.id == id }

    /** Copy the festival must never carry: real pictures,
     *  real franchises, real festivals — the denylist the
     *  suite scans every authored string against. */
    val DENYLIST: List<String> = listOf(
        "star wars", "star trek", "harry potter", "marvel",
        "avengers", "spider-man", "spiderman", "batman",
        "superman", "pokemon", "disney", "pixar", "frozen",
        "shrek", "minion", "jurassic", "transformers",
        "ghibli", "mario", "zelda", "ghostbusters",
        "titanic", "jaws", "terminator", "top gun",
        "barbie", "oppenheimer", "avatar", "encanto",
        "moana", "zootopia", "inside out", "cannes",
        "sundance", "terrance", "phillip", "coco")

    /** This year's slate, in the order the nights play
     *  it: a seeded shuffle of the five prints — stable
     *  within the year, different across years. */
    fun slate(year: Int): List<FestFilm> =
        FILMS.shuffled(Random(year * 1_000_003L + 77L))

    /** The film on festival night [night] of [year]. */
    fun filmFor(year: Int, night: Int): FestFilm =
        slate(year)[night.coerceIn(0, NIGHTS - 1)]

    /** A festival film's stars, drawn the way
     *  MovieNights draws its features: KID_ORDER minus
     *  the never-come, seeded — here by (year, film), so
     *  the cast is stable from poster to record. */
    fun starsFor(year: Int, film: FestFilm): List<String> {
        val rnd = Random(year * 31_337L +
            film.id.hashCode().toLong())
        val nStars = 2 + rnd.nextInt(3)
        return KID_ORDER
            .filter { it !in MovieNights.NEVER_ATTENDS }
            .shuffled(rnd).take(nStars)
    }

    // ---------------- the jury (the Yearbook pattern) ----------------

    data class Juror(val name: String, val lean: String,
                     val taste: String)

    /** The pool, in leaning trios (craft / heart /
     *  weird): nine townsfolk who take the festival
     *  seriously in three different directions. */
    val JURY_POOL: List<Juror> = listOf(
        Juror("Victoria", "craft",
            "judges a film the way she runs a school: " +
                "structure first, mercy scheduled."),
        Juror("Chef", "heart",
            "votes for whichever film made him feel " +
                "something before the second reel."),
        Juror("Mephesto", "weird",
            "rewards the picture that took the strangest " +
                "risk and defended it in public."),
        Juror("Alexis", "craft",
            "keeps minutes on every plot hole. Every " +
                "one. Alphabetized."),
        Juror("Pusslicker", "heart",
            "holds the old anchor's rule: if the room " +
                "felt it, the film did its job."),
        Juror("Skeeter", "weird",
            "has seen every picture ever shown at the " +
                "drive-in he swears used to exist."),
        Juror("Lu Kim", "craft",
            "rates a production by its discipline: a " +
                "tight film is a fed film."),
        Juror("Big Gay Al", "heart",
            "believes every film is secretly a pageant " +
                "and scores the heart first."),
        Juror("Dr. Doctor", "weird",
            "diagnoses films. The healthy ones bore him."))

    /** [year]'s jury: one whole trio, rotating by year —
     *  the Yearbook's plain-rotation pattern over the
     *  pool, so consecutive years share no juror at all. */
    fun juryFor(year: Int): List<Juror> {
        val trio = ((year % 3) + 3) % 3
        return (0..2).map { JURY_POOL[trio * 3 + it] }
    }

    // ---------------- scoring (derived, recomputed) ----------------

    fun qualityOf(film: FestFilm): Int =
        film.craft * W_CRAFT + film.heart * W_HEART +
            film.weird * W_WEIRD

    fun juryPoints(film: FestFilm, jurors: List<Juror>): Int =
        jurors.sumOf { film.dim(it.lean) } * JURY_PER_LEAN

    fun audiencePoints(buzz: Int): Int =
        buzz.coerceIn(-3, 6) * AUD_PER_BUZZ

    /** The house's own buzz for a night nobody sat: the
     *  room still watched the film, and its answer is a
     *  pure function of (year, film) — the same number
     *  on the poster, the page and the recompute. */
    fun houseBuzz(year: Int, film: FestFilm): Int =
        Random(year * 1_000_003L +
            film.id.hashCode().toLong() * 131L + 7L)
            .nextInt(0, 7)

    data class ScoreCard(val film: FestFilm, val quality: Int,
                         val jury: Int, val audience: Int) {
        val total: Int get() = quality + jury + audience
    }

    fun scoreCard(year: Int, film: FestFilm, buzz: Int): ScoreCard =
        ScoreCard(film, qualityOf(film),
            juryPoints(film, juryFor(year)), audiencePoints(buzz))

    /** The Golden Popcorn, by the stated ladder: total,
     *  then the audience's answer, then heart, then the
     *  title A–Z. A pure function of the cards — the
     *  suite drives ties at every level of it. */
    fun pickWinner(cards: List<ScoreCard>): ScoreCard? =
        cards.maxWithOrNull(
            compareBy<ScoreCard> { it.total }
                .thenBy { it.audience }
                .thenBy { it.film.heart }
                .thenByDescending { it.film.title })

    // ---------------- the year's record ----------------

    data class FestRecord(
        val year: Int,
        val attended: Map<Int, Int> = emptyMap(),  // night -> your buzz
        val yielded: Set<Int> = emptySet(),        // nights the town owned
        val settled: Set<Int> = emptySet(),        // nights fully accounted
        val wrapped: Boolean = false,
        val wrapDay: Int = -1,
        val paid: Boolean = false,
        val photoId: String = ""
    )

    private fun recKey(year: Int) = "festRec:$year"
    private const val K_YEARS = "festYears"        // lifeText: "2026,2027"

    fun record(state: GameState, year: Int): FestRecord? {
        val raw = state.lifeText[recKey(year)] ?: return null
        val p = raw.split("|")
        if (p.size < 2 || p[0].toIntOrNull() != year) return null
        var att = mapOf<Int, Int>()
        var yld = setOf<Int>()
        var set = setOf<Int>()
        var wrapped = false
        var wrapDay = -1
        var paid = false
        var photo = ""
        for (pair in p.drop(1)) {
            val kv = pair.split("=", limit = 2)
            if (kv.size != 2) continue
            when (kv[0]) {
                "att" -> att = kv[1].split(",")
                    .mapNotNull { e ->
                        val b = e.split(":")
                        val n = b.getOrNull(0)?.toIntOrNull()
                        val z = b.getOrNull(1)?.toIntOrNull()
                        if (n != null && z != null &&
                            n in 0 until NIGHTS && z in -9..99)
                            n to z else null
                    }.toMap()
                "yield" -> yld = kv[1].split(",")
                    .mapNotNull { it.toIntOrNull() }
                    .filter { it in 0 until NIGHTS }.toSet()
                "settled" -> set = kv[1].split(",")
                    .mapNotNull { it.toIntOrNull() }
                    .filter { it in 0 until NIGHTS }.toSet()
                "wrapped" -> wrapped = kv[1] == "1"
                "wrapDay" -> wrapDay =
                    kv[1].toIntOrNull() ?: -1
                "paid" -> paid = kv[1] == "1"
                "photo" -> photo = kv[1]
            }
        }
        return FestRecord(year, att, yld, set, wrapped,
            wrapDay, paid, photo)
    }

    fun saveRecord(state: GameState, r: FestRecord) {
        val att = r.attended.entries.sortedBy { it.key }
            .joinToString(",") { "${it.key}:${it.value}" }
        val yld = r.yielded.sorted().joinToString(",")
        val set = r.settled.sorted().joinToString(",")
        state.lifeText[recKey(r.year)] =
            "${r.year}|att=$att|yield=$yld|settled=$set" +
                "|wrapped=${if (r.wrapped) 1 else 0}" +
                "|wrapDay=${r.wrapDay}" +
                "|paid=${if (r.paid) 1 else 0}" +
                "|photo=${r.photoId}"
        val years = (recordYears(state) + r.year).distinct()
            .sorted()
        state.lifeText[K_YEARS] = years.joinToString(",")
    }

    private fun recordOrNew(state: GameState,
                            year: Int): FestRecord =
        record(state, year) ?: FestRecord(year)

    fun recordYears(state: GameState): List<Int> =
        (state.lifeText[K_YEARS] ?: "")
            .split(",").mapNotNull { it.toIntOrNull() }

    fun allRecords(state: GameState): List<FestRecord> =
        recordYears(state).mapNotNull { record(state, it) }

    /** The buzz a night counts for scoring: yours if
     *  you sat it, the house's own if you didn't. */
    fun buzzFor(state: GameState, year: Int,
                night: Int): Int {
        val rec = record(state, year)
        rec?.attended?.get(night)?.let { return it }
        return houseBuzz(year, filmFor(year, night))
    }

    /** Every night's card for [year], in slate order. */
    fun scoreCards(state: GameState,
                   year: Int): List<ScoreCard> =
        (0 until NIGHTS).map { n ->
            scoreCard(year, filmFor(year, n),
                buzzFor(state, year, n))
        }

    fun winnerCard(state: GameState,
                   year: Int): ScoreCard? =
        pickWinner(scoreCards(state, year))

    fun winnerFilm(state: GameState, year: Int): FestFilm? =
        winnerCard(state, year)?.film

    /** The night a film played, by title of the record —
     *  for the paper's and the page's winner lines. */
    fun winnerNight(state: GameState, year: Int): Int {
        val w = winnerFilm(state, year) ?: return -1
        return slate(year).indexOfFirst { it.id == w.id }
    }

    // ---------------- deference (MovieNights' own) ----------------

    /** Why tonight's festival is dark, or null if the
     *  show goes on: MovieNights' ladder, read for the
     *  festival — anything it names except the festival
     *  itself owns the night (a case climax, a finale
     *  evening, a holiday with its own beat). */
    fun nightDarkReason(state: GameState): String? {
        val r = MovieNights.darkReason(state) ?: return null
        if (r == MovieNights.DARK_FESTIVAL) return null
        // the Dance/FieldTrip pattern: the fact is
        // recorded where it's learned — the first
        // consult on the day that finds an owner stamps
        // the reason, so the morning settle can say why
        // even after the owner's own state has moved on.
        val t = tonight(state)
        if (t != null) {
            val k = "festYield:${t.first}:${t.second}"
            if (state.lifeText[k] == null) {
                state.lifeText[k] = r
            }
        }
        return r
    }

    fun nightDark(state: GameState): Boolean =
        nightDarkReason(state) != null

    /** The honest words for a dark festival night. */
    fun darkText(state: GameState): String =
        when (nightDarkReason(state)) {
            MovieNights.DARK_CASE ->
                "the Bijou's booked solid — town business"
            MovieNights.DARK_FINALE ->
                "the whole town's at the season finale"
            MovieNights.DARK_HOLIDAY -> {
                val name = state.holidayId?.let { id ->
                    HOLIDAYS.firstOrNull { it.id == id }?.name
                } ?: "the holiday"
                "the Bijou closes for $name"
            }
            else -> "the marquee is dark"
        }

    // ---------------- the audience ----------------

    private data class AttKey(val year: Int, val night: Int,
                              val bondsHash: Int)
    private var attMemo: Pair<AttKey, List<String>>? = null

    /** A festival night's house, in seating order: the
     *  film's stars first, then the drawn — friends,
     *  hecklers, regulars — by MovieNights.drawOf,
     *  capped at MovieNights' house cap. Deterministic
     *  in the state; memoized like the screenings'. */
    fun attendance(state: GameState, year: Int,
                   night: Int): List<String> {
        val film = filmFor(year, night)
        val stars = starsFor(year, film)
        val key = AttKey(year, night, state.npcBonds.hashCode())
        attMemo?.let { (k, v) -> if (k == key) return v }
        val slot = year * 10 + night
        val drawn = KID_ORDER
            .filter { it !in stars }
            .map { it to MovieNights.drawOf(state, it, stars, slot) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<String, Int>> {
                it.second }.thenBy { it.first })
            .map { it.first }
        val house = (stars + drawn).take(MovieNights.HOUSE_CAP)
        attMemo = key to house
        return house
    }

    /** The Engine's gathering hook, seated TWENTIETH —
     *  after the field trips, before sleepovers: on a
     *  live festival night the house is at the Bijou
     *  from doors until the feature lets out. The lite
     *  deference split is MovieNights' own: the full
     *  climax scan is for the player-facing surfaces;
     *  here, a finale evening or a holiday stands the
     *  claim down, and the session's own gate keeps the
     *  rest honest. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in DOORS_FROM until GATHER_TO) return null
        val (year, night) = tonight(state) ?: return null
        if (npcName !in attendance(state, year, night)) return null
        if (Finales.windowAt(state) != null) return null
        val hid = state.holidayId
        if (hid != null && HolidayBeats.beatFor(hid) != null) {
            return null
        }
        return AgendaBlock(THEATER,
            "Film festival night at the Bijou — in their seat " +
                "for “${filmFor(year, night).title}.”")
    }

    // ---------------- the session (the feature's own beats) ----------------

    private fun kSeat(day: Int) = "festSeat:$day"
    private fun kStep(day: Int) = "festStep:$day"
    private fun kBuzz(day: Int) = "festBuzz:$day"
    private fun kFilm(day: Int) = "festFilm:$day"

    fun isSeated(state: GameState): Boolean =
        (state.life[kSeat(state.absDay)] ?: 0) == 1 &&
            !nightSeen(state)

    /** Tonight's film has rolled its credits for you:
     *  the night stands in the year's attended map. */
    fun nightSeen(state: GameState): Boolean {
        val (year, night) = tonight(state) ?: return false
        return record(state, year)?.attended
            ?.containsKey(night) == true
    }

    private fun fill(template: String, film: FestFilm,
                     stars: List<String>): String =
        template
            .replace("{s1}", stars.getOrNull(0) ?: "someone")
            .replace("{s2}", stars.getOrNull(1)
                ?: stars.getOrNull(0) ?: "someone")
            .replace("{s3}", stars.getOrNull(2)
                ?: stars.getOrNull(1)
                ?: stars.getOrNull(0) ?: "someone")
            .replace("{noun}", film.prop)

    /** Tonight's five cards, assembled the way
     *  MovieNights assembles a feature: the film's own
     *  opener and finale, its flop at the rotating dud
     *  seat, and two shared middles between them. */
    fun cardsFor(year: Int, night: Int): List<MovieNights.Card> {
        val film = filmFor(year, night)
        val stars = starsFor(year, film)
        val rnd = Random(year * 77_777L + night * 131L + 7L)
        val midIdx = MovieNights.MIDDLES.indices
            .shuffled(rnd).take(2)
        val dudAt = 1 + ((year + night) % 3)
        val cards = arrayOfNulls<MovieNights.Card>(5)
        cards[0] = MovieNights.Card(film.openerBeat,
            fill(film.opener, film, stars))
        cards[4] = MovieNights.Card(film.finaleBeat,
            fill(film.finale, film, stars))
        cards[dudAt] = MovieNights.Card(MovieNights.BEAT_DUD,
            fill(film.flop, film, stars))
        var mi = 0
        for (i in 1..3) {
            if (cards[i] != null) continue
            val (beat, text) = MovieNights.MIDDLES[midIdx[mi++]]
            cards[i] = MovieNights.Card(beat,
                fill(text, film, stars))
        }
        return cards.map { it!! }
    }

    /** The feature as MovieNights' reaction machinery
     *  wants it — the room's lines are composed from who
     *  is actually in it, around tonight's stars. */
    fun asFeature(year: Int, night: Int): MovieNights.Feature {
        val film = filmFor(year, night)
        return MovieNights.Feature(
            key = "fest$year:$night", title = film.title,
            genreId = "festival", genreLabel = film.genre,
            stars = starsFor(year, film), headline = false,
            cards = cardsFor(year, night))
    }

    // The evening's running order — the feature's own:
    // five cards, the room in the two early gaps, a
    // prompt after cards 1, 3 and 4, then the credits.
    private val BEAT_KINDS = listOf(
        "card", "reaction", "card", "prompt",
        "card", "reaction", "card", "prompt",
        "card", "prompt", "credits")
    private val BEAT_CARDS = listOf(
        0, 0, 1, 1, 2, 2, 3, 3, 4, 4, -1)

    const val ST_PROGRAM = "program"
    const val ST_DARK = "dark"
    const val ST_BEFORE = "before"
    const val ST_SEATING = "seating"
    const val ST_RESUME = "resume"
    const val ST_LATE = "late"
    const val ST_DONE = "done"
    const val ST_AWARD = "award"
    const val ST_CLOSED = "closed"

    data class Status(val kind: String, val text: String)

    /** The screen is the festival's on its five days —
     *  the poster week keeps the regular marquee (the
     *  program rides the Look text, the paper and the
     *  Journal there), so no screening loses its rope
     *  to a poster. */
    fun screenOpen(state: GameState): Boolean =
        isFestivalDay(state.absDay)

    private fun programText(state: GameState,
                            year: Int): String {
        val mon = FestivalCalendar.nightDay(year, 0)
        val fri = FestivalCalendar.nightDay(year, NIGHTS - 1)
        val sb = StringBuilder()
        sb.append("🎬 $NAME — five nights at the Bijou, " +
            "${dateLabel(mon)}–${dateLabel(fri)}. Admission " +
            "is the town's treat: showing up is the ticket.\n")
        for (n in 0 until NIGHTS) {
            val f = filmFor(year, n)
            sb.append("· ${Jobs.WEEKDAY_NAMES[
                Jobs.weekdayOf(FestivalCalendar
                    .nightDay(year, n))]}: “${f.title}” " +
                "(${f.genre}), starring " +
                starsFor(year, f).joinToString(", ") + ".\n")
        }
        sb.append("The jury: " +
            juryFor(year).joinToString(", ") { it.name } +
            ". Their cards stay sealed until closing night, " +
            "when $AWARD is awarded.")
        return sb.toString()
    }

    /** Where the screen stands for a player looking at
     *  it — the festival dialog's whole script in one
     *  reading. */
    fun sessionStatus(state: GameState): Status {
        val t = tonight(state)
        if (t == null) {
            val y = FestivalCalendar.nextYearAfter(state.absDay)
            return Status(ST_PROGRAM, programText(state, y))
        }
        val (year, night) = t
        val film = filmFor(year, night)
        val starsLine = starsFor(year, film).joinToString(", ")
        if (nightSeen(state)) {
            if (night == NIGHTS - 1) {
                val rec = record(state, year)
                val w = winnerFilm(state, year)
                val awardLine = if (w != null)
                    "\n🏆 $AWARD went to “${w.title}” — the " +
                        "jury's cards are read, the room has " +
                        "its answer."
                else ""
                return if (rec != null && rec.photoId.isEmpty() &&
                    state.timeMin < 22 * 60) {
                    Status(ST_AWARD,
                        "🎬 Closing night is in the books: " +
                            "“${film.title}” has rolled its " +
                            "credits for you.$awardLine\nThe " +
                            "house is still here, lit and " +
                            "buzzing — there's time for one " +
                            "festival photograph.")
                } else {
                    Status(ST_CLOSED,
                        "🎬 Closing night is in the books: " +
                            "“${film.title}” has rolled its " +
                            "credits for you.$awardLine")
                }
            }
            return Status(ST_DONE,
                "🎬 Tonight's festival film — “${film.title}” — " +
                    "has rolled its credits for you. It's on " +
                    "your 🎬 FILM FESTIVAL page.")
        }
        if (isSeated(state)) {
            return Status(ST_RESUME,
                "Your seat is still warm. The festival film " +
                    "is waiting where you left it.")
        }
        if (nightDark(state)) {
            return Status(ST_DARK,
                "🎬 Festival night ${night + 1} is dark " +
                    "tonight — ${darkText(state)}. The week " +
                    "keeps its other nights; the jury scores " +
                    "this one on the house's word alone.")
        }
        return when {
            state.timeMin < DOORS_FROM -> Status(ST_BEFORE,
                "🎬 Festival night ${night + 1} of $NIGHTS: " +
                    "“${film.title}” — a ${film.genre.lowercase()} " +
                    "starring $starsLine.\n${film.note}\nDoors " +
                    "at 7. Admission is the town's treat.")
            state.timeMin <= LAST_SEATING -> Status(ST_SEATING,
                "🎬 Festival night ${night + 1} of $NIGHTS: " +
                    "“${film.title}” — a ${film.genre.lowercase()} " +
                    "starring $starsLine.\nThe house is filling. " +
                    "Your seat is free — the town already paid " +
                    "for the print.")
            else -> Status(ST_LATE,
                "The festival film — “${film.title}” — is " +
                    "already rolling, and the usher's rope is " +
                    "up. The jury will score this one on the " +
                    "house's word alone.")
        }
    }

    /** Take the seat. True only on a live festival
     *  night, inside the doors window, before the
     *  credits have rolled — and it costs nothing, by
     *  the theater's standing design. */
    fun seat(state: GameState): Boolean {
        val (year, night) = tonight(state) ?: return false
        if (nightDark(state)) return false
        if (nightSeen(state)) return false
        if (state.timeMin !in DOORS_FROM..LAST_SEATING) {
            return false
        }
        if ((state.life[kSeat(state.absDay)] ?: 0) == 1) {
            return false
        }
        val film = filmFor(year, night)
        state.life[kSeat(state.absDay)] = 1
        state.life[kStep(state.absDay)] = 0
        state.life[kBuzz(state.absDay)] = 0
        state.lifeText[kFilm(state.absDay)] = film.id
        return true
    }

    data class Beat(val kind: String, val text: String,
                    val cardIndex: Int = -1)

    /** The beat on screen right now for a seated player —
     *  or null when there's no live session. Reaction
     *  text is MovieNights' own, composed from the room
     *  as it actually stands. */
    fun sessionBeat(state: GameState): Beat? {
        if (!isSeated(state)) return null
        val (year, night) = tonight(state) ?: return null
        val step = (state.life[kStep(state.absDay)] ?: 0)
            .coerceIn(0, BEAT_KINDS.size - 1)
        val kind = BEAT_KINDS[step]
        val cardIdx = BEAT_CARDS[step]
        val feature = asFeature(year, night)
        return when (kind) {
            "card" -> Beat("card",
                "🎞 ${cardIdx + 1} — ${feature.cards[cardIdx].text}",
                cardIdx)
            "reaction" -> Beat("reaction",
                MovieNights.reactionLine(state, feature, cardIdx),
                cardIdx)
            "prompt" -> Beat("prompt",
                "The card lands. The room holds its breath — " +
                    "and looks, somehow, at you.", cardIdx)
            else -> Beat("credits",
                creditsText(state, year, night), cardIdx)
        }
    }

    fun buzzNow(state: GameState): Int =
        state.life[kBuzz(state.absDay)] ?: 0

    /** Answer the current prompt with [reactionId] —
     *  scored by MovieNights' own table. Returns the
     *  room's response, or null if no prompt is up. */
    fun react(state: GameState, reactionId: String): String? {
        if (!isSeated(state)) return null
        if (reactionId !in MovieNights.REACTIONS
                .map { it.first }) return null
        val (year, night) = tonight(state) ?: return null
        val step = (state.life[kStep(state.absDay)] ?: 0)
            .coerceIn(0, BEAT_KINDS.size - 1)
        if (BEAT_KINDS[step] != "prompt") return null
        val feature = asFeature(year, night)
        val card = feature.cards[BEAT_CARDS[step]]
        val score = MovieNights.scoreFor(card.beat, reactionId)
        state.life[kBuzz(state.absDay)] =
            (state.life[kBuzz(state.absDay)] ?: 0) + score
        state.life[kStep(state.absDay)] = step + 1
        return reactionOutcomeText(score, card.beat, reactionId)
    }

    /** The room's answer to a reaction choice — the
     *  screening room's own words, shared with the
     *  Friday premieres (the room is the same room). */
    private fun reactionOutcomeText(score: Int, beat: String,
                                    reaction: String): String =
        when {
            reaction == MovieNights.REACT_HECKLE &&
                beat == MovieNights.BEAT_DUD ->
                "The heckle lands and the whole row loses " +
                    "it. Even the usher nods. Especially the " +
                    "usher. (+$score buzz)"
            reaction == MovieNights.REACT_HECKLE ->
                "You heckle straight into the scene's held " +
                    "breath. The row in front turns around. " +
                    "All of them. Slowly. ($score buzz)"
            score >= MovieNights.SCORE_RIGHT ->
                "The room goes with you like you cued it. " +
                    "Perfect timing — the strangers in the " +
                    "next row think you planned it. (+$score buzz)"
            score == MovieNights.SCORE_CLOSE ->
                "A few people join in half a beat late. The " +
                    "room calls it close enough, and means it " +
                    "kindly. (+$score buzz)"
            else ->
                "Your reaction lands in total silence. One " +
                    "cough, from the very back, makes it worse. " +
                    "(+$score buzz)"
        }

    /** Move past a card, a reaction, or the credits.
     *  Past the credits is the wrap: the night lands in
     *  the year's record and the session ends. */
    fun advance(state: GameState) {
        if (!isSeated(state)) return
        val step = (state.life[kStep(state.absDay)] ?: 0)
            .coerceIn(0, BEAT_KINDS.size - 1)
        if (BEAT_KINDS[step] == "credits") {
            finishNight(state)
            return
        }
        if (BEAT_KINDS[step] == "prompt") return
        state.life[kStep(state.absDay)] = step + 1
    }

    private fun creditsText(state: GameState, year: Int,
                            night: Int): String {
        val film = filmFor(year, night)
        val buzz = buzzNow(state)
        val tier = MovieNights.tierFor(buzz)
        val starsLine = starsFor(year, film).joinToString(", ")
        val tierLine = when (tier) {
            MovieNights.TIER_LEGENDARY ->
                "The house BUZZED off the scale — people " +
                    "are still arguing about the flop scene " +
                    "in the lobby, fondly, at volume."
            MovieNights.TIER_BUZZING ->
                "The house buzzed — a real roar at the " +
                    "finale, and the flop scene got the " +
                    "laugh it was owed."
            MovieNights.TIER_POLITE ->
                "A polite house: kind claps, warm seats, " +
                    "home by ten. The jury scribbles anyway."
            else ->
                "The house sat on its hands. The projector's " +
                    "applause track played to no one — not " +
                    "even the jury."
        }
        val base = "🎬 CREDITS — “${film.title}”, starring " +
            "$starsLine.\n$tierLine\nTonight was " +
            "${MovieNights.tierLabel(tier)} (buzz $buzz)."
        if (night < NIGHTS - 1) {
            return base + "\nThe jury takes its notes home " +
                "in the dark. Their cards stay sealed until " +
                "closing night."
        }
        // closing night: every night's buzz is known —
        // the attended ones from the record, this one
        // from the seat you're sitting in — so the
        // award is read before the house lights come up.
        val cards = (0 until NIGHTS).map { n ->
            val b = if (n == night) buzz
                else buzzFor(state, year, n)
            scoreCard(year, filmFor(year, n), b)
        }
        val w = pickWinner(cards) ?: return base
        val wStars = starsFor(year, w.film).joinToString(", ")
        return base + "\n🏆 And the jury's cards are read: " +
            "$AWARD goes to “${w.film.title}” — starring " +
            "$wStars — on ${w.total} festival points. The " +
            "room does what rooms do. The popcorn is " +
            "ceremonial. Nobody eats the popcorn."
    }

    private fun plantFestivalTopic(name: String) {
        if (name !in KID_ORDER) return
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** The night's wrap, at the credits: the night
     *  lands in the year's record at the buzz it
     *  reached, the house carries the festival into
     *  tomorrow, and the session ends. Exactly once —
     *  the record is the guard. */
    fun finishNight(state: GameState) {
        val (year, night) = tonight(state) ?: return
        val rec = recordOrNew(state, year)
        if (rec.attended.containsKey(night)) return
        val buzz = buzzNow(state)
        saveRecord(state, rec.copy(
            attended = rec.attended + (night to buzz),
            settled = rec.settled + night))
        for (name in attendance(state, year, night)) {
            plantFestivalTopic(name)
        }
        val film = filmFor(year, night)
        note(state, "🎬 Festival night ${night + 1}: " +
            "“${film.title}” played to " +
            "${MovieNights.tierLabel(MovieNights.tierFor(buzz))} " +
            "(buzz $buzz).")
        townLogLine(state, "🎬 Festival night ${night + 1}: " +
            "“${film.title}” — " +
            MovieNights.tierLabel(MovieNights.tierFor(buzz)))
        state.life.remove(kSeat(state.absDay))
    }

    // ---------------- the closing-night photograph ----------------

    /** The festival's ONE photograph: closing night,
     *  after your credits, while the house is still
     *  here — the photo system's own group composition
     *  from the people physically in the room, with the
     *  winner named in the caption. Returns the text to
     *  show, or null when there's no frame to take. */
    fun takePhoto(state: GameState): String? {
        val (year, night) = tonight(state) ?: return null
        if (night != NIGHTS - 1) return null
        val rec = record(state, year) ?: return null
        if (!rec.attended.containsKey(night)) return null
        if (rec.photoId.isNotEmpty()) return null
        if (state.timeMin >= 22 * 60) return null
        val present = MovieNights.presentCast(state)
        if (present.isEmpty()) return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        val w = winnerFilm(state, year) ?: return null
        val shot = Photos.composeGroup(state, present,
            emptyList(), me.name, me.appearance)
        val poster = shot.copy(caption =
            "🏆 Closing night at the Bijou Film Festival — " +
                "${dateLabel(state.absDay)}. “${w.title}” " +
                "took $AWARD, and the room stayed for the " +
                "photograph.")
        Photos.applyPhoto(state, poster)
        saveRecord(state, rec.copy(photoId = poster.id))
        return "📸 THE FESTIVAL PHOTO — the house arranges " +
            "itself around the ceremonial popcorn (front " +
            "row crouching, one juror mid-speech forever) " +
            "and the shutter takes closing night whole. " +
            "It's in your album."
    }

    // ---------------- surfaces ----------------

    /** The morning report's line: the announcement
     *  week's countdown, tonight's entry on a festival
     *  day (or its dark reason), and the wrap's answer
     *  on the morning it lands. */
    fun morningNote(state: GameState): String? {
        for (y in yearsAround(state)) {
            val rec = record(state, y)
            if (rec != null && rec.wrapped &&
                rec.wrapDay == state.absDay) {
                val w = winnerFilm(state, y) ?: continue
                val wStars = starsFor(y, w).joinToString(", ")
                return "🏆 The Bijou Film Festival closed: " +
                    "$AWARD went to “${w.title}” (starring " +
                    "$wStars). The jury has gone back to " +
                    "being neighbors."
            }
        }
        val t = tonight(state)
        if (t != null) {
            val (year, night) = t
            if (nightDark(state)) {
                return "🎬 Festival night ${night + 1} is " +
                    "dark tonight — ${darkText(state)}. The " +
                    "week keeps its other nights."
            }
            val film = filmFor(year, night)
            return "🎬 Festival night ${night + 1} of " +
                "$NIGHTS at the Bijou: “${film.title}” " +
                "(${film.genre}), starring " +
                starsFor(year, film).joinToString(", ") +
                ". Doors at 7, and the town already paid " +
                "for the print."
        }
        for (y in yearsAround(state)) {
            if (state.absDay == FestivalCalendar.announceDay(y)) {
                val mon = FestivalCalendar.nightDay(y, 0)
                val fri = FestivalCalendar.nightDay(
                    y, NIGHTS - 1)
                return "🎬 POSTER UP at the Bijou: $NAME — " +
                    "five nights, ${dateLabel(mon)}–" +
                    "${dateLabel(fri)}. Five town prints, " +
                    "one jury, and $AWARD on Friday."
            }
        }
        return null
    }

    /** The Look text at the Bijou: during the poster
     *  week the marquee carries the program; on a
     *  festival day it carries tonight's entry (honest
     *  when dark); the rest of the year, nothing — the
     *  regular marquee speaks for itself. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != THEATER) return null
        val t = tonight(state)
        if (t != null) {
            val (year, night) = t
            if (nightDark(state)) {
                return "🎬 The festival marquee is dark " +
                    "tonight — ${darkText(state)}. The week " +
                    "keeps its other nights."
            }
            val film = filmFor(year, night)
            val starsLine = starsFor(year, film)
                .joinToString(", ")
            return if (state.playerInside) {
                "🎬 Festival night ${night + 1}: " +
                    "“${film.title}” — a " +
                    "${film.genre.lowercase()} starring " +
                    "$starsLine. The screen is the big " +
                    "silver rectangle. Tap it when you're " +
                    "ready to take your seat."
            } else {
                "🎬 The marquee is lit: FESTIVAL NIGHT " +
                    "${night + 1} — “${film.title}” " +
                    "(${film.genre}), starring $starsLine. " +
                    "Doors at 7; admission is the town's treat."
            }
        }
        for (y in yearsAround(state)) {
            val ann = FestivalCalendar.announceDay(y)
            if (state.absDay in ann until
                FestivalCalendar.nightDay(y, 0)) {
                val mon = FestivalCalendar.nightDay(y, 0)
                val fri = FestivalCalendar.nightDay(
                    y, NIGHTS - 1)
                return "🎬 A second poster is up at the " +
                    "Bijou: $NAME — five nights, " +
                    "${dateLabel(mon)}–${dateLabel(fri)}: " +
                    slate(y).joinToString("; ") {
                        "“${it.title}”" } + ". The jury: " +
                    juryFor(y).joinToString(", ") { it.name } +
                    ". Doors at 7, the town's treat."
            }
        }
        return null
    }

    /** The Gazette's festival lines — TOWN NEWS only,
     *  never a headline tier (the ladder stands at ten;
     *  the festival is town news like the dance, the
     *  show and the parade before it): the announcement
     *  in its week, and the winner in the closing
     *  week's edition, both derived whole off the
     *  calendar and the year's record. */
    fun paperLine(state: GameState, week: Int): String? {
        for (y in yearsAround(state)) {
            val closing = FestivalCalendar.nightDay(
                y, NIGHTS - 1)
            if (Jobs.weekIndex(closing) == week) {
                val rec = record(state, y)
                if (rec != null && rec.wrapped) {
                    val w = winnerCard(state, y)
                    val film = w?.film
                    if (film != null && w != null) {
                        val wStars = starsFor(y, film)
                            .joinToString(", ")
                        val sat = if (rec.attended.containsKey(
                                NIGHTS - 1))
                            " You were in the house for " +
                                "closing night."
                        else ""
                        return "🎬 The Bijou Film Festival " +
                            "closed Friday: $AWARD went to " +
                            "“${film.title}” (starring " +
                            "$wStars) on ${w.total} festival " +
                            "points. The jury: " +
                            juryFor(y).joinToString(", ") {
                                it.name } + ".$sat"
                    }
                }
            }
        }
        for (y in yearsAround(state)) {
            val ann = FestivalCalendar.announceDay(y)
            if (Jobs.weekIndex(ann) == week) {
                val mon = FestivalCalendar.nightDay(y, 0)
                val fri = FestivalCalendar.nightDay(
                    y, NIGHTS - 1)
                return "🎬 The Bijou Film Festival is " +
                    "coming — five nights, " +
                    "${dateLabel(mon)}–${dateLabel(fri)}: " +
                    slate(y).joinToString("; ") {
                        "“${it.title}”" } + ". The jury: " +
                    juryFor(y).joinToString(", ") { it.name } +
                    ". Admission is the town's treat, as ever."
            }
        }
        return null
    }

    /** The year's festival line for the school year's
     *  book — December's festival belongs to the school
     *  year that ends the following spring (the talent
     *  show's convention: [year] here is the SCHOOL
     *  year, the record's is the calendar year). */
    fun yearbookLine(state: GameState,
                     year: Int): String? {
        val rec = record(state, year - 1) ?: return null
        if (!rec.wrapped) return null
        val w = winnerCard(state, year - 1) ?: return null
        return "🎬 The Bijou Film Festival, December " +
            "${year - 1} (${Yearbook.labelOf(year)}): " +
            "$AWARD went to “${w.film.title}” — starring " +
            starsFor(year - 1, w.film).joinToString(", ") +
            " — on ${w.total} festival points."
    }

    /** The festival's ONE morning-after text, in the
     *  harvest's fact discipline: from the winning
     *  film's first star, on the wrap's own morning,
     *  fact-only, moving nothing. */
    fun phoneBeat(state: GameState):
            Triple<String, String, String>? {
        for (y in yearsAround(state)) {
            val rec = record(state, y) ?: continue
            if (!rec.wrapped) continue
            if (rec.wrapDay != state.absDay) continue
            val w = winnerFilm(state, y) ?: continue
            val sender = starsFor(y, w).firstOrNull()
                ?: continue
            val sat = rec.attended.containsKey(NIGHTS - 1)
            val text = if (sat) {
                "festival's closed — we took the golden " +
                    "popcorn for “${w.title}.” you were in " +
                    "the house friday. the jury pretended " +
                    "not to cry. see the journal page."
            } else {
                "festival's closed — the golden popcorn " +
                    "went to “${w.title}.” you missed " +
                    "closing night. the journal has the " +
                    "week, what there was of it."
            }
            return Triple(sender, "closed", text)
        }
        return null
    }

    /** The 🎬 FILM FESTIVAL Journal page, assembled
     *  pure like the other boards: the live year's
     *  program or its read-back, then the festivals on
     *  the record. Scores print only once a year wraps —
     *  the jury's cards are sealed until then, in the
     *  page's own words. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val years = recordYears(state)
        out += "🎬 FILM FESTIVAL — ${years.size} on the record"
        out += "One week a year — the December week that " +
            "holds the 8th, Monday to Friday — the Bijou " +
            "runs five town prints before a jury of three " +
            "and whoever the room draws in. The slate is " +
            "posted a week ahead; the jury's cards stay " +
            "sealed until closing night, when $AWARD is " +
            "awarded. Admission is free: the town bought " +
            "the prints. Showing up is the ticket."
        val next = FestivalCalendar.nextYearAfter(state.absDay)
        val liveYear = tonight(state)?.first
        val showYear = liveYear ?: next
        val rec = record(state, showYear)
        when {
            rec != null && rec.wrapped -> {
                out += yearLines(state, showYear, rec)
            }
            liveYear != null -> {
                val (year, _) = tonight(state)!!
                out += "🎞 THIS WEEK — the jury: " +
                    juryFor(year).joinToString(", ") {
                        "${it.name} (leans ${it.lean})" } + "."
                val att = rec?.attended ?: emptyMap()
                val yld = rec?.yielded ?: emptySet()
                for (n in 0 until NIGHTS) {
                    val f = filmFor(year, n)
                    val day = FestivalCalendar.nightDay(year, n)
                    val mark = when {
                        att.containsKey(n) ->
                            "✓ seen (buzz ${att[n]})"
                        n in yld ->
                            "dark — the town owned the night"
                        day < state.absDay -> "— the house's night"
                        day == state.absDay -> "TONIGHT"
                        else -> "to come"
                    }
                    out += "· ${Jobs.WEEKDAY_NAMES[
                        Jobs.weekdayOf(day)]} " +
                        "${dateLabel(day)}: “${f.title}” " +
                        "(${f.genre}), starring " +
                        starsFor(year, f).joinToString(", ") +
                        " — $mark."
                }
                out += "The jury's cards are sealed until " +
                    "closing night."
            }
            else -> {
                val mon = FestivalCalendar.nightDay(next, 0)
                val fri = FestivalCalendar.nightDay(
                    next, NIGHTS - 1)
                out += "🎞 Next: ${dateLabel(mon)}–" +
                    "${dateLabel(fri)} — " +
                    slate(next).joinToString("; ") {
                        "“${it.title}”" } + ". The jury: " +
                    juryFor(next).joinToString(", ") {
                        it.name } + "."
            }
        }
        val past = years.filter { it != showYear }
            .sortedDescending().take(HISTORY_CAP)
        if (past.isNotEmpty()) {
            out += "📽 PAST FESTIVALS:"
            for (y in past) {
                val r = record(state, y) ?: continue
                if (!r.wrapped) continue
                out += yearLines(state, y, r)
            }
        }
        return out
    }

    private fun yearLines(state: GameState, year: Int,
                          rec: FestRecord): String {
        val w = winnerCard(state, year)
        val seen = rec.attended.size
        val dark = rec.yielded.size
        val wBit = if (w != null)
            " 🏆 “${w.film.title}” took $AWARD on " +
                "${w.total} points."
        else ""
        return "· December $year: you sat $seen of " +
            "$NIGHTS nights" +
            (if (dark > 0) ", $dark dark" else "") +
            ".$wBit"
    }

    // ---------------- lifecycle (the Engine's rollover calls) ----------------

    /** Was a case climax due on festival night [night]
     *  of [year]? Judged the way MovieNights judges
     *  tonight: the arcs' due-functions read on a copy
     *  of the state standing at that evening's doors. */
    private fun climaxDueOn(state: GameState, year: Int,
                            night: Int): Boolean {
        val day = FestivalCalendar.nightDay(year, night)
        val key = keyFromOrdinal(
            FairCalendar.ordinalOfAbsDay(day))
        val copy = state.copy(
            absDay = day, timeMin = MovieNights.DOORS_FROM,
            todayKey = key,
            gameYear = key.substring(0, 4).toIntOrNull()
                ?: state.gameYear,
            gameMonth = key.substring(4, 6).toIntOrNull()
                ?: state.gameMonth,
            gameDay = key.substring(6, 8).toIntOrNull()
                ?: state.gameDay)
        return MovieNights.anyClimaxDueTonight(copy)
    }

    private fun bumpFriendship(state: GameState,
                               name: String, delta: Int) {
        if (delta == 0) return
        val cur = state.friendship[name] ?: 0
        state.friendship[name] =
            (cur + delta).coerceIn(-50, 100)
    }

    /** The festival's mornings, called by
     *  Engine.onNewDay beside the other set pieces,
     *  before the paper and the phone (their line and
     *  text derive from these facts): the poster's
     *  ticker on announcement morning; every past
     *  festival night settled — a session left
     *  mid-feature becomes an attended night at the
     *  buzz it reached, a night a case climax owned is
     *  stamped yielded, the rest are simply the
     *  house's; and the year's wrap on the first
     *  morning after closing night — the winner
     *  derived, the winning stars paid if you sat
     *  closing night, exactly once. Yesterday's
     *  session keys are pruned. */
    fun dailyRollover(state: GameState) {
        val today = state.absDay
        for (y in yearsAround(state)) {
            // announcement morning: the poster's ticker
            if (today == FestivalCalendar.announceDay(y) &&
                state.life["festAnn:$y"] == null) {
                state.life["festAnn:$y"] = 1
                val mon = FestivalCalendar.nightDay(y, 0)
                val fri = FestivalCalendar.nightDay(
                    y, NIGHTS - 1)
                note(state, "🎬 POSTER UP at the Bijou: " +
                    "$NAME — five nights, " +
                    "${dateLabel(mon)}–${dateLabel(fri)}. " +
                    "Five town prints, one jury, and " +
                    "$AWARD on closing night. Admission " +
                    "is the town's treat.")
            }
            // settle the nights that have ended
            var rec = record(state, y)
            for (n in 0 until NIGHTS) {
                val d = FestivalCalendar.nightDay(y, n)
                if (d >= today || d < today - 10) continue
                if (rec != null && n in rec.settled) continue
                var r = rec ?: FestRecord(y)
                val yieldStamp =
                    state.lifeText["festYield:$y:$n"]
                r = when {
                    (state.life[kSeat(d)] ?: 0) == 1 -> {
                        val buzz = state.life[kBuzz(d)] ?: 0
                        townLogLine(state,
                            "🎬 Festival night ${n + 1}: " +
                                "“${filmFor(y, n).title}” — " +
                                "you slipped out before " +
                                "the credits.")
                        r.copy(
                            attended = r.attended +
                                (n to buzz),
                            settled = r.settled + n)
                    }
                    yieldStamp != null ||
                        climaxDueOn(state, y, n) -> r.copy(
                            yielded = r.yielded + n,
                            settled = r.settled + n)
                    else -> r.copy(settled = r.settled + n)
                }
                saveRecord(state, r)
                if (yieldStamp != null) {
                    state.lifeText.remove("festYield:$y:$n")
                }
                rec = r
            }
            // the wrap: the first morning after closing
            val closing = FestivalCalendar.nightDay(
                y, NIGHTS - 1)
            if (today > closing && today <= closing +
                WRAP_TAIL) {
                val r0 = record(state, y)
                if (r0 == null || !r0.wrapped) {
                    var r = (r0 ?: FestRecord(y)).copy(
                        wrapped = true, wrapDay = today)
                    saveRecord(state, r)
                    val w = winnerFilm(state, y)
                    if (w != null) {
                        if (r.attended.containsKey(
                                NIGHTS - 1) && !r.paid) {
                            for (star in starsFor(y, w)) {
                                bumpFriendship(state, star,
                                    WINNER_STAR_PAY)
                            }
                            r = r.copy(paid = true)
                            saveRecord(state, r)
                        }
                        note(state, "🏆 $NAME has its " +
                            "winner: “${w.title}” takes " +
                            "$AWARD. The jury has gone " +
                            "back to being neighbors.")
                    }
                }
            }
        }
        // prune yesterday's session keys — the records
        // carry the permanent marks
        state.life.keys.removeAll {
            (it.startsWith("festSeat:") ||
                it.startsWith("festStep:") ||
                it.startsWith("festBuzz:")) &&
                (it.substringAfter(':').toIntOrNull()
                    ?: Int.MAX_VALUE) < today
        }
        state.lifeText.keys.removeAll {
            it.startsWith("festFilm:") &&
                (it.substringAfter(':').toIntOrNull()
                    ?: Int.MAX_VALUE) < today
        }
    }
}

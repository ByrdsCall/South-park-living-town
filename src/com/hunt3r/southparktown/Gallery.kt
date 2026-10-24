package com.hunt3r.southparktown

// ============================================================
// The Gallery Show — v0.15.0f (Queue #15 item 4): the
// Community Hall's seasonal photo exhibition.
//
// Four times a year — once a season — the hall at City
// Hall (the town's community hall; the ladder in
// Mystery6's audit case was signed out of it by its own
// key) hangs a photo show: a theme derived from the
// season and the year, a submission window that runs
// from the season's first morning to hanging night, a
// hanging night as a set piece, seven days on the wall,
// and a jury in the yearbook-judge pattern that settles
// it at the wrap. The paper gives it TOWN NEWS lines and
// no headline tier (the ladder stands at ten); the phone
// carries one morning-after text under the GALLERY
// trigger; the Journal keeps the page.
//
// ENTRIES ARE REAL OR THEY ARE NOTHING. The player
// enters prints they actually own: album photographs
// taken this season, or darkroom prints they hold — a
// print is the distinguished class (developed by hand,
// judged from its frozen copy, and lent, never consumed:
// the darkroom's second life). Cast entries derive from
// cast members' own history in the same album — a photo
// they commissioned, or a frame they stood in — picked
// by a stated ladder, one frame per entrant, no frame
// twice on the wall. Nothing is fabricated for anyone:
// if the player owns nothing eligible, the show hangs
// around them and the page says so; if nobody has
// anything, the walls go up bare and the book says that
// too. Every entry is stored AS A COPY (the yearbook
// volume's survival pattern), so the wall and the page
// render the real frame even after the album cap fades
// the original.
//
// THE JURY. Six townsfolk in two leaning trios (craft /
// heart / light), rotating by show. A score is DERIVED,
// never rolled: twice the theme fit (read off the
// record's own fields — its place, its weather, its
// sky, its hour, who stood in it and how) + the craft
// read + each juror's leaning read at face value. The
// winner takes the wall's center frame as champion
// until the next show hangs, a keepsake first; the coin
// prize is the arts line's 10 🪙 when the winner is you,
// and a cast winner takes +1 friendship at the wrap —
// only if you were there (hanging night or a visit),
// the festival's played-through rule. Placements are
// marks: jury notes whose every factual claim is a slot
// filled from the record itself, argued in the paper's
// TOWN NEWS and kept on the page.
//
// State: ONE additive save key, "gallery" (Prefs) —
// the capped list of show records (the newest
// FULL_SHOWS whole; elders reduce to their winners
// line, the yearbook shelf's discipline). Transient
// hanging-beat progress rides a pruned life key, like
// the festival's session keys. Everything else —
// phases, eligibility, scores, placements, the
// champion — derives from the calendar and the
// records, and a hostile doc can't mint a winner the
// arithmetic didn't earn.
// ============================================================

/** One entered frame. [record] is the photo's facts,
 *  copied at entry time — later album edits (a star
 *  toggled, the record itself fading) never rewrite it.
 *  [kind] = album | print | cast. A "print" entry's
 *  [printId] names the darkroom print lent to the wall;
 *  a cast entry's [photoId] names the album record it
 *  derives from. */
data class GalleryEntry(
    val entrant: String,          // Gallery.YOU or a cast name
    val kind: String,
    val photoId: String,
    val printId: String = "",
    val record: PhotoRecord
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "entrant" to entrant, "kind" to kind,
        "photoId" to photoId, "printId" to printId,
        "record" to record.toMap())

    companion object {
        fun fromMap(m: Map<String, Any?>): GalleryEntry? {
            val entrant = m["entrant"]?.toString() ?: return null
            val rec = (m["record"] as? Map<String, Any?>)
                ?.let { PhotoRecord.fromMap(it) } ?: return null
            val kind = m["kind"]?.toString() ?: return null
            if (kind !in Gallery.ENTRY_KINDS) return null
            return GalleryEntry(
                entrant = entrant, kind = kind,
                photoId = m["photoId"]?.toString() ?: rec.id,
                printId = m["printId"]?.toString() ?: "",
                record = rec)
        }
    }
}

/** One season's show. Derived facts (theme, hang day,
 *  scores, placements, the winner) are recomputed from
 *  the calendar and the entries on every read; the
 *  stored fields are the ones only the living can know:
 *  what was entered, whether the evening was kept, and
 *  whether the wrap has landed. */
data class GalleryShow(
    /** "g<year>-<season ordinal>" — unique per season. */
    val id: String,
    val year: Int,
    /** Season.name — validated against the enum. */
    val season: String,
    val themeId: String,
    val hangDay: Int,
    val closeDay: Int,
    var entries: List<GalleryEntry> = emptyList(),
    var locked: Boolean = false,
    /** The hanging happened without its set piece —
     *  the town's bigger business owned the evening. */
    var hungQuietly: Boolean = false,
    var attendedHanging: Boolean = false,
    var visitedShow: Boolean = false,
    var wrapped: Boolean = false,
    var wrapDay: Int = -1,
    /** The wrap's pays have landed (winner friendship). */
    var paid: Boolean = false,
    /** Reduced by the shelf cap: winners line only. */
    var indexOnly: Boolean = false,
    var winnersLine: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "year" to year, "season" to season,
        "themeId" to themeId, "hangDay" to hangDay,
        "closeDay" to closeDay,
        "entries" to entries.map { it.toMap() },
        "locked" to locked, "hungQuietly" to hungQuietly,
        "attendedHanging" to attendedHanging,
        "visitedShow" to visitedShow,
        "wrapped" to wrapped, "wrapDay" to wrapDay,
        "paid" to paid, "indexOnly" to indexOnly,
        "winnersLine" to winnersLine)

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(m: Map<String, Any?>): GalleryShow? {
            val id = m["id"]?.toString() ?: return null
            val seasonName = m["season"]?.toString() ?: return null
            val season = Season.values().firstOrNull {
                it.name == seasonName } ?: return null
            val year = (m["year"] as? Number)?.toInt() ?: return null
            val themeId = m["themeId"]?.toString() ?: return null
            // a theme the table doesn't know is a forged
            // show — dropped whole, never re-themed
            if (Gallery.themeById(themeId) == null) return null
            if (Gallery.themeById(themeId)?.season != season)
                return null
            val cal = GalleryCalendar.cal(year, season)
            val entries = (m["entries"] as? List<Any?>)
                ?.mapNotNull {
                    (it as? Map<String, Any?>)?.let { em ->
                        GalleryEntry.fromMap(em) }
                } ?: emptyList()
            return GalleryShow(
                id = id, year = year, season = seasonName,
                themeId = themeId,
                hangDay = cal.hangDay, closeDay = cal.closeDay,
                entries = entries.take(Gallery.WALL_CAP),
                locked = m["locked"] as? Boolean ?: false,
                hungQuietly = m["hungQuietly"] as? Boolean
                    ?: false,
                attendedHanging = m["attendedHanging"] as?
                    Boolean ?: false,
                visitedShow = m["visitedShow"] as? Boolean
                    ?: false,
                wrapped = m["wrapped"] as? Boolean ?: false,
                wrapDay = (m["wrapDay"] as? Number)?.toInt()
                    ?: -1,
                paid = m["paid"] as? Boolean ?: false,
                indexOnly = m["indexOnly"] as? Boolean ?: false,
                winnersLine = m["winnersLine"]?.toString()
                    ?: "")
        }
    }
}

/** The calendar shape of one season's show — pure
 *  calendar math, computed the same on every read. */
data class GalleryCal(
    val year: Int, val season: Season,
    /** absDay submissions open (the season's 1st). */
    val announceDay: Int,
    /** absDay of hanging night (2nd Friday of the
     *  season's middle month). */
    val hangDay: Int,
    /** absDay the show comes down (hangDay + RUN_DAYS). */
    val closeDay: Int,
    /** The morning the wrap lands. */
    val wrapDay: Int,
    /** Ordinal span of the season itself — a record
     *  competes in the season of its own dayKey. */
    val seasonStartOrd: Int,
    val seasonEndOrd: Int
) {
    val id: String get() = "g$year-${GalleryCalendar.seasonOrdinal(season)}"
    val index: Int get() = year * 4 + GalleryCalendar.seasonOrdinal(season)
}

object GalleryCalendar {

    fun seasonOrdinal(s: Season): Int = when (s) {
        Season.WINTER -> 0
        Season.SPRING -> 1
        Season.SUMMER -> 2
        Season.FALL -> 3
    }

    /** (year, month) of a season's first / middle / last
     *  month. Winter straddles the year turn: it opens in
     *  December of [year] and closes in February of the
     *  next — the season is named for the December. */
    private fun firstMonth(year: Int, s: Season): Pair<Int, Int> =
        when (s) {
            Season.WINTER -> year to 12
            Season.SPRING -> year to 3
            Season.SUMMER -> year to 6
            Season.FALL -> year to 9
        }

    private fun middleMonth(year: Int, s: Season): Pair<Int, Int> =
        when (s) {
            Season.WINTER -> (year + 1) to 1
            Season.SPRING -> year to 4
            Season.SUMMER -> year to 7
            Season.FALL -> year to 10
        }

    private fun lastMonth(year: Int, s: Season): Pair<Int, Int> =
        when (s) {
            Season.WINTER -> (year + 1) to 2
            Season.SPRING -> year to 5
            Season.SUMMER -> year to 8
            Season.FALL -> year to 11
        }

    private fun firstOrd(year: Int, month: Int): Int =
        ordinalOfKey("%04d%02d01".format(year, month))

    /** The second Friday of [year]-[month] (Dance's
     *  formula: the 1st, forward to Friday, +7). */
    private fun secondFridayOrdinal(year: Int, month: Int): Int {
        val first = firstOrd(year, month)
        val toFriday = (5 - FairCalendar.dowOfOrdinal(first) + 7) % 7
        return first + toFriday + 7
    }

    /** The full calendar of [year]'s [season] show. */
    fun cal(year: Int, season: Season): GalleryCal {
        val (fy, fm) = firstMonth(year, season)
        val (my, mm) = middleMonth(year, season)
        val (ly, lm) = lastMonth(year, season)
        val startOrd = firstOrd(fy, fm)
        val (ny, nm) = if (lm == 12) (ly + 1) to 1 else ly to (lm + 1)
        val endOrd = firstOrd(ny, nm) - 1
        val hangOrd = secondFridayOrdinal(my, mm)
        val hangDay = FairCalendar.absDayOfOrdinal(hangOrd)
        return GalleryCal(
            year = year, season = season,
            announceDay = FairCalendar.absDayOfOrdinal(startOrd),
            hangDay = hangDay,
            closeDay = hangDay + Gallery.RUN_DAYS,
            wrapDay = hangDay + Gallery.RUN_DAYS + 1,
            seasonStartOrd = startOrd, seasonEndOrd = endOrd)
    }

    /** The (season, season-year) a date key belongs to —
     *  January and February belong to the winter that
     *  opened the December before. Null on a bad key. */
    fun seasonOfKey(key: String): Pair<Season, Int>? {
        if (key.length != 8) return null
        val y = key.substring(0, 4).toIntOrNull() ?: return null
        val m = key.substring(4, 6).toIntOrNull() ?: return null
        if (m !in 1..12) return null
        return when (m) {
            12 -> Season.WINTER to y
            1, 2 -> Season.WINTER to (y - 1)
            in 3..5 -> Season.SPRING to y
            in 6..8 -> Season.SUMMER to y
            else -> Season.FALL to y
        }
    }

    /** The show whose CYCLE (announce morning through
     *  the wrap morning) contains [absDay], or — in the
     *  quiet weeks between a wrap and the next season —
     *  the NEXT show, flagged by [Cycle.next]. */
    data class Cycle(val cal: GalleryCal, val next: Boolean)

    fun cycleAt(absDay: Int): Cycle {
        val key = keyFromOrdinal(FairCalendar.ordinalOfAbsDay(absDay))
        val (season, year) = seasonOfKey(key) ?: (Season.FALL to 2026)
        val here = cal(year, season)
        // a date key always falls inside its own season,
        // and a season contains its announce morning — so
        // the only question is whether the wrap has passed
        if (absDay <= here.wrapDay) return Cycle(here, false)
        val nxt = nextOf(year, season)
        return Cycle(cal(nxt.first, nxt.second), true)
    }

    private fun nextOf(year: Int, s: Season): Pair<Int, Season> =
        when (s) {
            Season.WINTER -> year to Season.SPRING
            Season.SPRING -> year to Season.SUMMER
            Season.SUMMER -> year to Season.FALL
            Season.FALL -> year to Season.WINTER
        }

    private fun prevOf(year: Int, s: Season): Pair<Int, Season> =
        when (s) {
            Season.WINTER -> (year - 1) to Season.FALL
            Season.SPRING -> year to Season.WINTER
            Season.SUMMER -> year to Season.SPRING
            Season.FALL -> year to Season.SUMMER
        }

}

object Gallery {

    // ---------------- the dials (README tuning ledger) ----------------

    const val VENUE = "CityHall"
    const val VENUE_NAME = "the Community Hall"
    const val NAME = "The Gallery Show"
    const val YOU = "You"
    const val TOPIC = "gallery show"

    /** The show hangs this many days after hanging
     *  night (the night itself belongs to the hanging). */
    const val RUN_DAYS = 7

    /** Frames the wall holds, all entrants together. */
    const val WALL_CAP = 10
    /** Prints of your own you may enter in one show. */
    const val PLAYER_ENTRY_CAP = 2
    /** Cast frames the jury will hang beside yours. */
    const val CAST_ENTRY_CAP = 8

    /** The hanging's evening window. */
    const val HANG_FROM = 18 * 60
    const val HANG_TO = 21 * 60
    /** The hung show's daily open hours. */
    const val OPEN_FROM = 10 * 60
    const val OPEN_TO = 20 * 60
    /** The visiting claim's afternoon window on show
     *  days (the hall's busy hours). */
    const val VISIT_FROM = 12 * 60
    const val VISIT_TO = 16 * 60

    /** The arts line's prize when the winner is you —
     *  a keepsake first (the champion frame), coins
     *  second, inside the one-off prize range the bee
     *  and the fair set. */
    const val WINNER_COINS = 10
    /** A cast winner's friendship at the wrap — paid
     *  once, and only if you kept the hanging or came
     *  to see the show (the played-through rule). */
    const val WINNER_PAY = 1
    /** A darkroom print's jury read: the hand-made
     *  bonus, stated on the page and in the notes. */
    const val PRINT_BONUS = 3

    /** Wrapped shows the record keeps whole; elders
     *  reduce to their winners line (the yearbook
     *  shelf's discipline). */
    const val FULL_SHOWS = 4
    /** Days after the wrap a slept-through show can
     *  still settle (the festival's tail). */
    const val WRAP_TAIL = 30

    val ENTRY_KINDS = setOf("album", "print", "cast")
    const val KIND_ALBUM = "album"
    const val KIND_PRINT = "print"
    const val KIND_CAST = "cast"

    /** Copy the show must never carry: real pictures,
     *  real franchises, real exhibitions — the denylist
     *  the suite scans every authored string against. */
    val DENYLIST: List<String> = listOf(
        "star wars", "star trek", "harry potter", "marvel",
        "avengers", "spider-man", "spiderman", "batman",
        "superman", "pokemon", "disney", "pixar", "frozen",
        "shrek", "minion", "jurassic", "transformers",
        "ghibli", "mario", "zelda", "ghostbusters",
        "titanic", "jaws", "terminator", "top gun",
        "barbie", "oppenheimer", "avatar", "encanto",
        "moana", "zootopia", "inside out", "ansel adams",
        "monet", "van gogh", "picasso", "warhol", "banksy",
        "national geographic", "kodak", "instagram")

    // ---------------- the themes ----------------
    // One theme per season per year, drawn from the
    // season's pool of three by the show's index — so a
    // season never repeats its theme two years running.
    // Every fit reads ONLY the record's own fields (its
    // place, its weather, its sky by Yearbook's own
    // derivation, its hour's band, who stood in it and
    // how, its stamp) — a theme can always be answered
    // by a photograph the town can actually take.

    data class GalTheme(
        val id: String, val season: Season, val title: String,
        val blurb: String, val fit: (PhotoRecord) -> Int)

    private fun skyOf(rec: PhotoRecord): String =
        Yearbook.skySpecialOf(rec)

    private fun bandOf(rec: PhotoRecord): String =
        SceneLight.bandOf(rec.timeMin)

    private fun dramaOf(rec: PhotoRecord): Int =
        Yearbook.WEATHER_DRAMA[rec.weatherId] ?: 0

    private fun happyOf(rec: PhotoRecord): Int =
        rec.npcs.count { it.reaction == Photos.HAPPY }

    private fun goofyOf(rec: PhotoRecord): Int =
        rec.npcs.count { it.reaction == Photos.GOOFY }

    val THEMES: List<GalTheme> = listOf(
        GalTheme("winter_snow", Season.WINTER, "First Snow",
            "The season the world goes white — show the " +
                "hall what the snow did to the town.",
            { rec ->
                val w = when (rec.weatherId) {
                    "blizzard" -> 14
                    "heavy_snow" -> 11
                    "light_snow" -> 8
                    else -> 0
                }
                w + minOf(rec.figures.size, 6) +
                    if (rec.inside) 0 else 4
            }),
        GalTheme("winter_night", Season.WINTER, "The Long Night",
            "Winter's dark comes early and stays — " +
                "photograph what the town does by lamplight.",
            { rec ->
                val band = when (bandOf(rec)) {
                    "night" -> 10
                    "dusk" -> 4
                    else -> 0
                }
                val sky = when (skyOf(rec)) {
                    SkyState.AURORA -> 12
                    SkyState.METEOR -> 10
                    else -> 0
                }
                band + sky + minOf(dramaOf(rec) / 3, 5)
            }),
        GalTheme("winter_in", Season.WINTER, "Gathered In",
            "Cold outside, everybody in — the season of " +
                "full rooms and fogged windows.",
            { rec ->
                (if (rec.inside) 10 else 0) +
                    minOf(2 * rec.figures.size, 10) +
                    minOf(happyOf(rec) + goofyOf(rec), 5)
            }),
        GalTheme("spring_thaw", Season.SPRING, "Thaw Water",
            "The ice lets go — the pond, the puddles, the " +
                "first rain that isn't snow.",
            { rec ->
                val place = if (rec.locId == "StarksPond")
                    12 else 0
                val w = when (rec.weatherId) {
                    "rain" -> 8
                    "fog" -> 6
                    else -> 0
                }
                place + w + if (rec.inside) 0 else 5
            }),
        GalTheme("spring_light", Season.SPRING, "New Light",
            "The sky remembers its colors — shoot the " +
                "season's returning light.",
            { rec ->
                val w = if (rec.weatherId == "sunny") 8 else 0
                val sky = when (skyOf(rec)) {
                    SkyState.METEOR -> 10
                    SkyState.FIREWORKS -> 8
                    SkyState.AURORA -> 6
                    else -> 0
                }
                w + sky + if (bandOf(rec) == "day") 4 else 0
            }),
        GalTheme("spring_faces", Season.SPRING, "First Faces",
            "Spring cleaning for the soul: one or two " +
                "faces, up close, being themselves.",
            { rec ->
                val frame = if (!rec.isGroup &&
                    rec.npcs.size in 1..2) 10 else 0
                frame + minOf(3 * goofyOf(rec) +
                    2 * happyOf(rec), 9) +
                    if (rec.npcs.size == 1) 3 else 0
            }),
        GalTheme("summer_gang", Season.SUMMER, "The Whole Gang",
            "Summer is a group activity — get everybody " +
                "in the frame.",
            { rec ->
                (if (rec.isGroup) 10 else 0) +
                    minOf(2 * rec.npcs.size, 12) +
                    minOf(rec.figures.size, 4)
            }),
        GalTheme("summer_air", Season.SUMMER, "Open Air",
            "Out from under the roofs — the town at its " +
                "most outdoors.",
            { rec ->
                (if (rec.inside) 0 else 10) +
                    when (bandOf(rec)) {
                        "day" -> 5
                        "dusk" -> 2
                        else -> 0
                    } +
                    minOf(rec.figures.size, 6) +
                    if (rec.weatherId == "sunny") 4 else 0
            }),
        GalTheme("summer_sky", Season.SUMMER, "High Summer Sky",
            "The biggest sky of the year — shoot up.",
            { rec ->
                val sky = when (skyOf(rec)) {
                    SkyState.METEOR -> 12
                    SkyState.FIREWORKS -> 10
                    SkyState.AURORA -> 8
                    SkyState.HARVEST -> 6
                    else -> 0
                }
                sky + minOf(dramaOf(rec) / 2, 8) +
                    if (rec.inside) 0 else 3
            }),
        GalTheme("fall_harvest", Season.FALL, "Harvest Light",
            "The low gold hour — the moon coming up big " +
                "and the light going amber.",
            { rec ->
                val sky = if (skyOf(rec) == SkyState.HARVEST)
                    14 else 0
                sky + if (bandOf(rec) == "dusk") 6 else 0 +
                    minOf(dramaOf(rec) / 3, 4) +
                    minOf(rec.figures.size, 4)
            }),
        GalTheme("fall_in", Season.FALL, "Back Inside",
            "The year turns in — lamplight, warm rooms, " +
                "everybody home earlier.",
            { rec ->
                (if (rec.inside) 10 else 0) +
                    when (bandOf(rec)) {
                        "night" -> 4
                        "dusk" -> 2
                        else -> 0
                    } +
                    minOf(2 * rec.figures.size, 8)
            }),
        GalTheme("fall_work", Season.FALL, "The Town at Work",
            "Photographs somebody ASKED for — the " +
                "commissioned print, judged as work.",
            { rec ->
                (if (rec.isCommissioned) 14 else 0) +
                    minOf(2 * rec.npcs.size, 8) +
                    minOf(rec.followersGained / 5, 4)
            })
    )

    fun themeById(id: String?): GalTheme? =
        THEMES.firstOrNull { it.id == id }

    /** A show's theme: its season's pool, rotated by the
     *  show's index — deterministic, and different from
     *  the same season's theme last year. */
    fun themeFor(cal: GalleryCal): GalTheme {
        val pool = THEMES.filter { it.season == cal.season }
        return pool[((cal.index % pool.size) + pool.size) %
            pool.size]
    }

    fun themeFit(theme: GalTheme, rec: PhotoRecord): Int =
        theme.fit(rec).coerceAtLeast(0)

    // ---------------- the jury (the Yearbook pattern) ----------------

    data class GalJuror(val name: String, val lean: String,
                        val taste: String)

    /** Six townsfolk in two leaning trios. The chair is
     *  the trio's first name. Jurors work their show —
     *  they never enter it (their frames can still be
     *  IN it, in somebody else's photograph). */
    val JURY_TRIOS: List<List<GalJuror>> = listOf(
        listOf(
            GalJuror("Victoria", "craft",
                "judges a frame the way she runs a school: " +
                    "composition first, mercy scheduled."),
            GalJuror("Wendy", "heart",
                "reads a photograph for the people in it " +
                    "before she reads it for anything else."),
            GalJuror("Mephesto", "light",
                "keeps asking what the light was DOING. " +
                    "Nobody has ever answered him properly.")),
        listOf(
            GalJuror("Jimmy", "light",
                "the yearbook's photo desk, grown into the " +
                    "job: he gasps at weather professionally."),
            GalJuror("Alexis", "craft",
                "keeps minutes on every crooked horizon. " +
                    "Every one. Alphabetized."),
            GalJuror("Big Gay Al", "heart",
                "believes every wall is secretly a pageant " +
                    "and scores the feeling first.")))

    /** The show's trio: rotated by season AND year, so
     *  neighbouring seasons differ and no season keeps
     *  the same jury two years running. */
    fun juryFor(cal: GalleryCal): List<GalJuror> {
        val k = cal.year + GalleryCalendar
            .seasonOrdinal(cal.season)
        return JURY_TRIOS[((k % 2) + 2) % 2]
    }

    fun chairFor(cal: GalleryCal): GalJuror = juryFor(cal)[0]

    // ---------------- scoring (derived, recomputed) ----------------

    /** The craft read: the frame's own facts, no jury
     *  in it yet — size, the followers it actually
     *  earned, your star, the posed composition, the
     *  commission stamp. */
    fun craftOf(rec: PhotoRecord): Int =
        2 * rec.figures.size +
            minOf(rec.followersGained / 4, 6) +
            (if (rec.starred) 4 else 0) +
            (if (rec.isGroup) 3 else 0) +
            (if (rec.isCommissioned) 2 else 0)

    /** One juror's leaning read of a record, at face
     *  value — craft reads the making, heart reads the
     *  faces, light reads the sky it was taken under. */
    fun leanRead(lean: String, rec: PhotoRecord): Int =
        when (lean) {
            "craft" -> craftOf(rec) / 2
            "heart" -> 2 * goofyOf(rec) + 2 * happyOf(rec) +
                (if (rec.isCommissioned) 3 else 0)
            else -> minOf(dramaOf(rec) / 4, 6) +
                when (skyOf(rec)) {
                    SkyState.AURORA -> 8
                    SkyState.METEOR -> 7
                    SkyState.HARVEST -> 6
                    SkyState.FIREWORKS -> 5
                    else -> 0
                } +
                when (bandOf(rec)) {
                    "night" -> 3
                    "dusk" -> 1
                    else -> 0
                }
        }

    /** An entry's score: twice the theme fit (the theme
     *  is the show's whole argument) + the craft read +
     *  the jury's three leanings + the print bonus for a
     *  hand-developed entry. A pure function of the
     *  entry and the show's jury — the same wall scores
     *  the same forever. */
    fun scoreOf(show: GalleryShow, entry: GalleryEntry): Int {
        val cal = GalleryCalendar.cal(show.year,
            Season.valueOf(show.season))
        val theme = themeById(show.themeId) ?: return 0
        val jury = juryFor(cal)
        return 2 * themeFit(theme, entry.record) +
            craftOf(entry.record) +
            jury.sumOf { leanRead(it.lean, entry.record) } +
            if (entry.kind == KIND_PRINT) PRINT_BONUS else 0
    }

    /** The wall, judged: entries best-first down the
     *  stated ladder — score, then theme fit, then
     *  craft, then the order the frames went up (the
     *  wall's own order), so a tie is settled by the
     *  hanging, never by a coin flip. */
    fun placements(show: GalleryShow): List<GalleryEntry> {
        val theme = themeById(show.themeId)
        return show.entries.mapIndexed { i, e -> i to e }
            .sortedWith(
                compareByDescending<Pair<Int, GalleryEntry>> {
                    scoreOf(show, it.second) }
                    .thenByDescending {
                        if (theme != null)
                            themeFit(theme, it.second.record)
                        else 0 }
                    .thenByDescending {
                        craftOf(it.second.record) }
                    .thenBy { it.first })
            .map { it.second }
    }

    fun winnerOf(show: GalleryShow): GalleryEntry? =
        if (show.entries.isEmpty()) null else placements(show)[0]

    // ---------------- the judges' notes ----------------
    // Every factual claim in a note is a slot filled
    // from the record: {names} (who is in frame), {loc}
    // (where), {weather} (the record's own weather
    // label), {theme} (the show's theme title). Template
    // bodies name nobody and no place — if a fact isn't
    // in the frame, no bank can say it (the yearbook
    // judges' discipline; the suite audits exactly that).

    private class Facts(rec: PhotoRecord, theme: GalTheme) {
        val loc: String = LOCATIONS[rec.locId]?.name ?: rec.locId
        val names: List<String> = rec.npcs.map { it.name }
        val namesLine: String = when {
            names.isEmpty() -> "a solo frame"
            names.size == 1 -> names[0]
            names.size == 2 -> "${names[0]} and ${names[1]}"
            else -> "${names[0]}, ${names[1]} and " +
                "${names.size - 2} more of the town"
        }
        val weather: String =
            (WEATHER[rec.weatherId]?.label ?: rec.weatherId)
                .lowercase()
        val theme: String = theme.title
    }

    private fun fill(t: String, f: Facts): String = t
        .replace("{names}", f.namesLine)
        .replace("{loc}", f.loc)
        .replace("{weather}", f.weather)
        .replace("{theme}", f.theme)

    /** A juror's line when THEY are in the frame being
     *  noted — keyed to that fact alone. */
    private val IN_FRAME_LINES: Map<String, String> = mapOf(
        "Victoria" to "Full disclosure: I am in this frame, " +
            "and I recused myself for a full ten seconds " +
            "before scoring it anyway. It earned it.",
        "Wendy" to "That's me in the frame, so — conflict " +
            "of interest, noted for the record. It's still " +
            "the warmest picture on the wall.",
        "Mephesto" to "I appear in this photograph. The " +
            "light on my face is doing something I have " +
            "chased for years. I scored the light.",
        "Jimmy" to "That's ME in the winning frame! I have " +
            "never been more f-famous. Hang it higher.",
        "Alexis" to "For the minutes: I am in this frame, " +
            "the horizon is level, and my score stands. " +
            "Filed under V, for vindicated.",
        "Big Gay Al" to "Well, I am IN this one, sugar, " +
            "and I look sensational. The score is for the " +
            "photograph. The glow is free.")

    private val NOTE_BANKS: Map<String, List<String>> = mapOf(
        "craft" to listOf(
            "{names} at {loc} — composed, balanced, and " +
                "somehow all looking the same direction. " +
                "Do you know how hard that is?",
            "A frame that knows where its edges are: " +
                "{names}, at {loc}, under {weather}. The " +
                "making is the argument."),
        "heart" to listOf(
            "{names}, unguarded at {loc}. The theme asked " +
                "for {theme} and the faces answered first.",
            "The wall has technically finer frames. It " +
                "does not have a warmer one than {names} " +
                "at {loc}."),
        "light" to listOf(
            "Shot at {loc} under {weather} — the light " +
                "did half the work and the frame had the " +
                "sense to let it.",
            "{theme}, caught at {loc}: {names}, lit like " +
                "the season meant it."))

    private val WINNER_EXTRA: Map<String, String> = mapOf(
        "craft" to " The wall's center frame, until the " +
            "next show hangs.",
        "heart" to " The wall's center frame, until the " +
            "next show hangs.",
        "light" to " The wall's center frame, until the " +
            "next show hangs.")

    /** The judges on an empty wall — nobody entered, and
     *  the book says so instead of faking a winner. */
    private val BLANK_LINES: Map<String, String> = mapOf(
        "Victoria" to "No entries this season — the wall " +
            "is just nice frames. Shoot something for us.",
        "Jimmy" to "No entries! The wall is just nice " +
            "frames. I drew a little sun on the theme " +
            "card. You're welcome.",
        "Alexis" to "No entries. The minutes record a " +
            "beautifully hung absence. Somebody take a " +
            "photograph next season.")

    fun blankLine(cal: GalleryCal): String =
        BLANK_LINES[chairFor(cal).name]
            ?: "No entries this season — the wall is just " +
                "nice frames."

    /** The jury's note for one placed entry — authored
     *  from the record's facts, deterministic by the
     *  record's id and the noting juror's lean. */
    fun noteFor(show: GalleryShow, entry: GalleryEntry,
                place: Int): String {
        val cal = GalleryCalendar.cal(show.year,
            Season.valueOf(show.season))
        val theme = themeById(show.themeId) ?: return ""
        val jury = juryFor(cal)
        val juror = jury[(place - 1).coerceIn(0, 2)]
        val f = Facts(entry.record, theme)
        if (juror.name in f.names) {
            IN_FRAME_LINES[juror.name]?.let { line ->
                return line + if (place == 1)
                    (WINNER_EXTRA[juror.lean] ?: "") else ""
            }
        }
        val bank = NOTE_BANKS[juror.lean] ?: return ""
        val t = bank[(entry.record.id.hashCode() and
            Int.MAX_VALUE) % bank.size]
        return fill(t, f) + if (place == 1)
            (WINNER_EXTRA[juror.lean] ?: "") else ""
    }

    // ---------------- records ----------------

    fun shows(state: GameState): List<GalleryShow> =
        state.galleryShows.toList()

    fun showFor(state: GameState, cal: GalleryCal):
            GalleryShow? =
        state.galleryShows.firstOrNull { it.id == cal.id }

    /** The show's record, created on first need — its
     *  theme and hang day derive from the calendar at
     *  creation and are stored, so a record always
     *  agrees with its season. */
    fun recordFor(state: GameState, cal: GalleryCal):
            GalleryShow {
        showFor(state, cal)?.let { return it }
        val theme = themeFor(cal)
        val show = GalleryShow(
            id = cal.id, year = cal.year,
            season = cal.season.name, themeId = theme.id,
            hangDay = cal.hangDay, closeDay = cal.closeDay)
        state.galleryShows += show
        return show
    }

    fun currentCycle(state: GameState):
            GalleryCalendar.Cycle =
        GalleryCalendar.cycleAt(state.absDay)

    fun currentCal(state: GameState): GalleryCal =
        currentCycle(state).cal

    fun dateLabel(absDay: Int): String = Dance.dateLabel(absDay)

    fun seasonLabel(season: Season): String = when (season) {
        Season.WINTER -> "Winter"
        Season.SPRING -> "Spring"
        Season.SUMMER -> "Summer"
        Season.FALL -> "Fall"
    }

    fun themeOf(show: GalleryShow): GalTheme? =
        themeById(show.themeId)

    // ---------------- phases ----------------

    const val PH_BETWEEN = "between"
    const val PH_SUBMIT = "submit"
    const val PH_HANGDAY = "hangday"
    const val PH_RUN = "run"
    const val PH_DONE = "done"

    /** Where the relevant show stands today — pure
     *  calendar, record or no record. */
    fun phaseOf(state: GameState): String {
        val cyc = currentCycle(state)
        if (cyc.next) return PH_BETWEEN
        val cal = cyc.cal
        return when {
            state.absDay < cal.hangDay -> PH_SUBMIT
            state.absDay == cal.hangDay -> PH_HANGDAY
            state.absDay <= cal.closeDay -> PH_RUN
            else -> PH_DONE
        }
    }

    /** Submissions stand open from the season's first
     *  morning until the hanging starts. */
    fun submissionsOpen(state: GameState): Boolean {
        val cyc = currentCycle(state)
        if (cyc.next) return false
        val cal = cyc.cal
        if (state.absDay < cal.hangDay) return true
        return state.absDay == cal.hangDay &&
            state.timeMin < HANG_FROM
    }

    /** The evening is the town's, not the hall's: a case
     *  climax, a finale, a holiday with its own beat —
     *  MovieNights' ladder reads them the same for
     *  every venue in town. */
    fun eveningDark(state: GameState): Boolean =
        when (MovieNights.darkReason(state)) {
            MovieNights.DARK_CASE, MovieNights.DARK_FINALE,
            MovieNights.DARK_HOLIDAY -> true
            else -> false
        }

    // ---------------- eligibility & entry ----------------

    /** A record competes in the season of its own
     *  dayKey — never re-dated, never borrowed. A
     *  record with an unreadable date competes nowhere
     *  (the yearbook's rule). */
    fun seasonEligible(cal: GalleryCal, rec: PhotoRecord):
            Boolean {
        val ord = ordinalOfKey(rec.dayKey)
        return ord in cal.seasonStartOrd..cal.seasonEndOrd
    }

    fun playerEntriesOf(show: GalleryShow):
            List<GalleryEntry> =
        show.entries.filter { it.entrant == YOU }

    private fun enteredPhotoIds(show: GalleryShow):
            Set<String> = show.entries.map { it.photoId }.toSet()

    /** Album photographs you could enter right now:
     *  this season's, not already on the wall. */
    fun eligibleAlbum(state: GameState, cal: GalleryCal,
                      show: GalleryShow): List<PhotoRecord> {
        val taken = enteredPhotoIds(show)
        return state.photos.filter {
            seasonEligible(cal, it) && it.id !in taken }
    }

    /** Darkroom prints you could lend right now: held
     *  (a gifted print lives with its holder), their
     *  photograph not already on this wall, and not
     *  already lent to another live show. A print is
     *  season-free — the darkroom's second life is
     *  exactly that an old photograph, developed by
     *  hand, can hang in a new season. */
    fun eligiblePrints(state: GameState, cal: GalleryCal,
                       show: GalleryShow):
            List<DarkroomPrint> {
        val taken = enteredPhotoIds(show)
        val lentHere = show.entries.map { it.printId }.toSet()
        return Darkroom.held(state).filter { p ->
            p.record.id !in taken && p.id !in lentHere &&
                !isLent(state, p.id, show.id) }
    }

    /** Is this print hanging in a live (unwrapped) show
     *  other than [exceptShowId]? While it is, the
     *  darkroom won't gift it or hang it at home — a
     *  print can't be in two places (Darkroom reads
     *  this before offering either). */
    fun isLent(state: GameState, printId: String,
               exceptShowId: String = ""): Boolean =
        state.galleryShows.any { s ->
            !s.wrapped && s.id != exceptShowId &&
                s.entries.any { it.printId == printId } }

    private fun entryGate(state: GameState,
                          show: GalleryShow): String? {
        if (show.locked) return "The frames are up — the " +
            "wall for this show is set."
        if (!submissionsOpen(state)) return "Submissions " +
            "aren't open right now."
        if (playerEntriesOf(show).size >= PLAYER_ENTRY_CAP)
            return "Two frames of yours is the wall's " +
                "limit for one show — withdraw one first."
        if (show.entries.size >= WALL_CAP) return "The " +
            "wall is full."
        return null
    }

    /** Enter an album photograph. The entry stores its
     *  own copy of the record as it stands right now. */
    fun enterAlbum(state: GameState, photoId: String):
            Boolean {
        val cal = currentCal(state)
        val show = recordFor(state, cal)
        if (entryGate(state, show) != null) return false
        val rec = eligibleAlbum(state, cal, show)
            .firstOrNull { it.id == photoId } ?: return false
        val copy = PhotoRecord.fromMap(rec.toMap())
            ?: return false
        show.entries = show.entries + GalleryEntry(
            YOU, KIND_ALBUM, rec.id, "", copy)
        return true
    }

    /** Lend a darkroom print. The entry is judged from
     *  the print's own frozen copy; the print itself is
     *  never consumed — it comes home when the show
     *  comes down. */
    fun enterPrint(state: GameState, printId: String):
            Boolean {
        val cal = currentCal(state)
        val show = recordFor(state, cal)
        if (entryGate(state, show) != null) return false
        val p = eligiblePrints(state, cal, show)
            .firstOrNull { it.id == printId } ?: return false
        val copy = PhotoRecord.fromMap(p.record.toMap())
            ?: return false
        show.entries = show.entries + GalleryEntry(
            YOU, KIND_PRINT, p.record.id, p.id, copy)
        return true
    }

    /** Take one of your frames back before the hanging.
     *  After the lock, the wall is the wall. */
    fun withdraw(state: GameState, photoId: String):
            Boolean {
        val cal = currentCal(state)
        val show = showFor(state, cal) ?: return false
        if (show.locked) return false
        val before = show.entries.size
        show.entries = show.entries.filter {
            !(it.entrant == YOU && it.photoId == photoId) }
        return show.entries.size != before
    }

    // ---------------- cast entries (derived, never fabricated) ----

    /** One cast member's history in the season's album:
     *  every record they commissioned, and every frame
     *  they stood in (a photobomb is not a sitting).
     *  Their entry is the best of it down a stated
     *  ladder — the followers the shot actually earned,
     *  then the fuller frame, then the earlier day. */
    fun castPool(state: GameState, cal: GalleryCal,
                 name: String): List<PhotoRecord> =
        state.photos.filter { rec ->
            seasonEligible(cal, rec) &&
                (rec.commissionAsker == name ||
                    rec.figures.any { f ->
                        f.name == name && f.kind != "player" &&
                            f.reaction != Photos.PHOTOBOMB })
        }

    private fun bestOf(pool: List<PhotoRecord>,
                       unused: Set<String>): PhotoRecord? =
        pool.filter { it.id !in unused }.sortedWith(
            compareByDescending<PhotoRecord> {
                it.followersGained }
                .thenByDescending { it.figures.size }
                .thenBy { ordinalOfKey(it.dayKey) }
                .thenBy { it.id }).firstOrNull()

    /** The cast wall, derived: who enters, and with
     *  what. Jurors sit their show out as entrants.
     *  Candidates rank by having commissioned work in
     *  the pool (they asked for a photograph — they're
     *  invested), then by their best frame's followers,
     *  then by name; each takes their best frame nobody
     *  has taken, so no photograph hangs twice. */
    fun castEntries(state: GameState, cal: GalleryCal,
                    taken: Set<String>): List<GalleryEntry> {
        val jurors = juryFor(cal).map { it.name }.toSet()
        data class Cand(val name: String,
                        val commissioned: Boolean,
                        val best: PhotoRecord)
        val cands = mutableListOf<Cand>()
        for (name in CharacterSystem.CAST.keys) {
            if (name in jurors) continue
            val pool = castPool(state, cal, name)
            if (pool.isEmpty()) continue
            val best = bestOf(pool, taken) ?: continue
            cands += Cand(name,
                pool.any { it.commissionAsker == name }, best)
        }
        val ranked = cands.sortedWith(
            compareByDescending<Cand> { it.commissioned }
                .thenByDescending { it.best.followersGained }
                .thenBy { it.name })
        val used = taken.toMutableSet()
        val out = mutableListOf<GalleryEntry>()
        for (c in ranked) {
            if (out.size >= CAST_ENTRY_CAP) break
            val rec = bestOf(castPool(state, cal, c.name), used)
                ?: continue
            val copy = PhotoRecord.fromMap(rec.toMap())
                ?: continue
            used += rec.id
            out += GalleryEntry(c.name, KIND_CAST, rec.id, "",
                copy)
        }
        return out
    }

    /** The wall as it will hang: your entries (already
     *  stored) plus the derived cast wall, trimmed to
     *  the wall's size. Pure — the agenda claim and the
     *  page read the same preview the lock will store. */
    fun previewWall(state: GameState, cal: GalleryCal,
                    show: GalleryShow): List<GalleryEntry> {
        if (show.locked) return show.entries
        val mine = playerEntriesOf(show)
        val room = (WALL_CAP - mine.size).coerceAtLeast(0)
        val cast = castEntries(state, cal,
            mine.map { it.photoId }.toSet()).take(room)
        return mine + cast
    }

    // ---------------- the lock & the hanging ----------------

    private fun plantTopic(name: String) {
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

    /** Lock the wall: cast entries derive and freeze,
     *  the town hears what it's arguing about, and the
     *  show is set. [quiet] when the evening was the
     *  town's — the frames go up without their set
     *  piece, and the record says so. */
    fun lockShow(state: GameState, show: GalleryShow,
                 quiet: Boolean) {
        if (show.locked) return
        val cal = GalleryCalendar.cal(show.year,
            Season.valueOf(show.season))
        val wall = previewWall(state, cal, show)
        show.entries = wall
        show.locked = true
        show.hungQuietly = quiet
        for (e in wall) {
            if (e.entrant != YOU) plantTopic(e.entrant)
            for (f in e.record.npcs) plantTopic(f.name)
        }
        val theme = themeOf(show)
        note(state, "🖼 The ${seasonLabel(cal.season)} " +
            "Gallery Show is hung at $VENUE_NAME — " +
            "“${theme?.title ?: ""}”, ${wall.size} frame" +
            (if (wall.size == 1) "" else "s") + " on the " +
            "wall through ${dateLabel(cal.closeDay)}.")
        townLogLine(state, "🖼 Gallery Show hung " +
            "(${seasonLabel(cal.season)}: " +
            "${theme?.title ?: ""}, ${wall.size} frames)")
    }

    /** Why tonight's hanging is dark, or null if the
     *  hall has its evening. */
    fun hangDarkText(state: GameState): String? {
        if (!eveningDark(state)) return null
        return when (MovieNights.darkReason(state)) {
            MovieNights.DARK_CASE ->
                "the hall's booked solid — town business"
            MovieNights.DARK_FINALE ->
                "the whole town's at the season finale"
            MovieNights.DARK_HOLIDAY -> {
                val name = state.holidayId?.let { id ->
                    HOLIDAYS.firstOrNull { it.id == id }?.name
                } ?: "the holiday"
                "the town is keeping $name"
            }
            else -> "the hall is dark tonight"
        }
    }

    // ---------------- the agenda claim ----------------

    /** Everyone the claim seats: the jury, the cast
     *  entrants, and every cast member standing in a
     *  hung frame (they come to see themselves). */
    fun attendees(state: GameState, cal: GalleryCal,
                  show: GalleryShow): Set<String> {
        val wall = previewWall(state, cal, show)
        val out = mutableSetOf<String>()
        out += juryFor(cal).map { it.name }
        for (e in wall) {
            if (e.entrant != YOU) out += e.entrant
            for (f in e.record.npcs) {
                if (f.kind == "cast" &&
                    f.reaction != Photos.PHOTOBOMB)
                    out += f.name
            }
        }
        return out
    }

    /** The Engine's gathering hook, seated after the
     *  film festival and before the sleepovers (the
     *  stack's twenty-second claim is the crew's, now —
     *  this one widened the stack to twenty-two): on
     *  hanging night the hall holds its people from six
     *  to nine, and on the show's days it holds them
     *  through the visiting afternoon. The deference
     *  split is the festival's own: a finale evening, a
     *  holiday beat, or a moment the town owns stands
     *  the claim down. No claim stands before the lock
     *  — until the frames are chosen, the hall is just
     *  a hall. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        val cyc = currentCycle(state)
        if (cyc.next) return null
        val cal = cyc.cal
        val show = recordFor(state, cal)
        val hanging = state.absDay == cal.hangDay &&
            timeMin in HANG_FROM until HANG_TO
        val visiting = state.absDay in
            (cal.hangDay + 1)..cal.closeDay &&
            timeMin in VISIT_FROM until VISIT_TO
        if (!hanging && !visiting) return null
        if (!show.locked && !hanging) return null
        if (npcName !in attendees(state, cal, show))
            return null
        if (Finales.windowAt(state) != null) return null
        val hid = state.holidayId
        if (hid != null && HolidayBeats.beatFor(hid) != null)
            return null
        if (Emergencies.momentOwned(state)) return null
        val theme = themeOf(show)
        return AgendaBlock(VENUE,
            if (hanging)
                "🖼 Hanging night at $VENUE_NAME — frames " +
                    "going up for “${theme?.title ?: ""}.”"
            else
                "🖼 At the Gallery Show — “${theme?.title ?: ""}” " +
                    "is on the wall at $VENUE_NAME.")
    }

    // ---------------- the hanging set piece ----------------

    const val ST_BETWEEN = "between"
    const val ST_SUBMIT = "submit"
    const val ST_HANGDAY = "hangday"
    const val ST_HANGING = "hanging"
    const val ST_QUIET = "quiet"
    const val ST_OPEN = "open"
    const val ST_RESULTS = "results"
    const val ST_CHAMPION = "champion"

    data class Status(val kind: String, val text: String)

    private fun kStep(day: Int) = "gallStep:$day"

    fun hangStep(state: GameState): Int =
        (state.life[kStep(state.absDay)] ?: 0).coerceIn(0, 3)

    /** The hall's whole script in one reading — the
     *  gallery dialog renders what this says. */
    fun hallStatus(state: GameState): Status {
        val cyc = currentCycle(state)
        val cal = cyc.cal
        val theme = themeFor(cal)
        if (cyc.next) {
            val champ = championShow(state)
            val champLine = if (champ != null) {
                val w = winnerOf(champ)
                "\n🖼 The wall still holds the last show's " +
                    "champion: ${w?.entrant ?: ""} — " +
                    "“${themeOf(champ)?.title ?: ""}”."
            } else ""
            return Status(ST_BETWEEN,
                "🖼 $VENUE_NAME is between shows. Next: " +
                    "the ${seasonLabel(cal.season)} show — " +
                    "“${theme.title}”. ${theme.blurb}\n" +
                    "Submissions open ${dateLabel(cal.announceDay)}; " +
                    "hanging night is ${dateLabel(cal.hangDay)}." +
                    champLine)
        }
        val show = showFor(state, cal)
        return when (phaseOf(state)) {
            PH_SUBMIT -> {
                val mine = show?.let { playerEntriesOf(it) }
                    ?: emptyList()
                val elig = show?.let {
                    eligibleAlbum(state, cal, it).size +
                        eligiblePrints(state, cal, it).size
                } ?: state.photos.count {
                    seasonEligible(cal, it) }
                Status(ST_SUBMIT,
                    "🖼 THE GALLERY SHOW — the " +
                        "${seasonLabel(cal.season)} show, " +
                        "“${theme.title}”. ${theme.blurb}\n" +
                        "The jury: ${juryFor(cal).joinToString(", ") { it.name }}. " +
                        "Submissions close at the hanging, " +
                        "${dateLabel(cal.hangDay)} at 6 PM. " +
                        "Entry is free — the arts line buys " +
                        "the frames.\n" +
                        (if (mine.isNotEmpty())
                            "Your frames so far: ${mine.size} " +
                                "of $PLAYER_ENTRY_CAP."
                        else if (elig > 0)
                            "You have $elig print" +
                                (if (elig == 1) "" else "s") +
                                " you could enter."
                        else
                            "You own nothing this season " +
                                "you could enter — the show " +
                                "will hang around you, and " +
                                "say so honestly."))
            }
            PH_HANGDAY -> when {
                show != null && show.locked &&
                    show.attendedHanging ->
                    Status(ST_RESULTS,
                        "🖼 The frames are up — the " +
                            "${seasonLabel(cal.season)} show, " +
                            "“${theme.title}”, opens tomorrow " +
                            "and hangs through " +
                            "${dateLabel(cal.closeDay)}. The " +
                            "jury's cards stay sealed until " +
                            "the show comes down.")
                show != null && show.locked &&
                    show.hungQuietly ->
                    Status(ST_QUIET, quietText(state, cal, show))
                eveningDark(state) ->
                    Status(ST_QUIET,
                        "🖼 Hanging night is the town's " +
                            "tonight — ${hangDarkText(state)}. " +
                            "The frames will go up quietly " +
                            "and be ready by morning; the " +
                            "show opens on schedule.")
                state.timeMin >= HANG_TO ->
                    Status(ST_QUIET,
                        "🖼 The hanging's done for tonight — " +
                            "the frames went up at sundown " +
                            "and the show opens tomorrow.")
                state.timeMin < HANG_FROM ->
                    Status(ST_SUBMIT,
                        "🖼 Hanging night is TONIGHT — the " +
                            "frames go up at 6, and the hall " +
                            "could use your hands. Submissions " +
                            "close when the hanging starts.")
                else -> Status(ST_HANGING,
                    "🖼 Hanging night — “${theme.title}”. " +
                        "The frames are leaning against the " +
                        "wall in their order, the ladder is " +
                        "out, and the jury is pretending not " +
                        "to peek at the scores they haven't " +
                        "written yet.")
            }
            PH_RUN -> {
                if (show == null || !show.locked)
                    Status(ST_OPEN,
                        "🖼 The ${seasonLabel(cal.season)} " +
                            "show — “${theme.title}” — is on " +
                            "the wall through " +
                            "${dateLabel(cal.closeDay)}.")
                else Status(ST_OPEN, wallText(state, cal, show))
            }
            else -> { // PH_DONE
                if (show != null && show.wrapped)
                    Status(ST_RESULTS, resultsText(state, show))
                else Status(ST_OPEN,
                    "🖼 The show has come down — the jury " +
                        "is reading its cards.")
            }
        }
    }

    private fun quietText(state: GameState, cal: GalleryCal,
                          show: GalleryShow): String =
        "🖼 The frames went up quietly — the evening " +
            "belonged to the town, so the hall keeper " +
            "hung the show at dawn instead. " +
            "${show.entries.size} frame" +
            (if (show.entries.size == 1) "" else "s") +
            ", “${themeOf(show)?.title ?: ""}”, on the " +
            "wall through ${dateLabel(cal.closeDay)}. " +
            "The jury's cards stay sealed until the " +
            "show comes down."

    /** The hung wall, read out loud: every frame with
     *  its entrant and its caption, off the entries'
     *  own frozen copies. */
    fun wallText(state: GameState, cal: GalleryCal,
                 show: GalleryShow): String {
        val theme = themeOf(show)
        val sb = StringBuilder()
        sb.append("🖼 THE GALLERY SHOW — " +
            "${seasonLabel(cal.season)}, " +
            "“${theme?.title ?: ""}”\n")
        sb.append("On the wall through " +
            "${dateLabel(cal.closeDay)}. The jury: " +
            juryFor(cal).joinToString(", ") { it.name } +
            " — their cards stay sealed until the show " +
            "comes down.\n")
        if (show.entries.isEmpty()) {
            sb.append("\nThe wall is bare — no entries " +
                "this season. ${blankLine(cal)}")
            return sb.toString()
        }
        for ((i, e) in show.entries.withIndex()) {
            sb.append("\n${i + 1}. ${e.entrant}" +
                (if (e.kind == KIND_PRINT)
                    " — a darkroom print, lent" else "") +
                "\n   ${Photos.captionFor(e.record)}")
        }
        return sb.toString()
    }

    /** The judged wall, read out loud: placements,
     *  scores, and the jury's notes — every fact off
     *  the entries' own copies. */
    fun resultsText(state: GameState, show: GalleryShow):
            String {
        val cal = GalleryCalendar.cal(show.year,
            Season.valueOf(show.season))
        val theme = themeOf(show)
        val sb = StringBuilder()
        sb.append("🖼 THE GALLERY SHOW — " +
            "${seasonLabel(cal.season)}, " +
            "“${theme?.title ?: ""}” — JUDGED\n")
        if (show.entries.isEmpty()) {
            sb.append(blankLine(cal))
            return sb.toString()
        }
        val placed = placements(show)
        for ((i, e) in placed.withIndex()) {
            val medal = when (i) {
                0 -> "🏆"
                1 -> "🥈"
                2 -> "🥉"
                else -> "·"
            }
            sb.append("\n$medal ${i + 1}. ${e.entrant} — " +
                "${scoreOf(show, e)} points" +
                (if (e.kind == KIND_PRINT)
                    " (darkroom print)" else "") +
                "\n   ${Photos.captionFor(e.record)}")
            if (i < 3) sb.append("\n   Jury: " +
                noteFor(show, e, i + 1))
        }
        val w = placed[0]
        sb.append("\n\n${if (w.entrant == YOU)
            "Your frame holds the wall's center until " +
                "the next show hangs."
        else
            "${w.entrant}'s frame holds the wall's " +
                "center until the next show hangs."}")
        return sb.toString()
    }

    // ---------------- the hanging beats ----------------

    /** The hanging, in three beats — the frames in
     *  their order, the ladder moment, the lights.
     *  Every name in them is read off the locked wall.
     *  Null when no hanging is live for you. */
    fun hangBeat(state: GameState): String? {
        val cyc = currentCycle(state)
        if (cyc.next) return null
        val cal = cyc.cal
        if (state.absDay != cal.hangDay) return null
        if (state.timeMin !in HANG_FROM until HANG_TO)
            return null
        if (eveningDark(state)) return null
        val show = recordFor(state, cal)
        if (!show.locked) lockShow(state, show, quiet = false)
        if (show.attendedHanging) return null
        val theme = themeOf(show)
        val wall = show.entries
        val chair = chairFor(cal)
        return when (hangStep(state)) {
            0 -> {
                val first = wall.take(3).joinToString("; ") {
                    "${it.entrant}: ${Photos.captionFor(it.record)}"
                }
                "🖼 THE HANGING — the hall smells of paper " +
                    "and picture wire. ${chair.name} reads " +
                    "the wall order off a clipboard: " +
                    "${wall.size} frame" +
                    (if (wall.size == 1) "" else "s") +
                    " for “${theme?.title ?: ""}”." +
                    (if (wall.isEmpty())
                        " The clipboard is mostly doodles. " +
                            "The wall, this season, is a " +
                            "study in negative space."
                    else " First up: $first")
            }
            1 -> {
                val mid = wall.getOrNull(wall.size / 2)
                "🖼 THE LADDER — a frame goes up crooked, " +
                    "comes down, goes up straight. " +
                    (if (mid != null)
                        "The middle of the wall is " +
                            "${mid.entrant}'s — " +
                            Photos.captionFor(mid.record)
                    else
                        "The theme card goes up in the " +
                            "middle, alone and unbothered") +
                    ". Somebody's level is older than the " +
                    "hall and twice as certain."
            }
            else -> {
                val w = winnerOf(show)
                "🖼 THE LIGHTS — the hall's spots come on " +
                    "one bank at a time, and the wall " +
                    "becomes a wall. ${chair.name} walks " +
                    "it end to end, saying nothing, which " +
                    "the room agrees is the scariest " +
                    "review of all. The show opens " +
                    "tomorrow and hangs through " +
                    "${dateLabel(cal.closeDay)}" +
                    (if (w != null)
                        " — and no, nobody knows yet. " +
                            "That's the point of sealed cards."
                    else "") + "."
            }
        }
    }

    /** Move the hanging along; past the last beat, the
     *  evening is kept and the record says so. */
    fun advanceHang(state: GameState) {
        val cyc = currentCycle(state)
        if (cyc.next) return
        val cal = cyc.cal
        val show = showFor(state, cal) ?: return
        if (!show.locked || show.attendedHanging) return
        val step = hangStep(state)
        if (step >= 2) {
            show.attendedHanging = true
            state.life.remove(kStep(state.absDay))
        } else {
            state.life[kStep(state.absDay)] = step + 1
        }
    }

    /** Standing in the hung show marks the visit — the
     *  played-through half of the wrap's pay rule. The
     *  dialog calls this when the wall is read during
     *  the run. */
    fun visitShow(state: GameState) {
        val cyc = currentCycle(state)
        if (cyc.next) return
        val show = showFor(state, cyc.cal) ?: return
        if (!show.locked || show.wrapped) return
        if (state.absDay in (cyc.cal.hangDay + 1)..
            cyc.cal.closeDay) show.visitedShow = true
    }

    // ---------------- the champion ----------------

    /** The most recent wrapped show that had a wall —
     *  its winner's frame holds the hall until the next
     *  show hangs. */
    fun championShow(state: GameState): GalleryShow? =
        state.galleryShows.filter {
            it.wrapped && it.entries.isNotEmpty() }
            .maxByOrNull { it.wrapDay }

    fun championEntry(state: GameState): GalleryEntry? =
        championShow(state)?.let { winnerOf(it) }

    // ---------------- the wall, as data (GameView paints it) ----

    data class WallView(
        val mode: String,          // show|submit|champion|card|none
        val theme: GalTheme?,
        val frames: List<GalleryEntry>,
        val champion: GalleryEntry?)

    /** What the hall's wall carries right now: a locked
     *  show's frames; before the lock, the theme card
     *  and any of your frames already in; between
     *  shows, the standing champion; and ahead of the
     *  very first show, just the theme card. */
    fun wallView(state: GameState): WallView {
        val cyc = currentCycle(state)
        val cal = cyc.cal
        val show = showFor(state, cal)
        if (!cyc.next && show != null && show.locked &&
            !show.wrapped) {
            return WallView("show", themeOf(show),
                show.entries, null)
        }
        if (!cyc.next && show != null && !show.locked &&
            playerEntriesOf(show).isNotEmpty()) {
            return WallView("submit", themeOf(show),
                playerEntriesOf(show), championEntry(state))
        }
        val champ = championEntry(state)
        if (champ != null) {
            return WallView("champion", themeFor(cal),
                emptyList(), champ)
        }
        if (!cyc.next) {
            return WallView("card", themeFor(cal),
                emptyList(), null)
        }
        return WallView("none", null, emptyList(), null)
    }

    // ---------------- surfaces ----------------

    /** The Look text at City Hall: the poster's line in
     *  the submission window, the hanging tonight, the
     *  hung wall, the champion between shows. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != VENUE) return null
        val cyc = currentCycle(state)
        val cal = cyc.cal
        val theme = themeFor(cal)
        if (cyc.next) {
            val champ = championShow(state)
            return if (champ != null) {
                val w = winnerOf(champ)
                "🖼 The Gallery Show's champion frame " +
                    "holds the hall's wall: ${w?.entrant} " +
                    "took the ${seasonLabel(champSeason(champ))} " +
                    "show. Next: the " +
                    "${seasonLabel(cal.season)} show — " +
                    "“${theme.title}”, submissions open " +
                    "${dateLabel(cal.announceDay)}."
            } else {
                "🖼 A card on the hall's wall: THE GALLERY " +
                    "SHOW — the ${seasonLabel(cal.season)} " +
                    "show, “${theme.title}”, takes entries " +
                    "from ${dateLabel(cal.announceDay)}. " +
                    "Yours, from your own album."
            }
        }
        val show = showFor(state, cal)
        return when (phaseOf(state)) {
            PH_SUBMIT -> "🖼 A poster in the hall: THE " +
                "GALLERY SHOW — the " +
                "${seasonLabel(cal.season)} show, " +
                "“${theme.title}”. ${theme.blurb} Entries " +
                "from your own album (or a darkroom print " +
                "you hold), at the show wall inside, until " +
                "the hanging on ${dateLabel(cal.hangDay)}."
            PH_HANGDAY -> if (eveningDark(state))
                "🖼 Hanging night at the hall — but the " +
                    "evening is the town's (${hangDarkText(state)}). " +
                    "The frames go up quietly by morning."
            else
                "🖼 Hanging night TONIGHT at the hall — " +
                    "the ${seasonLabel(cal.season)} show, " +
                    "“${theme.title}”, goes up at 6. Inside, " +
                    "at the show wall, if you're coming to " +
                    "help hang it."
            PH_RUN -> "🖼 The Gallery Show is UP at the " +
                "hall — “${theme.title}”, " +
                "${show?.entries?.size ?: 0} frames, " +
                "through ${dateLabel(cal.closeDay)}. The " +
                "jury's cards stay sealed until it comes " +
                "down."
            else -> "🖼 The ${seasonLabel(cal.season)} show " +
                "has come down — the jury is reading its " +
                "cards. The word lands tomorrow."
        }
    }

    private fun champSeason(show: GalleryShow): Season =
        Season.valueOf(show.season)

    /** The morning report's line: the poster's morning,
     *  hanging day, and the wrap's answer on the morning
     *  it lands. */
    fun morningNote(state: GameState): String? {
        for (s in state.galleryShows) {
            if (s.wrapped && s.wrapDay == state.absDay) {
                val cal = GalleryCalendar.cal(s.year,
                    Season.valueOf(s.season))
                val w = winnerOf(s)
                return if (w == null)
                    "🖼 The ${seasonLabel(cal.season)} " +
                        "Gallery Show is judged: no entries " +
                        "this season — the wall was bare, " +
                        "and beautifully lit."
                else
                    "🖼 The ${seasonLabel(cal.season)} " +
                        "Gallery Show is judged: " +
                        "${w.entrant} took the wall for " +
                        "“${themeOf(s)?.title ?: ""}” on " +
                        "${scoreOf(s, w)} points. The jury " +
                        "has gone back to being neighbors."
            }
        }
        val cyc = currentCycle(state)
        if (cyc.next) return null
        val cal = cyc.cal
        val theme = themeFor(cal)
        if (state.absDay == cal.announceDay) {
            return "🖼 POSTER UP at $VENUE_NAME: THE " +
                "GALLERY SHOW — the " +
                "${seasonLabel(cal.season)} show, " +
                "“${theme.title}”. ${theme.blurb} Entries " +
                "from your own album until the hanging, " +
                "${dateLabel(cal.hangDay)}."
        }
        if (state.absDay == cal.hangDay) {
            return "🖼 Hanging night at $VENUE_NAME — the " +
                "${seasonLabel(cal.season)} Gallery Show " +
                "(“${theme.title}”) goes up at 6. The " +
                "jury: ${juryFor(cal).joinToString(", ") { it.name }}."
        }
        return null
    }

    /** The Gazette's gallery lines — TOWN NEWS only,
     *  never a headline tier (the ladder stands at ten;
     *  a photo show is town news like the dance, the
     *  talent show and the festival before it): the
     *  announcement in its week, and the winner in the
     *  closing week's edition, both derived whole off
     *  the calendar and the show's record. */
    fun paperLine(state: GameState, week: Int): String? {
        for (s in state.galleryShows) {
            if (!s.wrapped) continue
            val cal = GalleryCalendar.cal(s.year,
                Season.valueOf(s.season))
            if (Jobs.weekIndex(cal.closeDay) != week) continue
            val w = winnerOf(s)
            return if (w == null)
                "🖼 The ${seasonLabel(cal.season)} Gallery " +
                    "Show came down at $VENUE_NAME: no " +
                    "entries this season — the jury judged " +
                    "a bare wall, and found the lighting " +
                    "excellent."
            else
                "🖼 The ${seasonLabel(cal.season)} Gallery " +
                    "Show closed at $VENUE_NAME: " +
                    "${w.entrant} took the wall for " +
                    "“${themeOf(s)?.title ?: ""}” on " +
                    "${scoreOf(s, w)} jury points. The " +
                    "jury: ${juryFor(cal).joinToString(", ") { it.name }}." +
                    (if (w.entrant == YOU)
                        " The champion frame is yours until " +
                            "the next show hangs."
                    else "")
        }
        val cyc = currentCycle(state)
        val cal = cyc.cal
        if (Jobs.weekIndex(cal.announceDay) == week) {
            val theme = themeFor(cal)
            return "🖼 The Gallery Show is coming to " +
                "$VENUE_NAME — the " +
                "${seasonLabel(cal.season)} show, " +
                "“${theme.title}”. Entries from your own " +
                "album (a held darkroom print may be " +
                "lent); hanging night ${dateLabel(cal.hangDay)}, " +
                "the show up through " +
                "${dateLabel(cal.closeDay)}. The jury: " +
                juryFor(cal).joinToString(", ") { it.name } + "."
        }
        return null
    }

    /** The show's ONE morning-after text, in the
     *  harvest's fact discipline: from the winner when
     *  a neighbor won, from the jury's chair when you
     *  did — fact-only, on the wrap's own morning,
     *  moving nothing. An empty show sends nothing:
     *  nothing happened to anyone. */
    fun phoneBeat(state: GameState):
            Triple<String, String, String>? {
        for (s in state.galleryShows) {
            if (!s.wrapped || s.wrapDay != state.absDay)
                continue
            val w = winnerOf(s) ?: return null
            val cal = GalleryCalendar.cal(s.year,
                Season.valueOf(s.season))
            val theme = themeOf(s)?.title ?: ""
            val sat = s.attendedHanging || s.visitedShow
            return if (w.entrant == YOU) {
                val chair = chairFor(cal).name
                Triple(chair, "judged",
                    "the gallery show's judged — YOUR " +
                        "frame took the wall for " +
                        "“$theme.” champion till the next " +
                        "hanging. " +
                        (if (sat) "you were there for it. "
                        else "you missed the hanging. ") +
                        "the journal has the wall.")
            } else {
                Triple(w.entrant, "judged",
                    "the gallery show's judged — my " +
                        "frame took the wall for " +
                        "“$theme” on ${scoreOf(s, w)} " +
                        "points. " +
                        (if (sat) "you were there — thanks " +
                            "for coming to see it. "
                        else "you missed it. it's the " +
                            "champion frame at the hall " +
                            "if you want a look. ") +
                        "the journal has the wall.")
            }
        }
        return null
    }

    /** The 🖼 GALLERY SHOW Journal page: the live show
     *  (theme, window, your frames, the wall once it's
     *  up, the judging once it's down), the champion,
     *  and the wrapped shows on the record. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val wrappedCount = state.galleryShows.count {
            it.wrapped }
        out += "🖼 GALLERY SHOW — $wrappedCount on the record"
        out += "Four shows a year at $VENUE_NAME (City " +
            "Hall) — one a season, a theme derived from " +
            "the season, entries from your real album and " +
            "your held darkroom prints, a jury of three, " +
            "and the winner's frame holding the wall " +
            "until the next hanging. Entry is free; the " +
            "winner takes $WINNER_COINS 🪙 from the arts " +
            "line when it's you, and the town's respect " +
            "either way."
        val cyc = currentCycle(state)
        val cal = cyc.cal
        val theme = themeFor(cal)
        if (cyc.next) {
            out += "Next: the ${seasonLabel(cal.season)} " +
                "show — “${theme.title}”. ${theme.blurb} " +
                "Submissions open ${dateLabel(cal.announceDay)}; " +
                "hanging night ${dateLabel(cal.hangDay)}; " +
                "the jury: ${juryFor(cal).joinToString(", ") { it.name }}."
        } else {
            val show = showFor(state, cal)
            out += "This season: the " +
                "${seasonLabel(cal.season)} show — " +
                "“${theme.title}”. ${theme.blurb}"
            out += "The jury: ${juryFor(cal).joinToString(", ") {
                "${it.name} (${it.lean})" }} — " +
                juryFor(cal).joinToString(" ") { it.taste }
            when (phaseOf(state)) {
                PH_SUBMIT -> {
                    val mine = show?.let {
                        playerEntriesOf(it) } ?: emptyList()
                    val elig = show?.let {
                        eligibleAlbum(state, cal, it).size +
                            eligiblePrints(state, cal, it).size
                    } ?: 0
                    out += "Submissions are open until the " +
                        "hanging, ${dateLabel(cal.hangDay)} " +
                        "at 6 PM — enter at the show wall " +
                        "inside City Hall."
                    out += if (mine.isNotEmpty())
                        "Your frames in: ${mine.size} of " +
                            "$PLAYER_ENTRY_CAP — " +
                            mine.joinToString("; ") {
                                Photos.captionFor(it.record) }
                    else if (elig > 0)
                        "Nothing of yours is in yet — $elig " +
                            "eligible print" +
                            (if (elig == 1) "" else "s") +
                            " in your album and print box."
                    else
                        "You own nothing eligible this " +
                            "season — take photographs; the " +
                            "season is young and the wall " +
                            "is patient."
                }
                PH_HANGDAY -> out += "Hanging night is " +
                    "TONIGHT (6–9 PM) at the hall — the " +
                    "frames go up, and the show opens " +
                    "tomorrow through ${dateLabel(cal.closeDay)}."
                PH_RUN -> {
                    val wall = show?.let {
                        previewWall(state, cal, it) }
                        ?: emptyList()
                    out += "The show is UP through " +
                        "${dateLabel(cal.closeDay)} — " +
                        "${wall.size} frame" +
                        (if (wall.size == 1) "" else "s") +
                        " on the wall."
                    for ((i, e) in wall.withIndex()) {
                        out += "${i + 1}. ${e.entrant}" +
                            (if (e.kind == KIND_PRINT)
                                " (darkroom print)" else "") +
                            " — ${Photos.captionFor(e.record)}"
                    }
                }
                else -> {
                    if (show != null && show.wrapped) {
                        out += resultsSummary(show)
                    } else {
                        out += "The show has come down — " +
                            "the jury is reading its cards."
                    }
                }
            }
        }
        val champ = championShow(state)
        if (champ != null && !(!cyc.next &&
                showFor(state, cal)?.wrapped == true &&
                showFor(state, cal) == champ)) {
            val w = winnerOf(champ)
            if (w != null) {
                out += "👑 The champion frame at the hall: " +
                    "${w.entrant} — " +
                    Photos.captionFor(w.record) +
                    " (${seasonLabel(champSeason(champ))}, " +
                    "“${themeOf(champ)?.title ?: ""}”)."
            }
        }
        val past = state.galleryShows.filter { it.wrapped }
            .sortedByDescending { it.wrapDay }
        if (past.isNotEmpty()) {
            out += "On the record:"
            for (s in past) {
                val scal = GalleryCalendar.cal(s.year,
                    Season.valueOf(s.season))
                out += "· ${seasonLabel(scal.season)} " +
                    "${s.year} — “${themeOf(s)?.title ?: ""}”: " +
                    s.winnersLine
            }
        }
        return out
    }

    private fun resultsSummary(show: GalleryShow): String {
        val w = winnerOf(show)
        return if (w == null)
            "Judged: no entries — the wall was bare. " +
                blankLine(GalleryCalendar.cal(show.year,
                    Season.valueOf(show.season)))
        else
            "Judged: ${w.entrant} took the wall on " +
                "${scoreOf(show, w)} points — " +
                Photos.captionFor(w.record)
    }

    // ---------------- lifecycle (the Engine's rollover calls) ----

    /** The one-line index summary a show reduces to —
     *  composed at the wrap, from the judged wall. */
    fun winnersLineOf(show: GalleryShow): String {
        val w = winnerOf(show)
        val theme = themeOf(show)
        return if (w == null)
            "the wall was bare — no entries"
        else
            "🏆 ${w.entrant} took “${theme?.title ?: ""}” " +
                "with ${Photos.captionFor(w.record)} " +
                "(${scoreOf(show, w)} points)"
    }

    /** Reduce the oldest full shows past the shelf cap
     *  to their index lines (the yearbook's rule). */
    private fun reduceShelf(state: GameState) {
        var full = state.galleryShows.count {
            it.wrapped && !it.indexOnly }
        var i = 0
        while (full > FULL_SHOWS &&
            i < state.galleryShows.size) {
            val s = state.galleryShows[i]
            if (s.wrapped && !s.indexOnly) {
                s.indexOnly = true
                s.entries = emptyList()
                full--
            }
            i++
        }
    }

    /** Did this save live any of [cal]'s season? A show
     *  from before the save's first day never gets a
     *  record — you can't have a history you weren't
     *  here for (the yearbook's guard). */
    private fun livedSeason(state: GameState,
                            cal: GalleryCal): Boolean {
        val dayOne = Commissions.keyForAbsDay(state, 0)
        val ord = ordinalOfKey(dayOne)
        return ord > 0 && ord <= cal.seasonEndOrd
    }

    /** The show's mornings, called by Engine.onNewDay
     *  beside the festival's, before the paper and the
     *  phone (their lines derive from these facts):
     *  on the season's first morning the record opens
     *  (the poster's ticker is the morning report's);
     *  on the morning after hanging night an unlocked
     *  show locks — quietly, if the evening wasn't
     *  kept; and on the first morning after the show
     *  comes down, the wrap lands exactly once: the
     *  winner derived, the pays settled (your coins
     *  from the arts line when you won; a cast winner's
     *  +1 only if you kept the hanging or came to see
     *  the show), the index line written, the shelf
     *  reduced. Yesterday's hanging-beat key is pruned. */
    fun dailyRollover(state: GameState) {
        val today = state.absDay
        // the record opens on the season's first morning
        val cyc = GalleryCalendar.cycleAt(today)
        if (!cyc.next && today == cyc.cal.announceDay &&
            livedSeason(state, cyc.cal)) {
            recordFor(state, cyc.cal)
        }
        // settle every show whose days have run
        val cals = mutableListOf<GalleryCal>()
        for (y in (state.gameYear - 1)..(state.gameYear + 1)) {
            for (s in Season.values()) {
                cals += GalleryCalendar.cal(y, s)
            }
        }
        for (cal in cals) {
            if (today <= cal.hangDay) continue
            if (today > cal.wrapDay + WRAP_TAIL) continue
            if (!livedSeason(state, cal)) continue
            val show = recordFor(state, cal)
            if (!show.locked) {
                lockShow(state, show,
                    quiet = !show.attendedHanging)
            }
            if (today >= cal.wrapDay && !show.wrapped) {
                show.wrapped = true
                show.wrapDay = today
                show.winnersLine = winnersLineOf(show)
                val w = winnerOf(show)
                if (w != null && !show.paid) {
                    if (w.entrant == YOU) {
                        Minigames.awardCoins(state, WINNER_COINS)
                    } else if (show.attendedHanging ||
                        show.visitedShow) {
                        Engine().relAdjust(state, w.entrant,
                            WINNER_PAY)
                    }
                    show.paid = true
                }
                if (w != null) {
                    note(state, "🖼 The " +
                        "${seasonLabel(cal.season)} Gallery " +
                        "Show is judged: ${w.entrant} took " +
                        "the wall for " +
                        "“${themeOf(show)?.title ?: ""}” on " +
                        "${scoreOf(show, w)} points.")
                    townLogLine(state, "🖼 Gallery Show " +
                        "judged (${seasonLabel(cal.season)} " +
                        "${cal.year}): ${w.entrant}, " +
                        "${scoreOf(show, w)} points")
                }
                reduceShelf(state)
            }
        }
        // prune the hanging-beat key — the record
        // carries the permanent marks
        state.life.keys.removeAll {
            it.startsWith("gallStep:") &&
                (it.substringAfter(':').toIntOrNull()
                    ?: Int.MAX_VALUE) < today
        }
    }
}

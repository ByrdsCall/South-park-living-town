package com.hunt3r.southparktown

// ============================================================
// The town yearbook (v0.8.0d, Queue #8 item 4) — every year,
// the Elementary prints your album back to you.
//
// THE TURN. This town's school year ends with the spring:
// all summer the Engine rewrites every Elementary agenda
// block away ("Summer break! School is a rumor"), and
// Finales already fixes spring's last day at May 31. So the
// yearbook settles on the rollover into JUNE 1 — the first
// morning of summer — judging the school year that ended
// the day before: the twelve months Jun 1 .. May 31, named
// for the spring it closes in (the "2026–27" book settles on
// Jun 1, 2027). Publication rides Engine.onNewDay like every
// module's rollover, announces itself on the events ticker
// (which is the morning report's overnight block), and
// settles EXACTLY once per year — the stored volume is the
// guard. A save that slept through the turn (or predates
// this feature) publishes its most recent ended year on the
// next rollover instead, provided the save actually lived
// that year; a year the save never lived never gets a book.
//
// RECORDS ONLY. Judging reads PhotoRecords and nothing else:
// six categories, each scored by a stated formula over the
// record's own fields (figures, poses, composition, place,
// weather, sky, the commissioned stamp, followers). No
// randomness at judging time — the same album makes the
// same book, forever, on every device. A category with no
// qualifying record that year stays honestly blank ("no
// entry — the page is just a nice border"); a year with no
// photos at all publishes covers only. The book never
// invents, borrows, or re-dates a photo: a record competes
// in exactly one year — the school year of its own dayKey.
//
// CURATION. During the year you can STAR a photo from the
// Album (PhotoRecord.starred, an additive field like the
// commission stamp). A star is worth STAR_BONUS points in
// every category the photo enters — a nudge, not a gate:
// unstarred photos compete on their fields alone. Starred
// photos also never fade from the album (the cap sheds
// unstarred, uncommissioned photos first, like prints).
//
// THE VOLUME. Winners are stored AS RECORD COPIES inside
// the volume (state.yearbooks, one additive save key), so a
// page renders its winner through PhotoView even after the
// album cap has faded the original. The shelf keeps the
// newest FULL_VOLUMES volumes whole; older ones reduce to
// a one-line index entry (year, photo count, winners) —
// the established cap pattern. Three cast editors judge:
// Wendy (editor-in-chief), Jimmy (photo desk), Cartman
// (critic-at-large), rotating by year and category. Their
// notes are authored templates whose every factual claim
// is a slot filled from the winning record itself — a name,
// the place, the weather, the sky — so a note can never
// praise people or weather that aren't in the frame (the
// suite checks exactly that, over every note the banks can
// produce).
//
// THE ARGUMENT. Publication plants a memory topic: the
// cast in winning frames carry "yearbook famous", the cast
// in runner-up frames carry "yearbook snub" (Threads has
// banks for both — winners bask, the snubbed grumble, in
// character). Like every topic, it fades as new talk pushes
// it out of the six-slot memory. Publication also raises
// ONE derived house decor piece (House reads the shelf).
// ============================================================

data class YearbookEntry(
    val categoryId: String,
    val judge: String,
    val score: Int,
    val note: String,
    /** The winning record, copied at judging time so the
     *  page survives the album cap. Null = blank page. */
    val record: PhotoRecord?
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "categoryId" to categoryId, "judge" to judge,
        "score" to score, "note" to note,
        "record" to record?.toMap()
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(m: Map<String, Any?>): YearbookEntry? {
            val cat = m["categoryId"]?.toString() ?: return null
            val rec = (m["record"] as? Map<String, Any?>)
                ?.let { PhotoRecord.fromMap(it) }
            return YearbookEntry(
                categoryId = cat,
                judge = m["judge"]?.toString() ?: "",
                score = (m["score"] as? Number)?.toInt() ?: 0,
                note = m["note"]?.toString() ?: "",
                record = rec
            )
        }
    }
}

data class YearbookVolume(
    /** The school year's ending year: the book named
     *  "2026–27" has year 2027 (it closed May 31, 2027). */
    val year: Int,
    /** Records judged (the year's album, at the turn). */
    val photoCount: Int,
    val publishedKey: String,
    val publishedAbsDay: Int,
    /** A thin year — judged proudly anyway. */
    val pamphlet: Boolean,
    /** Reduced by the shelf cap: index line only. */
    val indexOnly: Boolean,
    /** The one-line index summary (survives reduction). */
    val winnersLine: String,
    val entries: List<YearbookEntry>
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "year" to year, "photoCount" to photoCount,
        "publishedKey" to publishedKey,
        "publishedAbsDay" to publishedAbsDay,
        "pamphlet" to pamphlet, "indexOnly" to indexOnly,
        "winnersLine" to winnersLine,
        "entries" to entries.map { it.toMap() }
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(m: Map<String, Any?>): YearbookVolume? {
            val year = (m["year"] as? Number)?.toInt() ?: return null
            val entries = (m["entries"] as? List<Any?>)?.mapNotNull {
                (it as? Map<String, Any?>)?.let { em ->
                    YearbookEntry.fromMap(em) }
            } ?: emptyList()
            return YearbookVolume(
                year = year,
                photoCount = (m["photoCount"] as? Number)?.toInt() ?: 0,
                publishedKey = m["publishedKey"]?.toString() ?: "",
                publishedAbsDay =
                    (m["publishedAbsDay"] as? Number)?.toInt() ?: -1,
                pamphlet = m["pamphlet"] as? Boolean ?: false,
                indexOnly = m["indexOnly"] as? Boolean ?: false,
                winnersLine = m["winnersLine"]?.toString() ?: "",
                entries = entries
            )
        }
    }
}

object Yearbook {

    // ---------------- the dials (README tuning ledger) ----------------

    /** A starred photo's judging bonus, in EVERY category
     *  it enters. The star is the player's one editorial
     *  voice — worth a nudge, never a coronation: a runaway
     *  field still beats a starred dud on raw score. */
    const val STAR_BONUS = 15

    /** How many volumes the shelf keeps whole. Older ones
     *  reduce to their index line. */
    const val FULL_VOLUMES = 3

    /** A year with fewer judged photos than this prints as
     *  a pamphlet — and says so, proudly. */
    const val THIN_COUNT = 6

    /** Days after publication the town keeps arguing (the
     *  page says so, and the planted topics are fresh). */
    const val ARGUMENT_DAYS = 4

    /** The memory topics publication plants. */
    const val TOPIC_WIN = "yearbook famous"
    const val TOPIC_SNUB = "yearbook snub"

    /** The game's first day (Engine: Day 1 = Oct 1, 2026) —
     *  the epoch a legacy record's absDay derives from when
     *  the record predates the absDay field. */
    const val EPOCH_KEY = "20261001"

    // ---------------- the editors ----------------

    data class Editor(val name: String, val role: String)

    val EDITORS: List<Editor> = listOf(
        Editor("Wendy", "editor-in-chief"),
        Editor("Jimmy", "photo desk"),
        Editor("Cartman", "critic-at-large")
    )

    /** Who judges [categoryIndex] in [year]'s book — a
     *  plain rotation, so every volume spreads its pages
     *  across the whole masthead. */
    fun judgeFor(year: Int, categoryIndex: Int): Editor =
        EDITORS[(year + categoryIndex) % EDITORS.size]

    // ---------------- the categories ----------------

    data class YbCategory(val id: String, val title: String,
                          val emoji: String)

    val CAT_GROUP = YbCategory("group", "Best Group Shot", "📸")
    val CAT_SELFIE = YbCategory("selfie", "Best Selfie", "🤳")
    val CAT_SKY = YbCategory("sky", "Most Dramatic Sky", "🌌")
    val CAT_COMMISSIONED =
        YbCategory("commissioned", "Best Commissioned Print", "🖨")
    val CAT_BUSY = YbCategory("busy", "Busiest Scene", "🎪")
    val CAT_FAVORITE = YbCategory("favorite", "Town Favorite", "🏆")

    val CATEGORIES: List<YbCategory> = listOf(
        CAT_GROUP, CAT_SELFIE, CAT_SKY,
        CAT_COMMISSIONED, CAT_BUSY, CAT_FAVORITE)

    fun categoryById(id: String): YbCategory? =
        CATEGORIES.firstOrNull { it.id == id }

    // ---------------- the calendar ----------------

    private fun keyOf(y: Int, m: Int, d: Int) =
        "%04d%02d%02d".format(y, m, d)

    /** The school year a date key belongs to, named by its
     *  ending spring: Jun 1 .. May 31. Null for a key that
     *  isn't a real date. */
    fun schoolYearOf(key: String): Int? {
        if (key.length != 8 || ordinalOfKey(key) <= 0) return null
        val y = key.substring(0, 4).toIntOrNull() ?: return null
        val m = key.substring(4, 6).toIntOrNull() ?: return null
        return if (m >= 6) y + 1 else y
    }

    /** The school year in progress today. */
    fun currentYear(state: GameState): Int =
        schoolYearOf(state.todayKey) ?: state.gameYear

    /** The most recent school year that has ENDED as of
     *  today: from June on, the one that closed this May;
     *  before June, the one that closed last May. */
    fun mostRecentEndedYear(state: GameState): Int =
        if (state.gameMonth >= 6) state.gameYear else state.gameYear - 1

    /** The day a school year closes (spring's last day —
     *  the boundary Finales computes too). */
    fun endKeyOf(year: Int): String {
        val (m, d) = Finales.seasonEndMonthDay(Season.SPRING, year)
        return keyOf(year, m, d)
    }

    /** "2026–27" — the label a [year] book wears. */
    fun labelOf(year: Int): String =
        "${year - 1}–${(year % 100).toString().padStart(2, '0')}"

    fun volumeFor(state: GameState, year: Int): YearbookVolume? =
        state.yearbooks.firstOrNull { it.year == year }

    fun volumes(state: GameState): List<YearbookVolume> =
        state.yearbooks.toList()

    /** The year's album: every record whose own dayKey puts
     *  it in [year]'s school year. Records with an unreadable
     *  date compete nowhere — the book never re-dates. */
    fun albumFor(state: GameState, year: Int): List<PhotoRecord> =
        state.photos.filter { schoolYearOf(it.dayKey) == year }

    /** This school year's album so far (the in-progress
     *  standings the Journal page reports). */
    fun currentAlbum(state: GameState): List<PhotoRecord> =
        albumFor(state, currentYear(state))

    // ---------------- scoring (records only) ----------------

    /** The absDay a record's sky is judged on: its own
     *  field, or — for records from before the field — the
     *  ordinal distance from the game's first day. */
    fun absDayOf(rec: PhotoRecord): Int {
        if (rec.absDay >= 0) return rec.absDay
        val ord = ordinalOfKey(rec.dayKey)
        if (ord <= 0) return -1
        return (ord - ordinalOfKey(EPOCH_KEY)).coerceAtLeast(0)
    }

    private fun goofyCount(rec: PhotoRecord): Int =
        rec.npcs.count { it.reaction == Photos.GOOFY }

    private fun happyCount(rec: PhotoRecord): Int =
        rec.npcs.count { it.reaction == Photos.HAPPY }

    /** Posed back-to-back pairs (figures are written in
     *  pairs by the group composer). */
    private fun backToBackPairs(rec: PhotoRecord): Int =
        rec.figures.count { it.pose == "backToBack" } / 2

    /** Weather drama, by the record's own weather id. */
    val WEATHER_DRAMA: Map<String, Int> = mapOf(
        "blizzard" to 25, "heavy_snow" to 18, "fog" to 15,
        "rain" to 12, "wind" to 8, "light_snow" to 6,
        "cloudy" to 4, "sunny" to 2)

    /** The sky over a record, from its own fields alone:
     *  its day, its hour, its weather, its season. */
    fun skySpecialOf(rec: PhotoRecord): String {
        val abs = absDayOf(rec)
        if (abs < 0 || rec.dayKey.length != 8) return SkyState.NONE
        val month = rec.dayKey.substring(4, 6).toIntOrNull()
            ?: return SkyState.NONE
        return SkyState.specialAt(abs, rec.timeMin, rec.weatherId,
            seasonFor(month))
    }

    /** Does [rec] qualify for [cat] at all? (Town Favorite
     *  takes every record; the rest have honest gates.) */
    fun qualifies(cat: YbCategory, rec: PhotoRecord): Boolean =
        when (cat.id) {
            CAT_GROUP.id -> rec.isGroup
            CAT_SELFIE.id -> !rec.isGroup && rec.npcs.size in 1..2 &&
                rec.figures.any { it.kind == "player" }
            CAT_SKY.id -> !rec.inside
            CAT_COMMISSIONED.id -> rec.isCommissioned
            CAT_BUSY.id -> rec.figures.size >= 4
            else -> true
        }

    /** The raw field score — the stated formula, nothing
     *  else. The star bonus rides on top in [scoreOf]. */
    fun rawScore(cat: YbCategory, rec: PhotoRecord): Int =
        when (cat.id) {
            CAT_GROUP.id ->
                12 * rec.npcs.size + 6 * backToBackPairs(rec) +
                    3 * goofyCount(rec) + rec.followersGained
            CAT_SELFIE.id ->
                rec.followersGained + 6 * goofyCount(rec) +
                    3 * happyCount(rec) + 2 * rec.npcs.size
            CAT_SKY.id -> {
                val special = when (skySpecialOf(rec)) {
                    SkyState.AURORA -> 40
                    SkyState.METEOR -> 35
                    SkyState.HARVEST -> 30
                    SkyState.FIREWORKS -> 25
                    else -> 0
                }
                val night = if (SceneLight.bandOf(rec.timeMin) ==
                    "night") 3 else 0
                special + (WEATHER_DRAMA[rec.weatherId] ?: 0) + night
            }
            CAT_COMMISSIONED.id ->
                rec.followersGained + 5 * rec.npcs.size + 10
            CAT_BUSY.id ->
                10 * rec.figures.size + 4 * rec.refused.size +
                    rec.npcs.size
            else -> 2 * rec.followersGained + 3 * rec.npcs.size
        }

    /** The judged score: fields, plus the star. */
    fun scoreOf(cat: YbCategory, rec: PhotoRecord): Int =
        rawScore(cat, rec) + if (rec.starred) STAR_BONUS else 0

    /** The category's field, best first. Order is total
     *  and deterministic: score, then the earlier day, then
     *  the record id — the same album ranks identically
     *  forever. */
    fun ranked(cat: YbCategory,
               album: List<PhotoRecord>): List<PhotoRecord> =
        album.filter { qualifies(cat, it) }.sortedWith(
            compareByDescending<PhotoRecord> { scoreOf(cat, it) }
                .thenBy { ordinalOfKey(it.dayKey) }
                .thenBy { it.id })

    // ---------------- the judges' notes ----------------
    // Every factual claim in a note is a slot filled from
    // the record: {names} (who is in frame), {loc} (where),
    // {weather} (the record's own weather label), {sky}
    // (the record's own sky special, when there is one),
    // {asker} (who commissioned it), {count}, {followers}.
    // Template bodies name nobody and no place — if a fact
    // isn't in the frame, no bank can say it.

    private class Facts(rec: PhotoRecord) {
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
            (WEATHER[rec.weatherId]?.label ?: rec.weatherId).lowercase()
        val sky: String = when (skySpecialOf(rec)) {
            SkyState.AURORA -> "a sky rippling green overhead"
            SkyState.METEOR -> "falling stars streaking the sky"
            SkyState.HARVEST -> "a harvest moon the size of a hubcap"
            else -> ""
        }
        val asker: String = rec.commissionAsker
        val count: Int = rec.figures.size
        val followers: Int = rec.followersGained
    }

    private fun fill(t: String, f: Facts): String = t
        .replace("{names}", f.namesLine)
        .replace("{loc}", f.loc)
        .replace("{weather}", f.weather)
        .replace("{sky}", f.sky)
        .replace("{asker}", f.asker)
        .replace("{count}", f.count.toString())
        .replace("{followers}", f.followers.toString())

    /** A judge's line when THEY are in the winning frame —
     *  keyed to that fact alone. */
    private val IN_FRAME_LINES: Map<String, List<String>> = mapOf(
        "Wendy" to listOf(
            "Full disclosure: I am in this photo, and I recused " +
                "myself for a full ten seconds before voting for " +
                "it anyway. It earned it."),
        "Jimmy" to listOf(
            "That's ME in the winning photo! I have never been " +
                "more f-famous. Frame it. Frame ME."),
        "Cartman" to listOf(
            "I am in this picture, which the committee agrees " +
                "is doing most of the work. You're welcome, " +
                "yearbook.")
    )

    /** Note banks per judge per category — two variants
     *  each, chosen by the record's own id hash. */
    private val NOTE_BANKS: Map<String, Map<String, List<String>>> =
        mapOf(
            "Wendy" to mapOf(
                "group" to listOf(
                    "{names} — posed, balanced, and somehow all " +
                        "looking the same direction at {loc}. " +
                        "Do you know how hard that is?",
                    "A real ensemble: {names}, one frame, zero " +
                        "escapees. This is the one the year " +
                        "gets remembered by."),
                "selfie" to listOf(
                    "{names}, up close at {loc} — unguarded and " +
                        "completely themselves. An easy call.",
                    "The year's most honest face: {names}. No " +
                        "posing survives a lens that close."),
                "sky" to listOf(
                    "Shot at {loc} under {sky}: the town doing " +
                        "its best impression of a painting.",
                    "{weather} over {loc}, and the sky showed " +
                        "up like it had something to prove."),
                "commissioned" to listOf(
                    "{asker} asked for a photo, and the year got " +
                        "its best one: {names} at {loc}.",
                    "Commissioned work is still work — {names}, " +
                        "delivered for {asker}, framed forever."),
                "busy" to listOf(
                    "{count} in the frame at {loc} and every one " +
                        "of them mid-something. The year, at full " +
                        "volume.",
                    "Controlled chaos at {loc}: {names}, plus " +
                        "everyone the caption couldn't hold."),
                "favorite" to listOf(
                    "+{followers} followers don't lie. {names} " +
                        "at {loc} was the shot the town kept.",
                    "The people's choice, by the numbers and by " +
                        "the feeling: {names}.")
            ),
            "Jimmy" to mapOf(
                "group" to listOf(
                    "{names} in one f-frame! I counted twice. " +
                        "Nobody blinked. A miracle of science.",
                    "A group shot where the group actually " +
                        "SHOT — {names}, all present at {loc}."),
                "selfie" to listOf(
                    "{names}, SO close to the camera at {loc}. " +
                        "You can count the freckles. I did.",
                    "The lens loves {names}. The lens is " +
                        "correct. A triumph."),
                "sky" to listOf(
                    "{sky} over {loc} — I gasped out loud. In " +
                        "public. At volume. Worth it.",
                    "W-weather report: {weather}, and at {loc} " +
                        "it looked like THIS. Incredible."),
                "commissioned" to listOf(
                    "{asker} commissioned it and {names} " +
                        "DELIVERED. Put it on the fridge. Put " +
                        "the fridge in a museum.",
                    "A paid assignment with soul: {names} at " +
                        "{loc}. {asker} got their money's worth."),
                "busy" to listOf(
                    "{count} people at {loc}!! I tried to name " +
                        "them all and ran out of breath at " +
                        "{names}.",
                    "The busiest frame of the year — {names} " +
                        "and a whole crowd besides, at {loc}."),
                "favorite" to listOf(
                    "+{followers}! The town has spoken and the " +
                        "town has TASTE. {names}, forever.",
                    "Everybody's favorite, officially: {names} " +
                        "at {loc}. I showed my mom. She approves.")
            ),
            "Cartman" to mapOf(
                "group" to listOf(
                    "{names} managed to stand in a group without " +
                        "ruining it. I've decided to allow it.",
                    "A group photo at {loc} that doesn't waste " +
                        "the frame. Fine. FINE. It wins."),
                "selfie" to listOf(
                    "{names} at {loc}. The camera adds ten " +
                        "pounds and they spent it wisely.",
                    "Up close with {names}. Brave. Flattering. " +
                        "Suspiciously both."),
                "sky" to listOf(
                    "{sky}, over {loc}. Even I can't make fun " +
                        "of weather. It's bigger than me. " +
                        "Barely.",
                    "{weather} at {loc}. The sky did all the " +
                        "work and the photo takes the credit. " +
                        "Respect."),
                "commissioned" to listOf(
                    "{asker} paid for {names} and got a winner. " +
                        "I respect the hustle on both ends.",
                    "Commissioned by {asker}, shot at {loc}. " +
                        "Capitalism's finest hour. Frame it " +
                        "twice."),
                "busy" to listOf(
                    "{count} in one frame at {loc}. Statistically " +
                        "someone in it is awesome. Solid odds.",
                    "A crowd scene at {loc} — {names} and the " +
                        "whole circus. I approve of spectacle."),
                "favorite" to listOf(
                    "+{followers} followers. The masses have " +
                        "chosen {names}. The masses are followers. " +
                        "I'm just saying.",
                    "The town's favorite: {names} at {loc}. " +
                        "Democracy works. This time.")
            )
        )

    /** The judges on a blank page — nobody entered, and the
     *  book says so instead of faking a winner. */
    private val BLANK_LINES: Map<String, String> = mapOf(
        "Wendy" to "No entry this year — the page is just a " +
            "nice border. Shoot something for us.",
        "Jimmy" to "No entry! The page is just a nice border. " +
            "I drew a little sun in the corner. You're welcome.",
        "Cartman" to "No entry. The page is just a nice " +
            "border. Everybody was too scared. Of excellence."
    )

    /** The judge's note for one entry — authored from the
     *  record's facts, deterministic by the record's id. */
    fun noteFor(judge: Editor, cat: YbCategory,
                rec: PhotoRecord): String {
        val f = Facts(rec)
        if (judge.name in f.names) {
            val bank = IN_FRAME_LINES[judge.name] ?: emptyList()
            if (bank.isNotEmpty())
                return bank[(rec.id.hashCode() and Int.MAX_VALUE) %
                    bank.size]
        }
        val bank = NOTE_BANKS[judge.name]?.get(cat.id)
            ?: return "The committee has voted. The photo speaks."
        var t = bank[(rec.id.hashCode() and Int.MAX_VALUE) % bank.size]
        // the sky slot only reads honestly when there IS a
        // special; a sky entry with plain weather falls back
        // to the variant that argues from the weather.
        if ("{sky}" in t && f.sky.isEmpty()) {
            t = bank.firstOrNull { "{sky}" !in it } ?: t
            if ("{sky}" in t) t = t.replace("{sky}",
                "the whole sky doing its thing")
        }
        return fill(t, f)
    }

    // ---------------- judging ----------------

    class Judged(
        val volume: YearbookVolume,
        /** category id -> the runner-up record (the argument). */
        val runnerUps: Map<String, PhotoRecord>
    )

    /** Judge [year] from [state]'s album — pure: same album,
     *  same book, and the state is not touched. */
    fun judge(state: GameState, year: Int): Judged {
        val album = albumFor(state, year)
        val entries = mutableListOf<YearbookEntry>()
        val runnerUps = mutableMapOf<String, PhotoRecord>()
        if (album.isNotEmpty()) {
            for ((idx, cat) in CATEGORIES.withIndex()) {
                val judge = judgeFor(year, idx)
                val field = ranked(cat, album)
                val winner = field.firstOrNull()
                if (winner == null) {
                    entries += YearbookEntry(cat.id, judge.name, 0,
                        BLANK_LINES[judge.name] ?: "", null)
                } else {
                    entries += YearbookEntry(cat.id, judge.name,
                        scoreOf(cat, winner),
                        noteFor(judge, cat, winner),
                        PhotoRecord.fromMap(winner.toMap()))
                    field.getOrNull(1)?.let { runnerUps[cat.id] = it }
                }
            }
        }
        val winnersLine = if (album.isEmpty())
            "covers only — no photos this year"
        else entries.filter { it.record != null }.joinToString(" · ") {
            val cat = categoryById(it.categoryId)
            val rec = it.record!!
            val who = rec.npcs.firstOrNull()?.name ?: "a solo frame"
            "${cat?.emoji ?: "📷"} $who"
        }
        return Judged(YearbookVolume(
            year = year,
            photoCount = album.size,
            publishedKey = state.todayKey,
            publishedAbsDay = state.absDay,
            pamphlet = album.size in 1 until THIN_COUNT,
            indexOnly = false,
            winnersLine = winnersLine,
            entries = entries
        ), runnerUps)
    }

    // ---------------- publication ----------------

    private fun plantTopic(name: String, topic: String) {
        if (name !in KID_ORDER) return
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(topic)
        mem.topics.add(topic)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** The argument: winners bask, runners-up grumble —
     *  planted once, at publication, and left to fade like
     *  every topic in the six-slot memory. A face that won
     *  never also grumbles about the same book. */
    private fun plantArgument(judged: Judged) {
        val won = mutableSetOf<String>()
        for (e in judged.volume.entries) {
            val rec = e.record ?: continue
            for (fig in rec.npcs) {
                if (fig.kind == "cast") {
                    plantTopic(fig.name, TOPIC_WIN)
                    won += fig.name
                }
            }
        }
        for ((_, rec) in judged.runnerUps) {
            for (fig in rec.npcs) {
                if (fig.kind == "cast" && fig.name !in won)
                    plantTopic(fig.name, TOPIC_SNUB)
            }
        }
    }

    /** Reduce the oldest full volumes past the shelf cap
     *  to their index lines. */
    private fun reduceShelf(state: GameState) {
        var full = state.yearbooks.count { !it.indexOnly }
        var i = 0
        while (full > FULL_VOLUMES && i < state.yearbooks.size) {
            val v = state.yearbooks[i]
            if (!v.indexOnly) {
                state.yearbooks[i] = v.copy(
                    indexOnly = true, entries = emptyList())
                full--
            }
            i++
        }
    }

    /** The rollover (Engine.onNewDay calls this like every
     *  module's): if the most recent ended school year has
     *  no volume yet AND this save actually lived that year,
     *  judge it, shelve it, announce it, and start the
     *  argument. Exactly once per year — the stored volume
     *  is the guard. */
    fun dailyRollover(state: GameState) {
        if (!state.agents.values.any { it.isPlayer }) return
        val ended = mostRecentEndedYear(state)
        if (volumeFor(state, ended) != null) return
        // the save must have lived some of that year: its
        // first day falls on or before the year's last day.
        val dayOne = Commissions.keyForAbsDay(state, 0)
        if (dayOne > endKeyOf(ended)) return
        val judged = judge(state, ended)
        state.yearbooks += judged.volume
        reduceShelf(state)
        val v = judged.volume
        val line = if (v.photoCount == 0)
            "📖 The ${labelOf(ended)} yearbook is out — covers " +
                "only this year. Nobody took a single photo. " +
                "The border work is exquisite."
        else if (v.pamphlet)
            "📖 The ${labelOf(ended)} yearbook is out — a thin " +
                "year, a proud pamphlet: ${v.winnersLine}. " +
                "The whole book is on the 📖 YEARBOOK shelf."
        else
            "📖 The ${labelOf(ended)} yearbook is out! " +
                "${v.photoCount} photos judged — ${v.winnersLine}. " +
                "See the 📖 YEARBOOK shelf in your Journal."
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "📖 The ${labelOf(ended)} yearbook " +
            "published (${v.photoCount} photos judged)")
        plantArgument(judged)
    }

    /** Is the town still arguing about [v]? (Publication
     *  day counts; the talk runs ARGUMENT_DAYS.) */
    fun arguingAbout(state: GameState, v: YearbookVolume): Boolean =
        v.publishedAbsDay >= 0 &&
            state.absDay - v.publishedAbsDay in 0..ARGUMENT_DAYS

    // ---------------- the Journal page ----------------

    /** The shelf card's lines: what exists, what's still
     *  in the running, and the star's worth — the page the
     *  Journal renders above the volumes themselves. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val vols = volumes(state)
        val cur = currentAlbum(state)
        val curStarred = cur.count { it.starred }
        if (vols.isEmpty()) {
            out += "No yearbook yet — the Elementary prints " +
                "your album back to you every spring."
            out += "The book judges the school year (June to " +
                "May) from your photos alone: group shots, " +
                "selfies, dramatic skies, commissioned prints, " +
                "busy scenes, and the town favorite."
        } else {
            out += "${vols.size} volume${if (vols.size == 1) "" else "s"} " +
                "on the shelf — the newest " +
                "${minOf(FULL_VOLUMES, vols.count { !it.indexOnly })} " +
                "in full, the elders as index lines."
            out += "At home, they live on your yearbook shelf " +
                "by the cork board."
        }
        out += "This school year so far: ${cur.size} photo" +
            (if (cur.size == 1) "" else "s") + " in the running" +
            (if (curStarred > 0) ", $curStarred starred" else "") +
            ". A ⭐ star is worth +$STAR_BONUS with the judges " +
            "— star from the Album."
        val latest = vols.lastOrNull()
        if (latest != null && arguingAbout(state, latest)) {
            out += "🗣 The town is STILL arguing about the " +
                "${labelOf(latest.year)} book."
        }
        return out
    }

    /** One volume's cover lines (the Journal renders these
     *  above its category pages). */
    fun coverLines(v: YearbookVolume): List<String> {
        val out = mutableListOf<String>()
        out += "📖 THE ${labelOf(v.year)} YEARBOOK — " +
            "South Park Elementary"
        out += "Judged by " + EDITORS.joinToString(", ") {
            "${it.name} (${it.role})" } +
            " — from the album, and nothing but the album."
        out += when {
            v.photoCount == 0 ->
                "No photos this year. The book is covers and " +
                    "confidence; the pages are just nice borders."
            v.pamphlet ->
                "A thin year — ${v.photoCount} photo" +
                    (if (v.photoCount == 1) "" else "s") +
                    " in the running, and the pamphlet is PROUD " +
                    "of every one."
            else ->
                "${v.photoCount} photos in the running this year."
        }
        return out
    }

    /** An index-only volume's one line. */
    fun indexLine(v: YearbookVolume): String =
        "📖 ${labelOf(v.year)} — ${v.photoCount} photo" +
            (if (v.photoCount == 1) "" else "s") +
            (if (v.pamphlet) ", the pamphlet year" else "") +
            ": ${v.winnersLine}"
}

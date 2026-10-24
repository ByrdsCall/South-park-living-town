package com.hunt3r.southparktown

// ============================================================
// SkyState — v0.3.0e, sky parity (Queue #3 item 5).
//
// The sky is DATA now: a pure function of (absDay, timeMin,
// weatherId, season) — no save state, no randomness — so the
// scene renderer (SceneArt), the selfie backdrop (PhotoView),
// the ambience picker (SoundGen via GameActivity) and the
// town's own memory of last night (Engine's rollover record)
// all read the SAME sky and can never contradict each other.
//
//  - Moon: the 29-day, 8-step cycle TownLife has always used
//    (#44) — SkyState owns the formula now and TownLife
//    delegates, so the Towelie full-moon line and the drawn
//    moon are the same moon. Phase also sets a MOONLIGHT
//    level: full-moon nights genuinely read brighter (the
//    pond's ice sheen in the LIGHT layer scales by it).
//  - Aurora: clear winter nights only, on a sparse pair of
//    modular residues — 7 eligible nights in a canonical
//    winter (5–7 in any 90-day window, suite-pinned), and
//    winter sun cuts that to the ~2 a player actually sees.
//  - Meteor showers: fixed residue sets per season modulo a
//    91-day cycle — exactly 9 nights a year (2 spring,
//    3 summer, 2 fall, 2 winter), clear skies only.
//  - Harvest moon: ONE autumn full moon — the full-moon
//    block whose index is ≡ 0 (mod 3) — blood-orange and
//    oversized, up from dusk.
//
// Frequencies are pinned as constants and proven over a
// simulated year in the smoke suite (sectionV030e). The
// specials are deliberately RARE: most nights, the sky is
// just the sky, and that's what makes the other ones land.
// ============================================================
object SkyState {

    const val NONE = "none"
    const val AURORA = "aurora"
    const val METEOR = "meteor"
    const val HARVEST = "harvest"
    /** 🎡 v0.9.0c: the county fair's closing-night fireworks
     *  — one authored night a year, keyed to Fair's own
     *  calendar (FairCalendar), so every surface (render,
     *  Look, photos, yearbook sky judging) agrees. */
    const val FIREWORKS = "fireworks"

    /** The specials, in summary-priority order. */
    val SPECIALS: List<String> =
        listOf(AURORA, METEOR, HARVEST, FIREWORKS)

    // ---------------- the moon ----------------

    /** The lunar cycle, in days (TownLife's #44 formula). */
    const val MOON_PERIOD = 29

    /** 0..7 — moon phase index (0 = new, 4 = full). */
    fun moonPhase(absDay: Int): Int =
        (((absDay % MOON_PERIOD) + MOON_PERIOD) % MOON_PERIOD) * 8 / MOON_PERIOD

    fun isFullMoon(absDay: Int): Boolean = moonPhase(absDay) == 4

    val MOON_NAMES: List<String> = listOf(
        "new moon", "waxing crescent", "first quarter", "waxing gibbous",
        "full moon", "waning gibbous", "last quarter", "waning crescent")

    fun moonName(absDay: Int): String = MOON_NAMES[moonPhase(absDay)]

    /** How much moonlight a night gets, by phase: a new moon
     *  is a rumor, a full moon is a streetlight. The LIGHT
     *  layer's moonlit tokens scale by this. */
    val MOONLIGHT: List<Float> =
        listOf(0.05f, 0.30f, 0.55f, 0.80f, 1.0f, 0.80f, 0.55f, 0.30f)

    fun moonlight(absDay: Int): Float = MOONLIGHT[moonPhase(absDay)]

    // ---------------- clear skies ----------------

    /** The weathers a special sky can be SEEN through. The
     *  game's one truly clear sky is "sunny" (a Colorado
     *  winter night can be sunny at midnight — the label is
     *  about clouds, not the hour). */
    val CLEAR_WEATHERS: Set<String> = setOf("sunny")

    fun isClear(weatherId: String): Boolean = weatherId in CLEAR_WEATHERS

    // ---------------- aurora ----------------

    /** Aurora-eligible dates: two sparse residue families.
     *  Any 90-day window holds 5–7 of them (suite-pinned);
     *  the winter + clear gates do the rest of the rarity. */
    fun auroraDate(absDay: Int): Boolean =
        absDay % 23 == 5 || absDay % 31 == 17

    /** Tonight-level aurora: the DATE qualifies (winter,
     *  clear, eligible) — what TownInfo and the rollover
     *  record mean by "an aurora night". */
    fun auroraTonight(absDay: Int, weatherId: String, season: Season): Boolean =
        season == Season.WINTER && isClear(weatherId) && auroraDate(absDay)

    /** Aurora strength by hour: the long dark middle of the
     *  night burns brightest; the edges of the night band
     *  only get the hem of the curtains. */
    fun auroraIntensity(hour: Int): Float = when (hour) {
        23, 0 -> 1.0f
        1 -> 0.95f
        22, 2 -> 0.85f
        3 -> 0.75f
        21, 4 -> 0.55f
        5 -> 0.45f
        6 -> 0.35f
        else -> 0f
    }

    // ---------------- meteor showers ----------------

    /** The shower cycle: residues repeat every 91 days
     *  (about a season), keyed by the season they fall in. */
    const val METEOR_CYCLE = 91

    /** Fixed shower nights per season — 9 a year, total. */
    val METEOR_RESIDUES: Map<Season, Set<Int>> = mapOf(
        Season.SPRING to setOf(38, 39),
        Season.SUMMER to setOf(12, 13, 14),
        Season.FALL to setOf(61, 62),
        Season.WINTER to setOf(80, 81))

    fun meteorDate(absDay: Int, season: Season): Boolean =
        ((absDay % METEOR_CYCLE) + METEOR_CYCLE) % METEOR_CYCLE in
            (METEOR_RESIDUES[season] ?: emptySet())

    /** Tonight-level meteors: the date is a shower night and
     *  the sky is clear enough to see them. */
    fun meteorTonight(absDay: Int, weatherId: String, season: Season): Boolean =
        meteorDate(absDay, season) && isClear(weatherId)

    // ---------------- the harvest moon ----------------

    /** One full-moon block in three is THE autumn one. */
    const val HARVEST_BLOCK_RESIDUE = 0

    /** Tonight is the harvest moon: fall, a full moon, and
     *  the designated block. Blood-orange, oversized, and up
     *  from dusk (see [specialAt]). */
    fun isHarvestMoon(absDay: Int, season: Season): Boolean =
        season == Season.FALL && isFullMoon(absDay) &&
            (absDay / MOON_PERIOD) % 3 == HARVEST_BLOCK_RESIDUE

    // ---------------- the composed sky ----------------

    /** What's visible in the sky at one moment. [special]
     *  is one of NONE / AURORA / METEOR / HARVEST /
     *  FIREWORKS, in that priority: washes beat streaks
     *  beat the big moon beat the town's own show. */
    fun specialAt(absDay: Int, timeMin: Int, weatherId: String,
                  season: Season): String {
        val band = SceneLight.bandOf(timeMin)
        if (band == "night" &&
            auroraTonight(absDay, weatherId, season)) return AURORA
        if (band == "night" &&
            meteorTonight(absDay, weatherId, season)) return METEOR
        if (band != "day" && isHarvestMoon(absDay, season)) return HARVEST
        // 🎡 v0.9.0c: the fair's fireworks — one night a
        // year, after the sky's own authored shows, and
        // never through a blizzard (nobody stands in one,
        // and neither does the fuse crew).
        if (band == "night" && weatherId != "blizzard" &&
            FairCalendar.isFireworksAbsDay(absDay)) return FIREWORKS
        return NONE
    }

    /** The DATE's special, hour aside — what the midnight
     *  rollover records and the morning after buzzes about.
     *  Weather is the ending day's, the midnight proxy for
     *  the night's: if it wasn't clear, nobody saw anything,
     *  and the town has nothing to talk about. */
    fun dateSpecial(absDay: Int, weatherId: String, season: Season): String =
        when {
            auroraTonight(absDay, weatherId, season) -> AURORA
            meteorTonight(absDay, weatherId, season) -> METEOR
            isHarvestMoon(absDay, season) -> HARVEST
            // 🎡 v0.9.0c: the fair's closing night — the
            // date math lives in FairCalendar; weather
            // decides what was actually seen at [specialAt].
            FairCalendar.isFireworksAbsDay(absDay) &&
                weatherId != "blizzard" -> FIREWORKS
            else -> NONE
        }

    /** The full sky at one moment — everything a renderer
     *  (or a witness) needs, computed once. */
    data class SkyNow(
        val phase: Int,
        val moonlight: Float,
        val moonName: String,
        val special: String,
        /** Tonight is a harvest-moon DATE (the moon itself
         *  may not be up yet at this hour). */
        val harvest: Boolean,
        /** 0 unless an aurora is visible right now. */
        val auroraIntensity: Float
    )

    fun now(absDay: Int, timeMin: Int, weatherId: String,
            season: Season): SkyNow {
        val special = specialAt(absDay, timeMin, weatherId, season)
        return SkyNow(
            phase = moonPhase(absDay),
            moonlight = moonlight(absDay),
            moonName = moonName(absDay),
            special = special,
            harvest = isHarvestMoon(absDay, season),
            auroraIntensity = if (special == AURORA)
                auroraIntensity(timeMin / 60) else 0f)
    }

    /** The aurora is up right now (the ambience hush asks). */
    fun auroraNow(absDay: Int, timeMin: Int, weatherId: String,
                  season: Season): Boolean =
        specialAt(absDay, timeMin, weatherId, season) == AURORA

    /** Meteors are up right now (the fishing table asks). */
    fun meteorNow(absDay: Int, timeMin: Int, weatherId: String,
                  season: Season): Boolean =
        specialAt(absDay, timeMin, weatherId, season) == METEOR

    /** The one line Look gives a special sky. */
    fun lookLine(special: String): String? = when (special) {
        AURORA -> "🌌 The sky is rippling — slow green curtains of " +
            "light rolling over the mountains. Half the town is " +
            "outside in pajamas, looking up."
        METEOR -> "☄️ Meteors keep tearing across the sky tonight — " +
            "streak after streak. Everyone in town is making wishes " +
            "and pretending they're not."
        HARVEST -> "🌕 The moon is ENORMOUS tonight — a blood-orange " +
            "harvest moon squatting over the mountains. Everything " +
            "looks dipped in pumpkin."
        FIREWORKS -> "🎆 Fireworks over the fairground — the " +
            "fair's last night, and the whole town is still " +
            "out under them, faces up."
        else -> null
    }

    // ---------------- the morning after ----------------

    /** The record key family the rollover writes when a
     *  night ends: lifeText[LAST_SKY_KEY] = the special,
     *  life[LAST_SKY_DAY_KEY] = the NEW day's absDay. */
    const val LAST_SKY_KEY = "lastSky"
    const val LAST_SKY_DAY_KEY = "lastSkyDay"

    /** The special sky the town woke up talking about, or
     *  null: last night's sky, still today's news. */
    fun buzzKind(state: GameState): String? {
        if (state.life[LAST_SKY_DAY_KEY] != state.absDay) return null
        val kind = state.lifeText[LAST_SKY_KEY] ?: return null
        return if (kind in SPECIALS) kind else null
    }
}

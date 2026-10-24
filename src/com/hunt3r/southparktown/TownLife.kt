package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// TownLife — v0.1.8 Phase 2 (the 100 living features).
//
// TownInfo is the render context: GameActivity refreshes it from
// the live GameState on every UI tick so the pure renderers
// (SceneArt / InteriorArt / GameView) can ask occupancy questions
// — whose windows are lit, who keeps late hours, which shops are
// shut — without threading the whole save through every draw call.
// TownLife itself holds the pure, deterministic helpers the
// features key off (same inputs -> same town, on every screen).
// ============================================================

object TownInfo {
    var todayKey: String = ""
    var ordinal: Int = 0
    var timeMin: Int = 480
    var weatherId: String = "sunny"
    var season: Season = Season.FALL
    var holidayId: String? = null
    /** House locIds whose owner is physically home right now. */
    var occupiedHomes: Set<String> = emptySet()
    /** House locId -> minute the owner's last light dies. */
    var bedtimeOf: Map<String, Int> = emptyMap()
    /** Shop locIds locked right now (keeper away). */
    var closedShops: Set<String> = emptySet()
    /** Houses where someone fell asleep in front of the TV. */
    var tvHouses: Set<String> = emptySet()
    var auroraTonight: Boolean = false
    /** v0.3.0e: the composed sky right now (SkyState) — the
     *  special visible at this hour, the moonlight level the
     *  LIGHT layer scales by, tonight's harvest flag, and
     *  the aurora's strength. Refreshed with the rest. */
    var skySpecial: String = SkyState.NONE
    var moonlight: Float = 1f
    var harvestMoon: Boolean = false
    var auroraIntensity: Float = 0f
    var mudToday: Boolean = false
    var freezeThaw: Boolean = false
    /** 0f..1f — how far spring's buds have come. */
    var budLevel: Float = 0f
    /** 0..7 — moon phase index (0 = new, 4 = full). */
    var moonPhase: Int = 0
    /** The holdout house still wearing a past holiday's lights. */
    var holdoutLoc: String? = null
    var holdoutHoliday: Holiday? = null
    /** True when the interior's owner likes the player (curtains). */
    var curtainWarm: Boolean = false
    /** v0.3.0d: the decor tokens earned in YOUR house right now
     *  (House.decorTokensFor) — InteriorArt reads this to fill
     *  the room with what its owner has actually done. */
    var houseDecor: Set<String> = emptySet()
    /** v0.4.0f: the dig dressing live right now, locId ->
     *  tokens (Mystery3.digTokensByLoc) — SceneArt reads this
     *  to dress the open dig sites, and to keep the marker
     *  stone standing after the case retires. */
    var digTokens: Map<String, List<String>> = emptyMap()
    /** v0.6.0c: the gardener case's fix dressing live right
     *  now, locId -> tokens (Mystery4.fixTokensByLoc) — SceneArt
     *  reads this to keep the town's repairs standing; unlike
     *  the dig sites, a fix never comes down. */
    var fixTokens: Map<String, List<String>> = emptyMap()
    /** v0.5.0d: the faction chains' PERMANENT marks, locId ->
     *  tokens (FactionMissions.markTokensByLoc, derived from
     *  finished chains) — SceneArt's mark layer draws these;
     *  once a chain is done its mark stands forever. */
    var factionMarkTokens: Map<String, List<String>> = emptyMap()
    /** v0.9.0c: the county fair's dressing, live only while
     *  fair week stands (Fair.fairTokensByLoc) — SceneArt's
     *  fair layer draws these; the morning after closing the
     *  map is empty, which IS the strike. */
    var fairTokens: Map<String, List<String>> = emptyMap()
    /** v0.11.0e: the winter carnival's dressing, live only
     *  while carnival week stands (Carnival.carnivalTokensByLoc)
     *  — SceneArt's carnival layer draws these at the pond;
     *  the morning after closing the map is empty. */
    var carnivalTokens: Map<String, List<String>> = emptyMap()
}

/** Night-owl spots (#42): who keeps late hours, and where. */
val OWL_SPOTS: Map<String, String> = mapOf(
    "Michael" to "StarksPond", "Pete" to "StarksPond",
    "Henrietta" to "StarksPond",
    "Towelie" to "MainStreet", "Father Maxi" to "Church"
)

val HOUSE_LOCS: List<String> = listOf(
    "StanHouse", "KyleHouse", "CartmanHouse", "KennysHouse",
    "ButtersHouse", "TolkienHouse"
)

/** Cast birthdays (#55), "MMDD". */
/** v0.12.0c: the registry grew to the whole cast and moved
 *  into Birthdays (its sourcing split lives there); this
 *  stays as the #55-era name for it, one table by reference
 *  so the two can never drift. */
val BIRTHDAYS: Map<String, String> = Birthdays.DATES

fun birthdayOf(state: GameState): String? {
    // v0.1.9f: total on a short key (a corrupt todayKey used to
    // crash the first tick on StringIndexOutOfBoundsException).
    if (state.todayKey.length < 8) return null
    return Birthdays.birthdayOf(state)
}

object TownLife {

    // ---------- reputation (#52) ----------
    fun rep(state: GameState): Int = state.life["rep"] ?: 0

    fun repAdd(state: GameState, n: Int) {
        state.life["rep"] = rep(state) + n
    }

    fun repTier(state: GameState): String = when {
        rep(state) >= 25 -> "a local legend"
        rep(state) >= 10 -> "the helpful new kid"
        rep(state) >= 3 -> "a friendly face"
        rep(state) <= -10 -> "that menace"
        rep(state) <= -3 -> "a bit of a jerk"
        else -> "the new kid"
    }

    /** Faction accent colors for peak-influence days (#59). */
    val FACTION_ACCENTS: Map<String, String> = mapOf(
        "boys_crew" to "#2a6fd4", "goth_kids" to "#2a2a2e",
        "girls_circle" to "#e87ab0", "craigs_gang" to "#3a7a4a",
        "parents" to "#8a6a3a", "school_staff" to "#52b84b",
        "church_faithful" to "#e8c53a", "tegridy_crew" to "#5aa85a"
    )

    fun peakFaction(state: GameState): String? =
        state.factionStates.entries.firstOrNull { it.value.influence >= 75 }?.key

    // ---------- sky & calendar ----------
    /** Moon phase 0..7 from the absolute day count (#44).
     *  v0.3.0e: SkyState owns the formula now — this stays as
     *  the delegate so the #44 call sites never change. */
    fun moonPhase(ordinal: Int): Int = SkyState.moonPhase(ordinal)

    fun isFullMoon(state: GameState): Boolean = moonPhase(state.absDay) == 4

    /** v0.3.0e: SkyState owns the aurora rule too (clear —
     *  sunny — winter nights on the eligible dates). */
    fun auroraTonight(state: GameState): Boolean =
        SkyState.auroraTonight(state.absDay, state.weatherId,
            seasonFor(state.gameMonth))

    // ---------- daily oddities (deterministic by day) ----------
    /** The traveling gnome's yard today (#91). */
    fun gnomeLocFor(state: GameState): String =
        HOUSE_LOCS[((state.absDay * 7 + 3) % HOUSE_LOCS.size + HOUSE_LOCS.size) %
            HOUSE_LOCS.size]

    /** Which kid's house gets the shaking alarm today (#89). */
    fun alarmKidFor(state: GameState): String {
        val kids = listOf("Stan", "Kyle", "Cartman", "Kenny", "Butters",
            "Craig", "Tweek", "Clyde", "Jimmy", "Tolkien")
        return kids[((state.absDay % kids.size) + kids.size) % kids.size]
    }

    /** The stray cat's route (#5): a new yard every two hours. */
    private val CAT_ROUTE = listOf(
        "KennysHouse", "MainStreet", "KidsPark", "StarksPond",
        "CartmanHouse", "BusStop", "StanHouse", "MainStreet"
    )

    fun catLocAt(timeMin: Int): String = CAT_ROUTE[(timeMin / 120) % CAT_ROUTE.size]

    /** Today's lost-&-found loop (#64): owner | item | found at City Hall. */
    fun lostToday(state: GameState): Triple<String, String, String>? {
        val rand = Random(state.absDay * 977L + 13)
        val owners = listOf("Butters", "Wendy", "Craig", "Tweek", "Clyde",
            "Bebe", "Jimmy", "Tolkien", "Stan", "Kyle")
        val items = listOf("woodsy_poster", "craig_foam_finger", "cassette_single",
            "comic_book", "shiny_rock", "mystery_fuzz", "flower")
        val owner = owners[rand.nextInt(owners.size)]
        val item = items[rand.nextInt(items.size)]
        return Triple(owner, item, KID_HOUSE[owner] ?: "MainStreet")
    }

    /** Puddle level at a location (#31): 0 = dry, up to 3. */
    fun puddleSizeAt(state: GameState, locId: String): Int =
        state.puddles[locId] ?: 0

    // ========================================================
    // v0.1.9a — the v0.1.8 leftovers, finished. All pure and
    // deterministic: same (name, day, minute) -> same town.
    // ========================================================

    // ---------- #35 crowd density, strict form ----------
    /** How chatty an hour is, 0..1: midday bazaars buzz loudest,
     *  the small hours barely murmur. */
    fun densityWeight(hour: Int): Double = when (hour) {
        in 0..4 -> 0.17
        5 -> 0.30
        6 -> 0.45
        7, 8 -> 0.65
        in 9..15 -> 1.0
        16, 17 -> 0.80
        in 18..20 -> 0.60
        21 -> 0.40
        22 -> 0.30
        else -> 0.20
    }

    /** Ambient conversations allowed per tick AT ONE location:
     *  a strict function of that location's occupancy and the
     *  hour — never a flat cap, never a shared global pool. */
    fun densityCap(occupancy: Int, hour: Int): Int =
        (occupancy * densityWeight(hour)).toInt().coerceIn(0, 6)

    // ---------- #12 mail check ritual ----------
    /** Minute the resident's mail ritual starts (6:50–8:59). */
    fun mailStart(name: String, absDay: Int): Int =
        410 + Random(name.hashCode().toLong() * 31 + absDay * 101L).nextInt(130)

    /** 55% of mornings bring a letter (hop); the rest, a bill (slump). */
    fun mailHasLetter(name: String, absDay: Int): Boolean =
        Random(name.hashCode().toLong() * 77 + absDay * 13L).nextFloat() < 0.55f

    /** 0f..1f through the 12-minute ritual, or null outside it. */
    fun mailPhaseAt(name: String, absDay: Int, timeMin: Int): Float? {
        val start = mailStart(name, absDay)
        return if (timeMin in start until start + 12) (timeMin - start) / 12f else null
    }

    // ---------- #92 mystery lunchbox ----------
    private val LUNCHBOX_KIDS = listOf("Stan", "Kyle", "Kenny", "Butters",
        "Craig", "Tweek", "Clyde", "Jimmy", "Wendy", "Bebe")

    /** A different kid retrieves the lunchbox each day. */
    fun lunchboxKidFor(state: GameState): String =
        LUNCHBOX_KIDS[((state.absDay % LUNCHBOX_KIDS.size) + LUNCHBOX_KIDS.size) %
            LUNCHBOX_KIDS.size]

    // ---------- #33 snow buildup & shake-off ----------
    /** 26-minute personal cycle: 20 min of buildup, a 2-minute
     *  shake-off window, then a clean coat again. */
    fun snowPhase(name: String, timeMin: Int): Int {
        val off = (name.hashCode() and 0x7fffffff) % 13
        return ((timeMin + off) % 26 + 26) % 26
    }

    fun snowCover(name: String, timeMin: Int): Float =
        snowPhase(name, timeMin).coerceAtMost(20) / 20f

    fun snowShaking(name: String, timeMin: Int): Boolean =
        snowPhase(name, timeMin) in 21..22

    // ---------- #62 chalk drawings ----------
    val CHALK_SPOTS: Set<String> = setOf("BusStop", "Elementary", "KidsPark", "MainStreet")

    val CHALK_DOODLES: List<String> = listOf(
        "a star", "a heart", "a spiral", "a smiley face",
        "a little house", "a flower", "a fish", "a sun")

    /** lifeText["chalk:<loc>"] = "<doodleIdx>:<placedAbsDay>". */
    fun chalkRecord(state: GameState, locId: String): Pair<Int, Int>? {
        val raw = state.lifeText["chalk:$locId"] ?: return null
        val parts = raw.split(":")
        val idx = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val day = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return idx to day
    }

    /** A doodle lives 5 days, then the weather takes it. */
    fun chalkActive(state: GameState, locId: String): Boolean {
        val rec = chalkRecord(state, locId) ?: return false
        return (state.absDay - rec.second) in 0..5
    }

    /** Doodles on show: yours, plus one addition from another kid
     *  every other day, up to three — they add to it when you're
     *  not looking. */
    fun chalkShownCount(state: GameState, locId: String): Int {
        if (!chalkActive(state, locId)) return 0
        val rec = chalkRecord(state, locId) ?: return 0
        return 1 + minOf(2, (state.absDay - rec.second) / 2)
    }

    fun chalkShown(state: GameState, locId: String): List<Int> {
        val rec = chalkRecord(state, locId) ?: return emptyList()
        return (0 until chalkShownCount(state, locId))
            .map { (rec.first + it) % CHALK_DOODLES.size }
    }

    // ---------- #93 the lost mitten tree ----------
    const val MAX_MITTENS: Int = 12

    fun mittensHung(state: GameState): Int = state.life["mittensHung"] ?: 0

    /** Hang one held lost mitten on the tree. False if the
     *  player holds none or the tree is already full. */
    fun tryHangMitten(state: GameState): Boolean {
        val held = state.inventory["lost_mitten"] ?: 0
        if (held <= 0 || mittensHung(state) >= MAX_MITTENS) return false
        if (held == 1) state.inventory.remove("lost_mitten")
        else state.inventory["lost_mitten"] = held - 1
        state.life["mittensHung"] = mittensHung(state) + 1
        return true
    }

    // ---------- #97 the New Kid fan club ----------
    /** Three or more Close Friends and the club exists. */
    fun fanClubActive(state: GameState): Boolean =
        state.friendship.values.count {
            Relationships.tierOf(it) >= Relationships.CLOSE_FRIEND
        } >= 3

    // ---------- #98 3:33 AM sightings ----------
    /** Roughly half of nights, something crosses at 3:33 AM. */
    fun sightingTonight(state: GameState): Boolean =
        Random(state.absDay * 331L + 7).nextFloat() < 0.55f

    // ---------- #41 bedtime yawn ----------
    /** The yawn bubble pops as the first window dies. */
    fun yawnAt(bedtime: Int, timeMin: Int): Boolean =
        timeMin in bedtime until bedtime + 9

    /** Deterministic per (loc, day) 0f..1f scatter value for props. */
    fun scatter(locId: String, day: Int, salt: Int): Float {
        val r = Random(locId.hashCode().toLong() * 131 + day * 17L + salt)
        return r.nextFloat()
    }
}

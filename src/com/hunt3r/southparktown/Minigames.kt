package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Minigames — v0.2.0e (Queue #2, item 5): three little games
// on one shared session scaffold.
//
//   🎣 FISHING at Stark's Pond — cast, wait for the bobber
//      dip, tap inside the bite window. Weather and the hour
//      bend the bite; the catch table runs smelt → golden
//      carp → three-eyed fish → one (1) boot.
//   ❄️ SNOWBALL SHOWDOWN at Kid's Park / Main Street —
//      first to 3 hits against a kid standing with you.
//      Your throw is a timing-bar release; theirs is a
//      dodge-or-eat-it window. Snow weather only, because
//      rules are rules.
//   🌮 TACO CATCH inside Freeman's Tacos — 60 seconds of
//      falling food over an arcade cabinet. Catch tacos,
//      dodge the TVs and the 'berries.
//
// SCAFFOLD: every game is a session object built from a
// seed — pure Kotlin, fully deterministic under that seed
// (the JVM suite drives them tick by tick), advanced by
// the UI layer (MinigameViews), and settled EXACTLY ONCE
// through an apply* function here. Rewards pay through the
// systems the town already has — no new currencies:
//   · catches & prizes land in the INVENTORY as items
//     (the fish are giftable; NPCs have opinions),
//   · 🪙 CAPSULE COINS are an inventory item too — the
//     capsule-machine coin kids trade and gift — paid by
//     the Showdown and Taco Catch, counted on the Journal
//     🎮 GAMES page,
//   · friendship moves through Engine.relAdjust, the same
//     pipe gifts and insults use.
// Records (fish log, showdown W/L, taco high score, game
// feats) are additive GameState fields — old saves default
// to an empty games page.
// ============================================================

object Minigames {

    /** The snows a showdown (and a snowball) can happen in. */
    val SNOW_WEATHERS: Set<String> = setOf("light_snow", "heavy_snow", "blizzard")

    /** Where a showdown can be called. */
    val SHOWDOWN_LOCS: Set<String> = setOf("KidsPark", "MainStreet")

    const val POND_LOC = "StarksPond"
    const val TACO_LOC = "FreemansTacos"
    const val COIN_ITEM = "capsule_coin"

    // ---------------- availability (the entry points read these) ----------------

    /** Fishing: at the pond, outdoors (the dock is outside the hut). */
    fun fishingAvailable(state: GameState): Boolean {
        val me = state.agents.values.firstOrNull { it.isPlayer } ?: return false
        return me.location == POND_LOC && !state.playerInside
    }

    /** Showdown: outside at the park or Main Street, snow falling,
     *  and at least one awake cast kid standing with you. */
    fun showdownAvailable(state: GameState): Boolean {
        val me = state.agents.values.firstOrNull { it.isPlayer } ?: return false
        if (me.location !in SHOWDOWN_LOCS || state.playerInside) return false
        if (state.weatherId !in SNOW_WEATHERS) return false
        return showdownOpponents(state).isNotEmpty()
    }

    /** Cast kids standing with the player who could be challenged
     *  (awake, same spot, same side of the door). */
    fun showdownOpponents(state: GameState): List<String> {
        val me = state.agents.values.firstOrNull { it.isPlayer } ?: return emptyList()
        return state.agents.values.filter {
            !it.isPlayer && it.name in CharacterSystem.CAST_KIDS &&
                it.location == me.location && !it.asleep &&
                it.waypoints.isEmpty()
        }.map { it.name }
    }

    /** Taco Catch: inside Freeman's Tacos, at the cabinet. */
    fun tacoAvailable(state: GameState): Boolean {
        val me = state.agents.values.firstOrNull { it.isPlayer } ?: return false
        return me.location == TACO_LOC && state.playerInside
    }

    // ---------------- capsule coins (an inventory item, not a currency) ----------------

    fun capsuleCoins(state: GameState): Int = state.inventory[COIN_ITEM] ?: 0

    /** Grant [n] coins; returns the new balance. Callers settle
     *  once per session (the sessions' settled flags guard it). */
    fun awardCoins(state: GameState, n: Int): Int {
        if (n <= 0) return capsuleCoins(state)
        state.inventory[COIN_ITEM] = capsuleCoins(state) + n
        return capsuleCoins(state)
    }

    // ---------------- game feats (additions only, like photo feats) ----------------

    data class GameFeat(val id: String, val name: String, val desc: String)

    val GAME_FEATS: List<GameFeat> = listOf(
        GameFeat("first_cast", "🎣 First Cast",
            "Catch anything at Stark's Pond. Even the boot counts."),
        GameFeat("golden", "🎣 Golden Touch",
            "Land the Golden Carp."),
        GameFeat("three_eyed", "🎣 Local Legend",
            "Pull the Three-Eyed Fish out of Stark's Pond."),
        GameFeat("shutout", "❄️ Fort Shutout",
            "Win a Snowball Showdown 3–0."),
        GameFeat("sore_loser", "❄️ Sore Loser",
            "Beat Cartman at the Showdown. He has prepared a statement."),
        GameFeat("taco_500", "🌮 Taco Titan",
            "Score 500+ in one game of Taco Catch."),
        // v0.7.0d: the bee's feat — the win also hangs the
        // bee trophy (House decor) and writes the SCHOOL page.
        GameFeat("bee_champion", "🐝 Bee Champion",
            "Win the Friday spelling bee at the Elementary. " +
                "Sleeves down, letters up.")
    )

    fun featById(id: String): GameFeat? = GAME_FEATS.firstOrNull { it.id == id }

    /** Complete feat [id] if it isn't already; posts the ticker
     *  line the other feat systems post. Returns true when new. */
    fun awardFeat(state: GameState, id: String): Boolean {
        val f = featById(id) ?: return false
        if (!state.gameFeats.add(id)) return false
        state.events.add("${f.name} — game feat complete!")
        while (state.events.size > 40) state.events.removeAt(0)
        Phone.noteFeat(state, f.name)
        return true
    }

    /** Lifetime fish caught (the boot counts — it counts). */
    fun totalCaught(state: GameState): Int =
        state.fishLog.values.sumOf { it.count }

    // ---------------- first-entry how-tos (v0.2.0g) ----------------
    // One line, the first time each game is entered on a save,
    // appended to the game's dialog subtitle by the entry
    // points. The seen flag is an additive `life` key — old
    // saves simply haven't seen them yet, so every existing
    // player gets the line exactly once too.

    val HOW_TO: Map<String, String> = mapOf(
        "fishing" to "🎣 HOW TO: cast, watch the bobber — TAP the moment " +
            "it dips. Early spooks the fish; late, it's gone.",
        "showdown" to "❄️ HOW TO: TAP to release your throw inside the " +
            "RED zone — and when their snowball flies, TAP late in " +
            "its flight to dodge.",
        "taco" to "🌮 HOW TO: DRAG the tray under tacos (+10) and " +
            "burritos (+15). TVs and Memberberries stun the tray — " +
            "let them drop."
    )

    /** The how-to line for [game]'s first entry on this save
     *  (marks it seen); null on every later entry. */
    fun firstEntryHowTo(state: GameState, game: String): String? {
        val line = HOW_TO[game] ?: return null
        val key = "howto:$game"
        if ((state.life[key] ?: 0) > 0) return null
        state.life[key] = 1
        return line
    }
}

// ============================================================
// Feel — every tuning number the three games play by.
// v0.2.0g: centralized from literals scattered through the
// sessions, VALUES UNCHANGED (the v0.2.0e behavior pins in
// the suite read exactly these numbers) — the next feel pass
// is a one-object diff instead of a scavenger hunt.
// ============================================================
object Feel {
    // ---- 🎣 fishing ----
    /** Base wait before the bobber can dip, plus a random spread. */
    const val BITE_DELAY_BASE_MS = 1600
    const val BITE_DELAY_SPREAD_MS = 4200
    /** The bobber never dips sooner than this, however bitey it is. */
    const val BITE_DELAY_FLOOR_MS = 800L
    /** How long the dip stays tappable, by weather. */
    const val BITE_WINDOW_MS = 950L
    const val BITE_WINDOW_HEAVY_SNOW_MS = 820L
    const val BITE_WINDOW_BLIZZARD_MS = 700L
    // bite-delay multipliers by hour (lower = the fish are ON it)
    const val BITE_DAWN = 0.55
    const val BITE_DUSK = 0.60
    const val BITE_MIDDAY = 1.15
    const val BITE_NIGHT = 0.85
    // bite-delay multipliers by weather
    const val BITE_WX_BLIZZARD = 2.4
    const val BITE_WX_HEAVY_SNOW = 1.6
    const val BITE_WX_LIGHT_SNOW = 1.1
    const val BITE_WX_RAIN = 0.8
    const val BITE_WX_FOG = 0.9
    const val BITE_WX_WIND = 1.3
    /** v0.3.0e: on meteor nights the pond's rare fish rise to
     *  the streaks — rare-species weights scale by this in
     *  FishingSession (three-eyed fish 4 -> 6 of 112). The
     *  three-eyed one especially: it likes meteor nights. */
    const val METEOR_RARE_MULT = 1.5

    // ---- ❄️ snowball showdown ----
    /** Release-zone edges on the timing bar (0..1 across). */
    const val ZONE_PERFECT_LO = 0.44f
    const val ZONE_PERFECT_HI = 0.56f
    const val ZONE_GOOD_LO = 0.30f
    const val ZONE_GOOD_HI = 0.70f
    const val ZONE_OK_LO = 0.15f
    const val ZONE_OK_HI = 0.85f
    /** Throw power per zone (before the opponent's dodge stat). */
    const val POWER_PERFECT = 1.0
    const val POWER_GOOD = 0.78
    const val POWER_OK = 0.50
    const val POWER_WILD = 0.18
    /** The dodge window: a base, minus per-round wear, minus the
     *  thrower's dodge stat scaled by SKILL, clamped to a fair band. */
    const val DODGE_BASE_MS = 660.0
    const val DODGE_PER_ROUND_MS = 45.0
    const val DODGE_SKILL_MS = 120.0
    const val DODGE_MIN_MS = 380L
    const val DODGE_MAX_MS = 700L

    // ---- 🌮 taco catch ----
    /** The first item falls at this run-time; spawns stop this
     *  far before the buzzer. */
    const val SPAWN_FIRST_MS = 1100L
    const val SPAWN_CUTOFF_MS = 800L
    /** Fall time: starts at BASE, ramps down per elapsed second,
     *  never faster than FLOOR, jittered per item. */
    const val FALL_BASE_MS = 2500.0
    const val FALL_RAMP_PER_SEC = 16.0
    const val FALL_FLOOR_MS = 1450.0
    const val FALL_JITTER_LO = 0.9
    const val FALL_JITTER_SPAN = 0.3
    /** Spawn interval: the same ramp shape as the fall. */
    const val INTERVAL_BASE_MS = 940.0
    const val INTERVAL_RAMP_PER_SEC = 8.0
    const val INTERVAL_FLOOR_MS = 430.0
    const val INTERVAL_JITTER_MS = 160
    /** Kind roll (of 100): under TACO_MAX a taco, then burrito,
     *  then berry; the rest are falling TVs. */
    const val KIND_TACO_MAX = 55
    const val KIND_BURRITO_MAX = 80
    const val KIND_BERRY_MAX = 88
    /** Spawn x: a margin off each wall, spanning the middle. */
    const val X_MARGIN = 0.06f
    const val X_SPAN = 0.88f
    /** Hazard payback: the tray freezes this long. */
    const val STUN_TV_MS = 950L
    const val STUN_BERRY_MS = 650L
    /** Score per kind caught. */
    const val POINTS_TACO = 10
    const val POINTS_BURRITO = 15
    const val POINTS_BERRY = -15
    const val POINTS_TV = -25
    /** The cabinet's payout table: score floor -> capsule coins. */
    val COIN_TIERS: List<Pair<Int, Int>> = listOf(
        600 to 12, 450 to 9, 300 to 6, 180 to 4, 80 to 2, 1 to 1)
}

// ============================================================
// Persistent records (additive GameState fields)
// ============================================================

/** One species in the fishing log: how many, and the biggest. */
data class FishRecord(var count: Int = 0, var biggestCm: Int = 0) {
    fun toMap(): Map<String, Any?> = mapOf("count" to count, "biggest" to biggestCm)

    companion object {
        fun fromMap(m: Map<String, Any?>): FishRecord = FishRecord(
            count = (m["count"] as? Number)?.toInt() ?: 0,
            biggestCm = (m["biggest"] as? Number)?.toInt() ?: 0
        )
    }
}

/** Your showdown history against one kid. */
data class ShowdownRecord(var wins: Int = 0, var losses: Int = 0) {
    fun toMap(): Map<String, Any?> = mapOf("wins" to wins, "losses" to losses)

    companion object {
        fun fromMap(m: Map<String, Any?>): ShowdownRecord = ShowdownRecord(
            wins = (m["wins"] as? Number)?.toInt() ?: 0,
            losses = (m["losses"] as? Number)?.toInt() ?: 0
        )
    }
}

// ============================================================
// 🎣 FISHING
// ============================================================

data class FishSpecies(
    val id: String, val name: String, val icon: String,
    val weight: Int, val minCm: Int, val maxCm: Int,
    val itemId: String, val rare: Boolean = false, val joke: Boolean = false
)

/** The pond's whole population, common → rare → boot. Weights
 *  sum to 100; the roll walks them in order. */
val FISH_TABLE: List<FishSpecies> = listOf(
    FishSpecies("pond_smelt", "Pond Smelt", "🐟", 34, 8, 18, "fish_smelt"),
    FishSpecies("mud_minnow", "Mud Minnow", "🐟", 26, 5, 12, "fish_minnow"),
    FishSpecies("speckled_trout", "Speckled Trout", "🐠", 18, 18, 35, "fish_trout"),
    FishSpecies("rainbow_trout", "Rainbow Trout", "🌈", 10, 22, 40, "fish_rainbow", rare = true),
    FishSpecies("golden_carp", "Golden Carp", "🏆", 6, 30, 55, "fish_carp", rare = true),
    FishSpecies("three_eyed_fish", "Three-Eyed Fish", "☢️", 4, 15, 25, "fish_three_eyed", rare = true),
    FishSpecies("old_boot", "Old Boot", "🥾", 2, 24, 30, "old_boot", joke = true)
)

fun fishById(id: String): FishSpecies? = FISH_TABLE.firstOrNull { it.id == id }

/** How bitey the pond is, as a delay multiplier (lower = the
 *  fish are ON it). Dawn and dusk feed; midday sulks; a
 *  blizzard shuts the whole operation down. Rain, per every
 *  fishing grandpa ever, is excellent. */
fun biteFactor(weatherId: String, hour: Int): Double {
    val timeF = when (hour) {
        in 5..7 -> Feel.BITE_DAWN
        in 17..19 -> Feel.BITE_DUSK
        in 11..15 -> Feel.BITE_MIDDAY
        in 22..23, in 0..4 -> Feel.BITE_NIGHT
        else -> 1.0
    }
    val wxF = when (weatherId) {
        "blizzard" -> Feel.BITE_WX_BLIZZARD
        "heavy_snow" -> Feel.BITE_WX_HEAVY_SNOW
        "light_snow" -> Feel.BITE_WX_LIGHT_SNOW
        "rain" -> Feel.BITE_WX_RAIN
        "fog" -> Feel.BITE_WX_FOG
        "wind" -> Feel.BITE_WX_WIND
        else -> 1.0
    }
    return timeF * wxF
}

/** How long the bobber-dip window stays open. A blizzard
 *  does not care about your feelings OR your reaction time. */
fun biteWindowMs(weatherId: String): Long = when (weatherId) {
    "blizzard" -> Feel.BITE_WINDOW_BLIZZARD_MS
    "heavy_snow" -> Feel.BITE_WINDOW_HEAVY_SNOW_MS
    else -> Feel.BITE_WINDOW_MS
}

/**
 * One cast of the line. Time is milliseconds since the cast
 * (the UI supplies its own clock); every roll comes from the
 * seeded rng in a fixed order — delay first, then (on a
 * hooked tap) species, then size — so a seed replays exactly.
 *
 * WAITING --tick past biteAt--> BITE --tap--> CAUGHT
 * WAITING --tap--> SCARED (you yanked at nothing)
 * BITE --tick past biteEnd--> ESCAPED
 */
class FishingSession(seed: Long, weatherId: String, hour: Int,
                     private val meteorNight: Boolean = false) {
    enum class Phase { WAITING, BITE, OVER }
    enum class Outcome { NONE, CAUGHT, SCARED, ESCAPED }

    private val rng = Random(seed)
    val biteAtMs: Long
    val windowMs: Long = biteWindowMs(weatherId)
    val biteEndMs: Long get() = biteAtMs + windowMs

    var phase: Phase = Phase.WAITING
        private set
    var outcome: Outcome = Outcome.NONE
        private set
    var species: FishSpecies? = null
        private set
    var sizeCm: Int = 0
        private set
    /** Reward settlement guard — applyCatch runs once. */
    var settled: Boolean = false
        internal set

    init {
        val base = Feel.BITE_DELAY_BASE_MS + rng.nextInt(Feel.BITE_DELAY_SPREAD_MS)
        biteAtMs = (base * biteFactor(weatherId, hour)).toLong()
            .coerceAtLeast(Feel.BITE_DELAY_FLOOR_MS)
    }

    fun tick(nowMs: Long) {
        if (phase == Phase.WAITING && nowMs >= biteAtMs) phase = Phase.BITE
        if (phase == Phase.BITE && nowMs > biteEndMs) {
            phase = Phase.OVER
            outcome = Outcome.ESCAPED
        }
    }

    /** The one button fishing has. Tapping inside the window
     *  (both edges inclusive) hooks whatever the roll says. */
    fun tap(nowMs: Long) {
        tick(nowMs)
        when (phase) {
            Phase.WAITING -> {
                phase = Phase.OVER
                outcome = Outcome.SCARED
            }
            Phase.BITE -> {
                // v0.3.0e: meteor nights bend the table toward
                // the rare end (Feel.METEOR_RARE_MULT). A plain
                // night rolls exactly the legacy table: weights
                // sum to 100 and the draw order never changes.
                fun effW(sp: FishSpecies): Int =
                    if (meteorNight && sp.rare)
                        (sp.weight * Feel.METEOR_RARE_MULT).toInt()
                            .coerceAtLeast(sp.weight + 1)
                    else sp.weight
                val roll = rng.nextInt(FISH_TABLE.sumOf { effW(it) })
                var acc = 0
                var caught = FISH_TABLE.last()
                for (sp in FISH_TABLE) {
                    acc += effW(sp)
                    if (roll < acc) { caught = sp; break }
                }
                species = caught
                sizeCm = caught.minCm + rng.nextInt(caught.maxCm - caught.minCm + 1)
                phase = Phase.OVER
                outcome = Outcome.CAUGHT
            }
            Phase.OVER -> { /* settled casts ignore pokes */ }
        }
    }
}

data class FishResult(
    val species: FishSpecies, val cm: Int,
    val isNew: Boolean, val feats: List<String>
)

/** 🌠 Lifetime wishes made at the pond (v0.3.0e) — the count
 *  rides the existing life counters (Engine.pondWish writes
 *  it on meteor nights); no new save key. The Journal's
 *  🎮 GAMES page keeps the tally. */
fun wishCount(state: GameState): Int = state.life["wishes"] ?: 0

/** Feats a catch of [speciesId] completes against the log as
 *  it stands BEFORE this catch is filed. Pure — the suite
 *  pins it; applyCatch awards what it returns. */
fun catchFeats(state: GameState, speciesId: String): List<String> {
    val out = mutableListOf<String>()
    if ("first_cast" !in state.gameFeats && Minigames.totalCaught(state) == 0)
        out.add("first_cast")
    if (speciesId == "golden_carp" && "golden" !in state.gameFeats) out.add("golden")
    if (speciesId == "three_eyed_fish" && "three_eyed" !in state.gameFeats)
        out.add("three_eyed")
    return out
}

/** Count a cast (the Activity calls this when a session opens). */
fun noteCast(state: GameState) {
    state.fishCasts++
}

/** Settle a caught fish: log it (count + biggest), put it in
 *  the pack as a giftable item, award its feats. Runs ONCE —
 *  a second call on the same session returns null. */
fun applyCatch(state: GameState, s: FishingSession): FishResult? {
    if (s.settled || s.outcome != FishingSession.Outcome.CAUGHT) return null
    val sp = s.species ?: return null
    s.settled = true
    val feats = catchFeats(state, sp.id)
    val rec = state.fishLog.getOrPut(sp.id) { FishRecord() }
    val isNew = rec.count == 0
    rec.count++
    if (s.sizeCm > rec.biggestCm) rec.biggestCm = s.sizeCm
    state.inventory[sp.itemId] = (state.inventory[sp.itemId] ?: 0) + 1
    for (f in feats) Minigames.awardFeat(state, f)
    // v0.4.0b: a catch can be somebody's errand (PLAY-fish)
    Errands.onPlayCatch(state, sp.id)
    // v0.4.0d: and on the Fourth itself, any honest fish is
    // the holiday beat's picnic (HolidayBeats gates the day)
    HolidayBeats.onCatch(state)
    // v0.5.0d: an armed faction CATCH beat takes its fish too
    FactionMissions.onCatch(state, sp.id)
    // v0.8.0b: and an armed capstone CATCH beat (the carp)
    Capstones.onCatch(state, sp.id)
    // v0.8.0e: and an armed rival fishing duel — your cast
    // is the contest's player half; theirs rolls on the
    // same table the moment yours lands
    Rival.onCatch(state, sp, s.sizeCm)
    // v0.9.0c: and the county fair's fishing contest — on
    // its one day, a pond catch is a contest cast; your
    // best stands as your entry (Fair.onCatch)
    Fair.onCatch(state, sp, s.sizeCm)
    // v0.11.0e: and the winter carnival's ice fishing —
    // on its one day, a pond catch comes up through the
    // ice as a contest cast (Carnival.onCatch)
    Carnival.onCatch(state, sp, s.sizeCm)
    return FishResult(sp, s.sizeCm, isNew, feats)
}

// ============================================================
// ❄️ SNOWBALL SHOWDOWN
// ============================================================

object Showdown {
    const val HITS_TO_WIN = 3
    /** One sweep of the timing bar, and one incoming snowball's flight. */
    const val BAR_MS = 1150L
    const val FLIGHT_MS = 950L

    /** How slippy each kid is when you're the one throwing.
     *  Tweek dodges on reflex and terror; Butters is a barn. */
    fun dodgeFor(name: String): Double = when (name) {
        "Butters" -> 0.08
        "Cartman" -> 0.12
        "Timmy" -> 0.15
        "Jimmy" -> 0.18
        "Bebe" -> 0.22
        "Stan", "Kyle", "Clyde" -> 0.25
        "Tolkien" -> 0.28
        "Kenny", "Michael", "Pete" -> 0.30
        "Wendy" -> 0.32
        "Craig" -> 0.35
        "Tweek" -> 0.40
        // wave 2 (v0.3.0b): Francis is a barn with a heart; Kevin's
        // reflexes are tuned for movie trivia, not snow
        "Francis" -> 0.13
        "DogPoo" -> 0.19
        "Kevin" -> 0.23
        "Nichole" -> 0.26
        "Heidi" -> 0.27
        "Red" -> 0.29
        // wave 3 (v0.7.0a): Scott's hero reflexes are rehearsed,
        // Firkle is the smallest target in town, Karen is quick
        // on little-kid legs
        "Scott" -> 0.24
        "Bradley" -> 0.26
        "Annie" -> 0.27
        "Karen" -> 0.31
        "Firkle" -> 0.34
        // wave 4 (v0.8.0a): the eldest McCormick is a big,
        // unhurried target; Shelley is teen-quick; Ike is
        // three years old and nearly all head — the smallest
        // target in town
        "Kevin McCormick" -> 0.21
        "Shelley" -> 0.28
        "Ike" -> 0.36
        // wave 5 (v0.9.0a): the schemer is hard to hit because
        // he has already planned where the snowball lands;
        // the new girl is average-quick and learning fast
        "Nathan" -> 0.33
        "Sophie" -> 0.29
        // wave 6 (v0.11.0d): the audit's kids — the sporty
        // one and the hereditary schemer are hard to hit;
        // the henchman is a big, loyal target; the
        // kindergarten trio duck at knee height
        "Pip" -> 0.27
        "Dougie" -> 0.31
        "Jason" -> 0.34
        "Terrance Mephesto" -> 0.35
        "Jenny" -> 0.30
        "Flora" -> 0.22
        "Filmore" -> 0.28
        "Quaid" -> 0.29
        "Mimsy" -> 0.18
        "Tricia" -> 0.26
        else -> 0.25
    }

    /** The release zone a timing-bar position lands in. */
    fun zoneOf(release01: Float): String = when {
        release01 in Feel.ZONE_PERFECT_LO..Feel.ZONE_PERFECT_HI -> "perfect"
        release01 in Feel.ZONE_GOOD_LO..Feel.ZONE_GOOD_HI -> "good"
        release01 in Feel.ZONE_OK_LO..Feel.ZONE_OK_HI -> "ok"
        else -> "wild"
    }

    fun zonePower(zone: String): Double = when (zone) {
        "perfect" -> Feel.POWER_PERFECT
        "good" -> Feel.POWER_GOOD
        "ok" -> Feel.POWER_OK
        else -> Feel.POWER_WILD
    }

    /** The dodge window (the tail of the incoming flight you
     *  can actually react in). Shrinks as rounds wear on and
     *  against kids with arms on them. */
    fun dodgeWindowMs(round: Int, opponent: String): Long {
        val base = Feel.DODGE_BASE_MS - (round - 1) * Feel.DODGE_PER_ROUND_MS -
            dodgeFor(opponent) * Feel.DODGE_SKILL_MS
        return base.toLong().coerceIn(Feel.DODGE_MIN_MS, Feel.DODGE_MAX_MS)
    }

    fun dodgeWindowStart(round: Int, opponent: String): Long =
        FLIGHT_MS - dodgeWindowMs(round, opponent)
}

/**
 * One duel: rounds alternate your throw / their throw until
 * somebody eats [Showdown.HITS_TO_WIN] snowballs. All rng
 * flows from the seed in resolution order (throws only —
 * dodging is pure timing), so a seed + an input script
 * replays the same duel in the suite.
 */
class ShowdownDuel(seed: Long, val opponent: String) {
    private val rng = Random(seed)
    var youHits: Int = 0
        private set
    var oppHits: Int = 0
        private set
    /** 1-based; a round is your throw + their throw. */
    var round: Int = 1
        private set
    var over: Boolean = false
        private set
    var youWon: Boolean = false
        private set
    var settled: Boolean = false
        internal set

    /** Your throw, released at [release01] across the bar.
     *  Returns true when it lands. Reaching 3 ends the duel
     *  on the spot — they don't get a parting throw. */
    fun resolveThrow(release01: Float): Boolean {
        if (over) return false
        val chance = Showdown.zonePower(Showdown.zoneOf(release01)) *
            (1.0 - Showdown.dodgeFor(opponent))
        val hit = rng.nextDouble() < chance
        if (hit) {
            oppHits++
            if (oppHits >= Showdown.HITS_TO_WIN) {
                over = true
                youWon = true
            }
        }
        return hit
    }

    /** Their throw: you tapped [tapOffsetMs] into the flight
     *  (null = you never tapped). Inside the window (edges
     *  inclusive) = dodged; early = you flinched into it. */
    fun resolveDodge(tapOffsetMs: Long?): Boolean {
        if (over) return false
        val start = Showdown.dodgeWindowStart(round, opponent)
        val dodged = tapOffsetMs != null &&
            tapOffsetMs >= start && tapOffsetMs <= Showdown.FLIGHT_MS
        if (!dodged) {
            youHits++
            if (youHits >= Showdown.HITS_TO_WIN) {
                over = true
                youWon = false
            }
        }
        if (!over) round++
        return dodged
    }
}

data class ShowdownResult(
    val youWon: Boolean, val coins: Int, val friendshipDelta: Int,
    val line: String, val feats: List<String>
)

/** Feats a finished duel completes. Pure (see catchFeats). */
fun showdownFeats(state: GameState, d: ShowdownDuel): List<String> {
    if (!d.over) return emptyList()
    val out = mutableListOf<String>()
    if (d.youWon && d.youHits == 0 && "shutout" !in state.gameFeats) out.add("shutout")
    if (d.youWon && d.opponent == "Cartman" && "sore_loser" !in state.gameFeats)
        out.add("sore_loser")
    return out
}

/** The line the loser/winner exchange at the end. Kenny's
 *  are muffled (the parka is load-bearing); Cartman has a
 *  legal team of one (himself) and a statement either way. */
fun showdownLine(opponent: String, youWon: Boolean, playerName: String): String =
    when {
        opponent == "Kenny" && youWon ->
            "(muffled) Mmph mmph! *thumbs up, face still full of snow*"
        opponent == "Kenny" && !youWon ->
            "(muffled) MMPH! *victory wiggle* Mmmph mmph mmph!"
        opponent == "Cartman" && youWon ->
            "That is BULL. You cheated, $playerName! I saw the cheat! " +
                "My mom is calling your mom!"
        opponent == "Cartman" && !youWon ->
            "Nyeh nyeh nyeh NYEH nyeh! Weak, $playerName. WEAK. " +
                "The fort remains MINE."
        youWon -> listOf(
            "Okay. OKAY. You throw like you mean it, $playerName.",
            "Uncle. UNCLE. Tell my mom I died with honor.",
            "Best of seven? ...Best of nine? I have nowhere to be."
        )[(opponent.hashCode() and 0x7fffffff) % 3]
        else -> listOf(
            "The fort provides, $playerName. The fort provides.",
            "Good game. Terrible outcome for you, but good game.",
            "I have been training for this since the FIRST snow. You never stood a chance."
        )[(opponent.hashCode() and 0x7fffffff) % 3]
    }

/** Settle a finished duel, once: W/L record, capsule coins
 *  (5 for a win, 1 for showing up), the friendship swing
 *  through the town's own pipe, and feats. Cartman's swing
 *  runs backwards — beating him costs you, and so does
 *  losing to him. Everyone else respects the game. */
fun applyShowdownResult(state: GameState, d: ShowdownDuel): ShowdownResult? {
    if (d.settled || !d.over) return null
    d.settled = true
    val rec = state.showdown.getOrPut(d.opponent) { ShowdownRecord() }
    if (d.youWon) rec.wins++ else rec.losses++
    val coins = if (d.youWon) 5 else 1
    Minigames.awardCoins(state, coins)
    val delta = when {
        d.opponent == "Cartman" -> -2
        d.youWon -> 3
        else -> 1
    }
    Engine().relAdjust(state, d.opponent, delta)
    val feats = showdownFeats(state, d)
    for (f in feats) Minigames.awardFeat(state, f)
    // v0.4.0b: a win can settle somebody's bet (PLAY-showdown)
    Errands.onPlayShowdown(state, d.youWon)
    // v0.5.0d: a win can also defend a faction's banner
    FactionMissions.onShowdown(state, d.youWon, d.opponent)
    // v0.8.0b: and beat a capstone's named champion
    Capstones.onShowdown(state, d.youWon, d.opponent)
    // v0.8.0e: and settle an armed rival showdown contest —
    // a duel against the rival while the contest is armed
    // IS the contest (the W/L record above already counted
    // it as a duel; the scoreboard counts it as a contest)
    Rival.onShowdown(state, d.youWon, d.opponent)
    // v0.9.0e: and an armed crew showdown bracket settles
    // its other pairs on the same math — this duel was
    // the captains' pair (RivalCrew checks the arming
    // and the opponent, so an ordinary duel is untouched)
    RivalCrew.onShowdown(state, d.youWon, d.opponent)
    // v0.9.0b: and the crew standing with you watched it —
    // a witnessed showdown tallies on their page
    Crew.onShowdown(state)
    val me = state.agents.values.firstOrNull { it.isPlayer }
    if (me != null) {
        me.memories.add(
            if (d.youWon) "❄️ Beat ${d.opponent} at the Snowball Showdown, " +
                "${Showdown.HITS_TO_WIN}–${d.youHits}."
            else "❄️ Lost the Snowball Showdown to ${d.opponent}, " +
                "${d.oppHits}–${Showdown.HITS_TO_WIN}.")
    }
    return ShowdownResult(d.youWon, coins, delta,
        showdownLine(d.opponent, d.youWon, me?.name ?: "New Kid"), feats)
}

// ============================================================
// 🌮 TACO CATCH
// ============================================================

/** One thing falling off the Freeman's Tacos menu board.
 *  kind: taco | burrito | berry | tv. */
data class FallingItem(
    val id: Int, val atMs: Long, val x01: Float,
    val kind: String, val fallMs: Long
)

/**
 * One 60-second run. The whole spawn schedule is rolled up
 * front from the seed — kinds, positions, fall speeds, in a
 * fixed draw order — so the run is a data list the UI just
 * plays back, and the suite can pin every crumb of it.
 * Scoring lives here too (catches, hazards, stuns, tiers).
 */
class TacoCatchSession(seed: Long) {
    companion object { const val DURATION_MS = 60000L }

    val spawns: List<FallingItem>
    var score: Int = 0
        private set
    var caught: Int = 0
        private set
    var hazardsHit: Int = 0
        private set
    var missed: Int = 0
        private set
    /** Tray is frozen until this run-ms (hazard payback). */
    var stunUntilMs: Long = 0L
        private set
    var settled: Boolean = false
        internal set

    init {
        val rng = Random(seed)
        val list = mutableListOf<FallingItem>()
        var t = Feel.SPAWN_FIRST_MS
        var id = 0
        while (t < DURATION_MS - Feel.SPAWN_CUTOFF_MS) {
            val kindRoll = rng.nextInt(100)
            val kind = when {
                kindRoll < Feel.KIND_TACO_MAX -> "taco"
                kindRoll < Feel.KIND_BURRITO_MAX -> "burrito"
                kindRoll < Feel.KIND_BERRY_MAX -> "berry"
                else -> "tv"
            }
            val x = Feel.X_MARGIN + rng.nextFloat() * Feel.X_SPAN
            val fallBase = (Feel.FALL_BASE_MS - (t / 1000.0) * Feel.FALL_RAMP_PER_SEC)
                .coerceAtLeast(Feel.FALL_FLOOR_MS)
            val fall = (fallBase *
                (Feel.FALL_JITTER_LO + rng.nextFloat() * Feel.FALL_JITTER_SPAN)).toLong()
            list.add(FallingItem(id++, t, x, kind, fall))
            val interval = (Feel.INTERVAL_BASE_MS -
                (t / 1000.0) * Feel.INTERVAL_RAMP_PER_SEC)
                .coerceAtLeast(Feel.INTERVAL_FLOOR_MS).toLong() +
                rng.nextInt(Feel.INTERVAL_JITTER_MS)
            t += interval
        }
        spawns = list
    }

    fun pointsFor(kind: String): Int = when (kind) {
        "taco" -> Feel.POINTS_TACO
        "burrito" -> Feel.POINTS_BURRITO
        "berry" -> Feel.POINTS_BERRY
        "tv" -> Feel.POINTS_TV
        else -> 0
    }

    fun isHazard(kind: String): Boolean = kind == "berry" || kind == "tv"

    /** The tray caught [kind] at run-time [nowMs]. Applies the
     *  points (score never drops below zero) and, for hazards,
     *  the stun. Returns the points actually applied. */
    fun onCatch(kind: String, nowMs: Long): Int {
        val raw = pointsFor(kind)
        val before = score
        score = (score + raw).coerceAtLeast(0)
        if (isHazard(kind)) {
            hazardsHit++
            stunUntilMs = nowMs + if (kind == "tv") Feel.STUN_TV_MS else Feel.STUN_BERRY_MS
        } else {
            caught++
        }
        return score - before
    }

    /** A good item hit the floor. (Hazards hitting the floor
     *  is called "dodging" and pays nothing either way.) */
    fun onGround(kind: String) {
        if (!isHazard(kind)) missed++
    }

    fun stunnedAt(nowMs: Long): Boolean = nowMs < stunUntilMs
}

object TacoCatch {
    /** Capsule coins for a final score — the cabinet pays in
     *  the coin of the realm. (Table: Feel.COIN_TIERS.) */
    fun coinsForScore(score: Int): Int {
        for ((floor, coins) in Feel.COIN_TIERS) if (score >= floor) return coins
        return 0
    }
}

data class TacoResult(
    val score: Int, val coins: Int,
    val newHigh: Boolean, val feats: List<String>
)

/** Feats a finished run completes. Pure (see catchFeats). */
fun tacoFeats(state: GameState, score: Int): List<String> =
    if (score >= 500 && "taco_500" !in state.gameFeats) listOf("taco_500")
    else emptyList()

/** Settle a finished run, once: high score, play count,
 *  coins by tier, feats. */
fun applyTacoResult(state: GameState, s: TacoCatchSession): TacoResult? {
    if (s.settled) return null
    s.settled = true
    state.tacoPlays++
    val newHigh = s.score > state.tacoHigh
    if (newHigh) state.tacoHigh = s.score
    val coins = TacoCatch.coinsForScore(s.score)
    Minigames.awardCoins(state, coins)
    val feats = tacoFeats(state, s.score)
    for (f in feats) Minigames.awardFeat(state, f)
    // v0.4.0b: the final score can settle a dare (PLAY-taco)
    Errands.onPlayTaco(state, s.score)
    // v0.5.0d: and fund a faction's war chest (its TACO beat)
    FactionMissions.onTaco(state, s.score)
    // v0.8.0b: and fill a capstone's re-casting fund
    Capstones.onTaco(state, s.score)
    return TacoResult(s.score, coins, newHigh, feats)
}

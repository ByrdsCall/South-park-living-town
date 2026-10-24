package com.hunt3r.southparktown

import kotlin.math.roundToInt
import kotlin.random.Random

// ============================================================
// Walk — v0.5.0a (Queue #5 item 1): the town becomes a place
// you CROSS, not a menu you pick.
//
// Until now an exit tap teleported you along one graph edge
// for free. Now every trip runs on the town's real geography
// (the EXITS graph + the gx/gy grid Horizon already reads):
//
//  - ROUTING: a destination is reached leg by leg along the
//    shortest path over EXITS (BFS, neighbors in EXITS order —
//    the same (from, to) always walks the same streets, so the
//    geography is learnable). Each leg lands you IN the
//    intermediate location for real: its people, its weather,
//    its verbs. Walking on is one more tap on the next
//    signpost. Stopping is just... stopping.
//  - TIME: a leg costs minutes by true map distance
//    (MINUTES_PER_UNIT), banked into whole 15-minute ticks
//    through walkCarryMin — two short hops ≈ one tick, a
//    cross-town trek costs the better part of two hours, and
//    the town lives (Engine.inGameTick) while you walk.
//  - SHORTCUTS: the first time you walk a directed leg you
//    learn its cut-through (recorded in walkShortcuts); that
//    leg costs TICK_MINUTES less forever after (floored).
//  - ROAD BEATS: at a pass-through stop (never the final
//    destination), at most ONE ambient beat per trip may
//    fire from the pool below — an overheard argument, a
//    dropped thing, a recruiting pitch, a dog. Per-beat
//    cooldowns run in days; noisy beats keep quiet hours;
//    nothing fires while a case finale is on the table.
//  - THE BUS PASS: instant travel returns as an EARNED thing —
//    bought at the Mall transit kiosk for capsule coins, or
//    handed over free to a Town Fixture (50 errands). A ride
//    costs the minutes of ONE leg of the trip, whatever the
//    distance, and skips beats and pass-throughs entirely.
//
// All rules live here, pure; GameActivity only wires taps,
// dialogs and toasts to them. State is additive on GameState
// (walkDest / walkCarryMin / walkTrips / walkLegs /
// walkShortcuts / busPass) plus counters in the capped `life`
// map (trip serials, beat cooldowns, the morning record).
// ============================================================

data class WalkFeat(
    val id: String, val name: String, val desc: String,
    val at: Int, val kind: String, val title: String
)

data class RoadBeatDef(
    val id: String, val noisy: Boolean, val cooldownDays: Int
)

/** A beat that fired on a leg: its short ticker/morning form
 *  and the full card text the activity shows. */
data class FiredBeat(
    val id: String, val short: String, val text: String
)

data class LegOutcome(
    val moved: Boolean,
    val from: String,
    val to: String,
    val destId: String,          // the trip's destination ("" = the tap went nowhere)
    val arrived: Boolean,        // this leg completed the trip
    val passThrough: Boolean,    // landed mid-trip at a real stop
    val nextStop: String,        // where the trip goes next ("" when arrived/none)
    val minutes: Int,
    val ticks: Int,
    val shortcutFound: Boolean,  // this leg taught you its cut-through
    val beat: FiredBeat?,
    val flavor: String,          // one line of street flavor for the stroll card
    val tripDropped: Boolean     // the tap wandered off an earlier trip's route
)

object Walk {

    // ---------------- the dials ----------------
    /** Paper math: a neighborhood block (adjacent houses sit
     *  13 units apart) should take most of ten minutes on
     *  short kid legs — 0.6 min/unit puts a house hop at 8
     *  minutes and the longest edge in town (Main Street <->
     *  Tegridy Farms, 55.2 units) at 33. Two banked hops
     *  ≈ one 15-minute tick (see applyMinutes). */
    const val MINUTES_PER_UNIT = 0.6
    const val TICK_MINUTES = 15
    /** A known cut-through takes a full tick off its leg's
     *  minutes, floored so no leg is ever quite free. */
    const val SHORTCUT_SAVE_MINUTES = TICK_MINUTES
    const val MIN_LEG_MINUTES = 2

    /** Capsule coins for the pass at the Mall kiosk. The
     *  minigame economy pays 1–5 a session (showdown/taco
     *  tiers) and errands 0–3, so 25 is a real purchase —
     *  roughly a week of play — without being a second job. */
    const val BUS_PASS_PRICE = 25
    const val PASS_ITEM = "bus_pass"

    /** A pass-through stop rolls for a pool beat at this
     *  rate (after the shortcut and weather claims). */
    const val BEAT_CHANCE = 0.55

    const val BEAT_SHORTCUT = "shortcut"
    const val BEAT_WEATHER = "weather_turn"

    /** Decode cap for the shortcut set: exactly the number
     *  of directed edges in town. */
    val SHORTCUT_CAP: Int = EXITS.values.sumOf { it.size }

    // ---------------- feats ----------------
    val WALK_FEATS: List<WalkFeat> = listOf(
        WalkFeat("walk_10", "🚶 First Steps",
            "Complete 10 trips on foot. The town is walkable. You have the calves to prove it.",
            10, "trips", "First Steps"),
        WalkFeat("walk_50", "🚶 Commuter",
            "Complete 50 trips on foot. The bus driver waves at you now. You wave back. You never ride.",
            50, "trips", "Commuter"),
        WalkFeat("walk_shortcuts_10", "🗺 Local",
            "Learn 10 shortcuts. You don't take streets anymore. You take OPINIONS about streets.",
            10, "shortcuts", "Local")
    )

    fun featById(id: String): WalkFeat? = WALK_FEATS.firstOrNull { it.id == id }

    fun featCount(state: GameState, f: WalkFeat): Int = when (f.kind) {
        "shortcuts" -> state.walkShortcuts.size
        else -> state.walkTrips
    }

    fun featEarned(state: GameState, f: WalkFeat): Boolean =
        featCount(state, f) >= f.at

    // ---------------- geography ----------------

    fun legKey(from: String, to: String): String = "$from>$to"

    fun distOf(from: String, to: String): Float {
        val a = LOCATIONS[from] ?: return 0f
        val b = LOCATIONS[to] ?: return 0f
        return Horizon.distFor(b.gx - a.gx, b.gy - a.gy)
    }

    /** The raw minutes of one leg, before any shortcut. */
    fun legMinutesRaw(from: String, to: String): Int =
        (distOf(from, to) * MINUTES_PER_UNIT).roundToInt()
            .coerceAtLeast(MIN_LEG_MINUTES)

    /** The minutes this leg costs THIS save: known
     *  cut-throughs shave a tick off, floored. */
    fun legMinutes(state: GameState, from: String, to: String): Int {
        val raw = legMinutesRaw(from, to)
        return if (legKey(from, to) in state.walkShortcuts)
            (raw - SHORTCUT_SAVE_MINUTES).coerceAtLeast(MIN_LEG_MINUTES)
        else raw
    }

    /** Shortest path over EXITS from [from] to [to], both
     *  endpoints included; empty when unreachable (the town
     *  graph is connected — the suite proves it). Neighbors
     *  are explored in EXITS order and parents are first-
     *  found, so the route is a pure function of (from, to):
     *  the same trip always walks the same streets. */
    fun route(from: String, to: String): List<String> {
        if (from !in LOCATIONS || to !in LOCATIONS) return emptyList()
        if (from == to) return listOf(from)
        val prev = HashMap<String, String>()
        val seen = HashSet<String>()
        seen += from
        val queue = mutableListOf(from)
        var head = 0
        while (head < queue.size) {
            val cur = queue[head++]
            for (nxt in EXITS[cur].orEmpty()) {
                if (nxt in seen) continue
                seen += nxt
                prev[nxt] = cur
                if (nxt == to) {
                    val path = mutableListOf(to)
                    var c = to
                    while (c != from) {
                        c = prev.getValue(c)
                        path += c
                    }
                    return path.reversed()
                }
                queue += nxt
            }
        }
        return emptyList()
    }

    /** Total minutes of a whole trip on this save (shortcut
     *  discounts as they stand) — the picker's "~N min".
     *  v0.13.0d: on a live parade day the closed stretch
     *  prices in — when the route's INTERMEDIATE stops
     *  include Main Street, the trip waits one stated
     *  tick (+15) to get through the parade; trips TO or
     *  FROM Main Street pay nothing (you are going to the
     *  parade, or leaving it), and the route itself never
     *  changes: parade traffic yields, geography doesn't. */
    fun routeMinutes(state: GameState, from: String, to: String): Int {
        val path = route(from, to)
        var total = 0
        for (i in 0 until path.size - 1) total += legMinutes(state, path[i], path[i + 1])
        if (path.size > 2 && Parade.streetClosed(state) &&
            path.subList(1, path.size - 1)
                .contains(Parade.STREET)) {
            total += Parade.PASS_DELAY_MIN
        }
        return total
    }

    /** The bus is fast, not free: a ride costs the whole
     *  route's minutes at bus speed — BusNetwork owns the
     *  derivation (v0.15.0c retired the old flat "one leg,
     *  any distance" price, which charged a cross-town
     *  ride the same as a two-minute hop). */
    fun rideCostMinutes(from: String, to: String): Int =
        BusNetwork.rideMinutes(from, to)

    // ---------------- trip state ----------------

    /** The destination of the trip in progress, or null. */
    fun activeDest(state: GameState): String? =
        state.walkDest.takeIf { it in LOCATIONS }

    /** The next stop on the active trip from where the
     *  player stands, or null when no trip is live. */
    fun nextStop(state: GameState): String? {
        val dest = activeDest(state) ?: return null
        val from = Engine().player(state).location
        if (from == dest) return null
        val path = route(from, dest)
        return if (path.size >= 2) path[1] else null
    }

    private fun startTrip(state: GameState, destId: String) {
        state.walkDest = destId
        state.life["walkSerial"] = (state.life["walkSerial"] ?: 0) + 1
    }

    private fun cancelTrip(state: GameState) {
        state.walkDest = ""
    }

    /** True while a mystery finale choice sits on the table
     *  — the road stays quiet then; some moments are the
     *  whole point of the walk home and some are not. */
    fun caseCritical(state: GameState): Boolean =
        Mystery.finaleChoiceDue(state) ||
            Mystery2.accusationDue(state) ||
            Mystery3.dispositionDue(state) ||
            Mystery4.vigilDue(state) ||
            Mystery5.confrontationDue(state) ||
            Mystery6.meetingDue(state) ||
            Mystery7.restageDue(state) ||
            Mystery8.readingDue(state) ||
            Mystery9.auditDue(state) ||
            Mystery10.hangingDue(state) ||
            Mystery11.voicingDue(state) ||
            Mystery12.returnsDue(state) ||
            Mystery13.verdictDue(state)

    // ---------------- time ----------------

    /** Bank [minutes] into the town clock: whole 15-minute
     *  ticks run as real inGameTicks (the town lives while
     *  you walk); the remainder carries in walkCarryMin, so
     *  two short hops cost about one tick between them.
     *  With [advance] false (real-time sync owns the clock)
     *  nothing is banked or ticked. Returns the ticks run. */
    fun applyMinutes(state: GameState, minutes: Int, advance: Boolean = true): Int {
        if (minutes <= 0 || !advance) return 0
        val total = state.walkCarryMin + minutes
        val ticks = total / TICK_MINUTES
        state.walkCarryMin = total % TICK_MINUTES
        if (ticks > 0) {
            val eng = Engine()
            repeat(ticks) { eng.inGameTick(state) }
        }
        return ticks
    }

    // ---------------- the walk itself ----------------

    /** The one door for signpost taps and picker choices:
     *  walk ONE leg toward [destId] from wherever the player
     *  stands. Adjacent taps on the live trip's route keep
     *  the trip; any other tap drops the old plan (a detour
     *  is a new trip, or a one-leg stroll when adjacent). */
    fun walkTo(state: GameState, destId: String, rnd: Random,
               advance: Boolean = true): LegOutcome {
        val eng = Engine()
        val from = eng.player(state).location
        val flavor = flavorFor(state, rnd)
        if (destId !in LOCATIONS || destId == from) {
            return LegOutcome(false, from, from,
                state.walkDest, false, false, "", 0, 0,
                false, null, flavor, false)
        }
        val adjacent = destId in EXITS[from].orEmpty()
        val active = activeDest(state)
        val onRoute = active != null &&
            (destId == active || destId == nextStop(state))
        var dropped = false
        if (!onRoute) {
            if (active != null) {
                cancelTrip(state)
                dropped = true
            }
            startTrip(state, destId)
        }
        val out = walkLeg(state, rnd, advance)
        return if (out == null) {
            LegOutcome(false, from, from, state.walkDest, false, false, "",
                0, 0, false, null, flavor, dropped)
        } else out.copy(flavor = flavor, tripDropped = dropped)
    }

    /** Walk the next leg of the active trip. The leg moves
     *  the player for real (Engine.playerGo: visits, activity,
     *  the ticker line), charges its minutes as ticks, and —
     *  at a pass-through stop or on a first traversal — may
     *  fire the trip's one road beat. */
    fun walkLeg(state: GameState, rnd: Random,
                advance: Boolean = true): LegOutcome? {
        val eng = Engine()
        val me = eng.player(state)
        val dest = activeDest(state) ?: return null
        val from = me.location
        if (from == dest) {
            cancelTrip(state)
            return LegOutcome(false, from, from, dest, true, false, "",
                0, 0, false, null, "", false)
        }
        val path = route(from, dest)
        if (path.size < 2) {
            cancelTrip(state)
            return null
        }
        val next = path[1]
        val key = legKey(from, next)
        val known = key in state.walkShortcuts
        var minutes = legMinutes(state, from, next)
        // v0.13.0d: 🎺 the parade's street closure, charged
        // on the leg that ENTERS Main Street mid-trip (not
        // on arrival — going TO the parade is free) on a
        // live parade day: one tick, waiting for a float's
        // width. routeMinutes prices the same tick, so the
        // picker and the charge agree.
        if (next == Parade.STREET && next != dest &&
            Parade.streetClosed(state)) {
            minutes += Parade.PASS_DELAY_MIN
        }
        val weatherBefore = state.weatherId
        eng.playerGo(state, next)
        val ticks = applyMinutes(state, minutes, advance)
        state.walkLegs++
        val arrived = next == dest

        var beat: FiredBeat? = null
        var found = false
        val slotFree = (state.life["walkBeatSerial"] ?: -1) !=
            (state.life["walkSerial"] ?: 0)
        if (slotFree && !caseCritical(state)) {
            when {
                // a first traversal teaches its cut-through —
                // the leg's own reward, destination or not
                !known -> {
                    state.walkShortcuts += key
                    found = true
                    beat = fireBeat(state, BEAT_SHORTCUT,
                        "🗺 Shortcut learned: ${LOCATIONS[from]?.name} → " +
                            "${LOCATIONS[next]?.name}",
                        "🗺 SHORTCUT — halfway along, you spot it: a gap in " +
                            "the fence, a cut through the lot, the way the " +
                            "locals actually go. ${LOCATIONS[from]?.name} → " +
                            "${LOCATIONS[next]?.name} will cost you a tick " +
                            "less from now on. The town is getting smaller, " +
                            "in the good way.")
                    checkFeatCrossings(state)
                }
                // the sky turned while your back was to it
                !arrived && state.weatherId != weatherBefore &&
                    cooldownOk(state, BEAT_WEATHER) ->
                    beat = fireWeatherBeat(state)
                !arrived -> beat = rollPoolBeat(state, next, rnd)
            }
        }

        if (arrived) {
            cancelTrip(state)
            state.walkTrips++
            checkFeatCrossings(state)
        }
        val nextStop = if (arrived) "" else
            route(next, dest).getOrNull(1) ?: ""
        return LegOutcome(true, from, next, dest, arrived, !arrived,
            nextStop, minutes, ticks, found, beat, "", false)
    }

    // ---------------- road beats ----------------

    val POOL: List<RoadBeatDef> = listOf(
        RoadBeatDef("argument", noisy = true, cooldownDays = 2),
        RoadBeatDef("dropped", noisy = false, cooldownDays = 2),
        RoadBeatDef("pitch", noisy = true, cooldownDays = 3),
        RoadBeatDef("dog", noisy = false, cooldownDays = 3),
        RoadBeatDef("wave", noisy = false, cooldownDays = 1),
        RoadBeatDef("directions", noisy = false, cooldownDays = 2),
        RoadBeatDef("busker", noisy = true, cooldownDays = 3),
        RoadBeatDef("gnome_road", noisy = false, cooldownDays = 5)
    )

    private val DROPPED_POOL = listOf(
        "bottle_cap", "shiny_rock", "snack_cake", "old_sock", "comic_book")

    /** Topic tags the Threads banks know how to follow up —
     *  an overheard fight plants one in both memories. */
    private val ARGUMENT_TOPICS = listOf(
        "food", "money", "games", "music", "school", "family")

    private val ARGUMENT_SPATS: List<Pair<String, String>> = listOf(
        "I'm just SAYING, if the nachos are shared, they're SHARED." to
            "You ate the whole top layer. THE WHOLE TOP. That's not sharing, that's archaeology.",
        "Dude. Dude. You cannot return a fish. It's a fish. It's yours now." to
            "The receipt says thirty days! It doesn't say the fish has to be — okay the fish is dead, that's on me.",
        "It's not a phase, it's a LIFESTYLE. The cloak is machine washable." to
            "You wore it to breakfast. To BREAKFAST. The waffles were scared.",
        "I'm telling you, the bus comes when it wants. It's a philosophy." to
            "It's a SCHEDULE. There is a printed schedule. I have seen the schedule.",
        "Five dollars says the pond thing is just a big frog." to
            "You owe me five dollars from the LAST pond bet. This town runs on pond debt now.")

    private val PITCH_LINES: List<String> = listOf(
        "Psst. {faction}. We're not saying the others are wrong. We're saying we're RIGHT, out loud, near you.",
        "You walk these streets like one of us. {faction} could use feet like yours. Think about it.",
        "No pressure, but {faction} noticed you noticing us. That's basically step one.",
        "Between us? {faction} runs this town's soul. The others run its errands.")

    private val DOG_LINES: List<String> = listOf(
        "A dog falls in beside you like it pays rent on this street. It walks your whole leg in perfect step, barks ONCE at absolutely nothing, and peels off at the corner like its shift ended.",
        "A scruffy dog adopts you for exactly one block. No collar, no agenda, excellent company. At the corner it looks at you like 'we're good, right?' and trots home.",
        "There's a dog walking with you now. There wasn't. There is. It barks a single, formal bark — a punctuation mark — and considers its duties done.")

    private val BUSKER_LINES: List<String> = listOf(
        "Somebody's busking on the corner — a kazoo, a bucket, and total commitment. A passerby tosses a coin without breaking stride. The coin misses the bucket. The kazoo does not care.",
        "Street music: one kid, one recorder, three notes, infinite confidence. It's not good. It's not BAD either. It's South Park's whole deal in one kid.")

    private val WAVE_LINES: List<String> = listOf(
        "{name} spots you mid-stride and throws a wave like you were the parade.",
        "\"{name}!\" — no wait, that's YOUR name you're hearing. {name} is yelling it, waving both arms, delighted you're out walking.",
        "{name} was mid-sentence to nobody, saw you, and rerouted the whole sentence into a hello.")

    private fun cooldownOk(state: GameState, beatId: String): Boolean {
        val def = POOL.firstOrNull { it.id == beatId }
        val cd = def?.cooldownDays ?: 3
        val last = state.life["rb:$beatId"] ?: Int.MIN_VALUE / 2
        return state.absDay - last >= cd
    }

    private fun caseGated(state: GameState, def: RoadBeatDef): Boolean {
        if (!cooldownOk(state, def.id)) return false
        if (def.noisy && isSleepHour(state.timeMin / 60)) return false
        return true
    }

    /** The weather-turn beat: the sky changed during the
     *  leg's ticks, and the walk notices out loud. */
    private fun fireWeatherBeat(state: GameState): FiredBeat {
        val w = WEATHER[state.weatherId]
        val label = w?.label ?: state.weatherId
        val text = "🌦️ THE SKY, MID-WALK — you left under one sky and you're " +
            "arriving under another: $label. ${w?.desc ?: ""} South Park " +
            "weather doesn't change. It REPLACES."
        return fireBeat(state, BEAT_WEATHER,
            "🌦️ The weather turned mid-walk — $label now", text)
    }

    private fun rollPoolBeat(state: GameState, atLoc: String,
                             rnd: Random): FiredBeat? {
        if (rnd.nextDouble() >= BEAT_CHANCE) return null
        val eng = Engine()
        val present = eng.presentWithPlayer(state).filter { !it.asleep }
        val cands = POOL.filter { def ->
            caseGated(state, def) && when (def.id) {
                "argument" -> present.size >= 2
                "pitch" -> present.any { FACTION_OF_CAST[it.name] != null }
                "wave" -> present.isNotEmpty()
                else -> true
            }
        }
        if (cands.isEmpty()) return null
        val def = cands[rnd.nextInt(cands.size)]
        return firePoolBeat(state, def, atLoc, present.map { it.name }, rnd)
    }

    private fun firePoolBeat(state: GameState, def: RoadBeatDef, atLoc: String,
                             presentNames: List<String>, rnd: Random): FiredBeat {
        val eng = Engine()
        val place = LOCATIONS[atLoc]?.name ?: atLoc
        return when (def.id) {
            "argument" -> {
                val a = presentNames[rnd.nextInt(presentNames.size)]
                var b = presentNames[rnd.nextInt(presentNames.size)]
                if (b == a) b = presentNames.first { it != a }
                val spat = ARGUMENT_SPATS[rnd.nextInt(ARGUMENT_SPATS.size)]
                val topic = ARGUMENT_TOPICS[rnd.nextInt(ARGUMENT_TOPICS.size)]
                state.lastConversation = listOf(
                    Line(a, spat.first), Line(b, spat.second))
                for (who in listOf(a, b)) {
                    val mem = NpcMemoryStore.getOrCreate(who)
                    mem.topics.remove(topic)
                    mem.topics.add(topic)
                    while (mem.topics.size > 6) mem.topics.removeAt(0)
                    val agent = state.agents[who]
                    if (agent != null) {
                        agent.memories.add(
                            "🗣 Argued with ${if (who == a) b else a} about $topic, right in front of the new kid.")
                        while (agent.memories.size > 10) agent.memories.removeAt(0)
                    }
                }
                fireBeat(state, def.id,
                    "🗣 Overheard at $place: $a and $b, mid-argument",
                    "🗣 OVERHEARD AT ${place.uppercase()} — you slow down. " +
                        "Everybody slows down. It's the law of the sidewalk.\n\n" +
                        "$a: \"${spat.first}\"\n$b: \"${spat.second}\"\n\n" +
                        "It is, somehow, about $topic. It is ALWAYS about $topic.")
            }
            "dropped" -> {
                if (rnd.nextBoolean()) {
                    val itemId = DROPPED_POOL[rnd.nextInt(DROPPED_POOL.size)]
                    state.inventory[itemId] = (state.inventory[itemId] ?: 0) + 1
                    val item = itemById(itemId)
                    fireBeat(state, def.id,
                        "👀 Something in the slush at $place — ${item?.name ?: itemId}",
                        "👀 IN THE SLUSH AT ${place.uppercase()} — half-buried, " +
                            "glinting: ${item?.icon ?: ""} ${item?.name ?: itemId}. " +
                            "Dropped by somebody, missed by nobody, yours by the " +
                            "ancient law of finders. Into the pack it goes.")
                } else {
                    Minigames.awardCoins(state, 1)
                    fireBeat(state, def.id,
                        "👀 A capsule coin in the gutter at $place (+1 🪙)",
                        "👀 GUTTER TREASURE AT ${place.uppercase()} — a capsule " +
                            "coin, winking up from the slush like it fell out of " +
                            "the town's pocket. +1 🪙. The economy provides.")
                }
            }
            "pitch" -> {
                val who = presentNames.filter { FACTION_OF_CAST[it] != null }
                    .let { it[rnd.nextInt(it.size)] }
                val fid = FACTION_OF_CAST.getValue(who)
                val fname = FACTION_BY_ID[fid]?.name ?: fid
                val fs = state.factionStates[fid]
                if (fs != null)
                    fs.playerStanding = (fs.playerStanding + 2).coerceIn(-50, 100)
                val line = PITCH_LINES[rnd.nextInt(PITCH_LINES.size)]
                    .replace("{faction}", fname)
                fireBeat(state, def.id,
                    "🎣 $who made a pitch for $fname at $place",
                    "🎣 THE PITCH AT ${place.uppercase()} — $who falls in " +
                        "beside you for exactly ten paces, which is how all " +
                        "recruiting works here.\n\n\"$line\"\n\n" +
                        "(+2 standing with $fname. Ten paces. Zero paperwork.)")
            }
            "dog" -> {
                val line = DOG_LINES[rnd.nextInt(DOG_LINES.size)]
                fireBeat(state, def.id,
                    "🐕 A dog walked your leg at $place",
                    "🐕 COMPANY AT ${place.uppercase()} — $line")
            }
            "wave" -> {
                val who = presentNames[rnd.nextInt(presentNames.size)]
                eng.relAdjust(state, who, 1)
                val line = WAVE_LINES[rnd.nextInt(WAVE_LINES.size)]
                    .replace("{name}", who)
                fireBeat(state, def.id,
                    "👋 $who waved you down at $place (+1 💛)",
                    "👋 HAILED AT ${place.uppercase()} — $line (+1 💛 $who)")
            }
            "directions" -> {
                TownLife.repAdd(state, 1)
                val week = (state.gameDay + (state.gameMonth - 1) * 31) / 7
                val visitor = try {
                    VisitorSystem.presentAt(atLoc, state.playerInside,
                        state.todayKey, state.timeMin, week, dayOfWeek(state))
                        .firstOrNull()?.persona?.loreName
                } catch (e: Exception) { null }
                val who = visitor ?: "A tourist in a parka two sizes too proud"
                fireBeat(state, def.id,
                    "🧭 You gave directions at $place (+rep)",
                    "🧭 DIRECTIONS AT ${place.uppercase()} — $who stops you, " +
                        "phone in hand, map upside down in every way that " +
                        "matters. You point. You point again, slower. A light " +
                        "dawns. \"Oh! It's RIGHT there.\" It was right there. " +
                        "(+1 town rep — the streets pay their guides.)")
            }
            "busker" -> {
                val line = BUSKER_LINES[rnd.nextInt(BUSKER_LINES.size)]
                fireBeat(state, def.id,
                    "🎵 Busking on the corner at $place",
                    "🎵 ON THE CORNER AT ${place.uppercase()} — $line")
            }
            "gnome_road" -> {
                state.life["walkGnomes"] = (state.life["walkGnomes"] ?: 0) + 1
                fireBeat(state, def.id,
                    "🧌 The gnome. Here now. It travels too",
                    "🧌 AT ${place.uppercase()} — the gnome is here. It was " +
                        "somewhere else yesterday, and it will be somewhere " +
                        "else tomorrow, and nobody has ever seen it move. " +
                        "You nod at it. It does not nod back. It has never " +
                        "nodded back. The nodding is the point.")
            }
            else -> fireBeat(state, def.id, "🚶 Something on the road",
                "🚶 The road provided. Details unclear. Vibes immaculate.")
        }
    }

    /** Common firing bookkeeping: the trip's one-beat slot,
     *  the per-beat cooldown, the ticker line, and the
     *  morning record (House.morningReport reads it back —
     *  yesterday's road, in one line, never a spoiler). */
    private fun fireBeat(state: GameState, beatId: String,
                         short: String, text: String): FiredBeat {
        state.life["walkBeatSerial"] = state.life["walkSerial"] ?: 0
        if (beatId != BEAT_SHORTCUT) state.life["rb:$beatId"] = state.absDay
        state.events.add(short)
        while (state.events.size > 40) state.events.removeAt(0)
        if (state.life["roadBeatDay"] != state.absDay) {
            state.life["roadBeatDay"] = state.absDay
            state.life["roadBeatCount"] = 1
        } else {
            state.life["roadBeatCount"] = (state.life["roadBeatCount"] ?: 1) + 1
        }
        state.lifeText["roadBeat"] = short
        return FiredBeat(beatId, short, text)
    }

    private fun checkFeatCrossings(state: GameState) {
        for (f in WALK_FEATS) {
            val key = "walkFeat:${f.id}"
            if (state.life[key] == 1) continue
            if (featEarned(state, f)) {
                state.life[key] = 1
                state.events.add("${f.name} — walk feat complete!")
                while (state.events.size > 40) state.events.removeAt(0)
                townLogLine(state, "🗺 ${f.title} — ${featCount(state, f)} " +
                    "${if (f.kind == "shortcuts") "shortcuts learned" else "trips walked"} " +
                    "in South Park")
            }
        }
    }

    // ---------------- street flavor ----------------

    private val STREET_FLAVOR: List<String> = listOf(
        "the slush sings under your boots",
        "somewhere, a screen door slams in agreement",
        "the mountains watch. They've seen worse walks",
        "a tumbleweed of grocery bags rolls past, on business",
        "the cold makes your nose run and your resolve firm",
        "every porch light in town is on — it's that kind of street",
        "you count mailboxes to pass the time. There are many",
        "the snow squeaks the exact note of a kid yelling, far away")

    private val STREET_FLAVOR_BAD: List<String> = listOf(
        "the weather leans in to make the walk a story",
        "you walk like the weather owes you money",
        "every step is a small negotiation with the sky")

    /** One line of street flavor for the stroll card —
     *  weather-aware, seeded by the walk itself. */
    fun flavorFor(state: GameState, rnd: Random): String {
        val pool = if (state.weatherId in
            setOf("heavy_snow", "blizzard", "rain", "storm"))
            STREET_FLAVOR_BAD else STREET_FLAVOR
        return pool[rnd.nextInt(pool.size)]
    }

    // ---------------- the bus pass ----------------

    fun hasPass(state: GameState): Boolean = state.busPass

    /** Town Fixture = the 50-errand feat (Errands' own table
     *  is the source of truth for the threshold). */
    fun townFixture(state: GameState): Boolean =
        Errands.featById("errand_50")?.let { Errands.featEarned(state, it) } == true

    /** Hand the pass over. The flag is the authority; the
     *  card itself lands in the pack as a "key" item. */
    fun grantPass(state: GameState, why: String): Boolean {
        if (state.busPass) return false
        state.busPass = true
        state.inventory[PASS_ITEM] = (state.inventory[PASS_ITEM] ?: 0) + 1
        state.events.add("🚌 BUS PASS — $why")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🚌 The new kid got a bus pass ($why)")
        return true
    }

    /** Retroactive grant, called on load: a save that earned
     *  Town Fixture before the pass existed gets it the
     *  moment the town next sees them. */
    fun ensureFeatPass(state: GameState): Boolean =
        !state.busPass && townFixture(state) &&
            grantPass(state, "the Town Fixture perk, claimed late")

    /** The Mall kiosk transaction. Returns "owned" |
     *  "granted" (the Town Fixture perk) | "bought" | "poor". */
    fun buyPass(state: GameState): String {
        if (state.busPass) return "owned"
        if (townFixture(state)) {
            grantPass(state, "the Town Fixture perk — the kiosk hands it over free")
            return "granted"
        }
        val coins = Minigames.capsuleCoins(state)
        if (coins < BUS_PASS_PRICE) return "poor"
        val left = coins - BUS_PASS_PRICE
        if (left > 0) state.inventory[Minigames.COIN_ITEM] = left
        else state.inventory.remove(Minigames.COIN_ITEM)
        grantPass(state, "bought at the Mall kiosk for $BUS_PASS_PRICE 🪙")
        return "bought"
    }

    data class RideOutcome(
        val ok: Boolean, val reason: String,
        val minutes: Int, val ticks: Int
    )

    /** Ride the bus to [destId]: no legs, no beats, no
     *  pass-throughs — just the route's minutes at bus
     *  speed (BusNetwork), charged through the same
     *  banked-tick conversion as walking. */
    fun ride(state: GameState, destId: String,
             advance: Boolean = true): RideOutcome {
        if (!state.busPass) return RideOutcome(false, "nopass", 0, 0)
        val eng = Engine()
        val from = eng.player(state).location
        if (destId !in LOCATIONS || destId == from)
            return RideOutcome(false, "nowhere", 0, 0)
        val minutes = rideCostMinutes(from, destId)
        cancelTrip(state)
        eng.playerGo(state, destId)
        val ticks = applyMinutes(state, minutes, advance)
        state.life["walkRides"] = (state.life["walkRides"] ?: 0) + 1
        return RideOutcome(true, "", minutes, ticks)
    }

    // ---------------- the Journal page ----------------

    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🗺 WALKS — ${state.walkTrips} trip${if (state.walkTrips == 1) "" else "s"} " +
            "on foot · ${state.walkLegs} legs · ${state.walkShortcuts.size} shortcut" +
            "${if (state.walkShortcuts.size == 1) "" else "s"} known"
        val rides = state.life["walkRides"] ?: 0
        if (rides > 0) out += "🚌 Bus rides taken: $rides (the driver nods at you now)"
        out += if (state.busPass)
            "🚌 Bus pass: IN YOUR WALLET — rides cost the route's minutes " +
                "at bus speed (BusNetwork prices them). The walking is still " +
                "better. You know it. The bus knows it."
        else
            "🚌 Bus pass: not yet — the transit kiosk inside the Mall sells one " +
                "for $BUS_PASS_PRICE 🪙. Town Fixtures (50 errands) get it free."
        val dest = activeDest(state)
        if (dest != null) {
            val from = Engine().player(state).location
            val next = nextStop(state)
            out += "▶ ON THE ROAD TO ${LOCATIONS[dest]?.name} — next stop " +
                "${LOCATIONS[next]?.name ?: "—"}. Tap its signpost to keep " +
                "walking, or stay: you're really at ${LOCATIONS[from]?.name} " +
                "until you do."
        }
        for (f in WALK_FEATS) {
            val n = featCount(state, f)
            out += if (n >= f.at) "✅ ${f.name} — done (${f.title})"
            else "⬜ ${f.name} — $n/${f.at} ${f.kind}"
        }
        if (state.walkLegs == 0) {
            out += "You haven't walked anywhere yet. The signposts work, " +
                "promise — and the menu's 🗺 Go Somewhere walks you clear " +
                "across town, one real street at a time."
        }
        return out
    }
}

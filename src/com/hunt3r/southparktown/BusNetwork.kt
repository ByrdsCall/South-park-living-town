package com.hunt3r.southparktown

// ============================================================
// BusNetwork — v0.15.0c, the bus made real (Lucky's
// overhaul steering): the school bus actually takes you to
// school and back, and a PUBLIC BUS network links stations
// all over town. Tap a station (you walk to it — PaperWalk
// owns that), the bus pulls up, you pick a destination
// station, and you ride: the minutes are charged, the bus
// is seen to travel, and you step off at the far station.
//
// PRICING (the honesty rule, suite-pinned): a ride costs
// the minutes of the SAME route walked, priced at bus
// speed — [BUS_SHARE] of the route's raw walking minutes,
// derived leg by leg from the Walk graph itself
// (Walk.legMinutesRaw over Walk.route), floored at
// [MIN_RIDE_MIN] so no ride is ever free or a teleport,
// and always under the walked price on any real route.
// The bus ignores the parade's through-street delay, as
// it always has: parade traffic yields to the bus.
//
// The old flat rule ("one leg's minutes, any distance")
// is retired by this derivation — it priced a cross-town
// ride the same as a two-minute hop. Walk.rideCostMinutes
// delegates here, so the picker, the station dialog and
// the Journal all quote the same number.
//
// The SCHOOL BUS is its own thing: no pass (kids ride the
// school bus), the BusStop ⇄ Elementary run only, at the
// morning and evening windows on school days — the run
// the town's kids already live by (Engine's lunchbox kid
// catches it; now so can you).
// ============================================================

object BusNetwork {

    /** Station stops, spread over the town: the hub stop,
     *  downtown, the school, the civic center, the mall,
     *  the park, the pond, the church corner, SoDoSoPa and
     *  the farm gate. PaperDressing reads this same list
     *  for its busStation flag — one list, two readers,
     *  pinned to agree. */
    val STATIONS: Set<String> = linkedSetOf(
        "BusStop", "MainStreet", "Elementary", "CityHall",
        "Mall", "KidsPark", "StarksPond", "Church",
        "SoDoSoPa", "TegridyFarms")

    /** The bus covers a route in this share of the time
     *  feet do — faster than walking (that is its whole
     *  purpose) and never absurdly so. */
    const val BUS_SHARE_NUM = 2
    const val BUS_SHARE_DEN = 5

    /** No ride costs less than this: the bus still has to
     *  come and get you. Never free, never a teleport. */
    const val MIN_RIDE_MIN = 10

    /** The route's raw walking minutes, leg by leg off the
     *  Walk graph — bus-ignorant (no shortcut shaving, no
     *  parade delay: the bus runs its own road). */
    fun rawRouteMinutes(from: String, to: String): Int {
        val path = Walk.route(from, to)
        var total = 0
        for (i in 0 until path.size - 1)
            total += Walk.legMinutesRaw(path[i], path[i + 1])
        return total
    }

    /** What a ride from [from] to [to] costs, in game
     *  minutes: the route at bus speed, floored. */
    fun rideMinutes(from: String, to: String): Int {
        val raw = rawRouteMinutes(from, to)
        if (raw <= 0) return 0
        val scaled = (raw * BUS_SHARE_NUM + BUS_SHARE_DEN - 1) / BUS_SHARE_DEN
        return maxOf(MIN_RIDE_MIN, scaled)
    }

    fun isStation(locId: String): Boolean = locId in STATIONS

    /** Stations you can ride TO from [fromId], with their
     *  prices — the station picker's data. */
    fun destinationsFrom(fromId: String): List<Pair<String, Int>> =
        STATIONS.filter { it != fromId && it in LOCATIONS }
            .map { it to rideMinutes(fromId, it) }
}

/** The school run: BusStop ⇄ Elementary, mornings and
 *  after school, on school days. Free of coins and passes
 *  (it's the school bus); the minutes are the network's
 *  own derivation, charged through the same banked-tick
 *  conversion as every trip (Walk.applyMinutes). */
/** v0.16.0a (items 8+9): the state of BEING on a bus.
 *  Boarding no longer teleports: the player steps into a
 *  real bus interior (GameView paints the bus layout
 *  while this record stands), sits down, and the ride
 *  completes when they tap the route board / door to
 *  alight — GameActivity then runs the existing ride
 *  (Walk.ride / SchoolBus.ride) so the minutes, prices
 *  and ledgers are the very same ones as ever. Stored in
 *  lifeText["busRide"] as "kind|from|to". */
object BusRideState {
    const val KEY = "busRide"
    data class Ride(val kind: String, val from: String, val to: String) {
        val school: Boolean get() = kind == "school"
    }

    fun read(state: GameState): Ride? {
        val raw = state.lifeText[KEY] ?: return null
        val parts = raw.split("|")
        if (parts.size != 3) return null
        return Ride(parts[0], parts[1], parts[2])
    }

    fun board(state: GameState, kind: String, from: String, to: String) {
        state.lifeText[KEY] = "$kind|$from|$to"
        state.playerInside = true
    }

    fun clear(state: GameState) {
        state.lifeText.remove(KEY)
    }

    fun active(state: GameState): Boolean = read(state) != null
}

object SchoolBus {
    const val STOP = "BusStop"
    const val SCHOOL = "Elementary"

    /** The morning window: the bus waits at the stop from
     *  6:50 and the last one rolls at 8:20 — school starts
     *  at 8:30, and the run itself takes the derived time. */
    const val MORNING_FROM = 6 * 60 + 50
    const val MORNING_TO = 8 * 60 + 20

    /** The evening window: from the 3:05 bell to 4:40. */
    const val EVENING_FROM = 15 * 60 + 5
    const val EVENING_TO = 16 * 60 + 40

    fun morningWaiting(state: GameState): Boolean =
        School.isSchoolDay(state.absDay) &&
            state.timeMin in MORNING_FROM..MORNING_TO

    fun eveningWaiting(state: GameState): Boolean =
        School.isSchoolDay(state.absDay) &&
            state.timeMin in EVENING_FROM..EVENING_TO

    /** Is the school bus standing at [locId] right now? */
    fun busAt(state: GameState, locId: String): Boolean =
        (locId == STOP && morningWaiting(state)) ||
            (locId == SCHOOL && eveningWaiting(state))

    /** Where the bus at [locId] would take you, or null
     *  when no bus stands there. */
    fun destFrom(state: GameState, locId: String): String? = when {
        locId == STOP && morningWaiting(state) -> SCHOOL
        locId == SCHOOL && eveningWaiting(state) -> STOP
        else -> null
    }

    // ---- v0.15.0g: who the crowd at the stop IS ----

    /** The driver. She is AT the stop on the run by her
     *  own agenda (her morning blocks are the route
     *  itself) — she is crew, never a rider, and her
     *  HOME is the Elementary (CastHomes' hand-placed
     *  row): nobody defaults to the stop, the driver
     *  included. */
    const val DRIVER = "Crabtree"

    /** The scheduled riders at the stop RIGHT NOW: every
     *  non-adult cast member whose effective agenda
     *  block puts them at the Bus Stop at the state's
     *  clock — the kids whose schedules actually route
     *  them there, and nobody else. [crowdAt] widens
     *  this to everyone with schedule business at the
     *  stop (the driver, an adult on patrol) — together
     *  they are the crowd the renderer composes, which
     *  is what Lucky's round-2 note demanded: the crowd
     *  is the schedule, not the whole town spawned on
     *  one spot. Reads the engine's own effective block,
     *  so every claim on the agenda stack counts. */
    fun ridersAt(state: GameState): Set<String> {
        val eng = Engine()
        return state.agents.values.filter { a ->
            !a.isPlayer && !a.asleep && a.name != DRIVER &&
                CharacterSystem.bandOf(a.name) != AgeBand.ADULT &&
                eng.effectiveAgendaBlock(state, a, state.timeMin)
                    ?.location == STOP
        }.map { it.name }.toSet()
    }

    /** Everyone with schedule business at the stop right
     *  now: the riders, the driver on her route, and any
     *  adult whose own block puts them there (Barbrady's
     *  morning patrol walks past the stop) — the full
     *  set a settled town would show standing at the
     *  stop, each of them there because a schedule says
     *  so. The crowd the renderer composes is this set;
     *  the RIDERS are its non-adult subset. */
    fun crowdAt(state: GameState): Set<String> {
        val eng = Engine()
        return state.agents.values.filter { a ->
            !a.isPlayer && !a.asleep &&
                eng.effectiveAgendaBlock(state, a, state.timeMin)
                    ?.location == STOP
        }.map { it.name }.toSet()
    }

    /** Ride it. The run lands you at the far end with the
     *  derived minutes banked; counts in the town's own
     *  ledger (life["schoolBusRides"]). */
    fun ride(state: GameState, advance: Boolean = true): Walk.RideOutcome {
        val eng = Engine()
        val from = eng.player(state).location
        val dest = destFrom(state, from)
            ?: return Walk.RideOutcome(false, "nobus", 0, 0)
        val minutes = BusNetwork.rideMinutes(from, dest)
        state.walkDest = "" // the run replaces any walk plan
        eng.playerGo(state, dest)
        val ticks = Walk.applyMinutes(state, minutes, advance)
        state.life["schoolBusRides"] =
            (state.life["schoolBusRides"] ?: 0) + 1
        return Walk.RideOutcome(true, "", minutes, ticks)
    }
}

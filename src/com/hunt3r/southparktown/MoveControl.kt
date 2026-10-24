package com.hunt3r.southparktown

// ============================================================
// MoveControl — v0.16.0e, Part 2 item 2: the movement
// spec's OTHER steal (sot-movement-animation-spec.md,
// A0 + Part C): the ONE-OWNER CONTROLLER CONTRACT.
//
// Exactly one system owns an actor's movement at any
// moment. Our town has no tactical board (the spec's
// second controller is honestly not applicable — no
// combat), but it grew the same shape of problem in
// miniature: the player can be free-roaming (PaperWalk),
// riding a bus (BusRideState), or standing inside a
// street-house section (lifeText "insideSection"), and a
// cast member can be settled in a scene, mid-trip under
// the engine's grid walker (Travel's records), or asleep.
// Those states were always resolved by convention, in
// whatever order each reader happened to check them.
// This object makes the convention a CONTRACT:
//
//  - OWNERSHIP IS DERIVED, IN ONE PLACE. [ownerOfPlayer]
//    and [ownerOfAgent] are the only ownership reads;
//    they are pure functions of the save state, and the
//    suite pins their truth tables plus their mutual
//    exclusivity across the lived-day soak.
//
//  - HANDOFFS ARE EXPLICIT, AND THERE ARE THREE. A
//    cast trip starts and ends at Travel.maintain (it
//    stamps the departure from the scene the walker
//    visibly occupies and sheds the record on arrival —
//    the incoming owner starts from the actor's actual
//    position, never a remembered one). A bus ride
//    starts at BusRideState.board and ends at
//    BusRideState.clear. A section visit starts and
//    ends where GameActivity sets and clears the
//    "insideSection" key. No other code path moves an
//    actor between owners.
//
//  - INPUT LOCK: while the bus owns the player, scene
//    movement input is not the player's to give —
//    [inputLocked] is the read. (The ride's own tap —
//    the route board — is an interaction WITH the owner,
//    not movement input, and stays live.)
//
//  - ANIMATION ONLY READS. The figure renderer reads
//    movement state (velocity, owner, pose) and picks a
//    cycle; the only write-back channel is Gait's
//    footstep notifies (step sounds, snow puffs), which
//    fire EVENTS and never touch a position. Nothing in
//    the render path writes a coordinate — the suite
//    scans for it.
// ============================================================

object MoveControl {

    enum class Owner {
        /** The player, walking the scene (PaperWalk). */
        FREE_ROAM,
        /** A cast member crossing the town (Travel). */
        TRAVEL,
        /** The player aboard a bus (BusRideState). */
        BUS_RIDE,
        /** The player inside a street-house section. */
        INSIDE_SECTION,
        /** A cast member settled in its scene — standing,
         *  seated, working, asleep. The crowd pose is
         *  presentation; no system is steering them. */
        SETTLED
    }

    /** The player's movement owner, derived from the
     *  save alone. Order IS the contract: a ride outranks
     *  a section visit outranks the open street. */
    fun ownerOfPlayer(state: GameState): Owner = when {
        BusRideState.active(state) -> Owner.BUS_RIDE
        state.lifeText.containsKey("insideSection") ->
            Owner.INSIDE_SECTION
        else -> Owner.FREE_ROAM
    }

    /** A cast member's movement owner. A walking agent
     *  belongs to Travel's walker; anyone else is settled
     *  where they stand (asleep included — sleep is a
     *  pose the settled owner wears, not a controller). */
    fun ownerOfAgent(state: GameState, a: Agent): Owner = when {
        a.waypoints.isNotEmpty() -> Owner.TRAVEL
        else -> Owner.SETTLED
    }

    /** Movement input is locked while an owner other
     *  than the player's own walk holds them. */
    fun inputLocked(state: GameState): Boolean =
        ownerOfPlayer(state) == Owner.BUS_RIDE

    /** The state predicates behind ownership, exposed
     *  for the suite's exclusivity pins: at most one of
     *  these may hold for the player at any time (the
     *  resolver's order makes ownership single-valued by
     *  construction; these pins keep the UNDERLYING state
     *  honest — a ride record and a section key must
     *  never coexist in a real save). */
    fun playerClaims(state: GameState): List<Owner> {
        val out = mutableListOf<Owner>()
        if (BusRideState.active(state)) out += Owner.BUS_RIDE
        if (state.lifeText.containsKey("insideSection"))
            out += Owner.INSIDE_SECTION
        return out
    }
}

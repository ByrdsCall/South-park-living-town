package com.hunt3r.southparktown

import kotlin.math.sqrt

// ============================================================
// Travel — v0.16.0c, closing the Big Update's item-12
// PARTIAL: cast VISIBLY TRAVEL between locations.
//
// The engine has always moved agents between schedule
// blocks on the map grid (Engine.updatePositions walks
// coords along waypoints), but presence was binary: an
// agent stood in its origin scene, then — its location
// reading HUB (MainStreet) for the whole walk — it stood
// in the destination. Nobody was ever SEEN crossing the
// scenes between, and a mid-trip agent read as standing
// in Main Street wherever it actually was.
//
// Travel makes the trip a place. When the engine's
// dispatcher sends an agent somewhere (waypoints set,
// destination changed), updatePositions records the trip
// here — origin scene, destination, and the walker's
// remaining path length at departure — in the save's
// lifeText (key "trip:<name>", disclosed in the v0.16.0c
// docs/TUNING.md ledger; additive only, cleared on arrival).
// Presence is then DERIVED, purely, from that record:
//
//   route    = Walk.route(origin, destination) — the
//              street network's own BFS over EXITS, the
//              same streets the player's trips walk.
//   progress = 1 − remaining path length / departure
//              length, read live off the agent's grid
//              coords + waypoint list — the grid walker
//              IS the clock: nobody travels instantly,
//              and progress is monotone per trip.
//   pose     = the scene the walker is in NOW and where
//              across it they stand. Each route leg maps
//              half to its origin scene (crowd heart →
//              exit edge) and half to its destination
//              scene (entry edge → crowd heart), so a
//              figure leaves one scene's edge and enters
//              the next at the opposite edge, in profile,
//              facing the way it walks.
//
// The renderer (GameView.drawFigures) draws a traveler
// only in pose.sceneId, only outdoors, at the sidewalk
// band — so at every tick every awake cast member is
// somewhere real: settled in a crowd, or crossing one
// named scene on a named street. The engine's location
// field keeps its hub convention mid-trip (forty-plus
// systems read it); Travel.pose is the presence truth
// the street renders and the suite pins.
// ============================================================

object Travel {

    /** lifeText key prefix for the per-agent trip record:
     *  "trip:<name>" -> "origin|destination|startLen". */
    const val KEY_PREFIX = "trip:"

    /** The sidewalk band travelers walk: street-side of
     *  the crowd's rows, on WalkBand's own ground plane. */
    const val BAND = 0.92f

    data class Record(val origin: String, val dest: String,
                      val startLen: Double)

    data class Pose(
        /** The scene the walker visibly occupies now. */
        val sceneId: String,
        /** Where across that scene's world they stand
         *  (0..1; may breathe slightly past an edge at a
         *  leg's seam so the exit reads as an exit). */
        val xFrac: Float,
        val facingRight: Boolean,
        /** Whole-trip progress, 0..1, monotone per trip. */
        val progress: Float,
        val legFrom: String, val legTo: String,
        val route: List<String>
    )

    fun keyOf(name: String): String = KEY_PREFIX + name

    fun recordOf(state: GameState, name: String): Record? =
        parse(state.lifeText[keyOf(name)])

    private fun parse(raw: String?): Record? {
        raw ?: return null
        val parts = raw.split("|")
        if (parts.size != 3) return null
        if (parts[0] !in LOCATIONS || parts[1] !in LOCATIONS)
            return null
        val len = parts[2].toDoubleOrNull() ?: return null
        return Record(parts[0], parts[1], len)
    }

    /** The walker's remaining path length on the map
     *  grid: current coords to the head waypoint, plus
     *  every queued leg behind it. */
    fun remainingLen(a: Agent): Double {
        if (a.waypoints.isEmpty()) return 0.0
        var total = 0.0
        var (px, py) = a.coords
        for ((wx, wy) in a.waypoints) {
            val dx = (wx - px).toDouble()
            val dy = (wy - py).toDouble()
            total += sqrt(dx * dx + dy * dy)
            px = wx; py = wy
        }
        return total
    }

    /**
     * The per-tick bookkeeping, called from
     * Engine.updatePositions for every agent before it
     * moves: a settled agent sheds any trip record; a
     * walking agent with no record (or a record for a
     * different destination — a mid-trip redirect)
     * starts a fresh record from the scene it visibly
     * occupies, so progress and route restart honestly
     * from where the walker actually stands.
     */
    fun maintain(state: GameState, a: Agent) {
        val key = keyOf(a.name)
        if (a.waypoints.isEmpty()) {
            if (state.lifeText.containsKey(key))
                state.lifeText.remove(key)
            return
        }
        val rec = parse(state.lifeText[key])
        // A live record stands while the walk still fits
        // inside its stamped length. A mid-trip re-route
        // can hand the walker a LONGER path to the same
        // destination (waypoints regenerated from the hub
        // after the walker already passed it): the stamp
        // no longer describes the walk, so fall through
        // and restamp from where the walker visibly
        // stands — progress stays honest per segment.
        if (rec != null && rec.dest == a.destination &&
            remainingLen(a) <= rec.startLen) return
        val origin = when {
            a.location != HUB -> a.location
            rec != null -> poseFor(a, rec)?.sceneId ?: HUB
            else -> HUB
        }
        state.lifeText[key] = "$origin|${a.destination}|" +
            remainingLen(a).toString()
    }

    /** Whole-trip progress for [a] under [rec]:
     *  1 − remaining/departure, clamped; a trip whose
     *  recorded departure length is degenerate reads
     *  complete rather than stuck. */
    fun progressOf(a: Agent, rec: Record): Float {
        if (rec.startLen <= 0.0) return 1f
        val p = 1.0 - remainingLen(a) / rec.startLen
        return p.coerceIn(0.0, 1.0).toFloat()
    }

    /** The route [rec] walks: the street graph's own
     *  shortest path, endpoints included. */
    fun routeOf(rec: Record): List<String> =
        Walk.route(rec.origin, rec.dest)

    private fun cellDist(from: String, to: String): Double {
        val a = LOCATIONS[from] ?: return 0.0
        val b = LOCATIONS[to] ?: return 0.0
        val dx = (b.gx - a.gx).toDouble()
        val dy = (b.gy - a.gy).toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    /** Walking direction between two route neighbours:
     *  east (+1) / west (−1) by map x; a purely vertical
     *  hop reads east on a southward leg, west on a
     *  northward one — deterministic, as the route is. */
    fun directionSign(from: String, to: String): Int {
        val a = LOCATIONS[from] ?: return 1
        val b = LOCATIONS[to] ?: return 1
        if (b.gx != a.gx) return if (b.gx > a.gx) 1 else -1
        return if (b.gy >= a.gy) 1 else -1
    }

    /** The walker's visible pose, or null when the agent
     *  is not traveling (no record, no waypoints, or a
     *  route that isn't a route). Pure: the suite pins
     *  presence off this function alone. */
    fun pose(state: GameState, a: Agent): Pose? {
        if (a.waypoints.isEmpty()) return null
        val rec = recordOf(state, a.name) ?: return null
        return poseFor(a, rec)
    }

    private fun poseFor(a: Agent, rec: Record): Pose? {
        val route = routeOf(rec)
        if (route.size < 2) return null
        val p = progressOf(a, rec).toDouble()
        val weights = (0 until route.size - 1).map {
            cellDist(route[it], route[it + 1])
        }
        val total = weights.sum()
        if (total <= 0.0) return null
        var acc = 0.0
        var leg = weights.lastIndex
        var q = 1.0
        for (i in weights.indices) {
            val w = weights[i] / total
            if (p <= acc + w || i == weights.lastIndex) {
                leg = i
                q = if (w > 0.0)
                    ((p - acc) / w).coerceIn(0.0, 1.0) else 0.0
                break
            }
            acc += w
        }
        val from = route[leg]
        val to = route[leg + 1]
        val dir = directionSign(from, to)
        val exitX = if (dir > 0) 1.03f else -0.03f
        val entryX = if (dir > 0) -0.03f else 1.03f
        return if (q < 0.5) {
            val s = (q * 2).toFloat()
            Pose(from,
                lerp(FigureCompose.focalFrac(from), exitX, s),
                dir > 0, p.toFloat(), from, to, route)
        } else {
            val s = ((q - 0.5) * 2).toFloat()
            Pose(to,
                lerp(entryX, FigureCompose.focalFrac(to), s),
                dir > 0, p.toFloat(), from, to, route)
        }
    }

    private fun lerp(a: Float, b: Float, t: Float): Float =
        a + (b - a) * t
}

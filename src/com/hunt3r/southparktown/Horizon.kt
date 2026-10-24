package com.hunt3r.southparktown

import kotlin.math.atan2
import kotlin.math.sqrt

// ============================================================
// Horizon — v0.4.0a, scene parity pass #4: horizons &
// sight-lines.
//
// Passes #1–#3 made every place agree with ITSELF (its props,
// its palette, its light). This pass makes the town agree
// with ITSELF: standing anywhere, the skyline behind the
// facade is the actual neighborhood — the graph neighbors
// (EXITS) on their true map bearings, shrunk by their true
// map distance, erased by weather that would erase them.
//
// The derivation is a PURE function of (location, weather):
// no save state, no randomness, nothing stored. Geography
// does not change with the light, so the day band is not an
// input to placement — band enters at draw time through
// [colorFor] + the renderer's window rules (SceneLight's
// bands, LOC_LIGHT warmth, occupancy), exactly like the
// other layers. SceneArt.drawScene and PhotoView both render
// THIS list, so a photo can never show a skyline the scene
// wouldn't.
//
// Bearing convention (the panorama rule): the horizon is
// the map unrolled — north (up-map, low gy) at center, east
// (high gx) to the right, west to the left, due south wraps
// to the seam at the edges. xFrac comes straight off atan2,
// which buys the reciprocity the suite pins: whatever A sees
// of B, B sees of A exactly half a panorama away.
// ============================================================
object Horizon {

    // ---------------- distance tiers ----------------
    // Map units are the gx/gy grid (the whole town spans
    // ~90 x ~80). Adjacent houses sit 13 apart; the longest
    // graph edge in town (Main Street <-> Tegridy Farms)
    // runs 55.2.
    const val TIER_NEAR = 0
    const val TIER_MID = 1
    const val TIER_FAR = 2

    const val NEAR_MAX = 24f
    const val MID_MAX = 44f

    /** A landmark can be seen from farther than a neighbor's
     *  roofline — the water tower is tall. The farthest seat
     *  in town (Kenny's porch) is 80.4 units out, so every
     *  location earns the tower on a clear day. */
    const val LANDMARK_MAX = 85f

    fun tierFor(dist: Float): Int = when {
        dist <= NEAR_MAX -> TIER_NEAR
        dist <= MID_MAX -> TIER_MID
        else -> TIER_FAR
    }

    /** How far you can see in this weather, in map units.
     *  Fog ends the world at the end of the block (near tier
     *  only); a blizzard is a little kinder than fog but
     *  crueler than heavy snow. Everything else sees the
     *  whole town. (The whitening itself is SceneLight's
     *  wear — this is only the SIGHT cut.) */
    fun sightRange(weatherId: String): Float = when (weatherId) {
        "fog" -> NEAR_MAX
        "blizzard" -> 30f
        "heavy_snow" -> MID_MAX
        else -> Float.MAX_VALUE
    }

    // ---------------- landmarks ----------------
    const val RIDGE_TOKEN = "mountain-ridge"
    const val WATER_TOWER_TOKEN = "water-tower"

    /** The town water tower stands north of downtown, over
     *  the park side of the map — a fixed point like any
     *  location, seen by bearing and distance like any
     *  neighbor. It is not a location: nobody walks to it. */
    const val WATER_TOWER_GX = 54
    const val WATER_TOWER_GY = 14

    /** The town's rim: the map's edge, where the generic far
     *  mountains stop being generic and a nearer ridge closes
     *  the view. Pure rule over the coords, no registry to
     *  drift: gx <= 12 or >= 88, gy <= 14 or >= 90 — Kenny's
     *  and Tegridy in the west, the school/Tolkien/SoDoSoPa
     *  in the east, the pond north, the bus stop south. */
    fun isRim(loc: Location): Boolean =
        loc.gx <= 12 || loc.gx >= 88 || loc.gy <= 14 || loc.gy >= 90

    // ---------------- the derivation ----------------
    /**
     * One thing on a horizon. [token] is the silhouette to
     * draw (a LOC_SILHOUETTES entry for a location, or a
     * landmark token). [targetId] is the location seen, or
     * null for landmarks/backdrop. [xFrac] is its center
     * across the horizon (0..1, the panorama rule). [dist]
     * is the true map distance (0 for the ridge, which is a
     * backdrop, not a place).
     */
    data class Placement(
        val token: String,
        val targetId: String?,
        val xFrac: Float,
        val tier: Int,
        val dist: Float,
        val backdrop: Boolean = false
    )

    fun distFor(dx: Int, dy: Int): Float =
        sqrt((dx * dx + dy * dy).toDouble()).toFloat()

    /** The panorama rule: azimuth off atan2 (0 = north/center,
     *  +90 = east/right), unrolled onto 0..1. */
    fun xFracFor(dx: Int, dy: Int): Float {
        val phi = atan2(dx.toDouble(), -dy.toDouble())
        return ((phi * 180.0 / Math.PI) + 180.0).toFloat() / 360f
    }

    /**
     * What [locId]'s horizon shows in [weatherId], in PAINT
     * ORDER (ridge first, then farthest to nearest) so a
     * renderer can draw the list as-is: the ridge on rim
     * spots, the water tower where range and weather allow,
     * and exactly the graph neighbors (EXITS) inside sight —
     * nobody else. A place never sees itself.
     */
    fun placementsFor(locId: String, weatherId: String): List<Placement> {
        val from = LOCATIONS[locId] ?: return emptyList()
        val sight = sightRange(weatherId)
        val out = ArrayList<Placement>()
        if (isRim(from)) {
            out += Placement(RIDGE_TOKEN, null, 0.5f, TIER_FAR, 0f, backdrop = true)
        }
        val tdx = WATER_TOWER_GX - from.gx
        val tdy = WATER_TOWER_GY - from.gy
        val td = distFor(tdx, tdy)
        if (td <= LANDMARK_MAX && td <= sight) {
            out += Placement(WATER_TOWER_TOKEN, null, xFracFor(tdx, tdy), tierFor(td), td)
        }
        for (nextId in EXITS[locId].orEmpty()) {
            val to = LOCATIONS[nextId] ?: continue
            val token = LOC_SILHOUETTES[nextId] ?: continue
            val dx = to.gx - from.gx
            val dy = to.gy - from.gy
            val d = distFor(dx, dy)
            if (d > sight) continue
            out += Placement(token, nextId, xFracFor(dx, dy), tierFor(d), d)
        }
        // backdrop first, then far -> near: distant shapes get
        // overpainted by nearer ones, never the reverse.
        return out.sortedWith(
            compareBy<Placement> { if (it.backdrop) 0 else 1 }
                .thenBy { -it.dist })
    }

    /**
     * The full contract surface — (location, band, weather)
     * -> placements. The band does not move anything (light
     * changes colors, not sight-lines; see the header), so
     * this delegates to [placementsFor]; renderers take the
     * band to [colorFor] and their window rules.
     */
    @Suppress("UNUSED_PARAMETER") // band is contract, not input — see above
    fun sightLines(locId: String, band: String, weatherId: String): List<Placement> =
        placementsFor(locId, weatherId)

    // ---------------- band color & scale ----------------
    /** The silhouette base color of a band, BEFORE weather
     *  wear — renderers run it through SceneLight.wearHex,
     *  the same transforms as every other exterior color, so
     *  fog grays the skyline, a blizzard whites it out and
     *  rain darkens it a step. Day: pale blue-grey (nearer
     *  than the far range, lighter than a facade). Dusk:
     *  a dark cutout against the afterglow. Night: near-
     *  black — the windows do the talking. */
    fun colorFor(band: String): String = when (band) {
        "dusk" -> "#57486b"
        "night" -> "#131b30"
        else -> "#8fa9c8"
    }

    /** How tall a silhouette stands, as a fraction of the
     *  scene's height unit, by distance tier — near roofs
     *  bite into the sky, far ones hug the horizon line. */
    fun heightFrac(tier: Int): Float = when (tier) {
        TIER_NEAR -> 0.115f
        TIER_MID -> 0.082f
        else -> 0.055f
    }

    /** Tower and ridge heights run on their own fractions:
     *  the tower is the tallest thing in town from every
     *  seat; the ridge is a band, not a building. */
    const val TOWER_HEIGHT_FRAC = 0.17f
    const val RIDGE_HEIGHT_FRAC = 0.10f
}

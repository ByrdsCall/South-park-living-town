package com.hunt3r.southparktown

// ============================================================
// ResidentialStreets — v0.16.0a, Lucky's BIG UPDATE
// (item 20).
//
// Every house EXTERIOR is a street of houses: the scene's
// own facade plus the neighbor row the painter draws.
// This registry gives EVERY house standing on a house
// scene an identity:
//
//  - the named baseline houses keep their own location
//    (their door leads to their own interior, as always);
//  - the baseline's unnamed houses and the generic row
//    houses past them get authored names, their own
//    palettes, and their own interior SECTION (an
//    InteriorLayouts layout keyed to the section id) —
//    no backdrop copies sharing one generic interior.
//
// Geometry mirrors SceneArt.drawNeighborRow exactly
// (same flank spans, same slot math, same row sequence),
// so the door hit rects GameView plants sit on the
// doors the painter draws. Pure Kotlin.
// ============================================================

object ResidentialStreets {

    data class StreetHouse(
        val seqIndex: Int,          // index in the row sequence (-1 = the facade itself)
        val label: String,
        val bodyHex: String, val roofHex: String, val doorHex: String,
        val centerFrac: Float,      // world fraction of the house center
        val widthFrac: Float,       // world fraction of the house width
        val targetLocId: String,    // named house location, "" for a section
        val sectionId: String       // own interior section, "" for a named house
    )

    /** Authored identities for the baseline's unnamed
     *  houses, by street + baseline order. */
    private val BASELINE_FILLER_NAMES: Map<String, String> = mapOf(
        "rich:1" to "The Harrington House",
        "rich:3" to "The Whitfield House"
    )

    /** Continuation-house name parts for the generic row
     *  houses past the baseline's own buildings: combined
     *  deterministically per street + slot, always
     *  distinct within one street. */
    private val FAMILY_A = listOf("Miller", "Novak", "Peters", "Hale",
        "Osei", "Lindqvist", "Marsh", "Duval", "Kowalski", "Reyes")
    private val FAMILY_B = listOf("Bennett", "Ortiz", "Falk", "Nguyen",
        "Barros", "Ellery", "Voss", "Marlowe", "Petrakis", "Halloway")

    private val GEN_BODIES = listOf("#c8d8e8", "#e8d8b0", "#d8c4b8",
        "#b8d0b8", "#e0c8d8", "#c2cdd8", "#dcc7a0", "#b0c4de")
    private val GEN_ROOFS = listOf("#7a4a3a", "#5a5a66", "#6a3a3a",
        "#4a5a6a", "#7a6a4a", "#54423a")
    private val GEN_DOORS = listOf("#6a4a36", "#33512f", "#7a2e2e",
        "#2a4a8a", "#e8e2d2", "#4e3420")

    fun isHouseScene(loc: Location): Boolean = loc.kind == "house"

    /** The row geometry, mirrored from SceneArt: the
     *  neighbor count after growth, split west/east by
     *  the alternating row sequence, in flank spans
     *  [0.006, 0.40] and [0.60, 0.994]. */
    fun housesFor(loc: Location): List<StreetHouse> {
        if (!isHouseScene(loc)) return emptyList()
        val d = PaperDressing.forLocation(loc)
        val nCount = (d.neighborCount *
            SceneScale.growth(loc, false)).roundToInt()
        val seq = TownBaseline.rowSequence(loc.id, nCount)
        val out = mutableListOf<StreetHouse>()
        // the facade itself, at the street's heart
        val me = TownBaseline.of(loc.id)
        out += StreetHouse(-1, loc.name,
            me?.bodyHex ?: "#8a6a7d", me?.roofHex ?: "#54424e",
            me?.accentHex ?: "#d9c8d2",
            0.5f, 0.13f, loc.id, "")
        val west = seq.filterIndexed { i, _ -> i % 2 == 0 }
        val east = seq.filterIndexed { i, _ -> i % 2 == 1 }
        val flanks = listOf(0.006f to 0.40f, 0.60f to 0.994f)
        for (side in 0..1) {
            val buildings = if (side == 0) west else east
            val n = buildings.size
            if (n <= 0) continue
            val (fx0, fx1) = flanks[side]
            val slot = (fx1 - fx0) / n
            for (i in 0 until n) {
                val seqIdx = if (side == 0) i * 2 else i * 2 + 1
                val slotIdx = if (side == 0) (n - 1 - i) else i
                val cx = fx0 + slot * (slotIdx + 0.5f)
                val bw = slot * 0.88f
                val bld = buildings[i]
                out += identity(loc, seqIdx, bld, cx, bw)
            }
        }
        return out.sortedBy { it.centerFrac }
    }

    private fun identity(loc: Location, seqIdx: Int,
                         bld: TownBaseline.Building?,
                         cx: Float, bw: Float): StreetHouse {
        if (bld != null && bld.locId.isNotEmpty() &&
            LOCATIONS[bld.locId]?.kind == "house") {
            return StreetHouse(seqIdx, LOCATIONS.getValue(bld.locId).name,
                bld.bodyHex, bld.roofHex, bld.accentHex,
                cx, bw, bld.locId, "")
        }
        if (bld != null && bld.locId.isNotEmpty()) {
            // a non-house baseline building (the bus stop
            // shelter, a shop): not a house, no door entry
            return StreetHouse(seqIdx, bld.sign.ifEmpty { bld.locId },
                bld.bodyHex, bld.roofHex, bld.accentHex,
                cx, bw, "", "")
        }
        val street = TownBaseline.streetOf(loc.id) ?: "boys"
        val sectionId: String
        val label: String
        if (bld != null) {
            sectionId = "baseline:$street:${bld.order.toInt()}"
            label = BASELINE_FILLER_NAMES["$street:${bld.order.toInt()}"]
                ?: "The House at ${streetName(street)} ${bld.order.toInt()}"
            // the baseline's own palette is what the
            // painter draws for this house — the registry
            // answers with the same colors
            return StreetHouse(seqIdx, label,
                bld.bodyHex, bld.roofHex, bld.accentHex,
                cx, bw, "", sectionId)
        } else {
            sectionId = "row:${loc.id}:$seqIdx"
            val fam = FAMILY_A[(seqIdx * 3 + loc.id.length) % FAMILY_A.size] +
                "-" + FAMILY_B[(seqIdx * 5 + 1) % FAMILY_B.size]
            label = "The $fam House"
        }
        val gi = (seqIdx * 3 + loc.id.length)
        return StreetHouse(seqIdx, label,
            GEN_BODIES[gi % GEN_BODIES.size],
            GEN_ROOFS[(gi * 5 + 1) % GEN_ROOFS.size],
            GEN_DOORS[(gi * 7 + 2) % GEN_DOORS.size],
            cx, bw, "", sectionId)
    }

    private fun streetName(street: String): String =
        TownBaseline.STREET_NAMES[street] ?: street

    /** The identity for one row-sequence slot — the
     *  painter's hook, so generated houses wear the same
     *  colors in SceneArt that their doors answer to. */
    fun entryFor(loc: Location, seqIndex: Int): StreetHouse? =
        housesFor(loc).firstOrNull { it.seqIndex == seqIndex }

    /** The houses on [loc]'s street that are HOUSES
     *  (facade + neighbors with a door of their own). */
    fun doorHouses(loc: Location): List<StreetHouse> =
        housesFor(loc).filter {
            it.targetLocId.isNotEmpty() || it.sectionId.isNotEmpty()
        }

    /** A section's display name. */
    fun sectionName(sectionId: String): String {
        for (loc in LOCATIONS.values) {
            if (!isHouseScene(loc)) continue
            housesFor(loc).firstOrNull { it.sectionId == sectionId }
                ?.let { return it.label }
        }
        return "A House on the Street"
    }

    /** Every section id in town (coverage pins). */
    fun allSections(): List<String> =
        LOCATIONS.values.filter { isHouseScene(it) }
            .flatMap { loc -> housesFor(loc).map { it.sectionId } }
            .filter { it.isNotEmpty() }.distinct()

    private fun Float.roundToInt(): Int =
        kotlin.math.floor(this + 0.5f).toInt()
}

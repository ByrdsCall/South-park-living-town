package com.hunt3r.southparktown

// ============================================================
// TownBaseline — v0.15.0d, THE MERGED-MAP BASELINE TOWN.
//
// Lucky's references (Oct 17): the IGN map of South Park
// from The Stick of Truth and the fandom map of South
// Park from The Fractured but Whole. Both maps describe
// the same town; this table merges them into ONE
// baseline: every building that appears on either map
// appears here ONCE (where the maps overlap, the
// building is entered a single time, in its Stick of
// Truth position with its Fractured but Whole look
// notes folded into the same row).
//
// The baseline is the town's NATURAL STATE. It owns:
//  - the streets: which buildings line which street,
//    in what order (west → east by [order]);
//  - each building's LOOK: body / roof / accent colors
//    and its facade sign, drawn new in the paper idiom
//    from the reference (reference and measurement
//    only — no copied art);
//  - the neighbor rows: a scene's flanking buildings
//    are the ACTUAL adjacent buildings on its street
//    (SceneArt's neighbor row reads this table);
//  - the structures outdoor places front the street
//    with (the bus shelter, the park pavilion, the
//    ice-fishing hut, the Tegridy barn, the SoDoSoPa
//    loft, the Main Street diner).
//
// Event, season, turf and mark systems layer ONTO this
// baseline exactly as before — they dress scenes, they
// never replace a building's identity. Priced travel
// (the Walk graph) is untouched by this table: it is
// geography and looks, not routes.
//
// Street names in [STREET_NAMES] are the baseline
// table's own labels for its rows (the games name Main
// Street outright; the residential rows are labeled by
// who lives on them, as the town itself talks about
// them).
// ============================================================

object TownBaseline {

    /** One building of the baseline town. [locId] is the
     *  game location it fronts ("" for a pure filler
     *  building that exists only to line a street).
     *  [order] runs west → east along [street]. */
    data class Building(
        val locId: String,
        val street: String,
        val order: Float,
        val bodyHex: String,
        val roofHex: String,
        val accentHex: String,
        val sign: String,
        val structure: String
    )

    const val BOYS = "boys"
    const val RICH = "rich"
    const val MAIN = "main"
    const val CIVIC = "civic"
    const val SCHOOL = "school"
    const val OUTSKIRTS = "outskirts"

    /** The baseline's street labels, painted on the
     *  corner street signs. */
    val STREET_NAMES: Map<String, String> = mapOf(
        BOYS to "BOYS' STREET",
        RICH to "RICH ROW",
        MAIN to "MAIN STREET",
        CIVIC to "CIVIC ROW",
        SCHOOL to "SCHOOL ROW",
        OUTSKIRTS to "THE OUTSKIRTS"
    )

    /** The merged baseline, street by street, west →
     *  east. Every one of the town's 27 locations
     *  appears exactly once. */
    val BUILDINGS: List<Building> = listOf(
        // ---- the boys' street: the main four's houses
        // in a row, Kenny's at the tracks end, the bus
        // stop mid-row on the south side ----
        Building("KennysHouse", BOYS, 1f,
            "#8a8578", "#5d5648", "#6b4a2f", "", "house"),
        Building("ButtersHouse", BOYS, 2f,
            "#a8c8e8", "#5a6e8a", "#f0ead8", "", "house"),
        Building("CartmanHouse", BOYS, 3f,
            "#4f7d4a", "#6e4a35", "#33512f", "", "house"),
        Building("BusStop", BOYS, 3.5f,
            "#7a8a96", "#4a5a66", "#e8c53a", "BUS STOP", "shelter"),
        Building("StanHouse", BOYS, 4f,
            "#3e6b8a", "#33475e", "#7a2e2e", "", "house"),
        Building("KyleHouse", BOYS, 5f,
            "#5d7350", "#46392a", "#2f4a2a", "", "house"),
        Building("YourHouse", BOYS, 6f,
            "#8a6a7d", "#54424e", "#d9c8d2", "", "house"),
        // ---- the rich row, up the hill ----
        Building("", RICH, 1f,
            "#c8b8a8", "#5e5648", "#e8e2d2", "", "house"),
        Building("TolkienHouse", RICH, 2f,
            "#b5654a", "#5e3a2e", "#e8d8b0", "", "house"),
        Building("", RICH, 3f,
            "#9aa8b8", "#4e5a68", "#e8e2d2", "", "house"),
        // ---- Main Street, west → east ----
        Building("SkeetersBar", MAIN, 1f,
            "#6b4a2f", "#3f2c1c", "#e8c53a", "SKEETER'S BAR", "shop"),
        Building("FreemansTacos", MAIN, 2f,
            "#e8932a", "#94522c", "#7a2e2e", "FREEMAN'S TACOS", "shop"),
        Building("TweekBros", MAIN, 3f,
            "#7a5230", "#4e3420", "#d9b380", "TWEEK BROS. COFFEE", "shop"),
        Building("TomsRhino", MAIN, 4f,
            "#e8a0b0", "#a86a7c", "#ffffff", "TOM'S RHINOPLASTY", "shop"),
        Building("PostOffice", MAIN, 5f,
            "#7a8a9a", "#4e5a68", "#2a4a8a", "POST OFFICE", "shop"),
        Building("MainStreet", MAIN, 5.5f,
            "#c8b8a0", "#8a6f52", "#a83a32", "MAIN STREET DINER", "diner"),
        Building("Bank", MAIN, 6f,
            "#8a94a0", "#5d646e", "#2e4a34", "BANK OF SOUTH PARK", "shop"),
        Building("JimbosGuns", MAIN, 7f,
            "#b85c2a", "#6e3a1e", "#f0e0c0", "JIMBO'S GUNS", "shop"),
        Building("CityWok", MAIN, 8f,
            "#c0392b", "#6e1f16", "#ffffff", "CITY WOK", "shop"),
        Building("Raisins", MAIN, 9f,
            "#d96a4a", "#7c4436", "#ffffff", "RAISINS", "shop"),
        Building("Bijou", MAIN, 10f,
            "#6a4a8a", "#3c2a52", "#e8c53a", "BIJOU", "shop"),
        Building("Mall", MAIN, 11f,
            "#c8a878", "#8a6f4c", "#4a6a8a", "SOUTH PARK MALL", "mall"),
        // ---- the civic row, north of Main ----
        Building("Church", CIVIC, 1f,
            "#f0f0f0", "#6a6a72", "#8a6a3a", "SOUTH PARK CHURCH", "civic"),
        Building("CityHall", CIVIC, 2f,
            "#c8b88a", "#6f6248", "#e8e2d2", "CITY HALL", "civic"),
        // ---- the school row, east side ----
        Building("Elementary", SCHOOL, 1f,
            "#a05a3a", "#6e3b26", "#e8dcc2", "SOUTH PARK ELEMENTARY", "school"),
        Building("KidsPark", SCHOOL, 2f,
            "#7fb069", "#4e6e42", "#f0ead8", "KID'S PARK", "pavilion"),
        // ---- the outskirts: the town's rim ----
        Building("StarksPond", OUTSKIRTS, 1f,
            "#7a6a55", "#4e4234", "#5a7a9a", "STARK'S POND", "hut"),
        Building("SoDoSoPa", OUTSKIRTS, 2f,
            "#c86a9a", "#7c4460", "#f0d8e4", "SODOSOPA", "loft"),
        Building("TegridyFarms", OUTSKIRTS, 3f,
            "#9a4438", "#5e2a20", "#f0e8d8", "TEGRIDY FARMS", "barn")
    )

    private val BY_ID: Map<String, Building> =
        BUILDINGS.filter { it.locId.isNotEmpty() }
            .associateBy { it.locId }

    /** The baseline row for a location, if it has one. */
    fun of(locId: String): Building? = BY_ID[locId]

    /** The street a location stands on. */
    fun streetOf(locId: String): String? = BY_ID[locId]?.street

    /** The corner-sign label for a location's street. */
    fun streetSignOf(locId: String): String? =
        streetOf(locId)?.let { STREET_NAMES[it] }

    /** A street's buildings in west → east order. */
    fun streetLine(street: String): List<Building> =
        BUILDINGS.filter { it.street == street }.sortedBy { it.order }

    /** The ACTUAL adjacent buildings on [locId]'s
     *  street: the nearest building west and the
     *  nearest east in the baseline order (fillers
     *  included — a mansion beside Tolkien's is a real
     *  neighbor even though you can't enter it). The
     *  neighbor row paints these first, flanking the
     *  facade, before any generic row houses. */
    fun neighborsOf(locId: String): List<Building> {
        val me = BY_ID[locId] ?: return emptyList()
        val line = streetLine(me.street)
        val i = line.indexOf(me)
        if (i < 0) return emptyList()
        val out = mutableListOf<Building>()
        if (i > 0) out += line[i - 1]
        if (i < line.size - 1) out += line[i + 1]
        return out
    }

    /** The neighbor sequence the row painter walks:
     *  alternating west, east, west, east outward from
     *  the facade, so the closest buildings stand
     *  closest. [count] caps the row (PaperDressing owns
     *  the count); entries past the baseline's named
     *  buildings come back null = a generic row house in
     *  the street's own palette. */
    fun rowSequence(locId: String, count: Int): List<Building?> {
        val me = BY_ID[locId]
        if (me == null) return List(count) { null }
        val line = streetLine(me.street)
        val i = line.indexOf(me)
        val out = mutableListOf<Building?>()
        var w = 1
        var e = 1
        while (out.size < count) {
            var added = false
            if (i - w >= 0) { out += line[i - w]; w++; added = true }
            if (out.size >= count) break
            if (i + e < line.size) { out += line[i + e]; e++; added = true }
            if (!added) break
        }
        while (out.size < count) out += null
        return out
    }
}

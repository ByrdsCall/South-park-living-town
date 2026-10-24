package com.hunt3r.southparktown

// ============================================================
// Depth — v0.5.0g, the scene depth pass (Lucky's playtest
// correction: "add a background and foreground and move
// props and buildings accordingly"). Until now a scene was
// one flat stack of drawing calls whose order lived only in
// the renderers' habits. Now the depth is DATA:
//
//  - every scene is three bands — BACKGROUND (the world
//    behind the play space: sky, horizon, the buildings),
//    MIDGROUND (the play space itself: light pools, props
//    you can tap, the crowd), FOREGROUND (the near strip
//    between the crowd and the camera: fence tops, snowbank
//    lips, counter fronts — drawn OVER the figures' feet);
//  - [EXTERIOR_LAYERS] / [INTERIOR_LAYERS] are the canonical
//    layer stacks, in draw order, that SceneArt / InteriorArt
//    / GameView / PhotoView consume — the suite pins the
//    renderers' call order against them, so a layer can
//    never silently drift across a band again;
//  - [TOKEN_BANDS] gives EVERY registered dressing token in
//    the game a band (parity, season, light, silhouettes,
//    dig, faction marks, interior details, interior props,
//    and the two new foreground registries) — no token left
//    bandless, pinned by the suite;
//  - the foreground occlusion budget is geometry here, not
//    taste: the strip's top edge ([FG_TOP_OUT]/[FG_TOP_IN])
//    sits below the front row's feet and clears the worst
//    possible chin ([worstChin]) by a wide margin, so near
//    dressing can only ever overlap ground and ankles —
//    never a face, never a name tag's text at sane sizes.
//
// Pure Kotlin, like Horizon: the renderers draw, Depth
// decides what depth things live at.
// ============================================================

enum class Band { BACKGROUND, MIDGROUND, FOREGROUND }

/** v0.16.0d (spec stages 1+7) — the four-band STAGE the source
 *  pipeline authors its worlds as, made explicit over the
 *  layer stacks: GROUND (the ground plane itself), BACKGROUND
 *  (the flats: the backdrop behind the ground and the standing
 *  flats — buildings, pines, features — planted on it),
 *  MIDGROUND (the action band: light, props, the crowd),
 *  FOREGROUND (the near props between the action and the
 *  camera). [Depth.stageOfLayer] assigns every layer; the one
 *  interleave is honest staging, not disorder: the ground
 *  plane paints AFTER the backdrop it grounds (the backdrop's
 *  feet stand on it) and BEFORE every flat that stands on
 *  it — the suite pins exactly that ordering. */
enum class StageBand { GROUND, BACKGROUND, MIDGROUND, FOREGROUND }

/** The near-dressing vocabulary. Exterior kinds read as the
 *  bottom edge of the world (fence, hedge, dock…); interior
 *  kinds as the furniture between you and the room (counter
 *  fronts, pew backs, seat backs…). One painter per kind in
 *  SceneArt / InteriorArt, colored per location. */
enum class FgKind {
    FENCE, PICKETS, HEDGE, SNOWBANK, CURB, RAIL, PLANTER, DOCK,
    ROPE, STEPS, CRATES, RACK,
    COUNTER, PEWS, SHELF_EDGE, SOFA_BACK, DESK_EDGE, SEAT_BACKS,
    TABLE_EDGE
}

object Depth {

    /** One scene layer: its id, its band, and the source
     *  marker the suite greps for to pin the renderer's real
     *  call order against this list. */
    data class Layer(val id: String, val band: Band, val marker: String)

    /** The exterior stack, in draw order. The scene-side
     *  layers (sky → light) are drawn by SceneArt.drawScene;
     *  the rest by GameView around the figure pass. Buildings
     *  live in the BACKGROUND: the neighbors are "horizon",
     *  the location's own building is "facade" — one
     *  background construction, not two. */
    val EXTERIOR_LAYERS: List<Layer> = listOf(
        Layer("sky", Band.BACKGROUND, "sky bands"),
        Layer("stars", Band.BACKGROUND, "drawStars("),
        Layer("aurora", Band.BACKGROUND, "AURORA NIGHTS"),
        Layer("meteors", Band.BACKGROUND, "METEOR NIGHTS"),
        Layer("moon", Band.BACKGROUND, "MOON PHASES"),
        Layer("mountains", Band.BACKGROUND, "drawMountains("),
        Layer("horizon", Band.BACKGROUND, "drawHorizon("),
        Layer("ground", Band.BACKGROUND, "drawGround("),
        Layer("facade", Band.BACKGROUND, "drawFacade("),
        Layer("pines", Band.BACKGROUND, "drawPines("),
        Layer("feature", Band.BACKGROUND, "drawLocationFeature("),
        Layer("parity", Band.BACKGROUND, "drawParityDetails("),
        Layer("season", Band.BACKGROUND, "drawSeasonDetails("),
        Layer("dig", Band.BACKGROUND, "drawDigDetails("),
        Layer("faction-mark", Band.BACKGROUND, "drawFactionMarkDetails("),
        Layer("fair", Band.BACKGROUND, "drawFairDetails("),
        Layer("carnival", Band.BACKGROUND, "drawCarnivalDetails("),
        Layer("holiday-decor", Band.BACKGROUND, "drawHolidayDecor("),
        // v0.15.0c: the overhaul's street construction — the
        // neighbor row (a street is lined with houses, never
        // one lone facade) and the street strip itself along
        // the very bottom, both painted inside drawScene as
        // part of the background construction.
        Layer("neighbors", Band.BACKGROUND, "drawNeighborRow("),
        Layer("street", Band.BACKGROUND, "drawStreetStrip("),
        Layer("light", Band.MIDGROUND, "drawLightLayer("),
        Layer("critters", Band.MIDGROUND, "drawCritter("),
        Layer("spawns", Band.MIDGROUND, "drawSpawns("),
        Layer("trash", Band.MIDGROUND, "drawTrash("),
        Layer("living", Band.MIDGROUND, "drawLivingOutdoor("),
        Layer("living-props", Band.MIDGROUND, "drawLivingProps("),
        Layer("figures", Band.MIDGROUND, "drawFigures("),
        Layer("foreground", Band.FOREGROUND, "SceneArt.drawForegroundLayer("),
        // v0.15.0c: traffic rides the street in the
        // FOREGROUND — cars pass in front of the crowd,
        // between the sidewalk band and the camera.
        Layer("traffic", Band.FOREGROUND, "drawTraffic("),
        Layer("signs", Band.FOREGROUND, "drawSigns("),
        Layer("weather-particles", Band.FOREGROUND, "drawWeatherParticles("),
        Layer("holiday-particles", Band.FOREGROUND, "drawHolidayParticles(")
    )

    /** The interior stack, same discipline: the room itself
     *  (wall/floor/window, wall + floor dressing, the prop
     *  row, the door) is the BACKGROUND; the living room and
     *  its fixtures are the MIDGROUND play space; the near
     *  furniture strip is the FOREGROUND. */
    val INTERIOR_LAYERS: List<Layer> = listOf(
        Layer("room", Band.BACKGROUND, "wall + floor"),
        Layer("window", Band.BACKGROUND, "window: the weather + time"),
        Layer("wall-details", Band.BACKGROUND, "drawInteriorWallDetails("),
        Layer("props", Band.BACKGROUND, "drawProp("),
        Layer("floor-details", Band.BACKGROUND, "drawInteriorFloorDetails("),
        Layer("door", Band.BACKGROUND, "EXIT door (left)"),
        Layer("garland", Band.BACKGROUND, "holiday garland"),
        Layer("home-props", Band.MIDGROUND, "drawHomeLifeProps("),
        Layer("living-interior", Band.MIDGROUND, "drawLivingInterior("),
        Layer("fixtures", Band.MIDGROUND, "drawArcadeCabinet("),
        Layer("figures", Band.MIDGROUND, "drawFigures("),
        Layer("foreground", Band.FOREGROUND, "InteriorArt.drawForegroundLayer(")
    )

    fun bandOfLayer(id: String, interior: Boolean): Band? =
        (if (interior) INTERIOR_LAYERS else EXTERIOR_LAYERS)
            .firstOrNull { it.id == id }?.band

    /** v0.16.0d: the STAGE band of a layer (see [StageBand]).
     *  The exterior ground plane is the one GROUND layer;
     *  every other layer takes its stage from its depth band.
     *  Interiors carry their ground (the floor) inside the
     *  room layer, which stages as BACKGROUND. */
    fun stageOfLayer(id: String, interior: Boolean): StageBand? {
        val band = bandOfLayer(id, interior) ?: return null
        if (!interior && id == "ground") return StageBand.GROUND
        return when (band) {
            Band.BACKGROUND -> StageBand.BACKGROUND
            Band.MIDGROUND -> StageBand.MIDGROUND
            Band.FOREGROUND -> StageBand.FOREGROUND
        }
    }

    // ---------------- foreground registries ----------------

    /** Which near-dressing KIND each exterior wears. The
     *  token it answers to lives in GameData.LOC_FOREGROUND
     *  beside the other per-location registries. */
    val FOREGROUND_KINDS: Map<String, FgKind> = mapOf(
        "KennysHouse" to FgKind.FENCE,
        "ButtersHouse" to FgKind.PICKETS,
        "CartmanHouse" to FgKind.CURB,
        "StanHouse" to FgKind.HEDGE,
        "KyleHouse" to FgKind.HEDGE,
        "YourHouse" to FgKind.RAIL,
        "TolkienHouse" to FgKind.FENCE,
        "BusStop" to FgKind.SNOWBANK,
        "MainStreet" to FgKind.CURB,
        "TweekBros" to FgKind.PLANTER,
        "JimbosGuns" to FgKind.CRATES,
        "CityWok" to FgKind.STEPS,
        "SkeetersBar" to FgKind.RAIL,
        "Bijou" to FgKind.ROPE,
        "TomsRhino" to FgKind.PLANTER,
        "Bank" to FgKind.STEPS,
        "PostOffice" to FgKind.CRATES,
        "FreemansTacos" to FgKind.RAIL,
        "Church" to FgKind.PICKETS,
        "CityHall" to FgKind.STEPS,
        "Elementary" to FgKind.RACK,
        "KidsPark" to FgKind.FENCE,
        "StarksPond" to FgKind.DOCK,
        "Mall" to FgKind.PLANTER,
        "SoDoSoPa" to FgKind.RAIL,
        "TegridyFarms" to FgKind.FENCE,
        "Raisins" to FgKind.RAIL
    )

    /** Which near-furniture KIND each interior THEME wears
     *  (the token lives in CharacterSystem.INTERIOR_FOREGROUND). */
    val INTERIOR_FOREGROUND_KINDS: Map<String, FgKind> = mapOf(
        "home" to FgKind.SOFA_BACK,
        "loft" to FgKind.SOFA_BACK,
        "classroom" to FgKind.DESK_EDGE,
        "office" to FgKind.DESK_EDGE,
        "cafe" to FgKind.COUNTER,
        "restaurant" to FgKind.COUNTER,
        "diner" to FgKind.COUNTER,
        "bar" to FgKind.COUNTER,
        "clinic" to FgKind.COUNTER,
        "bank" to FgKind.COUNTER,
        "shop" to FgKind.SHELF_EDGE,
        "mall" to FgKind.SHELF_EDGE,
        "church" to FgKind.PEWS,
        "theater" to FgKind.SEAT_BACKS,
        "bus" to FgKind.SEAT_BACKS,
        "pavilion" to FgKind.RAIL,
        "hut" to FgKind.TABLE_EDGE,
        "barn" to FgKind.FENCE
    )

    fun foregroundKind(locId: String): FgKind? = FOREGROUND_KINDS[locId]

    fun interiorForegroundKind(theme: String): FgKind? =
        INTERIOR_FOREGROUND_KINDS[theme]

    // ---------------- token bands ----------------

    /** Every dressing token in the game, banded. Built once
     *  from the registries themselves, so a token added to
     *  any registry is banded automatically — and the suite
     *  fails if two registries ever claim one token (the map
     *  would silently keep only one band). */
    val TOKEN_BANDS: Map<String, Band> by lazy {
        val m = mutableMapOf<String, Band>()
        fun put(tokens: Collection<String>, band: Band) {
            for (t in tokens) m[t] = band
        }
        // the background construction: silhouettes on the
        // horizon, the parity/season/dig/mark dressing of the
        // place itself, the rooms' signature pieces and the
        // furniture row against their walls
        put(LOC_SILHOUETTES.values, Band.BACKGROUND)
        put(LOC_ART_DETAILS.values.flatten(), Band.BACKGROUND)
        put(LOC_SEASON_DETAILS.values.flatten(), Band.BACKGROUND)
        put(Mystery3.DIG_TOKENS, Band.BACKGROUND)
        put(Mystery4.FIX_TOKENS, Band.BACKGROUND)
        put(FactionMissions.MARK_TOKENS, Band.BACKGROUND)
        put(Capstones.MARK_TOKENS, Band.BACKGROUND)
        put(Fair.FAIR_TOKENS, Band.BACKGROUND)
        put(INTERIOR_ART_DETAILS.values.flatten(), Band.BACKGROUND)
        put(INTERIORS.values.flatMap { it.props }.toSet(), Band.BACKGROUND)
        // the light layer pools and glows ON the play space
        put(LOC_LIGHT_DETAILS.values.flatten(), Band.MIDGROUND)
        // the near strip
        put(LOC_FOREGROUND.values, Band.FOREGROUND)
        put(INTERIOR_FOREGROUND.values, Band.FOREGROUND)
        m
    }

    fun tokenBand(token: String): Band? = TOKEN_BANDS[token]

    // ---------------- the occlusion budget ----------------

    /** Where the front row stands (fraction of scene height),
     *  mirrored from GameView's layout (inside / outside).
     *  v0.15.0g: the outdoor front row stands at the
     *  sidewalk's street edge — WalkBand.BOTTOM_OUT — the
     *  old 0.905 mirrored a front row that stood INSIDE the
     *  walk band, and the road band below the sidewalk
     *  needs the front feet at the curb for the crossing
     *  geometry to read. */
    const val FRONT_FEET_OUT = 0.875f
    const val FRONT_FEET_IN = 0.90f

    /** The foreground strip's top edge. Below the front feet
     *  by ~0.027 of the scene — far enough to clear the name
     *  tag under a front-row figure's feet at sane sizes, near
     *  enough to read as the ground/furniture edge right in
     *  front of the crowd. v0.15.0g: outdoors this line IS
     *  the road's top edge (WalkBand.ROAD_TOP_OUT, pinned
     *  equal) — the strip below it is the road itself,
     *  asphalt with both curbs and lane markings, wearing
     *  the location's near-edge dressing, and vehicles
     *  drive on it. Indoors nothing moved. */
    const val FG_TOP_OUT = 0.885f
    const val FG_TOP_IN = 0.928f

    /** The tallest a figure can ever be drawn: the layout's
     *  base height cap times the tallest measured heightRel
     *  in the cast (the suite verifies both against KidArt). */
    const val FH_BASE_MAX = 0.205f
    const val FIG_SCALE_MAX = 1.50f

    /** The smallest head share any measured sheet carries —
     *  the chin of the tallest figure sits at
     *  feet − height × (1 − headShare); this is its floor. */
    const val HEAD_SHARE_MIN = 0.32f

    /** The lowest chin (face-zone bottom) any standing figure
     *  can present at [feetFrac]: the tallest figure with the
     *  smallest head share. The foreground strip must stay
     *  well below this — and does, by > 0.15 of the scene. */
    fun worstChin(feetFrac: Float): Float =
        feetFrac - FH_BASE_MAX * FIG_SCALE_MAX * (1f - HEAD_SHARE_MIN)
}

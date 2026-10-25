package com.hunt3r.southparktown

// ============================================================
// PaperScene — v0.15.0c, THE VISUAL OVERHAUL (Lucky's direct
// directive, Oct 17: "Do an entire visual overhaul focusing
// on the layering… make the town look like a town, not just
// a blank house per location. Completely redo the movement
// system and add a player character representation on the
// screen that interacts when you click things and physically
// moves where you click it… scene depth… everything layered
// to make up everything like a paper cut-out — people,
// houses, and world." Plus his layering correction: "In the
// South Park style. But it all needs to work correctly —
// hair, eyes and mouth should all be layered correctly.")
//
// This file is the renderer's pure contract — composition
// DATA and state machines, no android imports, so the JVM
// suite pins all of it:
//
//  - PaperPlanes: the scene's layer stack, back to front,
//    for exteriors and interiors. Depth.kt keeps owning the
//    band registry (BACKGROUND/MIDGROUND/FOREGROUND); the
//    planes refine it — every plane names its Depth band,
//    and the suite asserts the two never disagree.
//
//  - PaperDressing: the per-location dressing table. Every
//    location gets a dressed scene by DATA, not by 27 one-
//    off paintings: a zone, a row of neighbor buildings
//    across the background plane (a street is lined with
//    houses — never one lone house), standing props, the
//    street + traffic facts, downtown density where the
//    place IS downtown, and the bus-station flag (which
//    must agree with BusNetwork's station list).
//
//  - FigurePieces: a figure's piece stack in the sheets'
//    assembled-figure order (Lucky's correction, stated as
//    data): BACK → BEHIND_HAIR → LEGS → TORSO → ARMS (their
//    sheet position, at the shoulder anchors) → HEAD base →
//    EYES → MOUTH → FACE_EXTRA → HAIR → HAT. Derived from
//    the spec's own HeadStack layers, so it cannot drift
//    from what KidArt paints; the suite additionally proves
//    from the stack geometry that hair and hat shapes never
//    cover the eye or mouth anchors of any sheet.
//
//  - PaperWalk: the tap-to-move state machine. The player
//    stands IN the scene; a tap resolves to a walk target
//    plus a pending interaction, the kid physically walks
//    there (facing flips with direction, phase advances
//    with distance), and on arrival the pending interaction
//    fires through GameView's existing callbacks. Intra-
//    scene walking is animation — zero game-minutes; the
//    Walk economy still prices every inter-location leg.
//
//  - PaperTap: tap resolution (figures by nearest center,
//    everything else topmost — the crowd rule the old view
//    already lived by) and the y-sort every figure/prop in
//    the ground plane obeys: lower baseline = nearer the
//    camera = painted later.
// ============================================================

/** One plane of a paper scene, back to front. [band] is the
 *  Depth band the plane lives in; [parallax] is the camera
 *  factor the plane's loose construction rides at — the
 *  far range and horizon at FAR's 0.55, the neighbor row
 *  at BUILDINGS' 0.85, the ground and everything standing
 *  on it locked to the world at 1.0. SceneArt applies the
 *  factors; GameView hands it the camera. */
enum class PaperPlane(val band: Band, val parallax: Float) {
    SKY(Band.BACKGROUND, 0f),
    FAR(Band.BACKGROUND, 0.55f),
    BUILDINGS(Band.BACKGROUND, 0.85f),
    GROUND(Band.BACKGROUND, 1f),
    PROPS(Band.MIDGROUND, 1f),
    FIGURES(Band.MIDGROUND, 1f),
    TRAFFIC(Band.FOREGROUND, 1f),
    FOREGROUND(Band.FOREGROUND, 1f)
}

/** Interior planes, back to front: the wall plane, the
 *  floor plane, the furniture row against the wall, the
 *  y-sorted figures (the player among them), and the near
 *  furniture fronts of Depth's foreground strip. */
enum class PaperRoomPlane(val band: Band) {
    WALL(Band.BACKGROUND),
    FLOOR(Band.BACKGROUND),
    FURNITURE_BACK(Band.BACKGROUND),
    FIGURES(Band.MIDGROUND),
    FURNITURE_FRONT(Band.FOREGROUND)
}

object PaperScene {
    /** The exterior stack in draw order. */
    val EXTERIOR: List<PaperPlane> = listOf(
        PaperPlane.SKY, PaperPlane.FAR, PaperPlane.BUILDINGS,
        PaperPlane.GROUND, PaperPlane.PROPS, PaperPlane.FIGURES,
        PaperPlane.TRAFFIC, PaperPlane.FOREGROUND)

    /** The interior stack in draw order. */
    val INTERIOR: List<PaperRoomPlane> = listOf(
        PaperRoomPlane.WALL, PaperRoomPlane.FLOOR,
        PaperRoomPlane.FURNITURE_BACK, PaperRoomPlane.FIGURES,
        PaperRoomPlane.FURNITURE_FRONT)

    /** Y-sort for everything standing in the ground plane:
     *  ascending baseline paints back-to-front, so a figure
     *  lower on screen (nearer the street, nearer the camera)
     *  overlaps one higher up. Stable for ties. */
    fun <T> depthSorted(items: List<T>, baseline: (T) -> Float): List<T> =
        items.sortedBy(baseline)
}

// ---------------- scene scale (v0.15.0g) ----------------

/** How wide a scene's world is, in screen widths —
 *  v0.15.0g, THE SCALE & SPACE BUILD (Lucky's playtest
 *  round 2, Oct 18: "areas 2–3× bigger"). Through
 *  v0.15.0f every scene's world was OLD_FACTOR screens
 *  wide (GameView's old WORLD_SCALE constant, retired
 *  here); now the world GROWS by scene class while the
 *  figures keep their v0.15.0d natural sizes — interiors
 *  double, street scenes grow two and a half times, and
 *  the open places (pond, farm, park land) triple. The
 *  camera and the parallax machinery were always generic
 *  over the world width; they simply run over a wider
 *  world now. Composition that was fraction-tuned
 *  against the old width (facade bodies, structure
 *  bodies, the dash rhythm) anchors to [unitW] — the
 *  pre-widening world width — so a building keeps its
 *  composed size at the scene's heart and the world
 *  grows AROUND it, with the neighbor row and the
 *  standing-prop spots tiled to fill the new width
 *  honestly instead of stretching thin. */
object SceneScale {
    /** The world-width factor every scene was composed
     *  at through v0.15.0f — and the width the fraction
     *  tables still speak. */
    const val OLD_FACTOR = 1.7f

    // v0.16.0a (item 4, supersedes the v0.15.0g ledger pin):
    // interiors grow to 2.4× — the multi-room layouts of
    // InteriorLayouts need the extra width so every room
    // keeps a full screen of its own.
    const val GROW_INTERIOR = 2.4f
    const val GROW_STREET = 2.5f
    const val GROW_OPEN = 3.0f

    /** This scene's growth over the composed width:
     *  interiors 2×, street scenes 2.5×, open land 3× —
     *  every value inside Lucky's "two to three times". */
    fun growth(loc: Location, inside: Boolean): Float = when {
        inside -> GROW_INTERIOR
        PaperDressing.forLocation(loc).hasStreet -> GROW_STREET
        else -> GROW_OPEN
    }

    /** The world width factor (world px per screen px)
     *  for [loc]: worldW = screenW × factor. */
    fun factor(loc: Location, inside: Boolean): Float =
        OLD_FACTOR * growth(loc, inside)

    /** The composition width a widened [worldW] was
     *  built from: painters anchor fraction-tuned widths
     *  (facades, structures, dash rhythm, stripe widths)
     *  to this, and positions to the world itself. */
    fun unitW(worldW: Float, loc: Location, inside: Boolean): Float =
        worldW / growth(loc, inside)
}

// ---------------- the dressing table ----------------

/** The dressing zones: what KIND of street a place is. */
enum class PaperZone {
    RESIDENTIAL, DOWNTOWN, COMMERCIAL, CIVIC, SCHOOL,
    NATURE, FARM
}

/** One location's dressing, as data: the neighbor row
 *  across its background plane, the standing props in its
 *  ground plane, and its street facts. [neighborCount]
 *  counts the buildings flanking the location's own facade
 *  — together with the facade itself and the far rooftop
 *  row, a street scene is never one lone house. */
data class PaperDressing(
    val zone: PaperZone,
    val neighborCount: Int,
    val neighborTall: Boolean,
    val props: List<String>,
    val hasStreet: Boolean,
    val traffic: Boolean,
    val downtown: Boolean,
    val busStation: Boolean
) {
    companion object {
    // the standing-prop vocabulary (drawn as cut-paper
    // shapes by GameView/SceneArt; solids only)
    const val TREE = "tree"
    const val PINE = "pine"
    const val LAMP = "lamp"
    const val FENCE = "fence"
    const val HYDRANT = "hydrant"
    const val MAILBOX = "mailbox"
    const val BUSH = "bush"
    const val SIGN = "sign"
    const val BENCH = "bench"
    const val AWNING = "awning"
    const val STOREFRONT = "storefront"
    const val PLANTER = "planter"
    const val FLAG = "flag"
    const val BIKE_RACK = "bike_rack"
    const val SILO = "silo"
    const val DOCK = "dock"

    /** Rooftops on the far plane for any street scene —
     *  the town continues past the block you're standing
     *  on. Nature scenes keep a tree line instead. */
    const val FAR_ROW_MIN = 3

    fun forLocation(loc: Location): PaperDressing {
        val station = loc.id in BusNetwork.STATIONS
        return when {
            loc.id == "MainStreet" -> PaperDressing(
                PaperZone.DOWNTOWN, neighborCount = 5, neighborTall = true,
                props = listOf(STOREFRONT, AWNING, LAMP, SIGN, PLANTER, BENCH),
                hasStreet = true, traffic = true, downtown = true,
                busStation = station)
            loc.id == "BusStop" -> PaperDressing(
                PaperZone.RESIDENTIAL, neighborCount = 3, neighborTall = false,
                props = listOf(SIGN, BENCH, LAMP, MAILBOX, FENCE),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
            loc.id == "Elementary" -> PaperDressing(
                PaperZone.SCHOOL, neighborCount = 2, neighborTall = false,
                props = listOf(FLAG, BIKE_RACK, SIGN, LAMP, BUSH),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
            loc.id == "StarksPond" -> PaperDressing(
                PaperZone.NATURE, neighborCount = 0, neighborTall = false,
                props = listOf(PINE, DOCK, BENCH, SIGN),
                hasStreet = false, traffic = false, downtown = false,
                busStation = station)
            loc.id == "KidsPark" -> PaperDressing(
                PaperZone.NATURE, neighborCount = 1, neighborTall = false,
                props = listOf(TREE, BENCH, LAMP, FENCE),
                hasStreet = true, traffic = false, downtown = false,
                busStation = station)
            loc.id == "TegridyFarms" -> PaperDressing(
                PaperZone.FARM, neighborCount = 1, neighborTall = false,
                props = listOf(SILO, FENCE, TREE, MAILBOX),
                hasStreet = false, traffic = false, downtown = false,
                busStation = station)
            loc.kind == "house" -> PaperDressing(
                PaperZone.RESIDENTIAL, neighborCount = 4, neighborTall = false,
                props = listOf(TREE, FENCE, MAILBOX, BUSH, LAMP),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
            loc.kind == "shop" -> PaperDressing(
                PaperZone.COMMERCIAL, neighborCount = 3, neighborTall = false,
                props = listOf(STOREFRONT, AWNING, SIGN, LAMP, PLANTER),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
            loc.kind == "civic" -> PaperDressing(
                PaperZone.CIVIC, neighborCount = 3, neighborTall = false,
                props = listOf(FLAG, LAMP, SIGN, PLANTER, BENCH),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
            loc.kind == "school" -> PaperDressing(
                PaperZone.SCHOOL, neighborCount = 2, neighborTall = false,
                props = listOf(FLAG, BIKE_RACK, SIGN, LAMP),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
            loc.kind == "outdoor" -> PaperDressing(
                PaperZone.NATURE, neighborCount = 1, neighborTall = false,
                props = listOf(TREE, BENCH, SIGN, LAMP),
                hasStreet = true, traffic = false, downtown = false,
                busStation = station)
            else -> PaperDressing( // hub and anything unlisted
                PaperZone.COMMERCIAL, neighborCount = 3, neighborTall = false,
                props = listOf(STOREFRONT, SIGN, LAMP, PLANTER, BENCH),
                hasStreet = true, traffic = true, downtown = false,
                busStation = station)
        }
    }

    fun forId(locId: String): PaperDressing? =
        LOCATIONS[locId]?.let { forLocation(it) }

    /** The dressing census: every paper piece a scene is
     *  built from — the facade itself, the neighbor row,
     *  the far plane's minimum stand (rooftops across a
     *  street scene; the tree line / field rows where
     *  there is no street), street + curb where a street
     *  runs, and the standing props. The suite holds
     *  every location to a minimum: no blank scenes. */
    fun census(loc: Location): Int {
        val d = forLocation(loc)
        return 1 + d.neighborCount + d.props.size + FAR_ROW_MIN +
            (if (d.hasStreet) 2 else 0) // street + curb
    }

        /** The minimum census any location may carry. */
        const val MIN_CENSUS = 8

    // ---- v0.15.0d: standing-prop spots + footprints ----
    // The dressing table's standing props get ONE set of
    // spots per location, read by both the painter
    // (GameView's standing-prop pass) and the walk clamp
    // (a prop's footprint blocks the ground plane: you
    // walk up beside a tree, never into its trunk).
    // Deterministic from the location id — no seed state.

    /** Props that stand in the ground plane (facade
     *  dressing — awnings, storefronts — is painted with
     *  the buildings, not stood on the ground). */
    fun standingProps(loc: Location): List<String> =
        forLocation(loc).props.filter {
            it != AWNING && it != STOREFRONT && it != SIGN
        }

    /** Where each standing prop stands: (kind, x across
     *  the world, band down the ground plane). Spread
     *  by a golden-ratio walk over the location's hash,
     *  kept out of the facade's center and the street.
     *  v0.15.0g: in the widened world the spot set TILES
     *  — one full set per growth segment, each segment
     *  carrying the same dressing at the same local
     *  fractions — so density per screen holds across
     *  the wider street instead of stretching thin. The
     *  middle segment of an odd count is the composed
     *  scene itself: its center is the facade's center,
     *  and the facade exclusion applies there unchanged. */
    fun propSpots(loc: Location): List<Triple<String, Float, Float>> {
        val kinds = standingProps(loc)
        val hash = (loc.id.hashCode() and 0x7fffffff).toFloat()
        val base = kinds.mapIndexed { i, kind ->
            var fx = 0.06f + ((hash % 97f) / 97f +
                i * 0.618f) % 1f * 0.88f
            if (fx in 0.40f..0.60f) fx = if (fx < 0.5f) 0.36f else 0.64f
            val band = 0.06f + ((hash % 53f) / 53f + i * 0.37f) % 1f * 0.36f
            Triple(kind, fx, band)
        }
        val segs = kotlin.math.ceil(
            SceneScale.growth(loc, false).toDouble()).toInt()
        if (segs <= 1) return base
        return (0 until segs).flatMap { s ->
            base.map { (k, fx, b) -> Triple(k, (s + fx) / segs, b) }
        }
    }

    /** The footprint a standing prop blocks, by kind:
     *  (radius across the world, radius down the band). */
    fun footprintOf(kind: String): Pair<Float, Float> = when (kind) {
        TREE, PINE -> 0.028f to 0.13f
        SILO -> 0.045f to 0.16f
        BENCH -> 0.048f to 0.11f
        LAMP, FLAG -> 0.011f to 0.07f
        FENCE -> 0.055f to 0.08f
        BIKE_RACK, DOCK -> 0.040f to 0.12f
        else -> 0.020f to 0.09f // mailbox, bush, hydrant, planter
    }

    /** Every footprint standing in [loc]'s ground
     *  plane, plus the bus-stop sign's post at station
     *  scenes (the sign GameView plants at SIGN_FX).
     *  v0.15.0g: radii divide by the scene's growth —
     *  a footprint is a physical size, and the wider
     *  world's fractions are smaller change. */
    fun footprintsFor(loc: Location): List<WalkBand.Footprint> {
        val g = SceneScale.growth(loc, false)
        val out = propSpots(loc).map { (kind, fx, band) ->
            val (rx, rb) = footprintOf(kind)
            WalkBand.Footprint(fx, band, rx / g, rb)
        }.toMutableList()
        if (forLocation(loc).busStation) {
            out += WalkBand.Footprint(SIGN_FX,
                WalkBand.bandForY(0.715f, 1f, false), 0.014f / g, 0.08f)
        }
        return out
    }

    // ---- v0.15.0g: the station sign + the crosswalks ----

    /** Where the bus-stop sign stands, as a world
     *  fraction: at the station cluster's west edge,
     *  clear of the standing bus's body (the bus centers
     *  at GameView's BUS_FX) — GameView paints the sign
     *  here, its hit rect lives here, and the sign's
     *  footprint above reads the same number. In the
     *  narrow world it stood at 0.16; the widened world
     *  moved the cluster to the scene's heart. */
    const val SIGN_FX = 0.34f

    /** The marked crosswalks of a street scene, as
     *  world-fraction x spans (start..end): one at each
     *  end of the street, one at the scene's heart —
     *  every facade's door faces its own crossing, and
     *  the station and the school front sit inside the
     *  heart span — plus two more mid-block crossings
     *  downtown, where a downtown earns them. A scene
     *  with no street has none (there is no road to
     *  cross); nature's paths are all walkable ground.
     *  SceneArt zebra-stripes exactly these spans on the
     *  road, and GameView's tap layer lets feet onto the
     *  road inside exactly these spans — markings and
     *  movement are the same table, so they can never
     *  disagree. */
    fun crosswalks(loc: Location): List<Pair<Float, Float>> {
        val d = forLocation(loc)
        if (!d.hasStreet) return emptyList()
        val spans = mutableListOf(
            0.015f to 0.075f,
            0.465f to 0.535f,
            0.925f to 0.985f)
        if (d.downtown) {
            spans += 0.245f to 0.295f
            spans += 0.695f to 0.745f
        }
        return spans.sortedBy { it.first }
    }
    }
}

// ---------------- figure pieces ----------------

/** A figure's pieces in the sheets' assembled order.
 *  Lucky's layering rule, as data: the body plane first
 *  (back items, the behind-hair mass, legs, torso, arms at
 *  their sheet anchors), then the head stack — head base,
 *  eyes, mouth ON the face, face extras, hair framing the
 *  face from above the head base, hat above the hair. */
enum class FigurePiece {
    BACK, BEHIND_HAIR, LEGS, TORSO, ARMS,
    HEAD, EYES, MOUTH, FACE_EXTRA, HAIR, HAT
}

object FigurePieces {
    /** The canonical stack for [spec], derived from the
     *  spec itself: its back items, and the InkKinds its
     *  own HeadStack actually carries — so the composition
     *  can never claim a piece the painter doesn't paint,
     *  or miss one it does. */
    fun stackFor(spec: SheetSpec): List<FigurePiece> {
        val kinds = HeadStack.layers(spec).map { it.kind }.toSet()
        val out = mutableListOf<FigurePiece>()
        if (spec.backStyle != "none" || spec.backpack) out += FigurePiece.BACK
        if (InkKind.BEHIND_HAIR in kinds) out += FigurePiece.BEHIND_HAIR
        out += FigurePiece.LEGS
        out += FigurePiece.TORSO
        out += FigurePiece.ARMS
        out += FigurePiece.HEAD
        out += FigurePiece.EYES
        // Kenny's parka has no mouth, ever (his sheet's rule)
        if (spec.special != "kenny_parka") out += FigurePiece.MOUTH
        if (spec.beard || spec.mustache || spec.glasses || spec.sunglasses ||
            spec.eyewear.isNotEmpty() || spec.browHex != null ||
            spec.freckles || spec.wrinkles)
            out += FigurePiece.FACE_EXTRA
        if (InkKind.HAIR in kinds) out += FigurePiece.HAIR
        if (InkKind.HAT in kinds) out += FigurePiece.HAT
        return out
    }

    // ---- v0.16.0a: the SIDE-PROFILE stack (items 14+15) ----
    // The same layer discipline turned sideways: back,
    // legs, torso, arms, head, the one eye, the mouth,
    // face extras, hair, hat — in draw order, facing the
    // way the figure moves. KidArt's profile painter
    // draws exactly this order, and the expression it
    // wears comes from Expressions' profile fields, so a
    // face reads the same from the front and the side.

    val PROFILE_STACK: List<FigurePiece> = listOf(
        FigurePiece.BACK, FigurePiece.LEGS, FigurePiece.TORSO,
        FigurePiece.ARMS, FigurePiece.HEAD, FigurePiece.EYES,
        FigurePiece.MOUTH, FigurePiece.FACE_EXTRA,
        FigurePiece.HAIR, FigurePiece.HAT
    )

    /** Which pieces of [PROFILE_STACK] a spec actually
     *  draws in profile: legs/torso/arms/head/eye always;
     *  mouth except Kenny's parka; face extras, hair and
     *  hat per the sheet's own flags and ink. */
    fun profileStackFor(spec: SheetSpec): List<FigurePiece> {
        val kinds = HeadStack.layers(spec).map { it.kind }.toSet()
        val out = mutableListOf<FigurePiece>()
        if (spec.backStyle != "none" || spec.backpack)
            out += FigurePiece.BACK
        out += FigurePiece.LEGS
        out += FigurePiece.TORSO
        out += FigurePiece.ARMS
        out += FigurePiece.HEAD
        out += FigurePiece.EYES
        if (spec.special != "kenny_parka") out += FigurePiece.MOUTH
        if (spec.beard || spec.mustache || spec.glasses || spec.sunglasses ||
            spec.eyewear.isNotEmpty() || spec.browHex != null ||
            spec.freckles || spec.wrinkles || spec.ears)
            out += FigurePiece.FACE_EXTRA
        if (InkKind.HAIR in kinds) out += FigurePiece.HAIR
        if (InkKind.HAT in kinds) out += FigurePiece.HAT
        return out
    }

    /** The face-feature zone of [spec], in unit space: the
     *  eye band and the mouth point, read off the derived
     *  geometry KidArt itself draws from. Hair and hat
     *  shapes must never cover these anchors — the suite
     *  samples them against every sheet's HeadStack. */
    fun eyeAnchors(spec: SheetSpec): List<Pair<Float, Float>> {
        val g = FigureGeometry.derive(spec)
        val cy = (g.eyeT + g.eyeB) / 2f
        return listOf(
            (g.eyeL + g.eyeR) / 2f to cy,          // between the eyes
            g.eyeL + (g.eyeR - g.eyeL) * 0.25f to cy,
            g.eyeL + (g.eyeR - g.eyeL) * 0.75f to cy)
    }

    fun mouthAnchor(spec: SheetSpec): Pair<Float, Float> {
        val g = FigureGeometry.derive(spec)
        return g.hx to g.mouthY
    }

    /** True when no HAIR or HAT layer of [spec]'s stack
     *  covers any face-feature anchor — hair frames the
     *  face; it never paints over the eyes or the mouth. */
    fun faceClear(spec: SheetSpec): Boolean {
        val layers = HeadStack.layers(spec)
            .filter { it.kind == InkKind.HAIR || it.kind == InkKind.HAT }
        if (layers.isEmpty()) return true
        val pts = eyeAnchors(spec) +
            (if (spec.special != "kenny_parka")
                listOf(mouthAnchor(spec)) else emptyList())
        return pts.none { (x, y) -> layers.any { it.shape.contains(x, y) } }
    }
}

// ---------------- the player walk ----------------

object PaperWalk {
    /** World widths per second — a paper scuttle: the kid
     *  crosses the whole scene in about three seconds. */
    const val SPEED = 0.34f

    /** Band travel weighs less than x travel (the ground
     *  plane is shallow): stepping toward/away reads
     *  slower than striding sideways. */
    const val BAND_WEIGHT = 0.45f

    /** Arrival radius, in weighted world units. */
    const val ARRIVE_EPS = 0.008f

    /** An interaction waiting at the end of a walk. Kinds
     *  are GameView's own hit kinds (kid / visitor / exit /
     *  spawn / door / exitdoor / trash / litter / the scene
     *  props / busstation / schoolbus); "ground" never
     *  pends — open ground just walks. */
    data class Pending(val kind: String, val id: String)

    /** Where the player stands: [x] across the world
     *  (0..1), [band] down the ground plane (0 = the back,
     *  by the buildings; 1 = the street edge). Transient
     *  scene state — never saved. */
    data class State(
        val x: Float = 0.5f, val band: Float = 0.6f,
        val tx: Float = 0.5f, val tband: Float = 0.6f,
        val walking: Boolean = false, val facing: Int = 1,
        val phase: Float = 0f, val pending: Pending? = null
    )

    /** Tap open ground: walk there, nothing pends.
     *  v0.15.0g: the band clamp widens to the road's far
     *  edge (WalkBand.BAND_MAX_OUT) — a crossing target
     *  is a legal order here; the CROSSWALK gate itself
     *  lives in GameView's tap layer, which clamps a
     *  road target back to the sidewalk before ordering
     *  whenever the tap's x is outside every crossing. */
    fun tapGround(s: State, x: Float, band: Float): State =
        s.copy(tx = x.coerceIn(0.02f, 0.98f),
            tband = band.coerceIn(0f, WalkBand.BAND_MAX_OUT),
            walking = true, pending = null,
            facing = if (x < s.x) -1 else if (x > s.x) 1 else s.facing)

    /** Tap a thing: walk to its standing spot (just off
     *  its near side) and pend its interaction. */
    fun tapTarget(s: State, kind: String, id: String,
                  tx: Float, tband: Float): State =
        s.copy(tx = tx.coerceIn(0.02f, 0.98f),
            tband = tband.coerceIn(0f, WalkBand.BAND_MAX_OUT),
            walking = true, pending = Pending(kind, id),
            facing = if (tx < s.x) -1 else if (tx > s.x) 1 else s.facing)

    /** Weighted distance to the current target. */
    fun distTo(s: State, x: Float, band: Float): Float {
        val dx = x - s.x
        val db = (band - s.band) * BAND_WEIGHT
        return kotlin.math.sqrt(dx * dx + db * db)
    }

    /** Advance the walk by [dtSec]. Returns the new state
     *  and the pending interaction to fire when this step
     *  arrives (null while still walking, and null on
     *  arrival at open ground). Already there = arrives
     *  on the first step, so a tap on the thing you're
     *  standing at still fires its interaction.
     *
     *  v0.16.0e (spec B3): NO ROOT MOTION — the position
     *  above is the controller's alone; the walk phase is
     *  a pure READ of the distance just travelled, scaled
     *  by [phasePerFrac] — the player's own leg geometry
     *  at his drawn size, from Gait (GameView computes
     *  it per scene). The default keeps the town's
     *  historical 22 rad per world width for callers
     *  without a figure at hand (the suite's state-
     *  machine pins); Gait's derivation lands within a
     *  few percent of it at reference geometry, by
     *  calibration. When the walk stops, the phase stops
     *  — idle rate is zero. */
    fun step(s: State, dtSec: Float,
             phasePerFrac: Float = 22f): Pair<State, Pending?> {
        if (!s.walking) return s to null
        val d = distTo(s, s.tx, s.tband)
        if (d <= ARRIVE_EPS) {
            return s.copy(x = s.tx, band = s.tband, walking = false,
                pending = null) to s.pending
        }
        val move = (SPEED * dtSec).coerceAtMost(d)
        val f = move / d
        val nx = s.x + (s.tx - s.x) * f
        val nb = s.band + (s.tband - s.band) * f
        val facing = when {
            s.tx - s.x > 0.001f -> 1
            s.tx - s.x < -0.001f -> -1
            else -> s.facing
        }
        return s.copy(x = nx, band = nb, facing = facing,
            phase = s.phase + move * phasePerFrac) to null
    }

    /** v0.17.0b: [step] with FEET CONTACT. The town's
     *  one collision law, learned from Lucky's
     *  playtest ("collision with characters causes
     *  them to move but it's hitbox is bad"): a cast
     *  member is NEVER displaced by the player — not
     *  by the walk, not by the draw. [blockers] are the
     *  scene's composed feet footprints (FigureCompose
     *  .Feet, screen px) and they are READ-ONLY here:
     *  the only position this function may move is the
     *  player's own. When the step would carry the
     *  player into a figure's feet ellipse
     *  (FigureCompose.feetTouch), the player stops ON
     *  the ellipse's edge: if the figure is the walk's
     *  own pending target, that IS the arrival (you
     *  walked up to them — the interaction fires); if
     *  the step still makes band progress the player
     *  slides along the edge as his depth pulls him
     *  clear; if it makes no progress at all, the walk
     *  ends, pressed to a stop. A step that STARTS
     *  inside an ellipse (the crowd composed around a
     *  standing player) is never clamped — he may
     *  always walk out of a spawn overlap. */
    fun stepClamped(s: State, dtSec: Float,
                    phasePerFrac: Float = 22f,
                    blockers: List<FigureCompose.Feet>,
                    worldW: Float, h: Float,
                    inside: Boolean, fhBase: Float):
        Pair<State, Pending?> {
        val (ns, fired) = step(s, dtSec, phasePerFrac)
        if (blockers.isEmpty() || worldW <= 0f || h <= 0f)
            return ns to fired
        val pFeetY = WalkBand.feetY(ns.band, h, inside)
        val pFh = fhBase * WalkBand.depthScale(ns.band)
        val px = ns.x * worldW
        val sFeetY = WalkBand.feetY(s.band, h, inside)
        val sFh = fhBase * WalkBand.depthScale(s.band)
        val sPx = s.x * worldW
        for (b in blockers) {
            if (!FigureCompose.feetTouch(px, pFeetY, pFh, b))
                continue
            // already DEEP inside this one before the step:
            // a spawn overlap, not a collision — walk out.
            // (Deep = dist2 < 0.90: a step that merely rests
            // ON the edge, dist2 ≈ 1, must still clamp, or
            // the resting player walks through next step —
            // the float-exact edge is not "inside".)
            if (FigureCompose.feetDist2(sPx, sFeetY, sFh, b)
                < 0.90f)
                continue
            val edge = FigureCompose.feetEdgeX(px, pFeetY,
                pFh, b) / worldW
            if (fired != null) {
                // arrived this step (the target snapped
                // into the ellipse): stand at the edge,
                // the interaction still fires
                return ns.copy(x = edge) to fired
            }
            if (s.pending != null && s.pending.id == b.name) {
                // the figure we came for IS the contact:
                // standing at their feet is the arrival
                return s.copy(x = edge, band = ns.band,
                    facing = ns.facing, phase = ns.phase,
                    walking = false, pending = null) to s.pending
            }
            val noProgress =
                kotlin.math.abs(edge - s.x) < 0.0005f &&
                    kotlin.math.abs(ns.band - s.band) < 0.0005f
            return if (noProgress) {
                // pressed to a full stop against a figure
                // who isn't the target: the walk is over
                s.copy(x = edge, walking = false,
                    pending = null) to null
            } else {
                // stopped at the edge for now; the walk
                // (and its order) survives — band progress
                // slides the player along and clear
                ns.copy(x = edge) to null
            }
        }
        return ns to fired
    }
}

// ---------------- tap resolution ----------------

object PaperTap {
    /** One tappable thing in the scene, in world coords:
     *  its center, its size, and the ground-plane baseline
     *  it stands on. */
    data class Item(
        val kind: String, val id: String,
        val cx: Float, val cy: Float,
        val w: Float, val h: Float,
        val baseline: Float
    )

    fun contains(it: Item, x: Float, y: Float): Boolean =
        x >= it.cx - it.w / 2f && x <= it.cx + it.w / 2f &&
            y >= it.cy - it.h / 2f && y <= it.cy + it.h / 2f

    /** Resolve a tap the way the town reads on screen:
     *  VEHICLES first — a bus stands on the TRAFFIC
     *  plane, in front of the crowd, and it paints over
     *  the figures at the curb; a tap anywhere on its
     *  body is a tap on the bus, never on a kid who
     *  happens to be standing in front of it (v0.15.0d:
     *  this is the boarding fix — in v0.15.0c a figure
     *  at the stop ate every tap on the bus, so the
     *  school run felt broken). Among FIGURES containing
     *  the point, the nearest center wins (clustered
     *  crowds overlap by design); among everything
     *  else, the last registered — the topmost painted —
     *  wins. Null = open ground. */
    fun resolve(items: List<Item>, x: Float, y: Float): Item? {
        items.lastOrNull {
            (it.kind == "schoolbus" || it.kind == "busstation") &&
                contains(it, x, y)
        }?.let { return it }
        var bestFig: Item? = null
        var bestDist = Float.MAX_VALUE
        for (it in items) {
            if (it.kind != "kid" && it.kind != "visitor") continue
            if (!contains(it, x, y)) continue
            val dx = it.cx - x
            val dy = it.cy - y
            val d = dx * dx + dy * dy
            if (d < bestDist ||
                (d == bestDist && bestFig != null && it.id < bestFig!!.id)) {
                bestDist = d; bestFig = it
            }
        }
        if (bestFig != null) return bestFig
        return items.lastOrNull { contains(it, x, y) }
    }
}

// ---------------- ambient traffic (v0.15.0g) ----------------

/** The ambient traffic derivation — v0.15.0g, Lucky's
 *  "make less cars go through" (less = fewer, not zero:
 *  a quiet street, a car now and then). The rate is a
 *  pure function of the view tick, shared by GameView's
 *  traffic painter and the suite: one pass slot per lane
 *  every PERIOD_TICKS, a pass taking PASS_TICKS to cross
 *  (the crossing TIME is unchanged — cars drive at the
 *  same speed they always did; they just come rarely).
 *
 *  The old derivation ran a 340-tick period with a
 *  210-tick pass: an expected 2 × 1200/340 ≈ 7.1 passes
 *  per scene-minute (at the view's 50 ms tick), and the
 *  street was busy more than half the time. The new
 *  1140-tick period expects 2 × 1200/1140 ≈ 2.1 passes
 *  per scene-minute — 0.30× the old rate, under the
 *  "at most a third" the correction set — and the street
 *  stands empty about 82% of the time. */
object Traffic {
    /** Ticks between pass slots, per lane. */
    const val PERIOD_TICKS = 1140L

    /** Ticks a pass takes to cross the scene (unchanged
     *  from the old derivation — same driving speed). */
    const val PASS_TICKS = 210L

    /** The far lane's slot offset, in ticks (half a
     *  period, as the old two-lane stagger offset by
     *  half its period). */
    const val LANE_GAP = 570L

    /** View ticks per scene-minute (50 ms tick). */
    const val TICKS_PER_MINUTE = 1200L

    /** Where [lane]'s car is in its pass, 0..1, or null
     *  when the lane is empty this tick. Lane 0 is the
     *  far lane, lane 1 the near lane. */
    fun progress(tick: Long, lane: Int): Float? {
        val local = (tick + lane * LANE_GAP) % PERIOD_TICKS
        if (local < 0 || local > PASS_TICKS) return null
        return local / PASS_TICKS.toFloat()
    }

    /** Direction is a property of the LANE, as it was
     *  in the old inline loop: the near lane carries
     *  traffic left to right, the far lane right to
     *  left — right-hand traffic, readable at a glance. */
    fun dirRight(lane: Int): Boolean = lane == 1

    /** Expected ambient passes per scene-minute across
     *  both lanes: ≈ 2.1 (the old derivation: ≈ 7.1). */
    fun passesPerMinute(): Float =
        2f * TICKS_PER_MINUTE / PERIOD_TICKS
}

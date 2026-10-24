package com.hunt3r.southparktown

// ============================================================
// WorldScale — v0.15.0d, THE OVERHAUL CORRECTION (Lucky's
// playtest of v0.15.0c, Oct 17: figures "far too large
// against the scene", adults rendering kid-sized, trees
// and props at the wrong scale, the player walking up
// onto walls, doors floating as free slabs, the school
// bus drawn as a door).
//
// ONE world scale, derived from figure height, read by
// every painter that puts a sized thing in a scene:
//
//  - [WorldScale]: the size ladder in KID UNITS (1.0 = a
//    kid figure's on-screen height). The kid figure is
//    KID_FRAC of scene height (target band 18–22%);
//    adults compose at ADULT_RATIO × kid through their
//    own measured heightRel (CharacterSystem/KidArt own
//    the per-character numbers, all ≥ ~1.28 for adults);
//    every other thing in the world — houses, trees,
//    cars, doors, trash cans — takes its size from this
//    one table, so the ordering house > tree > car >
//    adult > kid > trash holds BY CONSTRUCTION and the
//    suite pins it from these same numbers.
//
//  - [WalkBand]: the walkable ground band, per scene.
//    Outdoors it is the sidewalk/yard band between the
//    building line and the street; indoors the floor.
//    Feet lines are fractions of scene height; a figure's
//    feet never render above the band's top line.
//
//  - [FacadeGeom]: the ONE facade/door geometry — the
//    rect SceneArt paints and the rect GameView outlines
//    and hit-tests are the same numbers, so a door can
//    never drift off its building again. Doors stand ON
//    facades with their base on the ground line; outdoor
//    and hub locations get a drawn STRUCTURE (shelter,
//    pavilion, hut, barn, loft, diner front) whose front
//    carries the door — there is no free-standing door
//    slab anywhere in the town anymore.
//
//  - [BusInteriorSpec]: "Inside the School Bus" as a
//    layered paper interior, described as data (window
//    band with the street outside, seat rows on the back
//    and front furniture planes, the aisle floor, the
//    driver area) — InteriorArt paints exactly this
//    list; the floating-shapes version is gone.
//
// Pure Kotlin, no android imports: the JVM suite pins
// all of it from these same functions the painters call.
// ============================================================

object WorldScale {
    /** A kid figure's height as a fraction of scene
     *  height. Lucky's target band: 18–22%. */
    const val KID_FRAC = 0.19f

    /** Adults compose at this × the kid height (their
     *  per-character heightRel scatters around it). */
    const val ADULT_RATIO = 1.42f

    // ---- the ladder, in kid units (1.0 = kid height) ----
    const val HOUSE_PEAK_U = 2.62f    // house, eaves + roof peak
    const val HOUSE_EAVES_U = 1.95f   // house body to the eaves
    const val BLOCK_PEAK_U = 2.90f    // downtown block (taller by design)
    const val NEIGHBOR_PEAK_U = 2.45f // a neighbor house, total
    const val PINE_U = 2.15f          // a mature pine
    const val TREE_U = 1.95f          // a mature street tree
    const val LAMP_U = 2.40f          // a lamp post
    const val FLAGPOLE_U = 2.60f      // the school flagpole
    const val CAR_LEN_U = 2.00f       // a car's length
    const val CAR_H_U = 0.92f         // a car's height
    const val BUS_LEN_U = 3.40f       // the bus's length
    const val BUS_H_U = 1.50f         // the bus's height
    const val DOOR_U = 1.73f          // a door ≈ 1.22 × adult
    const val SIGN_TOP_U = 1.85f      // the bus-stop sign, ground to top
    const val BENCH_LEN_U = 1.45f
    const val BENCH_H_U = 0.68f
    const val FENCE_U = 0.62f
    const val MAILBOX_U = 0.55f
    const val BUSH_U = 0.55f
    const val TRASH_U = 0.40f         // a trash can ≤ 0.45 × kid
    const val HYDRANT_U = 0.36f

    // ---- deliberate exceptions, named in the data ----
    /** A sapling is SUPPOSED to be small — an exception
     *  in the data, not an accident of the painter. */
    const val SAPLING_U = 0.80f
    /** The Tegridy silo is a landmark: taller than the
     *  barn it stands beside, on purpose. */
    const val SILO_U = 3.10f

    /** The ladder as an ordered table (name → kid units),
     *  the exact chain the suite pins: house > tree >
     *  car length > adult > kid > trash. */
    val LADDER: List<Pair<String, Float>> = listOf(
        "house" to HOUSE_PEAK_U,
        "tree" to PINE_U,
        "car length" to CAR_LEN_U,
        "adult" to ADULT_RATIO,
        "kid" to 1.0f,
        "trash can" to TRASH_U
    )

    /** One kid height in px for a scene [h] px tall. */
    fun kidPx(h: Float): Float = h * KID_FRAC

    /** [u] kid units in px for a scene [h] px tall. */
    fun units(u: Float, h: Float): Float = u * kidPx(h)

    /** A figure's composed on-screen height: the kid
     *  unit scaled by the character's own measured
     *  heightRel (KidArt.heightFor — kids ~0.8–1.14,
     *  adults ~1.28–1.48, teens between). This is the
     *  function GameView's crowd pass sizes figures by. */
    fun figurePx(h: Float, heightRel: Float): Float =
        kidPx(h) * heightRel

    /** A door's composed height in px. */
    fun doorPx(h: Float): Float = units(DOOR_U, h)
}

// ---------------- the walkable ground band ----------------

object WalkBand {
    /** Feet lines (fractions of scene height) outdoors:
     *  the band runs from just in front of the building
     *  line (the facades' ground line is 0.66) down to
     *  the sidewalk at the street's edge. v0.15.0g: the
     *  band's front edge is the sidewalk's street edge —
     *  below it lies the ROAD's own band (see
     *  ROAD_TOP_OUT), which feet enter only inside a
     *  crosswalk. */
    const val TOP_OUT = 0.685f
    const val BOTTOM_OUT = 0.875f

    // v0.15.0g (playtest round 2: "the sidewalk is
    // walkable the road is not unless a crosswalk", and
    // the road itself must actually READ as a road): the
    // road is a real band below the sidewalk — asphalt
    // from ROAD_TOP_OUT to the scene's bottom edge — and
    // an outdoor band BEYOND 1 is feet ON the road:
    // band 1 is the near curb (the sidewalk's street
    // edge), band BAND_MAX_OUT the bottom edge of the
    // road, where a crossing ends at the far curb.
    // Road bands exist outdoors only; the tap layer
    // gates them by crosswalk (PaperDressing.crosswalks)
    // so a tap on open road clamps back to band 1.
    // Interiors never leave 0..1.
    const val ROAD_TOP_OUT = 0.885f   // == Depth.FG_TOP_OUT (pinned)
    const val ROAD_FEET_BOTTOM_OUT = 0.985f
    const val BAND_MAX_OUT = 1.5f

    /** Feet lines indoors: the floor runs from just
     *  below the wall/floor seam (InteriorArt's floorY
     *  is 0.64) to the room's near edge. */
    const val TOP_IN = 0.675f
    const val BOTTOM_IN = 0.905f

    fun top(inside: Boolean): Float = if (inside) TOP_IN else TOP_OUT
    fun bottom(inside: Boolean): Float = if (inside) BOTTOM_IN else BOTTOM_OUT

    /** The feet line (px) for a ground-plane [band]
     *  (0 = the back of the band, by the buildings;
     *  1 = the front, at the street / near edge). Any
     *  band in 0..1 lands INSIDE the walkable band by
     *  construction — feet can never render above the
     *  band's top line. Outdoors, bands past 1 walk the
     *  road strip out to ROAD_FEET_BOTTOM_OUT. */
    fun feetY(band: Float, h: Float, inside: Boolean): Float {
        val t = top(inside) * h
        val b = bottom(inside) * h
        val bb = band.coerceIn(0f,
            if (inside) 1f else BAND_MAX_OUT)
        if (bb <= 1f) return t + bb * (b - t)
        val roadB = ROAD_FEET_BOTTOM_OUT * h
        return b + (bb - 1f) / (BAND_MAX_OUT - 1f) * (roadB - b)
    }

    /** Inverse of [feetY]: which band a screen/world
     *  y belongs to. Outdoors, y below the sidewalk's
     *  edge reads as a road band (1..BAND_MAX_OUT);
     *  indoors everything clamps into the floor. */
    fun bandForY(y: Float, h: Float, inside: Boolean): Float {
        val t = top(inside) * h
        val b = bottom(inside) * h
        if (inside || y <= b)
            return ((y - t) / (b - t)).coerceIn(0f, 1f)
        val roadB = ROAD_FEET_BOTTOM_OUT * h
        return (1f + (y - b) / (roadB - b) *
            (BAND_MAX_OUT - 1f)).coerceIn(1f, BAND_MAX_OUT)
    }

    /** True when [band] puts feet on the road (a state
     *  only a crosswalk may legitimately produce). */
    fun isRoad(band: Float): Boolean = band > 1f

    /** True when world-fraction [x] falls inside any
     *  of [spans] (PaperDressing.crosswalks' shape). */
    fun inSpan(x: Float, spans: List<Pair<Float, Float>>): Boolean =
        spans.any { x >= it.first && x <= it.second }

    /** Depth scale at a band — v0.15.0g strengthens the
     *  curve to the read Lucky asked for ("if my
     *  character walks up to the house then he appears
     *  smaller against the house but when he's next to
     *  the street he's bigger"): 0.75× at the back of
     *  the band, swelling to 1.15× at the front edge.
     *  The old 0.80→1.00 breath is retired with it. ONE
     *  curve for the player, every crowd figure and
     *  every standing prop; it multiplies every figure
     *  at the same band by the same factor, so the
     *  v0.15.0d ladder holds at EVERY depth. Bands past
     *  1 (mid-crossing) hold the front value. */
    fun depthScale(band: Float): Float =
        0.75f + 0.40f * band.coerceIn(0f, 1f)

    /** A standing prop's footprint in the ground plane:
     *  center ([x] across the world, [band] down the
     *  band) and radii in the same units. The player
     *  walks AROUND footprints, never through them. */
    data class Footprint(val x: Float, val band: Float,
                         val rx: Float, val rBand: Float)

    /** Push a walk target out of every footprint it
     *  lands in: the target slides to the footprint's
     *  near edge (the closest point of the ellipse), so
     *  a tap on a tree walks you up beside it, never
     *  into its trunk. Deterministic; a target outside
     *  every footprint comes back unchanged. */
    fun avoid(x: Float, band: Float,
              prints: List<Footprint>): Pair<Float, Float> {
        var px = x
        var pb = band
        for (f in prints) {
            val dx = (px - f.x) / f.rx
            val db = (pb - f.band) / f.rBand
            val d2 = dx * dx + db * db
            if (d2 < 1f) {
                if (d2 < 0.0001f) {
                    // dead center: step out toward the street
                    pb = f.band + f.rBand
                } else {
                    val d = kotlin.math.sqrt(d2)
                    px = f.x + (px - f.x) / d
                    pb = f.band + (pb - f.band) / d
                }
            }
        }
        // v0.15.0g: the band clamp widens outdoors to the
        // road's far edge, so a crossing target beside a
        // footprint is pushed out along the road, not
        // folded back onto the sidewalk. (Footprints
        // themselves all live at band <= 1; a road target
        // is outside every one of them by construction.)
        return px.coerceIn(0.02f, 0.98f) to
            pb.coerceIn(0f, BAND_MAX_OUT)
    }
}

// ---------------- facade + door geometry ----------------

/** A pixel rect, pure (the painters wrap it in RectF). */
data class GeomRect(val left: Float, val top: Float,
                    val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val cx: Float get() = (left + right) / 2f
}

object FacadeGeom {
    /** The ground line every facade stands on and
     *  every door's base sits on (fraction of scene
     *  height). SceneArt's facade, neighbor row and
     *  structure painters and GameView's door pass all
     *  read this one line. */
    const val GROUND_LINE = 0.66f

    /** Facade body widths (fraction of the COMPOSITION
     *  width — see [facadeRect]'s unitW), per location
     *  kind — unchanged from the town's established
     *  composition. */
    fun bodyWFrac(kind: String): Float = when (kind) {
        "house" -> 0.52f; "shop" -> 0.66f; "civic" -> 0.58f
        "school" -> 0.74f; else -> 0.40f
    }

    /** Facade body height (px, to the eaves) per kind,
     *  from the ladder: houses at HOUSE_EAVES, shops and
     *  civic buildings a touch grander, the school the
     *  grandest building on its row. */
    fun bodyH(kind: String, h: Float): Float = when (kind) {
        "shop" -> WorldScale.units(2.05f, h)
        "civic" -> WorldScale.units(2.20f, h)
        "school" -> WorldScale.units(2.30f, h)
        else -> WorldScale.units(WorldScale.HOUSE_EAVES_U, h)
    }

    /** The location's own facade body: centered on the
     *  world, base on the ground line. v0.15.0g: widths
     *  compose against [unitW] — the width the town's
     *  fraction tables were written for (the pre-widening
     *  world width, SceneScale's job to supply) — so in
     *  the widened world the building keeps its composed
     *  size at the scene's heart while the world grows
     *  around it; callers that compose a whole scene in
     *  one width (the suite, photos) keep unitW == w. */
    fun facadeRect(kind: String, w: Float, h: Float,
                   unitW: Float = w): GeomRect {
        val bw = unitW * bodyWFrac(kind)
        val bh = bodyH(kind, h)
        val base = GROUND_LINE * h
        return GeomRect(w / 2f - bw / 2f, base - bh, w / 2f + bw / 2f, base)
    }

    /** The facade's door: ON the facade, centered, its
     *  base ON the ground line, its height from the
     *  ladder (DOOR_U ≈ 1.22 × adult). One rect, read by
     *  the facade painter (which fills it) and by the
     *  view's door pass (which pulses its outline and
     *  hit-tests it) — the same numbers both times. */
    fun doorRect(kind: String, w: Float, h: Float,
                 unitW: Float = w): GeomRect {
        val f = facadeRect(kind, w, h, unitW)
        val dh = WorldScale.doorPx(h)
        val dw = WorldScale.units(0.46f, h)
        return GeomRect(f.cx - dw / 2f, f.bottom - dh, f.cx + dw / 2f, f.bottom)
    }

    // ---- outdoor / hub structures ----

    /** The drawn structure an outdoor or hub location
     *  fronts the street with (TownBaseline owns which):
     *  its body rect, base on the ground line. v0.15.0g:
     *  the structure stands at the scene's HEART (the
     *  world center) — the 0.68 offset it carried in the
     *  narrow world would strand it down the widened
     *  street, away from the crowd window, the door the
     *  player spawns at and the name sign over it. Its
     *  width composes against [unitW] like the facade's. */
    fun structureRect(structure: String, w: Float, h: Float,
                      unitW: Float = w): GeomRect {
        val base = GROUND_LINE * h
        val cx = w * 0.5f
        val (wf, hu) = when (structure) {
            "shelter" -> 0.40f to 1.55f   // the bus shelter: open front
            "pavilion" -> 0.42f to 1.70f
            "hut" -> 0.30f to 1.80f
            "barn" -> 0.44f to 2.30f
            "loft" -> 0.40f to 2.45f
            "diner" -> 0.52f to 2.05f
            "mall" -> 0.56f to 2.60f
            else -> 0.36f to 1.90f
        }
        val bw = unitW * wf
        val bh = WorldScale.units(hu, h)
        return GeomRect(cx - bw / 2f, base - bh, cx + bw / 2f, base)
    }

    /** The structure's door (or, for the open-front
     *  shelter and pavilion, its framed opening): base
     *  on the ground line, ladder height, centered on
     *  the structure's front. */
    fun structureDoorRect(structure: String, w: Float, h: Float,
                          unitW: Float = w): GeomRect {
        val s = structureRect(structure, w, h, unitW)
        val dh = WorldScale.doorPx(h)
        val dw = WorldScale.units(
            if (structure == "shelter" || structure == "pavilion")
                1.30f else 0.46f, h)
        return GeomRect(s.cx - dw / 2f, s.bottom - dh, s.cx + dw / 2f, s.bottom)
    }
}

// ---------------- the bus interior, as data ----------------

/** "Inside the School Bus" (the BusStop interior),
 *  composed as a layered paper interior. InteriorArt
 *  paints exactly these pieces on exactly these
 *  planes; the suite pins the list, so the room can
 *  never collapse back into a blank void with floating
 *  shapes. */
object BusInteriorSpec {
    data class Piece(val plane: PaperRoomPlane, val kind: String)

    const val WINDOW_BAND = "window band"
    const val CEILING = "ceiling band"
    const val ROUTE_BOARD = "route board"
    const val DRIVER = "driver area"
    const val SEAT_BACKS = "seat row backs"
    const val SEAT_FRONTS = "seat row fronts"
    const val AISLE = "aisle floor"
    const val REAR_DOOR = "rear door"

    val PIECES: List<Piece> = listOf(
        Piece(PaperRoomPlane.WALL, CEILING),
        Piece(PaperRoomPlane.WALL, WINDOW_BAND),
        Piece(PaperRoomPlane.WALL, ROUTE_BOARD),
        Piece(PaperRoomPlane.FURNITURE_BACK, DRIVER),
        Piece(PaperRoomPlane.FURNITURE_BACK, SEAT_BACKS),
        Piece(PaperRoomPlane.FLOOR, AISLE),
        Piece(PaperRoomPlane.FURNITURE_FRONT, SEAT_FRONTS),
        Piece(PaperRoomPlane.FURNITURE_FRONT, REAR_DOOR)
    )

    /** Seat rows along the bus: pairs of seats on the
     *  wall side (their backs face the aisle on the
     *  back plane) and the near row's backs on the
     *  front plane. */
    const val SEAT_ROWS = 4

    /** The window band's vertical span (fractions of
     *  room height): the street rolls past outside it. */
    const val WINDOW_TOP = 0.16f
    const val WINDOW_BOTTOM = 0.46f
}

// ---------------- set pieces (v0.17.0a, spgen §6) ----------------

/** A set piece's three cards, back to front. The planes
 *  slot into the scene's layer stack the way the painters
 *  already compose them: the BACK card reads behind the
 *  action at the piece's ground line, the MID card is the
 *  piece's body at its band, the FRONT card is the near
 *  lip that can overlap feet (the Depth foreground law:
 *  ground and ankles only, never a face). */
enum class SetPlane { BACK, MID, FRONT }

data class SetCard(val plane: SetPlane,
                   val region: PaperRegion,
                   val detail: String)

/** One registered set piece: generated to the footprint of
 *  the parcel it occupies, drawn as three cards, under the
 *  same scale contract + outline engine + region paper as
 *  every figure — a dumpster and the cardboard fort are
 *  dimensionally coherent. [heightU] is the size the town
 *  composes it at (the WorldScale ladder value, or the
 *  prop-relative contract for fiction pieces); [contractU]
 *  is the independent contract value the audit checks it
 *  against; [painterMarker] is the source token the suite
 *  greps for to prove the painter really composes from
 *  the ladder and not from a hardcoded drift. */
data class SetPiece(
    val id: String,
    val heightU: Float,
    val contractU: Float,
    val cards: List<SetCard>,
    val painterMarker: String,
    val region: PaperRegion = PaperRegion.SURFACE,
    val madeFrom: String = ""
)

object SetPieces {
    private fun cards(region: PaperRegion, back: String,
                      mid: String,
                      front: String): List<SetCard> = listOf(
        SetCard(SetPlane.BACK, region, back),
        SetCard(SetPlane.MID, region, mid),
        SetCard(SetPlane.FRONT, region, front))

    val ALL: List<SetPiece> = listOf(
        SetPiece("bench", WorldScale.BENCH_H_U, 0.68f,
            cards(PaperRegion.SURFACE, "seat-back slats", "seat", "front legs"),
            "WorldScale.BENCH_H_U"),
        SetPiece("trash_can", WorldScale.TRASH_U, 0.40f,
            cards(PaperRegion.METAL, "rim", "body", "front lip"),
            "WorldScale.TRASH_U", PaperRegion.METAL,
            "galvanized can"),
        SetPiece("mailbox", WorldScale.MAILBOX_U, 0.55f,
            cards(PaperRegion.SURFACE, "post", "box", "flag"),
            "WorldScale.MAILBOX_U"),
        SetPiece("hydrant", WorldScale.HYDRANT_U, 0.36f,
            cards(PaperRegion.METAL, "cap", "body", "nozzle"),
            "WorldScale.HYDRANT_U", PaperRegion.METAL,
            "cast iron"),
        SetPiece("bush", WorldScale.BUSH_U, 0.55f,
            cards(PaperRegion.SURFACE, "back mass", "body mass", "near lip"),
            "WorldScale.BUSH_U"),
        SetPiece("fence", WorldScale.FENCE_U, 0.62f,
            cards(PaperRegion.SURFACE, "far rail", "posts", "near rail lip"),
            "WorldScale.FENCE_U"),
        SetPiece("pine", WorldScale.PINE_U, 2.15f,
            cards(PaperRegion.SURFACE, "far foliage", "trunk + main foliage",
                "near branch lip"),
            "WorldScale.PINE_U"),
        SetPiece("tree", WorldScale.TREE_U, 1.95f,
            cards(PaperRegion.SURFACE, "far canopy", "trunk + canopy",
                "near canopy lip"),
            "WorldScale.TREE_U"),
        SetPiece("lamp", WorldScale.LAMP_U, 2.40f,
            cards(PaperRegion.METAL, "pole", "arm + head", "light pool lip"),
            "WorldScale.LAMP_U", PaperRegion.METAL,
            "street iron"),
        SetPiece("bus_sign", WorldScale.SIGN_TOP_U, 1.85f,
            cards(PaperRegion.METAL, "pole", "sign board", "sign face"),
            "WorldScale.SIGN_TOP_U", PaperRegion.METAL,
            "sign iron"),
        // ---- fiction set pieces: the same registry, the
        // same contract — dressings ARE set pieces with
        // provenance, unified here, not forked ----
        SetPiece("crate", 0.35f,
            ScaleContract.PROP_RELATIVE["crate"] ?: 0.35f,
            cards(PaperRegion.SURFACE, "back panel", "box body", "tape cross"),
            "cardboard_wall", PaperRegion.SURFACE,
            "refrigerator_box"),
        SetPiece("banner_stand", 0.90f,
            ScaleContract.PROP_RELATIVE["banner"] ?: 0.90f,
            cards(PaperRegion.CLOTH, "sheet back", "sheet face",
                "sagging hem + tape tabs"),
            "bedsheet_banner", PaperRegion.CLOTH,
            "bedsheet")
    )

    fun byId(id: String): SetPiece? = ALL.firstOrNull { it.id == id }

    /** Fractional drift of a piece from its contract. */
    fun drift(p: SetPiece): Float =
        kotlin.math.abs(p.heightU - p.contractU) /
            p.contractU
}

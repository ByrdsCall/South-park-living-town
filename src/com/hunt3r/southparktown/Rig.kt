package com.hunt3r.southparktown

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ============================================================
// Rig — v0.17.0a: the CHARACTER SYSTEM REDO. Lucky's spgen
// pipeline doc (Oct 24, 2026) lands its disciplines as code:
//
//  - [ScaleContract] (spgen §1): ONE canonical unit system.
//    The unit is the kid height; body scales per class;
//    outline weight is law in FINAL SCREEN PIXELS (window
//    [3, 7] px at 1:1 — see OutlineEngine); props scale from
//    the WEARER, never absolutely; a quality floor gates
//    every render context.
//
//  - [Skeleton] + [Rigger] (spgen §§4+7): figure assembly
//    is a rigger over ONE shared 20-joint skeleton — no
//    fingers, no facial bones. Every part is a card
//    {id, bone, pivot, z, coverage, made_from}; assembly
//    sorts by z; an unbound bone is a hard error; and the
//    PIVOT GATE runs over every cast member, the player
//    and the fiction kit: a pivot outside its silhouette
//    or off its joint fails the build. KidArt draws its
//    limb anchors FROM the assembled cards, so the rig is
//    load-bearing, not decorative.
//
//  - [FaceManifest] (spgen §5): expressions are a named
//    grid — 8 emotions × 6 mouths, stable ids (angry_wide)
//    that dialogue and animation reference by name. Every
//    legacy expression maps onto the grid; what exceeds it
//    (animal faces, state faces) is logged as extensions,
//    never silently dropped. [FaceCompat] keeps every
//    identifier a pre-redo save can hold resolving.
//
// Pure Kotlin (no android): the JVM suite IS the validation
// gate (spgen §10) and pins all of it from these functions.
// ============================================================

object ScaleContract {
    /** The canonical unit: one kid height. */
    const val UNIT = "kid_height"

    /** Authoring resolution: texels per unit in the doc.
     *  Our parts are procedural, so this is the REFERENCE
     *  the density floor is measured against, not a texture
     *  size. */
    const val AUTHOR_PPM = 512

    /** Outline law, final screen pixels at 1:1. */
    const val OUTLINE_MIN_PX = 3f
    const val OUTLINE_MAX_PX = 7f

    /** The gate: a part more than 5% off its contract
     *  scale is rejected or individually logged. */
    const val SCALE_TOLERANCE = 0.05f

    /** Minimum density floor (spgen's min_texel_density):
     *  enforced as a render-scale floor — see
     *  [densityFloorOk]. */
    const val MIN_DENSITY = 0.85f

    /** Body scale per class (spgen §1, adopted verbatim). */
    val BODY_SCALE: Map<String, Float> = mapOf(
        "kid" to 1.00f,
        "adult" to 1.35f,
        "cartman" to 0.95f,
        "toddler" to 0.70f,
        "elder" to 1.30f
    )

    /** Extension classes (ours): the teen bands ride
     *  between kid and adult on AgeBand's own ladder —
     *  logged here because the doc's table has no teens. */
    val BODY_SCALE_EXT: Map<String, Float> = mapOf(
        "young_teen" to 1.12f,
        "teen" to 1.22f,
        "older_teen" to 1.31f
    )

    /** Prop sizes relative to the WEARER's height (spgen
     *  §1's four, adopted verbatim, plus our fiction kit
     *  under the same law — a prop is sized from whoever
     *  wears or carries it, never absolutely). */
    val PROP_RELATIVE: Map<String, Float> = mapOf(
        "lance" to 1.20f,
        "shield" to 0.40f,
        "banner" to 0.90f,
        "crate" to 0.35f,
        "broom_lance" to 1.20f,
        "lid_shield" to 0.40f,
        "bedsheet_banner" to 0.90f,
        "cardboard_wall" to 1.05f,
        "stick_sword" to 0.45f,
        "cape" to 0.62f
    )

    /** The scale class one character belongs to. Single-
     *  member classes first (their class IS the character),
     *  then the age-band ladder. */
    fun classOf(name: String): String = when (name) {
        "Cartman" -> "cartman"
        "Ike" -> "toddler"
        "Marvin" -> "elder"
        else -> when (CharacterSystem.bandOf(name)) {
            AgeBand.KID -> "kid"
            AgeBand.YOUNG_TEEN -> "young_teen"
            AgeBand.TEEN -> "teen"
            AgeBand.OLDER_TEEN -> "older_teen"
            AgeBand.ADULT -> "adult"
        }
    }

    /** The contract value for a class. */
    fun contractOf(cls: String): Float =
        BODY_SCALE[cls] ?: BODY_SCALE_EXT[cls] ?: 1.0f

    /** The anchor OUR town actually composes the class at:
     *  the age band's heightScale for band classes; the one
     *  member's measured heightRel for single-member
     *  classes (their sheet is their class anchor). */
    fun ourAnchor(cls: String): Float = when (cls) {
        "kid" -> AgeBand.KID.heightScale
        "young_teen" -> AgeBand.YOUNG_TEEN.heightScale
        "teen" -> AgeBand.TEEN.heightScale
        "older_teen" -> AgeBand.OLDER_TEEN.heightScale
        "adult" -> AgeBand.ADULT.heightScale
        "cartman" -> KidArt.heightFor("Cartman")
        "toddler" -> KidArt.heightFor("Ike")
        "elder" -> KidArt.heightFor("Marvin")
        else -> 1.0f
    }

    /** Fractional divergence of a class anchor from its
     *  contract value. */
    fun divergence(cls: String): Float {
        val c = contractOf(cls)
        return abs(ourAnchor(cls) - c) / c
    }

    /** Class divergences past the 5% gate, each with its
     *  measured reason. Lucky's per-character spec data is
     *  law over the generic class — these are LOGGED, not
     *  "corrected" off his sheets. The suite pins this map:
     *  a class may diverge only if it is named here, and
     *  every entry here must still be true. */
    val DIVERGENCES: Map<String, String> = mapOf(
        "cartman" to "Anchor 1.000 vs contract 0.95 (+5.3%): " +
            "Cartman's bulk is WIDTH-carried (headBodyWidth " +
            "1.346, the widest silhouette in town); his " +
            "measured sheet height is exactly one kid. " +
            "Shrinking him to the generic class would move " +
            "him off Lucky's approved sheet — logged.",
        "toddler" to "Anchor 0.80 vs contract 0.70 (+14.3%): " +
            "Ike's measured heightRel is 0.80 — the smallest " +
            "figure in town, all head (head share 0.655). " +
            "The generic toddler class would shrink him " +
            "below his own reference sheet — logged."
    )

    /** A prop's size in px for a wearer [wearerPx] tall. */
    fun propPx(wearerPx: Float, prop: String): Float =
        wearerPx * (PROP_RELATIVE[prop] ?: 0.5f)

    /** The density floor, translated for a procedural
     *  engine: a render context is legal only if one kid
     *  unit renders at >= MIN_DENSITY × the unit's size at
     *  the reference context (a kid at scene scale on a
     *  1280-tall frame = WorldScale.KID_FRAC × 1280). Below
     *  the floor a part would mush — the context must not
     *  be used for parts at all. */
    const val REFERENCE_UNIT_PX = 243.2f // 0.19 × 1280
    fun densityFloorOk(unitPx: Float): Boolean =
        unitPx >= REFERENCE_UNIT_PX * MIN_DENSITY
}

// ---------------- the shared skeleton (spgen §7) ----------------

object Skeleton {
    /** The ONE skeleton every human figure assembles on:
     *  20 joints, no fingers, no facial bones (faces are
     *  swaps — FaceManifest's business, not the rig's). */
    val JOINTS: List<String> = listOf(
        "root", "hips", "spine", "chest", "neck", "head",
        "shoulder.L", "upperarm.L", "forearm.L", "hand.L",
        "shoulder.R", "upperarm.R", "forearm.R", "hand.R",
        "thigh.L", "shin.L", "foot.L",
        "thigh.R", "shin.R", "foot.R"
    )

    fun isJoint(name: String): Boolean = name in JOINTS
}

/** One part card (spgen §4): a flat card carrying its full
 *  metadata. [pivot] is in the figure's unit space (the
 *  FigureGeometry box: 100 wide, 128 kid / 150 adult tall),
 *  AT the joint it rotates around. [z] resolves draw order
 *  at assembly. [coverage] and [madeFrom] are the fiction
 *  layer's provenance, carried on every card — a cape is a
 *  bedsheet in the fiction and cloth in the creator. */
data class PartCard(
    val id: String,
    val bone: String,
    val pivot: FPt,
    val z: Float,
    val coverage: Float,
    val madeFrom: String,
    val region: PaperRegion
)

object Rigger {

    data class Rig(val owner: String,
                   val parts: List<PartCard>,
                   val errors: List<String>) {
        fun part(id: String): PartCard? = parts.firstOrNull { it.id == id }
    }

    /** The front-view draw stack the painters actually
     *  run, as part ids in draw order — KidArt's marker
     *  comments name these steps, and the suite pins the
     *  painter's order against the assembled z order. */
    val FRONT_STACK: List<String> = listOf(
        "back", "thigh.L", "thigh.R", "foot.L", "foot.R",
        "upperarm.L", "upperarm.R", "forearm.L", "forearm.R",
        "hand.L", "hand.R", "torso", "head", "hair", "hat"
    )

    /** Assemble one figure's cards from its spec. Pure:
     *  the same spec assembles the same rig every time.
     *  Errors are collected, not thrown — the suite reads
     *  them as gate failures with reasons. */
    fun assemble(owner: String, spec: SheetSpec): Rig {
        val d = FigureGeometry.derive(spec)
        val p = d.plan
        val parts = ArrayList<PartCard>()
        val errors = ArrayList<String>()
        fun add(id: String, bone: String, x: Float, y: Float,
                z: Float, madeFrom: String, region: PaperRegion,
                coverage: Float = 1f) {
            parts += PartCard(id, bone, FPt(x, y), z,
                coverage, madeFrom, region)
        }
        // back item — behind everything the body wears
        if (spec.backStyle != "none" || spec.backpack) {
            val made = when (spec.backStyle) {
                "cape" -> "bedsheet"
                "wings" -> "cardboard"
                else -> "canvas pack"
            }
            add("back", "chest", 50f, p.torsoTop + 4f, -40f,
                made, PaperRegion.CLOTH)
        }
        val skinRegion = PaperRegion.SKIN
        val legRegion = if (spec.bottomStyle == "pants")
            PaperRegion.CLOTH else PaperRegion.SKIN
        for (side in listOf(-1f, 1f)) {
            val tag = if (side < 0f) "L" else "R"
            val legZ = if (side < 0f) -20f else -19f
            add("thigh.$tag", "thigh.$tag",
                50f + side * p.hipX, p.hipY, legZ,
                "self", legRegion)
            add("foot.$tag", "foot.$tag",
                50f + side * p.hipX, p.ankleY + 3f, legZ + 2f,
                "self", if (spec.barefoot) skinRegion
                    else PaperRegion.SURFACE)
            // the arm at rest: rotated by the sheet's own
            // tilt around the shoulder anchor, exactly the
            // transform KidArt.drawArm applies
            val rot = Math.toRadians((-side * spec.armTilt)
                .toDouble())
            val sx = 50f + side * p.shoulderX
            val sy = p.shoulderY
            val hx = sx - (p.armLen * sin(rot)).toFloat()
            val hy = sy + (p.armLen * cos(rot)).toFloat()
            // z resolves the FRONT_STACK order at assembly:
            // both upper arms, both forearms, both hands —
            // each pair behind the torso, which swallows the
            // sleeve tops (the attachment the sheets draw)
            val armBase = if (side < 0f) 0f else 0.4f
            add("upperarm.$tag", "upperarm.$tag", sx, sy,
                -8f + armBase, "self",
                if (spec.special == "underwear_top")
                    skinRegion else PaperRegion.CLOTH)
            add("forearm.$tag", "forearm.$tag",
                sx + (hx - sx) * 0.8f, sy + (hy - sy) * 0.8f,
                -7.2f + armBase, "self",
                if (spec.special == "underwear_top")
                    skinRegion else PaperRegion.CLOTH)
            add("hand.$tag", "hand.$tag", hx, hy,
                -6.4f + armBase,
                spec.handColor ?: "self", skinRegion)
        }
        if (spec.special == "wheelchair") {
            add("wheelchair", "hips", 50f, p.ankleY - 7f, -15f,
                "metal chair", PaperRegion.METAL)
        }
        add("torso", "spine", 50f, p.torsoBottom - 2f, 0f,
            if (spec.special == "towel") "bath towel" else "self",
            if (spec.special == "underwear_top" ||
                spec.special == "towel")
                PaperRegion.CLOTH else PaperRegion.CLOTH)
        add("head", "head", d.hx, d.hy + d.hry * 0.8f, 20f,
            "self", skinRegion)
        if (spec.hairStyle != "bald")
            add("hair", "head", d.hx, d.hy + d.hry * 0.8f, 30f,
                "self", PaperRegion.SURFACE)
        if (spec.hatId != "none")
            add("hat", "head", d.hx, d.hy + d.hry * 0.8f, 40f,
                "self", PaperRegion.CLOTH)
        // ---- the gate, run at assembly ----
        val seen = HashSet<String>()
        for (part in parts) {
            if (!seen.add(part.id))
                errors += "duplicate part id ${part.id} ($owner)"
            if (!Skeleton.isJoint(part.bone))
                errors += "unbound bone ${part.bone} in " +
                    "${part.id} ($owner)"
            if (!insideSilhouette(spec, d, part.pivot))
                errors += "pivot outside silhouette: " +
                    "${part.id} ($owner) at ${part.pivot}"
        }
        return Rig(owner, parts.sortedBy { it.z }, errors)
    }

    /** The pivot of one part of one spec — KidArt reads
     *  its limb anchors through here, so the painter and
     *  the gate can never disagree about where a joint is. */
    fun anchorOf(owner: String, spec: SheetSpec,
                 partId: String): FPt =
        assemble(owner, spec).part(partId)?.pivot
            ?: FPt(50f, 0f)

    /** Is [pt] (unit space) inside the figure's silhouette?
     *  The union of the head ellipse, the torso trapezium
     *  (with sleeve slack — the shoulder anchor bites into
     *  it by construction) and the four limb capsules at
     *  rest. This is the pivot gate's geometry: a pivot
     *  outside every mass would detach when the rig moves. */
    fun insideSilhouette(spec: SheetSpec,
                         d: FigureGeometry.Derived,
                         pt: FPt): Boolean {
        val p = d.plan
        val ex = (pt.x - d.hx) / maxOf(d.hrx, 0.01f)
        val ey = (pt.y - d.hy) / maxOf(d.hry, 0.01f)
        if (ex * ex + ey * ey <= 1.05f) return true
        if (pt.y >= p.torsoTop - 3f && pt.y <= p.torsoBottom + 3f) {
            val half = FigureGeometry.torsoHalfAt(p,
                pt.y.coerceIn(p.torsoTop, p.torsoBottom))
            if (abs(pt.x - 50f) <= half + p.armRad + 1.5f)
                return true
        }
        // the wheelchair's seat box: the chair's silhouette
        // spans hip width at seat height, below the torso's
        // hem — its pivot rides there by design (Timmy)
        if (spec.special == "wheelchair" &&
            pt.y >= p.torsoTop && pt.y <= p.ankleY + 2f &&
            abs(pt.x - 50f) <= p.torsoHalfBot + 8f) return true
        for (side in listOf(-1f, 1f)) {
            if (distSeg(pt, FPt(50f + side * p.hipX, p.hipY),
                    FPt(50f + side * p.hipX, p.ankleY)) <=
                p.legRad + 2f) return true
            val rot = Math.toRadians((-side * spec.armTilt)
                .toDouble())
            val sx = 50f + side * p.shoulderX
            val sy = p.shoulderY
            val hx = sx - (p.armLen * sin(rot)).toFloat()
            val hy = sy + (p.armLen * cos(rot)).toFloat()
            if (distSeg(pt, FPt(sx, sy), FPt(hx, hy)) <=
                p.armRad + 2f) return true
        }
        return false
    }

    private fun distSeg(pt: FPt, a: FPt, b: FPt): Float {
        val abx = b.x - a.x; val aby = b.y - a.y
        val l2 = abx * abx + aby * aby
        val t = if (l2 < 0.0001f) 0f else
            (((pt.x - a.x) * abx + (pt.y - a.y) * aby) / l2)
                .coerceIn(0f, 1f)
        val dx = pt.x - (a.x + abx * t)
        val dy = pt.y - (a.y + aby * t)
        return sqrt(dx * dx + dy * dy)
    }

    /** The fiction kit as cards, in WEARER-UNIT space (the
     *  wearer is 1.0 tall; sizes from ScaleContract's
     *  prop-relative table). A prop's silhouette is its
     *  rect; the pivot is its grip — the gate for props is
     *  the same as for limbs: grip inside the silhouette,
     *  or the prop floats off its carrier. */
    fun propCard(prop: String): PartCard {
        val rel = ScaleContract.PROP_RELATIVE[prop] ?: 0.5f
        val (made, region) = when (prop) {
            "lid_shield", "shield" -> "trash_can_lid" to
                PaperRegion.METAL
            "bedsheet_banner", "banner" -> "bedsheet" to
                PaperRegion.CLOTH
            "broom_lance", "lance" -> "broom" to
                PaperRegion.SURFACE
            "cardboard_wall", "crate" -> "refrigerator_box" to
                PaperRegion.SURFACE
            else -> "stick" to PaperRegion.SURFACE
        }
        // grip at 35% from the base for carried props, centre
        // for the shield; the silhouette rect is centred on x
        val gripY = if (prop == "lid_shield" ||
            prop == "shield") rel / 2f else rel * 0.35f
        return PartCard(prop, "hand.R", FPt(0f, gripY), 50f,
            0.8f, made, region)
    }

    /** Prop pivot gate: the grip sits inside the prop's own
     *  rect silhouette (half-width 0.06 wearer units for
     *  carried props, the shield's radius for the shield). */
    fun propPivotInside(prop: String): Boolean {
        val card = propCard(prop)
        val rel = ScaleContract.PROP_RELATIVE[prop] ?: 0.5f
        val halfW = if (prop == "lid_shield" ||
            prop == "shield") rel / 2f else 0.06f
        return abs(card.pivot.x) <= halfW &&
            card.pivot.y >= 0f && card.pivot.y <= rel
    }
}

// ---------------- the expressions manifest (spgen §5) ----------------

object FaceManifest {
    /** The grid: 8 emotion columns × 6 mouth rows. */
    val EMOTIONS: List<String> = listOf(
        "neutral", "happy", "angry", "scared",
        "smug", "sad", "shocked", "deadpan")
    val MOUTHS: List<String> = listOf(
        "closed", "small_o", "wide", "grin", "teeth", "frown")

    /** Canonical mouth rows: (mouthCurve, mouthOpen). The
     *  state machine picks the emotion column; lip-sync
     *  picks the mouth row — a manifest id is the pair. */
    val MOUTH_ROWS: Map<String, Pair<Float, Float>> = mapOf(
        "closed" to (0.05f to 0.0f),
        "small_o" to (0.1f to 0.35f),
        "wide" to (0.15f to 0.9f),
        "grin" to (0.85f to 0.3f),
        "teeth" to (0.7f to 0.55f),
        "frown" to (-0.75f to 0.1f))

    fun id(emotion: String, mouth: String): String =
        "${emotion}_${mouth}"

    /** All 48 manifest ids → (column, row). */
    val GRID: Map<String, Pair<Int, Int>> = run {
        val m = LinkedHashMap<String, Pair<Int, Int>>()
        for ((c, e) in EMOTIONS.withIndex())
            for ((r, mo) in MOUTHS.withIndex())
                m[id(e, mo)] = c to r
        m
    }

    /** Parse a manifest id back to (emotion, mouth), or
     *  null if it isn't one. Mouth names contain '_' —
     *  match the longest known mouth suffix first. */
    fun parse(faceId: String): Pair<String, String>? {
        for (mo in MOUTHS.sortedByDescending { it.length }) {
            if (faceId.endsWith("_$mo")) {
                val em = faceId.dropLast(mo.length + 1)
                if (em in EMOTIONS) return em to mo
            }
        }
        return null
    }

    /** Legacy emotion words → grid emotion columns. */
    private val EMOTION_OF: Map<String, String> = mapOf(
        "neutral" to "neutral", "calm" to "neutral",
        "curious" to "neutral", "hungry" to "neutral",
        "happy" to "happy", "pleased" to "happy",
        "excited" to "happy", "silly" to "happy",
        "playful" to "happy",
        "angry" to "angry", "stern" to "angry",
        "scared" to "scared", "worried" to "scared",
        "wary" to "scared",
        "smug" to "smug", "amused" to "smug",
        "proud" to "smug",
        "sad" to "sad",
        "shocked" to "shocked", "surprised" to "shocked",
        "startled" to "shocked", "alert" to "shocked",
        "deadpan" to "deadpan", "tired" to "deadpan",
        "resolute" to "deadpan")

    /** Which mouth row an existing expression's own numbers
     *  land on — the classifier the migration was built
     *  with, so every legacy face has a stated cell. */
    fun mouthOf(e: Expressions.Expr): String = when {
        e.mouthOpen >= 0.75f -> "wide"
        e.mouthOpen >= 0.45f && e.mouthCurve >= 0.4f -> "teeth"
        e.mouthCurve >= 0.6f -> "grin"
        // a hard frown outranks a merely-open mouth: the
        // angry face (curve -0.7, open 0.3) is a FROWN that
        // happens to be open, not a small_o (probe-verified
        // against the drawn strip, v0.17.0a)
        e.mouthCurve <= -0.5f -> "frown"
        e.mouthOpen >= 0.25f -> "small_o"
        else -> "closed"
    }

    /** A legacy expression id (any of the three sets) → its
     *  manifest id. Total: every legacy face lands on a
     *  cell; nothing resolves to a blank. */
    fun legacyToGrid(kind: FaceKind, exprId: String): String {
        val set = Expressions.setFor(kind)
        val e = set.firstOrNull { it.id == exprId }
            ?: Expressions.forMood(kind, exprId)
        val emotion = EMOTION_OF[e.id]
            ?: EMOTION_OF[exprId] ?: "neutral"
        return id(emotion, mouthOf(e))
    }

    /** Legacy ids that EXCEED the grid — state faces, not
     *  emotions. They still resolve to cells (legacyToGrid),
     *  but the manifest logs them as extensions so nobody
     *  mistakes a state for a column. The ANIMAL set is an
     *  extension atlas entire: its faces read through ears
     *  and muzzle, and it maps onto the grid per-id rather
     *  than pretending to be one. */
    val EXTENSIONS: Map<String, String> = mapOf(
        "talk" to "a STATE (speaking), not an emotion: the " +
            "emotion column is the speaker's current one and " +
            "lip-sync drives the mouth row",
        "hurt" to "a STATE (pain): eyes shut is carried by " +
            "the Expr lid, which the grid does not model",
        "sleepy" to "a STATE (drowsiness): lid-carried, " +
            "like hurt — no drowsiness column in the grid",
        "animal_set" to "the ANIMAL expression set is an " +
            "extension atlas: ear/antenna tilt carries what " +
            "brows carry for humans; its ids map per-id")

    /** Resolve a manifest id to a drawable expression in
     *  [kind]'s own set: the emotion column picks the base
     *  face (its eyes, brows, lids), the mouth row overrides
     *  the mouth — the doc's split, exactly. */
    fun exprFor(faceId: String, kind: FaceKind):
        Expressions.Expr {
        val (emotion, mouth) = parse(faceId)
            ?: return Expressions.forMood(kind, "idle")
        val set = Expressions.setFor(kind)
        val base = when (emotion) {
            "neutral" -> set.firstOrNull { it.id == "neutral" }
                ?: set.firstOrNull { it.id == "calm" }
                ?: set.first()
            "happy" -> Expressions.forMood(kind, "happy")
            "angry" -> Expressions.forMood(kind, "angry")
            "scared" -> Expressions.forMood(kind, "worried")
            "smug" -> when (kind) {
                FaceKind.ADULT ->
                    Expressions.forMood(kind, "amused")
                FaceKind.ANIMAL ->
                    Expressions.forMood(kind, "proud")
                else -> Expressions.forMood(kind, "proud")
            }
            "sad" -> Expressions.forMood(kind, "sad")
            "shocked" -> Expressions.forMood(kind, "surprised")
            "deadpan" -> {
                val n = set.firstOrNull { it.id == "neutral" }
                    ?: set.first()
                n.copy(lid = maxOf(n.lid, 0.45f))
            }
            else -> set.first()
        }
        val (curve, open) = MOUTH_ROWS[mouth] ?: (0.05f to 0f)
        return base.copy(id = faceId, mouthCurve = curve,
            mouthOpen = open, profMouthCurve = curve,
            profMouthOpen = open)
    }
}

/** Save compatibility for face identifiers: the resting-
 *  face strings a pre-redo save can hold (SheetSpec.face
 *  values and legacy expression ids) resolve to manifest
 *  ids through here — never a dead string, never a wipe. */
object FaceCompat {
    /** SheetSpec.face (the stored resting face) → manifest. */
    val STORED_FACE: Map<String, String> = mapOf(
        "normal" to "neutral_closed",
        "deadpan" to "deadpan_closed",
        "nervous" to "scared_small_o",
        "worried" to "scared_frown",
        "smug" to "smug_closed",
        "grin" to "happy_grin",
        // braces draw OVER the teeth row — a mouth
        // extension carried by the head painter, cell here
        "braces" to "happy_teeth",
        // lashes are eye furniture, not a face cell
        "lashes" to "neutral_closed")

    /** Resolve any stored face identifier — a SheetSpec.face
     *  value, a legacy expression id, a renderer mood, or a
     *  manifest id itself — to a manifest id. Total by
     *  construction: unknown strings land on neutral. */
    fun resolveStoredFace(stored: String?): String {
        if (stored == null) return "neutral_closed"
        if (FaceManifest.GRID.containsKey(stored)) return stored
        STORED_FACE[stored]?.let { return it }
        for (kind in FaceKind.values()) {
            val set = Expressions.setFor(kind)
            if (set.any { it.id == stored })
                return FaceManifest.legacyToGrid(kind, stored)
        }
        return FaceManifest.legacyToGrid(
            FaceKind.CHILD, stored)
    }
}

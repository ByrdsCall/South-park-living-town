package com.hunt3r.southparktown

// ============================================================
// CharacterSystem — the single data-driven character layer.
//
// STANDING ART RULE (Lucky, v0.1.6): every character HAS a
// reference sheet in the construction-sheet format — HEAD /
// TORSO / NORMAL ARMS / ARMS 2 / STAND / SIT / EYES OPEN /
// HURT EYES / SLEEP EYES / EYELIDS / mouth set / assembled
// figure — and the in-game figure FOLLOWS ITS SHEET. SheetSpec
// below IS that sheet, as data: fields are grouped by sheet
// slot, and KidArt renders exactly those slots (arms rotate
// from the sheet's shoulder anchors, SIT uses the SIT legs,
// sleeping uses SLEEP EYES, blinking uses the EYELIDS, talk
// uses the mouth set). The five supplied sheets (Kenny,
// Cartman, Craig, Stan, Kyle) and the worked Garrison sheet
// are ground truth for those six — their slot values in
// KidArt.SHEETS are transcribed from the sheets. Every other
// cast member's v0.1.4 signature design is formalized into
// the same slots; generated NPCs get a SheetSpec assembled by
// NpcGenerator, which is their sheet.
//
// SheetSpec lives here (pure Kotlin, no android.* imports) so
// the generator + JVM tests can build sheets anywhere.
// ============================================================

/**
 * Per-character proportions MEASURED from the character's supplied
 * reference image (v0.1.7 — Lucky: "Actually use the images to build
 * the characters" + "Everybody should have their own proportion").
 * Produced by tools/measure_specs.py (PIL silhouette + center-column
 * run analysis of the construction sheets / character models / the
 * adolescence chart); the raw output lives in tools/measurements.json.
 *
 *  - [headShare]: head ELLIPSE height / total figure height. The hat
 *    cap above the ellipse is drawn by the hat layer, not counted here.
 *  - [torsoShare] / [legShare]: garment split of the figure height;
 *    headShare + the hat cap + torsoShare + legShare = 1.
 *  - [headAspect]: head ellipse width / height (kids ~0.95-1.35,
 *    Garrison's measured 1.043 — near-round, NOT a balloon).
 *  - [headBodyWidth]: head silhouette width / torso silhouette width
 *    (arms included) — drives how wide the torso plan is built.
 *  - [heightRel]: this character's total on-screen height vs the
 *    10-12 baseline (kid = ~1.0, adult = ~1.38 x per-character
 *    factor from their measured head share vs their band's).
 *
 * Cast members WITHOUT a usable image in the supplied set carry a
 * design-derived set (individualized from their v0.1.6 design +
 * the chart's band ratios) — same slot, documented in the v0.1.7
 * report. Generated NPCs carry null and keep the band fallback.
 */
data class MeasuredProps(
    val headShare: Float,
    val torsoShare: Float,
    val legShare: Float,
    val headAspect: Float,
    val headBodyWidth: Float,
    val heightRel: Float
)

data class SheetSpec(
    // ---- HEAD slot: skin, hair, hat, ears, face furniture ----
    val skin: String = "#f5cfa8",
    val hairStyle: String = "bald",    // bald | buzz | mop | spiky | messy | sidepart | ponytail | long | afro | mohawk | bob | beehive | swirl | swoop | fringeflick | curls | fringe (bald dome, side fringe only — Garrison)
    val hairColor: String = "#6b4a2f",
    val hatId: String = "none",        // none | beanie | cap | trapper | cowboy | wizard | crown | yarmulke | beret | chefhat | trucker | pomhat | ushanka | chullo
    val hatColor: String = "#888888",
    val hatAccent: String = "#f2c230",
    val ears: Boolean = false,         // side ear ellipses on the sheet (Garrison)
    val beard: Boolean = false,
    val beardColor: String = "#3a2a1a",
    val beardStyle: String = "full",   // full | chinstrap | goatee (creator facial-hair set, v0.1.9d)
    val mustache: Boolean = false,
    val glasses: Boolean = false,
    val sunglasses: Boolean = false,
    val eyewear: String = "",          // "" = none (or the glasses/sunglasses flags) | goggles | star_shades (v0.1.9d)
    val freckles: Boolean = false,
    val scarfHex: String? = null,      // creator accessory: scarf band color at the neck (v0.1.9d)
    val wrinkles: Boolean = false,     // forehead wrinkle lines on the bald dome (Garrison sheet)
    val browHex: String? = null,       // eyebrow bar color (Garrison gray #8E8E8E); null = no drawn brows
    // ---- FACE slots: EYES OPEN/HURT/SLEEP/EYELIDS are states of the
    // shared eye geometry; `face` is the sheet's resting expression
    // (which mouth of the mouth set + pupil/lid personality shows) ----
    val face: String = "normal",       // normal|deadpan|nervous|worried|smug|grin|braces|lashes
    val lipstick: Boolean = false,
    val lipColor: String = "#c0392b",
    // ---- TORSO slot: shirt/coat shape + its drawn details ----
    val top: String = "#2a6fd4",
    val collarHex: String? = null,     // V collar band color (Stan red, Garrison green…)
    val placket: Boolean = false,      // center line down the torso (zipper / button line)
    val buttons: Boolean = false,      // buttons down the placket
    val beltHex: String? = null,       // belt buckle color at the waist (Garrison silver #C0C0C0)
    val pockets: Int = 0,              // flap pockets: 2 = kid jacket pair (Kyle), 1 = adult chest pocket (Garrison)
    val tie: Boolean = false,
    val tieColor: String = "#f0f0f0",
    val collar: Boolean = false,       // white priest collar (Father Maxi)
    val backpack: Boolean = false,
    val backStyle: String = "none",    // none | cape | wings — creator back items (v0.1.9d); backpack keeps its own flag
    val backColor: String = "#8a2a2a",
    // ---- ARMS slots: NORMAL ARMS (angled rest pose, tilt in degrees)
    // and ARMS 2 (straight down). Sleeves wear the TORSO color; the
    // hand at the arm's end wears handColor (mittens/gloves) or skin.
    // Arms rotate from FigureGeometry shoulder anchors — never float.
    val handColor: String? = null,
    val armTilt: Float = 12f,
    // ---- STAND slot: legs + shoes ----
    val bottom: String = "#3a5a8a",
    val bottomStyle: String = "pants", // pants | shorts | skirt
    val shoeColor: String = "#2a2a2a",
    val barefoot: Boolean = false,     // New Kid in underwear: bare feet, no shoes
    // ---- SIT slot is derived from STAND (seat band in `bottom`,
    // forward shoe ovals in `shoeColor`) — exactly as the sheets draw it.
    // ---- Proportions (the sheet's assembled-figure silhouette) ----
    val tall: Boolean = false,         // adult proportions
    val width: Float = 1f,             // silhouette X scale (Cartman wide, Mackey narrow)
    val headScale: Float = 1f,         // head radius scale — ONLY for unmeasured specs (avatar, generated NPCs); measured specs bake head size into MeasuredProps.headShare
    val measured: MeasuredProps? = null, // per-character measured proportions (v0.1.7); null = band/base geometry fallback
    val sig: String? = null,           // tokens: chin|coffee|twitch|stripe|chain|vest|sash|halo
    val special: String? = null        // kenny_parka | crutches | wheelchair | apron | towel | underwear_top
)

/**
 * FigureGeometry — the pure-math skeleton every sheet hangs on:
 * unit-space body plans (KID 100x128, ADULT 100x150) with the
 * torso silhouette and the SHOULDER / HIP anchor points the
 * limbs rotate from. KidArt draws from THESE numbers, and the
 * JVM suite proves the attachment invariant from them: at every
 * age band and pose, each limb capsule starts close enough to
 * its anchor that the capsule body always overlaps the torso —
 * no floating arms or legs, ever.
 */
object FigureGeometry {
    data class Plan(
        val boxH: Float,
        val torsoTop: Float, val torsoBottom: Float,
        val torsoHalfTop: Float, val torsoHalfBot: Float, // half-width at top/bottom edge
        val shoulderX: Float, val shoulderY: Float,      // anchor offset from center / y
        val hipX: Float, val hipY: Float,
        val armLen: Float, val armRad: Float, val handRad: Float,
        val legLen: Float, val legRad: Float, val ankleY: Float
    )

    // Kid torso path spans x 36..64 at the top (y~86) and 38..62 at
    // the bottom (y~106-110); adult spans 35..65 (y~76) to 37..63 (y~114).
    val KID = Plan(
        boxH = 128f, torsoTop = 86f, torsoBottom = 108f,
        torsoHalfTop = 14f, torsoHalfBot = 12f,
        shoulderX = 15.5f, shoulderY = 91f,
        hipX = 5f, hipY = 104f,
        // v0.1.8 sprite #64: hands are BIG MITTENS on the sheets —
        // handRad nearly the sleeve width, not a fingertip dot
        armLen = 12f, armRad = 5.5f, handRad = 6.5f,
        legLen = 13f, legRad = 3.5f, ankleY = 117f
    )
    val ADULT = Plan(
        boxH = 150f, torsoTop = 76f, torsoBottom = 116f,
        torsoHalfTop = 15f, torsoHalfBot = 13f,
        shoulderX = 16f, shoulderY = 86f,
        hipX = 5f, hipY = 110f,
        armLen = 17f, armRad = 6f, handRad = 7f,
        legLen = 27f, legRad = 3.5f, ankleY = 137f
    )

    fun plan(tall: Boolean): Plan = if (tall) ADULT else KID

    /** Torso half-width at unit height [y] (linear between the edges). */
    fun torsoHalfAt(p: Plan, y: Float): Float {
        val t = ((y - p.torsoTop) / (p.torsoBottom - p.torsoTop)).coerceIn(0f, 1f)
        return p.torsoHalfTop + (p.torsoHalfBot - p.torsoHalfTop) * t
    }

    /** How deep the arm capsule bites into the torso at the shoulder
     *  anchor: positive = attached (capsule crosses the torso edge).
     *  Rotation-invariant — the arm rotates AROUND the anchor. */
    fun shoulderBite(p: Plan): Float =
        torsoHalfAt(p, p.shoulderY) + p.armRad - p.shoulderX

    /** Hip anchors must sit inside the torso's bottom span so the
     *  leg capsule emerges from under the torso, never beside it. */
    fun hipInside(p: Plan): Boolean =
        p.hipX + p.legRad <= torsoHalfAt(p, p.hipY) + 2f &&
            p.hipY <= p.torsoBottom && p.hipY >= p.torsoBottom - 8f

    /** The full attachment invariant for one body plan. */
    fun limbsAttached(p: Plan): Boolean =
        shoulderBite(p) >= 2f && hipInside(p) &&
            p.armLen > p.armRad && p.legLen >= 8f

    /**
     * Fully derived render geometry for one spec: the body plan plus
     * the head ellipse and face anchors, all in unit space. KidArt
     * draws ONLY from this — spec -> derive -> draw, nothing hand-
     * placed in between (v0.1.7 fix for the playtest defects: spec
     * values were dropped or double-applied along the old path).
     * [hs] = head size relative to the base plan's head; decorative
     * hair/hat offsets scale by it so they stay glued to the head.
     */
    data class Derived(
        val plan: Plan,
        val hx: Float, val hy: Float, val hrx: Float, val hry: Float,
        val eyeL: Float, val eyeR: Float, val eyeT: Float, val eyeB: Float,
        val mouthY: Float,
        val hs: Float,
        val headAspect: Float,
        val silhouetteW: Float,
        val headShare: Float, val torsoShare: Float, val legShare: Float
    )

    // Figure span (head top .. shoe bottom) inside each unit box:
    // kid box 128: head top 3, shoes end 124. Adult box 150: 10..144.
    private fun spanOf(tall: Boolean) = if (tall) 134f else 121f
    private fun topOf(tall: Boolean) = if (tall) 10f else 3f

    /**
     * Derive the render geometry for [spec].
     *
     * UNMEASURED specs (avatar, generated NPCs, Towelie) reproduce the
     * v0.1.6 base geometry EXACTLY, with headScale applied as before
     * (chin-anchored uniform scale) — the age bands remain their
     * fallback, per Lucky's v0.1.7 ruling.
     *
     * MEASURED specs are built from their own MeasuredProps: the head
     * ellipse from headShare x headAspect (width clamped to the box,
     * bending height), the chin from the torso/leg split, torso plan
     * from the split, torso WIDTH from headBodyWidth (shoulder anchor
     * placed for a constant 3-unit bite, so limbsAttached holds by
     * construction at any width), legs from legShare. headScale is
     * ignored here — measured head size lives in headShare.
     */
    /** v0.16.0a (item 10): the SEATED leg, as geometry.
     *  A seated figure keeps BOTH leg segments: thigh from
     *  the hip anchor forward-down to a knee, shin from the
     *  knee down to the ankle at the plan's own ankle line.
     *  Segment lengths ride the plan's legLen, so an adult's
     *  long legs stay long — and present — while sitting.
     *  Returns (hip, knee, ankle) per side in unit space. */
    fun seatedLegs(p: Plan): List<Triple<Pair<Float, Float>,
            Pair<Float, Float>, Pair<Float, Float>>> =
        listOf(-1f, 1f).map { side ->
            val hip = (50f + side * p.hipX) to p.hipY
            val knee = (50f + side * p.torsoHalfBot * 0.92f) to
                (p.torsoBottom + p.legLen * 0.40f)
            val ankle = (50f + side * p.hipX * 1.55f) to p.ankleY
            Triple(hip, knee, ankle)
        }

    /** v0.16.0a (items 14+15): the side-profile anchors of
     *  a derived head, in unit space, for facing [f]
     *  (+1 = facing right). One eye, set toward the facing
     *  side; the mouth on the facing cheek; the nose point
     *  at the head's facing edge. KidArt's profile painter
     *  draws from these numbers only. */
    data class ProfileAnchors(
        val eyeX: Float, val eyeY: Float,
        val mouthX: Float, val mouthY: Float,
        val noseX: Float, val noseY: Float,
        val earX: Float
    )

    fun profileAnchors(d: Derived, f: Int): ProfileAnchors {
        val eyeY = (d.eyeT + d.eyeB) / 2f
        return ProfileAnchors(
            eyeX = d.hx + f * d.hrx * 0.34f,
            eyeY = eyeY,
            mouthX = d.hx + f * d.hrx * 0.52f,
            mouthY = d.mouthY,
            noseX = d.hx + f * d.hrx * 0.98f,
            noseY = eyeY + (d.mouthY - eyeY) * 0.45f,
            earX = d.hx - f * d.hrx * 0.30f
        )
    }

    fun derive(spec: SheetSpec): Derived {
        val base = plan(spec.tall)
        val tall = spec.tall
        val span = spanOf(tall)
        val top = topOf(tall)
        val feetY = top + span
        val m = spec.measured
        if (m == null) {
            val hr0 = if (tall) 34f else 42.5f
            val chin = if (tall) 78f else 88f
            val hry = hr0 * spec.headScale
            val hrx = hry
            val hy = chin - hry
            return Derived(
                plan = base, hx = 50f, hy = hy, hrx = hrx, hry = hry,
                eyeL = if (tall) 30f else 28f, eyeR = if (tall) 70f else 72f,
                eyeT = if (tall) 32f else 53f, eyeB = if (tall) 56f else 75f,
                mouthY = if (tall) 64f else 80f,
                hs = spec.headScale, headAspect = 1f,
                silhouetteW = 2f * (base.shoulderX + base.armRad),
                headShare = 2f * hry / span,
                torsoShare = (base.torsoBottom - base.torsoTop) / span,
                legShare = (feetY - base.torsoBottom + 2f) / span
            )
        }
        // ---- measured path: this character's own proportions ----
        var headH = m.headShare * span
        var headW = headH * m.headAspect
        if (headW > 96f) {           // never wider than the unit box
            headW = 96f
            headH = headW / m.headAspect
        }
        val hrx = headW / 2f
        val hry = headH / 2f
        val chinY = top + (1f - m.torsoShare - m.legShare) * span
        val hy = chinY - hry
        val torsoTop = chinY - 2f
        val torsoBot = torsoTop + m.torsoShare * span
        val ankleY = feetY - 7f
        var hipY = torsoBot - if (tall) 6f else 4f
        var legLen = ankleY - hipY
        if (legLen < 4f) {           // Cartman: pants are a strip; the
            legLen = 4f              // shoe ovals carry the leg read
            hipY = ankleY - legLen
        }
        // torso width from the measured head:body ratio. The plan's
        // silhouette at torso rows is the torso edge + arms; the jacket
        // itself is ~88% (kid) / ~72% (adult) of that silhouette.
        val targetSil = headW / m.headBodyWidth
        val halfF = if (tall) 0.36f else 0.44f
        val halfTop = (targetSil * halfF).coerceIn(
            base.torsoHalfTop * 0.72f, base.torsoHalfTop * 1.9f)
        val halfBot = halfTop * (base.torsoHalfBot / base.torsoHalfTop)
        val shoulderY = torsoTop + (base.shoulderY - base.torsoTop)
        val halfAtShoulder = halfTop + (halfBot - halfTop) *
            ((shoulderY - torsoTop) / (torsoBot - torsoTop)).coerceIn(0f, 1f)
        // shoulder anchor placed for a constant 3-unit bite at ANY
        // torso width — the attachment invariant by construction
        val shoulderX = halfAtShoulder + base.armRad - 3f
        val hipX = (base.hipX * (halfBot / base.torsoHalfBot)).coerceIn(
            2.5f, maxOf(2.5f, halfBot - base.legRad - 0.5f))
        val armLen = ((hipY - shoulderY) *
            (base.armLen / (base.hipY - base.shoulderY)))
            .coerceIn(base.armLen * 0.75f, base.armLen * 1.6f)
        val plan = base.copy(
            torsoTop = torsoTop, torsoBottom = torsoBot,
            torsoHalfTop = halfTop, torsoHalfBot = halfBot,
            shoulderX = shoulderX, shoulderY = shoulderY,
            hipX = hipX, hipY = hipY,
            armLen = armLen, legLen = legLen, ankleY = ankleY
        )
        // face anchors ride the ellipse: eyes/mouth at the base
        // geometry's fractions of (hrx, hry) — they scale WITH the
        // head instead of staying behind on a scaled-up balloon
        val eyeDx = hrx * if (tall) 20f / 34f else 22f / 42.5f
        val eyeT = hy + hry * if (tall) -0.3529f else 0.1765f
        val eyeB = hy + hry * if (tall) 0.3529f else 0.6941f
        val mouthY = hy + hry * if (tall) 0.5882f else 0.8118f
        return Derived(
            plan = plan, hx = 50f, hy = hy, hrx = hrx, hry = hry,
            eyeL = 50f - eyeDx, eyeR = 50f + eyeDx,
            eyeT = eyeT, eyeB = eyeB, mouthY = mouthY,
            hs = hry / if (tall) 34f else 42.5f,
            headAspect = m.headAspect,
            silhouetteW = 2f * (shoulderX + base.armRad),
            headShare = headH / span,
            torsoShare = (torsoBot - torsoTop) / span,
            legShare = (feetY - torsoBot + 2f) / span
        )
    }
}

// ============================================================
// v0.5.0f: ink shapes + HeadStack — the head stack (behind-hair,
// head, hair, hat) as PURE DATA. Lucky's playtest verdict was
// that hair floated off domes, skin slivered out at hairlines,
// and parts read pasted-on. The fix is construction, not
// patching: every cap and dome below is built on an ellipse
// PADDED past the head ellipse (hair +2 units, hats +3.5), so
// the hair mass provably contains the dome sector it covers
// and a hat provably hugs the head it sits on. KidArt paints
// EXACTLY these shapes; the JVM suite rasterizes them with
// the point tests — the measurement IS the drawing.
// ============================================================

/** A point in figure unit space. */
data class FPt(val x: Float, val y: Float)

/** A filled shape in figure unit space that answers point tests. */
sealed interface InkShape {
    fun contains(x: Float, y: Float): Boolean
}

/** Ellipse, optionally rotated (the beret's tilt). */
data class EllipseInk(
    val cx: Float, val cy: Float, val rx: Float, val ry: Float,
    val rotDeg: Float = 0f
) : InkShape {
    override fun contains(x: Float, y: Float): Boolean {
        var dx = x - cx
        var dy = y - cy
        if (rotDeg != 0f) {
            val a = Math.toRadians(-rotDeg.toDouble())
            val cos = Math.cos(a).toFloat()
            val sin = Math.sin(a).toFloat()
            val nx = dx * cos - dy * sin
            val ny = dx * sin + dy * cos
            dx = nx; dy = ny
        }
        if (rx <= 0f || ry <= 0f) return false
        val qx = dx / rx
        val qy = dy / ry
        return qx * qx + qy * qy <= 1f
    }
}

/** Rounded rectangle (rad 0 = plain rect). */
data class RoundRectInk(
    val l: Float, val t: Float, val r: Float, val b: Float, val rad: Float
) : InkShape {
    override fun contains(x: Float, y: Float): Boolean {
        if (x < l || x > r || y < t || y > b) return false
        val radC = minOf(rad, (r - l) / 2f, (b - t) / 2f)
        if (radC <= 0f) return true
        val cx = when {
            x < l + radC -> l + radC
            x > r - radC -> r - radC
            else -> return true
        }
        val cy = when {
            y < t + radC -> t + radC
            y > b - radC -> b - radC
            else -> return true
        }
        val dx = x - cx
        val dy = y - cy
        return dx * dx + dy * dy <= radC * radC
    }
}

/** Simple polygon, ray-cast point test. */
data class PolyInk(val pts: List<FPt>) : InkShape {
    override fun contains(x: Float, y: Float): Boolean {
        if (pts.size < 3) return false
        var inside = false
        var j = pts.size - 1
        for (i in pts.indices) {
            val xi = pts[i].x; val yi = pts[i].y
            val xj = pts[j].x; val yj = pts[j].y
            if ((yi > y) != (yj > y)) {
                val xInt = xi + (y - yi) * (xj - xi) / (yj - yi)
                if (x < xInt) inside = !inside
            }
            j = i
        }
        return inside
    }
}

/** Which pass of the head stack a layer belongs to — KidArt
 *  paints kinds at the same slots the old hand-drawn layers
 *  occupied (behind-hair in the figure builders, head/hair/hat
 *  inside drawHead). */
enum class InkKind { BEHIND_HAIR, HEAD, HAIR, HAT }

/** One filled layer of the head stack. [role] names its job
 *  (cap / lock / extra / dome / band / flap / brim / trim /
 *  tuft / fringe / wig / head / behind) — the under-hat rules
 *  filter on it. */
data class InkLayer(
    val shape: InkShape, val color: String, val kind: InkKind,
    val role: String = "", val alpha: Int = 255
)

object HeadStack {
    /** Hair rides this far proud of the head ellipse; hats ride
     *  prouder still, over the hair. The pads are the whole
     *  seamless guarantee: a cap built on the padded ellipse
     *  contains the head's dome sector by construction. */
    const val HAIR_PAD = 2f
    const val HAT_PAD = 3.5f

    /** Hats whose dome reaches the hairline all the way around.
     *  Under one of these, hair survives only as the under-brim
     *  fringe strip, the temple tufts, and whatever length hangs
     *  below the brim (locks). Perch hats — beret, yarmulke,
     *  earmuffs, headband, cowboy, wizard, crown, chef's toque,
     *  taco — sit ON the hair, which keeps its full mass. */
    private val COVERING_HATS = setOf(
        "beanie", "cap", "mr_hat_cap", "trucker", "trapper", "pomhat",
        "ushanka", "chullo", "bandana", "mall_rat_cap", "santa_hat",
        "sleuth_cap")

    fun hatCoversHair(hatId: String): Boolean = hatId in COVERING_HATS

    /** Styles whose sheet shows a forehead fringe — the fringe
     *  survives under a covering hat as a strip below the brim. */
    private val FRINGE_STYLES = setOf(
        "mop", "messy", "shaggy", "swoop", "sidepart", "swirl",
        "curtains", "bowl", "fringeflick")

    fun hasFringe(style: String): Boolean = style in FRINGE_STYLES

    private val cache = HashMap<SheetSpec, List<InkLayer>>()

    /** The full head stack for [spec], in paint order within
     *  each kind. Memoized — a spec's stack never changes. */
    fun layers(spec: SheetSpec): List<InkLayer> =
        cache.getOrPut(spec) { build(spec) }

    /** The topmost head-stack color at unit point (x, y), or
     *  null for bare background. (The torso and face details
     *  are not head stack; this answers for hair/hat/head/
     *  behind-hair only — exactly what the suite's hair and
     *  hat pins ask about.) */
    fun colorAt(spec: SheetSpec, x: Float, y: Float): String? {
        var col: String? = null
        for (layer in layers(spec)) {
            if (layer.shape.contains(x, y)) col = layer.color
        }
        return col
    }

    /** The y of a covering hat's brim bottom at the forehead —
     *  where hat ends and under-brim hair may begin. */
    fun brimBottomCenter(hatId: String, d: FigureGeometry.Derived): Float =
        when (hatId) {
            "beanie", "pomhat" -> d.eyeT + 2f
            "cap", "mr_hat_cap", "mall_rat_cap", "trucker", "santa_hat" -> d.eyeT - 1f
            "sleuth_cap" -> d.eyeT - 2f
            "bandana" -> d.eyeT - 3f
            else -> d.eyeT
        }

    // ---------------- shape construction ----------------

    /** Point on an ellipse at angle [deg] (0 = right, 90 =
     *  below center, 270 = crown — y grows downward). */
    private fun ePt(cx: Float, cy: Float, rx: Float, ry: Float, deg: Float): FPt {
        val a = Math.toRadians(deg.toDouble())
        return FPt(cx + rx * Math.cos(a).toFloat(), cy + ry * Math.sin(a).toFloat())
    }

    /** The top arc of an ellipse: sampled from the left point
     *  at height [sideY], over the crown, to the right point at
     *  the same height. */
    private fun topArc(cx: Float, cy: Float, rx: Float, ry: Float,
                       sideY: Float, steps: Int = 26): List<FPt> {
        val k = ((sideY - cy) / ry).coerceIn(-1f, 1f)
        val asin = Math.asin(k.toDouble())
        val start = Math.PI - asin
        val end = 2 * Math.PI + asin
        return (0..steps).map { i ->
            val a = start + (end - start) * i / steps
            FPt(cx + rx * Math.cos(a).toFloat(), cy + ry * Math.sin(a).toFloat())
        }
    }

    /** A cap/dome polygon: the top arc of the ellipse (rx, ry)
     *  centered on the head, closed by a bottom edge sampled
     *  right-to-left from [edge] (x -> y). Because the arc rides
     *  the PADDED ellipse, the polygon contains every head point
     *  above the edge line — no slivers, no floating. */
    private fun capPoly(d: FigureGeometry.Derived, rx: Float, ry: Float,
                        sideY: Float, edge: (Float) -> Float): PolyInk =
        capPolyC(d.hx, d.hy, rx, ry, sideY, edge)

    private fun capPolyC(cx: Float, cy: Float, rx: Float, ry: Float,
                         sideY: Float, edge: (Float) -> Float): PolyInk {
        val arc = topArc(cx, cy, rx, ry, sideY)
        val xL = arc.first().x
        val xR = arc.last().x
        val m = 16
        val bottom = (m downTo 0).map { i ->
            val x = xL + (xR - xL) * i / m
            FPt(x, edge(x))
        }
        return PolyInk(arc + bottom)
    }

    /** Triangle wave in [0, 1] — the mop fringe's scallops. */
    private fun tri(x: Float, wavelength: Float): Float {
        val u = ((x / wavelength) % 1f + 1f) % 1f
        return if (u < 0.5f) u * 2f else 2f - u * 2f
    }

    /** Deterministic bedhead scallops: how far the hairline
     *  RISES inside each [wavelength]-wide segment (half-sine),
     *  heights cycling through [table]. */
    private fun bump(x: Float, table: List<Float>, wavelength: Float): Float {
        val segF = Math.floor((x / wavelength).toDouble()).toInt()
        val u = (x / wavelength) - segF
        val h = table[((segF % table.size) + table.size) % table.size]
        return h * Math.sin(Math.PI * u).toFloat()
    }

    private val MESSY_BUMPS = listOf(1f, 5f, 2f, 6f, 3f, 7f, 2f, 5f)
    private val SHAGGY_BUMPS = listOf(2f, 6f, 3f, 7f, 4f, 6f, 3f, 5f)

    /** The bottom-edge (hairline) curve of a cap style, as a
     *  function of x. Shared by the cap itself and by the
     *  under-brim fringe strip, so the strip is the cap's own
     *  fringe, clipped by the hat. */
    private fun hairlineFn(style: String, d: FigureGeometry.Derived):
            (Float) -> Float {
        val f = d.eyeT - 6f
        val top = d.hy - d.hry
        val rx = d.hrx + HAIR_PAD
        fun t(x: Float) = (x - d.hx) / rx
        return when (style) {
            "buzz" -> {
                val sideY = top + d.hry * 0.40f
                ({ x: Float -> sideY - 5f * (1f - t(x) * t(x)) })
            }
            "mop" -> ({ x: Float -> f - 2f + 4.5f * tri(x, 17f) })
            "messy" -> ({ x: Float -> f - 2f - bump(x, MESSY_BUMPS, 15f) })
            "shaggy" -> ({ x: Float -> f + 2f - bump(x, SHAGGY_BUMPS, 16f) })
            "spiky" -> ({ _: Float -> f + 2f })
            "sidepart" -> ({ x: Float -> f + 2f + 2.5f * t(x) })
            "swirl" -> {
                val bow = (f - top) * 0.42f
                ({ x: Float -> f + 5f - bow * (1f - t(x) * t(x)) })
            }
            "swoop" -> ({ x: Float ->
                val u = (t(x) + 1f) / 2f
                f - 4f + 18f * u - 4f * Math.sin(Math.PI * u).toFloat()
            })
            "fringeflick" -> ({ _: Float -> f + 1f })
            "curtains" -> ({ _: Float -> f + 8f })
            "bowl" -> ({ x: Float -> f + 4f - 2f * (1f - t(x) * t(x)) })
            "flattop" -> {
                val mid = (f + top) / 2f + 7.5f
                ({ x: Float -> f + 6f - ((f + 6f) - mid) * (1f - t(x) * t(x)) })
            }
            else -> ({ x: Float -> f + 5f - 3f * (1f - t(x) * t(x)) })
        }
    }

    /** The side height at which a cap style's arc springs from
     *  the padded ellipse. */
    private fun capSideY(style: String, d: FigureGeometry.Derived): Float {
        val f = d.eyeT - 6f
        val top = d.hy - d.hry
        return when (style) {
            "buzz" -> top + d.hry * 0.40f
            "messy" -> f - 2f
            "shaggy" -> f + 2f
            "swirl" -> f + 2f
            "swoop" -> f + 6f
            "sidepart" -> f + 2f
            "bowl" -> f + 4f
            "flattop" -> f + 2f
            "long" -> f - 2f
            else -> f
        }
    }

    // ---------------- the stack ----------------

    private fun build(spec: SheetSpec): List<InkLayer> {
        if (spec.special == "towel") return emptyList()
        val d = FigureGeometry.derive(spec)
        val out = mutableListOf<InkLayer>()
        out += behindLayers(spec, d)
        if (spec.special == "kenny_parka") {
            // the hood IS the head: an oval hugging the head
            // ellipse (+4), the dark ring on the derived eye box
            out += InkLayer(
                EllipseInk(d.hx, d.hy - 2f, d.hrx + 4f, d.hry + 4f),
                spec.top, InkKind.HEAD, "head")
            out += InkLayer(
                RoundRectInk(d.eyeL - 3f, d.eyeT - 4f, d.eyeR + 3f, d.eyeB + 4f, 10f),
                "#460905", InkKind.HEAD, "head")
            return out
        }
        out += InkLayer(EllipseInk(d.hx, d.hy, d.hrx, d.hry),
            spec.skin, InkKind.HEAD, "head")
        val wig = spec.hatId == "lorde_wig_opt"
        if (!wig) out += hairLayers(spec, d)
        out += hatLayers(spec, d, wig)
        return out
    }

    /** Behind-head hair: afro halo, curl manes, long panels.
     *  Painted before the body, so long panels fall BEHIND the
     *  shoulders — they end below the torso's top edge, tucked
     *  under the torso the figure builder draws next. */
    private fun behindLayers(spec: SheetSpec, d: FigureGeometry.Derived):
            List<InkLayer> {
        val hair = spec.hairColor
        val hx = d.hx; val hy = d.hy; val hrx = d.hrx; val hry = d.hry
        return when (spec.hairStyle) {
            "afro" -> listOf(InkLayer(
                EllipseInk(hx, hy - 6f, hrx + 8f, hry + 8f),
                hair, InkKind.BEHIND_HAIR, "behind"))
            "curls" -> {
                val layers = mutableListOf<InkLayer>()
                layers += InkLayer(
                    RoundRectInk(hx - hrx - 4f, hy - hry * 0.32f,
                        hx + hrx + 4f, hy + hry * 0.99f, 12f),
                    hair, InkKind.BEHIND_HAIR, "behind")
                var by = hy - hry * 0.05f
                while (by < hy + hry * 0.9f) {
                    layers += InkLayer(
                        EllipseInk(hx - hrx - 3f, by, 6.5f * d.hs, 6.5f * d.hs),
                        hair, InkKind.BEHIND_HAIR, "behind")
                    layers += InkLayer(
                        EllipseInk(hx + hrx + 3f, by, 6.5f * d.hs, 6.5f * d.hs),
                        hair, InkKind.BEHIND_HAIR, "behind")
                    by += hry * 0.24f
                }
                layers
            }
            "long" -> {
                val bottom = maxOf(hy + hry * 0.98f, d.plan.torsoTop + 14f)
                listOf(
                    InkLayer(
                        capPolyC(hx, hy, hrx + HAT_PAD, hry + HAT_PAD,
                            hy - 4f) { hy - 4f },
                        hair, InkKind.BEHIND_HAIR, "behind"),
                    InkLayer(
                        RoundRectInk(hx - hrx - 1.5f, hy - hry * 0.19f,
                            hx - hrx * 0.74f, bottom, 7f),
                        hair, InkKind.BEHIND_HAIR, "behind"),
                    InkLayer(
                        RoundRectInk(hx + hrx * 0.74f, hy - hry * 0.19f,
                            hx + hrx + 1.5f, bottom, 7f),
                        hair, InkKind.BEHIND_HAIR, "behind"))
            }
            else -> emptyList()
        }
    }

    /** Front hair. Under a covering hat the cap mass is
     *  replaced by the under-brim set (fringe strip + temple
     *  tufts + below-brim length); perch hats and no hat keep
     *  the full head of hair. */
    private fun hairLayers(spec: SheetSpec, d: FigureGeometry.Derived):
            List<InkLayer> {
        val st = spec.hairStyle
        if (st == "bald") return emptyList()
        val hair = spec.hairColor
        val acc = spec.hatAccent
        val hx = d.hx; val hy = d.hy; val hrx = d.hrx; val hry = d.hry
        val top = hy - hry
        val f = d.eyeT - 6f
        val rx = hrx + HAIR_PAD
        val ry = hry + HAIR_PAD
        val layers = mutableListOf<InkLayer>()
        fun add(shape: InkShape, role: String, color: String = hair,
                kind: InkKind = InkKind.HAIR) {
            layers += InkLayer(shape, color, kind, role)
        }
        fun cap(role: String = "cap") {
            add(capPoly(d, rx, ry, capSideY(st, d), hairlineFn(st, d)), role)
        }
        when (st) {
            "buzz", "mop", "sidepart", "swirl", "swoop", "bowl" -> cap()
            "messy", "shaggy" -> {
                cap()
                // bedhead tufts standing off the crown arc
                for (aDeg in listOf(228f, 262f, 298f)) {
                    val b1 = ePt(hx, hy, hrx - 1f, hry - 1f, aDeg - 8f)
                    val tip = ePt(hx, hy, rx + 10f, ry + 10f, aDeg)
                    val b2 = ePt(hx, hy, hrx - 1f, hry - 1f, aDeg + 8f)
                    add(PolyInk(listOf(b1, tip, b2)), "cap")
                }
                if (st == "shaggy") {
                    add(RoundRectInk(hx - hrx - 1f, hy - hry * 0.3f,
                        hx - hrx * 0.8f, hy + hry * 0.55f, 6f), "lock")
                    add(RoundRectInk(hx + hrx * 0.8f, hy - hry * 0.3f,
                        hx + hrx + 1f, hy + hry * 0.55f, 6f), "lock")
                }
            }
            "spiky" -> {
                cap()
                // seven bold spikes rooted on the padded ellipse
                val step = (333f - 207f) / 7f
                for (i in 0 until 7) {
                    val a0 = 207f + step * i
                    val a1 = a0 + step * 0.55f
                    val a2 = a0 + step
                    add(PolyInk(listOf(
                        ePt(hx, hy, hrx - 1f, hry - 1f, a0),
                        ePt(hx, hy, rx + 15f, ry + 15f, a1),
                        ePt(hx, hy, hrx - 1f, hry - 1f, a2))), "cap")
                }
            }
            "fringeflick" -> {
                // dyed tips first — the cap covers their roots
                var tx = hx - hrx + 6f
                while (tx < hx + hrx - 8f) {
                    add(PolyInk(listOf(
                        FPt(tx, f - 2f), FPt(tx + 5f, f + 10f),
                        FPt(tx + 10f, f - 2f))), "extra", "#b03040")
                    tx += 11f
                }
                cap()
            }
            "curtains" -> {
                for (sign in listOf(-1f, 1f)) {
                    val sideY = f + 12f
                    val k = ((sideY - hy) / ry).coerceIn(-1f, 1f)
                    val asinDeg = Math.toDegrees(Math.asin(k.toDouble())).toFloat()
                    val endAng = if (sign < 0) 180f - asinDeg else asinDeg
                    val pts = mutableListOf<FPt>()
                    val steps = 12
                    for (i in 0..steps) {
                        val ang = if (sign < 0)
                            270f - (270f - endAng) * i / steps
                        else
                            270f + (endAng + 360f - 270f) * i / steps
                        pts += ePt(hx, hy, rx, ry, ang)
                    }
                    val sideX = pts.last().x
                    val partY = top + 6f
                    for (i in 1..6) {
                        val u = i / 6f
                        pts += FPt(
                            sideX + (hx + sign * 1f - sideX) * u,
                            sideY + (partY - sideY) *
                                Math.pow(u.toDouble(), 1.3).toFloat())
                    }
                    add(PolyInk(pts), "cap")
                }
            }
            "ponytail", "bun", "pigtails", "braid" -> {
                cap()
                when (st) {
                    "ponytail" -> {
                        add(PolyInk(listOf(
                            FPt(hx + hrx - 8f, top + 10f),
                            FPt(hx + hrx + 14f, hy + 6f),
                            FPt(hx + hrx + 8f, hy + 30f),
                            FPt(hx + hrx + 2f, hy + 15f),
                            FPt(hx + hrx - 13f, top + 17f))), "lock")
                        add(EllipseInk(hx + hrx - 6f, top + 12f, 4f, 4f),
                            "lock", acc)
                    }
                    "bun" -> {
                        add(EllipseInk(hx, top - 4f, 10f * d.hs, 10f * d.hs), "cap")
                        add(EllipseInk(hx, top - 4f, 3.5f * d.hs, 3.5f * d.hs),
                            "cap", acc)
                    }
                    "pigtails" -> {
                        val cyO = (top + 8f + hy + hry * 0.52f) / 2f
                        val ryO = (hy + hry * 0.52f - top - 8f) / 2f
                        for (sd in listOf(-1f, 1f)) {
                            add(EllipseInk(hx + sd * hrx, cyO, 8f, ryO), "lock")
                            add(EllipseInk(hx + sd * (hrx - 1f), top + 9f,
                                3.5f * d.hs, 3.5f * d.hs), "lock", acc)
                        }
                    }
                    "braid" -> {
                        var by = top + 12f
                        var flip = false
                        while (by < hy + hry * 0.62f) {
                            add(EllipseInk(
                                hx + hrx - 3f + if (flip) 2.5f else -2.5f,
                                by, 6f * d.hs, 6f * d.hs), "cap")
                            by += hry * 0.17f
                            flip = !flip
                        }
                        add(EllipseInk(hx + hrx - 3f, by, 3f * d.hs, 3f * d.hs),
                            "cap", acc)
                    }
                }
            }
            "flattop" -> {
                val arc = topArc(hx, hy, rx, ry, capSideY(st, d))
                    .map { p -> FPt(p.x, maxOf(p.y, top + 1f)) }
                val xL = arc.first().x
                val xR = arc.last().x
                val fn = hairlineFn(st, d)
                val m = 16
                val bottom = (m downTo 0).map { i ->
                    val x = xL + (xR - xL) * i / m
                    FPt(x, fn(x))
                }
                add(PolyInk(arc + bottom), "cap")
            }
            "mohawk" -> add(PolyInk(listOf(
                FPt(hx - 11f, top + 4f), FPt(hx - 9f, top - 19f),
                FPt(hx - 2f, top - 23f), FPt(hx + 5f, top - 22f),
                FPt(hx + 8f, top - 18f), FPt(hx + 11f, top + 4f))), "cap")
            "beehive" -> {
                cap()
                add(EllipseInk(hx, top - hry * 0.4f + 7f,
                    hrx * 0.52f, hry * 0.4f + 7f), "cap")
            }
            "bob", "lorde_wig_opt" ->
                layers += bobFrame(d, hair, InkKind.HAIR)
            "long" -> {
                cap()
                add(RoundRectInk(hx - hrx - 1f, hy - hry * 0.55f,
                    hx - hrx * 0.72f, hy + hry * 0.96f, 7f), "lock")
                add(RoundRectInk(hx + hrx * 0.72f, hy - hry * 0.55f,
                    hx + hrx + 1f, hy + hry * 0.96f, 7f), "lock")
            }
            "curls" -> {
                cap()
                var bx = hx - hrx + 4f
                while (bx < hx + hrx - 4f) {
                    add(EllipseInk(bx, f - 2f, 6.5f * d.hs, 6.5f * d.hs), "extra")
                    bx += 13.5f
                }
            }
            "afro" ->
                add(capPolyC(hx, hy - 6f, hrx + 8f, hry + 8f, f + 2f) { f + 2f },
                    "cap")
            "fringe" -> {
                // Garrison: the dome stays skin — hair survives as
                // fringe bands hugging the head's sides, temple to jaw
                add(RoundRectInk(hx - hrx - 1.5f, hy - hry * 0.52f,
                    hx - hrx * 0.60f, hy + hry * 0.55f, 6f), "lock")
                add(RoundRectInk(hx + hrx * 0.60f, hy - hry * 0.52f,
                    hx + hrx + 1.5f, hy + hry * 0.55f, 6f), "lock")
            }
            else -> cap()
        }
        if (!hatCoversHair(spec.hatId)) return layers
        // ---- under a covering hat ----
        val kept = layers.filter { it.role == "lock" || it.role == "extra" }
        val under = kept.toMutableList()
        // temple tufts: a wedge of hair below the brim at each
        // side — flaps (ushanka/chullo/trapper) paint over them
        val yT = brimBottomCenter(spec.hatId, d)
        for (sd in listOf(-1f, 1f)) {
            under += InkLayer(PolyInk(listOf(
                FPt(hx + sd * hrx * 0.84f, yT - 1f),
                FPt(hx + sd * (hrx + 1.5f), yT + 2f),
                FPt(hx + sd * (hrx + 0.5f), minOf(yT + 9f, d.eyeB + 4f)),
                FPt(hx + sd * hrx * 0.80f, yT + 5f))),
                hair, InkKind.HAIR, "tuft")
        }
        // the fringe strip: the cap's own hairline, clipped by
        // the brim — only where the hairline actually dips
        // below the brim line somewhere
        if (st in FRINGE_STYLES) {
            val fn = hairlineFn(st, d)
            val brimY = yT - 1f
            val k = ((brimY - hy) / ry).coerceIn(-1f, 1f)
            val wB = rx * Math.sqrt(1.0 - k * k).toFloat()
            val m = 14
            val bottom = (m downTo 0).map { i ->
                val x = hx - wB + 2f * wB * i / m
                FPt(x, fn(x).coerceIn(brimY + 0.5f, d.eyeT + 3f))
            }
            val maxBottom = bottom.maxOf { it.y }
            if (maxBottom > brimY + 1.5f) {
                val topEdge = (0..m).map { i ->
                    FPt(hx - wB + 2f * wB * i / m, brimY)
                }
                under += InkLayer(PolyInk(topEdge + bottom),
                    hair, InkKind.HAIR, "fringe")
            }
        }
        return under
    }

    /** The bob/wig frame: a padded-ellipse arc over the crown,
     *  closed by an inner edge that rises to the hairline —
     *  hair (or a wig) framing the face to the chin. */
    private fun bobFrame(d: FigureGeometry.Derived, color: String,
                         kind: InkKind): InkLayer {
        val hx = d.hx; val hy = d.hy; val hrx = d.hrx; val hry = d.hry
        val f = d.eyeT - 6f
        val arc = topArc(hx, hy, hrx + 2.5f, hry + 2.5f, hy + 21f)
        val inner = listOf(
            FPt(hx + hrx - 6f, hy + 21f), FPt(hx + hrx - 9f, hy + 2f),
            FPt(hx + hrx - 11f, f), FPt(hx, f + 2f),
            FPt(hx - hrx + 11f, f), FPt(hx - hrx + 9f, hy + 2f),
            FPt(hx - hrx + 6f, hy + 21f))
        return InkLayer(PolyInk(arc + inner), color, kind, "lock")
    }

    // ---------------- hats ----------------

    private fun hatLayers(spec: SheetSpec, d: FigureGeometry.Derived,
                          wig: Boolean): List<InkLayer> {
        if (wig) return listOf(bobFrame(d, spec.hatColor, InkKind.HAT)
            .copy(role = "wig"))
        val hat = spec.hatColor
        val acc = spec.hatAccent
        val hx = d.hx; val hy = d.hy; val hrx = d.hrx; val hry = d.hry
        val top = hy - hry
        val eyeT = d.eyeT; val eyeB = d.eyeB
        val rx = hrx + HAT_PAD
        val ry = hry + HAT_PAD
        val layers = mutableListOf<InkLayer>()
        fun add(shape: InkShape, role: String, color: String = hat,
                alpha: Int = 255) {
            layers += InkLayer(shape, color, InkKind.HAT, role, alpha)
        }
        fun dome(sideY: Float) {
            add(capPoly(d, rx, ry, sideY) { sideY }, "dome")
        }
        when (spec.hatId) {
            "beanie" -> {
                dome(eyeT)
                add(RoundRectInk(hx - hrx - 1.5f, eyeT - 10f,
                    hx + hrx + 1.5f, eyeT + 2f, 4f), "band")
                add(EllipseInk(hx, top - 5f, 8f, 8f), "trim", acc)
            }
            "pomhat" -> {
                dome(eyeT)
                add(RoundRectInk(hx - hrx - 1.5f, eyeT - 9f,
                    hx + hrx + 1.5f, eyeT + 2f, 4f), "band", acc)
                add(EllipseInk(hx, top - 5f, 8f, 8f), "trim", acc)
            }
            "nightcap" -> {
                // v0.12.0d: the floppy nightcap — a soft cone
                // off the crown, tipping sideways, ONE bobble
                // at the tip and ONE band at the brow in the
                // accent. Solids only: no pattern, no repeat.
                // A perch, not a covering hat: the hair keeps
                // its full mass underneath (see COVERING_HATS).
                add(PolyInk(listOf(
                    FPt(hx - hrx * 0.66f, top + hry * 0.22f),
                    FPt(hx - hrx * 0.34f, top - hry * 0.40f),
                    FPt(hx + hrx * 0.30f, top - hry * 0.46f),
                    FPt(hx + hrx * 1.04f, top - hry * 0.06f),
                    FPt(hx + hrx * 0.62f, top + hry * 0.22f))), "dome")
                add(RoundRectInk(hx - hrx * 0.68f, top + hry * 0.14f,
                    hx + hrx * 0.68f, top + hry * 0.34f, 4f), "band", acc)
                add(EllipseInk(hx + hrx * 1.04f, top - hry * 0.06f,
                    5f * d.hs, 5f * d.hs), "trim", acc)
            }
            "cap", "mr_hat_cap", "mall_rat_cap", "trucker", "sleuth_cap" -> {
                dome(if (spec.hatId == "sleuth_cap") eyeT - 3f else eyeT - 4f)
                if (spec.hatId == "sleuth_cap") {
                    add(RoundRectInk(hx - hrx * 1.10f, eyeT - 7f,
                        hx + hrx * 1.10f, eyeT - 2f, 3f), "brim")
                    add(RoundRectInk(hx - hrx + 4f, eyeT - 9f,
                        hx + hrx - 4f, eyeT - 6f, 0f), "band", acc)
                } else {
                    add(RoundRectInk(hx - hrx - 1f, eyeT - 8f,
                        hx + hrx + 1f, eyeT - 1f, 4f), "brim")
                }
                when (spec.hatId) {
                    "cap" -> add(RoundRectInk(hx - hrx + 4f, eyeT - 6f,
                        hx + hrx - 4f, eyeT - 3f, 0f), "trim", acc)
                    "mall_rat_cap" -> add(
                        EllipseInk(hx, top + 1f, 4.5f * d.hs, 4.5f * d.hs),
                        "trim", acc)
                    "trucker" -> add(
                        EllipseInk(hx, (top + 2f + eyeT - 8f) / 2f,
                            hrx * 0.33f, ((eyeT - 8f) - (top + 2f)) / 2f),
                        "trim", "#f0f0f0", 200)
                }
            }
            "trapper" -> {
                dome(eyeT)
                add(RoundRectInk(hx - hrx - 2.5f, eyeT - 6f,
                    hx - hrx + 12f, eyeB + 6f, 5f), "flap")
                add(RoundRectInk(hx + hrx - 12f, eyeT - 6f,
                    hx + hrx + 2.5f, eyeB + 6f, 5f), "flap")
                add(EllipseInk(hx, top - 5f, 8f, 8f), "trim", acc)
            }
            "ushanka" -> {
                // squared dome: the padded arc with its crown
                // flattened at the sheet's flat-top line
                val arc = topArc(hx, hy, rx, ry, eyeT - 2f)
                    .map { p -> FPt(p.x, maxOf(p.y, top - 8f)) }
                val xL = arc.first().x
                val xR = arc.last().x
                val m = 8
                val bottom = (m downTo 0).map { i ->
                    FPt(xL + (xR - xL) * i / m, eyeT - 2f)
                }
                add(PolyInk(arc + bottom), "dome")
                add(RoundRectInk(hx - hrx - 1.5f, eyeT - 10f,
                    hx + hrx + 1.5f, eyeT, 4f), "band", darkerHex(hat))
                add(RoundRectInk(hx - hrx - 3.5f, eyeT - 4f,
                    hx - hrx + 13f, eyeB + 12f, 6f), "flap")
                add(RoundRectInk(hx + hrx - 13f, eyeT - 4f,
                    hx + hrx + 3.5f, eyeB + 12f, 6f), "flap")
            }
            "chullo" -> {
                dome(eyeT)
                add(RoundRectInk(hx - hrx - 2.5f, eyeT - 4f,
                    hx - hrx + 12f, eyeB + 4f, 5f), "flap")
                add(RoundRectInk(hx + hrx - 12f, eyeT - 4f,
                    hx + hrx + 2.5f, eyeB + 4f, 5f), "flap")
                add(EllipseInk(hx, top - 5f, 8f, 8f), "trim", acc)
                add(EllipseInk(hx - hrx + 3f, eyeB + 16f, 3f, 3f), "trim", acc)
                add(EllipseInk(hx + hrx - 3f, eyeB + 16f, 3f, 3f), "trim", acc)
            }
            "beret" -> {
                add(EllipseInk(hx - hrx * 0.045f, top - hry * 0.025f,
                    hrx * 0.755f, hry * 0.215f, -12f), "dome")
                val cos = 0.978f; val sin = -0.208f
                val dx = -0.42f * hrx; val dy = -0.14f * hry
                add(EllipseInk(hx + dx * cos - dy * sin,
                    top + dx * sin + dy * cos, 3.5f * d.hs, 3.5f * d.hs, -12f),
                    "trim")
            }
            "yarmulke" -> add(
                EllipseInk(hx, top + hry * 0.03f, hrx * 0.30f, hry * 0.13f),
                "dome")
            "chefhat" -> {
                add(RoundRectInk(hx - hrx * 0.55f, top - hry * 0.71f,
                    hx + hrx * 0.55f, top + hry * 0.14f, 10f),
                    "dome", "#f4f4f4")
                add(RoundRectInk(hx - hrx * 0.6f, top + hry * 0.05f,
                    hx + hrx * 0.6f, top + hry * 0.24f, 0f),
                    "band", "#f4f4f4")
            }
            "cowboy", "rhinestone_cowboy" -> {
                add(EllipseInk(hx, top + hry * 0.11f,
                    hrx * 1.19f, hry * 0.13f), "brim")
                add(PolyInk(listOf(
                    FPt(hx - hrx * 0.42f, top + 2f),
                    FPt(hx - hrx * 0.40f, top - hry * 0.30f),
                    FPt(hx - hrx * 0.30f, top - hry * 0.52f),
                    FPt(hx, top - hry * 0.56f),
                    FPt(hx + hrx * 0.30f, top - hry * 0.52f),
                    FPt(hx + hrx * 0.40f, top - hry * 0.30f),
                    FPt(hx + hrx * 0.42f, top + 2f))), "dome")
                if (spec.hatId == "cowboy") {
                    add(RoundRectInk(hx - hrx * 0.42f, top - hry * 0.19f,
                        hx + hrx * 0.42f, top - hry * 0.07f, 0f), "band", acc)
                } else {
                    add(RoundRectInk(hx - hrx * 0.42f, top - hry * 0.19f,
                        hx + hrx * 0.42f, top - hry * 0.07f, 0f),
                        "band", darkerHex(hat))
                    for (gx in listOf(-0.24f, 0f, 0.24f)) {
                        add(EllipseInk(hx + hrx * gx, top - hry * 0.13f,
                            2.2f * d.hs, 2.2f * d.hs), "trim", acc)
                    }
                }
            }
            "wizard" -> {
                add(EllipseInk(hx, top + hry * 0.085f,
                    hrx * 1.1f, hry * 0.105f), "brim")
                add(PolyInk(listOf(
                    FPt(hx - hrx * 0.47f, top + 2f),
                    FPt(hx - hrx * 0.18f, top - hry * 1.0f),
                    FPt(hx + hrx * 0.14f, top - hry * 1.06f),
                    FPt(hx + hrx * 0.30f, top - hry * 0.78f),
                    FPt(hx + hrx * 0.47f, top + 2f))), "dome")
                add(EllipseInk(hx + hrx * 0.30f, top - hry * 0.78f,
                    4f * d.hs, 4f * d.hs), "trim", acc)
            }
            "crown" -> {
                val yb = top + 4f
                val cw = hrx * 0.56f
                add(PolyInk(listOf(
                    FPt(hx - cw, yb), FPt(hx - cw, yb - 14f * d.hs),
                    FPt(hx - cw * 0.5f, yb - 6f * d.hs),
                    FPt(hx, yb - 18f * d.hs),
                    FPt(hx + cw * 0.5f, yb - 6f * d.hs),
                    FPt(hx + cw, yb - 14f * d.hs), FPt(hx + cw, yb))),
                    "dome", acc)
                add(RoundRectInk(hx - cw, top + 4f, hx + cw, top + 10f, 0f),
                    "band", acc)
            }
            "earmuffs" -> {
                for (sd in listOf(-1f, 1f)) {
                    add(EllipseInk(hx + sd * (hrx + 0.5f), hy + 2f,
                        7.5f * d.hs, 7.5f * d.hs), "dome")
                    add(EllipseInk(hx + sd * (hrx + 0.5f), hy + 2f,
                        3.8f * d.hs, 3.8f * d.hs), "trim", acc)
                }
            }
            "headband" -> add(
                RoundRectInk(hx - hrx * 0.99f, eyeT - 13f,
                    hx + hrx * 0.99f, eyeT - 5f, 3.5f), "band")
            "bandana" -> {
                dome(eyeT - 3f)
                add(EllipseInk(hx + hrx - 3f, eyeT - 1f,
                    4.5f * d.hs, 4.5f * d.hs), "trim")
                add(PolyInk(listOf(
                    FPt(hx + hrx - 6f, eyeT + 1f),
                    FPt(hx + hrx + 3f, eyeT + 13f),
                    FPt(hx + hrx - 1f, eyeT + 14f),
                    FPt(hx + hrx - 3f, eyeT + 3f))), "trim")
            }
            "taco_hat" -> {
                val pts = mutableListOf<FPt>()
                val x0 = hx - hrx * 0.62f
                val x1 = hx + hrx * 0.62f
                val yB = top + hry * 0.10f
                for (i in 0..10) {
                    val u = i / 10f
                    val qx = (1 - u) * (1 - u) * x0 + 2 * (1 - u) * u * hx +
                        u * u * x1
                    val qy = (1 - u) * (1 - u) * yB +
                        2 * (1 - u) * u * (top - hry * 0.78f) + u * u * yB
                    pts += FPt(qx, qy)
                }
                add(PolyInk(pts), "dome")
                var lx = hx - hrx * 0.55f
                while (lx < hx + hrx * 0.55f) {
                    add(EllipseInk(lx, yB, 3.2f * d.hs, 3.2f * d.hs),
                        "trim", "#5aa843")
                    lx += hrx * 0.22f
                }
                add(EllipseInk(hx - hrx * 0.22f, top - hry * 0.28f,
                    2.6f * d.hs, 2.6f * d.hs), "trim", "#c0392b")
                add(EllipseInk(hx + hrx * 0.20f, top - hry * 0.36f,
                    2.6f * d.hs, 2.6f * d.hs), "trim", "#c0392b")
            }
            "santa_hat" -> {
                add(PolyInk(listOf(
                    FPt(hx - hrx * 0.92f, eyeT - 6f),
                    FPt(hx - hrx * 0.75f, top - hry * 0.35f),
                    FPt(hx - hrx * 0.2f, top - hry * 0.60f),
                    FPt(hx + hrx * 0.1f, top - hry * 0.62f),
                    FPt(hx + hrx * 0.55f, top - hry * 0.55f),
                    FPt(hx + hrx * 0.95f, top + hry * 0.05f),
                    FPt(hx + hrx * 0.92f, eyeT - 6f))), "dome")
                add(RoundRectInk(hx - hrx - 1f, eyeT - 11f,
                    hx + hrx + 1f, eyeT - 1f, 4f), "band", "#f4f4f4")
                add(EllipseInk(hx + hrx * 0.95f, top + hry * 0.10f,
                    5.5f * d.hs, 5.5f * d.hs), "trim", "#f4f4f4")
            }
        }
        return layers
    }
}

enum class SizeClass { CHILD, ADULT }
enum class CharKind { CAST, GENERATED }

/**
 * Age-band proportion ladder, modeled on Lucky's adolescence
 * proportion reference (bands 10-12 / 13-14 / 15-16 / 17-18 /
 * 19-20): heads stay near-constant in absolute size while bodies
 * grow taller and slimmer, so the head SHRINKS relative to the
 * body with age. heightScale = on-screen height vs a 10-year-old;
 * headFactor multiplies the spec's head radius; adultGeo switches
 * KidArt to the adult body plan (17-18 reads adult-shaped but
 * slimmer, with a relatively bigger head than a full adult).
 * Resulting head share of height: ~66% / 64% / 60% / 50% / 45%.
 */
enum class AgeBand(
    val heightScale: Float,
    val headFactor: Float,
    val adultGeo: Boolean,
    val widthFactor: Float
) {
    KID(1.00f, 1.00f, false, 1.00f),        // 10-12: big head, stub body
    YOUNG_TEEN(1.12f, 0.97f, false, 0.97f), // 13-14
    TEEN(1.22f, 0.90f, false, 0.94f),       // 15-16
    OLDER_TEEN(1.31f, 1.12f, true, 0.94f),  // 17-18: adult shape, bigger head
    ADULT(1.38f, 1.00f, true, 1.00f)        // 19+: full adult proportions
}

data class CharacterDef(
    val name: String,
    val kind: CharKind,
    val ageBand: AgeBand,
    val home: String?,
    val loreName: String?
) {
    val sizeClass: SizeClass
        get() = if (ageBand == AgeBand.ADULT || ageBand == AgeBand.OLDER_TEEN)
            SizeClass.ADULT else SizeClass.CHILD
    val isAdult: Boolean get() = sizeClass == SizeClass.ADULT
}

object CharacterSystem {

    /** The 32 kids of the cast (big-head proportions; +6 in v0.3.0b wave 2,
     *  +5 in v0.7.0a wave 3, +3 in v0.8.0a wave 4, +2 in v0.9.0a wave 5). */
    val CAST_KIDS: Set<String> = setOf(
        "Cartman", "Stan", "Kyle", "Kenny", "Butters",
        "Wendy", "Craig", "Tweek", "Clyde", "Jimmy", "Timmy", "Tolkien", "Bebe",
        "Michael", "Pete", "Henrietta",
        "Heidi", "Nichole", "Red", "Kevin", "Francis", "DogPoo",
        "Karen", "Scott", "Firkle", "Annie", "Bradley",
        "Ike", "Kevin McCormick", "Shelley",
        "Nathan", "Sophie",
        // wave 6 (v0.11.0d): the evidence audit's kids
        "Pip", "Dougie", "Jason", "Terrance Mephesto", "Jenny",
        "Flora", "Filmore", "Quaid", "Mimsy", "Tricia"
    )

    /** The 38 adults of the cast (taller, adult proportions; +6 in v0.3.0b
     *  wave 2, +5 in v0.7.0a wave 3, +5 in v0.8.0a wave 4, +6 in v0.9.0a
     *  wave 5). */
    val CAST_ADULTS: Set<String> = setOf(
        "Randy", "Sharon", "Gerald", "Sheila", "Liane", "Stuart", "Carol",
        "Stephen", "Linda", "Garrison", "Mackey", "Chef", "Jesus", "Jimbo",
        "Father Maxi", "Towelie",
        "McDaniels", "Barbrady", "Crabtree", "Mr. Tweek", "Mrs. Tweek", "Ned",
        "Skeeter", "Victoria", "Marvin", "Mr. Tucker", "Steve",
        "Laura", "Mephesto", "Yates", "Adler", "Lu Kim",
        "Roger", "Ryan", "Sarah", "Alexis", "Mr. Testaburger", "Big Gay Al",
        // wave 6 (v0.11.0d): the evidence audit's grown-ups
        "Mr. Slave", "Linda Black", "Harriet", "Mr. Biggle", "Mr. Turner",
        "Nancy", "Mr. Stevens", "Mrs. Stevens", "Mr. Stoley", "Mrs. Stoley",
        "Clark", "Maggie", "Mitch", "Bob Johnson", "Pusslicker", "Dr. Doctor",
        "Choksondik", "Strong Woman"
    )

    /** Cast age bands by actual age: the fourth-graders are KID;
     *  Michael (the oldest goth kid) reads YOUNG_TEEN, as do the
     *  wave-4 teens Shelley and Kevin McCormick; grown-ups ADULT. */
    val CAST_BANDS: Map<String, AgeBand> = run {
        val m = mutableMapOf<String, AgeBand>()
        for (n in CAST_KIDS) m[n] = AgeBand.KID
        for (n in CAST_ADULTS) m[n] = AgeBand.ADULT
        m["Michael"] = AgeBand.YOUNG_TEEN
        m["Shelley"] = AgeBand.YOUNG_TEEN
        m["Kevin McCormick"] = AgeBand.YOUNG_TEEN
        m
    }

    /** The one registry for the defined cast. Built from existing data only. */
    val CAST: Map<String, CharacterDef> = run {
        val m = linkedMapOf<String, CharacterDef>()
        for (name in KID_ORDER) {
            m[name] = CharacterDef(
                name = name,
                kind = CharKind.CAST,
                ageBand = CAST_BANDS[name] ?: AgeBand.ADULT,
                home = KID_HOUSE[name],
                loreName = LORE_CHAR_ALIASES[name]
            )
        }
        m
    }

    fun def(name: String): CharacterDef? = CAST[name]

    fun bandOf(name: String): AgeBand = CAST[name]?.ageBand ?: AgeBand.ADULT

    fun sizeClass(name: String): SizeClass =
        CAST[name]?.sizeClass ?: SizeClass.ADULT

    fun isAdult(name: String): Boolean = sizeClass(name) == SizeClass.ADULT

    /** On-screen height multiplier from the character's age band. */
    fun heightScale(name: String): Float = bandOf(name).heightScale

    fun heightScale(band: AgeBand): Float = band.heightScale

    /**
     * Apply the age-band ladder to a spec WITHOUT touching the stored
     * design: geometry plan (kid/adult), head radius factor and
     * silhouette slimming only. FALLBACK ONLY (v0.1.7): generated NPCs
     * have no image to measure, so they ride the band ladder (with
     * per-seed size jitter from NpcGen). CAST specs are never band-
     * adjusted — each cast member renders from their own measured
     * proportions in their SheetSpec.
     */
    fun bandAdjust(spec: SheetSpec, band: AgeBand): SheetSpec {
        if (band == AgeBand.KID && !spec.tall) return spec
        if (band == AgeBand.ADULT && spec.tall) return spec
        return spec.copy(
            tall = band.adultGeo,
            headScale = spec.headScale * band.headFactor,
            width = spec.width * band.widthFactor
        )
    }

    /** Location kinds that are buildings with interiors: NPCs there are INSIDE. */
    val BUILDING_KINDS: Set<String> = setOf("house", "shop", "civic", "school")

    fun isBuilding(locId: String): Boolean =
        LOCATIONS[locId]?.kind in BUILDING_KINDS

    /** Is an NPC at [locId] indoors right now? (Schedules target locations;
     *  inside/outside derives from the location — single source of truth.)
     *  v0.3.0d exception: YOUR house is the one building nobody is
     *  ever inside but you — an NPC standing at YourHouse is on
     *  the DOORSTEP (present while you're outside, gone the moment
     *  you step in). Visitors knock; they never wander in. */
    fun npcInside(locId: String): Boolean =
        locId != YOUR_HOUSE && isBuilding(locId)

    /** Halloween costumes (moved here from GameView, unchanged mapping). */
    fun costumeFor(name: String, holiday: Holiday?): String? {
        if (holiday?.id != "halloween") return null
        return when (name) {
            "Butters" -> "ghost"
            "Kenny" -> "vampire"
            "Cartman" -> "witch"
            "Stan", "Kyle" -> "mask"
            else -> null
        }
    }

    /** Neat-freaks never litter; slobs litter at triple rate (trash system). */
    val NEAT_NPCS: Set<String> = setOf("Kyle", "Wendy", "Mackey", "Sheila", "Linda")
    val SLOB_NPCS: Set<String> = setOf("Cartman", "Randy", "Towelie", "Stuart")

    fun litterRate(name: String): Double = when (name) {
        in NEAT_NPCS -> 0.0
        in SLOB_NPCS -> 0.03
        else -> 0.01
    }
}

/** A location's interior definition (all 27 locations are enterable). */
data class InteriorDef(
    val locId: String,
    val name: String,
    val theme: String, // home|classroom|cafe|shop|restaurant|bar|theater|clinic|bank|office|church|mall|bus|pavilion|hut|barn|loft|diner
    val wallHex: String,
    val floorHex: String,
    val props: List<String>
)

val INTERIORS: Map<String, InteriorDef> = linkedMapOf(
    "KennysHouse" to InteriorDef("KennysHouse", "Inside the McCormick House", "home",
        "#8a8578", "#6b5a44", listOf("sofa", "tv", "table", "lamp", "crate")),
    "ButtersHouse" to InteriorDef("ButtersHouse", "Inside the Stotch Residence", "home",
        "#a8c8e8", "#ac8658", listOf("sofa", "tv", "table", "plant", "lamp")),
    "CartmanHouse" to InteriorDef("CartmanHouse", "Inside Cartman's House", "home",
        "#d9b380", "#75512f", listOf("sofa", "tv", "table", "shelf", "lamp")),
    "StanHouse" to InteriorDef("StanHouse", "Inside the Marsh Residence", "home",
        "#64906b", "#8a6a4a", listOf("sofa", "tv", "table", "plant", "shelf")),
    "KyleHouse" to InteriorDef("KyleHouse", "Inside the Broflovski Residence", "home",
        "#8a9a7b", "#7c5540", listOf("sofa", "table", "shelf", "plant", "lamp")),
    "YourHouse" to InteriorDef("YourHouse", "Inside Your House", "home",
        "#7d5a70", "#4e3a44", listOf("sofa", "tv", "table", "desk", "lamp")),
    "TolkienHouse" to InteriorDef("TolkienHouse", "Inside Tolkien's House", "home",
        "#b5654a", "#4e3226", listOf("sofa", "tv", "table", "shelf", "plant")),
    "BusStop" to InteriorDef("BusStop", "Inside the School Bus", "bus",
        "#e8c53a", "#4c4c50", listOf("busseats", "busseats", "wheel", "sign")),
    "MainStreet" to InteriorDef("MainStreet", "Main Street Diner", "diner",
        "#c8b8a0", "#c9b391", listOf("counter", "booth", "booth", "menu", "plant")),
    "TweekBros" to InteriorDef("TweekBros", "Inside Tweek Bros. Coffeehouse", "cafe",
        "#7a5230", "#5c4030", listOf("counter", "espresso", "table", "table", "menu")),
    "JimbosGuns" to InteriorDef("JimbosGuns", "Inside Jimbo's Guns", "shop",
        "#c86a2a", "#5f5a52", listOf("counter", "shelf", "shelf", "sign", "crate")),
    "CityWok" to InteriorDef("CityWok", "Inside City Wok", "restaurant",
        "#c0392b", "#7a3a2a", listOf("counter", "table", "table", "lantern", "menu")),
    "SkeetersBar" to InteriorDef("SkeetersBar", "Inside Skeeter's Bar", "bar",
        "#5f3d27", "#33241a", listOf("barcounter", "stool", "stool", "dartboard", "tv")),
    "Bijou" to InteriorDef("Bijou", "Inside the Bijou Theatre", "theater",
        "#4a3a6a", "#33254d", listOf("screen", "seats", "seats", "popcorn", "sign")),
    "TomsRhino" to InteriorDef("TomsRhino", "Inside Tom's Rhinoplasty", "clinic",
        "#e8a0b0", "#c8c8c8", listOf("examtable", "counter", "plant", "poster", "shelf")),
    "Bank" to InteriorDef("Bank", "Inside the Bank of South Park", "bank",
        "#9a9a9a", "#757575", listOf("counter", "vault", "rope", "plant", "sign")),
    "PostOffice" to InteriorDef("PostOffice", "Inside the Post Office", "office",
        "#7a8a9a", "#75875f", listOf("counter", "mailslots", "shelf", "crate", "sign")),
    "FreemansTacos" to InteriorDef("FreemansTacos", "Inside Freeman's Tacos", "restaurant",
        "#e8932a", "#94522c", listOf("counter", "table", "booth", "menu", "plant")),
    "Raisins" to InteriorDef("Raisins", "Inside Raisins", "restaurant",
        "#e87a8a", "#8a4a3a", listOf("booth", "booth", "barcounter", "tv", "menu")),
    "Church" to InteriorDef("Church", "Inside South Park Church", "church",
        "#f0ead8", "#9c7c4e", listOf("altar", "pew", "pew", "candles", "window")),
    "CityHall" to InteriorDef("CityHall", "Inside City Hall", "office",
        "#c8b88a", "#6f7263", listOf("desk", "flag", "podium", "plant", "shelf")),
    "Elementary" to InteriorDef("Elementary", "Inside South Park Elementary", "classroom",
        "#bfa06a", "#8a7a5a", listOf("blackboard", "desk", "desk", "flag", "shelf")),
    "KidsPark" to InteriorDef("KidsPark", "The Park Pavilion", "pavilion",
        "#7fb069", "#9a8a6a", listOf("picnic", "picnic", "plant", "sign", "crate")),
    "StarksPond" to InteriorDef("StarksPond", "The Ice-Fishing Hut", "hut",
        "#7a6a55", "#5a7a9a", listOf("stove", "icehole", "bench", "crate", "lantern")),
    "Mall" to InteriorDef("Mall", "Inside South Park Mall", "mall",
        "#e0cfae", "#a89880", listOf("fountain", "bench", "shelf", "sign", "plant")),
    "SoDoSoPa" to InteriorDef("SoDoSoPa", "A SoDoSoPa Loft", "loft",
        "#c86a9a", "#b59a6b", listOf("sofa", "table", "plant", "shelf", "tv")),
    "TegridyFarms" to InteriorDef("TegridyFarms", "Inside the Tegridy Barn", "barn",
        "#7aa84a", "#83682f", listOf("hay", "crate", "crate", "plant", "lantern"))
)

/**
 * v0.2.0a — interior parity registry (pass #2, the inside pass):
 * the signature set-dressing InteriorArt draws for each room on
 * top of the shared prop vocabulary — the specific furniture,
 * wall dressing and objects that make the room THAT room, from
 * lore/show knowledge, all drawn new in the flat style. Each
 * token appears as a `// idetail: <token>` marker in
 * InteriorArt.kt; the smoke suite cross-checks this registry
 * against the art source (same discipline as LOC_ART_DETAILS),
 * and every token belongs to exactly ONE interior — a signature,
 * never a shared template.
 */
val INTERIOR_ART_DETAILS: Map<String, List<String>> = linkedMapOf(
    "KennysHouse" to listOf("parka-row", "bare-bulb", "cardboard-floor"),
    "ButtersHouse" to listOf("chore-chart", "photo-frames", "corner-chair"),
    "CartmanHouse" to listOf("plush-frog", "snack-bags", "litter-box"),
    "StanHouse" to listOf("guitar", "maple-poster", "dog-bed"),
    "KyleHouse" to listOf("menorah-shelf", "crayon-drawing", "toy-blocks"),
    "YourHouse" to listOf("band-poster", "cork-board", "moving-boxes"),
    "TolkienHouse" to listOf("gold-frames", "chandelier", "piano"),
    "BusStop" to listOf("mirror-strip", "route-board", "bus-aisle"),
    "MainStreet" to listOf("wall-clock", "pie-case", "jukebox"),
    "TweekBros" to listOf("profit-chart", "cup-shelf", "coffee-sacks"),
    "JimbosGuns" to listOf("rifle-rack", "deer-mount", "hunting-sign"),
    "CityWok" to listOf("lantern-string", "city-sign", "wok-station"),
    "SkeetersBar" to listOf("bar-neon", "beer-taps", "pool-table"),
    "Bijou" to listOf("screen-curtains", "lobby-posters", "ticket-booth"),
    "TomsRhino" to listOf("nose-chart", "hoff-poster", "waiting-chairs"),
    "Bank" to listOf("teller-bars", "gone-plaque", "carpet-runner"),
    "PostOffice" to listOf("wanted-poster", "parcel-scale", "mail-sacks"),
    "FreemansTacos" to listOf("taco-menu-boards", "pepper-string", "mic-booth"),
    "Raisins" to listOf("round-sign", "name-plates", "wings-sign"),
    "Church" to listOf("rose-window", "hymn-board", "organ-pipes", "collection-stand"),
    "CityHall" to listOf("mayor-portrait", "minutes-board", "seal-rug"),
    "Elementary" to listOf("hall-of-honor", "cubby-row", "globe"),
    "KidsPark" to listOf("pavilion-banner", "notice-board", "lost-found-box"),
    "StarksPond" to listOf("fish-mount", "woodpile", "tipup-flags"),
    "Mall" to listOf("sale-windows", "directory-board", "kiosk"),
    "SoDoSoPa" to listOf("brick-band", "craft-sign", "vinyl-crates"),
    "TegridyFarms" to listOf("tegridy-banner", "jar-shelf", "feed-sacks")
)

/**
 * v0.5.0g — the interior FOREGROUND registry (the scene
 * depth pass): the near furniture each room THEME puts
 * between the crowd and the camera — the back of the sofa
 * at home, the counter front in a cafe, the pew backs at
 * church — drawn by InteriorArt.drawForegroundLayer over
 * the figures' feet, in the bottom strip only. One token
 * per theme, each marked `// iforeground: <token>` in
 * InteriorArt.kt; its KIND lives in
 * Depth.INTERIOR_FOREGROUND_KINDS, its BAND in
 * Depth.TOKEN_BANDS.
 */
val INTERIOR_FOREGROUND: Map<String, String> = linkedMapOf(
    "home" to "sofa-back-row",
    "loft" to "loft-sofa-back",
    "classroom" to "desk-front-row",
    "office" to "office-desk-front",
    "cafe" to "cafe-counter-front",
    "restaurant" to "restaurant-counter-front",
    "diner" to "diner-counter-front",
    "bar" to "bar-front",
    "clinic" to "clinic-counter-front",
    "bank" to "bank-counter-front",
    "shop" to "shop-shelf-edge",
    "mall" to "mall-shelf-edge",
    "church" to "pew-backs",
    "theater" to "theater-seat-backs",
    "bus" to "bus-seat-backs",
    "pavilion" to "pavilion-rail",
    "hut" to "hut-table-edge",
    "barn" to "barn-fence-rail"
)

/**
 * v0.2.0a — interior signage: the drawn lettering the detail
 * layer puts on signs, boards and plaques inside (menus, route
 * boards, neon, name plates). Maps a detail token to the exact
 * headline text InteriorArt draws for it; the smoke suite checks
 * the literal is in the art source, so signage can't silently
 * lose its words.
 */
val INTERIOR_SIGN_TEXTS: Map<String, String> = mapOf(
    "route-board" to "SCHOOL ROUTE 4",
    "profit-chart" to "3: PROFIT",
    "coffee-sacks" to "COFFEE",
    "hunting-sign" to "IT'S COMING RIGHT FOR US!",
    "city-sign" to "CITY WOK",
    "bar-neon" to "SKEETER'S",
    "ticket-booth" to "TICKETS",
    "hoff-poster" to "THE HOFF",
    "gone-plaque" to "AND... IT'S GONE",
    "wanted-poster" to "WANTED",
    "mail-sacks" to "MAIL",
    "taco-menu-boards" to "TACOS",
    "mic-booth" to "ON AIR",
    "round-sign" to "RAISINS",
    "name-plates" to "LEXUS",
    "wings-sign" to "WINGS",
    "hymn-board" to "HYMN 42",
    "minutes-board" to "TOWN MEETING",
    "hall-of-honor" to "HALL OF HONOR",
    "pavilion-banner" to "KID'S PARK",
    "lost-found-box" to "LOST & FOUND",
    "sale-windows" to "SALE",
    "directory-board" to "YOU ARE HERE",
    "craft-sign" to "CRAFT",
    "tegridy-banner" to "TEGRIDY FARMS"
)

fun interiorOf(locId: String): InteriorDef? = INTERIORS[locId]

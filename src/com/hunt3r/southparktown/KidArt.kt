package com.hunt3r.southparktown

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

// ============================================================
// KidArt — every figure in the game drawn in ONE consistent style:
// the blank New Kid. Giant round head (~60% of height for kids),
// NO neck, huge white oval eyes touching at center with dot pupils,
// tiny stub body, nub arms, mitten hands. Flat colors, no outlines
// on fills, no gradients, no shading. All art redrawn originals.
// v0.1.4 signature pass: every character carries an unmistakable
// silhouette, hat shape, hair, resting face and/or prop, so the cast
// reads at a glance WITHOUT name tags. Proportions stay New Kid.
// ============================================================

// SheetSpec lives in CharacterSystem.kt (v0.1.5) so the pure-Kotlin
// character layer + NPC generator can build specs without android imports.
// The SHEETS below — the v0.1.4 signature designs — are unchanged.

/**
 * Procedural animation state for one figure on one frame. The
 * simulation (GameView) fills it in from the character's CURRENT
 * ACTIVITY — walking to a new spot, standing idle, speaking,
 * sitting, sleeping — and KidArt turns it into motion with pure
 * math. Nothing here is random decoration: [walking]/[walkPhase]
 * advance only while the figure actually moves.
 */
data class FigureAnim(
    val timeMs: Long = 0,
    val seed: Int = 0,
    val walking: Boolean = false,
    val walkPhase: Float = 0f,
    /** v0.1.9a #16: windy day — Kenny's hood strings flap. */
    val wind: Boolean = false,
    /** v0.16.0a (items 14+18): side profile — 0 = front,
     *  -1 = profile facing left, +1 = profile facing
     *  right. A walking figure wears its profile; a
     *  standing one faces front. */
    val facing: Int = 0,
    /** v0.16.0a (items 16+17): the ActionAnims id this
     *  figure is playing, if any — drives arm pose and
     *  body dip over the base cycle. */
    val action: String = "",
    /** v0.16.0a (item 16): this character's own stride
     *  amplitude as a multiple of the base swing — from
     *  MotionRegistry, so the walk cycle itself is
     *  per-character, not one cycle recolored. */
    val motionAmp: Float = 1f
)

/** Head/body geometry is FigureGeometry.Derived (v0.1.7): every
 *  figure — cast from its measured SheetSpec.measured, avatar and
 *  generated NPCs from the base plans — derives its plan + head
 *  ellipse + face anchors through FigureGeometry.derive, and the
 *  draw code below consumes ONLY that. `hr` in the old code is now
 *  split honestly: hry = vertical head radius (heights), hrx =
 *  horizontal head radius (widths) — the old single-radius circle
 *  is what made heads balloon. */
private typealias Geo = FigureGeometry.Derived

object KidArt {

    /**
     * Derived per-frame animation values — ALL procedural math (trig on
     * the wall clock + the walk phase the simulation feeds in). No sprite
     * assets, no per-frame allocation beyond this tiny value object.
     *  - breath: idle breathing bob (slow + deep for sleepers)
     *  - blink: the sheets' EYELIDS slot, shown ~130ms every few seconds
     *  - walk: legs scissor + arms counter-swing around the FigureGeometry
     *    anchors, upper body bobs and sways (the South Park waddle)
     *  - talk: head bobble + oscillating talk mouth
     *  - jitter: nervous-face fidget (Tweek)
     */
    private class AnimCalc(anim: FigureAnim, mood: String, spec: SheetSpec) {
        private val seed = anim.seed and 0x7fffffff
        private val t = anim.timeMs
        val breathPeriod = if (mood == "sleep") 3400L else 2100L + seed % 500L
        val breath: Float =
            kotlin.math.sin(6.2832 * ((t % breathPeriod).toFloat() / breathPeriod) +
                (seed % 628) / 100f).toFloat()
        val walkSwingDeg: Float =
            if (anim.walking) kotlin.math.sin(anim.walkPhase) *
                13f * anim.motionAmp else 0f
        val walkBob: Float =
            if (anim.walking) kotlin.math.abs(kotlin.math.sin(anim.walkPhase)) * 1.1f else 0f
        val sway: Float =
            if (anim.walking) kotlin.math.sin(anim.walkPhase) * 1.2f else 0f
        /** Vertical offset (unit space) applied to everything above the legs. */
        val upperBob: Float = when {
            mood == "sleep" -> breath * 0.8f - walkBob
            anim.walking -> walkBob + breath * 0.25f
            else -> breath * 0.45f
        }
        val blink: Boolean = mood == "idle" && !anim.walking &&
            (t + seed * 137L) % (2500L + seed % 1700L) < 130L
        val jitter: Float =
            if (spec.face == "nervous" && mood != "sleep")
                (kotlin.math.sin(t * 0.11 + seed) * 0.45).toFloat() else 0f
        /** 0..1 head-bobble phase while this figure is speaking. */
        val talkBob: Float =
            if (mood == "talk") (kotlin.math.sin(t * 0.045 + seed) * 0.5f + 0.5f).toFloat() else 0f
        /** 0.25..1 talking-mouth openness. */
        val mouthOpen: Float =
            (0.6f + 0.4f * kotlin.math.sin(t * 0.05 + seed)).toFloat()
        /** v0.1.9a #16: hood-string flap amplitude on windy days. */
        val windFlap: Float =
            if (anim.wind) (kotlin.math.sin(t * 0.021 + seed) * 3.5).toFloat() else 0f
    }

    // ================= PER-CHARACTER SHEETS =================
    // The cast's reference sheets as data (see CharacterSystem for the
    // standing rule). v0.1.7: palettes + proportions are MEASURED from
    // Lucky's supplied images by tools/measure_specs.py (raw output:
    // tools/measurements.json) for the core six — Kenny / Cartman /
    // Craig / Stan / Kyle from their construction sheets, Garrison
    // from his character-model photo. The remaining cast had no
    // usable base-model image in the supplied set (variants and
    // alter-egos only), so they carry individualized design-derived
    // MeasuredProps (their v0.1.6 design proportions, spread per
    // character against the adolescence chart's band ratios) — every
    // cast member still renders from their OWN proportion set, never
    // a shared band template. Towelie is a towel block (special
    // renderer) and carries none.
    // Kids.
    val SHEETS: Map<String, SheetSpec> = mapOf(
        // Cartman — MEASURED: red jacket #ca152c (v0.1.6 shipped a
        // generic #d43d2a), brown pants read #5b2d24, teal hat
        // #52b9ba + yellow trim #ebda3a, near-black shoes; the WIDEST
        // kid by silhouette (measured width ratio 1.32 vs Kyle) and
        // legs that are essentially a pants strip (leg share 0.024).
        "Cartman" to SheetSpec(skin = "#fedeb7", top = "#ca152c", bottom = "#5b2d24",
            hatId = "pomhat", hatColor = "#52b9ba", hatAccent = "#ebda3a",
            hairStyle = "mop", hairColor = "#6b4a2f",
            width = 1.3f, face = "smug", handColor = "#ebda3a", sig = "chin",
            shoeColor = "#170407",
            placket = true, buttons = true,
            measured = MeasuredProps(0.643f, 0.276f, 0.024f, 1.269f, 1.102f, 1.000f)),
        // Stan — MEASURED: the sheet's jacket is a muted brown-red
        // #9c5b55 (v0.1.6 shipped plain brown #6b4a2f), pants a gray-
        // blue #636182 (was navy #3a5a8a), hat a slate blue #5668a6
        // with deep red trim #c92344 (was bright #2a6fd4/#d43d2a).
        "Stan" to SheetSpec(skin = "#fedcb6", top = "#9c5b55", bottom = "#636182",
            hatId = "pomhat", hatColor = "#5668a6", hatAccent = "#c92344",
            hairStyle = "mop", hairColor = "#3a2a1a", handColor = "#c92344",
            collarHex = "#c92344", shoeColor = "#252436",
            placket = true, buttons = true,
            measured = MeasuredProps(0.653f, 0.210f, 0.080f, 0.946f, 1.085f, 0.985f)),
        // Kyle — MEASURED: jacket a saturated orange #f66118, pants a
        // dark desaturated green #43554b, ushanka a pure green
        // #239637 (v0.1.6: #e07b2a/#2a6f4a/#2a9a4a). His head reads
        // widest vs body of the core boys (headBodyWidth 1.346).
        "Kyle" to SheetSpec(skin = "#fedcb6", top = "#f66118", bottom = "#43554b",
            hatId = "ushanka", hatColor = "#239637", hatAccent = "#f66118",
            hairStyle = "afro", hairColor = "#c0392b", handColor = "#239637",
            collarHex = "#239637", shoeColor = "#43524d",
            placket = true, pockets = 2,
            measured = MeasuredProps(0.618f, 0.263f, 0.066f, 1.085f, 1.346f, 1.040f)),
        // Kenny — MEASURED: parka orange #fc5c06 (v0.1.6 #e07b2a),
        // hood ring #460905 (drawn from the spec in drawHead). His
        // boots were too thin to read in the sheet — dark brown kept.
        "Kenny" to SheetSpec(skin = "#fedbb3", top = "#fc5c06", bottom = "#f95c07",
            handColor = "#6b4a2f", shoeColor = "#3a2a1a",
            placket = true, special = "kenny_parka",
            measured = MeasuredProps(0.607f, 0.267f, 0.126f, 1.155f, 1.089f, 1.040f)),
        "Butters" to SheetSpec(top = "#3aa8a8", bottom = "#2a5a3a",
            hairStyle = "swirl", hairColor = "#e8c53a", face = "worried",
            measured = MeasuredProps(0.630f, 0.255f, 0.115f, 1.12f, 1.30f, 1.02f)),
        "Wendy" to SheetSpec(top = "#8a4a9a", bottom = "#4a4a5a",
            hatId = "beret", hatColor = "#e87a9a",
            hairStyle = "long", hairColor = "#2a2a2a", face = "lashes",
            measured = MeasuredProps(0.625f, 0.265f, 0.110f, 1.10f, 1.28f, 1.03f)),
        // Craig — MEASURED: his sheet jacket + chullo are the same
        // slate blue (#636aa2 / #636da6 — v0.1.6 shipped a bright
        // #3a7ad4 top and a different blue hat), pants near-black
        // #322c36, and his head is the NARROWEST of the core boys
        // (aspect 0.900 — taller than wide).
        "Craig" to SheetSpec(skin = "#fedbb7", top = "#636aa2", bottom = "#322c36",
            hatId = "chullo", hatColor = "#636da6", hatAccent = "#fadb16",
            face = "deadpan", collarHex = "#454e80", shoeColor = "#2b242a",
            placket = true, buttons = true,
            measured = MeasuredProps(0.647f, 0.230f, 0.065f, 0.900f, 1.073f, 0.994f)),
        "Tweek" to SheetSpec(top = "#2a7a4a", bottom = "#6b4a2f",
            hairStyle = "spiky", hairColor = "#e8c53a",
            face = "nervous", sig = "coffee,twitch",
            measured = MeasuredProps(0.640f, 0.250f, 0.110f, 1.05f, 1.22f, 1.00f)),
        "Clyde" to SheetSpec(top = "#8a2a3a", bottom = "#3a5a8a",
            hairStyle = "messy", hairColor = "#6b4a2f", sig = "stripe",
            measured = MeasuredProps(0.635f, 0.260f, 0.105f, 1.08f, 1.25f, 1.01f)),
        "Jimmy" to SheetSpec(top = "#c8a82a", bottom = "#4a4a4a",
            hairStyle = "messy", hairColor = "#6b4a2f", special = "crutches",
            face = "braces",
            measured = MeasuredProps(0.620f, 0.270f, 0.110f, 1.06f, 1.24f, 1.04f)),
        // Timmy: seated — heightRel set directly (0.85) so the chair
        // figure sits lower than standing kids.
        "Timmy" to SheetSpec(top = "#d43d2a", bottom = "#2a2a2a",
            hairStyle = "messy", hairColor = "#2a2a2a", special = "wheelchair",
            measured = MeasuredProps(0.600f, 0.240f, 0.160f, 1.10f, 1.26f, 0.85f)),
        "Tolkien" to SheetSpec(top = "#8a5a9a", bottom = "#2a2a2a",
            hairStyle = "buzz", hairColor = "#2a2a2a", skin = "#7a5230",
            sig = "chain",
            measured = MeasuredProps(0.615f, 0.270f, 0.115f, 1.07f, 1.23f, 1.04f)),
        "Bebe" to SheetSpec(top = "#d43d2a", bottom = "#2a2a2a",
            hairStyle = "curls", hairColor = "#e8c53a",
            measured = MeasuredProps(0.628f, 0.258f, 0.114f, 1.14f, 1.30f, 1.02f)),
        // Michael — the one YOUNG_TEEN: taller than every 4th grader,
        // head share already shrinking toward the teen ratio.
        "Michael" to SheetSpec(skin = "#f2ddc0", top = "#1a1a1a", bottom = "#1a1a1a",
            hairStyle = "swoop", hairColor = "#1a1a1a",
            measured = MeasuredProps(0.560f, 0.280f, 0.160f, 1.02f, 1.18f, 1.14f)),
        "Pete" to SheetSpec(skin = "#f2ddc0", top = "#1a1a1a", bottom = "#1a1a1a",
            hairStyle = "fringeflick", hairColor = "#1a1a1a",
            measured = MeasuredProps(0.638f, 0.252f, 0.110f, 1.04f, 1.21f, 1.00f)),
        "Henrietta" to SheetSpec(skin = "#f2ddc0", top = "#1a1a1a", bottom = "#1a1a1a",
            bottomStyle = "skirt", hairStyle = "bob", hairColor = "#1a1a1a",
            lipstick = true, lipColor = "#1a1a1a",
            measured = MeasuredProps(0.633f, 0.262f, 0.105f, 1.09f, 1.27f, 1.02f)),
        // Adults — every one legs >= torso (Lucky's adult-sheet rule).
        "Randy" to SheetSpec(tall = true, top = "#2a6fd4", bottom = "#6a6a6a",
            hairStyle = "messy", hairColor = "#2a2a2a", mustache = true, beardColor = "#2a2a2a",
            measured = MeasuredProps(0.345f, 0.315f, 0.340f, 1.00f, 1.05f, 1.44f)),
        "Sharon" to SheetSpec(tall = true, top = "#b8a890", bottom = "#4a4a5a", bottomStyle = "skirt",
            hairStyle = "bob", hairColor = "#6b4a2f",
            measured = MeasuredProps(0.350f, 0.310f, 0.340f, 1.02f, 1.08f, 1.42f)),
        "Gerald" to SheetSpec(tall = true, top = "#5a6b3a", bottom = "#3a3a3a",
            hatId = "yarmulke", hatColor = "#e89ab0",
            hairStyle = "buzz", hairColor = "#4a3220",
            beard = true, beardColor = "#4a3220",
            measured = MeasuredProps(0.355f, 0.320f, 0.325f, 0.98f, 1.02f, 1.40f)),
        "Sheila" to SheetSpec(tall = true, top = "#2a3a6a", bottom = "#2a2a3a", bottomStyle = "skirt",
            hairStyle = "beehive", hairColor = "#c0392b", glasses = true,
            measured = MeasuredProps(0.365f, 0.315f, 0.320f, 1.05f, 1.12f, 1.36f)),
        "Liane" to SheetSpec(tall = true, top = "#3a8a8a", bottom = "#c0392b", bottomStyle = "skirt",
            hairStyle = "bob", hairColor = "#6b4a2f", lipstick = true,
            measured = MeasuredProps(0.340f, 0.318f, 0.342f, 1.06f, 1.10f, 1.46f)),
        "Stuart" to SheetSpec(tall = true, top = "#3a7a8a", bottom = "#3a5a8a",
            hatId = "cap", hatColor = "#c0392b", hatAccent = "#f0f0f0",
            beard = true, mustache = true, beardColor = "#3a2a1a",
            measured = MeasuredProps(0.352f, 0.318f, 0.330f, 0.99f, 1.04f, 1.41f)),
        "Carol" to SheetSpec(tall = true, top = "#8ac858", bottom = "#2a4a6a",
            hairStyle = "bob", hairColor = "#c0392b",
            measured = MeasuredProps(0.358f, 0.312f, 0.330f, 1.03f, 1.09f, 1.39f)),
        "Stephen" to SheetSpec(tall = true, top = "#6b7a3a", bottom = "#3a3a3a",
            hairStyle = "sidepart", hairColor = "#4a3220", tie = true,
            measured = MeasuredProps(0.348f, 0.322f, 0.330f, 0.97f, 1.00f, 1.43f)),
        "Linda" to SheetSpec(tall = true, top = "#8a2a3a", bottom = "#6a4a9a", bottomStyle = "skirt",
            hairStyle = "bob", hairColor = "#e8c53a",
            measured = MeasuredProps(0.362f, 0.308f, 0.330f, 1.04f, 1.11f, 1.37f)),
        // Garrison — MEASURED from his character-model photo: skin
        // #fbdbb6, bright green shirt #55c624 (his v10 sheet's
        // #52B84B was close), dark green pants #2d6543 (sheet said
        // #1F6038 — the model's dominant read is lighter), shoes
        // #2b232a. Proportions measured: head 0.330 / torso 0.322 /
        // legs 0.348 — legs LONGER than torso, per Lucky's rule.
        // Bald dome with gray fringe #9A9A9A, wrinkles, gray brows
        // #8E8E8E, ears, glasses, collar #43A03E, placket + buttons
        // + chest pocket, silver belt #C0C0C0. Slim (width 0.92).
        "Garrison" to SheetSpec(tall = true, skin = "#fbdbb6",
            top = "#55c624", bottom = "#2d6543",
            hairStyle = "fringe", hairColor = "#9A9A9A", glasses = true,
            ears = true, wrinkles = true, browHex = "#8E8E8E",
            collarHex = "#43A03E", placket = true, beltHex = "#C0C0C0",
            buttons = true, pockets = 1, shoeColor = "#2b232a",
            width = 0.92f,
            measured = MeasuredProps(0.330f, 0.322f, 0.348f, 1.043f, 0.960f, 1.435f)),
        // Mackey: the giant head is a MEASURED proportion now
        // (headShare 0.460, aspect 1.18) — v0.1.6 faked it with
        // headScale 1.3 on the band circle, the ballooning suspect.
        "Mackey" to SheetSpec(tall = true, top = "#2a9a4a", bottom = "#2a4a6a",
            hairStyle = "buzz", hairColor = "#9a9a9a", glasses = true,
            tie = true, tieColor = "#2a4a8a", width = 0.92f,
            measured = MeasuredProps(0.460f, 0.265f, 0.275f, 1.18f, 1.55f, 1.30f)),
        "Chef" to SheetSpec(tall = true, skin = "#7a5230", top = "#d43d2a", bottom = "#2a2a2a",
            hatId = "chefhat", special = "apron", beard = true, beardColor = "#1a1a1a",
            measured = MeasuredProps(0.335f, 0.330f, 0.335f, 1.08f, 1.02f, 1.48f)),
        "Jesus" to SheetSpec(tall = true, top = "#f0f0f0", bottom = "#f0f0f0", bottomStyle = "skirt",
            hairStyle = "long", hairColor = "#4a3220", beard = true, beardColor = "#4a3220",
            sig = "sash,halo",
            measured = MeasuredProps(0.342f, 0.300f, 0.358f, 0.96f, 1.06f, 1.45f)),
        "Jimbo" to SheetSpec(tall = true, top = "#8a7a5a", bottom = "#3a5a8a",
            hatId = "cap", hatColor = "#e07b2a", hatAccent = "#2a2a2a",
            mustache = true, beardColor = "#5a4028", sig = "vest",
            measured = MeasuredProps(0.338f, 0.330f, 0.332f, 1.01f, 0.98f, 1.47f)),
        "Father Maxi" to SheetSpec(tall = true, top = "#1a1a1a", bottom = "#1a1a1a",
            hairStyle = "messy", hairColor = "#2a2a2a", collar = true,
            measured = MeasuredProps(0.352f, 0.318f, 0.330f, 0.95f, 1.03f, 1.41f)),
        "Towelie" to SheetSpec(tall = true, skin = "#a8d8e8", special = "towel"),
        // ---- v0.3.0b wave 2 (design-derived, adolescence-chart logic) ----
        // Heidi — long auburn hair, pink jacket, lashes: the girls'
        // diplomat, readable by the hair + pink.
        "Heidi" to SheetSpec(top = "#d46a8a", bottom = "#4a4a5a",
            hairStyle = "long", hairColor = "#7a3a22", face = "lashes",
            measured = MeasuredProps(0.622f, 0.262f, 0.116f, 1.08f, 1.27f, 1.01f)),
        // Nichole — dark skin, black curl mane, mustard sweater:
        // Tolkien-toned skin with a cheerleader's silhouette.
        "Nichole" to SheetSpec(skin = "#7a5230", top = "#e8a83a", bottom = "#3a3a5a",
            hairStyle = "curls", hairColor = "#1a1a1a", face = "lashes",
            measured = MeasuredProps(0.618f, 0.266f, 0.116f, 1.06f, 1.24f, 1.02f)),
        // Red — the name is the spec: long red hair, teal jacket.
        "Red" to SheetSpec(top = "#2a7a7a", bottom = "#2a2a3a",
            hairStyle = "long", hairColor = "#b83a1e", face = "lashes",
            measured = MeasuredProps(0.626f, 0.260f, 0.114f, 1.11f, 1.29f, 1.03f)),
        // Kevin — messy brown hair, freckles, saga-blue jacket.
        "Kevin" to SheetSpec(top = "#3a6ab8", bottom = "#4a4a4a",
            hairStyle = "messy", hairColor = "#4a3220", freckles = true,
            measured = MeasuredProps(0.632f, 0.256f, 0.112f, 1.04f, 1.22f, 1.00f)),
        // Francis — wide, gentle, pale-blue shirt, mop of brown.
        "Francis" to SheetSpec(top = "#7ab8d8", bottom = "#5a4a3a", width = 1.18f,
            hairStyle = "mop", hairColor = "#5a4028",
            measured = MeasuredProps(0.612f, 0.272f, 0.116f, 1.13f, 1.26f, 1.02f)),
        // DogPoo — messy dark hair, freckle-grime, dirt-olive top:
        // the smudge is the signature.
        "DogPoo" to SheetSpec(top = "#6a6248", bottom = "#4a3a2a",
            hairStyle = "messy", hairColor = "#3a2a1a", freckles = true,
            measured = MeasuredProps(0.636f, 0.254f, 0.110f, 1.07f, 1.23f, 0.99f)),
        // McDaniels — bob, teal pantsuit, lipstick: City Hall polish.
        "McDaniels" to SheetSpec(tall = true, top = "#2a6a7a", bottom = "#24485a",
            hairStyle = "bob", hairColor = "#2a2a2a", lipstick = true,
            lipColor = "#a83a4a", collarHex = "#f0e8d0",
            measured = MeasuredProps(0.352f, 0.316f, 0.332f, 1.03f, 1.09f, 1.38f)),
        // Barbrady — navy uniform + cap with a gold accent, shades,
        // mustache, silver belt: the whole force at a glance.
        "Barbrady" to SheetSpec(tall = true, top = "#2a3a6a", bottom = "#22304e",
            hatId = "cap", hatColor = "#22345c", hatAccent = "#c8a82a",
            sunglasses = true, mustache = true, beardColor = "#4a3220",
            beltHex = "#C0C0C0", width = 1.12f,
            measured = MeasuredProps(0.344f, 0.326f, 0.330f, 1.05f, 1.06f, 1.44f)),
        // Crabtree — huge dark curl mane, maroon blouse, skirt,
        // deadpan scowl under lipstick: volume you can see.
        "Crabtree" to SheetSpec(tall = true, top = "#8a4a6a", bottom = "#3a3a4a",
            bottomStyle = "skirt", hairStyle = "curls", hairColor = "#4a3220",
            face = "deadpan", lipstick = true, lipColor = "#8a2a3a",
            measured = MeasuredProps(0.356f, 0.318f, 0.326f, 1.09f, 1.13f, 1.40f)),
        // Mr. Tweek — blond sidepart, pale-green sweater, and the
        // coffee sig he shares with his son's side of the family.
        "Mr. Tweek" to SheetSpec(tall = true, top = "#7aa87a", bottom = "#5a4a3a",
            hairStyle = "sidepart", hairColor = "#c8a84a", sig = "coffee",
            measured = MeasuredProps(0.350f, 0.320f, 0.330f, 0.99f, 1.03f, 1.42f)),
        // Mrs. Tweek — brown bun with the accent dot, pink blouse,
        // skirt: the steady hand, drawn warm.
        "Mrs. Tweek" to SheetSpec(tall = true, top = "#d48aa8", bottom = "#5a3a5a",
            bottomStyle = "skirt", hairStyle = "bun", hairColor = "#6b4a2f",
            face = "lashes",
            measured = MeasuredProps(0.358f, 0.314f, 0.328f, 1.05f, 1.10f, 1.38f)),
        // Ned — Jimbo's partner: hunting vest over olive, mustache,
        // glasses (the voice box lives in his lines, not the sprite).
        "Ned" to SheetSpec(tall = true, top = "#6a7a4a", bottom = "#4a4a3a",
            hairStyle = "messy", hairColor = "#5a4a38", mustache = true,
            beardColor = "#5a4a38", glasses = true, sig = "vest",
            measured = MeasuredProps(0.346f, 0.324f, 0.330f, 1.00f, 1.00f, 1.43f)),
        // ---- v0.7.0a wave 3 (design-derived, adolescence-chart logic) ----
        // Karen — the littlest McCormick: long brown hair, magenta
        // coat, small frame, big head (youngest-kid proportions).
        "Karen" to SheetSpec(top = "#b84a78", bottom = "#4a3a5a", width = 0.94f,
            hairStyle = "long", hairColor = "#6a4530",
            measured = MeasuredProps(0.648f, 0.246f, 0.106f, 1.06f, 1.24f, 0.88f)),
        // Scott — freckles, messy brown hair, olive hero-in-training
        // jacket: Captain Diabetes on school days.
        "Scott" to SheetSpec(top = "#6a7a3a", bottom = "#3a4a5a",
            hairStyle = "messy", hairColor = "#5a3a22", freckles = true,
            measured = MeasuredProps(0.630f, 0.258f, 0.112f, 1.05f, 1.23f, 1.00f)),
        // Firkle — all black like his idols, fringe-flick bangs,
        // the smallest silhouette in town.
        "Firkle" to SheetSpec(top = "#1c1c22", bottom = "#1a1a1a", width = 0.92f,
            hairStyle = "fringeflick", hairColor = "#141414",
            measured = MeasuredProps(0.652f, 0.240f, 0.108f, 1.02f, 1.26f, 0.90f)),
        // Annie — long blond hair, purple top, lashes: the circle's
        // bright antenna.
        "Annie" to SheetSpec(top = "#7a4a9a", bottom = "#3a3a4a",
            hairStyle = "long", hairColor = "#e3b93c", face = "lashes",
            measured = MeasuredProps(0.624f, 0.262f, 0.114f, 1.09f, 1.25f, 1.01f)),
        // Bradley — blond mop, teal top: the quiet camouflage of
        // Mintberry Crunch.
        "Bradley" to SheetSpec(top = "#3f7a6a", bottom = "#4a4a4a", width = 0.97f,
            hairStyle = "mop", hairColor = "#d9b84a",
            measured = MeasuredProps(0.638f, 0.252f, 0.110f, 1.03f, 1.21f, 0.99f)),
        // Skeeter — red mop AND beard, rust flannel, bar-owner
        // build: the voice of the bar drawn at counter height.
        "Skeeter" to SheetSpec(tall = true, top = "#8a3a2a", bottom = "#3a4a5a",
            hairStyle = "mop", hairColor = "#b85a28", beard = true,
            beardColor = "#b85a28", width = 1.06f,
            measured = MeasuredProps(0.342f, 0.326f, 0.332f, 1.02f, 1.04f, 1.44f)),
        // Victoria — brown bun, glasses, teal blouse and skirt,
        // lipstick: the clipboard, drawn standing straight.
        "Victoria" to SheetSpec(tall = true, top = "#2a5a6a", bottom = "#3a3a4a",
            bottomStyle = "skirt", hairStyle = "bun", hairColor = "#5a3a26",
            glasses = true, lipstick = true, lipColor = "#a83a4a",
            measured = MeasuredProps(0.350f, 0.320f, 0.330f, 1.00f, 1.06f, 1.40f)),
        // Marvin — white fringe, wrinkles, brown cardigan, the
        // shortest adult in town (stooped by seventy years of right).
        "Marvin" to SheetSpec(tall = true, top = "#6a5a48", bottom = "#4a4238",
            hairStyle = "fringe", hairColor = "#e8e8e8", wrinkles = true,
            width = 0.96f,
            measured = MeasuredProps(0.348f, 0.322f, 0.330f, 0.98f, 1.00f, 1.33f)),
        // Mr. Tucker — blond sidepart, navy sweater, tall and flat:
        // Craig, thirty years on, factory settings.
        "Mr. Tucker" to SheetSpec(tall = true, top = "#2f4a74", bottom = "#4a4238",
            hairStyle = "sidepart", hairColor = "#c9a44e", width = 1.02f,
            measured = MeasuredProps(0.346f, 0.324f, 0.330f, 1.01f, 1.02f, 1.45f)),
        // Steve — dark suit, tie, short black hair: the library
        // wing's proprietor, warm and exact.
        "Steve" to SheetSpec(tall = true, skin = "#5a3a22", top = "#33334a",
            bottom = "#2a2a38", hairStyle = "buzz", hairColor = "#14100c",
            tie = true, collarHex = "#e8e4da", width = 1.04f,
            measured = MeasuredProps(0.344f, 0.328f, 0.328f, 1.04f, 1.05f, 1.46f)),
        // ---- wave 4 (v0.8.0a) ----
        // Ike — the smallest figure in town: a Canadian toddler
        // in powder-blue pajamas, one black tuft, all head.
        "Ike" to SheetSpec(top = "#7ab5d6", bottom = "#5a8ab5", width = 0.90f,
            hairStyle = "buzz", hairColor = "#1a1a1a",
            measured = MeasuredProps(0.655f, 0.235f, 0.110f, 1.04f, 1.28f, 0.80f)),
        // Kevin McCormick — the eldest McCormick: messy brown
        // hair, a worn blue work jacket, teen height on kid
        // geometry (taller than every boy in the core crew;
        // only Michael the goth stands taller among the kids).
        "Kevin McCormick" to SheetSpec(top = "#3a5a74", bottom = "#4a4238",
            hairStyle = "messy", hairColor = "#7a5230", width = 1.00f,
            measured = MeasuredProps(0.605f, 0.270f, 0.125f, 1.05f, 1.20f, 1.12f)),
        // Shelley — Stan's big sister: long brown hair and the
        // famous braces, drawn unimpressed at teen height.
        "Shelley" to SheetSpec(top = "#a85a6a", bottom = "#3a3a4a", width = 0.98f,
            hairStyle = "long", hairColor = "#5a3a26", face = "braces",
            measured = MeasuredProps(0.610f, 0.265f, 0.125f, 1.04f, 1.22f, 1.10f)),
        // Laura — Craig's mom: blond bob, magenta blouse,
        // lipstick; the Tuckers' smiling half.
        "Laura" to SheetSpec(tall = true, top = "#8a3a5a", bottom = "#2a2a2a",
            hairStyle = "bob", hairColor = "#d9b84a", lipstick = true,
            width = 1.00f,
            measured = MeasuredProps(0.345f, 0.325f, 0.330f, 1.01f, 1.03f, 1.42f)),
        // Mephesto — bald dome, grey fringe AND beard, the white
        // lab coat buttoned over forty years of questions.
        "Mephesto" to SheetSpec(tall = true, top = "#e8e6df", bottom = "#4a4a52",
            hairStyle = "fringe", hairColor = "#c8c8c8", beard = true,
            beardColor = "#c8c8c8", placket = true, buttons = true, width = 1.02f,
            measured = MeasuredProps(0.350f, 0.318f, 0.332f, 1.00f, 1.05f, 1.43f)),
        // Yates — the sergeant: sidepart, the mustache, aviators,
        // police navy worn broad.
        "Yates" to SheetSpec(tall = true, top = "#2a3a6a", bottom = "#23233a",
            hairStyle = "sidepart", hairColor = "#4a3423", mustache = true,
            sunglasses = true, placket = true, buttons = true, width = 1.05f,
            measured = MeasuredProps(0.347f, 0.323f, 0.330f, 1.02f, 1.06f, 1.45f)),
        // Adler — the shop teacher: swept brown hair going thin,
        // mustache, work shirt and the shop apron (Chef's token,
        // second trade).
        "Adler" to SheetSpec(tall = true, top = "#5a6a7a", bottom = "#4a3a2a",
            hairStyle = "messy", hairColor = "#4a3423", mustache = true,
            special = "apron", width = 1.01f,
            measured = MeasuredProps(0.349f, 0.321f, 0.330f, 1.00f, 1.04f, 1.44f)),
        // Lu Kim — City Wok's owner: black mop, white shirt,
        // black bow tie — the wok's whole front of house.
        "Lu Kim" to SheetSpec(tall = true, top = "#e8e6df", bottom = "#1f1f26",
            hairStyle = "mop", hairColor = "#141414", tie = true,
            tieColor = "#1a1a1a", width = 0.98f,
            measured = MeasuredProps(0.344f, 0.326f, 0.330f, 1.01f, 1.02f, 1.40f)),
        // ---- wave 5 (v0.9.0a) ----
        // Roger — Clyde's dad: sidepart going grey at the
        // edges, the green dad sweater, widower's tidy bulk.
        "Roger" to SheetSpec(tall = true, top = "#4a6741", bottom = "#3a3a44",
            hairStyle = "sidepart", hairColor = "#5a4632", width = 1.03f,
            measured = MeasuredProps(0.346f, 0.324f, 0.330f, 1.03f, 1.04f, 1.44f)),
        // Ryan — the fire chief: short brown hair, the
        // mustache, station red worn broad and proud.
        "Ryan" to SheetSpec(tall = true, top = "#7a3a2e", bottom = "#2f3336",
            hairStyle = "buzz", hairColor = "#7a4a28", mustache = true,
            beardColor = "#7a4a28", width = 1.04f,
            measured = MeasuredProps(0.347f, 0.323f, 0.330f, 1.02f, 1.05f, 1.45f)),
        // Sarah — Jimmy's mom: long fair hair, mauve top,
        // lipstick; front-row posture even standing still.
        "Sarah" to SheetSpec(tall = true, top = "#b57ea6", bottom = "#4a3a4a",
            hairStyle = "long", hairColor = "#c9a44e", lipstick = true,
            width = 0.99f,
            measured = MeasuredProps(0.344f, 0.326f, 0.330f, 1.00f, 1.02f, 1.41f)),
        // Alexis — the city planner: dark bob, navy blazer
        // over a pale collar; the clipboard is implied.
        "Alexis" to SheetSpec(tall = true, top = "#3a5a74", bottom = "#2a2a32",
            hairStyle = "bob", hairColor = "#2e2320", lipstick = true,
            collarHex = "#e8e4da", width = 1.00f,
            measured = MeasuredProps(0.344f, 0.326f, 0.330f, 1.02f, 1.04f, 1.43f)),
        // Mr. Testaburger — Wendy's dad: quiet brown
        // sidepart, glasses, soft blue shirt; library
        // volume at stadium-pride scale.
        "Mr. Testaburger" to SheetSpec(tall = true, top = "#5a7a9a",
            bottom = "#3f3a33", hairStyle = "sidepart",
            hairColor = "#4a3423", glasses = true, width = 1.01f,
            measured = MeasuredProps(0.348f, 0.322f, 0.330f, 1.00f, 1.03f, 1.43f)),
        // Nathan — the nemesis: dark mop, schemer's blue;
        // a fourth-grader drawn like a chess problem.
        "Nathan" to SheetSpec(top = "#3f6fb5", bottom = "#5a4632",
            hairStyle = "mop", hairColor = "#3a2a1c", width = 1.00f,
            face = "smug",
            measured = MeasuredProps(0.615f, 0.262f, 0.123f, 1.04f, 1.21f, 1.06f)),
        // Sophie — the new girl: long auburn hair, freckles,
        // Scottsdale teal in a town of parkas.
        "Sophie" to SheetSpec(top = "#2e8f86", bottom = "#4a3a5a",
            hairStyle = "long", hairColor = "#8a4a2a", freckles = true,
            width = 0.97f,
            measured = MeasuredProps(0.608f, 0.266f, 0.126f, 1.03f, 1.20f, 1.04f)),
        // Big Gay Al — super: blond swoop, the pink shirt,
        // a white scarf at the neck; drawn at pageant scale.
        "Big Gay Al" to SheetSpec(tall = true, top = "#e56aa0",
            bottom = "#f0ead8", hairStyle = "swoop",
            hairColor = "#e3c26a", mustache = true,
            beardColor = "#e3c26a", scarfHex = "#f5f2ea", width = 1.06f,
            face = "grin",
            measured = MeasuredProps(0.343f, 0.327f, 0.330f, 1.05f, 1.06f, 1.44f)),
        // ---- wave 6 (v0.11.0d): the evidence audit ----
        // Pip — the English kid: blond swoop, the brown
        // jacket of a boy dressed by a stricter century.
        "Pip" to SheetSpec(top = "#6b4a2f", bottom = "#8a7a52",
            hairStyle = "swoop", hairColor = "#d9b84a", width = 0.96f,
            measured = MeasuredProps(0.618f, 0.258f, 0.124f, 1.05f, 1.22f, 1.03f)),
        // Dougie — the second-grader: bowl mop, the blue
        // shirt of Butters' smallest general.
        "Dougie" to SheetSpec(top = "#3a7ca5", bottom = "#4a3a2c",
            hairStyle = "mop", hairColor = "#5a3a22", width = 0.95f,
            measured = MeasuredProps(0.628f, 0.252f, 0.120f, 1.06f, 1.19f, 0.92f)),
        // Jason — sports first: navy top, dark pants, a
        // ball permanently implied under one arm.
        "Jason" to SheetSpec(top = "#2a4a8a", bottom = "#3a3a3a",
            hairStyle = "mop", hairColor = "#3a2a1c", width = 1.01f,
            measured = MeasuredProps(0.610f, 0.264f, 0.126f, 1.02f, 1.23f, 1.05f)),
        // Terrance Mephesto — the geneticist's son: black
        // mop, ranch green, a rival's smug at rest.
        "Terrance Mephesto" to SheetSpec(top = "#5a6e46", bottom = "#3f3a33",
            hairStyle = "mop", hairColor = "#141414", width = 1.00f,
            face = "smug",
            measured = MeasuredProps(0.622f, 0.256f, 0.122f, 1.07f, 1.20f, 1.02f)),
        // Jenny — the list committee: long brown hair,
        // committee pink, a folder always implied.
        "Jenny" to SheetSpec(top = "#c25a7a", bottom = "#3a3a4a",
            hairStyle = "long", hairColor = "#4a2e1e", width = 0.98f,
            measured = MeasuredProps(0.612f, 0.263f, 0.125f, 1.01f, 1.21f, 1.03f)),
        // Flora — the kingmaker: kindergartner green, a
        // skirt, the smallest measured figure in the school.
        "Flora" to SheetSpec(top = "#7ab648", bottom = "#4a7a3a",
            bottomStyle = "skirt",
            hairStyle = "bob", hairColor = "#6b4a2f", width = 0.94f,
            measured = MeasuredProps(0.640f, 0.240f, 0.120f, 1.08f, 1.18f, 0.80f)),
        // Filmore — the challenger: buzz cut, campaign
        // orange, grievance at full posture.
        "Filmore" to SheetSpec(skin = "#7a5230", top = "#b5542a",
            bottom = "#2f3a5a", hairStyle = "buzz", hairColor = "#141414",
            width = 0.97f,
            measured = MeasuredProps(0.636f, 0.244f, 0.120f, 1.09f, 1.19f, 0.82f)),
        // Quaid — the second chair: sandy mop, loyal blue.
        "Quaid" to SheetSpec(top = "#4a6ea5", bottom = "#5a4632",
            hairStyle = "mop", hairColor = "#8a6a3a", width = 0.96f,
            measured = MeasuredProps(0.633f, 0.246f, 0.121f, 1.04f, 1.17f, 0.84f)),
        // Mimsy — the henchman: the biggest kid frame in
        // the school, dull henchman green.
        "Mimsy" to SheetSpec(top = "#5f7355", bottom = "#4a4038",
            hairStyle = "mop", hairColor = "#4a3423", width = 1.05f,
            measured = MeasuredProps(0.620f, 0.260f, 0.120f, 1.03f, 1.22f, 1.07f)),
        // Tricia — Craig's sister: long dark hair, the
        // Tucker girl's magenta, witness posture.
        "Tricia" to SheetSpec(top = "#a55a8a", bottom = "#3a3a44",
            hairStyle = "long", hairColor = "#3a2418", width = 0.95f,
            measured = MeasuredProps(0.626f, 0.254f, 0.120f, 1.05f, 1.20f, 0.95f)),
        // Mr. Slave — Al's husband: buzz cut, the mustache,
        // the dark vest of the sanctuary's bass line.
        "Mr. Slave" to SheetSpec(tall = true, top = "#2a2a2e",
            bottom = "#3a2a2a", hairStyle = "buzz", hairColor = "#1a1a1a",
            mustache = true, beardColor = "#1a1a1a", width = 1.05f,
            measured = MeasuredProps(0.342f, 0.326f, 0.332f, 1.04f, 1.05f, 1.46f)),
        // Linda Black — Tolkien's mom: long dark hair,
        // plum top, the welcome at full posture.
        "Linda Black" to SheetSpec(tall = true, skin = "#7a5230",
            top = "#7a4a6e", bottom = "#2e2a33", hairStyle = "long",
            hairColor = "#1f1712", lipstick = true, width = 1.00f,
            measured = MeasuredProps(0.345f, 0.324f, 0.331f, 1.01f, 1.03f, 1.42f)),
        // Harriet — the Biggle roof: long near-black hair,
        // goth-adjacent plum, praise-first posture.
        "Harriet" to SheetSpec(tall = true, top = "#4a3a52",
            bottom = "#2a2430", hairStyle = "long", hairColor = "#241a20",
            lipstick = true, width = 1.00f,
            measured = MeasuredProps(0.346f, 0.324f, 0.330f, 1.03f, 1.04f, 1.41f)),
        // Mr. Biggle — the foundation: graying sidepart,
        // work browns, maintenance posture.
        "Mr. Biggle" to SheetSpec(tall = true, top = "#5a5148",
            bottom = "#33302c", hairStyle = "sidepart",
            hairColor = "#3a2e26", width = 1.02f,
            measured = MeasuredProps(0.343f, 0.327f, 0.330f, 1.02f, 1.05f, 1.44f)),
        // Mr. Turner — Heidi's dad: the biggest adult
        // frame in town, buzz cut, perimeter posture.
        "Mr. Turner" to SheetSpec(tall = true, top = "#74563a",
            bottom = "#2f2a26", hairStyle = "buzz", hairColor = "#2e2018",
            width = 1.09f,
            measured = MeasuredProps(0.340f, 0.328f, 0.332f, 1.06f, 1.07f, 1.47f)),
        // Nancy — the Turner diplomacy: fair long hair,
        // hearth rose, the gate held open.
        "Nancy" to SheetSpec(tall = true, top = "#8a5a74",
            bottom = "#33383e", hairStyle = "long", hairColor = "#c9a44e",
            lipstick = true, width = 0.99f,
            measured = MeasuredProps(0.347f, 0.323f, 0.330f, 1.00f, 1.02f, 1.40f)),
        // Mr. Stevens — Bebe's dad: quiet sidepart, the
        // stage crew's greens, ballast posture.
        "Mr. Stevens" to SheetSpec(tall = true, top = "#6e7f5a",
            bottom = "#3a3f35", hairStyle = "sidepart",
            hairColor = "#4a3423", width = 1.01f,
            measured = MeasuredProps(0.344f, 0.325f, 0.331f, 1.03f, 1.03f, 1.43f)),
        // Mrs. Stevens — the glamour department: chestnut
        // bob, Stevens mauve, arrival posture.
        "Mrs. Stevens" to SheetSpec(tall = true, top = "#a56a8a",
            bottom = "#33302e", hairStyle = "bob", hairColor = "#8a5a2a",
            lipstick = true, width = 0.98f,
            measured = MeasuredProps(0.348f, 0.322f, 0.330f, 1.01f, 1.02f, 1.42f)),
        // Mr. Stoley — the producer: sidepart, glasses,
        // scheme teal, props implied.
        "Mr. Stoley" to SheetSpec(tall = true, top = "#4f6e7a",
            bottom = "#2e3138", hairStyle = "sidepart",
            hairColor = "#5a4632", glasses = true, width = 1.02f,
            measured = MeasuredProps(0.345f, 0.326f, 0.329f, 1.04f, 1.04f, 1.44f)),
        // Mrs. Stoley — the director: auburn bob, violet
        // top, front-row posture.
        "Mrs. Stoley" to SheetSpec(tall = true, top = "#7a6ea0",
            bottom = "#333040", hairStyle = "bob", hairColor = "#6e4526",
            lipstick = true, width = 0.99f,
            measured = MeasuredProps(0.346f, 0.325f, 0.329f, 1.02f, 1.03f, 1.41f)),
        // Clark — Scott's dad: buzz cut, glasses, cable-war
        // green, scepter posture.
        "Clark" to SheetSpec(tall = true, top = "#5a6e46",
            bottom = "#2f3336", hairStyle = "buzz", hairColor = "#3a2c20",
            glasses = true, width = 1.03f,
            measured = MeasuredProps(0.343f, 0.326f, 0.331f, 1.05f, 1.04f, 1.43f)),
        // Maggie — Yates' wife: long dark hair, appeal-court
        // blue, kettle posture.
        "Maggie" to SheetSpec(tall = true, top = "#6a7a9a",
            bottom = "#2e2a30", hairStyle = "long", hairColor = "#2a1e16",
            lipstick = true, width = 0.99f,
            measured = MeasuredProps(0.344f, 0.327f, 0.329f, 1.01f, 1.03f, 1.42f)),
        // Mitch — the aide-de-camp: dark sidepart, the tie,
        // trench browns at desk posture.
        "Mitch" to SheetSpec(tall = true, top = "#4a423c",
            bottom = "#26262a", hairStyle = "sidepart",
            hairColor = "#241c16", tie = true, tieColor = "#6e2a2a",
            width = 1.02f,
            measured = MeasuredProps(0.342f, 0.328f, 0.330f, 1.03f, 1.05f, 1.45f)),
        // Bob Johnson — the mayor's aide: brown mop, the
        // council tie, notebook posture.
        "Bob Johnson" to SheetSpec(tall = true, top = "#54606e",
            bottom = "#2c3038", hairStyle = "mop", hairColor = "#3a2a1a",
            tie = true, tieColor = "#2e4a6e", width = 1.01f,
            measured = MeasuredProps(0.345f, 0.324f, 0.331f, 1.02f, 1.04f, 1.43f)),
        // Pusslicker — the anchor: suit navy, the red tie,
        // the placket of a decade at the desk.
        "Pusslicker" to SheetSpec(tall = true, top = "#2e3a52",
            bottom = "#23262e", hairStyle = "sidepart",
            hairColor = "#2e2620", tie = true, tieColor = "#a52a2a",
            placket = true, width = 1.02f,
            measured = MeasuredProps(0.346f, 0.323f, 0.331f, 1.04f, 1.03f, 1.44f)),
        // Dr. Doctor — the town doctor: the white coat,
        // graying sidepart, chart posture.
        "Dr. Doctor" to SheetSpec(tall = true, top = "#e8e6df",
            bottom = "#3a3f45", hairStyle = "sidepart",
            hairColor = "#6a6258", placket = true, buttons = true,
            width = 1.01f,
            measured = MeasuredProps(0.343f, 0.325f, 0.332f, 1.02f, 1.04f, 1.44f)),
        // Choksondik — the succession's middle term: the
        // beehive, the glasses, classroom posture.
        "Choksondik" to SheetSpec(tall = true, top = "#7a4a56",
            bottom = "#3a3230", hairStyle = "beehive",
            hairColor = "#4a3423", glasses = true, width = 1.00f,
            measured = MeasuredProps(0.341f, 0.327f, 0.332f, 1.03f, 1.05f, 1.43f)),
        // Strong Woman — the vice-principal: dark hair up
        // in a bun (the sheet geometry's tied-back style —
        // the ponytail lock breaks the temple rule every
        // other style obeys), standard blue, the tallest
        // frame in the school.
        "Strong Woman" to SheetSpec(tall = true, top = "#3a6ea5",
            bottom = "#2e3138", hairStyle = "bun",
            hairColor = "#1f1712", width = 1.05f,
            measured = MeasuredProps(0.339f, 0.329f, 0.332f, 1.05f, 1.06f, 1.48f))
    )

    /**
     * Draw any figure. (cx, cy) = center, h = total height.
     * mood: idle | happy | angry | sad | sleep
     */
    /**
     * Draw a figure. mood: idle | happy | angry | sad | sleep | talk | hurt
     * (the construction-sheet expression states: open eyes, squeezed HURT
     * eyes, SLEEP line eyes, open talking mouth). pose: stand | sit
     * (sit = the sheets' SIT legs: seat band + big shoe ovals forward).
     * [anim] drives the fully procedural animation (see FigureAnim).
     */
    fun drawFigure(c: Canvas, spec: SheetSpec, cx: Float, cy: Float, h: Float,
                   mood: String = "idle", pose: String = "stand",
                   anim: FigureAnim = FigureAnim(),
                   doll: Boolean = false) {
        if (spec.width == 1f) {
            drawFigureInner(c, spec, cx, cy, h, mood, pose, anim, doll)
            return
        }
        // silhouette width (Cartman wide / Mackey narrow) around the figure center
        c.save()
        c.translate(cx, cy)
        c.scale(spec.width, 1f)
        c.translate(-cx, -cy)
        drawFigureInner(c, spec, cx, cy, h, mood, pose, anim, doll)
        c.restore()
    }

    private fun drawFigureInner(c: Canvas, spec: SheetSpec, cx: Float, cy: Float, h: Float,
                                mood: String, pose: String = "stand",
                                anim: FigureAnim = FigureAnim(),
                                doll: Boolean = false) {
        // v0.16.0d (stage 10): the DOLL path never turns — a
        // still is a front pose by doll rules, whatever facing
        // the caller's anim happens to carry
        if (anim.facing != 0 && !doll) {
            drawProfileFigure(c, spec, cx, cy, h, mood, pose, anim)
            return
        }
        if (spec.tall) drawAdultFigure(c, spec, cx, cy, h, mood, pose, anim, doll)
        else drawKidFigure(c, spec, cx, cy, h, mood, pose, anim, doll)
    }

    /** The cut seed of a spec: a stable hash of the spec's
     *  own defining data (SheetSpec carries no name — its
     *  colors and cuts ARE its identity), so the same
     *  character is scissored the same way every frame, and
     *  a costume variant, being a different cut of paper,
     *  is cut its own way. */
    private fun cutSeed(spec: SheetSpec): Long =
        (spec.skin + "|" + spec.top + "|" + spec.bottom + "|" +
            spec.hairStyle + "|" + spec.hairColor + "|" +
            spec.hatId + "|" + spec.face + "|" + spec.special)
            .hashCode().toLong()

    // ================= CUT SILHOUETTE (v0.16.0d, stages 4+5) ====
    // The figure's painted outline, laid as an INK UNDERLAY
    // before the part stack: the head, torso and leg masses
    // are drawn once more as scissor-cut contours (PaperCut)
    // pushed outward by the painted outline's varying weight,
    // in a warm dark ink. The figure's own flat parts then
    // paint inset inside that rim — the outline is part of
    // the shape, its weight breathes like a marker's along
    // its length, and the same character is cut the same way
    // every frame (the seed is the spec's own data — see
    // cutSeed). Dolls wear
    // the rim at portrait weight. The face and small detail
    // paint clean over it, exactly as the source pipeline's
    // faces sit inside their baked line.
    private fun drawCutSilhouette(c: Canvas, spec: SheetSpec, d: Geo,
                                  x0: Float, y0: Float, s: Float,
                                  pose: String, doll: Boolean,
                                  swayU: Float, upperBob: Float,
                                  headOffY: Float, walking: Boolean) {
        val seed = cutSeed(spec)
        val ink = "#2b2320"
        val p = d.plan
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        // the torso and head ride the same sway/bob as the
        // parts they rim (the builders' XB/YB and headOffY);
        // the legs stand unbobbed on the ground line
        fun XB(x: Float) = x0 + (x + swayU) * s
        fun YB(y: Float) = y0 + (y + upperBob) * s
        fun XH(x: Float) = x0 + (x + swayU) * s
        fun YH(y: Float) = y0 + (y + headOffY) * s
        fun rim(contour: List<CutPt>) {
            // v0.17.0a: the OUTLINE ENGINE's ribbon — widths
            // from the contour's own curvature, in final
            // screen pixels, recomputed in this context
            // (the doll context biases to portrait weight).
            // The old uniform baseW formula is removed whole.
            if (contour.size >= 3) {
                val widths = OutlineEngine.offsets(contour, seed,
                    detailBias = if (doll)
                        OutlineEngine.DOLL_DETAIL_BIAS
                    else OutlineEngine.DETAIL_BIAS)
                Paper.fillCut(c,
                    PaperCut.offsetContourW(contour, widths),
                    ink, 228)
            }
        }
        if (spec.special == "towel") {
            // the towel body IS the silhouette: one tall panel
            rim(PaperCut.cutRectContour(XB(30f), YB(70f), XB(70f), YB(140f),
                10f * s, seed))
            return
        }
        // legs (not when seated — the seat band carries the
        // lower silhouette — not for the wheelchair, whose
        // chair is its own silhouette, and not mid-stride:
        // a swung leg leaves a static rim behind, so walking
        // legs carry no rim and read by their swing alone)
        if (pose != "sit" && spec.special != "wheelchair" && !walking) {
            for (side in listOf(-1f, 1f)) {
                val cxL = 50f + side * p.hipX
                rim(PaperCut.cutRectContour(
                    X(cxL - p.legRad - 0.6f), Y(p.hipY - 2f),
                    X(cxL + p.legRad + 0.6f), Y(p.ankleY + 6.5f),
                    p.legRad * s, seed + side.toInt()))
            }
        }
        // torso mass (a skirt flares the hem)
        val halfW = maxOf(p.torsoHalfTop, p.torsoHalfBot) + 0.6f
        val hem = if (spec.bottomStyle == "skirt")
            p.torsoBottom + 10f else p.torsoBottom + 0.5f
        rim(PaperCut.cutRectContour(
            XB(50f - halfW), YB(p.torsoTop - 0.5f),
            XB(50f + halfW), YB(hem), 3.5f * s, seed + 3))
        // head mass
        rim(PaperCut.cutEllipseContour(XH(d.hx), YH(d.hy),
            d.hrx * s, d.hry * s, seed + 5))
    }

    // ================= SIDE PROFILE (v0.16.0a, items 14+15+18) ====
    // Every figure turned sideways, in FigurePieces'
    // PROFILE_STACK order: back → legs → torso → arms →
    // head → eye → mouth → face extras → hair → hat.
    // Geometry comes from FigureGeometry's derived plan +
    // profileAnchors; the face wears the SAME expression
    // the front view wears, read through Expressions'
    // profile fields. Drawn in the figure's own unit box,
    // flipped by the facing sign.
    private fun drawProfileFigure(c: Canvas, spec: SheetSpec, cx: Float,
                                  cy: Float, h: Float, mood: String,
                                  pose: String, anim: FigureAnim) {
        val f = if (anim.facing < 0) -1 else 1
        val d = FigureGeometry.derive(spec)
        val plan = d.plan
        val s = h / plan.boxH
        val x0 = cx - 50f * s
        val y0 = cy - (plan.boxH / 2f) * s
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val an = AnimCalc(anim, mood, spec)
        val kind = if (spec.tall) FaceKind.ADULT else FaceKind.CHILD
        // the resting face rides along at idle, exactly as
        // the front view's drawEyes/drawMouth read spec.face
        val exprMood = if (mood == "idle") when (spec.face) {
            "nervous", "worried" -> "worried"
            "smug" -> if (spec.tall) "amused" else "proud"
            "grin", "braces" -> if (spec.tall) "pleased" else "happy"
            else -> "idle"
        } else mood
        val expr = Expressions.forMood(kind, exprMood)
        val a = FigureGeometry.profileAnchors(d, f)
        val sitting = pose == "sit" && spec.special != "wheelchair"
        val skinP = Paper.fillPaint(spec.skin)
        val botP = Paper.fillPaint(spec.bottom)
        val bodyP = Paper.fillPaint(
            if (spec.special == "underwear_top") spec.skin else spec.top)
        val shoeP = Paper.fillPaint(spec.shoeColor)
        val bob = an.upperBob * s

        // ground shadow
        c.drawOval(RectF(X(50f - d.silhouetteW * 0.4f), Y(plan.ankleY + 2f),
            X(50f + d.silhouetteW * 0.4f), Y(plan.boxH - 1f)),
            Paper.fillPaint("#000000", 60))

        // BACK: cape/wings/backpack read as a panel behind
        // the torso's rear edge
        if (spec.backStyle == "cape" || spec.backStyle == "wings" ||
            spec.backpack) {
            val bp = Paper.fillPaint(
                if (spec.backStyle != "none") spec.backColor else "#8a4a2a")
            c.drawRoundRect(RectF(
                X(50f - f * plan.torsoHalfBot - f * 7f),
                Y(plan.torsoTop) + bob,
                X(50f - f * plan.torsoHalfBot + f * 3f),
                Y(plan.ankleY - 2f)), S(4f), S(4f), bp)
        }

        // LEGS: profile stride — the near leg swings with
        // the walk phase, the far leg counter-swings a
        // shade darker; seated, both bend at the knee
        // (thigh forward, shin down — the full leg, item 10)
        val stride = if (anim.walking)
            kotlin.math.sin(anim.walkPhase) * plan.legLen * 0.38f else 0f
        fun profileLeg(near: Boolean) {
            val legHex = if (near) spec.bottom
                else hexOf(Paper.darker(spec.bottom))
            val sw = if (near) stride else -stride * 0.8f
            val hipX = 50f
            val hipY = plan.hipY
            val lp = Paper.strokePaint(legHex, S(plan.legRad * 2f))
            lp.strokeCap = Paint.Cap.ROUND
            if (sitting) {
                val kneeX = hipX + f * plan.legLen * 0.52f
                val kneeY = plan.torsoBottom + plan.legLen * 0.34f
                val ankX = kneeX + f * 1.5f
                val ankY = plan.ankleY
                c.drawLine(X(hipX), Y(hipY), X(kneeX), Y(kneeY), lp)
                c.drawLine(X(kneeX), Y(kneeY), X(ankX), Y(ankY), lp)
                c.drawRoundRect(RectF(X(ankX) - S(2f), Y(ankY) - S(1f),
                    X(ankX + f * 9f), Y(ankY) + S(6f)), S(3f), S(3f), shoeP)
            } else {
                val ankX = hipX + f * sw
                c.drawLine(X(hipX), Y(hipY), X(ankX), Y(plan.ankleY), lp)
                c.drawRoundRect(RectF(
                    X(ankX) - if (f > 0) S(2f) else S(7f), Y(plan.ankleY) - S(1f),
                    X(ankX) + if (f > 0) S(7f) else S(2f), Y(plan.ankleY) + S(6f)),
                    S(3f), S(3f), shoeP)
            }
        }
        if (spec.special == "wheelchair") {
            val wp = Paper.strokePaint("#2a2a3a", S(4f))
            c.drawCircle(X(50f + f * 4f), Y(plan.ankleY - 7f), S(17f), wp)
            c.drawCircle(X(50f + f * 4f), Y(plan.ankleY - 7f), S(3f),
                Paper.fillPaint("#5a5a6a"))
            c.drawRect(RectF(X(50f - plan.torsoHalfBot - 4f),
                Y(plan.torsoBottom - 14f), X(50f + plan.torsoHalfBot + 4f),
                Y(plan.torsoBottom - 6f)), Paper.fillPaint("#3a3a4a"))
        } else {
            profileLeg(false)
            profileLeg(true)
        }

        // TORSO: a narrower slab than the front view (the
        // body's depth, not its width), leaning a breath
        // into the walk
        val lean = if (anim.walking) f * 1.6f else 0f
        c.drawRoundRect(RectF(
            X(50f - plan.torsoHalfTop * 0.52f + lean),
            Y(plan.torsoTop) + bob,
            X(50f + plan.torsoHalfTop * 0.52f + lean),
            Y(plan.torsoBottom) + bob), S(5f), S(5f), bodyP)
        if (spec.bottomStyle == "skirt") {
            c.drawPath(Path().apply {
                moveTo(X(50f - plan.torsoHalfBot * 0.55f), Y(plan.torsoBottom - 4f))
                lineTo(X(50f + plan.torsoHalfBot * 0.55f), Y(plan.torsoBottom - 4f))
                lineTo(X(50f + plan.torsoHalfBot * 0.8f), Y(plan.torsoBottom + 10f))
                lineTo(X(50f - plan.torsoHalfBot * 0.8f), Y(plan.torsoBottom + 10f))
                close()
            }, botP)
        }

        // ARMS: one arm reads in profile, swinging against
        // the stride (or holding the action's pose)
        val armSwing = if (anim.walking)
            -kotlin.math.sin(anim.walkPhase) * 14f else 0f
        val actArm = when (anim.action) {
            "phone" -> -38f
            "pickup_trash", "use_object" -> -52f
            "talk", "conversation" -> -24f - an.talkBob * 14f
            else -> armSwing
        }
        run {
            val ax = X(50f + lean)
            val ay = Y(plan.shoulderY) + bob
            c.save()
            c.translate(ax, ay)
            c.rotate(f * actArm * 0.6f)
            val r = plan.armRad * s
            c.drawRoundRect(RectF(-r, -3f * s, r, plan.armLen * 0.8f * s),
                r, r, bodyP)
            c.drawCircle(0f, plan.armLen * s, plan.handRad * s,
                if (spec.handColor != null)
                    Paper.fillPaint(spec.handColor) else skinP)
            c.restore()
        }

        // HEAD: the head ellipse, turned — drawn at the
        // derived center with its profile depth (hrx
        // compressed toward the facing axis)
        val headOffY = an.upperBob + an.talkBob * 1.3f
        val hx = X(d.hx)
        val hy = Y(d.hy) + headOffY * s
        c.drawOval(RectF(hx - S(d.hrx * 0.92f), hy - S(d.hry),
            hx + S(d.hrx * 0.92f), hy + S(d.hry)), skinP)
        // the nose: a small step at the facing edge
        c.drawCircle(X(a.noseX), Y(a.noseY) + headOffY * s, S(2.6f), skinP)
        // the ear, on the rear cheek
        c.drawCircle(X(a.earX), Y(a.eyeY + 3f) + headOffY * s,
            S(3.2f), skinP)

        // EYES: one eye, white + pupil riding the
        // expression's profile pupil shift; lids per the
        // expression's lid value
        val eyeXPx = X(a.eyeX) + expr.profPupil * S(2.2f)
        val eyeYPx = Y(a.eyeY) + headOffY * s
        if (expr.lid >= 0.95f || mood == "sleep") {
            val ep = Paper.strokePaint("#1a1a1a", S(2f))
            c.drawPath(Path().apply {
                moveTo(eyeXPx - S(4.5f), eyeYPx)
                quadTo(eyeXPx, eyeYPx + S(3f), eyeXPx + S(4.5f), eyeYPx)
            }, ep)
        } else {
            c.drawOval(RectF(eyeXPx - S(5.5f), eyeYPx - S(7f),
                eyeXPx + S(5.5f), eyeYPx + S(7f)),
                Paper.fillPaint("#ffffff"))
            if (!an.blink) {
                c.drawCircle(eyeXPx + f * S(1.2f), eyeYPx + S(0.8f),
                    S(if (spec.tall) 1.7f else 2f),
                    Paper.fillPaint("#1a1a1a"))
            }
            if (expr.lid > 0.25f) {
                val lidY = eyeYPx - S(7f) + S(14f * expr.lid)
                c.drawRect(RectF(eyeXPx - S(5.5f), eyeYPx - S(7.5f),
                    eyeXPx + S(5.5f), lidY), skinP)
                c.drawLine(eyeXPx - S(5.5f), lidY, eyeXPx + S(5.5f), lidY,
                    Paper.strokePaint("#1a1a1a", S(1.6f)))
            }
            // brow, tilted by the expression
            val bp2 = Paper.strokePaint(
                spec.browHex ?: spec.hairColor, S(2f))
            val bt = expr.brow * S(2.4f)
            c.drawLine(eyeXPx - S(4.5f), eyeYPx - S(10.5f) + bt,
                eyeXPx + S(4.5f), eyeYPx - S(10.5f) - bt * 0.4f, bp2)
        }

        // MOUTH: on the facing cheek, curved and opened by
        // the expression's profile mouth fields (Kenny's
        // parka has none, as ever)
        if (spec.special != "kenny_parka") {
            val mx = X(a.mouthX)
            val my = Y(a.mouthY) + headOffY * s
            val mp = Paper.strokePaint(
                if (spec.lipstick) spec.lipColor else "#1a1a1a", S(2f))
            val cw = S(4.2f)
            val curve = expr.profMouthCurve * S(3.4f)
            if (expr.profMouthOpen > 0.35f) {
                c.drawOval(RectF(mx - cw * 0.55f, my - S(1f),
                    mx + cw * 0.55f, my + S(1f + 5.5f * expr.profMouthOpen)),
                    Paper.fillPaint("#7a2a1a"))
            } else {
                c.drawPath(Path().apply {
                    moveTo(mx - cw, my)
                    quadTo(mx, my + curve, mx + cw, my - curve * 0.3f)
                }, mp)
            }
        }

        // FACE EXTRAS: glasses ride the one eye; a beard
        // wraps the jaw's facing edge; a mustache sits
        // under the nose
        if (spec.glasses || spec.sunglasses) {
            val gp = if (spec.sunglasses) Paper.fillPaint("#1a1a1a")
                else Paper.strokePaint("#1a1a1a", S(2.2f))
            if (spec.sunglasses) {
                c.drawRoundRect(RectF(eyeXPx - S(6.5f), eyeYPx - S(5f),
                    eyeXPx + S(6.5f), eyeYPx + S(5f)), S(3f), S(3f), gp)
            } else {
                c.drawCircle(eyeXPx, eyeYPx, S(6.8f), gp)
            }
            c.drawLine(eyeXPx - f * S(6f), eyeYPx - S(1f),
                X(a.earX), eyeYPx - S(1f),
                Paper.strokePaint("#1a1a1a", S(2f)))
        }
        if (spec.beard) {
            val bp3 = Paper.fillPaint(spec.beardColor)
            c.drawOval(RectF(hx + f * S(d.hrx * 0.25f) - S(4f),
                hy + S(d.hry * 0.30f), hx + f * S(d.hrx * 0.25f) + S(9f),
                hy + S(d.hry * 0.95f)), bp3)
        }
        if (spec.mustache) {
            val mp3 = Paper.fillPaint(spec.beardColor)
            c.drawOval(RectF(X(a.mouthX) - S(5f), Y(a.mouthY) + headOffY * s - S(6.5f),
                X(a.mouthX) + S(5f), Y(a.mouthY) + headOffY * s - S(1.5f)), mp3)
        }
        if (spec.freckles) {
            val fp = Paper.fillPaint("#c08050")
            c.drawCircle(X(a.mouthX) - f * S(3f), Y(a.mouthY) + headOffY * s - S(7f),
                S(1.4f), fp)
            c.drawCircle(X(a.mouthX) - f * S(6.5f), Y(a.mouthY) + headOffY * s - S(5f),
                S(1.4f), fp)
        }

        // HAIR: the sheet's hair mass, shifted a breath
        // toward the rear so it wraps the profile head
        paintKind(c, spec, InkKind.HAIR,
            x0 - f * S(2.5f), y0 + headOffY * s, s)
        drawHairOverlays(c, spec, d, x0, y0 + headOffY * s, s)

        // HAT: the sheet's hat mass over the hair
        paintKind(c, spec, InkKind.HAT,
            x0 - f * S(1.5f), y0 + headOffY * s, s)
        drawHatOverlays(c, spec, d, x0, y0 + headOffY * s, s)

        // Kenny's parka hood ring reads in profile too
        if (spec.special == "kenny_parka") {
            val hp = Paper.strokePaint(spec.top, S(5f))
            c.drawOval(RectF(hx - S(d.hrx * 0.80f), hy - S(d.hry * 0.82f),
                hx + S(d.hrx * 0.80f), hy + S(d.hry * 0.82f)), hp)
        }
    }

    /** On-screen height multiplier for a cast member: their OWN
     *  measured heightRel (v0.1.7) — never the shared band template.
     *  Falls back to the band ladder only for names without a sheet. */
    fun heightFor(name: String): Float =
        SHEETS[name]?.measured?.heightRel ?: CharacterSystem.heightScale(name)

    /** Draw a named character (kids + adults). Costume: ghost | vampire | witch | mask.
     *  v0.1.7: NO band adjustment for cast — the spec IS the character's
     *  own sheet, measured proportions included; FigureGeometry.derive
     *  turns it into the exact body plan + head ellipse drawn. */
    fun drawKid(c: Canvas, name: String, cx: Float, cy: Float, h: Float,
                mood: String = "idle", costume: String? = null, pose: String = "stand",
                anim: FigureAnim = FigureAnim()) {
        val spec = SHEETS[name] ?: SheetSpec()
        drawFigure(c, spec, cx, cy, h, mood, pose, anim)
        if (costume != null) {
            if (spec.width == 1f) {
                drawCostume(c, spec, costume, cx, cy, h)
            } else {
                c.save()
                c.translate(cx, cy)
                c.scale(spec.width, 1f)
                c.translate(-cx, -cy)
                drawCostume(c, spec, costume, cx, cy, h)
                c.restore()
            }
        }
    }

    /** Draw the player's custom avatar from creator appearance ids.
     *  v0.1.9d: the appearance -> SheetSpec mapping lives in the pure
     *  avatarSpec() (GameData) — the deepened creator's categories
     *  (build via MeasuredProps, shoes, eyewear, facial hair, gloves,
     *  back items, constructed tops) all flow through it, and the
     *  figure still renders ONLY via FigureGeometry.derive(). */
    fun drawPlayerAvatar(c: Canvas, appearance: Map<String, String>,
                         cx: Float, cy: Float, h: Float,
                         anim: FigureAnim = FigureAnim(),
                         pose: String = "stand",
                         doll: Boolean = false) {
        val a = validateAppearance(appearance)
        val acc = a["accessory"]!!
        val spec = avatarSpec(a)
        drawFigure(c, spec, cx, cy, h, "happy", pose, anim, doll)
        // v0.1.8: the creator's foam-finger accessory — a big foam
        // hand gripped at the avatar's right hand, finger up
        if (acc == "foam_finger") {
            val s = h / 128f
            val fp2 = Paper.fillPaint("#f2c230")
            c.drawRoundRect(RectF(cx + 12f * s, cy + 22f * s, cx + 26f * s, cy + 52f * s),
                5f * s, 5f * s, fp2)
            c.drawRoundRect(RectF(cx + 16f * s, cy + 8f * s, cx + 22f * s, cy + 26f * s),
                3f * s, 3f * s, fp2)
            val tp = Paper.textPaint("#1a1a1a", 10f * s, bold = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText("#1", cx + 19f * s, cy + 42f * s, tp)
        }
    }

    /** v0.1.9d: the creator's back items, drawn behind the body
     *  (after the ground shadow, before limbs/torso) in BOTH figure
     *  builders. Cape: a cloth panel from the shoulders flaring to
     *  the ankles. Wings: two flat ovals shoulder-high. All coords
     *  ride the derived plan, so they fit every build/proportion. */
    private fun drawBackItem(c: Canvas, spec: SheetSpec, p: FigureGeometry.Plan,
                             x0: Float, y0: Float, s: Float, swayU: Float, bobU: Float) {
        if (spec.backStyle != "cape" && spec.backStyle != "wings") return
        fun XB(x: Float) = x0 + (x + swayU) * s
        fun YB(y: Float) = y0 + (y + bobU) * s
        fun Y(y: Float) = y0 + y * s
        val bp = Paper.fillPaint(spec.backColor)
        if (spec.backStyle == "cape") {
            c.drawPath(Path().apply {
                moveTo(XB(50f - p.torsoHalfTop - 3f), YB(p.torsoTop - 1f))
                lineTo(XB(50f + p.torsoHalfTop + 3f), YB(p.torsoTop - 1f))
                lineTo(XB(50f + p.torsoHalfBot + 9f), Y(p.ankleY - 2f))
                lineTo(XB(50f - p.torsoHalfBot - 9f), Y(p.ankleY - 2f))
                close()
            }, bp)
        } else {
            c.drawOval(RectF(XB(50f - p.torsoHalfTop - 15f), YB(p.torsoTop - 8f),
                XB(50f - 2f), YB(p.torsoTop + 16f)), bp)
            c.drawOval(RectF(XB(50f + 2f), YB(p.torsoTop - 8f),
                XB(50f + p.torsoHalfTop + 15f), YB(p.torsoTop + 16f)), bp)
        }
    }

    // ================= ANCHORED LIMBS =================
    // Arms and legs are capsules rotated around the FigureGeometry
    // SHOULDER / HIP anchors. Each capsule starts tucked INTO the
    // torso silhouette (FigureGeometry.shoulderBite / hipInside), so
    // the limb stays attached at every swing angle, pose, size band
    // and width — the v0.1.5 floating-ovals bug cannot come back.

    /** One arm: sleeve capsule (TORSO color) from the shoulder anchor,
     *  hand circle at its end (the sheet's arm part pair). [side] -1 =
     *  figure's left; [rotDeg] is the total rotation — rest tilt
     *  (NORMAL ARMS) or 0 (ARMS 2) plus the walk swing. */
    private fun drawArm(c: Canvas, p: FigureGeometry.Plan, x0: Float, y0: Float, s: Float,
                        bobU: Float, swayU: Float, side: Float, rotDeg: Float,
                        sleeveP: Paint, handP: Paint,
                        anchor: FPt? = null) {
        // v0.17.0a: the shoulder anchor comes from the
        // ASSEMBLED RIG (Rigger's card pivot) when the caller
        // has one — the painter and the pivot gate read the
        // same joint. The plan fallback is the identical
        // number by construction.
        val ax = x0 + ((anchor?.x ?: (50f + side * p.shoulderX)) + swayU) * s
        val ay = y0 + ((anchor?.y ?: p.shoulderY) + bobU) * s
        c.save()
        c.translate(ax, ay)
        c.rotate(rotDeg)
        // sleeve: starts 3 units above the anchor (tucked into the
        // torso) and runs 80% of the arm; the hand overlaps its tip.
        // v0.1.8 sprite #61: a SLANTED PARALLELOGRAM, not a capsule —
        // the top edge cuts diagonally, the sheet's sleeve cut.
        val r = p.armRad * s
        val slant = r * 0.75f
        c.drawPath(Path().apply {
            moveTo(-r, -3f * s)
            lineTo(r, -3f * s - slant)
            lineTo(r, p.armLen * 0.8f * s)
            lineTo(-r, p.armLen * 0.8f * s)
            close()
        }, sleeveP)
        c.drawCircle(0f, p.armLen * s, p.handRad * s, handP)
        c.restore()
    }

    /** One leg: capsule from the HIP anchor to the ankle with the shoe
     *  oval riding at its end (the sheet's STAND part set). Segment
     *  colors follow the sheet: pants full-length, shorts two-tone,
     *  skirt bare legs, underwear bare legs below the briefs. */
    private fun drawLeg(c: Canvas, p: FigureGeometry.Plan, x0: Float, y0: Float, s: Float,
                        side: Float, rotDeg: Float, spec: SheetSpec,
                        botP: Paint, skinP: Paint, shoeP: Paint,
                        anchor: FPt? = null) {
        // v0.17.0a: hip anchor from the assembled rig, as
        // with the arm (see drawArm).
        val hx = x0 + (anchor?.x ?: (50f + side * p.hipX)) * s
        val hy = y0 + (anchor?.y ?: p.hipY) * s
        c.save()
        c.translate(hx, hy)
        c.rotate(rotDeg)
        val r = p.legRad * s
        val top = -3f * s
        val bot = p.legLen * s
        when (spec.bottomStyle) {
            "shorts" -> if (spec.bottom == "#f0f0f0") {
                c.drawRoundRect(RectF(-r, top, r, bot), r, r, skinP)
            } else {
                c.drawRoundRect(RectF(-r, top, r, bot * 0.5f), r, r, botP)
                c.drawRoundRect(RectF(-r, bot * 0.42f, r, bot), r, r, skinP)
            }
            "skirt" -> c.drawRoundRect(RectF(-r, top, r, bot), r, r, skinP)
            else -> c.drawRoundRect(RectF(-r, top, r, bot), r, r, botP)
        }
        // shoe (or bare foot) at the ankle, overlapping the leg end.
        // v0.1.8 sprite #73: WIDE with a FLAT sole — broader than the
        // leg, bottom edge nearly straight, not a floating oval.
        val footP = if (spec.barefoot) skinP else shoeP
        c.drawRoundRect(RectF(-6.5f * s, (p.legLen - 1f) * s, 6.5f * s, (p.legLen + 7f) * s),
            3f * s, 3f * s, footP)
        c.restore()
    }

    /** v0.16.0a (item 10): the SEATED front legs — thigh
     *  and shin segments from FigureGeometry.seatedLegs,
     *  drawn in the STAND slot's colors with the shoe at
     *  the ankle. An adult's long legs stay long and
     *  present while sitting; the old seat-band-only read
     *  (legs vanishing into a band + floating shoe ovals)
     *  is retired whole. */
    private fun drawSeatedLegs(c: Canvas, p: FigureGeometry.Plan,
                               x0: Float, y0: Float, s: Float,
                               spec: SheetSpec, botP: Paint, shoeP: Paint) {
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val thighP = Paper.strokePaint(spec.bottom, S(p.legRad * 2f))
        thighP.strokeCap = Paint.Cap.ROUND
        val shinHex = if (spec.bottomStyle == "pants") spec.bottom
            else spec.skin
        val shinP = Paper.strokePaint(shinHex, S(p.legRad * 1.8f))
        shinP.strokeCap = Paint.Cap.ROUND
        for ((hip, knee, ankle) in FigureGeometry.seatedLegs(p)) {
            c.drawLine(X(hip.first), Y(hip.second),
                X(knee.first), Y(knee.second), thighP)
            c.drawLine(X(knee.first), Y(knee.second),
                X(ankle.first), Y(ankle.second), shinP)
            val footP = if (spec.barefoot)
                Paper.fillPaint(spec.skin) else shoeP
            c.drawRoundRect(RectF(X(ankle.first - 6.5f), Y(ankle.second - 1f),
                X(ankle.first + 6.5f), Y(ankle.second + 6f)),
                S(3f), S(3f), footP)
        }
    }

    /** TORSO slot details from the sheet — V collar band, placket line,
     *  buttons, flap pockets — drawn on the torso, under the head.
     *  v0.1.7: every Y comes from the DERIVED plan (this character's
     *  own torso span), every X from the derived torso half-width —
     *  the old fixed 86/108 + 76/116 band values silently overrode
     *  per-character proportions for details. */
    private fun drawTorsoDetails(c: Canvas, spec: SheetSpec, p: FigureGeometry.Plan,
                                 x0: Float, y0: Float, s: Float, bobU: Float, swayU: Float) {
        fun X(x: Float) = x0 + (x + swayU) * s
        fun Y(y: Float) = y0 + (y + bobU) * s
        fun S(v: Float) = v * s
        val topY = p.torsoTop
        val botY = p.torsoBottom
        val dark = hexOf(Paper.darker(spec.top))
        spec.collarHex?.let { ch ->
            c.drawPath(Path().apply {
                moveTo(X(50f - p.torsoHalfTop * 0.86f), Y(topY + 1f))
                lineTo(X(50f), Y(topY + 11f))
                lineTo(X(50f + p.torsoHalfTop * 0.86f), Y(topY + 1f))
            }, Paper.strokePaint(ch, S(5f)))
        }
        if (spec.placket) {
            c.drawLine(X(50f), Y(topY + 5f), X(50f), Y(botY - 2f),
                Paper.strokePaint(dark, S(2f)))
        }
        if (spec.buttons) {
            val bp = Paper.fillPaint(dark)
            for (f in listOf(0.38f, 0.62f, 0.86f)) {
                c.drawCircle(X(50f), Y(topY + (botY - topY) * f), S(1.7f), bp)
            }
        }
        if (spec.pockets == 2) {
            // Kyle's jacket: two flap pockets with the sheet's V stitch
            val pk = Paper.strokePaint(dark, S(2f))
            for (cx0 in listOf(50f - p.torsoHalfTop * 0.62f - 8f, 50f + p.torsoHalfTop * 0.62f)) {
                c.drawRoundRect(RectF(X(cx0), Y(topY + 12f), X(cx0 + 8f), Y(topY + 20f)),
                    S(1.5f), S(1.5f), pk)
                c.drawLine(X(cx0 + 1f), Y(topY + 15f), X(cx0 + 4f), Y(topY + 18.5f), pk)
                c.drawLine(X(cx0 + 7f), Y(topY + 15f), X(cx0 + 4f), Y(topY + 18.5f), pk)
            }
        } else if (spec.pockets == 1) {
            // Garrison's chest pocket (sheet: viewer's right of the placket)
            c.drawRect(RectF(X(50f + p.torsoHalfTop * 0.35f), Y(topY + 9f),
                X(50f + p.torsoHalfTop * 0.35f + 10f), Y(topY + 19f)),
                Paper.strokePaint(dark, S(2f)))
        }
        spec.beltHex?.let { bh ->
            // belt buckle at the waist, centered on the placket —
            // the final Garrison sheet's silver buckle where the
            // shirt meets the pants
            c.drawRect(RectF(X(45.5f), Y(botY - 3.5f), X(54.5f), Y(botY - 0.5f)),
                Paper.fillPaint(bh))
        }
        spec.scarfHex?.let { sh ->
            // v0.1.9d creator accessory: a scarf hugging the neck —
            // band across the torso top (it peeks below the chin)
            // with a short tail down the chest. Plan-derived, like
            // every other detail here, so it rides any build.
            val scp = Paper.fillPaint(sh)
            c.drawRoundRect(RectF(X(50f - p.torsoHalfTop * 0.92f), Y(topY - 1f),
                X(50f + p.torsoHalfTop * 0.92f), Y(topY + 6f)), S(3f), S(3f), scp)
            c.drawRoundRect(RectF(X(50f + p.torsoHalfTop * 0.26f), Y(topY + 5f),
                X(50f + p.torsoHalfTop * 0.54f), Y(topY + 15f)), S(2f), S(2f), scp)
        }
    }

    // ================= KID FIGURE (100 x 128) =================
    private fun drawKidFigure(c: Canvas, spec: SheetSpec, cx: Float, cy: Float, h: Float,
                              mood: String, pose: String = "stand", anim: FigureAnim = FigureAnim(),
                              doll: Boolean = false) {
        val s = h / 128f
        val x0 = cx - 50f * s
        val y0 = cy - 64f * s
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s

        val skinP = Paper.fillPaint(spec.skin)
        // v0.1.7: ALL geometry from this spec's derived plan + head
        // ellipse (measured proportions where the spec carries them)
        val d = FigureGeometry.derive(spec)
        val g: Geo = d
        val plan = d.plan
        val an = AnimCalc(anim, mood, spec)
        val swayU = an.sway + an.jitter
        val sitting = pose == "sit" && spec.special != "wheelchair" && spec.special != "crutches"
        // the head's total vertical offset — behind-hair rides the
        // SAME offset as the head itself, talk bobble included, so
        // the layers can never shear apart mid-sentence
        val headOffY = an.upperBob + an.talkBob * 1.3f
        // bobbed/swayed variants for everything above the legs
        fun XB(x: Float) = x0 + (x + swayU) * s
        fun YB(y: Float) = y0 + (y + an.upperBob) * s

        // ---- ground shadow (width follows the derived silhouette) ----
        val shw = d.silhouetteW * 0.55f
        c.drawOval(RectF(X(50f - shw), Y(plan.ankleY + 2f), X(50f + shw), Y(127f)),
            Paper.fillPaint("#000000", 60))

        // ---- v0.16.0d: the cut silhouette (the painted
        // outline's ink underlay) under the whole part stack ----
        drawCutSilhouette(c, spec, d, x0, y0, s, pose, doll,
            swayU, an.upperBob, headOffY, anim.walking)

        // ---- v0.1.9d back items (creator): cape / wings behind
        // everything the body wears, plan-derived like the shadow ----
        drawBackItem(c, spec, plan, x0, y0, s, swayU, an.upperBob)

        // ---- backpack behind torso ----
        if (spec.backpack) {
            val bp = Paper.fillPaint("#8a4a2a")
            c.drawRoundRect(RectF(XB(50f - plan.torsoHalfTop - 12f), YB(plan.torsoTop - 2f),
                XB(50f - plan.torsoHalfTop + 2f), YB(plan.torsoBottom)), S(6f), S(6f), bp)
            c.drawRoundRect(RectF(XB(50f + plan.torsoHalfTop - 2f), YB(plan.torsoTop - 2f),
                XB(50f + plan.torsoHalfTop + 12f), YB(plan.torsoBottom)), S(6f), S(6f), bp)
        }

        // ---- behind-head hair (long / afro) — bobs with the head ----
        paintKind(c, spec, InkKind.BEHIND_HAIR, x0 + swayU * s, y0 + headOffY * s, s)

        // ---- legs: anchored capsules from the hip anchors, feet
        // planted (no bob); the walk cycle scissors them around the hips
        val botP = Paper.fillPaint(spec.bottom)
        val shoeP = Paper.fillPaint(spec.shoeColor)
        // ---- v0.17.0a: the figure is ASSEMBLED by the
        // rigger first; the limb anchors below are the
        // assembled cards' pivots, so the painter and the
        // pivot gate read the same joints ----
        val rig = Rigger.assemble("figure", spec)
        val hipAnchorL = rig.part("thigh.L")?.pivot
        val hipAnchorR = rig.part("thigh.R")?.pivot
        val armAnchorL = rig.part("upperarm.L")?.pivot
        val armAnchorR = rig.part("upperarm.R")?.pivot
        if (!sitting) {
            val legSwing = if (spec.special == "wheelchair") 0f else an.walkSwingDeg * 0.85f
            drawLeg(c, plan, x0, y0, s, -1f, -legSwing, spec, botP, skinP, shoeP, hipAnchorL)
            drawLeg(c, plan, x0, y0, s, 1f, legSwing, spec, botP, skinP, shoeP, hipAnchorR)
        }
        // ---- underwear briefs at the hip (New Kid base) ----
        if (spec.bottomStyle == "shorts" && spec.bottom == "#f0f0f0") {
            c.drawRoundRect(RectF(X(50f - plan.torsoHalfBot * 0.75f), Y(plan.hipY - 1f),
                X(50f + plan.torsoHalfBot * 0.75f), Y(plan.hipY + 9f)), S(4f), S(4f), botP)
            c.drawLine(X(50f - plan.torsoHalfBot * 0.75f), Y(plan.hipY + 1f),
                X(50f + plan.torsoHalfBot * 0.75f), Y(plan.hipY + 1f),
                Paper.strokePaint("#d8d8d8", S(1.5f)))
        }
        // ---- skirt over the hips ----
        if (spec.bottomStyle == "skirt") {
            c.drawPath(Path().apply {
                moveTo(X(50f - plan.torsoHalfBot - 2f), Y(plan.torsoBottom - 4f))
                lineTo(X(50f + plan.torsoHalfBot + 2f), Y(plan.torsoBottom - 4f))
                lineTo(X(50f + plan.torsoHalfBot + 8f), Y(plan.torsoBottom + 10f))
                lineTo(X(50f - plan.torsoHalfBot - 8f), Y(plan.torsoBottom + 10f)); close()
            }, botP)
        }

        // ---- special: wheelchair (Timmy) replaces lower half ----
        if (spec.special == "wheelchair") {
            val metal = Paper.fillPaint("#5a5a6a")
            val wp = Paper.strokePaint("#2a2a3a", S(4f))
            val wx = plan.torsoHalfBot + 8f
            val wy = plan.ankleY - 7f
            c.drawCircle(X(50f - wx), Y(wy), S(17f), wp)
            c.drawCircle(X(50f + wx), Y(wy), S(17f), wp)
            c.drawCircle(X(50f - wx), Y(wy), S(3f), metal)
            c.drawCircle(X(50f + wx), Y(wy), S(3f), metal)
            c.drawRect(RectF(X(50f - plan.torsoHalfBot - 4f), Y(plan.torsoBottom - 14f),
                X(50f + plan.torsoHalfBot + 4f), Y(plan.torsoBottom - 6f)),
                Paper.fillPaint("#3a3a4a"))
        }

        // ---- torso: one flat blob (mostly tucked under the giant head),
        // riding the breath/walk bob with the arms and head. Its outline
        // IS the derived plan: half-widths at the plan's own top/bottom. ----
        val bare = spec.special == "underwear_top"
        val bodyP = Paper.fillPaint(if (bare) spec.skin else spec.top)

        // ---- arms (v0.1.8 sprite #63: drawn BEFORE the torso so the
        // torso silhouette swallows the sleeve tops — attached, never
        // pasted on. EXCEPT Jimmy: his hands must land ON the crutch
        // crossbars, so his arms draw after the crutches as before.)
        // Rest = the sheet's NORMAL ARMS tilt; walking/sleeping = ARMS 2
        // (straight), walking adds the counter-swing; talking lifts
        // one arm in gesture (#67); the rain pose roofs both arms
        // over the head.
        val sleeveP = Paper.fillPaint(if (bare) spec.skin else spec.top)
        val handP = if (spec.handColor != null) Paper.fillPaint(spec.handColor) else skinP
        val baseTilt = when {
            mood == "sleep" -> 0f
            anim.walking -> 0f
            sitting -> spec.armTilt * 0.6f
            else -> spec.armTilt
        }
        val talkLift = if (mood == "talk") an.talkBob * 16f else 0f
        fun drawArmsNow() {
            if (pose == "hatgrab") {
                // v0.1.9a #16 HAT GRABS: one arm snatches UP at the
                // hat before the gust can take it; the other rides
                // the normal rest tilt.
                drawArm(c, plan, x0, y0, s, an.upperBob, swayU, -1f,
                    baseTilt + an.walkSwingDeg, sleeveP, handP, armAnchorL)
                drawArm(c, plan, x0, y0, s, an.upperBob, swayU, 1f, -146f, sleeveP, handP, armAnchorR)
            } else if (pose == "rain") {
                drawArm(c, plan, x0, y0, s, an.upperBob, swayU, -1f, 152f, sleeveP, handP, armAnchorL)
                drawArm(c, plan, x0, y0, s, an.upperBob, swayU, 1f, -152f, sleeveP, handP, armAnchorR)
            } else {
                drawArm(c, plan, x0, y0, s, an.upperBob, swayU, -1f,
                    baseTilt + an.walkSwingDeg, sleeveP, handP, armAnchorL)
                drawArm(c, plan, x0, y0, s, an.upperBob, swayU, 1f,
                    -baseTilt - an.walkSwingDeg - talkLift, sleeveP, handP, armAnchorR)
            }
        }
        if (spec.special != "crutches") drawArmsNow()

        Paper.crooked(c, 1f, XB(g.hx), YB((plan.torsoTop + plan.torsoBottom) / 2f)) {
            c.drawPath(Path().apply {
                moveTo(XB(50f - plan.torsoHalfTop), YB(plan.torsoTop))
                quadTo(XB(g.hx), YB(plan.torsoTop - 4f), XB(50f + plan.torsoHalfTop), YB(plan.torsoTop))
                lineTo(XB(50f + plan.torsoHalfBot), YB(plan.torsoBottom - 2f))
                // v0.1.8 sprite #51: FLAT hem — a straight chord
                // across the bottom, no belly bulge
                lineTo(XB(50f - plan.torsoHalfBot), YB(plan.torsoBottom - 2f))
                close()
            }, bodyP)
        }
        // v0.17.0a: CLOTH region grain over the torso fill —
        // coarse woven fibre under the details (the face is
        // nowhere near this rect, and details paint after)
        if (!bare) Paper.applyMaterial(c,
            XB(50f - plan.torsoHalfTop - 1f), YB(plan.torsoTop),
            XB(50f + plan.torsoHalfTop + 1f),
            YB(plan.torsoBottom - 2f),
            spec.top, cutSeed(spec) + 21, 0.55f,
            PaperRegion.CLOTH)
        drawTorsoDetails(c, spec, plan, x0, y0, s, an.upperBob, swayU)

        drawBodySig(c, spec, x0 + swayU * s, y0 + an.upperBob * s, s)

        // ---- special: crutches (Jimmy) — moved in so the hands land
        // on the crossbars; drawn before the arms so hands grip over ----
        if (spec.special == "crutches") {
            val cr = Paper.fillPaint("#8a8a9a")
            for (sx in listOf(50f - plan.torsoHalfBot - 11f, 50f + plan.torsoHalfBot + 11f)) {
                c.drawRect(RectF(X(sx - 2f), Y(plan.torsoTop - 6f), X(sx + 2f), Y(plan.ankleY + 3f)), cr)
                c.drawRect(RectF(X(sx - 8f), Y(plan.shoulderY + 5f), X(sx + 8f), Y(plan.shoulderY + 9f)), cr)
            }
        }

        // ---- SIT pose (v0.16.0a, item 10): the seat band
        // stays (it is the seat), and the figure's FULL
        // legs bend over it — thigh + shin + shoe at the
        // ankle, from FigureGeometry.seatedLegs ----
        if (sitting) {
            c.drawRoundRect(RectF(X(50f - plan.torsoHalfBot - 3f), Y(plan.torsoBottom - 5f),
                X(50f + plan.torsoHalfBot + 3f), Y(plan.torsoBottom + 7f)), S(3f), S(3f), botP)
            drawSeatedLegs(c, plan, x0, y0, s, spec, botP,
                Paper.fillPaint(spec.shoeColor))
        }

        // Jimmy's arms come last of the body layers (see above)
        if (spec.special == "crutches") drawArmsNow()

        drawHead(c, spec, g, x0 + swayU * s, y0 + headOffY * s,
            s, mood, skinP, an)
    }

    // ================= ADULT FIGURE (100 x 150) =================
    private fun drawAdultFigure(c: Canvas, spec: SheetSpec, cx: Float, cy: Float, h: Float,
                                mood: String, pose: String = "stand", anim: FigureAnim = FigureAnim(),
                                doll: Boolean = false) {
        val s = h / 150f
        val x0 = cx - 50f * s
        val y0 = cy - 75f * s
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s

        val skinP = Paper.fillPaint(spec.skin)
        // v0.1.7: derived plan + head ellipse from THIS spec (see kid)
        val d = FigureGeometry.derive(spec)
        val g: Geo = d
        val plan = d.plan
        val an = AnimCalc(anim, mood, spec)
        val swayU = an.sway + an.jitter
        val sitting = pose == "sit"
        val headOffY = an.upperBob + an.talkBob * 1.3f
        fun XB(x: Float) = x0 + (x + swayU) * s
        fun YB(y: Float) = y0 + (y + an.upperBob) * s

        // ---- ground shadow (width follows the derived silhouette) ----
        val shw = d.silhouetteW * 0.55f
        c.drawOval(RectF(X(50f - shw), Y(plan.ankleY + 4f), X(50f + shw), Y(149f)),
            Paper.fillPaint("#000000", 60))

        // ---- v0.16.0d: the cut silhouette (the painted
        // outline's ink underlay) under the whole part stack ----
        drawCutSilhouette(c, spec, d, x0, y0, s, pose, doll,
            swayU, an.upperBob, headOffY, anim.walking)

        // ---- v0.1.9d back items (creator): same layer as kids ----
        drawBackItem(c, spec, plan, x0, y0, s, swayU, an.upperBob)

        // ---- Towelie special: towel body instead of person ----
        if (spec.special == "towel") {
            c.save()
            c.translate(0f, an.breath * 0.6f * s)
            val towelP = Paper.fillPaint("#f4f4f4")
            // towel: tall rounded sheet with fringe at the bottom
            c.drawRoundRect(RectF(X(30f), Y(70f), X(70f), Y(140f)), S(10f), S(10f), towelP)
            // fringe
            val fp = Paper.strokePaint("#d8d8d8", S(3f))
            for (i in 0..6) {
                val fx = X(34f + i * 5.2f)
                c.drawLine(fx, Y(140f), fx, Y(148f), fp)
            }
            // wrinkle lines
            val wp = Paper.strokePaint("#d8d8d8", S(2f))
            c.drawLine(X(42f), Y(86f), X(40f), Y(120f), wp)
            c.drawLine(X(58f), Y(86f), X(60f), Y(120f), wp)
            // eyes ON the towel, New Kid style
            val white = Paper.fillPaint("#ffffff")
            val black = Paper.fillPaint("#1a1a1a")
            c.drawOval(RectF(X(38f), Y(84f), X(50f), Y(100f)), white)
            c.drawOval(RectF(X(50f), Y(84f), X(62f), Y(100f)), white)
            c.drawCircle(X(44f), Y(92f), S(2.2f), Paper.fillPaint("#8a2a20"))
            c.drawCircle(X(56f), Y(92f), S(2.2f), Paper.fillPaint("#8a2a20"))
            // heavy-lidded shadow lines over the eyes
            val lp = Paper.strokePaint("#7ab8cc", S(2.5f))
            c.drawLine(X(39f), Y(87f), X(49f), Y(87f), lp)
            c.drawLine(X(51f), Y(87f), X(61f), Y(87f), lp)
            c.drawLine(X(46f), Y(108f), X(54f), Y(108f),
                Paper.strokePaint("#1a1a1a", S(2f)))
            c.restore()
            return
        }

        // ---- behind-head hair — bobs with the head ----
        paintKind(c, spec, InkKind.BEHIND_HAIR, x0 + swayU * s, y0 + headOffY * s, s)

        // ---- legs: anchored capsules from the hip anchors (feet
        // planted); skirt floats over the hips as before ----
        val botP = Paper.fillPaint(spec.bottom)
        val shoeP = Paper.fillPaint(spec.shoeColor)
        // ---- v0.17.0a: the figure is ASSEMBLED by the
        // rigger first; the limb anchors below are the
        // assembled cards' pivots, so the painter and the
        // pivot gate read the same joints ----
        val rig = Rigger.assemble("figure", spec)
        val hipAnchorL = rig.part("thigh.L")?.pivot
        val hipAnchorR = rig.part("thigh.R")?.pivot
        val armAnchorL = rig.part("upperarm.L")?.pivot
        val armAnchorR = rig.part("upperarm.R")?.pivot
        if (!sitting) {
            drawLeg(c, plan, x0, y0, s, -1f, -an.walkSwingDeg * 0.85f, spec, botP, skinP, shoeP, hipAnchorL)
            drawLeg(c, plan, x0, y0, s, 1f, an.walkSwingDeg * 0.85f, spec, botP, skinP, shoeP, hipAnchorR)
        }
        if (spec.bottomStyle == "skirt") {
            c.drawPath(Path().apply {
                moveTo(X(50f - plan.torsoHalfBot - 1f), Y(plan.torsoBottom - 6f))
                lineTo(X(50f + plan.torsoHalfBot + 1f), Y(plan.torsoBottom - 6f))
                lineTo(X(50f + plan.torsoHalfBot + 9f), Y(plan.torsoBottom + 22f))
                lineTo(X(50f - plan.torsoHalfBot - 9f), Y(plan.torsoBottom + 22f)); close()
            }, botP)
        }

        // ---- torso (rides the breath/walk bob) — outline IS the plan ----
        val bodyP = Paper.fillPaint(spec.top)

        // ---- arms BEFORE the torso (v0.1.8 sprite #63, as kids) ----
        val handP = if (spec.handColor != null) Paper.fillPaint(spec.handColor) else skinP
        val baseTilt = when {
            mood == "sleep" -> 0f
            anim.walking -> 0f
            sitting -> spec.armTilt * 0.6f
            else -> spec.armTilt
        }
        val talkLift = if (mood == "talk") an.talkBob * 16f else 0f
        if (pose == "hatgrab") {
            // v0.1.9a #16 HAT GRABS (adult form — see kid figure)
            drawArm(c, plan, x0, y0, s, an.upperBob, swayU, -1f,
                baseTilt + an.walkSwingDeg, bodyP, handP, armAnchorL)
            drawArm(c, plan, x0, y0, s, an.upperBob, swayU, 1f, -146f, bodyP, handP, armAnchorR)
        } else if (pose == "rain") {
            drawArm(c, plan, x0, y0, s, an.upperBob, swayU, -1f, 152f, bodyP, handP, armAnchorL)
            drawArm(c, plan, x0, y0, s, an.upperBob, swayU, 1f, -152f, bodyP, handP, armAnchorR)
        } else {
            drawArm(c, plan, x0, y0, s, an.upperBob, swayU, -1f,
                baseTilt + an.walkSwingDeg, bodyP, handP, armAnchorL)
            drawArm(c, plan, x0, y0, s, an.upperBob, swayU, 1f,
                -baseTilt - an.walkSwingDeg - talkLift, bodyP, handP, armAnchorR)
        }

        Paper.crooked(c, 1f, XB(g.hx), YB((plan.torsoTop + plan.torsoBottom) / 2f)) {
            c.drawPath(Path().apply {
                moveTo(XB(50f - plan.torsoHalfTop), YB(plan.torsoTop))
                quadTo(XB(g.hx), YB(plan.torsoTop - 4f), XB(50f + plan.torsoHalfTop), YB(plan.torsoTop))
                lineTo(XB(50f + plan.torsoHalfBot), YB(plan.torsoBottom - 2f))
                // v0.1.8 sprite #51: FLAT hem (see kid figure)
                lineTo(XB(50f - plan.torsoHalfBot), YB(plan.torsoBottom - 2f))
                close()
            }, bodyP)
        }
        // v0.17.0a: CLOTH region grain over the torso fill
        // (see the kid figure — under the details, always)
        Paper.applyMaterial(c,
            XB(50f - plan.torsoHalfTop - 1f), YB(plan.torsoTop),
            XB(50f + plan.torsoHalfTop + 1f),
            YB(plan.torsoBottom - 2f),
            spec.top, cutSeed(spec) + 21, 0.55f,
            PaperRegion.CLOTH)
        drawTorsoDetails(c, spec, plan, x0, y0, s, an.upperBob, swayU)
        // ---- tie ----
        if (spec.tie) {
            val tp = Paper.fillPaint(spec.tieColor)
            c.drawPath(Path().apply {
                moveTo(XB(46f), YB(plan.torsoTop)); lineTo(XB(54f), YB(plan.torsoTop))
                lineTo(XB(52f), YB(plan.torsoBottom - 10f)); lineTo(XB(50f), YB(plan.torsoBottom - 6f))
                lineTo(XB(48f), YB(plan.torsoBottom - 10f)); close()
            }, tp)
            c.drawCircle(XB(50f), YB(plan.torsoTop + 3f), S(3f), tp)
        }
        // ---- apron (Chef) ----
        if (spec.special == "apron") {
            c.drawPath(Path().apply {
                moveTo(XB(50f - plan.torsoHalfTop * 0.8f), YB(plan.torsoTop + 2f))
                lineTo(XB(50f + plan.torsoHalfTop * 0.8f), YB(plan.torsoTop + 2f))
                lineTo(XB(50f + plan.torsoHalfBot * 0.8f + 2f), YB(plan.torsoBottom + 2f))
                lineTo(XB(50f - plan.torsoHalfBot * 0.8f - 2f), YB(plan.torsoBottom + 2f)); close()
            }, Paper.fillPaint("#f4f4f4"))
        }

        drawBodySig(c, spec, x0 + swayU * s, y0 + an.upperBob * s, s)

        // ---- SIT pose (v0.16.0a, item 10): seat band +
        // the adult's FULL long legs, bent at the knee —
        // thigh, shin and shoe all present at adult scale,
        // where the old shoe-ovals-only read let the legs
        // vanish into the band ----
        if (sitting) {
            c.drawRoundRect(RectF(X(50f - plan.torsoHalfBot - 3f), Y(plan.torsoBottom - 6f),
                X(50f + plan.torsoHalfBot + 3f), Y(plan.torsoBottom + 8f)), S(3f), S(3f), botP)
            drawSeatedLegs(c, plan, x0, y0, s, spec, botP,
                Paper.fillPaint(spec.shoeColor))
        }

        drawHead(c, spec, g, x0 + swayU * s, y0 + headOffY * s,
            s, mood, skinP, an)
    }

    /**
     * v0.5.0f: paint one pure ink layer from [HeadStack] — the
     * exact shape the JVM suite rasterizes with the shape's own
     * point test, so what the suite proves IS what renders.
     */
    private fun paintInk(c: Canvas, layer: InkLayer, x0: Float, y0: Float, s: Float) {
        val p = Paper.fillPaint(layer.color, layer.alpha)
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        when (val sh = layer.shape) {
            is EllipseInk -> {
                val rect = RectF(X(sh.cx - sh.rx), Y(sh.cy - sh.ry),
                    X(sh.cx + sh.rx), Y(sh.cy + sh.ry))
                if (sh.rotDeg == 0f) {
                    c.drawOval(rect, p)
                } else {
                    c.save()
                    c.rotate(sh.rotDeg, X(sh.cx), Y(sh.cy))
                    c.drawOval(rect, p)
                    c.restore()
                }
            }
            is RoundRectInk -> c.drawRoundRect(
                RectF(X(sh.l), Y(sh.t), X(sh.r), Y(sh.b)),
                sh.rad * s, sh.rad * s, p)
            is PolyInk -> c.drawPath(Path().apply {
                sh.pts.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(X(pt.x), Y(pt.y)) else lineTo(X(pt.x), Y(pt.y))
                }
                close()
            }, p)
        }
    }

    /** Paint every layer of one [InkKind] of the spec's stack. */
    private fun paintKind(c: Canvas, spec: SheetSpec, kind: InkKind,
                          x0: Float, y0: Float, s: Float) {
        for (layer in HeadStack.layers(spec)) {
            if (layer.kind == kind) paintInk(c, layer, x0, y0, s)
        }
    }

    // ================= HEAD (shared by kids + adults) =================
    private fun drawHead(c: Canvas, spec: SheetSpec, g: Geo,
                         x0: Float, y0: Float, s: Float, mood: String, skinP: Paint,
                         an: AnimCalc) {
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s

        // ---- ears FIRST (sheet HEAD slot — Garrison): they peek from
        // BEHIND the head's edge at eye level; v0.1.6 drew them over
        // the face as blobs. Anchored to the ellipse's horizontal
        // radius so they track the measured head width. ----
        if (spec.ears) {
            c.drawOval(RectF(X(g.hx - g.hrx - 3.5f), Y(g.hy - 2f),
                X(g.hx - g.hrx + 3f), Y(g.hy + 9f)), skinP)
            c.drawOval(RectF(X(g.hx + g.hrx - 3f), Y(g.hy - 2f),
                X(g.hx + g.hrx + 3.5f), Y(g.hy + 9f)), skinP)
            // v0.1.8 sprite #2: one inner arc per ear — a shade of
            // the skin itself, never an outline
            val ep2 = Paper.strokePaint(hexOf(Paper.darker(spec.skin)), S(1.2f), 170)
            c.drawArc(RectF(X(g.hx - g.hrx - 1.8f), Y(g.hy + 0.5f),
                X(g.hx - g.hrx + 1.8f), Y(g.hy + 7.5f)), -50f, 110f, false, ep2)
            c.drawArc(RectF(X(g.hx + g.hrx - 1.8f), Y(g.hy + 0.5f),
                X(g.hx + g.hrx + 1.8f), Y(g.hy + 7.5f)), 120f, 110f, false, ep2)
        }

        // ---- HEAD: the HeadStack HEAD layers — a flat ELLIPSE
        // from the derived geometry (measured width:height aspect
        // per character — the old single-radius circle is what
        // ballooned), no outline, no neck. Kenny's hood + dark
        // ring are his HEAD layers instead. ----
        paintKind(c, spec, InkKind.HEAD, x0, y0, s)

        // ---- v0.17.0a: SKIN region on the head mass — the
        // finest grain at whisper strength, clipped to the
        // head ellipse and laid UNDER every feature: eyes,
        // mouth, beard and hair all paint after this, clean
        // (stage 6's rule — faces stay legible — is about the
        // features, and they are untouched). Kenny's mass is
        // his hood, so his grain rides the hood's color. ----
        run {
            val headHex = if (spec.special == "kenny_parka")
                spec.top else spec.skin
            c.save()
            val headPath = Path().apply {
                addOval(RectF(X(g.hx - g.hrx), Y(g.hy - g.hry),
                    X(g.hx + g.hrx), Y(g.hy + g.hry)),
                    Path.Direction.CW)
            }
            c.clipPath(headPath)
            Paper.applyMaterial(c, X(g.hx - g.hrx),
                Y(g.hy - g.hry), X(g.hx + g.hrx),
                Y(g.hy + g.hry), headHex,
                headHex.hashCode().toLong() + 7, 0.5f,
                PaperRegion.SKIN)
            c.restore()
        }

        // ---- priest collar band below the chin (Father Maxi) ----
        if (spec.collar) {
            c.drawRoundRect(RectF(X(g.hx - 14f), Y(g.hy + g.hry - 8f),
                X(g.hx + 14f), Y(g.hy + g.hry + 2f)), S(3f), S(3f),
                Paper.fillPaint("#f0f0f0"))
            // v0.1.8 sprite #5: the collar's center notch (tab line)
            c.drawLine(X(g.hx), Y(g.hy + g.hry - 8f), X(g.hx), Y(g.hy + g.hry - 1f),
                Paper.strokePaint("#d8d8d8", S(1.5f)))
        }

        // ---- special: Kenny parka (hood + face slit instead of normal head) ----
        if (spec.special == "kenny_parka") {
            // hood + dark ring were painted as his HEAD layers
            // above (HeadStack) — the same shapes the suite pins;
            // what remains here is the inner ring stroke + face
            // v0.1.8 sprite #3: a second, inner ring stroke between
            // the face slit and the dark ring — the hood's layers
            c.drawRoundRect(RectF(X(g.eyeL - 1.2f), Y(g.eyeT - 2.2f),
                X(g.eyeR + 1.2f), Y(g.eyeB + 2.2f)), S(9f), S(9f),
                Paper.strokePaint("#2a0302", S(1.8f)))
            drawEyes(c, spec, g, x0, y0, s, mood, an.blink)
            // hood drawstrings with toggles — v0.1.9a #16: on
            // windy days they FLAP the whole time
            val flap = an.windFlap
            val stp = Paper.strokePaint("#f0e0c8", S(2f))
            c.drawLine(X(45f), Y(g.eyeB + 6f), X(43f + flap), Y(g.eyeB + 17f), stp)
            c.drawLine(X(55f), Y(g.eyeB + 6f), X(57f + flap), Y(g.eyeB + 17f), stp)
            c.drawCircle(X(43f + flap), Y(g.eyeB + 18f), S(2f), Paper.fillPaint("#f0e0c8"))
            c.drawCircle(X(57f + flap), Y(g.eyeB + 18f), S(2f), Paper.fillPaint("#f0e0c8"))
            // NO mouth for Kenny, ever. (His brown mittens are the
            // arms' hand color now — they move with the anchored arms.)
            return
        }

        // ---- beard (behind mouth, over lower head) ----
        // v0.1.9d: the creator's facial-hair set — "full" is the
        // sheet beard every cast beard already wears; chinstrap is a
        // jaw band, goatee a chin patch with its soul strip.
        if (spec.beard && spec.beardStyle == "chinstrap") {
            val bp = Paper.strokePaint(spec.beardColor, S(5f))
            c.drawArc(RectF(X(g.hx - g.hrx * 0.9f), Y(g.hy - g.hry * 0.9f),
                X(g.hx + g.hrx * 0.9f), Y(g.hy + g.hry * 0.98f)),
                25f, 130f, false, bp)
        } else if (spec.beard && spec.beardStyle == "goatee") {
            val bp = Paper.fillPaint(spec.beardColor)
            c.drawRect(RectF(X(g.hx - g.hrx * 0.1f), Y(g.mouthY + 1f),
                X(g.hx + g.hrx * 0.1f), Y(g.mouthY + 4.5f)), bp)
            c.drawOval(RectF(X(g.hx - g.hrx * 0.17f), Y(g.mouthY + 3f),
                X(g.hx + g.hrx * 0.17f), Y(g.hy + g.hry - 1.5f)), bp)
        } else if (spec.beard) {
            val bp = Paper.fillPaint(spec.beardColor)
            val bw = g.hrx * 0.66f
            c.drawPath(Path().apply {
                moveTo(X(g.hx - bw), Y(g.hy + g.hry * 0.19f))
                quadTo(X(g.hx - bw * 0.79f), Y(g.hy + g.hry - 4f), X(g.hx), Y(g.hy + g.hry - 2f))
                quadTo(X(g.hx + bw * 0.79f), Y(g.hy + g.hry - 4f), X(g.hx + bw), Y(g.hy + g.hry * 0.19f))
                quadTo(X(g.hx + bw * 0.54f), Y(g.hy + g.hry * 0.49f), X(g.hx), Y(g.hy + g.hry * 0.45f))
                quadTo(X(g.hx - bw * 0.54f), Y(g.hy + g.hry * 0.49f), X(g.hx - bw), Y(g.hy + g.hry * 0.19f))
                close()
            }, bp)
        }

        // ---- hair (front layer): HeadStack HAIR layers + the
        // stroke-only overlays (part line, swirl curl, hive wraps)
        paintKind(c, spec, InkKind.HAIR, x0, y0, s)
        drawHairOverlays(c, spec, g, x0, y0, s)

        // ---- hat: HeadStack HAT layers + stroke overlays
        // (chullo weave + strings, the M, toque pleats, muff band)
        paintKind(c, spec, InkKind.HAT, x0, y0, s)
        drawHatOverlays(c, spec, g, x0, y0, s)

        // ---- freckles ----
        if (spec.freckles) {
            val fp = Paper.fillPaint("#c08050")
            for (dx in listOf(-0.33f, -0.19f, 0.33f, 0.19f)) {
                c.drawCircle(X(g.hx + dx * g.hrx), Y(g.mouthY - 12f), S(1.5f), fp)
            }
        }

        // ---- forehead wrinkles + eyebrow bars (sheet HEAD slot —
        // Garrison's bald dome carries both, above the glasses) ----
        if (spec.wrinkles) {
            // v0.1.8 sprite #7: forehead lines stay LIGHT — a shade
            // of the skin at partial alpha, thinner than any feature
            val wp = Paper.strokePaint(hexOf(Paper.darker(spec.skin)), S(1.4f), 170)
            c.drawPath(Path().apply {
                moveTo(X(g.hx - 13f), Y(g.hy - g.hry * 0.55f))
                quadTo(X(g.hx), Y(g.hy - g.hry * 0.63f), X(g.hx + 13f), Y(g.hy - g.hry * 0.55f))
            }, wp)
            c.drawPath(Path().apply {
                moveTo(X(g.hx - 10f), Y(g.hy - g.hry * 0.41f))
                quadTo(X(g.hx), Y(g.hy - g.hry * 0.48f), X(g.hx + 10f), Y(g.hy - g.hry * 0.41f))
            }, wp)
        }
        spec.browHex?.let { bh ->
            val brp = Paper.fillPaint(bh)
            c.drawRoundRect(RectF(X(g.eyeL + 3f), Y(g.eyeT - 7.5f), X(50f - 3f), Y(g.eyeT - 4f)),
                S(1.8f), S(1.8f), brp)
            c.drawRoundRect(RectF(X(50f + 3f), Y(g.eyeT - 7.5f), X(g.eyeR - 3f), Y(g.eyeT - 4f)),
                S(1.8f), S(1.8f), brp)
        }

        // ---- eyes ----
        drawEyes(c, spec, g, x0, y0, s, mood, an.blink,
            jy = if (spec.face == "nervous") an.jitter * 1.6f else 0f)

        // ---- glasses / sunglasses over eyes ----
        if (spec.glasses || spec.sunglasses) {
            val exL = (g.eyeL + 50f) / 2f
            val exR = (50f + g.eyeR) / 2f
            val ey = (g.eyeT + g.eyeB) / 2f
            if (spec.sunglasses) {
                // v0.1.8 sprite #9: shades are ROUNDED-RECT lenses
                // joined by a bar — never two floating circles
                val gp = Paper.fillPaint("#1a1a1a")
                val lw = (g.eyeR - g.eyeL) / 2f
                c.drawRoundRect(RectF(X(g.eyeL - 1f), Y(ey - lw * 0.40f),
                    X(50f + 1f), Y(ey + lw * 0.40f)), S(4f), S(4f), gp)
                c.drawRoundRect(RectF(X(50f - 1f), Y(ey - lw * 0.40f),
                    X(g.eyeR + 1f), Y(ey + lw * 0.40f)), S(4f), S(4f), gp)
                c.drawRect(RectF(X(50f - 3f), Y(ey - lw * 0.22f),
                    X(50f + 3f), Y(ey - lw * 0.02f)), gp)
            } else {
                val gp = Paper.strokePaint("#1a1a1a", S(2.5f))
                // lens radius in PIXELS: the unit-space radius converted
                // through S() like everything else (v0.1.6 added a pixel
                // value to a unit value, so lenses shrank as figures grew)
                val er = S((g.eyeR - g.eyeL) / 4f + 1f)
                c.drawCircle(X(exL), Y(ey), er, gp)
                c.drawCircle(X(exR), Y(ey), er, gp)
                // v0.1.8 sprite #8: a REAL bridge — v0.1.7 drew a
                // zero-length line at the center (a dot, invisible)
                c.drawLine(X(exL) + er * 0.72f, Y(ey) - S(1.2f),
                    X(exR) - er * 0.72f, Y(ey) - S(1.2f), gp)
                c.drawLine(X(g.eyeL - 2f), Y(ey), X(g.hx - g.hrx + 4f), Y(ey - 2f), gp)
                c.drawLine(X(g.eyeR + 2f), Y(ey), X(g.hx + g.hrx - 4f), Y(ey - 2f), gp)
            }
        }

        // ---- v0.1.9d eyewear set: goggles + star shades (the
        // classic glasses/sunglasses above ride the spec flags) ----
        if (spec.eyewear == "goggles") {
            val ey2 = (g.eyeT + g.eyeB) / 2f
            val strap = Paper.fillPaint("#4a3220")
            c.drawRect(RectF(X(g.hx - g.hrx - 1f), Y(ey2 - 2.5f),
                X(g.eyeL + 3f), Y(ey2 + 2.5f)), strap)
            c.drawRect(RectF(X(g.eyeR - 3f), Y(ey2 - 2.5f),
                X(g.hx + g.hrx + 1f), Y(ey2 + 2.5f)), strap)
            val rim = Paper.strokePaint("#1a1a1a", S(3.2f))
            val lens = Paper.fillPaint("#e8a33a", 165)
            for (ec in listOf((g.eyeL + 50f) / 2f, (50f + g.eyeR) / 2f)) {
                c.drawCircle(X(ec), Y(ey2), S((g.eyeR - g.eyeL) / 4f + 1.5f), lens)
                c.drawCircle(X(ec), Y(ey2), S((g.eyeR - g.eyeL) / 4f + 1.5f), rim)
            }
            c.drawRect(RectF(X(50f - 2.5f), Y(ey2 - 2f), X(50f + 2.5f), Y(ey2 + 2f)),
                Paper.fillPaint("#1a1a1a"))
        } else if (spec.eyewear == "star_shades") {
            val ey2 = (g.eyeT + g.eyeB) / 2f
            val gold = Paper.fillPaint("#e8c53a")
            val edge = Paper.strokePaint(hexOf(Paper.darker("#e8c53a")), S(1.6f))
            val r = (g.eyeR - g.eyeL) / 4f + 2.5f
            for (ec in listOf((g.eyeL + 50f) / 2f, (50f + g.eyeR) / 2f)) {
                val star = Path()
                for (i in 0 until 10) {
                    val ang = Math.PI * i / 5 - Math.PI / 2
                    val rad = if (i % 2 == 0) r else r * 0.48f
                    val px = X(ec) + (rad * s * Math.cos(ang)).toFloat()
                    val py = Y(ey2) + (rad * s * Math.sin(ang)).toFloat()
                    if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
                }
                star.close()
                c.drawPath(star, gold)
                c.drawPath(star, edge)
            }
            c.drawRect(RectF(X(50f - 3f), Y(ey2 - 1.8f), X(50f + 3f), Y(ey2 + 1.2f)),
                Paper.fillPaint("#e8c53a"))
        }

        // ---- mustache ----
        if (spec.mustache) {
            val mp = Paper.fillPaint(spec.beardColor)
            val mw = g.hrx * 0.24f
            c.drawOval(RectF(X(g.hx - mw), Y(g.mouthY - 7f), X(g.hx), Y(g.mouthY - 1f)), mp)
            c.drawOval(RectF(X(g.hx), Y(g.mouthY - 7f), X(g.hx + mw), Y(g.mouthY - 1f)), mp)
        }

        // ---- mouth ----
        drawMouth(c, spec, g, x0, y0, s, mood, an)

        // ---- head-level signatures (chin / twitch / halo) ----
        drawHeadSig(c, spec, g, x0, y0, s)
    }

    /** Big white ovals OVERLAPPING at center, dot pupils low in the
     *  ovals. Resting-face variants ride along: deadpan half-lids
     *  (Craig), jitter pupils (Tweek), lashes (Wendy). v0.1.8 sprite
     *  pass #11-#18 lives here. [jy] = the nervous vertical jitter. */
    private fun drawEyes(c: Canvas, spec: SheetSpec, g: Geo, x0: Float, y0: Float, s: Float,
                         mood: String, blink: Boolean = false, jy: Float = 0f) {
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val white = Paper.fillPaint("#ffffff")
        val black = Paper.fillPaint("#1a1a1a")
        val skinP = Paper.fillPaint(spec.skin)
        // #11: the two ovals OVERLAP ~10% at the center — they read as
        // one binocular pair, not two circles kissing at a point
        val ov = (g.eyeR - g.eyeL) * 0.05f
        fun eyeWhites() {
            c.drawOval(RectF(X(g.eyeL), Y(g.eyeT), X(50f + ov), Y(g.eyeB)), white)
            c.drawOval(RectF(X(50f - ov), Y(g.eyeT), X(g.eyeR), Y(g.eyeB)), white)
        }
        // #12: pupils sit LOW-CENTER in the ovals, never dead-center;
        // #13: adult pupils are a touch smaller than kid pupils
        val pr = if (spec.tall) 1.7f else 2f
        val py = (g.eyeT + g.eyeB) / 2f + (g.eyeB - g.eyeT) * 0.08f
        fun pupils(jx: Float, jyv: Float) {
            c.drawCircle(X((g.eyeL + 50f) / 2f + jx), Y(py + jyv), S(pr), black)
            c.drawCircle(X((50f + g.eyeR) / 2f - jx), Y(py + jyv), S(pr), black)
        }
        // a skin dome over the top of each eye, its lower edge an arc
        // that FOLLOWS THE EYE'S CURVE (#15) — flat rects read as hats
        fun lidDome(frac: Float) {
            val lidY = g.eyeT + (g.eyeB - g.eyeT) * frac
            val ll = Paper.strokePaint("#1a1a1a", S(1.8f))
            for (pair in listOf(g.eyeL to 50f + ov, 50f - ov to g.eyeR)) {
                c.drawPath(Path().apply {
                    moveTo(X(pair.first), Y(lidY + 2.5f))
                    quadTo(X((pair.first + pair.second) / 2f), Y(lidY - 3f),
                        X(pair.second), Y(lidY + 2.5f))
                    lineTo(X(pair.second), Y(g.eyeT - 1f))
                    lineTo(X(pair.first), Y(g.eyeT - 1f))
                    close()
                }, skinP)
                c.drawPath(Path().apply {
                    moveTo(X(pair.first + 1f), Y(lidY + 2.5f))
                    quadTo(X((pair.first + pair.second) / 2f), Y(lidY - 3f),
                        X(pair.second - 1f), Y(lidY + 2.5f))
                }, ll)
            }
        }
        if (mood == "sleep") {
            // #18: closed eyes SAG in a shallow downward arc
            val ep = Paper.strokePaint("#1a1a1a", S(2f))
            val ey = (g.eyeT + g.eyeB) / 2f
            c.drawPath(Path().apply {
                moveTo(X(g.eyeL + 4f), Y(ey - 1f))
                quadTo(X((g.eyeL + 50f) / 2f), Y(ey + 3.5f), X(50f - 4f), Y(ey - 1f))
            }, ep)
            c.drawPath(Path().apply {
                moveTo(X(50f + 4f), Y(ey - 1f))
                quadTo(X((50f + g.eyeR) / 2f), Y(ey + 3.5f), X(g.eyeR - 4f), Y(ey - 1f))
            }, ep)
            return
        }
        if (mood == "hurt") {
            // squeezed-shut HURT EYES from the construction sheets: > <
            val ep = Paper.strokePaint("#1a1a1a", S(2.4f))
            val ey = (g.eyeT + g.eyeB) / 2f
            c.drawLine(X(g.eyeL + 5f), Y(ey - 6f), X(50f - 5f), Y(ey), ep)
            c.drawLine(X(50f - 5f), Y(ey), X(g.eyeL + 5f), Y(ey + 6f), ep)
            c.drawLine(X(g.eyeR - 5f), Y(ey - 6f), X(50f + 5f), Y(ey), ep)
            c.drawLine(X(50f + 5f), Y(ey), X(g.eyeR - 5f), Y(ey + 6f), ep)
            return
        }
        if (mood == "soft") {
            // #17 (new in v0.1.8): the SOFT face — eyes open under
            // low, curved lids, pupils showing beneath: warmth.
            // The feeling-faces system asks for it by name.
            eyeWhites()
            pupils(0f, 0f)
            lidDome(0.46f)
            return
        }
        if (mood == "goofy") {
            // v0.2.0c: the SELFIE face — eyes squeezed into happy
            // upside-down U arcs (the ^^ face), no whites at all.
            // Pairs with drawMouth's tongue-out grin.
            val ep = Paper.strokePaint("#1a1a1a", S(2.4f))
            val ey = (g.eyeT + g.eyeB) / 2f
            c.drawPath(Path().apply {
                moveTo(X(g.eyeL + 3f), Y(ey + 2f))
                quadTo(X((g.eyeL + 50f) / 2f), Y(ey - 8f), X(50f - 3f), Y(ey + 2f))
            }, ep)
            c.drawPath(Path().apply {
                moveTo(X(50f + 3f), Y(ey + 2f))
                quadTo(X((50f + g.eyeR) / 2f), Y(ey - 8f), X(g.eyeR - 3f), Y(ey + 2f))
            }, ep)
            return
        }
        if (blink) {
            // EYELIDS slot: skin lids slide over the eyes for ~130ms —
            // a skin dome per eye with the lid line arcing across it
            val lidP = Paper.strokePaint("#1a1a1a", S(1.8f))
            for (pair in listOf(g.eyeL to 50f, 50f to g.eyeR)) {
                c.drawOval(RectF(X(pair.first), Y(g.eyeT), X(pair.second), Y(g.eyeB)), skinP)
                val ly = g.eyeT + (g.eyeB - g.eyeT) * 0.45f
                c.drawPath(Path().apply {
                    moveTo(X(pair.first + 1f), Y(ly))
                    quadTo(X((pair.first + pair.second) / 2f), Y(ly + 3.5f), X(pair.second - 1f), Y(ly))
                }, lidP)
            }
            return
        }
        eyeWhites()
        // #14: Tweek's jitter runs on BOTH axes now (x from the face,
        // y from the animation jitter the caller passes in)
        val jx = if (spec.face == "nervous" && mood == "idle") 1.6f else 0f
        val jyv = if (spec.face == "nervous" && mood == "idle") jy else 0f
        // v0.16.0a (item 15): the universal expression
        // registry rides the front view too — the pupil
        // drift and extra lid of the resolved expression
        // for this face's own set (child / adult)
        val exprE = Expressions.forMood(
            if (spec.tall) FaceKind.ADULT else FaceKind.CHILD, mood)
        pupils(jx + exprE.pupilX * 1.4f, jyv + exprE.pupilY * 1.1f)
        if (exprE.lid >= 0.5f && mood != "idle") {
            lidDome((exprE.lid * 0.55f).coerceIn(0.3f, 0.6f))
        }
        if (spec.face == "deadpan" && mood == "idle") {
            // heavy half-lids over the eye tops — Craig's permanent
            // boredom, the lid edge curved with the eye (#15)
            lidDome(0.34f)
        }
        if (spec.face == "lashes") {
            // #16: TWO flicks per eye, outer longer than inner
            val lp = Paper.strokePaint("#1a1a1a", S(1.8f))
            c.drawLine(X(g.eyeL + 2f), Y(g.eyeT + 3f), X(g.eyeL - 2.5f), Y(g.eyeT - 0.5f), lp)
            c.drawLine(X(g.eyeL + 6.5f), Y(g.eyeT + 1.2f), X(g.eyeL + 3.5f), Y(g.eyeT - 2.2f), lp)
            c.drawLine(X(g.eyeR - 2f), Y(g.eyeT + 3f), X(g.eyeR + 2.5f), Y(g.eyeT - 0.5f), lp)
            c.drawLine(X(g.eyeR - 6.5f), Y(g.eyeT + 1.2f), X(g.eyeR - 3.5f), Y(g.eyeT - 2.2f), lp)
        }
    }

    private fun drawMouth(c: Canvas, spec: SheetSpec, g: Geo,
                          x0: Float, y0: Float, s: Float, mood: String,
                          an: AnimCalc) {
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val mp = Paper.strokePaint(if (spec.lipstick) spec.lipColor else "#1a1a1a", S(2f))
        if (mood == "idle") {
            // resting faces — a big chunk of instant recognizability
            when (spec.face) {
                "deadpan" -> c.drawLine(X(46f), Y(g.mouthY), X(54f), Y(g.mouthY), mp)
                "nervous" -> c.drawPath(Path().apply {
                    moveTo(X(42f), Y(g.mouthY))
                    lineTo(X(46f), Y(g.mouthY - 3f)); lineTo(X(50f), Y(g.mouthY + 3f))
                    lineTo(X(54f), Y(g.mouthY - 3f)); lineTo(X(58f), Y(g.mouthY))
                }, mp)
                "worried" -> c.drawPath(Path().apply {
                    moveTo(X(45f), Y(g.mouthY + 3f))
                    quadTo(X(50f), Y(g.mouthY - 2f), X(55f), Y(g.mouthY + 3f))
                }, mp)
                "smug" -> c.drawPath(Path().apply {
                    moveTo(X(42f), Y(g.mouthY + 2f))
                    quadTo(X(52f), Y(g.mouthY + 6f), X(58f), Y(g.mouthY - 2f))
                }, mp)
                "grin", "braces" -> {
                    // open smile: dark mouth, white teeth, brace wire for Jimmy
                    c.drawPath(Path().apply {
                        moveTo(X(41f), Y(g.mouthY - 2f))
                        quadTo(X(50f), Y(g.mouthY + 11f), X(59f), Y(g.mouthY - 2f))
                        close()
                    }, Paper.fillPaint("#7a2a1a"))
                    c.drawPath(Path().apply {
                        moveTo(X(41f), Y(g.mouthY - 2f))
                        quadTo(X(50f), Y(g.mouthY + 4f), X(59f), Y(g.mouthY - 2f))
                        close()
                    }, Paper.fillPaint("#ffffff"))
                    // v0.1.8 sprite #23: one vertical divider splits
                    // the teeth band into two front teeth
                    c.drawLine(X(50f), Y(g.mouthY - 0.5f), X(50f), Y(g.mouthY + 3.2f),
                        Paper.strokePaint("#d8d8d8", S(1.2f)))
                    if (spec.face == "braces") {
                        val bp2 = Paper.strokePaint("#9a9a9a", S(1.8f))
                        c.drawLine(X(43f), Y(g.mouthY + 0.5f), X(57f), Y(g.mouthY + 0.5f), bp2)
                        c.drawLine(X(47f), Y(g.mouthY - 1f), X(47f), Y(g.mouthY + 2f), bp2)
                        c.drawLine(X(53f), Y(g.mouthY - 1f), X(53f), Y(g.mouthY + 2f), bp2)
                    }
                }
                else -> c.drawLine(X(44f), Y(g.mouthY), X(56f), Y(g.mouthY), mp)
            }
            return
        }
        when (mood) {
            "talk" -> {
                // v0.16.0d (stage 9.4 audit): the talking mouth
                // is a SWAP, not a morph — the source pipeline's
                // mouths are a swap library, and ours now swaps
                // too: the anim's speech phase selects one of
                // two fixed glyphs (open oval / closed line) and
                // nothing is ever drawn between them. The old
                // per-frame height interpolation is removed.
                // v0.1.8 sprite #29: lipstick faces talk in lip
                // color.
                if (an.mouthOpen >= 0.55f) {
                    c.drawOval(RectF(X(45f), Y(g.mouthY - 2f), X(55f),
                        Y(g.mouthY + 8f)),
                        Paper.fillPaint(if (spec.lipstick) spec.lipColor
                            else "#7a2a1a"))
                } else {
                    c.drawLine(X(45f), Y(g.mouthY + 2f), X(55f),
                        Y(g.mouthY + 2f),
                        Paper.strokePaint(if (spec.lipstick) spec.lipColor
                            else "#1a1a1a", S(2f)))
                }
            }
            "happy" -> c.drawPath(Path().apply {
                moveTo(X(42f), Y(g.mouthY)); quadTo(X(50f), Y(g.mouthY + 8f), X(58f), Y(g.mouthY))
            }, mp)
            "goofy" -> {
                // v0.2.0c: open grin + tongue lolling out — the
                // face you make when a camera points at you
                c.drawPath(Path().apply {
                    moveTo(X(41f), Y(g.mouthY - 2f))
                    quadTo(X(50f), Y(g.mouthY + 11f), X(59f), Y(g.mouthY - 2f))
                    close()
                }, Paper.fillPaint("#7a2a1a"))
                c.drawPath(Path().apply {
                    moveTo(X(41f), Y(g.mouthY - 2f))
                    quadTo(X(50f), Y(g.mouthY + 4f), X(59f), Y(g.mouthY - 2f))
                    close()
                }, Paper.fillPaint("#ffffff"))
                // the tongue: a pink tab hanging from the grin
                c.drawRoundRect(RectF(X(46f), Y(g.mouthY + 3f), X(54f), Y(g.mouthY + 12f)),
                    S(3.5f), S(3.5f), Paper.fillPaint("#e87a9a"))
                c.drawLine(X(50f), Y(g.mouthY + 6f), X(50f), Y(g.mouthY + 11f),
                    Paper.strokePaint("#c95f82", S(1.2f)))
            }
            "angry" -> {
                c.drawPath(Path().apply {
                    moveTo(X(43f), Y(g.mouthY + 4f))
                    quadTo(X(50f), Y(g.mouthY - 1f), X(57f), Y(g.mouthY + 4f))
                }, mp)
                val bp = Paper.strokePaint("#1a1a1a", S(2.5f))
                c.drawLine(X(34f), Y(g.eyeT - 4f), X(46f), Y(g.eyeT), bp)
                c.drawLine(X(66f), Y(g.eyeT - 4f), X(54f), Y(g.eyeT), bp)
            }
            "sad" -> c.drawPath(Path().apply {
                moveTo(X(43f), Y(g.mouthY + 4f)); quadTo(X(50f), Y(g.mouthY), X(57f), Y(g.mouthY + 4f))
            }, mp)
            else -> c.drawLine(X(44f), Y(g.mouthY), X(56f), Y(g.mouthY), mp)
        }
    }

    // ================= SIGNATURES =================
    /** Body-level signature props: Tweek's coffee, Clyde's jacket stripe,
     *  Tolkien's chain, Jimbo's vest, Jesus' sash. Coords are figure-local;
     *  kid and adult torsos overlap enough that one set serves both. */
    private fun drawBodySig(c: Canvas, spec: SheetSpec,
                            x0: Float, y0: Float, s: Float) {
        val sig = spec.sig ?: return
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val tokens = sig.split(",")
        if ("coffee" in tokens) {
            // Tweek's coffee cup, clutched in his right hand (the anchored
            // arm's hand lands at ~(68,103) in both body plans), steam rising
            c.drawRoundRect(RectF(X(63f), Y(94f), X(76f), Y(109f)), S(2f), S(2f),
                Paper.fillPaint("#f4f4f4"))
            c.drawRect(RectF(X(63f), Y(99f), X(76f), Y(104f)), Paper.fillPaint("#6b4a2f"))
            c.drawArc(RectF(X(74f), Y(97f), X(82f), Y(105f)), -80f, 160f, false,
                Paper.strokePaint("#f4f4f4", S(2.5f)))
            c.drawArc(RectF(X(65f), Y(84f), X(72f), Y(92f)), 0f, 180f, false,
                Paper.strokePaint("#ffffff", S(1.5f), 160))
        }
        if ("stripe" in tokens) {
            // Clyde's jacket trim: dark center band down the torso
            c.drawRect(RectF(X(46f), Y(88f), X(54f), Y(107f)), Paper.fillPaint("#2a2a2a", 170))
        }
        if ("chain" in tokens) {
            // Tolkien's gold chain under the chin
            c.drawPath(Path().apply {
                moveTo(X(40f), Y(88f)); quadTo(X(50f), Y(97f), X(60f), Y(88f))
            }, Paper.strokePaint("#f2c230", S(2.5f)))
        }
        if ("vest" in tokens) {
            // Jimbo's hunting vest over his shirt, with pockets
            val vp = Paper.fillPaint("#4a5d3a")
            c.drawPath(Path().apply {
                moveTo(X(35f), Y(76f)); lineTo(X(47f), Y(76f))
                lineTo(X(47f), Y(114f)); lineTo(X(37f), Y(114f)); close()
            }, vp)
            c.drawPath(Path().apply {
                moveTo(X(53f), Y(76f)); lineTo(X(65f), Y(76f))
                lineTo(X(63f), Y(114f)); lineTo(X(53f), Y(114f)); close()
            }, vp)
            val pk = Paper.fillPaint("#3a4a2e")
            c.drawRect(RectF(X(39f), Y(96f), X(45f), Y(104f)), pk)
            c.drawRect(RectF(X(55f), Y(96f), X(61f), Y(104f)), pk)
        }
        if ("sash" in tokens) {
            // Jesus' red sash, diagonal across the robe
            c.drawLine(X(38f), Y(82f), X(62f), Y(110f),
                Paper.strokePaint("#b03030", S(8f)))
        }
    }

    /** Head-level signatures: Cartman's double chin, Tweek's twitch marks,
     *  Jesus' halo. */
    private fun drawHeadSig(c: Canvas, spec: SheetSpec, g: Geo,
                            x0: Float, y0: Float, s: Float) {
        val sig = spec.sig ?: return
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val tokens = sig.split(",")
        if ("chin" in tokens) {
            // v0.1.8 sprite #1: the double chin is a FLAT CHORD —
            // one straight line under the mouth, not a smile arc
            c.drawLine(X(38f), Y(g.mouthY + 10f), X(62f), Y(g.mouthY + 10f),
                Paper.strokePaint("#d8a878", S(2f)))
        }
        if ("twitch" in tokens) {
            val tp = Paper.strokePaint("#1a1a1a", S(2f), 150)
            c.drawLine(X(g.hx - g.hrx - 10f), Y(g.hy - 6f), X(g.hx - g.hrx - 3f), Y(g.hy - 10f), tp)
            c.drawLine(X(g.hx + g.hrx + 3f), Y(g.hy - 14f), X(g.hx + g.hrx + 10f), Y(g.hy - 18f), tp)
        }
        if ("halo" in tokens) {
            val top = g.hy - g.hry
            c.drawOval(RectF(X(g.hx - 17f), Y(top - 17f), X(g.hx + 17f), Y(top - 6f)),
                Paper.strokePaint("#f2c230", S(3f), 190))
        }
    }

    // ================= HAIR + HATS (v0.5.0f) =================
    // The hair/hat MASSES are HeadStack ink layers now (pure
    // data in CharacterSystem.kt — padded-ellipse caps and
    // domes, painted by paintKind at the drawHead slots above).
    // What remains hand-drawn here are the stroke-only details
    // that ride ON those masses and have no silhouette of their
    // own: the part line, Butters' curl, the hive's wrap lines,
    // the chullo's weave + strings, the toque pleats, the
    // earmuff band arc and the Mr. Hat M.

    /** Stroke details over the HAIR layers. */
    private fun drawHairOverlays(c: Canvas, spec: SheetSpec, g: Geo,
                                 x0: Float, y0: Float, s: Float) {
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val top = g.hy - g.hry
        val fringe = g.eyeT - 6f
        when (spec.hairStyle) {
            "sidepart" ->
                // the part line, a shade of the hair itself
                c.drawLine(X(g.hx - 12f), Y(top + 2f), X(g.hx - 8f), Y(fringe + 4f),
                    Paper.strokePaint(hexOf(Paper.darker(spec.hairColor)), S(2f)))
            "swirl" ->
                // Butters' single curl standing up top-center
                c.drawArc(RectF(X(g.hx - 6f), Y(top - 13f), X(g.hx + 6f), Y(top - 1f)),
                    40f, 300f, false, Paper.strokePaint(spec.hairColor, S(3.5f)))
            "beehive" -> {
                // wrap lines around the hive
                val wp = Paper.strokePaint(hexOf(Paper.darker(spec.hairColor)), S(2f))
                for (i in 0..2) {
                    val wy = top - g.hry * 0.52f + i * g.hry * 0.28f
                    c.drawLine(X(g.hx - g.hrx * 0.42f), Y(wy), X(g.hx + g.hrx * 0.42f), Y(wy), wp)
                }
            }
        }
    }

    /** Stroke details over the HAT layers. */
    private fun drawHatOverlays(c: Canvas, spec: SheetSpec, g: Geo,
                                x0: Float, y0: Float, s: Float) {
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val top = g.hy - g.hry
        when (spec.hatId) {
            "mr_hat_cap" -> {
                // the little white M on the cap's front panel
                val mp2 = Paper.textPaint("#f0f0f0", S(10f), bold = true)
                mp2.textAlign = Paint.Align.CENTER
                c.drawText("M", X(g.hx), Y(g.eyeT - 11f), mp2)
            }
            "chullo" -> {
                // THIN VERTICAL WEAVE STRIPS down each flap, in
                // the accent — flat color, no texture
                val stp3 = Paper.strokePaint(spec.hatAccent, S(1.6f), 210)
                for (fx0 in listOf(g.hx - g.hrx + 2.5f, g.hx - g.hrx + 7.5f)) {
                    c.drawLine(X(fx0), Y(g.eyeT - 1f), X(fx0), Y(g.eyeB + 2f), stp3)
                }
                for (fx0 in listOf(g.hx + g.hrx - 7.5f, g.hx + g.hrx - 2.5f)) {
                    c.drawLine(X(fx0), Y(g.eyeT - 1f), X(fx0), Y(g.eyeB + 2f), stp3)
                }
                // the hanging strings off the flaps
                val sp2 = Paper.strokePaint(spec.hatColor, S(2f))
                c.drawLine(X(g.hx - g.hrx + 5f), Y(g.eyeB + 4f), X(g.hx - g.hrx + 3f), Y(g.eyeB + 14f), sp2)
                c.drawLine(X(g.hx + g.hrx - 5f), Y(g.eyeB + 4f), X(g.hx + g.hrx - 3f), Y(g.eyeB + 14f), sp2)
            }
            "chefhat" -> {
                // the toque's pleats
                val pp = Paper.strokePaint("#d8d8d8", S(2f))
                for (i in -1..1) {
                    c.drawLine(X(g.hx + i * g.hrx * 0.3f), Y(top - g.hry * 0.57f),
                        X(g.hx + i * g.hrx * 0.3f), Y(top), pp)
                }
            }
            "earmuffs" ->
                // the band arc over the crown (hair shows under it)
                c.drawArc(RectF(X(g.hx - g.hrx * 0.94f), Y(top - g.hry * 0.16f),
                    X(g.hx + g.hrx * 0.94f), Y(g.hy + g.hry * 0.35f)),
                    180f, 180f, false, Paper.strokePaint(spec.hatColor, S(4.5f)))
        }
    }

    // ================= HALLOWEEN COSTUMES =================
    private fun drawCostume(c: Canvas, spec: SheetSpec, costume: String,
                            cx: Float, cy: Float, h: Float) {
        val tall = spec.tall
        val boxH = if (tall) 150f else 128f
        // costume anchors ride the character's OWN derived geometry
        val g: Geo = FigureGeometry.derive(spec)
        val s = h / boxH
        val x0 = cx - 50f * s
        val y0 = cy - boxH / 2f * s
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        when (costume) {
            "ghost" -> {
                // white sheet over everything, wavy bottom, dark eye holes
                val sheet = Paper.fillPaint("#f4f4f4")
                c.drawPath(Path().apply {
                    moveTo(X(6f), Y(8f))
                    quadTo(X(50f), Y(0f), X(94f), Y(8f))
                    lineTo(X(94f), Y(boxH - 10f))
                    var px = 94f
                    var up = true
                    while (px > 12f) {
                        val nx = (px - 13f).coerceAtLeast(6f)
                        quadTo(X((px + nx) / 2f), Y(boxH - 10f + if (up) 8f else -2f), X(nx), Y(boxH - 10f))
                        px = nx; up = !up
                    }
                    close()
                }, sheet)
                val ep = Paper.fillPaint("#1a1a1a")
                c.drawOval(RectF(X(g.eyeL + 2f), Y(g.eyeT + 2f), X(50f - 2f), Y(g.eyeB - 2f)), ep)
                c.drawOval(RectF(X(50f + 2f), Y(g.eyeT + 2f), X(g.eyeR - 2f), Y(g.eyeB - 2f)), ep)
                // "BOO!" no — mouth hole
                c.drawOval(RectF(X(44f), Y(g.mouthY - 2f), X(56f), Y(g.mouthY + 8f)), ep)
            }
            "vampire" -> {
                // cape wings at the sides
                val cape = Paper.fillPaint("#1a1a1a")
                c.drawPath(Path().apply {
                    moveTo(X(28f), Y(70f)); lineTo(X(8f), Y(boxH - 8f)); lineTo(X(30f), Y(boxH - 8f)); close()
                }, cape)
                c.drawPath(Path().apply {
                    moveTo(X(72f), Y(70f)); lineTo(X(92f), Y(boxH - 8f)); lineTo(X(70f), Y(boxH - 8f)); close()
                }, cape)
                // high collar
                c.drawPath(Path().apply {
                    moveTo(X(28f), Y(66f)); lineTo(X(20f), Y(48f)); lineTo(X(38f), Y(62f)); close()
                }, cape)
                c.drawPath(Path().apply {
                    moveTo(X(72f), Y(66f)); lineTo(X(80f), Y(48f)); lineTo(X(62f), Y(62f)); close()
                }, cape)
                // fangs over the mouth
                val fp = Paper.fillPaint("#ffffff")
                c.drawPath(Path().apply {
                    moveTo(X(44f), Y(g.mouthY - 4f)); lineTo(X(48f), Y(g.mouthY - 4f))
                    lineTo(X(46f), Y(g.mouthY + 5f)); close()
                }, fp)
                c.drawPath(Path().apply {
                    moveTo(X(52f), Y(g.mouthY - 4f)); lineTo(X(56f), Y(g.mouthY - 4f))
                    lineTo(X(54f), Y(g.mouthY + 5f)); close()
                }, fp)
            }
            "witch" -> {
                // black witch hat over the head — brim follows the
                // derived head width, cone scales with the head
                val hp = Paper.fillPaint("#2a2a2a")
                val top = g.hy - g.hry
                c.drawOval(RectF(X(g.hx - g.hrx - 6f), Y(top - 2f),
                    X(g.hx + g.hrx + 6f), Y(top + 10f)), hp)
                c.drawPath(Path().apply {
                    moveTo(X(g.hx - g.hrx * 0.52f), Y(top + 2f))
                    quadTo(X(g.hx - g.hrx * 0.24f), Y(top - g.hry * 1.24f), X(g.hx + g.hrx * 0.24f), Y(top - g.hry * 1.14f))
                    lineTo(X(g.hx + g.hrx * 0.52f), Y(top + 2f))
                    close()
                }, hp)
                c.drawRect(RectF(X(g.hx - g.hrx * 0.52f), Y(top - g.hry * 0.29f),
                    X(g.hx + g.hrx * 0.52f), Y(top - g.hry * 0.14f)),
                    Paper.fillPaint("#8a2a9a"))
            }
            "mask" -> {
                // white paper eye-mask band with black outline
                c.drawRoundRect(RectF(X(22f), Y(g.eyeT - 8f), X(78f), Y(g.eyeB + 8f)),
                    S(10f), S(10f), Paper.fillPaint("#f4f4f4"))
                c.drawRoundRect(RectF(X(22f), Y(g.eyeT - 8f), X(78f), Y(g.eyeB + 8f)),
                    S(10f), S(10f), Paper.strokePaint("#1a1a1a", S(2f)))
                val ep = Paper.fillPaint("#1a1a1a")
                c.drawOval(RectF(X(g.eyeL + 4f), Y(g.eyeT + 4f), X(50f - 4f), Y(g.eyeB - 4f)), ep)
                c.drawOval(RectF(X(50f + 4f), Y(g.eyeT + 4f), X(g.eyeR - 4f), Y(g.eyeB - 4f)), ep)
            }
        }
    }

    /** v0.10.0b: draw a figure IN HERO COSTUME. The cape is
     *  in the spec already (Heroes.costumeSpec turned the
     *  sheet's back item into a cape in the persona's color);
     *  this pass adds what a spec can't say — the colored
     *  mask band over the eyes and the emblem on the chest,
     *  both riding the figure's own derived geometry, in the
     *  costume code's colors ("cape|mask|emblem"). Used for
     *  cast in the street (drawKidHero) and for ANY figure in
     *  a photo record (PhotoView), so the street and the
     *  album render the same truth. */
    fun drawHeroFigure(c: Canvas, spec: SheetSpec, code: String,
                       cx: Float, cy: Float, h: Float,
                       mood: String = "idle", pose: String = "stand",
                       anim: FigureAnim = FigureAnim(),
                       doll: Boolean = false) {
        val parsed = Heroes.parseCode(code)
        drawFigure(c, if (parsed != null)
            Heroes.costumeSpec(spec, code) else spec,
            cx, cy, h, mood, pose, anim, doll)
        if (parsed == null) return
        val (_, mask, emblem) = parsed
        if (spec.width == 1f) {
            drawHeroOverlay(c, spec, mask, emblem, cx, cy, h,
                anim.facing)
        } else {
            c.save()
            c.translate(cx, cy)
            c.scale(spec.width, 1f)
            c.translate(-cx, -cy)
            drawHeroOverlay(c, spec, mask, emblem, cx, cy, h,
                anim.facing)
            c.restore()
        }
    }

    /** v0.12.0d: draw a figure IN SLEEPWEAR. Everything
     *  rides the spec (Sleepwear.sleepSpec turned the
     *  sheet into its pajamas — softened own colors, hats
     *  off or a nightcap in the ink) — there is no overlay
     *  to add: bedtime is a change of clothes, not a badge.
     *  Used for ANY figure in a photo record (PhotoView),
     *  so the album, the street re-render and the darkroom
     *  all draw the same night. A bad code draws street
     *  clothes, exactly like a bad hero code. */
    fun drawSleepFigure(c: Canvas, spec: SheetSpec, code: String,
                        cx: Float, cy: Float, h: Float,
                        mood: String = "idle", pose: String = "stand",
                        anim: FigureAnim = FigureAnim(),
                        doll: Boolean = false) {
        val parsed = Sleepwear.parseCode(code)
        drawFigure(c, if (parsed != null)
            Sleepwear.sleepSpec(spec, code) else spec,
            cx, cy, h, mood, pose, anim, doll)
    }

    /** A named cast kid in their hero costume, street pass. */
    fun drawKidHero(c: Canvas, name: String, cx: Float, cy: Float,
                    h: Float, mood: String = "idle",
                    pose: String = "stand",
                    anim: FigureAnim = FigureAnim()) {
        val spec = SHEETS[name] ?: SheetSpec()
        drawHeroFigure(c, spec, Heroes.costumeCodeFor(name),
            cx, cy, h, mood, pose, anim)
    }

    private fun drawHeroOverlay(c: Canvas, spec: SheetSpec,
                                maskHex: String, emblem: String,
                                cx: Float, cy: Float, h: Float,
                                facing: Int = 0) {
        val tall = spec.tall
        val boxH = if (tall) 150f else 128f
        val g: Geo = FigureGeometry.derive(spec)
        val s = h / boxH
        val x0 = cx - 50f * s
        val y0 = cy - boxH / 2f * s
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        // v0.16.0c (item 18 closed): a hero in profile wears
        // a PROFILE mask and emblem — the front band across
        // both eyes is never painted over a turned head.
        if (facing != 0) {
            drawHeroProfileOverlay(c, spec, maskHex, emblem,
                cx, cy, h, facing)
            return
        }
        // the mask: a band in the persona's mask color across
        // the eyes, holes where the eyes look out
        c.drawRoundRect(RectF(X(g.eyeL - 7f), Y(g.eyeT - 6f),
            X(g.eyeR + 7f), Y(g.eyeB + 6f)),
            S(9f), S(9f), Paper.fillPaint(maskHex))
        c.drawRoundRect(RectF(X(g.eyeL - 7f), Y(g.eyeT - 6f),
            X(g.eyeR + 7f), Y(g.eyeB + 6f)),
            S(9f), S(9f), Paper.strokePaint("#1a1a1a", S(1.6f)))
        val ep = Paper.fillPaint("#1a1a1a")
        c.drawOval(RectF(X(g.eyeL + 3f), Y(g.eyeT + 3f),
            X(50f - 3f), Y(g.eyeB - 3f)), ep)
        c.drawOval(RectF(X(50f + 3f), Y(g.eyeT + 3f),
            X(g.eyeR - 3f), Y(g.eyeB - 3f)), ep)
        drawHeroEmblem(c, spec, maskHex, emblem, ::X, ::Y, ::S)
    }

    /** The hero emblem on the chest, in the mask color
     *  with a dark edge — a kid drew this, and it shows,
     *  proudly. Shared by the front overlay (identity
     *  mapping) and the v0.16.0c profile overlay (the
     *  same badge, foreshortened at the chest's facing
     *  edge), so a hero wears ONE badge from every side.
     *  [X]/[Y]/[S] map unit-box coords onto the caller's
     *  canvas. */
    private fun drawHeroEmblem(c: Canvas, spec: SheetSpec,
                               maskHex: String, emblem: String,
                               X: (Float) -> Float,
                               Y: (Float) -> Float,
                               S: (Float) -> Float) {
        val chestY = if (spec.tall) 92f else 97f
        val r = 6.5f
        val ink = Paper.fillPaint(maskHex)
        val edge = Paper.strokePaint("#1a1a1a", S(1.4f))
        fun badge(draw: () -> Unit) { draw(); }
        when (emblem) {
            "star" -> {
                val pts = Path()
                for (i in 0 until 10) {
                    val ang = -Math.PI / 2 + i * Math.PI / 5
                    val rad = if (i % 2 == 0) r else r * 0.45f
                    val px = 50f + (Math.cos(ang) * rad).toFloat()
                    val py = chestY + (Math.sin(ang) * rad).toFloat()
                    if (i == 0) pts.moveTo(X(px), Y(py))
                    else pts.lineTo(X(px), Y(py))
                }
                pts.close()
                badge { c.drawPath(pts, ink); c.drawPath(pts, edge) }
            }
            "bolt" -> {
                val pts = Path().apply {
                    moveTo(X(53f), Y(chestY - r))
                    lineTo(X(45f), Y(chestY + 1f))
                    lineTo(X(49.5f), Y(chestY + 1f))
                    lineTo(X(47f), Y(chestY + r))
                    lineTo(X(55f), Y(chestY - 1f))
                    lineTo(X(50.5f), Y(chestY - 1f))
                    close()
                }
                badge { c.drawPath(pts, ink); c.drawPath(pts, edge) }
            }
            "moon" -> {
                badge {
                    c.drawCircle(X(50f), Y(chestY), S(r), ink)
                    c.drawCircle(X(52.5f), Y(chestY - 1.5f),
                        S(r * 0.78f), Paper.fillPaint(spec.top))
                }
            }
            "shield" -> {
                val pts = Path().apply {
                    moveTo(X(50f - r), Y(chestY - r * 0.7f))
                    lineTo(X(50f + r), Y(chestY - r * 0.7f))
                    lineTo(X(50f + r), Y(chestY + r * 0.2f))
                    quadTo(X(50f + r), Y(chestY + r),
                        X(50f), Y(chestY + r * 1.15f))
                    quadTo(X(50f - r), Y(chestY + r),
                        X(50f - r), Y(chestY + r * 0.2f))
                    close()
                }
                badge { c.drawPath(pts, ink); c.drawPath(pts, edge) }
            }
            "paw" -> {
                badge {
                    c.drawOval(RectF(X(50f - r * 0.62f),
                        Y(chestY - r * 0.1f), X(50f + r * 0.62f),
                        Y(chestY + r * 0.95f)), ink)
                    for (dx in listOf(-0.78f, 0f, 0.78f)) {
                        c.drawCircle(X(50f + dx * r),
                            Y(chestY - r * 0.55f), S(r * 0.3f), ink)
                    }
                }
            }
            "sun" -> {
                badge {
                    val sp = Paper.strokePaint(maskHex, S(1.8f))
                    for (i in 0 until 8) {
                        val ang = i * Math.PI / 4
                        c.drawLine(
                            X(50f + (Math.cos(ang) * r * 0.75f).toFloat()),
                            Y(chestY + (Math.sin(ang) * r * 0.75f).toFloat()),
                            X(50f + (Math.cos(ang) * r * 1.2f).toFloat()),
                            Y(chestY + (Math.sin(ang) * r * 1.2f).toFloat()), sp)
                    }
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.62f), ink)
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.62f), edge)
                }
            }
            "wave" -> {
                badge {
                    val sp = Paper.strokePaint(maskHex, S(2f))
                    for (dy in listOf(-2.6f, 1.8f)) {
                        val pts = Path().apply {
                            moveTo(X(50f - r), Y(chestY + dy))
                            quadTo(X(50f - r * 0.4f),
                                Y(chestY + dy - 3.4f),
                                X(50f), Y(chestY + dy))
                            quadTo(X(50f + r * 0.4f),
                                Y(chestY + dy + 3.4f),
                                X(50f + r), Y(chestY + dy))
                        }
                        c.drawPath(pts, sp)
                    }
                }
            }
            "crown" -> {
                val pts = Path().apply {
                    moveTo(X(50f - r), Y(chestY + r * 0.6f))
                    lineTo(X(50f - r), Y(chestY - r * 0.5f))
                    lineTo(X(50f - r * 0.45f), Y(chestY + r * 0.05f))
                    lineTo(X(50f), Y(chestY - r * 0.85f))
                    lineTo(X(50f + r * 0.45f), Y(chestY + r * 0.05f))
                    lineTo(X(50f + r), Y(chestY - r * 0.5f))
                    lineTo(X(50f + r), Y(chestY + r * 0.6f))
                    close()
                }
                badge { c.drawPath(pts, ink); c.drawPath(pts, edge) }
            }
            "eye" -> {
                badge {
                    c.drawOval(RectF(X(50f - r), Y(chestY - r * 0.62f),
                        X(50f + r), Y(chestY + r * 0.62f)), ink)
                    c.drawOval(RectF(X(50f - r), Y(chestY - r * 0.62f),
                        X(50f + r), Y(chestY + r * 0.62f)), edge)
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.3f),
                        Paper.fillPaint("#1a1a1a"))
                }
            }
            "gear" -> {
                badge {
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.8f),
                        Paper.strokePaint(maskHex, S(2.6f)))
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.3f), ink)
                }
            }
            "comet" -> {
                badge {
                    val sp = Paper.strokePaint(maskHex, S(1.8f))
                    c.drawLine(X(50f + 1f), Y(chestY + 1f),
                        X(50f + r + 2f), Y(chestY + r + 2f), sp)
                    c.drawLine(X(50f - 1f), Y(chestY + 2.5f),
                        X(50f + r), Y(chestY + r + 3.5f), sp)
                    c.drawCircle(X(50f - 1.5f), Y(chestY - 1.5f),
                        S(r * 0.55f), ink)
                    c.drawCircle(X(50f - 1.5f), Y(chestY - 1.5f),
                        S(r * 0.55f), edge)
                }
            }
            "drop" -> {
                val pts = Path().apply {
                    moveTo(X(50f), Y(chestY - r))
                    quadTo(X(50f + r * 0.85f), Y(chestY + r * 0.3f),
                        X(50f + r * 0.55f), Y(chestY + r * 0.75f))
                    quadTo(X(50f), Y(chestY + r * 1.05f),
                        X(50f - r * 0.55f), Y(chestY + r * 0.75f))
                    quadTo(X(50f - r * 0.85f), Y(chestY + r * 0.3f),
                        X(50f), Y(chestY - r))
                    close()
                }
                badge { c.drawPath(pts, ink); c.drawPath(pts, edge) }
            }
            else -> {
                badge {
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.7f), ink)
                    c.drawCircle(X(50f), Y(chestY), S(r * 0.7f), edge)
                }
            }
        }
    }

    /** How flat the hero emblem reads in profile: the
     *  badge sits on the chest's facing edge, squashed
     *  to this fraction of its front width — a badge
     *  seen from the side, not a billboard. */
    const val HERO_EMBLEM_FORESHORTEN = 0.55f

    /** The profile hero overlay's geometry, in the
     *  figure's unit box, for facing [f] (−1 left,
     *  +1 right): the mask band wrapping the turned
     *  head from the nose side past the ear, the one
     *  pupil looking out of it, and the emblem's seat
     *  at the chest's facing edge. Pure (derived from
     *  FigureGeometry alone) — the suite pins it for
     *  both facings. */
    data class HeroProfileGeom(
        val maskL: Float, val maskR: Float,
        val maskT: Float, val maskB: Float,
        val pupilCx: Float, val pupilCy: Float,
        val pupilRx: Float, val pupilRy: Float,
        val emblemCx: Float, val emblemCy: Float
    )

    fun heroProfileGeom(spec: SheetSpec, f: Int): HeroProfileGeom {
        val d = FigureGeometry.derive(spec)
        val a = FigureGeometry.profileAnchors(d, f)
        val chestY = if (spec.tall) 92f else 97f
        val front = a.eyeX + f * 11f
        val back = a.earX - f * 7f
        return HeroProfileGeom(
            maskL = minOf(front, back), maskR = maxOf(front, back),
            maskT = d.eyeT - 6f, maskB = d.eyeB + 6f,
            pupilCx = a.eyeX, pupilCy = (d.eyeT + d.eyeB) / 2f,
            pupilRx = 4.6f, pupilRy = (d.eyeB - d.eyeT) * 0.30f,
            emblemCx = 50f + f *
                FigureGeometry.torsoHalfAt(d.plan, chestY) * 0.45f,
            emblemCy = chestY)
    }

    /** v0.16.0c (item 18 closed): the hero overlay TURNED
     *  — the mask as one band wrapping the profile head
     *  (nose side to past the ear, the same eye padding
     *  the front band wears), one pupil looking out, and
     *  the shared emblem foreshortened at the chest's
     *  facing edge. The cape needs no overlay: it is
     *  back-item geometry (Heroes.costumeSpec) and the
     *  profile pass already draws it behind the torso. */
    private fun drawHeroProfileOverlay(c: Canvas, spec: SheetSpec,
                                       maskHex: String, emblem: String,
                                       cx: Float, cy: Float, h: Float,
                                       facing: Int) {
        val f = if (facing < 0) -1 else 1
        val boxH = if (spec.tall) 150f else 128f
        val s = h / boxH
        val x0 = cx - 50f * s
        val y0 = cy - boxH / 2f * s
        fun X(x: Float) = x0 + x * s
        fun Y(y: Float) = y0 + y * s
        fun S(v: Float) = v * s
        val pg = heroProfileGeom(spec, f)
        val band = RectF(X(pg.maskL), Y(pg.maskT),
            X(pg.maskR), Y(pg.maskB))
        c.drawRoundRect(band, S(9f), S(9f), Paper.fillPaint(maskHex))
        c.drawRoundRect(band, S(9f), S(9f),
            Paper.strokePaint("#1a1a1a", S(1.6f)))
        c.drawOval(RectF(X(pg.pupilCx - pg.pupilRx),
            Y(pg.pupilCy - pg.pupilRy),
            X(pg.pupilCx + pg.pupilRx),
            Y(pg.pupilCy + pg.pupilRy)),
            Paper.fillPaint("#1a1a1a"))
        c.save()
        c.translate(X(pg.emblemCx) - HERO_EMBLEM_FORESHORTEN * X(50f), 0f)
        c.scale(HERO_EMBLEM_FORESHORTEN, 1f)
        drawHeroEmblem(c, spec, maskHex, emblem, ::X, ::Y, ::S)
        c.restore()
    }

    /** "#rrggbb" from an android color int. */
    private fun hexOf(color: Int): String = "#%06X".format(0xFFFFFF and color)
}

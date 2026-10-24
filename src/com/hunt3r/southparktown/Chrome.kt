package com.hunt3r.southparktown

import kotlin.math.pow

// ============================================================
// Chrome — the games' UI language as tokens + layout math
// (v0.1.9b). PURE Kotlin, no android imports: the JVM smoke
// suite pins the layout invariants here (text fitting,
// vertical stacking, bubble geometry, name-tag fitting,
// contrast pairs) so the parity restyle can never reintroduce
// the v0.1.6 overlap class of bug.
//
// The visual language — Stick of Truth / Fractured But Whole
// menus, redrawn in this app's flat paper discipline:
// charcoal grounds, parchment/cardstock panels with a darker
// edge line, chunky red primary buttons, yellow accents, bold
// casual lettering, pieces glued on a hair crooked. The
// Android-side view builders live in UiKit.kt; Paper.kt owns
// the Canvas-side painting.
// ============================================================
object Chrome {
    // ---- palette (mirrors Paper.kt) ----
    const val CHARCOAL = "#1e1e1e"
    const val PANEL = "#2e2e2e"
    const val RED = "#d43d2a"
    const val YELLOW = "#f2c230"
    const val PARCHMENT = "#f0e8d0"
    const val PARCHMENT_EDGE = "#c9b98a"
    const val PAPER_WHITE = "#fffdf4"
    const val INK = "#1a1a1a"
    const val INK_SOFT = "#5a4a3a"

    // ---- shape tokens (dp) ----
    const val CARD_RADIUS_DP = 8f
    const val PLAQUE_RADIUS_DP = 8f
    const val BUTTON_RADIUS_DP = 10f
    const val CARD_STROKE_DP = 2f
    const val BUTTON_STROKE_DP = 2.5f
    /** "Glued on crooked" — the tilt for title plaques. */
    const val TILT_DEG = -1.2f

    // ---- top chrome stacking (dp, GameActivity) ----
    // Ticker strip at the very top, location/time/weather bar
    // under it, quest chip under that; GameView's in-scene
    // overlays stack below the chip (chromeTop). These numbers
    // ARE the layout — activities must use them, not re-derive.
    const val TICKER_TOP_DP = 4
    const val TOPBAR_TOP_DP = 32
    const val CHIP_TOP_DP = 92
    const val TICKER_MIN_H_DP = 20   // 12sp line + 2+2 padding
    const val TOPBAR_MIN_H_DP = 30   // 17sp title + 4+4 padding
    const val CHIP_MIN_H_DP = 27     // 12.5sp line + 5+5 padding
    /** GameView's fallback chrome height (dp) before measuring. */
    const val CHROME_FALLBACK_DP = 124f

    // ---- in-view overlay geometry (raw px, as GameView draws) ----
    const val EVENT_STRIP_H_PX = 26f
    const val EVENT_STRIP_TEXT_DY_PX = 18f
    /** Bubble top offset when the event strip is showing:
     *  the strip (26) + a 10px breathing gap. */
    const val BUBBLE_GAP_EVENT_PX = 36f
    const val BUBBLE_GAP_PLAIN_PX = 10f
    const val BUBBLE_MARGIN_PX = 16f
    /** First text baseline inside the bubble overlay. */
    const val BUBBLE_TOP_PAD_PX = 28f
    const val BUBBLE_LINE_H_PX = 20f
    /** Baseline-to-baseline for stacked conversation lines. */
    const val BUBBLE_LINE_STEP_PX = 34f

    fun eventStripBottom(chromeTop: Float): Float = chromeTop + EVENT_STRIP_H_PX

    fun bubbleTop(chromeTop: Float, hasEvent: Boolean): Float =
        chromeTop + if (hasEvent) BUBBLE_GAP_EVENT_PX else BUBBLE_GAP_PLAIN_PX

    /** Bubble overlay height for [lineCount] stacked lines —
     *  the formula GameView has shipped since v0.1.5. */
    fun bubbleHeight(lineCount: Int): Float = 30f + lineCount * 2 * BUBBLE_LINE_H_PX

    /** Lowest text baseline + one line height, for [lineCount]. */
    fun bubbleContentBottom(lineCount: Int): Float =
        BUBBLE_TOP_PAD_PX + (lineCount - 1) * BUBBLE_LINE_STEP_PX + BUBBLE_LINE_H_PX

    // ---- accessibility: the system font scale (v0.1.9f) ----
    // Activity text is all TextViews in sp, so Android scales it
    // natively. GameView's Canvas chrome (event strip, speech
    // bubble, name tags) draws raw px text, so it reads
    // Configuration.fontScale itself and routes it through here.
    // Rules that keep the v0.1.6 invariants untouchable:
    //  - the scale is clamped to [0.85, 1.30];
    //  - TEXT follows the clamped scale both ways;
    //  - GEOMETRY (strip/bubble/tag boxes) only ever GROWS
    //    (geoScale >= 1) — boxes never shrink under their text;
    //  - at scale 1.0 every value below is EXACTLY the shipped
    //    constant, so the default layout is byte-identical.
    const val FONT_SCALE_MIN = 0.85f
    const val FONT_SCALE_MAX = 1.30f

    fun fontScaleOf(raw: Float): Float =
        raw.coerceIn(FONT_SCALE_MIN, FONT_SCALE_MAX)

    /** Geometry multiplier for a font scale: >= 1, always. */
    fun geoScale(fontScale: Float): Float = maxOf(1f, fontScale)

    fun eventStripHeight(fontScale: Float): Float =
        EVENT_STRIP_H_PX * geoScale(fontScale)

    fun eventStripTextDy(fontScale: Float): Float =
        EVENT_STRIP_TEXT_DY_PX * geoScale(fontScale)

    /** bubbleTop with the strip at its (possibly grown) height:
     *  the 10px breathing gap under the strip is preserved. */
    fun bubbleTop(chromeTop: Float, hasEvent: Boolean, fontScale: Float): Float =
        chromeTop + if (hasEvent) eventStripHeight(fontScale) +
            (BUBBLE_GAP_EVENT_PX - EVENT_STRIP_H_PX)
        else BUBBLE_GAP_PLAIN_PX

    fun bubbleHeight(lineCount: Int, fontScale: Float): Float =
        bubbleHeight(lineCount) * geoScale(fontScale)

    fun bubbleTopPad(fontScale: Float): Float =
        BUBBLE_TOP_PAD_PX * geoScale(fontScale)

    fun bubbleLineStep(fontScale: Float): Float =
        BUBBLE_LINE_STEP_PX * geoScale(fontScale)

    fun bubbleContentBottom(lineCount: Int, fontScale: Float): Float =
        bubbleTopPad(fontScale) + (lineCount - 1) * bubbleLineStep(fontScale) +
            BUBBLE_LINE_H_PX * geoScale(fontScale)

    // ---- text fitting ----
    /** Shrink-to-fit by dropping trailing characters — the exact
     *  semantics the event strip and bubble overlay have always
     *  used (never returns fewer than [minLen] characters). */
    fun fitText(text: String, maxWidth: Float, minLen: Int = 4,
                measure: (String) -> Float): String {
        var t = text
        while (measure(t) > maxWidth && t.length > minLen) t = t.dropLast(1)
        return t
    }

    /** fitText plus an ellipsis when truncation happened; the
     *  ellipsis itself is counted in the fit. */
    fun ellipsize(text: String, maxWidth: Float, minLen: Int = 4,
                  measure: (String) -> Float): String {
        val fit = fitText(text, maxWidth, minLen, measure)
        if (fit == text) return text
        var t = fit.trimEnd()
        while (t.isNotEmpty() && measure(t + "…") > maxWidth) t = t.dropLast(1).trimEnd()
        return if (t.isEmpty()) fit else t + "…"
    }

    // ---- name tags ----
    const val TAG_MIN_SIZE = 10.5f

    /** Exact mirror of GameView's name-tag fit: shrink the font
     *  in 1px steps down to a floor, then drop TWO characters at
     *  a time and add an ellipsis. [measure] is Paint.measureText
     *  on device and a fake in the JVM suite. Returns the final
     *  size and the final label. */
    fun nameTagFit(tag: String, maxWidth: Float, baseSize: Float,
                   measure: (String, Float) -> Float): Pair<Float, String> {
        var size = baseSize
        while (measure(tag, size) > maxWidth && size > TAG_MIN_SIZE) size -= 1f
        var label = tag
        while (measure(label, size) > maxWidth && label.length > 4) {
            label = label.dropLast(2)
        }
        if (label != tag) label = label.trimEnd() + "…"
        return size to label
    }

    // ---- contrast (pure math; guards the chrome's text pairs) ----
    /** WCAG relative luminance of a #rrggbb color. */
    fun luminance(hex: String): Float {
        fun chan(v: Int): Float {
            val s = v / 255f
            return if (s <= 0.03928f) s / 12.92f
            else ((s + 0.055f) / 1.055f).pow(2.4f)
        }
        val r = hex.substring(1, 3).toInt(16)
        val g = hex.substring(3, 5).toInt(16)
        val b = hex.substring(5, 7).toInt(16)
        return 0.2126f * chan(r) + 0.7152f * chan(g) + 0.0722f * chan(b)
    }

    fun contrast(a: String, b: String): Float {
        val la = luminance(a)
        val lb = luminance(b)
        val hi = maxOf(la, lb)
        val lo = minOf(la, lb)
        return (hi + 0.05f) / (lo + 0.05f)
    }
}

// ============================================================
// BarFit — v0.15.0g: the bottom action bar's fit math.
//
// The bar's seven buttons (LOOK / TALK / SELFIE / PACK /
// JOURNAL / PHONE / MENU) wrapped mid-word on device
// (SELFI-E, JOUR-NAL, PHON-E, MEN-U in Lucky's
// screenshots): a fixed 13sp label in a one-seventh
// share of the screen simply did not fit, so the
// TextView broke the word. The fix in GameActivity is
// structural — setSingleLine(true) + auto-size text
// (MIN_SP..MAX_SP) + zeroed button padding/min-width —
// and this object is its measurable shadow: the shipped
// layout numbers (bar padding, margins, button count)
// and a stated advance model, so the suite can prove
// every label FITS its share on one line at the shipped
// density math, at and above the auto-size floor,
// instead of trusting the claim.
//
// The advance model (casual bold, rendered uppercase by
// the button theme): an emoji or wide symbol advances
// 1.0 em, a space 0.30 em, a letter or digit 0.56 em.
// It is deliberately a touch wide — if the model fits,
// the real face fits with room to spare.
// ============================================================

object BarFit {
    const val MIN_SP = 8
    const val MAX_SP = 14
    const val BUTTONS = 7

    /** The bar's own horizontal padding, both sides
     *  together (GameActivity: dp(2) + dp(2)). */
    const val BAR_PAD_DP = 4f

    /** All seven buttons' horizontal margins together
     *  (dp(1) start + dp(1) end, per button). */
    const val MARGINS_DP = 14f

    /** One button's own horizontal padding, both sides
     *  (zero in GameActivity's barButton — the whole
     *  share belongs to the label). */
    const val INNER_PAD_DP = 0f

    /** One button's share of a [screenDp]-wide screen. */
    fun buttonWidthDp(screenDp: Float): Float =
        (screenDp - BAR_PAD_DP - MARGINS_DP) / BUTTONS

    /** Estimated advance of [label] at [sp], in dp,
     *  under the model in this file's header. Counts
     *  code points, so an emoji is one glyph. */
    fun estWidthDp(label: String, sp: Float): Float {
        var em = 0f
        var i = 0
        val up = label.uppercase()
        while (i < up.length) {
            val cp = up.codePointAt(i)
            em += when {
                cp == 0x20 -> 0.30f        // space
                cp > 0x2000 -> 1.0f        // emoji / wide symbol (☰ too)
                else -> 0.56f              // letter, digit
            }
            i += Character.charCount(cp)
        }
        return em * sp
    }

    /** The size auto-size would settle on for [label]
     *  on a [screenDp]-wide screen: the largest size in
     *  [MIN_SP]..[MAX_SP] (half-step) whose estimated
     *  advance fits the button's share. MIN_SP when even
     *  the floor is tight — the suite pins that the
     *  floor itself fits, which is what makes the
     *  one-line guarantee real. */
    fun fittedSp(label: String, screenDp: Float): Float {
        val avail = buttonWidthDp(screenDp) - INNER_PAD_DP
        var sp = MAX_SP.toFloat()
        while (sp > MIN_SP && estWidthDp(label, sp) > avail)
            sp -= 0.5f
        return sp
    }

    /** True when [label] fits its share on ONE line at
     *  the auto-size floor on a [screenDp] screen. */
    fun fitsAtFloor(label: String, screenDp: Float): Boolean =
        estWidthDp(label, MIN_SP.toFloat()) <=
            buttonWidthDp(screenDp) - INNER_PAD_DP
}

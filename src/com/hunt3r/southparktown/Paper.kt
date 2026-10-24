package com.hunt3r.southparktown

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

// ============================================================
// Paper — South Park paper-cutout style utilities.
// FLAT colors, visible paper grain, slightly crooked shapes,
// soft offset drop shadows, darker edge strokes (black only
// for eyes/mouth/bubble outlines).
// ============================================================
object Paper {
    // Stick of Truth UI chrome
    const val CHARCOAL = "#1e1e1e"
    const val RED = "#d43d2a"
    const val YELLOW = "#f2c230"
    const val PARCHMENT = "#f0e8d0"

    // Sky bands
    const val SKY_DAY_TOP = "#dfeef7"
    const val SKY_DAY_BOT = "#ffffff"
    const val SKY_DUSK_TOP = "#e8b48a"
    const val SKY_DUSK_BOT = "#f7e8d0"
    const val SKY_NIGHT_TOP = "#1a2340"
    const val SKY_NIGHT_BOT = "#2e3a5e"

    const val MOUNTAIN = "#b8d0e8"
    const val MOUNTAIN_DARK = "#9ab4d4"
    const val SNOW = "#ffffff"
    const val SNOW_SHADOW = "#cfe0f2"
    const val PINE = "#2f5f4f"
    const val PINE_DARK = "#24483d"

    // ---- v0.1.9f perf: pure-function caches ----
    // Hex-to-color parsing is a pure function of its string, and the
    // draw loops below call the paint factories HUNDREDS of times
    // per frame (GameView redraws at 20fps) — re-parsing the same
    // hex strings every call was pure waste. The caches return
    // bit-identical values; paints themselves are still allocated
    // per call because callers legitimately mutate them
    // (textAlign / alpha / textSize), so paints are NOT pooled.
    private val colorCache = HashMap<String, Int>()
    private val shadeCache = HashMap<String, Int>()

    fun colorOf(colorHex: String): Int =
        colorCache.getOrPut(colorHex) { Color.parseColor(colorHex) }

    /** Run [block] rotated [degrees] around (cx, cy) — the "kid glued it crooked" look. */
    inline fun crooked(c: Canvas, degrees: Float, cx: Float, cy: Float, block: () -> Unit) {
        c.save()
        c.rotate(degrees, cx, cy)
        block()
        c.restore()
    }

    /** Soft glued-layer drop shadow: draw [block] first offset in translucent black. */
    inline fun withShadow(c: Canvas, dx: Float = 3f, dy: Float = 4f, alpha: Int = 90, block: () -> Unit) {
        c.save()
        c.translate(dx, dy)
        block()
        c.restore()
    }

    fun fillPaint(colorHex: String, alpha: Int = 255): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.FILL
        p.color = colorOf(colorHex)
        p.alpha = alpha
        return p
    }

    fun fillPaint(color: Int, alpha: Int = 255): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.FILL
        p.color = color
        p.alpha = alpha
        return p
    }

    fun strokePaint(colorHex: String, widthPx: Float, alpha: Int = 255): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = widthPx
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.color = colorOf(colorHex)
        p.alpha = alpha
        return p
    }

    /** Darker edge stroke color for a flat fill (show uses colored darker edges, not black). */
    fun darker(colorHex: String, factor: Float = 0.72f): Int =
        shadeCache.getOrPut("d|$colorHex|$factor") {
            val c = colorOf(colorHex)
            Color.rgb(
                (Color.red(c) * factor).toInt().coerceIn(0, 255),
                (Color.green(c) * factor).toInt().coerceIn(0, 255),
                (Color.blue(c) * factor).toInt().coerceIn(0, 255)
            )
        }

    fun lighter(colorHex: String, factor: Float = 1.18f): Int =
        shadeCache.getOrPut("l|$colorHex|$factor") {
            val c = colorOf(colorHex)
            Color.rgb(
                (Color.red(c) * factor).toInt().coerceIn(0, 255),
                (Color.green(c) * factor).toInt().coerceIn(0, 255),
                (Color.blue(c) * factor).toInt().coerceIn(0, 255)
            )
        }

    fun textPaint(colorHex: String, sizePx: Float, bold: Boolean = false, casual: Boolean = false): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = colorOf(colorHex)
        p.textSize = sizePx
        if (bold) p.isFakeBoldText = true
        if (casual) p.typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
        return p
    }

    fun dp(res: Resources, dp: Float): Float = dp * res.displayMetrics.density

    // ================================================================
    // v0.16.0d — the PaperCut canvas application (spec stages 3, 4,
    // 5, 8). All geometry is computed by PaperCut.kt (pure, suite-
    // pinned); these functions only draw it. The retired per-pixel
    // speckle grain tile is gone whole: a stipple field is exactly
    // what the material rule forbids, and fibrous grain replaces it
    // everywhere (surfaces via applyMaterial, frames via
    // drawFibreGrain).
    // ================================================================

    private fun strokePaintInt(color: Int, widthPx: Float, alpha: Int): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = widthPx
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.color = color
        p.alpha = alpha
        return p
    }

    private val fibreCache = HashMap<String, List<PaperCut.Fibre>>()

    private fun fibreFieldCached(seed: Long, cols: Int, rows: Int):
        List<PaperCut.Fibre> {
        val key = "$seed|$cols|$rows"
        fibreCache[key]?.let { return it }
        if (fibreCache.size > 96) fibreCache.clear()
        val f = PaperCut.fibreField(seed, cols, rows)
        fibreCache[key] = f
        return f
    }

    /** A closed path through cut-contour points. */
    fun cutPath(pts: List<CutPt>): Path {
        val p = Path()
        pts.forEachIndexed { i, pt ->
            if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
        }
        p.close()
        return p
    }

    /** Fill a cut contour (stage 4 silhouettes). */
    fun fillCut(c: Canvas, pts: List<CutPt>, colorHex: String,
                alpha: Int = 255) {
        if (pts.size < 3) return
        c.drawPath(cutPath(pts), fillPaint(colorHex, alpha))
    }

    /** Stage 5 — the PAINTED OUTLINE: a filled ribbon between the
     *  shape's own contour and its seeded offset contour, so the
     *  line's weight varies along its length like a marker's and
     *  the silhouette sits inset inside its own line. Replaces the
     *  uniform-width edge stroke wherever a shape carries an
     *  outline at all. */
    fun paintedOutline(c: Canvas, contour: List<CutPt>, seed: Long,
                       baseW: Float, colorHex: String, alpha: Int = 255) {
        if (contour.size < 3 || baseW <= 0f) return
        val outer = PaperCut.offsetContour(contour, seed, baseW)
        val p = cutPath(outer)
        p.addPath(cutPath(contour.reversed()))
        p.fillType = Path.FillType.EVEN_ODD
        c.drawPath(p, fillPaint(colorHex, alpha))
    }

    private val regionFibreCache = HashMap<String, List<PaperCut.Fibre>>()

    private fun regionFibreFieldCached(region: PaperRegion,
        seed: Long, cols: Int, rows: Int): List<PaperCut.Fibre> {
        if (region == PaperRegion.SURFACE)
            return fibreFieldCached(seed, cols, rows)
        val key = "$region|$seed|$cols|$rows"
        regionFibreCache[key]?.let { return it }
        if (regionFibreCache.size > 96) regionFibreCache.clear()
        val f = PaperCut.regionFibreField(region, seed, cols, rows)
        regionFibreCache[key] = f
        return f
    }

    /** Stage 3 — the procedural construction-paper material:
     *  seeded fibre grain + soft mottling over a surface the
     *  caller has already filled with [baseHex]. Original and
     *  generated in code from PaperCut's fields — no texture
     *  assets, nothing scanned, nothing ripped. Faces and small
     *  detail never come here. [strength] scales the wash.
     *  v0.17.0a: [region] re-parameterises the SAME fields
     *  (spgen §3) — SKIN fine + quiet, CLOTH coarse + woven,
     *  METAL sparse + a sheen pass; SURFACE is the original
     *  behaviour, bit for bit. */
    fun applyMaterial(c: Canvas, l: Float, t: Float, r: Float, b: Float,
                      baseHex: String, seed: Long, strength: Float = 1f,
                      region: PaperRegion = PaperRegion.SURFACE) {
        val wdt = r - l
        val hgt = b - t
        if (wdt < 10f || hgt < 10f || strength <= 0f) return
        val rp = PaperRegions.params(region)
        c.save()
        c.clipRect(l, t, r, b)
        val short = minOf(wdt, hgt)
        // soft mottling first: a few LARGE blotches, lighter and
        // darker than the stock — the uneven dye of the paper,
        // never a pattern. The region's speckle rate scales it
        // (skin and metal carry none of their own).
        val mottleAlpha = if (region == PaperRegion.SURFACE) 1f
            else (rp.speckle / 0.02f).coerceIn(0f, 1f)
        if (mottleAlpha > 0f) {
            for (m in PaperCut.mottleField(seed)) {
                val paint = if (m.light)
                    fillPaint(lighter(baseHex, 1.09f),
                        (10 * strength * mottleAlpha).toInt()
                            .coerceIn(0, 255))
                else
                    fillPaint(darker(baseHex, 0.91f),
                        (10 * strength * mottleAlpha).toInt()
                            .coerceIn(0, 255))
                val mx = l + m.x * wdt
                val my = t + m.y * hgt
                c.drawOval(RectF(mx - m.r * short, my - m.r * short * 0.72f,
                    mx + m.r * short, my + m.r * short * 0.72f), paint)
            }
        }
        // then the fibres: exactly one per jittered grid cell
        // (PaperCut's construction guarantee — fibres can never
        // cluster), batched into two stroke paths
        val cell = 30f
        val cols = (wdt / cell).toInt().coerceIn(1, 48)
        val rows = (hgt / cell).toInt().coerceIn(1, 48)
        val fibres = regionFibreFieldCached(region,
            seed * 3 + 11, cols, rows)
        val dark = Path()
        val light = Path()
        val cw = wdt / cols
        val chh = hgt / rows
        for (f in fibres) {
            val cxp = l + f.cx * cw
            val cyp = t + f.cy * chh
            val hx = f.dx * f.len * cw * 0.5f
            val hy = f.dy * f.len * chh * 0.5f
            val path = if (f.light) light else dark
            path.moveTo(cxp - hx, cyp - hy)
            path.lineTo(cxp + hx, cyp + hy)
        }
        c.drawPath(dark, strokePaintInt(darker(baseHex, 0.82f), 1.3f,
            (16 * strength * rp.fibreAlphaMul).toInt()
                .coerceIn(0, 255)))
        c.drawPath(light, strokePaintInt(lighter(baseHex, 1.13f), 1.3f,
            (13 * strength * rp.fibreAlphaMul).toInt()
                .coerceIn(0, 255)))
        // metal's hint of edge sheen: one pale diagonal band
        // across the top of the surface, nothing more
        if (rp.sheen) {
            val sheen = Path().apply {
                moveTo(l, t + hgt * 0.30f)
                lineTo(l + wdt * 0.55f, t)
                lineTo(l + wdt * 0.80f, t)
                lineTo(l, t + hgt * 0.52f)
                close()
            }
            c.drawPath(sheen, fillPaint("#ffffff",
                (14 * strength).toInt().coerceIn(0, 255)))
        }
        c.restore()
    }

    /** The OutlineEngine's canvas hand (v0.17.0a): a painted
     *  outline whose ribbon widths come from the contour's
     *  own curvature, computed fresh in THIS render context
     *  (never drawn at one scale and stretched to another).
     *  Same ribbon construction as [paintedOutline]; only
     *  the widths are the engine's. */
    fun paintedOutlineCurved(c: Canvas, contour: List<CutPt>,
                             seed: Long, colorHex: String,
                             alpha: Int = 255,
                             detailBias: Float =
                                 OutlineEngine.DETAIL_BIAS) {
        if (contour.size < 3) return
        val widths = OutlineEngine.offsets(contour, seed,
            detailBias = detailBias)
        val outer = PaperCut.offsetContourW(contour, widths)
        val p = cutPath(outer)
        p.addPath(cutPath(contour.reversed()))
        p.fillType = Path.FillType.EVEN_ODD
        c.drawPath(p, fillPaint(colorHex, alpha))
    }

    /** Stage 8 — the post pass's film grain: the same fibre
     *  construction as the paper material, laid over the finished
     *  frame at whisper alpha — fibrous, never stippled. */
    fun drawFibreGrain(c: Canvas, l: Float, t: Float, r: Float, b: Float,
                       seed: Long, alpha: Int = PaperCut.GRAIN_ALPHA) {
        val wdt = r - l
        val hgt = b - t
        if (wdt < 10f || hgt < 10f) return
        val cols = (wdt / 52f).toInt().coerceIn(1, 40)
        val rows = (hgt / 52f).toInt().coerceIn(1, 40)
        val fibres = fibreFieldCached(seed * 7 + 3, cols, rows)
        val cw = wdt / cols
        val chh = hgt / rows
        val dark = Path()
        val light = Path()
        for (f in fibres) {
            val cxp = l + f.cx * cw
            val cyp = t + f.cy * chh
            val hx = f.dx * f.len * cw * 0.5f
            val hy = f.dy * f.len * chh * 0.5f
            val path = if (f.light) light else dark
            path.moveTo(cxp - hx, cyp - hy)
            path.lineTo(cxp + hx, cyp + hy)
        }
        c.drawPath(dark, strokePaint("#14100c", 1.1f, alpha))
        c.drawPath(light, strokePaint("#ffffff", 1.1f, alpha))
    }

    /** Stage 8 — the vignette: three stepped paper-dark bands at
     *  the frame's edges. Canvas-cheap — no shader, no offscreen
     *  pass, no per-pixel work. */
    fun drawVignette(c: Canvas, l: Float, t: Float, r: Float, b: Float,
                     alpha: Int = PaperCut.VIGNETTE_ALPHA) {
        val wdt = r - l
        val hgt = b - t
        for (i in 0..2) {
            val a = (alpha * (1f - i * 0.3f)).toInt()
            val p = fillPaint("#0c0a08", a)
            val ix = wdt * 0.035f * i
            val iy = hgt * 0.035f * i
            val bw2 = wdt * 0.035f
            val bh2 = hgt * 0.035f
            c.drawRect(l + ix, t + iy, r - ix, t + iy + bh2, p)
            c.drawRect(l + ix, b - iy - bh2, r - ix, b - iy, p)
            c.drawRect(l + ix, t + iy + bh2, l + ix + bw2, b - iy - bh2, p)
            c.drawRect(r - ix - bw2, t + iy + bh2, r - ix, b - iy - bh2, p)
        }
    }

    /** Crooked paper sign with the location name. */
    fun drawSign(c: Canvas, cx: Float, cy: Float, w: Float, h: Float, text: String, textSizePx: Float) {
        val bg = fillPaint(PARCHMENT)
        val edge = strokePaint("#c9b98a", 2f)
        crooked(c, -2f, cx, cy) {
            val r = RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)
            // shadow layer
            val sh = fillPaint("#000000", 70)
            c.drawRoundRect(RectF(r.left + 3, r.top + 4, r.right + 3, r.bottom + 4), 6f, 6f, sh)
            c.drawRoundRect(r, 6f, 6f, bg)
            c.drawRoundRect(r, 6f, 6f, edge)
            // v0.16.0b: the words must live INSIDE the board —
            // shrink the text to fit the board's width, and if
            // it still cannot fit at the floor size, ellipsize
            // it whole rather than letting it spill past the
            // parchment edges (the floating-fragment class,
            // playtest (d): "hal", "achool", cut-off labels)
            var size = textSizePx
            val tp = textPaint("#3a2a1a", size, bold = true, casual = true)
            tp.textAlign = Paint.Align.CENTER
            val maxW = w * 0.92f
            val tw = tp.measureText(text)
            var shown = text
            if (tw > maxW && tw > 0f) {
                val scaled = textSizePx * (maxW / tw)
                if (scaled >= textSizePx * 0.55f) {
                    size = scaled
                    tp.textSize = size
                } else {
                    size = textSizePx * 0.55f
                    tp.textSize = size
                    while (shown.length > 4 &&
                        tp.measureText(shown + "…") > maxW) {
                        shown = shown.dropLast(1)
                    }
                    shown = shown.trimEnd() + "…"
                }
            }
            c.drawText(shown, cx, cy + size * 0.35f, tp)
        }
    }
}

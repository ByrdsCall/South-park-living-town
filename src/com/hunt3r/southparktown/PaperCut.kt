package com.hunt3r.southparktown

import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ============================================================
// PaperCut — v0.16.0d: the Stick-of-Truth pipeline's geometry
// core, re-derived for our 2D canvas engine (spec stages 3, 4,
// 5 and 8; see sot-pipeline-spec.md and the README ledger).
//
// Everything here is PURE (no android types): the JVM suite
// pins determinism, variance and spacing directly against
// these functions, and the Canvas application lives in
// Paper.kt (fillCut / paintedOutline / applyMaterial /
// drawPostGrain), which only ever draws what this file
// computes.
//
// Three techniques, all seeded and deterministic:
//
//  - PAPER MATERIAL (stage 3): a fibre field + a mottle
//    field. The fibre field is a JITTERED GRID — exactly one
//    fibre per cell, centred in its cell with bounded jitter —
//    so fibres can never cluster: the minimum centre gap is
//    a construction guarantee, not a hope. That is the
//    trypophobia rule enforced geometrically: paper fibre
//    and soft blotches at scene scale, never a stipple field,
//    never clustered dots or holes.
//
//  - SCISSOR-CUT CONTOURS (stage 4): shape contours sampled
//    with a slight seeded radial/perpendicular wobble and
//    genuinely rounded corners. The wobble is a pure function
//    of (seed, point index) — the same character or building
//    is cut the same way every frame. Boil (stage 8) never
//    re-rolls an edge; it only translates finished linework.
//
//  - PAINTED OUTLINES (stage 5): the outline is a filled
//    RIBBON between a shape's contour and an offset contour,
//    whose width varies along its length like a marker's —
//    [outlineWeight] per segment, seeded per shape. The
//    silhouette is inset inside its own line, the way ink
//    sits on cut paper in the source pipeline.
// ============================================================

/** One point of a cut contour (canvas units). */
data class CutPt(val x: Float, val y: Float)

object PaperCut {

    // ---------- deterministic hash ----------
    /** Stable [0,1) hash of (seed, index) — same on every
     *  device and every run (java.util.Random's algorithm is
     *  specified, so a seeded field is a fixed asset). */
    fun hash01(seed: Long, i: Int): Float =
        Random(seed * 1000003L + i.toLong() * 97L + 17L).nextFloat()

    // ---------- stage 3: paper material fields ----------

    /** One paper fibre: centre in CELL units (the caller
     *  scales by the cell size), direction, length in cell
     *  units, and whether it lightens or darkens the stock. */
    data class Fibre(val cx: Float, val cy: Float,
                     val dx: Float, val dy: Float,
                     val len: Float, val light: Boolean)

    /** The fibre field for a [cols] x [rows] grid: exactly
     *  one fibre per cell, centre jittered at most ±0.28 of a
     *  cell from the cell centre. Minimum centre distance
     *  between any two fibres is therefore >= 0.44 cells —
     *  fibres read as paper grain and can never cluster. */
    fun fibreField(seed: Long, cols: Int, rows: Int): List<Fibre> {
        if (cols <= 0 || rows <= 0) return emptyList()
        val rnd = Random(seed)
        val out = ArrayList<Fibre>(cols * rows)
        for (gy in 0 until rows) {
            for (gx in 0 until cols) {
                val jx = (rnd.nextFloat() - 0.5f) * 0.56f
                val jy = (rnd.nextFloat() - 0.5f) * 0.56f
                val ang = rnd.nextFloat() * PI.toFloat()
                val len = 0.22f + rnd.nextFloat() * 0.26f
                out.add(Fibre(gx + 0.5f + jx, gy + 0.5f + jy,
                    cos(ang), sin(ang), len, rnd.nextBoolean()))
            }
        }
        return out
    }

    /** One soft mottle blotch: centre in unit-square space,
     *  radius as a fraction of the surface's short side. */
    data class Mottle(val x: Float, val y: Float,
                      val r: Float, val light: Boolean)

    /** A handful of LARGE, soft blotches — the uneven dye of
     *  construction-paper stock. Few and wide by design (the
     *  opposite of stipple). */
    fun mottleField(seed: Long, n: Int = 5): List<Mottle> {
        if (n <= 0) return emptyList()
        val rnd = Random(seed * 31L + 7L)
        return List(n) {
            Mottle(rnd.nextFloat(), rnd.nextFloat(),
                0.20f + rnd.nextFloat() * 0.22f, rnd.nextBoolean())
        }
    }

    // ---------- stage 5: painted outline weight ----------

    /** The outline's width at segment [seg] of a shape seeded
     *  [seed]: base * [0.72 .. 1.27] — a marker line that
     *  breathes along its length, identical every frame. */
    fun outlineWeight(seed: Long, seg: Int, base: Float): Float =
        base * (0.72f + 0.55f * hash01(seed, seg * 13 + 5))

    // ---------- stage 4: scissor-cut contours ----------

    /** Radial wobble for contour point [i]: ±[amp] at most,
     *  seeded — the hand-cut irregularity. */
    fun wobble(seed: Long, i: Int, amp: Float): Float =
        (hash01(seed, i * 7 + 1) - 0.5f) * 2f * amp

    /** A cut ellipse contour: [n] points around (cx, cy),
     *  each pushed radially by its seeded wobble (amp scales
     *  with the smaller radius, so the cut stays subtle at
     *  any size). */
    fun cutEllipseContour(cx: Float, cy: Float, rx: Float, ry: Float,
                          seed: Long, n: Int = 30): List<CutPt> {
        val amp = minOf(rx, ry) * 0.02f + 0.4f
        return List(n) { i ->
            val a = (i.toFloat() / n) * 2f * PI.toFloat()
            val w = 1f + wobble(seed, i, amp) / maxOf(1f, minOf(rx, ry))
            CutPt(cx + cos(a) * rx * w, cy + sin(a) * ry * w)
        }
    }

    /** A cut rounded-rect contour: straight runs sampled
     *  [per] points per side with a slight perpendicular
     *  wobble, corners as true arcs of radius [rad] (the
     *  scissor-rounded corner). Points run clockwise from
     *  the top edge. */
    fun cutRectContour(l: Float, t: Float, r: Float, b: Float,
                       rad: Float, seed: Long, per: Int = 4): List<CutPt> {
        val out = ArrayList<CutPt>()
        val rr = minOf(rad, (r - l) / 2f, (b - t) / 2f).coerceAtLeast(0f)
        val amp = 0.9f
        var idx = 0
        fun pt(x: Float, y: Float, nx: Float, ny: Float) {
            val w = wobble(seed, idx++, amp)
            out.add(CutPt(x + nx * w, y + ny * w))
        }
        // top edge, left -> right (outward normal = up)
        for (i in 0..per) {
            val f = i.toFloat() / (per + 1)
            pt(l + rr + (r - l - 2 * rr) * f, t, 0f, -1f)
        }
        // top-right corner arc
        for (i in 1..3) {
            val a = -PI.toFloat() / 2f + (i / 4f) * (PI.toFloat() / 2f)
            val px = r - rr + cos(a) * rr
            val py = t + rr + sin(a) * rr
            pt(px, py, cos(a), sin(a))
        }
        // right edge, top -> bottom
        for (i in 0..per) {
            val f = i.toFloat() / (per + 1)
            pt(r, t + rr + (b - t - 2 * rr) * f, 1f, 0f)
        }
        // bottom-right corner arc
        for (i in 1..3) {
            val a = (i / 4f) * (PI.toFloat() / 2f)
            val px = r - rr + cos(a) * rr
            val py = b - rr + sin(a) * rr
            pt(px, py, cos(a), sin(a))
        }
        // bottom edge, right -> left
        for (i in 0..per) {
            val f = i.toFloat() / (per + 1)
            pt(r - rr - (r - l - 2 * rr) * f, b, 0f, 1f)
        }
        // bottom-left corner arc
        for (i in 1..3) {
            val a = PI.toFloat() / 2f + (i / 4f) * (PI.toFloat() / 2f)
            val px = l + rr + cos(a) * rr
            val py = b - rr + sin(a) * rr
            pt(px, py, cos(a), sin(a))
        }
        // left edge, bottom -> top
        for (i in 0..per) {
            val f = i.toFloat() / (per + 1)
            pt(l, b - rr - (b - t - 2 * rr) * f, -1f, 0f)
        }
        // top-left corner arc
        for (i in 1..3) {
            val a = PI.toFloat() + (i / 4f) * (PI.toFloat() / 2f)
            val px = l + rr + cos(a) * rr
            val py = t + rr + sin(a) * rr
            pt(px, py, cos(a), sin(a))
        }
        return out
    }

    /** [contour] pushed outward along its per-point normals
     *  by [outlineWeight] — the outer edge of a painted
     *  outline ribbon, or the visible rim of a silhouette
     *  underlay. Same point count, same order. */
    fun offsetContour(contour: List<CutPt>, seed: Long,
                      baseW: Float): List<CutPt> =
        offsetContourW(contour,
            contour.indices.map { outlineWeight(seed, it, baseW) })

    /** [contour] pushed outward along its per-point normals
     *  by PER-POINT widths — the OutlineEngine's curvature-
     *  aware ribbons (v0.17.0a) ride on this. Same normal
     *  math as the seeded ribbon; only the widths differ. */
    fun offsetContourW(contour: List<CutPt>,
                       widths: List<Float>): List<CutPt> {
        val n = contour.size
        if (n < 3) return contour
        return List(n) { i ->
            val p = contour[i]
            val prev = contour[(i + n - 1) % n]
            val next = contour[(i + 1) % n]
            var nx = (next.y - prev.y)
            var ny = -(next.x - prev.x)
            val len = sqrt(nx * nx + ny * ny)
            if (len > 0.0001f) { nx /= len; ny /= len }
            // orient the normal away from the centroid
            val cx = contour.sumOf { it.x.toDouble() } / n
            val cy = contour.sumOf { it.y.toDouble() } / n
            if ((p.x - cx) * nx + (p.y - cy) * ny < 0) { nx = -nx; ny = -ny }
            val w = widths.getOrElse(i) { 0f }
            CutPt(p.x + nx * w, p.y + ny * w)
        }
    }

    // ---------- v0.17.0a: region-aware paper (spgen §3) ----------

    /** The fibre field of one paper REGION. The construction
     *  NEVER changes — a jittered grid, one fibre per cell,
     *  centres bounded inside their cells, so the minimum
     *  centre gap (>= 0.44 cells) holds in every region and
     *  fibres can never cluster (the trypophobia rule is
     *  structural, not regional). What changes per region is
     *  fibre LENGTH, DIRECTION bias and DENSITY:
     *  SKIN — short fine fibres; CLOTH — long fibres with a
     *  horizontal weave bias on alternate rows; METAL — whole
     *  cells left bare on the same grid (the survivors keep
     *  their cell centres, so sparsity cannot cluster either)
     *  and the shortest fibres of all. */
    fun regionFibreField(region: PaperRegion, seed: Long,
                          cols: Int, rows: Int): List<Fibre> {
        val base = fibreField(seed, cols, rows)
        return when (region) {
            PaperRegion.SURFACE -> base
            PaperRegion.SKIN -> base.map {
                it.copy(len = it.len * 0.55f)
            }
            PaperRegion.CLOTH -> base.mapIndexed { i, f ->
                val row = if (cols > 0) i / cols else 0
                if (row % 2 == 0) {
                    // weave row: direction pulled toward the
                    // horizontal, length stretched
                    val hl = sqrt(f.dx * f.dx + f.dy * f.dy)
                    val ndx = if (hl > 0.0001f)
                        (f.dx / hl) * 0.35f +
                            (if (f.dx >= 0f) 0.65f else -0.65f)
                    else f.dx
                    val ndy = if (hl > 0.0001f)
                        (f.dy / hl) * 0.35f else f.dy
                    val nl = sqrt(ndx * ndx + ndy * ndy)
                    f.copy(dx = ndx / nl, dy = ndy / nl,
                        len = f.len * 1.45f)
                } else f.copy(len = f.len * 1.2f)
            }
            PaperRegion.METAL -> base.filterIndexed { i, _ ->
                hash01(seed * 7 + 3, i) < 0.34f
            }.map { it.copy(len = it.len * 0.8f) }
        }
    }

    // ---------- stage 8: boil ----------

    /** Boil runs a 3-frame cycle (~260 ms a frame): the drawn
     *  world's linework breathes by at most a pixel, the
     *  hand-drawn boil of the source pipeline. */
    const val BOIL_FRAME_MS = 260L
    const val BOIL_FRAMES = 3

    fun boilPhase(timeMs: Long): Int =
        ((timeMs / BOIL_FRAME_MS) % BOIL_FRAMES).toInt()

    /** The boil translation for a surface seeded [seed] at
     *  cycle [phase]: phase 0 is the neutral frame; the other
     *  two are seeded offsets of at most ±1 px per axis.
     *  Applied to finished linework as a whole — edges are
     *  never re-rolled, and facial features (drawn with their
     *  figure, or after the backdrop pass) never shear. */
    fun boilOffset(seed: Long, phase: Int): CutPt {
        if (phase == 0) return CutPt(0f, 0f)
        val x = (hash01(seed, phase * 31 + 3) - 0.5f) * 2f
        val y = (hash01(seed, phase * 31 + 11) - 0.5f) * 2f
        return CutPt(x.coerceIn(-1f, 1f), y.coerceIn(-1f, 1f))
    }

    // ---------- stage 8: band depth cueing ----------

    /** The post pass's depth cue: how far toward grey a band
     *  washes. The ACTION band is never washed (1.0 = full
     *  saturation stays); the background band sits slightly
     *  back, the foreground band a breath back. On a 2D
     *  canvas this ships as a low-alpha grey wash per band —
     *  the desaturation substitute the README ledger records
     *  in place of a real DOF blur. */
    const val BG_WASH_ALPHA = 24
    const val FG_WASH_ALPHA = 10
    const val VIGNETTE_ALPHA = 26
    const val GRAIN_ALPHA = 7
}

// ============================================================
// v0.17.0a — paper REGIONS + the OUTLINE ENGINE (spgen §§2–3,
// adopted into the pipeline: the generator's disciplines as
// runtime law). Paper stays 100% PROCEDURAL — the synthesis
// approach is what was adopted, never any scanned kit.
// ============================================================

/** The region a paper surface belongs to. Region treatments
 *  re-parameterise the SAME jittered-grid fibre construction
 *  (PaperCut.regionFibreField) — no region can stipple or
 *  cluster, because no region changes the construction. */
enum class PaperRegion { SKIN, CLOTH, METAL, SURFACE }

object PaperRegions {
    /** fibreLenMul — fibre length vs the surface field;
     *  fibreAlphaMul — grain strength; speckle — the pulp
     *  inclusion rate of the doc's synthesis, carried here by
     *  the mottle field's alpha (our speckle is soft blotches,
     *  never dots); satLift — saturation lift for the stock;
     *  sheen — an edge-sheen pass is allowed (metal only). */
    data class Params(val fibreLenMul: Float,
                      val fibreAlphaMul: Float,
                      val speckle: Float,
                      val satLift: Float,
                      val sheen: Boolean)

    fun params(region: PaperRegion): Params = when (region) {
        PaperRegion.SKIN -> Params(0.55f, 0.8f, 0.0f, 1.05f, false)
        PaperRegion.CLOTH -> Params(1.45f, 1.15f, 0.02f, 1.0f, false)
        PaperRegion.METAL -> Params(0.8f, 0.5f, 0.0f, 1.0f, true)
        PaperRegion.SURFACE -> Params(1.0f, 1.0f, 0.02f, 1.0f, false)
    }
}

/** The OUTLINE ENGINE — spgen §2's weight law as a pure
 *  function, replacing the figure side's uniform-seeded
 *  ribbons. Weight is specified in FINAL SCREEN PIXELS:
 *  w = clip(MAX × (1 − detailBias) + curvature × 2, MIN, MAX)
 *  — thick on the outer contour, thickest where the contour
 *  bends hardest, interior detail lines at w × 0.6. Because
 *  our parts are procedural (no texture atlas, no mips), the
 *  mip-safety rule lands as PER-CONTEXT REGENERATION: the
 *  weights are recomputed from the contour's own curvature
 *  in every render context (world scene, PaperDoll portrait,
 *  reference sheet) — an outline is never drawn at one scale
 *  and stretched to another. A seeded marker variance
 *  (0.85..1.15) rides INSIDE the window, so the hand-drawn
 *  breath survives but the rendered line can never leave
 *  [MIN_PX, MAX_PX] at 1:1 — a toddler's outline and an
 *  adult's read the same weight, which is the whole point
 *  of the scale contract. */
object OutlineEngine {
    const val MIN_PX = 3f
    const val MAX_PX = 7f
    const val INTERIOR_MUL = 0.6f
    const val DETAIL_BIAS = 0.5f
    /** Portraits sit closer to the eye: the doll context
     *  biases detail down so its lines ride the window's
     *  upper half — the v0.16.0d "portrait weight", now a
     *  parameter of the law instead of a fudge factor. */
    const val DOLL_DETAIL_BIAS = 0.32f

    /** Local curvature at contour point [i], 0..1: 0 on a
     *  straight run, ~0.8 at a right-angle corner, 1 at a
     *  hairpin. From the turning angle between the segments
     *  either side of the point. */
    fun curvatureAt(contour: List<CutPt>, i: Int): Float {
        val n = contour.size
        if (n < 3) return 0f
        val p = contour[i]
        val a = contour[(i + n - 1) % n]
        val b = contour[(i + 1) % n]
        val v1x = p.x - a.x; val v1y = p.y - a.y
        val v2x = b.x - p.x; val v2y = b.y - p.y
        val l1 = sqrt(v1x * v1x + v1y * v1y)
        val l2 = sqrt(v2x * v2x + v2y * v2y)
        if (l1 < 0.0001f || l2 < 0.0001f) return 0f
        val dot = ((v1x * v2x + v1y * v2y) / (l1 * l2))
            .coerceIn(-1f, 1f)
        val ang = kotlin.math.acos(dot.toDouble()).toFloat()
        return (ang / PI.toFloat() * 1.6f).coerceIn(0f, 1f)
    }

    /** The weight law, in screen pixels. */
    fun weightPx(curvature: Float, interior: Boolean = false,
                 detailBias: Float = DETAIL_BIAS): Float {
        var w = MAX_PX * (1f - detailBias) + curvature * 2f
        w = w.coerceIn(MIN_PX, MAX_PX)
        return if (interior) w * INTERIOR_MUL else w
    }

    /** Per-point ribbon widths for [contour] in a render
     *  context whose canvas runs at [ctxPx] canvas px per
     *  screen px (1.0 wherever the canvas IS the screen —
     *  world, doll and sheets alike in this engine; the
     *  parameter is the conversion point if a context ever
     *  supersamples). Curvature sets the shape, the seeded
     *  variance breathes inside the window, and every width
     *  is clamped back into the window after variance — the
     *  rendered line is in [MIN_PX, MAX_PX] at every point,
     *  at every figure scale. */
    fun offsets(contour: List<CutPt>, seed: Long,
                ctxPx: Float = 1f,
                detailBias: Float = DETAIL_BIAS): List<Float> =
        contour.indices.map { i ->
            val w = weightPx(curvatureAt(contour, i),
                detailBias = detailBias)
            val v = 0.85f + 0.30f *
                PaperCut.hash01(seed, i * 13 + 5)
            (w * v).coerceIn(MIN_PX, MAX_PX) * ctxPx
        }

    /** Interior detail weight for a contour whose outer
     *  weight at this point is [outerW]: the doc's 0.6×. */
    fun interiorOf(outerW: Float): Float = outerW * INTERIOR_MUL
}

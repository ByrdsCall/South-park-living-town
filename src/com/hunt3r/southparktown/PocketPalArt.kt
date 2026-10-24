package com.hunt3r.southparktown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

// ============================================================
// PocketPalArt — v0.5.0c: the Pocket Pals card renderer.
//
// Every card's art is drawn HERE, from the card's spec in
// PocketPals (body shape + palette + feature flags) — zero
// image files, and the same spec draws the same creature
// forever, like a photo. Solid shapes, stripes and stars
// only: no clustered dots/holes patterns, ever (the
// trypophobia guard the whole game designs under).
// Silhouette mode draws the identical geometry in near-
// black — the album's "you don't have this one yet" idiom.
// ============================================================

class PocketPalView(
    context: Context,
    private val card: PalCard,
    private val owned: Boolean
) : View(context) {

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val frame = RectF(w * 0.04f, h * 0.03f, w * 0.96f, h * 0.97f)
        // ---- the card stock: a school tint, a rarity edge ----
        val tint = if (owned) schoolTint(card.school) else "#2b2b2b"
        c.drawRoundRect(frame, 10f, 10f, Paper.fillPaint(tint))
        val edge = when {
            !owned -> "#4a4a4a"
            card.rarity == PocketPals.RARE -> "#d4a017"
            card.rarity == PocketPals.UNCOMMON -> "#3f9e4d"
            else -> "#8a8a8a"
        }
        c.drawRoundRect(frame, 10f, 10f,
            Paper.strokePaint(edge, if (card.rarity == PocketPals.RARE && owned) 5f else 3f))
        // ---- the creature, centered in the frame ----
        val cx = frame.centerX()
        val cy = frame.centerY()
        val bw = frame.width() * 0.62f
        val bh = frame.height() * 0.62f
        drawCreature(c, cx, cy, bw, bh)
        // a rare card wears three solid stars, one per corner
        if (owned && card.rarity == PocketPals.RARE) {
            star(c, frame.left + frame.width() * 0.13f, frame.top + frame.height() * 0.12f,
                frame.width() * 0.055f, card.accentColor)
            star(c, frame.right - frame.width() * 0.13f, frame.top + frame.height() * 0.12f,
                frame.width() * 0.055f, card.accentColor)
            star(c, frame.right - frame.width() * 0.13f, frame.bottom - frame.height() * 0.12f,
                frame.width() * 0.055f, card.accentColor)
        }
    }

    private fun ink(hex: String): String = if (owned) hex else "#16130f"

    private fun bodyPath(cx: Float, cy: Float, bw: Float, bh: Float): Path {
        val p = Path()
        when (card.body) {
            "TALL" -> p.addRoundRect(
                RectF(cx - bw * 0.36f, cy - bh * 0.5f, cx + bw * 0.36f, cy + bh * 0.5f),
                bw * 0.3f, bw * 0.3f, Path.Direction.CW)
            "WIDE" -> p.addOval(
                RectF(cx - bw * 0.5f, cy - bh * 0.36f, cx + bw * 0.5f, cy + bh * 0.36f),
                Path.Direction.CW)
            "SQUARE" -> p.addRoundRect(
                RectF(cx - bw * 0.42f, cy - bh * 0.42f, cx + bw * 0.42f, cy + bh * 0.42f),
                bw * 0.10f, bw * 0.10f, Path.Direction.CW)
            "STAR" -> {
                val pts = 5
                for (i in 0 until pts * 2) {
                    val ang = -Math.PI / 2 + i * Math.PI / pts
                    val r = if (i % 2 == 0) bw * 0.52f else bw * 0.24f
                    val x = cx + (cos(ang) * r).toFloat()
                    val y = cy + (sin(ang) * r * (bh / bw)).toFloat()
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                p.close()
            }
            "BLOB" -> {
                // a lopsided puddle-blob: four big arcs, no two alike
                p.moveTo(cx - bw * 0.48f, cy + bh * 0.30f)
                p.cubicTo(cx - bw * 0.52f, cy - bh * 0.28f, cx - bw * 0.18f,
                    cy - bh * 0.52f, cx + bw * 0.10f, cy - bh * 0.44f)
                p.cubicTo(cx + bw * 0.42f, cy - bh * 0.36f, cx + bw * 0.52f,
                    cy - bh * 0.02f, cx + bw * 0.46f, cy + bh * 0.30f)
                p.cubicTo(cx + bw * 0.30f, cy + bh * 0.52f, cx - bw * 0.30f,
                    cy + bh * 0.52f, cx - bw * 0.48f, cy + bh * 0.30f)
                p.close()
            }
            else -> p.addOval( // ROUND
                RectF(cx - bw * 0.44f, cy - bh * 0.46f, cx + bw * 0.44f, cy + bh * 0.46f),
                Path.Direction.CW)
        }
        return p
    }

    private fun star(c: Canvas, cx: Float, cy: Float, r: Float, hex: String) {
        val p = Path()
        for (i in 0 until 10) {
            val ang = -Math.PI / 2 + i * Math.PI / 5
            val rr = if (i % 2 == 0) r else r * 0.45f
            val x = cx + (cos(ang) * rr).toFloat()
            val y = cy + (sin(ang) * rr).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        c.drawPath(p, Paper.fillPaint(ink(hex)))
    }

    private fun drawCreature(c: Canvas, cx: Float, cy: Float, bw: Float, bh: Float) {
        val body = ink(card.bodyColor)
        val belly = ink(card.bellyColor)
        val accent = ink(card.accentColor)
        val top = cy - bh * 0.46f

        // ---- behind-body features: wings, tail, ears ----
        when (card.extra) {
            "WINGS" -> {
                c.drawOval(RectF(cx - bw * 0.72f, cy - bh * 0.30f,
                    cx - bw * 0.34f, cy + bh * 0.10f), Paper.fillPaint(accent))
                c.drawOval(RectF(cx + bw * 0.34f, cy - bh * 0.30f,
                    cx + bw * 0.72f, cy + bh * 0.10f), Paper.fillPaint(accent))
            }
            "TAIL" -> {
                c.drawPath(Path().apply {
                    moveTo(cx + bw * 0.34f, cy + bh * 0.28f)
                    cubicTo(cx + bw * 0.66f, cy + bh * 0.30f, cx + bw * 0.70f,
                        cy + bh * 0.02f, cx + bw * 0.60f, cy - bh * 0.08f)
                }, Paper.strokePaint(accent, bw * 0.09f))
                c.drawCircle(cx + bw * 0.60f, cy - bh * 0.10f, bw * 0.075f,
                    Paper.fillPaint(accent))
            }
        }
        when (card.ears) {
            "POINTS" -> {
                c.drawPath(Path().apply {
                    moveTo(cx - bw * 0.34f, top + bh * 0.14f)
                    lineTo(cx - bw * 0.22f, top - bh * 0.16f)
                    lineTo(cx - bw * 0.08f, top + bh * 0.10f); close()
                }, Paper.fillPaint(body))
                c.drawPath(Path().apply {
                    moveTo(cx + bw * 0.08f, top + bh * 0.10f)
                    lineTo(cx + bw * 0.22f, top - bh * 0.16f)
                    lineTo(cx + bw * 0.34f, top + bh * 0.14f); close()
                }, Paper.fillPaint(body))
            }
            "HORNS" -> {
                c.drawPath(Path().apply {
                    moveTo(cx - bw * 0.36f, top + bh * 0.16f)
                    quadTo(cx - bw * 0.44f, top - bh * 0.22f, cx - bw * 0.20f,
                        top - bh * 0.20f)
                    quadTo(cx - bw * 0.30f, top - bh * 0.04f, cx - bw * 0.24f,
                        top + bh * 0.10f); close()
                }, Paper.fillPaint(accent))
                c.drawPath(Path().apply {
                    moveTo(cx + bw * 0.36f, top + bh * 0.16f)
                    quadTo(cx + bw * 0.44f, top - bh * 0.22f, cx + bw * 0.20f,
                        top - bh * 0.20f)
                    quadTo(cx + bw * 0.30f, top - bh * 0.04f, cx + bw * 0.24f,
                        top + bh * 0.10f); close()
                }, Paper.fillPaint(accent))
            }
            "FLOPPY" -> {
                c.drawOval(RectF(cx - bw * 0.52f, top - bh * 0.02f,
                    cx - bw * 0.28f, top + bh * 0.42f), Paper.fillPaint(body))
                c.drawOval(RectF(cx + bw * 0.28f, top - bh * 0.02f,
                    cx + bw * 0.52f, top + bh * 0.42f), Paper.fillPaint(body))
            }
            "ANTENNA" -> {
                c.drawLine(cx - bw * 0.14f, top + bh * 0.06f, cx - bw * 0.26f,
                    top - bh * 0.24f, Paper.strokePaint(accent, bw * 0.045f))
                c.drawLine(cx + bw * 0.14f, top + bh * 0.06f, cx + bw * 0.26f,
                    top - bh * 0.24f, Paper.strokePaint(accent, bw * 0.045f))
                c.drawCircle(cx - bw * 0.26f, top - bh * 0.27f, bw * 0.06f,
                    Paper.fillPaint(accent))
                c.drawCircle(cx + bw * 0.26f, top - bh * 0.27f, bw * 0.06f,
                    Paper.fillPaint(accent))
            }
            "FIN" -> {
                c.drawPath(Path().apply {
                    moveTo(cx - bw * 0.07f, top + bh * 0.10f)
                    quadTo(cx, top - bh * 0.30f, cx + bw * 0.16f, top + bh * 0.02f)
                    quadTo(cx + bw * 0.04f, top - bh * 0.02f, cx - bw * 0.07f,
                        top + bh * 0.10f); close()
                }, Paper.fillPaint(accent))
            }
            "TUFT" -> {
                for (dx in listOf(-0.13f, 0f, 0.13f)) {
                    c.drawPath(Path().apply {
                        moveTo(cx + bw * dx - bw * 0.05f, top + bh * 0.08f)
                        lineTo(cx + bw * dx, top - bh * 0.18f)
                        lineTo(cx + bw * dx + bw * 0.05f, top + bh * 0.08f); close()
                    }, Paper.fillPaint(accent))
                }
            }
        }

        // ---- the body, then its markings clipped inside it ----
        val bp = bodyPath(cx, cy, bw, bh)
        c.drawPath(bp, Paper.fillPaint(body))
        c.drawPath(bp, Paper.strokePaint(if (owned) "#1a1a1a" else "#000000", 2.5f))
        c.save()
        c.clipPath(bp)
        // belly
        c.drawOval(RectF(cx - bw * 0.26f, cy + bh * 0.06f,
            cx + bw * 0.26f, cy + bh * 0.50f), Paper.fillPaint(belly))
        when (card.extra) {
            "STRIPES" -> {
                for (i in 0..2) {
                    val y = cy - bh * 0.26f + i * bh * 0.20f
                    c.drawRect(cx - bw * 0.55f, y, cx + bw * 0.55f,
                        y + bh * 0.075f, Paper.fillPaint(accent))
                }
            }
            "BELT" -> c.drawRect(cx - bw * 0.55f, cy - bh * 0.02f,
                cx + bw * 0.55f, cy + bh * 0.075f, Paper.fillPaint(accent))
            "CAP" -> c.drawRect(cx - bw * 0.55f, cy - bh * 0.55f,
                cx + bw * 0.55f, cy - bh * 0.22f, Paper.fillPaint(accent))
        }
        c.restore()
        if (card.extra == "STAR_MARK")
            star(c, cx, cy + bh * 0.24f, bw * 0.11f, card.accentColor)

        // ---- the face ----
        val ey = cy - bh * 0.12f
        val ex = bw * 0.17f
        val dark = if (owned) "#1a1a1a" else "#000000"
        when (card.eyes) {
            "HAPPY" -> {
                c.drawArc(RectF(cx - ex - bw * 0.08f, ey - bh * 0.07f,
                    cx - ex + bw * 0.08f, ey + bh * 0.07f), 180f, 180f, false,
                    Paper.strokePaint(dark, bw * 0.045f))
                c.drawArc(RectF(cx + ex - bw * 0.08f, ey - bh * 0.07f,
                    cx + ex + bw * 0.08f, ey + bh * 0.07f), 180f, 180f, false,
                    Paper.strokePaint(dark, bw * 0.045f))
            }
            "SLEEPY" -> {
                c.drawLine(cx - ex - bw * 0.08f, ey, cx - ex + bw * 0.08f, ey,
                    Paper.strokePaint(dark, bw * 0.045f))
                c.drawLine(cx + ex - bw * 0.08f, ey, cx + ex + bw * 0.08f, ey,
                    Paper.strokePaint(dark, bw * 0.045f))
            }
            "STAR" -> {
                star(c, cx - ex, ey, bw * 0.085f, card.accentColor)
                star(c, cx + ex, ey, bw * 0.085f, card.accentColor)
            }
            "WINK" -> {
                c.drawCircle(cx - ex, ey, bw * 0.05f, Paper.fillPaint(dark))
                c.drawArc(RectF(cx + ex - bw * 0.08f, ey - bh * 0.07f,
                    cx + ex + bw * 0.08f, ey + bh * 0.07f), 180f, 180f, false,
                    Paper.strokePaint(dark, bw * 0.045f))
            }
            "ROUND" -> {
                c.drawOval(RectF(cx - ex - bw * 0.075f, ey - bh * 0.10f,
                    cx - ex + bw * 0.075f, ey + bh * 0.10f),
                    Paper.fillPaint(if (owned) "#ffffff" else "#000000"))
                c.drawOval(RectF(cx + ex - bw * 0.075f, ey - bh * 0.10f,
                    cx + ex + bw * 0.075f, ey + bh * 0.10f),
                    Paper.fillPaint(if (owned) "#ffffff" else "#000000"))
                c.drawCircle(cx - ex, ey, bw * 0.038f, Paper.fillPaint(dark))
                c.drawCircle(cx + ex, ey, bw * 0.038f, Paper.fillPaint(dark))
            }
            else -> { // DOTS
                c.drawCircle(cx - ex, ey, bw * 0.05f, Paper.fillPaint(dark))
                c.drawCircle(cx + ex, ey, bw * 0.05f, Paper.fillPaint(dark))
            }
        }
        // a small solid smile under every face
        c.drawArc(RectF(cx - bw * 0.09f, cy + bh * 0.02f,
            cx + bw * 0.09f, cy + bh * 0.16f), 15f, 150f, false,
            Paper.strokePaint(dark, bw * 0.04f))
    }

    private fun schoolTint(school: String): String = when (school) {
        "Spark" -> "#fdf3c8"
        "Frost" -> "#e2f2fc"
        "Leaf" -> "#e6f4d8"
        "Grit" -> "#efe6d8"
        else -> "#f4ecd8"
    }
}

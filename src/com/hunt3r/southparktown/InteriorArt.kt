package com.hunt3r.southparktown

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.random.Random

// ============================================================
// InteriorArt — v0.1.5 interior scenes. Every location has a
// room (INTERIORS in CharacterSystem.kt): wall + floor, a
// window showing the time/weather outside, an EXIT door, and
// a row of freshly-drawn paper props from a small vocabulary
// (sofa, blackboard, counter, altar, bus seats, ice hole...).
// All shapes are original code-drawn geometry in Paper style.
//
// Prop convention: r(l, t, rgt, b) draws a rect whose TOP edge
// is t prop-heights above the floor line and BOTTOM edge is b
// prop-heights above it; widths are in prop-width units.
// ============================================================
object InteriorArt {

    /** The EXIT door rect (shared with GameView's hit testing). */
    fun doorRect(w: Float, h: Float): RectF =
        RectF(w * 0.035f, h * 0.30f, w * 0.165f, h * 0.66f)

    fun drawRoom(c: Canvas, w: Int, h: Int, def: InteriorDef, timeMin: Int,
                 weatherId: String, seed: Long, holiday: Holiday?) {
        // v0.15.0d: the school bus is its own layered
        // interior now (BusInteriorSpec) — the generic
        // room it used to borrow was a blank yellow void
        // with floating shapes
        if (def.theme == "bus") {
            drawBusRoom(c, w, h, def, timeMin, weatherId, seed)
            return
        }
        val wf = w.toFloat()
        val hf = h.toFloat()
        val tod = SceneArt.timeOfDay(timeMin)
        val night = tod == "night"
        val floorY = hf * 0.64f

        // ---- wall + floor ----
        c.drawRect(0f, 0f, wf, floorY, Paper.fillPaint(def.wallHex))
        // v0.16.0d (stage 3): interior walls and floors wear
        // the procedural paper material — washed in BEFORE
        // the night tint, so the tint still falls over the
        // stock exactly as it falls over the paint
        Paper.applyMaterial(c, 0f, 0f, wf, floorY,
            def.wallHex, seed + 7L)
        if (night) c.drawRect(0f, 0f, wf, floorY, Paper.fillPaint("#1a2340", 110))
        c.drawRect(0f, floorY, wf, hf, Paper.fillPaint(def.floorHex))
        Paper.applyMaterial(c, 0f, floorY, wf, hf,
            def.floorHex, seed + 13L)
        val boardP = Paper.strokePaint("#000000", 2f)
        boardP.alpha = 40
        for (i in 1..5) {
            val y = floorY + (hf - floorY) * i / 6f
            c.drawLine(0f, y, wf, y, boardP)
        }
        c.drawRect(0f, floorY - 6f, wf, floorY, Paper.fillPaint("#000000", 60))
        // rug
        Paper.crooked(c, 0.5f, wf / 2, floorY + (hf - floorY) * 0.55f) {
            c.drawRoundRect(RectF(wf * 0.28f, floorY + (hf - floorY) * 0.30f,
                wf * 0.72f, floorY + (hf - floorY) * 0.78f), 18f, 18f,
                Paper.fillPaint(Paper.RED, 60))
        }

        // ---- window: the weather + time, visible from inside ----
        val winR = RectF(wf * 0.60f, hf * 0.10f, wf * 0.86f, hf * 0.36f)
        val skyC = when (tod) {
            "day" -> if (weatherId == "sunny") "#7ab8e8" else "#8a97a8"
            "dusk" -> "#c86a4a"
            else -> "#141c33"
        }
        c.drawRect(winR, Paper.fillPaint(skyC))
        if (night) {
            val sr = Random(seed + 5)
            val sp = Paper.fillPaint("#ffffff", 200)
            repeat(14) {
                c.drawCircle(winR.left + sr.nextFloat() * winR.width(),
                    winR.top + sr.nextFloat() * winR.height(), 2f, sp)
            }
            c.drawCircle(winR.right - 34f, winR.top + 30f, 13f, Paper.fillPaint("#e8e4c8"))
        }
        if (weatherId in setOf("light_snow", "heavy_snow", "blizzard")) {
            val sr = Random(seed + 9)
            val sp = Paper.fillPaint("#ffffff", 190)
            repeat(16) {
                c.drawCircle(winR.left + sr.nextFloat() * winR.width(),
                    winR.top + sr.nextFloat() * winR.height(), 2.4f, sp)
            }
        }
        if (weatherId == "rain") {
            val rp = Paper.strokePaint("#5a8ac8", 2f)
            rp.alpha = 170
            val sr = Random(seed + 11)
            repeat(10) {
                val x = winR.left + sr.nextFloat() * winR.width()
                val y = winR.top + sr.nextFloat() * winR.height()
                c.drawLine(x, y, x - 4f, y + 14f, rp)
            }
        }
        if (weatherId == "fog") c.drawRect(winR, Paper.fillPaint("#cfd6dd", 130))
        // v0.3.0a: the exterior light layer, seen from inside —
        // where the street outside has lamps (LOC_LIGHT), their
        // glow smudges the window's lower corner at night, in
        // the same warmth the scene outside is burning.
        if (night && SceneLight.lampsOf(def.locId) > 0) {
            val lw = SceneLight.warmthOf(def.locId)
            c.drawOval(RectF(winR.left + 2, winR.bottom - 30, winR.left + 64,
                winR.bottom + 4), Paper.fillPaint(lw, 55))
            c.drawCircle(winR.left + 20, winR.bottom - 12, 5f, Paper.fillPaint(lw, 150))
        }
        // #10 FROST FERNS: on the coldest mornings the corners grow
        // frost that melts off as the room's morning warms up.
        val frost = when {
            timeMin >= 10 * 60 -> 0f
            weatherId in setOf("light_snow", "heavy_snow", "blizzard") ||
                TownInfo.freezeThaw ->
                ((10 * 60 - timeMin) / 300f).coerceIn(0.25f, 1f)
            else -> 0f
        }
        if (frost > 0f) {
            val fp = Paper.strokePaint("#eef4ff", 2f, (200 * frost).toInt())
            val reach = winR.width() * 0.30f * frost
            for ((fx, fy, sx, sy) in listOf(
                listOf(winR.left, winR.top, 1f, 1f),
                listOf(winR.right, winR.top, -1f, 1f),
                listOf(winR.left, winR.bottom, 1f, -1f),
                listOf(winR.right, winR.bottom, -1f, -1f))) {
                for (b in 0..2) {
                    val ang = b * 0.5f
                    val ex = fx + sx * reach * kotlin.math.cos(ang * 0.9f)
                    val ey = fy + sy * reach * kotlin.math.sin(ang * 0.9f + 0.4f)
                    c.drawLine(fx, fy, ex, ey, fp)
                    c.drawLine(ex, ey, ex - sx * reach * 0.25f, ey, fp)
                    c.drawLine(ex, ey, ex, ey - sy * reach * 0.25f, fp)
                }
            }
        }
        // #22 CURTAIN TWITCHES: the curtains part for a second now
        // and then — much more often in homes where you're liked.
        run {
            val cycle = RenderClock.nowMs() / 1000f
            val period = if (TownInfo.curtainWarm) 6f else 14f
            val parted = (cycle % period) < 1.1f
            val gap = if (parted) winR.width() * 0.16f else winR.width() * 0.035f
            val cur = Paper.fillPaint("#7a2436", 235)
            c.drawRect(winR.left, winR.top, winR.left + winR.width() * 0.20f + gap, winR.bottom, cur)
            c.drawRect(winR.right - winR.width() * 0.20f - gap, winR.top, winR.right, winR.bottom, cur)
            if (parted && TownInfo.curtainWarm) {
                // somebody peeked: a soft shadow in the gap
                c.drawOval(RectF(winR.centerX() - 13f, winR.top + winR.height() * 0.30f,
                    winR.centerX() + 13f, winR.top + winR.height() * 0.62f),
                    Paper.fillPaint("#0c0c14", 130))
            }
        }
        c.drawRect(winR, Paper.strokePaint("#3a2a1a", 6f))
        c.drawLine(winR.centerX(), winR.top, winR.centerX(), winR.bottom, Paper.strokePaint("#3a2a1a", 4f))
        c.drawLine(winR.left, winR.centerY(), winR.right, winR.centerY(), Paper.strokePaint("#3a2a1a", 4f))

        // ---- room name sign ----
        Paper.drawSign(c, wf * 0.36f, hf * 0.055f, wf * 0.52f, hf * 0.042f,
            def.name, hf * 0.018f)

        // ---- v0.2.0a: signature set-dressing, wall layer ----
        drawInteriorWallDetails(c, wf, hf, def, night)

        // ---- props along the floor line ----
        val slots = listOf(0.26f, 0.41f, 0.56f, 0.71f, 0.86f)
        def.props.forEachIndexed { i, prop ->
            if (i >= slots.size) return@forEachIndexed
            drawProp(c, prop, wf * slots[i], floorY + hf * 0.015f, wf, hf, night)
        }

        // ---- v0.2.0a: signature set-dressing, floor layer ----
        drawInteriorFloorDetails(c, wf, hf, def, night)

        // ---- EXIT door (left) ----
        val dr = doorRect(wf, hf)
        c.drawRect(dr, Paper.fillPaint("#6b4a2f"))
        c.drawRect(dr, Paper.strokePaint("#3a2a1a", 5f))
        c.drawCircle(dr.right - 14f, dr.centerY(), 5f, Paper.fillPaint(Paper.YELLOW))
        Paper.drawSign(c, dr.centerX(), dr.top - hf * 0.028f, dr.width() * 1.15f,
            hf * 0.032f, "EXIT", hf * 0.016f)
        if (night) {
            c.drawOval(RectF(dr.left - 10, dr.top - 10, dr.right + 10, dr.bottom + 16),
                Paper.fillPaint("#f2c230", 26))
        }

        // ---- holiday garland (sparse lights on a string) ----
        if (holiday != null) {
            val gp = Paper.strokePaint("#2a5a2a", 3f)
            c.drawLine(0f, hf * 0.045f, wf, hf * 0.045f, gp)
            var gx = wf * 0.06f
            var k = 0
            while (gx < wf * 0.96f) {
                val col = listOf("#d43d2a", "#f2c230", "#2a9a4a", "#2a6fd4")[k % 4]
                c.drawCircle(gx, hf * 0.045f + 9f, 5f, Paper.fillPaint(col, 220))
                gx += wf * 0.09f
                k++
            }
        }

        if (night) c.drawRect(0f, 0f, wf, hf, Paper.fillPaint("#0a1030", 46))
        // v0.16.0d (stage 8): fibrous film grain, not the
        // retired speckle tile
        Paper.drawFibreGrain(c, 0f, 0f, wf, hf, seed + 29L)
    }

    // ============================================================
    // v0.15.0d — INSIDE THE SCHOOL BUS, rebuilt as a layered
    // paper interior off BusInteriorSpec's piece list:
    // a ceiling band, a window band with the street
    // outside, the route board, the driver area and seat
    // row backs on the back plane, the aisle floor, and
    // the near seat backs + rear door closing the front.
    // The player stands in the aisle at figure scale.
    // ============================================================
    private fun drawBusRoom(c: Canvas, w: Int, h: Int, def: InteriorDef,
                            timeMin: Int, weatherId: String, seed: Long) {
        val wf = w.toFloat()
        val hf = h.toFloat()
        val tod = SceneArt.timeOfDay(timeMin)
        val night = tod == "night"
        val yellow = def.wallHex
        val yellowDark = Paper.darker(yellow, 0.78f)
        val floorY = hf * 0.64f
        fun spec(kind: String) =
            BusInteriorSpec.PIECES.any { it.kind == kind }

        // ---- WALL: ceiling band + the bus's side wall ----
        c.drawRect(0f, 0f, wf, hf * BusInteriorSpec.WINDOW_TOP,
            Paper.fillPaint(yellowDark))
        c.drawRect(0f, hf * BusInteriorSpec.WINDOW_TOP, wf, floorY,
            Paper.fillPaint(yellow))
        // the grab rail along the ceiling edge
        c.drawRect(0f, hf * 0.145f, wf, hf * 0.155f,
            Paper.fillPaint("#8a8f96"))
        // the room's name, on the ceiling band
        if (spec(BusInteriorSpec.CEILING)) {
            Paper.drawSign(c, wf * 0.30f, hf * 0.028f, wf * 0.44f, hf * 0.034f,
                def.name, hf * 0.015f)
        }

        // ---- WALL: window band — the street outside ----
        if (spec(BusInteriorSpec.WINDOW_BAND)) {
            val wt = hf * BusInteriorSpec.WINDOW_TOP
            val wb = hf * BusInteriorSpec.WINDOW_BOTTOM
            val skyC = when (tod) {
                "day" -> if (weatherId == "sunny") "#7ab8e8" else "#8a97a8"
                "dusk" -> "#c86a4a"
                else -> "#141c33"
            }
            // one long band of street, then the frames cut it
            c.drawRect(0f, wt, wf, wb, Paper.fillPaint(skyC))
            // rooftops rolling past + the road at the sill
            val roofP = Paper.fillPaint(if (night) "#232c47" else "#6a7a94")
            var hx = -wf * 0.04f
            val hrnd = Random(seed + 31)
            while (hx < wf) {
                val hw = wf * (0.10f + hrnd.nextFloat() * 0.08f)
                val hh = (wb - wt) * (0.24f + hrnd.nextFloat() * 0.16f)
                c.drawRect(hx, wb - (wb - wt) * 0.30f - hh, hx + hw,
                    wb - (wb - wt) * 0.30f, roofP)
                hx += hw + wf * 0.015f
            }
            c.drawRect(0f, wb - (wb - wt) * 0.30f, wf, wb,
                Paper.fillPaint(if (night) "#232a38" else "#4a4f57"))
            c.drawRect(0f, wb - (wb - wt) * 0.16f, wf, wb - (wb - wt) * 0.13f,
                Paper.fillPaint("#e8c53a", 170))
            if (night) {
                val sr = Random(seed + 5)
                val sp = Paper.fillPaint("#ffffff", 190)
                repeat(10) {
                    c.drawCircle(sr.nextFloat() * wf,
                        wt + sr.nextFloat() * (wb - wt) * 0.5f, 2f, sp)
                }
            }
            // window frames: five honest windows
            val frameP = Paper.fillPaint(yellowDark)
            val nWin = 5
            for (i in 0..nWin) {
                val fx = wf * i / nWin
                c.drawRect(fx - wf * 0.008f, wt, fx + wf * 0.008f, wb, frameP)
            }
            c.drawRect(0f, wt, wf, wt + hf * 0.010f, frameP)
            c.drawRect(0f, wb - hf * 0.010f, wf, wb, frameP)
        }
        // the rub rail under the windows
        c.drawRect(0f, hf * 0.52f, wf, hf * 0.545f,
            Paper.fillPaint(yellowDark))

        // ---- WALL: route board ----
        if (spec(BusInteriorSpec.ROUTE_BOARD)) {
            Paper.drawSign(c, wf * 0.78f, hf * 0.565f, wf * 0.34f, hf * 0.038f,
                "SCHOOL ROUTE 4", hf * 0.015f)
        }

        // ---- FLOOR: the aisle ----
        c.drawRect(0f, floorY, wf, hf, Paper.fillPaint("#3f4652"))
        if (spec(BusInteriorSpec.AISLE)) {
            // the aisle runner down the middle, ribbed
            val aL = wf * 0.40f
            val aR = wf * 0.60f
            c.drawRect(aL, floorY, aR, hf, Paper.fillPaint(def.floorHex))
            val rib = Paper.strokePaint("#2c313a", 2f)
            var ry = floorY + hf * 0.03f
            while (ry < hf) {
                c.drawLine(aL + 3f, ry, aR - 3f, ry, rib)
                ry += hf * 0.035f
            }
        }

        // ---- FURNITURE_BACK: seat row backs ----
        val seat = "#2a6f8a"
        val seatDark = Paper.darker(seat, 0.72f)
        fun seatBack(x: Float, base: Float, sw: Float, sh: Float) {
            c.drawRoundRect(RectF(x, base - sh, x + sw, base), 6f, 6f,
                Paper.fillPaint(seat))
            c.drawRoundRect(RectF(x, base - sh, x + sw, base - sh * 0.62f),
                6f, 6f, Paper.fillPaint(seatDark))
            c.drawRect(x + sw * 0.10f, base, x + sw * 0.22f, base + sh * 0.14f,
                Paper.fillPaint("#22252b"))
            c.drawRect(x + sw * 0.78f, base, x + sw * 0.90f, base + sh * 0.14f,
                Paper.fillPaint("#22252b"))
        }
        if (spec(BusInteriorSpec.SEAT_BACKS)) {
            // rows along both walls, bases on the floor
            // line, growing a breath toward the front
            for (row in 0 until BusInteriorSpec.SEAT_ROWS) {
                val f = row / (BusInteriorSpec.SEAT_ROWS - 1f)
                val base = floorY + hf * (0.03f + f * 0.13f)
                val sw = wf * (0.105f + f * 0.02f)
                val sh = hf * (0.115f + f * 0.025f)
                val lx = wf * (0.045f + row * 0.008f)
                val rx = wf - lx - sw
                seatBack(lx, base, sw, sh)
                seatBack(rx, base, sw, sh)
            }
        }

        // ---- FURNITURE_BACK: the driver area, up front ----
        if (spec(BusInteriorSpec.DRIVER)) {
            val dx = wf * 0.86f
            // the dash
            c.drawRect(dx - wf * 0.015f, floorY - hf * 0.115f, wf, floorY,
                Paper.fillPaint("#2c2f36"))
            c.drawRect(dx - wf * 0.015f, floorY - hf * 0.115f, wf,
                floorY - hf * 0.10f, Paper.fillPaint("#454a54"))
            // the wheel, on its column
            val wx = dx + wf * 0.045f
            val wy = floorY - hf * 0.085f
            c.drawLine(wx, floorY - hf * 0.01f, wx - wf * 0.012f, wy,
                Paper.strokePaint("#1c1e24", 5f))
            c.drawCircle(wx - wf * 0.016f, wy, hf * 0.026f,
                Paper.strokePaint("#1c1e24", 5.5f))
            c.drawCircle(wx - wf * 0.016f, wy, hf * 0.007f,
                Paper.fillPaint("#1c1e24"))
            // the driver's seat, taller than the kids'
            seatBack(dx + wf * 0.055f, floorY + hf * 0.015f, wf * 0.075f,
                hf * 0.155f)
            // the rear-view mirror strip the driver glares into
            c.drawRect(wf * 0.70f, hf * 0.175f, wf * 0.97f, hf * 0.225f,
                Paper.fillPaint("#2a2a2a"))
            c.drawRect(wf * 0.706f, hf * 0.181f, wf * 0.964f, hf * 0.219f,
                Paper.fillPaint("#b8ccd8", 200))
        }

        // ---- FURNITURE_FRONT: the rear door (the way
        // out), standing on the floor line at the left ----
        if (spec(BusInteriorSpec.REAR_DOOR)) {
            val dr = doorRect(wf, hf)
            c.drawRect(dr, Paper.fillPaint(Paper.darker(yellow, 0.8f)))
            c.drawRect(dr, Paper.strokePaint("#3a2a1a", 5f))
            c.drawRect(dr.left + dr.width() * 0.14f, dr.top + dr.height() * 0.10f,
                dr.right - dr.width() * 0.14f, dr.top + dr.height() * 0.42f,
                Paper.fillPaint("#bcd6f2"))
            c.drawCircle(dr.right - 14f, dr.centerY(), 5f,
                Paper.fillPaint(Paper.YELLOW))
            Paper.drawSign(c, dr.centerX(), dr.top - hf * 0.028f,
                dr.width() * 1.15f, hf * 0.032f, "EXIT", hf * 0.016f)
        }

        if (night) c.drawRect(0f, 0f, wf, hf, Paper.fillPaint("#0a1030", 46))
        // v0.16.0d (stage 8): fibrous film grain, not the
        // retired speckle tile
        Paper.drawFibreGrain(c, 0f, 0f, wf, hf, seed + 29L)
    }

    // ============================================================
    // v0.2.0a — scene parity pass #2: interiors depth. Pass #1
    // (v0.1.9a) gave every EXTERIOR its defining details; the
    // rooms inside were still five generic props on a wall. Now
    // each interior carries its own signature set-dressing —
    // the furniture, wall dressing and objects that make the
    // room THAT room (lore/show knowledge, paraphrased, drawn
    // new in the same flat style). The registry of what belongs
    // in each room lives in CharacterSystem.INTERIOR_ART_DETAILS;
    // every piece is marked `// idetail: <token>` and the smoke
    // suite cross-checks registry vs. source, like pass #1.
    // Layout discipline (fractions of the room): wall pieces live
    // in the free bands left of the window (x .185–.575) and the
    // strip right of it (x .885–.985), clear of the door, the
    // room sign and the prop row; floor pieces stand in front of
    // the prop bases, behind the figures GameView draws after.
    // ============================================================

    /** Centered casual text for the detail layer's lettering. */
    private fun txt(c: Canvas, s: String, x: Float, y: Float, size: Float,
                    hex: String, bold: Boolean = true, alpha: Int = 255) {
        val p = Paper.textPaint(hex, size, bold = bold, casual = true)
        p.textAlign = Paint.Align.CENTER
        p.alpha = alpha
        c.drawText(s, x, y, p)
    }

    /** A simple five-point star (seals, chart rewards). */
    private fun star(c: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val path = Path()
        for (i in 0 until 10) {
            val ang = -Math.PI / 2 + i * Math.PI / 5
            val rad = if (i % 2 == 0) r else r * 0.45f
            val x = cx + (rad * kotlin.math.cos(ang)).toFloat()
            val y = cy + (rad * kotlin.math.sin(ang)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        c.drawPath(path, p)
    }

    private fun drawInteriorWallDetails(c: Canvas, w: Float, h: Float,
                                        def: InteriorDef, night: Boolean) {
        val accent = LOC_PALETTES[def.locId]?.accentHex ?: Paper.RED
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(hex, alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) = Paper.strokePaint(hex, sw, alpha)
        fun rr(l: Float, t: Float, r: Float, b: Float, p: Paint, rad: Float = 0f) =
            if (rad > 0f) c.drawRoundRect(RectF(l * w, t * h, r * w, b * h), rad, rad, p)
            else c.drawRect(RectF(l * w, t * h, r * w, b * h), p)
        fun ov(l: Float, t: Float, r: Float, b: Float, p: Paint) =
            c.drawOval(RectF(l * w, t * h, r * w, b * h), p)
        fun cir(x: Float, y: Float, r: Float, p: Paint) = c.drawCircle(x * w, y * h, r * w, p)
        fun ln(x1: Float, y1: Float, x2: Float, y2: Float, p: Paint) =
            c.drawLine(x1 * w, y1 * h, x2 * w, y2 * h, p)

        when (def.locId) {
            "KennysHouse" -> {
                // idetail: parka-row — a rail of identical orange
                // parkas; the McCormicks are ready for anything
                ln(0.22f, 0.155f, 0.50f, 0.155f, sp("#4a3a26", 3f))
                for (i in 0..3) {
                    val px = 0.245f + i * 0.06f
                    rr(px - 0.020f, 0.168f, px + 0.020f, 0.40f, fp(accent), 6f)
                    cir(px, 0.188f, 0.020f, fp(accent))
                    cir(px, 0.192f, 0.011f, fp("#5a2a10"))
                    ln(px, 0.215f, px, 0.392f, sp("#a85a1e", 2f))
                }
                // idetail: bare-bulb — one bulb on a cord, right side
                ln(0.93f, 0f, 0.93f, 0.13f, sp("#2a2a2a", 2.5f))
                if (night) cir(0.93f, 0.148f, 0.032f, fp("#f7e9a0", 36))
                cir(0.93f, 0.148f, 0.011f, fp("#f7e9a0", if (night) 255 else 205))
            }
            "ButtersHouse" -> {
                // idetail: chore-chart — stars for chores done,
                // an empty row where he got grounded instead
                rr(0.235f, 0.135f, 0.405f, 0.405f, fp("#fdfdf8"))
                rr(0.235f, 0.135f, 0.405f, 0.405f, sp("#7a9ab5", 3f))
                rr(0.25f, 0.15f, 0.39f, 0.175f, fp("#7a9ab5"))
                for (row in 0..2) {
                    val y = 0.235f + row * 0.06f
                    ln(0.25f, y, 0.39f, y, sp("#c8d4e0", 2f))
                    ln(0.25f, y + 0.045f, 0.355f, y + 0.045f, sp("#c8d4e0", 2f))
                }
                star(c, 0.375f * w, 0.252f * h, 0.009f * w, fp("#d9a441"))
                star(c, 0.375f * w, 0.312f * h, 0.009f * w, fp("#d9a441"))
                // idetail: photo-frames — the family, very neat
                for (i in 0..2) {
                    val fx = 0.465f + i * 0.045f
                    rr(fx - 0.017f, 0.155f, fx + 0.017f, 0.305f, fp("#6b4a2f"))
                    rr(fx - 0.013f, 0.165f, fx + 0.013f, 0.295f, fp("#e8eef4"))
                    cir(fx, 0.215f, 0.008f, fp("#8a6a4a"))
                    ov(fx - 0.011f, 0.245f, fx + 0.011f, 0.295f, fp("#8a6a4a"))
                }
            }
            "CartmanHouse" -> {
                // idetail: plush-frog — a beloved stuffed frog
                // keeps watch from the shelf
                rr(0.23f, 0.315f, 0.39f, 0.33f, fp("#6b4a2f"))
                ov(0.285f, 0.255f, 0.335f, 0.315f, fp("#58a03c"))
                cir(0.31f, 0.245f, 0.021f, fp("#58a03c"))
                cir(0.298f, 0.222f, 0.0075f, fp("#ffffff"))
                cir(0.322f, 0.222f, 0.0075f, fp("#ffffff"))
                cir(0.298f, 0.222f, 0.0035f, fp("#1a1a1a"))
                cir(0.322f, 0.222f, 0.0035f, fp("#1a1a1a"))
                c.drawArc(RectF(0.298f * w, 0.245f * h, 0.322f * w, 0.268f * h),
                    20f, 140f, false, sp("#2a5a1e", 2f))
            }
            "StanHouse" -> {
                // idetail: guitar — leaning by the wall, mid-life
                // crisis sold separately
                c.save()
                c.rotate(-18f, 0.235f * w, 0.30f * h)
                cir(0.235f, 0.35f, 0.030f, fp("#8a5a2a"))
                cir(0.235f, 0.295f, 0.023f, fp("#8a5a2a"))
                cir(0.235f, 0.325f, 0.010f, fp("#3a2410"))
                rr(0.228f, 0.13f, 0.242f, 0.28f, fp("#5a3a1a"))
                rr(0.226f, 0.115f, 0.244f, 0.135f, fp("#3a2410"))
                c.restore()
                // idetail: maple-poster — a Canadian comedy duo
                // tribute, leaves only, we know nothing
                rr(0.40f, 0.14f, 0.545f, 0.345f, fp("#f4efe2"))
                rr(0.40f, 0.14f, 0.545f, 0.345f, sp("#b03a2e", 3f))
                star(c, 0.4725f * w, 0.228f * h, 0.032f * w, fp("#c22f2f"))
                ln(0.4725f, 0.258f, 0.4725f, 0.295f, sp("#c22f2f", 3f))
            }
            "KyleHouse" -> {
                // idetail: menorah-shelf — nine small flames on
                // the shelf, lit for the holiday
                rr(0.235f, 0.325f, 0.405f, 0.34f, fp("#6b4a2f"))
                rr(0.317f, 0.272f, 0.323f, 0.325f, fp("#c8a028"))
                c.drawArc(RectF(0.262f * w, 0.238f * h, 0.378f * w, 0.325f * h),
                    180f, 180f, false, sp("#c8a028", 3.5f))
                for (i in 0..8) {
                    val fx = 0.268f + i * 0.013f
                    val t = (i - 4) / 4f
                    val topY = 0.252f + t * t * 0.020f
                    rr(fx - 0.0018f, topY, fx + 0.0018f, topY + 0.048f, fp("#e8e4da"))
                    c.drawCircle(fx * w, (topY - 0.007f) * h, 2.8f,
                        fp("#f2a230", if (night) 255 else 200))
                }
                // idetail: crayon-drawing — a kid's masterpiece,
                // pinned up with pride
                rr(0.435f, 0.15f, 0.565f, 0.335f, fp("#fdfdf6"))
                rr(0.435f, 0.15f, 0.565f, 0.335f, sp("#d8d8d0", 2f))
                rr(0.475f, 0.24f, 0.525f, 0.30f, sp("#d43d2a", 2.5f))
                c.drawPath(Path().apply {
                    moveTo(0.470f * w, 0.24f * h)
                    lineTo(0.50f * w, 0.205f * h)
                    lineTo(0.530f * w, 0.24f * h)
                }, sp("#d43d2a", 2.5f))
                cir(0.545f, 0.175f, 0.008f, sp("#e8a020", 2.5f))
                ln(0.445f, 0.30f, 0.555f, 0.30f, sp("#2a9a4a", 2.5f))
            }
            "TolkienHouse" -> {
                // idetail: gold-frames — portraits in gold,
                // because this house can
                for (fx in listOf(0.29f, 0.475f)) {
                    rr(fx - 0.057f, 0.125f, fx + 0.057f, 0.385f, fp("#c9a227"))
                    rr(fx - 0.049f, 0.14f, fx + 0.049f, 0.37f, fp("#4a3a30"))
                    cir(fx, 0.225f, 0.020f, fp("#8a6a4a"))
                    ov(fx - 0.032f, 0.255f, fx + 0.032f, 0.36f, fp("#8a6a4a"))
                }
                // idetail: chandelier — over the right side,
                // five patient candles
                ln(0.93f, 0f, 0.93f, 0.095f, sp("#3a3a3a", 2.5f))
                ov(0.904f, 0.112f, 0.956f, 0.138f, sp("#c9a227", 3f))
                for (fx in listOf(0.908f, 0.919f, 0.93f, 0.941f, 0.952f)) {
                    rr(fx - 0.0022f, 0.098f, fx + 0.0022f, 0.115f, fp("#e8e4da"))
                    c.drawCircle(fx * w, 0.092f * h, 2.6f,
                        fp("#f2a230", if (night) 255 else 195))
                }
            }
            "BusStop" -> {
                // idetail: mirror-strip — the long rear-view
                // mirror every bus driver glares into
                rr(0.20f, 0.115f, 0.46f, 0.175f, fp("#2a2a2a"))
                rr(0.208f, 0.122f, 0.452f, 0.168f, fp("#b8ccd8", 200))
                ln(0.23f, 0.165f, 0.33f, 0.125f, sp("#ffffff", 3f, 90))
                // idetail: route-board — lettered, like the
                // sign on the real thing
                Paper.drawSign(c, 0.44f * w, 0.29f * h, 0.30f * w, 0.042f * h,
                    "SCHOOL ROUTE 4", 0.017f * h)
            }
            "MainStreet" -> {
                // idetail: wall-clock — diner time runs slow
                cir(0.30f, 0.21f, 0.032f, fp("#fdfdf8"))
                cir(0.30f, 0.21f, 0.032f, sp("#3a3a3a", 4f))
                ln(0.30f, 0.21f, 0.30f, 0.186f, sp("#3a3a3a", 3f))
                ln(0.30f, 0.21f, 0.314f, 0.219f, sp("#3a3a3a", 3f))
                cir(0.30f, 0.21f, 0.0035f, fp("#3a3a3a"))
                // idetail: pie-case — under glass, always one
                // slice missing
                rr(0.40f, 0.295f, 0.575f, 0.44f, fp("#8a8a8a"))
                rr(0.408f, 0.303f, 0.567f, 0.415f, fp("#cfe4ee", 110))
                ov(0.417f, 0.407f, 0.558f, 0.423f, fp("#e8e8e8"))
                c.drawPath(Path().apply {
                    moveTo(0.45f * w, 0.413f * h)
                    lineTo(0.525f * w, 0.413f * h)
                    lineTo(0.4875f * w, 0.372f * h)
                    close()
                }, fp("#d98a3a"))
            }
            "TweekBros" -> {
                // idetail: profit-chart — the business plan,
                // in three phases, phase two is fine
                rr(0.215f, 0.13f, 0.465f, 0.425f, fp("#2e3a34"))
                rr(0.215f, 0.13f, 0.465f, 0.425f, sp("#6b4a2f", 5f))
                txt(c, "THE PLAN", 0.34f * w, 0.175f * h, 0.014f * h, "#9fb8a8")
                txt(c, "1: COFFEE", 0.34f * w, 0.245f * h, 0.017f * h, "#f0ead8")
                txt(c, "2: ???", 0.34f * w, 0.315f * h, 0.017f * h, "#f0ead8")
                txt(c, "3: PROFIT", 0.34f * w, 0.385f * h, 0.017f * h, "#f2c230")
                // idetail: cup-shelf — clean cups, hanging ready
                rr(0.47f, 0.30f, 0.585f, 0.315f, fp("#4a3423"))
                for (fx in listOf(0.49f, 0.525f, 0.56f)) {
                    rr(fx - 0.011f, 0.318f, fx + 0.011f, 0.358f, fp("#f0e8d0"))
                    c.drawArc(RectF((fx + 0.008f) * w, 0.325f * h,
                        (fx + 0.022f) * w, 0.35f * h), -80f, 160f, false,
                        sp("#f0e8d0", 2f))
                }
            }
            "JimbosGuns" -> {
                // idetail: rifle-rack — the wall of freedom,
                // safety on, probably
                rr(0.22f, 0.17f, 0.46f, 0.185f, fp("#4a3423"))
                rr(0.22f, 0.345f, 0.46f, 0.36f, fp("#4a3423"))
                for (fx in listOf(0.26f, 0.34f, 0.42f)) {
                    rr(fx - 0.003f, 0.15f, fx + 0.003f, 0.33f, fp("#2a2a2a"))
                    rr(fx - 0.006f, 0.24f, fx + 0.006f, 0.348f, fp("#6b4423"))
                }
                // idetail: deer-mount — he was coming right
                // for them, allegedly
                rr(0.487f, 0.15f, 0.543f, 0.33f, fp("#7a5230"), 6f)
                ov(0.50f, 0.20f, 0.53f, 0.29f, fp("#8a6a4a"))
                ov(0.506f, 0.262f, 0.524f, 0.30f, fp("#77572f"))
                val ant = sp("#d8cbb2", 3f)
                ln(0.503f, 0.205f, 0.49f, 0.155f, ant)
                ln(0.496f, 0.175f, 0.484f, 0.165f, ant)
                ln(0.527f, 0.205f, 0.54f, 0.155f, ant)
                ln(0.534f, 0.175f, 0.546f, 0.165f, ant)
                // idetail: hunting-sign — the rule of the woods,
                // lettered over the counter
                Paper.drawSign(c, 0.40f * w, 0.435f * h, 0.36f * w, 0.036f * h,
                    "IT'S COMING RIGHT FOR US!", 0.0135f * h)
            }
            "CityWok" -> {
                // idetail: lantern-string — red paper lanterns
                // strung over the dining room
                c.drawPath(Path().apply {
                    moveTo(0.20f * w, 0.085f * h)
                    quadTo(0.39f * w, 0.135f * h, 0.58f * w, 0.085f * h)
                }, sp("#3a2a1a", 2f))
                for ((lx, ly) in listOf(0.2475f to 0.102f, 0.3425f to 0.115f,
                    0.4375f to 0.115f, 0.5325f to 0.102f)) {
                    ov(lx - 0.013f, ly, lx + 0.013f, ly + 0.045f, fp("#d43d2a"))
                    ln(lx, ly, lx, ly + 0.045f, sp("#a5242a", 1.5f, 150))
                    rr(lx - 0.006f, ly - 0.004f, lx + 0.006f, ly + 0.002f, fp("#c8a028"))
                }
                // idetail: city-sign — the menu board, lettered
                // exactly the way the owner says it
                rr(0.235f, 0.155f, 0.525f, 0.425f, fp("#f7ead2"))
                rr(0.235f, 0.155f, 0.525f, 0.425f, sp("#c0392b", 5f))
                txt(c, "CITY WOK", 0.38f * w, 0.228f * h, 0.026f * h, "#c0392b")
                txt(c, "CITY BEEF", 0.38f * w, 0.292f * h, 0.015f * h, "#3a2a1a")
                txt(c, "CITY CHICKEN", 0.38f * w, 0.338f * h, 0.015f * h, "#3a2a1a")
                txt(c, "CITY SHRIMP", 0.38f * w, 0.384f * h, 0.015f * h, "#3a2a1a")
            }
            "SkeetersBar" -> {
                // idetail: bar-neon — the sign buzzes a little,
                // nobody fixes it
                rr(0.245f, 0.155f, 0.555f, 0.29f, fp("#241a12", 235))
                txt(c, "SKEETER'S", 0.40f * w, 0.248f * h, 0.032f * h,
                    accent, alpha = if (night) 110 else 70)
                txt(c, "SKEETER'S", 0.40f * w, 0.248f * h, 0.030f * h, "#d9ffe0")
                // idetail: beer-taps — three handles, one is
                // always "the good stuff"
                rr(0.465f, 0.335f, 0.58f, 0.44f, fp("#4a3423"))
                for ((i, fx) in listOf(0.485f, 0.5225f, 0.56f).withIndex()) {
                    val knob = listOf("#d43d2a", "#f2c230", "#2a6fd4")[i]
                    rr(fx - 0.007f, 0.265f, fx + 0.007f, 0.30f, fp(knob))
                    rr(fx - 0.004f, 0.30f, fx + 0.004f, 0.352f, fp("#c8c8c8"))
                    rr(fx - 0.009f, 0.352f, fx + 0.009f, 0.362f, fp("#8a8a8a"))
                }
            }
            "Bijou" -> {
                // idetail: screen-curtains — velvet swags hug
                // the big screen (first prop slot)
                rr(0.15f, 0.115f, 0.375f, 0.16f, fp("#8a1f2d"))
                for (i in 0..4) cir(0.172f + i * 0.045f, 0.16f, 0.014f, fp("#8a1f2d"))
                rr(0.15f, 0.16f, 0.182f, 0.46f, fp("#a52436"))
                rr(0.343f, 0.16f, 0.375f, 0.46f, fp("#a52436"))
                ln(0.158f, 0.17f, 0.158f, 0.45f, sp("#7e1a29", 2.5f))
                ln(0.367f, 0.17f, 0.367f, 0.45f, sp("#7e1a29", 2.5f))
                cir(0.182f, 0.38f, 0.005f, fp("#c8a028"))
                cir(0.343f, 0.38f, 0.005f, fp("#c8a028"))
                // idetail: lobby-posters — coming soon, forever
                rr(0.89f, 0.13f, 0.975f, 0.27f, fp("#2a2a3a"))
                rr(0.897f, 0.137f, 0.968f, 0.263f, fp("#e8e2d2"))
                c.drawPath(Path().apply {
                    moveTo(0.905f * w, 0.24f * h)
                    lineTo(0.9325f * w, 0.165f * h)
                    lineTo(0.96f * w, 0.24f * h)
                    close()
                }, fp("#7a9a7b"))
                cir(0.915f, 0.165f, 0.008f, fp("#e8c53a"))
                rr(0.89f, 0.285f, 0.975f, 0.425f, fp("#2a2a3a"))
                rr(0.897f, 0.292f, 0.968f, 0.418f, fp("#33254d"))
                cir(0.9325f, 0.345f, 0.016f, fp("#c86a9a"))
                cir(0.912f, 0.315f, 0.0035f, fp("#ffffff"))
                cir(0.95f, 0.38f, 0.0035f, fp("#ffffff"))
            }
            "TomsRhino" -> {
                // idetail: nose-chart — before, before, after
                rr(0.215f, 0.135f, 0.425f, 0.405f, fp("#fdfdf8"))
                rr(0.215f, 0.135f, 0.425f, 0.405f, sp("#c8a0b0", 4f))
                rr(0.23f, 0.15f, 0.41f, 0.175f, fp("#e8a0b0"))
                for ((i, cy) in listOf(0.225f, 0.30f, 0.375f).withIndex()) {
                    c.drawPath(Path().apply {
                        moveTo(0.27f * w, (cy - 0.022f) * h)
                        lineTo(0.273f * w, (cy - 0.002f) * h)
                        quadTo(0.298f * w, (cy + 0.004f) * h, 0.293f * w, (cy + 0.012f) * h)
                        quadTo(0.29f * w, (cy + 0.018f) * h, 0.28f * w, (cy + 0.018f) * h)
                        lineTo(0.308f * w, (cy + 0.02f) * h)
                    }, sp("#5a4a52", 2.5f))
                    txt(c, if (i == 2) "✓" else "✗", 0.365f * w, (cy + 0.012f) * h,
                        0.018f * h, if (i == 2) "#2a9a4a" else "#c0392b")
                }
                // idetail: hoff-poster — don't hassle the poster
                rr(0.44f, 0.15f, 0.575f, 0.385f, fp("#2a2a3a"))
                rr(0.44f, 0.15f, 0.575f, 0.385f, sp(accent, 4f))
                rr(0.462f, 0.21f, 0.498f, 0.245f, fp("#0a0a0a"), 4f)
                rr(0.517f, 0.21f, 0.553f, 0.245f, fp("#0a0a0a"), 4f)
                ln(0.498f, 0.222f, 0.517f, 0.222f, sp("#0a0a0a", 3f))
                c.drawArc(RectF(0.485f * w, 0.255f * h, 0.53f * w, 0.30f * h),
                    20f, 140f, false, sp("#e8e8e8", 2.5f))
                txt(c, "THE HOFF", 0.5075f * w, 0.352f * h, 0.02f * h, "#f5d76e")
            }
            "Bank" -> {
                // idetail: teller-bars — your money is behind
                // these, being invested, probably
                rr(0.22f, 0.15f, 0.445f, 0.42f, fp("#8a8a8a"))
                rr(0.22f, 0.15f, 0.445f, 0.42f, sp("#4a4a52", 4f))
                rr(0.235f, 0.165f, 0.43f, 0.375f, fp("#d8d4c4"))
                for (bx in listOf(0.27f, 0.305f, 0.34f, 0.375f, 0.41f))
                    ln(bx, 0.165f, bx, 0.375f, sp("#4a4a52", 4f))
                rr(0.225f, 0.375f, 0.44f, 0.395f, fp("#6a6a6a"))
                // idetail: gone-plaque — the bank's motto,
                // engraved with pride
                Paper.drawSign(c, 0.522f * w, 0.30f * h, 0.148f * w, 0.038f * h,
                    "AND... IT'S GONE", 0.013f * h)
            }
            "PostOffice" -> {
                // idetail: wanted-poster — reward: one stamp
                rr(0.24f, 0.135f, 0.365f, 0.385f, fp("#f0e6cc"))
                rr(0.24f, 0.135f, 0.365f, 0.385f, sp("#8a6a4a", 3f))
                txt(c, "WANTED", 0.3025f * w, 0.178f * h, 0.021f * h, "#3a2a1a")
                cir(0.3025f, 0.235f, 0.016f, fp("#3a3a3a"))
                ov(0.2825f, 0.258f, 0.3225f, 0.30f, fp("#3a3a3a"))
                ln(0.255f, 0.328f, 0.35f, 0.328f, sp("#8a7a5c", 2f))
                ln(0.255f, 0.35f, 0.335f, 0.35f, sp("#8a7a5c", 2f))
            }
            "FreemansTacos" -> {
                // idetail: taco-menu-boards — narrated menus
                // cost extra, obviously
                rr(0.215f, 0.14f, 0.395f, 0.365f, fp("#33302c"))
                rr(0.215f, 0.14f, 0.395f, 0.365f, sp("#94522c", 4f))
                txt(c, "TACOS", 0.305f * w, 0.192f * h, 0.02f * h, "#f39c12")
                rr(0.415f, 0.14f, 0.575f, 0.365f, fp("#33302c"))
                rr(0.415f, 0.14f, 0.575f, 0.365f, sp("#94522c", 4f))
                txt(c, "BURRITOS", 0.495f * w, 0.192f * h, 0.018f * h, "#f39c12")
                for (by in listOf(0.245f, 0.29f, 0.335f)) {
                    ln(0.235f, by, 0.375f, by, sp("#f0e8d0", 2f, 170))
                    ln(0.435f, by, 0.555f, by, sp("#f0e8d0", 2f, 170))
                }
                // idetail: pepper-string — a ristra by the
                // door, for luck and tacos
                ln(0.93f, 0.10f, 0.93f, 0.40f, sp("#2a5a2a", 2f))
                for (i in 0..5) {
                    val px = 0.93f + if (i % 2 == 0) -0.008f else 0.008f
                    val py = 0.125f + i * 0.048f
                    ov(px - 0.006f, py, px + 0.006f, py + 0.030f, fp("#d43d2a"))
                    cir(px, py + 0.002f, 0.003f, fp("#2a5a2a"))
                }
            }
            "Raisins" -> {
                // idetail: round-sign — big, round, orange,
                // impossible to miss, that's the point
                cir(0.30f, 0.245f, 0.058f, fp("#e8932a"))
                cir(0.30f, 0.245f, 0.058f, sp("#f7ead2", 5f))
                cir(0.30f, 0.245f, 0.047f, sp("#f7ead2", 2f))
                txt(c, "RAISINS", 0.30f * w, 0.253f * h, 0.017f * h, "#f7ead2")
                // idetail: name-plates — the staff wall, named
                // after cars, all of them
                for ((i, cy) in listOf(0.17f, 0.25f, 0.33f).withIndex()) {
                    val nm = listOf("LEXUS", "PORSCHE", "FERRARI")[i]
                    rr(0.435f, cy - 0.016f, 0.565f, cy + 0.016f, fp("#3a3a3a"))
                    rr(0.435f, cy - 0.016f, 0.565f, cy + 0.016f, sp(accent, 2f))
                    txt(c, nm, 0.50f * w, (cy + 0.005f) * h, 0.013f * h, "#f0e8d0")
                }
            }
            "Church" -> {
                // idetail: rose-window — the good window,
                // six petals of somebody's donation
                val rcx = 0.30f * w; val rcy = 0.225f * h; val rad = 0.072f * w
                val petalCols = listOf("#c86a9a", "#7a9ac8", "#f2c230")
                for (i in 0..5) {
                    c.drawArc(RectF(rcx - rad, rcy - rad, rcx + rad, rcy + rad),
                        i * 60f, 60f, true, fp(petalCols[i % 3], 205))
                }
                cir(0.30f, 0.225f, 0.026f, fp("#f2e2a0"))
                cir(0.30f, 0.225f, 0.072f, sp("#4a4a52", 5f))
                cir(0.30f, 0.225f, 0.026f, sp("#4a4a52", 3f))
                // idetail: hymn-board — today's numbers,
                // chosen by dart, allegedly
                rr(0.435f, 0.15f, 0.565f, 0.365f, fp("#4a3423"))
                rr(0.435f, 0.15f, 0.565f, 0.365f, sp("#c8a028", 3f))
                txt(c, "HYMN 42", 0.50f * w, 0.208f * h, 0.014f * h, "#f0ead8")
                txt(c, "HYMN 108", 0.50f * w, 0.268f * h, 0.014f * h, "#f0ead8")
                txt(c, "HYMN 7", 0.50f * w, 0.328f * h, 0.014f * h, "#f0ead8")
                // idetail: organ-pipes — four pipes, one
                // wheezes, nobody talks about it
                rr(0.895f, 0.40f, 0.98f, 0.44f, fp("#4a3423"))
                for ((i, px) in listOf(0.908f, 0.93f, 0.952f, 0.972f).withIndex()) {
                    val top = listOf(0.14f, 0.115f, 0.14f, 0.19f)[i]
                    rr(px - 0.0075f, top, px + 0.0075f, 0.40f, fp("#b8b0a0"))
                    rr(px - 0.0075f, top, px + 0.0075f, 0.40f, sp("#6a6258", 2f))
                    rr(px - 0.004f, 0.345f, px + 0.004f, 0.365f, fp("#4a4238"))
                }
            }
            "CityHall" -> {
                // idetail: mayor-portrait — Her Honor,
                // watching the meeting watch her
                rr(0.235f, 0.13f, 0.405f, 0.40f, fp("#6a2a2a"))
                rr(0.25f, 0.145f, 0.39f, 0.35f, fp("#2a4a5a"))
                cir(0.32f, 0.225f, 0.024f, fp("#c8a080"))
                ov(0.275f, 0.258f, 0.365f, 0.35f, fp("#3a3a4a"))
                rr(0.27f, 0.362f, 0.37f, 0.385f, fp("#c9a227"))
                // idetail: minutes-board — democracy, itemized
                rr(0.425f, 0.15f, 0.58f, 0.385f, fp("#fdfdf6"))
                rr(0.425f, 0.15f, 0.58f, 0.385f, sp("#7a6a4a", 3f))
                txt(c, "TOWN MEETING", 0.5025f * w, 0.192f * h, 0.015f * h, "#3a2a1a")
                txt(c, "AGENDA:", 0.5025f * w, 0.238f * h, 0.013f * h, "#3a2a1a")
                txt(c, "1. NOTHING", 0.5025f * w, 0.278f * h, 0.013f * h, "#3a2a1a")
                txt(c, "2. ADJOURN", 0.5025f * w, 0.318f * h, 0.013f * h, "#3a2a1a")
            }
            "Elementary" -> {
                // idetail: hall-of-honor — the memorial wall,
                // a chef's hat under glass, respect
                rr(0.22f, 0.135f, 0.385f, 0.385f, fp("#3a2e28"))
                rr(0.22f, 0.135f, 0.385f, 0.385f, sp("#c9a227", 4f))
                rr(0.235f, 0.15f, 0.37f, 0.33f, fp("#241f1c"))
                cir(0.29f, 0.235f, 0.011f, fp("#f0f0f0"))
                cir(0.3025f, 0.228f, 0.013f, fp("#f0f0f0"))
                cir(0.315f, 0.235f, 0.011f, fp("#f0f0f0"))
                rr(0.282f, 0.242f, 0.323f, 0.267f, fp("#f0f0f0"))
                txt(c, "HALL OF HONOR", 0.3025f * w, 0.368f * h, 0.0125f * h, "#c9a227")
                // idetail: cubby-row — six cubbies, somebody's
                // lunch is already missing. v0.17.0b: the row
                // belongs to the HALL span (0.00–0.20 of the
                // wall); it used to sit at 0.40–0.58, inside
                // the CAFETERIA's fifth, with bare placeholder
                // shapes (flat block / disc / slab) for contents
                rr(0.01f, 0.26f, 0.19f, 0.44f, fp("#a8763e"))
                for (gx in listOf(0.07f, 0.13f))
                    ln(gx, 0.26f, gx, 0.44f, sp("#7a5228", 3f))
                ln(0.01f, 0.35f, 0.19f, 0.35f, sp("#7a5228", 3f))
                rr(0.01f, 0.26f, 0.19f, 0.44f, sp("#7a5228", 3f))
                // a red lunchbox in the first cubby: lid line,
                // handle and clasp — an object, not a block
                rr(0.025f, 0.285f, 0.045f, 0.335f, fp("#c0452f"))
                ln(0.025f, 0.30f, 0.045f, 0.30f, sp("#7a2a1e", 2f))
                ln(0.030f, 0.285f, 0.040f, 0.285f, sp("#7a2a1e", 2f))
                // a blue playground ball in the last cubby:
                // a disc with a lighter inner ring
                cir(0.16f, 0.315f, 0.011f, fp("#2a6fd4"))
                cir(0.16f, 0.315f, 0.005f, fp("#7ab0e8"))
                // a green backpack in the middle-lower cubby:
                // body, front pocket, handle loop
                rr(0.085f, 0.375f, 0.115f, 0.415f, fp("#2a9a4a"))
                rr(0.093f, 0.39f, 0.107f, 0.415f, fp("#1f7a38"))
                ln(0.095f, 0.375f, 0.105f, 0.375f, sp("#1f7a38", 2f))
            }
            "KidsPark" -> {
                // idetail: pavilion-banner — lettered bunting
                // over the pavilion's back wall
                rr(0.21f, 0.095f, 0.59f, 0.165f, fp(accent))
                for (i in 0..11) cir(0.225f + i * 0.032f, 0.165f, 0.012f, fp(accent))
                txt(c, "KID'S PARK", 0.40f * w, 0.147f * h, 0.021f * h, "#ffffff")
                // idetail: notice-board — lost gloves, found
                // gloves, one ransom note
                rr(0.885f, 0.135f, 0.98f, 0.405f, fp("#b08954"))
                rr(0.885f, 0.135f, 0.98f, 0.405f, sp("#6b4a2f", 4f))
                Paper.crooked(c, -4f, 0.917f * w, 0.185f * h) {
                    rr(0.90f, 0.16f, 0.935f, 0.21f, fp("#fdfdf6"))
                }
                Paper.crooked(c, 3f, 0.952f * w, 0.255f * h) {
                    rr(0.935f, 0.23f, 0.97f, 0.28f, fp("#f5d76e"))
                }
                Paper.crooked(c, -2f, 0.922f * w, 0.315f * h) {
                    rr(0.905f, 0.29f, 0.94f, 0.34f, fp("#c8e2f2"))
                }
                cir(0.917f, 0.163f, 0.0035f, fp("#d43d2a"))
                cir(0.952f, 0.233f, 0.0035f, fp("#d43d2a"))
                cir(0.922f, 0.293f, 0.0035f, fp("#d43d2a"))
            }
            "StarksPond" -> {
                // idetail: fish-mount — the one that didn't
                // get away, mounted over the stove's wall
                rr(0.235f, 0.185f, 0.425f, 0.315f, fp("#6b4a2f"), 6f)
                ov(0.26f, 0.225f, 0.385f, 0.275f, fp("#7a8a6a"))
                c.drawPath(Path().apply {
                    moveTo(0.385f * w, 0.225f * h)
                    lineTo(0.41f * w, 0.21f * h)
                    lineTo(0.41f * w, 0.29f * h)
                    lineTo(0.385f * w, 0.275f * h)
                    close()
                }, fp("#7a8a6a"))
                cir(0.275f, 0.245f, 0.004f, fp("#1a1a1a"))
                c.drawPath(Path().apply {
                    moveTo(0.31f * w, 0.228f * h)
                    lineTo(0.335f * w, 0.205f * h)
                    lineTo(0.345f * w, 0.23f * h)
                    close()
                }, fp("#5f7050"))
            }
            "Mall" -> {
                // idetail: sale-windows — half the stores are
                // closed; the other half are having a SALE
                rr(0.195f, 0.14f, 0.465f, 0.40f, fp(accent))
                rr(0.21f, 0.155f, 0.45f, 0.385f, fp("#cfe0e8", 190))
                ln(0.33f, 0.155f, 0.33f, 0.385f, sp("#3a6a72", 3f))
                ln(0.21f, 0.27f, 0.45f, 0.27f, sp("#3a6a72", 3f))
                for ((sx, scol) in listOf(0.26f to "#d4688a", 0.365f to "#7a9a7b")) {
                    rr(sx, 0.21f, sx + 0.035f, 0.26f, fp(scol))
                    c.drawPath(Path().apply {
                        moveTo(sx * w, 0.215f * h)
                        lineTo((sx - 0.012f) * w, 0.235f * h)
                        lineTo(sx * w, 0.245f * h)
                        close()
                    }, fp(scol))
                    c.drawPath(Path().apply {
                        moveTo((sx + 0.035f) * w, 0.215f * h)
                        lineTo((sx + 0.047f) * w, 0.235f * h)
                        lineTo((sx + 0.035f) * w, 0.245f * h)
                        close()
                    }, fp(scol))
                }
                rr(0.285f, 0.315f, 0.375f, 0.36f, fp("#d43d2a"))
                txt(c, "SALE", 0.33f * w, 0.349f * h, 0.016f * h, "#ffffff")
                // idetail: directory-board — you are here,
                // the food court is eternal
                rr(0.475f, 0.15f, 0.58f, 0.40f, fp("#2f3a42"))
                rr(0.475f, 0.15f, 0.58f, 0.40f, sp(accent, 3f))
                txt(c, "YOU ARE HERE", 0.5275f * w, 0.188f * h, 0.012f * h, "#ffffff")
                cir(0.5275f, 0.228f, 0.006f, fp("#d43d2a"))
                for ((i, ly) in listOf(0.27f, 0.30f, 0.33f, 0.36f).withIndex())
                    ln(0.49f, ly, if (i == 3) 0.535f else 0.565f, ly, sp("#9fb6c2", 2.5f))
            }
            "SoDoSoPa" -> {
                // idetail: brick-band — exposed brick, exposed
                // on purpose, it cost extra
                rr(0.20f, 0.295f, 0.58f, 0.445f, fp("#a85a48"))
                for (ry in listOf(0.335f, 0.375f, 0.415f))
                    ln(0.20f, ry, 0.58f, ry, sp("#d8b09a", 2f, 160))
                for ((row, ry) in listOf(0.295f, 0.335f, 0.375f).withIndex()) {
                    var bx = 0.225f + (row % 2) * 0.022f
                    while (bx < 0.575f) {
                        ln(bx, ry, bx, ry + 0.04f, sp("#d8b09a", 2f, 140))
                        bx += 0.045f
                    }
                }
                // idetail: craft-sign — one word, glowing,
                // said out loud constantly
                rr(0.29f, 0.16f, 0.51f, 0.255f, fp("#2e2230", 235))
                txt(c, "CRAFT", 0.40f * w, 0.232f * h, 0.030f * h, accent,
                    alpha = if (night) 110 else 70)
                txt(c, "CRAFT", 0.40f * w, 0.232f * h, 0.028f * h, "#f2d7ea")
            }
            "TegridyFarms" -> {
                // idetail: tegridy-banner — the brand, hung
                // over the barn's back wall with pride
                rr(0.22f, 0.13f, 0.52f, 0.24f, fp("#2e5a2a"))
                rr(0.22f, 0.13f, 0.52f, 0.24f, sp("#c8a028", 3f))
                txt(c, "TEGRIDY FARMS", 0.355f * w, 0.20f * h, 0.019f * h, "#f0ead8")
                ov(0.475f, 0.155f, 0.505f, 0.215f, fp("#4a9a3a"))
                ln(0.49f, 0.215f, 0.49f, 0.155f, sp("#2e5a2a", 2f))
                // idetail: jar-shelf — the harvest, jarred and
                // labeled by a towel
                rr(0.235f, 0.315f, 0.445f, 0.33f, fp("#6b4a2f"))
                for (jx in listOf(0.255f, 0.305f, 0.355f, 0.405f)) {
                    rr(jx - 0.016f, 0.255f, jx + 0.016f, 0.315f, fp("#cfe0c8", 170))
                    rr(jx - 0.016f, 0.285f, jx + 0.016f, 0.315f, fp("#4a7a3a", 200))
                    rr(jx - 0.017f, 0.245f, jx + 0.017f, 0.257f, fp("#8a8a8a"))
                }
            }
            "YourHouse" -> {
                // idetail: band-poster — a gig poster from a show
                // nobody can prove happened, left of the shelf
                rr(0.195f, 0.13f, 0.275f, 0.35f, fp("#2a2f45"))
                rr(0.195f, 0.13f, 0.275f, 0.35f, sp("#c9a227", 3f))
                cir(0.235f, 0.205f, 0.026f, fp("#e8c53a"))
                ln(0.212f, 0.30f, 0.258f, 0.26f, sp("#f4ecd8", 3f))
                ln(0.258f, 0.26f, 0.252f, 0.315f, sp("#f4ecd8", 3f))
                txt(c, "LIVE?", 0.235f * w, 0.335f * h, 0.016f * h, "#f4ecd8")
                // idetail: cork-board — the new kid's evidence
                // board: notes, a map, one (1) feeling
                rr(0.30f, 0.37f, 0.52f, 0.495f, fp("#b59a6b"))
                rr(0.30f, 0.37f, 0.52f, 0.495f, sp("#6b4a2f", 3.5f))
                rr(0.325f, 0.40f, 0.385f, 0.45f, fp("#f4ecd8"))
                rr(0.40f, 0.395f, 0.455f, 0.44f, fp("#dfe8d8"))
                rr(0.435f, 0.445f, 0.50f, 0.485f, fp("#f0d8d8"))
                ln(0.33f, 0.415f, 0.38f, 0.415f, sp("#8a8578", 1.5f))
                ln(0.33f, 0.432f, 0.375f, 0.432f, sp("#8a8578", 1.5f))
                cir(0.355f, 0.398f, 0.006f, fp("#c22e2e"))
                cir(0.427f, 0.393f, 0.006f, fp("#2a6fd4"))
                cir(0.467f, 0.443f, 0.006f, fp("#2a9a4a"))
                // ---- v0.3.0d decor: earned pieces (House.DECOR),
                // drawn only when TownInfo.houseDecor holds the
                // token — the room fills as its owner plays ----
                if ("episode-poster" in TownInfo.houseDecor) {
                    // decor: episode-poster — a framed still from
                    // your first episode, right-strip wall
                    rr(0.885f, 0.42f, 0.975f, 0.58f, fp("#1d1d26"))
                    rr(0.885f, 0.42f, 0.975f, 0.58f, sp("#c9a227", 3f))
                    rr(0.897f, 0.435f, 0.963f, 0.53f, fp("#7ab8e8"))
                    rr(0.897f, 0.50f, 0.963f, 0.53f, fp("#eef2f6"))
                    cir(0.918f, 0.478f, 0.011f, fp("#f2c230"))
                    txt(c, "EP. 1", 0.93f * w, 0.568f * h, 0.015f * h, "#f4ecd8")
                }
                if ("photo-strip" in TownInfo.houseDecor) {
                    // decor: photo-strip — three tiny frames in a
                    // row, pinned at the cork board's right edge
                    for (k in 0..2) {
                        val fy = 0.378f + k * 0.040f
                        rr(0.532f, fy, 0.562f, fy + 0.034f, fp("#f4ecd8"))
                        rr(0.535f, fy + 0.004f, 0.559f, fy + 0.024f,
                            fp(listOf("#7ab8e8", "#e87a8a", "#8ac87a")[k]))
                    }
                    cir(0.547f, 0.372f, 0.006f, fp("#c22e2e"))
                }
                if ("foil-star" in TownInfo.houseDecor) {
                    // decor: foil-star — a star folded out of
                    // foil, pinned under the poster. For reasons.
                    star(c, 0.222f * w, 0.435f * h, 0.020f * w, fp("#cfd6dd"))
                    star(c, 0.222f * w, 0.435f * h, 0.020f * w, sp("#8a94a0", 1.5f))
                    cir(0.222f, 0.408f, 0.005f, fp("#c22e2e"))
                }
                if ("sealed-envelope" in TownInfo.houseDecor) {
                    // decor: sealed-envelope — cream, unopened,
                    // pinned to the board at a careful angle
                    rr(0.445f, 0.402f, 0.505f, 0.442f, fp("#f7f1de"))
                    rr(0.445f, 0.402f, 0.505f, 0.442f, sp("#b8a888", 2f))
                    ln(0.445f, 0.402f, 0.475f, 0.424f, sp("#b8a888", 1.5f))
                    ln(0.505f, 0.402f, 0.475f, 0.424f, sp("#b8a888", 1.5f))
                    cir(0.475f, 0.428f, 0.007f, fp("#c22e2e"))
                }
                // ---- v0.4.0d decor: the holiday shelf of honor ----
                if ("holiday-garland" in TownInfo.houseDecor) {
                    // decor: holiday-garland — a paper garland
                    // swagged along the top of the wall, one
                    // loop per holiday you've actually played
                    ln(0.06f, 0.055f, 0.30f, 0.115f, sp("#2a6e3a", 3f))
                    ln(0.30f, 0.115f, 0.54f, 0.055f, sp("#2a6e3a", 3f))
                    ln(0.54f, 0.055f, 0.78f, 0.115f, sp("#2a6e3a", 3f))
                    ln(0.78f, 0.115f, 0.94f, 0.06f, sp("#2a6e3a", 3f))
                    val garlandDots = listOf(
                        Triple(0.12f, 0.077f, "#c22e2e"),
                        Triple(0.20f, 0.098f, "#e8a33a"),
                        Triple(0.38f, 0.098f, "#e87a8a"),
                        Triple(0.46f, 0.077f, "#2a9a4a"),
                        Triple(0.62f, 0.085f, "#7ab8e8"),
                        Triple(0.70f, 0.102f, "#c9a227"),
                        Triple(0.84f, 0.095f, "#8a5aa8"),
                        Triple(0.90f, 0.075f, "#c22e2e"))
                    for ((gx, gy, gc) in garlandDots) {
                        cir(gx, gy, 0.008f, fp(gc))
                    }
                }
                if ("full-calendar" in TownInfo.houseDecor) {
                    // decor: full-calendar — a wall calendar by
                    // the door, all eight holidays ringed in red
                    rr(0.60f, 0.40f, 0.70f, 0.52f, fp("#f7f1de"))
                    rr(0.60f, 0.40f, 0.70f, 0.52f, sp("#8a6f45", 2.5f))
                    rr(0.60f, 0.40f, 0.70f, 0.432f, fp("#c22e2e"))
                    txt(c, "YEAR", 0.65f * w, 0.424f * h, 0.014f * h, "#f7f1de")
                    for (row in 0..1) for (col in 0..3) {
                        val cx = 0.618f + col * 0.021f
                        val cy = 0.452f + row * 0.034f
                        cir(cx, cy, 0.0085f, sp("#c22e2e", 1.8f))
                        cir(cx, cy, 0.0032f, fp("#5a4632"))
                    }
                }
                if ("sleeping-bags" in TownInfo.houseDecor) {
                    // v0.11.0c dressing: the sleepover's bags —
                    // temporary, like the night itself: three
                    // sleeping bags unrolled across the living
                    // room floor while a sleepover is planned
                    // tonight, struck in the morning. The third
                    // bag's pillow sits at a brave angle.
                    rr(0.16f, 0.845f, 0.40f, 0.905f, fp("#3f6fb0"))
                    rr(0.16f, 0.845f, 0.40f, 0.905f, sp("#2c4f7e", 2.5f))
                    rr(0.175f, 0.852f, 0.235f, 0.878f, fp("#f4ecd8"))
                    rr(0.44f, 0.86f, 0.68f, 0.92f, fp("#b03a3a"))
                    rr(0.44f, 0.86f, 0.68f, 0.92f, sp("#7e2c2c", 2.5f))
                    rr(0.455f, 0.867f, 0.515f, 0.893f, fp("#f4ecd8"))
                    rr(0.70f, 0.845f, 0.90f, 0.90f, fp("#3f8a4f"))
                    rr(0.70f, 0.845f, 0.90f, 0.90f, sp("#2c6338", 2.5f))
                    rr(0.715f, 0.838f, 0.775f, 0.866f, fp("#f4ecd8"))
                    rr(0.715f, 0.838f, 0.775f, 0.866f, sp("#b8a888", 1.5f))
                }
                // ---- v0.5.0h decor: the season finale shelf —
                // one piece per season played, hung as earned ----
                if ("garden-ribbon" in TownInfo.houseDecor) {
                    // decor: garden-ribbon — a rosette ribbon on
                    // a nail, spring-green, faintly smug
                    ln(0.115f, 0.40f, 0.115f, 0.425f, sp("#8a6f45", 2f))
                    cir(0.115f, 0.452f, 0.020f, fp("#2a9a4a"))
                    cir(0.115f, 0.452f, 0.020f, sp("#1e6e34", 2f))
                    cir(0.115f, 0.452f, 0.009f, fp("#c9a227"))
                    ln(0.108f, 0.468f, 0.100f, 0.50f, sp("#2a9a4a", 3f))
                    ln(0.122f, 0.468f, 0.130f, 0.50f, sp("#2a9a4a", 3f))
                }
                if ("regatta-pennant" in TownInfo.houseDecor) {
                    // decor: regatta-pennant — a little triangular
                    // pennant, pond-blue with a white stripe,
                    // pinned along the top-left of the wall
                    ln(0.05f, 0.17f, 0.21f, 0.17f, sp("#5a4632", 2f))
                    for (k in 0..2) {
                        val px = 0.065f + k * 0.048f
                        ln(px, 0.17f, px + 0.036f, 0.185f, sp("#2a6fd4", 2f))
                        ln(px + 0.036f, 0.185f, px, 0.20f, sp("#2a6fd4", 2f))
                        ln(px, 0.185f, px + 0.028f, 0.185f, sp("#eef2f6", 1.5f))
                    }
                    cir(0.05f, 0.17f, 0.005f, fp("#c22e2e"))
                    cir(0.21f, 0.17f, 0.005f, fp("#c22e2e"))
                }
                if ("harvest-plaque" in TownInfo.houseDecor) {
                    // decor: harvest-plaque — a wooden farm-gate
                    // board in miniature, two names burned in
                    rr(0.755f, 0.40f, 0.865f, 0.472f, fp("#a8793f"))
                    rr(0.755f, 0.40f, 0.865f, 0.472f, sp("#5a4632", 2.5f))
                    ln(0.762f, 0.424f, 0.858f, 0.424f, sp("#5a4632", 1.5f, 170))
                    ln(0.762f, 0.448f, 0.842f, 0.448f, sp("#5a4632", 1.5f, 170))
                    cir(0.762f, 0.408f, 0.004f, fp("#3a2c1a"))
                    cir(0.858f, 0.408f, 0.004f, fp("#3a2c1a"))
                    txt(c, "TIE", 0.81f * w, 0.418f * h, 0.013f * h, "#3a2c1a")
                }
                if ("lantern-glow" in TownInfo.houseDecor) {
                    // decor: lantern-glow — a paper lantern
                    // hanging in the top-right corner, warm
                    // even unlit; the retired one, with honors
                    ln(0.925f, 0.045f, 0.925f, 0.10f, sp("#5a4632", 2f))
                    rr(0.905f, 0.10f, 0.945f, 0.112f, fp("#8a6f45"))
                    ov(0.898f, 0.112f, 0.952f, 0.185f, fp("#e8a33a"))
                    ov(0.898f, 0.112f, 0.952f, 0.185f, sp("#a8722a", 2f))
                    ln(0.907f, 0.122f, 0.907f, 0.175f, sp("#a8722a", 1.5f, 150))
                    ln(0.925f, 0.115f, 0.925f, 0.182f, sp("#a8722a", 1.5f, 150))
                    ln(0.943f, 0.122f, 0.943f, 0.175f, sp("#a8722a", 1.5f, 150))
                    cir(0.925f, 0.148f, 0.007f, fp("#f7d774"))
                    rr(0.905f, 0.185f, 0.945f, 0.196f, fp("#8a6f45"))
                }
                // ---- v0.7.0b decor: the secrets shelf ----
                if ("secret-map" in TownInfo.houseDecor) {
                    // decor: secret-map — the whole town on one
                    // hand-drawn page, pencil roads, X's where
                    // the quiet things were, pinned low by the
                    // cork board once all twelve are found
                    rr(0.235f, 0.56f, 0.375f, 0.70f, fp("#f0e4c3"))
                    rr(0.235f, 0.56f, 0.375f, 0.70f, sp("#8a6f45", 2.5f))
                    ln(0.248f, 0.665f, 0.285f, 0.628f, sp("#5a4632", 1.5f, 170))
                    ln(0.285f, 0.628f, 0.315f, 0.652f, sp("#5a4632", 1.5f, 170))
                    ln(0.315f, 0.652f, 0.352f, 0.595f, sp("#5a4632", 1.5f, 170))
                    ln(0.262f, 0.598f, 0.272f, 0.608f, sp("#c22e2e", 2f))
                    ln(0.272f, 0.598f, 0.262f, 0.608f, sp("#c22e2e", 2f))
                    ln(0.330f, 0.672f, 0.340f, 0.682f, sp("#c22e2e", 2f))
                    ln(0.340f, 0.672f, 0.330f, 0.682f, sp("#c22e2e", 2f))
                    star(c, 0.352f * w, 0.592f * h, 0.009f * w, fp("#c9a227"))
                    cir(0.248f, 0.552f, 0.006f, fp("#c22e2e"))
                    cir(0.362f, 0.552f, 0.006f, fp("#2a6fd4"))
                }
                // ---- v0.7.0d decor: the spelling bee trophy ----
                if ("bee-trophy" in TownInfo.houseDecor) {
                    // decor: bee-trophy — a small gold cup on a
                    // wall shelf right of the map, one star on
                    // the bowl, earned on a Friday
                    rr(0.785f, 0.688f, 0.885f, 0.698f, fp("#8a6f45"))
                    ov(0.792f, 0.606f, 0.812f, 0.640f, sp("#c9a227", 2.5f))
                    ov(0.858f, 0.606f, 0.878f, 0.640f, sp("#c9a227", 2.5f))
                    rr(0.806f, 0.598f, 0.864f, 0.652f, fp("#e3b93c"))
                    rr(0.806f, 0.598f, 0.864f, 0.652f, sp("#a8722a", 2f))
                    star(c, 0.835f * w, 0.622f * h, 0.010f * w, fp("#a8722a"))
                    rr(0.827f, 0.652f, 0.843f, 0.668f, fp("#c9a227"))
                    rr(0.812f, 0.668f, 0.858f, 0.680f, fp("#e3b93c"))
                    rr(0.812f, 0.668f, 0.858f, 0.680f, sp("#a8722a", 2f))
                }
                // ---- v0.8.0d decor: the yearbook shelf ----
                if ("yearbook-shelf" in TownInfo.houseDecor) {
                    // decor: yearbook-shelf — a low shelf under
                    // the bee trophy's, holding the printed
                    // volumes spine-out: school colors, one
                    // leaning (the pamphlet year), room on the
                    // right for the books still coming
                    rr(0.775f, 0.752f, 0.898f, 0.763f, fp("#8a6f45"))
                    rr(0.782f, 0.706f, 0.800f, 0.752f, fp("#a52a2a"))
                    rr(0.782f, 0.716f, 0.800f, 0.722f, fp("#e8dcc0"))
                    rr(0.802f, 0.700f, 0.822f, 0.752f, fp("#2a6f6f"))
                    rr(0.802f, 0.712f, 0.822f, 0.718f, fp("#e8dcc0"))
                    rr(0.824f, 0.710f, 0.840f, 0.752f, fp("#c9a227"))
                    rr(0.824f, 0.720f, 0.840f, 0.726f, fp("#7a4a12"))
                    rr(0.845f, 0.716f, 0.859f, 0.752f, fp("#4a5a8a"))
                    ln(0.866f, 0.752f, 0.878f, 0.714f, sp("#63507a", 5f))
                }
                // ---- v0.9.0c decor: the ribbon display ----
                if ("fair-ribbons" in TownInfo.houseDecor) {
                    // decor: fair-ribbons — a small plaque left
                    // of the yearbook shelf: the first blue
                    // ribbon as a rosette with two tails, the
                    // red and white waiting their years beside
                    // it as small plain pins
                    rr(0.618f, 0.694f, 0.762f, 0.764f, fp("#5d4630"))
                    rr(0.624f, 0.700f, 0.756f, 0.758f, fp("#7a5c3d"))
                    cir(0.662f, 0.722f, 0.016f, fp("#2a6fd4"))
                    cir(0.662f, 0.722f, 0.008f, fp("#e8dcc0"))
                    ln(0.654f, 0.736f, 0.648f, 0.756f, sp("#c23b3b", 4f))
                    ln(0.670f, 0.736f, 0.676f, 0.756f, sp("#f2ead8", 4f))
                    cir(0.706f, 0.722f, 0.007f, fp("#c23b3b"))
                    cir(0.730f, 0.722f, 0.007f, fp("#f2ead8"))
                }
                if ("hero-mask" in TownInfo.houseDecor) {
                    // decor: hero-mask — your retired hero mask
                    // on a hook by the door, right of the ribbon
                    // plaque: a colored band, two eye holes, the
                    // elastic still looped over the peg
                    cir(0.795f, 0.706f, 0.006f, fp("#6b4a2f"))
                    rr(0.772f, 0.712f, 0.822f, 0.742f, fp("#2a4a8a"), 0.012f)
                    rr(0.772f, 0.712f, 0.822f, 0.742f, sp("#1a1a1a", 2f), 0.012f)
                    ov(0.780f, 0.721f, 0.792f, 0.733f, fp("#1a1a1a"))
                    ov(0.802f, 0.721f, 0.814f, 0.733f, fp("#1a1a1a"))
                    ln(0.822f, 0.718f, 0.834f, 0.752f, sp("#3a3a3a", 2f))
                }
            }
        }
    }

    private fun drawInteriorFloorDetails(c: Canvas, w: Float, h: Float,
                                         def: InteriorDef, night: Boolean) {
        val accent = LOC_PALETTES[def.locId]?.accentHex ?: Paper.RED
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(hex, alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) = Paper.strokePaint(hex, sw, alpha)
        fun rr(l: Float, t: Float, r: Float, b: Float, p: Paint, rad: Float = 0f) =
            if (rad > 0f) c.drawRoundRect(RectF(l * w, t * h, r * w, b * h), rad, rad, p)
            else c.drawRect(RectF(l * w, t * h, r * w, b * h), p)
        fun ov(l: Float, t: Float, r: Float, b: Float, p: Paint) =
            c.drawOval(RectF(l * w, t * h, r * w, b * h), p)
        fun cir(x: Float, y: Float, r: Float, p: Paint) = c.drawCircle(x * w, y * h, r * w, p)
        fun ln(x1: Float, y1: Float, x2: Float, y2: Float, p: Paint) =
            c.drawLine(x1 * w, y1 * h, x2 * w, y2 * h, p)

        when (def.locId) {
            "KennysHouse" -> {
                // idetail: cardboard-floor — cardboard over
                // the worst of the floorboards
                rr(0.27f, 0.845f, 0.44f, 0.885f, fp("#b59a6b"))
                rr(0.27f, 0.845f, 0.44f, 0.885f, sp("#8a6f45", 2f))
                rr(0.52f, 0.86f, 0.66f, 0.895f, fp("#a8895a"))
                rr(0.70f, 0.85f, 0.86f, 0.885f, fp("#bfa06e"))
                ln(0.30f, 0.865f, 0.41f, 0.865f, sp("#8a6f45", 1.5f, 150))
            }
            "ButtersHouse" -> {
                // idetail: corner-chair — THE corner, facing
                // the wall, you know the one
                rr(0.905f, 0.72f, 0.955f, 0.83f, fp("#7a9ab5"))
                rr(0.90f, 0.83f, 0.96f, 0.85f, fp("#6589a3"))
                rr(0.905f, 0.85f, 0.915f, 0.90f, fp("#4a3423"))
                rr(0.945f, 0.85f, 0.955f, 0.90f, fp("#4a3423"))
            }
            "CartmanHouse" -> {
                // idetail: snack-bags — a small drift of
                // crumpled snack bags by the couch
                for (fx in listOf(0.30f, 0.345f, 0.39f)) {
                    rr(fx - 0.016f, 0.815f, fx + 0.016f, 0.885f, fp("#e8862a"), 4f)
                    ln(fx - 0.010f, 0.815f, fx - 0.010f, 0.83f, sp("#c96a1a", 2f))
                    ln(fx + 0.002f, 0.815f, fx + 0.002f, 0.83f, sp("#c96a1a", 2f))
                    rr(fx - 0.016f, 0.845f, fx + 0.016f, 0.858f, fp(accent))
                }
                // idetail: litter-box — the cat's corner;
                // the ears say she's home
                c.drawPath(Path().apply {
                    moveTo(0.912f * w, 0.848f * h)
                    lineTo(0.918f * w, 0.824f * h)
                    lineTo(0.926f * w, 0.848f * h)
                    close()
                }, fp("#4a4a4a"))
                c.drawPath(Path().apply {
                    moveTo(0.946f * w, 0.848f * h)
                    lineTo(0.952f * w, 0.824f * h)
                    lineTo(0.960f * w, 0.848f * h)
                    close()
                }, fp("#4a4a4a"))
                rr(0.895f, 0.845f, 0.98f, 0.90f, fp("#8a8a8a"))
                rr(0.895f, 0.845f, 0.98f, 0.90f, sp("#5a5a5a", 2.5f))
                ov(0.898f, 0.84f, 0.977f, 0.86f, fp("#6a6258"))
            }
            "StanHouse" -> {
                // idetail: dog-bed — a good boy's bed, plus
                // his bowl, plus optimism
                ov(0.855f, 0.845f, 0.985f, 0.90f, fp("#7a3f4e"))
                ov(0.875f, 0.858f, 0.965f, 0.892f, fp("#96606e"))
                ov(0.812f, 0.872f, 0.852f, 0.90f, fp("#2a6fd4"))
                ov(0.818f, 0.878f, 0.846f, 0.892f, fp("#7ab8e8"))
            }
            "KyleHouse" -> {
                // idetail: toy-blocks — a little brother's
                // architecture, mid-collapse
                val blocks = listOf(
                    Triple(0.29f, 0.86f, "#d43d2a"), Triple(0.322f, 0.86f, "#2a6fd4"),
                    Triple(0.306f, 0.832f, "#f2c230"), Triple(0.42f, 0.864f, "#2a9a4a"))
                for ((i, b) in blocks.withIndex()) {
                    rr(b.first, b.second, b.first + 0.026f, b.second + 0.027f, fp(b.third), 2f)
                    txt(c, listOf("A", "B", "C", "D")[i], (b.first + 0.013f) * w,
                        (b.second + 0.019f) * h, 0.013f * h, "#ffffff")
                }
            }
            "TolkienHouse" -> {
                // idetail: piano — a grand, because the
                // upright was just renting
                c.drawPath(Path().apply {
                    moveTo(0.56f * w, 0.86f * h)
                    lineTo(0.56f * w, 0.76f * h)
                    lineTo(0.74f * w, 0.76f * h)
                    quadTo(0.82f * w, 0.76f * h, 0.80f * w, 0.83f * h)
                    quadTo(0.79f * w, 0.87f * h, 0.72f * w, 0.865f * h)
                    lineTo(0.56f * w, 0.86f * h)
                    close()
                }, fp("#1e1e24"))
                ln(0.56f, 0.76f, 0.70f, 0.665f, sp("#1e1e24", 5f))
                ln(0.70f, 0.665f, 0.70f, 0.76f, sp("#1e1e24", 2.5f))
                rr(0.562f, 0.815f, 0.635f, 0.835f, fp("#f4f4f4"))
                for (i in 0..4) {
                    val kx = 0.575f + i * 0.012f
                    ln(kx, 0.815f, kx, 0.827f, sp("#1e1e24", 2f))
                }
                rr(0.575f, 0.86f, 0.59f, 0.905f, fp("#1e1e24"))
                rr(0.755f, 0.855f, 0.77f, 0.90f, fp("#1e1e24"))
            }
            "BusStop" -> {
                // idetail: bus-aisle — the long walk of shame
                // to the back seats
                rr(0.435f, 0.645f, 0.565f, 0.965f, fp("#3a3a40"))
                ln(0.435f, 0.645f, 0.435f, 0.965f, sp("#e8c53a", 2f, 150))
                ln(0.565f, 0.645f, 0.565f, 0.965f, sp("#e8c53a", 2f, 150))
                ln(0.435f, 0.80f, 0.565f, 0.80f, sp("#6a6a72", 2f))
                ln(0.435f, 0.90f, 0.565f, 0.90f, sp("#6a6a72", 2f))
            }
            "MainStreet" -> {
                // idetail: jukebox — it has both kinds of
                // music: country AND western
                ov(0.885f, 0.665f, 0.99f, 0.78f, fp(accent))
                rr(0.885f, 0.72f, 0.99f, 0.895f, fp(accent))
                ov(0.905f, 0.69f, 0.97f, 0.775f, fp("#f2c230", 220))
                rr(0.905f, 0.79f, 0.97f, 0.86f, fp("#7a241c"))
                for (gx in listOf(0.922f, 0.9375f, 0.953f))
                    ln(gx, 0.795f, gx, 0.855f, sp("#e8b7a0", 2f))
                rr(0.885f, 0.885f, 0.99f, 0.905f, fp("#7a241c"))
            }
            "TweekBros" -> {
                // idetail: coffee-sacks — the supply chain,
                // such as it is
                for (sx in listOf(0.885f, 0.932f)) {
                    rr(sx, 0.80f, sx + 0.055f, 0.895f, fp("#a8895a"), 5f)
                    rr(sx, 0.80f, sx + 0.055f, 0.822f, fp("#8a6f45"))
                    txt(c, "COFFEE", (sx + 0.0275f) * w, 0.858f * h,
                        0.010f * h, "#4a3423")
                }
            }
            "CityWok" -> {
                // idetail: wok-station — one wok, very hot,
                // very city
                rr(0.905f, 0.82f, 0.955f, 0.895f, fp("#3a3a3a"))
                for (fx in listOf(0.918f, 0.93f, 0.942f)) {
                    c.drawPath(Path().apply {
                        moveTo((fx - 0.005f) * w, 0.838f * h)
                        lineTo(fx * w, 0.815f * h)
                        lineTo((fx + 0.005f) * w, 0.838f * h)
                        close()
                    }, fp("#f2a230", 200))
                }
                c.drawPath(Path().apply {
                    moveTo(0.885f * w, 0.80f * h)
                    quadTo(0.93f * w, 0.862f * h, 0.975f * w, 0.80f * h)
                    close()
                }, fp("#26262c"))
                ln(0.885f, 0.80f, 0.975f, 0.80f, sp("#4a4a52", 3f))
                c.drawArc(RectF(0.905f * w, 0.715f * h, 0.935f * w, 0.775f * h),
                    300f, 110f, false, sp("#e8e8e8", 2.5f, 150))
                c.drawArc(RectF(0.93f * w, 0.695f * h, 0.96f * w, 0.755f * h),
                    300f, 110f, false, sp("#e8e8e8", 2.5f, 130))
            }
            "SkeetersBar" -> {
                // idetail: pool-table — the felt has seen
                // things, mostly Randy
                rr(0.29f, 0.795f, 0.53f, 0.825f, fp("#2a7a4a"))
                rr(0.29f, 0.795f, 0.53f, 0.825f, sp("#4a3423", 4f))
                cir(0.297f, 0.81f, 0.008f, fp("#12241a"))
                cir(0.523f, 0.81f, 0.008f, fp("#12241a"))
                cir(0.40f, 0.808f, 0.005f, fp("#f0f0f0"))
                cir(0.44f, 0.813f, 0.005f, fp("#d43d2a"))
                rr(0.295f, 0.825f, 0.525f, 0.865f, fp("#5a3a22"))
                rr(0.305f, 0.865f, 0.325f, 0.90f, fp("#4a3423"))
                rr(0.495f, 0.865f, 0.515f, 0.90f, fp("#4a3423"))
            }
            "Bijou" -> {
                // idetail: ticket-booth — one please, and
                // make it a matinee
                rr(0.895f, 0.665f, 0.97f, 0.70f, fp("#f2c230"))
                txt(c, "TICKETS", 0.9325f * w, 0.691f * h, 0.012f * h, "#3a2a1a")
                rr(0.885f, 0.70f, 0.98f, 0.895f, fp("#4a3a6a"))
                rr(0.895f, 0.715f, 0.97f, 0.80f, fp("#cfe4ee", 170))
                rr(0.885f, 0.80f, 0.98f, 0.825f, fp("#33254d"))
            }
            "TomsRhino" -> {
                // idetail: waiting-chairs — sit, flip a
                // magazine from 2019, wait
                for (fx in listOf(0.31f, 0.375f, 0.44f)) {
                    rr(fx - 0.024f, 0.755f, fx + 0.024f, 0.825f, fp("#5a8a94"), 3f)
                    rr(fx - 0.026f, 0.825f, fx + 0.026f, 0.845f, fp("#6fa0aa"))
                    ln(fx - 0.02f, 0.845f, fx - 0.02f, 0.875f, sp("#3a5a62", 2.5f))
                    ln(fx + 0.02f, 0.845f, fx + 0.02f, 0.875f, sp("#3a5a62", 2.5f))
                }
            }
            "Bank" -> {
                // idetail: carpet-runner — the long red walk
                // to financial disappointment
                rr(0.30f, 0.922f, 0.70f, 0.962f, fp("#8a2432", 235))
                rr(0.31f, 0.928f, 0.69f, 0.956f, sp("#c9a227", 2f))
            }
            "PostOffice" -> {
                // idetail: parcel-scale — it has never once
                // said the weight you hoped
                ov(0.895f, 0.735f, 0.975f, 0.765f, fp("#8a8a8a"))
                rr(0.915f, 0.705f, 0.955f, 0.74f, fp("#b08954"))
                rr(0.928f, 0.76f, 0.942f, 0.885f, fp("#6a6a6a"))
                cir(0.935f, 0.815f, 0.016f, fp("#f0ead8"))
                cir(0.935f, 0.815f, 0.016f, sp("#4a4a4a", 2f))
                ln(0.935f, 0.815f, 0.945f, 0.805f, sp("#c0392b", 2f))
                rr(0.895f, 0.885f, 0.975f, 0.90f, fp("#5a5a5a"))
                // idetail: mail-sacks — canvas sacks, full of
                // other people's magazines
                for (sx in listOf(0.28f, 0.325f)) {
                    rr(sx, 0.815f, sx + 0.052f, 0.895f, fp("#cbbfa4"), 5f)
                    rr(sx, 0.815f, sx + 0.052f, 0.832f, fp("#a89572"))
                    txt(c, "MAIL", (sx + 0.026f) * w, 0.862f * h,
                        0.011f * h, "#5a4a3a")
                }
            }
            "FreemansTacos" -> {
                // idetail: mic-booth — he narrates from in
                // here; yes, really
                rr(0.90f, 0.645f, 0.97f, 0.675f, fp("#d43d2a"))
                txt(c, "ON AIR", 0.935f * w, 0.667f * h, 0.011f * h, "#ffffff")
                rr(0.885f, 0.68f, 0.985f, 0.895f, fp("#5a4030"))
                rr(0.895f, 0.70f, 0.975f, 0.82f, fp("#d8e8ee", 130))
                ln(0.935f, 0.79f, 0.935f, 0.895f, sp("#2a2a2a", 3f))
                ov(0.928f, 0.755f, 0.942f, 0.795f, fp("#2a2a2a"))
            }
            "Raisins" -> {
                // idetail: wings-sign — famous, it says so
                // right on the sign
                c.drawPath(Path().apply {
                    moveTo(0.90f * w, 0.90f * h)
                    lineTo(0.915f * w, 0.775f * h)
                    lineTo(0.945f * w, 0.775f * h)
                    lineTo(0.96f * w, 0.90f * h)
                    close()
                }, fp("#f7ead2"))
                c.drawPath(Path().apply {
                    moveTo(0.90f * w, 0.90f * h)
                    lineTo(0.915f * w, 0.775f * h)
                    lineTo(0.945f * w, 0.775f * h)
                    lineTo(0.96f * w, 0.90f * h)
                    close()
                }, sp("#e8932a", 3f))
                txt(c, "FAMOUS", 0.93f * w, 0.818f * h, 0.012f * h, "#c0392b")
                txt(c, "WINGS", 0.93f * w, 0.848f * h, 0.012f * h, "#c0392b")
            }
            "Church" -> {
                // idetail: collection-stand — the plate,
                // passed with great ceremony
                ov(0.905f, 0.755f, 0.955f, 0.785f, fp("#c8a028"))
                ov(0.913f, 0.762f, 0.947f, 0.778f, fp("#a8821f"))
                cir(0.93f, 0.769f, 0.004f, fp("#f2e2a0"))
                rr(0.926f, 0.78f, 0.934f, 0.875f, fp("#6b4a2f"))
                rr(0.915f, 0.875f, 0.945f, 0.888f, fp("#6b4a2f"))
            }
            "CityHall" -> {
                // idetail: seal-rug — the town seal, walked
                // on by everyone, daily
                ov(0.415f, 0.885f, 0.585f, 0.925f, fp("#7a1f1f", 200))
                ov(0.425f, 0.889f, 0.575f, 0.921f, sp("#c9a227", 2.5f))
                star(c, 0.50f * w, 0.905f * h, 0.011f * w, fp("#c9a227"))
            }
            "Elementary" -> {
                // idetail: globe — the whole world, and
                // somehow still mostly this town
                rr(0.932f, 0.83f, 0.938f, 0.885f, fp("#4a3423"))
                ov(0.905f, 0.885f, 0.965f, 0.90f, fp("#6b4a2f"))
                ln(0.905f, 0.715f, 0.965f, 0.835f, sp("#4a3423", 2.5f))
                cir(0.935f, 0.775f, 0.037f, fp("#5a8ac8"))
                ov(0.908f, 0.752f, 0.932f, 0.775f, fp("#5aa85a"))
                ov(0.935f, 0.775f, 0.958f, 0.80f, fp("#5aa85a"))
                ov(0.92f, 0.79f, 0.938f, 0.805f, fp("#5aa85a"))
            }
            "KidsPark" -> {
                // idetail: lost-found-box — mittens check in,
                // they rarely check out
                rr(0.278f, 0.815f, 0.335f, 0.835f, fp("#b8894e"))
                rr(0.345f, 0.815f, 0.402f, 0.835f, fp("#b8894e"))
                rr(0.325f, 0.772f, 0.355f, 0.835f, fp("#d43d2a"), 5f)
                cir(0.354f, 0.815f, 0.008f, fp("#d43d2a"))
                rr(0.28f, 0.83f, 0.40f, 0.895f, fp("#a8763e"))
                rr(0.28f, 0.83f, 0.40f, 0.895f, sp("#7a5228", 2.5f))
                txt(c, "LOST & FOUND", 0.34f * w, 0.872f * h, 0.0105f * h, "#f7ead2")
            }
            "StarksPond" -> {
                // idetail: woodpile — stove wood, stacked
                // against the long dark
                for ((lx, ly) in listOf(0.895f to 0.875f, 0.928f to 0.875f,
                    0.961f to 0.875f, 0.9115f to 0.848f, 0.9445f to 0.848f,
                    0.928f to 0.821f)) {
                    cir(lx, ly, 0.016f, fp("#8a6a44"))
                    cir(lx, ly, 0.016f, sp("#5a4028", 2f))
                    cir(lx, ly, 0.008f, sp("#6a5136", 1.5f))
                }
                // idetail: tipup-flags — two lines down,
                // waiting on the big one
                for (fx in listOf(0.30f, 0.365f)) {
                    rr(fx - 0.02f, 0.855f, fx + 0.02f, 0.865f, fp("#4a3423"))
                    ln(fx, 0.79f, fx, 0.86f, sp("#4a3423", 2.5f))
                    c.drawPath(Path().apply {
                        moveTo(fx * w, 0.79f * h)
                        lineTo((fx + 0.016f) * w, 0.80f * h)
                        lineTo(fx * w, 0.815f * h)
                        close()
                    }, fp("#f26d21"))
                }
            }
            "Mall" -> {
                // idetail: kiosk — sunglasses and phone
                // cases, the mall's last economy
                rr(0.45f, 0.70f, 0.458f, 0.80f, fp("#6b4a2f"))
                rr(0.542f, 0.70f, 0.55f, 0.80f, fp("#6b4a2f"))
                c.drawPath(Path().apply {
                    moveTo(0.435f * w, 0.70f * h)
                    lineTo(0.565f * w, 0.70f * h)
                    lineTo(0.545f * w, 0.665f * h)
                    lineTo(0.455f * w, 0.665f * h)
                    close()
                }, fp("#d4688a"))
                cir(0.50f, 0.715f, 0.010f, fp("#f2c230"))
                rr(0.445f, 0.80f, 0.555f, 0.875f, fp("#8a6a4a"))
                rr(0.455f, 0.815f, 0.545f, 0.86f, fp(accent))
            }
            "SoDoSoPa" -> {
                // idetail: vinyl-crates — records, obviously,
                // played ironically, obviously
                rr(0.855f, 0.815f, 0.925f, 0.895f, fp("#5a4632"))
                rr(0.855f, 0.815f, 0.925f, 0.895f, sp("#3a2c1e", 2.5f))
                rr(0.862f, 0.775f, 0.885f, 0.83f, fp("#d4688a"))
                rr(0.885f, 0.77f, 0.908f, 0.83f, fp("#7a9ac8"))
                cir(0.8965f, 0.80f, 0.005f, fp("#f0ead8"))
                rr(0.915f, 0.815f, 0.985f, 0.895f, fp("#5a4632"))
                rr(0.915f, 0.815f, 0.985f, 0.895f, sp("#3a2c1e", 2.5f))
                rr(0.93f, 0.778f, 0.953f, 0.83f, fp("#e8c53a"))
            }
            "TegridyFarms" -> {
                // idetail: feed-sacks — tegridy, bagged
                for (sx in listOf(0.87f, 0.925f)) {
                    rr(sx, 0.805f, sx + 0.058f, 0.895f, fp("#b3a06b"), 5f)
                    rr(sx, 0.805f, sx + 0.058f, 0.827f, fp("#97834f"))
                    rr(sx, 0.845f, sx + 0.058f, 0.858f, fp("#4a7a2a"))
                }
                ov(0.892f, 0.862f, 0.91f, 0.885f, fp("#4a9a3a"))
            }
            "YourHouse" -> {
                // idetail: moving-boxes — the new kid moved in
                // recently and it shows: two boxes, one still open
                rr(0.585f, 0.815f, 0.645f, 0.90f, fp("#b59a6b"))
                rr(0.585f, 0.815f, 0.645f, 0.90f, sp("#8a6f45", 2f))
                txt(c, "STUFF", 0.615f * w, 0.868f * h, 0.014f * h, "#5a4632")
                rr(0.65f, 0.795f, 0.705f, 0.90f, fp("#a8895a"))
                rr(0.65f, 0.795f, 0.705f, 0.90f, sp("#8a6f45", 2f))
                ln(0.65f, 0.795f, 0.628f, 0.772f, sp("#8a6f45", 3f))
                ln(0.705f, 0.795f, 0.727f, 0.772f, sp("#8a6f45", 3f))
                cir(0.677f, 0.782f, 0.016f, fp("#7a3f4e"))
                // ---- v0.3.0d decor: earned pieces (House.DECOR) ----
                if ("game-trophy" in TownInfo.houseDecor) {
                    // decor: game-trophy — a gold cup, floor
                    // place of honor beside the boxes
                    cir(0.730f, 0.852f, 0.016f, fp("#c9a227"))
                    rr(0.722f, 0.845f, 0.738f, 0.862f, fp("#a8821d"))
                    ov(0.706f, 0.842f, 0.722f, 0.862f, sp("#c9a227", 2.5f))
                    ov(0.738f, 0.842f, 0.754f, 0.862f, sp("#c9a227", 2.5f))
                    rr(0.726f, 0.862f, 0.734f, 0.878f, fp("#c9a227"))
                    rr(0.716f, 0.878f, 0.744f, 0.888f, fp("#8a6f45"))
                }
                if ("tiny-gavel" in TownInfo.houseDecor) {
                    // decor: tiny-gavel — the town has spoken;
                    // the gavel rests by the wall now
                    ln(0.898f, 0.872f, 0.928f, 0.888f, sp("#6b4a2f", 4f))
                    rr(0.918f, 0.862f, 0.938f, 0.878f, fp("#8a6a3a"), 3f)
                    rr(0.892f, 0.888f, 0.93f, 0.896f, fp("#4a3423"), 2f)
                }
            }
        }
    }

    // ---------------- props ----------------
    private fun drawProp(c: Canvas, prop: String, cx: Float, baseY: Float,
                         w: Float, h: Float, night: Boolean) {
        val pw = w * 0.13f
        val ph = h * 0.115f
        val brown = Paper.fillPaint("#6b4a2f")
        val darkBrown = Paper.fillPaint("#4a3423")
        val cream = Paper.fillPaint("#f0e8d0")
        fun r(l: Float, t: Float, rgt: Float, b: Float, p: Paint) =
            c.drawRect(cx + l * pw, baseY - t * ph, cx + rgt * pw, baseY - b * ph, p)
        fun cy(f: Float) = baseY - f * ph
        when (prop) {
            "sofa" -> {
                r(-0.55f, 0.88f, 0.55f, 0.42f, Paper.fillPaint("#7a3f4e"))
                r(-0.55f, 0.55f, 0.55f, 0f, Paper.fillPaint("#8a4a5a"))
                r(-0.58f, 0.72f, -0.40f, 0.18f, Paper.fillPaint("#7a3f4e"))
                r(0.40f, 0.72f, 0.58f, 0.18f, Paper.fillPaint("#7a3f4e"))
            }
            "tv" -> {
                r(-0.08f, 0.15f, 0.08f, 0f, darkBrown)
                r(-0.42f, 0.92f, 0.42f, 0.15f, Paper.fillPaint("#2a2a3a"))
                r(-0.36f, 0.84f, 0.36f, 0.23f, Paper.fillPaint(if (night) "#5a7a9a" else "#3a4a5a"))
            }
            "table", "picnic" -> {
                r(-0.5f, 0.55f, 0.5f, 0.45f, brown)
                r(-0.42f, 0.45f, -0.34f, 0f, darkBrown)
                r(0.34f, 0.45f, 0.42f, 0f, darkBrown)
            }
            "desk" -> {
                r(-0.4f, 0.5f, 0.4f, 0f, Paper.fillPaint("#77572f"))
                r(-0.43f, 0.57f, 0.43f, 0.5f, Paper.fillPaint("#8a6a42"))
                // a book on the desk (v0.17.0b: a muted cover,
                // a page block and a spine — it used to be one
                // bare bright-red block)
                r(-0.05f, 0.70f, 0.30f, 0.57f, Paper.fillPaint("#8a3a34"))
                r(-0.03f, 0.685f, 0.28f, 0.585f, Paper.fillPaint("#e8e2d2"))
                r(-0.05f, 0.70f, -0.035f, 0.57f, Paper.fillPaint("#5f2a24"))
            }
            "lamp" -> {
                r(-0.04f, 0.62f, 0.04f, 0f, darkBrown)
                c.drawPath(Path().apply {
                    moveTo(cx - 0.20f * pw, cy(0.62f))
                    lineTo(cx + 0.20f * pw, cy(0.62f))
                    lineTo(cx + 0.12f * pw, cy(0.92f))
                    lineTo(cx - 0.12f * pw, cy(0.92f))
                    close()
                }, Paper.fillPaint(Paper.YELLOW, if (night) 255 else 200))
                if (night) c.drawOval(RectF(cx - pw * 0.38f, cy(1.0f),
                    cx + pw * 0.38f, cy(0.3f)), Paper.fillPaint("#f2c230", 34))
            }
            "plant" -> {
                r(-0.16f, 0.22f, 0.16f, 0f, Paper.fillPaint("#b5654a"))
                val gp = Paper.fillPaint("#2a7a4a")
                c.drawOval(RectF(cx - 0.30f * pw, cy(0.62f), cx, cy(0.2f)), gp)
                c.drawOval(RectF(cx, cy(0.62f), cx + 0.30f * pw, cy(0.2f)), gp)
                c.drawOval(RectF(cx - 0.18f * pw, cy(0.78f), cx + 0.18f * pw, cy(0.3f)), gp)
            }
            "shelf" -> {
                r(-0.45f, 1.1f, 0.45f, 0f, brown)
                for (row in 0..2) {
                    val topF = 0.98f - row * 0.32f
                    var bx = cx - 0.38f * pw
                    var k = 0
                    while (bx < cx + 0.28f * pw) {
                        // v0.17.0b: the muted book palette the
                        // furniture painter already uses, plus a
                        // spine shade line — the old cycle was
                        // four flat primaries (red blocks on
                        // shelves in Lucky's screenshots)
                        val bc = listOf("#7a4a3a", "#33512f", "#2a4a8a",
                            "#8a6a3a", "#6a3a5a", "#e8e2d2")[(k + row) % 6]
                        c.drawRect(bx, baseY - topF * ph, bx + pw * 0.09f,
                            baseY - (topF - 0.22f) * ph, Paper.fillPaint(bc))
                        c.drawRect(bx + pw * 0.02f, baseY - topF * ph,
                            bx + pw * 0.035f, baseY - (topF - 0.22f) * ph,
                            Paper.fillPaint(darkerHex(bc, 0.65f)))
                        bx += pw * 0.13f
                        k++
                    }
                }
            }
            "crate" -> {
                r(-0.35f, 0.6f, 0.35f, 0f, Paper.fillPaint("#9a7a4a"))
                val xp = Paper.strokePaint("#6b4a2f", 3f)
                c.drawLine(cx - 0.35f * pw, cy(0.6f), cx + 0.35f * pw, cy(0f), xp)
                c.drawLine(cx + 0.35f * pw, cy(0.6f), cx - 0.35f * pw, cy(0f), xp)
            }
            "hay" -> {
                c.drawOval(RectF(cx - 0.45f * pw, cy(0.55f), cx + 0.45f * pw, baseY),
                    Paper.fillPaint("#d9b34a"))
                c.drawOval(RectF(cx - 0.3f * pw, cy(0.8f), cx + 0.3f * pw, cy(0.35f)),
                    Paper.fillPaint("#e8c53a"))
            }
            "counter", "barcounter" -> {
                r(-0.6f, 0.55f, 0.6f, 0f, if (prop == "counter") Paper.fillPaint("#8a8a8a") else darkBrown)
                r(-0.66f, 0.66f, 0.66f, 0.55f, cream)
            }
            "stool" -> {
                c.drawOval(RectF(cx - 0.2f * pw, cy(0.55f), cx + 0.2f * pw, cy(0.38f)),
                    Paper.fillPaint("#b5654a"))
                r(-0.04f, 0.42f, 0.04f, 0f, darkBrown)
            }
            "espresso" -> {
                r(-0.25f, 0.62f, 0.25f, 0.05f, Paper.fillPaint("#5a5a6a"))
                r(-0.25f, 0.73f, 0.25f, 0.62f, Paper.fillPaint("#3a3a4a"))
                c.drawCircle(cx, cy(0.18f), pw * 0.06f, cream)
            }
            "menu", "poster", "sign" -> {
                r(-0.35f, 1.05f, 0.35f, 0.25f, cream)
                val lp = Paper.strokePaint("#5a4a3a", 2.5f)
                for (i in 0..3) {
                    val ly = cy(0.88f - i * 0.15f)
                    c.drawLine(cx - 0.26f * pw, ly, cx + 0.26f * pw - (i % 2) * 14f, ly, lp)
                }
            }
            "dartboard" -> {
                c.drawCircle(cx, cy(0.62f), pw * 0.28f, Paper.fillPaint("#2a2a2a"))
                c.drawCircle(cx, cy(0.62f), pw * 0.19f, cream)
                c.drawCircle(cx, cy(0.62f), pw * 0.09f, Paper.fillPaint(Paper.RED))
            }
            "screen" -> {
                r(-0.6f, 1.4f, 0.6f, 0.3f, Paper.fillPaint(if (night) "#c8d4e8" else "#e8ecf2"))
                r(-0.6f, 1.4f, 0.6f, 0.3f, Paper.strokePaint("#2a2a3a", 4f))
            }
            "seats", "pew", "pews", "busseats" -> {
                val col = when (prop) {
                    "pew", "pews" -> "#7a5c40"
                    "busseats" -> "#2a6f8a"
                    else -> "#7a2a3a"
                }
                r(-0.55f, 0.72f, -0.08f, 0f, Paper.fillPaint(col))
                r(0.08f, 0.72f, 0.55f, 0f, Paper.fillPaint(col))
                r(-0.55f, 0.72f, -0.08f, 0.6f, Paper.fillPaint(Paper.darker(col)))
                r(0.08f, 0.72f, 0.55f, 0.6f, Paper.fillPaint(Paper.darker(col)))
            }
            "popcorn" -> {
                r(-0.14f, 0.42f, 0.14f, 0f, Paper.fillPaint("#f0f0f0"))
                r(-0.14f, 0.42f, 0.14f, 0.28f, Paper.fillPaint(Paper.RED))
                val pp = Paper.fillPaint("#f2e2a0")
                repeat(5) {
                    c.drawCircle(cx - 0.1f * pw + it * 0.05f * pw, cy(0.47f), 4.5f, pp)
                }
            }
            "examtable" -> {
                r(-0.55f, 0.5f, 0.55f, 0.4f, Paper.fillPaint("#c8d4d8"))
                r(-0.5f, 0.4f, -0.42f, 0f, darkBrown)
                r(0.42f, 0.4f, 0.5f, 0f, darkBrown)
                r(-0.55f, 0.63f, -0.2f, 0.5f, Paper.fillPaint("#e8eef2"))
            }
            "vault" -> {
                c.drawCircle(cx, cy(0.5f), pw * 0.42f, Paper.fillPaint("#6a6a72"))
                c.drawCircle(cx, cy(0.5f), pw * 0.30f, Paper.fillPaint("#8a8a94"))
                c.drawCircle(cx, cy(0.5f), pw * 0.08f, Paper.fillPaint("#4a4a52"))
            }
            "rope" -> {
                r(-0.43f, 0.55f, -0.37f, 0f, Paper.fillPaint(Paper.RED))
                r(0.37f, 0.55f, 0.43f, 0f, Paper.fillPaint(Paper.RED))
                c.drawArc(RectF(cx - 0.4f * pw, cy(0.75f), cx + 0.4f * pw, cy(0.3f)),
                    20f, 140f, false, Paper.strokePaint("#8a2a3a", 4f))
            }
            "mailslots" -> {
                r(-0.5f, 1.1f, 0.5f, 0.15f, Paper.fillPaint("#7a6a4a"))
                val lp = Paper.strokePaint("#4a3a26", 2f)
                for (row in 0..2) for (colI in 0..3) {
                    val l = cx + (-0.42f + colI * 0.22f) * pw
                    val t = baseY - (1.0f - row * 0.27f) * ph
                    c.drawRect(l, t, l + 0.16f * pw, t + 0.18f * ph, lp)
                }
            }
            "blackboard" -> {
                r(-0.62f, 1.35f, 0.62f, 0.35f, Paper.fillPaint("#2a4a3a"))
                r(-0.62f, 1.35f, 0.62f, 0.35f, Paper.strokePaint("#6b4a2f", 6f))
                val cp = Paper.textPaint("#ffffff", ph * 0.17f, casual = true)
                cp.textAlign = Paint.Align.CENTER
                c.drawText("2 + 2 = 4", cx, cy(0.95f), cp)
                c.drawText("Sit down!", cx, cy(0.68f), cp)
            }
            "flag" -> {
                val fp = Paper.strokePaint("#5a5a5a", 4f)
                c.drawLine(cx + 0.2f * pw, cy(0.05f), cx + 0.2f * pw, cy(1.25f), fp)
                c.drawRect(cx + 0.2f * pw, cy(1.25f), cx + 0.52f * pw, cy(0.97f),
                    Paper.fillPaint(Paper.RED))
            }
            "podium" -> {
                c.drawPath(Path().apply {
                    moveTo(cx - 0.28f * pw, cy(0.85f))
                    lineTo(cx + 0.28f * pw, cy(0.85f))
                    lineTo(cx + 0.19f * pw, baseY)
                    lineTo(cx - 0.19f * pw, baseY)
                    close()
                }, brown)
            }
            "altar" -> {
                r(-0.5f, 0.55f, 0.5f, 0f, cream)
                r(-0.5f, 0.63f, 0.5f, 0.55f, Paper.fillPaint("#c8a878"))
                val cp = Paper.strokePaint("#c8a028", 5f)
                c.drawLine(cx, cy(1.05f), cx, cy(0.7f), cp)
                c.drawLine(cx - 0.12f * pw, cy(0.93f), cx + 0.12f * pw, cy(0.93f), cp)
            }
            "candles" -> {
                for (i in -1..1) {
                    r(i * 0.18f - 0.035f, 0.35f, i * 0.18f + 0.035f, 0.05f, cream)
                    c.drawCircle(cx + i * 0.18f * pw, cy(0.43f), 4.5f,
                        Paper.fillPaint("#f2a230", if (night) 255 else 190))
                }
            }
            "window" -> { // stained-glass style window (simple bands)
                r(-0.25f, 1.35f, 0.25f, 0.3f, Paper.fillPaint("#7a9ac8"))
                r(-0.25f, 1.35f, 0.25f, 1.0f, Paper.fillPaint("#f2c230", 150))
                r(-0.25f, 0.68f, 0.25f, 0.3f, Paper.fillPaint("#c86a9a", 150))
            }
            "booth" -> {
                r(-0.28f, 0.5f, 0.28f, 0.42f, brown)
                r(-0.56f, 0.64f, -0.34f, 0f, Paper.fillPaint("#a03a4a"))
                r(0.34f, 0.64f, 0.56f, 0f, Paper.fillPaint("#a03a4a"))
            }
            "stove" -> {
                r(-0.22f, 0.6f, 0.22f, 0f, Paper.fillPaint("#3a3a3a"))
                r(-0.05f, 1.15f, 0.05f, 0.6f, Paper.fillPaint("#2a2a2a"))
                c.drawCircle(cx, cy(0.3f), 5f, Paper.fillPaint("#f2a230", 230))
            }
            "icehole" -> {
                c.drawOval(RectF(cx - 0.35f * pw, baseY - 0.13f * ph,
                    cx + 0.35f * pw, baseY + 0.05f * ph), Paper.fillPaint("#2a4a6a"))
                c.drawOval(RectF(cx - 0.35f * pw, baseY - 0.13f * ph,
                    cx + 0.35f * pw, baseY + 0.05f * ph), Paper.strokePaint("#c8e2f2", 3f))
            }
            "bench" -> {
                r(-0.5f, 0.76f, 0.5f, 0.66f, brown)
                r(-0.5f, 0.42f, 0.5f, 0.34f, brown)
                r(-0.42f, 0.34f, -0.36f, 0f, darkBrown)
                r(0.36f, 0.34f, 0.42f, 0f, darkBrown)
            }
            "lantern" -> {
                r(-0.1f, 0.34f, 0.1f, 0.02f, Paper.fillPaint("#3a3a3a"))
                c.drawCircle(cx, cy(0.18f), pw * 0.07f,
                    Paper.fillPaint("#f2c230", if (night) 255 else 170))
            }
            "fountain" -> {
                c.drawOval(RectF(cx - 0.55f * pw, cy(0.22f), cx + 0.55f * pw, baseY + 0.04f * ph),
                    Paper.fillPaint("#5a8ac8"))
                r(-0.05f, 0.55f, 0.05f, 0.15f, Paper.fillPaint("#8a8a8a"))
                c.drawCircle(cx, cy(0.62f), 6f, Paper.fillPaint("#a8c8e8", 200))
            }
            "wheel" -> {
                c.drawCircle(cx, cy(0.55f), pw * 0.26f, Paper.strokePaint("#2a2a2a", 6f))
                c.drawLine(cx - 0.26f * pw, cy(0.55f), cx + 0.26f * pw, cy(0.55f),
                    Paper.strokePaint("#2a2a2a", 4f))
            }
            else -> r(-0.3f, 0.5f, 0.3f, 0f, brown)
        }
    }

    // ---------------- trash + litter sprites (shared inside/outside) ----------------

    fun drawTrashContainer(c: Canvas, cx: Float, baseY: Float, size: Float,
                           kind: String, searched: Boolean) {
        val body = if (searched) "#6a6a6a" else "#3f6a4a"
        when (kind) {
            "dumpster" -> {
                c.drawRect(cx - size * 0.7f, baseY - size * 0.62f, cx + size * 0.7f, baseY,
                    Paper.fillPaint(body))
                if (searched) {
                    c.drawRect(cx - size * 0.7f, baseY - size * 1.02f, cx + size * 0.7f,
                        baseY - size * 0.9f, Paper.fillPaint("#33543d"))
                } else {
                    c.drawRect(cx - size * 0.72f, baseY - size * 0.74f, cx + size * 0.72f,
                        baseY - size * 0.62f, Paper.fillPaint("#33543d"))
                }
            }
            "pile", "bags" -> {
                val p = Paper.fillPaint(if (searched) "#5a5a52" else "#4a4a44")
                c.drawOval(RectF(cx - size * 0.55f, baseY - size * 0.5f, cx + size * 0.1f, baseY), p)
                c.drawOval(RectF(cx - size * 0.1f, baseY - size * 0.62f, cx + size * 0.55f, baseY), p)
                if (!searched) c.drawCircle(cx + size * 0.3f, baseY - size * 0.55f, 3.5f,
                    Paper.fillPaint("#f2c230", 220))
            }
            else -> { // can
                c.drawRect(cx - size * 0.3f, baseY - size * 0.66f, cx + size * 0.3f, baseY,
                    Paper.fillPaint(body))
                c.drawRect(cx - size * 0.34f, baseY - size * 0.74f, cx + size * 0.34f,
                    baseY - size * 0.66f, Paper.fillPaint(if (searched) "#555555" else "#33543d"))
                if (searched) c.drawOval(RectF(cx - size * 0.2f, baseY - size * 0.8f,
                    cx + size * 0.2f, baseY - size * 0.68f), Paper.fillPaint("#2a2a2a"))
            }
        }
        if (!searched) {
            c.drawCircle(cx + size * 0.5f, baseY - size * 0.85f, 3f,
                Paper.fillPaint("#ffffff", 200))
        }
    }

    fun drawLitter(c: Canvas, cx: Float, cy: Float, size: Float, icon: String) {
        c.drawCircle(cx, cy, size * 0.42f, Paper.fillPaint("#000000", 50))
        val tp = Paper.textPaint("#ffffff", size * 0.8f)
        tp.textAlign = Paint.Align.CENTER
        c.drawText(icon, cx, cy + size * 0.28f, tp)
    }

    // ============================================================
    // v0.5.0g — the interior FOREGROUND layer (the scene depth
    // pass). The near furniture between the crowd and the
    // camera — the back of the sofa, the counter front, the
    // pew backs — drawn by GameView AFTER the figures, inside
    // the bottom strip only (Depth.FG_TOP_IN; the occlusion
    // budget is pinned in Depth: ground and ankles, never a
    // face). One painter per theme KIND, colored from the
    // room's own floor/wall. The token each theme answers to
    // lives in CharacterSystem.INTERIOR_FOREGROUND; markers:
    // iforeground: sofa-back-row
    // iforeground: loft-sofa-back
    // iforeground: desk-front-row
    // iforeground: office-desk-front
    // iforeground: cafe-counter-front
    // iforeground: restaurant-counter-front
    // iforeground: diner-counter-front
    // iforeground: bar-front
    // iforeground: clinic-counter-front
    // iforeground: bank-counter-front
    // iforeground: shop-shelf-edge
    // iforeground: mall-shelf-edge
    // iforeground: pew-backs
    // iforeground: theater-seat-backs
    // iforeground: bus-seat-backs
    // iforeground: pavilion-rail
    // iforeground: hut-table-edge
    // iforeground: barn-fence-rail
    // ============================================================
    fun drawForegroundLayer(c: Canvas, w: Int, h: Int, def: InteriorDef,
                            timeMin: Int, weatherId: String, seed: Long) {
        val kind = Depth.interiorForegroundKind(def.theme) ?: return
        val strip = RectF(0f, h * Depth.FG_TOP_IN, w.toFloat(), h.toFloat())
        drawForegroundStrip(c, strip, kind, def.floorHex, def.wallHex,
            SceneArt.timeOfDay(timeMin) == "night", seed)
    }

    /** The strip painter, shared with PhotoView. All ink
     *  stays inside [strip]. */
    fun drawForegroundStrip(c: Canvas, strip: RectF, kind: FgKind,
                            primary: String, secondary: String,
                            night: Boolean, seed: Long) {
        val L = strip.left; val T = strip.top
        val R = strip.right; val B = strip.bottom
        val W = R - L; val H = B - T
        if (W <= 0f || H <= 0f) return
        val p = Paper.fillPaint(primary)
        val dk = Paper.fillPaint(Paper.darker(primary, 0.60f))
        val dkHex = "#%06X".format(0xFFFFFF and Paper.darker(primary, 0.60f))
        val sec = Paper.fillPaint(secondary)
        // the seat shadow where the near strip meets the room
        c.drawRect(L, T, R, T + H * 0.07f, Paper.fillPaint("#000000", 42))
        // v0.16.0d (stage 3): the near furniture strip is a
        // large prop surface — the paper material washes its
        // band under the kind's own fronts and backs
        Paper.applyMaterial(c, L, T, R, B, primary, seed + 31L, 0.8f)
        when (kind) {
            FgKind.COUNTER -> {
                c.drawRect(L, T + H * 0.20f, R, B, dk)
                c.drawRect(L, T, R, T + H * 0.20f, p)
                c.drawRect(L, T + H * 0.20f, R, T + H * 0.26f,
                    Paper.fillPaint("#ffffff", 46))
            }
            FgKind.PEWS -> {
                val step = W / 6f
                var x = L + step * 0.08f
                while (x < R - step * 0.5f) {
                    val pw = step * 0.80f
                    c.drawRoundRect(RectF(x, T + H * 0.10f, x + pw, B),
                        H * 0.09f, H * 0.09f, p)
                    c.drawRoundRect(RectF(x, T + H * 0.10f, x + pw, T + H * 0.30f),
                        H * 0.09f, H * 0.09f, dk)
                    x += step
                }
            }
            FgKind.SHELF_EDGE -> {
                c.drawRect(L, T + H * 0.08f, R, T + H * 0.27f, dk)
                val rnd = Random(seed)
                var x = L + W * 0.012f
                while (x < R - W * 0.03f) {
                    val gw = W * (0.018f + rnd.nextFloat() * 0.02f)
                    val gh = H * (0.34f + rnd.nextFloat() * 0.38f)
                    val paint = when (rnd.nextInt(3)) {
                        0 -> p; 1 -> sec; else -> dk
                    }
                    c.drawRect(x, T + H * 0.27f, x + gw, T + H * 0.27f + gh, paint)
                    x += gw + W * 0.008f
                }
                c.drawRect(L, B - H * 0.10f, R, B, dk)
            }
            FgKind.SOFA_BACK -> {
                c.drawRoundRect(RectF(L + W * 0.03f, T + H * 0.06f,
                    R - W * 0.03f, B + H * 0.1f), H * 0.14f, H * 0.14f, p)
                val seam = Paper.strokePaint(dkHex, 3f, 130)
                for (f in listOf(0.36f, 0.64f)) {
                    val x = L + W * f
                    c.drawLine(x, T + H * 0.16f, x, B, seam)
                }
                c.drawRect(L + W * 0.03f, T + H * 0.06f, R - W * 0.03f,
                    T + H * 0.13f, Paper.fillPaint("#ffffff", 40))
            }
            FgKind.DESK_EDGE -> {
                val step = W / 5f
                var x = L + step * 0.06f
                while (x < R - step * 0.6f) {
                    val dw = step * 0.82f
                    c.drawRect(x, T + H * 0.24f, x + dw, B, p)
                    c.drawRect(x - dw * 0.05f, T + H * 0.13f,
                        x + dw * 1.05f, T + H * 0.28f, sec)
                    x += step
                }
            }
            FgKind.SEAT_BACKS -> {
                val step = W / 7f
                var x = L + step * 0.07f
                while (x < R - step * 0.55f) {
                    val sw = step * 0.78f
                    c.drawRoundRect(RectF(x, T + H * 0.08f, x + sw, B),
                        sw * 0.32f, sw * 0.32f, p)
                    c.drawRoundRect(RectF(x, T + H * 0.08f, x + sw, T + H * 0.34f),
                        sw * 0.32f, sw * 0.32f, dk)
                    x += step
                }
            }
            FgKind.TABLE_EDGE -> {
                c.drawRect(L, T + H * 0.18f, R, B, p)
                c.drawRect(L, T, R, T + H * 0.18f, sec)
                for (fx in listOf(0.30f, 0.62f)) {
                    val mx = L + W * fx
                    c.drawRect(mx, T + H * 0.02f, mx + W * 0.022f,
                        T + H * 0.18f, dk)
                }
            }
            FgKind.RAIL -> {
                var x = L + W * 0.015f
                while (x < R) {
                    c.drawRect(x, T + H * 0.20f, x + W * 0.012f, B, dk)
                    x += W / 10f
                }
                c.drawRect(L, T, R, T + H * 0.21f, p)
                c.drawRect(L, T + H * 0.56f, R, T + H * 0.66f, p)
            }
            FgKind.FENCE -> {
                var x = L + W * 0.02f
                while (x < R) {
                    c.drawRect(x, T, x + W * 0.014f, B, dk)
                    x += W / 9f
                }
                c.drawRect(L, T + H * 0.30f, R, T + H * 0.44f, p)
                c.drawRect(L, T + H * 0.63f, R, T + H * 0.77f, p)
            }
            else -> c.drawRect(L, T + H * 0.3f, R, B, p)
        }
        // rooms darken their near furniture with the room
        if (night) c.drawRect(L, T, R, B, Paper.fillPaint("#0a1030", 60))
    }
}

// ============================================================
// FurnitureArt — v0.16.0e, Part 1 item 3: THE INTERIOR
// FURNITURE LANGUAGE. GameView's layout painter used to
// draw every piece as one rounded rect with a lit top
// edge — a slab in a body color, whatever the piece was
// supposed to be. That painter is removed whole; this is
// its replacement. Every kind is drawn as the thing it
// is, in the paper pipeline: a cut silhouette with a
// painted outline, paper material on the front face, a
// lit TOP FACE and a shaded SIDE — depth in the cut-paper
// idiom, not in slabs. Sizes still come off the one
// kid-unit ladder (InteriorLayouts + WorldScale); the
// seat/work line still lands at the piece's seatYU; the
// hit rect GameView plants is unchanged. Per-ROOM-FAMILY
// dressing: a trim accent keyed to the room kind, so a
// kitchen reads kitchen, a classroom reads classroom,
// even before you name the pieces.
// ============================================================
object FurnitureArt {

    /** The body color of a piece, by kind (the palette
     *  the slab painter used — kept, so rooms don't
     *  change their colors, only their language). */
    fun bodyHex(kind: String): String = when (kind) {
        "bed" -> "#7a9ac0"
        "sofa", "chair", "bench", "pew", "busseat" -> "#8a5a48"
        "counter" -> "#a8845c"
        "shelf" -> "#6e5136"
        "desk", "table" -> "#8a6a44"
        else -> "#7d6a52"
    }

    /** The room family's trim accent: cushions, blankets,
     *  counter tops and shelf dressing take this color's
     *  family, so the room's pieces agree with each other
     *  and disagree with the next room's. */
    fun trimHex(roomKind: String): String = when (roomKind) {
        "kitchen", "counter", "cafe" -> "#e8e2d2"
        "bed" -> "#b03a2e"
        "living" -> "#33512f"
        "bath" -> "#bcd6f2"
        "class", "library" -> "#7a2e2e"
        "office", "stock" -> "#4a5a6a"
        "sales" -> "#d9a441"
        "worship", "stage" -> "#8a6a3a"
        "seats", "cab" -> "#2a4a8a"
        else -> "#e8e2d2"
    }

    private fun fmt(colorInt: Int): String =
        "#%06X".format(0xFFFFFF and colorInt)

    /**
     * Draw one dressed piece. [fx] is its center, [baseY]
     * the line it stands on (wall line for back pieces,
     * floor line for floor-front), [fw]/[fhgt] its ladder
     * size in px, [seatYPx] the seat height in px above
     * [baseY] where the piece seats anyone (0 = no seat),
     * [back] whether it stands against the wall (no legs;
     * a plinth instead), [seed] its paper seed.
     */
    fun drawPiece(c: Canvas, kind: String, roomKind: String,
                  fx: Float, baseY: Float, fw: Float, fhgt: Float,
                  seatYPx: Float, back: Boolean, seed: Long) {
        if (fw < 4f || fhgt < 4f) return
        val l = fx - fw / 2f
        val r = fx + fw / 2f
        val top = baseY - fhgt
        val body = bodyHex(kind)
        val trim = trimHex(roomKind)
        val ink = fmt(Paper.darker(body, 0.55f))
        val side = fmt(Paper.darker(body, 0.78f))
        val topFace = fmt(Paper.lighter(body, 1.16f))
        val depth = minOf(fw * 0.07f, fhgt * 0.16f, 9f)
        // the paper shadow the cut-out stands on
        c.drawOval(RectF(l + 2f, baseY - 3f, r - 2f, baseY + 4f),
            Paper.fillPaint("#1a2436", 40))
        // the silhouette: material + painted outline,
        // shared by every piece (the pipeline's base read)
        fun silhouette(t: Float, b: Float) {
            c.drawRect(l, t, r, b, Paper.fillPaint(body))
            Paper.applyMaterial(c, l, t, r, b, body, seed, 0.7f)
            Paper.paintedOutline(c,
                PaperCut.cutRectContour(l, t, r, b, 3f, seed),
                seed, 1.7f, ink)
            // the SIDE shade: a darker cut along the right
            // edge — the piece's thickness, in paper
            c.drawRect(r - depth, t, r, b, Paper.fillPaint(side, 110))
        }
        // legs or a plinth under a top-bearing piece
        fun legs(t: Float) {
            if (back) {
                c.drawRect(l + 1f, baseY - fhgt * 0.07f, r - 1f, baseY,
                    Paper.fillPaint(side))
            } else {
                val lw = maxOf(3f, fw * 0.05f)
                c.drawRect(l + fw * 0.07f, t, l + fw * 0.07f + lw, baseY,
                    Paper.fillPaint(side))
                c.drawRect(r - fw * 0.07f - lw, t, r - fw * 0.07f, baseY,
                    Paper.fillPaint(side))
            }
        }
        fun seatSlab(y: Float) {
            // the seat/work surface: a lit top face with a
            // dark front edge — the line figures sit ON,
            // at exactly the ladder's seat height
            c.drawRect(l - 1f, y - depth * 0.55f, r + 1f, y,
                Paper.fillPaint(topFace))
            c.drawRect(l - 1f, y, r + 1f, y + 2.5f,
                Paper.fillPaint("#3a2c20", 120))
        }
        when (kind) {
            "bed" -> {
                // headboard, blanket over the mattress, pillow
                c.drawRect(l, top, r, top + fhgt * 0.30f,
                    Paper.fillPaint(side))
                silhouette(top + fhgt * 0.22f, baseY)
                c.drawRect(l + 2f, top + fhgt * 0.30f, r - 2f,
                    top + fhgt * 0.52f, Paper.fillPaint("#e8e2d2"))
                c.drawRoundRect(RectF(l + fw * 0.04f, top + fhgt * 0.24f,
                    l + fw * 0.30f, top + fhgt * 0.46f), 4f, 4f,
                    Paper.fillPaint("#f4ecd8"))
                // the blanket, in the room's trim, with its fold
                c.drawRect(l + 2f, top + fhgt * 0.52f, r - 2f, baseY - 2f,
                    Paper.fillPaint(trim))
                c.drawRect(l + 2f, top + fhgt * 0.52f, r - 2f,
                    top + fhgt * 0.58f, Paper.fillPaint(
                        fmt(Paper.darker(trim, 0.8f))))
            }
            "sofa" -> {
                // back cushions over a seat, arms at the ends
                silhouette(top, baseY)
                val backBot = baseY - seatYPx - fhgt * 0.06f
                val n = if (fw > 130f) 3 else 2
                for (i in 0 until n) {
                    val cl = l + fw * 0.08f + (fw * 0.84f) * i / n
                    val cr = l + fw * 0.08f + (fw * 0.84f) * (i + 1) / n
                    c.drawRoundRect(RectF(cl + 1.5f, top + fhgt * 0.08f,
                        cr - 1.5f, backBot), 4f, 4f,
                        Paper.fillPaint(fmt(Paper.lighter(body, 1.07f))))
                }
                c.drawRect(l, top + fhgt * 0.10f, l + fw * 0.09f, baseY,
                    Paper.fillPaint(side))
                c.drawRect(r - fw * 0.09f, top + fhgt * 0.10f, r, baseY,
                    Paper.fillPaint(side))
                // seat cushions in the room's trim family
                c.drawRect(l + fw * 0.09f, backBot, r - fw * 0.09f,
                    baseY - seatYPx, Paper.fillPaint(
                        fmt(Paper.darker(trim, 0.9f)), 90))
                if (seatYPx > 0f) seatSlab(baseY - seatYPx)
            }
            "chair", "busseat" -> {
                // a back over a seat: the chair's whole grammar
                val backTop = if (kind == "busseat") top else top + fhgt * 0.04f
                c.drawRoundRect(RectF(l + fw * 0.06f, backTop,
                    r - fw * 0.06f, baseY - seatYPx), 5f, 5f,
                    Paper.fillPaint(body))
                Paper.applyMaterial(c, l + fw * 0.06f, backTop,
                    r - fw * 0.06f, baseY - seatYPx, body, seed, 0.7f)
                if (kind == "busseat") {
                    // the bus seat's cushion seam + headrest band
                    c.drawRect(l + fw * 0.06f, backTop + fhgt * 0.16f,
                        r - fw * 0.06f, backTop + fhgt * 0.20f,
                        Paper.fillPaint(trim, 160))
                }
                if (seatYPx > 0f) {
                    seatSlab(baseY - seatYPx)
                    legs(baseY - seatYPx + 2.5f)
                }
            }
            "bench" -> {
                // two slats and their supports — a bench is
                // boards, and reads as boards
                val slatH = fhgt * 0.13f
                c.drawRect(l, top, r, top + slatH, Paper.fillPaint(body))
                c.drawRect(l, top + slatH * 1.7f, r, top + slatH * 2.7f,
                    Paper.fillPaint(body))
                Paper.paintedOutline(c,
                    PaperCut.cutRectContour(l, top, r,
                        top + slatH * 2.7f, 2f, seed), seed, 1.5f, ink)
                if (seatYPx > 0f) {
                    seatSlab(baseY - seatYPx)
                    legs(baseY - seatYPx + 2.5f)
                }
            }
            "pew" -> {
                // the long back, the seat, the two end panels
                silhouette(top, baseY)
                c.drawRect(l, top, l + fw * 0.035f, baseY,
                    Paper.fillPaint(fmt(Paper.lighter(body, 1.12f))))
                c.drawRect(r - fw * 0.035f, top, r, baseY,
                    Paper.fillPaint(fmt(Paper.lighter(body, 1.12f))))
                c.drawRect(l + fw * 0.04f, top + fhgt * 0.30f,
                    r - fw * 0.04f, top + fhgt * 0.36f,
                    Paper.fillPaint(side, 130))
                if (seatYPx > 0f) seatSlab(baseY - seatYPx)
            }
            "counter" -> {
                silhouette(top, baseY)
                // the work top overhangs, lit — the counter's read
                c.drawRect(l - fw * 0.03f, top - depth * 0.5f,
                    r + fw * 0.03f, top + fhgt * 0.07f,
                    Paper.fillPaint(trim))
                c.drawRect(l - fw * 0.03f, top + fhgt * 0.07f,
                    r + fw * 0.03f, top + fhgt * 0.10f,
                    Paper.fillPaint(fmt(Paper.darker(trim, 0.75f))))
                // cabinet fronts: two inset panels, sparse
                val insetP = Paper.strokePaint(side, 2f)
                for (fxFrac in listOf(0.08f, 0.54f)) {
                    c.drawRect(l + fw * fxFrac, top + fhgt * 0.24f,
                        l + fw * (fxFrac + 0.36f), top + fhgt * 0.82f,
                        insetP)
                }
            }
            "shelf" -> {
                // posts, three boards, and what's ON them:
                // book blocks and boxes — large, sparse,
                // seeded per piece (never a dot pattern)
                silhouette(top, baseY)
                val rnd = java.util.Random(seed)
                for (s in 0 until 3) {
                    val sy = top + fhgt * (0.30f + s * 0.30f)
                    c.drawRect(l + 2f, sy, r - 2f, sy + 3f,
                        Paper.fillPaint(fmt(Paper.lighter(body, 1.2f))))
                    var bx = l + fw * 0.08f
                    val maxX = r - fw * 0.10f
                    while (bx < maxX) {
                        val bw2 = fw * (0.07f + rnd.nextFloat() * 0.06f)
                        val bh2 = fhgt * (0.13f + rnd.nextFloat() * 0.08f)
                        val bookHex = listOf("#7a4a3a", "#33512f",
                            "#2a4a8a", "#8a6a3a", "#6a3a5a",
                            "#e8e2d2")[rnd.nextInt(6)]
                        c.drawRect(bx, sy - bh2, minOf(bx + bw2, maxX), sy,
                            Paper.fillPaint(bookHex, 200))
                        // v0.17.0b: a spine shade line per book,
                        // so a shelf reads as books, not blocks
                        c.drawRect(bx + bw2 * 0.22f, sy - bh2,
                            bx + bw2 * 0.36f, sy,
                            Paper.fillPaint(darkerHex(bookHex, 0.65f), 200))
                        bx += bw2 + fw * 0.025f
                    }
                }
            }
            "desk" -> {
                // a top slab, a drawer stack at one side, an
                // open knee space — a desk, not a block
                c.drawRect(l, top, r, top + fhgt * 0.10f,
                    Paper.fillPaint(topFace))
                Paper.paintedOutline(c,
                    PaperCut.cutRectContour(l, top, r,
                        top + fhgt * 0.10f, 2f, seed), seed, 1.5f, ink)
                val stackW = fw * 0.34f
                c.drawRect(r - stackW, top + fhgt * 0.10f, r, baseY,
                    Paper.fillPaint(body))
                val drawerP = Paper.strokePaint(side, 2f)
                for (d in 0 until 2) {
                    val dy = top + fhgt * (0.18f + d * 0.34f)
                    c.drawRect(r - stackW + 4f, dy, r - 4f,
                        dy + fhgt * 0.26f, drawerP)
                    c.drawCircle(r - stackW / 2f, dy + fhgt * 0.13f,
                        2.2f, Paper.fillPaint("#e8c53a"))
                }
                c.drawRect(l + fw * 0.05f, top + fhgt * 0.10f,
                    l + fw * 0.05f + maxOf(3f, fw * 0.045f), baseY,
                    Paper.fillPaint(side))
            }
            "table" -> {
                // top slab with an apron, two legs
                c.drawRect(l, top, r, top + fhgt * 0.09f,
                    Paper.fillPaint(topFace))
                Paper.paintedOutline(c,
                    PaperCut.cutRectContour(l, top, r,
                        top + fhgt * 0.09f, 2f, seed), seed, 1.5f, ink)
                c.drawRect(l + fw * 0.06f, top + fhgt * 0.09f,
                    r - fw * 0.06f, top + fhgt * 0.22f,
                    Paper.fillPaint(body))
                legs(top + fhgt * 0.22f)
            }
            else -> {
                silhouette(top, baseY)
                c.drawRect(l, top, r, top + fhgt * 0.10f,
                    Paper.fillPaint(topFace))
                if (seatYPx > 0f) seatSlab(baseY - seatYPx)
            }
        }
    }
}

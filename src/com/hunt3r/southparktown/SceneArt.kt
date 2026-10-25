package com.hunt3r.southparktown

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// ============================================================
// SceneArt — first-person POV backgrounds, paper-cutout style.
// Flat sky bands, jagged snowy mountains, location facade in
// the location's color, snow ground, weather particles.
// Holiday decorations when a holiday is active.
// ============================================================
object SceneArt {

    /** v0.16.0e PASS 2 (the costume system): the active
     *  fiction's dressing set, handed in by GameView
     *  every frame. The renderer consumes BASE + ACTIVE
     *  DIFFS and nothing else — the painters below draw
     *  the frozen town exactly as ever, and the overlay
     *  painter lays the current tick's dressings over
     *  it. Empty (no fiction) = the bare town. */
    var fictionDress: Map<String, Fiction.ParcelDress> =
        emptyMap()
    var fictionDef: Fiction.Def? = null

    /** day: 7-18, dusk: 18-21, night: 21-7 */
    // v0.3.0a: the band table lives in SceneLight now (one
    // source of truth shared with PhotoView and the suite).
    fun timeOfDay(timeMin: Int): String = SceneLight.bandOf(timeMin)

    fun drawScene(c: Canvas, w: Int, h: Int, loc: Location, weatherId: String,
                  timeMin: Int, seed: Long, season: Season, holiday: Holiday?,
                  parallaxShift: Float = 0f) {
        val rnd = Random(seed + loc.id.hashCode())
        val tod = timeOfDay(timeMin)
        val night = tod == "night"

        // ---- sky bands ----
        val skyTop: String
        val skyBot: String
        when (tod) {
            "day" -> { skyTop = Paper.SKY_DAY_TOP; skyBot = Paper.SKY_DAY_BOT }
            "dusk" -> { skyTop = Paper.SKY_DUSK_TOP; skyBot = Paper.SKY_DUSK_BOT }
            else -> { skyTop = Paper.SKY_NIGHT_TOP; skyBot = Paper.SKY_NIGHT_BOT }
        }
        // weather tints the sky
        var topC = skyTop
        var botC = skyBot
        if (tod != "night") {
            when (weatherId) {
                "cloudy", "light_snow" -> { topC = "#cfd8e2"; botC = "#eef2f6" }
                "heavy_snow", "blizzard" -> { topC = "#b9c6d4"; botC = "#dde5ec" }
                "rain" -> { topC = "#aeb9c6"; botC = "#d4dbe2" }
            }
        }
        val bands = 8
        for (i in 0 until bands) {
            val t0 = i / bands.toFloat()
            val t1 = (i + 1) / bands.toFloat()
            val p = Paper.fillPaint(mix(topC, botC, t0))
            c.drawRect(0f, h * 0.55f * t0, w.toFloat(), h * 0.55f * t1, p)
        }
        if (night) drawStars(c, w, h, rnd)
        // #49 AURORA NIGHTS -> v0.3.0e SKY PARITY: SkyState owns
        // the rule now (clear winter nights, sparse dates); the
        // drawing is wide smooth color WASHES in the sky band
        // only — green into teal into a breath of violet — with
        // their strength set by the hour (SkyState's intensity,
        // bridged through TownInfo).
        if (night && TownInfo.skySpecial == SkyState.AURORA) {
            val inten = if (TownInfo.auroraIntensity > 0f)
                TownInfo.auroraIntensity else 0.8f
            val t = RenderClock.nowMs() / 900f
            val washCols = listOf("#3ae88a", "#2fd4b8", "#3ab8e8", "#8a6ae8")
            val washBase = listOf(34, 26, 17, 11)
            for (band in washCols.indices) {
                val baseY = h * (0.03f + band * 0.055f)
                val path = Path()
                path.moveTo(0f, baseY + h * 0.075f)
                var x = 0f
                while (x <= w) {
                    path.lineTo(x, baseY + sin(x * 0.008f + t + band * 1.7f) * h * 0.02f)
                    x += w * 0.05f
                }
                for (xx in w downTo 0 step (w / 20).coerceAtLeast(1)) {
                    path.lineTo(xx.toFloat(), baseY + h * 0.075f +
                        sin(xx * 0.008f + t + band * 1.7f) * h * 0.02f)
                }
                path.close()
                c.drawPath(path, Paper.fillPaint(washCols[band],
                    (washBase[band] * inten).toInt().coerceIn(0, 255)))
            }
        }
        // v0.3.0e: METEOR NIGHTS — a few clean streaks, nothing
        // clustered: seeded per scene so they hold still while
        // you look, with a slow shimmer in the tails.
        if (night && TownInfo.skySpecial == SkyState.METEOR) {
            val mr2 = Random(seed + 4242)
            val shimmer = (RenderClock.nowMs() / 700 % 3).toInt()
            repeat(4) { i ->
                val sx = w * (0.12f + mr2.nextFloat() * 0.68f)
                val sy = h * (0.05f + mr2.nextFloat() * 0.20f)
                val len = w * (0.05f + mr2.nextFloat() * 0.05f)
                for (s in 0..2) {
                    val off = s * len * 0.33f
                    val a = (205 - s * 62 - if (i == shimmer) 0 else 45)
                        .coerceIn(30, 255)
                    c.drawLine(sx - off, sy + off * 0.45f,
                        sx - off - len * 0.33f, sy + (off + len * 0.33f) * 0.45f,
                        Paper.strokePaint("#ffffff", 3f - s * 0.7f, a))
                }
            }
        }
        // #44 MOON PHASES: the moon cycles with the in-game months.
        // v0.3.0e: the phase is SkyState's (TownInfo carries it),
        // a full moon throws a halo scaled by its own light, and
        // the HARVEST moon is blood-orange and oversized — up
        // from dusk, squatting low and huge.
        if (night || (tod == "dusk" && TownInfo.harvestMoon)) {
            val harvest = TownInfo.harvestMoon
            val mx = w * 0.82f
            val my = if (tod == "dusk") h * 0.30f else h * 0.11f
            val mr = h * 0.034f * if (harvest) 1.45f else 1f
            val moonCol = if (harvest) "#f08a3a" else "#e9ecf4"
            if (night) {
                // the halo: moonlight made visible, scaled by
                // the phase's own brightness (SkyState.MOONLIGHT)
                c.drawCircle(mx, my, mr * 2.1f, Paper.fillPaint(
                    if (harvest) "#f08a3a" else "#c8d4ee",
                    (26 * TownInfo.moonlight).toInt().coerceIn(0, 255)))
            }
            c.drawCircle(mx, my, mr, Paper.fillPaint(moonCol, 235))
            val phase = TownInfo.moonPhase // 0 new .. 4 full .. 7
            if (phase != 4) {
                val off = (phase - 4) / 4f * mr * 1.6f
                c.drawCircle(mx + off, my, mr * 0.92f,
                    Paper.fillPaint(if (tod == "dusk") Paper.SKY_DUSK_TOP
                        else Paper.SKY_NIGHT_TOP, 235))
            }
        }

        // ---- jagged snowy mountains ----
        // v0.15.0c: the FAR plane parallaxes — the range and
        // the horizon silhouettes ride at PaperPlane.FAR's
        // factor against the camera, so the world slides
        // past them slower than it slides past you.
        c.save()
        c.translate(parallaxShift * (1f - PaperPlane.FAR.parallax), 0f)
        drawMountains(c, w, h, rnd, night)

        // ---- v0.4.0a: the HORIZON layer — the actual
        // neighborhood behind the far range: the graph
        // neighbors' silhouettes on their true bearings, cut
        // by the weather's sight range (Horizon.kt derives,
        // this draws) — behind the ground and the facade, so
        // the parity field always wins the foreground ----
        drawHorizon(c, 0f, h * 0.618f, w.toFloat(), h.toFloat(), loc.id, tod, weatherId)
        c.restore()

        // ---- ground: yard, path, sidewalk (structured) ----
        drawGround(c, w, h, rnd, season, night, weatherId, loc)

        // ---- v0.15.0c: the NEIGHBOR ROW (v0.15.0d: it
        // paints BEFORE the facade now, on its own plane —
        // PaperPlane.BUILDINGS sits behind the facade in
        // the stack, and the row's inner houses share
        // walls with it). A location never stands alone:
        // the baseline's buildings line its street. ----
        c.save()
        c.translate(parallaxShift * (1f - PaperPlane.BUILDINGS.parallax), 0f)
        drawNeighborRow(c, w, h, loc, seed, night, season, weatherId)
        c.restore()

        // ---- location facade ----
        drawFacade(c, w, h, loc, rnd, night, holiday, weatherId)

        // ---- outdoor dressing / pine trees ----
        if (loc.kind == "outdoor" || loc.kind == "hub") {
            drawPines(c, w, h, rnd, night, if (loc.kind == "hub") 3 else 6, loc)
        }
        drawLocationFeature(c, w, h, loc, rnd, night)
        // ---- v0.1.9a Scope B: the parity pass — every location
        // gets its defining details (see LOC_ART_DETAILS) ----
        drawParityDetails(c, w, h, loc, rnd, night, weatherId)

        // ---- v0.3.0a: seasonal exterior dressing (own Random
        // stream, so the layers above keep their exact draws) ----
        drawSeasonDetails(c, w, h, loc, Random(seed + loc.id.hashCode() * 7 + 101),
            season, night, weatherId)

        // ---- v0.4.0f: the DIG layer — the buried case's open
        // sites dress their locations while the arc runs, and
        // one stone stays standing after it (own Random stream;
        // the live token set rides TownInfo from Mystery3) ----
        drawDigDetails(c, w, h, loc, Random(seed + loc.id.hashCode() * 13 + 707),
            night, weatherId)

        // ---- v0.6.0c: the FIX layer — the gardener case's
        // repairs dress their locations from the night they're
        // made and, unlike the dig sites, they never come down
        // (the live token set rides TownInfo from Mystery4) ----
        drawFixDetails(c, w, h, loc, night, weatherId)

        // ---- v0.5.0d: the FACTION MARK layer — a finished
        // chain's permanent dressing at its named location
        // (tokens ride TownInfo from FactionMissions, derived
        // from the chain masks; once standing, forever) ----
        drawFactionMarkDetails(c, w, h, loc, night, weatherId)

        // ---- v0.9.0c: the FAIR layer — for one week a
        // year the fairground dresses: booths, bunting, the
        // wheel on the horizon, the tank (tokens ride
        // TownInfo from Fair; empty map = struck) ----
        drawFairDetails(c, w, h, loc, night, weatherId)

        // ---- v0.11.0e: the CARNIVAL layer — for one week
        // a year the frozen pond dresses: the banner, three
        // ice holes, lantern posts, the sculpture row
        // (tokens ride TownInfo from Carnival) ----
        drawCarnivalDetails(c, w, h, loc, night, weatherId)

        // ---- holiday decorations ----
        if (holiday != null) drawHolidayDecor(c, w, h, loc, holiday, rnd, night)
        // #30 THE HOLDOUT HOUSE: one house keeps the lights up for
        // weeks after everyone else took theirs down.
        else if (TownInfo.holdoutHoliday != null && TownInfo.holdoutLoc == loc.id) {
            drawHolidayDecor(c, w, h, loc, TownInfo.holdoutHoliday!!, rnd, night)
        }

        drawStreetStrip(c, w, h, loc, night, season, weatherId)

        // ---- v0.15.0d: the corner street sign — the
        // baseline's name for the street you're standing
        // on, so walking the town reads spatially. Exit
        // signposts (GameView) name where each edge
        // leads; this one names where you ARE. ----
        val streetSign = TownBaseline.streetSignOf(loc.id)
        if (streetSign != null &&
            PaperDressing.forLocation(loc).zone != PaperZone.NATURE) {
            val sx = w * 0.045f
            val sBase = h * WalkBand.TOP_OUT
            val postP = Paper.fillPaint(if (night) "#3a3f4c" else "#5a4632")
            c.drawRect(sx - 2.5f, sBase - h * 0.075f, sx + 2.5f, sBase, postP)
            // v0.15.0g: the board's width anchors to the
            // unit width (its text size is h-based; an
            // unanchored board would stretch with the
            // widened world into a billboard)
            val signUnitW = SceneScale.unitW(w.toFloat(), loc, false)
            Paper.drawSign(c, sx + signUnitW * 0.055f, sBase - h * 0.088f,
                signUnitW * 0.105f, h * 0.026f, streetSign, h * 0.0115f)
        }

        // ---- v0.3.0a: the LIGHT layer — dusk/night only, drawn
        // last so pools and halos sit over everything ----
        if (SceneLight.lightLevel(timeMin) > 0f) {
            drawLightLayer(c, w, h, loc, tod)
        }

        // ---- v0.16.0d: no grain here anymore — the retired
        // speckle tile is gone whole, and the fibrous frame
        // grain belongs to the diorama post pass (GameView
        // draws it over the finished frame, stage 8) ----
    }

    // ================= v0.15.0c: the street construction =================
    // The overhaul's background-plane painters. Every piece
    // is a flat cut-paper shape: a soft offset shadow under
    // it and a slightly darker cut edge, so the stack reads
    // as layered construction paper.

    /** The neighbor row: PaperDressing's buildings lined
     *  along both flanks of the location's own facade, on
     *  the facade's own baseline — a street of houses (or,
     *  downtown, a wall of taller storefront blocks), never
     *  one lone building. Deterministic per location+seed. */
    // ============================================================
    // v0.16.0e — THE STRUCTURAL RE-DRESS: the dressing
    // vocabulary the facades, storefronts and structures
    // are rebuilt in. Framed windows, dressed doors and
    // true glass fronts, all cut from the v0.16.0d paper
    // stock (PaperCut materials + painted outlines +
    // scissor edges through the shared Paper painters).
    // The flat painters they replace — plain window rects,
    // the door slab, the solid storefront band — are
    // removed whole; nothing in town draws them anymore.
    // ============================================================

    /** A FRAMED WINDOW: trim board around the glass, a
     *  muntin cross, a sill ledge, and a painted outline
     *  whose weight breathes like a marker's. [glassHex]
     *  is the caller's lit/unlit read — the frame is the
     *  same either way, the way a real window's frame
     *  doesn't change when the light comes on. */
    private fun dressedWindow(c: Canvas, x: Float, y: Float,
                                ww: Float, wh: Float,
                                glassHex: String, trimHex: String,
                                seed: Long) {
        val tw = ww * 0.13f
        val ink = "#%06X".format(0xFFFFFF and
            Paper.darker(trimHex, 0.55f))
        // sill first (it sits under the frame's foot)
        c.drawRect(x - ww * 0.07f, y + wh, x + ww * 1.07f,
            y + wh + maxOf(2f, wh * 0.07f), Paper.fillPaint(trimHex))
        // trim board, then its painted outline
        c.drawRect(x - tw, y - tw, x + ww + tw, y + wh + tw,
            Paper.fillPaint(trimHex))
        Paper.paintedOutline(c,
            PaperCut.cutRectContour(x - tw, y - tw, x + ww + tw,
                y + wh + tw, 2f, seed),
            seed, 1.5f, ink)
        // glass, then the muntin cross over it
        c.drawRect(x, y, x + ww, y + wh, Paper.fillPaint(glassHex))
        val bar = maxOf(1.6f, ww * 0.055f)
        c.drawRect(x + ww / 2f - bar / 2f, y, x + ww / 2f + bar / 2f,
            y + wh, Paper.fillPaint(trimHex))
        c.drawRect(x, y + wh / 2f - bar / 2f, x + ww,
            y + wh / 2f + bar / 2f, Paper.fillPaint(trimHex))
    }

    /** A DRESSED DOOR in [dr] — the one rect FacadeGeom
     *  hands out (the painter fills it, GameView's door
     *  pass pulses and hit-tests it, same numbers): a trim
     *  frame, a transom pane over the leaf, two panel
     *  insets, the knob, and a stone step at the foot. */
    private fun dressedDoor(c: Canvas, dr: GeomRect, doorHex: String,
                            trimHex: String, seed: Long) {
        val fw = dr.width * 0.10f
        // the step, on the ground in front of the leaf
        val stepH = maxOf(3f, dr.height * 0.035f)
        c.drawRect(dr.left - dr.width * 0.10f, dr.bottom,
            dr.right + dr.width * 0.10f, dr.bottom + stepH,
            Paper.fillPaint("#8a8a90"))
        // frame, then the leaf inside it
        c.drawRect(dr.left - fw, dr.top - fw, dr.right + fw, dr.bottom,
            Paper.fillPaint(trimHex))
        Paper.paintedOutline(c,
            PaperCut.cutRectContour(dr.left - fw, dr.top - fw,
                dr.right + fw, dr.bottom, 2f, seed),
            seed, 1.6f,
            "#%06X".format(0xFFFFFF and Paper.darker(trimHex, 0.55f)))
        c.drawRect(dr.left, dr.top, dr.right, dr.bottom,
            Paper.fillPaint(doorHex))
        Paper.applyMaterial(c, dr.left, dr.top, dr.right, dr.bottom,
            doorHex, seed + 4L, 0.55f)
        // transom pane + two panel insets, in a darker cut
        val panel = Paper.strokePaint(
            "#%06X".format(0xFFFFFF and Paper.darker(doorHex, 0.62f)), 2f)
        val inset = dr.width * 0.16f
        c.drawRect(dr.left + inset, dr.top + dr.height * 0.06f,
            dr.right - inset, dr.top + dr.height * 0.24f,
            Paper.fillPaint("#bcd6f2", 150))
        c.drawRect(dr.left + inset, dr.top + dr.height * 0.06f,
            dr.right - inset, dr.top + dr.height * 0.24f, panel)
        c.drawRect(dr.left + inset, dr.top + dr.height * 0.32f,
            dr.right - inset, dr.top + dr.height * 0.58f, panel)
        c.drawRect(dr.left + inset, dr.top + dr.height * 0.64f,
            dr.right - inset, dr.bottom - dr.height * 0.06f, panel)
        c.drawCircle(dr.right - dr.width * 0.16f, (dr.top + dr.bottom) / 2f,
            dr.width * 0.055f, Paper.fillPaint("#e8c53a"))
    }

    /** A GLASS FRONT: the storefront the games draw —
     *  a real interior hinted behind real glass. Behind
     *  the panes: a warm back wall, a counter silhouette
     *  and shelf blocks (large shapes, low contrast — a
     *  hint, never a pattern). Over it: the glass wash, a
     *  diagonal sheen, and the mullion grid in the frame
     *  color. At night a lit shop glows from inside (the
     *  interior takes the warmth and the glass thins);
     *  a shut shop pulls its shades over the glass. */
    private fun glassFront(c: Canvas, l: Float, t: Float, r: Float,
                           b: Float, seed: Long, frameHex: String,
                           night: Boolean, lit: Boolean,
                           shut: Boolean = false) {
        val wdt = r - l
        val hgt = b - t
        if (wdt < 12f || hgt < 12f) return
        // the interior hint
        val backHex = if (lit) "#5a4030" else "#33302c"
        c.drawRect(l, t, r, b, Paper.fillPaint(backHex))
        val sil = Paper.fillPaint(if (lit) "#6e5136" else "#3e3a35")
        // counter silhouette along the foot
        c.drawRect(l + wdt * 0.06f, b - hgt * 0.30f, l + wdt * 0.52f, b, sil)
        // two shelf blocks on the back wall — big, sparse
        c.drawRect(l + wdt * 0.58f, t + hgt * 0.16f, l + wdt * 0.94f,
            t + hgt * 0.24f, sil)
        c.drawRect(l + wdt * 0.58f, t + hgt * 0.36f, l + wdt * 0.94f,
            t + hgt * 0.44f, sil)
        c.drawRect(l + wdt * 0.10f, t + hgt * 0.14f, l + wdt * 0.30f,
            t + hgt * 0.46f, sil)
        Paper.applyMaterial(c, l, t, r, b, backHex, seed + 9L, 0.5f)
        // the glass over it, and its sheen
        val glassAlpha = if (lit) 74 else 118
        c.drawRect(l, t, r, b, Paper.fillPaint("#bcd6f2", glassAlpha))
        val sheen = Path().apply {
            moveTo(l + wdt * 0.10f, b)
            lineTo(l + wdt * 0.42f, t)
            lineTo(l + wdt * 0.60f, t)
            lineTo(l + wdt * 0.28f, b)
            close()
        }
        c.drawPath(sheen, Paper.fillPaint("#ffffff", 34))
        // mullions: a transom bar and a seeded run of
        // verticals — the frame the glass sits in
        val frame = Paper.fillPaint(frameHex)
        val barW = maxOf(2.2f, wdt * 0.016f)
        c.drawRect(l, t + hgt * 0.20f - barW / 2f, r,
            t + hgt * 0.20f + barW / 2f, frame)
        val panes = (wdt / 92f).toInt().coerceIn(3, 7)
        for (i in 1 until panes) {
            val mx = l + wdt * i / panes
            c.drawRect(mx - barW / 2f, t, mx + barW / 2f, b, frame)
        }
        // the frame's own painted outline around the whole
        Paper.paintedOutline(c,
            PaperCut.cutRectContour(l, t, r, b, 2f, seed),
            seed, 2f,
            "#%06X".format(0xFFFFFF and Paper.darker(frameHex, 0.55f)))
        if (shut) {
            // shades pulled: a blind band over the upper
            // glass, slat lines over the rest (the #23
            // locked-shop read, kept on the new glass)
            c.drawRect(l, t, r, t + hgt * 0.34f,
                Paper.fillPaint("#20242a", 216))
            val shade = Paper.strokePaint("#101418", 2.5f)
            for (s in 1..3) {
                val sy = t + hgt * (0.34f + s * 0.16f)
                c.drawLine(l, sy, r, sy, shade)
            }
        }
    }

    private fun drawNeighborRow(c: Canvas, w: Int, h: Int, loc: Location,
                                  seed: Long, night: Boolean, season: Season,
                                  weatherId: String) {
        val d = PaperDressing.forLocation(loc)
        if (d.neighborCount <= 0) return
        val baseY = h * FacadeGeom.GROUND_LINE
        val snowy = season == Season.WINTER ||
            weatherId in setOf("light_snow", "heavy_snow", "blizzard")
        val rnd = Random(seed + 777)
        val houseCols = listOf("#c8d8e8", "#e8d8b0", "#d8c4b8",
            "#b8d0b8", "#e0c8d8", "#c2cdd8", "#dcc7a0")
        val roofCols = listOf("#7a4a3a", "#5a5a66", "#6a3a3a",
            "#4a5a6a", "#7a6a4a")
        fun col(hex: String) = if (night) mix(hex, "#232f4a", 0.55f) else hex
        // v0.15.0d: the row is the BASELINE's row — the
        // buildings actually adjacent on this street stand
        // nearest the facade (TownBaseline.rowSequence:
        // west, east, west, east outward), each in its own
        // baseline look; generic row houses only fill in
        // past the street's named buildings. Heights come
        // off the ladder: a neighbor house totals
        // NEIGHBOR_PEAK kid units, downtown blocks BLOCK_PEAK.
        // v0.15.0g: the row FILLS the widened street — the
        // building count scales by the scene's growth, so
        // the baseline's named buildings stand nearest the
        // facade as ever and the row continues past them
        // at the same composed lot width (a 2.5× street
        // carries ~2.5× the buildings, not the same
        // buildings stretched thin).
        val nCount = (d.neighborCount *
            SceneScale.growth(loc, false)).roundToInt()
        val seq = TownBaseline.rowSequence(loc.id, nCount)
        val west = seq.filterIndexed { i, _ -> i % 2 == 0 }
        val east = seq.filterIndexed { i, _ -> i % 2 == 1 }
        // flanks: [leftStart, leftEnd], [rightStart, rightEnd]
        // in world fractions — the facade keeps the center.
        // the flanks reach in under the facade's zone:
        // row houses share walls in a row town, and the
        // facade (painted after this row) covers the seam
        val flanks = listOf(0.006f to 0.40f, 0.60f to 0.994f)
        var idx = 0
        for (side in 0..1) {
            val buildings = if (side == 0) west else east
            val n = buildings.size
            if (n <= 0) continue
            val (fx0, fx1) = flanks[side]
            val slot = (fx1 - fx0) / n
            for (i in 0 until n) {
                idx++
                val bld = buildings[i]
                // v0.16.0a (item 20): on a house street,
                // every neighbor house wears the palette of
                // its ResidentialStreets identity — the same
                // colors its door hit and interior section
                // answer to, so no two houses on the street
                // are the same house twice
                val seqIdx = if (side == 0) i * 2 else i * 2 + 1
                val streetEntry = if (loc.kind == "house")
                    ResidentialStreets.entryFor(loc, seqIdx) else null
                // left flank: adjacent building nearest the
                // facade = the rightmost slot; right flank:
                // nearest = the leftmost slot
                val slotIdx = if (side == 0) (n - 1 - i) else i
                val cx = w * (fx0 + slot * (slotIdx + 0.5f))
                val bw = w * slot * 0.88f
                val jitter = 0.94f + rnd.nextFloat() * 0.12f
                val totalU = if (d.neighborTall) WorldScale.BLOCK_PEAK_U
                    else WorldScale.NEIGHBOR_PEAK_U
                // ladder height, capped by the slot's own
                // aspect so a narrow lot never reads as a
                // tower (portrait screens squeeze the row;
                // the background plane composes a breath
                // smaller than the facade by design)
                val bh = minOf(WorldScale.units(totalU, h.toFloat()) * jitter,
                    bw * if (d.neighborTall) 3.2f else 2.6f)
                val body = col(bld?.bodyHex ?: streetEntry?.bodyHex
                    ?: houseCols[(idx * 3 + loc.id.length) % houseCols.size])
                val roof = col(bld?.roofHex ?: streetEntry?.roofHex
                    ?: roofCols[(idx * 5 + 1) % roofCols.size])
                val left = cx - bw / 2f
                val top = baseY - bh
                // the paper shadow + the cut body
                c.drawRect(left + 4f, top + 5f, left + bw + 4f, baseY + 5f,
                    Paper.fillPaint("#1a2436", 42))
                if (d.neighborTall) {
                    // downtown block: parapet, window rows,
                    // a storefront band with an awning + sign
                    val sfH = bh * 0.24f
                    c.drawRect(left, top, left + bw, baseY - sfH,
                        Paper.fillPaint(body))
                    // v0.16.0d (stages 3+5): paper material +
                    // painted outline on the block's body,
                    // seeded per building — the row inherits
                    // the pipeline through the shared painter
                    val nbSeed = loc.id.hashCode().toLong() * 17L +
                        idx * 101L
                    Paper.applyMaterial(c, left, top, left + bw,
                        baseY - sfH, body, nbSeed)
                    Paper.paintedOutline(c,
                        PaperCut.cutRectContour(left, top, left + bw,
                            baseY - sfH, 3f, nbSeed),
                        nbSeed, 2.4f,
                        "#%06X".format(0xFFFFFF and
                            Paper.darker(body, 0.55f)))
                    c.drawRect(left - 2f, top - h * 0.012f, left + bw + 2f, top,
                        Paper.fillPaint(roof))
                    val rows = 3
                    val colsN = 2
                    val blockTrim = "#%06X".format(0xFFFFFF and
                        Paper.darker(body, 0.6f))
                    for (r in 0 until rows) for (cc in 0 until colsN) {
                        val wwx = left + bw * (0.16f + cc * 0.42f)
                        val wwy = top + bh * (0.07f + r * 0.17f)
                        val lit = night && rnd.nextFloat() < 0.5f
                        // v0.16.0e: framed, like every window
                        // in town now — trim, muntins, sill
                        dressedWindow(c, wwx, wwy, bw * 0.24f, bh * 0.10f,
                            if (lit) "#ffd98a" else col("#8fa8bc"),
                            blockTrim, nbSeed + r * 7L + cc)
                    }
                    val sfTop = baseY - sfH
                    // v0.16.0e: the block's storefront is a
                    // GLASS FRONT now — interior hinted
                    // behind mullioned panes — not the old
                    // solid band of blue-grey slab
                    glassFront(c, left + bw * 0.05f, sfTop,
                        left + bw * 0.95f, baseY, nbSeed + 31L,
                        col("#33302c"), night, lit = night)
                    c.drawRect(left + bw * 0.03f, sfTop - h * 0.030f,
                        left + bw * 0.97f, sfTop, Paper.fillPaint(roof))
                    if (bld != null && bld.sign.isNotEmpty() &&
                        bld.sign.length <= 12) {
                        Paper.drawSign(c, cx, sfTop - h * 0.047f, bw * 0.62f,
                            h * 0.026f, bld.sign, h * 0.012f)
                    } else {
                        c.drawRect(left + bw * 0.22f, sfTop - h * 0.058f,
                            left + bw * 0.78f, sfTop - h * 0.034f,
                            Paper.fillPaint(col(bld?.accentHex ?: "#e8e2d2")))
                    }
                    // the block's door, on the ground line —
                    // dressed, framed, standing in the glass
                    val ddh = minOf(WorldScale.doorPx(h.toFloat()), sfH * 1.6f)
                    dressedDoor(c, GeomRect(cx - ddh * 0.13f, baseY - ddh,
                        cx + ddh * 0.13f, baseY),
                        "#%06X".format(0xFFFFFF and
                            Paper.darker(body, 0.55f)),
                        col("#e8e2d2"), nbSeed + 7L)
                } else {
                    // a house: pitched roof, a door on the
                    // ground line at ladder size, two windows
                    val roofH = bh * 0.30f
                    val bodyTop = top + roofH
                    c.drawRect(left, bodyTop, left + bw, baseY,
                        Paper.fillPaint(body))
                    // v0.16.0d (stages 3+5): paper material +
                    // painted outline on the house body,
                    // seeded per building
                    val nbSeed = loc.id.hashCode().toLong() * 17L +
                        idx * 101L
                    Paper.applyMaterial(c, left, bodyTop, left + bw,
                        baseY, body, nbSeed)
                    Paper.paintedOutline(c,
                        PaperCut.cutRectContour(left, bodyTop,
                            left + bw, baseY, 3f, nbSeed),
                        nbSeed, 2.4f,
                        "#%06X".format(0xFFFFFF and
                            Paper.darker(body, 0.55f)))
                    c.drawRect(left, baseY - 2f, left + bw, baseY,
                        Paper.fillPaint(Paper.darker(body), 120))
                    c.drawPath(Path().apply {
                        moveTo(left - bw * 0.05f, bodyTop + 1f)
                        lineTo(cx, top)
                        lineTo(left + bw * 1.05f, bodyTop + 1f)
                        close()
                    }, Paper.fillPaint(roof))
                    if (snowy) {
                        c.drawPath(Path().apply {
                            moveTo(left + bw * 0.10f, top + roofH * 0.38f)
                            lineTo(cx, top)
                            lineTo(left + bw * 0.90f, top + roofH * 0.38f)
                            lineTo(left + bw * 0.78f, top + roofH * 0.58f)
                            lineTo(cx, top + roofH * 0.26f)
                            lineTo(left + bw * 0.22f, top + roofH * 0.58f)
                            close()
                        }, Paper.fillPaint(if (night) "#5a6e9e" else "#ffffff", 220))
                    }
                    // v0.16.0e: a row house is TWO stories —
                    // an eaves line under the roof, a floor
                    // band across the body, framed windows
                    // on both floors, a dressed door whose
                    // leaf is the very rect its door hit
                    // answers to (centered, ladder height)
                    val bodyH = baseY - bodyTop
                    c.drawRect(left - bw * 0.02f, bodyTop - 3f,
                        left + bw * 1.02f, bodyTop + 1f,
                        Paper.fillPaint("#%06X".format(0xFFFFFF and
                            Paper.darker(roof, 0.8f))))
                    c.drawRect(left, bodyTop + bodyH * 0.46f, left + bw,
                        bodyTop + bodyH * 0.46f + 2.5f,
                        Paper.fillPaint(
                            "#%06X".format(0xFFFFFF and
                                Paper.darker(body, 0.82f)), 120))
                    val ddh = minOf(WorldScale.doorPx(h.toFloat()), bh * 0.62f)
                    dressedDoor(c, GeomRect(cx - ddh * 0.135f, baseY - ddh,
                        cx + ddh * 0.135f, baseY),
                        col(bld?.accentHex ?: streetEntry?.doorHex
                            ?: "#6a4a36"),
                        col("#e8e2d2"), nbSeed + 7L)
                    val winTrim = "#%06X".format(0xFFFFFF and
                        Paper.darker(body, 0.62f))
                    var wi = 0
                    for (rowFrac in listOf(0.10f, 0.56f)) {
                        for (sx in listOf(-0.30f, 0.30f)) {
                            val lit = night && rnd.nextFloat() < 0.55f
                            dressedWindow(c,
                                cx + bw * sx - bw * 0.085f,
                                bodyTop + bodyH * rowFrac,
                                bw * 0.17f, bodyH * 0.24f,
                                if (lit) "#ffd98a" else col("#cfe4f2"),
                                winTrim, nbSeed + 13L + wi)
                            wi++
                        }
                    }
                }
                // v0.16.0e pass 2: a row house's dressing,
                // keyed by the same street + slot identity
                // its door answers to
                if (!d.neighborTall) {
                    fictionDress["house:${loc.id}:$seqIdx"]?.let { fd ->
                        drawFictionDressing(c, fd, left, top, bw,
                            bh, baseY, night, bannerYFrac = 0.36f)
                    }
                }
            }
        }
    }

    /** The street strip along the very bottom of the scene
     *  (Depth's FG_TOP_OUT is its top edge): curb, asphalt,
     *  worn center dashes. Traffic rides on it later, in
     *  the foreground; the crowd never stands on it. */
    private fun drawStreetStrip(c: Canvas, w: Int, h: Int, loc: Location,
                                  night: Boolean, season: Season,
                                  weatherId: String) {
        val d = PaperDressing.forLocation(loc)
        if (!d.hasStreet) return
        val y0 = h * Depth.FG_TOP_OUT
        val snowy = season == Season.WINTER ||
            weatherId in setOf("light_snow", "heavy_snow", "blizzard")
        val asphalt = when {
            night -> "#232a38"
            snowy -> "#59636f"
            else -> "#4a4f57"
        }
        c.drawRect(0f, y0, w.toFloat(), h.toFloat(), Paper.fillPaint(asphalt))
        // BOTH curbs: a pale cut edge where the sidewalk
        // meets the road, and its twin at the road's near
        // edge, where the road runs off the scene's bottom
        val curbP = Paper.fillPaint(if (night) "#3d4657" else "#9aa0a8")
        c.drawRect(0f, y0, w.toFloat(), y0 + h * 0.010f, curbP)
        c.drawRect(0f, h - h * 0.010f, w.toFloat(), h.toFloat(), curbP)
        if (snowy) {
            // plowed snow banks hug both edges of the road
            c.drawRect(0f, y0 + h * 0.010f, w.toFloat(), y0 + h * 0.020f,
                Paper.fillPaint(if (night) "#5a6e9e" else "#ffffff", 190))
            c.drawRect(0f, h - h * 0.022f, w.toFloat(), h - h * 0.012f,
                Paper.fillPaint(if (night) "#5a6e9e" else "#ffffff", 190))
        }
        // v0.16.0d (stage 3): the road is paper too — the
        // material wash under the crossings and dashes
        Paper.applyMaterial(c, 0f, y0, w.toFloat(), h.toFloat(),
            asphalt, loc.id.hashCode().toLong() * 41L + 3L, 0.8f)
        // v0.15.0g: the ZEBRA CROSSINGS — PaperDressing's
        // spans, striped on the asphalt exactly where the
        // tap layer lets feet cross (the same table, so
        // the paint and the permission can never
        // disagree): worn white bars running across the
        // road's full depth at each span
        // detail: crosswalk-zebra
        val zebraP = Paper.fillPaint(
            if (night) "#aab4c0" else "#e8ecf0", if (snowy) 120 else 165)
        for ((x0, x1) in PaperDressing.crosswalks(loc)) {
            val spanL = x0 * w
            val spanW = (x1 - x0) * w
            val bars = 4
            val barW = spanW / (bars * 2 - 1)
            for (b in 0 until bars) {
                val bx = spanL + b * barW * 2f
                c.drawRect(bx, y0 + h * 0.014f, bx + barW,
                    h - h * 0.014f, zebraP)
            }
        }
        // worn center dashes — long flat marks, widely spaced
        // (v0.15.0g: the dash rhythm anchors to the unit
        // width, so the widened road keeps the composed
        // spacing instead of stretching it)
        val unitW = SceneScale.unitW(w.toFloat(), loc, false)
        val dashP = Paper.fillPaint(if (snowy) "#d8dde2" else "#e8c53a", 150)
        val dashW = unitW * 0.045f
        val midY = (y0 + h) / 2f
        var dx = w * 0.02f
        while (dx < w) {
            c.drawRect(dx, midY - h * 0.0035f, dx + dashW, midY + h * 0.0035f, dashP)
            dx += dashW * 2.1f
        }
    }

    /** Our own bus-stop sign, drawn from the style: a post,
     *  a cream board with a blue band, a little drawn bus on
     *  it and the word BUS. A landmark — it reads instantly.
     *  [x] = post center, [baseY] = ground line, [s] = the
     *  board's height in px. */
    fun drawBusStopSign(c: Canvas, x: Float, baseY: Float, s: Float,
                        night: Boolean) {
        fun col(hex: String) = if (night) mix(hex, "#232f4a", 0.45f) else hex
        // post
        c.drawRect(x - s * 0.035f, baseY - s * 1.55f, x + s * 0.035f, baseY,
            Paper.fillPaint(col("#8a8f96")))
        // board: paper shadow, cream face, blue band
        val bw = s * 1.5f
        val bh = s * 1.0f
        val bl = x - bw / 2f
        val bt = baseY - s * 1.55f - bh
        c.drawRoundRect(RectF(bl + 3f, bt + 4f, bl + bw + 3f, bt + bh + 4f),
            s * 0.10f, s * 0.10f, Paper.fillPaint("#1a2436", 42))
        c.drawRoundRect(RectF(bl, bt, bl + bw, bt + bh), s * 0.10f, s * 0.10f,
            Paper.fillPaint(col("#f4ecd8")))
        c.drawRoundRect(RectF(bl, bt, bl + bw, bt + bh * 0.26f), s * 0.10f,
            s * 0.10f, Paper.fillPaint(col("#2a4a8a")))
        // the little bus: body, windshield, wheels
        val busW = bw * 0.52f
        val busH = bh * 0.30f
        val busL = x - busW / 2f
        val busT = bt + bh * 0.36f
        c.drawRoundRect(RectF(busL, busT, busL + busW, busT + busH),
            s * 0.05f, s * 0.05f, Paper.fillPaint(col("#f0ad2e")))
        c.drawRect(busL + busW * 0.08f, busT + busH * 0.12f,
            busL + busW * 0.92f, busT + busH * 0.48f,
            Paper.fillPaint(col("#bcd6f2")))
        val wheelP = Paper.fillPaint(col("#2a2d33"))
        c.drawCircle(busL + busW * 0.24f, busT + busH * 1.02f, s * 0.055f, wheelP)
        c.drawCircle(busL + busW * 0.76f, busT + busH * 1.02f, s * 0.055f, wheelP)
        val tp = Paper.textPaint(col("#1a2a4a"), s * 0.21f, bold = true)
        tp.textAlign = Paint.Align.CENTER
        c.drawText("BUS STOP", x, bt + bh * 0.92f, tp)
    }

    /** One paper bus. [school] paints the school livery
     *  (yellow, stop arm, SCHOOL band); otherwise the town's
     *  public bus (teal + cream). [cx] center, [baseY] the
     *  road line its wheels rest on, [w] its length. */
    fun drawBusVehicle(c: Canvas, cx: Float, baseY: Float, w: Float,
                       night: Boolean, school: Boolean) {
        fun col(hex: String) = if (night) mix(hex, "#232f4a", 0.4f) else hex
        // v0.15.0d: the bus's proportions come off the
        // ladder (BUS_H / BUS_LEN), and it wears its door
        // on its side like a vehicle, not a slab
        val bodyH = w * (WorldScale.BUS_H_U / WorldScale.BUS_LEN_U)
        val left = cx - w / 2f
        val top = baseY - bodyH
        val body = if (school) "#f0ad2e" else "#3a8a9a"
        c.drawRoundRect(RectF(left + 4f, top + 5f, left + w + 4f, baseY + 5f),
            w * 0.03f, w * 0.03f, Paper.fillPaint("#1a2436", 42))
        c.drawRoundRect(RectF(left, top, left + w, baseY), w * 0.03f, w * 0.03f,
            Paper.fillPaint(col(body)))
        // window band with dividers
        val winTop = top + bodyH * 0.14f
        val winBot = top + bodyH * 0.52f
        c.drawRect(left + w * 0.05f, winTop, left + w * 0.95f, winBot,
            Paper.fillPaint(col("#bcd6f2")))
        val divP = Paper.fillPaint(col(body))
        var dx = left + w * 0.17f
        while (dx < left + w * 0.93f) {
            c.drawRect(dx, winTop, dx + w * 0.018f, winBot, divP)
            dx += w * 0.12f
        }
        // livery band + (school) its name band
        c.drawRect(left, top + bodyH * 0.66f, left + w, top + bodyH * 0.78f,
            Paper.fillPaint(col(if (school) "#2a2d33" else "#f4ecd8")))
        if (school) {
            val tp = Paper.textPaint(col("#f4ecd8"), w * 0.052f, bold = true)
            tp.textAlign = Paint.Align.CENTER
            c.drawText("SCHOOL", cx, top + bodyH * 0.755f, tp)
            // the stop arm: a small red octagon by the door
            val ox = left + w * 0.10f
            val oy = top + bodyH * 0.60f
            val orad = w * 0.028f
            val oct = Path()
            for (k in 0..7) {
                val a = Math.toRadians((k * 45 + 22).toDouble())
                val px = ox + orad * Math.cos(a).toFloat()
                val py = oy + orad * Math.sin(a).toFloat()
                if (k == 0) oct.moveTo(px, py) else oct.lineTo(px, py)
            }
            oct.close()
            c.drawPath(oct, Paper.fillPaint(col("#c0392b")))
        }
        // the side door: a panel with a window, up front
        val doorL = left + w * 0.845f
        c.drawRect(doorL, top + bodyH * 0.10f, doorL + w * 0.085f, baseY,
            Paper.fillPaint(Paper.darker(col(body), 0.82f)))
        c.drawRect(doorL + w * 0.012f, top + bodyH * 0.16f,
            doorL + w * 0.073f, top + bodyH * 0.50f,
            Paper.fillPaint(col("#bcd6f2")))
        c.drawLine(doorL + w * 0.0425f, top + bodyH * 0.10f,
            doorL + w * 0.0425f, baseY, Paper.strokePaint(col(body), 2.5f))
        // wheels
        val wheelP = Paper.fillPaint(col("#22252b"))
        val hubP = Paper.fillPaint(col("#9aa0a8"))
        for (wx in listOf(left + w * 0.20f, left + w * 0.80f)) {
            c.drawCircle(wx, baseY - w * 0.012f, w * 0.052f, wheelP)
            c.drawCircle(wx, baseY - w * 0.012f, w * 0.022f, hubP)
        }
        if (night) {
            c.drawCircle(left + w * 0.985f, top + bodyH * 0.60f, w * 0.016f,
                Paper.fillPaint("#ffd98a", 230))
        }
    }

    /** One paper car for the traffic plane: flat body, a
     *  cabin cut-out, two wheels. [facing] flips it. */
    fun drawPaperCar(c: Canvas, cx: Float, baseY: Float, w: Float,
                     colorHex: String, night: Boolean, facing: Int) {
        fun col(hex: String) = if (night) mix(hex, "#232f4a", 0.4f) else hex
        c.save()
        c.translate(cx, 0f)
        if (facing < 0) c.scale(-1f, 1f)
        // v0.15.0d: car proportions off the ladder —
        // total height (cabin + body) is CAR_H of CAR_LEN
        val bodyH = w * (WorldScale.CAR_H_U / WorldScale.CAR_LEN_U) * 0.58f
        val top = baseY - bodyH - w * 0.045f
        c.drawRoundRect(RectF(-w / 2f + 3f, top + 4f, w / 2f + 3f, baseY + 4f),
            w * 0.05f, w * 0.05f, Paper.fillPaint("#1a2436", 42))
        // cabin
        c.drawPath(Path().apply {
            moveTo(-w * 0.26f, top + bodyH * 0.1f)
            lineTo(-w * 0.14f, top - bodyH * 0.72f)
            lineTo(w * 0.16f, top - bodyH * 0.72f)
            lineTo(w * 0.30f, top + bodyH * 0.1f)
            close()
        }, Paper.fillPaint(col(colorHex)))
        c.drawRect(-w * 0.13f, top - bodyH * 0.62f, w * 0.13f, top + bodyH * 0.05f,
            Paper.fillPaint(col("#bcd6f2")))
        // body
        c.drawRoundRect(RectF(-w / 2f, top, w / 2f, baseY - w * 0.03f),
            w * 0.05f, w * 0.05f, Paper.fillPaint(col(colorHex)))
        val wheelP = Paper.fillPaint(col("#22252b"))
        c.drawCircle(-w * 0.30f, baseY - w * 0.035f, w * 0.062f, wheelP)
        c.drawCircle(w * 0.30f, baseY - w * 0.035f, w * 0.062f, wheelP)
        if (night) c.drawCircle(w * 0.47f, top + bodyH * 0.45f, w * 0.02f,
            Paper.fillPaint("#ffd98a", 230))
        c.restore()
    }

    private fun mix(a: String, b: String, t: Float): String {
        val ca = Color.parseColor(a)
        val cb = Color.parseColor(b)
        return String.format("#%02x%02x%02x",
            (Color.red(ca) + (Color.red(cb) - Color.red(ca)) * t).toInt(),
            (Color.green(ca) + (Color.green(cb) - Color.green(ca)) * t).toInt(),
            (Color.blue(ca) + (Color.blue(cb) - Color.blue(ca)) * t).toInt())
    }

    private fun drawStars(c: Canvas, w: Int, h: Int, rnd: Random) {
        val p = Paper.fillPaint("#ffffff")
        repeat(40) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h * 0.4f
            val r = 1f + rnd.nextFloat() * 2f
            p.alpha = 120 + rnd.nextInt(135)
            c.drawCircle(x, y, r, p)
        }
        p.alpha = 255
    }

    private fun drawMountains(c: Canvas, w: Int, h: Int, rnd: Random, night: Boolean) {
        val baseY = h * 0.55f
        for (layer in 0..1) {
            val mColor = if (night) "#3a4a6e" else if (layer == 0) Paper.MOUNTAIN_DARK else Paper.MOUNTAIN
            val path = Path()
            val startY = baseY - h * (0.10f + layer * 0.07f)
            path.moveTo(-20f, baseY)
            var x = -20f
            var peak = startY
            while (x < w + 20) {
                val peakX = x + w * (0.08f + rnd.nextFloat() * 0.10f)
                peak = startY - rnd.nextFloat() * h * 0.08f
                path.lineTo(peakX, peak)
                // snow cap
                x = peakX + w * (0.08f + rnd.nextFloat() * 0.10f)
                path.lineTo(x, startY + rnd.nextFloat() * h * 0.02f)
            }
            path.lineTo(w + 20f, baseY)
            path.close()
            Paper.crooked(c, (rnd.nextFloat() - 0.5f) * 2f, w / 2f, baseY) {
                c.drawPath(path, Paper.fillPaint(mColor))
            }
            // white caps: small triangles at peaks
            val capP = Paper.fillPaint(if (night) "#5a6e9e" else "#ffffff")
            val capPath = Path()
            // (simplified: a second jagged strip near the top)
            capPath.moveTo(-20f, startY + h * 0.02f)
            var cx2 = -20f
            while (cx2 < w + 20) {
                val px = cx2 + w * 0.09f
                capPath.lineTo(px, startY - h * 0.015f - rnd.nextFloat() * h * 0.02f)
                cx2 = px + w * 0.09f
                capPath.lineTo(cx2, startY + h * 0.02f)
            }
            capPath.lineTo(w + 20f, startY + h * 0.05f)
            capPath.lineTo(-20f, startY + h * 0.05f)
            capPath.close()
            c.drawPath(capPath, capP)
        }
    }

    // ============================================================
    // v0.4.0a — the HORIZON layer (scene parity pass #4).
    // Horizon.kt derives WHAT stands on a place's skyline and
    // where; this draws it: flat solid shapes in a band color
    // run through SceneLight's weather wear (day pale
    // blue-grey, dusk cutouts, night near-black), one paint
    // for the whole skyline, then — only when the town's
    // lights are on — the far windows: a neighbor's warmth
    // (LOC_LIGHT) in one window or two, the church's glass,
    // Skeeter's neon smudge, the Tegridy beacon blinking on
    // its true bearing. Occupancy discipline holds at
    // distance too: an empty house and a shut shop stay dark
    // on the horizon, like they do up close.
    //
    // Public (not private) because PhotoView draws its album
    // backdrops through this same function: a photo's skyline
    // is the scene's skyline, by construction.
    // ============================================================
    fun drawHorizon(c: Canvas, x0: Float, baseY: Float, width: Float, unitH: Float,
                    locId: String, band: String, weatherId: String) {
        val placements = Horizon.placementsFor(locId, weatherId)
        if (placements.isEmpty()) return
        val worn = SceneLight.wearHex(Horizon.colorFor(band), weatherId)
        val alpha = when (band) {
            "night" -> 238
            "dusk" -> 218
            else -> 225
        }
        val paint = Paper.fillPaint(worn, alpha)
        for (p in placements) {
            if (p.backdrop) {
                // silhouette: mountain-ridge — on the town's rim
                // the far range steps closer and darker: a near
                // ridge under the generic peaks, seeded by the
                // place so it holds still while you look.
                val rr = Random(locId.hashCode() * 31L + 17L)
                val rh = unitH * Horizon.RIDGE_HEIGHT_FRAC
                val path = Path()
                path.moveTo(x0 - 4f, baseY)
                var x = x0 - 4f
                while (x < x0 + width + 4f) {
                    val px = x + width * (0.09f + rr.nextFloat() * 0.08f)
                    path.lineTo(px, baseY - rh * (0.55f + rr.nextFloat() * 0.45f))
                    x = px + width * (0.09f + rr.nextFloat() * 0.08f)
                    path.lineTo(x, baseY - rh * (0.25f + rr.nextFloat() * 0.25f))
                }
                path.lineTo(x0 + width + 4f, baseY)
                path.close()
                c.drawPath(path, paint)
                continue
            }
            val cxp = x0 + p.xFrac * width
            if (p.token == Horizon.WATER_TOWER_TOKEN) {
                drawWaterTower(c, cxp, baseY, unitH * Horizon.TOWER_HEIGHT_FRAC,
                    worn, alpha, band)
                continue
            }
            drawSilhouette(c, p, cxp, baseY, unitH * Horizon.heightFrac(p.tier),
                paint, band)
        }
    }

    /** // silhouette: water-tower — the town's tower: tank on
     *  legs, cone roof, and at night one steady red aircraft
     *  dot. Drawn from its Horizon placement like any
     *  neighbor; it just isn't a place. */
    private fun drawWaterTower(c: Canvas, cx: Float, baseY: Float, sh: Float,
                               hex: String, alpha: Int, band: String) {
        val paint = Paper.fillPaint(hex, alpha)
        val tankW = sh * 0.52f
        val tankTop = baseY - sh
        val tankBot = baseY - sh * 0.62f
        // legs: two splayed strokes down to the ground line
        val legs = Paper.strokePaint(hex, sh * 0.055f, alpha)
        c.drawLine(cx - tankW * 0.32f, tankBot, cx - tankW * 0.52f, baseY, legs)
        c.drawLine(cx + tankW * 0.32f, tankBot, cx + tankW * 0.52f, baseY, legs)
        c.drawLine(cx - tankW * 0.44f, baseY - sh * 0.18f,
            cx + tankW * 0.44f, baseY - sh * 0.18f, legs)
        // the tank + its cone roof
        c.drawRoundRect(RectF(cx - tankW / 2, tankTop + sh * 0.10f,
            cx + tankW / 2, tankBot), sh * 0.05f, sh * 0.05f, paint)
        c.drawPath(Path().apply {
            moveTo(cx - tankW * 0.58f, tankTop + sh * 0.12f)
            lineTo(cx, tankTop - sh * 0.06f)
            lineTo(cx + tankW * 0.58f, tankTop + sh * 0.12f)
            close()
        }, paint)
        if (band == "night") {
            // one red dot, steady — towers don't blink, they warn
            c.drawCircle(cx, tankTop - sh * 0.075f, 2.6f,
                Paper.fillPaint("#ff4444", 225))
        }
    }

    /** One neighbor's distant shape. The body language is the
     *  close-up facade's, flattened: a block, a roof, and the
     *  one feature you'd recognize from a block away. Every
     *  token of LOC_SILHOUETTES lands here, marked, and the
     *  windows/specials follow in the same paint discipline. */
    private fun drawSilhouette(c: Canvas, p: Horizon.Placement, cx: Float,
                               baseY: Float, sh: Float, paint: Paint, band: String) {
        fun block(sw: Float, bodyFrac: Float) {
            c.drawRect(cx - sw / 2, baseY - sh * bodyFrac, cx + sw / 2, baseY, paint)
        }
        fun roof(sw: Float, bodyFrac: Float, peakFrac: Float) {
            c.drawPath(Path().apply {
                moveTo(cx - sw * 0.56f, baseY - sh * bodyFrac)
                lineTo(cx, baseY - sh * peakFrac)
                lineTo(cx + sw * 0.56f, baseY - sh * bodyFrac)
                close()
            }, paint)
        }
        fun pine(px: Float, ph: Float) {
            c.drawPath(Path().apply {
                moveTo(px - ph * 0.30f, baseY)
                lineTo(px, baseY - ph)
                lineTo(px + ph * 0.30f, baseY)
                close()
            }, paint)
        }
        when (p.token) {
            // silhouette: shack-roofline — low, and the roof sags
            "shack-roofline" -> {
                block(sh * 2.1f, 0.52f)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 1.16f, baseY - sh * 0.52f)
                    quadTo(cx, baseY - sh * 0.60f, cx + sh * 1.16f, baseY - sh * 0.52f)
                    lineTo(cx + sh * 1.16f, baseY - sh * 0.44f)
                    quadTo(cx, baseY - sh * 0.52f, cx - sh * 1.16f, baseY - sh * 0.44f)
                    close()
                }, paint)
            }
            // silhouette: neat-gable — a tidy triangle over a tidy box
            "neat-gable" -> { block(sh * 2.2f, 0.60f); roof(sh * 2.2f, 0.60f, 0.97f) }
            // silhouette: wide-ranch — long, low, shallow slab roof
            "wide-ranch" -> {
                block(sh * 2.7f, 0.50f)
                c.drawRect(cx - sh * 1.48f, baseY - sh * 0.60f, cx + sh * 1.48f,
                    baseY - sh * 0.50f, paint)
            }
            // silhouette: garage-house — the house, plus Randy's half
            "garage-house" -> {
                block(sh * 1.9f, 0.60f); roof(sh * 1.9f, 0.60f, 0.95f)
                c.drawRect(cx + sh * 0.72f, baseY - sh * 0.42f, cx + sh * 1.55f,
                    baseY, paint)
            }
            // silhouette: hedge-house — roof over a hedge-bump base
            "hedge-house" -> {
                block(sh * 2.2f, 0.62f); roof(sh * 2.2f, 0.62f, 0.97f)
                for (i in -2..2) {
                    c.drawCircle(cx + i * sh * 0.42f, baseY - sh * 0.05f,
                        sh * 0.11f, paint)
                }
            }
            // silhouette: new-kid-house — plain gable, one porch nub
            "new-kid-house" -> {
                block(sh * 2.2f, 0.60f); roof(sh * 2.2f, 0.60f, 0.95f)
                c.drawRect(cx - sh * 0.14f, baseY - sh * 0.30f, cx + sh * 0.14f,
                    baseY, paint)
            }
            // silhouette: manor-columns — the big house, pediment and all
            "manor-columns" -> {
                block(sh * 2.4f, 0.68f)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 0.55f, baseY - sh * 0.68f)
                    lineTo(cx, baseY - sh * 0.88f)
                    lineTo(cx + sh * 0.55f, baseY - sh * 0.68f)
                    close()
                }, paint)
                c.drawRect(cx - sh * 1.28f, baseY - sh * 0.74f, cx + sh * 1.28f,
                    baseY - sh * 0.68f, paint)
            }
            // silhouette: bus-sign — just the post and the sign
            "bus-sign" -> {
                c.drawRect(cx - sh * 0.03f, baseY - sh * 0.78f, cx + sh * 0.03f,
                    baseY, paint)
                c.drawRoundRect(RectF(cx - sh * 0.30f, baseY - sh * 0.98f,
                    cx + sh * 0.30f, baseY - sh * 0.74f), sh * 0.05f, sh * 0.05f, paint)
            }
            // silhouette: street-skyline — downtown's attached row
            "street-skyline" -> {
                val hs = listOf(0.55f, 0.78f, 0.64f, 0.88f, 0.58f)
                var lx = cx - sh * 1.55f
                for ((i, hf) in hs.withIndex()) {
                    val bw2 = sh * (0.55f + (i % 3) * 0.08f)
                    c.drawRect(lx, baseY - sh * hf, lx + bw2, baseY, paint)
                    lx += bw2
                }
            }
            // silhouette: cup-roofline — a shop, and its cup on top
            "cup-roofline" -> {
                block(sh * 2.0f, 0.55f)
                c.drawRect(cx + sh * 0.28f, baseY - sh * 0.78f, cx + sh * 0.72f,
                    baseY - sh * 0.55f, paint)
                c.drawRect(cx + sh * 0.38f, baseY - sh * 0.86f, cx + sh * 0.62f,
                    baseY - sh * 0.78f, paint)
            }
            // silhouette: gun-shop-block — a block with a parapet step
            "gun-shop-block" -> {
                block(sh * 2.2f, 0.62f)
                c.drawRect(cx - sh * 0.40f, baseY - sh * 0.74f, cx + sh * 0.40f,
                    baseY - sh * 0.62f, paint)
            }
            // silhouette: pagoda-roof — two eaves stacked over a low shop
            "pagoda-roof" -> {
                block(sh * 1.9f, 0.45f)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 1.12f, baseY - sh * 0.45f)
                    lineTo(cx + sh * 1.12f, baseY - sh * 0.45f)
                    lineTo(cx + sh * 0.80f, baseY - sh * 0.62f)
                    lineTo(cx - sh * 0.80f, baseY - sh * 0.62f)
                    close()
                }, paint)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 0.78f, baseY - sh * 0.62f)
                    lineTo(cx + sh * 0.78f, baseY - sh * 0.62f)
                    lineTo(cx + sh * 0.52f, baseY - sh * 0.78f)
                    lineTo(cx - sh * 0.52f, baseY - sh * 0.78f)
                    close()
                }, paint)
            }
            // silhouette: bar-block — low block, one sign nub on the roof
            "bar-block" -> {
                block(sh * 2.2f, 0.58f)
                c.drawRect(cx - sh * 0.30f, baseY - sh * 0.76f, cx + sh * 0.30f,
                    baseY - sh * 0.58f, paint)
            }
            // silhouette: marquee-tower — the Bijou's tower over its canopy
            "marquee-tower" -> {
                block(sh * 1.9f, 0.52f)
                c.drawRect(cx - sh * 0.24f, baseY - sh * 1.0f, cx + sh * 0.24f,
                    baseY - sh * 0.52f, paint)
                c.drawRect(cx - sh * 0.55f, baseY - sh * 0.60f, cx + sh * 0.55f,
                    baseY - sh * 0.52f, paint)
            }
            // silhouette: clinic-block — a block with a cross on top
            "clinic-block" -> {
                block(sh * 2.1f, 0.60f)
                c.drawRect(cx - sh * 0.06f, baseY - sh * 0.86f, cx + sh * 0.06f,
                    baseY - sh * 0.60f, paint)
                c.drawRect(cx - sh * 0.17f, baseY - sh * 0.79f, cx + sh * 0.17f,
                    baseY - sh * 0.68f, paint)
            }
            // silhouette: bank-pediment — stone block, temple triangle
            "bank-pediment" -> {
                block(sh * 2.3f, 0.60f)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 1.22f, baseY - sh * 0.60f)
                    lineTo(cx, baseY - sh * 0.84f)
                    lineTo(cx + sh * 1.22f, baseY - sh * 0.60f)
                    close()
                }, paint)
            }
            // silhouette: post-block — flat block, flag on a pole
            "post-block" -> {
                block(sh * 2.2f, 0.58f)
                c.drawRect(cx + sh * 0.78f, baseY - sh * 0.92f, cx + sh * 0.84f,
                    baseY - sh * 0.58f, paint)
                c.drawRect(cx + sh * 0.84f, baseY - sh * 0.92f, cx + sh * 1.14f,
                    baseY - sh * 0.78f, paint)
            }
            // silhouette: taco-silhouette — the shell, arched over the shop
            "taco-silhouette" -> {
                block(sh * 2.1f, 0.48f)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 0.62f, baseY - sh * 0.48f)
                    lineTo(cx - sh * 0.62f, baseY - sh * 0.79f)
                    arcTo(RectF(cx - sh * 0.62f, baseY - sh * 1.10f,
                        cx + sh * 0.62f, baseY - sh * 0.48f), 180f, 180f)
                    lineTo(cx + sh * 0.62f, baseY - sh * 0.48f)
                    close()
                }, paint)
            }
            // silhouette: church-steeple — low nave, tall spire, cross
            "church-steeple" -> {
                block(sh * 1.9f, 0.48f)
                c.drawRect(cx - sh * 0.16f, baseY - sh * 0.82f, cx + sh * 0.16f,
                    baseY - sh * 0.48f, paint)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 0.20f, baseY - sh * 0.82f)
                    lineTo(cx, baseY - sh * 1.06f)
                    lineTo(cx + sh * 0.20f, baseY - sh * 0.82f)
                    close()
                }, paint)
                c.drawRect(cx - sh * 0.025f, baseY - sh * 1.20f, cx + sh * 0.025f,
                    baseY - sh * 1.04f, paint)
                c.drawRect(cx - sh * 0.10f, baseY - sh * 1.15f, cx + sh * 0.10f,
                    baseY - sh * 1.10f, paint)
            }
            // silhouette: city-hall-dome — civic block, dome at center
            "city-hall-dome" -> {
                block(sh * 2.2f, 0.56f)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 0.42f, baseY - sh * 0.56f)
                    lineTo(cx - sh * 0.42f, baseY - sh * 0.77f)
                    arcTo(RectF(cx - sh * 0.42f, baseY - sh * 0.98f,
                        cx + sh * 0.42f, baseY - sh * 0.56f), 180f, 180f)
                    lineTo(cx + sh * 0.42f, baseY - sh * 0.56f)
                    close()
                }, paint)
                c.drawRect(cx - sh * 0.03f, baseY - sh * 1.04f, cx + sh * 0.03f,
                    baseY - sh * 0.94f, paint)
            }
            // silhouette: school-block — the long brick block, band raised
            "school-block" -> {
                block(sh * 2.7f, 0.54f)
                c.drawRect(cx - sh * 0.55f, baseY - sh * 0.66f, cx + sh * 0.55f,
                    baseY - sh * 0.54f, paint)
            }
            // silhouette: hoop-and-trees — court trees and one hoop
            "hoop-and-trees" -> {
                pine(cx - sh * 0.95f, sh * 0.88f)
                pine(cx - sh * 0.45f, sh * 0.68f)
                pine(cx + sh * 0.85f, sh * 0.80f)
                c.drawRect(cx + sh * 0.22f, baseY - sh * 0.62f, cx + sh * 0.28f,
                    baseY, paint)
                c.drawRect(cx + sh * 0.05f, baseY - sh * 0.78f, cx + sh * 0.42f,
                    baseY - sh * 0.60f, paint)
            }
            // silhouette: pond-treeline — the shore's dark tree wall
            "pond-treeline" -> {
                c.drawRect(cx - sh * 1.45f, baseY - sh * 0.14f, cx + sh * 1.45f,
                    baseY, paint)
                val hs = listOf(0.95f, 0.70f, 1.0f, 0.78f, 0.92f, 0.66f, 0.88f)
                for ((i, hf) in hs.withIndex()) {
                    pine(cx - sh * 1.25f + i * sh * 0.42f, sh * hf)
                }
            }
            // silhouette: mall-pylon — low mall slab, the sign tower
            "mall-pylon" -> {
                block(sh * 2.5f, 0.40f)
                c.drawRect(cx - sh * 0.95f, baseY - sh * 0.92f, cx - sh * 0.83f,
                    baseY, paint)
                c.drawRoundRect(RectF(cx - sh * 1.14f, baseY - sh * 1.06f,
                    cx - sh * 0.64f, baseY - sh * 0.80f), sh * 0.04f, sh * 0.04f, paint)
            }
            // silhouette: loft-boxes — SoDoSoPa's stepped lofts
            "loft-boxes" -> {
                c.drawRect(cx - sh * 1.15f, baseY - sh * 0.52f, cx - sh * 0.38f,
                    baseY, paint)
                c.drawRect(cx - sh * 0.38f, baseY - sh * 0.78f, cx + sh * 0.38f,
                    baseY, paint)
                c.drawRect(cx + sh * 0.38f, baseY - sh * 0.62f, cx + sh * 1.15f,
                    baseY, paint)
            }
            // silhouette: farm-silo — the barn, and the silo over it
            "farm-silo" -> {
                c.drawRect(cx - sh * 0.95f, baseY - sh * 0.44f, cx + sh * 0.18f,
                    baseY, paint)
                c.drawPath(Path().apply {
                    moveTo(cx - sh * 1.02f, baseY - sh * 0.44f)
                    lineTo(cx - sh * 0.38f, baseY - sh * 0.62f)
                    lineTo(cx + sh * 0.25f, baseY - sh * 0.44f)
                    close()
                }, paint)
                val sx = cx + sh * 0.52f
                c.drawRect(sx - sh * 0.17f, baseY - sh * 0.94f, sx + sh * 0.17f,
                    baseY, paint)
                c.drawPath(Path().apply {
                    moveTo(sx - sh * 0.17f, baseY - sh * 0.94f)
                    arcTo(RectF(sx - sh * 0.17f, baseY - sh * 1.10f,
                        sx + sh * 0.17f, baseY - sh * 0.78f), 180f, 180f)
                    close()
                }, paint)
            }
            // silhouette: raisins-block — block, brand band, wing fin
            "raisins-block" -> {
                block(sh * 2.2f, 0.56f)
                c.drawRect(cx - sh * 1.16f, baseY - sh * 0.64f, cx + sh * 1.16f,
                    baseY - sh * 0.56f, paint)
                c.drawPath(Path().apply {
                    moveTo(cx + sh * 0.55f, baseY - sh * 0.64f)
                    lineTo(cx + sh * 0.95f, baseY - sh * 0.84f)
                    lineTo(cx + sh * 1.02f, baseY - sh * 0.64f)
                    close()
                }, paint)
            }
            else -> block(sh * 2.0f, 0.55f)
        }

        // ---- the far windows & specials: dusk/night only ----
        if (band == "day") return
        val target = p.targetId ?: return
        val lv = SceneLight.levelForBand(band)
        fun wa(base: Int): Int = (base * lv).toInt().coerceIn(0, 255)
        when (target) {
            "Church" -> {
                // the glass, burning from inside, visible a
                // town away — one tall arch in the church's own
                // warmth, the same arch pass #3 draws up close
                val warmth = SceneLight.warmthOf(target)
                val gw = sh * 0.13f
                val gy2 = baseY - sh * 0.50f
                c.drawPath(Path().apply {
                    moveTo(cx - gw, gy2)
                    lineTo(cx - gw, gy2 - sh * 0.20f)
                    quadTo(cx, gy2 - sh * 0.36f, cx + gw, gy2 - sh * 0.20f)
                    lineTo(cx + gw, gy2)
                    close()
                }, Paper.fillPaint(warmth, wa(120)))
            }
            "SkeetersBar" -> {
                // the neon, a pink smudge on the skyline
                val sg = SceneLight.signGlowOf(target) ?: "#ff5a8a"
                c.drawOval(RectF(cx - sh * 0.34f, baseY - sh * 0.80f,
                    cx + sh * 0.34f, baseY - sh * 0.52f),
                    Paper.fillPaint(sg, wa(52)))
            }
            "TegridyFarms" -> {
                // the silo beacon, blinking on its true bearing
                if (band == "night" &&
                    (RenderClock.nowMs() / 800) % 2L == 0L) {
                    val sx = cx + sh * 0.52f
                    val sy = baseY - sh * 1.02f
                    c.drawOval(RectF(sx - sh * 0.09f, sy - sh * 0.09f,
                        sx + sh * 0.09f, sy + sh * 0.09f),
                        Paper.fillPaint("#ff4444", wa(55)))
                    c.drawCircle(sx, sy, sh * 0.028f,
                        Paper.fillPaint("#ff4444", wa(235)))
                }
            }
            else -> {
                // sparse far windows — one, two for a near
                // neighbor — in the place's own warmth, and
                // only while the place is awake: the occupancy
                // rules of the facade, honored at distance.
                val tLoc = LOCATIONS[target] ?: return
                val dark = (tLoc.kind == "house" && target !in TownInfo.occupiedHomes) ||
                    target in TownInfo.closedShops
                if (dark) return
                if (p.token == "pond-treeline" || p.token == "hoop-and-trees" ||
                    p.token == "bus-sign") return
                val warmth = SceneLight.warmthOf(target)
                val wp = Paper.fillPaint(warmth, wa(165))
                val ws = sh * 0.075f
                val wy = baseY - sh * 0.30f
                if (p.tier == Horizon.TIER_NEAR) {
                    c.drawRect(cx - sh * 0.30f, wy, cx - sh * 0.30f + ws, wy + ws, wp)
                    c.drawRect(cx + sh * 0.30f - ws, wy, cx + sh * 0.30f, wy + ws, wp)
                } else {
                    c.drawRect(cx - ws / 2, wy, cx + ws / 2, wy + ws, wp)
                }
            }
        }
    }

    private fun drawGround(c: Canvas, w: Int, h: Int, rnd: Random, season: Season,
                           night: Boolean, weatherId: String, loc: Location) {
        val top = h * 0.62f
        // v0.3.0a weather wear: the ground's colors run through
        // SceneLight's measured transforms (rain grays the snow,
        // fog mutes the grass) — night keeps its stylized blues.
        fun worn(hex: String): String =
            if (night) hex else SceneLight.wearHex(hex, weatherId)
        val snowy = season == Season.WINTER ||
            weatherId in setOf("light_snow", "heavy_snow", "blizzard")
        val dressing = PaperDressing.forLocation(loc)
        // ---- v0.15.0d: STRUCTURED ground. The v0.15.0c
        // ground was a blank field scattered with speck
        // dabs; the correction builds the ground out of
        // real pieces — a yard band in front of the
        // buildings, a packed path from the door, sidewalk
        // slabs along the street, and snow (or grass) laid
        // as COVER with edges and drifts, never confetti.
        val baseCol = when {
            night -> "#2c3a5e"
            season == Season.SUMMER -> worn("#69a869")
            else -> worn(Paper.SNOW)
        }
        c.drawRect(0f, top, w.toFloat(), h.toFloat(), Paper.fillPaint(baseCol))
        val groundLine = h * FacadeGeom.GROUND_LINE
        val walkTop = h * WalkBand.TOP_OUT
        val walkBot = h * WalkBand.BOTTOM_OUT
        // v0.15.0g: widths that were composed against the
        // old world width (stripe rhythm, drift spans,
        // path width, seam spacing) anchor to the unit
        // width in the widened world; positions stay in
        // the world itself
        val unitW = SceneScale.unitW(w.toFloat(), loc, false)
        // the yard band: the ground the crowd stands on,
        // a breath deeper than the field behind it
        val yardCol = when {
            night -> "#33436a"
            season == Season.SUMMER -> worn("#5d9c5d")
            else -> worn("#e8eef5")
        }
        c.drawRect(0f, groundLine, w.toFloat(), walkBot + h * 0.008f,
            Paper.fillPaint(yardCol))
        if (season == Season.SUMMER) {
            // mow stripes: wide alternating bands, the
            // way a lawn actually reads from the street
            val stripe = Paper.fillPaint("#ffffff", 16)
            var sx = 0f
            var k = 0
            while (sx < w) {
                if (k % 2 == 0) c.drawRect(sx, groundLine, sx + unitW * 0.09f,
                    walkBot + h * 0.008f, stripe)
                sx += unitW * 0.09f
                k++
            }
        }
        // the packed path: from the facade's (or the
        // structure's) door down to the sidewalk
        val look = TownBaseline.of(loc.id)
        val doorCx = if (loc.kind == "outdoor" || loc.kind == "hub")
            FacadeGeom.structureDoorRect(look?.structure ?: "hut",
                w.toFloat(), h.toFloat(), unitW).cx
            else FacadeGeom.doorRect(loc.kind, w.toFloat(), h.toFloat(), unitW).cx
        val pathCol = when {
            night -> "#3d4c74"
            season == Season.SUMMER -> worn("#a89372")
            else -> worn("#d7dfe9")
        }
        val pw = unitW * 0.055f
        c.drawPath(Path().apply {
            moveTo(doorCx - pw * 0.8f, groundLine)
            lineTo(doorCx + pw * 0.8f, groundLine)
            lineTo(doorCx + pw, walkBot + h * 0.008f)
            lineTo(doorCx - pw, walkBot + h * 0.008f)
            close()
        }, Paper.fillPaint(pathCol))
        // #35 MUD SEASON: after melt days the path goes
        // brown — three honest patches ON the path, not
        // a scatter
        if (TownInfo.mudToday && season == Season.SPRING) {
            val mud = Paper.fillPaint(if (night) "#3a2c1c" else "#7a5a36", 170)
            for (i in 0..2) {
                val my = groundLine + (walkBot - groundLine) * (0.2f + i * 0.3f)
                c.drawOval(RectF(doorCx - pw * 0.75f, my - h * 0.008f,
                    doorCx + pw * 0.75f, my + h * 0.008f), mud)
            }
        }
        // snow as COVER: drift edges banked along the
        // building line and spilling at the yard's edges
        if (snowy) {
            val drift = Paper.fillPaint(if (night) "#3a4a75" else worn("#ffffff"))
            repeat(4) {
                val x = rnd.nextFloat() * w * 0.9f
                val dw = unitW * (0.22f + rnd.nextFloat() * 0.18f)
                c.drawOval(RectF(x, groundLine - h * 0.016f, x + dw,
                    groundLine + h * 0.020f), drift)
            }
            repeat(3) {
                val x = rnd.nextFloat() * w * 0.9f
                val dw = unitW * (0.16f + rnd.nextFloat() * 0.14f)
                c.drawOval(RectF(x, walkBot - h * 0.012f, x + dw,
                    walkBot + h * 0.014f), drift)
            }
        }
        if (season == Season.FALL) {
            // leaf drifts: raked piles banked at the
            // building line, leaf-colored — v0.15.0g: the
            // piles repeat along the widened street (one
            // per growth segment, the same three-bank
            // rhythm per composed width) instead of three
            // lonely piles stretched across it
            val leaf = Paper.fillPaint(if (night) "#8a5a24" else worn("#c97a2c"))
            val segs = kotlin.math.ceil(
                SceneScale.growth(loc, false).toDouble()).toInt()
            repeat(3 * segs) {
                val x = w * (0.08f + (it % 3) * 0.34f) / segs +
                    w * (it / 3) / segs + rnd.nextFloat() * unitW * 0.06f
                val dw = unitW * (0.13f + rnd.nextFloat() * 0.06f)
                c.drawOval(RectF(x, groundLine - h * 0.010f, x + dw,
                    groundLine + h * 0.016f), leaf)
            }
        }
        // the sidewalk: pale slabs along the street, with
        // seams — the band the crowd's front row stands on,
        // ending at the road's top edge (its curb)
        if (dressing.hasStreet) {
            val slabTop = walkBot - h * 0.012f
            val slabBot = h * Depth.FG_TOP_OUT
            c.drawRect(0f, slabTop, w.toFloat(), slabBot,
                Paper.fillPaint(if (night) "#3d4657" else worn("#b9bec6")))
            val seam = Paper.strokePaint(if (night) "#2c3444" else "#9aa0a8", 2f)
            var sx = w * 0.03f
            while (sx < w) {
                c.drawLine(sx, slabTop, sx, slabBot, seam)
                sx += unitW * 0.085f
            }
            c.drawLine(0f, slabTop, w.toFloat(), slabTop, seam)
        }
        // ---- v0.16.0d (stage 3): the ground itself is paper —
        // the procedural material (fibre + mottle, PaperCut)
        // washed over the whole ground plane: field, yard,
        // path and slabs alike, one stock. The street strip
        // paints its own material over the bottom below. ----
        Paper.applyMaterial(c, 0f, top, w.toFloat(), h.toFloat(),
            baseCol, loc.id.hashCode().toLong() * 31L + 5L)
    }

    private fun drawPines(c: Canvas, w: Int, h: Int, rnd: Random, night: Boolean, count: Int,
                          loc: Location) {
        val groundTop = h * 0.62f
        // v0.16.0b: the location's own column stays clear at
        // the ground line — a ground-plane pine planted in
        // the facade/structure's lot stands in its doorway
        // (and shows through the bus shelter's glass as if
        // planted inside it). Same rule GameView's standing
        // props now keep. Pines behind the ground line still
        // peek over the roofs.
        val unitW = SceneScale.unitW(w.toFloat(), loc, false)
        val foot = if (loc.kind in CharacterSystem.BUILDING_KINDS)
            FacadeGeom.facadeRect(loc.kind, w.toFloat(), h.toFloat(), unitW)
            else FacadeGeom.structureRect(
                TownBaseline.of(loc.id)?.structure ?: "hut",
                w.toFloat(), h.toFloat(), unitW)
        val footMargin = WorldScale.units(0.30f, h.toFloat())
        repeat(count) { i ->
            val x = w * (0.06f + 0.88f * i / count) + (rnd.nextFloat() - 0.5f) * 30
            val y = groundTop + 10 + rnd.nextFloat() * (h - groundTop - 60)
            // the pine layer paints AFTER the facade, so a pine
            // rooted in the facade's lower band drapes its crown
            // over the front — and the door — even when its base
            // sits a few px behind the ground line (the Tegridy
            // barn pine). Rooted high behind the roof, it only
            // peeks over the top: that one stays.
            if (x > foot.left - footMargin && x < foot.right + footMargin &&
                y > foot.top + foot.height * 0.35f) return@repeat
            // v0.15.0d: pines at WORLD SCALE — a mature
            // pine is PINE_U kid heights at the street
            // edge, composing smaller toward the back of
            // the ground plane (depth), never the 46px
            // shrubs the old pass planted
            val depth = ((y - groundTop) / (h - groundTop)).coerceIn(0f, 1f)
            val s = 0.55f + 0.45f * depth
            val pineH = WorldScale.units(WorldScale.PINE_U, h.toFloat()) *
                s * (0.9f + rnd.nextFloat() * 0.2f)
            drawPine(c, x, y, pineH, night)
        }
    }

    fun drawPine(c: Canvas, x: Float, y: Float, h: Float, night: Boolean) {
        val green = if (night) "#1f3a32" else Paper.PINE
        val dark = if (night) "#16291f" else Paper.PINE_DARK
        // trunk
        c.drawRect(x - h * 0.05f, y - h * 0.25f, x + h * 0.05f, y, Paper.fillPaint("#5d3f24"))
        // 3 stacked triangles, crooked
        Paper.crooked(c, -2f, x, y - h * 0.5f) {
            for (i in 0..2) {
                val ty = y - h * 0.25f - i * h * 0.24f
                val tw = h * (0.42f - i * 0.10f)
                c.drawPath(Path().apply {
                    moveTo(x - tw, ty); lineTo(x + tw, ty); lineTo(x, ty - h * 0.30f); close()
                }, Paper.fillPaint(if (i % 2 == 0) green else dark))
            }
            // snow dusting
            c.drawPath(Path().apply {
                val ty = y - h * 0.25f - 2 * h * 0.24f
                moveTo(x - h * 0.14f, ty - h * 0.10f)
                lineTo(x + h * 0.14f, ty - h * 0.10f)
                lineTo(x, ty - h * 0.28f); close()
            }, Paper.fillPaint(if (night) "#5a6e9e" else "#ffffff", 200))
            // #80 BUD WATCH: early spring tips the branches green,
            // a little more every day until the leaves pop
            if (TownInfo.budLevel > 0f) {
                val buds = (TownInfo.budLevel * 7).toInt()
                val bp = Paper.fillPaint("#7ac74a", 220)
                val br = Random((x * 10).toInt().toLong())
                repeat(buds) {
                    val bx = x + (br.nextFloat() - 0.5f) * h * 0.5f
                    val by = y - h * (0.35f + br.nextFloat() * 0.55f)
                    c.drawCircle(bx, by, 3.2f, bp)
                }
            }
        }
    }

    private fun drawFacade(c: Canvas, w: Int, h: Int, loc: Location, rnd: Random,
                           night: Boolean, holiday: Holiday?, weatherId: String) {
        val cx = w / 2f
        val baseY = h * FacadeGeom.GROUND_LINE
        // v0.15.0d: ONE geometry (FacadeGeom) and ONE look
        // (the merged-map baseline) — the facade stands at
        // world scale now: eaves at ~1.95 kid heights, the
        // door at 1.22 × an adult, both read off the same
        // table GameView's door pass hit-tests.
        val look = TownBaseline.of(loc.id)
        // v0.15.0g: the facade composes against the unit
        // width — its composed size at the widened
        // world's heart, the same rect GameView's door
        // pass reads (it computes the same unitW)
        val unitW = SceneScale.unitW(w.toFloat(), loc, false)
        val bw: Float
        val bh: Float
        if (loc.kind == "outdoor" || loc.kind == "hub") {
            // a drawn STRUCTURE fronts the street — shelter,
            // pavilion, hut, barn, loft or the diner front —
            // with its own door on the ground line. No
            // free-standing door slab, ever.
            val structure = look?.structure ?: "hut"
            drawStructure(c, w, h, loc, structure, night)
            val sr = FacadeGeom.structureRect(structure, w.toFloat(), h.toFloat(), unitW)
            // v0.16.0b: the name sign sits ON the structure's
            // front, under its roofline — it used to float a
            // sign-height ABOVE the roof, unattached to anything
            // (Lucky's playtest (b): the "Main Street" / "South
            // Park Bus Stop" plaques hovering over the scene)
            Paper.drawSign(c, sr.cx, sr.top + sr.height * 0.10f,
                minOf(unitW * 0.62f, sr.width * 0.92f),
                h * 0.045f, loc.name, h * 0.020f)
            return
        }
        val fr = FacadeGeom.facadeRect(loc.kind, w.toFloat(), h.toFloat(), unitW)
        bw = fr.width
        bh = fr.height
        // v0.3.0a: the body's base color runs through weather
        // wear first, then the night mix — worn, then darkened.
        // v0.15.0d: the color itself is the baseline look's.
        val wornBody = SceneLight.wearHex(look?.bodyHex ?: loc.colorHex, weatherId)
        val bodyC = if (night) mix(wornBody, "#1a2340", 0.45f) else wornBody
        val body = Paper.fillPaint(bodyC)
        val left = cx - bw / 2
        val right = cx + bw / 2
        val top = baseY - bh
        Paper.crooked(c, (rnd.nextFloat() - 0.5f) * 2.4f, cx, baseY - bh / 2) {
            // shadow
            c.drawRect(left + 4, top + 5, right + 4, baseY + 5, Paper.fillPaint("#000000", 70))
            // main block
            c.drawRect(left, top, right, baseY, body)
            // v0.16.0d (stages 3+4+5): the body wears the
            // procedural paper material, and its edge is a
            // PAINTED outline — a seeded ribbon whose weight
            // breathes along its length like a marker's, the
            // silhouette cut just inside its line. The old
            // uniform-width edge stroke is removed whole.
            val facadeSeed = loc.id.hashCode().toLong() * 131L + 7L
            Paper.applyMaterial(c, left, top, right, baseY,
                bodyC, facadeSeed)
            Paper.paintedOutline(c,
                PaperCut.cutRectContour(left, top, right, baseY,
                    3f, facadeSeed),
                facadeSeed, 2.8f,
                "#%06X".format(0xFFFFFF and
                    Paper.darker(bodyC, 0.55f)))
            // roof per kind (v0.15.0d: the baseline look's roof)
            val roofHexStr = look?.roofHex ?: mix(loc.colorHex, "#000000", 0.40f)
            val roofC = Paper.fillPaint(
                if (night) mix(roofHexStr, "#1a2340", 0.45f) else roofHexStr)
            when (loc.kind) {
                "house", "civic" -> c.drawPath(Path().apply {
                    moveTo(left - bw * 0.06f, top)
                    lineTo(cx, top - bh * 0.55f)
                    lineTo(right + bw * 0.06f, top)
                    close()
                }, roofC)
                else -> c.drawRect(left - bw * 0.03f, top - bh * 0.12f,
                    right + bw * 0.03f, top, roofC) // flat shop roof / parapet
            }
            // v0.16.0e: the ROOFLINE is dressed — an eaves
            // board runs the wall head under the roof's
            // overhang (gable kinds), a fascia trim caps the
            // flat roofs, and every two-story kind wears the
            // FLOOR BAND where its upper floor ends: the
            // games' houses read as two stories because the
            // front SAYS so, in trim, not just in height.
            val eavesHex = "#%06X".format(0xFFFFFF and
                Paper.darker(roofHexStr, 0.78f))
            when (loc.kind) {
                "house", "civic" -> c.drawRect(left - bw * 0.06f, top - 2f,
                    right + bw * 0.06f, top + 3.5f, Paper.fillPaint(eavesHex))
                "shop", "school" -> c.drawRect(left - bw * 0.03f, top,
                    right + bw * 0.03f, top + 3.5f, Paper.fillPaint(eavesHex))
            }
            if (loc.kind == "house" || loc.kind == "civic" ||
                loc.kind == "school") {
                c.drawRect(left, top + bh * 0.47f, right,
                    top + bh * 0.47f + 2.5f,
                    Paper.fillPaint("#%06X".format(0xFFFFFF and
                        Paper.darker(bodyC, 0.80f)), 110))
            }
            // a gable wears its attic vent at the peak
            if (loc.kind == "house") {
                val vx = cx
                val vy = top - bh * 0.27f
                val vr = bh * 0.052f
                c.drawCircle(vx, vy, vr, Paper.fillPaint("#e8e2d2"))
                c.drawCircle(vx, vy, vr * 0.68f,
                    Paper.fillPaint("#2a3a4a"))
            }
            // #2 CHIMNEY SMOKE SIGNALS: occupied houses breathe;
            // cooking hours breathe THICK. Empty houses are cold.
            if (loc.kind == "house") {
                val chX = left + bw * 0.16f
                val chTop = top - bh * 0.52f
                c.drawRect(chX, chTop, chX + bw * 0.07f, top - bh * 0.18f,
                    Paper.fillPaint(Paper.darker(loc.colorHex, 0.5f)))
                if (loc.id in TownInfo.occupiedHomes) {
                    val tm = TownInfo.timeMin
                    val cooking = (tm in 7 * 60 until 9 * 60) ||
                        (tm in 17 * 60 until 19 * 60)
                    val puffs = if (cooking) 3 else 2
                    val t = RenderClock.nowMs() / 1100f
                    for (i in 0 until puffs) {
                        val ph = (t + i * 0.9f) % 2.4f
                        val pr = bh * (0.05f + ph * 0.045f) * (if (cooking) 1.35f else 1f)
                        val alpha = (150 - ph * 55).toInt().coerceIn(25, 150)
                        c.drawCircle(chX + bw * 0.035f + ph * bw * 0.05f,
                            chTop - ph * bh * 0.22f, pr,
                            Paper.fillPaint(if (night) "#8a94b8" else "#e8e8e8", alpha))
                    }
                }
            }
            // door — v0.15.0d: ON the facade, base on the
            // ground line, sized to the figures who use it
            // (FacadeGeom's one rect: the painter fills it,
            // GameView's door pass pulses + hit-tests it).
            // v0.16.0e: the leaf is DRESSED now — frame,
            // transom, panels, step — in that same rect.
            val dr = FacadeGeom.doorRect(loc.kind, w.toFloat(), h.toFloat(), unitW)
            val doorHex = "#%06X".format(0xFFFFFF and Paper.darker(
                look?.accentHex ?: loc.colorHex, 0.75f))
            val trimHex = if (night) mix("#e8e2d2", "#1a2340", 0.45f)
                else "#e8e2d2"
            // ---- lit by OCCUPANCY, not the clock alone ----
            // #21 houses light up as residents get home (occupied);
            // #41 after bedtime the windows die one by one;
            // #45 one window may flicker TV-blue past bedtime;
            // #24 an empty house stays dark, porch light included;
            // #23 a locked shop pulls its shades (on the new glass).
            // v0.3.0a: lit windows burn in the location's own
            // warmth (LOC_LIGHT) instead of one town-wide amber.
            val warmth = SceneLight.warmthOf(loc.id)
            val tmNow = TownInfo.timeMin
            val isHouse = loc.kind == "house"
            val occupied = loc.id in TownInfo.occupiedHomes
            val shut = loc.id in TownInfo.closedShops
            val bedtime = TownInfo.bedtimeOf[loc.id]
            fun windowLit(i: Int): Boolean {
                if (shut) return false
                if (!night && tmNow < 17 * 60) return false
                if (isHouse) {
                    if (!occupied) return false
                    if (bedtime != null && tmNow > bedtime) {
                        if (loc.id in TownInfo.tvHouses && i == 1) return true
                        val alive = 2 - (tmNow - bedtime) / 12
                        return i < alive.coerceAtLeast(0)
                    }
                    return true
                }
                return night || tmNow >= 17 * 60
            }
            if (loc.kind == "shop") {
                // ---- v0.16.0e: THE GLASS-FRONT SHOP ----
                // A fascia band carries the baseline's sign;
                // the striped awning rides under it; below
                // that the whole front is glass — interior
                // hinted behind mullioned panes — with the
                // dressed door standing in it at the one
                // rect the door pass hit-tests. The old
                // front (an awning over two flat windows and
                // a slab door) is removed whole.
                val fasciaHex = "#%06X".format(0xFFFFFF and
                    Paper.darker(look?.accentHex ?: bodyC, 0.72f))
                c.drawRect(left, top + bh * 0.015f, right, top + bh * 0.21f,
                    Paper.fillPaint(fasciaHex))
                Paper.applyMaterial(c, left, top + bh * 0.015f, right,
                    top + bh * 0.21f, fasciaHex, facadeSeed + 17L, 0.6f)
                if (look != null && look.sign.isNotEmpty()) {
                    Paper.drawSign(c, cx, top + bh * 0.115f, bw * 0.72f,
                        h * 0.036f, look.sign, h * 0.0165f)
                }
                val awnW = bw / 8
                for (i in 0 until 8) {
                    val ax = left + i * awnW
                    c.drawRect(ax, top + bh * 0.21f, ax + awnW, top + bh * 0.315f,
                        Paper.fillPaint(if (i % 2 == 0) "#f0e8d0" else Paper.RED))
                }
                val shopLit = !shut && (night || tmNow >= 17 * 60)
                glassFront(c, left + bw * 0.045f, top + bh * 0.315f,
                    right - bw * 0.045f, baseY, facadeSeed + 21L,
                    "#%06X".format(0xFFFFFF and Paper.darker(bodyC, 0.45f)),
                    night, lit = shopLit, shut = shut)
                dressedDoor(c, dr, doorHex, trimHex, facadeSeed + 3L)
            } else {
                dressedDoor(c, dr, doorHex, trimHex, facadeSeed + 3L)
                // the baseline's facade sign, on the front for
                // every building that carries one in town
                if (look != null && look.sign.isNotEmpty() && loc.kind != "house") {
                    Paper.drawSign(c, cx, top + bh * 0.13f, bw * 0.72f,
                        h * 0.036f, look.sign, h * 0.0165f)
                }
                // ---- v0.16.0e: TWO STORIES OF FRAMED WINDOWS ----
                // The upper floor (over the floor band) and
                // the lower floor both wear dressedWindow —
                // trim, muntin cross, sill — in the places
                // the old flat rects stood, with the same
                // occupancy lighting read per window. Houses
                // seeded shuttered wear shutters on the
                // upper floor; the school, widest front in
                // town, takes three across.
                val ww = if (loc.kind == "school") bw * 0.09f else bw * 0.11f
                val wh = bh * 0.19f
                val colXs = if (loc.kind == "school")
                    listOf(left + bw * 0.08f, cx - ww / 2f, right - bw * 0.08f - ww)
                else listOf(left + bw * 0.10f, right - bw * 0.10f - ww)
                val shuttered = isHouse &&
                    (loc.id.hashCode() and 0x7fffffff) % 2 == 0
                val shutterHex = "#%06X".format(0xFFFFFF and
                    Paper.darker(look?.accentHex ?: bodyC, 0.8f))
                for (row in 0..1) {
                    // houses carry no sign, so their upper
                    // floor rides high under the gable; the
                    // signed kinds keep the row below the
                    // baseline's lettering, and the school's
                    // lower center is its door, not a window
                    val wy = if (row == 0) {
                        if (isHouse) top + bh * 0.15f else top + bh * 0.24f
                    } else top + bh * 0.54f
                    for (i in colXs.indices) {
                        if (loc.kind == "school" && row == 1 && i == 1)
                            continue
                        val wx = colXs[i]
                        val tvFlicker = row == 1 && isHouse &&
                            loc.id in TownInfo.tvHouses && i == 1 &&
                            bedtime != null && tmNow > bedtime
                        val lit = windowLit(i)
                        val col = when {
                            tvFlicker ->
                                if ((RenderClock.nowMs() / 280) % 2L == 0L)
                                    "#7ab8e8" else "#5a94c8"
                            lit -> warmth
                            else -> "#2a3a4a"
                        }
                        if (lit && night) {
                            c.drawOval(RectF(wx - 14, wy - 14,
                                wx + ww + 14, wy + wh + 14),
                                Paper.fillPaint(
                                    if (tvFlicker) "#7ab8e8" else warmth, 36))
                        }
                        dressedWindow(c, wx, wy, ww, wh, col, trimHex,
                            facadeSeed + row * 11L + i)
                        if (row == 0 && shuttered) {
                            c.drawRect(wx - ww * 0.46f, wy, wx - ww * 0.10f,
                                wy + wh, Paper.fillPaint(shutterHex))
                            c.drawRect(wx + ww * 1.10f, wy, wx + ww * 1.46f,
                                wy + wh, Paper.fillPaint(shutterHex))
                        }
                    }
                }
            }
            // #24 porch light: on for occupied homes, dark for empty
            if (isHouse) {
                val plit = occupied && (night || tmNow >= 17 * 60)
                c.drawCircle(cx + bw * 0.11f, baseY - bh * 0.48f, 5f,
                    Paper.fillPaint(if (plit) warmth else "#232a38"))
                if (plit && night) {
                    c.drawCircle(cx + bw * 0.11f, baseY - bh * 0.48f, 13f,
                        Paper.fillPaint(warmth, 40))
                }
            }
            // church steeple / school flag
            if (loc.id == "Church") {
                c.drawRect(cx - 8, top - bh * 1.05f, cx + 8, top - bh * 0.45f, body)
                val cp = Paper.strokePaint(if (night) warmth else "#3a2a1a", 4f)
                c.drawLine(cx, top - bh * 1.25f, cx, top - bh * 1.0f, cp)
                c.drawLine(cx - 8, top - bh * 1.17f, cx + 8, top - bh * 1.17f, cp)
            }
            if (loc.kind == "school") {
                val fp = Paper.strokePaint("#5a5a5a", 4f)
                c.drawLine(right - bw * 0.1f, top - bh * 0.5f, right - bw * 0.1f, top + 6, fp)
                c.drawRect(right - bw * 0.1f, top - bh * 0.5f, right - bw * 0.1f + 44, top - bh * 0.5f + 26,
                    Paper.fillPaint("#d43d2a"))
            }
        }
        // v0.1.9a #12: every house wears its mailbox out front —
        // the morning mail ritual happens right here (GameView
        // walks the resident to this exact x). And #41: as the
        // house's bedtime lands and the windows die one by one,
        // a yawn bubble pops over the glass.
        if (loc.kind == "house") {
            val mbX = left - bw * 0.10f
            c.drawRect(mbX - 3f, baseY - bh * 0.16f, mbX + 3f, baseY,
                Paper.fillPaint("#4a3a26"))
            c.drawRoundRect(RectF(mbX - bw * 0.045f, baseY - bh * 0.30f,
                mbX + bw * 0.045f, baseY - bh * 0.16f), 6f, 6f,
                Paper.fillPaint("#2e3438"))
            c.drawRect(mbX + bw * 0.045f - 4f, baseY - bh * 0.37f,
                mbX + bw * 0.045f, baseY - bh * 0.28f, Paper.fillPaint("#c22e2e"))
            val bed = TownInfo.bedtimeOf[loc.id]
            if (bed != null && loc.id in TownInfo.occupiedHomes &&
                TownLife.yawnAt(bed, TownInfo.timeMin)) {
                val bx = left + bw * 0.10f + bw * 0.055f
                // v0.16.0e: over the lower floor's glass —
                // the window row moved in the re-dress
                val by = top + bh * 0.54f - bh * 0.20f
                c.drawOval(RectF(bx - 26, by - 17, bx + 26, by + 13),
                    Paper.fillPaint("#ffffff", 235))
                c.drawPath(Path().apply {
                    moveTo(bx - 8, by + 10); lineTo(bx - 2, by + 22); lineTo(bx + 6, by + 11); close()
                }, Paper.fillPaint("#ffffff", 235))
                val yp = Paper.textPaint("#1a1a1a", h * 0.021f, bold = true, casual = true)
                yp.textAlign = android.graphics.Paint.Align.CENTER
                c.drawText("🥱", bx, by + h * 0.008f, yp)
            }
        }
        // crooked sign with the name — v0.16.0b: ONLY where
        // the facade carries no sign of its own. Shops,
        // civic buildings and the school wear the merged
        // map baseline's lettering ON the facade (above);
        // this generic board floated over their roofs as a
        // second, duplicating plaque (Lucky's playtest (b)).
        // Houses carry no baseline sign, so theirs stays —
        // seated on the roof peak, the only name they wear.
        if (look?.sign.isNullOrEmpty()) {
            Paper.drawSign(c, cx, top - h * 0.045f, (bw * 0.9f).coerceAtMost(w * 0.7f),
                h * 0.042f, loc.name, h * 0.019f)
        }
        // v0.16.0e pass 2: the costume overlay — this
        // parcel's dressing over its real facade
        fictionDress[loc.id]?.let { fd ->
            drawFictionDressing(c, fd, left, top, bw, bh,
                top + bh, night, bannerYFrac = 0.30f)
        }
    }

    /** v0.16.0e PASS 2: one parcel's dressing, drawn
     *  OVER its real facade/structure (never instead of
     *  it — every dressing's coverage < 1.0, so the real
     *  building shows through its costume; tape and
     *  string stay visible per the attachment). The
     *  channels ride along: surface (palette wash),
     *  props (banner / cardboard / shield / lance /
     *  sword, each the fiction prop its provenance
     *  names), signage (the institution's notice board).
     *  [progress] < 1 is the DRESSING phase made visible:
     *  the banner is part-up and a kid is on the job. */
    private fun drawFictionDressing(c: Canvas,
        d: Fiction.ParcelDress, L: Float, top: Float,
        bw: Float, bh: Float, baseY: Float, night: Boolean,
        bannerYFrac: Float) {
        val def = fictionDef ?: return
        val prog = d.progress
        fun col(hex: String) =
            if (night) mix(hex, "#232f4a", 0.45f) else hex
        if (d.paletteHex != null) {
            c.drawRect(L, top, L + bw, baseY,
                Paper.fillPaint(col(d.paletteHex),
                    (44 * prog).toInt()))
        }
        val seed = Fiction.seedOf(def.id, d.parcelId)
        for ((i, prop) in d.props.withIndex()) {
            val cov = prop.coverage * prog
            when (prop.fictionProp) {
                "bedsheet_banner" -> {
                    val bx = L + bw * 0.07f
                    val bwid = bw * 0.86f * cov
                    val by = top + bh * bannerYFrac
                    val bhgt = bh * 0.16f
                    val sag = bh * 0.02f * (0.5f + prop.wear)
                    val hem = Path().apply {
                        moveTo(bx, by)
                        lineTo(bx + bwid, by)
                        lineTo(bx + bwid, by + bhgt - sag * 0.4f)
                        quadTo(bx + bwid * 0.72f, by + bhgt + sag,
                            bx + bwid * 0.5f, by + bhgt - sag * 0.3f)
                        quadTo(bx + bwid * 0.26f, by + bhgt + sag,
                            bx, by + bhgt - sag * 0.4f)
                        close()
                    }
                    c.drawPath(hem, Paper.fillPaint(col("#efe6cc")))
                    // v0.17.0a: the banner is CLOTH — woven
                    // fibre over the sheet, clipped to its
                    // own sagging hem (region synthesis)
                    c.save()
                    c.clipPath(hem)
                    Paper.applyMaterial(c, bx, by,
                        bx + bwid, by + bhgt, "#efe6cc",
                        (seed + i * 7).toLong(), 0.5f, prop.region)
                    c.restore()
                    c.drawRect(bx, by + bhgt - sag * 0.3f - bh * 0.018f,
                        bx + bwid, by + bhgt - sag * 0.3f,
                        Paper.fillPaint(col(def.accentHex)))
                    // the tape: two tabs at the top corners
                    val tab = Paper.fillPaint("#d9cf9e", 215)
                    c.drawRect(bx - 2f, by - bh * 0.016f,
                        bx + bw * 0.045f, by + bh * 0.008f, tab)
                    c.drawRect(bx + bwid - bw * 0.045f, by - bh * 0.016f,
                        bx + bwid + 2f, by + bh * 0.008f, tab)
                    if (d.claimName != null) {
                        val tp = Paper.textPaint(col("#4a2f1a"),
                            bh * 0.052f, bold = true, casual = true)
                        tp.textAlign = android.graphics.Paint.Align.CENTER
                        c.drawText(d.claimName.uppercase(),
                            bx + bwid / 2f, by + bhgt * 0.62f, tp)
                    }
                }
                "cardboard_wall" -> {
                    val pw = bw * 0.30f * cov
                    val ph = bh * 0.24f
                    val px = L + bw * (0.05f + i * 0.30f)
                    val tan = col("#b08d57")
                    val tanD = col("#8a6c3e")
                    for (seg in 0..1) {
                        val sx = px + pw * seg / 2f
                        val sh = ph * (1f - seg * 0.10f * prop.wear)
                        c.drawRect(sx, baseY - sh, sx + pw / 2f - 2f,
                            baseY, Paper.fillPaint(tan))
                        c.drawRect(sx, baseY - sh, sx + pw / 2f - 2f,
                            baseY - sh + 3f, Paper.fillPaint(tanD))
                    }
                    // one flute line + the tape cross
                    c.drawRect(px + pw * 0.24f, baseY - ph * 0.9f,
                        px + pw * 0.24f + 2f, baseY - ph * 0.1f,
                        Paper.fillPaint(tanD, 130))
                    val tab = Paper.fillPaint("#d9cf9e", 200)
                    c.drawRect(px + pw * 0.40f, baseY - ph - 2f,
                        px + pw * 0.60f, baseY - ph + bh * 0.03f, tab)
                }
                "lid_shield" -> {
                    val cx = L + bw * 0.80f
                    val cy = baseY - bh * 0.16f
                    val r = bh * 0.065f
                    c.drawCircle(cx, cy, r, Paper.fillPaint(col("#9aa2ab")))
                    c.drawCircle(cx, cy, r * 0.78f,
                        Paper.fillPaint(col("#7d858f")))
                    // v0.17.0a: the lid is METAL — sparse
                    // grain + the region's edge sheen over
                    // the disc (region synthesis)
                    c.save()
                    val lidPath = Path().apply {
                        addCircle(cx, cy, r,
                            Path.Direction.CW)
                    }
                    c.clipPath(lidPath)
                    Paper.applyMaterial(c, cx - r, cy - r,
                        cx + r, cy + r, "#9aa2ab",
                        (seed + i * 13).toLong(), 0.7f, prop.region)
                    c.restore()
                    c.drawCircle(cx, cy, r * 0.3f,
                        Paper.fillPaint(col(def.accentHex)))
                }
                "broom_lance" -> {
                    val sx = L + bw * 0.16f
                    val p = Paper.strokePaint(col("#7a5230"), 3.5f)
                    c.drawLine(sx, baseY, sx + bw * 0.07f,
                        baseY - bh * 0.36f, p)
                    c.drawCircle(sx + bw * 0.07f, baseY - bh * 0.36f,
                        bh * 0.016f, Paper.fillPaint(col(def.accentHex)))
                }
                "stick_sword" -> {
                    val sx = L + bw * 0.90f
                    val p = Paper.strokePaint(col("#6a4a2e"), 3f)
                    c.drawLine(sx, baseY, sx + bw * 0.012f,
                        baseY - bh * 0.17f, p)
                    c.drawLine(sx - bw * 0.02f, baseY - bh * 0.115f,
                        sx + bw * 0.032f, baseY - bh * 0.115f, p)
                }
            }
        }
        if (d.signText != null) {
            val nw = bw * 0.46f
            val nh = bh * 0.115f
            val nx = L + bw * 0.52f
            val ny = top + bh * 0.585f
            c.drawRect(nx - 3f, ny - 3f, nx + nw + 3f, ny + nh + 3f,
                Paper.fillPaint(col("#5a4632")))
            c.drawRect(nx, ny, nx + nw, ny + nh,
                Paper.fillPaint(col("#f2e8ce")))
            val tp = Paper.textPaint(col("#33241a"),
                bh * 0.033f, bold = true, casual = true)
            tp.textAlign = android.graphics.Paint.Align.CENTER
            val words = d.signText.split(" — ")
            c.drawText(words[0], nx + nw / 2f, ny + nh * 0.44f, tp)
            if (words.size > 1) c.drawText(words[1],
                nx + nw / 2f, ny + nh * 0.82f, tp)
        }
        if (prog < 1f) {
            // a kid on the job, taping this parcel up
            val kidName = listOf("Stan", "Kyle",
                "Cartman", "Kenny")[
                (seed and 0x7fffffff) % 4]
            val kfh = bh * 0.52f
            KidArt.drawKid(c, kidName, L + bw * 0.90f,
                baseY - kfh / 2f, kfh, "idle", null, "stand")
            c.drawCircle(L + bw * 0.90f, baseY - bh * 0.012f,
                bh * 0.02f, Paper.strokePaint("#d9cf9e", 3f))
        }
    }

    /** v0.15.0d: the STRUCTURE an outdoor or hub location
     *  fronts the street with — drawn at world scale,
     *  base on the ground line, its door (or framed
     *  opening) at FacadeGeom.structureDoorRect, the
     *  exact rect GameView's door pass outlines and
     *  hit-tests. This replaces the free-standing door
     *  slab the old view planted in these scenes. */
    private fun drawStructure(c: Canvas, w: Int, h: Int, loc: Location,
                              structure: String, night: Boolean) {
        val look = TownBaseline.of(loc.id)
        fun col(hex: String) = if (night) mix(hex, "#232f4a", 0.45f) else hex
        val body = col(look?.bodyHex ?: loc.colorHex)
        val roof = col(look?.roofHex ?: "#5a5a66")
        val accent = col(look?.accentHex ?: "#e8c53a")
        // v0.15.0g: the structure composes at unit width
        // at the world's heart, like the facade
        val unitW = SceneScale.unitW(w.toFloat(), loc, false)
        val sr = FacadeGeom.structureRect(structure, w.toFloat(), h.toFloat(), unitW)
        val dr = FacadeGeom.structureDoorRect(structure, w.toFloat(), h.toFloat(), unitW)
        // paper shadow
        c.drawRect(sr.left + 4f, sr.top + 5f, sr.right + 4f, sr.bottom + 5f,
            Paper.fillPaint("#1a2436", 42))
        fun doorFill() {
            c.drawRect(dr.left, dr.top, dr.right, dr.bottom,
                Paper.fillPaint(Paper.darker(accent, 0.7f)))
            c.drawCircle(dr.right - dr.width * 0.14f, (dr.top + dr.bottom) / 2f,
                dr.width * 0.05f, Paper.fillPaint("#e8c53a"))
        }
        when (structure) {
            "shelter" -> {
                // the bus shelter: a back panel, two posts,
                // a flat roof, a bench inside — its opening
                // is the entry (the door rect), framed by
                // the posts, never a slab
                val postW = sr.width * 0.035f
                c.drawRect(sr.left, sr.top + sr.height * 0.10f, sr.right,
                    sr.bottom, Paper.fillPaint(body, 165))
                c.drawRect(sr.left, sr.top, sr.left + postW, sr.bottom,
                    Paper.fillPaint(roof))
                c.drawRect(sr.right - postW, sr.top, sr.right, sr.bottom,
                    Paper.fillPaint(roof))
                c.drawRect(sr.left - sr.width * 0.03f, sr.top - sr.height * 0.07f,
                    sr.right + sr.width * 0.03f, sr.top, Paper.fillPaint(roof))
                val benchY = sr.bottom - sr.height * 0.16f
                c.drawRect(sr.left + sr.width * 0.10f, benchY,
                    sr.right - sr.width * 0.10f, benchY + sr.height * 0.045f,
                    Paper.fillPaint("#7a5230"))
                c.drawRect(sr.left + sr.width * 0.10f, benchY - sr.height * 0.13f,
                    sr.right - sr.width * 0.10f, benchY - sr.height * 0.09f,
                    Paper.fillPaint("#7a5230"))
            }
            "pavilion" -> {
                // open picnic pavilion: posts + a pitched
                // roof; the center opening is the entry
                val postW = sr.width * 0.03f
                for (px in listOf(sr.left, sr.cx - postW / 2f, sr.right - postW)) {
                    c.drawRect(px, sr.top + sr.height * 0.22f, px + postW,
                        sr.bottom, Paper.fillPaint(roof))
                }
                c.drawPath(Path().apply {
                    moveTo(sr.left - sr.width * 0.05f, sr.top + sr.height * 0.24f)
                    lineTo(sr.cx, sr.top)
                    lineTo(sr.right + sr.width * 0.05f, sr.top + sr.height * 0.24f)
                    close()
                }, Paper.fillPaint(roof))
                val tableY = sr.bottom - sr.height * 0.10f
                c.drawRect(sr.left + sr.width * 0.16f, tableY,
                    sr.right - sr.width * 0.16f, tableY + sr.height * 0.04f,
                    Paper.fillPaint(body))
            }
            "barn" -> {
                c.drawRect(sr.left, sr.top + sr.height * 0.16f, sr.right,
                    sr.bottom, Paper.fillPaint(body))
                // gambrel shoulders: two sloped cuts
                c.drawPath(Path().apply {
                    moveTo(sr.left - sr.width * 0.03f, sr.top + sr.height * 0.18f)
                    lineTo(sr.left + sr.width * 0.18f, sr.top)
                    lineTo(sr.right - sr.width * 0.18f, sr.top)
                    lineTo(sr.right + sr.width * 0.03f, sr.top + sr.height * 0.18f)
                    close()
                }, Paper.fillPaint(roof))
                // hayloft window
                c.drawRect(sr.cx - sr.width * 0.07f, sr.top + sr.height * 0.08f,
                    sr.cx + sr.width * 0.07f, sr.top + sr.height * 0.24f,
                    Paper.fillPaint(accent))
                doorFill()
                // the X brace on the barn door
                val dp = Paper.strokePaint(accent, 3f)
                c.drawLine(dr.left, dr.top, dr.right, dr.bottom, dp)
                c.drawLine(dr.left, dr.bottom, dr.right, dr.top, dp)
            }
            "loft" -> {
                // a SoDoSoPa loft block: two floors of glass
                // v0.16.0e: the glass is FRAMED now — trim
                // boards and mullion verticals over the panes
                c.drawRect(sr.left, sr.top, sr.right, sr.bottom,
                    Paper.fillPaint(body))
                for (r in 0..1) {
                    val wy = sr.top + sr.height * (0.12f + r * 0.30f)
                    dressedWindow(c, sr.left + sr.width * 0.08f, wy,
                        sr.width * 0.84f, sr.height * 0.20f,
                        col("#bcd6f2"), col("#e8e2d2"),
                        loc.id.hashCode().toLong() * 53L + r)
                }
                c.drawRect(sr.left - 2f, sr.top - sr.height * 0.03f,
                    sr.right + 2f, sr.top, Paper.fillPaint(roof))
                dressedDoor(c, dr,
                    "#%06X".format(0xFFFFFF and
                        Paper.darker(accent, 0.7f)),
                    col("#e8e2d2"),
                    loc.id.hashCode().toLong() * 53L + 3L)
            }
            "diner" -> {
                // the Main Street diner front
                // v0.16.0e: a true GLASS FRONT — the counter
                // hinted behind mullioned panes — under the
                // striped awning, the dressed door at its
                // center (the structure door rect, as ever).
                // The two flat window slabs are removed whole.
                c.drawRect(sr.left, sr.top, sr.right, sr.bottom,
                    Paper.fillPaint(body))
                c.drawRect(sr.left - 2f, sr.top - sr.height * 0.04f,
                    sr.right + 2f, sr.top, Paper.fillPaint(roof))
                val winTop = sr.top + sr.height * 0.30f
                // v0.16.0b: the band's foot is a share of the
                // front's own height — anchored to the door's
                // px height it inverted (winBot above winTop)
                // and the diner's windows never drew at all.
                val winBot = sr.bottom - sr.height * 0.18f
                val structSeed = loc.id.hashCode().toLong() * 53L + 9L
                glassFront(c, sr.left + sr.width * 0.05f, winTop,
                    sr.right - sr.width * 0.05f, winBot, structSeed + 5L,
                    col("#33302c"), night, lit = night)
                val awnW = sr.width / 8f
                for (i in 0 until 8) {
                    c.drawRect(sr.left + i * awnW, sr.top + sr.height * 0.16f,
                        sr.left + (i + 1) * awnW, sr.top + sr.height * 0.27f,
                        Paper.fillPaint(if (i % 2 == 0) "#f0e8d0" else accent))
                }
                dressedDoor(c, dr,
                    "#%06X".format(0xFFFFFF and
                        Paper.darker(accent, 0.7f)),
                    col("#e8e2d2"), structSeed + 3L)
            }
            "mall" -> {
                // v0.16.0e: the mall gets its front at last —
                // it used to fall through to the hut branch.
                // A parapet cap, then glass across the whole
                // ground floor with the entrance doors at
                // its center; the name sign the facade pass
                // seats on the front rides above the glass.
                c.drawRect(sr.left, sr.top, sr.right, sr.bottom,
                    Paper.fillPaint(body))
                c.drawRect(sr.left - 3f, sr.top - sr.height * 0.05f,
                    sr.right + 3f, sr.top, Paper.fillPaint(roof))
                val structSeed = loc.id.hashCode().toLong() * 53L + 9L
                glassFront(c, sr.left + sr.width * 0.04f,
                    sr.top + sr.height * 0.34f,
                    sr.right - sr.width * 0.04f, sr.bottom,
                    structSeed + 5L, col("#33302c"), night, lit = night)
                dressedDoor(c, dr,
                    "#%06X".format(0xFFFFFF and
                        Paper.darker(accent, 0.7f)),
                    col("#e8e2d2"), structSeed + 3L)
            }
            else -> {
                // the ice-fishing hut and any other front:
                // a small cabin body, a pitched roof, a door
                c.drawRect(sr.left, sr.top + sr.height * 0.18f, sr.right,
                    sr.bottom, Paper.fillPaint(body))
                c.drawPath(Path().apply {
                    moveTo(sr.left - sr.width * 0.05f, sr.top + sr.height * 0.20f)
                    lineTo(sr.cx, sr.top)
                    lineTo(sr.right + sr.width * 0.05f, sr.top + sr.height * 0.20f)
                    close()
                }, Paper.fillPaint(roof))
                c.drawRect(sr.left + sr.width * 0.10f, sr.top + sr.height * 0.38f,
                    sr.left + sr.width * 0.26f, sr.top + sr.height * 0.58f,
                    Paper.fillPaint(col("#ffd98a")))
                doorFill()
            }
        }
        // v0.16.0d (stage 3): the structure's solid walls wear
        // the paper material too. Open structures (the
        // pavilion) have no wall to wash — posts and a roof
        // are timber, not paper stock; the shelter's back
        // panel takes a lighter wash.
        val structSeed = loc.id.hashCode().toLong() * 53L + 9L
        when (structure) {
            "barn" -> Paper.applyMaterial(c, sr.left,
                sr.top + sr.height * 0.16f, sr.right, sr.bottom,
                body, structSeed)
            "loft", "diner", "mall" -> Paper.applyMaterial(c, sr.left,
                sr.top, sr.right, sr.bottom, body, structSeed)
            "shelter" -> Paper.applyMaterial(c, sr.left,
                sr.top + sr.height * 0.10f, sr.right, sr.bottom,
                body, structSeed, 0.6f)
            "pavilion" -> {}
            else -> Paper.applyMaterial(c, sr.left,
                sr.top + sr.height * 0.18f, sr.right, sr.bottom,
                body, structSeed)
        }
        // v0.16.0e pass 2: a wild/urban parcel's dressing
        // over its structure — Stark's Pond's cardboard
        // fort stands here when the fiction fabricates it
        fictionDress[loc.id]?.let { fd ->
            drawFictionDressing(c, fd, sr.left, sr.top,
                sr.width, sr.height, sr.bottom, night,
                bannerYFrac = 0.06f)
        }
    }

    /** Per-location outdoor features: bus bench, pond, hoop, barn, lofts. */
    private fun drawLocationFeature(c: Canvas, w: Int, h: Int, loc: Location, rnd: Random, night: Boolean) {
        val cx = w / 2f
        val baseY = h * 0.66f
        // v0.16.0b: detail SIZES and detail-local offsets compose
        // against the unit width (SceneScale's rule: widths anchor
        // to unitW, positions to the world) — the pre-fix painters
        // here multiplied the full widened world width, so a bench
        // spanned half the street and Main Street's old storefront
        // row painted screen-wide slabs over the real facades
        // (Lucky's playtest shots). World-fraction POSITIONS of
        // landscape features (the pond, the hoop) are unchanged.
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        when (loc.id) {
            "BusStop" -> {
                // bench + sign
                val wood = Paper.fillPaint("#7a5230")
                Paper.crooked(c, -1.5f, cx - uw * 0.22f, baseY + 40) {
                    c.drawRect(cx - uw * 0.30f, baseY + 30, cx - uw * 0.14f, baseY + 44, wood)
                    c.drawRect(cx - uw * 0.30f, baseY + 8, cx - uw * 0.14f, baseY + 22, wood)
                    c.drawRect(cx - uw * 0.29f, baseY + 22, cx - uw * 0.27f, baseY + 60, wood)
                    c.drawRect(cx - uw * 0.17f, baseY + 22, cx - uw * 0.15f, baseY + 60, wood)
                }
                val pole = Paper.strokePaint("#5a5a5a", 5f)
                c.drawLine(cx + uw * 0.24f, baseY - 40, cx + uw * 0.24f, baseY + 50, pole)
                Paper.drawSign(c, cx + uw * 0.24f, baseY - 62, uw * 0.22f, h * 0.035f, "BUS STOP", h * 0.016f)
            }
            "StarksPond" -> {
                // frozen pond ellipse, misty
                val ice = Paper.fillPaint(if (night) "#3a4e78" else "#9fc4e8")
                c.drawOval(RectF(cx - w * 0.36f, baseY, cx + w * 0.36f, baseY + h * 0.10f), ice)
                val mist = Paper.fillPaint("#ffffff", 90)
                c.drawOval(RectF(cx - w * 0.30f, baseY + 6, cx + w * 0.30f, baseY + h * 0.07f), mist)
                // cracks
                val crack = Paper.strokePaint(if (night) "#22304e" else "#5a8ac8", 2f)
                c.drawLine(cx - 40, baseY + 30, cx + 30, baseY + 44, crack)
                c.drawLine(cx + 10, baseY + 24, cx - 20, baseY + 52, crack)
            }
            "KidsPark" -> {
                // basketball hoop
                val pole = Paper.strokePaint("#8a8a8a", 7f)
                c.drawLine(cx + w * 0.26f, baseY - 90, cx + w * 0.26f, baseY + 40, pole)
                c.drawRect(cx + w * 0.26f - 44, baseY - 120, cx + w * 0.26f + 10, baseY - 78,
                    Paper.fillPaint("#f0e8d0"))
                c.drawOval(RectF(cx + w * 0.26f - 52, baseY - 78, cx + w * 0.26f - 16, baseY - 64),
                    Paper.strokePaint("#d43d2a", 4f))
            }
            "TegridyFarms" -> {
                // red barn
                val barn = Paper.fillPaint(if (night) "#5e2a22" else "#a03a2a")
                val bx = cx + w * 0.18f
                Paper.crooked(c, 1.5f, bx, baseY - 60) {
                    c.drawRect(bx - 70, baseY - 110, bx + 70, baseY, barn)
                    c.drawPath(Path().apply {
                        moveTo(bx - 78, baseY - 110); lineTo(bx, baseY - 150); lineTo(bx + 78, baseY - 110); close()
                    }, Paper.fillPaint(Paper.darker("#a03a2a", 0.6f)))
                    c.drawRect(bx - 18, baseY - 60, bx + 18, baseY, Paper.fillPaint("#3a2a1a"))
                }
            }
            "SoDoSoPa" -> {
                // gentrified loft boxes
                for (i in 0..2) {
                    val lx = cx - w * 0.28f + i * w * 0.20f
                    Paper.crooked(c, (rnd.nextFloat() - 0.5f) * 3f, lx, baseY - 50) {
                        c.drawRect(lx - 44, baseY - 100 - i * 12, lx + 44, baseY,
                            Paper.fillPaint("#c8b8a0"))
                    }
                }
            }
        }
    }

    // ============================================================
    // v0.1.9a Scope B — the parity pass. The characters were
    // measured to the show in v0.1.7; the PLACES weren't. Every
    // location now gets its defining construction-paper details
    // on top of the generic facade: flat color fields, bold
    // simple shapes, the props and signage the lorebook blurbs
    // and the show's stock shots agree on. The registry of what
    // belongs here lives in GameData.LOC_ART_DETAILS; each detail
    // is marked with a `// detail: <token>` comment and the
    // smoke suite cross-checks registry vs. source.
    // ============================================================
    private fun drawParityDetails(c: Canvas, w: Int, h: Int, loc: Location,
                                   rnd: Random, night: Boolean, weatherId: String) {
        val cx = w / 2f
        val baseY = h * 0.66f
        // v0.16.0b: the detail zone IS the facade's body width —
        // FacadeGeom's fractions composed against the unit width,
        // exactly as the facade painter composes them. The pre-fix
        // table multiplied the full widened world width, so every
        // porch, garage, gate and storefront detail sprawled 2.5-3x
        // wider than the building it dresses (Lucky's playtest).
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        val bw = uw * FacadeGeom.bodyWFrac(loc.kind)
        val bh = when (loc.kind) {
            "house" -> h * 0.20f; "shop" -> h * 0.22f; "civic" -> h * 0.21f
            "school" -> h * 0.20f; else -> h * 0.10f
        }
        val left = cx - bw / 2
        val right = cx + bw / 2
        val top = baseY - bh
        // v0.3.0a: parity colors run through weather wear first
        // (rain-darkened awnings, fog-muted signs), then the
        // night mix — same order as the facade body.
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        val doorTop = baseY - bh * 0.45f
        val winY = top + bh * 0.30f
        val winW = bw * 0.11f

        when (loc.id) {
            "KennysHouse" -> {
                // detail: railroad-tracks — the shack sits across them
                val rail = Paper.strokePaint(if (night) "#5a6a86" else "#8a8a92", 3.5f)
                val tie = Paper.fillPaint(col("#4a3626"))
                var tx = 6f
                while (tx < w) {
                    c.drawRect(tx, h * 0.888f, tx + 26f, h * 0.928f, tie)
                    tx += 46f
                }
                c.drawLine(0f, h * 0.898f, w.toFloat(), h * 0.898f, rail)
                c.drawLine(0f, h * 0.918f, w.toFloat(), h * 0.918f, rail)
                // detail: sagging-porch — the porch roof droops, one post shorter
                val porch = Paper.strokePaint(col("#5d5142"), 6f)
                c.drawLine(left + 4f, top + bh * 0.50f, left + bw * 0.44f, top + bh * 0.62f, porch)
                c.drawLine(left + 10f, top + bh * 0.52f, left + 10f, baseY, porch)
                c.drawLine(left + bw * 0.42f, top + bh * 0.63f, left + bw * 0.42f, baseY - bh * 0.06f, porch)
                // detail: boarded-window — crossed planks over the right window
                val wx = right - bw * 0.10f - winW
                val plank = Paper.fillPaint(col("#9a7a4a"))
                Paper.crooked(c, 24f, wx + winW / 2, winY + winW / 2) {
                    c.drawRect(wx - 4, winY + winW * 0.32f, wx + winW + 4, winY + winW * 0.62f, plank)
                }
                Paper.crooked(c, -24f, wx + winW / 2, winY + winW / 2) {
                    c.drawRect(wx - 4, winY + winW * 0.38f, wx + winW + 4, winY + winW * 0.68f, plank)
                }
            }
            "ButtersHouse" -> {
                // detail: picket-fence — suspiciously pristine, like the house
                val picket = fp("#f4f6f8")
                var fx = left - bw * 0.10f
                while (fx < right + bw * 0.10f) {
                    c.drawRect(fx, baseY + h * 0.028f, fx + 7f, baseY + h * 0.062f, picket)
                    c.drawPath(Path().apply {
                        moveTo(fx, baseY + h * 0.028f); lineTo(fx + 3.5f, baseY + h * 0.020f)
                        lineTo(fx + 7f, baseY + h * 0.028f); close()
                    }, picket)
                    fx += 15f
                }
                c.drawRect(left - bw * 0.10f, baseY + h * 0.040f, right + bw * 0.10f,
                    baseY + h * 0.045f, picket)
                // detail: neat-lawn — mow stripes even in the snow season
                c.drawRect(left - bw * 0.06f, baseY + h * 0.075f, cx - 6f, baseY + h * 0.088f,
                    fp("#b9d2a8", 120))
                c.drawRect(cx + 6f, baseY + h * 0.075f, right + bw * 0.06f, baseY + h * 0.088f,
                    fp("#b9d2a8", 120))
            }
            "CartmanHouse" -> {
                // detail: driveway — a cracked slab out front
                c.drawRect(right + 6f, baseY, right + bw * 0.32f, h * 0.965f, fp("#a8a8a8"))
                val crack = Paper.strokePaint(col("#6a6a6a"), 1.8f)
                c.drawLine(right + bw * 0.10f, baseY + 6, right + bw * 0.16f, h * 0.94f, crack)
                c.drawLine(right + bw * 0.16f, h * 0.90f, right + bw * 0.26f, h * 0.925f, crack)
                // detail: red-wagon — left on the lawn, as always
                val wx = left - bw * 0.16f
                val wy = baseY + h * 0.045f
                c.drawRect(wx, wy - 14, wx + 42, wy, fp("#c22e2e"))
                c.drawLine(wx + 42, wy - 6, wx + 58, wy - 16, Paper.strokePaint(col("#5a5a5a"), 3f))
                c.drawCircle(wx + 9, wy + 2, 6f, fp("#22262e"))
                c.drawCircle(wx + 34, wy + 2, 6f, fp("#22262e"))
            }
            "StanHouse" -> {
                // detail: garage — Randy's domain, attached on the right
                c.drawRect(right - 2f, top + bh * 0.16f, right + bw * 0.36f, baseY, fp("#466a80"))
                c.drawRect(right - 2f, top + bh * 0.16f, right + bw * 0.36f, top + bh * 0.24f,
                    fp("#38566a"))
                val panel = Paper.strokePaint(col("#2e4656"), 2.5f)
                for (i in 1..3) {
                    val py = top + bh * 0.16f + (bh * 0.84f) * i / 4f + bh * 0.10f
                    if (py < baseY) c.drawLine(right + 6f, py, right + bw * 0.36f - 6f, py, panel)
                }
                c.drawCircle(right + bw * 0.18f, baseY - bh * 0.10f, 3.5f, fp("#d8dee6"))
                // detail: driveway-slab — oil-stained, Randy was here
                c.drawRect(right + 2f, baseY, right + bw * 0.34f, h * 0.96f, fp("#9aa0a4"))
                c.drawOval(RectF(right + bw * 0.08f, h * 0.90f, right + bw * 0.22f, h * 0.925f),
                    fp("#3c4148", 110))
            }
            "KyleHouse" -> {
                // detail: hedge-row — trimmed flat, morally upright
                val hedge = fp("#3f6b35")
                var hx = left - bw * 0.04f
                while (hx < right + bw * 0.02f) {
                    c.drawOval(RectF(hx, baseY - 2, hx + bw * 0.11f, baseY + h * 0.020f), hedge)
                    hx += bw * 0.095f
                }
                // detail: neat-walkway — a straight, swept path to the door
                c.drawPath(Path().apply {
                    moveTo(cx - bw * 0.045f, baseY); lineTo(cx + bw * 0.045f, baseY)
                    lineTo(cx + bw * 0.075f, h * 0.965f); lineTo(cx - bw * 0.075f, h * 0.965f)
                    close()
                }, fp("#c8c2b2"))
            }
            "TolkienHouse" -> {
                // detail: columns — the biggest house in South Park has them
                val colP = fp("#e8e2d4")
                for (sx in listOf(cx - bw * 0.17f, cx + bw * 0.17f)) {
                    c.drawRect(sx - 6f, top + bh * 0.06f, sx + 6f, baseY, colP)
                    c.drawRect(sx - 9f, top + bh * 0.06f, sx + 9f, top + bh * 0.12f, colP)
                }
                // detail: dark-meadows-arch — the gated community's gate arch
                val post = Paper.strokePaint(col("#2c2f36"), 7f)
                c.drawLine(left - bw * 0.17f, baseY, left - bw * 0.17f, baseY - bh * 0.85f, post)
                c.drawLine(right + bw * 0.17f, baseY, right + bw * 0.17f, baseY - bh * 0.85f, post)
                c.drawPath(Path().apply {
                    moveTo(left - bw * 0.17f, baseY - bh * 0.85f)
                    quadTo(cx, baseY - bh * 1.18f, right + bw * 0.17f, baseY - bh * 0.85f)
                }, Paper.strokePaint(col("#2c2f36"), 6f))
                Paper.drawSign(c, cx, baseY - bh * 0.98f, bw * 0.52f, h * 0.030f,
                    "DARK MEADOWS", h * 0.014f)
                // detail: iron-gate — bars across the drive, mostly closed
                val bar = Paper.strokePaint(col("#2c2f36"), 3f)
                var gx = left - bw * 0.15f
                while (gx < cx - bw * 0.10f) {
                    c.drawLine(gx, baseY - bh * 0.42f, gx, baseY, bar)
                    gx += bw * 0.045f
                }
                c.drawLine(left - bw * 0.17f, baseY - bh * 0.40f, cx - bw * 0.08f, baseY - bh * 0.40f, bar)
            }
            "BusStop" -> {
                // detail: road-strip — the road the bus actually runs on
                c.drawRect(0f, h * 0.905f, w.toFloat(), h * 0.978f, fp("#3c4148"))
                val dash = fp("#e8c53a")
                var dx = 10f
                while (dx < w) {
                    c.drawRect(dx, h * 0.937f, dx + 34f, h * 0.944f, dash)
                    dx += 74f
                }
                // detail: snowbank — plowed up behind the sign, all winter
                // (v0.16.0b: unit-width offsets, like the bench it backs)
                c.drawOval(RectF(cx + uw * 0.17f, baseY + 22, cx + uw * 0.36f, baseY + 58),
                    fp("#eef4ff"))
                c.drawOval(RectF(cx + uw * 0.21f, baseY + 10, cx + uw * 0.32f, baseY + 44),
                    fp("#ffffff"))
            }
            "MainStreet" -> {
                // detail: gazette-boxes — two newspaper boxes flank
                // the diner front at the ground line, unit-width
                // composed like every other facade detail.
                // v0.16.0b RETIRED here, whole: the old
                // "storefront-row" (five fake shops at world-width
                // fractions — in the widened world each painted a
                // screen-wide slab over the real facades, burying
                // their signs: Lucky's playtest shots (a)/(d)) and
                // the "town-banner" (a SOUTH PARK band strung at
                // w*0.30 across the middle of the scene, over
                // buildings and people — playtest (b); a one-screen
                // street flourish with no correct place in the
                // widened world, so per the brief it does not render
                // in-scene at all). The real baseline row behind
                // carries Main Street's shops now.
                for (s in listOf(-1f, 1f)) {
                    val bx = cx + s * bw * 0.62f
                    val bwBox = uw * 0.052f
                    val bhBox = h * 0.052f
                    c.drawRect(bx - bwBox / 2f + 3f, baseY - bhBox + 4f,
                        bx + bwBox / 2f + 3f, baseY + 4f, fp("#1a2436", 42))
                    c.drawRect(bx - bwBox / 2f, baseY - bhBox, bx + bwBox / 2f,
                        baseY, fp(if (s < 0f) "#2a6f8a" else "#a03a2a"))
                    c.drawRect(bx - bwBox * 0.34f, baseY - bhBox * 0.82f,
                        bx + bwBox * 0.34f, baseY - bhBox * 0.38f, fp("#f4ecd8"))
                    c.drawRect(bx - bwBox * 0.34f, baseY - bhBox * 0.30f,
                        bx + bwBox * 0.34f, baseY - bhBox * 0.22f, fp("#3a2a1a"))
                }
            }
            "TweekBros" -> {
                // detail: cup-sign — a steaming cup hangs by the door
                // (v0.16.0b: seated ON the wall right of the door —
                // it hung past the facade's right edge, off the
                // building over the neighbor gap, out of frame)
                val bx = cx + bw * 0.16f
                c.drawLine(bx - 34, top + bh * 0.08f, bx + 16, top + bh * 0.08f,
                    Paper.strokePaint(col("#3a2a1a"), 4f))
                c.drawLine(bx + 16, top + bh * 0.08f, bx + 16, top + bh * 0.16f,
                    Paper.strokePaint(col("#3a2a1a"), 3f))
                c.drawRoundRect(RectF(bx - 14, top + bh * 0.16f, bx + 46, top + bh * 0.52f),
                    7f, 7f, fp("#f0e8d0"))
                c.drawPath(Path().apply {
                    moveTo(bx + 2, top + bh * 0.30f); lineTo(bx + 26, top + bh * 0.30f)
                    lineTo(bx + 22, top + bh * 0.44f); lineTo(bx + 6, top + bh * 0.44f); close()
                }, fp("#7a5230"))
                val steam = Paper.strokePaint(col("#ffffff"), 2.5f)
                steam.alpha = 200
                c.drawLine(bx + 9, top + bh * 0.28f, bx + 11, top + bh * 0.23f, steam)
                c.drawLine(bx + 18, top + bh * 0.28f, bx + 20, top + bh * 0.23f, steam)
                steam.alpha = 255
                // detail: menu-board — the A-frame by the walk
                // (v0.16.0b: on the walk in front of the facade,
                // left of the door — it stood off the facade's
                // left edge, over the neighbor's lot)
                val mx = cx - bw * 0.30f
                Paper.crooked(c, -4f, mx, baseY) {
                    c.drawRect(mx - 17, baseY - bh * 0.34f, mx + 17, baseY, fp("#5c4030"))
                    val lp = Paper.strokePaint("#f0e8d0", 2f)
                    for (i in 0..2) {
                        val ly = baseY - bh * 0.27f + i * bh * 0.08f
                        c.drawLine(mx - 11, ly, mx + 11 - (i % 2) * 7, ly, lp)
                    }
                }
            }
            "JimbosGuns" -> {
                // detail: wagon-wheel — out front, per the blurb
                val wx = left - bw * 0.075f
                val wy = baseY - bh * 0.10f
                val wr = bh * 0.20f
                val rim = Paper.strokePaint(col("#4a3423"), 6f)
                c.drawCircle(wx, wy, wr, rim)
                val spoke = Paper.strokePaint(col("#4a3423"), 3.5f)
                for (i in 0..5) {
                    val a = i * Math.PI / 3
                    c.drawLine(wx, wy, (wx + wr * kotlin.math.cos(a)).toFloat(),
                        (wy + wr * kotlin.math.sin(a)).toFloat(), spoke)
                }
                c.drawCircle(wx, wy, wr * 0.18f, fp("#4a3423"))
                // detail: antlers — mounted over the door, of course
                val ant = Paper.strokePaint(col("#d8cbb2"), 3.5f)
                for (s in listOf(-1f, 1f)) {
                    val bx = cx + s * bw * 0.045f
                    val by = doorTop - 4f
                    c.drawLine(bx, by, bx + s * bw * 0.05f, by - bh * 0.16f, ant)
                    c.drawLine(bx + s * bw * 0.025f, by - bh * 0.07f,
                        bx + s * bw * 0.065f, by - bh * 0.12f, ant)
                    c.drawLine(bx + s * bw * 0.038f, by - bh * 0.11f,
                        bx + s * bw * 0.07f, by - bh * 0.20f, ant)
                }
            }
            "CityWok" -> {
                // detail: pagoda-eaves — upturned layered roof trim
                for (tier in 0..1) {
                    val tw = bw * (1.06f - tier * 0.16f)
                    val ty = top - bh * (0.10f + tier * 0.13f)
                    c.drawPath(Path().apply {
                        moveTo(cx - tw / 2, ty)
                        quadTo(cx - tw * 0.30f, ty - bh * 0.055f, cx, ty - bh * 0.02f)
                        quadTo(cx + tw * 0.30f, ty - bh * 0.055f, cx + tw / 2, ty)
                        lineTo(cx + tw / 2 - 8, ty + bh * 0.07f)
                        quadTo(cx, ty + bh * 0.045f, cx - tw / 2 + 8, ty + bh * 0.07f)
                        close()
                    }, fp("#8e2f24"))
                    c.drawLine(cx - tw / 2, ty, cx + tw / 2, ty,
                        Paper.strokePaint(col("#e8c53a"), 2.5f))
                }
                // detail: lanterns — a red pair flanking the door
                for (s in listOf(-1f, 1f)) {
                    val lx = cx + s * bw * 0.13f
                    val ly = doorTop + bh * 0.10f
                    c.drawOval(RectF(lx - 10, ly - 13, lx + 10, ly + 13), fp("#d43d2a"))
                    c.drawRect(lx - 5, ly - 16, lx + 5, ly - 12, fp("#e8c53a"))
                    c.drawRect(lx - 5, ly + 12, lx + 5, ly + 16, fp("#e8c53a"))
                    if (night) c.drawOval(RectF(lx - 18, ly - 20, lx + 18, ly + 20),
                        fp("#f2a230", 45))
                }
            }
            "SkeetersBar" -> {
                // detail: neon-bar-sign — pink neon over the door, humming
                if (night) c.drawOval(RectF(cx - bw * 0.15f, doorTop - bh * 0.30f,
                    cx + bw * 0.15f, doorTop + bh * 0.02f), fp("#ff5a8a", 40))
                val neon = Paper.strokePaint(col("#ff5a8a"), 3.5f)
                c.drawRoundRect(RectF(cx - bw * 0.11f, doorTop - bh * 0.26f,
                    cx + bw * 0.11f, doorTop - bh * 0.04f), 8f, 8f, neon)
                val np = Paper.textPaint(col("#ff8ab0"), h * 0.021f, bold = true, casual = true)
                np.textAlign = android.graphics.Paint.Align.CENTER
                c.drawText("BAR", cx, doorTop - bh * 0.085f, np)
                // detail: bar-window-glow — dim amber, regulars inside since '97
                for (wx in listOf(left + bw * 0.10f, right - bw * 0.10f - winW)) {
                    c.drawRect(wx, winY, wx + winW, winY + winW, fp("#e8a33a", 120))
                }
            }
            "Bijou" -> {
                // detail: marquee — bulb-lit canopy over the door
                val my = doorTop - bh * 0.06f
                c.drawRect(cx - bw * 0.23f, my - bh * 0.13f, cx + bw * 0.23f, my, fp("#f0e8d0"))
                c.drawRect(cx - bw * 0.23f, my - bh * 0.13f, cx + bw * 0.23f, my,
                    Paper.strokePaint(col("#c0392b"), 3.5f))
                val bulb = fp("#f2c230")
                var bx = cx - bw * 0.21f
                while (bx < cx + bw * 0.22f) {
                    c.drawCircle(bx, my - bh * 0.115f, 3f, bulb)
                    c.drawCircle(bx, my - bh * 0.015f, 3f, bulb)
                    bx += bw * 0.045f
                }
                val mp = Paper.textPaint(col("#c0392b"), h * 0.015f, bold = true, casual = true)
                mp.textAlign = android.graphics.Paint.Align.CENTER
                c.drawText("NOW SHOWING", cx, my - bh * 0.055f, mp)
                // detail: poster-boxes — two framed posters flank the doors
                for (s in listOf(-1f, 1f)) {
                    val px = cx + s * bw * 0.30f
                    c.drawRect(px - bw * 0.055f, doorTop - bh * 0.05f, px + bw * 0.055f,
                        baseY - bh * 0.06f, fp("#e8ecf2"))
                    c.drawRect(px - bw * 0.055f, doorTop - bh * 0.05f, px + bw * 0.055f,
                        baseY - bh * 0.06f, Paper.strokePaint(col("#2a2a3a"), 3f))
                    c.drawCircle(px, doorTop + bh * 0.08f, bw * 0.020f,
                        fp(if (s < 0) "#d43d2a" else "#2a6fd4"))
                    c.drawRect(px - bw * 0.035f, baseY - bh * 0.22f, px + bw * 0.035f,
                        baseY - bh * 0.17f, fp("#3a4a5a"))
                }
            }
            "TomsRhino" -> {
                // detail: nose-sign — the clinic's sign is a nose. Obviously.
                val nx = cx
                val ny = top - bh * 0.16f
                c.drawRect(nx - bw * 0.10f, ny - bh * 0.10f, nx + bw * 0.10f, ny + bh * 0.12f,
                    fp("#f0e8d0"))
                c.drawOval(RectF(nx - bw * 0.052f, ny - bh * 0.09f, nx + bw * 0.010f,
                    ny + bh * 0.09f), fp("#e8a0b0"))
                c.drawOval(RectF(nx - bw * 0.010f, ny - bh * 0.09f, nx + bw * 0.052f,
                    ny + bh * 0.09f), fp("#e8a0b0"))
                c.drawOval(RectF(nx - bw * 0.032f, ny + bh * 0.038f, nx - bw * 0.010f,
                    ny + bh * 0.075f), fp("#8a4a5e"))
                c.drawOval(RectF(nx + bw * 0.010f, ny + bh * 0.038f, nx + bw * 0.032f,
                    ny + bh * 0.075f), fp("#8a4a5e"))
                // detail: before-after-posters — results may vary
                for (s in listOf(-1f, 1f)) {
                    val px = cx + s * bw * 0.30f
                    c.drawRect(px - bw * 0.045f, doorTop, px + bw * 0.045f,
                        baseY - bh * 0.08f, fp("#f0e8d0"))
                    c.drawRect(px - bw * 0.045f, doorTop, px + bw * 0.045f,
                        baseY - bh * 0.08f, Paper.strokePaint(col("#8a4a5e"), 2.5f))
                    c.drawOval(RectF(px - bw * 0.028f, doorTop + bh * 0.10f,
                        px + bw * 0.004f, doorTop + bh * 0.26f), fp("#e8a0b0"))
                    c.drawOval(RectF(px + bw * 0.002f, doorTop + bh * 0.10f,
                        px + bw * 0.028f, doorTop + bh * 0.26f), fp("#e8b7c2"))
                }
            }
            "Bank" -> {
                // detail: dollar-pediment — a stone triangle with the town's net worth
                c.drawPath(Path().apply {
                    moveTo(left - bw * 0.03f, top); lineTo(cx, top - bh * 0.30f)
                    lineTo(right + bw * 0.03f, top); close()
                }, fp("#9aa4ae"))
                val dp = Paper.textPaint(col("#4a5a3a"), h * 0.022f, bold = true, casual = true)
                dp.textAlign = android.graphics.Paint.Align.CENTER
                c.drawText("$", cx, top - bh * 0.05f, dp)
                // detail: bank-columns — four stone columns across the front
                val colP = fp("#d8dde2")
                for (i in 0..3) {
                    val colX = left + bw * (0.10f + i * 0.24f)
                    c.drawRect(colX, top + 4, colX + bw * 0.045f, baseY, colP)
                    c.drawRect(colX - 3, top + 4, colX + bw * 0.045f + 3, top + bh * 0.07f, colP)
                }
                // detail: bank-steps — wide, cold, judgmental
                c.drawRect(cx - bw * 0.20f, baseY, cx + bw * 0.20f, baseY + h * 0.012f, fp("#9aa4ae"))
                c.drawRect(cx - bw * 0.24f, baseY + h * 0.012f, cx + bw * 0.24f,
                    baseY + h * 0.024f, fp("#8a94a0"))
            }
            "PostOffice" -> {
                // detail: post-flag — a small flag by the entrance
                val fx = right - bw * 0.06f
                c.drawLine(fx, top - bh * 0.30f, fx, baseY, Paper.strokePaint(col("#5a5a5a"), 4f))
                c.drawRect(fx, top - bh * 0.30f, fx + bw * 0.09f, top - bh * 0.13f, fp("#d43d2a"))
                c.drawRect(fx, top - bh * 0.30f, fx + bw * 0.038f, top - bh * 0.215f, fp("#2a3a6a"))
                // detail: collection-box — the blue drop box out front
                // (pairs with #12: this is where the morning mail GOES)
                val mx = right + bw * 0.05f
                c.drawRect(mx, baseY - bh * 0.30f, mx + bw * 0.075f, baseY, fp("#2a4a8a"))
                c.drawArc(RectF(mx, baseY - bh * 0.42f, mx + bw * 0.075f, baseY - bh * 0.18f),
                    180f, 180f, false, fp("#2a4a8a"))
                c.drawRect(mx + 6, baseY - bh * 0.24f, mx + bw * 0.075f - 6, baseY - bh * 0.20f,
                    fp("#101c33"))
                val mp = Paper.textPaint("#ffffff", h * 0.010f, bold = true, casual = true)
                mp.textAlign = android.graphics.Paint.Align.CENTER
                c.drawText("MAIL", mx + bw * 0.037f, baseY - bh * 0.06f, mp)
            }
            "FreemansTacos" -> {
                // detail: roof-taco — a giant taco on the roof. Morgan Freeman narrates it.
                val tx = cx
                val ty = top - bh * 0.02f
                c.drawPath(Path().apply {
                    moveTo(tx - bw * 0.13f, ty)
                    arcTo(RectF(tx - bw * 0.13f, ty - bh * 0.30f, tx + bw * 0.13f, ty + bh * 0.26f),
                        180f, 180f, false)
                    close()
                }, fp("#e8b34a"))
                val lettuce = Paper.strokePaint(col("#5aa85a"), 5f)
                var lx = tx - bw * 0.115f
                while (lx < tx + bw * 0.10f) {
                    c.drawLine(lx, ty - bh * 0.145f, lx + bw * 0.016f, ty - bh * 0.20f, lettuce)
                    lx += bw * 0.024f
                }
                val tom = fp("#d43d2a")
                for (i in 0..3) {
                    c.drawCircle(tx - bw * 0.08f + i * bw * 0.05f, ty - bh * 0.175f, 4.5f, tom)
                }
                // detail: taco-menu-board — today's special, narrated daily
                val mx = left - bw * 0.115f
                Paper.crooked(c, 3f, mx, baseY) {
                    c.drawRect(mx - 18, baseY - bh * 0.36f, mx + 18, baseY, fp("#e8b34a"))
                    c.drawRect(mx - 18, baseY - bh * 0.36f, mx + 18, baseY - bh * 0.27f,
                        fp("#d43d2a"))
                    val lp = Paper.strokePaint("#5a4028", 2f)
                    for (i in 0..2) {
                        val ly = baseY - bh * 0.20f + i * bh * 0.07f
                        c.drawLine(mx - 12, ly, mx + 12 - (i % 2) * 8, ly, lp)
                    }
                }
            }
            "Church" -> {
                // detail: clapboard-siding — white wooden parish boards
                val siding = Paper.strokePaint(col("#cfd4d8"), 2f)
                siding.alpha = 110
                var sy = top + bh * 0.12f
                while (sy < baseY - 4) {
                    c.drawLine(left + 4, sy, right - 4, sy, siding)
                    sy += bh * 0.11f
                }
                siding.alpha = 255
                // detail: stained-glass — one tall arched window over the door
                val gx = cx
                val gr = bw * 0.075f
                c.drawPath(Path().apply {
                    moveTo(gx - gr, doorTop - bh * 0.02f)
                    lineTo(gx - gr, doorTop - bh * 0.42f)
                    quadTo(gx, doorTop - bh * 0.72f, gx + gr, doorTop - bh * 0.42f)
                    lineTo(gx + gr, doorTop - bh * 0.02f)
                    close()
                }, fp("#7a9ac8"))
                c.drawRect(gx - gr * 0.34f, doorTop - bh * 0.50f, gx + gr * 0.34f,
                    doorTop - bh * 0.02f, fp("#f2c230", 170))
                c.drawRect(gx - gr, doorTop - bh * 0.24f, gx + gr, doorTop - bh * 0.02f,
                    fp("#c86a9a", 130))
                c.drawPath(Path().apply {
                    moveTo(gx - gr, doorTop - bh * 0.02f)
                    lineTo(gx - gr, doorTop - bh * 0.42f)
                    quadTo(gx, doorTop - bh * 0.72f, gx + gr, doorTop - bh * 0.42f)
                    lineTo(gx + gr, doorTop - bh * 0.02f)
                    close()
                }, Paper.strokePaint(col("#3a3f4a"), 3.5f))
            }
            "CityHall" -> {
                // detail: hall-dome — a little capitol dome on the roofline
                // (v0.16.0b: seated on FacadeGeom's roofline — the
                // old seat math landed it ON the facade sign,
                // clipping "CITY HALL" to fragments at its sides,
                // playtest (d)'s class in a second place)
                val fr = FacadeGeom.facadeRect(loc.kind, w.toFloat(), h.toFloat(), uw)
                val dr = bw * 0.095f
                // the base clears the facade sign's board (its
                // top edge sits at fr.top + 0.13fh - board/2,
                // ~0.085 of the height) with room to spare
                val domeBase = fr.top + fr.height * 0.06f
                c.drawArc(RectF(cx - dr, domeBase - dr, cx + dr, domeBase + dr),
                    180f, 180f, false, fp("#8a94a0"))
                c.drawRect(cx - dr, domeBase - 2, cx + dr, domeBase + fr.height * 0.03f,
                    fp("#8a94a0"))
                c.drawRect(cx - 4, domeBase - dr - 12, cx + 4, domeBase - dr + 2,
                    fp("#6a747e"))
                c.drawCircle(cx, domeBase - dr - 15, 3.5f, fp("#e8c53a"))
                // detail: hall-columns — civic cream, four of them
                val colP = fp("#e8e2d4")
                for (i in 0..3) {
                    val colX = left + bw * (0.12f + i * 0.22f)
                    c.drawRect(colX, top + bh * 0.05f, colX + bw * 0.040f, baseY, colP)
                }
            }
            "Elementary" -> {
                // detail: entrance-steps — broad steps up to the
                // double doors (v0.16.0b: the "name-band" that
                // lettered the school's name a SECOND time across
                // the brick — under the baseline facade sign that
                // already names it — is retired; one name per
                // building, on the building)
                val stw = bw * 0.36f
                c.drawRect(cx - stw * 0.38f, baseY - bh * 0.075f,
                    cx + stw * 0.62f, baseY - bh * 0.038f, fp("#9a9aa2"))
                c.drawRect(cx - stw * 0.50f, baseY - bh * 0.038f,
                    cx + stw * 0.74f, baseY, fp("#8a8a92"))
                // detail: double-doors — the main entrance is a pair
                c.drawRect(cx + bw * 0.085f, doorTop, cx + bw * 0.205f, baseY,
                    fp("#5a3a2a"))
                c.drawRect(cx + bw * 0.085f, doorTop, cx + bw * 0.205f, baseY,
                    Paper.strokePaint(col("#3a2617"), 2.5f))
                c.drawCircle(cx + bw * 0.105f, baseY - bh * 0.22f, 3f, fp("#d8dee6"))
                // detail: playground-slide — recess infrastructure, right side
                val sx = right + bw * 0.05f
                val lad = Paper.strokePaint(col("#8a8a92"), 4f)
                c.drawLine(sx, baseY - bh * 0.52f, sx, baseY, lad)
                c.drawLine(sx + bw * 0.05f, baseY - bh * 0.52f, sx + bw * 0.05f, baseY, lad)
                for (i in 1..3) {
                    val ry = baseY - bh * 0.52f * i / 4f
                    c.drawLine(sx, ry, sx + bw * 0.05f, ry, lad)
                }
                c.drawPath(Path().apply {
                    moveTo(sx - bw * 0.02f, baseY - bh * 0.50f)
                    quadTo(sx - bw * 0.10f, baseY - bh * 0.30f, sx - bw * 0.16f, baseY - bh * 0.04f)
                    lineTo(sx - bw * 0.16f, baseY)
                    quadTo(sx - bw * 0.08f, baseY - bh * 0.28f, sx + bw * 0.01f, baseY - bh * 0.44f)
                    close()
                }, fp("#c0392b"))
            }
            "KidsPark" -> {
                // detail: swing-set — two swings by the court, swaying a little
                val sx = cx - w * 0.33f
                val sy = baseY - h * 0.085f
                val frame = Paper.strokePaint(col("#5a6a72"), 5f)
                c.drawLine(sx - 52, baseY + 26, sx - 40, sy, frame)
                c.drawLine(sx + 52, baseY + 26, sx + 40, sy, frame)
                c.drawLine(sx - 44, sy, sx + 44, sy, frame)
                val sway = kotlin.math.sin(RenderClock.nowMs() * 0.0011f) * 6f
                for (i in 0..1) {
                    val seatX = sx - 22 + i * 40
                    val chain = Paper.strokePaint(col("#3a3f4a"), 2f)
                    c.drawLine(seatX - 7, sy, seatX - 7 + sway, sy + 44, chain)
                    c.drawLine(seatX + 7, sy, seatX + 7 + sway, sy + 44, chain)
                    c.drawRect(seatX - 10 + sway, sy + 44, seatX + 10 + sway, sy + 49,
                        fp("#7a5230"))
                }
                // detail: sandbox — a sand pit with one abandoned bucket
                val bx = cx - w * 0.02f
                val by = h * 0.945f
                c.drawRect(bx - 52, by - 8, bx + 52, by + 8, fp("#8a6a44"))
                c.drawRect(bx - 46, by - 5, bx + 46, by + 5, fp("#e3cf9e"))
                c.drawRect(bx + 14, by - 16, bx + 26, by - 6, fp("#c0392b"))
                c.drawLine(bx + 20, by - 16, bx + 30, by - 20,
                    Paper.strokePaint(col("#7a2436"), 2.5f))
            }
            "StarksPond" -> {
                // detail: dock — planks out over the ice from the left shore
                val dy = baseY + h * 0.028f
                c.drawRect(cx - w * 0.37f, dy, cx - w * 0.20f, dy + 9, fp("#7a5c40"))
                val seam = Paper.strokePaint(col("#5a4030"), 2f)
                for (i in 1..4) {
                    val px = cx - w * 0.37f + i * w * 0.034f
                    c.drawLine(px, dy, px, dy + 9, seam)
                }
                c.drawRect(cx - w * 0.35f, dy + 9, cx - w * 0.33f, dy + 20, fp("#5a4030"))
                c.drawRect(cx - w * 0.23f, dy + 9, cx - w * 0.21f, dy + 20, fp("#5a4030"))
                // detail: cattails — brown tips at the right shore
                for (i in 0..2) {
                    val cxx = cx + w * (0.335f + i * 0.018f)
                    val stem = Paper.strokePaint(col("#4a6b35"), 2.5f)
                    c.drawLine(cxx, baseY + h * 0.055f, cxx + 2, baseY - h * 0.010f, stem)
                    c.drawRect(cxx - 2.5f, baseY - h * 0.032f, cxx + 4.5f, baseY - h * 0.006f,
                        fp("#6b4a2f"))
                }
                // detail: pond-sign — the little wooden name sign
                // (v0.16.0b: the board keeps its composed size —
                // unit width — at its shore position; at world
                // width it was a billboard with postcard text)
                Paper.drawSign(c, cx - w * 0.42f, baseY - h * 0.028f, uw * 0.17f,
                    h * 0.032f, "STARK'S POND", h * 0.013f)
                c.drawRect(cx - w * 0.42f - uw * 0.005f, baseY - h * 0.012f,
                    cx - w * 0.42f + uw * 0.005f, baseY + h * 0.03f, fp("#5a4028"))
            }
            "Mall" -> {
                // detail: sign-pylon — the tall lot sign, left of the doors
                val px = left - bw * 0.10f
                c.drawRect(px - 4, top - bh * 0.42f, px + 4, baseY, fp("#5a6068"))
                c.drawRoundRect(RectF(px - bw * 0.075f, top - bh * 0.78f, px + bw * 0.075f,
                    top - bh * 0.40f), 8f, 8f, fp("#7a4a6a"))
                val pp = Paper.textPaint("#ffffff", h * 0.016f, bold = true, casual = true)
                pp.textAlign = android.graphics.Paint.Align.CENTER
                c.drawText("SP", px, top - bh * 0.60f, pp)
                c.drawText("MALL", px, top - bh * 0.47f, pp)
                // detail: entrance-canopy — a swoop of glass over the doors
                c.drawPath(Path().apply {
                    moveTo(cx - bw * 0.20f, doorTop - bh * 0.02f)
                    quadTo(cx, doorTop - bh * 0.20f, cx + bw * 0.20f, doorTop - bh * 0.02f)
                    lineTo(cx + bw * 0.20f, doorTop + bh * 0.03f)
                    quadTo(cx, doorTop - bh * 0.13f, cx - bw * 0.20f, doorTop + bh * 0.03f)
                    close()
                }, fp("#7ab8c8", 200))
                // detail: parking-stripes — the lot, half empty since forever
                val stripe = fp("#e8e8e8", 170)
                var sx = cx - w * 0.34f
                while (sx < cx + w * 0.34f) {
                    Paper.crooked(c, 18f, sx, h * 0.945f) {
                        c.drawRect(sx, h * 0.925f, sx + 5, h * 0.965f, stripe)
                    }
                    // v0.16.0b: stripe rhythm is a size — unit width
                    sx += uw * 0.085f
                }
            }
            "SoDoSoPa" -> {
                // detail: courtyard-planters — boxed shrubs flank
                // the loft entry (v0.16.0b: the "district-arch"
                // name board that hung here repeated the loft's
                // own name board and crossed its door — retired;
                // one name per building, on the building)
                for (s in listOf(-1f, 1f)) {
                    val px = cx + s * uw * 0.17f
                    c.drawRect(px - uw * 0.045f, baseY - h * 0.030f,
                        px + uw * 0.045f, baseY, fp("#8a6a4a"))
                    c.drawOval(RectF(px - uw * 0.052f, baseY - h * 0.078f,
                        px + uw * 0.052f, baseY - h * 0.026f), fp("#4e7a3a"))
                }
                // detail: string-lights — strung between the loft tops, always on-brand
                val wire = Paper.strokePaint(col("#2a2a2a"), 2f)
                var prevX = cx - uw * 0.30f
                var prevY = baseY - h * 0.125f
                for (i in 1..10) {
                    val x = cx - uw * 0.30f + i * (uw * 0.60f / 10)
                    val y = baseY - h * 0.125f + kotlin.math.sin(i * 0.95f) * 9
                    c.drawLine(prevX, prevY, x, y, wire)
                    c.drawCircle(x, y + 5, 4f, fp(if (night) "#f2c230" else "#e8d9a8"))
                    prevX = x; prevY = y
                }
                // detail: patio-umbrellas — craft everything, outdoors
                for (s in listOf(-1f, 1f)) {
                    val ux = cx + s * w * 0.21f
                    val uy = baseY + h * 0.05f
                    c.drawLine(ux, uy - 34, ux, uy + 12, Paper.strokePaint(col("#3a3f4a"), 3.5f))
                    c.drawPath(Path().apply {
                        moveTo(ux - 30, uy - 30); lineTo(ux + 30, uy - 30); lineTo(ux, uy - 52); close()
                    }, fp(if (s < 0) "#c86a9a" else "#e8932a"))
                }
            }
            "TegridyFarms" -> {
                // detail: silo — watching over the crop since Season 22
                // (v0.16.0b: composed width — at world width the silo
                // out-bulked the fixed-size barn beside it)
                val sx = cx + w * 0.015f
                c.drawRect(sx, baseY - h * 0.185f, sx + uw * 0.045f, baseY, fp("#b9bec4"))
                c.drawArc(RectF(sx, baseY - h * 0.225f, sx + uw * 0.045f, baseY - h * 0.145f),
                    180f, 180f, false, fp("#8a9096"))
                val band = Paper.strokePaint(col("#8a9096"), 2.5f)
                c.drawLine(sx, baseY - h * 0.10f, sx + uw * 0.045f, baseY - h * 0.10f, band)
                c.drawLine(sx, baseY - h * 0.05f, sx + uw * 0.045f, baseY - h * 0.05f, band)
                // detail: farm-fence — post-and-rail along the front forty
                val postP = fp("#6b4a2f")
                var fx = 8f
                while (fx < cx - w * 0.05f) {
                    c.drawRect(fx, h * 0.885f, fx + 6, h * 0.94f, postP)
                    fx += uw * 0.085f
                }
                c.drawRect(0f, h * 0.895f, cx - w * 0.05f, h * 0.902f, postP)
                c.drawRect(0f, h * 0.918f, cx - w * 0.05f, h * 0.925f, postP)
                // detail: hemp-rows — neat green rows running to the horizon
                val rowP = fp("#4a7c3a", 200)
                for (i in 0..4) {
                    val ry = h * (0.945f + i * 0.011f)
                    var rx = cx + w * 0.10f + i * w * 0.012f
                    while (rx < w - 12) {
                        c.drawOval(RectF(rx, ry - 4, rx + 15, ry + 3), rowP)
                        rx += uw * 0.052f
                    }
                }
            }
            "Raisins" -> {
                // detail: orange-roof-band — the brand color, worn loud
                c.drawRect(left - bw * 0.02f, top - bh * 0.02f, right + bw * 0.02f,
                    top + bh * 0.13f, fp("#e07b2a"))
                c.drawRect(left - bw * 0.02f, top + bh * 0.13f, right + bw * 0.02f,
                    top + bh * 0.17f, fp("#f0e8d0"))
                // detail: wing-sign — two wings flanking the entrance
                for (s in listOf(-1f, 1f)) {
                    val wx = cx + s * bw * 0.21f
                    val wy = doorTop + bh * 0.16f
                    val wing = fp("#f4f6f8")
                    for (f in 0..2) {
                        c.drawOval(RectF(wx - 16 + f * 7 * s, wy - 12 + f * 6,
                            wx + 8 + f * 7 * s, wy + 2 + f * 6), wing)
                    }
                }
            }
            "YourHouse" -> {
                // detail: new-kid-mailbox — the generic house
                // mailbox (drawn by the facade), lettered for its
                // owner on a little plaque nailed underneath.
                val mbX = left - bw * 0.10f
                val plq = RectF(mbX - bw * 0.075f, baseY - bh * 0.13f,
                    mbX + bw * 0.075f, baseY - bh * 0.015f)
                c.drawRoundRect(plq, 4f, 4f, fp("#6b4a2f"))
                c.drawRoundRect(plq, 4f, 4f, Paper.strokePaint(col("#3a2a1a"), 2f))
                val mp = Paper.textPaint(col("#f4ecd8"), h * 0.0135f,
                    bold = true, casual = true)
                mp.textAlign = Paint.Align.CENTER
                c.drawText("THE NEW KID", mbX, plq.centerY() + h * 0.005f, mp)
                // detail: bike-lean — a bike against the fence,
                // gathering snow. He walks everywhere. Everywhere.
                val bx = right + bw * 0.03f
                val by = baseY + h * 0.012f
                val tire = Paper.strokePaint(col("#23262b"), 3.5f)
                c.drawCircle(bx, by - bh * 0.09f, bh * 0.09f, tire)
                c.drawCircle(bx + bw * 0.115f, by - bh * 0.09f, bh * 0.09f, tire)
                val frame = Paper.strokePaint(col("#2a7a4a"), 3.5f)
                c.drawLine(bx, by - bh * 0.09f, bx + bw * 0.055f, by - bh * 0.185f, frame)
                c.drawLine(bx + bw * 0.055f, by - bh * 0.185f, bx + bw * 0.115f, by - bh * 0.09f, frame)
                c.drawLine(bx + bw * 0.055f, by - bh * 0.185f, bx + bw * 0.05f, by - bh * 0.09f, frame)
                c.drawLine(bx + bw * 0.045f, by - bh * 0.20f, bx + bw * 0.075f, by - bh * 0.20f, frame)
                // detail: welcome-mat — at the door, managing
                // expectations honestly.
                val mat = RectF(cx - bw * 0.085f, baseY + h * 0.006f,
                    cx + bw * 0.085f, baseY + h * 0.034f)
                c.drawRoundRect(mat, 4f, 4f, fp("#7a4a3a"))
                val wp = Paper.textPaint(col("#f4ecd8"), h * 0.0125f,
                    bold = true, casual = true)
                wp.textAlign = Paint.Align.CENTER
                c.drawText("WELCOME-ISH", cx, mat.centerY() + h * 0.0048f, wp)
            }
        }
    }

    // ============================================================
    // v0.3.0a — scene parity pass #3: exteriors & light.
    // Two registered layers join the stack:
    //
    //  drawSeasonDetails — seasonal exterior dressing beyond
    //  snow cover (LOC_SEASON_DETAILS in GameData; one token
    //  per season per location, marked `// season: <token>`;
    //  the gate is SceneLight.seasonTokensFor's prefix rule,
    //  and this function only ever draws the current season's
    //  pieces). Own Random stream, like the light layer, so
    //  passes #1/#2 keep their exact draws.
    //
    //  drawLightLayer — the dusk/night LIGHT layer
    //  (LOC_LIGHT_DETAILS; marked `// light: <token>`). Drawn
    //  only when SceneLight.lightLevel > 0 (dusk 0.65, night
    //  1.0) and last, so pools and halos sit over the scene.
    //  Window overlays respect the facade's own occupancy
    //  rules — an empty house stays dark here too (#24).
    // ============================================================
    private fun drawSeasonDetails(c: Canvas, w: Int, h: Int, loc: Location,
                                  rnd: Random, season: Season, night: Boolean,
                                  weatherId: String) {
        val cx = w / 2f
        val baseY = h * 0.66f
        // v0.16.0b: unit-width facade zone (see drawParityDetails)
        val bw = SceneScale.unitW(w.toFloat(), loc, false) *
            FacadeGeom.bodyWFrac(loc.kind)
        val bh = when (loc.kind) {
            "house" -> h * 0.20f; "shop" -> h * 0.22f; "civic" -> h * 0.21f
            "school" -> h * 0.20f; else -> h * 0.10f
        }
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        val left = cx - bw / 2
        val right = cx + bw / 2
        val top = baseY - bh
        val doorTop = baseY - bh * 0.45f
        val winY = top + bh * 0.30f
        val winW = bw * 0.11f
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) = Paper.strokePaint(col(hex), sw, alpha)
        fun rr(l: Float, t: Float, r: Float, b: Float, p: Paint) =
            c.drawRect(RectF(l, t, r, b), p)
        fun ov(l: Float, t: Float, r: Float, b: Float, p: Paint) =
            c.drawOval(RectF(l, t, r, b), p)
        fun leafPile(x: Float, y: Float, s: Float) {
            ov(x - 34 * s, y - 12 * s, x + 34 * s, y + 10 * s, fp("#b8641e"))
            ov(x - 24 * s, y - 20 * s, x + 16 * s, y + 4 * s, fp("#d88a2a"))
            ov(x - 6 * s, y - 16 * s, x + 30 * s, y + 6 * s, fp("#e8a33a"))
            val lp = fp("#8a4a1f")
            repeat(4) {
                val lx = x + (rnd.nextFloat() - 0.5f) * 52 * s
                Paper.crooked(c, rnd.nextFloat() * 50 - 25, lx, y) {
                    c.drawRect(lx - 4, y - 2, lx + 4, y + 2, lp)
                }
            }
        }
        fun leafBag(x: Float) {
            rr(x, baseY - bh * 0.17f, x + bw * 0.062f, baseY, fp("#8a6a3a"))
            c.drawPath(Path().apply {
                moveTo(x + bw * 0.012f, baseY - bh * 0.17f)
                lineTo(x + bw * 0.031f, baseY - bh * 0.23f)
                lineTo(x + bw * 0.050f, baseY - bh * 0.17f); close()
            }, fp("#8a6a3a"))
            ov(x + bw * 0.008f, baseY - bh * 0.20f, x + bw * 0.054f,
                baseY - bh * 0.13f, fp("#d88a2a"))
        }
        fun icicles(x0: Float, y: Float, n: Int, spacing: Float, len: Float,
                    uniform: Boolean = false) {
            val ice = fp("#dff2ff", 235)
            for (i in 0 until n) {
                val x = x0 + i * spacing
                val l = if (uniform) len else len * (0.7f + 0.5f * ((i * 37 % 5) / 4f))
                c.drawPath(Path().apply {
                    moveTo(x - 4, y); lineTo(x + 4, y); lineTo(x, y + l); close()
                }, ice)
            }
        }
        fun snowCap(x0: Float, x1: Float, y: Float) =
            c.drawRoundRect(RectF(x0, y - 7, x1, y + 3), 5f, 5f, fp("#ffffff", 235))
        fun puddle(x: Float, y: Float, rx: Float, hex: String) {
            ov(x - rx, y - rx * 0.22f, x + rx, y + rx * 0.22f, fp(hex, 205))
            c.drawLine(x - rx * 0.5f, y, x + rx * 0.3f, y, sp("#ffffff", 2f, 80))
        }
        fun sprinkler(x: Float, y: Float) {
            rr(x - 3, y - 8, x + 3, y, fp("#5a6068"))
            val dp = fp("#a8d4f0", 200)
            for (i in -2..2) {
                val dx = i * 9f
                c.drawCircle(x + dx, y - 16 - (2 - kotlin.math.abs(i)) * 4f, 2.2f, dp)
                c.drawCircle(x + dx * 1.5f, y - 4, 2f, dp)
            }
        }
        fun tulip(x: Float, y: Float, hex: String) {
            c.drawLine(x, y, x, y - 14, sp("#3f6b35", 2.5f))
            c.drawCircle(x, y - 17, 4.5f, fp(hex))
        }
        fun grassTuft(x: Float, y: Float, s: Float) {
            val gp = sp("#4a7c3a", 2.5f)
            c.drawLine(x, y, x - 5 * s, y - 13 * s, gp)
            c.drawLine(x, y, x, y - 16 * s, gp)
            c.drawLine(x, y, x + 5 * s, y - 12 * s, gp)
        }
        fun coneShrub(x: Float) {
            c.drawPath(Path().apply {
                moveTo(x - 14, baseY); lineTo(x + 14, baseY); lineTo(x, baseY - 46); close()
            }, fp("#3f6b35"))
        }

        when (loc.id) {
            "KennysHouse" -> when (season) {
                Season.SPRING -> { // season: spring-mud-puddle
                    puddle(left - uw * 0.02f, baseY + h * 0.075f, uw * 0.070f, "#7a5a36")
                    puddle(left + bw * 0.30f, baseY + h * 0.045f, uw * 0.038f, "#6a4e30")
                }
                Season.SUMMER -> { // season: summer-sad-sprinkler
                    sprinkler(cx + uw * 0.21f, baseY + h * 0.055f)
                }
                Season.FALL -> { // season: autumn-leaf-drift
                    leafPile(left - uw * 0.03f, baseY + h * 0.05f, 1f)
                }
                Season.WINTER -> { // season: winter-porch-icicles
                    icicles(left + bw * 0.05f, top + bh * 0.56f, 5, bw * 0.075f, bh * 0.17f)
                }
            }
            "ButtersHouse" -> when (season) {
                Season.SPRING -> { // season: spring-tulip-row
                    val cols = listOf("#d43d2a", "#f2c230", "#e87a8a")
                    for (i in 0..5) tulip(left - bw * 0.04f + i * bw * 0.20f,
                        baseY + h * 0.072f, cols[i % 3])
                }
                Season.SUMMER -> { // season: summer-even-sprinkler
                    sprinkler(cx, baseY + h * 0.078f)
                }
                Season.FALL -> { // season: autumn-leaf-bags
                    for (i in 0..2) leafBag(right + bw * 0.03f + i * bw * 0.085f)
                }
                Season.WINTER -> { // season: winter-even-icicles
                    icicles(left + 10f, top, 7, (bw - 20f) / 6f, bh * 0.14f, uniform = true)
                }
            }
            "CartmanHouse" -> when (season) {
                Season.SPRING -> { // season: spring-melt-rivulet
                    val rp = sp("#7a9cc0", 3f, 150)
                    c.drawLine(right + bw * 0.10f, baseY + 4, right + bw * 0.15f, h * 0.86f, rp)
                    c.drawLine(right + bw * 0.15f, h * 0.86f, right + bw * 0.12f, h * 0.905f, rp)
                    c.drawLine(right + bw * 0.12f, h * 0.905f, right + bw * 0.22f, h * 0.945f, rp)
                    puddle(right + bw * 0.24f, h * 0.952f, uw * 0.035f, "#7a9cc0")
                }
                Season.SUMMER -> { // season: summer-kiddie-pool
                    val px = left - bw * 0.17f
                    val py = baseY + h * 0.082f
                    ov(px - uw * 0.075f, py - 13, px + uw * 0.075f, py + 15, fp("#3a9ad4"))
                    ov(px - uw * 0.058f, py - 8, px + uw * 0.058f, py + 10, fp("#7ac8e8"))
                    c.drawCircle(px + uw * 0.045f, py - 4, 5.5f, fp("#e8b34a"))
                }
                Season.FALL -> { // season: autumn-leaf-pile
                    leafPile(cx + uw * 0.10f, baseY + h * 0.055f, 1.5f)
                }
                Season.WINTER -> { // season: winter-roof-snowload
                    val rp = sp("#ffffff", 8f, 235)
                    c.drawLine(left - bw * 0.06f, top, cx, top - bh * 0.55f, rp)
                    c.drawLine(cx, top - bh * 0.55f, right + bw * 0.06f, top, rp)
                    snowCap(cx - bw * 0.09f, cx + bw * 0.09f, top - bh * 0.55f + 2)
                }
            }
            "StanHouse" -> when (season) {
                Season.SPRING -> { // season: spring-cleanout-boxes
                    rr(right + bw * 0.04f, baseY - bh * 0.20f, right + bw * 0.16f, baseY,
                        fp("#b08c58"))
                    rr(right + bw * 0.06f, baseY - bh * 0.38f, right + bw * 0.15f,
                        baseY - bh * 0.20f, fp("#a37f4e"))
                    rr(right + bw * 0.19f, baseY - bh * 0.17f, right + bw * 0.29f, baseY,
                        fp("#b08c58"))
                }
                Season.SUMMER -> { // season: summer-bike-lean
                    val bx = right + bw * 0.20f
                    val wp = sp("#2a2a2a", 3f)
                    c.drawCircle(bx, baseY - 11, 11f, wp)
                    c.drawCircle(bx + 32, baseY - 11, 11f, wp)
                    val fr = sp("#c0392b", 3f)
                    c.drawLine(bx, baseY - 11, bx + 14, baseY - 26, fr)
                    c.drawLine(bx + 14, baseY - 26, bx + 32, baseY - 11, fr)
                    c.drawLine(bx + 14, baseY - 26, bx + 12, baseY - 11, fr)
                }
                Season.FALL -> { // season: autumn-firewood-stack
                    for (r in 0..1) for (ci in 0..2) {
                        val lx = left - bw * 0.13f + ci * 15f
                        val ly = baseY - 8 - r * 14f
                        c.drawCircle(lx, ly.toFloat(), 7f, fp("#7a5230"))
                        c.drawCircle(lx, ly.toFloat(), 3.5f, fp("#c8a878"))
                    }
                }
                Season.WINTER -> { // season: winter-garage-icicles
                    icicles(right + 6f, top + bh * 0.16f, 5, bw * 0.065f, bh * 0.15f)
                }
            }
            "KyleHouse" -> when (season) {
                Season.SPRING -> { // season: spring-garden-rows
                    for (i in 0..2) {
                        val y = baseY + h * (0.032f + i * 0.022f)
                        rr(left - bw * 0.32f, y, left - bw * 0.06f, y + 6, fp("#5a4030"))
                        for (d in 0..3) c.drawCircle(
                            left - bw * (0.27f - d * 0.065f), y - 2, 2.6f, fp("#7ac74a"))
                    }
                }
                Season.SUMMER -> { // season: summer-veggie-stakes
                    for (i in 0..3) {
                        val x = left - bw * 0.29f + i * bw * 0.075f
                        c.drawLine(x, baseY + h * 0.078f, x, baseY + h * 0.006f,
                            sp("#8a6a44", 2.5f))
                        ov(x - 9, baseY + h * 0.004f, x + 9, baseY + h * 0.034f, fp("#4a7c3a"))
                        if (i % 2 == 0) c.drawCircle(x + 4, baseY + h * 0.024f, 3.2f,
                            fp("#d43d2a"))
                    }
                }
                Season.FALL -> { // season: autumn-bagged-leaves
                    leafBag(left - bw * 0.30f)
                    leafBag(left - bw * 0.20f)
                }
                Season.WINTER -> { // season: winter-frost-windows
                    val frr = sp("#eef4ff", 2f, 190)
                    for (wx in listOf(left + bw * 0.10f, right - bw * 0.10f - winW)) {
                        val l = winW * 0.32f
                        c.drawLine(wx, winY + l, wx, winY, frr)
                        c.drawLine(wx, winY, wx + l, winY, frr)
                        c.drawLine(wx + winW - l, winY, wx + winW, winY, frr)
                        c.drawLine(wx + winW, winY, wx + winW, winY + l, frr)
                        c.drawLine(wx, winY + winW - l, wx, winY + winW, frr)
                        c.drawLine(wx, winY + winW, wx + l, winY + winW, frr)
                        c.drawLine(wx + winW - l, winY + winW, wx + winW, winY + winW, frr)
                        c.drawLine(wx + winW, winY + winW, wx + winW, winY + winW - l, frr)
                    }
                }
            }
            "TolkienHouse" -> when (season) {
                Season.SPRING -> { // season: spring-topiary-buds
                    for (gx in listOf(left - bw * 0.17f, right + bw * 0.17f)) {
                        coneShrub(gx)
                        val bp = fp("#7ac74a", 220)
                        for (d in 0..4) c.drawCircle(
                            gx + (d - 2) * 5f, baseY - 12 - (d % 3) * 11f, 3f, bp)
                    }
                }
                Season.SUMMER -> { // season: summer-pool-glint
                    rr(cx + bw * 0.14f, baseY + h * 0.030f, cx + bw * 0.34f,
                        baseY + h * 0.052f, fp("#7ac8e8", 220))
                    val gp = sp("#ffffff", 2f, 170)
                    c.drawLine(cx + bw * 0.17f, baseY + h * 0.038f, cx + bw * 0.23f,
                        baseY + h * 0.038f, gp)
                    c.drawLine(cx + bw * 0.25f, baseY + h * 0.044f, cx + bw * 0.30f,
                        baseY + h * 0.044f, gp)
                }
                Season.FALL -> { // season: autumn-amber-topiary
                    for (gx in listOf(left - bw * 0.17f, right + bw * 0.17f)) {
                        coneShrub(gx)
                        c.drawPath(Path().apply {
                            moveTo(gx - 14, baseY); lineTo(gx + 14, baseY)
                            lineTo(gx, baseY - 46); close()
                        }, fp("#e07b2a", 110))
                        ov(gx - 18, baseY - 5, gx + 18, baseY + 5, fp("#b8641e", 160))
                    }
                }
                Season.WINTER -> { // season: winter-gate-frost
                    snowCap(left - bw * 0.17f - 8, left - bw * 0.17f + 8, baseY - bh * 0.85f)
                    snowCap(right + bw * 0.17f - 8, right + bw * 0.17f + 8, baseY - bh * 0.85f)
                    c.drawPath(Path().apply {
                        moveTo(left - bw * 0.17f, baseY - bh * 0.85f)
                        quadTo(cx, baseY - bh * 1.18f, right + bw * 0.17f, baseY - bh * 0.85f)
                    }, sp("#ffffff", 5f, 200))
                }
            }
            "BusStop" -> when (season) {
                Season.SPRING -> { // season: spring-bench-puddle
                    puddle(cx - uw * 0.22f, baseY + 68, uw * 0.075f, "#7a9cc0")
                }
                Season.SUMMER -> { // season: summer-tall-grass
                    for (i in 0..5) grassTuft(w * (0.08f + i * 0.16f) +
                        (rnd.nextFloat() - 0.5f) * 20, h * 0.90f, 1.1f)
                }
                Season.FALL -> { // season: autumn-bench-leaves
                    leafPile(cx - uw * 0.22f, baseY + 60, 0.8f)
                    ov(cx - uw * 0.28f, baseY + 26, cx - uw * 0.17f, baseY + 36, fp("#d88a2a", 170))
                }
                Season.WINTER -> { // season: winter-sign-icicles
                    icicles(cx + uw * 0.24f - uw * 0.09f, baseY - 62 + h * 0.018f, 4,
                        uw * 0.05f, h * 0.030f)
                }
            }
            "MainStreet" -> when (season) {
                // v0.16.0b: these branches dressed the RETIRED
                // storefront-row's fake shops (spring boxes on their
                // windows, winter caps on their rooflines, summer
                // baskets on the retired town-banner's wire). They
                // now dress the real diner front instead.
                Season.SPRING -> { // season: spring-flower-boxes
                    val cols = listOf("#d43d2a", "#f2c230", "#e87a8a")
                    val sr = FacadeGeom.structureRect(
                        TownBaseline.of(loc.id)?.structure ?: "hut",
                        w.toFloat(), h.toFloat(),
                        SceneScale.unitW(w.toFloat(), loc, false))
                    for (s in listOf(-1f, 1f)) {
                        val bx = sr.cx + s * sr.width * 0.30f
                        val bwBox = uw * 0.075f
                        rr(bx - bwBox / 2f, baseY - h * 0.020f, bx + bwBox / 2f,
                            baseY - h * 0.006f, fp("#5c4030"))
                        for (d in 0..2) c.drawCircle(bx - bwBox * 0.28f + d * bwBox * 0.28f,
                            baseY - h * 0.023f, 3.2f, fp(cols[d % 3]))
                    }
                }
                Season.SUMMER -> { // season: summer-hanging-baskets
                    val sr = FacadeGeom.structureRect(
                        TownBaseline.of(loc.id)?.structure ?: "hut",
                        w.toFloat(), h.toFloat(),
                        SceneScale.unitW(w.toFloat(), loc, false))
                    val awnY = sr.top + sr.height * 0.16f
                    for (px in listOf(sr.cx - uw * 0.30f, sr.cx + uw * 0.30f)) {
                        c.drawLine(px, awnY, px, awnY + h * 0.025f, sp("#3a3f4a", 3f))
                        ov(px - 15, awnY + h * 0.025f, px + 15, awnY + h * 0.055f, fp("#7a5230"))
                        val cols = listOf("#e87a8a", "#f2c230", "#d43d2a")
                        for (d in 0..3) c.drawCircle(px - 10 + d * 7f, awnY + h * 0.022f, 3.4f,
                            fp(cols[d % 3]))
                    }
                }
                Season.FALL -> { // season: autumn-street-piles
                    leafPile(cx - uw * 0.30f, baseY + h * 0.032f, 0.9f)
                    leafPile(cx + uw * 0.05f, baseY + h * 0.040f, 1.1f)
                    leafPile(cx + uw * 0.33f, baseY + h * 0.030f, 0.8f)
                }
                Season.WINTER -> { // season: winter-roofline-snow
                    val sr = FacadeGeom.structureRect(
                        TownBaseline.of(loc.id)?.structure ?: "hut",
                        w.toFloat(), h.toFloat(),
                        SceneScale.unitW(w.toFloat(), loc, false))
                    snowCap(sr.left, sr.right, sr.top)
                }
            }
            "TweekBros" -> when (season) {
                Season.SPRING -> { // season: spring-patio-table
                    val tx = left - bw * 0.30f
                    val ty = baseY + h * 0.045f
                    c.drawLine(tx, ty, tx, ty + 16, sp("#3a3f4a", 3f))
                    ov(tx - 17, ty - 6, tx + 17, ty + 5, fp("#e8e2d4"))
                    c.drawCircle(tx + 6, ty - 4, 3.5f, fp("#7a5230"))
                }
                Season.SUMMER -> { // season: summer-iced-board
                    val mx = left - bw * 0.13f
                    Paper.crooked(c, -9f, mx - 16, baseY - bh * 0.10f) {
                        rr(mx - 32, baseY - bh * 0.24f, mx - 2, baseY, fp("#7ac8e8"))
                        rr(mx - 26, baseY - bh * 0.19f, mx - 8, baseY - bh * 0.08f,
                            fp("#f0f6fa"))
                    }
                }
                Season.FALL -> { // season: autumn-leaf-mat
                    leafPile(cx, baseY + h * 0.020f, 0.7f)
                }
                Season.WINTER -> { // season: winter-cup-icicles
                    val bx = right + bw * 0.055f
                    icicles(bx - 10, top + bh * 0.52f, 4, 16f, bh * 0.13f)
                }
            }
            "JimbosGuns" -> when (season) {
                Season.SPRING -> { // season: spring-mud-prints
                    val mp = fp("#7a5a36", 170)
                    for (i in 0..3) ov(left - bw * 0.02f + i * bw * 0.075f,
                        baseY + h * 0.028f + (i % 2) * 7, left + bw * 0.015f + i * bw * 0.075f,
                        baseY + h * 0.042f + (i % 2) * 7, mp)
                }
                Season.SUMMER -> { // season: summer-window-fan
                    val fx = left + bw * 0.10f + winW / 2
                    val fy = winY + winW / 2
                    c.drawCircle(fx, fy, winW * 0.34f, sp("#d8dee6", 3f))
                    val bl = sp("#d8dee6", 2.5f)
                    for (a in 0..2) {
                        val ang = a * Math.PI * 2 / 3 + 0.5
                        c.drawLine(fx, fy, (fx + winW * 0.28f * kotlin.math.cos(ang)).toFloat(),
                            (fy + winW * 0.28f * kotlin.math.sin(ang)).toFloat(), bl)
                    }
                    c.drawCircle(fx, fy, 3f, fp("#d8dee6"))
                }
                Season.FALL -> { // season: autumn-shop-leaves
                    leafPile(right + bw * 0.05f, baseY + h * 0.04f, 0.9f)
                }
                Season.WINTER -> { // season: winter-wheel-snow
                    val wx = left - bw * 0.075f
                    val wy = baseY - bh * 0.10f
                    val wr = bh * 0.20f
                    c.drawArc(RectF(wx - wr, wy - wr, wx + wr, wy + wr), 195f, 150f,
                        false, sp("#ffffff", 6f, 235))
                }
            }
            "CityWok" -> when (season) {
                Season.SPRING -> { // season: spring-steam-vents
                    val vp = sp("#ffffff", 2.5f, 150)
                    for (s in listOf(-0.08f, 0.05f)) {
                        val vx = cx + bw * s
                        c.drawLine(vx, top - bh * 0.30f, vx + 4, top - bh * 0.38f, vp)
                        c.drawLine(vx + 4, top - bh * 0.38f, vx, top - bh * 0.45f, vp)
                    }
                }
                Season.SUMMER -> { // season: summer-shade-band
                    rr(left, top + bh * 0.02f, right, top + bh * 0.10f, fp("#000000", 46))
                }
                Season.FALL -> { // season: autumn-amber-lanterns
                    for (s in listOf(-1f, 1f)) {
                        val lx = cx + s * bw * 0.13f
                        val ly = doorTop + bh * 0.10f
                        ov(lx - 13, ly - 16, lx + 13, ly + 16, fp("#ffb84a", 95))
                        c.drawCircle(lx, ly, 4f, fp("#ffd76a"))
                    }
                }
                Season.WINTER -> { // season: winter-eave-icicles
                    icicles(left + bw * 0.02f, top - bh * 0.02f, 9, bw * 0.115f, bh * 0.12f)
                }
            }
            "SkeetersBar" -> when (season) {
                Season.SPRING -> { // season: spring-open-window
                    val wx = left + bw * 0.10f
                    rr(wx, winY + winW * 0.45f, wx + winW, winY + winW, fp("#10161f", 220))
                    Paper.crooked(c, -16f, wx + winW / 2, winY + winW * 0.2f) {
                        rr(wx, winY - 3, wx + winW, winY + winW * 0.42f, fp("#8ab8d8", 200))
                    }
                }
                Season.SUMMER -> { // season: summer-patio-crates
                    val kx = right + bw * 0.05f
                    rr(kx, baseY - bh * 0.20f, kx + bw * 0.075f, baseY, fp("#8a6a44"))
                    rr(kx + bw * 0.085f, baseY - bh * 0.20f, kx + bw * 0.16f, baseY,
                        fp("#96754c"))
                    rr(kx - 2, baseY - bh * 0.26f, kx + bw * 0.165f, baseY - bh * 0.21f,
                        fp("#7a5a38"))
                }
                Season.FALL -> { // season: autumn-skeeter-leaves
                    leafPile(cx - bw * 0.20f, baseY + h * 0.032f, 0.85f)
                }
                Season.WINTER -> { // season: winter-neon-frost
                    val fr = sp("#eef4ff", 2f, 170)
                    val nl = cx - bw * 0.11f; val nr = cx + bw * 0.11f
                    val nt = doorTop - bh * 0.26f; val nb = doorTop - bh * 0.04f
                    val l = bw * 0.035f
                    c.drawLine(nl, nt + l, nl, nt, fr); c.drawLine(nl, nt, nl + l, nt, fr)
                    c.drawLine(nr - l, nt, nr, nt, fr); c.drawLine(nr, nt, nr, nt + l, fr)
                    c.drawLine(nl, nb - l, nl, nb, fr); c.drawLine(nl, nb, nl + l, nb, fr)
                    c.drawLine(nr - l, nb, nr, nb, fr); c.drawLine(nr, nb, nr, nb - l, fr)
                }
            }
            "Bijou" -> when (season) {
                Season.SPRING -> { // season: spring-fresh-poster
                    val px = cx - bw * 0.30f
                    rr(px - bw * 0.055f, doorTop - bh * 0.05f, px + bw * 0.055f,
                        baseY - bh * 0.06f, sp("#7ac74a", 4.5f))
                }
                Season.SUMMER -> { // season: summer-matinee-ropes
                    for (s in listOf(-1f, 1f)) {
                        val px = cx + s * bw * 0.16f
                        rr(px - 3, baseY - bh * 0.22f, px + 3, baseY, fp("#8a8a92"))
                        c.drawCircle(px, baseY - bh * 0.23f, 4.5f, fp("#c9a227"))
                    }
                    c.drawPath(Path().apply {
                        moveTo(cx - bw * 0.16f, baseY - bh * 0.20f)
                        quadTo(cx, baseY - bh * 0.11f, cx + bw * 0.16f, baseY - bh * 0.20f)
                    }, sp("#c0392b", 3.5f))
                }
                Season.FALL -> { // season: autumn-fall-poster
                    val px = cx + bw * 0.30f
                    rr(px - bw * 0.055f, doorTop - bh * 0.05f, px + bw * 0.055f,
                        baseY - bh * 0.06f, sp("#e07b2a", 4.5f))
                }
                Season.WINTER -> { // season: winter-marquee-snow
                    val my = doorTop - bh * 0.06f
                    snowCap(cx - bw * 0.23f, cx + bw * 0.23f, my - bh * 0.13f)
                }
            }
            "TomsRhino" -> when (season) {
                Season.SPRING -> { // season: spring-flower-pot
                    val px = left - bw * 0.03f
                    c.drawPath(Path().apply {
                        moveTo(px - 11, baseY - 16); lineTo(px + 11, baseY - 16)
                        lineTo(px + 8, baseY); lineTo(px - 8, baseY); close()
                    }, fp("#b85c2a"))
                    tulip(px - 6, baseY - 14, "#e87a8a")
                    tulip(px + 5, baseY - 15, "#f2c230")
                }
                Season.SUMMER -> { // season: summer-sunshade
                    c.drawPath(Path().apply {
                        moveTo(cx - bw * 0.20f, doorTop - bh * 0.32f)
                        lineTo(cx + bw * 0.20f, doorTop - bh * 0.32f)
                        lineTo(cx, doorTop - bh * 0.12f); close()
                    }, fp("#f0e8d0", 150))
                }
                Season.FALL -> { // season: autumn-clinic-leaves
                    leafPile(left + bw * 0.02f, baseY + h * 0.035f, 0.85f)
                }
                Season.WINTER -> { // season: winter-nose-icicle
                    val ny = top - bh * 0.16f
                    c.drawPath(Path().apply {
                        moveTo(cx - 4.5f, ny + bh * 0.10f)
                        lineTo(cx + 4.5f, ny + bh * 0.10f)
                        lineTo(cx + 1, ny + bh * 0.44f); close()
                    }, fp("#dff2ff", 240))
                }
            }
            "Bank" -> when (season) {
                Season.SPRING -> { // season: spring-planters
                    for (s in listOf(-1f, 1f)) {
                        val px = cx + s * bw * 0.29f
                        rr(px - 14, baseY - 13, px + 14, baseY, fp("#7a7f85"))
                        ov(px - 12, baseY - 24, px + 12, baseY - 10, fp("#4a7c3a"))
                    }
                }
                Season.SUMMER -> { // season: summer-column-shade
                    val sh = fp("#000000", 34)
                    for (i in 0..3) {
                        val colX = left + bw * (0.10f + i * 0.24f)
                        c.drawPath(Path().apply {
                            moveTo(colX, baseY); lineTo(colX + bw * 0.045f, baseY)
                            lineTo(colX + bw * 0.105f, baseY + h * 0.030f)
                            lineTo(colX + bw * 0.055f, baseY + h * 0.030f); close()
                        }, sh)
                    }
                }
                Season.FALL -> { // season: autumn-leaf-corners
                    leafPile(left + bw * 0.02f, baseY + h * 0.032f, 0.8f)
                    leafPile(right - bw * 0.02f, baseY + h * 0.032f, 0.8f)
                }
                Season.WINTER -> { // season: winter-column-caps
                    for (i in 0..3) {
                        val colX = left + bw * (0.10f + i * 0.24f)
                        snowCap(colX - 3, colX + bw * 0.045f + 3, top + 4)
                    }
                }
            }
            "PostOffice" -> when (season) {
                Season.SPRING -> { // season: spring-new-flag
                    val fx = right - bw * 0.06f
                    rr(fx, top - bh * 0.30f, fx + bw * 0.09f, top - bh * 0.13f, fp("#e8483a"))
                    rr(fx, top - bh * 0.30f, fx + bw * 0.038f, top - bh * 0.215f, fp("#3a5aa8"))
                }
                Season.SUMMER -> { // season: summer-open-door
                    rr(cx - bw * 0.07f, doorTop, cx + bw * 0.015f, baseY, fp("#141e2a", 235))
                    rr(cx - bw * 0.005f, doorTop + bh * 0.10f, cx + bw * 0.008f,
                        baseY - bh * 0.20f, fp("#f0e8d0"))
                }
                Season.FALL -> { // season: autumn-post-leaves
                    leafPile(right + bw * 0.03f, baseY + h * 0.032f, 0.85f)
                }
                Season.WINTER -> { // season: winter-box-snowcap
                    val mx = right + bw * 0.05f
                    snowCap(mx, mx + bw * 0.075f, baseY - bh * 0.40f)
                }
            }
            "FreemansTacos" -> when (season) {
                Season.SPRING -> { // season: spring-taco-planter
                    val px = left - bw * 0.03f
                    rr(px - 20, baseY - 12, px + 20, baseY, fp("#94522c"))
                    for (d in 0..3) {
                        val sx = px - 14 + d * 9f
                        c.drawLine(sx, baseY - 11, sx, baseY - 20, sp("#4a7c3a", 2f))
                        c.drawCircle(sx, baseY - 22, 3.4f,
                            fp(if (d % 2 == 0) "#d43d2a" else "#7ac74a"))
                    }
                }
                Season.SUMMER -> { // season: summer-patio-tables
                    for (s in listOf(0.09f, 0.25f)) {
                        val tx = right + bw * s
                        val ty = baseY + h * 0.045f
                        c.drawLine(tx, ty - 26, tx, ty + 10, sp("#3a3f4a", 3f))
                        c.drawPath(Path().apply {
                            moveTo(tx - 24, ty - 24); lineTo(tx + 24, ty - 24)
                            lineTo(tx, ty - 44); close()
                        }, fp("#e8932a"))
                        ov(tx - 13, ty + 6, tx + 13, ty + 13, fp("#e8e2d4"))
                    }
                }
                Season.FALL -> { // season: autumn-freeman-leaves
                    leafPile(left - bw * 0.05f, baseY + h * 0.04f, 0.9f)
                }
                Season.WINTER -> { // season: winter-taco-snowcap
                    val tx = cx
                    val ty = top - bh * 0.02f
                    c.drawArc(RectF(tx - bw * 0.13f, ty - bh * 0.30f, tx + bw * 0.13f,
                        ty + bh * 0.26f), 180f, 180f, false, sp("#ffffff", 7f, 240))
                }
            }
            "Church" -> when (season) {
                Season.SPRING -> { // season: spring-lily-bed
                    for (s in listOf(-1f, 1f)) for (d in 0..1) {
                        val lx = cx + s * bw * (0.11f + d * 0.035f)
                        c.drawLine(lx, baseY, lx, baseY - 13, sp("#3f6b35", 2.2f))
                        c.drawCircle(lx, baseY - 16, 3.8f, fp("#ffffff"))
                    }
                }
                Season.SUMMER -> { // season: summer-full-flowers
                    val cols = listOf("#d43d2a", "#f2c230", "#e87a8a", "#7ac74a")
                    var fx = left + bw * 0.08f
                    var k = 0
                    while (fx < right - bw * 0.06f) {
                        if (fx < cx - bw * 0.10f || fx > cx + bw * 0.10f) {
                            c.drawCircle(fx, baseY - 4, 3.6f, fp(cols[k % 4]))
                        }
                        fx += bw * 0.055f; k++
                    }
                }
                Season.FALL -> { // season: autumn-leaf-carpet
                    leafPile(cx, baseY + h * 0.026f, 1.35f)
                }
                Season.WINTER -> { // season: winter-steeple-snow
                    snowCap(cx - 10, cx + 10, top - bh * 1.05f)
                    rr(cx - 8, top - bh * 1.19f, cx + 8, top - bh * 1.15f, fp("#ffffff", 230))
                }
            }
            "CityHall" -> when (season) {
                Season.SPRING -> { // season: spring-flags-out
                    val dr = bw * 0.095f
                    for (s in listOf(-1f, 1f)) {
                        val fx = cx + s * (dr + 18)
                        val fy = top - bh * 0.46f
                        c.drawLine(fx, fy, fx, fy - 22, sp("#5a5a5a", 3f))
                        c.drawPath(Path().apply {
                            moveTo(fx, fy - 22); lineTo(fx + s * 15, fy - 18)
                            lineTo(fx, fy - 13); close()
                        }, fp(if (s < 0) "#d43d2a" else "#2a6fd4"))
                    }
                }
                Season.SUMMER -> { // season: summer-flag-flying
                    val dr = bw * 0.095f
                    val fy = top - bh * 0.55f - dr - 15
                    c.drawLine(cx, fy, cx, fy - 26, sp("#5a5a5a", 3f))
                    c.drawPath(Path().apply {
                        moveTo(cx, fy - 26); lineTo(cx + 24, fy - 22)
                        lineTo(cx + 24, fy - 13); lineTo(cx, fy - 10); close()
                    }, fp("#d43d2a"))
                }
                Season.FALL -> { // season: autumn-civic-leaves
                    leafPile(left + bw * 0.04f, baseY + h * 0.032f, 0.9f)
                    leafPile(right - bw * 0.04f, baseY + h * 0.032f, 0.9f)
                }
                Season.WINTER -> { // season: winter-dome-snowcap
                    val dr = bw * 0.095f
                    c.drawArc(RectF(cx - dr, top - bh * 0.55f - dr, cx + dr,
                        top - bh * 0.55f + dr), 180f, 180f, false, sp("#ffffff", 6f, 240))
                }
            }
            "Elementary" -> when (season) {
                Season.SPRING -> { // season: spring-slide-mud
                    val sx = right + bw * 0.05f
                    puddle(sx - bw * 0.16f, baseY + h * 0.022f, bw * 0.07f, "#7a5a36")
                }
                Season.SUMMER -> { // season: summer-field-sprinklers
                    sprinkler(right + bw * 0.12f, baseY + h * 0.055f)
                    sprinkler(left - bw * 0.03f, baseY + h * 0.065f)
                }
                Season.FALL -> { // season: autumn-playground-leaves
                    val sx = right + bw * 0.05f
                    leafPile(sx - bw * 0.05f, baseY + h * 0.032f, 1f)
                }
                Season.WINTER -> { // season: winter-slide-snow
                    val sx = right + bw * 0.05f
                    c.drawLine(sx - bw * 0.02f, baseY - bh * 0.50f, sx - bw * 0.155f,
                        baseY - bh * 0.05f, sp("#ffffff", 6f, 230))
                    snowCap(sx - 2, sx + bw * 0.05f + 2, baseY - bh * 0.52f)
                }
            }
            "KidsPark" -> when (season) {
                Season.SPRING -> { // season: spring-budding-branches
                    val tx = cx - uw * 0.05f
                    rr(tx - 3, baseY - 26, tx + 3, baseY + h * 0.02f, fp("#5d3f24"))
                    val br = sp("#5d3f24", 3f)
                    c.drawLine(tx, baseY - 22, tx - 18, baseY - 44, br)
                    c.drawLine(tx, baseY - 24, tx + 4, baseY - 50, br)
                    c.drawLine(tx, baseY - 20, tx + 19, baseY - 40, br)
                    val bp = fp("#7ac74a", 230)
                    for ((dx, dy) in listOf(-18f to -46f, 4f to -52f, 19f to -42f,
                        -9f to -38f, 12f to -34f)) {
                        c.drawCircle(tx + dx, baseY + dy, 3.4f, bp)
                    }
                }
                Season.SUMMER -> { // season: summer-sprinkler-arch
                    val sx = cx + w * 0.05f
                    val sy = baseY + h * 0.062f
                    sprinkler(sx, sy)
                    val dp = fp("#a8d4f0", 170)
                    for (i in -3..3) {
                        c.drawCircle(sx + i * uw * 0.016f,
                            sy - 26 - (3 - kotlin.math.abs(i)) * 5f, 2.4f, dp)
                        c.drawCircle(sx + i * uw * 0.026f, sy - 6, 2.2f, dp)
                    }
                }
                Season.FALL -> { // season: autumn-orange-canopy
                    val tx = cx - uw * 0.05f
                    rr(tx - 3, baseY - 26, tx + 3, baseY + h * 0.02f, fp("#5d3f24"))
                    ov(tx - 34, baseY - 78, tx + 34, baseY - 24, fp("#e07b2a", 215))
                    ov(tx - 22, baseY - 92, tx + 24, baseY - 40, fp("#d88a2a", 215))
                    ov(tx - 8, baseY - 66, tx + 40, baseY - 22, fp("#b8641e", 200))
                }
                Season.WINTER -> { // season: winter-swing-snow
                    val sx = cx - w * 0.33f
                    val sy = baseY - h * 0.085f
                    snowCap(sx - 46, sx + 46, sy)
                    for (i in 0..1) snowCap(sx - 32 + i * 40, sx - 12 + i * 40, sy + 44)
                }
            }
            "StarksPond" -> when (season) {
                Season.SPRING -> { // season: spring-thaw-cracks
                    ov(cx + uw * 0.02f, baseY + h * 0.026f, cx + uw * 0.20f,
                        baseY + h * 0.082f, fp("#3a6ea8", 190))
                    val cp = sp("#5a8ac8", 2f, 170)
                    c.drawLine(cx - uw * 0.06f, baseY + h * 0.05f, cx + uw * 0.01f,
                        baseY + h * 0.062f, cp)
                    c.drawLine(cx + uw * 0.22f, baseY + h * 0.04f, cx + uw * 0.28f,
                        baseY + h * 0.058f, cp)
                }
                Season.SUMMER -> { // season: summer-lily-pads
                    ov(cx - uw * 0.36f, baseY, cx + uw * 0.36f, baseY + h * 0.10f,
                        fp("#5a94c8", 130))
                    for ((dx, dy, r) in listOf(
                        Triple(-0.14f, 0.045f, 13f), Triple(0.06f, 0.062f, 16f),
                        Triple(0.20f, 0.038f, 11f))) {
                        c.drawCircle(cx + w * dx, baseY + h * dy, r, fp("#4a7c3a"))
                    }
                    c.drawCircle(cx + uw * 0.06f, baseY + h * 0.058f, 4f, fp("#e87a8a"))
                }
                Season.FALL -> { // season: autumn-rust-reeds
                    for (i in 0..2) {
                        val cxx = cx + w * (0.335f + i * 0.018f)
                        rr(cxx - 2.5f, baseY - h * 0.032f, cxx + 4.5f, baseY - h * 0.006f,
                            fp("#b85c2a"))
                    }
                    ov(cx - uw * 0.10f, baseY + h * 0.040f, cx - uw * 0.02f,
                        baseY + h * 0.058f, fp("#d88a2a", 150))
                }
                Season.WINTER -> { // season: winter-ice-holes
                    for ((dx, dy) in listOf(-0.05f to 0.055f, 0.17f to 0.045f)) {
                        c.drawCircle(cx + w * dx, baseY + h * dy, 8f, fp("#22304e"))
                        c.drawCircle(cx + w * dx, baseY + h * dy, 8f, sp("#dfe9f5", 2f, 160))
                    }
                }
            }
            "Mall" -> when (season) {
                Season.SPRING -> { // season: spring-sale-banners
                    rr(left + bw * 0.07f, top + bh * 0.08f, left + bw * 0.115f,
                        top + bh * 0.62f, fp("#7ac74a"))
                    rr(right - bw * 0.115f, top + bh * 0.08f, right - bw * 0.07f,
                        top + bh * 0.62f, fp("#e87a8a"))
                }
                Season.SUMMER -> { // season: summer-shade-sails
                    c.drawPath(Path().apply {
                        moveTo(cx - bw * 0.24f, doorTop - bh * 0.12f)
                        lineTo(cx + bw * 0.24f, doorTop - bh * 0.12f)
                        lineTo(cx, doorTop + bh * 0.02f); close()
                    }, fp("#f0e8d0", 120))
                }
                Season.FALL -> { // season: autumn-lot-leaves
                    leafPile(cx - uw * 0.28f, h * 0.945f, 0.9f)
                    leafPile(cx + uw * 0.24f, h * 0.952f, 0.8f)
                }
                Season.WINTER -> { // season: winter-plow-piles
                    ov(cx - uw * 0.38f, h * 0.905f, cx - uw * 0.20f, h * 0.958f, fp("#eef4ff"))
                    ov(cx - uw * 0.35f, h * 0.895f, cx - uw * 0.24f, h * 0.940f, fp("#ffffff"))
                    ov(cx + uw * 0.20f, h * 0.912f, cx + uw * 0.36f, h * 0.960f, fp("#eef4ff"))
                    ov(cx + uw * 0.23f, h * 0.902f, cx + uw * 0.33f, h * 0.944f, fp("#ffffff"))
                }
            }
            "SoDoSoPa" -> when (season) {
                Season.SPRING -> { // season: spring-planter-boxes
                    for (i in 0..2) {
                        val lx = cx - uw * 0.28f + i * uw * 0.20f
                        val ly = baseY - 100 - i * 12
                        rr(lx - 30, ly - 11, lx + 30, ly, fp("#7a5230"))
                        for (d in 0..3) c.drawCircle(lx - 20 + d * 13f, ly - 13, 3.6f,
                            fp("#7ac74a"))
                    }
                }
                Season.SUMMER -> { // season: summer-rooftop-plants
                    for (i in 0..2) {
                        val lx = cx - uw * 0.28f + i * uw * 0.20f
                        val ly = baseY - 100 - i * 12
                        ov(lx - 26, ly - 24, lx + 2, ly - 4, fp("#4a7c3a"))
                        ov(lx - 2, ly - 28, lx + 27, ly - 6, fp("#5a8a44"))
                        if (i == 1) c.drawCircle(lx + 10, ly - 14, 3.6f, fp("#d43d2a"))
                    }
                }
                Season.FALL -> { // season: autumn-amber-string
                    for (i in 1..10) {
                        val x = cx - uw * 0.30f + i * (uw * 0.60f / 10)
                        val y = baseY - h * 0.125f + kotlin.math.sin(i * 0.95f) * 9
                        c.drawCircle(x, y + 5, 5.5f, fp("#e07b2a", 120))
                    }
                }
                Season.WINTER -> { // season: winter-loft-snowcaps
                    for (i in 0..2) {
                        val lx = cx - uw * 0.28f + i * uw * 0.20f
                        snowCap(lx - 44, lx + 44, baseY - 100 - i * 12)
                    }
                }
            }
            "TegridyFarms" -> when (season) {
                Season.SPRING -> { // season: spring-seedling-rows
                    for (i in 0..4) {
                        val ry = h * (0.945f + i * 0.011f)
                        var rx = cx + uw * 0.10f + i * uw * 0.012f
                        var n = 0
                        while (rx < w - 12 && n < 4) {
                            c.drawCircle(rx + 7, ry - 2, 3f, fp("#7ac74a"))
                            rx += uw * 0.052f; n++
                        }
                    }
                }
                Season.SUMMER -> { // season: summer-tall-crop
                    val rowP = fp("#4a7c3a", 220)
                    for (i in 0..4) {
                        val ry = h * (0.945f + i * 0.011f)
                        var rx = cx + uw * 0.10f + i * uw * 0.012f
                        while (rx < w - 12) {
                            ov(rx, ry - 11, rx + 15, ry + 3, rowP)
                            rx += uw * 0.052f
                        }
                    }
                }
                Season.FALL -> { // season: autumn-harvest-crates
                    val bx = cx + w * 0.18f
                    rr(bx - 96, baseY - 20, bx - 76, baseY, fp("#8a6a44"))
                    rr(bx - 96, baseY - 40, bx - 77, baseY - 21, fp("#96754c"))
                    c.drawCircle(bx + 84, baseY - 10, 11f, fp("#c8a848"))
                    c.drawLine(bx + 76, baseY - 16, bx + 92, baseY - 16, sp("#8a6a3a", 2.5f))
                }
                Season.WINTER -> { // season: winter-fence-snow
                    rr(0f, h * 0.889f, cx - uw * 0.05f, h * 0.896f, fp("#ffffff", 220))
                    rr(0f, h * 0.912f, cx - uw * 0.05f, h * 0.919f, fp("#ffffff", 200))
                    var fx = 8f
                    val cap = fp("#ffffff", 230)
                    while (fx < cx - uw * 0.05f) {
                        c.drawCircle(fx + 3, h * 0.887f, 4.5f, cap)
                        fx += uw * 0.085f
                    }
                }
            }
            "Raisins" -> when (season) {
                Season.SPRING -> { // season: spring-wing-planters
                    for (s in listOf(-1f, 1f)) {
                        val px = cx + s * bw * 0.21f
                        rr(px - 16, baseY - 10, px + 16, baseY, fp("#8a4a3a"))
                        for (d in 0..2) c.drawCircle(px - 9 + d * 9f, baseY - 13, 3.4f,
                            fp("#e87a8a"))
                    }
                }
                Season.SUMMER -> { // season: summer-patio-misters
                    val mp = fp("#cfe8f8", 150)
                    for (i in 0..5) {
                        val mx = cx - bw * 0.14f + i * bw * 0.056f
                        c.drawCircle(mx, doorTop - bh * 0.10f +
                            kotlin.math.sin(i * 1.1f) * 4, 2.4f, mp)
                    }
                }
                Season.FALL -> { // season: autumn-raisins-leaves
                    leafPile(cx + bw * 0.31f, baseY + h * 0.036f, 0.9f)
                }
                Season.WINTER -> { // season: winter-band-frost
                    rr(left - bw * 0.02f, top + bh * 0.095f, right + bw * 0.02f,
                        top + bh * 0.135f, fp("#eef4ff", 150))
                    icicles(left + bw * 0.06f, top + bh * 0.135f, 6, bw * 0.15f, bh * 0.08f)
                }
            }
            "YourHouse" -> when (season) {
                Season.SPRING -> { // season: spring-doormat-flowers
                    for (s in listOf(-1f, 1f)) {
                        val px = cx + s * bw * 0.13f
                        rr(px - 13, baseY - 12, px + 13, baseY, fp("#8a4a3a"))
                        tulip(px - 6, baseY - 12, "#d43d2a")
                        tulip(px + 5, baseY - 12, "#f2c230")
                    }
                }
                Season.SUMMER -> { // season: summer-chalk-driveway
                    val cp = sp("#f4ecd8", 3f, 200)
                    c.drawCircle(cx - bw * 0.24f, baseY + h * 0.055f, uw * 0.016f, cp)
                    for (k in 0..3) {
                        val sx = cx - bw * 0.24f + (k % 2) * uw * 0.030f
                        val sy = baseY + h * 0.052f + (k / 2) * uw * 0.030f
                        rr(sx, sy, sx + uw * 0.024f, sy + uw * 0.024f,
                            sp("#e87a8a", 2.5f, 190))
                    }
                    c.drawLine(cx + bw * 0.16f, baseY + h * 0.075f,
                        cx + bw * 0.26f, baseY + h * 0.045f, sp("#7ab8e8", 3f, 190))
                }
                Season.FALL -> { // season: autumn-pumpkin-porch
                    val px = cx + bw * 0.155f
                    c.drawOval(RectF(px - 17, baseY - 24, px + 17, baseY), fp("#e07b2a"))
                    c.drawOval(RectF(px - 6, baseY - 24, px + 6, baseY), fp("#c96a22"))
                    rr(px - 3, baseY - 30, px + 3, baseY - 24, fp("#4a7a2a"))
                    val qx = cx - bw * 0.165f
                    c.drawOval(RectF(qx - 11, baseY - 16, qx + 11, baseY), fp("#e07b2a"))
                    rr(qx - 2, baseY - 20, qx + 2, baseY - 16, fp("#4a7a2a"))
                }
                Season.WINTER -> { // season: winter-wreath-door
                    val wx = cx
                    val wy = baseY - bh * 0.30f
                    c.drawCircle(wx, wy, bh * 0.068f, sp("#2a6b35", bh * 0.036f))
                    c.drawCircle(wx - bh * 0.045f, wy - bh * 0.045f, 3.2f, fp("#d43d2a"))
                    c.drawCircle(wx + bh * 0.05f, wy + bh * 0.03f, 3.2f, fp("#d43d2a"))
                    c.drawCircle(wx + bh * 0.01f, wy + bh * 0.075f, 3.2f, fp("#f2c230"))
                    snowCap(left - bw * 0.145f, left - bw * 0.055f, baseY - bh * 0.30f)
                }
            }
        }
    }

    // ============================================================
    // v0.4.0f — the DIG layer: while the buried case runs, its
    // open sites dress their locations (Mystery3.DIG_SITES'
    // tokens, carried live by TownInfo.digTokens): a smooth-
    // edged pit, its spoil heap, the string grid over it, a
    // work tent at the farm sites. After the case retires the
    // sites grass over — all that may remain is the marker
    // stone by the Tegridy gate. Every shape here is a smooth
    // field or a straight string line — no clustered-hole
    // patterns, ever.
    // ============================================================
    private fun drawDigDetails(c: Canvas, w: Int, h: Int, loc: Location,
                               rnd: Random, night: Boolean, weatherId: String) {
        val toks = TownInfo.digTokens[loc.id] ?: return
        if (toks.isEmpty()) return
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) =
            Paper.strokePaint(col(hex), sw, alpha)

        // A dig pit: a pale soil rim, a dark smooth interior,
        // one lighter layer-band across it. [px] = center,
        // [py] = ground line, [pw] = half-width.
        fun pit(px: Float, py: Float, pw: Float) {
            val ph = pw * 0.30f
            c.drawOval(RectF(px - pw, py - ph, px + pw, py + ph), fp("#8a6a44"))
            c.drawOval(RectF(px - pw * 0.82f, py - ph * 0.78f,
                px + pw * 0.82f, py + ph * 0.78f), fp("#4a3423"))
            c.drawOval(RectF(px - pw * 0.62f, py - ph * 0.34f,
                px + pw * 0.62f, py + ph * 0.10f), fp("#5d4430"))
            val band = sp("#a8835c", 2.2f, 190)
            c.drawLine(px - pw * 0.80f, py - ph * 0.18f,
                px + pw * 0.80f, py - ph * 0.18f, band)
        }
        // The spoil heap: two smooth mounds and a resting
        // shovel — dug dirt, kept simple and round.
        fun spoil(px: Float, py: Float, s: Float) {
            c.drawOval(RectF(px - 34 * s, py - 15 * s, px + 34 * s, py + 6 * s),
                fp("#6e5133"))
            c.drawOval(RectF(px - 15 * s, py - 23 * s, px + 26 * s, py + 2 * s),
                fp("#7d5c39"))
            Paper.crooked(c, 28f, px + 30 * s, py - 18 * s) {
                c.drawLine(px + 30 * s, py - 34 * s, px + 30 * s, py - 8 * s,
                    sp("#8a6a44", 3.4f))
                c.drawRect(px + 25 * s, py - 10 * s, px + 35 * s, py + 2 * s,
                    fp("#9aa0a8"))
            }
        }
        // The string grid: taut straight strings over the pit,
        // four corner stakes — an excavation's graph paper.
        fun grid(px: Float, py: Float, pw: Float) {
            val ph = pw * 0.30f
            val string = sp("#efe8d4", 1.6f, 215)
            for (i in -1..1) {
                val gx = px + i * pw * 0.42f
                c.drawLine(gx, py - ph, gx, py + ph, string)
            }
            for (i in -1..1 step 2) {
                val gy = py + i * ph * 0.45f
                c.drawLine(px - pw, gy, px + pw, gy, string)
            }
            val stake = fp("#a83c2a")
            for (sx in listOf(px - pw, px + pw)) {
                for (sy in listOf(py - ph, py + ph)) {
                    c.drawRect(sx - 3f, sy - 3f, sx + 3f, sy + 3f, stake)
                }
            }
        }
        // A work tent: canvas triangle, a door shadow, a crate.
        fun tent(px: Float, py: Float, s: Float) {
            val canvasP = fp("#b8a888")
            c.drawPath(Path().apply {
                moveTo(px - 44 * s, py); lineTo(px, py - 46 * s)
                lineTo(px + 44 * s, py); close()
            }, canvasP)
            c.drawPath(Path().apply {
                moveTo(px - 13 * s, py); lineTo(px, py - 26 * s)
                lineTo(px + 13 * s, py); close()
            }, fp("#6a5c42"))
            c.drawLine(px, py - 46 * s, px, py - 54 * s, sp("#5a4c34", 2.6f))
            c.drawRect(px + 48 * s, py - 16 * s, px + 70 * s, py, fp("#8a6a44"))
            c.drawRect(px + 48 * s, py - 16 * s, px + 70 * s, py - 11 * s,
                fp("#75582f"))
        }

        when (loc.id) {
            "TegridyFarms" -> {
                // the farm carries two sites: the first field,
                // left foreground; the deep corner, right.
                if ("field-pit" in toks) pit(w * 0.27f, h * 0.885f, uw * 0.145f)
                    // dig: field-pit
                if ("field-spoil" in toks) spoil(w * 0.435f, h * 0.875f, 1.0f)
                    // dig: field-spoil
                if ("field-grid" in toks) grid(w * 0.27f, h * 0.885f, uw * 0.145f)
                    // dig: field-grid
                if ("field-tent" in toks) tent(w * 0.135f, h * 0.815f, 0.95f)
                    // dig: field-tent
                if ("deep-pit" in toks) pit(w * 0.70f, h * 0.90f, uw * 0.13f)
                    // dig: deep-pit
                if ("deep-spoil" in toks) spoil(w * 0.845f, h * 0.89f, 0.9f)
                    // dig: deep-spoil
                if ("deep-grid" in toks) grid(w * 0.70f, h * 0.90f, uw * 0.13f)
                    // dig: deep-grid
                if ("deep-tent" in toks) tent(w * 0.565f, h * 0.83f, 0.85f)
                    // dig: deep-tent
            }
            "StarksPond" -> {
                // the far bank: the dig works the right shore,
                // clear of the dock and the sign.
                if ("bank-pit" in toks) pit(w * 0.68f, h * 0.885f, uw * 0.125f)
                    // dig: bank-pit
                if ("bank-spoil" in toks) spoil(w * 0.825f, h * 0.875f, 0.9f)
                    // dig: bank-spoil
                if ("bank-grid" in toks) grid(w * 0.68f, h * 0.885f, uw * 0.125f)
                    // dig: bank-grid
            }
            "Elementary" -> {
                // the old oak lot: a smaller, careful dig in the
                // school's front ground, left of the walk.
                if ("oak-pit" in toks) pit(w * 0.36f, h * 0.90f, uw * 0.115f)
                    // dig: oak-pit
                if ("oak-spoil" in toks) spoil(w * 0.50f, h * 0.89f, 0.85f)
                    // dig: oak-spoil
                if ("oak-grid" in toks) grid(w * 0.36f, h * 0.90f, uw * 0.115f)
                    // dig: oak-grid
            }
        }
        if (Mystery3.MARKER_TOKEN in toks) {
            // dig: marker-stone — a flat stone by the gate,
            // two inscription lines, here to stay.
            val sx = w * 0.875f
            val sy = h * 0.905f
            c.drawPath(Path().apply {
                moveTo(sx - uw * 0.045f, sy)
                lineTo(sx - uw * 0.045f, sy - h * 0.052f)
                quadTo(sx, sy - h * 0.085f, sx + uw * 0.045f, sy - h * 0.052f)
                lineTo(sx + uw * 0.045f, sy); close()
            }, fp("#9aa0a4"))
            val insc = sp("#5a6068", 2.0f, 200)
            c.drawLine(sx - uw * 0.028f, sy - h * 0.040f, sx + uw * 0.028f,
                sy - h * 0.040f, insc)
            c.drawLine(sx - uw * 0.020f, sy - h * 0.026f, sx + uw * 0.020f,
                sy - h * 0.026f, insc)
        }
    }

    // ============================================================
    // v0.6.0c — the FIX layer: while the gardener case runs,
    // its overnight repairs appear at their spots (Mystery4.
    // FIX_SITES' tokens, carried live by TownInfo.fixTokens)
    // — and they STAY: a mended bench, a running fountain, a
    // sound rope are part of the town forever after, along
    // with whatever the endings keep dealing. Every shape is
    // a smooth field, a plank, a bloom or a bow — no
    // clustered-hole patterns, ever (the dig layer's law).
    // ============================================================
    private fun drawFixDetails(c: Canvas, w: Int, h: Int, loc: Location,
                               night: Boolean, weatherId: String) {
        val toks = TownInfo.fixTokens[loc.id] ?: return
        if (toks.isEmpty()) return
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) =
            Paper.strokePaint(col(hex), sw, alpha)
        val gy = h * 0.90f // the town's ground line

        // The signature: a red twine bow — two smooth loops
        // and a knot, ends trimmed. Whoever you are, your work
        // is known by this.
        fun bow(px: Float, py: Float, s: Float) {
            c.drawOval(RectF(px - 9 * s, py - 4.5f * s, px - 0.5f, py + 4.5f * s),
                sp("#c23a2a", 2.2f * s))
            c.drawOval(RectF(px + 0.5f, py - 4.5f * s, px + 9 * s, py + 4.5f * s),
                sp("#c23a2a", 2.2f * s))
            c.drawCircle(px, py, 2.4f * s, fp("#a82e20"))
            c.drawLine(px - 2 * s, py + 3 * s, px - 4.5f * s, py + 8 * s,
                sp("#c23a2a", 1.8f * s))
            c.drawLine(px + 2 * s, py + 3 * s, px + 4.5f * s, py + 8 * s,
                sp("#c23a2a", 1.8f * s))
        }
        // The second signature: a blue ribbon, tied the
        // beginner's way next to the bow (the joined fixes).
        fun ribbon(px: Float, py: Float, s: Float) {
            c.drawOval(RectF(px - 7 * s, py - 3.5f * s, px - 0.5f, py + 3.5f * s),
                sp("#2a6fd4", 2.0f * s))
            c.drawOval(RectF(px + 0.5f, py - 3.5f * s, px + 7 * s, py + 3.5f * s),
                sp("#2a6fd4", 2.0f * s))
            c.drawCircle(px, py, 2.0f * s, fp("#2058a8"))
        }
        // A bench that will never wobble again: level slats,
        // true legs, sealed wood.
        fun bench(px: Float, withRibbon: Boolean) {
            val wood = fp("#8a6844")
            val woodDark = fp("#6e5133")
            c.drawRect(px - uw * 0.055f, gy - h * 0.052f, px - uw * 0.047f, gy, woodDark)
            c.drawRect(px + uw * 0.047f, gy - h * 0.052f, px + uw * 0.055f, gy, woodDark)
            c.drawRect(px - uw * 0.062f, gy - h * 0.062f, px + uw * 0.062f,
                gy - h * 0.052f, wood)
            c.drawRect(px - uw * 0.058f, gy - h * 0.105f, px - uw * 0.050f,
                gy - h * 0.062f, woodDark)
            c.drawRect(px + uw * 0.050f, gy - h * 0.105f, px + uw * 0.058f,
                gy - h * 0.062f, woodDark)
            c.drawRect(px - uw * 0.062f, gy - h * 0.108f, px + uw * 0.062f,
                gy - h * 0.098f, wood)
            c.drawRect(px - uw * 0.062f, gy - h * 0.088f, px + uw * 0.062f,
                gy - h * 0.080f, wood)
            bow(px + uw * 0.052f, gy - h * 0.058f, 0.9f)
            if (withRibbon) ribbon(px - uw * 0.052f, gy - h * 0.058f, 0.85f)
        }
        // A planter box in full, defiant bloom: three round
        // heads, well spaced — smooth colour, never a cluster
        // of holes.
        fun planter(px: Float, withRibbon: Boolean) {
            c.drawRect(px - uw * 0.038f, gy - h * 0.034f, px + uw * 0.038f, gy,
                fp("#6e5133"))
            c.drawRect(px - uw * 0.042f, gy - h * 0.040f, px + uw * 0.042f,
                gy - h * 0.032f, fp("#8a6844"))
            for (dx in listOf(-0.026f, 0f, 0.026f)) {
                val sx = px + w * dx
                c.drawLine(sx, gy - h * 0.036f, sx, gy - h * 0.062f,
                    sp("#3f7a2e", 2.4f))
                c.drawCircle(sx, gy - h * 0.070f, uw * 0.011f, fp("#e8a83a"))
                c.drawCircle(sx, gy - h * 0.070f, uw * 0.005f, fp("#c46a1a"))
            }
            bow(px + uw * 0.030f, gy - h * 0.020f, 0.7f)
            if (withRibbon) ribbon(px - uw * 0.030f, gy - h * 0.020f, 0.65f)
        }

        when (loc.id) {
            "KidsPark" -> {
                if ("gard-bench" in toks) bench(w * 0.30f, false)
                    // fix: gard-bench
                if ("gard-swing" in toks) {
                    // fix: gard-swing — a swing on true, silent
                    // chains, its seat level at last.
                    val px = w * 0.72f
                    c.drawRect(px - uw * 0.045f, gy - h * 0.155f,
                        px + uw * 0.045f, gy - h * 0.148f, fp("#5a6068"))
                    c.drawLine(px - uw * 0.038f, gy - h * 0.148f,
                        px - uw * 0.020f, gy - h * 0.045f, sp("#8a9098", 2.2f))
                    c.drawLine(px + uw * 0.038f, gy - h * 0.148f,
                        px + uw * 0.020f, gy - h * 0.045f, sp("#8a9098", 2.2f))
                    c.drawRect(px - uw * 0.026f, gy - h * 0.048f,
                        px + uw * 0.026f, gy - h * 0.040f, fp("#8a6844"))
                    bow(px, gy - h * 0.152f, 0.7f)
                }
            }
            "MainStreet" -> {
                if ("gard-fountain" in toks) {
                    // fix: gard-fountain — the fountain RUNS:
                    // basin, pedestal, and clear water in
                    // smooth arcs (which freeze to a sheen in
                    // winter and still count).
                    val px = w * 0.50f
                    c.drawOval(RectF(px - uw * 0.085f, gy - h * 0.030f,
                        px + uw * 0.085f, gy + h * 0.008f), fp("#7a8894"))
                    c.drawOval(RectF(px - uw * 0.072f, gy - h * 0.026f,
                        px + uw * 0.072f, gy + h * 0.002f), fp("#5f96b8"))
                    c.drawRect(px - uw * 0.012f, gy - h * 0.085f,
                        px + uw * 0.012f, gy - h * 0.022f, fp("#8a96a2"))
                    c.drawOval(RectF(px - uw * 0.026f, gy - h * 0.098f,
                        px + uw * 0.026f, gy - h * 0.080f), fp("#7a8894"))
                    val water = sp("#a8d4ee", 2.6f, 220)
                    for (dx in listOf(-0.030f, -0.010f, 0.010f, 0.030f)) {
                        c.drawLine(px + w * dx * 0.4f, gy - h * 0.088f,
                            px + w * dx, gy - h * 0.030f, water)
                    }
                    bow(px + uw * 0.078f, gy - h * 0.012f, 0.75f)
                }
            }
            "StarksPond" -> {
                if ("gard-gate" in toks) {
                    // fix: gard-gate — the shore fence's gate,
                    // hung true: level rails, a bound latch.
                    val px = w * 0.82f
                    c.drawRect(px - uw * 0.038f, gy - h * 0.105f,
                        px - uw * 0.032f, gy, fp("#6e5133"))
                    c.drawRect(px + uw * 0.032f, gy - h * 0.105f,
                        px + uw * 0.038f, gy, fp("#6e5133"))
                    c.drawRect(px - uw * 0.032f, gy - h * 0.095f,
                        px + uw * 0.032f, gy - h * 0.086f, fp("#8a6844"))
                    c.drawRect(px - uw * 0.032f, gy - h * 0.055f,
                        px + uw * 0.032f, gy - h * 0.046f, fp("#8a6844"))
                    c.drawLine(px - uw * 0.032f, gy - h * 0.046f,
                        px + uw * 0.032f, gy - h * 0.095f, sp("#75582f", 3.0f))
                    bow(px + uw * 0.030f, gy - h * 0.070f, 0.8f)
                }
            }
            "CityHall" -> {
                if ("gard-planters" in toks) {
                    planter(w * 0.30f, false)
                    // fix: gard-planters
                    planter(w * 0.70f, false)
                }
            }
            "Elementary" -> {
                if ("gard-rope" in toks) {
                    // fix: gard-rope — the flagpole, its rope
                    // sound at last, the flag flying straight.
                    val px = w * 0.84f
                    val top = gy - h * 0.34f
                    c.drawLine(px, gy, px, top, sp("#9aa0a8", 3.2f))
                    c.drawLine(px + 3f, top + h * 0.02f, px + 3f,
                        gy - h * 0.10f, sp("#d8d2c2", 1.8f))
                    c.drawRect(px, top, px + uw * 0.055f, top + h * 0.038f,
                        fp("#2a6fd4"))
                    c.drawRect(px, top + h * 0.013f, px + uw * 0.055f,
                        top + h * 0.025f, fp("#e8e4da"))
                    bow(px, gy - h * 0.10f, 0.8f)
                }
            }
            "YourHouse" -> {
                if ("gard-porch" in toks) {
                    // fix: gard-porch — the mended step, and a
                    // potted marigold that was not there at
                    // supper. The last fix, given privately.
                    val px = w * 0.50f
                    c.drawRect(px - uw * 0.060f, gy - h * 0.016f,
                        px + uw * 0.060f, gy, fp("#8a6844"))
                    c.drawRect(px - uw * 0.048f, gy - h * 0.032f,
                        px + uw * 0.048f, gy - h * 0.016f, fp("#75582f"))
                    val mx = px + uw * 0.085f
                    c.drawRect(mx - uw * 0.014f, gy - h * 0.026f,
                        mx + uw * 0.014f, gy, fp("#a85a32"))
                    c.drawLine(mx, gy - h * 0.024f, mx, gy - h * 0.046f,
                        sp("#3f7a2e", 2.2f))
                    c.drawCircle(mx, gy - h * 0.054f, uw * 0.010f, fp("#e8a83a"))
                    c.drawCircle(mx, gy - h * 0.054f, uw * 0.0045f, fp("#c46a1a"))
                }
            }
            "BusStop" -> {
                if ("gard-sign" in toks) {
                    // fix: gard-sign — the leaning sign,
                    // standing straight and freshly striped.
                    val px = w * 0.20f
                    c.drawRect(px - 2f, gy - h * 0.13f, px + 2f, gy,
                        fp("#5a6068"))
                    c.drawCircle(px, gy - h * 0.145f, uw * 0.026f, fp("#e8e4da"))
                    c.drawCircle(px, gy - h * 0.145f, uw * 0.019f, fp("#2a6fd4"))
                    bow(px, gy - h * 0.115f, 0.65f)
                }
            }
            "Church" -> {
                if ("gard-railing" in toks) {
                    // fix: gard-railing — the church steps and
                    // a handrail that no longer moves at all.
                    val px = w * 0.38f
                    c.drawRect(px - uw * 0.05f, gy - h * 0.018f,
                        px + uw * 0.05f, gy, fp("#9aa0a4"))
                    c.drawRect(px - uw * 0.035f, gy - h * 0.036f,
                        px + uw * 0.05f, gy - h * 0.018f, fp("#8a9096"))
                    c.drawLine(px - uw * 0.055f, gy - h * 0.075f,
                        px + uw * 0.055f, gy - h * 0.038f, sp("#4c4c50", 3.4f))
                    c.drawRect(px - uw * 0.055f, gy - h * 0.075f,
                        px - uw * 0.051f, gy, fp("#4c4c50"))
                    c.drawRect(px + uw * 0.051f, gy - h * 0.038f,
                        px + uw * 0.055f, gy, fp("#4c4c50"))
                    bow(px + uw * 0.053f, gy - h * 0.040f, 0.65f)
                }
            }
            "SoDoSoPa" -> {
                if ("gard-joined-bench" in toks) bench(w * 0.62f, true)
                    // fix: gard-joined-bench
            }
            "Mall" -> {
                if ("gard-joined-planter" in toks) planter(w * 0.24f, true)
                    // fix: gard-joined-planter
            }
            "TegridyFarms" -> {
                if ("gard-joined-fence" in toks) {
                    // fix: gard-joined-fence — a fence line run
                    // true, two signatures at the gate end.
                    val px = w * 0.16f
                    for (i in 0..3) {
                        c.drawRect(px + i * uw * 0.052f, gy - h * 0.082f,
                            px + i * uw * 0.052f + 4f, gy, fp("#6e5133"))
                    }
                    c.drawRect(px - 2f, gy - h * 0.072f, px + uw * 0.165f,
                        gy - h * 0.064f, fp("#8a6844"))
                    c.drawRect(px - 2f, gy - h * 0.046f, px + uw * 0.165f,
                        gy - h * 0.038f, fp("#8a6844"))
                    bow(px + uw * 0.158f, gy - h * 0.068f, 0.8f)
                    ribbon(px + uw * 0.140f, gy - h * 0.042f, 0.75f)
                }
            }
        }
    }

    // ============================================================
    // v0.5.0d — the FACTION MARK layer: when a faction chain is
    // finished, its mark stands at its named location forever
    // (FactionMissions.markTokensByLoc, carried live by
    // TownInfo.factionMarkTokens): a claim flag, a poetry
    // panel, a list board, a banner, a watch sign, a plaque,
    // a labeled box, a farm sign. Every shape is a smooth
    // field, a board or a banner — no clustered-hole patterns,
    // ever (the dig layer's discipline).
    // ============================================================
    // ============================================================
    // 🎡 v0.9.0c: the FAIR layer — one week a year, Kid's
    // Park dresses for the county fair: a ferris silhouette
    // on the horizon, a row of booths along the back of the
    // lawn, pennant strings (wide-spaced triangles on a
    // line — never a clustered pattern), and the dunk tank
    // by the path. Everything here is a flat shape or a
    // smooth field; when the week ends TownInfo.fairTokens
    // empties and the park is a park again.
    // ============================================================
    private fun drawFairDetails(c: Canvas, w: Int, h: Int, loc: Location,
                                night: Boolean, weatherId: String) {
        val toks = TownInfo.fairTokens[loc.id] ?: return
        if (toks.isEmpty()) return
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) =
            Paper.strokePaint(col(hex), sw, alpha)
        val hy = h * 0.845f // the horizon the wheel rides
        val gy = h * 0.90f  // the ground line the booths stand on

        if ("fair-ferris" in toks) {
            // the wheel: a rim, eight spokes, small cabins —
            // a silhouette against the sky side of the lawn
            val cx = w * 0.80f
            val cy = hy - h * 0.16f
            val r = h * 0.13f
            val ink = sp("#4a5468", 3f, 230)
            c.drawLine(cx, gy, cx - r * 0.55f, gy, ink)
            c.drawLine(cx, gy, cx + r * 0.55f, gy, ink)
            c.drawLine(cx, cy + r, cx, gy, ink)
            c.drawCircle(cx, cy, r, sp("#4a5468", 3.5f, 235))
            for (i in 0 until 8) {
                val a = i * Math.PI / 4.0
                val ex = cx + (Math.cos(a) * r).toFloat()
                val ey = cy + (Math.sin(a) * r).toFloat()
                c.drawLine(cx, cy, ex, ey, sp("#4a5468", 2f, 200))
                c.drawRect(ex - uw * 0.008f, ey - h * 0.006f,
                    ex + uw * 0.008f, ey + h * 0.010f, fp("#6a768c", 220))
            }
            c.drawCircle(cx, cy, uw * 0.006f, fp("#8a94a8", 240))
        }
        if ("fair-booths" in toks) {
            // three booths along the back of the lawn: a box,
            // a striped awning (two flat bands — stripes, not
            // a pattern), a counter line
            val booths = listOf(0.10f to "#c23b3b", 0.30f to "#2a6fd4",
                0.52f to "#e3a72c")
            for ((bx, cloth) in booths) {
                val x0 = w * bx
                val bw = uw * 0.15f
                val top = gy - h * 0.115f
                c.drawRect(x0, top + h * 0.03f, x0 + bw, gy, fp("#7a5a3a"))
                c.drawRect(x0 - uw * 0.006f, top, x0 + bw + uw * 0.006f,
                    top + h * 0.030f, fp(cloth))
                c.drawRect(x0 - uw * 0.006f, top + h * 0.014f,
                    x0 + bw + uw * 0.006f, top + h * 0.021f, fp("#f2ead8"))
                c.drawRect(x0 + uw * 0.008f, top + h * 0.052f,
                    x0 + bw - uw * 0.008f, top + h * 0.058f, fp("#f2ead8", 200))
            }
        }
        if ("fair-bunting" in toks) {
            // two pennant strings across the top of the lawn:
            // a sagging line, wide-spaced triangle flags
            for ((y0, sag) in listOf(0.70f to 0.035f, 0.755f to 0.030f)) {
                val flags = listOf("#c23b3b", "#e3a72c", "#2a6fd4",
                    "#3f9e4d", "#c23b3b")
                var prevX = w * 0.06f
                var prevY = h * y0
                for (i in 0..10) {
                    val fx = w * (0.06f + i * 0.085f)
                    val fy = h * y0 + (sin(i * 0.9f) * h * sag * 0.3f) +
                        h * sag * (1f - kotlin.math.abs(i - 5) / 5f) * 0.4f
                    c.drawLine(prevX, prevY, fx, fy, sp("#5a4a3a", 1.8f, 210))
                    if (i in 1..9 && i % 2 == 1) {
                        val path = android.graphics.Path()
                        path.moveTo(fx - uw * 0.012f, fy)
                        path.lineTo(fx + uw * 0.012f, fy)
                        path.lineTo(fx, fy + h * 0.020f)
                        path.close()
                        c.drawPath(path, fp(flags[(i / 2) % flags.size], 235))
                    }
                    prevX = fx; prevY = fy
                }
            }
        }
        if ("fair-dunk-tank" in toks) {
            // the tank: a glass box, the water line inside,
            // the seat plank, and the one round target
            val x0 = w * 0.635f
            val top = gy - h * 0.105f
            c.drawRect(x0, top, x0 + uw * 0.075f, gy, fp("#9fc6d8", 150))
            c.drawRect(x0, top + h * 0.045f, x0 + uw * 0.075f, gy,
                fp("#3f7fa8", 170))
            c.drawRect(x0 - 2f, top, x0 + uw * 0.075f + 2f, top + 3f,
                fp("#4c4c50"))
            c.drawRect(x0 + uw * 0.012f, top + h * 0.028f,
                x0 + uw * 0.063f, top + h * 0.034f, fp("#8a5a3a"))
            c.drawRect(x0 + uw * 0.085f, top + h * 0.01f,
                x0 + uw * 0.089f, gy, fp("#4c4c50"))
            c.drawCircle(x0 + uw * 0.105f, top + h * 0.030f,
                uw * 0.016f, fp("#c23b3b"))
            c.drawCircle(x0 + uw * 0.105f, top + h * 0.030f,
                uw * 0.016f, sp("#f2ead8", 2.5f))
            c.drawCircle(x0 + uw * 0.105f, top + h * 0.030f,
                uw * 0.006f, fp("#f2ead8"))
        }
    }

    // ============================================================
    // ❄️ v0.11.0e: the CARNIVAL layer — one week a year,
    // Stark's Pond dresses for the winter carnival: a
    // banner strung between two shore posts, THREE ice
    // holes (broad dark ellipses, wide apart — holes in
    // ice you could fish through, never a clustered
    // pattern), lantern posts along the shore with warm
    // paper globes, and the sculpture row: blocky snow
    // figures standing their week's watch. Everything is
    // a flat shape or a smooth field; when the week ends
    // TownInfo.carnivalTokens empties and the pond is a
    // pond again.
    // ============================================================
    private fun drawCarnivalDetails(c: Canvas, w: Int, h: Int, loc: Location,
                                    night: Boolean, weatherId: String) {
        val toks = TownInfo.carnivalTokens[loc.id] ?: return
        if (toks.isEmpty()) return
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) =
            Paper.strokePaint(col(hex), sw, alpha)
        val gy = h * 0.90f  // the shore line everything stands on

        if ("carn-holes" in toks) {
            // three fishing holes in the ice: broad ellipses,
            // far apart, each with its rim of auger snow and
            // one bucket standing by — the pond, at work
            val holes = listOf(0.28f to 0.925f, 0.52f to 0.945f,
                0.74f to 0.918f)
            for ((hx, hy) in holes) {
                val cx = w * hx
                val cy = h * hy
                val rx = uw * 0.030f
                val ry = h * 0.008f
                c.drawOval(cx - rx - 3f, cy - ry - 2f,
                    cx + rx + 3f, cy + ry + 2f, fp("#eef4f8", 200))
                c.drawOval(cx - rx, cy - ry, cx + rx, cy + ry,
                    fp("#12222e", 235))
                c.drawRect(cx + rx + uw * 0.012f, cy - h * 0.020f,
                    cx + rx + uw * 0.030f, cy + ry, fp("#8a4a2a", 230))
            }
        }
        if ("carn-lanterns" in toks) {
            // lantern posts along the shore: a post, a cross
            // arm, one round paper globe apiece (lit warm at
            // night, pale by day)
            val posts = listOf(0.08f, 0.38f, 0.64f, 0.90f)
            val globe = if (night) "#ffb85c" else "#f2d8a0"
            for (px in posts) {
                val x = w * px
                val top = gy - h * 0.115f
                c.drawRect(x - 2f, top, x + 2f, gy, fp("#4c4238"))
                c.drawRect(x - uw * 0.016f, top, x + uw * 0.016f,
                    top + 3f, fp("#4c4238"))
                c.drawCircle(x, top + h * 0.020f, uw * 0.013f,
                    fp(globe, 235))
                c.drawCircle(x, top + h * 0.020f, uw * 0.013f,
                    sp("#8a6a3a", 1.8f, 220))
            }
        }
        if ("carn-sculptures" in toks) {
            // the shore row: three blocky snow figures — a
            // snowman (two smooth spheres and a hat line), a
            // fish (one smooth oval and a tail), a castle
            // block (a square with two square towers)
            val snow = "#f4f8fb"
            val shade = "#c9d8e4"
            // snowman at 0.16
            run {
                val x = w * 0.16f
                c.drawCircle(x, gy - h * 0.020f, uw * 0.020f, fp(shade))
                c.drawCircle(x, gy - h * 0.048f, uw * 0.014f, fp(snow))
                c.drawRect(x - uw * 0.016f, gy - h * 0.066f,
                    x + uw * 0.016f, gy - h * 0.060f, fp("#2a2a30"))
            }
            // fish at 0.46
            run {
                val x = w * 0.46f
                val cy = gy - h * 0.026f
                c.drawOval(x - uw * 0.034f, cy - h * 0.016f,
                    x + uw * 0.026f, cy + h * 0.016f, fp(snow))
                val path = android.graphics.Path()
                path.moveTo(x + uw * 0.024f, cy)
                path.lineTo(x + uw * 0.046f, cy - h * 0.018f)
                path.lineTo(x + uw * 0.046f, cy + h * 0.018f)
                path.close()
                c.drawPath(path, fp(shade))
                c.drawCircle(x - uw * 0.018f, cy - h * 0.004f,
                    uw * 0.0035f, fp("#2a2a30"))
            }
            // castle block at 0.82
            run {
                val x = w * 0.82f
                c.drawRect(x - uw * 0.030f, gy - h * 0.042f,
                    x + uw * 0.030f, gy, fp(snow))
                c.drawRect(x - uw * 0.030f, gy - h * 0.058f,
                    x - uw * 0.016f, gy, fp(shade))
                c.drawRect(x + uw * 0.016f, gy - h * 0.058f,
                    x + uw * 0.030f, gy, fp(shade))
            }
        }
        if ("carn-banner" in toks) {
            // the banner: two tall posts and one long cloth
            // band between them over the shore — a single
            // smooth field with a pale stripe, no lettering
            // (the Look carries the words)
            val y0 = gy - h * 0.185f
            for (px in listOf(0.05f, 0.95f)) {
                val x = w * px
                c.drawRect(x - 2.5f, y0 - h * 0.01f, x + 2.5f, gy,
                    fp("#5a4632"))
            }
            c.drawRect(w * 0.05f, y0, w * 0.95f, y0 + h * 0.030f,
                fp("#2a6fa8", 235))
            c.drawRect(w * 0.05f, y0 + h * 0.012f, w * 0.95f,
                y0 + h * 0.018f, fp("#f2ead8", 200))
        }
    }

    private fun drawFactionMarkDetails(c: Canvas, w: Int, h: Int, loc: Location,
                                       night: Boolean, weatherId: String) {
        val toks = TownInfo.factionMarkTokens[loc.id] ?: return
        if (toks.isEmpty()) return
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        fun col(hex: String): String {
            val worn = SceneLight.wearHex(hex, weatherId)
            return if (night) mix(worn, "#1a2340", 0.35f) else worn
        }
        fun fp(hex: String, alpha: Int = 255) = Paper.fillPaint(col(hex), alpha)
        fun sp(hex: String, sw: Float, alpha: Int = 255) =
            Paper.strokePaint(col(hex), sw, alpha)
        val gy = h * 0.90f // the marks stand on the ground line

        // A posted board: two posts, a panel, paper stripes.
        fun board(px: Float, panel: String, paper: String) {
            c.drawRect(px - 3f, gy - h * 0.075f, px - 1f, gy, fp("#5a4632"))
            c.drawRect(px + uw * 0.055f - 1f, gy - h * 0.075f,
                px + uw * 0.055f + 1f, gy, fp("#5a4632"))
            c.drawRect(px - uw * 0.008f, gy - h * 0.115f,
                px + uw * 0.063f, gy - h * 0.070f, fp(panel))
            c.drawRect(px + uw * 0.004f, gy - h * 0.107f,
                px + uw * 0.051f, gy - h * 0.078f, fp(paper))
            val ink = sp("#5a4a3a", 1.6f, 200)
            for (i in 0..2) {
                val ly = gy - h * (0.100f - i * 0.008f)
                c.drawLine(px + uw * 0.009f, ly, px + uw * 0.046f, ly, ink)
            }
        }
        // A hanging banner between two poles, one flat stripe.
        fun banner(px: Float, cloth: String, stripe: String) {
            c.drawRect(px - 2f, gy - h * 0.13f, px, gy, fp("#4c4c50"))
            c.drawRect(px + uw * 0.13f, gy - h * 0.13f, px + uw * 0.13f + 2f, gy,
                fp("#4c4c50"))
            c.drawRect(px, gy - h * 0.128f, px + uw * 0.13f, gy - h * 0.082f,
                fp(cloth))
            c.drawRect(px, gy - h * 0.110f, px + uw * 0.13f, gy - h * 0.100f,
                fp(stripe))
        }

        when (loc.id) {
            "StarksPond" -> if ("crew-flag" in toks) {
                // fmark: crew-flag — a crooked pole on the left
                // shore and a crayon-red flag that means it.
                val px = w * 0.15f
                c.drawLine(px, gy, px + uw * 0.006f, gy - h * 0.15f,
                    sp("#5a4632", 3.4f))
                c.drawPath(Path().apply {
                    moveTo(px + uw * 0.006f, gy - h * 0.15f)
                    lineTo(px + uw * 0.062f, gy - h * 0.138f)
                    lineTo(px + uw * 0.006f, gy - h * 0.122f); close()
                }, fp("#c0392b"))
            }
            "Elementary" -> {
                if ("goth-mural" in toks) {
                    // fmark: goth-mural — a black poetry panel on
                    // two stakes, one pale crescent painted on.
                    val px = w * 0.08f
                    c.drawRect(px, gy - h * 0.105f, px + uw * 0.085f, gy - h * 0.018f,
                        fp("#1c1c22"))
                    c.drawRect(px, gy - h * 0.105f, px + uw * 0.085f, gy - h * 0.098f,
                        fp("#3a3a44"))
                    c.drawCircle(px + uw * 0.058f, gy - h * 0.068f, uw * 0.013f,
                        fp("#e8e4d8"))
                    c.drawCircle(px + uw * 0.066f, gy - h * 0.072f, uw * 0.011f,
                        fp("#1c1c22"))
                }
                if ("honor-plaque" in toks) {
                    // fmark: honor-plaque — a brass plaque on a
                    // stone stand, dead level, with two lines.
                    val px = w * 0.84f
                    c.drawRect(px - uw * 0.006f, gy - h * 0.055f,
                        px + uw * 0.066f, gy, fp("#8a8578"))
                    c.drawRect(px, gy - h * 0.088f, px + uw * 0.060f,
                        gy - h * 0.050f, fp("#c9a227"))
                    val insc = sp("#7a5f10", 1.8f, 210)
                    c.drawLine(px + uw * 0.010f, gy - h * 0.074f,
                        px + uw * 0.050f, gy - h * 0.074f, insc)
                    c.drawLine(px + uw * 0.016f, gy - h * 0.062f,
                        px + uw * 0.044f, gy - h * 0.062f, insc)
                }
            }
            "Mall" -> if ("list-board" in toks) {
                // fmark: list-board — the Circle's board by the
                // entrance, one level sheet in the good pen.
                board(w * 0.10f, "#6a4a8a", "#f5efdd")
            }
            "KidsPark" -> if ("craig-banner" in toks) {
                // fmark: craig-banner — one word's worth of
                // banner over the fort ground, supremely flat.
                banner(w * 0.62f, "#636aa2", "#fadb16")
            }
            "MainStreet" -> if ("watch-sign" in toks) {
                // fmark: watch-sign — a tall watch board with a
                // bold band, re-hung and watching.
                val px = w * 0.845f
                c.drawRect(px, gy - h * 0.16f, px + 3f, gy, fp("#4c4c50"))
                c.drawRect(px - uw * 0.030f, gy - h * 0.175f,
                    px + uw * 0.036f, gy - h * 0.125f, fp("#e8c53a"))
                c.drawRect(px - uw * 0.030f, gy - h * 0.155f,
                    px + uw * 0.036f, gy - h * 0.145f, fp("#b03a2e"))
            }
            "Church" -> if ("lostfound-box" in toks) {
                // fmark: lostfound-box — a labeled box by the
                // door: lid, body, one pale label stripe.
                val px = w * 0.79f
                c.drawRect(px, gy - h * 0.052f, px + uw * 0.075f, gy,
                    fp("#a8835c"))
                c.drawRect(px - uw * 0.004f, gy - h * 0.062f,
                    px + uw * 0.079f, gy - h * 0.050f, fp("#8a6a44"))
                c.drawRect(px + uw * 0.018f, gy - h * 0.042f,
                    px + uw * 0.057f, gy - h * 0.028f, fp("#f5efdd"))
            }
            "TegridyFarms" -> if ("tegridy-sign" in toks) {
                // fmark: tegridy-sign — the Standard sign by the
                // gate, two posts and a level green board.
                val px = w * 0.055f
                c.drawRect(px, gy - h * 0.09f, px + 3f, gy, fp("#5a4632"))
                c.drawRect(px + uw * 0.105f, gy - h * 0.09f,
                    px + uw * 0.105f + 3f, gy, fp("#5a4632"))
                c.drawRect(px - uw * 0.008f, gy - h * 0.135f,
                    px + uw * 0.118f, gy - h * 0.085f, fp("#4a7a2a"))
                c.drawRect(px - uw * 0.008f, gy - h * 0.118f,
                    px + uw * 0.118f, gy - h * 0.108f, fp("#f2c230"))
            }
        }
        // ---- v0.8.0b: the CAPSTONE traces — the same merged
        // token map carries them (Capstones.markTokensByLoc);
        // each stands beside its chain's mark, in the same
        // smooth-fields-only discipline: a framed print, a
        // broadsheet, a charter, a totem, a bell, a quilt ----
        when (loc.id) {
            "StarksPond" -> if ("premiere-post" in toks) {
                // capmark: premiere-post — the framed premiere
                // print nailed beside the claim flag: dark frame,
                // pale print, one red stripe for the credits.
                val px = w * 0.235f
                c.drawRect(px, gy - h * 0.105f, px + uw * 0.070f, gy - h * 0.030f,
                    fp("#2e2a26"))
                c.drawRect(px + uw * 0.006f, gy - h * 0.098f,
                    px + uw * 0.064f, gy - h * 0.037f, fp("#f2ead8"))
                c.drawRect(px + uw * 0.006f, gy - h * 0.052f,
                    px + uw * 0.064f, gy - h * 0.044f, fp("#c0392b"))
            }
            "Elementary" -> {
                if ("goth-broadsheet" in toks) {
                    // capmark: goth-broadsheet — the town's
                    // obituary in a black frame by the mural,
                    // one black stripe where the last line sits.
                    val px = w * 0.205f
                    c.drawRect(px, gy - h * 0.115f, px + uw * 0.062f, gy - h * 0.022f,
                        fp("#141418"))
                    c.drawRect(px + uw * 0.005f, gy - h * 0.108f,
                        px + uw * 0.057f, gy - h * 0.029f, fp("#e8e4d8"))
                    c.drawRect(px + uw * 0.009f, gy - h * 0.046f,
                        px + uw * 0.053f, gy - h * 0.038f, fp("#141418"))
                }
                if ("staff-bell" in toks) {
                    // capmark: staff-bell — the re-cast bell by
                    // the door: a brass dome on a short post.
                    val px = w * 0.86f
                    c.drawRect(px, gy - h * 0.075f, px + 3f, gy, fp("#4c4c50"))
                    c.drawCircle(px + 1.5f, gy - h * 0.088f, uw * 0.016f,
                        fp("#c9a227"))
                    c.drawRect(px - uw * 0.014f, gy - h * 0.080f,
                        px + uw * 0.015f, gy - h * 0.073f, fp("#a8821d"))
                }
            }
            "Mall" -> if ("circle-charter" in toks) {
                // capmark: circle-charter — the framed charter
                // beside the list board: gold frame, white
                // paper, two column lines.
                val px = w * 0.66f
                c.drawRect(px, gy - h * 0.120f, px + uw * 0.068f, gy - h * 0.032f,
                    fp("#a8821d"))
                c.drawRect(px + uw * 0.005f, gy - h * 0.113f,
                    px + uw * 0.063f, gy - h * 0.039f, fp("#f7f2e4"))
                val ink = sp("#5a4a3a", 1.6f, 200)
                c.drawLine(px + uw * 0.012f, gy - h * 0.100f, px + uw * 0.012f,
                    gy - h * 0.048f, ink)
                c.drawLine(px + uw * 0.036f, gy - h * 0.100f, px + uw * 0.036f,
                    gy - h * 0.048f, ink)
            }
            "KidsPark" -> if ("gang-totem" in toks) {
                // capmark: gang-totem — three stacked crates at
                // the fort, each a flat field, none apologizing.
                val px = w * 0.78f
                c.drawRect(px, gy - h * 0.036f, px + uw * 0.058f, gy,
                    fp("#8a6844"))
                c.drawRect(px + uw * 0.004f, gy - h * 0.070f,
                    px + uw * 0.054f, gy - h * 0.036f, fp("#6e5133"))
                c.drawRect(px + uw * 0.009f, gy - h * 0.102f,
                    px + uw * 0.049f, gy - h * 0.070f, fp("#a8835c"))
            }
            "SkeetersBar" -> if ("watch-bell" in toks) {
                // capmark: watch-bell — the Watch bell over the
                // bar door: a brass dome on a dark bracket.
                val px = w * 0.60f
                c.drawRect(px, gy - h * 0.150f, px + uw * 0.030f, gy - h * 0.142f,
                    fp("#2e2a26"))
                c.drawCircle(px + uw * 0.015f, gy - h * 0.128f, uw * 0.015f,
                    fp("#c9a227"))
                c.drawRect(px + uw * 0.004f, gy - h * 0.121f,
                    px + uw * 0.026f, gy - h * 0.114f, fp("#a8821d"))
            }
            "Church" -> if ("church-quilt" in toks) {
                // capmark: church-quilt — the Third Pew quilt
                // by the door: four flat fields and a pale
                // label, hanging from a short rail.
                val px = w * 0.60f
                c.drawRect(px - uw * 0.004f, gy - h * 0.118f,
                    px + uw * 0.084f, gy - h * 0.112f, fp("#5a4632"))
                c.drawRect(px, gy - h * 0.112f, px + uw * 0.040f, gy - h * 0.072f,
                    fp("#7a4a68"))
                c.drawRect(px + uw * 0.040f, gy - h * 0.112f,
                    px + uw * 0.080f, gy - h * 0.072f, fp("#4a6a8a"))
                c.drawRect(px, gy - h * 0.072f, px + uw * 0.040f, gy - h * 0.034f,
                    fp("#8a6a3a"))
                c.drawRect(px + uw * 0.040f, gy - h * 0.072f,
                    px + uw * 0.080f, gy - h * 0.034f, fp("#5a7a4a"))
                c.drawRect(px + uw * 0.024f, gy - h * 0.048f,
                    px + uw * 0.056f, gy - h * 0.038f, fp("#f5efdd"))
            }
            "TegridyFarms" -> if ("tegridy-bench" in toks) {
                // capmark: tegridy-bench — the Signatories'
                // Bench by the gate: two legs, one long seat,
                // a back plank, all of it holding.
                val px = w * 0.30f
                c.drawRect(px, gy - h * 0.038f, px + 3f, gy, fp("#5a4632"))
                c.drawRect(px + uw * 0.088f, gy - h * 0.038f,
                    px + uw * 0.088f + 3f, gy, fp("#5a4632"))
                c.drawRect(px - uw * 0.006f, gy - h * 0.046f,
                    px + uw * 0.098f, gy - h * 0.038f, fp("#8a6844"))
                c.drawRect(px - uw * 0.006f, gy - h * 0.072f,
                    px + uw * 0.098f, gy - h * 0.064f, fp("#8a6844"))
            }
        }
    }

    // ---------------- v0.3.0a: the LIGHT layer ----------------
    // Called from drawScene only when SceneLight.lightLevel > 0
    // (dusk or night). Every glow alpha scales by that level, so
    // dusk is the town halfway lit. Geometry mirrors the parity
    // layer's so pieces land on the props they belong to.
    private fun drawLightLayer(c: Canvas, w: Int, h: Int, loc: Location, tod: String) {
        val cx = w / 2f
        val baseY = h * 0.66f
        // v0.16.0b: unit-width facade zone (see drawParityDetails)
        val bw = SceneScale.unitW(w.toFloat(), loc, false) *
            FacadeGeom.bodyWFrac(loc.kind)
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        val bh = when (loc.kind) {
            "house" -> h * 0.20f; "shop" -> h * 0.22f; "civic" -> h * 0.21f
            "school" -> h * 0.20f; else -> h * 0.10f
        }
        val left = cx - bw / 2
        val right = cx + bw / 2
        val top = baseY - bh
        val doorTop = baseY - bh * 0.45f
        val winY = top + bh * 0.30f
        val winW = bw * 0.11f
        val lv = SceneLight.levelForBand(tod)
        val warmth = SceneLight.warmthOf(loc.id)
        val night = tod == "night"
        fun a(base: Int): Int = (base * lv).toInt().coerceIn(0, 255)
        fun fp(hex: String, base: Int) = Paper.fillPaint(hex, a(base))
        fun glow(x: Float, y: Float, rx: Float, ry: Float, hex: String, base: Int) =
            c.drawOval(RectF(x - rx, y - ry, x + rx, y + ry), Paper.fillPaint(hex, a(base)))
        fun pool(x: Float, rx: Float, hex: String, base: Int) =
            c.drawOval(RectF(x - rx, baseY + h * 0.016f, x + rx,
                baseY + h * 0.016f + rx * 0.30f), Paper.fillPaint(hex, a(base)))
        fun lampPost(x: Float, hex: String) {
            val topY = h * 0.475f
            c.drawLine(x, baseY + h * 0.012f, x, topY, Paper.strokePaint("#22262e", 5f))
            glow(x, topY - 4, uw * 0.046f, h * 0.036f, hex, 55)
            c.drawCircle(x, topY - 4, 6f, Paper.fillPaint(hex, a(255)))
            pool(x, uw * 0.052f, hex, 48)
        }
        fun winHalo(i: Int, hex: String = warmth, base: Int = 46) {
            val wx = if (i == 0) left + bw * 0.10f else right - bw * 0.10f - winW
            glow(wx + winW / 2, winY + winW / 2, winW * 0.95f, winW * 0.95f, hex, base)
        }
        fun winFill(i: Int, hex: String, base: Int) {
            val wx = if (i == 0) left + bw * 0.10f else right - bw * 0.10f - winW
            c.drawRect(wx + 2, winY + 2, wx + winW - 2, winY + winW - 2, fp(hex, base))
        }
        fun porchGlow() {
            glow(cx + bw * 0.11f, baseY - bh * 0.48f, uw * 0.034f, h * 0.028f, warmth, 62)
            pool(cx + bw * 0.07f, uw * 0.048f, warmth, 40)
        }
        // the facade's own discipline, restated for overlays:
        // a shut shop and an empty house stay dark here too.
        val shut = loc.id in TownInfo.closedShops
        val houseDark = loc.kind == "house" && loc.id !in TownInfo.occupiedHomes
        val windowsLive = !shut && !houseDark

        // ---- sign halo: the businesses whose signs glow ----
        SceneLight.signGlowOf(loc.id)?.let { sg ->
            val signY = if (loc.kind == "outdoor" || loc.kind == "hub")
                baseY - bh - h * 0.045f else top - h * 0.045f
            glow(cx, signY, bw * 0.34f, h * 0.040f, sg, 34)
        }

        when (loc.id) {
            "KennysHouse" -> if (windowsLive) {
                // light: shack-porch-bulb — one bare bulb on a cord
                val bx = left + bw * 0.22f
                c.drawLine(bx, top + bh * 0.52f, bx, top + bh * 0.60f,
                    Paper.strokePaint("#22262e", 2f))
                glow(bx, top + bh * 0.63f, uw * 0.030f, h * 0.026f, warmth, 60)
                c.drawCircle(bx, top + bh * 0.63f, 4f, fp(warmth, 255))
                pool(bx, uw * 0.034f, warmth, 36)
                // light: shack-tv-window — TV leaks through the boards
                val wx = right - bw * 0.10f - winW
                val fl = if ((RenderClock.nowMs() / 280) % 2L == 0L) 165 else 110
                c.drawRect(wx + 2, winY + winW * 0.36f, wx + winW - 2,
                    winY + winW * 0.46f, fp("#7ab8e8", fl))
                c.drawRect(wx + 2, winY + winW * 0.56f, wx + winW - 2,
                    winY + winW * 0.64f, fp("#7ab8e8", fl - 30))
            }
            "ButtersHouse" -> if (windowsLive) {
                // light: stotch-porch-lamp
                porchGlow()
                // light: stotch-curtain-window — curtains in the glow
                val wx = left + bw * 0.10f
                c.drawRect(wx + 2, winY + 2, wx + winW - 2, winY + winW * 0.46f,
                    fp("#7a2436", 190))
                winHalo(1)
            }
            "CartmanHouse" -> if (windowsLive) {
                // light: cartman-tv-window — the big TV, window two
                val fl = if ((RenderClock.nowMs() / 280) % 2L == 0L) 185 else 130
                winFill(1, "#7ab8e8", fl)
                winHalo(1, "#7ab8e8", 40)
                // light: cartman-kitchen-window — warm, snacks happen here
                winHalo(0, warmth, 58)
                val sx = left + bw * 0.10f + winW / 2
                val st = Paper.strokePaint("#ffffff", 2f, a(120))
                c.drawLine(sx - 3, winY - 4, sx - 1, winY - 12, st)
                c.drawLine(sx + 3, winY - 4, sx + 5, winY - 12, st)
            }
            "StanHouse" -> {
                // light: marsh-garage-light — Randy's motion light
                val gx = right + bw * 0.18f
                glow(gx, top + bh * 0.12f, uw * 0.036f, h * 0.028f, warmth, 58)
                c.drawCircle(gx, top + bh * 0.12f, 4.5f, fp(warmth, 255))
                pool(gx, uw * 0.052f, warmth, 42)
                // light: marsh-porch-lamp
                if (windowsLive) porchGlow()
            }
            "KyleHouse" -> if (windowsLive) {
                // light: broflovski-study-window — somebody's reading
                winFill(0, "#fff3d0", 215)
                winHalo(0, warmth, 60)
                // light: broflovski-porch-lamp
                porchGlow()
            }
            "TolkienHouse" -> {
                // light: tolkien-gate-lanterns — on timers, always on
                for (gx in listOf(left - bw * 0.17f, right + bw * 0.17f)) {
                    glow(gx, baseY - bh * 0.85f, uw * 0.026f, h * 0.024f, warmth, 60)
                    c.drawRect(gx - 4, baseY - bh * 0.85f - 5, gx + 4,
                        baseY - bh * 0.85f + 5, fp(warmth, 255))
                }
                // light: tolkien-driveway-pool
                pool(cx - bw * 0.22f, uw * 0.085f, warmth, 26)
                // light: tolkien-chandelier-windows
                if (windowsLive) {
                    for (i in 0..1) {
                        winFill(i, "#fff0c0", 195)
                        val wx = (if (i == 0) left + bw * 0.10f
                            else right - bw * 0.10f - winW) + winW / 2
                        val wy = winY + winW / 2
                        val dp = fp("#8a6a1f", 235)
                        c.drawCircle(wx - 5, wy - 2, 1.8f, dp)
                        c.drawCircle(wx + 5, wy - 2, 1.8f, dp)
                        c.drawCircle(wx, wy + 3, 1.8f, dp)
                        winHalo(i, warmth, 52)
                    }
                }
            }
            "BusStop" -> {
                // light: busstop-lamp — the stop's own streetlamp
                lampPost(cx - uw * 0.38f, warmth)
                // light: busstop-pool — lamplight on the bench slats
                pool(cx - uw * 0.22f, uw * 0.085f, warmth, 28)
                glow(cx - uw * 0.22f, baseY + 24, uw * 0.075f, 20f, warmth, 22)
            }
            "MainStreet" -> {
                // light: main-streetlamps — a pair over the street
                lampPost(cx - uw * 0.36f, warmth)
                lampPost(cx + uw * 0.36f, warmth)
                // light: main-shop-glow — the diner front still lit
                // (v0.16.0b: this loop used to mirror the retired
                // storefront-row's windows; it now lights the real
                // diner structure's window band and door pool)
                if (!shut) {
                    val sr = FacadeGeom.structureRect(
                        TownBaseline.of(loc.id)?.structure ?: "hut",
                        w.toFloat(), h.toFloat(),
                        SceneScale.unitW(w.toFloat(), loc, false))
                    c.drawRect(sr.left + sr.width * 0.07f, sr.top + sr.height * 0.32f,
                        sr.right - sr.width * 0.07f, sr.bottom - sr.height * 0.06f,
                        fp(warmth, 46))
                    pool(sr.cx, uw * 0.026f, warmth, 26)
                }
            }
            "TweekBros" -> if (!shut) {
                // light: tweek-cup-glow — the cup sign, backlit
                val bx = right + bw * 0.055f + 16
                glow(bx, top + bh * 0.34f, uw * 0.048f, bh * 0.30f, warmth, 52)
                c.drawRoundRect(RectF(bx - 30, top + bh * 0.16f, bx + 30,
                    top + bh * 0.52f), 7f, 7f, fp(warmth, 55))
                // light: tweek-window-steam — espresso steam on glass
                winHalo(0, warmth, 54)
                val sx = left + bw * 0.10f + winW / 2
                val st = Paper.strokePaint("#ffffff", 2f, a(110))
                c.drawLine(sx - 3, winY - 4, sx - 1, winY - 13, st)
                c.drawLine(sx + 3, winY - 4, sx + 5, winY - 13, st)
            }
            "JimbosGuns" -> if (!shut) {
                // light: jimbo-porch-bulb
                glow(cx, doorTop - 8, uw * 0.030f, h * 0.024f, warmth, 58)
                c.drawCircle(cx, doorTop - 8, 4.5f, fp(warmth, 255))
                pool(cx, uw * 0.042f, warmth, 36)
                // light: jimbo-window-glow
                winHalo(0); winHalo(1)
            }
            "CityWok" -> if (!shut) {
                // light: wok-door-pool — red-gold spill from the door
                pool(cx, uw * 0.068f, "#f2432a", 30)
                pool(cx, uw * 0.042f, warmth, 38)
                // light: wok-window-steam — wok steam fogging the glass
                winFill(1, warmth, 120)
                val sx = right - bw * 0.10f - winW / 2
                val st = Paper.strokePaint("#ffffff", 2f, a(100))
                c.drawLine(sx, winY + winW * 0.3f, sx + 2, winY + winW * 0.6f, st)
            }
            "SkeetersBar" -> if (!shut) {
                // light: skeeter-neon-halo — the neon, felt a block away
                glow(cx, doorTop - bh * 0.15f, bw * 0.23f, bh * 0.25f, "#ff5a8a", 44)
                pool(cx, uw * 0.080f, "#ff5a8a", 28)
                // light: skeeter-pool-window — green felt glow, window one
                winFill(0, "#58b368", 115)
                winHalo(0, "#58b368", 38)
            }
            "Bijou" -> if (!shut) {
                // light: bijou-marquee-halo
                val my = doorTop - bh * 0.06f
                glow(cx, my - bh * 0.065f, bw * 0.31f, bh * 0.17f, "#c86ae8", 38)
                glow(cx, my - bh * 0.065f, bw * 0.26f, bh * 0.12f, warmth, 30)
                // light: bijou-poster-lights — a lamp over each poster
                for (s in listOf(-1f, 1f)) {
                    val px = cx + s * bw * 0.30f
                    c.drawCircle(px, doorTop - bh * 0.10f, 3.2f, fp(warmth, 255))
                    glow(px, doorTop - bh * 0.10f, bw * 0.055f, bh * 0.09f, warmth, 40)
                }
            }
            "TomsRhino" -> if (!shut) {
                // light: rhino-nose-glow — the nose, lit like a beacon
                val ny = top - bh * 0.16f
                glow(cx, ny + bh * 0.01f, bw * 0.135f, bh * 0.17f, "#f28ab0", 42)
                // light: rhino-window-glow
                winHalo(0); winHalo(1)
            }
            "Bank" -> {
                // light: bank-floodlight — the stone, uplight and serious
                glow(cx, top - bh * 0.10f, bw * 0.30f, bh * 0.22f, warmth, 28)
                // light: bank-column-pools — a pool at every column base
                for (i in 0..3) {
                    val colX = left + bw * (0.10f + i * 0.24f) + bw * 0.022f
                    pool(colX, uw * 0.028f, warmth, 24)
                }
            }
            "PostOffice" -> if (!shut) {
                // light: post-flag-spot — the flag, spotlit all night
                val fx = right - bw * 0.06f + bw * 0.045f
                glow(fx, top - bh * 0.21f, bw * 0.095f, bh * 0.15f, warmth, 46)
                // light: post-window-glow — fluorescent, sorting never sleeps
                winHalo(0, "#eaf2ff", 50)
                winHalo(1, "#eaf2ff", 36)
            }
            "FreemansTacos" -> if (!shut) {
                // light: taco-roof-glow — the taco, outlined in light
                val tx = cx
                val ty = top - bh * 0.02f
                glow(tx, ty - bh * 0.10f, bw * 0.17f, bh * 0.20f, "#ffb84a", 40)
                for (k in 0..6) {
                    val ang = Math.PI * k / 6
                    val bxx = (tx + bw * 0.125f * kotlin.math.cos(ang)).toFloat()
                    val byy = (ty - bh * 0.02f - bh * 0.27f * kotlin.math.sin(ang)).toFloat()
                    c.drawCircle(bxx, byy, 3f, fp("#ffe9a8", 235))
                }
                // light: taco-window-glow
                winHalo(0)
            }
            "Church" -> {
                // light: church-glass-glow — the glass burns from inside
                val gx = cx
                val gr = bw * 0.075f
                c.drawPath(Path().apply {
                    moveTo(gx - gr, doorTop - bh * 0.02f)
                    lineTo(gx - gr, doorTop - bh * 0.42f)
                    quadTo(gx, doorTop - bh * 0.72f, gx + gr, doorTop - bh * 0.42f)
                    lineTo(gx + gr, doorTop - bh * 0.02f)
                    close()
                }, fp(warmth, 140))
                glow(gx, doorTop - bh * 0.37f, bw * 0.115f, bh * 0.38f, warmth, 36)
                // light: church-porch-lantern
                val lx = cx + bw * 0.10f
                c.drawCircle(lx, doorTop + bh * 0.04f, 3.8f, fp(warmth, 255))
                glow(lx, doorTop + bh * 0.04f, uw * 0.024f, h * 0.022f, warmth, 55)
                pool(lx, uw * 0.034f, warmth, 34)
            }
            "CityHall" -> {
                // light: hall-dome-flood — floodlit dome
                glow(cx, top - bh * 0.55f, bw * 0.15f, bh * 0.32f, warmth, 34)
                // light: hall-late-window — one office, still working
                val wx = left + bw * 0.64f
                c.drawRect(wx, winY, wx + winW, winY + winW, fp(warmth, 235))
                winHalo(1, warmth, 40)
            }
            "Elementary" -> if (!shut) {
                // light: school-window-row — classrooms after hours
                for (f in listOf(0.235f, 0.365f)) {
                    val wx = left + bw * f
                    c.drawRect(wx, winY, wx + winW, winY + winW, fp(warmth, 195))
                }
                winHalo(0, warmth, 42)
                // light: school-flag-spot
                glow(right - bw * 0.1f + 22, top - bh * 0.5f + 13, bw * 0.065f,
                    bh * 0.10f, warmth, 44)
            }
            "KidsPark" -> {
                // light: park-court-light — the hoop light buzzes on
                val hx = cx + uw * 0.26f - 20
                c.drawLine(cx + uw * 0.26f, baseY - 118, hx, baseY - 126,
                    Paper.strokePaint("#22262e", 4f))
                glow(hx, baseY - 128, uw * 0.055f, h * 0.038f, warmth, 55)
                c.drawCircle(hx, baseY - 128, 5.5f, fp(warmth, 255))
                pool(cx + uw * 0.26f - 28, uw * 0.080f, warmth, 40)
                // light: park-pavilion-glow — string bulbs on the swing frame
                val sx = cx - uw * 0.33f
                val sy = baseY - h * 0.085f
                for (i in 0..4) {
                    val bx2 = sx - 36 + i * 18f
                    c.drawCircle(bx2, sy + 6, 3f, fp("#ffd98f", 235))
                    glow(bx2, sy + 6, 9f, 9f, "#ffd98f", 30)
                }
            }
            "StarksPond" -> {
                // light: pond-dock-lantern — one lantern at the dock's end
                val dy = baseY + h * 0.028f
                val lx = cx - uw * 0.215f
                c.drawLine(lx, dy + 9, lx, dy - 30, Paper.strokePaint("#3a2c1c", 4f))
                glow(lx, dy - 36, uw * 0.032f, h * 0.028f, warmth, 60)
                c.drawRect(lx - 4, dy - 41, lx + 4, dy - 31, fp(warmth, 255))
                glow(lx, baseY + h * 0.055f, uw * 0.045f, h * 0.012f, warmth, 30)
                // light: pond-ice-sheen — moonlight pooled on the ice.
                // v0.3.0e: the pool is only as bright as the moon
                // (SkyState.MOONLIGHT via TownInfo) — a full moon
                // lays a road of light on the ice; a new moon
                // leaves the lantern to do the work alone.
                val moonF = 0.30f + 0.70f * TownInfo.moonlight
                glow(cx + uw * 0.12f, baseY + h * 0.050f, uw * 0.16f, h * 0.028f,
                    "#a8d4f0", (24 * moonF).toInt())
                glow(cx + uw * 0.12f, baseY + h * 0.050f, uw * 0.08f, h * 0.016f,
                    "#dfe9f5", (22 * moonF).toInt())
            }
            "Mall" -> if (!shut) {
                // light: mall-pylon-glow — SP MALL, visible from orbit
                val px = left - bw * 0.10f
                glow(px, top - bh * 0.59f, bw * 0.115f, bh * 0.27f, "#7ad4e8", 48)
                c.drawRoundRect(RectF(px - bw * 0.075f, top - bh * 0.78f,
                    px + bw * 0.075f, top - bh * 0.40f), 8f, 8f, fp("#7ad4e8", 36))
                // light: mall-entrance-spill
                glow(cx, doorTop + bh * 0.08f, bw * 0.25f, bh * 0.22f, warmth, 32)
                pool(cx, uw * 0.10f, warmth, 38)
                // light: mall-lot-lamps
                lampPost(left - bw * 0.32f, "#eaf6ff")
                lampPost(right + bw * 0.30f, "#eaf6ff")
            }
            "SoDoSoPa" -> {
                // light: sodo-string-halo — the string lights, haloed
                for (i in 1..10) {
                    val x = cx - uw * 0.30f + i * (uw * 0.60f / 10)
                    val y = baseY - h * 0.125f + kotlin.math.sin(i * 0.95f) * 9
                    glow(x, y + 5, 10f, 10f, "#ffd98f", 34)
                    c.drawCircle(x, y + 5, 4f, fp("#ffd98f", 255))
                }
                // light: sodo-loft-windows — somebody's home up there
                for (i in 0..2) {
                    val lx = cx - uw * 0.28f + i * uw * 0.20f
                    val wy = baseY - 78 - i * 12
                    if (i != 1) c.drawRect(lx - 20, wy.toFloat(), lx + 2,
                        wy + 26f, fp(warmth, 185))
                }
            }
            "TegridyFarms" -> {
                // light: tegridy-barn-lantern — a lantern by the barn door
                val bx = cx + uw * 0.18f
                glow(bx - 32, baseY - 68, uw * 0.030f, h * 0.026f, warmth, 58)
                c.drawRect(bx - 36, baseY - 73, bx - 28, baseY - 63, fp(warmth, 255))
                pool(bx - 20, uw * 0.045f, warmth, 36)
                // light: tegridy-silo-beacon — the silo blinks at planes
                if (night && (RenderClock.nowMs() / 800) % 2L == 0L) {
                    val sx = cx + uw * 0.015f + uw * 0.0225f
                    val sy = baseY - h * 0.225f
                    glow(sx, sy, 13f, 13f, "#ff4444", 60)
                    c.drawCircle(sx, sy, 3.2f, fp("#ff4444", 255))
                }
            }
            "Raisins" -> if (!shut) {
                // light: raisins-wing-glow — the wings, backlit orange
                for (s in listOf(-1f, 1f)) {
                    glow(cx + s * bw * 0.21f, doorTop + bh * 0.16f, bw * 0.075f,
                        bh * 0.13f, "#ff8a3a", 42)
                }
                // light: raisins-band-glow — the brand band, lit from under
                c.drawRect(left - bw * 0.02f, top + bh * 0.13f, right + bw * 0.02f,
                    top + bh * 0.17f, fp("#ff8a3a", 115))
                glow(cx, top + bh * 0.15f, bw * 0.40f, bh * 0.06f, "#ff8a3a", 26)
            }
            "YourHouse" -> if (windowsLive) {
                // light: newkid-porch-glow — your porch light,
                // burning in your house's own warmth, pool and all
                porchGlow()
                // light: newkid-window-glow — the left window
                // stays up latest; somebody's awake in there
                winHalo(0, warmth, 52)
                winFill(0, warmth, 150)
            }
        }
    }

    private fun drawHolidayDecor(c: Canvas, w: Int, h: Int, loc: Location, holiday: Holiday,
                                 rnd: Random, night: Boolean) {
        val cx = w / 2f
        val baseY = h * 0.66f
        val flags = holiday.flags
        val uw = SceneScale.unitW(w.toFloat(), loc, false)
        if ("pumpkins" in flags) {
            // crude pumpkins flanking the facade
            for (side in listOf(-1f, 1f)) {
                val px = cx + side * uw * 0.36f
                val py = baseY - 6
                Paper.crooked(c, side * 4f, px, py) {
                    c.drawOval(RectF(px - 26, py - 30, px + 26, py + 6),
                        Paper.fillPaint("#e07b2a"))
                    // crude carved face
                    val fp = Paper.fillPaint("#2a1a0a")
                    c.drawPath(Path().apply {
                        moveTo(px - 14, py - 20); lineTo(px - 8, py - 12); lineTo(px - 2, py - 20); close()
                    }, fp)
                    c.drawPath(Path().apply {
                        moveTo(px + 2, py - 20); lineTo(px + 8, py - 12); lineTo(px + 14, py - 20); close()
                    }, fp)
                    c.drawPath(Path().apply {
                        moveTo(px - 12, py - 6); lineTo(px - 6, py - 2); lineTo(px, py - 6)
                        lineTo(px + 6, py - 2); lineTo(px + 12, py - 6); close()
                    }, fp)
                    if (night) { // candle glow
                        c.drawOval(RectF(px - 34, py - 38, px + 34, py + 14),
                            Paper.fillPaint("#f2c230", 50))
                    }
                }
            }
        }
        if ("lights" in flags) {
            // tacky Christmas light string across the facade
            val cols = listOf("#d43d2a", "#2a9a4a", "#2a6fd4", "#f2c230")
            val y0 = baseY - h * 0.24f
            var prevX = cx - uw * 0.36f
            var prevY = y0
            val wire = Paper.strokePaint("#2a2a2a", 2f)
            for (i in 1..14) {
                val x = cx - uw * 0.36f + i * (uw * 0.72f / 14)
                val y = y0 + sin(i * 0.9f) * 14
                c.drawLine(prevX, prevY, x, y, wire)
                c.drawCircle(x, y + 5, 5f, Paper.fillPaint(cols[i % cols.size]))
                prevX = x; prevY = y
            }
        }
        if ("bunting" in flags) {
            val cols = listOf("#d43d2a", "#ffffff", "#2a6fd4")
            val y0 = baseY - h * 0.26f
            for (i in 0..11) {
                val x = cx - uw * 0.33f + i * (uw * 0.66f / 11)
                val y = y0 + sin(i * 0.8f) * 10
                c.drawPath(Path().apply {
                    moveTo(x - 9, y); lineTo(x + 9, y); lineTo(x, y + 16); close()
                }, Paper.fillPaint(cols[i % cols.size]))
            }
        }
        if ("hearts" in flags) {
            val hp = Paper.fillPaint("#e87a8a")
            repeat(6) {
                val x = rnd.nextFloat() * w
                val y = baseY - h * 0.30f + rnd.nextFloat() * h * 0.2f
                drawHeart(c, x, y, 10 + rnd.nextFloat() * 8, hp)
            }
        }
        if ("cowspots" in flags) {
            // cow spots on the ground
            val sp = Paper.fillPaint("#2a2a2a")
            repeat(8) {
                val x = rnd.nextFloat() * w
                val y = baseY + 20 + rnd.nextFloat() * (h - baseY - 40)
                c.drawOval(RectF(x, y, x + 26 + rnd.nextFloat() * 30, y + 18 + rnd.nextFloat() * 14), sp)
            }
        }
        // v0.1.9e holiday dressing, per holiday (ids cross-checked
        // against HOLIDAY_DRESSING by the smoke suite — if you
        // rename an element here, rename it there too):
        when (holiday.id) {
            "halloween" -> {
                // tombstones: two lopsided stones on the ground
                val stone = Paper.fillPaint("#8a8a92")
                for (sx in listOf(cx - uw * 0.30f, cx + uw * 0.24f)) {
                    c.drawOval(RectF(sx - 14, baseY - 30, sx + 14, baseY + 16), stone)
                    c.drawRect(RectF(sx - 14, baseY - 8, sx + 14, baseY + 16), stone)
                }
                // porch pumpkins are the flag-driven ones above
            }
            "christmas" -> {
                // wreath on the facade + luminarias along the base
                c.drawCircle(cx, baseY - h * 0.20f, 20f, Paper.fillPaint("#2a7a3a"))
                c.drawCircle(cx, baseY - h * 0.20f, 11f, Paper.fillPaint(loc.colorHex))
                c.drawCircle(cx, baseY - h * 0.20f + 18f, 5f, Paper.fillPaint("#d43d2a"))
                val lum = Paper.fillPaint("#f2c230", 200)
                for (i in 0..5) {
                    val lx = cx - uw * 0.34f + i * (uw * 0.68f / 5)
                    c.drawRect(RectF(lx - 7, baseY - 10, lx + 7, baseY + 6), lum)
                }
            }
            "newyear" -> {
                // confetti cannons staged at the street edges
                val can = Paper.fillPaint("#5a4a8a")
                for (sx in listOf(cx - uw * 0.38f, cx + uw * 0.38f)) {
                    c.drawRect(RectF(sx - 8, baseY - 34, sx + 8, baseY + 6), can)
                    c.drawCircle(sx, baseY - 36, 9f, Paper.fillPaint("#c8c8d2"))
                }
                // banner 2027: the HAPPY NEW YEAR banner
                val bp = Paper.textPaint("#f2c230", 26f)
                bp.textAlign = Paint.Align.CENTER
                c.drawText("HAPPY NEW YEAR", cx, baseY - h * 0.30f, bp)
            }
            "valentine" -> {
                // heart garland strung across the facade
                val hp2 = Paper.fillPaint("#d43d6a")
                for (i in 0..7) {
                    val x = cx - uw * 0.32f + i * (uw * 0.64f / 7)
                    val y = baseY - h * 0.26f + sin(i * 0.85f) * 10
                    drawHeart(c, x, y, 9f, hp2)
                }
                // valentine drift: rejected cards piled at the base
                repeat(5) {
                    val x = cx - uw * 0.3f + rnd.nextFloat() * uw * 0.6f
                    c.drawRect(RectF(x, baseY + 2, x + 14, baseY + 10),
                        Paper.fillPaint("#e8a0b0"))
                }
            }
            "july4" -> {
                // fireworks pile: a crate of cones mid-street
                c.drawRect(RectF(cx - 26, baseY - 16, cx + 26, baseY + 6),
                    Paper.fillPaint("#8a6a3a"))
                val cone = Paper.fillPaint("#d43d2a")
                for (dx in listOf(-16f, 0f, 16f)) {
                    c.drawPath(Path().apply {
                        moveTo(cx + dx - 7, baseY - 16); lineTo(cx + dx + 7, baseY - 16)
                        lineTo(cx + dx, baseY - 40); close()
                    }, cone)
                }
                // bunting swags are the flag-driven bunting above
            }
            "cowdays" -> {
                // cow banners: a spotted strip across the facade
                c.drawRect(RectF(cx - uw * 0.34f, baseY - h * 0.28f,
                    cx + uw * 0.34f, baseY - h * 0.28f + 22), Paper.fillPaint("#f2f0ea"))
                val sp2 = Paper.fillPaint("#2a2a2a")
                repeat(6) {
                    val x = cx - uw * 0.3f + rnd.nextFloat() * uw * 0.6f
                    c.drawOval(RectF(x, baseY - h * 0.28f + 4, x + 18, baseY - h * 0.28f + 16), sp2)
                }
                // the patient cow, standing in judgment
                val cowY = baseY + 14
                c.drawOval(RectF(cx + uw * 0.16f, cowY, cx + uw * 0.16f + 54, cowY + 30),
                    Paper.fillPaint("#f2f0ea"))
                c.drawOval(RectF(cx + uw * 0.16f + 8, cowY + 6, cx + uw * 0.16f + 26, cowY + 18), sp2)
                c.drawCircle(cx + uw * 0.16f + 58, cowY + 10, 11f, Paper.fillPaint("#f2f0ea"))
                c.drawCircle(cx + uw * 0.16f + 60, cowY + 12, 3f, sp2)
            }
            "stpatricks" -> {
                // shamrock dots scattered on the facade (sparse)
                val gp = Paper.fillPaint("#2a9a4a")
                repeat(5) {
                    val x = cx - uw * 0.3f + rnd.nextFloat() * uw * 0.6f
                    val y = baseY - h * 0.30f + rnd.nextFloat() * h * 0.16f
                    for (a in 0..2) {
                        c.drawCircle(x + kotlin.math.cos(a * 2.1f) * 5,
                            y + sin(a * 2.1f) * 5, 5f, gp)
                    }
                }
                // green bunting swags under the roofline
                for (i in 0..9) {
                    val x = cx - uw * 0.32f + i * (uw * 0.64f / 9)
                    val y = baseY - h * 0.28f + sin(i * 0.8f) * 8
                    c.drawPath(Path().apply {
                        moveTo(x - 8, y); lineTo(x + 8, y); lineTo(x, y + 14); close()
                    }, gp)
                }
            }
            "blackfriday" -> {
                // sale banners: red strips shouting percentages
                val rp = Paper.fillPaint("#d43d2a")
                c.drawRect(RectF(cx - uw * 0.34f, baseY - h * 0.29f,
                    cx + uw * 0.34f, baseY - h * 0.29f + 24), rp)
                val tp = Paper.textPaint("#ffffff", 20f)
                tp.textAlign = Paint.Align.CENTER
                c.drawText("SALE 40% OFF", cx, baseY - h * 0.29f + 18, tp)
                // line tents: the queue has camped out
                for (tx in listOf(cx - uw * 0.30f, cx + uw * 0.22f)) {
                    c.drawPath(Path().apply {
                        moveTo(tx - 22, baseY + 8); lineTo(tx + 22, baseY + 8)
                        lineTo(tx, baseY - 20); close()
                    }, Paper.fillPaint("#4a6a8a"))
                }
            }
        }
    }

    fun drawHeart(c: Canvas, x: Float, y: Float, s: Float, paint: Paint) {
        // jagged paper heart (crude South Park style, not cute-pastel)
        c.drawPath(Path().apply {
            moveTo(x, y + s * 0.9f)
            quadTo(x - s * 1.2f, y, x - s * 0.55f, y - s * 0.35f)
            quadTo(x - s * 0.1f, y - s * 0.6f, x, y - s * 0.15f)
            quadTo(x + s * 0.1f, y - s * 0.6f, x + s * 0.55f, y - s * 0.35f)
            quadTo(x + s * 1.2f, y, x, y + s * 0.9f)
            close()
        }, paint)
    }

    // ---------------- Weather particles (animated; call every frame with tick) ----------------
    fun drawWeatherParticles(c: Canvas, w: Int, h: Int, weatherId: String, tick: Long, night: Boolean) {
        when (weatherId) {
            "light_snow" -> {
                val p = Paper.fillPaint("#ffffff", 220)
                val r = Random(tick / 6)
                repeat(40) {
                    val x = r.nextFloat() * w
                    val y = ((r.nextFloat() * h + tick * 1.2f) % h).toFloat()
                    c.drawCircle(x, y, 2.2f, p)
                }
            }
            "heavy_snow" -> {
                val p = Paper.fillPaint("#ffffff", 235)
                val r = Random(tick / 4)
                repeat(140) {
                    val x = r.nextFloat() * w
                    val y = ((r.nextFloat() * h + tick * 2.6f) % h).toFloat()
                    c.drawCircle(x, y, 2.8f, p)
                }
            }
            "blizzard" -> {
                // diagonal white streaks + fog band
                val p = Paper.strokePaint("#ffffff", 3f)
                p.alpha = 200
                val r = Random(tick / 3)
                repeat(60) {
                    val x = r.nextFloat() * (w + 200) - 100
                    val y = ((r.nextFloat() * h + tick * 7f) % h).toFloat()
                    c.drawLine(x, y, x - 44, y + 22, p)
                }
                c.drawRect(0f, h * 0.3f, w.toFloat(), h * 0.7f, Paper.fillPaint("#ffffff", 46))
            }
            "rain" -> {
                val p = Paper.strokePaint("#5a8ac8", 2.5f)
                p.alpha = 210
                val r = Random(tick / 3)
                repeat(80) {
                    val x = r.nextFloat() * w
                    val y = ((r.nextFloat() * h + tick * 9f) % h).toFloat()
                    c.drawLine(x, y, x - 8, y + 26, p)
                }
            }
            "sunny" -> {
                if (!night) {
                    val sx = w * 0.82f
                    val sy = h * 0.10f
                    c.drawCircle(sx, sy, 44f, Paper.fillPaint("#f2e08a", 120))
                    c.drawCircle(sx, sy, 30f, Paper.fillPaint("#f7ecc0"))
                    val glare = Paper.strokePaint("#f2e08a", 3f)
                    glare.alpha = 150
                    for (i in 0 until 8) {
                        val a = i * Math.PI / 4 + tick * 0.002
                        c.drawLine(
                            (sx + 52 * kotlin.math.cos(a)).toFloat(), (sy + 52 * kotlin.math.sin(a)).toFloat(),
                            (sx + 72 * kotlin.math.cos(a)).toFloat(), (sy + 72 * kotlin.math.sin(a)).toFloat(),
                            glare)
                    }
                }
            }
            "cloudy" -> {
                c.drawRect(0f, 0f, w.toFloat(), h * 0.16f, Paper.fillPaint("#9aa4ae", 90))
            }
            "fog" -> {
                // slow translucent bands drifting sideways — visibility soup
                val r = Random(7)
                for (i in 0 until 5) {
                    val yBase = h * (0.18f + i * 0.15f)
                    val drift = ((tick * (2 + i)) % (w * 2)) - w
                    val bandW = w * (0.7f + r.nextFloat() * 0.5f)
                    var x = (r.nextFloat() * w + drift) % (w * 1.4f)
                    if (x < 0) x += w * 1.4f
                    x -= w * 0.2f
                    c.drawRoundRect(RectF(x, yBase, x + bandW, yBase + h * 0.07f),
                        30f, 30f, Paper.fillPaint("#e8edf2", 52))
                }
                c.drawRect(0f, h * 0.55f, w.toFloat(), h.toFloat(), Paper.fillPaint("#dfe6ec", 30))
            }
            "wind" -> {
                // fast horizontal streaks + tumbling specks
                val p = Paper.strokePaint("#d8e2ea", 2.5f)
                p.alpha = 160
                val r = Random(tick / 2)
                repeat(26) {
                    val y = r.nextFloat() * h * 0.8f
                    val x = ((r.nextFloat() * w + tick * 14f) % (w + 120)) - 60f
                    c.drawLine(x, y, x + 34 + r.nextFloat() * 30, y - 3, p)
                }
                val lp = Paper.fillPaint("#b8c8a8", 200)
                repeat(7) {
                    val x = ((r.nextFloat() * w + tick * 11f) % (w + 60)) - 30f
                    val y = h * 0.3f + r.nextFloat() * h * 0.55f
                    c.drawCircle(x, y, 3f, lp)
                }
            }
        }
    }

    /** Fireworks (New Year / July 4) and drifting hearts (Valentine's). */
    fun drawHolidayParticles(c: Canvas, w: Int, h: Int, holiday: Holiday?, tick: Long) {
        if (holiday == null) return
        if ("fireworks" in holiday.flags) {
            val cols = listOf("#d43d2a", "#f2c230", "#2a6fd4", "#2a9a4a", "#ffffff")
            for (b in 0 until 3) {
                val phase = (tick / 40 + b * 23) % 90
                if (phase > 30) continue
                val fx = w * (0.2f + 0.3f * b) + sin(tick * 0.01f + b) * 20
                val fy = h * 0.18f + b * 30
                val rad = phase * 3.2f
                val p = Paper.strokePaint(cols[(b + tick / 40).toInt() % cols.size], 3f)
                p.alpha = (255 - phase * 8).coerceIn(0, 255).toInt()
                for (i in 0 until 12) {
                    val a = i * Math.PI / 6
                    c.drawLine(fx, fy,
                        (fx + rad * kotlin.math.cos(a)).toFloat(),
                        (fy + rad * kotlin.math.sin(a)).toFloat(), p)
                }
            }
        }
        if ("hearts" in holiday.flags) {
            val hp = Paper.fillPaint("#e87a8a", 200)
            val r = Random(tick / 30)
            repeat(10) {
                val x = r.nextFloat() * w
                val y = h - ((r.nextFloat() * h + tick * 0.8f) % h).toFloat()
                drawHeart(c, x, y, 8f, hp)
            }
        }
    }

    // ---------------- Ambient critters (small, paper style) ----------------
    // v0.16.0a (items 14+15): the critters ARE side-profile
    // drawings — body horizontal, head at the facing edge,
    // one eye. The ANIMAL expression set rides the profile:
    // lid, pupil and muzzle read from Expressions' animal
    // fields, keyed by [mood] (calm/alert/hungry/sleepy…).
    fun drawCritter(c: Canvas, species: String, x: Float, y: Float, s: Float, night: Boolean,
                    mood: String = "calm") {
        val dark = night
        when (species) {
            "squirrel" -> {
                val b = Paper.fillPaint(if (dark) "#4a3423" else "#8a5a34")
                c.drawOval(RectF(x - 10 * s, y - 8 * s, x + 10 * s, y + 8 * s), b)
                c.drawCircle(x + 9 * s, y - 8 * s, 6 * s, b)
                // tail
                c.drawPath(Path().apply {
                    moveTo(x - 8 * s, y)
                    quadTo(x - 22 * s, y - 18 * s, x - 14 * s, y - 26 * s)
                    quadTo(x - 6 * s, y - 18 * s, x - 8 * s, y)
                    close()
                }, b)
                c.drawCircle(x + 11 * s, y - 9 * s, 1.6f * s, Paper.fillPaint("#1a1a1a"))
            }
            "rabbit" -> {
                val b = Paper.fillPaint(if (dark) "#5a5a66" else "#9a9aa6")
                c.drawOval(RectF(x - 9 * s, y - 7 * s, x + 9 * s, y + 7 * s), b)
                c.drawCircle(x + 8 * s, y - 6 * s, 5.5f * s, b)
                c.drawOval(RectF(x + 5 * s, y - 24 * s, x + 9 * s, y - 10 * s), b)
                c.drawOval(RectF(x + 10 * s, y - 24 * s, x + 14 * s, y - 10 * s), b)
                c.drawCircle(x + 10 * s, y - 7 * s, 1.5f * s, Paper.fillPaint("#1a1a1a"))
            }
            "porcupine" -> {
                val b = Paper.fillPaint(if (dark) "#2e2e38" else "#4a4a58")
                c.drawOval(RectF(x - 11 * s, y - 7 * s, x + 11 * s, y + 7 * s), b)
                c.drawCircle(x + 10 * s, y - 4 * s, 4.5f * s, b)
                val sp = Paper.strokePaint(if (dark) "#4a4a58" else "#6a6a78", 1.6f * s)
                for (i in -3..3) {
                    c.drawLine(x + i * 4 * s, y - 6 * s, x + i * 5 * s, y - 14 * s, sp)
                }
                c.drawCircle(x + 11 * s, y - 5 * s, 1.4f * s, Paper.fillPaint("#1a1a1a"))
            }
            "beaver" -> {
                val b = Paper.fillPaint(if (dark) "#3d2c1e" else "#6b4a2f")
                c.drawOval(RectF(x - 11 * s, y - 7 * s, x + 11 * s, y + 7 * s), b)
                c.drawCircle(x + 10 * s, y - 5 * s, 5 * s, b)
                c.drawOval(RectF(x - 24 * s, y - 3 * s, x - 10 * s, y + 4 * s),
                    Paper.fillPaint(if (dark) "#2e2115" else "#4a3220")) // flat tail
                c.drawCircle(x + 12 * s, y - 6 * s, 1.5f * s, Paper.fillPaint("#1a1a1a"))
            }
            "field mouse" -> {
                val b = Paper.fillPaint(if (dark) "#5e5e68" else "#8a8a94")
                c.drawOval(RectF(x - 6 * s, y - 4 * s, x + 6 * s, y + 4 * s), b)
                c.drawCircle(x + 5 * s, y - 3 * s, 3.5f * s, b)
                c.drawCircle(x + 3 * s, y - 7 * s, 2 * s, b)
                c.drawCircle(x + 7 * s, y - 7 * s, 2 * s, b)
                val tp = Paper.strokePaint(if (dark) "#4a4a52" else "#6a6a74", 1.4f * s)
                c.drawLine(x - 6 * s, y, x - 16 * s, y - 4 * s, tp)
            }
            "christmas critter" -> {
                // the show's woodland critter: small, dead-eyed
                val b = Paper.fillPaint(if (dark) "#3a2c20" else "#6b5138")
                c.drawOval(RectF(x - 8 * s, y - 6 * s, x + 8 * s, y + 6 * s), b)
                c.drawCircle(x + 7 * s, y - 4 * s, 4.5f * s, b)
                // dead eyes: white with tiny pupils
                c.drawCircle(x + 6 * s, y - 5 * s, 2 * s, Paper.fillPaint("#ffffff"))
                c.drawCircle(x + 9 * s, y - 5 * s, 2 * s, Paper.fillPaint("#ffffff"))
                c.drawCircle(x + 6 * s, y - 5 * s, 0.8f * s, Paper.fillPaint("#1a1a1a"))
                c.drawCircle(x + 9 * s, y - 5 * s, 0.8f * s, Paper.fillPaint("#1a1a1a"))
                // tiny Santa hat
                c.drawPath(Path().apply {
                    moveTo(x + 3 * s, y - 8 * s); lineTo(x + 7 * s, y - 16 * s); lineTo(x + 11 * s, y - 8 * s); close()
                }, Paper.fillPaint("#d43d2a"))
                c.drawCircle(x + 7 * s, y - 16 * s, 1.8f * s, Paper.fillPaint("#ffffff"))
            }
        }
        // ---- the ANIMAL expression overlay (item 15):
        // lid over the profile eye + the muzzle curve,
        // both from the species' own expression — the head
        // anchor per species matches the drawings above ----
        val (hx, hy) = when (species) {
            "squirrel" -> (x + 9 * s) to (y - 8 * s)
            "rabbit" -> (x + 8 * s) to (y - 6 * s)
            "porcupine" -> (x + 10 * s) to (y - 4 * s)
            "beaver" -> (x + 10 * s) to (y - 5 * s)
            "field mouse" -> (x + 5 * s) to (y - 3 * s)
            else -> (x + 7 * s) to (y - 4 * s) // christmas critter
        }
        val exprA = Expressions.forMood(FaceKind.ANIMAL, mood)
        if (exprA.lid > 0.45f) {
            val lp = Paper.strokePaint("#1a1a1a", 1.5f * s)
            c.drawPath(Path().apply {
                moveTo(hx - 2.6f * s, hy - 1.2f * s)
                quadTo(hx, hy + (exprA.lid * 1.6f - 0.6f) * s,
                    hx + 2.6f * s, hy - 1.2f * s)
            }, lp)
        }
        // the muzzle: a small curve under the nose, its
        // bend from the expression's profile mouth curve
        val mp = Paper.strokePaint("#1a1a1a", 1.3f * s)
        val mzY = hy + 3.4f * s
        c.drawPath(Path().apply {
            moveTo(hx + 0.5f * s, mzY)
            quadTo(hx + 2.6f * s, mzY + exprA.profMouthCurve * 2.2f * s,
                hx + 4.6f * s, mzY - exprA.profMouthCurve * 0.8f * s)
        }, mp)
        if (exprA.profMouthOpen > 0.4f) {
            c.drawOval(RectF(hx + 2.2f * s, mzY, hx + 4.4f * s,
                mzY + 2.4f * s * exprA.profMouthOpen),
                Paper.fillPaint("#7a2a1a"))
        }
    }

    // ============================================================
    // v0.5.0g — the FOREGROUND layer (the scene depth pass).
    // The near strip between the crowd and the camera: each
    // location's own fence/hedge/curb/dock edge, drawn by
    // GameView AFTER the figures, inside the bottom strip
    // only (Depth.FG_TOP_OUT — the occlusion budget there is
    // pinned: the strip can touch ground and ankles, never a
    // face). One painter per FgKind, colored from the
    // location's palette, weather-worn and night-mixed like
    // every other layer. The token each location answers to
    // lives in GameData.LOC_FOREGROUND; the markers:
    // foreground: broken-fence-tops
    // foreground: picket-points
    // foreground: driveway-curb
    // foreground: marsh-hedge-lip
    // foreground: broflovski-hedge-lip
    // foreground: porch-rail-newkid
    // foreground: iron-fence-tops
    // foreground: busstop-snowbank
    // foreground: main-street-curb
    // foreground: tweek-planter-edge
    // foreground: jimbo-crate-stack
    // foreground: wok-step-edge
    // foreground: skeeter-porch-rail
    // foreground: bijou-rope-line
    // foreground: rhino-planter-tops
    // foreground: bank-step-lip
    // foreground: post-box-tops
    // foreground: taco-patio-rail
    // foreground: church-picket-tops
    // foreground: hall-step-lip
    // foreground: bike-rack-tops
    // foreground: park-fence-rail
    // foreground: pond-dock-planks
    // foreground: mall-planter-wall
    // foreground: sodo-patio-fence
    // foreground: farm-fence-tops
    // foreground: raisins-patio-rail
    // ============================================================
    fun drawForegroundLayer(c: Canvas, w: Int, h: Int, loc: Location,
                            weatherId: String, timeMin: Int, seed: Long) {
        val kind = Depth.foregroundKind(loc.id) ?: return
        val pal = LOC_PALETTES[loc.id]
        val strip = RectF(0f, h * Depth.FG_TOP_OUT, w.toFloat(), h.toFloat())
        drawForegroundStrip(c, strip, kind, pal?.fieldHex ?: "#8a8578",
            pal?.accentHex ?: "#e07b2a", timeOfDay(timeMin) == "night",
            weatherId, seed + loc.id.hashCode())
    }

    /** The strip painter, shared with PhotoView (a photo's
     *  foreground is the same near edge, in the photo's own
     *  bottom strip). All ink stays inside [strip]. */
    fun drawForegroundStrip(c: Canvas, strip: RectF, kind: FgKind,
                            primary: String, secondary: String,
                            night: Boolean, weatherId: String, seed: Long) {
        val L = strip.left; val T = strip.top
        val R = strip.right; val B = strip.bottom
        val W = R - L; val H = B - T
        if (W <= 0f || H <= 0f) return
        fun col(hex: String): String {
            val mixed = if (night) mix(hex, "#1c2745", 0.45f) else hex
            return if (night) mixed else SceneLight.wearHex(mixed, weatherId)
        }
        val pHex = col(primary)
        val sHex = col(secondary)
        val p = Paper.fillPaint(pHex)
        val dk = Paper.fillPaint(Paper.darker(pHex, 0.62f))
        val lt = Paper.fillPaint(mix(pHex, "#ffffff", 0.25f))
        // the seat shadow: a soft dark line where the near
        // strip meets the play space, so the eye reads "in
        // front of" instead of "painted on"
        c.drawRect(L, T, R, T + H * 0.07f, Paper.fillPaint("#000000", 42))
        // v0.16.0d (stage 3): the near strip is a large prop
        // surface — the paper material washes its whole band
        // (the kind's own slats and rails paint over it)
        Paper.applyMaterial(c, L, T, R, B, pHex, seed + 31L, 0.8f)
        when (kind) {
            FgKind.FENCE -> {
                var x = L + W * 0.02f
                while (x < R) {
                    c.drawRect(x, T, x + W * 0.014f, B, dk)
                    x += W / 9f
                }
                c.drawRect(L, T + H * 0.30f, R, T + H * 0.44f, p)
                c.drawRect(L, T + H * 0.63f, R, T + H * 0.77f, p)
            }
            FgKind.PICKETS -> {
                c.drawRect(L, T + H * 0.55f, R, T + H * 0.68f, p)
                val step = W / 15f
                var x = L + step * 0.3f
                while (x < R - step * 0.4f) {
                    val pw = step * 0.34f
                    c.drawRect(x, T + H * 0.14f, x + pw, B, p)
                    c.drawPath(Path().apply {
                        moveTo(x - pw * 0.12f, T + H * 0.14f)
                        lineTo(x + pw / 2, T)
                        lineTo(x + pw * 1.12f, T + H * 0.14f)
                        close()
                    }, p)
                    x += step
                }
            }
            FgKind.HEDGE -> {
                c.drawRect(L, T + H * 0.40f, R, B, p)
                for (i in 0 until 6) {
                    val cx = L + W * (i + 0.5f) / 6f
                    c.drawOval(RectF(cx - W * 0.105f, T - H * 0.06f,
                        cx + W * 0.105f, T + H * 0.62f), p)
                }
                for (i in 0 until 6) {
                    val cx = L + W * (i + 0.5f) / 6f - W * 0.03f
                    c.drawOval(RectF(cx - W * 0.05f, T + H * 0.02f,
                        cx + W * 0.05f, T + H * 0.30f),
                        Paper.fillPaint(sHex, 70))
                }
            }
            FgKind.SNOWBANK -> {
                val snow = if (night) "#33436a" else
                    SceneLight.wearHex("#eef4fa", weatherId)
                val shade = if (night) "#24304e" else
                    SceneLight.wearHex("#b9c9de", weatherId)
                for (i in 0 until 5) {
                    val cx = L + W * (i + 0.5f) / 5f
                    c.drawOval(RectF(cx - W * 0.17f, T + H * 0.06f,
                        cx + W * 0.17f, T + H * 1.15f), Paper.fillPaint(snow))
                }
                for (i in 0 until 5) {
                    val cx = L + W * (i + 0.5f) / 5f + W * 0.05f
                    c.drawOval(RectF(cx - W * 0.09f, T + H * 0.52f,
                        cx + W * 0.09f, T + H * 0.95f), Paper.fillPaint(shade, 130))
                }
                c.drawRect(L, T + H * 0.78f, R, B, Paper.fillPaint(snow))
            }
            FgKind.CURB -> {
                val asphalt = col("#4c4c50")
                c.drawRect(L, T, R, B, Paper.fillPaint(asphalt))
                c.drawRect(L, T, R, T + H * 0.17f,
                    Paper.fillPaint(mix(asphalt, "#ffffff", 0.22f)))
                val drain = Paper.fillPaint("#232328")
                for (fx in listOf(0.33f, 0.72f)) {
                    val dx = L + W * fx
                    c.drawRect(dx, T + H * 0.34f, dx + W * 0.075f, T + H * 0.86f, drain)
                    val slit = Paper.fillPaint(mix(asphalt, "#ffffff", 0.30f))
                    for (sIdx in 0 until 3) {
                        val sx = dx + W * 0.012f + sIdx * W * 0.022f
                        c.drawRect(sx, T + H * 0.40f, sx + W * 0.008f, T + H * 0.80f, slit)
                    }
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
            FgKind.PLANTER -> {
                val leaf = Paper.fillPaint(col("#4a7a3a"))
                for (i in 0 until 5) {
                    val cx = L + W * (i + 0.5f) / 5f
                    c.drawOval(RectF(cx - W * 0.08f, T - H * 0.05f,
                        cx + W * 0.08f, T + H * 0.52f), leaf)
                }
                c.drawRect(L, T + H * 0.36f, R, B, p)
                c.drawRect(L, T + H * 0.36f, R, T + H * 0.46f, dk)
            }
            FgKind.DOCK -> {
                for (j in 0 until 3) {
                    val y0 = T + H * (0.05f + 0.32f * j)
                    val y1 = y0 + H * 0.27f
                    c.drawRect(L, y0, R, y1, if (j % 2 == 0) p else dk)
                    val seam = Paper.strokePaint(
                        if (j % 2 == 0) mix(pHex, "#000000", 0.38f) else pHex, 2f)
                    var sx = L + W * (0.13f + 0.11f * j)
                    while (sx < R) {
                        c.drawLine(sx, y0 + 2f, sx, y1 - 2f, seam)
                        sx += W * 0.24f
                    }
                }
                c.drawRect(L + W * 0.03f, T, L + W * 0.065f, B, dk)
                c.drawRect(R - W * 0.065f, T, R - W * 0.03f, B, dk)
            }
            FgKind.ROPE -> {
                // v0.16.0b: posts every ~0.12 of the world, sized
                // off the strip height — at four posts per world
                // the rope sagged between off-screen posts and
                // read as a stray band floating over the road,
                // its knobs wider than a figure's head.
                val posts = (0 until 9).map { i -> L + W * (0.03f + 0.1175f * i) }
                for (px in posts) {
                    c.drawRect(px - H * 0.045f, T + H * 0.10f, px + H * 0.045f, B, dk)
                    c.drawCircle(px, T + H * 0.09f, H * 0.13f, p)
                }
                val rope = Paper.strokePaint(sHex, H * 0.075f, 235)
                for (i in 0 until posts.size - 1) {
                    val x1 = posts[i]; val x2 = posts[i + 1]
                    val path = Path()
                    path.moveTo(x1, T + H * 0.12f)
                    path.quadTo((x1 + x2) / 2, T + H * 0.46f, x2, T + H * 0.12f)
                    c.drawPath(path, rope)
                }
            }
            FgKind.STEPS -> {
                val x0 = L + W * 0.16f; val x1 = R - W * 0.16f
                c.drawRect(x0, T + H * 0.14f, x1, T + H * 0.50f, lt)
                c.drawRect(x0, T + H * 0.50f, x1, B, p)
                val edge = Paper.strokePaint(mix(pHex, "#000000", 0.38f), 2.5f)
                c.drawLine(x0, T + H * 0.50f, x1, T + H * 0.50f, edge)
                c.drawLine(x0, T + H * 0.14f, x1, T + H * 0.14f, edge)
            }
            FgKind.CRATES -> {
                fun crate(x0: Float, y0: Float, x1: Float) {
                    c.drawRect(x0, y0, x1, B, p)
                    val edge = Paper.strokePaint(mix(pHex, "#000000", 0.45f), 2.5f)
                    c.drawRect(x0, y0, x1, B, edge)
                    c.drawLine(x0, y0, x1, B, edge)
                    c.drawLine(x0, B, x1, y0, edge)
                }
                crate(L + W * 0.01f, T + H * 0.22f, L + W * 0.17f)
                crate(R - W * 0.17f, T + H * 0.32f, R - W * 0.01f)
                // v0.16.0b: the middle stack is two crates, not
                // one world-fraction slab (at 0.20 of the widened
                // world it read as an X-braced barricade panel
                // laid across the street)
                crate(L + W * 0.44f, T + H * 0.60f, R - W * 0.44f)
                crate(L + W * 0.455f, T + H * 0.38f, R - W * 0.47f)
            }
            FgKind.RACK -> {
                val bar = Paper.strokePaint(pHex, H * 0.065f)
                var x = L + W * 0.06f
                while (x < R - W * 0.03f) {
                    val path = Path()
                    path.moveTo(x - W * 0.028f, B)
                    path.quadTo(x - W * 0.028f, T + H * 0.02f, x, T + H * 0.04f)
                    path.quadTo(x + W * 0.028f, T + H * 0.02f, x + W * 0.028f, B)
                    c.drawPath(path, bar)
                    x += W / 9f
                }
            }
            else -> {
                // interior kinds never reach the exterior painter;
                // a plain band keeps any mistake quiet, not weird
                c.drawRect(L, T + H * 0.3f, R, B, p)
            }
        }
    }
}

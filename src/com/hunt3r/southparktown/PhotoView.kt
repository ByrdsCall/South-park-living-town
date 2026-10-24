package com.hunt3r.southparktown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.random.Random

// ============================================================
// PhotoView — renders a PhotoRecord (v0.2.0c). A photo is
// DATA; this view re-draws it on demand with the game's own
// figure renderer (KidArt) over a backdrop built from the
// location's palette — no bitmaps are ever stored. The same
// record renders the same picture in the album thumbnail,
// the full-screen viewer, forever.
//
// Layout: a polaroid — paper-white frame, the shot inside,
// the date + place stamped on the chin. Figures stand in
// rows like the scene renderer (back row smaller, higher);
// you stand front and center; a photobomber leans in from
// the right edge, bigger than life, drawn last over
// everybody. Bunny ears are drawn behind the head before
// the figure itself; hat swaps resolve through SheetSpec
// copies, so the figure system never grows a second style.
//
// v0.4.0c: records with a composition take drawGroupFigures
// instead — the figure list is already in frame order, and
// the composition (lineup / squad / stacked) names the
// geometry. Classic records render by the original layout,
// byte for byte.
// ============================================================
class PhotoView(context: Context) : View(context) {

    var record: PhotoRecord? = null

    private fun hexOf(color: Int): String = "#%06X".format(0xFFFFFF and color)

    /** v0.3.0e: the month inside a photo's dayKey (yyyyMMdd),
     *  for SkyState's season. Legacy/blank keys read as
     *  January — the sky falls back gracefully either way. */
    private fun monthOfKey(dayKey: String): Int =
        if (dayKey.length == 8)
            dayKey.substring(4, 6).toIntOrNull() ?: 1
        else 1

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val rec = record ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        drawInto(c, rec, RectF(0f, 0f, w, h))
    }

    /** v0.9.0d: the polaroid, painted into any rect on any
     *  canvas — the album's one renderer, shared with the
     *  darkroom's print wall (GameView hangs the actual
     *  prints with THIS code), so a print on the wall is
     *  the print in the book: one renderer, no drift. */
    fun drawInto(c: Canvas, rec: PhotoRecord, frame: RectF) {
        val w = frame.width()
        val h = frame.height()
        if (w <= 0 || h <= 0) return

        // ---- polaroid frame ----
        c.drawRoundRect(frame, 10f, 10f, Paper.fillPaint("#f7f2e6"))
        c.drawRoundRect(frame, 10f, 10f,
            Paper.strokePaint("#d8cdb2", 3f))
        val insetX = frame.left + w * 0.05f
        val top = frame.top + h * 0.05f
        val bottom = frame.top + h * 0.82f
        val photo = RectF(insetX, top, frame.right - w * 0.05f, bottom)

        c.save()
        c.clipRect(photo)
        drawBackdrop(c, rec, photo)
        drawFigures(c, rec, photo)
        drawForeground(c, rec, photo)
        c.restore()
        c.drawRect(photo, Paper.strokePaint("#000000", 2f, 60))

        // ---- chin stamp: when + where ----
        val stamp = Paper.textPaint("#8a7a5c", h * 0.052f, bold = true, casual = true)
        stamp.textAlign = Paint.Align.LEFT
        val locName = LOCATIONS[rec.locId]?.name ?: rec.locId
        c.drawText("📸 ${rec.dateLabel} · $locName", insetX + 2f,
            frame.top + h * 0.945f, stamp)
        val cam = Paper.textPaint("#8a7a5c", h * 0.05f, casual = true)
        cam.textAlign = Paint.Align.RIGHT
        c.drawText("say cheese!", frame.right - w * 0.05f - 2f,
            frame.top + h * 0.945f, cam)
    }

    // ---------------- backdrop ----------------

    private fun drawBackdrop(c: Canvas, rec: PhotoRecord, r: RectF) {
        val pal = LOC_PALETTES[rec.locId]
        // v0.3.0a: the selfie reads the SAME light state as the
        // scene renderer — SceneLight's band table (day/dusk/
        // night) instead of a private hour rule that called
        // 6 PM midnight — and its ground wears the same weather
        // wear, so a photo can't contradict the place it was in.
        val band = SceneLight.bandOf(rec.timeMin)
        val night = band == "night"
        val dusk = band == "dusk"
        // v0.3.0e: the photo remembers its sky — SkyState from
        // the record's own capture day (rec.absDay), the same
        // function the live scene reads, so a selfie can never
        // argue with the night it was taken in. Records from
        // before the field existed (absDay -1) keep the plain
        // legacy moon.
        val sky = if (rec.absDay >= 0) SkyState.now(rec.absDay, rec.timeMin,
            rec.weatherId, seasonFor(monthOfKey(rec.dayKey))) else null
        if (rec.inside) {
            val wall = pal?.wallHex ?: "#a8c8e8"
            val floor = pal?.floorHex ?: "#8a6a4a"
            c.drawRect(r.left, r.top, r.right, r.top + r.height() * 0.60f,
                Paper.fillPaint(wall))
            c.drawRect(r.left, r.top + r.height() * 0.60f, r.right, r.bottom,
                Paper.fillPaint(floor))
            c.drawRect(r.left, r.top + r.height() * 0.585f, r.right,
                r.top + r.height() * 0.615f, Paper.fillPaint(Paper.darker(floor)))
            // a window with the day (or night) outside it
            val win = RectF(r.left + r.width() * 0.66f, r.top + r.height() * 0.10f,
                r.left + r.width() * 0.90f, r.top + r.height() * 0.44f)
            c.drawRect(win, Paper.fillPaint(when {
                night -> "#2a3450"; dusk -> "#e8975a"; else -> "#b8d4ee"
            }))
            c.drawRect(win, Paper.strokePaint(hexOf(Paper.darker(wall)), 4f))
            c.drawLine(win.centerX(), win.top, win.centerX(), win.bottom,
                Paper.strokePaint(hexOf(Paper.darker(wall)), 3f))
            c.drawLine(win.left, win.centerY(), win.right, win.centerY(),
                Paper.strokePaint(hexOf(Paper.darker(wall)), 3f))
            if (night) {
                // v0.3.0e: the window's moon is the night's own
                // moon — harvest-orange on its night, and an
                // aurora washes the glass green.
                if (sky?.special == SkyState.AURORA) {
                    c.drawRect(win, Paper.fillPaint("#3ae88a", 42))
                }
                c.drawCircle(win.left + win.width() * 0.72f,
                    win.top + win.height() * 0.28f, win.width() * 0.09f,
                    Paper.fillPaint(if (sky?.harvest == true)
                        "#f08a3a" else "#e8e4c8"))
                // the streetlamp outside, same as interiors show
                if (SceneLight.lampsOf(rec.locId) > 0) {
                    val lw = SceneLight.warmthOf(rec.locId)
                    c.drawOval(RectF(win.left + 2, win.bottom - win.height() * 0.22f,
                        win.left + win.width() * 0.28f, win.bottom + 2),
                        Paper.fillPaint(lw, 55))
                }
            } else if (dusk) {
                c.drawCircle(win.left + win.width() * 0.24f,
                    win.top + win.height() * 0.68f, win.width() * 0.10f,
                    Paper.fillPaint("#f28a4a"))
            } else {
                c.drawCircle(win.left + win.width() * 0.26f,
                    win.top + win.height() * 0.30f, win.width() * 0.11f,
                    Paper.fillPaint("#f2c230"))
            }
        } else {
            // sky + ground in the location's own field colors
            c.drawRect(r, Paper.fillPaint(when {
                night -> "#2a3450"; dusk -> "#d9905f"; else -> "#a8c8e8"
            }))
            val horizon = r.top + r.height() * 0.62f
            // mountains on the horizon (the town is IN the Rockies)
            val mtn = Paper.fillPaint(if (night) "#3d4a68" else "#7a94b8")
            val snow = Paper.fillPaint("#f4f4f4")
            c.drawPath(android.graphics.Path().apply {
                moveTo(r.left, horizon)
                lineTo(r.left + r.width() * 0.22f, r.top + r.height() * 0.18f)
                lineTo(r.left + r.width() * 0.44f, horizon)
                close()
            }, mtn)
            c.drawPath(android.graphics.Path().apply {
                moveTo(r.left + r.width() * 0.36f, horizon)
                lineTo(r.left + r.width() * 0.60f, r.top + r.height() * 0.10f)
                lineTo(r.left + r.width() * 0.84f, horizon)
                close()
            }, mtn)
            c.drawPath(android.graphics.Path().apply {
                moveTo(r.left + r.width() * 0.185f, r.top + r.height() * 0.245f)
                lineTo(r.left + r.width() * 0.22f, r.top + r.height() * 0.18f)
                lineTo(r.left + r.width() * 0.255f, r.top + r.height() * 0.245f)
                close()
            }, snow)
            c.drawPath(android.graphics.Path().apply {
                moveTo(r.left + r.width() * 0.558f, r.top + r.height() * 0.185f)
                lineTo(r.left + r.width() * 0.60f, r.top + r.height() * 0.10f)
                lineTo(r.left + r.width() * 0.642f, r.top + r.height() * 0.185f)
                close()
            }, snow)
            if (night) {
                // v0.3.0e: the SAME sky the scene showed —
                // aurora washes, meteor streaks, and the moon
                // in its true phase (harvest: orange, oversized)
                if (sky?.special == SkyState.AURORA) {
                    val washCols = listOf("#3ae88a", "#2fd4b8", "#8a6ae8")
                    for (i in washCols.indices) {
                        c.drawOval(RectF(r.left - r.width() * 0.1f,
                            r.top + r.height() * (0.02f + i * 0.09f),
                            r.right + r.width() * 0.1f,
                            r.top + r.height() * (0.16f + i * 0.09f)),
                            Paper.fillPaint(washCols[i], 44 - i * 11))
                    }
                }
                val harvestMoon = sky?.harvest == true
                val moonR = r.width() * 0.035f * if (harvestMoon) 1.45f else 1f
                val moonX = r.left + r.width() * 0.86f
                val moonY = r.top + r.height() * 0.14f
                c.drawCircle(moonX, moonY, moonR,
                    Paper.fillPaint(if (harvestMoon) "#f08a3a" else "#e8e4c8"))
                val phase = sky?.phase ?: 4
                if (sky != null && phase != 4) {
                    val off = (phase - 4) / 4f * moonR * 1.6f
                    c.drawCircle(moonX + off, moonY, moonR * 0.92f,
                        Paper.fillPaint("#2a3450"))
                }
                if (sky?.special == SkyState.METEOR) {
                    val streak = Paper.strokePaint("#ffffff", 2.5f, 210)
                    val tail = Paper.strokePaint("#ffffff", 1.6f, 90)
                    val srnd = Random(rec.seed + 4242)
                    repeat(3) {
                        val sx = r.left + r.width() * (0.15f + srnd.nextFloat() * 0.55f)
                        val sy = r.top + r.height() * (0.05f + srnd.nextFloat() * 0.18f)
                        val ln = r.width() * 0.07f
                        c.drawLine(sx, sy, sx - ln, sy + ln * 0.45f, streak)
                        c.drawLine(sx - ln, sy + ln * 0.45f,
                            sx - ln * 1.7f, sy + ln * 0.77f, tail)
                    }
                }
                val rnd = Random(rec.seed + 7)
                val star = Paper.fillPaint("#f4f4f4", 200)
                repeat(14) {
                    c.drawCircle(r.left + rnd.nextFloat() * r.width(),
                        r.top + rnd.nextFloat() * r.height() * 0.4f,
                        1.6f, star)
                }
            } else if (dusk) {
                c.drawCircle(r.left + r.width() * 0.84f, r.top + r.height() * 0.52f,
                    r.width() * 0.042f, Paper.fillPaint("#f28a4a"))
                // v0.3.0e: on its night, the harvest moon is
                // already up at dusk — low, orange, enormous.
                if (sky?.harvest == true) {
                    c.drawCircle(r.left + r.width() * 0.14f,
                        r.top + r.height() * 0.34f, r.width() * 0.05f,
                        Paper.fillPaint("#f08a3a", 225))
                }
            } else {
                c.drawCircle(r.left + r.width() * 0.86f, r.top + r.height() * 0.13f,
                    r.width() * 0.045f, Paper.fillPaint("#f2c230"))
            }
            // v0.4.0a: the photo's skyline is the scene's
            // skyline — SceneArt.drawHorizon over Horizon.kt's
            // derivation for the record's own place, hour and
            // weather, standing on the same horizon line the
            // backdrop's mountains use. A selfie can no more
            // invent a neighborhood than it can invent a sky.
            SceneArt.drawHorizon(c, r.left, horizon, r.width(), r.height(),
                rec.locId, band, rec.weatherId)
            // the ground itself wears the location's field color,
            // blanketed white whenever the weather is snowy
            val fieldHex = SceneLight.wearHex(pal?.fieldHex ?: "#e8e8e8",
                rec.weatherId)
            c.drawRect(r.left, horizon, r.right, r.bottom,
                Paper.fillPaint(fieldHex))
            // v0.16.0d (stage 3): the still's ground is paper
            // stock too — the material wash under the figures
            Paper.applyMaterial(c, r.left, horizon, r.right, r.bottom,
                fieldHex, rec.seed + 17L, 0.8f)
            if (rec.weatherId in setOf("light_snow", "heavy_snow", "blizzard",
                    "sunny", "partly_cloudy", "cloudy", "wind", "fog")) {
                c.drawRect(r.left, horizon, r.right, r.bottom,
                    Paper.fillPaint("#f4f4f4", 165))
            }
        }
        // weather in the frame
        val rnd = Random(rec.seed + 31)
        when (rec.weatherId) {
            "light_snow", "heavy_snow", "blizzard" -> {
                val flake = Paper.fillPaint("#ffffff", 220)
                val n = if (rec.weatherId == "light_snow") 22 else 44
                repeat(n) {
                    c.drawCircle(r.left + rnd.nextFloat() * r.width(),
                        r.top + rnd.nextFloat() * r.height(),
                        2f + rnd.nextFloat() * 2f, flake)
                }
            }
            "rain", "storm" -> {
                val drop = Paper.strokePaint("#7aa8c8", 2f, 170)
                repeat(30) {
                    val x = r.left + rnd.nextFloat() * r.width()
                    val y = r.top + rnd.nextFloat() * r.height()
                    c.drawLine(x, y, x - 3f, y + 9f, drop)
                }
            }
            "fog" -> c.drawRect(r.left, r.top + r.height() * 0.3f, r.right,
                r.bottom, Paper.fillPaint("#f0f0f0", 90))
        }
        // the place's name on a little paper sign, bottom-left
        val locName = LOCATIONS[rec.locId]?.name ?: rec.locId
        Paper.drawSign(c, r.left + r.width() * 0.16f, r.bottom - r.height() * 0.055f,
            r.width() * 0.30f, r.height() * 0.085f, locName, r.height() * 0.048f)
    }

    // ---------------- figures ----------------

    private fun baseSpecFor(fig: PhotoFigure, rec: PhotoRecord): SheetSpec =
        when {
            // v0.10.0a: your parents derive from the record's
            // OWN appearance map — a family photo remembers the
            // family as it looked the day it was taken
            Family.isParent(fig.name) -> Family.specFor(fig.name, rec.appearance)
            fig.kind == "player" -> avatarSpec(rec.appearance)
            fig.kind == "cast" -> KidArt.SHEETS[fig.name] ?: SheetSpec()
            else -> VisitorSystem.personaByName(fig.name)?.spec ?: SheetSpec()
        }

    /** The spec a figure is drawn with: hat swaps copy the
     *  partner's hat onto this figure (and off again — the
     *  partner's own entry points back). */
    private fun specFor(fig: PhotoFigure, rec: PhotoRecord): SheetSpec {
        val base = baseSpecFor(fig, rec)
        if (fig.pose != "swap" || fig.swapWith == null) return base
        val partner = rec.figures.firstOrNull { it.name == fig.swapWith }
            ?: return base
        val pSpec = baseSpecFor(partner, rec)
        return base.copy(hatId = pSpec.hatId, hatColor = pSpec.hatColor,
            hatAccent = pSpec.hatAccent)
    }

    private fun figScale(fig: PhotoFigure): Float = when (fig.kind) {
        "cast" -> KidArt.heightFor(fig.name)
        "player" -> 1f
        else -> {
            val band = VisitorSystem.personaByName(fig.name)?.band
            (if (band != null) CharacterSystem.heightScale(band) else 1f) * 0.96f
        }
    }

    private fun drawOne(c: Canvas, fig: PhotoFigure, rec: PhotoRecord,
                        cx: Float, feetY: Float, fh: Float,
                        flip: Boolean = false) {
        val spec = specFor(fig, rec)
        val effH = fh * figScale(fig)   // feet land exactly on feetY
        if (fig.pose == "bunny") drawBunnyEars(c, cx, feetY - effH, effH)
        // v0.10.0b: a figure the shutter caught in costume
        // renders in costume — the record's frozen hero code
        // drives KidArt's hero pass (cape from the code, mask
        // and emblem over the figure's own spec), state-free,
        // forever. Street clothes render exactly as before.
        // v0.12.0d: a figure the shutter caught in pajamas
        // renders in pajamas — the record's frozen sleepwear
        // code drives KidArt's sleep pass the same way. The
        // two freezes are mutually exclusive in honest play
        // (patrol nights are Wed/Sat, sleepovers Friday, and
        // only one stamp can read its moment); sleepwear is
        // checked first so a record somehow carrying both
        // still renders the night it was taken in.
        fun drawFig() {
            // v0.16.0d (stage 10): a photo is a STILL — its
            // figures render through the PaperDoll path (front
            // pose, doll clock, portrait-weight outline), not
            // the world renderer. This closes partial #14:
            // front-posing stills are correct doll behavior.
            if (fig.sleep.isNotEmpty())
                PaperDoll.drawDollSleep(c, spec, fig.sleep, cx,
                    feetY - effH / 2f, effH, fig.mood,
                    fig.name.hashCode().toLong())
            else if (fig.hero.isNotEmpty())
                PaperDoll.drawDollHero(c, spec, fig.hero, cx,
                    feetY - effH / 2f, effH, fig.mood,
                    fig.name.hashCode().toLong())
            else
                PaperDoll.drawDoll(c, spec, cx, feetY - effH / 2f, effH,
                    fig.mood, fig.name.hashCode().toLong())
        }
        if (!flip) {
            drawFig()
        } else {
            // v0.4.0c: mirrored around the figure's own center
            // — the cheap, honest "turned away": every
            // asymmetric detail (hair part, hat accent) swaps
            // sides, so a back-to-back pair reads as two kids
            // facing OUT, backs together.
            c.save()
            c.translate(cx, 0f)
            c.scale(-1f, 1f)
            c.translate(-cx, 0f)
            drawFig()
            c.restore()
        }
    }

    /** The photobomber lean-in: bigger than life, tilted,
     *  drawn over everybody from the right edge. Shared by
     *  the classic selfie layout and the group frames —
     *  a crasher crashes the same way in both. */
    private fun drawBomber(c: Canvas, b: PhotoFigure, rec: PhotoRecord,
                           r: RectF, groundY: Float) {
        val fh = r.height() * 0.98f
        val cx = r.right - r.width() * 0.10f
        c.save()
        c.translate(cx, groundY - fh / 2f)
        c.rotate(-7f)
        c.translate(-cx, -(groundY - fh / 2f))
        drawOne(c, b, rec, cx, groundY, fh)
        c.restore()
    }

    private fun drawBunnyEars(c: Canvas, cx: Float, topY: Float, fh: Float) {
        // two tall ears peeking over the head, pink inside —
        // drawn BEFORE the figure so the head overlaps their base
        val earW = fh * 0.085f
        val earH = fh * 0.24f
        val cyEar = topY + earH * 0.30f
        for ((dx, tilt) in listOf(-0.085f to -13f, 0.085f to 13f)) {
            c.save()
            c.translate(cx + fh * dx, cyEar)
            c.rotate(tilt)
            c.drawOval(RectF(-earW / 2, -earH / 2, earW / 2, earH / 2),
                Paper.fillPaint("#f4f4f4"))
            c.drawOval(RectF(-earW * 0.26f, -earH * 0.32f, earW * 0.26f, earH * 0.32f),
                Paper.fillPaint("#e87a9a"))
            c.restore()
        }
    }

    // v0.5.0g: the photo's FOREGROUND strip — the same near
    // edge the scene wears (Depth's bands, same painters),
    // drawn over the figures' feet inside the photo's own
    // bottom strip, so a group photo sits IN the place the
    // way the scene does instead of floating in front of it
    private fun drawForeground(c: Canvas, rec: PhotoRecord, r: RectF) {
        val night = SceneLight.bandOf(rec.timeMin) == "night"
        val strip = RectF(r.left, r.bottom - r.height() * 0.055f,
            r.right, r.bottom)
        if (rec.inside) {
            val def = interiorOf(rec.locId) ?: return
            val kind = Depth.interiorForegroundKind(def.theme) ?: return
            InteriorArt.drawForegroundStrip(c, strip, kind, def.floorHex,
                def.wallHex, night, rec.locId.hashCode().toLong())
        } else {
            val kind = Depth.foregroundKind(rec.locId) ?: return
            val pal = LOC_PALETTES[rec.locId]
            SceneArt.drawForegroundStrip(c, strip, kind,
                pal?.fieldHex ?: "#8a8578", pal?.accentHex ?: "#e07b2a",
                night, rec.weatherId, rec.locId.hashCode().toLong())
        }
    }

    private fun drawFigures(c: Canvas, rec: PhotoRecord, r: RectF) {
        // v0.4.0c: group records carry a composition and a
        // figure list already in frame order — they take the
        // group layout. Everything else (every record from
        // before v0.4.0c, every classic selfie since) renders
        // by the original layout below, untouched.
        if (rec.composition.isNotEmpty()) {
            drawGroupFigures(c, rec, r)
            return
        }
        val normals = rec.figures.filter { it.pose != "bomb" }
        val bombers = rec.figures.filter { it.pose == "bomb" }
        if (normals.isEmpty()) return
        val player = normals.firstOrNull { it.kind == "player" }
        val others = normals.filter { it.kind != "player" }
        // front row: you in the middle, flanked; overflow goes back
        val frontOthers = others.take(3)
        val back = others.drop(3)
        val front = mutableListOf<PhotoFigure>()
        val half = frontOthers.size / 2
        front.addAll(frontOthers.take(half))
        if (player != null) front.add(player)
        front.addAll(frontOthers.drop(half))

        val groundY = r.bottom - r.height() * 0.045f
        fun row(figs: List<PhotoFigure>, feetY: Float, maxH: Float) {
            if (figs.isEmpty()) return
            val slotW = r.width() * 0.94f / figs.size
            val fh = minOf(maxH, slotW * 1.18f)
            figs.forEachIndexed { i, fig ->
                val cx = r.left + r.width() * 0.03f + slotW * (i + 0.5f)
                drawOne(c, fig, rec, cx, feetY, fh)
            }
        }
        if (back.isNotEmpty()) {
            row(back, groundY - r.height() * 0.17f, r.height() * 0.60f)
        }
        row(front, groundY, r.height() * 0.80f)
        // the photobomber leans in over everybody, front-right
        for (b in bombers) drawBomber(c, b, rec, r, groundY)
    }

    /** v0.4.0c: the group layouts. The record's figure order
     *  IS the frame order — composeGroup already arranged
     *  it (chemistry adjacent, rivals gapped, the player in
     *  their slot) — so these layouts only name geometry:
     *  LINEUP one front row; SQUAD 2 back / 2 front, the
     *  back row narrowed so it peeks between the front;
     *  STACKED the pile, the back half climbed on and
     *  squeezed into the middle with their heads over the
     *  front row. In every composition the FIRST figure of
     *  a backToBack pair is drawn mirrored — turned away
     *  from their partner, arms crossed by mood. The
     *  crasher leans in last, over everybody.
     *  v0.4.0g: the geometry itself lives in
     *  Photos.GroupGeom (pure, suite-measured — the same
     *  fractions, plus its headroom cap); this only paints
     *  the placements it hands back, in its draw order. */
    private fun drawGroupFigures(c: Canvas, rec: PhotoRecord, r: RectF) {
        val normals = rec.figures.filter { it.pose != "bomb" }
        val bombers = rec.figures.filter { it.pose == "bomb" }
        if (normals.isEmpty()) return
        val groundY = r.bottom - r.height() * 0.045f
        val figs = normals.map { fig ->
            Photos.GroupGeom.Fig(figScale(fig),
                Photos.halfWidthFrac(specFor(fig, rec)),
                fig.pose == "backToBack")
        }
        val placed = Photos.GroupGeom.layout(rec.composition, figs,
            r.width() / r.height())
        for (p in placed) {
            drawOne(c, normals[p.index], rec,
                r.left + p.cx * r.width(),
                r.top + p.feet * r.height(),
                p.h * r.height(), p.flipped)
        }
        for (b in bombers) drawBomber(c, b, rec, r, groundY)
    }
}

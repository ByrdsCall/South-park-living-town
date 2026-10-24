package com.hunt3r.southparktown

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sin

// ============================================================
// MinigameViews — the Android half of v0.2.0e's minigames:
// three custom views that DRAW and TAKE TAPS for the pure
// sessions in Minigames.kt. All rules (bite windows, duel
// math, spawn schedules, scoring) live in the sessions —
// these views own clocks, canvas and callbacks only, the
// same split Photos/PhotoView use.
//
// Each view reports its settled session through ONE
// callback; GameActivity applies the rewards there (once —
// the sessions' settled flags see to it) and hands back
// the text the view shows over the final frame.
// ============================================================

private fun centerText(c: Canvas, txt: String, x: Float, y: Float, size: Float,
                       colorHex: String, bold: Boolean = true) {
    val p = Paper.textPaint(colorHex, size, bold = bold, casual = true)
    p.textAlign = Paint.Align.CENTER
    c.drawText(txt, x, y, p)
}

// ============================================================
// 🎣 Fishing
// ============================================================
class FishingView(context: Context) : View(context) {

    /** Builds the next cast's session (fresh seed per cast). */
    var sessionFactory: () -> FishingSession = { FishingSession(0L, "light_snow", 12) }
    /** A cast happened (the Activity counts it). */
    var onCast: () -> Unit = {}
    /** The fish is hooked and settled; returns the result line. */
    var onCaught: (FishingSession) -> String = { "" }
    var playerSpec: SheetSpec = SheetSpec()

    private var session: FishingSession? = null
    private var castAtMs = 0L
    private var running = false
    private var biteFired = false
    private var resolvedFired = false
    private var headline = ""
    private var subline = ""
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            val s = session ?: return
            val before = s.phase
            s.tick(nowMs())
            if (s.phase == FishingSession.Phase.BITE &&
                before == FishingSession.Phase.WAITING && !biteFired) {
                biteFired = true
                SoundEngine.fishBite()
            }
            if (s.phase == FishingSession.Phase.OVER && !resolvedFired) {
                resolvedFired = true
                onResolved()
            }
            invalidate()
            if (running) handler.postDelayed(this, 50)
        }
    }

    fun begin() = newCast()

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis() - castAtMs

    private fun newCast() {
        session = sessionFactory()
        castAtMs = SystemClock.uptimeMillis()
        biteFired = false
        resolvedFired = false
        headline = ""
        subline = ""
        onCast()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
        invalidate()
    }

    private fun onResolved() {
        val s = session ?: return
        when (s.outcome) {
            FishingSession.Outcome.CAUGHT -> {
                SoundEngine.fishCatch()
                headline = onCaught(s)
                subline = "Tap to cast again · Done when you're done"
            }
            FishingSession.Outcome.ESCAPED -> {
                headline = "It got away…"
                subline = "The pond keeps its secrets. Tap to cast again."
            }
            FishingSession.Outcome.SCARED -> {
                headline = "Too soon! You scared it off."
                subline = "Wait for the dip, THEN tap. Tap to cast again."
            }
            FishingSession.Outcome.NONE -> {}
        }
        invalidate()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            val s = session ?: return true
            if (s.phase == FishingSession.Phase.OVER) {
                newCast()
            } else {
                SoundEngine.tap()
                s.tap(nowMs())
                if (s.phase == FishingSession.Phase.OVER && !resolvedFired) {
                    resolvedFired = true
                    onResolved()
                }
                invalidate()
            }
            return true
        }
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()

        // sky, far shore, water
        c.drawRect(0f, 0f, w, h * 0.40f, Paper.fillPaint("#a8c8e8"))
        c.drawRect(0f, h * 0.36f, w, h * 0.44f, Paper.fillPaint("#7a94b8"))
        c.drawRect(0f, h * 0.44f, w, h, Paper.fillPaint("#4a6a94"))
        val streak = Paper.strokePaint("#dcecfc", 2f, 70)
        for (i in 0..3) {
            val yy = h * (0.55f + i * 0.11f)
            c.drawLine(w * (0.08f + i * 0.07f), yy, w * (0.30f + i * 0.07f), yy, streak)
        }
        // the dock, coming in from the left
        c.drawRect(0f, h * 0.76f, w * 0.34f, h * 0.82f, Paper.fillPaint("#8a6a4a"))
        c.drawRect(0f, h * 0.76f, w * 0.34f, h * 0.785f, Paper.fillPaint("#a5855c"))
        c.drawRect(w * 0.05f, h * 0.82f, w * 0.09f, h * 0.97f, Paper.fillPaint("#6e5236"))
        c.drawRect(w * 0.26f, h * 0.82f, w * 0.30f, h * 0.97f, Paper.fillPaint("#6e5236"))
        // you, on the dock, holding the rod like you mean it
        KidArt.drawFigure(c, playerSpec, w * 0.15f, h * 0.615f, h * 0.33f,
            if (s.phase == FishingSession.Phase.BITE) "happy" else "idle", "stand",
            FigureAnim(timeMs = now, seed = 7))
        val handX = w * 0.21f
        val handY = h * 0.60f
        val tipX = w * 0.56f
        val tipY = h * 0.30f
        val rod = Paper.strokePaint("#5a4028", 3.5f)
        c.drawLine(handX, handY, tipX, tipY, rod)

        // bobber on the water, line down from the tip
        val bx = w * 0.63f
        val waterY = h * 0.68f
        val biting = s.phase == FishingSession.Phase.BITE
        val dip = if (biting) 10f + sin(now * 0.05) * 3f
            else sin(now * 0.004) * 3f
        val by = waterY + dip.toFloat()
        val line = Paper.strokePaint("#f4f4f4", 1.6f, 200)
        c.drawLine(tipX, tipY, bx, by, line)
        if (biting) {
            val rr = ((now % 500) / 500f) * 30f + 8f
            c.drawOval(RectF(bx - rr, by - rr * 0.3f, bx + rr, by + rr * 0.3f),
                Paper.strokePaint("#ffffff", 2.5f, (190 - rr * 4).toInt().coerceIn(30, 190)))
        }
        c.drawCircle(bx, by, 8f, Paper.fillPaint("#f4f4f4"))
        c.drawArc(RectF(bx - 8f, by - 8f, bx + 8f, by + 8f), 0f, 180f, true,
            Paper.fillPaint(Paper.RED))
        if (biting) centerText(c, "!", bx, by - 20f, 30f, Paper.YELLOW)

        // the catch, mid-leap, at its proudest
        if (s.outcome == FishingSession.Outcome.CAUGHT && s.species != null) {
            val sp = s.species!!
            val leap = sin(((now % 900) / 900f) * Math.PI).toFloat()
            centerText(c, sp.icon, w * 0.63f, waterY - 26f - leap * 22f, 46f, "#ffffff")
        }

        // words
        when {
            headline.isNotEmpty() -> {
                centerText(c, headline, w / 2f, h * 0.16f, 21f, "#ffffff")
                if (subline.isNotEmpty())
                    centerText(c, subline, w / 2f, h * 0.16f + 26f, 13.5f, "#dcecfc", bold = false)
            }
            biting -> centerText(c, "❗ BITE! TAP!", w / 2f, h * 0.16f, 27f, Paper.YELLOW)
            else -> {
                centerText(c, "🎣 Waiting for a bite…", w / 2f, h * 0.16f, 19f, "#ffffff")
                centerText(c, "tap the moment the bobber dips", w / 2f,
                    h * 0.16f + 25f, 13f, "#dcecfc", bold = false)
            }
        }
    }
}

// ============================================================
// ❄️ Snowball Showdown
// ============================================================
class ShowdownView(context: Context) : View(context) {

    var duel: ShowdownDuel? = null
    var playerSpec: SheetSpec = SheetSpec()
    var playerName: String = "You"
    /** The duel is over and settled; returns the result lines. */
    var onDuelOver: (ShowdownDuel) -> List<String> = { emptyList() }

    private enum class Ui { INTRO, BAR, THROW_FLY, WINDUP, DODGE_FLY, REACT, OVER }

    private var ui: Ui = Ui.INTRO
    private var uiStartMs = 0L
    private var running = false
    private var releasePos = 0f
    private var throwHit = false
    private var dodgeResolved = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var youMood = "idle"
    private var oppMood = "idle"
    private var shakeUntilMs = 0L
    private var flashText = ""
    private var flashUntilMs = 0L
    private var reactText = ""
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 30)
        }
    }

    fun begin() {
        ui = Ui.INTRO
        uiStartMs = SystemClock.uptimeMillis()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis()
    private fun uiElapsed(): Long = nowMs() - uiStartMs

    private fun go(next: Ui) {
        ui = next
        uiStartMs = nowMs()
    }

    private fun step() {
        val d = duel ?: return
        when (ui) {
            Ui.INTRO -> if (uiElapsed() > 1000) go(Ui.BAR)
            Ui.THROW_FLY -> if (uiElapsed() > 430) {
                if (throwHit) {
                    oppMood = "hurt"
                    shakeUntilMs = nowMs() + 280
                    flashText = "🎯 SPLAT!"
                } else {
                    flashText = "💨 Whoosh — wide!"
                }
                flashUntilMs = nowMs() + 750
                if (d.over) finish() else go(Ui.WINDUP)
            }
            Ui.WINDUP -> if (uiElapsed() > 600) {
                dodgeResolved = false
                go(Ui.DODGE_FLY)
            }
            Ui.DODGE_FLY -> if (uiElapsed() >= Showdown.FLIGHT_MS && !dodgeResolved) {
                dodgeResolved = true
                resolveDodge(null)
            }
            Ui.REACT -> if (uiElapsed() > 750) {
                youMood = "idle"; oppMood = "idle"
                if (d.over) finish() else go(Ui.BAR)
            }
            else -> {}
        }
    }

    private fun finish() {
        val d = duel ?: return
        youMood = if (d.youWon) "happy" else "sad"
        oppMood = if (d.youWon) "sad" else "happy"
        go(Ui.OVER)
        if (!overFired) {
            overFired = true
            resultLines = onDuelOver(d)
            if (d.youWon) SoundEngine.questComplete() else SoundEngine.thud()
        }
    }

    /** During DODGE_FLY a tap judges the dodge at that exact
     *  offset; during REACT after a landed throw it also flows
     *  on (impatience is a mechanic everywhere else in town). */
    private fun resolveDodge(tapOffsetMs: Long?) {
        val d = duel ?: return
        val dodged = d.resolveDodge(tapOffsetMs)
        if (dodged) {
            SoundEngine.select()
            youMood = "happy"
            reactText = "😤 Dodged it!"
        } else {
            SoundEngine.thud()
            youMood = "hurt"
            shakeUntilMs = nowMs() + 320
            reactText = "💥 Direct hit!"
        }
        go(Ui.REACT)
    }

    private fun barPos(): Float {
        val sweep = Showdown.BAR_MS
        val ph = uiElapsed() % (2 * sweep)
        return if (ph < sweep) ph.toFloat() / sweep
        else 1f - (ph - sweep).toFloat() / sweep
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_UP) return true
        val d = duel ?: return true
        when (ui) {
            Ui.BAR -> {
                releasePos = barPos()
                throwHit = d.resolveThrow(releasePos)
                SoundEngine.snowball()
                oppMood = "idle"
                go(Ui.THROW_FLY)
            }
            Ui.DODGE_FLY -> if (!dodgeResolved) {
                dodgeResolved = true
                resolveDodge(uiElapsed())
            }
            else -> {}
        }
        invalidate()
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val d = duel ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()
        c.save()
        if (now < shakeUntilMs) {
            c.translate((sin(now * 0.09) * 5.0).toFloat(), 0f)
        }
        // sky + the snowy grounds
        c.drawRect(0f, 0f, w, h * 0.66f, Paper.fillPaint("#a8c8e8"))
        c.drawRect(0f, h * 0.66f, w, h, Paper.fillPaint("#eef4fa"))
        c.drawRect(0f, h * 0.66f, w, h * 0.685f, Paper.fillPaint("#c3d5e8"))
        // two little forts, because this is a formal occasion
        for (fx in listOf(w * 0.10f, w * 0.90f)) {
            c.drawRect(fx - 26f, h * 0.80f, fx + 26f, h * 0.875f, Paper.fillPaint("#ffffff"))
            c.drawRect(fx - 26f, h * 0.80f, fx + 26f, h * 0.815f, Paper.fillPaint("#c3d5e8"))
        }
        val feetY = h * 0.875f
        val figH = h * 0.44f
        val youX = w * 0.26f
        val oppX = w * 0.74f
        KidArt.drawFigure(c, playerSpec, youX, feetY - figH / 2f, figH,
            youMood, "stand", FigureAnim(timeMs = now, seed = 11))
        KidArt.drawKid(c, d.opponent, oppX, feetY - figH / 2f, figH,
            oppMood, null, "stand", FigureAnim(timeMs = now, seed = 13))

        // score + round, top center
        centerText(c, "YOU ${d.youHits} — ${d.oppHits} ${d.opponent.uppercase()}",
            w / 2f, h * 0.075f, 19f, "#ffffff")
        if (ui != Ui.OVER) {
            centerText(c, "first to ${Showdown.HITS_TO_WIN} · round ${d.round}",
                w / 2f, h * 0.075f + 24f, 12.5f, "#dcecfc", bold = false)
        }

        // snowballs in flight
        val chestY = feetY - figH * 0.62f
        if (ui == Ui.THROW_FLY) {
            val t = (uiElapsed() / 430f).coerceIn(0f, 1f)
            val bx = youX + (oppX - youX) * t
            val by = chestY - sin(t * Math.PI).toFloat() * h * 0.13f
            c.drawCircle(bx, by, 9f, Paper.fillPaint("#ffffff"))
        }
        if (ui == Ui.DODGE_FLY) {
            val t = (uiElapsed() / Showdown.FLIGHT_MS.toFloat()).coerceIn(0f, 1f)
            val bx = oppX + (w * 0.44f - oppX) * t
            val by = chestY + (h * 0.80f - chestY) * t
            c.drawCircle(bx, by, 7f + t * 20f, Paper.fillPaint("#ffffff"))
        }

        // phase words
        when (ui) {
            Ui.INTRO -> centerText(c, "❄️ ${d.opponent} accepts the duel of the fort!",
                w / 2f, h * 0.30f, 18f, "#ffffff")
            Ui.BAR -> centerText(c, "YOUR THROW — tap to release!",
                w / 2f, h * 0.30f, 17f, Paper.YELLOW)
            Ui.WINDUP -> centerText(c,
                if (now < flashUntilMs && flashText.isNotEmpty()) flashText
                else "${d.opponent} is winding up…",
                w / 2f, h * 0.30f, 16f, "#ffffff")
            Ui.DODGE_FLY -> centerText(c, "INCOMING — TAP TO DODGE!",
                w / 2f, h * 0.30f, 19f, Paper.YELLOW)
            Ui.REACT -> centerText(c, reactText, w / 2f, h * 0.30f, 18f, "#ffffff")
            Ui.OVER -> {
                var yy = h * 0.24f
                for (ln in resultLines) {
                    centerText(c, ln, w / 2f, yy, 16.5f, "#ffffff")
                    yy += 24f
                }
            }
            else -> if (now < flashUntilMs && flashText.isNotEmpty()) {
                centerText(c, flashText, w / 2f, h * 0.30f, 18f, Paper.YELLOW)
            }
        }

        // the timing bar (your throw only)
        if (ui == Ui.BAR) {
            val barW = w * 0.78f
            val barX = (w - barW) / 2f
            val barY = h * 0.925f
            val barH = 20f
            fun band(from: Float, to: Float, hex: String) {
                c.drawRect(barX + barW * from, barY, barX + barW * to, barY + barH,
                    Paper.fillPaint(hex))
            }
            band(0f, 0.15f, "#9aa4ae"); band(0.15f, 0.30f, "#cfd8e0")
            band(0.30f, 0.44f, "#f2c230"); band(0.44f, 0.56f, Paper.RED)
            band(0.56f, 0.70f, "#f2c230"); band(0.70f, 0.85f, "#cfd8e0")
            band(0.85f, 1f, "#9aa4ae")
            val nx = barX + barW * barPos()
            c.drawRect(nx - 2.5f, barY - 6f, nx + 2.5f, barY + barH + 6f,
                Paper.fillPaint("#ffffff"))
            c.drawRect(nx - 2.5f, barY - 6f, nx + 2.5f, barY + barH + 6f,
                Paper.strokePaint("#1a1a1a", 1.5f))
        }
        c.restore()
    }
}

// ============================================================
// 🌮 Taco Catch
// ============================================================
class TacoCatchView(context: Context) : View(context) {

    var session: TacoCatchSession? = null
    var bestScore: Int = 0
    /** The clock ran out and the run settled; returns the result lines. */
    var onRunOver: (TacoCatchSession) -> List<String> = { emptyList() }

    private var startMs = 0L
    private var lastFrameMs = 0L
    private var running = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var trayX01 = 0.5f
    private val resolved = mutableSetOf<Int>()
    private data class FloatText(val txt: String, val x01: Float, val bornMs: Long, val good: Boolean)
    private val floats = mutableListOf<FloatText>()
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 16)
        }
    }

    fun begin() {
        startMs = SystemClock.uptimeMillis()
        lastFrameMs = 0L
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis() - startMs

    private fun step() {
        val s = session ?: return
        val now = nowMs()
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) { lastFrameMs = now; return }
        val trayLineY = h * 0.86f
        val groundY = h * 0.97f
        val fieldTop = h * 0.13f
        for (item in s.spawns) {
            if (item.id in resolved || now < item.atMs) continue
            val prevT = ((lastFrameMs - item.atMs).toFloat() / item.fallMs).coerceIn(0f, 1f)
            val curT = ((now - item.atMs).toFloat() / item.fallMs).coerceIn(0f, 1f)
            if (lastFrameMs >= item.atMs && prevT < 1f) {
                val prevY = fieldTop + prevT * (groundY - fieldTop)
                val curY = fieldTop + curT * (groundY - fieldTop)
                if (prevY < trayLineY && curY >= trayLineY &&
                    abs(item.x01 - trayX01) <= 0.115f) {
                    resolved.add(item.id)
                    val delta = s.onCatch(item.kind, now)
                    if (s.isHazard(item.kind)) {
                        SoundEngine.thud()
                        floats.add(FloatText("$delta", item.x01, now, false))
                    } else {
                        SoundEngine.tacoPop()
                        floats.add(FloatText("+$delta", item.x01, now, true))
                    }
                    continue
                }
                if (curY >= groundY) {
                    resolved.add(item.id)
                    s.onGround(item.kind)
                }
            }
        }
        floats.removeAll { now - it.bornMs > 750 }
        lastFrameMs = now
        if (now >= TacoCatchSession.DURATION_MS && !overFired) {
            overFired = true
            resultLines = onRunOver(s)
            SoundEngine.coinDing()
            running = false // the final frame still draws below
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val s = session ?: return true
        when (ev.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (!s.stunnedAt(nowMs()) && width > 0) {
                    trayX01 = (ev.x / width.toFloat()).coerceIn(0.08f, 0.92f)
                }
                invalidate()
                return true
            }
        }
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs().coerceAtMost(TacoCatchSession.DURATION_MS)

        // the joint: warm wall, a menu board, the counter line
        c.drawRect(0f, 0f, w, h, Paper.fillPaint("#f3b25e"))
        c.drawRect(0f, 0f, w, h * 0.115f, Paper.fillPaint(Paper.RED))
        centerText(c, "🌮 FREEMAN'S TACOS 🌮", w / 2f, h * 0.082f, 19f, "#ffffff")
        c.drawRect(0f, h * 0.90f, w, h, Paper.fillPaint("#94522c"))
        c.drawRect(0f, h * 0.90f, w, h * 0.915f, Paper.fillPaint("#7c4526"))

        // HUD: score left, best, the clock bar
        val hud = Paper.textPaint("#1a1a1a", 17f, bold = true, casual = true)
        hud.textAlign = Paint.Align.LEFT
        c.drawText("SCORE ${s.score}", w * 0.04f, h * 0.155f, hud)
        val hudR = Paper.textPaint("#5a3a1a", 12.5f, bold = true, casual = true)
        hudR.textAlign = Paint.Align.RIGHT
        c.drawText("🏆 BEST ${maxOf(bestScore, s.score)}", w * 0.96f, h * 0.155f, hudR)
        val remain = 1f - now.toFloat() / TacoCatchSession.DURATION_MS
        c.drawRect(w * 0.04f, h * 0.175f, w * 0.96f, h * 0.19f, Paper.fillPaint("#00000033"))
        c.drawRect(w * 0.04f, h * 0.175f, w * 0.04f + (w * 0.92f) * remain,
            h * 0.19f, Paper.fillPaint(if (remain < 0.18f) Paper.RED else "#7ab648"))

        // falling food
        val fieldTop = h * 0.13f
        val groundY = h * 0.97f
        for (item in s.spawns) {
            if (item.id in resolved || now < item.atMs) continue
            val t = ((now - item.atMs).toFloat() / item.fallMs).coerceIn(0f, 1f)
            if (t >= 1f) continue
            val y = fieldTop + t * (groundY - fieldTop)
            val glyph = when (item.kind) {
                "taco" -> "🌮"; "burrito" -> "🌯"; "berry" -> "🫐"; else -> "📺"
            }
            centerText(c, glyph, item.x01 * w, y, 30f, "#ffffff")
        }

        // the tray (stunned = gray, regretful)
        val trayW = w * 0.21f
        val trayX = trayX01 * w
        val trayY = h * 0.86f
        val stunned = s.stunnedAt(nowMs())
        c.drawRoundRect(RectF(trayX - trayW / 2f, trayY, trayX + trayW / 2f, trayY + 13f),
            6f, 6f, Paper.fillPaint(if (stunned) "#9aa4ae" else "#c8b088"))
        c.drawRoundRect(RectF(trayX - trayW / 2f, trayY, trayX + trayW / 2f, trayY + 5f),
            4f, 4f, Paper.fillPaint(if (stunned) "#7c8894" else Paper.RED))
        if (stunned) centerText(c, "💫", trayX, trayY - 12f, 20f, "#ffffff")
        if (!overFired) {
            centerText(c, "drag to slide the tray · tacos good · TVs & 'berries BAD",
                w / 2f, h * 0.985f, 12f, "#5a3a1a", bold = false)
        }

        // floating score pops
        for (f in floats) {
            val age = (nowMs() - f.bornMs).toFloat() / 750f
            if (age in 0f..1f) {
                centerText(c, f.txt, f.x01 * w, trayY - 26f - age * 30f, 17f,
                    if (f.good) "#2a7a2a" else Paper.RED)
            }
        }

        // the settled run, on a parchment card over the field
        if (overFired) {
            val card = RectF(w * 0.10f, h * 0.30f, w * 0.90f, h * 0.68f)
            c.drawRoundRect(card, 12f, 12f, Paper.fillPaint(Paper.PARCHMENT))
            c.drawRoundRect(card, 12f, 12f, Paper.strokePaint("#b8a888", 3f))
            var yy = card.top + 44f
            for (ln in resultLines) {
                centerText(c, ln, w / 2f, yy, 17.5f, "#1a1a1a")
                yy += 27f
            }
        }
    }
}

// ============================================================
// 🎯 v0.9.0c: DUNK TANK — the county fair's third contest.
// The showdown's bar and zones, thrown at a tank: release
// in the zones to price the throw (Fair.DunkSession owns
// the math against the sitter's own dodge), three throws,
// and the sitter goes in the water or doesn't. The view
// only presents the session; the bracket settles on
// closing night through Fair.
// ============================================================
class DunkTankView(context: Context) : View(context) {

    var session: Fair.DunkSession? = null
    /** The tank is done; returns the result lines. */
    var onDunkOver: (Fair.DunkSession) -> List<String> = { emptyList() }

    private enum class Ui { INTRO, BAR, FLY, OVER }
    private var ui: Ui = Ui.INTRO
    private var uiStartMs = 0L
    private var running = false
    private var lastHit = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var flashText = ""
    private var flashUntilMs = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 30)
        }
    }

    fun begin() {
        ui = Ui.INTRO
        uiStartMs = SystemClock.uptimeMillis()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis()
    private fun uiElapsed(): Long = nowMs() - uiStartMs
    private fun go(next: Ui) { ui = next; uiStartMs = nowMs() }

    private fun step() {
        val s = session ?: return
        when (ui) {
            Ui.INTRO -> if (uiElapsed() > 1000) go(Ui.BAR)
            Ui.FLY -> if (uiElapsed() > 430) {
                flashText = if (lastHit) "🎯 SPLASH! Down they go!"
                    else "💨 Clang — off the frame!"
                flashUntilMs = nowMs() + 750
                if (s.over) {
                    if (!overFired) {
                        overFired = true
                        resultLines = onDunkOver(s)
                    }
                    go(Ui.OVER)
                } else go(Ui.BAR)
            }
            else -> {}
        }
    }

    private fun barPos(): Float {
        val sweep = Showdown.BAR_MS
        val ph = uiElapsed() % (2 * sweep)
        return if (ph < sweep) ph.toFloat() / sweep
        else 1f - (ph - sweep).toFloat() / sweep
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_UP) return true
        val s = session ?: return true
        if (ui == Ui.BAR) {
            lastHit = s.throwBall(barPos())
            SoundEngine.snowball()
            go(Ui.FLY)
        }
        invalidate()
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()

        // the fairground: sky, the lawn, the midway behind
        c.drawRect(0f, 0f, w, h * 0.55f, Paper.fillPaint("#a8c8e8"))
        c.drawRect(0f, h * 0.55f, w, h, Paper.fillPaint("#7fae56"))
        c.drawRect(0f, h * 0.10f, w * 0.30f, h * 0.16f, Paper.fillPaint("#c23b3b"))
        c.drawRect(w * 0.38f, h * 0.10f, w * 0.58f, h * 0.16f, Paper.fillPaint("#2a6fd4"))
        c.drawRect(w * 0.66f, h * 0.10f, w * 0.86f, h * 0.16f, Paper.fillPaint("#e3a72c"))

        // the tank: glass, water, the seat, the sitter
        val tx = w * 0.60f
        val ty = h * 0.30f
        val tw = w * 0.30f
        val th = h * 0.42f
        val dunked = ui == Ui.FLY && lastHit || ui == Ui.OVER && s.hits > 0 && lastHit
        c.drawRect(tx, ty, tx + tw, ty + th, Paper.fillPaint("#9fc6d8"))
        c.drawRect(tx, ty + th * 0.45f, tx + tw, ty + th, Paper.fillPaint("#3f7fa8"))
        c.drawRect(tx - 3f, ty - 3f, tx + tw + 3f, ty + 3f, Paper.fillPaint("#4c4c50"))
        val seatY = if (dunked) ty + th * 0.52f else ty + th * 0.34f
        c.drawRect(tx + tw * 0.18f, seatY, tx + tw * 0.82f, seatY + 6f,
            Paper.fillPaint("#8a5a3a"))
        // the sitter, as the town draws anybody: a big head
        // on a small resolve
        val headR = w * 0.045f
        val headY = seatY - headR - 4f
        c.drawCircle(tx + tw / 2f, headY, headR, Paper.fillPaint("#f2c894"))
        c.drawCircle(tx + tw / 2f - headR * 0.35f, headY - headR * 0.1f,
            headR * 0.12f, Paper.fillPaint("#1a1a1a"))
        c.drawCircle(tx + tw / 2f + headR * 0.35f, headY - headR * 0.1f,
            headR * 0.12f, Paper.fillPaint("#1a1a1a"))
        // the target
        c.drawCircle(tx - w * 0.055f, ty + th * 0.30f, w * 0.030f,
            Paper.fillPaint("#c23b3b"))
        c.drawCircle(tx - w * 0.055f, ty + th * 0.30f, w * 0.030f,
            Paper.strokePaint("#f2ead8", 3f))
        c.drawCircle(tx - w * 0.055f, ty + th * 0.30f, w * 0.011f,
            Paper.fillPaint("#f2ead8"))
        centerText(c, s.sitter, tx + tw / 2f, ty - 14f, 16f, "#1a1a1a")

        // the flying ball
        if (ui == Ui.FLY) {
            val t = (uiElapsed().toFloat() / 430f).coerceIn(0f, 1f)
            val bx = w * 0.12f + (tx - w * 0.055f - w * 0.12f) * t
            val by = h * 0.62f - sin(t * Math.PI.toFloat()) * h * 0.18f +
                (ty + th * 0.30f - h * 0.62f) * t * t
            c.drawCircle(bx, by, w * 0.016f, Paper.fillPaint("#f2ead8"))
            c.drawCircle(bx, by, w * 0.016f, Paper.strokePaint("#8a7a5a", 2f))
        }

        // throws left / dunks so far
        centerText(c, "🎯 throws left: ${s.throwsLeft} · dunks: ${s.hits}",
            w / 2f, h * 0.80f, 17f, "#1a1a1a")

        // the bar (the showdown's own zones)
        if (ui == Ui.BAR || ui == Ui.INTRO) {
            val barX = w * 0.12f
            val barW = w * 0.76f
            val barY = h * 0.86f
            val barH = 20f
            fun zx(f: Float) = barX + barW * f
            c.drawRoundRect(RectF(barX, barY, barX + barW, barY + barH),
                8f, 8f, Paper.fillPaint("#d8cdb4"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_GOOD_LO), barY,
                zx(Feel.ZONE_GOOD_HI), barY + barH), 6f, 6f,
                Paper.fillPaint("#7fb069"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_PERFECT_LO), barY,
                zx(Feel.ZONE_PERFECT_HI), barY + barH), 6f, 6f,
                Paper.fillPaint(Paper.RED))
            val nx = zx(barPos())
            c.drawRect(nx - 2.5f, barY - 7f, nx + 2.5f, barY + barH + 7f,
                Paper.fillPaint("#1a1a1a"))
            centerText(c, if (ui == Ui.INTRO)
                "tap to throw — release in the zones, red is best"
                else "tap to throw!", w / 2f, barY - 16f, 14.5f, "#3a3a3a",
                bold = false)
        }

        if (now < flashUntilMs) {
            centerText(c, flashText, w / 2f, h * 0.22f, 21f, "#1a1a1a")
        }

        // the settled tank, on a parchment card
        if (ui == Ui.OVER) {
            val card = RectF(w * 0.08f, h * 0.30f, w * 0.92f, h * 0.66f)
            c.drawRoundRect(card, 12f, 12f, Paper.fillPaint(Paper.PARCHMENT))
            c.drawRoundRect(card, 12f, 12f, Paper.strokePaint("#b8a888", 3f))
            var yy = card.top + 44f
            for (ln in resultLines) {
                centerText(c, ln, w / 2f, yy, 16.5f, "#1a1a1a")
                yy += 27f
            }
        }
    }
}

// ============================================================
// 🛏️ v0.11.0c: PILLOW FIGHT — the sleepover's played beat.
// The dunk tank's honest machine in a living room: the
// showdown's bar and zones, swung at a guest standing
// among the sleeping bags (Sleepovers.PillowSession owns
// the math against the guest's own dodge), three swings,
// couch rights to the winner. The view only presents the
// session; the beat lands through Sleepovers.
// ============================================================
class PillowFightView(context: Context) : View(context) {

    var session: Sleepovers.PillowSession? = null
    /** The fight is done; returns the result lines. */
    var onPillowOver: (Sleepovers.PillowSession) -> List<String> =
        { emptyList() }

    private enum class Ui { INTRO, BAR, FLY, OVER }
    private var ui: Ui = Ui.INTRO
    private var uiStartMs = 0L
    private var running = false
    private var lastHit = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var flashText = ""
    private var flashUntilMs = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 30)
        }
    }

    fun begin() {
        ui = Ui.INTRO
        uiStartMs = SystemClock.uptimeMillis()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis()
    private fun uiElapsed(): Long = nowMs() - uiStartMs
    private fun go(next: Ui) { ui = next; uiStartMs = nowMs() }

    private fun step() {
        val s = session ?: return
        when (ui) {
            Ui.INTRO -> if (uiElapsed() > 1000) go(Ui.BAR)
            Ui.FLY -> if (uiElapsed() > 430) {
                flashText = if (lastHit) "🛏️ WHOMP! Right in the face!"
                    else "💨 Dodged — the pillow eats couch!"
                flashUntilMs = nowMs() + 750
                if (s.over) {
                    if (!overFired) {
                        overFired = true
                        resultLines = onPillowOver(s)
                    }
                    go(Ui.OVER)
                } else go(Ui.BAR)
            }
            else -> {}
        }
    }

    private fun barPos(): Float {
        val sweep = Showdown.BAR_MS
        val ph = uiElapsed() % (2 * sweep)
        return if (ph < sweep) ph.toFloat() / sweep
        else 1f - (ph - sweep).toFloat() / sweep
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_UP) return true
        val s = session ?: return true
        if (ui == Ui.BAR) {
            lastHit = s.swing(barPos().toDouble()) >= Feel.POWER_GOOD
            SoundEngine.snowball()
            go(Ui.FLY)
        }
        invalidate()
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()

        // the living room at night: wall, floor, a lamp's pool
        c.drawRect(0f, 0f, w, h * 0.58f, Paper.fillPaint("#4a4460"))
        c.drawRect(0f, h * 0.58f, w, h, Paper.fillPaint("#6b4a2f"))
        c.drawRect(0f, h * 0.10f, w * 0.16f, h * 0.34f, Paper.fillPaint("#2e2a40"))
        c.drawCircle(w * 0.86f, h * 0.18f, w * 0.055f, Paper.fillPaint("#e8c96a"))

        // the couch — the prize itself
        val cx = w * 0.56f
        val cy = h * 0.40f
        val cw = w * 0.36f
        val chh = h * 0.16f
        c.drawRoundRect(RectF(cx, cy, cx + cw, cy + chh), 10f, 10f,
            Paper.fillPaint("#8a4a3a"))
        c.drawRoundRect(RectF(cx, cy - chh * 0.45f, cx + cw, cy + 4f),
            10f, 10f, Paper.fillPaint("#9c5844"))

        // the sleeping bags on the floor — the night's dressing
        val bagColors = listOf("#3f6fb0", "#b03a3a", "#3f8a4f")
        for (i in 0 until 3) {
            val bx = w * (0.10f + i * 0.26f)
            val by = h * (0.64f + (i % 2) * 0.05f)
            c.drawRoundRect(RectF(bx, by, bx + w * 0.20f, by + h * 0.055f),
                12f, 12f, Paper.fillPaint(bagColors[i]))
        }

        // the guest, as the town draws anybody: a big head
        // on a small resolve, pillow raised
        val headR = w * 0.048f
        val gx = cx + cw / 2f
        val gy = cy - chh * 0.45f - headR - 2f
        val floored = ui == Ui.FLY && lastHit || ui == Ui.OVER && s.youWon
        val headY = if (floored) gy + headR * 0.9f else gy
        c.drawCircle(gx, headY, headR, Paper.fillPaint("#f2c894"))
        c.drawCircle(gx - headR * 0.35f, headY - headR * 0.1f,
            headR * 0.12f, Paper.fillPaint("#1a1a1a"))
        c.drawCircle(gx + headR * 0.35f, headY - headR * 0.1f,
            headR * 0.12f, Paper.fillPaint("#1a1a1a"))
        if (!floored) {
            // their pillow, held high and entirely unrepentant
            c.drawRoundRect(RectF(gx + headR * 0.9f, headY - headR * 1.5f,
                gx + headR * 2.1f, headY - headR * 0.4f), 8f, 8f,
                Paper.fillPaint("#f2ead8"))
        }
        centerText(c, s.guest, gx, cy - chh * 0.45f - headR * 2.6f,
            16f, "#f2ead8")

        // your flying pillow
        if (ui == Ui.FLY) {
            val t = (uiElapsed().toFloat() / 430f).coerceIn(0f, 1f)
            val bx = w * 0.10f + (gx - w * 0.10f) * t
            val by = h * 0.62f - sin(t * Math.PI.toFloat()) * h * 0.16f +
                (headY - h * 0.62f) * t * t
            c.drawRoundRect(RectF(bx - w * 0.028f, by - h * 0.020f,
                bx + w * 0.028f, by + h * 0.020f), 8f, 8f,
                Paper.fillPaint("#f2ead8"))
            if (lastHit) {
                // feathers, honorably
                c.drawCircle(gx - headR, headY - headR * 0.4f,
                    3.5f, Paper.fillPaint("#ffffff"))
                c.drawCircle(gx + headR * 0.6f, headY - headR * 0.9f,
                    3f, Paper.fillPaint("#ffffff"))
                c.drawCircle(gx + headR * 1.3f, headY,
                    2.5f, Paper.fillPaint("#ffffff"))
            }
        }

        // swings left / hits so far
        centerText(c, "🛏️ swings left: ${Sleepovers.PillowSession.SWINGS - s.swings}" +
            " · clean hits: ${s.hits}", w / 2f, h * 0.80f, 17f, "#f2ead8")

        // the bar (the showdown's own zones)
        if (ui == Ui.BAR || ui == Ui.INTRO) {
            val barX = w * 0.12f
            val barW = w * 0.76f
            val barY = h * 0.86f
            val barH = 20f
            fun zx(f: Float) = barX + barW * f
            c.drawRoundRect(RectF(barX, barY, barX + barW, barY + barH),
                8f, 8f, Paper.fillPaint("#d8cdb4"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_GOOD_LO), barY,
                zx(Feel.ZONE_GOOD_HI), barY + barH), 6f, 6f,
                Paper.fillPaint("#7fb069"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_PERFECT_LO), barY,
                zx(Feel.ZONE_PERFECT_HI), barY + barH), 6f, 6f,
                Paper.fillPaint(Paper.RED))
            val nx = zx(barPos())
            c.drawRect(nx - 2.5f, barY - 7f, nx + 2.5f, barY + barH + 7f,
                Paper.fillPaint("#1a1a1a"))
            centerText(c, if (ui == Ui.INTRO)
                "tap to swing — release in the zones, red is best"
                else "tap to swing!", w / 2f, barY - 16f, 14.5f, "#f2ead8",
                bold = false)
        }

        if (now < flashUntilMs) {
            centerText(c, flashText, w / 2f, h * 0.22f, 21f, "#f2ead8")
        }

        // the verdict, on a parchment card
        if (ui == Ui.OVER) {
            val card = RectF(w * 0.08f, h * 0.28f, w * 0.92f, h * 0.64f)
            c.drawRoundRect(card, 12f, 12f, Paper.fillPaint(Paper.PARCHMENT))
            c.drawRoundRect(card, 12f, 12f, Paper.strokePaint("#b8a888", 3f))
            var yy = card.top + 44f
            for (ln in resultLines) {
                centerText(c, ln, w / 2f, yy, 16.5f, "#1a1a1a")
                yy += 27f
            }
        }
    }
}

// ============================================================
// 💃 THE SLOW DANCE (v0.13.0b)
//
// The showdown bar in a gym at night: four phrases of a
// slow song, one tap per phrase to step, and the step
// holds if the zone you released in — steadied by your
// partner's nerve — carries you (Dance.DanceSession owns
// the math). With a date, two figures sway; stag, one
// figure and a spotlight. The view only presents the
// session; the beat lands through Dance.
// ============================================================
class SlowDanceView(context: Context) : View(context) {

    var session: Dance.DanceSession? = null
    /** The song is over; returns the result lines. */
    var onDanceOver: (Dance.DanceSession) -> List<String> =
        { emptyList() }

    private enum class Ui { INTRO, BAR, STEP, OVER }
    private var ui: Ui = Ui.INTRO
    private var uiStartMs = 0L
    private var running = false
    private var lastHeld = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var flashText = ""
    private var flashUntilMs = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 30)
        }
    }

    fun begin() {
        ui = Ui.INTRO
        uiStartMs = SystemClock.uptimeMillis()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis()
    private fun uiElapsed(): Long = nowMs() - uiStartMs
    private fun go(next: Ui) { ui = next; uiStartMs = nowMs() }

    private fun step() {
        val s = session ?: return
        when (ui) {
            Ui.INTRO -> if (uiElapsed() > 1000) go(Ui.BAR)
            Ui.STEP -> if (uiElapsed() > 430) {
                flashText = if (lastHeld) "🎵 Steady — the step holds!"
                    else "💫 A wobble — hold on, hold on…"
                flashUntilMs = nowMs() + 750
                if (s.over) {
                    if (!overFired) {
                        overFired = true
                        resultLines = onDanceOver(s)
                    }
                    go(Ui.OVER)
                } else go(Ui.BAR)
            }
            else -> {}
        }
    }

    private fun barPos(): Float {
        val sweep = Showdown.BAR_MS
        val ph = uiElapsed() % (2 * sweep)
        return if (ph < sweep) ph.toFloat() / sweep
        else 1f - (ph - sweep).toFloat() / sweep
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_UP) return true
        val s = session ?: return true
        if (ui == Ui.BAR) {
            lastHeld = s.step(barPos().toDouble()) >= Feel.POWER_GOOD
            SoundEngine.snowball()
            go(Ui.STEP)
        }
        invalidate()
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()

        // the gym at night: evening wall, court floor, and
        // the banner over it all (one banner, big letters —
        // dressing, not pattern)
        c.drawRect(0f, 0f, w, h * 0.58f, Paper.fillPaint("#3d3355"))
        c.drawRect(0f, h * 0.58f, w, h, Paper.fillPaint("#8a6a42"))
        c.drawRect(w * 0.14f, h * 0.07f, w * 0.86f, h * 0.20f,
            Paper.fillPaint("#a33b5c"))
        centerText(c, "💃 THE SWEETHEART DANCE", w / 2f,
            h * 0.155f, 19f, "#f2ead8")

        // the dancers, as the town draws anybody: big heads
        // on small resolves, swaying with the phrase count
        val headR = w * 0.052f
        val sway = (s.steps % 2) * headR * 0.35f -
            headR * 0.175f
        val baseY = h * 0.44f
        fun dancer(cx: Float, name: String) {
            c.drawCircle(cx + sway, baseY, headR,
                Paper.fillPaint("#f2c894"))
            c.drawCircle(cx + sway - headR * 0.35f,
                baseY - headR * 0.1f, headR * 0.12f,
                Paper.fillPaint("#1a1a1a"))
            c.drawCircle(cx + sway + headR * 0.35f,
                baseY - headR * 0.1f, headR * 0.12f,
                Paper.fillPaint("#1a1a1a"))
            centerText(c, name, cx + sway, baseY - headR * 1.7f,
                15f, "#f2ead8")
        }
        if (s.partner.isEmpty()) {
            // stag: one figure, and the spotlight's cone
            val path = android.graphics.Path().apply {
                moveTo(w * 0.5f, h * 0.22f)
                lineTo(w * 0.30f, h * 0.60f)
                lineTo(w * 0.70f, h * 0.60f)
                close()
            }
            c.drawPath(path, Paper.fillPaint("#e8c96a", 50))
            dancer(w * 0.5f, "you, stag")
        } else {
            dancer(w * 0.40f, "you")
            dancer(w * 0.60f, s.partner)
        }

        // the song's phrases: four notes, filled as held
        val pips = (0 until Dance.DanceSession.PHRASES).map { i ->
            when {
                i < s.held -> "🎵"
                i < s.steps -> "🎶"
                else -> "·"
            }
        }.joinToString("  ")
        centerText(c, pips, w / 2f, h * 0.68f, 20f, "#f2ead8")
        centerText(c, "phrase ${minOf(s.steps + 1,
            Dance.DanceSession.PHRASES)} of " +
            "${Dance.DanceSession.PHRASES} · held: ${s.held}",
            w / 2f, h * 0.745f, 15.5f, "#f2ead8")

        // the bar (the showdown's own zones)
        if (ui == Ui.BAR || ui == Ui.INTRO) {
            val barX = w * 0.12f
            val barW = w * 0.76f
            val barY = h * 0.86f
            val barH = 20f
            fun zx(f: Float) = barX + barW * f
            c.drawRoundRect(RectF(barX, barY, barX + barW,
                barY + barH), 8f, 8f, Paper.fillPaint("#d8cdb4"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_GOOD_LO), barY,
                zx(Feel.ZONE_GOOD_HI), barY + barH), 6f, 6f,
                Paper.fillPaint("#7fb069"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_PERFECT_LO), barY,
                zx(Feel.ZONE_PERFECT_HI), barY + barH), 6f, 6f,
                Paper.fillPaint(Paper.RED))
            val nx = zx(barPos())
            c.drawRect(nx - 2.5f, barY - 7f, nx + 2.5f,
                barY + barH + 7f, Paper.fillPaint("#1a1a1a"))
            centerText(c, if (ui == Ui.INTRO)
                "tap to step — release in the zones, red is steadiest"
                else "tap to step!", w / 2f, barY - 16f, 14.5f,
                "#f2ead8", bold = false)
        }

        if (now < flashUntilMs) {
            centerText(c, flashText, w / 2f, h * 0.30f, 21f,
                "#f2ead8")
        }

        // the verdict, on a parchment card
        if (ui == Ui.OVER) {
            val card = RectF(w * 0.08f, h * 0.28f, w * 0.92f,
                h * 0.64f)
            c.drawRoundRect(card, 12f, 12f,
                Paper.fillPaint(Paper.PARCHMENT))
            c.drawRoundRect(card, 12f, 12f,
                Paper.strokePaint("#b8a888", 3f))
            var yy = card.top + 44f
            for (ln in resultLines) {
                centerText(c, ln, w / 2f, yy, 16.5f, "#1a1a1a")
                yy += 27f
            }
        }
    }
}


// ============================================================
// 🎤 YOUR ACT (v0.13.0c)
//
// The showdown bar on the Bijou's stage: your act's own
// phrases, sweep and shake (Talent.TalentSession owns the
// math — each of the four acts sweeps, shakes and holds
// differently). One figure under the spotlight: you. The
// view only presents the session; the judging lands
// through Talent when the act closes.
// ============================================================
class TalentActView(context: Context) : View(context) {

    var session: Talent.TalentSession? = null
    /** The act is over; returns the result lines. */
    var onActOver: (Talent.TalentSession) -> List<String> =
        { emptyList() }

    private enum class Ui { INTRO, BAR, STEP, OVER }
    private var ui: Ui = Ui.INTRO
    private var uiStartMs = 0L
    private var running = false
    private var lastHeld = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var flashText = ""
    private var flashUntilMs = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 30)
        }
    }

    fun begin() {
        ui = Ui.INTRO
        uiStartMs = SystemClock.uptimeMillis()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis()
    private fun uiElapsed(): Long = nowMs() - uiStartMs
    private fun go(next: Ui) { ui = next; uiStartMs = nowMs() }

    private fun step() {
        val s = session ?: return
        when (ui) {
            Ui.INTRO -> if (uiElapsed() > 1000) go(Ui.BAR)
            Ui.STEP -> if (uiElapsed() > 430) {
                flashText = if (lastHeld) "🎤 It lands — " +
                    "the room is with you!"
                    else "💫 A wobble — the room stays kind…"
                flashUntilMs = nowMs() + 750
                if (s.over) {
                    if (!overFired) {
                        overFired = true
                        resultLines = onActOver(s)
                    }
                    go(Ui.OVER)
                } else go(Ui.BAR)
            }
            else -> {}
        }
    }

    private fun barPos(): Float {
        val s = session ?: return 0f
        val sweep = s.act.sweepMs
        val ph = uiElapsed() % (2 * sweep)
        return if (ph < sweep) ph.toFloat() / sweep
        else 1f - (ph - sweep).toFloat() / sweep
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_UP) return true
        val s = session ?: return true
        if (ui == Ui.BAR) {
            lastHeld = s.step(barPos().toDouble()) >=
                s.act.holdAt
            SoundEngine.snowball()
            go(Ui.STEP)
        }
        invalidate()
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()

        // the Bijou's stage: dark wall, solid curtain
        // panels at the wings and a valance over the top
        // (dressing in blocks — never a pattern), boards
        // underfoot
        c.drawRect(0f, 0f, w, h * 0.62f,
            Paper.fillPaint("#2e2438"))
        c.drawRect(0f, h * 0.62f, w, h,
            Paper.fillPaint("#7a5a38"))
        c.drawRect(0f, 0f, w * 0.13f, h * 0.24f,
            Paper.fillPaint("#7e2233"))
        c.drawRect(w * 0.87f, 0f, w, h * 0.24f,
            Paper.fillPaint("#7e2233"))
        c.drawRect(0f, 0f, w, h * 0.09f,
            Paper.fillPaint("#7e2233"))
        centerText(c, "🎤 TALENT NIGHT", w / 2f, h * 0.065f,
            19f, "#f2ead8")

        // the spotlight's cone and you under it, swaying
        // with the phrase count like anybody on a stage
        val path = android.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.10f)
            lineTo(w * 0.32f, h * 0.60f)
            lineTo(w * 0.68f, h * 0.60f)
            close()
        }
        c.drawPath(path, Paper.fillPaint("#e8c96a", 46))
        val headR = w * 0.052f
        val sway = (s.steps % 2) * headR * 0.35f -
            headR * 0.175f
        val baseY = h * 0.46f
        val cx = w * 0.5f
        c.drawCircle(cx + sway, baseY, headR,
            Paper.fillPaint("#f2c894"))
        c.drawCircle(cx + sway - headR * 0.35f,
            baseY - headR * 0.1f, headR * 0.12f,
            Paper.fillPaint("#1a1a1a"))
        c.drawCircle(cx + sway + headR * 0.35f,
            baseY - headR * 0.1f, headR * 0.12f,
            Paper.fillPaint("#1a1a1a"))
        c.drawRect(cx + sway - headR * 0.55f,
            baseY + headR * 0.9f, cx + sway + headR * 0.55f,
            baseY + headR * 2.1f, Paper.fillPaint("#405a78"))
        centerText(c, "you", cx + sway, baseY - headR * 1.7f,
            15f, "#f2ead8")
        centerText(c, s.act.blurb, w / 2f, h * 0.315f,
            13.5f, "#f2ead8", bold = false)

        // the act's phrases, filled as held
        val pips = (0 until s.act.phrases).map { i ->
            when {
                i < s.held -> "🎤"
                i < s.steps -> "🎶"
                else -> "·"
            }
        }.joinToString("  ")
        centerText(c, pips, w / 2f, h * 0.68f, 20f, "#f2ead8")
        centerText(c, "phrase ${minOf(s.steps + 1,
            s.act.phrases)} of ${s.act.phrases} · " +
            "held: ${s.held}", w / 2f, h * 0.745f, 15.5f,
            "#f2ead8")

        // the bar (the showdown's own zones, this act's
        // own sweep)
        if (ui == Ui.BAR || ui == Ui.INTRO) {
            val barX = w * 0.12f
            val barW = w * 0.76f
            val barY = h * 0.86f
            val barH = 20f
            fun zx(f: Float) = barX + barW * f
            c.drawRoundRect(RectF(barX, barY, barX + barW,
                barY + barH), 8f, 8f, Paper.fillPaint("#d8cdb4"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_GOOD_LO), barY,
                zx(Feel.ZONE_GOOD_HI), barY + barH), 6f, 6f,
                Paper.fillPaint("#7fb069"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_PERFECT_LO), barY,
                zx(Feel.ZONE_PERFECT_HI), barY + barH), 6f, 6f,
                Paper.fillPaint(Paper.RED))
            val nx = zx(barPos())
            c.drawRect(nx - 2.5f, barY - 7f, nx + 2.5f,
                barY + barH + 7f, Paper.fillPaint("#1a1a1a"))
            centerText(c, if (ui == Ui.INTRO)
                "tap to perform — release in the zones, " +
                    "red is steadiest"
                else "tap to perform!", w / 2f, barY - 16f,
                14.5f, "#f2ead8", bold = false)
        }

        if (now < flashUntilMs) {
            centerText(c, flashText, w / 2f, h * 0.28f, 21f,
                "#f2ead8")
        }

        // the verdict, on a parchment card
        if (ui == Ui.OVER) {
            val card = RectF(w * 0.08f, h * 0.28f, w * 0.92f,
                h * 0.64f)
            c.drawRoundRect(card, 12f, 12f,
                Paper.fillPaint(Paper.PARCHMENT))
            c.drawRoundRect(card, 12f, 12f,
                Paper.strokePaint("#b8a888", 3f))
            var yy = card.top + 44f
            for (ln in resultLines) {
                centerText(c, ln, w / 2f, yy, 16.5f, "#1a1a1a")
                yy += 27f
            }
        }
    }
}


// ============================================================
// 🍬 THE CANDY CATCH (v0.13.0d)
//
// The showdown bar at curb height: a parade float throws
// three times, and each throw is one release on the bar
// (Parade.CatchSession owns the math — the dance's bar,
// re-aimed at incoming snacks). Daytime sky, the street,
// the float at the curb: dressing, not pattern. The view
// only presents the session; the judging lands through
// Parade when the view closes it.
// ============================================================
class ParadeCatchView(context: Context) : View(context) {

    var session: Parade.CatchSession? = null
    /** The throws are done; returns the result lines. */
    var onCatchOver: (Parade.CatchSession) -> List<String> =
        { emptyList() }
    /** The throwing float's name, for the banner. */
    var floatName: String = "the float"

    private enum class Ui { INTRO, BAR, TOSS, OVER }
    private var ui: Ui = Ui.INTRO
    private var uiStartMs = 0L
    private var running = false
    private var lastCaught = false
    private var overFired = false
    private var resultLines: List<String> = emptyList()
    private var flashText = ""
    private var flashUntilMs = 0L
    /** Each throw's own result, in order — the pips tell
     *  the truth about which throws landed. */
    private val tossResults = mutableListOf<Boolean>()
    private val handler = Handler(Looper.getMainLooper())

    private val frame = object : Runnable {
        override fun run() {
            step()
            invalidate()
            if (running) handler.postDelayed(this, 30)
        }
    }

    fun begin() {
        ui = Ui.INTRO
        uiStartMs = SystemClock.uptimeMillis()
        running = true
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun nowMs(): Long = SystemClock.uptimeMillis()
    private fun uiElapsed(): Long = nowMs() - uiStartMs
    private fun go(next: Ui) { ui = next; uiStartMs = nowMs() }

    private fun step() {
        val s = session ?: return
        when (ui) {
            Ui.INTRO -> if (uiElapsed() > 1000) go(Ui.BAR)
            Ui.TOSS -> if (uiElapsed() > 430) {
                flashText = if (lastCaught) "🍬 Caught it!"
                    else "🍬 It bounces off the curb…"
                flashUntilMs = nowMs() + 750
                if (s.over) {
                    if (!overFired) {
                        overFired = true
                        resultLines = onCatchOver(s)
                    }
                    go(Ui.OVER)
                } else go(Ui.BAR)
            }
            else -> {}
        }
    }

    private fun barPos(): Float {
        val sweep = Showdown.BAR_MS
        val ph = uiElapsed() % (2 * sweep)
        return if (ph < sweep) ph.toFloat() / sweep
        else 1f - (ph - sweep).toFloat() / sweep
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_UP) return true
        val s = session ?: return true
        if (ui == Ui.BAR) {
            lastCaught = s.toss(barPos().toDouble())
            tossResults += lastCaught
            SoundEngine.snowball()
            go(Ui.TOSS)
        }
        invalidate()
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val s = session ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        val now = nowMs()

        // parade day: late-morning sky, the street below,
        // and the banner strung over it (one banner, big
        // letters — dressing, not pattern)
        c.drawRect(0f, 0f, w, h * 0.58f,
            Paper.fillPaint("#7fb2d9"))
        c.drawRect(0f, h * 0.58f, w, h,
            Paper.fillPaint("#6e6e6e"))
        c.drawRect(w * 0.14f, h * 0.07f, w * 0.86f, h * 0.20f,
            Paper.fillPaint("#a33b5c"))
        centerText(c, "🎺 $floatName", w / 2f, h * 0.155f,
            19f, "#f2ead8")

        // the float at the curb: a plain wagon bed, two
        // big smooth wheels, and the thrower's arm up
        val bedY = h * 0.46f
        c.drawRoundRect(RectF(w * 0.30f, bedY, w * 0.72f,
            bedY + h * 0.055f), 6f, 6f,
            Paper.fillPaint("#8a6a42"))
        c.drawCircle(w * 0.38f, bedY + h * 0.085f, w * 0.032f,
            Paper.fillPaint("#3a3a3a"))
        c.drawCircle(w * 0.64f, bedY + h * 0.085f, w * 0.032f,
            Paper.fillPaint("#3a3a3a"))
        c.drawCircle(w * 0.51f, bedY - h * 0.035f, w * 0.030f,
            Paper.fillPaint("#f2c894"))
        centerText(c, "hands up!", w * 0.51f, bedY - h * 0.10f,
            14.5f, "#1a1a1a", bold = false)

        // the throws so far: one pip per throw, filled as
        // THAT throw was caught
        val pips = (0 until Parade.THROWS_PER).map { i ->
            when {
                i < tossResults.size && tossResults[i] -> "🍬"
                i < tossResults.size -> "·"
                else -> "○"
            }
        }.joinToString("  ")
        centerText(c, pips, w / 2f, h * 0.68f, 20f, "#f2ead8")
        centerText(c, "throw ${minOf(s.throws + 1,
            Parade.THROWS_PER)} of ${Parade.THROWS_PER} · " +
            "caught: ${s.caught}", w / 2f, h * 0.745f, 15.5f,
            "#f2ead8")

        // the bar (the showdown's own zones)
        if (ui == Ui.BAR || ui == Ui.INTRO) {
            val barX = w * 0.12f
            val barW = w * 0.76f
            val barY = h * 0.86f
            val barH = 20f
            fun zx(f: Float) = barX + barW * f
            c.drawRoundRect(RectF(barX, barY, barX + barW,
                barY + barH), 8f, 8f,
                Paper.fillPaint("#d8cdb4"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_GOOD_LO), barY,
                zx(Feel.ZONE_GOOD_HI), barY + barH), 6f, 6f,
                Paper.fillPaint("#7fb069"))
            c.drawRoundRect(RectF(zx(Feel.ZONE_PERFECT_LO),
                barY, zx(Feel.ZONE_PERFECT_HI), barY + barH),
                6f, 6f, Paper.fillPaint(Paper.RED))
            val nx = zx(barPos())
            c.drawRect(nx - 2.5f, barY - 7f, nx + 2.5f,
                barY + barH + 7f, Paper.fillPaint("#1a1a1a"))
            centerText(c, if (ui == Ui.INTRO)
                "tap to catch — release in the zones, red is steadiest"
                else "tap to catch!", w / 2f, barY - 16f,
                14.5f, "#f2ead8", bold = false)
        }

        if (now < flashUntilMs) {
            centerText(c, flashText, w / 2f, h * 0.30f, 21f,
                "#f2ead8")
        }

        // the haul, on a parchment card
        if (ui == Ui.OVER) {
            val card = RectF(w * 0.08f, h * 0.28f, w * 0.92f,
                h * 0.64f)
            c.drawRoundRect(card, 12f, 12f,
                Paper.fillPaint(Paper.PARCHMENT))
            c.drawRoundRect(card, 12f, 12f,
                Paper.strokePaint("#b8a888", 3f))
            var yy = card.top + 44f
            for (ln in resultLines) {
                centerText(c, ln, w / 2f, yy, 16.5f, "#1a1a1a")
                yy += 27f
            }
        }
    }
}

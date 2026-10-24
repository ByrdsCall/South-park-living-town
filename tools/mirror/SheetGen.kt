package com.hunt3r.southparktown

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * SHEETGEN — the games-style reference sheets (v0.16.0c).
 *
 * Lucky (Oct 18, 2026): "Make new reference sheets using
 * the South Park games style." These sheets are drawn BY
 * THE GAME'S OWN RENDERER — every panel is a KidArt
 * render of the character's own SheetSpec (or a crop of
 * one, taken on FigureGeometry's derived anchors), so a
 * sheet and the figure on the street can never drift
 * apart: if they disagree, one of them is a bug, and the
 * sheet is the one that was just generated from the code.
 * Presentation follows the games' cleaner construction
 * read — flat paper ground, ruled panels, the assembled
 * figure large — with the panel vocabulary of Lucky's
 * original uploaded sheets (HEAD / TORSO / ARMS / STAND /
 * SIT / EYES OPEN / HURT EYES / SLEEP EYES / EYELIDS /
 * mouths / assembled), plus this build's PROFILE panel.
 *
 * The sheets are study/construction documents. They are
 * NEVER bundled into the APK.
 *
 * Usage: SheetGen <outDir> [name ...]
 *   No names: the full cast (KidArt.SHEETS) + the player
 *   avatar sheet + a contact sheet.
 */
object SheetGen {

    private const val SHEET_W = 1300
    private const val SHEET_H = 1040
    private val BG = Color(0xf7, 0xf3, 0xea)
    private val INK = Color(0x2b, 0x2b, 0x30)
    private val RULE = Color(0x8a, 0x84, 0x78)

    @JvmStatic
    fun main(args: Array<String>) {
        val outDir = File(args.getOrElse(0) { "sheets-games-style" })
        outDir.mkdirs()
        val home = File(System.getProperty("user.home"))
        Context.dataDir = File(home, ".sptown-mirror")
        val projectDir = File(System.getenv("SPT_PROJECT") ?: System.getProperty("user.dir"))
        AssetManager.baseDir = File(projectDir, "assets")
        val act = Activity()
        ensureLoreLoaded(act)

        val only = args.drop(1)
        val names = (if (only.isEmpty()) KidArt.SHEETS.keys.sorted()
            else only).filter { it in KidArt.SHEETS }
        var done = 0
        for (name in names) {
            val spec = KidArt.SHEETS.getValue(name)
            write(sheetFor(name, spec),
                File(outDir, slug(name) + "-games-style-sheet.png"))
            done++
        }
        // the player avatar sheet: the creator's default
        // appearance, plus the LIVE LOOKS strip (day /
        // sleep / hero × front / profile L / profile R) —
        // the v0.16.0c item-18 proof sheet.
        val appearance = validateAppearance(DEFAULT_APPEARANCE)
        val aSpec = avatarSpec(appearance)
        write(sheetFor("New Kid (You)", aSpec),
            File(outDir, "new-kid-games-style-sheet.png"))
        write(looksSheet(appearance),
            File(outDir, "new-kid-looks-sheet.png"))
        done++
        // contact sheet: every assembled front, named.
        write(contactSheet(names),
            File(outDir, "contact-sheet.png"))
        println("[sheetgen] $done sheets + contact sheet -> $outDir")
    }

    private fun slug(name: String): String =
        name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private fun write(img: BufferedImage, f: File) {
        ImageIO.write(img, "png", f)
        println("[sheetgen] ${f.name} (${f.length()} B)")
    }

    // ---------- figure renders (the game's own KidArt) ----------

    /** Render one figure state at figure height [hPx] on a
     *  transparent ground; returns the image and the unit
     *  transform (px per unit, center x, center y). */
    private data class Render(val img: BufferedImage, val spu: Float,
                              val cx: Float, val cy: Float,
                              val spec: SheetSpec)

    private fun render(spec: SheetSpec, hPx: Float,
                       draw: (Canvas, Float, Float, Float) -> Unit): Render {
        val d = FigureGeometry.derive(spec)
        val spu = hPx / d.plan.boxH
        val wPx = (130 * spu).toInt()
        val hAll = (hPx * 1.06f).toInt()
        val bmp = Bitmap.createBitmap(wPx, hAll, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = wPx / 2f
        val cy = hAll / 2f
        draw(c, cx, cy, hPx)
        return Render(bmp.img, spu, cx, cy, spec)
    }

    private fun renderFigure(spec: SheetSpec, mood: String = "idle",
                             pose: String = "stand",
                             anim: FigureAnim = FigureAnim(timeMs = 1000L),
                             hPx: Float = 920f): Render =
        render(spec, hPx) { c, cx, cy, h ->
            KidArt.drawFigure(c, spec, cx, cy, h, mood, pose, anim)
        }

    /** Crop a unit-space rect out of a render. */
    private fun crop(r: Render, l: Float, t: Float, rr: Float, b: Float): BufferedImage {
        val d = FigureGeometry.derive(r.spec)
        fun px(ux: Float) = (r.cx + (ux - 50f) * r.spu).toInt()
        fun py(uy: Float) = (r.cy + (uy - d.plan.boxH / 2f) * r.spu).toInt()
        val x0 = px(l).coerceIn(0, r.img.width - 1)
        val y0 = py(t).coerceIn(0, r.img.height - 1)
        val x1 = px(rr).coerceIn(x0 + 1, r.img.width)
        val y1 = py(b).coerceIn(y0 + 1, r.img.height)
        return r.img.getSubimage(x0, y0, x1 - x0, y1 - y0)
    }

    // ---------- sheet composition ----------

    private fun panel(g: Graphics2D, label: String, x: Int, y: Int,
                      w: Int, h: Int, img: BufferedImage?) {
        g.color = BG
        g.fillRect(x, y, w, h)
        g.color = RULE
        g.drawRect(x, y, w, h)
        g.color = INK
        g.font = Font("SansSerif", Font.BOLD, 21)
        g.drawString(label, x + 10, y + 27)
        if (img == null) return
        val availW = w - 16
        val availH = h - 44
        val scale = minOf(availW.toFloat() / img.width,
            availH.toFloat() / img.height)
        val dw = (img.width * scale).toInt()
        val dh = (img.height * scale).toInt()
        g.drawImage(img, x + (w - dw) / 2, y + 36 + (availH - dh) / 2,
            dw, dh, null)
    }

    private fun sheetFor(name: String, spec: SheetSpec): BufferedImage {
        val d = FigureGeometry.derive(spec)
        val front = renderFigure(spec)
        val sit = renderFigure(spec, pose = "sit")
        val profile = renderFigure(spec,
            anim = FigureAnim(walking = true, walkPhase = 0.9f, facing = 1))
        val hurt = renderFigure(spec, mood = "hurt")
        val sleep = renderFigure(spec, mood = "sleep")
        val soft = renderFigure(spec, mood = "soft")
        val happy = renderFigure(spec, mood = "happy")
        val head = crop(front, d.hx - d.hrx - 7, d.hy - d.hry - 14,
            d.hx + d.hrx + 7, d.hy + d.hry + 7)
        val eyes = crop(front, d.eyeL - 9, d.eyeT - 9,
            d.eyeR + 9, d.eyeB + 9)
        val eyesHurt = crop(hurt, d.eyeL - 9, d.eyeT - 9,
            d.eyeR + 9, d.eyeB + 9)
        val eyesSleep = crop(sleep, d.eyeL - 9, d.eyeT - 9,
            d.eyeR + 9, d.eyeB + 9)
        val eyesLids = crop(soft, d.eyeL - 9, d.eyeT - 9,
            d.eyeR + 9, d.eyeB + 9)
        val mouths = mouthStrip(spec, front, happy, hurt)
        val torso = crop(front, 50f - d.plan.torsoHalfTop - 11,
            d.plan.torsoTop - 5, 50f + d.plan.torsoHalfTop + 11,
            d.plan.torsoBottom + 7)
        val arms = crop(front, 50f - d.silhouetteW / 2f - 5,
            d.plan.shoulderY - 8, 50f + d.silhouetteW / 2f + 5,
            d.plan.hipY + 10)
        val stand = crop(front, 50f - d.plan.torsoHalfBot - 9,
            d.plan.torsoBottom + 2, 50f + d.plan.torsoHalfBot + 9,
            d.plan.ankleY + 9)

        val sheet = BufferedImage(SHEET_W, SHEET_H,
            BufferedImage.TYPE_INT_ARGB)
        val g = sheet.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = BG
        g.fillRect(0, 0, SHEET_W, SHEET_H)
        g.color = INK
        g.font = Font("SansSerif", Font.BOLD, 40)
        g.drawString(name.uppercase(), 24, 52)
        g.font = Font("SansSerif", Font.PLAIN, 19)
        g.drawString("games-style construction sheet — drawn by the " +
            "game's own renderer from this character's spec (v0.17.0a)",
            24, 80)
        g.color = RULE
        g.drawRect(2, 2, SHEET_W - 4, SHEET_H - 4)
        panel(g, "HEAD", 20, 96, 300, 300, head)
        panel(g, "EYES OPEN", 336, 96, 290, 142, eyes)
        panel(g, "HURT EYES", 336, 250, 290, 146, eyesHurt)
        panel(g, "SLEEP EYES", 642, 96, 290, 142, eyesSleep)
        panel(g, "EYELIDS", 642, 250, 290, 146, eyesLids)
        panel(g, "TORSO", 20, 412, 300, 280, torso)
        panel(g, "ARMS", 336, 412, 290, 280, arms)
        panel(g, "STAND (LEGS)", 642, 412, 290, 132, stand)
        panel(g, "MOUTHS: idle happy hurt", 642, 556, 290, 136, mouths)
        panel(g, "SIT", 20, 708, 440, 312, sit.img)
        panel(g, "PROFILE — walking", 476, 708, 456, 312, profile.img)
        panel(g, "ASSEMBLED", 948, 96, 332, 924, front.img)
        g.dispose()
        return sheet
    }

    /** Three mouth-zone crops in a row: idle, happy, hurt. */
    private fun mouthStrip(spec: SheetSpec, idle: Render,
                           happy: Render, hurt: Render): BufferedImage {
        val d = FigureGeometry.derive(spec)
        fun mc(r: Render) = crop(r, d.hx - d.hrx * 0.58f,
            d.mouthY - 11, d.hx + d.hrx * 0.58f, d.mouthY + 13)
        val parts = listOf(mc(idle), mc(happy), mc(hurt))
        val hgt = 120
        val wid = parts.sumOf { (it.width * hgt / it.height) } + 20
        val strip = BufferedImage(wid, hgt, BufferedImage.TYPE_INT_ARGB)
        val g = strip.createGraphics()
        var x = 0
        for (p in parts) {
            val w = p.width * hgt / p.height
            g.drawImage(p, x, 0, w, hgt, null)
            x += w + 10
        }
        g.dispose()
        return strip
    }

    /** The player's three live looks × three facings, drawn
     *  with the very calls GameView's street pass makes —
     *  the item-18 proof: sleep and hero walk in true
     *  authored profiles, both directions. */
    private fun looksSheet(appearance: Map<String, String>): BufferedImage {
        val spec = avatarSpec(appearance)
        val sleepCode = Sleepwear.playerCode(appearance)
        val heroState = GameState()
        val heroCode = Heroes.playerCostumeCodeFor(heroState, appearance)
        val walkL = FigureAnim(walking = true, walkPhase = 0.9f, facing = -1)
        val walkR = FigureAnim(walking = true, walkPhase = 0.9f, facing = 1)
        // timeMs clear of the blink instant (t=0, seed=0 blinks):
        // a construction sheet shows the eyes it names.
        val still = FigureAnim(timeMs = 1000L)
        fun cell(draw: (Canvas, Float, Float, Float, FigureAnim) -> Unit,
                 anim: FigureAnim): BufferedImage =
            render(spec, 560f) { c, cx, cy, h -> draw(c, cx, cy, h, anim) }.img
        val rows: List<Pair<String, List<BufferedImage>>> = listOf(
            "DAY" to listOf(
                cell({ c, x, y, h, a -> KidArt.drawPlayerAvatar(c, appearance, x, y, h, a) }, still),
                cell({ c, x, y, h, a -> KidArt.drawPlayerAvatar(c, appearance, x, y, h, a) }, walkL),
                cell({ c, x, y, h, a -> KidArt.drawPlayerAvatar(c, appearance, x, y, h, a) }, walkR)),
            "SLEEP" to listOf(
                cell({ c, x, y, h, a -> KidArt.drawSleepFigure(c, spec, sleepCode, x, y, h, "idle", "stand", a) }, still),
                cell({ c, x, y, h, a -> KidArt.drawSleepFigure(c, spec, sleepCode, x, y, h, "idle", "stand", a) }, walkL),
                cell({ c, x, y, h, a -> KidArt.drawSleepFigure(c, spec, sleepCode, x, y, h, "idle", "stand", a) }, walkR)),
            "HERO" to listOf(
                cell({ c, x, y, h, a -> KidArt.drawHeroFigure(c, spec, heroCode, x, y, h, "idle", "stand", a) }, still),
                cell({ c, x, y, h, a -> KidArt.drawHeroFigure(c, spec, heroCode, x, y, h, "idle", "stand", a) }, walkL),
                cell({ c, x, y, h, a -> KidArt.drawHeroFigure(c, spec, heroCode, x, y, h, "idle", "stand", a) }, walkR))
        )
        val sheet = BufferedImage(1140, 1900, BufferedImage.TYPE_INT_ARGB)
        val g = sheet.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = BG
        g.fillRect(0, 0, 1140, 1900)
        g.color = INK
        g.font = Font("SansSerif", Font.BOLD, 38)
        g.drawString("NEW KID — LIVE LOOKS × FACINGS", 24, 52)
        g.font = Font("SansSerif", Font.PLAIN, 19)
        g.drawString("front / profile left / profile right, walking — " +
            "the street pass's own calls (v0.17.0a)", 24, 82)
        val labels = listOf("FRONT", "PROFILE L", "PROFILE R")
        for ((ri, row) in rows.withIndex()) {
            val y = 110 + ri * 600
            g.color = INK
            g.font = Font("SansSerif", Font.BOLD, 26)
            g.drawString(row.first, 24, y + 30)
            for ((ci, img) in row.second.withIndex()) {
                panel(g, labels[ci], 24 + ci * 372, y + 44, 356, 540, img)
            }
        }
        g.dispose()
        return sheet
    }

    private fun contactSheet(names: List<String>): BufferedImage {
        val cols = 10
        val cellW = 196
        val cellH = 232
        val rows = (names.size + cols - 1) / cols
        val sheet = BufferedImage(cols * cellW, rows * cellH,
            BufferedImage.TYPE_INT_ARGB)
        val g = sheet.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = BG
        g.fillRect(0, 0, sheet.width, sheet.height)
        for ((i, name) in names.withIndex()) {
            val spec = KidArt.SHEETS.getValue(name)
            val r = renderFigure(spec, hPx = 380f)
            val cx = (i % cols) * cellW
            val cy = (i / cols) * cellH
            val scale = minOf((cellW - 12).toFloat() / r.img.width,
                (cellH - 44).toFloat() / r.img.height)
            val dw = (r.img.width * scale).toInt()
            val dh = (r.img.height * scale).toInt()
            g.drawImage(r.img, cx + (cellW - dw) / 2, cy + 4, dw, dh, null)
            g.color = INK
            g.font = Font("SansSerif", Font.BOLD, 15)
            val tw = g.fontMetrics.stringWidth(name)
            g.drawString(name, cx + (cellW - tw) / 2, cy + cellH - 12)
        }
        g.dispose()
        return sheet
    }
}

package com.hunt3r.southparktown

import android.app.Activity
import android.graphics.Bitmap
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import javax.imageio.ImageIO

/**
 * THE GOLDEN HARNESS — v0.18.0c (Phase 1 — Sight, D22).
 *
 * The mirror could shoot captures for months and nothing ever
 * diffed them, so pixel regression was invisible: Phase 0's
 * verification found two runs on an IDENTICAL tree differing
 * on 4.6–12.0% of pixels. This harness renders the whole
 * visible surface of the game — all 27 exteriors, all 27
 * interiors, all 101 reference sheets — under a fully pinned
 * staging, hashes every render (whole frame + a 4×4 region
 * grid), and compares against the stored baseline in
 * tools/golden/hashes.tsv. A gate run fails, naming the
 * target and the regions that drifted, when any target's
 * drifted-region count exceeds its allowance (stored per
 * target; the baseline ships with allowance 0 everywhere —
 * an allowance is a documented confession, never a default).
 *
 * THE PINS (each one kills a named nondeterminism):
 *  - Engine seed: Capture stages through Engine(GOLDEN_SEED).
 *    The shipped default seed is the wall clock, so an
 *    unpinned staging populates a different town every run.
 *  - Render clock: RenderClock.fixedMs = GOLDEN_CLOCK_MS.
 *    The view layer's animation phases (gait, wander, boil,
 *    shimmer, sway, flicker) read RenderClock.nowMs(); in the
 *    shipped game the seam is unset and reads the wall clock.
 *    The frozen value lands the boil on phase 1 — a boiled
 *    frame, the way the game actually ships.
 *  - Frame tick: Capture's golden mode detaches the view's
 *    ticker and zeroes tick. The shim Handler runs the ticker
 *    on the Swing EDT; left alive it advances a
 *    scheduling-dependent number of times during a capture.
 *  - Clock & calendar: a fresh Engine state is day 1,
 *    2026-10-01, stepped by the engine itself to the target's
 *    pinned timeMin. Weather is pinned sunny, fiction off,
 *    window 720×1280, camera snapped to the target's pinned
 *    pan fraction on every draw.
 *  - Prefs: capture.sh runs golden mode against a FRESH
 *    mirror data dir, so appearance and settings are the
 *    game's own defaults (boil on, as shipped).
 *
 * Usage (via tools/mirror/capture.sh):
 *   capture.sh --golden update   re-render and rewrite the
 *                                baseline (allowances are
 *                                preserved across an update)
 *   capture.sh --golden check    re-render and compare;
 *                                exit 1 on any failure
 * Renders land in <work>/renders and <work>/sheets so a
 * failure can be eyeballed. Hashes are of decoded ARGB
 * pixels, so they are JVM/machine-local — a golden baseline
 * belongs to the build box that produced it, like every
 * golden harness.
 */
object Golden {

    const val GOLDEN_SEED: Long = 20261024L
    const val GOLDEN_CLOCK_MS: Long = 1700000000000L
    const val GOLDEN_W = 720
    const val GOLDEN_H = 1280
    const val REGION_COLS = 4
    const val REGION_ROWS = 4
    const val WEATHER = "sunny"

    private class Img(val w: Int, val h: Int, val px: IntArray)

    private class Row(
        val name: String, val kind: String, val w: Int, val h: Int,
        var allowed: Int, val whole: String, val regions: List<String>
    )

    /** Pinned staging time per location (minutes after
     *  midnight): the bus at the stop before school, class in
     *  session at the Elementary, lunch hour everywhere else. */
    private fun timeFor(loc: String): Int = when (loc) {
        "BusStop" -> 7 * 60 + 50
        "Elementary" -> 10 * 60
        else -> 12 * 60 + 15
    }

    /** Pinned camera: the middle of the world. Interiors are
     *  one screen wide and clamp to the room. */
    private fun panFor(@Suppress("UNUSED_PARAMETER") loc: String): Float = 0.5f

    fun run(args: List<String>, dataDir: File, projectDir: File) {
        val mode = args.getOrElse(0) { "check" }
        var work = File(System.getProperty("java.io.tmpdir"), "spt-golden")
        var i = 1
        while (i < args.size) {
            if (args[i] == "--work") work = File(args[++i])
            i++
        }
        val rendersDir = File(work, "renders").apply { mkdirs() }
        val sheetsDir = File(work, "sheets").apply { mkdirs() }

        MirrorMain.act = Activity()
        ensureLoreLoaded(MirrorMain.act)
        val prefs = Prefs(FileSaveStore(File(dataDir, "prefs")))
        val appearance = prefs.getAppearance()

        RenderClock.fixedMs = GOLDEN_CLOCK_MS
        Capture.goldenMode = true
        Capture.goldenSeed = GOLDEN_SEED

        val targets = LinkedHashMap<String, Img>()
        val errors = mutableListOf<String>()
        Capture.bitmapSink = { spec, bmp ->
            targets["scene:$spec"] = snapshot(bmp)
            ImageIO.write(bmp.img, "png",
                File(rendersDir, spec.replace(":", "_") + ".png"))
        }
        for (loc in LOCATIONS.keys) {
            for (inside in listOf(false, true)) {
                val spec = if (inside) "$loc:in" else loc
                try {
                    Capture.render(spec, rendersDir, GOLDEN_W, GOLDEN_H,
                        timeFor(loc), WEATHER, "golden", 4,
                        appearance, panFor(loc), emptyList())
                } catch (e: Exception) {
                    errors.add("scene:$spec: $e")
                    println("[golden] FAILED scene:$spec: $e")
                }
            }
        }
        Capture.bitmapSink = null

        SheetGen.main(arrayOf(sheetsDir.path))
        val sheetFiles = (sheetsDir.listFiles { _, n -> n.endsWith(".png") }
            ?: emptyArray()).sortedBy { it.name }
        for (f in sheetFiles) {
            val bi = ImageIO.read(f)
            targets["sheet:" + f.name.removeSuffix(".png")] = snapshot(bi)
        }
        RenderClock.fixedMs = null
        Capture.goldenMode = false

        if (errors.isNotEmpty()) {
            println("[golden] ${errors.size} target(s) failed to render:")
            errors.forEach { println("[golden]   $it") }
            kotlin.system.exitProcess(2)
        }
        println("[golden] rendered ${targets.size} targets")

        val rows = targets.map { (name, img) -> rowOf(name, img) }
        val tsvFile = File(projectDir, "tools/golden/hashes.tsv")
        when (mode) {
            "update" -> {
                val prior = if (tsvFile.exists()) {
                    readTsv(tsvFile).associate { it.name to it.allowed }
                } else {
                    emptyMap()
                }
                rows.forEach { it.allowed = prior[it.name] ?: 0 }
                writeTsv(tsvFile, rows)
                println("[golden] baseline written: ${rows.size} targets -> $tsvFile")
            }
            else -> {
                if (!tsvFile.exists()) {
                    println("[golden] NO BASELINE at $tsvFile — run update first")
                    kotlin.system.exitProcess(1)
                }
                val baseline = readTsv(tsvFile)
                var failures = 0
                val byName = rows.associateBy { it.name }
                for (base in baseline) {
                    val got = byName[base.name]
                    if (got == null) {
                        println("[golden] FAIL ${base.name}: not rendered")
                        failures++
                        continue
                    }
                    if (got.w != base.w || got.h != base.h) {
                        println("[golden] FAIL ${base.name}: size " +
                            "${got.w}x${got.h} != baseline ${base.w}x${base.h}")
                        failures++
                        continue
                    }
                    val drifted = base.regions.indices.filter {
                        base.regions[it] != got.regions[it]
                    }
                    if (drifted.size > base.allowed) {
                        println("[golden] FAIL ${base.name}: " +
                            "${drifted.size} region(s) drifted " +
                            "(allowed ${base.allowed}): " +
                            drifted.joinToString(" ") { "r$it" })
                        failures++
                    } else if (drifted.isNotEmpty()) {
                        println("[golden] ok   ${base.name} " +
                            "(${drifted.size} drifted region(s) within " +
                            "allowance ${base.allowed})")
                    } else {
                        println("[golden] ok   ${base.name}")
                    }
                }
                val extra = byName.keys - baseline.map { it.name }.toSet()
                for (name in extra) {
                    println("[golden] FAIL $name: rendered but not in baseline")
                    failures++
                }
                println("[golden] check complete: ${baseline.size} targets, " +
                    "$failures failure(s)")
                kotlin.system.exitProcess(if (failures > 0) 1 else 0)
            }
        }
    }

    private fun snapshot(bmp: Bitmap): Img =
        snapshot(bmp.img)

    private fun snapshot(bi: BufferedImage): Img {
        val px = bi.getRGB(0, 0, bi.width, bi.height, null, 0, bi.width)
        return Img(bi.width, bi.height, px)
    }

    private fun digestOf(px: IntArray, x0: Int, y0: Int,
                         x1: Int, y1: Int, w: Int): String {
        val md = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray((x1 - x0) * 4)
        for (y in y0 until y1) {
            var o = 0
            for (x in x0 until x1) {
                val v = px[y * w + x]
                buf[o++] = (v ushr 24).toByte()
                buf[o++] = (v ushr 16).toByte()
                buf[o++] = (v ushr 8).toByte()
                buf[o++] = v.toByte()
            }
            md.update(buf)
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun rowOf(name: String, img: Img): Row {
        val regions = mutableListOf<String>()
        for (r in 0 until REGION_ROWS) {
            val y0 = r * img.h / REGION_ROWS
            val y1 = (r + 1) * img.h / REGION_ROWS
            for (c in 0 until REGION_COLS) {
                val x0 = c * img.w / REGION_COLS
                val x1 = (c + 1) * img.w / REGION_COLS
                regions.add(digestOf(img.px, x0, y0, x1, y1, img.w))
            }
        }
        val whole = digestOf(img.px, 0, 0, img.w, img.h, img.w)
        val kind = name.substringBefore(':')
        return Row(name, kind, img.w, img.h, 0, whole, regions)
    }

    private fun writeTsv(f: File, rows: List<Row>) {
        f.parentFile.mkdirs()
        val sb = StringBuilder()
        sb.append("# Golden-image baseline — South Park Living Town\n")
        sb.append("# Produced by tools/mirror/capture.sh --golden update " +
            "(v0.18.0c). One row per target: name, kind, width, height,\n")
        sb.append("# allowed drifted regions, whole-frame SHA-256, then " +
            "the 4x4 region SHA-256 grid (row-major).\n")
        sb.append("# Hashes are of decoded ARGB pixels from this build " +
            "box's JVM rasterizer; regenerate on a new box.\n")
        for (r in rows) {
            sb.append(r.name).append('\t').append(r.kind).append('\t')
                .append(r.w).append('\t').append(r.h).append('\t')
                .append(r.allowed).append('\t').append(r.whole)
            for (h in r.regions) sb.append('\t').append(h)
            sb.append('\n')
        }
        f.writeText(sb.toString())
    }

    private fun readTsv(f: File): List<Row> =
        f.readLines().filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { line ->
                val p = line.split("\t")
                Row(p[0], p[1], p[2].toInt(), p[3].toInt(), p[4].toInt(),
                    p[5], p.drop(6))
            }
}

package com.hunt3r.southparktown

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

// ============================================================
// SoundGen — v0.2.0b: ALL of the game's sound, synthesized in
// code at runtime. Pure Kotlin (no android.* imports) so the
// JVM smoke suite can pin every buffer: deterministic renders,
// bounded sizes, the original melody's note data, the ambience
// selection table, and the settings/gain math.
//
// HARD RULE (the audio twin of the art rule): zero audio files
// ship in the APK, nothing is sampled, nothing is ripped. Every
// buffer below is math: additive harmonic plucks, shaped noise,
// sine-partial bells. The town tune is an ORIGINAL composition
// written for this game (see TownMusic) — a plain major-key
// pentatonic folk line over a I–IV–I–V–vi–IV–I–V progression,
// composed note by note in the table below. It does not quote,
// approximate, or arrange any existing melody.
// ============================================================

/** Tiny deterministic LCG — same seed, same noise, everywhere. */
private class Lcg(seed: Long) {
    private var s: Long = seed * 6364136223846793005L + 1442695040888963407L
    fun nextFloat(): Float {
        s = s * 6364136223846793005L + 1442695040888963407L
        val v = ((s ushr 40) and 0xFFFFFF).toFloat() / 16777216f
        return v * 2f - 1f
    }
}

object SoundGen {
    const val SAMPLE_RATE = 22050
    const val MAX_SFX_SEC = 2.5
    const val AMB_LOOP_SEC = 8
    const val AMB_LOOP_SAMPLES = SAMPLE_RATE * AMB_LOOP_SEC

    // ---------------- SFX registry ----------------
    // Every one-shot the game can play. Renders are pure:
    // renderSfx(id, seed) twice = identical samples.
    val SFX_IDS: List<String> = listOf(
        "ui_tap", "ui_select", "ui_back",
        "step_floor",
        "door_open", "door_close",
        "snow_throw", "snow_splat",
        "gift_chime", "found_sparkle",
        "quest_accept", "quest_complete",
        "error_thud",
        "sting_title", "sting_act", "sting_credits", "cue_stage",
        "camera_shutter",
        "fish_splash", "coin_ding", "taco_pop"
    )
    const val SNOW_STEP_VARIANTS = 4

    fun renderSfx(id: String, seed: Long = 0L): ShortArray = limitPeak(when (id) {
        "ui_tap" -> uiTap()
        "ui_select" -> uiSelect()
        "ui_back" -> uiBack()
        "step_floor" -> stepFloor(seed)
        "door_open" -> door(open = true)
        "door_close" -> door(open = false)
        "snow_throw" -> snowThrow()
        "snow_splat" -> snowSplat()
        "gift_chime" -> giftChime()
        "found_sparkle" -> foundSparkle()
        "quest_accept" -> questAccept()
        "quest_complete" -> questComplete()
        "error_thud" -> errorThud()
        "sting_title" -> stingTitle()
        "sting_act" -> stingAct()
        "sting_credits" -> stingCredits()
        "cue_stage" -> cueStage()
        "camera_shutter" -> cameraShutter()
        "fish_splash" -> fishSplash()
        "coin_ding" -> coinDing()
        "taco_pop" -> tacoPop()
        else -> ShortArray(0)
    }, PeakBudget.SFX_CEILING)

    fun renderSnowStep(seed: Long): ShortArray =
        limitPeak(snowStep(seed), PeakBudget.SFX_CEILING)

    // ---------------- voice blips ----------------
    enum class BlipFam(val baseHz: Double, val durSec: Double) {
        PLAYER(285.0, 0.09),
        KID(262.0, 0.09),
        KID_HIGH(338.0, 0.085),
        ADULT_M(138.0, 0.12),
        ADULT_DEEP(105.0, 0.13),
        ADULT_F(212.0, 0.11),
        MUFFLED(175.0, 0.11)
    }

    val BLIP_FAMILIES: Map<String, BlipFam> = mapOf(
        // the parka swallows everything — Kenny is his own family
        "Kenny" to BlipFam.MUFFLED,
        // kids, brighter
        "Wendy" to BlipFam.KID_HIGH, "Bebe" to BlipFam.KID_HIGH,
        "Henrietta" to BlipFam.KID_HIGH,
        // wave 2 (v0.3.0b): the new girls join the bright family
        "Heidi" to BlipFam.KID_HIGH, "Nichole" to BlipFam.KID_HIGH,
        "Red" to BlipFam.KID_HIGH,
        // kids
        "Stan" to BlipFam.KID, "Kyle" to BlipFam.KID, "Cartman" to BlipFam.KID,
        "Butters" to BlipFam.KID, "Craig" to BlipFam.KID, "Tweek" to BlipFam.KID,
        "Clyde" to BlipFam.KID, "Jimmy" to BlipFam.KID, "Timmy" to BlipFam.KID,
        "Tolkien" to BlipFam.KID, "Michael" to BlipFam.KID, "Pete" to BlipFam.KID,
        // wave 2: the new boys
        "Kevin" to BlipFam.KID, "Francis" to BlipFam.KID, "DogPoo" to BlipFam.KID,
        // wave 3 (v0.7.0a): the littlest sister and the circle's antenna
        // join the bright family; the new boys ride the standard kid voice
        "Karen" to BlipFam.KID_HIGH, "Annie" to BlipFam.KID_HIGH,
        "Scott" to BlipFam.KID, "Firkle" to BlipFam.KID, "Bradley" to BlipFam.KID,
        // adults, female
        "Sharon" to BlipFam.ADULT_F, "Sheila" to BlipFam.ADULT_F,
        "Liane" to BlipFam.ADULT_F, "Carol" to BlipFam.ADULT_F,
        "Linda" to BlipFam.ADULT_F,
        // wave 2: the Mayor, the bus, and the coffee shop's steady hand
        "McDaniels" to BlipFam.ADULT_F, "Crabtree" to BlipFam.ADULT_F,
        "Mrs. Tweek" to BlipFam.ADULT_F,
        // wave 3: the principal
        "Victoria" to BlipFam.ADULT_F,
        // adults, male — Chef sings from the basement
        "Chef" to BlipFam.ADULT_DEEP,
        "Randy" to BlipFam.ADULT_M, "Gerald" to BlipFam.ADULT_M,
        "Stuart" to BlipFam.ADULT_M, "Stephen" to BlipFam.ADULT_M,
        "Garrison" to BlipFam.ADULT_M, "Mackey" to BlipFam.ADULT_M,
        "Jesus" to BlipFam.ADULT_M, "Jimbo" to BlipFam.ADULT_M,
        "Father Maxi" to BlipFam.ADULT_M, "Towelie" to BlipFam.ADULT_M,
        // wave 2: the law, the coffee philosopher, and the voice box
        "Barbrady" to BlipFam.ADULT_M, "Mr. Tweek" to BlipFam.ADULT_M,
        "Ned" to BlipFam.ADULT_M,
        // wave 3: the bar, the couch, the fence, and the library wing
        "Skeeter" to BlipFam.ADULT_M, "Marvin" to BlipFam.ADULT_M,
        "Mr. Tucker" to BlipFam.ADULT_M, "Steve" to BlipFam.ADULT_M,
        // wave 4 (v0.8.0a): the toddler and the big sister ride
        // the bright family; the eldest McCormick the standard
        // kid voice; the new grown-ups their grown families
        "Ike" to BlipFam.KID_HIGH, "Shelley" to BlipFam.KID_HIGH,
        "Kevin McCormick" to BlipFam.KID,
        "Laura" to BlipFam.ADULT_F,
        "Mephesto" to BlipFam.ADULT_M, "Yates" to BlipFam.ADULT_M,
        "Adler" to BlipFam.ADULT_M, "Lu Kim" to BlipFam.ADULT_M,
        // wave 5 (v0.9.0a): the nemesis rides the standard kid
        // voice (cold comes from the words); the new girl the
        // bright family; the last parents their grown families —
        // Big Gay Al gets the deep shelf, because super is a
        // full-chested register
        "Nathan" to BlipFam.KID, "Sophie" to BlipFam.KID_HIGH,
        "Sarah" to BlipFam.ADULT_F, "Alexis" to BlipFam.ADULT_F,
        "Roger" to BlipFam.ADULT_M, "Ryan" to BlipFam.ADULT_M,
        "Mr. Testaburger" to BlipFam.ADULT_M,
        "Big Gay Al" to BlipFam.ADULT_DEEP,
        // wave 6 (v0.11.0d): the audit's voices — the little
        // kids high, the schemer and the sports kid standard,
        // the henchman low for a kid; Mr. Slave and the
        // Turner perimeter on the deep shelf; the anchor and
        // the aide their grown families
        "Pip" to BlipFam.KID, "Dougie" to BlipFam.KID_HIGH,
        "Jason" to BlipFam.KID, "Terrance Mephesto" to BlipFam.KID,
        "Jenny" to BlipFam.KID_HIGH, "Flora" to BlipFam.KID_HIGH,
        "Filmore" to BlipFam.KID_HIGH, "Quaid" to BlipFam.KID_HIGH,
        "Mimsy" to BlipFam.KID, "Tricia" to BlipFam.KID_HIGH,
        "Mr. Slave" to BlipFam.ADULT_DEEP,
        "Mr. Turner" to BlipFam.ADULT_DEEP,
        "Linda Black" to BlipFam.ADULT_F, "Harriet" to BlipFam.ADULT_F,
        "Nancy" to BlipFam.ADULT_F, "Mrs. Stevens" to BlipFam.ADULT_F,
        "Mrs. Stoley" to BlipFam.ADULT_F, "Maggie" to BlipFam.ADULT_F,
        "Choksondik" to BlipFam.ADULT_F, "Strong Woman" to BlipFam.ADULT_F,
        "Mr. Biggle" to BlipFam.ADULT_M, "Mr. Stevens" to BlipFam.ADULT_M,
        "Mr. Stoley" to BlipFam.ADULT_M, "Clark" to BlipFam.ADULT_M,
        "Mitch" to BlipFam.ADULT_M, "Bob Johnson" to BlipFam.ADULT_M,
        "Pusslicker" to BlipFam.ADULT_M, "Dr. Doctor" to BlipFam.ADULT_M
    )

    /** Cast names hit the table; anyone else (generated townsfolk)
     *  gets a stable hash-picked family — same name, same voice. */
    fun familyFor(name: String): BlipFam =
        BLIP_FAMILIES[name] ?: when (kotlin.math.abs(name.hashCode()) % 3) {
            0 -> BlipFam.KID
            1 -> BlipFam.ADULT_M
            else -> BlipFam.ADULT_F
        }

    /** v0.4.0e: the emotional contour of a spoken line, read
     *  off the line itself. A blip is one chirp; the contour
     *  bends it — excited rides higher and faster, sad drags
     *  lower and slower, angry comes out sharper (a harder
     *  soft-clip = more harmonics = more edge). NEUTRAL is
     *  the contour every blip shipped with through v0.4.0d:
     *  its multipliers are all exactly 1, so a NEUTRAL render
     *  is byte-identical to the legacy renderBlip — the
     *  suite pins that, because the SoundPool cache on every
     *  phone in town is full of the old chirps. */
    enum class BlipMood(val pitch: Double, val dur: Double, val grit: Float) {
        NEUTRAL(1.0, 1.0, 1.0f),
        EXCITED(1.16, 0.82, 1.0f),
        SAD(0.84, 1.30, 0.8f),
        ANGRY(1.07, 0.90, 1.7f)
    }

    private val ANGRY_WORDS = setOf("dammit", "damn", "hate", "stupid",
        "shut", "screw", "idiot", "jerk")
    private val SAD_WORDS = setOf("sigh", "sorry", "lonely", "miss", "alone",
        "sad", "crying")
    private val EXCITED_WORDS = setOf("awesome", "yay", "whoa", "sweet",
        "amazing", "incredible")

    /** Read a line's mood from the data the line already
     *  carries — its words and punctuation. Caps-shouting is
     *  anger (unless the speaker is Timmy, for whom caps are
     *  just… talking); ellipses and the sad words drag;
     *  exclamation lifts. Ordered: sad beats excited when a
     *  line does both ("I miss you!!" is not a party). */
    fun moodOfLine(speaker: String, text: String): BlipMood {
        val words = text.lowercase().split(Regex("[^a-z']+"))
            .filter { it.isNotEmpty() }
        val capsShout = speaker != "Timmy" && text.split(Regex("\\s+")).any { w ->
            val letters = w.filter { it.isLetter() }
            letters.length >= 3 && letters.all { it.isUpperCase() }
        }
        if (capsShout || words.any { it in ANGRY_WORDS }) return BlipMood.ANGRY
        if (text.contains("…") || text.contains("...") ||
            words.any { it in SAD_WORDS }) return BlipMood.SAD
        if (text.contains("!") || words.any { it in EXCITED_WORDS })
            return BlipMood.EXCITED
        return BlipMood.NEUTRAL
    }

    /** A short voice-like chirp: two-segment pitch glide through a
     *  soft-clipped harmonic stack. MUFFLED runs the same chirp
     *  through a heavy one-pole lowpass with a parka wobble — the
     *  shape of speech with the words sat on. */
    fun renderBlip(fam: BlipFam, seed: Long): ShortArray =
        renderBlip(fam, seed, BlipMood.NEUTRAL)

    /** The mood-contoured chirp (v0.4.0e). Same voice, bent:
     *  [BlipMood.pitch] scales the base pitch, [BlipMood.dur]
     *  the chirp's length, [BlipMood.grit] the soft-clip's
     *  hardness. At NEUTRAL every multiplier is exactly 1 and
     *  the arithmetic below reduces term-for-term to the
     *  legacy render — byte-identical, pinned by the suite. */
    fun renderBlip(fam: BlipFam, seed: Long, mood: BlipMood): ShortArray {
        val n = (fam.durSec * mood.dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(seed + fam.ordinal * 7919L)
        val wob = 0.92 + 0.16 * (rnd.nextFloat() * 0.5 + 0.5)
        val f0 = fam.baseHz * wob * mood.pitch
        var lp = 0f
        val lpAlpha = if (fam == BlipFam.MUFFLED) 0.10f else 1.0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            // chirp contour: quick rise, settle down
            val glide = if (frac < 0.3) 1.0 + 0.22 * (frac / 0.3)
                else 1.22 - 0.30 * ((frac - 0.3) / 0.7)
            val f = f0 * glide
            val ph = 2 * PI * f * t
            var v = (sin(ph) + 0.55 * sin(2 * ph) + 0.25 * sin(3 * ph)).toFloat()
            v = v / (1f + kotlin.math.abs(v) * mood.grit) // soft clip = vocal-ish grit
            if (fam == BlipFam.MUFFLED) {
                // parka AM wobble + the lowpass eats the consonants
                v *= 0.75f + 0.25f * sin(2 * PI * 28.0 * t).toFloat()
                lp += lpAlpha * (v - lp)
                v = lp * 2.2f
            }
            val env = attackRelease(frac, 0.12)
            out[i] = v * env * 0.55f
        }
        return limitPeak(toPcm(out), PeakBudget.BLIP_CEILING)
    }

    // ---------------- v0.4.0e: greeting barks ----------------

    /** Render one character's whole bark pattern (Barks.kt)
     *  as a single buffer: each note is the character's own
     *  blip voice, pitched by the note's semitone offset and
     *  stretched to the note's beat length, with the pattern's
     *  gap of silence between notes. One buffer per bark —
     *  the rhythm can't drift the way scheduled one-shots
     *  would. Unknown names render silence (callers gate on
     *  the table, so this is a belt-and-braces default). */
    fun renderBark(name: String): ShortArray {
        // v0.13.0a: patternForAny — the cast table first, the
        // household table (Mom/Dad) behind it; a name in
        // neither still renders silence, as before.
        val pat = Barks.patternForAny(name) ?: return ShortArray(0)
        val fam = familyFor(name)
        val beatSec = fam.durSec
        val totalSec = Barks.totalBeats(pat) * beatSec
        val buf = FloatArray((totalSec * SAMPLE_RATE).toInt().coerceAtLeast(1))
        var atSec = 0.0
        pat.notes.forEachIndexed { idx, note ->
            val durSec = note.len * beatSec
            val pcm = barkNote(fam, note.semi, durSec,
                seed = name.hashCode().toLong() * 131L + idx * 17L,
                electronic = pat.electronic)
            val at = (atSec * SAMPLE_RATE).toInt()
            for (j in pcm.indices) {
                val i = at + j
                if (i in buf.indices) buf[i] += pcm[j] / 32768f
            }
            atSec += (note.len + pat.gap) * beatSec
        }
        return limitPeak(toPcm(buf), PeakBudget.BARK_CEILING)
    }

    /** One note of a bark, in the family's voice: the blip
     *  chirp's harmonic stack and soft clip, the MUFFLED
     *  family's parka lowpass when the family calls for it —
     *  or, for an electronic voice box, a stepped pitch (no
     *  glide: the box only knows the note it's told), a
     *  squared-off harder clip, and a faint fast buzz in the
     *  amplitude. */
    private fun barkNote(fam: BlipFam, semi: Int, durSec: Double,
                         seed: Long, electronic: Boolean): ShortArray {
        val n = (durSec * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val out = FloatArray(n)
        val rnd = Lcg(seed + fam.ordinal * 7919L)
        val wob = 0.92 + 0.16 * (rnd.nextFloat() * 0.5 + 0.5)
        val f0 = fam.baseHz * wob * 2.0.pow(semi / 12.0)
        var lp = 0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val glide = when {
                electronic -> 1.0 // the box does not bend pitch
                frac < 0.3 -> 1.0 + 0.22 * (frac / 0.3)
                else -> 1.22 - 0.30 * ((frac - 0.3) / 0.7)
            }
            val f = f0 * glide
            val ph = 2 * PI * f * t
            var v = (sin(ph) + 0.55 * sin(2 * ph) + 0.25 * sin(3 * ph)).toFloat()
            v = if (electronic) {
                // squared off: a much harder clip flattens the
                // wave toward a square, plus the buzz
                var e = v / (1f + kotlin.math.abs(v) * 2.6f) * 1.65f
                e *= 0.82f + 0.18f * sin(2 * PI * 55.0 * t).toFloat()
                e
            } else v / (1f + kotlin.math.abs(v))
            if (!electronic && fam == BlipFam.MUFFLED) {
                v *= 0.75f + 0.25f * sin(2 * PI * 28.0 * t).toFloat()
                lp += 0.10f * (v - lp)
                v = lp * 2.2f
            }
            val env = attackRelease(frac, 0.12)
            out[i] = v * env * 0.55f
        }
        return toPcm(out)
    }

    // ---------------- instruments ----------------

    /** Banjo-ish pluck: additive harmonics, each decaying faster
     *  than the one below it, plus a pick transient. [tau] is the
     *  fundamental's decay constant — short tau = banjo snap. */
    fun pluck(freq: Double, durSec: Double, gain: Float, tau: Double,
              seed: Long = 0L): ShortArray {
        val n = (durSec * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val out = FloatArray(n)
        val rnd = Lcg(seed + (freq * 10).toLong())
        val amps = doubleArrayOf(1.0, 0.55, 0.32, 0.18, 0.10, 0.05)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            // tiny attack pitch-bend, like a string settling
            val bend = 1.0 + 0.004 * exp(-t * 40.0)
            var v = 0.0
            for (h in amps.indices) {
                val tauH = tau / (1.0 + 0.55 * h)
                v += amps[h] * sin(2 * PI * freq * bend * (h + 1) * t) *
                    exp(-t / tauH)
            }
            // pick noise: 10ms of lowpassed static at the strike
            if (i < SAMPLE_RATE / 100) {
                v += rnd.nextFloat() * 0.30 * exp(-t * 260.0)
            }
            out[i] = (v * gain * envCap(t, durSec)).toFloat()
        }
        return toPcm(out)
    }

    /** Sine-partial bell/chime tone. */
    private fun bell(freq: Double, durSec: Double, gain: Float): ShortArray {
        val n = (durSec * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val partials = listOf(1.0 to 1.0, 2.76 to 0.45, 5.40 to 0.18, 8.93 to 0.07)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            var v = 0.0
            for ((mult, a) in partials) {
                v += a * sin(2 * PI * freq * mult * t) * exp(-t * (2.2 + mult * 0.9))
            }
            out[i] = (v * gain * envCap(t, durSec)).toFloat()
        }
        return toPcm(out)
    }

    private fun midiHz(midi: Int): Double =
        440.0 * 2.0.pow((midi - 69) / 12.0)

    // ---------------- the SFX themselves ----------------

    private fun uiTap(): ShortArray {
        val tone = pluck(1180.0, 0.07, 0.35f, 0.03)
        return tone
    }

    private fun uiSelect(): ShortArray {
        val buf = FloatArray((0.18 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(76), 0.16, 0.34f, 0.06), 0)
        mixInto(buf, pluck(midiHz(83), 0.16, 0.34f, 0.06), (0.055 * SAMPLE_RATE).toInt())
        return toPcm(buf)
    }

    private fun uiBack(): ShortArray {
        val buf = FloatArray((0.15 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(79), 0.13, 0.30f, 0.05), 0)
        mixInto(buf, pluck(midiHz(71), 0.13, 0.30f, 0.05), (0.05 * SAMPLE_RATE).toInt())
        return toPcm(buf)
    }

    private fun snowStep(seed: Long): ShortArray {
        // crunch: shaped noise through a lowpass whose cutoff
        // collapses as the boot sinks, over a low thump. Two-stage
        // envelope = the crust breaking, then the pack.
        val dur = 0.16
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(4242 + seed * 131)
        var lp = 0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val alpha = (0.55 - 0.38 * frac).toFloat()
            lp += alpha * (rnd.nextFloat() - lp)
            val env = (exp(-frac * 5.5) * 0.8 + exp(-((frac - 0.24) * (frac - 0.24)) / 0.004) * 0.5)
            val thump = sin(2 * PI * 68.0 * t) * exp(-t * 30.0) * 0.35
            out[i] = (lp * 1.5 * env + thump).toFloat()
        }
        return toPcm(out)
    }

    private fun stepFloor(seed: Long): ShortArray {
        val dur = 0.09
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(977 + seed * 57)
        var lp = 0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            lp += 0.5f * (rnd.nextFloat() - lp)
            val knock = sin(2 * PI * 165.0 * t) * exp(-t * 55.0) * 0.5
            out[i] = (lp * exp(-t * 60.0) * 0.5 + knock).toFloat()
        }
        return toPcm(out)
    }

    private fun door(open: Boolean): ShortArray {
        // creak: a slow, gritty pitch glide (saw-ish stack) with a
        // latch click at the end of opening / start of closing.
        val dur = 0.34
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(if (open) 31337 else 31338)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val f = if (open) 82.0 + 55.0 * frac else 128.0 - 48.0 * frac
            var v = (sin(2 * PI * f * t) + 0.6 * sin(4 * PI * f * t) +
                0.3 * sin(6 * PI * f * t)).toFloat()
            v *= 0.16f + 0.05f * rnd.nextFloat() // hinges are not smooth
            val creakEnv = sin(PI * frac.coerceIn(0.0, 1.0)).toFloat()
            out[i] = v * creakEnv * 0.8f
        }
        val clickAt = if (open) (0.30 * SAMPLE_RATE).toInt() else (0.02 * SAMPLE_RATE).toInt()
        mixInto(out, pluck(2400.0, 0.03, 0.25f, 0.008), clickAt)
        val thudAt = if (open) 0 else (0.26 * SAMPLE_RATE).toInt()
        if (!open) mixInto(out, errorThudPcm(0.35f), thudAt)
        return toPcm(out)
    }

    private fun snowThrow(): ShortArray {
        // whoosh: noise split into a fast lowpass minus a slow
        // lowpass — the band between them sweeps up as it leaves
        val dur = 0.28
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(60606)
        var fast = 0f; var slow = 0f
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val w = rnd.nextFloat()
            fast += (0.10 + 0.35 * frac).toFloat() * (w - fast)
            slow += 0.015f * (w - slow)
            val band = fast - slow
            val env = sin(PI * frac).pow(1.5)
            out[i] = (band * 3.4 * env).toFloat()
        }
        return toPcm(out)
    }

    private fun snowSplat(): ShortArray {
        val dur = 0.22
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(8181)
        var lp = 0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            lp += 0.35f * (rnd.nextFloat() - lp)
            val burst = lp * exp(-t * 22.0) * 1.6
            val thump = sin(2 * PI * 95.0 * t) * exp(-t * 26.0) * 0.55
            out[i] = (burst + thump).toFloat()
        }
        return toPcm(out)
    }

    private fun giftChime(): ShortArray {
        val buf = FloatArray((0.55 * SAMPLE_RATE).toInt())
        mixInto(buf, bell(midiHz(88), 0.5, 0.32f), 0)                       // E6
        mixInto(buf, bell(midiHz(95), 0.5, 0.30f), (0.09 * SAMPLE_RATE).toInt()) // B6
        return toPcm(buf)
    }

    private fun foundSparkle(): ShortArray {
        // pentatonic run up + a shimmer tail — the "shiny!" sound
        val notes = listOf(74, 78, 81, 86) // D5 F#5 A5 D6
        val buf = FloatArray((0.6 * SAMPLE_RATE).toInt())
        notes.forEachIndexed { i, m ->
            mixInto(buf, bell(midiHz(m), 0.45, 0.30f), (i * 0.07 * SAMPLE_RATE).toInt())
        }
        mixInto(buf, bell(midiHz(93), 0.5, 0.12f), (0.30 * SAMPLE_RATE).toInt()) // A6 shimmer
        return toPcm(buf)
    }

    private fun questAccept(): ShortArray {
        // two plucked fifths: "alright, let's go"
        val buf = FloatArray((0.4 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(62), 0.35, 0.45f, 0.16, seed = 11), 0)          // D4
        mixInto(buf, pluck(midiHz(69), 0.35, 0.45f, 0.16, seed = 12),
            (0.11 * SAMPLE_RATE).toInt())                                          // A4
        return toPcm(buf)
    }

    private fun questComplete(): ShortArray {
        val notes = listOf(62, 69, 74, 81) // D4 A4 D5 A5
        val buf = FloatArray((0.7 * SAMPLE_RATE).toInt())
        notes.forEachIndexed { i, m ->
            mixInto(buf, pluck(midiHz(m), 0.5, 0.42f, 0.22, seed = 20L + i),
                (i * 0.10 * SAMPLE_RATE).toInt())
        }
        return toPcm(buf)
    }

    private fun errorThudPcm(gain: Float): FloatArray {
        val dur = 0.18
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(1357)
        var lp = 0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val f = 110.0 - 45.0 * (i.toDouble() / n)
            lp += 0.25f * (rnd.nextFloat() - lp)
            out[i] = (sin(2 * PI * f * t) * exp(-t * 20.0) * 0.8 +
                lp * exp(-t * 30.0) * 0.5).toFloat() * gain
        }
        return out
    }

    private fun errorThud(): ShortArray = toPcm(errorThudPcm(1.0f))

    private fun stingTitle(): ShortArray {
        // the episode opens: a banjo-roll flourish into the tune's
        // first two melody notes — original motif, same instruments
        val buf = FloatArray((1.1 * SAMPLE_RATE).toInt())
        val roll = listOf(62, 66, 69, 74, 69, 66)
        roll.forEachIndexed { i, m ->
            mixInto(buf, pluck(midiHz(m), 0.3, 0.34f, 0.10, seed = 30L + i),
                (i * 0.055 * SAMPLE_RATE).toInt())
        }
        mixInto(buf, pluck(midiHz(69), 0.5, 0.48f, 0.30, seed = 40), (0.36 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(71), 0.6, 0.48f, 0.34, seed = 41), (0.55 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(50), 0.7, 0.36f, 0.5, seed = 42), (0.36 * SAMPLE_RATE).toInt())
        return toPcm(buf)
    }

    private fun stingAct(): ShortArray {
        // act break: one dry pluck figure, there and gone
        val buf = FloatArray((0.45 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(66), 0.3, 0.42f, 0.12, seed = 51), 0)
        mixInto(buf, pluck(midiHz(62), 0.35, 0.42f, 0.14, seed = 52),
            (0.10 * SAMPLE_RATE).toInt())
        return toPcm(buf)
    }

    private fun stingCredits(): ShortArray {
        // credits: the tune's closing gesture, slower and softer
        val notes = listOf(69 to 0.0, 66 to 0.42, 64 to 0.84, 62 to 1.26)
        val buf = FloatArray((2.2 * SAMPLE_RATE).toInt())
        for ((m, at) in notes) {
            mixInto(buf, pluck(midiHz(m), 0.9, 0.40f, 0.42, seed = 60L + m),
                (at * SAMPLE_RATE).toInt())
        }
        mixInto(buf, pluck(midiHz(50), 1.0, 0.30f, 0.6, seed = 70), (1.26 * SAMPLE_RATE).toInt())
        return toPcm(buf)
    }

    private fun cueStage(): ShortArray {
        // something shifted in town: two close plucks, unresolved
        val buf = FloatArray((0.5 * SAMPLE_RATE).toInt())
        mixInto(buf, pluck(midiHz(63), 0.35, 0.40f, 0.16, seed = 81), 0)
        mixInto(buf, pluck(midiHz(62), 0.4, 0.40f, 0.18, seed = 82),
            (0.13 * SAMPLE_RATE).toInt())
        return toPcm(buf)
    }

    private fun cameraShutter(): ShortArray {
        // v0.2.0c: a camera in three beats, all synthesized —
        // mirror CLICK (bright noise tick + a 1.9 kHz ping), the
        // second curtain clack a beat lower, then the FLASH POP:
        // a soft lowpassed-noise puff with a low thump under it.
        val buf = FloatArray((0.45 * SAMPLE_RATE).toInt())
        val rnd = Lcg(90210)
        fun click(at: Int, bright: Double, gain: Float) {
            val n = (0.03 * SAMPLE_RATE).toInt()
            var hp = 0f; var prev = 0f
            for (j in 0 until n) {
                val t = j.toDouble() / SAMPLE_RATE
                val w = rnd.nextFloat()
                hp = (0.72 * (hp + w - prev)).toFloat(); prev = w
                val ping = sin(2 * PI * bright * t) * exp(-t * 160.0)
                val v = (hp * 1.6 + ping * 0.7) * exp(-t * 90.0) * gain
                if (at + j in buf.indices) buf[at + j] += v.toFloat()
            }
        }
        click(0, 1900.0, 0.85f)
        click((0.075 * SAMPLE_RATE).toInt(), 1300.0, 0.7f)
        // flash pop at 0.16s
        val popAt = (0.16 * SAMPLE_RATE).toInt()
        val popN = (0.14 * SAMPLE_RATE).toInt()
        var lp = 0f
        for (j in 0 until popN) {
            val t = j.toDouble() / SAMPLE_RATE
            lp += 0.16f * (rnd.nextFloat() - lp)
            val thump = sin(2 * PI * 210.0 * t) * exp(-t * 26.0) * 0.6
            val v = (lp * 2.4 * exp(-t * 30.0) + thump) * 0.8
            if (popAt + j in buf.indices) buf[popAt + j] += v.toFloat()
        }
        return toPcm(buf)
    }

    private fun fishSplash(): ShortArray {
        // v0.2.0e: the bite — a bobber PLIP (sine dropping
        // 640→190 Hz, the classic cartoon water drop) chased by
        // a short splash of lowpassed noise, like something
        // just remembered it has a tail.
        val dur = 0.42
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        val rnd = Lcg(424242)
        var lp = 0f
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val f = 640.0 - 450.0 * (t / 0.14).coerceIn(0.0, 1.0)
            val plip = if (t < 0.14)
                sin(2 * PI * f * t) * sin(PI * (t / 0.14)).pow(1.5) * 0.55
            else 0.0
            lp += 0.22f * (rnd.nextFloat() - lp)
            val splash = if (t > 0.10)
                lp * 1.9 * exp(-(t - 0.10) * 13.0) * sin(PI * ((t - 0.10) / 0.30).coerceIn(0.0, 1.0))
            else 0.0
            out[i] = (plip + splash).toFloat()
        }
        return toPcm(out)
    }

    private fun coinDing(): ShortArray {
        // v0.2.0e: the capsule coin payout — the two-bell ding
        // every arcade on Earth agrees on (B5 then E6, quick).
        val buf = FloatArray((0.5 * SAMPLE_RATE).toInt())
        mixInto(buf, bell(midiHz(83), 0.32, 0.30f), 0)                       // B5
        mixInto(buf, bell(midiHz(88), 0.45, 0.32f), (0.07 * SAMPLE_RATE).toInt()) // E6
        return toPcm(buf)
    }

    private fun tacoPop(): ShortArray {
        // v0.2.0e: a catch — one soft round pop, like the tray
        // just said "got it" in a very small voice.
        val dur = 0.10
        val n = (dur * SAMPLE_RATE).toInt()
        val out = FloatArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val f = 320.0 - 210.0 * (t / dur)
            out[i] = (sin(2 * PI * f * t) * exp(-t * 46.0) * 0.6).toFloat()
        }
        return toPcm(out)
    }

    // ---------------- ambience ----------------

    enum class AmbBed { NONE, WIND_LIGHT, WIND_STRONG, RAIN, CRICKETS, ROOM }

    /** Which bed the world gets. Weather beats the clock; indoors
     *  beats everything. Pure — the suite pins the whole table.
     *  v0.3.0e: [aweNight] is the SkyState aurora flag — on an
     *  aurora night the crickets stand down for the soft-air
     *  hush (WIND_LIGHT at clear-sky weather, which is the
     *  only weather an aurora can be seen through anyway):
     *  the whole town, insects included, is watching the sky. */
    fun selectAmbience(weatherId: String, timeMin: Int, inside: Boolean,
                       aweNight: Boolean = false): AmbBed {
        if (inside) return AmbBed.ROOM
        when (weatherId) {
            "rain" -> return AmbBed.RAIN
            "blizzard", "heavy_snow", "wind" -> return AmbBed.WIND_STRONG
            "light_snow", "fog" -> return AmbBed.WIND_LIGHT
        }
        val night = timeMin >= 22 * 60 || timeMin < 5 * 60
        if (night && aweNight) return AmbBed.WIND_LIGHT
        if (night && (weatherId == "sunny" || weatherId == "cloudy")) return AmbBed.CRICKETS
        return AmbBed.NONE
    }

    /** Seamless loops, exactly AMB_LOOP_SAMPLES long. Noise beds
     *  are wrap-crossfaded into themselves; the crickets are laid
     *  on a grid that tiles, so the loop point is silent. */
    fun renderAmbience(bed: AmbBed): ShortArray {
        val n = AMB_LOOP_SAMPLES
        val raw = FloatArray(n + SAMPLE_RATE) // +1s tail for the wrap
        when (bed) {
            AmbBed.NONE -> return ShortArray(n)
            AmbBed.ROOM -> {
                // leaky-integrated brown noise, barely there
                val rnd = Lcg(20240)
                var last = 0f
                for (i in raw.indices) {
                    last = (last + 0.02f * rnd.nextFloat()) / 1.02f
                    raw[i] = last * 1.4f
                }
            }
            AmbBed.WIND_LIGHT, AmbBed.WIND_STRONG -> {
                val strong = bed == AmbBed.WIND_STRONG
                val rnd = Lcg(if (strong) 9090 else 8080)
                var lp = 0f
                for (i in raw.indices) {
                    val t = i.toDouble() / SAMPLE_RATE
                    // gusts: slow LFOs open the filter and lift gain
                    val gust = 0.55 + 0.30 * sin(2 * PI * 0.07 * t) +
                        0.15 * sin(2 * PI * 0.023 * t + 1.7)
                    val alpha = ((if (strong) 0.05 else 0.035) +
                        (if (strong) 0.075 else 0.04) * gust).toFloat()
                    lp += alpha * (rnd.nextFloat() - lp)
                    raw[i] = (lp * (if (strong) 2.6 else 1.9) *
                        (0.55 + 0.45 * gust)).toFloat()
                }
            }
            AmbBed.RAIN -> {
                val rnd = Lcg(5150)
                var lp = 0f
                for (i in raw.indices) {
                    val w = rnd.nextFloat()
                    lp += 0.06f * (w - lp)
                    raw[i] = (w - lp) * 0.5f // patter = the hiss above the wash
                }
                // droplet ticks, seeded — same rain every loop
                val tickRnd = kotlin.random.Random(777)
                repeat(110) {
                    val at = tickRnd.nextInt(raw.size - 400)
                    val f = 1700.0 + tickRnd.nextInt(1500)
                    val len = 90
                    for (j in 0 until len) {
                        val t = j.toDouble() / SAMPLE_RATE
                        raw[at + j] += (sin(2 * PI * f * t) * exp(-t * 300.0) * 0.16).toFloat()
                    }
                }
            }
            AmbBed.CRICKETS -> {
                // two insects trading chirps on a 0.8s grid; each
                // chirp = 3 short pulses. Grid tiles the 8s loop.
                for (g in 0 until 10) {
                    val base = g * 0.8
                    val (f, off) = if (g % 2 == 0) 4200.0 to 0.0 else 4600.0 to 0.45
                    for (p in 0 until 3) {
                        val start = ((base + off + p * 0.11) * SAMPLE_RATE).toInt()
                        val len = (0.055 * SAMPLE_RATE).toInt()
                        for (j in 0 until len) {
                            if (start + j >= raw.size) break
                            val t = j.toDouble() / SAMPLE_RATE
                            val env = sin(PI * j.toDouble() / len)
                            raw[start + j] += (sin(2 * PI * f * t) * env * 0.14).toFloat()
                        }
                    }
                }
            }
        }
        // wrap-crossfade the last second into the first so the
        // loop point is the render's own continuation
        val fade = SAMPLE_RATE
        val out = FloatArray(n)
        for (i in 0 until n) {
            out[i] = if (i < fade) {
                val w = i.toFloat() / fade
                raw[i] * w + raw[n + i] * (1f - w)
            } else raw[i]
        }
        return toPcm(out)
    }

    // ---------------- mixing helpers ----------------

    private fun attackRelease(frac: Double, attack: Double): Float {
        val a = if (frac < attack) frac / attack else 1.0
        val r = if (frac > 0.8) (1.0 - frac) / 0.2 else 1.0
        return (a * r).toFloat()
    }

    /** Gentle end-cap so decaying synth tails don't click. */
    private fun envCap(t: Double, durSec: Double): Double {
        val tail = 0.012
        return if (t > durSec - tail) (durSec - t) / tail else 1.0
    }

    private fun mixInto(dst: FloatArray, src: ShortArray, at: Int) {
        for (j in src.indices) {
            val i = at + j
            if (i in dst.indices) dst[i] += src[j] / 32768f
        }
    }

    private fun mixInto(dst: FloatArray, src: FloatArray, at: Int) {
        for (j in src.indices) {
            val i = at + j
            if (i in dst.indices) dst[i] += src[j]
        }
    }

    /** Float mix → 16-bit PCM with a soft peak normalize only
     *  when the mix actually runs hot (keeps quiet sounds quiet
     *  and every render deterministic). The 0.92 ceiling here is
     *  the AMBIENCE path's; one-shots get peak-limited further
     *  at their entry points (see PeakBudget). */
    private fun toPcm(buf: FloatArray): ShortArray {
        var peak = 0f
        for (v in buf) peak = maxOf(peak, kotlin.math.abs(v))
        val scale = if (peak > 0.92f) 0.92f / peak else 1f
        return ShortArray(buf.size) { i ->
            (buf[i] * scale * 32767f).toInt().coerceIn(-32768, 32767).toShort()
        }
    }

    /** Peak-limit a finished render to [ceiling] of full scale.
     *  Renders already under the ceiling pass through untouched
     *  (byte-identical to their unlimited render); hotter ones
     *  scale down whole, so the waveform — and its determinism —
     *  survives. This is the render-path half of the peak
     *  budget: it guarantees the per-source peaks the budget
     *  math below assumes. */
    private fun limitPeak(pcm: ShortArray, ceiling: Float): ShortArray {
        var peak = 0
        for (v in pcm) peak = maxOf(peak, kotlin.math.abs(v.toInt()))
        val cap = (ceiling * 32767f).toInt()
        if (peak <= cap || peak == 0) return pcm
        val s = cap.toFloat() / peak.toFloat()
        return ShortArray(pcm.size) { i -> (pcm[i] * s).toInt().toShort() }
    }

    // ---------------- v0.2.0g: the peak budget ----------------
    // Every channel the engine can sound at once, at the
    // DEFAULT volumes, with the square-law gains AudioSettings
    // applies. Measured on the real renders, the pre-budget
    // stack summed to 1.60 (music 0.245 + loudest bed 0.182 +
    // two loudest one-shots at 0.589 each) — hard clipping the
    // moment a sting landed over a gust of wind. The budget:
    // each source family gets a peak allocation, enforced by
    // render ceilings (one-shots, blips) and by the chain
    // multipliers Sound.kt applies on top of the channel gains
    // (it reads THESE constants, so player and budget can
    // never drift apart). Both designed stacks must stay under
    // 1.0 full scale:
    //   namedStack     = music + bed + loudest sting + blip
    //   doubleSfxStack = music + bed + 2 x loudest one-shot
    // (the SoundPool's designed concurrency is two one-shots;
    // snow steps ride under the SFX allocation at a 0.9 play
    // scale, so they never dominate it.)
    object PeakBudget {
        /** Sound.kt: music track volume = gain x presence(<=1) x this. */
        const val MUSIC_CHAIN = 0.7f
        /** Sound.kt: ambience track volume = gain x this. */
        const val AMB_CHAIN = 0.45f
        /** Render ceiling for every one-shot (renderSfx / snow steps). */
        const val SFX_CEILING = 0.48f
        /** Render ceiling for voice blips. */
        const val BLIP_CEILING = 0.48f
        /** v0.4.0e: render ceiling for greeting barks — the
         *  same allocation as a blip; a bark occupies the
         *  blip's slot in the stack, just longer. */
        const val BARK_CEILING = 0.48f
        /** Sound.kt plays blips at this gain scale. */
        const val BLIP_GAIN_SCALE = 0.9f
        /** v0.4.0e: Sound.kt plays barks at this gain scale
         *  (before Barks' own indoor/evening shaping). */
        const val BARK_GAIN_SCALE = 0.9f
        /** The loudest play() call uses gain scale 1.0. */
        const val SFX_MAX_GAIN_SCALE = 1.0f

        fun peakOf(pcm: ShortArray): Float {
            var peak = 0
            for (v in pcm) peak = maxOf(peak, kotlin.math.abs(v.toInt()))
            return peak / 32767f
        }

        /** The tune at full presence, post-gain. */
        fun musicPeakOut(): Float =
            AudioSettings.DEFAULT.gain(AudioChannel.MUSIC) * MUSIC_CHAIN *
                peakOf(TownMusic.renderLoop())

        /** The loudest bed, post-gain. */
        fun ambiencePeakOut(): Float =
            AudioSettings.DEFAULT.gain(AudioChannel.AMBIENCE) * AMB_CHAIN *
                AmbBed.values().filter { it != AmbBed.NONE }
                    .maxOf { peakOf(renderAmbience(it)) }

        /** The loudest one-shot at the loudest play scale, post-gain. */
        fun sfxPeakOut(): Float =
            AudioSettings.DEFAULT.gain(AudioChannel.SFX) * SFX_MAX_GAIN_SCALE *
                SFX_IDS.maxOf { peakOf(renderSfx(it)) }

        /** The loudest cached blip at its play scale, post-gain. */
        fun blipPeakOut(): Float =
            AudioSettings.DEFAULT.gain(AudioChannel.SFX) * BLIP_GAIN_SCALE *
                BlipFam.values().maxOf { peakOf(renderBlip(it, 1234L + it.ordinal)) }

        /** The loudest bark at its play scale, post-gain
         *  (v0.4.0e). Measured on the real renders across the
         *  whole cast table. */
        fun barkPeakOut(): Float =
            AudioSettings.DEFAULT.gain(AudioChannel.SFX) * BARK_GAIN_SCALE *
                Barks.PATTERNS.keys.maxOf { peakOf(renderBark(it)) }

        /** The loudest mood-contoured blip, post-gain
         *  (v0.4.0e): every family x every contour. */
        fun moodBlipPeakOut(): Float =
            AudioSettings.DEFAULT.gain(AudioChannel.SFX) * BLIP_GAIN_SCALE *
                BlipFam.values().maxOf { fam ->
                    BlipMood.values().maxOf {
                        peakOf(renderBlip(fam, 1234L + fam.ordinal, it))
                    }
                }

        /** Blip + sting + ambience + tune, peaks summed: the
         *  stack that must never clip. */
        fun namedStack(): Float =
            musicPeakOut() + ambiencePeakOut() + sfxPeakOut() + blipPeakOut()

        /** v0.4.0e: the named stack with a bark in the voice
         *  slot — a greeting landing over a sting, a bed, and
         *  the tune is the loudest polite moment the new
         *  system can make. Must stay under full scale too. */
        fun barkStack(): Float =
            musicPeakOut() + ambiencePeakOut() + sfxPeakOut() + barkPeakOut()

        /** Two one-shots over bed + tune (the pool's designed
         *  concurrency), peaks summed. */
        fun doubleSfxStack(): Float =
            musicPeakOut() + ambiencePeakOut() + 2f * sfxPeakOut()
    }

    /** 16-bit mono PCM WAV — the runtime cache format for the
     *  SoundPool (files are generated on the phone, never shipped). */
    fun wavBytes(pcm: ShortArray): ByteArray {
        val dataSize = pcm.size * 2
        val out = java.io.ByteArrayOutputStream(44 + dataSize)
        fun wStr(s: String) { for (c in s) out.write(c.code) }
        fun wInt(v: Int) { for (k in 0..3) out.write(v shr (8 * k) and 0xff) }
        fun wShort(v: Int) { for (k in 0..1) out.write(v shr (8 * k) and 0xff) }
        wStr("RIFF"); wInt(36 + dataSize); wStr("WAVE")
        wStr("fmt "); wInt(16); wShort(1); wShort(1)
        wInt(SAMPLE_RATE); wInt(SAMPLE_RATE * 2); wShort(2); wShort(16)
        wStr("data"); wInt(dataSize)
        for (s in pcm) wShort(s.toInt())
        return out.toByteArray()
    }
}

// ============================================================
// TownMusic — the town tune. ORIGINAL composition for this
// game, written as data: a D-major pentatonic melody (30 notes,
// below) over D – G – D – A – Bm – G – D – A, one chord per bar,
// 96 BPM, banjo-roll accompaniment. The melody's contour was
// composed for this table; it is not a transcription, quote, or
// arrangement of any existing tune — folk progressions and the
// pentatonic scale belong to everyone, this line belongs to
// the game.
// ============================================================
object TownMusic {
    const val BPM = 96
    const val BARS = 8
    const val BEATS_PER_BAR = 4
    const val LOOP_SEC = BARS * BEATS_PER_BAR * 60.0 / BPM // 20.0
    val LOOP_SAMPLES: Int = (LOOP_SEC * SoundGen.SAMPLE_RATE).toInt()

    /** Per bar: chord-root midi (for the bass) + the four roll
     *  tones (midi). Progression: D G D A Bm G D A. */
    val CHORD_BASS: List<Int> = listOf(38, 43, 38, 45, 47, 43, 38, 45)
    val CHORD_TONES: List<List<Int>> = listOf(
        listOf(62, 66, 69, 74), // D
        listOf(62, 67, 71, 74), // G
        listOf(62, 66, 69, 74), // D
        listOf(64, 69, 73, 76), // A
        listOf(62, 66, 71, 74), // Bm
        listOf(62, 67, 71, 74), // G
        listOf(62, 66, 69, 74), // D
        listOf(64, 69, 73, 76)  // A
    )

    /** One melody note: bar, beat-in-bar, midi, length in beats.
     *  Strictly D-major pentatonic pitch classes {D, E, F#, A, B}. */
    data class Note(val bar: Int, val beat: Double, val midi: Int, val len: Double)

    val MELODY: List<Note> = listOf(
        Note(0, 0.0, 69, 1.0), Note(0, 1.0, 71, 1.0),
        Note(0, 2.0, 74, 1.5), Note(0, 3.5, 71, 0.5),
        Note(1, 0.0, 74, 1.0), Note(1, 1.0, 71, 1.0), Note(1, 2.0, 69, 2.0),
        Note(2, 0.0, 66, 1.0), Note(2, 1.0, 69, 1.0),
        Note(2, 2.0, 71, 1.0), Note(2, 3.0, 69, 1.0),
        Note(3, 0.0, 76, 1.5), Note(3, 1.5, 74, 0.5),
        Note(3, 2.0, 71, 1.0), Note(3, 3.0, 69, 1.0),
        Note(4, 0.0, 71, 1.0), Note(4, 1.0, 74, 1.0),
        Note(4, 2.0, 78, 1.0), Note(4, 3.0, 74, 1.0),
        Note(5, 0.0, 71, 1.5), Note(5, 1.5, 69, 0.5),
        Note(5, 2.0, 66, 1.0), Note(5, 3.0, 69, 1.0),
        Note(6, 0.0, 74, 1.0), Note(6, 1.0, 71, 1.0),
        Note(6, 2.0, 69, 1.0), Note(6, 3.0, 66, 1.0),
        Note(7, 0.0, 64, 1.0), Note(7, 1.0, 66, 1.0), Note(7, 2.0, 62, 2.0)
    )

    /** The banjo roll: eighth-note pattern over the chord tones. */
    val ROLL_PATTERN: List<Int> = listOf(0, 1, 2, 3, 2, 1, 2, 3)

    fun renderLoop(): ShortArray {
        val sr = SoundGen.SAMPLE_RATE
        val beatSec = 60.0 / BPM
        val mix = FloatArray(LOOP_SAMPLES + sr) // tail room for decays
        fun addPluck(midi: Int, atSec: Double, durSec: Double,
                     gain: Float, tau: Double, seed: Long) {
            val pcm = SoundGen.pluck(
                440.0 * 2.0.pow((midi - 69) / 12.0), durSec, gain, tau, seed)
            val at = (atSec * sr).toInt()
            for (j in pcm.indices) {
                val i = at + j
                if (i in mix.indices) mix[i] += pcm[j] / 32768f
            }
        }
        for (bar in 0 until BARS) {
            val barStart = bar * BEATS_PER_BAR * beatSec
            // roll: eighth notes across the chord tones
            for (e in 0 until 8) {
                val tone = CHORD_TONES[bar][ROLL_PATTERN[e]]
                addPluck(tone, barStart + e * 0.5 * beatSec, 0.30, 0.16f, 0.10,
                    seed = 1000L + bar * 16 + e)
            }
            // bass on 1 and 3
            addPluck(CHORD_BASS[bar], barStart, 0.8, 0.30f, 0.45, seed = 2000L + bar)
            addPluck(CHORD_BASS[bar] + 7, barStart + 2 * beatSec, 0.7, 0.22f, 0.4,
                seed = 2100L + bar)
        }
        for (n in MELODY) {
            val at = (n.bar * BEATS_PER_BAR + n.beat) * beatSec
            addPluck(n.midi, at, maxOf(0.35, n.len * beatSec * 0.95), 0.42f, 0.30,
                seed = 3000L + n.bar * 8 + (n.beat * 2).toInt())
        }
        // fold the decay tail back over the head: the loop wraps
        // seamlessly instead of clicking at the seam
        val out = FloatArray(LOOP_SAMPLES)
        for (i in out.indices) out[i] = mix[i]
        for (i in LOOP_SAMPLES until mix.size) {
            out[i - LOOP_SAMPLES] += mix[i] * 0.8f
        }
        var peak = 0f
        for (v in out) peak = maxOf(peak, kotlin.math.abs(v))
        val scale = if (peak > 0.85f) 0.85f / peak else 1f
        return ShortArray(LOOP_SAMPLES) { i ->
            (out[i] * scale * 32767f).toInt().coerceIn(-32768, 32767).toShort()
        }
    }
}

// ============================================================
// AudioSettings — the three channel volumes + master mute.
// Pure value math (Prefs persists the raw fields; the engine
// consumes gains). Gain curve is squared for a perceptual taper.
// ============================================================
enum class AudioChannel { MUSIC, SFX, AMBIENCE }

data class AudioSettings(
    val music: Int = 60,
    val sfx: Int = 80,
    val ambience: Int = 60,
    val muted: Boolean = false
) {
    fun volumeOf(channel: AudioChannel): Int = when (channel) {
        AudioChannel.MUSIC -> music
        AudioChannel.SFX -> sfx
        AudioChannel.AMBIENCE -> ambience
    }.coerceIn(0, 100)

    fun gain(channel: AudioChannel): Float {
        if (muted) return 0f
        val v = volumeOf(channel) / 100f
        return v * v
    }

    fun withVolume(channel: AudioChannel, v: Int): AudioSettings {
        val c = v.coerceIn(0, 100)
        return when (channel) {
            AudioChannel.MUSIC -> copy(music = c)
            AudioChannel.SFX -> copy(sfx = c)
            AudioChannel.AMBIENCE -> copy(ambience = c)
        }
    }

    companion object {
        val DEFAULT = AudioSettings()
    }
}

/** Should the town tune be playing, and how present? Night puts
 *  it to bed; indoors it comes through the walls, softer. */
fun musicPresence(inside: Boolean, timeMin: Int): Float {
    val night = timeMin >= 23 * 60 || timeMin < 6 * 60
    if (night) return 0f
    return if (inside) 0.35f else 1f
}

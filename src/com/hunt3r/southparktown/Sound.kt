package com.hunt3r.southparktown

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import java.io.File
import java.util.concurrent.Executors

// ============================================================
// SoundEngine — v0.2.0b playback. All sample DATA comes from
// SoundGen (pure synthesis); this file only moves it to the
// speakers:
//
//  - SFX: one-shots rendered to WAV files in the app's cache
//    dir on first launch (generated on the phone — the APK
//    itself contains ZERO audio files), loaded into a SoundPool.
//  - Music + ambience: looping AudioTracks in static mode,
//    fed the generated loop buffers. Ambience crossfades between
//    beds; the tune's presence follows time/indoors.
//
// Everything rides STREAM_MUSIC (AudioAttributes USAGE_MEDIA):
// the media volume keys rule, and when the ringer is on SILENT
// the beds and tune politely stay off. Rendering happens on a
// background executor; playback calls are cheap and null-safe
// before init finishes (early taps are just quiet).
// ============================================================
object SoundEngine {

    private const val CACHE_DIR_NAME = "soundgen_v1"
    private const val STAMP = "v020g_r1"

    private val bgExec = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())

    @Volatile private var settings: AudioSettings = AudioSettings.DEFAULT
    @Volatile private var poolReady = false
    @Volatile private var loopsReady = false
    @Volatile private var started = false
    private var initStarted = false

    private var pool: SoundPool? = null
    private val soundIds = HashMap<String, Int>()

    private var musicPcm: ShortArray? = null
    private val ambPcm = HashMap<SoundGen.AmbBed, ShortArray>()
    private var musicTrack: AudioTrack? = null
    private var ambTrack: AudioTrack? = null
    private var ambFading: AudioTrack? = null
    private var curBed: SoundGen.AmbBed = SoundGen.AmbBed.NONE
    private var curMusicPresence: Float = -1f
    private var worldWeather: String = ""
    private var worldTimeMin: Int = 480
    private var worldInside: Boolean = false
    private var worldAwe: Boolean = false
    private var appCtx: Context? = null

    private var lastBlipMs = 0L

    // ---------------- setup ----------------

    @Synchronized
    fun init(ctx: Context) {
        if (initStarted) return
        initStarted = true
        appCtx = ctx.applicationContext
        bgExec.execute {
            try {
                buildPool(ctx.applicationContext)
                musicPcm = TownMusic.renderLoop()
                for (bed in SoundGen.AmbBed.values()) {
                    if (bed != SoundGen.AmbBed.NONE) {
                        ambPcm[bed] = SoundGen.renderAmbience(bed)
                    }
                }
                loopsReady = true
                ui.post { if (started) refreshLoops() }
            } catch (e: Exception) {
                // sound is garnish — never take the town down
            }
        }
    }

    private fun buildPool(ctx: Context) {
        val dir = File(ctx.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        val stamp = File(dir, "stamp.txt")
        val fresh = stamp.exists() && stamp.readText() == STAMP
        val sp = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            .build()
        fun load(id: String, pcm: ShortArray) {
            val f = File(dir, "$id.wav")
            if (!fresh || !f.exists()) f.writeBytes(SoundGen.wavBytes(pcm))
            soundIds[id] = sp.load(f.absolutePath, 1)
        }
        // v0.2.0g: readiness is EARNED, not estimated. The old
        // code slept 120 ms and declared the pool ready — on a
        // slow first launch the first taps of the first session
        // could land before their sounds existed and be silently
        // dropped. Now the pool reports each load as it lands;
        // EVERY callback counts (a failed load too — one bad
        // file must not mute the whole town), and the pool is
        // ready exactly when the last expected sound checks in.
        val moodVariants = SoundGen.BlipMood.values().count { it != SoundGen.BlipMood.NEUTRAL }
        val expectedLoads = SoundGen.SFX_IDS.size +
            SoundGen.SNOW_STEP_VARIANTS + SoundGen.BlipFam.values().size +
            SoundGen.BlipFam.values().size * moodVariants + Barks.PATTERNS.size +
            Barks.HOUSEHOLD_PATTERNS.size
        var completedLoads = 0
        sp.setOnLoadCompleteListener { _, _, _ ->
            completedLoads++
            if (completedLoads >= expectedLoads) poolReady = true
        }
        for (id in SoundGen.SFX_IDS) load(id, SoundGen.renderSfx(id))
        for (i in 0 until SoundGen.SNOW_STEP_VARIANTS) {
            load("step_snow_$i", SoundGen.renderSnowStep(i.toLong()))
        }
        for (fam in SoundGen.BlipFam.values()) {
            load("blip_${fam.name}", SoundGen.renderBlip(fam, 1234L + fam.ordinal))
        }
        // v0.4.0e: the mood-contoured blips (NEUTRAL is the
        // legacy render above, byte-identical — no new file)
        // and one rendered bark pattern per cast member. All
        // NEW filenames: the stamp stays "v020g_r1" because
        // every pre-existing render is unchanged, and load()
        // writes any file that's missing regardless of stamp.
        for (fam in SoundGen.BlipFam.values()) {
            for (mood in SoundGen.BlipMood.values()) {
                if (mood == SoundGen.BlipMood.NEUTRAL) continue
                load("blip_${fam.name}_${mood.name}",
                    SoundGen.renderBlip(fam, 1234L + fam.ordinal, mood))
            }
        }
        for (name in Barks.PATTERNS.keys) {
            load("bark_$name", SoundGen.renderBark(name))
        }
        // v0.13.0a: the household barks (Mom/Dad) render and
        // load the same way, from their own table.
        for (name in Barks.HOUSEHOLD_PATTERNS.keys) {
            load("bark_$name", SoundGen.renderBark(name))
        }
        if (!fresh) stamp.writeText(STAMP)
        pool = sp
    }

    fun applySettings(s: AudioSettings) {
        settings = s
        ui.post { refreshLoops(force = true) }
    }

    // ---------------- lifecycle ----------------

    fun startLoops() {
        started = true
        ui.post { refreshLoops() }
    }

    fun pauseLoops() {
        started = false
        ui.post {
            try {
                musicTrack?.pause()
                ambTrack?.pause()
                ambFading?.pause()
            } catch (e: Exception) { /* garnish */ }
        }
    }

    /** The GameActivity calls this whenever the world visibly
     *  changes (and on the 10s tick); it only acts on real changes.
     *  v0.3.0e: [aweNight] carries the SkyState aurora flag into
     *  the bed picker (the crickets stand down for the hush). */
    fun updateWorld(weatherId: String, timeMin: Int, inside: Boolean,
                    aweNight: Boolean = false) {
        worldWeather = weatherId
        worldTimeMin = timeMin
        worldInside = inside
        worldAwe = aweNight
        ui.post { refreshLoops() }
    }

    private fun ringerSilent(): Boolean {
        val am = appCtx?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return false
        return am.ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    private fun refreshLoops(force: Boolean = false) {
        if (!loopsReady || !started) return
        val polite = !ringerSilent()
        // ---- music ----
        val presence = if (polite) musicPresence(worldInside, worldTimeMin) else 0f
        val mGain = settings.gain(AudioChannel.MUSIC) * presence *
            SoundGen.PeakBudget.MUSIC_CHAIN
        if (mGain > 0.01f) {
            val pcm = musicPcm
            if (pcm != null && musicTrack == null) musicTrack = loopTrack(pcm)
            musicTrack?.let { t ->
                try {
                    t.setVolume(mGain)
                    if (t.playState != AudioTrack.PLAYSTATE_PLAYING) t.play()
                } catch (e: Exception) { /* garnish */ }
            }
        } else {
            try { musicTrack?.pause() } catch (e: Exception) { /* garnish */ }
        }
        if (force || presence != curMusicPresence) curMusicPresence = presence
        // ---- ambience ----
        val want = if (polite) SoundGen.selectAmbience(worldWeather, worldTimeMin, worldInside, worldAwe)
            else SoundGen.AmbBed.NONE
        val aGain = settings.gain(AudioChannel.AMBIENCE) * SoundGen.PeakBudget.AMB_CHAIN
        if (want != curBed || force) {
            crossfadeAmbience(want, aGain)
            curBed = want
        } else if (want != SoundGen.AmbBed.NONE) {
            try { ambTrack?.setVolume(aGain) } catch (e: Exception) { /* garnish */ }
        }
    }

    private fun loopTrack(pcm: ShortArray): AudioTrack? {
        return try {
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SoundGen.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            t.setLoopPoints(0, pcm.size, -1)
            t
        } catch (e: Exception) {
            null
        }
    }

    /** Swap the ambience bed: old track ramps down over ~0.5s
     *  while the new one ramps up — beds never stack. */
    private fun crossfadeAmbience(want: SoundGen.AmbBed, gain: Float) {
        val old = ambTrack
        ambTrack = null
        if (old != null) {
            ambFading?.let { try { it.stop(); it.release() } catch (e: Exception) {} }
            ambFading = old
        }
        if (want != SoundGen.AmbBed.NONE && gain > 0.01f) {
            val pcm = ambPcm[want]
            if (pcm != null) {
                val t = loopTrack(pcm)
                if (t != null) {
                    ambTrack = t
                    try {
                        t.setVolume(0f)
                        t.play()
                    } catch (e: Exception) { /* garnish */ }
                }
            }
        }
        val newT = ambTrack
        val steps = 10
        for (s in 1..steps) {
            ui.postDelayed({
                val f = s.toFloat() / steps
                try {
                    newT?.setVolume(gain * f)
                    ambFading?.setVolume(gain * (1f - f))
                } catch (e: Exception) { /* garnish */ }
                if (s == steps) {
                    try { ambFading?.stop(); ambFading?.release() } catch (e: Exception) {}
                    ambFading = null
                }
            }, s * 50L)
        }
    }

    // ---------------- one-shots ----------------

    private fun play(id: String, gainScale: Float = 1f, rate: Float = 1f) {
        if (!poolReady) return
        val g = settings.gain(AudioChannel.SFX) * gainScale
        if (g <= 0.001f) return
        val sid = soundIds[id] ?: return
        try {
            pool?.play(sid, g, g, 1, 0, rate.coerceIn(0.5f, 2f))
        } catch (e: Exception) { /* garnish */ }
    }

    fun tap() = play("ui_tap")
    fun select() = play("ui_select")
    fun back() = play("ui_back")
    fun thud() = play("error_thud")

    /** A voice blip for whoever just got a speech bubble. Rate
     *  wobbles a little per line so nobody sounds like a metronome;
     *  calls are throttled so a 4-line batch chirps, not buzzes. */
    fun blip(speaker: String, isPlayer: Boolean) =
        blip(speaker, isPlayer, SoundGen.BlipMood.NEUTRAL)

    /** v0.4.0e: the mood-contoured blip. The contour is baked
     *  into the cached render (pitch/length/grit), so the
     *  playback rate stays near 1 — just a breath of jitter —
     *  instead of the rate doing double duty as the pitch. */
    fun blip(speaker: String, isPlayer: Boolean, mood: SoundGen.BlipMood) {
        val now = System.currentTimeMillis()
        if (now - lastBlipMs < 80) return
        lastBlipMs = now
        val fam = if (isPlayer) SoundGen.BlipFam.PLAYER
            else SoundGen.familyFor(speaker)
        if (mood == SoundGen.BlipMood.NEUTRAL) {
            val jitter = 0.94f + (kotlin.math.abs(speaker.hashCode() + now.toInt()) % 13) / 100f
            play("blip_${fam.name}", 0.9f, jitter)
        } else {
            val jitter = 0.98f + (kotlin.math.abs(speaker.hashCode() + now.toInt()) % 5) / 100f
            play("blip_${fam.name}_${mood.name}",
                SoundGen.PeakBudget.BLIP_GAIN_SCALE, jitter)
        }
    }

    /** v0.4.0e: a greeting bark — [name]'s rhythm signature,
     *  one cached render, played at the budget's bark scale
     *  shaped by Barks.volumeScale (the caller computes it:
     *  indoors softer, evening quieter). */
    fun bark(name: String, gainScale: Float) {
        // v0.13.0a: patternForAny — cast unchanged; Mom and
        // Dad resolve through the household table too.
        if (Barks.patternForAny(name) == null) return
        play("bark_$name", SoundGen.PeakBudget.BARK_GAIN_SCALE * gainScale)
    }

    /** v0.16.0e (spec B4): ONE footstep, fired by a gait
     *  notify — the walk cycle's phase crossing a foot-
     *  down — never by a timer. [variant] alternates the
     *  snow renders per foot; [gain] lets a traveler's
     *  step sit under the player's. If the cycle stops,
     *  no notify fires and no step sounds: the silence is
     *  the contract working. */
    fun footstep(snow: Boolean, inside: Boolean, variant: Int,
                 gain: Float = 1f) {
        if (inside) play("step_floor", 0.8f * gain, 0.95f)
        else if (snow) play(
            "step_snow_${variant % SoundGen.SNOW_STEP_VARIANTS}",
            0.9f * gain)
        else play("step_floor", 0.55f * gain, 0.9f)
    }

    fun footsteps(snow: Boolean, inside: Boolean, steps: Int = 4) {
        for (i in 0 until steps) {
            ui.postDelayed({
                if (inside) play("step_floor", 0.8f, 0.95f + i * 0.03f)
                else if (snow) play("step_snow_${i % SoundGen.SNOW_STEP_VARIANTS}", 0.9f)
                else play("step_floor", 0.55f, 0.9f) // bare-ground patter rides the floor tap, softer
            }, i * 240L)
        }
    }

    fun door(open: Boolean) {
        if (open) play("door_open") else play("door_close")
    }

    /** Throw, then the splat lands a beat later. */
    fun snowball() {
        play("snow_throw")
        ui.postDelayed({ play("snow_splat") }, 330)
    }

    fun gift(loved: Boolean) {
        play("gift_chime")
        if (loved) ui.postDelayed({ play("found_sparkle", 0.7f) }, 130)
    }

    fun found(setComplete: Boolean) {
        play("found_sparkle")
        if (setComplete) ui.postDelayed({ play("quest_complete") }, 200)
    }

    fun titleSting() = play("sting_title")
    fun actSting() = play("sting_act")
    fun creditsMotif() = play("sting_credits")
    fun stageCue() = play("cue_stage")
    fun questAccept() = play("quest_accept")
    fun questComplete() = play("quest_complete")

    /** v0.2.0c: the selfie shutter — click-clack, flash pop. */
    fun shutter() = play("camera_shutter")

    /** v0.2.0e: fishing — the bite plip and the catch splash
     *  are one render at two gains; the splash IS the plip,
     *  believed in harder. */
    fun fishBite() = play("fish_splash", 0.55f)
    fun fishCatch() = play("fish_splash", 1.0f)

    /** v0.2.0e: a capsule coin lands in the stack. */
    fun coinDing() = play("coin_ding")

    /** v0.2.0e: Taco Catch — the tray says "got it". */
    fun tacoPop() = play("taco_pop")
}

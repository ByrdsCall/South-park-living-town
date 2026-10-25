package com.hunt3r.southparktown

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

// ============================================================
// SettingsActivity — API key (never logged), About with the
// fan-project disclaimer, Reset Town, re-run creator.
// ============================================================
class SettingsActivity : Activity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(AndroidSaveStore(this))
        SoundEngine.init(this)
        SoundEngine.applySettings(prefs.audioSettings())

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val scroll = ScrollView(this)
        scroll.addView(box)
        setContentView(scroll)

        fun title(t: String) {
            box.addView(TextView(this).apply {
                text = t
                setTextColor(Color.parseColor(Paper.RED))
                textSize = 22f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                setPadding(0, dp(12), 0, dp(6))
            })
        }

        fun label(t: String) {
            box.addView(TextView(this).apply {
                text = t
                setTextColor(Color.parseColor(Paper.YELLOW))
                textSize = 15f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
                setPadding(0, dp(6), 0, dp(4))
            })
        }

        fun body(t: String) {
            box.addView(TextView(this).apply {
                text = t
                setTextColor(Color.parseColor("#e8e8e8"))
                textSize = 14f
                setPadding(0, 0, 0, dp(8))
            })
        }

        fun button(t: String, onClick: () -> Unit) {
            box.addView(Button(this).apply {
                text = t
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                // v0.1.9b: chunky red game button (was a flat fill)
                background = UiKit.buttonBg(this@SettingsActivity, Paper.RED)
                setOnClickListener { SoundEngine.tap(); onClick() }
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = dp(8)
                layoutParams = lp
            })
        }

        title("⚙ SETTINGS")

        // ---- LLM provider ----
        label("LLM PROVIDER")
        body("Pick who makes the kids talk. Without a key for the selected provider, " +
            "the kids run on the built-in mock brain (still rude, still funny).")
        val providerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val geminiBtn = Button(this)
        val orBtn = Button(this)
        fun refreshProviderButtons() {
            val sel = prefs.getLlmProvider()
            geminiBtn.apply {
                text = "🔷 Gemini"
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@SettingsActivity,
                    if (sel == "gemini") Paper.RED else "#555555")
            }
            orBtn.apply {
                text = "🟣 OpenRouter"
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@SettingsActivity,
                    if (sel == "openrouter") Paper.RED else "#555555")
            }
        }
        geminiBtn.setOnClickListener {
            prefs.setLlmProvider("gemini")
            refreshProviderButtons()
            Toast.makeText(this, "Provider: Gemini", Toast.LENGTH_SHORT).show()
        }
        orBtn.setOnClickListener {
            prefs.setLlmProvider("openrouter")
            refreshProviderButtons()
            Toast.makeText(this, "Provider: OpenRouter", Toast.LENGTH_SHORT).show()
        }
        val btnLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        providerRow.addView(geminiBtn, btnLp)
        providerRow.addView(orBtn, btnLp)
        box.addView(providerRow)
        refreshProviderButtons()

        fun keyField(initial: String): EditText {
            return EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setText(initial)
                setTextColor(Color.parseColor("#1a1a1a"))
                // v0.1.9b: cardstock field with an edge line
                background = UiKit.inputBg(this@SettingsActivity)
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
        }

        fun modelField(initial: String): EditText {
            return EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                setText(initial)
                setTextColor(Color.parseColor("#1a1a1a"))
                background = UiKit.inputBg(this@SettingsActivity)
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
        }

        // ---- Gemini ----
        label("🔷 GEMINI")
        body("Free key from Google AI Studio. Your key is stored on this phone only — " +
            "it is never logged and never sent anywhere except Google's API.")
        label("Model")
        val geminiModelInput = modelField(prefs.getGeminiModel())
        box.addView(geminiModelInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        label("API key")
        val geminiKeyInput = keyField(prefs.getGeminiKey())
        geminiKeyInput.hint = "paste key here"
        box.addView(geminiKeyInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        button("💾 Save Gemini settings") {
            prefs.setGeminiModel(geminiModelInput.text.toString())
            prefs.setGeminiKey(geminiKeyInput.text.toString())
            Toast.makeText(this, "Gemini settings saved.", Toast.LENGTH_SHORT).show()
        }
        button("🧹 Clear Gemini key (back to mock brain)") {
            prefs.setGeminiKey("")
            geminiKeyInput.setText("")
            Toast.makeText(this, "Key cleared. Mock brain engaged.", Toast.LENGTH_SHORT).show()
        }

        // ---- OpenRouter ----
        label("🟣 OPENROUTER")
        body("Use any OpenRouter model with your own OpenRouter key. " +
            "Your key is stored on this phone only — it is never logged and " +
            "never sent anywhere except OpenRouter's API.")
        label("Model")
        val orModelInput = modelField(prefs.getOpenRouterModel())
        box.addView(orModelInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        label("API key")
        val orKeyInput = keyField(prefs.getOpenRouterKey())
        orKeyInput.hint = "paste key here"
        box.addView(orKeyInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        button("💾 Save OpenRouter settings") {
            prefs.setOpenRouterModel(orModelInput.text.toString())
            prefs.setOpenRouterKey(orKeyInput.text.toString())
            Toast.makeText(this, "OpenRouter settings saved.", Toast.LENGTH_SHORT).show()
        }
        button("🧹 Clear OpenRouter key (back to mock brain)") {
            prefs.setOpenRouterKey("")
            orKeyInput.setText("")
            Toast.makeText(this, "Key cleared. Mock brain engaged.", Toast.LENGTH_SHORT).show()
        }

        // ---- Clock ----
        label("🕗 TOWN CLOCK")
        body("In-game clock (default): the town runs on 15-minute ticks from 8 AM, " +
            "advancing as you play and wait. Day, night, and seasons all follow it. " +
            "Real-time sync: the town follows your phone's actual clock instead.")
        val clockBtn = Button(this)
        fun refreshClockButton() {
            val on = prefs.isRealtimeSync()
            clockBtn.apply {
                text = if (on) "🟢 Real-time sync: ON" else "⚪ Real-time sync: OFF (in-game clock)"
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@SettingsActivity,
                    if (on) Paper.RED else "#555555")
            }
        }
        clockBtn.setOnClickListener {
            val next = !prefs.isRealtimeSync()
            prefs.setRealtimeSync(next)
            refreshClockButton()
            Toast.makeText(this,
                if (next) "Real-time sync ON. Town follows your phone's clock."
                else "In-game clock ON. 15-minute ticks from 8 AM.",
                Toast.LENGTH_SHORT).show()
        }
        box.addView(clockBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        refreshClockButton()

        // ---- Sound (v0.2.0b: every sound is synthesized live in
        // code — plucks, blips, weather beds, one original town
        // tune. No audio files exist anywhere in this app.) ----
        label("🔊 SOUND")
        body("The town tune, speech blips, footsteps, and weather are all " +
            "synthesized on the spot by the app — original sounds, made of math. " +
            "Volumes ride your phone's media volume; on silent, the tune and " +
            "weather beds politely sit out.")
        val muteBtn = Button(this)
        fun refreshMuteButton() {
            val s = prefs.audioSettings()
            muteBtn.apply {
                text = if (s.muted) "🔇 Sound: MUTED" else "🔊 Sound: ON"
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@SettingsActivity,
                    if (s.muted) "#555555" else Paper.RED)
            }
        }
        muteBtn.setOnClickListener {
            val s = prefs.audioSettings()
            val next = s.copy(muted = !s.muted)
            prefs.setAudioSettings(next)
            SoundEngine.applySettings(next)
            refreshMuteButton()
            if (!next.muted) SoundEngine.select()
        }
        box.addView(muteBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        refreshMuteButton()

        fun volumeRow(channel: AudioChannel, titleText: String) {
            label(titleText)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val minus = Button(this)
            val value = TextView(this)
            val plus = Button(this)
            fun refreshRow() {
                val s = prefs.audioSettings()
                value.text = "${s.volumeOf(channel)}%"
                value.setTextColor(Color.parseColor("#e8e8e8"))
                value.textSize = 15f
                value.gravity = android.view.Gravity.CENTER
            }
            fun stepBtn(b: Button, labelText: String, delta: Int) {
                b.apply {
                    text = labelText
                    setTextColor(Color.parseColor(Chrome.INK))
                    typeface = android.graphics.Typeface.create("casual",
                        android.graphics.Typeface.BOLD)
                    background = UiKit.buttonBg(this@SettingsActivity, Paper.PARCHMENT)
                    setOnClickListener {
                        val cur = prefs.audioSettings()
                        val next = cur.withVolume(channel, cur.volumeOf(channel) + delta)
                        prefs.setAudioSettings(next)
                        SoundEngine.applySettings(next)
                        refreshRow()
                        // each channel previews itself
                        when (channel) {
                            AudioChannel.SFX -> SoundEngine.select()
                            AudioChannel.MUSIC -> SoundEngine.actSting()
                            AudioChannel.AMBIENCE -> SoundEngine.tap()
                        }
                    }
                }
            }
            stepBtn(minus, "−", -10)
            stepBtn(plus, "+", 10)
            val sideLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            val midLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
            row.addView(minus, sideLp)
            row.addView(value, midLp)
            row.addView(plus, sideLp)
            box.addView(row)
            refreshRow()
        }
        volumeRow(AudioChannel.MUSIC, "🎵 Tune")
        volumeRow(AudioChannel.SFX, "🎺 Effects & voices")
        volumeRow(AudioChannel.AMBIENCE, "🌨 Weather beds")

        // ---- Greeting barks (v0.4.0e: everybody has a rhythm
        // signature in their own blip voice — walk into a room
        // and somebody hails you, pattern first, hello bubble
        // over their head. Softer indoors and in the evening,
        // silent in the small hours.) ----
        label("🗣 GREETING BARKS")
        body("Walk into a place and one of the townsfolk there greets you " +
            "out loud — a little rhythm in their own voice, with a hello over " +
            "their head. Your closest friends speak up most; nobody barks " +
            "while asleep or after half past ten at night.")
        val barkBtn = Button(this)
        fun refreshBarkButton() {
            val on = prefs.barksEnabled()
            barkBtn.apply {
                text = if (on) "🗣 Greeting barks: ON" else "🤐 Greeting barks: OFF"
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@SettingsActivity,
                    if (on) Paper.RED else "#555555")
            }
        }
        barkBtn.setOnClickListener {
            val next = !prefs.barksEnabled()
            prefs.setBarksEnabled(next)
            refreshBarkButton()
            // the toggle previews itself: Butters is never not ready
            if (next) SoundEngine.bark("Butters", 1f)
        }
        box.addView(barkBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        refreshBarkButton()

        // ---- Paper boil (v0.16.0d: the diorama post pass's
        // hand-drawn breathing — the painted backdrop shifts a
        // single pixel on a slow 3-frame cycle, the way hand-
        // inked lines shimmer. Faces and words never move.) ----
        label("🎞 PAPER BOIL")
        body("The town's painted backdrops breathe by a pixel on a slow " +
            "cycle — the hand-drawn boil of cut-paper animation. Only the " +
            "scenery moves; faces, words and buttons stay perfectly still.")
        val boilBtn = Button(this)
        fun refreshBoilButton() {
            val on = prefs.boilEnabled()
            boilBtn.apply {
                text = if (on) "🎞 Paper boil: ON" else "🧊 Paper boil: OFF"
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@SettingsActivity,
                    if (on) Paper.RED else "#555555")
            }
        }
        boilBtn.setOnClickListener {
            prefs.setBoilEnabled(!prefs.boilEnabled())
            refreshBoilButton()
        }
        box.addView(boilBtn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        refreshBoilButton()

        // ---- About ----
        title("📖 ABOUT")
        body("SOUTH PARK: LIVING TOWN — a fan-made toy where Cartman, Stan, Kyle, Kenny " +
            "and Butters live their day across South Park, Colorado, and you walk in as the new kid.")
        label("FAN PROJECT, NON-COMMERCIAL")
        body("South Park and all characters are the property of their respective owners. " +
            "This is a free fan-made toy — no distribution, no packaging, no monetization. " +
            "All art in this app was newly drawn in code in the show's paper-cutout style; " +
            "no game files, screenshots, audio, or art were ripped into it. " +
            "Reference frames from the games were studied and transformed, never copied 1:1.")
        label("COZY RULES — NO FAIL STATES")
        body("No game over. No punishments. No missables. Collectibles re-roll every day, " +
            "friendships never decay, and a missed day just comes back around. " +
            "It's South Park — it just can't hurt you here.")
        label("💾 SAVE DATA")
        body(when (val fmt = prefs.storedSaveFormat()) {
            0 -> "No town saved yet. This build writes save format v$SAVE_FORMAT_VERSION."
            -1 -> "A save exists but its main file is unreadable — don't worry: on the " +
                "next launch the town salvages your friendships, collection and dates " +
                "from the backup keys instead of wiping anything. " +
                "This build writes save format v$SAVE_FORMAT_VERSION."
            else -> "Save format: v$fmt — this build writes v$SAVE_FORMAT_VERSION. " +
                "Older saves upgrade themselves on load; an update never wipes your town."
        })

        // ---- Danger zone ----
        title("☠ DANGER ZONE")
        button("🎨 Re-run character creator") {
            startActivity(Intent(this, CreatorActivity::class.java))
        }
        button("🌅 Reset Town (fresh day)") {
            // v0.1.9b: paper dialog kit (was a stock AlertDialog)
            UiKit.showMessageDialog(this, "Reset Town?",
                message = "Wipe the current day? Your kid and settings stay.",
                positiveLabel = "Reset",
                onPositive = {
                    prefs.clearGame()
                    Toast.makeText(this, "Town wiped. Reopen the game for a fresh day.", Toast.LENGTH_LONG).show()
                    finish()
                },
                negativeLabel = "Nah")
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

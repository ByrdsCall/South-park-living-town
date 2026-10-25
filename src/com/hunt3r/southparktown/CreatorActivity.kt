package com.hunt3r.southparktown

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// CreatorActivity — the character creator (13 categories since
// v0.1.9d: the original 7 + build, shoes, eyewear, facial hair,
// gloves, back items), plus the games' dice roll and 3 preset
// slots. Stick-of-Truth styling: charcoal, red casual title,
// yellow step labels, red-ring progress dots, red selection
// border, LIVE Canvas avatar preview.
// ============================================================
class CreatorActivity : Activity() {

    private val steps = CREATOR_ORDER + "name"
    private var step = 0
    private val appearance = DEFAULT_APPEARANCE.toMutableMap()
    private var playerName = ""
    private val presetBtns = mutableListOf<Button>()

    // v0.5.0b: the two lock shelves — #42 collectible finds and
    // the Wardrobe unlockables — read off the saved town (after
    // a retroactive sync, so feats crossed since the last visit
    // to town are already hanging in the closet)
    private var collectedSet: Set<String> = emptySet()
    private var unlockedSet: Set<String> = emptySet()

    // v0.15.0a: the saved town, kept so the preview can
    // show the LIVE look (LiveLook) — sleepwear on a
    // sleepover night, hero costume on a patrol night —
    // derived from the look under edit. A fresh town
    // (no save yet) previews the plain day look.
    private var townState: GameState? = null

    private lateinit var prefs: Prefs
    private lateinit var titleView: TextView
    private lateinit var stepLabel: TextView
    private lateinit var dotsRow: LinearLayout
    private lateinit var preview: AvatarPreviewView
    private lateinit var optionsScroll: ScrollView
    private lateinit var optionsGrid: GridLayout
    private lateinit var nameInput: EditText
    private lateinit var backBtn: Button
    private lateinit var nextBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(AndroidSaveStore(this))
        // v0.5.0b: load the town's lock shelves (syncing any
        // wardrobe grants owed since the town last ran)
        try {
            val st = prefs.loadGame()
            if (st != null) {
                townState = st
                if (Wardrobe.syncUnlocks(st).isNotEmpty()) prefs.saveGame(st)
                collectedSet = st.collected
                unlockedSet = st.unlockedWardrobe.toSet()
            }
        } catch (e: Exception) { /* a fresh town: everything locked */ }
        // resume in-progress appearance if re-running
        if (prefs.hasPlayer()) {
            // a look may only ever hold owned pieces — sanitize
            // on the way in (bites only a hand-edited save)
            appearance.putAll(Wardrobe.sanitizeAppearance(
                prefs.getAppearance(), collectedSet, unlockedSet))
            playerName = prefs.getPlayerName() ?: ""
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        titleView = TextView(this).apply {
            text = "MAKE YOUR KID"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 28f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            // v0.1.9b: the title rides a crooked parchment plaque,
            // like the games' character-sheet headers
            background = UiKit.plaqueBg(this@CreatorActivity, Paper.PARCHMENT)
            rotation = Chrome.TILT_DEG
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }
        root.addView(titleView, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })

        stepLabel = TextView(this).apply {
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 16f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(4))
        }
        root.addView(stepLabel)

        dotsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(dotsRow)

        preview = AvatarPreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(230))
        }
        root.addView(preview)

        // v0.1.9d: the games' creator furniture — a dice that rolls
        // a whole coherent look, and three preset slots for favorite
        // looks (tap = wear it, hold = save this look into the slot)
        val funRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        }
        val rollBtn = Button(this).apply {
            text = "🎲 Roll"
            setTextColor(Color.WHITE)
            background = UiKit.buttonBg(this@CreatorActivity, "#4a4a4a")
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setOnClickListener {
                appearance.putAll(randomAppearance(kotlin.random.Random.Default))
                render()
                android.widget.Toast.makeText(this@CreatorActivity,
                    "🎲 The dice have spoken.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        funRow.addView(rollBtn, LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f).apply { marginEnd = dp(6) })
        for (slot in 0 until CREATOR_PRESET_SLOTS) {
            val pb = Button(this).apply {
                setTextColor(Color.parseColor(Paper.YELLOW))
                background = UiKit.buttonBg(this@CreatorActivity, "#2a2a2a")
                setOnClickListener {
                    val saved = prefs.getCreatorPresets().getOrNull(slot)
                    if (saved != null) {
                        // v0.5.0b: a preset wears only owned
                        // pieces — sanitize on load like any look
                        appearance.clear()
                        appearance.putAll(Wardrobe.sanitizeAppearance(
                            saved, collectedSet, unlockedSet))
                        render()
                        android.widget.Toast.makeText(this@CreatorActivity,
                            "Preset ${slot + 1} — looking sharp.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(this@CreatorActivity,
                            "Slot ${slot + 1} is empty — HOLD it to save this look.",
                            android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                setOnLongClickListener {
                    // v0.5.0b: presets save owned sets only —
                    // the in-memory look already qualifies by
                    // construction; sanitize pins it regardless
                    prefs.saveCreatorPreset(slot, Wardrobe.sanitizeAppearance(
                        appearance, collectedSet, unlockedSet))
                    renderPresetBtns()
                    android.widget.Toast.makeText(this@CreatorActivity,
                        "💾 Saved this look to slot ${slot + 1}.", android.widget.Toast.LENGTH_SHORT).show()
                    true
                }
            }
            presetBtns.add(pb)
            funRow.addView(pb, LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(3); marginEnd = dp(3)
            })
        }
        root.addView(funRow)

        optionsScroll = ScrollView(this)
        optionsGrid = GridLayout(this).apply { columnCount = 2 }
        optionsScroll.addView(optionsGrid, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val gridLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(optionsScroll, gridLp)

        nameInput = EditText(this).apply {
            hint = "Your name, new kid..."
            setHintTextColor(Color.parseColor("#8a8a8a"))
            setTextColor(Color.parseColor("#1a1a1a"))
            inputType = InputType.TYPE_CLASS_TEXT
            background = UiKit.inputBg(this@CreatorActivity)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            visibility = View.GONE
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            textSize = 18f
        }
        root.addView(nameInput, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }
        backBtn = Button(this).apply {
            text = "◀ Back"
            setTextColor(Color.WHITE)
            background = UiKit.buttonBg(this@CreatorActivity, "#4a4a4a")
            setOnClickListener { if (step > 0) { step--; render() } else finish() }
        }
        nextBtn = Button(this).apply {
            text = "Next ▶"
            setTextColor(Color.WHITE)
            background = UiKit.buttonBg(this@CreatorActivity, Paper.RED)
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setOnClickListener { onNext() }
        }
        navRow.addView(backBtn, LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(8) })
        navRow.addView(nextBtn, LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) })
        root.addView(navRow)

        setContentView(root)
        render()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun onNext() {
        if (steps[step] == "name") {
            val n = nameInput.text.toString().trim()
            playerName = n.ifEmpty { PLAYER_DEFAULT_NAME }
            prefs.savePlayer(playerName, appearance)
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        } else if (step < steps.size - 1) {
            step++
            render()
        }
    }

    override fun onBackPressed() {
        if (step > 0) { step--; render() } else super.onBackPressed()
    }

    /** v0.15.0a: push the look under edit into the
     *  preview TOGETHER with its live derivation — the
     *  one place the preview's inputs are set, so the
     *  figure can never lag the edit or invent a look. */
    private fun syncPreview() {
        preview.appearance = appearance.toMap()
        val look = townState?.let {
            LiveLook.lookFor(it, appearance.toMap())
        }
        preview.liveKind = look?.kind ?: LiveLook.DAY
        preview.liveCode = look?.code ?: ""
        preview.invalidate()
    }

    private fun render() {
        val key = steps[step]
        stepLabel.text = "Step ${step + 1} of ${steps.size} — " +
            if (key == "name") "Name" else (CREATOR_OPTIONS[key]?.first ?: key)
        renderDots()
        renderPresetBtns()
        syncPreview()
        val isName = key == "name"
        optionsScroll.visibility = if (isName) View.GONE else View.VISIBLE
        nameInput.visibility = if (isName) View.VISIBLE else View.GONE
        if (isName) {
            nameInput.setText(playerName)
            nextBtn.text = "FINISH"
        } else {
            nextBtn.text = "Next ▶"
            renderOptions(key)
        }
        backBtn.visibility = if (step == 0) View.INVISIBLE else View.VISIBLE
    }

    /** Preset slot labels: 💾 N when a look is saved there. */
    private fun renderPresetBtns() {
        val presets = try { prefs.getCreatorPresets() }
            catch (e: Exception) { emptyList() }
        for ((i, b) in presetBtns.withIndex()) {
            b.text = if (presets.getOrNull(i) != null) "💾 ${i + 1}" else "${i + 1} ···"
        }
    }

    private fun renderDots() {
        dotsRow.removeAllViews()
        for (i in steps.indices) {
            val dot = TextView(this).apply {
                text = "●"
                textSize = 18f
                setTextColor(Color.parseColor(
                    when {
                        i == step -> Paper.YELLOW
                        i < step -> Paper.RED
                        else -> "#5a5a5a"
                    }))
                setPadding(dp(4), 0, dp(4), 0)
            }
            dotsRow.addView(dot)
        }
    }

    private fun renderOptions(category: String) {
        optionsGrid.removeAllViews()
        val opts = CREATOR_OPTIONS[category]?.second ?: return
        val chosen = appearance[category]
        // #42: wearable collectibles — found in the world, worn
        // here. v0.5.0b: the Wardrobe shelf hangs beside it —
        // locked pieces show greyed WITH their unlock hint and
        // cannot be picked (the dice never rolls them either).
        for (opt in opts) {
            val piece = Wardrobe.pieceFor(category, opt.id)
            val locked = (opt.unlockId != null && opt.unlockId !in collectedSet) ||
                (piece != null && piece.id !in unlockedSet)
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(10), dp(10), dp(10))
            }
            val selected = opt.id == chosen && !locked
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(10).toFloat()
                setColor(Color.parseColor(if (selected) "#3a1a1a" else "#2a2a2a"))
                setStroke(dp(if (selected) 4 else 1),
                    Color.parseColor(if (selected) Paper.RED else "#5a5a5a"))
            }
            cell.background = bg
            // swatch for color options
            opt.color?.let { hex ->
                val swatch = View(this).apply {
                    val d = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor(hex))
                        setStroke(dp(2), Color.parseColor("#000000"))
                    }
                    background = d
                }
                cell.addView(swatch, LinearLayout.LayoutParams(dp(44), dp(44)))
            }
            val nameView = TextView(this).apply {
                text = if (locked) "🔒 ${opt.name}" else opt.name
                setTextColor(Color.parseColor(
                    when {
                        locked -> "#7a7a7a"
                        selected -> Paper.YELLOW
                        else -> "#e8e8e8"
                    }))
                textSize = 14f
                typeface = android.graphics.Typeface.create("casual",
                    if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                gravity = Gravity.CENTER
                setPadding(0, dp(6), 0, 0)
            }
            cell.addView(nameView)
            // v0.5.0b: a locked WARDROBE piece wears its hint
            // right on the cell — where it comes from is the
            // whole point of the grey
            if (locked && piece != null) {
                cell.addView(TextView(this).apply {
                    text = piece.hint
                    setTextColor(Color.parseColor("#9a8a6a"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setPadding(dp(2), dp(3), dp(2), 0)
                })
            }
            cell.setOnClickListener {
                if (locked) {
                    if (piece != null) {
                        android.widget.Toast.makeText(this,
                            "🔒 ${piece.name} — ${piece.hint}",
                            android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        val want = itemById(opt.unlockId!!)?.name ?: "a rare collectible"
                        android.widget.Toast.makeText(this,
                            "🔒 Find the $want collectible out in town to wear this.",
                            android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    appearance[category] = opt.id
                    renderOptions(category)
                    syncPreview()
                }
            }
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }
            optionsGrid.addView(cell, lp)
        }
    }
}

/** Live avatar preview.
 *  v0.15.0a: the preview shows the LIVE look — the
 *  caller sets [liveKind]/[liveCode] from LiveLook
 *  (the one derivation shared with the mirror, the
 *  street and the shutter): sleepwear on a sleepover
 *  night through KidArt's sleep pass, hero costume
 *  on a patrol night through its hero pass, the day
 *  look otherwise. The costume passes draw the
 *  figure from the same avatarSpec as the day pass,
 *  so the build under edit wears identically in
 *  every kind. Defaults are the day look, so a
 *  caller that sets nothing previews plainly. */
class AvatarPreviewView(context: android.content.Context) : View(context) {
    var appearance: Map<String, String> = DEFAULT_APPEARANCE
    var liveKind: String = LiveLook.DAY
    var liveCode: String = ""
    override fun onDraw(c: android.graphics.Canvas) {
        super.onDraw(c)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return
        // parchment card backdrop (v0.1.9b: + the cardstock edge line)
        val card = android.graphics.RectF(w * 0.28f, 8f, w * 0.72f, h - 8f)
        c.drawRoundRect(android.graphics.RectF(card.left + 3, card.top + 4, card.right + 3, card.bottom + 4),
            12f, 12f, Paper.fillPaint("#000000", 90))
        c.drawRoundRect(card, 12f, 12f, Paper.fillPaint(Paper.PARCHMENT))
        c.drawRoundRect(card, 12f, 12f, Paper.strokePaint(Chrome.PARCHMENT_EDGE, 3f))
        // v0.16.0d (stage 10): the creator IS the paper-doll
        // screen — its previews render through the doll path
        when (liveKind) {
            LiveLook.SLEEP -> PaperDoll.drawDollSleep(c,
                avatarSpec(appearance), liveCode,
                w / 2f, h / 2f, h * 0.92f, "happy",
                appearance.hashCode().toLong())
            LiveLook.HERO -> PaperDoll.drawDollHero(c,
                avatarSpec(appearance), liveCode,
                w / 2f, h / 2f, h * 0.92f, "happy",
                appearance.hashCode().toLong())
            else -> PaperDoll.drawDollAvatar(c, appearance,
                w / 2f, h / 2f, h * 0.92f)
        }
        Paper.drawFibreGrain(c, 0f, 0f, w, h, 41L)
    }
}

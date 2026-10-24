package com.hunt3r.southparktown

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// QuestActivity — the quest tracker (v0.1.5).
//
// DYNAMIC APPEARANCE: the whole screen reskins to the active
// shenanigans event's QuestTheme (header band color + icon +
// framing text come from the event definition); with no event
// active it falls back to the default Town Board theme. Shows
// the event chain's 4 linear stages (done / current / next),
// the current objective, and the standing system objectives.
// ============================================================
class QuestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureLoreLoaded(this)
        val prefs = Prefs(this)
        prefs.loadNpcMemory()
        val state = prefs.loadGame()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
        }
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        scroll.addView(box)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        if (state == null) {
            box.addView(TextView(this).apply {
                text = "No town yet. Go walk around first, dude."
                setTextColor(Color.parseColor(Paper.YELLOW))
                textSize = 16f
            })
            setContentView(root)
            return
        }

        val view = Quests.build(state)
        val theme = view.theme

        // ---- themed header band ----
        // v0.1.9b: the band is a themed paper plaque (rounded,
        // darker same-hue edge) instead of a flat color block
        box.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.plaqueBg(this@QuestActivity, theme.colorHex)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(10)
            layoutParams = lp
            addView(TextView(this@QuestActivity).apply {
                text = "${theme.icon} ${theme.header}"
                setTextColor(Color.parseColor("#1a1a1a"))
                textSize = 22f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            })
            addView(TextView(this@QuestActivity).apply {
                text = view.headline
                setTextColor(Color.parseColor("#3a2f22"))
                textSize = 14f
            })
            addView(TextView(this@QuestActivity).apply {
                text = "🎯 Now: ${view.currentObjective}"
                setTextColor(Color.parseColor("#1a1a1a"))
                textSize = 15f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                setPadding(0, dp(6), 0, 0)
            })
        })

        fun card(): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.paperCardBg(this@QuestActivity)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(8)
            layoutParams = lp
        }

        // ---- event chain stages ----
        if (view.stages.isNotEmpty()) {
            box.addView(TextView(this).apply {
                text = "📖 THE STORY SO FAR"
                setTextColor(Color.parseColor(theme.colorHex))
                textSize = 19f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                setPadding(0, dp(6), 0, dp(6))
            })
            for (s in view.stages) {
                val c = card()
                val mark = when (s.state) {
                    0 -> "✅"
                    1 -> "▶️"
                    else -> "🔒"
                }
                c.addView(TextView(this).apply {
                    text = "$mark ${s.title}"
                    setTextColor(Color.parseColor(
                        if (s.state == 2) "#8a8078" else "#1a1a1a"))
                    textSize = 16f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                if (s.state != 2) {
                    c.addView(TextView(this).apply {
                        text = s.note
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 13f
                    })
                }
                box.addView(c)
            }
        }

        // ---- standing objectives ----
        box.addView(TextView(this).apply {
            text = "📌 TOWN OBJECTIVES"
            setTextColor(Color.parseColor(theme.colorHex))
            textSize = 19f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(6))
        })
        for (o in view.objectives) {
            val c = card()
            c.addView(TextView(this).apply {
                text = "${if (o.done) "✅" else o.icon} ${o.text}  —  ${o.progress}"
                setTextColor(Color.parseColor(if (o.done) "#2a7a4a" else "#1a1a1a"))
                textSize = 15f
            })
            box.addView(c)
        }

        setContentView(root)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

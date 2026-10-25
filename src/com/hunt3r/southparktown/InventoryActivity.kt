package com.hunt3r.southparktown

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// InventoryActivity — the player's pack (v0.1.5).
// Everything carried: collectibles found, trash-dive loot,
// litter, gifts received. Tap a person in town to give items.
// ============================================================
class InventoryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(AndroidSaveStore(this))
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

        box.addView(TextView(this).apply {
            text = "🎒 YOUR PACK"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 22f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(4))
        })
        box.addView(TextView(this).apply {
            text = "Tap a person in town, then 'Give a gift' — people remember gifts. " +
                "Even the junk. ESPECIALLY the junk."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 13f
            setPadding(0, 0, 0, dp(10))
        })

        val entries = state?.inventory?.entries?.filter { it.value > 0 }
            ?.sortedBy { itemById(it.key)?.tier ?: "" } ?: emptyList()
        if (state == null || entries.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "Empty. Search some trash, pick up litter, find the daily shinies — " +
                    "South Park provides."
                setTextColor(Color.parseColor(Paper.YELLOW))
                textSize = 15f
            })
        } else {
            for ((id, count) in entries) {
                val item = itemById(id) ?: continue
                val c = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    // v0.1.9b: cardstock panel (rounded + edged)
                    background = UiKit.paperCardBg(this@InventoryActivity)
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    val lp = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    lp.bottomMargin = dp(8)
                    layoutParams = lp
                }
                c.addView(TextView(this).apply {
                    text = "${item.icon} ${item.name}  ×$count"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 17f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                c.addView(TextView(this).apply {
                    text = "${tierLabel(item.tier)} — ${item.desc}"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                })
                box.addView(c)
            }
        }
        setContentView(root)
    }

    private fun tierLabel(tier: String): String = when (tier) {
        "junk" -> "🗑️ Junk (giftable — weirdos exist)"
        "gift" -> "🎁 Giftable"
        "useful" -> "🧰 Useful"
        "rare" -> "⭐ Rare find"
        "lore" -> "📖 Lore treasure"
        else -> tier
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

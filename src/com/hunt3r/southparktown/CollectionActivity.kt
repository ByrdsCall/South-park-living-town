package com.hunt3r.southparktown

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// CollectionActivity — found vs silhouette ("???") grid.
// Cozy: everything re-rolls daily, nothing is missable.
// ============================================================
class CollectionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(AndroidSaveStore(this))
        val state = prefs.loadGame()
        val found = state?.collected ?: emptySet()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        root.addView(TextView(this).apply {
            text = "✨ COLLECTION"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 24f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(4))
        })
        root.addView(TextView(this).apply {
            text = "${found.size}/${COLLECTIBLES.size} found — 5 new ones spawn every day. Nothing's missable, dude."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 13f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        })

        val scroll = ScrollView(this)
        val scrollBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val grid = GridLayout(this).apply { columnCount = 3 }
        scrollBox.addView(grid)
        scroll.addView(scrollBox)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        for (c in COLLECTIBLES) {
            val has = c.id in found
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(10), dp(8), dp(10))
                // v0.1.9b: cells are cardstock tiles — parchment
                // when found, dark panel (still edged) when not
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            cell.addView(TextView(this).apply {
                text = if (has) c.icon else "???"
                textSize = 34f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#5a5a5a"))
            })
            cell.addView(TextView(this).apply {
                text = if (has) c.name else "???"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#5a5a5a"))
                textSize = 12f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
            })
            if (has) {
                cell.addView(TextView(this).apply {
                    text = c.flavor
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setPadding(0, dp(2), 0, 0)
                })
            }
            if (c.rare && has) {
                cell.addView(TextView(this).apply {
                    text = "★ RARE"
                    setTextColor(Color.parseColor(Paper.RED))
                    textSize = 10f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                    gravity = Gravity.CENTER
                })
            }
            val lp = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(5), dp(5), dp(5), dp(5))
            }
            grid.addView(cell, lp)
        }

        // ---- PHOTO FEATS (v0.2.0c): selfie challenges, pure
        // additions — they never gate the collection above;
        // they're just bragging rights with a camera. ----
        scrollBox.addView(TextView(this).apply {
            text = "📸 PHOTO FEATS"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 20f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(2))
        })
        scrollBox.addView(TextView(this).apply {
            text = "Bonus shots for your album (📸 Selfie in town). " +
                "Feats never lock anything — they're just fame."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        })
        val featsDone = state?.photoFeats ?: emptySet()
        for (f in Photos.PHOTO_FEATS) {
            val has = f.id in featsDone
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            row.addView(TextView(this).apply {
                text = "${if (has) "✅" else "⬜"} ${f.name}"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                textSize = 14f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = f.desc
                setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                textSize = 12f
            })
            scrollBox.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(4), dp(5), dp(4))
            })
        }
        if (state != null) {
            scrollBox.addView(TextView(this).apply {
                text = "👥 ${state.followers} followers · " +
                    "${state.photos.size} photos in the album (Journal 📓)"
                setTextColor(Color.parseColor(Paper.YELLOW))
                textSize = 12.5f
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, dp(4))
            })
        }

        // ---- ERRAND FEATS (v0.4.0b): the helpful titles —
        // pure counts off the lifetime errand tally; like the
        // photo feats they never lock anything, they're just
        // what the town calls you now. ----
        scrollBox.addView(TextView(this).apply {
            text = "📋 ERRAND FEATS"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 20f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(2))
        })
        scrollBox.addView(TextView(this).apply {
            val n = state?.errandsDoneCount ?: 0
            text = "Small work for townsfolk (Journal 📋 / the board at City Hall). " +
                "$n run so far" +
                (Errands.titleFor(n)?.let { " — the town calls you: $it" } ?: "") + "."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        })
        for (f in Errands.ERRAND_FEATS) {
            val has = state != null && Errands.featEarned(state, f)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            row.addView(TextView(this).apply {
                text = "${if (has) "✅" else "⬜"} ${f.name}"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                textSize = 14f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = f.desc
                setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                textSize = 12f
            })
            scrollBox.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(4), dp(5), dp(4))
            })
        }

        // ---- CHAIN FEATS (v0.6.0b): favor chains finished —
        // counted in chains, not links; the links already
        // counted toward the errand feats above. ----
        scrollBox.addView(TextView(this).apply {
            val n = state?.let { Errands.chainsDone(it) } ?: 0
            text = "🧵 Favor chains: $n finished — one favor " +
                "pulling the next, all the way home."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(8))
        })
        for (f in Errands.CHAIN_FEATS) {
            val has = state != null && Errands.chainFeatEarned(state, f)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            row.addView(TextView(this).apply {
                text = "${if (has) "✅" else "⬜"} ${f.name}"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                textSize = 14f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = f.desc
                setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                textSize = 12f
            })
            scrollBox.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(4), dp(5), dp(4))
            })
        }

        // ---- RESCUE FEATS (v0.8.0c): emergencies answered —
        // counted in rescues, like the chain feats above are
        // counted in chains; the rescues also counted toward
        // the errand feats in Errands.complete. ----
        scrollBox.addView(TextView(this).apply {
            val n = state?.let { Emergencies.rescuesDone(it) } ?: 0
            text = "🚨 Emergencies: $n answered in time — the " +
                "town's small disasters, beaten to the clock."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(8))
        })
        for (f in Emergencies.RESCUE_FEATS) {
            val has = state != null && Emergencies.rescueFeatEarned(state, f)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            row.addView(TextView(this).apply {
                text = "${if (has) "✅" else "⬜"} ${f.name}"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                textSize = 14f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = f.desc
                setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                textSize = 12f
            })
            scrollBox.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(4), dp(5), dp(4))
            })
        }

        // ---- FAIR FEATS (v0.9.0c): the county fair's pair —
        // counted in blue ribbons off Fair's own records,
        // same rendering contract as the rescue feats above. ----
        scrollBox.addView(TextView(this).apply {
            val n = state?.let { Fair.blueRibbons(it) } ?: 0
            text = "🎡 County fair: $n blue ribbon(s) taken — " +
                "the photo, the fishing, and the tank, one " +
                "week every August."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(8))
        })
        for (f in Fair.FAIR_FEATS) {
            val has = state != null && Fair.fairFeatEarned(state, f)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            row.addView(TextView(this).apply {
                text = "${if (has) "✅" else "⬜"} ${f.name}"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                textSize = 14f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = f.desc
                setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                textSize = 12f
            })
            scrollBox.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(4), dp(5), dp(4))
            })
        }

        // ---- WALK FEATS (v0.5.0a): the town on foot — pure
        // counts off the walk records, same rendering contract
        // as the errand feats above. ----
        scrollBox.addView(TextView(this).apply {
            text = "🗺 WALK FEATS"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 20f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(2))
        })
        scrollBox.addView(TextView(this).apply {
            val trips = state?.walkTrips ?: 0
            val cuts = state?.walkShortcuts?.size ?: 0
            text = "Trips walked and shortcuts learned (Journal 🗺). " +
                "$trips trips · $cuts shortcuts" +
                (if (state?.busPass == true) " · 🚌 bus pass in your wallet." else ".")
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        })
        for (f in Walk.WALK_FEATS) {
            val has = state != null && Walk.featEarned(state, f)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                    else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                        Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
            }
            row.addView(TextView(this).apply {
                text = "${if (has) "✅" else "⬜"} ${f.name}"
                setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                textSize = 14f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(this).apply {
                text = f.desc
                setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                textSize = 12f
            })
            scrollBox.addView(row, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(4), dp(5), dp(4))
            })
        }

        // ---- WARDROBE (v0.5.0b): your closet, in the
        // collection's own owned-vs-silhouette idiom — this
        // surface is the town's catalogue of things you can
        // own, which is exactly what a wardrobe is; the lines
        // themselves are pure (Wardrobe.collectionLines). ----
        scrollBox.addView(TextView(this).apply {
            text = "👕 WARDROBE"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 20f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(2))
        })
        if (state != null) {
            for ((i, line) in Wardrobe.collectionLines(state).withIndex()) {
                val ownedLine = line.startsWith("✅")
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                    background = when {
                        i == 0 -> null
                        ownedLine -> UiKit.paperCardBg(this@CollectionActivity)
                        else -> UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                            Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
                    }
                }
                row.addView(TextView(this).apply {
                    text = line
                    setTextColor(Color.parseColor(when {
                        i == 0 -> Paper.YELLOW
                        ownedLine -> "#1a1a1a"
                        else -> "#8a8a8a"
                    }))
                    textSize = if (i == 0) 12f else 12.5f
                    if (i == 0) gravity = Gravity.CENTER
                })
                scrollBox.addView(row, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(dp(5), dp(4), dp(5), dp(4))
                })
            }
        }
        // ---- POCKET PALS (v0.5.0c): the 24-card binder — the
        // collection's idiom applied to cards. Owned cards are
        // drawn from their specs by PocketPalView with dupe
        // counts; missing ones stand as silhouettes carrying
        // their hint (machine / trade / the two Look spots).
        // The lines (header + feats) are pure: PocketPals. ----
        scrollBox.addView(TextView(this).apply {
            text = "🃏 POCKET PALS"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 20f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(2))
        })
        if (state != null) {
            for ((i, line) in PocketPals.collectionLines(state).withIndex()) {
                val featLine = line.startsWith("✅") || line.startsWith("⬜")
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(10), dp(8), dp(10), dp(8))
                    background = when {
                        i == 0 -> null
                        featLine && line.startsWith("✅") ->
                            UiKit.paperCardBg(this@CollectionActivity)
                        featLine -> UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                            Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
                        else -> null
                    }
                }
                row.addView(TextView(this).apply {
                    text = line
                    setTextColor(Color.parseColor(when {
                        i == 0 -> Paper.YELLOW
                        featLine && line.startsWith("✅") -> "#1a1a1a"
                        featLine -> "#8a8a8a"
                        else -> Paper.YELLOW
                    }))
                    textSize = 12f
                    if (i == 0) gravity = Gravity.CENTER
                })
                scrollBox.addView(row, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(dp(5), dp(4), dp(5), dp(4))
                })
            }
            val palGrid = GridLayout(this).apply { columnCount = 3 }
            scrollBox.addView(palGrid)
            for (card in PocketPals.CARDS) {
                val n = PocketPals.count(state, card)
                val has = n > 0
                val cell = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(6), dp(8), dp(6), dp(8))
                    background = if (has) UiKit.paperCardBg(this@CollectionActivity)
                        else UiKit.panelBg(this@CollectionActivity, "#2a2a2a",
                            Chrome.CARD_RADIUS_DP, strokeHex = "#454545")
                }
                cell.addView(PocketPalView(this, card, has),
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(104)))
                cell.addView(TextView(this).apply {
                    text = if (has) card.name else "???"
                    setTextColor(Color.parseColor(if (has) "#1a1a1a" else "#8a8a8a"))
                    textSize = 11.5f
                    typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    gravity = Gravity.CENTER
                    setPadding(0, dp(4), 0, 0)
                })
                cell.addView(TextView(this).apply {
                    text = if (has)
                        "${PocketPals.schoolIcon(card.school)} ${card.school} · " +
                            (when (card.rarity) {
                                PocketPals.RARE -> "★ RARE"
                                PocketPals.UNCOMMON -> "◆ uncommon"
                                else -> "common"
                            }) + (if (n > 1) " · ×$n" else "")
                    else PocketPals.hintFor(card)
                    setTextColor(Color.parseColor(if (has) "#5a4a3a" else "#6a6a6a"))
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setPadding(0, dp(2), 0, 0)
                })
                if (has) {
                    cell.addView(TextView(this).apply {
                        text = card.flavor
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 9.5f
                        gravity = Gravity.CENTER
                        setPadding(0, dp(2), 0, 0)
                    })
                }
                val lp = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(dp(5), dp(5), dp(5), dp(5))
                }
                palGrid.addView(cell, lp)
            }
        }
        setContentView(root)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

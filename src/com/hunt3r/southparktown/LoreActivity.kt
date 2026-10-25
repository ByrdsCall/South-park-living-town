package com.hunt3r.southparktown

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// LoreActivity — browse + search Lucky's full South Park
// lorebook (3,322 entries) in-game: characters, locations,
// episodes, movies/specials and games. Default view surfaces
// the town's own cast and places first.
// ============================================================
class LoreActivity : Activity() {

    private lateinit var results: LinearLayout
    private lateinit var countLabel: TextView
    private lateinit var searchInput: EditText
    private var activeCat: String = "all"
    /** v0.1.9b: category chips, kept so the selected one can
     *  wear the games' tab treatment (red plaque, white text). */
    private val chipButtons = mutableListOf<Pair<String, Button>>()
    private val maxShow = 60
    /** v0.1.8 #9: the live town state, for live page cross-links. */
    private var liveState: GameState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureLoreLoaded(this)
        liveState = try { Prefs(AndroidSaveStore(this)).loadGame() } catch (e: Exception) { null }

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
            text = "📖 LOREBOOK"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 24f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setPadding(0, dp(2), 0, dp(2))
        })
        box.addView(TextView(this).apply {
            text = "Every character, place and episode in South Park history — " +
                "paraphrased from the wiki, packed by Lucky. ${LoreBook.size} entries."
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 13f
            setPadding(0, 0, 0, dp(8))
        })

        // ---- search row ----
        // v0.1.9b: dark panel field with an edge line (was a flat
        // dark fill) — padding added since the drawable replaces
        // the stock field chrome.
        val searchRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        searchInput = EditText(this).apply {
            hint = "Search the lorebook…"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#9a9a9a"))
            background = UiKit.panelBg(this@LoreActivity, "#2e2e2e",
                Chrome.CARD_RADIUS_DP, strokeHex = "#4d4d4d")
            setPadding(dp(10), dp(10), dp(10), dp(10))
            textSize = 15f
            setSingleLine(true)
        }
        searchRow.addView(searchInput, LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        searchRow.addView(Button(this).apply {
            text = "🔍"
            background = UiKit.buttonBg(this@LoreActivity, "#2e2e2e")
            setOnClickListener { render() }
        })
        box.addView(searchRow)

        // ---- category chips ----
        val chipRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val chips = listOf(
            "all" to "All", "char" to "👤", "loc" to "📍",
            "ep" to "📺", "spec" to "🎬", "game" to "🎮"
        )
        for ((cat, label) in chips) {
            val chipBtn = Button(this).apply {
                text = label
                textSize = 13f
                setOnClickListener { activeCat = cat; render() }
            }
            chipButtons.add(cat to chipBtn)
            chipRow.addView(chipBtn, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(1); marginEnd = dp(1)
            })
        }
        box.addView(chipRow)
        styleChips()

        countLabel = TextView(this).apply {
            setTextColor(Color.parseColor("#9a9a9a"))
            textSize = 12f
            setPadding(0, dp(8), 0, dp(4))
        }
        box.addView(countLabel)

        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        box.addView(results)

        setContentView(root)
        render()
    }

    private fun currentEntries(): List<LoreEntry> {
        val q = searchInput.text.toString().trim()
        var list = if (q.isEmpty()) {
            if (activeCat == "all") defaultEntries() else LoreBook.byCat(activeCat)
        } else {
            LoreBook.search(q, 400)
        }
        if (q.isNotEmpty() && activeCat != "all") {
            list = list.filter { it.cat == activeCat }
        }
        return list
    }

    /** Town-first default: the playable cast, then the game's 27 places. */
    private fun defaultEntries(): List<LoreEntry> {
        val out = mutableListOf<LoreEntry>()
        for (kid in KID_ORDER) LoreBook.charLore(kid)?.let { out.add(it) }
        for (locId in LOCATIONS.keys) LoreBook.locLore(locId)?.let { out.add(it) }
        return out
    }

    /** #9 THE LOREBOOK KNOWS THE TOWN: a live line for an entry —
     *  characters say where they are right now and what faction
     *  they're in; locations list who's inside right now. */
    private fun liveLine(e: LoreEntry): String? {
        val st = liveState ?: return null
        return try {
            when (e.cat) {
                "char" -> {
                    val agent = st.agents[e.name]
                    if (agent != null && !agent.isPlayer) {
                        val fid = factionOfCast(e.name)
                        "📍 Right now: ${LOCATIONS[agent.location]?.name ?: agent.location}" +
                            " — ${agent.activity}" +
                            (fid?.let { " · ⚔️ ${FACTION_BY_ID[it]?.name ?: ""}" } ?: "")
                    } else {
                        val week = (st.gameDay + (st.gameMonth - 1) * 31) / 7
                        val all = VisitorSystem.visitorsFor(st.todayKey, st.timeMin) +
                            VisitorSystem.residentsNow(st.timeMin, week,
                                isSleepHour(st.timeMin / 60), dayOfWeek(st), st.todayKey)
                        val g = all.firstOrNull { it.persona.loreName == e.name }
                        g?.let {
                            "📍 In town now: ${LOCATIONS[it.locId]?.name ?: it.locId}" +
                                (if (it.persona.resident) " (lives here)" else " (visiting)")
                        }
                    }
                }
                "loc" -> {
                    val locId = LORE_LOC_ALIASES.entries.firstOrNull { it.value == e.name }?.key
                        ?: LOCATIONS.entries.firstOrNull { it.value.name == e.name }?.key
                    locId?.let { id ->
                        val week = (st.gameDay + (st.gameMonth - 1) * 31) / 7
                        val cast = st.agents.values
                            .filter { !it.isPlayer && it.location == id && it.waypoints.isEmpty() }
                            .map { it.name }
                        val gen = (VisitorSystem.visitorsFor(st.todayKey, st.timeMin) +
                            VisitorSystem.residentsNow(st.timeMin, week,
                                isSleepHour(st.timeMin / 60), dayOfWeek(st), st.todayKey))
                            .filter { it.locId == id }.map { it.persona.loreName }
                        val who = (cast + gen).distinct()
                        if (who.isEmpty()) "👥 Nobody here right now."
                        else "👥 Here now: ${who.joinToString(", ")}"
                    }
                }
                else -> null
            }
        } catch (ex: Exception) { null }
    }

    /** v0.1.9b: selected category = red plaque chip with white
     *  text (the games' tab treatment); the rest stay dark panel
     *  chips with yellow text. Selection state itself is
     *  unchanged (activeCat, set by the same click handlers). */
    private fun styleChips() {
        for ((cat, btn) in chipButtons) {
            val selected = cat == activeCat
            btn.background = UiKit.buttonBg(this,
                if (selected) Paper.RED else "#2e2e2e")
            btn.setTextColor(Color.parseColor(
                if (selected) "#ffffff" else Paper.YELLOW))
        }
    }

    private fun render() {
        styleChips()
        results.removeAllViews()
        if (!LoreBook.loaded) {
            countLabel.text = "Lorebook didn't load, dude. Try again later."
            return
        }
        val list = currentEntries()
        val shown = list.take(maxShow)
        countLabel.text = when {
            list.isEmpty() -> "Nothing in the lorebook matches. Weird."
            list.size > shown.size ->
                "Showing ${shown.size} of ${list.size} — keep typing to narrow it down."
            else -> "${list.size} ${if (list.size == 1) "entry" else "entries"}"
        }
        for (e in shown) {
            val c = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                // v0.1.9b: cardstock panel (rounded + edged)
                background = UiKit.paperCardBg(this@LoreActivity)
                setPadding(dp(12), dp(10), dp(12), dp(10))
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = dp(8)
                layoutParams = lp
            }
            c.addView(TextView(this).apply {
                text = e.name
                setTextColor(Color.parseColor("#1a1a1a"))
                textSize = 16f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            })
            c.addView(TextView(this).apply {
                text = LoreBook.CAT_LABELS[e.cat] ?: e.cat
                setTextColor(Color.parseColor("#8a6a2a"))
                textSize = 11f
            })
            c.addView(TextView(this).apply {
                text = e.text
                setTextColor(Color.parseColor("#1a1a1a"))
                textSize = 14f
                setPadding(0, dp(4), 0, 0)
            })
            liveLine(e)?.let { line ->
                c.addView(TextView(this).apply {
                    text = line
                    setTextColor(Color.parseColor("#2a5a8a"))
                    textSize = 13f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(4), 0, 0)
                })
            }
            results.addView(c)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

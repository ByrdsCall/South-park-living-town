package com.hunt3r.southparktown

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

// ============================================================
// FictionActivity — v0.16.0e PASS 2: the play menu for
// the costume system. The kids' game is started and
// ended HERE (the phone's 🎭 Play tile); everything it
// does to the town is derived by Fiction.dress from
// (fiction, start tick, tick) — this screen only writes
// the four save keys Fiction owns (id / start / end /
// residue) and reads the rest back as pure functions.
// ============================================================
class FictionActivity : Activity() {

    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val prefs = Prefs(this)
        val state = prefs.loadGame() ?: GameState()
        val def = Fiction.activeDef(state)
        val phase = Fiction.phaseOf(state)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        root.addView(TextView(this).apply {
            text = "🎭 PLAY A GAME"
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 24f
            typeface = android.graphics.Typeface.create(
                "casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(4))
        })

        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(box)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        fun line(txt: String, color: String = Paper.YELLOW,
                 size: Float = 14f) {
            box.addView(TextView(this).apply {
                text = txt
                setTextColor(Color.parseColor(color))
                textSize = size
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.NORMAL)
                setPadding(0, dp(3), 0, dp(3))
            })
        }

        fun button(label: String, action: () -> Unit) {
            box.addView(TextView(this).apply {
                text = label
                setTextColor(Color.parseColor(Paper.CHARCOAL))
                setBackgroundColor(Color.parseColor(Paper.YELLOW))
                textSize = 16f
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                setOnClickListener { action() }
            })
            box.addView(TextView(this).apply {
                text = ""
                textSize = 6f
            })
        }

        if (def == null || phase == Fiction.Phase.REAL ||
            phase == Fiction.Phase.RESIDUE) {
            if (phase == Fiction.Phase.RESIDUE && def != null) {
                line("The game is over. The town is itself " +
                    "again — almost. Left behind:")
                for (r in Fiction.residueOf(state)) {
                    val (pid, prop) = r.split(":")
                    line("  • a $prop, still standing at " +
                        "$pid (${Fiction.MADE_FROM[prop] ?: "junk"} " +
                        "underneath, obviously)", Paper.PARCHMENT, 13f)
                }
                line("")
            }
            line("Start a game and the town leans in: " +
                "cardboard goes up, towels become cloaks, " +
                "and City Hall becomes a keep. It plays " +
                "out over the next two days of game time — " +
                "or end it early, right here.")
            line("")
            for (f in Fiction.ALL) {
                line("⚔️ ${f.title}", Paper.YELLOW, 16f)
                line("${f.factionA} vs ${f.factionB}. " +
                    "You are ${f.playerTitle}.", Paper.PARCHMENT)
                line("Ranks: ${f.rankNames.joinToString(", ")}")
                line("Made from: " + f.propKit.joinToString(", ") {
                    "$it (${Fiction.MADE_FROM[it] ?: "junk"})" },
                    Paper.PARCHMENT, 13f)
                button("▶ Start: ${f.title}") {
                    Fiction.start(state, f)
                    prefs.saveGame(state)
                    Toast.makeText(this,
                        "🎭 The game begins — watch the town " +
                            "get dressed.",
                        Toast.LENGTH_LONG).show()
                    render()
                }
            }
        } else {
            line("⚔️ ${def.title} — ${phase.name}",
                Paper.YELLOW, 17f)
            line("${def.factionA} vs ${def.factionB}. " +
                "You are ${def.playerTitle}.")
            val dressed = Fiction.dressNow(state)
            line("Parcels dressed: ${dressed.size} of " +
                "${Fiction.parcels().size}")
            val claims = dressed.values.mapNotNull { it.claimName }
            if (claims.isNotEmpty())
                line("Claimed: ${claims.joinToString(", ")}",
                    Paper.PARCHMENT)
            val fulls = CharacterSystem.CAST_ADULTS.count {
                Fiction.costumeOf(def.id, it,
                    isKid = false) == Fiction.Costume.FULL }
            line("Adults all-in (cape and everything): $fulls — " +
                "the rest are towel-cloak at best.", Paper.PARCHMENT)
            line("")
            when (phase) {
                Fiction.Phase.DRESSING -> line(
                    "The kids are out taping cardboard to " +
                        "the town right now. Walk around and " +
                        "watch it go up.")
                Fiction.Phase.LIVE -> line(
                    "The game is LIVE. The Keep (City Hall) " +
                        "has a dungeon in its basement — the " +
                        "cardboard door inside leads down.")
                Fiction.Phase.ESCALATING -> line(
                    "It is getting serious. Fresh supplies " +
                        "arrived; the far side of town is " +
                        "giving in.")
                Fiction.Phase.COLLAPSING -> line(
                    "The game is coming down — grown-ups " +
                        "are taking the cardboard off.")
                else -> {}
            }
            line("")
            button("⏹ End the game (collapse)") {
                Fiction.requestEnd(state)
                prefs.saveGame(state)
                Toast.makeText(this,
                    "🎭 The game winds down — the town " +
                        "undresses itself.",
                    Toast.LENGTH_LONG).show()
                render()
            }
        }
        setContentView(root)
    }
}

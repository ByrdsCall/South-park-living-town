package com.hunt3r.southparktown

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// PhoneActivity — v0.5.0e: the New Kid's phone. A HUB, not a
// rewrite: the tiles open the surfaces that already exist
// (Journal + its album/games sections, Settings, and — via a
// result route back to GameActivity — the 🗺 Go Somewhere
// picker). Messages is the one new room: the ambient text
// threads Phone.kt generates from your day.
//
// State discipline matches the Journal's: GameActivity saves
// before launching us; we load a fresh copy, mutate it
// (read marks, quick replies), and save it back.
// ============================================================
class PhoneActivity : Activity() {

    private lateinit var prefs: Prefs
    private var state: GameState? = null
    private lateinit var box: LinearLayout
    private var screen: String = "home"       // home | threads | thread
    private var threadName: String = ""
    private var replyNote: String = ""        // one-shot line under the reply row

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureLoreLoaded(this)
        prefs = Prefs(AndroidSaveStore(this))
        prefs.loadNpcMemory()
        state = prefs.loadGame()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
        }
        val scroll = ScrollView(this)
        box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        scroll.addView(box)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        setContentView(root)
        render()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    // ---------------- shared bits ----------------

    private fun title(t: String) {
        box.addView(TextView(this).apply {
            text = t
            setTextColor(Color.parseColor(Paper.RED))
            textSize = 22f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setPadding(0, dp(4), 0, dp(8))
        })
    }

    private fun card(): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = UiKit.paperCardBg(this@PhoneActivity)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(8)
            layoutParams = lp
        }
        box.addView(c)
        return c
    }

    private fun line(parent: LinearLayout, t: String, big: Boolean = false,
                     color: String = "#5a4a3a", size: Float = 13f) {
        parent.addView(TextView(this).apply {
            text = t
            setTextColor(Color.parseColor(if (big) "#1a1a1a" else color))
            textSize = if (big) 15.5f else size
            if (big) typeface = android.graphics.Typeface.create(
                "casual", android.graphics.Typeface.BOLD)
            setPadding(0, dp(2), 0, 0)
        })
    }

    private fun backButton(label: String, to: String) {
        val b = Button(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.parseColor(Chrome.INK))
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            background = UiKit.buttonBg(this@PhoneActivity, Paper.PARCHMENT)
            setOnClickListener {
                screen = to
                render()
            }
        }
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(8)
        box.addView(b, lp)
    }

    private fun dateLabel(dayKey: String): String {
        if (dayKey.length != 8) return ""
        val m = dayKey.substring(4, 6).toIntOrNull() ?: return ""
        val d = dayKey.substring(6, 8).toIntOrNull() ?: return ""
        return "$m/$d"
    }

    // ---------------- screens ----------------

    private fun render() {
        box.removeAllViews()
        val st = state
        if (st == null) {
            title("📱 PHONE")
            val c = card()
            line(c, "No town yet — go make a kid first.", big = true)
            return
        }
        when (screen) {
            "threads" -> renderThreads(st)
            "thread" -> renderThread(st, threadName)
            else -> renderHome(st)
        }
    }

    private fun renderHome(st: GameState) {
        title("📱 PHONE")
        // ---- status strip ----
        val hub = Phone.hubStatus(st)
        val strip = card()
        line(strip, "🕐 ${hub.time} · ${hub.weatherIcon} ${hub.weatherLabel} · " +
            "🪙 ${hub.coins}", big = true)
        line(strip, if (hub.unread > 0) "💬 ${hub.unread} unread text" +
            (if (hub.unread == 1) "" else "s") + " — the town has opinions"
            else "💬 all caught up — for now")
        // ---- tiles (two per row, from Phone.APPS) ----
        val rows = Phone.APPS.chunked(2)
        for (rowApps in rows) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = dp(8)
                layoutParams = lp
            }
            for (app in rowApps) {
                val label = if (app.id == "messages" && hub.unread > 0)
                    "${app.label} (${hub.unread})" else app.label
                val b = Button(this).apply {
                    text = label
                    textSize = 16f
                    setTextColor(Color.parseColor(Chrome.INK))
                    typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    background = UiKit.buttonBg(this@PhoneActivity, Paper.PARCHMENT)
                    setPadding(dp(6), dp(14), dp(6), dp(14))
                    setOnClickListener { openApp(app.id) }
                }
                row.addView(b, LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(3); marginEnd = dp(3)
                })
            }
            if (rowApps.size == 1) {
                row.addView(TextView(this), LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            box.addView(row)
        }
        val foot = card()
        line(foot, "Your phone. It buzzes when the town has something to say — " +
            "usually in the morning, always about you.")
    }

    private fun openApp(id: String) {
        when (id) {
            "journal" -> startActivity(Intent(this, JournalActivity::class.java))
            "album" -> startActivity(Intent(this, JournalActivity::class.java)
                .putExtra("section", "album"))
            "games" -> startActivity(Intent(this, JournalActivity::class.java)
                .putExtra("section", "games"))
            "settings" -> startActivity(Intent(this, SettingsActivity::class.java))
            "play" -> startActivity(Intent(this, FictionActivity::class.java))
            "messages" -> { screen = "threads"; render() }
            "map" -> {
                // the picker lives in GameActivity — hand the
                // route back and close the phone.
                setResult(RESULT_OK, Intent().putExtra("phone_route", "map"))
                finish()
            }
        }
    }

    private fun renderThreads(st: GameState) {
        backButton("‹ 📱 Phone", "home")
        title("💬 MESSAGES")
        val threads = Phone.threads(st)
        if (threads.isEmpty()) {
            val c = card()
            line(c, "No texts yet.", big = true)
            line(c, "The town is quiet. Suspiciously quiet. Live your day — " +
                "do a favor, take a photo, pull something shiny — and check " +
                "back in the morning. They'll find you.")
            return
        }
        for (t in threads) {
            val c = card()
            val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            head.addView(TextView(this).apply {
                text = if (GroupChats.isGroup(t.name))
                    GroupChats.titleFor(st, t.name) else t.name
                setTextColor(Color.parseColor("#1a1a1a"))
                textSize = 16f
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            head.addView(TextView(this).apply {
                text = if (t.unread > 0) "● ${t.unread} new" else dateLabel(t.last.dayKey)
                setTextColor(Color.parseColor(if (t.unread > 0) Paper.RED else "#8a7a5c"))
                textSize = 12.5f
                gravity = Gravity.END
            })
            c.addView(head)
            val status = if (GroupChats.isGroup(t.name))
                GroupChats.memberLine(st, t.name)
            else Phone.statusLine(st, t.name)
            if (status.isNotEmpty()) line(c, status, size = 12f, color = "#8a7a5c")
            line(c, (when {
                t.last.sender == "you" -> "You: "
                GroupChats.isGroup(t.name) -> "${t.last.sender}: "
                else -> ""
            }) + t.last.text, size = 13f)
            c.setOnClickListener {
                threadName = t.name
                Phone.markThreadRead(st, t.name)
                prefs.saveGame(st)
                screen = "thread"
                render()
            }
            box.addView(c)
        }
    }

    private fun renderThread(st: GameState, name: String) {
        backButton("‹ 💬 Messages", "threads")
        val group = GroupChats.isGroup(name)
        title(if (group) GroupChats.titleFor(st, name) else name)
        if (group) {
            // a group thread shows its members, and whether
            // the room is still live or has gone quiet
            val c = card()
            line(c, "with " + GroupChats.memberLine(st, name),
                size = 12.5f, color = "#8a7a5c")
            line(c, if (GroupChats.live(st, name))
                "the chat is live — your replies can draw a reaction " +
                    "(reactions only — a group chat moves no friendship)"
            else "this chat has gone quiet — the record stays, " +
                "but the moment has passed",
                size = 12.5f, color = "#8a7a5c")
        } else {
            val status = Phone.statusLine(st, name)
            if (status.isNotEmpty()) {
                val c = card()
                line(c, status, size = 12.5f, color = "#8a7a5c")
            }
        }
        val texts = Phone.threadTexts(st, name)
        if (texts.isEmpty()) {
            val c = card()
            line(c, "Nothing here yet.")
        }
        for (t in texts) {
            val mine = t.sender == "you"
            val c = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = UiKit.paperCardBg(this@PhoneActivity,
                    if (mine) "#dfe8c8" else Paper.PARCHMENT)
                setPadding(dp(12), dp(8), dp(12), dp(8))
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = dp(6)
                if (mine) lp.marginStart = dp(40) else lp.marginEnd = dp(40)
                layoutParams = lp
            }
            line(c, t.text, big = false, color = "#1a1a1a", size = 14f)
            // in a group the bubble names its SPEAKER; solo,
            // the thread's name and the speaker are one
            line(c, (if (mine) "You" else t.sender) + " · " + dateLabel(t.dayKey),
                size = 11f, color = "#8a7a5c")
            box.addView(c)
        }
        // ---- quick replies ----
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topMargin = dp(6)
            layoutParams = lp
        }
        for (emoji in Phone.QUICK_REPLIES) {
            val b = Button(this).apply {
                text = emoji
                textSize = 18f
                background = UiKit.buttonBg(this@PhoneActivity, Paper.PARCHMENT)
                setOnClickListener {
                    if (group) {
                        val r = GroupChats.quickReply(st, name, emoji)
                        prefs.saveGame(st)
                        prefs.saveNpcMemory()
                        replyNote = if (r.ack != null) "they felt that one"
                            else "sent ✓"
                    } else {
                        val r = Phone.quickReply(st, name, emoji)
                        prefs.saveGame(st)
                        prefs.saveNpcMemory()
                        replyNote = if (r.bonusApplied) "💛 +1 — they felt that one"
                            else "sent ✓"
                    }
                    render()
                }
            }
            row.addView(b, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(3); marginEnd = dp(3)
            })
        }
        box.addView(row)
        if (replyNote.isNotEmpty()) {
            val c = card()
            line(c, replyNote, size = 12.5f, color = "#8a7a5c")
            replyNote = ""
        }
    }

    override fun onBackPressed() {
        when (screen) {
            "thread" -> { screen = "threads"; render() }
            "threads" -> { screen = "home"; render() }
            else -> super.onBackPressed()
        }
    }
}

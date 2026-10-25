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
// JournalActivity — Who (kids, whereabouts, friendship hearts)
// + Journal (memories, recent events).
// ============================================================
class JournalActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureLoreLoaded(this)
        val prefs = Prefs(AndroidSaveStore(this))
        prefs.loadNpcMemory()
        val state = prefs.loadGame()
        val engine = Engine()

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

        var albumAnchor: TextView? = null
        var gamesAnchor: TextView? = null
        var penpalsAnchor: TextView? = null
        fun sectionTitle(t: String): TextView {
            val tv = TextView(this).apply {
                text = t
                setTextColor(Color.parseColor(Paper.RED))
                textSize = 22f
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                setPadding(0, dp(10), 0, dp(6))
            }
            box.addView(tv)
            return tv
        }

        fun card(): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                // v0.1.9b: cardstock panel — rounded parchment with
                // a darker edge (was a flat parchment fill)
                background = UiKit.paperCardBg(this@JournalActivity)
                setPadding(dp(12), dp(10), dp(12), dp(10))
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = dp(8)
                layoutParams = lp
            }
        }

        // ---- TODAY: active shenanigans event ----
        if (state != null) {
            sectionTitle("🎪 TODAY IN SOUTH PARK")
            val def = Quests.activeDef(state)
            val c = card()
            if (def != null) {
                val stage = Shenanigans.stageOf(def, state.timeMin)
                c.addView(TextView(this).apply {
                    text = "${def.theme.icon} ${def.title}"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 17f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                c.addView(TextView(this).apply {
                    text = "▶ ${stage.title}\n${stage.journalNote}\n🎯 ${stage.objective}"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                })
            } else {
                c.addView(TextView(this).apply {
                    text = "No shenanigans today. The boys are quiet. TOO quiet. " +
                        "(Events roll weekly; at least one is guaranteed every month.)"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                })
            }
            box.addView(c)
        }

        // ---- EPISODES (v0.1.9c): every story the town has aired,
        // newest first; tap one for its credits page ----
        if (state != null && state.episodes.isNotEmpty()) {
            sectionTitle("📺 EPISODES")
            for (ep in state.episodes.reversed()) {
                val c = card()
                c.addView(TextView(this).apply {
                    text = (if (ep.finished) "🎬 " else "▶ ") + "\"${ep.title}\""
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 16f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                c.addView(TextView(this).apply {
                    text = "${ep.eventTitle} — ${ep.dateLabel}" +
                        (if (ep.finished) " · ✋ your hand: ${ep.participation}/10"
                        else " · NOW PLAYING") +
                        " · tap for the episode page"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 12.5f
                })
                val stageNow = if (ep.finished) -1 else Quests.currentStage(state)
                c.setOnClickListener {
                    UiKit.showEpisodeDialog(this@JournalActivity,
                        Episodes.page(ep, stageNow), "Close") {}.show()
                }
                box.addView(c)
            }
        }

        // ---- HOLIDAYS (v0.4.0d): every holiday's own beat —
        // today's progress while its window is open, and the
        // permanent marks of the ones you've played, by year.
        // Assembly is pure (HolidayBeats.pageLines); renders only.
        if (state != null) {
            sectionTitle("🎉 HOLIDAYS")
            val c = card()
            val lines = HolidayBeats.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- SEASON FINALES (v0.5.0h): each season's last
        // week, its live progress, and the standing marks —
        // the calls that reshaped the town, by year and hand.
        // Assembly is pure (Finales.pageLines); this renders.
        if (state != null) {
            sectionTitle("❄ SEASON FINALES")
            val c = card()
            val lines = Finales.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- FAMILY EVENINGS (v0.6.0d): the town's tables —
        // tonight's invitation or hosting, the dinners behind
        // you at theirs and at yours, and who's still to
        // visit or host. Assembly is pure (Dinners.pageLines);
        // this renders.
        if (state != null) {
            sectionTitle("🍽 FAMILY EVENINGS")
            val c = card()
            val lines = Dinners.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- BIRTHDAYS (v0.12.0c): the town's dates, on
        // the record — today and tonight stated plainly
        // (a party standing, or the yield and why), what's
        // coming up, and the parties behind you. Assembly
        // is pure (Birthdays.journalLines); this renders.
        if (state != null) {
            sectionTitle("🎂 BIRTHDAYS")
            val c = card()
            val lines = Birthdays.journalLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- YOUR BIRTHDAY (v0.14.0d): your own date, on
        // the record — the derivation in plain words,
        // today's standing or the countdown, the card
        // kept verbatim, who wished, and the parties
        // behind you. Assembly is pure
        // (PlayerBirthday.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎂 YOUR BIRTHDAY")
            val c = card()
            val lines = PlayerBirthday.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- WORK (v0.6.0e): the job, if you've got one —
        // its schedule, your streak and what it's worth, the
        // week's tab and when payday lands; or the three
        // counters in town that hire. Assembly is pure
        // (Jobs.pageLines); this renders.
        if (state != null) {
            sectionTitle("💼 WORK")
            val c = card()
            val lines = Jobs.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- APPRENTICESHIPS (v0.15.0h): the second tier
        // of the town's work — the five counters and their
        // keepers, the counts that earn an offer, the
        // chains and their stages, the skills that deepen
        // and the marks that get signed. Assembly is pure
        // (Apprenticeships.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎓 APPRENTICESHIPS")
            val c = card()
            val lines = Apprenticeships.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- TOWN VOTES (v0.6.0f): this week's question at
        // City Hall, your ballot, the standings the town is
        // living with, and the counts it remembers. Assembly
        // is pure (Votes.pageLines); this renders.
        if (state != null) {
            sectionTitle("🗳 TOWN VOTES")
            val c = card()
            val lines = Votes.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- TOWN SECRETS (v0.7.0b): the half-hidden page —
        // what's found is written up; what isn't is a riddle
        // and a count, nothing more. Assembly is pure
        // (Secrets.pageLines); this renders.
        if (state != null) {
            sectionTitle("🔎 TOWN SECRETS")
            val c = card()
            val lines = Secrets.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MOVIE NIGHTS (v0.7.0c): the Bijou's page —
        // tonight's showing, your houses, the features you've
        // seen with their stars. Assembly is pure
        // (MovieNights.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎬 MOVIE NIGHTS")
            val c = card()
            val lines = MovieNights.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- SCHOOL (v0.7.0d): the Elementary's page —
        // lessons sat with their topics, the bee record,
        // recess beats refereed. No grades: attendance is
        // texture. Assembly is pure (School.pageLines);
        // this renders.
        if (state != null) {
            sectionTitle("🎓 SCHOOL")
            val c = card()
            val lines = School.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- SCHOOL PLAY (v0.10.0c): the year's
        // production — the cast, your part, your rehearsal
        // record, the night's verdict and the school
        // paper's review, quoted. Past productions reduce
        // to history lines at the bottom of the card.
        // Assembly is pure (Play.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎭 SCHOOL PLAY")
            val c = card()
            val lines = Play.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- THE DANCE (v0.13.0b): the year's February
        // night — the poster, your plan (a date, or stag,
        // stated as the whole plan it is), the ask's honest
        // state, and the wrapped night read back from the
        // record. Assembly is pure (Dance.pageLines); this
        // renders.
        if (state != null) {
            sectionTitle("💃 THE DANCE")
            val c = card()
            val lines = Dance.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- TALENT SHOW (v0.13.0c): the year's April
        // night at the Bijou — the posted bill while the
        // cycle is live, the night's table read back from
        // the record after. Assembly is pure
        // (Talent.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎤 TALENT SHOW")
            val c = card()
            val lines = Talent.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- THE PARADE (v0.13.0d): the year's May
        // Saturday, rendered whole — this year's derived
        // procession order while the cycle is live, your
        // part read back from the record after. Assembly
        // is pure (Parade.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎺 THE PARADE")
            val c = card()
            val lines = Parade.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- FIELD TRIPS (v0.14.0b): the year's three
        // Fridays out of the building — the posted trip
        // while it's ahead, the live day while it's live,
        // the wrapped trips read back from their records.
        // Assembly is pure (FieldTrip.pageLines); this
        // renders.
        if (state != null) {
            sectionTitle("🚌 FIELD TRIPS")
            val c = card()
            val lines = FieldTrip.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- FILM FESTIVAL (v0.14.0c): the Bijou's
        // December week — the live year's program while
        // it's ahead or live, the wrapped years read back
        // from their records with their winners. Assembly
        // is pure (Festival.pageLines); this renders.
        if (state != null) {
            sectionTitle("🎬 FILM FESTIVAL")
            val c = card()
            val lines = Festival.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- GALLERY SHOW (v0.15.0f): the Community
        // Hall's seasonal exhibition — the live show's
        // theme and wall, the champion, and the wrapped
        // shows on the record. Assembly is pure
        // (Gallery.pageLines); this renders.
        if (state != null) {
            sectionTitle("🖼 GALLERY SHOW")
            val c = card()
            val lines = Gallery.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }


        // ---- PEN PALS (v0.13.0e): the correspondence, kept
        // on the Journal's own paper — the pairing, the
        // standing, the letters (latest 16 whole, older
        // folded to a line), and the visit's record.
        // PenPals.pageLines derives the page from the
        // archive on every read; the one line this page
        // never carries is a headline — the Gazette stays
        // out of private mail, and so does the phone.
        if (state != null) {
            penpalsAnchor = sectionTitle("📬 PEN PALS")
            val c = card()
            val lines = PenPals.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY (v0.2.0f): the clue board for the season
        // arc — chapters aired, clues found, the theory the town
        // currently believes, and the case's stamp once it closes.
        // Assembly is pure (Mystery.boardLines); this only renders.
        if (state != null && Mystery.boardVisible(state)) {
            sectionTitle("🔍 MYSTERY")
            val c = card()
            val lines = Mystery.boardLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 2 (v0.3.0f): "The Locked Booth" —
        // its own section on the same page, below the pond
        // case's board (which stays readable as town history).
        // Assembly is pure (Mystery2.boardLines2); this renders.
        if (state != null && Mystery2.boardVisible2(state)) {
            if (!Mystery.boardVisible(state)) sectionTitle("🔍 MYSTERY")
            val c = card()
            val lines = Mystery2.boardLines2(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 3 (v0.4.0f): "What's Buried" —
        // its own section on the same page, below the booth
        // case's board (both earlier boards stay readable —
        // town history). Assembly is pure (Mystery3.boardLines3);
        // this renders.
        if (state != null && Mystery3.boardVisible3(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery3.boardLines3(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 4 (v0.6.0c): "The Night Gardener" —
        // its own section on the same page, below the buried
        // case's board (all three earlier boards stay readable —
        // town history). Assembly is pure (Mystery4.boardLines4);
        // this renders.
        if (state != null && Mystery4.boardVisible4(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery4.boardLines4(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 5 (v0.7.0f): "The Two Confessions" —
        // its own section on the same page, below the gardener
        // case's board (all four earlier boards stay readable —
        // town history). Assembly is pure (Mystery5.boardLines5);
        // this renders.
        if (state != null && Mystery5.boardVisible5(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery5.boardLines5(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 6 (v0.8.0f): "The Court of Public
        // Opinion" — its own section on the same page, below the
        // confession case's board (all five earlier boards stay
        // readable — town history). Assembly is pure
        // (Mystery6.boardLines6 — two columns, strictly separate);
        // this renders.
        if (state != null && Mystery6.boardVisible6(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery6.boardLines6(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 7 (v0.9.0f): "The Case Told
        // Backwards" — its own section on the same page, below
        // the opinion case's board (all six earlier boards stay
        // readable — town history). Assembly is pure
        // (Mystery7.boardLines7 — a timeline in true order,
        // filled in backwards); this renders.
        if (state != null && Mystery7.boardVisible7(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery7.boardLines7(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 8 (v0.10.0f): "Everybody Was
        // Somewhere" — its own section on the same page, below
        // the backwards case's board (all seven earlier boards
        // stay readable — town history). Assembly is pure
        // (Mystery8.boardLines8 — an alibi graph, re-counted
        // on every read); this renders.
        if (state != null && Mystery8.boardVisible8(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state) &&
                !Mystery7.boardVisible7(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery8.boardLines8(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 9 (v0.11.0f): "One More Than
        // Counted" — its own section on the same page, below
        // the alibi case's board (all eight earlier boards
        // stay readable — town history). Assembly is pure
        // (Mystery9.boardLines9 — a ledger of count-sites,
        // re-read on every read); this renders.
        if (state != null && Mystery9.boardVisible9(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state) &&
                !Mystery7.boardVisible7(state) && !Mystery8.boardVisible8(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery9.boardLines9(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 10 (v0.12.0f): "The Photograph
        // Nobody Took" — its own section on the same page,
        // below the audit case's board (all nine earlier
        // boards stay readable — town history). Assembly is
        // pure (Mystery10.boardLines10 — a comparison of
        // frames, re-read on every read); this renders.
        if (state != null && Mystery10.boardVisible10(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state) &&
                !Mystery7.boardVisible7(state) && !Mystery8.boardVisible8(state) &&
                !Mystery9.boardVisible9(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery10.boardLines10(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 11 (v0.13.0f): "Signed in
        // Another's Voice" — its own section on the same
        // page, below the photo case's board (all ten
        // earlier boards stay readable — town history).
        // Assembly is pure (Mystery11.boardLines11 — a
        // ledger of voices, re-read on every read); this
        // renders.
        if (state != null && Mystery11.boardVisible11(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state) &&
                !Mystery7.boardVisible7(state) && !Mystery8.boardVisible8(state) &&
                !Mystery9.boardVisible9(state) && !Mystery10.boardVisible10(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery11.boardLines11(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 12 (v0.14.0f): "Back to the
        // Wrong Owner" — its own section on the same page,
        // below the voice case's board (all eleven earlier
        // boards stay readable — town history). Assembly is
        // pure (Mystery12.boardLines12 — a ledger of
        // returns, re-read on every read); this renders.
        if (state != null && Mystery12.boardVisible12(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state) &&
                !Mystery7.boardVisible7(state) && !Mystery8.boardVisible8(state) &&
                !Mystery9.boardVisible9(state) && !Mystery10.boardVisible10(state) &&
                !Mystery11.boardVisible11(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery12.boardLines12(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- MYSTERY, CASE 13 (v0.15.0j): "Not What the
        // Frame Says" — its own section on the same page,
        // below the returns case's board (all twelve earlier
        // boards stay readable — town history). Assembly is
        // pure (Mystery13.boardLines13 — an audit of frames,
        // re-read on every read); this renders.
        if (state != null && Mystery13.boardVisible13(state)) {
            if (!Mystery.boardVisible(state) && !Mystery2.boardVisible2(state) &&
                !Mystery3.boardVisible3(state) && !Mystery4.boardVisible4(state) &&
                !Mystery5.boardVisible5(state) && !Mystery6.boardVisible6(state) &&
                !Mystery7.boardVisible7(state) && !Mystery8.boardVisible8(state) &&
                !Mystery9.boardVisible9(state) && !Mystery10.boardVisible10(state) &&
                !Mystery11.boardVisible11(state) && !Mystery12.boardVisible12(state)) {
                sectionTitle("🔍 MYSTERY")
            }
            val c = card()
            val lines = Mystery13.boardLines13(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i <= 1) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 16f else 13f
                    if (i <= 1) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- STORY ARCS (v0.3.0c): the relationship storylines
        // — stage progress while they run, their outcome in one
        // line once they've landed. Assembly is pure
        // (StoryArcs.pageLines); this only renders. ----
        if (state != null && StoryArcs.pageVisible(state)) {
            sectionTitle("📖 STORY ARCS")
            val c = card()
            val lines = StoryArcs.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- ERRANDS (v0.4.0b): the town's small work — your
        // plate, today's board at City Hall, lately's runs, and
        // the title the town calls you. Assembly is pure
        // (Errands.pageLines); tapping the card manages the
        // plate (drop one — the board at City Hall does too). ----
        if (state != null) {
            sectionTitle("📋 ERRANDS")
            val c = card()
            val lines = Errands.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            val stJ = state
            if (Errands.active(stJ).isNotEmpty()) {
                c.setOnClickListener {
                    val act = Errands.active(stJ)
                    val labels = act.map { "📋 ${Errands.objective(it)}" }
                        .toTypedArray()
                    UiKit.showItemsDialog(this@JournalActivity,
                        "Drop an errand?", labels) { which ->
                        val e = act[which]
                        UiKit.showMessageDialog(this@JournalActivity,
                            "Drop ${Errands.displayAsker(e)}'s errand?",
                            message = "“${e.text}”\n\nDropping costs " +
                                "nothing but the look on their face." +
                                (if (e.chainLink > 0 && e.chainLen >= 2)
                                    "\n\n🧵 It's link ${e.chainLink} of " +
                                        "${e.chainLen} in a favor chain — " +
                                        "the whole chain goes with it."
                                else ""),
                            positiveLabel = "Drop it",
                            onPositive = {
                                val sigh = Errands.abandon(stJ, e.id)
                                prefs.saveGame(stJ)
                                UiKit.showMessageDialog(this@JournalActivity,
                                    "📋 Dropped",
                                    message = "“${sigh ?: "Oh. Okay."}” — ${Errands.displayAsker(e)}",
                                    positiveLabel = "OK",
                                    onPositive = { recreate() })
                            },
                            negativeLabel = "Keep it")
                    }
                }
            }
            box.addView(c)
        }

        // ---- WALKS (v0.5.0a): the town on foot — trips,
        // legs, the shortcuts your feet have learned, the bus
        // pass, and any trip still in progress. Assembly is
        // pure (Walk.pageLines); renders only. ----
        if (state != null) {
            sectionTitle("🗺 WALKS")
            val c = card()
            val lines = Walk.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- FACTION MISSIONS (v0.5.0d): every faction's
        // chain — beats taken on and waiting, standing gates
        // named, and the permanent marks + titles of the
        // finished ones. Assembly is pure
        // (FactionMissions.pageLines); renders only. ----
        if (state != null) {
            sectionTitle("⚔️ FACTION MISSIONS")
            val c = card()
            val lines = FactionMissions.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- CAPSTONES (v0.8.0b): every faction's big ask —
        // the chain it waits behind, the Beloved gate named,
        // beats taken on, and the traces + titles of the set
        // ones. Assembly is pure (Capstones.pageLines);
        // renders only. ----
        if (state != null) {
            sectionTitle("🏛 CAPSTONES")
            val c = card()
            val lines = Capstones.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- THE RIVAL (v0.8.0e): somebody in town picked
        // you as their measuring stick — the running
        // you–them ledger, the standing your pattern of
        // answers is writing, and the latest state of play.
        // Assembly is pure (Rival.pageLines); renders only.
        // ----
        if (state != null) {
            sectionTitle("🆚 THE RIVAL")
            val c = card()
            val lines = Rival.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- YOUR CREW (v0.9.0b): who's in, since when,
        // and the honest tallies — outings, photos together,
        // places together, contests witnessed. Assembly is
        // pure (Crew.pageLines); this renders only. ----
        if (state != null) {
            sectionTitle("👥 YOUR CREW")
            val c = card()
            val lines = Crew.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- THE CLUBHOUSE (v0.14.0e): the crew's seasonal
        // spot — the rotation, today's standing, and the
        // record of afternoons. Assembly is pure
        // (Clubhouse.pageLines); this renders only. ----
        if (state != null) {
            sectionTitle("🏠 THE CLUBHOUSE")
            val c = card()
            val lines = Clubhouse.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- SLEEPOVERS (v0.11.0c): Friday nights at your
        // house — who's coming when one's planned, and the
        // record of the nights: who came, the beats, the
        // snacks, the movie, the pillow fight. Assembly is
        // pure (Sleepovers.pageLines); this renders only. ----
        if (state != null) {
            sectionTitle("🛌 SLEEPOVERS")
            val c = card()
            val lines = Sleepovers.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- YOUR FAMILY (v0.13.0a): your folks' page —
        // warmth standings, the table's record, Mom's
        // allowance book, the chores and their askers, the
        // frame. Assembly is pure (Family.pageLines); this
        // renders only. ----
        if (state != null) {
            sectionTitle("👨‍👩‍👧 YOUR FAMILY")
            val c = card()
            val lines = Family.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- COUNTY FAIR (v0.9.0c): this year's contests,
        // entrants, places and ribbons; past fairs reduce
        // to one-line summaries (the yearbook shelf's own
        // cap discipline). Assembly is pure (Fair.pageLines);
        // this renders only. ----
        if (state != null) {
            sectionTitle("🎡 COUNTY FAIR")
            val c = card()
            val lines = Fair.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- WINTER CARNIVAL (v0.11.0e): the fair's winter
        // counterpart — this year's contests, places and
        // ribbons at Stark's Pond; past carnivals reduce to
        // one-line summaries (the fair page's own cap
        // discipline). Assembly is pure (Carnival.pageLines);
        // this renders only. ----
        if (state != null) {
            sectionTitle("🎪 WINTER CARNIVAL")
            val c = card()
            val lines = Carnival.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- HERO LOG (v0.10.0b): your identity, the patrol
        // tallies (patrols stood, assists, incidents,
        // escorts), the caper's chapter and the roster, so
        // the town's hero nights read as a record, not a
        // rumor. Assembly is pure (Heroes.pageLines); this
        // renders only. ----
        if (state != null) {
            sectionTitle("🦸 HERO LOG")
            val c = card()
            val lines = Heroes.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- YOUR STAND (v0.10.0e): the pitch, the crate,
        // the prices, the rival crew's review, and the
        // BOOKS — days open, units bought/sold/spoiled,
        // takings, costs, profit or loss, every number
        // derived from the recorded transactions.
        // Assembly is pure (Stand.pageLines); this
        // renders only. ----
        if (state != null) {
            sectionTitle("💰 YOUR STAND")
            val c = card()
            val lines = Stand.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- YOUR GARDEN (v0.11.0b): every bed's state —
        // crop, planted day, water status, growth read,
        // days to ripe, weeds — and the harvest record,
        // every claim traced to the recorded sky.
        // Assembly is pure (Garden.pageLines); this
        // renders only. ----
        if (state != null) {
            sectionTitle("🌱 YOUR GARDEN")
            val c = card()
            val lines = Garden.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- GAMES (v0.2.0e): the one home for every minigame
        // record — capsule coins, the fishing log, showdown
        // W/L, the taco high score, and the game feats. ----
        if (state != null) {
            gamesAnchor = sectionTitle("🎮 GAMES")
            run {
                val c = card()
                fun line(t: String, big: Boolean = false) {
                    c.addView(TextView(this).apply {
                        text = t
                        setTextColor(Color.parseColor(if (big) "#1a1a1a" else "#5a4a3a"))
                        textSize = if (big) 15.5f else 13f
                        if (big) typeface = android.graphics.Typeface.create(
                            "casual", android.graphics.Typeface.BOLD)
                        setPadding(0, dp(2), 0, 0)
                    })
                }
                line("🪙 Capsule Coins ×${Minigames.capsuleCoins(state)}", big = true)
                line("won at the Snowball Showdown & the Taco Catch cabinet · " +
                    "giftable, hoardable, braggable")
                // v0.5.0c: the capsule machine's ledger
                line("🃏 Pocket Pal pulls ×${PocketPals.pulls(state)} · " +
                    "album ${PocketPals.ownedCount(state)}/${PocketPals.CARDS.size} " +
                    "(Collection 🃏) · pulls are ${PocketPals.MACHINE_COST} 🪙 " +
                    "at the machine inside the Mall")
                line("🎣 FISHING — Stark's Pond", big = true)
                line("casts ${state.fishCasts} · caught ${Minigames.totalCaught(state)} · " +
                    "dawn, dusk & rain bite best")
                for (sp in FISH_TABLE) {
                    val rec = state.fishLog[sp.id]
                    line(if (rec != null && rec.count > 0)
                        "${sp.icon} ${sp.name} — ×${rec.count} · biggest ${rec.biggestCm} cm"
                    else "${sp.icon} ??? — not caught yet")
                }
                // v0.3.0e: the pond keeps your wishes too.
                line("🌠 Wishes at the pond — ×${wishCount(state)} · " +
                    "made on meteor nights, kept by the water")
                line("❄️ SNOWBALL SHOWDOWN — first to 3", big = true)
                if (state.showdown.isEmpty()) {
                    line("no duels yet — the fort at Kid's Park awaits snow")
                } else {
                    for ((n, r) in state.showdown.toSortedMap()) {
                        line("$n — ${r.wins}W · ${r.losses}L")
                    }
                }
                line("🌮 TACO CATCH — Freeman's Tacos", big = true)
                line("high score ${state.tacoHigh} · ${state.tacoPlays} run(s) at the cabinet")
                // v0.7.0d: the bee's record rides the games
                // page too — School owns the counters.
                line("🐝 SPELLING BEE — the Elementary, Fridays", big = true)
                line("entered ×${School.beeEntered(state)} · won ×${School.beeWon(state)} · " +
                    "best ${School.beeBestRound(state)} word(s) — full record on the 🎓 SCHOOL page")
                line("🏆 GAME FEATS — ${state.gameFeats.size}/${Minigames.GAME_FEATS.size}",
                    big = true)
                for (f in Minigames.GAME_FEATS) {
                    line((if (f.id in state.gameFeats) "✅ " else "⬜ ") +
                        "${f.name} — ${f.desc}")
                }
                box.addView(c)
            }
        }

        // ---- ALBUM (v0.2.0c): the selfie shelf. Photos are
        // records; PhotoView re-renders each one from its data
        // right here, and tapping opens it full-size. ----
        // v0.3.0d: the section exists even when empty — the
        // album book in your house opens THIS page (intent extra
        // "section"="album" scrolls straight to the anchor).
        if (state != null && state.photos.isEmpty()) {
            albumAnchor = sectionTitle("📸 ALBUM")
            val c = card()
            c.addView(TextView(this).apply {
                text = "No photos yet. The book on your desk is " +
                    "patient — take a 📸 SELFIE with somebody and " +
                    "the first page fills itself."
                setTextColor(Color.parseColor("#5a4a3a"))
                textSize = 13f
            })
            box.addView(c)
        }
        if (state != null && state.photos.isNotEmpty()) {
            albumAnchor = sectionTitle("📸 ALBUM")
            run {
                val c = card()
                val badge = Photos.badgeFor(state.followers)
                val next = Photos.nextMilestone(state.followers)
                c.addView(TextView(this).apply {
                    text = "👥 ${state.followers} followers" +
                        (badge?.let { " · 🏅 $it club" } ?: "") +
                        (next?.let { " · next badge at $it" } ?: "")
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 16f
                    typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                })
                val latest = state.photos.last()
                c.addView(TextView(this).apply {
                    text = "${state.photos.size} photos · " +
                        "${state.photographed.size} townsfolk photographed · " +
                        "latest shot: +${latest.followersGained} followers"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 12.5f
                })
                box.addView(c)
            }
            val stA = state
            for (rec in state.photos.reversed()) {
                val c = card()
                val view = PhotoView(this).apply { record = rec }
                c.addView(view, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(190)))
                c.addView(TextView(this).apply {
                    text = rec.caption
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 13f
                    setPadding(0, dp(4), 0, 0)
                })
                // v0.8.0d: the yearbook star — the player's
                // one editorial voice in the spring judging
                // (Yearbook.STAR_BONUS, and a starred photo
                // never fades). Its own tap target: the card
                // itself still opens the photo full-size.
                c.addView(TextView(this).apply {
                    text = if (rec.starred)
                        "⭐ Starred for the yearbook — tap to unstar"
                    else
                        "☆ Star for the yearbook " +
                            "(+${Yearbook.STAR_BONUS} with the judges)"
                    setTextColor(Color.parseColor("#7a4a12"))
                    textSize = 12.5f
                    setPadding(0, dp(3), 0, 0)
                    setOnClickListener {
                        rec.starred = !rec.starred
                        prefs.saveGame(stA)
                        recreate()
                    }
                })
                // v0.6.0a: a commissioned print wears its stamp
                // — who asked, and the ask itself, forever.
                if (rec.isCommissioned) {
                    c.addView(TextView(this).apply {
                        text = "🖨 COMMISSIONED PRINT — for ${rec.commissionAsker}\n" +
                            "“${rec.commissionText}”"
                        setTextColor(Color.parseColor("#7a4a12"))
                        textSize = 12.5f
                        setPadding(0, dp(2), 0, 0)
                    })
                }
                c.addView(TextView(this).apply {
                    text = "📈 +${rec.followersGained} followers · tap to view"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 12f
                })
                c.setOnClickListener {
                    UiKit.showPhotoDialog(this@JournalActivity, rec).show()
                }
                box.addView(c)
            }
        }

        // ---- COMMISSIONS (v0.6.0a): the town's photo
        // assignments — this week's asks, your plate, and the
        // wall of prints you've landed. Assembly is pure
        // (Commissions.pageLines); tapping the card takes an
        // ask on or lets one go. The asks live on the album
        // side of the book, next to the photos they become. ----
        if (state != null) {
            sectionTitle("📷 COMMISSIONS")
            val c = card()
            val lines = Commissions.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            val stJ = state
            val openAsks = Commissions.boardOpen(stJ)
            val held = Commissions.active(stJ)
            if (openAsks.isNotEmpty() || held.isNotEmpty()) {
                c.setOnClickListener {
                    val labels = mutableListOf<String>()
                    val actions = mutableListOf<() -> Unit>()
                    for (ask in Commissions.boardOpen(stJ)) {
                        labels.add("📌 Take: ${Commissions.objective(ask)} " +
                            "(${ask.asker} · ${ask.pay} 🪙)")
                        actions.add {
                            UiKit.showMessageDialog(this@JournalActivity,
                                "📷 ${ask.asker} commissions you",
                                message = "“${ask.text}”\n\n" +
                                    "🎯 ${Commissions.objective(ask)}\n" +
                                    "🧭 ${Commissions.hintFor(stJ, ask)}\n\n" +
                                    "Pays: +${Commissions.FRIENDSHIP} friendship · " +
                                    "+${ask.pay} 🪙 capsule coins. " +
                                    "Yours for ${Commissions.EXPIRY_DAYS} days once taken; " +
                                    "letting it lapse costs nothing.",
                                positiveLabel = "📌 Take it",
                                onPositive = {
                                    val ok = Commissions.accept(stJ, ask)
                                    prefs.saveGame(stJ)
                                    UiKit.showMessageDialog(this@JournalActivity,
                                        if (ok) "📷 Commission taken" else "📷 Plate's full",
                                        message = if (ok)
                                            Commissions.objective(ask)
                                        else
                                            "Your plate holds ${Commissions.MAX_PLATE} — " +
                                                "land one or let one lapse first.",
                                        positiveLabel = "OK",
                                        onPositive = { recreate() })
                                },
                                negativeLabel = "Leave it")
                        }
                    }
                    for (h in Commissions.active(stJ)) {
                        labels.add("🗂 Let go: ${Commissions.objective(h)} (${h.asker})")
                        actions.add {
                            UiKit.showMessageDialog(this@JournalActivity,
                                "Let ${h.asker}'s commission go?",
                                message = "“${h.text}”\n\nLetting go costs " +
                                    "nothing but the sigh.",
                                positiveLabel = "Let it go",
                                onPositive = {
                                    val sigh = Commissions.abandon(stJ, h.id)
                                    prefs.saveGame(stJ)
                                    UiKit.showMessageDialog(this@JournalActivity,
                                        "📷 Let go",
                                        message = "“${sigh ?: "Oh. Okay."}” — ${h.asker}",
                                        positiveLabel = "OK",
                                        onPositive = { recreate() })
                                },
                                negativeLabel = "Keep it")
                        }
                    }
                    UiKit.showItemsDialog(this@JournalActivity,
                        "📷 Commissions — manage", labels.toTypedArray()) { which ->
                        actions[which]()
                    }
                }
            }
            box.addView(c)
        }

        // ---- THE PAPER (v0.11.0a): the town's weekly
        // newspaper — every edition composed entirely from
        // the record and frozen at the week's turn; past
        // editions keep their headline lines below the
        // latest. Assembly is pure (Newspaper.pageLines);
        // this renders only. ----
        if (state != null) {
            sectionTitle("📰 THE PAPER")
            val c = card()
            val lines = Newspaper.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
        }

        // ---- YEARBOOK (v0.8.0d): the shelf — every spring
        // the Elementary judges your album and prints it
        // back to you. Assembly is pure (Yearbook); the
        // winning pages render their records through
        // PhotoView like the album does. The newest volumes
        // sit whole; the elders are index lines. ----
        if (state != null) {
            sectionTitle("📖 YEARBOOK")
            val c = card()
            val lines = Yearbook.pageLines(state)
            lines.forEachIndexed { i, t ->
                c.addView(TextView(this).apply {
                    text = t
                    setTextColor(Color.parseColor(
                        if (i == 0) "#1a1a1a" else "#5a4a3a"))
                    textSize = if (i == 0) 15.5f else 13f
                    if (i == 0) typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                    setPadding(0, dp(2), 0, 0)
                })
            }
            // v0.13.0c: each volume's talent-night line —
            // the April show belongs to the school year
            // it lands in, so the line stands in the card
            // with the volume it names (Talent owns the
            // record; this renders).
            for (r in Talent.allRecords(state).take(3)) {
                val tl = Talent.yearbookLine(state, r.year)
                    ?: continue
                c.addView(TextView(this).apply {
                    text = tl
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                    setPadding(0, dp(2), 0, 0)
                })
            }
            // v0.14.0c: each volume's festival line —
            // December's festival belongs to the school
            // year it lands in, so the line stands in
            // the card with the volume it names (Festival
            // owns the record; this renders).
            for (r in Festival.allRecords(state).takeLast(3)) {
                val fl = Festival.yearbookLine(state, r.year + 1)
                    ?: continue
                c.addView(TextView(this).apply {
                    text = fl
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                    setPadding(0, dp(2), 0, 0)
                })
            }
            box.addView(c)
            for (v in Yearbook.volumes(state)
                .filter { !it.indexOnly }.reversed()) {
                val cc = card()
                Yearbook.coverLines(v).forEachIndexed { i, t ->
                    cc.addView(TextView(this).apply {
                        text = t
                        setTextColor(Color.parseColor(
                            if (i == 0) "#1a1a1a" else "#5a4a3a"))
                        textSize = if (i == 0) 16f else 12.5f
                        if (i == 0) typeface =
                            android.graphics.Typeface.create(
                                "casual", android.graphics.Typeface.BOLD)
                        setPadding(0, dp(2), 0, 0)
                    })
                }
                box.addView(cc)
                for (e in v.entries) {
                    val cat = Yearbook.categoryById(e.categoryId)
                    val ec = card()
                    ec.addView(TextView(this).apply {
                        text = "${cat?.emoji ?: "📷"} " +
                            "${cat?.title ?: e.categoryId}"
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 15f
                        typeface = android.graphics.Typeface.create(
                            "casual", android.graphics.Typeface.BOLD)
                    })
                    val rec = e.record
                    if (rec != null) {
                        val view = PhotoView(this).apply {
                            record = rec }
                        ec.addView(view, LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, dp(190)))
                        ec.addView(TextView(this).apply {
                            text = rec.caption
                            setTextColor(Color.parseColor("#1a1a1a"))
                            textSize = 13f
                            setPadding(0, dp(4), 0, 0)
                        })
                        ec.addView(TextView(this).apply {
                            text = "Judges' score: ${e.score}" +
                                (if (rec.starred) " (⭐ starred)" else "")
                            setTextColor(Color.parseColor("#5a4a3a"))
                            textSize = 12f
                        })
                    }
                    val role = Yearbook.EDITORS
                        .firstOrNull { it.name == e.judge }?.role
                    ec.addView(TextView(this).apply {
                        text = "“${e.note}”" +
                            (if (e.judge.isNotEmpty())
                                "\n— ${e.judge}" +
                                    (role?.let { ", $it" } ?: "")
                            else "")
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 12.5f
                        setPadding(0, dp(3), 0, 0)
                    })
                    box.addView(ec)
                }
            }
            val elders = Yearbook.volumes(state)
                .filter { it.indexOnly }
            if (elders.isNotEmpty()) {
                val c2 = card()
                c2.addView(TextView(this).apply {
                    text = "📚 THE EARLIER BOOKS"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 15f
                    typeface = android.graphics.Typeface.create(
                        "casual", android.graphics.Typeface.BOLD)
                })
                for (v in elders.reversed()) {
                    c2.addView(TextView(this).apply {
                        text = Yearbook.indexLine(v)
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 12.5f
                        setPadding(0, dp(2), 0, 0)
                    })
                }
                box.addView(c2)
            }
        }

        // ---- WHO ----
        sectionTitle("🧒 WHO'S WHERE")
        if (state == null) {
            box.addView(TextView(this).apply {
                text = "No town yet. Go walk around first, dude."
                setTextColor(Color.parseColor(Paper.YELLOW))
            })
        } else {
            for (kid in KID_ORDER) {
                val a = state.agents[kid] ?: continue
                val pts = state.friendship[kid] ?: 0
                val hearts = friendHearts(pts)
                val heartStr = "♥".repeat(hearts) + "♡".repeat(5 - hearts)
                val locName = LOCATIONS[a.location]?.name ?: a.location
                val c = card()
                c.addView(TextView(this).apply {
                    text = a.name
                    setTextColor(Color.parseColor(a.color))
                    textSize = 18f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                c.addView(TextView(this).apply {
                    text = heartStr
                    setTextColor(Color.parseColor("#d43d2a"))
                    textSize = 18f
                })
                c.addView(TextView(this).apply {
                    text = "${friendTier(pts)} ($pts)"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
                })
                // v0.12.0c: the birthday, on the card — the
                // date the town keeps for them (Birthdays
                // owns the registry and its sourcing).
                c.addView(TextView(this).apply {
                    text = "🎂 Birthday: ${Birthdays.dateLabel(kid)}" +
                        if (Birthdays.isBirthday(state, kid)) " — TODAY! 🎉" else ""
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                })
                // v0.1.5: relationship tier + character memory excerpt
                c.addView(TextView(this).apply {
                    text = "${Relationships.tierIcon(pts)} Relationship: ${Relationships.tierLabel(pts)}" +
                        (factionOfCast(kid)?.let { fid ->
                            " · ${FACTION_BY_ID[fid]?.icon ?: ""} ${FACTION_BY_ID[fid]?.name ?: ""}"
                        } ?: "")
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 13f
                })
                NpcMemoryStore.get(kid)?.let { mem ->
                    if (mem.summary.isNotEmpty()) {
                        c.addView(TextView(this).apply {
                            text = "🧠 ${mem.summary}"
                            setTextColor(Color.parseColor("#5a4a3a"))
                            textSize = 12.5f
                            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.ITALIC)
                        })
                    }
                    // #12: taste is visible once learned — gift smarter
                    if (mem.likes.isNotEmpty()) {
                        c.addView(TextView(this).apply {
                            text = "❤️ Likes: ${mem.likes.joinToString(", ")}"
                            setTextColor(Color.parseColor("#a03028"))
                            textSize = 12.5f
                        })
                    }
                }
                relationsOf(kid).take(2).forEach { other ->
                    c.addView(TextView(this).apply {
                        // v0.3.0c: a finished storyline's permanent
                        // note overrides the static one here
                        text = "🔗 $other — ${StoryArcs.relationNoteFor(state, kid, other)}"
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 12.5f
                    })
                }
                c.addView(TextView(this).apply {
                    text = if (a.asleep) "😴 Asleep at $locName" else "📍 $locName — ${a.activity}"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 14f
                    setPadding(0, dp(4), 0, 0)
                })
                // v0.1.9e: what they're up to — the current goal
                c.addView(TextView(this).apply {
                    text = NpcGoals.journalLine(state, kid)
                    setTextColor(Color.parseColor("#7a4a12"))
                    textSize = 12.5f
                })
                if (!a.asleep) {
                    engine.nextAgendaStop(state, a, state.timeMin)?.let { nxt ->
                        c.addView(TextView(this).apply {
                            text = "🕐 Later: $nxt"
                            setTextColor(Color.parseColor("#5a4a3a"))
                            textSize = 12.5f
                        })
                    }
                }
                // #30: the whole day on the same card — past blocks
                // struck through, current one marked, future plain.
                try {
                    val dayBlocks = a.agendaBase.entries
                        .sortedBy { timeStrToMinutes(it.key) }
                    var curIdx = -1
                    for ((i, e) in dayBlocks.withIndex()) {
                        if (timeStrToMinutes(e.key) <= state.timeMin) curIdx = i
                    }
                    val sb = StringBuilder()
                    val strikes = mutableListOf<IntRange>()
                    for ((i, e) in dayBlocks.withIndex()) {
                        val start = sb.length
                        val marker = if (i == curIdx) "▶ " else ""
                        sb.append("$marker${e.key} " +
                            "${LOCATIONS[e.value.location]?.name ?: e.value.location}" +
                            " — ${e.value.activity}\n")
                        if (i < curIdx) strikes.add(start until sb.length)
                    }
                    val span = android.text.SpannableString(sb.toString().trimEnd())
                    for (r in strikes) {
                        if (r.first < span.length) {
                            span.setSpan(android.text.style.StrikethroughSpan(),
                                r.first, minOf(r.last, span.length), 0)
                        }
                    }
                    c.addView(TextView(this).apply {
                        text = span
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 12f
                        setPadding(0, dp(4), 0, 0)
                    })
                } catch (e: Exception) { /* agenda card is flavor */ }
                LoreBook.charLore(kid)?.let { lore ->
                    c.addView(TextView(this).apply {
                        text = "📖 ${LoreBook.excerpt(lore.text, 240)}"
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 13f
                        typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.ITALIC)
                        setPadding(0, dp(4), 0, 0)
                    })
                }
                box.addView(c)
            }
            // collection progress
            val found = state.collected.size
            val total = COLLECTIBLES.size
            // v0.1.9b: nav buttons are chunky parchment game buttons
            box.addView(Button(this).apply {
                text = "✨ Collection ($found/$total)"
                setTextColor(Color.parseColor(Chrome.INK))
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@JournalActivity, Paper.PARCHMENT)
                setOnClickListener { startActivity(Intent(this@JournalActivity, CollectionActivity::class.java)) }
            })
            if (LoreBook.loaded) {
                box.addView(Button(this).apply {
                    text = "📖 Lorebook (${LoreBook.size} entries)"
                    setTextColor(Color.parseColor(Chrome.INK))
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                    background = UiKit.buttonBg(this@JournalActivity, Paper.PARCHMENT)
                    setOnClickListener { startActivity(Intent(this@JournalActivity, LoreActivity::class.java)) }
                })
            }

            // ---- INVENTORY button ----
            val invCount = state.inventory.values.sum()
            box.addView(Button(this).apply {
                text = "🎒 Inventory ($invCount items)"
                setTextColor(Color.parseColor(Chrome.INK))
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@JournalActivity, Paper.PARCHMENT)
                setOnClickListener { startActivity(Intent(this@JournalActivity, InventoryActivity::class.java)) }
            })
            box.addView(Button(this).apply {
                text = "🎯 Quest Tracker"
                setTextColor(Color.parseColor(Chrome.INK))
                typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                background = UiKit.buttonBg(this@JournalActivity, Paper.PARCHMENT)
                setOnClickListener { startActivity(Intent(this@JournalActivity, QuestActivity::class.java)) }
            })

            // ---- #43 THE "YOU" CARD: standings, closest friends,
            // and your own event history on one card ----
            sectionTitle("🪪 YOU")
            run {
                val c = card()
                val meJ = state.agents.values.firstOrNull { it.isPlayer }
                c.addView(TextView(this).apply {
                    text = meJ?.name ?: "New Kid"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 17f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                // v0.1.9d: your kid, drawn — the same avatarSpec /
                // FigureGeometry.derive() construction as the creator
                // preview, so your build wears identically here
                val youFig = AvatarPreviewView(this@JournalActivity).apply {
                    appearance = meJ?.appearance?.takeIf { it.isNotEmpty() }
                        ?: prefs.getAppearance()
                    // v0.15.0a: the YOU card is a preview too —
                    // it wears the LIVE look (LiveLook, the one
                    // derivation): pajamas on a sleepover night,
                    // the costume on a patrol night, the day
                    // look otherwise, like the creator's preview
                    // and the mirror glass.
                    val look = LiveLook.playerLook(state)
                    liveKind = look.kind
                    liveCode = look.code
                }
                c.addView(youFig, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(150)))
                c.addView(TextView(this).apply {
                    text = "⚔️ Standings: " + FACTIONS.joinToString(" · ") { f ->
                        val fs = state.factionStates[f.id]
                        "${f.name} ${fs?.let { Factions.standingLabel(it.playerStanding) } ?: "—"}"
                    }
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 13f
                })
                val scored = (state.friendship.entries.map { it.key to it.value } +
                    NpcMemoryStore.all().entries
                        .filter { it.key !in state.friendship }
                        .map { it.key to it.value.points })
                    .sortedByDescending { it.second }.take(3)
                if (scored.isNotEmpty()) {
                    c.addView(TextView(this).apply {
                        text = "💛 Closest: " + scored.joinToString(", ") { (n, p) ->
                            "$n (${Relationships.tierLabel(p)})"
                        }
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 13f
                    })
                }
                // v0.5.0d: faction titles earned from finished
                // chains ride the YOU card, like a log of names
                val factionTitles = FactionMissions.titlesEarned(state)
                if (factionTitles.isNotEmpty()) {
                    c.addView(TextView(this).apply {
                        text = "⚔️ Titles: ${factionTitles.joinToString(" · ")}"
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 13f
                    })
                }
                // v0.10.0b: your hero identity rides the YOU
                // card too — the name and power, the same
                // words the mirror and the patrol use.
                Heroes.youCardLine(state)?.let { heroLine ->
                    c.addView(TextView(this).apply {
                        text = heroLine
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 13f
                    })
                }
                // v0.8.0b: capstone titles ride beside them —
                // the names the factions themselves conferred
                val capstoneTitles = Capstones.titlesEarned(state)
                if (capstoneTitles.isNotEmpty()) {
                    c.addView(TextView(this).apply {
                        text = "🏛 Capstone titles: ${capstoneTitles.joinToString(" · ")}"
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 13f
                    })
                }
                val marks = state.lifeText.keys.count { it.startsWith("mark:") }
                c.addView(TextView(this).apply {
                    text = "🎪 Town history so far: ${state.townLog.size} entries in the log" +
                        (if (marks > 0) " · 🗿 $marks event marks on the town" else "") +
                        (if (state.convertedResidents.isNotEmpty())
                            " · 🏠 Moved in because of you: ${state.convertedResidents.joinToString(", ")}"
                        else "")
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 12.5f
                })
                box.addView(c)
            }

            // ---- FACTIONS (v0.1.5 living political layer) ----
            sectionTitle("⚔️ FACTIONS")
            for (f in FACTIONS) {
                val fs = state.factionStates[f.id] ?: continue
                val c = card()
                c.addView(TextView(this).apply {
                    text = "${f.icon} ${f.name}"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 16f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                })
                c.addView(TextView(this).apply {
                    text = f.blurb
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 12.5f
                    typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.ITALIC)
                })
                val bars = "█".repeat((fs.influence / 10).coerceIn(0, 10)) +
                    "░".repeat(10 - (fs.influence / 10).coerceIn(0, 10))
                c.addView(TextView(this).apply {
                    // #3: the mood shown is the MECHANICAL one —
                    // event swing + weather pressure, one truth
                    text = "Influence: $bars ${fs.influence}\n" +
                        "Your standing: ${Factions.standingLabel(fs.playerStanding)} (${fs.playerStanding})\n" +
                        "Mood: ${Factions.moodLabel(Factions.moodIndex(f.id, fs, state.weatherId))} (${fs.mood})"
                    setTextColor(Color.parseColor("#1a1a1a"))
                    textSize = 13f
                })
                val relBits = FACTIONS.filter { it.id != f.id }.mapNotNull { other ->
                    val v = state.factionRelations[Factions.relationKey(f.id, other.id)] ?: 0
                    if (v != 0) "${other.name}: ${Factions.relationLabel(v)}" else null
                }
                if (relBits.isNotEmpty()) {
                    c.addView(TextView(this).apply {
                        text = relBits.joinToString(" · ")
                        setTextColor(Color.parseColor("#5a4a3a"))
                        textSize = 12.5f
                    })
                }
                c.addView(TextView(this).apply {
                    text = "Members: ${f.members.joinToString(", ")}\n" +
                        "Turf: ${f.turf.joinToString(", ") { LOCATIONS[it]?.name ?: it }}"
                    setTextColor(Color.parseColor("#5a4a3a"))
                    textSize = 12.5f
                })
                box.addView(c)
            }

            // ---- GENERATED LOREBOOK PEOPLE IN TOWN ----
            try {
                val weekJ = (state.gameDay + (state.gameMonth - 1) * 31) / 7
                val genNow = VisitorSystem.visitorsFor(state.todayKey, state.timeMin) +
                    VisitorSystem.residentsNow(state.timeMin, weekJ,
                        isSleepHour(state.timeMin / 60), dayOfWeek(state), state.todayKey)
                if (genNow.isNotEmpty()) {
                    // #24: generated folks get the same Who's Where
                    // treatment — current activity included, and
                    // visitors warn you they're leaving.
                    sectionTitle("🧳 LOREBOOK FOLKS IN TOWN")
                    for (g in genNow) {
                        val c = card()
                        val mem = NpcMemoryStore.get(g.persona.loreName)
                        c.addView(TextView(this).apply {
                            text = "${g.persona.loreName}${if (g.persona.resident) " (local)" else " (visiting)"}"
                            setTextColor(Color.parseColor(g.persona.color))
                            textSize = 15f
                            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
                        })
                        c.addView(TextView(this).apply {
                            text = "📍 ${LOCATIONS[g.locId]?.name ?: g.locId}" +
                                (if (g.inside) " (inside)" else "") +
                                (mem?.let { " · ${Relationships.tierIcon(it.points)} ${Relationships.tierLabel(it.points)}" } ?: "") +
                                (g.persona.factionId?.let { fid ->
                                    " · ${FACTION_BY_ID[fid]?.name ?: ""}"
                                } ?: "")
                            setTextColor(Color.parseColor("#1a1a1a"))
                            textSize = 13f
                        })
                        c.addView(TextView(this).apply {
                            text = if (g.persona.resident) {
                                "🗓️ " + NpcGenerator.residentBlockAt(g.persona,
                                    state.timeMin, weekJ, dayOfWeek(state)).second
                            } else {
                                "🧳 Passing through — the visitor rotation turns every " +
                                    "few hours. Catch them while they're here."
                            }
                            setTextColor(Color.parseColor("#5a4a3a"))
                            textSize = 12.5f
                        })
                        // v0.1.9e: generated folks have goals too
                        c.addView(TextView(this).apply {
                            text = NpcGoals.journalLine(state, g.persona.loreName)
                            setTextColor(Color.parseColor("#7a4a12"))
                            textSize = 12.5f
                        })
                        // #12 + #23: learned tastes and derived loves,
                        // in the journal's own format
                        val tasteBits = mutableListOf<String>()
                        if (mem != null && mem.likes.isNotEmpty()) {
                            tasteBits.add("learned: ${mem.likes.joinToString(", ")}")
                        }
                        if (g.persona.loves.isNotEmpty()) {
                            tasteBits.add("into: ${g.persona.loves.joinToString(", ")}")
                        }
                        if (tasteBits.isNotEmpty()) {
                            c.addView(TextView(this).apply {
                                text = "❤️ Likes — " + tasteBits.joinToString(" · ")
                                setTextColor(Color.parseColor("#a03028"))
                                textSize = 12.5f
                            })
                        }
                        c.addView(TextView(this).apply {
                            text = "📖 ${LoreBook.excerpt(g.persona.bio, 200)}"
                            setTextColor(Color.parseColor("#5a4a3a"))
                            textSize = 12.5f
                            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.ITALIC)
                        })
                        box.addView(c)
                    }
                }
            } catch (e: Exception) {
                // visitors are flavor; never break the Journal
            }

            // ---- JOURNAL ----
            sectionTitle("📓 JOURNAL")
            val me = state.agents.values.firstOrNull { it.isPlayer }
            val mems = me?.memories?.takeLast(15) ?: emptyList()
            if (mems.isEmpty()) {
                box.addView(TextView(this).apply {
                    text = "Nothing yet. Go yap at some kids."
                    setTextColor(Color.parseColor("#8a8a8a"))
                })
            } else {
                for (m in mems.reversed()) {
                    val c = card()
                    c.addView(TextView(this).apply {
                        text = "• $m"
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 14f
                    })
                    box.addView(c)
                }
            }
            sectionTitle("📢 WORD ON THE STREET")
            for (e in state.events.takeLast(8).reversed()) {
                box.addView(TextView(this).apply {
                    text = e
                    setTextColor(Color.parseColor(Paper.YELLOW))
                    textSize = 13f
                    setPadding(0, dp(3), 0, dp(3))
                })
            }
            // #38: the town keeps its own history — event outcomes,
            // holidays, move-ins — written on completion, read here.
            if (state.townLog.isNotEmpty()) {
                sectionTitle("🏛️ TOWN HISTORY")
                for (e in state.townLog.takeLast(20).reversed()) {
                    box.addView(TextView(this).apply {
                        text = e
                        setTextColor(Color.parseColor(Paper.YELLOW))
                        textSize = 13f
                        setPadding(0, dp(3), 0, dp(3))
                    })
                }
                val marks = state.lifeText.entries.filter { it.key.startsWith("mark:") }
                if (marks.isNotEmpty()) {
                    sectionTitle("🗿 MARKS ON THE TOWN")
                    for ((k, v) in marks) {
                        box.addView(TextView(this).apply {
                            text = "${LOCATIONS[k.removePrefix("mark:")]?.name ?: k}: $v"
                            setTextColor(Color.parseColor(Paper.YELLOW))
                            textSize = 13f
                            setPadding(0, dp(3), 0, dp(3))
                        })
                    }
                }
            }

            // ---- #56 MILESTONE PHOTO WALL: framed firsts ----
            sectionTitle("🖼️ MILESTONE WALL")
            val miles = mutableListOf<String>()
            val bestFriend = state.friendship.entries.maxByOrNull { it.value }
            if (bestFriend != null &&
                Relationships.tierOf(bestFriend.value) >= Relationships.CLOSE_FRIEND) {
                miles.add("💛 First Close Friend — ${bestFriend.key}")
            }
            if (state.townLog.any { it.startsWith("🎪") }) {
                miles.add("🎪 First event weathered — it's in the Town History")
            }
            if (state.factionRelations.values.any { it != 0 }) {
                miles.add("⚔️ First faction shift you caused")
            }
            if (state.convertedResidents.isNotEmpty()) {
                miles.add("🏠 First move-in — ${state.convertedResidents.first()}")
            }
            if ((state.life["holidaysSeen"] ?: 0) > 0) {
                miles.add("🎉 First holiday in town")
            }
            if (TownLife.rep(state) >= 10) {
                miles.add("⭐ A reputation forms — ${TownLife.repTier(state)}")
            }
            if ((state.life["gnomeSeen"] ?: 0) > 0) {
                miles.add("🧌 Traveling gnome sightings — ${state.life["gnomeSeen"]} so far")
            }
            // v0.2.0c: the follower count joins the wall of firsts
            Photos.badgeFor(state.followers)?.let { badge ->
                miles.add("📸 $badge followers — minor local celebrity era")
            }
            if (state.lifeText["sighting333"] != null) {
                miles.add("??? — 3:33 AM: something small crossed a lit yard")
            }
            if (state.lifeText["capsule"] != null) {
                miles.add("🕰️ A time capsule sleeps under the park")
            }
            if (miles.isEmpty()) {
                box.addView(TextView(this).apply {
                    text = "No firsts yet. The wall is patient."
                    setTextColor(Color.parseColor("#8a8a8a"))
                })
            } else {
                for (m in miles) {
                    val c = card()
                    c.addView(TextView(this).apply {
                        text = "🖼️ $m"
                        setTextColor(Color.parseColor("#1a1a1a"))
                        textSize = 14f
                    })
                    box.addView(c)
                }
            }
        }
        setContentView(root)
        // v0.3.0d: the album book in your house lands you on the
        // album shelf, not the top of the Journal.
        // v0.5.0e: the phone's tiles land on their sections the
        // same way ("album" / "games").
        val section = intent?.getStringExtra("section")
        if (section == "album" || section == "games" ||
            section == "penpals") {
            scroll.post {
                val anchor = when (section) {
                    "games" -> gamesAnchor
                    "penpals" -> penpalsAnchor
                    else -> albumAnchor
                }
                if (anchor != null) scroll.scrollTo(0, anchor.top)
            }
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

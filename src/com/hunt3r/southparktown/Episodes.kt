package com.hunt3r.southparktown

// ============================================================
// Episodes — episode framing for the story machine (v0.1.9c).
//
// Every shenanigans event (and every holiday, through the same
// machinery since #31) plays like an EPISODE of the show:
//   - a TITLE CARD when it kicks off (generated episode title
//     from the def + a rotating title treatment, SOUTH PARK
//     header, the causers credited like a title card),
//   - ACT-BREAK stings at each stage transition (queued by the
//     engine; the Activity presents them at the next safe
//     moment — never mid-dialogue),
//   - a CREDITS wrap-up at resolution (episode page: acts,
//     recap, your participation, faction shifts, the mark and
//     residue the event left behind),
//   - episodes ACCUMULATE in a Journal episode list (feeds
//     the event lineage from #32 — runs of the same story get
//     rotating titles, like a returning bit).
//
// All text is newly written / generated from the defs — no
// show scripts, no assets. PURE Kotlin: the engine flips the
// flags, the JVM suite pins the behavior, the Activities only
// present what the flags say. The underlying 4-stage engine,
// participation, marks and quest reskinning are untouched —
// this is presentation + accumulation on top.
// ============================================================

data class EpisodeRecord(
    val key: String,                 // "<eventId>@<startDayKey>" — unique per run
    val kind: String,                // "event" | "holiday"
    val eventId: String,             // ShenaniganDef.id or Holiday.id
    val eventTitle: String,          // the def's own title / the holiday's name
    val title: String,               // generated episode title for THIS run
    val treatment: String,           // rotating presentation line
    val causers: List<String>,       // title-card credits
    val dayKey: String,              // in-game day the episode started
    val dateLabel: String,           // "10/24"
    val stageTitles: List<String> = emptyList(),  // 4 for events, none for holidays
    val stageNotes: List<String> = emptyList(),
    var endDayKey: String = "",
    var titleShown: Boolean = false,
    val actsShown: MutableSet<Int> = mutableSetOf(),
    var actPending: Int = -1,        // stage index awaiting its act-break sting
    var coldOpen: Boolean = false,   // the player was there for the opening beat
    val startInfluence: Map<String, Int> = emptyMap(), // faction snapshot at kickoff
    var outcome: String = "",
    var participation: Int = 0,
    var witness: String? = null,
    var markText: String = "",
    var markLocName: String = "",
    var residueName: String = "",
    var holidayTalks: Int = 0,
    var daysRun: Int = 1,
    var finished: Boolean = false,
    var wrapShown: Boolean = false,
    var recap: List<String> = emptyList(),
    // v0.7.0e: the climax tier this episode settled at
    // ("heroic" | "handled" | "fumbled" | "absent"; "" on
    // records written before the playable climaxes — see
    // climaxTierOf, which reads those as the before-record).
    var climaxTier: String = "",
    var climaxHow: String = ""
) {
    /** Prefs/JSON shape (the smoke suite round-trips this too). */
    fun toMap(): Map<String, Any?> = mapOf(
        "key" to key, "kind" to kind, "eventId" to eventId,
        "eventTitle" to eventTitle, "title" to title,
        "treatment" to treatment, "causers" to causers,
        "dayKey" to dayKey, "dateLabel" to dateLabel,
        "stageTitles" to stageTitles, "stageNotes" to stageNotes,
        "endDayKey" to endDayKey, "titleShown" to titleShown,
        "actsShown" to actsShown.toList(), "actPending" to actPending,
        "coldOpen" to coldOpen, "startInfluence" to startInfluence,
        "outcome" to outcome, "participation" to participation,
        "witness" to witness, "markText" to markText,
        "markLocName" to markLocName, "residueName" to residueName,
        "holidayTalks" to holidayTalks, "daysRun" to daysRun,
        "finished" to finished, "wrapShown" to wrapShown,
        "recap" to recap,
        "climaxTier" to climaxTier, "climaxHow" to climaxHow
    )
}

/** The rendered episode page (title card / wrap-up / Journal) —
 *  pure data; UiKit turns it into the paper-chrome dialog. */
data class EpisodePage(
    val titleLine: String,
    val subLine: String,
    val starringLine: String,
    val body: List<String>,
    val footer: String
)

object Episodes {

    val TREATMENTS = listOf(
        "TONIGHT, ON SOUTH PARK…",
        "A BRAND-NEW EPISODE",
        "THE TOWN PRESENTS",
        "A VERY SPECIAL EPISODE",
        "LIVE FROM A QUIET MOUNTAIN TOWN",
        "PREVIOUSLY, NOTHING LIKE THIS HAPPENED"
    )

    private val ACT_NAMES = listOf("ACT I", "ACT II", "ACT III", "ACT IV")

    fun actName(stageIdx: Int): String =
        ACT_NAMES[stageIdx.coerceIn(0, 3)]

    /** How many episodes of this story have ever aired (incl. this one
     *  once begun) — drives the rotating title, like a returning bit. */
    fun runIndex(state: GameState, eventId: String): Int =
        state.episodes.count { it.kind == "event" && it.eventId == eventId }

    /** The generated episode title + treatment for a run of [def]. */
    fun titleFor(def: ShenaniganDef, runIndex: Int, dayOrdinal: Int): Pair<String, String> {
        val variants = listOf(def.title) + def.episodeTitles
        val title = variants[((runIndex % variants.size) + variants.size) % variants.size]
        val ti = ((runIndex + dayOrdinal) % TREATMENTS.size + TREATMENTS.size) % TREATMENTS.size
        return title to TREATMENTS[ti]
    }

    fun coldOpenLines(def: ShenaniganDef): List<Line> =
        if (def.coldOpen.isNotEmpty()) def.coldOpen
        else def.causers.firstOrNull()?.let { listOf(Line(it, def.cause)) } ?: emptyList()

    // ---------------- record lookup ----------------

    /** The unfinished episode of any kind started most recently. */
    fun airing(state: GameState): EpisodeRecord? =
        state.episodes.lastOrNull { !it.finished }

    fun airingEvent(state: GameState, eventId: String, dayKey: String): EpisodeRecord? =
        state.episodes.lastOrNull {
            !it.finished && it.kind == "event" && it.eventId == eventId && it.dayKey == dayKey
        }

    fun airingHoliday(state: GameState): EpisodeRecord? =
        state.episodes.lastOrNull { !it.finished && it.kind == "holiday" }

    private fun trim(state: GameState) {
        // keep the shelf bounded; the airing episode is always the
        // newest, so the front of the list is safe to retire
        while (state.episodes.size > 30) state.episodes.removeAt(0)
    }

    // ---------------- lifecycle (called by the Engine) ----------------

    /** An event just kicked off: open its episode. Snapshots faction
     *  influence BEFORE stage-0 effects so the wrap-up can diff the
     *  whole day's shifts. */
    fun beginEvent(state: GameState, def: ShenaniganDef): EpisodeRecord {
        airingEvent(state, def.id, state.todayKey)?.let { return it }
        val (title, treatment) = titleFor(def, runIndex(state, def.id),
            ordinalOfKey(state.todayKey))
        val ep = EpisodeRecord(
            key = "${def.id}@${state.todayKey}",
            kind = "event",
            eventId = def.id,
            eventTitle = def.title,
            title = title,
            treatment = treatment,
            causers = def.causers,
            dayKey = state.todayKey,
            dateLabel = "${state.gameMonth}/${state.gameDay}",
            stageTitles = def.stages.map { it.title },
            stageNotes = def.stages.map { it.journalNote },
            startInfluence = state.factionStates.mapValues { it.value.influence },
            witness = state.eventWitness
        )
        state.episodes.add(ep)
        trim(state)
        return ep
    }

    /** A stage transition landed: queue the act-break sting (the
     *  Activity presents it at the next safe moment and marks every
     *  act up to it shown — a long absence never stacks stings). */
    fun noteStage(state: GameState, def: ShenaniganDef, stageIdx: Int) {
        if (stageIdx !in 1..3) return
        val ep = airingEvent(state, def.id, state.shenaniganDay) ?: return
        if (stageIdx !in ep.actsShown && ep.actPending < stageIdx) {
            ep.actPending = stageIdx
        }
    }

    /** The event just ended (midnight rollover, next to the outcome
     *  lines in rollShenanigan): finalize the episode — diff the
     *  faction shifts, write the recap, queue the credits wrap-up. */
    fun finishEvent(
        state: GameState, def: ShenaniganDef, participation: Int, outcome: String,
        markText: String, markLocName: String, residueName: String?, witness: String?,
        extraRecap: List<String> = emptyList(),
        climaxTier: String = "", climaxHow: String = ""
    ): EpisodeRecord {
        val ep = airingEvent(state, def.id, state.shenaniganDay)
            ?: ensureEventRecord(state, def)
        ep.participation = participation
        ep.outcome = outcome
        ep.witness = witness ?: ep.witness
        ep.markText = markText
        ep.markLocName = markLocName
        ep.residueName = residueName ?: ""
        val shifts = mutableListOf<String>()
        for ((fid, start) in ep.startInfluence) {
            val now = state.factionStates[fid]?.influence ?: continue
            val diff = now - start
            if (diff != 0) {
                val name = FACTION_BY_ID[fid]?.name ?: fid
                shifts.add("$name sway ${if (diff > 0) "+$diff" else "$diff"}")
            }
        }
        val recap = mutableListOf<String>()
        recap.add("It began like this: ${def.cause}")
        recap.add("The acts: " + def.stages.joinToString(" → ") { it.title })
        recap.add("✋ Your hand in it: $participation/10 — $outcome")
        ep.witness?.let {
            recap.add("👀 $it was right in the middle of it — the town's unofficial witness.")
        }
        recap.add(if (shifts.isNotEmpty())
            "⚔️ Faction shifts: ${shifts.joinToString("; ")}."
        else "⚔️ The factions barely budged.")
        // v0.2.0f: story-specific closing lines (the mystery's
        // finale branch writes its own ending into the credits).
        recap.addAll(extraRecap)
        // v0.7.0e: a PLAYED climax writes its own line into the
        // credits — the tier and the one-line how-it-went. An
        // absent climax records its tier silently and adds no
        // line: the page stays exactly what it always was.
        ep.climaxTier = climaxTier
        ep.climaxHow = climaxHow
        if (climaxTier == Climaxes.TIER_HEROIC ||
            climaxTier == Climaxes.TIER_HANDLED ||
            climaxTier == Climaxes.TIER_FUMBLED) {
            recap.add("🎬 The climax, your way: $climaxHow " +
                "(${Climaxes.tierLabel(climaxTier)})")
        }
        if (markText.isNotEmpty()) {
            recap.add("🗿 Left its mark at $markLocName: $markText")
        }
        if (ep.residueName.isNotEmpty()) {
            recap.add("🌀 ${ep.residueName} kept a piece of it in their routine.")
        }
        ep.recap = recap
        ep.actPending = -1
        ep.endDayKey = state.todayKey
        ep.finished = true
        ep.wrapShown = false
        return ep
    }

    /** v0.7.0e: the climax tier an episode REMEMBERS. A
     *  record written by this build carries its tier. A
     *  FINISHED event-episode from before the playable
     *  climaxes carries none — those are the BEFORE-RECORDS:
     *  read as "legacy" here, which counts as GOT IT DONE
     *  (Climaxes.TIER_HANDLED) for any memory purpose — the
     *  player was there in the old way — and stays marked
     *  as a before-record so later code never mistakes it
     *  for a played tier. Anything else (unfinished, or a
     *  non-event kind) has no tier at all. */
    fun climaxTierOf(ep: EpisodeRecord): String {
        if (ep.climaxTier.isNotEmpty()) return ep.climaxTier
        if (ep.finished && ep.kind == "event" &&
            ep.eventId in Climaxes.CHAINS) return "legacy"
        return ""
    }

    /** The memory-facing read of an episode's climax tier:
     *  the before-record counts as GOT IT DONE. */
    fun climaxTierForMemory(ep: EpisodeRecord): String =
        when (val t = climaxTierOf(ep)) {
            "legacy" -> Climaxes.TIER_HANDLED
            else -> t
        }

    /** A holiday just started: it gets an episode too (#31's story
     *  machine, now with the full framing). */
    fun beginHoliday(state: GameState, holiday: Holiday): EpisodeRecord {
        airingHoliday(state)?.let { if (it.eventId == holiday.id) return it }
        val ep = EpisodeRecord(
            key = "holiday:${holiday.id}@${state.todayKey}",
            kind = "holiday",
            eventId = holiday.id,
            eventTitle = holiday.name,
            title = holiday.name,
            treatment = "A SOUTH PARK HOLIDAY SPECIAL",
            causers = listOf("The Whole Town"),
            dayKey = state.todayKey,
            dateLabel = "${state.gameMonth}/${state.gameDay}"
        )
        state.episodes.add(ep)
        trim(state)
        return ep
    }

    /** Bank yesterday's holiday-talk counter into the airing holiday
     *  episode (called while the counter is still yesterday's). */
    fun noteHolidayTalks(state: GameState) {
        val ep = airingHoliday(state) ?: return
        ep.holidayTalks += state.questCounters["holidayTalks"] ?: 0
    }

    /** The same holiday rolls into another day. */
    fun noteHolidayDay(state: GameState) {
        airingHoliday(state)?.let { it.daysRun++ }
    }

    // ---------------- season finales (v0.5.0h) ----------------

    /** A season finale's week opened: it gets the episode
     *  treatment too — a title card on the first morning and
     *  credits when the week wraps (Finales owns the state;
     *  this is framing only, like the holidays). */
    fun beginFinale(state: GameState, def: FinaleDef): EpisodeRecord {
        state.episodes.lastOrNull {
            !it.finished && it.kind == "finale" && it.eventId == def.id
        }?.let { return it }
        val ep = EpisodeRecord(
            key = "finale:${def.id}@${state.todayKey}",
            kind = "finale",
            eventId = def.id,
            eventTitle = "${def.seasonName} Finale",
            title = def.title,
            treatment = "A SOUTH PARK SEASON FINALE",
            causers = listOf(def.host) + def.ensemble
                .filter { it != def.host }.take(3),
            dayKey = state.todayKey,
            dateLabel = "${state.gameMonth}/${state.gameDay}",
            stageTitles = def.steps.map { it.title } + "The Call",
            stageNotes = def.steps.map { it.short },
            startInfluence = state.factionStates.mapValues { it.value.influence }
        )
        state.episodes.add(ep)
        trim(state)
        return ep
    }

    /** The morning after the window: finalize the finale's
     *  episode — the beats caught, the call (or the town's,
     *  or the empty chair), the standing mark it leaves. */
    fun finishFinale(state: GameState, def: FinaleDef, stepsLanded: Int,
                     outcome: String, markText: String,
                     branch: String): EpisodeRecord? {
        val ep = state.episodes.lastOrNull {
            !it.finished && it.kind == "finale" && it.eventId == def.id
        } ?: return null
        ep.participation = stepsLanded
        ep.outcome = outcome
        ep.witness = def.host
        ep.markText = markText
        ep.markLocName = LOCATIONS[def.loc]?.name ?: def.loc
        val shifts = mutableListOf<String>()
        for ((fid, start) in ep.startInfluence) {
            val now = state.factionStates[fid]?.influence ?: continue
            val diff = now - start
            if (diff != 0) {
                val name = FACTION_BY_ID[fid]?.name ?: fid
                shifts.add("$name sway ${if (diff > 0) "+$diff" else "$diff"}")
            }
        }
        val recap = mutableListOf<String>()
        recap.add("It began like this: ${def.intro}")
        recap.add("✋ Your hand in it: $stepsLanded of ${def.steps.size} " +
            "beats caught — " + when {
                branch.startsWith("choice:") ->
                    "and the closing call was yours."
                branch == "town" ->
                    "the closing call went to the town."
                else ->
                    "the week went by without you."
            })
        recap.add("🎬 $outcome")
        if (shifts.isNotEmpty()) {
            recap.add("⚔️ Faction shifts: ${shifts.joinToString("; ")}.")
        }
        if (markText.isNotEmpty()) {
            recap.add("🗿 Left standing at ${ep.markLocName}: $markText")
        }
        ep.recap = recap
        ep.actPending = -1
        ep.endDayKey = state.todayKey
        ep.finished = true
        ep.wrapShown = false
        return ep
    }

    /** The holiday just ended: finalize its episode. */
    fun finishHoliday(state: GameState): EpisodeRecord? {
        val ep = airingHoliday(state) ?: return null
        val recap = mutableListOf<String>()
        recap.add("🎉 ${ep.eventTitle} came to South Park" +
            if (ep.daysRun > 1) " and stayed ${ep.daysRun} days." else ".")
        if (ep.holidayTalks > 0) {
            recap.add("💬 You celebrated with the town ${ep.holidayTalks} " +
                if (ep.holidayTalks == 1) "time." else "times.")
        }
        // v0.4.0d: if the holiday's own beat got played during
        // the run, the credits say so — the instance year is
        // read off the just-ended window, not today's holiday.
        HOLIDAYS.firstOrNull { it.id == ep.eventId }?.let { h ->
            if (HolidayBeats.isDone(state, h.id,
                    HolidayBeats.instanceYear(state, h))) {
                HolidayBeats.beatFor(h.id)?.let { beat ->
                    recap.add("🎉 You played the holiday's own beat: " +
                        "“${beat.title}”.")
                }
            }
        }
        recap.add("The decorations came down. The memories didn't.")
        ep.recap = recap
        ep.actPending = -1
        ep.endDayKey = state.todayKey
        ep.finished = true
        ep.wrapShown = false
        return ep
    }

    // ---------------- old-save migration ----------------

    /** A save written before episodes existed can be MID-EVENT (or
     *  mid-holiday): synthesize its record with the cards already
     *  "shown" so nothing re-triggers — the past is the past. New
     *  stage stings and the wrap-up still land normally. */
    fun syncAiring(state: GameState) {
        state.shenaniganId?.let { id ->
            SHENANIGAN_BY_ID[id]?.let { def -> ensureEventRecord(state, def) }
        }
        state.holidayId?.let { hid ->
            if (airingHoliday(state) == null) {
                HOLIDAYS.firstOrNull { it.id == hid }?.let { h ->
                    val ep = beginHoliday(state, h)
                    ep.titleShown = true
                }
            }
        }
    }

    /** The joined-in-progress record for the currently running event. */
    fun ensureEventRecord(state: GameState, def: ShenaniganDef): EpisodeRecord {
        airingEvent(state, def.id, state.shenaniganDay)?.let { return it }
        val (title, treatment) = titleFor(def, runIndex(state, def.id),
            ordinalOfKey(state.shenaniganDay))
        val ep = EpisodeRecord(
            key = "${def.id}@${state.shenaniganDay}",
            kind = "event",
            eventId = def.id,
            eventTitle = def.title,
            title = title,
            treatment = treatment,
            causers = def.causers,
            dayKey = state.shenaniganDay,
            dateLabel = "${state.gameMonth}/${state.gameDay}",
            stageTitles = def.stages.map { it.title },
            stageNotes = def.stages.map { it.journalNote },
            titleShown = true,   // joined mid-episode: no title card
            startInfluence = state.factionStates.mapValues { it.value.influence },
            witness = state.eventWitness
        )
        for (s in 0..state.lastStageSeen.coerceIn(0, 3)) ep.actsShown.add(s)
        state.episodes.add(ep)
        trim(state)
        return ep
    }

    // ---------------- the episode page ----------------

    /** Everything a credits page shows, for the wrap-up card and
     *  the Journal alike. [currentStage] = the stage on screen
     *  for a still-airing episode (-1 when unknown/finished). */
    fun page(ep: EpisodeRecord, currentStage: Int): EpisodePage {
        val body = mutableListOf<String>()
        if (ep.coldOpen) body.add("🎬 Cold open: you were there when it started.")
        if (ep.kind == "event") {
            for (i in ep.stageTitles.indices) {
                val mark = when {
                    ep.finished -> "✓"
                    i < currentStage -> "✓"
                    i == currentStage -> "▶"
                    else -> "·"
                }
                body.add("$mark ${actName(i)} — ${ep.stageTitles[i]}")
            }
        }
        if (ep.finished) {
            body.addAll(ep.recap)
        } else if (ep.kind == "finale") {
            // v0.5.0h: a finale's beats list like acts, but
            // the page can't know the day's progress — the
            // Journal's FINALES page carries that; here the
            // week's shape is enough.
            for (t in ep.stageTitles) body.add("· $t")
            body.add("▶ NOW PLAYING — the season is ending; be there.")
            ep.witness?.let {
                body.add("👀 $it is holding the whole thing together.")
            }
        } else if (ep.kind == "event") {
            body.add("▶ NOW PLAYING — the story is still unfolding.")
            if (ep.participation > 0) {
                body.add("✋ Your hand in it so far: ${ep.participation}/10.")
            }
        } else {
            body.add("▶ The holiday is on — go celebrate with the town.")
        }
        ep.witness?.let {
            if (!ep.finished) body.add("👀 $it is right in the middle of it.")
        }
        return EpisodePage(
            titleLine = "\"${ep.title}\"",
            subLine = ep.treatment,
            starringLine = if (ep.causers.isEmpty()) ""
                else "STARRING " + ep.causers.joinToString(" · "),
            body = body,
            footer = if (ep.finished) "THE END" else "TO BE CONTINUED…"
        )
    }

    // ---------------- serialization ----------------

    fun fromMap(m: Map<String, Any?>): EpisodeRecord? {
        val key = m["key"]?.toString() ?: return null
        fun strList(k: String): List<String> =
            (m[k] as? List<Any?>)?.map { it.toString() } ?: emptyList()
        fun intOf(k: String, def: Int = 0): Int =
            (m[k] as? Number)?.toInt() ?: def
        val ep = EpisodeRecord(
            key = key,
            kind = m["kind"]?.toString() ?: "event",
            eventId = m["eventId"]?.toString() ?: "",
            eventTitle = m["eventTitle"]?.toString() ?: "",
            title = m["title"]?.toString() ?: "",
            treatment = m["treatment"]?.toString() ?: "",
            causers = strList("causers"),
            dayKey = m["dayKey"]?.toString() ?: "",
            dateLabel = m["dateLabel"]?.toString() ?: "",
            stageTitles = strList("stageTitles"),
            stageNotes = strList("stageNotes"),
            endDayKey = m["endDayKey"]?.toString() ?: "",
            titleShown = m["titleShown"] as? Boolean ?: false,
            actPending = intOf("actPending", -1),
            coldOpen = m["coldOpen"] as? Boolean ?: false,
            startInfluence = (m["startInfluence"] as? Map<String, Any?>)
                ?.mapValues { (it.value as? Number)?.toInt() ?: 0 } ?: emptyMap(),
            outcome = m["outcome"]?.toString() ?: "",
            participation = intOf("participation"),
            witness = m["witness"]?.toString(),
            markText = m["markText"]?.toString() ?: "",
            markLocName = m["markLocName"]?.toString() ?: "",
            residueName = m["residueName"]?.toString() ?: "",
            holidayTalks = intOf("holidayTalks"),
            daysRun = intOf("daysRun", 1),
            finished = m["finished"] as? Boolean ?: false,
            wrapShown = m["wrapShown"] as? Boolean ?: false,
            recap = strList("recap"),
            climaxTier = m["climaxTier"]?.toString() ?: "",
            climaxHow = m["climaxHow"]?.toString() ?: ""
        )
        (m["actsShown"] as? List<Any?>)?.forEach {
            (it as? Number)?.toInt()?.let { s -> ep.actsShown.add(s) }
        }
        return ep
    }
}

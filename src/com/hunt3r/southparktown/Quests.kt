package com.hunt3r.southparktown

// ============================================================
// Quests — the quest tracker model (v0.1.5).
//
// The tracker has one primary quest line — the active
// shenanigans event chain with its 4 linear stages — plus
// system objectives the game's other systems naturally
// produce (gifts, trash diving, meeting visitors, making
// friends, daily collectibles). Its APPEARANCE is dynamic:
// while an event runs, the tracker (and the main-screen chip)
// reskins to the event's own QuestTheme (icon, color, header
// from the event definition); otherwise it uses the default
// Town Board theme. State persists via GameState (the event
// fields + daily counters live in the save).
// ============================================================

data class QuestObjective(
    val icon: String,
    val text: String,
    val progress: String,
    val done: Boolean
)

/** state: 0 = done, 1 = current, 2 = upcoming */
data class QuestStageLine(val title: String, val note: String, val state: Int)

data class QuestView(
    val theme: QuestTheme,
    val headline: String,
    val stages: List<QuestStageLine>,
    val currentObjective: String,
    val objectives: List<QuestObjective>
)

object Quests {

    fun activeDef(state: GameState): ShenaniganDef? =
        state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }

    fun currentStage(state: GameState): Int =
        if (state.shenaniganId == null) -1 else Shenanigans.stageFor(state.timeMin)

    fun themeOf(state: GameState): QuestTheme =
        activeDef(state)?.theme ?: DEFAULT_QUEST_THEME

    /** Compact one-line objective for the main-screen chip. */
    fun chipText(state: GameState): String = chipText(state, 0)

    /** #8 THE QUEST CHIP PULSES WITH THE TOWN: the chip rotates the
     *  current objective with live town state — a faction mood swing,
     *  the weather forecast, the holiday — in the same themed skin. */
    fun chipText(state: GameState, pulse: Int): String {
        val options = mutableListOf<String>()
        val def = activeDef(state)
        if (def != null) {
            val st = Shenanigans.stageOf(def, state.timeMin)
            options.add("${def.theme.icon} ${st.objective}")
        } else {
            options.add("📋 Explore South Park — talk, gift, dive some trash.")
        }
        val loudMood = FACTIONS.mapNotNull { f ->
            val fs = state.factionStates[f.id] ?: return@mapNotNull null
            val mi = Factions.moodIndex(f.id, fs, state.weatherId)
            if (mi >= 2 || mi <= -2) f to mi else null
        }.firstOrNull()
        if (loudMood != null) {
            options.add("⚔️ ${loudMood.first.name} is ${Factions.moodLabel(loudMood.second)} today.")
        }
        val fc = forecastWeather(state)
        if (fc != state.weatherId) {
            options.add("🔮 Turning to ${WEATHER[fc]?.label ?: fc} later — plan around it.")
        }
        if (state.holidayId != null) {
            val h = HOLIDAYS.firstOrNull { it.id == state.holidayId }
            if (h != null) options.add("🎉 ${h.name} in South Park today!")
        }
        if (def != null && state.participation > 0) {
            options.add("${def.theme.icon} Your hand in it: ${state.participation}/10.")
        }
        return options[((pulse % options.size) + options.size) % options.size]
    }

    fun build(state: GameState): QuestView {
        val def = activeDef(state)
        val stages = mutableListOf<QuestStageLine>()
        var currentObjective = "Explore South Park. Something always happens eventually."
        if (def != null) {
            val cur = Shenanigans.stageFor(state.timeMin)
            def.stages.forEachIndexed { i, s ->
                stages.add(QuestStageLine(
                    title = s.title,
                    note = s.journalNote,
                    state = when { i < cur -> 0; i == cur -> 1; else -> 2 }
                ))
            }
            currentObjective = def.stages[cur].objective
        }
        // #31: a holiday with no event still shows as a story line.
        if (def == null && state.holidayId != null) {
            val h = HOLIDAYS.firstOrNull { it.id == state.holidayId }
            if (h != null) {
                stages.add(QuestStageLine("🎉 ${h.name}",
                    "A holiday runs through the same town machinery today — banners, memories and all.", 1))
                currentObjective = "Celebrate ${h.name} with the town."
            }
        }
        val objectives = mutableListOf<QuestObjective>()

        // Relationship milestone: friends made across cast + generated NPCs.
        val castFriends = KID_ORDER.count {
            Relationships.tierOf(state.friendship[it] ?: 0) >= Relationships.FRIEND
        }
        val genFriends = NpcMemoryStore.all().values.count {
            Relationships.tierOf(it.points) >= Relationships.FRIEND
        }
        val friends = castFriends + genFriends
        objectives.add(QuestObjective("💛", "Make friends (Friend tier or better)",
            "$friends/3", friends >= 3))

        val gifts = state.questCounters["gifts"] ?: 0
        objectives.add(QuestObjective("🎁", "Give a gift to someone today",
            "$gifts/1", gifts >= 1))

        val trash = state.questCounters["trash"] ?: 0
        objectives.add(QuestObjective("🗑️", "Search trash containers today",
            "$trash/2", trash >= 2))

        val met = state.questCounters["visitorsMet"] ?: 0
        objectives.add(QuestObjective("🧳", "Meet visitors from the lorebook today",
            "$met/2", met >= 2))

        // #22 OBJECTIVES NAME NAMES: point at one specific generated
        // resident by name and role, not an anonymous category.
        val residents = VisitorSystem.residents()
        if (residents.isNotEmpty()) {
            val target = residents[state.absDay % residents.size]
            val metTarget = (NpcMemoryStore.get(target.loreName)?.meets ?: 0) > 0
            val roleTxt = target.role?.let { "the $it" } ?: "a local"
            objectives.add(QuestObjective("🧑", "Meet ${target.loreName} ($roleTxt)",
                if (metTarget) "met!" else "not yet", metTarget))
        }

        // #33: your hand in the active event is a tracked objective.
        if (def != null) {
            val stg = Shenanigans.stageOf(def, state.timeMin)
            val at = stg.clusterLoc?.let { LOCATIONS[it]?.name } ?: "the action"
            objectives.add(QuestObjective("✋", "Get your hands dirty at $at (talk, gift, clean up)",
                "${state.participation}/5", state.participation >= 5))
        }

        // #31: holidays are objectives in the same tracker skin.
        if (state.holidayId != null) {
            val h = HOLIDAYS.firstOrNull { it.id == state.holidayId }
            val talks = state.questCounters["holidayTalks"] ?: 0
            objectives.add(QuestObjective("🎉", "Celebrate ${h?.name ?: "the holiday"} — talk with people today",
                "$talks/2", talks >= 2))
        }

        val foundToday = state.questCounters["shinies"] ?: 0
        objectives.add(QuestObjective("✨", "Find shiny collectibles today",
            "$foundToday/5", foundToday >= 5))

        val litter = state.questCounters["litter"] ?: 0
        objectives.add(QuestObjective("🧤", "Pick up litter (keep the town... tidy-ish)",
            "$litter/3", litter >= 3))

        return QuestView(
            theme = themeOf(state),
            headline = def?.let { "${it.title} — ${it.cause}" }
                ?: "No shenanigans today. Suspiciously quiet.",
            stages = stages,
            currentObjective = currentObjective,
            objectives = objectives
        )
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Goals — NPC goals (v0.1.9e, simulation depth).
//
// EVERY NPC — the 32 cast, generated residents, and even the
// rotating visitors — carries a current short-term goal: an
// errand, a meet-up, a small project, a grudge, a craving, a
// shift, a scheme. Goals are DERIVED from who the NPC is:
//   cast      -> their profile description keywords, faction,
//                lorebook relations, home and agenda places
//   generated -> their persona's traits, occupation, faction,
//                home / workplace / hangout
// and they DO things:
//   - schedules bend toward the goal's place (Engine's living
//     transform for the cast; the resident placement and the
//     visitor rotation for generated folks),
//   - NPCs talk about them (ambient lines, player dialogue,
//     greetings), 
//   - standing at the goal place (or meeting the goal person)
//     makes progress; finishing writes a memory trace and a
//     ticker line,
//   - goals live on a 2-in-game-day cycle, then expire and a
//     fresh one is derived — nobody pursues the same errand
//     forever.
//
// Identity is a PURE function of (npc key, cycle): the Journal,
// the renderer, the dialogue brains and this engine all derive
// the same goal for the same NPC on the same days without any
// coordination. Progress/completion is the only state, kept in
// GameState.npcGoals (additive save key) as a working copy of
// the derived template.
// ============================================================

data class NpcGoal(
    val npcKey: String,
    val cycle: Int,               // 2-day cycle index this goal belongs to
    val kind: String,             // ERRAND | MEETUP | PROJECT | GRUDGE | CRAVING | SCHEME | WORK | FACTION
    val text: String,             // "Currently:" — what they're up to
    val doneText: String,         // the completed version, for traces/Journal
    val locId: String? = null,    // where it happens (loc goals)
    val personName: String? = null, // who it involves (people goals)
    val needTicks: Int = 4,       // presence ticks at the place to finish
    var progress: Int = 0,
    var done: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "npcKey" to npcKey, "cycle" to cycle, "kind" to kind,
        "text" to text, "doneText" to doneText,
        "locId" to locId, "personName" to personName,
        "needTicks" to needTicks, "progress" to progress, "done" to done
    )

    companion object {
        fun fromMap(m: Map<String, Any?>): NpcGoal? {
            val key = m["npcKey"]?.toString() ?: return null
            return NpcGoal(
                npcKey = key,
                cycle = (m["cycle"] as? Number)?.toInt() ?: 0,
                kind = m["kind"]?.toString() ?: "ERRAND",
                text = m["text"]?.toString() ?: "",
                doneText = m["doneText"]?.toString() ?: "",
                locId = m["locId"]?.toString(),
                personName = m["personName"]?.toString(),
                needTicks = (m["needTicks"] as? Number)?.toInt() ?: 4,
                progress = (m["progress"] as? Number)?.toInt() ?: 0,
                done = m["done"] as? Boolean ?: false
            )
        }
    }
}

object NpcGoals {

    /** Goals turn over every 2 in-game days. The cycle is derived
     *  from the day key alone (game epoch = Oct 1, 2026), so every
     *  reader — engine, Journal, visitor system — agrees. */
    fun cycleOf(dayKey: String): Int =
        ((ordinalOfKey(dayKey) - ordinalOfKey("20261001")) / 2).coerceAtLeast(0)

    private val ERRANDS = listOf(
        "picking up a package they've been putting off for days",
        "returning something they borrowed and definitely should have returned already",
        "buying supplies for a plan that has not been explained to anyone",
        "getting a form signed before somebody notices it was due Tuesday",
        "hunting down the good snacks before they're gone",
        "dropping off a note that absolutely cannot be a text, for reasons"
    )
    private val PROJECTS = listOf(
        "a cardboard fortress with fully working cardboard politics",
        "a snow sculpture with a strong grudge theme",
        "a lemonade-based business with no permits and big dreams",
        "an extremely serious fort, do not call it a pile of boxes",
        "a diorama nobody asked for, at museum quality",
        "a snowman army, ranked by menace"
    )
    private val FOOD_LOCS = listOf("TweekBros", "CityWok", "FreemansTacos", "Raisins", "Mall")

    /** The derived goal template for [key] in [cycle] — PURE:
     *  same key + same cycle = same goal, on every screen. */
    fun goalFor(key: String, cycle: Int): NpcGoal {
        val rnd = Random(key.hashCode().toLong() * 31 + cycle * 7919L + 17)
        val isCast = key in AGENT_PROFILES
        val persona = if (isCast) null else VisitorSystem.personaByName(key)
        // ---- identity sources ----
        val factionId = if (isCast) FACTION_OF_CAST[key] else persona?.factionId
        val relations = if (isCast) relationsOf(key) else emptyList()
        val home = if (isCast) KID_HOUSE[key] else persona?.home
        val workLoc = if (isCast) null else persona?.workLoc
        val role = if (isCast) null else persona?.role
        val traits = if (isCast) emptyList() else (persona?.traits ?: emptyList())
        val desc = if (isCast) (AGENT_PROFILES[key]?.description ?: "")
            else (persona?.bio ?: "")
        val agendaLocs = if (isCast) {
            (AGENT_PROFILES[key]?.agenda?.values?.map { it.location } ?: emptyList())
                .distinct()
        } else listOfNotNull(persona?.hangout, persona?.home, persona?.workLoc)
        val leisure = listOf("KidsPark", "Mall", "StarksPond", "MainStreet",
            "TweekBros", "Bijou", "SoDoSoPa")
        val placePool = (agendaLocs + leisure).distinct()
            .filter { it in LOCATIONS && it != home }

        fun place(): String =
            placePool.getOrElse(rnd.nextInt(placePool.size.coerceAtLeast(1))) { "MainStreet" }
        fun locName(id: String) = LOCATIONS[id]?.name ?: id

        // ---- kind selection: who they are weights the draw ----
        val kinds = mutableListOf("ERRAND", "PROJECT", "CRAVING")
        if (relations.isNotEmpty()) { kinds += "MEETUP"; kinds += "GRUDGE" }
        if (factionId != null) kinds += "FACTION"
        if (workLoc != null || role != null) { kinds += "WORK"; kinds += "WORK" }
        if ("greedy" in traits || "sneaky" in traits || "scheme" in desc.lowercase() ||
            key == "Cartman" || key == "Randy") kinds += "SCHEME"
        if ("kind" in traits || "friendly" in traits) kinds += "ERRAND"
        val kind = kinds[rnd.nextInt(kinds.size)]

        // v0.2.0c: occasionally the goal IS a photo with the new
        // kid. A separate probe (its own seed stream) so adding
        // it doesn't reshuffle everyone else's derivations: the
        // goal is replaced wholesale, and it completes only via
        // NpcGoals.photoTick — a shutter click with them in it.
        val photoProbe = Random(key.hashCode().toLong() * 17 + cycle * 104729L + 5)
        if (photoProbe.nextInt(9) == 0) {
            return NpcGoal(key, cycle, "PHOTO",
                "Trying to get a photo with the new kid — for the fridge door. It's a whole project.",
                "Got the photo with the new kid. Fridge door: upgraded.",
                personName = null, needTicks = 0)
        }

        return when (kind) {
            "MEETUP" -> {
                val p = relations[rnd.nextInt(relations.size)]
                NpcGoal(key, cycle, kind,
                    "Supposed to meet $p today — if $p ever actually shows up.",
                    "Finally caught up with $p. Worth the wait, mostly.",
                    personName = p, needTicks = 0)
            }
            "GRUDGE" -> {
                val p = relations[rnd.nextInt(relations.size)]
                NpcGoal(key, cycle, kind,
                    "Holding a grudge against $p and waiting for the perfect moment.",
                    "Had it out with $p. The grudge is settled. For now.",
                    personName = p, needTicks = 0)
            }
            "CRAVING" -> {
                val loc = FOOD_LOCS[rnd.nextInt(FOOD_LOCS.size)]
                NpcGoal(key, cycle, kind,
                    "On a solemn mission for food at ${locName(loc)}.",
                    "Got the food at ${locName(loc)}. Order restored to the universe.",
                    locId = loc, needTicks = 3)
            }
            "WORK" -> {
                val loc = workLoc ?: place()
                val job = role ?: "a shift"
                NpcGoal(key, cycle, kind,
                    "Covering $job hours at ${locName(loc)}. The pay is theoretical.",
                    "Survived the shift at ${locName(loc)}. Heroic, really.",
                    locId = loc, needTicks = 6)
            }
            "FACTION" -> {
                val turf = FACTION_BY_ID[factionId]?.turf?.firstOrNull() ?: place()
                val fname = FACTION_BY_ID[factionId]?.name ?: "the faction"
                NpcGoal(key, cycle, kind,
                    "Doing quiet business for $fname at ${locName(turf)}.",
                    "Handled the $fname business at ${locName(turf)}. No further questions.",
                    locId = turf, needTicks = 5)
            }
            "SCHEME" -> {
                val loc = place()
                NpcGoal(key, cycle, kind,
                    "Plotting something at ${locName(loc)}. Nobody has been told what. That's the worrying part.",
                    "The ${locName(loc)} scheme concluded. Details remain sealed.",
                    locId = loc, needTicks = 6)
            }
            "PROJECT" -> {
                val loc = place()
                val proj = PROJECTS[rnd.nextInt(PROJECTS.size)]
                NpcGoal(key, cycle, kind,
                    "Working on a project at ${locName(loc)}: $proj.",
                    "Finished the project at ${locName(loc)}. History will judge it kindly.",
                    locId = loc, needTicks = 8)
            }
            else -> { // ERRAND
                val loc = place()
                val err = ERRANDS[rnd.nextInt(ERRANDS.size)]
                NpcGoal(key, cycle, kind,
                    "Running an errand at ${locName(loc)}: $err.",
                    "Errand at ${locName(loc)} finally done. Adulting: handled.",
                    locId = loc, needTicks = 4)
            }
        }
    }

    /** The live record for [key]: the stored working copy if it's
     *  still this cycle's goal, else a fresh copy of the template. */
    fun record(state: GameState, key: String): NpcGoal {
        val cycle = cycleOf(state.todayKey)
        val cur = state.npcGoals[key]
        if (cur != null && cur.cycle == cycle) return cur
        val fresh = goalFor(key, cycle)
        state.npcGoals[key] = fresh
        return fresh
    }

    /** Should this NPC's schedule bend toward their goal place for
     *  the block starting at [blockKey]? Deterministic per
     *  (npc, day, block) so the Journal, renderer and brain agree.
     *  Only leisure/home blocks bend — school and shifts hold. */
    fun deviationLoc(state: GameState, key: String, blockLoc: String,
                     blockKey: String?, bendable: Set<String>): String? {
        val g = record(state, key)
        if (g.done) return null
        val target = g.locId ?: return null
        if (target == blockLoc || target !in LOCATIONS) return null
        if (blockLoc !in bendable) return null
        val probe = Random(key.hashCode().toLong() * 131 +
            state.todayKey.hashCode() + (blockKey?.hashCode() ?: 0))
        return if (probe.nextDouble() < 0.45) target else null
    }

    /** Activity text worn while bent toward the goal. */
    fun deviationActivity(g: NpcGoal): String = "On a mission: ${g.text}"

    /** One tick of presence at the goal place (or contact with the
     *  goal person). Returns true when this tick FINISHED the goal. */
    fun progressTick(state: GameState, key: String): Boolean {
        val g = record(state, key)
        if (g.done) return false
        g.progress++
        if (g.needTicks > 0 && g.progress >= g.needTicks) {
            complete(state, key, g)
            return true
        }
        return false
    }

    /** v0.2.0c: PHOTO goals finish on a shutter click with
     *  this NPC in the frame (Photos.applyPhoto calls this for
     *  every photographed NPC). Returns true when it finished. */
    fun photoTick(state: GameState, key: String): Boolean {
        val g = record(state, key)
        if (g.done || g.kind != "PHOTO") return false
        complete(state, key, g)
        return true
    }

    /** People goals finish on contact, not presence. */
    fun contactTick(state: GameState, key: String, otherName: String): Boolean {
        val g = record(state, key)
        if (g.done || g.personName == null) return false
        if (g.personName != otherName) return false
        complete(state, key, g)
        return true
    }

    private fun complete(state: GameState, key: String, g: NpcGoal) {
        g.done = true
        state.events.add("✅ $key: ${g.doneText}")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🎯 $key — ${g.doneText}")
        // memory trace: cast keep it in their agent memories;
        // generated folks keep it in their character memory doc.
        state.agents[key]?.memories?.add("Goal done: ${g.doneText}")
        if (key !in (state.agents)) {
            val mem = NpcMemoryStore.getOrCreate(key)
            mem.eventNote = "Recently finished something: ${g.doneText}"
        }
    }

    /** A short spoken clause about the goal, for dialogue beats. */
    fun mention(g: NpcGoal): String {
        val loc = g.locId?.let { LOCATIONS[it]?.name ?: it }
        return when (g.kind) {
            "ERRAND" -> "I still gotta swing by $loc — errand stuff. It's a whole thing."
            "MEETUP" -> "I'm supposed to meet ${g.personName} today. If they flake again, that's IT."
            "PROJECT" -> "I've got a project going at $loc. It's important. Probably."
            "GRUDGE" -> "Don't get me started on ${g.personName} right now."
            "CRAVING" -> "I would commit minor crimes for something from $loc right now."
            "SCHEME" -> "I'm working on something at $loc. No questions."
            "WORK" -> "I'm due at $loc for a shift. Being a grown-up is a scam."
            "FACTION" -> "Faction business at $loc. You wouldn't get it."
            "PHOTO" -> "Okay so — don't make it weird — but I NEED a photo with you. For my fridge door."
            else -> "I've got stuff to do at $loc."
        }
    }

    /** The Journal card line for whoever's card this is. */
    fun journalLine(state: GameState, key: String): String {
        val g = record(state, key)
        return if (g.done) "🎯 Wrapped up: ${g.doneText}"
        else "🎯 Currently: ${g.text}"
    }

    /** Midnight housekeeping: announce a couple of quiet expiries
     *  and prune stale records for generated folks who rotated
     *  out (cast + current residents keep theirs). */
    fun dailyRollover(state: GameState) {
        val cycle = cycleOf(state.todayKey)
        val residentNames = try {
            VisitorSystem.residents().map { it.loreName }.toSet()
        } catch (e: Exception) { emptySet() }
        var announced = 0
        val it = state.npcGoals.entries.iterator()
        while (it.hasNext()) {
            val (key, g) = it.next()
            if (g.cycle >= cycle) continue
            if (!g.done && announced < 2 && key in state.agents) {
                state.events.add("⌛ $key never did finish: ${g.text.replaceFirstChar { c -> c.lowercase() }}")
                while (state.events.size > 40) state.events.removeAt(0)
                announced++
            }
            if (key !in state.agents && key !in residentNames) it.remove()
        }
    }
}

package com.hunt3r.southparktown

// ============================================================
// Faction Capstones — v0.8.0b (Queue #8 item 2): the eight
// faction chains get their endgame.
//
// FactionMissions gave every faction a three-beat chain and
// a mark. But standing kept accumulating past Respected with
// nowhere to go — the scale's top rung, Beloved (60), bought
// nothing. Now it buys the only thing left: each faction has
// ONE capstone, a second three-beat chain it only asks of
// family. The gate is CAPSTONE_MIN_STANDING — Beloved, the
// standingLabel table's own top threshold, exactly like the
// mission gates were the Liked/Respected thresholds. A
// player gets there the slow way: the chain's own 30
// standing, then +3 every time befriending a member crosses
// a friendship tier (relAdjust's nudge), +2 a road pitch,
// +3 a big event's wrap — all repeatable, none of it fast.
// Below the gate there is no menu and no offer: talk to the
// faction's senior figure and they tell you, once a day, in
// one line, that the town isn't ready to ask this of you.
//
// The capstone speaks the missions' language exactly: beats
// are offered by a member through the Talk gate's
// FACTION_MISSION rail (an armed TALK beat lands first, then
// the offer), judged ENTIRELY by the verbs the game already
// had — TALK / GIVE / LOOK / PHOTO / CATCH / SHOWDOWN /
// TACO — plus two beats judged by other systems' own
// funnels (a VOTE lands inside Votes.countWeek if your
// ballot was in the count; a JOB lands inside Jobs' clock-
// out when a shift settles) and one PHOTO that only counts
// while a Bijou screening is live (MovieNights). Offers
// wait while a mystery finale is due (Walk.caseCritical,
// the missions' silencing rule); armed beats still land.
// Beats have no fail state and no expiry.
//
// Beat 3 of every capstone is the CEREMONY: a TALK with the
// senior figure at the faction's place, in the late-
// afternoon window, while the whole faction gathers there
// (gatheringBlock rides Engine.effectiveAgendaBlock in the
// established override pattern, below the finale's). The
// exchange plays, and the mark is set in view of the player.
//
// The reward shape is ONE shape, eight times: a permanent
// town trace — a drawn dressing token at a faction place,
// claimed in Depth's BACKGROUND band exactly once, with a
// Look line — plus a title that rides the Journal's 🪪 YOU
// card beside the chain titles, plus the 🏛 CAPSTONES
// Journal page. Standing economics: a capstone beat pays NO
// standing with any faction — standing is the price of
// admission, not the prize; you're past payment, you're
// family. (Friendship still pays at the missions' rate, and
// whatever tier crossings that causes are the friendship
// system's own physics, here as everywhere.) Finishing
// costs RIVAL_COST with the faction's declared rival —
// FactionMissions.RIVAL_OF, the same perfect pairing — so
// a lifetime of chains AND capstones can cost any one
// faction at most 2 × RIVAL_COST, and standing is
// re-earnable without limit: all eight are earnable in one
// long save, just not cheaply.
//
// Memory: accepting a capstone plants TOPIC_PENDING ("the
// big ask") in every member's memory; finishing swaps it
// for TOPIC_DONE ("our capstone") — two Threads banks that
// know, between them, whether theirs is done. The morning
// report notes a completion once, the day after (the
// Secrets.morningNote pattern).
//
// State discipline: ZERO new save keys. Progress lives in
// the existing capped `life` map — "capMask:<id>" (0..7),
// "capActive:<id>" (0..3) — and marks, titles and done-ness
// DERIVE from the mask, exactly like the chains'. A
// v0.8.0a save loads with every capstone unasked.
// ============================================================

/** One judged beat of a capstone — FactionBeat's shape,
 *  plus the two funnel verbs and the screening condition.
 *  [line] is the full narration when the beat lands;
 *  [short] its ticker form. */
data class CapstoneBeat(
    val factionId: String,
    val beat: Int,               // 1..3 (3 = the ceremony)
    val title: String,
    val offerNpc: String,        // a member of the faction — Talk to them to be offered it
    val offerText: String,       // the briefing, in the offerer's voice
    val verb: String,            // TALK | GIVE | LOOK | PHOTO | CATCH | SHOWDOWN | TACO | VOTE | JOB
    val line: String,
    val short: String,
    val hint: String,            // the Journal's where-to-go line while it's armed
    val targetNpc: String? = null,  // TALK/GIVE target · SHOWDOWN named opponent
    val itemId: String? = null,     // GIVE: the item that counts
    val locId: String? = null,      // LOOK/PHOTO/CATCH place · TALK: where the target must be
    val fishId: String? = null,     // CATCH: species (null = any honest fish)
    val tacoMin: Int? = null,       // TACO: the score to post
    val minMin: Int = 0,            // time-of-day window (minutes)
    val maxMin: Int = 1440,
    val needsScreening: Boolean = false  // PHOTO: only while a Bijou screening is live
)

/** One faction's capstone: its senior figure (who fronts
 *  the below-gate hint and hosts the ceremony), the mark it
 *  leaves, and the title it confers. */
data class CapstoneChain(
    val factionId: String,
    val title: String,
    val seniorNpc: String,
    val markLoc: String,
    val markToken: String,
    val markLabel: String,       // what the mark is, player-facing
    val playerTitle: String,     // the title finishing this capstone earns
    val hintLine: String,        // the senior's below-gate line (once a day, no menu)
    val beats: List<CapstoneBeat> // exactly 3, beat numbers 1..3 in order
)

/** A capstone beat that just landed — MissionHit's shape. */
data class CapstoneHit(
    val chain: CapstoneChain,
    val beat: CapstoneBeat,
    val text: String,
    val capstoneCompletedNow: Boolean,
    val completionText: String?
)

object Capstones {

    // ---------------- the dials (all suite-pinned) ----------------

    /** The capstone gate: Beloved — Factions.standingLabel's
     *  own top threshold, so the Journal's word and the gate
     *  are the same number, as with the mission gates. */
    const val CAPSTONE_MIN_STANDING = 60

    /** Capsule coins a landed beat pays — errand scale, a
     *  touch richer at the ceremony. (Standing is never
     *  paid: see the header.) */
    val BEAT_COINS: List<Int> = listOf(3, 4, 6)

    /** Friendship a landed beat pays its target (or its
     *  offerer) — the missions' own rate. */
    const val BEAT_FRIENDSHIP = 4

    /** The ceremony's daily window: the faction gathers at
     *  its place from mid-afternoon, and the senior will
     *  only hold the rite inside it. */
    const val CEREMONY_FROM = 15 * 60
    const val CEREMONY_TO = 19 * 60

    /** The member-memory topics: the ask outstanding, and
     *  the ask answered. Threads carries a bank for each. */
    const val TOPIC_PENDING = "the big ask"
    const val TOPIC_DONE = "our capstone"

    /** The verbs a capstone beat may be judged by — the
     *  missions' seven rails, plus the vote count and the
     *  job clock-out, whose funnels judge theirs. */
    val VERBS: List<String> = listOf(
        "TALK", "GIVE", "LOOK", "PHOTO", "CATCH", "SHOWDOWN", "TACO",
        "VOTE", "JOB")

    // ---------------- state (the `life` map, nothing new) ----------------

    private fun maskKey(fid: String) = "capMask:$fid"
    private fun activeKey(fid: String) = "capActive:$fid"
    private fun hintKey(fid: String) = "capHint:$fid"
    private const val NOTE_KEY = "capNote"        // lifeText: faction ids, ";" joined
    private const val NOTE_DAY_KEY = "capNoteDay" // life: absDay of the latest completion

    /** Capstone beats of [fid] already landed, as a bitmask. */
    fun mask(state: GameState, fid: String): Int =
        (state.life[maskKey(fid)] ?: 0).coerceIn(0, 7)

    fun beatsDone(state: GameState, fid: String): Int =
        Integer.bitCount(mask(state, fid))

    fun capstoneDone(state: GameState, fid: String): Boolean =
        mask(state, fid) == 7

    fun capstonesDone(state: GameState): Int =
        CHAINS.count { capstoneDone(state, it.factionId) }

    /** The capstone beat currently taken on for [fid]. */
    fun activeBeat(state: GameState, fid: String): CapstoneBeat? {
        val n = (state.life[activeKey(fid)] ?: 0).coerceIn(0, 3)
        if (n == 0) return null
        val chain = chainById(fid) ?: return null
        val beat = chain.beats.getOrNull(n - 1) ?: return null
        return if (mask(state, fid) and (1 shl (n - 1)) == 0) beat else null
    }

    /** The next beat [fid] will offer: the first unlanded one. */
    fun nextBeat(state: GameState, fid: String): CapstoneBeat? {
        val chain = chainById(fid) ?: return null
        val m = mask(state, fid)
        return chain.beats.firstOrNull { m and (1 shl (it.beat - 1)) == 0 }
    }

    fun standingOf(state: GameState, fid: String): Int =
        FactionMissions.standingOf(state, fid)

    /** May [beat] be offered right now? The Beloved gate
     *  guards the ASKING (beat 1, nothing yet landed): once
     *  a faction has asked you, its later beats stay offered
     *  even if the town's politics have since cost you
     *  standing there — family doesn't un-ask. */
    fun gatePasses(state: GameState, beat: CapstoneBeat): Boolean {
        if (!FactionMissions.chainDone(state, beat.factionId)) return false
        if (mask(state, beat.factionId) != 0) return true
        return standingOf(state, beat.factionId) >= CAPSTONE_MIN_STANDING
    }

    // ---------------- memory (the members carry it) ----------------

    /** Plant [topic] in every member's memory, in the town's
     *  own way (Votes.plantVoteTopic's idiom): the other
     *  capstone topic is replaced, never stacked. */
    private fun plantTopic(fid: String, topic: String) {
        val f = FACTION_BY_ID[fid] ?: return
        for (name in f.members) {
            if (name !in KID_ORDER) continue
            val mem = NpcMemoryStore.getOrCreate(name)
            mem.topics.remove(TOPIC_PENDING)
            mem.topics.remove(TOPIC_DONE)
            mem.topics.add(topic)
            while (mem.topics.size > 6) mem.topics.removeAt(0)
        }
    }

    // ---------------- the below-gate hint (one line, no menu) ----------------

    /** The senior figure's honest hint, or null: [npc] fronts
     *  a faction whose chain you've finished, whose capstone
     *  hasn't been asked yet, and whose standing is short of
     *  Beloved. Once a day per faction — the stamping is the
     *  playing, like the errand thank-you in owedLines. */
    fun hintLine(state: GameState, npc: String): Line? {
        val chain = CHAINS.firstOrNull { it.seniorNpc == npc } ?: return null
        val fid = chain.factionId
        if (!FactionMissions.chainDone(state, fid)) return null
        if (capstoneDone(state, fid) || mask(state, fid) != 0) return null
        if (standingOf(state, fid) >= CAPSTONE_MIN_STANDING) return null
        if ((state.life[hintKey(fid)] ?: -9999) == state.absDay) return null
        state.life[hintKey(fid)] = state.absDay
        return Line(npc, chain.hintLine)
    }

    // ---------------- offers (the Talk gate's rail) ----------------

    /** The capstone beat [npc] would offer you right now, or
     *  null — the missions' discipline exactly: they front
     *  their faction's next beat, no beat of that faction is
     *  already taken on, the gate passes, and no mystery
     *  finale is on the table. Pure. */
    fun offerFor(state: GameState, npc: String): CapstoneBeat? {
        if (Walk.caseCritical(state)) return null
        for (chain in CHAINS) {
            val next = nextBeat(state, chain.factionId) ?: continue
            if (next.offerNpc != npc) continue
            if (activeBeat(state, chain.factionId) != null) return null
            return if (gatePasses(state, next)) next else null
        }
        return null
    }

    /** Take [beat] on. Refuses anything offerFor wouldn't
     *  offer. Taking on a faction's FIRST beat tells its
     *  members: the big ask is on the table. */
    fun accept(state: GameState, beat: CapstoneBeat): Boolean {
        if (offerFor(state, beat.offerNpc) != beat) return false
        state.life[activeKey(beat.factionId)] = beat.beat
        if (beat.beat == 1) plantTopic(beat.factionId, TOPIC_PENDING)
        return true
    }

    /** The one-line objective for an armed beat. */
    fun objective(beat: CapstoneBeat): String = when (beat.verb) {
        "TALK" -> if (beat.locId != null)
            "Talk to ${beat.targetNpc} at " +
                "${LOCATIONS[beat.locId]?.name ?: beat.locId} " +
                "(the ceremony, ${minutesToTimeStr(beat.minMin)}–" +
                "${minutesToTimeStr(beat.maxMin)})"
        else "Talk to ${beat.targetNpc}"
        "GIVE" -> "Give ${itemById(beat.itemId ?: "")?.name ?: beat.itemId} " +
            "to ${beat.targetNpc}"
        "LOOK" -> "Look carefully at ${LOCATIONS[beat.locId]?.name ?: beat.locId}" +
            timeSuffix(beat)
        "PHOTO" -> if (beat.needsScreening)
            "Take a photo at the Bijou while a screening is live " +
                "(Friday/Saturday, doors at 7 PM)"
        else "Take a photo at ${LOCATIONS[beat.locId]?.name ?: beat.locId}"
        "CATCH" -> if (beat.fishId != null)
            "Catch a ${fishById(beat.fishId)?.name ?: "fish"} at Stark's Pond"
        else "Catch any fish at Stark's Pond"
        "SHOWDOWN" -> if (beat.targetNpc != null)
            "Beat ${beat.targetNpc} at the Snowball Showdown (Kid's Park fort)"
        else "Win a Snowball Showdown (Kid's Park fort)"
        "TACO" -> "Score ${beat.tacoMin}+ at Taco Catch (Freeman's Tacos)"
        "VOTE" -> "Cast your ballot in a town vote at City Hall — " +
            "and be in the count when the week turns"
        "JOB" -> "Work one full town-job shift — clock in, work " +
            "the card, clock out"
        else -> beat.hint
    }

    private fun timeSuffix(beat: CapstoneBeat): String = when {
        beat.minMin == 0 && beat.maxMin == 1440 -> ""
        beat.maxMin == 1440 -> " (after ${minutesToTimeStr(beat.minMin)})"
        beat.minMin == 0 -> " (before ${minutesToTimeStr(beat.maxMin)})"
        else -> " (between ${minutesToTimeStr(beat.minMin)} and " +
            "${minutesToTimeStr(beat.maxMin)})"
    }

    // ---------------- the ceremony gathering ----------------

    /** While a faction's ceremony beat is taken on, its
     *  members gather at the ceremony place inside the
     *  ceremony window — the established agenda-override
     *  pattern (Engine.effectiveAgendaBlock consults this
     *  after MovieNights, before Finales, so a finale's own
     *  ensemble still wins its people). A due case empties
     *  the room, like it mutes the offers. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in CEREMONY_FROM until CEREMONY_TO) return null
        if (Walk.caseCritical(state)) return null
        for (chain in CHAINS) {
            val active = activeBeat(state, chain.factionId) ?: continue
            if (active.beat != 3) continue
            val f = FACTION_BY_ID[chain.factionId] ?: continue
            if (npcName !in f.members) continue
            return AgendaBlock(chain.markLoc,
                "Gathered at ${LOCATIONS[chain.markLoc]?.name ?: chain.markLoc} " +
                    "for ${f.name}'s capstone ceremony.")
        }
        return null
    }

    // ---------------- the judges (one per existing funnel) ----------------

    private fun timeOk(state: GameState, beat: CapstoneBeat): Boolean =
        state.timeMin >= beat.minMin && state.timeMin < beat.maxMin

    private fun activeBeats(state: GameState): List<CapstoneBeat> =
        CHAINS.mapNotNull { activeBeat(state, it.factionId) }

    /** Is a Bijou screening live on the state as it stands —
     *  a screening day, the house not dark, inside the
     *  doors-to-feature-end window (MovieNights' own dials). */
    fun screeningLive(state: GameState): Boolean =
        MovieNights.isScreeningDay(state.absDay) &&
            !MovieNights.isDarkTonight(state) &&
            state.timeMin >= MovieNights.DOORS_FROM &&
            state.timeMin < MovieNights.FEATURE_END

    /** TALK query half: an armed TALK beat a conversation
     *  with [npc] would land right now, or null. Pure. */
    fun talkBeatFor(state: GameState, npc: String): CapstoneBeat? =
        activeBeats(state).firstOrNull {
            it.verb == "TALK" && it.targetNpc == npc && timeOk(state, it) &&
                (it.locId == null ||
                    state.agents[npc]?.location == it.locId)
        }

    /** TALK judged (the Talk gate calls this, like a delivery). */
    fun judgeTalk(state: GameState, npc: String): List<CapstoneHit> {
        val beat = talkBeatFor(state, npc) ?: return emptyList()
        return listOf(completeBeat(state, beat))
    }

    /** GIVE judged: a real hand-off through the gift rail. */
    fun onGive(state: GameState, npc: String, itemId: String): List<CapstoneHit> =
        activeBeats(state).filter {
            it.verb == "GIVE" && it.targetNpc == npc && it.itemId == itemId
        }.map { completeBeat(state, it) }

    /** LOOK judged — and the marks speak here too: the text
     *  a careful Look at [locId] earns, from armed LOOK
     *  beats there (time windows included) and from any
     *  finished capstone whose trace stands at this place,
     *  or null. */
    fun lookText(state: GameState, locId: String): String? {
        val parts = mutableListOf<String>()
        val hits = activeBeats(state).filter {
            it.verb == "LOOK" && it.locId == locId && timeOk(state, it)
        }.map { completeBeat(state, it) }
        for (hit in hits) {
            parts += hit.text + (hit.completionText?.let { "\n$it" } ?: "")
        }
        for (chain in CHAINS) {
            if (chain.markLoc == locId && capstoneDone(state, chain.factionId)) {
                parts += "🏛 Standing here: ${chain.markLabel} — set the day " +
                    "${FACTION_BY_ID[chain.factionId]?.name ?: "the faction"} " +
                    "made you ${chain.playerTitle}."
            }
        }
        return if (parts.isEmpty()) null else parts.joinToString("\n")
    }

    /** PHOTO judged — called from inside Photos.applyPhoto.
     *  A screening beat counts only while the house is live:
     *  the right room on the wrong night is just a room. */
    fun onPhoto(state: GameState, rec: PhotoRecord): List<CapstoneHit> =
        activeBeats(state).filter {
            it.verb == "PHOTO" && it.locId == rec.locId &&
                (!it.needsScreening || screeningLive(state))
        }.map { completeBeat(state, it) }

    /** CATCH judged — called from Minigames.applyCatch. */
    fun onCatch(state: GameState, speciesId: String): List<CapstoneHit> =
        activeBeats(state).filter {
            it.verb == "CATCH" && (it.fishId == null || it.fishId == speciesId)
        }.map { completeBeat(state, it) }

    /** SHOWDOWN judged — called from applyShowdownResult:
     *  only a WIN lands it, against the named opponent. */
    fun onShowdown(state: GameState, won: Boolean,
                   opponent: String): List<CapstoneHit> {
        if (!won) return emptyList()
        return activeBeats(state).filter {
            it.verb == "SHOWDOWN" &&
                (it.targetNpc == null || it.targetNpc == opponent)
        }.map { completeBeat(state, it) }
    }

    /** TACO judged — called from applyTacoResult. */
    fun onTaco(state: GameState, score: Int): List<CapstoneHit> =
        activeBeats(state).filter {
            it.verb == "TACO" && score >= (it.tacoMin ?: Int.MAX_VALUE)
        }.map { completeBeat(state, it) }

    /** VOTE judged — called from inside Votes.countWeek,
     *  the count itself: an armed VOTE beat lands if the
     *  player's ballot was in the week just counted. The
     *  hit's line rides the events ticker (the count happens
     *  at rollover; there is no dialog to return it to). */
    fun onVoteCounted(state: GameState, playerVoted: Boolean): List<CapstoneHit> {
        if (!playerVoted) return emptyList()
        val hits = activeBeats(state).filter { it.verb == "VOTE" }
            .map { completeBeat(state, it) }
        for (hit in hits) {
            state.events.add("🏛 ${hit.beat.line}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
        return hits
    }

    /** JOB judged — called from inside Jobs' clock-out, at
     *  the moment a shift settles: an armed JOB beat lands
     *  on any honestly worked shift, at any of the town's
     *  three jobs. */
    fun onShiftCompleted(state: GameState): List<CapstoneHit> =
        activeBeats(state).filter { it.verb == "JOB" }
            .map { completeBeat(state, it) }

    // ---------------- completion ----------------

    /** The standing math of a landed capstone beat, alone:
     *  NOTHING with any faction for beats 1–2, and at the
     *  ceremony the rival pays RIVAL_COST — the missions'
     *  own cost, the same pairing. That is the whole ledger;
     *  this function exists so the suite can pin it. */
    fun applyCompletionStanding(state: GameState, beat: CapstoneBeat) {
        if (beat.beat != 3) return
        val rival = FactionMissions.RIVAL_OF[beat.factionId] ?: return
        val rs = state.factionStates[rival] ?: return
        rs.playerStanding = (rs.playerStanding -
            FactionMissions.RIVAL_COST).coerceIn(-50, 100)
    }

    /** Land [beat]: set its bit, disarm it, pay coins and
     *  friendship at the missions' scale — never standing —
     *  and, on the ceremony, write the capstone into the
     *  town: rival cost, log, morning note, member memory,
     *  and the completion text that names the mark. */
    fun completeBeat(state: GameState, beat: CapstoneBeat): CapstoneHit {
        val chain = chainById(beat.factionId)!!
        state.life[maskKey(beat.factionId)] =
            mask(state, beat.factionId) or (1 shl (beat.beat - 1))
        if ((state.life[activeKey(beat.factionId)] ?: 0) == beat.beat)
            state.life[activeKey(beat.factionId)] = 0
        applyCompletionStanding(state, beat)
        val coins = BEAT_COINS[beat.beat - 1]
        if (coins > 0) Minigames.awardCoins(state, coins)
        val friendName = beat.targetNpc ?: beat.offerNpc
        if (friendName in state.friendship)
            Engine().relAdjust(state, friendName, BEAT_FRIENDSHIP)
        val doneNow = capstoneDone(state, beat.factionId)
        var completionText: String? = null
        if (doneNow) {
            val rivalName = FactionMissions.RIVAL_OF[beat.factionId]
                ?.let { FACTION_BY_ID[it]?.name } ?: "a rival"
            completionText = "🏛 CAPSTONE COMPLETE — “${chain.title}” is done. " +
                "The mark stands: ${chain.markLabel}. The town has a title " +
                "for you now: ${chain.playerTitle}. (${rivalName} noticed: " +
                "-${FactionMissions.RIVAL_COST} standing there. Worth it.)"
            townLogLine(state, "🏛 ${chain.title} — ${FACTION_BY_ID[beat.factionId]?.name}: " +
                "the new kid is ${chain.playerTitle} now; ${chain.markLabel} stands.")
            // the morning report hears about it tomorrow, once
            val prior = state.lifeText[NOTE_KEY]
                ?.split(";")?.filter { it.isNotBlank() } ?: emptyList()
            state.lifeText[NOTE_KEY] = (prior + beat.factionId).joinToString(";")
            state.life[NOTE_DAY_KEY] = state.absDay
            plantTopic(beat.factionId, TOPIC_DONE)
        }
        state.events.add(beat.short)
        while (state.events.size > 40) state.events.removeAt(0)
        return CapstoneHit(chain, beat, beat.line, doneNow, completionText)
    }

    // ---------------- the morning after ----------------

    /** The morning report's one line for a capstone that
     *  completed YESTERDAY (the Secrets pattern: a stamped
     *  day and a queued note, read once by the calendar). */
    fun morningNote(state: GameState): String? {
        if ((state.life[NOTE_DAY_KEY] ?: -999) != state.absDay - 1) return null
        val fids = state.lifeText[NOTE_KEY]
            ?.split(";")?.filter { it.isNotBlank() } ?: return null
        if (fids.isEmpty()) return null
        val parts = fids.mapNotNull { fid ->
            val chain = chainById(fid) ?: return@mapNotNull null
            val f = FACTION_BY_ID[fid] ?: return@mapNotNull null
            "${f.name} made you ${chain.playerTitle} — ${chain.markLabel} stands"
        }
        if (parts.isEmpty()) return null
        return "🏛 Yesterday, a capstone was set: ${parts.joinToString("; ")}. " +
            "It's written up in your Journal — the town is still saying the title."
    }

    // ---------------- marks & titles (all derived) ----------------

    /** The permanent capstone traces standing right now,
     *  locId -> tokens — derived whole from finished
     *  capstones, so a trace appears exactly when its
     *  ceremony lands and can never be lost. SceneArt's
     *  mark layer draws these (GameActivity merges them
     *  into TownInfo.factionMarkTokens with the chains'). */
    fun markTokensByLoc(state: GameState): Map<String, List<String>> {
        val out = linkedMapOf<String, MutableList<String>>()
        for (chain in CHAINS) {
            if (capstoneDone(state, chain.factionId))
                out.getOrPut(chain.markLoc) { mutableListOf() }
                    .add(chain.markToken)
        }
        return out
    }

    /** Every trace token the system can ever leave. */
    val MARK_TOKENS: Set<String> get() = CHAINS.map { it.markToken }.toSet()

    /** Capstone titles earned so far, in faction order —
     *  the 🪪 YOU card carries these beside the chain titles. */
    fun titlesEarned(state: GameState): List<String> =
        CHAINS.filter { capstoneDone(state, it.factionId) }
            .map { "${FACTION_BY_ID[it.factionId]?.icon ?: "🏛"} ${it.playerTitle}" }

    // ---------------- the Journal page ----------------

    /** The 🏛 CAPSTONES page: every faction, its chain's
     *  state, and its big ask — where it stands, what its
     *  next beat wants, the gate named when standing is
     *  short, and — once done — its mark and title. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val done = capstonesDone(state)
        out += "🏛 CAPSTONES — " +
            if (done == 0) "past the chains, every faction keeps one big ask " +
                "for family only — Beloved standing, a finished chain, and nerve"
            else "$done of ${CHAINS.size} capstones set; the town says the titles"
        for (chain in CHAINS) {
            val f = FACTION_BY_ID[chain.factionId] ?: continue
            val standing = standingOf(state, chain.factionId)
            when {
                capstoneDone(state, chain.factionId) ->
                    out += "✓ ${f.icon} ${f.name} — “${chain.title}”: DONE. " +
                        "${chain.markLabel} stands at " +
                        "${LOCATIONS[chain.markLoc]?.name ?: chain.markLoc}. " +
                        "Title: ${chain.playerTitle}."
                !FactionMissions.chainDone(state, chain.factionId) ->
                    out += "· ${f.icon} ${f.name} — their chain comes first: " +
                        "finish “${FactionMissions.chainById(chain.factionId)?.title}” " +
                        "before anyone speaks of capstones."
                else -> {
                    val active = activeBeat(state, chain.factionId)
                    val next = nextBeat(state, chain.factionId)
                    if (active != null) {
                        out += "▶ ${f.icon} ${f.name} — “${chain.title}”: beat " +
                            "${active.beat} of 3 taken on — ${objective(active)}. " +
                            active.hint
                    } else if (next != null) {
                        val gateNote = if (gatePasses(state, next)) ""
                        else " (wants ${Factions.standingLabel(CAPSTONE_MIN_STANDING)} " +
                            "standing with them — you're at $standing; their chain, " +
                            "their people's friendship, the road and the big days " +
                            "move it)"
                        out += "· ${f.icon} ${f.name} — “${chain.title}”: beat " +
                            "${next.beat} of 3 waits with ${next.offerNpc}$gateNote."
                    }
                }
            }
        }
        return out
    }

    fun chainById(fid: String): CapstoneChain? =
        CHAINS.firstOrNull { it.factionId == fid }

    // ---------------- the eight capstones ----------------

    val CHAINS: List<CapstoneChain> = listOf(

        // ---------- THE BOYS' CREW: the premiere print ----------
        CapstoneChain("boys_crew", "The Premiere Print",
            seniorNpc = "Cartman",
            markLoc = "StarksPond", markToken = "premiere-post",
            markLabel = "the Crew's framed premiere print on the pond signpost",
            playerTitle = "First Name in the Credits",
            hintLine = "There's one more thing we'd ask. The BIG thing. We only ask " +
                "family, and you're not — look, the town isn't ready to ask it of " +
                "you yet. Get beloved first. Then we'll talk about the movie.",
            beats = listOf(
                CapstoneBeat("boys_crew", 1, "A Director Is Named",
                    offerNpc = "Cartman",
                    offerText = "Cartman waits until the others are distracted, which " +
                        "takes four seconds. “Okay. The BIG ask. The Crew is making a " +
                        "movie — a real one, for the Bijou — and I can't direct it " +
                        "because I'm STARRING in it, which is a conflict of genius. " +
                        "Stan directs. He's seen a movie with THEMES. Talk to Stan. " +
                        "Tell him it's already decided.”",
                    verb = "TALK",
                    line = "Stan hears 'you're directing' the way other kids hear " +
                        "'you're grounded.' “Who decided — ” He stops. He looks at " +
                        "the pond, at the flag, at you. “…Fine. FINE. But I'm not " +
                        "wearing the beret Cartman bought. There IS a beret, isn't " +
                        "there.” There is. The production has a director.",
                    short = "🧢 The production has a director. There is a beret.",
                    hint = "Find Stan and talk to him — he's directing, it's decided.",
                    targetNpc = "Stan"),
                CapstoneBeat("boys_crew", 2, "The Premiere Still",
                    offerNpc = "Stan",
                    offerText = "Stan is storyboarding on a napkin with the focus of " +
                        "a kid defusing something. “Every real movie has ONE shot — " +
                        "the still they frame. Ours gets taken at the Bijou, during " +
                        "an actual screening, while the house is live. Doors at " +
                        "seven, Friday or Saturday. You take the photo; the light " +
                        "does the rest. Don't make it weird. It'll be weird.”",
                    verb = "PHOTO",
                    line = "📸 The shutter goes off in the dark of the Bijou and " +
                        "catches the whole beautiful mess — the screen glowing, " +
                        "the crew mid-something, Kenny asleep upright in the " +
                        "third row like a king. Stan looks at it for a long time. " +
                        "“…That's the one,” he says. “Frame that. That's the " +
                        "movie, whether the movie's good or not.”",
                    short = "🧢 The premiere still is THE one. Frame it.",
                    hint = "Take a photo at the Bijou while a screening is live — " +
                        "Friday or Saturday, after the 7 PM doors.",
                    locId = "Bijou", needsScreening = true),
                CapstoneBeat("boys_crew", 3, "The Framing at the Pond",
                    offerNpc = "Cartman",
                    offerText = "Cartman produces the framed print from behind his " +
                        "back like a magician with a permit. “Last step. The print " +
                        "goes up at OUR place — the pond, under the claim flag, " +
                        "where everybody has to look at it forever. Late afternoon. " +
                        "The whole crew. You stand where I tell you. (The Girls' " +
                        "Circle will have OPINIONS about the framing. It'll cost " +
                        "us a little peace there. Art costs.)”",
                    verb = "TALK",
                    line = "THE FRAMING AT THE POND — the whole crew is there, " +
                        "which never happens by accident. Cartman holds the print " +
                        "against the signpost, under the claim flag. Kyle levels " +
                        "it with his phone. Kenny holds the nail. Stan says " +
                        "nothing, which for Stan is a speech. The nail goes in. " +
                        "“Credits,” Cartman announces to the pond, “are " +
                        "permanent.” Your name is first. He checked twice.",
                    short = "🧢 The print is framed at the pond. Credits are permanent.",
                    hint = "Meet Cartman at Stark's Pond in the late afternoon " +
                        "(3–7 PM) — the whole crew will be there.",
                    targetNpc = "Cartman", locId = "StarksPond",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- THE GOTH KIDS: the town's obituary ----------
        CapstoneChain("goth_kids", "The Town's Obituary",
            seniorNpc = "Michael",
            markLoc = "Elementary", markToken = "goth-broadsheet",
            markLabel = "the Goths' framed obituary broadsheet behind the school",
            playerTitle = "Poet Laureate of the End",
            hintLine = "There's a last work. We don't offer it to the merely " +
                "tolerated. When the town loves you — actually loves you, which " +
                "is disgusting — come back. We'll be here. We're always here.",
            beats = listOf(
                CapstoneBeat("goth_kids", 1, "Study the Opposition",
                    offerNpc = "Henrietta",
                    offerText = "Henrietta opens the black book to a blank page, " +
                        "which is the goth equivalent of a drumroll. “The capstone. " +
                        "We're writing the town's obituary. Not because it's dying — " +
                        "because everything dies, and somebody should get the " +
                        "words right BEFORE the rush. First: research. Go to the " +
                        "Church and LOOK at how the opposition writes about " +
                        "endings. Steal their structure. Change nothing else.”",
                    verb = "LOOK",
                    line = "The hymn board by the door lists the week's endings in " +
                        "a cheerful hand: verses, times, a bake sale. It's all so " +
                        "organized. So unafraid. You take notes the goth way — " +
                        "slowly, from the back, in pencil that gets darker as it " +
                        "goes. The opposition's structure: stolen. Their hope: " +
                        "untouched, unfortunately.",
                    short = "🦇 Research done: the opposition is organized and unafraid.",
                    hint = "Look carefully at the Church — the hymn board by the door.",
                    locId = "Church"),
                CapstoneBeat("goth_kids", 2, "The Youngest Writes the Last Line",
                    offerNpc = "Pete",
                    offerText = "Pete flips the hair out of one eye, which is how " +
                        "you know it's serious. “The obituary's written except the " +
                        "last line, and by law — OUR law — the last line belongs " +
                        "to the youngest. Firkle. Talk to Firkle. He'll pretend " +
                        "he hasn't already written it. He has. It's devastating.”",
                    verb = "TALK",
                    line = "Firkle pretends for exactly one sigh. Then, from the " +
                        "small black notebook: “'It was a town. It had a pond. " +
                        "Everyone pretended the pond was the point.'” He closes " +
                        "the notebook. Somewhere, a conformist poet laureate " +
                        "feels a chill and doesn't know why. The obituary is " +
                        "complete. It was always going to end like that.",
                    short = "🦇 The last line is in. It's devastating.",
                    hint = "Find Firkle and talk to him — the last line is his.",
                    targetNpc = "Firkle"),
                CapstoneBeat("goth_kids", 3, "The Reading Behind the School",
                    offerNpc = "Michael",
                    offerText = "Michael regards the finished obituary like weather. " +
                        "“It gets read once, aloud, where we read everything that " +
                        "matters: behind the school, late afternoon, the light " +
                        "going. Then it goes in the frame by the mural, forever. " +
                        "Be there. Dress like yourself; it's already funeral " +
                        "enough. (The Church will call it morbid. It'll cost us " +
                        "a little light with them — Maxi keeps that ledger. " +
                        "Everything does.)”",
                    verb = "TALK",
                    line = "THE READING — all four goths behind the school, the " +
                        "light going down on cue like it was hired. Michael reads " +
                        "the obituary start to finish without looking up. Henrietta " +
                        "doesn't smoke out of respect, which alarms everyone. Pete " +
                        "nods at the commas. Firkle mouths his own last line. Then " +
                        "the frame goes up by the mural — black border, white " +
                        "paper, the town's whole story — and nobody, anywhere, " +
                        "applauds. It is the best review it could have gotten.",
                    short = "🦇 The obituary is read and framed. No applause. Perfect.",
                    hint = "Meet Michael behind the school (South Park Elementary) " +
                        "in the late afternoon (3–7 PM) — all four will be there.",
                    targetNpc = "Michael", locId = "Elementary",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- THE GIRLS' CIRCLE: the charter ----------
        CapstoneChain("girls_circle", "The Charter",
            seniorNpc = "Wendy",
            markLoc = "Mall", markToken = "circle-charter",
            markLabel = "the Circle's framed charter at the Mall",
            playerTitle = "Charter Signatory",
            hintLine = "There is a final document, and it is not discussed with " +
                "people the town merely likes. Beloved, or it isn't mentioned " +
                "again. Work on that. The columns are watching.",
            beats = listOf(
                CapstoneBeat("girls_circle", 1, "The Final Wording",
                    offerNpc = "Wendy",
                    offerText = "Wendy lowers her voice, and the Mall gets quieter " +
                        "out of respect. “The big ask, since you're family now: " +
                        "the Circle is writing a CHARTER. Not a list — a charter. " +
                        "What we are, what we protect, who's in the good column " +
                        "forever. Bebe holds the final wording and she surrenders " +
                        "paper to no one over a fence. Talk to Bebe.”",
                    verb = "TALK",
                    line = "Bebe reads the final wording aloud from the good folder, " +
                        "slowly, like a treaty being test-driven. “…'and the good " +
                        "column is earned, and it is PERMANENT.'” She looks up. " +
                        "“That's the whole charter, really. The rest is margins.” " +
                        "The wording is final. The margins are immaculate.",
                    short = "💄 The final wording is read. The margins are immaculate.",
                    hint = "Find Bebe and talk to her — she holds the wording.",
                    targetNpc = "Bebe"),
                CapstoneBeat("girls_circle", 2, "Ratified by the Town",
                    offerNpc = "Bebe",
                    offerText = "Bebe seals nothing — she wants witnesses. “A " +
                        "charter isn't real until the TOWN has touched it, and " +
                        "in this town that means a vote. Any vote, any week — " +
                        "your pick, your conscience. Cast your ballot at City " +
                        "Hall and be in the count when the week turns. Your " +
                        "name in a count, on the record. That's ratification.”",
                    verb = "VOTE",
                    line = "The count comes in at the week's turn, and your name " +
                        "is in it — cast, weighed, written down in City Hall's " +
                        "hand. Bebe receives the news like a notary. “Ratified,” " +
                        "she says, and stamps the charter with the Circle's seal, " +
                        "which is a lipstick print and legally binding in at " +
                        "least one jurisdiction: this one.",
                    short = "💄 Ratified — your name is in the count, on the record.",
                    hint = "Vote at the ballot box inside City Hall, then let the " +
                        "week turn — the count lands at the week's first morning.",
                    locId = "CityHall"),
                CapstoneBeat("girls_circle", 3, "The Framing at the Mall",
                    offerNpc = "Wendy",
                    offerText = "Wendy has the charter in a frame and the frame in " +
                        "both hands, which for Wendy is ceremony dress. “It hangs " +
                        "beside the list board at the Mall, late afternoon, when " +
                        "the light's honest and the dinner crowd can witness. " +
                        "The whole Circle. You sign where I point. (The Boys will " +
                        "photograph it and pretend they didn't. It'll cost us a " +
                        "little peace there. Peace is renewable.)”",
                    verb = "TALK",
                    line = "THE FRAMING — the whole Circle at the Mall, the list " +
                        "board on one side, the frame going up level on the other. " +
                        "Wendy holds it. Heidi steadies it. Nichole says nothing " +
                        "and somehow officiates. You sign where Wendy points — " +
                        "the last line, under 'witnessed and held permanent.' A " +
                        "passing mom reads the charter, then the list, then " +
                        "nods once at each. Two documents. One law.",
                    short = "💄 The charter hangs beside the List. Two documents, one law.",
                    hint = "Meet Wendy at the Mall in the late afternoon (3–7 PM) — " +
                        "the whole Circle will be there.",
                    targetNpc = "Wendy", locId = "Mall",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- CRAIG'S GANG: the long whatever ----------
        CapstoneChain("craigs_gang", "The Long Whatever",
            seniorNpc = "Craig",
            markLoc = "KidsPark", markToken = "gang-totem",
            markLabel = "Craig's Gang's stacked-crate totem at the park fort",
            playerTitle = "Whatever, Emeritus",
            hintLine = "There's a thing we'd ask family. You're not family yet. " +
                "Town doesn't love you enough. Whatever. Come back when it does.",
            beats = listOf(
                CapstoneBeat("craigs_gang", 1, "Beat the Champion",
                    offerNpc = "Craig",
                    offerText = "Craig looks up, which means paperwork somewhere " +
                        "just caught fire. “The big ask. The Long Whatever. It " +
                        "starts how everything starts: you beat our champion at " +
                        "the fort. Tolkien. He's champion because he owns a " +
                        "coat with actual pockets, but also he's good. Beat " +
                        "him. Then we'll talk about the fish.”",
                    verb = "SHOWDOWN",
                    line = "Tolkien goes down in the snow with the dignity of a " +
                        "kid who owns a coat with actual pockets, and the gang " +
                        "marks the moment the way it marks everything: Clyde " +
                        "says 'huh.' Tweek vibrates at a congratulatory " +
                        "frequency. Craig closes his eyes, which is the gang's " +
                        "trophy ceremony. Champion: beaten. The Long Whatever " +
                        "has begun.",
                    short = "👆 Champion beaten. The gang's trophy ceremony was held.",
                    hint = "Win the Snowball Showdown against Tolkien at the " +
                        "Kid's Park fort.",
                    targetNpc = "Tolkien"),
                CapstoneBeat("craigs_gang", 2, "The Fish That Doesn't Care",
                    offerNpc = "Clyde",
                    offerText = "Clyde is holding a landing net like it's a " +
                        "diplomatic credential. “Phase two. The pond has one " +
                        "fish that truly, completely does not care — the golden " +
                        "carp. Catch it. Hold it. Let it go. The Whatever " +
                        "requires a witness who can't be impressed, and it's " +
                        "the only one in town.” It takes as long as it takes. " +
                        "That's the point, apparently.",
                    verb = "CATCH",
                    line = "The golden carp comes up slow, gleaming, radiating " +
                        "the energy of a creature that has never once checked " +
                        "the time. You hold it. It regards you the way Craig " +
                        "regards everything. You let it go. Clyde nods once. " +
                        "“Witnessed,” he says, “by the only honest judge in " +
                        "the county.” Phase two: complete.",
                    short = "👆 The carp witnessed it. It did not care. Phase two done.",
                    hint = "Fish at Stark's Pond until the golden carp bites — " +
                        "it's rare, and it is in no hurry.",
                    fishId = "golden_carp"),
                CapstoneBeat("craigs_gang", 3, "The Stacking at the Fort",
                    offerNpc = "Craig",
                    offerText = "Craig stands, which nobody has ever seen and " +
                        "history will note. “Last phase. We stack the totem at " +
                        "the fort — crates, one per member, late afternoon, " +
                        "whole gang. You stack yours last. On top. Don't make " +
                        "it a thing. (The School Staff will write the totem up " +
                        "as a structure. It'll cost us a little peace with " +
                        "them. It's not a structure. It's a statement.)”",
                    verb = "TALK",
                    line = "THE STACKING — the whole gang at the fort, each crate " +
                        "going up with its owner's entire review of the process, " +
                        "which is silence. Craig's: silence. Tweek's: vibrating " +
                        "silence. Tolkien's: pocketed silence. Yours goes last, " +
                        "on top, and the totem stands there being the most " +
                        "unbothered object in the county. Nobody applauds. " +
                        "Somebody — reports are unclear — almost smiles. The " +
                        "Long Whatever is complete, and it is never, ever " +
                        "explained.",
                    short = "👆 The totem stands. Nobody applauded. It is never explained.",
                    hint = "Meet Craig at Kid's Park in the late afternoon (3–7 PM) — " +
                        "the whole gang will be there.",
                    targetNpc = "Craig", locId = "KidsPark",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- THE PARENTS: the watch, official ----------
        CapstoneChain("parents", "The Watch, Official",
            seniorNpc = "McDaniels",
            markLoc = "SkeetersBar", markToken = "watch-bell",
            markLabel = "the Neighborhood Watch bell over Skeeter's bar",
            playerTitle = "Watch Captain",
            hintLine = "The Watch keeps one commission for its own, and I do not " +
                "discuss it with residents the town merely approves of. Beloved " +
                "standing, dear. Then City Hall will have a word.",
            beats = listOf(
                CapstoneBeat("parents", 1, "The Roster",
                    offerNpc = "McDaniels",
                    offerText = "Mayor McDaniels slides a single sheet of paper " +
                        "across the desk like it weighs something. “The big " +
                        "commission, since the town insists on loving you: the " +
                        "Watch goes OFFICIAL. Rosters, a bell, a captain. Sharon " +
                        "holds the roster — nothing in this town is official " +
                        "until Sharon's clipboard says so. Talk to Sharon.”",
                    verb = "TALK",
                    line = "Sharon reviews the roster with the stillness of a " +
                        "woman proofreading the town itself. “Watch Captain,” " +
                        "she reads, “— one vacancy.” She looks at you over the " +
                        "clipboard for exactly as long as it takes. “The " +
                        "clipboard accepts the nomination,” she says, and " +
                        "writes your name in her best hand. It is, now, a fact " +
                        "with handwriting.",
                    short = "🍷 The clipboard accepts the nomination. It's a fact now.",
                    hint = "Find Sharon and talk to her — the roster is hers.",
                    targetNpc = "Sharon"),
                CapstoneBeat("parents", 2, "Show Up, On the Record",
                    offerNpc = "Sharon",
                    offerText = "Sharon taps the roster once. “The Watch trusts " +
                        "people who show up — on paper, on time, for a whole " +
                        "shift. Any of the town's jobs: the route, the farm, a " +
                        "counter. Clock in, work the card, clock out clean, " +
                        "once, while this roster's open. The town's employers " +
                        "keep the record. The Watch reads records.”",
                    verb = "JOB",
                    line = "The shift closes out clean — card worked, boss " +
                        "satisfied, name written in somebody's book the way " +
                        "reliable people's names get written. Sharon receives " +
                        "the report without looking up from the roster. " +
                        "“Shows up,” she notes, in the margin, in her best " +
                        "hand. “On the record.” The roster is satisfied.",
                    short = "🍷 Shift worked, name in the book. The roster is satisfied.",
                    hint = "Take any town job, clock in on a shift day, finish the " +
                        "card and clock out — one full shift, on the record.",
                    locId = null),
                CapstoneBeat("parents", 3, "The Hanging of the Bell",
                    offerNpc = "McDaniels",
                    offerText = "McDaniels has acquired a bell and the air of a " +
                        "woman dedicating a bridge. “Last step. The Watch bell " +
                        "hangs over Skeeter's — the Watch's unofficial " +
                        "headquarters since before it was official anything — " +
                        "late afternoon, every parent in town in attendance, " +
                        "which is to say Skeeter's regular hours. You pull the " +
                        "first ring. (Tegridy will call it a noise ordinance " +
                        "violation. It'll cost us a little goodwill out there. " +
                        "Randy can file a complaint like everyone else.)”",
                    verb = "TALK",
                    line = "THE HANGING OF THE BELL — Skeeter's, late light, the " +
                        "whole parent town wedged in with their glasses down. " +
                        "Skeeter holds the ladder and says nothing, which for " +
                        "Skeeter is a speech. Sharon checks the roster once, " +
                        "finally, and closes the clipboard. McDaniels nods at " +
                        "you. You pull the rope. The bell rings ONCE over Main " +
                        "Street's end of town — clear, official, slightly too " +
                        "loud — and every parent in the room pretends they " +
                        "weren't waiting all week to hear it. The Watch is " +
                        "official. The bell stays.",
                    short = "🍷 The bell rang once. The Watch is official. It stays.",
                    hint = "Meet Mayor McDaniels at Skeeter's Bar in the late " +
                        "afternoon (3–7 PM) — every parent in town will be there.",
                    targetNpc = "McDaniels", locId = "SkeetersBar",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- SCHOOL STAFF: the bell, re-cast ----------
        CapstoneChain("school_staff", "The Bell, Re-Cast",
            seniorNpc = "Victoria",
            markLoc = "Elementary", markToken = "staff-bell",
            markLabel = "the Staff's re-cast brass bell by the Elementary door",
            playerTitle = "Keeper of the Bell",
            hintLine = "The staff keeps one trust it does not name to students the " +
                "school merely approves of. When this town holds you beloved, " +
                "come to my office. Not before. The bell can wait; it is, " +
                "technically, broken.",
            beats = listOf(
                CapstoneBeat("school_staff", 1, "Inspect the Housing",
                    offerNpc = "Victoria",
                    offerText = "Principal Victoria does not look up from the " +
                        "file, which is how you know you've been summoned for " +
                        "weeks. “The big trust, since the town insists: the " +
                        "school bell is CRACKED. It has been cracked since " +
                        "before you arrived and it will be re-cast. First, an " +
                        "inspection of the housing, by someone the building " +
                        "trusts. Look at the school — the bell housing, " +
                        "carefully. Report by existing.”",
                    verb = "LOOK",
                    line = "The bell housing gives up its secrets to a careful " +
                        "look: the crack runs like a river system, the mount " +
                        "has been shimmed with what appears to be a folded " +
                        "detention slip from another decade, and somebody — " +
                        "Chef, the smart money says — has been oiling a bell " +
                        "that cannot ring true for years, out of loyalty. You " +
                        "report by existing, exactly as instructed. Victoria " +
                        "nods once. The file gains a page.",
                    short = "🏫 Inspection reported, by existing. The file gains a page.",
                    hint = "Look carefully at South Park Elementary — the bell " +
                        "housing, up by the door.",
                    locId = "Elementary"),
                CapstoneBeat("school_staff", 2, "The Re-Casting Fund",
                    offerNpc = "Chef",
                    offerText = "Chef leans in with the ladle and the ledger. " +
                        "“Hello there, children. Re-casting a bell takes brass, " +
                        "and brass takes a FUND, and this school funds things " +
                        "the honest way: tacos. The Taco Catch cabinet at " +
                        "Freeman's — post 240, the posted maximum, and the " +
                        "fund is full and the bell is as good as cast. " +
                        "Children've been savin' their appetites for this.”",
                    verb = "TACO",
                    line = "The cabinet pays out its top tier and Chef enters it " +
                        "in the ledger in the handwriting he saves for menus " +
                        "and miracles. “Fund: FULL,” he says. “That bell is as " +
                        "good as cast, children.” Somewhere a foundry receives " +
                        "an order it will tell stories about. The staff room " +
                        "pretends it wasn't listening at the door.",
                    short = "🏫 Fund: FULL. The bell is as good as cast.",
                    hint = "Score 240 or better at the Taco Catch cabinet inside " +
                        "Freeman's Tacos.",
                    tacoMin = 240),
                CapstoneBeat("school_staff", 3, "The Hanging at the Door",
                    offerNpc = "Victoria",
                    offerText = "Victoria stands with the new bell in a crate " +
                        "stenciled FRAGILE in three hands. “Last step. It hangs " +
                        "by the school door, late afternoon, the whole staff " +
                        "in attendance — which at this school is a quorum and " +
                        "a half. You will hold the rope. Mackey levels it. " +
                        "Garrison has been instructed to say nothing, which is " +
                        "his applause. (Craig's little gang will claim they " +
                        "heard it first. It'll cost us a little peace with " +
                        "them. Children hear everything first. It's their one " +
                        "skill.)”",
                    verb = "TALK",
                    line = "THE HANGING — the whole staff by the school door in " +
                        "the late light: Mackey with an actual level, twice, " +
                        "m'kay; Chef with the ladle lowered like a flag; " +
                        "Garrison saying nothing at a volume that carries; " +
                        "Crabtree arrived by bus, because there was no other " +
                        "way she was coming. Victoria nods. You pull. The new " +
                        "bell rings over the schoolyard — true, level, and " +
                        "loud enough to be heard by every kid who ever " +
                        "pretended not to hear the old one. It stays by the " +
                        "door. So does the silence after it, briefly.",
                    short = "🏫 The new bell rang true and level. It stays by the door.",
                    hint = "Meet Principal Victoria at South Park Elementary in " +
                        "the late afternoon (3–7 PM) — the whole staff will be there.",
                    targetNpc = "Victoria", locId = "Elementary",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- CHURCH & FAITHFUL: the quilt of the late ----------
        CapstoneChain("church_faithful", "The Quilt of the Late",
            seniorNpc = "Father Maxi",
            markLoc = "Church", markToken = "church-quilt",
            markLabel = "the Third Pew quilt hanging by the Church door",
            playerTitle = "Keeper of the Third Pew",
            hintLine = "There is one more work, child, and it is not offered to " +
                "the merely welcome. When the whole town holds you beloved — " +
                "come. The third pew will still be there. So will I. So, mostly, " +
                "will he.",
            beats = listOf(
                CapstoneBeat("church_faithful", 1, "Ask Who It's For",
                    offerNpc = "Father Maxi",
                    offerText = "Father Maxi has a quilt frame in the parish hall " +
                        "and the look of a man building an argument out of " +
                        "cloth. “The capstone, child, since you're family: a " +
                        "quilt for the third pew. For the late, the working, " +
                        "the hip — everyone that pew has ever held and " +
                        "missed. But a quilt needs its dedications true. Talk " +
                        "to Jesus. Ask him who it's for. He'll know. He always " +
                        "knows, it's his whole thing.”",
                    verb = "TALK",
                    line = "Jesus considers the question with the patience of " +
                        "weather. “It's for the ones who are coming back at " +
                        "their own speed,” he says. “Mrs. Abernathy's hip. " +
                        "The Johnsons' baby, when she's older and furious " +
                        "about it. The man on Sundays, who is not lost, who " +
                        "is missed, which is a different sacrament entirely.” " +
                        "He folds his hands. “Tell Maxi: stitch their names " +
                        "loose. They'll tighten themselves.”",
                    short = "⛪ The dedications are true. Stitch the names loose.",
                    hint = "Find Jesus and talk to him — ask who the quilt is for.",
                    targetNpc = "Jesus"),
                CapstoneBeat("church_faithful", 2, "Count the Pew at Dusk",
                    offerNpc = "Jesus",
                    offerText = "Jesus is by the church door in the evening light. " +
                        "“One thing the quilt needs that no hand can sew: a " +
                        "count, taken honestly. Come to the Church at dusk — " +
                        "evening prayers, the light going — and LOOK at the " +
                        "third pew. Count who's there and love who's not. " +
                        "Both numbers go in the label. Maxi insists on the " +
                        "label. Everything in this parish gets a label now. " +
                        "It's his love language.”",
                    verb = "LOOK",
                    line = "At dusk the third pew holds exactly who it holds: " +
                        "two regulars, one newcommer sitting like a question, " +
                        "and the spaces — kept, warmed, unembarrassed — where " +
                        "the late belong. You count the present and the absent " +
                        "with the same care, which is the entire theology of " +
                        "the thing. Both numbers go in the label. The label " +
                        "will outlive everyone, which is rather the point of " +
                        "labels, and of pews.",
                    short = "⛪ Both numbers counted, with the same care. Label written.",
                    hint = "Look at the Church at dusk (5–9 PM) — the third pew, " +
                        "at evening prayers.",
                    locId = "Church", minMin = 17 * 60, maxMin = 21 * 60),
                CapstoneBeat("church_faithful", 3, "The Hanging by the Door",
                    offerNpc = "Father Maxi",
                    offerText = "Father Maxi holds the finished quilt like a " +
                        "banner he's slightly afraid of. “Last step, child. It " +
                        "hangs by the church door, late afternoon, where " +
                        "everyone arriving late — which is to say everyone " +
                        "it's FOR — will see it first. The whole parish. " +
                        "Both of us, and whoever the light brings. You hang " +
                        "the left corner; it's the corner that blesses. (The " +
                        "Goths will call it a blanket with an agenda. It'll " +
                        "cost us a little gloom over there. It IS a blanket " +
                        "with an agenda. The agenda is mercy.)”",
                    verb = "TALK",
                    line = "THE HANGING — the parish entire by the church door: " +
                        "Maxi on the step stool he's not supposed to use, " +
                        "Jesus holding the right corner in a way that makes " +
                        "the left corner your whole responsibility, and the " +
                        "light doing what it does at that hour whether " +
                        "anyone planned it or not. The quilt goes up — names " +
                        "stitched loose, both numbers in the label — and the " +
                        "third pew's whole congregation, present and " +
                        "otherwise, is officially expected. Forever. In " +
                        "writing. In cloth.",
                    short = "⛪ The quilt hangs by the door. The late are expected, forever.",
                    hint = "Meet Father Maxi at the Church in the late afternoon " +
                        "(3–7 PM) — the whole parish will be there.",
                    targetNpc = "Father Maxi", locId = "Church",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            )),

        // ---------- TEGRIDY & LOCAL BUSINESS: the standard, sworn ----------
        CapstoneChain("tegridy_crew", "The Standard, Sworn",
            seniorNpc = "Jimbo",
            markLoc = "TegridyFarms", markToken = "tegridy-bench",
            markLabel = "the Signatories' Bench by the farm gate",
            playerTitle = "Standard, Sworn",
            hintLine = "There's one signature the Standard still wants, boy, and " +
                "it ain't asked of customers. Town's gotta love you first — " +
                "beloved, all the way. Then we'll set you a seat at the gate.",
            beats = listOf(
                CapstoneBeat("tegridy_crew", 1, "City Wok Signs",
                    offerNpc = "Jimbo",
                    offerText = "Jimbo has the Standard's charter in a frame that's " +
                        "only slightly a gun rack. “The big ask, boy. The " +
                        "Standard goes SWORN — every real business in town " +
                        "signs it, or it's just a sign. First signature " +
                        "matters most, and the hardest signature in Colorado " +
                        "commerce is Lu Kim's. Talk to Lu Kim. Get City Wok " +
                        "on the Standard.”",
                    verb = "TALK",
                    line = "Lu Kim reads the entire charter twice, once for the " +
                        "words and once for the trap. “No trap,” he says, with " +
                        "the air of a man disappointed professionally. “Is " +
                        "good standard. City Wok sign.” He signs with a pen he " +
                        "produces from nowhere, in characters that make the " +
                        "whole document look more official by standing near " +
                        "them. First signature: gotten. The Standard has a " +
                        "wok in it now.",
                    short = "🌿 City Wok signs. The Standard has a wok in it now.",
                    hint = "Find Lu Kim and talk to him — City Wok's signature is first.",
                    targetNpc = "Lu Kim"),
                CapstoneBeat("tegridy_crew", 2, "The Coffee Empire's Tribute",
                    offerNpc = "Towelie",
                    offerText = "Towelie is holding the charter and a towel, in " +
                        "that order of confidence. “Okay so — the Tweek " +
                        "signature. The coffee empire doesn't sign PAPER, man, " +
                        "the empire signs in BEANS. One bag of coffee beans, " +
                        "delivered to Mr. Tweek himself, with ceremony. " +
                        "That's the tribute. That's the signature. Don't " +
                        "forget the — you know what, just do the beans.”",
                    verb = "GIVE",
                    line = "Mr. Tweek receives the beans with both hands and a " +
                        "tremor that might be emotion and is definitely also " +
                        "the usual tremor. “A TRIBUTE?? From the STANDARD??” " +
                        "He signs the charter so fast the pen squeaks. Mrs. " +
                        "Tweek witnesses, calm as a notary in a storm. The " +
                        "coffee empire is on the Standard, and somewhere a " +
                        "fresh pot starts, as is traditional.",
                    short = "🌿 The empire signs in beans. A fresh pot starts, traditionally.",
                    hint = "Give a bag of coffee beans to Mr. Tweek — the tribute " +
                        "is the signature.",
                    targetNpc = "Mr. Tweek", itemId = "coffee_beans"),
                CapstoneBeat("tegridy_crew", 3, "The Bench at the Gate",
                    offerNpc = "Jimbo",
                    offerText = "Jimbo has built a bench and it is, charitably, " +
                        "structural. “Last step, boy. The Signatories' Bench " +
                        "goes by the farm gate, late afternoon, every signer " +
                        "in attendance, and the sworn one — that's you, once " +
                        "you sit — sits FIRST. A bench ain't a bench until " +
                        "it's sat in order. (The Parents'll call it loitering " +
                        "with joinery. It'll cost us a little goodwill in the " +
                        "cul-de-sacs. Sharon can sit on it and think about " +
                        "that.)”",
                    verb = "TALK",
                    line = "THE BENCH AT THE GATE — the whole crew at the farm " +
                        "gate in the late light: Jimbo standing like the bench " +
                        "might need defending, Towelie holding the charter " +
                        "right side up on the second try, Ned present at a " +
                        "volume only dogs and old friends can hear. You sit " +
                        "first, per the order of the thing. The bench holds. " +
                        "Jimbo nods like a tenure committee that's seen the " +
                        "future. “Sworn,” he says. The gate has a bench now, " +
                        "and the bench has a roster, and the roster starts " +
                        "with your name.",
                    short = "🌿 The bench holds. Sworn, seated, first in the roster.",
                    hint = "Meet Jimbo at Tegridy Farms in the late afternoon " +
                        "(3–7 PM) — every signer will be there.",
                    targetNpc = "Jimbo", locId = "TegridyFarms",
                    minMin = CEREMONY_FROM, maxMin = CEREMONY_TO)
            ))
    )
}

package com.hunt3r.southparktown

// ============================================================
// Faction Missions — v0.5.0d (Queue #5 item 4): every faction
// gets a real mission chain.
//
// Until now the eight factions had standing, mood, turf and
// opinions — but nothing they ASKED of you that was theirs.
// Now each faction runs ONE escalating three-beat chain in
// its own obsession's voice: beat 1 an errand, beat 2 a
// scheme, beat 3 a set piece. Beats are judged ENTIRELY by
// verbs the game already has — the same rails errands and
// holiday beats ride:
//
//   TALK     talk to a named person (the Talk gate judges it)
//   GIVE     hand a named item to a named person (gift rail)
//   LOOK     a careful Look at a named place (sometimes in a
//            time window — "be somewhere at a time" is a LOOK
//            with minMin/maxMin, the HolidayBeats clock gates)
//   PHOTO    a selfie at a named place (Photos.applyPhoto)
//   CATCH    a catch at the pond (Minigames.applyCatch) —
//            any fish, or one named species
//   SHOWDOWN win a Snowball Showdown (applyShowdownResult) —
//            any opponent, or one named opponent
//   TACO     post a score at Taco Catch (applyTacoResult)
//
// Standing gates: beat 1 is open at ANY standing, beat 2
// wants BEAT2_MIN_STANDING (Liked), beat 3 wants
// BEAT3_MIN_STANDING (Respected) — the Factions.standingLabel
// thresholds themselves, so the Journal's word and the gate
// are the same number. Chains are offered by a faction member
// through the Talk gate (TalkGate.Rail.FACTION_MISSION, below
// the today-only holiday beat, above a fresh errand ask) and
// tracked on the Journal's ⚔️ FACTION MISSIONS page; the
// titles they earn ride the Journal's 🪪 YOU card.
//
// Stakes & marks: finishing beat 3 leaves a PERMANENT MARK —
// a small scene-dressing token at a named location, drawn by
// the SceneArt mark layer in smooth shapes only (the dig
// layer's discipline: fields, banners, plaques and boxes,
// never a clustered-hole pattern) — plus a faction title the
// player carries. It also costs RIVAL_COST standing with the
// chain's named rival, telegraphed in beat 3's offer text.
// RIVAL_OF is a perfect pairing (each faction is exactly one
// other faction's rival), so a whole lifetime of chains can
// cost any one faction at most RIVAL_COST standing, ever —
// and standing is re-earnable without limit (road pitches,
// tier crossings), so no chain can ever lock out.
//
// Cross-talk: while a mystery finale / accusation /
// disposition is due, mission OFFERS wait — the road beats'
// silencing rule (Walk.caseCritical). Beats already taken on
// still judge normally; a case never strands a chain. Beats
// have no fail state and no expiry: decline an offer and it
// is simply still there next time you talk to them.
//
// State discipline: ZERO new save keys. Progress lives in
// the existing capped `life` map — one bitmask and one
// active-beat number per faction ("fmMask:<id>", values
// 0..7; "fmActive:<id>", values 0..3) — and everything else
// (chain done, mark standing, title earned) DERIVES from the
// mask. A v0.5.0c save loads with every chain untouched.
// ============================================================

/** One judged beat of a faction chain. [line] is the full
 *  narration when the beat lands; [short] its ticker form. */
data class FactionBeat(
    val factionId: String,
    val beat: Int,               // 1..3
    val title: String,
    val offerNpc: String,        // a member of the faction — Talk to them to be offered it
    val offerText: String,       // the briefing, in the offerer's voice
    val verb: String,            // TALK | GIVE | LOOK | PHOTO | CATCH | SHOWDOWN | TACO
    val line: String,
    val short: String,
    val hint: String,            // the Journal's where-to-go line while it's armed
    val targetNpc: String? = null,  // TALK/GIVE target · SHOWDOWN named opponent
    val itemId: String? = null,     // GIVE: the item that counts
    val locId: String? = null,      // LOOK/PHOTO place · TALK: where the target must be
    val fishId: String? = null,     // CATCH: species (null = any honest fish)
    val tacoMin: Int? = null,       // TACO: the score to post
    val minMin: Int = 0,            // time-of-day window (minutes)
    val maxMin: Int = 1440
)

/** One faction's whole chain, plus the permanent things its
 *  last beat leaves behind: the mark token standing at
 *  [markLoc], and the [playerTitle] the town starts using. */
data class FactionChain(
    val factionId: String,
    val title: String,
    val markLoc: String,
    val markToken: String,
    val markLabel: String,       // what the mark is, player-facing
    val playerTitle: String,     // the title finishing this chain earns
    val beats: List<FactionBeat> // exactly 3, beat numbers 1..3 in order
)

/** A beat that just landed — what the rails show the player. */
data class MissionHit(
    val chain: FactionChain,
    val beat: FactionBeat,
    val text: String,
    val chainCompletedNow: Boolean,
    val completionText: String?
)

object FactionMissions {

    // ---------------- the dials (all suite-pinned) ----------------

    /** Beat 1 is open at any standing a save can hold. */
    const val BEAT1_MIN_STANDING = -50

    /** Beat 2's gate: Liked (Factions.standingLabel's own line). */
    const val BEAT2_MIN_STANDING = 10

    /** Beat 3's gate: Respected (the same table, one rung up). */
    const val BEAT3_MIN_STANDING = 30

    /** The gate for beat [n] (1-based) — monotonic by construction. */
    fun gateForBeat(beat: Int): Int = when (beat) {
        1 -> BEAT1_MIN_STANDING
        2 -> BEAT2_MIN_STANDING
        else -> BEAT3_MIN_STANDING
    }

    /** Standing a landed beat pays with its OWN faction. */
    val BEAT_STANDING: List<Int> = listOf(8, 10, 12)

    /** Capsule coins a landed beat pays — errand scale. */
    val BEAT_COINS: List<Int> = listOf(2, 3, 5)

    /** Friendship a landed beat pays its target (or its offerer). */
    const val BEAT_FRIENDSHIP = 4

    /** What finishing a chain costs with its named rival —
     *  small, telegraphed in beat 3's offer text, and (the
     *  pairing below) the most any faction can ever lose to
     *  the whole mission system in a lifetime. */
    const val RIVAL_COST = 4

    /** Each faction's named rival — a perfect pairing: every
     *  faction appears exactly once as a rival, so the losses
     *  can never stack on one faction. */
    val RIVAL_OF: Map<String, String> = mapOf(
        "boys_crew" to "girls_circle",
        "girls_circle" to "boys_crew",
        "goth_kids" to "church_faithful",
        "church_faithful" to "goth_kids",
        "parents" to "tegridy_crew",
        "tegridy_crew" to "parents",
        "craigs_gang" to "school_staff",
        "school_staff" to "craigs_gang"
    )

    /** The verbs a beat may be judged by — every one an
     *  existing rail (see the header). Nothing else is legal. */
    val VERBS: List<String> = listOf(
        "TALK", "GIVE", "LOOK", "PHOTO", "CATCH", "SHOWDOWN", "TACO")

    // ---------------- state (the `life` map, nothing new) ----------------

    private fun maskKey(fid: String) = "fmMask:$fid"
    private fun activeKey(fid: String) = "fmActive:$fid"

    /** Beats of [fid] already landed, as a bitmask (bit beat-1). */
    fun mask(state: GameState, fid: String): Int =
        (state.life[maskKey(fid)] ?: 0).coerceIn(0, 7)

    fun beatsDone(state: GameState, fid: String): Int =
        Integer.bitCount(mask(state, fid))

    fun chainDone(state: GameState, fid: String): Boolean =
        mask(state, fid) == 7

    fun chainsDone(state: GameState): Int =
        CHAINS.count { chainDone(state, it.factionId) }

    /** The beat currently taken on for [fid] (offered and
     *  accepted, not yet landed), or null. */
    fun activeBeat(state: GameState, fid: String): FactionBeat? {
        val n = (state.life[activeKey(fid)] ?: 0).coerceIn(0, 3)
        if (n == 0) return null
        val chain = chainById(fid) ?: return null
        val beat = chain.beats.getOrNull(n - 1) ?: return null
        return if (mask(state, fid) and (1 shl (n - 1)) == 0) beat else null
    }

    /** The next beat [fid] will offer: the first unlanded one.
     *  Beats land in order, so this is also the only one that
     *  can be armed. */
    fun nextBeat(state: GameState, fid: String): FactionBeat? {
        val chain = chainById(fid) ?: return null
        val m = mask(state, fid)
        return chain.beats.firstOrNull { m and (1 shl (it.beat - 1)) == 0 }
    }

    fun standingOf(state: GameState, fid: String): Int =
        state.factionStates[fid]?.playerStanding ?: 0

    /** May [beat] be offered right now — standing gate only.
     *  (The case-finale silencing lives in offerFor.) */
    fun gatePasses(state: GameState, beat: FactionBeat): Boolean =
        standingOf(state, beat.factionId) >= gateForBeat(beat.beat)

    // ---------------- offers (the Talk gate's rail) ----------------

    /** The mission [npc] would offer you right now, or null:
     *  they front their faction's next beat, no beat of that
     *  faction is already taken on, your standing clears the
     *  beat's gate — and no mystery finale is on the table
     *  (Walk.caseCritical: while a case finale / accusation /
     *  disposition is due, offers wait, exactly like the road
     *  beats go quiet). Pure. */
    fun offerFor(state: GameState, npc: String): FactionBeat? {
        if (Walk.caseCritical(state)) return null
        for (chain in CHAINS) {
            val next = nextBeat(state, chain.factionId) ?: continue
            if (next.offerNpc != npc) continue
            if (activeBeat(state, chain.factionId) != null) return null
            return if (gatePasses(state, next)) next else null
        }
        return null
    }

    /** Take [beat] on: arm it as its faction's active beat.
     *  Refuses anything offerFor wouldn't offer — the gate
     *  and the arm can never disagree. */
    fun accept(state: GameState, beat: FactionBeat): Boolean {
        if (offerFor(state, beat.offerNpc) != beat) return false
        state.life[activeKey(beat.factionId)] = beat.beat
        return true
    }

    /** The one-line objective for an armed beat (Journal / offer card). */
    fun objective(beat: FactionBeat): String = when (beat.verb) {
        "TALK" -> "Talk to ${beat.targetNpc}"
        "GIVE" -> "Give ${itemById(beat.itemId ?: "")?.name ?: beat.itemId} " +
            "to ${beat.targetNpc}"
        "LOOK" -> "Look carefully at ${LOCATIONS[beat.locId]?.name ?: beat.locId}" +
            timeSuffix(beat)
        "PHOTO" -> "Take a photo at ${LOCATIONS[beat.locId]?.name ?: beat.locId}"
        "CATCH" -> if (beat.fishId != null)
            "Catch a ${fishById(beat.fishId)?.name ?: "fish"} at Stark's Pond"
        else "Catch any fish at Stark's Pond"
        "SHOWDOWN" -> if (beat.targetNpc != null)
            "Beat ${beat.targetNpc} at the Snowball Showdown (Kid's Park fort)"
        else "Win a Snowball Showdown (Kid's Park fort)"
        "TACO" -> "Score ${beat.tacoMin}+ at Taco Catch (Freeman's Tacos)"
        else -> beat.hint
    }

    private fun timeSuffix(beat: FactionBeat): String = when {
        beat.minMin == 0 && beat.maxMin == 1440 -> ""
        beat.maxMin == 1440 -> " (after ${minutesToTimeStr(beat.minMin)})"
        beat.minMin == 0 -> " (before ${minutesToTimeStr(beat.maxMin)})"
        else -> " (between ${minutesToTimeStr(beat.minMin)} and " +
            "${minutesToTimeStr(beat.maxMin)})"
    }

    // ---------------- the judges (one per existing verb) ----------------

    private fun timeOk(state: GameState, beat: FactionBeat): Boolean =
        state.timeMin >= beat.minMin && state.timeMin < beat.maxMin

    private fun activeBeats(state: GameState): List<FactionBeat> =
        CHAINS.mapNotNull { activeBeat(state, it.factionId) }

    /** TALK query half: an armed TALK beat a conversation with
     *  [npc] would land right now, or null. Pure. */
    fun talkBeatFor(state: GameState, npc: String): FactionBeat? =
        activeBeats(state).firstOrNull {
            it.verb == "TALK" && it.targetNpc == npc && timeOk(state, it) &&
                (it.locId == null ||
                    state.agents[npc]?.location == it.locId)
        }

    /** TALK judged (the Talk gate calls this, like a delivery). */
    fun judgeTalk(state: GameState, npc: String): List<MissionHit> {
        val beat = talkBeatFor(state, npc) ?: return emptyList()
        return listOf(completeBeat(state, beat))
    }

    /** GIVE judged: a real hand-off through the gift rail —
     *  an armed GIVE beat whose named item reaches its person.
     *  (The gift itself still lands normally; this is the
     *  mission's ear, exactly like HolidayBeats.onGive.) */
    fun onGive(state: GameState, npc: String, itemId: String): List<MissionHit> =
        activeBeats(state).filter {
            it.verb == "GIVE" && it.targetNpc == npc && it.itemId == itemId
        }.map { completeBeat(state, it) }

    /** LOOK judged: the text a careful Look at [locId] earns
     *  from armed LOOK beats there (time windows included),
     *  or null. */
    fun lookText(state: GameState, locId: String): String? {
        val hits = activeBeats(state).filter {
            it.verb == "LOOK" && it.locId == locId && timeOk(state, it)
        }.map { completeBeat(state, it) }
        if (hits.isEmpty()) return null
        return hits.joinToString("\n") { hit ->
            hit.text + (hit.completionText?.let { "\n$it" } ?: "")
        }
    }

    /** PHOTO judged — called from inside Photos.applyPhoto, so
     *  a shutter click settles a mission's portrait beat like
     *  it settles PHOTO errands and holiday beats. */
    fun onPhoto(state: GameState, rec: PhotoRecord): List<MissionHit> =
        activeBeats(state).filter {
            it.verb == "PHOTO" && it.locId == rec.locId
        }.map { completeBeat(state, it) }

    /** CATCH judged — called from Minigames.applyCatch: an
     *  armed CATCH beat takes any honest fish, or its named
     *  species only. */
    fun onCatch(state: GameState, speciesId: String): List<MissionHit> =
        activeBeats(state).filter {
            it.verb == "CATCH" && (it.fishId == null || it.fishId == speciesId)
        }.map { completeBeat(state, it) }

    /** SHOWDOWN judged — called from applyShowdownResult: only
     *  a WIN lands it, against anyone or the named opponent. */
    fun onShowdown(state: GameState, won: Boolean,
                   opponent: String): List<MissionHit> {
        if (!won) return emptyList()
        return activeBeats(state).filter {
            it.verb == "SHOWDOWN" &&
                (it.targetNpc == null || it.targetNpc == opponent)
        }.map { completeBeat(state, it) }
    }

    /** TACO judged — called from applyTacoResult: the final
     *  score against the beat's posted number. */
    fun onTaco(state: GameState, score: Int): List<MissionHit> =
        activeBeats(state).filter {
            it.verb == "TACO" && score >= (it.tacoMin ?: Int.MAX_VALUE)
        }.map { completeBeat(state, it) }

    // ---------------- completion ----------------

    /** The standing math of a landed beat, alone: its own
     *  faction gains, and a finished chain's rival pays
     *  RIVAL_COST — every value clamped to the standing range,
     *  so the math can never push anyone past the floor. */
    fun applyCompletionStanding(state: GameState, beat: FactionBeat) {
        val fs = state.factionStates[beat.factionId] ?: return
        fs.playerStanding = (fs.playerStanding +
            BEAT_STANDING[beat.beat - 1]).coerceIn(-50, 100)
        if (beat.beat == 3) {
            val rival = RIVAL_OF[beat.factionId] ?: return
            val rs = state.factionStates[rival] ?: return
            rs.playerStanding = (rs.playerStanding - RIVAL_COST).coerceIn(-50, 100)
        }
    }

    /** Land [beat]: set its bit, disarm it, pay standing /
     *  coins / friendship at errand scale, and — on beat 3 —
     *  write the chain into the town log with the mark and
     *  the title it leaves. */
    fun completeBeat(state: GameState, beat: FactionBeat): MissionHit {
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
        val doneNow = chainDone(state, beat.factionId)
        var completionText: String? = null
        if (doneNow) {
            val rivalName = RIVAL_OF[beat.factionId]
                ?.let { FACTION_BY_ID[it]?.name } ?: "a rival"
            completionText = "⚔️ CHAIN COMPLETE — “${chain.title}” is done. " +
                "The mark stands: ${chain.markLabel}. The town has a title " +
                "for you now: ${chain.playerTitle}. (${rivalName} noticed: " +
                "-$RIVAL_COST standing there. Worth it.)"
            townLogLine(state, "⚔️ ${chain.title} — ${FACTION_BY_ID[beat.factionId]?.name}: " +
                "the new kid is ${chain.playerTitle} now; ${chain.markLabel} stands.")
        }
        state.events.add(beat.short)
        while (state.events.size > 40) state.events.removeAt(0)
        return MissionHit(chain, beat, beat.line, doneNow, completionText)
    }

    // ---------------- marks & titles (all derived) ----------------

    /** The permanent marks standing right now, locId -> tokens
     *  — derived whole from finished chains, so a mark appears
     *  exactly when its chain's beat 3 lands and can never be
     *  lost. SceneArt's mark layer draws these. */
    fun markTokensByLoc(state: GameState): Map<String, List<String>> {
        val out = linkedMapOf<String, MutableList<String>>()
        for (chain in CHAINS) {
            if (chainDone(state, chain.factionId))
                out.getOrPut(chain.markLoc) { mutableListOf() }
                    .add(chain.markToken)
        }
        return out
    }

    /** Every mark token the system can ever leave. (A getter,
     *  not an initializer: CHAINS is declared below.) */
    val MARK_TOKENS: Set<String> get() = CHAINS.map { it.markToken }.toSet()

    /** Titles earned so far, in faction order — the 🪪 YOU
     *  card carries these. Derived from the masks, like marks. */
    fun titlesEarned(state: GameState): List<String> =
        CHAINS.filter { chainDone(state, it.factionId) }
            .map { "${FACTION_BY_ID[it.factionId]?.icon ?: "⚔️"} ${it.playerTitle}" }

    // ---------------- the Journal page ----------------

    /** The ⚔️ FACTION MISSIONS page: every faction, its chain,
     *  where it stands, what its next beat wants (gate named
     *  when standing is short), and — once done — its mark
     *  and the title it left you. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val done = chainsDone(state)
        out += "⚔️ FACTION MISSIONS — " +
            if (done == 0) "every faction in town has a chain of work " +
                "with your name on it — talk to its people"
            else "$done of ${CHAINS.size} chains run; the town keeps the marks"
        for (chain in CHAINS) {
            val f = FACTION_BY_ID[chain.factionId] ?: continue
            val standing = standingOf(state, chain.factionId)
            when {
                chainDone(state, chain.factionId) ->
                    out += "✓ ${f.icon} ${f.name} — “${chain.title}”: DONE. " +
                        "${chain.markLabel} stands at " +
                        "${LOCATIONS[chain.markLoc]?.name ?: chain.markLoc}. " +
                        "Title: ${chain.playerTitle}."
                else -> {
                    val active = activeBeat(state, chain.factionId)
                    val next = nextBeat(state, chain.factionId)
                    if (active != null) {
                        out += "▶ ${f.icon} ${f.name} — “${chain.title}”: beat " +
                            "${active.beat} of 3 taken on — ${objective(active)}. " +
                            active.hint
                    } else if (next != null) {
                        val gate = gateForBeat(next.beat)
                        val gateNote = if (standing >= gate) ""
                        else " (wants ${Factions.standingLabel(gate)} standing " +
                            "with them — you're at $standing; road pitches and " +
                            "friends-of-the-faction move it)"
                        out += "· ${f.icon} ${f.name} — “${chain.title}”: beat " +
                            "${next.beat} of 3 waits with ${next.offerNpc}$gateNote."
                    }
                }
            }
        }
        return out
    }

    fun chainById(fid: String): FactionChain? =
        CHAINS.firstOrNull { it.factionId == fid }

    // ---------------- the eight chains ----------------

    val CHAINS: List<FactionChain> = listOf(

        // ---------- THE BOYS' CREW: the pond is theirs, obviously ----------
        FactionChain("boys_crew", "The Great Pond Claim",
            markLoc = "StarksPond", markToken = "crew-flag",
            markLabel = "the Crew's claim flag at Stark's Pond",
            playerTitle = "Admiral of the Pond",
            beats = listOf(
                FactionBeat("boys_crew", 1, "The Lookout's Price",
                    offerNpc = "Cartman",
                    offerText = "Cartman unfolds a map of Stark's Pond drawn in crayon, " +
                        "mostly red. “We're claiming the pond. Officially. Kenny's the " +
                        "lookout, but Kenny's loyalty is — how do I put this — SNACK-BASED. " +
                        "Bring him a snack cake. A whole one. Then he's ours.”",
                    verb = "GIVE",
                    line = "Kenny takes the snack cake with both hands and a lookout's " +
                        "gravity. “(muffled) Mmph mmph.” He salutes you with it, then eats " +
                        "the salute. The pond's eyes are yours. The claim has begun.",
                    short = "🧢 The lookout is bought. Kenny salutes with a snack cake.",
                    hint = "Kenny's wherever Kenny is — Give him the snack cake.",
                    targetNpc = "Kenny", itemId = "snack_cake"),
                FactionBeat("boys_crew", 2, "War Chest at the Cabinet",
                    offerNpc = "Kyle",
                    offerText = "Kyle has a notebook labeled CLAIM FINANCES. “Cartman's " +
                        "plan needs a war chest, and the Taco Catch cabinet at Freeman's " +
                        "is — legally, somehow — our treasury. Post 180 or better. For " +
                        "the claim. I can't believe I'm saying any of this.”",
                    verb = "TACO",
                    line = "The cabinet pays out and Kyle enters it in the notebook in " +
                        "green pen, which is the serious color. “War chest: funded. " +
                        "History will note the tacos.” The claim is no longer a crayon " +
                        "map. It has a BUDGET.",
                    short = "🧢 War chest funded at the Taco Catch cabinet.",
                    hint = "The cabinet inside Freeman's Tacos — score 180 or better.",
                    tacoMin = 180),
                FactionBeat("boys_crew", 3, "Raise the Colors",
                    offerNpc = "Cartman",
                    offerText = "Cartman is wearing a paper admiral's hat. “Final phase. " +
                        "We raise the colors at the pond and we DOCUMENT it — a photo, " +
                        "at Stark's Pond, so the Girls' Circle and their clipboards can " +
                        "suck it. (The Girls will hear about this. It'll cost us a " +
                        "little goodwill there. WORTH IT.)”",
                    verb = "PHOTO",
                    line = "📸 The flag goes up on the pond's signpost — crayon-red, " +
                        "slightly crooked, absolutely official — and the shutter catches " +
                        "it with the whole crew pretending not to pose. Somewhere across " +
                        "town, a clipboard senses a disturbance. The pond is claimed.",
                    short = "🧢 The colors are raised at the pond. Documented.",
                    hint = "Take a photo at Stark's Pond — the flag deserves an audience.",
                    locId = "StarksPond")
            )),

        // ---------- THE GOTH KIDS: a census of the doomed ----------
        FactionChain("goth_kids", "The Conformist Census",
            markLoc = "Elementary", markToken = "goth-mural",
            markLabel = "the Goths' black poetry panel behind the school",
            playerTitle = "Honorary Nonconformist",
            beats = listOf(
                FactionBeat("goth_kids", 1, "Count the Smiling",
                    offerNpc = "Henrietta",
                    offerText = "Henrietta closes her poetry book on one finger. “We're " +
                        "taking a census. Of conformists. Go behind the school and LOOK — " +
                        "count the smiling. The mural wall. You'll know it by the despair.”",
                    verb = "LOOK",
                    line = "Behind the school, the count is devastating: everybody is " +
                        "smiling, all the time, like it's a job. You tally it on the back " +
                        "of a detention slip the way Henrietta taught you — slowly, with " +
                        "contempt for the data. The census has its first page.",
                    short = "🦇 Census page one: the smiling, counted.",
                    hint = "Look carefully at South Park Elementary — the back wall.",
                    locId = "Elementary"),
                FactionBeat("goth_kids", 2, "The Verdict Goes Upstairs",
                    offerNpc = "Pete",
                    offerText = "Pete flips his hair out of exactly one eye. “The census " +
                        "is done being data. It has to become a VERDICT, and Michael's " +
                        "the only one of us who can deliver it without accidentally " +
                        "caring. Talk to Michael. Tell him the number. He'll know what " +
                        "it means. It means everything is bad.”",
                    verb = "TALK",
                    line = "You give Michael the number. He is quiet for a long time, " +
                        "which for Michael is a standing ovation. “So it's official,” he " +
                        "says. “We're surrounded.” He writes it in the book. The verdict " +
                        "is entered.",
                    short = "🦇 The verdict is entered: surrounded.",
                    hint = "Find Michael and talk to him — deliver the number.",
                    targetNpc = "Michael"),
                FactionBeat("goth_kids", 3, "Nobody Smiles for the Portrait",
                    offerNpc = "Michael",
                    offerText = "Michael regards the census like a finished grave. “One " +
                        "thing left. A portrait of the nonconformists, at the school, " +
                        "for the record. NOBODY smiles. If anybody smiles, we burn it " +
                        "and the Church wins. (The Father Maxi fan club will hear " +
                        "about the mural. Let them.)”",
                    verb = "PHOTO",
                    line = "📸 Three goths arrange themselves by the mural wall with the " +
                        "care of people defusing something. Nobody smiles. Henrietta " +
                        "almost ruins it by looking content, catches herself, and deepens " +
                        "the gloom at the last second. The portrait is perfect. The " +
                        "census is closed, forever, in black paint.",
                    short = "🦇 The portrait: nobody smiled. The census is closed.",
                    hint = "Take a photo at South Park Elementary. Do not smile.",
                    locId = "Elementary")
            )),

        // ---------- THE GIRLS' CIRCLE: the list to end lists ----------
        FactionChain("girls_circle", "The List to End Lists",
            markLoc = "Mall", markToken = "list-board",
            markLabel = "the Circle's official list board at the Mall",
            playerTitle = "In The Good Column",
            beats = listOf(
                FactionBeat("girls_circle", 1, "The First Entry",
                    offerNpc = "Wendy",
                    offerText = "Wendy has a fresh notebook and a pen that means business. " +
                        "“We're making the list to end lists — one list, final, fair, " +
                        "with columns. Bebe is holding the first entry and she will not " +
                        "surrender it over the phone. Talk to Bebe. In person. That's " +
                        "the whole system.”",
                    verb = "TALK",
                    line = "Bebe produces the first entry from a folder labeled with a " +
                        "heart and a warning. “It's not gossip if it's ARCHIVED,” she " +
                        "says, and honestly, the folder work is impeccable. The list " +
                        "has begun — and your name is nowhere near the bad column. Yet.",
                    short = "💄 The first entry is archived. The list has begun.",
                    hint = "Find Bebe and talk to her — in person, per regulations.",
                    targetNpc = "Bebe"),
                FactionBeat("girls_circle", 2, "Diplomacy, Delivered by Hand",
                    offerNpc = "Bebe",
                    offerText = "Bebe is sealing an envelope with the gravity of a treaty. " +
                        "“Every list needs a peace offering or it curdles. Heidi's our " +
                        "diplomat, and diplomats run on flowers. Bring Heidi a flower. " +
                        "Tell her it's from the Circle. She'll know what it costs us.”",
                    verb = "GIVE",
                    line = "Heidi takes the flower and reads the whole situation off it " +
                        "in one glance, the way diplomats do. “From the Circle,” she " +
                        "says, “with columns.” She tucks it behind her ear like a flag " +
                        "of truce. The list stays fair for another day.",
                    short = "💄 Diplomacy delivered. Heidi wears the truce.",
                    hint = "Give a flower to Heidi — check the yards, check the shops.",
                    targetNpc = "Heidi", itemId = "flower"),
                FactionBeat("girls_circle", 3, "The Posting",
                    offerNpc = "Wendy",
                    offerText = "Wendy caps the good pen. “Tonight we post it — the " +
                        "finished list, on the board at the Mall, after the dinner " +
                        "crowd, when the light's honest. Be at the Mall in the evening " +
                        "and LOOK at the board. Witnesses make it law. (The Boys will " +
                        "pretend not to care. It'll cost us a little peace there.)”",
                    verb = "LOOK",
                    line = "The board at the Mall is mostly dead flyers and one lost-cat " +
                        "poster from spring — and now, centered, LEVEL, in the good " +
                        "pen: THE LIST. Two columns. Every name placed like it was " +
                        "always going to be there. A passing mom reads it and nods " +
                        "once, slowly. It's law.",
                    short = "💄 The List is posted at the Mall. It's law.",
                    hint = "Look at the Mall board in the evening (after 5 PM).",
                    locId = "Mall", minMin = 17 * 60)
            )),

        // ---------- CRAIG'S GANG: the great whatever ----------
        FactionChain("craigs_gang", "The Great Whatever",
            markLoc = "KidsPark", markToken = "craig-banner",
            markLabel = "Craig's Gang's supremely unbothered banner at Kid's Park",
            playerTitle = "Certified Whatever",
            beats = listOf(
                FactionBeat("craigs_gang", 1, "Provisions, Allegedly",
                    offerNpc = "Craig",
                    offerText = "Craig doesn't look up. “We're doing a thing at the park. " +
                        "A big thing. It needs a speckled trout from the pond. Don't " +
                        "ask why. Asking why is how they get you.” A pause. “They is " +
                        "everyone.”",
                    verb = "CATCH",
                    line = "The speckled trout comes up fighting and Craig regards it " +
                        "like a coworker. “Okay,” he says. “Provisions: done. The " +
                        "thing is officially a thing now.” Nobody explains the thing. " +
                        "The trout is released with full honors and a small nod.",
                    short = "👆 Provisions: done. The thing is a thing now.",
                    hint = "Fish at Stark's Pond until a speckled trout bites.",
                    fishId = "speckled_trout"),
                FactionBeat("craigs_gang", 2, "Recruit the Lookout",
                    offerNpc = "Clyde",
                    offerText = "Clyde is practicing looking casual, which is harder than " +
                        "it looks. “The thing needs a lookout, and Tweek is the best " +
                        "lookout in town because he is ALWAYS already alarmed. Talk to " +
                        "Tweek. Sell it as relaxing. It's not relaxing.”",
                    verb = "TALK",
                    line = "Tweek hears 'lookout' and vibrates at a frequency that " +
                        "attracts dogs. “LOOKOUT?? OH GOD, WHAT IF I MISS SOMETHING — ” " +
                        "Clyde nods, deeply satisfied. “See? PERFECT. He's already " +
                        "looking.” The gang has its alarm system.",
                    short = "👆 Lookout recruited. He is already alarmed.",
                    hint = "Find Tweek and talk to him — sell it as relaxing.",
                    targetNpc = "Tweek"),
                FactionBeat("craigs_gang", 3, "The Thing Itself",
                    offerNpc = "Craig",
                    offerText = "Craig finally looks up, which veterans agree is an " +
                        "omen. “The thing is a banner. At the park. Ours. And a banner " +
                        "isn't real until it's defended — beat Clyde at the Showdown " +
                        "fort, in front of everybody, and the banner flies. (The School " +
                        "Staff will write us up for the banner. Whatever. That's the " +
                        "review we wanted.)”",
                    verb = "SHOWDOWN",
                    line = "Clyde goes down in the snow like a man betrayed by weather " +
                        "itself, and the banner goes up over the fort — one word, huge, " +
                        "in Craig's flattest lettering: WHATEVER. The park reads it. " +
                        "The park understands. The Great Whatever is complete, and " +
                        "nobody, anywhere, can be bothered about it.",
                    short = "👆 WHATEVER, in huge letters, defended in the snow.",
                    hint = "Win the Snowball Showdown against Clyde at the Kid's Park fort.",
                    targetNpc = "Clyde")
            )),

        // ---------- THE PARENTS: the watch, again ----------
        FactionChain("parents", "The Neighborhood Watch (Again)",
            markLoc = "MainStreet", markToken = "watch-sign",
            markLabel = "the Neighborhood Watch sign on Main Street",
            playerTitle = "Deputy Grown-Up",
            beats = listOf(
                FactionBeat("parents", 1, "Inspect the Danger Corner",
                    offerNpc = "Sharon",
                    offerText = "Sharon has a clipboard and the calm of a woman who has " +
                        "chaired things. “The Watch is relaunching, and step one is " +
                        "an inspection — the corner on Main Street where everything " +
                        "happens. Go LOOK at it. Properly. If you see something, say " +
                        "something. To me. With the clipboard.”",
                    verb = "LOOK",
                    line = "The corner provides: a tipped cone, three mysterious " +
                        "footprints, and a smell you file under 'nobody's business.' " +
                        "You note it all with clipboard energy. Somewhere, Sharon " +
                        "senses a form being respected, and approves.",
                    short = "🍷 Danger corner inspected. The clipboard approves.",
                    hint = "Look carefully around Main Street — the corner.",
                    locId = "MainStreet"),
                FactionBeat("parents", 2, "Keep the Watch Awake",
                    offerNpc = "Randy",
                    offerText = "Randy is wearing a reflective vest over a regular vest. " +
                        "“Watch duty is a marathon, new kid, and Barbrady's running the " +
                        "night end of it on spite and fumes. Bring the man a hot " +
                        "chocolate. Warm. The Watch provides.” He salutes with a " +
                        "flashlight. It is not on.",
                    verb = "GIVE",
                    line = "Barbrady takes the hot chocolate in both hands like it's " +
                        "been deputized too. “For the WATCH?” he says, and stands a " +
                        "little straighter, which for Barbrady is a parade. “Tell " +
                        "Randy the night is HELD.” It is, now. Mostly.",
                    short = "🍷 The night watch is caffeinated and held.",
                    hint = "Give a hot chocolate to Officer Barbrady.",
                    targetNpc = "Barbrady", itemId = "hot_chocolate"),
                FactionBeat("parents", 3, "The Report and the Sign",
                    offerNpc = "McDaniels",
                    offerText = "Mayor McDaniels reviews your inspection notes over the " +
                        "top of her glasses. “Adequate. Which, from City Hall, is a " +
                        "parade. Final step: deliver the Watch report to Randy in " +
                        "person, and the sign goes back up on Main Street. (Tegridy " +
                        "will call it government overreach. Tegridy calls everything " +
                        "that. It'll cost us a little goodwill out there.)”",
                    verb = "TALK",
                    line = "Randy receives the report like a treaty. “Deputy,” he says, " +
                        "and means it so hard his voice cracks. By supper the sign is " +
                        "back up on Main Street — WATCH, in letters a block long — and " +
                        "Randy is under it, in two vests, watching. The town is safe. " +
                        "The town has always been safe. That's not the point.",
                    short = "🍷 The Watch sign is back up. Deputy.",
                    hint = "Find Randy and talk to him — deliver the report in person.",
                    targetNpc = "Randy")
            )),

        // ---------- SCHOOL STAFF: the hall, restored ----------
        FactionChain("school_staff", "The Hall of Honor Restoration",
            markLoc = "Elementary", markToken = "honor-plaque",
            markLabel = "the restored Hall of Honor plaque at the Elementary",
            playerTitle = "Staff (Honorary, Unpaid)",
            beats = listOf(
                FactionBeat("school_staff", 1, "The Staff-Room Emergency",
                    offerNpc = "Mackey",
                    offerText = "Mackey adjusts his tie like a man defusing his own " +
                        "morning. “We have a situation, m'kay. The staff room is OUT " +
                        "of coffee beans, Garrison has a first period, and those two " +
                        "facts cannot coexist, m'kay. Bring Garrison the beans. " +
                        "You're the only courier the budget allows. You're free.”",
                    verb = "GIVE",
                    line = "Garrison takes the beans and holds them at eye level, like a " +
                        "man reading a ransom note from God. “…Acceptable,” he says, " +
                        "which from Garrison is a parade with confetti. The staff " +
                        "room exhales. First period will happen. Unfortunately.",
                    short = "🏫 Beans delivered. First period will happen.",
                    hint = "Give a bag of coffee beans to Mr. Garrison.",
                    targetNpc = "Garrison", itemId = "coffee_beans"),
                FactionBeat("school_staff", 2, "Inventory of the Missing",
                    offerNpc = "Garrison",
                    offerText = "Garrison is standing in front of the Hall of Honor with " +
                        "his arms crossed at it. “Somebody — SOMEbody — has let this " +
                        "hall go to pieces. Plaques crooked. One MISSING. Go LOOK at " +
                        "it and tell me what's gone. And no, children, 'everything' " +
                        "is not an inventory.”",
                    verb = "LOOK",
                    line = "The inventory, taken properly: four plaques crooked, one " +
                        "screw living on the floor now, and the center plaque — the " +
                        "big one, the HONOR one — gone entirely, leaving a clean " +
                        "rectangle of unfaded paint like a held breath. Garrison is " +
                        "going to need a moment. And a ladder. And a budget.",
                    short = "🏫 Inventory complete: the big plaque is GONE.",
                    hint = "Look carefully at South Park Elementary — the Hall of Honor.",
                    locId = "Elementary"),
                FactionBeat("school_staff", 3, "The Re-Hanging",
                    offerNpc = "Chef",
                    offerText = "Chef leans in the cafeteria door with a ladle and the " +
                        "news. “Hello there, children. The missing plaque was in the " +
                        "KITCHEN, bein' used as a tray. It's clean now, it's goin' " +
                        "back up, and a re-hanging needs a PHOTO for the newsletter. " +
                        "At the school. (Craig's little gang will claim they found " +
                        "it first. Children, please. It'll cost us a little peace " +
                        "with them.)”",
                    verb = "PHOTO",
                    line = "📸 The plaque goes back up dead level — Mackey checks it with " +
                        "an actual level, twice — and the shutter catches the whole " +
                        "staff pretending they weren't emotional about a piece of " +
                        "brass. The Hall of Honor is a hall again. The newsletter " +
                        "gets its photo. Dignity: restored, pending children.",
                    short = "🏫 The plaque is re-hung, dead level. Dignity restored.",
                    hint = "Take a photo at South Park Elementary — for the newsletter.",
                    locId = "Elementary")
            )),

        // ---------- CHURCH & FAITHFUL: the box by the door ----------
        FactionChain("church_faithful", "The Lost-and-Found of Souls",
            markLoc = "Church", markToken = "lostfound-box",
            markLabel = "the Church's labeled lost-and-found box",
            playerTitle = "Friend of the Parish",
            beats = listOf(
                FactionBeat("church_faithful", 1, "The Empty Pew Problem",
                    offerNpc = "Father Maxi",
                    offerText = "Father Maxi is counting pews like they owe him money. " +
                        "“Third pew, left side. Empty three Sundays running, and a " +
                        "congregation is a garden, child — you notice a missing row. " +
                        "Talk to Jesus. He walks among them. Ask him who's missing. " +
                        "Gently. He's sensitive about attendance.”",
                    verb = "TALK",
                    line = "Jesus listens the way weather listens. “The third pew,” he " +
                        "says, “isn't empty because they stopped believing. It's empty " +
                        "because Mrs. Abernathy's hip, and the Johnsons' new baby, and " +
                        "one man who works Sundays now and feels terrible about it.” " +
                        "He smiles. “Tell Maxi: nobody's lost. They're just late.”",
                    short = "⛪ Nobody's lost. They're just late.",
                    hint = "Find Jesus and talk to him — ask about the third pew.",
                    targetNpc = "Jesus"),
                FactionBeat("church_faithful", 2, "The Bake-Sale Reserve",
                    offerNpc = "Jesus",
                    offerText = "Jesus is folding a tablecloth with impossible corners. " +
                        "“The bake sale is Sunday, and Maxi stress-bakes when he " +
                        "worries, which means by Sunday there will be NO bake sale, " +
                        "only evidence. Bring him a snack cake for the reserve table. " +
                        "Store-bought is fine. Grace is not a recipe contest.”",
                    verb = "GIVE",
                    line = "Father Maxi receives the snack cake like a relic. “For the " +
                        "RESERVE table,” he says, labeling it on the spot in his best " +
                        "hand. “If the sale sells out, this cake carries the parish.” " +
                        "It won't sell out. He has stress-baked forty muffins. But " +
                        "the reserve stands, and so does he, a little straighter.",
                    short = "⛪ The reserve cake stands. So does Maxi.",
                    hint = "Give a snack cake to Father Maxi — for the reserve table.",
                    targetNpc = "Father Maxi", itemId = "snack_cake"),
                FactionBeat("church_faithful", 3, "Labels for the Lost",
                    offerNpc = "Father Maxi",
                    offerText = "Father Maxi has a cardboard box and a vision. “Every " +
                        "church has a lost-and-found, and every lost-and-found is a " +
                        "sermon nobody preaches: gloves, umbrellas, one (1) soul, " +
                        "temporarily misplaced. Sunday morning, before the rush — " +
                        "come to the Church early and LOOK at the box with me. We " +
                        "label everything. EVERYTHING. (The Goths will call it " +
                        "conformity with stickers. Let them. It'll cost us a little " +
                        "gloom over there.)”",
                    verb = "LOOK",
                    line = "In the early light the box gives up its congregation: three " +
                        "gloves (no pairs, obviously), an umbrella with opinions, a " +
                        "cassette, two scarves, and — at the bottom — the Abernathy " +
                        "hymnal, inscribed, from the third pew. Everything gets a " +
                        "label in Maxi's best hand. The hymnal gets a ride home. The " +
                        "box stays by the door, labeled, forever: LOST. FOUND. " +
                        "LABELLED. In that order, like a promise.",
                    short = "⛪ LOST. FOUND. LABELLED. The box stays by the door.",
                    hint = "Look at the Church on a morning (before noon) — the box by the door.",
                    locId = "Church", minMin = 6 * 60, maxMin = 12 * 60)
            )),

        // ---------- TEGRIDY & LOCAL BUSINESS: the standard ----------
        FactionChain("tegridy_crew", "The Tegridy Standard",
            markLoc = "TegridyFarms", markToken = "tegridy-sign",
            markLabel = "the Tegridy Standard sign by the farm gate",
            playerTitle = "Standard-Bearer",
            beats = listOf(
                FactionBeat("tegridy_crew", 1, "Research, With Fins",
                    offerNpc = "Jimbo",
                    offerText = "Jimbo is re-hanging the shop wall and it has OPINIONS. " +
                        "“Wall's unbalanced, boy. Left side's all stories, right side's " +
                        "bare. Research assignment: catch somethin' with fins at the " +
                        "pond — anythin' honest — and report back. Science.”",
                    verb = "CATCH",
                    line = "You report the catch with measurements, and Jimbo nods like " +
                        "a tenure committee. “Honest fish. Honest report. That's the " +
                        "whole standard, right there.” He writes the weight on the " +
                        "wall card in pencil, then goes over it in pen. PEN. The " +
                        "research is accepted.",
                    short = "🌿 Research accepted — in pen, not pencil.",
                    hint = "Catch any fish at Stark's Pond and the report writes itself.",
                    fishId = null),
                FactionBeat("tegridy_crew", 2, "The Endorsement of Ned",
                    offerNpc = "Towelie",
                    offerText = "Towelie is holding a clipboard upside down, which he " +
                        "insists is a technique. “Okay so — the Standard needs an " +
                        "endorsement, and Ned is the most trusted voice in local " +
                        "business on account of he barely uses it. Talk to Ned. Get " +
                        "the endorsement. Don't forget to bring a towel. Just — " +
                        "in general. As a person.”",
                    verb = "TALK",
                    line = "Ned considers the Standard for a full minute, which is a " +
                        "keynote address by Ned standards. Through the voice box, " +
                        "finally, one word, slowed down so the whole counter can " +
                        "hear it: “Mmmkay.” Towelie writes it down in triplicate. " +
                        "The endorsement is unanimous.",
                    short = "🌿 Ned has endorsed the Standard. One word. Unanimous.",
                    hint = "Find Ned and talk to him — get the endorsement.",
                    targetNpc = "Ned"),
                FactionBeat("tegridy_crew", 3, "The Sign Goes Level",
                    offerNpc = "Jimbo",
                    offerText = "Jimbo has built a sign and it is, charitably, " +
                        "agricultural. “THE TEGRIDY STANDARD, by the farm gate, for " +
                        "the new ad photo. It goes up today and it goes up LEVEL, " +
                        "and the ad needs the photo taken AT the farm to prove it. " +
                        "(The Parents'll call it an eyesore at the next meeting. " +
                        "It'll cost us a little goodwill in the cul-de-sacs. An " +
                        "eyesore with STANDARDS, Sharon.)”",
                    verb = "PHOTO",
                    line = "📸 The sign goes up with a level, a prayer, and Ned holding " +
                        "the post like it owes him money — and the shutter catches " +
                        "it dead level, TEGRIDY STANDARD in letters you can read " +
                        "from the road, with the whole crew pretending they always " +
                        "build things this straight. It is, by one bubble's width, " +
                        "the levelest thing in the county.",
                    short = "🌿 The Standard sign is up — dead level, by a bubble.",
                    hint = "Take a photo at Tegridy Farms — the sign, for the ad.",
                    locId = "TegridyFarms")
            ))
    )

    val CHAIN_BY_ID: Map<String, FactionChain> = CHAINS.associateBy { it.factionId }
}

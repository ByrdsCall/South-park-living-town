package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Rival — v0.8.0e: THE RIVAL (Queue #8 item 5).
//
// Somebody in town has decided you are their measuring stick.
//
// THE PICK. Early in a save, one kid is crowned THE RIVAL —
// a pure function of the save's seed (the player's name and
// created appearance, the two facts chosen when the save is
// born and carried in every save file), drawn from an
// authored pool of seven kids whose data supports a
// competitive streak. The pick is revealed in a played
// moment — you both reach for the same small glory — and it
// is stable for the life of the save: once recorded it is
// only ever read back. A pre-0.8.0e save gains its rival by
// exactly the same rule the first time the system is
// consulted.
//
// RECONCILIATION, honestly. The town already had three
// things with "rival" in the name, and none of them is
// this: Cartman & Kyle's arch-rivalry is THEIRS (a relation
// note between two NPCs); the factions' declared rivals are
// factions' business (the missions' RIVAL_COST); and the
// whoopee cushion of Engine #57 is a friendship-tier mood —
// ANY kid who currently hates you may leave one. This is
// the player's own rival: one named kid, a scoreboard, and
// an arc.
//
// THE CONTESTS. A few a month at most, always invitations,
// declining always free. Four kinds are OFFERED in person
// (the Talk gate's RIVAL rail, below the emergencies — a
// disaster outranks a grudge — and above a fresh ask):
//   ❄️ SHOWDOWN — the real duel, on their real dodge stat
//      (the settle rides applyShowdownResult's funnel).
//   🎣 FISHING — one cast each at the pond: yours is the
//      cast you actually make (applyCatch's funnel), theirs
//      is rolled on the same FISH_TABLE with their authored
//      luck — rarer beats bigger, and a boot is not a fish.
//   🏃 THE RACE — the one new shape: a footrace judged by
//      the honest clock. Your time is your real elapsed
//      minutes (Walk-priced ticks and all); theirs is the
//      Walk graph's own route minutes plus their authored
//      pace, posted in half-minutes. You post whole minutes
//      (an even count of half-minutes), they post odd — a
//      tie is arithmetically impossible, by construction.
//   📸 FOLLOWERS — first to the next Photos milestone: your
//      real follower count against their tally, which starts
//      shoulder-to-shoulder with yours and grows only at
//      their authored posting pace, settled at the rollover
//      so a same-day photo-finish is judged by the day, then
//      the overshoot — never by whose code ran first.
// Two kinds are STANDING contests, judged at their own
// funnels whenever you were both in them:
//   🐝 THE BEE — School.finishBee: if the rival was in the
//      field, best finish takes it (the bracket is the
//      bracket; their rounds are the bracket's own math).
//   🎬 CLIMAX — Episodes.finishEvent: if the rival was a
//      causer and your tier was PLAYED, theirs is rolled
//      from their authored nerve on the same three tiers.
// FAIRNESS IS THE DESIGN: every rival outcome derives from
// their authored stats exactly as yours derives from play —
// the suite simulates all of it and shows them winning a
// real share, including beating a perfect race time when
// their pace says they should.
//
// THE SCORE & THE ARC. Every contest lands on the Journal's
// 🆚 THE RIVAL page (the capped log; the counters never
// cap), plants "the rivalry" in the rival's memory (Threads
// carries the follow-ups), earns the town's gossip on the
// owed-lines rail for a few days, and a phone text the next
// morning — a taunt when they're ahead, grudging respect
// when they're not. Now and then the rival pulls a PRANK
// (authored, signed, cosmetic only — a dressing line at one
// spot, gone when answered or expired; it touches no other
// system's state, and the suite proves a full prank cycle
// leaves the rest of the save byte-identical). You answer
// PRANK BACK or LET IT GO, and the pattern of your answers
// matures the rivalry into a standing — WORTHY OPPONENT,
// NEMESIS, or FRIENDLY RIVAL — computed from the response
// history, never chosen. At LOCK_AT contests the standing
// locks with one small played beat: a handshake, a shove,
// or a shared laugh, by standing.
//
// DEFERENCE. Challenges, the reveal, the lock beat and
// prank answers never fire while Emergencies.momentOwned
// owns the town (a case climax, a finale week, a holiday
// beat, a live screening, bee hours, a capstone ceremony)
// or while an emergency's clock is live. The standing
// contests judge at funnels that ARE the set pieces, so
// nothing ever stacks. The rival's other life is untouched:
// agendas, pair scripts, story arcs, dinners, votes and
// audiences all run exactly as authored.
//
// STATE: the life/lifeText maps only — no new save keys, no
// new GameState fields. A v0.8.0d save loads with no rival
// recorded and gains one by the rule above.
// ============================================================

object Rival {

    // ---------------- the dials (tuning ledger) ----------------
    const val REVEAL_FROM = 1          // first absDay the reveal may be due
    const val REVEAL_SPREAD = 3        // due day = REVEAL_FROM + seed % 3 (days 1–3)
    const val FIRST_CHALLENGE_GAP = 2  // quiet days between the reveal and challenge #1
    const val CHALLENGE_GAP_MIN = 7    // settled challenge → next: this…
    const val CHALLENGE_GAP_SPREAD = 5 // …plus a seeded 0–4 (a few a month, at most)
    const val DECLINE_GAP = 5          // a declined challenge waits this (+ seed % 3)
    const val PENDING_LAPSE_DAYS = 4   // an armed contest unsettled this long lapses, free
    const val PRANK_FIRST_DELAY = 9    // first prank no earlier than this after the reveal
    const val PRANK_GAP_MIN = 16       // pranks are occasional: this…
    const val PRANK_GAP_SPREAD = 11    // …plus a seeded 0–10 between them
    const val PRANK_EXPIRE_DAYS = 5    // an unanswered prank evaporates
    const val PRANK_CHANCE = 0.6       // seeded chance a due prank actually lands
    const val FRESH_DAYS = 3           // a result is gossip for this many days
    const val LOCK_AT = 10             // contests on the board when the standing locks
    const val LOG_CAP = 24             // contest lines kept on the page (counters never cap)
    const val RACE_MIN_ROUTE = 10      // a race leg is at least this many Walk minutes

    const val TOPIC = "the rivalry"

    // standings
    const val WORTHY = "worthy"
    const val NEMESIS = "nemesis"
    const val FRIENDLY = "friendly"

    // contest kinds
    const val K_SHOWDOWN = "showdown"
    const val K_FISHING = "fishing"
    const val K_RACE = "race"
    const val K_FOLLOWERS = "followers"
    const val K_BEE = "bee"
    const val K_CLIMAX = "climax"
    val OFFERED_KINDS = listOf(K_SHOWDOWN, K_FISHING, K_RACE, K_FOLLOWERS)
    val ALL_KINDS = listOf(K_SHOWDOWN, K_FISHING, K_RACE, K_FOLLOWERS, K_BEE, K_CLIMAX)

    fun kindLabel(kind: String): String = when (kind) {
        K_SHOWDOWN -> "the Snowball Showdown"
        K_FISHING -> "the fishing duel"
        K_RACE -> "the cross-town race"
        K_FOLLOWERS -> "the follower race"
        K_BEE -> "the spelling bee"
        K_CLIMAX -> "the climax"
        // v0.9.0e: the crew contests share the board (and
        // so its labels) — RivalCrew owns their rules
        RivalCrew.K_CREW_RELAY -> "the crew relay"
        RivalCrew.K_CREW_PHOTO -> "the crew photo showdown"
        RivalCrew.K_CREW_SHOWDOWN -> "the crew showdown bracket"
        else -> "the contest"
    }

    fun kindEmoji(kind: String): String = when (kind) {
        K_SHOWDOWN -> "❄️"
        K_FISHING -> "🎣"
        K_RACE -> "🏃"
        K_FOLLOWERS -> "📸"
        K_BEE -> "🐝"
        K_CLIMAX -> "🎬"
        RivalCrew.K_CREW_RELAY -> "🏃"
        RivalCrew.K_CREW_PHOTO -> "📸"
        RivalCrew.K_CREW_SHOWDOWN -> "❄️"
        else -> "🆚"
    }

    // ---------------- the pool ----------------

    /** One eligible rival. [dodge] and spelling skill are NOT
     *  stored here — they are read live from Showdown.dodgeFor
     *  and School.skillOf, the town's own tables, so a contest
     *  can never grade the rival on a number the real games
     *  don't use. What lives here is what only the rivalry
     *  needs: [nerve] (the climax roll), [paceHalf] (the race
     *  offset in half-minutes — ALWAYS ODD, the parity the
     *  no-tie proof stands on), [castLuck] (the fishing
     *  table's weight shift) and [postPace] (followers a day
     *  while a follower race is armed). */
    data class RivalDef(
        val name: String, val why: String,
        val nerve: Int, val paceHalf: Int, val castLuck: Int, val postPace: Int,
        val crownLine: String, val offerLine: String,
        val winLine: String, val loseLine: String
    )

    val POOL: List<RivalDef> = listOf(
        RivalDef("Cartman",
            "Everything is a contest and he is the prize committee; he has " +
                "been keeping score against the new kid since the bus stop.",
            nerve = 7, paceHalf = -3, castLuck = 2, postPace = 3,
            crownLine = "Screw you guys — it's official. You and me, " +
                "{you}. Everybody else is just audience.",
            offerLine = "New contest. I invented it, I'll win it, " +
                "you'll tell people it was close.",
            winLine = "Nyeh nyeh. The scoreboard grows, {you}. It GROWS.",
            loseLine = "You cheated. I don't know how yet, but the " +
                "investigation is fully funded."),
        RivalDef("Kyle",
            "A principled competitor: he would rather lose honestly than " +
                "win ugly, and he has decided your rise needs auditing.",
            nerve = 8, paceHalf = 1, castLuck = 0, postPace = 1,
            crownLine = "Okay. For the record: this is a rivalry now. " +
                "Somebody in this town has to keep you honest, {you}.",
            offerLine = "I made a spreadsheet. There's a tab for today. " +
                "It's a contest, {you}. A fair one.",
            winLine = "Fair and square, {you}. That's the part that " +
                "should worry you.",
            loseLine = "Fine. FINE. The spreadsheet says what it says. " +
                "Rematch, though. Obviously rematch."),
        RivalDef("Wendy",
            "Top of the class, first in every line: she does not lose, " +
                "and she has questions about somebody who might make her.",
            nerve = 9, paceHalf = -1, castLuck = 1, postPace = 2,
            crownLine = "Let's be clear about what this is, {you}: I win " +
                "things here. You're the first variable. Variables get solved.",
            offerLine = "I've scheduled us a contest. It's in my planner " +
                "under 'inevitable'. Show up, {you}.",
            winLine = "As projected. Nothing personal, {you} — I just " +
                "did the reading.",
            loseLine = "I am going to think about this for a normal " +
                "amount of time. Which is until I beat you."),
        RivalDef("Craig",
            "Deadpan on the outside, a scoreboard on the inside; his " +
                "gang already runs rematches as a way of life.",
            nerve = 6, paceHalf = -1, castLuck = 0, postPace = 1,
            crownLine = "Whatever. You're my rival now, I guess. " +
                "Don't make it weird, {you}. It's just... official.",
            offerLine = "Contest. Today. If I win I will be very " +
                "quiet about it, {you}, which is worse.",
            winLine = "Huh. Anyway. Scoreboard, {you}.",
            loseLine = "Cool. Great. Love that for the rivalry, {you}. " +
                "Rematch whenever. I'll be equally thrilled."),
        RivalDef("Clyde",
            "He keeps lists — best dressed, most datable, top five " +
                "everything — and the new kid keeps placing on them.",
            nerve = 5, paceHalf = 5, castLuck = -1, postPace = 2,
            crownLine = "I made a list, {you}: 'People Clyde Has To " +
                "Beat'. It's laminated. You're the whole list.",
            offerLine = "I posted about this contest already, so we " +
                "kind of HAVE to do it now, {you}. For the followers.",
            winLine = "AND the crowd goes mildly impressed. That's " +
                "another one for me, {you}.",
            loseLine = "This is NOT going in the recap post, {you}. " +
                "The recap post is cancelled. Rematch post pending."),
        RivalDef("Bebe",
            "The girls' circle's intelligence service runs on contests " +
                "nobody admits are contests; Bebe admits it, which is worse.",
            nerve = 6, paceHalf = 3, castLuck = 1, postPace = 3,
            crownLine = "Okay so — rivalry. You and me, {you}. I already " +
                "told three people, so it's basically law now.",
            offerLine = "New contest, {you}. I picked it, I look great " +
                "in it, and winning is my whole aesthetic today.",
            winLine = "Told you. The scoreboard is SO cute when it's " +
                "winning, {you}.",
            loseLine = "Ugh. Fine. That one's yours, {you}. I'm letting " +
                "people know it was close. It wasn't. Rematch."),
        RivalDef("Tweek",
            "Pressure makes him faster, and nobody in town applies " +
                "pressure like a kid with something to prove to him.",
            nerve = 4, paceHalf = -5, castLuck = -1, postPace = 1,
            crownLine = "Gah! Fine! FINE! Rivalry! You and me, {you}! " +
                "It's too much pressure and I LOVE it — I hate it — BOTH!",
            offerLine = "I drank three coffees about this, {you}. " +
                "Contest. NOW. Before the jitters become performance art.",
            winLine = "I WON?! I mean — I WON. Naturally. Gah. The " +
                "scoreboard was RIGHT to fear me, {you}.",
            loseLine = "Too... much... pressure... which is EXACTLY " +
                "what a rematch is for, {you}. Everything is rematch-shaped now."),
        // v0.9.0a wave 5: the kid whose entire career is being
        // somebody's nemesis joins the pool he was born for.
        RivalDef("Nathan",
            "A professional nemesis: he has a five-year plan, a " +
                "notebook of your weaknesses (mostly 'is nice'), and " +
                "a standing grudge against the whole concept of your " +
                "winning streak.",
            nerve = 8, paceHalf = 1, castLuck = 1, postPace = 2,
            crownLine = "It is decided, {you}. Every operation needs " +
                "an adversary, and yours lacked... me. Consider the " +
                "position filled. Permanently.",
            offerLine = "A new contest, {you}. The terms are fair, " +
                "the notebook has verified them twice, and I have " +
                "already cleared my afternoon for the victory.",
            winLine = "All according to plan, {you}. The scoreboard " +
                "is merely the plan's most public page.",
            loseLine = "A rehearsal, {you}. The operation does not " +
                "lose; it collects... data. The rematch will be " +
                "educational. For you."),
        // v0.11.0d wave 6: the ORIGINAL rival — the geneticist's
        // son, who was scheming against the boys before Nathan
        // had a notebook — appends LAST, so every crown recorded
        // in an old save keeps its seat and its meaning.
        RivalDef("Terrance Mephesto",
            "The original rival: a Mephesto, raised on schemes " +
                "with a science budget, who once cloned a classmate " +
                "just to win a fair. Your winning streak is, to " +
                "him, a rough draft he intends to annotate.",
            nerve = 7, paceHalf = -3, castLuck = 1, postPace = 1,
            crownLine = "It is hereditary, {you}. Every legacy needs " +
                "a rival, and mine has lacked... you. Consider the " +
                "position bred for. Permanently.",
            offerLine = "A new contest, {you}. The terms are " +
                "hereditary, the notebook has annotated them twice, " +
                "and the ranch has already cleared the afternoon " +
                "for the result.",
            winLine = "As the data predicted, {you}. The scoreboard " +
                "is merely heredity's most public page.",
            loseLine = "A first draft, {you}. I annotate my drafts. " +
                "The second edition of this contest will be... " +
                "peer-reviewed. By you. Unwillingly.")
    )

    fun defOf(name: String): RivalDef? = POOL.firstOrNull { it.name == name }

    // ---------------- the seed & the pick ----------------

    /** The save's seed: a hash of the two facts chosen when
     *  the save was born — the player's name and their created
     *  appearance — both carried in every save file, old or
     *  new. Computed once, then stored and only read back, so
     *  a later trip to the mirror can never re-crown anyone. */
    fun saveSeed(state: GameState): Int {
        state.life["rivalSeed"]?.let { return it }
        val me = state.agents.values.firstOrNull { it.isPlayer }
        val basis = (me?.name ?: PLAYER_DEFAULT_NAME) + "|" +
            (me?.appearance?.entries?.sortedBy { it.key }
                ?.joinToString(",") { "${it.key}=${it.value}" } ?: "")
        var h = basis.hashCode()
        if (h == Int.MIN_VALUE) h = 0
        if (h < 0) h = -h
        state.life["rivalSeed"] = h
        return h
    }

    /** The pick, pure: the same seed crowns the same rival,
     *  on a fresh save and on a legacy one alike. */
    fun pickFor(state: GameState): String {
        val seed = saveSeed(state)
        return POOL[Random(seed * 31L + 7L).nextInt(POOL.size)].name
    }

    /** The save's rival: the recorded name once the rollover
     *  has written it, the pure pick until then — one answer
     *  either way, for the life of the save. */
    fun rivalOf(state: GameState): String =
        state.lifeText["rival"]?.takeIf { defOf(it) != null } ?: pickFor(state)

    fun revealed(state: GameState): Boolean =
        (state.life["rivalRevealedDay"] ?: 0) > 0

    fun revealedDay(state: GameState): Int = state.life["rivalRevealedDay"] ?: 0

    /** The day the reveal falls due: the save's first days. A
     *  legacy save is long past it — the reveal is due the
     *  first time the rival is consulted. */
    fun revealDueDay(state: GameState): Int =
        REVEAL_FROM + saveSeed(state) % REVEAL_SPREAD

    fun revealDue(state: GameState): Boolean =
        !revealed(state) && state.absDay >= revealDueDay(state)

    // ---------------- the score ----------------

    fun winsYou(state: GameState): Int = state.life["rivalYou"] ?: 0
    fun winsThem(state: GameState): Int = state.life["rivalThem"] ?: 0
    fun draws(state: GameState): Int = state.life["rivalDraws"] ?: 0
    fun contestsPlayed(state: GameState): Int =
        winsYou(state) + winsThem(state) + draws(state)

    data class RivalContest(val day: Int, val kind: String,
                            val winner: String, val detail: String)

    fun contestLog(state: GameState): List<RivalContest> {
        val raw = state.lifeText["rivalLog"] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            val p = e.split("|")
            if (p.size != 4) null
            else RivalContest(p[0].toIntOrNull() ?: return@mapNotNull null,
                p[1], p[2], p[3])
        }
    }

    private fun numberWord(n: Int): String = when (n) {
        1 -> "one"; 2 -> "two"; 3 -> "three"; 4 -> "four"; 5 -> "five"
        6 -> "six"; 7 -> "seven"; 8 -> "eight"; 9 -> "nine"; 10 -> "ten"
        11 -> "eleven"; 12 -> "twelve"; else -> n.toString()
    }

    /** The score, stated in words — the one factual sentence
     *  every surface (page, owed line, gossip, text) quotes,
     *  so they can never disagree about who's ahead. */
    fun scoreLine(state: GameState): String {
        val rival = rivalOf(state)
        val you = winsYou(state); val them = winsThem(state); val dr = draws(state)
        val drawsBit = if (dr > 0) ", with ${numberWord(dr)} drawn" else ""
        fun margin(n: Int) = "by ${numberWord(n)} " +
            if (n == 1) "contest." else "contests."
        return when {
            you > them -> "You're ahead of $rival, $you–$them$drawsBit — " +
                margin(you - them)
            them > you -> "$rival leads you $them–$you$drawsBit — " +
                margin(them - you)
            else -> "You and $rival are level at $you–$them$drawsBit."
        }
    }

    /** One-sentence account of the latest result, composed
     *  from the recorded fields only — gossip can embroider
     *  the telling, never the facts. */
    fun lastResultSentence(state: GameState): String? {
        val last = contestLog(state).lastOrNull() ?: return null
        val rival = rivalOf(state)
        val kind = kindLabel(last.kind)
        return when (last.winner) {
            "you" -> "the new kid took $kind off $rival"
            "them" -> "$rival took $kind off the new kid"
            else -> "$kind ended in a draw between the new kid and $rival"
        }
    }

    private fun pushTicker(state: GameState, text: String) {
        state.events.add(text)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    private fun plantTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** The central settle: every contest, however judged,
     *  lands here exactly once — the counters, the capped
     *  log, the freshness fields, the memory topic, the
     *  morning-report beat, the next challenge's scheduling.
     *  No friendship, no coins, no other system's state: the
     *  scoreboard is the whole stakes. */
    fun recordResult(state: GameState, kind: String, winner: String,
                     detail: String) {
        when (winner) {
            "you" -> state.life["rivalYou"] = winsYou(state) + 1
            "them" -> state.life["rivalThem"] = winsThem(state) + 1
            else -> state.life["rivalDraws"] = draws(state) + 1
        }
        val kept = (contestLog(state) + RivalContest(state.absDay,
            kind, winner,
            detail.replace("|", " ").replace(";", " "))).takeLast(LOG_CAP)
        state.lifeText["rivalLog"] = kept.joinToString(";") {
            "${it.day}|${it.kind}|${it.winner}|${it.detail}" }
        state.life["rivalLastDay"] = state.absDay
        state.lifeText["rivalLastKind"] = kind
        state.lifeText["rivalLastWinner"] = winner
        if (pendingKind(state) == kind) clearPending(state)
        val rival = rivalOf(state)
        plantTopic(rival)
        val emoji = kindEmoji(kind)
        state.lifeText["rivalBeat"] = "$emoji ${lastResultSentence(state)} — $detail."
        state.life["rivalBeatDay"] = state.absDay
        state.life["rivalNextDay"] = state.absDay + CHALLENGE_GAP_MIN +
            Random(saveSeed(state) * 17L + state.absDay).nextInt(CHALLENGE_GAP_SPREAD)
        // v0.9.0b: the player's crew, standing with them,
        // witnessed this one — the tallies live on their
        // page, and this page quotes the total below
        Crew.onContest(state)
    }

    // ---------------- pending (armed) contests ----------------

    fun pendingKind(state: GameState): String? =
        state.lifeText["rivalPending"]?.takeIf { it.isNotEmpty() }

    private fun armPending(state: GameState, kind: String) {
        state.lifeText["rivalPending"] = kind
        state.life["rivalPendingDay"] = state.absDay
    }

    private fun clearPending(state: GameState) {
        state.lifeText.remove("rivalPending")
        state.life.remove("rivalPendingDay")
    }

    // ---------------- challenges ----------------

    /** The first challenge falls due this many days after the
     *  reveal; afterwards the settle/decline stamps rule. */
    fun nextChallengeDay(state: GameState): Int =
        state.life["rivalNextDay"]
            ?: (revealedDay(state) + FIRST_CHALLENGE_GAP)

    fun challengeLive(state: GameState): Boolean =
        revealed(state) && pendingKind(state) == null &&
            state.absDay >= nextChallengeDay(state)

    /** Today's offered kind, a pure function of the day, the
     *  rival and the weather: the showdown needs snow on the
     *  ground, the pond needs to be liquid, and the follower
     *  race needs a milestone left to race to. */
    fun offerKindFor(state: GameState): String {
        val snow = state.weatherId in setOf("light_snow", "heavy_snow", "blizzard")
        val kinds = OFFERED_KINDS.filter { k ->
            when (k) {
                K_SHOWDOWN -> snow
                K_FISHING -> state.weatherId != "blizzard"
                K_FOLLOWERS -> state.followers < Photos.MILESTONES.last()
                else -> true
            }
        }
        val seed = state.absDay * 131L + rivalOf(state).hashCode()
        return kinds[Random(seed).nextInt(kinds.size)]
    }

    fun offerText(state: GameState, kind: String): String {
        val def = defOf(rivalOf(state)) ?: POOL[0]
        val you = playerNameOf(state)
        return def.offerLine.replace("{you}", you) + "\n\n" +
            when (kind) {
                K_SHOWDOWN -> "❄️ ${kindLabel(kind)} — the real duel, " +
                    "first to 3. The fort is neutral ground. It is not."
                K_FISHING -> "🎣 ${kindLabel(kind)} — one cast each at " +
                    "Stark's Pond. Yours is the next fish you actually " +
                    "catch there; theirs gets rolled the moment yours " +
                    "lands. Rarer beats bigger. A boot is not a fish."
                K_RACE -> "🏃 ${kindLabel(kind)} — accept, and it's on: " +
                    "get to the named place. Your minutes are your real " +
                    "minutes; theirs is the route's own time plus their " +
                    "pace, and the stopwatch splits halves — no ties, ever."
                K_FOLLOWERS -> "📸 ${kindLabel(kind)} — first to the next " +
                    "follower milestone. Yours is your real count; theirs " +
                    "posts at their own pace in the night. Judged at the " +
                    "morning rollover, by the day, then the overshoot."
                else -> ""
            } + "\n\nDeclining costs nothing. It always costs nothing."
    }

    private fun playerNameOf(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.name ?: PLAYER_DEFAULT_NAME

    /** The gate query: what business a Talk with [npcName]
     *  has with the rivalry right now. Pure reads only. The
     *  order is the design: the crowning before the locking,
     *  the debt (a prank's answer) before a fresh challenge,
     *  and everything waits while the town's bigger moments
     *  own the hour. */
    fun talkStepFor(state: GameState, npcName: String): RivalHit? {
        if (npcName != rivalOf(state)) return null
        if (Emergencies.momentOwned(state)) return null
        if (Emergencies.liveEmergency(state) != null) return null
        if (revealDue(state)) return RivalHit("reveal")
        if (lockReady(state)) return RivalHit("lock")
        if (prankActive(state) && prankFound(state)) return RivalHit("prank")
        if (pendingKind(state) == K_SHOWDOWN) return RivalHit("resume")
        // v0.9.0e: an armed crew bracket is owed its
        // captains' duel before anything new is offered
        if (RivalCrew.bracketPending(state))
            return RivalHit("crewresume")
        // v0.9.0e: THE CREWS FINAL outranks a solo offer on
        // its day — the season closes once a year
        if (RivalCrew.finalDue(state) && !raceLive(state) &&
            !Barks.inQuietHours(state.timeMin))
            return RivalHit("crewoffer",
                RivalCrew.offerKindFor(state), crewFinal = true)
        if (challengeLive(state) && !raceLive(state) &&
            !Barks.inQuietHours(state.timeMin))
            return RivalHit("offer", offerKindFor(state))
        // v0.9.0e: a due crew challenge takes the rail when
        // no solo offer claimed it (RivalCrew's own due
        // checks exclude armed crew pendings and the final)
        if (RivalCrew.offerDue(state) && !raceLive(state) &&
            !Barks.inQuietHours(state.timeMin))
            return RivalHit("crewoffer",
                RivalCrew.offerKindFor(state))
        return null
    }

    data class RivalHit(val kind: String, val offerKind: String = "",
                        val crewFinal: Boolean = false)

    /** Take a challenge on. The immediate kinds start now;
     *  the passive kinds arm and wait for their funnel. */
    fun accept(state: GameState, kind: String): RivalAccept {
        val rival = rivalOf(state)
        return when (kind) {
            K_SHOWDOWN -> {
                armPending(state, K_SHOWDOWN)
                RivalAccept(kind, "❄️ It's on — the Snowball Showdown, " +
                    "you vs $rival, first to 3. Talk to $rival again to " +
                    "step to the fort if the moment gets away from you.")
            }
            K_FISHING -> {
                armPending(state, K_FISHING)
                RivalAccept(kind, "🎣 Armed: your next catch at Stark's " +
                    "Pond is the duel. $rival's cast gets rolled the " +
                    "moment yours lands.")
            }
            K_RACE -> {
                val from = state.agents.values.firstOrNull { it.isPlayer }
                    ?.location ?: "BusStop"
                val cands = LOCATIONS.keys.filter { it != from &&
                    Walk.routeMinutes(state, from, it) >= RACE_MIN_ROUTE }
                    .sorted()
                val target = if (cands.isEmpty())
                    LOCATIONS.keys.filter { it != from }
                        .maxByOrNull { Walk.routeMinutes(state, from, it) }
                        ?: "StarksPond"
                else cands[Random(saveSeed(state) * 7L + state.absDay * 3L)
                    .nextInt(cands.size)]
                state.lifeText["rivalRaceFrom"] = from
                state.lifeText["rivalRaceTo"] = target
                state.life["rivalRaceStartMin"] = state.timeMin
                state.life["rivalRaceStartCarry"] = state.walkCarryMin
                state.life["rivalRaceDay"] = state.absDay
                state.lifeText["rivalPendingRace"] = K_RACE
                val place = LOCATIONS[target]?.name ?: target
                RivalAccept(kind, "🏃 THE RACE IS ON: get to $place. " +
                    "Your clock is already running — every real minute " +
                    "counts, detours and all. $rival's time is the " +
                    "route's own minutes plus their pace, posted to " +
                    "the half-minute. No ties. Ever.", target)
            }
            K_FOLLOWERS -> {
                val jitter = Random(saveSeed(state) * 13L + state.absDay)
                    .nextInt(9) - 4
                state.life["rivalFollowers"] =
                    (state.followers + jitter).coerceAtLeast(0)
                val target = Photos.MILESTONES.first {
                    it > maxOf(state.followers, state.life["rivalFollowers"] ?: 0) }
                state.life["rivalTarget"] = target
                state.life.remove("rivalRaceYouDay")
                armPending(state, K_FOLLOWERS)
                RivalAccept(kind, "📸 The follower race is on: first to " +
                    "$target followers. Yours is the real count under " +
                    "your selfies; theirs posts at their pace while " +
                    "the town sleeps. Judged at the morning rollover.")
            }
            else -> RivalAccept(kind, "")
        }
    }

    data class RivalAccept(val kind: String, val text: String,
                           val raceTarget: String? = null)

    /** Declining: exactly nothing happens except the calendar
     *  moves — no score, no friendship, no grudge. Ever. */
    fun decline(state: GameState) {
        state.life["rivalNextDay"] = state.absDay + DECLINE_GAP +
            Random(saveSeed(state) + state.absDay).nextInt(3)
    }

    // ---------------- the race (the honest clock) ----------------

    fun raceLive(state: GameState): Boolean =
        state.lifeText["rivalPendingRace"] == K_RACE &&
            (state.life["rivalRaceDay"] ?: -1) == state.absDay

    /** Their posted time for the live race, in half-minutes:
     *  the Walk graph's own route minutes, doubled, plus their
     *  authored pace — an ODD number of half-minutes, against
     *  your always-EVEN count of whole minutes. The parity is
     *  the no-tie proof. */
    fun rivalRaceHalves(state: GameState): Int? {
        val from = state.lifeText["rivalRaceFrom"] ?: return null
        val to = state.lifeText["rivalRaceTo"] ?: return null
        val def = defOf(rivalOf(state)) ?: return null
        return Walk.routeMinutes(state, from, to) * 2 + def.paceHalf
    }

    fun halvesLabel(halves: Int): String {
        val mins = halves / 2
        return if (halves % 2 == 0) "$mins min" else "$mins½ min"
    }

    /** Arrival: GameActivity asks on every refresh; the race
     *  answers exactly once, when the player stands at the
     *  target on the race's own day. */
    fun raceArrival(state: GameState, locId: String): String? {
        if (!raceLive(state)) return null
        if (locId != state.lifeText["rivalRaceTo"]) return null
        val elapsed = (state.timeMin - (state.life["rivalRaceStartMin"] ?: state.timeMin)) +
            (state.walkCarryMin - (state.life["rivalRaceStartCarry"] ?: state.walkCarryMin))
        val rivalHalves = rivalRaceHalves(state) ?: return null
        state.lifeText.remove("rivalPendingRace")
        if (elapsed < 1 || elapsed > 600) return null   // the day got away; it never happened
        val youHalves = elapsed * 2
        val winner = if (youHalves < rivalHalves) "you" else "them"
        val place = LOCATIONS[locId]?.name ?: locId
        val detail = "$place — you ${halvesLabel(youHalves)}, " +
            "${rivalOf(state)} ${halvesLabel(rivalHalves)}"
        recordResult(state, K_RACE, winner, detail)
        return "🏃 $detail. " + if (winner == "you")
            "You took the race." else "${rivalOf(state)} took the race."
    }

    // ---------------- contest funnels ----------------

    /** applyShowdownResult's funnel: a duel against the rival
     *  while a showdown contest is armed IS the contest. */
    fun onShowdown(state: GameState, youWon: Boolean, opponent: String) {
        if (opponent != rivalOf(state)) return
        if (pendingKind(state) != K_SHOWDOWN) return
        recordResult(state, K_SHOWDOWN, if (youWon) "you" else "them",
            "the fort, first to 3")
    }

    /** A cast's rank on the table: rarer (lower weight) wins;
     *  the boot is not a fish and ranks below everything. */
    fun fishRank(sp: FishSpecies): Int = if (sp.joke) 999 else sp.weight

    /** The rival's cast: the same FISH_TABLE, their authored
     *  luck shifting the weights (never below 1), size rolled
     *  inside the species' own range. A seed replays it. */
    fun rivalCast(def: RivalDef, seed: Long): Pair<FishSpecies, Int> {
        val rnd = Random(seed)
        fun effW(sp: FishSpecies): Int =
            (sp.weight + def.castLuck).coerceAtLeast(1)
        val roll = rnd.nextInt(FISH_TABLE.sumOf { effW(it) })
        var acc = 0
        var caught = FISH_TABLE.last()
        for (sp in FISH_TABLE) {
            acc += effW(sp)
            if (roll < acc) { caught = sp; break }
        }
        val cm = caught.minCm + rnd.nextInt(caught.maxCm - caught.minCm + 1)
        return caught to cm
    }

    /** applyCatch's funnel: your first catch while a fishing
     *  contest is armed settles it, against a cast rolled on
     *  the same table at the same moment. */
    fun onCatch(state: GameState, sp: FishSpecies, cm: Int) {
        if (pendingKind(state) != K_FISHING) return
        val def = defOf(rivalOf(state)) ?: return
        val seed = state.absDay * 1000003L + contestsPlayed(state) * 7919L +
            saveSeed(state)
        val (rsp, rcm) = rivalCast(def, seed)
        val winner = when {
            fishRank(sp) != fishRank(rsp) ->
                if (fishRank(sp) < fishRank(rsp)) "you" else "them"
            cm != rcm -> if (cm > rcm) "you" else "them"
            else -> "draw"
        }
        pushTicker(state, "🆚 ${kindEmoji(K_FISHING)} The fishing duel: your " +
            "${sp.name} ($cm cm) vs ${def.name}'s ${rsp.name} ($rcm cm) — " +
            when (winner) {
                "you" -> "yours takes it."
                "them" -> "theirs takes it."
                else -> "a dead heat. The pond judges nobody."
            })
        recordResult(state, K_FISHING, winner,
            "your ${sp.name} $cm cm vs their ${rsp.name} $rcm cm")
    }

    /** School.finishBee's funnel: both of you in the field →
     *  best finish takes it. Their rounds are the bracket's
     *  own remainingAfter lists; yours are the record's. */
    fun onBeeSettled(state: GameState, rec: School.BeeRecord) {
        if (!revealed(state)) return
        val rival = rivalOf(state)
        if (rival !in School.beeField(rec.day)) return
        if (contestLog(state).any { it.day == rec.day && it.kind == K_BEE }) return
        val bracket = School.beeBracket(rec.day)
        val winner = when {
            rec.won -> "you"
            rec.other == rival -> "them"
            else -> {
                val rivalRounds = bracket.rounds.count {
                    rival in it.remainingAfter }
                when {
                    rec.rounds > rivalRounds -> "you"
                    rec.rounds < rivalRounds -> "them"
                    else -> "draw"
                }
            }
        }
        val detail = if (rec.won) "you took the trophy; they were in the field"
            else "the trophy went to ${rec.other}"
        recordResult(state, K_BEE, winner, detail)
    }

    /** Their climax tier, rolled from their authored nerve on
     *  the same three tiers the player is graded on, seeded
     *  by the episode so the same climax grades them the same
     *  way on every read. */
    fun rivalClimaxTier(def: RivalDef, ep: EpisodeRecord): String {
        val roll = def.nerve + Random(
            ep.key.hashCode().toLong() * 31L + saveSeedOf(ep)).nextInt(5)
        return when {
            roll >= 12 -> Climaxes.TIER_HEROIC
            roll >= 8 -> Climaxes.TIER_HANDLED
            else -> Climaxes.TIER_FUMBLED
        }
    }

    // the episode's own facts seed their roll; the save seed
    // is not readable off an episode, so the roll keys on the
    // episode key + its day — deterministic per episode, which
    // is the property the settle-once funnel actually needs.
    private fun saveSeedOf(ep: EpisodeRecord): Long = ep.dayKey.hashCode().toLong()

    private fun tierRank(tier: String): Int = when (tier) {
        Climaxes.TIER_HEROIC -> 3
        Climaxes.TIER_HANDLED -> 2
        Climaxes.TIER_FUMBLED -> 1
        else -> 0
    }

    /** Episodes.finishEvent's landing (called from Engine's
     *  rollover, on the finished record): the rival was a
     *  causer, your tier was PLAYED — theirs is rolled and
     *  compared on the same scale. Legacy and absent tiers
     *  judge nothing: there is no version of yours to beat. */
    fun onClimaxSettled(state: GameState, ep: EpisodeRecord) {
        if (!revealed(state)) return
        if (!ep.finished || ep.kind != "event") return
        val rival = rivalOf(state)
        if (rival !in ep.causers) return
        val yours = Episodes.climaxTierOf(ep)
        if (tierRank(yours) == 0 || yours == "legacy") return
        if (contestLog(state).any {
                it.kind == K_CLIMAX && it.detail.startsWith(ep.key) }) return
        val def = defOf(rival) ?: return
        val theirs = rivalClimaxTier(def, ep)
        val winner = when {
            tierRank(yours) > tierRank(theirs) -> "you"
            tierRank(yours) < tierRank(theirs) -> "them"
            else -> "draw"
        }
        recordResult(state, K_CLIMAX, winner,
            "${ep.key} — your ${Climaxes.tierLabel(yours)} vs their " +
                Climaxes.tierLabel(theirs))
    }

    /** Photos' milestone funnel: your crossing is RECORDED,
     *  never settled on the spot — the race judges at the
     *  rollover, by the day, so a photo-finish can't be won
     *  by whose code ran first. */
    fun onFollowers(state: GameState) {
        if (pendingKind(state) != K_FOLLOWERS) return
        if ((state.life["rivalRaceYouDay"] ?: 0) == 0)
            state.life["rivalRaceYouDay"] = state.absDay
    }

    // ---------------- standings ----------------

    fun escalations(state: GameState): Int = state.life["rivalEsc"] ?: 0
    fun mercies(state: GameState): Int = state.life["rivalMercy"] ?: 0

    /** The standing, computed from the response history:
     *  mercy ahead is FRIENDLY, escalation ahead is NEMESIS,
     *  and everything contested clean in between is WORTHY.
     *  Before the lock it is a live read; at the lock the
     *  read of that moment is what gets recorded. */
    fun standingOf(state: GameState): String {
        state.lifeText["rivalStanding"]?.takeIf { it.isNotEmpty() }
            ?.let { return it }
        val esc = escalations(state); val mer = mercies(state)
        return when {
            mer > esc && mer >= 2 -> FRIENDLY
            esc > mer && esc >= 2 -> NEMESIS
            else -> WORTHY
        }
    }

    fun standingLabel(standing: String): String = when (standing) {
        NEMESIS -> "NEMESIS"
        FRIENDLY -> "FRIENDLY RIVAL"
        else -> "WORTHY OPPONENT"
    }

    fun standingLine(state: GameState): String = when (standingOf(state)) {
        NEMESIS -> "Every courtesy has been answered with escalation. " +
            "This is a feud now, and the town sells popcorn."
        FRIENDLY -> "You keep letting them off the hook, and they keep " +
            "almost thanking you. It's basically friendship with a scoreboard."
        else -> "You contest everything and keep it clean. The town " +
            "respects the fixture."
    }

    fun locked(state: GameState): Boolean =
        (state.life["rivalLockedDay"] ?: 0) > 0

    fun lockReady(state: GameState): Boolean =
        revealed(state) && !locked(state) && contestsPlayed(state) >= LOCK_AT

    /** The lock beat: one small played moment, by standing.
     *  Records the standing as it stands today — the response
     *  history has spoken. */
    fun playLock(state: GameState): String {
        if (!lockReady(state)) return ""
        val standing = standingOf(state)
        state.lifeText["rivalStanding"] = standing
        state.life["rivalLockedDay"] = state.absDay
        val rival = rivalOf(state)
        plantTopic(rival)
        state.lifeText["rivalBeat"] = "🆚 Your rivalry with $rival is " +
            "officially a ${standingLabel(standing)} — the town has " +
            "stopped pretending it isn't a fixture."
        state.life["rivalBeatDay"] = state.absDay
        val beat = when (standing) {
            NEMESIS -> "No handshake. A shove. Then — because it's " +
                "still South Park — you both go get tacos anyway."
            FRIENDLY -> "You both crack up at the exact same moment, " +
                "which ruins the menace completely. Neither of you " +
                "would have it any other way."
            else -> "The handshake is firm, mutual, and held exactly " +
                "one second too long. It's official."
        }
        return "$rival looks at the scoreboard — the real one, the " +
            "town's — and nods once.\n\n$beat\n\n" +
            "🆚 THE RIVALRY IS SET: ${standingLabel(standing)}. " +
            standingLine(state)
    }

    // ---------------- the reveal ----------------

    data class RevealFrame(val setup: String, val grab: String,
                           val even: String, val letGo: String)

    private val REVEAL_FRAMES: List<RevealFrame> = listOf(
        RevealFrame(
            "Lunch. One slice left on the tray — the good one, corner " +
                "slice, extra cheese. Your hand is moving. So is " +
                "{rival}'s. Your eyes meet over it. The whole cafeteria " +
                "goes quiet in the way cafeterias do when history is " +
                "happening.",
            "Take the slice. It's yours. It was always yours.",
            "Split it down the middle, like civilized people.",
            "\"…You take it.\" (A power move. Probably.)"),
        RevealFrame(
            "The Taco Catch cabinet at Freeman's. The high score sits " +
                "ONE point above yours — and {rival} is standing right " +
                "there with a token in hand, watching you read it. They " +
                "put those initials in this morning. They have been " +
                "waiting here for you to see them.",
            "Token in. Right now. In front of them.",
            "Race you for the cabinet — winner takes the initials.",
            "\"Enjoy it. For now.\" Turn and walk away slowly."),
        RevealFrame(
            "The Bijou, five minutes to the feature. One perfect seat " +
                "left — dead center, aisle-adjacent, nobody's grandma in " +
                "front of it. You see it. {rival} sees it. You see each " +
                "other seeing it.",
            "That's my seat. It has my name on it. Spiritually.",
            "Rock-paper-scissors for it. Like adults.",
            "\"Sit. I'll take the other one.\"")
    )

    fun revealFrame(state: GameState): RevealFrame =
        REVEAL_FRAMES[saveSeed(state) % REVEAL_FRAMES.size]

    fun revealText(state: GameState): String =
        revealFrame(state).setup.replace("{rival}", rivalOf(state))

    fun revealOptions(state: GameState): List<String> {
        val f = revealFrame(state)
        return listOf(f.grab, f.even, f.letGo)
    }

    /** The crowning. Every choice crowns them — the choices
     *  differ in what they seed: a grab is the rivalry's first
     *  escalation, a yielding its first mercy, and calling it
     *  even is just clean competition. */
    fun playReveal(state: GameState, choice: Int): String {
        val rival = rivalOf(state)
        state.lifeText["rival"] = rival
        state.life["rivalRevealedDay"] = state.absDay
        state.life["rivalNextDay"] = state.absDay + FIRST_CHALLENGE_GAP +
            Random(saveSeed(state)).nextInt(2)
        state.life["rivalPrankNext"] = state.absDay + PRANK_FIRST_DELAY +
            Random(saveSeed(state) * 3L).nextInt(6)
        when (choice) {
            0 -> state.life["rivalEsc"] = escalations(state) + 1
            2 -> state.life["rivalMercy"] = mercies(state) + 1
        }
        plantTopic(rival)
        pushTicker(state, "🆚 $rival has officially decided: you are " +
            "their rival. There's a scoreboard now. It's on the " +
            "Journal's 🆚 page.")
        state.lifeText["rivalBeat"] = "🆚 $rival has decided you are " +
            "their rival. There's a scoreboard now."
        state.life["rivalBeatDay"] = state.absDay
        val def = defOf(rival) ?: POOL[0]
        val reaction = when (choice) {
            0 -> "Something in their eyes lights up like a marquee: " +
                "CHALLENGE ACCEPTED."
            2 -> "They take it — slowly, watching you the whole time, " +
                "like generosity might be a move. It might be."
            else -> "They look almost disappointed it stayed civil. Almost."
        }
        return reaction + "\n\n“" +
            def.crownLine.replace("{you}", playerNameOf(state)) + "”\n\n" +
            "🆚 THE RIVAL: $rival. Contests will come — a few a month, " +
            "always invitations, declining always free. The score " +
            "lives on the Journal's 🆚 THE RIVAL page."
    }

    // ---------------- pranks ----------------

    /** One authored prank. [spot] is a location id, or
     *  "haunt" for the player's most-visited place. Every
     *  line is cosmetic: a prank is a dressing string and a
     *  choice, and when both are gone the save is exactly
     *  what it was — the suite proves it. */
    data class Prank(val id: String, val spot: String, val found: String,
                     val confess: String, val backVow: String,
                     val mercyLine: String, val counter: String)

    val PRANKS: List<Prank> = listOf(
        Prank("pinecones", "haunt",
            "💢 Your usual spot is FULL of pine cones. Neatly stacked. " +
                "Pyramids. This took time. This took {rival}.",
            "The pine cones were ART and also a message. The message " +
                "is: I can reach your favorite places. Your move, or " +
                "your mercy. Choose carefully, I have a chart.",
            "Oh, it's LIKE that. Okay. Okay okay okay. You chose " +
                "this timeline, remember that.",
            "…Huh. No revenge? That's either class or strategy and " +
                "I respect both against my will.",
            "💢 This morning, {rival} found the pine cones returned — " +
                "glued into a perfect trophy on their own porch. " +
                "Nobody in town knows anything. The town is wrong."),
        Prank("sign", "BusStop",
            "💢 Somebody has lettered a sign over the bench: " +
                "'RESERVED: THE NEW KID (APPLAUSE ON ARRIVAL)'. The " +
                "handwriting is {rival}'s. You would know it anywhere.",
            "The sign is just accurate signage. You're welcome. " +
                "Anyway — prank back, or are we being MATURE about " +
                "it? I have follow-up signage ready either way.",
            "Signage war. You understand this is a signage war now. " +
                "I have SO much cardboard.",
            "Letting it go. Wow. The sign stays up, by the way. " +
                "It's load-bearing now. People applaud.",
            "💢 Overnight, the bus-stop sign acquired a second line in " +
                "different handwriting: 'RESERVED: {rival} (SMALL " +
                "APPLAUSE)'. Witnesses report {rival} read it four times."),
        Prank("chalkboard", "Elementary",
            "💢 The classroom board carries a chalk scoreboard: " +
                "'{rival} 100 — NEW KID 0'. It is not from a real " +
                "contest. It's just up there. In chalk. In their " +
                "handwriting.",
            "The board needed a scoreboard and I provided. The " +
                "numbers are ASPIRATIONAL. Your move — erase my era, " +
                "or rise above it. Rising above it is also on the board.",
            "A chalk war. In THIS classroom. Victoria is going to " +
                "hear about the rematch and she's going to allow it.",
            "Erase nothing, say nothing. Classy. The board keeps " +
                "the score it has, then. I'll allow the real one to " +
                "speak eventually.",
            "💢 The board this morning: the chalk score now reads " +
                "'{rival} 100 — NEW KID 101 (CHALK)'. {rival} has " +
                "demanded a handwriting inquiry. There will be no inquiry."),
        Prank("snowtwin", "KidsPark",
            "💢 There's a snowman at the park wearing a hat in exactly " +
                "your colors, holding a sign with your name on it, " +
                "spelled wrong on purpose. This is {rival}'s work. " +
                "The craftsmanship is honestly upsetting.",
            "His name is also your name, but worse. He represents " +
                "you in all snow matters now. Prank back, or let " +
                "the snowman keep your title? He defends it poorly.",
            "You would prank a SNOWMAN'S creator. He's melting with " +
                "pride already. This means war, obviously.",
            "You let the snowman stand. He thanks you. {rival} " +
                "thanks you less, but the season thanks you most.",
            "💢 The snowtwin has a companion this morning: a second, " +
                "smaller snowman with {rival}'s name spelled wronger. " +
                "They face each other. It's a whole thing now."),
        Prank("flyers", "YourHouse",
            "💢 Your mailbox is STUFFED with flyers: 'THE NEW KID — " +
                "A RETROSPECTIVE. LOSSES ONLY.' Somebody printed these. " +
                "Somebody paid for paper. It was {rival}.",
            "Circulation: one mailbox. Impact: enormous. I have " +
                "back issues if you want the set. Or you could answer " +
                "in kind. Or — radical — let it go.",
            "A print war. I invented print wars in this town. " +
                "You'll see. Everyone will see. That's the medium.",
            "No counter-edition. Hmm. Either you're above it or " +
                "you're plotting something larger. I choose to " +
                "believe it's the second one. It's respect.",
            "💢 A rival publication appeared overnight, town-wide, " +
                "one copy per porch: '{rival} — A RETROSPECTIVE. " +
                "TIES ONLY.' The print quality is suspiciously good."),
        Prank("outline", "MainStreet",
            "💢 Somebody has drawn a chalk outline of a dramatic faint " +
                "on the Main Street pavement and labeled it, in " +
                "{rival}'s handwriting: 'THE NEW KID, LOSING'.",
            "It's a public service announcement about your " +
                "trajectory. The pose is from the climax, I studied. " +
                "Answer it, or walk around it forever. People are " +
                "already walking around it.",
            "Chalk for chalk. The pavement of this town will not " +
                "survive our rivalry and I accept that completely.",
            "You just stepped around it. In front of everyone. " +
                "That's either zen or the coldest move yet and I'm " +
                "updating my notes either way.",
            "💢 Beside the first outline this morning: a second one, " +
                "labeled '{rival}, ALSO LOSING, BUT DRAMATICALLY'. " +
                "The town has begun leaving flowers for both.")
    )

    fun prankById(id: String): Prank? = PRANKS.firstOrNull { it.id == id }

    fun prankActive(state: GameState): Boolean =
        prankById(state.lifeText["rivalPrank"] ?: "") != null

    fun prankFound(state: GameState): Boolean =
        (state.life["rivalPrankFound"] ?: 0) == 1

    fun prankLoc(state: GameState): String? {
        val prank = prankById(state.lifeText["rivalPrank"] ?: "") ?: return null
        return if (prank.spot == "haunt")
            state.lifeText["rivalPrankLoc"] ?: "KidsPark"
        else prank.spot
    }

    private fun plantPrank(state: GameState) {
        val seed = saveSeed(state) * 29L + state.absDay * 5L
        val prank = PRANKS[Random(seed).nextInt(PRANKS.size)]
        state.lifeText["rivalPrank"] = prank.id
        state.life["rivalPrankDay"] = state.absDay
        state.life.remove("rivalPrankFound")
        if (prank.spot == "haunt") {
            val haunt = state.locVisits.maxByOrNull { it.value }?.key
                ?: "KidsPark"
            state.lifeText["rivalPrankLoc"] = haunt
        }
        state.life["rivalPrankNext"] = state.absDay + PRANK_GAP_MIN +
            Random(seed + 1).nextInt(PRANK_GAP_SPREAD)
    }

    private fun clearPrank(state: GameState) {
        state.lifeText.remove("rivalPrank")
        state.life.remove("rivalPrankDay")
        state.life.remove("rivalPrankFound")
        state.lifeText.remove("rivalPrankLoc")
    }

    /** The answer. PRANK BACK escalates and schedules the
     *  counter-prank's landing for tomorrow's ticker; LET IT
     *  GO is mercy, and the standing remembers both. Either
     *  way the prank itself leaves no trace behind. */
    fun answerPrank(state: GameState, back: Boolean): String {
        val prank = prankById(state.lifeText["rivalPrank"] ?: "")
            ?: return ""
        val rival = rivalOf(state)
        clearPrank(state)
        plantTopic(rival)
        return if (back) {
            state.life["rivalEsc"] = escalations(state) + 1
            state.lifeText["rivalPrankBack"] = prank.id
            state.life["rivalPrankBackDay"] = state.absDay + 1
            "“${prank.backVow}”\n\n(Somewhere across town, a counter-" +
                "prank is being prepared. It will land by morning.)"
        } else {
            state.life["rivalMercy"] = mercies(state) + 1
            "“${prank.mercyLine}”"
        }
    }

    fun prankConfessText(state: GameState): String {
        val prank = prankById(state.lifeText["rivalPrank"] ?: "")
            ?: return ""
        return "“${prank.confess}”"
    }

    // ---------------- the rollover ----------------

    /** Midnight bookkeeping, in Engine.onNewDay before the
     *  phone harvests: the pick gets recorded, the race and
     *  stale arms lapse free, the follower race accrues and
     *  settles, a counter-prank lands, a due prank plants,
     *  and a fresh challenge day announces itself once. */
    fun dailyRollover(state: GameState) {
        // the pick, written down the first time a midnight
        // sees this save (the pure rule already agrees).
        if (state.lifeText["rival"] == null)
            state.lifeText["rival"] = pickFor(state)
        // a race belongs to its day; the new day never happened
        if ((state.life["rivalRaceDay"] ?: -1) != state.absDay &&
            state.lifeText["rivalPendingRace"] == K_RACE) {
            state.lifeText.remove("rivalPendingRace")
            state.lifeText.remove("rivalRaceFrom")
            state.lifeText.remove("rivalRaceTo")
        }
        // an armed contest the town forgets: free, silent
        val pk = pendingKind(state)
        if (pk != null &&
            state.absDay - (state.life["rivalPendingDay"] ?: state.absDay) >
                PENDING_LAPSE_DAYS) {
            clearPending(state)
            state.life["rivalNextDay"] = state.absDay + 2
        }
        // the follower race: they post in the night, then the
        // day comparison judges — your crossing day (recorded
        // at the photo funnel) against their tally as of this
        // morning. Earlier day wins; same day, the overshoot.
        if (pendingKind(state) == K_FOLLOWERS) {
            val def = defOf(rivalOf(state))
            if (def != null) {
                state.life["rivalFollowers"] =
                    (state.life["rivalFollowers"] ?: 0) + def.postPace
                val target = state.life["rivalTarget"] ?: Int.MAX_VALUE
                val youDay = state.life["rivalRaceYouDay"] ?: 0
                val themIn = (state.life["rivalFollowers"] ?: 0) >= target
                if (youDay > 0 || themIn) {
                    // youDay is the day your crossing was recorded
                    // (an earlier day than this morning's accrual,
                    // or yesterday — the same round as their post).
                    // Earlier day wins; same round, the overshoot.
                    val winner = when {
                        youDay > 0 && themIn && youDay >= state.absDay - 1 ->
                            when {
                                state.followers > (state.life["rivalFollowers"] ?: 0) -> "you"
                                state.followers < (state.life["rivalFollowers"] ?: 0) -> "them"
                                else -> "draw"
                            }
                        youDay > 0 -> "you"
                        else -> "them"
                    }
                    state.life.remove("rivalRaceYouDay")
                    recordResult(state, K_FOLLOWERS, winner,
                        "first to $target followers — you " +
                            "${state.followers}, them " +
                            "${state.life["rivalFollowers"] ?: 0}")
                }
            }
        }
        // a counter-prank lands
        if (state.life["rivalPrankBackDay"] == state.absDay) {
            val prank = prankById(state.lifeText["rivalPrankBack"] ?: "")
            if (prank != null) pushTicker(state,
                prank.counter.replace("{rival}", rivalOf(state)))
            state.lifeText.remove("rivalPrankBack")
            state.life.remove("rivalPrankBackDay")
        }
        // an unanswered prank evaporates, unrecorded
        if (prankActive(state) &&
            state.absDay - (state.life["rivalPrankDay"] ?: state.absDay) >
                PRANK_EXPIRE_DAYS) clearPrank(state)
        // a due prank plants — on a day the town's bigger
        // moments don't own, and only by its seeded chance
        if (revealed(state) && !prankActive(state) &&
            state.absDay >= (state.life["rivalPrankNext"] ?: Int.MAX_VALUE) &&
            !Emergencies.momentOwned(state)) {
            val roll = Random(saveSeed(state) * 11L + state.absDay)
                .nextDouble()
            if (roll < PRANK_CHANCE) plantPrank(state)
            else state.life["rivalPrankNext"] = state.absDay + 3
        }
        // a fresh challenge day announces itself, once
        if (challengeLive(state) && state.absDay == nextChallengeDay(state))
            pushTicker(state, "🆚 ${rivalOf(state)} is looking pleased " +
                "with themselves. There's a challenge in it, if you " +
                "want it — declining's free, as always.")
    }

    // ---------------- surfaces ----------------

    /** The prank's dressing, found by Looking at its spot:
     *  the find is the discovery; the answer is a talk. */
    fun lookText(state: GameState, locId: String): String? {
        if (!prankActive(state) || prankFound(state)) return null
        if (locId != prankLoc(state)) return null
        val prank = prankById(state.lifeText["rivalPrank"] ?: "") ?: return null
        state.life["rivalPrankFound"] = 1
        return prank.found.replace("{rival}", rivalOf(state)) +
            "\n(Find ${rivalOf(state)} — they'll want to confess in " +
            "person. You can answer in kind, or let it go.)"
    }

    private val AMBIENT_WIN = listOf(
        "Still up from {kind}, by the way. The replay is free. I give it nightly.",
        "The scoreboard says what it says, {you}. I checked again this morning. Still says it.",
        "Mother asked why I'm smiling. I said '{kind}'. She understood completely.")
    private val AMBIENT_LOSE = listOf(
        "We don't talk about {kind}. We DO talk about the rematch. Constantly. Starting now.",
        "I've reviewed {kind} from every angle and the angle is: rematch.",
        "Enjoy it while it lasts, {you}. Scores are just grudges with numbers.")
    private val AMBIENT_DRAW = listOf(
        "A draw. In {kind}. I have told three people it was basically my win. It was not. It was a draw.",
        "{kind}: unsettled. The rivalry does not accept unfinished business, {you}.")

    /** The owed-lines rail: the rival's latest-result line
     *  while it's fresh (once a day), a challenge-day nudge,
     *  and the town's gossip — one carrier a day, stating the
     *  recorded facts through the score line's own words. */
    fun ambientLine(state: GameState, npcName: String): Line? {
        if (!revealed(state)) return null
        val rival = rivalOf(state)
        val lastDay = state.life["rivalLastDay"] ?: 0
        val fresh = lastDay > 0 && state.absDay - lastDay <= FRESH_DAYS
        if (npcName == rival) {
            if ((state.life["rivalLineDay"] ?: -1) == state.absDay) return null
            val line = when {
                fresh -> {
                    val winner = state.lifeText["rivalLastWinner"] ?: ""
                    val kind = kindLabel(state.lifeText["rivalLastKind"] ?: "")
                    val bank = when (winner) {
                        "them" -> AMBIENT_WIN
                        "you" -> AMBIENT_LOSE
                        else -> AMBIENT_DRAW
                    }
                    bank[Random(saveSeed(state) + lastDay).nextInt(bank.size)]
                        .replace("{kind}", kind)
                        .replace("{you}", playerNameOf(state))
                }
                challengeLive(state) && pendingKind(state) == null ->
                    "I've got a contest with your name on it today, " +
                        "${playerNameOf(state)}. Come and take it — or " +
                        "don't. Declining's free. Losing's the expensive one."
                locked(state) -> "The standing's set: " +
                    "${standingLabel(standingOf(state))}. The contests " +
                    "aren't, though. They never are."
                else -> null
            } ?: return null
            state.life["rivalLineDay"] = state.absDay
            return Line(npcName, line)
        }
        // the town's gossip: one carrier a day, facts only
        if (!fresh) return null
        if ((state.life["rivalGossipDay"] ?: -1) == state.absDay) return null
        val sentence = lastResultSentence(state) ?: return null
        state.life["rivalGossipDay"] = state.absDay
        return Line(npcName, "Did you hear? " +
            sentence.replaceFirstChar { it.uppercase() } +
            ". ${scoreLine(state)}")
    }

    /** The morning report's line: yesterday's beat (a result,
     *  the crowning, the lock), or the reveal's tease on the
     *  morning it falls due. */
    fun morningNote(state: GameState): String? {
        if ((state.life["rivalBeatDay"] ?: -999) == state.absDay - 1)
            return state.lifeText["rivalBeat"]
        if (revealDue(state))
            return "🆚 Somebody in town has been watching you size the " +
                "place up. Today feels like a day they make it official."
        return null
    }

    // ---------------- the phone ----------------

    data class RivalTextDue(val rival: String, val winner: String,
                            val kindLabel: String)

    /** A text is due the morning after a result lands (or the
     *  same harvest, for a rollover-settled race) — once per
     *  result, stamped when the phone actually sends it. */
    fun textDue(state: GameState): RivalTextDue? {
        val lastDay = state.life["rivalLastDay"] ?: 0
        if (lastDay == 0) return null
        if ((state.life["rivalTextDay"] ?: -1) == lastDay) return null
        if (state.absDay - lastDay !in 0..2) return null
        return RivalTextDue(rivalOf(state),
            state.lifeText["rivalLastWinner"] ?: "",
            kindLabel(state.lifeText["rivalLastKind"] ?: ""))
    }

    fun markTexted(state: GameState) {
        state.life["rivalTextDay"] = state.life["rivalLastDay"] ?: 0
    }

    // ---------------- the Journal page ----------------

    /** The 🆚 THE RIVAL page's lines — pure assembly; the
     *  Journal renders. Line 0 is the header. */
    fun pageLines(state: GameState): List<String> {
        val lines = mutableListOf<String>()
        if (!revealed(state)) {
            lines += "🆚 THE RIVAL"
            lines += "Nobody has claimed you yet. Somebody will — a " +
                "town this size always produces one. They'll want a " +
                "small glory at the exact moment you reach for it, " +
                "and that will be that."
            lines += "When it happens, the score lives here."
            return lines
        }
        val rival = rivalOf(state)
        val def = defOf(rival)
        lines += "🆚 THE RIVAL — $rival"
        if (def != null) lines += def.why
        lines += "⚖️ Standing: ${standingLabel(standingOf(state))}" +
            (if (locked(state)) " (locked — the town has stopped " +
                "pretending it isn't a fixture)" else " (still being " +
                "written by how you answer)") + ". ${standingLine(state)}"
        lines += "🏆 ${scoreLine(state)}"
        when {
            raceLive(state) -> lines += "🏃 A race is live: get to " +
                "${LOCATIONS[state.lifeText["rivalRaceTo"]]?.name ?: "the mark"} — " +
                "your clock is running."
            pendingKind(state) == K_FOLLOWERS -> lines += "📸 A follower " +
                "race is armed: first to ${state.life["rivalTarget"] ?: 0} — " +
                "you ${state.followers}, them ${state.life["rivalFollowers"] ?: 0}."
            pendingKind(state) == K_FISHING -> lines += "🎣 A fishing " +
                "duel is armed: your next catch at the pond is the duel."
            pendingKind(state) == K_SHOWDOWN -> lines += "❄️ A showdown " +
                "is owed: talk to $rival and step to the fort."
            challengeLive(state) -> lines += "🆚 $rival has a challenge " +
                "for you today (${kindLabel(offerKindFor(state))}). " +
                "Declining costs nothing."
        }
        if (prankActive(state) && prankFound(state))
            lines += "💢 There's a prank by $rival waiting on your " +
                "answer — prank back, or let it go."
        // v0.9.0b: the crew's witnessing, quoted from
        // Crew's own counters — the scoreboard had an
        // audience, and the page says so
        if (Crew.contestsWitnessed(state) > 0)
            lines += "👥 Your crew has stood and watched " +
                "${Crew.contestsWitnessed(state)} of them. " +
                "$rival performs differently with an audience " +
                "and denies it completely."
        val log = contestLog(state)
        if (log.isEmpty()) {
            lines += "No contests yet. The scoreboard is clean, " +
                "which $rival describes as 'unspoiled evidence'."
        } else {
            lines += "— the board —"
            for (c in log.asReversed()) {
                lines += "${kindEmoji(c.kind)} Day ${c.day} · " +
                    "${kindLabel(c.kind)} — " +
                    when (c.winner) {
                        "you" -> "YOU took it"
                        "them" -> "$rival took it"
                        else -> "drawn"
                    } + " (${c.detail})"
            }
        }
        // v0.9.0e: the crews — their roster, yours, the
        // season standings and its history (RivalCrew owns
        // the facts; this page just makes room)
        lines += RivalCrew.pageSection(state)
        return lines
    }
}

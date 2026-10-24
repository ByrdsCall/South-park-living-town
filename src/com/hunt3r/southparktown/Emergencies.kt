package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Emergencies — v0.8.0c (Queue #8 item 3): errand
// emergencies, the favor that can't wait. The errand
// system's third expansion (board/direct/shop asks in
// v0.4.0b, favor chains in v0.6.0b) — and the smallest of
// the three in machinery, because everything it needs
// already exists: the errand verbs judge it, the Walk
// prices it, the clock runs it. What is new is the CLOCK,
// the urgency, and the outcomes.
//
// The shape:
//   FIND YOU — an emergency is never posted. The day's
//     scenario (when the day has one: EMERG_CHANCE of
//     eligible days, a pure function of the day seed) waits
//     on its asker, and meeting them — or walking into
//     their space, which earns a shout — offers it in the
//     moment, on the Talk gate's EMERG_OFFER rail, above a
//     fresh ask and below the set pieces. One a day, never
//     in quiet hours, and never while a bigger moment owns
//     the town (momentOwned: a case climax, a finale week,
//     a holiday beat, a live screening, the spelling bee,
//     a capstone ceremony — the established deferrals).
//   THE CLOCK IS HONEST — accepting stamps a deadline on
//     the errand: the TRUE walk time of its route (the
//     Walk graph's own minutes, chained stop to stop from
//     where you stand) plus a working allowance
//     (WORK_MIN_PER_STOP a stop, SLACK_MIN on top). The
//     generator may only offer what that arithmetic
//     finishes before quiet hours; once stamped, nothing
//     extends it — walking costs Walk minutes, talk costs
//     talk minutes, and when timeMin reaches the deadline
//     Engine.advanceTime settles it, mid-whatever.
//   OUTCOMES — success is judged by the ordinary verb
//     funnels and lands inside Errands.complete, which
//     pays the rescue its bonus (settleRescue): rescue
//     rates, a "rescue" thread in the asker's memory, the
//     asker's own words as the thank, and the rescue feats
//     (RESCUE_FEATS, counted in the life map like chains).
//     Failure is the clock alone: the errand leaves the
//     plate, the town copes (the coping beat waits in
//     lifeText and plays once, as an owed line, at your
//     next meeting), the morning report says so in one
//     line — and nothing else. No friendship moves, no
//     coins move, no crater: exactly an abandoned errand's
//     disappointment, once, and the day is spent.
//
// An emergency takes its OWN plate slot: acceptEmergency
// bypasses the plate cap rather than evicting anything,
// and the Journal shows it on its own ⏰ line, apart from
// the plate's count. State rides the errand record itself
// (two additive fields, written only on EMERG errands —
// ordinary errands serialize byte-for-byte as they always
// have) plus the usual life/lifeText keys.
// ============================================================

/** One authored emergency: who, which verb, what words.
 *  [targetName] is a DELIVER's recipient; [targetLoc] a
 *  FIND's place or a FIX bench's place (null FIX = the
 *  night's flickering lamp, wherever it misbehaves);
 *  [pickupLoc] is a FETCH's first stop, where the item
 *  honestly comes from. */
data class EmergScenario(
    val id: String,
    val asker: String,
    val type: String,            // FETCH | DELIVER | FIND | FIX
    val shortTitle: String,      // "the slip run" — the morning beat's name
    val offerText: String,
    val shout: String,
    val successLine: String,
    val copingLine: String,
    val itemId: String? = null,
    val targetName: String? = null,
    val targetLoc: String? = null,
    val fixKind: String? = null,
    val pickupLoc: String? = null
)

/** The offer as the Talk gate carries it: the scenario,
 *  the route it priced, and the errand as it would sit on
 *  the plate — deadline already stamped. */
data class EmergOffer(
    val scenario: EmergScenario,
    val errand: Errand,
    val stops: List<String>,
    val routeMin: Int
) {
    val deadlineMin: Int get() = errand.emergDeadline
}

object Emergencies {

    // ---------------- the dials (all suite-pinned) ----------------

    /** The share of days that carry an emergency at all —
     *  in the 30–50% band on purpose: often enough that the
     *  town feels alive with small disasters, rare enough
     *  that a shout still means drop everything. */
    const val EMERG_CHANCE = 0.40

    /** Working minutes priced into every stop on the
     *  route — finding the pew, catching the stand kid's
     *  eye, doing the actual fix. */
    const val WORK_MIN_PER_STOP = 10

    /** The flat grace on top of walk + work: the deadline
     *  is firm, but it is not a trap. */
    const val SLACK_MIN = 20

    /** Rescue rates: an emergency pays its verb's ordinary
     *  errand rates plus this much again — a rescue should
     *  feel like a rescue. */
    const val RESCUE_FRIENDSHIP_BONUS = 3
    const val RESCUE_COIN_BONUS = 2

    /** The memory topic a landed rescue plants in the
     *  asker's conversation memory — Threads' follow-up
     *  bank carries a bank for it. */
    const val RESCUE_TOPIC = "rescue"

    // ---------------- the scenarios ----------------

    val SCENARIOS: List<EmergScenario> = listOf(
        EmergScenario(
            id = "slip_run", asker = "Sheila", type = "DELIVER",
            shortTitle = "the slip run",
            targetName = "Victoria",
            offerText = "It's Kyle's field-trip permission slip — " +
                "signed, sealed, and sitting in my hand instead of in " +
                "Principal Victoria's. The bus roster closes TODAY. " +
                "If that slip isn't in her hands at the Elementary " +
                "before the clock runs out, my son watches the bus " +
                "leave. Please.",
            shout = "The slip! Kyle's permission slip — the roster " +
                "closes today, I need a runner!",
            successLine = "Victoria has the slip, Kyle has a bus " +
                "seat, and I have a new kid I trust with paper. " +
                "That's the whole list. Thank you.",
            copingLine = "So. Kyle attended the field trip anyway. " +
                "Somebody's signature was on that slip, in purple " +
                "crayon, and Victoria has chosen peace. We're all " +
                "choosing peace."),
        EmergScenario(
            id = "filing", asker = "Gerald", type = "DELIVER",
            shortTitle = "the filing run",
            targetName = "McDaniels",
            offerText = "This filing has to be in Mayor McDaniels' " +
                "hands at City Hall before the clerk locks the drawer " +
                "for the day, and I am due in a deposition across town " +
                "in ten minutes. Billable minutes, both ways. Carry " +
                "it like it's money, because legally, it is.",
            shout = "The filing! It has to reach City Hall before " +
                "the drawer locks — I need legs, not billable hours!",
            successLine = "Filed, stamped, and in the record. The " +
                "clerk asked who my new courier was. I said 'the " +
                "best-documented legs in South Park.' Thank you.",
            copingLine = "The filing missed the drawer by four " +
                "minutes. The clerk 'found' a second drawer. There " +
                "is always a second drawer. We're fine. It's fine. " +
                "It's billed as fine."),
        EmergScenario(
            id = "ticket_stub", asker = "Clyde", type = "FIND",
            shortTitle = "the stub hunt",
            itemId = "golden_ticket_stub", targetLoc = "BusStop",
            offerText = "My golden ticket stub — the matinee stub, " +
                "the one that proves I was FIRST in line — blew off " +
                "the bench at the bus stop and it's gone somewhere " +
                "in the grass. Doors at the Bijou close soon and I " +
                "am NOT re-queuing like a person without a stub. " +
                "It's there. I know it's there.",
            shout = "My stub! It blew away at the bus stop — the " +
                "matinee doors, they're SOON!",
            successLine = "THE STUB. Crisp edges, golden as ever. " +
                "I shall present it at the Bijou like a knight " +
                "presenting a sword. You. Are. A. Legend.",
            copingLine = "The stub turned up. In my sock. It was in " +
                "my sock the entire afternoon, including during " +
                "this conversation. The matinee was great. I don't " +
                "want to talk about the sock."),
        EmergScenario(
            id = "wallet_church", asker = "Mr. Tucker", type = "FIND",
            shortTitle = "the wallet in the pew",
            itemId = "velcro_wallet", targetLoc = "Church",
            offerText = "Family portrait at the church in a little " +
                "while, and my wallet — the good one, with the " +
                "deposit money in it — is sitting in a pew where I " +
                "left it at Sunday service. Craig cannot know. " +
                "Laura cannot know. The photographer takes cash or " +
                "dignity, and I'm short on both. The pew. Please.",
            shout = "My wallet! It's in a pew at the church and " +
                "the portrait's TODAY — Craig cannot know!",
            successLine = "Wallet recovered, deposit paid, portrait " +
                "taken. In it, I look like a man who never lost " +
                "anything in his life. That's your doing.",
            copingLine = "Laura had the wallet. It was in her purse " +
                "'for safekeeping,' which apparently started the " +
                "moment I lost it and ended never. The portrait is " +
                "lovely. I am smiling in it. On the outside."),
        EmergScenario(
            id = "bench_photo", asker = "Victoria", type = "FIX",
            shortTitle = "the front-row bench",
            targetLoc = "KidsPark", fixKind = "bench",
            offerText = "The class photograph is being taken at " +
                "Kid's Park, and the front row sits on that bench — " +
                "the bench that wobbles like it's deciding something. " +
                "If it tips mid-photo, I will have a class portrait " +
                "of nine children horizontal. Tighten it before the " +
                "photographer arrives. Please. Quickly.",
            shout = "The bench at the park! The class photo is " +
                "today and it WOBBLES — I need it steady!",
            successLine = "Nine children, vertical, smiling, on a " +
                "bench that did not move a millimeter. The " +
                "photograph is already in the yearbook file. " +
                "Well done.",
            copingLine = "The photographer had the children stand. " +
                "'More dynamic,' he said. The bench wobbles on in " +
                "the background of a perfectly dynamic photograph. " +
                "We adapt. That is education."),
        EmergScenario(
            id = "bench_bus", asker = "Marvin", type = "FIX",
            shortTitle = "Marvin's bench",
            targetLoc = "BusStop", fixKind = "bench",
            offerText = "That bus-stop bench has got a short leg " +
                "and I've got a hip that's older than the bus " +
                "company. When my bus comes, I rise, I ride — " +
                "that's the arrangement. If I go down in front of " +
                "the whole stop, they'll put it in the paper Billy " +
                "reads. Shim the leg before my bus. There's time. " +
                "Barely.",
            shout = "Billy! The bench at the bus stop's about to " +
                "take me down — shim it before my bus!",
            successLine = "Steady as a church pew. I rose from that " +
                "bench like a man half my age, Billy. The whole " +
                "stop saw it. Quietly majestic, is what it was.",
            copingLine = "The bus driver stopped six inches early " +
                "so I could use the handrail. Whole stop pretended " +
                "not to notice. Decent people. The bench and I have " +
                "reached an understanding: we both lean, we both hold."),
        EmergScenario(
            id = "lemonade_run", asker = "Nichole", type = "FETCH",
            shortTitle = "the lemonade run",
            itemId = "lemonade", pickupLoc = "KidsPark",
            offerText = "The bake-sale pitcher's cracked, we're " +
                "OUT of lemonade, and there's a line forming — an " +
                "actual line, at a bake sale, in this economy. The " +
                "stand at Kid's Park pours the real thing. Bring " +
                "me one before the line becomes a committee. Run " +
                "like the wind, please.",
            shout = "We're OUT of lemonade at the bake sale — the " +
                "stand at the park! Please, there's a LINE!",
            successLine = "Cold, sweet, exactly right. The line " +
                "applauded. An actual line at a bake sale applauded " +
                "a beverage, because of you.",
            copingLine = "We served water 'with a story' — the " +
                "story was that it's what lemonade dreams of being. " +
                "Sold out anyway. This town is kind. And thirsty."),
        EmergScenario(
            id = "lamp_telescope", asker = "Kevin", type = "FIX",
            shortTitle = "the dark-sky bonk",
            fixKind = "lamp",
            offerText = "Tonight is THE night — clear sky, no moon, " +
                "the comet window — and the streetlight that " +
                "flickers is washing out my whole sky. One bonk to " +
                "steady it before full dark and the telescope sees " +
                "everything. Miss the window and it's cloudy for a " +
                "week. Astronomers have cried over less.",
            shout = "The flickering light! It's washing out the " +
                "comet window — it needs a bonk before dark!",
            successLine = "Steady light, dark sky, comet logged. " +
                "There's a footnote in my star journal with your " +
                "name in it. That's the highest honor astronomy has.",
            copingLine = "I draped a blanket over the telescope and " +
                "logged the comet from under it, like a kid in a " +
                "fort. Science adapts. The blanket had dinosaurs " +
                "on it. Science adapts in style.")
    )

    fun scenarioById(id: String): EmergScenario? =
        SCENARIOS.firstOrNull { it.id == id }

    // ---------------- the feats ----------------

    /** The rescue feats — counted in rescues, in the life
     *  map like the chain count (no new save key). */
    val RESCUE_FEATS: List<ErrandFeat> = listOf(
        ErrandFeat("rescue_1", "🚨 First Responder",
            "Answer an emergency in time — somebody's worst " +
                "five minutes, handled.", 1, "First Responder"),
        ErrandFeat("rescue_10", "🚨 Steady Hands",
            "Answer 10 emergencies in time. The town's small " +
                "disasters ask for you by name.", 10, "Steady Hands")
    )

    fun rescuesDone(state: GameState): Int =
        state.life["rescuesDone"] ?: 0

    fun rescueFeatEarned(state: GameState, f: ErrandFeat): Boolean =
        rescuesDone(state) >= f.at

    // ---------------- the day's emergency ----------------

    /** Today's scenario, if the day has one: a pure
     *  function of the day seed (same discipline as the
     *  request board) — EMERG_CHANCE of days carry one.
     *  Null once the day is spent (state.life["emergDay"]
     *  holds the day it was taken, declined, landed or
     *  lost) — one emergency a day, never two. */
    fun todaysScenario(state: GameState): EmergScenario? {
        if (state.life["emergDay"] == state.absDay) return null
        val rnd = Random(dailySeed(state.todayKey) * 977L + 0xE436L)
        if (rnd.nextDouble() >= EMERG_CHANCE) return null
        return SCENARIOS[rnd.nextInt(SCENARIOS.size)]
    }

    /** The live emergency on the plate, if one is running. */
    fun liveEmergency(state: GameState): Errand? =
        state.errands.firstOrNull { it.source == Errands.EMERG && !it.done }

    /** Is a bigger moment owning the town right now? The
     *  established deferrals, composed: a case's climax
     *  choice on the table (any of the five), a season
     *  finale week, a holiday beat's day, a Bijou screening
     *  actually live, the spelling bee's Friday hours, a
     *  capstone ceremony in its window. While any holds, no
     *  emergency fires — small disasters wait their turn
     *  behind big ones. */
    fun momentOwned(state: GameState): Boolean {
        if (Walk.caseCritical(state)) return true
        if (Finales.windowAt(state) != null) return true
        if (HolidayBeats.holidayOf(state) != null) return true
        if (Capstones.screeningLive(state)) return true
        if (School.isBeeDay(state.absDay) &&
            state.timeMin in School.BEE_FROM until School.BEE_TO) return true
        if (state.timeMin in Capstones.CEREMONY_FROM until
            Capstones.CEREMONY_TO &&
            Capstones.CHAINS.any {
                Capstones.activeBeat(state, it.factionId)?.beat == 3
            }) return true
        return false
    }

    // ---------------- the route & the clock ----------------

    /** The stops a scenario's route must visit, in order,
     *  read off the world as it stands: a delivery goes to
     *  its recipient (where they are now), a find/fix to its
     *  place (the lamp to tonight's flicker), a fetch to
     *  its source and then BACK to its asker. */
    fun stopsFor(state: GameState, sc: EmergScenario): List<String>? =
        when (sc.type) {
            "DELIVER" -> listOfNotNull(
                state.agents[sc.targetName]?.location)
                .takeIf { it.isNotEmpty() }
            "FIND" -> listOfNotNull(sc.targetLoc)
            "FIX" -> listOf(sc.targetLoc ?: Errands.flickerLoc(state.absDay))
            "FETCH" -> {
                val back = state.agents[sc.asker]?.location
                if (sc.pickupLoc == null || back == null) null
                else listOf(sc.pickupLoc, back)
            }
            else -> null
        }

    /** The route's true minutes: the Walk graph's own
     *  pricing, chained from [from] through every stop. */
    fun routeMinutes(state: GameState, from: String,
                     stops: List<String>): Int {
        var total = 0
        var at = from
        for (stop in stops) {
            total += Walk.routeMinutes(state, at, stop)
            at = stop
        }
        return total
    }

    /** The working allowance a route of [stops] stops is
     *  priced with, on top of its walk minutes. */
    fun allowance(stops: Int): Int =
        WORK_MIN_PER_STOP * stops + SLACK_MIN

    /** The deadline a route offered from [from] right now
     *  would carry: now + true walk + allowance. This is
     *  the whole formula — the suite pins it against the
     *  Walk matrix for every (from, stop) pair in town. */
    fun deadlineFor(state: GameState, from: String,
                    stops: List<String>): Int =
        state.timeMin + routeMinutes(state, from, stops) +
            allowance(stops.size)

    // ---------------- the offer ----------------

    /** The emergency on offer from [npc] right now, or
     *  null: today must carry their scenario, the day must
     *  be unspent and the offer unmade, no rescue may be
     *  running, the town's bigger moments must not own the
     *  hour, it must not be quiet hours — and the route,
     *  priced from where you stand, must honestly finish
     *  before quiet hours begin. Pure: the gate asks, the
     *  callers judge. */
    fun pendingOffer(state: GameState, npc: String,
                     playerLocId: String): EmergOffer? {
        val sc = todaysScenario(state) ?: return null
        if (sc.asker != npc) return null
        if (state.life["emergOfferedDay"] == state.absDay) return null
        if (liveEmergency(state) != null) return null
        if (Barks.inQuietHours(state.timeMin)) return null
        if (momentOwned(state)) return null
        val stops = stopsFor(state, sc) ?: return null
        val deadline = deadlineFor(state, playerLocId, stops)
        if (deadline > Barks.QUIET_START) return null
        val e = Errand(
            id = "e${state.absDay}:${sc.id}", source = Errands.EMERG,
            asker = sc.asker, type = sc.type, text = sc.offerText,
            hint = "", itemId = sc.itemId, targetName = sc.targetName,
            targetLoc = sc.targetLoc, fixKind = sc.fixKind,
            dayKey = state.todayKey,
            emergDeadline = deadline, emergScenario = sc.id)
        return EmergOffer(sc, e.copy(hint = Errands.hintFor(state, e)),
            stops, routeMinutes(state, playerLocId, stops))
    }

    /** Record that today's emergency was OFFERED (taken,
     *  declined or dismissed) — it won't be offered again
     *  today, like every ask. */
    fun markOffered(state: GameState, npc: String) {
        state.life["emergOfferedDay"] = state.absDay
    }

    /** The shout: walking into the asker's space while
     *  their emergency stands unoffered earns one cry for
     *  help — the ticker carries it, once a day, and only
     *  when the offer itself would really fire. */
    fun entryShout(state: GameState): String? {
        if (liveEmergency(state) != null) return null
        val sc = todaysScenario(state) ?: return null
        if (state.life["emergShoutDay"] == state.absDay) return null
        val me = Engine().player(state)
        val asker = state.agents[sc.asker] ?: return null
        if (asker.asleep || asker.location != me.location) return null
        if (pendingOffer(state, sc.asker, me.location) == null) return null
        state.life["emergShoutDay"] = state.absDay
        val line = "🚨 ${sc.asker}: “${sc.shout}”"
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
        return line
    }

    // ---------------- accept / decline / settle ----------------

    /** Take the emergency on: it goes on the plate in its
     *  OWN slot (the plate cap is not consulted — a rescue
     *  never evicts a held favor, and nothing is ever
     *  removed to make room), and the day is spent. The
     *  deadline it carries is the one the offer priced —
     *  recomputed nowhere, extended never. */
    fun accept(state: GameState, offer: EmergOffer): Errand? {
        if (liveEmergency(state) != null) return null
        if (state.timeMin >= offer.deadlineMin) return null
        if (state.errands.any { it.id == offer.errand.id }) return null
        val placed = offer.errand.copy(acceptedDay = state.absDay)
        state.errands.add(placed)
        state.life["emergDay"] = state.absDay
        state.events.add("🚨 Emergency taken on — " +
            "${Errands.objective(placed)} " +
            "(by ${minutesToTimeStr(placed.emergDeadline)})")
        while (state.events.size > 40) state.events.removeAt(0)
        return placed
    }

    /** Turn it down: the day is spent, and that is the
     *  whole cost — no disappointment, no mark. The asker
     *  finds another way, offstage. */
    fun decline(state: GameState) {
        state.life["emergDay"] = state.absDay
    }

    /** A landed rescue's own settlement — called from
     *  inside Errands.complete, which has already paid the
     *  verb's ordinary rates: this pays the rescue bonus,
     *  counts the rescue, plants the thread, replaces the
     *  generic thank with the asker's own words, writes
     *  the morning beat, and calls the feats. Returns the
     *  bonus (friendship, coins) so the reward the UI
     *  shows is the whole truth. */
    fun settleRescue(state: GameState, e: Errand): Pair<Int, Int> {
        val sc = scenarioById(e.emergScenario)
        if (e.asker in state.friendship)
            Engine().relAdjust(state, e.asker, RESCUE_FRIENDSHIP_BONUS)
        else {
            val mem = NpcMemoryStore.getOrCreate(e.asker)
            mem.points = (mem.points + RESCUE_FRIENDSHIP_BONUS)
                .coerceIn(-50, 100)
        }
        Minigames.awardCoins(state, RESCUE_COIN_BONUS)
        val before = rescuesDone(state)
        state.life["rescuesDone"] = before + 1
        // the thread: the asker will follow up on it
        val mem = NpcMemoryStore.getOrCreate(e.asker)
        mem.topics.remove(RESCUE_TOPIC)
        mem.topics.add(RESCUE_TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
        // the asker's own words ride the owed-lines rail
        // where every errand thank rides
        if (sc != null)
            state.lifeText["errandThanks:${e.asker}"] = sc.successLine
        state.life["emergBeatDay"] = state.absDay
        state.lifeText["emergBeat"] =
            "you answered ${e.asker}'s emergency in time — ${sc?.shortTitle ?: "the rescue"}"
        state.events.add("🚨 RESCUE — ${Errands.objective(e)} landed " +
            "(+$RESCUE_FRIENDSHIP_BONUS 💛 ${e.asker}, " +
            "+$RESCUE_COIN_BONUS 🪙 rescue bonus)")
        while (state.events.size > 40) state.events.removeAt(0)
        for (f in RESCUE_FEATS.filter {
            before < it.at && rescuesDone(state) >= it.at }) {
            state.events.add("${f.name} — rescue feat complete!")
            Phone.noteFeat(state, f.name)
            while (state.events.size > 40) state.events.removeAt(0)
            townLogLine(state, "🚨 ${f.title} — " +
                "${rescuesDone(state)} emergencies answered in South Park")
        }
        return RESCUE_FRIENDSHIP_BONUS to RESCUE_COIN_BONUS
    }

    /** The records of an emergency that got away — the
     *  clock ran out, or the player put it down. Shared by
     *  checkExpiry and Errands.abandon (which removes the
     *  record and returns the sigh itself): the recent
     *  note, the coping beat waiting as an owed line, the
     *  morning beat, and NOTHING else — no friendship, no
     *  coins, no counts. The disappointment is the asker's
     *  line, once, like an abandoned errand's. */
    fun settleExpired(state: GameState, e: Errand, dropped: Boolean) {
        val sc = scenarioById(e.emergScenario)
        state.errandsRecent.add(ErrandDoneNote(e.id, state.todayKey,
            "${Errands.objective(e)} — " +
                if (dropped) "you put it down; the town coped"
                else "the moment passed; the town coped"))
        while (state.errandsRecent.size > Errands.STATE_RECENT_CAP)
            state.errandsRecent.removeAt(0)
        if (sc != null)
            state.lifeText["emergCoping:${e.asker}"] = sc.copingLine
        state.life["emergDay"] = e.acceptedDay
        state.life["emergBeatDay"] = state.absDay
        state.lifeText["emergBeat"] =
            "${e.asker}'s emergency — ${sc?.shortTitle ?: "the call"} — " +
                "came and went; the town coped"
        state.events.add("⏰ ${e.asker}'s emergency — " +
            if (dropped) "you put it down. The town copes."
            else "the moment passed. The town coped.")
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** The clock, checked on every tick (Engine.advanceTime
     *  calls this): a live emergency whose deadline has
     *  arrived — or whose day has somehow rolled past it —
     *  settles as expired, exactly once, wherever the
     *  player is standing. Returns the settled errand. */
    fun checkExpiry(state: GameState): Errand? {
        val e = liveEmergency(state) ?: return null
        if (e.acceptedDay >= state.absDay &&
            state.timeMin < e.emergDeadline) return null
        state.errands.remove(e)
        settleExpired(state, e, dropped = false)
        return e
    }
}

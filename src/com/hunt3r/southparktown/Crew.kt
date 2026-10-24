package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Crew — v0.9.0b: YOUR CREW (Queue #9 item 2).
//
// The new kid doesn't have to roam alone. Up to four cast
// members can be asked into THE CREW, and a crew, in this
// town, is a standing arrangement: wherever you go, they
// come — on foot, at walking speed, when their own lives
// allow it.
//
// THE ASK. Inviting lives where talking starts: the
// tap-a-person menu (Talk / Gift / Trade / …) gains "Ask
// to join your crew" / "Ask to leave the crew" — an
// in-talk choice, not a TalkGate rail, because an invite
// is the player's initiative, never a beat that pre-empts
// a conversation. The gate is the Friend tier (friendship
// ≥ 30, Relationships.FRIEND): below it the ask gets an
// honest in-character not-yet, by tier. Joining, leaving
// and swapping are always free; somebody who leaves can't
// be re-asked the same day, and the menu says so. ANYONE
// can be asked — including the rival (v0.8.0e): a frenemy
// in the crew is a feature. Their contests still fire,
// their scoreboard still runs, and their crew lines know
// exactly what they are.
//
// RENDEZVOUS, HONESTLY TIMED. Crew members live their own
// agendas. When you arrive somewhere, every member who is
// FREE sets out from wherever they actually are, and the
// trip takes the Walk graph's own minutes —
// Walk.routeMinutes(from, to), the same pricing the
// emergencies (v0.8.0c) swear by. While they travel they
// are simply not with you yet (they wait at their last
// spot, coat on — the pixel-walk can't meter a journey,
// so the timed machine here does, and the arrival lands
// on the exact minute). When you move again, anyone
// standing with you re-departs from the spot you shared.
// When you go home, or sleep, the crew disperses to their
// own lives until the next outing. Membership itself is
// permanent until changed — dispersal is nightly, not a
// breakup.
//
// OCCUPIED IS OCCUPIED, SAID OUT LOUD. A member is busy,
// and stays where their life is, when: school is in
// session and their class is at the Elementary (kids AND
// the day's teacher); a system's gathering claim holds
// them (a family dinner, a job's counter, a Bijou
// audience, a capstone ceremony, a finale's ensemble);
// their boss's shift is live in a job YOU hold; a live
// climax has them as a causer; a live emergency has their
// name on it; or they're asleep. A busy member sends
// their regrets down the established channel — an owed
// line on the TalkGate rail, played once, the next time
// you talk — and rejoins when free (a fresh call goes out
// when you next arrive somewhere, or when they free up
// while you're still out). While the town's bigger
// moments own the hour (Emergencies.momentOwned: a case
// climax, a finale, a holiday, a screening, the bee, a
// ceremony), no NEW rendezvous starts — the crew waits
// like everyone else. Set pieces also pull a standing
// member away mid-outing: the pull is detected, the
// regret is owed, never silent.
//
// WHAT A CREW DOES. Standing members render inside your
// Crowds cluster — the layout reads the crew as a standing
// cluster (Crowds.Chem.crew), so the four of you stand
// together in a crowd the way the pair scripts gather
// their pairs. They talk crew: context lines in Talk
// (about the place, about each other) drawn from real
// state only — a line never names a place you aren't in
// or a member who isn't there. Shared things count, at
// the town's own rates and no richer: a completed
// rendezvous pays +1 friendship, once per member per day
// (the school-lesson rate); a photo with crew in frame
// feeds the frame's existing group chemistry — no new
// judging — and tallies as a photo together; showdowns
// and rival contests settled while crew stand with you
// tally as witnessed, and the RIVAL page says so.
//
// THE RECORD. A Journal 👥 YOUR CREW page: who's in,
// since when, and the honest tallies — outings (completed
// rendezvous), photos together, distinct places visited
// together, contests witnessed. The town notices: arrivals
// plant the "your crew" topic on present non-members
// (Threads carries the follow-ups), a full crew rolling
// into a public place draws a spotted line on the ticker
// from somebody watching, and if three or more of your
// crew share a faction, that faction's people have a
// line about it (informational only — no standing moves).
//
// INTEGRITY. Crew membership changes nobody's pair
// scripts, story arcs, dinners, jobs or case roles — the
// crew hook sits LAST in the agenda-override stack, below
// every gathering claim, so any set piece takes the
// person first and the crew reads the result. A member's
// own errands, mission beats and capstone beats proceed
// exactly as authored — they're busy, see above.
//
// STATE: the life/lifeText maps only — no new save keys,
// no new GameState fields. A pre-0.9.0b save loads with no
// crew and simply starts asking.
// ============================================================

object Crew {

    // ---------------- the dials (tuning ledger) ----------------
    const val MAX_CREW = 4              // a crew, not a parade
    const val INVITE_MIN_POINTS = 30    // the Friend tier floor (Relationships.FRIEND)
    const val TOPIC = "your crew"

    // ---------------- state keys ----------------
    private const val K_MEMBERS = "crew:members"        // lifeText: csv, join order
    private fun kSince(n: String) = "crew:since:$n"     // life: absDay joined
    private fun kMode(n: String) = "crew:mode:$n"       // lifeText: "" | "go" | "with"
    private fun kArr(n: String) = "crew:arr:$n"         // life: arrival absMin
    private fun kFrom(n: String) = "crew:from:$n"       // lifeText: departure locId
    private fun kWith(n: String) = "crew:with:$n"       // lifeText: locId stood at
    private fun kCalled(n: String) = "crew:called:$n"   // lifeText: "loc#absDay"
    private fun kLeft(n: String) = "crew:left:$n"       // life: absDay they left
    private fun kRegret(n: String) = "crewRegret:$n"    // lifeText: reason key (owed line)
    private fun kOut(n: String) = "crew:out:$n"         // life: outings (arrivals)
    private fun kPic(n: String) = "crew:pic:$n"         // life: photos together
    private fun kWit(n: String) = "crew:wit:$n"         // life: contests witnessed
    private fun kPlaces(n: String) = "crew:places:$n"   // lifeText: csv of locIds
    private fun kGain(n: String) = "crew:gain:$n"       // life: absDay of last +1
    private fun kLine(n: String) = "crew:line:$n"       // life: last talk-line slot
    private const val K_PLOC = "crew:ploc"             // lifeText: last player loc seen
    private const val K_WIT_TOTAL = "crew:witTotal"     // life: crew-witnessed contests

    /** The player agent, or null on a pre-creation /
     *  decoded-empty state — the day-0 town ticks through
     *  Engine.advanceTime with no new kid yet, and every
     *  hook reachable from there must shrug, not throw. */
    private fun playerOrNull(state: GameState) =
        state.agents.values.firstOrNull { it.isPlayer }

    private fun nowAbsMin(state: GameState): Int =
        state.absDay * 1440 + state.timeMin

    private fun ticker(state: GameState, text: String) {
        state.events.add(text)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    // ---------------- membership ----------------

    fun members(state: GameState): List<String> =
        (state.lifeText[K_MEMBERS] ?: "")
            .split(",").map { it.trim() }
            .filter { it in KID_ORDER }

    fun isMember(state: GameState, name: String): Boolean =
        name in members(state)

    fun memberSince(state: GameState, name: String): Int =
        state.life[kSince(name)] ?: state.absDay

    fun modeOf(state: GameState, name: String): String =
        state.lifeText[kMode(name)] ?: ""

    private fun setMode(state: GameState, name: String, mode: String) {
        if (mode.isEmpty()) state.lifeText.remove(kMode(name))
        else state.lifeText[kMode(name)] = mode
    }

    /** Why [name] can't be asked right now, or null when
     *  the ask is live. The menu and the suite read the
     *  same words. */
    fun inviteBlock(state: GameState, name: String): String? = when {
        name !in KID_ORDER -> "unknown"
        isMember(state, name) -> "member"
        members(state).size >= MAX_CREW -> "full"
        (state.life[kLeft(name)] ?: -1) == state.absDay -> "leftToday"
        (state.friendship[name] ?: 0) < INVITE_MIN_POINTS -> "tier"
        else -> null
    }

    /** The tap-menu's crew option for [name], or null when
     *  no option belongs there (not cast / nothing to ask). */
    fun menuOption(state: GameState, name: String): String? {
        if (name !in KID_ORDER) return null
        if (isMember(state, name)) return "👋 Ask $name to leave the crew"
        return when (inviteBlock(state, name)) {
            null -> "🤝 Ask $name to join your crew"
            "full" -> "🤝 Your crew is full ($MAX_CREW) — ask someone to leave first"
            "leftToday" -> "🤝 $name left the crew today — ask again tomorrow"
            else -> null   // below the tier: no option; the ask would only sting
        }
    }

    /** Ask them in. Call only when inviteBlock is null;
     *  returns their acceptance, in their voice's register. */
    fun invite(state: GameState, name: String): String {
        val cur = members(state).toMutableList()
        if (name !in cur) cur.add(name)
        state.lifeText[K_MEMBERS] = cur.joinToString(",")
        state.life[kSince(name)] = state.absDay
        state.life.remove(kLeft(name))
        setMode(state, name, "")
        state.lifeText.remove(kCalled(name))
        plantTopic(name)
        return acceptLine(state, name)
    }

    /** Ask them out. Free, final for today, and the
     *  tallies stay on the page's memory of them not at
     *  all — the page lists the crew you HAVE. Their
     *  counters are cleared so a later rejoin starts its
     *  own honest record. */
    fun leave(state: GameState, name: String): String {
        val cur = members(state).toMutableList()
        cur.remove(name)
        if (cur.isEmpty()) state.lifeText.remove(K_MEMBERS)
        else state.lifeText[K_MEMBERS] = cur.joinToString(",")
        state.life[kLeft(name)] = state.absDay
        clearStanding(state, name)
        state.life.remove(kSince(name))
        state.life.remove(kOut(name))
        state.life.remove(kPic(name))
        state.life.remove(kWit(name))
        state.lifeText.remove(kPlaces(name))
        return leaveLine(state, name)
    }

    private fun clearStanding(state: GameState, name: String) {
        setMode(state, name, "")
        state.life.remove(kArr(name))
        state.lifeText.remove(kFrom(name))
        state.lifeText.remove(kWith(name))
        state.lifeText.remove(kCalled(name))
    }

    // ---------------- the lines ----------------

    private val ACCEPT_LINES = listOf(
        "Your crew? …Yeah. Yeah, okay. Somebody has to keep you alive out there.",
        "I was literally just thinking my day needed more walking. I'm in.",
        "Fine. But I get veto power on exactly one snack decision per outing.",
        "A crew. With ranks and everything? No ranks? Perfect, I'm in.",
        "Took you long enough to ask. I had a whole speech ready. It's gone now. Let's go.",
        "Obviously. I was already walking behind you half the time anyway."
    )

    private val LEAVE_LINES = listOf(
        "Alright — I'll be around. Crews change, people stay. Mostly.",
        "Copy that. If the crew needs me, the crew knows where I live.",
        "Fine. I was carrying the vibe anyway. …Kidding. Mostly.",
        "Okay. No hard feelings — the schedule in my head thanks you."
    )

    /** The honest not-yet, by the tier you're actually on. */
    fun notYetLine(state: GameState, name: String): String {
        val pts = state.friendship[name] ?: 0
        return when {
            pts < -10 -> "Your CREW? After everything? Get out of here."
            pts < 10 -> "I barely know you. Crews are earned, new kid."
            else -> "We're not there yet. Walk with me a while first — " +
                "crews aren't a first-week thing."
        }
    }

    fun acceptLine(state: GameState, name: String): String {
        if (name == Rival.rivalOf(state))
            return "Join YOUR crew? …Fine. Somebody has to keep the " +
                "rivalry honest from the inside. It changes NOTHING " +
                "about the scoreboard."
        val idx = (name.hashCode() and Int.MAX_VALUE) % ACCEPT_LINES.size
        return ACCEPT_LINES[idx]
    }

    fun leaveLine(state: GameState, name: String): String {
        if (name == Rival.rivalOf(state))
            return "Out, then. For the record, the crew was the " +
                "second-best thing about this rivalry. The best " +
                "thing is me winning."
        val idx = (name.hashCode() and Int.MAX_VALUE) % LEAVE_LINES.size
        return LEAVE_LINES[idx]
    }

    /** Why a member couldn't come — the regret's words, by
     *  reason. Spoken by the member, next time you talk. */
    fun regretLine(reason: String): String = when (reason) {
        "school" -> "Sorry I couldn't come out — school had me. " +
            "All day. Tell the crew I suffered educationally."
        "work" -> "Couldn't make it — work. Actual, scheduled, " +
            "no-escape work. Next time."
        "dinner" -> "Family dinner. Non-negotiable, apparently. " +
            "I thought of the crew at the table. It helped. A little."
        "movie" -> "I was at the Bijou thing. Couldn't exactly " +
            "walk out mid-feature. Well. I could. I didn't."
        "ceremony" -> "Faction business. The ceremonial kind. " +
            "You know how it is. You don't, but say you do."
        "finale" -> "Finale week had me. The whole town was " +
            "holding its breath, including my lungs."
        "event" -> "There was event business and I was IN it. " +
            "Rain check?"
        "emergency" -> "There was an actual emergency with my " +
            "name on it. This town's timing is a prank."
        "town" -> "The town had a whole thing going on and I got " +
            "swept up in it. Rain check."
        "sleepover" -> "I was AT your house. For the " +
            "sleepover. That YOU planned."
        else -> "Couldn't make it — my actual life got in the way. " +
            "Rain check."
    }

    /** The owed regret, played exactly once — the removal
     *  IS the playing, like every owed line before it. */
    fun owedLine(state: GameState, name: String): Line? {
        val reason = state.lifeText.remove(kRegret(name)) ?: return null
        return Line(name, regretLine(reason))
    }

    private fun sendRegret(state: GameState, name: String, reason: String) {
        state.lifeText[kRegret(name)] = reason
    }

    // ---------------- occupied is occupied ----------------

    /** Why [name]'s own life holds them right now, or null
     *  when they're free to walk. The claims are the
     *  systems' own — this reads them, never re-derives
     *  them: a gathering claim IS the occupation. */
    fun busyReason(state: GameState, name: String): String? {
        val a = state.agents[name] ?: return "gone"
        if (a.asleep || isSleepHour(state.timeMin / 60)) return "asleep"
        // school: in session, no quiet-day excuse, and their
        // own agenda has them at the Elementary — kids in
        // class and the day's teacher alike, off the one
        // truth (the agenda), never a name list.
        if (School.isSchoolDay(state.absDay) &&
            School.quietReason(state) == null &&
            state.timeMin in School.CLASS_FROM until School.CLASS_TO &&
            Engine().currentAgendaBlock(a, state.timeMin)?.location ==
                "Elementary") return "school"
        // gathering claims, in the agenda stack's own order
        if (Dinners.gatheringBlock(state, name, state.timeMin) != null)
            return "dinner"
        if (Jobs.gatheringBlock(state, name, state.timeMin) != null)
            return "work"
        if (MovieNights.gatheringBlock(state, name, state.timeMin) != null)
            return "movie"
        if (Capstones.gatheringBlock(state, name, state.timeMin) != null)
            return "ceremony"
        if (Finales.gatheringBlock(state, name, state.timeMin) != null)
            return "finale"
        // a boss's shift in a job YOU hold: the counter is
        // theirs until the shift ends, rota or no rota.
        val job = Jobs.currentJob(state)
        if (job != null && Jobs.isShiftDay(state.absDay, job) &&
            Jobs.bossFor(state, job) == name) {
            val (inFrom, _, shiftEnd) = Jobs.windowFor(state, job)
            if (state.timeMin in inFrom until shiftEnd) return "work"
        }
        // a live climax owns its causers
        if (Climaxes.isLive(state) && name in Climaxes.liveCausers(state))
            return "event"
        // a live emergency with their name on it
        Emergencies.liveEmergency(state)?.let { e ->
            if (e.asker == name || e.targetName == name) return "emergency"
        }
        // v0.11.0c: a planned sleepover guest is claimed to
        // the new kid's house for the evening — the whole
        // town's occupied discipline reads it here, so no
        // system double-books a kid who's in a sleeping bag.
        if (Sleepovers.claims(state, name)) return "sleepover"
        // v0.14.0d: a crewmate at your own birthday party
        // is spoken for the same way — the party's claim
        // (and the whole town's discipline) reads it here.
        if (PlayerBirthday.claims(state, name)) return "birthday party"
        return null
    }

    // ---------------- the agenda hook ----------------

    /** The Engine's agenda lookup asks here LAST, below
     *  every gathering claim — so any set piece takes the
     *  person first, and what this returns is only ever the
     *  crew's own claim: standing with you, or waiting at
     *  the spot they set out from. While a claim holds them
     *  (busyReason), this returns null and their own life
     *  takes them — tick() narrates the exit. */
    fun gatheringBlock(state: GameState, name: String,
                       timeMin: Int): AgendaBlock? {
        if (!isMember(state, name)) return null
        return when (modeOf(state, name)) {
            "with" -> {
                if (busyReason(state, name) != null) null
                else state.lifeText[kWith(name)]?.let {
                    AgendaBlock(it, "Hanging out with the crew.")
                }
            }
            "go" -> state.lifeText[kFrom(name)]?.let {
                AgendaBlock(it, "On the way to meet the new kid.")
            }
            else -> null
        }
    }

    // ---------------- the rendezvous machine ----------------

    /** Every world tick (Engine.advanceTime, after the
     *  emergencies settle): watch the player, call the
     *  free, land the arrivals on their honest minute, and
     *  notice — out loud — when a set piece takes one of
     *  yours away. */
    fun tick(state: GameState) {
        val crew = members(state)
        val me = playerOrNull(state) ?: return
        val ploc = me.location
        val now = nowAbsMin(state)
        if (crew.isEmpty()) {
            state.lifeText[K_PLOC] = ploc
            return
        }
        val home = ploc == YOUR_HOUSE || me.asleep
        if (home) {
            var any = false
            for (n in crew) if (modeOf(state, n).isNotEmpty()) {
                clearStanding(state, n); any = true
            }
            if (any) ticker(state, "👥 The crew heads home — " +
                "everybody back to their own lives until the " +
                "next outing.")
            state.lifeText[K_PLOC] = ploc
            return
        }
        for (n in crew) {
            val a = state.agents[n] ?: continue
            if (a.asleep) { clearStanding(state, n); continue }
            val busy = busyReason(state, n)
            when (modeOf(state, n)) {
                "go" -> {
                    if (busy != null) {
                        clearStanding(state, n)
                        state.lifeText[kCalled(n)] =
                            "$ploc#${state.absDay}"
                        sendRegret(state, n, busy)
                    } else if (state.lifeText[kWith(n)] != null &&
                        state.lifeText[kWith(n)] != ploc) {
                        // you moved mid-route: the leg re-prices
                        // from where they actually are (the call
                        // below fires on the new place's stamp)
                        setMode(state, n, "")
                        state.life.remove(kArr(n))
                        state.lifeText.remove(kFrom(n))
                        state.lifeText.remove(kWith(n))
                    } else if (a.location == ploc ||
                        now >= (state.life[kArr(n)] ?: Int.MAX_VALUE)) {
                        arrive(state, n, ploc)
                    }
                }
                "with" -> {
                    val withLoc = state.lifeText[kWith(n)]
                    if (busy != null) {
                        // a set piece took them: said, never silent
                        clearStanding(state, n)
                        state.lifeText[kCalled(n)] =
                            "$ploc#${state.absDay}"
                        sendRegret(state, n, busy)
                        ticker(state, "👥 $n got pulled away — " +
                            regretShort(busy))
                    } else if (withLoc != ploc &&
                        a.location != ploc) {
                        // you moved on without them — the call
                        // below re-departs them from where they
                        // actually stand
                        setMode(state, n, "")
                        state.lifeText.remove(kWith(n))
                    }
                }
            }
            // the call: once per (place, day) per member —
            // a fresh arrival somewhere, or a new morning,
            // asks again; standing around doesn't nag.
            if (modeOf(state, n).isEmpty()) {
                val stamp = "$ploc#${state.absDay}"
                if (state.lifeText[kCalled(n)] != stamp) {
                    state.lifeText[kCalled(n)] = stamp
                    when {
                        Emergencies.momentOwned(state) ->
                            sendRegret(state, n, "town")
                        busy != null -> sendRegret(state, n, busy)
                        else -> depart(state, n, ploc, now)
                    }
                }
            }
        }
        state.lifeText[K_PLOC] = ploc
    }

    private fun regretShort(reason: String): String = when (reason) {
        "school" -> "school called."
        "work" -> "work called."
        "dinner" -> "family dinner called."
        "movie" -> "the Bijou called."
        "ceremony" -> "faction business called."
        "finale" -> "finale week called."
        "event" -> "the event called."
        "emergency" -> "an emergency with their name on it."
        "sleepover" -> "the sleepover called. (Your sleepover.)"
        else -> "their own life called."
    }

    /** Set one member walking: from where they actually
     *  are, priced by the Walk graph, arriving on the
     *  honest minute. Zero-minute trips land at once. */
    private fun depart(state: GameState, name: String,
                       to: String, now: Int) {
        val a = state.agents[name] ?: return
        val from = a.location
        val minutes = Walk.routeMinutes(state, from, to)
        if (minutes <= 0 || from == to) {
            arrive(state, name, to)
            return
        }
        setMode(state, name, "go")
        state.lifeText[kFrom(name)] = from
        state.lifeText[kWith(name)] = to   // the leg's destination
        state.life[kArr(name)] = now + minutes
    }

    /** The arrival, on its minute: the member is placed
     *  where you stand (the timed machine's one placement
     *  — the journey was metered by the Walk graph, not
     *  the pixel-walk), the outing counts, the town sees. */
    private fun arrive(state: GameState, name: String, loc: String) {
        val a = state.agents[name] ?: return
        setMode(state, name, "with")
        state.lifeText[kWith(name)] = loc
        state.life.remove(kArr(name))
        state.lifeText.remove(kFrom(name))
        a.location = loc
        a.destination = loc
        a.waypoints.clear()
        LOCATIONS[loc]?.let { a.coords = it.gx to it.gy }
        a.activity = "Hanging out with the crew."
        // the record: an outing, a place
        state.life[kOut(name)] = (state.life[kOut(name)] ?: 0) + 1
        val places = (state.lifeText[kPlaces(name)] ?: "")
            .split(",").filter { it.isNotEmpty() }.toMutableSet()
        places.add(loc)
        state.lifeText[kPlaces(name)] = places.joinToString(",")
        // the pay: +1, once per member per day — the
        // school-lesson rate, never richer
        if ((state.life[kGain(name)] ?: -1) != state.absDay) {
            state.life[kGain(name)] = state.absDay
            val cur = state.friendship[name] ?: 0
            state.friendship[name] =
                (cur + 1).coerceIn(-50, 100)
        }
        ticker(state, "👥 $name caught up with you at " +
            "${LOCATIONS[loc]?.name ?: loc}.")
        // the town notices: the topic plants on whoever's
        // watching, and a crew rolling in at strength gets
        // its spotted line
        val watchers = presentNonMembers(state, loc)
        for (w in watchers.take(2)) plantTopic(w)
        val withCount = members(state).count { modeOf(state, it) == "with" }
        if (withCount >= 2 && watchers.isNotEmpty()) {
            val seed = (state.absDay * 1440 + state.timeMin).toLong() *
                31L + name.hashCode()
            val rnd = Random(seed)
            if (rnd.nextDouble() < 0.5) {
                val speaker = watchers[rnd.nextInt(watchers.size)]
                val line = SPOTTED[rnd.nextInt(SPOTTED.size)]
                ticker(state, "👥 $speaker: $line")
            }
        }
    }

    /** Cast standing at [loc], settled and awake, who are
     *  neither the player nor crew — the witnesses. */
    private fun presentNonMembers(state: GameState, loc: String): List<String> {
        val crew = members(state).toSet()
        return state.agents.values
            .filter {
                !it.isPlayer && it.location == loc &&
                    it.waypoints.isEmpty() && !it.asleep &&
                    it.name !in crew &&
                    CharacterSystem.npcInside(it.location) ==
                        state.playerInside
            }
            .map { it.name }
            .sorted()
    }

    /** Members standing with the player RIGHT NOW — mode
     *  says with, and the body agrees. The hooks and the
     *  page read this one truth. */
    fun presentCrew(state: GameState): List<String> {
        val ploc = playerOrNull(state)?.location ?: return emptyList()
        return members(state).filter { n ->
            modeOf(state, n) == "with" &&
                state.agents[n]?.location == ploc
        }
    }

    /** Midnight: the crew disperses (sleep does that to a
     *  crew) — membership, since-days and tallies persist;
     *  every standing arrangement clears, and yesterday's
     *  call stamps go with it. */
    fun dailyRollover(state: GameState) {
        for (n in members(state)) {
            clearStanding(state, n)
            state.lifeText.remove(kCalled(n))
        }
        state.lifeText.remove(K_PLOC)
    }

    // ---------------- the shared things count ----------------

    /** A shutter fired (Photos.applyPhoto's funnel): crew
     *  standing with you who are IN the frame tally a
     *  photo together. The frame's chemistry was the
     *  Photos module's own — this only counts. */
    fun onPhoto(state: GameState, rec: PhotoRecord) {
        val present = presentCrew(state).toSet()
        if (present.isEmpty()) return
        val framed = rec.npcs.map { it.name }.toSet()
        for (n in present intersect framed) {
            state.life[kPic(n)] = (state.life[kPic(n)] ?: 0) + 1
        }
    }

    /** A showdown settled (Minigames' funnel): whoever
     *  stood with you watched it. */
    fun onShowdown(state: GameState) {
        val present = presentCrew(state)
        for (n in present) {
            state.life[kWit(n)] = (state.life[kWit(n)] ?: 0) + 1
        }
    }

    /** A rival contest settled (Rival.recordResult's
     *  funnel): same witnessing, plus the total the
     *  RIVAL page quotes. */
    fun onContest(state: GameState) {
        val present = presentCrew(state)
        if (present.isEmpty()) return
        for (n in present) {
            state.life[kWit(n)] = (state.life[kWit(n)] ?: 0) + 1
        }
        state.life[K_WIT_TOTAL] =
            (state.life[K_WIT_TOTAL] ?: 0) + 1
    }

    /** Contests your crew has stood and watched — the
     *  RIVAL page's line reads this. */
    fun contestsWitnessed(state: GameState): Int =
        state.life[K_WIT_TOTAL] ?: 0

    // ---------------- the town notices ----------------

    private val SPOTTED = listOf(
        "“Okay, the new kid's crew is actually a crew now. Look at them.”",
        "“Some of us walk places ALONE like peasants, but sure, enjoy the entourage.”",
        "“It's just some kids standing near each other. The legend grows.”",
        "“There goes the crew. I give it a name and everything. Privately. In my head.”",
        "“Do they practice walking in like that, or does it just happen?”"
    )

    private fun plantTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** The crew's faction color, if it has one: the
     *  faction three or more members share, else null. */
    fun majorityFaction(state: GameState): String? =
        members(state).mapNotNull { factionOfCast(it) }
            .groupingBy { it }.eachCount()
            .entries.firstOrNull { it.value >= 3 }?.key

    // ---------------- crew talk ----------------

    private val PLACE_LINES = listOf(
        "Okay, {place} with the crew. This is a good crew day. " +
            "I'm putting it in the record.",
        "You know what this place needed? Us. Specifically us, " +
            "standing right here.",
        "If anybody asks, the crew was at {place} and we were " +
            "all extremely normal.",
        "Crew field-trip energy is off the charts right now. " +
            "{place} has no idea.",
        "I vote we simply stay at {place} until somebody makes " +
            "us leave. Democracy.",
        "This is the part of the day I'll be exaggerating " +
            "about later."
    )

    private val OTHER_LINES = listOf(
        "Me and {other} already voted. You're outvoted. The " +
            "motion was snacks.",
        "{other} has been telling the SAME story since we left. " +
            "It's getting better every time, somehow.",
        "Don't look at me — {other} planned this route. I'm " +
            "just the talent.",
        "If {other} says 'shortcut' one more time I'm walking " +
            "home alone. …No I'm not.",
        "The crew hierarchy is simple: whoever's holding " +
            "snacks leads. Currently: chaos."
    )

    private val RIVAL_CREW_LINES = listOf(
        "Being in your crew changes NOTHING about the " +
            "scoreboard. I want that noted. In writing.",
        "Yes, I'm rolling with the crew today. No, I will " +
            "not be taking questions."
    )

    private val RIVAL_OPINION_LINES = listOf(
        "Saw your little crew. Cute. A crew is just witnesses " +
            "with snacks, you know.",
        "Your crew follows you around town and you call ME " +
            "obsessed. Noted. Filed. Framed."
    )

    private fun pickIdx(seed: Int, size: Int): Int =
        (seed and Int.MAX_VALUE) % size

    /** Lines a Talk opens with, in memoryPreLines' stack —
     *  at most one per call, each stamped so a person says
     *  their crew piece once in a while, not every tap.
     *  Every slot fills from real state: the place you are
     *  actually in, a member actually standing here. */
    fun talkLines(state: GameState, npcName: String): List<Line> {
        val crew = members(state)
        if (crew.isEmpty()) return emptyList()
        val slot = state.absDay * 4 + state.timeMin / 360
        if ((state.life[kLine(npcName)] ?: Int.MIN_VALUE) == slot)
            return emptyList()
        val me = playerOrNull(state) ?: return emptyList()
        // a member, standing with you: crew talk
        if (npcName in crew && modeOf(state, npcName) == "with" &&
            state.agents[npcName]?.location == me.location) {
            state.life[kLine(npcName)] = slot
            if (npcName == Rival.rivalOf(state)) {
                val idx = pickIdx(state.absDay + npcName.hashCode(),
                    RIVAL_CREW_LINES.size)
                return listOf(Line(npcName, RIVAL_CREW_LINES[idx]))
            }
            val others = presentCrew(state) - npcName
            val place = LOCATIONS[me.location]?.name ?: me.location
            return if (others.isNotEmpty() &&
                (state.absDay + state.timeMin / 60) % 2 == 0) {
                val other = others[pickIdx(
                    state.absDay + npcName.hashCode(), others.size)]
                val idx = pickIdx(state.absDay * 7 + npcName.hashCode(),
                    OTHER_LINES.size)
                listOf(Line(npcName, OTHER_LINES[idx]
                    .replace("{other}", other)))
            } else {
                val idx = pickIdx(state.absDay * 7 + npcName.hashCode(),
                    PLACE_LINES.size)
                listOf(Line(npcName, PLACE_LINES[idx]
                    .replace("{place}", place)))
            }
        }
        // the rival, watching your crew from outside it
        if (npcName == Rival.rivalOf(state) && npcName !in crew &&
            Rival.revealed(state)) {
            state.life[kLine(npcName)] = slot
            val idx = pickIdx(state.absDay + npcName.hashCode(),
                RIVAL_OPINION_LINES.size)
            return listOf(Line(npcName, RIVAL_OPINION_LINES[idx]))
        }
        // faction color: three-plus of the crew share a
        // faction, and this person flies that flag
        val majority = majorityFaction(state)
        if (majority != null && npcName !in crew &&
            factionOfCast(npcName) == majority) {
            state.life[kLine(npcName)] = slot
            val fname = FACTIONS.firstOrNull { it.id == majority }
                ?.name ?: majority
            return listOf(Line(npcName,
                "Word is you're running with a $fname crew these " +
                    "days. $fname notices these things."))
        }
        return emptyList()
    }

    // ---------------- the Journal page ----------------

    /** The 👥 YOUR CREW page's lines — pure assembly; the
     *  Journal renders. Line 0 is the header. */
    fun pageLines(state: GameState): List<String> {
        val lines = mutableListOf<String>()
        val crew = members(state)
        lines += "👥 YOUR CREW"
        if (crew.isEmpty()) {
            lines += "No crew yet. A new kid alone is just a " +
                "kid; a new kid with a crew is a developing story."
            lines += "Ask people in: tap somebody you're at " +
                "least Friends with (friendship 30+) and choose " +
                "“Ask to join your crew.” Room for $MAX_CREW."
            lines += "Crew members come find you when you're " +
                "out — on foot, at walking speed, when their own " +
                "lives allow. School, work, dinners and the town's " +
                "big moments still own them first, and they'll " +
                "tell you so."
            return lines
        }
        val me = playerOrNull(state)
        for (n in crew) {
            val places = (state.lifeText[kPlaces(n)] ?: "")
                .split(",").count { it.isNotEmpty() }
            lines += "🤝 $n — crew since Day ${memberSince(state, n)}" +
                when (modeOf(state, n)) {
                    "with" -> " · with you now"
                    "go" -> " · on their way"
                    else -> " · out in their own life"
                }
            lines += "   outings ${state.life[kOut(n)] ?: 0} · " +
                "📸 ${state.life[kPic(n)] ?: 0} photos together · " +
                "🗺 $places places together · " +
                "👀 ${state.life[kWit(n)] ?: 0} contests witnessed"
        }
        val rival = Rival.rivalOf(state)
        if (rival != null && rival in crew)
            lines += "🆚 Yes, $rival is in the crew. The rivalry " +
                "has not noticed any difference. The scoreboard " +
                "runs either way."
        majorityFaction(state)?.let { fid ->
            val fname = FACTIONS.firstOrNull { it.id == fid }?.name
                ?: fid
            lines += "🏳 Three or more of you fly the $fname flag. " +
                "The town has noticed. $fname has noticed louder."
        }
        lines += "Joining, leaving and swapping are always free — " +
            "somebody who leaves can't be re-asked the same day. " +
            "Room for $MAX_CREW; ${crew.size} aboard."
        lines += "When ${me?.name ?: "the new kid"} goes home or " +
            "sleeps, the crew disperses — membership stays, " +
            "the outing ends."
        return lines
    }

    // ---------------- tallies (read by the suite & page) ----------------

    fun outings(state: GameState, name: String): Int =
        state.life[kOut(name)] ?: 0

    fun photosTogether(state: GameState, name: String): Int =
        state.life[kPic(name)] ?: 0

    fun witnessed(state: GameState, name: String): Int =
        state.life[kWit(name)] ?: 0

    fun placesTogether(state: GameState, name: String): Int =
        (state.lifeText[kPlaces(name)] ?: "")
            .split(",").count { it.isNotEmpty() }

    /** The last place this member stood with you (the
     *  outing record's tail; null when they've never been
     *  anywhere with you) — the group chat's outing facts
     *  cite it, so the line can only name a real visit. */
    fun lastPlaceTogether(state: GameState, name: String):
        String? =
        (state.lifeText[kPlaces(name)] ?: "")
            .split(",").map { it.trim() }
            .filter { it.isNotEmpty() }.lastOrNull()
}

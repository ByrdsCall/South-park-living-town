package com.hunt3r.southparktown

// ============================================================
// THE CLUBHOUSE (v0.14.0e, Queue #14 item 5) — a crew hangout.
//
// Your crew (v0.9.0b) roams with you, but until now it had
// nowhere that was ITS. This build gives the crew a spot
// it claims for itself:
//
//   THE SPOT, BY SEASON — the crew's claim rotates with
//   the town's own seasons (seasonFor, read off the game
//   month — the one season truth every surface reads):
//     SPRING → the TREEHOUSE at Kid's Park (outdoor, new
//              leaves, somebody's dad built it in a weekend
//              and it shows, proudly)
//     SUMMER → the GARAGE at the Marsh Residence (the one
//              place on the block with shade, a fridge that
//              hums, and a door that stays open — the Marsh
//              garage is named in the house's own record:
//              its blurb and its drawn driveway/garage)
//     FALL   → the TREEHOUSE again (leaf season is
//              treehouse season; the park wears it)
//     WINTER → the BASEMENT at Your House (indoors, warm,
//              yours — the house that already grew a
//              darkroom and an attic grows its crew a room)
//   No new map location: every spot is a fixture at a
//   location the town already has, and the agenda claim
//   seats the crew AT that location, the way every claim
//   in this town works.
//
//   THE HANGOUT — every day, 15:00–19:00, the crew is
//   claimed to the spot by ONE new agenda claim, seated
//   after the stand's in Engine's stack (the
//   twenty-second call there since v0.15.0f seated the
//   gallery show above the sleepovers; the twenty-first
//   call before that), before your crew's own hook —
//   above only the crew hook, below every set piece in
//   town, and self-guarded
//   the established way (the crew's occupied oracle,
//   Crew.busyReason, read whole, plus a veto pass over
//   every claim seated above it that busyReason doesn't
//   know). A hangout is the lowest-stakes claim in town
//   after the rival crew's court, and it yields like one.
//
//   PRICED LIKE A RENDEZVOUS — a member is only claimed
//   when the trip honestly fits: priceFor reads
//   Walk.routeMinutes from where the member actually is
//   to the spot (the same pricing Crew's rendezvous and
//   the emergencies swear by), and a member whose walk
//   would land after the window closes is not claimed —
//   they simply can't make it today, and the Journal page
//   says the spot waits. Nobody teleports; the claim is
//   the crew's standing plan, and the Walk graph is its
//   price list. (A member already walking to the spot
//   under the claim stays claimed — their destination
//   IS the spot.)
//
//   THE BEATS — three, in the town's verbs: 0 SETTLE IN
//   (Talk), 1 THE PROJECT (Look at the spot — every
//   hangout has a project: a fort plan, a ranked list, a
//   repair, authored per spot and season), 2 CREW
//   BUSINESS (Talk). No new TalkGate rail: the Talk beats
//   ride the conversation's crew-talk channel — the seam
//   Crew.talkLines and Commissions.talkLines already
//   speak through in GameActivity's memoryPreLines —
//   judged by Clubhouse.talkLines itself when a beat is
//   pending, and the Look beat hides in the Look at the
//   spot, the dinners/sleepovers pattern. The gate stands
//   at eleven rails, proven in the suite.
//
//   CREW-ONLY TALK — at the spot, in the window, a crew
//   member speaks clubhouse: lines about THIS spot, THIS
//   season, who is actually here and what the crew has
//   actually done (hangouts on the record, places the
//   crew has been — Crew's own tallies, quoted, never
//   invented). Non-members at the same place get none of
//   it: the banks are the crew's, the way the crew's
//   rendezvous regrets are the crew's. Kenny muffled and
//   Timmy by proclamation, as everywhere.
//
//   THE PAY — none, and stated: a hangout is not a job.
//   The day's +1 friendship for a member who comes to you
//   is Crew's own arrival rate, paid by Crew's rendezvous
//   machine when it fires, and the clubhouse does not
//   pay it twice. The wrap plants the "clubhouse" topic
//   in everyone who came (Threads carries the follow-ups)
//   and writes the day to the record — that is all.
//
//   EMPTY CREW — the clubhouse stands down politely: no
//   claim, no beats, no lines, and the Journal page says
//   so plainly — a clubhouse needs a crew — punished
//   nowhere, exactly like the birthday's household-only
//   day and the sleepover's empty plan.
//
//   THE RECORDS — the Journal 🏠 THE CLUBHOUSE page and
//   ONE morning-after line in the house report when a
//   hangout wrapped (the sleepovers' one-line discipline).
//   NO phone text and NO paper line, deliberately: a
//   hangout is a daily, low-stakes afternoon, not an
//   event — a text per hangout would spend the shared
//   MAX_TEXTS budget on routine, and the paper covers
//   the town's occasions, not its afternoons. The page
//   and the morning line carry the record whole.
//
// STATE: life/lifeText only, club* keys listed at their
// sites — no new save keys, no new GameState fields. A
// pre-0.14.0e save loads with no hangouts and simply
// starts having them once it has a crew.
// ============================================================

object Clubhouse {

    // ---------------- dials ----------------

    /** Hangout hours: the after-school stretch, every day. */
    const val HANG_FROM = 15 * 60
    const val HANG_TO = 19 * 60

    /** The afternoon's beats: settle in, the project,
     *  crew business. */
    const val STEP_COUNT = 3
    val STEP_TITLES = listOf("Settle In", "The Project",
        "Crew Business")

    /** The record keeps this many afternoons. */
    const val HISTORY_CAP = 8

    /** The topic a played-through hangout plants in a
     *  member who came (Threads follow-ups carry it). */
    const val TOPIC = "the clubhouse"

    // ---------------- state keys ----------------
    // All additive, all in the life/lifeText idiom:
    //   clubDay        life: the beats' day
    //   clubMask       life: the beats' mask for clubDay
    //   clubAtt        lifeText: attendees, csv (beat-unioned)
    //   clubDays       lifeText: csv absDays wrapped, cap 8
    //   clubRec:<day>  lifeText: "spotId|names csv|beats"
    //   clubTotal      life: hangouts wrapped, all told
    //   clubWrapDay    life: the last wrapped day
    //   clubRollDay    life: the rollover's day guard
    //   clubLine:<n>   life: <n>'s last talk-line slot

    private fun kLine(n: String) = "clubLine:$n"
    private fun kRec(day: Int) = "clubRec:$day"

    // ---------------- the spot ----------------

    /** One claimed spot: a fixture at a location the town
     *  already has. [seasonLabel] is how the page names
     *  the season it belongs to. */
    data class Spot(
        val id: String,
        val name: String,
        val locId: String,
        val fixture: String
    )

    val TREEHOUSE = Spot("treehouse", "the treehouse",
        "KidsPark",
        "a platform, a rope ladder, and a sign that says " +
            "CREW in letters of increasing confidence")
    val GARAGE = Spot("garage", "the garage", "StanHouse",
        "a workbench nobody works at, a fridge that hums, " +
            "and lawn chairs in a strict hierarchy")
    val BASEMENT = Spot("basement", "the basement",
        YOUR_HOUSE,
        "a couch that has seen things, a rug with a map " +
            "drawn on it in marker, and the good lamp")

    /** The season's spot, off the town's one season truth.
     *  Spring and fall share the treehouse — the park in
     *  leaf and the park in leaves are two different rooms
     *  wearing one address, and the copy says so. */
    fun spotFor(season: Season): Spot = when (season) {
        Season.WINTER -> BASEMENT
        Season.SUMMER -> GARAGE
        Season.SPRING, Season.FALL -> TREEHOUSE
    }

    fun seasonOf(state: GameState): Season =
        seasonFor(state.gameMonth)

    fun spot(state: GameState): Spot = spotFor(seasonOf(state))

    fun spotLoc(state: GameState): String = spot(state).locId

    fun spotName(state: GameState): String {
        val s = spot(state)
        val place = LOCATIONS[s.locId]?.name ?: s.locId
        return "${s.name} at $place"
    }

    // ---------------- standing ----------------

    fun members(state: GameState): List<String> =
        Crew.members(state)

    /** The crew exists — the one condition the whole
     *  module stands or stands down on. */
    fun hasCrew(state: GameState): Boolean =
        members(state).isNotEmpty()

    fun inWindow(state: GameState): Boolean =
        state.timeMin in HANG_FROM until HANG_TO

    /** Does the hangout stand right now? A crew, the
     *  window, and the town's bigger moments not owning
     *  the hour (Emergencies.momentOwned — the hangout
     *  yields wholesale, like the rival crew's court). */
    fun stands(state: GameState): Boolean =
        hasCrew(state) && inWindow(state) &&
            !Emergencies.momentOwned(state)

    /** The rendezvous price, in the Walk graph's own
     *  minutes, for [name] to reach the spot from where
     *  they actually are. The hangout's price list — the
     *  page quotes it and the claim swears by it. */
    fun priceFor(state: GameState, name: String): Int {
        val a = state.agents[name] ?: return Int.MAX_VALUE
        return Walk.routeMinutes(state, a.location, spotLoc(state))
    }

    /** Can [name] honestly make the hangout? Free by the
     *  crew's occupied oracle, and the walk fits the
     *  window that's left — or they're already here, or
     *  already walking here under the claim. */
    fun canGather(state: GameState, name: String): Boolean {
        val a = state.agents[name] ?: return false
        if (a.asleep) return false
        if (a.location == spotLoc(state)) return true
        if (a.destination == spotLoc(state)) return true
        val busy = Crew.busyReason(state, name) ?: return true
        return false
    }

    /** The price gate, apart from the occupied oracle:
     *  the walk from where [name] stands lands by the
     *  window's close. (canGather's oracle half is read
     *  in gatheringBlock; the two halves are pinned
     *  apart in the suite so neither can drift.) */
    fun priceFits(state: GameState, name: String): Boolean {
        val a = state.agents[name] ?: return false
        if (a.location == spotLoc(state)) return true
        if (a.destination == spotLoc(state)) return true
        val price = priceFor(state, name)
        if (price == Int.MAX_VALUE) return false
        return state.timeMin + price <= HANG_TO
    }

    /** Members at the spot right now, awake — the room. */
    fun presentCrew(state: GameState): List<String> =
        members(state).filter { name ->
            val a = state.agents[name]
            a != null && !a.asleep && a.location == spotLoc(state)
        }

    // ---------------- the claim ----------------

    /** The occupied-oracle reader on the PlayerBirthday
     *  pattern — pure (no busyReason, no vetoes): is this
     *  member the clubhouse's to claim, on the clock?
     *  (Kept pure for the same reason the birthday's is:
     *  the oracle must never recurse.) */
    fun claims(state: GameState, name: String): Boolean =
        stands(state) && name in members(state)

    /** The Engine's gathering hook — seat twenty-one,
     *  after the stand's claim and before the crew's own
     *  hook: a free member whose walk fits is at the spot
     *  for the window, unless a bigger claim holds them.
     *  The veto pass reads every claim seated above this
     *  one that Crew.busyReason doesn't already know —
     *  the established self-guard, written out whole. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in HANG_FROM until HANG_TO) return null
        if (!stands(state)) return null
        if (npcName !in members(state)) return null
        val busy = Crew.busyReason(state, npcName)
        if (busy != null) return null
        if (!priceFits(state, npcName)) return null
        if (Fair.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Carnival.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (RivalCrew.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Heroes.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Play.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Dance.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Talent.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Parade.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (FieldTrip.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Festival.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Sleepovers.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (PlayerBirthday.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Stand.gatheringBlock(state, npcName, timeMin) != null)
            return null
        val s = spot(state)
        return AgendaBlock(s.locId,
            "Clubhouse hours — the crew's ${s.name}, " +
                "claimed for the season.")
    }

    // ---------------- beats ----------------

    private fun stepsMask(state: GameState): Int =
        if ((state.life["clubDay"] ?: -1) == state.absDay)
            state.life["clubMask"] ?: 0 else 0

    fun stepsDone(state: GameState): Int =
        Integer.bitCount(stepsMask(state))

    fun currentStep(state: GameState): Int {
        val mask = stepsMask(state)
        var s = 0
        while (s < STEP_COUNT && (mask and (1 shl s)) != 0) s++
        return s
    }

    /** Members the afternoon actually saw (beat-unioned —
     *  presence is the record, the parties' discipline). */
    fun attendees(state: GameState): List<String> =
        (state.lifeText["clubAtt"] ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

    private fun fire(state: GameState, step: Int) {
        state.life["clubDay"] = state.absDay
        val seen = attendees(state).toMutableSet()
        seen.addAll(presentCrew(state))
        if (seen.isNotEmpty())
            state.lifeText["clubAtt"] = members(state)
                .filter { it in seen }.joinToString(",")
                .ifEmpty { seen.joinToString(",") }
        state.life["clubMask"] = stepsMask(state) or (1 shl step)
    }

    /** A hangout beat a Talk would land right now, or
     *  null. Steps 0 and 2 are Talks; step 1 is the Look
     *  and never answers here. Pure. */
    fun talkStep(state: GameState, npcName: String): Int? {
        if (!stands(state)) return null
        if (Sleepovers.plannedTonight(state)) return null
        if (PlayerBirthday.partyStands(state)) return null
        if (npcName !in members(state)) return null
        val a = state.agents[npcName] ?: return null
        if (a.asleep || a.location != spotLoc(state)) return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        if (me.location != spotLoc(state)) return null
        val step = currentStep(state)
        if (step == 1 || step >= STEP_COUNT) return null
        return step
    }

    private val SETTLE_INS: List<String> = listOf(
        "🏠 SETTLE IN. {G} gets to {spot} first and does " +
            "the honors: the good seat claimed, the second-" +
            "good seat argued over, the crew officially in " +
            "session. This is the clubhouse. It has always " +
            "been the clubhouse; today it's official.",
        "🏠 SETTLE IN. {G} arrives at {spot} with the " +
            "expression of a person clocking in to their " +
            "favorite job. Chairs are arranged by seniority, " +
            "which everyone disputes and nobody can prove. " +
            "Session: open.",
        "🏠 SETTLE IN. {G} signs the crew into {spot} — " +
            "not literally, there's no book, but the energy " +
            "of signing is there. The hangout is underway, " +
            "and the afternoon is already better than the " +
            "morning was.")

    private val BUSINESS: List<String> = listOf(
        "🏠 CREW BUSINESS. {G} calls the meeting nobody " +
            "scheduled: one item — the crew is doing fine — " +
            "and one motion — snacks are to be taken more " +
            "seriously as a crew value. Both pass unanimously. " +
            "The clubhouse adjourns in good order.",
        "🏠 CREW BUSINESS. {G} raises the only order of " +
            "business {spot} ever really has: where the crew " +
            "goes next, and who's bringing what. Decisions " +
            "are reached, immediately forgotten, and somehow " +
            "still kept. That's crew law.",
        "🏠 CREW BUSINESS. {G} does the rounds: everybody " +
            "reports in, nobody has anything to report, and " +
            "the reporting itself turns out to be the point. " +
            "The clubhouse keeps the crew a crew. Minutes " +
            "are not kept. Memories are.")

    private fun scene(bank: List<String>, state: GameState,
                      guest: String): String =
        bank[kotlin.math.abs(state.absDay * 3 +
            guest.hashCode()) % bank.size]
            .replace("{G}", guest)
            .replace("{spot}", spotName(state))

    /** The Look beat's texts — the project, per spot: a
     *  season of afternoons, each with its small work.
     *  Fact-bound: the text names only the spot, the
     *  season and who is actually here. */
    fun projectText(state: GameState): String {
        val s = spot(state)
        val here = presentCrew(state)
        val who = if (here.isEmpty()) "the crew"
            else here.joinToString(" and ")
        return when (s.id) {
            "treehouse" ->
                "🏠 THE PROJECT. The treehouse gets its " +
                    "season's inspection: $who checks the " +
                    "boards, re-ties the rope ladder's worst " +
                    "knot, and updates the ranking of branches " +
                    "by sitting quality. The sign gets a fresh " +
                    "letter where last season's faded. It " +
                    "holds. It all holds."
            "garage" ->
                "🏠 THE PROJECT. The garage gets its season's " +
                    "sort: $who ranks the lawn chairs, evicts " +
                    "exactly one box of somebody else's stuff " +
                    "to a slightly different corner, and " +
                    "declares the workbench a snack station " +
                    "until further notice. The fridge hums " +
                    "its approval."
            else ->
                "🏠 THE PROJECT. The basement gets its " +
                    "season's council: $who re-draws one street " +
                    "of the rug map from memory, argues it " +
                    "right, and re-aims the good lamp at the " +
                    "couch's reading corner. Winter headquarters: " +
                    "improved, marginally, proudly."
        }
    }

    /** The Look at the spot while the afternoon waits on
     *  step 1 IS the project (the dinners' pattern): it
     *  fires the beat. Any other Look in the window reads
     *  the venue line instead (venueText). */
    fun lookText(state: GameState, locId: String): String? {
        if (!stands(state)) return null
        if (locId != spotLoc(state)) return null
        if (currentStep(state) != 1) return null
        if (presentCrew(state).isEmpty()) return null
        val text = projectText(state)
        fire(state, 1)
        return text
    }

    /** What a Look at the spot says while the hangout
     *  stands but its project beat isn't up — the venue
     *  line, so the place reads as claimed. Never fires. */
    fun venueText(state: GameState, locId: String): String? {
        if (!stands(state)) return null
        if (locId != spotLoc(state)) return null
        if (currentStep(state) == 1) return null
        val s = spot(state)
        val here = presentCrew(state)
        val who = if (here.isEmpty())
            "The crew isn't in yet — the Walk prices the " +
                "trip, and afternoons run on walking time."
        else "${here.joinToString(", ")} " +
            (if (here.size == 1) "is" else "are") +
            " in session."
        return "🏠 The clubhouse — ${s.name}, claimed by " +
            "your crew for the season: ${s.fixture}. $who"
    }

    // ---------------- crew-only talk ----------------

    /** The clubhouse banks: at the spot, in the window,
     *  a member talks about the spot, the season, and the
     *  crew's real record. Every slot fills from real
     *  state — the spot you are standing in, a member
     *  actually here, tallies Crew already keeps. */
    val SPOT_LINES: List<String> = listOf(
        "This is the spot. {spot}. I've decided it's " +
            "officially the best room the crew has, and " +
            "the crew has had some rooms.",
        "{spot} hours are the best hours. I don't make " +
            "the rules. I do enforce them, though.",
        "We should put a sign up. A real one. 'Crew " +
            "property, trespassers will be offered a chair " +
            "and then ignored.'",
        "Every crew needs a place that's just theirs. " +
            "This is ours until the season turns, and then " +
            "the next one is. That's the deal with seasons.",
        "I counted: this is a top-three afternoon. The " +
            "list is not written down. The list is sacred.")

    val RECORD_LINES: List<String> = listOf(
        "The crew's been to {places} places together. " +
            "{spot} is the one we keep coming back to. " +
            "There's a lesson in that. The lesson is chairs.",
        "{hangouts} hangouts on the record and this one's " +
            "already in my top half. Low bar? No. Good crew.",
        "Outings, photos, all of it — but THIS is the " +
            "part that feels like the crew is a real thing " +
            "and not just walking near each other with intent.")

    val KENNY_LINES: List<String> = listOf(
        "(muffled) Mmph mmph mmph! (The clubhouse is " +
            "great, apparently — Kenny's enthusiasm needs " +
            "no translation.)",
        "(muffled) Mmmph! (Kenny has claimed a seat and " +
            "is not moving. Clubhouse law respects this.)")

    const val TIMMY_LINE =
        "TIMMY! (Translation: the clubhouse rules. Some " +
            "laws are one word long.)"

    /** The conversation's clubhouse line for [npcName],
     *  or null — the crew-talk channel's clubhouse seat
     *  (GameActivity's memoryPreLines calls this beside
     *  Crew.talkLines). Two jobs, in order: (1) a pending
     *  Talk beat IS this line — judged as it plays, the
     *  witness-line discipline; (2) failing that, a
     *  clubhouse bank line, stamped once per member per
     *  slot like Crew's own talk, so a person says their
     *  clubhouse piece once in a while, not every tap.
     *  Crew-only, spot-only, fact-bound: a non-member, a
     *  member elsewhere, or anyone outside the window
     *  gets silence from this bank. */
    fun talkLines(state: GameState, npcName: String): List<Line> {
        // (1) the pending Talk beat, judged as it plays
        val step = talkStep(state, npcName)
        if (step != null) {
            val text = when (step) {
                0 -> scene(SETTLE_INS, state, npcName)
                else -> scene(BUSINESS, state, npcName)
            }
            fire(state, step)
            return listOf(Line(npcName, text))
        }
        // (2) the bank — crew at the spot, in the window
        if (!stands(state)) return emptyList()
        if (npcName !in members(state)) return emptyList()
        val a = state.agents[npcName] ?: return emptyList()
        if (a.asleep || a.location != spotLoc(state))
            return emptyList()
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return emptyList()
        if (me.location != spotLoc(state)) return emptyList()
        val slot = state.absDay * 4 + state.timeMin / 360
        if ((state.life[kLine(npcName)] ?: Int.MIN_VALUE) == slot)
            return emptyList()
        state.life[kLine(npcName)] = slot
        if (npcName == "Kenny") {
            val idx = kotlin.math.abs(state.absDay +
                npcName.hashCode()) % KENNY_LINES.size
            return listOf(Line(npcName, KENNY_LINES[idx]))
        }
        if (npcName == "Timmy")
            return listOf(Line(npcName, TIMMY_LINE))
        val hangouts = hangoutsHeld(state)
        val places = Crew.placesTogether(state, npcName)
        val useRecord = hangouts > 0 &&
            (state.absDay + state.timeMin / 60) % 2 == 0
        return if (useRecord) {
            val idx = kotlin.math.abs(state.absDay * 7 +
                npcName.hashCode()) % RECORD_LINES.size
            listOf(Line(npcName, RECORD_LINES[idx]
                .replace("{spot}", spotName(state))
                .replace("{places}", places.toString())
                .replace("{hangouts}", hangouts.toString())))
        } else {
            val idx = kotlin.math.abs(state.absDay * 7 +
                npcName.hashCode()) % SPOT_LINES.size
            listOf(Line(npcName, SPOT_LINES[idx]
                .replace("{spot}", spotName(state))))
        }
    }

    // ---------------- the wrap ----------------

    data class Hangout(
        val day: Int, val spotId: String,
        val came: List<String>, val beats: Int
    )

    fun hangouts(state: GameState): List<Hangout> {
        val days = (state.lifeText["clubDays"] ?: "")
            .split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.mapNotNull { d ->
            val parts = (state.lifeText[kRec(d)] ?: "").split("|")
            if (parts.size < 3) null else Hangout(
                d, parts[0],
                parts[1].split(",").map { it.trim() }
                    .filter { it.isNotEmpty() },
                parts[2].toIntOrNull() ?: 0)
        }.reversed()
    }

    fun hangoutsHeld(state: GameState): Int =
        state.life["clubTotal"] ?: 0

    fun hangoutsAttended(state: GameState, name: String): Int =
        hangouts(state).count { name in it.came }

    private fun plantTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** Rollover (Engine.onNewDay, beside the crew's own):
     *  yesterday's afternoon wraps — a hangout with beats
     *  and a room goes on the record, plants its topic,
     *  and counts; an afternoon nobody reached leaves no
     *  trace, and NO friendship moves here (Crew's arrival
     *  rate is the day's pay, paid by Crew's machine). */
    fun dailyRollover(state: GameState) {
        if (state.life["clubRollDay"] == state.absDay) return
        state.life["clubRollDay"] = state.absDay
        val y = state.absDay - 1
        if ((state.life["clubDay"] ?: -2) == y) {
            val beats = Integer.bitCount(state.life["clubMask"] ?: 0)
            val came = attendees(state)
                .filter { it in members(state) }
            if (beats > 0 && came.isNotEmpty()) {
                // yesterday's spot, re-derived from
                // yesterday's season — the calendar moved
                // at this same rollover, so read the spot
                // off the ENDED day's month, not today's.
                val endedKey = keyFromOrdinal(
                    FairCalendar.ordinalOfAbsDay(y))
                val endedSeason = seasonFor(
                    endedKey.substring(4, 6).toInt())
                state.lifeText[kRec(y)] = listOf(
                    spotFor(endedSeason).id,
                    came.joinToString(","),
                    beats.toString()
                ).joinToString("|")
                val days = ((state.lifeText["clubDays"] ?: "")
                    .split(",").mapNotNull {
                        it.trim().toIntOrNull() }
                    .toMutableList())
                days.add(y)
                while (days.size > HISTORY_CAP) {
                    val old = days.removeAt(0)
                    state.lifeText.remove(kRec(old))
                }
                state.lifeText["clubDays"] = days.joinToString(",")
                state.life["clubTotal"] =
                    (state.life["clubTotal"] ?: 0) + 1
                state.life["clubWrapDay"] = y
                for (g in came) plantTopic(g)
            }
        }
        state.life.remove("clubDay")
        state.life.remove("clubMask")
        state.lifeText.remove("clubAtt")
    }

    /** The morning-after line for the house report: only
     *  the morning right after a wrapped hangout. */
    fun morningNote(state: GameState): String? {
        if ((state.life["clubWrapDay"] ?: -1) != state.absDay - 1)
            return null
        val last = hangouts(state).firstOrNull() ?: return null
        if (last.came.isEmpty()) return null
        val spot = listOf(TREEHOUSE, GARAGE, BASEMENT)
            .firstOrNull { it.id == last.spotId } ?: return null
        return "🏠 Clubhouse: yesterday the crew held " +
            "${spot.name} — ${last.came.joinToString(", ")} " +
            "came by, ${last.beats} of $STEP_COUNT beats of " +
            "the afternoon in."
    }

    // ---------------- the Journal page ----------------

    /** The 🏠 THE CLUBHOUSE page: the season's claim in
     *  plain words, today's standing, and the record.
     *  Pure assembly; the Journal renders. Line 0 is the
     *  header. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🏠 THE CLUBHOUSE"
        val s = spot(state)
        val place = LOCATIONS[s.locId]?.name ?: s.locId
        if (!hasCrew(state)) {
            out += "No clubhouse yet — a clubhouse needs a " +
                "crew, and yours is empty. The spot is " +
                "waiting anyway: this season it's ${s.name} " +
                "at $place (${s.fixture})."
            out += "Win some friends first (the Journal's " +
                "👥 YOUR CREW page explains the asking). " +
                "The day you have a crew, the claim stands " +
                "on its own — nothing to unlock, nobody to " +
                "ask, punished nowhere for the wait."
            return out
        }
        out += "This season the crew claims ${s.name} at " +
            "$place — ${s.fixture}."
        out += "The rotation: spring and fall, the " +
            "treehouse at Kid's Park; summer, the garage " +
            "at the Marsh Residence; winter, the basement " +
            "at your house. The season turns, the crew " +
            "moves, the clubhouse is wherever the crew is."
        when {
            inWindow(state) && stands(state) -> {
                val here = presentCrew(state)
                out += "🏠 Clubhouse hours are on — " +
                    "3 PM to 7 PM at ${s.name}."
                out += if (here.isEmpty())
                    "Nobody's in yet. Getting here is priced " +
                        "like a rendezvous: the Walk graph's " +
                        "own minutes from wherever each member " +
                        "actually is, and a member whose walk " +
                        "lands after 7 isn't claimed today — " +
                        "the spot waits, nobody teleports."
                else
                    "In session: ${here.joinToString(", ")}."
                val step = currentStep(state)
                out += if (step >= STEP_COUNT)
                    "The afternoon is whole: settle in, the " +
                        "project, crew business."
                else "The afternoon so far: " +
                    "${stepsDone(state)} of $STEP_COUNT beats " +
                    "in. Next: " + listOf(
                        "settle in (talk to a crewmate here)",
                        "the project (look around the spot)",
                        "crew business (talk to a crewmate here)"
                    )[step] + "."
            }
            inWindow(state) ->
                out += "It's clubhouse hours, but the town's " +
                    "bigger moments own this one — the " +
                    "hangout stands down and the spot waits. " +
                    "Nothing is owed and nothing is lost."
            else ->
                out += "Clubhouse hours: 3 PM to 7 PM daily, " +
                    "at ${s.name}. Outside them the spot is " +
                    "just a place; inside them it's the crew's."
        }
        val hist = hangouts(state)
        val total = hangoutsHeld(state)
        if (total > 0) {
            out += "🏠 Hangouts on the record: $total."
            for (h in hist) {
                val hSpot = listOf(TREEHOUSE, GARAGE, BASEMENT)
                    .firstOrNull { it.id == h.spotId }
                out += "— ${hSpot?.name ?: h.spotId}: " +
                    h.came.joinToString(", ") +
                    " · ${h.beats} of $STEP_COUNT beats"
            }
        } else {
            out += "· No hangouts yet. Show up at the spot " +
                "in the hours with your crew around — the " +
                "afternoon does the rest."
        }
        out += "A hangout pays no coins and no friendship " +
            "of its own — the crew's arrival rate is Crew's " +
            "own, and it isn't paid twice. What a hangout " +
            "pays is the afternoon itself, and the record."
        return out
    }
}

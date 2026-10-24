package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// 👥 THE RIVAL'S CREW (v0.9.0e) — your rival has friends too
// ============================================================
//
// v0.8.0e crowned a rival. v0.9.0b gave YOU a crew. This
// module gives the rival theirs — three kids, DERIVED, never
// authored per-save: an authored precedence over real data
// (pair-script partners first, then the rival's faction
// mates, then every other kid; inside a tier, the pair's
// real chemistry with the rival decides, name breaks ties).
// The pick is recorded in lifeText the first time it is
// consulted after the crowning and is never re-derived — a
// pre-0.9.0e save grows the crew by the same rule the first
// midnight (or first look) after the update.
//
// The four of them hold court on HOME TURF — the rival's
// faction's own authored turf, first entry — on a seeded
// rhythm (one day in three, 15:30–19:30), through an agenda
// claim in the established stack: after the fair's claim,
// before your crew's, self-guarded like the fair's (it
// yields to dinners, jobs, movie nights, capstones,
// finales, live climaxes, emergencies — and to
// Emergencies.momentOwned wholesale: a hangout is the
// lowest-stakes claim in town). Their members stay full
// citizens; nothing here touches their agendas beyond the
// claim, their dinners, or any case.
//
// THREE CREW CONTESTS ride the Rival scheduling discipline
// (invitations on the RIVAL rail, declining free, deference
// via momentOwned exactly as solo contests):
//   🏃 crew relay     — every runner's leg priced off the
//                       Walk matrix from where they actually
//                       stand at the accept; YOUR leg is the
//                       anchor, run for real on the clock,
//                       exactly like the solo race.
//   📸 crew photo     — armed at the accept; your next
//      showdown          group frame AT THE TURF with at
//                       least two fielded crew in it settles.
//                       Yours scores by Yearbook's group
//                       math over the real record; theirs
//                       is rolled on the same scale from
//                       their real chemistry; judge notes
//                       cite contest facts only.
//   ❄️ crew showdown  — a bracket of paired duels on
//      bracket            authored dodge. You play the
//                       captains' duel for real; the other
//                       pairs settle on the same math
//                       (Feel.POWER_GOOD × (1 − dodge)).
// Every crew contest lands on the solo 🆚 ledger through
// Rival.recordResult — once, or not at all. Crew contests
// move the CREWS STANDINGS and the ledger ONLY: no
// friendship, no coins, no economy (the captains' duel is
// a real duel with its own settled rules, exactly as the
// solo showdown contest is).
//
// THE STANDINGS are anchored to the SCHOOL YEAR (the
// yearbook's own June 1 – May 31 — the rhythm the fair
// also hangs off August in): win 2, draw 1, across the
// season; the season's last three weeks can turn the next
// due contest into THE FINAL (double points, a played
// captain's beat with a stance choice in the reveal's
// pattern); past seasons keep one line of history each.
//
// EDGE CASES, said plainly in the rival's own voice:
//   - You have NO crew: the offer still comes, and says
//     you need a crew of your own first. Solo contests are
//     untouched; their crew exists and roams regardless.
//   - The FRENEMY case (the rival is in YOUR crew): for
//     crew contests they captain their own side and do not
//     count on yours — stated in the offer and their lines.
//   - A kid in BOTH crews sits crew contests out (loudly).
//   - A side that cannot field two free crew waits — the
//     offer names who is holding it up (Crew.busyReason,
//     the crew build's occupied honesty). Never stacked.
//
// All state rides state.life / state.lifeText under
// rivalCrew* keys — no new GameState field, no save-format
// change. Spoiler discipline: nothing here knows anything
// about any case.

object RivalCrew {

    // ---------------- the contests ----------------

    const val K_CREW_RELAY = "crew_relay"
    const val K_CREW_PHOTO = "crew_photo"
    const val K_CREW_SHOWDOWN = "crew_showdown"
    val CREW_KINDS = listOf(K_CREW_RELAY, K_CREW_PHOTO,
        K_CREW_SHOWDOWN)

    /** Crew challenges arrive on their own stamp, spaced
     *  like the solo ones are: a fortnight, give or take. */
    const val CREW_GAP_MIN = 12
    const val CREW_GAP_SPREAD = 6
    /** The first crew challenge lands a few days after
     *  their crew forms — time to see them around first. */
    const val FIRST_CREW_GAP = 3
    const val DECLINE_CREW_GAP = 5
    /** An offer you can't field yet waits a couple of days
     *  (busy people get free; nothing silently stacks). */
    const val WAIT_GAP = 2
    const val NO_CREW_GAP = 4

    // ---------------- the standings ----------------

    const val POINTS_WIN = 2
    const val POINTS_DRAW = 1
    const val FINAL_MULT = 2
    /** The final can only be crowned once the season has
     *  actually happened (a couple of contests on the
     *  books) and the school year is nearly out. */
    const val FINAL_MIN_PLAYED = 2
    const val FINAL_WINDOW_DAYS = 21
    const val HISTORY_CAP = 4

    // ---------------- the turf ----------------

    const val TURF_FROM = 15 * 60 + 30
    const val TURF_TO = 19 * 60 + 30

    const val TOPIC = "the crews"

    private fun playerName(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.name
            ?: "New Kid"

    // ---------------- recorded state ----------------

    private fun recordedCrew(state: GameState): List<String> =
        state.lifeText["rivalCrew"]
            ?.split(",")?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()

    /** Their crew of three — derived by the authored
     *  precedence below, RECORDED on first consult after
     *  the crowning, and read back forever after (bonds
     *  keep moving; a crew is a crew). Empty before the
     *  rivalry exists. */
    fun theirCrew(state: GameState): List<String> {
        val rec = recordedCrew(state)
        if (rec.size == 3) return rec
        if (!Rival.revealed(state)) return emptyList()
        val crew = deriveCrew(state)
        if (crew.size == 3) {
            state.lifeText["rivalCrew"] = crew.joinToString(",")
            if ((state.life["rivalCrewNextDay"] ?: 0) <= 0)
                state.life["rivalCrewNextDay"] =
                    state.absDay + FIRST_CREW_GAP +
                        (Rival.saveSeed(state) % 3)
        }
        return crew
    }

    /** The derivation, pure over the state's real data.
     *  Candidates are the KID cast (the rival is a kid;
     *  their crew is a kid crew — an adult double act is
     *  family, not crew). Precedence: (1) the rival's
     *  pair-script partners — the authored double acts;
     *  (2) the rival's faction mates; (3) every other kid.
     *  Inside a tier: the pair's chemistry with the rival,
     *  high to low (Photos.chemistryBetween — live bonds
     *  plus the recognized-pair bonus), then name A–Z so
     *  two runs over the same facts agree. */
    fun deriveCrew(state: GameState): List<String> {
        val rival = Rival.rivalOf(state)
        val partners = PAIR_SCRIPTS.keys
            .filter { rival in it }
            .map { it.first { n -> n != rival } }.toSet()
        val faction = factionOfCast(rival)
        val tierOf = { name: String -> when {
            name in partners -> 0
            faction != null && factionOfCast(name) == faction -> 1
            else -> 2
        } }
        return KID_ORDER
            .filter { it != rival && it in
                CharacterSystem.CAST_KIDS }
            .sortedWith(compareBy<String> { tierOf(it) }
                .thenByDescending {
                    Photos.chemistryBetween(state, rival, it) }
                .thenBy { it })
            .take(3)
    }

    /** The four of them, captain first — the set the town
     *  renders as a cluster and the contests field from. */
    fun crewSet(state: GameState): Set<String> {
        if (!Rival.revealed(state)) return emptySet()
        val crew = theirCrew(state)
        if (crew.size != 3) return emptySet()
        return (crew + Rival.rivalOf(state)).toSet()
    }

    /** Home turf: the rival's faction's own authored turf,
     *  first entry — the ground their people already claim.
     *  Fallbacks (a faction with no turf): the rival's
     *  house, then the park. */
    fun turfOf(state: GameState): String {
        val rival = Rival.rivalOf(state)
        val fid = factionOfCast(rival)
        val turf = FACTIONS.firstOrNull { it.id == fid }
            ?.turf?.firstOrNull()
        if (turf != null) return turf
        return KID_HOUSE[rival] ?: "KidsPark"
    }

    fun turfName(state: GameState): String =
        LOCATIONS[turfOf(state)]?.name ?: "the park"

    // ---------------- the turf claim ----------------

    /** The four holding court on their turf: one day in
     *  three (seeded), mid-afternoon into the evening.
     *  Checked per person by Engine.effectiveAgendaBlock
     *  (after Fair, before Crew) and SELF-GUARDED like the
     *  fair's claim: a set piece owning the moment, the
     *  fair's own claim, any Crew.busyReason (asleep,
     *  school, a dinner, a shift, a screening, a ceremony,
     *  a finale, a boss on shift, a live climax, a live
     *  emergency), or standing with YOUR crew each dissolve
     *  it. A hangout yields to everything. */
    fun gatheringBlock(state: GameState, name: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in TURF_FROM until TURF_TO) return null
        val set = crewSet(state)
        if (name !in set) return null
        if ((state.absDay + Rival.saveSeed(state)) % 3 != 0)
            return null
        if (Emergencies.momentOwned(state)) return null
        if (Fair.gatheringBlock(state, name, timeMin) != null)
            return null
        if (Crew.busyReason(state, name) != null) return null
        if (Crew.isMember(state, name) &&
            Crew.modeOf(state, name).let {
                it == "with" || it == "go" })
            return null
        val rival = Rival.rivalOf(state)
        return AgendaBlock(turfOf(state),
            if (name == rival)
                "Holding court at the ${turfName(state)} " +
                    "with the crew."
            else "Rolling with $rival's crew at the " +
                "${turfName(state)}.")
    }

    fun turfDayToday(state: GameState): Boolean =
        crewSet(state).isNotEmpty() &&
            (state.absDay + Rival.saveSeed(state)) % 3 == 0

    // ---------------- scheduling ----------------

    fun nextCrewDay(state: GameState): Int =
        state.life["rivalCrewNextDay"] ?: Int.MAX_VALUE

    fun crewPending(state: GameState): String =
        state.lifeText["rivalCrewPending"] ?: ""

    fun bracketPending(state: GameState): Boolean =
        (state.life["rivalCrewBracket"] ?: 0) == 1

    fun relayLive(state: GameState): Boolean =
        (state.life["rivalCrewPendingRace"] ?: 0) == 1

    /** Any crew contest armed, running, or owed — the
     *  rail never stacks a second crew offer on top. */
    fun crewBusy(state: GameState): Boolean =
        crewPending(state).isNotEmpty() || bracketPending(state) ||
            relayLive(state)

    /** The school-year season the standings belong to —
     *  Yearbook.currentYear (the June 1 – May 31 year,
     *  named by its ending spring). */
    fun seasonOf(state: GameState): Int =
        Yearbook.currentYear(state)

    fun seasonStored(state: GameState): Int =
        state.life["rivalCrewSeason"] ?: 0

    fun pointsYou(state: GameState): Int =
        state.life["rivalCrewPtsYou"] ?: 0

    fun pointsThem(state: GameState): Int =
        state.life["rivalCrewPtsThem"] ?: 0

    fun seasonPlayed(state: GameState): Int =
        state.life["rivalCrewSeasonPlayed"] ?: 0

    fun finalPlayedSeason(state: GameState): Int =
        state.life["rivalCrewFinalSeason"] ?: 0

    /** Days left in the school year (May 31 close),
     *  computed on civil-date ordinals so it is exact in
     *  every month and testable in the suite. */
    fun daysLeftInSchoolYear(state: GameState): Int =
        daysFromCivil(seasonOf(state), 5, 31) -
            daysFromCivil(state.gameYear, state.gameMonth,
                state.gameDay)

    private fun daysFromCivil(y: Int, m: Int, d: Int): Int {
        val yy = if (m <= 2) y - 1 else y
        val era = Math.floorDiv(yy, 400)
        val yoe = yy - era * 400
        val mp = (m + 9) % 12
        val doy = (153 * mp + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    /** The final's conditions: a season with contests on
     *  the books, its final unplayed, the school year
     *  inside its last three weeks, and a crew challenge
     *  actually due. */
    fun finalDue(state: GameState): Boolean {
        if (!Rival.revealed(state) || crewBusy(state)) return false
        if (seasonStored(state) != seasonOf(state)) return false
        if (seasonPlayed(state) < FINAL_MIN_PLAYED) return false
        if (finalPlayedSeason(state) == seasonOf(state))
            return false
        val left = daysLeftInSchoolYear(state)
        if (left !in 0..FINAL_WINDOW_DAYS) return false
        return state.absDay >= nextCrewDay(state)
    }

    /** A regular crew challenge is due. */
    fun offerDue(state: GameState): Boolean {
        if (!Rival.revealed(state) || crewBusy(state)) return false
        if (theirCrew(state).size != 3) return false
        if (finalDue(state)) return false
        return state.absDay >= nextCrewDay(state)
    }

    fun challengeDue(state: GameState): Boolean =
        finalDue(state) || offerDue(state)

    /** The crew kind on offer today, seeded — with the
     *  snowball bracket honestly gated on snow, exactly
     *  like the solo showdown. Relay and photo always run. */
    fun offerKindFor(state: GameState): String {
        val kinds = CREW_KINDS.filter { kind ->
            kind != K_CREW_SHOWDOWN ||
                state.weatherId in listOf("light_snow",
                    "heavy_snow", "blizzard")
        }
        val idx = ((state.absDay * 137 +
            Rival.rivalOf(state).hashCode()) and 0x7fffffff) %
            kinds.size
        return kinds[idx]
    }

    // ---------------- fielding ----------------

    data class Fielding(
        val you: List<String>,
        val them: List<String>,
        val blockers: List<String>,
        val youHaveCrew: Boolean,
        val frenemy: Boolean
    )

    /** Who can actually field today, and who is holding
     *  it up — Crew.busyReason's occupied honesty, applied
     *  to BOTH sides. The rival captains their own side
     *  even when they sit in yours (the frenemy rule); a
     *  kid in both crews sits out entirely. */
    fun fielding(state: GameState): Fielding {
        val mine = Crew.members(state)
        val rival = Rival.rivalOf(state)
        val theirs = theirCrew(state)
        val blockers = mutableListOf<String>()
        val both = mine.toSet() intersect theirs.toSet()
        for (n in both.sorted())
            blockers += "$n is in BOTH crews and is sitting " +
                "this one out, loudly"
        val you = mine.filter { name ->
            when {
                name == rival -> false   // captains the other side
                name in both -> false
                else -> {
                    val busy = Crew.busyReason(state, name)
                    if (busy != null) {
                        blockers += "$name — $busy"
                        false
                    } else true
                }
            }
        }
        val them = theirs.filter { name ->
            when {
                name in both -> false
                else -> {
                    val busy = Crew.busyReason(state, name)
                    if (busy != null) {
                        blockers += "$name — $busy"
                        false
                    } else true
                }
            }
        }
        return Fielding(you, them, blockers,
            youHaveCrew = mine.isNotEmpty(),
            frenemy = rival in mine)
    }

    /** A crew contest needs two free crew a side beside
     *  the captains — under that, it waits, said plainly. */
    fun canField(f: Fielding): Boolean =
        f.youHaveCrew && f.you.size >= 2 && f.them.size >= 2

    // ---------------- the offers ----------------

    fun kindBlurb(kind: String): String = when (kind) {
        K_CREW_RELAY -> "a crew relay across town — every " +
            "leg priced off where people are actually " +
            "standing when you say yes, and you run the " +
            "anchor leg yourself, for real"
        K_CREW_PHOTO -> "a crew photo showdown at the " +
            "turf — your next group frame there with at " +
            "least two of your crew in it gets judged " +
            "against theirs, yearbook math, no mercy"
        else -> "a crew showdown bracket — paired snowball " +
            "duels, and the captains' duel is yours to play " +
            "for real"
    }

    fun offerText(state: GameState, kind: String,
                  fin: Boolean): String {
        val def = Rival.defOf(Rival.rivalOf(state))
        val rival = Rival.rivalOf(state)
        val f = fielding(state)
        val sb = StringBuilder()
        if (fin) sb.append("🏆 THE CREWS FINAL — " +
            "double points, season on the line.\n\n")
        sb.append("“${def?.offerLine ?: ""}”\n\n")
        sb.append("$rival has a CREW now — " +
            "${theirCrew(state).joinToString(", ")} — and " +
            "they hold court at the ${turfName(state)}. " +
            "Today it's ${kindBlurb(kind)}.")
        sb.append("\n\n🏆 Crews season " +
            "(${Yearbook.labelOf(seasonOf(state))}): " +
            "you ${pointsYou(state)} — ${pointsThem(state)} " +
            "them, over ${seasonPlayed(state)} contests" +
            (if (fin) ", and this one counts DOUBLE." else "."))
        if (!f.youHaveCrew) {
            sb.append("\n\n“One problem, " +
                "${playerName(state)}. You don't HAVE " +
                "a crew. Mine has matching jackets — " +
                "metaphorically. Mostly. Get three friends " +
                "to Friend tier, bring them together, THEN " +
                "come talk to me about crew business.”")
        } else {
            if (f.frenemy) sb.append("\n\n“And before you " +
                "ask: yeah, I'm in YOUR crew. For crew " +
                "business I captain MY side. That's not " +
                "betrayal, that's BRACKET INTEGRITY.”")
            if (f.blockers.isNotEmpty())
                sb.append("\n\n⚠️ Holding it up: " +
                    f.blockers.joinToString("; ") + ".")
            if (!canField(f))
                sb.append("\n\nA crew contest needs two " +
                    "free crew standing beside each captain. " +
                    "It waits until both sides can field " +
                    "that — no stacking, no ringers.")
        }
        sb.append("\n\n(The 🆚 scoreboard counts it once, " +
            "win, lose or draw. Declining costs nothing.)")
        return sb.toString()
    }

    /** The stance choices for the final's captain's beat —
     *  the reveal's established pattern: three ways to
     *  play it, each remembered by the standing math. */
    fun finalStanceOptions(): List<String> = listOf(
        "🏆 “Then we take it clean. Best crew wins.”",
        "😈 “Whatever it takes. It's a final.”",
        "🤝 “Win or lose — this was the best season.”")

    data class CrewAccept(
        val kind: String, val text: String,
        val startDuel: Boolean, val waited: Boolean)

    /** Accept a crew challenge. Either it arms/runs (with
     *  the fielded sides snapshotted — legs and pairs are
     *  priced off the world AS IT STANDS at the yes), or
     *  it waits, plainly, and reschedules a few days out. */
    fun accept(state: GameState, kind: String,
               fin: Boolean): CrewAccept {
        val rival = Rival.rivalOf(state)
        val f = fielding(state)
        if (!f.youHaveCrew) {
            state.life["rivalCrewNextDay"] = state.absDay +
                NO_CREW_GAP
            return CrewAccept(kind,
                "“Crew first, THEN crew contests. I'm not " +
                    "racing an imaginary formation.” $rival " +
                    "walks off toward the " +
                    "${turfName(state)}, where three actual " +
                    "people are waiting.",
                startDuel = false, waited = true)
        }
        if (!canField(f)) {
            state.life["rivalCrewNextDay"] = state.absDay +
                WAIT_GAP
            val why = f.blockers.joinToString("; ")
                .ifEmpty { "not enough free crew" }
            return CrewAccept(kind,
                "“Not today — $why. A crew contest needs " +
                    "its people actually standing there. It " +
                    "waits. I'll ask again when the world's " +
                    "less busy being busy.”",
                startDuel = false, waited = true)
        }
        state.lifeText["rivalCrewFieldYou"] =
            f.you.joinToString(",")
        state.lifeText["rivalCrewFieldThem"] =
            f.them.joinToString(",")
        if (fin) state.life["rivalCrewFinalArm"] = 1
        return when (kind) {
            K_CREW_RELAY -> acceptRelay(state, f)
            K_CREW_PHOTO -> {
                state.lifeText["rivalCrewPending"] = kind
                state.lifeText["rivalCrewPlace"] = turfOf(state)
                state.life["rivalCrewArmDay"] = state.absDay
                CrewAccept(kind,
                    "“Then pose at OUR turf. The " +
                        "${turfName(state)}. Get at least " +
                        "two of yours — " +
                        f.you.joinToString(", ") +
                        " — in a real group frame there, and " +
                        "the judges do the rest. Ours is " +
                        "already perfect, obviously.”",
                    startDuel = false, waited = false)
            }
            else -> {   // K_CREW_SHOWDOWN
                state.life["rivalCrewBracket"] = 1
                state.life["rivalCrewArmDay"] = state.absDay
                val pairs = minOf(f.you.size, f.them.size)
                val bench = when {
                    f.you.size > pairs -> "\n(${f.you[pairs]} " +
                        "rides the bench for yours.)"
                    f.them.size > pairs -> "\n(${f.them[pairs]} " +
                        "rides the bench for theirs.)"
                    else -> ""
                }
                CrewAccept(kind,
                    "“Captains first. You and me, for real — " +
                        "then our crews settle their pairs on " +
                        "the same math: " +
                        (0 until pairs).joinToString("; ") { i ->
                            "${f.you[i]} vs ${f.them[i]}" } +
                        ".$bench”",
                    startDuel = true, waited = false)
            }
        }
    }

    /** Declining a crew challenge is free, like the solo
     *  ones: a few days' peace, nothing recorded (the solo
     *  decline's own arithmetic: gap + seed % 3). */
    fun decline(state: GameState) {
        state.life["rivalCrewNextDay"] = state.absDay +
            DECLINE_CREW_GAP +
            ((Rival.saveSeed(state) + state.absDay) % 3)
    }

    /** The final's stance beat: your captain's line, and
     *  the rival answering it — booked on the same esc /
     *  mercy ledgers the reveal feeds, so the standing you
     *  earn by May is the standing you actually played. */
    fun playFinalStance(state: GameState, which: Int): String {
        val rival = Rival.rivalOf(state)
        state.life["rivalCrewStance"] = which
        return when (which) {
            0 -> {
                state.life["rivalEsc"] =
                    (state.life["rivalEsc"] ?: 0) + 1
                "“Clean. CLEAN. Fine — then when we win, " +
                    "and we will, nobody gets to say a word " +
                    "about it. Deal, " +
                    "${playerName(state)}.” $rival " +
                    "sticks out a hand like it's a contract."
            }
            1 -> {
                state.life["rivalEsc"] =
                    (state.life["rivalEsc"] ?: 0) + 1
                "“Whatever it takes? WHATEVER IT TAKES is " +
                    "MY whole deal. You just declared war " +
                    "with a handshake, " +
                    "${playerName(state)}.” $rival is " +
                    "grinning like a holiday."
            }
            else -> {
                state.life["rivalMercy"] =
                    (state.life["rivalMercy"] ?: 0) + 1
                "“…Best season. Yeah. Don't — don't make " +
                    "it a THING right before the final, " +
                    "${playerName(state)}. …It was, " +
                    "though.” $rival looks at the turf " +
                    "like it owes them money."
            }
        }
    }

    // ---------------- 🏃 the crew relay ----------------

    /** A posted leg's price in half-minutes: the Walk
     *  matrix over the runner's real ground, plus a pace.
     *  Rival-pool kids run on their authored paceHalf;
     *  everyone else's pace derives from their authored
     *  dodge — a barn is slow, a greased weasel is not —
     *  forced odd on the solo race's tie-proofing rule. */
    fun paceHalfOf(name: String): Int {
        Rival.defOf(name)?.let { return it.paceHalf }
        val raw = ((0.30 - Showdown.dodgeFor(name)) * 40)
            .toInt()
        return if (raw % 2 == 0) raw + 1 else raw
    }

    fun legHalves(state: GameState, runner: String,
                  from: String, to: String): Int =
        Walk.routeMinutes(state, from, to) * 2 +
            paceHalfOf(runner)

    private fun acceptRelay(state: GameState,
                            f: Fielding): CrewAccept {
        val rival = Rival.rivalOf(state)
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return CrewAccept(K_CREW_RELAY,
                "$rival shrugs — nowhere to race from.",
                startDuel = false, waited = true)
        val cands = LOCATIONS.keys.filter { loc ->
            loc != me.location &&
                Walk.routeMinutes(state, me.location, loc) >=
                    Rival.RACE_MIN_ROUTE
        }
        if (cands.isEmpty()) return CrewAccept(K_CREW_RELAY,
            "“Nowhere far enough to relay to. Town's too " +
                "small today.”",
            startDuel = false, waited = true)
        val seed = (Rival.saveSeed(state) * 31L +
            state.absDay * 7L).toInt()
        val to = cands[(seed and 0x7fffffff) % cands.size]
        val locOf = { name: String ->
            state.agents[name]?.location ?: me.location }
        var youPost = 0
        for (m in f.you) youPost += legHalves(state, m,
            locOf(m), to)
        var themPost = legHalves(state, rival, locOf(rival), to)
        for (m in f.them) themPost += legHalves(state, m,
            locOf(m), to)
        // equal legs: a short-handed captain runs the
        // anchor twice (priced whole, both times — your
        // real clock counts double; their posted anchor
        // leg is charged again at the settle)
        val legsThem = 1 + f.them.size
        state.life["rivalCrewPendingRace"] = 1
        state.lifeText["rivalCrewRaceTo"] = to
        state.life["rivalCrewRaceStartMin"] = state.timeMin
        state.life["rivalCrewRaceDay"] = state.absDay
        state.life["rivalCrewYouPost"] = youPost
        state.life["rivalCrewThemPost"] = themPost
        state.life["rivalCrewYourLegs"] =
            maxOf(1, legsThem - f.you.size)
        state.life["rivalCrewTheirAnchorLeg"] =
            legHalves(state, rival, locOf(rival), to)
        val placeName = LOCATIONS[to]?.name ?: to
        return CrewAccept(K_CREW_RELAY,
            "“Relay. Everybody runs a leg INTO " +
                "$placeName from wherever they're standing " +
                "— priced honest, off the real map. Your " +
                "crew's legs are posted. YOUR leg is the " +
                "anchor: you run it for real, " +
                "${playerName(state)}, clock's " +
                "already running. Short-handed captains " +
                "run it twice.”",
            startDuel = false, waited = false)
    }

    /** Arrival funnel (GameActivity calls this beside
     *  Rival.raceArrival). Prices your real elapsed clock
     *  against the posted legs — yours and theirs — and
     *  settles once. An absurd clock voids it free, as
     *  the solo race's does. */
    fun raceArrival(state: GameState, locId: String): String? {
        if ((state.life["rivalCrewPendingRace"] ?: 0) != 1)
            return null
        val to = state.lifeText["rivalCrewRaceTo"] ?: ""
        if (locId != to) return null
        val rival = Rival.rivalOf(state)
        val startMin = state.life["rivalCrewRaceStartMin"] ?: 0
        val startDay = state.life["rivalCrewRaceDay"] ?: 0
        val elapsed = (state.absDay - startDay) * 1440 +
            state.timeMin - startMin
        val yourLegs = state.life["rivalCrewYourLegs"] ?: 1
        val youPost = state.life["rivalCrewYouPost"] ?: 0
        var themTotal = state.life["rivalCrewThemPost"] ?: 0
        val anchorLeg = state.life["rivalCrewTheirAnchorLeg"]
            ?: 0
        val fThem = fieldThem(state).size
        val fYou = fieldYou(state).size
        clearRelay(state)
        if (elapsed !in 1..600) {   // the solo race's bound
            // a voided final is not a played final: drop
            // the arm and let the final re-offer while
            // its window lasts
            consumeFinalArm(state)
            return "The crew relay to " +
                "${LOCATIONS[to]?.name ?: to} took so long " +
                "both crews agree it never happened."
        }
        val youTotal = elapsed * 2 * yourLegs + youPost
        // a short-handed rival captain's second anchor leg
        // is priced off their posted anchor, whole
        if (fYou > fThem) themTotal += anchorLeg
        val fin = consumeFinalArm(state)
        val winner = when {
            youTotal < themTotal -> "you"
            themTotal < youTotal -> "them"
            else -> "draw"
        }
        val fmt = { h: Int ->
            if (h % 2 == 0) "${h / 2}" else "${h / 2}.5" }
        val verdict = when (winner) {
            "you" -> "🏆 YOUR CREW TAKES THE RELAY!"
            "them" -> "💀 $rival's crew takes the relay."
            else -> "🤝 The relay is a dead heat."
        }
        settleCrewContest(state, K_CREW_RELAY, winner,
            "to ${LOCATIONS[to]?.name ?: to} — yours " +
                "${fmt(youTotal)} min against their " +
                "${fmt(themTotal)}", fin)
        return "$verdict (Your crew: ${fmt(youTotal)} min " +
            "all-in — your anchor counted " +
            "${fmt(elapsed * 2 * yourLegs)} of it. " +
            "Theirs: ${fmt(themTotal)} min.)"
    }

    // ---------------- 📸 the crew photo showdown ---------

    /** Photos.applyPhoto's funnel, beside Crew.onPhoto: an
     *  armed crew photo settles on the first group frame
     *  AT THE TURF with at least two fielded crew in it.
     *  Every other frame is just a photo. */
    fun onPhoto(state: GameState, rec: PhotoRecord) {
        if (crewPending(state) != K_CREW_PHOTO) return
        if (rec.locId != state.lifeText["rivalCrewPlace"])
            return
        if (!rec.isGroup) return
        val framed = fieldYou(state).filter {
            it in rec.npcs.map { f -> f.name } }
        if (framed.size < 2) return
        val rival = Rival.rivalOf(state)
        val yourScore = Yearbook.scoreOf(Yearbook.CAT_GROUP,
            rec)
        val side = listOf(rival) + fieldThem(state)
        val n = side.size
        val seed = (state.life["rivalCrewArmDay"] ?: 0) *
            131L + Rival.saveSeed(state)
        val frame = theirFrame(state, side, seed)
        val pairs = frame.pairs
        val goofy = frame.goofy
        val followers = frame.followers
        val theirScore = frame.score
        val yourPairs = rec.figures.count {
            it.pose == "backToBack" } / 2
        val yourGoofy = rec.npcs.count {
            it.reaction == Photos.GOOFY }
        val fin = consumeFinalArm(state)
        val winner = when {
            yourScore > theirScore -> "you"
            theirScore > yourScore -> "them"
            else -> "draw"
        }
        settleCrewContest(state, K_CREW_PHOTO, winner,
            "at the ${turfName(state)} — your frame " +
                "$yourScore (${rec.npcs.size} in frame, " +
                "$yourPairs posed pairs, $yourGoofy goofy) " +
                "to their $theirScore ($n of them, " +
                "$pairs chemistry pairs, $goofy goofy, " +
                "+$followers followers)", fin)
    }

    data class TheirFrame(val score: Int, val pairs: Int,
                          val goofy: Int, val followers: Int)

    /** Their frame, rolled on the yearbook's group scale
     *  from their members' real chemistry: 12 a head, 6 a
     *  posed pair (their high-chemistry pairs, capped at
     *  what a four-kid frame can pose), 3 a goofy face,
     *  plus a seeded follower trickle — the same fields
     *  Yearbook.rawScore reads off a real record. */
    fun theirFrame(state: GameState, side: List<String>,
                   seed: Long): TheirFrame {
        val n = side.size
        var chemPairs = 0
        for (i in side.indices) for (j in i + 1 until n)
            if (Photos.chemistryBetween(state, side[i],
                    side[j]) >= Photos.HIGH_CHEMISTRY)
                chemPairs++
        val pairs = minOf(2, chemPairs)
        val rnd = Random(seed)
        val goofy = rnd.nextInt(n + 1)
        val followers = rnd.nextInt(13)
        return TheirFrame(12 * n + 6 * pairs + 3 * goofy +
            followers, pairs, goofy, followers)
    }

    /** Their frame's would-be score alone — the suite's
     *  fairness probe. */
    fun theirPhotoScore(state: GameState, side: List<String>,
                        seed: Long): Int =
        theirFrame(state, side, seed).score

    // ---------------- ❄️ the crew showdown bracket -------

    /** One simulated pair on the duel's own math: throws
     *  alternate, each lands at Feel.POWER_GOOD ×
     *  (1 − the defender's authored dodge), first to
     *  Showdown.HITS_TO_WIN. Returns true if [yours] takes
     *  the pair. [yoursFirst] alternates by pair so the
     *  first throw doesn't favor one side all bracket. */
    fun simDuel(yours: String, theirs: String,
                yoursFirst: Boolean, seed: Long): Boolean {
        val rnd = Random(seed)
        var yHits = 0
        var tHits = 0
        var yoursThrow = yoursFirst
        var guard = 0
        while (yHits < Showdown.HITS_TO_WIN &&
            tHits < Showdown.HITS_TO_WIN && guard++ < 400) {
            if (yoursThrow) {
                if (rnd.nextDouble() < Feel.POWER_GOOD *
                    (1.0 - Showdown.dodgeFor(theirs))) yHits++
            } else {
                if (rnd.nextDouble() < Feel.POWER_GOOD *
                    (1.0 - Showdown.dodgeFor(yours))) tHits++
            }
            yoursThrow = !yoursThrow
        }
        return yHits >= tHits
    }

    /** applyShowdownResult's funnel, beside Rival's: your
     *  duel with the rival IS the captains' pair of an
     *  armed bracket; the crew pairs settle on the same
     *  math in the same moment, and the bracket settles
     *  once. A duel against anyone else is just a duel. */
    fun onShowdown(state: GameState, youWon: Boolean,
                   opponent: String) {
        if (!bracketPending(state)) return
        if (opponent != Rival.rivalOf(state)) return
        val fy = fieldYou(state)
        val ft = fieldThem(state)
        val pairs = minOf(fy.size, ft.size)
        var yourPairs = if (youWon) 1 else 0
        var theirPairs = if (youWon) 0 else 1
        val notes = mutableListOf(
            "captains: ${if (youWon) "yours" else "theirs"}")
        val seedBase = (state.life["rivalCrewArmDay"]
            ?: state.absDay) * 977L + Rival.saveSeed(state)
        for (i in 0 until pairs) {
            val won = simDuel(fy[i], ft[i], yoursFirst =
                i % 2 == 0, seed = seedBase + i * 31L)
            if (won) yourPairs++ else theirPairs++
            notes += "${fy[i]} vs ${ft[i]}: " +
                if (won) "yours" else "theirs"
        }
        val fin = consumeFinalArm(state)
        state.life["rivalCrewBracket"] = 0
        val winner = when {
            yourPairs > theirPairs -> "you"
            theirPairs > yourPairs -> "them"
            else -> "draw"
        }
        settleCrewContest(state, K_CREW_SHOWDOWN, winner,
            "pairs $yourPairs–$theirPairs (" +
                notes.joinToString("; ") + ")", fin)
    }

    // ---------------- the settle ----------------

    private fun fieldYou(state: GameState): List<String> =
        state.lifeText["rivalCrewFieldYou"]
            ?.split(",")?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()

    private fun fieldThem(state: GameState): List<String> =
        state.lifeText["rivalCrewFieldThem"]
            ?.split(",")?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()

    private fun consumeFinalArm(state: GameState): Boolean {
        val fin = (state.life["rivalCrewFinalArm"] ?: 0) == 1
        state.life.remove("rivalCrewFinalArm")
        return fin
    }

    private fun clearRelay(state: GameState) {
        state.life.remove("rivalCrewPendingRace")
        state.lifeText.remove("rivalCrewRaceTo")
        state.life.remove("rivalCrewRaceStartMin")
        state.life.remove("rivalCrewRaceDay")
        state.life.remove("rivalCrewYouPost")
        state.life.remove("rivalCrewThemPost")
        state.life.remove("rivalCrewYourLegs")
        state.life.remove("rivalCrewTheirAnchorLeg")
    }

    private fun clearPending(state: GameState) {
        state.lifeText.remove("rivalCrewPending")
        state.lifeText.remove("rivalCrewPlace")
        state.life.remove("rivalCrewArmDay")
        state.life["rivalCrewBracket"] = 0
        // a lapsed arm takes its final flag with it — the
        // next contest is never accidentally the final
        // (a settling path consumes the flag first, so a
        // played final keeps its mark in settleCrewContest)
        state.life.remove("rivalCrewFinalArm")
        clearRelay(state)
    }

    /** Every crew contest settles HERE: the standings
     *  move, the next crew day is stamped, and the result
     *  lands on the solo 🆚 ledger through
     *  Rival.recordResult — exactly once. The solo
     *  challenge calendar (rivalNextDay) is preserved
     *  across the call: a crew contest makes the board,
     *  it doesn't shove the solo queue. */
    fun settleCrewContest(state: GameState, kind: String,
                          winner: String, detail: String,
                          fin: Boolean) {
        val mult = if (fin) FINAL_MULT else 1
        when (winner) {
            "you" -> state.life["rivalCrewPtsYou"] =
                pointsYou(state) + POINTS_WIN * mult
            "them" -> state.life["rivalCrewPtsThem"] =
                pointsThem(state) + POINTS_WIN * mult
            else -> {
                state.life["rivalCrewPtsYou"] =
                    pointsYou(state) + POINTS_DRAW * mult
                state.life["rivalCrewPtsThem"] =
                    pointsThem(state) + POINTS_DRAW * mult
            }
        }
        state.life["rivalCrewSeasonPlayed"] =
            seasonPlayed(state) + 1
        if (fin) state.life["rivalCrewFinalSeason"] =
            seasonOf(state)
        state.life["rivalCrewLastDay"] = state.absDay
        state.lifeText["rivalCrewLastKind"] = kind
        state.lifeText["rivalCrewLastWinner"] = winner
        state.life["rivalCrewNextDay"] = state.absDay +
            CREW_GAP_MIN +
            Random(Rival.saveSeed(state).toLong() * 31 +
                state.absDay).nextInt(CREW_GAP_SPREAD)
        clearPending(state)
        val keepNext = state.life["rivalNextDay"]
        Rival.recordResult(state, kind, winner,
            (if (fin) "🏆 THE FINAL — " else "") + detail)
        if (keepNext != null)
            state.life["rivalNextDay"] = keepNext
        else state.life.remove("rivalNextDay")
        plantCrewsTopic(state)
        pushTicker(state,
            "👥 ${Rival.kindLabel(kind)} settled: " +
                crewsScoreLine(state))
    }

    private fun pushTicker(state: GameState, text: String) {
        state.events.add(text)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    fun plantCrewsTopic(state: GameState) {
        val who = (listOf(Rival.rivalOf(state)) +
            theirCrew(state) + Crew.members(state)).distinct()
        for (n in who) {
            val mem = NpcMemoryStore.getOrCreate(n)
            mem.topics.remove(TOPIC)
            mem.topics.add(TOPIC)
            while (mem.topics.size > 6) mem.topics.removeAt(0)
        }
    }

    // ---------------- standings surfaces ----------------

    /** The one factual crews sentence — the page, the
     *  ticker and the town's gossip all quote it. */
    fun crewsScoreLine(state: GameState): String {
        val y = pointsYou(state)
        val t = pointsThem(state)
        val lead = when {
            y > t -> "your crew leads"
            t > y -> "their crew leads"
            seasonPlayed(state) == 0 -> "no contests yet"
            else -> "dead level"
        }
        return "The crews stand $y–$t ($lead) in the " +
            "${Yearbook.labelOf(seasonOf(state))} season, " +
            "over ${seasonPlayed(state)} crew contests."
    }

    fun historyLines(state: GameState): List<String> =
        state.lifeText["rivalCrewHistory"]
            ?.split(";")?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()

    /** The 🆚 page's crews section (Rival.pageLines appends
     *  it once the rivalry exists): both rosters, the
     *  standings, the season's shape, and one line per
     *  finished season. */
    fun pageSection(state: GameState): List<String> {
        val lines = mutableListOf<String>()
        val rival = Rival.rivalOf(state)
        val theirs = theirCrew(state)
        lines += ""
        lines += "👥 THE CREWS — season " +
            Yearbook.labelOf(seasonOf(state))
        if (theirs.size == 3) {
            lines += "Theirs: $rival (captain), " +
                theirs.joinToString(", ") +
                " — turf: the ${turfName(state)}" +
                (if (turfDayToday(state))
                    " (they're holding court there today, " +
                        "15:30–19:30)" else "")
            val mine = Crew.members(state)
            lines += if (mine.isEmpty())
                "Yours: no crew yet — theirs holds the " +
                    "turf unopposed. (Get three friends to " +
                    "Friend tier and bring them together.)"
            else "Yours: ${playerName(state)} " +
                "(captain), " + mine.joinToString(", ") +
                (if (rival in mine)
                    " — yes, $rival is in your crew too. " +
                        "For crew contests they captain " +
                        "theirs. Bracket integrity."
                else "")
            lines += "🏆 ${crewsScoreLine(state)}"
            when {
                finalPlayedSeason(state) == seasonOf(state) ->
                    lines += "🏆 The final is played — the " +
                        "season's book is closed but the " +
                        "bragging isn't."
                finalDue(state) ->
                    lines += "🏆 THE FINAL is due — double " +
                        "points. $rival is waiting to be " +
                        "talked to."
                seasonPlayed(state) >= FINAL_MIN_PLAYED &&
                    daysLeftInSchoolYear(state) >
                        FINAL_WINDOW_DAYS ->
                    lines += "🏆 The final comes in the " +
                        "school year's last three weeks — " +
                        "double points."
                crewBusy(state) ->
                    lines += "⏳ A crew contest is live — " +
                        "see it through."
                state.absDay < nextCrewDay(state) ->
                    lines += "⏳ The next crew challenge " +
                        "brews for a few days yet."
            }
            historyLines(state).forEach { lines += it }
        } else {
            lines += "$rival hasn't gathered a crew yet."
        }
        return lines
    }

    /** TalkGate's owed lines: the town repeats the
     *  standings while they're fresh — the same discipline
     *  as Rival.ambientLine's scoreboard gossip, quoting
     *  the one factual crews sentence. */
    fun gossipLine(state: GameState, npcName: String):
        Line? {
        if (!Rival.revealed(state)) return null
        val last = state.life["rivalCrewLastDay"]
            ?: return null
        if (state.absDay - last !in 0..Rival.FRESH_DAYS)
            return null
        if (npcName == Rival.rivalOf(state)) return null
        if (npcName in crewSet(state)) return null
        if (Crew.isMember(state, npcName)) return null
        val key = "rivalCrewGossip:$npcName"
        if (state.life[key] == state.absDay) return null
        state.life[key] = state.absDay
        return Line(npcName, "Did you hear? " +
            crewsScoreLine(state))
    }

    // ---------------- midnight ----------------

    /** Engine.onNewDay, beside Rival.dailyRollover: form
     *  the crew on the first midnight after the crowning,
     *  close a finished season into its one history line,
     *  and lapse stale arms free — exactly as the solo
     *  contests lapse theirs. */
    fun dailyRollover(state: GameState) {
        if (Rival.revealed(state)) {
            val crew = theirCrew(state)
            if (crew.size == 3 && seasonStored(state) == 0)
                state.life["rivalCrewSeason"] = seasonOf(state)
            if (crew.size == 3 &&
                seasonStored(state) != seasonOf(state))
                closeSeason(state)
        }
        // a live relay lapses overnight; armed photo /
        // bracket pendings lapse after the solo contests'
        // own lapse window
        if (relayLive(state)) {
            clearRelay(state)
            state.life["rivalCrewNextDay"] = state.absDay + 1
        }
        val armDay = state.life["rivalCrewArmDay"] ?: -99
        if ((crewPending(state).isNotEmpty() ||
                bracketPending(state)) &&
            state.absDay - armDay > Rival.PENDING_LAPSE_DAYS) {
            clearPending(state)
            state.life["rivalCrewNextDay"] = state.absDay + 2
        }
    }

    private fun closeSeason(state: GameState) {
        val old = seasonStored(state)
        val y = pointsYou(state)
        val t = pointsThem(state)
        val n = seasonPlayed(state)
        if (n > 0 || y > 0 || t > 0) {
            val verdict = when {
                y > t -> "your crew took the season"
                t > y -> "their crew took the season"
                else -> "nobody took it — dead level"
            }
            val fin = if (finalPlayedSeason(state) == old)
                " · final played" else " · final never played"
            val line = "📅 Crews ${Yearbook.labelOf(old)}: " +
                "you $y — $t them over $n contests · " +
                "$verdict$fin"
            val hist = (historyLines(state) + line)
                .takeLast(HISTORY_CAP)
            state.lifeText["rivalCrewHistory"] =
                hist.joinToString(";")
        }
        state.life["rivalCrewPtsYou"] = 0
        state.life["rivalCrewPtsThem"] = 0
        state.life["rivalCrewSeasonPlayed"] = 0
        state.life["rivalCrewSeason"] = seasonOf(state)
    }
}

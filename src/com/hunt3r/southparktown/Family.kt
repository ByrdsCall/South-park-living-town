package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Family — v0.10.0a (Queue #10, item 1): YOUR FAMILY ARRIVES.
//
// The New Kid stops living alone. Mom and Dad move into Your
// House as HOUSEHOLD NPCs in the generated-NPC pattern
// (VisitorPersona + ActiveNpc, like the lorebook residents and
// the arc-2 usher) — deliberately NOT cast: they are not in
// AGENT_PROFILES, not in state.agents, not in KID_ORDER, and
// every census the suite pins (70 cast, bark floors, pair
// scripts, faction tables, the rival pool) is untouched. The
// suite pins that from both sides: the cast tables don't name
// them, and Family never writes state.friendship (family warmth
// lives in life["famPts:<parent>"], so no cast-facing system —
// mail lines, tier events, HELLO texts — can ever read a
// parent as a townsfolk).
//
// State (all additive, riding the life/lifeText maps):
//   life["famArrivalDay"]  the morning they arrive (stamped
//                          at first consult, RivalCrew-style)
//   life["famArrived"]     1 once the arrival beat is played
//   lifeText["famStance"]   warm | fine | complicated
//   life["famPts:Mom"/"famPts:Dad"]  family warmth, -50..100
//   family-dinner keys     famDinDay/famDinMask/famDinFirst,
//                          famDinLast (last attended), famDinMissed
//   allowance keys         famAllowanceDay/famAllowanceTotal
//   chore keys             famChore (kind), famChoreBy,
//                          famChoreDay, famChoreDone,
//                          famChoreLastDone,
//                          famChoreDoneBy:<parent> (v0.13.0a)
//   owed keys              famOwed:<parent> (a pending line id)
//
// Citizenship limits, stated: the parents hold no faction, join
// no crew (yours or the rival's), enter no contest, bee, fair
// event or case, and are never a TalkGate rail. Their evenings
// are their own except the established deference: family-dinner
// beats wait out climax-pending exactly like Dinners' beats do,
// a holiday beat owning the evening cancels the family table,
// and hosting YOUR guests at your table moves the family to
// the counter. Their work blocks yield to holidays the way cast
// agendas do (a holiday at home).
//
// Spoiler discipline: family writing reacts only to a case's
// PUBLIC shape — that a case is open, that the town is talking —
// never to any solution, suspect truth, or sealed content.
//
// v0.13.0a (Queue #13, item 1): THE FAMILY PAGE + HOME BARKS.
// The Journal gains a 👨‍👩‍👧 YOUR FAMILY page (pageLines below):
// a pure derivation over the family's real records — warmth,
// dinners, allowance, chores, the frame — rendered fresh on
// every read, storing nothing of its own. And Mom and Dad
// gain greeting/idle barks at home, in the barks system's
// pattern: their rhythm signatures live in
// Barks.HOUSEHOLD_PATTERNS (their own table — PATTERNS stays
// census-pinned to the 98 cast), their hello lines in the
// HOME_GREETINGS / HOME_IDLE banks below, and the decision
// itself in barkDecide (pure; GameActivity only wires it).
// Barks are flavor: they move no warmth, no coins, no state.
// The ONE new stored fact in this build: per-parent chore
// counters (famChoreDoneBy:<parent>) — who ASKED each finished
// chore exists nowhere else (famChoreBy is transient and is
// cleared at completion), and the page's "for whom" owes the
// truth, so the counters are kept from this build on and the
// page says plainly when a total predates them.
// ============================================================

object Family {

    const val MOM = "Mom"
    const val DAD = "Dad"
    val PARENTS: List<String> = listOf(MOM, DAD)

    /** A fresh save meets its family on the morning of this
     *  absDay. A pre-0.10.0a save (absDay already past it) gets
     *  the NEXT eligible morning — the derived arrival day is
     *  max(absDay, ARRIVAL_ABS_DAY) until it is stamped. */
    const val ARRIVAL_ABS_DAY = 3

    /** Their town jobs — real agenda anchors at real places. */
    const val MOM_WORK = "Bank"          // the new-accounts desk
    const val DAD_WORK = "PostOffice"    // the counter

    /** Family-dinner weekdays (absDay % 7) — the complement of
     *  the cast households' {1,3,5} (Dinners.DINNER_WEEKDAYS),
     *  by construction: the two tables can never claim the same
     *  evening, and day 6 belongs to nobody's schedule. */
    val DINNER_WEEKDAYS: Set<Int> = setOf(0, 2, 4)

    /** The weekly allowance: 3 capsule coins on the week's
     *  first morning (absDay % 7 == 0), IF a family dinner was
     *  sat at within the last 7 days — Mom's rule, stated in
     *  her own lines. It is the ONLY coin source this build
     *  adds, and it is named in the docs/TUNING.md tuning ledger:
     *  against a full paper-route week's 30 🪙 (5 shifts of
     *  2 base + 4 tasks), 3 🪙 for showing up at your own
     *  table is a gesture, not a wage. */
    const val ALLOWANCE_COINS = 3
    const val ALLOWANCE_WEEKDAY = 0

    /** Seeded share of family-dinner nights that seat a guest. */
    const val GUEST_CHANCE = 0.45

    fun isParent(name: String): Boolean = name == MOM || name == DAD

    fun workLocOf(parent: String): String =
        if (parent == MOM) MOM_WORK else DAD_WORK

    // ---------------- arrival & stance ----------------

    /** The arrival morning, stamped into the save the first
     *  time anyone asks (the RivalCrew first-consult rule):
     *  max(today, ARRIVAL_ABS_DAY), so a legacy save's family
     *  arrives on its first morning under this build and a new
     *  save's on day 3 — and the day never slides afterwards. */
    fun arrivalDay(state: GameState): Int {
        state.life["famArrivalDay"]?.let { return it }
        val d = maxOf(state.absDay, ARRIVAL_ABS_DAY)
        state.life["famArrivalDay"] = d
        return d
    }

    fun arrived(state: GameState): Boolean =
        (state.life["famArrived"] ?: 0) == 1

    /** The arrival beat is due: the day has come, the beat has
     *  not been played. (Presence itself begins with the beat —
     *  before it, the house is still the empty one.) */
    fun arrivalDue(state: GameState): Boolean =
        !arrived(state) && state.absDay >= arrivalDay(state)

    /** warm | fine | complicated — how the New Kid took the
     *  move; "" before the beat is played. */
    fun stance(state: GameState): String =
        state.lifeText["famStance"] ?: ""

    /** Play the arrival: one of the three stances, chosen in
     *  the safe-moment dialog (GameActivity). The house fills
     *  in from this moment (GameView gates the family corner
     *  on [arrived]) and both parents take their places. */
    fun chooseStance(state: GameState, stance: String): Boolean {
        if (!arrivalDue(state)) return false
        if (stance !in listOf("warm", "fine", "complicated")) return false
        state.life["famArrived"] = 1
        state.lifeText["famStance"] = stance
        adjustPoints(state, MOM, 4)
        adjustPoints(state, DAD, 4)
        return true
    }

    /** The arrival script, played as one conversation beat in
     *  the doorstep dialog's follow-up toast/conversation: kept
     *  here so the suite pins its spoiler-safety and the stance
     *  echo lines stay beside the banks that reuse them. */
    fun arrivalLine(parent: String, stance: String): String = when (parent) {
        MOM -> when (stance) {
            "warm" -> "There's my kid. Come here — new town, same us. " +
                "Your room's still your room; we just finally live in it too."
            "fine" -> "Hi, honey. It's fine, it's all fine — the truck's " +
                "unloaded, your father's already fixed a shelf that wasn't " +
                "broken. We're home."
            else -> "I know. I know it's a lot, all at once. We're not here " +
                "to crowd you — we're just... here. The kettle's on."
        }
        else -> when (stance) {
            "warm" -> "Hey, champ. Missed this. Missed YOU. The house " +
                "already looks better with your stuff next to our stuff."
            "fine" -> "Well. We're moved in. Everything's fine. I alphabetized " +
                "the spice rack, so — it's a home now, officially."
            else -> "Hey. Yeah. Big change, huh. I'm not gonna make a speech. " +
                "Just... my door's open. The fridge is full. That's the speech."
        }
    }

    fun stanceEchoLine(parent: String, stance: String): String = when (parent) {
        MOM -> when (stance) {
            "warm" -> "You know, you took this move like a champ. " +
                "A mother notices that."
            "fine" -> "You said you were fine about the move, and you know " +
                "what? You mostly have been. That's not nothing."
            else -> "Moving's hard. You never pretended otherwise, and I " +
                "respect that more than a big smile would've fooled me."
        }
        else -> when (stance) {
            "warm" -> "Still glad we're all under one roof. Don't tell your " +
                "mother I got sentimental; she'll frame it."
            "fine" -> "Everything still fine? Good. Fine is underrated. " +
                "Fine is most of a life, done right."
            else -> "I remember you needed some room about the move. " +
                "Hope the room's feeling bigger these days."
        }
    }

    // ---------------- family warmth ----------------

    /** Family points live OUTSIDE state.friendship on purpose
     *  (see the header): -50..100, same clamps as NpcMemory. */
    fun points(state: GameState, parent: String): Int =
        (state.life["famPts:$parent"] ?: 0).coerceIn(-50, 100)

    fun adjustPoints(state: GameState, parent: String, delta: Int): Int {
        val v = (points(state, parent) + delta).coerceIn(-50, 100)
        state.life["famPts:$parent"] = v
        return v
    }

    /** One warmth a day per parent for talking, first talk only
     *  (the cast's daily-gain shape, kept honest and small). */
    fun noteTalk(state: GameState, parent: String) {
        val key = "famTalkDay:$parent"
        if (state.life[key] == state.absDay) return
        state.life[key] = state.absDay
        adjustPoints(state, parent, 1)
    }

    // ---------------- looks: derived + authored ----------------

    /** THE DERIVATION, stated plainly (the brief asks what is
     *  derived and what is authored):
     *   DERIVED from the player's own creator appearance —
     *   the actual genetics the save honestly holds:
     *     · skin tone: the player's exact skin hex, both parents
     *     · hair color: the player's exact hair hex for Mom;
     *       for Dad, the same hex darkened (darkerHex 0.72) —
     *       and Dad's BEARD wears the player's exact hair hex
     *     · baldness: if the player's hair_style is "bald",
     *       Dad is bald too
     *   AUTHORED (they are their own people): hairstyles (Mom's
     *   bob, Dad's side part), clothes and their colors, Mom's
     *   lipstick, faces, widths. Adult proportions ride the
     *   band fallback (tall = true, unmeasured — exactly like
     *   generated NPCs), per the standing adult rule.
     *  [appearance] is the appearance to derive from — the live
     *  one in play, or a photo record's frozen one, so a family
     *  photo remembers the family as it looked that day. */
    fun specFor(parent: String, appearance: Map<String, String>): SheetSpec {
        val you = avatarSpec(appearance)
        val skin = you.skin
        val hair = you.hairColor
        return if (parent == MOM) SheetSpec(
            skin = skin, hairStyle = "bob", hairColor = hair,
            top = "#7a4a6a", collarHex = darkerHex("#7a4a6a"),
            bottom = "#463850", bottomStyle = "pants",
            shoeColor = "#33262b", face = "normal", lipstick = true,
            tall = true, width = 0.98f
        ) else SheetSpec(
            skin = skin,
            hairStyle = if (you.hairStyle == "bald") "bald" else "sidepart",
            hairColor = darkerHex(hair, 0.72f),
            beard = true, beardColor = hair, beardStyle = "full",
            top = "#4a6076", collarHex = darkerHex("#4a6076"),
            bottom = "#3c3a38", bottomStyle = "pants",
            shoeColor = "#2a2320", face = "normal",
            tall = true, width = 1.04f
        )
    }

    fun specFor(state: GameState, parent: String): SheetSpec {
        val you = state.agents.values.firstOrNull { it.isPlayer }
        return specFor(parent, you?.appearance ?: emptyMap())
    }

    /** The persona, in the generated-NPC pattern. The speech
     *  banks ride along (any generic surface that reads a
     *  persona's banks gets the family's real voice), but the
     *  family's own sheet (GameActivity) drives conversation
     *  through [greeting]/[reply] below. */
    fun persona(state: GameState, parent: String): VisitorPersona {
        val mom = parent == MOM
        return VisitorPersona(
            loreName = parent,
            spec = specFor(state, parent),
            band = AgeBand.ADULT,
            traits = if (mom) listOf("kind", "strict", "funny")
                     else listOf("steady", "handy", "dry"),
            role = if (mom) "the new-accounts desk at the Bank"
                   else "the counter at the Post Office",
            bio = if (mom)
                "Your mom. Runs the new-accounts desk at the Bank, " +
                "runs the house like a second shift, and can tell " +
                "from the doorway whether you've eaten."
            else
                "Your dad. Works the counter at the Post Office, " +
                "fixes things that aren't broken yet, and believes " +
                "most problems shrink after a meal.",
            facts = listOf(
                if (mom) "Mom does the week's wages-and-bills math on Sundays."
                else "Dad has never once thrown away a screw that " +
                    "“might fit something.”",
                "The family moved into Your House — the New Kid " +
                    "doesn't live alone anymore."
            ),
            resident = true, factionId = null,
            home = YOUR_HOUSE, workLoc = workLocOf(parent),
            hangout = YOUR_HOUSE,
            greetingLines = GREETINGS[parent] ?: emptyList(),
            factLines = FACTORY_LINES,
            deepLines = TOPICS[parent] ?: emptyList(),
            byeLines = BYES[parent] ?: emptyList(),
            ambientLines = AMBIENT[parent] ?: emptyList(),
            loves = if (mom) setOf("food", "coffee", "flower")
                    else setOf("food", "music", "sport")
        )
    }

    // ---------------- presence & the day's shape ----------------

    private val OUTINGS = listOf("MainStreet", "StarksPond", "KidsPark")

    /** One parent's block right now: (location, activity) or
     *  null when they're offstage (asleep, or not yet arrived).
     *  The shape mirrors the resident pattern (home bookends,
     *  work middays) with the family's own hours: they're up
     *  before the cast's 8 AM, home by 5, and their evenings
     *  are home evenings. Work yields on weekends and holidays
     *  exactly the way cast agendas yield. */
    fun blockAt(state: GameState, parent: String,
                timeMin: Int = state.timeMin): Pair<String, String>? {
        if (!arrived(state)) return null
        val hour = timeMin / 60
        if (isSleepHour(hour)) return null
        val work = workLocOf(parent)
        val dinnerPrep = dinnerTonight(state) &&
            timeMin >= Dinners.DINNER_FROM - 90
        if (dinnerPrep && timeMin < Dinners.DINNER_TO)
            return YOUR_HOUSE to (if (parent == MOM)
                "Home — getting family dinner together."
                else "Home — setting the table, mostly correctly.")
        if (isWeekend(state) || state.holidayId != null) {
            if (timeMin in 11 * 60 until 16 * 60) {
                val out = OUTINGS[kotlin.math.abs(
                    state.absDay * 3 + (if (parent == MOM) 0 else 1)) %
                    OUTINGS.size]
                return out to (if (parent == MOM)
                    "Out — errands and fresh air."
                    else "Out — a walk he calls 'checking on the town.'")
            }
            return YOUR_HOUSE to "Home — a slow family day."
        }
        return when {
            timeMin < 9 * 60 -> YOUR_HOUSE to
                "Home — morning, coffee, the day getting organized."
            timeMin < 17 * 60 -> work to (if (parent == MOM)
                "At the Bank — the new-accounts desk."
                else "At the Post Office — the counter.")
            else -> YOUR_HOUSE to "Home — evening, off the clock."
        }
    }

    /** The generated-NPC surface: who of the family is at
     *  [locId] with the player's [inside] right now. Parents
     *  are indoors at buildings and at home (they LIVE in Your
     *  House — the one household the “nobody inside but you”
     *  doorstep rule was always going to bend for), outdoors
     *  only on their weekend outing blocks. */
    fun presentAt(state: GameState, locId: String,
                  inside: Boolean): List<ActiveNpc> {
        if (!arrived(state)) return emptyList()
        // parents stand where the building-ness of the place
        // says: indoors at home and at work, outdoors on an outing
        if (CharacterSystem.isBuilding(locId) != inside) return emptyList()
        return PARENTS.mapNotNull { p ->
            val (loc, _) = blockAt(state, p) ?: return@mapNotNull null
            if (loc != locId) null
            else ActiveNpc(persona(state, p), locId, inside)
        }
    }

    /** The family-dinner GUEST's claim on their evening: on a
     *  guest night, the named cast kid is at Your House (at the
     *  door — cast never render inside your home) for the dinner
     *  window. Hooked into Engine.effectiveAgendaBlock right
     *  after Dinners' own claim, so finales and the town's
     *  bigger moments further down the stack still win. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (!arrived(state)) return null
        val guest = guestTonight(state) ?: return null
        if (npcName != guest) return null
        if (timeMin !in Dinners.DINNER_FROM until Dinners.DINNER_TO)
            return null
        return AgendaBlock(YOUR_HOUSE, "Family dinner at your house — " +
            "your folks invited them, and they actually came.")
    }

    // ---------------- photos ----------------

    /** Is a parent standing in this photo record? */
    fun inPhoto(rec: PhotoRecord): Boolean =
        rec.figures.any { it.name in PARENTS }

    /** The family's newest photo together — what hangs in the
     *  family frame in Your House (empty until one exists). */
    fun latestFamilyPhoto(state: GameState): PhotoRecord? =
        state.photos.lastOrNull { it.locId == YOUR_HOUSE && inPhoto(it) }

    // ---------------- talk ----------------

    private fun poolPick(pool: List<String>, seed: Int): String =
        if (pool.isEmpty()) "…" else pool[kotlin.math.abs(seed) % pool.size]

    /** The line a fresh conversation opens with. */
    fun greeting(state: GameState, parent: String): String {
        if (state.life["famStanceHeard:$parent"] != 1 &&
            stance(state).isNotEmpty()) {
            // the first real talk after the move carries the echo
            state.life["famStanceHeard:$parent"] = 1
            return stanceEchoLine(parent, stance(state))
        }
        owedLine(state, parent)?.let { return it }
        val hour = state.timeMin / 60
        val seed = state.absDay * 7 + (if (parent == MOM) 1 else 2)
        return when {
            hour < 11 -> poolPick(GREETINGS[parent] ?: emptyList(), seed)
            hour >= 17 -> poolPick(EVENINGS[parent] ?: emptyList(), seed)
            else -> poolPick(GREETINGS[parent] ?: emptyList(), seed + 3)
        }
    }

    /** A pending owed line (dinner thanks, a chore's thank-you,
     *  a print's weight) — delivered once, then cleared. The
     *  darkroom's print-owed line routes through Darkroom
     *  itself (GameActivity checks it on this sheet too). */
    fun owedLine(state: GameState, parent: String): String? {
        val id = state.lifeText["famOwed:$parent"] ?: return null
        state.lifeText.remove("famOwed:$parent")
        return when (id) {
            "dinner" -> if (parent == MOM)
                "Last night was nice. The whole table, full. " +
                    "That's the whole point of a house, near as I can tell."
                else "Still thinking about dinner. Good table. " +
                    "We should do that every — well. We basically do."
            "chore" -> if (parent == MOM)
                "And thank you for the help earlier. Noted, appreciated, " +
                    "remembered at allowance time."
                else "Appreciate the hand earlier. A house runs on that."
            else -> null
        }
    }

    private fun setOwed(state: GameState, parent: String, id: String) {
        state.lifeText["famOwed:$parent"] = id
    }

    /** Fact-slot lines: the parents notice the player's REAL
     *  life and say so with the fact in the sentence (the
     *  darkroom discipline). Derived fresh at every call; the
     *  rotation in [reply] shows the first unshown one today.
     *  Cases appear only in their public shape. */
    fun factLines(state: GameState, parent: String): List<String> {
        val out = mutableListOf<String>()
        val mom = parent == MOM
        val crew = Crew.members(state)
        if (crew.isNotEmpty()) out.add(if (mom)
            "You've got your crew — ${crew.joinToString(", ")}. " +
                "Good. A kid should have people. Bring them by sometime."
            else "Saw you with your crew — ${crew.joinToString(", ")}. " +
                "That's a solid bunch. I can tell by the laughing.")
        if (Rival.revealed(state)) out.add(
            Rival.scoreLine(state).let { line -> if (mom)
                "This rivalry of yours — $line Your father says it's " +
                    "“character building.” I say stay hydrated."
                else "The rivalry: $line Keep it friendly-ish. " +
                    "Mostly. A little spice is traditional." })
        val ribbons = Fair.blueRibbons(state)
        if (ribbons > 0) out.add(if (mom)
            "Those fair ribbons on the display — $ribbons blue, if I'm " +
                "counting right. I counted right. I'm so proud I could burst."
            else "$ribbons blue ribbons from the fair. I tell people at " +
                "the counter. They pretend not to care. They care.")
        val decor = House.decorFor(state).size
        if (decor >= 6) out.add(if (mom)
            "The house is looking like a home — $decor pieces of proper " +
                "decor, last I counted. It shows, honey."
            else "Counted $decor bits of decor in this house. " +
                "It's coming together. Slowly, like all good houses.")
        if (Darkroom.held(state).any { it.onWall && inPhoto(it.record) })
            out.add(if (mom)
                "That family print on the darkroom wall — I look at it " +
                    "every time I pass. Every single time."
                else "The family photo on the print wall. Good paper. " +
                    "Good people. I'm allowed to say it, it's my wall too.")
        if (Yearbook.volumes(state).isNotEmpty()) out.add(if (mom)
            "The yearbook's on the shelf where it belongs. " +
                "A house should keep its years."
            else "Saw the yearbook shelf. Years, kept. That's a good habit.")
        val caseOpen = state.arcStarted && !state.arcRetired
        if (caseOpen) out.add(if (mom)
            "The whole town's talking about the case at Starks Pond. " +
                "You be careful out there, and you eat before you go " +
                "solving anything."
            else "Town's all stirred up about the pond business. " +
                "Whatever it is, it can wait until after breakfast.")
        return out
    }

    /** The conversation reply: dinner beats are judged in
     *  GameActivity before this runs (Family.judgeTalk). Here:
     *  owed lines, a chore offer when one's due, the day's
     *  fact lines, then the authored topic rotation. Seeded by
     *  the conversation itself so the suite can pin it. */
    fun reply(state: GameState, parent: String, seed: Int): String {
        noteTalk(state, parent)
        settleFetch(state, parent)?.let { return it }
        owedLine(state, parent)?.let { return it }
        choreOffer(state, parent)?.let { return it }
        val facts = factLines(state, parent)
        if (facts.isNotEmpty() && kotlin.math.abs(seed) % 2 == 0) {
            // fact lines rotate (parents DO repeat themselves,
            // but they rotate through what they've noticed)
            val idx = state.life["famFactIdx:$parent"] ?: 0
            state.life["famFactIdx:$parent"] = idx + 1
            return facts[idx % facts.size]
        }
        val topics = TOPICS[parent] ?: emptyList()
        val general = GENERAL[parent] ?: emptyList()
        val idx = kotlin.math.abs(seed)
        return if (idx % 3 == 2 && general.isNotEmpty())
            poolPick(general, idx / 3 + state.absDay)
        else poolPick(topics, idx + state.absDay)
    }

    // ---------------- family dinners ----------------

    /** Tonight is a family-dinner night at YOUR table: the
     *  weekday rhythm, the family arrived — and the table is
     *  not already claimed by a hosted dinner (your guests win
     *  your table; the family eats at the counter, content),
     *  by a holiday beat owning the evening, or by a patrol
     *  night: the {0,2,4} rhythm turns on absDay % 7 while
     *  the patrol cadence turns on the calendar week, and in
     *  the lived calendar the two share Saturday — so on a
     *  patrol night the table stands down and the folks eat
     *  early, because somebody has to walk the park in a
     *  cape. (Found by the v0.10.0g audit: the two rhythms
     *  were pinned disjoint in different weekday spaces and
     *  the claim only held on paper.) Consulted live
     *  all day; it also maintains the day's beat mask, reset
     *  when the day turns (the Dinners.dinnerFor discipline). */
    fun dinnerTonight(state: GameState): Boolean {
        if (!arrived(state)) return false
        if (state.absDay % 7 !in DINNER_WEEKDAYS) return false
        if (Dinners.hostingTonight(state) != null) return false
        if (HolidayBeats.activeBeat(state) != null) return false
        if (Heroes.isPatrolNight(state.absDay)) return false
        if (state.life["famDinDay"] != state.absDay) {
            state.life["famDinDay"] = state.absDay
            state.life["famDinMask"] = 0
            state.lifeText.remove("famDinFirst")
        }
        return true
    }

    /** Was [absDay] a family-dinner night whose table nobody
     *  sat at? For the morning-after record (and Mom's text). */
    fun missedDinnerDay(state: GameState): Int =
        state.life["famDinMissed"] ?: -1

    fun dishOf(absDay: Int): String =
        DISHES[kotlin.math.abs(absDay) % DISHES.size]

    /** Tonight's dish as SERVED (v0.11.0b): if the pantry
     *  holds garden produce, the folks cook THAT — the
     *  homegrown dish names the real item, and judgeTalk
     *  serves (consumes) it when the first table beat
     *  lands. dishOf stays the calendar's own rotation for
     *  every other night. */
    fun dishTonight(state: GameState): String {
        // a dish named once tonight stays named (step 0
        // serves the homegrown item; the table doesn't
        // rename dinner afterward)
        if (state.life["famDinDishDay"] == state.absDay)
            state.lifeText["famDinDish"]?.let { return it }
        val home = Garden.homegrownItem(state)
        return if (home != null)
            "homegrown " + (itemById(home)?.name?.lowercase()
                ?: home) + ", from the garden"
        else dishOf(state.absDay)
    }

    /** Tonight's guest, if any: seeded by the day. Your crew
     *  gets first invitation (they're your people); otherwise
     *  the best-friended school kid in town. Deterministic —
     *  the same name all evening, in the text and at the door. */
    fun guestTonight(state: GameState): String? {
        if (!dinnerTonight(state)) return null
        val rnd = Random(state.absDay * 131L + 17L)
        if (rnd.nextDouble() >= GUEST_CHANCE) return null
        val crew = Crew.members(state)
        if (crew.isNotEmpty()) return crew[rnd.nextInt(crew.size)]
        val friends = CharacterSystem.CAST_KIDS
            .filter { (state.friendship[it] ?: 0) >= 30 }
            .sortedByDescending { state.friendship[it] ?: 0 }
        return friends.firstOrNull()
    }

    data class FamHit(val step: Int, val parent: String,
                      val text: String, val allLanded: Boolean)

    private fun dinBit(idx: Int) = 1 shl idx
    private fun dinMask(state: GameState): Int =
        if (state.life["famDinDay"] == state.absDay)
            state.life["famDinMask"] ?: 0 else 0

    fun dinStepsDone(state: GameState): Int =
        (0..2).count { dinMask(state) and dinBit(it) != 0 }

    /** The family table's beats defer exactly like the cast
     *  tables' do: a case's climax pending owns the evening's
     *  attention (Dinners.beatsDeferred). */
    private fun beatsReady(state: GameState): Boolean =
        dinnerTonight(state) && !Dinners.beatsDeferred(state) &&
            state.timeMin in Dinners.DINNER_FROM until Dinners.DINNER_TO

    /** Talk steps 0-1: step 0 is served by whichever parent you
     *  talk to first; step 1 belongs to the OTHER one (a table
     *  has two ends). Step 2 is the wash-up Look. */
    fun talkStepFor(state: GameState, parent: String): FamHit? {
        if (!beatsReady(state)) return null
        val mask = dinMask(state)
        val dish = dishTonight(state)
        val guest = guestTonight(state)
        if (mask and dinBit(0) == 0) {
            val line = poolPick(SERVE[parent] ?: emptyList(),
                state.absDay).replace("{dish}", dish)
            val g = if (guest != null)
                "\n\n($guest is here too — your folks invited them, " +
                    "and they actually came.)" else ""
            return FamHit(0, parent, line + g, false)
        }
        if (mask and dinBit(1) == 0 &&
            state.lifeText["famDinFirst"] != parent) {
            return FamHit(1, parent,
                poolPick(MOMENTS[parent] ?: emptyList(),
                    state.absDay).replace("{dish}", dish), false)
        }
        return null
    }

    /** Apply a Talk beat (GameActivity calls this before the
     *  general reply, exactly like Dinners.judgeTalk). */
    fun judgeTalk(state: GameState, parent: String): FamHit? {
        val hit = talkStepFor(state, parent) ?: return null
        if (hit.step == 0) {
            state.lifeText["famDinFirst"] = parent
            // v0.11.0b: a homegrown dish is SERVED, not just
            // named — one real produce item leaves the
            // pantry for the table, like any hosted spread;
            // the dish's name is stamped for the evening.
            val tonightDish = dishTonight(state)
            state.life["famDinDishDay"] = state.absDay
            state.lifeText["famDinDish"] = tonightDish
            val home = Garden.homegrownItem(state)
            if (home != null) {
                val held = state.inventory[home] ?: 0
                if (held <= 1) state.inventory.remove(home)
                else state.inventory[home] = held - 1
            }
        }
        state.life["famDinMask"] = dinMask(state) or dinBit(hit.step)
        if (hit.step == 1) {
            // table talk warms the far end of the table too
            adjustPoints(state, if (parent == MOM) DAD else MOM, 1)
        }
        return hit.copy(allLanded = dinStepsDone(state) == 3)
    }

    /** Step 2, the wash-up: Look inside your own house during
     *  the window (a little grace after) with both talks landed. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != YOUR_HOUSE) return null
        if (!dinnerTonight(state) || Dinners.beatsDeferred(state))
            return null
        if (state.timeMin !in Dinners.DINNER_FROM until
            Dinners.DINNER_TO + 60) return null
        val mask = dinMask(state)
        if (mask and dinBit(0) == 0 || mask and dinBit(1) == 0)
            return null
        if (mask and dinBit(2) != 0) return null
        state.life["famDinMask"] = mask or dinBit(2)
        adjustPoints(state, MOM, 1)
        adjustPoints(state, DAD, 1)
        return poolPick(WASHUP, state.absDay)
    }

    /** The wrap facts for the phone: yesterday's table, as
     *  (attended, fullyLanded) — null when yesterday had no
     *  family dinner on the books. */
    fun wrapFor(state: GameState, absDay: Int): Pair<Boolean, Boolean>? {
        if ((state.life["famDinLast"] ?: -1) == absDay)
            return true to ((state.life["famDinFull"] ?: 0) == 1 &&
                (state.life["famDinFullDay"] ?: -1) == absDay)
        if (missedDinnerDay(state) == absDay) return false to false
        return null
    }

    // ---------------- chores ----------------

    /** Tiny optional favors in the errand pattern: an ask in
     *  Talk, a real verb, a judged settle, a warm reward — and
     *  a quiet lapse at midnight if life got in the way. Never
     *  a grind (one at a time, at most one new one a day),
     *  never blocking, never coins (allowance is the only new
     *  coin source in this build, and chores are family, not
     *  wages). FETCH items are pantry staples the town's own
     *  shops sell (sourceOf-pinned in the suite). */
    val FETCH_ITEMS = listOf("lemonade", "hot_chocolate", "snack_cake")
    val CHORE_KINDS = listOf("table", "photo", "fetch")

    fun activeChore(state: GameState): String? =
        state.lifeText["famChore"]?.takeIf { it.isNotEmpty() }

    fun choreFetchItem(state: GameState): String? =
        state.lifeText["famChoreItem"]?.takeIf { it.isNotEmpty() }

    private fun choreOffer(state: GameState, parent: String): String? {
        if (activeChore(state) != null) return null
        if ((state.life["famChoreLastDone"] ?: -100) >= state.absDay)
            return null   // one completed chore a day is plenty
        val (loc, _) = blockAt(state, parent) ?: return null
        if (loc != YOUR_HOUSE) return null   // chores live at home
        val rnd = Random(state.absDay * 977L +
            (if (parent == MOM) 5L else 9L))
        if (rnd.nextDouble() >= 0.5) return null
        val kind = CHORE_KINDS[rnd.nextInt(CHORE_KINDS.size)]
        state.lifeText["famChore"] = kind
        state.lifeText["famChoreBy"] = parent
        state.life["famChoreDay"] = state.absDay
        if (kind == "fetch") {
            val item = FETCH_ITEMS[rnd.nextInt(FETCH_ITEMS.size)]
            state.lifeText["famChoreItem"] = item
            val name = itemById(item)?.name ?: item
            return poolPick(CHORE_ASKS[parent]?.get("fetch") ?: emptyList(),
                state.absDay).replace("{item}", name)
        }
        state.lifeText.remove("famChoreItem")
        return poolPick(CHORE_ASKS[parent]?.get(kind) ?: emptyList(),
            state.absDay)
    }

    private fun completeChore(state: GameState): String {
        val by = state.lifeText["famChoreBy"] ?: MOM
        state.lifeText.remove("famChore")
        state.lifeText.remove("famChoreItem")
        state.lifeText.remove("famChoreBy")
        state.life["famChoreDone"] = (state.life["famChoreDone"] ?: 0) + 1
        // v0.13.0a: who asked is kept per parent from here
        // on (see the header — the transient famChoreBy is
        // cleared below, and the family page's "for whom"
        // has no other honest source)
        state.life["famChoreDoneBy:$by"] =
            (state.life["famChoreDoneBy:$by"] ?: 0) + 1
        state.life["famChoreLastDone"] = state.absDay
        state.life["famChoreDoneDay"] = state.absDay
        adjustPoints(state, by, 3)
        setOwed(state, by, "chore")
        return if (by == MOM)
            "Done and dusted. Thank you, honey — that's how a house works."
        else "Would you look at that. Done right. Thanks, kiddo."
    }

    /** The dining-table fixture tap settles the table chore. */
    fun noteFixture(state: GameState, fixtureId: String): String? {
        if (activeChore(state) == "table" && fixtureId == "table")
            return completeChore(state)
        return null
    }

    /** A photo at home with a parent in frame settles the
     *  family-photo chore (the takePhoto counting trick, like
     *  Climaxes.notePhoto). */
    fun notePhoto(state: GameState, rec: PhotoRecord) {
        if (activeChore(state) != "photo") return
        if (rec.locId == YOUR_HOUSE && inPhoto(rec)) completeChore(state)
    }

    /** Holding the fetched item when you talk to the asker
     *  settles the fetch chore — checked at the top of [reply],
     *  so the hand-over IS the conversation. */
    private fun settleFetch(state: GameState, parent: String): String? {
        if (activeChore(state) != "fetch") return null
        if (state.lifeText["famChoreBy"] != parent) return null
        val item = choreFetchItem(state) ?: return null
        if ((state.inventory[item] ?: 0) <= 0) return null
        state.inventory[item] = (state.inventory[item] ?: 1) - 1
        if ((state.inventory[item] ?: 0) <= 0) state.inventory.remove(item)
        return completeChore(state)
    }

    // ---------------- mornings ----------------

    /** The family's line in House.morningReport (the contributor
     *  pattern — invited dinners, jobs and finales each add
     *  their own the same way). */
    fun morningNote(state: GameState): String? {
        if (arrivalDue(state))
            return "🚚 Big day at YOUR HOUSE: your family's moving in " +
                "today. Head home when you can — they'll be there."
        if (!arrived(state)) return null
        if ((state.life["famAllowanceDay"] ?: -1) == state.absDay)
            return "🪙 Allowance morning: $ALLOWANCE_COINS 🪙 on the " +
                "kitchen counter, under the fruit bowl (Mom's rule: " +
                "dinner seats earn it)."
        if (dinnerTonight(state))
            return "🍽 Family dinner at home tonight: " +
                "${dishOf(state.absDay)} · your table · " +
                Dinners.windowLabel() + "." +
                (guestTonight(state)?.let {
                    " $it is invited — your folks' idea." } ?: "")
        return null
    }

    /** Midnight bookkeeping, called from Engine.onNewDay right
     *  after Dinners.dailyRollover (Engine's order is pinned by
     *  the suite): wrap yesterday's table, lapse a stale chore,
     *  land the weekly allowance, keep the arrival day stamped. */
    fun dailyRollover(state: GameState) {
        if (state.life["famRollDay"] == state.absDay) return
        state.life["famRollDay"] = state.absDay
        arrivalDay(state)   // stamp the day for legacy saves
        val y = state.absDay - 1
        // yesterday's table wraps
        if ((state.life["famDinDay"] ?: -1) == y) {
            val mask = state.life["famDinMask"] ?: 0
            if (mask != 0) {
                state.life["famDinLast"] = y
                if (mask == 0b111) {
                    state.life["famDinFull"] = 1
                    state.life["famDinFullDay"] = y
                    setOwed(state, MOM, "dinner")
                    setOwed(state, DAD, "dinner")
                }
                adjustPoints(state, MOM, 2)
                adjustPoints(state, DAD, 2)
                state.life["famDinCount"] =
                    (state.life["famDinCount"] ?: 0) + 1
            } else if (HolidayBeats.activeBeat(state) == null) {
                state.life["famDinMissed"] = y
            }
        }
        // a stale chore lapses free — no guilt, no record but the lapse
        if (activeChore(state) != null &&
            (state.life["famChoreDay"] ?: state.absDay) < state.absDay) {
            state.lifeText.remove("famChore")
            state.lifeText.remove("famChoreItem")
            state.lifeText.remove("famChoreBy")
        }
        // the allowance: the week's first morning, earned at the table
        if (arrived(state) && state.absDay % 7 == ALLOWANCE_WEEKDAY &&
            (state.life["famAllowanceDay"] ?: -1) != state.absDay) {
            val last = state.life["famDinLast"] ?: -100
            if (last >= state.absDay - 7 && last >= arrivalDay(state)) {
                Minigames.awardCoins(state, ALLOWANCE_COINS)
                state.life["famAllowanceDay"] = state.absDay
                state.life["famAllowanceTotal"] =
                    (state.life["famAllowanceTotal"] ?: 0) + ALLOWANCE_COINS
            }
        }
    }

    // ---------------- the Journal page (v0.13.0a) ----------------

    /** The arrival day WITHOUT the first-consult stamp:
     *  the same value [arrivalDay] derives (the stamped day,
     *  or max(today, the arrival constant)), read purely —
     *  the page renders on a brand-new save without writing
     *  anything into it. */
    fun arrivalDayPreview(state: GameState): Int =
        state.life["famArrivalDay"] ?:
            maxOf(state.absDay, ARRIVAL_ABS_DAY)

    /** [dinnerTonight]'s verdict minus its day-mask
     *  maintenance: the same gates in the same order
     *  (arrived, the {0,2,4} rhythm, no hosted dinner, no
     *  holiday beat owning the evening, no patrol night),
     *  read purely. The verdict is identical — the stamp
     *  inside dinnerTonight happens only after every gate
     *  has passed, so it can never change the answer. */
    private fun dinnerTonightPure(state: GameState): Boolean {
        if (!arrived(state)) return false
        if (state.absDay % 7 !in DINNER_WEEKDAYS) return false
        if (Dinners.hostingTonight(state) != null) return false
        if (HolidayBeats.activeBeat(state) != null) return false
        if (Heroes.isPatrolNight(state.absDay)) return false
        return true
    }

    /** Where a parent is right now, as (location, activity)
     *  or null when they're offstage: the page's pure mirror
     *  of [blockAt]. blockAt itself consults dinnerTonight,
     *  whose mask maintenance writes on a day change — the
     *  page must write nothing, ever, so it reads this twin
     *  instead: blockAt's shape gate for gate, with
     *  [dinnerTonightPure] in the one place blockAt calls
     *  dinnerTonight. The suite sweeps the week's hours and
     *  pins the two agree everywhere. */
    fun whereNow(state: GameState, parent: String,
                 timeMin: Int = state.timeMin): Pair<String, String>? {
        if (!arrived(state)) return null
        val hour = timeMin / 60
        if (isSleepHour(hour)) return null
        val work = workLocOf(parent)
        val dinnerPrep = dinnerTonightPure(state) &&
            timeMin >= Dinners.DINNER_FROM - 90
        if (dinnerPrep && timeMin < Dinners.DINNER_TO)
            return YOUR_HOUSE to (if (parent == MOM)
                "Home — getting family dinner together."
                else "Home — setting the table, mostly correctly.")
        if (isWeekend(state) || state.holidayId != null) {
            if (timeMin in 11 * 60 until 16 * 60) {
                val out = OUTINGS[kotlin.math.abs(
                    state.absDay * 3 + (if (parent == MOM) 0 else 1)) %
                    OUTINGS.size]
                return out to (if (parent == MOM)
                    "Out — errands and fresh air."
                    else "Out — a walk he calls 'checking on the town.'")
            }
            return YOUR_HOUSE to "Home — a slow family day."
        }
        return when {
            timeMin < 9 * 60 -> YOUR_HOUSE to
                "Home — morning, coffee, the day getting organized."
            timeMin < 17 * 60 -> work to (if (parent == MOM)
                "At the Bank — the new-accounts desk."
                else "At the Post Office — the counter.")
            else -> YOUR_HOUSE to "Home — evening, off the clock."
        }
    }

    /** A warmth standing in plain words — presentation only,
     *  derived from the same points the talk sheet warms. */
    private fun warmthWord(pts: Int): String = when {
        pts >= 60 -> "close as can be"
        pts >= 30 -> "warm, and getting warmer"
        pts >= 10 -> "settling in nicely"
        pts >= 0 -> "still new, in the good way"
        else -> "a rough patch — dinners help"
    }

    /** The 👨‍👩‍👧 YOUR FAMILY page's lines — pure assembly;
     *  the Journal renders. Line 0 is the header. Everything
     *  on it is derived from the family's real records on
     *  every read: the arrival day, the stance, the warmth
     *  lanes, the dinner count and its most recent night,
     *  the allowance book, the chore tally, the frame (read
     *  through the same functions the attic reads, so the
     *  two pages can never disagree about what's in it).
     *  It never reads state.friendship — a parent is not a
     *  townsfolk, and the page keeps the wall the talk
     *  sheet keeps. Before the arrival it is an honest
     *  not-yet page: they're coming, and the public shape
     *  of what's coming (dinners, the rule) is all it says. */
    fun pageLines(state: GameState): List<String> {
        val lines = mutableListOf<String>()
        lines += "👨‍👩‍👧 YOUR FAMILY"
        if (!arrived(state)) {
            lines += "Mom and Dad don't live here yet — for " +
                "now the house is yours alone, and it sounds " +
                "like it."
            lines += "They're moving in on Day " +
                "${arrivalDayPreview(state) + 1}. After that: " +
                "family dinners at your table three nights a " +
                "week, chores that are really just love with " +
                "a clipboard, and Mom's allowance rule — show " +
                "up for dinner, and Monday provides."
            lines += "When the truck comes, this page keeps " +
                "the family's record: the dinners, the " +
                "allowance, the chores, the frame on the wall."
            return lines
        }
        lines += "Mom and Dad — your folks, under this roof " +
            "since Day ${arrivalDayPreview(state) + 1}."
        val st = stance(state)
        if (st.isNotEmpty())
            lines += "How the move landed — $st. Mom, on it: " +
                "“${stanceEchoLine(MOM, st)}”"
        for (p in PARENTS) {
            val now = whereNow(state, p)
            lines += "$p — " + (now?.second ?: (if (p == MOM)
                "asleep — the house is quiet, and so is she."
                else "asleep — the house is quiet, and so is he."))
            val pts = points(state, p)
            lines += "   💛 warmth $pts — ${warmthWord(pts)}"
            lines += "   💼 $p works ${persona(state, p).role} " +
                "— weekdays, nine to five."
        }
        // the table's record
        val dinCount = state.life["famDinCount"] ?: 0
        val lastDin = state.life["famDinLast"] ?: -1
        val missed = missedDinnerDay(state)
        lines += "🍽 Family dinners: $dinCount sat at your table."
        when {
            lastDin < 0 && missed < 0 ->
                lines += "   None yet — three nights a week " +
                    "the table is set and nobody has to be " +
                    "invited. It's your table."
            missed > lastDin ->
                lines += "   The most recent table night " +
                    "(Day ${missed + 1}) went unsat — life " +
                    "happens; the calendar just notes it."
            else -> {
                var l = "   Most recent: Day ${lastDin + 1}"
                if ((state.life["famDinFullDay"] ?: -1) == lastDin)
                    l += " — the full table, first bite to " +
                        "wash-up."
                lines += l
            }
        }
        if (dinnerTonightPure(state))
            lines += "   Tonight's a family-dinner night: " +
                "${dishTonight(state)}."
        // Mom's book
        val aTotal = state.life["famAllowanceTotal"] ?: 0
        val aDay = state.life["famAllowanceDay"] ?: -1
        lines += "🪙 Allowance: $aTotal 🪙 earned all told" +
            (if (aDay >= 0) ", most recently on Day ${aDay + 1}."
             else ".")
        lines += "   Mom's rule, famous in exactly one house: " +
            "a family dinner within the week earns " +
            "$ALLOWANCE_COINS 🪙 on the week's first morning, " +
            "under the fruit bowl."
        // the chores, with their askers
        val done = state.life["famChoreDone"] ?: 0
        val byMom = state.life["famChoreDoneBy:$MOM"] ?: 0
        val byDad = state.life["famChoreDoneBy:$DAD"] ?: 0
        val named = byMom + byDad
        val lastDone = state.life["famChoreLastDone"] ?: -1
        lines += "🧹 Chores pitched in: $done" +
            (if (lastDone >= 0)
                " — most recently on Day ${lastDone + 1}." else ".")
        if (done > 0 && named > 0)
            lines += "   Asked by Mom: $byMom · asked by " +
                "Dad: $byDad" + (if (named < done)
                    " · ${done - named} more from before this " +
                        "page kept the askers' names — the " +
                        "work counted just the same." else ".")
        else if (done > 0)
            lines += "   Those were all from before this page " +
                "kept the askers' names — the work counted " +
                "just the same."
        activeChore(state)?.let { kind ->
            val by = state.lifeText["famChoreBy"] ?: MOM
            lines += "   Open right now: a $kind chore from " +
                "$by (asked on Day " +
                "${(state.life["famChoreDay"] ?: state.absDay) + 1})."
        }
        // the frame — the attic reads the same functions,
        // so the two pages agree by construction
        val framed = latestFamilyPhoto(state)
        if (framed == null) {
            lines += "🖼 The family frame: still empty. A " +
                "photo taken at home with Mom or Dad in it " +
                "hangs here — the newest one does."
        } else {
            val albumCount = Attic.familyPhotos(state).size
            lines += "🖼 The family frame: " +
                "${Photos.captionFor(framed)} " +
                "(${framed.dateLabel}) — the newest of the " +
                "$albumCount in the album."
            val outs = Attic.outtakes(state).size
            if (outs > 0)
                lines += "   The other $outs " +
                    (if (outs == 1) "hangs" else "hang") +
                    " in the attic as outtakes — nothing's " +
                    "lost, just upstairs."
        }
        lines += "They hold no faction, join no crew, and " +
            "enter no contests. They're not townsfolk. " +
            "They're family."
        return lines
    }

    // ---------------- home barks (v0.13.0a) ----------------

    /** How likely a lingering moment at home draws an idle
     *  bark from a parent who is present, per decide call
     *  (GameActivity consults on its refresh rhythm). Low on
     *  purpose: a house murmurs, it doesn't narrate. Named
     *  here and in the docs/TUNING.md ledger beside the cast's own
     *  ARRIVAL_CHANCE. */
    const val HOME_IDLE_CHANCE = 0.08

    /** The line a home bark speaks: [idle] picks the
     *  register (greeting vs lingering), the seed picks the
     *  line — deterministic in the seed, like every seeded
     *  bank in the family. Falls back to the pattern hello. */
    fun homeBarkLine(parent: String, idle: Boolean, seed: Int): String {
        val bank = (if (idle) HOME_IDLE else HOME_GREETINGS)[parent]
            ?: return Barks.HOUSEHOLD_PATTERNS[parent]?.hello ?: "…"
        return poolPick(bank, seed)
    }

    /** The whole home-bark decision, pure — Barks.decide's
     *  discipline, at the family's scale: the master gate
     *  (settings + quiet hours) first; an ENTRY into the
     *  house offers every parent present a greeting; a
     *  parent who newly appears while you linger offers
     *  one on the cast's arrival chance; otherwise, when
     *  nobody is new, a lingering moment can draw an idle
     *  bark at HOME_IDLE_CHANCE. One pick, weighted by
     *  nothing but the pick roll (parents don't compete on
     *  a friendship lane — there isn't one), and the shared
     *  bark cooldown vetoes the pick without re-rolling.
     *  Flavor only: this reads records and writes nothing —
     *  no warmth, no coins, no state. Returns the parent
     *  and the line, or null for silence. */
    fun barkDecide(state: GameState, enabled: Boolean,
                   present: List<String>, entered: Boolean,
                   newcomers: Set<String>,
                   cooldowns: Map<String, Int>, nowAbsMin: Int,
                   arrivalRoll: Double, idleRoll: Double,
                   pickRoll: Double, lineSeed: Int): Pair<String, String>? {
        if (!arrived(state)) return null
        if (!Barks.gateOpen(enabled, state.timeMin)) return null
        val here = present.filter { it in PARENTS }
        if (here.isEmpty()) return null
        val idle: Boolean
        val candidates: List<String> = when {
            entered -> { idle = false; here }
            newcomers.any { it in here } && arrivalRoll < Barks.ARRIVAL_CHANCE -> {
                idle = false; here.filter { it in newcomers } }
            newcomers.isEmpty() && idleRoll < HOME_IDLE_CHANCE -> {
                idle = true; here }
            else -> return null
        }
        if (candidates.isEmpty()) return null
        val pick = candidates[(pickRoll.coerceIn(0.0, 0.999999) *
            candidates.size).toInt()]
        if (!Barks.cooledDown(cooldowns[pick], nowAbsMin)) return null
        return pick to homeBarkLine(pick, idle,
            lineSeed + state.absDay * 3 + (if (pick == MOM) 0 else 1))
    }

    // ---------------- the banks ----------------
    // Authored, per parent, in the cast's voice discipline.
    // Per parent: 5 morning greetings, 4 evening greetings,
    // 8 rotating topics, 6 general lines, 3 ambient, 3 byes,
    // 3 dinner serves, 3 table moments, 6 chore asks — plus
    // the shared dishes/washup/fact banks. 41 lines each.

    val DISHES = listOf(
        "meatloaf", "baked chicken", "spaghetti night",
        "pork chops", "vegetable soup", "taco night (a bold import)",
        "breakfast-for-dinner")

    private val FACTORY_LINES = listOf(
        "Family dinners at your house run on the odd rhythm: " +
            "three nights a week, your table, no invitations needed.",
        "Mom's allowance rule is famous in exactly one house: " +
            "show up for dinner, and Monday provides.")

    private val GREETINGS: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Morning, honey. There's coffee — well, there's coffee " +
                "for me and cocoa for you, don't get ideas.",
            "Well, look who's up. Sit. Food first, heroics after.",
            "Morning! I already know how you slept by the hair. " +
                "It was 'adventurously,' wasn't it.",
            "Hi, sweetheart. Your father's outside being handy at " +
                "something. Eat before he recruits you.",
            "Good morning. New day, same house — I still love " +
                "saying that."),
        DAD to listOf(
            "Morning, kiddo. Coffee's on. Yours is the one that's " +
                "mostly cocoa, before your mother corrects me.",
            "Up and at 'em. I fixed the porch step. It wasn't broken. " +
                "It's EXTRA not-broken now.",
            "Morning. Weather report from the porch: it's a day. " +
                "Make it a good one.",
            "Hey, champ. Slept? Ate? Then you're ahead of half " +
                "this town already.",
            "Good morning. Your mother made a list. It's a good " +
                "list. We're all going to respect the list."))

    private val EVENINGS: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Evening, honey. Long day? They all are, at your age. " +
                "That's not a bad thing.",
            "There's my kid. Home in one piece — my favorite " +
                "kind of home.",
            "Evening! Kettle's on. Tell me one thing about today, " +
                "any one thing.",
            "Welcome home. Yes, home. I keep practicing the word " +
                "so it sounds normal. It's getting there."),
        DAD to listOf(
            "Evening. Tools down, kid home. Good ordering of events.",
            "Hey. You look like a person who did Things today. " +
                "Capital T.",
            "Evening, champ. Your mother's making the good smells " +
                "again. We live like kings.",
            "There's the kid. Town still standing? Then you did " +
                "your part."))

    private val TOPICS: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Work was work. A man opened an account, cried a little — " +
                "happy tears, new start. I keep tissues in the drawer " +
                "for exactly that. Banking is people, honey.",
            "I alphabetized the pantry. Your father says that's not " +
                "a hobby. Your father is wrong and can file it under P.",
            "The neighbors asked about you. I said you're thriving. " +
                "It's not bragging if it's reconnaissance.",
            "I called my sister. She says hi and also that you owe " +
                "her a drawing. Her fridge has a spot saved. No pressure — " +
                "some pressure.",
            "School behaving itself? You'd tell me anyway. Eventually. " +
                "Through a drawing, a rumor, and a note on the fridge.",
            "I've been thinking about a garden. Small. Tomatoes, " +
                "herbs, one ambitious pumpkin. A family project, " +
                "when we get there.",
            "You know what I like about this town? It notices people. " +
                "Loudly, constantly, at top volume — but it notices.",
            "If you ever need money for something GOOD — a book, a " +
                "trip, a friend in a fix — you come to me first. " +
                "That's what the desk at the bank is FOR, spiritually."),
        DAD to listOf(
            "Man at the counter today tried to mail a live goldfish. " +
                "In an envelope. Anyway, the Post Office has seen " +
                "everything, and now so have I.",
            "I've been measuring the hallway for a shelf. Your mother " +
                "says we don't need a shelf. The hallway disagrees, " +
                "in measurements.",
            "Root for whoever you want in this town's contests, but " +
                "root LOUD. Half-hearted cheering is how teams get " +
                "haunted. Probably.",
            "Your grandfather fixed things with wire and confidence. " +
                "I use actual screws. We evolve, as a family.",
            "I like your school. Good bones. A building with good " +
                "bones can survive a lot of fourth graders.",
            "If a friend of yours ever needs a meal and a quiet " +
                "couch, this house has both. That's a standing offer, " +
                "not a one-time thing.",
            "People ask me at the counter, 'how's the kid?' I say: " +
                "'busy.' Then I smile like a man who knows things.",
            "Winter's coming eventually. I've checked the windows, " +
                "the pipes, the whole situation. We're ready. " +
                "We're the ready house."))

    private val GENERAL: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Dishes are a team sport and I am a benevolent coach.",
            "I folded your laundry. There were sticks in a pocket. " +
                "Sticks, plural, categorized by size. No questions.",
            "The house makes sounds at night. I've decided they're " +
                "friendly sounds. We've agreed to coexist.",
            "Eat a vegetable today. Any vegetable. It can be an " +
                "adventure vegetable if that helps.",
            "I love it here. There, I said it out loud. The house " +
                "heard. Your move, house.",
            "If your friends are hungry, the fridge is not a riddle. " +
                "Open, take, tell me after."),
        DAD to listOf(
            "I have strong opinions about soup now. This is what " +
                "settling down does to a man.",
            "Found a screw in the couch. It's from something. " +
                "Everything is from something. I'll find it.",
            "Your mother laughed at my joke at breakfast. The day " +
                "is charmed. Go do something worthy of it.",
            "A house is just a toolbox you live in. We're well " +
                "stocked.",
            "I keep a list of the town's dogs on the fridge. For " +
                "greeting purposes. A man has standards.",
            "Porch sitting is a skill and I am gifted."))

    private val AMBIENT: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "…no, the good towels are for guests, the GOOD good " +
                "towels are theoretical…",
            "…if I move the fern two inches, the whole room breathes…",
            "…list, list, who's got the list…"),
        DAD to listOf(
            "…measure twice, cut once, tell nobody about the " +
                "third board…",
            "…that shelf is load-bearing in spirit…",
            "…hm, the porch gets the good light at four…"))

    private val BYES: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Go on, then. The town's waiting. So's dinner, later.",
            "Bye, honey. Shoulders back. You're somebody's " +
                "favorite person, you know. Mine, for one.",
            "Off you go. If it's boring, come home. If it's " +
                "wonderful, come home and tell me."),
        DAD to listOf(
            "See you, kiddo. Kick the day's tires for me.",
            "Go get 'em. And by 'em I mean a reasonable, " +
                "well-hydrated good time.",
            "Later, champ. The porch and I will hold the fort."))

    private val SERVE: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Dinner's on: {dish}. Sit, sit. In this house we eat " +
                "at the table like people in a painting.",
            "Come and get it — {dish}, hot and entirely judgment-free. " +
                "Well. Mostly judgment-free.",
            "{dish} tonight, made with love and a recipe I pretend " +
                "I invented. Chairs in, elbows reasonable."),
        DAD to listOf(
            "Table's set — mostly correctly, forks on the side forks " +
                "go. Tonight: {dish}. Your mother's triumph, my " +
                "logistics.",
            "{dish}, hot. I carried it the whole six steps from the " +
                "stove. You're welcome, family.",
            "Dinner! {dish}. I have reviewed the table and found " +
                "it adequate. High praise."))

    private val MOMENTS: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "One good thing about today, everybody. Mine: a lady at " +
                "the bank told me her grandson got the job. Stranger's " +
                "luck is still luck. Pass the {dish}.",
            "When I was your age, dinner was where the day got " +
                "explained. We're explaining. This is explaining.",
            "Look at us. A table, a meal, the whole family inside " +
                "one set of walls. People write songs about less."),
        DAD to listOf(
            "Report from the counter: the town's mail says everybody's " +
                "fine. Postcards don't lie. Much.",
            "I propose a toast — to {dish}, to this table, to the " +
                "kid, who remains my favorite coworker in the " +
                "business of this house.",
            "Someday you'll tell people about dinners like this and " +
                "they'll think you're exaggerating. Let them."))

    private val WASHUP = listOf(
        "🧽 You wash up after family dinner — a sink full of warm " +
            "water and other people's opinions about your scrubbing. " +
            "The table's clear. The house feels lived in.",
        "🧽 Dish duty, family style: your dad dries, your mom finds " +
            "the three spots you missed, and the kitchen ends the " +
            "night clean.",
        "🧽 You stack the last plate. Family dinner, start to " +
            "finish, done properly. The house approves.")

    private val CHORE_ASKS: Map<String, Map<String, List<String>>> = mapOf(
        MOM to mapOf(
            "table" to listOf(
                "Small favor, honey? Set the table for me today — " +
                    "the dining table, tap it when it's done. " +
                    "Forks on the left. You know where the left is.",
                "Could you set the table sometime today? Tap the " +
                    "dining table when it's laid. It makes the whole " +
                    "house feel ahead of schedule."),
            "photo" to listOf(
                "Family favor: I want a photo of us — here, at home, " +
                    "with your camera thing. Take one with me or your " +
                    "father in it, would you? The frame's been empty " +
                    "too long.",
                "Take a family photo today, honey — at home, with " +
                    "one of us in the frame. I have a spot picked out " +
                    "and everything."),
            "fetch" to listOf(
                "Errand, if you're heading out: could you pick up " +
                    "{item} from town? Bring it back to me. " +
                    "A mother's infrastructure thanks you.",
                "If you're passing a shop, honey — {item}, please. " +
                    "Bring it home and I'll call it even.")),
        DAD to mapOf(
            "table" to listOf(
                "Chore draft, kiddo: set the table today. Tap the " +
                    "dining table when it's laid. Your mother notices " +
                    "these things. So do I, loudly.",
                "Set the table for dinner, would you? Tap it when " +
                    "it's done. A set table is a promise."),
            "photo" to listOf(
                "Family photo, today, at home — that's my ask. Get " +
                    "one of us in the frame with you. A house should " +
                    "have evidence.",
                "Take a picture of the family today, champ. At home. " +
                    "I'll even stand still, which is my whole range " +
                    "as a model."),
            "fetch" to listOf(
                "Supply run, if you're out: {item}, from any shop " +
                    "in town. Bring it back here. The house runs " +
                    "on such missions.",
                "Could you grab {item} while you're in town? " +
                    "Hand it to me at home. Simple logistics, " +
                    "family-grade.")))

    // v0.13.0a: the home-bark banks — the bark register is
    // shorter and warmer than conversation: a hello across
    // the room, a murmur from the couch. Home, meals, the
    // weather, your day — the parent's whole beat. The first
    // line of each greeting bank IS the parent's bark
    // pattern hello (Barks.HOUSEHOLD_PATTERNS), so the
    // bubble and the bank can never drift apart.
    val HOME_GREETINGS: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Well, look who's home.",
            "There's my kid. Shoes off or don't — I've given " +
                "up on shoes.",
            "Home! Good. The house was doing a quiet " +
                "impression of itself.",
            "Hey, honey. Kettle's on, if the day needs a kettle.",
            "Back before dark, even. A mother logs these things."),
        DAD to listOf(
            "Hey — the kid's home.",
            "There you are. I was just telling the porch " +
                "about you.",
            "Home base. Good. The couch kept your spot warm-ish.",
            "Hey, champ. Day treat you alright out there?",
            "Well. The whole family in one building. Rare footage."))

    val HOME_IDLE: Map<String, List<String>> = mapOf(
        MOM to listOf(
            "Dinner thinking has begun. No decisions, " +
                "just thinking.",
            "I can hear the town from here. It sounds busy " +
                "and mostly harmless.",
            "Rain later, they said. The windows and I are " +
                "prepared either way.",
            "Your father's fixing something. I've made peace " +
                "with the sound.",
            "A house is quieter with people in it. Explain " +
                "that. You can't."),
        DAD to listOf(
            "Measured the hallway again. Still hallway-sized. Good.",
            "Soup opinions update: I'm pro. Strongly pro.",
            "The porch gets the good light right about now. " +
                "Filed that away.",
            "If anyone asks, the shelf was always level.",
            "Quiet house, full fridge. We live correctly."))

    // bank sizes, pinned by the suite so the banks stay real
    fun bankSizes(parent: String): Map<String, Int> = mapOf(
        "greetings" to (GREETINGS[parent]?.size ?: 0),
        "evenings" to (EVENINGS[parent]?.size ?: 0),
        "topics" to (TOPICS[parent]?.size ?: 0),
        "general" to (GENERAL[parent]?.size ?: 0),
        "ambient" to (AMBIENT[parent]?.size ?: 0),
        "byes" to (BYES[parent]?.size ?: 0),
        "serve" to (SERVE[parent]?.size ?: 0),
        "moments" to (MOMENTS[parent]?.size ?: 0),
        "choreAsks" to (CHORE_ASKS[parent]?.values?.sumOf { it.size } ?: 0),
        "homeGreetings" to (HOME_GREETINGS[parent]?.size ?: 0),
        "homeIdle" to (HOME_IDLE[parent]?.size ?: 0))
}

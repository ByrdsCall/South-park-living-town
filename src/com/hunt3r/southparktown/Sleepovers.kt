package com.hunt3r.southparktown

import kotlin.random.Random

/**
 * v0.11.0c: SLEEPOVERS — the crew, sleeping over at your
 * house. Queue #11 item 3.
 *
 * The model, stated plain:
 *  - THE NIGHT: sleepovers are a FRIDAY-night institution
 *    (Jobs.weekdayOf == 5), so at most one a week exists by
 *    construction. The date arithmetic of the rival claims
 *    was proven in ONE time space (the polish-#10 lesson):
 *    patrol nights are Wed/Sat, the family table's rhythm
 *    is absDay % 7 in {0,2,4} and a Friday is always
 *    absDay % 7 == 1, cast dinners rotate on {1,3,5} with
 *    hosted tables on the player's own say-so — the suite
 *    sweeps the calendar proving no two of these cadences
 *    ever claim the same evening.
 *  - THE GUESTS: crew members who are KIDS (kids and young
 *    teens, CharacterSystem's bands). Grown-ups don't do
 *    sleepovers — stated plainly in the plan; an adult crew
 *    member is simply never offered, and their crew
 *    membership is untouched. Every guest's attendance
 *    runs the FULL occupied discipline (Crew.busyReason
 *    read on the evening): a guest claimed by a screening,
 *    a shift, a ceremony, a finale, a case or another
 *    table doesn't come, and the plan NAMES them and the
 *    reason — nobody is silently swapped. Below MIN_GUESTS
 *    free guests the night proposes a different Friday
 *    instead of running empty.
 *  - THE EVENING: guests are claimed to Your House
 *    18:00–22:00 by the agenda stack (the claim seats after
 *    the play, before the stand, self-guarded by every
 *    bigger claim), and Crew.busyReason reports "sleepover"
 *    for a claimed guest, so the whole town's occupied
 *    discipline sees the night. Four beats in the dinners'
 *    discipline — SNACKS (Talk; the real pantry, consumed
 *    and named, Mom's stated fallback when the family is
 *    home and the cupboard is bare), THE MOVIE (Look; the
 *    Bijou's own feature when a screening stands, else a
 *    home movie described from one of YOUR album records'
 *    own facts), the PILLOW FIGHT (Talk into the played
 *    view — honest input on the showdown bar against the
 *    guest you tapped), LIGHTS OUT (Talk; arrangements and
 *    any early departure resolved in the telling).
 *  - THE MORNING: the rollover wraps the night — friendship
 *    once per guest at the cheap stated rates (+2 a full
 *    night, +1 a partial one, nothing for a night that
 *    never got going), a "sleepover" topic planted in every
 *    guest who came (Threads follow-ups carry it), the
 *    night written to the record under a cap of 8, and if
 *    the family is home, Mom makes pancakes — her treat,
 *    consuming nothing, stated as such.
 *  - THE PHOTO: no special machinery — the guests are
 *    physically at your house all evening, so the photo
 *    system's own composition seats them; the wrap reads
 *    the album for a real group frame from the night and
 *    the record says so. (v0.12.0d: the sleepwear pass
 *    now exists — Sleepwear.kt; a frame composed here on
 *    a live night freezes the pajamas onto its sleepover
 *    figures, this module's plan being the only key.)
 *
 * All state rides life/lifeText (slp* keys); a save from
 * before this build simply never hosted one. The writing
 * never touches any mystery's sealed ground — a home
 * movie describes a photo by its record's public facts
 * (place, date, who was in frame) and nothing else.
 */
object Sleepovers {

    // ---------------- dials ----------------

    /** The sleepover weekday (Jobs.weekdayOf: 0 = Sunday
     *  … 6 = Saturday). Friday — never a patrol night
     *  (Wed/Sat), never a family-table night (a Friday is
     *  always absDay % 7 == 1; the table runs {0,2,4}). */
    const val SLEEPOVER_WEEKDAY = 5

    /** Guests are claimed to the house at 18:00 … */
    const val ARRIVE_FROM = 18 * 60

    /** … and lights-out is 22:00, when the town's own
     *  sleep hours take everyone anyway. */
    const val LIGHTS_OUT = 22 * 60

    /** A sleepover needs at least this many free guests,
     *  or the plan proposes a different Friday. */
    const val MIN_GUESTS = 2

    /** The evening's beats: 0 snacks, 1 the movie,
     *  2 the pillow fight, 3 lights out. */
    const val STEP_COUNT = 4

    /** Friendship per guest who came: a full night (all
     *  four beats) pays this … */
    const val PAY_FULL = 2

    /** … a partial night (at least PARTIAL_MIN beats)
     *  pays this, and a night that never got going pays
     *  nothing. Arrival-style rates, paid once, at the
     *  wrap — never per beat. */
    const val PAY_PARTIAL = 1
    const val PARTIAL_MIN = 2

    /** The record keeps this many nights. */
    const val HISTORY_CAP = 8

    /** The topic planted in every guest who came. */
    const val TOPIC = "sleepover"

    /** The beats' names, for the dialogs and the page. */
    val STEP_TITLES = listOf("Snacks", "The Movie",
        "The Pillow Fight", "Lights Out")

    // ---------------- state keys ----------------

    private const val K_DAY = "slpDay"          // life: absDay planned
    private const val K_GUESTS = "slpGuests"    // lifeText: csv, planned
    private const val K_MASK = "slpMask"        // life: beats landed
    private const val K_ATT = "slpAtt"          // lifeText: csv, seen here
    private const val K_SNACKS = "slpSnacks"    // lifeText: csv item ids
    private const val K_MOVIE = "slpMovie"      // lifeText: the movie record
    private const val K_FIGHT = "slpFight"      // lifeText: the fight record
    private const val K_LABEL = "slpLabel"      // lifeText: "M/D" of the night
    private const val K_WRAP = "slpWrapDay"     // life: last wrapped night
    private const val K_DAYS = "slpNightDays"   // lifeText: csv absDays
    private const val K_NIGHT = "slpNight:"     // lifeText per-night record

    // ---------------- plan ----------------

    /** One crew member's line in the plan. [ok] guests are
     *  free to come; a refused guest carries the honest
     *  [reason], named in the plan, never swapped away. */
    data class GuestPlan(val name: String, val ok: Boolean,
                         val reason: String)

    /** The whole proposal, read off real state. [nightOk]
     *  is the town-level verdict (cadence, deference, the
     *  player's own evening); [nightReason] says why not
     *  when it isn't. [notGuests] are the crew's grown-ups,
     *  with the plain rule attached. */
    data class Plan(
        val nightOk: Boolean,
        val nightReason: String,
        val guests: List<GuestPlan>,
        val notGuests: List<GuestPlan>,
        /** The family whose table has invited YOU tonight,
         *  when one has — named in the plan, never a block
         *  (the hosted table's precedent: your evening is
         *  yours to spend at home). */
        val inviteFamily: String? = null
    ) {
        val okGuests: List<String> get() =
            guests.filter { it.ok }.map { it.name }
        val canPropose: Boolean get() =
            nightOk && okGuests.size >= MIN_GUESTS
    }

    /** Is [absDay] a sleepover night at all? The cadence
     *  is a pure function of the day — the suite sweeps it
     *  against every rival cadence in the same space. */
    fun isSleepoverDay(absDay: Int): Boolean =
        Jobs.weekdayOf(absDay) == SLEEPOVER_WEEKDAY

    /** The next sleepover Friday on or after [fromDay]. */
    fun nextSleepoverDay(fromDay: Int): Int {
        var d = fromDay
        while (!isSleepoverDay(d)) d++
        return d
    }

    /** Kid bands sleep over; grown-ups don't. */
    fun isKidBand(name: String): Boolean =
        CharacterSystem.bandOf(name).let {
            it == AgeBand.KID || it == AgeBand.YOUNG_TEEN
        }

    /** The plan for tonight, exactly as the dialog shows
     *  it. Pure: reads state, changes nothing. */
    fun planFor(state: GameState): Plan {
        val members = Crew.members(state)
        val kids = members.filter { isKidBand(it) }
        val grown = members.filter { !isKidBand(it) }
            .map { GuestPlan(it, false, "grown-ups don't do sleepovers") }
        // already planned: the planned guests read as coming
        if (plannedTonight(state)) {
            val planned = plannedGuests(state)
            val guests = kids.map { name ->
                if (name in planned) GuestPlan(name, true, "coming — it's planned")
                else GuestPlan(name, false,
                    planRefusal(state, name) ?: "the plan's already set")
            }
            return Plan(false,
                "Tonight's sleepover is already planned — " +
                    "${planned.joinToString(", ")} at your house, " +
                    "6 PM to lights-out.",
                guests, grown,
                Dinners.todaysInvite(state)?.family)
        }
        val nightReason = nightBlock(state)
        val guests = kids.map { name ->
            val r = planRefusal(state, name)
            if (r == null) GuestPlan(name, true, "free Friday night")
            else GuestPlan(name, false, r)
        }
        val ok = guests.count { it.ok }
        val reason = when {
            nightReason != null -> nightReason
            members.isEmpty() ->
                "No crew yet — a sleepover needs your crew. " +
                    "Win some friends first (the Journal's " +
                    "👥 YOUR CREW page explains)."
            kids.isEmpty() ->
                "Your whole crew is grown-ups, and grown-ups " +
                    "don't do sleepovers. A kid or two in the " +
                    "crew and this is a plan."
            ok < MIN_GUESTS ->
                "Only $ok of your crew can make it — a " +
                    "sleepover needs at least $MIN_GUESTS. " +
                    "Try next Friday."
            else -> ""
        }
        return Plan(reason.isEmpty(), reason, guests, grown,
            Dinners.todaysInvite(state)?.family)
    }

    /** The town-level blocks on tonight, or null when the
     *  night itself is free. The deference set, in the
     *  dinners' own spirit: a finale window, a holiday
     *  beat, a case coming to a head, opening night, the
     *  fair — any of them owns the evening outright. */
    fun nightBlock(state: GameState): String? {
        if (!isSleepoverDay(state.absDay))
            return "Sleepovers are a Friday-night institution " +
                "here — tonight isn't one. Next Friday: " +
                dateLabelOf(nextSleepoverDay(state.absDay + 1)) + "."
        if (Finales.windowAt(state) != null)
            return "A season finale owns this week — the " +
                "whole town's holding its breath. The " +
                "sleepover waits."
        if (HolidayBeats.activeBeat(state) != null)
            return "A holiday beat owns tonight. The " +
                "sleepover waits for an ordinary Friday."
        if (MovieNights.anyClimaxDueTonight(state))
            return "A case comes to a head tonight — nobody " +
                "in this town is sleeping over anywhere. " +
                "Another Friday."
        if (Play.nightCanHappen(state))
            return "It's opening night at the Elementary — " +
                "the whole town's spoken for. Another Friday."
        if (Fair.windowToday(state) != null)
            return "The county fair's in town — Friday " +
                "belongs to the fairground. Another Friday."
        if (Dinners.hostingTonight(state) != null)
            return "You're hosting dinner at your own table " +
                "tonight — one set piece a night is the " +
                "house rule."
        // An invitation to someone else's table is NOT a
        // block: every Friday is a rotation day, so refusing
        // on an invite would refuse every sleepover ever —
        // and the town already settled this question at the
        // hosted table, where an outstanding invitation to
        // another house never bars hosting your own night.
        // The invite is the player's own choice to decline;
        // the plan NAMES it (Plan.inviteFamily) rather than
        // hiding it, and the inviting household's own
        // members are claimed at their table by the real
        // discipline and refused by name below.
        if (Family.dinnerTonight(state))
            return "It's family dinner night at your house " +
                "— the sleepover waits a week."
        // v0.12.0c: a birthday party standing tonight at
        // one of the town's tables owns the evening the
        // same way a hosted dinner does — one set piece a
        // night.
        Birthdays.partyTonight(state)?.let { p ->
            return "There's a birthday party tonight — " +
                "${p.person}'s, at the " +
                "${p.household.family}'s. One set piece a " +
                "night is the house rule."
        }
        return null
    }

    /** Why this one guest can't come tonight, or null when
     *  they're free. The full occupied discipline, read on
     *  the evening itself (the state copy at 19:00 trick
     *  MovieNights' climax scan uses), plus the two claims
     *  busyReason doesn't know: your family's own guest and
     *  your stand's shift. */
    fun planRefusal(state: GameState, name: String): String? {
        if (Family.guestTonight(state) == name)
            return "coming to your family's dinner instead"
        if (Stand.assignedStaff(state) == name)
            return "work — on your stand's shift tonight"
        val ev = state.copy(timeMin = 19 * 60)
        Crew.busyReason(ev, name)?.let { return planWords(it) }
        if (Play.gatheringBlock(ev, name, 19 * 60) != null)
            return "the play has them tonight"
        // v0.13.0b: the dance's claim is the other one
        // busyReason doesn't know — a kid the gym holds
        // that Friday is named, not double-booked.
        if (Dance.gatheringBlock(ev, name, 19 * 60) != null)
            return "the school dance has them tonight"
        return null
    }

    /** busyReason's codes, in plan language. */
    private fun planWords(code: String): String = when (code) {
        "dinner" -> "spoken for — dinner plans tonight"
        "work" -> "work — a shift tonight"
        "movie" -> "at the Bijou tonight"
        "ceremony" -> "a faction ceremony tonight"
        "finale" -> "finale business tonight"
        "event" -> "case business tonight"
        "emergency" -> "an emergency has their name on it"
        "asleep" -> "already asleep"
        "gone" -> "not around tonight"
        "play" -> "the play has them tonight"
        else -> "busy tonight"
    }

    /** Lock the plan in: tonight, these guests, 6 PM.
     *  Refuses (false) unless the plan says it can happen —
     *  the dialog and the suite ride the same gate. */
    fun propose(state: GameState): Boolean {
        val plan = planFor(state)
        if (!plan.canPropose) return false
        state.life[K_DAY] = state.absDay
        state.lifeText[K_GUESTS] =
            plan.okGuests.joinToString(",")
        state.life[K_MASK] = 0
        state.lifeText.remove(K_ATT)
        state.lifeText.remove(K_SNACKS)
        state.lifeText.remove(K_MOVIE)
        state.lifeText.remove(K_FIGHT)
        state.lifeText[K_LABEL] = dateLabelOf(state.absDay)
        note(state, "🛌 Sleepover tonight! ${plan.okGuests.joinToString(", ")} " +
            "are coming over at 6 — snacks, a movie, and " +
            "lights-out at 10.")
        return true
    }

    /** Call it off before it starts — a plan with beats
     *  landed is a night in progress and can't be unmade. */
    fun cancel(state: GameState): Boolean {
        if (!plannedTonight(state)) return false
        if ((state.life[K_MASK] ?: 0) != 0) return false
        clearPlan(state)
        note(state, "🛌 Sleepover called off — the sleeping " +
            "bags go back in the closet.")
        return true
    }

    // ---------------- tonight ----------------

    fun plannedTonight(state: GameState): Boolean =
        (state.life[K_DAY] ?: -1) == state.absDay

    fun plannedGuests(state: GameState): List<String> =
        (state.lifeText[K_GUESTS] ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

    /** The person-menu line (the crew ask's pattern): a
     *  kid in your crew can be asked about sleepover
     *  plans; anyone else never sees the option at all. */
    fun menuOption(state: GameState, npcName: String): String? =
        if (Crew.isMember(state, npcName) && isKidBand(npcName))
            "🛌 Sleepover plans…" else null

    /** The crew's occupied oracle: is this person claimed
     *  by tonight's sleepover right now? Crew.busyReason
     *  asks exactly this — one reader, no second opinion. */
    fun claims(state: GameState, name: String): Boolean =
        plannedTonight(state) && name in plannedGuests(state) &&
            state.timeMin in ARRIVE_FROM until LIGHTS_OUT

    /** Who's actually here: planned guests whose agent
     *  stands at your house, awake. */
    fun presentGuests(state: GameState): List<String> =
        plannedGuests(state).filter { name ->
            val a = state.agents[name]
            a != null && !a.asleep && a.location == YOUR_HOUSE
        }

    /** The beat the evening is on: the first unlanded of
     *  the four, or STEP_COUNT when the night is whole. */
    fun currentStep(state: GameState): Int {
        val mask = state.life[K_MASK] ?: 0
        for (i in 0 until STEP_COUNT) if (mask and (1 shl i) == 0) return i
        return STEP_COUNT
    }

    fun beatsLanded(state: GameState): Int =
        Integer.bitCount(state.life[K_MASK] ?: 0)

    /** The Engine's gathering hook (the set-piece pattern):
     *  a planned guest is at your house for the window —
     *  unless a bigger claim holds them, in which case this
     *  returns null and their life wins. Seated after the
     *  play's claim and before the stand's; the veto list
     *  is every claim seated earlier plus the stand's. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (!plannedTonight(state)) return null
        if (npcName !in plannedGuests(state)) return null
        if (timeMin !in ARRIVE_FROM until LIGHTS_OUT) return null
        val busy = Crew.busyReason(state, npcName)
        if (busy != null && busy != "sleepover") return null
        if (Fair.gatheringBlock(state, npcName, timeMin) != null) return null
        if (RivalCrew.gatheringBlock(state, npcName, timeMin) != null) return null
        if (Heroes.gatheringBlock(state, npcName, timeMin) != null) return null
        if (Play.gatheringBlock(state, npcName, timeMin) != null) return null
        if (Stand.gatheringBlock(state, npcName, timeMin) != null) return null
        val done = (state.life[K_MASK] ?: 0) and (1 shl 3) != 0
        return AgendaBlock(YOUR_HOUSE,
            if (done) "Asleep at the new kid's — sleepover. Do not wake."
            else "Sleepover at the new kid's — sleeping bags, " +
                "big plans, zero adult supervision worth mentioning.")
    }

    /** The tick (Engine.advanceTime, beside Crew.tick):
     *  presence is evidence — a planned guest standing at
     *  your house during the window is stamped as here, so
     *  the morning's record names who actually came, and a
     *  guest a bigger claim pulled away is never invented
     *  into the night. */
    fun tick(state: GameState) {
        if (!plannedTonight(state)) return
        if (state.timeMin !in ARRIVE_FROM until LIGHTS_OUT) return
        val here = presentGuests(state)
        if (here.isEmpty()) return
        val seen = attendees(state).toMutableSet()
        if (seen.addAll(here)) {
            state.lifeText[K_ATT] = Crew.members(state)
                .filter { it in seen }.joinToString(",")
                .ifEmpty { seen.joinToString(",") }
        }
    }

    /** Guests seen at the house tonight (tick-stamped,
     *  unioned again at every beat — the two records of
     *  presence agree by construction). */
    fun attendees(state: GameState): List<String> =
        (state.lifeText[K_ATT] ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // ---------------- beats ----------------

    /** Dinners' TalkHit shape, one module over: a Talk
     *  with [npcName] right now would land sleepover beat
     *  [stepIndex]. Pure. Step 1 (the movie) is a Look and
     *  never answers here; steps 0, 2, 3 are Talks. */
    fun talkStepFor(state: GameState, npcName: String): SleepHit? {
        if (!plannedTonight(state)) return null
        if (state.timeMin !in ARRIVE_FROM until LIGHTS_OUT) return null
        if (npcName !in plannedGuests(state)) return null
        if (Finales.climaxPending(state)) return null
        val a = state.agents[npcName] ?: return null
        if (a.asleep || a.location != YOUR_HOUSE) return null
        val step = currentStep(state)
        if (step == 1 || step >= STEP_COUNT) return null
        val guests = presentGuests(state)
        val text = when (step) {
            0 -> snackText(state, npcName, guests)
            2 -> "🛏️ The movie's over and nobody is even slightly " +
                "sleepy. $npcName is holding a couch pillow like " +
                "it owes them money. This can only be settled " +
                "one way."
            else -> lightsOutText(state, npcName, guests)
        }
        return SleepHit(step, text, step == STEP_COUNT - 1)
    }

    /** Judge a Talk beat: snacks and lights-out land here;
     *  the pillow fight is played in its view and judged by
     *  [judgePillowFight] — judgeTalk refuses it, so no
     *  caller can narrate the fight by accident. */
    fun judgeTalk(state: GameState, npcName: String): SleepHit? {
        val hit = talkStepFor(state, npcName) ?: return null
        if (hit.stepIndex == 2) return null
        fire(state, hit.stepIndex)
        return hit
    }

    /** The movie beat hides in the Look at your house (the
     *  Dinners.lookText pattern): while the evening waits
     *  on step 1, a careful Look at home IS the screening. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != YOUR_HOUSE) return null
        if (!plannedTonight(state)) return null
        if (state.timeMin !in ARRIVE_FROM until LIGHTS_OUT) return null
        if (currentStep(state) != 1) return null
        if (Finales.climaxPending(state)) return null
        if (presentGuests(state).isEmpty()) return null
        val text = movieText(state)
        fire(state, 1)
        return text
    }

    /** Beat 0's text: the spread is the REAL pantry — up
     *  to two stocked items, one unit each, consumed and
     *  named. A bare cupboard is stated, never dressed up;
     *  when the family is home, Mom covers, and the text
     *  says the items were hers. Pure preview: the actual
     *  consumption happens in [fire] off the same reader,
     *  so the toast and the shelf can never disagree. */
    private fun snackText(state: GameState, npcName: String,
                          guests: List<String>): String {
        val served = snackItems(state)
        val who = guests.joinToString(" and ").ifEmpty { "The crew" }
        return when {
            served.size >= 2 ->
                "🍿 SNACKS. You put out ${itemName(served[0])} and " +
                    "${itemName(served[1])} — the good stuff, " +
                    "from the actual pantry. $who descend like " +
                    "it's a nature documentary. $npcName, mouth " +
                    "full: \"This is the best house.\""
            served.size == 1 ->
                "🍿 SNACKS. The pantry offers up ${itemName(served[0])} " +
                    "and you stretch it across ${guests.size} " +
                    "sleeping bags with showmanship. Nobody " +
                    "complains. The bowl gets passed like a " +
                    "ceremony."
            momHome(state) ->
                "🍿 SNACKS. The pantry is bare — and then Mom " +
                    "appears with cocoa and a sleeve of crackers " +
                    "from the parental reserves. \"A sleepover " +
                    "runs on snacks,\" she says. \"This is not " +
                    "negotiable.\" Her treat; the cupboard owes " +
                    "her one."
            else ->
                "🍿 SNACKS. The pantry is honestly empty, so the " +
                    "spread is pretzel-ish sticks, tap water in " +
                    "the good cups, and confidence. The crew " +
                    "reviews it like critics: harsh, fair, " +
                    "still eating."
        }
    }

    /** The stocked pantry items tonight's spread serves:
     *  the first two of Dinners' pantry list actually in
     *  the bag. One reader for the text and the shelf. */
    fun snackItems(state: GameState): List<String> =
        Dinners.PANTRY_ITEMS.distinct()
            .filter { (state.inventory[it] ?: 0) > 0 }
            .take(2)

    /** Beat 1's text: fact-tied or nothing. A screening
     *  stands tonight → the TV runs the Bijou's own feature
     *  (title, genre, stars — the listing's real facts).
     *  Otherwise a home movie: one of YOUR photos, described
     *  from its record alone. No album → the honest fallback. */
    private fun movieText(state: GameState): String {
        if (MovieNights.isScreeningDay(state.absDay) &&
            MovieNights.darkReason(state) == null) {
            val f = MovieNights.featureFor(state)
            state.lifeText[K_MOVIE] = "“${f.title}” (${f.genreLabel})"
            return "🎬 THE MOVIE. The Bijou's showing " +
                "“${f.title}” tonight — ${f.genreLabel}, " +
                "starring ${f.stars.joinToString(", ")} — and " +
                "the house TV has the same picture on, because " +
                "in this town the Friday feature finds YOU. " +
                "Sleeping bags turn toward the glow like " +
                "sunflowers."
        }
        val rec = homeMoviePick(state)
        if (rec != null) {
            val place = LOCATIONS[rec.locId]?.name ?: rec.locId
            val names = rec.figures.map { it.name }
            val who = if (names.size > 1)
                names.dropLast(1).joinToString(", ") + " and " + names.last()
                else names.firstOrNull() ?: "you"
            state.lifeText[K_MOVIE] = "home movie: $place, ${rec.dateLabel}"
            return "🎬 HOME MOVIE. No Bijou picture tonight, so " +
                "the TV plays the album: the photo from $place, " +
                "${rec.dateLabel} — $who in frame. The couch " +
                "provides commentary. The commentary is not " +
                "kind, and it is not wrong."
        }
        state.lifeText[K_MOVIE] = "the weather channel (the album was empty)"
        return "🎬 THE MOVIE. No Bijou picture tonight and the " +
            "album's empty, so the TV plays the weather " +
            "channel and a heroic amount of static. The crew " +
            "narrates the forecast like it's a thriller. It " +
            "somehow works."
    }

    /** The album's pick for the home movie: the newest
     *  photo with at least two of tonight's guests in
     *  frame; failing that, the newest with any guest;
     *  failing that, the newest photo there is. */
    fun homeMoviePick(state: GameState): PhotoRecord? {
        if (state.photos.isEmpty()) return null
        val guests = plannedGuests(state).toSet()
        fun guestCount(r: PhotoRecord) =
            r.figures.count { it.name in guests }
        return state.photos.lastOrNull { guestCount(it) >= 2 }
            ?: state.photos.lastOrNull { guestCount(it) >= 1 }
            ?: state.photos.lastOrNull()
    }

    /** Beat 3's text: the night closes honestly — who got
     *  the couch (the pillow fight's loser, by house law),
     *  and any planned guest the evening never actually
     *  saw named as called away, not written in. */
    private fun lightsOutText(state: GameState, npcName: String,
                              guests: List<String>): String {
        val fight = state.lifeText[K_FIGHT] ?: ""
        val couch = when {
            fight.startsWith("you") ->
                "The couch is yours — pillow-fight law, won fair."
            fight.isNotEmpty() ->
                "The couch goes to the pillow-fight winner; " +
                    "the floor bags go to everyone else, " +
                    "including, pointedly, you."
            else -> "Bags are claimed by speed and seniority."
        }
        val away = plannedGuests(state).filter { it !in attendees(state) &&
            it !in guests }
        val awayLine = if (away.isEmpty()) ""
            else " ${away.joinToString(" and ")} got called away " +
                "before lights-out — the night kept their bag " +
                "unrolled anyway."
        return "🌙 LIGHTS OUT. 10 PM, house rules. $couch " +
            "Whispering continues for exactly as long as " +
            "whispering takes to become sleep talking, and " +
            "$npcName is the first to go under, mid-sentence, " +
            "like a professional.$awayLine One by one, the " +
            "sleeping bags stop moving. Best night ever, " +
            "says nobody, out loud, because they're asleep."
    }

    /** Land a beat: stamp the mask, union the room into
     *  the attendees, and for the snacks beat take the
     *  items off the real shelf (the same list the text
     *  named — one reader, no drift). */
    private fun fire(state: GameState, step: Int) {
        if (step == 0) {
            val served = snackItems(state)
            for (id in served) {
                val n = (state.inventory[id] ?: 0) - 1
                if (n <= 0) state.inventory.remove(id)
                else state.inventory[id] = n
            }
            if (served.isNotEmpty())
                state.lifeText[K_SNACKS] = served.joinToString(",")
        }
        val seen = attendees(state).toMutableSet()
        seen.addAll(presentGuests(state))
        if (seen.isNotEmpty())
            state.lifeText[K_ATT] =
                seen.joinToString(",")
        state.life[K_MASK] =
            (state.life[K_MASK] ?: 0) or (1 shl step)
    }

    // ---------------- the pillow fight (played) ----------------

    /** The pillow fight, judged like the dunk tank: three
     *  swings on the showdown bar; each swing's zone power
     *  is cut by the guest's own dodge (Minigames' authored
     *  numbers — Tweek is a nightmare, Butters is a
     *  cushion with a pulse). A swing connects at Feel's
     *  good-power line. Take two of three and the couch is
     *  yours. Seeded per night + guest: replayable, and the
     *  suite replays it. */
    class PillowSession(seed: Long, val guest: String) {
        private val rnd = Random(seed)
        var swings = 0; private set
        var hits = 0; private set
        var powerMilli = 0; private set
        val over: Boolean get() = swings >= SWINGS

        /** Swing at release position 0..1 on the bar.
         *  Returns the power that actually landed (0 on a
         *  whiff), like DunkSession.throwBall. */
        fun swing(release01: Double): Double {
            if (over) return 0.0
            swings++
            val zone = Showdown.zoneOf(release01.toFloat())
            val caught = (rnd.nextDouble() * 0.06 - 0.03)
                .coerceIn(-0.05, 0.05)
            val power = Showdown.zonePower(zone) *
                (1.0 - Showdown.dodgeFor(guest)) + caught
            powerMilli += (power * 1000).toInt()
            if (power >= Feel.POWER_GOOD) hits++
            return power
        }

        val youWon: Boolean get() = hits * 2 > SWINGS

        companion object { const val SWINGS = 3 }
    }

    fun pillowSeed(state: GameState, guest: String): Long =
        ("slp:$guest:${state.absDay}").hashCode().toLong()

    /** Judge the played fight: lands beat 2, records the
     *  result for the lights-out telling and the Journal,
     *  and the crew who watched it count it — the showdown
     *  funnel's own tally (Crew.onShowdown), because a
     *  pillow fight in front of your crew IS one. */
    fun judgePillowFight(state: GameState,
                         session: PillowSession): SleepHit? {
        if (!plannedTonight(state)) return null
        if (currentStep(state) != 2) return null
        if (session.guest !in plannedGuests(state)) return null
        if (!session.over) return null
        val g = session.guest
        state.lifeText[K_FIGHT] = if (session.youWon)
            "you out-pillowed $g, ${session.hits} of " +
                "${PillowSession.SWINGS} swings clean"
        else "$g took the pillow fight — ${session.hits} of " +
            "your ${PillowSession.SWINGS} swings landed"
        val text = if (session.youWon)
            "🛏️ PILLOW FIGHT. ${session.hits} of your " +
                "${PillowSession.SWINGS} swings land clean on " +
                "$g, who goes down into the sleeping bags " +
                "laughing too hard to mount a defense. The " +
                "couch is yours. Pillow law is pillow law."
        else
            "🛏️ PILLOW FIGHT. Only ${session.hits} of your " +
                "${PillowSession.SWINGS} swings land — $g is " +
                "simply too quick, a pillow ghost, and the " +
                "last thing you see is feathers and justice. " +
                "The couch is theirs."
        fire(state, 2)
        Crew.onShowdown(state)
        return SleepHit(2, text, false)
    }

    // ---------------- the morning ----------------

    /** Wrap last night (Engine.onNewDay, beside the family
     *  rollover — the home systems settle together): pay
     *  each guest who actually came, once, at the stated
     *  rates; plant the topic; write the record; strike
     *  the set. A stale plan (two days old, never wrapped)
     *  is cleared with no pay and no record — the night
     *  simply didn't happen. */
    fun dailyRollover(state: GameState) {
        val day = state.life[K_DAY] ?: -1
        if (day < 0) return
        if (day != state.absDay - 1) { clearPlan(state); return }
        val beats = beatsLanded(state)
        val came = attendees(state)
            .filter { it in plannedGuests(state) }
        val pay = when {
            beats >= STEP_COUNT -> PAY_FULL
            beats >= PARTIAL_MIN -> PAY_PARTIAL
            else -> 0
        }
        if (pay > 0) for (g in came) Engine().relAdjust(state, g, pay)
        if (beats >= PARTIAL_MIN) for (g in came) plantTopic(g)
        val photo = state.photos.any { r ->
            r.absDay == day && r.locId == YOUR_HOUSE &&
                r.figures.count { it.name in came } >= 2
        }
        val rec = listOf(
            state.lifeText[K_LABEL] ?: "",
            came.joinToString(","),
            beats.toString(),
            (state.lifeText[K_SNACKS] ?: "")
                .split(",").filter { it.isNotEmpty() }
                .joinToString("+") { itemName(it) },
            (state.lifeText[K_MOVIE] ?: "").replace("|", "/"),
            (state.lifeText[K_FIGHT] ?: "").replace("|", "/"),
            if (photo) "1" else "0",
            pay.toString()
        ).joinToString("|")
        state.lifeText[K_NIGHT + day] = rec
        val days = ((state.lifeText[K_DAYS] ?: "")
            .split(",").mapNotNull { it.trim().toIntOrNull() }
            .toMutableList())
        days.add(day)
        while (days.size > HISTORY_CAP) {
            val old = days.removeAt(0)
            state.lifeText.remove(K_NIGHT + old)
        }
        state.lifeText[K_DAYS] = days.joinToString(",")
        state.life[K_WRAP] = day
        clearPlan(state)
    }

    /** The morning-after line for the wake-up report
     *  (House.morningReport, beside the family's note):
     *  only the morning right after a wrapped night. If
     *  the family is home, Mom makes pancakes — her treat,
     *  consuming no pantry item, stated as the call. */
    fun morningNote(state: GameState): String? {
        if ((state.life[K_WRAP] ?: -1) != state.absDay - 1) return null
        val rec = lastNight(state) ?: return null
        if (rec.came.isEmpty()) return null
        val who = rec.came.joinToString(" and ")
        return if (Family.arrived(state))
            "🥞 Sleepover morning: Mom makes pancakes for " +
                "the whole couch-army — $who, plus whoever " +
                "else surfaced. Her treat; no pantry item " +
                "was harmed. The guests leave fed, which in " +
                "this house is a review."
        else
            "🛌 Sleepover morning: $who surface from the " +
                "sleeping bags, eat standing up, and head " +
                "out into Saturday like it owes them money."
    }

    /** Strike the plan (the bags go back in the closet). */
    private fun clearPlan(state: GameState) {
        state.life.remove(K_DAY)
        state.lifeText.remove(K_GUESTS)
        state.life.remove(K_MASK)
        state.lifeText.remove(K_ATT)
        state.lifeText.remove(K_SNACKS)
        state.lifeText.remove(K_MOVIE)
        state.lifeText.remove(K_FIGHT)
        state.lifeText.remove(K_LABEL)
    }

    // ---------------- the record ----------------

    /** One wrapped night, read back from lifeText. */
    data class Night(
        val day: Int, val label: String, val came: List<String>,
        val beats: Int, val snacks: String, val movie: String,
        val fight: String, val photo: Boolean, val pay: Int
    )

    fun nights(state: GameState): List<Night> {
        val days = (state.lifeText[K_DAYS] ?: "")
            .split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.mapNotNull { d ->
            val parts = (state.lifeText[K_NIGHT + d] ?: "")
                .split("|")
            if (parts.size < 8) null else Night(
                d, parts[0],
                parts[1].split(",").map { it.trim() }
                    .filter { it.isNotEmpty() },
                parts[2].toIntOrNull() ?: 0, parts[3], parts[4],
                parts[5], parts[6] == "1",
                parts[7].toIntOrNull() ?: 0)
        }.reversed()
    }

    fun lastNight(state: GameState): Night? =
        nights(state).firstOrNull()

    fun nightsHosted(state: GameState): Int = nights(state).size

    /** The Journal's 🛌 SLEEPOVERS page, in the established
     *  page discipline: tonight's plan when there is one
     *  (guests coming, and every refusal named), then the
     *  record, newest first, capped like the shelf itself. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val hist = nights(state)
        when {
            plannedTonight(state) -> {
                out += "🛌 Tonight: the crew sleeps over."
                out += "Coming: ${plannedGuests(state).joinToString(", ")} " +
                    "— 6 PM to lights-out at 10."
                val step = currentStep(state)
                out += if (step >= STEP_COUNT)
                    "The night is whole: snacks, the movie, the " +
                        "pillow fight, lights out."
                else "The evening so far: ${beatsLanded(state)} of " +
                    "$STEP_COUNT beats in. Next: " +
                    listOf("snacks (talk to a guest)",
                        "the movie (look around the house)",
                        "the pillow fight (talk to a guest)",
                        "lights out (talk to a guest)")[step] + "."
            }
            hist.isEmpty() -> {
                out += "🛌 No sleepovers yet."
                out += "Friday nights. Your crew — the kids, " +
                    "anyway; grown-ups don't do sleepovers — " +
                    "at your house: snacks from the real " +
                    "pantry, a movie, a pillow fight, lights " +
                    "out at ten."
                out += "Plan one from your bed, or from a " +
                    "crew member's menu. A case, a finale, a " +
                    "holiday or opening night cancels — the " +
                    "town's bigger nights win, and the plan " +
                    "says so."
            }
            else -> out += "🛌 ${hist.size} sleepover" +
                (if (hist.size == 1) "" else "s") + " on the record."
        }
        for (n in hist) {
            out += "— Night of ${n.label}: ${n.came.joinToString(", ")}"
            out += "   ${n.beats} of $STEP_COUNT beats" +
                (if (n.snacks.isNotEmpty()) " · snacks: ${n.snacks}" else "") +
                (if (n.movie.isNotEmpty()) " · ${n.movie}" else "") +
                (if (n.fight.isNotEmpty()) " · ${n.fight}" else "")
            if (n.photo)
                out += "   📸 A group photo from the night is in the album."
            if (n.pay > 0)
                out += "   (+${n.pay} friendship each — the night paid out.)"
        }
        return out
    }

    /** The evening's dressing, in the house decor token
     *  pattern: while a sleepover is planned tonight the
     *  living room wears its sleeping bags; the morning
     *  wrap strikes them (the plan keys are gone, and so
     *  are the bags). */
    fun dressingTokens(state: GameState): Set<String> =
        if (plannedTonight(state)) setOf("sleeping-bags")
        else emptySet()

    // ---------------- small readers ----------------

    fun momHome(state: GameState): Boolean =
        Family.arrived(state) &&
            Family.blockAt(state, Family.MOM, state.timeMin)
                ?.first == YOUR_HOUSE

    private fun itemName(id: String): String = itemById(id)?.name ?: id

    /** "M/D" for an absDay, off the calendar itself. */
    fun dateLabelOf(absDay: Int): String {
        val key = keyFromOrdinal(
            FairCalendar.ordinalOfAbsDay(absDay))
        if (key.length < 8) return ""
        return "${key.substring(4, 6).toInt()}/" +
            key.substring(6, 8).toInt()
    }

    /** The topic plant, in Crew.plantTopic's exact idiom:
     *  the store's own memory, topic moved to the top of
     *  the thread list, capped at six like every thread. */
    private fun plantTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** Module ticker note (the dinners' idiom — the call
     *  sites in GameActivity add their own toasts). */
    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }
}

/** Sleepovers' TalkHit — Dinners.DinnerHit's shape: the
 *  step the Talk lands, its text, and whether it closed
 *  the night. */
data class SleepHit(
    val stepIndex: Int,
    val text: String,
    val allLanded: Boolean
)

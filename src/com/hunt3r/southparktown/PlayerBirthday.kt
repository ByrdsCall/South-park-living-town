package com.hunt3r.southparktown

// ============================================================
// YOUR BIRTHDAY (v0.14.0d, Queue #14 item 4) — the town
// celebrates the new kid.
//
// Cast birthdays (v0.12.0c) put all 98 townsfolk on the
// calendar and stated plainly what they did NOT do: "The
// PLAYER's character gets NO birthday from any of this —
// no real-world date is wired into the game." This build
// keeps that promise and gives the player a birthday
// anyway, out of the game's own cloth:
//
//   THE DATE, DERIVED — your birthday is the anniversary
//   of the save's arrival day: the Family module's arrival
//   record (life["famArrivalDay"], stamped since v0.10.0a —
//   the day this household's life in South Park began).
//   It recurs every 365 game days from that record and
//   from nothing else: no new stored date, no date picker,
//   and — counted in days, never in calendar dates — leap
//   years cannot move it (the cast registry in Birthdays
//   is calendar-dated "MMDD"; this one is arithmetic).
//   The first birthday a save can see is one full year in:
//   a younger save simply hasn't had one, and nothing
//   arrives retroactively. The Journal page says so.
//
//   THE MORNING — at rollover into the birthday, Mom's
//   bake lands in your pack: ONE real snack cake, the same
//   item the town's parties run on (Birthdays.CAKE_ITEM,
//   read by reference — never re-typed), stamped per year
//   so a save/reload on the birthday morning can never
//   duplicate it. A card from the household arrives in the
//   mail the same morning; its text is kept verbatim on
//   the Journal's 🎂 YOUR BIRTHDAY page. And the first
//   time you talk to each parent at home that day, they
//   mark it before anything else — a line of their own,
//   and +GREET_WARMTH in the family's own famPts lane
//   (never state.friendship: a parent is not a townsfolk,
//   Family's wall, kept).
//
//   THE WISHES — across the day, cast members wish you
//   well IN PERSON: a wish rides the conversation's owed-
//   lines seam (TalkGate.owedLines — a seam, not a rail;
//   the gate still stands at eleven), derived by the
//   wisher's friendship band off Relationships.tierOf:
//   CLOSE_FRIEND wishes are warm and specific-feeling,
//   FRIEND wishes are glad, ACQUAINTANCE wishes are
//   standard and kind; strangers and rivals don't know
//   it's your birthday, and the page says word travels by
//   friendship. A wish moves NO friendship, NO coins,
//   nothing but its own stamp — at most once per person
//   per birthday, pinned.
//
//   THE PARTY — in the evening, if your crew is non-empty
//   and the town lets the night be yours, your crew throws
//   a small party at YOUR house, in the sleepover/dinner
//   idiom: three beats (0 the Arrival — a Talk; 1 the Cake
//   — a Look at your house, serving exactly ONE real
//   snack_cake when one is held, Mom's improvisation
//   stated honestly when none is; 2 the Send-off — a
//   Talk), riding the DINNER TalkGate rail with a payload
//   beside Birthdays' PartyHit — no new rail. The guests
//   are claimed to your house on the sleepover claim's
//   pattern, consulted at the sleepover claim's own seat
//   in Engine's stack (the way Birthdays' claim rides
//   Dinners' seat): it takes no new seat, and it self-
//   guards on the crew's occupied oracle, so every bigger
//   claim seated earlier still wins its people. The wrap
//   pays by the sleepover discipline: +2 a guest for a
//   full night, +1 for a partial one, once, at rollover —
//   and a party nobody reached leaves no trace. If the
//   crew is empty the day stays household-only, stated
//   plainly and punished nowhere: the family beat, the
//   card and the cake carry it.
//
//   THE YIELD — a birthday that lands on a holiday, in a
//   finale week, on a case-climax night, opening night, a
//   fair or carnival week, a night your own table is
//   spoken for (family dinner, a hosted dinner, a planned
//   sleepover) still fires EVERYTHING above — the morning
//   is the household's, and the household is yours — while
//   the evening party yields, with the reason derived on
//   the birthday morning (Dance's guarded pattern),
//   stamped into the day's record, and stated wherever
//   the player would look: the morning report and the
//   Journal page.
//
//   THE RECORDS — ONE TOWN NEWS line in the birthday
//   week's edition (the headline ladder stands at ten);
//   ONE phone text on the birthday morning under the new
//   PLAYERBDAY trigger — from your closest friend, or
//   from Mom when no friend stands close enough (the
//   sender set stays closed: cast ∪ parents, as ever);
//   the Journal 🎂 YOUR BIRTHDAY page (the derivation in
//   plain words, the card kept, who wished, the party
//   read-back). State is life/lifeText stamps only —
//   pb* keys, listed at their sites and in the README.
// ============================================================

object PlayerBirthday {

    // ---------------- the date ----------------

    /** The year, in days. The derivation is day-counted
     *  arithmetic off the arrival record — never a
     *  calendar date — so February 29ths, which the game's
     *  calendar does grow, can never move, skip or double
     *  a birthday. */
    const val YEAR_DAYS = 365

    /** The arrival record the date derives from: Family's
     *  own preview reader (the stamped day, or the day it
     *  will stamp) — read purely, so asking about your
     *  birthday never writes anything by itself. */
    fun anchorDay(state: GameState): Int =
        Family.arrivalDayPreview(state)

    /** Is [absDay] a birthday, counting from [anchor]?
     *  Fires on anchor + 365k for k ≥ 1 — exactly one day
     *  in every 365, so two birthdays can never stack. */
    fun isBirthdayDay(absDay: Int, anchor: Int): Boolean =
        absDay > anchor && (absDay - anchor) % YEAR_DAYS == 0

    fun isBirthday(state: GameState): Boolean =
        isBirthdayDay(state.absDay, anchorDay(state))

    /** Which birthday today is (1 = the first), or 0 when
     *  today isn't one. */
    fun birthdayNumber(state: GameState): Int =
        if (!isBirthday(state)) 0
        else (state.absDay - anchorDay(state)) / YEAR_DAYS

    /** Birthdays whose day has arrived, today included. */
    fun birthdaysReached(state: GameState): Int {
        val elapsed = state.absDay - anchorDay(state)
        return if (elapsed < YEAR_DAYS) 0 else elapsed / YEAR_DAYS
    }

    /** The next birthday's absDay (today, when today is
     *  one) — the page's countdown source. */
    fun nextBirthdayDay(state: GameState): Int {
        val anchor = anchorDay(state)
        val elapsed = state.absDay - anchor
        if (elapsed < YEAR_DAYS) return anchor + YEAR_DAYS
        val rem = elapsed % YEAR_DAYS
        return if (rem == 0) state.absDay
        else state.absDay + (YEAR_DAYS - rem)
    }

    fun daysUntilBirthday(state: GameState): Int =
        nextBirthdayDay(state) - state.absDay

    // ---------------- dials ----------------

    /** The morning's gift: Mom's bake — the very item the
     *  town's birthday parties serve, read off Birthdays'
     *  own constant (the shop counter's staple in the
     *  pantry's tables). If it's still in your pack at the
     *  party, the cake beat serves it: one item, two beats
     *  of the same day, no second cake anywhere. */
    val GIFT_ITEM: String = Birthdays.CAKE_ITEM

    /** Warmth per parent for the morning's marking — the
     *  family's own lane (Family.adjustPoints' clamps),
     *  once per parent per birthday, stamped. */
    const val GREET_WARMTH = 2

    /** The party's window: the dinner hours, read off
     *  Dinners by reference — a party at your table runs
     *  on your table's clock. */
    const val PARTY_FROM = Dinners.DINNER_FROM
    const val PARTY_TO = Dinners.DINNER_TO

    /** The party's beats: arrival, cake, send-off — the
     *  dinners' invited-evening count, at your own house. */
    const val STEP_COUNT = 3
    val STEP_TITLES = listOf("The Arrival", "The Cake",
        "The Send-off")

    /** The wrap's pay, per guest who actually came — the
     *  sleepovers' arrival-style rates: a full night (all
     *  three beats) pays this … */
    const val PAY_FULL = 2

    /** … a partial night (at least one beat) pays this,
     *  and a night that never got going pays nothing and
     *  leaves no record. */
    const val PAY_PARTIAL = 1

    /** The topic a played-through party plants in a guest
     *  who came (Threads follow-ups carry it, like the
     *  sleepovers' and the cast parties'). */
    const val TOPIC = "birthday party"

    // ---------------- state keys ----------------
    // All additive, all in the life/lifeText idiom:
    //   pbRollDay        life: the rollover's day guard
    //   pbGiftYear       life: the k last gifted (the gift
    //                    lands exactly once per birthday)
    //   pbCardYear       life: the k the kept card is from
    //   pbCard           lifeText: the kept card, verbatim
    //   pbYieldDay       life: the day the yield stamp is for
    //   pbYield          lifeText: the yield reason, derived
    //                    on the birthday morning ("" = none)
    //   pbGreet:Mom/Dad  life: the morning the parent marked
    //   pbWishK:<name>   life: the k <name> last wished in
    //   pbStepDay        life: the party beats' day
    //   pbSteps          life: the party beats' mask
    //   pbCake           life: the cake beat's verdict
    //                    (1 = a real cake served, 0 = improvised)
    //   pbAtt            lifeText: the party's attendees, csv
    //   pbWrapDay        life: the morning the wrap landed
    //   pbNight          lifeText: the wrap's night line
    //   pbParties        life: parties wrapped, all told
    //   pbPartyDay       life: the last wrapped party's day
    //   pbLastParty      lifeText: the last party, in words

    private fun kSteps() = "pbSteps"
    private fun kStepDay() = "pbStepDay"
    private fun kCake() = "pbCake"
    private fun kAtt() = "pbAtt"

    // ---------------- the morning gift & card ----------------

    /** The household's cards, kept verbatim on the page.
     *  One per birthday, rotating by the year — a card is
     *  written by the household, in the household's voice,
     *  and it never names a date: the town keeps the day,
     *  the card keeps the feeling. */
    val CARDS: List<String> = listOf(
        "Happy birthday. Another year of you in this town, " +
            "and the house is better for every day of it. " +
            "There's cake with your name on it — your " +
            "father wrote it in frosting and spelled most " +
            "of it right. All our love, always. — Mom & Dad",
        "Happy birthday, from the whole household (both of " +
            "us; the house itself abstained). A year ago " +
            "this family landed in South Park, and it " +
            "remains the best decision we never quite made " +
            "on purpose. Cake's handled. You're handled. " +
            "We love you. — Mom & Dad",
        "Happy birthday. Your mother has measured the year " +
            "in dinners and reports you present at most of " +
            "them, which she calls 'a strong attendance " +
            "record.' Your father has measured it in " +
            "shelves. Both metrics say the same thing: " +
            "good year, good kid. — Mom & Dad")

    fun cardFor(k: Int): String =
        CARDS[kotlin.math.abs(k - 1) % CARDS.size]

    /** The kept card's text, or null before the first
     *  birthday's mail. */
    fun keptCard(state: GameState): String? =
        state.lifeText["pbCard"]?.takeIf { it.isNotEmpty() }

    // ---------------- the parents' morning beat ----------------

    private val GREETINGS: Map<String, List<String>> = mapOf(
        Family.MOM to listOf(
            "There it is — happy birthday, honey. A whole " +
                "year of you in this town. The card's on " +
                "the counter, and check your pack: birthday " +
                "rules, Mom's baking, handle with love.",
            "Happy birthday! I measured the year in dinners " +
                "and you sat at most of them, which is the " +
                "only metric this house trusts. Card's on " +
                "the counter. Something's in your pack. We " +
                "love you — that's the whole speech."),
        Family.DAD to listOf(
            "Happy birthday, champ. One year in town. I " +
                "checked the calendar twice and everything — " +
                "it's official. Card's on the counter. Your " +
                "mother's baking is in your pack. Handle " +
                "with care; it's load-bearing cake.",
            "Morning, and happy birthday. A year! The house " +
                "has kept the date so you don't have to. " +
                "Card's on the counter, cake's in your pack, " +
                "and your mother's already emotional, so " +
                "pace yourself."))

    /** The birthday's closer, by how the evening reads:
     *  the party's promise, the yield said kindly, or the
     *  household's own quiet plan. */
    private fun greetCloser(state: GameState, parent: String): String {
        val mom = parent == Family.MOM
        return when {
            partyStands(state) -> if (mom)
                " Your crew's throwing you a party here " +
                    "tonight — I've been asked to keep the " +
                    "kitchen clear, and I am at peace with it."
            else
                " Your crew's got a party planned here " +
                    "tonight. I've been assigned one job — " +
                    "stay out of the way — and I intend to " +
                    "excel at it."
            yieldReason(state).isNotEmpty() -> if (mom)
                " Tonight's the town's, one way or another, " +
                    "so it's just us and a cake. Best kind " +
                    "of party there is, whatever anybody says."
            else
                " Tonight's spoken for by the town, so the " +
                    "party's us and a cake. Your mother calls " +
                    "that 'the deluxe package.' She's right."
            else -> if (mom)
                " Tonight's quiet — just us and a cake, " +
                    "unless your people surprise us. Best " +
                    "kind of party there is."
            else
                " Quiet one tonight — the household " +
                    "edition. Cake, card, your folks. Deluxe."
        }
    }

    /** The family's birthday line for [parent], or null:
     *  the first talk at home on the birthday, before the
     *  everyday greeting (the owed-line discipline: the
     *  stamp IS the playing). Judges the warmth as it
     *  plays — Family's famPts lane only. Wired into
     *  GameActivity's family sheet beside the darkroom's
     *  owed line and the hero remark. */
    fun familyLine(state: GameState, parent: String): Line? {
        if (!isBirthday(state)) return null
        if (!Family.arrived(state)) return null
        if (parent !in Family.PARENTS) return null
        val key = "pbGreet:$parent"
        if (state.life[key] == state.absDay) return null
        val (loc, _) = Family.blockAt(state, parent) ?: return null
        if (loc != YOUR_HOUSE) return null
        val bank = GREETINGS[parent] ?: return null
        state.life[key] = state.absDay
        Family.adjustPoints(state, parent, GREET_WARMTH)
        val text = bank[kotlin.math.abs(
            birthdayNumber(state) + state.absDay) % bank.size] +
            greetCloser(state, parent)
        return Line(parent, text)
    }

    // ---------------- the wishes ----------------

    /** Wishes by band — the wisher's own register, three
     *  warmths of it. The pick is seeded by the year and
     *  the wisher, so a wish is the same wish all day. */
    val WISH_CLOSE: List<String> = listOf(
        "Happy birthday. I mean it in the specific way — " +
            "of all the people in this town, you're the one " +
            "I'd pick. Don't tell the others. Actually, " +
            "tell them. I don't care.",
        "It's your birthday?! Okay — best day. You're one " +
            "of my favorite people in this whole town and I " +
            "have the data to back that up. Cake. Soon. " +
            "Mandatory.",
        "Happy birthday to the person who makes this town " +
            "make sense. Seriously. Whatever you're doing " +
            "this year, I'm in.",
        "Another year of you. Good. The town's measurably " +
            "better with you in it — I did the math. It's " +
            "just me saying it, but still.",
        "Happy birthday! You know you're stuck with me, " +
            "right? That's not a threat, that's the gift. " +
            "The gift is me.",
        "For your birthday I got you exactly what you " +
            "wanted: me, saying something nice, in public, " +
            "on the record. Happy birthday.")

    val WISH_FRIEND: List<String> = listOf(
        "Hey — happy birthday! Hope the year's good to " +
            "you. You've earned a good one.",
        "Happy birthday! A whole year in town — you're " +
            "basically a local now. There's no going back.",
        "Heard it's your birthday. Many happy returns, or " +
            "whatever people say. I mean it, though.",
        "Happy birthday! If there's cake anywhere in this " +
            "town today, you legally get the first slice. " +
            "I checked. It's law.",
        "Oh, it's your birthday? Well — happy birthday. " +
            "Glad you ended up in this town.",
        "Happy birthday! One more year of you. The town " +
            "approves. I polled it. Unanimous-ish.")

    val WISH_ACQUAINTANCE: List<String> = listOf(
        "Oh — happy birthday. Many happy returns.",
        "It's your birthday? Happy birthday, then.",
        "Happy birthday. Hope it's a good day for you.",
        "Somebody said it's your birthday — well, happy " +
            "birthday.")

    /** Kenny speaks only muffled, on his birthday wishes
     *  as everywhere — his own override bank. */
    val WISH_KENNY: List<String> = listOf(
        "(muffled) Mmph mmph mmph! (Happy birthday, " +
            "apparently — with feeling.)",
        "(muffled) Mmmph mmph! (It's your birthday, and " +
            "Kenny is thrilled about it.)")

    /** Timmy's register is proclamation; his wish is one. */
    const val WISH_TIMMY =
        "TIMMY! (Translation: happy birthday. The " +
            "enthusiasm is the message.)"

    /** The band a wisher speaks from, off the town's own
     *  tier table: a tier index (Relationships), or -1
     *  when this person doesn't know you well enough to
     *  know it's your birthday. */
    fun wishBand(state: GameState, name: String): Int {
        val tier = Relationships.tierOf(
            state.friendship[name] ?: 0)
        return if (tier >= Relationships.ACQUAINTANCE) tier
        else -1
    }

    /** [name]'s wish for this birthday, unplayed — pure:
     *  the band's bank, the year's seed, the two registers
     *  that override any band. */
    fun wishText(state: GameState, name: String): String? {
        if (!isBirthday(state)) return null
        val band = wishBand(state, name)
        if (band < 0) return null
        if (name == "Kenny")
            return WISH_KENNY[kotlin.math.abs(
                birthdayNumber(state) * 7 + name.hashCode()) %
                WISH_KENNY.size]
        if (name == "Timmy") return WISH_TIMMY
        val bank = when (band) {
            Relationships.CLOSE_FRIEND -> WISH_CLOSE
            Relationships.FRIEND -> WISH_FRIEND
            else -> WISH_ACQUAINTANCE
        }
        return bank[kotlin.math.abs(
            birthdayNumber(state) * 31 + name.hashCode()) %
            bank.size]
    }

    /** Who has wished you well in birthday [k], roster
     *  order — the page's record, read off the stamps. */
    fun wishers(state: GameState, k: Int): List<String> =
        KID_ORDER.filter { (state.life["pbWishK:$it"] ?: 0) == k }

    /** The wish [npcName] speaks in conversation today,
     *  or null — TalkGate.owedLines' seam (a seam, not a
     *  rail). Talk-flavor only: it stamps its once-per-
     *  person-per-birthday record and moves NOTHING else —
     *  no friendship, no coins, no warmth, pinned. */
    fun wishLine(state: GameState, npcName: String): Line? {
        if (!isBirthday(state)) return null
        if (npcName !in KID_ORDER) return null
        val k = birthdayNumber(state)
        if ((state.life["pbWishK:$npcName"] ?: 0) == k) return null
        val text = wishText(state, npcName) ?: return null
        state.life["pbWishK:$npcName"] = k
        return Line(npcName, text)
    }

    // ---------------- the party ----------------

    /** Why tonight's party can't stand, town-claims only
     *  (the crew itself is read live, separately), or null
     *  when the evening is free for one. Birthdays'
     *  deference discipline, at your own house: anything
     *  that owns the evening, the week, or THIS table
     *  outranks a birthday — and says so, in words the
     *  morning report and the Journal page repeat. */
    fun partyBlock(state: GameState): String? {
        if (Finales.windowAt(state) != null)
            return "a season finale owns this week — parties wait"
        if (HolidayBeats.activeBeat(state) != null)
            return "a holiday beat owns tonight"
        if (MovieNights.anyClimaxDueTonight(state))
            return "a case comes to a head tonight"
        if (Dinners.beatsDeferred(state))
            return "a climax is on the table tonight"
        if (Play.nightCanHappen(state))
            return "it's opening night at the Elementary"
        if (Fair.windowToday(state) != null)
            return "the county fair owns this week"
        if (Carnival.windowToday(state) != null)
            return "the winter carnival owns this week"
        if (Dinners.hostingTonight(state) != null)
            return "you're hosting dinner at your own table tonight"
        if (Family.dinnerTonight(state))
            return "it's family dinner night at your house — " +
                "the folks have the table"
        if (Sleepovers.plannedTonight(state))
            return "there's a sleepover planned at your house tonight"
        return null
    }

    /** Tonight's yield reason as the day knows it: the
     *  birthday morning's stamp when it's for today (the
     *  rollover derives it before the day begins, Dance's
     *  discipline), else the live derivation. "" when the
     *  evening is free. */
    fun yieldReason(state: GameState): String {
        if ((state.life["pbYieldDay"] ?: -1) == state.absDay)
            return state.lifeText["pbYield"] ?: ""
        return partyBlock(state) ?: ""
    }

    /** The party guests: your crew, whole — the crew
     *  throws this party, grown-ups and all (a sleepover
     *  is a kid institution; a birthday is everybody's). */
    fun partyGuests(state: GameState): List<String> =
        Crew.members(state)

    /** Does tonight's party stand? The birthday, a crew
     *  to throw it, and the evening free. */
    fun partyStands(state: GameState): Boolean =
        isBirthday(state) && partyGuests(state).isNotEmpty() &&
            yieldReason(state).isEmpty()

    /** Guests actually in the room: at your house, awake. */
    fun presentGuests(state: GameState): List<String> =
        partyGuests(state).filter { name ->
            val a = state.agents[name]
            a != null && !a.asleep && a.location == YOUR_HOUSE
        }

    /** The crew's occupied oracle (Crew.busyReason reads
     *  it, beside the sleepovers' claim): is this guest
     *  claimed by tonight's party right now? */
    fun claims(state: GameState, name: String): Boolean =
        partyStands(state) && name in partyGuests(state) &&
            state.timeMin in PARTY_FROM..PARTY_TO

    /** The Engine's gathering hook, on the sleepover
     *  claim's pattern, consulted at the sleepover seat —
     *  it takes no seat of its own: a guest is at your
     *  house for the party window, unless a bigger claim
     *  holds them (the occupied oracle's veto, plus the
     *  sleepovers' own veto list read whole), in which
     *  case this returns null and their life wins. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (!partyStands(state)) return null
        if (npcName !in partyGuests(state)) return null
        if (timeMin !in PARTY_FROM..PARTY_TO) return null
        val busy = Crew.busyReason(state, npcName)
        if (busy != null && busy != "birthday party") return null
        if (Fair.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (RivalCrew.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Heroes.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Play.gatheringBlock(state, npcName, timeMin) != null)
            return null
        if (Stand.gatheringBlock(state, npcName, timeMin) != null)
            return null
        return AgendaBlock(YOUR_HOUSE,
            "Your birthday party at your house — your crew's " +
                "throwing it, and you're the occasion.")
    }

    // ---------------- party beats ----------------

    private fun stepsMask(state: GameState): Int =
        if ((state.life[kStepDay()] ?: -1) == state.absDay)
            state.life[kSteps()] ?: 0 else 0

    fun stepsDone(state: GameState): Int =
        Integer.bitCount(stepsMask(state))

    private fun currentStep(state: GameState): Int {
        val mask = stepsMask(state)
        var s = 0
        while (s < STEP_COUNT && (mask and (1 shl s)) != 0) s++
        return s
    }

    private fun inWindow(state: GameState): Boolean =
        state.timeMin in PARTY_FROM..PARTY_TO

    /** TalkGate's payload for a party beat (steps 0 and
     *  2; the cake is a Look and never answers here). */
    data class BdayHit(
        val stepIndex: Int,
        val text: String,
        val allLanded: Boolean
    )

    private val ARRIVALS: List<String> = listOf(
        "🎉 THE ARRIVAL. {G} comes through your door like " +
            "the party was their idea — because it was, " +
            "partly. The crew's claimed the living room, " +
            "the couch has been moved for no clear reason, " +
            "and somebody's put the good cups out. It's " +
            "your birthday and your house is full of your " +
            "people.",
        "🎉 THE ARRIVAL. {G} arrives with the expression " +
            "of a person who has been waiting all day to " +
            "say it: “HAPPY BIRTHDAY.” The rest of the crew " +
            "piles in behind. The house gets loud in the " +
            "good way, all at once.",
        "🎉 THE ARRIVAL. {G} is first in, holding a paper " +
            "crown with your name on it — spelled right, " +
            "which in this town is a love language. The " +
            "crew takes the room. Party: officially begun.")

    private val SENDOFFS: List<String> = listOf(
        "🎉 THE SEND-OFF. Coats, leftovers wrapped in foil " +
            "nobody asked for, and {G} at the door saying " +
            "it plainly: “Good year. Let's do another one.” " +
            "The crew drains out into the evening, loud " +
            "until the corner, then gone. The house is " +
            "quiet and full at the same time.",
        "🎉 THE SEND-OFF. {G} does the honors: one last " +
            "chorus at a volume the neighbors will mention " +
            "tomorrow, and then everybody's hugging " +
            "everybody like it's a regulation. Best house, " +
            "best crew, best year. The door closes soft.",
        "🎉 THE SEND-OFF. {G} leaves last, crown in hand — " +
            "yours, technically, but finders rules. “Same " +
            "time next year,” they say, like it's a " +
            "standing plan now. Maybe it is.")

    private fun scene(bank: List<String>, state: GameState,
                      guest: String): String =
        bank[kotlin.math.abs(state.absDay * 3 +
            guest.hashCode()) % bank.size]
            .replace("{G}", guest)

    /** A Talk with [npcName] right now would land party
     *  beat [BdayHit.stepIndex], or null. Pure. The beats
     *  are any present guest's to land — the crew is the
     *  host here; there is no door-keeper but the evening. */
    fun talkStepFor(state: GameState, npcName: String): BdayHit? {
        if (!partyStands(state)) return null
        if (!inWindow(state)) return null
        if (npcName !in partyGuests(state)) return null
        if (Dinners.beatsDeferred(state)) return null
        val a = state.agents[npcName] ?: return null
        if (a.asleep || a.location != YOUR_HOUSE) return null
        val step = currentStep(state)
        if (step == 1 || step >= STEP_COUNT) return null
        val text = when (step) {
            0 -> scene(ARRIVALS, state, npcName)
            else -> scene(SENDOFFS, state, npcName)
        }
        return BdayHit(step, text,
            stepsDone(state) + 1 >= STEP_COUNT)
    }

    /** Judge a Talk party beat — the TalkGate caller in
     *  GameActivity shows its text like a party beat. */
    fun judgeTalk(state: GameState, npcName: String): BdayHit? {
        val hit = talkStepFor(state, npcName) ?: return null
        fire(state, hit.stepIndex)
        return hit.copy(allLanded = stepsDone(state) >= STEP_COUNT)
    }

    /** The cake beat hides in the Look at your own house
     *  (the Dinners/Birthdays pattern): while the evening
     *  waits on step 1, a careful Look at home IS the cake.
     *  Fires the beat — and serves the cake, when one's
     *  held — exactly like the cast parties' cake. */
    fun lookText(state: GameState, locId: String): String? {
        if (!partyStands(state)) return null
        if (locId != YOUR_HOUSE) return null
        if (!inWindow(state)) return null
        if (currentStep(state) != 1) return null
        if (Dinners.beatsDeferred(state)) return null
        if (presentGuests(state).isEmpty()) return null
        val brought = (state.inventory[Birthdays.CAKE_ITEM] ?: 0) > 0
        val text = if (brought)
            "🎂 THE CAKE. You produce the Snacky Cake and " +
                "the table goes quiet in the good way. " +
                "Candles get found, lit, and sung at. The " +
                "wish is yours and you keep it — some things " +
                "stay private even at your own party. First " +
                "slice: yours, by law and by crew."
        else if (Family.arrived(state))
            "🎂 THE CAKE. Your pack is cakeless — and Mom, " +
                "who has clearly been ready for exactly this " +
                "since breakfast, produces a candle and sets " +
                "it into a stack of pancakes with total " +
                "confidence. “It's a cake if we say it's a " +
                "cake,” she says. The crew says it's a cake. " +
                "It's a cake."
        else
            "🎂 THE CAKE. Your pack is cakeless, and the " +
                "crew improvises without blinking: a candle " +
                "in a stack of whatever the pantry had, " +
                "presented like a state treasure. A cake is " +
                "a promise, and this is a promise."
        fire(state, 1)
        return text
    }

    /** What a Look at your house says while the party
     *  stands but its cake beat isn't up — the venue line,
     *  so the house reads as dressed for the night. Never
     *  fires anything. */
    fun venueText(state: GameState, locId: String): String? {
        if (!partyStands(state)) return null
        if (locId != YOUR_HOUSE) return null
        if (!inWindow(state)) return null
        if (currentStep(state) == 1) return null
        return "🎂 Your place is dressed for a birthday — " +
            "paper crown on the hook, the good cups out, " +
            "and your crew claiming the couch like they " +
            "pay rent."
    }

    /** Land a beat: stamp the mask, union the room into
     *  the attendees (presence is the record), and for
     *  the cake beat serve ONE real cake out of the
     *  player's pack when one is held — exactly one, and
     *  the record says which way it went (Birthdays.fire's
     *  discipline, on Birthdays' own cake constant). */
    private fun fire(state: GameState, step: Int) {
        state.life[kStepDay()] = state.absDay
        if (step == 1) {
            val held = state.inventory[Birthdays.CAKE_ITEM] ?: 0
            if (held > 0) {
                if (held == 1) state.inventory.remove(
                    Birthdays.CAKE_ITEM)
                else state.inventory[Birthdays.CAKE_ITEM] = held - 1
                state.life[kCake()] = 1
            } else {
                state.life[kCake()] = 0
            }
        }
        val seen = attendees(state).toMutableSet()
        seen.addAll(presentGuests(state))
        if (seen.isNotEmpty())
            state.lifeText[kAtt()] = partyGuests(state)
                .filter { it in seen }.joinToString(",")
                .ifEmpty { seen.joinToString(",") }
        state.life[kSteps()] =
            stepsMask(state) or (1 shl step)
    }

    /** Guests the evening actually saw (beat-unioned). */
    fun attendees(state: GameState): List<String> =
        (state.lifeText[kAtt()] ?: "")
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // ---------------- the wrap ----------------

    /** The morning-after line for last night's party, the
     *  night-line discipline: kept at the wrap, shown the
     *  one morning after. */
    fun nightLine(state: GameState): String? =
        if ((state.life["pbWrapDay"] ?: -1) == state.absDay)
            state.lifeText["pbNight"] else null

    private fun plantTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** Rollover (Engine.onNewDay, beside the sleepovers'):
     *  yesterday's party wraps — pay the guests who came,
     *  once, at the stated rates; plant the topic; write
     *  the night line and the record. Then the birthday
     *  itself, when today is one: the gift lands (stamped,
     *  once), the card arrives (kept), the yield is derived
     *  and stamped while the morning still knows the day. */
    fun dailyRollover(state: GameState) {
        if (state.life["pbRollDay"] == state.absDay) return
        state.life["pbRollDay"] = state.absDay
        // ---- wrap yesterday's party ----
        val y = state.absDay - 1
        if ((state.life[kStepDay()] ?: -2) == y &&
            isBirthdayDay(y, anchorDay(state))) {
            val beats = Integer.bitCount(state.life[kSteps()] ?: 0)
            val came = attendees(state)
                .filter { it in partyGuests(state) }
            if (beats > 0 && came.isNotEmpty()) {
                val pay = if (beats >= STEP_COUNT) PAY_FULL
                    else PAY_PARTIAL
                for (g in came) Engine().relAdjust(state, g, pay)
                if (beats >= 2) for (g in came) plantTopic(g)
                val full = beats >= STEP_COUNT
                val cakeBit = when {
                    !full -> "You caught part of it"
                    (state.life[kCake()] ?: 0) == 1 ->
                        "The cake you brought was the centerpiece"
                    else -> "No cake was brought; Mom " +
                        "improvised, and it counted anyway"
                }
                state.lifeText["pbNight"] =
                    "🎂 Your birthday party at your house — " +
                        came.joinToString(", ") + ". $cakeBit. " +
                        if (full) "All three beats of the " +
                            "evening, in."
                        else "The evening counted."
                state.life["pbWrapDay"] = state.absDay
                state.life["pbParties"] =
                    (state.life["pbParties"] ?: 0) + 1
                state.life["pbPartyDay"] = y
                state.lifeText["pbLastParty"] =
                    "birthday #" +
                        ((y - anchorDay(state)) / YEAR_DAYS) +
                        " — " + came.joinToString(", ") +
                        "; $beats of $STEP_COUNT beats; " +
                        (if ((state.life[kCake()] ?: 0) == 1)
                            "a real cake served"
                        else "the cake improvised")
            }
        }
        // ---- today's birthday, if it is one ----
        if (isBirthday(state)) {
            val k = birthdayNumber(state)
            if ((state.life["pbGiftYear"] ?: 0) != k) {
                state.life["pbGiftYear"] = k
                state.inventory[GIFT_ITEM] =
                    (state.inventory[GIFT_ITEM] ?: 0) + 1
                state.life["pbCardYear"] = k
                state.lifeText["pbCard"] = cardFor(k)
            }
            state.life["pbYieldDay"] = state.absDay
            state.lifeText["pbYield"] = partyBlock(state) ?: ""
        }
    }

    /** The House morning report's line: last night's party
     *  result when it's fresh; otherwise the birthday
     *  itself, announced with its morning facts — the
     *  gift, the card, and the evening stated plainly
     *  (the party, the yield, or the household's day). */
    fun morningNote(state: GameState): String? {
        nightLine(state)?.let { return it }
        if (!isBirthday(state)) return null
        val k = birthdayNumber(state)
        val head = "🎂 It's YOUR birthday today — birthday " +
            "#$k for this house, a year at a time since the " +
            "day you all came to town. Mom's bake is in your " +
            "pack, and a card came in the mail (kept on your " +
            "Journal's 🎂 page)."
        val tail = when {
            partyStands(state) ->
                " Your crew's throwing a party at your house " +
                    "tonight (${Dinners.windowLabel()}) — show " +
                    "up and be celebrated."
            yieldReason(state).isNotEmpty() ->
                " No party tonight: ${yieldReason(state)}. " +
                    "The household's marking it anyway — the " +
                    "morning, the card and the cake are yours."
            else ->
                " No crew to throw a party yet — the " +
                    "household's marking it: a card, a cake, " +
                    "and your folks, all day."
        }
        // v0.15.0i: a cast birthday sharing the day is
        // named in your own morning too — the calendar
        // doubled up, and both days count in full.
        val cast = Birthdays.birthdaysToday(state)
        val shareBit = if (cast.isNotEmpty())
            " It's also " +
                "${Birthdays.joinNames(cast)}'s birthday " +
                "today — the calendar doubled up, and both " +
                "days count in full."
        else ""
        return head + tail + shareBit
    }

    // ---------------- the phone ----------------

    /** The birthday morning's one text, fully composed
     *  (the Dance/Festival discipline: the module owns the
     *  facts, the harvest only delivers): from your closest
     *  friend when one stands close enough to know the
     *  day, else from Mom — the sender set stays closed
     *  either way, and the words carry facts only. */
    fun phoneBeat(state: GameState): Triple<String, String, String>? {
        if (!isBirthday(state)) return null
        val best = KID_ORDER.maxByOrNull {
            state.friendship[it] ?: 0 }
        val bestPts = best?.let { state.friendship[it] ?: 0 } ?: 0
        val evening = when {
            partyStands(state) ->
                "your crew's got a party at your place " +
                    "tonight — be there, it's yours."
            yieldReason(state).isNotEmpty() ->
                "tonight's the town's, so it's the " +
                    "household edition — cake at home, " +
                    "low-key, perfect."
            else ->
                "household rules tonight — cake at your " +
                    "place, low-key, perfect."
        }
        return if (best != null && Relationships.tierOf(bestPts) >=
            Relationships.FRIEND) {
            Triple(best, "friend",
                "happy birthday!! a whole year of you in " +
                    "this town. $evening the card's on your " +
                    "journal page.")
        } else if (Family.arrived(state)) {
            Triple(Family.MOM, "mom",
                "happy birthday, honey. card's on the " +
                    "counter at home, and check your pack — " +
                    "birthday rules. $evening we love you.")
        } else if (best != null) {
            Triple(best, "friend",
                "happy birthday!! a whole year of you in " +
                    "this town. $evening the card's on your " +
                    "journal page.")
        } else null
    }

    // ---------------- the paper ----------------

    /** The birthday week's line for the paper: the day
     *  fell this week, and the party joined it when one
     *  wrapped in the week. Names nothing the town doesn't
     *  know. Null when the week holds no birthday. */
    fun paperLine(state: GameState, week: Int): String? {
        val anchor = anchorDay(state)
        var inWeek = false
        for (d in 0..6) {
            if (isBirthdayDay(week * 7 + d, anchor)) {
                inWeek = true; break
            }
        }
        if (!inWeek) return null
        val partyDay = state.life["pbPartyDay"] ?: -1
        val partyBit = if (partyDay >= 0 &&
            Jobs.weekIndex(partyDay) == week)
            " The crew threw a party at the house, and the " +
                "cake situation was handled."
        else ""
        return "🎂 The new kid's birthday fell this week — " +
            "a card was seen on the counter at the new " +
            "kid's house, and the town's verdict is: many " +
            "happy returns.$partyBit"
    }

    // ---------------- the Journal page ----------------

    /** The 🎂 YOUR BIRTHDAY page: the derivation in plain
     *  words, today's standing (or the countdown), the
     *  kept card, who wished, and the party record. Pure
     *  assembly; the Journal renders. */
    fun pageLines(state: GameState): List<String> {
        val out = ArrayList<String>()
        val anchor = anchorDay(state)
        out += "🎂 YOUR BIRTHDAY"
        out += "Your birthday in this town is kept on the " +
            "anniversary of the day this household's story " +
            "here began — Day ${anchor + 1}, when your " +
            "family came to South Park. It's counted in " +
            "days, not calendar dates ($YEAR_DAYS to the " +
            "year), so leap years can't move it, and no " +
            "form anywhere asks you for a date."
        val reached = birthdaysReached(state)
        when {
            isBirthday(state) -> {
                val k = birthdayNumber(state)
                out += "🎉 TODAY is birthday #$k."
                out += "🎁 This morning: Mom's bake — one " +
                    "real ${(itemById(GIFT_ITEM)?.name ?: "snack cake")} — " +
                    "went into your pack (once; the record " +
                    "stamps it), and a card came in the mail."
                out += when {
                    partyStands(state) ->
                        "🎉 Tonight: your crew throws a party " +
                            "at your house " +
                            "(${Dinners.windowLabel()}) — the " +
                            "arrival, the cake, the send-off. " +
                            "Be home and be celebrated."
                    yieldReason(state).isNotEmpty() ->
                        "🎉 No party tonight: " +
                            "${yieldReason(state)}. The day " +
                            "still counts — the morning, the " +
                            "card, the cake and the wishes " +
                            "are all yours."
                    else ->
                        "🎉 No party tonight — your crew is " +
                            "empty, and a party needs a crew. " +
                            "The household carries the day: " +
                            "your folks, the card, the cake."
                }
                // v0.15.0i: the sharing, on the record —
                // the same words Birthdays stamped this
                // morning (or the live derivation).
                Birthdays.shareLine(state)?.let {
                    out += "📋 Shared on the record: $it"
                }
                val w = wishers(state, k)
                out += if (w.isNotEmpty())
                    "💬 Wishes so far today: " +
                        w.joinToString(", ") + "."
                else
                    "💬 No wishes yet today — word travels " +
                        "by friendship. Talk to people; " +
                        "anyone who knows you knows the day."
            }
            reached == 0 -> {
                out += "· No birthdays yet — the first lands " +
                    "on Day ${anchor + YEAR_DAYS + 1}, a full " +
                    "year in. Nothing is owed before then " +
                    "and nothing arrives retroactively; the " +
                    "calendar just hasn't come around."
            }
            else -> {
                val nextK = reached + 1
                out += "· Birthdays on the record: $reached. " +
                    "The next — #$nextK — lands in " +
                    "${daysUntilBirthday(state)} days " +
                    "(Day ${nextBirthdayDay(state) + 1})."
                val w = wishers(state, reached)
                if (w.isNotEmpty())
                    out += "💬 Who wished you on the last one: " +
                        w.joinToString(", ") + "."
            }
        }
        keptCard(state)?.let { card ->
            out += "✉️ The card from birthday #" +
                "${state.life["pbCardYear"] ?: reached}, " +
                "kept: “$card”"
        }
        if (keptCard(state) == null)
            out += "· No card yet — the first birthday " +
                "brings one, from the household, in the mail."
        val parties = state.life["pbParties"] ?: 0
        out += if (parties > 0)
            "🎉 Birthday parties at your house: $parties — " +
                "the latest: " +
                (state.lifeText["pbLastParty"] ?: "") + "."
        else
            "· No birthday parties yet. They stand on the " +
                "evening when you have a crew and the town " +
                "lets the night be yours."
        return out
    }
}

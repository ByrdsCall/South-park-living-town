package com.hunt3r.southparktown

// ============================================================
// Dinners — v0.6.0d (Queue #6 item 4): family evenings.
//
// The town's households have been eating dinner behind
// their own doors since v0.1 — the 18:00 agenda blocks were
// always a meal. Now the table has a chair for the new kid.
//
//   INVITED   three evenings a week (a pure rotation of the
//             calendar, like the errand board is of the day),
//             one household invites you to THEIR table: the
//             morning report names it, the host texts it,
//             and during the dinner window the family gathers
//             at home by schedule override (the Finales idiom).
//             Three judged beats — the welcome (Talk the
//             host), table talk (Talk any of the family; the
//             family's one small household moment lands here
//             and sticks in memory), and lend a hand (a Look
//             around the room). Leaving early is a shorter,
//             still-warm night; not showing is just a quiet
//             evening the town has without you (no-fail, as
//             ever).
//
//   HOSTED    once your house has a life lived in it (decor)
//             and a household counts you a friend, the dining
//             table at YOUR house can invite them instead.
//             The dinner is judged by what the house actually
//             holds: a pantry item served (consumed), the
//             decor and shelf and album the guests can see,
//             the marks standing in the town you share. A
//             well-hosted night pays best and may leave a
//             thank-you on the table.
//
// Dinners never fight the bigger set-pieces: the invite
// rotation skips a household whose evening collides with a
// live finale (its gathering day, its place), a holiday
// beat at their table, or a case chapter at their house —
// the dinner simply comes around again. And while a case's
// climax is on the table, dinner beats politely wait (the
// finale verdict's own courtesy, mirrored).
//
// State rides the existing `life` / `lifeText` maps (masks,
// day stamps, per-household records), every read clamped
// against the household registry — no new save keys. Pure
// Kotlin, like its siblings.
// ============================================================

/** One household that can hold a dinner. [members] includes
 *  the [host]. Every member's home (KID_HOUSE) is [house] —
 *  the registry is suite-pinned against the cast data, so a
 *  dinner can only ever be called where the family lives. */
data class HouseholdDef(
    val id: String,
    val family: String,          // "the Marshes"
    val house: String,           // location id
    val host: String,
    val members: List<String>,
    val dish: String,            // what's being served
    val ritual: String,          // the household's one ritual
    val welcome: String,         // invited beat 0: the host, at their door
    val moment: String,          // invited beat 1: table talk — the household moment
    val momentShort: String,     // the moment, as memory keeps it
    val hand: String,            // invited beat 2: lend a hand (Look)
    val handShort: String,
    val gatherActivity: String,  // agenda text while gathered at home
    val arrive: String,          // hosted beat 0: the guests at YOUR door
    val guestLine: String        // hosted beat 2 fallback: their table remark
)

/** A dinner beat that just landed — what the rails show. */
data class DinnerHit(
    val def: HouseholdDef,
    val hosted: Boolean,
    val stepIndex: Int,
    val text: String,
    val allLanded: Boolean
)

object Dinners {

    // ---------------- the dials (ledger-named) ----------------

    /** The dinner window: the family's evening hours. Ends
     *  well before the town's late night (22:00) — attendance
     *  respects bedtime by construction. */
    const val DINNER_FROM = 17 * 60 + 30        // 5:30 PM
    const val DINNER_TO = 20 * 60 + 30          // 8:30 PM

    /** absDay % 7 in this set = a dinner evening (three a
     *  week; the rotation walks the households in order). */
    val DINNER_WEEKDAYS: Set<Int> = setOf(1, 3, 5)

    /** Hosting gates: the house must hold at least this many
     *  earned decor pieces, and the household's host must
     *  count you at least a FRIEND (Relationships tier). */
    const val HOST_MIN_DECOR = 4
    const val HOST_MIN_TIER = Relationships.FRIEND

    /** Days before the same household will come to yours
     *  again — even a great table needs a rest. */
    const val HOST_COOLDOWN_DAYS = 10

    /** The spread's score for a WELL-hosted night: served
     *  food (2) + conversation pieces (up to 2) + every beat
     *  landed (1). */
    const val WELL_SCORE = 4

    /** Thank-you odds at a well-hosted table — Errands'
     *  own rail: 1 in 4 completions leaves a thank-you, and
     *  1 in ERRAND_CARD_EVERY of those is a Pocket Pal. */
    const val THANK_EVERY = 4

    /** What your pantry can serve: honest food and drink the
     *  town can actually produce (suite-pinned obtainable
     *  through Errands.sourceOf, like every fetch pool). */
    val PANTRY_ITEMS: List<String> = listOf(
        "snack_cake", "hot_chocolate", "lemonade", "cheesy_poof",
        "tweek_coffee", "fish_trout", "fish_rainbow", "fish_smelt",
        "fish_minnow",
        // v0.11.0b: the garden joins the pantry — produce
        // held is produce servable, listed last so the old
        // spread's habits are unchanged when both are held.
        "pumpkin", "corn", "sunflower", "tomato", "potato")

    /** The thank-you shelf: small good things a grateful
     *  family leaves on your table (gift items all). */
    val THANK_GIFTS: List<String> = listOf(
        "snack_cake", "hot_chocolate", "flower", "comic_book")

    // ---------------- the households ----------------

    val HOUSEHOLDS: List<HouseholdDef> = listOf(

        HouseholdDef(
            id = "marsh", family = "the Marshes",
            house = "StanHouse", host = "Sharon",
            members = listOf("Randy", "Sharon", "Stan", "Shelley"),
            dish = "meatloaf with the good gravy",
            ritual = "Randy says grace. It is about farming. It is always about farming.",
            welcome = "Sharon wipes her hands on her apron and does the thing she " +
                "does instead of panicking: she puts you at the table. " +
                "“Sit. Eat. Randy's grace is short this year — I made him " +
                "promise.” From the kitchen, Randy is already clearing his " +
                "throat like a man tuning an instrument.",
            moment = "Halfway through the meatloaf, Stan goes quiet in the " +
                "specific way that means he's about to be honest. “You guys " +
                "know I can hear the farm meetings from my room, right?” A " +
                "beat. Randy, gently: “Those are lullabies, son.” Sharon " +
                "refills everyone's gravy without a word, which in this " +
                "house is a ruling.",
            momentShort = "Stan can hear the farm meetings from his room. Nobody denied it.",
            hand = "You carry the gravy boat back to the kitchen and find the " +
                "Marsh family archive: a drawer of takeout menus organized by " +
                "cuisine, era, and Randy's confidence level. You wash the good " +
                "pan — the one nobody's allowed to use but everybody does — and " +
                "Sharon nods at it like you've passed a background check.",
            handShort = "Washed the good pan. Passed.",
            gatherActivity = "Family dinner at the Marsh house — grace is farming-related.",
            arrive = "Sharon arrives carrying a side dish she calls “just a " +
                "little something” and which is, structurally, a second " +
                "entrée. Randy is already reviewing your home like a health " +
                "inspector of vibes. Stan mouths 'hi' over the casserole dish " +
                "he's been deputized to hold.",
            guestLine = "Randy looks around your table and renders the verdict " +
                "of a man who judges rooms professionally: “Good bones. " +
                "Honest gravy potential. I approve of this house.”"),

        HouseholdDef(
            id = "broflovski", family = "the Broflovskis",
            house = "KyleHouse", host = "Sheila",
            members = listOf("Gerald", "Sheila", "Kyle", "Ike"),
            dish = "brisket, with the carrots Kyle pretends not to like",
            ritual = "Gerald makes a toast. It has footnotes.",
            welcome = "Sheila opens the door before you knock — she saw you " +
                "from the kitchen window, and the brisket waits for no one. " +
                "“Sit, sit. Gerald! The NEW KID is here, and the brisket is " +
                "at PEAK.” Gerald appears with glasses already raised. Kyle " +
                "mouths 'I'm sorry' with the fluency of long practice.",
            moment = "Over brisket, Gerald raises his glass: “To family — the " +
                "only clients who pay in brisket.” Kyle waits for the catch. " +
                "There isn't one. Sheila puts a hand over Gerald's before he " +
                "can add a footnote, and for one second the Broflovski table " +
                "is the quietest, warmest place in Colorado.",
            momentShort = "Gerald's toast had no catch. Sheila made sure of it.",
            hand = "You help clear and discover the Broflovski junk drawer " +
                "contains: batteries (tested, labeled), a yarmulke with a tag " +
                "from 1994, three generations of takeout menus, and an " +
                "emergency brisket notebook. You dry the good serving spoon. " +
                "Sheila inspects your work and promotes you to 'allowed in " +
                "the kitchen unsupervised.'",
            handShort = "Dried the good spoon. Kitchen clearance: granted.",
            gatherActivity = "Family dinner at the Broflovskis' — the brisket is at peak.",
            arrive = "Sheila arrives with a covered dish and the energy of a " +
                "woman who has brought her own backup gravy 'just in case, " +
                "bubbe.' Gerald carries the good glasses like state secrets. " +
                "Kyle carries the chairs. Kyle always carries the chairs.",
            guestLine = "Gerald raises his glass at your table: “To the new " +
                "kid — the only defendant this family has ever unanimously " +
                "acquitted.” Sheila does not stop the footnote. Tonight, " +
                "nobody stops the footnote."),

        HouseholdDef(
            id = "cartman", family = "the Cartmans",
            house = "CartmanHouse", host = "Liane",
            members = listOf("Liane", "Cartman"),
            dish = "chicken pot pie (one slice is missing; nobody asks)",
            ritual = "Cartman rates the meal out of ten, solemnly, like a judge " +
                "at a trial he is also committing.",
            welcome = "Liane answers with flour on her cheek and a smile like " +
                "a porch light. “Oh, sweetie, come in! Eric, your little " +
                "friend is here!” From the table, already seated, already " +
                "holding a fork: “It's about time. The pie waits for no man. " +
                "Except me. It waited for me.”",
            moment = "Halfway through, Cartman sets his fork down with " +
                "ceremony. “The crust: flaky. The filling: acceptable. The " +
                "company—” he looks at you, and something almost sincere " +
                "crosses his face and is immediately arrested — “—upgraded " +
                "from usual. Seven point five. Do NOT let it go to your " +
                "head.” Liane quietly serves you the biggest remaining slice.",
            momentShort = "Cartman rated the company 7.5. Liane upgraded your slice anyway.",
            hand = "You take plates to the kitchen and learn the Cartman house " +
                "runs on a labelled system: Eric's snacks, Eric's drinks, " +
                "Eric's shelf, and one small shelf labelled 'Mom'. You stack " +
                "the pie tin by the sink. Liane presses a warm cookie into " +
                "your hand 'for the road' and winks like it's contraband.",
            handShort = "Found the shelf labelled 'Mom'. Left with a contraband cookie.",
            gatherActivity = "Family dinner at the Cartmans' — the pie is being rated.",
            arrive = "Liane arrives with a pie and the calm of a woman who " +
                "has already fed her son twice to get him here. Cartman " +
                "arrives like visiting royalty and immediately begins " +
                "inspecting your kitchen with a jeweler's eye for snacks.",
            guestLine = "Cartman surveys your table, your chairs, your entire " +
                "operation, and delivers the review: “Service: prompt. " +
                "Seating: adequate. Host: …acceptable.” From Cartman, this " +
                "is a standing ovation with fireworks."),

        HouseholdDef(
            id = "mccormick", family = "the McCormicks",
            house = "KennysHouse", host = "Carol",
            members = listOf("Stuart", "Carol", "Kenny"),
            dish = "waffle supper — breakfast for dinner, because the coupons said so",
            ritual = "Before anyone touches a waffle, Carol says one true thing " +
                "the house is grateful for.",
            welcome = "Carol opens the door with a dish towel over her " +
                "shoulder and the whole house smelling like a diner at " +
                "sunrise. “Come in out of the cold, baby. Waffles wait for " +
                "nobody and neither does this family.” Kenny, already at the " +
                "table, slides the syrup toward the empty chair like it's a " +
                "chess move.",
            moment = "Carol looks around the little table — Stuart quiet, " +
                "Kenny mid-muffle, the waffles steaming — and says her " +
                "grateful thing: “We're all here. That's the whole list this " +
                "week. We're all here.” Stuart reaches over and tops up her " +
                "coffee without being asked. Kenny says something muffled " +
                "that everyone at the table chooses to hear as amen.",
            momentShort = "'We're all here.' That was the whole grace, and it was enough.",
            hand = "You wash up at a sink that has seen things — a whole " +
                "economy of coupons dries on the rack beside the plates. " +
                "Kenny dries with the good towel, the one with no holes, and " +
                "Carol declares it a special occasion on the spot. Stuart " +
                "starts the little TV for the weather like it's the evening " +
                "news at a palace.",
            handShort = "The good towel came out. It was declared a special occasion.",
            gatherActivity = "Family dinner at the McCormicks' — waffles, breakfast rules.",
            arrive = "Carol arrives with a covered waffle iron's worth of " +
                "batter 'because you never know,' and Stuart carries a " +
                "toolbox out of pure habit. Kenny holds the door for " +
                "everyone, twice, because the door sticks and he knows how " +
                "it likes to be asked.",
            guestLine = "Carol looks around your table, at the food, at her " +
                "family actually sitting still in somebody else's house, " +
                "and says her grateful thing: “Somebody fed US tonight. " +
                "That's the whole list this week.”"),

        HouseholdDef(
            id = "stotch", family = "the Stotches",
            house = "ButtersHouse", host = "Linda",
            members = listOf("Stephen", "Linda", "Butters"),
            dish = "tuna casserole (Stephen has inspected it; it passed)",
            ritual = "Before dessert, Stephen reviews the day's groundings. It " +
                "is a short segment. It is never a short segment.",
            welcome = "Linda opens the door in an apron that says KISS THE " +
                "COOK in letters Stephen has definitely read aloud for " +
                "compliance. “Well, aren't you punctual! Butters, our guest " +
                "is here — the one we practised for!” Butters appears at " +
                "parade rest, vibrating slightly: “H-hello! Welcome to our " +
                "home! I dusted the doorknobs!”",
            moment = "At dessert, Stephen clears his throat for the grounding " +
                "review — and looks at Butters, who has been good in four " +
                "counties tonight, including helping set the table AND not " +
                "humming. “…No groundings,” Stephen says, like the words are " +
                "new. “Carry on.” Butters glows like a porch light. Linda " +
                "serves him the corner piece.",
            momentShort = "No groundings. A first. Butters got the corner piece.",
            hand = "You help Linda stack the casserole dishes and discover " +
                "the Stotch pantry is labelled by meal, day, and emergency " +
                "level. You are trusted with the good gravy boat AND the " +
                "knowledge of where the batteries are kept. On your way " +
                "out, Stephen shakes your hand with both of his, like a treaty.",
            handShort = "Trusted with the battery drawer. A treaty was signed.",
            gatherActivity = "Family dinner at the Stotch house — casserole inspected, passed.",
            arrive = "Linda arrives with a casserole in a carrier with its " +
                "own seatbelt. Stephen arrives with a small speech of " +
                "thanks, pre-approved. Butters arrives holding a place card " +
                "he made for himself, in case it was that kind of dinner. " +
                "It is now.",
            guestLine = "Butters looks around your table with the wonder of a " +
                "kid at a state dinner: “Gee whiz, a place card an' " +
                "everything… I mean, there ISN'T one, but it FEELS like " +
                "there is. That's class, fellas.”"),

        HouseholdDef(
            id = "black", family = "the Blacks",
            house = "TolkienHouse", host = "Tolkien",
            members = listOf("Tolkien", "Steve", "Linda Black"),
            dish = "the chef's reheated Sunday roast (the chef has Sundays off; " +
                "Tolkien has notes)",
            ritual = "Tolkien chooses the dinner music and delivers a short " +
                "defence of it. The defence is airtight.",
            welcome = "Tolkien answers his own door, because the staff have " +
                "the evening and because, he explains, answering doors is " +
                "'a host's one non-delegable job.' Behind him the house is " +
                "warm and enormous and set for three: his dad Steve is home " +
                "tonight — no fundraiser, no board call, a genuine rarity " +
                "that Tolkien carries like good weather. “We are saving " +
                "this roast. Sit.”",
            moment = "Over the roast, Tolkien looks down the long table — " +
                "three settings, twelve chairs — and says, without any " +
                "self-pity at all: “People think a big house eats better. " +
                "It doesn't. It just echoes better.” Then he turns the " +
                "music up one notch, on purpose, and the echo loses.",
            momentShort = "'A big house doesn't eat better. It just echoes better.'",
            hand = "You help clear into a kitchen the size of your whole " +
                "house, where the leftovers go into containers labelled by " +
                "a system Tolkien designed at age seven and has never " +
                "revised. He shows you the pantry door, inside which " +
                "generations of Blacks have marked their heights — his mark " +
                "is annotated: 'growth spurt, alleged.'",
            handShort = "Saw the height marks. The growth spurt remains alleged.",
            gatherActivity = "Dinner at the Black house — three settings, twelve chairs.",
            arrive = "Tolkien arrives carrying a bottle of something sparkling " +
                "and non-negotiable, and a small speaker, because 'a table " +
                "without music is just furniture eating.' He surveys your " +
                "house the way he surveys everything: fairly.",
            guestLine = "Tolkien turns the music up one notch in your kitchen, " +
                "on purpose. “There,” he says. “Now the echo's losing here " +
                "too.”"),

        HouseholdDef(
            id = "tweek", family = "the Tweeks",
            house = "TweekBros", host = "Mrs. Tweek",
            members = listOf("Mr. Tweek", "Mrs. Tweek", "Tweek"),
            dish = "pie, and coffee that is not a beverage but a family member",
            ritual = "After the plates, Mr. Tweek tells a coffee parable. It " +
                "lasts exactly as long as the pour.",
            welcome = "Mrs. Tweek flips the sign to CLOSED with you on the " +
                "inside of it, which she explains is the whole honour " +
                "system. “Family supper, hon. Shop's ours now.” Tweek is " +
                "hand-drying a mug that is already dry. Mr. Tweek emerges " +
                "from the back with the good beans and the air of a man " +
                "about to perform a small sacrament.",
            moment = "Halfway through the pie, Tweek's hands — mid-vibrate " +
                "since roughly October — go still around his mug. His " +
                "mother notices and says nothing, which is how you know " +
                "it's the biggest thing she's said all day. Mr. Tweek pours " +
                "the quiet a refill. “Pressure's just love, extracted,” he " +
                "says, softly. Tweek almost smiles into the steam.",
            momentShort = "Tweek's hands went still around the mug. Nobody made it weird.",
            hand = "You help wash the shop's real dishes (as opposed to the " +
                "display mugs, which are actors). Behind the counter: the " +
                "emergency pie, the other emergency pie, and a laminated " +
                "card in Mrs. Tweek's handwriting: IN CASE OF PANIC — LIST " +
                "FIRST. You dry the family mugs. They go on the high shelf, " +
                "where the heirlooms live.",
            handShort = "Dried the family mugs. High shelf. Heirloom status.",
            gatherActivity = "Family supper at Tweek Bros — the sign says CLOSED, " +
                "the table says otherwise.",
            arrive = "Mrs. Tweek arrives with a pie in a box tied with string " +
                "and the good beans in a second box, also tied. Mr. Tweek " +
                "arrives already mid-parable. Tweek arrives carrying three " +
                "mugs 'in case of mugs,' and relaxes visibly when your " +
                "kitchen turns out to be mug-equipped.",
            guestLine = "Mr. Tweek holds your kitchen's steam like a wine " +
                "glass. “A calm house,” he pronounces. “You can taste it " +
                "in the coffee. This coffee can taste it in itself.”")
    )

    fun defById(id: String?): HouseholdDef? =
        HOUSEHOLDS.firstOrNull { it.id == id }

    /** The household a cast member eats with, if any. */
    fun householdOf(npc: String): HouseholdDef? =
        HOUSEHOLDS.firstOrNull { npc in it.members }

    /** Beat titles per kind, for the Talk/Look dialogs. */
    val INVITED_TITLES = listOf("The Welcome", "Table Talk", "Lend a Hand")
    val HOSTED_TITLES = listOf("The Welcome In", "The Spread", "Over the Table")
    const val STEP_COUNT = 3

    // ---------------- state keys (life / lifeText) ----------------
    // din* = the invited dinner; host* = the hosted one. One
    // dinner of each kind can exist per day, so one (day,
    // mask) slot per kind is the whole storage. Records are
    // per-household and read back clamped to the registry.

    private const val K_INVITE_ID = "dinInviteId"       // lifeText: today's invited household ("" = none)
    private const val K_INVITE_DAY = "dinInviteDay"     // life: absDay the invite record belongs to
    private const val K_MASK = "dinMask"                // life: invited beats landed (bits)
    private const val K_MASK_DAY = "dinDay"             // life: absDay the invited mask belongs to
    private const val K_HOST_ID = "hostHousehold"       // lifeText: tonight's hosted household
    private const val K_HOST_DAY = "hostDay"            // life: absDay hosting is scheduled (-1 = none)
    private const val K_HOST_MASK = "hostMask"          // life: hosted beats landed (bits)
    private const val K_SERVED = "hostServed"           // lifeText: pantry item id served tonight

    private fun kAttended(id: String) = "dinAttended:$id"
    private fun kLast(id: String) = "dinLast:$id"
    private fun kNight(id: String) = "dinNight:$id"
    private fun kMoment(id: String) = "dinMoment:$id"
    private fun kWrapDay(id: String) = "dinWrapDay:$id"
    private fun kOutcome(id: String) = "dinOutcome:$id"
    private fun kHosted(id: String) = "dinHosted:$id"
    private fun kHostLast(id: String) = "hostLast:$id"
    private fun kHostNight(id: String) = "hostNight:$id"
    private fun kHostWrapDay(id: String) = "hostWrapDay:$id"
    private fun kHostOutcome(id: String) = "hostOutcome:$id"

    // ---------------- the rotation (invited dinners) ----------------

    fun isDinnerDay(absDay: Int): Boolean =
        ((absDay % 7) + 7) % 7 in DINNER_WEEKDAYS

    /** How many dinner evenings fall in [0, absDay) — the
     *  rotation's odometer. Pure calendar math. */
    fun dinnerDaysBefore(absDay: Int): Int {
        if (absDay <= 0) return 0
        val weeks = absDay / 7
        var n = weeks * DINNER_WEEKDAYS.size
        for (d in 0 until (absDay % 7)) if (d in DINNER_WEEKDAYS) n++
        return n
    }

    /** Every case chapter/finale def id across the four arcs:
     *  a chapter night at a family's house is the case's
     *  night, not the dinner's. */
    val CASE_DEF_IDS: Set<String> =
        (Mystery.ARC_CHAPTERS + Mystery2.BEATS + Mystery3.CHAPTERS3 +
            Mystery4.CHAPTERS4).map { it.id }.toSet()

    /** Would dinner at [def]'s house tonight collide with a
     *  bigger set-piece? The finale's gathering day (its
     *  ensemble is spoken for) or its place (any window day),
     *  a holiday beat that plays at their table or through
     *  their people, or a case chapter whose cast is their
     *  family (or which gathers at their house). Day-stable
     *  inputs only (calendar and ids) — the invite derived
     *  at breakfast is the invite all day. */
    fun collides(state: GameState, def: HouseholdDef): Boolean {
        // a season finale owns its place all week, and its
        // people on the gathering days. (Deliberately NOT
        // gated on the finale being opened yet: the dinner
        // rollover anchors the day's invitation in the same
        // midnight pass that opens the finale, and the
        // anchor must see the week coming. An opening
        // deferred a day by a case climax is covered the
        // other way — dinner beats wait while a climax is
        // due, via beatsDeferred.)
        val w = Finales.windowAt(state)
        if (w != null &&
            !Finales.isWrapped(state, w.def, w.instanceYear)) {
            if (w.def.loc == def.house) return true
            if ((w.dayIndex == 0 || w.dayIndex == Finales.WINDOW_DAYS - 1) &&
                w.def.ensemble.any { it in def.members }) return true
        }
        // a holiday beat at their table, or through their family
        val beat = HolidayBeats.activeBeat(state)
        if (beat != null && beat.steps.any {
                it.loc == def.house || it.houseLoc == def.house ||
                    (it.who != null && it.who in def.members)
            }) return true
        // a case chapter whose CAST is this family (its
        // causers) — the chapter owns their evening; or a
        // chapter gathered at their house itself
        val shen = state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }
        if (shen != null && shen.id in CASE_DEF_IDS &&
            (shen.causers.any { it in def.members } ||
                shen.stages.any { it.clusterLoc == def.house })) return true
        return false
    }

    /** Tonight's invitation, derived: on a dinner evening,
     *  the rotation's household — scanning forward past any
     *  household whose evening collides (they'll come around
     *  again). Null on off evenings, and null if the whole
     *  rotation is spoken for (a very busy night in town). */
    fun inviteFor(state: GameState): HouseholdDef? {
        if (!isDinnerDay(state.absDay)) return null
        val n = dinnerDaysBefore(state.absDay)
        for (k in HOUSEHOLDS.indices) {
            val cand = HOUSEHOLDS[(n + k) % HOUSEHOLDS.size]
            if (!collides(state, cand)) return cand
        }
        return null
    }

    /** The invite the game is actually running today: the
     *  record the rollover anchored at breakfast (immune to
     *  mid-day state churn), or the live derivation for a
     *  state that never had a rollover (a brand-new save). */
    fun todaysInvite(state: GameState): HouseholdDef? {
        if ((state.life[K_INVITE_DAY] ?: -1) == state.absDay) {
            val id = state.lifeText[K_INVITE_ID] ?: ""
            return if (id.isEmpty()) null else defById(id)
        }
        return inviteFor(state)
    }

    /** The ANCHORED invite only — no fallback. The phone's
     *  morning harvest reads this: a text is a fact, and the
     *  fact is the record the real rollover wrote. A state
     *  that never had its dinner rollover (a fresh save's
     *  first morning, a fabricated one) gets no dinner text
     *  invented for it. */
    fun anchoredInvite(state: GameState): HouseholdDef? {
        if ((state.life[K_INVITE_DAY] ?: -1) != state.absDay) return null
        val id = state.lifeText[K_INVITE_ID] ?: ""
        return if (id.isEmpty()) null else defById(id)
    }

    /** The household coming to YOUR house tonight, if you've
     *  scheduled one for today. */
    fun hostingTonight(state: GameState): HouseholdDef? {
        if ((state.life[K_HOST_DAY] ?: -1) != state.absDay) return null
        return defById(state.lifeText[K_HOST_ID])
    }

    fun windowOpen(state: GameState): Boolean =
        state.timeMin in DINNER_FROM until DINNER_TO

    /** The window as the town says it: "5:30 PM–8:30 PM". */
    fun windowLabel(): String =
        "${minutesToTimeStr(DINNER_FROM)}–${minutesToTimeStr(DINNER_TO)}"

    /** While a case's climax is on the table, dinner beats
     *  wait — two player-facing moments never stack (the
     *  finale verdict's own rule, mirrored). */
    fun beatsDeferred(state: GameState): Boolean =
        Finales.climaxPending(state)

    // ---------------- records (all reads clamped) ----------------

    fun attendedCount(state: GameState, def: HouseholdDef): Int =
        (state.life[kAttended(def.id)] ?: 0).coerceIn(0, 9999)

    fun hostedCount(state: GameState, def: HouseholdDef): Int =
        (state.life[kHosted(def.id)] ?: 0).coerceIn(0, 9999)

    fun lastDinnerDay(state: GameState, def: HouseholdDef): Int =
        state.life[kLast(def.id)] ?: -1

    /** Did the player actually spend [absDay]'s evening at
     *  a family table — theirs (attended, at least one beat
     *  in) or yours (hosted, at least one beat in)? The wrap
     *  records are the evidence; a no-show wraps nothing.
     *  (v0.6.0e: Jobs' excuse rule reads this — a family
     *  table is one of the town's legitimate claims on an
     *  evening.) */
    fun eveningSpent(state: GameState, absDay: Int): Boolean =
        HOUSEHOLDS.any {
            (state.life[kWrapDay(it.id)] ?: -1) == absDay ||
                (state.life[kHostWrapDay(it.id)] ?: -1) == absDay
        }

    fun nightLine(state: GameState, def: HouseholdDef): String? =
        state.lifeText[kNight(def.id)]?.takeIf { it.isNotEmpty() }

    fun hostNightLine(state: GameState, def: HouseholdDef): String? =
        state.lifeText[kHostNight(def.id)]?.takeIf { it.isNotEmpty() }

    fun momentOf(state: GameState, def: HouseholdDef): String? =
        state.lifeText[kMoment(def.id)]?.takeIf { it.isNotEmpty() }

    fun totalAttended(state: GameState): Int =
        HOUSEHOLDS.sumOf { attendedCount(state, it) }

    fun totalHosted(state: GameState): Int =
        HOUSEHOLDS.sumOf { hostedCount(state, it) }

    // ---------------- the beats ----------------

    private fun invitedMask(state: GameState): Int =
        if ((state.life[K_MASK_DAY] ?: -1) == state.absDay)
            (state.life[K_MASK] ?: 0) and 0b111 else 0

    private fun hostedMask(state: GameState): Int =
        if ((state.life[K_HOST_DAY] ?: -1) == state.absDay)
            (state.life[K_HOST_MASK] ?: 0) and 0b111 else 0

    fun invitedStepsDone(state: GameState): Int =
        Integer.bitCount(invitedMask(state))

    fun hostedStepsDone(state: GameState): Int =
        Integer.bitCount(hostedMask(state))

    /** The first unlanded beat index, or null when the
     *  dinner's beats are all in. */
    private fun currentStep(mask: Int): Int? =
        (0 until STEP_COUNT).firstOrNull { mask and (1 shl it) == 0 }

    /** The live dinner a Talk with [npc] would advance, and
     *  at which beat — or null. Pure query (the gate calls
     *  this); judging is judgeTalk's. The hosted dinner wins
     *  the evening: if you've got guests coming, the invite
     *  across town waits (and lapses quietly). */
    fun talkStepFor(state: GameState, npc: String): DinnerHit? {
        if (!windowOpen(state) || beatsDeferred(state)) return null
        val hosted = hostingTonight(state)
        if (hosted != null) {
            return when (currentStep(hostedMask(state))) {
                0 -> if (npc == hosted.host)
                    DinnerHit(hosted, true, 0, "", false) else null
                2 -> if (npc in hosted.members)
                    DinnerHit(hosted, true, 2, "", false) else null
                else -> null
            }
        }
        val inv = todaysInvite(state) ?: return null
        return when (currentStep(invitedMask(state))) {
            0 -> if (npc == inv.host)
                DinnerHit(inv, false, 0, "", false) else null
            1 -> if (npc in inv.members)
                DinnerHit(inv, false, 1, "", false) else null
            else -> null
        }
    }

    private fun fire(state: GameState, hit: DinnerHit): DinnerHit {
        val text: String
        if (hit.hosted) {
            state.life[K_HOST_MASK] = hostedMask(state) or (1 shl hit.stepIndex)
            text = when (hit.stepIndex) {
                0 -> hit.def.arrive
                1 -> serveText(state, hit.def)
                else -> hostedCommentText(state, hit.def)
            }
            val m = hostedMask(state)
            return hit.copy(text = text,
                allLanded = m == 0b111)
        }
        state.life[K_MASK_DAY] = state.absDay
        state.life[K_MASK] = invitedMask(state) or (1 shl hit.stepIndex)
        text = when (hit.stepIndex) {
            0 -> hit.def.welcome
            1 -> {
                // the household moment lands at the table —
                // and sticks, in the family's own record
                state.lifeText[kMoment(hit.def.id)] = hit.def.momentShort
                hit.def.moment
            }
            else -> hit.def.hand
        }
        val m = invitedMask(state)
        return hit.copy(text = text, allLanded = m == 0b111)
    }

    /** TALK judged (the Talk gate calls this on the DINNER
     *  rail, like a finale beat through its judge). */
    fun judgeTalk(state: GameState, npc: String): DinnerHit? {
        val hit = talkStepFor(state, npc) ?: return null
        return fire(state, hit)
    }

    /** LOOK judged: the invited dinner's lend-a-hand beat at
     *  the family's house, or the hosted dinner's spread at
     *  yours. Returns the beat's text, or null. */
    fun lookText(state: GameState, locId: String): String? {
        if (!windowOpen(state) || beatsDeferred(state)) return null
        val hosted = hostingTonight(state)
        if (hosted != null) {
            if (locId != YOUR_HOUSE) return null
            if (currentStep(hostedMask(state)) != 1) return null
            return fire(state, DinnerHit(hosted, true, 1, "", false)).text
        }
        val inv = todaysInvite(state) ?: return null
        if (locId != inv.house) return null
        if (currentStep(invitedMask(state)) != 2) return null
        return fire(state, DinnerHit(inv, false, 2, inv.hand, false)).text
    }

    // ---------------- the hosted dinner: the judging ----------------

    /** What the guests can honestly remark on at YOUR table:
     *  lines composed only from state that is actually true
     *  right now — the decor on your walls, the shelf's
     *  count, the album's weight, the town's marks. A line
     *  is in this list only if its fact holds. */
    fun conversationFacts(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val decor = House.decorFor(state)
        if (decor.isNotEmpty()) {
            val piece = House.DECOR.first { it.id in decor }
            out += "“The ${piece.name.lowercase()} — earned, not bought. " +
                "You can tell. A room keeps its receipts.”"
        }
        val shelf = House.shelfSlots(state).count { it != null }
        if (shelf >= 6) {
            out += "“Counted the shelf on the way in. $shelf treasures, " +
                "arranged like they matter. They do.”"
        }
        val cups = House.featCups(state)
        if (cups >= 1) {
            out += "“Are those feat CUPS? Actual cups, for actual feats. " +
                "The town gives out so few of those.”"
        }
        val prints = state.photos.count { it.isCommissioned }
        if (prints >= 1) {
            out += "“You've got commissioned prints in that album — " +
                "people ASKED you for those. The town has a photographer.”"
        } else if (state.photos.size >= 10) {
            out += "“That album of yours is getting thick. Half the " +
                "town's in it by now, one way or another.”"
        }
        val marks = Finales.DEFS.count { Finales.markOf(state, it) != null }
        if (marks >= 1) {
            out += "“Every season in this town ends standing somewhere " +
                "because of a call somebody made. Some of those calls " +
                "were yours. We remember.”"
        }
        if (decor.any { it.startsWith("mystery_") || it.startsWith("finale_") }) {
            out += "“Some of what's on your walls, most people only hear " +
                "about. …We don't ask. We just nod, respectfully.”"
        }
        return out
    }

    /** The spread's score, as the wrap will judge it: the
     *  pantry item served is worth 2, the house's honest
     *  conversation pieces up to 2, and a full set of beats
     *  1. Computed live — the suite pins it against the same
     *  facts the guests remark on. [day] is the dinner's own
     *  day (the wrap computes it after midnight, when the
     *  calendar has already turned). */
    fun hostedScore(state: GameState, beats: Int,
                    day: Int = state.absDay): Int {
        var score = 0
        if ((state.lifeText[K_SERVED] ?: "").isNotEmpty() &&
            (state.life[K_HOST_DAY] ?: -1) == day) score += 2
        score += minOf(2, conversationFacts(state).size)
        if (beats >= STEP_COUNT) score += 1
        return score
    }

    /** Hosted beat 2's text: the guests talk about what's
     *  actually in the house — the facts, or the family's
     *  own table line when the walls are still bare.
     *  v0.9.0d: a wall print that features THIS household
     *  speaks first (Darkroom's fact-slot lines, audited
     *  like the facts) — but never crowds the house's own
     *  facts out entirely: one print line, then the facts. */
    fun hostedCommentText(state: GameState, def: HouseholdDef): String {
        val facts = conversationFacts(state)
        val printLines = Darkroom.guestPrintLines(state, def)
        val lines = (printLines.take(1) + facts).take(2)
        return if (lines.isEmpty()) def.guestLine
        else lines.joinToString("\n")
    }

    /** Hosted beat 1's text: the spread. Serves (and
     *  consumes) the first stocked pantry item, if any —
     *  the kitchen is judged on what it actually holds. */
    private fun serveText(state: GameState, def: HouseholdDef): String {
        val itemId = PANTRY_ITEMS.firstOrNull {
            (state.inventory[it] ?: 0) > 0
        }
        return if (itemId != null) {
            val held = state.inventory[itemId] ?: 0
            if (held == 1) state.inventory.remove(itemId)
            else state.inventory[itemId] = held - 1
            state.lifeText[K_SERVED] = itemId
            val name = itemById(itemId)?.name ?: "something good"
            // v0.11.0b: a homegrown serving is a FACT at the
            // table, stated in the same discipline as every
            // other served dish.
            val home = if (Garden.isProduce(itemId))
                " And it's homegrown — out of the new kid's " +
                    "own garden, which the table treats as " +
                    "breaking news." else ""
            "You bring out the $name, and the table does the little " +
                "murmur tables do when the food turns out to be real. " +
                "${def.host} accepts a serving with the gravity of a " +
                "head of state. The spread is officially a spread." +
                home
        } else {
            state.lifeText[K_SERVED] = ""
            "The cupboard, honestly assessed, offers atmosphere and " +
                "confidence. ${def.host} — a professional — pretends " +
                "this was the plan all along and compliments the " +
                "seasoning on the conversation. Note for next time: " +
                "a stocked pantry is a love language."
        }
    }

    // ---------------- hosting: eligibility & scheduling ----------------

    data class HostOption(
        val def: HouseholdDef,
        val eligible: Boolean,
        val reason: String       // why not, in the town's voice ("" when eligible)
    )

    /** Days until [def] could be hosted again (0 = now). */
    fun hostCooldownLeft(state: GameState, def: HouseholdDef): Int {
        val last = state.life[kHostLast(def.id)] ?: -1
        if (last < 0) return 0
        return (HOST_COOLDOWN_DAYS - (state.absDay - last)).coerceAtLeast(0)
    }

    /** The table at your house, household by household:
     *  who's eligible tonight and why the rest aren't — the
     *  dialog and the suite read the same list. */
    fun hostOptions(state: GameState): List<HostOption> {
        val decorCount = House.decorFor(state).size
        val tonight = todaysInvite(state)
        val already = hostingTonight(state)
        return HOUSEHOLDS.map { def ->
            val opt = when {
                already != null ->
                    HostOption(def, false,
                        "You've already got ${already.family} coming tonight.")
                decorCount < HOST_MIN_DECOR ->
                    HostOption(def, false,
                        "Your house is still more address than home — " +
                            "$decorCount of $HOST_MIN_DECOR decor pieces earned. " +
                            "Live a little first; the table will keep.")
                Relationships.tierOf(state.friendship[def.host] ?: 0) <
                    HOST_MIN_TIER ->
                    HostOption(def, false,
                        "You and ${def.host} aren't quite there yet — " +
                            "hosting is friends' work. Get to " +
                            "${Relationships.tierLabel(30)} first.")
                hostCooldownLeft(state, def) > 0 ->
                    HostOption(def, false,
                        "You hosted ${def.family} ${state.absDay - (state.life[kHostLast(def.id)] ?: state.absDay)} " +
                            "days ago — even a great table rests " +
                            "(${hostCooldownLeft(state, def)} days to go).")
                tonight != null && tonight.id == def.id ->
                    HostOption(def, false,
                        "They've invited YOU tonight — go to theirs. " +
                            "It's their turn to wash up.")
                collides(state, def) ->
                    HostOption(def, false,
                        "Tonight's spoken for in their world — bigger " +
                            "town business has their evening.")
                else -> HostOption(def, true, "")
            }
            opt
        }
    }

    fun hostEligible(state: GameState, id: String): Boolean =
        hostOptions(state).firstOrNull { it.def.id == id }?.eligible == true

    /** Invite [householdId] to yours for TONIGHT. Validates
     *  through hostOptions — the same list the dialog shows. */
    fun scheduleHosting(state: GameState, householdId: String): Boolean {
        val opt = hostOptions(state).firstOrNull { it.def.id == householdId }
            ?: return false
        if (!opt.eligible) return false
        state.life[K_HOST_DAY] = state.absDay
        state.lifeText[K_HOST_ID] = householdId
        state.life[K_HOST_MASK] = 0
        state.lifeText[K_SERVED] = ""
        note(state, "🍽 Tonight: ${opt.def.family} are coming to YOUR " +
            "house for dinner. The table's yours to set.")
        return true
    }

    /** Call tonight's hosting off before it starts (no beats
     *  landed, no cooldown spent — plans are plans). */
    fun cancelHosting(state: GameState): Boolean {
        if (hostingTonight(state) == null) return false
        if (hostedMask(state) != 0) return false
        state.life[K_HOST_DAY] = -1
        state.lifeText[K_HOST_ID] = ""
        state.life[K_HOST_MASK] = 0
        return true
    }

    // ---------------- the gathering (schedule override) ----------------

    /** During the dinner window, a household is AT its table:
     *  the invited family at home, your guests at your house.
     *  The Engine's agenda lookup asks here before the finale
     *  gathering — a finale's claim on its people still wins
     *  (dinners defer to finales in the rotation already;
     *  this is the belt to that suspender). */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in DINNER_FROM until DINNER_TO) return null
        val hosted = hostingTonight(state)
        if (hosted != null) {
            if (npcName in hosted.members) {
                return AgendaBlock(YOUR_HOUSE,
                    "Dinner at the new kid's house — being hosted, " +
                        "and judging it kindly but honestly.")
            }
            return null
        }
        val inv = todaysInvite(state) ?: return null
        if (npcName !in inv.members) return null
        return AgendaBlock(inv.house, inv.gatherActivity)
    }

    // ---------------- lifecycle (the Engine's rollover calls) ----------------

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** Midnight: yesterday's dinners resolve, and today's
     *  invitation is anchored into the record before the
     *  phone's morning harvest reads it. Called by
     *  Engine.onNewDay BEFORE Phone.dailyRollover (the
     *  thanks texts derive from the wrap records below). */
    fun dailyRollover(state: GameState) {
        // ---- wrap yesterday's hosted dinner ----
        // (read first: a hosting scheduled for the same day
        // SUPERSEDES the invited dinner — its beats may be
        // stamped, but the invitation lapsed the moment you
        // took guests of your own; only the hosted wrap pays)
        val hostDay = state.life[K_HOST_DAY] ?: -1
        val hostedThatDay = hostDay >= 0 && hostDay < state.absDay
        if (hostedThatDay) {
            defById(state.lifeText[K_HOST_ID])?.let { def ->
                wrapHosted(state, def,
                    (state.life[K_HOST_MASK] ?: 0) and 0b111, hostDay)
            }
            state.life[K_HOST_DAY] = -1
            state.life[K_HOST_MASK] = 0
            state.lifeText[K_HOST_ID] = ""
            state.lifeText[K_SERVED] = ""
        }
        // ---- wrap yesterday's invited dinner ----
        // (skipped when the same day was hosted instead —
        // see the supersede note above)
        val maskDay = state.life[K_MASK_DAY] ?: -1
        val superseded = hostedThatDay && hostDay == maskDay
        if (!superseded && maskDay >= 0 && maskDay < state.absDay &&
            (state.life[K_INVITE_DAY] ?: -1) == maskDay) {
            defById(state.lifeText[K_INVITE_ID])?.let { def ->
                wrapAttended(state, def,
                    (state.life[K_MASK] ?: 0) and 0b111, maskDay)
            }
        }
        if (maskDay in 0 until state.absDay) {
            state.life[K_MASK_DAY] = -1
            state.life[K_MASK] = 0
        }
        // ---- anchor today's invitation ----
        state.lifeText[K_INVITE_ID] = inviteFor(state)?.id ?: ""
        state.life[K_INVITE_DAY] = state.absDay
    }

    /** An invited dinner resolves: the full evening pays
     *  across the household, a partial one pays less and
     *  still warmly. (A no-show never reaches here — no
     *  beat landed, no mask day was ever stamped, nothing
     *  is recorded. The town just had its evening.) */
    private fun wrapAttended(state: GameState, def: HouseholdDef,
                             mask: Int, dinnerDay: Int) {
        val beats = Integer.bitCount(mask)
        if (beats == 0) return
        val full = beats >= STEP_COUNT
        val eng = Engine()
        eng.relAdjust(state, def.host, if (full) 3 else 2)
        for (name in def.members) {
            if (name != def.host) eng.relAdjust(state, name, if (full) 2 else 1)
        }
        state.life[kAttended(def.id)] = attendedCount(state, def) + 1
        state.life[kLast(def.id)] = dinnerDay
        state.life[kWrapDay(def.id)] = dinnerDay
        state.lifeText[kOutcome(def.id)] = if (full) "full" else "early"
        state.lifeText[kNight(def.id)] = if (full)
            "A full evening at ${def.family}'s — ${def.dish}. ${def.handShort}"
        else
            "A short, warm stop at ${def.family}'s — ${def.dish}."
        // the night sticks: a memory in the family's own
        // heads, and a thread the conversation banks can
        // follow up on
        val memory = if (mask and 0b010 != 0)
            "🍽 Dinner at ours — ${def.momentShort}"
        else
            "🍽 The new kid came to dinner at ours."
        for (name in def.members) {
            state.agents[name]?.memories?.add(memory)
            val mem = NpcMemoryStore.getOrCreate(name)
            mem.topics.remove(TOPIC)
            mem.topics.add(TOPIC)
            while (mem.topics.size > 6) mem.topics.removeAt(0)
        }
        note(state, if (full)
            "🍽 Dinner at ${def.family}'s: you stayed for the whole " +
                "thing — ${def.ritual}"
        else
            "🍽 You slipped out of ${def.family}'s dinner early — " +
                "warmly. The leftovers understood.")
        townLogLine(state, "🍽 Dinner at ${def.family}'s — " +
            if (full) "the full evening" else "a short, warm stop")
    }

    /** A hosted dinner resolves: scored on what the house
     *  actually held, paid like the bigger occasion it is,
     *  and — on a well-hosted night — leaving a thank-you
     *  on the table at the errands' own odds. A stood-up
     *  table (scheduled, never played) spends the
     *  household's cooldown and nothing else. */
    private fun wrapHosted(state: GameState, def: HouseholdDef,
                           mask: Int, dinnerDay: Int) {
        val beats = Integer.bitCount(mask)
        state.life[kHostLast(def.id)] = dinnerDay
        if (beats == 0) {
            state.lifeText[kHostNight(def.id)] =
                "They came to yours. You weren't quite there. The " +
                    "porch light did its best."
            note(state, "🍽 ${def.family} came to dinner at yours… " +
                "and you weren't there for it. The chairs kept the " +
                "secret, but the porch light saw everything.")
            return
        }
        val score = hostedScore(state, beats, dinnerDay)
        val well = beats >= STEP_COUNT && score >= WELL_SCORE
        val eng = Engine()
        eng.relAdjust(state, def.host, if (well) 4 else 2)
        for (name in def.members) {
            if (name != def.host) eng.relAdjust(state, name, if (well) 3 else 2)
        }
        state.life[kHosted(def.id)] = hostedCount(state, def) + 1
        state.life[kHostWrapDay(def.id)] = dinnerDay
        state.lifeText[kHostOutcome(def.id)] = if (well) "well" else "fine"
        val servedName = (state.lifeText[K_SERVED] ?: "")
            .let { if (it.isEmpty()) null else itemById(it)?.name }
        state.lifeText[kHostNight(def.id)] = if (well)
            "You hosted ${def.family} and it landed — " +
                (servedName?.let { "the $it, " } ?: "") +
                "the full table, the whole evening."
        else
            "You hosted ${def.family} — a good night at your table."
        for (name in def.members) {
            state.agents[name]?.memories?.add(
                "🍽 Dinner at the new kid's house." +
                    if (well) " It was a whole occasion." else "")
        }
        var giftLine = ""
        if (well) {
            val h = ((def.id.hashCode() and 0x3fffffff) + dinnerDay)
            if (h % THANK_EVERY == 0) {
                giftLine = if ((h / THANK_EVERY) % PocketPals.ERRAND_CARD_EVERY == 0) {
                    val card = PocketPals.errandCard(h)
                    PocketPals.grantCard(state, card,
                        "a thank-you from ${def.family}")
                    " They left a Pocket Pal on the table — ${card.name}."
                } else {
                    val gid = THANK_GIFTS[(h / THANK_EVERY) % THANK_GIFTS.size]
                    state.inventory[gid] = (state.inventory[gid] ?: 0) + 1
                    " They left you a ${itemById(gid)?.name ?: "little something"} " +
                        "on the table, wrapped in a napkin."
                }
            }
        }
        note(state, "🍽 You hosted ${def.family} at yours — " +
            (if (well) "and it was a whole occasion." else "a good night.") +
            giftLine)
        townLogLine(state, "🍽 Hosted ${def.family} for dinner — " +
            if (well) "a whole occasion" else "a good night")
    }

    /** The memory topic an attended dinner plants (Threads
     *  keeps a follow-up bank for it). */
    const val TOPIC = "dinner"

    // ---------------- conversation & notices ----------------

    /** The morning report's line: tonight's invitation, on
     *  the morning it can be acted on. (Hosting is scheduled
     *  during the day at your own table — it announces
     *  itself then, and on the Journal page.) */
    fun morningNote(state: GameState): String? {
        val inv = todaysInvite(state) ?: return null
        val locName = LOCATIONS[inv.house]?.name ?: inv.house
        return "🍽 Dinner invitation: ${inv.family} have asked you to " +
            "$locName tonight (${windowLabel()}) — ${inv.host} is making " +
            "${inv.dish}. Just show up hungry."
    }

    /** Phone derivations read these: the wrap of last
     *  night's dinner, for the host's morning-after text.
     *  Returns (household, "attended"|"hosted") for a dinner
     *  that wrapped on [day], else null. */
    fun wrapFor(state: GameState, day: Int): Pair<HouseholdDef, String>? {
        for (def in HOUSEHOLDS) {
            if ((state.life[kWrapDay(def.id)] ?: -1) == day &&
                (state.lifeText[kOutcome(def.id)] ?: "") in setOf("full", "early"))
                return def to "attended"
            if ((state.life[kHostWrapDay(def.id)] ?: -1) == day &&
                (state.lifeText[kHostOutcome(def.id)] ?: "") in setOf("well", "fine"))
                return def to "hosted"
        }
        return null
    }

    /** Tonight's live progress line for the Journal page. */
    fun tonightLine(state: GameState): String {
        val hosted = hostingTonight(state)
        if (hosted != null) {
            val n = hostedStepsDone(state)
            val live = if (windowOpen(state)) " — it's on NOW: the table's set" else ""
            return "▶ Tonight you're HOSTING ${hosted.family} at your house " +
                "(${windowLabel()}); $n of $STEP_COUNT beats in.$live"
        }
        val inv = todaysInvite(state)
        if (inv != null) {
            val locName = LOCATIONS[inv.house]?.name ?: inv.house
            val n = invitedStepsDone(state)
            val live = if (windowOpen(state)) " — it's on NOW: get over there hungry" else ""
            return "▶ Tonight: ${inv.family} have invited you — $locName, " +
                "${windowLabel()}. ${inv.host} is making ${inv.dish}. " +
                "$n of $STEP_COUNT beats in.$live"
        }
        return "· No dinner on tonight — the town eats behind its own " +
            "doors. Invitations come round with the rotation."
    }

    // ---------------- the Journal page ----------------

    /** The 🍽 FAMILY EVENINGS page: tonight's table, the
     *  dinners behind you, and who's still to visit or host. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val att = totalAttended(state)
        val hos = totalHosted(state)
        out += "🍽 FAMILY EVENINGS — " +
            if (att == 0 && hos == 0)
                "the town's tables have a chair with your name on it, " +
                    "a few evenings a week"
            else "$att dinner${if (att == 1) "" else "s"} at theirs · " +
                "$hos hosted at yours"
        out += tonightLine(state)
        for (def in HOUSEHOLDS) {
            val locName = LOCATIONS[def.house]?.name ?: def.house
            val a = attendedCount(state, def)
            val h = hostedCount(state, def)
            val parts = mutableListOf<String>()
            if (a > 0) {
                parts += "dined at theirs ×$a"
                nightLine(state, def)?.let { parts += "“$it”" }
            }
            momentOf(state, def)?.let { parts += "the moment: $it" }
            if (h > 0) {
                parts += "hosted ×$h"
                hostNightLine(state, def)?.let { parts += "“$it”" }
            }
            if (a == 0 && h == 0) {
                parts += "not yet — $locName, when the rotation calls " +
                    "(host: ${def.host})"
            } else if (a == 0) {
                parts += "their table still waits"
                hostNightLine(state, def)?.let { parts += "“$it”" }
            } else if (h == 0) {
                parts += "your table still waits for them"
            }
            out += "· ${def.family} — " + parts.joinToString(" · ")
        }
        return out
    }
}

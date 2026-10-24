package com.hunt3r.southparktown

// Mystery12.kt — v0.14.0f: Mystery Arc #12, the returns
// case. A structural sibling of Mystery11: it queues
// behind the eleventh case under the same rules — four
// chapters, the last the finale; a board of surfaced
// artifacts read against records the game already
// keeps; readings priced in live evidence; verdicts
// derived, never stored. The board here is the
// Elementary's lost-and-found, which has started
// RETURNING things: items are showing up back in
// cubbies and desks, each with a short slip — except
// the returns go to the wrong owners, and each slip
// is written in the hand of the kid it went to.
// Nothing is missing. Things are coming back.
// The spine is the RETURNS LEDGER: six returned items,
// each examined three ways on every read — THE LOSS
// (was it actually lost, when, and by whom, against
// the box's own loss record), THE HAND (does the
// slip's hand match the recipient's authored HAND
// card — letterforms, pressure, habits — checked
// mechanically), and THE FIT (does the item actually
// belong with the recipient — size, owner marks,
// paired-item facts). A reading is committed through
// the Calls machinery pattern, graded by the item's
// one settling piece, and re-derived forever from
// facts: the board stores fields, never conclusions.
// The case's truth — which slips are copies, and whose
// hand copied them — is SEALED: it lives only in the
// sealed fields below and in post-reveal copy. The
// suite's spoiler wall stands over everything public.

object Mystery12 {

    const val CASE_TITLE = "Back to the Wrong Owner"
    const val FINALE12_ID = "arc12_ch4_the_reading"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10
    const val POST_DELAY_DAYS = 7

    val CHOICE_KEYS = listOf("read", "name", "stand", "walk")
    val BRANCH_KEYS = listOf("read", "named", "standing", "unread")
    const val DEFAULT_BRANCH = "unread"
    val CALL_KEYS = listOf("true", "mistake", "planted")

    // ---------------- the chapters ----------------
    // Ch1: the returns desk — things come back. Ch2: the
    // spike — the box's own loss cards, a teacher's
    // penmanship unit, and a pond that keeps what it was
    // given. Ch3: the big sort — the whole box turned
    // out in public, and the town's theory hardening on
    // the kid with the notebook. Ch4: the reading — the
    // ledger read aloud at the school, and the town
    // decides what a return is worth.
    val CHAPTERS12: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc12_ch1_the_returns_desk",
            title = "The Returns Desk",
            cause = "The lost-and-found started giving things back — to the wrong kids.",
            blurb = "Nothing at the Elementary is missing. Things are coming BACK — into cubbies and onto desks, each with a short slip of paper. Except the returns keep landing with the wrong owners, and every slip is written in the hand of the kid it went to. The box by the office has a keeper, a spike of loss cards, and, as of this week, a reputation.",
            theme = QuestTheme("📦", "#c9a15a", "THE RETURNS DESK"),
            tintHex = "#c9a15a",
            causers = listOf("Victoria", "Butters"),
            episodeTitles = listOf("The Box Gives Back",
                "Found: Everything. Owner: Wrong."),
            coldOpen = listOf(
                Line("Victoria", "Let the record show: nothing is missing. I have counted. Twice. Things are RETURNING."),
                Line("Butters", "I keep the desk honest, Principal! Every card on the spike, every return by the book. The book just — the book has started writin' back, is all.")),
            stages = listOf(
                ShenaniganStage("Monday: The First Returns",
                    "📦 THE RETURNS DESK: a mitten, a scarf and a lunchbox came back over the weekend — to the wrong cubbies.",
                    "Three things the box has held for weeks are suddenly back in circulation, each with a slip. The slips are the strange part: each one is written in the hand of the kid it went to.",
                    "Look at what's come back at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Comparing a returned thing to its slip.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Cubby Row Reacts",
                    "📦 A scarf in the wrong cubby is a small thing. A whole school comparing slips is not.",
                    "Kids are reading each other's slips like box scores. Some returns are exactly right. Some are right the way a rumor is right.",
                    "Talk to the kids whose names are on the slips.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading a slip twice, then looking up slowly.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Desk's Keeper Explains",
                    "📦 THE RETURNS DESK: the keeper walks the cubby row through the box's one custom — a card for everything, a spike for the cards.",
                    "The box keeps a loss card for everything it holds, filed by the loser, in the loser's own hand, on a spike by the office. A return rides its card home. That's the whole system. It has never once explained a slip like these.",
                    "Hear the keeper out at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Keeping the desk, nervously, by the book.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Ledger Opens",
                    "📦 Somebody has to read the returns properly: the loss, the hand, and the fit. The school opens a ledger.",
                    "The principal has ruled that the returns will be READ — each one examined three ways, in the open, before anyone is allowed to say the word 'haunted' in the building again. The ledger's first page is blank and waiting.",
                    "The case of the returns is open.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("school_staff", 1))))),
        ShenaniganDef(
            id = "arc12_ch2_the_spike",
            title = "The Spike",
            cause = "The ledger reads the box's own cards — and a penmanship unit reads back.",
            blurb = "Every lost thing at the Elementary has a card, and every card is on one spike by the office, in the hand of the kid who lost it. Reading the spike means reading hands: a fall penmanship unit that taught half the school to write alike, a sister who takes her brother's dictation, and a pond across town that never gave anything back at all.",
            theme = QuestTheme("📌", "#c9a15a", "THE SPIKE"),
            tintHex = "#c9a15a",
            causers = listOf("Garrison", "Victoria"),
            episodeTitles = listOf("Cards on a Spike",
                "The Week Everyone Wrote Alike"),
            coldOpen = listOf(
                Line("Garrison", "For two weeks this fall I taught this school ONE hand. The copybook hand. If half the town writes alike now, class, that's not a mystery. That's homework."),
                Line("Victoria", "The spike is a record, Mr. Garrison. Records are read, not blamed. Kindly let the ledger do its work.")),
            stages = listOf(
                ShenaniganStage("The Spike, Read Closely",
                    "📌 THE SPIKE: every loss in the building, on one spike, in the losers' own hands.",
                    "A return can be checked against the spike: was this thing ever filed as lost, by this kid, in these words? The cards are small. The answers are in them anyway.",
                    "Read the loss cards at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading the spike, card by card.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Copybook Comes Out",
                    "📌 THE SPIKE: the fall penmanship unit's copybook is still on the classroom shelf — the week half the school wrote alike.",
                    "Mr. Garrison's unit drilled one slant, one roundness, one set of loops into every kid in the building — and forbade, in writing, at least one habit half the neat writers in school refuse to give up any other week of the year.",
                    "Look at the copybook at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Practicing the copybook slant, again, under supervision.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("A Household That Takes Dictation",
                    "📌 THE SPIKE: one returned slip wasn't written by its owner at all — it was taken down, the way that house takes everything down.",
                    "Across town there's a kitchen table where one kid's words have always arrived secondhand — muffle marks first, a sister's brackets after. The whole town knows the arrangement. The ledger just has to price it.",
                    "Visit the McCormick house.",
                    routine = "cluster", clusterLoc = "KennysHouse",
                    clusterActivity = "Taking dictation at the kitchen table.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("The Pond Keeps Its Counsel",
                    "📌 THE SPIKE: one of the six losses didn't happen at school at all — and the place it happened never sent anything home.",
                    "Stark's Pond has a shore full of other people's Saturdays. What's still lying in its mud is evidence of the purest kind: the thing that was actually lost, still lost, exactly where it was left.",
                    "Walk the pond's edge.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Looking at the water like it owes an answer.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))))),
        ShenaniganDef(
            id = "arc12_ch3_the_big_sort",
            title = "The Big Sort",
            cause = "The whole box, turned out in public — and the town finds its suspect.",
            blurb = "The principal has ordered the lost-and-found emptied and sorted in the open: every item, every card, every claim read against the spike. Sorting a town's losses in public turns up paper that was never the box's to hold — and gives the town a name to be wrong about, in the shape of the one kid in school who keeps a notebook on everybody.",
            theme = QuestTheme("🗂", "#c9a15a", "THE BIG SORT"),
            tintHex = "#c9a15a",
            causers = listOf("Victoria", "Garrison"),
            episodeTitles = listOf("Everything Out of the Box",
                "The Notebook Kid"),
            coldOpen = listOf(
                Line("Victoria", "On Friday the box comes off its table and the whole school sorts it. In the open. If the returns have an author, the sort will leave them nowhere to stand."),
                Line("Garrison", "A public sort. Mm. In my experience, class, the only thing a public sort ever proves quickly is who the room already suspects.")),
            stages = listOf(
                ShenaniganStage("The Box Comes Off Its Table",
                    "🗂 THE BIG SORT: the lost-and-found is emptied onto the gym mats, class by class, in front of everyone.",
                    "Every unclaimed thing the school owns is out in the light now, being matched against the spike by a dozen hands at once. Whatever the box has been hiding, it's hiding it in plain sight.",
                    "Join the sort at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Sorting the town's losses, in public.",
                    tone = "hype",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Notebook Comes Up",
                    "🗂 THE BIG SORT: it takes the room about an hour to start saying one name — the kid who keeps a notebook on the whole school.",
                    "He does keep one. Everyone knows it; he has never once hidden it. The question the ledger has to answer is whether a notebook full of plans is the same thing as a hand full of copies — or just the easiest shape in the room.",
                    "Hear the notebook's owner out at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Explaining the notebook, to a jury of fourth-graders.",
                    tone = "hype",
                    factionEffects = listOf(FactionEffect("parents", 1))),
                ShenaniganStage("What the Sort Can't Settle",
                    "🗂 THE BIG SORT: two returns still won't read — their evidence is all in hand, and their meaning still depends on company it hasn't had yet.",
                    "Some records only settle in combination: a spike audited whole, a notebook whose bearing arrives with the reading. The sort ends with the ledger heavier and two slips still warm.",
                    "Take stock at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading two slips for the twentieth time.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Reading Is Announced",
                    "🗂 The ledger will be read aloud at the school — every return, all three checks, in the open. One week.",
                    "The principal has set the day. Whatever the returns are — honest work, honest error, or something studied — they will be read in front of the whole school, and the school will have to decide what to do with what it hears.",
                    "The reading is coming.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("school_staff", 1))))),
        ShenaniganDef(
            id = FINALE12_ID,
            title = "The Reading",
            cause = "The ledger, read aloud — and the town decides what a return is.",
            blurb = "The whole school, one ledger, six returns. Read them truly and the school finally learns whose hand has been coming back with its lost things. Read them halfway and the room will settle for the name it already has warmed up — the notebook kid, who has been carrying the town's suspicion all week on the strength of keeping records in the open.",
            theme = QuestTheme("📖", "#c9a15a", "THE READING"),
            tintHex = "#c9a15a",
            causers = listOf("Victoria", "Butters"),
            episodeTitles = listOf("Six Returns, Read Aloud",
                "The Hand It Came Back In"),
            coldOpen = listOf(
                Line("Victoria", "The ledger is complete, the school is seated, and I will remind everyone once: we are reading RETURNS. Whoever they belong to when we're done is a decision, not a discovery."),
                Line("Butters", "I just ran the desk, ma'am. Whatever the ledger says the returns are — I'll stand by the readin'. I sorta have to. It's my desk.")),
            stages = listOf(
                ShenaniganStage("The School Takes Its Seats",
                    "📖 THE READING: the whole Elementary, seated, for six lost things and the hands they came back in.",
                    "Every kid in the building has handled at least one of the returns by now. The ledger holds the loss, the hand and the fit of each — derived, checked, and ready to be said out loud.",
                    "Be at the Elementary for the reading.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Seated, with a slip on every desk.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Slips Go Round Once More",
                    "📖 The six slips make one last circuit of the room — so that whatever is decided is decided with the evidence in everyone's hands.",
                    "The diary that was never its recipient's. The folded paper nobody filed. The mitten that's somebody's and the somebody isn't who the card says. The room is holding all of it now.",
                    "Let the slips finish their circuit.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Passing a slip along, quietly.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("The Ledger Is Read",
                    "📖 THE READING: loss, hand and fit — six returns, read aloud, in order.",
                    "This is the moment the case was built for: the ledger, complete, said where everyone can hear it — or the room settling for the name it brought with it.",
                    "Make the call while the school is listening.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Listening to the ledger, read aloud.",
                    tone = "whisper",
                    factionEffects = listOf(FactionEffect("school_staff", 1))),
                ShenaniganStage("What a Return Is Worth",
                    "📖 It's over. The returns have been read, and the school knows what comes back to it — and in whose hand.",
                    "However it landed, the box goes back on its table tomorrow, the spike goes back to its honest work, and the town files the week under things that came back wrong.",
                    "The returns case is closed.",
                    routine = "normal",
                    factionEffects = listOf(FactionEffect("school_staff", 1)))))
        )


    // ---------------- the hands, as records ----------------
    // Every kid the returns touched writes in a hand the
    // town can describe: authored trait cards in the
    // SPEECH_RULES idiom — letterforms, pressure, habits.
    // The ledger checks a slip's RECORDED features
    // against its recipient's card mechanically, on
    // every read: forms it may show, the one form it
    // must show, forms it never shows, and the tells a
    // studied copy of it carries. A slip written in the
    // fall penmanship unit carries the copybook's forms
    // instead — the ledger keeps the drill's forms as a
    // record of their own, because half the school
    // wrote alike for two weeks and honesty requires
    // pricing that in.
    data class HandCard12(
        val kid: String,
        val forms: List<String>,
        val must: List<String>,
        val nevers: List<String>,
        val tells: List<String>,
        val habit: String
    )

    val DRILL_FORMS12: List<String> = listOf(
        "the copybook slant", "the copybook's round o's",
        "the copybook's looped l's", "the copybook's even stems"
    )

    val HANDS12: List<HandCard12> = listOf(
        HandCard12("Tweek",
            forms = listOf("jagged print", "letters lean uphill",
                "hard pencil dents", "capitals mid-word"),
            must = listOf("jagged print"),
            nevers = listOf("even loops", "cursive joins"),
            tells = listOf("steady slant", "smoothed-out letters"),
            habit = "panic, in print; every letter its own small alarm, and the pressure dents the page through"),
        HandCard12("Wendy",
            forms = listOf("small neat print", "double-crossed t's",
                "even margins", "underlined names"),
            must = listOf("double-crossed t's"),
            nevers = listOf("smudged letters", "crossed-out words"),
            tells = listOf("flourished capitals", "looped y's"),
            habit = "precise and prepared, in ink as in argument; the t-crossing happens twice, every time, except the two weeks the unit forbade it"),
        HandCard12("Kyle",
            forms = listOf("block capitals for names", "careful print",
                "crossed sevens", "lists numbered twice"),
            must = listOf("careful print"),
            nevers = listOf("cursive", "doodles in margins"),
            tells = listOf("flourished capitals", "trailing dots"),
            habit = "careful and argued, one fact per line; a form filled out like it might be cross-examined"),
        HandCard12("Kenny",
            forms = listOf("muffle marks first",
                "brackets after, in a sister's hand", "short lines",
                "one word underlined twice"),
            must = listOf("muffle marks first"),
            nevers = listOf("long clear sentences", "a signature flourish"),
            tells = listOf("even school cursive",
                "full sentences, bracketed whole"),
            habit = "never one clear word in it; taken down in muffle marks by his sister, brackets after, or not taken down at all"),
        HandCard12("Bebe",
            forms = listOf("big round letters", "loopy g's",
                "names written twice, once fancy",
                "exclamation points, plural"),
            must = listOf("big round letters"),
            nevers = listOf("small cramped print", "no capitals at all"),
            tells = listOf("surname first, initial after",
                "printed labels, pasted straight"),
            habit = "bright, fast and social; writes like she's announcing — everything big, everything twice, nothing filed in a form's own format"),
        HandCard12("Stan",
            forms = listOf("flat plain print", "short words",
                "t's crossed once, low", "no capitals past the first"),
            must = listOf("flat plain print"),
            nevers = listOf("cursive", "exclamation points"),
            tells = listOf("surname first, initial after",
                "careful copybook joins"),
            habit = "deadpan and flat, on paper as in person; underreacts in print, signs with a first name and nothing after it")
    )

    fun handCard12(kid: String): HandCard12? =
        HANDS12.firstOrNull { it.kid == kid }

    // ---------------- the board ----------------
    // Each returned item carries the facts the ledger
    // reads: what the box's loss record ties to its
    // recipient (the filed kind), what the filing claims
    // the lost thing was (claimedMarks), what the
    // returned thing observably is (itemMarks), and the
    // slip itself — its words and its hand's recorded
    // features. The three checks are DERIVED from those
    // fields on every read; `truth` and its settling
    // piece grade a reading after the fact, exactly the
    // way the folder graded the letters before it.
    data class Return12(
        val id: String,
        val index: Int,
        val title: String,
        val place: String,      // where it surfaced
        val keeper: String,     // whose hands it is in now
        val claimLabel: String, // what the return claims to be
        val recipient: String,  // the kid it went to
        val kind: String,       // what the returned thing is
        val slipText: String,   // the slip, verbatim
        val handFeatures: List<String>, // the slip's hand, as recorded
        val filedKind: String?, // the spike's genuine kind for this kid
        val claimedMarks: List<String>, // the filing's facts of the loss
        val itemMarks: List<String>,    // the returned thing's own facts
        val truth: String,      // the reading the seals confirm
        val proofBy: String,    // the one piece that settles it
        val minChapter: Int,    // arc12Chapter gate for committing
        val callPrice: Int = 2
    )

    val ITEMS12: List<Return12> = listOf(
        Return12(
            id = "return_mitten", index = 0,
            title = "THE RED MITTEN",
            place = "the Elementary",
            keeper = "on top of the cubby it landed in, where the whole row can see it",
            claimLabel = "a red mitten, come back to the boy who lost two",
            recipient = "Tweek", kind = "red mitten",
            slipText = "Lost: one red mitten, right hand, wool, my size. If found, my cubby. — Tweek",
            handFeatures = listOf("jagged print", "letters lean uphill",
                "hard pencil dents", "capitals mid-word"),
            filedKind = "red mitten",
            claimedMarks = listOf("right hand", "wool", "grown-up size"),
            itemMarks = listOf("left hand", "wool", "kindergarten size"),
            truth = "mistake", proofBy = "mitten_cards", minChapter = 1),
        Return12(
            id = "return_scarf", index = 1,
            title = "THE STRIPED SCARF",
            place = "the Elementary",
            keeper = "over the back of the chair it was found on",
            claimLabel = "a striped scarf, come back to its owner's desk",
            recipient = "Wendy", kind = "striped scarf",
            slipText = "Lost in the assembly: my striped scarf, the long one, initials in the fringe. Wendy.",
            handFeatures = listOf("the copybook slant",
                "the copybook's round o's", "the copybook's looped l's",
                "small neat print"),
            filedKind = "striped scarf",
            claimedMarks = listOf("long", "striped",
                "initials in the fringe"),
            itemMarks = listOf("long", "striped",
                "initials in the fringe"),
            truth = "true", proofBy = "victoria_filing", minChapter = 1),
        Return12(
            id = "return_lunchbox", index = 2,
            title = "THE LUNCHBOX",
            place = "the Elementary",
            keeper = "in the cubby it came back to, lid up, like it lives there",
            claimLabel = "a blue lunchbox, come back from the lost",
            recipient = "Kyle", kind = "lunchbox",
            slipText = "LOST — lunchbox. Blue, two compartments, clasp CRACKED (it still works). Lost at the pond Saturday. Kyle B.",
            handFeatures = listOf("careful print",
                "block capitals for names", "crossed sevens"),
            filedKind = "lunchbox",
            claimedMarks = listOf("cracked clasp", "blue",
                "two compartments"),
            itemMarks = listOf("whole clasp", "blue",
                "two compartments"),
            truth = "mistake", proofBy = "pond_lunchbox", minChapter = 1),
        Return12(
            id = "return_cartridge", index = 3,
            title = "THE CARTRIDGE",
            place = "the Elementary",
            keeper = "in the recipient's own pocket, where it has already been forgiven for leaving",
            claimLabel = "one game cartridge, come back to the only kid who'd miss it",
            recipient = "Kenny", kind = "game cartridge",
            slipText = "(muffle marks) [one game cartridge. name scratched on the back. it's mine. — taken down for K.M.]",
            handFeatures = listOf("muffle marks first",
                "brackets after, in a sister's hand", "short lines"),
            filedKind = "game cartridge",
            claimedMarks = listOf("name scratched on the back",
                "one save file", "a cracked corner"),
            itemMarks = listOf("name scratched on the back",
                "one save file", "a cracked corner"),
            truth = "true", proofBy = "karen_cards", minChapter = 1),
        Return12(
            id = "return_diary", index = 4,
            title = "THE DIARY",
            place = "the Elementary",
            keeper = "on the desk it was left on, unopened, getting heavier by the day",
            claimLabel = "a diary, come back to the girl the slip says lost it",
            recipient = "Bebe", kind = "diary",
            slipText = "STEVENS, B. — one diary, pink cover, blank inside. Found. Returned per the card on file.",
            handFeatures = listOf("big round letters", "loopy g's",
                "surname first, initial after",
                "printed labels, pasted straight"),
            filedKind = "hair clip",
            claimedMarks = listOf("pink cover", "blank inside"),
            itemMarks = listOf("pink cover", "an owner's name inside"),
            truth = "planted", proofBy = "spike_audit", minChapter = 2),
        Return12(
            id = "return_paper", index = 5,
            title = "THE FOLDED PAPER",
            place = "the Elementary",
            keeper = "inside the desk it came back to, folded exactly as found",
            claimLabel = "one folded paper, personal, come back to the boy the slip names",
            recipient = "Stan", kind = "folded paper",
            slipText = "MARSH, S. — one folded paper, personal, blank outside. Found in the library. Returned per the card on file.",
            handFeatures = listOf("flat plain print", "short words",
                "surname first, initial after",
                "careful copybook joins"),
            filedKind = "one glove",
            claimedMarks = listOf("folded paper", "blank outside"),
            itemMarks = listOf("folded paper",
                "a draft inside, in another hand"),
            truth = "planted", proofBy = "authors_notebook", minChapter = 2)
    )

    val ITEM12_BY_ID: Map<String, Return12> =
        ITEMS12.associateBy { it.id }

    // The ledger's derived shape for an item, in words,
    // before any reading is committed.
    fun shapeLabel12(shape: String): String = when (shape) {
        "true" -> "TRUE"
        "mistake" -> "MISTAKE"
        else -> shape.uppercase()
    }

    // A reading's display text — what the call MEANS,
    // printed wherever a committed reading shows.
    fun readingText12(call: String): String = when (call) {
        "true" -> "TRUE RETURN — the box did its one job: the " +
            "thing that was lost is the thing that came back, " +
            "and the slip is the loser's own"
        "mistake" -> "KIND MISTAKE — an honest filing and a " +
            "wrong instance: the card is true, and the thing " +
            "is not theirs"
        "planted" -> "PLANTED — the slip is a copy: the hand " +
            "was studied, and the return was staged"
        else -> call
    }

    // The cross-item datum, derived on every read the
    // way the folder's routing phrase was: a slip that
    // addresses its cubby in the box roster's own form
    // — surname first, initial after — was composed off
    // the printed roster sheet by the box, not out of
    // knowing the kid. Genuine slips sign with first
    // names, the way kids do.
    private val ROSTER_FORM12 =
        Regex("\\b[A-Z]{3,}, [A-Z]\\.")

    fun hasRosterForm12(item: Return12): Boolean =
        ROSTER_FORM12.containsMatchIn(item.slipText)


    // ---------------- the three checks, derived ----------------
    // Recomputed from the item's recorded fields and its
    // recipient's HAND card on EVERY read, never stored.
    // The hand check counts like the voice scan did: a
    // copy is surface-rich and tell-bearing; a true hand
    // is surface-rich and tell-free; the drill hand is
    // the copybook's surface over a true filing. The
    // loss check reads the spike's record; the fit check
    // reads the lost thing's claimed marks against the
    // returned thing's own.
    data class Findings12(
        val formsHit: List<String>,
        val mustMissed: List<String>,
        val neversHit: List<String>,
        val tellsHit: List<String>,
        val drillHit: List<String>,
        val lossOk: Boolean,
        val fitOk: Boolean
    )

    fun findings12(item: Return12): Findings12 {
        val card = handCard12(item.recipient)
        val feats = item.handFeatures
        return Findings12(
            formsHit = card?.forms?.filter { it in feats }
                ?: emptyList(),
            mustMissed = card?.must?.filter { it !in feats }
                ?: emptyList(),
            neversHit = card?.nevers?.filter { it in feats }
                ?: emptyList(),
            tellsHit = card?.tells?.filter { it in feats }
                ?: emptyList(),
            drillHit = feats.filter { it in DRILL_FORMS12 },
            lossOk = item.filedKind != null &&
                item.filedKind == item.kind,
            fitOk = item.claimedMarks.toSet() ==
                item.itemMarks.toSet())
    }

    // The hand's class, in words: TRUE HAND (the kid's
    // own, must-form present, nothing foreign), THE
    // DRILL HAND (the copybook's forms over a true
    // filing — the unit week's signature), A COPIED HAND
    // (surface-rich AND tell-bearing), or NO HAND IN
    // TOWN (matches nothing the school keeps).
    fun handShape12(item: Return12): String {
        val f = findings12(item)
        return when {
            f.tellsHit.isNotEmpty() && f.formsHit.size >= 2 ->
                "copied"
            f.tellsHit.isEmpty() && f.neversHit.isEmpty() &&
                f.mustMissed.isEmpty() && f.formsHit.size >= 2 ->
                "true"
            f.tellsHit.isEmpty() && f.neversHit.isEmpty() &&
                f.drillHit.size >= 3 && f.formsHit.isNotEmpty() ->
                "drill"
            else -> "strange"
        }
    }

    fun handShapeLabel12(shape: String): String = when (shape) {
        "true" -> "TRUE HAND"
        "drill" -> "THE DRILL HAND"
        "copied" -> "A COPIED HAND"
        else -> "NO HAND IN TOWN"
    }

    // The ledger's reading of an item, derived whole:
    // PLANTED when the hand is a studied copy; TRUE
    // RETURN when the loss is real, the fit is exact
    // and the hand is the kid's own or the drill's;
    // KIND MISTAKE when the loss is real, the hand is
    // clean, and the fit fails — an honest card, a
    // wrong instance; UNFILED for everything else: a
    // loss nobody filed, a hand nobody in town writes.
    fun shape12(item: Return12): String {
        val f = findings12(item)
        val hand = handShape12(item)
        return when {
            f.tellsHit.isNotEmpty() && f.formsHit.size >= 2 ->
                "planted"
            f.tellsHit.isEmpty() && f.lossOk && f.fitOk &&
                (hand == "true" || hand == "drill") -> "true"
            f.tellsHit.isEmpty() && f.neversHit.isEmpty() &&
                f.lossOk && !f.fitOk -> "mistake"
            else -> "mixed"
        }
    }

    // ---------------- the evidence ----------------
    // Twelve pieces: five carried by witnesses, five
    // found by looking, two worked by the player's own
    // hands inside chapter climaxes. Two pieces per
    // item; one of the two settles it. The settling
    // pieces of the two load-bearing items are
    // context-locked: their text exists from the moment
    // they're held, but it only SAYS anything live in
    // the company of a later chapter — the spike's
    // audit reads whole only once the sort is underway,
    // and the fair-copy notebook only tells its story
    // in the reading's own week. Context is a kind of
    // handwriting.
    enum class Kind12 { WITNESS, LOOK, PARTICIPATION }

    data class Clue12(
        val id: String,
        val kind: Kind12,
        val minChapter: Int,
        val site: Int,
        val proof: Boolean,           // settles its item
        val unlockChapter: Int = 0,   // > minChapter: context-locked
        val title: String = "",
        val detail: String = "",
        val inertDetail: String = detail,
        val npc: String? = null,      // WITNESS: who carries it
        val spoken: String? = null,   // WITNESS: their telling
        val locId: String? = null,    // LOOK: where it surfaces
        val foundText: String? = null, // LOOK: the Look beat
        val chapterDefId: String? = null // PARTICIPATION: the beat
    )

    val CLUES12: List<Clue12> = listOf(
        // --- the mitten (item 0) ---
        Clue12("mitten_cards", Kind12.LOOK, 0, 0, true,
            title = "The mitten cards, on the spike",
            detail = "THE LOSS: the spike's mitten section, read whole. There are exactly two mitten cards on it, and both are Tweek's — he lost a red mitten in the first cold snap and its replacement a week later, and filed for both, in his own dents-through-the-page hand. THE FIT: both cards say the same three things — right hand, wool, my size, meaning a fourth-grader's. The mitten that came back is a LEFT, in a size a kindergartner would swim in. The loss is real twice over. The thing that answered it is neither of the things that were lost.",
            locId = "Elementary",
            foundText = "📌 THE SPIKE: two cards, both his, both right-handed — and the mitten downstairs is a left."),
        Clue12("unclaimed_bin", Kind12.PARTICIPATION, 1, 0, false,
            title = "The unclaimed bin, sorted by hand",
            detail = "THE FIT: the box keeps a bin under its table for finds nobody ever carded — walk-ins off the gym floor, the bus, the cubby row. Sorting it by hand during the spike week turns the returned mitten's twin fact plain: it came out of THIS bin, description-matched to a spike card on the keeper's round — red mitten to red mitten, the way you'd match socks in the dark. Nobody lied. Nobody even looked at the size. A card says what a thing is called; the bin holds what a thing is.",
            chapterDefId = "arc12_ch2_the_spike"),
        // --- the scarf (item 1) ---
        Clue12("victoria_filing", Kind12.WITNESS, 0, 1, true,
            title = "The principal proctored the filing",
            detail = "THE LOSS and THE HAND: the scarf's card is genuine, and its strange hand is explained by the calendar. The card was filed during the fall penmanship unit — the principal proctored the filing herself, row by row, as the unit's final exercise — and the whole class wrote their cards in the copybook hand that week, double-crossings forbidden by the unit's own rules. The filing is Wendy's, made under supervision, in the drill's slant. The scarf answers to it exactly: long, striped, her initials worked into the fringe.",
            npc = "Victoria",
            spoken = "I proctored the fall filings myself, during the penmanship unit — every card in that row was written in the copybook hand, by order. Hers included. Kindly do not mistake my homework policy for a conspiracy."),
        Clue12("copybook", Kind12.LOOK, 1, 1, false,
            title = "The copybook, still on the shelf",
            detail = "THE HAND: the unit's copybook, kept on the classroom shelf. Its forms are the week's whole story — one slant, round o's, looped l's, even stems — and its last page is a prohibition in the teacher's own hand: NO DOUBLE-CROSSING IN THIS UNIT, in capitals, underlined. A neat writer's signature habit, banned for a fortnight. Any card filed that week wears the drill's face over the filer's own, and the ledger is expected to know the difference.",
            locId = "Elementary",
            foundText = "📌 THE COPYBOOK: one slant for everybody — and one habit expressly banned for the fortnight."),
        // --- the lunchbox (item 2) ---
        Clue12("pond_lunchbox", Kind12.LOOK, 0, 2, true,
            title = "The other lunchbox, in the pond mud",
            detail = "THE FIT: at the pond's edge, half-sunk where a Saturday left it, sits a blue lunchbox — two compartments, and a clasp cracked clean through the middle, exactly where its owner always said it was. It is the lunchbox on Kyle's card, still lost, still at the pond. Which settles the returned one by arithmetic: a boy cannot be returned a thing that never left the mud. The lunchbox in his cubby is somebody else's lunchbox, wearing his card's description like a borrowed coat.",
            locId = "StarksPond",
            foundText = "📌 THE POND: a blue lunchbox in the mud, clasp cracked — the lost one. Still lost."),
        Clue12("kyle_pond", Kind12.WITNESS, 0, 2, false,
            title = "The owner, on where it actually went",
            detail = "THE LOSS: his telling is consistent in every particular a record can check. The lunchbox was lost at the pond on a Saturday — not at school, never in the box's keeping — and its clasp has been cracked since the fall, a fact he has mentioned to the entire school at a volume nobody requested. The one that came back has a whole clasp. He is keeping it on the desk, he says, for its actual owner, who is welcome to claim it any time.",
            npc = "Kyle",
            spoken = "I lost it at the POND. The clasp's been cracked since September, ask ANYONE. The one in my cubby is a perfectly good lunchbox that belongs to somebody else, and if I find out who staged this to make me look like a lunchbox thief, there will be a FORMAL complaint."),
        // --- the cartridge (item 3) ---
        Clue12("karen_cards", Kind12.WITNESS, 0, 3, true,
            title = "The sister who takes the dictation",
            detail = "THE HAND: the cartridge's card is genuine, and its oddity is the household's ordinary arrangement. His sister takes his cards down at the kitchen table — muffle marks first, her brackets after — the way she has taken down everything of his since he could talk. She wrote this card the week the cartridge went missing, and she scratched his name into the cartridge's back herself, last spring, so the two match in her own hand: the card is his by dictation, hers by pen, and the town's oldest open secret either way.",
            npc = "Karen",
            spoken = "I write all his cards. Muffle marks first, then I put what it means in the brackets. I wrote the cartridge one the week it went missing, and I'm the one who scratched his name on the back of it last spring. Whoever thinks the hand's funny doesn't know our house."),
        Clue12("cartridge_slot", Kind12.LOOK, 0, 3, false,
            title = "The empty slot, at the McCormick house",
            detail = "THE FIT: the cartridge's case, at its owner's house — a soft case with exactly one slot, and the slot has been empty since the week of the loss. His initial is scratched in the foam where the cartridge used to ride, in the same hand that scratched the cartridge itself. One slot, one cartridge, one household that could not have replaced the thing if it wanted to. What came back fits the slot like it was poured into it.",
            locId = "KennysHouse",
            foundText = "📌 THE EMPTY SLOT: one slot, one initial in the foam — and the thing that came back fits it exactly."),
        // --- the diary (item 4): load-bearing ---
        Clue12("spike_audit", Kind12.LOOK, 1, 4, true,
            unlockChapter = 2,
            title = "The spike, audited whole",
            detail = "THE LOSS and THE HAND: the spike, read as one document against the class roster. Every genuine card of the term hangs on it, and every genuine card is PUNCHED — the spike goes through each one; that is what a spike is. Against the roster, Bebe Stevens' whole filing history is one card: a hair clip, October. There is no diary card on this spike in her hand or any other. And the slip that sent the diary to her desk is UNPUNCHED — smooth paper, never spiked, never filed. It did not come off this record. Somebody composed it somewhere else, in a hand studied off the genuine article, and walked it in.",
            inertDetail = "THE SPIKE: the box's record, complete — every card of the term, in order, which is a chore to read and says nothing yet. An audit is a spike read against a roster, and the roster's company hasn't been called. For now the spike is just a spike.",
            locId = "Elementary",
            foundText = "📌 THE SPIKE, AUDITED: one filing in her name all term — and the diary's slip was never on this spike at all."),
        Clue12("nathan_notebook", Kind12.WITNESS, 1, 4, false,
            title = "The notebook's owner, on the record",
            detail = "THE HAND: his telling is checkable, and he insists on it being checked. He inventories the lost-and-found the way he inventories everything — the diary sat in that box for weeks this fall with a hair ribbon for a marker, and the ribbon is not on it anymore; somebody handled that book before it traveled. As for the town's theory: every scheme he has ever run is written in his notebook, in advance, in order, and he will produce it. There are no card copies in it. Copying hands isn't in the plan, he says — the plan is better than that, and besides, he writes his OWN name on his work. Somebody who files a slip in the roster's format — surname first, initial after — is working off the printed sheet by the box. He has never once needed the sheet. He knows everybody's first name. It's the whole point of him.",
            npc = "Nathan",
            spoken = "The diary sat in that box for weeks with a ribbon on it. The ribbon's gone. Somebody handled it. As for me: the notebook is available for inspection. It has ALWAYS been available for inspection. That is the difference between a plan and a frame."),
        // --- the folded paper (item 5): load-bearing ---
        Clue12("heidi_library", Kind12.WITNESS, 2, 5, false,
            title = "The finder, on the library floor",
            detail = "THE LOSS: her telling puts the paper's arrival at the box beyond suspicion. A folded paper slipped out of a notebook on the library floor; she picked it up and laid it on the returns desk like lost property should be, and never opened it — some things, she says, you can just tell are private, and private things get returned, not read. The paper reached the box honestly. What it cannot explain is the slip that sent it on: she filed no card for it, and neither, on her telling, did anyone standing near her.",
            npc = "Heidi",
            spoken = "It fell out of a notebook in the library, right on the floor. I didn't open it — you can just TELL some things are private. I put it on the returns desk where lost stuff goes. That's what you're supposed to do. Isn't it?"),
        Clue12("authors_notebook", Kind12.PARTICIPATION, 2, 5, true,
            unlockChapter = 3,
            title = "The fair-copy notebook",
            detail = "THE LOSS and THE FIT: among the sort's paper, handled by your own hands, a notebook kept the way some kids keep a second, truer diary — and in it, copied fair, is the folded paper's twin. Same words, same careful hand, with a line underneath in the same writing: not sending. probably never. The draft that reached the box was the loose working copy; this is the kept one. No card was ever made from this book — the spike holds no filing for a folded paper in any hand — and its keeper never lost it, because its keeper never let it go. The paper that came back to a desk at the school was a private thing that was never surrendered, sent on by a slip its writer never wrote.",
            inertDetail = "A FAIR-COPY BOOK: among the sort's paper sits a notebook copied fair, kept carefully, in a hand the ledger will need — a private book, and whatever it proves can't be read off it yet. Some books only tell their story in the reading's own company. It goes back on the pile, unquoted.",
            chapterDefId = "arc12_ch3_the_big_sort")
    )

    val CLUE12_BY_ID: Map<String, Clue12> =
        CLUES12.associateBy { it.id }
    val CLUE12_IDS: Set<String> = CLUES12.map { it.id }.toSet()

    fun clueCount12(state: GameState): Int =
        state.mystery12Clues.count { it in CLUE12_IDS }

    // A piece is AVAILABLE once its chapter has begun
    // (arc12Chapter counts chapters begun, like its
    // siblings). LOOK and WITNESS pieces wait for their
    // places and people; PARTICIPATION pieces wait for
    // their chapter's own climax machinery.
    fun clueAvailable12(state: GameState, clue: Clue12): Boolean {
        if (!state.arc12Started || state.arc12Retired) return false
        if (clue.id in state.mystery12Clues) return false
        return state.arc12Chapter > clue.minChapter
    }

    // A piece is LIVE once held and its context has
    // arrived — the context-locked pieces sit inert in
    // the hand until their chapter begins.
    fun clueLive12(state: GameState, clue: Clue12): Boolean =
        clue.id in state.mystery12Clues &&
            state.arc12Chapter > clue.unlockChapter

    fun clueText12(clue: Clue12, live: Boolean): String =
        if (live) clue.detail else clue.inertDetail

    // Grant a held piece: the events feed and the town
    // log carry it, in the arcs' wording — live pieces
    // announce themselves; locked ones announce only
    // that they are in hand.
    fun grantClue12(state: GameState, id: String): Boolean {
        val clue = CLUE12_BY_ID[id] ?: return false
        if (id in state.mystery12Clues) return false
        state.mystery12Clues.add(id)
        val live = clueLive12(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery12Clues.size}/${CLUES12.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery12Clues.size}/${CLUES12.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "coming, and in this case context is a kind of handwriting.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // WITNESS: TalkGate calls this when the carrier is
    // spoken to; a witness tells once, in a Line.
    fun witnessLine12(state: GameState, npcName: String): Line? {
        for (clue in CLUES12) {
            if (clue.kind != Kind12.WITNESS) continue
            if (clue.npc != npcName) continue
            if (!clueAvailable12(state, clue)) continue
            if (!grantClue12(state, clue.id)) continue
            return Line(npcName, clue.spoken ?: "")
        }
        return null
    }

    // LOOK: the Look action at a location surfaces the
    // earliest available piece waiting there.
    fun lookClueText12(state: GameState, locId: String): String? {
        for (clue in CLUES12) {
            if (clue.kind != Kind12.LOOK) continue
            if (clue.locId != locId) continue
            if (!clueAvailable12(state, clue)) continue
            grantClue12(state, clue.id)
            return clue.foundText
        }
        return null
    }

    // PARTICIPATION: Engine's participation hook calls
    // this as a chapter's stages advance — every
    // available piece tied to the running chapter is
    // worked (and granted) by the player's own hands.
    fun noteParticipation12(state: GameState, defId: String) {
        for (clue in CLUES12) {
            if (clue.kind != Kind12.PARTICIPATION) continue
            if (clue.chapterDefId != defId) continue
            if (!clueAvailable12(state, clue)) continue
            grantClue12(state, clue.id)
        }
    }


    // ---------------- readings & verdicts ----------------
    // The Calls machinery pattern, applied to returns:
    // a reading is committed per item at a price of
    // live evidence, graded by the item's settling
    // piece, and its verdict re-derived on every read.
    fun liveEvidence12(state: GameState, item: Return12): Int =
        CLUES12.count { it.site == item.index &&
            clueLive12(state, it) }

    fun liveHeld12(state: GameState): Int =
        CLUES12.count { clueLive12(state, it) }

    // An item can be read once its chapter has come and
    // its live evidence covers the price.
    fun itemCallable12(state: GameState, item: Return12): Boolean =
        state.arc12Started && !state.arc12Retired &&
            state.arc12Chapter > item.minChapter &&
            liveEvidence12(state, item) >= item.callPrice

    fun pendingItem12(state: GameState): Return12? {
        for (item in ITEMS12) {
            val call = state.arc12Calls[item.id]
            if (call != null &&
                letterVerdict12(state, item) != "REFUTED") continue
            if (itemCallable12(state, item)) return item
        }
        return null
    }

    fun committedCall12(state: GameState, item: Return12): String? =
        state.arc12Calls[item.id]?.takeIf { it in CALL_KEYS }

    // DERIVED, like every verdict before it: OPEN (no
    // standing reading), COMMITTED (read, ungraded),
    // PROVEN (read, and the settling piece — held live —
    // confirms the derived shape), REFUTED (read against
    // what the settling piece proves: strikeable).
    fun letterVerdict12(state: GameState, item: Return12): String {
        val call = committedCall12(state, item) ?: return "OPEN"
        val proof = CLUE12_BY_ID[item.proofBy] ?: return "COMMITTED"
        if (!clueLive12(state, proof)) return "COMMITTED"
        return if (call == shape12(item)) "PROVEN" else "REFUTED"
    }

    fun chooseCall12(state: GameState, itemId: String,
                     call: String): Boolean {
        val item = ITEM12_BY_ID[itemId] ?: return false
        if (call !in CALL_KEYS) return false
        if (!itemCallable12(state, item)) return false
        val standing = committedCall12(state, item)
        if (standing != null &&
            letterVerdict12(state, item) != "REFUTED") return false
        state.arc12Calls[item.id] = call
        return true
    }

    fun provenCount12(state: GameState): Int =
        ITEMS12.count { letterVerdict12(state, it) == "PROVEN" }

    fun boardCalled12(state: GameState): Boolean =
        ITEMS12.all { committedCall12(state, it) != null }

    fun boardClean12(state: GameState): Boolean =
        ITEMS12.none { letterVerdict12(state, it) == "REFUTED" }

    // ---------------- the finale: the reading ----------------
    // The reading stands due when the finale chapter is
    // airing, its climax stage has come, and every
    // return carries a reading. The full read is the
    // gated call: both load-bearing items PROVEN, the
    // whole board read, nothing standing refuted, and
    // at least four of the six proven besides — the
    // voice case's ratio, carried over whole.
    fun returnsDue(state: GameState): Boolean {
        if (state.arc12Retired) return false
        if (state.arc12Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE12_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return boardCalled12(state)
    }

    fun finishEligible12(state: GameState): Boolean {
        val diary = ITEMS12[4]
        val paper = ITEMS12[5]
        return letterVerdict12(state, diary) == "PROVEN" &&
            letterVerdict12(state, paper) == "PROVEN" &&
            boardCalled12(state) && boardClean12(state) &&
            provenCount12(state) >= 4
    }

    fun nameEligible12(state: GameState): Boolean =
        provenCount12(state) >= 1

    fun chooseFinale12(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!returnsDue(state)) return false
        if (choice == "read" && !finishEligible12(state)) return false
        if (choice == "name" && !nameEligible12(state)) return false
        state.arc12Choice = choice
        return true
    }

    data class ReturnsOutcome(val branch: String, val outcome: String,
                              val recapLines: List<String>)

    // At the wrap the gates are checked AGAIN — a choice
    // whose proof collapsed between the lock and the
    // close lands degraded, never upgraded.
    fun resolveReturns(state: GameState): ReturnsOutcome {
        val choice = state.arc12Choice
        val complete = boardCalled12(state)
        val branch = when {
            choice == "read" && complete && finishEligible12(state) ->
                "read"
            choice == "name" && complete && nameEligible12(state) ->
                "named"
            choice == "stand" && complete -> "standing"
            else -> DEFAULT_BRANCH
        }
        state.arc12Branch = branch
        state.arc12Retired = true
        state.arc12RetiredDay = state.absDay
        return when (branch) {
            "read" -> ReturnsOutcome(
                "read",
                "📖 THE READING: the ledger is read aloud, all six returns, and the two slips that were never filed read loudest of all. Their hands are copies — surface-perfect, tell-bearing, addressed the roster way, surname first, off the printed sheet by the box. And one kid in the room knows every cubby's news by lunch, files everybody's business by dinner, and has been handling returned things all term like they were stories waiting to be told. Annie Nelson doesn't deny it. She returned what the box wouldn't — to whoever would make the best story out of getting it. The diary goes back to its owner's desk. The folded paper goes back unopened. The notebook is entered into the record as an alibi, which its owner accepts as the nicest thing the school has ever said about him.",
                listOf("📦 The returns were read: two staged, two mistaken, two true — and the hand behind the studied pair has a name at last.",
                    "📦 The notebook stands cleared in the same record that names the hand."))
            "named" -> ReturnsOutcome(
                "named",
                "📖 THE READING: the room does what rooms do — it settles for the name it brought with it. The notebook kid. The one who writes everything down. He stands up, in front of the whole school, and says the sentence that will follow him to graduation: the notebook is available for inspection; it has always been available for inspection; that is the difference between a plan and a frame. It is a very good sentence. It changes nothing. The case closes on the easiest shape in the room.",
                listOf("📦 The returns case closed on a name — the notebook's owner, cleared by nothing but his own record.",
                    "📦 The box goes back on its table. Two slips in it were never explained to anyone's satisfaction but the room's."))
            "standing" -> ReturnsOutcome(
                "standing",
                "📖 THE READING: you let the returns stand. The slips stay where the box put them, the diary stays on the wrong desk, the mitten stays the wrong size, and the school decides — by saying nothing, together — that a return is a return, and asking whose hand it came back in is the kind of question that loses friends. The keeper closes the ledger with visible relief and writes nothing in it.",
                listOf("📦 The returns stand, unread and uncorrected, and the box keeps its secrets by acclamation.",
                    "📦 Somewhere in the building, two studied slips stay filed under their recipients' names."))
            else -> ReturnsOutcome(
                "unread",
                "📖 THE READING: the day ends with the ledger unopened. The school files the returns under weather — a strange week, best not examined — and the box goes back on its table with six small mysteries riding its spike. Whatever the slips were, they are the box's business now.",
                listOf("📦 The returns were never read. The box keeps its own counsel, and the town keeps walking past it."))
        }
    }

    fun applyReturnsEffects(state: GameState, branch: String) {
        fun ripple(factionId: String, delta: Int, note: String) {
            val fs = state.factionStates[factionId] ?: return
            fs.influence = (fs.influence + delta).coerceIn(0, 100)
            if (delta != 0) {
                fs.moodScore = (fs.moodScore + if (delta > 0) 1 else -1).coerceIn(-2, 2)
            }
            if (note.isNotEmpty()) {
                fs.mood = note
                state.events.add("⚔️ FACTION SHIFT: $note")
                while (state.events.size > 40) state.events.removeAt(0)
            }
        }
        fun befriend(name: String, delta: Int) {
            state.friendship[name] =
                ((state.friendship[name] ?: 0) + delta).coerceAtLeast(0)
        }
        when (branch) {
            "read" -> {
                befriend("Bebe", 3)
                befriend("Stan", 2)
                befriend("Butters", 2)
                ripple("school_staff", 1,
                    "The Staff watched a full school hear the returns explained from the records themselves — a spike, a copybook, a pond — and the two staged slips were neither of the things the room had guessed. The Staff is revising the box's rota. The Staff revises rotas the way other people make tea.")
                ripple("parents", -1,
                    "The Parents came for a haunting and got a filing audit instead. Half the Parents are re-telling it already. They're telling the roster part wrong, but they're telling it.")
                state.arc12PostPending = true
                state.arc12PostDay = state.absDay + POST_DELAY_DAYS
                state.lifeText["mark:Elementary"] = "🖋 The ledger was read " +
                    "aloud here — two staged slips, two honest mistakes, " +
                    "two true returns — and the desk's edge still wears " +
                    "the reading's ring. (the returns case)"
                townLogLine(state, "📦 READ: the ledger was read at " +
                    "the school. No return was staged on a stage. " +
                    "The returns were read against their records, " +
                    "in public, and the checks held up.")
            }
            "named" -> {
                befriend("Nathan", -4)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents got their name at the reading, before the bell, in the record, spelled right. The Parents are satisfied. The Parents call the objection a detail, a little, in private.")
                ripple("school_staff", -1,
                    "The Staff keeps the record of the naming in a hand slightly tighter than usual. One notebook in this school keeps a different record — every plan its owner ever made, in advance, in the open — and it is available for inspection. It has always been available for inspection. None of it was ever filed on a spike.")
                state.lifeText["mark:Elementary"] = "🖋 The returns were " +
                    "closed by name here — the notebook's, over his " +
                    "standing objection. (the returns case)"
                townLogLine(state, "📦 NAMED: the reading closed on " +
                    "the notebook's owner. His objection is on file. " +
                    "It was always going to be on file.")
            }
            "standing" -> {
                befriend("Butters", 1)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents are relieved the school asked nothing. A return is a return, the Parents say, at pickup, in the tone of people closing a door.")
                state.lifeText["mark:Elementary"] = "🖋 The returns were " +
                    "left standing here, unread and uncorrected, by the " +
                    "school's own silence. (the returns case)"
                townLogLine(state, "📦 STANDING: the returns stand " +
                    "as they landed. The box keeps its slips, and " +
                    "the school keeps its peace.")
            }
            else -> {
                befriend("Nathan", -2)
                ripple("parents", -1,
                    "The Parents heard the reading never happened and have decided not to wonder about it, together, out loud, several times.")
                state.lifeText["mark:Elementary"] = "🖋 The returns were " +
                    "never read here; the box keeps its counsel by the " +
                    "office door. (the returns case)"
                townLogLine(state, "📦 UNREAD: the ledger shut " +
                    "unopened. The box goes back on its table with " +
                    "six small mysteries riding its spike.")
            }
        }
    }

    // The delayed tail of the full read, and only the
    // full read: a week on, the two staged items are
    // quietly returned AGAIN — to their true owners,
    // with plain slips in nobody's studied hand.
    fun noteReturnsCorrected(state: GameState) {
        if (!state.arc12PostPending) return
        if (state.arc12Posted) return
        if (state.absDay < state.arc12PostDay) return
        state.arc12Posted = true
        state.arc12PostPending = false
        state.events.add("📦 A QUIET CORRECTION: the diary is back on " +
            "Red's desk this morning, ribbon and all, with a plain " +
            "unsigned slip that just says FOUND. The folded paper is " +
            "back inside its notebook in the library, unopened by " +
            "anyone new. No handwriting on either slip that anybody " +
            "would bother to study.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "📦 The staged returns were quietly " +
            "re-returned — to the desks they belonged on all along.")
        state.lifeText["mark:Elementary"] = "🖋 A week after the " +
            "reading, the two staged returns were quietly corrected " +
            "here — home to their true owners, with plain slips in " +
            "nobody's studied hand. (the returns case)"
    }

    // ---------------- scheduling ----------------
    // The same posture as the eleven cases before it:
    // the case QUEUES ten days behind the eleventh
    // case's retirement, never starts over its open
    // body, chapters pace a week apart, and a holiday
    // defers the chapter without repricing the queue.
    fun dueChapter12(state: GameState): ShenaniganDef? {
        if (state.arc12Retired) return null
        if (!state.arc11Retired) return null
        if (state.arc12Chapter >= CHAPTERS12.size) return null
        if (state.arc12DueAbsDay < 0) {
            val anchor = if (state.arc11RetiredDay >= 0)
                state.arc11RetiredDay else state.absDay
            state.arc12DueAbsDay = maxOf(state.absDay,
                anchor + OPEN_DELAY_DAYS)
        }
        if (state.absDay < state.arc12DueAbsDay) return null
        if (state.holidayId != null) return null
        return CHAPTERS12[state.arc12Chapter]
    }

    fun chapterIndexOf12(id: String): Int =
        CHAPTERS12.indexOfFirst { it.id == id }

    fun noteChapterStarted12(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf12(def.id)
        if (idx < 0) return
        state.arc12Started = true
        state.arc12Chapter = idx + 1
        state.arc12DueAbsDay = if (idx == CHAPTERS12.size - 1) -1
            else state.absDay + CHAPTER_GAP_DAYS
    }


    // ---------------- the Journal board ----------------

    fun boardVisible12(state: GameState): Boolean =
        state.arc12Started

    fun statusStamp12(state: GameState): String = when {
        state.arc12Retired -> when (state.arc12Branch) {
            "read" -> "📖 READ ALOUD"
            "named" -> "📖 CLOSED BY NAME"
            "standing" -> "📦 LEFT STANDING"
            else -> "📦 NEVER READ"
        }
        returnsDue(state) -> "📖 THE READING IS DUE"
        pendingItem12(state) != null -> "📦 A RETURN IS READABLE"
        state.arc12Started -> "📦 THE RETURNS LEDGER"
        else -> "📦 —"
    }

    // The town's current theory of the returns — the
    // loudest reading in the building, which is data
    // about the town, not the case.
    fun currentTheory12(state: GameState): String {
        if (state.arc12Retired) return when (state.arc12Branch) {
            "read" -> "the room knows now: two returns were " +
                "staged by the class's own news service, filing " +
                "people's losses where they'd make the best story"
            "named" -> "the notebook did it — or so the room " +
                "decided; his objections are on file, in his " +
                "own hand, under 'inspection'"
            "standing" -> "a return is a return; the school has " +
                "agreed, silently and together, not to ask hands"
            else -> "the box did it, or the weather did; nobody " +
                "was asked, and nobody answered"
        }
        return when {
            state.arc12Chapter <= 1 -> "the box is haunted, or " +
                "the new kid's cubby is a portal; the building " +
                "is split down the middle"
            state.arc12Chapter == 2 -> "the keeper's rounds are " +
                "sloppy — honest hands, wrong things, that's " +
                "all a lost-and-found ever is"
            state.arc12Chapter == 3 -> "the notebook kid is " +
                "running a five-year plan and the slips are " +
                "phase one; the room has its name already"
            else -> "everything is read except what the sort " +
                "couldn't settle; the school is saving its " +
                "voice for the reading"
        }
    }

    fun forecastLine12(state: GameState): String {
        if (state.arc12Retired) return ""
        val bits = ArrayList<String>()
        val pending = pendingItem12(state)
        if (pending != null) bits += "a return is ready to be " +
            "read (${pending.title.lowercase()})"
        val held = CLUES12.filter {
            it.id in state.mystery12Clues && !clueLive12(state, it) }
        if (held.isNotEmpty()) bits += "${held.size} held " +
            "piece(s) settle when their context comes"
        if (state.shenaniganId == FINALE12_ID &&
            !boardCalled12(state))
            bits += "the reading can't be called until every " +
                "return carries a reading"
        if (state.arc12DueAbsDay > state.absDay)
            bits += "the next chapter is booked for " +
                "day ${state.arc12DueAbsDay}"
        return if (bits.isEmpty()) "" else "• next: " +
            bits.joinToString("; ") + "."
    }

    fun boardLines12(state: GameState): List<String> {
        val out = ArrayList<String>()
        if (!boardVisible12(state)) return out
        out += "📦 CASE #12 — “${CASE_TITLE.uppercase()}”"
        out += statusStamp12(state)
        if (!state.arc12Retired)
            out += "Nothing is missing. Things are coming back — " +
                "to the wrong owners, in their own handwriting. " +
                "The ledger reads each return three ways: THE " +
                "LOSS (was it lost, when, by whom), THE HAND " +
                "(whose writing is the slip really in) and THE " +
                "FIT (is the thing actually theirs). The checks " +
                "are derived off the game's own records every " +
                "time this board is opened — the board keeps " +
                "facts; it has never once kept a conclusion."
        val heldCount = CLUES12.count { clueLive12(state, it) }
        val heldTotal = state.mystery12Clues.count {
            it in CLUE12_IDS }
        out += "• evidence held: $heldCount live of $heldTotal " +
            "held (${CLUES12.size} in the case). A reading is " +
            "priced at 2 LIVE pieces bearing on the return — " +
            "a piece whose context hasn't come is in the hand, " +
            "but it isn't live."
        for (item in ITEMS12) {
            val f = findings12(item)
            out += ""
            out += "${item.index + 1}. ${item.title} — surfaced " +
                "at ${item.place}; in hand: ${item.keeper}."
            out += "   the return claims: ${item.claimLabel}, " +
                "delivered to ${item.recipient}."
            out += "   “${item.slipText}”"
            out += if (f.lossOk)
                "   • THE LOSS: filed and real — the spike " +
                    "carries a ${item.kind} under " +
                    "${item.recipient}'s name."
            else
                "   • THE LOSS: the spike carries " +
                    (item.filedKind?.let {
                        "a $it under ${item.recipient}'s " +
                            "name — not a ${item.kind}" }
                        ?: "nothing at all under " +
                            "${item.recipient}'s name for a " +
                            item.kind) + "."
            val card = handCard12(item.recipient)
            out += "   • THE HAND: ${handShapeLabel12(handShape12(item))} — " +
                "${item.recipient}'s hand, as the record keeps " +
                "it: ${card?.habit}"
            if (f.formsHit.isNotEmpty())
                out += "     forms found: ${f.formsHit.joinToString(", ")}"
            if (f.mustMissed.isNotEmpty())
                out += "     the hand's must-form is " +
                    "missing: ${f.mustMissed.joinToString(", ")}"
            if (f.drillHit.isNotEmpty())
                out += "     the copybook's forms ride this " +
                    "slip: ${f.drillHit.joinToString(", ")}"
            if (f.neversHit.isNotEmpty())
                out += "     forms the hand NEVER shows: " +
                    f.neversHit.joinToString(", ")
            if (f.tellsHit.isNotEmpty())
                out += "     a copier's tells ride this slip: " +
                    f.tellsHit.joinToString(", ")
            if (hasRosterForm12(item))
                out += "     the slip addresses its cubby the " +
                    "roster way — surname first, initial after — " +
                    "the way the printed sheet by the box prints " +
                    "them; kids sign with first names."
            out += if (f.fitOk)
                "   • THE FIT: exact — the filing's facts " +
                    "(${item.claimedMarks.joinToString(", ")}) " +
                    "are the returned thing's own facts."
            else
                "   • THE FIT: broken — the filing claims " +
                    "(${item.claimedMarks.joinToString(", ")}); " +
                    "the returned thing is " +
                    "(${item.itemMarks.joinToString(", ")})."
            out += "   • THE LEDGER'S SHAPE, DERIVED: " +
                "${shapeLabel12(shape12(item))} SHAPED"
            for (clue in CLUES12) {
                if (clue.site != item.index) continue
                if (clue.id !in state.mystery12Clues) continue
                out += if (clueLive12(state, clue))
                    "   🔎 ${clue.title}: ${clue.detail}"
                else
                    "   🔒 ${clue.title}: held — its context " +
                        "hasn't arrived yet, and in this case " +
                        "context is a kind of handwriting."
            }
            val call = committedCall12(state, item)
            val verdict = letterVerdict12(state, item)
            when {
                call == null -> {
                    val live = liveEvidence12(state, item)
                    if (itemCallable12(state, item))
                        out += "   → ready to be read: it can be " +
                            "committed at $live live pieces."
                    else if (state.arc12Chapter <= item.minChapter)
                        out += "   → its chapter hasn't come."
                    else
                        out += "   → reading it takes " +
                            "${item.callPrice} live pieces " +
                            "bearing on it; $live in hand."
                }
                verdict == "PROVEN" ->
                    out += "   ✅ PROVEN: ${readingText12(call)}"
                verdict == "REFUTED" ->
                    out += "   💔 REFUTED — strike it and read " +
                        "again: ${readingText12(call)}"
                else ->
                    out += "   📌 COMMITTED (ungraded): " +
                        readingText12(call)
            }
        }
        out += ""
        out += "🤔 town theory: ${currentTheory12(state)}"
        if (!state.arc12Retired) {
            val fc = forecastLine12(state)
            if (fc.isNotEmpty()) out += fc
            out += "THE READING'S TERMS, PUBLISHED: the full " +
                "read requires both load-bearing returns — " +
                "the diary and the folded paper — PROVEN, " +
                "every return read, none standing refuted, " +
                "and at least four of the six proven. Naming " +
                "the hand takes one proven return. Standing " +
                "by the box, or walking away, takes nothing " +
                "but the nerve."
        } else {
            out += when (state.arc12Branch) {
                "read" -> "🏁 closed: read aloud — the diary " +
                    "home to its owner, the folded paper home " +
                    "unopened, and the notebook cleared in the " +
                    "same breath that named the hand."
                "named" -> "🏁 closed: by name — the notebook's " +
                    "owner carries the case, and his objection " +
                    "is filed where he always said it would be: " +
                    "in the open."
                "standing" -> "🏁 closed: left standing — the " +
                    "returns keep their desks, and the school " +
                    "keeps its peace, and the box keeps " +
                    "everything else."
                else -> "🏁 closed: never read — the ledger " +
                    "shut with its six returns inside it."
            }
            if (state.arc12PostPending)
                out += "• a week on, somebody still owes the " +
                    "two staged returns a quiet correction."
            if (state.arc12Posted)
                out += "• and a week on, the correction came: " +
                    "the diary and the paper, home at last, " +
                    "with plain slips in nobody's studied hand."
        }
        return out
    }

    // ---------------- aftermath & ambient ----------------
    // Standing lines: the ending stays in one person's
    // mouth at the school, whichever way it landed.
    private val AFTERMATH12: Map<String, Map<String, List<String>>> =
        mapOf(
            "read" to mapOf("Bebe" to listOf(
                "For the record: I never lost a diary, I never wrote that slip, and my handwriting is not a public utility. Whoever wants it can admire it from a distance, like everyone else.",
                "The diary's back with its actual owner and the slip count at this school has gone WAY down. Funny how a story loses interest once it's been read out loud.")),
            "named" to mapOf("Nathan" to listOf(
                "The notebook remains available for inspection. It has always been available for inspection. I am having that printed on a card. People can read IT in the hall.",
                "Phase one of my plan was never 'frame myself for stationery crimes.' I keep my plans in the notebook. The school is welcome to check — that's still the difference.")),
            "standing" to mapOf("Butters" to listOf(
                "The desk's still runnin', the spike's still pokin', and nobody asks a thing where it came from. That's my favorite kind of peace, fellas.",
                "I still match 'em by the cards, same as always. If a slip looks a little studied, well — I ain't the handwritin' police. I just run the desk.")),
            "unread" to mapOf("Tweek" to listOf(
                "MY MITTEN IS STILL THE WRONG MITTEN. IT'S TOO SMALL AND IT'S FOR THE WRONG HAND AND NOBODY WILL SAY WHY, GAAAH.",
                "Everyone says just wear it. JUST WEAR IT. It's not ABOUT the mitten, it's about the PRINCIPLE of the mitten! ...It's a little about the mitten."))
        )

    fun aftermathLine12(state: GameState, npcName: String): Line? {
        if (!state.arc12Retired) return null
        val bank = AFTERMATH12[state.arc12Branch]
            ?: AFTERMATH12[DEFAULT_BRANCH] ?: return null
        val lines = bank[npcName] ?: return null
        if (lines.isEmpty()) return null
        val idx = ((state.absDay % lines.size) + lines.size) % lines.size
        return Line(npcName, lines[idx])
    }

    // The ending drifts into ordinary talk at the
    // school, the way every ending in this town does.
    fun ambientCallback12(state: GameState,
                          random: kotlin.random.Random): String? {
        val banks = when (state.arc12Branch) {
            "read" -> listOf(
                "Turns out half the returns were honest and two were stories somebody planted. The box checks slips now. The box has STANDARDS now.",
                "The diary went home and the folded paper went home and nobody new read either. That's what a correction looks like when it's done right.")
            "named" -> listOf(
                "They closed the returns on the notebook kid. He keeps saying the notebook's available for inspection. Honestly? A guilty kid would've burned the notebook.",
                "He wrote his objection on a card and put it ON the spike. Filed. In his own hand. You have to respect the system, even when it's eating him.")
            "standing" -> listOf(
                "The school let the returns stand. The mitten's still wrong, the diary's still on the wrong desk, and everyone's agreed that's just how it is now.",
                "Ask anybody whose hand the slips were in and watch the conversation change the subject itself.")
            else -> listOf(
                "The returns case? Never did get read. The box is still over by the office. Things still come back sometimes. Nobody asks.",
                "Six returns and not one read aloud. In this town, that's practically a filing system.")
        }
        return banks[random.nextInt(banks.size)]
    }
}

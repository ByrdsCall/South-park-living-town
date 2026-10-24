package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery7 — the seventh case (v0.9.0f, Queue #9 #6):
// "The Case Told Backwards".
//
// The pond case was witness-driven, the booth case a
// compressed whodunit, the buried case an excavation, the
// gardener case followed acts of maintenance, the
// confession case was built of claims, and the opinion
// case was a meter. This one's shape is its TIMELINE:
// it opens with its own ending. On Founders' Assembly
// morning, in front of the whole school, Sheila Broflovski
// walks up to the display table, takes the school's
// silver Cornerstone Trowel off it — the trowel that laid
// the Elementary's first stone, missing from its case
// since the fire winter sixty years ago — announces to
// the room that the McCormick family has had it all these
// years, and carries it out to applause. The town treats
// it as settled by lunch. A rescue, witnessed by thirty
// people. The player is left holding an ending they
// cannot yet read.
//
// The investigation runs in REVERSE story-time. Four
// movements, reconstructed latest-first: the taking
// itself, then the night before it, then the ledger that
// started it, then — last, in the finale chapter — the
// fire winter where it truly began. The board is a
// TIMELINE laid out in true chronological order and
// filled in backwards. Each movement, once the player
// holds enough live evidence tagged to it, is COMMITTED
// as a reconstruction — one of two written readings, in
// the ledger discipline of the confession case — and a
// committed reading is testable by the past: as earlier
// movements surface, a reading is CONFIRMED by the piece
// that supports it, or BROKEN by the piece that refutes
// it. Verdicts are DERIVED from recorded inputs (the
// commits, the evidence in hand, the chapters begun),
// never stored. The sting is structural: the
// confrontation's reconstruction is committed FIRST, on
// the least evidence — and it is breakable by design.
// The most dramatic fact in the case is the least
// reliable one in it.
//
// Evidence: 12 pieces — 5 witness tellings, 5 Look finds,
// 2 participation beats — each tagged to its movement.
// Two pieces are UNREADABLE when found: they go into the
// evidence set honestly inert (an initials-only line; a
// smeared carbon), described as not-yet-making-sense,
// and become testimony-grade — LIVE — only once the
// chapter that unlocks their context has begun. A piece
// that is not live cannot be spent: it counts toward
// nothing, confirms nothing, breaks nothing. The suite
// proves it.
//
// The finale is the confrontation, AGAIN: a week after
// the taking, the school holds the trowel's homecoming
// ceremony — the same room, the same people, the argument
// scheduled to reignite in public — and this time the
// player plays the scene with the whole timeline known.
// Four calls: SAY WHAT REALLY STARTED IT (gated on the
// beginning movement committed, confirmed, and standing —
// the finale reads the FINAL state of the timeline, not
// first impressions), LET THE ORIGINAL ENDING STAND,
// CHANGE ONE THING (a specific, timeline-supported
// intervention; gated on at least one confirmed
// movement), or WALK AWAY. A ceremony nobody plays is
// its own landing: cold.
//
// The solution, in one breath, for the payoffs only: the
// trowel was never stolen. The winter the gym caught
// fire, the case cracked in the heat and the first
// principal — Enoch Webb — asked the school caretaker,
// old Mr. McCormick, to take the trowel home, clean the
// smoke off it, and keep it safe until the case could be
// mended. The case never was. The family kept it in
// trust for sixty years, cleaned it yearly, and lent it
// back for every Founders' display — the programs say
// so, in print, every year, "on loan from its keeper."
// The ledger Sheila found is real: it records the
// removal. Two pages on, in the same volume, it records
// the return into the keeper's hands — the page she
// never turned to. Sheila acted on a genuine document,
// in genuine belief, for the school's sake: the scene
// was real, and what it MEANT is the case. The keeper's
// silence the night before was pride, not guilt: she
// offered the trowel for the display, as always, and
// refused only to sign a confession she didn't owe. The
// game never says who was right. In every landing,
// somebody says the uncomfortable thing, standing,
// forever.
//
// Structure:
//  - CHAPTERS7: four full ShenaniganDefs on the same
//    episode machinery, outside the random pool,
//    resolving in SHENANIGAN_BY_ID like every case.
//  - Gating: the case queues into the mystery slot TEN
//    days after the opinion case retires. While any
//    earlier case is open this one does not even schedule.
//  - Retirement: every landing retires the arc forever.
//    The origin landing alone gets the long tail: a week
//    on, the school rebuilds the trowel's case at last,
//    and the trowel goes home under glass with the whole
//    story on the plaque.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery7 {

    const val CASE_TITLE = "The Case Told Backwards"
    const val FINALE7_ID = "arc7_ch4_beginning"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the opinion case retires

    /** The two readers of the scene — public premise; both
     *  are named in the first hour of the first chapter, by
     *  the crowd, on the strength of what thirty people saw
     *  and nobody checked. That is the case. */
    const val TAKER = "Sheila"
    const val KEEPER = "Carol"

    val CHOICE_KEYS = listOf("origin", "stand", "change", "walk")
    const val DEFAULT_BRANCH = "cold"
    val BRANCH_KEYS = listOf("origin", "again", "mended", "cold")

    const val CASE_RETURN_DELAY_DAYS = 7

    /** The two candidate readings of every movement, as the
     *  commit dialog and the save file name them. The easy
     *  reading is the one the scene hands you. The checked
     *  reading is the one the evidence assembles. */
    val PICK_KEYS = listOf("easy", "checked")

    // ---------------- the four chapters ----------------
    // They air in the player's present, one a week, and
    // each one reconstructs an EARLIER movement than the
    // one before: the taking (witnessed cold), the night
    // before, the ledger, and — in the finale — the fire
    // winter where it began, with the homecoming ceremony
    // waiting at its climax.

    val CHAPTERS7: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc7_ch1_the_taking",
            title = "The Taking",
            cause = "It's Founders' Assembly at the Elementary — " +
                "the one morning a year the school shows off its " +
                "own history. On the display table by the gym door " +
                "sits the Cornerstone Trowel: silver, engraved, " +
                "the trowel that laid this school's first stone, " +
                "out of its case for the day the way it always is " +
                "on Founders'. The case itself has been empty " +
                "since the fire winter, sixty years back. Everybody " +
                "knows that part. What happens next, everybody " +
                "sees.",
            blurb = "In front of the whole assembly, Sheila " +
                "Broflovski stands up, walks to the display table, " +
                "picks up the trowel, and tells the room where it " +
                "has really been for sixty years — and whose family " +
                "has been keeping it. Then she carries it out, to " +
                "applause. The town calls it a rescue by lunch. " +
                "You were there. You saw all of it. You have no " +
                "idea what you saw.",
            theme = QuestTheme("🕰️", "#7a5a2e", "THE CASE TOLD BACKWARDS"),
            tintHex = "#221a10",
            stages = listOf(
                ShenaniganStage("Kickoff: Founders' Morning",
                    "🕰️ FOUNDERS' ASSEMBLY: The gym smells of floor " +
                        "wax and old paper. The display table holds " +
                        "the school's whole biography: the first " +
                        "bell-book, a photograph of a photograph of " +
                        "the cornerstone laying, and the trowel — " +
                        "silver, heavy, engraved with the year — " +
                        "sitting on green felt the way it sits every " +
                        "Founders' morning, out where everyone can " +
                        "see it.",
                    "The trowel's case stands by the gym door, " +
                        "empty, the way it has stood for sixty years " +
                        "— glass cracked in a long-ago fire, a little " +
                        "card inside that just says THE CORNERSTONE " +
                        "TROWEL, as if the thing were out on an " +
                        "errand. Nobody looks at the case. Everybody " +
                        "looks at the trowel. That turns out to be " +
                        "the whole problem, in miniature.",
                    "See the display table at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Setting out the school's history. Reading the little cards. Standing where the applause will be.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff runs Founders' the way it runs everything: on time, in order, and with the good felt."))),
                ShenaniganStage("Escalation: She Stands Up",
                    "🕰️ Midway through the program — between the " +
                        "choir and the cornerstone speech — Sheila " +
                        "Broflovski stands up in the third row. She " +
                        "is not on the program. She walks to the " +
                        "display table the way people walk in " +
                        "courtrooms on television, picks up the " +
                        "trowel in both hands, and turns around to " +
                        "face the room.",
                    "She says it clearly, so the back can hear: " +
                        "the trowel was never lost. It has been in " +
                        "the McCormicks' trailer for sixty years. " +
                        "Her voice does what her voice does in rooms " +
                        "— it makes a verdict feel like a weather " +
                        "report. Heads turn toward Carol McCormick, " +
                        "who is sitting very still in the folding " +
                        "chairs, holding the program with both hands.",
                    "Watch the room at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Standing up out of turn. Turning around with a silver trowel in both hands.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are on their feet before the sentence ends. The Parents love a sentence with a villain in it."))),
                ShenaniganStage("Climax: Taken Back",
                    "🕰️ CLIMAX: 'I am taking this back,' she tells " +
                        "the room — the room, not Carol — 'for the " +
                        "school.' And she walks out with it, down " +
                        "the center aisle, while the room applauds " +
                        "itself for having witnessed a rescue. Carol " +
                        "McCormick does not stand up. She does not " +
                        "say a word. She folds her program in half, " +
                        "then in half again, and leaves by the side " +
                        "door when the choir starts.",
                    "Thirty people saw it. Thirty people can tell " +
                        "you exactly what happened: a stolen thing, " +
                        "rescued, in public. You saw it too. What you " +
                        "saw was a woman pick up a trowel and walk " +
                        "down an aisle. Everything else — stolen, " +
                        "rescued, sixty years of keeping explained " +
                        "in one sentence — the room brought with it, " +
                        "and handed to you on the way out, wrapped " +
                        "in applause.",
                    "You were there. Write down what you actually saw.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Applauding a rescue. Folding a program into smaller and smaller squares.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff is doing the arithmetic more slowly than the room. The Staff has to work in this building on Monday."))),
                ShenaniganStage("Resolution: Settled, By Lunch",
                    "🕰️ By lunch the story has set, like the paint " +
                        "on the last case's wall: the McCormicks had " +
                        "it, Sheila took it back, case closed, somebody " +
                        "should do something about that trailer. The " +
                        "trowel is downtown, in a drawer at City Hall, " +
                        "'pending the homecoming.' The school has " +
                        "announced a little ceremony for next week, " +
                        "when the trowel comes home for good.",
                    "An ending, delivered in one morning, with " +
                        "thirty witnesses and no questions. It has " +
                        "only one flaw: you were there for the whole " +
                        "of it, and you cannot make the middle of " +
                        "it hold still. Endings are supposed to " +
                        "explain beginnings. This one just points " +
                        "at a trailer and stops. The board in your " +
                        "Journal has opened a timeline — the story " +
                        "in the order it actually happened. It's " +
                        "mostly empty. It starts sixty years ago.",
                    "Hold the ending up to the light. Then start before it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Sheila", "Carol"),
            episodeTitles = listOf("The Taking", "Thirty Witnesses"),
            coldOpen = listOf(
                Line("Sheila", "What I did, I did in the open, in " +
                    "front of the whole school, because that is " +
                    "where it had to be done. Sixty years that " +
                    "trowel sat in a trailer while this school told " +
                    "its children a case was empty because of a " +
                    "fire. I read the record. I READ it. If the " +
                    "town wants to argue with the record, the town " +
                    "can argue with me first."),
                Line("Victoria", "For the minutes — and I keep the " +
                    "minutes — the assembly was interrupted at " +
                    "nine-seventeen by an unscheduled item. The " +
                    "item picked up school property and left with " +
                    "it, applauded. I have written it down exactly " +
                    "that way, because writing it down exactly is " +
                    "the only power this office has ever had, and " +
                    "I intend to keep using it."))),
        ShenaniganDef(
            id = "arc7_ch2_night_before",
            title = "The Night Before",
            cause = "A week after the taking, the town is still " +
                "telling the assembly story — but stories told " +
                "backwards start to itch, and this one's second " +
                "scene happened the evening before the assembly, " +
                "in a trailer across town, with no audience at " +
                "all. Sheila went to the McCormicks' house the " +
                "night before she stood up. That much, everybody " +
                "who was on that street agrees on. What happened " +
                "inside is the part with no witnesses — only " +
                "walls, a patrol car, and a kitchen table.",
            blurb = "Before the public scene there was a private " +
                "one: a knock, a kitchen table, a demand and a " +
                "refusal. The town has already decided what the " +
                "refusal meant. The town decides what refusals " +
                "mean the way it decides everything — quickly, " +
                "and in the direction of the story it already has.",
            theme = QuestTheme("🌙", "#4a5a7a", "THE NIGHT BEFORE"),
            tintHex = "#141a24",
            stages = listOf(
                ShenaniganStage("Kickoff: The Street Remembers a Knock",
                    "🌙 THE NIGHT BEFORE: The street by the trailer " +
                        "remembers the shape of it even if it can't " +
                        "swear to the words: a car that doesn't " +
                        "belong to the poor end of town, a knock at " +
                        "eight, a voice through a wall for the " +
                        "length of one argument, and then a long, " +
                        "long quiet.",
                    "Trailer walls are honest about volume and " +
                        "useless about content. The street heard a " +
                        "demand arrive at full volume and something " +
                        "quieter answer it for a long time. The town " +
                        "has since filled the quiet with a confession " +
                        "that was refused. The town fills quiets " +
                        "professionally.",
                    "Walk the street by Kenny's House. Listen to what walls keep.",
                    routine = "cluster", clusterLoc = "KennysHouse",
                    clusterActivity = "Remembering a knock. Arguing about what the quiet meant.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents have heard about the night before. The Parents are fitting it to the story the way you fit a lid to a pot it almost matches."))),
                ShenaniganStage("Escalation: What the Refusal Became",
                    "🌙 By the time the story reaches the coffee " +
                        "line, the night before has grown a plot: " +
                        "she went there to give them a chance — sign " +
                        "a paper, admit it, hand it over — and the " +
                        "woman REFUSED. Cornered, caught, silent. " +
                        "The town nods at the word refused the way " +
                        "it nods at a diagnosis.",
                    "A refusal is an act with the words left out, " +
                        "which makes it the most portable evidence " +
                        "in the world: it fits in any story you carry " +
                        "it into. What was actually refused — a " +
                        "thing, a paper, a version of events — is a " +
                        "question nobody in the coffee line is " +
                        "asking, because the coffee line has the " +
                        "answer it ordered.",
                    "Ask what, exactly, was refused. Precisely.",
                    routine = "cluster", clusterLoc = "KennysHouse",
                    clusterActivity = "Refusing, in retrospect, more completely every day.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: The Kitchen Table, Reconstructed",
                    "🌙 CLIMAX: Stand in that kitchen in your mind " +
                        "and put the evening together from what the " +
                        "walls and the patrol and one unsent piece " +
                        "of paper can actually swear to: a demand, " +
                        "made loudly. A refusal, made quietly. And " +
                        "then — attested by a patrolman's ears and " +
                        "nobody's story — the sound of something " +
                        "being wrapped, carefully, in paper, for a " +
                        "long time, like something being got ready.",
                    "Whatever was wrapped in that kitchen was " +
                        "wrapped the night BEFORE the assembly — " +
                        "before anyone knew there would be a scene " +
                        "to refuse or confess at. Timelines are " +
                        "rude that way. They keep insisting that " +
                        "the night before comes before the morning " +
                        "after, no matter which order you learn " +
                        "them in.",
                    "Reconstruct the night before. From what's sworn, not what's said.",
                    routine = "cluster", clusterLoc = "KennysHouse",
                    clusterActivity = "Wrapping something carefully, in paper, the night before anyone knew.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has started asking a question in the staff room, quietly, the way staff rooms do: refused WHAT, exactly?"))),
                ShenaniganStage("Resolution: The Timeline Grows a Night",
                    "🌙 The timeline on your board has a night in " +
                        "it now — the second-to-last thing that " +
                        "happened, reconstructed from walls and " +
                        "paper. It sits one slot before the taking, " +
                        "and it does not match the taking's story " +
                        "the way it should. There is a shape in it " +
                        "that points further back: a demand doesn't " +
                        "arrive from nowhere. Somewhere behind the " +
                        "night before there is a day Sheila found " +
                        "something.",
                    "Reconstructing backwards is slow work, and " +
                        "it has a discipline the town never uses: " +
                        "you may only write down what the pieces " +
                        "under your hands can carry. One movement " +
                        "at a time. The beginning is still sixty " +
                        "years away, and it is in no hurry at all.",
                    "Find the day the demand came from.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Carol", "Sheila"),
            episodeTitles = listOf("The Night Before", "Refused, Precisely"),
            coldOpen = listOf(
                Line("Carol", "She came to my house. She stood in " +
                    "my kitchen and she told me what my family " +
                    "is, in her good coat, at my table. I listened. " +
                    "I have listened to this town describe my " +
                    "family my whole life; it has excellent " +
                    "diction and no information. What I said to " +
                    "her, I said. What I didn't sign, I didn't " +
                    "sign. That's the whole of my statement, and " +
                    "it's more than the town's given me in sixty — " +
                    "in all my years."),
                Line("Barbrady", "Nothing to see here, and I mean " +
                    "that as a sworn statement. I was ON that " +
                    "street that night. Heard the whole thing, in " +
                    "the sense that I heard the parts of it that " +
                    "have volume. Move along. There is nothing to " +
                    "see, there is only to hear, and I heard it, " +
                    "and it's mine and the report's."))),
        ShenaniganDef(
            id = "arc7_ch3_the_ledger",
            title = "The Ledger",
            cause = "Behind the night before there is a quieter " +
                "scene: a reading room, an index card, a bound " +
                "school ledger from the fire winter, pulled from " +
                "the City Hall archive a month before the " +
                "assembly. Sheila did research. Nobody in this " +
                "town disputes that Sheila did research — research " +
                "is her native country. The ledger is real, the " +
                "entry is real, and the entry is why a silver " +
                "trowel got carried down a school aisle to applause.",
            blurb = "The story started, as so many of this town's " +
                "stories start, with a document that says less " +
                "than the person reading it. The ledger waits in " +
                "the archive, bound, indexed, patient — with " +
                "more pages than anybody has yet read aloud.",
            theme = QuestTheme("📒", "#5a4a6e", "THE LEDGER"),
            tintHex = "#1c1624",
            stages = listOf(
                ShenaniganStage("Kickoff: The Reading Room",
                    "📒 THE LEDGER: The archive at City Hall keeps " +
                        "the school's old years the way the school " +
                        "keeps its minutes: completely, and without " +
                        "the slightest idea which parts matter. " +
                        "Bound volumes. Index cards. A request slip " +
                        "for every hand that ever pulled a book. " +
                        "The fire-winter ledger is back on its " +
                        "shelf, reshelved, its errand done.",
                    "An archive is the one room in town where the " +
                        "past is stored in the order it happened. " +
                        "Everywhere else, the town keeps it in the " +
                        "order it was told. The volume Sheila read " +
                        "is heavy, and bound, and — this is the part " +
                        "the story skipped — longer than one page.",
                    "Go to the archive at City Hall. Ask for the whole volume.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Keeping everything. Knowing nothing. Being quoted from, partially.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff keeps the minutes and the archive keeps the years, and between the two of them the whole truth is filed in this town — in pieces, in different buildings."))),
                ShenaniganStage("Escalation: The Page Everybody Quotes",
                    "📒 The removal line is famous now — the town " +
                        "can recite it: the trowel, the fire winter, " +
                        "'removed,' and the caretaker's name in the " +
                        "caretaker's own hand. A signed confession, " +
                        "sixty years old, dug up by research. The " +
                        "town rests its case on a sentence it has " +
                        "never seen in its paragraph.",
                    "A line in a ledger is a fact with its context " +
                        "still attached, if you keep turning pages. " +
                        "The town quoted the line and closed the " +
                        "book — literally: the volume went back to " +
                        "the shelf with its later pages unread, in " +
                        "a building that records every reader and " +
                        "every request, and keeps those records " +
                        "too.",
                    "Read the line in its paragraph. All of its paragraph.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Quoting a line. Resting a case on it. Never having seen its paragraph.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: The Volume, Whole",
                    "📒 CLIMAX: The volume is on the reading table " +
                        "in front of you, all of it — the officers' " +
                        "list on the front board, the winter's " +
                        "entries in order, the fire, the smoke, the " +
                        "accounts. It is the first time in this " +
                        "whole case that you have held the past in " +
                        "the order the past actually happened in, " +
                        "and it changes the temperature of every " +
                        "scene you have already reconstructed.",
                    "Documents don't have tones of voice. That's " +
                        "their mercy and their menace: a removal is " +
                        "a removal, whatever carried it out of the " +
                        "building — a thief's bag or a caretaker's " +
                        "towel. The volume knows which. Volumes " +
                        "always know which; that's what the rest of " +
                        "the pages are for.",
                    "Read the volume whole. Then re-read your timeline.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Being read whole, for the first time in sixty years.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "Somebody in the Staff has started reading documents ALL the way through. It is spreading, slowly, like a rumor of competence."))),
                ShenaniganStage("Resolution: One Movement Left",
                    "📒 The timeline has three of its four " +
                        "movements now, and the story they make — " +
                        "read forward, the way time actually ran — " +
                        "is not the story the assembly applauded. " +
                        "One slot is still empty: the first one. " +
                        "The fire winter itself. The beginning, " +
                        "which in this case is the only thing left " +
                        "to find, and the only thing that can say " +
                        "what the ending meant.",
                    "The homecoming ceremony is days away — the " +
                        "trowel comes back to the school, the room " +
                        "refills, the argument is scheduled to " +
                        "reignite in public, politely, with a " +
                        "program. Whatever the beginning turns out " +
                        "to be, you will be standing in that room " +
                        "holding it. Reconstruct it first. It's the " +
                        "last piece of the past, and the whole case " +
                        "is leaning on it.",
                    "Find the beginning. It's the only direction left.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Gerald"),
            episodeTitles = listOf("The Ledger", "The Whole Volume"),
            coldOpen = listOf(
                Line("Gerald", "I pulled a book. That's my whole " +
                    "involvement, and I have rehearsed saying it " +
                    "in case of lawyers: Sheila needed a volume " +
                    "from the archive, I was going past City Hall, " +
                    "I signed the slip and pulled the book BY ITS " +
                    "CARD, the whole book, both gatherings, the " +
                    "way the index lists it. If pulling a book is " +
                    "a conspiracy, then the library is a crime " +
                    "syndicate and I am its most punctual member."),
                Line("Victoria", "The archive is a memory with " +
                    "shelves. People come to it for ammunition and " +
                    "leave with half a document, and then the " +
                    "school gets an assembly. I have watched it " +
                    "happen in this district for — some years. " +
                    "Read the whole volume. That's my entire " +
                    "sermon. Read the whole volume."))),
        ShenaniganDef(
            id = FINALE7_ID,
            title = "The Fire Winter",
            cause = "The last movement is the first one. Sixty " +
                "winters ago the Elementary's gym caught fire in " +
                "the night — a heater, a curtain, a school full " +
                "of smoke by morning and every child safe, which " +
                "is the part the town still tells correctly. The " +
                "trowel's glass case stood by the gym door. In " +
                "the morning the case was cracked and the trowel " +
                "was gone, and the town — younger then, and no " +
                "wiser — wrote 'lost in the fire' on a little " +
                "card and believed it for sixty years.",
            blurb = "The beginning, reconstructed last: a fire, " +
                "a cracked case, a caretaker, a principal, and a " +
                "sentence spoken in a smoky gym that nobody wrote " +
                "down. This week the school holds the trowel's " +
                "homecoming, and the argument comes back to the " +
                "room where it started — with you holding the " +
                "whole timeline, in order, for the first time.",
            theme = QuestTheme("🔥", "#a04a28", "THE FIRE WINTER"),
            tintHex = "#241209",
            stages = listOf(
                ShenaniganStage("Kickoff: What the Oldest Remember",
                    "🔥 THE FIRE WINTER: The beginning has " +
                        "witnesses still — barely, and at the " +
                        "speed of very old men remembering being " +
                        "nine. The fire winter is in living memory " +
                        "the way the far end of a long table is in " +
                        "a room: technically, and if you lean.",
                    "Sixty years is a long time for a town to " +
                        "keep a wrong card in a glass case, and a " +
                        "short time for the people who were there " +
                        "to stop being there. Ask now. The " +
                        "beginning is the only movement whose " +
                        "evidence is still walking around, and it " +
                        "walks slower every year.",
                    "Find the oldest memory of the fire winter.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Remembering being nine. Leaning toward the far end of the table.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has taken to walking past the empty case by the gym door a little more slowly. It notices things now."))),
                ShenaniganStage("Escalation: The Pieces Wake Up",
                    "🔥 Some of what you have been carrying for " +
                        "weeks — a line of initials in a caretaker's " +
                        "day-book, a smeared carbon in an archive " +
                        "box — has started, quietly, to make sense: " +
                        "the fire winter gives old paper its names " +
                        "back. Evidence you could not spend is " +
                        "evidence now.",
                    "This is the backwards case's whole trick, " +
                        "played fair: the past explains the present " +
                        "if you reconstruct it in the right order — " +
                        "which is to say, the order it happened, " +
                        "which is to say, the reverse of the order " +
                        "you lived it. The timeline on your board " +
                        "has one empty slot left, at the very " +
                        "beginning, where beginnings go.",
                    "Re-read what you couldn't read. Write the beginning.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Making sense, backwards, all at once.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: The Homecoming",
                    "🔥 CLIMAX: The homecoming ceremony, the gym " +
                        "again — the trowel carried in on its green " +
                        "felt, the folding chairs, the program with " +
                        "a blank space in it where the school's " +
                        "history is supposed to be read aloud. " +
                        "Sheila is here, chin up. Carol is here, " +
                        "in the back, because the school asked and " +
                        "she has never once refused the school " +
                        "anything. The room is warming up to argue " +
                        "the assembly all over again.",
                    "It is the same scene. That is the point of " +
                        "it: the same room, the same people, the " +
                        "same silver sentence at the center — and " +
                        "you, this time, holding the story in the " +
                        "order it actually happened, from the fire " +
                        "winter forward. What the scene means is " +
                        "about to be decided by the only person who " +
                        "has read it whole.",
                    "Play the scene again — with the whole timeline known.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Refilling the room. Warming up to argue it all over again.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have come back for the sequel. The Parents always come back for the sequel."))),
                ShenaniganStage("Resolution: The Story, In Order",
                    "🔥 The ceremony is over, one way or another, " +
                        "and the story of the trowel is a story " +
                        "again — told in some order, by a town that " +
                        "will now tell it that way for years. What " +
                        "the record says and what the room remembers " +
                        "have been introduced to each other, or " +
                        "have not. The timeline on your board is " +
                        "full. It will stay full. Timelines, unlike " +
                        "rooms, do not adjourn.",
                    "You came to this case holding its ending, " +
                        "the way the town hands everyone its " +
                        "endings — finished, applauded, unexplained. " +
                        "You are leaving it holding the whole of " +
                        "it, beginning to end, in order, which is " +
                        "a heavier and quieter thing to carry. The " +
                        "town will go on ending stories early. It " +
                        "always does. But this one, it ends where " +
                        "you say it ends. Or it ended where it " +
                        "always did. The board keeps it either way.",
                    "The board keeps it. Beginning to end, in order.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Sheila", "Carol"),
            episodeTitles = listOf("The Fire Winter", "The Homecoming"),
            coldOpen = listOf(
                Line("Marvin", "Sixty years, and the school wants " +
                    "to know about the fire winter NOW. I was " +
                    "nine. I remember the smoke better than I " +
                    "remember last week, and I remember the man " +
                    "who carried that trowel out of the gym, and " +
                    "I remember who was standing next to him with " +
                    "a hand on his shoulder. Ask me at the school. " +
                    "Some things you say in the building they " +
                    "happened in, or you don't say them right."),
                Line("Carol", "The school asked me to come to the " +
                    "homecoming. Me. After everything said in " +
                    "that gym — they sent a note, very polite, " +
                    "'the keeper's presence is requested.' Keeper. " +
                    "Sixty — all these years the town's paperwork " +
                    "has called me the keeper, and its mouth has " +
                    "called me something else. I'll be there. I " +
                    "have never refused the school anything. It's " +
                    "the family flaw, apparently.")))
    )

    val CHAPTER7_BY_ID: Map<String, ShenaniganDef> = CHAPTERS7.associateBy { it.id }

    /** 0-based chapter index of a def id, or -1 when it's not ours. */
    fun chapterIndexOf7(defId: String?): Int =
        CHAPTERS7.indexOfFirst { it.id == defId }

    fun isChapter7(defId: String?): Boolean = chapterIndexOf7(defId) >= 0

    // ---------------- the movements (the story, in order) ----------------

    /** One movement of the story, in TRUE chronological
     *  order (index 0 = the beginning). The player fills
     *  them in backwards — index 3 first, index 0 last —
     *  committing one of two written readings for each.
     *  [easy] is the reading the scene hands you; it is
     *  FALSE for every movement, which is the case's whole
     *  argument: the first reading of a scene, taken at
     *  speed, is a story, not a fact. [checked] is the
     *  reading the evidence assembles. A committed easy
     *  reading is BROKEN once the piece named by
     *  [easyBrokenBy] is live; a committed checked reading
     *  is CONFIRMED once the piece named by
     *  [checkedConfirmBy] is live. Both tests read LIVE
     *  evidence only — an inert piece tests nothing. */
    data class Movement7(
        val id: String,
        val index: Int,                // 0 = the beginning ... 3 = the taking
        val title: String,
        val whenText: String,
        val easy: String,
        val checked: String,
        val easyBrokenBy: String,      // clue id (must be live to break)
        val checkedConfirmBy: String,  // clue id (must be live to confirm)
        /** Live pieces tagged to this movement needed before
         *  its reconstruction may be committed. */
        val evidenceNeeded: Int = 2
    )

    val MOVEMENTS7: List<Movement7> = listOf(
        Movement7("m0", 0, "The Fire Winter",
            "sixty winters ago — the beginning, reconstructed last",
            "It began in the fire winter, when the caretaker " +
                "carried the trowel out of a smoky gym and home — " +
                "and his family kept it, quietly, for sixty years, " +
                "the way families keep things that aren't theirs.",
            "It began in the fire winter, when the first " +
                "principal asked the caretaker to take the trowel " +
                "home, clean the smoke off it, and keep it safe " +
                "until the school could mend its case — a trust " +
                "the town handed over and then forgot it had given.",
            easyBrokenBy = "daybook_line",
            checkedConfirmBy = "daybook_line"),
        Movement7("m1", 1, "The Ledger",
            "a month before the assembly — the discovery",
            "The school archive proves the taking: removed, in " +
                "writing, in the fire winter, in the caretaker's " +
                "own hand — and no return, anywhere, ever.",
            "The archive records a removal — and, two pages on " +
                "in the same volume, the return into the keeper's " +
                "hands. The line was always there. It was simply " +
                "never the page anyone read aloud.",
            easyBrokenBy = "ledger_pages",
            checkedConfirmBy = "ledger_pages"),
        Movement7("m2", 2, "The Night Before",
            "the evening before the assembly — the visit",
            "Cornered at her own table the night before, the " +
                "keeper refused to give the trowel up and refused " +
                "to say why — the silence of a family with " +
                "something to hide.",
            "The keeper offered the trowel for the display, the " +
                "way she had every year — what she refused was " +
                "signing a confession first. The refusal was " +
                "pride, not guilt.",
            easyBrokenBy = "unsent_reply",
            checkedConfirmBy = "unsent_reply"),
        Movement7("m3", 3, "The Taking",
            "Founders' morning — the confrontation you witnessed",
            "A stolen thing was taken back in front of thirty " +
                "witnesses — the town watched a rescue, and the " +
                "woman who had kept it hidden all these years " +
                "watched it go.",
            "A thing out on its yearly loan was seized in " +
                "public from the family that had kept it for " +
                "sixty years — the witnesses saw the act, and " +
                "supplied the meaning themselves.",
            easyBrokenBy = "ledger_pages",
            checkedConfirmBy = "display_sheet")
    )

    val MOVEMENT7_BY_ID: Map<String, Movement7> = MOVEMENTS7.associateBy { it.id }

    /** The order the player fills the timeline in: latest
     *  movement first, the beginning last. */
    val FILL_ORDER7: List<Movement7> = MOVEMENTS7.sortedByDescending { it.index }

    // ---------------- the evidence ----------------

    enum class Kind7 { WITNESS, LOOK, PARTICIPATION }

    data class Clue7(
        val id: String,
        val kind: Kind7,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        /** The movement (chronological index) this piece
         *  belongs to — the slot of the timeline it speaks
         *  to, however late it is found. */
        val movement: Int,
        /** 0-based chapter that must have BEGUN for this
         *  piece to be LIVE — testimony-grade, spendable.
         *  Equals [minChapter] for ordinary pieces. For the
         *  two context-locked pieces it is a LATER chapter:
         *  the piece can be held, honestly inert, before
         *  the chapter that gives it its context. */
        val unlockChapter: Int,
        val title: String,
        /** The board's account of the piece once LIVE. */
        val detail: String,
        /** The board's account while held-but-inert. For
         *  ordinary pieces this equals [detail] (they are
         *  never seen inert). For locked pieces it is the
         *  honest shrug: what the thing IS, without what
         *  it MEANS. */
        val inertDetail: String,
        /** WITNESS: the cast member who tells you. */
        val npc: String? = null,
        /** WITNESS: the line they say (the telling IS the proof). */
        val spoken: String? = null,
        /** LOOK: the location to Look at. */
        val locId: String? = null,
        /** LOOK: the sentence the Look reports. */
        val foundText: String? = null,
        /** PARTICIPATION: the chapter def whose climax turf grants it. */
        val chapterDefId: String? = null
    )

    val CLUES7: List<Clue7> = listOf(
        // ---------- movement 3: the taking (chapter 1) ----------
        Clue7("exact_words", Kind7.WITNESS, 0, 3, 0,
            "Her exact words, remembered by her son",
            "The stage-crew memory of the assembly's sound: she " +
                "said Carol's name once, at the start, like an " +
                "address — and after that she never spoke to Carol " +
                "at all. 'I am taking this back,' she said, to the " +
                "room, 'for the school.' The speech was aimed at " +
                "the audience the way a verdict is aimed at a " +
                "gallery. Whatever the act was, it was performed " +
                "FOR the witnesses — which is a fact about the act, " +
                "whatever else it was.",
            "Her exact words, remembered by her son",
            npc = "Kyle",
            spoken = "I was on stage crew, so I heard it the way " +
                "you hear things from the wings — clear. Mom said " +
                "Carol's name one time, right at the start, like " +
                "she was reading an address off an envelope. Then " +
                "she never talked to Carol again. 'I am taking " +
                "this back,' she said — to the ROOM. To everybody. " +
                "'For the school.' I keep thinking about that. " +
                "If you were really just — taking something back, " +
                "wouldn't you say it to the person? She said it " +
                "to the chairs."),
        Clue7("thirty_witnesses", Kind7.PARTICIPATION, 0, 3, 0,
            "You were one of the thirty",
            "Your own hands were in the assembly — programs, " +
                "chairs, the display table's green felt. You saw " +
                "the act at the distance everyone saw it: a woman, " +
                "a trowel, an aisle, applause. Up close, afterward, " +
                "the felt on the display table shows the trowel's " +
                "outline in dust, and the dust says the thing had " +
                "been sitting there since early morning, in the " +
                "open, in its usual place — not produced from " +
                "hiding, not fetched from anywhere. It was already " +
                "there. It had been put there, gently, hours " +
                "before the scene.",
            "You were one of the thirty",
            chapterDefId = "arc7_ch1_the_taking"),
        Clue7("display_sheet", Kind7.LOOK, 0, 3, 0,
            "The display sign-in book",
            "The sign-in book for Founders' displays, kept by " +
                "the gym door: every loaned item, signed in by " +
                "the hand that brought it. The trowel's line, this " +
                "year, in pencil, early: 'Cornerstone trowel — " +
                "for the display, as always. C.M., keeper.' And " +
                "the same hand, the same line, the same word — " +
                "keeper — Founders' after Founders', as far back " +
                "as the book goes, and the book goes back thirty " +
                "years. A thing in hiding does not sign itself " +
                "in, by name, every year, in pencil, by the door.",
            "The display sign-in book",
            locId = "Elementary",
            foundText = "🔍 The display sign-in book, by the gym " +
                "door — every loaned treasure signed in by the " +
                "hand that carried it in. This year's line for " +
                "the trowel, written early, in pencil: " +
                "'Cornerstone trowel — for the display, as " +
                "always. C.M., keeper.' Page back: the same " +
                "line. Page back: the same line. Thirty years " +
                "of the same hand signing the same thing in, " +
                "in the open, by the door."),
        // ---------- movement 2: the night before (chapter 2) ----------
        Clue7("neighbor_heard", Kind7.WITNESS, 1, 2, 1,
            "The patrol heard the shape of it",
            "The night patrol's sworn account of the visit, by " +
                "volume alone: a demand, at full voice, for the " +
                "first minutes. A long quiet of answers too low " +
                "to carry through a trailer wall. And then, for a " +
                "long while after the voice left, the sound of " +
                "paper — something being wrapped, carefully, by " +
                "somebody in no hurry at all. The patrol noted the " +
                "wrapping because it went on so long. Wrapping is " +
                "not a sound people make over things they are " +
                "hiding. It is a sound people make over things " +
                "they are getting ready to give.",
            "The patrol heard the shape of it",
            npc = "Barbrady",
            spoken = "Sworn statement, patrol division, which " +
                "is me. I was on that street at eight, on " +
                "account of the street being on my route. I " +
                "heard a voice do demanding for a bit — the " +
                "Broflovski voice, it carries, it's built to " +
                "carry — and then a whole lot of quiet, and the " +
                "voice left at eight-forty. And THEN, citizen, " +
                "then I heard paper. For a LONG time. Wrapping " +
                "sounds. I noted it because it was so careful. " +
                "You don't wrap a thing that careful unless " +
                "it's going somewhere in the morning. That's " +
                "not in the report. The report just says paper, " +
                "eight-forty to nine-fifteen. The careful is " +
                "my professional opinion."),
        Clue7("unsent_reply", Kind7.LOOK, 1, 2, 1,
            "The reply she never sent",
            "A draft on the kitchen table, in Carol " +
                "McCormick's hand, dated the night of the visit, " +
                "never sent: 'Mrs. Broflovski — the trowel will " +
                "be at the school for Founders', same as always, " +
                "on the table where it belongs. What I will not " +
                "do is sign a paper saying my family stole what " +
                "it has kept. Bring your paper to the display " +
                "and watch it sit there like it has every year " +
                "of your life.' Unsent — because in the morning " +
                "the trowel was going to speak for itself, on " +
                "the table. The morning had other plans.",
            "The reply she never sent",
            locId = "KennysHouse",
            foundText = "🔍 On the kitchen table, under a mug: a " +
                "draft reply, in Carol McCormick's hand, dated " +
                "the night of the visit and never sent. 'The " +
                "trowel will be at the school for Founders', " +
                "same as always, on the table where it belongs. " +
                "What I will not do is sign a paper saying my " +
                "family stole what it has kept.' It ends there. " +
                "She let the trowel do the answering, in the " +
                "morning, on the table."),
        Clue7("keeper_account", Kind7.WITNESS, 1, 2, 1,
            "The keeper's account, such as she'll give",
            "Carol McCormick's own statement, given once, on " +
                "the record, and not embroidered: yes, the visit " +
                "happened. Yes, there was a paper, and no, she " +
                "did not sign it. No, she will not say what the " +
                "paper said — 'ask her, she printed it.' And one " +
                "sentence more, offered like a door being closed " +
                "gently: 'If you want to know about the trowel, " +
                "kid, ask the school for its papers. Mine are my " +
                "own, and they've never once been enough for " +
                "this town anyway.'",
            "The keeper's account, such as she'll give",
            npc = "Carol",
            spoken = "I'll say it once, because you're the one " +
                "writing things down in order. She came. There " +
                "was a paper — a statement, all typed, saying " +
                "my family took the trowel and kept it hid, " +
                "with a line for my name. I wouldn't sign it. " +
                "That's the visit. That is the WHOLE visit, " +
                "whatever the street heard. If you want to know " +
                "about the trowel, ask the school for its " +
                "papers. Mine are my own — and they've never " +
                "once been enough for this town anyway."),
        // ---------- movement 1: the ledger (chapter 3) ----------
        Clue7("ledger_pages", Kind7.LOOK, 2, 1, 2,
            "The volume, read whole",
            "The fire-winter ledger, the whole of it: the front " +
                "board lists the school as it was — the officers, " +
                "the staff, the principal's initials, E.W. The " +
                "removal line is there, famous now, in the " +
                "caretaker's hand: the trowel, the fire winter, " +
                "removed. And two pages on, in the same hand, in " +
                "the winter's ordinary accounts, a second line " +
                "nobody has quoted: the trowel, cleaned, returned " +
                "into the keeper's hands, the case being unfit — " +
                "with the principal's initials beside it, in a " +
                "different pencil. The volume never hid anything. " +
                "It just had more pages than the story needed.",
            "The volume, read whole",
            locId = "CityHall",
            foundText = "🔍 The fire-winter ledger, whole: the " +
                "front board names the school's officers, " +
                "initials only — the principal, E.W. The famous " +
                "line is here, in the caretaker's hand: the " +
                "trowel, removed, the fire winter. And two " +
                "pages on — two pages nobody read aloud — the " +
                "winter's accounts carry a second line in the " +
                "same hand: the trowel, cleaned, returned into " +
                "the keeper's hands, the case unfit. Beside it, " +
                "in a different pencil: the same initials. E.W."),
        Clue7("request_slip", Kind7.LOOK, 2, 1, 2,
            "The archive request slip",
            "The archive's request slip for the ledger, signed " +
                "a month before the assembly, in Gerald " +
                "Broflovski's hand: the volume requested BY ITS " +
                "INDEX CARD, as the index lists it — both " +
                "gatherings, the accounts as well as the " +
                "removals. Nobody selected the famous page. " +
                "Nobody fed anybody a line. The whole book was " +
                "pulled, in the open, by card — and the whole " +
                "book sat in a reading room for a fortnight, " +
                "waiting to be read past its first famous page. " +
                "It is still waiting, some of it. The slip also " +
                "shows the volume's other life: pulled most " +
                "springs, for years, by the Founders' committee, " +
                "for the display program. A working book, not a " +
                "buried one.",
            "The archive request slip",
            locId = "CityHall",
            foundText = "🔍 The request slip, archive file: the " +
                "ledger, pulled a month before the assembly, " +
                "signed by Gerald Broflovski, requested BY ITS " +
                "INDEX CARD — listed in full, both gatherings, " +
                "accounts and removals together. And down the " +
                "slip's back, the volume's ordinary life: out " +
                "most springs for years, Founders' committee, " +
                "for the program. Nobody buried this book. " +
                "Nobody planted it, either. It was pulled " +
                "whole. It was read partially. There's a " +
                "difference, and the difference is the case."),
        Clue7("program_note", Kind7.WITNESS, 2, 1, 2,
            "The programs said it every year",
            "The principal's own testimony, from her own " +
                "files: the Founders' program — printed by her " +
                "office, distributed to every folding chair, " +
                "every year the keeper can remember — lists the " +
                "display table's treasures, and the trowel's " +
                "line has read, in print, for years: 'The " +
                "Cornerstone Trowel, on loan from its keeper.' " +
                "The town has held the truth in its hands, on " +
                "glossy paper, annually, and used it to follow " +
                "the choir numbers. Nobody forged anything. " +
                "Nobody hid anything. The information was " +
                "program-shaped, and the town does not read " +
                "programs. It reads scenes.",
            "The programs said it every year",
            npc = "Victoria",
            spoken = "I keep the programs; of course I keep the " +
                "programs. Read the display list with me — go " +
                "on, I'll wait. 'The Cornerstone Trowel, on " +
                "loan from its keeper.' That line is MINE. I " +
                "have set it in type, annually, for years. The " +
                "keeper's name is in my files as the lender of " +
                "record, because she IS the lender of record, " +
                "because she lends it, every year, and signs " +
                "your little book by the door. This office " +
                "published the truth on glossy paper and handed " +
                "it to three hundred people a year, and the " +
                "town used it to follow the choir. I cannot " +
                "make people read, Detective. I can only print. " +
                "I have done the printing."),
        // ---------- movement 0: the fire winter (finale chapter) ----------
        Clue7("elder_telling", Kind7.WITNESS, 3, 0, 3,
            "The oldest memory of the fire",
            "Marvin Marsh, nine years old in the fire winter, " +
                "remembers the morning after from the gym doorway " +
                "where the children were kept back: the smoke, " +
                "the cracked case with its glass gone milky — " +
                "and old Mr. McCormick carrying the trowel out " +
                "wrapped in a gym towel, 'like a baby with a " +
                "fever,' while the principal walked beside him " +
                "with a hand on his shoulder. The principal's " +
                "name, remembered plainly after sixty years: " +
                "Enoch Webb — the E.W. of the day-book and the " +
                "ledger both. The words he said, the witness " +
                "admits he only understood at forty: 'Keep it " +
                "cleaner than we kept this school, and bring " +
                "it back when the case is mended.' The case was " +
                "never mended. The keeping was.",
            "The oldest memory of the fire",
            npc = "Marvin",
            spoken = "I was nine, and they kept us at the gym " +
                "door because of the smoke, and I saw old Mr. " +
                "McCormick come out carrying that trowel wrapped " +
                "in a gym towel like a baby with a fever. And " +
                "Mr. Webb — Enoch Webb, first principal this " +
                "school ever had, the E.W. in every book in " +
                "that archive — he walked right beside him with " +
                "a hand on his shoulder, and he said, 'Keep it " +
                "cleaner than we kept this school, and bring it " +
                "back when the case is mended.' I didn't know " +
                "what it meant till I was forty. The case never " +
                "got mended, kid. Sixty years, they never fixed " +
                "the glass. But the keeping got done. Every " +
                "year of it. Say that part into whatever record " +
                "you're keeping."),
        Clue7("daybook_line", Kind7.LOOK, 1, 0, 2,
            "The caretaker's day-book, initials only",
            "The caretaker's day-book, the fire-winter page, in " +
                "full daylight at last: 'Took the trowel home to " +
                "clean and keep, by E.W.'s word.' With the " +
                "ledger open beside it, the initials resolve: " +
                "E.W. is the school's first principal, listed on " +
                "the volume's front board among its officers. " +
                "The line is not a thief's note; thieves do not " +
                "log their takings in the school's own book, on " +
                "the school's own shelf, with the principal's " +
                "word cited like a reference. It is an errand " +
                "done on a word, written down by the man who " +
                "carried it out — in initials, because in that " +
                "winter everyone alive knew whose word E.W. was.",
            "A line in the caretaker's day-book, in pencil: " +
                "'Took the trowel home to clean and keep, by " +
                "E.W.'s word.' The initials match nobody you " +
                "know — not yet. A careful hand, a school shelf, " +
                "an errand described like an errand. It doesn't " +
                "make sense yet. It knows it doesn't. It's " +
                "prepared to wait for the rest of its winter.",
            locId = "Elementary",
            foundText = "🔍 The caretaker's day-book, boiler-room " +
                "shelf, winters going back decades — boiler " +
                "readings, mostly, in a careful hand. One line, " +
                "the fire winter, in pencil: 'Took the trowel " +
                "home to clean and keep, by E.W.'s word.' E.W. " +
                "The initials match nobody you know. Yet. You " +
                "copy the line down exactly. Some evidence is " +
                "a key cut for a lock you haven't found, and " +
                "this is that, and it admits it."),
        Clue7("carbon_receipt", Kind7.PARTICIPATION, 2, 0, 3,
            "A carbon receipt, the payer line smeared",
            "The archive box's carbon, legible at last in the " +
                "fire winter's light: a receipt from the spring " +
                "after the fire — 're-silvering, one presentation " +
                "trowel; smoke damage made good; paid in full' — " +
                "and the payer line, no longer just a smear, " +
                "because now you know whose accounts the spring " +
                "after the fire ran through: …McCormick. The " +
                "caretaker paid, from his own pocket, to make " +
                "the trowel whole again, the year after he " +
                "carried it out. It is the keeping, itemized, in " +
                "carbon: nobody pays to restore what they stole " +
                "in the same season they stole it, and bills the " +
                "school nothing.",
            "A carbon of a receipt from an archive box, the " +
                "spring after the fire winter: 're-silvering, " +
                "one presentation trowel — paid in full.' The " +
                "payer's name is smeared to its last syllable: " +
                "…cormick. A payment, a trowel, a spring — and " +
                "a name you can't quite read, attached to money " +
                "somebody spent ON the thing the town says was " +
                "taken. It doesn't make sense yet. It's an " +
                "invoice in search of its winter.",
            chapterDefId = "arc7_ch3_the_ledger")
    )

    val CLUE7_BY_ID: Map<String, Clue7> = CLUES7.associateBy { it.id }
    val CLUE7_IDS: Set<String> = CLUES7.map { it.id }.toSet()

    fun clueCount7(state: GameState): Int = state.mystery7Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable7(state: GameState, clue: Clue7): Boolean =
        !state.arc7Retired && state.arc7Chapter > clue.minChapter

    /** A piece is LIVE — testimony-grade, spendable —
     *  when it is in hand AND the chapter that unlocks
     *  its context has begun. Ordinary pieces are live
     *  the moment they exist. The two locked pieces sit
     *  in hand, honestly inert, until their winter
     *  arrives: they count toward nothing, confirm
     *  nothing, break nothing. Derived, never stored. */
    fun clueLive7(state: GameState, clue: Clue7): Boolean =
        clue.id in state.mystery7Clues &&
            state.arc7Chapter > clue.unlockChapter

    /** The ids of everything currently spendable. */
    fun liveClueIds7(state: GameState): Set<String> =
        CLUES7.filter { clueLive7(state, it) }.map { it.id }.toSet()

    /** Live pieces tagged to one movement — the only
     *  currency a reconstruction can be bought with. */
    fun liveEvidence7(state: GameState, m: Movement7): Int =
        CLUES7.count { it.movement == m.index && clueLive7(state, it) }

    /** Grant a piece of evidence exactly once. The ticker
     *  + the town log hear about it; the board keeps it
     *  forever. A locked piece announces its own
     *  unreadability — honestly, at the moment of finding,
     *  so no player ever mistakes holding for knowing. */
    fun grantClue7(state: GameState, clueId: String): Boolean {
        val clue = CLUE7_BY_ID[clueId] ?: return false
        if (clueId in state.mystery7Clues) return false
        state.mystery7Clues.add(clueId)
        val live = clueLive7(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery7Clues.size}/${CLUES7.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery7Clues.size}/${CLUES7.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "in the past, and the past is still coming.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // ---------------- discovery: witnesses, looks, hands ----------------

    /** Talk-to discovery: if [npcName] is a live witness
     *  right now, their line — and the evidence it grants. */
    fun witnessLine7(state: GameState, npcName: String): Line? {
        val clue = CLUES7.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable7(state, clue)) return null
        if (!grantClue7(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId]
     *  reports the first time it finds each piece of
     *  evidence there — one at a time, in the order the
     *  case has opened them. A locked piece found early
     *  reports its inert face, and only its inert face:
     *  what it IS, never what it means. */
    fun lookClueText7(state: GameState, locId: String): String? {
        val clue = CLUES7.firstOrNull {
            it.kind == Kind7.LOOK && it.locId == locId &&
                it.id !in state.mystery7Clues &&
                clueAvailable7(state, it)
        } ?: return null
        if (!grantClue7(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation7(state: GameState, defId: String) {
        val clue = CLUES7.firstOrNull {
            it.kind == Kind7.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable7(state, clue)) return
        if (grantClue7(state, clue.id)) {
            state.events.add("🧩 Up close, something clicks: ${clue.title}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- THE TIMELINE (derived, never stored) ----------------
    // The only stored facts are the commits themselves and
    // the evidence in hand. Every verdict below is a pure
    // function of those recorded inputs, recomputed on
    // every read — the opinion case's meter discipline,
    // applied to a story: the past can't be argued with,
    // only re-read.

    /** A movement's verdict, derived: OPEN (unwritten),
     *  COMMITTED (written, untested), CONFIRMED (written
     *  checked, and the piece that supports it is live in
     *  hand), or BROKEN (written easy, and the piece that
     *  refutes it is live in hand). A broken line can be
     *  struck and written again; a standing line cannot —
     *  that is the backwards trap, and it has teeth on
     *  purpose: the confrontation is committed first, on
     *  the least evidence, and its refutation is filed
     *  two movements deeper in the past. */
    fun verdict7(state: GameState, m: Movement7): String {
        val pick = state.arc7Commits[m.id] ?: return "OPEN"
        val live = liveClueIds7(state)
        return when {
            pick == "easy" && m.easyBrokenBy in live -> "BROKEN"
            pick == "checked" && m.checkedConfirmBy in live -> "CONFIRMED"
            else -> "COMMITTED"
        }
    }

    /** The timeline is complete when every movement has a
     *  committed reading — standing, confirmed, or even
     *  broken. (The finale's own gate reads deeper than
     *  completeness; see restageDue and chooseFinale7.) */
    fun timelineComplete7(state: GameState): Boolean =
        MOVEMENTS7.all { it.id in state.arc7Commits }

    fun confirmedCount7(state: GameState): Int =
        MOVEMENTS7.count { verdict7(state, it) == "CONFIRMED" }

    /** The movement the commit dialog should offer next:
     *  the deepest unwritten movement (in fill order) whose
     *  later neighbours are all committed and whose live
     *  evidence meets its price — or, when nothing new can
     *  be written, the deepest BROKEN movement, offered for
     *  revision. Writing backwards means the beginning can
     *  only be committed last; revision never blocks the
     *  writing, it waits its turn behind it. */
    fun pendingCommit7(state: GameState): Movement7? {
        if (state.arc7Retired) return null
        for (m in FILL_ORDER7) {
            if (m.id in state.arc7Commits) continue
            val laterDone = MOVEMENTS7.all {
                it.index <= m.index || it.id in state.arc7Commits
            }
            if (laterDone && liveEvidence7(state, m) >= m.evidenceNeeded)
                return m
        }
        for (m in FILL_ORDER7) {
            if (verdict7(state, m) == "BROKEN") return m
        }
        return null
    }

    /** Commit (or re-commit, after a break) a movement's
     *  reconstruction. The pick is recorded; the verdict is
     *  NOT — it is derived on every read, so a line written
     *  in chapter 1 can break in chapter 3 without anything
     *  being edited: the past simply arrived. */
    fun chooseCommit7(state: GameState, movementId: String,
                      pick: String): Boolean {
        if (pick !in PICK_KEYS) return false
        val pending = pendingCommit7(state) ?: return false
        if (pending.id != movementId) return false
        val revising = movementId in state.arc7Commits
        state.arc7Commits[movementId] = pick
        val v = verdict7(state, pending)
        state.events.add(when {
            revising ->
                "🖊️ STRUCK AND REWRITTEN: '${pending.title}' — the " +
                    "timeline takes the new reading. ($v)"
            v == "CONFIRMED" ->
                "🖊️ COMMITTED AND CONFIRMED: '${pending.title}' — " +
                    "the evidence already in hand backs this reading."
            v == "BROKEN" ->
                "🖊️ COMMITTED — AND BROKEN ON ARRIVAL: " +
                    "'${pending.title}'. The evidence in hand " +
                    "already refutes this reading. The line " +
                    "stands struck. It can be written again."
            else ->
                "🖊️ COMMITTED: '${pending.title}' — the reading " +
                    "stands, untested. The past may yet have " +
                    "something to say about it."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🖊️ Timeline: '${pending.title}' committed ($v)")
        return true
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after the
     *  opinion case retires books chapter 1 ten days out
     *  from that retirement. While any earlier case is open
     *  this never fires — and never even books the date:
     *  the mystery slot has one case at a time. Chapters
     *  defer, day by day, past holiday episodes, like every
     *  case before them. */
    fun dueChapter7(state: GameState): ShenaniganDef? {
        if (state.arc7Retired) return null
        if (!state.arc6Retired) return null
        if (state.arc7Chapter >= CHAPTERS7.size) return null
        if (state.arc7DueAbsDay < 0) {
            state.arc7DueAbsDay = if (state.arc7Chapter == 0) {
                val anchor = if (state.arc6RetiredDay >= 0)
                    state.arc6RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc7DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS7[state.arc7Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted7(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf7(def.id)
        if (idx < 0) return
        state.arc7Started = true
        state.arc7Chapter = idx + 1
        state.arc7DueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the confrontation, again ----------------

    /** The homecoming can be played: the last chapter is
     *  airing, its climax has begun, no call is locked yet,
     *  and the timeline is COMPLETE — all four movements
     *  committed. The ceremony waits on a finished
     *  timeline the way the meeting waited on evidence:
     *  you cannot replay the scene until you have rebuilt
     *  the story that leads to it, in order, backwards. */
    fun restageDue(state: GameState): Boolean {
        if (state.arc7Retired || state.arc7Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE7_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return timelineComplete7(state)
    }

    /** Whether the SAY WHAT REALLY STARTED IT call is
     *  available: the beginning movement — committed last,
     *  on the deepest evidence — stands CONFIRMED. The
     *  gate reads the FINAL state of the timeline, never
     *  the first impressions: a beginning written wrong
     *  (BROKEN on its own evidence) buys nothing, and a
     *  beginning never confirmed buys nothing either. */
    fun originEligible7(state: GameState): Boolean =
        verdict7(state, MOVEMENTS7[0]) == "CONFIRMED"

    /** Whether CHANGE ONE THING is available: at least one
     *  movement stands CONFIRMED — you may only change
     *  what you actually understand. */
    fun changeEligible7(state: GameState): Boolean =
        confirmedCount7(state) >= 1

    /** Lock the homecoming's call. The per-call gates are
     *  enforced here, in the module, not just in the
     *  dialog: a call the timeline can't carry is refused
     *  outright. */
    fun chooseFinale7(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!restageDue(state)) return false
        if (choice == "origin" && !originEligible7(state)) return false
        if (choice == "change" && !changeEligible7(state)) return false
        state.arc7Choice = choice
        state.events.add(when (choice) {
            "origin" ->
                "🗣️ You stand up in the same gym, in front of " +
                    "the same chairs, and you start the story at " +
                    "its beginning — the fire winter, out loud, " +
                    "in order. The room hears the ending last " +
                    "this time."
            "stand" ->
                "🏛️ You let the original ending stand. The " +
                    "ceremony proceeds on the assembly's version, " +
                    "and the trowel gets its plaque in the story " +
                    "the town applauded."
            "change" ->
                "🔧 You change one thing: before anything is " +
                    "decided, the second page is read into the " +
                    "record — aloud, in the room, over the " +
                    "program's rustle — and the ceremony has to " +
                    "proceed in a town that has heard it."
            else ->
                "🚶 You walk away. The ceremony has the room, " +
                    "the chairs, the felt, the program with the " +
                    "blank space. It doesn't have you."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class RestageOutcome(
        val branch: String,          // "origin" | "again" | "mended" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, hand the wrap its
     *  words. Calls are re-gated here against the final
     *  timeline, exactly as locked — verdicts are monotone
     *  (evidence is never un-gathered, chapters never
     *  un-air), so a call that was lawful when locked is
     *  lawful now; anything else degrades to the cold
     *  landing, the discipline of every case before this
     *  one. A ceremony nobody played is cold by definition:
     *  the argument reignited without the one person who
     *  had read it whole. */
    fun resolveRestage(state: GameState): RestageOutcome {
        val chosen = state.arc7Choice
        val complete = timelineComplete7(state)
        val branch = when {
            chosen == "origin" && complete && originEligible7(state) -> "origin"
            chosen == "change" && complete && changeEligible7(state) -> "mended"
            chosen == "stand" && complete -> "again"
            else -> DEFAULT_BRANCH
        }
        state.arc7Branch = branch
        state.arc7Retired = true
        state.arc7RetiredDay = state.absDay
        val held = clueCount7(state)
        return when (branch) {
            "origin" -> RestageOutcome(branch,
                "The record now begins where the story begins. " +
                    "The homecoming heard the fire winter first — " +
                    "the smoke, the cracked case, the principal's " +
                    "word, the sixty years of keeping — and the " +
                    "taking last, in its place, at the end of the " +
                    "timeline where endings belong. Sheila " +
                    "Broflovski stood in a gym and listened to her " +
                    "own scene played back with its beginning " +
                    "attached. Carol McCormick stood in the back " +
                    "and listened to the town hear it. Neither of " +
                    "them said a word. The program's blank space " +
                    "got filled in, in pencil, by the principal, " +
                    "in the minutes, forever.",
                listOf(
                    "🕰️ TOLD FROM THE BEGINNING: It is the rarest " +
                        "thing a story like this ever gets: its " +
                        "first page, read aloud, in the room where " +
                        "its last page happened. The day-book line. " +
                        "The ledger's second page. The carbon with " +
                        "the payer's name finished at last. The " +
                        "oldest memory in town, saying the " +
                        "principal's name into a microphone. The " +
                        "keeping is on the record now. So is the " +
                        "taking. The record, for once, is in order.",
                    "🕰️ WHAT THE ROOM DOES WITH IT: Nobody " +
                        "apologizes; this is still South Park. " +
                        "Sheila sits down very slowly, in the third " +
                        "row, and does her arithmetic in public, " +
                        "which is the bravest thing she has ever " +
                        "done in a gym. Carol folds her program in " +
                        "half — once, not twice — and puts it in " +
                        "her pocket, to keep. The trowel goes back " +
                        "to the school at last, for real this time: " +
                        "the case by the gym door is getting its " +
                        "glass mended, sixty years on, and the " +
                        "plaque is going to tell the whole story, " +
                        "both names on it, the keeper's first."))
            "again" -> RestageOutcome(branch,
                "The original ending stands. The ceremony " +
                    "ratifies the assembly: the trowel is the " +
                    "school's again, 'recovered at the Founders' " +
                    "Assembly,' and the plaque says so in engraved " +
                    "letters that will outlast everyone in the " +
                    "room. Carol McCormick watches from the back " +
                    "while the keeping is read out of a record the " +
                    "town spent sixty years not reading. She " +
                    "leaves before the applause. " +
                    "There is applause. There was always going " +
                    "to be applause.",
                listOf(
                    "🕰️ THE FIRST ENDING, KEPT: The timeline was " +
                        "full — every movement written, the " +
                        "beginning included — and the room chose " +
                        "the story it already had. That is also a " +
                        "way a story can end: not unknown, just " +
                        "unspoken. The evidence stays proven. " +
                        "Proven and unspoken is a shelf the town " +
                        "knows well; its archive is full of it.",
                    "🕰️ THE PLAQUE: 'Recovered for the school at " +
                        "the Founders' Assembly, by a parent who " +
                        "cared.' Every word of it is true, the way " +
                        "the removal line was true — a line with " +
                        "its paragraph removed. The trowel will " +
                        "sit behind glass downtown, correctly " +
                        "labeled, forever, and the keeper will " +
                        "walk past the building on her way to the " +
                        "store, and not look up, and look up."))
            "mended" -> RestageOutcome(branch,
                "One thing changed. Before the ceremony could " +
                    "decide anything, the ledger's second page " +
                    "was read into the record — aloud, in the " +
                    "room, by the principal, whose programs had " +
                    "been saying it for years to people holding " +
                    "them upside down. The homecoming proceeds " +
                    "in a town that has heard the keeping: the " +
                    "trowel goes back to the McCormicks' keeping, " +
                    "on the record this time, in writing, with " +
                    "the school's thanks in the minutes — and a " +
                    "standing invitation to the display table " +
                    "every Founders', signed in by the keeper, " +
                    "as always.",
                listOf(
                    "🕰️ THE AMENDED RECORD: Nobody's scene got " +
                        "un-happened. Sheila's research is in the " +
                        "file, with its missing pages restored. " +
                        "Carol's keeping is in the file too, in " +
                        "the school's own hand — which, it turns " +
                        "out, it always was, in a program nobody " +
                        "read. The change is one sentence long, " +
                        "and it is load-bearing: the trowel's " +
                        "story now contains its middle.",
                    "🕰️ WHAT IT COSTS: The town does not get " +
                        "its clean rescue, and the keeper does " +
                        "not get her sixty years back, and nobody " +
                        "gets an apology in either direction. " +
                        "What everybody gets is a record that " +
                        "reads the same forwards as backwards — " +
                        "which, in this town, is what an apology " +
                        "looks like when it's built to last."))
            else -> RestageOutcome(branch,
                if (chosen == "walk")
                    "You walked away, and the ceremony went the " +
                        "way ceremonies go without the person who " +
                        "read the file: the argument reignited on " +
                        "schedule, in the doorway, at full volume — " +
                        "the ledger quoted, the trailer named, " +
                        "the keeper's silence entered as evidence " +
                        "by both sides — and the trowel came home " +
                        "to the school under the assembly's story, " +
                        "with its beginning known to exactly one " +
                        "person in town, who was not in the room."
                else
                    "The homecoming happened without you. The " +
                        "argument reignited on schedule — the " +
                        "ledger quoted at the trailer, the silence " +
                        "read as a confession, the applause in the " +
                        "same places as the first time, like a " +
                        "rerun — and the trowel came home under " +
                        "the assembly's story, its beginning known " +
                        "to $held of ${CLUES7.size} pieces of one " +
                        "person's evidence, none of it in the room.",
                listOf(
                    "🕰️ THE RERUN: A scene replayed without its " +
                        "past is just the scene again. The town " +
                        "is very good at its scenes; it has had so " +
                        "much practice. The timeline stays full in " +
                        "your Journal — beginning, ledger, night, " +
                        "taking, in order — and empty in the room " +
                        "where it was needed, which is a different " +
                        "kind of archive, and the town keeps that " +
                        "one too.",
                    "🕰️ THE EMPTY CHAIR, AGAIN: Somewhere in the " +
                        "back, a nine-year-old on stage crew kept " +
                        "looking at the door. He had heard his " +
                        "mother's speech from the wings, once, and " +
                        "he knew exactly which part of it was said " +
                        "to the room and which part was said to a " +
                        "person. The room never learned the " +
                        "difference. The wings know. The wings " +
                        "always know."))
        }
    }

    /** The landing's ripples + its permanent marks — called
     *  by the wrap BEFORE the episode's credits are built, so
     *  the recap's faction-shift diff includes them. */
    fun applyRestageEffects(state: GameState, branch: String) {
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
            "origin" -> {
                befriend("Carol", 4)
                befriend("Stuart", 2)
                befriend("Sheila", 1)
                ripple("school_staff", 1,
                    "The Staff watched a story get told from its beginning in a school gym, in order, with the documents present. The minutes have never been prouder, or quieter.")
                ripple("parents", -1,
                    "The Parents came for a sequel about a villain and got a sixty-year story about a favor instead. Half the Parents are telling it already. They're telling it backwards, but they're telling it.")
                state.arc7CasePending = true
                state.arc7CaseDay = state.absDay + CASE_RETURN_DELAY_DAYS
                state.lifeText["mark:Elementary"] =
                    "The trowel's case by the gym door is finally " +
                        "being mended — sixty years after the fire — " +
                        "because the story got told from its " +
                        "beginning in this gym, and the school " +
                        "decided the glass should be as whole as " +
                        "the record."
                townLogLine(state, "🕰️ TOLD FROM THE BEGINNING: the " +
                    "homecoming heard the fire winter first. The " +
                    "keeping is on the record. The case gets its " +
                    "glass back.")
            }
            "again" -> {
                befriend("Carol", -4)
                befriend("Stuart", -2)
                befriend("Sheila", 3)
                ripple("parents", 1,
                    "The Parents got their rescue ratified with a plaque and a ceremony. The Parents are framing the program. Upside down, but framing it.")
                ripple("school_staff", -1,
                    "The Staff keeps the minutes of the homecoming in a hand slightly tighter than usual. The record the Staff printed for sixty years is not the record the plaque tells, and the Staff knows which drawer the programs are in.")
                state.lifeText["mark:CityHall"] =
                    "The Cornerstone Trowel sits in a case at City " +
                        "Hall under a plaque: 'Recovered for the " +
                        "school at the Founders' Assembly.' Every " +
                        "word true. The paragraph is not included. " +
                        "Paragraphs rarely are, on plaques."
                townLogLine(state, "🕰️ THE FIRST ENDING STANDS: the " +
                    "taking is the story, engraved. The keeping " +
                    "stays in the archive, where it was always " +
                    "kept, by the family that kept everything.")
            }
            "mended" -> {
                befriend("Carol", 2)
                befriend("Sheila", 1)
                ripple("school_staff", 1,
                    "The Staff amended the record in a full room, out loud, and the roof did not fall in. The minutes call it 'the correction.' The Staff has started reading documents all the way through, as a matter of policy.")
                state.lifeText["mark:Elementary"] =
                    "A line in the school record, amended at the " +
                        "homecoming, names the trowel's keeper and " +
                        "sixty years of keeping — read aloud in " +
                        "this gym, over the program's rustle, by " +
                        "the principal, into the minutes, forever."
                townLogLine(state, "🕰️ ONE THING CHANGED: the second " +
                    "page is in the record now. The trowel goes " +
                    "back to its keeper — on paper, in public, " +
                    "with thanks.")
            }
            else -> {
                befriend("Carol", -2)
                ripple("parents", -1,
                    "The homecoming reran the assembly and the Parents applauded in the same places. A few of them noticed, afterward, that they had applauded in the same places, and couldn't say why that bothered them.")
                state.lifeText["mark:Elementary"] =
                    "The trowel came home to the school under " +
                        "the assembly's story, argued over in this " +
                        "gym twice in a month, in front of the same " +
                        "chairs. The case by the door is still " +
                        "cracked. The card inside still says what " +
                        "it has said for sixty years. It is waiting " +
                        "for a reader, like everything in this case."
                townLogLine(state, "🕰️ THE RERUN: the homecoming " +
                    "happened without the timeline. The story " +
                    "kept its first ending. The beginning stays " +
                    "where it was: in the archive, in order, unread.")
            }
        }
    }

    // ---------------- the case comes home (the ORIGIN landing's week after) ----------------

    /** The delayed landing of the ORIGIN verdict: called
     *  daily by the engine; fires exactly once, a week
     *  after the story was told from its beginning — the
     *  school finishes mending the trowel's case, sixty
     *  years after the fire cracked it, and the trowel
     *  goes home under glass with the whole story on the
     *  plaque, the keeper's name first. */
    fun noteCaseRebuilt(state: GameState): Boolean {
        if (!state.arc7CasePending) return false
        if (state.absDay < state.arc7CaseDay) return false
        state.arc7CasePending = false
        state.arc7CaseBuilt = true
        state.events.add("🏫 THE TROWEL IS HOME: the case by the " +
            "gym door has new glass this morning — mended at " +
            "last, sixty years on — and the trowel sits in it " +
            "on green felt, with a plaque that tells the whole " +
            "story: the fire, the asking, the keeping, the " +
            "loan, the taking, the telling. The keeper's name " +
            "is first. Carol McCormick came at seven, before " +
            "the buses, and read it twice, and straightened " +
            "the felt through the glass guy's ladder gap like " +
            "she still had the job. She does. She always did.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:Elementary"] =
            "The trowel is home: new glass, green felt, and a " +
                "plaque that tells the story whole — fire, " +
                "keeping, loan, taking, telling — beginning " +
                "first, the keeper's name first. The school " +
                "reads it to the new kids every September. It " +
                "takes four minutes. It is the longest four " +
                "minutes in the curriculum, and the only ones " +
                "about how a story works."
        townLogLine(state, "🏫 THE CASE IS MENDED, sixty years " +
            "after the fire: the trowel is home, the plaque " +
            "tells it whole, and the story starts where it starts.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "origin" to mapOf(
            "Victoria" to listOf(
                "The record begins at the beginning now. I have " +
                    "filed the homecoming under CORRECTED, which " +
                    "in my filing is the drawer that matters — and " +
                    "I will confess to you, since you keep " +
                    "records too: my office printed 'on loan from " +
                    "its keeper' on the program for years, and I " +
                    "never once read it as evidence. A thing can " +
                    "be in the minutes and still not be in the " +
                    "room. You put it in the room. That's the " +
                    "whole job, apparently. It only took sixty " +
                    "years and a child working backwards.",
                "The plaque tells it whole now. Both names. The " +
                    "fire first. I proofread it four times; it is " +
                    "correct, which is the highest praise this " +
                    "office issues, and I am aware — I am AWARE — " +
                    "that correct and read are different verbs. " +
                    "We're working on the second one. Assemblies " +
                    "help. Corrected assemblies especially.")),
        "again" to mapOf(
            "Carol" to listOf(
                "It's downtown now, behind glass, with a plaque " +
                    "that says she rescued it. From us. I walk " +
                    "past that building every week of my life, " +
                    "kid. Sixty years my family kept that trowel " +
                    "clean enough to lend, and the town's thank-" +
                    "you is a plaque that calls us the hiding " +
                    "place. You can keep a thing behind glass and " +
                    "still not have it. Write that in your " +
                    "timeline. Right under the part where I " +
                    "folded the program and left quiet, both " +
                    "times, because this family doesn't make " +
                    "scenes. We leave that to the rescuers.",
                "Kenny asked me why the trowel isn't at the " +
                    "school anymore, if the school is where it " +
                    "lives. I told him the truth — it's in a " +
                    "nicer case now. Children and plaques, kid. " +
                    "They both only say what's on them. The " +
                    "difference is children grow out of it.")),
        "mended" to mapOf(
            "Sheila" to listOf(
                "I read the second page. I want that understood " +
                    "— eventually, in a gym, in front of everyone, " +
                    "the second page got read, and one of the " +
                    "people who made sure of it was standing in " +
                    "the room because I'd put her there. Kyle " +
                    "asked me if I was sorry. I told him: I am " +
                    "sorry I read half a book at full volume. The " +
                    "research was impeccable. The research was " +
                    "HALF. There's a difference, and the " +
                    "difference has a name in the minutes now, " +
                    "and the name is hers, which is — that is " +
                    "the part I'm still — ask me next year.",
                "I've started turning to the second page of " +
                    "things. Contracts. Reports. My own speeches. " +
                    "You'd be amazed what's on second pages; " +
                    "it's where the world keeps the part it " +
                    "didn't say at the assembly. Gerald says " +
                    "it's made me slower. Gerald is right. " +
                    "Slower is the whole — slower is what it " +
                    "cost, and it's what it bought.")),
        "cold" to mapOf(
            "Kyle" to listOf(
                "You weren't at the homecoming either. I kept " +
                    "looking at the door, like Bradley with his " +
                    "bag, last case — except I wasn't holding " +
                    "evidence, I was holding a LIGHTING CUE. Mom " +
                    "did the speech again. The room clapped in " +
                    "the same places. I know because I was in " +
                    "the wings both times, and the wings hear " +
                    "everything — including the part where nobody, " +
                    "either time, ever said one word TO Carol. " +
                    "Just about her. There's a difference. You're " +
                    "the only other person who knows there's a " +
                    "difference, and you weren't THERE.",
                "Stage crew hears the truth at a different " +
                    "volume. That's not a saying, it's just — " +
                    "acoustics. I wrote down her exact words the " +
                    "first time and gave them to you, and you " +
                    "wrote down the whole story backwards, and " +
                    "then the story got told in a room you " +
                    "weren't in, the way it was always going to " +
                    "be told. Forget it. Next assembly I'm " +
                    "running the lights from the FRONT.")))

    /** The standing lines after the homecoming: gratitude
     *  is not among them — every landing leaves somebody
     *  saying the uncomfortable thing, forever, to your
     *  face. The game never admits a call was wrong; the
     *  wronged just never stop knowing, and the spared
     *  never stop knowing either. Null for everyone else. */
    fun aftermathLine7(state: GameState, npcName: String): Line? {
        if (!state.arc7Retired) return null
        val bank = AFTERMATH_LINES[state.arc7Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The seventh case shows once it exists: chapter 1
     *  has aired, a piece of evidence is in hand, a
     *  movement is committed, or the case is closed. */
    fun boardVisible7(state: GameState): Boolean =
        state.arc7Started || state.mystery7Clues.isNotEmpty() ||
            state.arc7Commits.isNotEmpty() || state.arc7Retired

    fun statusStamp7(state: GameState): String = when {
        state.arc7Retired && state.arc7Branch == "origin" ->
            "🕰️ TOLD FROM THE BEGINNING — the story now starts where it started"
        state.arc7Retired && state.arc7Branch == "again" ->
            "🕰️ THE FIRST ENDING STANDS — the taking is the story, on a plaque"
        state.arc7Retired && state.arc7Branch == "mended" ->
            "🕰️ ONE THING CHANGED — the record tells the keeping too"
        state.arc7Retired -> "🧊 COLD — the ceremony happened without the timeline"
        state.shenaniganId == FINALE7_ID -> "🏫 THE HOMECOMING CEREMONY"
        state.arc7Started ->
            "🕰️ OPEN — chapter ${state.arc7Chapter} of ${CHAPTERS7.size} aired, told backwards"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the case
     *  is — told, as the town tells everything, out of order. */
    fun currentTheory7(state: GameState): String = when {
        state.arc7Retired && state.arc7Branch == "origin" ->
            "CLOSED, from the top: the town now tells the " +
                "trowel's story fire-first — the smoke, the " +
                "carrying, the keeping, the loan, and only then " +
                "the assembly, which it tells last and quietest, " +
                "the way you tell the part you're in. It will " +
                "tell the next story wrong from the ending too. " +
                "But it will tell this one right, because this " +
                "one is on a plaque, and plaques are forever."
        state.arc7Retired && state.arc7Branch == "again" ->
            "CLOSED. The rescue stands, engraved: a parent who " +
                "cared, a trailer that hid, a school made whole. " +
                "The town tells it at the speed of applause. In " +
                "one trailer across town it is not told at all, " +
                "and in one archive it sits in a bound volume, " +
                "in order, in the dark, being true at nobody."
        state.arc7Retired && state.arc7Branch == "mended" ->
            "AMENDED, which is the town's hardest tense: the " +
                "rescue still gets told — towns do not surrender " +
                "scenes — but it gets told with its middle in " +
                "now: a reading, a page turned, a keeping " +
                "acknowledged in the minutes. People correct " +
                "each other at the coffee line, mildly, the " +
                "way you correct a date. It is not nothing. In " +
                "this town it might be everything."
        state.arc7Retired ->
            "DECIDED, in a room you weren't in: the homecoming " +
                "reran the assembly beat for beat, and the " +
                "trowel's story in the town's mouth is the " +
                "first version, the applauded one. The timeline " +
                "in your Journal is the only complete copy of " +
                "the story in existence. It has exactly one " +
                "reader. It had exactly one all along."
        !state.arc7Started ->
            "Nothing yet. The trowel is on its display table, " +
                "the case by the gym door is empty the way it " +
                "has been empty for sixty years, and the town " +
                "is rehearsing a founding it thinks it knows. " +
                "Give it a morning."
        state.arc7Chapter <= 1 ->
            "Settled, at applause speed: the McCormicks had " +
                "it, Sheila took it back, and thirty witnesses " +
                "can tell you the whole plot in one breath. The " +
                "breath is the problem. A story that fits in " +
                "one breath has had its beginnings removed for " +
                "portability."
        state.arc7Chapter == 2 ->
            "The story has grown a night before: a cornering, " +
                "a refusal, a silence — told in that order, " +
                "because the town tells them backwards too, " +
                "effect first and cause when convenient. The " +
                "refusal is doing all the work. Refusals do, " +
                "when nobody asks what was refused."
        state.arc7Chapter == 3 ->
            "The story now has a document, and is resting on " +
                "it: removed, fire winter, caretaker's hand. " +
                "The town treats a line of handwriting the way " +
                "it treated the applause — as a full stop. " +
                "Somewhere behind the line there is a volume, " +
                "and behind the volume a winter, and the " +
                "winter is not in the story yet. Winters keep."
        else ->
            "Everything checkable has been checked, and the " +
                "story, read in order, is a different story — " +
                "gentler, longer, with its heroism in the " +
                "keeping instead of the taking. The town is " +
                "about to hold a ceremony in the room where it " +
                "began, holding only the ending. You are " +
                "holding the rest. Rooms notice things like that."
    }

    /** The homecoming's published terms, in the board's
     *  own words — the forecast a player can plan by. */
    fun forecastLine7(state: GameState): String {
        val done = MOVEMENTS7.count { it.id in state.arc7Commits }
        val conf = confirmedCount7(state)
        return "🏫 THE HOMECOMING, AS IT STANDS: the ceremony " +
            "waits on a finished timeline — $done/${MOVEMENTS7.size} " +
            "movements committed, $conf standing confirmed. " +
            "SAY WHAT REALLY STARTED IT needs the timeline's " +
            "beginning — the fire winter — committed and " +
            "CONFIRMED: not your first impression of it, its " +
            "final state. CHANGE ONE THING needs any one " +
            "movement confirmed. LET IT STAND and WALK AWAY " +
            "need nothing but you. A broken line can be struck " +
            "and written again; a standing line cannot. Nothing " +
            "moves the timeline but evidence, read in order."
    }

    /** The seventh case's board as plain lines (the Journal
     *  renders them below the opinion case's; the suite
     *  reads them). THE TIMELINE: the story laid out in
     *  true chronological order — beginning at the top —
     *  while the player fills it from the bottom up. Each
     *  slot keeps its evidence and its reconstruction, and
     *  its reconstruction's verdict, derived on the spot:
     *  the board never stores a verdict, it re-reads the
     *  past every time you open it. */
    fun boardLines7(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp7(state))
        out.add("🕰️ THE INCIDENT: On Founders' morning, in " +
            "front of the whole school, Sheila Broflovski took " +
            "the Cornerstone Trowel off its display table, " +
            "named the family she said had kept it hidden for " +
            "sixty years, and carried it out to applause. The " +
            "town calls it a rescue. It is certainly an ending. " +
            "This board is for what endings stand on: the story " +
            "in the order it happened — which you are filling " +
            "in backwards, from the applause.")
        out.add("🧩 THE EVIDENCE — what you hold " +
            "(${clueCount7(state)}/${CLUES7.size})" +
            if (!state.arc7Retired &&
                CLUES7.any { it.id in state.mystery7Clues && !clueLive7(state, it) })
                " — some of it is in hand but not yet readable: " +
                    "the past hasn't given it its context" else "")
        out.add("🕰️ THE TIMELINE — oldest first, the way it " +
            "actually happened. You fill it from the taking " +
            "backwards. A committed line can be CONFIRMED by " +
            "the past, or BROKEN by it — the confrontation's " +
            "own line included. Especially that one.")
        for (m in MOVEMENTS7) {
            out.add("— ${m.index + 1}. ${m.title.uppercase()} — ${m.whenText}")
            val held = CLUES7.filter { it.movement == m.index &&
                it.id in state.mystery7Clues }
            if (held.isEmpty()) {
                out.add("• (no evidence yet — this part of the " +
                    "past hasn't surfaced)")
            }
            for (clue in held) {
                if (clueLive7(state, clue)) {
                    out.add("• ${clue.title} — ${clue.detail}")
                } else {
                    out.add("• 🔒 ${clue.title} — ${clue.inertDetail}")
                }
            }
            val live = liveEvidence7(state, m)
            val pick = state.arc7Commits[m.id]
            if (pick == null) {
                val deeper = MOVEMENTS7.any {
                    it.index > m.index && it.id !in state.arc7Commits
                }
                out.add(when {
                    deeper -> "✏️ RECONSTRUCTION: unwritten — the " +
                        "timeline fills backwards; a later movement " +
                        "is still open above this one."
                    live >= m.evidenceNeeded -> "✏️ RECONSTRUCTION: " +
                        "unwritten — you hold $live live pieces for " +
                            "this movement. It can be committed."
                    else -> "✏️ RECONSTRUCTION: unwritten — " +
                        "$live/${m.evidenceNeeded} live pieces in " +
                            "hand. Inert evidence doesn't count: " +
                            "it hasn't happened to you yet, " +
                            "meaning-wise."
                })
            } else {
                val text = if (pick == "easy") m.easy else m.checked
                val stamp = when (verdict7(state, m)) {
                    "CONFIRMED" -> "✅ CONFIRMED — the past, once " +
                        "reached, agrees with this line"
                    "BROKEN" -> "💔 BROKEN — the past refutes this " +
                        "line. It stands struck; it can be struck " +
                        "and written again"
                    else -> "📌 COMMITTED — standing, untested; " +
                        "the past hasn't reached it yet"
                }
                out.add("🖊️ $stamp: “$text”")
            }
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory7(state)}")
        if (!state.arc7Retired && state.arc7Started) {
            out.add(forecastLine7(state))
        }
        if (state.arc7Retired) {
            out.add(when (state.arc7Branch) {
                "origin" -> "🏁 The call was made from the " +
                    "beginning: the timeline, complete and " +
                    "confirmed where it counts, was read into " +
                    "the room in order. The story now starts " +
                    "sixty years before its ending, in smoke, " +
                    "with a name nobody had said aloud in the " +
                    "gym since the winter it happened."
                "again" -> "🏁 The ending stood. The timeline " +
                    "was complete — every movement written, the " +
                    "beginning included — and the room kept the " +
                    "first version anyway. What is proven stays " +
                    "proven, on this board, in order. What is " +
                    "plaqued stays plaqued. The two documents " +
                    "now disagree in public, forever, politely."
                "mended" -> "🏁 One thing changed: the record " +
                    "was amended in the room, on the day, with " +
                    "the keeping in it. The timeline stays on " +
                    "this board as the working draft of a story " +
                    "the town is learning to tell with its " +
                    "middle in."
                else -> "🏁 The ceremony happened without the " +
                    "timeline's voice. ${clueCount7(state)} pieces " +
                        "of the past were in one person's hands " +
                        "that day. The room ran on the ending " +
                        "alone, the way it did the first time. " +
                        "Rooms keep doing that until somebody " +
                        "reads them a beginning."
            })
            if (state.arc7Branch == "origin") {
                if (state.arc7CasePending) {
                    out.add("🏫 One thing is still out there: the " +
                        "trowel's case, cracked since the fire, " +
                        "is being mended at last. Some mornings " +
                        "are worth walking past the gym door for.")
                } else if (state.arc7CaseBuilt) {
                    out.add("🏫 The trowel is home — new glass, " +
                        "green felt, the whole story on the " +
                        "plaque, beginning first, the keeper's " +
                        "name first. The file stays closed: this " +
                        "is what a closed file looks like when " +
                        "it's closed in the right order.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS7 = mapOf(
        "origin" to listOf(
            "The trowel case? They tell it fire-first now. " +
                "Takes longer. Nobody minds, which around here " +
                "is basically a parade.",
            "Read the plaque at the school sometime. Whole " +
                "story, both names, beginning first. Town put " +
                "its longest sentence on its oldest object. Growth."),
        "again" to listOf(
            "The trowel's downtown now, behind glass. Rescued, " +
                "says the plaque. The woman who kept it clean " +
                "for sixty years walks past it to the store. " +
                "Funny town.",
            "Homecoming went fine. Same speeches as the " +
                "assembly, with better catering. The keeper " +
                "left early. Nobody can say why that part " +
                "sticks in the throat."),
        "mended" to listOf(
            "Heard the school amended its record? Read a " +
                "second page into it, in a full gym. The " +
                "trowel's story has a middle now. Middles are " +
                "underrated.",
            "The keeper signed the trowel in again this " +
                "Founders', same as always — only now the " +
                "minutes say thank you. Sixty years late. " +
                "Minutes are like that."),
        "cold" to listOf(
            "The homecoming? It was the assembly again, with " +
                "a program. The one kid who'd read the whole " +
                "file wasn't there. Town clapped in the same " +
                "places. Muscle memory.",
            "Some case at the school got settled twice the " +
                "same way, I hear. Once by applause, once by " +
                "rerun. The archive keeps the other version. " +
                "The archive keeps everything. It's the " +
                "reading that's scarce."))

    /** A post-case ambient aside in the town's own voice —
     *  the ending the town lives with is the one it keeps
     *  telling, backwards or forwards. Null until the case
     *  is closed. */
    fun ambientCallback7(state: GameState, rnd: Random): String? {
        if (!state.arc7Retired) return null
        val bank = CALLBACKS7[state.arc7Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

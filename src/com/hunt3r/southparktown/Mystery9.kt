package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery9 — the ninth case (v0.11.0f, Queue #11 #6):
// "One More Than Counted".
//
// The pond case was witness-driven, the booth case a
// compressed whodunit, the buried case an excavation, the
// gardener case followed acts of maintenance, the
// confession case was built of claims, the opinion case
// was a meter, the backwards case was a timeline, and the
// alibi case was a graph. This one's shape is an AUDIT.
// Nothing in this case was taken. Something was ADDED —
// or the town's counting says so, which in a town that
// now keeps real books (a paper with an archive, a stand
// with a ledger, a yearbook, fair records) amounts to an
// accusation with arithmetic behind it. In the fall the
// Mayor orders a general stock-take: every public
// counted thing in town counted against its book. Four
// counts come back wrong, and every one of them is
// wrong by exactly one — one MORE than the record holds:
// cups on a shelf against the school's wins list, seats
// in a theater against its seating chart, boxes in a
// bank wall against the rental ledger, portraits in the
// mayors' gallery against the mayors' list.
//
// The board is the ledger: four count-sites, each with
// its counted number and its book number — the
// discrepancy derived from the two on every read, never
// stored — and for each site the player commits a
// RECONCILIATION CALL: THE COUNT IS WRONG (the counters
// miscounted; the room matches its book), THE RECORD IS
// WRONG (the book under-wrote it; the thing was always
// honestly there), or SOMEONE ADDED ONE (a real extra
// thing was placed there). Calls are recorded; VERDICTS
// are derived, never stored — each site has one piece of
// evidence that settles it, and a call standing with its
// settling piece live is PROVEN, a call standing against
// it is REFUTED (and can be struck and called again),
// anything else is COMMITTED, untested.
//
// Evidence: 12 pieces — 5 witness tellings, 5 Look finds,
// 2 participation beats — three tagged to every site.
// Two pieces are UNREADABLE when found: they go into the
// evidence set honestly inert (a carbon-copied receiving
// form whose ticked boxes mean nothing yet; a minute-
// book page in a dead clerk's shorthand) and become
// spendable only once the chapter that unlocks their
// context has begun. A piece that is not live settles
// nothing, proves nothing. The suite proves it.
//
// The finale is the audit's public closing — the
// Reckoning, in the council chamber at City Hall, where
// the Mayor wants the count closed one way or another.
// The player makes one of four calls. READ THE AUDIT
// ALOUD (gated on the ledger's final derived state: the
// trophy case and the gallery PROVEN, every site called,
// none refuted, at least three proven), NAME THE ADDER
// (the town's own answer — something was added, so a
// hand added it; gated on at least one site PROVEN),
// LET THE BOOKS STAND, or WALK AWAY. A closing nobody
// plays is its own landing: cold. The game never admits
// a call was wrong; the counted things just keep
// standing where they stand.
//
// The solution, in one breath, for the payoffs only:
// three of the four extras are the town's own paperwork
// wearing disguises, and the fourth came home by post.
// The Bijou's chart was drawn before the renovation
// that fitted a single aisle-end seat for the old
// projectionist; the chart to follow never followed —
// the room is right, the record is short. The bank's
// counters counted the demonstration box by the door —
// the open-backed tour piece with the pamphlet inside —
// because it was on the wall, and things on walls
// counted; the ledger is right and even says so, in a
// footnote the ledger's own drafter wrote and the
// counters never read. The gallery's extra face is the
// town's acting mayor of one illness week, who took the
// chair, signed the week's papers, sat for the painter
// as a courtesy, and was left out of the next clerk's
// fair copy of the list in different ink — the wall is
// right, the record dropped a man. And the trophy case
// is the one true addition: the school's oldest county
// cup, misfiled under the county's name for decades,
// came back in August in a box of unclaimed school
// property, was signed for at the school office and
// shelved with its fellows by people doing their jobs
// in daylight. No hand added anything in the night.
// Things were added by form, in triplicate, in August —
// and three of the town's four alarms were its own
// books, failing in three different directions.
//
// Structure:
//  - CHAPTERS9: four full ShenaniganDefs on the same
//    episode machinery, outside the random pool,
//    resolving in SHENANIGAN_BY_ID like every case.
//  - Gating: the case queues into the mystery slot TEN
//    days after the alibi case retires. While any
//    earlier case is open this one does not even
//    schedule.
//  - Retirement: every landing retires the arc forever.
//    The reconciled landing alone gets the long tail: a
//    week on, the gallery's list is reprinted whole.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery9 {

    const val CASE_TITLE = "One More Than Counted"
    const val FINALE9_ID = "arc9_ch4_the_reckoning"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the alibi case retires

    val CHOICE_KEYS = listOf("read", "name", "stand", "walk")
    const val DEFAULT_BRANCH = "cold"
    val BRANCH_KEYS = listOf("reconciled", "blamed", "shelved", "cold")

    const val LIST_RETURN_DELAY_DAYS = 7

    /** A reconciliation call, as the dialog and the save
     *  file name it: the count is wrong, the record is
     *  wrong, or someone added one. The verdict is never
     *  the player's to pick — the evidence grades the
     *  call. */
    val CALL_KEYS = listOf("count", "record", "added")

    // ---------------- the four chapters ----------------
    // They air a week apart in the player's present: the
    // count comes in (four alarms, one scandal), the rooms
    // counted again in person (seat by seat, cup by cup),
    // the books themselves read (a ledger, a list, a form,
    // a page of shorthand), and — in the finale — the
    // Reckoning, where the audit closes in public.

    val CHAPTERS9: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc9_ch1_the_count",
            title = "The Count Comes In",
            cause = "It's stock-take week by order of the " +
                "Mayor: every public counted thing in town " +
                "counted against its book, because a town " +
                "with a paper, an archive and a yearbook can " +
                "no longer un-see a discrepancy. The counting " +
                "crews have been at it since Monday with " +
                "clickers and clipboards, and this morning " +
                "the results go up on the board at City Hall — " +
                "and four of them are wrong, every one by " +
                "exactly one, every one by one MORE.",
            blurb = "Four counts, four alarms, each off by " +
                "one extra thing: cups on the Elementary's " +
                "trophy shelf against the wins list, seats in " +
                "the Bijou against the seating chart on file, " +
                "boxes in the bank's wall against the rental " +
                "ledger, faces in the mayors' gallery against " +
                "the mayors' list. Nothing was taken from " +
                "anywhere. Things, apparently, were ADDED. " +
                "The town has already named the culprit, the " +
                "way the town does: the Adder. The Mayor would " +
                "like the audit closed — with an explanation, " +
                "a name, or a filing — by the Reckoning.",
            theme = QuestTheme("🧮", "#5a6e4a", "ONE MORE THAN COUNTED"),
            tintHex = "#161c12",
            stages = listOf(
                ShenaniganStage("Kickoff: Read the Board",
                    "🧮 CITY HALL, THE RESULTS BOARD: The " +
                        "stock-take's findings, posted in the " +
                        "clerk's fair hand, in columns: the " +
                        "thing, the book, the count. Column " +
                        "after column agrees to the unit — the " +
                        "town is good at counting, and says so — " +
                        "until the four lines near the bottom, " +
                        "each marked with a small red star, each " +
                        "off by exactly one, each off by one " +
                        "MORE than the book holds. A crowd reads " +
                        "the board the way crowds read anything " +
                        "with red stars on it: twice, then " +
                        "aloud, then to each other.",
                    "A discrepancy of one is the worst kind: " +
                        "too small to be a theft, too exact to " +
                        "be a shrug. Four of them, in four of " +
                        "the town's most public rooms, all " +
                        "leaning the same way — toward MORE. " +
                        "The Mayor is doing the arithmetic in " +
                        "public, which is what she does instead " +
                        "of shouting. Things do not add " +
                        "themselves, she says. The town is " +
                        "already nodding. The town loves a " +
                        "sentence with a villain in it.",
                    "Read the results board at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading the board twice. Doing the subtraction out loud.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have read the board three times and named three different Adders. The Parents are not helping."))),
                ShenaniganStage("Escalation: The Shelf Everyone Saw",
                    "🧮 THE TROPHY CASE, BY CROWD: Of the four " +
                        "alarms, one is at kid height, behind " +
                        "glass, in a hallway every child in " +
                        "town walks twice a day — and it is the " +
                        "one the whole town has decided is the " +
                        "case. The shelf holds its cups in their " +
                        "rows. Under them, framed, hangs the " +
                        "school's wins list, engraved, official. " +
                        "The crowd in the hallway is counting " +
                        "both, with fingers, slowly, the way you " +
                        "count something you expect to catch.",
                    "Count them yourself and the shelf wins: " +
                        "the cups on the glass outnumber the " +
                        "lines on the list by one. The extra cup " +
                        "is not hiding. It stands in the back " +
                        "row with the old ones, tarnished, at " +
                        "ease, like it has a right to be there — " +
                        "which is, of course, the exact thing " +
                        "under investigation. Principal Victoria " +
                        "keeps the list. She is standing by it " +
                        "with her arms folded in a way that has " +
                        "ended arguments in four decades.",
                    "Count the trophy case at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Counting cups with fingers. Reading the list with lips moving.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff keeps saying 'the list is the list' in the tone it uses for weather."))),
                ShenaniganStage("Climax: Four Alarms, One Shape",
                    "🧮 CLIMAX: The Mayor reads the four lines " +
                        "aloud in the chamber, one at a time, " +
                        "and lets each one land. A cup the list " +
                        "doesn't know. A seat the chart doesn't " +
                        "know. A box the ledger doesn't know. A " +
                        "face the list of mayors doesn't know. " +
                        "'One is a mistake,' she says. 'Four is " +
                        "a pattern. Patterns have hands.' The " +
                        "chamber likes it. Chambers do. Somewhere " +
                        "in the room the word goes round at " +
                        "whisper speed and comes back wearing a " +
                        "cape: the Adder.",
                    "Patterns have hands — it is a good " +
                        "sentence and a terrible method, and the " +
                        "whole case lives in the difference. " +
                        "Four counts lean toward MORE, yes. But " +
                        "a count has three moving parts — the " +
                        "things, the book, and the counting — " +
                        "and an extra at the end can be born in " +
                        "any of the three. The ledger in your " +
                        "Journal has opened: four sites, four " +
                        "books, four counts, and for each one a " +
                        "call you will have to make and defend: " +
                        "the count is wrong, the record is wrong, " +
                        "or someone added one.",
                    "Hold every count up to its book — and to the counting.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Saying 'the Adder' at whisper speed. Watching a pattern get a cape.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents have a cape design for the Adder already. It has a plus sign on it."))),
                ShenaniganStage("Resolution: The Audit Stays Open",
                    "🧮 The Mayor closes the morning the way " +
                        "the audit will close three weeks from " +
                        "now: on the record. The count stands " +
                        "posted. The four rooms keep their extra " +
                        "things in plain sight — the cup stays " +
                        "on its shelf, the seat in its row, the " +
                        "box in its wall, the face in its " +
                        "gallery; nothing is to be moved, " +
                        "covered or quietly corrected while the " +
                        "audit is open, by her order, in writing. " +
                        "'Count them again,' she says, 'in " +
                        "person. Then read the books. Then we'll " +
                        "close it — all of it, out loud, in this " +
                        "room.'",
                    "An open audit is a kind of gift: it means " +
                        "the town will hold still while you work. " +
                        "The ledger is in your Journal — four " +
                        "sites, each with its book and its " +
                        "count, each waiting for a call. Start " +
                        "where counting can be done with your " +
                        "own hands: the rooms themselves.",
                    "Work the ledger: for each site — the count, the record, or a hand?",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Victoria"),
            episodeTitles = listOf("The Count Comes In", "Four Red Stars"),
            coldOpen = listOf(
                Line("McDaniels", "For the record — and this " +
                    "week everything is for the record — the " +
                    "town counted its public things against " +
                    "their books, the way I ordered, and four " +
                    "counts came back with one more thing in " +
                    "them than the books allow. I do not hold " +
                    "with things appearing. Things that appear " +
                    "have help. I intend to know whose."),
                Line("Victoria", "I keep the wins list at the " +
                    "school. I have kept it for years, in " +
                    "engraving, and I will tell you what I told " +
                    "the chamber: the list is complete, the " +
                    "list is correct, and the list says " +
                    "twenty-three. The shelf says twenty-four. " +
                    "One of us is wrong, and I did not engrave " +
                    "myself into this job to be wrong about a " +
                    "number I can point at."))),
        ShenaniganDef(
            id = "arc9_ch2_counted",
            title = "Counted in Person",
            cause = "A week on, the audit goes back to the " +
                "rooms and counts them the slow way: in " +
                "person, by hand, with witnesses. The Bijou " +
                "opens its doors on a dark afternoon and gives " +
                "up its seats one row at a time. The trophy " +
                "cup comes out of its case under supervision " +
                "and gets looked at the way evidence gets " +
                "looked at — closely, and by people who are " +
                "prepared to be surprised by it.",
            blurb = "The rooms, recounted, refuse to help the " +
                "books: the Bijou's seats still outnumber " +
                "their chart, and the cup on the school shelf " +
                "is neither new nor cheap nor shy. The " +
                "counting itself comes under the lamp this " +
                "week — who counted, with what, and what a " +
                "clicker hears through a parka. If the extra " +
                "things were added, they were added by " +
                "somebody who works in daylight and files " +
                "paperwork. If they weren't added at all, the " +
                "town owes four rooms an apology and one " +
                "cape a retirement.",
            theme = QuestTheme("🔢", "#4a5a6e", "COUNTED IN PERSON"),
            tintHex = "#12161c",
            stages = listOf(
                ShenaniganStage("Kickoff: The Dark Auditorium",
                    "🔢 THE BIJOU, HOUSE LIGHTS HALF: A theater " +
                        "in the daytime is all business: rows " +
                        "standing at attention in the gloom, the " +
                        "aisle lights ticking their small orange " +
                        "row. The audit's crew works the room the " +
                        "way the room was built — from the back, " +
                        "row by row, out loud, two voices calling " +
                        "and confirming, the way you'd count " +
                        "anything you intended to swear to. The " +
                        "chart from City Hall lies open on the " +
                        "projectionist's ledge, saying its number " +
                        "to nobody.",
                    "Count a theater and you learn its " +
                        "manners: it wants to be counted in the " +
                        "dark, by rows, with the screen watching. " +
                        "The chart on the ledge is a handsome " +
                        "document — drawn in ink, framed once, " +
                        "kept like a portrait of the room. The " +
                        "room, meanwhile, has gone on being a " +
                        "room: lived in, repaired, rearranged by " +
                        "decades. Documents keep the year they " +
                        "were drawn in. Rooms don't.",
                    "Count the Bijou's seats, row by row.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Counting rows out loud, two voices, in the gloom.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 2,
                            note = "Local Business counted the Bijou twice and got the room's number both times. Business trusts rooms over paper."))),
                ShenaniganStage("Escalation: The Clicker Confesses",
                    "🔢 THE COUNTING, CROSS-EXAMINED: The first " +
                        "count of the Bijou was not done by the " +
                        "two-voice method. It was done by one " +
                        "volunteer, at speed, with a hand clicker, " +
                        "through a parka hood, in a room where a " +
                        "matinee had just let out. The audit " +
                        "hears the method described in full, " +
                        "proudly, by the volunteer himself, who " +
                        "regards his number as a personal best and " +
                        "sees no reason the room, the chart and " +
                        "the town should not all be reorganized " +
                        "around it.",
                    "A clicker counts what it hears, and a " +
                        "hood hears what it likes: the first " +
                        "count of the theater is a story about a " +
                        "boy, a machine and a double row, and it " +
                        "proves almost nothing except that the " +
                        "town's fastest number was its least " +
                        "careful. That is a lesson the audit will " +
                        "need again before it's done: a count is " +
                        "an event, with a person in it, and " +
                        "persons have methods. Methods have " +
                        "weather.",
                    "Hear how the first counts were taken.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Describing a clicker's whole career. Defending a personal best.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Boys' Crew has adopted the clicker number as a matter of crew honor. It will not survive the week, but it's sincere."))),
                ShenaniganStage("Climax: The Cup, in Hand",
                    "🔢 CLIMAX: The trophy cup, out of its case " +
                        "at last, on a cloth, in the principal's " +
                        "office, with the list framed on the wall " +
                        "behind it saying twenty-three to its " +
                        "face. It is heavy in the way old silver " +
                        "is heavy. It is worn in the way things " +
                        "are worn by decades of being held up by " +
                        "children. Whoever the town is picturing " +
                        "— a kid with a hardware-store cup and a " +
                        "weekend of nerve — the thing on the cloth " +
                        "is not that picture. The room goes a " +
                        "little quiet around it, the way rooms do " +
                        "when the evidence is older than the " +
                        "accusation.",
                    "The town's favorite story needs a new " +
                        "cup, snuck in. The cup on the cloth is " +
                        "an old cup, at home. Stories and " +
                        "evidence part company here, quietly, " +
                        "the way they do: not with an argument — " +
                        "with a weight, a wear pattern, and a " +
                        "year cut into metal by a hand that " +
                        "worked a long time ago. If this cup " +
                        "was added to the shelf, it was not " +
                        "added lately, or lightly, or by a child " +
                        "with something to prove. It was added " +
                        "the way institutions add things: " +
                        "eventually, and with paperwork.",
                    "Hold the extra cup up to the light — and to the list.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Holding old silver up to the light. Being quiet around it.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has started saying 'the August question' in the tone it uses for weather."))),
                ShenaniganStage("Resolution: Rooms Counted, Books Next",
                    "🔢 The second count closes with the rooms " +
                        "standing by their numbers and the books " +
                        "standing by theirs, and the audit " +
                        "changes its question in the minutes: not " +
                        "WHERE the extra things are — the rooms " +
                        "have answered that, twice — but how the " +
                        "books came to say less than the rooms " +
                        "hold, or the counting came to say more. " +
                        "Next week the audit reads: a ledger, a " +
                        "list, a form, and whatever the archive " +
                        "boxes are keeping.",
                    "The rooms are done talking; they only " +
                        "ever say their numbers. The books are " +
                        "the talkative ones: every book in this " +
                        "case was written by somebody, on a day, " +
                        "for a reason — and books, unlike rooms, " +
                        "remember their reasons. The bank's " +
                        "ledger has a drafter. The gallery's list " +
                        "has a history. The school's shelf has a " +
                        "paper trail with carbon copies. Read " +
                        "the books the way you'd read a witness: " +
                        "for what they were doing the day they " +
                        "were written.",
                    "Work the ledger: the rooms have said their numbers — now read the books.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Kenny", "Victoria"),
            episodeTitles = listOf("Counted in Person", "The Clicker Confesses"),
            coldOpen = listOf(
                Line("Kenny", "Mrrm mrrm mrrph — mmph, mmph " +
                    "mrrm! (I counted the whole theater myself, " +
                    "with the clicker, during the matinee rush, " +
                    "and I stand by my number. It is a big " +
                    "number. It is MY number. If the chart says " +
                    "less, the chart wasn't there. I was there. " +
                    "I clicked.)"),
                Line("Victoria", "The cup is out of the case " +
                    "this week, under supervision, and I will " +
                    "tell you what I told the counting crew: " +
                    "look at it as long as you like. Engrave " +
                    "your conclusions afterward. A list is not " +
                    "a rumor, and a shelf is not an argument, " +
                    "and I am tired of being the only person in " +
                    "this audit who has read both."))),
        ShenaniganDef(
            id = "arc9_ch3_books",
            title = "What the Books Say",
            cause = "A week on, the audit stops counting " +
                "things and starts reading books: the bank's " +
                "box ledger, open on its counter under its " +
                "drafter's eye; the mayors' list, unrolled " +
                "beside the gallery it claims to describe; a " +
                "receiving form from the school office's " +
                "August, much carbon-copied; and the archive " +
                "boxes in the City Hall basement, which keep " +
                "what the fair copies upstairs leave out.",
            blurb = "Every book in this case was written by " +
                "somebody, on a day, for a reason. The bank's " +
                "ledger carries a line at its back that its " +
                "own drafter wrote and its counters never " +
                "read. The gallery's list is a fair copy — " +
                "and fair copies are where names go to be " +
                "lost politely. The school's shelf turns out " +
                "to have a paper trail, in triplicate, most " +
                "of it carbon and none of it read. And in the " +
                "basement boxes, a loose page of minutes in a " +
                "dead clerk's shorthand is waiting for its " +
                "context to come back into the world.",
            theme = QuestTheme("📚", "#6e5a4a", "WHAT THE BOOKS SAY"),
            tintHex = "#1c1612",
            stages = listOf(
                ShenaniganStage("Kickoff: The Ledger, Open",
                    "📚 THE BANK, THE LEDGER ON THE COUNTER: " +
                        "The box ledger is a book built like a " +
                        "door: cloth boards, a spine that has " +
                        "been re-glued twice, page after page of " +
                        "boxes in their hundreds, each with a " +
                        "number, a name, a year. The counting " +
                        "crew counted the WALL. The book sits " +
                        "here saying what the wall means — all " +
                        "of what it means, including the part at " +
                        "the back, in the drafter's own hand, " +
                        "that nobody performing the count " +
                        "thought to turn to.",
                    "A ledger is a room's memory with an " +
                        "index, and this one remembers more " +
                        "than its counters asked it. The wall " +
                        "count and the book count differ by one, " +
                        "and the difference is standing by the " +
                        "bank's front door being shiny at " +
                        "tourists. Books should be read to the " +
                        "last page before walls are accused. " +
                        "So should people, the drafter says, " +
                        "from forty years down the counter. " +
                        "He's here all week. He's been here " +
                        "forty years.",
                    "Read the bank's box ledger at the Bank.",
                    routine = "cluster", clusterLoc = "Bank",
                    clusterActivity = "Turning a ledger's pages with clean hands. Reading to the last page.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 2,
                            note = "Local Business is reading its books to the last page this week, all over town, just in case."))),
                ShenaniganStage("Escalation: The Gallery, Face by Face",
                    "📚 THE MAYORS' GALLERY, UNROLLED LIST IN " +
                        "HAND: Portrait by portrait, name by " +
                        "name, the list keeps up beautifully — " +
                        "founder, successors, the whole painted " +
                        "century — until it doesn't: one face " +
                        "with a sash and a plaque and no line " +
                        "in the roll. The Mayor counted this " +
                        "wall herself, twice, and has brought " +
                        "the discrepancy to the audit the way " +
                        "she brings everything: as a decision " +
                        "she intends somebody to make. 'Either " +
                        "he's ours,' she says, 'or he's coming " +
                        "down. I don't hang strangers.'",
                    "The extra face is not a prank face. It " +
                        "wears the sash in the gallery's own " +
                        "style, by the gallery's own painter, " +
                        "in the gallery's own years — somebody " +
                        "sat for this, officially, and the town " +
                        "paid for the paint. A list can lose a " +
                        "line a dozen honest ways and one " +
                        "dishonest one, and a fair copy is " +
                        "where all of them look identical. The " +
                        "question is not whether the book is " +
                        "wrong. It's whose week the book " +
                        "declined to keep.",
                    "Walk the mayors' gallery with the list in hand.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Matching faces to names, slowly. Not hanging strangers.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are divided on the extra mayor: half want him kept for the mystery, half want him named by Friday."))),
                ShenaniganStage("Climax: Carbon, Shorthand, Ink",
                    "📚 CLIMAX: The audit's paper, laid out on " +
                        "the chamber table in one long row: a " +
                        "receiving form from an August nobody " +
                        "remembers, carbon-copied in triplicate, " +
                        "about one metal object the school " +
                        "office took in; the school's receiving " +
                        "book, with an August line in a clerk's " +
                        "initials that the audit reads aloud to " +
                        "a room that has stopped whispering; " +
                        "and, from the basement boxes, a loose " +
                        "page of minutes in shorthand — dates " +
                        "readable, everything else a dead " +
                        "clerk's private weather — which the " +
                        "room agrees, in the way rooms agree on " +
                        "things they can't do anything about, " +
                        "to keep until it can be read.",
                    "Paper keeps what people were doing, in " +
                        "the order they did it, with their " +
                        "initials on it — which is more than " +
                        "any witness in this town has ever " +
                        "managed. The form says something came " +
                        "in, in August, and was put with its " +
                        "kind. Whose kind, and by what road, " +
                        "is a story the form assumes its " +
                        "reader already knows. Forms always " +
                        "assume that. It's how the past hides: " +
                        "in paperwork that thinks it's boring.",
                    "Read the audit's paper: the form, the receiving book, the page.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading carbon copies aloud. Keeping a page until it can be read.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff's receiving book is being read this week with the attention usually reserved for scripture."))),
                ShenaniganStage("Resolution: One Page Still Shut",
                    "📚 The books week closes with the paper " +
                        "in its row and one page still shut: the " +
                        "shorthand minutes, dated in a gallery " +
                        "year, kept by the audit against the " +
                        "day its context walks back into the " +
                        "room — and contexts, in this town, have " +
                        "a way of arriving with the finale. Next " +
                        "week: the Reckoning, in the chamber, " +
                        "where the audit closes in public — with " +
                        "the ledger read aloud, or a name named, " +
                        "or the books let stand exactly as " +
                        "written, by the only person who has " +
                        "read all of them.",
                    "A ledger with three sites read and one " +
                        "page shut is not a finished ledger — " +
                        "the Reckoning's terms are on the board, " +
                        "and they are strict about finished. " +
                        "Whatever can still be counted, called " +
                        "or read, read it now. What the room " +
                        "does with a half-read audit is not " +
                        "the room's fault. Rooms run on what " +
                        "they're handed.",
                    "Work the ledger: call every site before the Reckoning — a half-read audit closes badly.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Butters", "McDaniels"),
            episodeTitles = listOf("What the Books Say", "Carbon and Shorthand"),
            coldOpen = listOf(
                Line("Butters", "Oh, I counted the bank wall " +
                    "real careful, yes I did — every single " +
                    "box, even the shiny one by the door, on " +
                    "account of it being on the wall and all. " +
                    "My dad says a count you can defend is a " +
                    "count you counted ALL of. I counted all " +
                    "of it. Every bit. If there's a problem, " +
                    "it's a problem with the wall, 'cause the " +
                    "count was complete."),
                Line("McDaniels", "The gallery is mine to " +
                    "keep, and I'll tell the audit what I told " +
                    "the council: I counted those faces myself. " +
                    "Twice. Fourteen faces, thirteen names, " +
                    "and the fourteenth has a sash, a plaque " +
                    "and a painter's signature our painter " +
                    "signed with, in a year our list behaves " +
                    "like it would rather forget. Either the " +
                    "man is ours, or he's coming down. I do " +
                    "not hang strangers in my hallway."))),
        ShenaniganDef(
            id = FINALE9_ID,
            title = "The Reckoning",
            cause = "Three weeks of counting close tonight: " +
                "the council chamber, the results board brought " +
                "in from the hall, the four books on the table " +
                "in a row, and a room full of townsfolk who " +
                "have come to watch an audit end. The Mayor " +
                "intends to close the count in public — with " +
                "the ledger read aloud, or the Adder named, or " +
                "the books let stand — and she has left a space " +
                "in the program, between the minutes and the " +
                "adjournment, labeled in the clerk's fair hand: " +
                "THE COUNT, CLOSED.",
            blurb = "The Reckoning: one room, four books, and " +
                "every extra thing in town standing exactly " +
                "where it stood. The ledger in your Journal " +
                "is either finished or it isn't — every site " +
                "called, its call graded by the piece that " +
                "settles it. Read it aloud and the town gets " +
                "its arithmetic back. Name the Adder and the " +
                "room gets the name it came for. Let the books " +
                "stand and the count stands with them, stars " +
                "and all. Walk away, and the chamber closes " +
                "the count the way chambers do: by feel, by " +
                "applause, by the nearness of a name.",
            theme = QuestTheme("⚖️", "#7a4a4a", "THE RECKONING"),
            tintHex = "#1c1214",
            stages = listOf(
                ShenaniganStage("Kickoff: The Books on the Table",
                    "⚖️ THE CHAMBER, COUNTING HOUR: The four " +
                        "books in a row on the long table — a " +
                        "wins list, a seating chart, a box " +
                        "ledger, a mayors' roll — and behind " +
                        "them the results board with its four " +
                        "red stars, brought in from the hall " +
                        "like evidence at a trial, which in " +
                        "every way that matters it is. The room " +
                        "fills the way it fills for a naming. " +
                        "Rooms can't help it. It's why they have " +
                        "chairs.",
                    "Every extra thing in this case is " +
                        "standing, tonight, exactly where it " +
                        "stood: a cup in the back row of a " +
                        "school shelf, a seat at the end of a " +
                        "theater aisle, a shiny box by a bank's " +
                        "door, a face in a hallway of mayors. " +
                        "Four things, four books, and one " +
                        "ledger that knows — or doesn't — how " +
                        "each of them got there. The Mayor is " +
                        "already looking at the empty space in " +
                        "the program. The space is looking back.",
                    "Take your seat at the Reckoning.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Filling a chamber the way rooms fill for a naming.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have come for a name and brought the cape, in case. The cape has been asked to wait in the hall."))),
                ShenaniganStage("Escalation: The Nearest Name",
                    "⚖️ THE ROOM WARMS ITS HANDS: An audit " +
                        "without a villain is a hard sell in a " +
                        "full chamber, and the room has been " +
                        "quietly solving that problem the way " +
                        "rooms do: by nearness. Who has the " +
                        "school's keys? Who has a workshop, a " +
                        "lathe, a shelf of school metal? The " +
                        "questions are not accusations. They are " +
                        "just the shape a room makes when it " +
                        "reaches for the nearest name — and the " +
                        "nearest name, tonight, works at the " +
                        "school, and has not been asked a single " +
                        "question by the audit, and is sitting " +
                        "very still in the third row.",
                    "Nearness is not evidence; it's just " +
                        "geography with confidence. The man in " +
                        "the third row has the keys because " +
                        "keys are his job, and a lathe because " +
                        "the shop is his classroom, and the " +
                        "whole of his involvement in this case " +
                        "is that the town's arithmetic, run " +
                        "carelessly, lands on the nearest desk. " +
                        "The ledger is the only thing in the " +
                        "room that can out-argue a room. It had " +
                        "better be finished.",
                    "Watch the room reach for the nearest name.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reaching for the nearest name. Sitting very still in the third row.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff is sitting with its shop teacher tonight, in a row, on purpose. Rows are also a kind of count."))),
                ShenaniganStage("Climax: The Count, Closed",
                    "⚖️ CLIMAX: The Mayor raps the table and " +
                        "the program reaches its labeled space: " +
                        "THE COUNT, CLOSED. The books are open. " +
                        "The board's red stars are lit by the " +
                        "chamber's lamps. Somewhere between " +
                        "the minutes and the adjournment, the " +
                        "audit ends — by a reading, by a name, " +
                        "by a filing, or by a silence shaped " +
                        "exactly like whichever of those the " +
                        "room wanted most. The clerk's pen is " +
                        "up. Pens, in this chamber, are how " +
                        "endings get their handwriting.",
                    "Everything countable has been counted " +
                        "twice. Everything readable is read " +
                        "except one page, whose context arrived " +
                        "with the week, the way contexts do. " +
                        "What remains is the call — not about " +
                        "what the numbers are; the numbers have " +
                        "been the numbers all along — but about " +
                        "what the town will say they mean: the " +
                        "whole audit, read aloud; or one name, " +
                        "for the hand the room is sure it sees; " +
                        "or nothing at all, the books standing " +
                        "as written, the stars standing as " +
                        "posted. The pen is up. The room is " +
                        "yours, or it isn't.",
                    "Close the count: read the audit, name the hand, let the books stand, or walk.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Holding a pen up over the minutes. Ending an audit in handwriting.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents have been promised the cape stays in the hall whatever happens. It is not a comfort to the man in the third row."))),
                ShenaniganStage("Resolution: In the Minutes",
                    "⚖️ The Reckoning ends the way audits end: " +
                        "in handwriting. Whatever was said is " +
                        "said into the minutes, in the clerk's " +
                        "fair hand, and the board's red stars " +
                        "come down or stay up by the same " +
                        "authority. The books go back to their " +
                        "rooms. The rooms keep their numbers. " +
                        "And the four extra things keep standing " +
                        "where they stand — explained, or named " +
                        "at, or filed — while the town walks " +
                        "home past all four of them in the dark, " +
                        "counting nothing, for once, at all.",
                    "An audit closes a count, not a town. The " +
                        "cup will be in the hallway tomorrow. " +
                        "The seat will be sat in. The shiny box " +
                        "will shine at the next tour. The face " +
                        "will hang in its row, watching the " +
                        "corridor with the others. What tonight " +
                        "decides is which story the town tells " +
                        "about them — the ledger's story, or " +
                        "the room's. It has always, only, ever " +
                        "been that.",
                    "The count is closed. Walk home past all four of them.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Adler"),
            episodeTitles = listOf("The Reckoning", "The Count, Closed"),
            coldOpen = listOf(
                Line("McDaniels", "Three weeks ago this town " +
                    "counted its things and found four of " +
                    "them multiplied by one. Tonight the count " +
                    "closes, in this room, on the record — " +
                    "with the whole audit read aloud, or with " +
                    "the name of the hand that helped them " +
                    "multiply, or with the books standing as " +
                    "written and the stars standing as posted. " +
                    "Those are the endings on the program. " +
                    "There is also the ending where nobody " +
                    "reads anything. I have seen that ending. " +
                    "It wears applause."),
                Line("Adler", "I was told there'd be questions " +
                    "for me tonight, on account of the keys. " +
                    "I've got the keys because the boilers are " +
                    "also mine, and the shop, and the stage " +
                    "flats, and the marquee's ladder, which — " +
                    "before anybody starts — is a ladder. I " +
                    "teach shop. I fix what's handed to me. " +
                    "Nobody handed me a cup, a seat, a box or " +
                    "a mayor, and I'd like the minutes to show " +
                    "I said that sitting down, in my seat, " +
                    "which is where I've been the whole case.")))
    )

    val CHAPTER9_BY_ID: Map<String, ShenaniganDef> =
        CHAPTERS9.associateBy { it.id }

    fun chapterIndexOf9(id: String): Int =
        CHAPTERS9.indexOfFirst { it.id == id }

    fun isChapter9(id: String?): Boolean =
        id != null && id in CHAPTER9_BY_ID

    // ---------------- the ledger's rows: four count-sites ----------------
    // A site is a counted set and its book: the shelf and
    // the wins list, the room and the chart, the wall and
    // the ledger, the gallery and the roll. The counted
    // number and the book number are facts of the case;
    // the DISCREPANCY is not stored anywhere — it is the
    // difference of the two, recomputed wherever it's
    // shown. Every one of the four differences is +1:
    // that is the premise the town itself states in
    // chapter 1, in public, with the board lit. What a
    // site is FOR is the call: which of the count's three
    // moving parts — the things, the book, the counting —
    // produced the extra.

    data class Site9(
        val id: String,
        /** Board order: the case, the seats, the boxes,
         *  the gallery. */
        val index: Int,
        val title: String,
        /** Where the counted set stands (a LOCATIONS id). */
        val place: String,
        /** Who keeps the book, as the town recorded it. */
        val keeper: String,
        /** The counted set, in words. */
        val countedLabel: String,
        /** The count the stock-take got. */
        val counted: Int,
        /** The book, in words. */
        val recordLabel: String,
        /** What the book holds. */
        val record: Int,
        /** The evening as it actually ran (sealed): which
         *  call this site's evidence settles on. */
        val truth: String,
        /** The one piece that settles this site. */
        val proofBy: String,
        /** 0-based chapter that must have BEGUN before
         *  this site can be called. */
        val minChapter: Int,
        /** Live pieces tagged to this site that a call
         *  costs. */
        val callPrice: Int = 2
    )

    val SITES9: List<Site9> = listOf(
        Site9("site_trophies", 0, "THE TROPHY CASE",
            "Elementary", "Principal Victoria, who keeps the wins list",
            "cups on the shelf", 24,
            "lines on the engraved wins list", 23,
            truth = "added", proofBy = "county_form",
            minChapter = 1),
        Site9("site_seats", 1, "THE BIJOU'S SEATS",
            "Bijou", "the seating chart on file at City Hall",
            "seats in the room", 121,
            "seats on the chart", 120,
            truth = "record", proofBy = "renovation_invoice",
            minChapter = 1),
        Site9("site_boxes", 2, "THE BANK'S BOX WALL",
            "Bank", "the rental ledger, kept at the counter",
            "boxes on the wall", 208,
            "boxes in the ledger", 207,
            truth = "count", proofBy = "demo_box",
            minChapter = 2),
        Site9("site_portraits", 3, "THE MAYORS' GALLERY",
            "CityHall", "the mayors' roll, kept by the clerk",
            "portraits on the wall", 14,
            "names on the roll", 13,
            truth = "record", proofBy = "succession_page",
            minChapter = 2)
    )

    val SITE9_BY_ID: Map<String, Site9> = SITES9.associateBy { it.id }

    /** A site's discrepancy, derived from its two numbers
     *  on every read — the ledger stores numbers, never
     *  conclusions. */
    fun discrepancy9(s: Site9): Int = s.counted - s.record

    // ---------------- the evidence ----------------

    enum class Kind9 { WITNESS, LOOK, PARTICIPATION }

    data class Clue9(
        val id: String,
        val kind: Kind9,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        /** The site (index) this piece is tagged to — the
         *  row of the ledger it speaks to, however late it
         *  is found. */
        val site: Int,
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

    val CLUES9: List<Clue9> = listOf(
        // ---------- site 0: the trophy case ----------
        Clue9("victoria_count", Kind9.WITNESS, 0, 0, 0,
            "The keeper of the list (Victoria)",
            "The wins list's keeper, on the record: she " +
                "counted the shelf herself when the crew's " +
                "number came in, twice, and got their number " +
                "both times — the shelf really does hold one " +
                "more cup than her list has lines. Her list, " +
                "she will tell you, is built from the school's " +
                "own records of wins, engraved as they " +
                "happen, and nothing goes on it from memory, " +
                "from hope, or from a box.",
            "The keeper of the list (Victoria)",
            npc = "Victoria",
            spoken = "I'll save you the shoe leather: I " +
                "counted the shelf myself, twice, when the " +
                "crew's figure came in, and I got their " +
                "figure. Twenty-four. My list has " +
                "twenty-three lines, and I engraved most of " +
                "them. So. Either a win happened that this " +
                "school forgot to feel proud of — or " +
                "something is standing on my shelf that " +
                "never won anything here. I keep the list. " +
                "Somebody else, apparently, keeps the shelf."),
        Clue9("old_cup", Kind9.LOOK, 0, 0, 0,
            "The extra cup, in hand",
            "Out of the case and in hand, the extra cup " +
                "makes the town's favorite story work very " +
                "hard: it is heavy, worn at the handles, and " +
                "engraved — in the old stamping hand, not " +
                "the modern one — with a county title and a " +
                "year from before the wins list's first " +
                "line. Punched small into the base, the way " +
                "offices used to mark property: a county " +
                "stamp. Whatever road this cup traveled, it " +
                "was a long one, and it did not start in a " +
                "hardware store last month.",
            "The extra cup, in hand",
            locId = "Elementary",
            foundText = "🧮 THE EXTRA CUP: heavy, handle-worn, " +
                "engraved in the old stamping hand with a " +
                "county title and a year from before the " +
                "list's first line — and punched small into " +
                "its base, the way offices mark property: a " +
                "county stamp."),
        Clue9("county_form", Kind9.LOOK, 1, 0, 2,
            "A receiving form, in triplicate",
            "The school office's receiving form for one " +
                "August week, read with the receiving book " +
                "open beside it — the two documents agree, " +
                "initial for initial: one metal cup, school " +
                "property by its base stamp, came into the " +
                "office in a county box of unclaimed school " +
                "property, was signed for by the office, and " +
                "was put — the form's own words, in the " +
                "clerk's fair hand — 'with its kind, back " +
                "shelf.' Its kind was the trophy shelf. The " +
                "box's road is all here, in carbon: a county " +
                "office, a misfiled shelf of decades, a " +
                "fall cleaning, a van. Nobody carried this " +
                "cup in the night. It traveled by form, in " +
                "daylight, in triplicate.",
            "A receiving form, much carbon-copied, about " +
                "one metal cup that came into the school " +
                "office in a box in August. Its ticked boxes " +
                "and initials mean nothing to you yet — the " +
                "receiving book it belongs with is across " +
                "town, and office paper only tells its story " +
                "in company.",
            locId = "CityHall",
            foundText = "🧮 A RECEIVING FORM: carbon-copied " +
                "in triplicate, from the school office's " +
                "August — one metal cup, a county box, a " +
                "clerk's initials. You can read every word " +
                "and understand none of it: its book is " +
                "elsewhere, and paper this dull keeps its " +
                "meaning in company."),
        // ---------- site 1: the Bijou's seats ----------
        Clue9("kenny_clicker", Kind9.WITNESS, 1, 1, 1,
            "The first count (Kenny)",
            "The Bijou's first stock-take count, told by " +
                "its taker: one volunteer, one hand clicker, " +
                "one matinee letting out, and a final number " +
                "— one hundred twenty-four — that he defends " +
                "the way boys defend a high score. His route " +
                "took him down one row twice ('the good " +
                "row,' by his account, muffled), and his " +
                "clicker clicked a patron asleep across two " +
                "seats as a seat, twice. The town's first " +
                "theory — the theater had quietly grown " +
                "seats to sell — was built on this number. " +
                "The number is a story about a boy and a " +
                "machine. The careful two-voice count that " +
                "followed it is the one on the board.",
            "The first count (Kenny)",
            npc = "Kenny",
            spoken = "Mmph mmph mrrm — mmph! (A hundred and " +
                "twenty-four! I counted during the rush " +
                "with the clicker and I barely even sat " +
                "down. The middle row's the good row, I did " +
                "that one twice to be safe. And the guy " +
                "asleep across the two seats — click, " +
                "click — he was basically furniture. If " +
                "the chart says a hundred and twenty, the " +
                "chart is missing seats AND a guy.)"),
        Clue9("own_count", Kind9.PARTICIPATION, 1, 1, 1,
            "Your own count of the Bijou",
            "Your own count of the theater, taken the " +
                "audit's way — from the back, row by row, " +
                "out loud, twice, with the chart open on " +
                "the ledge: one hundred twenty-one seats " +
                "both times, standing in the room, in the " +
                "flesh, upholstered. The room really does " +
                "hold one more seat than its chart. Whatever " +
                "the extra seat is, it is not a counting " +
                "error, and it is not new: its springs have " +
                "the settled sag of a seat that has been " +
                "sat in for years.",
            "Your own count of the Bijou",
            chapterDefId = "arc9_ch2_counted"),
        Clue9("renovation_invoice", Kind9.LOOK, 2, 1, 2,
            "The renovation invoice",
            "The Bijou's renovation file, in City Hall's " +
                "cabinets: permits, a painter's bill, and " +
                "one invoice for seating work with a single " +
                "line, initialed by the chart's own drawer, " +
                "for one seat fitted at the aisle's end, " +
                "'for the projectionist, per his back' — and " +
                "under it, in the same hand, a note to " +
                "himself: 'chart to follow.' The chart on " +
                "file is dated the spring BEFORE the " +
                "renovation. The chart to follow never " +
                "followed. The seat sat in the room for " +
                "years, holding projectionists, unrecorded, " +
                "because a man trusted his own margin note.",
            "The renovation invoice",
            locId = "CityHall",
            foundText = "🧮 THE RENOVATION FILE: one invoice " +
                "with one line — a single seat fitted at " +
                "the aisle's end, 'for the projectionist, " +
                "per his back,' initialed by the man who " +
                "drew the seating chart. In his margin, in " +
                "his hand: 'chart to follow.' The chart on " +
                "file predates the work by a season."),
        // ---------- site 2: the bank's box wall ----------
        Clue9("butters_boxes", Kind9.WITNESS, 2, 2, 2,
            "The wall's counter (Butters)",
            "The bank wall's stock-take counter, on the " +
                "record, complete and unrepentant: he counted " +
                "every box on the wall, top to bottom, " +
                "because a count you can defend is a count " +
                "you counted all of — including, he will tell " +
                "you twice, the shiny one by the door, 'on " +
                "account of it being on the wall and all.' " +
                "His number — the board's number — is the " +
                "number of things on the wall shaped like " +
                "boxes. What the wall's things ARE is a " +
                "question his method never asked.",
            "The wall's counter (Butters)",
            npc = "Butters",
            spoken = "Well, I counted 'em all, every one — " +
                "top row, bottom row, the ones behind the " +
                "plant, even the shiny one by the door, on " +
                "account of it bein' on the wall and all. " +
                "My dad says if you're gonna count a thing, " +
                "you count ALL of it, or folks'll say you " +
                "counted the easy part. I counted the easy " +
                "part AND the shiny part. If the book says " +
                "one less, then the book missed one, 'cause " +
                "I sure didn't."),
        Clue9("gerald_footnote", Kind9.WITNESS, 2, 2, 2,
            "The ledger's drafter (Gerald)",
            "The box ledger's drafter — the lawyer whose " +
                "office drew it up when the bank's wall was " +
                "built — has kept his counsel for forty " +
                "years and breaks it now exactly once: there " +
                "is a line at the back of the book, in his " +
                "own hand, that he has been waiting four " +
                "decades for a reader to reach. He will not " +
                "say what it says. He says only that he " +
                "wrote it the week the wall opened, that it " +
                "concerns the thing by the door, and that a " +
                "book should be read to its last page before " +
                "its wall is accused of anything.",
            "The ledger's drafter (Gerald)",
            npc = "Gerald",
            spoken = "I drew up that ledger, you know. " +
                "Young lawyer, first big client, very " +
                "proud of the numbering. There's a line at " +
                "the back of the book, in my hand, that I " +
                "wrote the week the wall opened — about " +
                "the thing by the door. I've waited a long " +
                "time for someone to read a book all the " +
                "way to the back before accusing it. Turn " +
                "to the last page, counselor. It's why we " +
                "put the important things where only the " +
                "thorough will find them."),
        Clue9("demo_box", Kind9.LOOK, 2, 2, 2,
            "The shiny box by the door",
            "The shiniest box on the bank's wall, examined: " +
                "it has no lock cylinder, its back stands " +
                "open, and inside it, curled with age, sits " +
                "a pamphlet explaining how to rent a box. " +
                "Screwed to its frame is a small brass " +
                "plate: ASK AT THE COUNTER FOR THE TOUR. It " +
                "is the wall's demonstration piece — the " +
                "box the staff have opened for customers " +
                "since the wall was built, the one thing on " +
                "the wall that was never a box for rent. " +
                "And at the back of the ledger, in the " +
                "drafter's hand, last page, it is written " +
                "down as exactly that: present, accounted, " +
                "and NOT among the numbered boxes. The book " +
                "was right. The wall was right. The count " +
                "counted a tour.",
            "The shiny box by the door",
            locId = "Bank",
            foundText = "🧮 THE SHINY BOX: no lock cylinder. " +
                "Its back stands open. Inside, a curled " +
                "pamphlet about renting a box, and screwed " +
                "to the frame, a small brass plate: ASK AT " +
                "THE COUNTER FOR THE TOUR."),
        // ---------- site 3: the mayors' gallery ----------
        Clue9("mcdaniels_gallery", Kind9.WITNESS, 2, 3, 2,
            "The gallery's keeper (McDaniels)",
            "The gallery's keeper, on the record: she " +
                "counted the faces herself, twice, and the " +
                "wall really does hold one more portrait " +
                "than the mayors' roll has names. The extra " +
                "face wears the sash, hangs in the gallery's " +
                "style, and carries the gallery painter's " +
                "signature in a year the roll treats as a " +
                "seam between two administrations. She has " +
                "one rule for her hallway and she states it " +
                "flatly: she does not hang strangers. Either " +
                "the man is the town's, by some road the " +
                "roll forgot — or he comes down.",
            "The gallery's keeper (McDaniels)",
            npc = "McDaniels",
            spoken = "I counted them myself, so let's not " +
                "waste the audit's time: fourteen faces, " +
                "thirteen names. The fourteenth has the " +
                "sash, the painter's signature — our " +
                "painter, child, not a copyist — and a " +
                "year on the plaque from a stretch the roll " +
                "papers over like it was a hallway between " +
                "two real rooms. I have kept this gallery " +
                "for years and I will tell you my rule: I " +
                "do not hang strangers. So either that man " +
                "belongs to this town in a way the roll " +
                "has misplaced — or he is coming down, and " +
                "I will take him down myself, and I will " +
                "enjoy the wall space."),
        Clue9("extra_portrait", Kind9.LOOK, 2, 3, 2,
            "The fourteenth face",
            "The gallery's extra portrait, up close: a " +
                "man in the mayoral sash, painted in the " +
                "gallery's house style, the brushwork and " +
                "the craquelure right for its wall-neighbors, " +
                "signed by the gallery's own painter. The " +
                "plaque gives a name — a town name, an old " +
                "council family — and a single year, the " +
                "year the roll steps over in one stride " +
                "between two administrations. He is not " +
                "smiling the way fakes smile. He has the " +
                "particular expression of a man sitting " +
                "for a portrait he did not expect to sit " +
                "for, in a week he did not expect to have.",
            "The fourteenth face",
            locId = "CityHall",
            foundText = "🧮 THE FOURTEENTH FACE: the sash, " +
                "the gallery's own painter's signature, and " +
                "a plaque with a town name and a single " +
                "year — the year the roll steps over " +
                "between two administrations."),
        Clue9("succession_page", Kind9.PARTICIPATION, 2, 3, 3,
            "A page of minutes, in shorthand",
            "The loose page from the archive boxes, read " +
                "at last with its week restored around it: " +
                "the succession minutes, in the dead clerk's " +
                "shorthand, for the year of the gallery's " +
                "seam. In the mayor's illness that year, the " +
                "council's senior member took the chair for " +
                "the days between two dates, signed the " +
                "week's papers in the mayor's stead, kept " +
                "the office's hours, and — the minutes' own " +
                "last line, underlined once — 'sat for the " +
                "gallery painter, the chair being the " +
                "chair, as a courtesy to the wall.' The " +
                "next clerk's fair copy of the roll, made " +
                "the year after, left his line out — the " +
                "one name in the book dropped between a " +
                "draft and a copy, in a different ink. The " +
                "wall kept him. The book lost him. The " +
                "portrait is not the extra thing here. " +
                "The missing line is.",
            "A loose page of council minutes, in a dead " +
                "clerk's shorthand, dated in a gallery year. " +
                "You can read the dates and the underlinings " +
                "and nothing else — shorthand is a locked " +
                "room, and its context hasn't come back into " +
                "the world yet.",
            chapterDefId = "arc9_ch3_books")
    )

    val CLUE9_BY_ID: Map<String, Clue9> = CLUES9.associateBy { it.id }
    val CLUE9_IDS: Set<String> = CLUES9.map { it.id }.toSet()

    fun clueCount9(state: GameState): Int = state.mystery9Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable9(state: GameState, clue: Clue9): Boolean =
        !state.arc9Retired && state.arc9Chapter > clue.minChapter

    /** A piece is LIVE — spendable, settling — when it is
     *  in hand AND the chapter that unlocks its context
     *  has begun. Ordinary pieces are live the moment they
     *  exist. The two locked pieces sit in hand, honestly
     *  inert, until their context arrives: they settle
     *  nothing, prove nothing. Derived, never stored. */
    fun clueLive9(state: GameState, clue: Clue9): Boolean =
        clue.id in state.mystery9Clues &&
            state.arc9Chapter > clue.unlockChapter

    /** The ids of everything currently spendable. */
    fun liveClueIds9(state: GameState): Set<String> =
        CLUES9.filter { clueLive9(state, it) }.map { it.id }.toSet()

    /** Live pieces tagged to one site — the only currency
     *  a call can be bought with. */
    fun liveEvidence9(state: GameState, s: Site9): Int =
        CLUES9.count { it.site == s.index && clueLive9(state, it) }

    /** Grant a piece of evidence exactly once. The ticker
     *  + the town log hear about it; the board keeps it
     *  forever. A locked piece announces its own
     *  unreadability — honestly, at the moment of finding,
     *  so no player ever mistakes holding for knowing. */
    fun grantClue9(state: GameState, clueId: String): Boolean {
        val clue = CLUE9_BY_ID[clueId] ?: return false
        if (clueId in state.mystery9Clues) return false
        state.mystery9Clues.add(clueId)
        val live = clueLive9(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery9Clues.size}/${CLUES9.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery9Clues.size}/${CLUES9.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "coming, and in this case context is a kind of paperwork.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // ---------------- discovery: witnesses, looks, hands ----------------

    /** Talk-to discovery: if [npcName] is a live witness
     *  right now, their line — and the evidence it grants. */
    fun witnessLine9(state: GameState, npcName: String): Line? {
        val clue = CLUES9.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable9(state, clue)) return null
        if (!grantClue9(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId]
     *  reports the first time it finds each piece of
     *  evidence there — one at a time, in the order the
     *  case has opened them. A locked piece found early
     *  reports its inert face, and only its inert face:
     *  what it IS, never what it means. */
    fun lookClueText9(state: GameState, locId: String): String? {
        val clue = CLUES9.firstOrNull {
            it.kind == Kind9.LOOK && it.locId == locId &&
                it.id !in state.mystery9Clues &&
                clueAvailable9(state, it)
        } ?: return null
        if (!grantClue9(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation9(state: GameState, defId: String) {
        val clue = CLUES9.firstOrNull {
            it.kind == Kind9.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable9(state, clue)) return
        if (grantClue9(state, clue.id)) {
            state.events.add("🧩 Up close, something clicks: ${clue.title}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- THE LEDGER (derived, never stored) ----------------
    // The only stored facts are the calls themselves and
    // the evidence in hand. Every verdict below is a pure
    // function of those recorded inputs, recomputed on
    // every read — the graph discipline of the alibi case,
    // applied to a count: a book can't be argued with,
    // only re-read.

    /** A site's verdict, derived: OPEN (uncalled),
     *  COMMITTED (called, standing, untested — its
     *  settling piece is not live), PROVEN (called as the
     *  site actually ran, with the settling piece live in
     *  hand), or REFUTED (called against the site, with
     *  the settling piece live — the call stands struck
     *  and can be called again). A standing call cannot
     *  be re-called; a refuted one must be, before the
     *  ledger can settle. */
    fun siteVerdict9(state: GameState, s: Site9): String {
        val called = state.arc9Calls[s.id] ?: return "OPEN"
        if (s.proofBy !in liveClueIds9(state)) return "COMMITTED"
        return if (called == s.truth) "PROVEN" else "REFUTED"
    }

    fun calledCount9(state: GameState): Int =
        SITES9.count { it.id in state.arc9Calls }

    fun provenCount9(state: GameState): Int =
        SITES9.count { siteVerdict9(state, it) == "PROVEN" }

    fun refutedCount9(state: GameState): Int =
        SITES9.count { siteVerdict9(state, it) == "REFUTED" }

    /** Every site has a call written on it — standing,
     *  proven, or even refuted. (The finale's own gates
     *  read deeper than completeness; see auditDue and
     *  chooseFinale9.) */
    fun ledgerCalled9(state: GameState): Boolean =
        SITES9.all { it.id in state.arc9Calls }

    /** No call on the ledger stands refuted — the state
     *  the hard call is gated on, with completeness and
     *  the two load-bearing sites proven. */
    fun ledgerClean9(state: GameState): Boolean =
        refutedCount9(state) == 0

    /** Whether [s] can be called (or re-called) right now:
     *  its chapter has begun, enough live evidence tagged
     *  to it is in hand, and either it stands uncalled or
     *  its call stands refuted. */
    fun siteCallable9(state: GameState, s: Site9): Boolean {
        if (state.arc9Retired) return false
        if (state.arc9Chapter <= s.minChapter) return false
        if (liveEvidence9(state, s) < s.callPrice) return false
        val v = siteVerdict9(state, s)
        return v == "OPEN" || v == "REFUTED"
    }

    /** The site the call dialog should offer next: the
     *  first uncalled site (in board order) whose evidence
     *  price is met — or, when nothing new can be called,
     *  the first REFUTED site, offered for re-calling.
     *  New calls come before revisions, the way new
     *  writing came before revision in the case before
     *  this one. */
    fun pendingSite9(state: GameState): Site9? {
        if (state.arc9Retired) return null
        for (s in SITES9) {
            if (s.id !in state.arc9Calls && siteCallable9(state, s))
                return s
        }
        for (s in SITES9) {
            if (siteVerdict9(state, s) == "REFUTED" &&
                siteCallable9(state, s)) return s
        }
        return null
    }

    /** Call (or re-call, after a refutation) a site: the
     *  count is wrong, the record is wrong, or someone
     *  added one. The call is recorded; the verdict is
     *  NOT — it is derived on every read, so a call
     *  written in week two can be refuted in week four
     *  without anything being edited: the settling piece
     *  simply arrived, or finished becoming readable. */
    fun chooseCall9(state: GameState, siteId: String,
                    call: String): Boolean {
        if (call !in CALL_KEYS) return false
        val pending = pendingSite9(state) ?: return false
        if (pending.id != siteId) return false
        val revising = siteId in state.arc9Calls
        state.arc9Calls[siteId] = call
        val v = siteVerdict9(state, pending)
        state.events.add(when {
            revising ->
                "✒️ STRUCK AND RE-CALLED: '${pending.title}' — " +
                    "the ledger takes the new call. ($v)"
            v == "PROVEN" ->
                "✒️ CALLED — AND PROVEN ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand backs this call."
            v == "REFUTED" ->
                "✒️ CALLED — AND REFUTED ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand settles the site the " +
                    "other way. The call stands struck. " +
                    "It can be called again."
            else ->
                "✒️ CALLED: '${pending.title}' — the " +
                    "call stands, untested. Its settling " +
                    "piece hasn't surfaced, or hasn't become " +
                    "readable. Yet."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "✒️ Audit ledger: '${pending.title}' called ($v)")
        return true
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after
     *  the alibi case retires books chapter 1 ten days out
     *  from that retirement. While any earlier case is
     *  open this never fires — and never even books the
     *  date: the mystery slot has one case at a time.
     *  Chapters defer, day by day, past holiday episodes,
     *  like every case before them. */
    fun dueChapter9(state: GameState): ShenaniganDef? {
        if (state.arc9Retired) return null
        if (!state.arc8Retired) return null
        if (state.arc9Chapter >= CHAPTERS9.size) return null
        if (state.arc9DueAbsDay < 0) {
            state.arc9DueAbsDay = if (state.arc9Chapter == 0) {
                val anchor = if (state.arc8RetiredDay >= 0)
                    state.arc8RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc9DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS9[state.arc9Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted9(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf9(def.id)
        if (idx < 0) return
        state.arc9Started = true
        state.arc9Chapter = idx + 1
        state.arc9DueAbsDay = if (idx == CHAPTERS9.size - 1) -1
            else state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the reckoning ----------------

    /** The reckoning can be played: the closing is airing,
     *  its climax has begun, no call is locked yet, and
     *  the ledger is COMPLETE — every site called. The
     *  chamber waits on a finished ledger the way the
     *  reading waited on a finished graph: you cannot
     *  close a count for the room until you have closed
     *  it for yourself. */
    fun auditDue(state: GameState): Boolean {
        if (state.arc9Retired || state.arc9Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE9_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return ledgerCalled9(state)
    }

    /** Whether READ THE AUDIT ALOUD is available: the
     *  ledger's final derived state can carry it — the
     *  trophy case and the gallery stand PROVEN (the two
     *  load-bearing rows: the one true addition, and the
     *  man the book dropped), every site is called, no
     *  call stands refuted, and at least three sites
     *  stand proven. The gate reads the FINAL state of
     *  the ledger, never first impressions: a ledger with
     *  a struck call still standing on it buys nothing,
     *  and neither does a ledger the evidence never
     *  finished settling. */
    fun readEligible9(state: GameState): Boolean =
        siteVerdict9(state, SITES9[0]) == "PROVEN" &&
            siteVerdict9(state, SITES9[3]) == "PROVEN" &&
            ledgerCalled9(state) && ledgerClean9(state) &&
            provenCount9(state) >= 3

    /** Whether NAME THE ADDER is available: at least one
     *  site stands PROVEN — the town will follow a naming
     *  that has any settled row under it, and it is
     *  reaching for a name either way. */
    fun nameEligible9(state: GameState): Boolean =
        provenCount9(state) >= 1

    /** Lock the reckoning's call. The per-call gates are
     *  enforced here, in the module, not just in the
     *  dialog: a call the ledger can't carry is refused
     *  outright. */
    fun chooseFinale9(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!auditDue(state)) return false
        if (choice == "read" && !readEligible9(state)) return false
        if (choice == "name" && !nameEligible9(state)) return false
        state.arc9Choice = choice
        state.events.add(when (choice) {
            "read" ->
                "🗣️ You stand up in the chamber, ledger in " +
                    "hand, and you read the audit aloud — " +
                    "site by site, call by call, the way the " +
                    "evidence settled each one. The room " +
                    "hears its own count explained."
            "name" ->
                "⚖️ You give the room the name its arithmetic " +
                    "has been reaching for — the hand nearest " +
                    "the shelf, the keys, the workshop. The " +
                    "room takes it the way rooms take names: " +
                    "all at once, and for good."
            "stand" ->
                "🏛️ You let the books stand. Four counts, " +
                    "four extras, and a file that keeps both " +
                    "exactly as first written, stars and all — " +
                    "the audit closes the way it opened: " +
                    "unreconciled, on the record."
            else ->
                "🚶 You walk away. The program has the " +
                    "books, the board, the labeled space in " +
                    "the minutes. It doesn't have the ledger, " +
                    "and it doesn't have you."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class AuditOutcome(
        val branch: String,          // "reconciled" | "blamed" | "shelved" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, hand the wrap its
     *  words. Calls are re-gated here against the final
     *  ledger, exactly as locked — verdicts are monotone
     *  in the inputs that matter (evidence is never
     *  un-gathered, chapters never un-air), so a call that
     *  was lawful when locked is lawful now; anything else
     *  degrades to the cold landing, the discipline of
     *  every case before this one. A closing nobody played
     *  is cold by definition: the count, closed by
     *  applause and nearness. */
    fun resolveAudit(state: GameState): AuditOutcome {
        val chosen = state.arc9Choice
        val complete = ledgerCalled9(state)
        val branch = when {
            chosen == "read" && complete && readEligible9(state) -> "reconciled"
            chosen == "name" && complete && nameEligible9(state) -> "blamed"
            chosen == "stand" && complete -> "shelved"
            else -> DEFAULT_BRANCH
        }
        state.arc9Branch = branch
        state.arc9Retired = true
        state.arc9RetiredDay = state.absDay
        val held = clueCount9(state)
        return when (branch) {
            "reconciled" -> AuditOutcome(branch,
                "The audit, read aloud, adds up. Four " +
                    "extras, explained in a full chamber, " +
                    "from the books themselves: a chart that " +
                    "never followed its seat; a count that " +
                    "counted a tour; a roll that dropped a " +
                    "man between a draft and a fair copy; " +
                    "and a cup that came home the way " +
                    "institutions move things — in August, " +
                    "in daylight, in triplicate. Nothing in " +
                    "this case was added in the night. The " +
                    "red stars come down off the board. In " +
                    "their place, in the clerk's fair hand: " +
                    "COUNTED, AND RECONCILED.",
                listOf(
                    "🧮 THE RECONCILIATION: It is the " +
                        "strangest thing a verdict can be in " +
                        "this town: an audit that ends in " +
                        "apologies to furniture. The theater " +
                        "gets a new chart, drawn from the " +
                        "room. The bank's counters get a " +
                        "one-line method, read aloud: read " +
                        "the book to the back before you " +
                        "count its wall. The school keeps " +
                        "its cup — with a small card under " +
                        "it now, in the office's hand, saying " +
                        "where it was all those years, which " +
                        "was: filed wrong, by the county, " +
                        "within sight of home.",
                    "🧮 WHAT THE ROOM DOES WITH IT: Nobody " +
                        "apologizes; this is still South Park. " +
                        "The man in the third row stands up " +
                        "before anyone can thank him for " +
                        "sitting still, and leaves at a pace " +
                        "just under a statement. The cape in " +
                        "the hall goes home un-worn. And the " +
                        "gallery keeps its fourteenth face — " +
                        "with one correction outstanding, " +
                        "which the clerk has promised the " +
                        "room in writing: the roll will be " +
                        "reprinted whole, with every name " +
                        "the wall has been keeping for it."))
            "blamed" -> AuditOutcome(branch,
                "The room gets its name. The audit had a " +
                    "settled row in it — something really had " +
                    "been added, once, somewhere — and the " +
                    "chamber read that the only way chambers " +
                    "read additions: as a hand, near the " +
                    "shelf, with the keys. The name goes into " +
                    "the minutes spelled right, which the " +
                    "minutes do for everyone, whatever the " +
                    "minutes are worth. The shop teacher says " +
                    "his piece, sitting down, to the record. " +
                    "The record keeps everything in this " +
                    "town, including the objection. It just " +
                    "keeps it in a smaller hand.",
                listOf(
                    "🧮 THE NAME: The nearest desk to the " +
                        "shelf was the shop teacher's, and " +
                        "nearness, in a full room, is nine " +
                        "points of the law. The keys are his " +
                        "because the boilers are his. The " +
                        "lathe is his because the shop is. " +
                        "The case is his because the room " +
                        "needed a case with a person in it, " +
                        "and the audit's one true addition " +
                        "gave the room its permission.",
                    "🧮 WHAT THE ROOM DOES WITH IT: The " +
                        "board's stars come down — all four, " +
                        "though the ledger in one Journal " +
                        "says three of the four rows were " +
                        "never a hand's work at all. The " +
                        "gallery keeps its face, unexplained, " +
                        "which the Mayor calls next year's " +
                        "problem in the tone of a woman " +
                        "filing a storm. The cup stays on " +
                        "its shelf. Cups, seats, boxes and " +
                        "faces all stay exactly where they " +
                        "stood. Only the story moved."))
            "shelved" -> AuditOutcome(branch,
                "The books stand. The audit closes with " +
                    "its four stars converted, by vote of the " +
                    "room, into four asterisks: the counts " +
                    "keep their extras, the books keep their " +
                    "numbers, and the file that holds both " +
                    "goes into the drawer the town keeps for " +
                    "questions it has decided to outlive. " +
                    "In one Journal, the ledger says what " +
                    "each extra actually was, row by row, " +
                    "proven. The drawer does not subscribe " +
                    "to Journals.",
                listOf(
                    "🧮 THE FILING: It is not nothing, a " +
                        "drawer. The town has closed counts " +
                        "worse ways than by shelving them: by " +
                        "cape, by applause, by cape again. " +
                        "This time the file is complete — " +
                        "every site called, $held pieces of " +
                        "the count in one person's hands — " +
                        "and it is shelved complete, which " +
                        "means the truth of the four rooms " +
                        "now exists in exactly two places: " +
                        "the ledger, and the rooms themselves, " +
                        "standing there, being it.",
                    "🧮 WHAT THE ROOM DOES WITH IT: The " +
                        "Mayor gets her closing, which is the " +
                        "ending she ordered, and thanks the " +
                        "audit in the minutes for its service. " +
                        "The keeper of the wins list goes " +
                        "home past her shelf and its " +
                        "twenty-fourth cup and says nothing " +
                        "to it, the way you say nothing to a " +
                        "question you've stopped asking out " +
                        "loud."))
            else -> AuditOutcome(branch,
                "The count closes the way chambers close " +
                    "things without the person holding the " +
                    "whole ledger: by feel, by nearness, by " +
                    "applause. The labeled space in the " +
                    "program fills with the sound a room " +
                    "makes instead of an answer. The books " +
                    "go back to their rooms un-reconciled; " +
                    "the stars stay up on the board a while " +
                    "longer, fading, the way postings do; " +
                    "and four extra things go on standing " +
                    "in four public rooms, unexplained, " +
                    "unmoved, and — in the way of things " +
                    "nobody ever actually checks — slowly " +
                    "becoming part of the count.",
                listOf(
                    "🧮 THE UNCLOSED COUNT: There is a " +
                        "version of this file that adds up. " +
                        "It exists in one Journal, in one " +
                        "hand, finished or not. The town's " +
                        "version is shorter: something got " +
                        "added, somewhere, and the man with " +
                        "the keys sat very still while the " +
                        "room decided how it felt about " +
                        "that. Feelings, minuted, are a kind " +
                        "of record too. They are just not the " +
                        "kind you can audit.",
                    "🧮 WHAT THE ROOM DOES WITH IT: The " +
                        "cape stays in the hall. The third " +
                        "row empties without a word. And on " +
                        "the walk home, past the school, the " +
                        "theater, the bank, the gallery, " +
                        "every extra thing stands in the dark " +
                        "being exactly what it always was — " +
                        "which the ledger knows, and the " +
                        "night doesn't."))
        }
    }

    fun applyAuditEffects(state: GameState, branch: String) {
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
            "reconciled" -> {
                befriend("Adler", 4)
                befriend("Victoria", 2)
                befriend("McDaniels", 1)
                ripple("school_staff", 1,
                    "The Staff watched a full chamber hear the books read to their last pages — footnotes, forms, a shorthand page and all — and nobody named a teacher. The minutes have never been prouder, or quieter.")
                ripple("parents", -1,
                    "The Parents came for a naming and got a bookkeeping lesson instead. Half the Parents are re-telling it already. They're telling the cup part wrong, but they're telling it.")
                state.arc9ListPending = true
                state.arc9ListDay = state.absDay + LIST_RETURN_DELAY_DAYS
                state.lifeText["mark:CityHall"] =
                    "The mayors' gallery hangs its faces in " +
                        "a row the roll can finally describe — " +
                        "all but one line of it, which is " +
                        "promised, in writing, in the minutes: " +
                        "the roll will be reprinted whole. " +
                        "Under the board where the red stars " +
                        "were, in the clerk's fair hand: COUNTED, " +
                        "AND RECONCILED. The fourteenth face " +
                        "keeps his year. He has the expression " +
                        "of a man who expected exactly this " +
                        "wait."
                townLogLine(state, "🧮 RECONCILED: the audit " +
                    "was read at the Reckoning. No hand added " +
                    "anything in the night. The books were " +
                    "read to their last pages, in public, " +
                    "and the count added up.")
            }
            "blamed" -> {
                befriend("Adler", -4)
                befriend("McDaniels", 1)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents got their name at the Reckoning, before the adjournment, in the minutes, spelled right. The Parents are framing the program. The cape was not needed. The Parents call that a shame, a little, in private.")
                ripple("school_staff", -1,
                    "The Staff keeps the minutes of the naming in a hand slightly tighter than usual. One member of the Staff keeps a different record — the things he was actually handed, and by whom — and has started keeping it in writing. He's been handed a lot of things, over the years. None of them was a cup.")
                state.lifeText["mark:CityHall"] =
                    "The gallery keeps its fourteenth face, " +
                        "unexplained, and the audit's file is " +
                        "closed by name: THE ADDER, singular, " +
                        "in the minutes, in the clerk's fair " +
                        "hand, spelled right. In the school " +
                        "hallway the trophy case keeps its " +
                        "twenty-fourth cup. Counting the " +
                        "shelf was the case, and the case is " +
                        "closed, and closed things keep their " +
                        "shape."
                townLogLine(state, "🧮 THE NAME: the count " +
                    "was closed at the Reckoning, by name. " +
                    "The shop teacher's objection is not in " +
                    "the headline. It is in the record, " +
                    "sitting down, where it will stay.")
            }
            "shelved" -> {
                befriend("McDaniels", 2)
                befriend("Adler", -1)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents kept the books they arrived with, which are the books the Parents trust most: the ones already written. The pool on the Adder paid out to nobody. The Parents call that a fair result.")
                state.lifeText["mark:CityHall"] =
                    "By the gallery door hangs a small new " +
                        "plaque, in the clerk's fair hand: " +
                        "AUDIT FILED. The gallery keeps its " +
                        "faces, the shelf its cup, the theater " +
                        "its seat, the bank its shiny box — " +
                        "and the town keeps its favorite shape " +
                        "for a question: a drawer, labeled, in " +
                        "a basement, marked UNRECONCILED."
                townLogLine(state, "🧮 THE BOOKS STAND: four " +
                    "counts, four extras, no reading. The " +
                    "ledger stays proven where only its " +
                    "keeper can read it.")
            }
            else -> {
                befriend("Adler", -2)
                ripple("parents", -1,
                    "The Reckoning ran long and settled thin, and the Parents applauded in the places where an answer should have been. A few of them noticed, afterward, that the applause had a hole in it shaped exactly like a name.")
                state.lifeText["mark:CityHall"] =
                    "The results board still hangs in the " +
                        "hall with its four red stars fading — " +
                        "a count closed by a room that didn't " +
                        "have the ledger: by feel, by nearness, " +
                        "by the sound a chamber makes instead " +
                        "of an answer. The ledger of that count " +
                        "exists in exactly one Journal. It is " +
                        "not this building's copy. This " +
                        "building doesn't have one."
                townLogLine(state, "🧮 THE UNREAD LEDGER: the " +
                    "Reckoning happened without the audit. " +
                    "The count kept the shape it arrived " +
                    "with. The shape is wrong in a way only " +
                    "one person can prove, and that person " +
                    "wasn't in the room.")
            }
        }
    }

    // ---------------- the roll is reprinted (the RECONCILED landing's week after) ----------------

    /** The delayed landing of the reconciled reading:
     *  called daily by the engine; fires exactly once, a
     *  week after the audit was read — the mayors' roll is
     *  reprinted whole at last, the fourteenth name
     *  restored to the book the wall kept for it, and the
     *  gallery hangs complete: fourteen faces, fourteen
     *  names, the count and the record in agreement, in
     *  public, in print. */
    fun noteListRestored(state: GameState): Boolean {
        if (!state.arc9ListPending) return false
        if (state.absDay < state.arc9ListDay) return false
        state.arc9ListPending = false
        state.arc9ListRestored = true
        state.events.add("🧮 THE ROLL IS WHOLE: the clerk " +
            "unrolled the new mayors' roll in the gallery " +
            "this morning — reprinted entire, every name " +
            "the wall has, in the order the wall hangs " +
            "them. The fourteenth line sits in its year " +
            "between its neighbors, in the same ink as " +
            "everyone else's, with a small star beside it " +
            "and a footnote in the clerk's fair hand: " +
            "'restored from the minutes, the chair being " +
            "the chair.' School groups are already being " +
            "brought past it. It takes three minutes. It " +
            "is the only three minutes in the curriculum " +
            "about how records work.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:CityHall"] =
            "The mayors' gallery hangs complete: fourteen " +
                "faces, fourteen names, the roll reprinted " +
                "whole with the fourteenth line restored " +
                "from the minutes — 'the chair being the " +
                "chair' — in the same ink as everyone " +
                "else's. Under the board, the clerk's line " +
                "stays: COUNTED, AND RECONCILED. The school " +
                "reads the footnote to the new kids every " +
                "September. It takes three minutes."
        townLogLine(state, "🧮 THE ROLL IS WHOLE: fourteen " +
            "faces, fourteen names. The case stays closed — " +
            "in the right order, at every count it actually " +
            "happened.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "reconciled" to mapOf(
            "McDaniels" to listOf(
                "The roll is reprinted, the gallery is " +
                    "whole, and I will tell you the part I " +
                    "told the council in closed session: I " +
                    "wanted that face DOWN. It offended my " +
                    "hallway. It offended my arithmetic. And " +
                    "all the while the man had more right to " +
                    "his year than half the names I was " +
                    "defending — he'd simply had the " +
                    "misfortune to serve in a week the book " +
                    "found untidy. I keep the gallery now " +
                    "the way I should have kept it all " +
                    "along: by the wall first, and the roll " +
                    "second, and the difference between them " +
                    "treated as a question instead of an " +
                    "insolence. You're the only one who read " +
                    "it that way from the start. I noticed. " +
                    "Mayors notice noticing. It's most of " +
                    "the job.",
                "They bring the school groups past the " +
                    "footnote now — 'the chair being the " +
                    "chair' — and the children ask who he " +
                    "was, and the answer takes three minutes " +
                    "and involves a sick mayor, a borrowed " +
                    "week and a copyist's clean conscience. " +
                    "Three minutes, and every child in this " +
                    "town now knows what a fair copy is and " +
                    "what it costs. I couldn't have bought " +
                    "that lesson with a bond issue. An audit " +
                    "bought it with a footnote.")),
        "blamed" to mapOf(
            "Adler" to listOf(
                "For the record — since the record is " +
                    "what we keep — nobody handed me a cup. " +
                    "In thirty years I've been handed boilers, " +
                    "stage flats, a marquee ladder, two " +
                    "flooded classrooms and a piano with a " +
                    "raccoon position on the lid, and I have " +
                    "fixed or filed every one of them in " +
                    "writing. There is a work-order book in " +
                    "my shop going back decades, and the " +
                    "audit is welcome to it any time. It was " +
                    "welcome to it during the case, come to " +
                    "that. Nobody asked. The room had its " +
                    "arithmetic, and arithmetic, apparently, " +
                    "doesn't take questions. I'll be in the " +
                    "shop. It's where I was. It's in the book.",
                "Funny thing about the cup, since the town's " +
                    "so sure of its shapes: years back, the " +
                    "school lost a cup — a real one, off the " +
                    "list, dropped at a county meet — and the " +
                    "office asked me, quiet, if the shop " +
                    "couldn't just make one up to stand in " +
                    "its place until the real one turned up. " +
                    "I said no. A shelf of things you won " +
                    "isn't a shelf, if the shop can top it " +
                    "up. They found the real cup in a gym " +
                    "bag in a week. I think about that " +
                    "whenever the town tells its version. " +
                    "The town remembers the asking. It never " +
                    "did learn the refusing.")),
        "shelved" to mapOf(
            "Victoria" to listOf(
                "The file is in the drawer and the drawer " +
                    "is labeled, and I am told that is what " +
                    "closure looks like at City Hall. My list " +
                    "still says twenty-three. My shelf still " +
                    "says twenty-four. I pass both of them " +
                    "every day, and I have stopped arguing " +
                    "with the shelf, the way you stop arguing " +
                    "with weather — not because it's right, " +
                    "but because the argument is in a drawer " +
                    "now, and drawers outrank hallways. You " +
                    "read the whole of it, I think. You're " +
                    "the only one who did. It's a strange " +
                    "thing, being the keeper of a list in a " +
                    "town that has decided lists are " +
                    "furniture. Mind you keep yours.",
                "A girl asked me this week which cup is " +
                    "the extra one. I told her the truth the " +
                    "school can afford: that one of them has " +
                    "a longer story than the list has room " +
                    "for, and that stories longer than their " +
                    "records are not the same as lies. She " +
                    "considered this with the seriousness it " +
                    "deserved, which is more than the chamber " +
                    "managed. The children will be fine. The " +
                    "children read to the end of things.")),
        "cold" to mapOf(
            "Kenny" to listOf(
                "Mrrph mrph mmmph — mmph mrrm mrph! (A " +
                    "hundred and twenty-four! That's MY " +
                    "number and the audit never even called " +
                    "it wrong out loud, it just — stopped " +
                    "being the number, one day, when nobody " +
                    "was looking? The chart says a hundred " +
                    "and twenty, the counters say a hundred " +
                    "and twenty-one, and I say a hundred and " +
                    "twenty-four, and mine is the only one " +
                    "nobody wrote in a book, so mine's the " +
                    "one that doesn't exist anymore. That's " +
                    "how it works, I guess. A number needs a " +
                    "book. Write that down somewhere. In a " +
                    "book.)",
                "Mmph mrrm, mrph mrrph mmph. (The guy " +
                    "asleep across the two seats? He's in my " +
                    "count forever. Click, click. The books " +
                    "can close whatever they want — in the " +
                    "only count that has him in it, he's " +
                    "furniture, and he's load-bearing. " +
                    "Somebody should tell him. He's probably " +
                    "still there.)")))

    /** The ending's standing voice: in each landing, one
     *  person says the uncomfortable thing, forever, to
     *  anyone who'll stand still for it — the keeper who
     *  wanted the face down, the man the room named, the
     *  keeper whose file went in a drawer, the boy whose
     *  number never got a book. Null before the case
     *  closes, and for everyone else, always. */
    fun aftermathLine9(state: GameState, npcName: String): Line? {
        if (!state.arc9Retired) return null
        val bank = AFTERMATH_LINES[state.arc9Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The ninth case shows once it exists: chapter 1
     *  has aired, a piece of evidence is in hand, a site
     *  is called, or the case is closed. */
    fun boardVisible9(state: GameState): Boolean =
        state.arc9Started || state.mystery9Clues.isNotEmpty() ||
            state.arc9Calls.isNotEmpty() || state.arc9Retired

    fun statusStamp9(state: GameState): String = when {
        state.arc9Retired && state.arc9Branch == "reconciled" ->
            "🧮 RECONCILED — the count adds up; no hand added anything in the night"
        state.arc9Retired && state.arc9Branch == "blamed" ->
            "🧮 NAMED — the count was closed by name at the Reckoning"
        state.arc9Retired && state.arc9Branch == "shelved" ->
            "🧮 THE BOOKS STAND — four counts, four extras, one drawer"
        state.arc9Retired -> "🧊 COLD — the Reckoning happened without the ledger"
        state.shenaniganId == FINALE9_ID -> "⚖️ THE RECKONING — THE COUNT, CLOSED"
        state.arc9Started ->
            "🧮 OPEN — chapter ${state.arc9Chapter} of ${CHAPTERS9.size} aired; the audit is counting"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the
     *  case is — believed, as the town believes
     *  everything, in one breath and at applause speed. */
    fun currentTheory9(state: GameState): String = when {
        state.arc9Retired && state.arc9Branch == "reconciled" ->
            "CLOSED, and reconciled: the town now tells " +
                "the count story as a story about books — " +
                "a chart that lagged its room, a count that " +
                "counted everything it saw including the " +
                "tour, a roll that lost a man to a fair " +
                "copy, and a cup whose road home was made " +
                "of forms. It takes longer to tell this " +
                "way. Nobody minds, which around here is " +
                "basically a parade. The man with the keys " +
                "is 'the man in the third row' now, in the " +
                "town's mouth, which is not an apology, but " +
                "it rhymes with one."
        state.arc9Retired && state.arc9Branch == "blamed" ->
            "CLOSED, by name: the shop teacher, the keys, " +
                "the shelf, settled. The town tells it at " +
                "the speed of relief — an adder, a name, a " +
                "closed file — and only gets quiet in one " +
                "workshop in town, where a work-order book " +
                "going back decades sits open to anyone " +
                "who asks, saying what its keeper was " +
                "actually handed, and when, and by whom. " +
                "Workshops keep what minutes drop. This " +
                "one will keep it for years."
        state.arc9Retired && state.arc9Branch == "shelved" ->
            "FILED, which the town calls closed: things " +
                "'appeared' in four public rooms, in a " +
                "window the file doesn't specify, and the " +
                "drawer says UNRECONCILED in capitals the " +
                "town has learned to read as a punchline. " +
                "In one Journal the count is reconciled " +
                "properly, site by site, and adds up. It " +
                "has exactly one reader. It had exactly one " +
                "all along."
        state.arc9Retired ->
            "DECIDED, in a room you weren't in: the " +
                "Reckoning ran on the board alone — four " +
                "stars and a room's sense of nearness — and " +
                "the count got the shape rooms give things " +
                "they cannot read: the nearest one. The " +
                "ledger in your Journal is the only " +
                "reconciled copy of that count in " +
                "existence. It is complete, or it isn't. " +
                "Either way it is unpublished, which in " +
                "this town is a tense of its own."
        !state.arc9Started ->
            "Nothing yet. The books say what they say, " +
                "the rooms hold what they hold, and four of " +
                "each are about to disagree by exactly one, " +
                "in public, with red stars. Give it a " +
                "stock-take."
        state.arc9Chapter <= 1 ->
            "Settled, at cape speed: somebody is adding " +
                "things — a cup here, a seat there, a face " +
                "in a hallway — and the only problem is the " +
                "small one that every addition in the case " +
                "was found standing in plain sight, dusty, " +
                "old, and utterly unsurprised to be counted. " +
                "The town is holding both halves of that " +
                "sentence without apparent discomfort. " +
                "Towns can hold anything, if you let them " +
                "hold it in one breath."
        state.arc9Chapter == 2 ->
            "The rooms have been recounted in person and " +
                "they hold: the extras are really there, " +
                "every one, in the flesh and the silver. " +
                "The town has begun, uneasily, to suspect " +
                "the paperwork rather than the people, which " +
                "for this town is a philosophical " +
                "breakthrough on the scale of plumbing. " +
                "Suspecting the paperwork doesn't name " +
                "anybody, though, and the town still wants " +
                "a name by the Reckoning. Wants are doing a " +
                "lot of the counting now."
        state.arc9Chapter == 3 ->
            "Voices are done and the books are open, and " +
                "the town's position has quietly split: the " +
                "people who have read to the last pages " +
                "think the count is guilty; the people who " +
                "only heard the numbers think a person is. " +
                "The ledger says both halves are asking " +
                "the wrong question. The ledger doesn't get " +
                "a vote. The ledger gets read, or it gets " +
                "shelved — next week, in the chamber, in " +
                "front of everyone."
        else ->
            "Everything countable has been counted twice " +
                "and everything readable is read except one " +
                "page, whose context arrived with the week, " +
                "the way contexts do. Tonight the room " +
                "hears one version of the count: the " +
                "board's, or the ledger's. The four extra " +
                "things stand in their rooms either way. " +
                "They have been the most patient witnesses " +
                "in the case, and the only ones that never " +
                "changed their number."
    }

    /** The Reckoning's published terms, in the board's
     *  own words — the forecast a player can plan by. */
    fun forecastLine9(state: GameState): String {
        val called = calledCount9(state)
        val proven = provenCount9(state)
        return "⚖️ THE RECKONING, AS IT STANDS: the closing " +
            "waits on a finished ledger — $called/${SITES9.size} sites " +
            "called, $proven standing proven. READ THE AUDIT " +
            "ALOUD needs the ledger's final state: THE TROPHY " +
            "CASE and THE MAYORS' GALLERY proven, every site " +
            "called, no call standing refuted, and at least " +
            "three sites proven. NAME THE ADDER needs any one " +
            "site proven. LET THE BOOKS STAND and WALK AWAY " +
            "need nothing but you. A refuted call can be struck " +
            "and called again; a standing one cannot. Nothing " +
            "moves the ledger but evidence, book by book, room " +
            "by room."
    }

    /** The ninth case's board as plain lines (the Journal
     *  renders them below the alibi case's; the suite
     *  reads them). THE LEDGER: four count-sites as rows,
     *  each carrying its two numbers and their derived
     *  difference, its evidence, its call, and its call's
     *  verdict — derived on the spot: the board never
     *  stores a verdict, it re-reads the books every time
     *  you open it. */
    fun boardLines9(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp9(state))
        out.add("🧮 THE COUNT: In stock-take week, by the " +
            "Mayor's order, the town counted its public " +
            "things against their books — and four counts " +
            "came back with one MORE than the record holds. " +
            "A cup the wins list doesn't know, on the " +
            "Elementary's shelf. A seat the chart doesn't " +
            "know, in the Bijou. A box the ledger doesn't " +
            "know, in the bank's wall. A face the roll " +
            "doesn't know, in the mayors' gallery. Nothing " +
            "was taken from anywhere. This board is for " +
            "the reconciling: every extra was born in one " +
            "of a count's three moving parts — the things, " +
            "the book, or the counting — and each site " +
            "waits for your call on which.")
        out.add("🧩 THE EVIDENCE — what you hold " +
            "(${clueCount9(state)}/${CLUES9.size})" +
            if (!state.arc9Retired &&
                CLUES9.any { it.id in state.mystery9Clues && !clueLive9(state, it) })
                " — some of it is in hand but not yet readable: " +
                    "its context hasn't arrived, and in this " +
                    "case context is a kind of paperwork" else "")
        out.add("🧮 THE LEDGER — four sites, four books, " +
            "four counts. The difference on each row is " +
            "derived from the two numbers, never stored: " +
            "the ledger keeps numbers, not conclusions.")
        for (s in SITES9) {
            val placeName = LOCATIONS[s.place]?.name ?: s.place
            out.add("— ${s.title} — at $placeName; the book " +
                "is kept by ${s.keeper}")
            out.add("• counted: ${s.counted} ${s.countedLabel} — " +
                "the book: ${s.record} ${s.recordLabel} — " +
                "the difference: +${discrepancy9(s)}")
            val held = CLUES9.filter { it.site == s.index &&
                it.id in state.mystery9Clues }
            if (held.isEmpty()) {
                out.add("• (no evidence yet — this row's " +
                    "pieces haven't surfaced)")
            }
            for (clue in held) {
                if (clueLive9(state, clue)) {
                    out.add("• ${clue.title} — ${clue.detail}")
                } else {
                    out.add("• 🔒 ${clue.title} — ${clue.inertDetail}")
                }
            }
            val live = liveEvidence9(state, s)
            val called = state.arc9Calls[s.id]
            if (called == null) {
                out.add(when {
                    state.arc9Chapter <= s.minChapter ->
                        "✏️ CALL: unwritten — this row's week " +
                            "hasn't come; the audit takes its " +
                            "sites in order, like its counts."
                    live >= s.callPrice ->
                        "✏️ CALL: unwritten — you hold $live " +
                            "live pieces for this row. It can " +
                            "be called."
                    else ->
                        "✏️ CALL: unwritten — " +
                            "$live/${s.callPrice} live pieces " +
                            "in hand. Inert evidence doesn't " +
                            "count: it hasn't become readable " +
                            "yet, meaning-wise."
                })
            } else {
                val text = when (called) {
                    "count" -> "THE COUNT IS WRONG — the " +
                        "counters miscounted; the room matches its book"
                    "record" -> "THE RECORD IS WRONG — the " +
                        "book under-wrote it; the thing was always honestly there"
                    else -> "SOMEONE ADDED ONE — a real extra " +
                        "thing was placed there"
                }
                val stamp = when (siteVerdict9(state, s)) {
                    "PROVEN" -> "✅ PROVEN — the settling piece " +
                        "in hand grades this call TRUE"
                    "REFUTED" -> "💔 REFUTED — the settling " +
                        "piece in hand settles this site the " +
                        "other way. The call stands struck; " +
                        "it can be struck and called again"
                    else -> "📌 COMMITTED — standing, untested; " +
                        "the settling piece hasn't surfaced, or " +
                        "hasn't become readable"
                }
                out.add("🖊️ $stamp: “$text”")
            }
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory9(state)}")
        if (!state.arc9Retired && state.arc9Started) {
            out.add(forecastLine9(state))
        }
        if (state.arc9Retired) {
            out.add(when (state.arc9Branch) {
                "reconciled" -> "🏁 The audit was read aloud: " +
                    "every site called, the load-bearing rows " +
                    "proven, no call left standing struck — " +
                    "and the count, explained in a full " +
                    "chamber from the books themselves, added " +
                    "up for the first time. No hand added " +
                    "anything in the night. The stars came " +
                    "down. The reconciliation went up in " +
                    "their place, in the same fair hand."
                "blamed" -> "🏁 The room got its name. One " +
                    "row of the ledger really was an addition, " +
                    "and the town read that the only way it " +
                    "reads additions: as a hand, near the " +
                    "shelf, with the keys. The objection is " +
                    "not on this board. It is in a workshop, " +
                    "in a book of work orders going back " +
                    "decades, where it will stay, saying what " +
                    "its keeper was actually handed."
                "shelved" -> "🏁 The books stood. The ledger " +
                    "was complete — every site called, its " +
                    "verdicts earned — and the room kept the " +
                    "books it arrived with anyway. What is " +
                    "proven stays proven, on this board, in " +
                    "full. What is filed stays filed. The two " +
                    "documents now disagree in a drawer, " +
                    "forever, politely."
                else -> "🏁 The Reckoning happened without " +
                    "the ledger's voice. ${clueCount9(state)} pieces " +
                        "of the count were in one person's " +
                        "hands that night. The room ran on the " +
                        "board alone, the way it did the first " +
                        "time. Rooms keep doing that until " +
                        "somebody reads them a ledger."
            })
            if (state.arc9Branch == "reconciled") {
                if (state.arc9ListPending) {
                    out.add("🧮 One thing is still out there: " +
                        "the roll's missing line, promised in " +
                        "writing in the minutes. Some mornings " +
                        "are worth walking past the gallery " +
                        "for.")
                } else if (state.arc9ListRestored) {
                    out.add("🧮 The roll is whole — fourteen " +
                        "faces, fourteen names, the line back " +
                        "in its year in the same ink as " +
                        "everyone else's, and for once nobody " +
                        "in town is demanding the restoring be " +
                        "explained. The file stays closed: " +
                        "this is what a closed file looks like " +
                        "when it's counted right.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS9 = mapOf(
        "reconciled" to listOf(
            "The count? Reconciled, read aloud, whole " +
                "chamber. Turns out three of the four extra " +
                "things were the books' fault and the fourth " +
                "came by post. Town's still adjusting to a " +
                "mystery with a paper trail.",
            "Heard the gallery's whole now — fourteen " +
                "faces, fourteen names? There was a mayor in " +
                "there the roll'd misplaced for decades. " +
                "Town reads his footnote to the school " +
                "groups. Three minutes, they say."),
        "blamed" to listOf(
            "The count's closed — named the shop teacher, " +
                "open and shut. Funny: he keeps a book of " +
                "everything he's ever been handed, in the " +
                "shop, open to anybody. Nobody's asked. " +
                "Ask him. Or don't. The book'll be there.",
            "The Reckoning? Oh, they named the man with " +
                "the keys. Nearest desk to the shelf, that's " +
                "the arithmetic the room used. There's " +
                "another arithmetic, in one Journal " +
                "somewhere, row by row. Journals keep " +
                "everything. It's the reading that's scarce."),
        "shelved" to listOf(
            "The count got filed, is the official story. " +
                "Four extra things, one drawer marked " +
                "UNRECONCILED. Town tells it as a ghost " +
                "story now, basically. The kids love it. The " +
                "kids' hallway has the cup in it. Funny town.",
            "There's a plaque by the gallery door bothers " +
                "people and nobody can say why. Small, " +
                "neat, printed-looking. Says AUDIT FILED. " +
                "It'll outlive the audit."),
        "cold" to listOf(
            "The Reckoning? Chamber was full. They closed " +
                "the count the usual way — thinnest story " +
                "loses. The ledger of it's in somebody's " +
                "Journal, complete as you like. Journals " +
                "keep everything.",
            "Some count at City Hall got decided by a " +
                "board with red stars on it, I hear. The " +
                "four extra things are all still standing " +
                "where they stood. Things are patient. " +
                "Longer the town waits, the more they look " +
                "like they belong. That's how counts drift, " +
                "probably."))

    /** A post-case ambient aside in the town's own voice —
     *  the ending the town lives with is the one it keeps
     *  telling, at every count it actually happened.
     *  Null until the case is closed. */
    fun ambientCallback9(state: GameState, rnd: Random): String? {
        if (!state.arc9Retired) return null
        val bank = CALLBACKS9[state.arc9Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

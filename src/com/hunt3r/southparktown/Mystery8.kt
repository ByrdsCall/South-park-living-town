package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery8 — the eighth case (v0.10.0f, Queue #10 #6):
// "Everybody Was Somewhere".
//
// The pond case was witness-driven, the booth case a
// compressed whodunit, the buried case an excavation, the
// gardener case followed acts of maintenance, the
// confession case was built of claims, the opinion case
// was a meter, and the backwards case was a timeline.
// This one's shape is an ALIBI GRAPH. On setup evening
// for Family Night, the Elementary's marquee — lettered
// that morning with the school's new slogan, REACH FOR
// THE STARS — is found re-lettered to read REACH FOR THE
// FART, and the final S is nowhere: not on the sign, not
// in the letter tray. The town computes a window between
// the sign crew going home and the doors opening, and
// assembles the seven people who were in or around the
// building in between. Every one of them produces an
// alibi. Every alibi is vouched for — honestly, by
// somebody who was there. And the sign got changed
// anyway, in a window when, by the town's own arithmetic,
// everybody was somewhere else.
//
// The board is the graph: seven alibis as nodes, and
// between them, four PAIRS of alibis that share an
// EVENT-SIGNATURE — a sound, a crossing, a ladder, a
// jingle — a detail two tellings hold in common. For
// each pair the player judges: ONE EVENT (the two
// tellings describe the same moment, seen from two
// sides) or TWO EVENTS (the tellings describe different
// moments that merely rhyme). Judgments are recorded;
// VERDICTS are derived, never stored — each pair has one
// piece of evidence that settles it, and a judgment
// standing with its settling piece live is PROVEN, a
// judgment standing against it is REFUTED (and can be
// judged again), anything else is COMMITTED, untested.
// The shape's trap is arithmetic, not dishonesty: if two
// verified alibis describe the same event, the evening
// has one fewer event in it than the town counted — and
// a window the town never computed opens where nobody
// was looking. One suspect's alibi collapses by MERGER:
// they told the truth, about a different moment than
// they thought.
//
// Evidence: 12 pieces — 5 witness tellings, 5 Look finds,
// 2 participation beats — three tagged to every pair.
// Two pieces are UNREADABLE when found: they go into the
// evidence set honestly inert (a faded log strip; a
// smudged book-drop slip) and become spendable only once
// the chapter that unlocks their context has begun. A
// piece that is not live settles nothing, proves
// nothing. The suite proves it.
//
// The finale is Family Night itself: the program pauses
// for "the matter of the sign", and the player makes one
// of four calls. READ THE GRAPH ALOUD (gated on the
// graph's final derived state: the buzzer pair and the
// ladder pair PROVEN, every pair judged, none refuted,
// at least three proven), NAME THE LIAR (the town's own
// answer — somebody's alibi fell, so somebody lied;
// gated on at least one pair PROVEN), LET THE COUNT
// STAND, or WALK AWAY. A program nobody plays is its
// own landing: cold. The game never admits a call was
// wrong; the vouched-for just never stop saying what
// they saw.
//
// The solution, in one breath, for the payoffs only: no
// alibi in the graph is a lie. The gym scoreboard buzzed
// its self-test once that evening, thirteen minutes
// before the hour Garrison took it for; detention ended
// early on an honest mistake, and Clyde — the town's
// answer — spent the minutes that matter returning an
// overdue library book at the drop in the school's far
// wing, where the machine stamped his slip. The ladder
// stood at the marquee twice: at setup, and again later,
// raised by the one person whose alibi covered the
// assumed window at both ends and was never checked
// against the stretch in between — Adler, who built the
// marquee's frame, lost the slogan vote to the stars,
// and re-lettered his opinion with the tray's own
// letters, pocketing the spare S without noticing. The
// jingle on the park side was a bicycle bell; the truck
// was on Main Street at its own minute. Every clock in
// the case is a little wrong, and every witness is
// exactly right, and those two facts are the same fact.
//
// Structure:
//  - CHAPTERS8: four full ShenaniganDefs on the same
//    episode machinery, outside the random pool,
//    resolving in SHENANIGAN_BY_ID like every case.
//  - Gating: the case queues into the mystery slot TEN
//    days after the backwards case retires. While any
//    earlier case is open this one does not even schedule.
//  - Retirement: every landing retires the arc forever.
//    The aligned landing alone gets the long tail: a
//    week on, the missing letter is back in its tray.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery8 {

    const val CASE_TITLE = "Everybody Was Somewhere"
    const val FINALE8_ID = "arc8_ch4_the_reading"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the backwards case retires

    val CHOICE_KEYS = listOf("read", "name", "stand", "walk")
    const val DEFAULT_BRANCH = "cold"
    val BRANCH_KEYS = listOf("aligned", "accused", "stood", "cold")

    const val LETTER_RETURN_DELAY_DAYS = 7

    /** A pair judgment, as the dialog and the save file
     *  name it: the two tellings describe ONE event, or
     *  TWO events. The verdict is never the player's to
     *  pick — the evidence grades the judgment. */
    val JUDGE_KEYS = listOf("same", "different")

    // ---------------- the four chapters ----------------
    // They air a week apart in the player's present: the
    // morning after (the sign, the seven names), the
    // vouches walked (every alibi checked where it was
    // given), the counting (every clock, log and book in
    // town read twice), and — in the finale — Family
    // Night, where the graph is read into the program.

    val CHAPTERS8: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc8_ch1_letters",
            title = "Letters Out of Order",
            cause = "It's setup evening for Family Night at " +
                "the Elementary, and the rehearsal audience is " +
                "already at the doors when Bebe Stevens, sign " +
                "crew, looks up at the school marquee — the one " +
                "she lettered herself that morning with the " +
                "school's brand-new slogan — and reads it the " +
                "way it reads now. Somebody has been up the " +
                "ladder since the crew went home. The sign " +
                "crew, the detention room, the setup crew, the " +
                "errand — seven people were in or around this " +
                "building in the stretch between, and Principal " +
                "Victoria is writing their names down in a " +
                "column, which is what she does instead of " +
                "shouting.",
            blurb = "The marquee said REACH FOR THE STARS at " +
                "breakfast. By the rehearsal doors it says " +
                "REACH FOR THE FART, and the last S is gone " +
                "entirely — not on the sign, not in the tray. " +
                "Seven people were around the school in the " +
                "stretch between. Every one of them can say " +
                "where they were. Every one of them has " +
                "somebody who'll swear to it. The town would " +
                "like to know how a thing can happen in a " +
                "window when everybody was somewhere else.",
            theme = QuestTheme("🔤", "#7a4a6e", "EVERYBODY WAS SOMEWHERE"),
            tintHex = "#1c1420",
            stages = listOf(
                ShenaniganStage("Kickoff: Read It Again",
                    "🔤 FAMILY NIGHT SETUP: The marquee over the " +
                        "front walk, in the amber light the school " +
                        "gets at this hour, lettered in the tall " +
                        "white letters the sign crew keeps in a " +
                        "felt-lined tray: REACH FOR THE FART. " +
                        "Underneath, in smaller letters the crew " +
                        "added at setup: TONIGHT 7 PM. The crowd " +
                        "at the rehearsal doors is doing what " +
                        "crowds do — reading it twice, then a " +
                        "third time, out loud, to be sure.",
                    "The slogan was REACH FOR THE STARS. It won " +
                        "a vote. There was a assembly about the " +
                        "vote. The tray in the office, counted " +
                        "twice by the crew at setup, is short " +
                        "exactly one letter tonight: the last S " +
                        "of STARS is not on the sign, not in the " +
                        "tray, not in the gutter anyone's checked. " +
                        "A letter is a small thing to build a " +
                        "case on. This case is built of smaller.",
                    "Read the marquee at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading the sign out loud, twice. Looking in the gutter for a letter.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff is reading the sign the way it reads everything: as a deadline with letters on it."))),
                ShenaniganStage("Escalation: The Column of Names",
                    "🔤 Principal Victoria has the rehearsal " +
                        "audience seated and the program held, and " +
                        "she is doing arithmetic in public. Between " +
                        "the sign crew going home and these doors " +
                        "opening, the school stood — as far as the " +
                        "town can tell — empty. Except it didn't: " +
                        "detention ran late in the west wing, the " +
                        "setup crew was at the gym door, the AV " +
                        "club signed out through the office, an " +
                        "errand went to Main Street and back. She " +
                        "writes seven names in a column. The column " +
                        "is very straight. Columns are.",
                    "Seven names, and by the end of the hour, " +
                        "seven accounts — and here is the thing " +
                        "nobody in the room can get past: every " +
                        "account comes with a second person in " +
                        "it. Nobody was alone. Nobody is ever " +
                        "alone in this town; it might be the town's " +
                        "one law. If every story checks, the sign " +
                        "changed itself — and the principal does " +
                        "not accept that stories check themselves " +
                        "into new letters.",
                    "Hear the seven accounts at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Giving an account. Being written down in a very straight column.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have read the sign three times and enjoyed it more each time. The Parents are not helping."))),
                ShenaniganStage("Climax: Everybody Was Somewhere",
                    "🔤 CLIMAX: Victoria reads the column aloud, " +
                        "and one by one the seven say where they " +
                        "were, and one by one the vouchers say it " +
                        "with them — the proctor and the detained, " +
                        "the fence and the gym door, the ladder " +
                        "and the sign-out book, the errand and " +
                        "its receipt. It is airtight. It is " +
                        "airtight the way a room is airtight when " +
                        "nobody has checked whether the windows " +
                        "are the same windows. The sign still says " +
                        "what it says, over all their heads.",
                    "Every alibi verified. Every voucher honest — " +
                        "you can tell; this town lies about " +
                        "dessert and fishing, not about where it " +
                        "stood. So the evening, as the town has " +
                        "counted it, cannot contain the act it " +
                        "plainly contains. Either the counting is " +
                        "wrong, or somebody is. The principal " +
                        "has already chosen which one she believes. " +
                        "The board in your Journal has opened a " +
                        "graph: seven alibis, and the details they " +
                        "share, drawn as lines between them.",
                    "Hold every account up to the light — and to each other.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Vouching. Being vouched for. Watching a column of names refuse to add up.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has started saying 'the matter of the sign' in the tone it uses for weather."))),
                ShenaniganStage("Resolution: A Name by Family Night",
                    "🔤 The rehearsal goes ahead, under the sign, " +
                        "because the show is the show. Victoria " +
                        "closes the evening with an announcement " +
                        "in her minutes voice: the matter of the " +
                        "sign will be settled at Family Night " +
                        "itself, in the program, before the concert " +
                        "— with a name, or with an explanation. " +
                        "Three weeks. The column of names is " +
                        "pinned above her desk, very straight, " +
                        "and the sign over the walk goes on saying " +
                        "what it says to everyone who arrives.",
                    "A deadline is a kind of gift: it means the " +
                        "town will hold still while you work. The " +
                        "graph is in your Journal — seven alibis, " +
                        "every one of them verified, and between " +
                        "some of them, details that match: a sound " +
                        "two people heard, a figure two people " +
                        "saw, an object two people touched, a " +
                        "jingle two streets heard. Matching " +
                        "details are where an evening keeps its " +
                        "real count. Start with the pairs.",
                    "Work the graph: which tellings are one event, and which are two?",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Bebe"),
            episodeTitles = listOf("Letters Out of Order", "The Column of Names"),
            coldOpen = listOf(
                Line("Victoria", "For the minutes — and I keep " +
                    "the minutes — the marquee was lettered at " +
                    "eight this morning, by the crew, with the " +
                    "slogan this school voted for, and it was " +
                    "read aloud at the rehearsal doors saying " +
                    "something else. Seven people were in or " +
                    "around this building in between. I have " +
                    "their accounts. I have their vouchers. What " +
                    "I do not have, yet, is an evening that adds " +
                    "up. I intend to have one by Family Night."),
                Line("Bebe", "I lettered that sign myself, at " +
                    "eight in the morning, letter by letter, and " +
                    "I counted the tray twice at setup because " +
                    "that's what you DO, and it was FULL, and " +
                    "now the whole town is reading my sign like " +
                    "it's a confession. Somebody used my ladder " +
                    "and my letters to write a review of my " +
                    "slogan, and everybody who could've done " +
                    "it was somewhere else. I checked. Twice."))),
        ShenaniganDef(
            id = "arc8_ch2_the_vouches",
            title = "Somewhere Else, Sworn",
            cause = "A week on, the column of names has been " +
                "walked, street by street and wing by wing: " +
                "every account in the graph re-told where it " +
                "was given, by the person who gave it, with " +
                "the voucher standing in the actual spot. The " +
                "town calls this checking. The accounts call " +
                "it Tuesday. Not one of them has moved an " +
                "inch — and the details that match between " +
                "them are starting to be the most interesting " +
                "thing in the case, because a matching detail " +
                "is a place where two evenings touch.",
            blurb = "The vouches hold. All of them. Detention " +
                "and its proctor, the fence and the gym door, " +
                "the ladder and the book, the errand and its " +
                "receipt — re-told in place, and not a word " +
                "out of line. If anything, the accounts agree " +
                "MORE than accounts should: whole details, " +
                "shared between people who never spoke that " +
                "evening. A sound heard through a wall. A " +
                "figure crossing a lot. Shared details are " +
                "either a conspiracy or a clock, and this " +
                "town has never once managed a conspiracy.",
            theme = QuestTheme("🧭", "#4a6e5a", "THE VOUCHES"),
            tintHex = "#121c16",
            stages = listOf(
                ShenaniganStage("Kickoff: Walk It Again",
                    "🧭 THE VOUCHES, WALKED: The detention room " +
                        "first, because it's closest: the west " +
                        "wing, the clock over the door, the wall " +
                        "it shares with the gym. Then the park " +
                        "fence, with its straight sightline across " +
                        "the lot to the gym door. Then the office, " +
                        "the counter, the sign-out book. Every " +
                        "spot in the graph gets stood in, by the " +
                        "people who stood there, saying it again.",
                    "Standing in a spot changes a story's " +
                        "temperature. Distances you assumed turn " +
                        "out to be twenty paces; walls you pictured " +
                        "turn out to be the thin kind, the kind a " +
                        "gym makes sounds through. The accounts " +
                        "don't change — that's the eerie part. " +
                        "Only the room around them does.",
                    "Walk the vouches: the detention room, the fence, the door, the office.",
                    routine = "cluster", clusterLoc = "KidsPark",
                    clusterActivity = "Standing in the exact spot. Saying it again, word for word.",
                    tone = "none",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff walks the vouches the way it walks fire drills: in order, out loud, counting heads."))),
                ShenaniganStage("Escalation: The Driver's Round",
                    "🧭 Ms. Crabtree drove the late bus that " +
                        "evening and walked the lot after with the " +
                        "crossing flag, and she has OPINIONS about " +
                        "being left out of the column. She was at " +
                        "the corner when she parked and at the pond " +
                        "path when she left, and she saw the parka " +
                        "kid at BOTH ends of her round, and she " +
                        "would like that in the minutes, in " +
                        "capitals, because a crossing seen twice " +
                        "is a crossing the town should be counting " +
                        "twice.",
                    "The driver is not a suspect; she's a " +
                        "witness with a flag, which makes her " +
                        "more dangerous. Her two sightings bracket " +
                        "the evening like bookends — and a bracket " +
                        "is not the book. What was between her " +
                        "sightings, and how many times, is a " +
                        "question the lot itself can answer. " +
                        "Gravel remembers what feet do.",
                    "Hear the driver's round — and check the lot she walked.",
                    routine = "cluster", clusterLoc = "KidsPark",
                    clusterActivity = "Holding a crossing flag at full volume. Seeing parka kids at both ends of everything.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents have adopted the driver's theory, whichever it is this hour. Volume is a kind of evidence, to the Parents."))),
                ShenaniganStage("Climax: Two Tellings of One Sound",
                    "🧭 CLIMAX: In the detention room, with the " +
                        "gym empty on the other side of the wall, " +
                        "the proctor tells it again: the buzzer " +
                        "went, the room emptied, that's the whole " +
                        "of it. Down the hall, the counselor who " +
                        "keeps the scoreboard tells HIS version: " +
                        "buzzers all day, plural, tests at lunch " +
                        "and the board's own habits in the evening. " +
                        "Both men are telling the truth. The two " +
                        "truths do not fit in the same evening — " +
                        "unless the evening is smaller than the " +
                        "town counted it.",
                    "A sound is not a time. A sound is a sound; " +
                        "the time is what tired people hang on it " +
                        "afterward, in good faith, in a column. " +
                        "The graph doesn't care what anybody " +
                        "believed — it cares which tellings share " +
                        "a moment. The buzzer pair can be judged " +
                        "now, if the pieces are in hand. Judged is " +
                        "not settled, though. Settling takes a " +
                        "piece that doesn't care about beliefs at " +
                        "all: a machine's memory, say. Machines " +
                        "keep the worst company and the best " +
                        "records.",
                    "Judge the pairs the evidence can carry — and hunt the settling pieces.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Telling it again in the room where it happened. Disagreeing about a sound, honestly.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff is discovering that it employs several clocks and they are not on speaking terms."))),
                ShenaniganStage("Resolution: The Count So Far",
                    "🧭 The week closes with the graph heavier " +
                        "and the column unchanged: seven alibis, " +
                        "all standing. Two of the pairs can be " +
                        "judged on what's in hand. The other two " +
                        "want the town's paper — the logs, the " +
                        "books, the counted things — and paper is " +
                        "next week's country. Victoria updates " +
                        "the column above her desk. She does not " +
                        "cross anything out. She adds, in the " +
                        "margin, a question mark in a hand so " +
                        "neat it looks printed.",
                    "A question mark in the margin is the " +
                        "principal's way of screaming. The case " +
                        "has moved exactly as far as voices can " +
                        "take it: everybody's story is true, and " +
                        "the stories don't fit. What fits stories " +
                        "together is never another story. It's " +
                        "the counted things. Next week, the town " +
                        "counts.",
                    "Bring the graph to the counted things.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Crabtree", "Stan"),
            episodeTitles = listOf("Somewhere Else, Sworn", "The Driver's Round"),
            coldOpen = listOf(
                Line("Crabtree", "SIT— stay seated, I'm not " +
                    "driving, force of habit. I parked that bus " +
                    "at the corner and I walked that lot with " +
                    "the flag when I left, and I saw the parka " +
                    "kid at BOTH ends, and NOBODY wrote it down " +
                    "because nobody asked the DRIVER. You want " +
                    "to know about crossings, baby, you ask the " +
                    "woman with the flag."),
                Line("Stan", "I keep telling it the same because " +
                    "it keeps BEING the same. I was at the fence. " +
                    "Heidi was at the door. We could see each " +
                    "other the whole time — that's the point of " +
                    "a vouch, right? The only thing that moved " +
                    "in that whole lot was Kenny, and he moved " +
                    "once. ...I think he moved once. The lot's " +
                    "big. Sound carries weird off the gym. " +
                    "Forget I said that last part."))),
        ShenaniganDef(
            id = "arc8_ch3_the_counting",
            title = "Checked and Double-Checked",
            cause = "The third week, the town counts. The " +
                "scoreboard's log, the library's stamp, the " +
                "sign-out book, the copy shop's till — every " +
                "machine and book that touched the evening " +
                "gets read, and read again, by people who did " +
                "not touch it the first time. The school is " +
                "also re-hanging the gym banner as a Family " +
                "Night rehearsal, with the ladder, and the " +
                "crew, and the whole choreography of setup " +
                "and cleanup — because the best way to check " +
                "an evening, the principal says, is to hold " +
                "it up against itself.",
            blurb = "Voices are done; now it's the counted " +
                "things. Some of them contradict the voices. " +
                "Some of them contradict EACH OTHER, which is " +
                "more interesting, because a book can't be " +
                "embarrassed and a till can't be loyal — when " +
                "counted things disagree, it's because they " +
                "were counting different evenings inside the " +
                "same one. The ladder gets folded and raised " +
                "and folded again, on schedule, by the boy who " +
                "did it the first time. Work the shift. Watch " +
                "the seams.",
            theme = QuestTheme("🧮", "#5a4a6e", "THE COUNTING"),
            tintHex = "#181422",
            stages = listOf(
                ShenaniganStage("Kickoff: The Counted Things",
                    "🧮 THE COUNTING: The district office lends " +
                        "the school its paper for a week: the " +
                        "scoreboard's log book, the library's " +
                        "stamp ledger, the copy shop's till roll, " +
                        "the sign-out book's own page, laid out " +
                        "on the long table like a hand of cards. " +
                        "Every one of them touched the evening. " +
                        "Not one of them agrees with another about " +
                        "what o'clock it was — because not one of " +
                        "them was set by the same hand, or sprang " +
                        "forward, or got wound this year.",
                    "A town's clocks are a census of its habits: " +
                        "the office clock wound weekly, the till " +
                        "in a timezone of its own, a phone that " +
                        "never sprang forward, a scoreboard that " +
                        "keeps its own counsel in twenty-four-hour " +
                        "digits nobody reads. The evening didn't " +
                        "happen at one time. It happened at seven " +
                        "or eight, laid over each other like " +
                        "sheets of tracing paper. The graph is " +
                        "how you line the sheets up.",
                    "Read the counted things at City Hall — twice each.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading logs aloud. Arguing with a till roll. Winding nothing, on principle.",
                    tone = "none",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has begun setting its watches by the office clock, which is a decision, not a solution."))),
                ShenaniganStage("Escalation: The Ladder, Rehearsed",
                    "🧮 At the school, the banner rehearsal runs " +
                        "the setup choreography end to end: ladder " +
                        "up, banner pinned, ladder folded, ladder " +
                        "to the gym wall, crew out. Jimmy folds it " +
                        "himself, the way he folded it that " +
                        "evening, and the hinge bites his glove " +
                        "again, in the same place, and he stares " +
                        "at the glove like it's a page he can " +
                        "finally read. A ladder is not up or down. " +
                        "A ladder is up, and down, and up — it " +
                        "depends entirely on when you ask it.",
                    "Some evidence is a thing you find. Some is " +
                        "a thing a person can finally say because " +
                        "their hands remembered it first. The " +
                        "ladder pair has its settling piece now, " +
                        "if you'll take a boy's glove for a " +
                        "document. The town takes less for more, " +
                        "every day, out loud. Take it.",
                    "Work the banner rehearsal at the Elementary — hands on the ladder.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Raising the ladder. Folding the ladder. Staring at a bitten glove.",
                    tone = "none", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents have started a small pool on the sign. The Parents would like it noted they are NEUTRAL."))),
                ShenaniganStage("Climax: Stretches and Seams",
                    "🧮 CLIMAX: The setup shift — the real one, " +
                        "the Family Night crew arriving early to " +
                        "be checked — and you work it, chair by " +
                        "chair, and you feel from inside how a " +
                        "vouch is built: one person's 'the whole " +
                        "stretch' hands off to another's, at a " +
                        "door, at a stair, at the kettle. Every " +
                        "alibi in the graph is a quilt of stretches " +
                        "like this one. Most quilts are warm. The " +
                        "graph's whole question is whether any of " +
                        "them has a seam where the evening shows " +
                        "through.",
                    "You cannot see a seam from outside a quilt; " +
                        "you have to have held it. Now you've " +
                        "held it. The last pairs can be judged, " +
                        "and most of the graph can be settled, on " +
                        "what the counted things say. One strip of " +
                        "paper still won't read — the scoreboard's " +
                        "own log, faded to a maybe — and one slip " +
                        "still won't name its owner. Their context " +
                        "is coming. Contexts do, the week they're " +
                        "needed.",
                    "Judge the pairs the counted things can settle.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Working the setup shift. Feeling for seams in the evening.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff has stopped asking when it happened and started asking what 'when' is even made of."))),
                ShenaniganStage("Resolution: The Night Before the Night",
                    "🧮 Family Night is tomorrow. The program " +
                        "is printed — extra copies, from the copy " +
                        "shop, in a box by the office door — and " +
                        "in the middle of it, in the principal's " +
                        "neat hand, an item that isn't music: THE " +
                        "MATTER OF THE SIGN. The gym will be full. " +
                        "The column will be read. And the graph — " +
                        "your graph, the only complete copy of " +
                        "the evening that exists — will either be " +
                        "read into that room, or it won't.",
                    "Everything checkable has been checked " +
                        "twice. What remains is the reading: an " +
                        "evening re-counted in front of the people " +
                        "who lived it at seven different o'clocks. " +
                        "Rooms notice when their count changes. " +
                        "Get the graph settled — every pair " +
                        "judged, the settling pieces live — and " +
                        "then be in the room, at the item, when " +
                        "the program reaches it.",
                    "Settle the graph. Then be at Family Night.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Mackey", "Jimmy"),
            episodeTitles = listOf("Checked and Double-Checked", "Stretches and Seams"),
            coldOpen = listOf(
                Line("Mackey", "Okay. Okay. I've got the " +
                    "scoreboard's log book here, m'kay, and the " +
                    "board's little printer strip, and I want it " +
                    "known I tested that board at lunch, " +
                    "personally, two buzzes, m'kay, because a " +
                    "scoreboard you don't test is a scoreboard " +
                    "that's lying to children. What the board " +
                    "did in the EVENING is the board's business. " +
                    "Boards keep secrets. It's in their manual. " +
                    "Probably."),
                Line("Jimmy", "I f-folded it. At cleanup. I keep " +
                    "saying it and people keep nodding like it's " +
                    "a weather report, but it's the wh-whole — " +
                    "wow, what a great audience — it's the whole " +
                    "thing, isn't it? The ladder wasn't UP and " +
                    "it wasn't DOWN. It was up, and then I folded " +
                    "it, and after that it's not my ladder " +
                    "anymore. After that it's whoever's ladder " +
                    "it was next."))),
        ShenaniganDef(
            id = "arc8_ch4_the_reading",
            title = "The Reading",
            cause = "Family Night. The gym is full, the " +
                "concert is tuned, the marquee over the walk " +
                "still says what it says — the crew voted to " +
                "leave it until the matter is settled, which " +
                "the program now reaches: item six, THE MATTER " +
                "OF THE SIGN, between the choir and the " +
                "awards. The column of names is on the podium. " +
                "The graph is in your Journal. The room is " +
                "about to hear one of them.",
            blurb = "This is the room where it gets decided — " +
                "not what happened, which happened already, in " +
                "an evening with more o'clocks than people — " +
                "but what the town will say happened, in the " +
                "minutes, forever. Read the graph aloud and " +
                "re-count the evening in public. Name the liar " +
                "the town's arithmetic demands. Let the count " +
                "stand. Or let the program move on without " +
                "you, the way programs do.",
            theme = QuestTheme("📖", "#8a6a2e", "THE READING"),
            tintHex = "#20180e",
            stages = listOf(
                ShenaniganStage("Kickoff: Item Six",
                    "📖 FAMILY NIGHT: The gym does its best " +
                        "impression of a theater: folding chairs, " +
                        "a banner hung straight this time, the " +
                        "smell of floor wax and program ink. " +
                        "Item six is in every program. People " +
                        "have been reading ahead to it all " +
                        "evening, the way you read ahead in a " +
                        "book you're pretending not to be " +
                        "reading. The seven are seated in a row " +
                        "that was not assigned and is not a " +
                        "coincidence.",
                    "The faded strip reads at last, in the " +
                        "gym's own light, with the log book open " +
                        "beside it. The smudged slip names its " +
                        "owner at last, in the librarian's hand. " +
                        "Pieces finish becoming evidence the way " +
                        "ice finishes becoming water: all at " +
                        "once, at the temperature of the room " +
                        "they're needed in. Settle what's left of " +
                        "the graph. The item is coming.",
                    "Settle the graph before item six is called.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading ahead in the program. Sitting in a row that is not a coincidence.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff is holding the program the way you hold a railing."))),
                ShenaniganStage("Escalation: The Column, Read Aloud",
                    "📖 Victoria reads the column aloud — seven " +
                        "names, seven accounts, seven vouchers — " +
                        "and the room hears its own evening back, " +
                        "whole and verified and impossible, and " +
                        "you can feel the room do the arithmetic " +
                        "it has been doing for three weeks and get " +
                        "the same sum it always gets: nobody. " +
                        "Nobody did it. The sign changed itself. " +
                        "The room does not like the sum. The room " +
                        "is looking, already, for the name it " +
                        "likes better than the sum — the name " +
                        "whose account wobbled, whose account " +
                        "needed a machine to rescue it.",
                    "An audience is a jury that has already had " +
                        "dinner. The principal is watching you — " +
                        "not the column, you — because the minutes " +
                        "have a space in them shaped like a " +
                        "person standing up. What's in the space " +
                        "is up to the graph, and the graph is up " +
                        "to you, and item six is next.",
                    "The room is ready. Item six is next.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Hearing your own evening read back. Looking for a name that fits the sum.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have put the pool on hold. Even the Parents can feel a room change its mind."))),
                ShenaniganStage("Climax: The Matter of the Sign",
                    "📖 CLIMAX: 'Item six,' says the principal, " +
                        "'the matter of the sign.' The choir " +
                        "rustles. The seven sit very straight. " +
                        "This is the moment the whole case has " +
                        "been a corridor to: the graph, read or " +
                        "unread, in a full gym, with the sign " +
                        "itself lit up over the walk outside like " +
                        "an exhibit that couldn't fit through the " +
                        "door.",
                    "Everything the evening actually was is in " +
                        "your Journal, in pairs: which tellings " +
                        "were one event wearing two descriptions, " +
                        "which were two events the town's ears " +
                        "filed as one — and what the corrected " +
                        "count does to the shape of the stretch " +
                        "nobody watched. Read it, and the room " +
                        "re-counts with you. Don't, and the room " +
                        "keeps the count it has, and the name it " +
                        "was already reaching for.",
                    "Make the call: read it, name it, let it stand, or walk.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Sitting very straight. Holding a program like a railing.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff will remember this item longer than the concert."))),
                ShenaniganStage("Resolution: What the Minutes Keep",
                    "📖 The concert happens. The awards happen. " +
                        "The banner, hung straight, holds. And " +
                        "in the office, on the long table, the " +
                        "counted things go back into their box, " +
                        "and the column of names comes down off " +
                        "the wall above the principal's desk — or " +
                        "stays up, which is also a kind of " +
                        "minutes. The marquee crew will re-letter " +
                        "the sign in the morning. What it says " +
                        "next is a different case. What the town " +
                        "says about this one is tonight's work, " +
                        "and it's done, and it keeps.",
                    "An evening, re-counted or not, becomes a " +
                        "story the town tells at the coffee line. " +
                        "You were the only person in town holding " +
                        "the whole graph. Whatever the room did " +
                        "with it, the graph stays in your Journal " +
                        "— seven alibis, four pairs, every verdict " +
                        "the evidence earned — the one complete " +
                        "copy of the evening, at every o'clock it " +
                        "actually happened.",
                    "The case is closed. The graph keeps.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Clyde"),
            episodeTitles = listOf("The Reading", "Item Six"),
            coldOpen = listOf(
                Line("Victoria", "Item six is in the program " +
                    "because I put it there, and I put it there " +
                    "because a school that cannot account for " +
                    "its own sign cannot account for much. " +
                    "Tonight the matter gets a name or it gets " +
                    "an explanation. I have kept a space in the " +
                    "minutes for both. I would prefer — the " +
                    "minutes would prefer — the explanation."),
                Line("Clyde", "I already know how it goes. The " +
                    "kid with detention, the kid who got let out " +
                    "'early', the kid whose story needs a machine " +
                    "to believe it. Everybody else got a person " +
                    "to vouch for them. I got a wall and a buzz " +
                    "and a library stamp. If the room wants a " +
                    "name tonight, it's going to be the one that " +
                    "rhymes with 'I told you so'. It always is.")))
    )

    val CHAPTER8_BY_ID: Map<String, ShenaniganDef> =
        CHAPTERS8.associateBy { it.id }

    fun chapterIndexOf8(id: String): Int =
        CHAPTERS8.indexOfFirst { it.id == id }

    fun isChapter8(id: String?): Boolean =
        id != null && id in CHAPTER8_BY_ID

    // ---------------- the graph's nodes: seven alibis ----------------
    // Every alibi in the graph is TRUE, and every voucher
    // is honest. That is not a spoiler; it is the premise
    // the town itself states in chapter 1, in public, with
    // the sign lit over its head. What the graph is FOR is
    // the count: which of these tellings share a moment,
    // and what the evening looks like once it's counted
    // properly. The board prints the alibis exactly as
    // given — claimed spans in the teller's own terms,
    // because the terms are where the evening hides.

    data class Alibi8(
        val id: String,
        /** The cast member whose account this is. */
        val suspect: String,
        /** Who vouches for them, as the town recorded it. */
        val voucher: String,
        /** Where the account places them (a LOCATIONS id). */
        val place: String,
        /** The account in one line, in the teller's terms. */
        val account: String,
        /** The shared detail this account carries, if any. */
        val signature: String
    )

    val ALIBIS8: List<Alibi8> = listOf(
        Alibi8("alibi_clyde", "Clyde", "Garrison, who proctored the room",
            "Elementary",
            "In detention in the west-wing room, the whole " +
                "stretch, until the buzzer let the room out — " +
                "then straight home, past nobody, because the " +
                "room let out into an empty hall.",
            "the buzzer, heard through the gym wall"),
        Alibi8("alibi_garrison", "Garrison", "Clyde, the only boy in the room",
            "Elementary",
            "Proctoring detention, the whole stretch, until " +
                "the buzzer went — the way it goes every day — " +
                "when he let the room out and locked up after " +
                "it, same as every day.",
            "the buzzer, heard through the gym wall"),
        Alibi8("alibi_stan", "Stan", "Heidi, across the lot at the gym door",
            "KidsPark",
            "At the park fence the whole stretch, waiting " +
                "out his sister's rehearsal, watching the lot: " +
                "a kid in an orange parka crossed it, and the " +
                "ice-cream jingle went past the park, and " +
                "nothing else moved.",
            "the crossing, seen from the fence; the jingle, heard at the park"),
        Alibi8("alibi_heidi", "Heidi", "Stan, across the lot at the fence",
            "Elementary",
            "At the gym door the whole stretch with the " +
                "setup crew's program box, and never away from " +
                "the door: a kid in an orange parka crossed " +
                "the lot, and she waved, and the parka waved " +
                "back.",
            "the crossing, seen from the gym door"),
        Alibi8("alibi_bebe", "Bebe", "Jimmy, sign crew, beside her at the ladder",
            "Elementary",
            "Sign crew at setup: steadied the ladder at the " +
                "marquee while the TONIGHT line went up, and " +
                "counted the letter tray full on the office " +
                "counter, twice, the way you do.",
            "the ladder, up at the marquee at setup"),
        Alibi8("alibi_tolkien", "Tolkien", "Bebe, who saw him sign out and go",
            "Elementary",
            "Out through the office at cleanup's end, past " +
                "the counter — the letter tray already short, " +
                "by his count — and past the gym wall, where " +
                "the ladder stood folded flat, and home.",
            "the ladder, folded flat by the gym wall"),
        Alibi8("alibi_adler", "Adler", "the copy shop on Main Street, by its till receipt",
            "MainStreet",
            "On the errand the whole stretch: extra programs " +
                "at the copy shop while the jingle went past " +
                "outside, then straight back to the gym before " +
                "the doors, programs in a box, box in hand.",
            "the jingle, heard on Main Street")
    )

    val ALIBI8_BY_ID: Map<String, Alibi8> = ALIBIS8.associateBy { it.id }

    // ---------------- the graph's edges: four pairs ----------------
    // A pair is two alibis holding one detail in common.
    // The player judges the pair — ONE EVENT or TWO — and
    // the pair's one settling piece grades the judgment:
    // PROVEN when the judgment matches the evening as it
    // actually ran, REFUTED when it stands against the
    // settling piece, COMMITTED while the piece is still
    // out there (or still unreadable). The truth of a pair
    // is a fact of the case, sealed in this file; the
    // verdict is a pure function of the judgment, the
    // evidence in hand, and the chapters begun — derived
    // on every read, never stored. A REFUTED judgment can
    // be struck and judged again; a standing one cannot.

    data class Pair8(
        val id: String,
        /** Board order: the buzzer, the crossing, the
         *  ladder, the jingle. */
        val index: Int,
        val title: String,
        val alibiA: String,
        val alibiB: String,
        /** The shared detail, as the board names it. */
        val signatureText: String,
        /** The evening as it actually ran (sealed). */
        val truthSame: Boolean,
        /** The one piece that settles this pair. */
        val proofBy: String,
        /** 0-based chapter that must have BEGUN before
         *  this pair can be judged. */
        val minChapter: Int,
        /** Live pieces tagged to this pair that a
         *  judgment costs. */
        val evidenceNeeded: Int = 2
    )

    val PAIRS8: List<Pair8> = listOf(
        Pair8("pair_buzzer", 0, "THE BUZZER",
            "alibi_clyde", "alibi_garrison",
            "One sound, heard through a shared wall, that " +
                "ended a detention and started an evening. " +
                "Two people hang their whole account on it — " +
                "and each of them is the other's voucher.",
            truthSame = true, proofBy = "scoreboard_log",
            minChapter = 1),
        Pair8("pair_crossing", 1, "THE CROSSING",
            "alibi_stan", "alibi_heidi",
            "A kid in an orange parka, crossing the school " +
                "lot, watched from the park fence and from " +
                "the gym door by two people who could see " +
                "each other the whole time.",
            truthSame = true, proofBy = "one_set_of_tracks",
            minChapter = 1),
        Pair8("pair_ladder", 2, "THE LADDER",
            "alibi_bebe", "alibi_tolkien",
            "The marquee's ladder: up against the sign in " +
                "one telling, folded flat by the gym wall in " +
                "another. The ladder is the only witness that " +
                "was at the sign itself, and ladders don't talk.",
            truthSame = false, proofBy = "ladder_folded",
            minChapter = 2),
        Pair8("pair_jingle", 3, "THE JINGLE",
            "alibi_stan", "alibi_adler",
            "A bright musical sound on two streets in the " +
                "same stretch of evening, filed by two honest " +
                "sets of ears as one ice-cream truck making " +
                "its round.",
            truthSame = false, proofBy = "bike_bell",
            minChapter = 2)
    )

    val PAIR8_BY_ID: Map<String, Pair8> = PAIRS8.associateBy { it.id }

    // ---------------- the evidence ----------------

    enum class Kind8 { WITNESS, LOOK, PARTICIPATION }

    data class Clue8(
        val id: String,
        val kind: Kind8,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        /** The pair (index) this piece is tagged to — the
         *  edge of the graph it speaks to, however late it
         *  is found. */
        val pair: Int,
        /** 0-based chapter that must have BEGUN for this
         *  piece to be LIVE — testimony-grade, spendable.
         *  Equals [minChapter] for ordinary pieces. For the
         *  two context-locked pieces it is a LATER chapter:
         *  the piece can be held, honestly inert, before the
         *  chapter that gives it its context. */
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

    val CLUES8: List<Clue8> = listOf(
        // ---------- pair 0: the buzzer ----------
        Clue8("marquee_crowd", Kind8.PARTICIPATION, 0, 0, 0,
            "The crowd under the marquee",
            "Up close under the sign, in the first hour, " +
                "you hear the buzzer told twice in two " +
                "minutes: once as the sound that let a " +
                "detention out, once as the scoreboard's " +
                "evening test — the same sound wearing two " +
                "stories, each sworn to by somebody who was " +
                "standing there. Nobody in the crowd notices " +
                "they've heard one sound described as two " +
                "different events. The crowd is not the " +
                "case's problem. The crowd is the case's mirror.",
            "The crowd under the marquee",
            chapterDefId = "arc8_ch1_letters"),
        Clue8("two_buzzers", Kind8.WITNESS, 1, 0, 1,
            "Buzzers, plural (Mackey)",
            "The counselor who keeps the scoreboard is " +
                "certain about buzzers: he tested the board " +
                "at lunch himself — twice, two buzzes, on " +
                "purpose — and the board 'has its habits' in " +
                "the evening besides. Told in full, his " +
                "account invites the town to hear the setup " +
                "evening as full of buzzers, one for every " +
                "telling in the graph. His lunch test is " +
                "real. What it proves about the evening is " +
                "the case's question, not his.",
            "Buzzers, plural (Mackey)",
            npc = "Mackey",
            spoken = "Okay. Okay, I want to be helpful here, " +
                "m'kay, because a scoreboard is a public " +
                "trust. I tested that board at lunch. " +
                "Personally. Two buzzes, m'kay — buzz, buzz — " +
                "because a board you don't test is a board " +
                "that's lying to children. Now, the EVENING: " +
                "the board has its habits, m'kay, it does its " +
                "little self-test, and detention heard " +
                "whatever it heard through a wall, and walls " +
                "are not counselors, m'kay, walls don't " +
                "explain anything to anybody. So if you're " +
                "asking me were there buzzers that day, the " +
                "answer is yes. Plural. Buzzers. Write that " +
                "down, m'kay. Plural."),
        Clue8("scoreboard_log", Kind8.LOOK, 1, 0, 3,
            "The scoreboard's test log, half-faded",
            "The strip, read at last in the gym's own light " +
                "with the log book open beside it: two " +
                "self-tests that day. Midday — the counselor's " +
                "lunch test, entered in his own hand in the " +
                "book. And the evening: the board tested " +
                "itself once, and the digits are not the hour " +
                "the detention room believed in — they stand " +
                "some minutes short of it, the way a sound " +
                "stands short of the hour tired people hang " +
                "on it. One buzz in the evening, heard " +
                "through a wall by two honest people, each " +
                "of whom hung the hour they expected on it. " +
                "The buzzer pair's two tellings are tellings " +
                "of this buzz. There was no other.",
            "A strip from the gym scoreboard's little " +
                "printer, kept in the log book's back pocket: " +
                "the board's self-test entries for the setup " +
                "day. Two entries — one at midday, one in " +
                "the evening — in the board's own " +
                "twenty-four-hour digits. The evening entry " +
                "is faded to a maybe: its digits could be " +
                "read two ways, and nobody in this town " +
                "reads scoreboard. It doesn't make sense " +
                "yet. It's prepared to wait for its context, " +
                "which is more patience than anyone else in " +
                "the case has shown.",
            locId = "Elementary",
            foundText = "🔍 The gym closet, under the spare " +
                "nets: the scoreboard's log book, seasons of " +
                "them, and in the back pocket of this year's, " +
                "the little printer strip for the setup day — " +
                "two test entries, the evening one faded to a " +
                "maybe. You copy nothing. You take the strip " +
                "itself, carefully, the way you'd take a " +
                "sleeping thing. It doesn't make sense yet. " +
                "It admits it, in fading ink."),
        // ---------- pair 1: the crossing ----------
        Clue8("guard_two_sightings", Kind8.WITNESS, 1, 1, 1,
            "The driver's two sightings",
            "The late-bus driver walked the lot with the " +
                "crossing flag that evening and is certain " +
                "she can improve the case: she saw a parka " +
                "kid at the street corner when she parked, " +
                "and 'again' at the pond path when she left, " +
                "and she wants both sightings in the minutes, " +
                "in capitals. Her sightings are real — a " +
                "parka at the corner early, a parka at the " +
                "path late. Whether either of them is the " +
                "crossing in the graph, and whether a kid at " +
                "the corner and a kid at the path adds up to " +
                "a kid crossing twice, is arithmetic, not " +
                "testimony — and the driver does not do " +
                "arithmetic. The driver does volume.",
            "The driver's two sightings",
            npc = "Crabtree",
            spoken = "STAY SEATED — I'm not driving, it's " +
                "force of habit, my voice does it on its own " +
                "now. LISTEN. I parked that bus at the corner " +
                "and I saw the parka kid, RIGHT THERE, plain " +
                "as a stop sign. And when I left, flag in " +
                "hand, I saw the parka kid AGAIN, down by " +
                "the pond path. TWO sightings. NOBODY wrote " +
                "it down because nobody ASKED THE DRIVER. " +
                "You want crossings counted in this town, " +
                "baby, you count the woman with the flag. " +
                "TWICE. Put it in capitals."),
        Clue8("kenny_crossing", Kind8.WITNESS, 1, 1, 1,
            "The crossing, from inside the parka",
            "The crossing itself, interviewed: muffled, " +
                "cheerful, and — translated by long town " +
                "practice — entirely clear on the points that " +
                "matter. He cut across the lot once, on his " +
                "way to the pond path, because his sister's " +
                "doll was waiting at the park shelter and the " +
                "way home goes past it. He waved at the gym " +
                "door; the gym door waved back. He did not " +
                "cross back. The pond way home doesn't " +
                "require it — and neither, it turns out, " +
                "does the case.",
            "The crossing, from inside the parka",
            npc = "Kenny",
            spoken = "(muffled) Mmph mmph rmph — mmph, " +
                "Karen's doll, mmph rmph pond path! Mmph " +
                "mmph waved, mmph waved back, rmph mmph. " +
                "Mmph? Mmph mmph mmph — ONCE, mmph. Mmph " +
                "rmph mmph home, mmph. (He holds up one " +
                "finger, mittened, with enormous emphasis, " +
                "in case the translation was in any doubt.)"),
        Clue8("one_set_of_tracks", Kind8.LOOK, 1, 1, 1,
            "One set of tracks in the gravel",
            "The lot's gravel strip, between the gym door " +
                "and the park fence, kept the evening better " +
                "than any person did: one set of small boot " +
                "prints crossing it, one direction, one kid's " +
                "weight, unhurried, with a pause in the " +
                "middle where somebody waved at somebody. " +
                "There is no second set and no return set. " +
                "Two watchers each saw a parka cross that lot " +
                "and the town counted two crossings — an out " +
                "and a back — because each watcher assumed " +
                "the other's sighting was a different leg of " +
                "the trip. The gravel says the trip had one " +
                "leg, and the waving happened in the middle " +
                "of it.",
            "One set of tracks in the gravel",
            locId = "Elementary",
            foundText = "🔍 The gravel strip between the gym " +
                "door and the park fence, in the low light, " +
                "raked by nobody since the setup evening: " +
                "one set of small boot prints across it, one " +
                "way, with a scuffed pause halfway where the " +
                "walker stopped and turned toward the door. " +
                "You walk it at the edge, not in it, the way " +
                "you've learned. One crossing. The lot kept " +
                "the count the town couldn't."),
        // ---------- pair 2: the ladder ----------
        Clue8("setup_photo", Kind8.LOOK, 1, 2, 1,
            "A setup photo, stamped by a lazy clock",
            "Bebe's phone photo of the setup: ladder up at " +
                "the marquee, banner going on, the letter " +
                "tray on the office counter with the crew " +
                "around it, everybody mid-task and nobody " +
                "posing. The phone stamps the photo with an " +
                "hour that would put the whole scene inside " +
                "the town's window — which would make the " +
                "folded-ladder telling the case's one lie. " +
                "The phone, asked, admits nothing; phones " +
                "don't. Its owner cheerfully admits the " +
                "rest: the clock in it has run an hour lazy " +
                "since spring, and she never fixed it, " +
                "because everybody she texts knows. The " +
                "photo is honest. Its timestamp is a rumor " +
                "the town has been treating as a witness.",
            "A setup photo, stamped by a lazy clock",
            locId = "Elementary",
            foundText = "🔍 Over the office counter, Bebe " +
                "unlocks her phone and turns it around: the " +
                "setup, caught mid-task — ladder up, banner " +
                "half-pinned, the tray on the counter, the " +
                "crew everywhere. The timestamp in the " +
                "corner says an hour that makes your stomach " +
                "drop. She watches your face and sighs the " +
                "sigh of a girl who has explained a clock " +
                "before. 'Spring,' she says. 'It never " +
                "sprang. Everybody knows.' Everybody did " +
                "not, in fact, know."),
        Clue8("ladder_folded", Kind8.WITNESS, 2, 2, 2,
            "The ladder, folded (Jimmy)",
            "The sign crew's other half, hands first: at " +
                "cleanup, when the TONIGHT line was on and " +
                "the banner crew called it done, Jimmy " +
                "folded the marquee ladder himself and " +
                "leaned it by the gym wall — he remembers " +
                "because the hinge bit his glove, and the " +
                "glove still has the bite. His telling sits " +
                "between the ladder pair's two tellings like " +
                "a hinge of its own: the ladder was up for " +
                "the crew at setup, flat for the boy who left " +
                "by the office after cleanup, and both of " +
                "them were describing the ladder they " +
                "actually saw.",
            "The ladder, folded (Jimmy)",
            npc = "Jimmy",
            spoken = "I f-folded it. At cleanup. Wow, what " +
                "a great audience — no, I'm serious, this is " +
                "the p-part nobody writes down. Bebe's ladder " +
                "was UP, at setup, she's right, I'd swear it " +
                "on the banner. And then the TONIGHT line " +
                "was on, and cleanup came, and I took it down " +
                "and f-folded it and put it by the gym wall, " +
                "and the hinge b-bit my glove — see? Still " +
                "got the bite. So when Tolkien says it was " +
                "flat, he's right TOO, he just saw it AFTER " +
                "me. The ladder wasn't up and it wasn't down. " +
                "It was up, and then it was mine, and then " +
                "it was flat. After that it's not my ladder " +
                "anymore. After that it's whoever's ladder " +
                "it was n-next."),
        Clue8("library_slip", Kind8.LOOK, 2, 2, 3,
            "A book-drop slip, its owner smudged",
            "The slip, matched at last against the " +
                "librarian's ledger in Family Night's light: " +
                "the book is the detention room's own overdue " +
                "copy — the very book its reader was kept " +
                "late over — and the drop that stamped the " +
                "slip stands in the school's far wing, at the " +
                "end of a route that never passes the gym " +
                "door's sightline and never goes near the " +
                "marquee. The stamp's minute sits squarely " +
                "in the stretch the town's count left " +
                "unwatched. The alibi that collapsed in the " +
                "counting has an address after all, and the " +
                "address is in a machine's handwriting: its " +
                "owner was returning a book, at the far end " +
                "of the building, while the front of the " +
                "building was being re-lettered.",
            "A library book-drop slip from the setup " +
                "evening, machine-stamped with the drop's " +
                "time: a book came home to the school " +
                "library that evening, while the building " +
                "was supposedly standing empty between " +
                "crews. The borrower's line is a smear — a " +
                "number, maybe, or a thumb. A slip proves a " +
                "book was carried somewhere, by somebody. " +
                "Whose hands, and why it matters, it won't " +
                "say. It doesn't make sense yet. It's " +
                "prepared to wait for the librarian.",
            locId = "Elementary",
            foundText = "🔍 The library's return bin, emptied " +
                "late and logged later: one book, and under " +
                "it the drop's stamped slip for the setup " +
                "evening, the borrower's line smeared to a " +
                "thumbprint. A book came back that night, " +
                "at a stamped minute, carried by somebody " +
                "the stamp doesn't name. You keep the slip " +
                "flat in your palm like a moth. It doesn't " +
                "make sense yet. Slips rarely do, until the " +
                "ledger gets a vote."),
        // ---------- pair 3: the jingle ----------
        Clue8("two_streets", Kind8.WITNESS, 2, 3, 2,
            "Two streets, one jingle (Nichole)",
            "A phone call is the witness stand here: " +
                "Nichole was talking to her cousin across " +
                "town that evening, and both of them heard " +
                "the jingle — in the same breath of the same " +
                "call, she swears — which would make it one " +
                "sound, one minute, one truck heard from two " +
                "streets. She is telling the exact truth " +
                "about the call. What a call does to minutes " +
                "at two ends of a town — how it irons them " +
                "flat and calls them simultaneous — is a " +
                "question about phones, not about her, and " +
                "the cousin, asked separately, heard it from " +
                "a street with a truck on it. Nichole's " +
                "street had something else on it.",
            "Two streets, one jingle (Nichole)",
            npc = "Nichole",
            spoken = "We were ON THE PHONE. That's the part " +
                "nobody gets. My cousin's on her porch, I'm " +
                "on mine, we're mid-sentence — mid-SENTENCE — " +
                "and we both go quiet because there's the " +
                "jingle, hers and mine, same two notes, same " +
                "second. You don't have to believe me. " +
                "Believe the call log. Two streets, one " +
                "truck, one minute. That's not an opinion, " +
                "that's just — that's just what 'at the same " +
                "time' MEANS. …Isn't it? Why are you writing " +
                "like that. Stop writing like that."),
        Clue8("bike_bell", Kind8.LOOK, 2, 3, 2,
            "A bicycle bell, tuned like a jingle",
            "At the park shelter: a kid's bicycle, left " +
                "behind after the rehearsal crowd went home, " +
                "with a bright two-note bell that rings the " +
                "ice-cream truck's exact greeting — the " +
                "truck's jingle is a recording of a bell, and " +
                "bells, it turns out, are not rare and not " +
                "registered. Rung in the shelter it is " +
                "indistinguishable from a truck a street " +
                "away. One of the jingle pair's two hearings " +
                "was of this: a kid coasting the park path in " +
                "the dusk, ringing at nobody, on his way to " +
                "leave his bike and forget it. The other " +
                "hearing was the truck itself, on its own " +
                "street, at its own minute. Two bright " +
                "sounds — and the town's ears filed them as " +
                "one truck making its round.",
            "A bicycle bell, tuned like a jingle",
            locId = "KidsPark",
            foundText = "🔍 The park shelter, in the dusk " +
                "that's left: a kid's bicycle no one claimed " +
                "after the rehearsal, and on its handlebar a " +
                "bell so bright it looks polished. You ring " +
                "it once, guiltily, the way you knock on a " +
                "door you're not sure is yours — and the " +
                "ice-cream truck answers out of your own " +
                "hand, two notes, exact. The shelter keeps " +
                "the echo a second longer than the park " +
                "does. Oh, you think. Oh, that's the whole " +
                "— you stop thinking it out loud. The bell " +
                "doesn't mind. Bells never do."),
        Clue8("setup_shift", Kind8.PARTICIPATION, 2, 3, 2,
            "A shift on the setup crew",
            "Working the Family Night setup shift puts you " +
                "inside a vouch while it's being built: one " +
                "person's 'I was with them the whole stretch' " +
                "hands off — at a door, at a stair, at the " +
                "kettle — to another person's stretch, each " +
                "handoff invisible to the people making it, " +
                "each of them honest about the part they " +
                "held. Every alibi in the graph is a quilt of " +
                "stretches like these, sewn by people who " +
                "never saw the whole quilt. From inside, you " +
                "can feel what the graph has been saying all " +
                "along: the question was never whether the " +
                "stretches are real. It's whether they meet.",
            "A shift on the setup crew",
            chapterDefId = "arc8_ch3_the_counting")
    )

    val CLUE8_BY_ID: Map<String, Clue8> = CLUES8.associateBy { it.id }
    val CLUE8_IDS: Set<String> = CLUES8.map { it.id }.toSet()

    fun clueCount8(state: GameState): Int = state.mystery8Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable8(state: GameState, clue: Clue8): Boolean =
        !state.arc8Retired && state.arc8Chapter > clue.minChapter

    /** A piece is LIVE — spendable, settling — when it is
     *  in hand AND the chapter that unlocks its context
     *  has begun. Ordinary pieces are live the moment they
     *  exist. The two locked pieces sit in hand, honestly
     *  inert, until their context arrives: they settle
     *  nothing, prove nothing. Derived, never stored. */
    fun clueLive8(state: GameState, clue: Clue8): Boolean =
        clue.id in state.mystery8Clues &&
            state.arc8Chapter > clue.unlockChapter

    /** The ids of everything currently spendable. */
    fun liveClueIds8(state: GameState): Set<String> =
        CLUES8.filter { clueLive8(state, it) }.map { it.id }.toSet()

    /** Live pieces tagged to one pair — the only currency
     *  a judgment can be bought with. */
    fun liveEvidence8(state: GameState, p: Pair8): Int =
        CLUES8.count { it.pair == p.index && clueLive8(state, it) }

    /** Grant a piece of evidence exactly once. The ticker
     *  + the town log hear about it; the board keeps it
     *  forever. A locked piece announces its own
     *  unreadability — honestly, at the moment of finding,
     *  so no player ever mistakes holding for knowing. */
    fun grantClue8(state: GameState, clueId: String): Boolean {
        val clue = CLUE8_BY_ID[clueId] ?: return false
        if (clueId in state.mystery8Clues) return false
        state.mystery8Clues.add(clueId)
        val live = clueLive8(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery8Clues.size}/${CLUES8.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery8Clues.size}/${CLUES8.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "coming, and in this case context is a kind of clock.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // ---------------- discovery: witnesses, looks, hands ----------------

    /** Talk-to discovery: if [npcName] is a live witness
     *  right now, their line — and the evidence it grants. */
    fun witnessLine8(state: GameState, npcName: String): Line? {
        val clue = CLUES8.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable8(state, clue)) return null
        if (!grantClue8(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId]
     *  reports the first time it finds each piece of
     *  evidence there — one at a time, in the order the
     *  case has opened them. A locked piece found early
     *  reports its inert face, and only its inert face:
     *  what it IS, never what it means. */
    fun lookClueText8(state: GameState, locId: String): String? {
        val clue = CLUES8.firstOrNull {
            it.kind == Kind8.LOOK && it.locId == locId &&
                it.id !in state.mystery8Clues &&
                clueAvailable8(state, it)
        } ?: return null
        if (!grantClue8(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation8(state: GameState, defId: String) {
        val clue = CLUES8.firstOrNull {
            it.kind == Kind8.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable8(state, clue)) return
        if (grantClue8(state, clue.id)) {
            state.events.add("🧩 Up close, something clicks: ${clue.title}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- THE ALIBI GRAPH (derived, never stored) ----------------
    // The only stored facts are the judgments themselves
    // and the evidence in hand. Every verdict below is a
    // pure function of those recorded inputs, recomputed
    // on every read — the timeline discipline of the
    // backwards case, applied to a count: an evening can't
    // be argued with, only re-counted.

    /** A pair's verdict, derived: OPEN (unjudged),
     *  COMMITTED (judged, standing, untested — its
     *  settling piece is not live), PROVEN (judged as the
     *  evening actually ran, with the settling piece live
     *  in hand), or REFUTED (judged against the evening,
     *  with the settling piece live — the judgment stands
     *  struck and can be judged again). A standing
     *  judgment cannot be re-judged; a refuted one must
     *  be, before the graph can settle. */
    fun pairVerdict8(state: GameState, p: Pair8): String {
        val judged = state.arc8Pairs[p.id] ?: return "OPEN"
        if (p.proofBy !in liveClueIds8(state)) return "COMMITTED"
        val truth = if (p.truthSame) "same" else "different"
        return if (judged == truth) "PROVEN" else "REFUTED"
    }

    fun judgedCount8(state: GameState): Int =
        PAIRS8.count { it.id in state.arc8Pairs }

    fun provenCount8(state: GameState): Int =
        PAIRS8.count { pairVerdict8(state, it) == "PROVEN" }

    fun refutedCount8(state: GameState): Int =
        PAIRS8.count { pairVerdict8(state, it) == "REFUTED" }

    /** Every pair has a judgment written on it — standing,
     *  proven, or even refuted. (The finale's own gates
     *  read deeper than completeness; see readingDue and
     *  chooseFinale8.) */
    fun graphJudged8(state: GameState): Boolean =
        PAIRS8.all { it.id in state.arc8Pairs }

    /** No judgment on the graph stands refuted — the state
     *  the hard call is gated on, with completeness and
     *  the two load-bearing pairs proven. */
    fun graphClean8(state: GameState): Boolean =
        refutedCount8(state) == 0

    /** Whether [p] can be judged (or re-judged) right now:
     *  its chapter has begun, enough live evidence tagged
     *  to it is in hand, and either it stands unjudged or
     *  its judgment stands refuted. */
    fun pairJudgeable8(state: GameState, p: Pair8): Boolean {
        if (state.arc8Retired) return false
        if (state.arc8Chapter <= p.minChapter) return false
        if (liveEvidence8(state, p) < p.evidenceNeeded) return false
        val v = pairVerdict8(state, p)
        return v == "OPEN" || v == "REFUTED"
    }

    /** The pair the judgment dialog should offer next: the
     *  first unjudged pair (in board order) whose evidence
     *  price is met — or, when nothing new can be judged,
     *  the first REFUTED pair, offered for re-judgment.
     *  New judgments come before revisions, the way new
     *  writing came before revision in the case before
     *  this one. */
    fun pendingPair8(state: GameState): Pair8? {
        if (state.arc8Retired) return null
        for (p in PAIRS8) {
            if (p.id !in state.arc8Pairs && pairJudgeable8(state, p))
                return p
        }
        for (p in PAIRS8) {
            if (pairVerdict8(state, p) == "REFUTED" &&
                pairJudgeable8(state, p)) return p
        }
        return null
    }

    /** Judge (or re-judge, after a refutation) a pair: the
     *  two tellings describe ONE event, or TWO. The
     *  judgment is recorded; the verdict is NOT — it is
     *  derived on every read, so a judgment written in
     *  week two can be refuted in week four without
     *  anything being edited: the settling piece simply
     *  arrived, or finished becoming readable. */
    fun choosePair8(state: GameState, pairId: String,
                    judgment: String): Boolean {
        if (judgment !in JUDGE_KEYS) return false
        val pending = pendingPair8(state) ?: return false
        if (pending.id != pairId) return false
        val revising = pairId in state.arc8Pairs
        state.arc8Pairs[pairId] = judgment
        val v = pairVerdict8(state, pending)
        state.events.add(when {
            revising ->
                "✒️ STRUCK AND RE-JUDGED: '${pending.title}' — " +
                    "the graph takes the new judgment. ($v)"
            v == "PROVEN" ->
                "✒️ JUDGED — AND PROVEN ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand backs this judgment."
            v == "REFUTED" ->
                "✒️ JUDGED — AND REFUTED ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand settles the pair the " +
                    "other way. The judgment stands struck. " +
                    "It can be judged again."
            else ->
                "✒️ JUDGED: '${pending.title}' — the " +
                    "judgment stands, untested. Its settling " +
                    "piece hasn't surfaced, or hasn't become " +
                    "readable. Yet."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "✒️ Alibi graph: '${pending.title}' judged ($v)")
        return true
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after
     *  the backwards case retires books chapter 1 ten days
     *  out from that retirement. While any earlier case is
     *  open this never fires — and never even books the
     *  date: the mystery slot has one case at a time.
     *  Chapters defer, day by day, past holiday episodes,
     *  like every case before them. */
    fun dueChapter8(state: GameState): ShenaniganDef? {
        if (state.arc8Retired) return null
        if (!state.arc7Retired) return null
        if (state.arc8Chapter >= CHAPTERS8.size) return null
        if (state.arc8DueAbsDay < 0) {
            state.arc8DueAbsDay = if (state.arc8Chapter == 0) {
                val anchor = if (state.arc7RetiredDay >= 0)
                    state.arc7RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc8DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS8[state.arc8Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted8(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf8(def.id)
        if (idx < 0) return
        state.arc8Started = true
        state.arc8Chapter = idx + 1
        state.arc8DueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the reading ----------------

    /** The reading can be played: Family Night is airing,
     *  its climax has begun, no call is locked yet, and
     *  the graph is COMPLETE — every pair judged. The
     *  program waits on a finished graph the way the
     *  homecoming waited on a finished timeline: you
     *  cannot re-count the evening for the room until you
     *  have counted it for yourself. */
    fun readingDue(state: GameState): Boolean {
        if (state.arc8Retired || state.arc8Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE8_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return graphJudged8(state)
    }

    /** Whether READ THE GRAPH ALOUD is available: the
     *  graph's final derived state can carry it — the
     *  buzzer pair and the ladder pair stand PROVEN (the
     *  two load-bearing edges: one merger, one split),
     *  every pair is judged, no judgment stands refuted,
     *  and at least three pairs stand proven. The gate
     *  reads the FINAL state of the graph, never first
     *  impressions: a graph with a struck judgment still
     *  standing on it buys nothing, and neither does a
     *  graph the evidence never finished settling. */
    fun readEligible8(state: GameState): Boolean =
        pairVerdict8(state, PAIRS8[0]) == "PROVEN" &&
            pairVerdict8(state, PAIRS8[2]) == "PROVEN" &&
            graphJudged8(state) && graphClean8(state) &&
            provenCount8(state) >= 3

    /** Whether NAME THE LIAR is available: at least one
     *  pair stands PROVEN — the town will follow a naming
     *  that has any settled edge under it, and it is
     *  reaching for a name either way. */
    fun nameEligible8(state: GameState): Boolean =
        provenCount8(state) >= 1

    /** Lock the reading's call. The per-call gates are
     *  enforced here, in the module, not just in the
     *  dialog: a call the graph can't carry is refused
     *  outright. */
    fun chooseFinale8(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!readingDue(state)) return false
        if (choice == "read" && !readEligible8(state)) return false
        if (choice == "name" && !nameEligible8(state)) return false
        state.arc8Choice = choice
        state.events.add(when (choice) {
            "read" ->
                "🗣️ You stand up at item six, in front of " +
                    "the whole gym, and you read the graph " +
                    "aloud — pair by pair, verdict by verdict, " +
                    "the way the evening actually counted. " +
                    "The room hears its own night re-counted."
            "name" ->
                "⚖️ You give the room the name its arithmetic " +
                    "has been reaching for — the one whose " +
                    "account fell when the pairs were settled. " +
                    "The room takes it the way rooms take " +
                    "names: all at once, and for good."
            "stand" ->
                "🏛️ You let the count stand. Seven alibis, " +
                    "verified, and an evening that doesn't add " +
                    "up — and the minutes will keep both, the " +
                    "way they keep everything: exactly as " +
                    "first written."
            else ->
                "🚶 You walk away. The program has the " +
                    "column, the choir, the awards. It doesn't " +
                    "have the graph, and it doesn't have you."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class ReadingOutcome(
        val branch: String,          // "aligned" | "accused" | "stood" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, hand the wrap its
     *  words. Calls are re-gated here against the final
     *  graph, exactly as locked — verdicts are monotone in
     *  the inputs that matter (evidence is never
     *  un-gathered, chapters never un-air), so a call that
     *  was lawful when locked is lawful now; anything
     *  else degrades to the cold landing, the discipline
     *  of every case before this one. A program nobody
     *  played is cold by definition: the matter of the
     *  sign, settled by applause and adjacency. */
    fun resolveReading(state: GameState): ReadingOutcome {
        val chosen = state.arc8Choice
        val complete = graphJudged8(state)
        val branch = when {
            chosen == "read" && complete && readEligible8(state) -> "aligned"
            chosen == "name" && complete && nameEligible8(state) -> "accused"
            chosen == "stand" && complete -> "stood"
            else -> DEFAULT_BRANCH
        }
        state.arc8Branch = branch
        state.arc8Retired = true
        state.arc8RetiredDay = state.absDay
        val held = clueCount8(state)
        return when (branch) {
            "aligned" -> ReadingOutcome(branch,
                "The evening, re-counted, adds up. The graph " +
                    "was read into the minutes pair by pair: a " +
                    "buzzer that was one buzz, a crossing that " +
                    "was one crossing, a ladder that stood at " +
                    "the sign twice with a folding in between, " +
                    "a jingle that was two sounds wearing one " +
                    "name — and a stretch of evening, small " +
                    "and unwatched, that the town's count had " +
                    "never contained at all. Nobody in the " +
                    "graph lied. The column comes down off the " +
                    "wall. In its place, in the same neat hand: " +
                    "COUNTED TWICE, THEN COUNTED RIGHT.",
                listOf(
                    "🔤 THE RE-COUNT: It is the strangest thing " +
                        "a verdict can be in this town: an " +
                        "arithmetic correction. No villain was " +
                        "produced. A detention that ended on a " +
                        "sound got its minutes back; a boy who " +
                        "returned a library book at the far end " +
                        "of the building got his name back, in " +
                        "the minutes, in print. The sign is " +
                        "re-lettered in the morning by the crew " +
                        "that lettered it first — all nine " +
                        "letters present, counted twice, out " +
                        "loud, by two people.",
                    "🔤 WHAT THE ROOM DOES WITH IT: Nobody " +
                        "apologizes; this is still South Park. " +
                        "Garrison says 'a buzz is a buzz' to no " +
                        "one, twice. The errand's box of programs " +
                        "gets carried back to the copy shop by " +
                        "its owner, who tells the till, on his " +
                        "way out, that its clock is wrong — the " +
                        "first true thing anyone has said to " +
                        "that till in years. It keeps its own " +
                        "counsel. Machines in this case do."))
            "accused" -> ReadingOutcome(branch,
                "The room gets its name. The account that fell " +
                    "when the pairs were settled — the detention " +
                    "that couldn't have covered the stretch, the " +
                    "boy whose alibi needed a wall and a buzz " +
                    "and a stamp to stand up — is read into the " +
                    "minutes as the matter's answer, and the " +
                    "room settles the way rooms settle on a " +
                    "name: gratefully, and permanently. The " +
                    "sign is re-lettered by Friday. The program " +
                    "says SETTLED where item six was. Some " +
                    "questions in the graph were never asked " +
                    "again, which is not the same as answered.",
                listOf(
                    "🔤 THE NAME IN THE MINUTES: It fits the " +
                        "arithmetic the town already had: one " +
                        "alibi fell, so one liar stood there all " +
                        "along, behind it. The minutes are very " +
                        "straight about it. Columns are. The " +
                        "proctor who let the room out on that " +
                        "buzz stood up, in the silence after the " +
                        "naming, and said — to the room, to the " +
                        "minutes, to nobody who was writing it " +
                        "down — 'I was there. I know what I " +
                        "heard.' He is still saying it. It is in " +
                        "no record. It does not need to be.",
                    "🔤 THE SIGN: Re-lettered, spelled the way " +
                        "the vote decided, every letter counted. " +
                        "The tray is still short one. Nobody " +
                        "counts the tray anymore. Counting the " +
                        "tray was the case, and the case is " +
                        "closed, and the closed case's tray sits " +
                        "in the office with its empty slot like a " +
                        "held breath."))
            "stood" -> ReadingOutcome(branch,
                "The count stands. The graph was complete — " +
                    "every pair judged, the evening re-counted " +
                    "in one person's Journal — and the room kept " +
                    "the count it arrived with: seven alibis, " +
                    "one impossible evening, no name, no " +
                    "explanation, the matter tabled into the " +
                    "minutes as UNRESOLVED in the principal's " +
                    "neat capitals. The sign is re-lettered in " +
                    "the morning. The story of the sign stays " +
                    "exactly the shape it was when the doors " +
                    "opened: a thing that happened in a window " +
                    "when everybody was somewhere else.",
                listOf(
                    "🔤 THE COUNT, KEPT: Nothing was produced " +
                        "and nothing was withdrawn; the town " +
                        "simply kept its arithmetic, the way it " +
                        "keeps everything it can't file. The " +
                        "graph stays settled in your Journal — " +
                        "the pairs, the verdicts, the small " +
                        "unwatched stretch — the one complete " +
                        "copy of the evening, read by exactly " +
                        "one person, ever. The town prefers its " +
                        "copy. Its copy is shorter.",
                    "🔤 WHAT STAYS UP: The column comes down " +
                        "eventually — everything does — but the " +
                        "question mark in its margin stays in " +
                        "the principal's hand for other columns, " +
                        "other evenings. She writes it smaller " +
                        "now. She writes it oftener. Nobody " +
                        "who's seen it can say why it bothers " +
                        "them. It bothers them."))
            else -> ReadingOutcome(branch,
                if (chosen == "walk")
                    "You walked away, and item six went the " +
                        "way items go without the person holding " +
                        "the graph: the column read aloud, the " +
                        "room's arithmetic done at applause " +
                        "speed, the name it was already reaching " +
                        "for reached for and held up to general " +
                        "agreement — and the concert after it " +
                        "played to a room that had settled the " +
                        "matter the way it settles everything it " +
                        "cannot count: by deciding whose story " +
                        "was thinnest, and keeping that one."
                else
                    "Item six happened without you. The column " +
                        "was read; the room did its three-week " +
                        "arithmetic in one breath and got the sum " +
                        "it always gets; the matter of the sign " +
                        "was settled by the shape of the stories " +
                        "rather than their count — $held of " +
                        "${CLUES8.size} pieces of one person's " +
                        "evidence, none of it in the room — and " +
                        "the concert played on, under a banner " +
                        "hung straight, in a gym that had decided.",
                listOf(
                    "🔤 THE UNREAD GRAPH: A graph unread is " +
                        "just a list of where everybody was — " +
                        "which the town already had, in a " +
                        "column, very straight. The pairs stay " +
                        "judged or unjudged in your Journal, " +
                        "proven or unproven, in the only copy " +
                        "of the evening that counts it right. " +
                        "Copies that aren't read are archives. " +
                        "This town archives beautifully and " +
                        "reads at applause speed.",
                    "🔤 THE EMPTY CHAIR: In the row of seven, " +
                        "one seat was watched all evening by the " +
                        "boy at the park fence, who kept glancing " +
                        "at the door the way you glance at a " +
                        "clock you're not sure is running. He " +
                        "had stood in a spot and said his piece " +
                        "and watched a crossing, once. He " +
                        "thought somebody was going to say the " +
                        "once part out loud. The program moved " +
                        "on. Programs do."))
        }
    }

    /** The landing's ripples + its permanent marks — called
     *  by the wrap BEFORE the episode's credits are built,
     *  so the recap's faction-shift diff includes them. */
    fun applyReadingEffects(state: GameState, branch: String) {
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
            "aligned" -> {
                befriend("Clyde", 4)
                befriend("Garrison", 2)
                befriend("Victoria", 1)
                ripple("school_staff", 1,
                    "The Staff watched an evening get re-counted in a full gym, with the documents present, and nobody named a liar. The minutes have never been prouder, or quieter.")
                ripple("parents", -1,
                    "The Parents came for a naming and got an arithmetic lesson instead. Half the Parents are re-telling it already. They're telling the buzz part wrong, but they're telling it.")
                state.arc8LetterPending = true
                state.arc8LetterDay = state.absDay + LETTER_RETURN_DELAY_DAYS
                state.lifeText["mark:Elementary"] =
                    "The marquee reads the slogan again, all " +
                        "letters counted in public, twice — and " +
                        "under it, in the crew's smaller letters, " +
                        "a line that wasn't voted on and wasn't " +
                        "removed: COUNTED TWICE, THEN COUNTED " +
                        "RIGHT. The letter tray in the office is " +
                        "one slot short, still. Some slots stay " +
                        "open a while, like questions."
                townLogLine(state, "🔤 COUNTED RIGHT: the graph " +
                    "was read at Family Night. Nobody lied. The " +
                    "column came down; the count went up in " +
                    "its place.")
            }
            "accused" -> {
                befriend("Clyde", -4)
                befriend("Garrison", -2)
                befriend("Bebe", 1)
                ripple("parents", 1,
                    "The Parents got their name at item six, in the program, before the concert. The Parents are framing the program. The name is spelled right in it. That has to count for something, the Parents say.")
                ripple("school_staff", -1,
                    "The Staff keeps the minutes of the naming in a hand slightly tighter than usual. One member of the Staff keeps a different record, in his head, out loud, to anyone in the hall: he was there, and he knows what he heard.")
                state.lifeText["mark:Elementary"] =
                    "The marquee reads the slogan again, " +
                        "re-lettered by Friday, every letter " +
                        "counted. In the office, the letter " +
                        "tray's last slot stands empty, and " +
                        "nobody counts the tray anymore. Counting " +
                        "the tray was the case, and the case is " +
                        "closed, and closed things keep their " +
                        "shape."
                townLogLine(state, "🔤 THE NAME: the matter of " +
                    "the sign was settled at item six, by name. " +
                    "The proctor's objection is not in the " +
                    "minutes. It is in the hall, standing, " +
                    "where it will stay.")
            }
            "stood" -> {
                befriend("Victoria", 2)
                befriend("Clyde", -1)
                befriend("Bebe", 1)
                ripple("parents", 1,
                    "The Parents kept the count they arrived with, which is the count the Parents trust most: the one they already had. The pool on the sign paid out to nobody. The Parents call that a fair result.")
                state.lifeText["mark:Elementary"] =
                    "The marquee reads the slogan again, and " +
                        "the story of the other reading stays " +
                        "the town's favorite shape for a story: " +
                        "it happened in a window when everybody " +
                        "was somewhere else. The tray is full " +
                        "again. The tray was always going to be " +
                        "full again. What fills it is letters. " +
                        "What emptied it is a question the town " +
                        "keeps in a drawer marked UNRESOLVED."
                townLogLine(state, "🔤 THE COUNT STANDS: seven " +
                    "alibis, one impossible evening, no name. " +
                    "The graph stays settled where only its " +
                    "keeper can read it.")
            }
            else -> {
                befriend("Clyde", -2)
                ripple("parents", -1,
                    "Item six ran long and settled thin, and the Parents applauded in the places where a name should have been. A few of them noticed, afterward, that the applause had a hole in it shaped exactly like a name.")
                state.lifeText["mark:Elementary"] =
                    "The marquee reads the slogan again — " +
                        "re-lettered after a Family Night that " +
                        "settled the matter of the sign the way " +
                        "rooms settle things without the person " +
                        "holding the whole story: by feel, by " +
                        "adjacency, by applause. The graph of " +
                        "that evening exists in exactly one " +
                        "Journal. It is not this building's copy. " +
                        "This building doesn't have one."
                townLogLine(state, "🔤 THE UNREAD GRAPH: item six " +
                    "happened without the graph. The evening " +
                    "kept the count it arrived with. The count " +
                    "is wrong in a way only one person can " +
                    "prove, and that person wasn't in the room.")
            }
        }
    }

    // ---------------- the letter comes back (the ALIGNED landing's week after) ----------------

    /** The delayed landing of the aligned reading: called
     *  daily by the engine; fires exactly once, a week
     *  after the graph was read — the marquee's letter
     *  tray is full again one morning, the missing S back
     *  in its slot, dust-free, in a tray that sits in a
     *  locked office. Nobody saw it returned. Nobody, for
     *  once, is asking to have the returning explained. */
    fun noteLetterReturned(state: GameState): Boolean {
        if (!state.arc8LetterPending) return false
        if (state.absDay < state.arc8LetterDay) return false
        state.arc8LetterPending = false
        state.arc8LetterBack = true
        state.events.add("🔤 THE LETTER IS BACK: the office " +
            "clerk counted the marquee tray this morning — " +
            "twice, because counting is a habit in that " +
            "office now — and the last slot is full. The S " +
            "sits in its place, dust-free, in a tray that " +
            "lives in a locked office, in a school that " +
            "locks its doors. It was gone a month. It is " +
            "back the way it went: in a stretch nobody " +
            "watched, between two counts. The clerk wrote " +
            "it in the book and underlined it once, which " +
            "in that office is a paragraph.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:Elementary"] =
            "The marquee reads the slogan with all its " +
                "letters, and the tray in the office is full " +
                "— the last S back in its slot one morning, " +
                "unwitnessed, unexplained, and (for the first " +
                "time in the case's history) uninvestigated. " +
                "Under the slogan, the crew's unvoted line " +
                "stays: COUNTED TWICE, THEN COUNTED RIGHT. " +
                "The school reads it to the new kids every " +
                "September. It takes three minutes. It is " +
                "the only three minutes in the curriculum " +
                "about how counting works."
        townLogLine(state, "🔤 THE LETTER IS BACK: the tray " +
            "is full. The case stays closed — in the right " +
            "order, at every o'clock it actually happened.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "aligned" to mapOf(
            "Clyde" to listOf(
                "Everybody believes me now. I want you to know " +
                    "I noticed the order of operations there. " +
                    "When it was my word, it was 'the detention " +
                    "kid's story'. When it was a machine's stamp, " +
                    "it was 'the record, corrected, in the " +
                    "minutes'. I'm not mad. I'm — okay, I'm a " +
                    "little mad, but at the stamp, not at you. " +
                    "You were the only one who went and READ the " +
                    "stamp. Everyone else just needed it to " +
                    "exist. There's a difference. You know " +
                    "there's a difference. You're basically the " +
                    "only one who does.",
                "Detention's different now. Garrison lets us " +
                    "out by the clock over the door — he checks " +
                    "it twice, out loud, like a pilot. 'The " +
                    "clock,' he says, 'not the wall.' Kids think " +
                    "it's a bit. It's not a bit. Me and him, we " +
                    "hear a buzz through that wall and we both " +
                    "look at the clock now, at the same time, " +
                    "like — like a little ceremony. It's the " +
                    "worst club in school and it has two " +
                    "members and a motto.")),
        "accused" to mapOf(
            "Garrison" to listOf(
                "I was there. I want that understood — I was " +
                    "IN the room, I heard the buzz, I let him " +
                    "out the way I've let out eleven years of " +
                    "rooms, and I locked up after, and I walked " +
                    "past that sign on my way to my car, and it " +
                    "said what it said in the morning. I have " +
                    "never once, in eleven years — you can put " +
                    "in your little graph that I said it, since " +
                    "your graph is so good at holding what " +
                    "people said. I was there. I know what I " +
                    "heard. The wall knows what I heard. Ask " +
                    "the wall. It was the only other witness " +
                    "and nobody asked it either.",
                "They read his name out of YOUR graph. I want " +
                    "you to sit with the workmanship of that. " +
                    "The boy's account and my account were the " +
                    "same account — that's what a vouch IS — and " +
                    "the town took the pair of them and kept " +
                    "mine as the proctor's diligence and filed " +
                    "his as the lie. Same buzz. Same wall. Two " +
                    "drawers. I teach in that room, Detective. I " +
                    "will teach in it for years, with that buzz " +
                    "in the wall, and I will let the rooms out " +
                    "by the clock like a coward, and I will " +
                    "know why. That's my sentence. It doesn't " +
                    "rhyme with his, but it scans.")),
        "stood" to mapOf(
            "Bebe" to listOf(
                "The sign's fixed. Crew re-lettered it the " +
                    "next morning, I counted the tray — full, " +
                    "by the way, FULL, it got its letter back " +
                    "from wherever letters go — and the whole " +
                    "thing's a funny story now. 'The night the " +
                    "sign pranked itself.' That's the shape the " +
                    "town keeps. I keep a different shape. I " +
                    "keep the shape where my ladder had an " +
                    "afternoon and my tray had a hole in it and " +
                    "seven people were somewhere and the " +
                    "somewhere ADDED UP, in your book, and the " +
                    "room picked the joke instead. You were " +
                    "holding the only copy of the truth and " +
                    "you let them frame the joke. I counted " +
                    "that too.",
                "People ask me if I'm mad about the sign. " +
                    "I'm not mad about the sign; the sign was " +
                    "funny, I'm not made of wood. I'm mad about " +
                    "the counting. I count things twice — it's " +
                    "my whole personality, ask anyone — and I " +
                    "watched a whole gym decline to count ONE " +
                    "thing once, on purpose, in writing. The " +
                    "minutes say UNRESOLVED like it's a weather " +
                    "report. It wasn't weather. It was a choice. " +
                    "Yours is the name I file it under. No " +
                    "offense. Some offense.")),
        "cold" to mapOf(
            "Stan" to listOf(
                "I stood at that fence the whole stretch and " +
                    "I said so, in the column, in the program, " +
                    "to anyone who'd stand still. And at item " +
                    "six I kept looking at the door — your door, " +
                    "the gym door, the one Heidi stood at that " +
                    "whole other evening — because you were the " +
                    "one with the pairs, and the pairs were the " +
                    "only version where the crossing happened " +
                    "once. Kenny crossed ONCE. I said it at the " +
                    "fence and I'll say it at this one: once. " +
                    "The room never heard it. The room heard " +
                    "the column instead. Columns don't have " +
                    "crossings in them. Just names.",
                "You know what the worst part is? My alibi " +
                    "needed you too. Heidi saw me, I saw Heidi, " +
                    "the whole stretch — our vouch was the good " +
                    "one, the one with the sightline. And it " +
                    "still didn't matter, because a vouch is " +
                    "just two people agreeing on a stretch, and " +
                    "the room wasn't counting stretches that " +
                    "night. It was counting names. I learned " +
                    "something at item six. I hate what I " +
                    "learned at item six. That's how you can " +
                    "tell it was educational.")))

    /** The standing lines after Family Night: gratitude
     *  is not among them — every landing leaves somebody
     *  saying the uncomfortable thing, forever, to your
     *  face. The game never admits a call was wrong; the
     *  vouched-for just never stop knowing what they saw,
     *  and the spared never stop knowing what it cost.
     *  Null for everyone else. */
    fun aftermathLine8(state: GameState, npcName: String): Line? {
        if (!state.arc8Retired) return null
        val bank = AFTERMATH_LINES[state.arc8Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The eighth case shows once it exists: chapter 1
     *  has aired, a piece of evidence is in hand, a pair
     *  is judged, or the case is closed. */
    fun boardVisible8(state: GameState): Boolean =
        state.arc8Started || state.mystery8Clues.isNotEmpty() ||
            state.arc8Pairs.isNotEmpty() || state.arc8Retired

    fun statusStamp8(state: GameState): String = when {
        state.arc8Retired && state.arc8Branch == "aligned" ->
            "🔤 COUNTED RIGHT — the evening adds up; nobody lied"
        state.arc8Retired && state.arc8Branch == "accused" ->
            "🔤 NAMED — the matter was settled by name at item six"
        state.arc8Retired && state.arc8Branch == "stood" ->
            "🔤 THE COUNT STANDS — seven alibis, one impossible evening"
        state.arc8Retired -> "🧊 COLD — item six happened without the graph"
        state.shenaniganId == FINALE8_ID -> "📖 FAMILY NIGHT — ITEM SIX"
        state.arc8Started ->
            "🔤 OPEN — chapter ${state.arc8Chapter} of ${CHAPTERS8.size} aired; the graph is counting"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the case
     *  is — counted, as the town counts everything, in one
     *  breath and at applause speed. */
    fun currentTheory8(state: GameState): String = when {
        state.arc8Retired && state.arc8Branch == "aligned" ->
            "CLOSED, and re-counted: the town now tells the " +
                "sign story as a story about counting — an " +
                "evening that held more o'clocks than people, " +
                "told with the pairs in it, the way you'd tell " +
                "it with the graph open. It takes longer this " +
                "way. Nobody minds, which around here is " +
                "basically a parade. The detention kid is " +
                "'the library kid' now, in the town's mouth, " +
                "which is not an apology, but it rhymes with one."
        state.arc8Retired && state.arc8Branch == "accused" ->
            "CLOSED, by name: the detention kid, the fallen " +
                "alibi, the minutes, settled. The town tells it " +
                "at the speed of relief — a prank, a culprit, " +
                "a Friday re-lettering — and only gets quiet in " +
                "one hallway in town, where a proctor still " +
                "says he was there and knows what he heard, to " +
                "anyone, at any hour, unprompted. Hallways keep " +
                "what minutes drop. This one will keep it for years."
        state.arc8Retired && state.arc8Branch == "stood" ->
            "OPEN FOREVER, which the town calls closed: the " +
                "sign 'pranked itself', in a window when " +
                "everybody was somewhere else, and the file " +
                "says UNRESOLVED in capitals the town has " +
                "learned to read as a punchline. In one Journal " +
                "the evening is counted properly, pairs and " +
                "all, and adds up. It has exactly one reader. " +
                "It had exactly one all along."
        state.arc8Retired ->
            "DECIDED, in a room you weren't in: item six ran " +
                "on the column — seven names, seven accounts, " +
                "and a sum the room reached the way it reaches " +
                "everything it cannot count: by feel. The graph " +
                "in your Journal is the only re-counted copy of " +
                "that evening in existence. It is complete, or " +
                "it isn't. Either way it is unpublished, which " +
                "in this town is a tense of its own."
        !state.arc8Started ->
            "Nothing yet. The marquee says the slogan, the " +
                "tray is full, the scoreboard is dark, and " +
                "seven people are about to have an evening " +
                "each of them will describe honestly, forever, " +
                "to anyone who asks. Give it a setup."
        state.arc8Chapter <= 1 ->
            "Settled, at reading-it-three-times speed: a kid " +
                "did it — a ladder, a tray, a dare — and the " +
                "only problem is the small one that every kid " +
                "in town, and both adults, were somewhere else " +
                "at the time, with witnesses. The town is " +
                "holding both halves of that sentence without " +
                "apparent discomfort. Towns can hold anything, " +
                "if you let them hold it in one breath."
        state.arc8Chapter == 2 ->
            "The alibis have been walked and they hold — all " +
                "seven, in place, word for word. The town has " +
                "begun, uneasily, to suspect the evening rather " +
                "than the people in it, which for this town is " +
                "a philosophical breakthrough on the scale of " +
                "plumbing. Suspecting the evening doesn't name " +
                "anybody, though, and the town still wants a " +
                "name by Family Night. Wants are doing a lot " +
                "of the counting now."
        state.arc8Chapter == 3 ->
            "Voices are done and the counted things have had " +
                "their say, and the town's position has quietly " +
                "split: the people who stood in the spots think " +
                "the evening is guilty; the people who only " +
                "heard the stories think a person is. The graph " +
                "says both halves are asking the wrong " +
                "question. The graph doesn't get a vote. The " +
                "graph gets read, or it gets shelved — tomorrow " +
                "night, item six, in front of everyone."
        else ->
            "Everything checkable has been checked twice, " +
                "and the evening, counted properly, is a " +
                "different shape than the one in the column — " +
                "roomier in one place, tighter in others, " +
                "honest everywhere. Tonight the room hears one " +
                "version of it: the column's, or the graph's. " +
                "The sign over the walk is lit either way. It's " +
                "been the most patient witness in the case, " +
                "and the only one that never changed its story."
    }

    /** Family Night's published terms, in the board's own
     *  words — the forecast a player can plan by. */
    fun forecastLine8(state: GameState): String {
        val judged = judgedCount8(state)
        val proven = provenCount8(state)
        return "📖 ITEM SIX, AS IT STANDS: the reading waits " +
            "on a finished graph — $judged/${PAIRS8.size} pairs " +
            "judged, $proven standing proven. READ THE GRAPH " +
            "ALOUD needs the graph's final state: THE BUZZER " +
            "and THE LADDER proven, every pair judged, no " +
            "judgment standing refuted, and at least three " +
            "pairs proven. NAME THE LIAR needs any one pair " +
            "proven. LET THE COUNT STAND and WALK AWAY need " +
            "nothing but you. A refuted judgment can be struck " +
            "and judged again; a standing one cannot. Nothing " +
            "moves the graph but evidence, matched detail by " +
            "matched detail."
    }

    /** The eighth case's board as plain lines (the Journal
     *  renders them below the backwards case's; the suite
     *  reads them). THE ALIBI GRAPH: seven alibis as nodes,
     *  four pairs as edges, each edge carrying its shared
     *  signature, its evidence, its judgment, and its
     *  judgment's verdict — derived on the spot: the board
     *  never stores a verdict, it re-counts the evening
     *  every time you open it. */
    fun boardLines8(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp8(state))
        out.add("🔤 THE INCIDENT: On setup evening for Family " +
            "Night, the Elementary's marquee — lettered that " +
            "morning with the school's new slogan, REACH FOR " +
            "THE STARS — was found at the rehearsal doors " +
            "reading REACH FOR THE FART, and the final S was " +
            "nowhere: not on the sign, not in the tray. Seven " +
            "people were in or around the building in the " +
            "stretch between the sign crew going home and " +
            "the doors opening. Every one of them produced " +
            "an alibi, and every alibi was vouched for, " +
            "honestly, by somebody who was there. The sign " +
            "changed anyway. This board is for the counting: " +
            "if every alibi is true, the evening itself must " +
            "be miscounted — and an evening, unlike a person, " +
            "cannot object to being re-counted.")
        out.add("🧩 THE EVIDENCE — what you hold " +
            "(${clueCount8(state)}/${CLUES8.size})" +
            if (!state.arc8Retired &&
                CLUES8.any { it.id in state.mystery8Clues && !clueLive8(state, it) })
                " — some of it is in hand but not yet readable: " +
                    "its context hasn't arrived, and in this " +
                    "case context is a kind of clock" else "")
        out.add("🔗 THE ALIBIS — seven accounts, every one " +
            "vouched for. Read them the way they were given: " +
            "the terms are the teller's own.")
        for (a in ALIBIS8) {
            out.add("— ${a.suspect.uppercase()} — vouched by ${a.voucher}")
            out.add("• at ${LOCATIONS[a.place]?.name ?: a.place}: ${a.account}")
            out.add("• signature carried: ${a.signature}")
        }
        out.add("✒️ THE PAIRS — two tellings, one shared " +
            "detail. Judge each pair: did the two tellings " +
            "describe ONE EVENT, seen from two sides — or TWO " +
            "EVENTS the town's ears filed as one? Each pair " +
            "has one piece of evidence that settles it. The " +
            "settling grades the judgment, never the reverse.")
        for (p in PAIRS8) {
            val aA = ALIBI8_BY_ID[p.alibiA]
            val aB = ALIBI8_BY_ID[p.alibiB]
            out.add("— ${p.title} — ${aA?.suspect} ↔ ${aB?.suspect}")
            out.add("• the shared detail: ${p.signatureText}")
            val held = CLUES8.filter { it.pair == p.index &&
                it.id in state.mystery8Clues }
            if (held.isEmpty()) {
                out.add("• (no evidence yet — this pair's " +
                    "pieces haven't surfaced)")
            }
            for (clue in held) {
                if (clueLive8(state, clue)) {
                    out.add("• ${clue.title} — ${clue.detail}")
                } else {
                    out.add("• 🔒 ${clue.title} — ${clue.inertDetail}")
                }
            }
            val live = liveEvidence8(state, p)
            val judged = state.arc8Pairs[p.id]
            if (judged == null) {
                out.add(when {
                    state.arc8Chapter <= p.minChapter ->
                        "✏️ JUDGMENT: unwritten — this pair's " +
                            "week hasn't come; the case takes " +
                            "its pairs in order, like its evenings."
                    live >= p.evidenceNeeded ->
                        "✏️ JUDGMENT: unwritten — you hold $live " +
                            "live pieces for this pair. It can " +
                            "be judged."
                    else ->
                        "✏️ JUDGMENT: unwritten — " +
                            "$live/${p.evidenceNeeded} live pieces " +
                            "in hand. Inert evidence doesn't " +
                            "count: it hasn't become readable " +
                            "yet, meaning-wise."
                })
            } else {
                val text = if (judged == "same")
                    "ONE EVENT — the two tellings describe the same moment"
                else
                    "TWO EVENTS — the two tellings describe different moments"
                val stamp = when (pairVerdict8(state, p)) {
                    "PROVEN" -> "✅ PROVEN — the settling piece " +
                        "in hand grades this judgment TRUE"
                    "REFUTED" -> "💔 REFUTED — the settling " +
                        "piece in hand settles this pair the " +
                        "other way. The judgment stands struck; " +
                        "it can be struck and judged again"
                    else -> "📌 COMMITTED — standing, untested; " +
                        "the settling piece hasn't surfaced, or " +
                        "hasn't become readable"
                }
                out.add("🖊️ $stamp: “$text”")
            }
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory8(state)}")
        if (!state.arc8Retired && state.arc8Started) {
            out.add(forecastLine8(state))
        }
        if (state.arc8Retired) {
            out.add(when (state.arc8Branch) {
                "aligned" -> "🏁 The graph was read aloud: " +
                    "every pair judged, the load-bearing pairs " +
                    "proven, no judgment left standing struck — " +
                    "and the evening, re-counted in a full gym, " +
                    "added up for the first time. Nobody lied. " +
                    "The column came down. The count went up " +
                    "in its place, in the same neat hand."
                "accused" -> "🏁 The room got its name. One " +
                    "alibi in the graph had fallen, and the " +
                    "town read the falling the only way it " +
                    "reads falling: as a liar standing behind " +
                    "it. The proctor's objection is not on " +
                    "this board. It is standing in a hallway, " +
                    "where it will stay, saying what he heard."
                "stood" -> "🏁 The count stood. The graph was " +
                    "complete — every pair judged, its verdicts " +
                    "earned — and the room kept the count it " +
                    "arrived with anyway. What is proven stays " +
                    "proven, on this board, in full. What is " +
                    "minuted stays minuted. The two documents " +
                    "now disagree in public, forever, politely."
                else -> "🏁 Item six happened without the " +
                    "graph's voice. ${clueCount8(state)} pieces " +
                        "of the evening were in one person's " +
                        "hands that night. The room ran on the " +
                        "column alone, the way it did the first " +
                        "time. Rooms keep doing that until " +
                        "somebody reads them a graph."
            })
            if (state.arc8Branch == "aligned") {
                if (state.arc8LetterPending) {
                    out.add("🔤 One thing is still out there: " +
                        "the tray's last slot, empty since the " +
                        "setup evening. Some mornings are worth " +
                        "walking past the school office for.")
                } else if (state.arc8LetterBack) {
                    out.add("🔤 The tray is full — the S came " +
                        "back the way it went, in a stretch " +
                        "nobody watched, and for once nobody in " +
                        "town is demanding the stretch be " +
                        "counted. The file stays closed: this " +
                        "is what a closed file looks like when " +
                        "it's counted right.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS8 = mapOf(
        "aligned" to listOf(
            "The sign? Says the slogan, plus a line the crew " +
                "added and nobody voted on. Town reads the " +
                "second line first now. Growth, probably.",
            "Heard Family Night got an arithmetic lesson " +
                "with its concert? Whole evening was miscounted, " +
                "turns out. Nobody lied. Town's still " +
                "adjusting to the concept."),
        "accused" to listOf(
            "The sign's fixed and the matter's settled, says " +
                "the program. There's a teacher at that school " +
                "who says one sentence about it, to anybody, " +
                "in the hall. Ask him. Or don't. He'll tell " +
                "you anyway.",
            "Item six? Oh, they named the kid. Detention " +
                "kid, fallen alibi, open and shut. Funny — the " +
                "proctor keeps the OTHER version, out loud, " +
                "in the hallway, like a second program nobody " +
                "printed."),
        "stood" to listOf(
            "The sign pranked itself, is the official story. " +
                "In a window when everybody was somewhere " +
                "else. Town tells it as a ghost story now, " +
                "basically. The kids love it. The kids were " +
                "supposed to be the suspects. Funny town.",
            "There's a question mark in the school minutes " +
                "that bothers people and nobody can say why. " +
                "Small, neat, printed-looking. It's in the " +
                "margin of the sign business. It'll outlive " +
                "the sign."),
        "cold" to listOf(
            "Family Night? Concert was nice. Item six was " +
                "the one where they settled the sign without " +
                "the one person who'd counted it. Settled it " +
                "the usual way — thinnest story loses.",
            "Some case at the school got decided by a " +
                "column of names, I hear. The graph of it is " +
                "in somebody's Journal, complete as you like. " +
                "Journals keep everything. It's the reading " +
                "that's scarce."))

    /** A post-case ambient aside in the town's own voice —
     *  the ending the town lives with is the one it keeps
     *  telling, at every o'clock it actually happened.
     *  Null until the case is closed. */
    fun ambientCallback8(state: GameState, rnd: Random): String? {
        if (!state.arc8Retired) return null
        val bank = CALLBACKS8[state.arc8Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

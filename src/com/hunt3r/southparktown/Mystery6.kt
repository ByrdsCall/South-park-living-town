package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery6 — the sixth case (v0.8.0f, Queue #8 #6):
// "The Court of Public Opinion".
//
// The pond case (Mystery.kt) was witness-driven, the booth
// case (Mystery2.kt) a compressed whodunit, the buried case
// (Mystery3.kt) an object-driven excavation, the gardener
// case (Mystery4.kt) followed acts of anonymous maintenance,
// and the confession case (Mystery5.kt) was built out of
// claims. This one's antagonist is not a person at all. It
// is the town's certainty. The incident is concrete: the
// town mural on the long Main Street wall — painted by the
// whole town over one summer, every kid's handprint in a
// row along the bottom, a quiet corner kept for the people
// who aren't here anymore — was found on unveiling morning
// painted OVER: every face blacked, the centerpiece gone
// under a night's work of black enamel. By breakfast the
// town had a name. The name was convenient: the goth girl,
// whose poetry is dark, whose clothes are black, who was
// seen near the wall with a sketchbook. The story was juicy
// and the evidence was thin, and the town did what towns
// do — it stopped checking.
//
// The case's engine is a CONVICTION METER, and its first
// rule is honesty: the meter is not stored, it is DERIVED,
// every time, from exactly three recorded things — the
// gossip beats the player has personally witnessed, the
// evidence the player has actually proven, and the stances
// the player has taken out loud. Nothing unwitnessed moves
// it; nothing unproven moves it; the hidden truth never
// touches it at all. The Journal board keeps the town's
// mind in two columns that never merge: THE STORY (beliefs,
// each tagged with where it was heard) and THE EVIDENCE
// (what the player has proven, by verb and place). A belief
// contradicted by evidence the player merely HOLDS stays
// believed — the board says so, in those words — until the
// player spends the evidence at the town meeting. The
// finale is that meeting: the verdict lands where the
// meter sits, unless the evidence the player presents
// outweighs it, or the player's own voice moves the room.
//
// The solution, in one breath, for the payoffs only: the
// hand was the Mayor's. Her own portrait in the mural —
// painted kindly, painted honestly, painted, in her words
// to the patrol log, like "a gourd in a wig" — shamed her
// ahead of the county visit, so she took the hall's key,
// the hall's ladder, and the hall's stored enamel, wore
// her site-visit boots, and blacked the whole center in
// the dark so no one could tell the mercy was aimed at a
// single face — hers. Then she chaired the outrage. The
// evidence never names her: it builds a profile (an adult,
// a borrowed ladder, hall paint, work boots, a hand that
// spared the children's row) that clears the accused on
// three independent channels and the obvious second name
// on two, and it holds exactly one proven fact that points
// at the chair of the meeting — which the meeting, being
// chaired from that chair, cannot spend. The game never
// says it, in any landing, in any line. What the game does
// say, in the ending where the town convicts anyway, is a
// little brother who will say the uncomfortable thing
// forever; and in the ending where the evidence wins, it
// is the chair herself who says it — "this town still owes
// that wall an answer" — never knowing she's right twice.
//
// Structure:
//  - CHAPTERS6: four full ShenaniganDefs on the same
//    episode machinery, outside the random pool, resolving
//    in SHENANIGAN_BY_ID like every case before them.
//  - Gating: the case queues into the mystery slot TEN
//    days after the confession case retires. While any
//    earlier case is open this one does not even schedule.
//  - Gossip: five authored beats, each carried by a named
//    townsfolk in ordinary Talk once its chapter has
//    aired, each witnessed exactly once. Witnessing adds
//    its BELIEF to the story column and its weight to the
//    meter — and opens a STANCE: the player may BACK the
//    story (+4 conviction), QUESTION it (−3), or SAY
//    NOTHING (0), each stated on the button, each
//    remembered on the board. The stance is taken in
//    conversation and offered at the next safe moment.
//  - Evidence: 12 pieces — 5 witness tellings, 5 Look
//    finds, 2 participation beats — each carrying an
//    opinion weight (what it does to the town's mind when
//    proven) and a meeting weight (what it does on the
//    table when presented). At least three pieces exist
//    solely to test the town's favorite story. One name
//    the evidence seems to point at mid-case is clearable
//    on two independent channels.
//  - Pressure: the meter's band (a story going around /
//    hardening / all but decided / decided but for the
//    vote) shades the accused's days in the open: her
//    lines change, her circle closes ranks, the wall and
//    the schoolyard say it in Look text, and the morning
//    ticker notes every hardening.
//  - The finale is THE TOWN MEETING at City Hall, gated
//    on the investigation's weight (the room waits on the
//    person who's done the work). Four calls: PRESENT
//    THE EVIDENCE (only the proven column can be
//    presented — the gate is the evidence itself), BACK
//    THE TOWN, SPEAK FOR THE ACCUSED (hearts, not facts),
//    or LET IT LAND. A meeting that happens without the
//    player is its own landing: cold.
//  - Retirement: every landing retires the arc forever.
//    The cleared landing alone gets the long tail: a week
//    on, the mural's quiet corner is whole again, painted
//    in the night, unsigned.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery6 {

    const val CASE_TITLE = "The Court of Public Opinion"
    const val FINALE6_ID = "arc6_ch4_meeting"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the confession case retires
    const val MEETING_MIN_CLUES = 6

    /** The town's instant answer — public premise; she is
     *  named in the first hour of the first chapter, by the
     *  crowd, on no evidence at all. That is the case. */
    const val ACCUSED = "Henrietta"

    /** The other names the meter tracks while the town
     *  mutters — suspicion drifts for them on the same
     *  three recorded inputs, and for nothing else. */
    val NAMES_IN_PLAY = listOf("Adler", "Randy")

    // ---------------- the meter's published arithmetic ----------------

    const val METER_BASE = 45          // a juicy story, a named kid, no facts yet
    const val VOICE_BACK = 4           // your voice joins the story
    const val VOICE_QUESTION = -3      // you ask for the proof, out loud
    const val VOICE_SILENT = 0         // also a position; also remembered
    const val BACK_BOOST = 10          // backing the town at the meeting itself
    const val SPEAK_RELIEF = 15        // speaking for her moves hearts, not facts
    const val VERDICT_CONVICT_AT = 45  // the score at/above which the story wins
    const val VERDICT_HUNG_AT = 15     // below it, the town can't carry its own story

    val CHOICE_KEYS = listOf("present", "back", "speak", "land")
    const val DEFAULT_BRANCH = "cold"
    val BRANCH_KEYS = listOf("cleared", "convicted", "hung", "cold")

    const val WALL_RETURN_DELAY_DAYS = 7
    const val CONVICT_SCAR = 6         // the convicted one's docked bond

    // ---------------- the four chapters ----------------

    val CHAPTERS6: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc6_ch1_unveiling",
            title = "The Unveiling That Wasn't",
            cause = "All summer the town painted the long wall on " +
                "Main Street — everybody's faces, everybody's names, " +
                "a row of the kids' handprints along the bottom, and " +
                "one quiet corner for the people who aren't here " +
                "anymore. The unveiling is at nine. At eight-forty " +
                "the cloth came down early, and the whole center of " +
                "the wall is GONE under black paint.",
            blurb = "Somebody painted over the town mural in the " +
                "night. The unveiling crowd is standing in front of " +
                "a black rectangle, and the town is already saying " +
                "a name. It is saying it very fast, and very loud, " +
                "and on the strength of a color.",
            theme = QuestTheme("🎨", "#d84a6a", "THE COURT OF PUBLIC OPINION"),
            tintHex = "#2c1c22",
            stages = listOf(
                ShenaniganStage("Kickoff: The Cloth Comes Down",
                    "🎨 UNVEILING DAY: The cloth was supposed to come " +
                        "down at nine, with the band. It came down at " +
                        "eight-forty, because somebody couldn't wait — " +
                        "and the center of the town's wall is a black " +
                        "rectangle, still shining wet in one corner.",
                    "All summer this wall was the town's mirror: " +
                        "everybody painted somebody. The quiet corner " +
                        "at the end — the names of the people who " +
                        "aren't here — took the longest, because " +
                        "everybody wanted it right. It is under the " +
                        "black now, same as the faces.",
                    "See the wall on Main Street.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Standing in front of the black part. Getting louder.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents painted that wall on their knees all summer. The Parents are taking this personally."))),
                ShenaniganStage("Escalation: A Name, By Breakfast",
                    "🎨 Before the band has even unpacked, the crowd " +
                        "has done the arithmetic the town always does: " +
                        "black paint, black clothes, dark poems, a " +
                        "sketchbook. The name goes around the square " +
                        "faster than the coffee does.",
                    "Nobody has looked at the wall yet — not closely, " +
                        "not the way you'd look at a question. They " +
                        "have looked at a girl in a black dress " +
                        "standing at the edge of the crowd, and the " +
                        "looking has been mistaken for evidence.",
                    "Listen to the crowd on Main Street.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Saying a name. Nodding at the name. Repeating the name with toppings.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("goth_kids", influenceDelta = 1,
                            note = "The Goths have heard the name going around. The Goths are closing ranks the way they close ranks: silently, in a row."))),
                ShenaniganStage("Climax: Up Close, Before the Crowd",
                    "🎨 CLIMAX: You get to the wall before the crowd " +
                        "finishes deciding. Up close the black is " +
                        "glossy, unhurried, professional almost — a " +
                        "whole night's work, laid on in long strokes, " +
                        "higher than anybody in the crowd can reach.",
                    "You put a fingertip near the shining corner. " +
                        "Tacky. Whatever else the town is about to " +
                        "get wrong, the paint itself is honest: this " +
                        "was done in the small hours, slowly, by " +
                        "somebody who brought their own ladder and " +
                        "wasn't afraid of the dark.",
                    "Look at the wall up close. Before anyone paints a story over it.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Looking at the wall. Looking at the crowd looking at the girl.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff is doing the arithmetic too — but the Staff has met the girl, and is doing it more slowly."))),
                ShenaniganStage("Resolution: The Story, Week One",
                    "🎨 The cloth is back up, 'for the repainting.' " +
                        "The Mayor has promised a meeting, eventually, " +
                        "and the town has promised itself a culprit, " +
                        "immediately. One of those promises is being " +
                        "kept at a run.",
                    "The wall will keep. Black paint is patient " +
                        "stuff. What won't keep is a name said ten " +
                        "thousand times before anybody checks a " +
                        "single thing — names set, like paint. You " +
                        "have seen the wall up close. Almost nobody " +
                        "else has. That is either nothing, or it is " +
                        "the whole job.",
                    "Check things. In order. Starting anywhere.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Jimbo"),
            episodeTitles = listOf("Unveiling Day", "Black on Black"),
            coldOpen = listOf(
                Line("McDaniels", "I want everyone to stay calm and " +
                    "let the process work. The process WILL work. " +
                    "This town painted that wall together and this " +
                    "town will have its answer together, at a " +
                    "meeting, which I will chair, because somebody " +
                    "has to be the adult in the square."),
                Line("Jimbo", "Forty summers I've watched this town " +
                    "paint things, burn things, and bury things, and " +
                    "I have never once seen it pick a culprit before " +
                    "breakfast. It's picked three. It's down to one. " +
                    "The one is a ten-year-old in a black dress, so " +
                    "you'll forgive me taking the long way round " +
                    "the evidence."))),
        ShenaniganDef(
            id = "arc6_ch2_story",
            title = "The Story Goes Around",
            cause = "A week into the repainting-that-isn't, the story " +
                "has a shape and the shape has a commute: it rides the " +
                "coffee line in the morning, the schoolyard at lunch, " +
                "and the bar rail at night. New details arrive daily. " +
                "None of them were there at the start. All of them " +
                "are believed by lunch.",
            blurb = "The story is growing the way stories do — by " +
                "addition. A poem becomes a confession. A sketchbook " +
                "becomes a revisit. And the girl at the center of it " +
                "has started eating lunch in a stairwell.",
            theme = QuestTheme("☕", "#b0783a", "THE STORY GOES AROUND"),
            tintHex = "#241c14",
            stages = listOf(
                ShenaniganStage("Kickoff: Poetry Night Rules",
                    "☕ THE STORY GOES AROUND: It's poetry night at " +
                        "the coffeehouse, and the town has discovered " +
                        "the accused wrote a poem, months ago, with a " +
                        "line in it about painting over a smiling " +
                        "lie. The town is reading it the way towns " +
                        "read: backwards, from the verdict.",
                    "A poem is being quoted at a wall. The quoting " +
                        "is doing all the work — nobody has asked " +
                        "when it was written, or what it was about, " +
                        "or whether the person who hosts poetry " +
                        "night keeps the programs. Somebody does. " +
                        "She files everything.",
                    "Hear the poem at Tweek Bros. Ask when it's from.",
                    routine = "cluster", clusterLoc = "TweekBros",
                    clusterActivity = "Quoting a poem. Misquoting it slightly worse each round.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have heard the poem. Half the Parents have opinions about poetry now, which is new."))),
                ShenaniganStage("Escalation: The Stairwell Lunch",
                    "☕ The cafeteria has made its ruling, and the " +
                        "accused eats in the stairwell now, with her " +
                        "sketchbook, behind the door that sticks. Her " +
                        "people eat with her — all three of them, in " +
                        "a row, saying nothing, which in goth is a " +
                        "parade.",
                    "She hasn't complained. She won't; complaining " +
                        "is a color she doesn't wear. But the " +
                        "sketchbook comes everywhere now, carried " +
                        "like a shield, and nobody in the cafeteria " +
                        "has asked the one question it answers: " +
                        "what's actually in it.",
                    "See the stairwell at the Elementary. Notice who's in it.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Eating in a stairwell. Not complaining. Carrying a sketchbook like a shield.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: Seen at the Wall",
                    "☕ CLIMAX: The story's newest detail lands " +
                        "with a witness's confidence: the accused " +
                        "was SEEN at the wall, the very night, " +
                        "sketchbook under her arm. The teller nods " +
                        "at his own telling. The room nods with him.",
                    "It might even be true. That's what makes it " +
                        "dangerous. Being at a wall and painting " +
                        "over a wall are different countries, and " +
                        "the story has stamped her passport for both " +
                        "without checking either. The sketchbook " +
                        "knows which country she was in. So does " +
                        "her little brother, if anyone asks him.",
                    "Ask the brother. Look in the sketchbook's direction.",
                    routine = "cluster", clusterLoc = "TweekBros",
                    clusterActivity = "Nodding at a telling. Being very sure, together.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew has a theory. The Crew has several theories. The Crew is keeping a chart, which is more than the cafeteria did."))),
                ShenaniganStage("Resolution: Additions",
                    "☕ The story ends the week bigger than the " +
                        "event it describes: a confession in verse, " +
                        "a criminal revisit, and a girl who has " +
                        "stopped correcting people because the " +
                        "corrections don't carry.",
                    "Stories this size don't shrink on their own. " +
                        "They shrink when somebody puts a fact on " +
                        "the table that the story can't digest — a " +
                        "date, a program, a page in a sketchbook. " +
                        "The town is full of undigested facts. It's " +
                        "also full of people repeating the additions.",
                    "Find a fact the story can't digest.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Mrs. Tweek", "Gerald"),
            episodeTitles = listOf("Coffee Talk", "The Poem"),
            coldOpen = listOf(
                Line("Mrs. Tweek", "We host the poetry night, so I " +
                    "hear everything twice — once when it's said " +
                    "and once when it comes back wrong. This one " +
                    "came back VERY wrong, and I told Richard, I " +
                    "said, that is not what that poem was — well. " +
                    "Nobody asked me. They asked the poem."),
                Line("Gerald", "Now, I'm not one to repeat coffee-" +
                    "shop talk. I'm repeating coffee-shop talk: " +
                    "there's a poem, apparently. Rhymes, apparently. " +
                    "My son says the whole cafeteria has decided, " +
                    "and Kyle doesn't decide things lightly, so — " +
                    "well. It's going around, is all I'm saying."))),
        ShenaniganDef(
            id = "arc6_ch3_second_look",
            title = "The Second Look",
            cause = "The repainting committee, pricing ladders, notices " +
                "the dents. Two of them, at the base of the wall, with " +
                "a drip arc above — and pricing paint, notices the " +
                "batch. The black on the wall is enamel, school-shop " +
                "stock, and suddenly the story has a second name in " +
                "it, an adult one, with keys.",
            blurb = "The wall itself has started testifying — dents, " +
                "a print, a paint batch — and its testimony doesn't " +
                "fit the story at all. It fits, for one bad week, a " +
                "shop teacher. The story is delighted to have options.",
            theme = QuestTheme("🔎", "#6a8ac8", "THE SECOND LOOK"),
            tintHex = "#1c222c",
            stages = listOf(
                ShenaniganStage("Kickoff: The Dents",
                    "🔎 THE SECOND LOOK: The committee's ladder man " +
                        "gets down on one knee at the base of the " +
                        "wall and goes quiet in the professional " +
                        "way. Two dents. A drip arc at three meters. " +
                        "Somebody worked this wall off a ladder, " +
                        "all night, at height.",
                    "The girl the town picked is four-foot-ten in " +
                        "boots she refuses to describe. The wall's " +
                        "top strokes sit at three meters with a " +
                        "ladder's wobble in them. The story has " +
                        "never once mentioned a ladder, because the " +
                        "story has never once looked at the wall.",
                    "Get down on one knee at the wall. Look at the dents.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Measuring dents. Going quiet in the professional way.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff has seen the dents now. The Staff is re-doing the arithmetic with a ladder in it."))),
                ShenaniganStage("Escalation: The Batch",
                    "🔎 The tin gives the story its second name. " +
                        "Black enamel, spring batch, stocked at the " +
                        "hardware shelf and the school shop — and " +
                        "the shop's keeper has the keys, the ladder, " +
                        "and a world-famous grudge about the arts " +
                        "budget. The town exhales: an adult. It knew " +
                        "it should have picked an adult.",
                    "A batch is not a person. A key is not a hand. " +
                        "But the story isn't built out of what things " +
                        "are; it's built out of what they can be " +
                        "said to be, at a counter, by lunch. The " +
                        "shop keeps books, though. Shops always " +
                        "keep books. Books are harder to say things " +
                        "about.",
                    "Read what the shop keeps. Count the enamel.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Being the story's new name. Chewing. Saying 'c'mon, c'mon' to a tin of paint.",
                    tone = "whisper", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents are relieved the story has an adult in it now. The Parents should examine that relief."))),
                ShenaniganStage("Climax: Inventory",
                    "🔎 CLIMAX: The shop gets counted the way shops " +
                        "get counted when a town needs a number more " +
                        "than it needs a villain: every tin, every " +
                        "brush, the sign-out sheet in its binder, the " +
                        "door with its one coat of black from May.",
                    "You helped count. A shop is a kind of memory " +
                        "that can't be talked into anything — the " +
                        "tin says how much left it, the sheet says " +
                        "where the ladder slept, and arithmetic, " +
                        "unlike a crowd, does not care how the story " +
                        "goes at lunch.",
                    "Help with the inventory at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Counting tins. Reading a sign-out sheet by shop light.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("goth_kids", influenceDelta = 2,
                            note = "Somebody is finally counting things. The Goths note it the way they note everything: once, in writing, in a diary."))),
                ShenaniganStage("Resolution: Two Names, One Wall",
                    "🔎 The story ends its third week owning two " +
                        "names and no arithmetic. The meeting is " +
                        "finally on the calendar — the Mayor has set " +
                        "the date, and the town is rehearsing its " +
                        "certainty in front of mirrors.",
                    "What the wall said, it said in dents and " +
                        "batches. What the books said, they said " +
                        "in numbers. Neither of them said a name. " +
                        "The meeting will try to make them. Meetings " +
                        "are where numbers go to be outvoted — or " +
                        "where a town remembers what numbers are for.",
                    "Bring the numbers to the meeting.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Adler", "Victoria"),
            episodeTitles = listOf("Inventory", "The Ladder"),
            coldOpen = listOf(
                Line("Adler", "C'mon, c'mon — thirty years I keep " +
                    "that shop's books to the brushful, and the " +
                    "town reads the BINDER as a confession. Count " +
                    "it. Count it twice. Then count your own " +
                    "ladders, everybody, and we'll see whose " +
                    "arithmetic survives lunch."),
                Line("Victoria", "The board will want this handled " +
                    "with procedure, and procedure is a word this " +
                    "town pronounces 'panic.' I sat that man in a " +
                    "folding chair until ten-forty the night in " +
                    "question, being budgeted AT. Whatever the " +
                    "batch says, the chair says otherwise."))),
        ShenaniganDef(
            id = FINALE6_ID,
            title = "The Town Meeting",
            cause = "The Mayor has called it: the meeting room at City " +
                "Hall, the wall's file on the table, and a town that " +
                "has been rehearsing its verdict for three weeks. " +
                "Tonight the story either becomes the record — or " +
                "meets the evidence in public, with witnesses.",
            blurb = "One room, one file, one name the town has been " +
                "saying since breakfast on unveiling day. What lands " +
                "in the record depends on what the town believes " +
                "when it's called — and on what you put on the table.",
            theme = QuestTheme("🏛️", "#e8c53a", "THE TOWN MEETING"),
            tintHex = "#2c2416",
            stages = listOf(
                ShenaniganStage("Kickoff: The Room Fills",
                    "🏛️ THE TOWN MEETING: The meeting room, set the " +
                        "way it is set for the verdicts that matter: " +
                        "the long table, the file, a chair left " +
                        "pointedly empty at the end of it, and the " +
                        "whole town fitting itself into folding " +
                        "chairs like a jury of neighbors.",
                    "She is not in the empty chair. She is in the " +
                        "back, with her people, in the row nobody " +
                        "sits in by accident anymore. The chair at " +
                        "the end of the table is for whoever the " +
                        "town decides she is. It has been sat in by " +
                        "her name for three weeks.",
                    "See the room set at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Fitting into folding chairs. Rehearsing certainty, quietly.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are watching how this town treats a kid it picked before breakfast."))),
                ShenaniganStage("Escalation: The Story, Read Aloud",
                    "🏛️ The story is read into the record first — " +
                        "all of it, the poem and the sketchbook and " +
                        "the black on black, in the Mayor's best " +
                        "meeting voice. Read aloud, in a quiet room, " +
                        "it sounds thinner than it did at the coffee " +
                        "line. It also sounds like the record already.",
                    "This is the part where stories usually win: " +
                        "the reading. Everything after the reading " +
                        "has to be heavier than a voice. Evidence " +
                        "is heavier — if it's in the room. Character " +
                        "is heavier — if it's believed. Silence is " +
                        "just the story, again, with better manners.",
                    "Hear the story read at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Listening to the story read aloud. Hearing how thin it sounds indoors.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: The Part the Story Can't Do",
                    "🏛️ CLIMAX: The reading ends. Down the table, " +
                        "past the file, the gavel stops over the one " +
                        "person who has spent three weeks checking " +
                        "things instead of repeating them. The room " +
                        "waits. The back row waits. The girl in it " +
                        "does not look up, which is its own testimony.",
                    "Put the evidence on the table — all of it, " +
                        "what's proven and nothing else. Or add your " +
                        "voice to the story's side of the room. Or " +
                        "stand up for a person, with no facts at all, " +
                        "and see if the room remembers it has a " +
                        "heart. Or let it land the way it was always " +
                        "going to land. The record will keep whichever " +
                        "one you choose. So will she.",
                    "Make the call at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Waiting on the call, in a room gone very deliberate.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: By Morning",
                    "🏛️ By morning the record will say something — " +
                        "a name cleared, a name convicted, a question " +
                        "left open, or a meeting that happened to a " +
                        "girl while her one checker was somewhere " +
                        "else. The town will repeat it for years, " +
                        "the way it repeats everything: slightly " +
                        "wrong, completely certain.",
                    "A story was told about a person for three " +
                        "weeks, at full volume, on no evidence. " +
                        "However the morning writes it, the telling " +
                        "is in the record too — every addition, every " +
                        "nod. Towns keep two files on a night like " +
                        "this. Only one of them has her name on it.",
                    "See what the morning makes of it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Henrietta"),
            episodeTitles = listOf("The Meeting", "The Score"),
            coldOpen = listOf(
                Line("McDaniels", "Order. ORDER. We are here to " +
                    "decide what this town believes, and we are " +
                    "going to decide it like adults — with a file, " +
                    "in a room, on the record. The wall deserves " +
                    "that. The town deserves that. Proceed."),
                Line("Henrietta", "…Whatever you decide in here, " +
                    "decide it about the WALL. You've all been " +
                    "deciding about a dress and a diary for three " +
                    "weeks. The wall is right there. It has dents " +
                    "in it. Nobody's read the dents.")))
    )

    val CHAPTER6_BY_ID: Map<String, ShenaniganDef> = CHAPTERS6.associateBy { it.id }

    /** 0-based chapter index of a def id, or -1 when it's not ours. */
    fun chapterIndexOf6(defId: String?): Int =
        CHAPTERS6.indexOfFirst { it.id == defId }

    fun isChapter6(defId: String?): Boolean = chapterIndexOf6(defId) >= 0

    // ---------------- the gossip (the story, being told) ----------------

    /** One telling of the story. A gossip beat exists in the
     *  world once its chapter has aired; it is WITNESSED
     *  exactly once, in Talk with its carrier — witnessing is
     *  the only door. Until witnessed it moves nothing, says
     *  nothing, and is nothing: the meter reads witnessed
     *  gossip only, by construction. */
    data class Gossip6(
        val id: String,
        /** The cast member whose mouth it comes out of. */
        val carrier: String,
        /** 0-based chapter that must have BEGUN for this
         *  telling to be going around. */
        val minChapter: Int,
        /** The telling, as the carrier tells it to you. */
        val spoken: String,
        /** The story in one breath — what the stance dialog
         *  and the board's story column call it. */
        val storyShort: String,
        /** The accused-meter weight of this telling. */
        val accusedDelta: Int,
        /** Suspicion this telling lays on the other names. */
        val suspicion: Map<String, Int> = emptyMap()
    )

    val GOSSIP6: List<Gossip6> = listOf(
        Gossip6("instant_verdict", "McDaniels", 0,
            "Between us and the record: the town had its name " +
                "before the band unpacked, and I will not pretend " +
                "otherwise to the person checking. Black paint. " +
                "Black dress. Poems like a basement. I chaired a " +
                "crowd this morning that had finished before it " +
                "started, and my job — my JOB — is to make the " +
                "record slower than the crowd. Help me make it " +
                "slower. Or don't. The crowd will keep its speed " +
                "either way.",
            "Henrietta Biggle painted over the wall — black " +
                "paint, black clothes, case closed by breakfast",
            accusedDelta = 12),
        Gossip6("the_poem", "Gerald", 1,
            "I'm not one to repeat coffee-shop talk, so I'll say " +
                "it once, quietly: there's a POEM. Hers. It has a " +
                "line in it — 'paint over their smiling lie' — and " +
                "half the parents are saying that's not poetry, " +
                "that's a plan she read out loud in advance. Kyle " +
                "says the cafeteria's decided. Kyle doesn't decide " +
                "things lightly. It's going around, is all.",
            "She wrote it down first — “paint over their " +
                "smiling lie” — practically a signed confession",
            accusedDelta = 8),
        Gossip6("seen_at_wall", "Garrison", 1,
            "Mm, well, the word in the staff room — and I am " +
                "only telling you what the staff room says — is " +
                "that she was SEEN. At the wall. That night. " +
                "Sketchbook under her arm, mmkay, looking at her " +
                "work. Guilty people go back and look, that's a " +
                "known thing, that's practically science. I don't " +
                "write the staff room's conclusions, I just — " +
                "mm — deliver them.",
            "She was seen at the wall that night, sketchbook " +
                "under her arm — guilty people go back to look",
            accusedDelta = 8),
        Gossip6("randy_squeaks", "Randy", 2,
            "Okay, okay — since it's going around anyway, let " +
                "me get AHEAD of it: yes, I said the mural's farm " +
                "corner looked small. SMALL. It's a mural, Sharon, " +
                "it's supposed to — the point is, now half the " +
                "town is saying I offered to 'fix it myself, free, " +
                "at night.' I fix things! That's my whole thing! " +
                "And now MY name's in the story too, as a BACKUP " +
                "name, which — honestly? Rude. The goth kid did " +
                "the wall. Focus, people.",
            "Randy Marsh offered to “fix the mural himself” — " +
                "that farm corner, at night, for free",
            accusedDelta = 2, suspicion = mapOf("Randy" to 20)),
        Gossip6("the_shops_paint", "Jimbo", 2,
            "Now, I sell paint, so hear me as a professional: " +
                "that black is shop enamel, spring batch, and " +
                "there's exactly two shelves in this town stock " +
                "it — the hardware's, and the school shop's. And " +
                "the school shop has a keeper, and the keeper has " +
                "the keys and the ladder and a grudge about the " +
                "arts budget you could set a watch by. I'm not " +
                "saying the man's name into the story. I'm saying " +
                "the story's saying it, and it isn't saying the " +
                "girl's quite as loud this week.",
            "The paint is the school shop's batch — and Adler " +
                "has the keys, the ladder, and the grudge",
            accusedDelta = 2, suspicion = mapOf("Adler" to 22))
    )

    val GOSSIP6_BY_ID: Map<String, Gossip6> = GOSSIP6.associateBy { it.id }

    /** A telling is going around once its chapter has aired
     *  and the case is still open. */
    fun gossipAvailable6(state: GameState, g: Gossip6): Boolean =
        !state.arc6Retired && state.arc6Chapter > g.minChapter

    /** Witness a telling: TalkGate's owed lines call this for
     *  the carrier — the telling lands, once, and the stance
     *  it opens stays open until the player takes it. */
    fun gossipLine6(state: GameState, npcName: String): Line? {
        val g = GOSSIP6.firstOrNull { it.carrier == npcName } ?: return null
        if (!gossipAvailable6(state, g)) return null
        if (g.id in state.arc6Gossip) return null
        state.arc6Gossip.add(g.id)
        state.events.add("🗣️ THE STORY SPREADS: ${g.storyShort}. (Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🗣️ The story spreads: ${g.storyShort}")
        return Line(npcName, g.spoken)
    }

    // ---------------- your voice (the stances) ----------------

    val STANCE_KEYS = listOf("back", "question", "silent")

    /** What a stance does to the needle — printed on the
     *  button before it is pressed, and pinned by the suite. */
    fun stanceDelta(stance: String): Int = when (stance) {
        "back" -> VOICE_BACK
        "question" -> VOICE_QUESTION
        else -> VOICE_SILENT
    }

    /** The oldest witnessed telling whose stance the player
     *  hasn't taken yet — the conversation the town is still
     *  waiting to hear the player's side of. */
    fun pendingStanceGossip(state: GameState): Gossip6? {
        if (state.arc6Retired) return null
        return GOSSIP6.firstOrNull {
            it.id in state.arc6Gossip && it.id !in state.arc6Voice
        }
    }

    /** Take the stance, out loud, on the record. Once per
     *  telling; the board remembers all of them forever. */
    fun chooseStance(state: GameState, gossipId: String, stance: String): Boolean {
        if (stance !in STANCE_KEYS) return false
        val g = GOSSIP6_BY_ID[gossipId] ?: return false
        if (g.id !in state.arc6Gossip) return false
        if (g.id in state.arc6Voice) return false
        if (state.arc6Retired) return false
        state.arc6Voice[g.id] = stance
        state.events.add(when (stance) {
            "back" -> "📣 You back the story, out loud: \"${g.storyShort}.\" " +
                "It goes around with your name under it now."
            "question" -> "🧐 You question the story, out loud: \"Checked it " +
                "how?\" The telling goes around a little slower after that."
            else -> "🤐 You say nothing. The story notes your silence " +
                "and keeps it, the way it keeps everything."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    // ---------------- the beliefs (THE STORY column) ----------------

    /** A belief is a gossip beat, written down as the town
     *  holds it. It stands on the board once its telling has
     *  been witnessed; it is never edited by evidence — the
     *  only thing evidence can do to a belief is be PRESENTED
     *  against it at the meeting, and the board records
     *  whether that happened, or pointedly did not. */
    data class Belief6(
        val id: String,
        val gossipId: String,
        /** The belief, as the town states it. */
        val text: String,
        /** Where it was heard — the tag the column keeps. */
        val source: String,
        /** The evidence that speaks to it, if any piece does. */
        val testedBy: String?
    )

    val BELIEFS6: List<Belief6> = listOf(
        Belief6("b_instant", "instant_verdict",
            "Henrietta Biggle painted over the wall in the " +
                "night. Black paint, black dress, black poems. " +
                "The town did the arithmetic by breakfast.",
            "the unveiling crowd, Main Street",
            "her_media"),
        Belief6("b_poem", "the_poem",
            "She confessed in advance, in verse: “paint over " +
                "their smiling lie.” A poem with a plan in it.",
            "coffee-shop talk, by way of Gerald",
            "poem_context"),
        Belief6("b_seen", "seen_at_wall",
            "She was at the wall that very night, sketchbook " +
                "under her arm. Guilty people go back to look " +
                "at their work.",
            "the staff room, by way of Garrison",
            "the_sketchbook"),
        Belief6("b_randy", "randy_squeaks",
            "Randy Marsh offered to fix the mural himself — " +
                "that farm corner, at night, for free. A backup " +
                "name, in case the first one washes out.",
            "Tweek Bros, from the man himself, loudly",
            "sharon_account"),
        Belief6("b_shop", "the_shops_paint",
            "The black on the wall is the school shop's batch, " +
                "and Adler has the keys, the ladder, and the " +
                "grudge. The story's second name, an adult one.",
            "the counter at Jimbo's",
            "shop_accounted")
    )

    val BELIEF6_BY_ID: Map<String, Belief6> = BELIEFS6.associateBy { it.id }

    /** A belief stands on the board once its telling has
     *  been witnessed — heard by the player, from its
     *  source, with the player's own ears. */
    fun beliefStanding(state: GameState, b: Belief6): Boolean =
        b.gossipId in state.arc6Gossip

    /** Where a belief ends up. Mid-case it is simply
     *  STANDING, whatever the player holds — the town has
     *  not heard the evidence yet, because the player has
     *  not presented it, because there has been no meeting.
     *  After the meeting the board tells the truth about
     *  the difference: put to the town, or held forever. */
    fun beliefFate(state: GameState, b: Belief6): String {
        if (!state.arc6Retired) return "STANDING"
        val tested = b.testedBy
        return when {
            tested != null && tested in state.mystery6Clues && state.arc6Presented ->
                "PUT TO THE MEETING"
            tested != null && tested in state.mystery6Clues ->
                "HELD, NEVER PRESENTED"
            else -> "STILL STANDING"
        }
    }

    /** True while the player holds evidence that bears on
     *  this belief — a marker on the board, nothing more.
     *  It does not soften the belief, move it, or correct
     *  it. The town cannot hear what hasn't been said. */
    fun beliefHasHeldEvidence(state: GameState, b: Belief6): Boolean =
        !state.arc6Retired && b.testedBy != null &&
            b.testedBy in state.mystery6Clues

    // ---------------- the evidence (THE EVIDENCE column) ----------------

    enum class Kind6 { WITNESS, LOOK, PARTICIPATION }

    data class Clue6(
        val id: String,
        val kind: Kind6,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        val title: String,
        val detail: String,
        /** What proving this does to the town's mind — the
         *  meter hears proven evidence (hard proof moves it
         *  a lot; impressions move it a little). */
        val opinion: Int,
        /** What this weighs on the table when PRESENTED at
         *  the meeting — facts already in the room are the
         *  only facts the record can use. */
        val weight: Int,
        /** Suspicion this proof lays off, or on, the other
         *  names in play. */
        val suspicion: Map<String, Int> = emptyMap(),
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

    val CLUES6: List<Clue6> = listOf(
        Clue6("saw_it_first", Kind6.PARTICIPATION, 0,
            "Still tacky at nine in the morning",
            "You were at the wall before the crowd finished " +
                "deciding, and you put a fingertip to the shining " +
                "corner yourself: tacky. Enamel laid on in the " +
                "small hours, in long unhurried strokes, higher " +
                "than the crowd reaches. Whatever the town gets " +
                "wrong today, the timing is not in dispute — " +
                "this was night work, done slowly.",
            opinion = 0, weight = 0,
            chapterDefId = "arc6_ch1_unveiling"),
        Clue6("handprints_spared", Kind6.LOOK, 0,
            "The handprint row was spared",
            "Every painted face on the wall is under the black — " +
                "except the bottom row, the children's handprints, " +
                "which the black stops at as cleanly as tape. A " +
                "brush that hated this wall would not have spared " +
                "the one row the town loves most. Whoever held " +
                "the brush knew exactly what that row was.",
            opinion = -6, weight = 8,
            locId = "MainStreet",
            foundText = "🔍 Along the bottom of the black, at " +
                "knee height: the handprints. Every child's " +
                "painted hand, untouched, in a row — the black " +
                "stops above them with an edge like masking " +
                "tape. Faces, names, the quiet corner: gone. " +
                "The little hands: spared, deliberately, one " +
                "by one."),
        Clue6("ladder_and_boot", Kind6.LOOK, 1,
            "Ladder dents, and a boot that isn't hers",
            "At the wall's base: two dents in the ground where a " +
                "ladder stood all night, and a drip arc at three " +
                "meters with a ladder's wobble in it. In the paint " +
                "strip pooled at the base, one perfect print: a " +
                "man's work boot, about an eleven, left heel worn " +
                "down. The accused's known footwear prints like a " +
                "waffle iron. This print is not that print. This " +
                "ladder was not her ladder either — she is four-" +
                "foot-ten, and the strokes say the painter stood " +
                "high and worked long.",
            opinion = -8, weight = 10,
            locId = "MainStreet",
            foundText = "🔍 The base of the wall, close: two " +
                "ladder dents, deep, hours-deep. A drip arc at " +
                "three meters. And in the black that pooled at " +
                "the bottom, one clear print — a work boot, " +
                "big, left heel worn. You have seen the accused's " +
                "shoes. Whatever made this print, she wasn't " +
                "wearing it."),
        Clue6("her_media", Kind6.LOOK, 1,
            "Charcoal, ink, and one dry gouache",
            "The accused's whole arsenal, gathered by the school " +
                "with her mother's permission: charcoal sticks, " +
                "ink pens, paper — and one gouache the size of a " +
                "postage stamp, dry since spring. The wall wears " +
                "enamel by the quart, laid on with a brush as " +
                "wide as a hand. Her media could not have done " +
                "this work in a month of nights.",
            opinion = -6, weight = 8,
            locId = "Elementary",
            foundText = "🔍 Her things, in a box the school kept: " +
                "charcoal. Ink. Paper. One gouache, stamp-sized, " +
                "dry since spring. Nothing in this box could " +
                "black a wall; nothing in it wants to. The " +
                "wall outside wears a quart of enamel and a " +
                "hand-wide brush. Different arsenal entirely."),
        Clue6("the_tin", Kind6.LOOK, 1,
            "The tin under the scaffold",
            "Under the scaffold plank at the wall: a quart of " +
                "black enamel, three-quarters gone, hardware label, " +
                "spring batch. The batch is stocked at exactly " +
                "two places in town — the hardware shelf, and the " +
                "school shop. A batch is not a person. But it is " +
                "where the second name in the story came from, " +
                "and now it is a thing, in a bag, with a label.",
            opinion = 0, weight = 4,
            suspicion = mapOf("Adler" to 12),
            locId = "MainStreet",
            foundText = "🔍 Under the scaffold plank: the tin. " +
                "Black enamel, quart, spring batch, hardware " +
                "label. Somebody worked out of it all night " +
                "and left it like it belonged here. The batch " +
                "card at the hardware names two stockists in " +
                "town. One of them is a school."),
        Clue6("the_sketchbook", Kind6.WITNESS, 1,
            "She drew it. A hundred times.",
            "The sketchbook is full of the mural — studies, " +
                "dated, going back to June. The last study is " +
                "dated the night itself, timed in the corner in " +
                "a careful hand: 8:10 PM. In it, the wall is " +
                "WHOLE. The accused was at the wall that night — " +
                "drawing it, at dusk, and went home. Whatever " +
                "happened to the wall happened after her page, " +
                "in the dark she wasn't in.",
            opinion = -10, weight = 12,
            npc = "Bradley",
            spoken = "You want to see it? She's drawn it like a " +
                "hundred times. Look — June. July. This one's " +
                "the corner, with the names. And THIS one — see " +
                "the corner, where she writes the time? 8:10. " +
                "That night. The wall's all THERE in it, every " +
                "face. She came home and showed Mom the page " +
                "and went to bed mad that the light went. " +
                "Whatever got painted got painted after her " +
                "page. She draws the truth, even when it's " +
                "boring. ESPECIALLY then."),
        Clue6("poem_context", Kind6.WITNESS, 1,
            "The poem was about a billboard",
            "The program file at the coffeehouse is kept the " +
                "way its keeper keeps everything: by date. The " +
                "poem with the line is from MARCH — read at the " +
                "spring poetry night, printed in the program, " +
                "and it is about the old car-wash billboard on " +
                "the highway, whose smiling family the accused " +
                "has hated for years. The mural did not exist " +
                "until summer. The town is quoting a billboard " +
                "poem at a wall.",
            opinion = -6, weight = 7,
            npc = "Mrs. Tweek",
            spoken = "I keep the programs. I keep EVERYTHING; " +
                "it's a condition. That poem — the one going " +
                "around — she read it here in MARCH, at the " +
                "spring night. It's about the billboard. The " +
                "car-wash family, out on the highway — she's " +
                "hated that billboard since she was six, it's " +
                "practically her oldest relationship. The wall " +
                "wasn't even primed in March. I told Richard: " +
                "they are quoting a billboard at a wall, and " +
                "nobody will ask the woman with the programs. " +
                "You're asking. It's — it's nice to be asked."),
        Clue6("sharon_account", Kind6.WITNESS, 2,
            "Home all night, being furious",
            "The second name in the story spent the night in " +
                "question in his bathrobe, rewatching the " +
                "unveiling rehearsal video and composing a " +
                "complaint about the size of a painted farm. The " +
                "doorbell camera has him at home at dusk, at " +
                "midnight, and at two in the morning, in the " +
                "same robe, angrier at a video each time. " +
                "Whatever painted the wall, it was not also " +
                "doing that.",
            opinion = -2, weight = 3,
            suspicion = mapOf("Randy" to -24),
            npc = "Sharon",
            spoken = "Randall was HOME. All night. I have the " +
                "doorbell footage and, God help me, the audio. " +
                "He watched the rehearsal video four times. He " +
                "believes the farm corner was painted small on " +
                "purpose, as part of a conspiracy of scale. He " +
                "was in that robe from dinner to two in the " +
                "morning, and I will swear to the robe in any " +
                "room this town books. A man cannot be in two " +
                "places at once, and he was, extensively, in " +
                "that one."),
        Clue6("boots_at_close", Kind6.WITNESS, 2,
            "Boots behind the hall, near eleven",
            "Locking up a little before eleven, the bar's " +
                "keeper watched a pair of work boots cross the " +
                "alley behind the hall, toward the wall, carrying " +
                "something long. Pale flecks on the left boot — " +
                "paint colors, plural, the mural's own palette. " +
                "No face; the alley light has been out since " +
                "spring, which the town also has opinions about.",
            opinion = -4, weight = 6,
            npc = "Skeeter",
            spoken = "Lockin' up, little before eleven, I see " +
                "boots come through the alley back of the hall. " +
                "Work boots. Carryin' somethin' long, like a " +
                "ladder or a grudge. Left boot's got paint on " +
                "it — not black, neither. Colors. Like they'd " +
                "stood in the mural's leavings sometime that " +
                "week. Didn't see a face. Alley light's been " +
                "out since spring and the town would rather " +
                "blame a child than a light bulb, so — here " +
                "we are."),
        Clue6("adler_meeting", Kind6.WITNESS, 2,
            "The board meeting ran to ten-forty",
            "The man the paint points at spent the night in a " +
                "folding chair at the school board meeting, being " +
                "budgeted at, until 10:40 PM — and then drove the " +
                "principal home himself, arriving after eleven. " +
                "The hours the wall was painted are the hours he " +
                "was most documented in the history of the town.",
            opinion = -2, weight = 4,
            suspicion = mapOf("Adler" to -18),
            npc = "Victoria",
            spoken = "The board meeting ran until ten-forty. " +
                "Mr. Adler sat two chairs from me for all three " +
                "hours of it — I have the minutes, the sign-in " +
                "sheet, and a crick in my neck shaped exactly " +
                "like that agenda. Afterward he drove me home, " +
                "because my car was being serviced, and we " +
                "arrived at eleven-oh-five, and I noted THAT " +
                "in the minutes too, because I note everything. " +
                "The town may suspect whomever it likes. It may " +
                "not suspect a man out of a chair I was " +
                "watching him sit in."),
        Clue6("shop_accounted", Kind6.PARTICIPATION, 2,
            "The shop's enamel adds up",
            "Inventory night, counted twice: the school shop's " +
                "black enamel is one tin, opened in May for the " +
                "shop door — and the door's coat, the tin's " +
                "remainder, and the brush log agree to the " +
                "brushful. The shop's paint painted a door. And " +
                "the ladder sign-out for the night in question " +
                "does not read the shop at all: it reads the " +
                "COMMUNITY HALL — signed out by the hall's own " +
                "key, signed back in at dawn, with paint on " +
                "its feet.",
            opinion = -4, weight = 8,
            suspicion = mapOf("Adler" to -16),
            chapterDefId = "arc6_ch3_second_look"),
        Clue6("patrol_log", Kind6.LOOK, 2,
            "The patrol log, in pencil",
            "The night patrol's log for the night, in pencil, " +
                "in full: 11:20 PM — boots, alley behind the " +
                "hall, toward the wall, carrying something long; " +
                "did not pursue; flashlight deceased. And two " +
                "nights earlier, same hand: the Mayor, inspecting " +
                "her own portrait on the wall at dusk, delivers " +
                "her assessment of it to the patrol — a gourd in " +
                "a wig — and says somebody ought to fix it before " +
                "the county sees. Noted, says the log. Noted.",
            opinion = -4, weight = 7,
            locId = "CityHall",
            foundText = "🔍 The patrol log, City Hall, open on " +
                "the counter in pencil: the night's entry, " +
                "11:20 — boots in the alley, something long " +
                "being carried, flashlight deceased. And two " +
                "nights before, in the same hand: the Mayor's " +
                "own review of her portrait on the wall, given " +
                "to the patrol at dusk. 'A gourd in a wig. " +
                "Somebody ought to fix it.' The log notes it " +
                "without comment. The log notes everything " +
                "without comment. It's the best-read document " +
                "in town.")
    )

    val CLUE6_BY_ID: Map<String, Clue6> = CLUES6.associateBy { it.id }
    val CLUE6_IDS: Set<String> = CLUES6.map { it.id }.toSet()

    fun clueCount6(state: GameState): Int = state.mystery6Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable6(state: GameState, clue: Clue6): Boolean =
        !state.arc6Retired && state.arc6Chapter > clue.minChapter

    /** Grant a piece of evidence exactly once. The ticker +
     *  the town log hear about it; the board keeps it forever. */
    fun grantClue6(state: GameState, clueId: String): Boolean {
        val clue = CLUE6_BY_ID[clueId] ?: return false
        if (clueId in state.mystery6Clues) return false
        state.mystery6Clues.add(clueId)
        state.events.add("🧩 EVIDENCE ${state.mystery6Clues.size}/${CLUES6.size}: ${clue.title}. " +
            "(Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🧩 Evidence proven: ${clue.title}")
        return true
    }

    /** The belief a piece of evidence speaks to, if it does. */
    fun beliefTestedBy(clueId: String): Belief6? =
        BELIEFS6.firstOrNull { it.testedBy == clueId }

    // ---------------- THE METER (derived, never stored) ----------------

    /** THE CONVICTION METER. This function is the whole
     *  machine: a base for a juicy story, plus every WITNESSED
     *  gossip beat's weight, plus every PROVEN piece of
     *  evidence's opinion, plus every stance the player has
     *  taken out loud — clamped to the dial. It reads nothing
     *  else. It cannot read the truth, because the truth is
     *  not in any of its inputs; it cannot move for an
     *  unwitnessed telling, because unwitnessed tellings are
     *  not in the gossip set; the suite proves the derivation
     *  by recomputing it from the raw records, over every
     *  subset of the inputs. */
    fun conviction(state: GameState): Int {
        var m = METER_BASE
        for (g in GOSSIP6) if (g.id in state.arc6Gossip) m += g.accusedDelta
        for (c in CLUES6) if (c.id in state.mystery6Clues) m += c.opinion
        for ((gid, stance) in state.arc6Voice) {
            if (gid in state.arc6Gossip) m += stanceDelta(stance)
        }
        return m.coerceIn(0, 100)
    }

    /** A suspicion value for one of the other names in play,
     *  derived from the same recorded inputs and nothing
     *  else — the town's muttering, made countable. */
    fun suspicion(state: GameState, name: String): Int {
        var s = 0
        for (g in GOSSIP6) if (g.id in state.arc6Gossip)
            s += g.suspicion[name] ?: 0
        for (c in CLUES6) if (c.id in state.mystery6Clues)
            s += c.suspicion[name] ?: 0
        return s.coerceIn(0, 100)
    }

    /** The meter's band — the accused's weather. Bands are
     *  the pressure system's whole vocabulary. */
    fun bandOf(meter: Int): Int = when {
        meter >= 80 -> 3
        meter >= 60 -> 2
        meter >= 40 -> 1
        else -> 0
    }

    fun bandName(band: Int): String = when (band) {
        3 -> "decided, in everything but the vote"
        2 -> "all but decided"
        1 -> "hardening"
        else -> "a story going around"
    }

    fun band(state: GameState): Int = bandOf(conviction(state))

    // ---------------- discovery: witnesses, looks, hands ----------------

    /** Talk-to discovery: if [npcName] is a live witness right
     *  now, their line — and the evidence it grants. */
    fun witnessLine6(state: GameState, npcName: String): Line? {
        val clue = CLUES6.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable6(state, clue)) return null
        if (!grantClue6(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId] reports
     *  the first time it finds each piece of evidence there —
     *  one at a time, in the order the case has opened them. */
    fun lookClueText6(state: GameState, locId: String): String? {
        val clue = CLUES6.firstOrNull {
            it.kind == Kind6.LOOK && it.locId == locId &&
                it.id !in state.mystery6Clues &&
                clueAvailable6(state, it)
        } ?: return null
        if (!grantClue6(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation6(state: GameState, defId: String) {
        val clue = CLUES6.firstOrNull {
            it.kind == Kind6.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable6(state, clue)) return
        if (grantClue6(state, clue.id)) {
            state.events.add("🧩 Up close, something clicks: ${clue.detail}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- the pressure (the meter, lived) ----------------

    private val PRESSURE_HENRIETTA: Map<Int, List<String>> = mapOf(
        1 to listOf(
            "…The cafeteria has a seating chart now. It's not " +
                "written down. Everybody just KNOWS it. Whatever. " +
                "I ate lunch before all of you decided I was a " +
                "headline, and the lunch was FINE.",
            "…People keep telling me they're 'keeping an open " +
                "mind.' An open mind doesn't need to announce " +
                "itself at my locker. A closed one does. Draw " +
                "your own conclusions — everybody else has."),
        2 to listOf(
            "…The stairwell has better light for drawing anyway. " +
                "That's what I tell my mom. She pretends to " +
                "believe me and I pretend her pretending works, " +
                "and that's the most honest conversation in this " +
                "town right now.",
            "…Somebody left a bucket and a scrub brush by the " +
                "wall. 'For when she has to clean it.' I drew " +
                "the bucket. It's a good drawing. It's the only " +
                "thing about this month that's exactly what it " +
                "looks like."),
        3 to listOf(
            "…They've started saying it in the past tense. Like " +
                "it's a thing that happened in a book. 'When " +
                "Henrietta painted the wall.' I live here. I can " +
                "hear the past tense from my desk.",
            "…Bradley keeps offering to carry my sketchbook, " +
                "like it's evidence and he's my lawyer. He's " +
                "NINE. The town did that. Not me. Whatever the " +
                "wall says happened, the town did THAT, in " +
                "daylight, for free."))

    private val PRESSURE_CIRCLE: Map<String, List<String>> = mapOf(
        "Bradley" to listOf(
            "They're wrong about my sister. I keep the drawings. " +
                "I can PROVE things with the drawings, except " +
                "nobody asks the brother, because the brother is " +
                "nine and the story is faster. The story is " +
                "ALWAYS faster. It's not fair that fast is a " +
                "kind of right.",
            "I sit with her at lunch. Pete sits too, and Michael, " +
                "and Firkle when he's not being a conformist " +
                "about it. That's four. The cafeteria has forty " +
                "and a seating chart. I counted. Counting is " +
                "what you do when you're losing."),
        "Firkle" to listOf(
            "The conformists needed a villain and they picked " +
                "the one who wears black to a WALL PAINTING. I " +
                "could've told them the ending. The ending is " +
                "always: nobody checks anything.",
            "Henrietta eats with us now. She always ate with " +
                "us, but now it has a MEANING, apparently. " +
                "Everything has a meaning when the town is " +
                "bored enough to assign one."),
        "Michael" to listOf(
            "The town's doing the thing where it decides a " +
                "person is a story. We've all read this story. " +
                "It doesn't have chapters. It just has a cover.",
            "For the record — since nobody's keeping one — " +
                "the stairwell has four people in it at lunch " +
                "and the cafeteria has the other forty, and " +
                "only one of those rooms is thinking."))

    /** The pressure, spoken: while the case is open, the
     *  accused's lines — and her circle's — come off the
     *  meter's band, not off the small-talk banks. She never
     *  says a word about the deed; she talks about what the
     *  town is doing, which is the case. */
    fun pressureLine6(state: GameState, npcName: String): Line? {
        if (state.arc6Retired || !state.arc6Started) return null
        val b = band(state)
        if (npcName == ACCUSED) {
            if (b < 1) return null
            val bank = PRESSURE_HENRIETTA[b] ?: return null
            val idx = ((state.absDay % bank.size) + bank.size) % bank.size
            return Line(npcName, bank[idx])
        }
        if (b < 2) return null
        val bank = PRESSURE_CIRCLE[npcName] ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    /** The pressure, seen: the wall and the schoolyard say
     *  what the meter is doing, in Look text. */
    fun pressureLookText6(state: GameState, locId: String): String? {
        if (state.arc6Retired || !state.arc6Started) return null
        val b = band(state)
        if (b < 2) return null
        return when (locId) {
            "MainStreet" ->
                "🗣️ By the wall, somebody has chalked an arrow " +
                    "at the black part and written THE GOTH DID " +
                    "IT in letters a foot high. Below it, in " +
                    "smaller chalk, a second hand has written: " +
                    "CHECK THE DENTS. The chalk war is the most " +
                    "honest debate the town has had all month."
            "Elementary" ->
                "🗣️ The cafeteria's seating chart isn't written " +
                    "down anywhere, which is how everyone can " +
                    "follow it exactly. The stairwell by the gym " +
                    "has four lunches in it now, eaten in a row, " +
                    "in silence, like a tiny parliament of the " +
                    "unconvinced."
            else -> null
        }
    }

    /** The morning after a band change, the ticker says so —
     *  once per crossing, in either direction. The band is
     *  remembered in the life map (the one piece of meter
     *  bookkeeping that is stored, and it stores the REPORT,
     *  never the value: the value is always derived). */
    fun noteNewDay6(state: GameState) {
        if (state.arc6Retired || !state.arc6Started) {
            state.life.remove("arc6Band")
            return
        }
        val b = band(state)
        val prev = state.life["arc6Band"]
        state.life["arc6Band"] = b
        if (prev == null || prev == b) return
        if (b > prev) {
            state.events.add("🗞️ THE STORY HARDENS: the town's mind is " +
                "${bandName(b)} (${conviction(state)}/100). The accused " +
                "eats lunch in the stairwell again today.")
            while (state.events.size > 40) state.events.removeAt(0)
            townLogLine(state, "🗞️ The story hardens: ${bandName(b)}")
        } else {
            state.events.add("🗞️ THE STORY LOOSENS: the town's mind is " +
                "${bandName(b)} (${conviction(state)}/100). Nothing " +
                "announced it. It never announces the loosening.")
            while (state.events.size > 40) state.events.removeAt(0)
            townLogLine(state, "🗞️ The story loosens: ${bandName(b)}")
        }
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after the
     *  confession case retires books chapter 1 ten days out
     *  from that retirement. While any earlier case is open
     *  this never fires — and never even books the date: the
     *  mystery slot has one case at a time. Chapters defer,
     *  day by day, past holiday episodes, like every case
     *  before them. */
    fun dueChapter6(state: GameState): ShenaniganDef? {
        if (state.arc6Retired) return null
        if (!state.arc5Retired) return null
        if (state.arc6Chapter >= CHAPTERS6.size) return null
        if (state.arc6DueAbsDay < 0) {
            state.arc6DueAbsDay = if (state.arc6Chapter == 0) {
                val anchor = if (state.arc5RetiredDay >= 0)
                    state.arc5RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc6DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS6[state.arc6Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted6(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf6(def.id)
        if (idx < 0) return
        state.arc6Started = true
        state.arc6Chapter = idx + 1
        state.arc6DueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the town meeting ----------------

    /** The meeting can be held: the last chapter is airing,
     *  its climax has begun, no call is locked yet, and the
     *  player holds enough of the evidence to be the one
     *  the room waits on. What the call can DO is another
     *  matter — that is the meter's business, and the
     *  evidence column's. */
    fun meetingDue(state: GameState): Boolean {
        if (state.arc6Retired || state.arc6Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE6_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return clueCount6(state) >= MEETING_MIN_CLUES
    }

    /** The weight of what the player can put on the table:
     *  exactly the proven column, piece by piece — unproven
     *  evidence weighs nothing, because it does not exist. */
    fun presentedWeight(state: GameState): Int =
        CLUES6.filter { it.id in state.mystery6Clues }.sumOf { it.weight }

    /** THE SCORE the verdict lands on, for a given call:
     *  the town's mind, moved by the call and nothing else.
     *  Presenting spends the evidence column's weight;
     *  backing adds your voice to the story's; speaking for
     *  her moves hearts by a stated amount; letting it land
     *  leaves the town exactly where it stood. One function,
     *  published on the board before the meeting is called,
     *  and pinned by the suite across the whole space. */
    fun meetingScore(state: GameState, choice: String): Int =
        when (choice) {
            "present" -> conviction(state) - presentedWeight(state)
            "back" -> conviction(state) + BACK_BOOST
            "speak" -> conviction(state) - SPEAK_RELIEF
            else -> conviction(state)
        }

    /** Where a score lands. Printed on the board, in
     *  advance, in these words: the record is not allowed
     *  to surprise anyone who read the board. */
    fun branchForScore(score: Int): String = when {
        score >= VERDICT_CONVICT_AT -> "convicted"
        score >= VERDICT_HUNG_AT -> "hung"
        else -> "cleared"
    }

    /** Make the call, in the room, on the record. */
    fun chooseMeeting(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!meetingDue(state)) return false
        state.arc6Choice = choice
        if (choice == "present") state.arc6Presented = true
        state.events.add(when (choice) {
            "present" ->
                "🧩 You put the evidence on the table — all of " +
                    "it, the proven column and nothing else. The " +
                    "room goes quiet in the way rooms do when " +
                    "arithmetic enters them."
            "back" ->
                "📣 You back the town. Your voice goes into the " +
                    "record under the story's, in your own " +
                    "handwriting. The back row hears it. She " +
                    "hears it."
            "speak" ->
                "🖤 You stand up for her — no facts, no file, " +
                    "just a person saying a person. The room " +
                    "doesn't know what to do with it, which is " +
                    "how you know it landed."
            else ->
                "🤐 You let it land. The story does what it was " +
                    "always going to do, at the speed it was " +
                    "always going to do it, and your silence goes " +
                    "in the record with everything else."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class MeetingOutcome(
        val branch: String,          // "cleared" | "convicted" | "hung" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, hand the wrap its
     *  words. A call locked without the evidence behind it
     *  (impossible through the UI — the meeting never opens)
     *  degrades to the cold landing, the discipline of every
     *  case before this one. A meeting nobody called at all
     *  is cold by definition: the town met without you, and
     *  did what towns do. */
    fun resolveMeeting(state: GameState): MeetingOutcome {
        val chosen = state.arc6Choice
        val backed = clueCount6(state) >= MEETING_MIN_CLUES
        val branch = if (chosen in CHOICE_KEYS && backed)
            branchForScore(meetingScore(state, chosen))
            else DEFAULT_BRANCH
        if (branch == DEFAULT_BRANCH) state.arc6Presented = false
        state.arc6Branch = branch
        state.arc6Retired = true
        state.arc6RetiredDay = state.absDay
        val held = clueCount6(state)
        return when (branch) {
            "cleared" -> MeetingOutcome(branch,
                "The record says the evidence outweighed the story. " +
                    "Henrietta Biggle is cleared — in the room, on " +
                    "the record, by the same town that picked her " +
                    "before breakfast. She accepts it the way she " +
                    "accepts weather: briefly, and without smiling " +
                    "for the minutes.",
                listOf(
                    "🏛️ CLEARED, ON THE EVIDENCE: It is the rarest " +
                        "sentence a story like this ever hears: the " +
                        "town checked. The dents, the print, the " +
                        "batch, the books, the boy with the " +
                        "sketchbook — put on the table one piece " +
                        "at a time, in a quiet room, until the " +
                        "story couldn't carry its own weight and " +
                        "set it down. The girl goes home to her " +
                        "own lunch table. The town goes home to a " +
                        "wall it still has to repaint, and a name " +
                        "it has to un-say, which is harder.",
                    "🏛️ WHAT THE RECORD DOESN'T SAY: Whose hand " +
                        "held the brush. The evidence built the " +
                        "shape of it — an adult's height, a " +
                        "borrowed ladder, paint borrowed from the " +
                        "hall's own stores, a hand careful around " +
                        "the children's row — and the shape, in " +
                        "the end, was enough. It had to be. It was " +
                        "all the town ever actually had."))
            "convicted" -> MeetingOutcome(branch,
                "The record says Henrietta Biggle painted over the " +
                    "town wall. The sentence is community service: " +
                    "she will repaint it herself, alone, to the " +
                    "town's satisfaction. She takes the news " +
                    "standing very straight, in the back row, and " +
                    "says 'fine' in a voice that carries to the " +
                    "gavel.",
                listOf(
                    "🏛️ CONVICTED, ON THE STORY: Three weeks of " +
                        "telling outweighed three weeks of " +
                        "checking, in a room where both were " +
                        "present. The poem was read into the " +
                        "record. The sketchbook's dates were read " +
                        "into the record too, by somebody, quietly, " +
                        "at the end, when it no longer mattered " +
                        "to the count. The record holds both. The " +
                        "record is very good at holding both.",
                    "🏛️ THE SENTENCE: One girl, one wall, one " +
                        "summer of repainting — hers to do alone, " +
                        "which the town calls fair and her brother " +
                        "calls something else, in a voice he is " +
                        "going to keep using. The wall will come " +
                        "back perfect. Everyone who watches her " +
                        "paint it will have to watch her paint the " +
                        "corner too — the names of the people who " +
                        "aren't here — and some of them, watching, " +
                        "are going to have a long summer to think " +
                        "about arithmetic."))
            "hung" -> MeetingOutcome(branch,
                "No verdict. The meeting argued until the chairs " +
                    "were stacked around it and adjourned without " +
                    "one — the story couldn't carry the room and " +
                    "couldn't be put down either. The wall stays " +
                    "half black and half argued-about. The accused " +
                    "stays accused, in the town's throat if not in " +
                    "its record.",
                listOf(
                    "🏛️ NO VERDICT: It is the landing nobody " +
                        "rehearsed. The story arrived with a " +
                        "breakfast's certainty and left with a " +
                        "question it couldn't digest — a date, a " +
                        "print, a page in a sketchbook, sitting in " +
                        "the middle of it like a stone in a shoe. " +
                        "The town will walk a little slower from " +
                        "now on. It will tell itself that's the " +
                        "same as an answer. It isn't. She knows " +
                        "it isn't. The wall knows.",
                    "🏛️ THE OPEN FILE: The case stays open in " +
                        "exactly the way cases stay open here: no " +
                        "drawer, no date, just a girl who eats " +
                        "lunch in a stairwell a little less each " +
                        "month, and a story that gets told a " +
                        "little quieter each time, by people who " +
                        "have started, privately, to check things."))
            else -> MeetingOutcome(branch,
                "The meeting happened without you. The town did " +
                    "what towns do when the one person checking " +
                    "things isn't in the room: it finished the " +
                    "story by acclamation before the coffee went " +
                    "round. The record says her name. The record " +
                    "does not say you weren't there. Records rarely " +
                    "record the empty chair.",
                listOf(
                    "🏛️ BY ACCLAMATION: No evidence was presented, " +
                        "because the person holding it was " +
                        "somewhere else. The poem was read. The " +
                        "dress was described. The vote took four " +
                        "minutes. Somewhere in the middle of it " +
                        "her brother stood up in the back and sat " +
                        "back down, because he is nine and the room " +
                        "was full of adults being certain.",
                    "🏛️ THE EMPTY CHAIR: $held of ${CLUES6.size} " +
                        "pieces of the truth were in somebody's " +
                        "pocket that night, proven and portable, " +
                        "and the meeting ran on none of them. The " +
                        "wall keeps its black. She keeps her " +
                        "stairwell. The town keeps its story, which " +
                        "is the one thing it never has any trouble " +
                        "keeping."))
        }
    }

    /** The landing's ripples + its permanent marks — called
     *  by the wrap BEFORE the episode's credits are built, so
     *  the recap's faction-shift diff includes them. */
    fun applyMeetingEffects(state: GameState, branch: String) {
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
            "cleared" -> {
                befriend("Henrietta", 4)
                befriend("Bradley", 3)
                ripple("parents", 1,
                    "The Parents watched this town clear a kid it had convicted at breakfast, on the strength of checked things. It has made the meetings quieter, in a good way.")
                state.arc6WallPending = true
                state.arc6WallDay = state.absDay + WALL_RETURN_DELAY_DAYS
                state.lifeText["mark:MainStreet"] =
                    "The wall is being repainted — by the town this " +
                        "time, slowly, with the record corrected. The " +
                        "accused's name was cleared in the room where " +
                        "it was convicted-by-breakfast. The quiet " +
                        "corner goes last. Everybody understands why " +
                        "the quiet corner goes last."
                townLogLine(state, "🏛️ CLEARED: the evidence outweighed " +
                    "the story, in the room, on the record. The town " +
                    "is repainting its wall and un-saying a name.")
            }
            "convicted" -> {
                befriend("Henrietta", -CONVICT_SCAR)
                befriend("Bradley", -3)
                ripple("parents", -1,
                    "The Parents are not proud of the mural verdict. A girl is repainting a wall alone, and the arithmetic never did get read into the record in order.")
                state.lifeText["mark:MainStreet"] =
                    "The wall is being repainted by order of the " +
                        "meeting — one girl, one brush, community " +
                        "service. She is doing it perfectly, " +
                        "including the corner. Especially the corner. " +
                        "The town watches more quietly than it " +
                        "expected to."
                townLogLine(state, "🏛️ CONVICTED: the story won. The " +
                    "accused repaints the wall herself. Her brother " +
                    "has opinions, and a voice he intends to keep using.")
            }
            "hung" -> {
                ripple("goth_kids", 1,
                    "No verdict. The Goths note that the town blinked — the first time a story this size has ever blinked here.")
                state.lifeText["mark:MainStreet"] =
                    "The wall stays half black, half primer, pending " +
                        "a verdict the town adjourned without. " +
                        "Somebody chalks the score on it some nights. " +
                        "Somebody else washes the chalk off. It is, " +
                        "in its way, the town's most honest document."
                townLogLine(state, "🏛️ NO VERDICT: the meeting adjourned " +
                    "with the story indigestible and the charge " +
                    "unproven. The wall stays half-and-half.")
            }
            else -> {
                befriend("Henrietta", -CONVICT_SCAR)
                state.lifeText["mark:MainStreet"] =
                    "The wall was decided by acclamation, in a " +
                        "meeting its one checker didn't attend. The " +
                        "black stays. The sentence is being served in " +
                        "a stairwell, at lunch, indefinitely."
                townLogLine(state, "🏛️ BY ACCLAMATION: the meeting ran " +
                    "without the evidence, and the story landed the " +
                    "way stories land in empty rooms.")
            }
        }
    }

    // ---------------- the corner comes back (the CLEARED landing's week after) ----------------

    /** The delayed landing of the CLEAR verdict: called daily
     *  by the engine; fires exactly once, a week after the
     *  town cleared the accused on the evidence — the mural's
     *  quiet corner is whole again one morning, repainted in
     *  the night, perfectly, unsigned. Every name restored.
     *  One face, everyone notices eventually, has been painted
     *  a little kinder than the summer's version. Nobody signs
     *  it. Nobody asks. The town has learned, expensively,
     *  what asking the wrong question costs. */
    fun noteWallRestored(state: GameState): Boolean {
        if (!state.arc6WallPending) return false
        if (state.absDay < state.arc6WallDay) return false
        state.arc6WallPending = false
        state.arc6WallRestored = true
        state.events.add("🎨 THE QUIET CORNER IS WHOLE AGAIN this " +
            "morning — repainted in the night, every name restored, " +
            "the brushwork patient and exact. Unsigned. One face in " +
            "it, everyone notices eventually, sits a little " +
            "kinder than it did in the summer's version. The town " +
            "has decided, formally, to admire it and say no more.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:MainStreet"] =
            "The wall is whole. The quiet corner came back in a " +
                "single night, a week after the clearing — every " +
                "name restored, unsigned, one face painted " +
                "kinder than before. The town admires it and says " +
                "no more. It is learning, slowly, at mural speed."
        townLogLine(state, "🎨 THE CORNER CAME BACK, a week after " +
            "the clearing, painted in the night by nobody the " +
            "town can name. The town has voted, privately, to " +
            "let it stand.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "cleared" to mapOf(
            "McDaniels" to listOf(
                "The verdict was the verdict and I chaired it " +
                    "fairly, which is all anyone can — the record " +
                    "is closed, the girl is cleared, and the wall " +
                    "is being repainted by volunteers. And I will " +
                    "say to you what I say to the mirror: this " +
                    "town still owes that wall an ANSWER. The " +
                    "evidence cleared a name. It never found a " +
                    "hand. Somebody's hand. I lose sleep in " +
                    "proportion, which the minutes do not record.",
                "People ask me if I'm satisfied. I'm the MAYOR. " +
                    "Satisfied is a setting I visit. The case is " +
                    "closed, the corner is — the corner is being " +
                    "seen to. Ask me about the budget. The budget " +
                    "I can look at directly.")),
        "convicted" to mapOf(
            "Bradley" to listOf(
                "My sister is repainting the wall. Alone. " +
                    "Because a room full of adults read a POEM " +
                    "and stopped thinking. She draws the truth " +
                    "in a book I can show anybody, any time, " +
                    "with the dates in the corners. Nobody asked " +
                    "for the book at the meeting. I was THERE. I " +
                    "had it IN MY BAG.",
                "She painted the corner last week. The names. " +
                    "She did them better than the summer did, and " +
                    "she wouldn't sign it. When she's done, the " +
                    "town will have its wall back and it'll be " +
                    "perfect, and I want everyone to remember " +
                    "WHOSE hands made it perfect, and why they " +
                    "were available.")),
        "hung" to mapOf(
            "Victoria" to listOf(
                "No verdict is not a verdict, whatever the " +
                    "minutes say — and I keep the minutes, so I " +
                    "know what they say. The town had the numbers " +
                    "read at it and the numbers sat there, being " +
                    "numbers, while the room decided it couldn't " +
                    "decide. I have filed it under UNFINISHED, " +
                    "which in my filing is the drawer that " +
                    "matters.",
                "Procedure was followed. I want that understood. " +
                    "Every rule was followed, every voice was " +
                    "heard, and the machinery produced — a shrug, " +
                    "in triplicate. If the evidence ever finishes " +
                    "arriving, my office will still be keeping " +
                    "the file. It's what offices are FOR.")),
        "cold" to mapOf(
            "Bradley" to listOf(
                "You weren't there. I kept looking at the door. " +
                    "The book was in my bag — the drawings, the " +
                    "dates, the page from that night with the " +
                    "whole wall in it — and they read the poem " +
                    "and voted and it took four minutes. She eats " +
                    "in the stairwell now. You weren't THERE.",
                "She says it's fine. She says the stairwell has " +
                    "better light. It doesn't. I checked the " +
                    "light. Nothing in that stairwell is better " +
                    "except she's in it.")))

    /** The standing lines after the meeting: gratitude is
     *  not among them — every landing leaves somebody saying
     *  the uncomfortable thing, forever, to your face. In the
     *  cleared landing it's the chair herself, insisting the
     *  town owes the wall an answer, and she is righter than
     *  she knows. The game never admits a call was wrong;
     *  the wronged just never stop knowing. Null for everyone
     *  else. */
    fun aftermathLine6(state: GameState, npcName: String): Line? {
        if (!state.arc6Retired) return null
        val bank = AFTERMATH_LINES[state.arc6Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The sixth case shows once it exists: chapter 1 has
     *  aired, a telling has been witnessed, a piece of
     *  evidence is in hand, or the case is closed. */
    fun boardVisible6(state: GameState): Boolean =
        state.arc6Started || state.arc6Gossip.isNotEmpty() ||
            state.mystery6Clues.isNotEmpty() || state.arc6Retired

    fun statusStamp6(state: GameState): String = when {
        state.arc6Retired && state.arc6Branch == "cleared" ->
            "⚖️ CLEARED — the evidence outweighed the story, in public, on the record"
        state.arc6Retired && state.arc6Branch == "convicted" ->
            "📛 CONVICTED — the story won; the wall is being repainted by its accused"
        state.arc6Retired && state.arc6Branch == "hung" ->
            "🌫️ NO VERDICT — the town couldn't carry it and couldn't put it down"
        state.arc6Retired -> "🧊 COLD — the meeting happened without you"
        state.shenaniganId == FINALE6_ID -> "🏛️ THE TOWN MEETING"
        state.arc6Started ->
            "🎨 OPEN — chapter ${state.arc6Chapter} of ${CHAPTERS6.size} aired"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the case is —
     *  the one-paragraph weather report of its mind. */
    fun currentTheory6(state: GameState): String = when {
        state.arc6Retired && state.arc6Branch == "cleared" ->
            "CLOSED, the hard way: the town put its story on a " +
                "table across from its evidence and watched the " +
                "evidence win. It tells the story differently " +
                "now — shorter, with the checking left in. At " +
                "the wall, people have started reading the " +
                "bottom row again: the little hands, spared all " +
                "along, that the story never once looked at."
        state.arc6Retired && state.arc6Branch == "convicted" ->
            "CLOSED. The town says the goth girl painted the " +
                "wall, because the town said it at breakfast and " +
                "the record now agrees with breakfast. At one " +
                "small house in town nobody says it. The record " +
                "and the town are not the same document, whatever " +
                "the gavel claims."
        state.arc6Retired && state.arc6Branch == "hung" ->
            "OPEN, unofficially: no verdict, a wall in two " +
                "colors, and a story that gets quieter every " +
                "time it's told near a fact. The town has not " +
                "apologized. The town has started, in private, " +
                "to check things first — which is either growth " +
                "or embarrassment. The wall isn't saying."
        state.arc6Retired ->
            "DECIDED, in an empty room: the meeting ran on the " +
                "poem and the dress and four minutes, and the " +
                "town has mostly stopped mentioning it, the way " +
                "it stops mentioning everything it did at full " +
                "volume. The stairwell keeps its own record."
        !state.arc6Started ->
            "Nothing yet. The wall is whole, the summer's paint " +
                "is dry, and nobody has painted over anything " +
                "this week. Give it time."
        state.arc6Chapter <= 1 ->
            "Certainty, at breakfast speed. The town did the " +
                "arithmetic — black paint, black dress — and is " +
                "resting on it like a verdict. Almost nobody has " +
                "looked at the wall closely. The wall is being " +
                "very patient about that."
        state.arc6Chapter == 2 ->
            "The story is growing by addition: a poem that " +
                "became a confession, a sketchbook that became " +
                "a revisit. The cafeteria has ruled. The " +
                "stairwell has appealed, silently, in a row of " +
                "four lunches."
        state.arc6Chapter == 3 ->
            "Two names now, and the town is quietly relieved " +
                "one of them is an adult — as if the story's " +
                "problem were its casting. The wall's testimony " +
                "(dents, a print, a batch) sits in the middle of " +
                "it, saying nothing a story can digest."
        else ->
            "Everything checkable has been checked. What " +
                "remains is a room, a file, a girl in the back " +
                "row, and a town about to find out what its own " +
                "mind weighs against its own evidence."
    }

    /** The meeting's published arithmetic, in the board's
     *  own words — the forecast a player can plan by. */
    fun forecastLine6(state: GameState): String {
        val m = conviction(state)
        val w = presentedWeight(state)
        return "🏛️ THE SCORE, AS IT STANDS: the town's mind is at " +
            "$m/100. At the meeting, the record lands where the " +
            "final score sits — $VERDICT_CONVICT_AT or more, the " +
            "story wins; $VERDICT_HUNG_AT to ${VERDICT_CONVICT_AT - 1}, " +
            "no verdict; under $VERDICT_HUNG_AT, she walks. " +
            "Presenting the evidence subtracts its weight " +
            "(yours currently weighs $w). Backing the town adds " +
            "$BACK_BOOST. Speaking for her subtracts $SPEAK_RELIEF. " +
            "Saying nothing changes nothing. Nothing else moves " +
            "the score. Nothing ever has."
    }

    /** The sixth case's board as plain lines (the Journal
     *  renders them below the confession case's; the suite
     *  reads them). Two columns, strictly separate: the
     *  story, then the evidence. Nothing migrates. */
    fun boardLines6(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp6(state))
        out.add("🎨 THE INCIDENT: Over the summer the whole town " +
            "painted the long wall on Main Street — every face, " +
            "every name, the children's handprints in a row, and " +
            "a quiet corner for the people who aren't here. The " +
            "night before the unveiling, somebody painted over " +
            "the center of it in black enamel. By breakfast, the " +
            "town had chosen who. The evidence, at the time, was " +
            "a color.")
        // ---------- THE STORY ----------
        out.add("🗣️ THE STORY — what the town believes " +
            "(${state.arc6Gossip.size}/${GOSSIP6.size} tellings heard)")
        val standing = BELIEFS6.filter { beliefStanding(state, it) }
        if (standing.isEmpty()) {
            out.add("• Nothing yet — the story hasn't reached you. It will.")
        }
        for (b in standing) {
            val fate = beliefFate(state, b)
            val mark = when {
                state.arc6Retired -> when (fate) {
                    "PUT TO THE MEETING" -> "⚖️ PUT TO THE MEETING"
                    "HELD, NEVER PRESENTED" -> "🤐 HELD, NEVER PRESENTED"
                    else -> "🌫️ STILL STANDING"
                }
                beliefHasHeldEvidence(state, b) ->
                    "🗣️ STANDING (you hold evidence that bears on this — the town hasn't heard it)"
                else -> "🗣️ STANDING"
            }
            out.add("• $mark — ${b.text} [heard: ${b.source}]")
        }
        // ---------- the meter ----------
        val m = conviction(state)
        out.add("📊 THE TOWN'S MIND: conviction $m/100 — " +
            "${bandName(bandOf(m))}")
        for (name in NAMES_IN_PLAY) {
            val s = suspicion(state, name)
            if (s > 0) out.add("• also muttered about: $name ($s/100)")
        }
        val backs = state.arc6Voice.values.count { it == "back" }
        val questions = state.arc6Voice.values.count { it == "question" }
        val silents = state.arc6Voice.values.count { it == "silent" }
        if (state.arc6Voice.isNotEmpty()) {
            out.add("🎙️ YOUR VOICE SO FAR: backed the story ×$backs · " +
                "questioned it ×$questions · said nothing ×$silents")
        }
        // ---------- THE EVIDENCE ----------
        out.add("🧩 THE EVIDENCE — what you have proven " +
            "(${clueCount6(state)}/${CLUES6.size})" +
            if (!state.arc6Retired && clueCount6(state) < MEETING_MIN_CLUES)
                " (the room waits on $MEETING_MIN_CLUES)" else "")
        for (clue in CLUES6) {
            if (clue.id in state.mystery6Clues) {
                out.add("• ${clue.title} — ${clue.detail}" +
                    if (clue.weight > 0) " [weighs ${clue.weight} at the meeting]" else "")
            }
        }
        val missing = CLUES6.size - clueCount6(state)
        if (missing > 0 && !state.arc6Retired) {
            out.add("• ??? — $missing still out there. Hear the " +
                "story being told. Then check it — ponds and " +
                "plaques were the last case; this one is dents, " +
                "books, programs, and a boy with a sketchbook.")
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory6(state)}")
        if (!state.arc6Retired && state.arc6Started) {
            out.add(forecastLine6(state))
        }
        if (state.arc6Retired) {
            out.add(when (state.arc6Branch) {
                "cleared" -> "🏁 The call was made, and the evidence " +
                    "was put on the table: ${clueCount6(state)} pieces, " +
                    "weighed in public. The town un-said a name it " +
                    "had said ten thousand times. It is still " +
                    "practicing the un-saying."
                "convicted" -> "🏁 The story landed. " +
                    if (state.arc6Presented)
                        "The evidence was presented — ${clueCount6(state)} " +
                            "pieces — and the room heard it, and the " +
                            "score still stood where the story put it."
                    else
                        "The evidence was never presented. What was " +
                            "proven stayed proven and unheard, which " +
                            "in a meeting is the same as unproven."
                "hung" -> "🏁 Nobody won the room. " +
                    if (state.arc6Presented)
                        "The evidence was presented and it moved the " +
                            "score — just not past the town. The file " +
                            "stays open in the drawer marked UNFINISHED."
                    else
                        "No evidence was presented; the story still " +
                            "couldn't carry its own room. The file " +
                            "stays open, and the wall stays two colors."
                else -> "🏁 The meeting happened without you. " +
                    "${clueCount6(state)} pieces of proven evidence " +
                    "were somewhere else that night. The record " +
                    "says her name. It does not record the empty chair."
            })
            if (state.arc6Branch == "cleared") {
                if (state.arc6WallPending) {
                    out.add("🎨 One thing is still out there: the " +
                        "quiet corner of the wall is still black, " +
                        "and the town keeps half-promising to repaint " +
                        "it on a weekend. Some mornings are worth " +
                        "walking past the wall for.")
                } else if (state.arc6WallRestored) {
                    out.add("🎨 The quiet corner came back a week " +
                        "after the clearing — painted in one night, " +
                        "every name restored, unsigned. One face in " +
                        "it sits a little kinder than the summer " +
                        "painted it. The town admires it and says " +
                        "no more. The file stays closed anyway: " +
                        "somebody's brushwork is in it now, filed " +
                        "under W.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS6 = mapOf(
        "cleared" to listOf(
            "The mural case? Town nearly convicted a kid on a " +
                "poem and a color, and then — checked things. In a " +
                "room. It can be done. Who knew it could be done.",
            "The corner of the wall came back one night, did " +
                "you hear? Every name. Nobody signed it. Town's " +
                "decided not to wonder out loud. Growth, maybe."),
        "convicted" to listOf(
            "The goth girl's repainting the wall, did you see? " +
                "Doing it perfect, too. Funny thing to watch a " +
                "town watch. Nobody watches it comfortably.",
            "Case is closed. Poem did it, they say. Her " +
                "brother carries that sketchbook everywhere now, " +
                "like a lawyer with one exhibit."),
        "hung" to listOf(
            "Whatever happened with the wall thing? Nothing. " +
                "That's the answer — nothing happened, in a " +
                "room, with minutes taken. Wall's still two colors.",
            "The mural case is the one where the town blinked. " +
                "Ask anybody. They'll tell you the story quieter " +
                "than they used to. Not apologizing. Quieter."),
        "cold" to listOf(
            "The wall meeting? Happened. Four minutes, poem, " +
                "vote, done. The person checking things wasn't " +
                "there. Town doesn't bring it up much.",
            "She eats in the stairwell, the mural girl. Still. " +
                "Town decided that in a meeting nobody can quite " +
                "remember attending."))

    /** A post-case ambient aside in the town's own voice — the
     *  ending the town lives with is the one it keeps telling.
     *  Null until the case is closed. */
    fun ambientCallback6(state: GameState, rnd: Random): String? {
        if (!state.arc6Retired) return null
        val bank = CALLBACKS6[state.arc6Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

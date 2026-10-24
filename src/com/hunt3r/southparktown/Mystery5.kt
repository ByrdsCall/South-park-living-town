package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery5 — the fifth case (v0.7.0f, Queue #7 #6):
// "The Two Confessions".
//
// The pond case (Mystery.kt) was witness-driven, the booth
// case (Mystery2.kt) a compressed whodunit, the buried case
// (Mystery3.kt) an object-driven excavation, and the gardener
// case (Mystery4.kt) followed acts of anonymous maintenance.
// This one is built out of CONFESSION FORENSICS. The incident
// is small and concrete: on derby day at Stark's Pond, the
// Golden Lure — the fishing derby's trophy, brass over a lead
// core, mounted on the winner's plaque — was swapped for a
// whittled wooden fake in craft-paint gold. By the next
// morning the town had not one suspect but two CONFESSIONS,
// delivered separately, sincerely, and in detail. Both
// confessors filled their statements with checkable claims —
// where they were, what they touched, what the lure weighs,
// how it mounts. The investigation is a LEDGER: every claim
// in both statements is tested against the world's evidence
// (witness timelines, Look artifacts, hands-on participation)
// and reads CONFIRMED, BROKEN, or UNTESTED on the Journal's
// board. Claims, never people.
//
// The solution, in one breath: neither of them did it.
// Butters confessed out of guilt over a moment he misread —
// he steadied the plaque at the ceremony, it wobbled, and he
// has been sure ever since that he knocked the lure loose
// himself; every checkable thing he says is wrong in a way
// only innocence is wrong (he has never held the lure, never
// been where he swears he was, and the pond he says it sank
// in gives it back nothing). Ned confessed to take a fall
// he was sure was coming for Jimbo — he believes his best
// friend swapped it as a shop publicity stunt, and Ned would
// rather carry it than watch Jimbo's name go in a file; every
// checkable thing Ned says is TRUE, because he really was
// there, alone with the plaque, doing his yearly caretaker's
// round — but being there is all his claims ever prove. The
// fake itself is the spine: whittled basswood under
// water-based craft gold, carved and sanded over days — a
// PLANNED lure, made before derby day, by hands the evidence
// never names. Jimbo, who bought gold paint, whittles
// decoys, was at the dock all morning and profits from the
// scandal, is provably not those hands (his gold is oil
// enamel, receipted, on his own sign; his dusk is witnessed
// at his own till). If the town clears both confessors on
// the evidence, the real lure quietly comes back to the
// bait-shop step a week later — a conscience, or a prankster
// who wanted a story and got more story than they bargained
// for. The game never names the maker, not even here in the
// payoffs; two arcs from now it will still just be the case
// of the two confessions. (This paragraph is the ONLY place
// outside the finale's own payoff text where the shape of
// it is written down; public surfaces — the board, the
// chapters, the changelog — carry the premise, the claims
// and the evidence, never the answer.)
//
// Structure:
//  - CHAPTERS5: four full ShenaniganDefs on the same episode
//    machinery, outside the random pool, resolving in
//    SHENANIGAN_BY_ID like every case before them.
//  - Gating: the case queues into the mystery slot TEN days
//    after the gardener case retires. While any earlier case
//    is open this one does not even schedule — the slot has
//    one case at a time, always.
//  - The confessions arrive STAGGERED: chapter 2 brings the
//    first (and the town relaxes — a confession feels like
//    an ending), chapter 3 brings the second and re-opens
//    everything. Claims open with their confession: nothing
//    in the first statement can be tested before it is made,
//    and the second statement's claims unlock a case that
//    thought it was done.
//  - Evidence: 12 pieces — 5 witness tellings, 5 Look finds,
//    2 participation beats — testing 8 claims and carrying
//    the making-trail and the herring's two clearances.
//  - The finale is a CONFRONTATION: both confessors in the
//    meeting room at City Hall, the ledger on the table. The
//    player's call: name the one whose claims all broke,
//    believe the one whose claims all held, clear them both
//    on the evidence — or never make the call and let two
//    confessions go cold. A wrong call is never admitted by
//    the game and sticks socially, the booth case's rule.
//  - Retirement: every landing retires the arc permanently.
//    The cleared ending alone gets the long tail: a week on,
//    the lure comes home, anonymously, the lost-and-found
//    beat this case earns.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery5 {

    const val CASE_TITLE = "The Two Confessions"
    const val FINALE5_ID = "arc5_ch4_confrontation"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the gardener case retires
    const val CONFRONT_MIN_CLUES = 8
    const val LURE_RETURN_DELAY_DAYS = 7
    const val NAME_SCAR = 8            // the grounded one's docked bond
    const val BELIEVE_SCAR = 6         // the believed one's quieter cost

    /** The two confessors — public premise; their statements
     *  are the case's opening text. Which confession holds up
     *  is the evidence's business, not this list's. */
    val CONFESSORS = listOf("Butters", "Ned")

    /** The three calls that can be made at the confrontation,
     *  and the landing an unmade call falls to. */
    val CHOICE_KEYS = listOf("name", "believe", "clear")
    const val DEFAULT_BRANCH = "cold"
    val BRANCH_KEYS = listOf("name", "believe", "clear", "cold")

    // ---------------- the four chapters ----------------

    val CHAPTERS5: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc5_ch1_derby",
            title = "The Derby and the Light Plaque",
            cause = "Derby day at Stark's Pond: the whole town fishes, " +
                "Scott Malkinson lands the catch of his life, and at the " +
                "handover the winner's plaque — the one that carries the " +
                "Golden Lure, the derby's trophy for longer than anyone " +
                "can remember — weighs nothing at all.",
            blurb = "The derby's Golden Lure has been swapped for a " +
                "whittled fake. The town is outraged, the winner is " +
                "crushed, and somewhere in South Park is a small brass " +
                "fish that isn't where it belongs.",
            theme = QuestTheme("🎣", "#c8a83a", "THE TWO CONFESSIONS"),
            tintHex = "#2c2416",
            stages = listOf(
                ShenaniganStage("Kickoff: Derby Day",
                    "🎣 DERBY DAY: Lines in the water all along the pond. " +
                        "The Golden Lure watches from its plaque, the way " +
                        "it watches every year — brass, bent-hooked, and " +
                        "completely above suspicion.",
                    "Jimbo runs the derby the way he runs everything: " +
                        "loudly, proudly, and with a weigh-in scale he " +
                        "trusts more than most people. The plaque sits at " +
                        "the dock pavilion all morning, in full view of " +
                        "half the town.",
                    "See the plaque at Stark's Pond.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Fishing the derby. Watching the plaque. Arguing about bait.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew takes the derby seriously. The Crew takes everything with a trophy seriously."))),
                ShenaniganStage("Escalation: The Catch of a Lifetime",
                    "🎣 Scott Malkinson has landed a fish the size of a " +
                        "toddler. The dock is going berserk. The ceremony " +
                        "is in ten minutes and the plaque is coming down " +
                        "off its post for the handover.",
                    "It is the best derby in years, which is exactly the " +
                        "kind of day a thing can go wrong in plain sight. " +
                        "Everyone is watching the fish. Nobody, in the " +
                        "way of crowds, is watching the trophy.",
                    "Watch the weigh-in at the dock.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Witnessing the catch. Losing their minds, politely.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are filming everything. Somewhere in that footage is a whole morning."))),
                ShenaniganStage("Climax: It Weighs Nothing",
                    "🎣 CLIMAX: The plaque comes down for the handover — " +
                        "and Scott stops mid-grin. The Golden Lure, " +
                        "forty-one grams of brass over lead, weighs " +
                        "NOTHING. Because it isn't one.",
                    "You held the plaque yourself. The 'lure' on it is " +
                        "painted wood over air — a whittled fish in gold " +
                        "paint, light as a Christmas ornament. The real " +
                        "one was on this plaque this morning. Half the " +
                        "town saw it. Brass doesn't evaporate.",
                    "Hold the plaque at Stark's Pond. Feel it for yourself.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Holding the light plaque. Passing it hand to hand, disbelieving.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff have seen the fake. A whittled fish is school-adjacent work, and the Staff knows it."))),
                ShenaniganStage("Resolution: Evidence, Drawer One",
                    "🎣 The plaque has gone to City Hall as evidence — " +
                        "drawer one, under lock the town doesn't entirely " +
                        "trust. The derby is over. The questions are not.",
                    "Somebody swapped a trophy in front of half the town " +
                        "and nobody saw a thing. The town can stand a " +
                        "crime. What it can't stand is not having anybody " +
                        "to be mad at. That never lasts long in South Park.",
                    "See the plaque at City Hall. Think it over.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Jimbo", "Scott"),
            episodeTitles = listOf("Derby Day", "It Weighs Nothing"),
            coldOpen = listOf(
                Line("Jimbo", "Forty years that lure's gone on that " +
                    "plaque, and I have polished it, weighed it, and " +
                    "saluted it every single one. Whoever swapped it " +
                    "swapped it under my nose, and Ned's. And I intend " +
                    "to be extremely normal about that."),
                Line("Scott", "I won. I actually WON, and the trophy " +
                    "ith made of WOOD. Captain Diabeteth doeth not " +
                    "accept thubstituteth."))),
        ShenaniganDef(
            id = "arc5_ch2_confession_a",
            title = "The First Confession",
            cause = "Three days after the derby, Butters Stotch walks " +
                "into City Hall, asks for the officer, and confesses to " +
                "the whole thing — in detail, in tears, and in writing. " +
                "The town exhales. A confession feels like an ending.",
            blurb = "One confession, signed and witnessed. The town is " +
                "ready to close the file. The file, it turns out, has " +
                "opinions about being closed.",
            theme = QuestTheme("✋", "#a8c86a", "THE FIRST CONFESSION"),
            tintHex = "#232c1c",
            stages = listOf(
                ShenaniganStage("Kickoff: A Boy Walks In",
                    "✋ THE FIRST CONFESSION: Butters Stotch has walked " +
                        "into City Hall and asked, politely, to confess " +
                        "to the lure. Barbrady has the form out. The form " +
                        "has never been used.",
                    "He isn't dragged. He isn't accused. He walked in " +
                        "under his own steam with a packed lunch and a " +
                        "statement he wrote at the kitchen table, and he " +
                        "is very, very sorry, about what sounds like " +
                        "everything.",
                    "Hear the confession at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Confessing. Witnessing a confession. Making tea nobody drinks.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are relieved it's over. The Parents are relieved too early, historically."))),
                ShenaniganStage("Escalation: The Statement, In Detail",
                    "✋ The statement is DETAILED: the ceremony, the " +
                        "wobble, the water, the dusk, the weight, the " +
                        "screws. Butters has thought about nothing else " +
                        "for three days, and it shows — in every line.",
                    "A confession this detailed should close a case by " +
                        "itself. Unless the details are the kind a person " +
                        "assembles afterward, out of worry and other " +
                        "people's talk, the way you assemble a dream " +
                        "you're sure you had.",
                    "Read the statement at City Hall. Every claim of it.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Reading the statement. Reading it again, slower.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: Checkable Things",
                    "✋ CLIMAX: The Mayor reads the statement aloud and " +
                        "the room does something unusual: it goes quiet " +
                        "and starts counting. Claims can be checked. " +
                        "Screws can be counted. Ponds can be dredged.",
                    "You were there for the reading. However a person " +
                        "feels about Butters Stotch, his statement is now " +
                        "the most testable document in South Park — and " +
                        "the town, to its credit, intends to test it " +
                        "before it grounds anybody forever.",
                    "Be there for the reading at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Counting claims on fingers. Looking at the evidence drawer.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff approves of checking. The Staff has graded this town's homework before."))),
                ShenaniganStage("Resolution: The Ledger Opens",
                    "✋ The file stays open one more week — for the " +
                        "checking, the Mayor says, not the doubting. The " +
                        "ledger is on the board: every claim in the " +
                        "statement, waiting for the town's evidence.",
                    "Butters is home, ungrounded for now, doing the " +
                        "dishes with the focus of a boy awaiting a " +
                        "verdict. His mother has opinions about the " +
                        "statement. His mother was IN it, in a manner " +
                        "of speaking.",
                    "Check the pond. Count the rivets. Ask his mom.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Butters", "Linda"),
            episodeTitles = listOf("A Boy Walks In", "Checkable Things"),
            coldOpen = listOf(
                Line("Butters", "Oh, hamburgers. I wrote it all down, " +
                    "fellas — every bit — on account of if it's wrote " +
                    "down it can't get worse. It can get worse, can't " +
                    "it. Ohhh, I knew it could get worse."),
                Line("Linda", "He confessed at breakfast yesterday too — " +
                    "to a plate he broke in 2023. It was his father " +
                    "who broke that plate. My son confesses the way " +
                    "other boys breathe, and somebody in this town " +
                    "is going to CHECK before they file him, or they " +
                    "will answer to me."))),
        ShenaniganDef(
            id = "arc5_ch3_confession_b",
            title = "The Second Confession",
            cause = "A week into the checking, Ned Gerblansky walks " +
                "into Jimbo's Guns, plugs in his voice box, and confesses " +
                "to the whole thing — in detail, in capitals, and over " +
                "his best friend's protests. The town's closed file " +
                "opens itself right back up.",
            blurb = "Two confessions now, and they cannot both be the " +
                "whole truth. The second one is disturbing in a new " +
                "way: every word of it can be checked, and every word " +
                "of it checks.",
            theme = QuestTheme("🎙️", "#6aa8c8", "THE SECOND CONFESSION"),
            tintHex = "#1c262e",
            stages = listOf(
                ShenaniganStage("Kickoff: The Voice Box",
                    "🎙️ THE SECOND CONFESSION: Ned has come into the " +
                        "shop on his day off, which he never does, and " +
                        "said the thing he came to say, which he never " +
                        "does either.",
                    "Jimbo is saying 'now hold on' the way a man says " +
                        "it when he's already too late. Ned has polished " +
                        "that lure every year of his life. Ned carried " +
                        "that plaque down himself. Ned's statement is " +
                        "short, specific, and signed.",
                    "Hear the second confession at Jimbo's Guns.",
                    routine = "cluster", clusterLoc = "JimbosGuns",
                    clusterActivity = "Confessing, by voice box. Protesting, by best friend.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents no longer know what to be relieved about. Two confessions is one too many for relief."))),
                ShenaniganStage("Escalation: Everything Checks",
                    "🎙️ The town goes through Ned's statement with a " +
                        "pencil. The carrying: witnessed. The polishing: " +
                        "his ritual, down to the snagged cloth. The ten " +
                        "minutes alone: logged by the Mayor's own office. " +
                        "The weight: the derby record book, word for word.",
                    "A confession whose every checkable claim survives " +
                        "checking is either the truth — or the truth " +
                        "about everything except the one thing it " +
                        "confesses. The pencil stops at that line every " +
                        "time. So does the town.",
                    "Go through the statement at Jimbo's Guns. Pencil provided.",
                    routine = "cluster", clusterLoc = "JimbosGuns",
                    clusterActivity = "Checking a statement. Checking it again. Putting the pencil down slowly.",
                    tone = "whisper", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew has a whiteboard now. The whiteboard has two columns and no answers."))),
                ShenaniganStage("Climax: Inventory Night",
                    "🎙️ CLIMAX: Jimbo closes the shop and does inventory " +
                        "the way men do when they need their hands busy — " +
                        "every box counted, every tin lined up, his best " +
                        "friend's confession sitting on the counter like " +
                        "a third employee.",
                    "You helped count. The paint shelf, the polish, the " +
                        "ledger with forty years of the shop in it. A " +
                        "shop keeps its own kind of testimony, in stock " +
                        "and receipts, and tonight it all gets read.",
                    "Help with inventory at Jimbo's Guns.",
                    routine = "cluster", clusterLoc = "JimbosGuns",
                    clusterActivity = "Counting stock. Reading a ledger by lamplight.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 1,
                            note = "The Staff notes the town is doing something unusual: reading the evidence before the verdict."))),
                ShenaniganStage("Resolution: Both Statements Stand",
                    "🎙️ Two statements are on file now. One is falling " +
                        "apart claim by claim under the town's pencil. " +
                        "The other has survived everything the town owns " +
                        "to check it with. The lure is still gone.",
                    "And in the middle of the file sits the fake itself — " +
                        "whittled, painted, patient — which neither " +
                        "statement explains at all, because neither " +
                        "statement ever mentions making anything.",
                    "Look at the fake. Read both statements. Sit with it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Ned", "Jimbo"),
            episodeTitles = listOf("The Voice Box", "Everything Checks"),
            coldOpen = listOf(
                Line("Ned", "I CAME IN ON MY DAY OFF, JIM. A MAN DOES " +
                    "THAT FOR ONE REASON. IT'S IN THE STATEMENT. IT'S " +
                    "SIGNED. STOP SAYING 'HOLD ON.'"),
                Line("Jimbo", "Now hold on. Hold ON. I've known that man " +
                    "since we were both young and stupid, and if this " +
                    "town thinks it can read his statement better than " +
                    "I can read HIM, the town has another think coming, " +
                    "and it's coming right for us."))),
        ShenaniganDef(
            id = FINALE5_ID,
            title = "The Confrontation",
            cause = "The Mayor has called it: one room, both confessors, " +
                "the ledger on the table. The town has checked everything " +
                "it knows how to check. What is left is the part checking " +
                "can't do.",
            blurb = "Both statements, both confessors, one table. The " +
                "town can name a name, believe a man, clear them both — " +
                "or fold its hands and let the file go cold.",
            theme = QuestTheme("⚖️", "#e8c53a", "THE CONFRONTATION"),
            tintHex = "#2c2416",
            stages = listOf(
                ShenaniganStage("Kickoff: One Room",
                    "⚖️ THE CONFRONTATION: The meeting room at City Hall, " +
                        "set for once without folding-chair chaos: two " +
                        "chairs on one side of the table, the ledger on " +
                        "it, and the whole town pretending it has " +
                        "somewhere else to be.",
                    "Butters brought a note from his mother. Ned " +
                        "brought nothing at all. Between them on the " +
                        "table: the fake lure, the record book, and every " +
                        "claim this town has spent weeks testing.",
                    "See the room set at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Sitting very still on opposite sides of a table.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents are watching how this town treats two people who asked to be blamed."))),
                ShenaniganStage("Escalation: The Ledger, Read Aloud",
                    "⚖️ The ledger is read aloud, claim by claim, the " +
                        "way the minutes get read — except nobody talks " +
                        "over it. Confirmed. Broken. Untested. The words " +
                        "land where they land.",
                    "It is a strange document, the ledger: it can clear " +
                        "a boy's whole story and still leave a man's " +
                        "standing. It can prove where everybody was and " +
                        "touch nothing of why anybody spoke. It is " +
                        "everything the town knows, and it is not the " +
                        "whole of anything.",
                    "Hear the ledger read at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Listening to a ledger. Watching two faces not watch each other.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: The Part Checking Can't Do",
                    "⚖️ CLIMAX: The reading ends. The Mayor looks down " +
                        "the table at the one person who has read every " +
                        "page of the file as it was written. The room " +
                        "waits. Both confessors wait.",
                    "Name the boy whose story fell apart. Believe the " +
                        "man whose story held. Or say what the evidence " +
                        "actually carries and no further. Every person " +
                        "in this room will live inside your answer, " +
                        "including the two who'll pay for it if it's wrong.",
                    "Make the call at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Waiting on the call, in a room that has gone very quiet.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: By Morning",
                    "⚖️ By morning, the file will say something — a name, " +
                        "a belief, a clearing, or nothing at all. The " +
                        "town will repeat whatever it says for years, " +
                        "the way it repeats everything: slightly wrong, " +
                        "completely certain.",
                    "Two people asked this town to blame them. However " +
                        "the morning writes it, that part stays in the " +
                        "file forever, and the town knows it owes the " +
                        "truth a cleaner handling than it usually manages.",
                    "See what the morning makes of it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Butters", "Ned"),
            episodeTitles = listOf("One Room", "The Part Checking Can't Do"),
            coldOpen = listOf(
                Line("Butters", "I brought a note from my mom, on " +
                    "account of whatever I say in this room I probably " +
                    "already said wrong once. It's a good note. She " +
                    "used the good paper."),
                Line("Ned", "WHATEVER GETS DECIDED, IT GETS DECIDED " +
                    "WITH ME IN THE ROOM. THAT'S ALL I CAME FOR. THAT " +
                    "AND THE TRUTH, IF IT'S GOING.")))
    )

    val CHAPTER5_BY_ID: Map<String, ShenaniganDef> = CHAPTERS5.associateBy { it.id }

    /** 0-based chapter index of a def id, or -1 when it's not ours. */
    fun chapterIndexOf5(defId: String?): Int =
        CHAPTERS5.indexOfFirst { it.id == defId }

    fun isChapter5(defId: String?): Boolean = chapterIndexOf5(defId) >= 0

    // ---------------- the claims ----------------

    enum class Verdict { CONFIRMED, BROKEN, UNTESTED }

    data class Claim5(
        val id: String,
        val confessor: String,
        /** 0-based chapter that must have BEGUN for this claim
         *  to stand on the ledger (its confession must exist). */
        val minChapter: Int,
        /** The claim, as the board states it. */
        val text: String,
        /** The evidence clue that tests it; null for a core
         *  claim (the taking itself), which no evidence can
         *  test directly and whose standing is DERIVED. */
        val clueId: String?,
        /** What the testing evidence settles, for a testable
         *  claim. Core claims carry UNTESTED here and derive. */
        val verdict: Verdict
    )

    val CLAIMS5: List<Claim5> = listOf(
        // ---- the first confession: every testable claim breaks ----
        Claim5("butters_sank", "Butters", 1,
            "He knocked the lure off the plaque at the ceremony, " +
                "and it sank by the dock post.",
            "dock_dredge", Verdict.BROKEN),
        Claim5("butters_alone", "Butters", 1,
            "He was alone at the dock at dusk, looking for it.",
            "home_by_five", Verdict.BROKEN),
        Claim5("butters_weight", "Butters", 1,
            "The Golden Lure is solid brass, heavy as a hammer.",
            "lure_true_weight", Verdict.BROKEN),
        Claim5("butters_screws", "Butters", 1,
            "The lure came off the plaque on two little screws.",
            "plaque_rivets", Verdict.BROKEN),
        Claim5("butters_took", "Butters", 1,
            "He is the one who took the Golden Lure.",
            null, Verdict.UNTESTED),
        // ---- the second confession: every testable claim holds ----
        Claim5("ned_carried", "Ned", 2,
            "He carried the plaque from the shop to the dock " +
                "that morning, the same as every year.",
            "carried_witness", Verdict.CONFIRMED),
        Claim5("ned_polished", "Ned", 2,
            "He polished the lure the night before; the bent " +
                "hook from the '09 snag catches the cloth.",
            "polish_cloth", Verdict.CONFIRMED),
        Claim5("ned_alone", "Ned", 2,
            "He was alone with the plaque on the dock for ten " +
                "minutes before the ceremony.",
            "dock_window", Verdict.CONFIRMED),
        Claim5("ned_weight", "Ned", 2,
            "The lure is brass over a lead core — light for " +
                "its size, like keys in a pocket.",
            "record_weight", Verdict.CONFIRMED),
        Claim5("ned_took", "Ned", 2,
            "He is the one who took the Golden Lure.",
            null, Verdict.UNTESTED)
    )

    val CLAIM5_BY_ID: Map<String, Claim5> = CLAIMS5.associateBy { it.id }

    fun claimsOf(confessor: String): List<Claim5> =
        CLAIMS5.filter { it.confessor == confessor }

    /** A claim stands on the ledger once its confession has
     *  been made — a statement cannot be tested before it
     *  exists, and the board never shows one early. */
    fun claimOpen(state: GameState, claim: Claim5): Boolean =
        state.arc5Chapter > claim.minChapter || state.arc5Retired

    /** THE LEDGER RULE: a testable claim reads exactly what
     *  its evidence settled, and only once that evidence is
     *  actually in hand — no evidence, UNTESTED, whatever the
     *  town suspects. A core claim (the taking itself) can
     *  never be CONFIRMED by any evidence; it reads BROKEN
     *  only when every testable claim of its confession has
     *  broken — a story that has fallen apart in every part
     *  cannot stand in the whole — and UNTESTED otherwise. */
    fun claimStatus(state: GameState, claim: Claim5): Verdict {
        val clueId = claim.clueId
        if (clueId != null) {
            return if (clueId in state.mystery5Clues) claim.verdict
                else Verdict.UNTESTED
        }
        val testable = claimsOf(claim.confessor).filter { it.clueId != null }
        val allBroken = testable.isNotEmpty() && testable.all {
            claimStatus(state, it) == Verdict.BROKEN
        }
        return if (allBroken) Verdict.BROKEN else Verdict.UNTESTED
    }

    /** The confession whose every testable claim broke, if
     *  the ledger has one — the shape of the "name" call. */
    fun collapsedConfessor(state: GameState): String? =
        CONFESSORS.firstOrNull { name ->
            val core = CLAIM5_BY_ID[name.lowercase() + "_took"] ?: return@firstOrNull false
            claimStatus(state, core) == Verdict.BROKEN
        }

    // ---------------- the evidence ----------------

    enum class Kind5 { WITNESS, LOOK, PARTICIPATION }

    data class Clue5(
        val id: String,
        val kind: Kind5,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        val title: String,
        val detail: String,
        /** WITNESS: the cast member who tells you. */
        val npc: String? = null,
        /** WITNESS: the line they say (the telling IS the discovery). */
        val spoken: String? = null,
        /** LOOK: the location to Look at. */
        val locId: String? = null,
        /** LOOK: the sentence the Look reports. */
        val foundText: String? = null,
        /** PARTICIPATION: the chapter def whose climax turf grants it. */
        val chapterDefId: String? = null
    )

    val CLUES5: List<Clue5> = listOf(
        Clue5("plaque_light", Kind5.PARTICIPATION, 0,
            "It weighed nothing in your hands",
            "At the handover you lifted the plaque yourself: it " +
                "weighs nothing. The 'lure' on it is painted wood " +
                "over air. Whatever happened to the Golden Lure, " +
                "it had already happened by the time the winner " +
                "grinned for the cameras.",
            chapterDefId = "arc5_ch1_derby"),
        Clue5("plaque_rivets", Kind5.LOOK, 1,
            "Rivets, not screws",
            "The plaque, in City Hall's evidence drawer: the lure " +
                "mount is three flat RIVETS, factory-set, older than " +
                "the derby's current scale. Nothing on this plaque " +
                "has ever been held by screws, and nothing on it " +
                "comes off by hand.",
            locId = "CityHall",
            foundText = "🔍 The plaque in the evidence drawer, up " +
                "close: the mount is three flat rivets, factory-set " +
                "and older than the scale Jimbo swears by. No " +
                "screws. There have never been screws."),
        Clue5("dock_dredge", Kind5.LOOK, 1,
            "The pond gave back nothing",
            "The town dredged the dock shallows at first light: " +
                "forty-one bottle caps, one boot, a shopping cart " +
                "that has been missing since roughly the dawn of " +
                "time. No lure. The pond gives back everything " +
                "eventually — it has not got the Golden Lure.",
            locId = "StarksPond",
            foundText = "🔍 The dredge marks are still in the mud " +
                "by the dock post: bottle caps in a bucket, one " +
                "boot, a shopping cart. The volunteers' list is " +
                "pinned to the post, and 'lure' is not on it. " +
                "Whatever sank here, it wasn't brass."),
        Clue5("home_by_five", Kind5.WITNESS, 1,
            "Home by five, dishes by six",
            "Butters was home by five on derby day: dishes, " +
                "homework, a bed-check at eight, and a mother whose " +
                "schedule-keeping is a contact sport. He was never " +
                "at the dock at dusk. He has, however, confessed " +
                "to three other things this week, including weather.",
            npc = "Linda",
            spoken = "You can write this in your little book: Leopold " +
                "was HOME by five on derby day. Dishes at six, " +
                "homework at seven, bed-check at eight, and me " +
                "standing in his doorway for all of it. He was not " +
                "at any dock at dusk. He cries at the DARK, " +
                "officer. He confessed to the rain on Tuesday. The " +
                "RAIN."),
        Clue5("lure_true_weight", Kind5.WITNESS, 1,
            "What the lure actually weighs",
            "The Golden Lure is brass plate over a lead core — " +
                "light for its size, forty-one grams on the derby " +
                "scale, a fact known to anyone who has ever held " +
                "it. Butters described it as solid brass, heavy " +
                "as a hammer. Butters has clearly never held it.",
            npc = "Jimbo",
            spoken = "Solid brass, heavy as a hammer — that's what " +
                "the boy wrote? Son, that lure is brass PLATE over " +
                "a lead core. Forty-one grams on my scale, light " +
                "for its size. I've weighed it every year since " +
                "before that boy was born. Anybody who's ever " +
                "HELD it knows its weight. He hasn't. Whatever " +
                "else he did, writing that statement, he did it " +
                "about a fish he's never touched."),
        Clue5("carried_witness", Kind5.WITNESS, 2,
            "Followed the plaque the whole way",
            "The derby winner watched the plaque's whole journey " +
                "that morning — followed it from the shop door to " +
                "the dock post like it was already his. Ned carried " +
                "it, alone, carefully, the same as every year, and " +
                "handed it to the post crew without a stop.",
            npc = "Scott",
            spoken = "I watched the whole way. I followed the plaque " +
                "from the shop door becauthe it wath going to be " +
                "MINE — I could feel it in my inthulin. Ned carried " +
                "it himthelf, both handth, careful ath a baby, and " +
                "he never oneth stopped or thwapped anything. " +
                "Captain Diabeteth doeth not lie about a trophy he " +
                "can thtill win back."),
        Clue5("polish_cloth", Kind5.LOOK, 2,
            "The snagged cloth",
            "On the back counter at Jimbo's: the brass polish, and " +
                "the cloth — soft, old, and torn in exactly one " +
                "place, a small hook-shaped snag worked into it " +
                "over years. Ned's yearly ritual, down to the tear " +
                "left by a bent hook from the '09 snag.",
            locId = "JimbosGuns",
            foundText = "🔍 The back counter at Jimbo's: an open " +
                "tin of brass polish, and the cloth — worn thin, " +
                "and torn once, a small hook-shaped snag. Years " +
                "of the same cloth on the same bent hook. The " +
                "night-before polishing happened exactly the way " +
                "the statement says it did."),
        Clue5("dock_window", Kind5.WITNESS, 2,
            "Ten minutes, one man, logged",
            "The Mayor's office arrived at the dock at 8:40 to set " +
                "up the ceremony. For ten minutes the dock held one " +
                "man and a plaque, and the man was Ned — noted in " +
                "the program log, because the Mayor was late and " +
                "the Mayor's office notes everything when the " +
                "Mayor is late.",
            npc = "McDaniels",
            spoken = "My office set up that ceremony, and I will " +
                "tell you what my log says, because my log is " +
                "never wrong: 8:40, dock, one man, one plaque, " +
                "ten full minutes. The man was Ned Gerblansky, " +
                "doing the caretaking he has done every year of " +
                "my administration. If this town intends to make " +
                "something of those ten minutes, this town had " +
                "better bring more than a feeling to my office."),
        Clue5("record_weight", Kind5.LOOK, 2,
            "The record book agrees with him",
            "The derby record book at City Hall — forty years of " +
                "winners in forty years of hands — describes the " +
                "Golden Lure on its first page: brass over a lead " +
                "core, 41 grams, hook bent in the '09 snag, keep. " +
                "Ned's statement matches it word for word, from " +
                "a book written before he ever confessed to anything.",
            locId = "CityHall",
            foundText = "🔍 The derby record book, first page, in " +
                "a hand forty years old: 'GOLDEN LURE — brass " +
                "over lead core, 41 g. Hook bent ('09 snag). " +
                "Keep.' Ned's statement could have been copied " +
                "off this page — except he has never once been " +
                "seen reading it, and he says it like a man " +
                "reciting his own address."),
        Clue5("fake_making", Kind5.LOOK, 2,
            "A lure made over days",
            "The fake, appraised on the counter: whittled " +
                "basswood — soft, pale, light — under water-based " +
                "craft gold, the kind that comes in little school " +
                "bottles. The carving is layered work, sanded " +
                "between coats, done over days. Nobody whittled " +
                "this at a dock, or in a morning. Somebody planned " +
                "this lure well before derby day.",
            locId = "JimbosGuns",
            foundText = "🔍 The fake, under the counter lamp at " +
                "Jimbo's: basswood, whittled and sanded in " +
                "layers, under two coats of water-based craft " +
                "gold — school-bottle paint, the flaky kind. " +
                "Days of work, done somewhere patient. Whoever " +
                "made this started long before the derby — and " +
                "neither statement on file so much as mentions " +
                "making anything."),
        Clue5("herring_dusk", Kind5.WITNESS, 2,
            "The herring's dusk, witnessed",
            "The dusk patrol stopped at Jimbo's for batteries and " +
                "found the whole evening accounted for: Jimbo at " +
                "his own till, doing inventory to a line of " +
                "customers, from the weigh-in to dark. Whatever " +
                "the town suspects about a man who bought gold " +
                "paint, his dusk was spent selling batteries.",
            npc = "Barbrady",
            spoken = "I have conducted an investigation of Jimbo's " +
                "dusk, on account of the town doing its eyebrow " +
                "thing. Findings: at dusk, Jimbo was at Jimbo's. " +
                "At his till. Selling ME batteries, and three " +
                "other customers line-of-sight. He never left " +
                "that shop from the weigh-in to full dark, which " +
                "I know because I am a trained observer and also " +
                "because my flashlight still doesn't work."),
        Clue5("till_ledger", Kind5.PARTICIPATION, 2,
            "The wrong gold, receipted",
            "Inventory night, the till ledger: Jimbo's gold paint " +
                "purchase — one quart of OIL ENAMEL, 'for the shop " +
                "sign,' receipted weeks back and half-used on the " +
                "sign outside, which you can still smell. The fake " +
                "wears water-based craft paint. Jimbo's gold is " +
                "the wrong gold, bought for the wrong job, with " +
                "a receipt — the three least suspicious properties " +
                "a paint can have.",
            chapterDefId = "arc5_ch3_confession_b")
    )

    val CLUE5_BY_ID: Map<String, Clue5> = CLUES5.associateBy { it.id }
    val CLUE5_IDS: Set<String> = CLUES5.map { it.id }.toSet()

    fun clueCount5(state: GameState): Int = state.mystery5Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable5(state: GameState, clue: Clue5): Boolean =
        !state.arc5Retired && state.arc5Chapter > clue.minChapter

    /** Grant a piece of evidence exactly once. The ticker +
     *  the town log hear about it; the board keeps it forever. */
    fun grantClue5(state: GameState, clueId: String): Boolean {
        val clue = CLUE5_BY_ID[clueId] ?: return false
        if (clueId in state.mystery5Clues) return false
        state.mystery5Clues.add(clueId)
        state.events.add("⚖️ EVIDENCE ${state.mystery5Clues.size}/${CLUES5.size}: ${clue.title}. " +
            "(Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "⚖️ Evidence found: ${clue.title}")
        return true
    }

    /** The claim a piece of evidence tests, if it tests one. */
    fun claimTestedBy(clueId: String): Claim5? =
        CLAIMS5.firstOrNull { it.clueId == clueId }

    /** Talk-to discovery: if [npcName] is a live witness right
     *  now, their line — and the evidence it grants. */
    fun witnessLine5(state: GameState, npcName: String): Line? {
        val clue = CLUES5.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable5(state, clue)) return null
        if (!grantClue5(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId] reports
     *  the first time it finds each piece of evidence there —
     *  one at a time, in the order the case has opened them. */
    fun lookClueText5(state: GameState, locId: String): String? {
        val clue = CLUES5.firstOrNull {
            it.kind == Kind5.LOOK && it.locId == locId &&
                it.id !in state.mystery5Clues &&
                clueAvailable5(state, it)
        } ?: return null
        if (!grantClue5(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation5(state: GameState, defId: String) {
        val clue = CLUES5.firstOrNull {
            it.kind == Kind5.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable5(state, clue)) return
        if (grantClue5(state, clue.id)) {
            state.events.add("⚖️ Up close, something clicks: ${clue.detail}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- the statements, reheard ----------------

    private val STATEMENTS5: Map<String, List<String>> = mapOf(
        "Butters" to listOf(
            "I'll say it just like I wrote it, fellas. At the " +
                "ceremony I steadied the plaque for Scott's photo " +
                "and it WOBBLED, and the lure knocked loose and went " +
                "in the water by the dock post — I heard it plink. " +
                "I didn't say anything on account of Dad would " +
                "ground me till I'm thirty. I was alone down there " +
                "at dusk looking for it, I swear I was. That lure's " +
                "solid brass, heavy as a hammer — it sank like one " +
                "too. And it came off on two little screws, that's " +
                "how loose it was. I'm real sorry. Write it all down.",
            "I know how it sounds when it's said back. But I wrote " +
                "it at the kitchen table and Mom checked my spelling, " +
                "and a fella doesn't confess to a thing he isn't " +
                "sure he did. I'm sure, fellas. I'm as sure as I've " +
                "ever been about anything, and I've been sure about " +
                "some real wrong stuff before, boy, let me tell you."),
        "Ned" to listOf(
            "IT'S IN THE STATEMENT, BUT I'LL SAY IT INTO THE " +
                "RECORD. I CARRIED THE PLAQUE FROM THE SHOP TO THE " +
                "DOCK THAT MORNING, SAME AS EVERY YEAR. I POLISHED " +
                "THE LURE THE NIGHT BEFORE — THE BENT HOOK FROM THE " +
                "'09 SNAG CATCHES THE CLOTH, SAME AS ALWAYS. BEFORE " +
                "THE CEREMONY I WAS ALONE WITH IT ON THE DOCK, TEN " +
                "MINUTES, NOBODY ELSE. AND I KNOW ITS WEIGHT — BRASS " +
                "OVER A LEAD CORE, LIGHT FOR ITS SIZE, LIKE KEYS IN " +
                "YOUR POCKET. IF IT WAS SWAPPED, IT WAS SWAPPED ON " +
                "MY WATCH. SO WRITE MY NAME.",
            "PEOPLE KEEP ASKING WHY A MAN WOULD SAY A THING LIKE " +
                "THAT ABOUT HIMSELF. FORTY YEARS I HAVE CARRIED THAT " +
                "PLAQUE. A MAN'S WATCH IS A MAN'S WATCH. WHAT " +
                "HAPPENED ON IT IS HIS TO CARRY, WHICHEVER WAY IT " +
                "HAPPENED. THAT'S THE WHOLE OF MY TESTIMONY, AND " +
                "THE STATEMENT STANDS."))

    /** A confessor's statement, reheard in full — available
     *  once their confession has been made and while the case
     *  is open. The telling grants nothing; it is the record,
     *  and the record is the game. */
    fun confessionLine5(state: GameState, npcName: String): Line? {
        if (state.arc5Retired) return null
        val chapterNeeded = when (npcName) {
            "Butters" -> 1
            "Ned" -> 2
            else -> return null
        }
        if (state.arc5Chapter <= chapterNeeded) return null
        val bank = STATEMENTS5[npcName] ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after the
     *  gardener case retires books chapter 1 ten days out from
     *  that retirement. While any earlier case is open this
     *  never fires — and never even books the date: the mystery
     *  slot has one case at a time. Chapters defer, day by
     *  day, past holiday episodes, like every case before them. */
    fun dueChapter5(state: GameState): ShenaniganDef? {
        if (state.arc5Retired) return null
        if (!state.arc4Retired) return null
        if (state.arc5Chapter >= CHAPTERS5.size) return null
        if (state.arc5DueAbsDay < 0) {
            state.arc5DueAbsDay = if (state.arc5Chapter == 0) {
                val anchor = if (state.arc4RetiredDay >= 0)
                    state.arc4RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc5DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS5[state.arc5Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted5(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf5(def.id)
        if (idx < 0) return
        state.arc5Started = true
        state.arc5Chapter = idx + 1
        state.arc5DueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the confrontation ----------------

    /** The confrontation can be held: the last chapter is
     *  airing, its climax has begun, no call is locked yet,
     *  and the player holds enough of the evidence to be the
     *  one the room waits on. */
    fun confrontationDue(state: GameState): Boolean {
        if (state.arc5Retired || state.arc5Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE5_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return clueCount5(state) >= CONFRONT_MIN_CLUES
    }

    /** Make the call, with both of them in the room. */
    fun chooseConfrontation(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!confrontationDue(state)) return false
        state.arc5Choice = choice
        state.events.add(when (choice) {
            "name" ->
                "⚖️ You say the name the ledger points at the " +
                    "hardest: the boy whose story fell apart. The " +
                    "room writes it down. His mother is already " +
                    "standing up."
            "believe" ->
                "⚖️ You say the town should believe the man whose " +
                    "every word survived the checking. The room " +
                    "writes it down. He doesn't move. He doesn't " +
                    "have to. It's in the file now."
            else ->
                "⚖️ You set the ledger flat on the table and say " +
                    "what it actually carries: not this one, and " +
                    "not that one either. The room takes a breath " +
                    "it has been holding for weeks."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class ConfrontationOutcome(
        val branch: String,          // "name" | "believe" | "clear" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, and hand the wrap its
     *  words. A choice locked without the evidence behind it
     *  (impossible through the UI — the confrontation never
     *  opens) degrades to the cold landing rather than a free
     *  decide, the discipline of every case before this one. */
    fun resolveConfrontation(state: GameState): ConfrontationOutcome {
        val chosen = state.arc5Choice
        val backed = clueCount5(state) >= CONFRONT_MIN_CLUES
        val branch = if (chosen in CHOICE_KEYS && backed) chosen else DEFAULT_BRANCH
        state.arc5Branch = branch
        state.arc5Retired = true
        state.arc5RetiredDay = state.absDay
        return when (branch) {
            "name" -> ConfrontationOutcome(branch,
                "The file says Butters Stotch, in the Mayor's best " +
                    "handwriting. His statement fell apart in every " +
                    "part the town could test, and the town has " +
                    "decided that is close enough to a story. He is " +
                    "grounded until further notice. Possibly forever.",
                listOf(
                    "⚖️ THE NAME IN THE FILE: Butters Stotch. The " +
                        "town reads the verdict the way it reads " +
                        "everything — completely certain, slightly " +
                        "wrong about the details. What the ledger " +
                        "actually said was narrower: four claims, " +
                        "four breakages, a boy who has never held " +
                        "the lure describing it wrong. The file does " +
                        "not record the difference. Files rarely do.",
                    "⚖️ THE COST: He took it the way he takes " +
                        "everything — apologizing. His mother took " +
                        "it standing in the doorway with the good " +
                        "paper still in her hand, and the Stotch " +
                        "house has gone a kind of quiet that dinner " +
                        "doesn't fix. The lure is still gone. The " +
                        "case is closed. Those two sentences live " +
                        "in the file together now."))
            "believe" -> ConfrontationOutcome(branch,
                "The file says Ned Gerblansky, because Ned " +
                    "Gerblansky's every checkable word was true and " +
                    "the town has decided that is the same thing as " +
                    "an answer. He accepted it the way he accepts " +
                    "weather. The case is closed.",
                listOf(
                    "⚖️ THE NAME IN THE FILE: Ned Gerblansky. The " +
                        "ledger behind the name is the strangest " +
                        "one this town has ever closed a case on: " +
                        "four claims tested, four claims standing — " +
                        "carried it, polished it, alone with it, " +
                        "knew its weight. A man can be true in " +
                        "every particular and the file can still " +
                        "be a story the town told itself. The file " +
                        "does not record the difference. Files " +
                        "rarely do.",
                    "⚖️ THE COST: Jimbo hasn't said the verdict " +
                        "out loud once, which for Jimbo is a " +
                        "symphony. Ned works the counter the same " +
                        "as ever. The lure is still gone, and a " +
                        "man is carrying a file with his name in " +
                        "it for a thing the evidence proved only " +
                        "that he was there for. There are worse " +
                        "sentences. Not many, in a town this size."))
            "clear" -> ConfrontationOutcome(branch,
                "The file says what the evidence says and no " +
                    "further: two confessions, both set aside — one " +
                    "that collapsed in every part, one that stood " +
                    "in every part and still only proved a man was " +
                    "there. The lure's taking belongs to hands " +
                    "this town never found. The case stays open " +
                    "in exactly one drawer.",
                listOf(
                    "⚖️ CLEARED, ON THE EVIDENCE: It is the hardest " +
                        "sentence a town like this can say: we " +
                        "checked everything we know how to check, " +
                        "and it does not carry either of them. The " +
                        "boy goes home to his mother's cooking and " +
                        "a grounding that never lands. The man goes " +
                        "back to his counter and his cloth. Nobody " +
                        "applauds. The truth rarely gets applause. " +
                        "It gets a drawer, and a town that sleeps " +
                        "a little more honestly.",
                    "⚖️ WHAT THE EVIDENCE LEAVES: A fake made " +
                        "over days, in basswood and school-bottle " +
                        "gold, by hands neither statement describes. " +
                        "Somewhere in South Park is a person who " +
                        "planned a small brass fish very carefully " +
                        "and watched two innocent people nearly " +
                        "carry it. The file knows their work. The " +
                        "file does not know their name. Yet."))
            else -> ConfrontationOutcome(branch,
                "No call was ever made. The ledger stayed on the " +
                    "table, the room emptied a chair at a time, and " +
                    "the file went into the drawer with two signed " +
                    "confessions in it and no ending at all — which " +
                    "is, the town slowly understands, its own kind " +
                    "of ending.",
                listOf(
                    "⚖️ TWO CONFESSIONS, NO ANSWER: Somewhere in the " +
                        "drawer the statements sit together still: " +
                        "the boy's, wrong in every checkable line; " +
                        "the man's, right in every checkable line; " +
                        "and under them the fake, whittled and " +
                        "patient, unexplained by either. The town " +
                        "has mostly stopped asking. The drawer has not.",
                    "⚖️ THE LOOSE ENDS: Butters does the dishes " +
                        "with the focus of a boy still awaiting a " +
                        "verdict, some nights. Ned polishes other " +
                        "things now, smaller things. The lure is " +
                        "still gone, and the derby will come round " +
                        "again, and somebody will have to decide " +
                        "what hangs on the plaque."))
        }
    }

    /** The landing's ripples + its permanent marks — called
     *  by the wrap BEFORE the episode's credits are built, so
     *  the recap's faction-shift diff includes them. */
    fun applyConfrontationEffects(state: GameState, branch: String) {
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
            "name" -> {
                befriend("Butters", -NAME_SCAR)
                ripple("parents", -1,
                    "The Parents are not proud of how the lure case ended. A boy is grounded, and the file feels thin at the edges.")
                state.lifeText["mark:CityHall"] =
                    "The file on the Golden Lure is closed. The name " +
                        "in it is Butters'. The fake still sits in the " +
                        "evidence drawer, whittled and patient, " +
                        "explained by nobody's statement — including his."
                townLogLine(state, "⚖️ NAMED: the lure file closed on " +
                    "the boy whose story fell apart. His mother has " +
                    "opinions, and a doorway to stand in.")
            }
            "believe" -> {
                befriend("Ned", -BELIEVE_SCAR)
                ripple("parents", -1,
                    "The Parents have a feeling about the lure verdict that they cannot quite say at meetings. It involves a man's whole life being 'there at the time.'")
                state.lifeText["mark:CityHall"] =
                    "The file on the Golden Lure is closed. The name " +
                        "in it is Ned's. The fake still sits in the " +
                        "evidence drawer, whittled and patient, " +
                        "explained by nobody's statement — including his."
                townLogLine(state, "⚖️ BELIEVED: the lure file closed " +
                    "on the man whose every word checked out. The " +
                    "town is calling that an answer.")
            }
            "clear" -> {
                befriend("Butters", 3)
                befriend("Ned", 3)
                befriend("Jimbo", 2)
                ripple("parents", 1,
                    "The Parents watched this town clear two people it could easily have blamed, and have decided to feel good about the meetings for once.")
                state.arc5LurePending = true
                state.arc5LureDay = state.absDay + LURE_RETURN_DELAY_DAYS
                state.lifeText["mark:StarksPond"] =
                    "Two confessions were made over this dock, and " +
                        "the town — on the evidence — believed neither. " +
                        "The plaque hangs here with a painted fish on " +
                        "it, waiting on whatever the truth does next."
                townLogLine(state, "⚖️ CLEARED: both confessions set " +
                    "aside on the evidence. The file stays open in " +
                    "one drawer, and two people sleep in their own " +
                    "beds tonight.")
            }
            else -> {
                state.lifeText["mark:StarksPond"] =
                    "Two confessions hang over this dock, and the " +
                        "town has stopped asking about either of them. " +
                        "The fake lure still hangs where the real one " +
                        "did, catching the light wrong."
                townLogLine(state, "⚖️ THE FILE WENT COLD with two " +
                    "signed confessions in it. The town has agreed, " +
                    "without ever voting, not to talk about it.")
            }
        }
    }

    // ---------------- the lure comes home (the CLEARED landing's week after) ----------------

    /** The delayed landing of the CLEAR call: called daily
     *  by the engine; fires exactly once, a week after the
     *  town cleared two innocent people — the Golden Lure is
     *  found on the bait-shop step one morning, wrapped in a
     *  polishing cloth that is not Ned's. No note. The town
     *  draws its own conclusions, carefully, and keeps them. */
    fun noteLureReturned(state: GameState): Boolean {
        if (!state.arc5LurePending) return false
        if (state.absDay < state.arc5LureDay) return false
        state.arc5LurePending = false
        state.arc5LureReturned = true
        state.events.add("🎣 ON THE BAIT-SHOP STEP THIS MORNING: the " +
            "Golden Lure — the real one, forty-one grams, bent hook " +
            "and all — wrapped in a polishing cloth that is not " +
            "Ned's. No note. It is back on its plaque by noon, and " +
            "the whole town is being extremely casual about it, " +
            "which is how the whole town says thank you to a person " +
            "it will never know.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:StarksPond"] =
            "The Golden Lure is back on its plaque. It came home a " +
                "week after the town cleared two innocent people — " +
                "left on the bait-shop step overnight, in a cloth " +
                "that isn't Ned's. Nobody saw. Nobody ever will. " +
                "Two people in this town sleep fine about it."
        townLogLine(state, "🎣 THE LURE CAME HOME, a week after the " +
            "clearing. Nobody saw. The town has decided, formally, " +
            "not to wonder too loudly.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "clear" to mapOf(
            "Butters" to listOf(
                "…Fellas, I keep waitin' to feel grounded and it " +
                    "just doesn't come. Mom says a person can be " +
                    "wrong AND sorry AND innocent, all three, and " +
                    "that's most people most days. I wrote THAT " +
                    "down too.",
                "…I still think about that wobble, though. Every " +
                    "plaque I ever touch, I'm gonna steady it with " +
                    "BOTH hands and a prayer. That's my lesson and " +
                    "I'm keepin' it."),
            "Ned" to listOf(
                "YOU STOOD IN THAT ROOM AND SAID WHAT THE LEDGER " +
                    "SAID. NOT MANY WOULD. THE COUNTER'S ALWAYS " +
                    "OPEN TO YOU. SO IS THE BACK ROOM. THAT'S THE " +
                    "WHOLE SPEECH.",
                "…(He sets a small paper bag of tackle on the " +
                    "counter and pushes it across, the way other " +
                    "men deliver a paragraph. It is, for him, a " +
                    "paragraph.)")),
        "believe" to mapOf(
            "Jimbo" to listOf(
                "Ned Gerblansky did not take that lure. I've never " +
                    "said it in the file's hearing and I'm saying " +
                    "it in yours: forty years I have known that " +
                    "man, and there is not a thief's hour in him. " +
                    "The file says what it says. I know what I know.",
                "Town wanted a name and it got a name, and the " +
                    "name was my best friend's, and I have to sell " +
                    "batteries in the same room as that fact every " +
                    "day. So you'll forgive the shop being a little " +
                    "quiet, some mornings.")),
        "name" to mapOf(
            "Linda" to listOf(
                "My son did not take that lure. He can't lie about " +
                    "brushing his teeth — we find the toothpaste on " +
                    "his chin. He confessed because he was SURE, " +
                    "and he was sure because he's nine and guilt is " +
                    "the only explanation he's ever been offered " +
                    "for a wobble. The file is closed. I am not.",
                "They grounded him on the strength of four claims " +
                    "nobody could make stand up in a garden shed. " +
                    "When you're ready to read that ledger again — " +
                    "the whole of it, including the parts about a " +
                    "painted fish nobody explained — my door is " +
                    "open and my schedule is kept.")))

    /** The standing lines after the confrontation: gratitude
     *  from the cleared, and — in the landings where the call
     *  was wrong — one person who will say the uncomfortable
     *  thing to your face, forever. The game never admits a
     *  call was wrong; the wronged just never stop knowing.
     *  Null for everyone else, and in the cold ending. */
    fun aftermathLine5(state: GameState, npcName: String): Line? {
        if (!state.arc5Retired) return null
        val bank = AFTERMATH_LINES[state.arc5Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The fifth case shows once it exists: chapter 1 has
     *  aired, a piece of evidence is in hand, or the case
     *  is closed. */
    fun boardVisible5(state: GameState): Boolean =
        state.arc5Started || state.mystery5Clues.isNotEmpty() || state.arc5Retired

    fun statusStamp5(state: GameState): String = when {
        state.arc5Retired && state.arc5Branch == "name" ->
            "📛 NAMED — a name is on the file, and the town will not un-hear it"
        state.arc5Retired && state.arc5Branch == "believe" ->
            "📛 BELIEVED — the survivor's word was taken as an answer"
        state.arc5Retired && state.arc5Branch == "clear" ->
            "⚖️ CLEARED — both confessions set aside, on the evidence"
        state.arc5Retired -> "🧊 COLD — two confessions, and the town stopped asking"
        state.shenaniganId == FINALE5_ID -> "⚖️ THE CONFRONTATION"
        state.arc5Started ->
            "🎣 OPEN — chapter ${state.arc5Chapter} of ${CHAPTERS5.size} aired"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the case is. */
    fun currentTheory5(state: GameState): String = when {
        state.arc5Retired && state.arc5Branch == "name" ->
            "CLOSED. The town says the boy did it, because the town " +
                "needs the file to say something. At the Stotch table " +
                "nobody says it. The file and the town are not the " +
                "same document, whatever the Mayor's handwriting claims."
        state.arc5Retired && state.arc5Branch == "believe" ->
            "CLOSED. The town says the caretaker did it, on the " +
                "strength of his own true words. At Jimbo's counter " +
                "nobody says it. The file and the town are not the " +
                "same document, whatever the town decided that day."
        state.arc5Retired && state.arc5Branch == "clear" ->
            "OPEN, by the town's own choice: the file stays in its " +
                "one drawer. Two confessions, both set aside. The " +
                "town finds it can live with a question better than " +
                "it feared — it just has to not look directly at " +
                "the plaque. The plaque, lately, has been easier " +
                "not to look at."
        state.arc5Retired ->
            "SHELVED. Two signed confessions and no ending; the town " +
                "has agreed, without ever voting, to leave the file " +
                "in its drawer. The fake still hangs at the pond, " +
                "catching the light wrong, being a question nobody asks."
        !state.arc5Started ->
            "Nothing yet. The lure is on its plaque, the derby is " +
                "a story people tell, and nobody has confessed to " +
                "anything this week. Give it time."
        state.arc5Chapter <= 1 ->
            "A swap at the derby, in front of everyone, seen by no " +
                "one. The town is at the stage where it suspects the " +
                "fish."
        state.arc5Chapter == 2 ->
            "Relief, mostly. A boy confessed — in detail, in tears, " +
                "in writing — and the town would like very much to " +
                "believe the file is closed. The ledger the Mayor " +
                "insisted on is the only thing in town still asking " +
                "questions."
        state.arc5Chapter == 3 ->
            "Two confessions, and the town's arithmetic no longer " +
                "works: one story breaks wherever it's touched, the " +
                "other survives everything and proves only presence. " +
                "The town has started saying 'the ledger' the way it " +
                "used to say 'the file,' in a slightly different tone."
        else ->
            "Everything checkable has been checked. What remains is " +
                "the part checking can't do, in a room with two " +
                "chairs, and the whole town is pretending it has " +
                "somewhere else to be."
    }

    private fun verdictWord(v: Verdict): String = when (v) {
        Verdict.CONFIRMED -> "✔ CONFIRMED"
        Verdict.BROKEN -> "✖ BROKEN"
        Verdict.UNTESTED -> "? UNTESTED"
    }

    /** The fifth case's board as plain lines (the Journal
     *  renders them below the gardener case's; the suite reads
     *  them). */
    fun boardLines5(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp5(state))
        out.add("🎣 THE INCIDENT: On derby day, the Golden Lure — " +
            "the derby trophy, brass over a lead core, 41 grams, " +
            "hook bent in the '09 snag — was swapped on its plaque " +
            "for a whittled wooden fake in craft-paint gold. Two " +
            "people have confessed to taking it. Separately. In " +
            "detail. Both cannot be telling the whole truth.")
        for (confessor in CONFESSORS) {
            val claims = claimsOf(confessor).filter { claimOpen(state, it) }
            if (claims.isEmpty()) continue
            out.add("✋ THE LEDGER — ${confessor.uppercase()}'S CONFESSION")
            for (claim in claims) {
                out.add("• ${verdictWord(claimStatus(state, claim))} — ${claim.text}")
            }
        }
        out.add("🧩 EVIDENCE — ${clueCount5(state)}/${CLUES5.size}" +
            if (!state.arc5Retired && clueCount5(state) < CONFRONT_MIN_CLUES)
                " (facing them both takes $CONFRONT_MIN_CLUES)" else "")
        for (clue in CLUES5) {
            if (clue.id in state.mystery5Clues) {
                out.add("• ${clue.title} — ${clue.detail}")
            }
        }
        val missing = CLUES5.size - clueCount5(state)
        if (missing > 0 && !state.arc5Retired) {
            out.add("• ??? — $missing still out there. Hear the " +
                "statements again. Check the claims against ponds, " +
                "plaques, record books and the people who keep them.")
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory5(state)}")
        if (state.arc5Retired) {
            out.add(when (state.arc5Branch) {
                "name" -> "🏁 The call was made: the boy whose story " +
                    "fell apart. The file is closed, the grounding " +
                    "stands, and the fake in the drawer — explained " +
                    "by nobody — sits in the record next to his name."
                "believe" -> "🏁 The call was made: the man whose " +
                    "every word held. The file is closed on the " +
                    "strength of true statements, and the fake in " +
                    "the drawer — explained by nobody — sits in " +
                    "the record next to his name."
                "clear" -> "🏁 The call was made: neither of them. " +
                    "The ledger carried what it carried and no " +
                    "further, and two people went home to their " +
                    "own beds with their own names."
                else -> "🏁 Nobody made the call. The room emptied, " +
                    "the drawer closed on two confessions and no " +
                    "ending, and the town got on with its year."
            })
            if (state.arc5Branch == "clear") {
                if (state.arc5LurePending) {
                    out.add("🎣 One thing is still out there: a lure " +
                        "doesn't stay taken forever in a town like " +
                        "this. Some mornings are worth checking the " +
                        "bait-shop step for.")
                } else if (state.arc5LureReturned) {
                    out.add("🎣 The Golden Lure came home a week " +
                        "after the clearing — left on the bait-shop " +
                        "step overnight, in a cloth that isn't " +
                        "Ned's. Nobody saw. The town has decided, " +
                        "formally, not to wonder too loudly. The " +
                        "file stays open in its one drawer anyway: " +
                        "somebody's conscience is in it now, filed " +
                        "under L.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS5 = mapOf(
        "name" to listOf(
            "Butters is grounded till he's thirty, over a fish made " +
                "of metal. His mom's face when it comes up, though. " +
                "His mom's face says a whole paragraph.",
            "The Stotches don't talk about the lure case. Nobody " +
                "at their table talks about much of anything, lately."),
        "believe" to listOf(
            "Ned took the lure, they say. Jimbo's face when they " +
                "say it, though. Jimbo's face is a whole dissenting " +
                "opinion.",
            "Case is closed. Name's in the file. Funny how nobody " +
                "around here says the name out loud, like saying " +
                "it would make it more closed."),
        "clear" to listOf(
            "Two people confessed to the same lure and the town " +
                "believed NEITHER. That's the most this town has " +
                "ever trusted its own paperwork.",
            "The lure came home, did you hear? Left where the " +
                "right person would find it. Whoever had it must " +
                "have had a long week of thinking."),
        "cold" to listOf(
            "Whatever happened with the lure thing? Two confessions " +
                "and then — nothing. Town just moved on. Town's good " +
                "at moving on.",
            "The fake one's still on the plaque at the pond. " +
                "Catches the light wrong. Somebody ought to look " +
                "into that. Somebody did, I guess. Once.")
    )

    /** A post-case ambient aside in the town's own voice — the
     *  ending the town lives with is the one it keeps telling.
     *  Null until the case is closed. */
    fun ambientCallback5(state: GameState, rnd: Random): String? {
        if (!state.arc5Retired) return null
        val bank = CALLBACKS5[state.arc5Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery11 — the eleventh case (v0.13.0f, Queue #13 #6):
// "Signed in Another's Voice".
//
// The pond case was witness-driven, the booth case a
// compressed whodunit, the buried case an excavation, the
// gardener case followed acts of maintenance, the
// confession case was built of claims, the opinion case
// was a meter, the backwards case was a timeline, the
// alibi case was a graph, the audit case was a ledger,
// and the photo case was a comparison. This one's shape
// is a VOICE LEDGER. Notes and letters have started
// surfacing around town — in mailboxes, on the school
// board, tucked into a locker — each signed with
// somebody's name. Some of them are kind. Some are cruel
// in a way the signer would never be. The town's problem
// isn't the handwriting; nobody writes alike twice, and
// half the town's mail is written by somebody else's hand
// for honest reasons — a mother copying fair, a sister
// taking dictation, a voice box flattening a man into
// print. It's the VOICE. Every person in town sounds
// like themselves in a way the game has always known:
// their tics, their habits, the words they'd never say —
// the speech cards the town has carried since its second
// season. The investigation reads the letters against
// the voices they claim.
//
// The board is the ledger: six surfaced letters, each
// claiming a signer. For each letter the ledger DERIVES
// its voice findings from the letter's own text against
// the claimed signer's voice profile — markers present,
// the marks that must be there, the nevers violated, the
// studied tells — recomputed on every read, never stored
// as verdicts. Per letter the player commits a READING:
// IN VOICE (they wrote it), OFF VOICE (they didn't; it's
// a stranger's hand), or BORROWED VOICE (someone studied
// them and wrote AS them — the deliberate imitation,
// distinguishable from a stranger's miss by the markers
// it gets RIGHT). The three-reading shape is the case's
// signature, and the ledger's columns keep the three
// genuinely apart in both directions: a stranger's miss
// has none of the surface; a borrowed voice has the
// surface AND a tell; a true voice has the surface and
// nothing else. Readings are recorded; VERDICTS are
// derived, never stored — each letter has one piece of
// evidence that settles it, and a reading standing with
// its settling piece live is PROVEN, a reading standing
// against it is REFUTED (and can be struck and read
// again), anything else is COMMITTED, untested.
//
// Evidence: 12 pieces — 5 witness tellings, 5 Look
// finds, 2 participation beats — two tagged to every
// letter. Two pieces are CONTEXT-LOCKED: they go into
// the evidence set honestly inert (a form whose example
// sentences mean nothing out of company; a draft folder
// whose annotations need the study's week to read) and
// become spendable only once the chapter that unlocks
// their context has begun. The two locked pieces are the
// settling pieces of the two LOAD-BEARING letters — the
// borrowed pair, the case's heart: the letters that
// matter most can only be settled late. The suite proves
// it.
//
// The finale is the Open Letters night at City Hall,
// where the town decides what its mail will mean. The
// player makes one of four calls. READ THE LEDGER ALOUD
// (gated on the board's final derived state: both
// borrowed letters PROVEN, every letter read, none
// refuted, at least four proven), NAME THE BORROWED HAND
// (the town's own answer — somewhere there is one hand
// that studied everybody, and the town has a name warmed
// up for it; gated on at least one letter PROVEN), LET
// THE LETTERS STAND, or WALK AWAY. A closing nobody
// plays is its own landing: unopened. The game never
// admits a call was wrong; the letters just keep meaning
// what the town lets them mean.
//
// The red herring is honest, the way this town's are:
// the town DOES contain true voice oddities. A kind
// letter in a suspiciously neat hand is a son's words in
// his mother's fair copy, taken down at his dictation,
// word for word. A boy who never writes a clear word has
// his notes taken down in muffle marks by his sister —
// and both of those letters are exactly what their
// signatures claim. The obvious suspect-shaped answer is
// the kid who does everybody's voice on stage — the
// talent show's impressions act — and the evidence
// carries his innocence in plain sight: every voice he
// borrows, he borrows into a microphone, announced, with
// his own name under it. The letters in the lockers were
// not performed. They were filed.
//
// The solution, in one breath, for the payoffs only:
// four of the six letters are strangers' work in part or
// in whole, but only two of them are STUDIED. The cruel
// counselor's note and the posted ranking were written
// by one hand that had done its homework — the school's
// own list-keeper, the girl who governs by footnote and
// keeps handwriting in every margin: Jenny Simon. She
// studied the voices the way she studies everything, to
// file people correctly — the counselor's feelings filed
// as a discipline problem, the class filed top to
// bottom — and the cruelty in them is what her filing
// calls accuracy. Both letters carry her homework's
// fingerprints: the openings are perfect because she
// copied them from real models (the office's worksheet
// form; the debate style she has filed all year), and
// both carry the office forms' routing line, "pass it
// on," which neither claimed signer has ever written in
// their life — she copied the forms so faithfully she
// copied their furniture too. The apology in Cartman's
// name and the long warm letter in Craig's are strangers'
// work of the ordinary kind — kind fakes, written by
// hands that never studied anybody (a well-meaner with a
// bake-sale conscience; a new kid copying what warmth is
// supposed to sound like) — and the thank-you and the
// notebook are exactly what they claim: a dictated
// thank-you, fair-copied by a mother who tried to soften
// two words and was made to put them back, and a warning
// the muffled boy never wrote at all, in prose clearer
// than he has ever owned. The town's mail is mostly
// honest. It just needed reading in the right tense:
// whose voice, not whose hand.
//
// Structure:
//  - CHAPTERS11: four full ShenaniganDefs on the same
//    episode machinery, outside the random pool,
//    resolving in SHENANIGAN_BY_ID like every case.
//  - Gating: the case queues into the mystery slot TEN
//    days after the photo case retires. While any
//    earlier case is open this one does not even
//    schedule.
//  - The pen-pal wall: NOTHING in this case touches the
//    correspondence — no letter, piece, witness or
//    settling fact lives behind a signup, and the case
//    is fully playable and solvable-shaped on a save
//    that has never heard of a pen pal. The suite pins
//    the module's source clean of the correspondence
//    and drives the whole case on such a save.
//  - Retirement: every landing retires the arc forever.
//    The voiced landing alone gets the long tail: a week
//    on, two correction cards go up where the borrowed
//    letters were found — one line each, in the signers'
//    own true voices, written by themselves this time.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery11 {

    const val CASE_TITLE = "Signed in Another's Voice"
    const val FINALE11_ID = "arc11_ch4_open_letters"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the photo case retires

    val CHOICE_KEYS = listOf("voice", "name", "stand", "walk")
    const val DEFAULT_BRANCH = "unopened"
    val BRANCH_KEYS = listOf("voiced", "named", "standing", "unopened")

    const val POST_DELAY_DAYS = 7

    /** A reading, as the dialog and the save file name
     *  it: in voice (the signer's own), off voice (a
     *  stranger's hand — the signer didn't write it and
     *  nobody studied them), or borrowed voice (a studied
     *  imitation — the surface is right because somebody
     *  did their homework). The verdict is never the
     *  player's to pick — the evidence grades the
     *  reading. */
    val CALL_KEYS = listOf("in", "off", "borrowed")

    /** The routing phrase the school office's forms
     *  carry — derived wherever it surfaces. */
    const val ROUTING_PHRASE = "pass it on"

    // ---------------- the four chapters ----------------
    // They air a week apart in the player's present: the
    // surfacing (letters in mailboxes, on the board, in a
    // locker, and the town reading signatures instead of
    // voices), the fair copies (hands examined, voices
    // introduced — the dictated letter, the muffle
    // notebook, the stage mimic), the study (the records
    // a voice leaves when it writes for real: forms,
    // drafts, notebooks), and — in the finale — the Open
    // Letters, where the town decides what its mail
    // means.

    val CHAPTERS11: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc11_ch1_the_mail_slot",
            title = "The Mail Slot",
            cause = "It's the season the town's mail starts " +
                "arriving twice: once in the boxes, and once " +
                "in the talk about the boxes. A thank-you " +
                "appears in the lunch line in a hand too neat " +
                "for its signer. A warning goes up on the " +
                "school board in prose too clear for its. A " +
                "note is found in a locker, a ranking is " +
                "posted by the library, an apology is weighted " +
                "down with a bake-sale tray, and a letter long " +
                "enough to have chapters is taped to a " +
                "classroom door. Six letters, six signatures, " +
                "and by the second morning the town is not " +
                "asking what they say. The town is asking " +
                "whether the names under them are true.",
            blurb = "Nothing was stolen and nothing was " +
                "threatened, which is exactly why it spreads: " +
                "a letter is the one thing in this town that " +
                "arrives already believed. The names under " +
                "these six are the town's own — a counselor, " +
                "a debate captain, two of the boys, the " +
                "muffled one, the quiet one — and some of the " +
                "letters are kind, and some of them are cruel " +
                "in a way their signers would never be, and " +
                "every one of them is being read the way the " +
                "town reads everything: by the handwriting. " +
                "The investigation starts from the other end. " +
                "Not whose hand. Whose voice.",
            theme = QuestTheme("🎙", "#6e4a5a", "SIGNED IN ANOTHER'S VOICE"),
            tintHex = "#1c1216",
            stages = listOf(
                ShenaniganStage("Kickoff: Six Letters",
                    "🎙 THE SCHOOL BOARD, FIRST BELL: They are " +
                        "all in one place by morning because the " +
                        "principal insists on it — evidence " +
                        "should hang together, she says, the way " +
                        "it arrived: separately, and believed. " +
                        "The thank-you from the lunch line, in " +
                        "its suspiciously fair hand. The warning " +
                        "about the gym floor, in prose like a " +
                        "textbook's. The counselor's note, out " +
                        "of a locker. The ranking, posted by " +
                        "the library with the care of a girl " +
                        "posting grades. The apology, under a " +
                        "bake-sale tray. The long letter, taped " +
                        "at eye height, still warm from being " +
                        "read aloud in the hall, badly, twice. " +
                        "Six signatures. The hall reads them in " +
                        "silence, the way you read a name under " +
                        "a thing you didn't expect: twice, and " +
                        "then a third time, slower.",
                    "A signature is a claim, not a fact — that " +
                        "is the whole case in one sentence, and " +
                        "the town will spend three weeks learning " +
                        "it. Every person in this town sounds " +
                        "like themselves in ways that can be " +
                        "checked: the words they always use, the " +
                        "words they would never use, the shape " +
                        "their sentences take when nobody is " +
                        "making them perform. The board in your " +
                        "Journal has opened: six letters, six " +
                        "claimed voices, and for each one a " +
                        "reading you will have to commit to and " +
                        "defend: it's theirs, it's a stranger's, " +
                        "or somebody studied them and wrote AS " +
                        "them.",
                    "Read the six letters where they surfaced, Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading signatures twice. Reading voices once, carefully.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff has read the ranking four times and the counselor's note twice, and has begun reading its own memos in a new and suspicious way."))),
                ShenaniganStage("Escalation: The Town Reads Hands",
                    "🎙 MAIN STREET, BY THE BOXES: The town's " +
                        "first theory is the handwriting, because " +
                        "handwriting is the part you can point " +
                        "at. The thank-you is TOO NEAT, the " +
                        "street says — no fourth-grader writes " +
                        "like a wedding invitation. The warning " +
                        "is TOO CLEAR. The long letter is TOO " +
                        "LONG, and its warmth is itemized like a " +
                        "receipt. By noon there are handwriting " +
                        "experts at both ends of the street, " +
                        "which in this town means two people with " +
                        "magnifying glasses and one with a " +
                        "ruler, and the verdicts are arriving " +
                        "faster than the mail ever did.",
                    "The handwriting theory has one flaw, and " +
                        "the flaw is the town itself: nobody in " +
                        "South Park writes their own mail. " +
                        "Mothers copy fair. Sisters take " +
                        "dictation. A man with a voice box writes " +
                        "flat because the box is flat, and a " +
                        "five-year-old files the gang's denials " +
                        "in sentences better than her father's. " +
                        "Hands are borrowed in this town every " +
                        "day, openly, for love and for " +
                        "logistics. Voices are the part nobody " +
                        "can lend. The case will be read in the " +
                        "voices, or it will not be read at all.",
                    "Hear the handwriting experts out — then start with the voices.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Comparing loops and crossings. Pointing at a signature like it confessed.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have compared the ranking's handwriting to their own children's and cleared, at last count, nobody, including two children who cannot yet write."))),
                ShenaniganStage("Climax: The Borrowed Hand",
                    "🎙 CLIMAX: By the climax of the first week " +
                        "the street has upgraded its theory, and " +
                        "the upgraded theory has a shape: THE " +
                        "BORROWED HAND. Somewhere in town, the " +
                        "theory says, is one person who can sound " +
                        "like anybody — who has been practicing — " +
                        "and the six letters are their " +
                        "masterwork, and it is only a matter of " +
                        "time before the town agrees on who has " +
                        "been practicing loudest. Names are " +
                        "being tried out the way the town tries " +
                        "out capes: somebody who does voices. " +
                        "Somebody who was SEEN doing voices, on " +
                        "a stage, recently, to applause. The " +
                        "theory does not mention that a voice " +
                        "done on a stage is announced. Theories " +
                        "don't take questions. Theories take " +
                        "nominations.",
                    "A stage voice is a borrowed voice with " +
                        "its name tag on — that distinction is " +
                        "the hinge of this whole case, and the " +
                        "ledger will keep it in a column of its " +
                        "own: not just whose markers a letter " +
                        "carries, but how it carries them. A " +
                        "stranger's miss has none of the surface " +
                        "— no tics, no habits, just a signature " +
                        "and a hope. A studied voice has the " +
                        "surface cold — and one thing more, one " +
                        "place where the homework shows: a tic " +
                        "bent, a habit inverted, an ending the " +
                        "real voice has never once written. The " +
                        "ledger reads for all three. The town, " +
                        "for now, reads for one name.",
                    "Hear the Borrowed Hand theory out — then start with the letters.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Trying names on the theory. Whispering 'practicing' like it's a charge.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 1,
                            note = "The Boys' Crew has a suspect board already. It is a list of everyone who has ever done a voice, including the lunch lady, including the parrot at the pet store."))),
                ShenaniganStage("Resolution: The Board Is Open",
                    "🎙 The principal closes the week the way " +
                        "the case will close three weeks from " +
                        "now: on the record. The letters stay " +
                        "where they surfaced — nothing quietly " +
                        "taken down while the case is open, by " +
                        "her order, in pencil, which in this " +
                        "school is the binding kind. 'Read " +
                        "them,' she says, 'the way their signers " +
                        "would have to read them: out loud, in " +
                        "their own voices, and we'll see which " +
                        "ones they can get through.' The ledger " +
                        "is in your Journal — six letters, six " +
                        "claimed voices, their markers, their " +
                        "nevers, and the tells a studied voice " +
                        "leaves when it thinks nobody is checking " +
                        "its homework.",
                    "An open case is a kind of gift: it means " +
                        "the town will hold still while you work. " +
                        "The ledger is in your Journal — six " +
                        "letters, each with its claimed voice's " +
                        "card and the findings the comparison " +
                        "derives, each waiting for a reading. " +
                        "Start where telling can be done in " +
                        "person: the households that take the " +
                        "town's dictation.",
                    "Work the ledger: for each letter — theirs, a stranger's, or studied?",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Victoria", "Mackey"),
            episodeTitles = listOf("The Mail Slot", "Six Signatures"),
            coldOpen = listOf(
                Line("Victoria", "For the record — and this " +
                    "school runs on the record — six letters " +
                    "have surfaced in a fortnight with this " +
                    "town's names under them, and I have read " +
                    "every one of them the way I read a form: " +
                    "for the signature first and the sense " +
                    "second, which is exactly backwards, and " +
                    "exactly how the whole town is reading " +
                    "them. Some of these letters are kind. Some " +
                    "are cruel in a way their signers would " +
                    "not be at gunpoint. All of them are being " +
                    "judged by their handwriting, which in " +
                    "this school is the one thing I can prove " +
                    "means nothing. Half my correspondence is " +
                    "in my secretary's hand. It is still mine. " +
                    "Read the voices. The voices are the " +
                    "signatures that matter."),
                Line("Mackey", "I'd like my, uh, my concern " +
                    "noted, m'kay. There's a note going around " +
                    "with my name under it that talks about " +
                    "feelings like they're a discipline problem, " +
                    "m'kay, and I want it in the minutes that I " +
                    "have never in my career told a child to get " +
                    "their feelings FIXED, and I would like the " +
                    "record to further reflect that I am not " +
                    "angry about it. I am disappointed that " +
                    "there IS a record question, m'kay, but I am " +
                    "not angry. Those are different pamphlets."))),
        ShenaniganDef(
            id = "arc11_ch2_fair_copies",
            title = "Fair Copies",
            cause = "A week on, the case goes to the hands " +
                "themselves — honestly this time: the households " +
                "that take the town's dictation open their " +
                "doors, a mother produces a rough draft and a " +
                "fair copy and the whole story of both, a sister " +
                "produces a notebook of muffle marks four years " +
                "deep, and at the Bijou the stage still smells " +
                "of the talent show, where one kid did " +
                "everybody's voice to a standing ovation and " +
                "signed the program in his own.",
            blurb = "Examined closely, the town's mail refuses " +
                "to behave like a scandal: the neatest hand in " +
                "the case belongs to a mother copying her son's " +
                "words at his dictation, and the rough draft " +
                "under it — his spelling, his crossings-out — " +
                "says every word the fair copy says, pudding " +
                "and all. The flattest voice in town writes " +
                "flat because a voice box is flat, and his " +
                "letter is his. If any letter in this case was " +
                "studied rather than written, it will not be " +
                "the ones the town is pointing at. It never " +
                "is. That's not evidence. It's just the way " +
                "the pointing usually goes.",
            theme = QuestTheme("✉️", "#5a6e4a", "FAIR COPIES"),
            tintHex = "#141c12",
            stages = listOf(
                ShenaniganStage("Kickoff: The Draft Underneath",
                    "✉️ THE STOTCH HOUSEHOLD, BY PHONE AND " +
                        "DOORSTEP: The thank-you's defense is " +
                        "produced the way families produce " +
                        "everything: from a drawer, with " +
                        "layers. On top, the fair copy — the " +
                        "wedding-invitation hand the street has " +
                        "been convicting all week. Under it, " +
                        "the rough draft: a fourth-grader's " +
                        "pencil, three spellings of 'pudding,' " +
                        "and every single word the fair copy " +
                        "carries, in the same order, with the " +
                        "same tics standing in the same places. " +
                        "The words were his before the hand was " +
                        "hers. The drawer closes on the " +
                        "handwriting theory's best exhibit, and " +
                        "the drawer makes a sound like a gavel " +
                        "in a very small court.",
                    "This is the case's first lesson and its " +
                        "kindest: a borrowed HAND is not a " +
                        "borrowed voice. The town is full of " +
                        "fair copies — letters written by one " +
                        "person and authored, entirely, by " +
                        "another — and the ledger's job is to " +
                        "keep the two borrowings in separate " +
                        "columns forever. The thank-you's voice " +
                        "never left its signer. Only its " +
                        "penmanship went to his mother, the way " +
                        "penmanship has gone to mothers since " +
                        "the invention of the spelling test.",
                    "Read the draft under the fair copy — the voice predates the hand.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Producing drafts from drawers. Dating a voice by its crossings-out.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have begun producing their children's drafts preemptively, in drawers, at volume, whether or not the children have written anything."))),
                ShenaniganStage("Escalation: The Voice at the Bijou",
                    "✉️ THE BIJOU, BETWEEN SHOWS: The stage " +
                        "still holds the talent show's chalk " +
                        "marks, and the act the town keeps " +
                        "talking about is third from the top of " +
                        "the bill: the voices act — one kid, a " +
                        "stool, and eleven citizens performed in " +
                        "rotation to a standing ovation. The " +
                        "program is still taped by the box " +
                        "office, and the program is the exhibit: " +
                        "the act is billed under the performer's " +
                        "own name, the voices are billed as " +
                        "impressions, and the last line of the " +
                        "program, in the performer's own hand, " +
                        "is a thank-you to the audience for " +
                        "letting him borrow everybody for four " +
                        "minutes. Borrowed, announced, and given " +
                        "back. It is the most honest document in " +
                        "the case so far.",
                    "The town's theory needs a mimic, and the " +
                        "town has nominated the only mimic it " +
                        "can see — the one who does it in the " +
                        "open, for applause, with a sign on it. " +
                        "The ledger keeps a colder distinction: " +
                        "a performed voice is a borrowed voice " +
                        "with its name tag on, and a name tag is " +
                        "the one thing the locker letters do not " +
                        "have. Whoever wrote as the counselor and " +
                        "the captain — if anyone did — filed " +
                        "their work under other people's names " +
                        "and walked away from it. That is not a " +
                        "performance. That is the opposite of a " +
                        "performance, wearing one's coat.",
                    "Read the talent bill at the Bijou — the announced borrowings, in the open.",
                    routine = "cluster", clusterLoc = "Bijou",
                    clusterActivity = "Reading a program like a deposition. Applauding, then remembering not to.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("girls_circle", influenceDelta = 1,
                            note = "The Girls' Circle has reviewed the voices act retroactively and wishes it noted that the Wendy impression got the openings right and the endings wrong, which the Circle files under: interesting."))),
                ShenaniganStage("Climax: Taken Down in Muffle",
                    "✉️ THE McCORMICK HOUSEHOLD, KITCHEN TABLE: " +
                        "The notebook is four years deep and it " +
                        "smells of pencil. Every note the " +
                        "muffled boy has ever needed in writing " +
                        "is in it, in his sister's careful hand: " +
                        "muffle marks, translations in brackets, " +
                        "dates. Doctor's excuses. A birthday " +
                        "list. One (1) formal complaint, " +
                        "translated, about a cafeteria tray. " +
                        "What is not in it — what has never once " +
                        "been in it, in four years of being taken " +
                        "down — is a clear sentence. The warning " +
                        "on the school board is six clear " +
                        "sentences with a conclusion. The " +
                        "notebook closes like the drawer did, " +
                        "one room over and one case-column " +
                        "later: the voice in that warning never " +
                        "passed through this kitchen.",
                    "Dictation cuts both ways, and the ledger " +
                        "records both cuts: a voice can ride in " +
                        "another person's hand and stay itself — " +
                        "and a signature can be worn by a voice " +
                        "that has never once been in the family. " +
                        "The muffled boy's whole written life is " +
                        "muffle marks and brackets. The warning " +
                        "is none of that. Whatever hand wrote it, " +
                        "the voice is a stranger's — not studied, " +
                        "not performed, just wrong in the plain " +
                        "way, all the way down to its " +
                        "conclusion.",
                    "Read the dictation notebook at the McCormick house.",
                    routine = "cluster", clusterLoc = "KennysHouse",
                    clusterActivity = "Translating muffle marks aloud. Counting clear sentences: zero.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Boys' Crew has accepted the notebook as evidence and begun keeping its own minutes in the same format: muffle marks, brackets, total confidence."))),
                ShenaniganStage("Resolution: Hands Are Not Voices",
                    "✉️ The second week closes with the case's " +
                        "arithmetic changed: two of the town's " +
                        "oddest letters are its most honest ones, " +
                        "and the street knows it, which does not " +
                        "slow the street down even slightly. The " +
                        "Borrowed Hand theory survives its first " +
                        "contact with evidence the way theories " +
                        "do — by moving to a smaller room. What " +
                        "remains is the study: the records the " +
                        "voices themselves keep when they write " +
                        "for real. A counselor's forms. A debate " +
                        "captain's drafts. The homework, checked " +
                        "against the homework-doer.",
                    "Two letters are read and settled in the " +
                        "ledger now, in the columns where " +
                        "settling happens: by voice, against the " +
                        "card, with the settling piece in hand. " +
                        "Four remain. The next week's work is " +
                        "quieter and worse: not who holds the " +
                        "pen, but who did the reading — whose " +
                        "models the studied letters were studied " +
                        "FROM. Every voice in this town leaves " +
                        "its homework somewhere. Forms. Drafts. " +
                        "Fridge doors. The study reads them all.",
                    "Close the fair-copies week: hands in one column, voices in the other.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Linda", "Karen"),
            episodeTitles = listOf("Fair Copies", "Taken Down in Muffle"),
            coldOpen = listOf(
                Line("Linda", "Oh, hello! Before anyone asks " +
                    "Stephen, ask me — the handwriting is mine " +
                    "and the words are Butters', and those are " +
                    "both true at once, which I gather is the " +
                    "whole problem in this town this week. He " +
                    "stood right there and told it to me a " +
                    "sentence at a time, and I copied it fair " +
                    "because his hand was cramping from the " +
                    "rough draft, and I will tell this to anyone " +
                    "who knocks: I tried to soften two of his " +
                    "words. Make it sound nicer, I said. He " +
                    "made me put them back. Both of them. " +
                    "Standing up, in my kitchen, in an apron. " +
                    "Whatever that letter is, it is his, all " +
                    "the way to the pudding."),
                Line("Karen", "My brother says— well. My " +
                    "brother doesn't SAY, in writing, and that's " +
                    "kind of my whole job. Four years I've " +
                    "taken him down: muffle marks first, then " +
                    "the brackets, so grown-ups can read him. " +
                    "It's all in the notebook, every note he's " +
                    "ever needed. Excuses, lists, one formal " +
                    "complaint about a tray. Not one clear " +
                    "sentence in four years, because he doesn't " +
                    "have any. If a note with his name on it " +
                    "sounds like a textbook, it didn't come " +
                    "through this kitchen table. I know what " +
                    "came through this kitchen table. I wrote " +
                    "it down."))),
        ShenaniganDef(
            id = "arc11_ch3_the_study",
            title = "The Study",
            cause = "The third week goes to the records the " +
                "voices keep of themselves: the counselor's " +
                "tray of forms, its example sentences printed " +
                "for a decade of check-ins; the debate folder, " +
                "a captain's real letters annotated in her own " +
                "hand; a fridge door in a house across town " +
                "that holds the genuine article in magnet " +
                "letters and marker; and a quiet boy's whole " +
                "written output, which fits, as his father " +
                "testifies, on an index card.",
            blurb = "Studied voices are studied FROM " +
                "something, and the somethings are all still " +
                "here: a feelings worksheet whose example " +
                "sentences a whole school has been bored by " +
                "for years, and a debate draft whose " +
                "corrections only ever make their sentences " +
                "more themselves. Read beside the locker note " +
                "and the posted ranking, the models do what " +
                "models do: they show exactly where the copy " +
                "follows them — and the one place, in each " +
                "letter, where the copy walks off the model " +
                "and keeps walking. Homework can be checked. " +
                "This week, it gets checked.",
            theme = QuestTheme("📚", "#4a5a6e", "THE STUDY"),
            tintHex = "#12161c",
            stages = listOf(
                ShenaniganStage("Kickoff: The Form in the Tray",
                    "📚 THE COUNSELOR'S OFFICE, TRAY BY TRAY: " +
                        "The top tray holds the feelings " +
                        "worksheet — the form, printed the year " +
                        "the copier still worked properly, a " +
                        "decade of check-ins deep. Its example " +
                        "sentences are the office's liturgy: the " +
                        "opener, the check-in, the pamphlet " +
                        "cadence, and at the foot of the form, " +
                        "the office's routing line, printed to " +
                        "be copied by whoever routes it on. The " +
                        "form has been read aloud over the " +
                        "locker note, in the hall, by a " +
                        "volunteer, and the hall went quiet in " +
                        "the wrong places: the note follows the " +
                        "form the way a tracing follows a " +
                        "drawing — and then, at the threat, the " +
                        "tracing lifts off the page. There is " +
                        "no threat on any form the office has " +
                        "ever printed. The office checked. Twice. " +
                        "With a highlighter.",
                    "A studied voice is two documents, not " +
                        "one: the letter, and the model it was " +
                        "studied from. Until this week the ledger " +
                        "had only letters. Now it has a model — " +
                        "and a model settles the question a " +
                        "letter alone can't: not whether the " +
                        "counselor's note SOUNDS like him " +
                        "(it does; that's the problem) but " +
                        "whether it sounds like him the way he " +
                        "sounds, or the way his FORM sounds, " +
                        "with one line the form never wrote " +
                        "standing in the middle of it like a " +
                        "chair in a hallway.",
                    "Read the worksheet form in the counselor's tray, Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading a form aloud over a letter. Going quiet at the same line, in unison.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff has read the worksheet form against the note line by line and highlighted one sentence in two colors, which the Staff calls its whole report."))),
                ShenaniganStage("Escalation: The Routing Line",
                    "📚 CITY HALL, THE MAIL TABLE: The town's " +
                        "mail passes one table on its way to the " +
                        "boxes, and the table keeps the office's " +
                        "forms the way the tray does: in stacks, " +
                        "by year, with the routing line at the " +
                        "foot of every one. It is the week's " +
                        "small discovery and the case's large " +
                        "one, and it arrives the way large " +
                        "discoveries do in this town: as " +
                        "furniture. Two of the six letters carry " +
                        "the routing line as a postscript — the " +
                        "counselor's note and the posted " +
                        "ranking, the two letters the ledger's " +
                        "tells have been circling all week. " +
                        "Neither claimed signer routes anything. " +
                        "One of them has never written the phrase " +
                        "in her life, in any draft her folder " +
                        "holds. The phrase is office furniture. " +
                        "Somebody copied the furniture.",
                    "The ledger's columns fill in by " +
                        "themselves now: a shared phrase is a " +
                        "derived fact, not a theory — it is in " +
                        "two letters, on the page, checkable by " +
                        "anyone with eyes, and it is in neither " +
                        "signer's voice on any other page in " +
                        "town. It does not name a hand. It names " +
                        "a LIBRARY: whoever wrote those two " +
                        "letters had the office's forms open in " +
                        "front of them, and copied so faithfully " +
                        "they copied the furniture too. The " +
                        "study's question narrows from 'who can " +
                        "sound like anybody' to 'who reads the " +
                        "forms.' The town files that under " +
                        "progress and goes home to dinner.",
                    "Check the mail table's forms at City Hall — the routing line, in print.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Lining two letters up under one form. Reading the postscripts in a new tone.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 1,
                            note = "The Parents have checked their own mail for routing lines and found, to their visible relief, only bills."))),
                ShenaniganStage("Climax: The Letter, Read to Its Signer",
                    "📚 THE STUDY'S CLIMAX, THE LONG LETTER: The " +
                        "week's last exhibit cannot be read in a " +
                        "tray or a folder, so it is read aloud, " +
                        "in full, in its claimed signer's " +
                        "hearing — all three paragraphs of it, " +
                        "the pride, the hearts, the itemized " +
                        "warmth, in front of witnesses, with the " +
                        "whole study standing by to record the " +
                        "response. The response takes one word. " +
                        "The witnesses write it down in three " +
                        "separate notebooks, and afterward the " +
                        "three notebooks are compared, and they " +
                        "agree, which in this town is a kind of " +
                        "weather event. A boy does not answer " +
                        "his own three-paragraph letter with one " +
                        "word and a gesture. The letter's voice " +
                        "and the boy's voice are recorded, " +
                        "officially, as different species.",
                    "Some evidence is a document and some is a " +
                        "room: this is the room kind, and the " +
                        "ledger enters it in the participation " +
                        "column, where the case keeps the truths " +
                        "your own presence settled. The long " +
                        "letter was never a study — nobody " +
                        "home-worked the quiet boy; his markers " +
                        "are absent the way stars are absent at " +
                        "noon. It is a stranger's warmth, " +
                        "unsigned in everything but name. And " +
                        "with it read, the board has one week " +
                        "and one door left: the hall at City " +
                        "Hall, where the letters get opened in " +
                        "public and the town decides, out loud, " +
                        "what its mail means.",
                    "Be in the room when the long letter is read to its signer.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Reading three paragraphs aloud. Writing down one word, three times, identically.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("craigs_gang", influenceDelta = 2,
                            note = "Craig's Gang has compared the three notebooks and declared the response genuine, complete, and — the Gang's word — 'restful.'"))),
                ShenaniganStage("Resolution: Homework, Checked",
                    "📚 The study closes with the models filed " +
                        "beside the letters in the ledger, and " +
                        "the town in possession of a distinction " +
                        "it did not have in the spring: a voice " +
                        "can be copied the way a drawing is " +
                        "copied, off a model, and the copy can be " +
                        "caught the same way — not by the parts " +
                        "it gets right, but by the one part it " +
                        "improvises. One week remains. The " +
                        "letters will be opened at City Hall, in " +
                        "the hall, in front of everyone, and the " +
                        "town will decide what to do with what " +
                        "it can no longer unread: that two of " +
                        "its letters were homework, two were " +
                        "strangers, and two were its own mail " +
                        "all along.",
                    "Everything lookable has been looked at " +
                        "twice and every model has been laid " +
                        "beside its letter. Tonight the room " +
                        "hears one version of the mail: the " +
                        "signatures', or the ledger's. The six " +
                        "letters will be in the room either way, " +
                        "in the folder, in the order they " +
                        "surfaced. They have been the most " +
                        "patient witnesses in the case, and the " +
                        "only ones that never changed their text.",
                    "Close the study: the ledger is complete. The hall is next week.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Mackey", "Wendy"),
            episodeTitles = listOf("The Study", "Homework, Checked"),
            coldOpen = listOf(
                Line("Mackey", "I have read the worksheet " +
                    "against the note eleven times now, m'kay. " +
                    "Eleven. The opener is my opener. The " +
                    "check-in is my check-in. Whoever wrote it " +
                    "had my form open like a hymnal, m'kay, and " +
                    "then — the threat. There is no threat in " +
                    "my form. There is no threat in my whole " +
                    "filing cabinet. I checked the cabinet. I " +
                    "checked the drawer under the cabinet. If " +
                    "you are going to borrow a man's voice for " +
                    "a threat, m'kay, the least you can do is " +
                    "borrow the part where he doesn't make " +
                    "them."),
                Line("Wendy", "I keep my drafts. All of them, " +
                    "annotated — my corrections, my second " +
                    "thoughts, the sentences I made MORE " +
                    "precise, never less. So let the record " +
                    "show what my folder can prove and what it " +
                    "can't. It can prove my hand has never once " +
                    "written 'forget it' about anything I " +
                    "ranked, cited or believed, in any draft, " +
                    "ever. It can prove the ranking carries a " +
                    "postscript from an office form I have " +
                    "never routed in my life. What it can't " +
                    "prove is whose homework it was. That's " +
                    "the ledger's job. Do the ledger's job."))),
        ShenaniganDef(
            id = "arc11_ch4_open_letters",
            title = "Open Letters",
            cause = "The display committee of one season " +
                "becomes the mail committee of the next: the " +
                "mayor has called an open meeting at City Hall — " +
                "the Open Letters — where the six letters will " +
                "be produced in the order they surfaced, the " +
                "ledger will be in the room, and the town will " +
                "decide, in the minutes, what its signatures " +
                "mean. The chairs are set. The folder is on " +
                "the table. The routing line is in the " +
                "program, in small print, where the truth in " +
                "this town usually sits.",
            blurb = "It ends the way mail ends: in public, " +
                "or not at all. Tonight the town hears the " +
                "letters the way the ledger reads them — " +
                "voice by voice, markers, nevers, tells — or " +
                "it hears the name its theory has been warming " +
                "up for three weeks, or it lets every letter " +
                "stand exactly as signed, or the folder goes " +
                "home unopened in one person's hands. The " +
                "letters do not care. Letters are patient. It " +
                "is the town that has to live in the town the " +
                "letters leave behind.",
            theme = QuestTheme("📬", "#6e4a5a", "OPEN LETTERS"),
            tintHex = "#1c1216",
            stages = listOf(
                ShenaniganStage("Kickoff: The Folder on the Table",
                    "📬 CITY HALL, THE HALL SET FOR MAIL: The " +
                        "chairs are out in their meeting " +
                        "formation, the water glasses are poured, " +
                        "and on the table, in a folder the color " +
                        "of forms, the six letters lie in the " +
                        "order they surfaced: thank-you, warning, " +
                        "note, ranking, apology, the long one. " +
                        "The room fills the way rooms fill when " +
                        "the agenda is other people's names. The " +
                        "mayor opens the meeting the way she " +
                        "opens everything — 'People of South " +
                        "Park—' — and the room settles, and the " +
                        "folder is opened, and for the first " +
                        "time in three weeks the letters are the " +
                        "quietest things in the building.",
                    "Everything the case has gathered is in " +
                        "this room: the drafts, the notebook, " +
                        "the forms, the folder of annotations, " +
                        "the fridge's genuine article, the " +
                        "one-word response in three notebooks. " +
                        "The ledger is complete or it isn't; " +
                        "its verdicts were never going to be " +
                        "announced anywhere but here. The " +
                        "meeting will take one version of the " +
                        "mail home with it: the ledger's, the " +
                        "theory's, the signatures', or — if the " +
                        "person holding the board isn't in the " +
                        "room — the version applause writes by " +
                        "itself.",
                    "Take your seat at the Open Letters, City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Setting chairs for mail. Reading the program's small print.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff has brought its own folder to the Open Letters, labeled, tabbed, and — the Staff wishes it noted — signed in its own hand throughout."))),
                ShenaniganStage("Escalation: The Names in the Room",
                    "📬 THE HALL, BEFORE THE CALL: Every signer " +
                        "the letters claim is in the room, which " +
                        "is the meeting's whole difficulty: the " +
                        "counselor, not angry, disappointed, in " +
                        "the third row; the captain, with her " +
                        "folder of drafts on her knees like a " +
                        "shield; the muffled boy and his sister, " +
                        "the notebook between them; the quiet " +
                        "boy, who has brought one sentence and " +
                        "is saving it; the boy whose apology is " +
                        "an exhibit, eating a snack off the " +
                        "exhibit tray; and at the back, by the " +
                        "refreshments, the boy whose kind " +
                        "thank-you started the town reading its " +
                        "mail — apologizing, quietly, to a " +
                        "chair he bumped. The room looks at the " +
                        "folder. The folder, being mail, looks " +
                        "at nobody.",
                    "This is the part of the case no column " +
                        "holds: what the room DOES with a voice " +
                        "once it's proven borrowed. The ledger " +
                        "can settle six letters; it cannot settle " +
                        "the question the meeting was actually " +
                        "called for, which is whether a town " +
                        "that knows two of its letters were " +
                        "homework goes back to believing its " +
                        "mail — or starts reading every kind " +
                        "note twice, forever, the way this room " +
                        "is reading the thank-you now: as a " +
                        "suspect that happens to be innocent.",
                    "Sit with the signers — every name the letters claim, in one room.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Holding folders like shields. Saving one sentence. Apologizing to a chair.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have read the thank-you a third time during the meeting and are now, officially, just proud of it, which the Parents call the meeting's first verdict."))),
                ShenaniganStage("Climax: The Folder Is Open",
                    "📬 CLIMAX: The folder is open and the " +
                        "meeting is at the part meetings reach " +
                        "when the evidence is done being " +
                        "evidence and starts being a decision. " +
                        "The ledger is in the room — that was " +
                        "always the terms. What it says, letter " +
                        "by letter, in the town's own voices, is " +
                        "ready to be read aloud by the one " +
                        "person who holds the whole of it. The " +
                        "theory is also ready: it has a name, " +
                        "and the name is the one the town has " +
                        "been applauding at talent shows — the " +
                        "voice act, the mimic, the kid who can " +
                        "be anybody for four minutes at a time. " +
                        "The name is warm. The ledger is cold. " +
                        "The chair recognizes the person holding " +
                        "the board.",
                    "Four ways out of this room, and the " +
                        "board decides which ones exist: read " +
                        "the ledger aloud and the mail gets its " +
                        "voices back; name the borrowed hand " +
                        "and the room gets its name, warm and " +
                        "ready, whether the name can carry it " +
                        "or not; let the letters stand and the " +
                        "signatures keep every word, borrowed " +
                        "and true alike; or walk away, and the " +
                        "folder closes unopened, and the mail " +
                        "keeps the shape it arrived with. The " +
                        "letters will be opened tonight. The " +
                        "only question the meeting actually " +
                        "votes on is who opens them.",
                    "The chair recognizes you. The folder is open.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Being recognized by the chair. Holding a folder like it weighs something.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 1,
                            note = "The Boys' Crew has its suspect board at the meeting, updated nightly for three weeks. It has agreed to accept the meeting's result and keep the board anyway, for the archive."))),
                ShenaniganStage("Resolution: The Mail Goes Home",
                    "📬 The meeting adjourns the way it " +
                        "convened: on the record, with the " +
                        "minutes taking down whatever the room " +
                        "decided the mail means. The letters go " +
                        "back to their folder in the order they " +
                        "surfaced — the thank-you to its " +
                        "corkboard, the warning to its " +
                        "photograph, the note and the ranking to " +
                        "their keepers, the apology to its tray, " +
                        "the long letter to its door — and the " +
                        "town walks home past its own mailboxes, " +
                        "reading them, unavoidably, in the new " +
                        "tense: not whose hand. Whose voice.",
                    "The board keeps what the meeting does " +
                        "with it. It always did. The question " +
                        "the case leaves in the minutes is the " +
                        "one the mail slot asked in the first " +
                        "week — whether a name under a letter is " +
                        "a claim or a fact — and the answer the " +
                        "town walks home with is whichever one " +
                        "was read aloud, named, stood by, or " +
                        "left in the folder. Six letters. Six " +
                        "voices. One ledger. The mail goes home.",
                    "Walk home past the mailboxes.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Mackey"),
            episodeTitles = listOf("Open Letters", "Whose Voice"),
            coldOpen = listOf(
                Line("McDaniels", "People of South Park— this " +
                    "meeting will come to order, and it will " +
                    "stay in order, because the agenda is the " +
                    "mail, and the mail is the one thing this " +
                    "town still agrees to take seriously. Six " +
                    "letters. Six names. Three weeks of this " +
                    "town reading its own correspondence like " +
                    "evidence, which, I am reliably informed, " +
                    "it now is. We are here to decide what the " +
                    "signatures mean. The folder is on the " +
                    "table. The ledger is in the room. This " +
                    "town will now do the thing it does " +
                    "instead of reading: it will listen to " +
                    "somebody read."),
                Line("Mackey", "For the minutes, m'kay: I would " +
                    "like it recorded that whatever this " +
                    "meeting decides, the feelings worksheet " +
                    "stays in the tray, the check-ins continue, " +
                    "and nobody's feelings are a discipline " +
                    "problem, m'kay. Not before this case, not " +
                    "during, not after. That's the whole of my " +
                    "statement. It's also, not coincidentally, " +
                    "the whole of my actual note, the real " +
                    "one, which I never wrote, because the one " +
                    "with my name on it wrote itself out of my " +
                    "form. Read the ledger. Some of us would " +
                    "like our voices back.")))
    )

    val CHAPTER11_BY_ID: Map<String, ShenaniganDef> =
        CHAPTERS11.associateBy { it.id }

    fun chapterIndexOf11(id: String): Int =
        CHAPTERS11.indexOfFirst { it.id == id }

    fun isChapter11(id: String?): Boolean =
        id != null && id in CHAPTER11_BY_ID

    // ---------------- the voice profiles ----------------
    // One authored card per claimed signer, structured for
    // the ledger: the markers the voice always carries
    // (its tics, in the letter's own medium), the marks
    // that MUST be there (the voice cannot appear without
    // them), the nevers (phrases the signer would never
    // write — the card's own Never column, made checkable),
    // and the studied tells (the phrases a homework-doer
    // produces and the real voice never does: a tic bent,
    // a habit inverted, an ending never written). Every
    // list is drawn from the signer's SPEECH_RULES card in
    // CastData — the case invents no voice facts, it makes
    // the existing ones inspectable. The lists are
    // pairwise disjoint by construction: a phrase is a
    // marker, a never, or a tell, never two.

    data class VoiceProfile11(
        val signer: String,
        /** The tics and signature phrases the voice
         *  carries when it writes as itself. */
        val markers: List<String>,
        /** Subset of [markers] the voice cannot appear
         *  without — the load-bearing marks. */
        val must: List<String>,
        /** Phrases the signer would never write. */
        val nevers: List<String>,
        /** Phrases only a studied imitation produces:
         *  the surface bent against the voice's own
         *  habits. */
        val tells: List<String>,
        /** The card's habit line, in the board's words:
         *  how the voice behaves when nobody is making
         *  it perform. */
        val habit: String
    )

    val PROFILES11: List<VoiceProfile11> = listOf(
        VoiceProfile11("Butters",
            markers = listOf("well, gee", "fellas",
                "oh hamburgers", "neato"),
            must = listOf("fellas"),
            nevers = listOf("screw you", "i hate",
                "shut up", "stupid jerk"),
            tells = listOf("neato, huh", "gee whiz"),
            habit = "bright stammer; agrees first, worries " +
                "later; apologizes to furniture; never " +
                "mean, never sarcastic, never swears on " +
                "purpose"),
        VoiceProfile11("Kenny",
            markers = listOf("mmph", "mrrph", "(muffled)"),
            must = listOf("mmph"),
            nevers = listOf("perfectly clear",
                "in conclusion", "let me explain"),
            tells = listOf("(muffled) sincerely",
                "(muffled) thank you very much"),
            habit = "speaks ONLY in muffled mumbles, never " +
                "one clear word; taken down by others in " +
                "muffle marks and brackets, or not at all"),
        VoiceProfile11("Mackey",
            markers = listOf("m'kay", "feelings check-in",
                "how does that make you feel"),
            must = listOf("m'kay"),
            nevers = listOf("or else", "furious",
                "punishment"),
            tells = listOf("feelings are bad",
                "or else, m'kay"),
            habit = "turns any topic into a feelings " +
                "check-in and a pamphlet; never angry — " +
                "just disappointed, m'kay"),
        VoiceProfile11("Wendy",
            markers = listOf("actually—", "the record shows",
                "on principle"),
            must = listOf("actually—"),
            nevers = listOf("i dunno", "who cares",
                "too hard to explain"),
            tells = listOf("but forget it",
                "doesn't matter anyway",
                "whatever that means"),
            habit = "precise and prepared; cites facts " +
                "like a debate captain; never backs down " +
                "from a principle or plays dumb"),
        VoiceProfile11("Cartman",
            markers = listOf("respect my authoritah",
                "screw you guys", "big-boned", "you guys"),
            must = emptyList(),
            nevers = listOf("my fault", "i was wrong",
                "i'm sorry", "i apologize"),
            tells = listOf("respect my authoritah, please",
                "screw you guys, respectfully"),
            habit = "short bossy bursts; instant victim " +
                "act when losing; bribes with food; never " +
                "admits fault or being fat ('big-boned')"),
        VoiceProfile11("Craig",
            markers = listOf("...sure", "sure."),
            must = emptyList(),
            nevers = listOf("i'm so proud", "!!!",
                "from the bottom of my heart"),
            tells = listOf("...sure, and i mean that warmly",
                "whatever, and i care deeply"),
            habit = "one sentence where others use five; " +
                "never raises his voice; emotion leaks only " +
                "about Tweek or his guinea pig Stripe")
    )

    val PROFILE11_BY_SIGNER: Map<String, VoiceProfile11> =
        PROFILES11.associateBy { it.signer }

    // ---------------- the board's rows: six letters ----------------
    // A letter is a text and a claim: where it surfaced,
    // who keeps it, and the name under it. The letter's
    // fields are facts of the case; the FINDINGS are not
    // stored anywhere — each is the comparison of the
    // letter's own text against its claimed signer's
    // profile, recomputed wherever it's shown. What a
    // letter is FOR is the reading: whether its voice is
    // the signer's own, a stranger's, or a studied copy.

    data class Letter11(
        val id: String,
        /** Board order: the order the letters surfaced. */
        val index: Int,
        val title: String,
        /** Where the letter surfaced (a LOCATIONS id). */
        val place: String,
        /** Who keeps it now, as the town recorded it. */
        val keeper: String,
        /** The claim, in words: whose name is under it. */
        val claimLabel: String,
        /** The claimed signer (a cast name with a
         *  SPEECH_RULES card and a profile above). */
        val signer: String,
        /** The letter itself, verbatim — the board's
         *  whole evidence, readable by anyone. */
        val text: String,
        /** The evening as it actually ran (sealed): which
         *  reading this letter's evidence settles on. */
        val truth: String,
        /** The one piece that settles this letter. */
        val proofBy: String,
        /** 0-based chapter that must have BEGUN before
         *  this letter can be read (called). */
        val minChapter: Int,
        /** Live pieces tagged to this letter that a
         *  reading costs. */
        val callPrice: Int = 2
    )

    val LETTERS11: List<Letter11> = listOf(
        Letter11("letter_butters", 0, "THE THANK-YOU IN THE LUNCH LINE",
            "Elementary",
            "the lunch staff's corkboard, where it was pinned the day it surfaced",
            "signed 'Butters Stotch, 4th grade'",
            "Butters",
            "Well, gee — fellas in the lunch line, I just " +
                "wanted to say thank you proper. Oh " +
                "hamburgers, I wrote it messy first and my " +
                "mom copied it fair, but every word in it " +
                "is mine, includin' the pudding part. The " +
                "extra pudding Tuesday was neato and nobody " +
                "had to do that. Yours, Butters",
            truth = "in", proofBy = "linda_dictation",
            minChapter = 1),
        Letter11("letter_kenny", 1, "THE WARNING ON THE BOARD",
            "Elementary",
            "the school board by the office, photographed for the ledger before it faded",
            "signed 'Kenny McCormick'",
            "Kenny",
            "Let me be perfectly clear, since nobody else " +
                "will say it plainly. In conclusion: do not " +
                "cross the gym floor before Friday. I saw " +
                "what the wax did to the last kid who " +
                "tried. This is not a joke and I am not " +
                "laughing. — Kenny",
            truth = "off", proofBy = "karen_dictation",
            minChapter = 1),
        Letter11("letter_mackey", 2, "THE COUNSELOR'S NOTE",
            "Elementary",
            "the locker it was left in, and the boy who found it there",
            "signed 'Mr. Mackey, Guidance'",
            "Mackey",
            "We need to talk about your feelings, m'kay. " +
                "Feelings are bad when they take up the " +
                "whole class's time, m'kay. A feelings " +
                "check-in is not a hallway event. Get them " +
                "fixed by Friday — or else, m'kay. P.S. " +
                "Pass it on. — Mr. Mackey",
            truth = "borrowed", proofBy = "worksheet_form",
            minChapter = 2),
        Letter11("letter_wendy", 3, "THE RANKING, POSTED",
            "Elementary",
            "the board outside the library, where it was posted and left standing",
            "signed 'Wendy Testaburger'",
            "Wendy",
            "Actually— the record shows exactly who " +
                "carried the science fair, and it was not " +
                "half the names on this list. On principle, " +
                "I ranked the whole class, top to bottom, " +
                "and posted it so we'd all know. But forget " +
                "it. It doesn't matter anyway if nobody " +
                "reads it. Whatever that means — the top " +
                "three already know. P.S. Pass it on. — Wendy",
            truth = "borrowed", proofBy = "debate_draft",
            minChapter = 2),
        Letter11("letter_cartman", 4, "THE APOLOGY",
            "Elementary",
            "the bake-sale table, where it was found weighted down with a tray",
            "signed 'Eric Cartman'",
            "Cartman",
            "You guys. About the bake sale money. It was " +
                "my fault. I was wrong about where it went " +
                "and I'm sorry. I apologize to the whole " +
                "table, and to the jar. — Eric",
            truth = "off", proofBy = "liane_apology",
            minChapter = 1),
        Letter11("letter_craig", 5, "THE LONG LETTER",
            "Elementary",
            "the classroom door, where it was taped at eye height",
            "signed 'Craig Tucker'",
            "Craig",
            "I have been thinking about this class for a " +
                "long time, and I want everyone to know: " +
                "I'm so proud of every single one of you!!! " +
                "From the bottom of my heart, your feelings " +
                "are the most important thing in this " +
                "school, and I have written them all down " +
                "in my notebook so I never forget a single " +
                "one. We are a family. I will be checking " +
                "in on each of you, warmly, at length, " +
                "forever. — Craig",
            truth = "off", proofBy = "craig_reaction",
            minChapter = 2)
    )

    val LETTER11_BY_ID: Map<String, Letter11> =
        LETTERS11.associateBy { it.id }

    // ---------------- THE VOICE COMPARISON (derived, never stored) ----------------
    // The findings below are pure functions of a letter's
    // text and its claimed signer's profile: the scan is
    // mechanical and inspectable — a phrase is either in
    // the letter or it isn't, and the board prints the
    // lists it counted. Nothing about a voice is ever
    // stored; the ledger re-reads the letters every time
    // you open it.

    data class Findings11(
        /** Profile markers found in the letter's text. */
        val markersHit: List<String>,
        /** Must-marks found in the letter's text. */
        val mustHit: List<String>,
        /** Must-marks the letter is missing. */
        val mustMissed: List<String>,
        /** The signer's nevers, found in the letter. */
        val neversHit: List<String>,
        /** Studied tells found in the letter. */
        val tellsHit: List<String>
    )

    /** The ledger's scan of one letter against its
     *  claimed signer's profile. Pure: the same letter
     *  scans the same way forever. */
    fun findings11(l: Letter11): Findings11 {
        val p = PROFILE11_BY_SIGNER[l.signer]
            ?: return Findings11(emptyList(), emptyList(),
                emptyList(), emptyList(), emptyList())
        val t = l.text.lowercase()
        return Findings11(
            markersHit = p.markers.filter { it in t },
            mustHit = p.must.filter { it in t },
            mustMissed = p.must.filter { it !in t },
            neversHit = p.nevers.filter { it in t },
            tellsHit = p.tells.filter { it in t })
    }

    /** The shape a letter's findings take, derived: "in"
     *  (the surface is the signer's, with nothing foreign
     *  in it), "borrowed" (the surface is studied — the
     *  markers are RIGHT, and a tell stands in them),
     *  "off" (the surface is absent — a stranger's hand,
     *  however the signature reads), or "mixed" (the
     *  counts contradict every clean shape, and the
     *  ledger says so rather than guessing). The three
     *  clean shapes are kept genuinely apart in both
     *  directions: borrowed is told from "in" by the
     *  tell alone, and from "off" by the markers alone —
     *  a letter cannot drift from one shape to another
     *  without a count changing on the page. */
    fun signature11(l: Letter11): String {
        val f = findings11(l)
        return when {
            f.tellsHit.isNotEmpty() && f.markersHit.size >= 2 ->
                "borrowed"
            f.tellsHit.isEmpty() && f.neversHit.isEmpty() &&
                f.markersHit.size >= 2 && f.mustMissed.isEmpty() ->
                "in"
            f.tellsHit.isEmpty() && f.markersHit.size <= 1 ->
                "off"
            else -> "mixed"
        }
    }

    /** Whether the letter carries the office forms'
     *  routing phrase — a derived cross-letter datum the
     *  board prints where it finds it: office furniture,
     *  in a letter that claims a bedroom desk. */
    fun hasRouting11(l: Letter11): Boolean =
        ROUTING_PHRASE in l.text.lowercase()

    // ---------------- the evidence ----------------

    enum class Kind11 { WITNESS, LOOK, PARTICIPATION }

    data class Clue11(
        val id: String,
        val kind: Kind11,
        /** 0-based chapter that must have BEGUN for this
         *  evidence to exist in the world. */
        val minChapter: Int,
        /** The letter (index) this piece is tagged to —
         *  the row of the ledger it speaks to, however
         *  late it is found. */
        val site: Int,
        /** 0-based chapter that must have BEGUN for this
         *  piece to be LIVE — spendable, settling.
         *  Equals [minChapter] for ordinary pieces. For
         *  the two context-locked pieces it is a LATER
         *  chapter: the piece can be held, honestly
         *  inert, before the chapter that gives it its
         *  context. */
        val unlockChapter: Int,
        val title: String,
        /** The board's account of the piece once LIVE. */
        val detail: String,
        /** The board's account while held-but-inert. For
         *  ordinary pieces this equals [detail] (they
         *  are never seen inert). For locked pieces it is
         *  the honest shrug: what the thing IS, without
         *  what it MEANS. */
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

    val CLUES11: List<Clue11> = listOf(
        // ---------- letter 0: the thank-you ----------
        Clue11("draft_under", Kind11.LOOK, 0, 0, 0,
            "The rough draft, underneath",
            "The draft under the fair copy, out of the " +
                "kitchen drawer: a fourth-grader's pencil, " +
                "three spellings of 'pudding,' and every " +
                "word the fair copy carries — the tics in " +
                "the same places, the thanks in the same " +
                "order, the crossings-out where the writer " +
                "changed his mind about a word and changed " +
                "it back. The voice was complete in the " +
                "draft, in the signer's own hand, before " +
                "any fair copy existed. Whatever the neat " +
                "hand is, it arrived AFTER the voice did, " +
                "and changed nothing but the spelling.",
            "The rough draft, underneath",
            locId = "Elementary",
            foundText = "✉️ THE ROUGH DRAFT: under the " +
                "fair copy, in the drawer: pencil, three " +
                "spellings of 'pudding,' and the whole " +
                "letter already there — every word the neat " +
                "version says, in the messy hand of the boy " +
                "whose name is under it."),
        Clue11("linda_dictation", Kind11.WITNESS, 0, 0, 0,
            "The fair copyist (Linda)",
            "The copyist herself, on the record: the " +
                "hand is hers and the words are her son's, " +
                "taken down at his dictation a sentence at " +
                "a time because his hand was cramping from " +
                "the draft — and the part she tells twice, " +
                "to be sure it lands: she tried to soften " +
                "two of his words and he made her put them " +
                "back, standing up, in an apron. A dictated " +
                "letter is the town's oldest mail: the hand " +
                "belongs to whoever holds the pen, and the " +
                "voice belongs to whoever's standing in the " +
                "kitchen saying the sentences. The voice in " +
                "the thank-you never left its signer. Only " +
                "the penmanship went home to his mother.",
            "The fair copyist (Linda)",
            npc = "Linda",
            spoken = "Oh, hello! Ask me before you ask " +
                "Stephen — the handwriting is mine and the " +
                "words are Butters', and both of those are " +
                "true at the same time, which I gather is " +
                "the week's whole puzzle. He dictated it a " +
                "sentence at a time, right in my kitchen, " +
                "because the rough draft cramped his hand. " +
                "I tried to soften two of his words — a " +
                "mother does that, it's in the job — and he " +
                "made me put them back. Both of them. If " +
                "that letter sounds like my son, it's " +
                "because it is my son, at dictation speed, " +
                "with a witness. The witness is me. The " +
                "words are his."),
        // ---------- letter 1: the warning ----------
        Clue11("kenny_notebook", Kind11.LOOK, 0, 1, 0,
            "The dictation notebook",
            "Four years of a muffled boy's written life, " +
                "taken down at a kitchen table in his " +
                "sister's careful hand: muffle marks first, " +
                "the translations in brackets after — " +
                "doctor's excuses, a birthday list, one " +
                "formal complaint about a cafeteria tray. " +
                "What the notebook does not contain, in four " +
                "years, is one clear sentence: the boy has " +
                "never owned one, in any medium, at any " +
                "age. The warning on the school board is " +
                "six clear sentences with a conclusion. " +
                "Whatever the notebook is evidence OF, it " +
                "is this: the warning's voice never passed " +
                "through this kitchen.",
            "The dictation notebook",
            locId = "KennysHouse",
            foundText = "✉️ THE DICTATION NOTEBOOK: four " +
                "years of muffle marks and bracketed " +
                "translations, in a sister's hand — every " +
                "note the boy has ever needed, and not one " +
                "clear sentence in the book."),
        Clue11("karen_dictation", Kind11.WITNESS, 0, 1, 0,
            "The dictation-taker (Karen)",
            "The boy's own sister, who has taken his " +
                "dictation for four years and keeps the " +
                "notebook to prove it: when her brother " +
                "needs a thing in writing it goes through " +
                "her, muffle marks and all — and the warning " +
                "on the school board did not go through " +
                "her, or through any hand in that house. " +
                "Her telling settles the letter's shape the " +
                "way only a dictation-taker's can: she knows " +
                "exactly what her brother's voice looks like " +
                "on paper, because she is the one who puts " +
                "it there. It looks like the notebook. The " +
                "warning looks like a textbook. The two have " +
                "never met.",
            "The dictation-taker (Karen)",
            npc = "Karen",
            spoken = "My brother says— well, that's the " +
                "thing, he doesn't SAY, in writing. I take " +
                "him down. Four years: muffle marks, then " +
                "the brackets, so people can read him. It's " +
                "all in the notebook. Excuses, lists, one " +
                "whole complaint about a tray, which he " +
                "dictated VERY seriously. What he has never " +
                "dictated, not once, is a paragraph that " +
                "sounds like a book. Kenny doesn't HAVE " +
                "clear sentences. He has mmphs, and he has " +
                "me. That warning on the board? Clear as a " +
                "bell, with a conclusion. It didn't come " +
                "from my brother, and it didn't come " +
                "through this kitchen. I would have the " +
                "muffle marks for it. I don't."),
        // ---------- letter 2: the counselor's note ----------
        Clue11("worksheet_form", Kind11.LOOK, 1, 2, 2,
            "The feelings worksheet form",
            "The form from the counselor's tray, read at " +
                "last with the study's week open beside it — " +
                "and the form gives the note its context: " +
                "the note's whole skeleton IS this form. The " +
                "opener is the form's example opener. The " +
                "check-in cadence is the form's printed " +
                "cadence. Even the borrowed catchphrase sits " +
                "where the form's examples put it. And then " +
                "the tracing lifts off the page: the threat " +
                "is on NO form the office has ever printed — " +
                "the office checked its whole cabinet, " +
                "twice, with a highlighter. Whoever wrote " +
                "the note studied this form the way you copy " +
                "a drawing — and at the one line that " +
                "matters, the cruelty, they improvised. The " +
                "surface is homework. The threat is the " +
                "homework-doer's own. And at the note's " +
                "foot, copied as faithfully as everything " +
                "else: the form's own routing line, which " +
                "the counselor has never once written at " +
                "the end of a note in his life. The study " +
                "copied the furniture too.",
            "A form out of the counselor's tray: the " +
                "feelings worksheet, printed years ago, its " +
                "example sentences worn smooth by a decade " +
                "of check-ins. You can read every word of " +
                "it and learn nothing yet: a form is a " +
                "form, and what it means depends entirely " +
                "on the company it is read in. Its company " +
                "is coming.",
            locId = "Elementary",
            foundText = "📚 A FORM IN THE TRAY: the " +
                "feelings worksheet — example opener, " +
                "example check-in, a routing line at the " +
                "foot. A decade old and read by everybody. " +
                "It doesn't mean anything yet. Forms wait " +
                "for their company, like everyone else in " +
                "this case."),
        Clue11("clyde_locker", Kind11.WITNESS, 1, 2, 1,
            "The locker it was left in (Clyde)",
            "The boy who found the note in his locker, " +
                "with his interest declared — it was left " +
                "for HIM: the counselor whose name is under " +
                "it has had a hundred chances to threaten " +
                "him, across a hundred check-ins, and has " +
                "threatened him exactly never — disappointed " +
                "in him, yes, with a pamphlet; threatened, " +
                "no, not once. And the part the boy adds on " +
                "his own, unasked, because the town's theory " +
                "is bothering him: the kid who does the " +
                "voices does them out loud, on a stage, with " +
                "his name under the act in the program. " +
                "Whoever left this in a locker wasn't " +
                "performing. They were filing. There's a " +
                "difference, and the difference is the case.",
            "The locker it was left in (Clyde)",
            npc = "Clyde",
            spoken = "*sniff* Okay. Okay, so — it was in " +
                "MY locker, so I get to say. Mr. Mackey has " +
                "had, like, a hundred chances to threaten " +
                "me. A HUNDRED. I'm not — I'm not crying, " +
                "I'm just saying it with my whole chest. " +
                "He's been disappointed in me with a " +
                "PAMPHLET. He has never once said 'or else' " +
                "to me, m'kay or no m'kay, and I would KNOW, " +
                "because threats are the kind of thing I " +
                "remember — I remember everything people " +
                "say to me, it's my whole problem. And the " +
                "voices kid? He does them on a STAGE. With " +
                "a PROGRAM. This wasn't a performance. " +
                "Performances don't go in lockers. This was " +
                "filed. *sniff* I'm not crying."),
        // ---------- letter 3: the ranking ----------
        Clue11("debate_draft", Kind11.LOOK, 2, 3, 3,
            "The debate folder",
            "The captain's own folder, read at last in " +
                "the Open Letters' week, when its company " +
                "has arrived — her real letters, annotated " +
                "in her own hand: the drafts of everything " +
                "she has actually sent, argued or posted in " +
                "a year. The folder settles the ranking the " +
                "way only a record of the genuine article " +
                "can: her corrections make her sentences " +
                "MORE precise, never less; her principles " +
                "are held to the last line of every draft, " +
                "including the drafts she threw away; and " +
                "the move the ranking makes in its middle — " +
                "the spine-drop, the 'forget it' — is a move " +
                "her hand has never made in any draft the " +
                "folder holds. Whoever wrote the ranking " +
                "studied her openings. The folder is the " +
                "proof they never reached her endings. And " +
                "one thing more, small and cold: the " +
                "ranking carries the office forms' routing " +
                "line as a postscript. Her drafts carry no " +
                "postscripts. She has never routed anything " +
                "in her life.",
            "A folder of the debate captain's real " +
                "letters, annotated in her own hand — " +
                "corrections, second thoughts, drafts kept " +
                "and drafts discarded. You can read every " +
                "annotation and learn nothing yet: a draft's " +
                "evidence is in what it is read AGAINST, " +
                "and what it's read against hasn't all " +
                "surfaced. Yet.",
            locId = "Elementary",
            foundText = "📚 THE DEBATE FOLDER: a year of " +
                "real letters, annotated in the writer's " +
                "own hand — every correction accounted " +
                "for. It doesn't settle anything yet: " +
                "drafts, like forms, tell their story in " +
                "company."),
        Clue11("ranking_papers", Kind11.PARTICIPATION, 2, 3, 2,
            "The second copy, in the papers",
            "Sorting the debate club's papers during the " +
                "study — sign-up sheets, rebuttal notes, a " +
                "year of other people's arguments — your " +
                "hands find the ranking's twin: a second " +
                "copy of the posted ranking, folded small, " +
                "the way a thing is folded to fit a locker. " +
                "It was never delivered. Whoever made the " +
                "ranking made it more than once: it was a " +
                "production, with copies, not an impulse " +
                "with a pen. Productions have producers. " +
                "Impulses have authors. The ledger files " +
                "the ranking, on this evidence and its " +
                "settler's, under productions.",
            "The second copy, in the papers",
            chapterDefId = "arc11_ch3_the_study"),
        // ---------- letter 4: the apology ----------
        Clue11("liane_apology", Kind11.WITNESS, 0, 4, 0,
            "The signer's mother (Liane)",
            "The mother of the boy whose name is under " +
                "the apology, with a mother's whole archive " +
                "behind her: her son has never apologized " +
                "in writing in his life. He has barely " +
                "apologized out loud, and never in the " +
                "apology's actual words — 'my fault' and 'I " +
                "was wrong' are sentences the boy has never " +
                "produced in any medium, at any age, under " +
                "any pressure including holidays. A stranger " +
                "reading the apology reads a nice boy " +
                "owning a mistake. His mother reads a " +
                "forgery of a nice boy — the niceness is the " +
                "tell: it is layered on in exactly the " +
                "places where her son would have put a " +
                "demand, a bribe, or a scapegoat. The " +
                "apology is kind. Her son's kindness, when " +
                "it exists, comes with a price tag and a " +
                "witness protection program.",
            "The signer's mother (Liane)",
            npc = "Liane",
            spoken = "Oh, sweetie. Oh, no, no — let me " +
                "save you the shoe leather. My little Eric " +
                "did not write that apology. My little Eric " +
                "has never apologized in writing in his " +
                "life — he has never apologized OUT LOUD " +
                "without a snack attached, and there is no " +
                "snack attached to that note, I checked the " +
                "tray. 'My fault'? 'I was wrong'? Sweetie, " +
                "Eric has never said those words in that " +
                "order in any room in this town, and I have " +
                "been in most of the rooms. Whoever wrote " +
                "it meant well, probably. People usually " +
                "mean well right before they forge my son. " +
                "Would you like a cookie? You look like " +
                "someone reading forgeries on an empty " +
                "stomach."),
        Clue11("fridge_note", Kind11.LOOK, 0, 4, 0,
            "The genuine article, on the fridge",
            "The real written output of the apology's " +
                "claimed signer, held under magnets in his " +
                "own kitchen: a note demanding snacks in a " +
                "hand like a ransom, a second note blaming " +
                "the first note's demands on his mother, a " +
                "pre-written defense of his bones, and a " +
                "shopping list in which four of six items " +
                "are the same item. The voice is unmistakable " +
                "and it is nothing like the apology's: bossy " +
                "in bursts, wounded at rest, faultless at " +
                "all times. The fridge is the boy's whole " +
                "published works. The apology is not among " +
                "them, in any sense the word 'among' has.",
            "The genuine article, on the fridge",
            locId = "CartmanHouse",
            foundText = "✉️ THE GENUINE ARTICLE: the " +
                "fridge door of the apology's signer — " +
                "demands, blame, a pre-written defense of " +
                "his bones, and not one apology in the " +
                "whole published works."),
        // ---------- letter 5: the long letter ----------
        Clue11("craig_reaction", Kind11.PARTICIPATION, 2, 5, 2,
            "The letter, read to its signer",
            "At the study's climax the long letter is " +
                "read aloud in full — all three paragraphs, " +
                "the pride, the hearts, the itemized warmth " +
                "— in its claimed signer's hearing, in front " +
                "of witnesses, and his entire response is " +
                "recorded verbatim in three separate " +
                "notebooks, which agree: one word and the " +
                "finger. A boy does not answer his own " +
                "three-paragraph letter with one word and a " +
                "gesture. The letter's voice and the boy's " +
                "voice are, on this witnessed evidence, " +
                "different species — and the letter's warmth " +
                "is no study either: nobody did homework on " +
                "the quiet boy. His markers are absent the " +
                "way stars are absent at noon. It is a " +
                "stranger's warmth, signed.",
            "The letter, read to its signer",
            chapterDefId = "arc11_ch3_the_study"),
        Clue11("tucker_words", Kind11.WITNESS, 1, 5, 1,
            "The signer's father (Mr. Tucker)",
            "The quiet boy's father, at factory settings: " +
                "the family's whole written output for a " +
                "month fits on one index card, and he has " +
                "brought the card. His son's longest letter " +
                "ever was three words, addressed to the " +
                "guinea pig, and it was considered a " +
                "filibuster. As for the town's other " +
                "oddity — the children copying their " +
                "parents' phrases — he testifies to that " +
                "too, without being asked: his daughter " +
                "files the gang's denials in full sentences " +
                "she learned at his table, and it is " +
                "innocent, and it is exactly how voices " +
                "travel in a family. The long letter did " +
                "not travel from his house. His house does " +
                "not have that much ink.",
            "The signer's father (Mr. Tucker)",
            npc = "Mr. Tucker",
            spoken = "*raises one finger* …The card. " +
                "Family's whole written output, one month. " +
                "Fits on it. Son's longest letter ever: " +
                "three words. To the guinea pig. Considered " +
                "a filibuster. …The three-paragraph one " +
                "with the hearts isn't his. House doesn't " +
                "have that much ink. …That's the whole " +
                "statement.")
    )

    val CLUE11_BY_ID: Map<String, Clue11> = CLUES11.associateBy { it.id }
    val CLUE11_IDS: Set<String> = CLUES11.map { it.id }.toSet()

    fun clueCount11(state: GameState): Int = state.mystery11Clues.size

    /** Evidence exists in the world once its chapter has
     *  begun and the case is still open. */
    fun clueAvailable11(state: GameState, clue: Clue11): Boolean =
        !state.arc11Retired && state.arc11Chapter > clue.minChapter

    /** A piece is LIVE — spendable, settling — when it is
     *  in hand AND the chapter that unlocks its context
     *  has begun. Ordinary pieces are live the moment they
     *  exist. The two locked pieces sit in hand, honestly
     *  inert, until their context arrives: they settle
     *  nothing, prove nothing. Derived, never stored. */
    fun clueLive11(state: GameState, clue: Clue11): Boolean =
        clue.id in state.mystery11Clues &&
            state.arc11Chapter > clue.unlockChapter

    /** The ids of everything currently spendable. */
    fun liveClueIds11(state: GameState): Set<String> =
        CLUES11.filter { clueLive11(state, it) }.map { it.id }.toSet()

    /** Live pieces tagged to one letter — the only
     *  currency a reading can be bought with. */
    fun liveEvidence11(state: GameState, l: Letter11): Int =
        CLUES11.count { it.site == l.index && clueLive11(state, it) }

    /** Grant a piece of evidence exactly once. The ticker
     *  + the town log hear about it; the ledger keeps it
     *  forever. A locked piece announces its own
     *  unreadability — honestly, at the moment of finding,
     *  so no player ever mistakes holding for knowing. */
    fun grantClue11(state: GameState, clueId: String): Boolean {
        val clue = CLUE11_BY_ID[clueId] ?: return false
        if (clueId in state.mystery11Clues) return false
        state.mystery11Clues.add(clueId)
        val live = clueLive11(state, clue)
        state.events.add(if (live)
            "🧩 EVIDENCE ${state.mystery11Clues.size}/${CLUES11.size}: ${clue.title}. " +
                "(Journal → 🔍 MYSTERY)"
        else
            "🧩 IN HAND, NOT YET READABLE (${state.mystery11Clues.size}/${CLUES11.size}): " +
                "${clue.title}. It doesn't make sense yet — its context is still " +
                "coming, and in this case context is a kind of voice.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, if (live) "🧩 Evidence proven: ${clue.title}"
            else "🧩 Evidence in hand, not yet readable: ${clue.title}")
        return true
    }

    // ---------------- discovery: witnesses, looks, hands ----------------

    /** Talk-to discovery: if [npcName] is a live witness
     *  right now, their line — and the evidence it grants. */
    fun witnessLine11(state: GameState, npcName: String): Line? {
        val clue = CLUES11.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable11(state, clue)) return null
        if (!grantClue11(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId]
     *  reports the first time it finds each piece of
     *  evidence there — one at a time, in the order the
     *  case has opened them. A locked piece found early
     *  reports its inert face, and only its inert face:
     *  what it IS, never what it means. */
    fun lookClueText11(state: GameState, locId: String): String? {
        val clue = CLUES11.firstOrNull {
            it.kind == Kind11.LOOK && it.locId == locId &&
                it.id !in state.mystery11Clues &&
                clueAvailable11(state, it)
        } ?: return null
        if (!grantClue11(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter.
     *  The study's climax carries TWO pieces on one turf (the
     *  second copy in the papers; the letter read to its
     *  signer), so every matching piece is offered, not just
     *  the first — participation lands more than once in a
     *  climax, and each landing can surface one more. */
    fun noteParticipation11(state: GameState, defId: String) {
        for (clue in CLUES11) {
            if (clue.kind != Kind11.PARTICIPATION ||
                clue.chapterDefId != defId) continue
            if (!clueAvailable11(state, clue)) continue
            if (grantClue11(state, clue.id)) {
                state.events.add("🧩 Up close, something clicks: ${clue.title}")
                while (state.events.size > 40) state.events.removeAt(0)
            }
        }
    }

    // ---------------- THE LEDGER (derived, never stored) ----------------
    // The only stored facts are the readings themselves
    // and the evidence in hand. Every verdict below is a
    // pure function of those recorded inputs, recomputed
    // on every read — the audit case's ledger discipline,
    // applied to voices: a letter can't be argued with,
    // only re-read.

    /** A letter's verdict, derived: OPEN (unread),
     *  COMMITTED (read, standing, untested — its settling
     *  piece is not live), PROVEN (read as the letter
     *  actually ran, with the settling piece live in
     *  hand), or REFUTED (read against the letter, with
     *  the settling piece live — the reading stands
     *  struck and can be read again). A standing reading
     *  cannot be re-read; a refuted one must be, before
     *  the ledger can settle. */
    fun letterVerdict11(state: GameState, l: Letter11): String {
        val called = state.arc11Calls[l.id] ?: return "OPEN"
        if (l.proofBy !in liveClueIds11(state)) return "COMMITTED"
        return if (called == l.truth) "PROVEN" else "REFUTED"
    }

    fun calledCount11(state: GameState): Int =
        LETTERS11.count { it.id in state.arc11Calls }

    fun provenCount11(state: GameState): Int =
        LETTERS11.count { letterVerdict11(state, it) == "PROVEN" }

    fun refutedCount11(state: GameState): Int =
        LETTERS11.count { letterVerdict11(state, it) == "REFUTED" }

    /** Every letter has a reading written on it —
     *  standing, proven, or even refuted. (The finale's
     *  own gates read deeper than completeness; see
     *  voicingDue and chooseFinale11.) */
    fun boardCalled11(state: GameState): Boolean =
        LETTERS11.all { it.id in state.arc11Calls }

    /** No reading on the ledger stands refuted — the
     *  state the hard call is gated on, with completeness
     *  and the two load-bearing letters proven. */
    fun boardClean11(state: GameState): Boolean =
        refutedCount11(state) == 0

    /** Whether [l] can be read (or re-read) right now:
     *  its chapter has begun, enough live evidence tagged
     *  to it is in hand, and either it stands unread or
     *  its reading stands refuted. */
    fun letterCallable11(state: GameState, l: Letter11): Boolean {
        if (state.arc11Retired) return false
        if (state.arc11Chapter <= l.minChapter) return false
        if (liveEvidence11(state, l) < l.callPrice) return false
        val v = letterVerdict11(state, l)
        return v == "OPEN" || v == "REFUTED"
    }

    /** The letter the reading dialog should offer next:
     *  the first unread letter (in board order) whose
     *  evidence price is met — or, when nothing new can
     *  be read, the first REFUTED letter, offered for
     *  re-reading. New readings come before revisions,
     *  the way new mail came before corrections in the
     *  cases before this one. */
    fun pendingLetter11(state: GameState): Letter11? {
        if (state.arc11Retired) return null
        for (l in LETTERS11) {
            if (l.id !in state.arc11Calls && letterCallable11(state, l))
                return l
        }
        for (l in LETTERS11) {
            if (letterVerdict11(state, l) == "REFUTED" &&
                letterCallable11(state, l)) return l
        }
        return null
    }

    /** Read (or re-read, after a refutation) a letter:
     *  in voice, off voice, or borrowed voice. The reading
     *  is recorded; the verdict is NOT — it is derived on
     *  every read, so a reading written in week two can
     *  be refuted in week four without anything being
     *  edited: the settling piece simply arrived, or
     *  finished becoming readable. */
    fun chooseCall11(state: GameState, letterId: String,
                    call: String): Boolean {
        if (call !in CALL_KEYS) return false
        val pending = pendingLetter11(state) ?: return false
        if (pending.id != letterId) return false
        val revising = letterId in state.arc11Calls
        state.arc11Calls[letterId] = call
        val v = letterVerdict11(state, pending)
        state.events.add(when {
            revising ->
                "✒️ STRUCK AND RE-READ: '${pending.title}' — " +
                    "the ledger takes the new reading. ($v)"
            v == "PROVEN" ->
                "✒️ READ — AND PROVEN ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand backs this reading."
            v == "REFUTED" ->
                "✒️ READ — AND REFUTED ON ARRIVAL: " +
                    "'${pending.title}'. The settling piece " +
                    "already in hand settles the letter the " +
                    "other way. The reading stands struck. " +
                    "It can be read again."
            else ->
                "✒️ READ: '${pending.title}' — the " +
                    "reading stands, untested. Its settling " +
                    "piece hasn't surfaced, or hasn't become " +
                    "readable. Yet."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "✒️ Voice ledger: '${pending.title}' read ($v)")
        return true
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after
     *  the photo case retires books chapter 1 ten days out
     *  from that retirement. While any earlier case is
     *  open this never fires — and never even books the
     *  date: the mystery slot has one case at a time.
     *  Chapters defer, day by day, past holiday episodes,
     *  like every case before them. */
    fun dueChapter11(state: GameState): ShenaniganDef? {
        if (state.arc11Retired) return null
        if (!state.arc10Retired) return null
        if (state.arc11Chapter >= CHAPTERS11.size) return null
        if (state.arc11DueAbsDay < 0) {
            state.arc11DueAbsDay = if (state.arc11Chapter == 0) {
                val anchor = if (state.arc10RetiredDay >= 0)
                    state.arc10RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc11DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS11[state.arc11Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted11(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf11(def.id)
        if (idx < 0) return
        state.arc11Started = true
        state.arc11Chapter = idx + 1
        state.arc11DueAbsDay = if (idx == CHAPTERS11.size - 1) -1
            else state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the Open Letters ----------------

    /** The Open Letters can be played: the meeting is
     *  airing, its climax has begun, no call is locked
     *  yet, and the ledger is COMPLETE — every letter
     *  read. The chair waits on a finished ledger the way
     *  the committee waited on a finished board: you
     *  cannot open a town's mail for the room until you
     *  have read it for yourself. */
    fun voicingDue(state: GameState): Boolean {
        if (state.arc11Retired || state.arc11Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE11_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return boardCalled11(state)
    }

    /** Whether READ THE LEDGER ALOUD is available: the
     *  ledger's final derived state can carry it — the
     *  counselor's note and the posted ranking stand
     *  PROVEN (the two load-bearing letters: the studied
     *  pair, whose borrowed voices are the case's whole
     *  heart), every letter is read, no reading stands
     *  refuted, and at least four letters stand proven.
     *  The gate reads the FINAL state of the ledger,
     *  never first impressions: a ledger with a struck
     *  reading still standing on it buys nothing, and
     *  neither does a ledger the evidence never finished
     *  settling. */
    fun voiceEligible11(state: GameState): Boolean =
        letterVerdict11(state, LETTERS11[2]) == "PROVEN" &&
            letterVerdict11(state, LETTERS11[3]) == "PROVEN" &&
            boardCalled11(state) && boardClean11(state) &&
            provenCount11(state) >= 4

    /** Whether NAME THE BORROWED HAND is available: at
     *  least one letter stands PROVEN — the town will
     *  follow a naming that has any settled letter under
     *  it, and it is reaching for a name either way. */
    fun nameEligible11(state: GameState): Boolean =
        provenCount11(state) >= 1

    /** Lock the Open Letters' call. The per-call gates
     *  are enforced here, in the module, not just in the
     *  dialog: a call the ledger can't carry is refused
     *  outright. */
    fun chooseFinale11(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!voicingDue(state)) return false
        if (choice == "voice" && !voiceEligible11(state)) return false
        if (choice == "name" && !nameEligible11(state)) return false
        state.arc11Choice = choice
        state.events.add(when (choice) {
            "voice" ->
                "🎙️ You stand up in the hall, ledger in " +
                    "hand, and you read the letters aloud — " +
                    "letter by letter, voice by voice, the " +
                    "way the evidence settled each one. The " +
                    "room hears its own mail explained."
            "name" ->
                "⚖️ You give the room the name its theory " +
                    "has been warming up — the voice act, the " +
                    "mimic, the one who can be anybody for " +
                    "four minutes. The room takes it the way " +
                    "rooms take names: all at once, and for " +
                    "good."
            "stand" ->
                "📬 You let the letters stand. Six letters, " +
                    "six signatures, and a ledger that keeps " +
                    "its readings where only its keeper can " +
                    "see them — the mail keeps every word it " +
                    "arrived with, borrowed and true alike."
            else ->
                "🚶 You walk away. The meeting has the " +
                    "chairs, the glasses, the folder on the " +
                    "table. It doesn't have the ledger, and " +
                    "it doesn't have you."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class VoicingOutcome(
        val branch: String,          // "voiced" | "named" | "standing" | "unopened"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, hand the wrap its
     *  words. Calls are re-gated here against the final
     *  ledger, exactly as locked — verdicts are monotone
     *  in the inputs that matter (evidence is never
     *  un-gathered, chapters never un-air), so a call
     *  that was lawful when locked is lawful now;
     *  anything else degrades to the unopened landing,
     *  the discipline of every case before this one. A
     *  closing nobody played is unopened by definition:
     *  the mail, settled by applause and nearness. */
    fun resolveVoicing(state: GameState): VoicingOutcome {
        val chosen = state.arc11Choice
        val complete = boardCalled11(state)
        val branch = when {
            chosen == "voice" && complete && voiceEligible11(state) -> "voiced"
            chosen == "name" && complete && nameEligible11(state) -> "named"
            chosen == "stand" && complete -> "standing"
            else -> DEFAULT_BRANCH
        }
        state.arc11Branch = branch
        state.arc11Retired = true
        state.arc11RetiredDay = state.absDay
        val held = clueCount11(state)
        return when (branch) {
            "voiced" -> VoicingOutcome(branch,
                "The ledger, read aloud, holds up. Six " +
                    "letters, explained in a full hall, from " +
                    "the voices themselves: a thank-you that " +
                    "was a son's at dictation speed, in his " +
                    "mother's fair hand; a warning in prose " +
                    "its muffled signer has never owned, in " +
                    "four years of notebooks; an apology whose " +
                    "sorrys its signer has never produced in " +
                    "any medium; a long letter its quiet " +
                    "signer answered with one word and a " +
                    "gesture; and two letters — the cruel one " +
                    "and the ranked one — that were homework: " +
                    "studied off a form and a folder by the " +
                    "school's own list-keeper, who filed the " +
                    "town's feelings under other people's " +
                    "names and called the filing accuracy. " +
                    "The names go back to their owners " +
                    "tonight. The routing line stays in the " +
                    "minutes, in small print, where it belongs.",
                listOf(
                    "🎙️ THE VOICES, RETURNED: It is the " +
                        "strangest thing a verdict can be in " +
                        "this town: an investigation that ends " +
                        "with two people getting their own " +
                        "voices back. The list-keeper studied " +
                        "them the way she studies everything — " +
                        "from the records, in the margins, to " +
                        "file people correctly. The counselor's " +
                        "form gave her his openings; the " +
                        "debate folder gave her the captain's; " +
                        "and both letters carry the office " +
                        "forms' routing line, copied with the " +
                        "rest of the furniture, by a hand that " +
                        "copies completely or not at all. She " +
                        "filed a boy's feelings as a discipline " +
                        "problem and a whole class top to " +
                        "bottom, and the filing was meticulous, " +
                        "and the meticulousness is what the " +
                        "room cannot get its applause around. " +
                        "Accuracy was the defense. It was also " +
                        "the confession.",
                    "🎙️ WHAT THE ROOM DOES WITH IT: Nobody " +
                        "apologizes; this is still South Park. " +
                        "The counselor stands in the third row " +
                        "and accepts his voice back the way he " +
                        "accepts everything: with a pamphlet. " +
                        "The captain closes her folder, slowly, " +
                        "like a book she has decided not to " +
                        "throw. The mimic at the back is not " +
                        "named, and stands a moment in the " +
                        "strange weather of not being blamed, " +
                        "holding a program with his own name on " +
                        "it, in his own hand. And two things " +
                        "are promised in the minutes, in " +
                        "writing: a correction card where each " +
                        "borrowed letter was found, one line " +
                        "each, in the signer's own voice, " +
                        "written by the signer. Within the " +
                        "week. The mail keeps receipts now."))
            "named" -> VoicingOutcome(branch,
                "The room gets its name. The ledger had " +
                    "settled letters in it — two of them " +
                    "really had been studied, off real models, " +
                    "by somebody — and the hall read that the " +
                    "only way halls read studying: as a stage, " +
                    "a stool, and a boy who can be anybody for " +
                    "four minutes. The name goes into the " +
                    "minutes spelled right, which the minutes " +
                    "do for everyone, whatever the minutes " +
                    "are worth. The boy at the back says his " +
                    "piece, standing up, to the record: every " +
                    "voice he has ever borrowed, he borrowed " +
                    "into a microphone, announced, with his " +
                    "own name under it in the program. The " +
                    "record keeps everything in this town, " +
                    "including the objection. It just keeps " +
                    "it in a smaller hand.",
                listOf(
                    "🎙️ THE NAME: The nearest stage to the " +
                        "letters was the mimic's, and " +
                        "nearness, in a full room, is nine " +
                        "points of the law. The voices are his " +
                        "because the applause was. The " +
                        "billing is his because the program " +
                        "says so, in his own hand — the same " +
                        "program that says the act was " +
                        "impressions, announced, and given " +
                        "back. The case is his because the " +
                        "room needed a case with a voice in " +
                        "it, and the ledger's two studied " +
                        "letters gave the room its permission.",
                    "🎙️ WHAT THE ROOM DOES WITH IT: The " +
                        "signatures stand — all six, though " +
                        "the ledger in one Journal says two " +
                        "of the six voices were homework and " +
                        "two more were strangers'. The " +
                        "counselor's note goes on being his " +
                        "in the town's mouth, which he treats " +
                        "the way he treats all weather: with a " +
                        "check-in. The ranking stays posted a " +
                        "while longer. The route of the mail " +
                        "does not change. Only the story moved."))
            "standing" -> VoicingOutcome(branch,
                "The letters stand. The meeting votes the " +
                    "mail back to itself: six letters, six " +
                    "signatures, every word kept — the " +
                    "thank-you and the note in the same " +
                    "standing, the ranking and the apology " +
                    "side by side in the minutes — and the " +
                    "ledger that holds what each letter " +
                    "actually is goes home in one person's " +
                    "pocket, complete. In one Journal, the " +
                    "comparison says what each voice is, " +
                    "letter by letter, proven. The town does " +
                    "not subscribe to Journals.",
                listOf(
                    "🎙️ THE STANDING: It is not nothing, a " +
                        "signature. The town has settled " +
                        "worse questions worse ways than by " +
                        "letting the mail stand: by cape, by " +
                        "applause, by cape again. This time " +
                        "the ledger is complete — every " +
                        "letter read, $held pieces of the " +
                        "comparison in one person's hands — " +
                        "and it stands complete, which means " +
                        "the truth of the six voices now " +
                        "exists in exactly two places: the " +
                        "ledger, and the letters themselves, " +
                        "standing there, being it.",
                    "🎙️ WHAT THE ROOM DOES WITH IT: The " +
                        "meeting gets its adjournment, which " +
                        "is the ending it ordered, and thanks " +
                        "the ledger in the minutes for its " +
                        "service. The counselor walks home " +
                        "past the tray of his own forms and " +
                        "says nothing to them, the way you say " +
                        "nothing to a voice you've stopped " +
                        "being sure is yours, out loud."))
            else -> VoicingOutcome(branch,
                "The meeting settles the mail the way " +
                    "meetings settle things without the person " +
                    "holding the whole ledger: by motion, by " +
                    "feel, by the sound a hall makes instead " +
                    "of an answer. The folder goes back to " +
                    "its table unopened. The signatures go " +
                    "back to their letters uncorrected; the " +
                    "question stays posted outside the " +
                    "library a while longer, fading, the way " +
                    "posted things do; and six letters go on " +
                    "meaning what their names say, in the " +
                    "town's ear, unexplained, unmoved, and — " +
                    "in the way of things nobody ever actually " +
                    "reads — slowly becoming part of the " +
                    "mail.",
                listOf(
                    "🎙️ THE UNOPENED FOLDER: There is a " +
                        "version of this mail that adds up. It " +
                        "exists in one Journal, in one hand, " +
                        "finished or not. The town's version " +
                        "is shorter: somebody studied " +
                        "somebody, somewhere, and the boy who " +
                        "does the voices stood very still " +
                        "while the room decided how it felt " +
                        "about that. Feelings, minuted, are a " +
                        "kind of voice too. They are just not " +
                        "the kind you can check.",
                    "🎙️ WHAT THE ROOM DOES WITH IT: The " +
                        "folder stays on the table. The " +
                        "glasses empty without a word. And on " +
                        "the walk home, past the school, the " +
                        "hall, the houses with their " +
                        "dictation drawers and notebooks and " +
                        "fridge doors, every letter sits in " +
                        "the dark being exactly what it always " +
                        "was — which the ledger knows, and " +
                        "the night doesn't."))
        }
    }

    fun applyVoicingEffects(state: GameState, branch: String) {
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
            "voiced" -> {
                befriend("Mackey", 3)
                befriend("Wendy", 2)
                befriend("Jimmy", 2)
                ripple("school_staff", 1,
                    "The Staff watched a full hall hear the mail explained from the voices themselves — a form, a folder, two notebooks and a fridge — and nobody named a performer. The Staff is annotating the minutes. The Staff annotates everything now. It's topical.")
                ripple("parents", -1,
                    "The Parents came for a mimic and got a filing system instead. Half the Parents are re-telling it already. They're telling the routing-line part wrong, but they're telling it.")
                state.arc11PostPending = true
                state.arc11PostDay = state.absDay + POST_DELAY_DAYS
                state.lifeText["mark:CityHall"] =
                    "The hall's minutes keep the Open " +
                        "Letters in full: six letters read " +
                        "aloud, voice by voice, and the names " +
                        "given back to their owners — with " +
                        "one correction still outstanding, " +
                        "promised in writing: a card where " +
                        "each borrowed letter was found, one " +
                        "line each, in the signer's own true " +
                        "voice."
                townLogLine(state, "🎙 VOICED: the ledger " +
                    "was read at the Open Letters. No voice " +
                    "was forged on a stage. The mail was " +
                    "read to its models, in public, and the " +
                    "comparison held up.")
            }
            "named" -> {
                befriend("Jimmy", -4)
                befriend("McDaniels", 1)
                befriend("Mackey", 1)
                ripple("parents", 1,
                    "The Parents got their name at the Open Letters, before the refreshments, in the minutes, spelled right. The Parents are framing the program of the talent show. The impression was announced. The Parents call that a detail, a little, in private.")
                ripple("school_staff", -1,
                    "The Staff keeps the minutes of the naming in a hand slightly tighter than usual. One program in this town keeps a different record — every voice its boy ever borrowed, and where, and announced how — and it is signed in his own hand, in ink, by the box office. He's borrowed a lot of voices, over the years. None of them was filed in a locker.")
                state.lifeText["mark:CityHall"] =
                    "The hall's minutes are closed by name: " +
                        "THE VOICE ACT, singular, in the " +
                        "secretary's fair hand, spelled right. " +
                        "By the box office of the Bijou the " +
                        "program keeps its place on the wall. " +
                        "Being the nearest stage was the case, " +
                        "and the case is closed, and closed " +
                        "things keep their shape."
                townLogLine(state, "🎙 THE NAME: the mail " +
                    "was closed at the Open Letters, by name. " +
                    "The performer's objection is not in the " +
                    "headline. It is in the program, signed, " +
                    "where it will stay.")
            }
            "standing" -> {
                befriend("McDaniels", 1)
                befriend("Victoria", 1)
                ripple("parents", 1,
                    "The Parents kept the signatures they arrived with, which are the signatures the Parents trust most: the ones already written. The pool on the mimic paid out to nobody. The Parents call that a fair result.")
                state.lifeText["mark:CityHall"] =
                    "By the hall door hangs a small new " +
                        "card, in the secretary's fair hand: " +
                        "POSTED AS SIGNED. The note keeps its " +
                        "threat, the ranking its list, the " +
                        "apology its sorrys — and the town " +
                        "keeps its favorite shape for a " +
                        "question: a signature, standing, " +
                        "under glass, marked UNRECONCILED."
                townLogLine(state, "🎙 AS SIGNED: six " +
                    "letters, six signatures, no reading. " +
                    "The ledger stays proven where only its " +
                    "keeper can read it.")
            }
            else -> {
                befriend("Jimmy", -2)
                ripple("parents", -1,
                    "The Open Letters ran long and settled thin, and the Parents applauded in the places where an answer should have been. A few of them noticed, afterward, that the applause had a hole in it shaped exactly like a voice at a microphone.")
                state.lifeText["mark:CityHall"] =
                    "The folder still sits on the hall's " +
                        "table with its question unopened — a " +
                        "meeting settled by a room that didn't " +
                        "have the ledger: by motion, by " +
                        "nearness, by the sound a hall makes " +
                        "instead of an answer. The ledger of " +
                        "that comparison exists in exactly one " +
                        "Journal. It is not this building's " +
                        "copy. This building doesn't have one."
                townLogLine(state, "🎙 THE UNOPENED FOLDER: " +
                    "the Open Letters happened without the " +
                    "ledger. The mail kept the shape it " +
                    "arrived with. The shape is wrong in a " +
                    "way only one person can prove, and " +
                    "that person wasn't in the room.")
            }
        }
    }

    // ---------------- the correction cards (the VOICED landing's week after) ----------------

    /** The delayed landing of the read-aloud: called
     *  daily by the engine; fires exactly once, a week
     *  after the ledger was read — the two correction
     *  cards go up where the borrowed letters were found,
     *  one line each, in the signers' own true voices,
     *  written by themselves this time: by the counselor's
     *  door, a check-in that threatens nobody; by the
     *  library board, a correction that holds its
     *  principle to the last line. The borrowed names,
     *  posted back in their own voices, in public. */
    fun noteNamesPosted(state: GameState): Boolean {
        if (!state.arc11PostPending) return false
        if (state.absDay < state.arc11PostDay) return false
        state.arc11PostPending = false
        state.arc11Posted = true
        state.events.add("🎙️ THE CORRECTION CARDS: two " +
            "cards went up this morning where the borrowed " +
            "letters were found — by the counselor's door, " +
            "in the counselor's own hand: a check-in, a " +
            "pamphlet, and not one threat in the building; " +
            "by the library board, in the captain's own " +
            "hand: a correction of the ranking, every fact " +
            "cited, the principle held to the very last " +
            "line. One line each. Their own voices, this " +
            "time, in their own hands, signed where anyone " +
            "can check them.")
        while (state.events.size > 40) state.events.removeAt(0)
        state.lifeText["mark:CityHall"] =
            "The hall's minutes keep the Open Letters in " +
                "full, and the file is closed by two cards " +
                "posted where the borrowed letters were " +
                "found: the counselor's check-in that " +
                "threatens nobody, and the captain's " +
                "correction that holds to its last line — " +
                "the borrowed names, standing in their own " +
                "voices at last, in the same fair hand as " +
                "everyone else's minutes. The school reads " +
                "the pair of them to the new kids every " +
                "September. It takes three minutes."
        townLogLine(state, "🎙️ THE CORRECTION CARDS: two " +
            "names, two letters, two cards — the case stays " +
            "closed, in the right voices, at every check it " +
            "actually happened.")
        return true
    }

    // ---------------- the aftermath (standing lines) ----------------

    private val AFTERMATH_LINES: Map<String, Map<String, List<String>>> = mapOf(
        "voiced" to mapOf(
            "Mackey" to listOf(
                "The cards are up, the voices are back in " +
                    "their owners, and I will tell you the " +
                    "part I told the meeting in closed " +
                    "session, m'kay: she studied my FORM. Not " +
                    "me — my form. A decade I kept that tray " +
                    "topped up so any child in this school " +
                    "could read exactly how a check-in goes, " +
                    "and the school's own list-keeper read it " +
                    "the way she reads everything, all the " +
                    "way to the routing line, and filed a " +
                    "boy's feelings under THREATS, which is a " +
                    "drawer I do not own, m'kay. I have had a " +
                    "feelings check-in about it. With myself. " +
                    "The pamphlet is called 'Your Voice, " +
                    "Returned.' I keep a copy in the tray. On " +
                    "top. Where the form used to be.",
                "They bring the new kids past the pair of " +
                    "cards now — my check-in and her " +
                    "correction, side by side — and the " +
                    "children ask which one is the real note, " +
                    "and the answer takes three minutes and " +
                    "involves a form, a folder, and a person " +
                    "who filed her classmates the way other " +
                    "people shelve books. Three minutes, and " +
                    "every child in this town now knows what " +
                    "a studied voice is and what it costs to " +
                    "leave one unsigned. I couldn't have " +
                    "bought that lesson with a bond issue. A " +
                    "locker bought it with a threat I never " +
                    "made, m'kay.")),
        "named" to mapOf(
            "Jimmy" to listOf(
                "For the record — since the record is what " +
                    "we keep — nobody handed me a locker. In " +
                    "four minutes on that stage I borrowed " +
                    "eleven voices and I gave back eleven " +
                    "voices, and the program says IMPRESSIONS " +
                    "on it in letters a foot high, and I " +
                    "signed it, in my own hand, by the box " +
                    "office, where it is still taped. You can " +
                    "check. I encourage checking. Checking is " +
                    "literally the theme of the season. The " +
                    "letters in the lockers? Not performed. " +
                    "Not announced. Not signed by the borrower " +
                    "— signed by the BORROWED, which is the " +
                    "whole difference, and it's in the " +
                    "program, and the program is in the " +
                    "minutes now too, in a smaller hand, under " +
                    "my name, which they spelled right. " +
                    "W-wow. What a great audience.",
                "Funny thing about that meeting, since the " +
                    "town's so sure of its shapes: I was at " +
                    "the back of it, doing what I do at the " +
                    "back of rooms, which is material. I have " +
                    "a bit now about getting named for a " +
                    "letter I didn't write in a voice I only " +
                    "borrow on stage. The bit kills. The bit " +
                    "is also, and I cannot stress this enough, " +
                    "the only version of those letters with " +
                    "my name under it that I have ever " +
                    "signed. Because when I borrow a voice, I " +
                    "sign the borrowing. It's the whole act. " +
                    "It's kind of my signature move. Pun " +
                    "intended. Nobody laughed at the meeting " +
                    "either.")),
        "standing" to mapOf(
            "Wendy" to listOf(
                "The ranking stands, the signatures stand, " +
                    "and I am told that is what closure looks " +
                    "like in a hall. My folder still says what " +
                    "it says: a year of drafts in which my " +
                    "hand never once dropped a principle, " +
                    "never once routed a form, never once " +
                    "wrote 'forget it' about anything I " +
                    "believed — and the ranking with my name " +
                    "on it does all three in nine lines, and " +
                    "it is still posted, and people still " +
                    "read it in my voice, because the BOARD " +
                    "outside the library does not subscribe " +
                    "to folders. You read the whole of it, I " +
                    "think. You're the only one who did. It's " +
                    "a strange thing, being the author of a " +
                    "letter in a town that has decided letters " +
                    "are furniture. Mind you keep your ledger.",
                "A girl asked me this week if I really " +
                    "ranked her. I told her the truth the " +
                    "school can afford: that all of us are " +
                    "ranked somewhere, by somebody, in some " +
                    "hand — and that the one with my name on " +
                    "it gave up on its own list halfway down, " +
                    "which I have never done with a list in my " +
                    "life, including grocery lists, including " +
                    "this conversation. She considered this " +
                    "with the seriousness it deserved, which " +
                    "is more than the meeting managed. The " +
                    "children will be fine. The children read " +
                    "to the end of things.")),
        "unopened" to mapOf(
            "Butters" to listOf(
                "Well, gee — I don't rightly know what " +
                    "happened at that meeting, on account of I " +
                    "wasn't called, but I know what happened " +
                    "after, which is: everybody reads my " +
                    "thank-you twice now. Twice! Even Mrs. " +
                    "Lunch-Lady, and it was FOR her. First " +
                    "they said it was too neat to be mine, and " +
                    "now they read it slow, like it's gonna " +
                    "confess. Fellas, it's a thank-you. It " +
                    "says thank you in it, three times if you " +
                    "count the pudding part, which I do. My " +
                    "mom's got the draft in a drawer that " +
                    "proves every word, and the draft's " +
                    "getting a little tired of proving it. I " +
                    "just wanted 'em to know about the " +
                    "pudding.",
                "Oh hamburgers. Mr. Mackey asked me if I " +
                    "was doin' okay about the letters, which " +
                    "was real nice, and I said I was, mostly, " +
                    "except now when I write ANYTHING the " +
                    "fellas lean in. I wrote a grocery list " +
                    "yesterday and two of 'em leaned in. It's " +
                    "a grocery list! It says eggs! If a fella " +
                    "can't write eggs without a hearing, the " +
                    "mail's in a bad way, and I said so, and " +
                    "Mr. Mackey wrote it down, which I guess " +
                    "means it's evidence now. Everything's " +
                    "evidence now. Even the eggs.")))

    /** The ending's standing voice: in each landing, one
     *  person says the uncomfortable thing, forever, to
     *  anyone who'll stand still for it — the counselor
     *  who got his voice back, the performer the room
     *  named, the captain whose ranking stands, the boy
     *  whose true letter is read twice now. Null before
     *  the case closes, and for everyone else, always. */
    fun aftermathLine11(state: GameState, npcName: String): Line? {
        if (!state.arc11Retired) return null
        val bank = AFTERMATH_LINES[state.arc11Branch]?.get(npcName) ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(npcName, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The eleventh case shows once it exists: chapter 1
     *  has aired, a piece of evidence is in hand, a letter
     *  is read, or the case is closed. */
    fun boardVisible11(state: GameState): Boolean =
        state.arc11Started || state.mystery11Clues.isNotEmpty() ||
            state.arc11Calls.isNotEmpty() || state.arc11Retired

    fun statusStamp11(state: GameState): String = when {
        state.arc11Retired && state.arc11Branch == "voiced" ->
            "🎙️ VOICED — the mail tells the truth, voice by voice"
        state.arc11Retired && state.arc11Branch == "named" ->
            "🎙️ NAMED — the mail was closed by name at the Open Letters"
        state.arc11Retired && state.arc11Branch == "standing" ->
            "🎙️ AS SIGNED — six letters, six signatures, one pocket of doubts"
        state.arc11Retired -> "📭 UNOPENED — the Open Letters happened without the readings"
        state.shenaniganId == FINALE11_ID -> "📬 OPEN LETTERS — THE MAIL, CORRECTED"
        state.arc11Started ->
            "🎙️ OPEN — chapter ${state.arc11Chapter} of ${CHAPTERS11.size} aired; the letters are being read against their voices"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the
     *  case is — believed, as the town believes
     *  everything, in one breath and at applause speed. */
    fun currentTheory11(state: GameState): String = when {
        state.arc11Retired && state.arc11Branch == "voiced" ->
            "CLOSED, and voiced: the town now tells the " +
                "letter story as a story about homework — a " +
                "form studied too well, a folder read too " +
                "closely, and a list-keeper who filed her " +
                "classmates under other people's names " +
                "because filing them correctly was the " +
                "point. It takes longer to tell this way. " +
                "Nobody minds, which around here is " +
                "basically a parade. The mimic is 'the act' " +
                "now, in the town's mouth, which is not an " +
                "apology, but it rhymes with one."
        state.arc11Retired && state.arc11Branch == "named" ->
            "CLOSED, by name: the voice act, the stool, " +
                "the ovation, settled. The town tells it at " +
                "the speed of relief — a borrower, a name, a " +
                "closed file — and only gets quiet at one " +
                "theater in town, where a program signed in " +
                "the borrower's own hand is being kept " +
                "taped by the box office, in case anyone " +
                "ever wants to check what 'announced' " +
                "means. Stages keep what minutes drop. This " +
                "one will keep it for years."
        state.arc11Retired && state.arc11Branch == "standing" ->
            "STANDING, which the town calls delivered: a " +
                "note 'appeared' with a threat in it its " +
                "signer never made, in a hand the file " +
                "doesn't specify, and the signatures say " +
                "SIGNED in pencil the town has learned to " +
                "read as a punchline. In one Journal the " +
                "comparison is settled properly, letter by " +
                "letter, and adds up. It has exactly one " +
                "reader. It had exactly one all along."
        state.arc11Retired ->
            "DECIDED, in a room you weren't in: the Open " +
                "Letters ran on the signatures alone — six " +
                "letters and a room's sense of nearness — " +
                "and the mail got the shape rooms give " +
                "things they cannot read: the nearest " +
                "stage. The ledger in your Journal is the " +
                "only settled copy of that comparison in " +
                "existence. It is complete, or it isn't. " +
                "Either way it is unopened, which in this " +
                "town is a tense of its own."
        !state.arc11Started ->
            "Nothing yet. The letters say what they say, " +
                "the signatures claim what they claim, and " +
                "six of them are about to disagree with " +
                "their own voices, in public, in a school " +
                "hallway. Give it a mail day."
        state.arc11Chapter <= 1 ->
            "Settled, at cape speed: somewhere there is " +
                "one hand that can sound like anybody — a " +
                "counselor here, a captain there, the whole " +
                "fourth grade in rotation — and the only " +
                "problem is the small one that every letter " +
                "in the case was found standing in plain " +
                "sight, pinned, posted and taped, utterly " +
                "unsurprised to be read. The town is holding " +
                "both halves of that sentence without " +
                "apparent discomfort. Towns can hold " +
                "anything, if you let them hold it in one " +
                "breath."
        state.arc11Chapter == 2 ->
            "The hands have been read in person and they " +
                "hold: the neatest letter in the case is a " +
                "dictation, the flattest voice in town is a " +
                "voice box being honest, and the loudest " +
                "borrower in the case signs his borrowings " +
                "in the program. The town has begun, " +
                "uneasily, to suspect the signatures rather " +
                "than the hands, which for this town is a " +
                "philosophical breakthrough on the scale of " +
                "plumbing. Suspecting the signatures " +
                "doesn't name anybody, though, and the town " +
                "still wants a name by the Open Letters. " +
                "Wants are doing a lot of the filing now."
        state.arc11Chapter == 3 ->
            "Hands are done and the study is open, and " +
                "the town's position has quietly split: the " +
                "people who have read the forms think the " +
                "models are guilty; the people who only read " +
                "the signatures think a person is. The " +
                "ledger says both halves are asking the " +
                "wrong question. The ledger doesn't get a " +
                "vote. The ledger gets read, or it gets " +
                "left in the folder — next week, in the " +
                "hall, in front of everyone."
        else ->
            "Everything lookable has been looked at twice " +
                "and everything readable is read except one " +
                "folder, whose context arrived with the " +
                "week, the way contexts do. Tonight the " +
                "room hears one version of the mail: the " +
                "signatures', or the ledger's. The six " +
                "letters will be in the room either way, in " +
                "the folder, in the order they surfaced. " +
                "They have been the most patient witnesses " +
                "in the case, and the only ones that never " +
                "changed their text."
    }

    /** The Open Letters' published terms, in the board's
     *  own words — the forecast a player can plan by. */
    fun forecastLine11(state: GameState): String {
        val called = calledCount11(state)
        val proven = provenCount11(state)
        return "📬 THE OPEN LETTERS, AS THEY STAND: the " +
            "meeting waits on a finished ledger — " +
            "$called/${LETTERS11.size} letters read, $proven " +
            "standing proven. READ THE LEDGER ALOUD needs " +
            "the ledger's final state: THE COUNSELOR'S " +
            "NOTE and THE RANKING, POSTED proven, every " +
            "letter read, no reading standing refuted, and " +
            "at least four letters proven. NAME THE " +
            "BORROWED HAND needs any one letter proven. LET " +
            "THE LETTERS STAND and WALK AWAY need nothing " +
            "but you. A refuted reading can be struck and " +
            "read again; a standing one cannot. Nothing " +
            "moves the ledger but evidence, letter by " +
            "letter, voice by voice."
    }

    /** The eleventh case's board as plain lines (the
     *  Journal renders them below the photo case's; the
     *  suite reads them). THE VOICE LEDGER: six letters as
     *  rows, each carrying its claim, its text, its
     *  claimed voice's card, and the findings the scan
     *  derives from the two — its evidence, its reading,
     *  and its reading's verdict — derived on the spot:
     *  the ledger never stores a finding or a verdict, it
     *  re-reads the letters every time you open it. */
    fun boardLines11(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp11(state))
        out.add("🎙️ THE VOICE LEDGER: Letters have been " +
            "surfacing around town — in mailboxes, on the " +
            "school board, tucked into a locker — each " +
            "signed with somebody's name. Some of them are " +
            "kind. Some are cruel in a way the signer would " +
            "never be. This board is not about the " +
            "handwriting; nobody writes alike twice, and " +
            "half the town's mail rides in another person's " +
            "hand for honest reasons — a mother copying " +
            "fair, a sister taking dictation, a voice box " +
            "being flat. It's the VOICE: every letter below " +
            "is read against the voice it claims — its " +
            "markers, its nevers, the tells a studied copy " +
            "leaves — and each letter waits for your " +
            "reading on what it is: theirs, a stranger's, " +
            "or studied.")
        out.add("🧩 THE EVIDENCE — what you hold " +
            "(${clueCount11(state)}/${CLUES11.size})" +
            if (!state.arc11Retired &&
                CLUES11.any { it.id in state.mystery11Clues && !clueLive11(state, it) })
                " — some of it is in hand but not yet readable: " +
                    "its context hasn't arrived, and in this " +
                    "case context is a kind of voice" else "")
        out.add("🎙️ THE LETTERS — six letters, six claimed " +
            "voices. The findings on each row are derived " +
            "from the letter's own text against its " +
            "signer's card, on every read: the ledger keeps " +
            "texts and cards, not conclusions.")
        for (l in LETTERS11) {
            val placeName = LOCATIONS[l.place]?.name ?: l.place
            val p = PROFILE11_BY_SIGNER[l.signer]
            val f = findings11(l)
            out.add("— ${l.title} — surfaced at $placeName; kept by ${l.keeper}")
            out.add("• its claim: ${l.claimLabel}")
            out.add("• the letter: “${l.text}”")
            if (p != null) {
                out.add("• the claimed voice (${p.signer}): ${p.habit}")
                val markerLine = if (f.markersHit.isEmpty())
                    "none of the voice's markers"
                else
                    "markers found: ${f.markersHit.joinToString(", ") { "“$it”" }}"
                val mustLine = when {
                    p.must.isEmpty() -> "no load-bearing marks"
                    f.mustMissed.isEmpty() ->
                        "load-bearing marks present: ${f.mustHit.joinToString(", ") { "“$it”" }}"
                    else ->
                        "load-bearing marks MISSING: ${f.mustMissed.joinToString(", ") { "“$it”" }}"
                }
                val neverLine = if (f.neversHit.isEmpty())
                    "no nevers violated"
                else
                    "NEVERS VIOLATED: ${f.neversHit.joinToString(", ") { "“$it”" }}"
                val tellLine = if (f.tellsHit.isEmpty())
                    "no studied tells"
                else
                    "STUDIED TELLS: ${f.tellsHit.joinToString(", ") { "“$it”" }}"
                out.add("• the scan: $markerLine · $mustLine · " +
                    "$neverLine · $tellLine")
                out.add("• the shape: ${when (signature11(l)) {
                    "in" -> "IN-VOICE SHAPED — the surface is the signer's, with nothing foreign in it"
                    "borrowed" -> "BORROWED SHAPED — the markers are right, and a tell stands in them"
                    "off" -> "OFF-VOICE SHAPED — the signer's surface is absent; a stranger's hand, however it signs"
                    else -> "MIXED — the counts contradict every clean shape; the ledger won't guess"
                }}")
            }
            if (hasRouting11(l)) {
                out.add("• postscript: “$ROUTING_PHRASE” — a " +
                    "routing phrase, the kind the school " +
                    "office's forms carry. Office furniture, " +
                    "in a letter.")
            }
            val held = CLUES11.filter { it.site == l.index &&
                it.id in state.mystery11Clues }
            if (held.isEmpty()) {
                out.add("• (no evidence yet — this row's " +
                    "pieces haven't surfaced)")
            }
            for (clue in held) {
                if (clueLive11(state, clue)) {
                    out.add("• ${clue.title} — ${clue.detail}")
                } else {
                    out.add("• 🔒 ${clue.title} — ${clue.inertDetail}")
                }
            }
            val live = liveEvidence11(state, l)
            val called = state.arc11Calls[l.id]
            if (called == null) {
                out.add(when {
                    state.arc11Chapter <= l.minChapter ->
                        "✏️ READING: unwritten — this row's " +
                            "week hasn't come; the ledger " +
                            "takes its letters in order, like " +
                            "its mail."
                    live >= l.callPrice ->
                        "✏️ READING: unwritten — you hold " +
                            "$live live pieces for this row. " +
                            "It can be read."
                    else ->
                        "✏️ READING: unwritten — " +
                            "$live/${l.callPrice} live pieces " +
                            "in hand. Inert evidence doesn't " +
                            "count: it hasn't become readable " +
                            "yet, meaning-wise."
                })
            } else {
                val text = when (called) {
                    "in" -> "IN VOICE — the signer's own: " +
                        "the hand may be borrowed, the voice " +
                        "never left them"
                    "off" -> "OFF VOICE — a stranger's hand: " +
                        "the signer didn't write it, and " +
                        "nobody studied them to do it"
                    else -> "BORROWED VOICE — a studied " +
                        "imitation: somebody did the homework " +
                        "and wrote AS them"
                }
                val stamp = when (letterVerdict11(state, l)) {
                    "PROVEN" -> "✅ PROVEN — the settling " +
                        "piece in hand grades this reading TRUE"
                    "REFUTED" -> "💔 REFUTED — the settling " +
                        "piece in hand settles this letter the " +
                        "other way. The reading stands struck; " +
                        "it can be struck and read again"
                    else -> "📌 COMMITTED — standing, " +
                        "untested; the settling piece hasn't " +
                        "surfaced, or hasn't become readable"
                }
                out.add("🖊️ $stamp: “$text”")
            }
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory11(state)}")
        if (!state.arc11Retired && state.arc11Started) {
            out.add(forecastLine11(state))
        }
        if (state.arc11Retired) {
            out.add(when (state.arc11Branch) {
                "voiced" -> "🏁 The ledger was read aloud: " +
                    "every letter read, the load-bearing " +
                    "pair proven, no reading left standing " +
                    "struck — and the mail, explained in a " +
                    "full hall from the voices themselves, " +
                    "held up for the first time. No voice was " +
                    "forged on a stage. The names went back " +
                    "to their owners. The routing line went " +
                    "into the minutes, in small print."
                "named" -> "🏁 The room got its name. Two " +
                    "letters on the ledger really were " +
                    "studied, and the town read that the only " +
                    "way it reads studying: as a stage, a " +
                    "stool, and an ovation. The objection is " +
                    "not on this board. It is in a program, " +
                    "signed in the borrower's own hand, by a " +
                    "box office, where it will stay, saying " +
                    "what 'announced' means."
                "standing" -> "🏁 The letters stood. The " +
                    "ledger was complete — every letter " +
                    "read, its verdicts earned — and the " +
                    "room kept the signatures it arrived " +
                    "with anyway. What is proven stays " +
                    "proven, on this board, in full. What " +
                    "is signed stays signed. The two " +
                    "documents now disagree in the minutes, " +
                    "forever, politely."
                else -> "🏁 The Open Letters happened " +
                    "without the ledger's voice. ${clueCount11(state)} pieces " +
                        "of the comparison were in one " +
                        "person's hands that night. The room " +
                        "ran on the signatures alone, the way " +
                        "it did the first time. Rooms keep " +
                        "doing that until somebody reads them " +
                        "a letter."
            })
            if (state.arc11Branch == "voiced") {
                if (state.arc11PostPending) {
                    out.add("🎙️ One thing is still out " +
                        "there: the correction cards, promised " +
                        "in writing in the minutes. Some " +
                        "mornings are worth walking past the " +
                        "school for.")
                } else if (state.arc11Posted) {
                    out.add("🎙️ The correction cards are " +
                        "posted — two names standing in their " +
                        "own voices at last, one line each, " +
                        "and for once nobody in town is " +
                        "demanding the pair of them be " +
                        "explained. The file stays closed: " +
                        "this is what a closed file looks like " +
                        "when it's read right.")
                }
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS11 = mapOf(
        "voiced" to listOf(
            "The letters? Read aloud, whole hall. Turns " +
                "out two of 'em were homework — studied off " +
                "a form and a folder, filed under other " +
                "people's names. Town's still adjusting to " +
                "a mystery with a routing line.",
            "Seen the pair of cards — the counselor's " +
                "check-in and the captain's correction? One " +
                "line each, their own voices. School reads " +
                "'em to the new kids. Three minutes, they " +
                "say."),
        "named" to listOf(
            "The mail's closed — named the voice act, " +
                "open and shut. Funny: his program's still " +
                "taped by the box office, signed in his own " +
                "hand, says IMPRESSIONS in letters a foot " +
                "high. Nobody's asked. Ask him. Or don't. " +
                "The program'll be there.",
            "The Open Letters? Oh, they named the kid " +
                "with the stool. Nearest stage to the " +
                "letters, that's the arithmetic the room " +
                "used. There's another arithmetic, in one " +
                "Journal somewhere, letter by letter. " +
                "Journals keep everything. It's the reading " +
                "that's scarce."),
        "standing" to listOf(
            "The mail got to stand as signed, is the " +
                "official story. Six letters, six " +
                "signatures, one card by the hall door that " +
                "says POSTED AS SIGNED in very small, neat, " +
                "printed-looking letters. Town tells it as " +
                "a ghost story now, basically. The kids " +
                "love it. The kids' hallway has the ranking " +
                "in it. Funny town.",
            "There's a card by the hall door bothers " +
                "people and nobody can say why. Small, neat, " +
                "printed-looking. Says AS SIGNED. It'll " +
                "outlive the meeting."),
        "unopened" to listOf(
            "The Open Letters? Hall was full. They " +
                "settled the mail the usual way — warmest " +
                "name wins. The ledger of it's in somebody's " +
                "Journal, complete as you like. Journals " +
                "keep everything.",
            "Some meeting at the hall got settled by a " +
                "folder nobody opened, I hear. The six " +
                "letters are all still standing where they " +
                "surfaced. Letters are patient. Longer the " +
                "town waits, the more they look like they " +
                "belong. That's how mail drifts, probably."))

    /** A post-case ambient aside in the town's own voice —
     *  the ending the town lives with is the one it keeps
     *  telling, at every check it actually happened.
     *  Null until the case is closed. */
    fun ambientCallback11(state: GameState, rnd: Random): String? {
        if (!state.arc11Retired) return null
        val bank = CALLBACKS11[state.arc11Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

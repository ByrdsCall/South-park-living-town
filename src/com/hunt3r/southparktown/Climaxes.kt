package com.hunt3r.southparktown

// ============================================================
// Climaxes — v0.7.0e (Queue #7 item 5): the shenanigans'
// climax stage stops resolving on participation alone.
//
// When one of the town's ten shenanigans reaches its CLIMAX
// stage (stage 2, 12:00–18:00) and the player is there, the
// climax plays as a set-piece: a chain of THREE judged beats
// written for that event's own story, hosted by whichever
// of the event's causers the player talks to at the climax
// turf (a new TalkGate rail, CLIMAX, sits between FINALE
// and DINNER — the climax is the day's peak, and its window
// is hours). The beats judge on the town's existing verbs:
//
//   TALK   a choice row — rally, negotiate, call it out.
//   LOOK   a choice row — spot the critical thing in time.
//   PHOTO  out-of-band: take a photo at the climax turf
//          while the climax is live (the shutter is the
//          verb; notePhoto hears it), then show the host.
//   FIND   out-of-band: a careful Look at a named spot
//          turns the thing up (lookText hears it), then
//          carry it back to the host.
//   STAND  the one new interaction, used three times in
//          the whole set: HOLD YOUR GROUND. Three times the
//          moment surges; three times the room wants you to
//          jump early or cave. Holding all three lands the
//          beat, cracking on the third is a near thing, and
//          cracking earlier is a miss. The first STAND a
//          save ever meets teaches the rule up front.
//
// Every beat scores LANDED 2 / CLOSE 1 / MISSED 0, and a
// resolved beat also bumps the event's participation by
// BEAT_PARTICIPATION — playing the climax IS having a hand
// in the day, on the same ledger the town already keeps.
//
// The wrap remembers HOW it went. A played climax resolves
// to a tier — HEROIC (every beat landed), HANDLED (mixed),
// FUMBLED (most beats missed — funny, never punishing: the
// event still resolves, the town's version just stars your
// fumble) — pays the causers friendship by tier, plants a
// tier-flavored memory topic the causers retell tomorrow
// ("the big save" / "the big day" / "the big fumble"), and
// writes the tier and a one-line how-it-went onto the
// episode's Journal record. A climax never played is
// ABSENT: the wrap is byte-for-byte the legacy behavior —
// outcome from participation, no recap line, no pays —
// and only the episode's tier field quietly says "absent".
//
// Interruption is safe by construction: the chain only
// lives inside stage 2, and at the midnight wrap a started
// chain settles at the tier earned so far (unplayed beats
// count as missed once at least one beat resolved; a chain
// opened but never played settles as ABSENT). Settle
// happens exactly once, at the wrap, behind the settled
// flag. Episodes finished before this build carry no tier:
// Episodes.climaxTierOf reads them as the before-record —
// GOT IT DONE for any memory purpose, marked "legacy".
//
// State: no new save keys — the chain, its scores and its
// flags ride the existing life/lifeText maps under
// day-scoped "climax:" keys, pruned at the wrap. Mystery
// chapters and beats are NOT in CHAINS: their climaxes are
// their own machinery and stay untouched, solutions and
// all.
// ============================================================

object Climaxes {

    // ---------------- the dials (all suite-pinned) ----------------

    /** Beats in every chain. */
    const val BEATS_PER_CHAIN = 3

    /** Beat scores: landed, close, missed. */
    const val SCORE_LANDED = 2
    const val SCORE_CLOSE = 1
    const val SCORE_MISSED = 0

    /** Participation each resolved beat adds to the event's
     *  own ledger (the wrap's standing rule reads it). */
    const val BEAT_PARTICIPATION = 2

    /** Causers' friendship at the wrap, by tier. */
    const val PAY_HEROIC = 2
    const val PAY_HANDLED = 1
    const val PAY_FUMBLED = 0

    // ---------------- tiers ----------------

    const val TIER_HEROIC = "heroic"
    const val TIER_HANDLED = "handled"
    const val TIER_FUMBLED = "fumbled"
    const val TIER_ABSENT = "absent"

    /** The memory topics the causers carry into tomorrow,
     *  by tier — Threads holds their retelling banks. */
    const val TOPIC_HEROIC = "the big save"
    const val TOPIC_HANDLED = "the big day"
    const val TOPIC_FUMBLED = "the big fumble"

    fun topicForTier(tier: String): String? = when (tier) {
        TIER_HEROIC -> TOPIC_HEROIC
        TIER_HANDLED -> TOPIC_HANDLED
        TIER_FUMBLED -> TOPIC_FUMBLED
        else -> null
    }

    fun tierLabel(tier: String): String = when (tier) {
        TIER_HEROIC -> "⭐ HEROIC"
        TIER_HANDLED -> "✔ GOT IT DONE"
        TIER_FUMBLED -> "💫 FUMBLED (beloved)"
        TIER_ABSENT -> "you weren't there"
        else -> tier
    }

    /** The outcome lines a played climax wraps with (an
     *  absent climax keeps the legacy participation lines —
     *  wrap returns a null outcome for it). */
    fun outcomeForTier(tier: String): String? = when (tier) {
        TIER_HEROIC -> "You stole the show at the climax — the town will retell it your way."
        TIER_HANDLED -> "You got the climax handled — not clean, but unmistakably yours."
        TIER_FUMBLED -> "The climax happened AT you, gloriously — and the town loves the story anyway."
        else -> null
    }

    /** The tier math, pure: HEROIC iff every beat landed;
     *  FUMBLED iff most beats missed; else HANDLED. */
    fun tierFromScores(scores: List<Int>): String {
        if (scores.all { it == SCORE_LANDED }) return TIER_HEROIC
        val missed = scores.count { it == SCORE_MISSED }
        if (missed * 2 > scores.size) return TIER_FUMBLED
        return TIER_HANDLED
    }

    // ---------------- beat kinds ----------------

    const val KIND_TALK = "talk"
    const val KIND_LOOK = "look"
    const val KIND_STAND = "stand"
    const val KIND_PHOTO = "photo"
    const val KIND_FIND = "find"

    // ---------------- the writing ----------------

    data class COption(val label: String, val score: Int, val result: String)

    /** One surge of a STAND beat: pressure text and its
     *  responses; exactly one (at [holdIndex]) holds. */
    data class Escalation(val pressure: String, val options: List<String>,
                          val holdIndex: Int)

    data class CBeat(
        val kind: String,
        val text: String,
        val options: List<COption> = emptyList(),
        val escalations: List<Escalation> = emptyList(),
        val standWin: String = "",
        val standClose: String = "",
        val standMiss: String = "",
        val taskDone: String = "",
        val taskSkip: String = "",
        val findLoc: String = "",
        val findText: String = ""
    )

    data class ClimaxChain(
        val eventId: String,
        val intro: String,
        val beats: List<CBeat>,
        val closing: String,
        val heroicLine: String,
        val handledLine: String,
        val fumbledLine: String
    )

    fun howLineFor(chain: ClimaxChain, tier: String): String = when (tier) {
        TIER_HEROIC -> chain.heroicLine
        TIER_HANDLED -> chain.handledLine
        TIER_FUMBLED -> chain.fumbledLine
        else -> ""
    }

    /** The rule of the one new interaction, taught by its
     *  first use in a save (and only its first use). */
    const val STAND_TEACHING =
        "HOLD YOUR GROUND — the rule: three times, the moment " +
        "will surge. Three times, the room will want you to jump " +
        "early or cave. Don't. Steady is the whole game.\n\n"

    val CHAINS: Map<String, ClimaxChain> = listOf(

        ClimaxChain(
            eventId = "chinpo_craze",
            intro = "Main Street is a war zone of lunch money. " +
                "Cartman's 'ultra-rare' is up on the crate, the " +
                "bidding is peaking, and the whole town is " +
                "watching. This is the moment the day gets " +
                "decided — and you're standing in the middle of it.",
            beats = listOf(
                CBeat(KIND_LOOK,
                    "Before the hammer falls, you get one good " +
                        "look at the ultra-rare. Really look.",
                    options = listOf(
                        COption("The seam along its belly — and the glitter rubbed raw at the edges.",
                            SCORE_LANDED,
                            "Glitter. Craft glitter, coming off on your fingers. It's a Pengin in a party hat of glue. You KNOW. Now the town has to hear it."),
                        COption("It sparkles. Admittedly. It sparkles a LOT.",
                            SCORE_CLOSE,
                            "It catches the light like a disco ball at a funeral. Suspicious — but sparkle alone won't convict anyone."),
                        COption("Cartman's honest, trustworthy face above the crate.",
                            SCORE_MISSED,
                            "You study his face for the truth. His face declines to cooperate. Nothing learned."))),
                CBeat(KIND_TALK,
                    "The bids are at 'everything in Kevin's " +
                        "pockets'. The crate is yours to crack " +
                        "open — with words.",
                    options = listOf(
                        COption("“Dude. That's a Pengin. With GLITTER on it.”",
                            SCORE_LANDED,
                            "Silence. Then Kevin turns his out — glitter. The auction becomes a weather event: money raining back into pockets. Cartman is already denying he was ever here."),
                        COption("“Hold on — where's the certificate of rareness?”",
                            SCORE_CLOSE,
                            "Cartman produces a certificate. It is written in crayon. It says 'RARE'. The crowd wavers — half convinced, half checking their pockets. The price sags, but the craze limps on."),
                        COption("“I BID MY LUNCH. AND TOMORROW'S LUNCH.”",
                            SCORE_MISSED,
                            "You own it now. You own a Pengin with glitter on it. The town will tell this part of the story forever, and they will not be kind about the price."))),
                CBeat(KIND_PHOTO,
                    "Stan's already drafting the expose for the " +
                        "school paper. It needs one thing: " +
                        "photographic evidence — a photo taken " +
                        "here on Main Street while the climax " +
                        "is live. Then show the host.",
                    taskDone = "Got it — glitter seam, price tag, Cartman's 'who, me?' face, all in frame. Stan runs it above the fold. There is no coming back from a photo.",
                    taskSkip = "No photo. The story runs on your word alone, and your word, today, has glitter on it too.")),
            closing = "The crate is empty, the lunch money is " +
                "back in circulation, and the glitter — like " +
                "all glitter — will outlive everyone involved.",
            heroicLine = "You called the glitter in front of everyone and got the photo to prove it.",
            handledLine = "You slowed the craze down enough that it tripped over itself.",
            fumbledLine = "You bid two lunches on a glittered Pengin and the town will NEVER let it die."),

        ClimaxChain(
            eventId = "pirate_week",
            intro = "The whole town is digging at the park on " +
                "the word of a map of extremely pirate " +
                "provenance. Butters is three feet down in " +
                "morale and rising. Somebody has to run this " +
                "dig like a captain.",
            beats = listOf(
                CBeat(KIND_LOOK,
                    "The treasure map is being held by four " +
                        "people arguing about which way is up. " +
                        "You get one clear look.",
                    options = listOf(
                        COption("The X sits over the SANDBOX — the oak is a decoy drawn by a liar.",
                            SCORE_LANDED,
                            "You flip the map right-side up. The X lands square on the sandbox. Every shovel in the park turns at once. THAT's captaincy."),
                        COption("There's a coffee ring on the map shaped exactly like a skull.",
                            SCORE_CLOSE,
                            "Ominous. Atmospheric. Not, strictly, navigational. The crew digs a respectful circle around it anyway."),
                        COption("The map appears to be a placemat from a seafood restaurant.",
                            SCORE_MISSED,
                            "It is. There's a coupon on the back. The crew loses a full minute to the coupon."))),
                CBeat(KIND_TALK,
                    "Morale is sinking faster than the hole. " +
                        "The crew needs a captain's words.",
                    options = listOf(
                        COption("“Dig, ye dogs! The tide waits for NO MAN!”",
                            SCORE_LANDED,
                            "Something ancient wakes in the crew. Butters salutes with a trowel. The digging doubles — this is the most organized this town has ever been."),
                        COption("“Nobody walks the plank if we find it before dinner.”",
                            SCORE_CLOSE,
                            "A cheer, of a sort. The threat of plank works on exactly the people it should. Digging resumes at three-quarter speed."),
                        COption("“Is there a snack break in piracy?”",
                            SCORE_MISSED,
                            "There is now. The crew mutinies into a snack break. It lasts twenty minutes. The foam parrot does not survive it."))),
                CBeat(KIND_FIND,
                    "Every good hunt ends with somebody " +
                        "actually checking the place nobody " +
                        "checked. The old oak's roots, at the " +
                        "edge of the park — go and Look there. " +
                        "Then bring the word back.",
                    taskDone = "You carry the lunchbox back like a captured standard. The crew gathers. It is, technically, treasure. It was always going to be batteries.",
                    taskSkip = "The oak keeps its secret. The crew digs the sandbox until the sandbox files a complaint, and the hunt ends the way all hunts end: tired.",
                    findLoc = "KidsPark",
                    findText = "🌴 Under the oak's roots: a lunchbox. Inside — old batteries, one foam-parrot feather, and a note in pirate: 'THE REAL TREASURE WAS THE DIGGING. — C.' You found the end of the hunt.")),
            closing = "The hole is filled, the lunchbox is " +
                "legend, and Butters is still in character. " +
                "The bet was never in doubt.",
            heroicLine = "You captained the dig, read the map right, and found the lunchbox yourself.",
            handledLine = "You kept the hunt together long enough for it to end like a story.",
            fumbledLine = "Your captaincy is remembered mostly for the snack-break mutiny."),

        ClimaxChain(
            eventId = "upside_down_day",
            intro = "City Hall is full of fourth-graders " +
                "running a town meeting, and it's going BETTER " +
                "than the real ones. The adults are at recess. " +
                "Somebody has to chair the peak of the " +
                "wrongness — and the gavel is warm.",
            beats = listOf(
                CBeat(KIND_TALK,
                    "You're recognized by the chair (also you, " +
                        "somehow). The floor wants a motion.",
                    options = listOf(
                        COption("“Motion: recess is a right, and the adults are abusing it.”",
                            SCORE_LANDED,
                            "The chamber erupts. The motion passes 12–0. Somewhere across town, Randy feels a disturbance and drops the recess bell."),
                        COption("“Motion: we adjourn for snacks and justice.”",
                            SCORE_CLOSE,
                            "Passed, with amendments. The snacks amendment eats the justice amendment. Still: governance."),
                        COption("“Motion: we read the entire tax code aloud.”",
                            SCORE_MISSED,
                            "The chamber exercises its one adult power: the filibuster of walking out. Order is restored by nobody being there."))),
                CBeat(KIND_LOOK,
                    "Wrongness this total has a weak point. " +
                        "One good look around the chamber.",
                    options = listOf(
                        COption("McDaniels is taking the minutes — REAL minutes — and enjoying it.",
                            SCORE_LANDED,
                            "You see it: the mayor is having the time of her life. The day's secret is that the kids run it better, and she's known since nine."),
                        COption("A grown-up's coffee mug on a kid-sized desk. Nobody claims it.",
                            SCORE_CLOSE,
                            "Evidence of a defection. Touching, low-grade evidence. The chamber murmurs."),
                        COption("The agenda. Item one is 'vibes'.",
                            SCORE_MISSED,
                            "Item one is vibes. Item two is also vibes. You learn nothing except everything."))),
                CBeat(KIND_TALK,
                    "Sundown ends the bet, but the flip-back " +
                        "needs a treaty or the town stays " +
                        "wrong-side up forever.",
                    options = listOf(
                        COption("“At sundown, everything flips back — and the meeting keeps the minutes.”",
                            SCORE_LANDED,
                            "Ratified. The town will un-swap at sundown, and today's minutes go into the real archive, where they will embarrass future councils forever."),
                        COption("“Everyone keeps ONE upside-down thing. Just one.”",
                            SCORE_CLOSE,
                            "The treaty holds, mostly. (Randy's one thing is the recess bell. Everyone saw this coming.)"),
                        COption("“New motion: every day is upside-down day now.”",
                            SCORE_MISSED,
                            "Defeated by acclamation and one thrown eraser. The treaty happens anyway, the way weather happens: later, and to everyone.")))),
            closing = "The gavel comes down. The town stays " +
                "exactly as wrong as it was — but now it's " +
                "wrong ON THE RECORD.",
            heroicLine = "You chaired the kids' meeting to a treaty and got recess enshrined.",
            handledLine = "You kept the meeting governing until sundown, which is more than most councils manage.",
            fumbledLine = "Your chairing is in the minutes under 'incidents', plural."),

        ClimaxChain(
            eventId = "tegridy_takeover",
            intro = "The harvest party is peaking under a " +
                "suspiciously perfect sun. The banners are up, " +
                "the merch table is doing numbers, and " +
                "Sharon's tone is gathering on the horizon " +
                "like weather. This party needs a steady hand.",
            beats = listOf(
                CBeat(KIND_TALK,
                    "A line has formed at the merch table and " +
                        "Randy is 'supervising', which means " +
                        "the table is yours.",
                    options = listOf(
                        COption("“Appreciation Day special: honest towels, honest prices.”",
                            SCORE_LANDED,
                            "The line moves like a river. Towelie weeps with professional pride. Randy calls it 'brand synergy' and takes the credit, which is also a kind of harvest."),
                        COption("“Autographs from the boys — they're basically celebrities.”",
                            SCORE_CLOSE,
                            "The boys sign merch for twenty minutes. Kevin's autograph appreciates in value twice. The table survives. Your conscience files a minor complaint."),
                        COption("“Everything must go! Randy said so! (Randy didn't.)”",
                            SCORE_MISSED,
                            "The table is stampeded politely. Randy's face does three emotions at once. The discount is honored. The discount is eternal now."))),
                CBeat(KIND_LOOK,
                    "A party this size has a weather system. " +
                        "Read it before it breaks.",
                    options = listOf(
                        COption("Sharon, at the edge, saying 'Randy.' in THAT tone. Four times. Counting.",
                            SCORE_LANDED,
                            "You clock it early: four tones is the warning line. There's still time to land this party soft — and now the whole farm knows you saw it coming."),
                        COption("Towelie, asleep standing up in a stack of his own towels.",
                            SCORE_CLOSE,
                            "A masterpiece of camouflage. No action needed. Almost respect."),
                        COption("The sun. It's perfect. Too perfect. Suns shouldn't aim.",
                            SCORE_MISSED,
                            "You stare at the sun for the truth. The sun wins. Your eyes file the complaint."))),
                CBeat(KIND_PHOTO,
                    "Before the come-down, this party deserves " +
                        "one honest photograph — the whole " +
                        "beautiful mess, here at the farm, while " +
                        "the sun holds. Then show the host.",
                    taskDone = "You get it: banners, towels, Randy mid-grandeur, Sharon mid-tone. The photo will out-argue every version of today anyone tells later.",
                    taskSkip = "No photo. By tomorrow the party will have been twelve feet tall and attended by celebrities. Memory is a brand journey too.")),
            closing = "The sun holds to the end. The banners " +
                "flutter. The party peaks exactly as high as " +
                "a party can peak before the tone arrives.",
            heroicLine = "You ran the merch table, read Sharon's tone early, and got the shot.",
            handledLine = "You kept the party upright until the sun was done with it.",
            fumbledLine = "Your shift at the merch table is now a cautionary tale told to new towels."),

        ClimaxChain(
            eventId = "mole_panic",
            intro = "The church is full, the doors are barred " +
                "with a lawn chair, and something under the " +
                "floor just knocked. Every face turns — not " +
                "to Father Maxi. To you. The room needs holding.",
            beats = listOf(
                CBeat(KIND_LOOK,
                    "You kneel and put your ear to the " +
                        "floorboards while the town holds " +
                        "its breath.",
                    options = listOf(
                        COption("Knock… knock-knock… pause. It swells and fades like something cooling. Like pipes.",
                            SCORE_LANDED,
                            "You've got it: a rhythm with a thermostat's soul. Metal cooling. Pipes. You stand up with the truth in your hands — now the room has to survive hearing it."),
                        COption("It's Morse. It spells H-E-L— no. It's just knocking.",
                            SCORE_CLOSE,
                            "For one bad second the whole church leans in. Then it knocks twice more, rudely un-alphabetical. Half the room exhales. Half sharpens."),
                        COption("Definitely teeth. A thousand of them. Chattering in shifts.",
                            SCORE_MISSED,
                            "You report the teeth theory with total confidence. The barricades double. Somewhere, a plumber is about to have a day."))),
                CBeat(KIND_STAND,
                    "The knocking is under the floor and the " +
                        "whole congregation is one wobble from " +
                        "a stampede. You're the still point. " +
                        "Be it.",
                    escalations = listOf(
                        Escalation("The knocking starts again — and the back pew starts to go.",
                            listOf("Stand up slow, and stay standing.",
                                "Rush the floorboards with a hymnal.",
                                "Agree, loudly, that it's teeth."), 0),
                        Escalation("A kid whimpers. The lawn chair barricading the door begins to slide.",
                            listOf("Offer your seat. Don't move otherwise.",
                                "Barricade yourself into the front pew.",
                                "Start knocking back."), 0),
                        Escalation("Father Maxi looks at YOU for the word. The knocking peaks.",
                            listOf("Nod. Steady. Let it pass through you.",
                                "Give the word: RUN.",
                                "Give a speech about teeth."), 0)),
                    standWin = "Three surges, three times you were the still point. The knocking fades. The room breathes out like one animal. Whatever's under there, it met someone it couldn't scare.",
                    standClose = "You held through the worst of it — one crack, at the very peak, and even that you'll all agree never happened. The room steadies.",
                    standMiss = "The room broke and you were, technically, part of the weather. The pews survive. Dignity is in the lost and found."),
                CBeat(KIND_TALK,
                    "The room is ready to hear it — or to " +
                        "throw you to the mole people. Choose " +
                        "the words.",
                    options = listOf(
                        COption("“It's the pipes. It's always been the pipes. And we handled it like people.”",
                            SCORE_LANDED,
                            "A laugh — the good kind, the kind a room laughs at itself. The lawn chair comes off the door. Father Maxi blesses the pipes on his way out, just in case."),
                        COption("“Whoever's down there: we're coming down. Politely.”",
                            SCORE_CLOSE,
                            "The room cheers the politeness policy. The pipes, wisely, say nothing. It's taken as surrender."),
                        COption("“IT WAS MOLE PEOPLE AND THEY RETREATED BECAUSE OF ME.”",
                            SCORE_MISSED,
                            "Cartman's line. You said Cartman's line. It is now your line forever, and the town will attribute it correctly.")))),
            closing = "The church empties into the evening " +
                "like nothing happened, which is the town's " +
                "way of saying everything did.",
            heroicLine = "You heard the pipes, held the room through three surges, and told the truth kindly.",
            handledLine = "You got the church through the knocking with most of its dignity in one piece.",
            fumbledLine = "The church remembers your part as 'the teeth speech'. Fondly. Endlessly."),

        ClimaxChain(
            eventId = "silent_challenge",
            intro = "Two finalists. One park full of people " +
                "watching two kids not talk. Kenny's parka " +
                "says nothing. Henrietta's stare says less. " +
                "The showdown needs a referee with nerve — " +
                "and that's you.",
            beats = listOf(
                CBeat(KIND_LOOK,
                    "A silent final is won in inches. Watch " +
                        "for the tell.",
                    options = listOf(
                        COption("Henrietta's stare is drifting — a goth eyelid at half-mast.",
                            SCORE_LANDED,
                            "You clock the drift early. The end is coming, and you'll be ready for the exact frame it lands in."),
                        COption("Kenny's parka moved. Breathing. Breathing is legal. Probably.",
                            SCORE_CLOSE,
                            "You make the call: still in. The crowd respects the ruling and does not respect you for needing to make it."),
                        COption("Cartman is holding a sign that says 'HE'S LOSING'. It specifies no one.",
                            SCORE_MISSED,
                            "You confiscate nothing, learn nothing, and the sign goes down in the records as 'atmospherics'."))),
                CBeat(KIND_STAND,
                    "You take a shift in the stare-down " +
                        "yourself — the referee's honor is on " +
                        "the line, and the silence is armed.",
                    escalations = listOf(
                        Escalation("A sneeze is building in the crowd. It is looking for a host body. Yours.",
                            listOf("Let it build. Let it pass. Say nothing.",
                                "Sneeze in Morse. Nobody will know.",
                                "Announce a sneeze warning."), 0),
                        Escalation("Somebody's phone buzzes — in YOUR pocket. Heads turn like sunflowers.",
                            listOf("Stone face. The buzz dies alone.",
                                "Check it under the table of your dignity.",
                                "Say 'it's probably nothing', to no one."), 0),
                        Escalation("Butters, at the fence, whispers 'oh, hamburgers…' directly into the silence.",
                            listOf("Hold. The silence holds because you do.",
                                "Laugh once. Briefly. Fatally.",
                                "Shush him at full volume."), 0)),
                    standWin = "Three traps, three times stone. The crowd will swear you didn't move for an hour. Kenny tips his parka to you — the highest honor the silence can give.",
                    standClose = "You cracked on the last one — a single human sound, gone in a breath. The finalists, graciously, pretend it was the wind.",
                    standMiss = "The silence record books will note your contribution under 'interruptions, local'. The finalists outlasted you from inside the contest."),
                CBeat(KIND_TALK,
                    "Somebody just blinked — er, talked. It's " +
                        "over. The first words about it belong " +
                        "to the referee.",
                    options = listOf(
                        COption("“Kenny wins. The parka takes the pizza.”",
                            SCORE_LANDED,
                            "The park erupts. Kenny shares the pizza with the dignity of a champion. Henrietta nods once — the goth seal of legitimate defeat."),
                        COption("“PIZZA HAS BEEN ACHIEVED.”",
                            SCORE_CLOSE,
                            "Not a ruling. A weather report. But the pizza arrives, and history is kind to whoever feeds it."),
                        COption("“I WAS NEVER EVEN PLAYING AND I TALKED FIRST, SO—”",
                            SCORE_MISSED,
                            "You claim a title that doesn't exist in a contest that just ended. The town adds it to your file, under 'confidence'.")))),
            closing = "The park is LOUD again, all at once — " +
                "like a held breath let go by a hundred people.",
            heroicLine = "You refereed the silent final, held your own silence through three traps, and called it clean.",
            handledLine = "You kept the showdown honest until somebody's mouth lost.",
            fumbledLine = "The quiet game's official record lists you as a sound effect."),

        ClimaxChain(
            eventId = "coon_sightings",
            intro = "Fog, a crowd, and a town that has " +
                "collectively decided to see a vigilante. " +
                "Every trash can on Main Street is evidence. " +
                "The big sighting is coming, and you're in " +
                "the front row of history.",
            beats = listOf(
                CBeat(KIND_LOOK,
                    "The fog is doing layers. Somewhere in " +
                        "it: a cape, allegedly. Watch like a " +
                        "professional.",
                    options = listOf(
                        COption("Low. A cape hem at RACCOON knee height. And it moves in sections.",
                            SCORE_LANDED,
                            "Three sections. You count three sections, and the sections have whiskers. You know exactly what's crossing Main Street — the question is what the town does with the knowing."),
                        COption("Eyes. Three pairs. Stacked vertically, blinking out of sync.",
                            SCORE_CLOSE,
                            "You see them. You choose, for one more minute, to be a person who has seen eyes and nothing more."),
                        COption("A heroic silhouette. Muscular. Definitely the Coon. Definitely.",
                            SCORE_MISSED,
                            "You announce the silhouette. The silhouette trips over a trash can. History records your confidence verbatim."))),
                CBeat(KIND_TALK,
                    "Barbrady is giving his official report to " +
                        "anyone with ears. Craig's gang is " +
                        "giving the other one. Somebody should " +
                        "get a version down straight.",
                    options = listOf(
                        COption("“Officer. Slowly. From the cape. Leave nothing out.”",
                            SCORE_LANDED,
                            "Barbrady delivers the most complete statement of his career: time, place, cape-status, whisker-status. It will hold up in any court the town cares to invent."),
                        COption("“Craig. Your gang's version. The short one.”",
                            SCORE_CLOSE,
                            "Craig flips off the fog and says 'raccoons', in a tone that ends departments. Not evidence. But closure-adjacent."),
                        COption("“THE COON IS REAL AND HE IS HANDSOME.”",
                            SCORE_MISSED,
                            "You have made a statement to the press of the sidewalk. Kyle's face will be quoted in the follow-up coverage."))),
                CBeat(KIND_PHOTO,
                    "When it crosses, there will be one second " +
                        "of visibility. The town needs a " +
                        "photograph — here on Main Street, in " +
                        "the fog, while the climax is live. " +
                        "Then show the host.",
                    taskDone = "You GOT it — cape, whiskers, three distinct dignity-free silhouettes stacked in ambition. The photo ends the panic and starts the legend.",
                    taskSkip = "The moment crosses unphotographed. By morning it will have been nine feet tall, and no one alive will be able to prove otherwise.")),
            closing = "The fog lifts on a street full of " +
                "knocked cans and satisfied citizens. Case " +
                "status: legend.",
            heroicLine = "You spotted the sections, took Barbrady's statement straight, and got the photo.",
            handledLine = "You kept the sighting honest enough that the legend has footnotes.",
            fumbledLine = "Your contributions to the Coon record are filed under 'eyewitness, enthusiastic'."),

        ClimaxChain(
            eventId = "memberberry_fever",
            intro = "An unplanned class reunion for classes " +
                "that never existed is peaking on Main " +
                "Street, and everyone is 'membering at full " +
                "volume. The present is having a day off. " +
                "Somebody has to emcee the past.",
            beats = listOf(
                CBeat(KIND_TALK,
                    "The crowd is trading flashbacks like " +
                        "currency. Your turn comes around " +
                        "like a collection plate.",
                    options = listOf(
                        COption("“'Member when the town fixed its own problems by Friday?”",
                            SCORE_LANDED,
                            "A hush of respect. That's a deep cut — a 'member with calluses on it. Randy wipes his eye and calls it 'the good stuff'."),
                        COption("“'Member last Tuesday?”",
                            SCORE_CLOSE,
                            "Polite 'membering. Last Tuesday was fine. Nobody's eyes mist. The reunion keeps its dignity and its schedule."),
                        COption("“'Member the FUTURE?”",
                            SCORE_MISSED,
                            "The crowd stares. You have broken the only rule the past has. A berry is taken from you, ceremonially."))),
                CBeat(KIND_LOOK,
                    "Somewhere in this reunion, the present " +
                        "is still happening. Find it.",
                    options = listOf(
                        COption("Mrs. Tweek is labeling muffins with TODAY'S date, and means it.",
                            SCORE_LANDED,
                            "One anchor in the present tense, holding the line with a label maker. You note her position like a lighthouse."),
                        COption("The 'vintage' berry tags — dated this morning, priced like antiques.",
                            SCORE_CLOSE,
                            "Commerce marches on, even backwards. Randy catches your eye and does not wink, which is worse."),
                        COption("Your own reflection in a shop window, looking strangely older.",
                            SCORE_MISSED,
                            "It's the nostalgia light. Probably. You choose not to investigate your own subplot."))),
                CBeat(KIND_PHOTO,
                    "A reunion this historic needs its class " +
                        "photo — here on Main Street, the whole " +
                        "class of whenever, while the fever is " +
                        "at its peak. Then show the host.",
                    taskDone = "Everyone 'members to face the camera. The photo will somehow be old the moment it develops. The town will treasure it incorrectly for years.",
                    taskSkip = "No photo. The reunion will be 'membered exclusively, each version grander, until it has a parade and a war in it.")),
            closing = "The fever peaks, glows, and begins its " +
                "slow slide into a story everyone will " +
                "misremember together.",
            heroicLine = "You out-'membered the town, found the present, and took the class photo.",
            handledLine = "You emceed the past without getting stuck in it.",
            fumbledLine = "Your 'member the future is quoted at every reunion from now on. There will be more reunions."),

        ClimaxChain(
            eventId = "imagination_leak",
            intro = "One speech, one pie break, one speech — " +
                "and the town decides what the shimmering " +
                "IS. Miracle, menace, or mascot. The glitter " +
                "has already given its opening statement. " +
                "Yours is due.",
            beats = listOf(
                CBeat(KIND_TALK,
                    "The floor is yours. Three positions are " +
                        "on the table, and the pie is watching.",
                    options = listOf(
                        COption("“It's a mascot. Every town has one. Ours is just honest about it.”",
                            SCORE_LANDED,
                            "The room settles like a held note. A mascot can be lived with. A mascot can be MERCHANDISED. The motion carries the hall."),
                        COption("“It's a miracle, and we should all be quietly weird about it.”",
                            SCORE_CLOSE,
                            "Father Maxi's eyebrows file a mixed review. The motion doesn't carry, but nobody's sure what did. Pie break heals all."),
                        COption("“It's a menace, and the glitter is its ADVANCE GUARD.”",
                            SCORE_MISSED,
                            "The goths slow-clap your commitment to a position nobody can use. The menace motion fails. The glitter, somewhere, approves of the drama."))),
                CBeat(KIND_STAND,
                    "Your motion is on the floor, and the " +
                        "debate has three more surges in it. " +
                        "Hold your position.",
                    escalations = listOf(
                        Escalation("The pie-break heckling starts: somebody's uncle demands a recount of the glitter.",
                            listOf("Let him finish. Then answer the point, not the uncle.",
                                "Demand the glitter be sworn in as a witness.",
                                "Concede everything. Loudly. All at once."), 0),
                        Escalation("A second glitter statement arrives — delivered by shoe, tracked across the church floor.",
                            listOf("Acknowledge it gravely. Move on. Mascots shed.",
                                "Announce the debate is haunted and adjourn to the parking lot.",
                                "Pick a fight with the shoe."), 0),
                        Escalation("Father Maxi rises with a pamphlet for every position at once. The hall wobbles.",
                            listOf("Thank him for his service to all sides. Hold the line.",
                                "Take a pamphlet and read it aloud, in a panic.",
                                "Second the pamphlet as your entire platform."), 0)),
                    standWin = "Three surges and your line never moved. The hall takes its cue from the still point. South Park has decided things with less spine than that.",
                    standClose = "You wobbled on the last surge — pamphlet-adjacent — but the hall had mostly decided by then. It counts.",
                    standMiss = "The debate's official minutes list your position as 'vibes, shifting'. The mascot motion carried without you, which the goths call very on-brand for reality."),
                CBeat(KIND_LOOK,
                    "While the hall argues, the evidence is " +
                        "on the floor. One careful look at the " +
                        "glitter trail.",
                    options = listOf(
                        COption("It loops — out the door, down the steps, back toward the pond. A round trip.",
                            SCORE_LANDED,
                            "You see the whole shape of it: it came from the pond, and it's going home to the pond. The debate has been about a commute."),
                        COption("It spells nothing. It's glitter. It does what it wants.",
                            SCORE_CLOSE,
                            "A deflation and a relief in one sparkle. You report 'no message', and the hall respects the honesty."),
                        COption("It spells your name. It definitely spells your name.",
                            SCORE_MISSED,
                            "It does not spell your name. You will be telling people it did, and some will believe you, and that is how legends work.")))),
            closing = "The vote is taken, the pie is gone, " +
                "and the shimmering — whatever the town " +
                "decided it is — has already left by the " +
                "pond road.",
            heroicLine = "You carried the mascot motion, held your line through three surges, and read the glitter's round trip.",
            handledLine = "You got the town to a decision about the undecidable, which is the job.",
            fumbledLine = "Your debate legacy is 'vibes, shifting' — and the glitter spells nothing. Officially."),

        ClimaxChain(
            eventId = "gold_fever",
            intro = "One scale, four hundred heirlooms, zero " +
                "chill — and Jimbo on a microphone with " +
                "strong feelings about karats. The Great " +
                "Weigh-In needs one honest set of eyes " +
                "before the town sells its history for keychains.",
            beats = listOf(
                CBeat(KIND_LOOK,
                    "The scale is the whole economy today. " +
                        "Watch it like it owes you money. " +
                        "It does.",
                    options = listOf(
                        COption("Jimbo's thumb. ON the scale. Doing thumb things. During a weighing.",
                            SCORE_LANDED,
                            "You clock it in the first minute: every heirloom in town is somehow a thumb heavier going in and lighter paying out. The microphone is about to become a liability — his."),
                        COption("The microphone weighs more than most of the brooches.",
                            SCORE_CLOSE,
                            "A fact. A useless, gleaming fact. Jimbo narrates a brooch up two karats on charm alone."),
                        COption("Everything here is definitely gold. All of it. The chairs too.",
                            SCORE_MISSED,
                            "You briefly consider selling the bank its own furniture. The fever thanks you for your service."))),
                CBeat(KIND_TALK,
                    "Jimbo's microphone is live and the line " +
                        "is listening. There's a number the " +
                        "whole town needs to hear in a human voice.",
                    options = listOf(
                        COption("“Nine cents on the dollar. The keychain is the PRODUCT, people.”",
                            SCORE_LANDED,
                            "The line goes quiet the way lines do when arithmetic lands. Jimbo's commentary slows to a respectful murmur. People start holding their heirlooms a little tighter."),
                        COption("“Quick question about the keychain quality, actually.”",
                            SCORE_CLOSE,
                            "Jimbo, delighted, reviews the keychain for six full minutes. The line learns nothing about gold and everything about the keychain. A partial victory."),
                        COption("“I'll sell you this keychain BACK. Vintage. One owner.”",
                            SCORE_MISSED,
                            "He buys it. For nine cents. You have participated in the economy, and the economy has won."))),
                CBeat(KIND_FIND,
                    "Somewhere in the weigh-in shuffle, one " +
                        "heirloom has gone astray. Mrs. Tweek's " +
                        "brooch count is one short, and the " +
                        "misfile tray by the scale has been " +
                        "swallowing small shiny things all day. " +
                        "Look at the Bank's floor properly. " +
                        "Then bring it back.",
                    taskDone = "You return the brooch in front of the line. The applause is small and completely real — the weigh-in's one unambiguous transaction.",
                    taskSkip = "The brooch stays in the tray. It'll turn up in spring cleaning, acquire a legend, and be weighed again next fever.",
                    findLoc = "Bank",
                    findText = "🪙 In the misfile tray, under a drift of keychains: a brooch, tagged 'MISC — SHINY'. It's the missing one. You lift it out like it matters, because to somebody it does.")),
            closing = "The scale is packed away. The heirlooms " +
                "go home. The keychains remain — genuinely nice, " +
                "as keychains go.",
            heroicLine = "You caught the thumb, said the math into the microphone, and returned the brooch.",
            handledLine = "You kept the weigh-in honest enough that the town kept its heirlooms.",
            fumbledLine = "You are now the proud owner of a secondhand keychain, purchased at the peak. The fever salutes you.")

    ).associateBy { it.eventId }

    // ---------------- state ----------------

    private fun base(state: GameState, def: ShenaniganDef): String =
        "climax:${def.id}@${state.shenaniganDay}"

    private fun activeDef(state: GameState): ShenaniganDef? =
        state.shenaniganId?.let { SHENANIGAN_BY_ID[it] }
            ?.takeIf { it.id in CHAINS }

    /** The turf the climax plays on: the climax stage's
     *  cluster, falling back to the escalation's (the same
     *  spot the wrap leaves its mark). */
    fun climaxLoc(def: ShenaniganDef): String? =
        def.stages[2].clusterLoc ?: def.stages[1].clusterLoc

    fun chainOf(def: ShenaniganDef): ClimaxChain? = CHAINS[def.id]

    fun isLive(state: GameState): Boolean {
        val def = activeDef(state) ?: return false
        return Shenanigans.stageFor(state.timeMin) == 2 &&
            state.lifeText["${base(state, def)}:settled"] == null
    }

    /** v0.9.0b: the live climax's causers, as a state-only
     *  query (the crew's occupied-check reads it: a causer
     *  mid-climax is busy, wherever the player is standing). */
    fun liveCausers(state: GameState): Set<String> {
        if (!isLive(state)) return emptySet()
        val def = activeDef(state) ?: return emptySet()
        if (isComplete(state, def)) return emptySet()
        return def.causers.toSet()
    }

    private fun started(state: GameState, def: ShenaniganDef): Boolean =
        state.lifeText["${base(state, def)}:started"] != null

    private fun idx(state: GameState, def: ShenaniganDef): Int =
        state.life["${base(state, def)}:idx"] ?: 0

    /** Per-beat scores; null where a beat hasn't resolved. */
    fun scoresOf(state: GameState, def: ShenaniganDef): List<Int?> {
        val b = base(state, def)
        return (0 until BEATS_PER_CHAIN).map { state.life["$b:s$it"] }
    }

    fun resolvedCount(state: GameState, def: ShenaniganDef): Int =
        scoresOf(state, def).count { it != null }

    fun isComplete(state: GameState, def: ShenaniganDef): Boolean =
        resolvedCount(state, def) >= BEATS_PER_CHAIN

    /** The tier the chain stands at, if it has been played
     *  at all: played beats plus (at the wrap) the unplayed
     *  counted as missed. Null when there's nothing to
     *  remember — never opened, or opened and untouched. */
    fun tierOf(state: GameState, def: ShenaniganDef): String? {
        if (!started(state, def)) return null
        val scores = scoresOf(state, def)
        if (scores.all { it == null }) return null
        return tierFromScores(scores.map { it ?: SCORE_MISSED })
    }

    // ---------------- the TalkGate rail ----------------

    /** A Talk with [npcName] plays the climax when the event
     *  is at its peak, the player stands on the climax turf,
     *  and the person is one of the event's causers — any of
     *  them can host; the chain is the day's, not theirs. */
    fun talkStepFor(state: GameState, npcName: String,
                    playerLocId: String): Boolean {
        if (!isLive(state)) return false
        val def = activeDef(state) ?: return false
        if (isComplete(state, def)) return false
        if (playerLocId != climaxLoc(def)) return false
        return npcName in def.causers
    }

    // ---------------- the session ----------------

    data class SBeat(val kind: String, val title: String,
                     val text: String,
                     val options: List<String> = emptyList())

    /** The step the dialog chain presents right now:
     *  intro → per-beat (choice / stand / task) with result
     *  cards between → done. Null when no climax is live. */
    fun sessionBeat(state: GameState): SBeat? {
        val def = activeDef(state) ?: return null
        if (!isLive(state)) return null
        val chain = CHAINS[def.id] ?: return null
        val stageTitle = def.stages[2].title
        pending(state, def)?.let {
            return SBeat("result", stageTitle, it)
        }
        if (!started(state, def))
            return SBeat("intro", stageTitle, chain.intro)
        if (isComplete(state, def))
            return SBeat("done", def.title, chain.closing)
        val i = idx(state, def)
        val beat = chain.beats[i]
        return when (beat.kind) {
            KIND_STAND -> {
                val step = standStep(state, def, i)
                val esc = beat.escalations[step]
                val teach = if (state.lifeText["climax:taught"] == null)
                    STAND_TEACHING else ""
                SBeat("stand", stageTitle,
                    (if (step == 0) beat.text + "\n\n" else "") +
                        teach + esc.pressure,
                    esc.options)
            }
            KIND_PHOTO, KIND_FIND ->
                SBeat("task", stageTitle, beat.text)
            else ->
                SBeat("choice", stageTitle, beat.text,
                    beat.options.map { it.label })
        }
    }

    /** The beat the chain stands on, if any. */
    fun currentBeat(state: GameState): CBeat? {
        val def = activeDef(state) ?: return null
        return CHAINS[def.id]?.beats?.getOrNull(idx(state, def))
    }

    /** The STAND surge the chain stands in, if it stands
     *  in one — its holdIndex says which response holds. */
    fun currentEscalation(state: GameState): Escalation? {
        val def = activeDef(state) ?: return null
        val beat = currentBeat(state) ?: return null
        if (beat.kind != KIND_STAND) return null
        return beat.escalations.getOrNull(standStep(state, def,
            idx(state, def)))
    }

    private fun pending(state: GameState, def: ShenaniganDef): String? =
        state.lifeText["${base(state, def)}:pending"]

    private fun setPending(state: GameState, def: ShenaniganDef,
                           text: String) {
        state.lifeText["${base(state, def)}:pending"] = text
    }

    /** Step into the chain (the intro's ▶). */
    fun begin(state: GameState) {
        val def = activeDef(state) ?: return
        if (!isLive(state)) return
        state.lifeText["${base(state, def)}:started"] = "1"
        state.life["${base(state, def)}:idx"] = 0
    }

    /** The result card's ▶: clear it and move to the next
     *  beat (or the closing, when that was the last). */
    fun advance(state: GameState) {
        val def = activeDef(state) ?: return
        val b = base(state, def)
        state.lifeText.remove("$b:pending")
        if (idx(state, def) < BEATS_PER_CHAIN)
            state.life["$b:idx"] = idx(state, def) + 1
    }

    /** Resolve the current beat: record its score, pay its
     *  participation into the event's own ledger, and leave
     *  the result card up for the ▶. */
    private fun resolveBeat(state: GameState, def: ShenaniganDef,
                            score: Int, result: String) {
        val b = base(state, def)
        val i = idx(state, def)
        state.life["$b:s$i"] = score
        state.participation =
            (state.participation + BEAT_PARTICIPATION).coerceAtMost(10)
        setPending(state, def, result)
        if (CHAINS[def.id]?.beats?.getOrNull(i)?.kind == KIND_STAND)
            state.lifeText["climax:taught"] = "1"
    }

    /** A TALK/LOOK choice row pick. */
    fun choose(state: GameState, which: Int) {
        val def = activeDef(state) ?: return
        if (!isLive(state) || pending(state, def) != null) return
        val beat = CHAINS[def.id]?.beats?.getOrNull(idx(state, def))
            ?: return
        if (beat.kind != KIND_TALK && beat.kind != KIND_LOOK) return
        val opt = beat.options.getOrNull(which) ?: return
        resolveBeat(state, def, opt.score, opt.result)
    }

    // ---------------- STAND: hold your ground ----------------

    private fun standStep(state: GameState, def: ShenaniganDef,
                          beatIdx: Int): Int {
        val b = base(state, def)
        if ((state.life["$b:standFor"] ?: -1) != beatIdx) {
            state.life["$b:standFor"] = beatIdx
            state.life["$b:standStep"] = 0
        }
        return state.life["$b:standStep"] ?: 0
    }

    /** One surge answered: holding advances the stand;
     *  breaking resolves the beat — a crack on the third
     *  surge is a near thing, anything earlier is a miss. */
    fun standChoose(state: GameState, which: Int) {
        val def = activeDef(state) ?: return
        if (!isLive(state) || pending(state, def) != null) return
        val i = idx(state, def)
        val beat = CHAINS[def.id]?.beats?.getOrNull(i) ?: return
        if (beat.kind != KIND_STAND) return
        val step = standStep(state, def, i)
        val esc = beat.escalations.getOrNull(step) ?: return
        val b = base(state, def)
        if (which == esc.holdIndex) {
            if (step >= beat.escalations.size - 1) {
                resolveBeat(state, def, SCORE_LANDED, beat.standWin)
            } else {
                state.life["$b:standStep"] = step + 1
            }
        } else {
            if (step >= beat.escalations.size - 1)
                resolveBeat(state, def, SCORE_CLOSE, beat.standClose)
            else
                resolveBeat(state, def, SCORE_MISSED, beat.standMiss)
        }
    }

    // ---------------- out-of-band tasks ----------------

    private fun shotFlag(state: GameState, def: ShenaniganDef): Boolean =
        state.lifeText["climaxShot:${def.id}@${state.shenaniganDay}"] != null

    private fun findFlag(state: GameState, def: ShenaniganDef): Boolean =
        state.lifeText["climaxFind:${def.id}@${state.shenaniganDay}"] != null

    fun taskDone(state: GameState, def: ShenaniganDef,
                 beat: CBeat): Boolean = when (beat.kind) {
        KIND_PHOTO -> shotFlag(state, def)
        KIND_FIND -> findFlag(state, def)
        else -> false
    }

    /** The task card's row: 0 = show them (lands it if the
     *  deed is done; otherwise a note and the chain waits),
     *  1 = let this one go (the beat resolves as a miss),
     *  2 = step away (nothing changes). Returns a note to
     *  show when the deed isn't done yet, else null. */
    fun taskAction(state: GameState, action: Int): String? {
        val def = activeDef(state) ?: return null
        if (!isLive(state) || pending(state, def) != null) return null
        val beat = CHAINS[def.id]?.beats?.getOrNull(idx(state, def))
            ?: return null
        if (beat.kind != KIND_PHOTO && beat.kind != KIND_FIND)
            return null
        return when (action) {
            0 -> if (taskDone(state, def, beat)) {
                resolveBeat(state, def, SCORE_LANDED, beat.taskDone)
                null
            } else if (beat.kind == KIND_PHOTO)
                "Not yet — no climax photo. Take one here, " +
                    "while it's live, and come back."
            else
                "Not yet — the thing's still out there. " +
                    "Look in the right place, then come back."
            1 -> {
                resolveBeat(state, def, SCORE_MISSED, beat.taskSkip)
                null
            }
            else -> null
        }
    }

    /** The shutter hears a photo: a shot taken on the
     *  climax turf while the climax is live counts toward
     *  the chain's PHOTO beat — taken before the chain even
     *  opens, it still counts; the camera was there. */
    fun notePhoto(state: GameState, locId: String) {
        val def = activeDef(state) ?: return
        if (Shenanigans.stageFor(state.timeMin) != 2) return
        if (locId != climaxLoc(def)) return
        val chain = CHAINS[def.id] ?: return
        if (chain.beats.none { it.kind == KIND_PHOTO }) return
        state.lifeText["climaxShot:${def.id}@${state.shenaniganDay}"] = "1"
    }

    /** The Look chain's piece: a pending FIND turns up at
     *  its spot (once, while the climax is live), and on the
     *  climax turf the live chain advertises itself. */
    fun lookText(state: GameState, locId: String): String? {
        val def = activeDef(state) ?: return null
        if (Shenanigans.stageFor(state.timeMin) != 2) return null
        val chain = CHAINS[def.id] ?: return null
        val findBeat = chain.beats.firstOrNull { it.kind == KIND_FIND }
        if (findBeat != null && locId == findBeat.findLoc &&
            !findFlag(state, def)) {
            state.lifeText["climaxFind:${def.id}@${state.shenaniganDay}"] = "1"
            return findBeat.findText
        }
        if (locId == climaxLoc(def) && isLive(state) &&
            !isComplete(state, def)) {
            val host = def.causers.firstOrNull() ?: "the causers"
            return if (started(state, def))
                "🎬 The climax of “${def.title}” is still live — " +
                    "talk to $host (or any of the causers here) to play it out."
            else
                "🎬 THE CLIMAX IS LIVE: “${def.title}” peaks here, " +
                    "now. Talk to $host — this is the part you can play."
        }
        return null
    }

    /** The stage hook: when the climax stage opens, the
     *  ticker says where it's playing and that it's
     *  playable. */
    fun noteStage(state: GameState, def: ShenaniganDef, stageIdx: Int) {
        if (stageIdx != 2 || def.id !in CHAINS) return
        val locName = climaxLoc(def)?.let { LOCATIONS[it]?.name } ?: "the scene"
        note(state, "🎬 CLIMAX: “${def.title}” peaks at $locName — " +
            "be there, and talk to one of the causers to play it.")
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    // ---------------- the wrap ----------------

    data class WrapResult(val tier: String, val outcome: String?,
                          val how: String)

    /** Settle the climax at the event's midnight wrap —
     *  exactly once. A played chain pays its causers and
     *  plants its retelling topic; an unplayed (or merely
     *  opened) one settles ABSENT with a null outcome, and
     *  the caller wraps exactly as it always has. */
    fun wrap(state: GameState, def: ShenaniganDef): WrapResult? {
        val chain = CHAINS[def.id] ?: return null
        val b = base(state, def)
        if (state.lifeText["$b:settled"] != null) return null
        val tier = tierOf(state, def)
        val result: WrapResult
        if (tier == null) {
            result = WrapResult(TIER_ABSENT, null, "")
        } else {
            val pay = when (tier) {
                TIER_HEROIC -> PAY_HEROIC
                TIER_HANDLED -> PAY_HANDLED
                else -> PAY_FUMBLED
            }
            if (pay > 0) {
                for (name in def.causers) bumpFriendship(state, name, pay)
            }
            topicForTier(tier)?.let { topic ->
                val names = (def.causers +
                    listOfNotNull(state.eventWitness)).distinct()
                for (name in names) plantTopic(name, topic)
            }
            result = WrapResult(tier, outcomeForTier(tier),
                howLineFor(chain, tier))
        }
        state.lifeText["$b:settled"] = result.tier
        prune(state, def)
        return result
    }

    /** The wrap consumes the chain's working keys (the
     *  settled flag stays, as the settle-once guard) and
     *  sweeps any older day's leftovers with them. */
    private fun prune(state: GameState, def: ShenaniganDef) {
        val b = base(state, def)
        val keep = "$b:settled"
        val deadText = state.lifeText.keys.filter {
            (it.startsWith("climax:") || it.startsWith("climaxShot:") ||
                it.startsWith("climaxFind:")) && it != keep &&
                it != "climax:taught" &&
                !it.contains("@${state.shenaniganDay}")
        }
        // the current chain's own working keys go too — all
        // but the settled flag and the once-ever taught flag
        val ownText = state.lifeText.keys.filter {
            it.startsWith("$b:") && it != keep
        }
        val ownLife = state.life.keys.filter { it.startsWith("$b:") }
        for (k in deadText + ownText) state.lifeText.remove(k)
        for (k in ownLife) state.life.remove(k)
        state.lifeText.remove("climaxShot:${def.id}@${state.shenaniganDay}")
        state.lifeText.remove("climaxFind:${def.id}@${state.shenaniganDay}")
    }

    private fun bumpFriendship(state: GameState, name: String, delta: Int) {
        if (delta == 0) return
        val cur = state.friendship[name] ?: 0
        state.friendship[name] = (cur + delta).coerceIn(-50, 100)
    }

    private fun plantTopic(name: String, topic: String) {
        if (name !in KID_ORDER) return
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(topic)
        mem.topics.add(topic)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }
}

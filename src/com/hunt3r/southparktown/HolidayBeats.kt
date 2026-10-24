package com.hunt3r.southparktown

// ============================================================
// Holiday Beats — v0.4.0d (Queue #4 item 4): every holiday
// gains ONE bespoke playable beat on top of its dressing and
// its gathering — a small story you can only play inside the
// holiday's own window, judged entirely by verbs the game
// already has:
//
//   TALK     a step lands when you talk to the right person
//            (the Talk gate judges it, like an errand delivery)
//   HOUSE    a trick-or-treat stop: Talk to the resident out
//            in costume, Look at their porch, or Give the
//            resident the good candy — first come, first served
//   LOOK     a careful Look at the right place finds the thing
//   GIVE     a real hand-off through the gift rail
//   PHOTO    a selfie at the named place (Photos.applyPhoto)
//   CATCH    any catch at the pond (Minigames.applyCatch)
//
// Beats are missable without punishment: skip a year and the
// beat returns next year, untouched. A completion is forever:
// the mark lives in capped additive save keys — one step-mask
// and one done-flag per (holiday, year) inside the existing
// `life` map, plus a per-holiday lifetime year count — and
// reads out on the Journal's 🎉 HOLIDAYS page and, for the
// first one ever, as a garland on your own wall (House.DECOR
// derives it; nothing here knows about drawing).
//
// Rewards stay at errand scale or below: a few friendship
// points spread over the cast involved, a capsule coin or
// three, one small item. The mark is the prize.
// ============================================================

/** One judged step of a holiday beat. [line] is the full
 *  narration shown when the step lands ({n}/{total} fill at
 *  fire time); [short] is its one-line ticker version.
 *  [talkLine] (HOUSE steps) is the in-costume exchange used
 *  when the step lands by Talk instead of by porch. */
data class BeatStep(
    val verb: String,            // TALK | LOOK | GIVE | PHOTO | CATCH | HOUSE
    val line: String,
    val short: String,
    val who: String? = null,     // TALK/GIVE target · HOUSE resident
    val loc: String? = null,     // required place (the NPC's for a named TALK, yours otherwise)
    val houseLoc: String? = null,// HOUSE: the porch (Look rail)
    val talkLine: String? = null,// HOUSE: the Talk rail's exchange
    val item: String? = null,    // GIVE: the item that counts
    val altItems: Set<String> = emptySet(),
    val onlyMd: String? = null,  // "MMdd" the calendar must show
    val minMin: Int = 0,         // time-of-day window, minutes
    val maxMin: Int = 1440,
    val last: Boolean = false,   // unordered beats: fires only when it's the last step standing
    val altLine: String? = null, // replaces [line] when [altArcId] resolved warm
    val altArcId: String? = null
)

data class HolidayBeat(
    val holidayId: String,
    val title: String,
    val intro: String,           // one line: what the beat is (morning note / Journal)
    val hint: String,            // the where-to-start line while it's live
    val ordered: Boolean,        // true: steps land in order · false: any order (last-flagged finisher aside)
    val steps: List<BeatStep>,
    val doneLine: String,        // the permanent Journal mark, in one line
    val rewardCoins: Int = 0,
    val rewardItem: String? = null,
    val rewardFriends: Map<String, Int> = emptyMap(),
    val ambient: Map<String, String> = emptyMap(), // cast -> their beat grumble while their step waits
    val ambientGeneric: List<String> = emptyList() // anyone else, referencing the beat
)

/** A step that just landed — what the rails show the player. */
data class BeatHit(
    val beat: HolidayBeat,
    val stepIndex: Int,
    val text: String,
    val completedNow: Boolean,
    val completionText: String?
)

object HolidayBeats {

    // ---------------- the dials ----------------

    /** Items that count as "the good candy" for a HOUSE stop's
     *  Give rail — sweets a kid would trade a route for. */
    val CANDY_ITEMS = setOf("snack_cake", "cheesy_poof")

    private fun maskKey(hid: String, iy: Int) = "hb:$hid:$iy:mask"
    private fun doneKey(hid: String, iy: Int) = "hb:$hid:$iy:done"
    private fun yearsKey(hid: String) = "hbYears:$hid"

    // ---------------- the eight beats ----------------

    val BEATS: List<HolidayBeat> = listOf(

        // ---------- HALLOWEEN: the full trick-or-treat route ----------
        HolidayBeat(
            holidayId = "halloween",
            title = "The Full Route",
            intro = "Five houses, one route, full-size bars at stake.",
            hint = "Work the route: all five kids' houses — knock (Look at the porch), " +
                "catch the kid out in costume (Talk), or bring the resident the good candy (Give). " +
                "Your own porch last.",
            ordered = false,
            steps = listOf(
                BeatStep("HOUSE",
                    line = "🎃 CARTMAN'S HOUSE — Liane answers in a bathrobe, holding a mixing bowl of " +
                        "FULL-SIZE bars like it's nothing. “Take two, sweetie. Eric's already had nine.” " +
                        "Behind her, Cartman is face-down in a pile of wrappers, bargaining with the " +
                        "concept of moderation. (Route: {n} of 5 houses.)",
                    short = "🎃 Cartman's house: FULL-SIZE bars. The route provides.",
                    who = "Cartman", houseLoc = "CartmanHouse",
                    talkLine = "Cartman is dressed as a witch. A SEXY witch. He holds out a pillowcase " +
                        "without breaking eye contact. “Trade. Your candy for my candy. It's called " +
                        "arbitrage.” Somehow you leave with candy AND he leaves with candy. " +
                        "(Route: {n} of 5 houses.)"),
                BeatStep("HOUSE",
                    line = "🎃 THE MARSH HOUSE — Sharon answers with a bowl of raisins and an apologetic " +
                        "smile that says she knows. Randy peers over her shoulder wearing last year's " +
                        "costume AND this year's, in layers, “for insulation.” You take the raisins. " +
                        "They're… fine. (Route: {n} of 5 houses.)",
                    short = "🎃 The Marsh house: raisins. Randy is dressed as two years at once.",
                    who = "Stan", houseLoc = "StanHouse",
                    talkLine = "Stan's ghost costume is a bedsheet with two eyeholes and a lifetime of " +
                        "regret. “Don't tell Wendy the sheet has a floral pattern.” He drops a fun-size " +
                        "bar in your bag out of solidarity. (Route: {n} of 5 houses.)"),
                BeatStep("HOUSE",
                    line = "🎃 THE BROFLOVSKI HOUSE — Sheila answers the door like she's auditing it. " +
                        "“ONE piece. We are not animals.” The bowl holds toothbrushes AND candy, in a " +
                        "ratio a dentist would call a cry for help. You take candy. Kyle mouths " +
                        "“sorry” from the stairs. (Route: {n} of 5 houses.)",
                    short = "🎃 The Broflovski house: toothbrushes AND candy. You chose wisely.",
                    who = "Kyle", houseLoc = "KyleHouse",
                    talkLine = "Kyle is a zombie — a MORALLY OUTRAGED zombie, the scariest kind. " +
                        "“Cartman got three houses to give him doubles. THREE. The system is broken.” " +
                        "He gives you a candy anyway. For justice. (Route: {n} of 5 houses.)"),
                BeatStep("HOUSE",
                    line = "🎃 THE STOTCH HOUSE — Stephen answers in full dad-mode, flashlight in YOUR " +
                        "eyes. “Name? Address? Candy intentions?” Linda slips you two pieces behind " +
                        "his back and winks like a smuggler. Butters, grounded from the porch, waves " +
                        "from an upstairs window in a ghost costume. (Route: {n} of 5 houses.)",
                    short = "🎃 The Stotch house: interrogated, then smuggled candy by Linda.",
                    who = "Butters", houseLoc = "ButtersHouse",
                    talkLine = "Butters the ghost floats up, beaming through the sheet. “Oh boy, trick " +
                        "or treat! I got grounded from my own porch, so I'm haunting the sidewalk " +
                        "instead!” He gives YOU candy. That's not how this works, but it's very " +
                        "Butters. (Route: {n} of 5 houses.)"),
                BeatStep("HOUSE",
                    line = "🎃 KENNY'S HOUSE — the porch light is off, but there's a bowl on the step " +
                        "with a note in wobbly letters: “TAKE ONE. — K.” Under the bowl, a second " +
                        "note: “SERIOUSLY ONE. I COUNTED. — K.” You take one. The curtain twitches. " +
                        "(Route: {n} of 5 houses.)",
                    short = "🎃 Kenny's house: the porch light's off, the bowl counts. You took ONE.",
                    who = "Kenny", houseLoc = "KennysHouse",
                    talkLine = "Kenny's vampire cape swishes. “(muffled) Mmph rmph!” He bites your neck " +
                        "(pretend), then produces — from inside the parka, somehow — a candy the " +
                        "size of a brick. Where was he KEEPING that. (Route: {n} of 5 houses.)"),
                BeatStep("LOOK",
                    line = "🎃 YOUR OWN PORCH — the bowl you left out this morning is empty except " +
                        "for ONE piece, and a note: “LEFT YOU ONE. DIDN'T WANT YOU TO MISS OUT. " +
                        "— THE ROUTE.” The neighborhood provides. You eat the last piece standing " +
                        "on your own porch like a little pumpkin king.",
                    short = "🎃 Your own porch: the route left YOU one. Full circle.",
                    loc = "YourHouse", last = true)
            ),
            doneLine = "Ran the full trick-or-treat route — all five houses, plus your own porch. " +
                "Full-size bars at Cartman's. Toothbrushes at Kyle's. Justice was served.",
            rewardCoins = 2, rewardItem = "snack_cake",
            ambient = mapOf(
                "Cartman" to "The route is MINE this year. Full-size bars, Butters. FULL. SIZE."),
            ambientGeneric = listOf(
                "You doing the full route tonight? Five houses. Legends only.",
                "I heard the Marsh house gives raisins. RAISINS. Pray for us.")),

        // ---------- CHRISTMAS: the secret-santa relay ----------
        HolidayBeat(
            holidayId = "christmas",
            title = "The Secret Santa Relay",
            intro = "Somebody has to carry Christmas across town. It's you.",
            hint = "The relay runs in order — whoever's holding a parcel is waiting on YOU. " +
                "Start with Cartman.",
            ordered = true,
            steps = listOf(
                BeatStep("TALK",
                    line = "Cartman thrusts a lopsided parcel at you. It's wrapped in newspaper and " +
                        "what appears to be a parking ticket. “You're my secret-santa mule. This " +
                        "goes to Kenny. NO shaking it. It rattles for reasons that are between me " +
                        "and God.”",
                    short = "📦 Cartman's secret-santa parcel: outbound to Kenny.",
                    who = "Cartman"),
                BeatStep("TALK",
                    line = "Kenny unwraps it on the spot. Inside: a single glove, and a note — “FOR " +
                        "THE OTHER ONE, ASK SANTA.” Kenny stares at it for a long moment. " +
                        "“(muffled) Mmph.” Then he digs in his parka and produces HIS parcel — " +
                        "neat, heavy, wrapped like a professional did it — and nods toward Butters. " +
                        "The relay provides.",
                    short = "📦 Kenny's parcel (the good one): outbound to Butters.",
                    who = "Kenny"),
                BeatStep("TALK",
                    line = "Butters opens Kenny's parcel and finds warm socks, a candy cane, and a " +
                        "drawing of the two of them holding hands. He cries. Obviously. “Oh " +
                        "hamburgers, he REMEMBERS my feet get cold!” He hands you his own parcel — " +
                        "for Liane, wrapped in paper he colored himself.",
                    short = "📦 Butters' parcel (hand-colored paper): outbound to Liane.",
                    who = "Butters"),
                BeatStep("TALK",
                    line = "Liane takes the parcel and opens it with surgical mom calm: a coupon " +
                        "book, handwritten — “ONE (1) HUG. ONE (1) DISHES, NO COMPLAINING. ONE (1) " +
                        "CLEAN ROOM (SURFACE).” Her eyes shine. “That boy.” She presses a cookie " +
                        "tin on you. “For the mule. The BEST mule.” The relay is closed.",
                    short = "📦 The relay closes at Liane's. Cookie tin acquired.",
                    who = "Liane")
            ),
            doneLine = "Ran the secret-santa relay end to end: Cartman's suspicious parcel → Kenny's " +
                "perfect one → Butters' tears → Liane's coupon book. Christmas, delivered by hand.",
            rewardItem = "snack_cake",
            rewardFriends = mapOf("Cartman" to 3, "Kenny" to 4, "Butters" to 4, "Liane" to 3),
            ambient = mapOf(
                "Cartman" to "My secret santa gift is WRAPPED. It's FINE. Stop asking what's in it.",
                "Kenny" to "(muffled) Mmph rmph. (He's been wrapping something for days. " +
                    "It has TAPE. So much tape.)"),
            ambientGeneric = listOf(
                "The relay's running — somebody's parcel is always mid-town this week.",
                "Mr. Hankey signed a hat this year. A HAT.")),

        // ---------- NEW YEAR: the countdown ----------
        HolidayBeat(
            holidayId = "newyear",
            title = "The Countdown",
            intro = "The cannons are staged. The crowd gathers at eight. Midnight does the rest.",
            hint = "Join the crowd on Main Street after 8 PM on New Year's Eve (Talk to anyone " +
                "there), then be back on Main Street for the last half hour (Look) as midnight comes.",
            ordered = true,
            steps = listOf(
                BeatStep("TALK",
                    line = "You're in the crowd now — shoulder to shoulder with the whole town, " +
                        "breath fogging, somebody's foghorn warming up in the distance. A kid on " +
                        "a dad's shoulders is already asleep. The cannons are loaded. All that's " +
                        "left is the end of the year itself. (Be on Main Street for the final " +
                        "countdown — Look, after 11:30 PM.)",
                    short = "🎆 In the countdown crowd on Main Street.",
                    loc = "MainStreet", onlyMd = "1231", minMin = 20 * 60),
                BeatStep("LOOK",
                    line = "🎆 TEN… NINE… — the whole street counts like one animal — EIGHT… " +
                        "SEVEN… — Randy is manning the cannons with a lighter and a prayer — SIX… " +
                        "FIVE… FOUR… — Kenny is counting in muffles and somehow louder than " +
                        "everyone — THREE… TWO… ONE — the sky goes white and gold, the cannons " +
                        "fire a full half-second EARLY, and the new year arrives the way everything " +
                        "arrives in South Park: ahead of schedule and slightly on fire. " +
                        "HAPPY NEW YEAR.",
                    short = "🎆 TEN… NINE… — the cannons fired early. HAPPY NEW YEAR.",
                    loc = "MainStreet", onlyMd = "1231", minMin = 23 * 60 + 30)
            ),
            doneLine = "Stood in the Main Street crowd and counted the year out — cannons early, " +
                "sky on fire, the whole town yelling. You were THERE for the turn.",
            rewardCoins = 3,
            ambientGeneric = listOf(
                "The foghorn guy is BACK. We're all a little deaf in the new year.",
                "My resolution is locked in. It's a secret. It's naps.")),

        // ---------- VALENTINE'S: cupid's route ----------
        HolidayBeat(
            holidayId = "valentine",
            title = "Cupid's Route",
            intro = "Four cards, four couples, one mailman: you.",
            hint = "Eight stops, in order: pick up from a sender, deliver to their person. " +
                "Start with Wendy.",
            ordered = true,
            steps = listOf(
                BeatStep("TALK",
                    line = "Wendy hands you a valentine sealed with a sticker and a level of " +
                        "calligraphy that should be illegal for a fourth-grader. “For Stan. And — " +
                        "this stays between us — if he throws up, that's GOOD. That's his tell.” " +
                        "You're a mailman now. A mailman of the heart.",
                    short = "💌 Carrying Wendy's valentine — for Stan.",
                    who = "Wendy"),
                BeatStep("TALK",
                    line = "Stan reads it standing up, sitting down, and then standing up again. " +
                        "His face does twelve things. “Dude. DUDE.” He presses his own valentine " +
                        "on you — for Wendy, folded nine times, structurally a weapon. The route " +
                        "provides.",
                    short = "💌 Delivered to Stan. His stomach confirms receipt.",
                    who = "Stan",
                    altLine = "Stan reads it and smiles — the easy kind, the kind that took a " +
                        "whole note-in-a-locker saga to earn. “Tell her… actually. She knows.” " +
                        "He still hands you his return valentine. Folded nine times. Some " +
                        "things don't change.",
                    altArcId = "stan_wendy_note"),
                BeatStep("TALK",
                    line = "Bebe is running a valentine operation with a clipboard. A CLIPBOARD. " +
                        "“Yours is for Clyde. Delivery window: today. Failure window: never.” " +
                        "She has a route map. You are ON the route map.",
                    short = "💌 Carrying Bebe's valentine — for Clyde. There is a clipboard.",
                    who = "Bebe"),
                BeatStep("TALK",
                    line = "Clyde reads Bebe's valentine twice, then holds it up like a trophy at " +
                        "an awards show nobody else is attending. “She DREW the shoes. She gets " +
                        "it. She GETS the shoes.” He bows to you, the messenger, with genuine " +
                        "ceremony.",
                    short = "💌 Delivered to Clyde. The shoes were drawn. He gets it.",
                    who = "Clyde"),
                BeatStep("TALK",
                    line = "Craig hands you a valentine for Tweek. It's plain, unsealed, and says " +
                        "exactly one thing, in Craig's flattest handwriting. Craig nods once. " +
                        "It's the most romantic gesture in the history of this town and he will " +
                        "not be elaborating.",
                    short = "💌 Carrying Craig's valentine — for Tweek. One sentence.",
                    who = "Craig"),
                BeatStep("TALK",
                    line = "Tweek takes the valentine like it's made of nitroglycerin and feelings. " +
                        "“OH GOD. OH GOD. IT'S — it's ONE SENTENCE. IT'S PERFECT. HOW DOES HE — " +
                        "GAH.” He reads it four more times, vibrating at a frequency only dogs " +
                        "can hear. It says: “You're okay.”",
                    short = "💌 Delivered to Tweek. Devastating. One sentence.",
                    who = "Tweek"),
                BeatStep("TALK",
                    line = "Butters has made a valentine for Kenny “because Kenny's my best friend " +
                        "and nobody ever makes him one on PURPOSE.” It's covered in stickers, " +
                        "reinforcements, and a popsicle-stick frame. It weighs two pounds. " +
                        "Love is heavy.",
                    short = "💌 Carrying Butters' valentine — for Kenny. Two pounds. Framed.",
                    who = "Butters"),
                BeatStep("TALK",
                    line = "Kenny opens it inside his parka, privately, the way Kenny does " +
                        "everything. A long pause. “(muffled) …Mmph.” He tucks it next to his " +
                        "heart — or the parka equivalent — and gives you a thumbs up that means " +
                        "more than most speeches.",
                    short = "💌 Delivered to Kenny. The thumbs up said everything.",
                    who = "Kenny")
            ),
            doneLine = "Ran Cupid's route: Wendy→Stan, Bebe→Clyde, Craig→Tweek (one sentence, " +
                "devastating), Butters→Kenny (two pounds, framed). Every card delivered. " +
                "Love, hand-carried.",
            rewardItem = "flower",
            rewardFriends = mapOf("Wendy" to 2, "Stan" to 2, "Bebe" to 2, "Clyde" to 2,
                "Craig" to 2, "Tweek" to 3, "Butters" to 2, "Kenny" to 2),
            ambient = mapOf(
                "Stan" to "If Wendy — no. Nothing. Forget it. HAPPY VALENTINE'S DAY.",
                "Tweek" to "Craig is — he's been WEIRD all week. GOOD weird?? IS there good " +
                    "weird?? GAH."),
            ambientGeneric = listOf(
                "Somebody's playing mailman for the whole town's valentines. Heroes walk among us.",
                "Kenny got a valentine in his parka hood last year. This year somebody's doing " +
                    "it on purpose.")),

        // ---------- 4TH OF JULY: the big one ----------
        HolidayBeat(
            holidayId = "july4",
            title = "The Big One",
            intro = "Randy's finale needs a steady hand and a full picnic. Yours.",
            hint = "Catch a fish at the pond today, find Randy, then be at Kid's Park after " +
                "7 PM and Look up.",
            ordered = true,
            steps = listOf(
                BeatStep("CATCH",
                    line = "The pond provides the picnic: one honest fish, caught on the Fourth " +
                        "itself. Randy's rules for the rehearsal are clear — nobody lights the " +
                        "Big One on an empty stomach. (Next: find Randy. He's… around. He's " +
                        "always around.)",
                    short = "🎣 Picnic fish caught — Randy's rehearsal is fed."),
                BeatStep("TALK",
                    line = "Randy is standing a legally safe distance from a fireworks pile the " +
                        "size of a car, holding a lighter like it owes him money. “New kid. YOU " +
                        "have the steadiest hands in this town, which is a low bar but a REAL " +
                        "one. When it's dark — the park, 7 PM — you hold the flashlight. I'll " +
                        "hold destiny.”",
                    short = "🎆 Deputized: you hold the flashlight. Randy holds destiny.",
                    who = "Randy"),
                BeatStep("LOOK",
                    line = "🎆 You hold the flashlight. Randy holds destiny. The Big One goes up " +
                        "like a second sun — a whistle, a bloom the size of the moon, gold over " +
                        "the whole park — and every car alarm in the next town goes off in " +
                        "applause. Randy is crying a little. “Probably legal,” he whispers. " +
                        "“PROBABLY legal.”",
                    short = "🎆 THE BIG ONE. The next town's car alarms applauded.",
                    loc = "KidsPark", minMin = 19 * 60)
            ),
            doneLine = "Fed the rehearsal from the pond, held Randy's flashlight, and lit the " +
                "Big One — the next town's car alarms applauded.",
            rewardCoins = 2, rewardFriends = mapOf("Randy" to 4),
            ambient = mapOf(
                "Randy" to "Tonight we light the sky a new one. Get it? A NEW ONE. " +
                    "Write that down."),
            ambientGeneric = listOf(
                "If the finale doesn't set off car alarms in Fairplay, we riot.")),

        // ---------- COW DAYS: the vigil of the patient cow ----------
        HolidayBeat(
            holidayId = "cowdays",
            title = "The Vigil of the Patient Cow",
            intro = "One cow. Seven days. She stands. The town keeps vigil.",
            hint = "Main Street: meet the cow (Look), take Jimbo's orders, document her (a " +
                "selfie on Main Street), then find Ned for the closing moo.",
            ordered = true,
            steps = listOf(
                BeatStep("LOOK",
                    line = "🐄 THE PATIENT COW regards you from the middle of Main Street, " +
                        "wearing the festival like weather. She has judged mayors, tourists, " +
                        "and one confetti cannon. Her verdict on you takes a while. Eventually " +
                        "— a single, slow blink. You've been weighed. (Next: Jimbo. The " +
                        "festival's marshal has opinions about what's required of you.)",
                    short = "🐄 The patient cow blinked at you. You've been weighed.",
                    loc = "MainStreet"),
                BeatStep("TALK",
                    line = "Jimbo jerks a thumb at the cow. “She's been standin' there since " +
                        "Tuesday, and I'll tell you what I told the tourists: that cow has seen " +
                        "things. Your job — OFFICIAL festival business — is to get her picture " +
                        "taken proper. A cow that patient deserves documentation. Ned'll close " +
                        "the ceremony out. He has a gift.”",
                    short = "🐄 Deputized by Jimbo: the cow deserves documentation.",
                    who = "Jimbo"),
                BeatStep("PHOTO",
                    line = "📸 The cow holds her pose like a professional — chin up, eyes " +
                        "level, banners behind her. Somewhere in the frame, a tourist is mooing " +
                        "with their whole chest. This is the festival's official portrait and " +
                        "everyone knows it. (Next: Ned, for the closing ceremony.)",
                    short = "📸 The official portrait of the patient cow: chin up, eyes level.",
                    loc = "MainStreet"),
                BeatStep("TALK",
                    line = "Ned raises his voice box for the ceremony. The cow turns her head. " +
                        "Ned, solemn as a judge, produces the festival's highest honor, slowed " +
                        "down by the little speaker so every tourist on Main Street can hear " +
                        "it: “Mmmmooooo.” The cow answers — one low, approving moo — and the " +
                        "crowd loses its mind. The vigil is complete. She approves of you. " +
                        "Specifically you.",
                    short = "🐄 Ned's ceremonial moo. She answered. The crowd lost it.",
                    who = "Ned")
            ),
            doneLine = "Kept the vigil of the patient cow: weighed by her blink, deputized by " +
                "Jimbo, portrait taken, and closed out by Ned's ceremonial moo. She approved. " +
                "Specifically you.",
            rewardCoins = 2, rewardFriends = mapOf("Jimbo" to 3, "Ned" to 3),
            ambient = mapOf(
                "Jimbo" to "That cow's been here since Tuesday. She's seen things, new kid. " +
                    "THINGS."),
            ambientGeneric = listOf(
                "Forty-one tourists mooed at so far today. The cow keeps count. " +
                    "The cow is winning.")),

        // ---------- ST. PATRICK'S: the shamrock hunt ----------
        HolidayBeat(
            holidayId = "stpatricks",
            title = "The Shamrock Hunt",
            intro = "Something small and green hid five shamrocks around town this morning.",
            hint = "Five paper shamrocks hide where the town learns, prays, plays, and perks " +
                "up — find four (Look), then check under Skeeter's dartboard last.",
            ordered = false,
            steps = listOf(
                BeatStep("LOOK",
                    line = "🍀 SHAMROCK {n} OF 5 — taped under the flag in the school hall, at " +
                        "exact kid height, with a note: “NO PINCHING THE FINDER.” The hunt " +
                        "is afoot.",
                    short = "🍀 Shamrock {n} of 5: the school hall.",
                    loc = "Elementary"),
                BeatStep("LOOK",
                    line = "🍀 SHAMROCK {n} OF 5 — behind the hymnals, where Father Maxi " +
                        "pretends not to know how it got there. Some mysteries are between a " +
                        "priest and a paper plant.",
                    short = "🍀 Shamrock {n} of 5: behind the hymnals.",
                    loc = "Church"),
                BeatStep("LOOK",
                    line = "🍀 SHAMROCK {n} OF 5 — under the bench, guarded by three pigeons " +
                        "who demanded a toll (crumbs, accepted).",
                    short = "🍀 Shamrock {n} of 5: the pigeons accepted crumbs.",
                    loc = "KidsPark"),
                BeatStep("LOOK",
                    line = "🍀 SHAMROCK {n} OF 5 — inside a coffee cup on the “lucky table.” " +
                        "Tweek found it first and has been staring at it for an hour. “WHY IS " +
                        "IT IN THE CUP, GAH — ” You may take it. He's relieved.",
                    short = "🍀 Shamrock {n} of 5: Tweek's cup. He's relieved.",
                    loc = "TweekBros"),
                BeatStep("LOOK",
                    line = "🍀 SHAMROCK 5 OF 5 — under the dartboard, taped to a little paper " +
                        "pot. Inside the pot: chocolate coins, exactly three, and a note in " +
                        "green ink — “YE WERE QUICKER THAN THE LEPRECHAUN. SPEND THEM LOUD.” " +
                        "The hunt is yours.",
                    short = "🍀 Shamrock 5 of 5: the pot, the coins, the note.",
                    loc = "SkeetersBar", last = true)
            ),
            doneLine = "Found all five shamrocks — school, church, park, the coffee cup, and " +
                "the pot under the dartboard — and beat the leprechaun to his own gold. " +
                "Three chocolate coins, spent loud.",
            rewardCoins = 3,
            ambientGeneric = listOf(
                "There's a leprechaun loose in this town and he owes me rent.",
                "Five shamrocks, they say. I've found zero and pinched nine people. Good day.")),

        // ---------- BLACK FRIDAY: the line feast ----------
        HolidayBeat(
            holidayId = "blackfriday",
            title = "The Line Feast",
            intro = "The line has a government now. And the government is planning Thanksgiving.",
            hint = "Butters is holding a place at the Mall and panicking about a casserole. " +
                "Start there.",
            ordered = true,
            steps = listOf(
                BeatStep("TALK",
                    line = "Butters is holding his mom's place in line with the focus of a bomb " +
                        "technician. “Oh, hamburgers — you're here! Listen: Mom made THE " +
                        "casserole — the Thanksgiving one — and it's at HOME, and the line " +
                        "provides, new kid, but the line does NOT provide casserole. If she " +
                        "doesn't bring it by feast time, the line government falls. FALLS.”",
                    short = "🦃 The line has a casserole emergency. Butters is government now.",
                    who = "Butters", loc = "Mall"),
                BeatStep("TALK",
                    line = "Linda hands you the casserole dish like it's a newborn. It's warm. " +
                        "It smells like every good decision anyone's ever made. “Stephen is " +
                        "holding the gravy down here. Tell Butters the line eats at noon, and " +
                        "tell him his mother loves him, and tell him to SIT UP in the photo " +
                        "this year.”",
                    short = "🦃 Casserole received. It smells like good decisions.",
                    who = "Linda"),
                BeatStep("TALK",
                    line = "Butters receives the casserole with both hands and a small bow. " +
                        "Around him, the line — tents, thermoses, folding chairs, one (1) " +
                        "government — produces plates like a magic trick. “THE FEAST IS SAVED. " +
                        "Tell Mom — tell her the line provides. And now it ALSO has casserole.” " +
                        "(One thing left: the line's feast photo. A selfie, here, for the fridge.)",
                    short = "🦃 The feast is saved. One photo remains.",
                    who = "Butters", loc = "Mall"),
                BeatStep("PHOTO",
                    line = "📸 The whole line, mid-feast: lawn chairs, paper plates, a turkey " +
                        "the size of a toddler, Butters front and center SITTING UP, casserole " +
                        "held high like a trophy. Behind them, the sale banners. Above them, " +
                        "the doors about to open. This photo goes on the fridge at the Stotch " +
                        "house forever.",
                    short = "📸 The Line Feast photo: fridge-door immortality.",
                    loc = "Mall")
            ),
            doneLine = "Saved the Line Feast: carried Linda's casserole from her kitchen to " +
                "the Mall line, and took the fridge-door photo. Thanksgiving happened in a " +
                "shopping line, and it was perfect.",
            rewardItem = "hot_chocolate",
            rewardFriends = mapOf("Butters" to 4, "Linda" to 4),
            ambient = mapOf(
                "Butters" to "The line provides, new kid. But it does NOT provide casserole. " +
                    "Yet."),
            ambientGeneric = listOf(
                "The line has rules. The line has a mayor. This year, the line has a feast.",
                "Forty percent off everything, one hundred percent off my dignity, and one " +
                    "casserole en route."))
    )

    fun beatFor(holidayId: String): HolidayBeat? =
        BEATS.firstOrNull { it.holidayId == holidayId }

    fun holidayOf(state: GameState): Holiday? =
        state.holidayId?.let { id -> HOLIDAYS.firstOrNull { it.id == id } }

    // ---------------- instance & progress ----------------

    /** The year of the holiday instance the calendar is
     *  currently inside (or has just left): for a window that
     *  wraps the new year, dates from Jan 1 on belong to the
     *  instance that STARTED last year — so a beat played on
     *  New Year's Eve and checked on Jan 2 reads the same key. */
    fun instanceYear(state: GameState, holiday: Holiday): Int {
        if (holiday.startMd <= holiday.endMd) return state.gameYear
        val md = state.todayKey.substring(4)
        return if (md >= holiday.startMd) state.gameYear else state.gameYear - 1
    }

    fun instanceYear(state: GameState): Int =
        holidayOf(state)?.let { instanceYear(state, it) } ?: state.gameYear

    /** The beat on the table right now (today's holiday, not
     *  yet played this instance), or null. */
    fun activeBeat(state: GameState): HolidayBeat? {
        val b = state.holidayId?.let { beatFor(it) } ?: return null
        return if (isDone(state, b)) null else b
    }

    fun mask(state: GameState, beat: HolidayBeat): Int =
        state.life[maskKey(beat.holidayId, instanceYear(state))] ?: 0

    fun stepsDone(state: GameState, beat: HolidayBeat): Int =
        Integer.bitCount(mask(state, beat))

    fun isDone(state: GameState, beat: HolidayBeat): Boolean =
        (state.life[doneKey(beat.holidayId, instanceYear(state))] ?: 0) > 0

    /** Done in a SPECIFIC instance year — the episode wrap-up
     *  asks this after the calendar has already rolled on. */
    fun isDone(state: GameState, holidayId: String, year: Int): Boolean =
        (state.life[doneKey(holidayId, year)] ?: 0) > 0

    /** Instances of this holiday ever completed (one per year
     *  at most, by construction of the keys). */
    fun yearsDone(state: GameState, holidayId: String): Int =
        state.life[yearsKey(holidayId)] ?: 0

    /** Every beat ever completed, all holidays, all years. */
    fun doneTotal(state: GameState): Int = state.life["hbTotal"] ?: 0

    /** Distinct holidays whose beat has ever been played —
     *  the Full Calendar's measure (House derives the decor). */
    fun distinctDone(state: GameState): Int =
        HOLIDAYS.count { yearsDone(state, it.id) > 0 }

    // ---------------- eligibility & firing ----------------

    private fun playerLoc(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.location ?: ""

    private fun gatesPass(state: GameState, step: BeatStep): Boolean {
        if (step.onlyMd != null && state.todayKey.substring(4) != step.onlyMd)
            return false
        if (state.timeMin < step.minMin || state.timeMin >= step.maxMin)
            return false
        return true
    }

    /** May step [idx] of [beat] land right now? (Order and the
     *  last-step rule; the verb/place gates are the judges' own.) */
    private fun eligible(state: GameState, beat: HolidayBeat, idx: Int): Boolean {
        val m = mask(state, beat)
        if (m and (1 shl idx) != 0) return false
        if (beat.ordered) {
            val first = beat.steps.indices.firstOrNull { m and (1 shl it) == 0 }
            if (idx != first) return false
        } else if (beat.steps[idx].last) {
            // the finisher waits until it's the last step standing
            val others = beat.steps.indices.count {
                it != idx && m and (1 shl it) == 0
            }
            if (others > 0) return false
        }
        return gatesPass(state, beat.steps[idx])
    }

    private fun fill(template: String, state: GameState, beat: HolidayBeat): String =
        template
            .replace("{n}", stepsDone(state, beat).toString())
            .replace("{total}", beat.steps.size.toString())

    private fun lineFor(state: GameState, step: BeatStep): String {
        if (step.altLine != null && step.altArcId != null) {
            val arc = state.storyArcs[step.altArcId]
            if (arc != null && arc.done && arc.outcomeId.endsWith("_warm"))
                return step.altLine
        }
        return step.line
    }

    /** Land step [idx]: set its bit, tick the ticker with the
     *  short version, and when the mask fills, complete. */
    private fun fire(state: GameState, beat: HolidayBeat, idx: Int): BeatHit {
        val step = beat.steps[idx]
        val key = maskKey(beat.holidayId, instanceYear(state))
        state.life[key] = (state.life[key] ?: 0) or (1 shl idx)
        val text = fill(lineFor(state, step), state, beat)
        val full = stepsDone(state, beat) == beat.steps.size
        var completionText: String? = null
        if (full) {
            completionText = complete(state, beat)
        } else {
            state.events.add(fill(step.short, state, beat))
            while (state.events.size > 40) state.events.removeAt(0)
        }
        return BeatHit(beat, idx, text, full, completionText)
    }

    /** The shared completion: the permanent marks (capped keys,
     *  one per holiday per year), errand-scale rewards, the
     *  ticker and the town log. Returns the text rails show. */
    private fun complete(state: GameState, beat: HolidayBeat): String {
        val h = holidayOf(state)
        val name = h?.name ?: beat.holidayId
        state.life[doneKey(beat.holidayId, instanceYear(state))] = 1
        state.life[yearsKey(beat.holidayId)] = yearsDone(state, beat.holidayId) + 1
        state.life["hbTotal"] = doneTotal(state) + 1
        if (beat.rewardCoins > 0) Minigames.awardCoins(state, beat.rewardCoins)
        beat.rewardItem?.let { id ->
            state.inventory[id] = (state.inventory[id] ?: 0) + 1
        }
        val eng = Engine()
        for ((who, pts) in beat.rewardFriends) {
            if (who in state.friendship) eng.relAdjust(state, who, pts)
        }
        val bits = mutableListOf<String>()
        if (beat.rewardCoins > 0) bits += "+${beat.rewardCoins} 🪙"
        beat.rewardItem?.let { id ->
            bits += (itemById(id)?.let { "${it.icon} ${it.name}" } ?: id)
        }
        for ((who, pts) in beat.rewardFriends) bits += "+$pts 💛 $who"
        val rewardText = if (bits.isEmpty()) "" else " (${bits.joinToString(", ")})"
        state.events.add("🎉 $name — “${beat.title}” played to the end!$rewardText")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🎉 $name: ${beat.doneLine}")
        return "🎉 BEAT COMPLETE — “${beat.title}” is yours, and it's in the Journal " +
            "for good.$rewardText\n${beat.doneLine}"
    }

    // ---------------- the judges (one per verb) ----------------

    /** TALK/HOUSE query half: the step index a conversation
     *  with [npc] would land right now, or null. Pure. */
    fun talkStepFor(state: GameState, npc: String): Int? {
        val beat = activeBeat(state) ?: return null
        for (idx in beat.steps.indices) {
            val s = beat.steps[idx]
            val matches = when {
                s.verb == "TALK" && s.who == npc -> {
                    s.loc == null ||
                        state.agents[npc]?.location == s.loc
                }
                s.verb == "TALK" && s.who == null -> {
                    s.loc == null || playerLoc(state) == s.loc
                }
                s.verb == "HOUSE" && s.who == npc -> true
                else -> false
            }
            if (matches && eligible(state, beat, idx)) return idx
        }
        return null
    }

    /** TALK judged (the Talk gate calls this, like a delivery):
     *  also the HOUSE rail when the resident is caught out in
     *  costume — [BeatStep.talkLine] is that step's text then. */
    fun judgeTalk(state: GameState, npc: String): BeatHit? {
        val beat = activeBeat(state) ?: return null
        val idx = talkStepFor(state, npc) ?: return null
        val step = beat.steps[idx]
        val hit = fire(state, beat, idx)
        val talkText = step.talkLine
        return if (step.verb == "HOUSE" && talkText != null)
            hit.copy(text = fill(talkText, state, beat))
        else hit
    }

    /** LOOK judged: the text a careful Look at [locId] earns
     *  (a step line, a porch answer, a finisher), or null. */
    fun lookText(state: GameState, locId: String): String? {
        val beat = activeBeat(state) ?: return null
        for (idx in beat.steps.indices) {
            val s = beat.steps[idx]
            val matches = (s.verb == "LOOK" && s.loc == locId) ||
                (s.verb == "HOUSE" && s.houseLoc == locId)
            if (matches && eligible(state, beat, idx)) {
                val hit = fire(state, beat, idx)
                return if (hit.completedNow)
                    hit.text + "\n" + (hit.completionText ?: "")
                else hit.text
            }
        }
        return null
    }

    /** GIVE judged: a real hand-off through the gift rail —
     *  a beat's named item, or the good candy brought to a
     *  HOUSE stop's resident (their porch step, by generosity). */
    fun onGive(state: GameState, npc: String, itemId: String): BeatHit? {
        val beat = activeBeat(state) ?: return null
        for (idx in beat.steps.indices) {
            val s = beat.steps[idx]
            val matches = when {
                s.verb == "GIVE" && s.who == npc ->
                    itemId == s.item || itemId in s.altItems
                s.verb == "HOUSE" && s.who == npc -> itemId in CANDY_ITEMS
                else -> false
            }
            if (matches && eligible(state, beat, idx)) {
                val hit = fire(state, beat, idx)
                return if (s.verb == "HOUSE")
                    hit.copy(text = "You hand $npc the good candy — the real stuff. " +
                        "The route counts it: some houses you knock on, this one you " +
                        "provide for. " + fill(s.short, state, beat))
                else hit
            }
        }
        return null
    }

    /** PHOTO judged — called from inside Photos.applyPhoto,
     *  so a shutter click settles a beat's portrait step like
     *  it settles PHOTO errands. */
    fun onPhoto(state: GameState, rec: PhotoRecord): List<BeatHit> {
        val beat = activeBeat(state) ?: return emptyList()
        for (idx in beat.steps.indices) {
            val s = beat.steps[idx]
            if (s.verb == "PHOTO" && s.loc == rec.locId &&
                eligible(state, beat, idx)) {
                return listOf(fire(state, beat, idx))
            }
        }
        return emptyList()
    }

    /** CATCH judged — called from Minigames.applyCatch: any
     *  honest fish on the holiday itself is the picnic. */
    fun onCatch(state: GameState): BeatHit? {
        val beat = activeBeat(state) ?: return null
        for (idx in beat.steps.indices) {
            if (beat.steps[idx].verb == "CATCH" &&
                eligible(state, beat, idx)) {
                return fire(state, beat, idx)
            }
        }
        return null
    }

    // ---------------- flavor rails ----------------

    /** The morning-report note: fires ONLY on the window's
     *  first morning (startMd itself) and only while this
     *  instance's beat is unplayed — once per holiday, by
     *  construction, no nagging machinery needed. */
    fun morningNote(state: GameState): String? {
        val h = holidayOf(state) ?: return null
        if (state.todayKey.substring(4) != h.startMd) return null
        val beat = beatFor(h.id) ?: return null
        if (isDone(state, beat)) return null
        return "🎉 ${h.name} starts today — and it has a beat of its own: " +
            "“${beat.title}.” ${beat.intro}"
    }

    /** A beat reference in conversation during the window: a
     *  participant with a step still waiting says their piece;
     *  anyone else may drop a generic one. Once per person per
     *  day (participants) / once per day (generic), marked in
     *  `life` as the line is produced — the witness-line rule. */
    fun ambientLine(state: GameState, npc: String): Line? {
        val beat = activeBeat(state) ?: return null
        val specific = beat.ambient[npc]
        if (specific != null) {
            val pending = beat.steps.indices.any { idx ->
                beat.steps[idx].who == npc &&
                    mask(state, beat) and (1 shl idx) == 0
            }
            if (pending && state.life["hbAmb:$npc"] != state.absDay) {
                state.life["hbAmb:$npc"] = state.absDay
                return Line(npc, specific)
            }
            return null
        }
        if (beat.ambientGeneric.isNotEmpty() &&
            state.life["hbAmbDay"] != state.absDay) {
            state.life["hbAmbDay"] = state.absDay
            val pick = (state.absDay +
                (npc.hashCode() and 0x7fffffff)) % beat.ambientGeneric.size
            return Line(npc, beat.ambientGeneric[pick])
        }
        return null
    }

    // ---------------- the Journal page ----------------

    private val MONTH_SHORT = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    private fun mdLabel(md: String): String {
        val mo = md.substring(0, 2).toIntOrNull() ?: return md
        val dy = md.substring(2, 4).toIntOrNull() ?: return md
        return "${MONTH_SHORT[(mo - 1).coerceIn(0, 11)]} $dy"
    }

    private fun windowLabel(h: Holiday): String =
        if (h.startMd == h.endMd) mdLabel(h.startMd)
        else "${mdLabel(h.startMd)} – ${mdLabel(h.endMd)}"

    /** The 🎉 HOLIDAYS page: every holiday, its window, its
     *  beat — today's progress live, past completions as the
     *  permanent marks they are. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val total = doneTotal(state)
        out += "🎉 HOLIDAYS — " +
            if (total == 0) "every holiday in this town has a beat of its own, " +
                "waiting in its window"
            else "$total beat${if (total == 1) "" else "s"} played, " +
                "${distinctDone(state)} of ${HOLIDAYS.size} holidays marked"
        for (h in HOLIDAYS) {
            val beat = beatFor(h.id) ?: continue
            val years = yearsDone(state, h.id)
            val live = state.holidayId == h.id
            val doneNow = live && isDone(state, beat)
            when {
                doneNow ->
                    out += "🎉 ${h.name} (${windowLabel(h)}) — “${beat.title}”: " +
                        beat.doneLine
                live -> {
                    val n = stepsDone(state, beat)
                    out += "▶ ${h.name} (${windowLabel(h)}) — “${beat.title}”: " +
                        "$n of ${beat.steps.size} steps. ${beat.hint}"
                    if (years > 0)
                        out += "· also played in $years past year" +
                            if (years == 1) "." else "s."
                }
                years > 0 ->
                    out += "✓ ${h.name} (${windowLabel(h)}) — “${beat.title}” played in " +
                        "$years year${if (years == 1) "" else "s"}. It returns " +
                        "every ${mdLabel(h.startMd)}."
                else ->
                    out += "· ${h.name} (${windowLabel(h)}) — its beat: " +
                        "“${beat.title}.” ${beat.intro}"
            }
        }
        return out
    }
}

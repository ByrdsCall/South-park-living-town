package com.hunt3r.southparktown

// ============================================================
// Apprenticeships — v0.15.0h (Queue #15 item 5): town jobs
// grow a second tier.
//
// A job is a standing commitment; an APPRENTICESHIP is the
// tier above it. Work a job site steady — shifts completed
// at that one counter, counted off the jobs system's own
// clock-out (Jobs calls onShiftCompleted, the Capstones
// seam) — and the site's keeper, a NAMED cast member who
// belongs to the place, makes an offer in person: a chain
// of five stages spread over the better part of two weeks
// by day-gates that cannot be blitzed, routed across town
// on the Walk graph's own streets (the errand leg is a
// real walk, priced by real minutes), deepening a named
// SKILL the Journal shows, and ending in a completion
// mark the town remarks on — the mentor's standing line,
// the neighbors' gossip for three weeks, one TOWN NEWS
// line in the Gazette's own pages, and the master's rate
// (+1 🪙 a shift at the mastered site, paid through Jobs'
// own clock-out) forever after.
//
// Five programs, one per job site the town already runs:
//   THE GAZETTE ROUND   McDaniels at City Hall (paper)
//   THE LONG ROWS       Randy at Tegridy (farm)
//   THE TWEEK COUNTER   Tweek at Tweek Bros (shop pool)
//   THE JIMBO COUNTER   Jimbo at Jimbo's Guns (shop pool)
//   THE FREEMAN COUNTER Chef at Freeman's Tacos (shop)
// The shop pool shares ONE job but keeps THREE counters,
// and the keepers compare notes — so each counter counts
// its own shifts and signs its own card. Where a site had
// no named keeper in the town's data there would be no
// program; every site the jobs system runs has one.
//
// The discipline, in the house pattern: ONE apprenticeship
// at a time (a second offer waits; ending one early is a
// conversation with the mentor, costs nothing but the
// chain, and the count that earned the offer still
// stands). Declining costs nothing and the offer returns
// the next day. TalkGate keeps its eleven rails and never
// learns this module's name — the beats ride below the
// gate whole, in GameActivity's dialogue branch, the way
// the clubhouse's beats ride the crew-talk channel. ONE
// new agenda claim — the mentor's hours — seats after
// Jobs' in Engine's stack: while an offer stands or a
// stage waits on the mentor, the mentor keeps their
// counter 9-to-5, and every set piece seated below still
// wins them. NO phone trigger: the marks are public the
// way work is public — in person and in the paper — and
// the phone's closed sender set stays closed (MAX_TEXTS
// unmoved). The paper carries completions as TOWN NEWS
// section copy, never a headline tier (the ladder stands
// at ten, the festival precedent).
//
// State rides the existing `life` / `lifeText` maps —
// every read clamped against the program registry, no new
// save keys, no Prefs blob. Pure Kotlin, like its siblings.
// ============================================================

/** One judgment resolution: a verb the player can enact.
 *  [verb] is TALK (to [target], wherever they stand — the
 *  delivery discipline) or LOOK (at [target], a location
 *  id). [quality] is the master's read of the choice —
 *  2 the fact-finding way, 1 the decent way, 0 the hasty
 *  way — and it prices the stage's stipend, never its
 *  completion: every road through a judgment is honest,
 *  and the chain cannot be failed, only learned from. */
data class ApprJudgeRes(
    val verb: String,            // "TALK" | "LOOK"
    val target: String,          // npc name | location id
    val label: String,           // the objective's short name
    val quality: Int,            // 2 | 1 | 0
    val text: String,
    val friendTarget: String? = null
)

/** One apprenticeship program: a site, its job, its named
 *  mentor, its skill, and its five authored stages. The
 *  stage SKELETON is the same at every site (the first
 *  morning / the routed errand / the deep work / the
 *  judgment / the mark) — what differs is the work, and
 *  the mentor's voice saying it. */
data class ApprProgram(
    val site: String,            // location id
    val jobId: String,           // the Jobs id whose shifts count here
    val mentor: String,
    val chainTitle: String,
    val skill: String,
    val threshold: Int,          // completed shifts at this site
    val offerText: String,
    val acceptText: String,
    val declineText: String,
    val quitText: String,
    val stageTitles: List<String>,
    val s0Text: String,          // stage 0: TALK mentor at site
    val recipient: String,       // stage 1's delivery target
    val pickupText: String,      // stage 1a: TALK mentor at site
    val deliverText: String,     // stage 1b: TALK recipient, anywhere
    val s2Text: String,          // stage 2: LOOK at site
    val judgePoseText: String,   // stage 3a: TALK mentor at site
    val judgeResolutions: List<ApprJudgeRes>,
    val markText: String,        // stage 4: TALK mentor at site
    val masterLine: String,      // the mentor's standing line, after
    val claimActivity: String,   // the agenda claim's activity string
    val paperText: String        // the TOWN NEWS line, on completion
)

/** A step of the apprenticeship a Talk would take — what
 *  GameActivity's dialogue branch hands its dialogs.
 *  [kind]: OFFER | STAGE | STATUS from the query;
 *  ACCEPT | DECLINE | STAGE | QUIT from the judge. */
data class ApprHit(
    val prog: ApprProgram,
    val kind: String,
    val text: String = "",
    val title: String = ""
)

object Apprenticeships {

    // ---------------- the dials (ledger-named) ----------------

    /** Day-gates between stages: stage k opens GAPS[k]
     *  days after stage k-1 landed (stage 0 on the accept
     *  day itself). The chain cannot be blitzed: its floor
     *  is MIN_SPAN days, the better part of two weeks. */
    val GAPS: List<Int> = listOf(0, 2, 3, 4, 7)
    const val MIN_SPAN: Int = 16        // GAPS.sum()

    /** Stage stipends, paid by the mentor's hand on the
     *  day (Minigames.awardCoins, like every honest coin):
     *  the base by stage, and the judgment stage adds its
     *  resolution's quality (0–2) on top of its base. A
     *  whole chain pays 15–17 🪙 — proven in-suite against
     *  what the site's ordinary work pays over the same
     *  span (see the economy block of the tuning ledger). */
    val STIPENDS: List<Int> = listOf(2, 3, 3, 2, 5)

    /** The master's rate: +1 🪙 on every shift clocked out
     *  at a mastered site, paid inside Jobs' own pay line
     *  (Jobs asks masterBonus at the clock-out). */
    const val MASTER_BONUS: Int = 1

    /** The mentor's hours: while an offer stands or a
     *  stage waits on them, the mentor keeps their site
     *  through the working day — the one agenda claim. */
    const val CLAIM_FROM: Int = 9 * 60
    const val CLAIM_TO: Int = 17 * 60

    /** How long the town talks about a fresh mark: three
     *  weeks of remarked-upon, then it settles into the
     *  furniture of the place (the framed card stays). */
    const val REMARK_DAYS: Int = 21

    const val STAGE_COUNT: Int = 5

    /** Skill rungs by stages landed — the Journal's words
     *  for what your hands know so far. */
    val SKILL_RUNGS: List<String> = listOf(
        "Unstarted", "Green hand", "Willing hand",
        "Steady hand", "Journeyman", "Master")

    // ---------------- the registry ----------------

    val PROGRAMS: List<ApprProgram> = listOf(
        ApprProgram(
            site = "CityHall", jobId = "paper", mentor = "McDaniels",
            chainTitle = "The Gazette Round", skill = "Route Sense",
            threshold = 6,
            offerText = "McDaniels looks up from the headlines and " +
                "studies you the way she studies turnout figures. " +
                "“Six routes you've walked for this office, and the " +
                "card comes back clean every time. The Gazette has " +
                "a second tier, new kid — an apprenticeship. My " +
                "round, my standards, my signature at the end of " +
                "it. It isn't a week of work; it's most of a month " +
                "of mornings, in stages, some of it clear across " +
                "town. The route teaches what the route teaches. " +
                "Say the word and the card is yours.”",
            acceptText = "“Then it's on the record.” She writes " +
                "your name on a fresh route card in the hand she " +
                "saves for ordinances. “The first stage is the " +
                "first morning: come back to this counter and I'll " +
                "show you how a master walks the round — bundle " +
                "high, boxes in order, no doorstep left guessing.”",
            declineText = "“The offer keeps,” McDaniels says, " +
                "already back to the headlines. “Cards don't expire " +
                "in this office. They wait.”",
            quitText = "McDaniels takes the route card back and " +
                "files it under A, for Apprentice, Attempted, " +
                "Accepted-as-final. “The stages stop here, then. " +
                "Work you did stays done — the town keeps what it " +
                "was given. If the round calls you again, the count " +
                "still stands. It usually calls.”",
            stageTitles = listOf("The First Bundle", "The Archive Run",
                "The Whole Route, From Memory", "The Missing Tuesday",
                "The Signed Route Card"),
            s0Text = "McDaniels walks the round with you herself, " +
                "once, at a pace that suggests the town is being " +
                "timed. “Bundle high on the shoulder — the boxes " +
                "in the card's order, not the street's. The " +
                "Broflovski box sticks; you know it sticks; a " +
                "master lifts before she pulls. Read the card back " +
                "to me at the end — a route you can't recite is a " +
                "route you're borrowing.” At City Hall she takes " +
                "the empty strap and signs the card's first line. " +
                "“That's the round. The rest of the stages are " +
                "yours to walk into.”",
            recipient = "Victoria",
            pickupText = "McDaniels produces a bound volume from " +
                "under the counter — the Gazette's archive year, " +
                "sealed with the office's tape. “The Elementary's " +
                "library takes the archive for the history shelf. " +
                "Principal Victoria signs for it — nobody else, " +
                "and the seal arrives intact or the trip didn't " +
                "happen. Walk it over like it's made of headlines.”",
            deliverText = "Victoria receives the bound archive " +
                "with both hands, checks the seal twice, and signs " +
                "the slip in a hand that has ended arguments. “The " +
                "school keeps its records, new kid. All of them.” " +
                "She shelves it at eye height. “Tell the Mayor her " +
                "archive has a home with a lock.”",
            s2Text = "You work the Gazette desk the deep way: the " +
                "route cards filed by week, the complaint ledger " +
                "read cover to cover (two names recur; both boxes " +
                "stick; both are noted, fairly), the bundle twine " +
                "knotted the office way — doubled, so a strap never " +
                "eats a shoulder by Friday. When you finish, the " +
                "desk looks like McDaniels' standards and your " +
                "hands know why.",
            judgePoseText = "McDaniels sets the route card between " +
                "you like evidence. “The Marsh house says Tuesday's " +
                "Gazette never arrived. The card — your card, " +
                "walked by my apprentice — says it did. This is " +
                "the part of the work that isn't walking, new kid. " +
                "Settle it: check the box yourself, ask the house, " +
                "or come back to me and call it lost. Choose like " +
                "a master, not like a headline.”",
            judgeResolutions = listOf(
                ApprJudgeRes("LOOK", "StanHouse",
                    "Check the Marsh box yourself", 2,
                    "The Marsh box sticks, exactly as advertised. " +
                        "You lift before you pull — and there it " +
                        "is: Tuesday's Gazette, slid under a " +
                        "grocery flyer the size of a small door. " +
                        "The house never missed the paper; the " +
                        "paper missed being seen. You leave a note " +
                        "in the box, weighted with a stone, in " +
                        "your best route hand: CHECK UNDER THE " +
                        "FLYER. Case closed by the box itself."),
                ApprJudgeRes("TALK", "Stan",
                    "Ask the house (Stan)", 1,
                    "Stan answers the door already apologizing to " +
                        "the wrong person. “Tuesday? I brought it " +
                        "in Tuesday. It's — oh. It's under the " +
                        "pizza coupons.” He holds it up like a " +
                        "flag of surrender. “Tell City Hall the " +
                        "Marsh house withdraws its complaint and " +
                        "its dignity.” The paper was home all " +
                        "along; the house just files its reading " +
                        "the way it files everything: later.",
                    friendTarget = "Stan"),
                ApprJudgeRes("TALK", "McDaniels",
                    "Report it lost and re-deliver", 0,
                    "You report it lost and re-walk the leg with " +
                        "a fresh copy before noon. McDaniels " +
                        "accepts the replacement without looking " +
                        "up. “A master checks the box before she " +
                        "spends the town's paper, new kid. The leg " +
                        "you walked twice was the tuition.” The " +
                        "complaint closes. The lesson stays open.")),
            markText = "McDaniels calls you to the desk and " +
                "produces the route card — your card, its five " +
                "lines full. She signs the last one with the " +
                "ordinance hand and turns the card to face you. " +
                "“Route Sense. The town reads on time because " +
                "somebody's apprentice learned the round whole. " +
                "The card hangs by the desk where the carriers " +
                "can see it — and the master's rate is yours on " +
                "every route you walk for this office from now " +
                "on. Don't make a speech. The Gazette doesn't " +
                "print those.”",
            masterLine = "McDaniels nods at the framed card by " +
                "the desk. “Route master. The town reads on " +
                "time.” It's the whole speech; from her, it's a " +
                "parade.",
            claimActivity = "At the Gazette desk — the " +
                "apprenticeship's hours are being kept.",
            paperText = "🎓 TOWN NEWS — the Gazette's own desk " +
                "reports a graduation: the new kid completed the " +
                "paper-route apprenticeship under Mayor McDaniels " +
                "and holds the Route Sense card at City Hall. The " +
                "route, the town is assured, has never been walked " +
                "better."),
        ApprProgram(
            site = "TegridyFarms", jobId = "farm", mentor = "Randy",
            chainTitle = "The Long Rows", skill = "Tegridy Hand",
            threshold = 6,
            offerText = "Randy leans on the fence and counts your " +
                "shifts off on his fingers — twice, because the " +
                "first count had a thumb in it. “Six mornings " +
                "you've worked these rows and the fence still " +
                "stands where you left it. Farm's got a second " +
                "tier, kid: an apprenticeship. My rows, my fence, " +
                "my inspection — and Sharon's book at the end of " +
                "it, which is the part that matters. It's stages, " +
                "spread over weeks, some of it running clear " +
                "across town. Tegridy isn't a job. It's a calling " +
                "with gloves. You in?”",
            acceptText = "Randy spits, misses the bucket, and " +
                "shakes your hand like a fence post being seated. " +
                "“Done. First stage is the first morning done " +
                "right — come find me at the farm and I'll show " +
                "you how a hand the rows remember works a row. " +
                "Sharon's already written your name in the book. " +
                "In pencil. Impress the book.”",
            declineText = "“Offer keeps,” Randy says, turning back " +
                "to the rows. “Farm's patient. It's everything " +
                "else about me that's not.”",
            quitText = "Randy takes the gloves back and hangs " +
                "them on their nail. “Chain ends here, then. Work " +
                "you did stays in the book — Sharon doesn't " +
                "unwrite. If the rows call you back, the count " +
                "stands. They call everybody back eventually. " +
                "It's the nitrogen.”",
            stageTitles = listOf("Rows Before Breakfast",
                "The Preserves Run", "The Fence Line, Whole",
                "What Came Through the Rows", "The Signed Book"),
            s0Text = "Randy works one row beside you, narrating " +
                "like a nature documentary he is also starring " +
                "in. “Water deep, not wide. Weed at the root, not " +
                "the rumor of the root. And you talk to 'em — not " +
                "because it helps the crop; because it helps the " +
                "farmer, and the farmer's the crop I'm growing " +
                "this season.” At the row's end he inspects your " +
                "work with the clipboard he never writes on. " +
                "“Rows remember that. So does the book.”",
            recipient = "Chef",
            pickupText = "Randy loads your arms with a crate of " +
                "Sharon's preserves, jarred and labeled in her " +
                "hand. “School kitchen ordered these a month ago. " +
                "Chef's written me three letters — three — in " +
                "handwriting that gets bigger every time. Take " +
                "'em to Chef. And if he 'samples' the crate light " +
                "on the way in, you saw nothing.”",
            deliverText = "Chef takes the crate and counts the " +
                "jars twice, holding one to the light like a " +
                "jewel. “The farm provides, children. THE FARM " +
                "PROVIDES.” He signs Randy's slip with a flourish " +
                "that gets gravy on it. “Tell Marsh his farm " +
                "feeds this school better than the district does. " +
                "And tell him I said it quietly, so it costs him.”",
            s2Text = "You walk the whole fence line the deep way " +
                "— every post tested with a shoulder, every rail " +
                "read like a sentence. Two posts get re-seated, " +
                "one rail gets replaced before it asks to be, and " +
                "the line runs true from the road to the back " +
                "rows. It's the work nobody photographs. It's " +
                "also the work the whole farm stands on.",
            judgePoseText = "Randy walks you to the north rows, " +
                "where the crop lies flat in a track too straight " +
                "for weather. “Something came through here last " +
                "night. Sharon says wind. The fence says a fence " +
                "doesn't bend like that for wind. Find out what " +
                "walked my rows, kid — check the tracks yourself, " +
                "ask the kid whose shortcut this is, or come tell " +
                "me it was the wind and watch me believe you. " +
                "Master's choice.”",
            judgeResolutions = listOf(
                ApprJudgeRes("LOOK", "TegridyFarms",
                    "Read the tracks by the fence", 2,
                    "You read the ground the way Sharon reads a " +
                        "ledger: boot prints, kid-sized, sure-" +
                        "footed, following the fence gap where " +
                        "the rail sits low — the pond path, worn " +
                        "smooth as a rumor. Not weather. Not " +
                        "malice. A shortcut with a schedule. You " +
                        "re-seat the low rail before you report a " +
                        "word of it, because the finding is only " +
                        "half the job."),
                ApprJudgeRes("TALK", "Butters",
                    "Ask Butters about his shortcut", 1,
                    "Butters goes pale in the way that is also a " +
                        "confession. “I — I cut through the Marsh " +
                        "rows to the pond, on account of the long " +
                        "way has that dog that barks in sentences. " +
                        "I didn't trample on purpose, Mr. Randy's " +
                        "apprentice, sir. The rows were just... " +
                        "under the shortcut.” He offers to " +
                        "apologize to the rows personally, and " +
                        "means it, and somehow that helps.",
                    friendTarget = "Butters"),
                ApprJudgeRes("TALK", "Randy",
                    "Tell Randy it was the wind", 0,
                    "“Wind,” you say. Randy looks at the flat " +
                        "rows. Randy looks at the fence. Randy " +
                        "looks at you for a long time. “Wind " +
                        "doesn't wear boots, kid. Wind doesn't " +
                        "cut for the pond.” He re-seats the low " +
                        "rail himself, slow. “A hand that won't " +
                        "say what it saw is just a glove. Remember " +
                        "that one.”")),
            markText = "Sharon does the honors, because the book " +
                "is the farm's real diploma. She turns its pages " +
                "back past your six shifts, past the stages, and " +
                "writes the last line in ink. “Tegridy Hand,” she " +
                "says. “The rows stand straighter where you've " +
                "worked, and now it's in the book, which is the " +
                "part that's true forever.” Randy nods like a " +
                "general of agriculture. “Master's rate on my " +
                "shifts from here on, kid. Tegridy work. Signed.”",
            masterLine = "Randy tips his head at the rows. " +
                "“Tegridy Hand. Book says so.” Sharon, passing " +
                "with the ledger, adds: “The book is never wrong.”",
            claimActivity = "By the fence line — the " +
                "apprenticeship's hours are being kept.",
            paperText = "🎓 TOWN NEWS — Tegridy Farms reports a " +
                "graduation: the new kid completed the farmhand " +
                "apprenticeship under Randy Marsh, and Sharon's " +
                "book now carries a Tegridy Hand in ink. The " +
                "rows, witnesses confirm, stand straighter for it."),
        ApprProgram(
            site = "TweekBros", jobId = "shop", mentor = "Tweek",
            chainTitle = "The Tweek Counter", skill = "Steady Hands",
            threshold = 4,
            offerText = "Tweek counts your counter shifts on a " +
                "napkin, twice, in two different colors. “Four " +
                "shifts. FOUR. And the shelves are still standing " +
                "and the till balanced EVERY time and — the " +
                "keepers compare notes, it's the POOL, and your " +
                "notes are — they're GOOD notes. There's a second " +
                "tier. An apprenticeship. My counter, my " +
                "standards, my signature — stages, over weeks, " +
                "some of it running to the other shops. It's a " +
                "lot of pressure. It's GOOD pressure. Probably. " +
                "GAAAH. I mean — think about it. Then say yes.”",
            acceptText = "“OKAY. Okay.” Tweek writes your name on " +
                "the rota sheet in the good pen, then writes it " +
                "again smaller underneath, for luck. “First " +
                "stage: a first shift at my counter, done the " +
                "master's way. Come talk to me here and I'll show " +
                "you the whole ballet — sacks, shelves, till, " +
                "regulars. Nobody's ever written it down. It's " +
                "all in the VIBRATING.”",
            declineText = "“OKAY. Okay okay okay. The offer's on " +
                "the rota — it WAITS. Offers are patient. I'M the " +
                "one who's — just, it waits.”",
            quitText = "Tweek erases your name from the rota sheet " +
                "— in pencil, shop law — and then, after a " +
                "moment, writes it back in the corner, smaller. " +
                "“In case. The stages stop, the work you did " +
                "COUNTS, it's ON THE NAPKIN. If you come back the " +
                "offer's STILL here. It's always here. EVERYTHING " +
                "is always here. That's the problem with this shop.”",
            stageTitles = listOf("The First Calm Counter",
                "The Bean Trade", "The Shelves, By Fear Level",
                "The Short Till", "The Signed Rota Card"),
            s0Text = "Tweek teaches the shift as choreography: " +
                "sacks up from the back in fear-level order (the " +
                "fear levels are real; the chart is in his head; " +
                "the chart is enormous), shelves squared " +
                "alphabetically-then-by-dread, till counted twice " +
                "— once for the money, once for the gnomes. Your " +
                "hands only shake a little by the till. “SEE?” he " +
                "says, delighted, shaking plenty. “STEADY. That's " +
                "the whole craft. The rest is coffee.”",
            recipient = "Jimbo",
            pickupText = "Tweek presses a sealed sample pouch and " +
                "a much-folded note into your hands like state " +
                "secrets. “Jimbo. JIMBO'S. It's a trade — beans " +
                "for — it doesn't matter WHAT, it's been on my " +
                "counter a WEEK and every customer can smell the " +
                "procrastination. Take it to Jimbo at his shop. " +
                "GO. Gently. But GO.”",
            deliverText = "Jimbo unfolds the note, sniffs the " +
                "pouch, and nods once, slow. “Trade's on. Tell " +
                "Tweek his beans pass inspection, and tell him " +
                "the floorboards at his place vibrate in a key I " +
                "almost recognize.” He files the note under T, " +
                "for Trade, Tolerated, Trembling.",
            s2Text = "You stock the deep way: every sack rotated " +
                "(old forward, new back), the fear-level section " +
                "re-sorted by this week's fears, the menu board's " +
                "chalk gone over so the specials read at ten " +
                "paces. Regulars drift in and nod at the shelves " +
                "like the shelves did something. The shelves did. " +
                "You did.",
            judgePoseText = "Tweek stares into the till like it " +
                "owes him an apology. “The count's short. TWO " +
                "COINS. It was right at open and now it's — either " +
                "I miscounted, or the regular did, or the GNOMES " +
                "have a union now. You're my apprentice, you " +
                "SETTLE it: recount the till yourself, ask Craig " +
                "— he paid last — or tell me to confess and cover " +
                "it. Choose FAST but choose TRUE.”",
            judgeResolutions = listOf(
                ApprJudgeRes("LOOK", "TweekBros",
                    "Recount the till yourself", 2,
                    "You count the till the way you were taught: " +
                        "once for the money, once for the gnomes. " +
                        "The money's all there. Two coins sit " +
                        "wedged under the drawer lip, exactly " +
                        "where a slammed drawer sends them — the " +
                        "till was never short; the count was. You " +
                        "fish them out and lay them on the " +
                        "counter in a row. Tweek stares. “THE " +
                        "DRAWER. It was the DRAWER. I'm going to " +
                        "write that down. I'm going to write " +
                        "EVERYTHING down.”"),
                ApprJudgeRes("TALK", "Craig",
                    "Ask Craig, the last regular", 1,
                    "Craig considers the question with the " +
                        "seriousness of a man countersigning " +
                        "history. “Paid exact. Counted twice. I " +
                        "have the receipt.” He produces a napkin " +
                        "with his order, the total, and the " +
                        "change, annotated. “So the till's wrong, " +
                        "or Tweek is. Statistically it's Tweek.” " +
                        "It is the most useful testimony ever " +
                        "given in this shop, delivered without " +
                        "inflection."),
                ApprJudgeRes("TALK", "Tweek",
                    "Tell Tweek to confess and cover it", 0,
                    "“Confess and cover it,” you say, and Tweek " +
                        "very nearly does — apron off, speech " +
                        "prepared, coins from his own pocket — " +
                        "before the drawer's lip gives up its two " +
                        "captives at close. He looks at the " +
                        "coins. He looks at you. “YOU TOLD ME TO " +
                        "CONFESS.” The till balances. The lesson " +
                        "is entered in the napkin ledger under P, " +
                        "for Premature.")),
            markText = "Tweek signs the rota card on the third " +
                "try — the first two signatures shook off the " +
                "line. “STEADY HANDS,” he says, loud, to the whole " +
                "shop, to no one, to the gnomes. “That's a MASTER. " +
                "My apprentice. The card goes on the wall by the " +
                "register and the master's rate is yours at my " +
                "counter from now on — the pool honors it, the " +
                "keepers COMPARED NOTES.” He drinks a whole coffee " +
                "in celebration and only regrets it at the usual " +
                "speed.",
            masterLine = "Tweek points at the card by the " +
                "register with a hand that only shakes a normal " +
                "amount. “Steady Hands. MY apprentice. The till " +
                "has never recovered.”",
            claimActivity = "Behind the counter — the " +
                "apprenticeship's hours are being kept.",
            paperText = "🎓 TOWN NEWS — Tweek Bros. Coffeehouse " +
                "announces a graduation, in the good pen: the new " +
                "kid completed the counter apprenticeship under " +
                "Tweek Tweek and holds the Steady Hands card. The " +
                "till, the paper confirms, balanced throughout."),
        ApprProgram(
            site = "JimbosGuns", jobId = "shop", mentor = "Jimbo",
            chainTitle = "The Jimbo Counter", skill = "Counter Craft",
            threshold = 4,
            offerText = "Jimbo counts your shifts at his counter " +
                "with one eye closed, which is either marksmanship " +
                "or approval. “Four shifts. Shelves squared, " +
                "receipts spiked, not one label out of place. " +
                "There's a second tier for hands like that: an " +
                "apprenticeship. My shop, my standards, my mark " +
                "at the end. Stages, spread over weeks — some of " +
                "it walking clear across town on shop business. " +
                "The craft is the counter, kid. The counter is " +
                "the craft. Interested?”",
            acceptText = "“Then you're on the board.” Jimbo " +
                "chalks your name on the shift board in block " +
                "letters. “First stage: a first shift done the " +
                "craftsman's way. Come talk to me here and I'll " +
                "walk you through the whole counter — boxes, " +
                "labels, glass eye and all. Bring your eyes. " +
                "Both of 'em. Open.”",
            declineText = "“Offer stands,” Jimbo says, turning " +
                "back to the shelves. “Counters are patient. It's " +
                "the customers that aren't.”",
            quitText = "Jimbo wipes your name off the shift board " +
                "with his thumb, then chalks it back at the " +
                "board's edge, smaller. “Board keeps the " +
                "history, kid. Stages stop here; the work you " +
                "did stays done. Door's open if the craft calls " +
                "again. It calls.”",
            stageTitles = listOf("Squared Boxes", "The Knife Run",
                "Labels Out, Whole Shelf", "The Swapped Labels",
                "The Signed Sweep Card"),
            s0Text = "Jimbo teaches the counter as terrain: boxes " +
                "squared to the shelf's edge, labels out like " +
                "faces, the good camo where the light hits it, " +
                "the glass eye over the till watching what hands " +
                "forget. “A customer reads a shelf like a track, " +
                "kid. Your job is the shelf says one thing, true, " +
                "in order.” At the end he inspects your work with " +
                "one eye closed. “Craft,” he pronounces, which " +
                "from Jimbo is a paragraph.",
            recipient = "Chef",
            pickupText = "Jimbo hands you a wrapped parcel, " +
                "labeled in his block hand: FOR THE FREEMAN'S " +
                "KITCHEN. “Chef ordered the good knife set. " +
                "Wrapped, labeled, and none of the town's " +
                "business. Walk it to Chef at Freeman's. A " +
                "delivery's like a shelf, kid — it should say one " +
                "thing: arrived.”",
            deliverText = "Chef unwraps one knife and holds it " +
                "to the light the way jewelers and head chefs " +
                "do, which is the same way. “A blade with " +
                "manners, children. A blade that introduces " +
                "itself.” He tests it on a tomato that never " +
                "stood a chance. “Tell Jimbo the kitchen " +
                "approves. Tell him the tomato sends its " +
                "regards. Its LAST regards.”",
            s2Text = "You audit the whole shop the deep way: " +
                "every shelf re-squared, every label read twice " +
                "(out loud, under your breath, the way Jimbo " +
                "does when he thinks the shop is empty), the " +
                "receipts spiked in order back to the month's " +
                "first morning. Halfway through, the shop starts " +
                "to feel less like a store and more like a " +
                "sentence you're learning to write.",
            judgePoseText = "Jimbo lowers his voice, which in " +
                "this shop counts as a town crier. “Two boxes on " +
                "the middle shelf traded labels sometime this " +
                "week. A customer nearly walked out with the " +
                "wrong size of everything. We sort it quiet, " +
                "kid — no paperwork, no fuss, no announcement. " +
                "Check the shelf yourself, ask Ned — he noticed " +
                "it first — or bring it to me and we'll make it " +
                "official. Master's choice.”",
            judgeResolutions = listOf(
                ApprJudgeRes("LOOK", "JimbosGuns",
                    "Check every label on the shelf", 2,
                    "You check every label on the middle shelf " +
                        "against its box, then every label in the " +
                        "shop for good measure. Exactly two are " +
                        "swapped — the pair Ned spotted, neighbors " +
                        "since the month turned. Both come right " +
                        "in your hands, quiet as the job wanted. " +
                        "The shelf reads one thing again: true, " +
                        "in order."),
                ApprJudgeRes("TALK", "Ned",
                    "Ask Ned, who noticed first", 1,
                    "Ned's voice box does the pointing. “THOSE. " +
                        "TWO.” A pause, while the box cools. " +
                        "“LABELS. WRONG.” It's the whole testimony " +
                        "and it is, on inspection, entirely " +
                        "correct. Jimbo nods like a range officer " +
                        "confirming a hit. “Ned's eye for a wrong " +
                        "label is why he's a regular and a warning " +
                        "system in one.”"),
                ApprJudgeRes("TALK", "Jimbo",
                    "Make it official with Jimbo", 0,
                    "You bring it to Jimbo straight — and Jimbo, " +
                        "being Jimbo, announces it to the whole " +
                        "counter at parade volume so it gets " +
                        "fixed properly. The labels come right by " +
                        "supper. The quiet doesn't survive the " +
                        "hour. “You chose official, kid,” he says, " +
                        "not unkindly. “Official works. Official " +
                        "is just loud.”")),
            markText = "Jimbo takes down the sweep card and signs " +
                "it under the glass eye's supervision. “Counter " +
                "Craft,” he says. “Boxes squared, labels true, a " +
                "delivery that arrived, and a quiet job done " +
                "quiet — mostly.” The card goes in the frame by " +
                "the door where the regulars can read it. " +
                "“Master's rate at my counter from here on. The " +
                "craft is the counter, and the counter is yours.”",
            masterLine = "Jimbo nods at the framed card by the " +
                "door. “Counter Craft. Kid earned that " +
                "signature.” One eye closes. Approval, probably.",
            claimActivity = "Behind the counter — the " +
                "apprenticeship's hours are being kept.",
            paperText = "🎓 TOWN NEWS — Jimbo's Guns records a " +
                "graduation: the new kid completed the counter " +
                "apprenticeship under Jimbo Kerns and holds the " +
                "Counter Craft card. Shelves were squared for " +
                "the occasion. Labels, all of them, faced out."),
        ApprProgram(
            site = "FreemansTacos", jobId = "shop", mentor = "Chef",
            chainTitle = "The Freeman Counter",
            skill = "Kitchen Rhythm",
            threshold = 4,
            offerText = "Chef counts your shifts at his counter " +
                "by smell, which he insists is a filing system. " +
                "“Four shifts at my line, children, and the trays " +
                "went out in rhythm every time. There's a second " +
                "tier for hands that keep time: an " +
                "apprenticeship. My kitchen, my standards, my " +
                "ticket at the end. Stages, over weeks — one of " +
                "them runs clear across town, because a kitchen " +
                "that can't feed another kitchen is just a room " +
                "with opinions. You feel the rhythm, apprentice? " +
                "Then say so.”",
            acceptText = "“THEN WE COOK.” Chef writes your name " +
                "on the ticket rail in grease pencil, in the " +
                "spot where the important tickets go. “First " +
                "stage: first service, done the master's way. " +
                "Talk to me at my counter and I'll call the whole " +
                "service for you once, the way the line is " +
                "supposed to hear it. A kitchen is a song, " +
                "children. You're about to learn the words.”",
            declineText = "“The offer keeps its place on the " +
                "rail,” Chef says, turning back to the line. " +
                "“Good tickets wait. It's the cooks who can't.”",
            quitText = "Chef takes your ticket off the rail and " +
                "folds it into the recipe box, where the almosts " +
                "live. “The song stops here, then. The verses " +
                "you sang stay sung — a kitchen doesn't unserve " +
                "a plate. If the rhythm calls you back, the " +
                "count still stands, children. It always stands.”",
            stageTitles = listOf("First Service",
                "The Restorative Plate", "Trays, Timed to the Rush",
                "The Off Tray", "The Signed Ticket"),
            s0Text = "Chef calls your first service himself, and " +
                "the line answers like a song it already knows: " +
                "shells towered with respect, trays filled in " +
                "the order the rush will ask for them, the pass " +
                "wiped between tickets to a shine that reflects " +
                "them back. “With respect, with rhythm, with a " +
                "little song you are not allowed to repeat " +
                "outside this kitchen,” Chef says, over the " +
                "steam. “That is the whole craft, children. The " +
                "rest is timing.”",
            recipient = "Tweek",
            pickupText = "Chef slides a covered dish and a " +
                "recipe card across the pass with unusual " +
                "gentleness. “For Tweek, children. Three hours of " +
                "sleep and a counter full of pressure — the man " +
                "is a drum with no skin. This is the Freeman's " +
                "restorative plate, and the card that explains " +
                "it, so his own keepers can make it when I'm not " +
                "watching. Tweek Bros. Gently. A kindness " +
                "carried carelessly arrives as a spill.”",
            deliverText = "Tweek stares at the covered dish like " +
                "it might be a rumor with a lid. “Chef SENT this? " +
                "For ME? — don't watch me eat it. Don't — " +
                "GAAAH.” He reads the card twice. Whatever is " +
                "under the lid, the second bite is slower than " +
                "the first, and by the fourth his hands have " +
                "almost stopped. “Tell him... tell him the trade " +
                "route goes both ways.”",
            s2Text = "You run the line the deep way: trays " +
                "filled in the order the rush will ask for them, " +
                "shells towered with respect and rhythm, the " +
                "pass wiped to a shine that reflects the tickets " +
                "back at themselves. Chef calls the orders; the " +
                "line answers like it rehearsed. Somewhere in " +
                "the middle of it you realize your hands are " +
                "keeping time without asking you first.",
            judgePoseText = "Chef goes quiet, which in this " +
                "kitchen is a fire alarm. “One filling tray " +
                "tastes like yesterday, children, and the lunch " +
                "rush does not forgive. I will not serve a " +
                "tragedy in a shell. Find the tray: work the " +
                "line yourself, ask Butters — he ate here " +
                "yesterday, and a regular's tongue keeps " +
                "records — or come to me and we'll dump them all " +
                "and start the morning over. Choose like a cook.”",
            judgeResolutions = listOf(
                ApprJudgeRes("LOOK", "FreemansTacos",
                    "Work the trays yourself", 2,
                    "You work the trays the way the line taught " +
                        "you: color, steam, smell, order. The " +
                        "corner tray is the one — filled at " +
                        "yesterday's close, mislabeled in a tired " +
                        "hand, hiding behind the fresh ones like " +
                        "a verse in the wrong song. The rest are " +
                        "this morning, clean and true. One tray " +
                        "out; the rush never knows how close it " +
                        "came."),
                ApprJudgeRes("TALK", "Butters",
                    "Ask Butters, yesterday's regular", 1,
                    "Butters considers yesterday's taco with " +
                        "enormous care. “It was a fine taco, " +
                        "mostly. It just seemed like it was " +
                        "havin' a day, is all. Like it'd been " +
                        "waitin' a while to be a taco.” He says " +
                        "it kindly, the way he'd want his own " +
                        "off day reported. Chef writes CORNER " +
                        "TRAY on the board in letters that could " +
                        "testify.",
                    friendTarget = "Butters"),
                ApprJudgeRes("TALK", "Chef",
                    "Dump them all and start over", 0,
                    "Dump them all, you say, and Chef does — " +
                        "every tray, the fresh with the stale, " +
                        "mourning each good one by name as it " +
                        "goes. The rush is late and the line is " +
                        "lean, but nothing questionable reaches a " +
                        "shell. “Safe,” Chef says at last, “is a " +
                        "flavor too. It is just the most expensive " +
                        "one.”")),
            markText = "Chef takes the last ticket off the rail " +
                "— your ticket, the long one, every stage " +
                "punched — and signs it with the grease pencil " +
                "in a hand like a headline. “Kitchen Rhythm,” he " +
                "announces to the line, the counter, the dining " +
                "room, and the concept of lunch. “The town's " +
                "got a master of the line, children, and it's MY " +
                "apprentice. The ticket hangs over the pass where " +
                "the rush can see it. Master's rate at my " +
                "counter from today — you've earned the rhythm.”",
            masterLine = "Chef points the tongs at the ticket " +
                "over the pass. “Kitchen Rhythm. My apprentice. " +
                "The line has never kept better time.”",
            claimActivity = "Behind the counter — the " +
                "apprenticeship's hours are being kept.",
            paperText = "🎓 TOWN NEWS — Freeman's Tacos announces " +
                "a graduation over the pass: the new kid " +
                "completed the kitchen apprenticeship under Chef " +
                "and holds the Kitchen Rhythm ticket. The lunch " +
                "rush, witnesses say, applauded in rhythm."))

    fun programAt(site: String?): ApprProgram? =
        PROGRAMS.firstOrNull { it.site == site }

    fun programByMentor(name: String?): ApprProgram? =
        PROGRAMS.firstOrNull { it.mentor == name }

    // ---------------- state keys (life / lifeText) ----------------
    // The active chain's site, and per-site records: stages
    // landed, the day the current stage's clock started,
    // the errand/judgment sub-step, the completion day, the
    // shifts the site's counter has counted, the last
    // decline, and who has already remarked on a mark.
    // Every read clamps against the registry — a hostile
    // or stale save reads as "no chain, no marks", never
    // as a phantom apprenticeship.

    private const val K_ACTIVE = "apprActive"        // lifeText: site id ("" = none)
    private fun kStage(site: String) = "apprStage:$site"
    private fun kStageDay(site: String) = "apprStageDay:$site"
    private fun kSub(site: String) = "apprSub:$site"
    private fun kDone(site: String) = "apprDone:$site"
    private fun kShifts(site: String) = "apprShifts:$site"
    private fun kDeclined(site: String) = "apprDeclined:$site"
    private fun kSaid(site: String, npc: String) = "apprSaid:$site:$npc"

    // ---------------- records (all reads clamped) ----------------

    fun activeProgram(state: GameState): ApprProgram? =
        programAt(state.lifeText[K_ACTIVE])

    fun stagesDone(state: GameState, prog: ApprProgram): Int =
        (state.life[kStage(prog.site)] ?: 0).coerceIn(0, STAGE_COUNT)

    fun subStep(state: GameState, prog: ApprProgram): Int =
        (state.life[kSub(prog.site)] ?: 0).coerceIn(0, 1)

    fun completionDay(state: GameState, prog: ApprProgram): Int =
        state.life[kDone(prog.site)] ?: -1

    fun isDone(state: GameState, prog: ApprProgram): Boolean =
        completionDay(state, prog) >= 0

    fun shiftsAt(state: GameState, prog: ApprProgram): Int =
        (state.life[kShifts(prog.site)] ?: 0).coerceIn(0, 99999)

    fun declinedDay(state: GameState, prog: ApprProgram): Int =
        state.life[kDeclined(prog.site)] ?: -1

    /** The day the active chain's current stage opens:
     *  the day its clock started (the accept day, or the
     *  day the previous stage landed) plus the stage's
     *  gate. -1 when the chain isn't this program's. */
    fun unlockDay(state: GameState, prog: ApprProgram): Int {
        if (activeProgram(state)?.site != prog.site) return -1
        val stage = stagesDone(state, prog)
        if (stage >= STAGE_COUNT) return -1
        val start = state.life[kStageDay(prog.site)] ?: state.absDay
        return start + GAPS[stage]
    }

    fun stageUnlocked(state: GameState, prog: ApprProgram): Boolean {
        val u = unlockDay(state, prog)
        return u >= 0 && state.absDay >= u
    }

    /** The skill as the town reads it: stages landed are
     *  the level (a quit resets the site's card, so an
     *  inactive, unfinished program honestly reads 0). */
    fun skillLevel(state: GameState, prog: ApprProgram): Int =
        if (isDone(state, prog)) STAGE_COUNT
        else stagesDone(state, prog)

    fun skillRung(state: GameState, prog: ApprProgram): String =
        SKILL_RUNGS[skillLevel(state, prog).coerceIn(0, STAGE_COUNT)]

    fun thresholdMet(state: GameState, prog: ApprProgram): Boolean =
        shiftsAt(state, prog) >= prog.threshold

    /** The offer stands: the count is full, the site is
     *  unfinished, no other chain is active (the caller
     *  checks), and it wasn't declined today. */
    fun offerLive(state: GameState, prog: ApprProgram): Boolean =
        !isDone(state, prog) && thresholdMet(state, prog) &&
            declinedDay(state, prog) != state.absDay

    // ---------------- the Jobs seams ----------------

    /** Jobs calls this at every clock-out (the Capstones
     *  seam): the shift's own site counts it. This is the
     *  reliability record — no grind currency, just the
     *  rota's own count of work actually finished. */
    fun onShiftCompleted(state: GameState, def: JobDef) {
        val site = Jobs.workplaceFor(state, def)
        if (programAt(site) == null) return
        state.life[kShifts(site)] =
            ((state.life[kShifts(site)] ?: 0) + 1).coerceIn(0, 99999)
    }

    /** Jobs asks this at the clock-out, inside its own pay
     *  line: a mastered site pays its master +MASTER_BONUS
     *  a shift, in the house's money, forever. */
    fun masterBonus(state: GameState, def: JobDef): Int {
        val site = Jobs.workplaceFor(state, def)
        val prog = programAt(site) ?: return 0
        return if (isDone(state, prog)) MASTER_BONUS else 0
    }

    // ---------------- objectives & status ----------------

    private fun siteName(prog: ApprProgram): String =
        LOCATIONS[prog.site]?.name ?: prog.site

    private fun stageTitle(prog: ApprProgram, stage: Int): String =
        prog.stageTitles.getOrNull(stage) ?: "The work"

    /** The current stage's ask, bare — what the player
     *  actually does next, without the stage framing. */
    fun stepText(state: GameState, prog: ApprProgram): String {
        val stage = stagesDone(state, prog)
        return when (stage) {
            0 -> "Talk to ${prog.mentor} at ${siteName(prog)} — " +
                "the first morning, done the master's way."
            1 -> if (subStep(state, prog) == 0)
                "Pick up the errand from ${prog.mentor} at " +
                    "${siteName(prog)}."
            else
                "Deliver it to ${prog.recipient} — find them " +
                    "and talk; the walk is the work."
            2 -> "The deep work: Look carefully at " +
                "${siteName(prog)}."
            3 -> if (subStep(state, prog) == 0)
                "Talk to ${prog.mentor} at ${siteName(prog)} — " +
                    "something needs a judgment."
            else
                "Settle it: " + prog.judgeResolutions.joinToString(" · ") {
                    it.label }
            else -> "Talk to ${prog.mentor} at ${siteName(prog)} — " +
                "the mark."
        }
    }

    /** What the current stage asks, in one line — the
     *  Journal's and the mentor's shared words for it. */
    fun objectiveText(state: GameState, prog: ApprProgram): String {
        if (isDone(state, prog))
            return "🏅 Mastered — the ${prog.skill} card is " +
                "signed and framed at ${siteName(prog)}."
        if (activeProgram(state)?.site != prog.site)
            return "Not started — the chain begins with " +
                "${prog.mentor}'s offer at ${siteName(prog)}."
        val stage = stagesDone(state, prog)
        val title = stageTitle(prog, stage)
        if (!stageUnlocked(state, prog)) {
            val day = unlockDay(state, prog)
            val wd = Jobs.WEEKDAY_NAMES[Jobs.weekdayOf(day)]
            return "Stage ${stage + 1} of $STAGE_COUNT — $title: " +
                "opens $wd (the work keeps until then)."
        }
        return "Stage ${stage + 1} of $STAGE_COUNT — $title: " +
            stepText(state, prog)
    }

    fun statusText(state: GameState, prog: ApprProgram): String =
        "🎓 ${prog.chainTitle} — ${prog.skill}, under " +
            "${prog.mentor} at ${siteName(prog)}.\n\n" +
            objectiveText(state, prog) +
            "\n\n(The chain ends when you say so — ending it " +
            "costs nothing but the chain itself. The stages " +
            "you've landed stay landed, and the count that " +
            "earned the offer still stands.)"

    // ---------------- the gate (pure queries) ----------------

    /** Does the active chain's current stage judge by THIS
     *  talk (this person, this place)? The errand's
     *  delivery and the judgment's TALK resolutions judge
     *  wherever the person stands (the delivery
     *  discipline); the mentor's own beats judge at the
     *  site, where the work is. */
    private fun stageTalkMatches(state: GameState, prog: ApprProgram,
                                 npc: String, playerLocId: String): Boolean {
        if (activeProgram(state)?.site != prog.site) return false
        if (!stageUnlocked(state, prog)) return false
        val stage = stagesDone(state, prog)
        val sub = subStep(state, prog)
        return when (stage) {
            0 -> npc == prog.mentor && playerLocId == prog.site
            1 -> if (sub == 0)
                npc == prog.mentor && playerLocId == prog.site
            else npc == prog.recipient
            3 -> if (sub == 0)
                npc == prog.mentor && playerLocId == prog.site
            else prog.judgeResolutions.any {
                it.verb == "TALK" && it.target == npc }
            4 -> npc == prog.mentor && playerLocId == prog.site
            else -> false
        }
    }

    /** The apprenticeship step a Talk with [npc] would
     *  take right now, or null. Pure query — GameActivity's
     *  dialogue branch calls this when the gate itself
     *  found nothing (the module rides below the gate
     *  whole: a standing offer waits out every set piece,
     *  delivery and shift, exactly like a chain offer
     *  should). */
    fun talkStepFor(state: GameState, npc: String,
                    playerLocId: String): ApprHit? {
        val active = activeProgram(state)
        if (active == null) {
            val offered = programByMentor(npc) ?: return null
            if (playerLocId != offered.site) return null
            if (!offerLive(state, offered)) return null
            return ApprHit(offered, "OFFER", offered.offerText,
                "🎓 ${offered.chainTitle} — the offer")
        }
        if (stageTalkMatches(state, active, npc, playerLocId))
            return ApprHit(active, "STAGE",
                title = "🎓 ${active.chainTitle} — " +
                    stageTitle(active, stagesDone(state, active)))
        if (npc == active.mentor && playerLocId == active.site)
            return ApprHit(active, "STATUS", statusText(state, active),
                "🎓 ${active.chainTitle}")
        return null
    }

    // ---------------- judging ----------------

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** Plant the work thread in the mentor's head, the way
     *  a finished shift does (Jobs' own seam): the mark is
     *  a thing the town's talks can follow up on. */
    private fun plantWorkThread(state: GameState, prog: ApprProgram) {
        state.agents[prog.mentor]?.memories?.add(
            "🎓 The new kid finished the ${prog.chainTitle} " +
                "under me — a master of ${prog.skill}, signed " +
                "and framed at ${siteName(prog)}.")
        val mem = NpcMemoryStore.getOrCreate(prog.mentor)
        mem.topics.remove(Jobs.TOPIC)
        mem.topics.add(Jobs.TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    private fun grantFriendship(state: GameState, name: String,
                                delta: Int) {
        val cur = state.friendship[name] ?: 0
        state.friendship[name] = (cur + delta).coerceIn(-50, 100)
    }

    /** Land the current stage: pay its stipend, advance
     *  the card, and — at the fifth — sign the mark whole.
     *  Returns the stage's text with its settlement lines. */
    private fun completeStage(state: GameState, prog: ApprProgram,
                              stageText: String, stipend: Int): String {
        val stage = stagesDone(state, prog)
        if (stipend > 0) Minigames.awardCoins(state, stipend)
        val payLine = if (stipend > 0)
            "\n\n(+$stipend 🪙 — ${prog.mentor}'s stipend, " +
                "paid on the day.)" else ""
        state.life[kSub(prog.site)] = 0
        if (stage + 1 >= STAGE_COUNT) {
            state.life[kStage(prog.site)] = STAGE_COUNT
            state.life[kStageDay(prog.site)] = state.absDay
            state.life[kDone(prog.site)] = state.absDay
            state.lifeText[K_ACTIVE] = ""
            plantWorkThread(state, prog)
            note(state, "🎓 MASTERED — ${prog.skill}, under " +
                "${prog.mentor} at ${siteName(prog)}. The town " +
                "will have things to say about that.")
            townLogLine(state, "🎓 The new kid is a master of " +
                "${prog.skill} — ${prog.mentor}'s mark, signed " +
                "at ${siteName(prog)}")
            return stageText + payLine +
                "\n\n(🏅 ${prog.chainTitle} — COMPLETE. The " +
                    "${prog.skill} card is signed and framed at " +
                    "${siteName(prog)}, and the master's rate " +
                    "(+$MASTER_BONUS 🪙 a shift here) is yours " +
                    "from now on.)"
        }
        state.life[kStage(prog.site)] = stage + 1
        state.life[kStageDay(prog.site)] = state.absDay
        return stageText + payLine +
            "\n\n(🎓 Next — " + objectiveText(state, prog) + ")"
    }

    /** End the active chain early: the card resets at the
     *  site, the stipends already paid stay paid (work
     *  done is work done), and the count that earned the
     *  offer is untouched — the offer can stand again. */
    fun quit(state: GameState) {
        val prog = activeProgram(state) ?: return
        state.lifeText[K_ACTIVE] = ""
        state.life[kStage(prog.site)] = 0
        state.life[kSub(prog.site)] = 0
        state.life[kStageDay(prog.site)] = -1
        note(state, "🎓 You ended the ${prog.chainTitle} early. " +
            "${prog.mentor} keeps the door open — the count " +
            "still stands.")
    }

    /** TALK judged (GameActivity's dialogue branch calls
     *  this on a talkStepFor hit). [action] is the dialog's
     *  chosen answer for the two beats with one: "DECLINE"
     *  on an offer, "QUIT" on the standing talk. Fires
     *  exactly the step the query named. */
    fun judgeTalk(state: GameState, npc: String, playerLocId: String,
                  action: String = ""): ApprHit? {
        if (action == "QUIT") {
            val active = activeProgram(state) ?: return null
            if (npc != active.mentor || playerLocId != active.site)
                return null
            quit(state)
            return ApprHit(active, "QUIT", active.quitText,
                "🎓 ${active.chainTitle} — ended")
        }
        val step = talkStepFor(state, npc, playerLocId) ?: return null
        val prog = step.prog
        return when (step.kind) {
            "OFFER" -> {
                if (action == "DECLINE") {
                    state.life[kDeclined(prog.site)] = state.absDay
                    note(state, "🎓 You left ${prog.mentor}'s " +
                        "offer standing at ${siteName(prog)} — " +
                        "it keeps, and returns tomorrow.")
                    ApprHit(prog, "DECLINE", prog.declineText,
                        "🎓 ${prog.chainTitle} — not today")
                } else {
                    state.lifeText[K_ACTIVE] = prog.site
                    state.life[kStage(prog.site)] = 0
                    state.life[kSub(prog.site)] = 0
                    state.life[kStageDay(prog.site)] = state.absDay
                    note(state, "🎓 Apprenticeship begun — " +
                        "${prog.chainTitle}, under ${prog.mentor} " +
                        "at ${siteName(prog)}. See the Journal's " +
                        "🎓 APPRENTICESHIPS page.")
                    townLogLine(state, "🎓 The new kid is " +
                        "${prog.mentor}'s apprentice now " +
                        "(${prog.chainTitle})")
                    ApprHit(prog, "ACCEPT", prog.acceptText +
                        "\n\n(🎓 " + objectiveText(state, prog) + ")",
                        "🎓 ${prog.chainTitle} — signed on")
                }
            }
            "STAGE" -> {
                val stage = stagesDone(state, prog)
                val sub = subStep(state, prog)
                when (stage) {
                    0 -> ApprHit(prog, "STAGE",
                        completeStage(state, prog, prog.s0Text,
                            STIPENDS[0]),
                        "🎓 ${prog.chainTitle} — ${stageTitle(prog, 0)}")
                    1 -> if (sub == 0) {
                        state.life[kSub(prog.site)] = 1
                        ApprHit(prog, "STAGE", prog.pickupText +
                            "\n\n(🎓 Carrying it now — deliver it " +
                                "to ${prog.recipient}. The walk is " +
                                "the work: legs cost Walk minutes, " +
                                "as ever.)",
                            "🎓 ${prog.chainTitle} — " +
                                stageTitle(prog, 1))
                    } else ApprHit(prog, "STAGE",
                        completeStage(state, prog, prog.deliverText,
                            STIPENDS[1]),
                        "🎓 ${prog.chainTitle} — ${stageTitle(prog, 1)}")
                    3 -> if (sub == 0) {
                        state.life[kSub(prog.site)] = 1
                        ApprHit(prog, "STAGE", prog.judgePoseText +
                            "\n\n(🎓 The judgment is posed. Settle " +
                                "it your way: " +
                                prog.judgeResolutions.joinToString(" · ") {
                                    it.label } + ".)",
                            "🎓 ${prog.chainTitle} — " +
                                stageTitle(prog, 3))
                    } else {
                        val res = prog.judgeResolutions.firstOrNull {
                            it.verb == "TALK" && it.target == npc
                        } ?: return null
                        res.friendTarget?.let {
                            grantFriendship(state, it, 1) }
                        ApprHit(prog, "STAGE",
                            completeStage(state, prog, res.text,
                                STIPENDS[3] + res.quality),
                            "🎓 ${prog.chainTitle} — " +
                                stageTitle(prog, 3))
                    }
                    4 -> ApprHit(prog, "STAGE",
                        completeStage(state, prog, prog.markText,
                            STIPENDS[4]),
                        "🎓 ${prog.chainTitle} — ${stageTitle(prog, 4)}")
                    else -> null
                }
            }
            else -> null
        }
    }

    /** LOOK judged: the deep-work stage at its site, and
     *  the judgment's LOOK resolutions wherever they
     *  stand — plus the standing traces a careful Look
     *  reads at a program's site (an offer waiting, your
     *  chain's next step, the framed card). The Look chain
     *  in GameActivity asks here after Jobs, like every
     *  participation beat. */
    fun lookText(state: GameState, locId: String): String? {
        val active = activeProgram(state)
        if (active != null && stageUnlocked(state, active)) {
            val stage = stagesDone(state, active)
            val sub = subStep(state, active)
            if (stage == 2 && locId == active.site) {
                return "🎓 ${stageTitle(active, 2)} — " +
                    completeStage(state, active, active.s2Text,
                        STIPENDS[2])
            }
            if (stage == 3 && sub == 1) {
                val res = active.judgeResolutions.firstOrNull {
                    it.verb == "LOOK" && it.target == locId }
                if (res != null) {
                    res.friendTarget?.let {
                        grantFriendship(state, it, 1) }
                    return "🎓 ${stageTitle(active, 3)} — " +
                        completeStage(state, active, res.text,
                            STIPENDS[3] + res.quality)
                }
            }
        }
        val prog = programAt(locId) ?: return null
        if (isDone(state, prog))
            return "🏅 A framed card at ${siteName(prog)} names " +
                "the new kid a master of ${prog.skill} — " +
                "${prog.mentor}'s mark, signed and kept."
        if (active?.site == prog.site)
            return "🎓 ${prog.chainTitle}: " +
                objectiveText(state, prog)
        if (active == null && offerLive(state, prog))
            return "🎓 Word at ${siteName(prog)}: ${prog.mentor} " +
                "has been watching your shifts here — the count " +
                "is full, and they may have something to say to " +
                "you."
        return null
    }

    // ---------------- the mentor's hours (the one claim) ----------------

    /** The Engine's agenda lookup asks here, right after
     *  Jobs (work's own family): while an offer stands for
     *  the player, or the active chain has a stage unlocked
     *  that waits on the mentor or their site, the mentor
     *  keeps their counter through the working day. Every
     *  claim seated below in the stack (screenings,
     *  ceremonies, finales, the fair, the set pieces)
     *  still wins the person — this claim only ever beats
     *  the base agenda and the dinner family's daytime
     *  silence, and it stands down whole while the town
     *  owns the moment. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        val prog = programByMentor(npcName) ?: return null
        if (timeMin !in CLAIM_FROM until CLAIM_TO) return null
        if (Emergencies.momentOwned(state)) return null
        val active = activeProgram(state)
        if (active == null) {
            if (!offerLive(state, prog)) return null
            return AgendaBlock(prog.site, prog.claimActivity)
        }
        if (active.site != prog.site) return null
        if (!stageUnlocked(state, prog)) return null
        val stage = stagesDone(state, prog)
        val sub = subStep(state, prog)
        val waitsOnMentor = when (stage) {
            0, 2, 4 -> true
            1 -> sub == 0
            3 -> sub == 0 || prog.judgeResolutions.any {
                it.verb == "LOOK" && it.target == prog.site ||
                    it.verb == "TALK" && it.target == prog.mentor }
            else -> false
        }
        if (!waitsOnMentor) return null
        return AgendaBlock(prog.site, prog.claimActivity)
    }

    // ---------------- the town remarks ----------------

    private val REMARK_BANK: List<String> = listOf(
        "Word travels: the new kid finished under {mentor} — " +
            "a master of {skill}, signed and framed at {site}.",
        "I saw the card at {site}. {mentor} doesn't sign " +
            "those for just anybody.",
        "They're saying {mentor}'s apprentice made master — " +
            "{skill}, the whole chain. That's you, isn't it.",
        "Historic day at {site}: {mentor} nodded at " +
            "somebody's work. Yours, apparently.")

    /** The lines a completion is owed in conversation, on
     *  the memory channel beside the clubhouse's (Games'
     *  talk assembly asks here): the mentor's own standing
     *  line — forever, the favor-line discipline — and the
     *  neighbors' remarks, once each, inside the three
     *  weeks the town is still talking about it. */
    fun remarkLines(state: GameState, npcName: String): List<Line> {
        val out = mutableListOf<Line>()
        programByMentor(npcName)?.let { prog ->
            if (isDone(state, prog))
                out.add(Line(npcName, prog.masterLine))
        }
        if (npcName !in state.agents) return out
        // the freshest mark this person hasn't remarked on
        val fresh = PROGRAMS
            .filter { prog ->
                val d = completionDay(state, prog)
                d >= 0 && state.absDay - d <= REMARK_DAYS &&
                    prog.mentor != npcName &&
                    (state.life[kSaid(prog.site, npcName)] ?: -1) != d
            }
            .maxByOrNull { completionDay(state, it) } ?: return out
        state.life[kSaid(fresh.site, npcName)] =
            completionDay(state, fresh)
        val bank = REMARK_BANK[
            ((npcName.hashCode() + fresh.site.hashCode()) %
                REMARK_BANK.size + REMARK_BANK.size) % REMARK_BANK.size]
        out.add(Line(npcName, bank
            .replace("{mentor}", fresh.mentor)
            .replace("{skill}", fresh.skill)
            .replace("{site}", siteName(fresh))))
        return out
    }

    // ---------------- the paper ----------------

    /** The Gazette headline stack stays at ten tiers; a
     *  graduation is section copy. The completion's own
     *  week carries the line, in TOWN NEWS — offers are
     *  private and in person, but a signed mark is the
     *  town's news, and the paper prints it in the week
     *  it happened (Newspaper asks here). */
    fun paperLine(state: GameState, week: Int): String? {
        for (prog in PROGRAMS) {
            val d = completionDay(state, prog)
            if (d >= 0 && Jobs.weekIndex(d) == week)
                return prog.paperText
        }
        return null
    }

    // ---------------- the Journal page ----------------

    private val MONTHS = listOf("January", "February", "March",
        "April", "May", "June", "July", "August", "September",
        "October", "November", "December")

    fun dateLabelOf(absDay: Int): String {
        val key = keyFromOrdinal(FairCalendar.ordinalOfAbsDay(absDay))
        val y = key.substring(0, 4)
        val m = key.substring(4, 6).toIntOrNull() ?: 1
        val d = key.substring(6, 8).toIntOrNull() ?: 1
        return "${MONTHS[(m - 1).coerceIn(0, 11)]} $d, $y"
    }

    /** The 🎓 APPRENTICESHIPS page, assembled pure like
     *  its siblings: the five counters, the counts, the
     *  chains, the skills, the marks — and the honest
     *  instructions for starting, and ending, one. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val active = activeProgram(state)
        out += "🎓 APPRENTICESHIPS — the town's second tier " +
            "of work: a keeper who has watched you work their " +
            "counter steady offers a chain of real stages " +
            "over the better part of two weeks, a skill that " +
            "deepens, and a signed mark at the end."
        for (prog in PROGRAMS) {
            val name = siteName(prog)
            val job = Jobs.defById(prog.jobId)
            when {
                isDone(state, prog) ->
                    out += "🏅 ${prog.chainTitle} (${name}) — " +
                        "MASTER of ${prog.skill}, signed by " +
                        "${prog.mentor} on " +
                        "${dateLabelOf(completionDay(state, prog))}. " +
                        "Master's rate: +$MASTER_BONUS 🪙 on " +
                        "every ${job?.title ?: "shift"} shift " +
                        "worked at $name."
                active?.site == prog.site -> {
                    out += "🎓 ${prog.chainTitle} (${name}) — " +
                        "under ${prog.mentor}: " +
                        "${stagesDone(state, prog)} of " +
                        "$STAGE_COUNT stages landed. Skill: " +
                        "${prog.skill} — ${skillRung(state, prog)}."
                    out += "▶ " + objectiveText(state, prog)
                    out += "To end it early, talk to " +
                        "${prog.mentor} at $name — no hard " +
                        "feelings; the card goes back in the " +
                        "drawer and the count still stands."
                }
                active == null && offerLive(state, prog) ->
                    out += "🎓 ${prog.chainTitle} (${name}) — " +
                        "${prog.mentor} is ready to make an " +
                        "offer: ${shiftsAt(state, prog)} of " +
                        "${prog.threshold} shifts counted here. " +
                        "Talk to ${prog.mentor} at $name."
                else ->
                    out += "🎓 ${prog.chainTitle} (${name}) — " +
                        "${shiftsAt(state, prog)} of " +
                        "${prog.threshold} shifts at $name on " +
                        "the count. Work the " +
                        "${job?.title ?: "job"} here steady and " +
                        "${prog.mentor} may offer you the chain." +
                        if (active != null)
                            " (One at a time — a chain is " +
                                "already running.)" else ""
            }
        }
        val anyDone = PROGRAMS.count { isDone(state, it) }
        if (anyDone > 0)
            out += "Marks held: $anyDone of ${PROGRAMS.size}. " +
                "The town remarks on a fresh one for about " +
                "three weeks; the framed cards are forever."
        return out
    }
}

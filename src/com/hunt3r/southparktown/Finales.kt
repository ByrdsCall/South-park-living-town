package com.hunt3r.southparktown

// ============================================================
// Season Finales — v0.5.0h (Queue #5 item 8): every season
// ends with a set-piece.
//
// A daily episode is a day; a holiday is a window; a FINALE
// is a WEEK — the last seven days of a season, by the game's
// own season boundaries (seasonFor's months: spring ends
// May 31, summer Aug 31, fall Nov 30, winter the last day of
// February). Each finale plays the same shape, bigger than
// a daily episode:
//
//   OPENING   the ensemble gathers at one place (a schedule
//             override on the opening and closing days) and
//             the host declares the thing — an episode title
//             card, like the holidays get;
//   BEATS     4 judged steps across the week (Talk / Look /
//             Photo, the verbs the holiday beats and faction
//             missions already taught) — catch them in their
//             day ranges or miss them; the show goes on;
//   THE CALL  on the last day, a player who caught at least
//             one beat is handed the closing choice, at a
//             safe moment, exactly like a case's climax —
//             and exactly like a case's climax, it DEFERS
//             while any mystery's choice is on the table.
//             Two player-facing climaxes never stack.
//
// The finale pre-empts the daily episode roll for its whole
// window (mystery chapters still take the slot first — the
// cases outrank everything). Ignoring a finale is safe: it
// wraps without you, the town makes the call itself, the
// Journal records that you weren't there, and it returns
// next year. Playing one leaves a SEASONAL MARK — a standing
// note at the finale's place that lasts until the same
// season's finale wraps again next year — and next year's
// opening acknowledges whoever history currently credits.
//
// State rides the existing `life` / `lifeText` maps (one
// mask, one choice, one done-flag per (finale, year), the
// standing mark per finale) — no new save fields, and every
// read is capped against the registry, so a hand-inflated
// save can't fake a verdict. Pure Kotlin, like its siblings.
// ============================================================

/** One judged beat of a finale week. [line] is the full
 *  narration when the beat lands; [short] is its ticker
 *  version. Beats are ordered: a beat unlocks when every
 *  earlier beat has landed OR aged out of its day range. */
data class FinaleStep(
    val verb: String,            // TALK | LOOK | PHOTO
    val title: String,           // the beat's name (episode acts)
    val line: String,
    val short: String,
    val who: String? = null,     // TALK: the person
    val loc: String? = null,     // TALK: where they must be · LOOK/PHOTO: where you must be
    val dayFrom: Int = 0,        // window days this beat can land (inclusive)
    val dayTo: Int = 6
)

/** One side of a finale's closing call. [markText] is the
 *  standing mark the call leaves at the finale's place;
 *  [winner] is who history credits (a name, or "a tie"). */
data class FinaleChoice(
    val key: String,
    val label: String,
    val pickLine: String,
    val outcome: String,
    val markText: String,
    val winner: String
)

data class FinaleDef(
    val id: String,              // "spring" | "summer" | "autumn" | "winter"
    val season: Season,
    val seasonName: String,      // "Spring"
    val emoji: String,
    val title: String,
    val loc: String,             // the gathering place
    val host: String,
    val ensemble: List<String>,  // incl. the host
    val intro: String,           // one line: what this finale is
    val hint: String,            // the where-to-be line while it's live
    val closingCall: String,     // the last morning's summons
    val gatherActivity: String,  // the ensemble's agenda text on gathering days
    val windowLabel: String,     // "the last week of May"
    val steps: List<FinaleStep>,
    val choices: List<FinaleChoice>,
    val townChoice: Int,         // the call the town makes when you don't
    val townOutcome: String,     // participated, but the call wasn't yours
    val absentOutcome: String,   // you never showed at all
    val decorId: String,         // the House piece a played finale earns
    val ambient: Map<String, String> = emptyMap(),
    val ambientGeneric: List<String> = emptyList()
)

/** A beat that just landed — what the rails show the player. */
data class FinaleHit(
    val def: FinaleDef,
    val stepIndex: Int,
    val text: String,
    val allLanded: Boolean
)

object Finales {

    /** The window's length, in days: the season's last week. */
    const val WINDOW_DAYS = 7

    /** Gathering hours on the opening and closing days. */
    const val GATHER_FROM = 9 * 60
    const val GATHER_TO = 18 * 60

    // ---------------- state keys (life / lifeText) ----------------

    private fun kStart(id: String, iy: Int) = "fin:$id:$iy:start"
    private fun kMask(id: String, iy: Int) = "fin:$id:$iy:mask"
    private fun kChoice(id: String, iy: Int) = "fin:$id:$iy:choice"
    private fun kDone(id: String, iy: Int) = "fin:$id:$iy:done"
    private fun kMark(id: String) = "finMark:$id"
    private fun kMarkYear(id: String) = "finMarkYear:$id"
    private fun kMarkBy(id: String) = "finMarkBy:$id"
    private fun kMarkWinner(id: String) = "finMarkWinner:$id"
    private fun kRuns(id: String) = "finRuns:$id"
    private fun kPlayed(id: String) = "finPlayed:$id"

    // ---------------- the four finales ----------------

    val DEFS: List<FinaleDef> = listOf(

        // ---------- SPRING: the Great Garden Verdict ----------
        FinaleDef(
            id = "spring", season = Season.SPRING, seasonName = "Spring",
            emoji = "🌷",
            title = "The Great Garden Verdict",
            loc = "KidsPark", host = "Sharon",
            ensemble = listOf("Sharon", "Randy", "Liane", "Butters",
                "Stan", "Sheila", "Gerald", "Wendy"),
            intro = "The community garden's first-ever judged show — " +
                "every plot in town, one verdict, absolutely no pressure.",
            hint = "The garden show runs at the Kid's Park plot all week. " +
                "Catch the beats there; the verdict lands on the last day.",
            closingCall = "the Garden Verdict is handed down at the plot today — " +
                "the judges have drafted YOU, the town's only neutral party.",
            gatherActivity = "Garden Verdict day — judging plots and " +
                "pretending not to have favorites.",
            windowLabel = "the last week of May",
            steps = listOf(
                FinaleStep("TALK", "The Staking of the Plots",
                    line = "Sharon hammers the last plot stake in with the heel of her " +
                        "gardening shoe and straightens up like a judge taking the bench. " +
                        "“Rules of the Verdict: everything judged as grown. No store-bought " +
                        "ringers, no performance-enhancing anything.” She looks directly at " +
                        "Randy when she says the last part. Randy studies the sky. The " +
                        "first Garden Verdict is officially ON.",
                    short = "🌷 The Garden Verdict is declared. Randy is studying the sky.",
                    who = "Sharon", loc = "KidsPark", dayFrom = 0, dayTo = 1),
                FinaleStep("LOOK", "The Suspicious Bag",
                    line = "The plots, row by row: Sheila's kale, standing to attention. " +
                        "Liane's roses, already showing off. Butters' one brave bean. And " +
                        "behind Randy's row — which is three inches taller than it was " +
                        "yesterday — a feed bag stenciled TEGRIDY MIRACLE DUST, with the " +
                        "ingredients listed as 'mostly minerals and confidence'. The " +
                        "minerals check out. The confidence is doing a lot of work.",
                    short = "🌷 Behind Randy's row: one (1) bag of Miracle Dust. Mostly confidence.",
                    loc = "KidsPark", dayFrom = 1, dayTo = 3),
                FinaleStep("TALK", "Sergeant Sprout",
                    line = "Butters is kneeling by a bean sprout in a paper cup, giving it " +
                        "a full pep talk. “Sergeant Sprout is scared, fellas. All the other " +
                        "plants are so big, an' he's just a little guy in a cup.” You tell " +
                        "him the Sergeant has something the kale doesn't: a story. Butters " +
                        "nods like a general who's just understood the war. The sprout " +
                        "stands noticeably straighter. Probably.",
                    short = "🌷 Sergeant Sprout gets a pep talk. Morale: up. Height: pending.",
                    who = "Butters", dayFrom = 2, dayTo = 4),
                FinaleStep("TALK", "The Confession of the Dust",
                    line = "You find Randy behind his row with the Miracle Dust bag and the " +
                        "guilty posture of a man watering a lie. “It's not cheating,” he " +
                        "says, immediately. “It's AGRICULTURAL ENTHUSIASM. Sharon's " +
                        "tomatoes have breeding. My kale has a dream and a bag.” A pause. " +
                        "“...The bag is mostly glitter, isn't it.” It's mostly glitter, " +
                        "Randy. “Yeah. Okay. But the kale believes in it, and that's real.”",
                    short = "🌷 Randy confesses: the dust is mostly glitter. The kale believes anyway.",
                    who = "Randy", dayFrom = 3, dayTo = 5)
            ),
            choices = listOf(
                FinaleChoice("sharon",
                    label = "🏆 Crown Sharon's heritage tomatoes — honest dirt, honest win",
                    pickLine = "🌷 You raise the tomato crate. The plot erupts. Randy is " +
                        "already telling people he 'grew the competition'.",
                    outcome = "You crowned Sharon's heritage tomatoes — grown on " +
                        "compost, patience and spite, and simply better than everything " +
                        "else in the ground.",
                    markText = "A hand-painted sign at the garden plot: SHARON'S TOMATOES — " +
                        "VERDICT: BEST IN TOWN. It weathers there, smug and accurate.",
                    winner = "Sharon"),
                FinaleChoice("butters",
                    label = "🏆 Crown Butters' bean sprout — one brave bean, grown with love",
                    pickLine = "🌷 You raise the paper cup. Butters salutes the sprout, " +
                        "then you, then the sprout again, crying a little.",
                    outcome = "You crowned Sergeant Sprout — one brave bean in a paper " +
                        "cup, out-argued every kale in town on story alone.",
                    markText = "A little sign at the garden plot: SERGEANT SPROUT — " +
                        "VERDICT: BRAVEST BEAN IN TOWN. The cup is bronzed. The bean is legend.",
                    winner = "Butters")
            ),
            townChoice = 0,
            townOutcome = "The judges' panel (Father Maxi, the Mayor, and one " +
                "very serious kindergartner) crowned Sharon's tomatoes by committee — " +
                "they were, objectively, showing off.",
            absentOutcome = "The Garden Verdict happened without you: Sharon's " +
                "tomatoes took it by committee, Randy's dust was confiscated as a " +
                "fire hazard, and Sergeant Sprout got an honorable mention and a " +
                "bigger cup.",
            decorId = "finale_garden",
            ambient = mapOf(
                "Sharon" to "Judged as grown, that's the rule. If Randy's kale " +
                    "glows in the dark I'm not responsible for what I say.",
                "Randy" to "My kale has a dream and a bag, and frankly the dream " +
                    "is load-bearing.",
                "Butters" to "Sergeant Sprout is in the Verdict! He's nervous. " +
                    "I'm nervous. We're a nervous unit."),
            ambientGeneric = listOf(
                "You going to the garden show? The whole town's pretending " +
                    "it's not a blood sport.",
                "Verdict week's the best week. Everybody's kale has a lawyer.")),

        // ---------- SUMMER: the Pond Days Regatta ----------
        FinaleDef(
            id = "summer", season = Season.SUMMER, seasonName = "Summer",
            emoji = "⛵",
            title = "The Pond Days Regatta",
            loc = "StarksPond", host = "Jimbo",
            ensemble = listOf("Jimbo", "Ned", "Stan", "Cartman",
                "Kenny", "Craig", "Kyle", "Butters"),
            intro = "Paper boats, real grudges: the town's fleet races " +
                "Stark's Pond for a brass plate and a year of bragging.",
            hint = "The fleet musters at Stark's Pond all week. Catch the " +
                "beats dockside; the race — and the call — land on the last day.",
            closingCall = "the Regatta runs TODAY — and if it's close, " +
                "Marshal Jimbo has named you the finish-line judge.",
            gatherActivity = "Regatta day — at the dock, arguing about handicaps.",
            windowLabel = "the last week of August",
            steps = listOf(
                FinaleStep("TALK", "The Mustering of the Fleet",
                    line = "Jimbo stands on the dock with a sailing hat he has not " +
                        "earned and a whistle he absolutely has. “RULES OF THE REGATTA: " +
                        "paper hulls, pond water, no motors, no whining — the whining " +
                        "rule is new, and it's because of last year.” Ned, beside him, " +
                        "raises the starting cap gun. Through the voice box: “ON. YOUR. " +
                        "MARKS. EVENTUALLY.” The fleet is mustered. The Pond Days " +
                        "Regatta is ON.",
                    short = "⛵ The fleet is mustered. Ned's cap gun is loaded. Eventually.",
                    who = "Jimbo", loc = "StarksPond", dayFrom = 0, dayTo = 1),
                FinaleStep("LOOK", "The Fleet, Inspected",
                    line = "The fleet, bow to stern: Stan's newspaper dart, folded like " +
                        "it has somewhere to be. Kenny's boat, made from a math test " +
                        "(graded: D — the boat is racing on spite). Craig's entry, an " +
                        "unfolded sheet of paper lying flat on the dock. “It's a boat,” " +
                        "Craig says. “It identifies as a boat.” And Cartman's so-called " +
                        "yacht, which is a cafeteria tray with a cocktail flag, riding " +
                        "suspiciously high in the water and everyone's suspicion.",
                    short = "⛵ Fleet inspected: one dart, one D-minus, one concept, one 'yacht'.",
                    loc = "StarksPond", dayFrom = 1, dayTo = 3),
                FinaleStep("TALK", "The Test Float",
                    line = "Kenny's boat is doing its test float and losing — the D-minus " +
                        "is dissolving at the corners and the whole hull is going the " +
                        "color of old tea. Kenny watches it sink an inch and says " +
                        "something muffled and philosophical. You help him tape the " +
                        "corners and write DNF INSURANCE on the sail in marker. “(muffled) " +
                        "Mmph rmph rmph.” He's right. A boat that expects to sink is a " +
                        "boat that's free.",
                    short = "⛵ Kenny's boat gets tape, a warning label, and a philosophy.",
                    who = "Kenny", dayFrom = 2, dayTo = 4),
                FinaleStep("PHOTO", "The Official Fleet Photo",
                    line = "Jimbo insists on documentation. “Every great fleet has an " +
                        "official portrait, and every great portrait has at least one " +
                        "kid making a face.” You raise the camera at the dock — boats " +
                        "in the foreground, sailors behind them, one tray-yacht already " +
                        "trying to edge into the middle. This one's for the town board.",
                    short = "⛵ The fleet sits for its official portrait. The tray behaved. Barely.",
                    loc = "StarksPond", dayFrom = 3, dayTo = 5)
            ),
            choices = listOf(
                FinaleChoice("stan",
                    label = "🏆 Call it for Stan — the dart was first across, by a fold",
                    pickLine = "⛵ You point at the dart. Jimbo fires the cap gun at " +
                        "the sky in ratification. Ned nods once, like a lighthouse.",
                    outcome = "You called the finish for Stan's newspaper dart — " +
                        "first across the pond by the width of one fold, fair and square.",
                    markText = "Stan's dart, varnished within an inch of its life, " +
                        "nailed to the dock post under a brass plate: REGATTA CHAMPION.",
                    winner = "Stan"),
                FinaleChoice("kenny",
                    label = "🏆 Call it for Kenny — the boat sank twice and STILL finished",
                    pickLine = "⛵ You point at the soggy homework. The dock loses its " +
                        "mind. Somewhere, a D-minus becomes a trophy.",
                    outcome = "You called the finish for Kenny's homework boat — it " +
                        "sank twice, dissolved once, and finished anyway, which the " +
                        "crowd decided was the whole point of boats.",
                    markText = "A brass plate on the dock post: KENNY'S BOAT — IT " +
                        "FINISHED. The boat itself retired to a shoebox, a hero.",
                    winner = "Kenny")
            ),
            townChoice = 0,
            townOutcome = "Jimbo squinted at the finish for a full minute and " +
                "gave it to Stan's dart by a fold — the tray-yacht's appeal was " +
                "denied on the grounds of being a tray.",
            absentOutcome = "The Regatta happened without you: Stan's dart took " +
                "the plate by a fold, Kenny's boat finished to a standing ovation " +
                "and immediately sank in celebration, and Craig's boat is still " +
                "out there, being a concept.",
            decorId = "finale_regatta",
            ambient = mapOf(
                "Jimbo" to "Handicaps are set, the dock is swept, and Ned has " +
                    "been practicing his countdown all week. It's chilling.",
                "Cartman" to "My yacht is regulation. The regulation is in " +
                    "international waters. You can't check.",
                "Kenny" to "(muffled) Mmph rmph rmph-mmph. — he believes in the boat."),
            ambientGeneric = listOf(
                "Regatta week! My money's on the dart, but my heart's with " +
                    "the homework.",
                "Nobody misses the Regatta. The tray discourse alone is worth it.")),

        // ---------- FALL: the Harvest Weigh-In ----------
        FinaleDef(
            id = "autumn", season = Season.FALL, seasonName = "Fall",
            emoji = "🌾",
            title = "The Harvest Weigh-In",
            loc = "TegridyFarms", host = "Randy",
            ensemble = listOf("Randy", "Sharon", "Stuart", "Carol",
                "Jimbo", "Stan", "Kyle", "Towelie"),
            intro = "One giant squash, one honest pumpkin, one farm scale " +
                "with a five-pound secret — the town weighs in.",
            hint = "The weigh-in runs at Tegridy Farms all week. Catch the " +
                "beats around the squash; the scale tells its story on the last day.",
            closingCall = "the Weigh-In is TODAY — and Sharon has put the " +
                "certified county hook in YOUR hands, because you're the only " +
                "one here nobody's related to.",
            gatherActivity = "Weigh-in day — guarding the squash like it's a bank vault.",
            windowLabel = "the last week of November",
            steps = listOf(
                FinaleStep("TALK", "The Unveiling",
                    line = "Randy gathers the town around a tarp the size of a " +
                        "swimming pool and clears his throat for a full ten seconds. " +
                        "“BEHOLD,” he says, and pulls the cord, “THE SQUASH.” It is, " +
                        "objectively, a ridiculous squash — orange as a sunset and " +
                        "roughly the square footage of a studio apartment. Somewhere " +
                        "behind it, Stuart's pumpkin sits modest and unbothered, " +
                        "like a man who has already read the ending. The Weigh-In " +
                        "is ON.",
                    short = "🌾 BEHOLD. THE SQUASH. (It has its own zip code.)",
                    who = "Randy", loc = "TegridyFarms", dayFrom = 0, dayTo = 1),
                FinaleStep("LOOK", "The Second Vine",
                    line = "Up close, the squash is even bigger and faintly smug. " +
                        "But follow its vine and there's a SECOND one, sneaking " +
                        "under the barn door — and in the barn's shadow, on a bed " +
                        "of straw like a understudy in a dressing room, sits the " +
                        "BACKUP SQUASH. Nearly as big. Randy has grown a spare. In " +
                        "case the first one 'gets ideas'. The first one has shown " +
                        "no signs of ideas. The system, though. The system works.",
                    short = "🌾 There's a backup squash in the barn. The system works.",
                    loc = "TegridyFarms", dayFrom = 1, dayTo = 3),
                FinaleStep("TALK", "The Honest Pumpkin",
                    line = "Stuart's pumpkin weighs what it weighs and so does " +
                        "Stuart. “Coffee grounds an' patience,” he says. “Kids " +
                        "think growin's a race. Growin's a nap you supervise.” He " +
                        "tells you he grew it for the pie, mostly — the ribbon's " +
                        "just where the town keeps its opinions. If the big one " +
                        "wins, he'll clap. If his wins, he'll clap. The clapping, " +
                        "he explains, is also for the pie.",
                    short = "🌾 Stuart grew his for the pie. The ribbon is the town's business.",
                    who = "Stuart", dayFrom = 2, dayTo = 4),
                FinaleStep("TALK", "The Generous Scale",
                    line = "Sharon takes you aside by the barn and tells you about " +
                        "the farm scale — the big hanging one, the official one. " +
                        "“It runs five pounds generous. Has since the county fair " +
                        "incident, and Randy knows, and Randy has FEELINGS about " +
                        "the number five.” She's kept the certified county hook in " +
                        "the barn for years, oiled and true. “On the day, the hook " +
                        "goes in the hands of somebody with no squash in the race. " +
                        "That's you, kid.”",
                    short = "🌾 The farm scale runs five pounds generous. There's a true hook. It's yours.",
                    who = "Sharon", dayFrom = 3, dayTo = 5)
            ),
            choices = listOf(
                FinaleChoice("truescale",
                    label = "🏆 Weigh it on the certified hook — a true number, whatever it says",
                    pickLine = "🌾 You hang the squash from the true hook. The needle " +
                        "settles. The whole farm goes quiet enough to hear a pumpkin think.",
                    outcome = "You weighed them true, and the true number was a dead " +
                        "tie — Randy's squash and Stuart's pumpkin, a shared crown. " +
                        "Randy melted down for exactly one minute, then melted: " +
                        "'CO-CHAMPION' has a ring, apparently.",
                    markText = "Two names on the farm-gate board: MARSH — McCORMICK — " +
                        "SHARED CROWN. Randy painted his name bigger. Stuart let him.",
                    winner = "a tie"),
                FinaleChoice("letride",
                    label = "🏆 Let the farm scale stand — Randy's number, Randy's day",
                    pickLine = "🌾 You nod at the farm scale. Randy's face does something " +
                        "complicated, like a man being forgiven by a squash.",
                    outcome = "You let the generous scale stand, and Randy's squash " +
                        "took the ribbon by four pounds and one open secret. Stuart " +
                        "shrugged and went home to make the pie, which was the plan " +
                        "all along.",
                    markText = "The farm-gate board reads: BIGGEST SQUASH — R. MARSH " +
                        "(BY FOUR POUNDS). Underneath, in pencil, Stuart has written: " +
                        "'generous scale'. It stays.",
                    winner = "Randy")
            ),
            townChoice = 0,
            townOutcome = "With no neutral party on the hook, Sharon quietly " +
                "used the certified one herself — dead tie, shared crown, and " +
                "Randy only found out at the engraving.",
            absentOutcome = "The Weigh-In happened without you: Sharon ran the " +
                "certified hook herself, it came out a dead tie, and Randy has " +
                "been saying 'co-champion' like a title of nobility ever since.",
            decorId = "finale_harvest",
            ambient = mapOf(
                "Randy" to "The squash is in the zone. Do not perceive the " +
                    "squash incorrectly this week.",
                "Sharon" to "Five pounds generous. The scale, the squash, AND " +
                    "the man — all three, generous. One of them I can fix.",
                "Stuart" to "Pie's the prize. Always was. Ribbon's just the town talkin'."),
            ambientGeneric = listOf(
                "Weigh-in week! The squash has security now. Security is Jimbo.",
                "Place your bets: squash by a mile, or pumpkin by a technicality?")),

        // ---------- WINTER: the Longest Night Lantern Walk ----------
        FinaleDef(
            id = "winter", season = Season.WINTER, seasonName = "Winter",
            emoji = "🕯️",
            title = "The Longest Night Lantern Walk",
            loc = "MainStreet", host = "Father Maxi",
            ensemble = listOf("Father Maxi", "McDaniels", "Butters",
                "Kenny", "Stan", "Kyle", "Sharon", "Liane"),
            intro = "On the darkest week of the year, the whole town " +
                "carries paper lanterns down Main Street — one for every " +
                "household, and one for whoever's missing.",
            hint = "The lanterns are strung along Main Street all week. " +
                "Catch the beats there; the Walk itself steps off on the last night.",
            closingCall = "the Lantern Walk steps off at dusk TODAY — and " +
                "there's one lantern left over, and a whole town watching " +
                "to see whose hands it ends in.",
            gatherActivity = "Lantern Walk day — carrying light for the whole street.",
            windowLabel = "the last week of February",
            steps = listOf(
                FinaleStep("TALK", "The Announcement",
                    line = "Father Maxi takes the library steps with a paper lantern " +
                        "in each hand and the air of a man who has done this in " +
                        "worse weather. “The Walk is simple,” he says. “Dusk, last " +
                        "day of the dark week. Every household carries a light down " +
                        "Main Street, and nobody walks in the dark alone.” The Mayor, " +
                        "beside him, nods with the full force of municipal government. " +
                        "Somebody in the crowd has already started crying, warmly. " +
                        "The Longest Night Lantern Walk is ON.",
                    short = "🕯️ The Walk is announced: dusk, last day, nobody walks dark alone.",
                    who = "Father Maxi", loc = "MainStreet", dayFrom = 0, dayTo = 1),
                FinaleStep("LOOK", "The Stringing of the Street",
                    line = "All week, Main Street gets ready: paper lanterns strung " +
                        "overhead on sagging lines, tested one by one — gold, rose, " +
                        "one shaped defiantly like a fish. At the far end of the " +
                        "street the memorial bench has been swept, and its little " +
                        "iron lantern hook polished bright where a hundred thumbs " +
                        "have held it. The street looks like it's holding its breath " +
                        "in a nice way.",
                    short = "🕯️ Lanterns strung, bench swept, hook polished. The street is ready.",
                    loc = "MainStreet", dayFrom = 1, dayTo = 3),
                FinaleStep("TALK", "A Candle in Each Mitten",
                    line = "Butters is doing lantern drills on the sidewalk, walking " +
                        "very slowly with his elbows out. “My lantern goes out every " +
                        "year, fellas. EVERY year. There's photos.” He shows you " +
                        "the system: a spare candle in the left mitten, a spare " +
                        "candle in the right mitten, and a third taped — carefully, " +
                        "with the good tape — to the lantern itself. “This year," +
                        "Sparky's stayin' lit if I have to carry the sun.”",
                    short = "🕯️ Butters has a candle system: two mittens and the good tape.",
                    who = "Butters", dayFrom = 2, dayTo = 4),
                FinaleStep("TALK", "The Pickle Jar Lantern",
                    line = "Kenny's household lantern is a pickle jar, washed out and " +
                        "proud, with a candle stub inside that his mom has been " +
                        "saving since before the snow. “(muffled) Mmph rmph.” The " +
                        "stub is maybe an inch tall. It has burned at three " +
                        "birthdays and one power outage, and it has never once gone " +
                        "out early. “(muffled) Mmph.” Kenny is right. Some lights " +
                        "are small because they spend it all on staying.",
                    short = "🕯️ Kenny's lantern: a pickle jar, an inch of candle, an undefeated record.",
                    who = "Kenny", dayFrom = 3, dayTo = 5)
            ),
            choices = listOf(
                FinaleChoice("carry",
                    label = "🕯️ Carry the last lantern yourself — the whole street, the whole way",
                    pickLine = "🕯️ You take the last lantern in both hands. Behind " +
                        "you, one by one, the whole street lights up and follows.",
                    outcome = "You carried the last lantern — the one for whoever's " +
                        "missing someone — the full length of Main Street, and the " +
                        "town walked behind your light in the good kind of quiet.",
                    markText = "The bench at the end of Main Street keeps its lantern " +
                        "hook bright, and the strings stay up a week past the snow — " +
                        "the town voted to leave them, every single household.",
                    winner = "you"),
                FinaleChoice("butters",
                    label = "🕯️ Give the last lantern to Butters — his went out; this one won't",
                    pickLine = "🕯️ You hold out the last lantern. Butters takes it " +
                        "like a relay baton made of starlight, and squares his mittens.",
                    outcome = "You gave the last lantern to Butters — whose own went " +
                        "out at the corner, right on schedule — and he carried the " +
                        "town's light the whole way, relit twice from the mitten " +
                        "candles, never dark for more than a breath.",
                    markText = "In the church window sits a small scorched paper " +
                        "lantern, retired with honors — Butters'. The label under it " +
                        "reads: IT STAYED LIT. (MOSTLY.) (IT COUNTS.)",
                    winner = "Butters")
            ),
            townChoice = 0,
            townOutcome = "With no volunteer, Father Maxi carried the last " +
                "lantern himself, the way he has every year the town needed " +
                "him to — slowly, and like it was nothing.",
            absentOutcome = "The Lantern Walk happened without you: Father Maxi " +
                "carried the last lantern himself, Butters' went out twice and " +
                "came back twice, and the whole street made it home lit.",
            decorId = "finale_lantern",
            ambient = mapOf(
                "Father Maxi" to "Nobody walks in the dark alone. That's the " +
                    "whole liturgy this week.",
                "Butters" to "Two mittens, two candles, one dream. This is my year.",
                "Kenny" to "(muffled) Mmph rmph-mmph. — the jar is ready. The jar is always ready."),
            ambientGeneric = listOf(
                "Lantern week. The street looks like it's holding its breath, in a nice way.",
                "You walking this year? Everybody walks. Even the goths. Especially the goths."))
    )

    fun defFor(season: Season): FinaleDef? = DEFS.firstOrNull { it.season == season }
    fun defById(id: String): FinaleDef? = DEFS.firstOrNull { it.id == id }

    // ---------------- the window (pure calendar math) ----------------

    data class FinaleWindow(
        val def: FinaleDef,
        val instanceYear: Int,
        val dayIndex: Int          // 0 = first day … WINDOW_DAYS-1 = last
    )

    private fun keyOf(y: Int, m: Int, d: Int) = "%04d%02d%02d".format(y, m, d)

    /** The (month, day) a season ENDS on in [year] — for winter,
     *  [year] is the February the season ends in (the season
     *  that began the previous December). */
    fun seasonEndMonthDay(season: Season, year: Int): Pair<Int, Int> =
        when (season) {
            Season.SPRING -> 5 to 31
            Season.SUMMER -> 8 to 31
            Season.FALL -> 11 to 30
            Season.WINTER -> 2 to if (isLeapYear(year)) 29 else 28
        }

    private fun endOrdinal(season: Season, year: Int): Int {
        val (m, d) = seasonEndMonthDay(season, year)
        return ordinalOfKey(keyOf(year, m, d))
    }

    /** Today's finale window, if the calendar is inside one:
     *  the last [WINDOW_DAYS] days of a season, endpoints
     *  included. Winter's instance year is its February's. */
    fun windowAt(state: GameState): FinaleWindow? {
        val today = ordinalOfKey(state.todayKey)
        if (today <= 0) return null
        for (def in DEFS) {
            for (iy in (state.gameYear - 1)..(state.gameYear + 1)) {
                val end = endOrdinal(def.season, iy)
                val start = end - (WINDOW_DAYS - 1)
                if (today in start..end) {
                    return FinaleWindow(def, iy, today - start)
                }
            }
        }
        return null
    }

    /** The finale instance still owed its wrap: an opened one
     *  whose window has ended, or an unopened one whose window
     *  ended exactly yesterday (the blocked-all-week case — an
     *  older unopened instance belongs to a save that predates
     *  finales and is left alone). The latest such instance
     *  wins; windows are months apart, so there's only ever
     *  one candidate in practice. */
    fun pendingWrap(state: GameState): FinaleWindow? {
        val today = ordinalOfKey(state.todayKey)
        if (today <= 0) return null
        var best: FinaleWindow? = null
        var bestEnd = -1
        for (def in DEFS) {
            for (iy in (state.gameYear - 1)..(state.gameYear + 1)) {
                if (isWrapped(state, def, iy)) continue
                val end = endOrdinal(def.season, iy)
                if (end >= today) continue
                val owed = isOpened(state, def, iy) || end == today - 1
                if (owed && end > bestEnd) {
                    bestEnd = end
                    best = FinaleWindow(def, iy, WINDOW_DAYS - 1)
                }
            }
        }
        return best
    }

    // ---------------- instance state (all reads capped) ----------------

    fun isOpened(state: GameState, def: FinaleDef, iy: Int): Boolean =
        (state.life[kStart(def.id, iy)] ?: 0) > 0

    fun isWrapped(state: GameState, def: FinaleDef, iy: Int): Boolean =
        (state.life[kDone(def.id, iy)] ?: 0) > 0

    /** The step mask, clamped to the steps that exist — an
     *  inflated save can't fake beats that aren't in the def. */
    fun mask(state: GameState, def: FinaleDef, iy: Int): Int =
        (state.life[kMask(def.id, iy)] ?: 0) and
            ((1 shl def.steps.size) - 1)

    fun stepsDone(state: GameState, def: FinaleDef, iy: Int): Int =
        Integer.bitCount(mask(state, def, iy))

    /** The locked choice's index, or -1 (capped to the def). */
    fun choiceIndex(state: GameState, def: FinaleDef, iy: Int): Int {
        val raw = (state.life[kChoice(def.id, iy)] ?: 0) - 1
        return if (raw in def.choices.indices) raw else -1
    }

    /** Did the player take part in this instance at all —
     *  one landed beat, or the call itself. */
    fun participated(state: GameState, def: FinaleDef, iy: Int): Boolean =
        mask(state, def, iy) != 0 || choiceIndex(state, def, iy) >= 0

    /** Instances of this finale ever wrapped (any outcome). */
    fun runs(state: GameState, def: FinaleDef): Int =
        (state.life[kRuns(def.id)] ?: 0).coerceIn(0, 999)

    /** Instances the player actually played (decor's gate). */
    fun playedCount(state: GameState, def: FinaleDef): Int =
        (state.life[kPlayed(def.id)] ?: 0).coerceIn(0, 999)

    /** The instance live right now: in its window, opened,
     *  not yet wrapped. Null outside a window (or in the
     *  deferred gap before the opening lands). */
    fun activeWindow(state: GameState): FinaleWindow? {
        val w = windowAt(state) ?: return null
        return if (isOpened(state, w.def, w.instanceYear) &&
            !isWrapped(state, w.def, w.instanceYear)) w else null
    }

    // ---------------- the standing mark ----------------

    /** The mark a past finale left standing, if any: its text,
     *  the year it was set, who history credits, and whether
     *  the call was the player's ("you") or the town's. */
    data class FinaleMark(val text: String, val year: Int,
                          val winner: String, val byYou: Boolean)

    fun markOf(state: GameState, def: FinaleDef): FinaleMark? {
        val text = state.lifeText[kMark(def.id)] ?: return null
        if (text.isEmpty()) return null
        return FinaleMark(
            text = text,
            year = (state.life[kMarkYear(def.id)] ?: 0).coerceIn(0, 9999),
            winner = state.lifeText[kMarkWinner(def.id)] ?: "",
            byYou = state.lifeText[kMarkBy(def.id)] == "you")
    }

    /** The standing mark at a place (the scene Look could
     *  read it; the Journal page always does). */
    fun markAt(state: GameState, locId: String): FinaleMark? =
        DEFS.firstOrNull { it.loc == locId }?.let { markOf(state, it) }

    // ---------------- climax courtesy ----------------

    /** Is any mystery's player-facing climax on the table
     *  right now? Finales open late and call later rather
     *  than stack a second climax on a case's moment. */
    fun climaxPending(state: GameState): Boolean =
        Mystery.finaleChoiceDue(state) ||
            Mystery2.accusationDue(state) ||
            Mystery3.dispositionDue(state) ||
            Mystery4.vigilDue(state) ||
            Mystery5.confrontationDue(state) ||
            Mystery6.meetingDue(state) ||
            Mystery7.restageDue(state) ||
            Mystery8.readingDue(state) ||
            Mystery9.auditDue(state) ||
            Mystery10.hangingDue(state) ||
            Mystery11.voicingDue(state) ||
            Mystery12.returnsDue(state) ||
            Mystery13.verdictDue(state)

    /** The daily episode roll stands down for a finale
     *  window — the finale IS the week's episode. (Mystery
     *  chapters still take the slot first; this only mutes
     *  the regular roll.) */
    fun suppressesDailyRoll(state: GameState): Boolean =
        windowAt(state) != null

    // ---------------- lifecycle (the Engine's rollover calls) ----------------

    /** The morning bookkeeping: wrap the finale whose week
     *  just ended, then open today's if the calendar is in a
     *  window and no case climax is pending. Called by
     *  Engine.onNewDay after the calendar has moved. */
    fun dailyRollover(state: GameState) {
        pendingWrap(state)?.let { wrapInstance(state, it.def, it.instanceYear) }
        val w = windowAt(state) ?: return
        if (isWrapped(state, w.def, w.instanceYear) ||
            isOpened(state, w.def, w.instanceYear)) return
        // a case's climax is on the table this morning — the
        // finale waits a day rather than stack its title card
        // on the moment. Its beats' day ranges have slack for
        // exactly this.
        if (climaxPending(state)) return
        openInstance(state, w)
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** The opening morning: the episode begins, the town is
     *  told, and last year's mark — if one is standing — is
     *  acknowledged before this year's is earned. */
    private fun openInstance(state: GameState, w: FinaleWindow) {
        val def = w.def
        state.life[kStart(def.id, w.instanceYear)] = state.absDay + 1
        Episodes.beginFinale(state, def)
        val locName = LOCATIONS[def.loc]?.name ?: def.loc
        note(state, "${def.emoji} SEASON FINALE: '${def.title}' — " +
            "${def.seasonName}'s last week belongs to $locName.")
        townLogLine(state, "${def.emoji} ${def.title} (${def.seasonName} finale) — " +
            "${state.gameMonth}/${state.gameDay}")
        markOf(state, def)?.let { mark ->
            if (mark.year != w.instanceYear) {
                note(state, "${def.emoji} Last ${def.seasonName.lowercase()}'s " +
                    "mark still stands at $locName: ${mark.text} " +
                    "(${if (mark.byYou) "your call" else "the town's call"}, " +
                    "${mark.year}.)")
            }
        }
    }

    /** The morning after the window: the finale resolves.
     *  The player's call if they made one; the town's call if
     *  they played but didn't call; the absent ending if
     *  they never showed. The standing mark is replaced,
     *  the year's instance closes forever, and the credits
     *  roll on its episode. */
    private fun wrapInstance(state: GameState, def: FinaleDef, iy: Int) {
        if (isWrapped(state, def, iy)) return
        state.life[kDone(def.id, iy)] = 1
        val opened = isOpened(state, def, iy)
        val played = opened && participated(state, def, iy)
        val choiceIdx = if (opened) choiceIndex(state, def, iy) else -1
        val branch: String
        val outcome: String
        val markText: String
        val winner: String
        val byYou: Boolean
        when {
            choiceIdx >= 0 -> {
                val c = def.choices[choiceIdx]
                branch = "choice:${c.key}"
                outcome = c.outcome
                markText = c.markText
                winner = c.winner
                byYou = true
            }
            played -> {
                val c = def.choices[def.townChoice]
                branch = "town"
                outcome = def.townOutcome
                markText = c.markText
                winner = c.winner
                byYou = false
            }
            else -> {
                val c = def.choices[def.townChoice]
                branch = "absent"
                outcome = def.absentOutcome
                markText = c.markText
                winner = c.winner
                byYou = false
            }
        }
        // the standing mark: this year's call replaces last
        // year's, wherever the week was played or not — the
        // town's story moves on either way.
        state.lifeText[kMark(def.id)] = markText
        state.life[kMarkYear(def.id)] = iy
        state.lifeText[kMarkBy(def.id)] = if (byYou) "you" else "town"
        state.lifeText[kMarkWinner(def.id)] = winner
        state.life[kRuns(def.id)] = runs(state, def) + 1
        val landed = if (opened) stepsDone(state, def, iy) else 0
        if (played) {
            state.life[kPlayed(def.id)] = playedCount(state, def) + 1
            // friendship across the ensemble — the host a
            // little more, as hosts are owed.
            val eng = Engine()
            eng.relAdjust(state, def.host, 3)
            for (name in def.ensemble) {
                if (name != def.host) eng.relAdjust(state, name, 2)
            }
            if (winner in state.friendship) eng.relAdjust(state, winner, 2)
        }
        note(state, "${def.emoji} '${def.title}' — $outcome")
        townLogLine(state, "${def.emoji} ${def.title} (${def.seasonName} finale, $iy) — $outcome")
        if (opened) {
            Episodes.finishFinale(state, def, landed, outcome,
                markText, branch)
        }
    }

    // ---------------- the beats: eligibility & judges ----------------

    /** The beat a verb could land right now, or null: the
     *  first unlanded beat whose earlier beats have all
     *  landed or aged out, inside its own day range. */
    fun currentStepIndex(state: GameState, def: FinaleDef, iy: Int,
                         dayIndex: Int): Int? {
        val m = mask(state, def, iy)
        for (idx in def.steps.indices) {
            if (m and (1 shl idx) != 0) continue
            val s = def.steps[idx]
            if (dayIndex > s.dayTo) continue   // aged out — the show goes on
            return if (dayIndex >= s.dayFrom) idx else null
        }
        return null
    }

    private fun fire(state: GameState, w: FinaleWindow, idx: Int): FinaleHit {
        val def = w.def
        val key = kMask(def.id, w.instanceYear)
        state.life[key] = mask(state, def, w.instanceYear) or (1 shl idx)
        val step = def.steps[idx]
        note(state, step.short)
        // the person you played the beat with feels it, at
        // errand scale — the ensemble spread waits for the wrap.
        step.who?.let { Engine().relAdjust(state, it, 2) }
        // every beat caught that can still be caught: the
        // rest have all landed or aged out of their ranges.
        val m = mask(state, def, w.instanceYear)
        val allLanded = def.steps.indices.all {
            m and (1 shl it) != 0 || def.steps[it].dayTo < w.dayIndex
        }
        return FinaleHit(def, idx, step.line, allLanded)
    }

    /** TALK query half: the beat index a conversation with
     *  [npc] would land right now, or null. Pure. */
    fun talkStepFor(state: GameState, npc: String): Int? {
        val w = activeWindow(state) ?: return null
        val idx = currentStepIndex(state, w.def, w.instanceYear,
            w.dayIndex) ?: return null
        val s = w.def.steps[idx]
        if (s.verb != "TALK" || s.who != npc) return null
        if (s.loc != null && state.agents[npc]?.location != s.loc) return null
        return idx
    }

    /** TALK judged (the Talk gate calls this, like a beat). */
    fun judgeTalk(state: GameState, npc: String): FinaleHit? {
        val w = activeWindow(state) ?: return null
        val idx = talkStepFor(state, npc) ?: return null
        return fire(state, w, idx)
    }

    /** LOOK judged: the beat a careful Look at [locId]
     *  lands, spoken in the Look text — or null. */
    fun lookText(state: GameState, locId: String): String? {
        val w = activeWindow(state) ?: return null
        val idx = currentStepIndex(state, w.def, w.instanceYear,
            w.dayIndex) ?: return null
        val s = w.def.steps[idx]
        if (s.verb != "LOOK" || s.loc != locId) return null
        val hit = fire(state, w, idx)
        return hit.text
    }

    /** PHOTO judged: a shutter at the finale's place lands
     *  its portrait beat (Photos.applyPhoto calls this, like
     *  the beats before it). Silent — the polaroid is the
     *  announcement; the ticker carries the short line. */
    fun onPhoto(state: GameState, rec: PhotoRecord) {
        val w = activeWindow(state) ?: return
        val idx = currentStepIndex(state, w.def, w.instanceYear,
            w.dayIndex) ?: return
        val s = w.def.steps[idx]
        if (s.verb != "PHOTO" || s.loc != rec.locId) return
        fire(state, w, idx)
    }

    // ---------------- the closing call ----------------

    /** The call is on the table: the finale's last day, the
     *  player took part in the week, and no call is locked.
     *  (The Activity additionally defers while a case climax
     *  is due — two climaxes never share a morning.) */
    fun choiceDue(state: GameState): Boolean {
        val w = activeWindow(state) ?: return false
        if (w.dayIndex != WINDOW_DAYS - 1) return false
        if (choiceIndex(state, w.def, w.instanceYear) >= 0) return false
        return participated(state, w.def, w.instanceYear)
    }

    /** The live window's def, when the call is due. */
    fun choiceDef(state: GameState): FinaleDef? =
        if (choiceDue(state)) activeWindow(state)?.def else null

    /** Lock the player's call in. */
    fun choose(state: GameState, key: String): Boolean {
        val w = activeWindow(state) ?: return false
        val idx = w.def.choices.indexOfFirst { it.key == key }
        if (idx < 0) return false
        if (!choiceDue(state)) return false
        state.life[kChoice(w.def.id, w.instanceYear)] = idx + 1
        note(state, w.def.choices[idx].pickLine)
        return true
    }

    // ---------------- the gathering (schedule override) ----------------

    /** On the opening and closing days of an opened finale,
     *  its ensemble is AT the finale's place for the day —
     *  the Engine's agenda lookup asks here, after the
     *  living transforms and before the shenanigan stage
     *  transform (an event's causers still answer their own
     *  call first). */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in GATHER_FROM until GATHER_TO) return null
        val w = activeWindow(state) ?: return null
        if (w.dayIndex != 0 && w.dayIndex != WINDOW_DAYS - 1) return null
        if (npcName !in w.def.ensemble) return null
        return AgendaBlock(w.def.loc, w.def.gatherActivity)
    }

    // ---------------- conversation & notices ----------------

    /** A finale reference in conversation during its week:
     *  an ensemble member with something to say says it,
     *  once a day each; anyone else in the ensemble may drop
     *  a generic line, once a day. */
    fun ambientLine(state: GameState, npc: String): Line? {
        val w = activeWindow(state) ?: return null
        val def = w.def
        if (npc !in def.ensemble) return null
        val specific = def.ambient[npc]
        if (specific != null) {
            if (state.life["finAmb:$npc"] == state.absDay) return null
            state.life["finAmb:$npc"] = state.absDay
            return Line(npc, specific)
        }
        if (def.ambientGeneric.isNotEmpty() &&
            state.life["finAmbDay"] != state.absDay) {
            state.life["finAmbDay"] = state.absDay
            val pick = (state.absDay +
                (npc.hashCode() and 0x7fffffff)) % def.ambientGeneric.size
            return Line(npc, def.ambientGeneric[pick])
        }
        return null
    }

    /** The morning report's line: the opening morning names
     *  the finale (the morning it actually opened, deferrals
     *  included); the closing morning makes the summons. */
    fun morningNote(state: GameState): String? {
        val w = windowAt(state) ?: return null
        val def = w.def
        if (!isOpened(state, def, w.instanceYear) ||
            isWrapped(state, def, w.instanceYear)) return null
        val locName = LOCATIONS[def.loc]?.name ?: def.loc
        if (w.dayIndex == WINDOW_DAYS - 1) {
            return "${def.emoji} ${def.seasonName} finale, last day: " +
                def.closingCall
        }
        if ((state.life[kStart(def.id, w.instanceYear)] ?: 0) ==
            state.absDay + 1) {
            return "${def.emoji} ${def.seasonName} finale week opens: " +
                "“${def.title}” at $locName. ${def.intro}"
        }
        return null
    }

    // ---------------- the Journal page ----------------

    /** The ❄ SEASON FINALES page: the live week's progress,
     *  and each season's standing mark with the year and the
     *  hand that made the call. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val marked = DEFS.count { markOf(state, it) != null }
        val playedTotal = DEFS.sumOf { runs(state, it) }
        out += "❄ SEASON FINALES — " +
            if (playedTotal == 0) "every season in this town ends with " +
                "one big town moment, in its last week"
            else "$playedTotal finale${if (playedTotal == 1) "" else "s"} " +
                "played out, $marked of ${DEFS.size} season marks standing"
        val live = windowAt(state)
        for (def in DEFS) {
            val locName = LOCATIONS[def.loc]?.name ?: def.loc
            val mark = markOf(state, def)
            val years = runs(state, def)
            when {
                live != null && live.def.id == def.id -> {
                    val n = stepsDone(state, def, live.instanceYear)
                    val total = def.steps.size
                    out += "▶ ${def.seasonName} — “${def.title}” is on at " +
                        "$locName (day ${live.dayIndex + 1} of $WINDOW_DAYS): " +
                        "$n of $total beats caught. ${def.hint}"
                    if (mark != null && mark.year != live.instanceYear) {
                        out += "· last year's mark: ${mark.text} " +
                            "(${mark.year}, ${if (mark.byYou) "your call" else "the town's"})"
                    }
                }
                mark != null -> {
                    val yearsNote = if (years > 1)
                        "; played $years years running" else ""
                    out += "✓ ${def.seasonName} — “${def.title}” " +
                        "(${def.windowLabel}): ${mark.text} " +
                        "(set ${mark.year}, " +
                        "${if (mark.byYou) "your call" else "the town's call"}" +
                        "$yearsNote)"
                }
                else ->
                    out += "· ${def.seasonName} — “${def.title}” plays in " +
                        "${def.windowLabel} at $locName. ${def.intro}"
            }
        }
        return out
    }
}

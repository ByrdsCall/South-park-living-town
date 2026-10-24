package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery4 — the fourth case (v0.6.0c, Queue #6 #3):
// "The Night Gardener".
//
// Where the pond case (Mystery.kt) was witness-driven, the
// booth case (Mystery2.kt) a compressed whodunit, and the
// buried case (Mystery3.kt) an object-driven excavation,
// this one follows acts of anonymous maintenance: things
// around town are being quietly FIXED in the night — a
// wobbly bench made solid, a dead fountain running, planters
// blooming that were sticks for two years. No damage, no
// note, no credit taken. The town is delighted, nosy, and
// determined to say thank you at somebody. There may not be
// a somebody who can survive being thanked.
//
// The solution, in one breath: the fixer is DogPoo — the
// quiet kid nobody notices. He sits in the back row at the
// monthly town meetings at City Hall (dragged along, silent,
// invisible), hears every complaint the town makes, tends
// the Elementary playground garden — the chore nobody else
// wanted — and at night he fixes what the town complained
// about, with the garden's red twine, the hall's own paint
// and parts, a small ladder and a watering can. Nobody ever
// writes down who'll fix things, so he just does. Public
// gratitude would end him; the fixing only works unobserved.
// The tell is a deduction, not a sighting: the planters
// answer a complaint Sheila voiced aloud AFTER Gerald had
// closed the minute book — never minuted, never posted —
// so the Gardener was IN THE ROOM, after the gavel; and the
// room's back row, the garden's twine and marigolds, a
// small thumbprint in wet paint, and a trail of potting soil
// running to one back-row chair all point one way. (This
// paragraph is the ONLY place outside the finale's own
// payoff text where the shape of it is written down; public
// surfaces — the board, the chapters, the changelog — carry
// the premise and never the answer.)
//
// Structure:
//  - CHAPTERS4: four full ShenaniganDefs on the same episode
//    machinery, outside the random pool, resolving in
//    SHENANIGAN_BY_ID like every case before them.
//  - Gating: the case queues into the mystery slot TEN days
//    after the buried case retires. While any earlier case
//    is open this one does not even schedule — the slot has
//    one case at a time, always.
//  - The fixes are REAL dressing: each chapter opens after
//    a new overnight fix appears (FIX_SITES' tokens, drawn
//    by SceneArt via TownInfo) — and unlike the dig sites,
//    a fix STAYS fixed forever. The case improves the town
//    permanently, one spot at a time.
//  - Clues: 12, in the kinds the earlier arcs underused —
//    2 fix-list PATTERN (one of them the spine: an un-minuted
//    complaint, answered anyway), 2 MATERIALS (town stores,
//    garden strain), 2 CRAFT (one careful hand, one careless
//    one — the red herring clears himself by his own nail),
//    4 WITNESS (the room, the patrol, the teacher, the
//    night), 2 PARTICIPATION.
//  - The finale is a VIGIL, not an accusation: on the night
//    the pattern says the Gardener will come, the player
//    waits at the spot — after dark, at the climax, holding
//    at least VIGIL_MIN_CLUES of the case. What they learn
//    is the truth. The choice is about GRATITUDE: name them
//    (the ceremony happens, the fixes stop, one last fix
//    lands somewhere personal), guard the secret (the fixes
//    keep coming, a few a year, forever), or join in (a
//    two-hands tradition starts). No vigil at all and the
//    case closes COLD: one winter the fixes simply stop,
//    and nobody ever knows why.
//  - Retirement: every landing retires the arc permanently.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery4 {

    const val CASE_TITLE = "The Night Gardener"
    const val FINALE4_ID = "arc4_ch4_vigil"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the buried case retires
    const val VIGIL_MIN_CLUES = 8
    /** The vigil is an after-dark beat: the choice opens at
     *  dusk (17:00), inside the finale's climax, never at noon. */
    const val VIGIL_FROM_MIN = 17 * 60
    const val LAST_FIX_DELAY_DAYS = 1

    /** The fixer — code and payoff text only. Nothing the
     *  player can read before the vigil names them. */
    const val CULPRIT = "DogPoo"

    /** The three ways gratitude can land, and the landing a
     *  vigil never kept falls to. */
    val CHOICE_KEYS = listOf("name", "guard", "join")
    const val DEFAULT_BRANCH = "cold"
    val BRANCH_KEYS = listOf("name", "guard", "join", "cold")

    // ---------------- the four chapters ----------------

    val CHAPTERS4: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc4_ch1_bench",
            title = "The Mended Bench",
            cause = "The bench by the sandbox at Kid's Park has wobbled since " +
                "roughly the dawn of time. Yesterday it wobbled. This morning " +
                "it does not. It does not so much as creak — and its missing " +
                "bolts are sitting beside it in a neat little pile, washed, " +
                "in size order, like a tip.",
            blurb = "Somebody fixed the bench in the night. Carefully. " +
                "Anonymously. The town has questions, and all of them are " +
                "'who do we thank?'",
            theme = QuestTheme("🌱", "#7ab86a", "THE NIGHT GARDENER"),
            tintHex = "#1e2c1c",
            stages = listOf(
                ShenaniganStage("Kickoff: Rock Solid",
                    "🌱 THE MENDED BENCH: The wobbliest bench in South Park " +
                        "is, as of this morning, the most solid object in it.",
                    "Somebody worked here in the dark and did it properly: " +
                        "joints re-seated, wood sealed, the loose bolts washed " +
                        "and lined up in size order on the seat. The joints " +
                        "are lashed with red twine, tied in a bow.",
                    "See the bench at Kid's Park.",
                    routine = "cluster", clusterLoc = "KidsPark",
                    clusterActivity = "Sitting on the bench. Bouncing, a little. Marvelling.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents approve of the bench and intend to approve at whoever did it."))),
                ShenaniganStage("Escalation: The Bolts, Arranged",
                    "🌱 Whoever mended the bench laid its old bolts out in " +
                        "size order and washed them first. In the night. " +
                        "For a bench.",
                    "It is the tidiness that gets people. A vandal breaks " +
                        "things at night. A contractor fixes them on invoice. " +
                        "This was a third thing: somebody who heard the town " +
                        "complain, and quietly handled it.",
                    "Look at the arranged bolts.",
                    routine = "cluster", clusterLoc = "KidsPark",
                    clusterActivity = "Studying the bolts. Arranging theories.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew has opened an investigation. The suspect: kindness itself."))),
                ShenaniganStage("Climax: Better Than the Contractor",
                    "🌱 CLIMAX: The town's daytime crew has inspected the " +
                        "bench, and their verdict is professional respect " +
                        "bordering on professional jealousy.",
                    "You held the bench steady for the inspection. Underneath, " +
                        "out of the weather: the twine bow, the trimmed ends — " +
                        "and somebody's careful hand has been everywhere on " +
                        "this wood. Whoever they are, they are GOOD at this.",
                    "Hold the bench at Kid's Park. Look underneath.",
                    routine = "cluster", clusterLoc = "KidsPark",
                    clusterActivity = "Crawling under the bench. Taking notes. Getting splinters, ironically.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The Staff note the workmanship. Good work is getting rare; somebody's hoarding it."))),
                ShenaniganStage("Resolution: Who Fixes a Bench?",
                    "🌱 The Mayor has announced the town will get to the " +
                        "bottom of this kindness. A thank-you ceremony has " +
                        "been mentioned. Twice.",
                    "Here's the thing the town keeps circling: at last " +
                        "month's town meeting, somebody complained about " +
                        "exactly this bench. It's in the posted minutes, " +
                        "word for word. And now the bench is fixed. Funny " +
                        "timing. If you can call it timing.",
                    "Read the posted minutes at City Hall.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Barbrady"),
            episodeTitles = listOf("Rock Solid", "The Bolts, Arranged"),
            coldOpen = listOf(
                Line("McDaniels", "A fixed bench, no invoice, no name. This " +
                    "town will find its benefactor and it will THANK them, " +
                    "publicly, with a microphone. See if it doesn't."),
                Line("Barbrady", "I jiggled that bench on patrol for six " +
                    "years. Six years it jiggled back. Whatever happened " +
                    "last night, it wasn't a jiggle."))),
        ShenaniganDef(
            id = "arc4_ch2_fountain",
            title = "The Fountain Runs",
            cause = "The Main Street fountain has been dry since anyone can " +
                "remember — a stone bowl the pigeons used as a condo. This " +
                "morning it runs clear and cold, the pigeons are furious, " +
                "and there is not one single witness, again.",
            blurb = "Second fix: the fountain. The parts are the town's own. " +
                "The hands are still nobody's. The patrolman has a description " +
                "that is almost a description.",
            theme = QuestTheme("⛲", "#6aa8c8", "THE FOUNTAIN RUNS"),
            tintHex = "#1c262e",
            stages = listOf(
                ShenaniganStage("Kickoff: Clear and Cold",
                    "⛲ THE FOUNTAIN RUNS: South Park's driest landmark is, " +
                        "as of dawn, extremely wet.",
                    "New washer, re-seated valve, basin scrubbed. The water " +
                        "runs the way water runs when somebody who knows " +
                        "plumbing has been at it in the dark, for free, for " +
                        "no reason the town can invoice.",
                    "See the fountain on Main Street.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Watching the fountain run. Throwing in pennies. Making wishes at it.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew is staking out the fountain. The fountain is innocent and so, probably, is everyone."))),
                ShenaniganStage("Escalation: The Town's Own Parts",
                    "⛲ The fountain's new washer carries a tiny stencil: " +
                        "PROPERTY OF SOUTH PARK. It came out of the town's " +
                        "own parts bin. So did the hands, apparently.",
                    "The parts bin lives in the City Hall basement, past " +
                        "the folding chairs, where everyone at the town " +
                        "meetings files by. Whoever fixed the fountain " +
                        "didn't buy a thing. They used the town's own stores, " +
                        "like somebody who considers the town theirs to mend.",
                    "Check the parts bin at City Hall.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Reading the stencil. Debating whose hands know the basement.",
                    tone = "hype", litterBurst = 1),
                ShenaniganStage("Climax: The Patrolman's Description",
                    "⛲ CLIMAX: Officer Barbrady has nearly apprehended the " +
                        "Gardener twice, and his description of them is " +
                        "almost a description.",
                    "Small. Quick. Polite. Ladder-ish. South Park's night " +
                        "watch has SEEN the fixer, is the thing — the way " +
                        "you see weather. And last month, come to think of " +
                        "it, the pond gate got re-hung in the night too, " +
                        "and nobody connected it until now.",
                    "Hear the patrol report on Main Street.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Giving statements. Being nearly witnesses.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents have started a gratitude rota for a person they cannot find."))),
                ShenaniganStage("Resolution: A Pattern of Repairs",
                    "⛲ A bench. A fountain. A pond gate, last month, " +
                        "noticed late. Every one of them something the town " +
                        "had complained about, out loud, in public.",
                    "The whole town is doing the same " +
                        "arithmetic now: complaints go in at the meetings, " +
                        "and fixes come out in the night. The gates of the " +
                        "pond wear the same red twine as the bench. Patterns " +
                        "are just promises that haven't been kept yet.",
                    "Look at the pond gate. Rest your eyes.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Gerald", "Jimbo"),
            episodeTitles = listOf("Clear and Cold", "The Town's Own Parts"),
            coldOpen = listOf(
                Line("Gerald", "I take the minutes at the town meetings. " +
                    "Every complaint, in my book, in my hand. Lately I read " +
                    "them and think: careful, Gerald. You're writing a " +
                    "shopping list for a ghost."),
                Line("Jimbo", "Town's got a night-shift saint with a toolbox. " +
                    "Ned and me, we respect it. A person fixes your fountain " +
                    "for free, you don't interrogate the fountain."))),
        ShenaniganDef(
            id = "arc4_ch3_planters",
            title = "The Planters Bloom",
            cause = "The planters flanking City Hall have been dead sticks " +
                "for two years — a standing joke, a civic embarrassment, a " +
                "line item of shame. This morning they are in full, deep, " +
                "defiant marigold bloom, and the soil is still damp.",
            blurb = "Third fix: the planters. This one answers a complaint " +
                "that was never written down. Gerald has gone very quiet " +
                "over his minute book.",
            theme = QuestTheme("🌼", "#e8a83a", "THE PLANTERS BLOOM"),
            tintHex = "#2c2416",
            stages = listOf(
                ShenaniganStage("Kickoff: In Defiance of the Record",
                    "🌼 THE PLANTERS BLOOM: City Hall's dead planters are, " +
                        "as of dawn, extremely alive.",
                    "Marigolds, deep orange, planted close and watered in. " +
                        "The boxes are staked with red twine. The town " +
                        "photographed them like a celebrity had landed. " +
                        "Somebody planted flowers in front of the mayor's " +
                        "own windows and left before the applause.",
                    "See the planters at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Admiring the planters. Posing by the planters. Suspecting the planters.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("girls_circle", influenceDelta = 2,
                            note = "The Circle approves of the planters and of whoever has this much nerve."))),
                ShenaniganStage("Escalation: A Special Meeting",
                    "🌼 A SPECIAL MEETING has been called: the town will " +
                        "discuss its benefactor, and this time the minutes " +
                        "will be VERY thorough.",
                    "Everyone talks at once, which is the meeting way. The " +
                        "complaints get read aloud — the bench, the fountain, " +
                        "the gate — and one by one the town checks them off " +
                        "against the fixes, like a ledger being balanced by " +
                        "a ghost.",
                    "Attend the special meeting at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Meeting about the Gardener. Folding chairs. Strong opinions.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 3,
                            note = "The Parents are balancing the ledger: every complaint, answered. Somebody loves this town out loud-quiet."))),
                ShenaniganStage("Climax: The Minute Book",
                    "🌼 CLIMAX: Gerald has opened his minute book on the " +
                        "table, and he is looking at it the way you look at " +
                        "a door you don't remember locking.",
                    "The planters were complained about in THIS room — " +
                        "standing up, after the gavel, after the book was " +
                        "closed. It never got written. It never got posted. " +
                        "And yet: marigolds. You helped stack the chairs " +
                        "after. The floor remembers things too.",
                    "Help stack the chairs at City Hall. Mind the floor.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Stacking chairs. Reading the minute book over a shoulder.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: One Complaint Left",
                    "🌼 The meeting adjourns with the ledger nearly balanced. " +
                        "One complaint from the last meeting is still " +
                        "outstanding. One. The whole town knows which.",
                    "The flagpole rope at the Elementary. 'Frayed to " +
                        "threads,' the minutes say. 'One good wind and the " +
                        "flag's in the pond.' The minutes went up this " +
                        "morning. If the pattern holds — and the pattern " +
                        "has done nothing but hold — the Gardener comes " +
                        "for the rope on a night like tonight.",
                    "Read the minutes. Look at the flagpole. Wait for dark.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Sheila", "McDaniels"),
            episodeTitles = listOf("In Defiance of the Record", "The Minute Book"),
            coldOpen = listOf(
                Line("Sheila", "I SAID it. In that room, out loud: those " +
                    "dead planters make us look like a town that gave up. " +
                    "And three mornings later — marigolds. Gerald says it " +
                    "was never in his minutes. So who was LISTENING, Gerald?"),
                Line("McDaniels", "The ceremony is drafted. The plaque is " +
                    "drafted. All the town lacks is a NAME to engrave, and " +
                    "this town will have it by the equinox, see if it doesn't."))),
        ShenaniganDef(
            id = FINALE4_ID,
            title = "The Vigil",
            cause = "One complaint left from the last meeting: the flagpole " +
                "rope at the Elementary, frayed to threads. The minutes went " +
                "up this morning. If the pattern holds, the Night Gardener " +
                "comes tonight — and tonight, for the first time, somebody " +
                "will be there. Quiet. In the dark. Being the town's eyes.",
            blurb = "The last spot on the list. The last night of the pattern. " +
                "What you do with the truth is the whole case.",
            theme = QuestTheme("🌙", "#e8c53a", "THE VIGIL"),
            tintHex = "#141c26",
            stages = listOf(
                ShenaniganStage("Kickoff: The Minutes Are Posted",
                    "🌙 THE VIGIL: The minutes are up, the complaint is on " +
                        "the board, and the whole town is pretending very " +
                        "hard not to watch the Elementary tonight.",
                    "The rope hangs frayed in the morning light, an honest " +
                        "piece of neglect with the whole town's signature " +
                        "on it. By dark it will either still be frayed — or " +
                        "the pattern will keep its promise one last time.",
                    "Walk past the flagpole at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Not watching the flagpole. Watching the flagpole.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew has taken positions. The Crew's positions are terrible. The Crew is not the one keeping the vigil."))),
                ShenaniganStage("Escalation: Dusk on the Flagpole",
                    "🌙 Dusk comes down on the schoolyard. The street empties. " +
                        "The rope ticks against the pole in the wind, " +
                        "thread by thread.",
                    "Everyone who knows is home, or pretending to be. The " +
                        "town has collectively decided that tonight belongs " +
                        "to whoever the dark brings — and to whoever is " +
                        "quiet enough to meet them in it.",
                    "Be at the Elementary as the dark comes down.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Waiting in the schoolyard as the light goes.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: Someone Small, Crossing the Street",
                    "🌙 CLIMAX: Lamplight. A watering can. A ladder carried " +
                        "the careful way, both hands. Someone small crosses " +
                        "the street toward the flagpole — and stops. Seeing " +
                        "you. Seeing that you see.",
                    "The whole case narrows to one held breath in a dark " +
                        "schoolyard: a kid the town has looked past a " +
                        "thousand times, holding the town together with " +
                        "twine and spare hours. What gratitude owes them " +
                        "is yours to decide. The dark is waiting on you.",
                    "Be there. Be quiet. Decide what kindness is owed.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Standing in the dark schoolyard. Holding a breath for the whole town.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: By Morning",
                    "🌙 By morning, the rope will have its answer — the " +
                        "town's, the dark's, or two pairs of hands'. The " +
                        "flag either flies or it doesn't. Either way, the " +
                        "town will never complain quite the same way again.",
                    "Whatever happens in the schoolyard tonight, the " +
                        "meetings will be quieter for a while. People have " +
                        "started listening to their own complaints like " +
                        "somebody out there is taking notes. Somebody is. " +
                        "Somebody always was.",
                    "See what the morning makes of it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("McDaniels", "Father Maxi"),
            episodeTitles = listOf("The Minutes Are Posted", "Someone Small, Crossing the Street"),
            coldOpen = listOf(
                Line("Father Maxi", "All these years the town prayed for " +
                    "someone to look after it. Turns out the looking-after " +
                    "arrives at 2 a.m. with a watering can. Humbling, " +
                    "isn't it, how the help never looks like help."),
                Line("McDaniels", "Tonight the town finds out. Tonight the " +
                    "town says thank you. I have a SPEECH. I have had a " +
                    "speech for a MONTH.")))
    )

    val CHAPTER4_BY_ID: Map<String, ShenaniganDef> = CHAPTERS4.associateBy { it.id }

    /** 0-based chapter index of a def id, or -1 when it's not ours. */
    fun chapterIndexOf4(defId: String?): Int =
        CHAPTERS4.indexOfFirst { it.id == defId }

    fun isChapter4(defId: String?): Boolean = chapterIndexOf4(defId) >= 0

    // ---------------- the fix sites ----------------

    data class FixSite(
        val id: String,
        /** 0-based chapter whose opening follows this fix. */
        val chapter: Int,
        val locId: String,
        val name: String,
        val tokens: List<String>,
        /** The sentence a Look reports once the fix stands. */
        val fixedText: String
    )

    /** The fixes the case itself leaves standing — they appear
     *  as their chapters open and, unlike the dig sites of the
     *  buried case, they NEVER grass over. The case retires;
     *  the town keeps the repairs. */
    val FIX_SITES: List<FixSite> = listOf(
        FixSite("bench", 0, "KidsPark", "The park bench",
            listOf("gard-bench"),
            "The park bench sits rock-solid, its joints lashed with " +
                "red twine in a neat double bow. Somebody mended it in " +
                "the night, and it has stayed mended."),
        FixSite("fountain", 1, "MainStreet", "The Main Street fountain",
            listOf("gard-fountain"),
            "The fountain runs clear and cold, the way it must have " +
                "run a hundred years ago. Somebody fixed it in the " +
                "night, with the town's own parts, and it has stayed fixed."),
        FixSite("gate", 1, "StarksPond", "The pond gate",
            listOf("gard-gate"),
            "The pond gate hangs true on re-seated hinges, its latch " +
                "bound with the red twine bow. Last month's fix — the " +
                "one the town only connected once the pattern formed."),
        FixSite("planters", 2, "CityHall", "The City Hall planters",
            listOf("gard-planters"),
            "The City Hall planters are in full marigold bloom, staked " +
                "with red twine, the soil kept damp by somebody's " +
                "watering can. They have stayed blooming."),
        FixSite("rope", 3, "Elementary", "The flagpole rope",
            listOf("gard-rope"),
            "The flagpole rope is sound and new, and the flag flies " +
                "straight over the schoolyard. It was frayed to threads " +
                "for a year. It is not frayed anymore.")
    )

    val FIX_SITE_BY_ID: Map<String, FixSite> = FIX_SITES.associateBy { it.id }

    /** The rope's fix is the vigil's own work — its token only
     *  stands once the case has closed on a kept vigil. */
    const val ROPE_TOKEN = "gard-rope"
    const val ROPE_LOC = "Elementary"

    /** The last fix of the NAME landing: one morning after the
     *  ceremony, the player's own porch step is mended, with a
     *  potted marigold beside it. Drawn by the same SceneArt
     *  pass (`// fix: gard-porch`). */
    const val PORCH_TOKEN = "gard-porch"
    const val PORCH_LOC = "YourHouse"

    /** The fixes the endings keep dealing, forever-ish: after
     *  the secret is guarded, the Gardener keeps working; after
     *  the player joins in, both hands do. Each entry stands
     *  from (retiredDay + dayOffset) on. Smooth shapes only,
     *  like all dressing in this town. */
    data class ExtraFix(
        val token: String,
        val locId: String,
        val branch: String,       // "guard" | "join"
        val dayOffset: Int,
        val label: String
    )

    val EXTRA_FIXES: List<ExtraFix> = listOf(
        ExtraFix("gard-sign", "BusStop", "guard", 30,
            "the leaning bus-stop sign, standing straight"),
        ExtraFix("gard-swing", "KidsPark", "guard", 60,
            "the squeaky swing, re-chained and silent"),
        ExtraFix("gard-railing", "Church", "guard", 105,
            "the church steps railing, tight as a drum"),
        ExtraFix("gard-joined-bench", "SoDoSoPa", "join", 21,
            "a two-hands bench — twine bow and blue ribbon"),
        ExtraFix("gard-joined-planter", "Mall", "join", 55,
            "a two-hands planter at the Mall"),
        ExtraFix("gard-joined-fence", "TegridyFarms", "join", 95,
            "a two-hands fence line at the farm")
    )

    val FIX_TOKENS: Set<String> =
        FIX_SITES.flatMap { it.tokens }.toSet() + PORCH_TOKEN +
            EXTRA_FIXES.map { it.token }.toSet()

    /** Is this site's fix standing in the world right now?
     *  Case fixes stand from the morning their chapter opens
     *  (the rope is the exception — the vigil's night decides
     *  it), extra fixes stand on their branch's schedule, and
     *  nothing about a standing fix ever comes down again. */
    fun siteStanding(state: GameState, site: FixSite): Boolean {
        if (site.id == "rope") {
            return state.arc4Retired && state.arc4Branch in
                listOf("name", "guard", "join")
        }
        if (!state.arc4Started && !state.arc4Retired) return false
        return state.arc4Chapter > site.chapter || state.arc4Retired
    }

    /** The extra fixes standing right now (guard/join tails). */
    fun extraFixesStanding(state: GameState): List<ExtraFix> {
        if (!state.arc4Retired) return emptyList()
        return EXTRA_FIXES.filter {
            it.branch == state.arc4Branch &&
                state.arc4RetiredDay >= 0 &&
                state.absDay >= state.arc4RetiredDay + it.dayOffset
        }
    }

    /** The fix dressing live right now, by location — what
     *  TownInfo carries into SceneArt. */
    fun fixTokensByLoc(state: GameState): Map<String, List<String>> {
        val out = linkedMapOf<String, MutableList<String>>()
        fun add(locId: String, token: String) {
            out.getOrPut(locId) { mutableListOf() }.add(token)
        }
        for (site in FIX_SITES) {
            if (siteStanding(state, site)) {
                for (t in site.tokens) add(site.locId, t)
            }
        }
        if (state.arc4LastFixLanded) add(PORCH_LOC, PORCH_TOKEN)
        for (fx in extraFixesStanding(state)) add(fx.locId, fx.token)
        return out
    }

    /** The Look-side of a standing fix: a location's repairs
     *  read as part of the place now, permanently. Before the
     *  vigil, the watched spot reads as what it is: frayed,
     *  outstanding, and about to be visited. */
    fun fixLookText(state: GameState, locId: String): String? {
        val parts = mutableListOf<String>()
        for (site in FIX_SITES) {
            if (site.locId == locId && siteStanding(state, site)) {
                parts.add("🌱 ${site.fixedText}")
            }
        }
        if (locId == ROPE_LOC && !state.arc4Retired &&
            state.arc4Started && state.arc4Chapter >= CHAPTERS4.size) {
            parts.add("🌱 The flagpole rope is frayed to threads — the " +
                "one outstanding complaint in the minutes. The whole " +
                "town is watching it now. So is somebody else, probably.")
        }
        if (locId == PORCH_LOC && state.arc4LastFixLanded) {
            parts.add("🌱 Your porch step is mended — nail heads lined " +
                "up, wood sealed — and a small potted marigold sits by " +
                "the door. No note. There was never going to be a note.")
        }
        for (fx in extraFixesStanding(state)) {
            if (fx.locId == locId) {
                parts.add("🌱 And here: ${fx.label}, fixed in the night " +
                    "and stayed fixed, like all of them do now.")
            }
        }
        return if (parts.isEmpty()) null else parts.joinToString("\n")
    }

    // ---------------- the clues ----------------

    enum class Kind4 { PATTERN, MATERIAL, CRAFT, WITNESS, PARTICIPATION }

    data class Clue4(
        val id: String,
        val kind: Kind4,
        /** 0-based chapter that must have BEGUN for this clue to exist. */
        val minChapter: Int,
        val title: String,
        val detail: String,
        /** WITNESS: the cast member who tells you. */
        val npc: String? = null,
        /** WITNESS: the line they say (the telling IS the discovery). */
        val spoken: String? = null,
        /** PATTERN / MATERIAL / CRAFT: the location to Look at. */
        val locId: String? = null,
        /** PATTERN / MATERIAL / CRAFT: the sentence the Look reports. */
        val foundText: String? = null,
        /** PARTICIPATION: the chapter def whose climax turf grants it. */
        val chapterDefId: String? = null
    )

    val CLUES4: List<Clue4> = listOf(
        Clue4("twine_signature", Kind4.CRAFT, 0,
            "The red twine bow",
            "Every repair the Gardener leaves is finished the same way: " +
                "lashed with red twine, a double bow, the ends trimmed " +
                "neat as a haircut. One pair of hands. Careful ones. The " +
                "twine is the kind they use in a garden, to tie up plants " +
                "that need holding up.",
            locId = "KidsPark",
            foundText = "🔍 Up close: the bench's loose joints are lashed " +
                "with red twine — a double bow, the ends trimmed neat as " +
                "a haircut. Not a repairman's rush job. A signature, " +
                "almost. In twine."),
        Clue4("complaint_match", Kind4.PATTERN, 0,
            "The complaint, word for word",
            "The posted minutes of the last town meeting, item 6: 'the " +
                "bench by the sandbox — a splinter hazard and a civic " +
                "embarrassment.' Action taken: none minuted. The bench " +
                "was mended the night these minutes went up. The Gardener " +
                "works from the town's own list.",
            locId = "CityHall",
            foundText = "🔍 The posted minutes of the last town meeting, " +
                "item 6: 'the bench by the sandbox — a splinter hazard " +
                "and a civic embarrassment.' Action taken: none minuted. " +
                "The bench was mended the night these minutes went up."),
        Clue4("meeting_room", Kind4.WITNESS, 0,
            "The back row of the meeting",
            "The mayor runs the monthly town meetings at City Hall: " +
                "half the town in folding chairs, every complaint said " +
                "out loud. And a back row of dragged-along kids, sitting " +
                "so still you'd swear the chairs were empty. Somebody in " +
                "that room has heard every word this town has said all year.",
            npc = "McDaniels",
            spoken = "You want to know who hears this town's complaints? " +
                "EVERYONE AT THE MEETINGS. That's the list. Half the town " +
                "in folding chairs, first Thursday, every month. And the " +
                "back row — parents drag the kids along, and those kids " +
                "sit SO still you'd swear the chairs were empty. Somebody " +
                "in that room has heard every complaint this town has " +
                "made all year. Every. Single. One. Including the bench."),
        Clue4("fountain_washer", Kind4.MATERIAL, 1,
            "The town's own washer",
            "The fountain runs on a new washer and a re-seated valve — " +
                "and the washer is stenciled PROPERTY OF SOUTH PARK, from " +
                "the town's own parts bin in the City Hall basement, the " +
                "bin everyone at the meetings files past. The Gardener " +
                "buys nothing. There is no purchase trail. There was " +
                "never going to be one.",
            locId = "MainStreet",
            foundText = "🔍 Inside the fountain's access hatch — left " +
                "spotless, of course: a new washer, a re-seated valve, " +
                "and on the washer, stenciled tiny: PROPERTY OF SOUTH " +
                "PARK. It came out of the town's own parts bin, the one " +
                "in the City Hall basement that everyone at the town " +
                "meetings walks past."),
        Clue4("almost_seen", Kind4.WITNESS, 1,
            "The patrolman's description",
            "The town's night patrol has nearly apprehended the " +
                "Gardener twice. The description: small. Quick. Polite. " +
                "Carrying a ladder, or possibly a very ambitious sandwich. " +
                "The patrolman is also out all night, every night — a " +
                "fact the town has begun to notice, in the way towns do.",
            npc = "Barbrady",
            spoken = "Twice now I've nearly apprehended your gardener. " +
                "Twice! Description: small. Quick. Polite — said 'scuse " +
                "me,' clear as a bell. …No wait. That was the wind, " +
                "probably. Small though. And carrying a ladder. Or " +
                "possibly a very ambitious sandwich. Anyway — I patrol " +
                "these streets ALL night, and I want it noted that I " +
                "have also jiggled that bench. Professionally. For years."),
        Clue4("steady_hands", Kind4.PARTICIPATION, 0,
            "A small thumbprint",
            "You held the bench steady for the daytime inspection. On " +
                "its underside, out of the weather: the twine bow, the " +
                "trimmed ends — and a thumbprint in the still-tacky " +
                "paint. A small thumbprint. A kid's.",
            chapterDefId = "arc4_ch1_bench"),
        Clue4("herring_nail", Kind4.CRAFT, 1,
            "The other hand at the gate",
            "The pond gate wears two repairs: on the post, a nail bent " +
                "flat and crooked, hammered by somebody strong and not " +
                "at all careful — and beside it, the real work: hinges " +
                "re-seated, the latch bound in red twine. Two different " +
                "hands have been at this gate. Only one of them knew " +
                "what it was doing. The night has more than one walker, " +
                "and the careless one isn't the Gardener.",
            locId = "StarksPond",
            foundText = "🔍 The pond gate, re-hung true. But look: on " +
                "the post, a nail bent flat and crooked, hammered by " +
                "somebody strong and not at all careful — and BESIDE it, " +
                "the real work: hinges re-seated, latch bound with the " +
                "red twine bow. Two different hands have been at this " +
                "gate. Only one of them knew what it was doing."),
        Clue4("unminuted_complaint", Kind4.PATTERN, 2,
            "The complaint that was never written down",
            "THE SPINE OF THE CASE. The planters were complained about " +
                "in the meeting room — out loud, standing up, AFTER the " +
                "gavel, after the minute book was closed. It was never " +
                "written, never posted, never read by anyone who wasn't " +
                "there. And yet: marigolds. Whoever fixed them was IN " +
                "THAT ROOM, after the meeting ended, close enough to " +
                "hear a thing said to nobody.",
            npc = "Gerald",
            spoken = "I keep the minutes. Every word this town complains " +
                "about goes in my book, and the book goes up on the " +
                "board, and that is the public record. The planters were " +
                "NOT in the book. Sheila said it standing up, after the " +
                "gavel, while I was putting my pen away: 'those dead " +
                "planters make us look like a town that gave up.' It was " +
                "never written. Never posted. And yet: marigolds. " +
                "Whoever fixed them was IN THAT ROOM, after the meeting " +
                "ended. I would like that understood."),
        Clue4("garden_keeper", Kind4.WITNESS, 2,
            "The garden's quiet keeper",
            "The Elementary playground garden — the beds behind the " +
                "school, marigolds and tomatoes — is tended by one of " +
                "the students. The quiet one. Out there every recess, in " +
                "the dirt, happy as anything; took the chore nobody " +
                "else wanted and has never once complained about it. " +
                "The planters' marigolds are the same strain as his beds.",
            npc = "Garrison",
            spoken = "The playground garden? One of my students tends " +
                "it. The quiet one. Out there every recess, in the dirt, " +
                "happy as anything — took the chore nobody else wanted " +
                "and has never once complained about it, which is more " +
                "than I can say for the faculty. The marigolds in front " +
                "of City Hall? Same strain as his beds. I may not know " +
                "children, but I know marigolds."),
        Clue4("seed_trace", Kind4.MATERIAL, 2,
            "Marigolds, watered from a can",
            "The planters' blooms are marigolds — the same deep-orange " +
                "strain as the beds in the Elementary playground garden — " +
                "and the boxes are staked with the garden's red twine. " +
                "The soil was still damp at dawn: watered from a can, " +
                "not a hose, the way you water something you're carrying " +
                "water TO.",
            locId = "CityHall",
            foundText = "🔍 The planters, up close: marigolds, the same " +
                "deep-orange strain as the beds in the Elementary " +
                "playground garden — and the boxes are staked with red " +
                "twine, bows trimmed neat. The soil is still damp. " +
                "Watered from a can, not a hose. Recently."),
        Clue4("night_crossing", Kind4.WITNESS, 2,
            "Seen crossing Main, 2 a.m.",
            "The town's other night-walker keeps the same hours as the " +
                "Gardener and saw them once, properly: someone small, a " +
                "ladder on one shoulder, a watering can in the other " +
                "hand, quiet as snow. He did not follow. It seemed rude.",
            npc = "Ned",
            spoken = "2 A.M. SOMEONE SMALL CROSSED MAIN STREET. LADDER " +
                "ON ONE SHOULDER. WATERING CAN IN THE OTHER HAND. QUIET " +
                "AS SNOW. I DID NOT FOLLOW. IT SEEMED RUDE."),
        Clue4("soil_trail", Kind4.PARTICIPATION, 2,
            "Potting soil to one chair",
            "After the special meeting, stacking chairs: on the hall " +
                "floor, a faint trail of potting soil, running from the " +
                "door to ONE chair in the back row. Somebody comes to " +
                "these meetings straight from a garden, sits in the back, " +
                "and hears everything.",
            chapterDefId = "arc4_ch3_planters")
    )

    val CLUE4_BY_ID: Map<String, Clue4> = CLUES4.associateBy { it.id }
    val CLUE4_IDS: Set<String> = CLUES4.map { it.id }.toSet()

    fun clueCount4(state: GameState): Int = state.mystery4Clues.size

    /** A clue exists in the world once its chapter has begun
     *  and the case is still open. */
    fun clueAvailable4(state: GameState, clue: Clue4): Boolean =
        !state.arc4Retired && state.arc4Chapter > clue.minChapter

    /** Grant a clue exactly once. The ticker + the town log
     *  hear about it; the Journal board keeps it forever. */
    fun grantClue4(state: GameState, clueId: String): Boolean {
        val clue = CLUE4_BY_ID[clueId] ?: return false
        if (clueId in state.mystery4Clues) return false
        state.mystery4Clues.add(clueId)
        state.events.add("🌱 CLUE ${state.mystery4Clues.size}/${CLUES4.size}: ${clue.title}. " +
            "(Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🌱 Clue found: ${clue.title}")
        return true
    }

    /** Talk-to discovery: if [npcName] is a live witness right
     *  now, their line — and the clue it grants. Null otherwise. */
    fun witnessLine4(state: GameState, npcName: String): Line? {
        // Delivery is the SPOKEN text, not the kind — the
        // spine itself is a PATTERN clue that only exists in
        // a witness's mouth, and it must ride this rail too.
        val clue = CLUES4.firstOrNull {
            it.spoken != null && it.npc == npcName
        } ?: return null
        if (!clueAvailable4(state, clue)) return null
        if (!grantClue4(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId] reports
     *  the first time it finds each thing the Gardener (or the
     *  room, or the record) left there — a location with
     *  several finds gives them up one at a time, in the order
     *  the case has opened them. */
    fun lookClueText4(state: GameState, locId: String): String? {
        val clue = CLUES4.firstOrNull {
            (it.kind == Kind4.PATTERN || it.kind == Kind4.MATERIAL ||
                it.kind == Kind4.CRAFT) &&
                it.locId == locId && it.id !in state.mystery4Clues &&
                clueAvailable4(state, it)
        } ?: return null
        if (!grantClue4(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation4(state: GameState, defId: String) {
        val clue = CLUES4.firstOrNull {
            it.kind == Kind4.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable4(state, clue)) return
        if (grantClue4(state, clue.id)) {
            state.events.add("🌱 Up close, something clicks: ${clue.detail}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after the
     *  buried case retires books chapter 1 ten days out from
     *  that retirement. While any earlier case is open this
     *  never fires — and never even books the date: the mystery
     *  slot has one case at a time. Chapters defer, day by
     *  day, past holiday episodes, like every case before them. */
    fun dueChapter4(state: GameState): ShenaniganDef? {
        if (state.arc4Retired) return null
        if (!state.arc3Retired) return null
        if (state.arc4Chapter >= CHAPTERS4.size) return null
        if (state.arc4DueAbsDay < 0) {
            state.arc4DueAbsDay = if (state.arc4Chapter == 0) {
                val anchor = if (state.arc3RetiredDay >= 0)
                    state.arc3RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc4DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS4[state.arc4Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted4(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf4(def.id)
        if (idx < 0) return
        state.arc4Started = true
        state.arc4Chapter = idx + 1
        state.arc4DueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the vigil ----------------

    /** The vigil is keepable: the last chapter is airing, its
     *  climax has begun, dusk has come down (the vigil is a
     *  dark-hours beat, never a noon one), no choice is locked
     *  yet, and the player holds enough of the case to be the
     *  one waiting in the schoolyard. */
    fun vigilDue(state: GameState): Boolean {
        if (state.arc4Retired || state.arc4Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE4_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        if (state.timeMin < VIGIL_FROM_MIN) return false
        return clueCount4(state) >= VIGIL_MIN_CLUES
    }

    /** Keep the vigil, and make the call about gratitude. */
    fun chooseVigil(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!vigilDue(state)) return false
        state.arc4Choice = choice
        state.events.add(when (choice) {
            "name" ->
                "🌙 You step out of the dark and say his name — gently, " +
                    "the way you'd want yours said. Tomorrow there'll be " +
                    "a ceremony. Tonight there's just a kid with a ladder, " +
                    "blinking like a light hurts."
            "guard" ->
                "🌙 You step back into the dark and let him pass, " +
                    "unseen. The rope gets fixed. So, in its time, does " +
                    "everything else."
            else ->
                "🌙 You don't step out. You leave your own fix beside " +
                    "his — and in the morning there will be two " +
                    "signatures on the flagpole, and only one name " +
                    "anybody knows. Yours."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class VigilOutcome(
        val branch: String,          // "name" | "guard" | "join" | "cold"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, and hand the wrap its
     *  words. A choice locked without the case behind it
     *  (impossible through the UI — the vigil never opens)
     *  degrades to the cold landing rather than a free decide,
     *  exactly like the disposition degraded to the county. */
    fun resolveVigil(state: GameState): VigilOutcome {
        val chosen = state.arc4Choice
        val backed = clueCount4(state) >= VIGIL_MIN_CLUES
        val branch = if (chosen in CHOICE_KEYS && backed) chosen else DEFAULT_BRANCH
        state.arc4Branch = branch
        state.arc4Retired = true
        state.arc4RetiredDay = state.absDay
        return when (branch) {
            "name" -> VigilOutcome(branch,
                "You said his name out loud, in the lamplight, kindly — " +
                    "and the town got its ceremony, its plaque, its speech. " +
                    "He stood in the middle of it staring at his shoes " +
                    "like they owed him money. The fixing stopped that day.",
                listOf(
                    "🌱 THE NAME: His name is DogPoo. He is nine years old. " +
                        "He sits in the back row at the town meetings " +
                        "because the chairs are quiet there, he tends the " +
                        "school garden because nobody else wanted the chore, " +
                        "and he fixed this town the way he does everything — " +
                        "completely, carefully, and without once asking to " +
                        "be seen doing it. Nobody ever wrote down who would " +
                        "fix things. So he did.",
                    "🌱 THE CEREMONY: The Mayor's speech ran eleven minutes. " +
                        "The plaque is by the City Hall door. He walks past " +
                        "it with his eyes down, every day, and the planters " +
                        "under it are already going brown at the edges. " +
                        "Kindness, it turns out, has one natural predator, " +
                        "and it is a microphone."))
            "guard" -> VigilOutcome(branch,
                "You kept the vigil, you learned the truth, and you carried " +
                    "it out of the schoolyard and told absolutely no one. " +
                    "The rope was fixed by morning. So, in their time, " +
                    "are all the rest.",
                listOf(
                    "🌱 THE SECRET: His name is DogPoo, and it will never " +
                        "be said at a ceremony, because there is never " +
                        "going to be one. He is nine. He sits in the back " +
                        "row because the chairs are quiet there, he hears " +
                        "every complaint this town makes, and he answers " +
                        "them at night with the garden's twine and the " +
                        "town's own parts, because nobody ever writes down " +
                        "who will. You know. That's the whole arrangement, " +
                        "and it's the one he wanted.",
                    "🌱 THE TOWN, KEPT: The fixes keep coming — a sign " +
                        "straightened here, a railing tightened there, a " +
                        "few a year, forever. The town calls it luck, or " +
                        "the new maintenance budget. Two people know " +
                        "better. One of them isn't telling, and the other " +
                        "one is nine."))
            "join" -> VigilOutcome(branch,
                "You didn't step out of the dark. You stepped INTO the " +
                    "work: your own fix, left beside his, signed in blue " +
                    "ribbon under his red twine. Two gardeners now, the " +
                    "board says. One unknown. One you.",
                listOf(
                    "🌱 THE NAME YOU KEEP: His name is DogPoo. He is nine, " +
                        "he sits in the back row because the chairs are " +
                        "quiet there, and the town he has been single-" +
                        "handedly holding together has just gained a " +
                        "second pair of hands. He found your ribbon in " +
                        "the morning. He was careful — so careful — not " +
                        "to cover it with his twine. That's the closest " +
                        "thing to a welcome the Night Gardener gives.",
                    "🌱 TWO GARDENERS: The fixes come in pairs now, " +
                        "sometimes — his twine and your ribbon, a bench " +
                        "here, a fence line there. The town has decided " +
                        "it's being kept by a married couple of ghosts. " +
                        "The town is wrong, and the town is welcome."))
            else -> VigilOutcome(branch,
                "Nobody waited in the right place. The minutes went up, " +
                    "the nights went by, the pattern held a while longer " +
                    "on momentum alone — and then, one winter, the fixes " +
                    "simply stopped, and nobody ever knew there had " +
                    "been hands to stop.",
                listOf(
                    "🌱 THE QUIET WINTER: The frayed rope was the last of " +
                        "it. It gave out in a January storm and the flag " +
                        "came down in the snow, and nobody fixed it, and " +
                        "everybody meant to get around to it, and nobody " +
                        "did. The planters went back to sticks. The bench " +
                        "by the sandbox wobbles again — children who've " +
                        "never known it solid think the wobble is the " +
                        "bench's personality.",
                    "🌱 THE LEDGER, UNBALANCED: Somewhere in this town " +
                        "there is a back-row chair that sits empty now, " +
                        "and a garden that gets watered in daylight, by " +
                        "rota, adequately. The case is closed. Whatever " +
                        "it was, it's over. That's the worst part: " +
                        "whatever it was, it's over."))
        }
    }

    /** The landing's ripples + its permanent marks — called
     *  by the wrap BEFORE the episode's credits are built, so
     *  the recap's faction-shift diff includes them. */
    fun applyVigilEffects(state: GameState, branch: String) {
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
                ripple("parents", 2,
                    "The Parents got their ceremony and their thank-you — gratitude with a microphone is still gratitude.")
                ripple("school_staff", 1,
                    "The Staff are quietly proud of one of theirs — and quietly sorry, in about that order.")
                befriend("McDaniels", 2)
                befriend("Garrison", 1)
                state.arc4LastFixPending = true
                state.arc4LastFixDay = state.absDay + LAST_FIX_DELAY_DAYS
                state.lifeText["mark:CityHall"] =
                    "A plaque by the door: 'THE TOWN THANKS ITS NIGHT " +
                        "GARDENER.' Under it, the planters are going brown " +
                        "at the edges. Nobody has watered them. Nobody " +
                        "knows whose job it was."
                townLogLine(state, "🌱 NAMED IN THE LAMPLIGHT: the town " +
                    "held its ceremony, and the fixing stopped the same day.")
            }
            "guard" -> {
                ripple("parents", 1,
                    "The Parents have a feeling the town is being looked after. They cannot prove it. They're right.")
                befriend("Gerald", 1)
                state.lifeText["mark:Elementary"] =
                    "The flag rope is sound, and the flag flies straight. " +
                        "Nobody talks about whose hands made it so. " +
                        "Somebody in town could tell you. Somebody won't."
                townLogLine(state, "🌱 THE VIGIL, KEPT QUIET: the rope was " +
                    "fixed by morning. The secret has a keeper now — two, counting him.")
            }
            "join" -> {
                ripple("boys_crew", 2,
                    "The Crew has concluded the town is kept by two ghosts and has opened a joint investigation. It will never close.")
                befriend(CULPRIT, 3)
                state.lifeText["mark:Elementary"] =
                    "The flag rope is sound, tied off with a red twine " +
                        "bow — and under it, a blue ribbon. Two signatures. " +
                        "One name nobody knows, and yours."
                townLogLine(state, "🌱 TWO GARDENERS: the flag flies over a " +
                    "schoolyard kept by two pairs of hands now, one unknown.")
            }
            else -> {
                state.lifeText["mark:Elementary"] =
                    "The flagpole stands bare. The rope gave out one " +
                        "winter and the flag came down in the snow. Nobody " +
                        "fixed it. Nobody fixes anything anymore, come to " +
                        "think of it."
                townLogLine(state, "🌱 THE FIXES STOPPED one winter, " +
                    "unexplained. The case closed itself, the way quiet things do.")
            }
        }
    }

    // ---------------- the last fix (the NAME landing's morning after) ----------------

    /** The delayed landing of the NAME choice: called daily
     *  by the engine; fires exactly once, the morning after
     *  the case closed — the one thank-you the Gardener could
     *  stand to give arrives at the player's own porch,
     *  privately, with a marigold. */
    fun noteLastFix(state: GameState): Boolean {
        if (!state.arc4LastFixPending) return false
        if (state.absDay < state.arc4LastFixDay) return false
        state.arc4LastFixPending = false
        state.arc4LastFixLanded = true
        state.events.add("🌱 ON YOUR PORCH THIS MORNING: the loose step, " +
            "mended — nail heads lined up, wood sealed — and a small " +
            "potted marigold by the door. No note. There was never " +
            "going to be a note. It's the last fix this town gets, " +
            "and he gave it to the one who said his name kindly.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🌱 One last fix, at the new kid's porch. " +
            "Then the town's quiet season began.")
        return true
    }

    // ---------------- the nod (the ending's standing line) ----------------

    private val NOD_LINES: Map<String, List<String>> = mapOf(
        "guard" to listOf(
            "…(He looks at you for one second longer than usual. " +
                "Then, down at the dirt between his shoes, he gives " +
                "the smallest nod there has ever been. It's enough.)",
            "…Thanks. For not — you know. Making it a thing. " +
                "(He is gone before you can answer, quiet as snow.)"),
        "join" to listOf(
            "…Yours held. The ribbon one. (A pause, enormous for " +
                "him.) I checked. I check all of them. Yours held.",
            "…(He almost smiles, which for him is a parade. There " +
                "is dirt on his hands and a watering can by his foot, " +
                "and tonight, apparently, is a work night. For both " +
                "of you. He doesn't say it. He doesn't have to.)"),
        "name" to listOf(
            "…(He stares at the ground like it owes him money. The " +
                "plaque's by the hall, the planters are going brown, " +
                "and he has not fixed so much as a hinge since the " +
                "ceremony. Being thanked, it turns out, was the one " +
                "repair he couldn't make.)",
            "…Eleven minutes, that speech was. I counted. " +
                "(He says it to the dirt, not to you. Then he walks " +
                "away, past the dead planters, at the speed of a kid " +
                "going nowhere in particular ever again.)")
    )

    /** The standing line: after the vigil, the Gardener has
     *  exactly this much to say to the one person who knows —
     *  which is to say, almost nothing, in three different
     *  flavors of almost. Null for everyone else, and in the
     *  cold ending (in the cold ending, he never knew you
     *  were looking for him at all). */
    fun gardenerNodLine4(state: GameState, npcName: String): Line? {
        if (npcName != CULPRIT || !state.arc4Retired) return null
        val bank = NOD_LINES[state.arc4Branch] ?: return null
        val idx = ((state.absDay % bank.size) + bank.size) % bank.size
        return Line(CULPRIT, bank[idx])
    }

    // ---------------- the Journal board ----------------

    /** The fourth case shows once it exists: chapter 1 has
     *  aired, a clue is in hand, or the case is closed. */
    fun boardVisible4(state: GameState): Boolean =
        state.arc4Started || state.mystery4Clues.isNotEmpty() || state.arc4Retired

    fun statusStamp4(state: GameState): String = when {
        state.arc4Retired && state.arc4Branch == "name" ->
            "🌟 NAMED — the town said thank you, and the fixing stopped"
        state.arc4Retired && state.arc4Branch == "guard" ->
            "🤫 KEPT — the secret has a keeper, and the fixes keep coming"
        state.arc4Retired && state.arc4Branch == "join" ->
            "🧤 TWO GARDENERS — one unknown, one you"
        state.arc4Retired -> "🧊 CLOSED — the fixes stopped one winter"
        state.shenaniganId == FINALE4_ID -> "🌙 THE VIGIL"
        state.arc4Started ->
            "🌱 FIXING — chapter ${state.arc4Chapter} of ${CHAPTERS4.size} aired"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the case is. */
    fun currentTheory4(state: GameState): String = when {
        state.arc4Retired && state.arc4Branch == "name" ->
            "CLOSED, with a plaque. The town knows his name, the town " +
                "said its thank-you into a microphone, and the town has " +
                "started to understand what the thank-you cost. The " +
                "planters are going brown at the edges."
        state.arc4Retired && state.arc4Branch == "guard" ->
            "OPEN FOREVER, by design. The fixes keep coming and the " +
                "town has settled into being looked after by hands it " +
                "will never see. Somebody knows. Somebody is never, " +
                "ever telling."
        state.arc4Retired && state.arc4Branch == "join" ->
            "DOUBLED. The town is kept by two gardeners now, and the " +
                "town's leading theory is a married couple of ghosts. " +
                "The town is wrong, and the town is welcome."
        state.arc4Retired ->
            "CLOSED, unexplained. Things used to fix themselves in " +
                "this town. They don't anymore. Nobody knows why, and " +
                "nobody ever will. That's the whole file."
        !state.arc4Started ->
            "Nothing yet. The town's benches wobble and its fountains " +
                "are dry, same as always. Probably."
        state.arc4Chapter <= 1 ->
            "A prank, says the town, or a contractor's mix-up. A prank " +
                "that does joinery, says the town, quieter."
        state.arc4Chapter == 2 ->
            "The fixes answer the complaints. That's the arithmetic " +
                "now: complaints go in at the meetings, and fixes come " +
                "out in the night. Somebody is LISTENING at those meetings."
        state.arc4Chapter == 3 ->
            "Somebody in the meeting room. Somebody small. Somebody " +
                "with a garden and a key to the parts bin and no wish " +
                "at all to be thanked. The back row has started " +
                "getting looked at, in the meetings, in a way it " +
                "never has been before."
        else ->
            "One complaint left, one rope, one night. The whole town " +
                "is holding its breath and pretending to be home asleep."
    }

    /** The fourth case's board as plain lines (the Journal
     *  renders them below the buried case's; the suite reads
     *  them). */
    fun boardLines4(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp4(state))
        out.add("🌱 THE FIXES")
        for (site in FIX_SITES) {
            val status = when {
                site.id == "rope" && !state.arc4Retired &&
                    state.arc4Started && state.arc4Chapter >= CHAPTERS4.size ->
                    "THE WATCHED ONE — tonight decides it"
                siteStanding(state, site) -> "FIXED — and stayed fixed"
                site.chapter < state.arc4Chapter || state.arc4Retired ->
                    "never got fixed"
                else -> "not yet"
            }
            out.add("• ${site.name} — $status")
        }
        for (fx in extraFixesStanding(state)) {
            out.add("• ${fx.label} — FIXED, in the night, like always now")
        }
        if (state.arc4LastFixLanded) {
            out.add("• Your porch step, and a marigold — the last fix, " +
                "given privately, the morning after the ceremony.")
        }
        out.add("🧩 CLUES — ${clueCount4(state)}/${CLUES4.size}" +
            if (!state.arc4Retired && clueCount4(state) < VIGIL_MIN_CLUES)
                " (keeping the vigil takes $VIGIL_MIN_CLUES)" else "")
        for (clue in CLUES4) {
            if (clue.id in state.mystery4Clues) {
                out.add("• ${clue.title} — ${clue.detail}")
            }
        }
        val missing = CLUES4.size - clueCount4(state)
        if (missing > 0 && !state.arc4Retired) {
            out.add("• ??? — $missing still out there. Talk to the " +
                "meeting-goers. Look at the fixes up close. Work a " +
                "chapter's ground. Read the posted minutes.")
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory4(state)}")
        if (state.arc4Retired) {
            out.add(when (state.arc4Branch) {
                "name" -> "🏁 You said his name kindly, in the lamplight, " +
                    "and the town said its thank-you into a microphone. " +
                    "The case is closed. So is the garden shed, most days."
                "guard" -> "🏁 You learned the truth and carried it out " +
                    "of the schoolyard alone. The town is kept, the " +
                    "secret is kept, and the two of you keep both."
                "join" -> "🏁 You joined the work instead of ending it. " +
                    "The town is kept by two gardeners now — one unknown, " +
                    "one of them you. The board approves of this arrangement."
                else -> "🏁 Nobody kept the vigil. The fixes faded, the " +
                    "rope gave out one winter, and the file closed itself, " +
                    "the way quiet things do."
            })
            if (state.arc4Branch == "name") {
                if (state.arc4LastFixPending) {
                    out.add("🌱 One thing is still coming: the morning " +
                        "after a ceremony like that, a fixer with one " +
                        "fix left in him tends to spend it somewhere " +
                        "personal. Check your porch.")
                } else if (state.arc4LastFixLanded) {
                    out.add("🌱 The last fix landed on your porch the " +
                        "morning after — a mended step, and a marigold. " +
                        "His thank-you, given the only way he could: " +
                        "privately, with his hands.")
                }
            }
            if (state.arc4Branch == "guard") {
                out.add("🤫 The fixes keep coming — a few a year, " +
                    "forever. You could stop them with one sentence. " +
                    "You will never say the sentence.")
            }
            if (state.arc4Branch == "join") {
                out.add("🧤 Both hands keep working — his twine, your " +
                    "ribbon. The town is kept by two gardeners, one " +
                    "unknown, and it has never been in better repair.")
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS4 = mapOf(
        "name" to listOf(
            "They put his name on a plaque at City Hall. He walks past " +
                "it with his eyes DOWN. A plaque is just a fix somebody " +
                "made loud.",
            "Nothing's been fixed in weeks. Funny — you don't notice " +
                "the gardener until the gardening stops."),
        "guard" to listOf(
            "The bus stop sign got straightened last month. Nobody " +
                "saw. Things around here just… stay fixed now. It's " +
                "becoming a real pattern.",
            "Mayor says the town's 'turning a corner.' The town's " +
                "being TURNED, says my dad. One bench at a time."),
        "join" to listOf(
            "Two fixes at the SoDoSoPa bench — you can TELL: one's " +
                "got that red twine, the other's got a ribbon. This " +
                "town has two secret gardeners now and I intend to " +
                "die not knowing either.",
            "Whoever they are, there's two of them now. The fixes " +
                "have started arriving in pairs, like the town's being " +
                "kept by a married couple of ghosts."),
        "cold" to listOf(
            "The flag's still down at the school. Somebody ought to " +
                "fix that rope. Somebody ought to fix a lot of things. " +
                "They used to just… get fixed.",
            "Remember when stuff around here fixed itself? My kid " +
                "thinks I imagined it. I did not imagine it.")
    )

    /** A post-case ambient aside in the town's own voice — the
     *  ending the town lives with is the one it keeps telling.
     *  Null until the case is closed. */
    fun ambientCallback4(state: GameState, rnd: Random): String? {
        if (!state.arc4Retired) return null
        val bank = CALLBACKS4[state.arc4Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

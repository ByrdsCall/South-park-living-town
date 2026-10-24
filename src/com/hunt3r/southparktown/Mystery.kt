package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery — the long-arc season mystery (v0.2.0f, Queue #2 #6).
//
// ONE story told across five in-game weeks through the
// Shenanigans / Episodes machinery: "The Lights Over Stark's
// Pond". Small round lights bob over the pond at night; small
// things go missing all over town; the town theorizes (aliens,
// a gnome franchise, a drone ad campaign). The truth is smaller
// and dumber than every theory: it is Butters — Professor Chaos
// on half-asleep night "treasure walks", a bike helmet wrapped
// in foil, requisitioning small light ridiculous things for a
// blanket-fort Fortress of Doom behind the pond hut.
//
// Structure:
//  - ARC_CHAPTERS: five full ShenaniganDefs (4 stages each,
//    episode framing data included). They live OUTSIDE the
//    regular SHENANIGANS pool — they never roll randomly — but
//    ARE registered in SHENANIGAN_BY_ID so every system that
//    resolves an event id (engine, quests, episodes, views)
//    treats a chapter exactly like any other event.
//  - Scheduling: a chapter becomes due every CHAPTER_GAP_DAYS
//    (7) in-game days. When due, it takes that day's event slot
//    outright (Engine.rollShenanigan asks here FIRST); it
//    defers, day by day, while a holiday episode is on, so a
//    chapter never shares a day with a regular event or a
//    holiday special. Regular events fill the other weeks.
//  - Clues: 12, in three kinds — WITNESS lines granted by
//    talking to specific cast during arc weeks (they ride the
//    talk sheet's pre-lines), LOOK objects granted by Looking
//    at specific places, and PARTICIPATION discoveries granted
//    by getting your hands dirty at a chapter's climax turf.
//    Each grants exactly once. The Journal's 🔍 MYSTERY page
//    is the clue board.
//  - Finale (chapter 5): at the climax, if the player holds
//    at least TRUTH_MIN_CLUES clues, they get a real choice —
//    present the truth, or say nothing and let the town have
//    its aliens. Below the threshold there is no choice: the
//    town picks its own (wrong, funnier) ending. All three
//    landings write their own outcome, credits lines, faction
//    ripples and permanent marks.
//  - Retirement: after the finale wraps, the arc RETIRES.
//    One arc per save — it never re-runs. Ambient pairs keep
//    talking about the ending afterward (Threads-style).
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery {

    const val ARC_TITLE = "The Lights Over Stark's Pond"
    const val CHAPTER_GAP_DAYS = 7
    const val FIRST_CHAPTER_MIN_ABSDAY = 3
    const val TRUTH_MIN_CLUES = 7
    const val FINALE_ID = "arc_ch5_reckoning"

    // ---------------- the five chapters ----------------

    val ARC_CHAPTERS: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc_ch1_lights",
            title = "Lights Over the Pond",
            cause = "Jimbo came down from the pond at dawn swearing the sky over Stark's Pond " +
                "'did a little dance'. Nobody believed him. Then Kenny saw it too.",
            blurb = "Strange lights over Stark's Pond. The town demands answers, snacks, " +
                "and possibly a pamphlet.",
            theme = QuestTheme("🛸", "#7ac8e8", "THE POND LIGHTS"),
            tintHex = "#1e2c44",
            stages = listOf(
                ShenaniganStage("Kickoff: The First Sighting",
                    "🛸 LIGHTS OVER THE POND: Jimbo saw the sky 'do a little dance' over Stark's Pond.",
                    "Jimbo's dawn sighting has the town split between 'aliens' and 'Jimbo saw a plane'. " +
                        "Kenny backing him up moved the needle.",
                    "Head to Stark's Pond and see where it happened.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Reenacting the sighting. Pointing at the sky a lot.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 4,
                            note = "The Boys' Crew has opened an investigation."))),
                ShenaniganStage("Escalation: Pond Watch",
                    "🛸 Half the town is camped at the pond with binoculars and thermoses. " +
                        "The lights have an audience now.",
                    "Pond Watch is a social event now. People brought folding chairs. The lights " +
                        "have not performed on schedule, which everyone agrees is suspicious.",
                    "Join Pond Watch at Stark's Pond.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "On Pond Watch. Snacks ready. Eyes up.",
                    tone = "whisper", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("church_faithful", influenceDelta = 2,
                            note = "Father Maxi notes the town looks up more lately."))),
                ShenaniganStage("Climax: The Lights Return",
                    "🛸 CLIMAX: THE LIGHTS ARE BACK over Stark's Pond — small, round, bobbing, " +
                        "and gone before anyone's camera opens!",
                    "Everyone saw them this time. Small round lights, bobbing over the water " +
                        "'like they were looking for something'. Fog ate the evidence.",
                    "Get to Stark's Pond — you might still catch them.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Watching the sky over the pond. Barely breathing.",
                    tone = "hype", weatherLock = "fog", litterBurst = 2,
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 3, relationWith = "craigs_gang",
                            relationDelta = -1,
                            note = "Craig's Gang thinks the Crew is staging it. " +
                                "The Crew is offended and thrilled."))),
                ShenaniganStage("Resolution: Just Weather (Allegedly)",
                    "🛸 The lights don't come back. The official word is 'weather balloon'. " +
                        "Nobody official said it. It's just the word now.",
                    "Pond Watch packs up. Jimbo is writing a pamphlet. Somebody left a folding " +
                        "chair. The pond keeps its counsel.",
                    "Watch Pond Watch pack up.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Jimbo", "Kenny"),
            episodeTitles = listOf("It Came From Over The Pond", "The Night The Sky Blinked"),
            coldOpen = listOf(
                Line("Jimbo", "I'm telling you, the sky over that pond did a DANCE. A whole routine."),
                Line("Kenny", "(muffled) He screamed. Jimbo screamed at lights. I saw them too, though."))
        ),
        ShenaniganDef(
            id = "arc_ch2_vanishing",
            title = "The Vanishing",
            cause = "The Elementary flag is gone. So is a case of Cheesy Poofs, the Church bell's " +
                "clapper, and four left shoes. Wendy has started a list.",
            blurb = "Small things are disappearing all over town. Nothing valuable. " +
                "Nothing heavy. Just… gone.",
            theme = QuestTheme("🧦", "#c8b06a", "THINGS ARE MISSING"),
            stages = listOf(
                ShenaniganStage("Kickoff: The Empty Flagpole",
                    "🧦 THE VANISHING: The school flag is GONE. Also a bell clapper, a snack " +
                        "shipment, and several left shoes.",
                    "The list is growing and makes no sense. Everything missing is small, light, " +
                        "and faintly ridiculous.",
                    "Check the empty flagpole at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Staring up at the empty flagpole. Audibly puzzled.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("girls_circle", influenceDelta = 3,
                            note = "The Girls' Circle is running the official List of Missing Things."))),
                ShenaniganStage("Escalation: Inventory Panic",
                    "🧦 The Mall is counting everything twice. A case of Cheesy Poofs vanished " +
                        "from the loading dock in the night.",
                    "Stores are chaining their displays. Someone chained the step stools too, " +
                        "which everyone agrees feels pointed.",
                    "Check the Mall loading dock story.",
                    routine = "cluster", clusterLoc = "Mall",
                    clusterActivity = "Counting inventory. Twice. Slowly.",
                    tone = "hype", litterBurst = 2,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = -3, standingDelta = -1,
                            note = "The Parents are missing things and mentioning it at dinner."))),
                ShenaniganStage("Climax: A Town Counts Its Socks",
                    "🧦 CLIMAX: Main Street holds an emergency inventory. Half the town discovers " +
                        "it's missing exactly one sock.",
                    "The emergency count confirms it: the thief takes ONE of things. One sock. " +
                        "One shoe. One clapper. Who steals in SINGLES?",
                    "Join the emergency count on Main Street.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Counting socks in public. A town united.",
                    tone = "hype", litterBurst = 2,
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = -2,
                            note = "Recess is now partly a missing-shoe investigation."))),
                ShenaniganStage("Resolution: Still Missing",
                    "🧦 Nothing came back overnight. The List of Missing Things gains a second " +
                        "page. The town sleeps with its shoes on.",
                    "No culprit, no pattern anyone will say out loud. Just a list, a pond with " +
                        "lights, and a town holding its belongings closer.",
                    "Read the List. Think it over.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Wendy", "Cartman"),
            episodeTitles = listOf("Small Things, Gone", "The Case Of The Empty Flagpole"),
            coldOpen = listOf(
                Line("Wendy", "The flag. A case of Cheesy Poofs. Four left shoes. " +
                    "Somebody in this town has a TYPE."),
                Line("Cartman", "I'm just saying, if it's aliens, they have terrible taste. " +
                    "If it's me — it is NOT me."))
        ),
        ShenaniganDef(
            id = "arc_ch3_theories",
            title = "Everybody's an Expert",
            cause = "The town meeting to discuss the lights and the vanishings lasted four hours. " +
                "It produced three official theories and a merchandise table.",
            blurb = "Aliens, a gnome franchise, or a drone ad campaign. The town will now " +
                "argue about it professionally.",
            theme = QuestTheme("🎤", "#b06ac8", "THEORY WAR"),
            stages = listOf(
                ShenaniganStage("Kickoff: The Meeting Is Called",
                    "🎤 THEORY WAR: City Hall calls a town meeting on the lights and the " +
                        "vanishings. Bring your own theory.",
                    "Every faction has a position. The goth kids' position is that everyone else " +
                        "is embarrassing. Noted in the minutes.",
                    "Get to City Hall for the meeting.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Presenting a theory. With charts.",
                    tone = "hype",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 3,
                            note = "Even as a theory, the drone idea is free advertising."))),
                ShenaniganStage("Escalation: Pamphlet Season",
                    "🎤 Jimbo's pamphlet is in its second printing. The gnome camp has MERCH now. " +
                        "Kyle has a spreadsheet and no patience.",
                    "The argument has structure now: camps, slogans, a bake sale of unclear allegiance.",
                    "Walk Main Street and hear the camps out.",
                    routine = "cluster", clusterLoc = "MainStreet",
                    clusterActivity = "Campaigning for a theory. Loudly, with flyers.",
                    tone = "hype", litterBurst = 3),
                ShenaniganStage("Climax: The Official Sketch",
                    "🎤 CLIMAX: The town unveils its OFFICIAL SKETCH of the entity: squat body, " +
                        "huge round shining head. Applause.",
                    "The sketch artist worked from thirty witness descriptions. The result is " +
                        "unanimous and, to a few quiet people, extremely familiar-looking.",
                    "See the official sketch at City Hall.",
                    routine = "cluster", clusterLoc = "CityHall",
                    clusterActivity = "Studying the official sketch. Nodding gravely.",
                    tone = "hype", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 0, relationWith = "craigs_gang",
                            relationDelta = -1,
                            note = "The camps are not speaking. Craig's Gang sells popcorn to both."))),
                ShenaniganStage("Resolution: Hung Jury",
                    "🎤 The meeting adjourns with no verdict, three theories, and a plan to " +
                        "'watch the pond harder'. Democracy.",
                    "Nothing was decided, but something shifted: the town stopped saying 'if' " +
                        "and started saying 'when it comes back'.",
                    "Catch your breath after democracy.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Randy", "Kyle", "Tweek"),
            episodeTitles = listOf("A Pamphlet For Every Theory", "The Town Meeting Will Come To Order"),
            coldOpen = listOf(
                Line("Randy", "I've crunched the numbers and the lights are a marketing " +
                    "opportunity. I mean — a THREAT. A threat we can monetize."),
                Line("Tweek", "It's the gnomes, man! They've FRANCHISED! " +
                    "The underpants were just PHASE ONE!"))
        ),
        ShenaniganDef(
            id = "arc_ch4_photograph",
            title = "The Photograph",
            cause = "Somebody got a photo. It's blurry, it's backlit, and it changes " +
                "everything and nothing.",
            blurb = "One blurry photograph of a small round-headed figure, and South Park " +
                "loses its mind all over again.",
            theme = QuestTheme("📸", "#e8a05a", "BLURRY EVIDENCE"),
            stages = listOf(
                ShenaniganStage("Kickoff: The Photo Drops",
                    "📸 THE PHOTOGRAPH: A blurry shot of the entity is going around Kid's Park. " +
                        "It has a round shining head and, witnesses insist, 'vibes'.",
                    "The photo shows almost nothing and convinces almost everyone. The figure is " +
                        "backlit, mid-dash, carrying something. It is not tall.",
                    "See the photo at Kid's Park.",
                    routine = "cluster", clusterLoc = "KidsPark",
                    clusterActivity = "Passing the photograph around. Enhancing it with squinting.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("girls_circle", influenceDelta = 2,
                            note = "The Girls' Circle has enhanced the photo four times. " +
                                "It is now just pixels and confidence."))),
                ShenaniganStage("Escalation: Pilgrims",
                    "📸 People are leaving snacks at the pond 'for it'. A juice box. Several " +
                        "juice boxes. The entity has a brand now.",
                    "The pond has a little shrine of offerings: juice boxes, a sock (left), " +
                        "three Cheesy Poofs arranged in a triangle.",
                    "Visit the pond and the offering shrine.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Leaving offerings at the pond. Just in case.",
                    tone = "whisper", litterBurst = 2,
                    factionEffects = listOf(
                        FactionEffect("church_faithful", influenceDelta = 3,
                            note = "Father Maxi has opinions about shrines and is keeping count."))),
                ShenaniganStage("Climax: The Candle Vigil",
                    "📸 CLIMAX: The Church holds a candlelight vigil for answers. Half the town " +
                        "attends. The other half is at the pond, watching.",
                    "Candles, hymns, and a collection 'for the entity's family, wherever they " +
                        "are'. The collection bought more candles.",
                    "Attend the vigil at the Church.",
                    routine = "cluster", clusterLoc = "Church",
                    clusterActivity = "Holding a candle for the truth. Melting slightly.",
                    tone = "whisper", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("church_faithful", influenceDelta = 4,
                            note = "The Church is having its best month in years."))),
                ShenaniganStage("Resolution: One Clear Night",
                    "📸 The sky over the pond is clear and empty all night. The town takes this " +
                        "as a sign. Of what is left as an exercise.",
                    "A decision is forming at City Hall: the town will stake out the pond next " +
                        "week and END this. Volunteers are requested. Costumes discouraged.",
                    "Rest up. Next week: the stakeout.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Towelie", "Stephen", "Butters"),
            episodeTitles = listOf("Enhance. ENHANCE.", "Four Feet Of Mystery"),
            coldOpen = listOf(
                Line("Towelie", "I didn't just SEE it, man. I got a PICTURE. " +
                    "Well — half a picture. The good half."),
                Line("Stephen", "That's… a very round head. And are those… little arms?"))
        ),
        ShenaniganDef(
            id = FINALE_ID,
            title = "The Reckoning at the Pond",
            cause = "The town is done wondering. Next clear night: a full stakeout at Stark's " +
                "Pond, everybody watching, nobody blinking.",
            blurb = "Five weeks of lights, lists, and theories come down to one night at the " +
                "pond. Bring a thermos.",
            theme = QuestTheme("🔍", "#e86a5a", "THE RECKONING"),
            tintHex = "#232338",
            stages = listOf(
                ShenaniganStage("Kickoff: Stakeout Stations",
                    "🔍 THE RECKONING: Stakeout stations are set around Stark's Pond. " +
                        "Everybody has a thermos. Nobody has a plan past 'watch'.",
                    "Thirty witnesses, one pond, zero blinking. If the lights come tonight, " +
                        "they come to an audience.",
                    "Take your station at Stark's Pond.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "On stakeout. Thermos ready. Eyes up.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 3,
                            note = "The Crew runs the stakeout roster."))),
                ShenaniganStage("Escalation: The Long Watch",
                    "🔍 Hours pass. The thermoses empty. The pond does nothing, beautifully. " +
                        "Someone starts a rumor it can hear them.",
                    "Morale holds on snacks alone. Towelie has been assigned a buddy so he " +
                        "stops narrating.",
                    "Stay on watch at Stark's Pond.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Holding the long watch. Blinking in shifts.",
                    tone = "whisper", litterBurst = 2),
                ShenaniganStage("Climax: The Light Comes",
                    "🔍 CLIMAX: A light bobs out of the fog over the pond — small, round, " +
                        "close enough to see by. The whole town holds its breath.",
                    "It's here. Low over the water, bobbing the way it has all month — and " +
                        "tonight there are thirty witnesses and one kid in town who might " +
                        "know exactly what they're looking at.",
                    "Watch the light. Decide what you know.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Watching the light. Not breathing. Not blinking.",
                    tone = "whisper", weatherLock = "fog"),
                ShenaniganStage("Resolution: Morning After",
                    "🔍 By morning, the town will have its answer — or the one it picked. " +
                        "Coffee first. History second.",
                    "Whatever happened at the pond last night, the List of Missing Things is " +
                        "closed. Some pages of it will never be explained to the town's " +
                        "satisfaction. Some won't need to be.",
                    "See what the town makes of it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Stan", "Kyle", "Wendy", "Butters"),
            episodeTitles = listOf("What The Light Was", "The Smallest Culprit"),
            coldOpen = listOf(
                Line("Stan", "Tonight we watch the pond and we END this. Agreed?"),
                Line("Butters", "Fellas, I just want to say — whatever it is, it probably has " +
                    "its reasons. Its small, tired reasons."))
        )
    )

    val ARC_BY_ID: Map<String, ShenaniganDef> = ARC_CHAPTERS.associateBy { it.id }

    /** 0-based chapter index of a def id, or -1 when it's not ours. */
    fun chapterIndexOf(defId: String?): Int =
        ARC_CHAPTERS.indexOfFirst { it.id == defId }

    fun isChapter(defId: String?): Boolean = chapterIndexOf(defId) >= 0

    // ---------------- the clues ----------------

    enum class ClueKind { WITNESS, LOOK, PARTICIPATION }

    data class Clue(
        val id: String,
        val kind: ClueKind,
        /** 0-based chapter that must have BEGUN for this clue to exist. */
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

    val CLUES: List<Clue> = listOf(
        Clue("lights_shopping", ClueKind.WITNESS, 0,
            "Jimbo's testimony",
            "Jimbo swears the lights 'moved like they was shopping. Pausing. Comparing. " +
                "One of 'em hovered over the dock like it couldn't decide.'",
            npc = "Jimbo",
            spoken = "I seen 'em again, over the pond. And I'll tell you what the government " +
                "won't: them lights moved like they was SHOPPING. Pausin'. Comparin'. One " +
                "hovered by the dock like it couldn't decide."),
        Clue("foil_square", ClueKind.LOOK, 0,
            "A perfect foil square",
            "On the dock: a square of aluminum foil, folded with insane precision. " +
                "Landing craft don't fold their trash this neatly. Small hands do.",
            locId = "StarksPond",
            foundText = "🔍 On the dock, weighted by a pebble, sits a square of aluminum " +
                "foil — folded with insane precision. You pocket the thought, not the foil."),
        Clue("boot_prints", ClueKind.PARTICIPATION, 0,
            "Small boot prints",
            "Helping out at the pond, you got a close look at the famous 'landing marks': " +
                "a spiral of boot prints. Small ones. With a wobbly left heel.",
            chapterDefId = "arc_ch1_lights"),
        Clue("kenny_sighting", ClueKind.WITNESS, 1,
            "Kenny saw it up close",
            "Kenny, who has died professionally, was not scared. He says the figure was " +
                "small, round-headed, shiny — and friendly.",
            npc = "Kenny",
            spoken = "(muffled) I seen it, you guys. By the hut. Little guy, shiny round " +
                "head, dragging a bag bigger than him. He waved at me. I waved back. " +
                "You wave back."),
        Clue("straw_trail", ClueKind.LOOK, 1,
            "The straw trail",
            "A trail of juice-box straws leads from the park toward Stark's Pond, one " +
                "dropped every few yards — a breadcrumb trail laid by someone with a " +
                "limited attention span.",
            locId = "KidsPark",
            foundText = "🔍 In the grass: a juice-box straw. Then another, ten paces on. " +
                "Then another. The trail points, unmistakably, at Stark's Pond."),
        Clue("wendy_pattern", ClueKind.WITNESS, 2,
            "Wendy's list",
            "Wendy's inventory of the missing: everything taken is small, light, and " +
                "faintly ridiculous. This is not a heist. It's a collection.",
            npc = "Wendy",
            spoken = "I made a list of everything that's gone missing. Flag, Cheesy Poofs, " +
                "one bell clapper, four left shoes. Notice anything? It's all SMALL. And " +
                "light. And honestly? Kind of fun. Nobody steals like this for money."),
        Clue("step_stool", ClueKind.LOOK, 2,
            "The step stool",
            "Beneath the empty flagpole: a step stool, the kind from a classroom. Whatever " +
                "took the flag could not reach it standing on the ground. Whatever took " +
                "the flag is not tall.",
            locId = "Elementary",
            foundText = "🔍 Under the flagpole stands a classroom step stool, scuffed and " +
                "abandoned. Someone needed the extra inches. Someone small."),
        Clue("handprint", ClueKind.LOOK, 2,
            "A small handprint",
            "In the dust on the Mall loading dock: one small handprint, fingers spread — " +
                "someone steadying themselves, climbing down with an armful of Cheesy Poofs.",
            locId = "Mall",
            foundText = "🔍 The loading-dock dust holds one perfect handprint, fingers " +
                "spread, much smaller than yours."),
        Clue("official_sketch", ClueKind.PARTICIPATION, 2,
            "The official sketch",
            "At the town meeting you studied the official artist's sketch of the 'alien': " +
                "squat body, huge round shiny head. It looks exactly like a kid in a bike " +
                "helmet. The town applauded the sketch.",
            chapterDefId = "arc_ch3_theories"),
        Clue("juice_box", ClueKind.WITNESS, 3,
            "Towelie's encounter",
            "Towelie was offered a juice box by the entity. He has thought about " +
                "nothing else since.",
            npc = "Towelie",
            spoken = "I met the little guy, man. By the pond. He offered me a juice box. " +
                "A JUICE BOX. Aliens don't do that, man. Aliens probe. This guy SNACKS."),
        Clue("stephen_tired", ClueKind.WITNESS, 3,
            "A father's report",
            "Stephen, not quite putting it together: a tired son, a missing bike helmet, " +
                "foil all over the garage.",
            npc = "Stephen",
            spoken = "Between us? Butters has been exhausted all week. His bike helmet's " +
                "gone missing, there's foil all over my garage, and he asked his mother " +
                "what 'statute of limitations' means. Fatherhood is just… a series of mysteries."),
        Clue("driveway_foil", ClueKind.LOOK, 3,
            "Foil in the driveway",
            "The Stotch driveway glitters: foil scraps, a dented juice box, and a small " +
                "spiral of boot prints with a wobbly left heel. You've seen that spiral before.",
            locId = "ButtersHouse",
            foundText = "🔍 The Stotch driveway glitters — foil scraps, a dented juice " +
                "box, and small boot prints spiraling out toward the street. " +
                "Wobbly left heel and all.")
    )

    val CLUE_BY_ID: Map<String, Clue> = CLUES.associateBy { it.id }
    val CLUE_IDS: Set<String> = CLUES.map { it.id }.toSet()

    fun clueCount(state: GameState): Int = state.mysteryClues.size

    /** A clue exists in the world once its chapter has begun and
     *  the case is still open. */
    fun clueAvailable(state: GameState, clue: Clue): Boolean =
        !state.arcRetired && state.arcChapter > clue.minChapter

    /** Grant a clue exactly once. The ticker + the town log hear
     *  about it; the Journal board keeps it forever. */
    fun grantClue(state: GameState, clueId: String): Boolean {
        val clue = CLUE_BY_ID[clueId] ?: return false
        if (clueId in state.mysteryClues) return false
        state.mysteryClues.add(clueId)
        state.events.add("🔍 CLUE ${state.mysteryClues.size}/${CLUES.size}: ${clue.title}. " +
            "(Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🔍 Clue found: ${clue.title}")
        return true
    }

    /** Talk-to discovery: if [npcName] is a live witness right
     *  now, their line — and the clue it grants. Null otherwise. */
    fun witnessLine(state: GameState, npcName: String): Line? {
        val clue = CLUES.firstOrNull {
            it.kind == ClueKind.WITNESS && it.npc == npcName
        } ?: return null
        if (!clueAvailable(state, clue)) return null
        if (!grantClue(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId] reports
     *  the first time it finds this chapter's evidence. */
    fun lookClueText(state: GameState, locId: String): String? {
        val clue = CLUES.firstOrNull {
            it.kind == ClueKind.LOOK && it.locId == locId
        } ?: return null
        if (!clueAvailable(state, clue)) return null
        if (!grantClue(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation(state: GameState, defId: String) {
        val clue = CLUES.firstOrNull {
            it.kind == ClueKind.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable(state, clue)) return
        if (grantClue(state, clue.id)) {
            state.events.add("🔍 Up close, something clicks: ${clue.detail}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if any.
     *  Also SCHEDULES the arc (a fresh arc's first due day is set
     *  the first time anyone asks — i.e. the first eligible
     *  rollover). Defers while a holiday episode is on: a
     *  chapter never shares a day with a holiday special, and
     *  the weekly event slot is the engine's single slot, so it
     *  can never share with a regular event either. */
    fun dueChapter(state: GameState): ShenaniganDef? {
        if (state.arcRetired) return null
        if (state.arcChapter >= ARC_CHAPTERS.size) return null
        if (state.arcDueAbsDay < 0) {
            state.arcDueAbsDay = if (state.arcChapter == 0)
                maxOf(state.absDay, FIRST_CHAPTER_MIN_ABSDAY)
            else state.absDay
        }
        if (state.absDay < state.arcDueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return ARC_CHAPTERS[state.arcChapter]
    }

    /** Engine hook: a chapter just took the slot. Books it and
     *  sets the next chapter's due day a week out. */
    fun noteChapterStarted(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf(def.id)
        if (idx < 0) return
        state.arcStarted = true
        state.arcChapter = idx + 1
        state.arcDueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale ----------------

    /** The choice is on the table: the finale is airing, its
     *  climax has begun, no choice is locked yet, and the player
     *  holds enough of the case to know the truth. */
    fun finaleChoiceDue(state: GameState): Boolean {
        if (state.arcRetired || state.arcChoice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return clueCount(state) >= TRUTH_MIN_CLUES
    }

    /** Lock the player's answer in at the climax. */
    fun chooseFinale(state: GameState, presentTruth: Boolean): Boolean {
        if (!finaleChoiceDue(state)) return false
        state.arcChoice = if (presentTruth) "truth" else "silent"
        state.events.add(if (presentTruth)
            "🔍 You step forward at the pond. You know what that light is. " +
                "You're going to tell them."
        else
            "🔍 You look at the light. You look at the town. You say nothing at all.")
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class FinaleOutcome(
        val branch: String,          // "truth" | "silent" | "town"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the branch, stamp
     *  the arc RETIRED forever, and hand the wrap its words.
     *  A "truth" choice without the clues to back it (impossible
     *  through the UI — the choice never appears) degrades to
     *  the town's ending rather than a free solve. */
    fun resolveFinale(state: GameState): FinaleOutcome {
        val clues = clueCount(state)
        val branch = when {
            state.arcChoice == "truth" && clues >= TRUTH_MIN_CLUES -> "truth"
            state.arcChoice == "silent" && clues >= TRUTH_MIN_CLUES -> "silent"
            else -> "town"
        }
        state.arcBranch = branch
        state.arcRetired = true
        return when (branch) {
            "truth" -> FinaleOutcome(branch,
                "You told them. It was Professor Chaos — one small boy, a bike helmet in " +
                    "foil, and a blanket fort behind the pond hut. The town is deflated, " +
                    "grounded-adjacent, and weirdly proud.",
                listOf(
                    "🔍 THE TRUTH: The lights were a foil-wrapped bike helmet on night " +
                        "'treasure walks'. The missing things were requisitions for a " +
                        "Fortress of Doom. The culprit has accepted one month of grounding " +
                        "with dignity.",
                    "🔍 At the pond, Butters looked at you — only you — and mouthed: " +
                        "'thanks for doing it gentle.'"))
            "silent" -> FinaleOutcome(branch,
                "You said nothing. The town banned the Pond Aliens by emergency decree and " +
                    "declared total victory. Somewhere, a helmet goes back into a closet — " +
                    "and its owner sleeps like a baby.",
                listOf(
                    "🔍 THE SECRET: You could have ended it in one sentence. You let them " +
                        "have the aliens instead. It cost the town one framed decree and " +
                        "gained it a legend.",
                    "🔍 Some weeks later, a juice box will appear on your doorstep. No note. " +
                        "You'll know."))
            else -> FinaleOutcome(branch,
                "The town voted: aliens. The lights stopped that week (a mother, somewhere, " +
                    "quietly found the batteries), and South Park counts it a triumph of " +
                    "municipal government.",
                listOf(
                    "🔍 THE OFFICIAL STORY: The Pond Aliens were banned, and — the record " +
                        "will show — they obeyed. The pamphlet gets a final chapter. The truth " +
                        "gets a smaller, warmer room to live in: your head.",
                    "🔍 Nobody ever found the Fortress of Doom. It was decommissioned the " +
                        "same week, its treasures returned to a lost-and-found nobody checks."))
        }
    }

    /** The branch's faction ripples + its permanent extra mark.
     *  Called by the wrap BEFORE the episode's credits are built,
     *  so the recap's faction-shift diff includes them. */
    fun applyFinaleEffects(state: GameState, branch: String) {
        fun ripple(factionId: String, delta: Int, note: String,
                   relationWith: String? = null, relationDelta: Int = 0) {
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
            if (relationWith != null && relationDelta != 0) {
                val k = Factions.relationKey(factionId, relationWith)
                val cur = state.factionRelations[k] ?: 0
                state.factionRelations[k] = (cur + relationDelta).coerceIn(-2, 2)
            }
        }
        when (branch) {
            "truth" -> {
                ripple("boys_crew", 4,
                    "The Boys' Crew ran the case that cracked the pond lights.")
                ripple("church_faithful", -4,
                    "The miracle was a helmet. The Church processes this quietly.")
                ripple("parents", 3,
                    "The Parents are relieved it was only ever a kid in the dark.",
                    relationWith = null)
                state.factionRelations[Factions.relationKey("boys_crew", "girls_circle")] =
                    ((state.factionRelations[Factions.relationKey("boys_crew", "girls_circle")]
                        ?: 0) + 1).coerceIn(-2, 2)
                state.lifeText["mark:ButtersHouse"] =
                    "A blanket fort behind the pond hut was quietly dismantled here. " +
                        "The Fortress of Doom is a lawn now. One month."
                townLogLine(state, "🔍 SOLVED: the pond lights were Professor Chaos. " +
                    "One small boy, one helmet, one month of grounding.")
            }
            "silent" -> {
                ripple("church_faithful", 2,
                    "The Church blesses the town's great victory over the Pond Aliens.")
                ripple("boys_crew", 2,
                    "The Boys' Crew accepts the town's thanks for a mystery well survived.")
                ripple("parents", 1, "")
                state.lifeText["mark:CityHall"] =
                    "THE POND ALIEN BAN is framed here: 'They are not to return.' " +
                        "Below it, in pencil, a small hand has added: 'sorry'."
                townLogLine(state, "🔍 CASE CLOSED (officially): the Pond Aliens were " +
                    "banned and never returned. You were there. You know.")
            }
            else -> {
                ripple("church_faithful", 6,
                    "Candle sales and civic pride: the Church calls the ban a triumph.")
                ripple("boys_crew", -2,
                    "The Boys' Crew's investigation was 'aliens' all along, apparently.")
                ripple("goth_kids", 2,
                    "The Goth Kids called 'kid in a helmet' in week one, as a joke. " +
                        "They are furious it was right.")
                state.lifeText["mark:CityHall"] =
                    "THE POND ALIEN BAN is framed here, with a small gold star: " +
                        "'Banned this day. Obeyed immediately.'"
                townLogLine(state, "🔍 CASE CLOSED: the town banned the Pond Aliens. " +
                    "They left at once. It is town history now.")
            }
        }
    }

    // ---------------- the Journal board ----------------

    /** The mystery page shows once the case exists: a chapter
     *  has aired, a clue is in hand, or the case is closed. */
    fun boardVisible(state: GameState): Boolean =
        state.arcStarted || state.mysteryClues.isNotEmpty() || state.arcRetired

    /** What the town currently believes, by where the case is. */
    fun currentTheory(state: GameState): String = when {
        state.arcRetired && state.arcBranch == "truth" ->
            "CLOSED. It was one small boy, a bike helmet wrapped in foil, and a blanket " +
                "fort. The pamphlet is a collector's item now."
        state.arcRetired ->
            "CLOSED. The aliens were formally banned and left immediately. It is in the " +
                "town history. It will be taught."
        !state.arcStarted ->
            "Nothing yet. The pond is just a pond. Probably."
        state.arcChapter <= 1 ->
            "Something's up there. Nobody agrees what. Jimbo has begun a pamphlet."
        state.arcChapter == 2 ->
            "ALIENS. The pamphlet is in its second printing."
        state.arcChapter == 3 ->
            "Three camps and no surrender: aliens (Jimbo), a gnome franchise (Tweek), " +
                "a drone ad campaign (Kyle). The gnome camp has merch."
        state.arcChapter == 4 ->
            "It's an omen now. Father Maxi is selling candles 'for the vigil'. " +
                "The vigil is whenever."
        else ->
            "Tonight's the stakeout. By morning, this town gets an answer — or picks one."
    }

    fun statusStamp(state: GameState): String = when {
        state.arcRetired && state.arcBranch == "truth" -> "✅ SOLVED — by you"
        state.arcRetired && state.arcBranch == "silent" -> "🤫 CLOSED — you know the truth"
        state.arcRetired -> "📜 CLOSED — official town history"
        state.arcChapter >= ARC_CHAPTERS.size -> "🔍 THE FINAL WEEK"
        state.arcStarted -> "🔍 INVESTIGATING — chapter ${state.arcChapter} of ${ARC_CHAPTERS.size} aired"
        else -> "🔍 A CASE IS COMING"
    }

    /** The whole clue board as plain lines (the Journal renders
     *  them; the suite reads them). */
    fun boardLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $ARC_TITLE")
        out.add(statusStamp(state))
        out.add("📖 CHAPTERS")
        ARC_CHAPTERS.forEachIndexed { i, def ->
            // arcChapter is the NEXT chapter index to air (bumped
            // at each chapter's start), so "aired" includes the
            // one on today; the airing check wins the ▶.
            val airing = state.shenaniganId == def.id
            val aired = state.arcChapter > i
            val mark = when { airing -> "▶"; aired -> "✓"; else -> "·" }
            val label = when { airing -> "AIRING NOW"; aired -> "aired"; else -> "not yet" }
            out.add("$mark Chapter ${i + 1}: ${def.title} — $label")
        }
        out.add("🧩 CLUES — ${clueCount(state)}/${CLUES.size}" +
            if (!state.arcRetired && clueCount(state) < TRUTH_MIN_CLUES)
                " (the truth needs $TRUTH_MIN_CLUES)" else "")
        for (clue in CLUES) {
            if (clue.id in state.mysteryClues) {
                out.add("• ${clue.title} — ${clue.detail}")
            }
        }
        val missing = CLUES.size - clueCount(state)
        if (missing > 0 && !state.arcRetired) {
            out.add("• ??? — $missing still out there. Talk to people. Look at places. " +
                "Get your hands dirty when the town gathers.")
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory(state)}")
        if (state.arcRetired) {
            out.add(when (state.arcBranch) {
                "truth" -> "🏁 You presented the truth at the pond. It was Professor Chaos. " +
                    "The case is closed and the fort is a lawn."
                "silent" -> "🏁 You knew, and you let them have the aliens. Some secrets " +
                    "are a kindness. This one wears a foil helmet."
                else -> "🏁 The town solved it the town's way. The ban holds. The pond is quiet."
            })
        }
        return out
    }

    // ---------------- the long tail (Threads-style callbacks) ----------------

    private val CALLBACKS_TRUTH = listOf(
        "Still can't believe the pond lights were just Butters. The helmet really sold it.",
        "The List of Missing Things is framed now. Wendy charges kids a quarter to see it.",
        "Professor Chaos has RETIRED, apparently. There was a ceremony. There shouldn't " +
            "have been, but there was.",
        "Jimbo's pamphlet has a corrections page now. It's longer than the pamphlet."
    )

    private val CALLBACKS_BELIEVERS = listOf(
        "We showed those pond aliens, huh. Banned 'em right out of the sky.",
        "The pond's been quiet since the ban. You're welcome, by the way. The town did that.",
        "I still leave a juice box out by the pond. You know. In case. Relations.",
        "My cousin says the whole thing was a kid in a helmet. My cousin also 'saw' the " +
            "lights, so. Grain of salt."
    )

    /** A post-case ambient aside in the town's own voice — the
     *  ending the town believes in is the one it keeps telling.
     *  Null until the case is closed. */
    fun ambientCallback(state: GameState, rnd: Random): String? {
        if (!state.arcRetired) return null
        val bank = if (state.arcBranch == "truth") CALLBACKS_TRUTH else CALLBACKS_BELIEVERS
        return bank[rnd.nextInt(bank.size)]
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Stories — relationship storylines (v0.3.0c, Queue #3 #3).
//
// Five multi-day arcs for the town's key pairs, told through
// the machinery the town already has — goals/threads/episodes
// style state, ticker beats, Journal pages, agent memories,
// npcBonds and friendship — NOT a parallel system:
//
//  - ARCS run one at a time, in order, each gated by a minimum
//    absDay so a fresh town meets them spaced out. An arc is
//    3-4 stages; a stage is a beat with dialogue the player
//    can witness by talking to the people in it.
//  - A stage ends at midnight: after TWO days on its own, or
//    after ONE day if the player NUDGED it — talked to (or
//    gifted, or took a selfie with) every core person in the
//    arc during that stage. Progress is never gated behind a
//    single missable interaction: left alone, every arc still
//    resolves — the town sorts its own heart out, slightly
//    differently.
//  - Endings are branching-lite: two outcome flavors per arc.
//    The warm one lands when the player showed up (nudged at
//    least half the stages) or the pair's own bond was already
//    strong; the solo one lands otherwise. Both STICK: the
//    pair's bond moves, a relation-note override is written
//    (the Journal reads it), agent memories keep the beat, and
//    the pair's ambient chatter gains callback lines in the
//    outcome's own voice.
//  - The mystery arc is a different beast and is NOT touched:
//    no ShenaniganDefs here, nothing joins SHENANIGAN_BY_ID.
//
// PURE Kotlin (no android imports): the Engine's midnight
// rollover drives the clock, GameActivity surfaces the talk
// beats, the Journal renders Stories.pageLines. All text is
// newly written. Ned speaks only through his voice box — in
// stage lines, talk lines and callbacks alike (suite-pinned).
// ============================================================

data class ArcStage(
    val title: String,
    val banner: String,                    // ticker line when the stage begins
    val journalNote: String,               // what the Journal says while it runs
    val objective: String,                 // "talk to X & Y" style nudge text
    val locId: String?,                    // where the beat lives (flavor)
    val lines: List<Line> = emptyList(),   // the beat itself (witness flavor)
    val talkLines: Map<String, String> = emptyMap() // per-person line when talked to this stage
)

data class ArcOutcome(
    val id: String,                        // "warm" | "solo" (per-arc flavor ids below)
    val title: String,                     // one-line outcome headline for the Journal
    val text: String,                      // the outcome paragraph
    val relationNote: String,              // the pair's new relation note (permanent)
    val bondDelta: Int,                    // npcBonds delta for the primary pair(s)
    val callbacks: List<Line>              // ambient callbacks, speaker-tagged, >= 2
)

data class StoryArcDef(
    val id: String,
    val title: String,
    val blurb: String,
    val parties: List<String>,             // the pair (or, for Heidi, the solo lead)
    val supporting: List<String> = emptyList(),
    /** Everyone the player must nudge in a stage to speed it. */
    val core: List<String>,
    val minAbsDay: Int,                    // earliest day the arc may open
    val stages: List<ArcStage>,            // 3-4
    val warm: ArcOutcome,
    val solo: ArcOutcome
) {
    /** The pairs whose bond + relation note the ending moves:
     *  the pair itself, or the solo lead against each supporter. */
    val primaryPairs: List<Set<String>>
        get() = if (parties.size >= 2) listOf(parties.toSet())
        else supporting.map { setOf(parties[0], it) }

    fun outcomeById(id: String): ArcOutcome = if (id == warm.id) warm else solo
}

data class StoryArcState(
    val arcId: String,
    var stage: Int = 0,
    var stageStartAbsDay: Int = 0,
    var startedAbsDay: Int = 0,
    var finishedAbsDay: Int = -1,
    var done: Boolean = false,
    var outcomeId: String = "",
    /** Stage indexes the player fully nudged (help credit). */
    val helpedStages: MutableSet<Int> = mutableSetOf(),
    /** People nudged during the CURRENT stage (reset per stage). */
    val stageNudges: MutableSet<String> = mutableSetOf(),
    /** People whose talk beat already played this stage. */
    val stageTalkShown: MutableSet<String> = mutableSetOf()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "arcId" to arcId, "stage" to stage,
        "stageStart" to stageStartAbsDay, "started" to startedAbsDay,
        "finished" to finishedAbsDay, "done" to done,
        "outcome" to outcomeId,
        "helped" to helpedStages.toList(),
        "nudges" to stageNudges.toList(),
        "shown" to stageTalkShown.toList()
    )

    companion object {
        fun fromMap(m: Map<String, Any?>): StoryArcState? {
            val id = m["arcId"]?.toString() ?: return null
            if (StoryArcs.ARC_BY_ID[id] == null) return null
            fun intOf(k: String, def: Int = 0): Int = (m[k] as? Number)?.toInt() ?: def
            fun strSet(k: String): MutableSet<String> =
                ((m[k] as? List<Any?>)?.map { it.toString() } ?: emptyList()).toMutableSet()
            fun intSet(k: String): MutableSet<Int> =
                ((m[k] as? List<Any?>)?.mapNotNull { (it as? Number)?.toInt() }
                    ?: emptyList()).toMutableSet()
            val def = StoryArcs.ARC_BY_ID[id]!!
            return StoryArcState(
                arcId = id,
                stage = intOf("stage").coerceIn(0, def.stages.size),
                stageStartAbsDay = intOf("stageStart"),
                startedAbsDay = intOf("started"),
                finishedAbsDay = intOf("finished", -1),
                done = m["done"] as? Boolean ?: false,
                outcomeId = m["outcome"]?.toString() ?: "",
                helpedStages = intSet("helped"),
                stageNudges = strSet("nudges"),
                stageTalkShown = strSet("shown")
            )
        }
    }
}

object StoryArcs {

    // ---------------- cadence (v0.3.0g: named, like Minigames' Feel) ----------------
    // Every timing/threshold number the storylines run on,
    // in one place; the values are the shipped v0.3.0c ones —
    // the v0.3.0g pins read these constants, so tuning an arc
    // is a one-line change with the suite watching.

    /** A stage the player fully nudged turns over at the
     *  next midnight. */
    const val STAGE_DAYS_NUDGED = 1

    /** A stage left to the town lives this many days. */
    const val STAGE_DAYS_SOLO = 2

    /** Warm ending, help path: the player nudged at least
     *  WARM_HELPED_NUM / WARM_HELPED_DEN of the arc's stages. */
    const val WARM_HELPED_NUM = 1
    const val WARM_HELPED_DEN = 2

    /** Warm ending, bond path: the pair's own bond already
     *  at this level when the last stage closes. */
    const val WARM_BOND_THRESHOLD = 6

    // ---------------- the five arcs ----------------

    val ARCS: List<StoryArcDef> = listOf(

        StoryArcDef(
            id = "stan_wendy_note",
            title = "The Note in the Locker",
            blurb = "Wendy wrote Stan a note. Stan has read half of it. " +
                "The other half is a town-wide incident waiting to happen.",
            parties = listOf("Stan", "Wendy"),
            supporting = listOf("Bebe", "Kyle"),
            core = listOf("Stan", "Wendy"),
            minAbsDay = 2,
            stages = listOf(
                ArcStage("The Note",
                    "💌 Wendy left a NOTE in Stan's locker. Stan has read the first half " +
                        "eleven times. The second half is face-down on his desk like a bomb.",
                    "A note exists. Stan is being brave about it in the worst order: " +
                        "panic first, reading second.",
                    "Talk to Stan and Wendy — somebody should finish reading that note.",
                    locId = "Elementary",
                    lines = listOf(
                        Line("Wendy", "It's a normal note. A friendly note. It says " +
                            "friendly things. He just has to READ it."),
                        Line("Stan", "I've read the first half. It's great. The second " +
                            "half could say anything. It could say 'also, spiders.'")),
                    talkLines = mapOf(
                        "Stan" to "Dude. The note. She wrote 'Dear Stan' and then — " +
                            "I panicked. Have YOU ever been Dear-Stan'd? It's a lot.",
                        "Wendy" to "Ask me what the note says and I'll tell you: it says " +
                            "I like him. That's the whole note. The suspense is HIS hobby, not mine.",
                        "Bebe" to "Okay so the note is ADORABLE and Stan is RUINING it " +
                            "by being Stan about it. Classic Stan.",
                        "Kyle" to "I've read the note. The note is fine. Stan's whole " +
                            "thing is the problem, as usual.")),
                ArcStage("The Misunderstanding",
                    "💌 RUMOR: the note was 'actually about Kyle'. The rumor's source: " +
                        "Cartman, who has not seen the note, the locker, or the truth.",
                    "One invented detail ('she dotted the i with a heart — for KYLE') " +
                        "has outrun the actual note, which remains unread past the fold.",
                    "Talk to Stan and Wendy before the rumor writes the ending.",
                    locId = "KidsPark",
                    lines = listOf(
                        Line("Stan", "People are saying the note's about Kyle?! KYLE " +
                            "can't even get a note. Kyle gets DETENTION slips."),
                        Line("Wendy", "I dotted ONE i. With a REGULAR dot. The heart is " +
                            "a lie told by a boy who eats paste-adjacent snacks.")),
                    talkLines = mapOf(
                        "Stan" to "Tell me the truth: is the heart thing real? Kyle " +
                            "says it isn't. Cartman says it is. I trust NO ONE.",
                        "Wendy" to "If one more person asks about the heart, I'm " +
                            "writing a SECOND note. To the whole school. About standards.",
                        "Bebe" to "The heart rumor? I may have repeated it. In my " +
                            "defense, it was EXTREMELY repeatable.",
                        "Kyle" to "For the record: not my note, not my heart, not my " +
                            "girlfriend. I have a math test. Leave me out of this.")),
                ArcStage("The Grand-ish Gesture",
                    "💌 Stan has a PLAN: a grand gesture at the park. Flowers, a speech, " +
                        "eye contact. His stomach has filed a formal objection.",
                    "The gesture is scheduled. The flowers are real. The speech is " +
                        "four words long and two of them are 'um'.",
                    "Talk to Stan and Wendy — the gesture could use witnesses.",
                    locId = "KidsPark",
                    lines = listOf(
                        Line("Stan", "Flowers: check. Speech: 'Wendy, I—' and then my " +
                            "brain clocks out. It's a two-word speech. It's mostly fine."),
                        Line("Wendy", "He's planning something. He gets a specific face. " +
                            "It's the face he makes before the fire alarm of romance.")),
                    talkLines = mapOf(
                        "Stan" to "If I throw up, catch the flowers. The flowers are " +
                            "the gesture. I'm just the delivery system. A nervous, green delivery system.",
                        "Wendy" to "Whatever he's planning: I already know. Bebe told " +
                            "me. I'm going to act surprised. That's MY gesture.",
                        "Bebe" to "I told Wendy EVERYTHING. Secrecy is a burden and I " +
                            "have put it down.",
                        "Kyle" to "I proofread the speech. It's two words and a noise. " +
                            "I approved it. It's perfect.")),
                ArcStage("Together (For Now)",
                    "💌 Today, at the park: a boy, a girl, a note finally read past " +
                        "the fold. The town holds its breath and its snacks.",
                    "Finale of the note saga: the second half says 'P.S. You're my " +
                        "favorite person to be annoyed at.' It always just said that.",
                    "Be around Stan and Wendy today — history is happening at recess scale.",
                    locId = "KidsPark",
                    lines = listOf(
                        Line("Wendy", "He read the whole note. Out loud. Including the " +
                            "P.S. Then he only threw up a LITTLE. Growth."),
                        Line("Stan", "We're good. We're like — GOOD good. I have the " +
                            "note in my pocket. It's basically a contract now.")),
                    talkLines = mapOf(
                        "Stan" to "We talked. Like people. I said the two words AND " +
                            "the noise. She said yes to all of it. I'm keeping the note forever.",
                        "Wendy" to "Yes, we're together. Yes, the note worked. No, " +
                            "there will not be a sequel. …There might be a sequel.",
                        "Bebe" to "I witnessed a successful romance and I only " +
                            "interfered a LITTLE. Personal growth for everyone.",
                        "Kyle" to "Happy for them. Genuinely. Now can we PLEASE talk " +
                            "about literally anything else?"))
            ),
            warm = ArcOutcome(
                id = "stan_wendy_warm",
                title = "Together — and the note is in his pocket to prove it",
                text = "You ran messages, killed the heart rumor, and stood witness " +
                    "at the gesture. Stan read the note past the fold: together, " +
                    "for now — which, for Stan and Wendy, is a long-term plan.",
                relationNote = "on-again sweethearts (currently ON — the note worked)",
                bondDelta = 3,
                callbacks = listOf(
                    Line("Stan", "Wendy and me are good, by the way. The note's still " +
                        "in my pocket. It's load-bearing now."),
                    Line("Wendy", "Stan keeps that note like it's a passport. It's a " +
                        "piece of notebook paper. …I check that he has it. Every day."))),
            solo = ArcOutcome(
                id = "stan_wendy_solo",
                title = "Together — eventually, the long way round",
                text = "Nobody nudged it, so it took the scenic route: two more days " +
                    "of rumor, one hallway summit, and Wendy finally reading the " +
                    "note ALOUD to Stan, in public, at reading-voice volume. Together.",
                relationNote = "on-again off-again sweethearts (sorted it out themselves, eventually)",
                bondDelta = 1,
                callbacks = listOf(
                    Line("Stan", "Wendy read the note TO me. In front of people. I " +
                        "think that's legally a wedding in some states."),
                    Line("Wendy", "Next time I write a note, I'm attaching a reading " +
                        "guide. And a sedative. For him.")))
        ),

        StoryArcDef(
            id = "craig_tweek_pressure",
            title = "Too Much Pressure",
            blurb = "Tweek Bros is demoing a cup-stacking routine for the school fair. " +
                "Tweek has to perform it. In front of EVERYONE. Craig has opinions.",
            parties = listOf("Craig", "Tweek"),
            supporting = listOf("Mr. Tweek", "Clyde"),
            core = listOf("Craig", "Tweek"),
            minAbsDay = 6,
            stages = listOf(
                ArcStage("The Spiral Starts",
                    "☕ Tweek has been signed up to stack cups at the fair. Tweek has " +
                        "stacked zero cups. Tweek is stacking ANXIETY instead. It's in a pyramid.",
                    "The demo is days away. Every cup in the coffeehouse has been " +
                        "practiced on, dropped, and apologized to.",
                    "Talk to Tweek and Craig — the spiral has a counterweight, and it's Craig.",
                    locId = "TweekBros",
                    lines = listOf(
                        Line("Tweek", "CUP STACKING. IN PUBLIC. What if I drop one?! " +
                            "What if I drop ALL of them?! What if the cups are GNOMES?!"),
                        Line("Craig", "He's fine. He's loud-fine. There's a difference. " +
                            "I'm handling it.")),
                    talkLines = mapOf(
                        "Tweek" to "GAAAH! The fair people want SPEED. And ACCURACY. " +
                            "And an AUDIENCE. Three pressures! That's the maximum pressures!",
                        "Craig" to "Don't tell him I said this: he's actually good at " +
                            "it. His hands are fast. It's his brain that's loud.",
                        "Mr. Tweek" to "My boy stacks like the wind when nobody watches. " +
                            "The watching is the whole ingredient we're missing.",
                        "Clyde" to "I cried watching him practice. Happy tears AND " +
                            "regular tears. He dropped a cup and it was BEAUTIFUL.")),
                ArcStage("The Town Watches",
                    "☕ Practice has an audience now. Everyone in town has stacking " +
                        "advice. All of it is terrible. All of it is loud.",
                    "Advice received so far: 'just relax' (useless), 'stack faster' " +
                        "(worse), and one breathing tip from Jesus, who meant well.",
                    "Talk to Craig and Tweek — maybe filter the town's wisdom a little.",
                    locId = "TweekBros",
                    lines = listOf(
                        Line("Tweek", "EVERYONE KEEPS SAYING RELAX. HAS RELAXING EVER " +
                            "WORKED BECAUSE SOMEONE YELLED IT?!"),
                        Line("Craig", "New rule: nobody talks to Tweek before the fair " +
                            "except me. I'm the firewall now.")),
                    talkLines = mapOf(
                        "Tweek" to "Craig banned advice. He stands by the door. A " +
                            "bouncer. For feelings. It's — GAAAH — it's HELPING?!",
                        "Craig" to "I have a method. It's called: one cup. One breath. " +
                            "Everyone else can stack their opinions somewhere else.",
                        "Mr. Tweek" to "Craig installed himself as door security for my " +
                            "son's nerves. I put him on the payroll. In beans.",
                        "Clyde" to "Craig flipped off the whole advice line. Even Jesus. " +
                            "ESPECIALLY the concept of advice. It was beautiful.")),
                ArcStage("Craig's Method",
                    "☕ Rehearsal day: one cup, one breath. The coffeehouse is quiet " +
                        "for the first time in recorded history. The cups are listening.",
                    "The method is working in rehearsal — slow, flat, repeatable. " +
                        "Craig times it with a stopwatch and says 'fine' like it's a medal.",
                    "Talk to them both — tomorrow, the pyramid goes up in public.",
                    locId = "TweekBros",
                    lines = listOf(
                        Line("Craig", "See that? Twelve cups. Zero screams. One small " +
                            "scream. It doesn't count, it was a happy one."),
                        Line("Tweek", "One cup. One breath. One cup. One breath. …I " +
                            "can hear my own hands. They're CALM. That's new.")),
                    talkLines = mapOf(
                        "Tweek" to "The trick is Craig watches the DOOR, not me. So " +
                            "the pressure has nowhere to sit. It just — leaves. GAAAH. Wow.",
                        "Craig" to "Tomorrow I stand where he can see me and nowhere " +
                            "else matters. That's the method. Don't make it a thing.",
                        "Mr. Tweek" to "Twelve cups, steady as a table. I may have " +
                            "wept into the beans. Once. Professionally.",
                        "Clyde" to "I have a sign for tomorrow. It says 'STACK, KING'. " +
                            "Craig said I can hold it if I don't cry on it. Negotiating.")),
                ArcStage("The Fair",
                    "☕ FAIR DAY. The pyramid goes up in front of the whole town. " +
                        "One cup. One breath. One Craig, in the front, not blinking.",
                    "Whatever happens out there, it happens at stacking speed — " +
                        "and the whole coffeehouse family is front row.",
                    "Talk to Craig and Tweek today — be front row too.",
                    locId = "Elementary",
                    lines = listOf(
                        Line("Tweek", "I DID IT. THE PYRAMID. IT'S STANDING. I'M " +
                            "STANDING. EVERYTHING IS STANDING."),
                        Line("Craig", "Told you. …He did it. Whatever. He did it.")),
                    talkLines = mapOf(
                        "Tweek" to "TWELVE CUPS. ZERO DROPS. I looked up ONCE and " +
                            "Craig was just — there. Breathing. So I breathed. THAT'S THE WHOLE SECRET.",
                        "Craig" to "He stacked. I watched. Clyde cried on the sign. " +
                            "Perfect day, I guess. Shut up.",
                        "Mr. Tweek" to "My son, the pyramid builder. The beans are " +
                            "free today. For him. And for the quiet boy who guards him.",
                        "Clyde" to "I cried so much the sign is ruined. IT WAS WORTH " +
                            "IT. STACK, KING. STACK."))
            ),
            warm = ArcOutcome(
                id = "craig_tweek_warm",
                title = "Steadier — the pyramid stood, and so did he",
                text = "You kept the advice mob off him and ran Craig's bottled " +
                    "water. Twelve cups, zero drops: Tweek performed, Craig " +
                    "firewalled, and the method is now coffeehouse law.",
                relationNote = "boyfriends (steadier — Craig's one-cup method, patented)",
                bondDelta = 3,
                callbacks = listOf(
                    Line("Tweek", "Craig's method, man. One cup, one breath. Works " +
                        "on everything. Taxes. Gnomes. Tuesdays."),
                    Line("Craig", "Tweek's fine. Pyramid's still up at the shop. " +
                        "Nobody touch it. Especially the gnomes."))),
            solo = ArcOutcome(
                id = "craig_tweek_solo",
                title = "Steadier — via one dropped cup and a do-over",
                text = "Left alone, it still landed — messier: a first-run drop, a " +
                    "long silence, Craig walking out to the table and standing " +
                    "there until the second run went up clean. The town counts it double.",
                relationNote = "boyfriends (survived the fair their own way)",
                bondDelta = 1,
                callbacks = listOf(
                    Line("Tweek", "I dropped ONE cup. In front of EVERYONE. And then " +
                        "I did it AGAIN, the stacking, and it WORKED. Both things are true!"),
                    Line("Craig", "First try doesn't count. I decided. Craig rules. " +
                        "The second try was perfect and so was he. Shut up.")))
        ),

        StoryArcDef(
            id = "heidi_herself",
            title = "Heidi, Herself",
            blurb = "Heidi is starting a girls' circle advice night — hers, run her " +
                "way. For the record: this is not about Cartman. He is history. This is hers.",
            parties = listOf("Heidi"),
            supporting = listOf("Nichole", "Red", "Bebe"),
            core = listOf("Heidi", "Nichole", "Red", "Bebe"),
            minAbsDay = 10,
            stages = listOf(
                ArcStage("The New Leaf",
                    "🌸 Heidi has a PLAN: a girls' circle advice night at the mall — " +
                        "her idea, her list, her rules. Sign-ups are open and filling.",
                    "Heidi is doing a thing that is hers. The poster says " +
                        "'NO EX-BOYFRIENDS WILL BE DISCUSSED. THIS MEANS ERIC.'",
                    "Talk to Heidi and the girls — a new leaf needs witnesses.",
                    locId = "Mall",
                    lines = listOf(
                        Line("Heidi", "It's not a comeback. You can't come back to a " +
                            "place you never left. It's just — my thing. Finally."),
                        Line("Bebe", "Advice night! I call the chair with the cushion. " +
                            "Seniority. I invented seniority.")),
                    talkLines = mapOf(
                        "Heidi" to "I used to make myself smaller so somebody else " +
                            "could be the biggest person in the room. Advice night is " +
                            "the opposite of that. It's mine.",
                        "Nichole" to "Heidi's list is good. Real problems, no ranking " +
                            "boys, nobody's ex gets airtime. I checked it twice. It's clean.",
                        "Red" to "If Cartman shows up to her night I will personally " +
                            "escort him to the parking lot. Repeatedly. It's on my calendar.",
                        "Bebe" to "I'm bringing the good snacks and my loudest support. " +
                            "Both are extensive.")),
                ArcStage("The Wobble",
                    "🌸 Somebody is saying Heidi's advice night is 'just to get " +
                        "attention'. Somebody is wrong, loud, and owns a red coat.",
                    "An old script is trying to run: shrink, apologize, cancel. " +
                        "The girls have seen this episode and they brought edits.",
                    "Talk to Heidi and the girls — a wobble is not a verdict.",
                    locId = "Mall",
                    lines = listOf(
                        Line("Heidi", "He said it's 'for attention'. Like that's an " +
                            "insult. Like girls aren't allowed to want a room to look at them kindly."),
                        Line("Red", "I've drafted a statement. It's one sentence. " +
                            "It rhymes 'Eric' with 'generic mistake'.")),
                    talkLines = mapOf(
                        "Heidi" to "Old me would've cancelled. Old me did cancel — " +
                            "things. Dinners. Opinions. I'm not cancelling. Say it back to me?",
                        "Nichole" to "She almost folded at lunch. Then she re-read her " +
                            "own first advice column. To herself. Out loud. It worked.",
                        "Red" to "The statement rhymes. It's devastating. She asked " +
                            "me not to deliver it. GROWTH. I'll just think it at him, hard.",
                        "Bebe" to "I told her: attention from people who LIKE you " +
                            "isn't a crime, it's the whole point of a night. Put it on a poster.")),
                ArcStage("Her Own Footing",
                    "🌸 ADVICE NIGHT. The mall food court, a circle of chairs, and " +
                        "Heidi at the head of it — her name first on the sign, in her handwriting.",
                    "Whatever tonight proves, it proves it in her handwriting: " +
                        "the circle came, the advice was good, the ex was not mentioned once.",
                    "Talk to Heidi and the girls — be there for the first night.",
                    locId = "Mall",
                    lines = listOf(
                        Line("Heidi", "Nobody fixed me. Nobody rescued me. I just " +
                            "stopped leaving the room. Look — full chairs."),
                        Line("Nichole", "First night. Every chair filled. She ran it " +
                            "like she's been running it for years. She has. Quietly. For everyone else.")),
                    talkLines = mapOf(
                        "Heidi" to "I keep waiting to feel like a fraud. Instead I " +
                            "feel like — me, at full volume. Is this what it's supposed to feel like?",
                        "Nichole" to "She opened with the wobble story. Told it " +
                            "herself, as a joke, and the room laughed WITH it. That's the whole win.",
                        "Red" to "No Eric incidents. I almost missed escorting him. " +
                            "Almost. Her night was better. Fine. BETTER.",
                        "Bebe" to "Advice night is OFFICIALLY a thing. I have already " +
                            "scheduled my feelings for next week."))
            ),
            warm = ArcOutcome(
                id = "heidi_warm",
                title = "Her own footing — first night, full chairs",
                text = "You showed up for all of it: the poster, the wobble, the " +
                    "first night. Heidi ran her circle, told the wobble story as a " +
                    "joke, and did not shrink once. The ex was not mentioned. Growth.",
                relationNote = "girls' circle — Heidi leading it herself now",
                bondDelta = 2,
                callbacks = listOf(
                    Line("Heidi", "Advice night's a standing thing now. My thing. " +
                        "People keep asking when I got so confident. I say: I didn't. I just stayed."),
                    Line("Bebe", "Heidi runs the circle now. I hold the cushion chair " +
                        "BY HER INVITATION. There's a difference. She taught me that."))),
            solo = ArcOutcome(
                id = "heidi_solo",
                title = "Her own footing — quieter, and still hers",
                text = "It landed the quiet way: the first night was small, the " +
                    "wobble nearly won a postponement, and then Nichole read the " +
                    "poster aloud at lunch and six more girls signed up on the spot.",
                relationNote = "girls' circle (Heidi found her footing, with less help)",
                bondDelta = 1,
                callbacks = listOf(
                    Line("Heidi", "Small first night. That's fine. Small is still " +
                        "a night. Still mine."),
                    Line("Red", "I still have the Eric statement. Laminated now. " +
                        "For emergencies. And slow days.")))
        ),

        StoryArcDef(
            id = "randy_sharon_line",
            title = "The Line in the Sand",
            blurb = "Randy's new scheme: rebrand the school fundraiser as a Tegridy " +
                "event. Sharon has found the college-fund jar. There will be a meeting.",
            parties = listOf("Randy", "Sharon"),
            supporting = listOf("Stan", "Towelie"),
            core = listOf("Randy", "Sharon"),
            minAbsDay = 14,
            stages = listOf(
                ArcStage("Scheme of the Week",
                    "🌿 Randy has 'synergized' the school fundraiser with Tegridy. " +
                        "There are banners. There is a MERCH booth. The school has questions.",
                    "The scheme's math, per Randy: the fundraiser raises money, " +
                        "the brand raises the fundraiser, everybody wins. Sharon is reading the fine print.",
                    "Talk to Randy and Sharon — the fine print has a jar in it.",
                    locId = "TegridyFarms",
                    lines = listOf(
                        Line("Randy", "It's not a takeover, it's a SYNERGY. The kids " +
                            "learn business! The business learns kids! Sharon'll love it."),
                        Line("Sharon", "Stanley. Family meeting. Tonight. Your father " +
                            "has synergized again.")),
                    talkLines = mapOf(
                        "Randy" to "Okay so the seed money came from a jar. A jar is " +
                            "not a budget, it's a SUGGESTION with a lid. Tell Sharon that. Gently.",
                        "Sharon" to "The jar says COLLEGE on it. In Stan's handwriting. " +
                            "From when he was six. Randy used it as 'seed capital'. His words. Not mine.",
                        "Stan" to "Dude. My college jar. I wrote COLLEGE on it in " +
                            "crayon. CRAYON. That's legally binding in kid law.",
                        "Towelie" to "I folded the banners. So many banners. Nobody " +
                            "pay me for this? I work for towels. I AM a towel.")),
                ArcStage("The Line",
                    "🌿 Sharon has drawn THE LINE: the jar is refilled by Friday, " +
                        "the merch booth becomes a bake sale, or the farm's parade float gets sold.",
                    "The line is in the sand, the sand is in the driveway, and the " +
                        "float — Randy's beloved Tegridy parade float — is on it.",
                    "Talk to them both — a line only works if both sides can see it.",
                    locId = "StanHouse",
                    lines = listOf(
                        Line("Sharon", "Friday. Jar full, booth honest, or the float " +
                            "goes. I love him. That's why it's Friday and not today."),
                        Line("Randy", "She can't sell the float. The float is my " +
                            "SOUL with wheels. …She can. She absolutely can. Sharon!!")),
                    talkLines = mapOf(
                        "Randy" to "You don't get it — the float WON 'Most Float' " +
                            "three years running. There was no other float. STILL COUNTS.",
                        "Sharon" to "I'm not the villain of his movie. I'm the " +
                            "accountant of his life. It's a harder job and nobody claps.",
                        "Stan" to "Mom's right and Dad knows it, which is why he's " +
                            "being loud in the garage. That's his thinking noise.",
                        "Towelie" to "Tension at the farm, man. I folded myself into " +
                            "a small square to be less noticeable. Didn't work.")),
                ArcStage("The Cost",
                    "🌿 Randy has listed his options on the garage wall: sell the " +
                        "float, sell the spare tractor, or — circled, underlined — 'be honest at the meeting'.",
                    "The honest option costs the most, and Randy knows it: stand " +
                        "up at the school meeting and say where the seed money came from. Out loud.",
                    "Talk to Randy and Sharon — honesty appreciates a witness.",
                    locId = "StanHouse",
                    lines = listOf(
                        Line("Randy", "If I say it at the meeting, Sheila records it. " +
                            "Sheila records EVERYTHING. It'll be in a newsletter. A NEWSLETTER."),
                        Line("Sharon", "He's circling the honest one. He's been " +
                            "circling it for a day. Randy circles things before he lands. Like a plane. A loud plane.")),
                    talkLines = mapOf(
                        "Randy" to "The float or my dignity in a newsletter. Why is " +
                            "the right choice always the embarrassing one? WHY.",
                        "Sharon" to "Whatever he picks, the jar comes back Friday. " +
                            "That's not the negotiation. The negotiation is only about what it costs him.",
                        "Stan" to "He's actually gonna do the honest one. I can tell. " +
                            "He's practicing in the mirror. There were gestures.",
                        "Towelie" to "I hid the float keys. Then I forgot where. Then " +
                            "I remembered I don't have pockets. Mystery solved: nobody had the keys.")),
                ArcStage("The Compromise",
                    "🌿 MEETING NIGHT at the school. The jar is on the table. The " +
                        "float keys are in Randy's hand. The newsletter lady is present.",
                    "The compromise, whatever shape it takes, will cost Randy " +
                        "something real — and the jar goes home full tonight.",
                    "Talk to Randy and Sharon — be there when it lands.",
                    locId = "Elementary",
                    lines = listOf(
                        Line("Randy", "I said it. Out loud. 'I borrowed from my kid's " +
                            "college jar for banners.' Sheila wrote it down VERY fast."),
                        Line("Sharon", "Jar's full. Booth's a bake sale. He's " +
                            "grounded from synergy. And he kept the float — barely. We're okay. We're actually okay.")),
                    talkLines = mapOf(
                        "Randy" to "I kept the float AND Sharon. Cost me a public " +
                            "apology and my dignity's resale value. Worth it. Mostly. The jar's full, kid.",
                        "Sharon" to "He did the hard one. In public. With Sheila's " +
                            "newsletter present. I am — don't tell him — a little bit proud of the idiot.",
                        "Stan" to "My jar is full and Dad's only MOSTLY humiliated. " +
                            "Best family meeting ever. Low bar. Cleared it.",
                        "Towelie" to "The bake sale sold out, man. Towelie-brand " +
                            "brownies. No — just brownies. Regular brownies. Why are you looking at me like that?"))
            ),
            warm = ArcOutcome(
                id = "randy_sharon_warm",
                title = "The compromise — jar full, float kept, dignity dented",
                text = "You carried messages and stood in the room. Randy chose " +
                    "the honest option at the meeting, refilled the jar himself, " +
                    "and kept the float by a nose. Sharon's line held — and so did they.",
                relationNote = "married (somehow — and this week, honestly)",
                bondDelta = 3,
                callbacks = listOf(
                    Line("Sharon", "Randy refilled the jar himself. Voluntarily. I " +
                        "framed the meeting minutes. The frame says NICE TRY, FUTURE RANDY."),
                    Line("Randy", "Kept the farm, kept Sharon, kept the float. Cost " +
                        "me one newsletter apology. I read it every morning. As a warning."))),
            solo = ArcOutcome(
                id = "randy_sharon_solo",
                title = "The compromise — at float-point",
                text = "Without a nudge it went to the wire: Friday came, the float " +
                    "was halfway listed for sale, and Randy refilled the jar at " +
                    "the last hour with Towelie's banner money and a speech nobody asked for.",
                relationNote = "married (somehow — the float survived; Sharon's patience is on a payment plan)",
                bondDelta = 1,
                callbacks = listOf(
                    Line("Randy", "The float lives. I live. Coincidence? I " +
                        "refilled a jar at midnight, that's why."),
                    Line("Sharon", "He made the deadline by eleven minutes. Eleven. " +
                        "I counted. I always count. That's the marriage.")))
        ),

        StoryArcDef(
            id = "jimbo_ned_hunt",
            title = "The Quiet Hunt",
            blurb = "The annual hunt is on — but Ned got a letter from his sister in " +
                "Florida, and Jimbo's knee has opinions. The hunt was never about the hunt.",
            parties = listOf("Jimbo", "Ned"),
            supporting = listOf("Stan", "Stuart"),
            core = listOf("Jimbo", "Ned"),
            minAbsDay = 18,
            stages = listOf(
                ArcStage("The Annual Hunt",
                    "🦌 The ANNUAL HUNT is announced at Jimbo's Guns. Permits ready, " +
                        "Ned's voice box charged, the ridge waiting like always.",
                    "It's the same hunt they've done for thirty years, which is " +
                        "the point of it — though Ned's been quiet, even for a man with a voice box.",
                    "Talk to Jimbo and Ned — some hunts you join by listening.",
                    locId = "JimbosGuns",
                    lines = listOf(
                        Line("Jimbo", "Thirty years, same ridge, same Ned. Nothing " +
                            "out there is safe, and neither are we. Tradition!"),
                        Line("Ned", "*voice box* Mmm. Ridge. Good. *click* Battery's " +
                            "fine. I'm fine. We're going.")),
                    talkLines = mapOf(
                        "Jimbo" to "Ned's been looking at that Florida letter all " +
                            "week. His sister's got a porch and no winters. A PORCH, kid.",
                        "Ned" to "*voice box* Letter's just a letter. Sister writes " +
                            "every year. This year I read it twice. Mmm.",
                        "Stan" to "Uncle Jimbo's worried. He does worry as yelling. " +
                            "He yelled about the weather for an hour. It wasn't about the weather.",
                        "Stuart" to "Those two been hunting since before you were a " +
                            "rumor. Ned goes to Florida, Jimbo's just a guy with a shop.")),
                ArcStage("What's Actually Wrong",
                    "🦌 Jimbo's knee has gone loud, Ned's letter is on its third " +
                        "read, and the hunt is in two days. Neither of them has mentioned either thing.",
                    "Two men, two unmentionables, one ridge. The shop is very " +
                        "loud with all the things not being said in it.",
                    "Talk to them both — the unsaid has excellent hearing.",
                    locId = "JimbosGuns",
                    lines = listOf(
                        Line("Jimbo", "It's not the knee. A knee is a knee. …It's " +
                            "the idea he might not be next to me on the ridge. Don't print that."),
                        Line("Ned", "*voice box* Not about hunting. Never was. He " +
                            "knows. I know. Ridge knows. Mmm.")),
                    talkLines = mapOf(
                        "Jimbo" to "Florida's warm. Porch. No winters. His sister's " +
                            "pool has a RAMP. I can't compete with a ramp, kid. I got a shop and a knee.",
                        "Ned" to "*voice box* He thinks I'd pick a porch over the " +
                            "ridge. Thirty years and he still underrates the ridge. And himself. Mmm.",
                        "Stan" to "They're both just — sad at each other, in advance, " +
                            "for no reason yet. Adults are exhausting.",
                        "Stuart" to "Jimbo without Ned is a bell without a clapper. " +
                            "Don't tell him I said something that pretty.")),
                ArcStage("The Last Ridge",
                    "🦌 HUNT DAY. They go up the ridge slow — Jimbo's knee setting " +
                        "the pace, Ned not minding it, the letter folded in a vest pocket.",
                    "Slow is still up. The ridge doesn't care how long it takes; " +
                        "it has been a ridge for a while and plans to continue.",
                    "Talk to Jimbo and Ned — today the pace is the point.",
                    locId = "TegridyFarms",
                    lines = listOf(
                        Line("Jimbo", "We didn't shoot a thing. Best hunt in years. " +
                            "Don't tell the shop. The shop expects numbers."),
                        Line("Ned", "*voice box* Saw a deer. Deer saw us. We all " +
                            "agreed to take the day off. Professional courtesy. Mmm.")),
                    talkLines = mapOf(
                        "Jimbo" to "He tore the letter up at the top. Not angry — " +
                            "done. Said the porch can wait till we're old. We ARE old. He laughed. The box laughed.",
                        "Ned" to "*voice box* Porch later. Ridge now. Sister " +
                            "understood. She wrote back one word: 'obviously'. Mmm. Family.",
                        "Stan" to "They came down slower than they went up and " +
                            "somehow happier. I don't get it. I hope I get it someday.",
                        "Stuart" to "Knew it. Thirty years ain't a habit, it's a " +
                            "load-bearing wall. You don't move those to Florida.")),
                ArcStage("What the Hunt Was",
                    "🦌 Back at the shop: the hunt is logged, the kettle's on, and " +
                        "Ned has already penciled NEXT year's date on the shop calendar.",
                    "The calendar has a new entry in Ned's handwriting: same " +
                        "ridge, same fools. The letter is answered. The knee is forgiven.",
                    "Talk to Jimbo and Ned — some endings are just a calendar entry.",
                    locId = "JimbosGuns",
                    lines = listOf(
                        Line("Jimbo", "Next year's on the calendar. He wrote it " +
                            "himself. In pen. PEN. You don't come back from pen."),
                        Line("Ned", "*voice box* Mmm. Next year. And the year " +
                            "after. Box'll need batteries. I'll manage. *click* We manage.")),
                    talkLines = mapOf(
                        "Jimbo" to "Thirty-one years next year. If his sister " +
                            "wants him, she can visit. There's a ridge. She can watch us not shoot it.",
                        "Ned" to "*voice box* Best lines I ever said, I said on " +
                            "that ridge. Most of 'em were 'mmm'. He heard all of it. Mmm.",
                        "Stan" to "Uncle Jimbo put the torn letter in a frame. A " +
                            "FRAME. It just says 'obviously' now. It's art, I guess.",
                        "Stuart" to "They're fine. Course they're fine. Was never " +
                            "a world where they weren't."))
            ),
            warm = ArcOutcome(
                id = "jimbo_ned_warm",
                title = "What the hunt was — next year's date is in pen",
                text = "You listened at the right counter at the right times. Ned " +
                    "tore the letter up on the ridge, Jimbo heard every 'mmm' of " +
                    "it, and next year is on the shop calendar. In pen.",
                relationNote = "old hunting partners (closer than brothers — the hunt continues, slower)",
                bondDelta = 3,
                callbacks = listOf(
                    Line("Jimbo", "Ned penciled— no, PENNED next year on that " +
                        "calendar. Thirty-one years. The ridge ain't ready."),
                    Line("Ned", "*voice box* Mmm. Ridge. Next year. Wouldn't " +
                        "miss it. Battery's charged. So am I. Mmm."))),
            solo = ArcOutcome(
                id = "jimbo_ned_solo",
                title = "What the hunt was — decided in a look, as usual",
                text = "Nobody carried a single message, and it didn't matter: on " +
                    "the ridge, Jimbo said 'Florida, huh' and Ned said 'mmm', and " +
                    "that was the entire negotiation — thirty years long, four words wide.",
                relationNote = "old hunting partners (the hunt continues — it was never in doubt)",
                bondDelta = 1,
                callbacks = listOf(
                    Line("Jimbo", "Whole Florida crisis took four words to settle. " +
                        "That's not a short friendship. That's an EFFICIENT one."),
                    Line("Ned", "*voice box* Porch can visit me. I'm not hard to " +
                        "find. Ridge. Shop. Jimbo. Mmm.")))
        )
    )

    val ARC_BY_ID: Map<String, StoryArcDef> = ARCS.associateBy { it.id }

    // ---------------- state lookup ----------------

    fun record(state: GameState, arcId: String): StoryArcState? = state.storyArcs[arcId]

    /** The arc currently on stage (record exists, not done). */
    fun activeDef(state: GameState): StoryArcDef? =
        ARCS.firstOrNull { state.storyArcs[it.id]?.done == false }

    fun activeRecord(state: GameState): StoryArcState? =
        activeDef(state)?.let { state.storyArcs[it.id] }

    fun allDone(state: GameState): Boolean =
        ARCS.all { state.storyArcs[it.id]?.done == true }

    /** Stage length in days: nudged stages turn over tomorrow,
     *  left-alone stages the day after. */
    fun stageDays(rec: StoryArcState): Int =
        if (rec.stage in rec.helpedStages) STAGE_DAYS_NUDGED
        else STAGE_DAYS_SOLO

    // ---------------- lifecycle (Engine.onNewDay calls dailyTick) ----------------

    /** One midnight for the storylines: finish the stage that's
     *  lived its length, open the next beat — or open the next
     *  arc entirely. Resolves everything unattended given enough
     *  days (the suite fast-forwards all five to prove it). */
    fun dailyTick(state: GameState) {
        val active = activeDef(state)
        if (active != null) {
            val rec = state.storyArcs[active.id]!!
            if (state.absDay - rec.stageStartAbsDay >= stageDays(rec)) {
                advance(state, active, rec)
            }
            return
        }
        // no arc on stage: open the next one whose gate has come,
        // at least a day after the last one closed.
        val next = ARCS.firstOrNull { it.id !in state.storyArcs } ?: return
        if (state.absDay < next.minAbsDay) return
        val lastFinish = state.storyArcs.values.maxOfOrNull { it.finishedAbsDay } ?: -1
        if (lastFinish >= 0 && state.absDay <= lastFinish) return
        startArc(state, next)
    }

    private fun startArc(state: GameState, def: StoryArcDef) {
        val rec = StoryArcState(
            arcId = def.id,
            stage = 0,
            stageStartAbsDay = state.absDay,
            startedAbsDay = state.absDay
        )
        state.storyArcs[def.id] = rec
        state.events.add("📖 NEW STORY: '${def.title}' — ${def.blurb}")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "📖 ${def.title} begins")
        state.events.add(def.stages[0].banner)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    private fun advance(state: GameState, def: StoryArcDef, rec: StoryArcState) {
        if (rec.stage >= def.stages.size - 1) {
            finishArc(state, def, rec)
            return
        }
        rec.stage++
        rec.stageStartAbsDay = state.absDay
        rec.stageNudges.clear()
        rec.stageTalkShown.clear()
        state.events.add(def.stages[rec.stage].banner)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** Was the ending earned with the player in it? Nudging at
     *  least half the stages — or the pair's own bond already
     *  being strong (>= 6) — lands the warm flavor. */
    fun warmEarned(state: GameState, def: StoryArcDef, rec: StoryArcState): Boolean {
        if (rec.helpedStages.size * WARM_HELPED_DEN >=
            def.stages.size * WARM_HELPED_NUM) return true
        val bond = def.primaryPairs.maxOfOrNull {
            state.npcBonds[Threads.pairKey(it.first(), it.last())] ?: 0
        } ?: 0
        return bond >= WARM_BOND_THRESHOLD
    }

    private fun finishArc(state: GameState, def: StoryArcDef, rec: StoryArcState) {
        val outcome = if (warmEarned(state, def, rec)) def.warm else def.solo
        rec.done = true
        rec.outcomeId = outcome.id
        rec.finishedAbsDay = state.absDay
        rec.stage = def.stages.size
        // the ending STICKS: bonds, the relation note, memories
        for (pair in def.primaryPairs) {
            val a = pair.first(); val b = pair.last()
            val key = Threads.pairKey(a, b)
            state.npcBonds[key] =
                ((state.npcBonds[key] ?: 0) + outcome.bondDelta).coerceIn(-5, 10)
            state.lifeText["arcNote:$key"] = outcome.relationNote
        }
        for (name in def.parties + def.supporting) {
            state.agents[name]?.memories?.add("📖 ${def.title}: ${outcome.title}")
        }
        // the town noticed you were part of it (warm endings only:
        // a solo ending is the town's own doing, not yours)
        if (outcome.id == def.warm.id) {
            for (name in def.parties) {
                val cur = state.friendship[name] ?: 0
                state.friendship[name] = (cur + 3).coerceIn(-50, 100)
            }
        }
        state.events.add("📖 STORY'S END: '${def.title}' — ${outcome.title}.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "📖 ${def.title} — ${outcome.title}")
    }

    // ---------------- player involvement ----------------

    /** Any witnessed interaction with an arc person counts as a
     *  nudge (talk / gift / selfie — GameActivity & Engine call
     *  this). When the nudges cover the arc's whole core for the
     *  current stage, the stage is HELPED: it turns over at the
     *  next midnight instead of the one after. Idempotent per
     *  stage; the credit is counted exactly once per stage. */
    fun noteInteraction(state: GameState, npcName: String, kind: String) {
        val def = activeDef(state) ?: return
        if (npcName !in def.core && npcName !in def.supporting) return
        val rec = state.storyArcs[def.id] ?: return
        if (rec.done) return
        rec.stageNudges.add(npcName)
        if (rec.stage !in rec.helpedStages &&
            rec.stageNudges.containsAll(def.core)) {
            rec.helpedStages.add(rec.stage)
            state.events.add("💞 You nudged '${def.title}' along — " +
                "this beat will turn over tomorrow.")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    /** The talk beat: talking to an arc person during their stage
     *  surfaces their stage line — once per person per stage.
     *  (memoryPreLines in GameActivity plays it before the chat,
     *  like the mystery's witness lines.) */
    fun talkLine(state: GameState, npcName: String): Line? {
        val def = activeDef(state) ?: return null
        val rec = state.storyArcs[def.id] ?: return null
        if (rec.done) return null
        val stage = def.stages.getOrNull(rec.stage) ?: return null
        val text = stage.talkLines[npcName] ?: return null
        if (!rec.stageTalkShown.add(npcName)) return null
        return Line(npcName, text)
    }

    // ---------------- permanent marks ----------------

    /** The relation note the Journal shows for a pair: the arc's
     *  permanent override when one exists, else the static note. */
    fun relationNoteFor(state: GameState, a: String, b: String): String? =
        state.lifeText["arcNote:${Threads.pairKey(a, b)}"] ?: relationNote(a, b)

    /** A finished arc lives on in the pair's ambient chatter:
     *  when the two of them meet again, one of the outcome's
     *  callback lines (in its own speaker's voice) can surface.
     *  Null until that pair's arc has actually ended. */
    fun ambientCallback(state: GameState, a: String, b: String, rnd: Random): Line? {
        val pair = setOf(a, b)
        for (def in ARCS) {
            if (pair !in def.primaryPairs) continue
            val rec = state.storyArcs[def.id] ?: continue
            if (!rec.done) continue
            val bank = def.outcomeById(rec.outcomeId).callbacks
                .filter { it.speaker == a || it.speaker == b }
            if (bank.isEmpty()) return null
            return bank[rnd.nextInt(bank.size)]
        }
        return null
    }

    // ---------------- the Journal page ----------------

    fun pageVisible(state: GameState): Boolean = state.storyArcs.isNotEmpty()

    /** The whole 📖 ARCS page as plain lines (the Journal renders
     *  them; the suite reads them) — arcs with stage progress,
     *  and once done, their outcome in one line. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("📖 STORY ARCS — the town's ongoing business of the heart")
        for (def in ARCS) {
            val rec = state.storyArcs[def.id]
            when {
                rec == null ->
                    out.add("· ${def.title} — not yet. The town hasn't got there.")
                !rec.done -> {
                    out.add("▶ ${def.title} — stage ${rec.stage + 1} of ${def.stages.size}")
                    def.stages.forEachIndexed { i, st ->
                        val mark = when {
                            i < rec.stage -> "✓"
                            i == rec.stage -> "▶"
                            else -> "·"
                        }
                        out.add("$mark ${st.title}")
                    }
                    // v0.3.0g audit: the loader clamps stage to
                    // 0..stages.size; a record at exactly size
                    // with done=false (hand-tampered save) must
                    // not index past the last stage here — the
                    // record self-heals at the next midnight.
                    val cur = def.stages[minOf(rec.stage, def.stages.size - 1)]
                    out.add("📝 ${cur.journalNote}")
                    out.add("🎯 ${cur.objective}" +
                        if (rec.stage in rec.helpedStages)
                            " (you've nudged this beat — it turns tomorrow)"
                        else "")
                }
                else -> {
                    val oc = def.outcomeById(rec.outcomeId)
                    out.add("✓ ${def.title} — ${oc.title}")
                    out.add("🏁 ${oc.text}")
                }
            }
        }
        return out
    }
}

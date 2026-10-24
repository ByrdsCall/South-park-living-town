package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Threads — conversation continuity (v0.1.9e, simulation depth).
//
// Conversations used to restart from zero every time. Now they
// have THREADS, and the store is the one that already exists:
// per-NPC character memory (NpcMemory.topics, capped at 6) for
// player threads, and a per-pair topic slot (state.lifeText)
// for NPC<->NPC threads.
//
//   - Follow-ups: from the second real conversation on, NPCs
//     ask after the last thing you talked about ("did you
//     ever…") instead of opening cold — a third conversation
//     with someone reads different from the first.
//   - Callbacks: earlier topics resurface as asides, in mock
//     lines and in the LLM's prompt context alike (Phase-1 #16
//     parity: both brains are handed the same thread context).
//   - Running jokes: pairs of NPCs who keep meeting develop a
//     seeded running gag + a shared last-topic they reference
//     when they bump into each other again.
//
// All generation is deterministic-friendly: templates keyed by
// the memory topic tags (NpcMemoryStore.TOPIC_WORDS), chosen
// with the caller's Random.
// ============================================================

object Threads {

    /** Follow-up openers per memory topic tag. {topic} = the tag. */
    private val FOLLOWUPS: Map<String, List<String>> = mapOf(
        "the play" to listOf(
            "So — {topic}. Who do you think gets the Mountain this year? I have a whole theory and it is mostly about standing still.",
            "Update me on {topic}: auditions are coming and the sign-up pencil has already seen three dramas. None of them were the play."),
        "school" to listOf(
            "Did you ever sort out that {topic} stuff? The homework situation looked DIRE.",
            "Follow-up on {topic}: did Garrison ever find out? Asking for everyone."),
        "food" to listOf(
            "So — did you ever get more of that {topic} you were on about? I think about it now.",
            "Update on the {topic} front? Last time you had OPINIONS."),
        "money" to listOf(
            "Did the {topic} thing ever work out, or are we still poor?",
            "Whatever happened with your whole {topic} plan? I never heard the end of it."),
        "weather" to listOf(
            "Remember when we talked about the {topic}? It's doing it AGAIN. It heard us.",
            "The {topic} is back. I blame our conversation. Personally."),
        "music" to listOf(
            "Did you ever finish that {topic} argument? Who won? Be honest.",
            "I had that {topic} thing stuck in my head for DAYS after we talked. Thanks for that."),
        "games" to listOf(
            "Did you ever beat that {topic} thing you mentioned? Be honest.",
            "Whatever happened with the {topic} saga? Last time it was life or death."),
        "family" to listOf(
            "How'd the {topic} stuff shake out? Sounded like a whole thing.",
            "Did your {topic} survive what we talked about? Mine barely did."),
        "gross stuff" to listOf(
            "I still can't un-know the {topic} facts you shared. A follow-up was NOT requested, but here we are.",
            "The {topic} conversation haunts me. Anyway. Hi."),
        "secrets" to listOf(
            "So — the {topic}. Did anyone find out? Blink twice if it went bad.",
            "Whatever happened with those {topic} of yours? I've been DYING to know."),
        "aliens & weird stuff" to listOf(
            "Did anything ever come of the {topic}? I keep looking at the sky now. Thanks.",
            "Follow-up: the {topic}. Still out there? I have follow-up questions and no courage.")
    )

    private val FOLLOWUPS_GENERIC = listOf(
        "Did that whole {topic} thing ever get resolved? I never heard the end.",
        "Whatever happened with {topic}? Last time it sounded serious.",
        "So. {topic}. Update me. I have been left on a cliffhanger and I hate it.",
        "I keep almost bringing up the {topic} thing and then it resolves itself. Rude of it.",
        "Status report on {topic}. The town needs closure. I need closure. Mostly me."
    )

    /** v0.2.0d: a second wave of follow-ups — two more templates
     *  per original topic class, plus three NEW topic classes
     *  (crushes & drama / pets & critters / tech & gadgets — the
     *  matching keyword tags live in NpcMemoryStore.TOPIC_WORDS). */
    private val FOLLOWUPS_MORE: Map<String, List<String>> = mapOf(
        "school" to listOf(
            "Update on {topic}: did the teacher ever figure it out? The suspense is unbearable.",
            "You ever finish that {topic} thing, or is it just part of your personality now?"),
        "food" to listOf(
            "I've been craving whatever that {topic} was ever since. Your influence is a problem.",
            "Follow-up: is the {topic} place still standing after your review?"),
        "money" to listOf(
            "Are we rich yet? The {topic} plan had a timeline and I have been WAITING.",
            "How's the {topic} empire? I assume there's an empire by now."),
        "weather" to listOf(
            "Every time the {topic} shows up I think of you now. That's your fault.",
            "The {topic} is trending again. Coincidence? After OUR conversation? Doubtful."),
        "music" to listOf(
            "That {topic} debate is still raging in my head, rent-free.",
            "I heard a song that sounded like our {topic} argument. It was also off-key."),
        "games" to listOf(
            "Rematch on the {topic} thing. I have trained. I have regrets. Mostly trained.",
            "Did you ever one-hundred-percent the {topic} thing? Be honest, I can tell when you're lying about achievements."),
        "family" to listOf(
            "Family update: is the {topic} situation survivable yet?",
            "I told my family about your {topic} story. It started a whole thing. Sorry. Not sorry."),
        "gross stuff" to listOf(
            "The {topic} facts live in my head permanently. There's rent due.",
            "I shared the {topic} knowledge at lunch. I'm not welcome at that table anymore."),
        "secrets" to listOf(
            "Loose end check: the {topic}. Still secret? Blink once for yes, twice for disaster.",
            "I've kept the {topic} secret this long. My silence deserves a trophy."),
        "aliens & weird stuff" to listOf(
            "Any movement on the {topic}? I bought a flashlight because of you.",
            "I saw something weird last night and my first thought was the {topic}. Your fault entirely."),
        "crushes & drama" to listOf(
            "So — the {topic} situation. Any updates? I've invested emotionally now.",
            "Did the {topic} thing ever turn into an actual thing? Asking as your drama correspondent.",
            "Last we spoke: {topic}. My notes say 'it's complicated.' Confirm or deny."),
        "pets & critters" to listOf(
            "How's the {topic} situation? Still ruling your house, I assume.",
            "Any updates on the {topic} front? I think about that little menace weekly.",
            "Did you ever sort out the whole {topic} thing? There were stakes. Furry stakes."),
        "tech & gadgets" to listOf(
            "Did the {topic} ever start working again, or did we just accept the chaos?",
            "Update on your {topic} saga? Last episode ended on a cliffhanger and a dead battery.",
            "Whatever happened with the {topic}? I ask as a concerned citizen of the group chat."),
        // v0.6.0b: the topic Errands plants while a favor
        // chain is being run — its links land in the town's
        // memory, and the town follows up like it does.
        "favor chain" to listOf(
            "That {topic} you ran — did the last link ever land? I NEED the ending.",
            "Update on the {topic}: the town is still talking about the middle part.",
            "Your {topic} era. Legendary. Exhausting to watch, but legendary."),
        // v0.8.0c: the topic Emergencies plants in an asker
        // whose clock you beat — the town's smallest
        // disasters remember their responder.
        "rescue" to listOf(
            "That {topic} — I still tell people about the timing. The TIMING. You were a weather event of helpfulness.",
            "Update on the {topic} front: I've started pointing at you when things break. Professionally. As a referral.",
            "Your {topic} era. The clock lost, and it has accepted this. We all watched it accept this."),
        // v0.6.0e: the topic Jobs plants when a shift lands
        // — the boss (and a coworker or two) keeps the
        // thread like everyone else in town.
        "work" to listOf(
            "How's the {topic} treating you? Boss fair, hours " +
                "honest? The town keeps score on that too.",
            "Still on the {topic} grind? I told someone you're " +
                "the reliable one. Don't make me a liar.",
            "Your {topic} era continues. I respect a schedule. " +
                "I fear one a little, but I respect it."),
        // v0.6.0f: the topic Votes plants in the champions
        // of the top two finishers when a count lands — the
        // winner gloats gently, the runner-up heals publicly.
        "vote" to listOf(
            "Still thinking about that {topic}. Democracy in this town is a contact sport and I brought a helmet.",
            "You saw the {topic} result? I've accepted it. Publicly. Privately there are charts.",
            "The {topic}, the {topic}. Everyone keeps asking my position. My position is: I voted correctly."),
        // v0.7.0b: the topic Secrets plants in whoever was
        // standing there when a town secret stopped being
        // one — the witness carries it like everyone else's.
        "town secret" to listOf(
            "That {topic} you turned up — you ever tell anyone else? Asking as its co-keeper, apparently.",
            "Update on the {topic} front: I checked again. Still there. Still weird. Still ours.",
            "I keep thinking about that {topic}. The town's got layers, and you peeled one."),
        // v0.7.0c: the topic MovieNights plants in the whole
        // house at the credits — the morning after, the
        // audience is still arguing about the flop scene.
        "movie night" to listOf(
            "That {topic} — I have reviewed it to three people already. My review is one word and it's devastating.",
            "Still thinking about {topic}. The flop scene lives in my head rent-free and I have stopped fighting it.",
            "Your {topic} era. I sat two rows back and I have NOTES, if you ever want them. You don't. Nobody does. I have them anyway."),
        // v0.6.0d: the topic Dinners plants when a family
        // has had the new kid at their table — the town's
        // tables keep their threads like everyone else's.
        "dinner" to listOf(
            "Still thinking about that {topic} at ours. The good pan has been mentioned in dispatches.",
            "When's the next {topic}? Asking for the whole table. The table is me. I'm asking.",
            "You, at {topic}, eating like family. It's going in the family group text of record."),
        // v0.7.0e: the topics a PLAYED climax plants on its
        // causers — the town retells YOUR version of the peak,
        // and the telling differs by how it went: heroic
        // retellings flatter, got-it-done ones are warm, and
        // the fumbled ones roast with love. (An absent climax
        // plants nothing: there's no version of yours to tell.)
        "the big save" to listOf(
            "I was THERE for {topic}. You, at the peak of it, ice in your veins. I have told it four times and it gets better every time.",
            "{topic}? I tell it at the bus stop now. I add a pause where you stepped in. The pause is load-bearing.",
            "People ask me about {topic} like I did it. I correct them. Eventually. After the good part."),
        "the big day" to listOf(
            "Still turning over {topic} in my head. Messy in the middle, yours at the end. That's the review.",
            "You know what nobody says about {topic}? That it was boring. It was a lot of things. Boring, no.",
            "{topic} is my go-to small talk now. I open with 'so the new kid was there when it PEAKED—' and people stay."),
        "the big fumble" to listOf(
            "{topic}. I saw it with my own eyes and I still don't believe them. Beautiful. No notes. Okay, many notes.",
            "I defend {topic} at parties now. Was it graceful? No. Was it the best part of the day? Also somehow yes.",
            "Everybody's got an impression of {topic}. Yours is the one with the sound effects. Treasure it."),
        // v0.8.0b: the topics Capstones plants — in every
        // member's memory when their faction's big ask is
        // taken on (the ask outstanding: anticipation, in on
        // it) and swapped when the capstone is set (the ask
        // answered: the title is just what they call you now).
        "the big ask" to listOf(
            "So {topic} is actually happening. Our faction's. I am being SO normal about being in on it.",
            "You know about {topic}, right? Ours. Don't rush it. Actually rush it a little. For us.",
            "I keep almost bringing up {topic} in normal conversation and having to swallow it like a pill."),
        "our capstone" to listOf(
            "{topic} is in the book now. Set, witnessed, permanent. I checked the spot this morning. Still there.",
            "People say the title like it's your name now. Because of {topic}. Because it IS your name now, a little.",
            "I was there for {topic}. Whatever this town does next, it does it with that already done."),
        // v0.8.0d: the topics Yearbook publication plants —
        // the cast in a winning frame bask (they are IN the
        // book), the cast in a runner-up frame grumble with
        // their whole chest. The argument fades like every
        // topic: new talk pushes it out of the memory.
        "yearbook famous" to listOf(
            "I'm {topic}. Officially. There's a book. I may never take a normal photo again.",
            "My mom cut out my page. The {topic} page. It's on the fridge, next to a coupon.",
            "Being {topic} is a lot of pressure. Every photo now I'm like — is THIS the legacy?"),
        "yearbook snub" to listOf(
            "The {topic} is being handled with total grace. By OTHER people. I'm still composing my statement.",
            "Second place. SECOND. The {topic} heard round the school and the school said 'huh'.",
            "I'm not saying the judges were wrong about the {topic}. I'm saying history vindicates me in the sequel."),
        // v0.8.0e: the topic Rival plants in the rival's
        // memory at the crowning and at every settle — the
        // scoreboard has a thread now, and the thread keeps
        // the rivalry's place in conversation. (The FACTUAL
        // score — who's ahead, by how much, in words — is
        // Rival.scoreLine's job, quoted verbatim by the
        // owed-lines gossip and the Journal page; these are
        // the feelings around it.)
        "the rivalry" to listOf(
            "So — {topic}. The scoreboard's on the Journal page and half the town has opinions. I have the correct ones.",
            "Update on {topic}: still official, still personal, still the best thing about this town's calendar.",
            "People keep asking me about {topic} like I didn't invent half of it. I did. The half that's winning."),
        // v0.9.0b: the topic Crew plants on the witnesses
        // when a crew rolls in — the town has FEELINGS
        // about the new kid having a formation now, in all
        // three registers: admiring, jealous, unimpressed.
        "your crew" to listOf(
            "So — {topic}. An actual formation. I've seen you all roll into a place and it's genuinely a whole entrance now.",
            "Update on {topic}: marched past my house today like a parade with no permit. I watched the entire time. Twice.",
            "People are talking about {topic} like it's a sports team. There are opinions. There are rankings. I have rankings.",
            "I'm not jealous of {topic}. I'm just saying some of us walk places ALONE like peasants, that's all I'm saying.",
            "It's some kids standing near each other, that's my whole review of {topic}. …They do stand near each other with real commitment, though."),
        // v0.9.0e: the topic RivalCrew plants on both crews
        // and their captain at every crew-contest settle —
        // the town has a second scoreboard and FEELINGS
        // about it. (The NUMBERS are RivalCrew's standings
        // line, quoted verbatim by the owed-lines gossip
        // and the 🆚 page; these are the feelings around
        // them, same discipline as the rivalry's thread.)
        "the crews" to listOf(
            "So — {topic}. Two formations, one season, and the whole town's picked a side like it's a custody hearing.",
            "Update on {topic}: I checked the standings on the Journal page twice today. There was nothing new the second time. I checked anyway.",
            "People keep score of {topic} on napkins now. NAPKINS. The diner had to buy more napkins.",
            "I'm not saying {topic} is the only sport this town has. I'm saying the trophy shelf believes it.",
            "Say what you want about {topic}, but when both crews roll into the same place the whole room stands up straighter."),
        // v0.10.0e: the topic Stand plants on the rival's
        // crew (and the rival) when their review posts —
        // the town gets opinions about the new kid's little
        // business. (The NUMBERS — markup, sell-through,
        // spoilage — are Stand's books, quoted verbatim on
        // the 💰 page; these are the feelings around them,
        // same discipline as the crews' thread.)
        "your stand" to listOf(
            "So — {topic}. An actual permitted business. With a sign. I have reviewed my feelings and my feelings are thirsty.",
            "Update on {topic}: the review is posted and half the town is pretending they wrote it. They did not. There are signatures.",
            "People have STANDINGS about {topic} now. Not opinions — standings. There's a difference and I will explain it at length.",
            "I'm not saying {topic} changed my route across town. I'm saying my route now includes a snack stop and that's just urban planning.",
            "Say what you want about {topic}, but a kid with a permit and a crate is more legitimate than half the businesses on Main Street."),
        // v0.11.0c: the topic Sleepovers plants in every
        // guest who came — Friday night at the new kid's,
        // retold by the people who were in the sleeping
        // bags. (The record — who came, what was served,
        // who took the pillow fight — is Sleepovers' own,
        // on the 🛌 page; these are the feelings.)
        "sleepover" to listOf(
            "So — {topic}. I have told my house about {topic} in full. With voices. The pillow fight gets three acts.",
            "Update on {topic}: I woke up on a floor and it was still the best sleep of my life. Science can't explain it. I won't let it try.",
            "People keep asking about {topic} like it wasn't the social event of the season. It was. There were snacks. There was a couch verdict.",
            "I'm not saying {topic} changed me. I'm saying I now have a ranked list of sleeping-bag spots and strong opinions about all of them.",
            "Say what you want about {topic}, but a Friday night done right echoes. I'm still hearing it. Mostly because someone snores.")
    )

    private val ALL_FOLLOWUPS: Map<String, List<String>> =
        mergeLineBanks(FOLLOWUPS, FOLLOWUPS_MORE)

    private val CALLBACKS = listOf(
        "Anyway — still can't believe the {topic} thing.",
        "This is giving me flashbacks to the {topic} conversation.",
        "Between you and me? The {topic} saga isn't over. I can feel it.",
        "This is exactly how the {topic} saga started. I'm just saying.",
        "If anyone asks, we're blaming the {topic} era.",
        "You, me, and the {topic} thing — that's a trilogy now.",
        "Somewhere out there, the {topic} is still unfolding. I can feel it.",
        "I have told three people about the {topic} and they all gasped correctly."
    )

    /** The pair running-gag bank: lines either member of a bonded
     *  pair can drop when they meet again. Seeded per pair so the
     *  SAME pair keeps the SAME joke. */
    val PAIR_JOKES = listOf(
        "This is just like the raccoon incident. Which we do NOT discuss.",
        "You still owe me a dollar, by the way. It's about the principle now.",
        "If this goes like last time, I'm blaming you in advance.",
        "We agreed: what happened at the pond STAYS at the pond.",
        "Still not over you eating my emergency sandwich. There will be a reckoning.",
        "One of these days I'm telling people what you did at the lake. One of these days.",
        "You know what your problem is? Same as last time. Consistency, though.",
        "If anyone asks, we were never here. Standard procedure.",
        "You still haven't apologized for the dodgeball thing. The statute of limitations is ME.",
        "One day we'll tell this story and I'll be the hero. We've discussed this.",
        "I saved you a seat. You owe me emotionally forever. Those are the terms.",
        "This friendship runs on snacks and secrets and you know it."
    )

    fun pairKey(a: String, b: String): String = if (a < b) "$a|$b" else "$b|$a"

    // ---------------- player threads (store: NpcMemory.topics) ----------------

    /** A follow-up opener from this NPC's most recent thread with
     *  the player. Null until the relationship has real history
     *  (2+ meets) and a stored topic — conversation one stays an
     *  opener, conversation three gets a callback. */
    fun followUp(mem: NpcMemory, rnd: Random): String? {
        if (mem.meets < 2) return null
        val topic = mem.topics.lastOrNull() ?: return null
        val bank = ALL_FOLLOWUPS[topic] ?: FOLLOWUPS_GENERIC
        val line = bank[rnd.nextInt(bank.size)]
        return line.replace("{topic}", topic)
    }

    /** A lighter callback aside on ANY stored topic (not just the
     *  latest) — for mid-conversation seasoning. */
    fun callback(mem: NpcMemory, rnd: Random): String? {
        if (mem.meets < 2 || mem.topics.isEmpty()) return null
        val topic = mem.topics[rnd.nextInt(mem.topics.size)]
        return CALLBACKS[rnd.nextInt(CALLBACKS.size)].replace("{topic}", topic)
    }

    /** The LLM prompt's thread context — the same context the
     *  mock brain seasons lines with (#16 parity). */
    fun threadHint(npcName: String, playerName: String): String {
        val mem = NpcMemoryStore.get(npcName) ?: return ""
        if (mem.meets < 2 || mem.topics.isEmpty()) return ""
        return "\nTHREAD: You and $playerName have talked ${mem.meets} times; " +
            "recent threads: ${mem.topics.takeLast(3).joinToString(", ")}. " +
            "A follow-up or callback would feel natural — ask after one of them " +
            "('did you ever…') instead of opening cold."
    }

    // ---------------- pair threads (store: state.lifeText) ----------------

    /** Remember what an ambient pair last talked about (read from
     *  the dialogue's own words, like player topics are). */
    fun recordPairTopic(state: GameState, a: String, b: String, dialogueText: String) {
        val tag = NpcMemoryStore.topicOf(dialogueText) ?: return
        state.lifeText["pairTopic:${pairKey(a, b)}"] = tag
    }

    fun pairTopic(state: GameState, a: String, b: String): String? =
        state.lifeText["pairTopic:${pairKey(a, b)}"]

    /** This pair's running joke — stable per pair (seeded by the
     *  pair key), so Cartman & Kyle keep THEIR gag. */
    fun pairJoke(a: String, b: String): String =
        PAIR_JOKES[(pairKey(a, b).hashCode() and 0x7fffffff) % PAIR_JOKES.size]

    /** A continuity line for an ambient reunion: a callback to the
     *  pair's last topic when they have one, else their running
     *  joke once the bond is real. Null when there's no history
     *  worth referencing yet. */
    fun pairCallback(state: GameState, a: String, b: String, bond: Int, rnd: Random): String? {
        val topic = pairTopic(state, a, b)
        if (topic != null && rnd.nextDouble() < 0.6) {
            return "Still not over the $topic thing, by the way."
        }
        if (bond >= 4) return pairJoke(a, b)
        return null
    }
}

package com.hunt3r.southparktown

// ============================================================
// NpcMemory — persistent per-NPC character memory (v0.1.5).
//
// EVERY NPC (the 32 cast AND procedurally generated lorebook
// NPCs, keyed by identity — cast name / lorebook entry name)
// gets a lightweight memory of the player, stored on-device in
// the same SharedPreferences file under its OWN key
// ("npc_memory") so the game_state schema carries zero risk:
// old saves load exactly as before; the memory doc is additive.
//
// Kept deliberately capped: counts + a rolling summary, never
// transcripts. Dialogue uses it two ways — LLM prompts get a
// memory block, and mock-mode first-vs-repeat greetings differ.
// ============================================================

data class NpcMemory(
    var meets: Int = 0,
    /** Relationship points for GENERATED npcs. Cast points live in
     *  GameState.friendship (the original system) and are mirrored here. */
    var points: Int = 0,
    var lastMetDay: String = "",
    var lastMetMin: Int = -1,
    val topics: MutableList<String> = mutableListOf(),  // <= 6 topic tags
    val gifts: MutableList<String> = mutableListOf(),   // <= 4 recent gift names
    var summary: String = "",
    var eventNote: String = "",                          // last shenanigans shared
    /** v0.1.8: learned gift preferences — item names this NPC LOVED.
     *  Written by recordGift when loved; shown on their Journal card. */
    val likes: MutableList<String> = mutableListOf(),   // <= 3 item names
    /** v0.1.8: the player appearance this NPC last saw (outfit comments). */
    var seenAppearance: String = ""
)

object NpcMemoryStore {
    private val map = mutableMapOf<String, NpcMemory>()
    private var loaded = false

    fun get(key: String): NpcMemory? = map[key]
    fun getOrCreate(key: String): NpcMemory = map.getOrPut(key) { NpcMemory() }
    fun all(): Map<String, NpcMemory> = map
    val isLoaded: Boolean get() = loaded

    private val TOPIC_WORDS: List<Pair<String, List<String>>> = listOf(
        "school" to listOf("school", "class", "teacher", "homework", "garrison"),
        "food" to listOf("food", "hungry", "eat", "cheesy", "taco", "pizza", "lunch", "coffee"),
        "money" to listOf("money", "rich", "poor", "buy", "sell", "cash", "job"),
        "weather" to listOf("snow", "cold", "rain", "weather", "blizzard", "fog", "wind"),
        "music" to listOf("music", "song", "band", "sing", "guitar"),
        "games" to listOf("game", "play", "chinpokomon", "video", "xbox"),
        "family" to listOf("mom", "dad", "family", "brother", "sister", "parents"),
        "gross stuff" to listOf("poop", "fart", "gross", "barf", "butt"),
        "secrets" to listOf("secret", "hide", "sneak", "plan", "scheme"),
        "aliens & weird stuff" to listOf("alien", "ghost", "monster", "weird", "crab"),
        // v0.2.0d: three more topic classes (appended LAST so the
        // original ten keep classification priority; Threads has
        // follow-up templates keyed to each of these tags).
        "crushes & drama" to listOf("crush", "girlfriend", "boyfriend", "sweetheart", "valentine", "wedding"),
        "pets & critters" to listOf("puppy", "kitten", "squirrel", "raccoon", "hamster", "goldfish", "my dog", "my cat", "my pet"),
        "tech & gadgets" to listOf("smartphone", "computer", "internet", "drone", "robot", "selfie", "gadget", "wifi"),
        // v0.8.0e: appended LAST, like every wave — the
        // rivalry's tag, so talk about it files under it.
        "the rivalry" to listOf("rivalry", "my rival", "the rival", "rematch", "scoreboard"),
        // v0.9.0b: appended LAST, like every wave — the
        // crew's tag, so talk about it files under it.
        "your crew" to listOf("your crew", "the crew", "crew", "entourage"),
        // v0.9.0e: appended LAST, like every wave — the
        // crews-standings tag RivalCrew plants at every
        // crew-contest settle.
        "the crews" to listOf("the crews", "crew standings", "their crew")
    )

    fun topicOf(text: String): String? {
        val t = text.lowercase()
        for ((tag, words) in TOPIC_WORDS) {
            if (words.any { it in t }) return tag
        }
        return null
    }

    /** Keywords behind a stored topic tag (topic weighting reads these). */
    fun topicWords(tag: String): List<String> =
        TOPIC_WORDS.firstOrNull { it.first == tag }?.second ?: listOf(tag)

    private fun rebuildSummary(key: String, mem: NpcMemory, playerName: String) {
        val tier = Relationships.tierLabel(mem.points)
        val bits = mutableListOf<String>()
        bits.add("Met ${mem.meets} time${if (mem.meets == 1) "" else "s"}.")
        if (mem.topics.isNotEmpty()) bits.add("Talked about ${mem.topics.takeLast(3).joinToString(", ")}.")
        if (mem.gifts.isNotEmpty()) bits.add("$playerName gave them: ${mem.gifts.joinToString(", ")}.")
        if (mem.eventNote.isNotEmpty()) bits.add(mem.eventNote)
        bits.add("Relationship: $tier.")
        mem.summary = bits.joinToString(" ").take(240)
    }

    /** Record a talk session. Session-counted: re-taps within 30 in-game
     *  minutes of the last record update topics but not the meet count. */
    fun recordTalk(key: String, dayKey: String, timeMin: Int, playerLine: String?,
                   points: Int, playerName: String, eventTitle: String? = null) {
        val mem = getOrCreate(key)
        val sameSession = mem.lastMetDay == dayKey &&
            kotlin.math.abs(timeMin - mem.lastMetMin) < 30 && mem.lastMetMin >= 0
        if (!sameSession) mem.meets++
        mem.lastMetDay = dayKey
        mem.lastMetMin = timeMin
        mem.points = points
        if (playerLine != null) {
            topicOf(playerLine)?.let { tag ->
                mem.topics.remove(tag)
                mem.topics.add(tag)
                while (mem.topics.size > 6) mem.topics.removeAt(0)
            }
        }
        if (eventTitle != null) mem.eventNote = "You were both around for $eventTitle."
        rebuildSummary(key, mem, playerName)
    }

    fun recordGift(key: String, itemName: String, loved: Boolean, points: Int,
                   dayKey: String, timeMin: Int, playerName: String) {
        val mem = getOrCreate(key)
        mem.gifts.add(if (loved) "$itemName (loved it!)" else itemName)
        while (mem.gifts.size > 4) mem.gifts.removeAt(0)
        // v0.1.8: a loved reaction is a LESSON — it sticks in memory.
        if (loved) {
            mem.likes.remove(itemName)
            mem.likes.add(itemName)
            while (mem.likes.size > 3) mem.likes.removeAt(0)
        }
        mem.points = points
        mem.lastMetDay = dayKey
        mem.lastMetMin = timeMin
        rebuildSummary(key, mem, playerName)
    }

    /** LLM prompt block: this NPC's memory of the player.
     *  v0.1.9e: carries the THREAD context too (recent topics +
     *  a follow-up steer) — the same context the mock brain
     *  seasons its lines with, per Phase-1 #16 brain parity. */
    fun promptBlock(key: String, playerName: String): String {
        val mem = map[key] ?: return ""
        if (mem.meets == 0 && mem.gifts.isEmpty()) return ""
        return "\nYOUR MEMORY OF $playerName: ${mem.summary} " +
            "Remember this naturally — greet them like someone you know, " +
            "and you may bring up past topics or gifts." +
            Threads.threadHint(key, playerName)
    }

    /** Repeat-meeting mock greeting flavor (first meetings stay original). */
    fun repeatGreeting(key: String, playerName: String): String? {
        val mem = map[key] ?: return null
        if (mem.meets < 1) return null
        val topic = mem.topics.lastOrNull()
        val gift = mem.gifts.lastOrNull()
        return when {
            gift != null -> "Oh hey, $playerName. Still thinking about that ${gift.substringBefore(" (")} you gave me."
            topic != null -> "Hey, $playerName. You again — last time it was all about $topic."
            mem.meets >= 5 -> "Oh, $playerName! My favorite regular. What's up?"
            else -> "Oh hey, $playerName. Back again, huh?"
        }
    }

    // ---- serialization (plain maps; Prefs Json handles the rest) ----

    /** v0.2.0g: the persisted npc_memory doc is capped at this
     *  many entries. Per-entry contents were always capped
     *  (topics/gifts/likes/summary), but the ENTRY COUNT only
     *  had the lorebook pool (~2k names) as its ceiling — a
     *  theoretical ~700 KB doc. At save time the doc keeps:
     *  every cast member, every current resident (base +
     *  converted), then the most recently met generated folk,
     *  up to the cap. Runtime memory is untouched (nobody is
     *  forgotten mid-session); a pruned generated NPC simply
     *  starts fresh next save, exactly like a first meeting. */
    const val MAX_ENTRIES = 400

    fun serialize(): Map<String, Any?> {
        val full = map.mapValues { (_, m) ->
            mapOf(
                "meets" to m.meets, "points" to m.points,
                "lastMetDay" to m.lastMetDay, "lastMetMin" to m.lastMetMin,
                "topics" to m.topics.toList(), "gifts" to m.gifts.toList(),
                "summary" to m.summary, "eventNote" to m.eventNote,
                // v0.1.8 additive keys
                "likes" to m.likes.toList(), "seenAppearance" to m.seenAppearance
            )
        }
        if (full.size <= MAX_ENTRIES) return full
        val pinned = HashSet<String>(CharacterSystem.CAST.keys)
        try {
            for (p in VisitorSystem.residents()) pinned.add(p.loreName)
            pinned.addAll(VisitorSystem.convertedProvider())
        } catch (e: Exception) { /* lore not loaded: cast stays pinned */ }
        val keep = HashSet<String>()
        for (k in full.keys) if (k in pinned) keep.add(k)
        val rest = map.entries
            .filter { it.key !in pinned }
            .sortedWith(compareByDescending<Map.Entry<String, NpcMemory>> {
                it.value.lastMetDay
            }.thenByDescending { it.value.meets })
        for (e in rest) {
            if (keep.size >= MAX_ENTRIES) break
            keep.add(e.key)
        }
        return full.filterKeys { it in keep }
    }

    @Suppress("UNCHECKED_CAST")
    fun deserialize(doc: Map<String, Any?>) {
        map.clear()
        for ((k, v) in doc) {
            val m = v as? Map<String, Any?> ?: continue
            val mem = NpcMemory(
                meets = (m["meets"] as? Number)?.toInt() ?: 0,
                points = (m["points"] as? Number)?.toInt() ?: 0,
                lastMetDay = m["lastMetDay"]?.toString() ?: "",
                lastMetMin = (m["lastMetMin"] as? Number)?.toInt() ?: -1,
                summary = m["summary"]?.toString() ?: "",
                eventNote = m["eventNote"]?.toString() ?: ""
            )
            ((m["topics"] as? List<Any?>)?.map { it.toString() } ?: emptyList())
                .forEach { mem.topics.add(it) }
            ((m["gifts"] as? List<Any?>)?.map { it.toString() } ?: emptyList())
                .forEach { mem.gifts.add(it) }
            // v0.1.8 additive keys (absent in old docs -> defaults)
            ((m["likes"] as? List<Any?>)?.map { it.toString() } ?: emptyList())
                .forEach { mem.likes.add(it) }
            mem.seenAppearance = m["seenAppearance"]?.toString() ?: ""
            map[k] = mem
        }
        loaded = true
    }

    fun markLoaded() { loaded = true }
    fun clear() { map.clear(); loaded = false }
}

// ============================================================
// Relationships — tiers on top of character memory (v0.1.5).
//
// One points scale for everyone: cast points live in
// GameState.friendship (the original cozy system, now allowed
// to go negative via insults/gifts), generated NPC points live
// in their NpcMemory. Tiers DO things:
//  STRANGER     — default lines.
//  ACQUAINTANCE — warmer greetings.
//  FRIEND       — secret/lore lines unlock (cast daily topics,
//                 visitor deep-fact lines), NPCs may reference
//                 your history, faction standing rises.
//  CLOSE_FRIEND — occasional gifts FROM the npc, ally lines
//                 during shenanigans climaxes, best greetings.
//  RIVAL        — hostile greetings + lines, faction standing
//                 falls. Earned by repeated rudeness (the
//                 dialogue classifier already detects insults).
// ============================================================
object Relationships {
    const val RIVAL = 0
    const val STRANGER = 1
    const val ACQUAINTANCE = 2
    const val FRIEND = 3
    const val CLOSE_FRIEND = 4

    fun tierOf(points: Int): Int = when {
        points < -10 -> RIVAL
        points < 10 -> STRANGER
        points < 30 -> ACQUAINTANCE
        points < 60 -> FRIEND
        else -> CLOSE_FRIEND
    }

    fun tierLabel(points: Int): String = when (tierOf(points)) {
        RIVAL -> "Rival"
        STRANGER -> "Stranger"
        ACQUAINTANCE -> "Acquaintance"
        FRIEND -> "Friend"
        else -> "Close Friend"
    }

    fun tierIcon(points: Int): String = when (tierOf(points)) {
        RIVAL -> "💢"
        STRANGER -> "🙂"
        ACQUAINTANCE -> "👋"
        FRIEND -> "😄"
        else -> "💛"
    }

    val RIVAL_GREETINGS: List<String> = listOf(
        "Oh. YOU again, {newkid}. My day is ruined.",
        "What do YOU want, {newkid}? Make it quick.",
        "Ugh. {newkid}. I liked today better before you showed up.",
        "...I'm still mad about what you said, {newkid}."
    )

    val RIVAL_LINES: List<String> = listOf(
        "Yeah, whatever, {newkid}.",
        "I'm not telling YOU anything, {newkid}.",
        "Talk to the hand, {newkid}. The face is busy hating you.",
        "Ask me again when you're less... you, {newkid}."
    )
}

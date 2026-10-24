package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// NpcGen — procedural NPC generation from the lorebook.
//
// Lucky's spec: "Use the lore book to make custom designs —
// a backend system for procedurally generating the NPCs.
// Appearance and personality pulled from the lorebook and
// created dynamically for the game."
//
// For each NON-CAST lorebook character entry, the generator:
//  1. scans the entry text for APPEARANCE signals (hair color
//     + style, facial hair, glasses, hats, clothing colors,
//     age cues -> AgeBand, skin cues, distinctive features)
//     and assembles a SheetSpec from the game's modular part
//     vocabulary (the KidArt part system: body plan by age
//     band + interchangeable hair/hat/face/expression sets);
//  2. extracts PERSONALITY (trait words, occupation/role,
//     bio + fact sentences) which drives fallback dialogue
//     and, with the entry itself, LLM prompt grounding;
//  3. detects RESIDENCY (lives in South Park?) — residents get
//     generated schedules; non-residents join the visitor
//     rotation;
//  4. assigns a FACTION when the entry affiliates them.
// Generation is DETERMINISTIC per character (seeded by the
// lorebook identity): same look, same personality, every day.
// Signal-poor entries get a seeded generic look — never any
// cast signature identifiers (no sig tokens, no specials, no
// signature hats/hair: beehive/swirl/swoop/curls/beret/pomhat/
// ushanka/chullo/chefhat are blacklisted below).
// Results are generated on spawn and cached.
// ============================================================

data class VisitorPersona(
    val loreName: String,          // identity + memory key + display name
    val spec: SheetSpec,
    val band: AgeBand,
    val traits: List<String>,
    val role: String?,
    val bio: String,
    val facts: List<String>,
    val resident: Boolean,
    val factionId: String?,
    val home: String?,
    val workLoc: String?,
    val hangout: String?,
    val greetingLines: List<String>,
    val factLines: List<String>,
    val deepLines: List<String>,
    val byeLines: List<String>,
    /** v0.2.0d: ambient small-talk pool templated by role +
     *  traits (+ a goth set when the entry reads goth), so a
     *  generated farmer and a generated goth don't sound alike. */
    val ambientLines: List<String> = emptyList(),
    /** v0.1.8 #23: taste tags derived from role + traits — their
     *  loved-gift reactions work the same way the cast's do. */
    val loves: Set<String> = emptySet()
) {
    /** Name-tag / speaker color: their top color, like cast colors. */
    val color: String get() = spec.top
}

/** A generated NPC placed in the world right now. */
data class ActiveNpc(
    val persona: VisitorPersona,
    val locId: String,
    val inside: Boolean
)

object NpcGenerator {
    private val cache = mutableMapOf<String, VisitorPersona>()

    private val HAIR_COLORS: List<Pair<List<String>, String>> = listOf(
        listOf("black") to "#2a2a2a", listOf("brown") to "#6b4a2f",
        listOf("blond", "blonde") to "#e8c53a", listOf("red") to "#c0392b",
        listOf("auburn") to "#8a4a2a", listOf("gray", "grey") to "#b8b8b8",
        listOf("white") to "#e8e8e8", listOf("orange") to "#e07b2a",
        listOf("pink") to "#e87a9a", listOf("blue") to "#2a6fd4",
        listOf("green") to "#2a9a4a", listOf("purple") to "#6a4a8a"
    )
    private val COLOR_WORDS: Map<String, String> = mapOf(
        "red" to "#c0392b", "blue" to "#2a6fd4", "green" to "#2a9a4a",
        "black" to "#2a2a2a", "brown" to "#6b4a2f", "white" to "#f0f0f0",
        "gray" to "#8a8a8a", "grey" to "#8a8a8a", "purple" to "#6a4a8a",
        "pink" to "#e87a9a", "orange" to "#e8932a", "yellow" to "#e8c53a",
        "teal" to "#3aa8a8", "tan" to "#c8a878"
    )
    private val PLAIN_TOPS = listOf("#5a7a9a", "#7a6a8a", "#4a6a5a", "#8a7a5a", "#6a5a4a",
        "#54687e", "#77685a", "#5d7261", "#71617a", "#686868")
    private val PLAIN_BOTTOMS = listOf("#3a4a5a", "#4a3a2a", "#2e2e3e", "#5a4a3a", "#445544")
    private val SKINS = listOf("#f5cfa8", "#f5cfa8", "#e0a878", "#f8e8d8")
    private val GEN_HAIR = listOf("mop", "messy", "sidepart", "buzz", "spiky", "bob", "ponytail", "long", "mop", "messy")
    private val GEN_HAIR_M = listOf("mop", "messy", "sidepart", "buzz", "spiky", "mop", "messy", "sidepart")
    private val GEN_HAIR_COLORS = listOf("#2a2a2a", "#6b4a2f", "#6b4a2f", "#2a2a2a",
        "#8a4a2a", "#e8c53a", "#3a2a1a", "#b8b8b8")
    private val GEN_HATS = listOf("none", "none", "none", "beanie", "cap", "none", "trucker", "cowboy")

    private val TRAIT_WORDS = listOf(
        "kind", "mean", "angry", "cheerful", "shy", "nervous", "greedy", "brave",
        "funny", "smart", "lazy", "grumpy", "friendly", "rude", "quiet", "loud",
        "sweet", "tough", "sneaky", "honest", "proud", "crazy", "gentle", "strict"
    )
    private val ROLE_WORDS = listOf(
        "mayor", "teacher", "police officer", "officer", "doctor", "nurse", "chef",
        "cook", "singer", "actor", "actress", "athlete", "farmer", "priest",
        "soldier", "scientist", "lawyer", "businessman", "businesswoman", "reporter",
        "artist", "musician", "principal", "counselor", "janitor", "waiter",
        "waitress", "clerk", "coach", "student", "celebrity", "rapper", "author"
    )
    private val WORKPLACE: Map<String, String> = mapOf(
        "teacher" to "Elementary", "principal" to "Elementary", "counselor" to "Elementary",
        "nurse" to "Elementary", "janitor" to "Elementary", "student" to "Elementary",
        "priest" to "Church", "mayor" to "CityHall", "clerk" to "CityHall",
        "police officer" to "CityHall", "officer" to "CityHall",
        "chef" to "CityWok", "cook" to "CityWok", "waiter" to "Raisins",
        "waitress" to "Raisins", "farmer" to "TegridyFarms", "doctor" to "TomsRhino",
        "singer" to "Bijou", "actor" to "Bijou", "actress" to "Bijou",
        "musician" to "Bijou", "celebrity" to "Bijou", "rapper" to "Bijou",
        "businessman" to "Mall", "businesswoman" to "Mall", "lawyer" to "CityHall",
        "scientist" to "CityHall", "reporter" to "MainStreet", "coach" to "KidsPark",
        "athlete" to "KidsPark", "artist" to "SoDoSoPa", "author" to "TweekBros",
        "soldier" to "JimbosGuns"
    )

    // ---------- v0.2.0d: ambient small-talk templates ----------
    // Generated NPCs used to speak only in lorebook facts. Now
    // each persona also carries an ambient pool built from
    // templates keyed to their occupation and traits (plus a
    // goth set for goth entries), so voices separate by kind of
    // person, not just by name. All lines newly written.
    private val ROLE_AMBIENT_ALIAS = mapOf(
        "police officer" to "police", "officer" to "police",
        "cook" to "chef", "actress" to "actor",
        "businessman" to "business", "businesswoman" to "business",
        "waitress" to "waiter"
    )

    private val ROLE_AMBIENT: Map<String, List<String>> = mapOf(
        "mayor" to listOf(
            "Running this town is three meetings and a ribbon cutting, all before lunch.",
            "I declared today an official day in South Park. Of what? Doesn't matter. It's official.",
            "The town budget? Bold of you to assume there is one."),
        "teacher" to listOf(
            "I graded thirty spelling tests and lost a little faith in each one.",
            "Summer can't come fast enough, and I say that in every season.",
            "A kid asked me a question today I couldn't answer. I gave him a gold star for it."),
        "police" to listOf(
            "Quiet day. In this town, a quiet day is the loudest alarm there is.",
            "I wrote three tickets and one apology. The apology took longer.",
            "Nothing weird's happened in an hour. I'm getting nervous."),
        "doctor" to listOf(
            "Fixed up a kid who fought a fence today. The fence is also fine. Emotionally, the fence won.",
            "In this town, 'you won't believe how it happened' is just called an intake form.",
            "Wash your hands. I say it to everyone. Nobody listens. I say it anyway."),
        "nurse" to listOf(
            "Bandaged four knees and one ego today. The ego needed more gauze.",
            "Flu season here is just called 'season.'",
            "I keep lollipops for brave patients. And a separate drawer for the dramatic ones."),
        "chef" to listOf(
            "Soup's on by six, and so is my opinion about your life choices.",
            "A recipe is just a rumor you can eat.",
            "I taste everything twice. Once for salt, once for courage."),
        "singer" to listOf(
            "I hum when I work. The town hums back. We've got a thing going.",
            "Wrote a song about this place. It's half love letter, half weather report.",
            "High notes heal things. So does soup. I do both, to be safe."),
        "actor" to listOf(
            "Every day's an audition in this town, darling. EVERY day.",
            "I practiced my surprised face for an hour. Then something actually surprised me. Wasted!",
            "Method acting? I AM the role. The role is 'person buying milk, dramatically.'"),
        "athlete" to listOf(
            "Trained at dawn. The mountain air's my coach and it is MEAN.",
            "Game face isn't a face. It's a lifestyle with a whistle.",
            "I stretch before everything. Conversations. Sandwiches. Everything."),
        "farmer" to listOf(
            "Crops don't care about your feelings, and frankly that's refreshing.",
            "Up before the sun, talking to the rows like they're coworkers.",
            "Weather's not small talk out here. Weather's the whole business plan."),
        "priest" to listOf(
            "The parish keeps me humble. Mostly by breaking things I just fixed.",
            "I light a candle for this town every morning. It takes a while. It's a lot of candles.",
            "Faith's easy on Sunday. Monday's where it earns its keep."),
        "soldier" to listOf(
            "Perimeter checked. All quiet. I check again at lunch. That's not paranoia, that's scheduling.",
            "Boots polished, chin up. A routine is just a mission you can win daily.",
            "I've stood guard in worse places than this. One time a goose was involved. Classified."),
        "scientist" to listOf(
            "I ran the numbers twice. The numbers ran back. Fascinating town, this.",
            "Hypothesis: nothing here is normal. Status: confirmed daily.",
            "My lab coat has a pocket for snacks. Science is hungry work."),
        "lawyer" to listOf(
            "I bill by the hour, so I'll keep this brief. Irony noted.",
            "Won a case on a technicality today. The technicality's name was 'evidence.'",
            "In this town, I keep a spare objection in my pocket at all times."),
        "business" to listOf(
            "Synergy isn't a word, it's a way of life. A profitable one.",
            "I closed two deals and a door I didn't know was open. Growth mindset.",
            "Quarterly goals, daily chaos. The market provides."),
        "reporter" to listOf(
            "A slow news day is a lie this town tells right before it happens.",
            "I've got a notebook for facts and a bigger one for 'allegedly.'",
            "If it bleeds, it leads. Around here, if it merely glows, it also leads."),
        "artist" to listOf(
            "I painted the mountains again. They keep changing their minds about the light.",
            "Sold a piece today! To a tourist! He thinks it's abstract. It's the pond.",
            "Every artist needs a muse. Mine's insomnia and this weird little town."),
        "musician" to listOf(
            "I hear this town in 4/4 time. Even the snow falls on beat.",
            "New song's almost done. It's about a mountain, a bus, and bad decisions.",
            "Practice isn't glamorous. That's why it works."),
        "principal" to listOf(
            "I run a tight ship. The ship is on fire weekly, but it's TIGHT.",
            "Detention rosters are just fan mail with consequences.",
            "A school is a tiny town with bells. I govern both accordingly."),
        "counselor" to listOf(
            "Feelings are weather, friend. I just help folks dress for them.",
            "I keep pamphlets for everything. There's one about pamphlets.",
            "Listening is a skill. This town gives me a LOT of practice."),
        "janitor" to listOf(
            "I see everything in this place. The mop hears more gossip than the radio.",
            "You'd be amazed what kids leave behind. Books. Lunches. Once, a single shoe with a note.",
            "Clean floors, clear conscience. That's the whole philosophy."),
        "waiter" to listOf(
            "Tables turn, tips vary, the regulars never do. That's comfort food for the soul.",
            "I can carry six plates and a grudge. Service with a smile.",
            "Kitchen's loud, dining room's louder. I'm the calm in the middle with your fries."),
        "clerk" to listOf(
            "Stamped forty forms today. Each one somebody's whole week. I stamp gently.",
            "The line's long but it moves. That's my motto for everything, honestly.",
            "Paperwork's just gossip that got organized."),
        "coach" to listOf(
            "Champions aren't born, they're hydrated.",
            "I whistle at practice, at games, once at a wedding. Force of habit.",
            "Teamwork means everybody's tired together. Beautiful thing."),
        "student" to listOf(
            "Homework is just school that follows you home. It knows where I live.",
            "I traded my sandwich for intel today. Worth it. It was a good sandwich though.",
            "When I grow up I'm going to be the person who cancels Mondays."),
        "celebrity" to listOf(
            "Yes, it's really me. No, I don't do autographs at breakfast. Okay, one.",
            "Being famous in a small town means everyone's seen you buy toilet paper. Humbling.",
            "My agent says lay low. My agent has clearly never been to this town."),
        "rapper" to listOf(
            "I put the mountains on the track and the track did numbers.",
            "Bars about altitude. Bars about attitude. I contain multitudes.",
            "Studio time's expensive, so I freestyle everywhere. The bus stop is a venue now."),
        "author" to listOf(
            "Chapter twelve is just me staring out a window with intent. That's writing.",
            "I base my characters on people here. They'll never know. The wizard is the mayor, though.",
            "Writer's block is just the book resting. I tell myself this daily.")
    )

    private val TRAIT_AMBIENT: Map<String, List<String>> = mapOf(
        "kind" to listOf("Brought extra lunch in case somebody forgot theirs. Somebody always does.", "Kindness is free, so I spend it like I'm rich."),
        "mean" to listOf("I told three people their hat was stupid today. It was community service.", "Nice is a hobby for people with worse aim."),
        "angry" to listOf("I'm not yelling. This is my indoor voice now. The town made it this way.", "Counted to ten. Got angrier at seven. It's a gift."),
        "cheerful" to listOf("Good day to be alive, isn't it? I tell the mountains every morning.", "I whistle while I work. The work hasn't complained yet."),
        "shy" to listOf("I practiced saying hello in the mirror. Nailed it. Then I stayed home.", "Crowds are just... a lot of people per people."),
        "nervous" to listOf("Did I lock the door? I checked twice. Checking a third time is just thorough.", "My backup plan has a backup plan. It also worries."),
        "greedy" to listOf("Found a quarter today. Day's a success. Everything else is bonus.", "I don't share snacks. I share snack-ADJACENT conversation."),
        "brave" to listOf("Stood up to the big dog on Fifth Street. We have an understanding now.", "Fear's just data. I collect a lot of data."),
        "funny" to listOf("I did a bit for the bus driver. He almost smiled. I'm counting it as a special.", "Life's short. Punchlines help."),
        "smart" to listOf("Read three chapters before breakfast. The book's about rocks. Riveting, actually.", "I correct people gently. Mostly gently. Okay, correctly."),
        "lazy" to listOf("I did one thing today and I did it slowly. Quality over quantity.", "My couch and I have a relationship built on trust and snacks."),
        "grumpy" to listOf("Hmph. That about covers the day.", "Kids these days. Adults these days. Days, generally."),
        "friendly" to listOf("I know everyone's name on this street. And their dog's name. Priorities.", "Strangers are just friends whose snacks I haven't tried."),
        "rude" to listOf("I say what everyone's thinking. You're welcome. Or whatever.", "Politeness is just honesty wearing a hat. I skip the hat."),
        "quiet" to listOf("I talk plenty. Just... efficiently.", "Some folks fill silence. I let it stretch its legs."),
        "loud" to listOf("I DON'T TALK LOUD, I TALK CLEAR. THERE'S A DIFFERENCE.", "My whisper got a noise complaint once. I'm almost proud."),
        "sweet" to listOf("I baked too much again. 'Too much' is just tomorrow's kindness.", "You look like you needed a compliment today. Consider it delivered."),
        "tough" to listOf("Broke a nail once. Didn't flinch. The nail apologized.", "Cold? This isn't cold. Cold is where I vacation."),
        "sneaky" to listOf("I know a shortcut everywhere. And two exits from every conversation.", "Wasn't eavesdropping. Was gathering ambience."),
        "honest" to listOf("I tell it like it is. People say that's brave. I say it's efficient.", "Lying takes planning. I'm honest out of respect for naptime."),
        "proud" to listOf("I don't brag. I report my achievements with confidence.", "My grandmother says humility's important. She's wrong about one thing, and it's that."),
        "crazy" to listOf("I licked a flagpole on a dare I gave myself. No regrets. Some regrets. Mostly fine.", "My ideas sound wild because I say the quiet part at full volume."),
        "gentle" to listOf("I move spiders outside with a cup and a kind word.", "Soft isn't weak. Soft is just strength that doesn't need the volume up."),
        "strict" to listOf("Rules are rules, even the ones about rules. Especially those.", "I run a tight household. The calendar is color-coded and feared.")
    )

    private val GOTH_AMBIENT: List<String> = listOf(
        "This town's so cheerful it hurts. I come here to suffer beautifully.",
        "I write poetry about the snow. It's all downhill. Literally. It's a mountain town.",
        "The vamp kids think they're dark. Bless. Posers sparkle; real goths brood.",
        "Black is not a phase. Black is load-bearing.",
        "Everyone here conforms to nonconformity differently. I'm the control group."
    )

    private val NEUTRAL_AMBIENT: List<String> = listOf(
        "Just passing through this town's beautiful nonsense, same as everyone.",
        "Days here have a way of getting away from you. In a good way. Mostly.",
        "I keep meaning to leave early and the town keeps being... this. In a nice way."
    )

    /** The ambient pool for a generated NPC of this kind: role
     *  templates first, the goth set when the entry reads goth,
     *  then trait templates; a neutral fallback so the pool is
     *  never empty. Pure and deterministic. */
    fun ambientPoolFor(role: String?, traits: List<String>, goth: Boolean): List<String> {
        val out = mutableListOf<String>()
        val key = role?.let { ROLE_AMBIENT_ALIAS[it] ?: it }
        if (key != null) out += ROLE_AMBIENT[key] ?: emptyList()
        if (goth) out += GOTH_AMBIENT
        for (t in traits) out += TRAIT_AMBIENT[t] ?: emptyList()
        if (out.isEmpty()) out += NEUTRAL_AMBIENT
        return out.distinct()
    }

    fun personaFor(entry: LoreEntry): VisitorPersona =
        cache.getOrPut(entry.name) { generate(entry) }

    fun cached(loreName: String): VisitorPersona? = cache[loreName]

    fun generate(entry: LoreEntry): VisitorPersona {
        val name = entry.name
        val text = entry.text
        val t = text.lowercase()
        val rand = Random(name.hashCode().toLong() * 2654435761L)
        // Entries open with "Name — ..." — strip that lead-in so bios and
        // greetings don't stutter the name or leak the separator.
        val cleanText = text.replaceFirst(
            Regex("^\\s*(The\\s+)?\\Q" + name + "\\E\\s*[—–\\-?:,]*\\s*"), "")
        val sentences = splitSentences(cleanText)
        val bio = (sentences.firstOrNull() ?: cleanText).take(220)
        val facts = sentences.drop(1).take(3).map { it.take(180) }

        val band = detectBand(t, rand)
        val adult = band == AgeBand.ADULT || band == AgeBand.OLDER_TEEN
        val traits = TRAIT_WORDS.filter { it in t }.take(3)
        val role = ROLE_WORDS.firstOrNull { it in t }
        val female = Regex("\\b(she|her|hers|woman|girl|mother|actress|waitress|businesswoman)\\b")
            .containsMatchIn(t)

        // ---------- appearance signals ----------
        var hairColor: String? = null
        for ((words, hex) in HAIR_COLORS) {
            if (words.any { w -> "$w hair" in t || "$w-haired" in t }) { hairColor = hex; break }
        }
        if (hairColor == null && ("blond" in t || "blonde" in t)) hairColor = "#e8c53a"
        val bald = "bald" in t
        val hairStyle = when {
            bald -> "bald"
            "ponytail" in t -> "ponytail"
            "mohawk" in t -> "mohawk"
            "afro" in t -> "afro"
            "long hair" in t -> "long"
            "curly hair" in t -> "mop" // curls are Bebe's signature; mop is the generic read
            "spiky hair" in t -> "spiky"
            "bob cut" in t || "bob hair" in t -> "bob"
            "messy hair" in t -> "messy"
            "buzz cut" in t -> "buzz"
            else -> null
        }
        val beard = adult && ("beard" in t || "bearded" in t)
        val mustache = adult && ("mustache" in t || "moustache" in t)
        val glasses = "glasses" in t && "sunglasses" !in t
        val sunglasses = "sunglasses" in t || "eye patch" in t || "eyepatch" in t
        val hat = when {
            "cowboy hat" in t -> "cowboy"
            "trucker hat" in t -> "trucker"
            "baseball cap" in t -> "cap"
            "beanie" in t -> "beanie"
            "yarmulke" in t -> "yarmulke"
            else -> null
        }
        // clothing: "(color) (shirt|jacket|...)" near-match
        var topColor: String? = null
        var skirt = false
        val clothRe = Regex("(red|blue|green|black|brown|white|gray|grey|purple|pink|orange|yellow|teal|tan)" +
            " (shirt|jacket|coat|hoodie|sweater|dress|robe|suit|blouse|uniform)")
        clothRe.find(t)?.let { m ->
            topColor = COLOR_WORDS[m.groupValues[1]]
            if (m.groupValues[2] == "dress") skirt = true
        }
        if ("wears a dress" in t || "in a dress" in t || " skirt" in t) skirt = true
        val skin = when {
            "dark skin" in t || "dark-skinned" in t -> "#7a5230"
            "brown skin" in t || "brown-skinned" in t -> "#b07848"
            "pale skin" in t || "very pale" in t -> "#f8e8d8"
            else -> null
        }
        // expression set (rig-style modular face) from personality
        val face = when {
            "grumpy" in traits || "angry" in traits || "mean" in traits || "strict" in traits -> "deadpan"
            "cheerful" in traits || "friendly" in traits || "sweet" in traits -> "grin"
            "shy" in traits || "nervous" in traits -> "nervous"
            "proud" in traits || "sneaky" in traits -> "smug"
            female && rand.nextFloat() < 0.3f -> "lashes"
            "braces" in t -> "braces"
            else -> "normal"
        }

        // ---------- assemble (signals win; seeded generic fills gaps) ----------
        val spec = SheetSpec(
            skin = skin ?: SKINS[rand.nextInt(SKINS.size)],
            top = topColor ?: PLAIN_TOPS[rand.nextInt(PLAIN_TOPS.size)],
            bottom = PLAIN_BOTTOMS[rand.nextInt(PLAIN_BOTTOMS.size)],
            bottomStyle = if (skirt) "skirt" else "pants",
            hairStyle = hairStyle ?: if (hairColor == null && rand.nextFloat() < 0.12f) "bald"
                else if (female) GEN_HAIR[rand.nextInt(GEN_HAIR.size)]
                else GEN_HAIR_M[rand.nextInt(GEN_HAIR_M.size)],
            hairColor = hairColor ?: GEN_HAIR_COLORS[rand.nextInt(GEN_HAIR_COLORS.size)],
            hatId = hat ?: GEN_HATS[rand.nextInt(GEN_HATS.size)],
            hatColor = PLAIN_TOPS[rand.nextInt(PLAIN_TOPS.size)],
            beard = beard,
            beardColor = hairColor ?: "#3a2a1a",
            mustache = mustache,
            glasses = glasses,
            sunglasses = sunglasses,
            freckles = "freckles" in t,
            lipstick = female && adult && rand.nextFloat() < 0.5f,
            tall = band.adultGeo,
            face = face,
            // v0.1.7: generated NPCs have no image to measure, so
            // they ride the band ladder — but with SEEDED per-NPC
            // size jitter (rand is name-seeded, so it's stable per
            // person): no two generated figures are the same build.
            width = 0.94f + rand.nextFloat() * 0.12f,
            headScale = 0.95f + rand.nextFloat() * 0.10f
            // sig/special deliberately NEVER set for generated NPCs.
        )

        // ---------- residency + faction + schedule anchors ----------
        val resident = residentScore(t) > 0
        val factionId = Factions.AFFILIATION_KEYWORDS.firstOrNull { (_, words) ->
            words.any { it in t }
        }?.first
        val surname = name.substringAfterLast(" ")
        val homeBySurname = when (surname) {
            "Marsh" -> "StanHouse"; "Broflovski" -> "KyleHouse"
            "Cartman" -> "CartmanHouse"; "McCormick" -> "KennysHouse"
            "Stotch" -> "ButtersHouse"; "Black" -> "TolkienHouse"
            else -> null
        }
        val houses = listOf("KennysHouse", "ButtersHouse", "CartmanHouse",
            "StanHouse", "KyleHouse", "TolkienHouse")
        val home = if (resident) homeBySurname ?: houses[rand.nextInt(houses.size)] else null
        val student = band == AgeBand.KID || band == AgeBand.YOUNG_TEEN || "student" in t
        val workLoc = if (resident) {
            if (student) "Elementary" else role?.let { WORKPLACE[it] } ?: "MainStreet"
        } else null
        val hangout = if (resident) hangoutFor(t, rand) else null

        // ---------- talk lines derived from the entry ----------
        val shortBio = LoreBook.excerpt(bio, 130)
        val greet = mutableListOf<String>()
        greet.add("Hey. I'm $name. $shortBio")
        if (role != null) greet.add("Name's $name — I'm the $role type around here. $shortBio")
        if (traits.isNotEmpty()) greet.add("I'm $name. People say I'm ${traits.joinToString(", ")}. They're not wrong.")
        greet.add("Oh, hi. $name. Just passing through your day, I guess.")
        val factLines = facts.map { "Did you know? $it" } +
            (if (role != null) listOf("Being a $role in this town? Whole story. $shortBio") else emptyList()) +
            listOf("People around here know me: $shortBio")
        val deepLines = (facts.drop(1) + facts).map { "Between you and me? $it" } +
            listOf("You're alright. Most people here never ask about me. $shortBio")
        val bye = when {
            "grumpy" in traits -> listOf("Yeah, yeah. Bye.", "Hmph. Later.")
            "cheerful" in traits || "friendly" in traits ->
                listOf("Bye! This was nice!", "See you around, friend!")
            else -> listOf("See you around.", "Later, then.")
        }

        // #23 GENERATED TASTES ARE REAL TASTES: loves derive from
        // role + traits, in the same tag vocabulary items use.
        val loves = mutableSetOf<String>()
        when (role) {
            "chef", "cook" -> loves.add("food")
            "singer", "musician", "rapper" -> loves.add("music")
            "artist", "author" -> loves.add("curious")
            "teacher", "principal", "counselor" -> loves.add("coffee")
        }
        if ("coffee" in t) loves.add("coffee")
        if ("music" in t || " song" in t) loves.add("music")
        if ("romantic" in traits || "sweet" in traits) loves.add("romantic")
        if ("curious" in traits || "sneaky" in traits) loves.add("curious")

        return VisitorPersona(
            loreName = name, spec = spec, band = band, traits = traits, role = role,
            bio = bio, facts = facts, resident = resident, factionId = factionId,
            home = home, workLoc = workLoc, hangout = hangout,
            greetingLines = greet, factLines = factLines.ifEmpty { greet },
            deepLines = deepLines, byeLines = bye,
            ambientLines = ambientPoolFor(role, traits, "goth" in t),
            loves = loves
        )
    }

    private fun bandForAge(age: Int): AgeBand = when {
        age <= 12 -> AgeBand.KID
        age <= 14 -> AgeBand.YOUNG_TEEN
        age <= 16 -> AgeBand.TEEN
        age <= 18 -> AgeBand.OLDER_TEEN
        else -> AgeBand.ADULT
    }

    private fun splitSentences(text: String): List<String> =
        text.split(Regex("(?<=[.!?])\\s+")).map { it.trim() }.filter { it.length > 12 }

    private fun detectBand(t: String, rand: Random): AgeBand {
        val ageRe = Regex("(\\d{1,2})[- ]year[- ]old|(\\d{1,2}) years old")
        ageRe.find(t)?.let { m ->
            val age = (m.groupValues[1].ifEmpty { m.groupValues[2] }).toIntOrNull()
            if (age != null) return bandForAge(age)
        }
        val wordAges = mapOf("six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
            "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
            "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
            "eighteen" to 18, "nineteen" to 19, "twenty" to 20)
        val wordRe = Regex("(${wordAges.keys.joinToString("|")})[- ]year[- ]old")
        wordRe.find(t)?.let { m ->
            return bandForAge(wordAges[m.groupValues[1]] ?: 30)
        }
        return when {
            "fourth grader" in t || "third grader" in t || "fifth grader" in t ||
                "elementary school student" in t ||
                "student at south park elementary" in t ||
                "attends south park elementary" in t ||
                "goes to south park elementary" in t -> AgeBand.KID
            "middle school" in t -> AgeBand.YOUNG_TEEN
            "high school senior" in t || "high-school senior" in t -> AgeBand.OLDER_TEEN
            "high school" in t -> AgeBand.TEEN
            "teenager" in t || "teenaged" in t -> AgeBand.TEEN
            "college student" in t -> AgeBand.OLDER_TEEN
            "young adult" in t -> AgeBand.OLDER_TEEN
            else -> AgeBand.ADULT
        }
    }

    private fun residentScore(t: String): Int {
        var s = 0
        if ("lives in south park" in t) s += 2
        if ("resident of south park" in t || "south park resident" in t) s += 2
        if ("student at south park elementary" in t) s += 2
        if ("moved to south park" in t) s += 1
        if ("his home in south park" in t || "her home in south park" in t) s += 1
        if ("works at the" in t && ("school" in t || "church" in t)) s += 1
        return s
    }

    private fun hangoutFor(t: String, rand: Random): String = when {
        "coffee" in t -> "TweekBros"
        "music" in t || "sing" in t -> "Bijou"
        "basketball" in t || "football" in t || "sport" in t -> "KidsPark"
        "nature" in t || "fishing" in t || "pond" in t -> "StarksPond"
        "shopping" in t || "mall" in t -> "Mall"
        "church" in t || "pray" in t -> "Church"
        "bar" in t || "drink" in t -> "SkeetersBar"
        "farm" in t -> "TegridyFarms"
        else -> listOf("MainStreet", "KidsPark", "Mall", "TweekBros", "StarksPond")[rand.nextInt(5)]
    }

    /** Weekly drift for residents: deterministic per (name, week). One
     *  hangout block may relocate to faction turf or a friend's spot. */
    fun residentHangout(persona: VisitorPersona, weekKey: Int): String {
        val base = persona.hangout ?: return "MainStreet"
        val r = Random(persona.loreName.hashCode().toLong() * 97 + weekKey)
        if (r.nextFloat() >= 0.35f) return base
        val turf = persona.factionId?.let { FACTION_BY_ID[it]?.turf?.firstOrNull() }
        return turf ?: listOf("KidsPark", "Mall", "TweekBros", "MainStreet")[r.nextInt(4)]
    }

    /** A resident's schedule block at [timeMin]: (locationId, activity).
     *  [dow] (0=Sun..6=Sat, -1 = unknown) gives residents weekends too. */
    fun residentBlockAt(persona: VisitorPersona, timeMin: Int, weekKey: Int,
                        dow: Int = -1): Pair<String, String> {
        val home = persona.home ?: "MainStreet"
        val work = persona.workLoc ?: "MainStreet"
        val hang = residentHangout(persona, weekKey)
        if ((dow == 0 || dow == 6) && timeMin in 9 * 60 until 15 * 60 + 30) {
            return (if (dow == 0 && timeMin < 13 * 60) "Church" else hang) to
                "Weekend off. Nowhere to be."
        }
        return when {
            timeMin < 8 * 60 -> home to "Home. Starting the day slow."
            timeMin < 9 * 60 -> home to "Getting ready at home."
            timeMin < 15 * 60 + 30 -> work to (if (work == "Elementary") "At school." else "At work.")
            timeMin < 18 * 60 -> hang to "Hanging out."
            timeMin < 21 * 60 -> home to "Home for the evening."
            else -> home to "Winding down at home."
        }
    }
}

// ============================================================
// VisitorSystem — who of the generated population is in town.
//
// Pool: lorebook char entries that are NOT the 32 cast (checked
// against LORE_CHAR_ALIASES values + cast names). ~2,190 entries
// qualify; personas generate lazily and cache.
//  - RESIDENTS (~6): entries whose text says they live in town.
//    Present every day, placed by their generated schedules.
//  - VISITORS (8 at a time): everyone else, rotating in 4-hour
//    blocks on the in-game clock, seeded by (day, block) so the
//    same faces hold for a block and recur across days.
// Positions are pure functions of (identity, day, block) — no
// save state needed; memory keys off the lorebook name anyway.
// ============================================================
object VisitorSystem {
    private val VISIT_LOCS = listOf(
        "MainStreet", "MainStreet", "Mall", "TweekBros", "CityWok", "KidsPark",
        "Bijou", "SkeetersBar", "Church", "StarksPond", "BusStop", "FreemansTacos",
        "Raisins", "JimbosGuns", "Elementary", "CityHall",
        // v0.1.8 #39 coverage: every location joins the living routes
        "TomsRhino", "Bank", "PostOffice", "ButtersHouse", "KennysHouse",
        "TolkienHouse", "SoDoSoPa"
    )
    const val VISITORS_AT_ONCE = 8
    const val RESIDENT_COUNT = 6

    /** v0.2.0g: the move-in cap. Converted residents are forever
     *  (they join every day's roster and the Journal's town
     *  roll), so conversions stop here — a generous twenty-four
     *  on top of the six base residents. The cap gates NEW
     *  conversions only: a save already past it keeps every
     *  resident it earned (nothing is ever trimmed on load). */
    const val MAX_CONVERTED = 24

    /** v0.1.8 UI-wired providers (GameActivity sets these from the
     *  live save + memory store; defaults = v0.1.7 behavior). */
    var favoritesProvider: () -> Set<String> = { emptySet() }
    var influenceProvider: () -> Map<String, Int> = { emptyMap() }
    var convertedProvider: () -> List<String> = { emptyList() }

    private var eligibleCache: List<LoreEntry>? = null
    private var residentsCache: List<VisitorPersona>? = null

    private fun normName(s: String): String =
        s.lowercase().filter { it.isLetterOrDigit() || it == ' ' }.trim()

    fun eligible(): List<LoreEntry> {
        eligibleCache?.let { return it }
        if (!LoreBook.loaded) return emptyList()
        val castLore = LORE_CHAR_ALIASES.values.map { normName(it) }.toSet()
        val castGame = KID_ORDER.map { normName(it) }.toSet()
        val out = LoreBook.byCat("char").filter { e ->
            e.text.length >= 80 &&
                !e.name.startsWith("List of") &&
                !e.name.contains("(Disambiguation)") &&
                normName(e.name) !in castLore && normName(e.name) !in castGame
        }
        eligibleCache = out
        return out
    }

    fun poolSize(): Int = eligible().size

    fun residents(): List<VisitorPersona> {
        residentsCache?.let { cache ->
            // #18: converted visitors join the cached base residents
            val converted = convertedProvider().mapNotNull { name ->
                cache.firstOrNull { it.loreName == name } ?: convertedPersona(name)
            }
            return (cache + converted).distinctBy { it.loreName }
        }
        val scored = eligible().map { e -> e to residentScoreOf(e.text.lowercase()) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<LoreEntry, Int>> { it.second }
                .thenBy { it.first.name.hashCode() })
        val out = scored.take(RESIDENT_COUNT).map { NpcGenerator.personaFor(it.first) }
        residentsCache = out
        return residents()
    }

    /** #18 VISITORS CAN MOVE IN: a converted visitor's persona,
     *  re-homed — surname house if any, else a seeded house; a work
     *  spot from their role; a hangout like any resident. */
    private fun convertedPersona(loreName: String): VisitorPersona? {
        val entry = eligible().firstOrNull { it.name == loreName } ?: return null
        val base = NpcGenerator.personaFor(entry)
        if (base.resident) return base
        val rand = Random(loreName.hashCode().toLong() * 7717)
        val surname = loreName.substringAfterLast(" ")
        val homeBySurname = when (surname) {
            "Marsh" -> "StanHouse"; "Broflovski" -> "KyleHouse"
            "Cartman" -> "CartmanHouse"; "McCormick" -> "KennysHouse"
            "Stotch" -> "ButtersHouse"; "Black" -> "TolkienHouse"
            else -> null
        }
        val houses = listOf("KennysHouse", "ButtersHouse", "CartmanHouse",
            "StanHouse", "KyleHouse", "TolkienHouse")
        val home = homeBySurname ?: houses[rand.nextInt(houses.size)]
        val work = base.workLoc ?: base.role?.let { r ->
            listOf("Elementary", "Church", "CityHall", "MainStreet")[rand.nextInt(4)]
        } ?: "MainStreet"
        val hang = base.hangout
            ?: listOf("MainStreet", "KidsPark", "Mall", "TweekBros", "StarksPond")[rand.nextInt(5)]
        return base.copy(resident = true, home = home, workLoc = work, hangout = hang)
    }

    fun isResident(loreName: String): Boolean =
        residents().any { it.loreName == loreName }

    private fun residentScoreOf(t: String): Int {
        var s = 0
        if ("lives in south park" in t) s += 2
        if ("resident of south park" in t || "south park resident" in t) s += 2
        if ("student at south park elementary" in t) s += 2
        if ("moved to south park" in t) s += 1
        if ("his home in south park" in t || "her home in south park" in t) s += 1
        return s
    }

    private fun pickIndices(poolSize: Int, todayKey: String, block: Int): Set<Int> {
        val rand = Random(dailySeed(todayKey) * 31 + block * 7919)
        val picks = mutableSetOf<Int>()
        var guard = 0
        while (picks.size < VISITORS_AT_ONCE && guard++ < 60) {
            picks.add(rand.nextInt(poolSize))
        }
        return picks
    }

    /** Visitors present in this 4-hour block of the in-game day. */
    fun visitorsFor(todayKey: String, timeMin: Int): List<ActiveNpc> {
        val pool = eligible()
        if (pool.isEmpty()) return emptyList()
        val residentNames = residents().map { it.loreName }.toSet()
        val block = timeMin / 240
        val picks = pickIndices(pool.size, todayKey, block).toMutableSet()
        // #19 FAVORITES RETURN: people you've befriended come back
        // sooner (seeded swap-in chance) and stay longer (a favorite
        // in last block's picks carries into this one).
        val favs = favoritesProvider()
        if (favs.isNotEmpty()) {
            val prev = if (block > 0) pickIndices(pool.size, todayKey, block - 1)
                else emptySet()
            val favIdx = pool.indices.filter { pool[it].name in favs }
            for (fi in favIdx) {
                if (fi in picks) continue
                val carry = fi in prev
                val sooner = Random(dailySeed(todayKey) * 7 + block * 131 + fi).nextBoolean()
                if (carry || sooner) {
                    val evict = picks.lastOrNull { pool[it].name !in favs }
                    if (evict != null) picks.remove(evict)
                    picks.add(fi)
                }
            }
        }
        // #36 RISING FACTIONS DRAW CROWDS: visitor spots tilt toward
        // the turf of whichever faction is gaining influence.
        val topFaction = influenceProvider().maxByOrNull { it.value }?.key
        val tiltTurf = topFaction?.let { FACTION_BY_ID[it]?.turf }
        val out = mutableListOf<ActiveNpc>()
        for (i in picks) {
            val e = pool[i]
            if (e.name in residentNames) continue
            val p = NpcGenerator.personaFor(e)
            val lr = Random(e.name.hashCode().toLong() * 131 + dailySeed(todayKey) + block)
            var loc = VISIT_LOCS[lr.nextInt(VISIT_LOCS.size)]
            if (tiltTurf != null && p.factionId == topFaction && lr.nextFloat() < 0.45f) {
                loc = tiltTurf[lr.nextInt(tiltTurf.size)]
            }
            // v0.1.9e NPC GOALS: half of visits are quietly ON
            // PURPOSE — the visitor's goal place pulls their
            // seeded spot toward it.
            val g = NpcGoals.goalFor(p.loreName, NpcGoals.cycleOf(todayKey))
            if (g.locId != null && lr.nextFloat() < 0.5f) loc = g.locId!!
            val inside = CharacterSystem.isBuilding(loc) && lr.nextFloat() < 0.65f
            out.add(ActiveNpc(p, loc, inside))
        }
        return out
    }

    /** Residents placed by their schedules right now. When the
     *  caller passes [dayKey], hangout blocks may bend toward the
     *  resident's current goal place (v0.1.9e NPC goals) — the
     *  same deviation the cast gets in the living transform. */
    fun residentsNow(timeMin: Int, weekKey: Int, night: Boolean, dow: Int = -1,
                     dayKey: String = ""): List<ActiveNpc> {
        if (night) return emptyList() // residents sleep at home, off-stage
        return residents().map { p ->
            val block = NpcGenerator.residentBlockAt(p, timeMin, weekKey, dow)
            var loc = block.first
            if (dayKey.isNotEmpty()) {
                val bendable = block.second == "Hanging out." ||
                    block.second == "Weekend off. Nowhere to be."
                if (bendable) {
                    val g = NpcGoals.goalFor(p.loreName, NpcGoals.cycleOf(dayKey))
                    if (g.locId != null && g.locId != loc) {
                        val probe = Random(p.loreName.hashCode().toLong() * 131 +
                            dayKey.hashCode() + timeMin / 60)
                        if (probe.nextDouble() < 0.45) loc = g.locId!!
                    }
                }
            }
            ActiveNpc(p, loc, CharacterSystem.isBuilding(loc))
        }
    }

    /** #21: during a cluster-stage event, up to two generated
     *  residents join the crowd at the cluster location. */
    fun eventCrowd(todayKey: String, def: ShenaniganDef?, stageIdx: Int): List<ActiveNpc> {
        if (def == null || stageIdx !in def.stages.indices) return emptyList()
        val stage = def.stages[stageIdx]
        val loc = stage.clusterLoc ?: return emptyList()
        if (stage.routine != "cluster") return emptyList()
        val rand = Random(dailySeed(todayKey) + def.id.hashCode())
        return residents().shuffled(rand).take(2).map { p ->
            ActiveNpc(p, loc, CharacterSystem.isBuilding(loc))
        }
    }

    /** Everyone generated who is at [locId] in the same inside/outside space. */
    fun presentAt(locId: String, inside: Boolean, todayKey: String, timeMin: Int,
                  weekKey: Int, dow: Int = -1): List<ActiveNpc> {
        val night = isSleepHour(timeMin / 60)
        val all = visitorsFor(todayKey, timeMin) +
            residentsNow(timeMin, weekKey, night, dow, todayKey)
        return all.filter { it.locId == locId && it.inside == inside }
    }

    fun personaByName(loreName: String): VisitorPersona? {
        NpcGenerator.cached(loreName)?.let { return it }
        val e = LoreBook.entry(loreName) ?: return null
        return NpcGenerator.personaFor(e)
    }
}

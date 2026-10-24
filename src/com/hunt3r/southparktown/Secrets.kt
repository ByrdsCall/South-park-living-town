package com.hunt3r.southparktown

// ============================================================
// Town secrets — v0.7.0b (Queue #7 item 2): the town keeps
// a dozen small things it never mentions.
//
// A secret is an unmarked, one-time discovery: a careful
// Look in the right place while its gate stands open (or a
// tap on one of two props the town already lets you touch —
// the bus-stop bench, the traveling gnome). Nothing about
// the scenery changes and nothing teases: with the gate
// shut, the spot reads as ordinary scenery, because it is.
// With the gate open, discovery is deterministic — no
// rolls, no grinding. First time only; then it's found
// forever, written up in the Journal's 🔎 TOWN SECRETS
// page, where the still-unfound are carried as riddles and
// nothing else: no locations, no triggers, no gates — the
// riddle is the only sanctioned hint in town.
//
// Rewards stay modest and honest: a few coins where a find
// plausibly holds them, one collectible the gnome was
// carrying anyway, a feat at half the set, and — for the
// full twelve — a house decor piece the House derives from
// the found-set like every other piece it hangs.
//
// 🔴 SPOILER DISCIPLINE: no secret reveals, hints at, or
// depends on the solution of any case (arcs 1–4). A secret
// may share a street with case scenery; its content never
// touches a case. The suite scans every player-facing word
// here against the cases' denylist.
//
// State: no new save keys — the found-set rides the
// existing `life`/`lifeText` maps, one record per secret
// (lifeText["secret:<id>"] = "<absDay>|<locId>"), every
// read clamped against the registry below, so a v0.7.0a
// save loads with zero secrets found and a hostile doc
// can't mint finds that don't exist.
// ============================================================

object Secrets {

    // ---------------- the dials (all suite-pinned) ----------------

    /** Lifetime secrets in the registry. */
    const val SECRET_COUNT = 12

    /** Finds that earn the Town Sleuth feat. */
    const val FEAT_THRESHOLD = 6

    /** The feat's name, as the Journal page carries it. */
    const val FEAT_NAME = "🔎 Town Sleuth"

    /** The memory topic a discovery plants in a witness. */
    const val TOPIC = "town secret"

    // ---------------- triggers & gates ----------------

    const val TRIGGER_LOOK = "look"
    const val TRIGGER_BENCH = "bench"
    const val TRIGGER_GNOME = "gnome"

    // Gate ids — each names its whole condition, so no two
    // secrets in the registry share a (trigger, gate) shape.
    const val GATE_ALWAYS_OUT = "always_out"     // any time, outdoors
    const val GATE_ALWAYS_IN = "always_in"       // any time, indoors
    const val GATE_DAY_IN = "day_in"             // daylight band, indoors
    const val GATE_DUSK_IN = "dusk_in"           // dusk band, indoors
    const val GATE_NIGHT_IN = "night_in"         // night band, indoors
    const val GATE_AURORA_OUT = "aurora_out"     // an aurora is up, outdoors
    const val GATE_SNOW_OUT = "snow_out"         // snow-family weather, outdoors
    const val GATE_RAIN_OUT = "rain_out"         // rain, outdoors
    const val GATE_SPRING_OUT = "spring_out"     // spring, outdoors
    const val GATE_VOTE_ANY = "vote_any"         // some outcome stands, either side of the door
    const val GATE_ALWAYS = "always"             // bench: any time, the tap is the gate
    const val GATE_NIGHT = "night"               // gnome: night band

    /** Snow-family weather ids (the set Look's snowball
     *  stash already uses). */
    val SNOW_WEATHERS: Set<String> = setOf("light_snow", "heavy_snow", "blizzard")

    data class Secret(
        val id: String,
        val title: String,          // the found name (Journal + morning line)
        val locId: String?,         // home location; null = travels (the gnome)
        val trigger: String,        // TRIGGER_*
        val gate: String,           // GATE_*
        val riddle: String,         // the ONLY hint: no place, trigger or gate named
        val reveal: String,         // the discovery beat, read at the moment
        val blurb: String,          // the Journal's found-entry lore line
        val coins: Int = 0,         // capsule coins the find plausibly holds
        val collectibleId: String? = null  // granted if unowned (coins are the fallback)
    )

    // ---------------- the registry: twelve quiet things ----------------

    val SECRETS: List<Secret> = listOf(
        Secret("dock_carving", "The Dock Census", "StarksPond",
            TRIGGER_LOOK, GATE_ALWAYS_OUT,
            "Something below the waterline has been keeping the town's " +
                "names for longer than the town has.",
            "🔎 Under the dock rail, where the wood stays wet, somebody " +
                "started carving initials a long time ago — S + K, a date " +
                "older than anyone will admit to — and the town has been " +
                "adding pairs ever since, each shallower than the last. " +
                "Wired to a piling in a sandwich bag: four quarters, " +
                "black with pond water. Dock tax. It's yours now. (+4 🪙)",
            "Initials under the dock rail, generation over generation, " +
                "and a piling that pays a small tax in blackened quarters.",
            coins = 4),
        Secret("second_plow", "The Second Plow", "TegridyFarms",
            TRIGGER_LOOK, GATE_AURORA_OUT,
            "The old farmers planted by a pattern no almanac prints " +
                "anymore. On the right evenings, it still shows up to work.",
            "🔎 With the sky rippling green, the field rows line up with " +
                "something overhead: seven bright points in the shape of " +
                "a plow, the Second Plow, the one the old farmers planted " +
                "by before there were almanacs to argue with. Randy is " +
                "standing in the yard in his bathrobe, pointing. For once, " +
                "he's right about the sky.",
            "A constellation shaped like a plow that only the field " +
                "rows agree with, visible when the sky does its green thing.",
            coins = 0),
        Secret("confessional_marks", "The Confessional's Other Side", "Church",
            TRIGGER_LOOK, GATE_ALWAYS_IN,
            "Generations have whispered their worst through a curtain, " +
                "and the wood on the other side remembers every one.",
            "🔎 Behind the confessional curtain, at hand height, the wood " +
                "is initialed by generations — shallow cuts, deep cuts, one " +
                "set still pale and fresh enough that the grain hasn't " +
                "darkened yet. You look at it for a while. You don't say " +
                "whose. Some things the town keeps for itself, and now " +
                "you're one of the keepers.",
            "Hand-cut initials behind the confessional curtain, the " +
                "newest still pale. You keep the town's counsel now.",
            coins = 0),
        Secret("seat_k9", "Seat K9", "Bijou",
            TRIGGER_LOOK, GATE_DUSK_IN,
            "Where lost things wait out their owners, the oldest ticket " +
                "in town is still saving somebody a seat.",
            "🔎 In the lost-and-found, at the hour the marquee comes on, " +
                "the low light finds it: a ticket stub from the theater's " +
                "first winter, and under the counter's lip a penciled note " +
                "in a careful hand — SEAT K9. BEST IN THE HOUSE. Somebody " +
                "loved this room enough to leave instructions. You leave " +
                "the stub where it lives. It is still, technically, " +
                "holding the seat.",
            "A first-winter ticket stub and a penciled verdict in the " +
                "lost-and-found: seat K9, best in the house.",
            coins = 0),
        Secret("ones_still_waiting", "For the Ones Still Waiting", "BusStop",
            TRIGGER_BENCH, GATE_ALWAYS,
            "Everyone waits in the same place. Almost nobody has ever " +
                "checked what the waiting is bolted to.",
            "🔎 You tip forward and look under the bench, because nobody " +
                "ever has. Screwed to the underside is a small brass plate, " +
                "hand-engraved, letters slightly drunk: FOR THE ONES STILL " +
                "WAITING. Under the plate, two quarters sit fused in gum " +
                "older than you are. Town ruling, unwritten but absolute: " +
                "whoever finally looks, keeps. (+2 🪙)",
            "A hand-engraved brass plate under the bus-stop bench, for " +
                "everyone who ever waited there. Which is everyone.",
            coins = 2),
        Secret("traveling_tag", "Property of the Traveling", null,
            TRIGGER_GNOME, GATE_NIGHT,
            "Something small travels the whole town and never says a " +
                "word. It only stops for people who are still awake.",
            "🔎 Tonight the gnome lets you tip its hat — slowly, like " +
                "it's deciding. Sewn under the brim is a cloth tag: " +
                "PROPERTY OF THE TRAVELING. DO NOT UNFOLD. And in the " +
                "hat's lining, folded with terrible neatness, a pair of " +
                "gnome underpants. Phase 1, complete. The gnome permits " +
                "this. The gnome permits nothing else.",
            "A sewn tag under the traveling gnome's hat, and a neatly " +
                "folded pair of underpants in the lining. Phase 1.",
            coins = 3, collectibleId = "gnome_underpants"),
        Secret("the_class_that_never_met", "The Class That Never Met", "Elementary",
            TRIGGER_LOOK, GATE_DAY_IN,
            "There's a roll of honor somewhere for a class that never " +
                "met, never enrolled, never missed a day — and its grades " +
                "were better than yours.",
            "🔎 Behind the Hall of Honor frame — it rocks a quarter-inch " +
                "loose, it always has — hangs a second roll, hand-lettered " +
                "on the back of a poster: an honor class of eleven names " +
                "nobody in this town has ever had. Perfect marks, all of " +
                "them, in subjects the school doesn't teach. The frame " +
                "goes back exactly as it was. They earned their privacy.",
            "A hand-lettered honor roll behind the official one, for a " +
                "class of eleven that never enrolled. Straight A's.",
            coins = 0),
        Secret("the_third_box", "The Third Box", "CityHall",
            TRIGGER_LOOK, GATE_VOTE_ANY,
            "The town's very first decision is kept safe somewhere, and " +
                "somebody, later, in a different crayon, improved it.",
            "🔎 At the back of the count room, framed under glass gone " +
                "wavy with age: the town's first ballot. Butcher paper, " +
                "crayon, one question nobody can read anymore, two boxes — " +
                "and a third box, drawn in later in a different crayon, " +
                "labeled BOTH, with the biggest checkmark of the three. " +
                "The town has been splitting the difference ever since. " +
                "It just got better at hiding it.",
            "The town's first ballot, crayon on butcher paper, with a " +
                "third box — BOTH — added later by a bolder hand.",
            coins = 0),
        Secret("the_warm_ring", "The Warm Ring", "KidsPark",
            TRIGGER_LOOK, GATE_SNOW_OUT,
            "Some mornings, the ground has been stood on all night by " +
                "something warm, in a perfect circle, that never arrived " +
                "and never left.",
            "🔎 In the fresh snow by the swings there's a perfect ring " +
                "melted down to the grass, wide as a kid is tall, where " +
                "something stood all night being warm. No prints lead in. " +
                "None lead out. The swings are moving, very slightly, in " +
                "no wind at all. You write it in the Journal and you do " +
                "not solve it. Some entries are just for keeping.",
            "A ring melted in fresh snow by the swings, warm all night, " +
                "with no way in or out of it. Kept, not solved.",
            coins = 0),
        Secret("admission_one_button", "Admission: One Button", "MainStreet",
            TRIGGER_LOOK, GATE_RAIN_OUT,
            "Ten years of weather couldn't do it, but one good soaking " +
                "can: wash what's written over, and read what's written " +
                "underneath.",
            "🔎 The rain has done what ten years of weather couldn't: " +
                "washed the grime and the current flyer clean off the old " +
                "poster frame, and underneath, printed when the street " +
                "was new, there's an older one — FIRST PICTURE SHOW, and " +
                "below, in type as big as your hand: ADMISSION: ONE " +
                "BUTTON. In the frame's corner, a button, black with age. " +
                "The bank gives you 3 🪙 for it, no questions. The poster " +
                "stays. It's holding up the frame. (+3 🪙)",
            "The street's first picture-show poster, priced in buttons, " +
                "hiding under a decade of newer paper.",
            coins = 3),
        Secret("still_gaining", "Still Gaining", "StanHouse",
            TRIGGER_LOOK, GATE_SPRING_OUT,
            "Three generations measured themselves against the same " +
                "frame, and one of them is still, out of pure spite, gaining.",
            "🔎 Spring cleaning has the garage door up, and there it is " +
                "on the frame, in pencil, in three hands: GRANDPA, in " +
                "capitals that lean like fenceposts. DAD, climbing fast and " +
                "then stopping all at once, the way pencil lines do. And " +
                "near the top, dated three different years, the same mark " +
                "re-measured and re-argued: RANDY, AGE 34. Still gaining. " +
                "Out of spite, mostly.",
            "A three-generation height chart on a garage frame, and one " +
                "grown mark still being re-argued upward every spring.",
            coins = 0),
        Secret("put_it_back", "Put It Back", "YourHouse",
            TRIGGER_LOOK, GATE_NIGHT_IN,
            "Your own floor has a board that sits a hair too proud. " +
                "Whatever sleeps under it was put there with love, and " +
                "with a warning.",
            "🔎 It's past dark when you finally check the floorboard by " +
                "the bed that sits a hair too proud. Under it, flat and " +
                "careful: a kid's crayon drawing of this house with one " +
                "extra room, and a note in pencil, spelled like it " +
                "mattered — THE HOUSE KEEPS WHAT YOU LOVE. PUT IT BACK. " +
                "You look at the drawing a long time. Then you put it " +
                "back. The board sits flush now. It just needed to be " +
                "known about, maybe.",
            "A crayon drawing of your house with one extra room, and a " +
                "note: the house keeps what you love. You put it back.",
            coins = 0)
    )

    fun secretById(id: String): Secret? = SECRETS.firstOrNull { it.id == id }

    // ---------------- the found-set (life/lifeText only) ----------------

    private fun kRec(id: String) = "secret:$id"

    /** The discovery record for [secret]: (absDay, locId) as
     *  written at the moment, or null if unfound. Reads clamp
     *  to the registry and to sane values — a hostile record
     *  parses to nothing. */
    fun discoveryOf(state: GameState, secret: Secret): Pair<Int, String>? {
        val raw = state.lifeText[kRec(secret.id)] ?: return null
        val parts = raw.split("|")
        if (parts.size != 2) return null
        val day = parts[0].toIntOrNull() ?: return null
        if (day < 0 || parts[1] !in LOCATIONS) return null
        return day to parts[1]
    }

    fun isFound(state: GameState, secret: Secret): Boolean =
        discoveryOf(state, secret) != null

    fun foundSecrets(state: GameState): List<Secret> =
        SECRETS.filter { isFound(state, it) }

    fun foundCount(state: GameState): Int = foundSecrets(state).size

    fun featDone(state: GameState): Boolean = foundCount(state) >= FEAT_THRESHOLD

    // ---------------- gates ----------------

    private fun band(state: GameState): String = SceneLight.bandOf(state.timeMin)

    private fun season(state: GameState): Season = seasonFor(state.gameMonth)

    /** Whether [secret]'s gate stands open in [state] right
     *  now. Pure in the state: the same moment reads the same
     *  on every surface. */
    fun gateOpen(state: GameState, secret: Secret): Boolean = when (secret.gate) {
        GATE_ALWAYS_OUT -> !state.playerInside
        GATE_ALWAYS_IN -> state.playerInside
        GATE_DAY_IN -> state.playerInside && band(state) == "day"
        GATE_DUSK_IN -> state.playerInside && band(state) == "dusk"
        GATE_NIGHT_IN -> state.playerInside && band(state) == "night"
        GATE_AURORA_OUT -> !state.playerInside &&
            SkyState.auroraNow(state.absDay, state.timeMin,
                state.weatherId, season(state))
        GATE_SNOW_OUT -> !state.playerInside && state.weatherId in SNOW_WEATHERS
        GATE_RAIN_OUT -> !state.playerInside && state.weatherId == "rain"
        GATE_SPRING_OUT -> !state.playerInside && season(state) == Season.SPRING
        GATE_VOTE_ANY -> Votes.QUESTIONS.any {
            Votes.standingOption(state, it) != null }
        GATE_ALWAYS -> true
        GATE_NIGHT -> band(state) == "night"
        else -> false
    }

    // ---------------- witnesses ----------------

    /** Who shares the player's exact space and is awake —
     *  the same presence rule the rest of the town uses. */
    private fun witnesses(state: GameState): List<Agent> {
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return emptyList()
        return state.agents.values.filter {
            !it.isPlayer && it.location == me.location &&
                it.waypoints.isEmpty() &&
                CharacterSystem.npcInside(it.location) == state.playerInside &&
                !it.asleep
        }.sortedBy { it.name }
    }

    private val REACTIONS: List<String> = listOf(
        "{name} stares at it for a long moment. “How long has THAT been there?”",
        "{name} looks at it, then at you. “We tell no one. This one is ours.”",
        "{name}: “Okay. That has been there the entire time, and I choose not to process it.”",
        "{name} whistles, low. “Town's older than it lets on, huh.”")

    private fun plantSecretTopic(name: String) {
        if (name !in KID_ORDER) return
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    // ---------------- discovery ----------------

    /** Settle [secret] as found, right now: the record, the
     *  reward (exactly once — the record is the guard), the
     *  morning beat, the witness's memory. Returns the text
     *  the player reads. Callers gate first; this trusts the
     *  gate was open and the secret unfound. */
    fun discover(state: GameState, secret: Secret): String {
        val me = state.agents.values.firstOrNull { it.isPlayer }
        val locId = me?.location ?: secret.locId ?: "MainStreet"
        state.lifeText[kRec(secret.id)] =
            "${state.absDay}|${if (locId in LOCATIONS) locId else "MainStreet"}"
        state.life["secretBeatDay"] = state.absDay
        state.lifeText["secretBeat"] = secret.title
        var text = secret.reveal
        // the tangible, if any: a collectible the find was
        // carrying (yours if you don't own it; its coin worth
        // in change if you do), else the find's own coins.
        val coll = secret.collectibleId
            ?.let { collectibleById(it) }
        if (coll != null && coll.id !in state.collected) {
            state.collected.add(coll.id)
            text += "\n📦 KEEPSAKE — ${coll.name} ${coll.icon} joins your " +
                "collection. (It's in the Journal's collection.)"
        } else if (secret.coins > 0) {
            Minigames.awardCoins(state, secret.coins)
            if (coll != null) {
                text += "\n(You already own the ${coll.name}; the find " +
                    "settles in change instead: +${secret.coins} 🪙.)"
            }
        }
        // the town notices, exactly once per find: the first
        // awake soul standing here carries it into memory.
        val witness = witnesses(state).firstOrNull()
        if (witness != null) {
            plantSecretTopic(witness.name)
            val idx = ((secret.id.hashCode() * 31 +
                witness.name.hashCode()) and 0x7fffffff) % REACTIONS.size
            text += "\n" + REACTIONS[idx].replace("{name}", witness.name)
        }
        val n = foundCount(state)
        if (n == FEAT_THRESHOLD) {
            state.events.add("🔎 $FEAT_NAME — feat complete! " +
                "Half the town's secrets are yours.")
            while (state.events.size > 40) state.events.removeAt(0)
        }
        text += "\n(🔎 Secret $n of ${SECRETS.size} — it's written up in " +
            "your Journal, 🔎 TOWN SECRETS.)"
        return text
    }

    /** The Look chain's hook: the first unfound secret at
     *  [locId] whose gate stands open fires. With the gate
     *  shut the spot is ordinary scenery — null, and the
     *  normal Look text stands alone. */
    fun lookText(state: GameState, locId: String): String? {
        for (secret in SECRETS) {
            if (secret.trigger != TRIGGER_LOOK) continue
            if (secret.locId != locId) continue
            if (isFound(state, secret)) continue
            if (!gateOpen(state, secret)) continue
            return discover(state, secret)
        }
        return null
    }

    /** The prop taps' hook: [kind] is a tap the world already
     *  offers ("bench", "gnome"). Fires the matching unfound
     *  secret if its gate stands open; otherwise null and the
     *  prop behaves exactly as it always has. */
    fun propTap(state: GameState, kind: String): String? {
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        for (secret in SECRETS) {
            if (secret.trigger != kind) continue
            if (isFound(state, secret)) continue
            when (kind) {
                TRIGGER_BENCH -> if (secret.locId != me.location) continue
                TRIGGER_GNOME -> {
                    // the gnome must actually be standing here
                    if (TownLife.gnomeLocFor(state) != me.location) continue
                    if (state.playerInside) continue
                }
                else -> continue
            }
            if (!gateOpen(state, secret)) continue
            return discover(state, secret)
        }
        return null
    }

    // ---------------- surfaces ----------------

    /** The morning report's line, the day after a discovery:
     *  the find is named (it's yours, it's on the page); the
     *  unfound are never so much as counted out loud here. */
    fun morningNote(state: GameState): String? {
        if ((state.life["secretBeatDay"] ?: -999) != state.absDay - 1) {
            return null
        }
        val title = state.lifeText["secretBeat"] ?: return null
        return "🔎 Yesterday, one of the town's secrets stopped being " +
            "one: “$title”. It's written up in your Journal — " +
            "the town keeps the rest of its counsel."
    }

    /** The 🔎 TOWN SECRETS Journal page, assembled pure like
     *  the other boards: what's found is written up (what,
     *  where, when); what isn't is a riddle and a count —
     *  never a location, a trigger or a gate. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val found = foundSecrets(state)
        out += "🔎 TOWN SECRETS — ${found.size} of ${SECRETS.size} found"
        out += "The town keeps a dozen small things it never mentions. " +
            "They don't glow. They don't sparkle. They're just there, " +
            "where they've always been, for whoever looks carefully — " +
            "in the right place, at the right time."
        if (featDone(state)) {
            out += "🏆 $FEAT_NAME — half the town's secrets are yours. " +
                "The town has started keeping quieter ones."
        } else {
            out += "Find $FEAT_THRESHOLD and the town marks you $FEAT_NAME. " +
                "Find all ${SECRETS.size} and something goes up on the wall at home."
        }
        if (found.isNotEmpty()) {
            out += "📖 FOUND:"
            for (secret in found) {
                val (day, locId) = discoveryOf(state, secret)!!
                out += "· ${secret.title} — ${secret.blurb} " +
                    "(Found at ${LOCATIONS[locId]?.name ?: locId}, " +
                    "day ${day + 1}.)"
            }
        }
        val unfound = SECRETS.filter { !isFound(state, it) }
        if (unfound.isNotEmpty()) {
            out += "🕯 STILL OUT THERE:"
            for (secret in unfound) out += "· ${secret.riddle}"
        } else {
            out += "🕯 Nothing left out there. All twelve are yours — " +
                "and your house keeps the map of them now."
        }
        return out
    }
}

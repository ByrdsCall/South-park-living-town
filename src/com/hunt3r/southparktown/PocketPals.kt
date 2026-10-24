package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Pocket Pals — v0.5.0c (Queue #5 item 3): collectible cards.
//
// A 24-card set of ORIGINAL parody creatures — every name,
// flavor line and design invented for this game; no existing
// franchise's creatures, names or card layouts. All card art
// is code-drawn from the per-card spec below (PocketPalArt):
// the same data draws the same card, forever, like a photo.
// Specs use solid shapes, stripes and stars only — never
// clustered dots/holes (the trypophobia guard).
//
// The economy is a faucet and a sink:
//  - FAUCET: the capsule machine inside the Mall (center
//    wall, between the clothing rack and the transit kiosk)
//    sells a pull for 2 Capsule Coins — a weighted rarity
//    roll over the 22 machine cards, dupes allowed and
//    counted. Errand thank-yous sometimes land as a card
//    instead of an item, and exactly 2 cards are Look-find
//    exclusives at named spots (never in the machine).
//  - SINK: trading. Every cast member keeps a deterministic
//    weekly stash (seeded by name + week) and a valuation
//    quirk — their obsession school is worth double TO THEM.
//    One-for-one trades from the Talk sheet, friendship moving
//    by the value gap, capped: they're kids, not brokers.
//    Nobody ever trades away your last copy of a card.
//
// Ownership lives in ONE additive save key — the `pocketPals`
// map on GameState (id -> count), decode-filtered to this
// registry and capped per card (Prefs). The pulls counter
// rides the existing `life` map ("palPulls").
// ============================================================

data class PalCard(
    val id: String,
    val name: String,
    val flavor: String,
    val school: String,          // one of PocketPals.SCHOOLS
    val rarity: String,          // COMMON | UNCOMMON | RARE
    val source: String,          // SOURCE_MACHINE | SOURCE_LOOK
    val lookLoc: String? = null, // SOURCE_LOOK: the location id
    val lookSpot: String? = null, // SOURCE_LOOK: the named spot, player-facing
    // ---- the drawing spec (PocketPalArt draws from this) ----
    val body: String,            // ROUND | TALL | WIDE | BLOB | SQUARE | STAR
    val bodyColor: String,
    val bellyColor: String,
    val accentColor: String,
    val ears: String,            // NONE | POINTS | FLOPPY | ANTENNA | HORNS | FIN | TUFT
    val eyes: String,            // DOTS | HAPPY | SLEEPY | STAR | WINK | ROUND
    val extra: String            // NONE | STRIPES | STAR_MARK | BELT | CAP | TAIL | WINGS
)

data class PalFeat(val id: String, val name: String, val desc: String)

/** A trade on the table: you give [give], they give [get],
 *  friendship moves by [delta] (already value-gap math,
 *  already capped) if it goes through. */
data class PalTradeOffer(
    val npc: String,
    val give: PalCard,
    val get: PalCard,
    val delta: Int
)

object PocketPals {

    const val COMMON = "COMMON"
    const val UNCOMMON = "UNCOMMON"
    const val RARE = "RARE"

    const val SOURCE_MACHINE = "machine"
    const val SOURCE_LOOK = "look"

    /** The four schools — original groupings, named plainly. */
    val SCHOOLS: List<String> = listOf("Spark", "Frost", "Leaf", "Grit")

    fun schoolIcon(school: String): String = when (school) {
        "Spark" -> "⚡"; "Frost" -> "❄️"; "Leaf" -> "🍃"; "Grit" -> "🪨"
        else -> "🃏"
    }

    // ---------------- the dials (all suite-pinned) ----------------

    /** One pull at the capsule machine, in Capsule Coins. */
    const val MACHINE_COST = 2

    /** The machine's home: a fixture inside the Mall. */
    const val MACHINE_LOC = "Mall"

    /** Rarity weights for one machine roll, per card. */
    const val WEIGHT_COMMON = 100
    const val WEIGHT_UNCOMMON = 36
    const val WEIGHT_RARE = 9

    /** Errand thank-yous: of the completions whose thank-you
     *  lands at all (Errands' 1-in-4), 1 in ERRAND_CARD_EVERY
     *  is a card instead of an item — 1 in 20 completions. */
    const val ERRAND_CARD_EVERY = 5

    /** Per-card count cap — the save decode holds it too, so a
     *  hand-inflated doc can't mint a thousand dupes. */
    const val COUNT_CAP = 99

    /** Friendship swing cap on a trade. Kids, not brokers. */
    const val TRADE_SWING_CAP = 6

    /** Cards in a kid's weekly stash. */
    const val STASH_SIZE = 3

    /** The `life` key counting lifetime machine pulls. */
    const val PULLS_KEY = "palPulls"

    fun weightOf(card: PalCard): Int = when (card.rarity) {
        COMMON -> WEIGHT_COMMON
        UNCOMMON -> WEIGHT_UNCOMMON
        else -> WEIGHT_RARE
    }

    /** Trade value of a card, before anyone's quirks. */
    fun valueOf(card: PalCard): Int = when (card.rarity) {
        COMMON -> 1
        UNCOMMON -> 3
        else -> 9
    }

    // ---------------- the set: 24 originals ----------------

    val CARDS: List<PalCard> = listOf(

        // ---- ⚡ SPARK (3 common / 2 uncommon / the chase rare) ----
        PalCard("zippit", "Zippit",
            "Stores static in its cheeks. Spends it all on doorbells.",
            "Spark", COMMON, SOURCE_MACHINE,
            body = "ROUND", bodyColor = "#f2c230", bellyColor = "#fbe89a",
            accentColor = "#e07b24", ears = "POINTS", eyes = "HAPPY", extra = "STRIPES"),
        PalCard("pluggin", "Pluggin",
            "Born with a tail that only fits wall sockets and destiny.",
            "Spark", COMMON, SOURCE_MACHINE,
            body = "TALL", bodyColor = "#f6d354", bellyColor = "#fdf0b3",
            accentColor = "#8a5a2a", ears = "ANTENNA", eyes = "DOTS", extra = "TAIL"),
        PalCard("buzzmoth", "Buzzmoth",
            "Orbits the porch light like the light owes it money.",
            "Spark", COMMON, SOURCE_MACHINE,
            body = "WIDE", bodyColor = "#eec93e", bellyColor = "#f9ecc0",
            accentColor = "#b3541e", ears = "NONE", eyes = "ROUND", extra = "WINGS"),
        PalCard("cracklet", "Cracklet",
            "Every step sounds like a tiny fireplace agreeing with itself.",
            "Spark", UNCOMMON, SOURCE_MACHINE,
            body = "BLOB", bodyColor = "#f0a832", bellyColor = "#ffe1a8",
            accentColor = "#c33d1e", ears = "POINTS", eyes = "SLEEPY", extra = "STAR_MARK"),
        PalCard("teslon", "Teslon",
            "Hums at exactly the frequency of a refrigerator at 3 AM.",
            "Spark", UNCOMMON, SOURCE_MACHINE,
            body = "SQUARE", bodyColor = "#d9b23a", bellyColor = "#f4e3ae",
            accentColor = "#5a4632", ears = "ANTENNA", eyes = "WINK", extra = "BELT"),
        PalCard("aurorath", "Aurorath, the Storm That Stayed",
            "The first thunderstorm never left town. It just got smaller, " +
                "older, and much, much harder to catch.",
            "Spark", RARE, SOURCE_MACHINE,
            body = "STAR", bodyColor = "#ffd94a", bellyColor = "#fff3b0",
            accentColor = "#7a3fd4", ears = "HORNS", eyes = "STAR", extra = "STAR_MARK"),

        // ---- ❄️ FROST (4 common / 1 uncommon / 1 rare, Look) ----
        PalCard("sniffler", "Sniffler",
            "Catches a cold every winter on purpose. It's tradition.",
            "Frost", COMMON, SOURCE_MACHINE,
            body = "ROUND", bodyColor = "#bfe3f2", bellyColor = "#eefaff",
            accentColor = "#3f7fbf", ears = "FLOPPY", eyes = "SLEEPY", extra = "NONE"),
        PalCard("cubecicle", "Cubecicle",
            "Perfectly square. Melts into a perfect puddle. No notes.",
            "Frost", COMMON, SOURCE_MACHINE,
            body = "SQUARE", bodyColor = "#a8d8ea", bellyColor = "#e8fbff",
            accentColor = "#2a6fd4", ears = "NONE", eyes = "DOTS", extra = "CAP"),
        PalCard("driftmouse", "Driftmouse",
            "Naps inside snowdrifts and bills them as real estate.",
            "Frost", COMMON, SOURCE_MACHINE,
            body = "WIDE", bodyColor = "#d6ecf7", bellyColor = "#ffffff",
            accentColor = "#7a6a9a", ears = "TUFT", eyes = "HAPPY", extra = "TAIL"),
        PalCard("cryzzle", "Cryzzle",
            "Its tears freeze mid-cheek into tiny chandeliers.",
            "Frost", COMMON, SOURCE_MACHINE,
            body = "BLOB", bodyColor = "#9fcbe8", bellyColor = "#e6f6ff",
            accentColor = "#4a7fb5", ears = "FIN", eyes = "SLEEPY", extra = "STAR_MARK"),
        PalCard("frostoon", "Frostoon",
            "Rolls downhill collecting scarf lint and confidence.",
            "Frost", UNCOMMON, SOURCE_MACHINE,
            body = "ROUND", bodyColor = "#8fc3e8", bellyColor = "#dff3ff",
            accentColor = "#274b73", ears = "TUFT", eyes = "WINK", extra = "STRIPES"),
        PalCard("pondmother", "The Pondmother",
            "Something old lives under the ice and keeps every secret " +
                "the pond has ever heard.",
            "Frost", RARE, SOURCE_LOOK,
            lookLoc = "StarksPond", lookSpot = "under the dock at Stark's Pond",
            body = "TALL", bodyColor = "#6fa8c9", bellyColor = "#cfeeff",
            accentColor = "#123a5c", ears = "FIN", eyes = "STAR", extra = "BELT"),

        // ---- 🍃 LEAF (3 common / 2 uncommon / 1 rare; one Look) ----
        PalCard("sproutato", "Sproutato",
            "Half potato, half ambition. Fully planted.",
            "Leaf", COMMON, SOURCE_MACHINE,
            body = "ROUND", bodyColor = "#c9a86a", bellyColor = "#ecd9ae",
            accentColor = "#4a9e4f", ears = "TUFT", eyes = "HAPPY", extra = "NONE"),
        PalCard("mossling", "Mossling",
            "So slow that birds have started nesting on it with intent.",
            "Leaf", COMMON, SOURCE_MACHINE,
            body = "BLOB", bodyColor = "#6fae5a", bellyColor = "#cfe8b8",
            accentColor = "#2f6b2f", ears = "NONE", eyes = "SLEEPY", extra = "CAP"),
        PalCard("bramblet", "Bramblet",
            "Hugs first. Asks about the thorns later.",
            "Leaf", COMMON, SOURCE_MACHINE,
            body = "TALL", bodyColor = "#5f9e4f", bellyColor = "#d8ecc0",
            accentColor = "#7a3f6b", ears = "POINTS", eyes = "DOTS", extra = "STRIPES"),
        PalCard("canopede", "Canopede",
            "A hundred tiny feet, all of them late for the same place.",
            "Leaf", UNCOMMON, SOURCE_MACHINE,
            body = "WIDE", bodyColor = "#4f9e57", bellyColor = "#d0eec2",
            accentColor = "#274b26", ears = "ANTENNA", eyes = "ROUND", extra = "BELT"),
        PalCard("pollenator", "The Pollenator",
            "It does not pollinate anything. It supervises. Loudly.",
            "Leaf", UNCOMMON, SOURCE_LOOK,
            lookLoc = "Elementary",
            lookSpot = "the playground garden at South Park Elementary",
            body = "WIDE", bodyColor = "#7fbf4f", bellyColor = "#f2ffc8",
            accentColor = "#e0a51e", ears = "FLOPPY", eyes = "WINK", extra = "WINGS"),
        PalCard("tanglewood", "Old Man Tanglewood",
            "The forest's grandfather. Every trail through him leads " +
                "somewhere he approves of.",
            "Leaf", RARE, SOURCE_MACHINE,
            body = "TALL", bodyColor = "#4a7d3a", bellyColor = "#bcd9a0",
            accentColor = "#5a4632", ears = "HORNS", eyes = "SLEEPY", extra = "STRIPES"),

        // ---- 🪨 GRIT (4 common / 2 uncommon) ----
        PalCard("peblit", "Peblit",
            "A rock that decided walking was worth trying exactly once a day.",
            "Grit", COMMON, SOURCE_MACHINE,
            body = "ROUND", bodyColor = "#a89a8c", bellyColor = "#ddd2c4",
            accentColor = "#5a4632", ears = "NONE", eyes = "DOTS", extra = "NONE"),
        PalCard("gritbun", "Gritbun",
            "Sweeps itself under rugs. Professionally.",
            "Grit", COMMON, SOURCE_MACHINE,
            body = "WIDE", bodyColor = "#b8a88f", bellyColor = "#e8dcc4",
            accentColor = "#8a5a2a", ears = "FLOPPY", eyes = "HAPPY", extra = "TAIL"),
        PalCard("clodhop", "Clodhop",
            "Jumps in mud with both feet and no regrets on record.",
            "Grit", COMMON, SOURCE_MACHINE,
            body = "BLOB", bodyColor = "#8a6a4f", bellyColor = "#cbb59a",
            accentColor = "#3f5a36", ears = "POINTS", eyes = "WINK", extra = "STAR_MARK"),
        PalCard("mudlet", "Mudlet",
            "Technically a puddle with opinions.",
            "Grit", COMMON, SOURCE_MACHINE,
            body = "BLOB", bodyColor = "#7a5c40", bellyColor = "#bfa586",
            accentColor = "#2a6f8f", ears = "NONE", eyes = "ROUND", extra = "BELT"),
        PalCard("bouldash", "Bouldash",
            "Rolls downhill at alarming speed toward extremely specific destinations.",
            "Grit", UNCOMMON, SOURCE_MACHINE,
            body = "ROUND", bodyColor = "#8f8578", bellyColor = "#cfc4b2",
            accentColor = "#c33d1e", ears = "TUFT", eyes = "HAPPY", extra = "STRIPES"),
        PalCard("quarrion", "Quarrion",
            "Carries a pebble collection it describes only as 'the good rocks'.",
            "Grit", UNCOMMON, SOURCE_MACHINE,
            body = "SQUARE", bodyColor = "#9a8f7d", bellyColor = "#e0d6c2",
            accentColor = "#b8860b", ears = "HORNS", eyes = "SLEEPY", extra = "CAP")
    )

    val CARD_IDS: Set<String> = CARDS.map { it.id }.toSet()
    val CARD_BY_ID: Map<String, PalCard> = CARDS.associateBy { it.id }

    /** The set's chase rare — the one the machine dreams about. */
    val CHASE: PalCard = CARD_BY_ID.getValue("aurorath")

    /** Machine stock: everything that isn't a Look exclusive. */
    val MACHINE_POOL: List<PalCard> = CARDS.filter { it.source == SOURCE_MACHINE }

    /** The two Look-find exclusives. They NEVER come from the machine. */
    val LOOK_CARDS: List<PalCard> = CARDS.filter { it.source == SOURCE_LOOK }

    val RARES: List<PalCard> = CARDS.filter { it.rarity == RARE }

    fun cardById(id: String): PalCard? = CARD_BY_ID[id]

    /** Franchise-term denylist for the originality guard — the
     *  suite scans every name and flavor line against it. */
    val DENYLIST: List<String> = listOf(
        "pokemon", "pokémon", "pikachu", "pokeball", "pokéball",
        "charmander", "bulbasaur", "squirtle", "charizard", "eevee",
        "jigglypuff", "meowth", "psyduck", "snorlax", "gengar", "mewtwo",
        "digimon", "agumon", "yugioh", "yu-gi-oh", "bakugan", "beyblade",
        "palworld", "temtem", "nexomon", "yo-kai", "yokai",
        "monster hunter", "dragon quest", "skylanders"
    )

    // ---------------- ownership ----------------

    fun count(state: GameState, card: PalCard): Int =
        state.pocketPals[card.id] ?: 0

    fun owned(state: GameState, card: PalCard): Boolean = count(state, card) > 0

    fun ownedCount(state: GameState): Int = CARDS.count { owned(state, it) }

    fun totalCards(state: GameState): Int = CARDS.sumOf { count(state, it) }

    fun pulls(state: GameState): Int = state.life[PULLS_KEY] ?: 0

    // ---------------- feats (derived, like the errand titles) ----------------

    val PAL_FEATS: List<PalFeat> = listOf(
        PalFeat("pal_rare_trio", "🃏 TRIPLE CROWN",
            "Own all three rare Pocket Pals — the Storm, the Pondmother, the Old Man."),
        PalFeat("pal_full_set", "🃏 FULL SET!",
            "Own all 24 Pocket Pals. Every last one. Frame the binder.")
    )

    fun featById(id: String): PalFeat? = PAL_FEATS.firstOrNull { it.id == id }

    fun featEarned(state: GameState, f: PalFeat): Boolean = when (f.id) {
        "pal_full_set" -> ownedCount(state) == CARDS.size
        "pal_rare_trio" -> RARES.all { owned(state, it) }
        else -> false
    }

    /** Add one copy of [card] (capped). Returns the feat ids
     *  THIS copy crossed — feats are derived from ownership, so
     *  a crossing is announced exactly once, here, by whoever
     *  handed the card over (machine, errand, Look, trade). */
    fun grantCard(state: GameState, card: PalCard, why: String): List<String> {
        val before = PAL_FEATS.filter { featEarned(state, it) }.map { it.id }
        val cur = count(state, card)
        if (cur < COUNT_CAP) state.pocketPals[card.id] = cur + 1
        val after = PAL_FEATS.filter { featEarned(state, it) }.map { it.id }
        val crossed = after - before
        for (id in crossed) {
            val f = featById(id) ?: continue
            state.events.add("${f.name} — Pocket Pals feat complete!")
            while (state.events.size > 40) state.events.removeAt(0)
            townLogLine(state, "🃏 ${f.name} — $why")
            Phone.noteFeat(state, f.name)
        }
        return crossed
    }

    // ---------------- the faucet: the capsule machine ----------------

    /** One weighted roll over the machine pool. Look exclusives
     *  are not in the pool — they can never be rolled. */
    fun roll(rand: Random): PalCard {
        val total = MACHINE_POOL.sumOf { weightOf(it) }
        var r = rand.nextInt(total)
        for (c in MACHINE_POOL) {
            r -= weightOf(c)
            if (r < 0) return c
        }
        return MACHINE_POOL.last()
    }

    data class PullResult(val status: String, val card: PalCard?)
    // status: "ok" (card dealt) | "poor" (not enough coins)

    /** Pull the machine: 2 capsule coins, one weighted card,
     *  dupes allowed and counted, the pulls counter ticks. */
    fun pull(state: GameState, rand: Random): PullResult {
        val coins = Minigames.capsuleCoins(state)
        if (coins < MACHINE_COST) return PullResult("poor", null)
        val left = coins - MACHINE_COST
        if (left > 0) state.inventory[Minigames.COIN_ITEM] = left
        else state.inventory.remove(Minigames.COIN_ITEM)
        state.life[PULLS_KEY] = pulls(state) + 1
        val card = roll(rand)
        grantCard(state, card, "pulled from the capsule machine at the Mall")
        // v0.5.0e: an uncommon-or-better pull is text-worthy —
        // some kid WILL be jealous by morning (commons aren't)
        if (card.rarity != COMMON) Phone.notePull(state, card.name)
        return PullResult("ok", card)
    }

    /** The errand thank-you card, chosen by the errand's hash —
     *  always machine stock (a thank-you is not a treasure hunt). */
    fun errandCard(hash: Int): PalCard =
        MACHINE_POOL[(hash and 0x7fffffff) % MACHINE_POOL.size]

    // ---------------- the faucet: Look exclusives ----------------

    /** A careful Look at a named spot finds its card — once,
     *  the first time, if you don't own it yet. Returns the
     *  Look text to append, or null. */
    fun lookText(state: GameState, locId: String): String? {
        val card = LOOK_CARDS.firstOrNull { it.lookLoc == locId } ?: return null
        if (owned(state, card)) return null
        grantCard(state, card, "found ${card.lookSpot}")
        return "🃏 POCKET PAL FOUND — wedged ${card.lookSpot}, in a " +
            "little plastic capsule, is a card: ${card.name} " +
            "(${schoolIcon(card.school)} ${card.school}, rare beyond sense). " +
            "Finders keepers. It's in your album now."
    }

    /** The album's hint for a missing card: where it lives. */
    fun hintFor(card: PalCard): String = when (card.source) {
        SOURCE_LOOK -> "🔍 Look — try ${card.lookSpot}."
        else -> "🕹️ The capsule machine at the Mall (2 🪙 a pull) — " +
            "or 🃏 trade with the town's kids."
    }

    // ---------------- the sink: trading ----------------

    /** The week a stash belongs to (absDay-based, like the
     *  town's other weekly rhythms). */
    fun weekOf(state: GameState): Int = state.absDay / 7

    /** What [npc] obsesses over — deterministic per cast member. */
    fun obsessionOf(npc: String): String =
        SCHOOLS[(npc.hashCode() and 0x7fffffff) % SCHOOLS.size]

    /** A card's value TO [npc]: their obsession school is
     *  worth double to them. */
    fun valueTo(npc: String, card: PalCard): Int =
        valueOf(card) * if (card.school == obsessionOf(npc)) 2 else 1

    /** [npc]'s stash for [week]: STASH_SIZE distinct cards,
     *  seeded by (name, week) — the same week always shows the
     *  same stash, on any save. Kids find things everywhere,
     *  so the stash draws from the whole set, Look cards too. */
    fun stashFor(npc: String, week: Int): List<PalCard> {
        val rand = Random((npc.hashCode() and 0x7fffffff) * 1000003L + week * 7919L)
        val out = mutableListOf<PalCard>()
        var guard = 0
        while (out.size < STASH_SIZE && guard++ < 60) {
            var r = rand.nextInt(CARDS.sumOf { weightOf(it) })
            var pick = CARDS.last()
            for (c in CARDS) {
                r -= weightOf(c)
                if (r < 0) { pick = c; break }
            }
            if (pick !in out) out += pick
        }
        return out
    }

    /** Has [npc] already traded with you this week? */
    fun tradedThisWeek(state: GameState, npc: String): Boolean =
        state.life["palTrade:$npc"] == weekOf(state)

    /** The trade on the table with [npc], or null when no trade
     *  is possible: cast only, one trade per kid per week, they
     *  must hold something you lack, and you must hold a DUPE
     *  (never your last copy) that they covet — a card of their
     *  obsession school, or one their stash doesn't already
     *  have. They ask for the card they value most; they offer
     *  the stash card they value least among the ones you lack.
     *  Kids, not brokers. */
    fun tradeOffer(state: GameState, npc: String): PalTradeOffer? {
        if (npc !in AGENT_PROFILES) return null
        if (tradedThisWeek(state, npc)) return null
        val stash = stashFor(npc, weekOf(state))
        val theirs = stash.filter { !owned(state, it) }
            .minWithOrNull(compareBy<PalCard> { valueTo(npc, it) }.thenBy { it.id })
            ?: return null
        val yours = CARDS.filter { c ->
            count(state, c) >= 2 &&
                (c.school == obsessionOf(npc) || c !in stash)
        }.maxWithOrNull(compareBy<PalCard> { valueTo(npc, it) }.thenBy { it.id })
            ?: return null
        val delta = (valueTo(npc, yours) - valueTo(npc, theirs))
            .coerceIn(-TRADE_SWING_CAP, TRADE_SWING_CAP)
        return PalTradeOffer(npc, yours, theirs, delta)
    }

    /** Settle the trade on the table. Recomputes the offer (the
     *  table can't change between the sheet and the tap), moves
     *  the cards one-for-one, moves friendship by the capped
     *  value gap, and marks the kid traded for the week. Your
     *  last copy of anything is never on the table — offers are
     *  dupes only, and this re-checks before taking. */
    fun executeTrade(state: GameState, npc: String): PalTradeOffer? {
        val offer = tradeOffer(state, npc) ?: return null
        if (count(state, offer.give) < 2) return null // last-copy protection, again
        val left = count(state, offer.give) - 1
        if (left > 0) state.pocketPals[offer.give.id] = left
        else state.pocketPals.remove(offer.give.id)
        grantCard(state, offer.get, "traded with $npc")
        if (offer.delta != 0) Engine().relAdjust(state, npc, offer.delta)
        state.life["palTrade:$npc"] = weekOf(state)
        state.events.add("🃏 Traded ${offer.give.name} to $npc for " +
            "${offer.get.name} (${if (offer.delta >= 0) "+" else ""}${offer.delta} 💛)")
        while (state.events.size > 40) state.events.removeAt(0)
        return offer
    }

    /** Declining is free and funny: [npc]'s shrug, deterministic. */
    fun declineLine(npc: String, state: GameState): String {
        val lines = listOf(
            "$npc shrugs: \"No trade? Fine. My binder, my rules.\"",
            "$npc closes the binder slowly. \"Your loss. It was mostly my loss too, but still.\"",
            "\"Okay,\" says $npc, \"but I'm telling everyone you hesitated.\"",
            "$npc nods like a tiny antique dealer. \"Come back when you're serious about cardboard.\"",
            "\"Deal's off the table,\" says $npc, putting the table (a shoebox) away."
        )
        val idx = (npc.hashCode() and 0x7fffffff) % lines.size
        return lines[(idx + weekOf(state)) % lines.size]
    }

    // ---------------- the album surface ----------------

    /** 🃏 POCKET PALS lines for the Collection: the header, the
     *  feats, and the machine's terms — the grid itself (owned
     *  art + dupe counts, silhouettes + hints) is drawn by the
     *  Collection from CARDS, in this order. */
    fun collectionLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🃏 POCKET PALS — ${ownedCount(state)}/${CARDS.size} owned · " +
            "${totalCards(state)} cards in the binder · ${pulls(state)} machine pulls. " +
            "Pulls are $MACHINE_COST 🪙 at the capsule machine inside the Mall; " +
            "kids trade from the Talk sheet, one-for-one, and two cards " +
            "hide at named spots for a careful Look."
        for (f in PAL_FEATS) {
            out += "${if (featEarned(state, f)) "✅" else "⬜"} ${f.name} — ${f.desc}"
        }
        if (ownedCount(state) == 0)
            out += "No cards yet. The machine at the Mall is humming. It knows your name. " +
                "It knows everybody's name. It's a machine."
        return out
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Errands — v0.4.0b (Queue #4 item 2): small work for
// townsfolk, giving ordinary days a spine.
//
// Three sources, one lifecycle:
//   BOARD — the request board inside City Hall (McDaniels'
//     domain). The day's postings are a PURE function of the
//     day seed, like collectible spawns and NPC goals: same
//     day, same board, on every screen, stored nowhere.
//   ASK   — a direct favor: when you Talk to a cast member
//     whose current NPC goal makes an ask sensible, a seeded
//     probe (ASK_CHANCE) lets their dialogue open with a
//     personal errand instead. Once offered — taken or not —
//     they don't ask again that day (state.errandAsked).
//   SHOP  — the trade keeps: Tweek (Tweek Bros), Jimbo
//     (Jimbo's Guns) and, for the two counters with no cast
//     keeper of their own, Chef (Freeman's — food trade) and
//     Gerald (the Bank — money trade). Talk to them AT their
//     shop and a trade-flavored ask may open the conversation.
//
// Six types, each judged by a verb the game already has —
// no parallel interaction systems were built:
//   FETCH   bring an item          -> the Give rail (handInFetch)
//   DELIVER a parcel to a person   -> Talk to them (deliverTo)
//   FIND    a lost item at a place -> Look there (recoverFind)
//   FIX     a broken spot          -> the lamp/bench taps (onFixTap)
//   PHOTO   a person / a place     -> the selfie (onPhoto, from
//                                     Photos.applyPhoto itself)
//   PLAY    a minigame result      -> Minigames' own settle
//                                     functions (onPlayCatch /
//                                     onPlayShowdown / onPlayTaco)
//
// FETCH targets come only from the OBTAINABLE universe:
// every id in ITEMS has exactly one named source (sourceOf),
// and the fetch pool is drawn from it — the generator can
// never ask for a thing the game cannot produce.
//
// Accepted errands live in GameState.errands (additive save
// keys, capped: MAX_ACTIVE on the plate, done ones pruned at
// rollover; history is a lifetime count + RECENT_CAP notes).
// Completion pays friendship with the asker (scaled by
// type), Capsule Coins for most types, occasionally a
// thank-you item, and sometimes plants a thread ("errand")
// in the asker's memory topics, where Threads' generic
// follow-up bank picks it up naturally. The asker's thank
// line waits in lifeText and plays once, as a talk pre-line.
//
// v0.6.0b — FAVOR CHAINS (Errands #2, Queue #6 item 2):
// some generated asks are the head of a chain of 2–3 links.
// The generators stamp the plan (chainLen) as a pure function
// of the ask, so generation itself is unchanged; accept()
// grows the tail — each link a buildErrand draw walking the
// favor from the last link's beneficiary onward to a
// relation, the final link of a 3-chain landing back on the
// origin asker. A chain occupies ONE plate slot: completing
// a link reveals the next onto the plate (re-validated
// against the world as it stands, substituted if stale),
// and dropping the live link drops the whole chain. Links
// pay the ordinary rates; the final link adds a closing
// bonus from the origin asker, whose thank and morning text
// land only at the end. Mid-chain, the town notices: the
// involved carry a "favor chain" memory topic, the morning
// report keeps a chain beat, and a friend of the origin
// asker gossips about it once a day on the owed-lines rail.
//
// v0.8.0c — EMERGENCIES (Errands #3, Queue #8 item 3): a
// fourth source joins the three above — the time-critical
// favor that finds YOU. Emergencies.kt owns the day-seeded
// scenarios, the Walk-priced deadline, the offer on the
// Talk gate's EMERG_OFFER rail, and the rescue/expire
// settlements; this module owns what it always owned —
// the record, the plate, and the completion funnel the
// ordinary verbs judge through. An emergency rides the
// plate in its own slot (the cap is not consulted), pays
// its verb's rates plus a rescue bonus inside complete(),
// and leaves by the clock (Emergencies.checkExpiry, on
// the Engine's tick) when the moment passes.
// ============================================================

data class Errand(
    val id: String,
    val source: String,          // BOARD | ASK | SHOP
    val asker: String,           // cast name
    val type: String,            // FETCH | DELIVER | FIND | FIX | PHOTO | PLAY
    val text: String,            // the ask itself, in the asker's voice
    val hint: String,            // where-to-go one-liner (lamp hint re-derives live)
    val itemId: String? = null,      // FETCH: bring it · FIND: it's lost
    val targetName: String? = null,  // DELIVER: recipient · PHOTO: person
    val targetLoc: String? = null,   // FIND: where · PHOTO: place · FIX bench: place
    val fixKind: String? = null,     // FIX: "lamp" | "bench"
    val playKind: String? = null,    // PLAY: "fish:<speciesId>" | "showdown" | "taco:<min>"
    val dayKey: String = "",         // the day it was generated
    var acceptedDay: Int = -1,       // absDay accepted (-1 = a board listing)
    var done: Boolean = false,
    // ---- v0.6.0b: favor-chain fields. Absent in older saves
    // and on single errands (chainLen 0) — an ordinary ask.
    // The generators STAMP chainLen on the asks they build:
    // 2..3 marks a chain head waiting to be taken; once taken,
    // chainLink counts the live link's place and the tail
    // rides in chainRest, flat, on the live link only. ----
    val chainId: String? = null,       // the chain this link belongs to
    val chainLink: Int = 0,            // 0 = untaken head / single; else 1-based
    val chainLen: Int = 0,             // 0 = single; else total links (2..3)
    val chainFrom: String? = null,     // the origin asker the chain belongs to
    val chainPrior: List<String> = emptyList(), // objectives of links landed
    val chainRest: List<Errand> = emptyList(),   // links not yet revealed
    // ---- v0.8.0c: emergency fields. Only ever set on
    // EMERG errands; absent in older saves and on every
    // ordinary errand (deadline -1, no scenario). ----
    val emergDeadline: Int = -1,       // minute-of-day the clock runs out
    val emergScenario: String = ""     // Emergencies scenario id
) {
    fun toMap(): Map<String, Any?> {
        val m = mutableMapOf<String, Any?>(
            "id" to id, "source" to source, "asker" to asker, "type" to type,
            "text" to text, "hint" to hint, "itemId" to itemId,
            "targetName" to targetName, "targetLoc" to targetLoc,
            "fixKind" to fixKind, "playKind" to playKind, "dayKey" to dayKey,
            "acceptedDay" to acceptedDay, "done" to done
        )
        // singles serialize exactly as they always have
        if (chainLen > 0) {
            m["chainId"] = chainId
            m["chainLink"] = chainLink
            m["chainLen"] = chainLen
            m["chainFrom"] = chainFrom
            m["chainPrior"] = chainPrior
            m["chainRest"] = chainRest.map { it.toMap() }
        }
        // v0.8.0c: emergencies carry their clock — and only
        // emergencies do, so every older record (and every
        // ordinary errand today) serializes exactly as before
        if (source == Errands.EMERG) {
            m["emergDeadline"] = emergDeadline
            m["emergScenario"] = emergScenario
        }
        return m
    }

    companion object {
        fun fromMap(m: Map<String, Any?>): Errand? = fromMapDepth(m, 0)

        /** Decode with the chain discipline held: lengths in
         *  range, the tail capped at the links actually left,
         *  nested links carrying no tail of their own (the
         *  tail is flat by construction), and a depth ceiling
         *  so a hand-inflated doc can't nest forever. */
        private fun fromMapDepth(m: Map<String, Any?>, depth: Int): Errand? {
            val id = m["id"]?.toString()?.takeIf { it.isNotEmpty() } ?: return null
            val asker = m["asker"]?.toString()?.takeIf { it.isNotEmpty() } ?: return null
            val type = m["type"]?.toString() ?: return null
            if (type !in Errands.TYPES) return null
            val cLen = ((m["chainLen"] as? Number)?.toInt() ?: 0)
                .takeIf { it in 2..Errands.CHAIN_MAX_LINKS } ?: 0
            val cLink = if (cLen > 0)
                ((m["chainLink"] as? Number)?.toInt() ?: 0).coerceIn(0, cLen)
            else 0
            val cFrom = (m["chainFrom"] as? String)
                ?.takeIf { it in KID_ORDER }
            // a live link (or an untaken head) is only a chain
            // with its paperwork in order
            val chained = cLen > 0 && depth == 0 &&
                (cLink == 0 || cFrom != null)
            return Errand(
                id = id,
                source = m["source"]?.toString() ?: "BOARD",
                asker = asker, type = type,
                text = m["text"]?.toString() ?: "",
                hint = m["hint"]?.toString() ?: "",
                itemId = m["itemId"]?.toString(),
                targetName = m["targetName"]?.toString(),
                targetLoc = m["targetLoc"]?.toString(),
                fixKind = m["fixKind"]?.toString(),
                playKind = m["playKind"]?.toString(),
                dayKey = m["dayKey"]?.toString() ?: "",
                acceptedDay = (m["acceptedDay"] as? Number)?.toInt() ?: -1,
                done = m["done"] as? Boolean ?: false,
                chainId = if (chained)
                    (m["chainId"] as? String)?.take(64) else null,
                chainLink = if (chained) cLink else 0,
                chainLen = if (chained) cLen else 0,
                chainFrom = if (chained) cFrom else null,
                chainPrior = if (chained)
                    (m["chainPrior"] as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.take(Errands.CHAIN_MAX_LINKS)
                        ?.map { it.take(160) } ?: emptyList()
                else emptyList(),
                chainRest = if (chained && cLink > 0)
                    (m["chainRest"] as? List<*>)
                        ?.mapNotNull { raw ->
                            (raw as? Map<String, Any?>)
                                ?.let { fromMapDepth(it, depth + 1) }
                                ?.copy(chainRest = emptyList())
                        }
                        ?.take(cLen - cLink) ?: emptyList()
                else emptyList(),
                emergDeadline = ((m["emergDeadline"] as? Number)?.toInt()
                    ?: -1).coerceIn(-1, 1439),
                emergScenario = (m["emergScenario"] as? String)
                    ?.take(40) ?: ""
            )
        }
    }
}

/** One line of errand history (the Journal's "lately" list). */
data class ErrandDoneNote(val id: String, val dayKey: String, val line: String) {
    fun toMap(): Map<String, Any?> = mapOf("id" to id, "day" to dayKey, "line" to line)

    companion object {
        fun fromMap(m: Map<String, Any?>): ErrandDoneNote? {
            val line = m["line"]?.toString()?.takeIf { it.isNotEmpty() } ?: return null
            return ErrandDoneNote(m["id"]?.toString() ?: "",
                m["day"]?.toString() ?: "", line)
        }
    }
}

/** What a completion paid out — the UI toasts from this. */
data class ErrandReward(
    val friendship: Int,
    val coins: Int,
    val thankItem: ItemDef?,
    val newFeats: List<String>,     // errand feat ids crossed by THIS completion
    val thanksLine: String,         // the asker's words, filled and ready
    // v0.5.0c: 1 in PocketPals.ERRAND_CARD_EVERY thank-yous is a
    // Pocket Pal card instead of an item (never both).
    val thankCard: PalCard? = null,
    // v0.6.0b: chain outcomes — the next link, revealed onto
    // the plate by THIS completion, with the voiced reveal
    // line; or, when the final link lands, the closing bonus
    // the origin asker paid on top of the link's own rates.
    val chainNext: Errand? = null,
    val chainRevealLine: String? = null,
    val chainFinished: Boolean = false,
    val chainBonusCoins: Int = 0,
    val chainBonusFriendship: Int = 0
)

/** A judged completion: the errand + what it paid. */
data class ErrandCompletion(val errand: Errand, val reward: ErrandReward)

data class ErrandFeat(
    val id: String, val name: String, val desc: String,
    val at: Int, val title: String
)

object Errands {

    // ---------------- the dials (all suite-pinned) ----------------

    /** Errands on your plate at once, board or otherwise. */
    const val MAX_ACTIVE = 6

    /** Board size: BOARD_MIN + a seeded 0/1 — 2–3 fresh
     *  postings a day, deterministic from the day seed. */
    const val BOARD_MIN = 2

    /** A sensible goal makes a direct ask this often. */
    const val ASK_CHANCE = 0.35

    /** Shopkeepers are bolder — it's their counter, their ask. */
    const val SHOP_ASK_CHANCE = 0.5

    /** Decode-side ceiling for the stored list (active cap +
     *  a day's completions awaiting the rollover prune). */
    const val STATE_CAP = MAX_ACTIVE + 12

    // ---------------- v0.6.0b: favor chains ----------------

    /** The share of generated asks that turn out to be the
     *  head of a favor chain, by source. The street and the
     *  board tangle about a third of the time; shopkeepers
     *  keep it rare — a counter favor stays a counter favor. */
    const val CHAIN_CHANCE_BOARD = 0.30
    const val CHAIN_CHANCE_ASK = 0.30
    const val CHAIN_CHANCE_SHOP = 0.15

    /** A chain runs 2..CHAIN_MAX_LINKS links, never more. */
    const val CHAIN_MAX_LINKS = 3

    /** The closing bonus the origin asker pays when the
     *  final link lands — per link in the chain, on top of
     *  every link's own rates. A 3-link chain should feel
     *  like a small arc, because it was one. */
    const val CHAIN_BONUS_COINS_PER_LINK = 2
    const val CHAIN_BONUS_FRIENDSHIP_PER_LINK = 2

    /** The memory topic a chain plants in its people's
     *  conversation memory as its links land — Threads'
     *  follow-up banks carry a bank for it. */
    const val CHAIN_TOPIC = "favor chain"

    /** History kept for display: the last few completions. */
    const val STATE_RECENT_CAP = 8

    const val BOARD = "BOARD"
    const val ASK = "ASK"
    const val SHOP = "SHOP"

    /** v0.8.0c: the fourth source — an emergency, which is
     *  never posted or asked at leisure; it finds you
     *  (Emergencies owns its generation, clock and rates). */
    const val EMERG = "EMERG"

    /** v0.9.0c: the fifth source — the county fair's own
     *  authored errands, which post only during fair week
     *  (Fair owns their scenarios, days and offering). */
    const val FAIR = "FAIR"

    val TYPES = listOf("FETCH", "DELIVER", "FIND", "FIX", "PHOTO", "PLAY")

    /** Friendship paid on completion, by type — finding the
     *  lost thing pays best; playing a game for someone is
     *  its own reward, so it pays least (and no coins: the
     *  game itself already paid you). */
    val FRIENDSHIP: Map<String, Int> = mapOf(
        "FETCH" to 6, "DELIVER" to 6, "FIND" to 7,
        "FIX" to 5, "PHOTO" to 5, "PLAY" to 4
    )
    val COINS: Map<String, Int> = mapOf(
        "FETCH" to 2, "DELIVER" to 2, "FIND" to 3,
        "FIX" to 2, "PHOTO" to 1, "PLAY" to 0
    )

    /** The helpful titles: run this many, wear this name. */
    val ERRAND_FEATS: List<ErrandFeat> = listOf(
        ErrandFeat("errand_10", "📋 Neighborly",
            "Run 10 errands for the town.", 10, "Neighborly"),
        ErrandFeat("errand_50", "📋 Town Fixture",
            "Run 50 errands. People pin notes to the board hoping you'll pass.",
            50, "Town Fixture"),
        ErrandFeat("errand_150", "📋 Indispensable",
            "Run 150 errands. Without you the town simply stops.",
            150, "Indispensable")
    )

    fun featById(id: String): ErrandFeat? = ERRAND_FEATS.firstOrNull { it.id == id }

    fun featEarned(state: GameState, f: ErrandFeat): Boolean =
        state.errandsDoneCount >= f.at

    /** The title the town currently calls you, or null before
     *  the first feat — the new kid earns names here. */
    fun titleFor(count: Int): String? =
        ERRAND_FEATS.lastOrNull { count >= it.at }?.title

    /** v0.6.0b: the chain feats — counted in finished
     *  chains, not links (links already count toward the
     *  errand feats above, one completion each). The count
     *  itself lives in the `life` map, the capped-key way —
     *  no new save key for it. */
    val CHAIN_FEATS: List<ErrandFeat> = listOf(
        ErrandFeat("chain_1", "🧵 Thread Puller",
            "Finish a favor chain — one good deed, fully tangled.",
            1, "Thread Puller"),
        ErrandFeat("chain_10", "🧵 Town Weaver",
            "Finish 10 favor chains. The town's favors are a " +
                "fabric, and you hold the needle.",
            10, "Town Weaver")
    )

    fun chainsDone(state: GameState): Int =
        state.life["chainsDone"] ?: 0

    fun chainFeatEarned(state: GameState, f: ErrandFeat): Boolean =
        chainsDone(state) >= f.at

    /** Whose name belongs on this errand: a chain answers to
     *  its origin asker, not whoever holds the current link. */
    fun displayAsker(e: Errand): String = e.chainFrom ?: e.asker

    /** The person a landed link points onward to: a
     *  delivery's recipient, a photo's subject, else the
     *  asker themselves — the favor walks from them. */
    fun beneficiaryOf(e: Errand): String = when {
        e.type == "DELIVER" -> e.targetName ?: e.asker
        e.type == "PHOTO" && e.targetName != null -> e.targetName
        else -> e.asker
    }

    // ---------------- the obtainable universe ----------------

    private val FISH_ITEM_IDS: Set<String> = FISH_TABLE.map { it.itemId }.toSet()
    private val CONTAINER_TIERS = setOf("junk", "gift", "useful", "lore")

    /** The ONE way each item enters the world:
     *  pond (the catch table) · stand (the lemonade stand) ·
     *  games (capsule payouts) · spawn (daily collectible
     *  spawns) · container (trash / litter / turf loot rolls).
     *  Null only for ids that aren't items at all — the suite
     *  pins that every ITEMS id resolves, so "obtainable" is
     *  a proof, not a hope. */
    fun sourceOf(itemId: String): String? = when {
        itemId in FISH_ITEM_IDS -> "pond"
        itemId == "lemonade" -> "stand"
        itemId == Minigames.COIN_ITEM -> "games"
        itemId == Walk.PASS_ITEM -> "kiosk"
        collectibleById(itemId) != null -> "spawn"
        itemById(itemId)?.tier in CONTAINER_TIERS -> "container"
        else -> null
    }

    /** What a FETCH may ask for: the common, producible end
     *  of the universe. No capsule coins (that's the reward,
     *  not a grocery), no lost mittens (winter litter only —
     *  a fetch must be runnable in any season). */
    val FETCH_POOL: List<String> = ITEMS
        .filter { it.tier in setOf("gift", "useful", "junk") }
        .map { it.id }
        .filter { it != Minigames.COIN_ITEM && it != "lost_mitten" }

    /** Named lost property a FIND can misplace. */
    val FIND_POOL: List<String> = listOf(
        "velcro_wallet", "golden_ticket_stub", "cassette_single",
        "comic_book", "shiny_rock", "umbrella", "hand_warmer",
        "old_sock", "flower", "lost_mitten"
    )

    /** Public places things get lost (the asker's own house
     *  joins the pool per-errand). */
    val FIND_LOCS: List<String> = listOf(
        "KidsPark", "StarksPond", "Elementary", "MainStreet",
        "BusStop", "Mall", "Bijou", "Church", "SoDoSoPa"
    )

    /** Scenic spots a PHOTO errand can send you to. */
    val PHOTO_LOCS: List<String> = listOf(
        "StarksPond", "KidsPark", "Mall", "Bijou",
        "TegridyFarms", "Church", "MainStreet", "SoDoSoPa"
    )

    /** PLAY-fish species: the honest end of the catch table. */
    val PLAY_FISH: List<String> = listOf(
        "pond_smelt", "mud_minnow", "speckled_trout"
    )

    /** PLAY-taco targets: respectable, not superhuman. */
    val TACO_TARGETS: List<Int> = listOf(120, 180, 240)

    /** A fixable spot: the tap kind that fixes it, where (null
     *  loc = the flickering lamp, which moves nightly), and
     *  what the Journal calls it. */
    data class FixSpot(val kind: String, val locId: String?, val label: String)

    val FIX_SPOTS: List<FixSpot> = listOf(
        FixSpot("lamp", null, "the flickering streetlamp"),
        FixSpot("bench", "KidsPark", "the wobbly bench at Kid's Park"),
        FixSpot("bench", "BusStop", "the bus-stop bench")
    )

    /** Tonight's flickering lamp lives here — the same formula
     *  GameView draws by (absDay into the location ring), so
     *  the hint and the streetlight always agree. */
    fun flickerLoc(absDay: Int): String {
        val keys = LOCATIONS.keys.toList()
        return keys[((absDay % keys.size) + keys.size) % keys.size]
    }

    /** Thank-you items: the gift shelf, minus the coin. */
    private val THANK_POOL: List<String> = ITEMS
        .filter { it.tier == "gift" && it.id != Minigames.COIN_ITEM }
        .map { it.id }

    // ---------------- text banks ----------------

    private val FETCH_LINES = listOf(
        "Any chance you could grab me a {item}? Mine vanished. Long story, and I am legally barred from follow-up questions.",
        "I need a {item} and I cannot leave this spot. Town reasons. You're a pal, right?",
        "One {item}. That is the entire errand. I've made it sound grander than it is, but I do genuinely need it."
    )

    /** Named voices that bend the generic banks — (asker, type). */
    private val SPECIAL_LINES: Map<Pair<String, String>, List<String>> = mapOf(
        ("Carol" to "FETCH") to listOf(
            "Hon, could you run into town for me? Kenny's dad keeps 'forgetting' the good coupons. Any {item} off the shelf proves the trip happened."),
        ("Randy" to "FETCH") to listOf(
            "I need a sample. A {item}. From nature. For the farm. This is entirely, definitely about farming."),
        ("Cartman" to "FETCH") to listOf(
            "I require a {item}. This is not a request, it is a destiny. …Fine, it is a request. A SERIOUS one."),
        ("McDaniels" to "FETCH") to listOf(
            "City business: the office needs a {item}, and procurement is… you. There is a form. There is always a form."),
        ("Jimbo" to "FETCH") to listOf(
            "Fetch me a {item} and I'll tell you the story behind it. The story's the real payment. There's coins too, don't worry."),
        ("Tweek" to "FETCH") to listOf(
            "A {item}!! Please!! We are OUT and the morning is COMING and my dad is doing the breathing thing!!")
    )

    private val DELIVER_LINES = listOf(
        "Take this {thing} to {target} for me? No peeking. Especially not at the {thing}.",
        "{target} is expecting this {thing}. Tell them I said 'you know what you did.' They'll know.",
        "Could you run this {thing} over to {target}? My legs have unionized and I respect the picket line."
    )
    private val DELIVER_THINGS = listOf(
        "parcel", "note", "folder", "thermos", "pie tin", "envelope"
    )

    private val FIND_LINES = listOf(
        "I lost my {item}. Last place I remember having it: {loc}. It is probably judging me from a corner.",
        "My {item} is missing, and I'm not saying where I had it last, but try {loc}. Please.",
        "Somewhere around {loc} there's a {item} with my name on it. Not literally. It knows what it did."
    )

    private val FIX_LINES: Map<String, List<String>> = mapOf(
        "lamp" to listOf(
            "That streetlight has been flickering for days and it's driving the whole block quietly insane. One good bonk, after dark, wherever it's misbehaving tonight."),
        "KidsPark" to listOf(
            "The bench at Kid's Park wobbles like it's keeping secrets. Tighten it before someone tips into the sandbox of history."),
        "BusStop" to listOf(
            "The bus-stop bench has one short leg and zero shame. Shim it, would you? The riders are developing a lean.")
    )

    private val PHOTO_PERSON_LINES = listOf(
        "Get me a photo of {target}. It's for {reason}. Totally normal reasons.",
        "I need a picture of {target} — {reason} demands it. You have a phone face. I trust you."
    )
    private val PHOTO_PLACE_LINES = listOf(
        "Take a photo at {loc} for me? I never get out there, and {reason} requires evidence."
    )
    private val PHOTO_REASONS = listOf(
        "the fridge door", "the scrapbook",
        "a completely normal folder I keep", "the yearbook page"
    )

    private val PLAY_FISH_LINES = listOf(
        "Catch me a {species}, fresh from the pond. I can tell store-bought. I don't know how, but I can.")
    private val PLAY_SHOWDOWN_LINES = listOf(
        "Win a Snowball Showdown — anybody, doesn't matter who. I have a small bet riding on you. A respectful one.")
    private val PLAY_TACO_LINES = listOf(
        "Post {score}+ on the Taco Catch cabinet at Freeman's. That machine has been talking smack. It doesn't talk. That's how bad it is.")

    /** Shop-flavored asks, keyed by keeper: (type, itemOverride, lines). */
    private data class ShopAsk(val type: String, val item: String?, val lines: List<String>)

    private val SHOP_ASKS: Map<String, List<ShopAsk>> = mapOf(
        "Tweek" to listOf(
            ShopAsk("FETCH", "coffee_beans", listOf(
                "We're OUT of the good beans and the morning rush is COMING. Please. PLEASE. The beans, from anywhere, just beans.")),
            ShopAsk("DELIVER", null, listOf(
                "A standing order! {target} gets a coffee delivery and today the delivery is YOU. Godspeed. Do not spill."))),
        "Jimbo" to listOf(
            ShopAsk("FETCH", "fish_trout", listOf(
                "Need a fresh trout for the shop wall. The old one's heard all my stories twice and it's starting to mouth along.")),
            ShopAsk("PLAY", null, listOf(
                "Catch anything with fins at the pond and I'll call it research. Catch a trout and I'll call it a career."))),
        "Chef" to listOf(
            ShopAsk("FETCH", null, listOf(
                "Menu research, baby: bring me a {item} and I'll tell you if it belongs next to a taco. It does. Everything does.")),
            ShopAsk("PLAY", null, listOf(
                "That cabinet by the counter? Post {score}+ and the Freeman's brunch crowd hears about it. All six of them."))),
        "Gerald" to listOf(
            ShopAsk("DELIVER", null, listOf(
                "These papers need {target}'s signature before close of business. I would go myself, but I have billable hours. So many hours.")),
            ShopAsk("FETCH", null, listOf(
                "A client left a {item} near the vault. Discretion, please. And speed. Mostly discretion.")))
    )

    /** shop location -> its keeper (the Engine.KEEPERS shops,
     *  plus Freeman's and the Bank by trade: Chef cooks,
     *  Gerald banks — neither counter has a cast keeper). */
    val SHOP_ASKERS: Map<String, String> = mapOf(
        "TweekBros" to "Tweek", "JimbosGuns" to "Jimbo",
        "FreemansTacos" to "Chef", "Bank" to "Gerald"
    )

    /** Which goal kinds make which asks sensible. GRUDGE is
     *  absent on purpose: nobody asks a favor mid-grudge. */
    private val ASK_TYPES_BY_GOAL: Map<String, List<String>> = mapOf(
        "ERRAND" to listOf("FETCH", "DELIVER"),
        "CRAVING" to listOf("FETCH"),
        "PROJECT" to listOf("FETCH", "FIX"),
        "WORK" to listOf("DELIVER", "FETCH"),
        "FACTION" to listOf("DELIVER", "FIND"),
        "SCHEME" to listOf("PHOTO", "DELIVER"),
        "PHOTO" to listOf("PHOTO"),
        "MEETUP" to listOf("DELIVER")
    )

    /** The asker's gratitude, by type — played once as a talk
     *  pre-line after completion (stored in lifeText). */
    private val THANKS: Map<String, List<String>> = mapOf(
        "FETCH" to listOf(
            "The {item} you fetched me? Exactly the one. You're a lifesaver, genuinely.",
            "Still thinking about that {item} run. Above and beyond, new kid.",
            "You got the {item}. Most people say they'll help. You helped."),
        "DELIVER" to listOf(
            "{target} got the delivery — and sent word back that you're 'shockingly reliable.' High praise, here.",
            "The {target} handoff? Flawless. I've put in a good word with the town.",
            "Still owe you for the {target} run. The town runs on people like you."),
        "FIND" to listOf(
            "My {item} — home again because of you. I check for it twice a day now. Fondly.",
            "You found my {item} at {loc}. I had written it off to the void. The void lost.",
            "Every time I see my {item} I think: the new kid. Every time."),
        "FIX" to listOf(
            "The fix held! Whatever you did to it, it's in the town's bones now. Thanks.",
            "People keep mentioning the repair like it's town news. It IS town news. Slow week.",
            "Still solid, your fix. Craftsmanship. In THIS town. Imagine."),
        "PHOTO" to listOf(
            "That photo you took? Framed. Well — fridge-magneted. Same honor, here.",
            "I showed everyone the photo. Everyone. There were opinions, all good.",
            "Your photo work holds up. The light, the moment — cinema."),
        "PLAY" to listOf(
            "Heard about your performance. The bet paid out. You're a legend of a very small pond.",
            "Your big game moment? Town legend already. The details grow daily.",
            "Still telling people about it. You did the thing. THE thing.")
    )

    /** What an asker hears when their errand gets dropped —
     *  shown once, no other penalty, ever. */
    private val DISAPPOINTED = listOf(
        "Oh. Okay. I'll just… handle it myself, I guess. Eventually. Probably.",
        "Huh. Alright. No hard feelings. (Some hard feelings, but quiet ones.)",
        "That's fine. That's FINE. I'll put a note on the board. For someone else.",
        "Sure, sure. Everyone's busy. The errand will just… be an errand. Unrun."
    )

    /** v0.6.0b: the reveal — spoken in the room when a link
     *  lands and the chain pulls its next thread. {next} is
     *  the next link's asker. */
    private val CHAIN_REVEALS = listOf(
        "Oh — one thing. Before this is really done: {next} still needs a hand. It's connected. It's ALWAYS connected in this town.",
        "Wait — there's a thread hanging off this favor. {next} is holding the other end of it.",
        "Funny story: this favor had a sequel already written. {next} has the next piece of it.",
        "And because this is South Park, there's a part two: {next} needs the next piece before any of it counts."
    )

    /** v0.6.0b: the origin asker's closing thank when the
     *  final link lands — stored like every errand thanks,
     *  played once on the owed-lines rail. */
    private val CHAIN_THANKS = listOf(
        "The whole chain — every link of it — landed because of you. This town runs on exactly that.",
        "You pulled one thread and the whole sweater came along. I owe you for all of it.",
        "Start to finish, you carried that chain. People are talking. Good things. ALL good things."
    )

    /** v0.6.0b: the town watching a chain — a friend of the
     *  origin asker's piece, said once per chain per day on
     *  the owed-lines rail, never in quiet hours. {asker} is
     *  the chain's origin. */
    private val CHAIN_GOSSIP = listOf(
        "So. {asker}'s little favor situation? It's a whole CHAIN now, and you're the middle of it. Iconic.",
        "Everyone's watching {asker}'s favor chain like it's a sport. You're carrying the baton.",
        "That chain of favors running back to {asker} — it ends somewhere legendary. Or at the pond. Possibly both.",
        "{asker} owes you a parade when this chain lands. A small one. Budget reasons."
    )

    // ---------------- shared look-ups ----------------

    fun active(state: GameState): List<Errand> = state.errands.filter { !it.done }

    fun activeCount(state: GameState): Int = state.errands.count { !it.done }

    fun lastNote(state: GameState): ErrandDoneNote? = state.errandsRecent.lastOrNull()

    private fun locName(id: String?): String =
        id?.let { LOCATIONS[it]?.name ?: it } ?: ""

    private fun itemName(id: String?): String =
        id?.let { itemById(it)?.name ?: it } ?: ""

    private fun fill(template: String, e: Errand): String = template
        .replace("{item}", itemName(e.itemId))
        .replace("{target}", e.targetName ?: "")
        .replace("{loc}", locName(e.targetLoc))
        .replace("{species}",
            e.playKind?.removePrefix("fish:")?.let { fishById(it)?.name } ?: "")

    /** The one-line objective (Journal / board / beat).
     *  v0.8.0c: an emergency's objective wears its ⏰ in
     *  every surface that prints one (plate, Journal,
     *  notes) — the clock is the point of the errand. */
    fun objective(e: Errand): String =
        (if (e.source == EMERG) "⏰ "
         else if (e.source == FAIR) "🎡 " else "") + objectiveBody(e)

    private fun objectiveBody(e: Errand): String = when (e.type) {
        "FETCH" -> "Bring ${itemName(e.itemId)} to ${e.asker}"
        "DELIVER" -> "Deliver ${e.asker}'s parcel to ${e.targetName}"
        "FIND" -> "Find ${e.asker}'s lost ${itemName(e.itemId)}"
        "FIX" -> "Fix ${FIX_SPOTS.firstOrNull {
            it.kind == e.fixKind && it.locId == e.targetLoc }?.label ?: "the thing"}"
        "PHOTO" -> if (e.targetName != null)
            "Photo with ${e.targetName} (for ${e.asker})"
        else "Photo at ${locName(e.targetLoc)} (for ${e.asker})"
        "PLAY" -> when {
            e.playKind?.startsWith("fish:") == true ->
                "Catch a ${fishById(e.playKind.removePrefix("fish:"))?.name ?: "fish"} (for ${e.asker})"
            e.playKind == "showdown" -> "Win a Snowball Showdown (for ${e.asker})"
            e.playKind?.startsWith("taco:") == true ->
                "Score ${e.playKind.removePrefix("taco:")}+ at Taco Catch (for ${e.asker})"
            else -> "Play a game (for ${e.asker})"
        }
        else -> e.text
    }

    /** The where-to-go line. Computed live so the lamp's
     *  hint names TONIGHT's street, not acceptance night's. */
    fun hintFor(state: GameState, e: Errand): String = when (e.type) {
        "FETCH" -> when (sourceOf(e.itemId ?: "")) {
            "pond" -> "Catch it at Stark's Pond 🎣"
            "stand" -> "The lemonade stand at Kid's Park pours it"
            "spawn" -> "It turns up around town — watch for today's sparkle spots, then Give it to ${e.asker}"
            "games" -> "Win it at the town's games, then Give it to ${e.asker}"
            else -> "Search the town's trash, keep an eye out — then Give it to ${e.asker}"
        }
        "DELIVER" -> "Find ${e.targetName} and talk to them — the handoff does itself"
        "FIND" -> "Look carefully around ${locName(e.targetLoc)}"
        "FIX" -> if (e.fixKind == "lamp")
            "Tonight it flickers at ${locName(flickerLoc(state.absDay))} — bonk it after dark"
        else "Give the bench at ${locName(e.targetLoc)} a tap and a wrench"
        "PHOTO" -> if (e.targetName != null)
            "Use 📸 SELFIE with ${e.targetName} in the frame"
        else "Use 📸 SELFIE at ${locName(e.targetLoc)}"
        "PLAY" -> when {
            e.playKind?.startsWith("fish:") == true -> "Fish at Stark's Pond"
            e.playKind == "showdown" -> "The fort at Kid's Park — a Showdown needs snow"
            else -> "The cabinet inside Freeman's Tacos"
        }
        else -> e.hint
    }

    // ---------------- generation ----------------

    private fun pickOther(rnd: Random, exclude: String): String {
        val pool = KID_ORDER.filter { it != exclude }
        return pool[rnd.nextInt(pool.size)]
    }

    // ---------------- v0.6.0b: chain planning ----------------

    /** How long a chain this GENERATED ask heads, if any: a
     *  pure function of the ask itself (its id + its source's
     *  rate), drawing nothing from any random stream — the
     *  board derivation, the ask probes and the shop rolls
     *  all stay byte-identical to before. Only the three
     *  generators stamp; a hand-built errand keeps chainLen 0
     *  and never chains. Of the asks that do chain, roughly
     *  three in ten run the full three links. */
    private fun plannedChainLen(e: Errand): Int {
        val chance = when (e.source) {
            SHOP -> CHAIN_CHANCE_SHOP
            BOARD -> CHAIN_CHANCE_BOARD
            else -> CHAIN_CHANCE_ASK
        }
        val h = (e.id.hashCode() * 31 + e.source.hashCode()) and 0x7fffffff
        if (h % 1000 >= (chance * 1000).toInt()) return 0
        return if ((h / 1000) % 10 < 3) CHAIN_MAX_LINKS else 2
    }

    private fun stampChainPlan(e: Errand): Errand =
        e.copy(chainLen = plannedChainLen(e))

    /** Walk a favor onward from [from]: stay with them half
     *  the time, else step to one of their relations. From
     *  the origin asker it always steps away — a chain that
     *  never leaves its asker isn't a chain. Never lands on
     *  the origin (only the final link of a 3-chain returns
     *  there, by construction) and always lands on cast. */
    private fun stepOnward(rnd: Random, from: String, origin: String): String {
        val rels = relationsOf(from)
            .filter { it in KID_ORDER && it != from && it != origin }
        return when {
            from == origin ->
                if (rels.isNotEmpty()) rels[rnd.nextInt(rels.size)]
                else pickOther(rnd, origin)
            rnd.nextBoolean() || rels.isEmpty() -> from
            else -> rels[rnd.nextInt(rels.size)]
        }
    }

    /** Link types a chain walks in — the board's own mix. */
    private val CHAIN_LINK_TYPES = listOf(
        "FETCH", "FETCH", "DELIVER", "DELIVER",
        "FIND", "FIX", "PHOTO", "PLAY")

    /** Grow the tail behind a chain head: links 2..len, each
     *  built by the same generator as a single ask — so each
     *  link's judge inputs exist in the world by the same
     *  proof singles carry — walking the favor onward from
     *  beneficiary to relation, and, on a full three links,
     *  landing the last one back on the origin asker's
     *  doorstep. Deterministic in the head's id: drop a
     *  chain head, take the same posting again, and the same
     *  chain grows back. The tail rides FLAT on the live
     *  link's chainRest; each reveal consumes its head. */
    private fun buildChainTail(state: GameState, head: Errand,
                               len: Int): List<Errand> {
        val chainId = "c:" + head.id
        val origin = head.asker
        val rnd = Random(chainId.hashCode().toLong() * 1_000_003L + len)
        val tail = mutableListOf<Errand>()
        var carrier = beneficiaryOf(head)
        for (n in 2..len) {
            val asker = if (n == len && len == CHAIN_MAX_LINKS) origin
                else stepOnward(rnd, carrier, origin)
            val type = CHAIN_LINK_TYPES[rnd.nextInt(CHAIN_LINK_TYPES.size)]
            val link = buildErrand(rnd, "$chainId:$n", head.source,
                asker, type, state)
            tail += link.copy(chainId = chainId, chainLink = n,
                chainLen = len, chainFrom = origin)
            carrier = beneficiaryOf(link)
        }
        return tail
    }

    /** Take a stamped head live: position 1, origin named,
     *  tail grown. */
    private fun startChain(state: GameState, head: Errand): Errand {
        val tail = buildChainTail(state, head, head.chainLen)
        return head.copy(chainId = "c:" + head.id, chainLink = 1,
            chainFrom = head.asker, chainRest = tail)
    }

    /** The one-liner shown when a chain head is on offer —
     *  taking it starts something longer than one favor. */
    fun chainTeaser(e: Errand): String? =
        if (e.chainLink == 0 && e.chainLen >= 2)
            "🧵 Town word: this one has the smell of a chain — " +
                "${e.chainLen} favors deep before it lands."
        else null

    /** A revealed link, checked against the world as it is
     *  NOW: every judge input it names must still exist —
     *  the same obtainable universe the generator drew
     *  from. (The generator's pools are registry-static, so
     *  this is a proof re-run, not a hope — and the
     *  substitution rail below exists for the day a pool
     *  ever does move.) */
    fun linkStillValid(e: Errand): Boolean = when (e.type) {
        "FETCH" -> e.itemId in FETCH_POOL
        "DELIVER" -> e.targetName in KID_ORDER && e.targetName != e.asker
        "FIND" -> e.itemId in FIND_POOL && e.targetLoc in LOCATIONS
        "FIX" -> FIX_SPOTS.any { it.kind == e.fixKind && it.locId == e.targetLoc }
        "PHOTO" -> (e.targetName in KID_ORDER) != (e.targetLoc in LOCATIONS)
        "PLAY" -> e.playKind == "showdown" ||
            (e.playKind?.startsWith("fish:") == true &&
                fishById(e.playKind.removePrefix("fish:")) != null) ||
            (e.playKind?.startsWith("taco:") == true &&
                (e.playKind.removePrefix("taco:").toIntOrNull() ?: -1)
                    in TACO_TARGETS)
        else -> false
    }

    /** Reveal the next link of [live]'s chain onto the
     *  plate. The link was composed when the chain started;
     *  the world may have moved since, so it re-validates
     *  first — a link whose inputs went stale is substituted
     *  with an equivalent ask (same type, same person, fresh
     *  draw) rather than stranding the chain. The chain
     *  keeps exactly ONE plate slot throughout: the link
     *  that just landed is done, this one is the live one. */
    private fun revealNext(state: GameState, live: Errand): Errand {
        val raw = live.chainRest.first()
        var next = raw
        if (!linkStillValid(raw)) {
            val rnd = Random(raw.id.hashCode().toLong() * 97L +
                state.absDay)
            next = buildErrand(rnd, raw.id, raw.source, raw.asker,
                raw.type, state)
        }
        val placed = next.copy(
            acceptedDay = state.absDay,
            chainId = live.chainId,
            chainLink = live.chainLink + 1,
            chainLen = live.chainLen,
            chainFrom = live.chainFrom,
            chainPrior = live.chainPrior + objective(live),
            chainRest = live.chainRest.drop(1))
        state.errands.add(placed)
        return placed
    }

    /** The voiced reveal for a link landing (see CHAIN_REVEALS). */
    fun revealLine(live: Errand, next: Errand): String {
        val h = ((live.chainId ?: live.id).hashCode() and 0x7fffffff)
        return CHAIN_REVEALS[h % CHAIN_REVEALS.size]
            .replace("{next}", next.asker)
    }

    /** The origin asker's closing thank (see CHAIN_THANKS). */
    fun chainThanksLine(live: Errand): String {
        val h = ((live.chainId ?: live.id).hashCode() and 0x7fffffff)
        return CHAIN_THANKS[h % CHAIN_THANKS.size]
    }

    /** The toast tail for a chain outcome: a revealed link
     *  announces its next step; a finished chain announces
     *  itself. Empty for singles, so every existing judge
     *  surface reads exactly as before. */
    fun chainToastSuffix(r: ErrandReward): String = when {
        r.chainNext != null ->
            " 🧵 But the favor tangles on — next: ${objective(r.chainNext)}"
        r.chainFinished ->
            " 🧵 And that's the whole chain — closed!"
        else -> ""
    }

    /** The dialog form of the same news. */
    fun chainDialogText(r: ErrandReward): String = when {
        r.chainNext != null ->
            "\n\n🧵 ${r.chainRevealLine ?: "Oh — one thing."}" +
                "\n🎯 Next link: ${objective(r.chainNext)}"
        r.chainFinished ->
            "\n\n🧵 That closes the whole chain — " +
                "+${r.chainBonusCoins} 🪙 chain bonus, and the " +
                "whole town saw it land."
        else -> ""
    }

    /** Drop a topic into an NPC's conversation memory —
     *  Threads' follow-up banks pick it up from there. */
    private fun plantTopic(npc: String, topic: String) {
        val mem = NpcMemoryStore.getOrCreate(npc)
        mem.topics.remove(topic)
        mem.topics.add(topic)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** v0.6.0b: the town watching a chain — a friend of the
     *  origin asker, meeting you while the chain is already
     *  past its first link, says the town's piece. Once per
     *  chain per day (the first friend you meet says it),
     *  never in quiet hours, never from someone involved.
     *  Rides TalkGate's owed-lines rail. */
    fun chainGossipLine(state: GameState, npcName: String): Line? {
        if (Barks.inQuietHours(state.timeMin)) return null
        val chain = active(state).firstOrNull {
            it.chainLink > 1 && it.chainFrom != null } ?: return null
        val origin = chain.chainFrom ?: return null
        if (npcName == origin || npcName == chain.asker) return null
        if (npcName !in relationsOf(origin)) return null
        val key = "chainGossip:${chain.chainId}"
        if (state.life[key] == state.absDay) return null
        state.life[key] = state.absDay
        return Line(npcName, CHAIN_GOSSIP[
            (chain.chainId.hashCode() and 0x7fffffff) % CHAIN_GOSSIP.size]
            .replace("{asker}", origin))
    }

    /** Build one errand of [type] for [asker] off the caller's
     *  random stream — every field drawn in a fixed order, so
     *  the same stream always builds the same errand. */
    private fun buildErrand(rnd: Random, id: String, source: String,
                            asker: String, type: String, state: GameState,
                            itemOverride: String? = null,
                            playOverride: String? = null,
                            preferTarget: String? = null): Errand {
        var itemId: String? = null
        var targetName: String? = null
        var targetLoc: String? = null
        var fixKind: String? = null
        var playKind: String? = null
        val text: String
        when (type) {
            "FETCH" -> {
                itemId = itemOverride ?: FETCH_POOL[rnd.nextInt(FETCH_POOL.size)]
                val bank = SPECIAL_LINES[asker to "FETCH"] ?: FETCH_LINES
                text = bank[rnd.nextInt(bank.size)]
                    .replace("{item}", itemName(itemId))
            }
            "DELIVER" -> {
                val relations = relationsOf(asker).filter { it in KID_ORDER && it != asker }
                targetName = preferTarget?.takeIf { it in KID_ORDER && it != asker }
                    ?: if (relations.isNotEmpty() && rnd.nextBoolean())
                        relations[rnd.nextInt(relations.size)]
                    else pickOther(rnd, asker)
                val thing = DELIVER_THINGS[rnd.nextInt(DELIVER_THINGS.size)]
                text = DELIVER_LINES[rnd.nextInt(DELIVER_LINES.size)]
                    .replace("{target}", targetName).replace("{thing}", thing)
            }
            "FIND" -> {
                itemId = FIND_POOL[rnd.nextInt(FIND_POOL.size)]
                val locs = (FIND_LOCS + listOfNotNull(KID_HOUSE[asker]))
                    .filter { it in LOCATIONS }.distinct()
                targetLoc = locs[rnd.nextInt(locs.size)]
                text = FIND_LINES[rnd.nextInt(FIND_LINES.size)]
                    .replace("{item}", itemName(itemId))
                    .replace("{loc}", locName(targetLoc))
            }
            "FIX" -> {
                val spot = FIX_SPOTS[rnd.nextInt(FIX_SPOTS.size)]
                fixKind = spot.kind
                targetLoc = spot.locId
                val bank = FIX_LINES[spot.locId ?: "lamp"] ?: FIX_LINES.getValue("lamp")
                text = bank[rnd.nextInt(bank.size)]
            }
            "PHOTO" -> {
                if (rnd.nextBoolean()) {
                    targetName = pickOther(rnd, asker)
                    val reason = PHOTO_REASONS[rnd.nextInt(PHOTO_REASONS.size)]
                    text = PHOTO_PERSON_LINES[rnd.nextInt(PHOTO_PERSON_LINES.size)]
                        .replace("{target}", targetName).replace("{reason}", reason)
                } else {
                    targetLoc = PHOTO_LOCS[rnd.nextInt(PHOTO_LOCS.size)]
                    val reason = PHOTO_REASONS[rnd.nextInt(PHOTO_REASONS.size)]
                    text = PHOTO_PLACE_LINES[rnd.nextInt(PHOTO_PLACE_LINES.size)]
                        .replace("{loc}", locName(targetLoc)).replace("{reason}", reason)
                }
            }
            else -> { // PLAY
                when (playOverride ?: listOf("fish", "showdown", "taco")[rnd.nextInt(3)]) {
                    "fish" -> {
                        playKind = "fish:" + PLAY_FISH[rnd.nextInt(PLAY_FISH.size)]
                        text = PLAY_FISH_LINES[rnd.nextInt(PLAY_FISH_LINES.size)]
                            .replace("{species}",
                                fishById(playKind.removePrefix("fish:"))?.name ?: "fish")
                    }
                    "showdown" -> {
                        playKind = "showdown"
                        text = PLAY_SHOWDOWN_LINES[rnd.nextInt(PLAY_SHOWDOWN_LINES.size)]
                    }
                    else -> {
                        val n = TACO_TARGETS[rnd.nextInt(TACO_TARGETS.size)]
                        playKind = "taco:$n"
                        text = PLAY_TACO_LINES[rnd.nextInt(PLAY_TACO_LINES.size)]
                            .replace("{score}", n.toString())
                    }
                }
            }
        }
        val e = Errand(id, source, asker, type, text, "",
            itemId, targetName, targetLoc, fixKind, playKind, state.todayKey)
        return e.copy(hint = hintFor(state, e))
    }

    /** Today's board at City Hall — a PURE function of the day
     *  seed. Same day, same postings, everywhere, stored
     *  nowhere; they expire by virtue of tomorrow deriving a
     *  different board. */
    fun boardFor(state: GameState): List<Errand> {
        val key = state.todayKey.ifEmpty { "20261001" }
        val rnd = Random(dailySeed(key) * 31L + 404L)
        val count = BOARD_MIN + rnd.nextInt(2)
        val out = mutableListOf<Errand>()
        val usedAskers = mutableSetOf<String>()
        var slot = 0
        var guard = 0
        while (out.size < count && guard++ < 24) {
            val asker = KID_ORDER[rnd.nextInt(KID_ORDER.size)]
            if (asker in usedAskers) continue
            usedAskers += asker
            val type = listOf("FETCH", "FETCH", "DELIVER", "DELIVER",
                "FIND", "FIX", "PHOTO", "PLAY")[rnd.nextInt(8)]
            out += stampChainPlan(buildErrand(rnd,
                "b${state.absDay}:$slot", BOARD, asker, type, state))
            slot++
        }
        return out
    }

    /** The board as the player may still act on it: postings
     *  neither accepted (they live on your plate now) nor
     *  already run today. */
    fun boardOpen(state: GameState): List<Errand> {
        val taken = state.errands.map { it.id }.toSet()
        val doneToday = state.errandsRecent
            .filter { it.dayKey == state.todayKey }.map { it.id }.toSet()
        return boardFor(state).filter { it.id !in taken && it.id !in doneToday }
    }

    /** A direct ask, if one is on offer from [npc] right now:
     *  their current goal must make an ask sensible (NpcGoals),
     *  the seeded probe must pass (ASK_CHANCE), they must not
     *  already have an errand out with you, and they must not
     *  have asked today. Pure given the state. */
    fun askFor(state: GameState, npc: String): Errand? {
        if (npc !in KID_ORDER) return null
        if (state.errandAsked[npc] == state.absDay) return null
        if (active(state).any { it.asker == npc }) return null
        val goal = NpcGoals.record(state, npc)
        if (goal.done) return null
        val types = ASK_TYPES_BY_GOAL[goal.kind] ?: return null
        val rnd = Random(dailySeed(state.todayKey) * 131L +
            npc.hashCode().toLong() * 17L + 7L)
        if (rnd.nextDouble() >= ASK_CHANCE) return null
        val type = types[rnd.nextInt(types.size)]
        val prefer = if (type == "DELIVER") goal.personName else null
        return stampChainPlan(buildErrand(rnd, "a${state.absDay}:$npc",
            ASK, npc, type, state, preferTarget = prefer))
    }

    /** A shopkeeper's trade ask, if [npc] keeps [locId] and
     *  you're standing in their shop: bolder odds than a
     *  street ask (SHOP_ASK_CHANCE), same once-a-day rule. */
    fun shopAskFor(state: GameState, npc: String, locId: String): Errand? {
        if (SHOP_ASKERS[locId] != npc) return null
        if (state.errandAsked[npc] == state.absDay) return null
        if (active(state).any { it.asker == npc }) return null
        val asks = SHOP_ASKS[npc] ?: return null
        val rnd = Random(dailySeed(state.todayKey) * 271L +
            npc.hashCode().toLong() * 31L + 13L)
        if (rnd.nextDouble() >= SHOP_ASK_CHANCE) return null
        val pick = asks[rnd.nextInt(asks.size)]
        val e = buildErrand(rnd, "s${state.absDay}:$npc", SHOP, npc,
            pick.type, state, itemOverride = pick.item,
            playOverride = if (pick.type == "PLAY")
                (if (npc == "Chef") "taco" else "fish") else null)
        // shop lines voice the ask themselves (the generic
        // type banks were already rolled inside buildErrand;
        // the shop's own words win)
        val line = pick.lines[rnd.nextInt(pick.lines.size)]
            .replace("{item}", itemName(e.itemId))
            .replace("{target}", e.targetName ?: "")
            .replace("{score}",
                e.playKind?.removePrefix("taco:") ?: "")
            .replace("{species}",
                e.playKind?.removePrefix("fish:")?.let { fishById(it)?.name } ?: "fish")
        return stampChainPlan(e.copy(text = line))
    }

    /** The ask on offer from this person, in this place: a
     *  shopkeeper at their counter outranks a street ask. */
    fun pendingAsk(state: GameState, npc: String, locId: String): Errand? =
        shopAskFor(state, npc, locId) ?: askFor(state, npc)

    /** Record that [npc] was OFFERED an ask today (taken or
     *  not) — they won't raise another until tomorrow. */
    fun markAsked(state: GameState, npc: String) {
        state.errandAsked[npc] = state.absDay
    }

    // ---------------- lifecycle ----------------

    /** Put an errand on your plate. Refuses past MAX_ACTIVE —
     *  finish something or drop it first. A stamped chain
     *  head (chainLen 2..3, chainLink 0) goes live as a
     *  chain here: its tail grows behind it, and the whole
     *  chain will occupy this one plate slot, link by link. */
    fun accept(state: GameState, e: Errand): Boolean {
        if (activeCount(state) >= MAX_ACTIVE) return false
        if (state.errands.any { it.id == e.id }) return false
        var placed = e.copy(acceptedDay = state.absDay)
        if (placed.chainLink == 0 && placed.chainLen >= 2)
            placed = startChain(state, placed)
        state.errands.add(placed)
        return true
    }

    /** Drop an accepted errand. The only cost is the asker's
     *  disappointment line, which the caller shows once.
     *  Dropping a chain's live link drops the whole chain —
     *  the tail rides inside the record — and the sigh is
     *  the origin asker's (the UI names them via
     *  displayAsker). */
    fun abandon(state: GameState, errandId: String): String? {
        val idx = state.errands.indexOfFirst { it.id == errandId && !it.done }
        if (idx < 0) return null
        val e = state.errands.removeAt(idx)
        // v0.8.0c: putting down an emergency is its clock
        // running out by hand — the town copes, the coping
        // beat waits, and the sigh below is the whole cost,
        // exactly an abandoned errand's.
        if (e.source == EMERG) Emergencies.settleExpired(state, e, dropped = true)
        val h = ((e.chainId ?: e.id).hashCode() and 0x7fffffff) %
            DISAPPOINTED.size
        return DISAPPOINTED[h]
    }

    /** The shared completion: rewards, records, the thank
     *  that waits in the asker's next conversation, the
     *  morning beat, the feat check. Every judge funnels here. */
    fun complete(state: GameState, e: Errand): ErrandCompletion? {
        val live = state.errands.firstOrNull { it.id == e.id && !it.done }
            ?: return null
        live.done = true
        // v0.6.0b: is this landing a link of a favor chain?
        val isChainLink = live.chainLink > 0 && live.chainLen >= 2 &&
            live.chainFrom != null
        val chainOrigin = if (isChainLink) live.chainFrom else null
        val fr = FRIENDSHIP[live.type] ?: 4
        if (live.asker in state.friendship) Engine().relAdjust(state, live.asker, fr)
        else {
            // generated askers don't exist (asks are cast-only),
            // but a hand-built record degrades gracefully
            val mem = NpcMemoryStore.getOrCreate(live.asker)
            mem.points = (mem.points + fr).coerceIn(-50, 100)
        }
        val coins = COINS[live.type] ?: 0
        if (coins > 0) Minigames.awardCoins(state, coins)
        val h = (live.id.hashCode() and 0x7fffffff)
        var thankItem: ItemDef? = null
        var thankCard: PalCard? = null
        if (h % 4 == 0 && THANK_POOL.isNotEmpty()) {
            // v0.5.0c: a thank-you can be a Pocket Pal instead of
            // an item — 1 in ERRAND_CARD_EVERY thank-yous (1 in 20
            // completions overall), machine stock only.
            if ((h / 4) % PocketPals.ERRAND_CARD_EVERY == 0) {
                val c = PocketPals.errandCard(h)
                PocketPals.grantCard(state, c, "an errand thank-you from ${live.asker}")
                thankCard = c
            } else {
                val id = THANK_POOL[(h / 4) % THANK_POOL.size]
                state.inventory[id] = (state.inventory[id] ?: 0) + 1
                thankItem = itemById(id)
            }
        }
        if (h % 3 == 0) {
            // the thread callback: a topic the Threads banks
            // know how to follow up on ("that whole errand
            // thing") lands in the asker's conversation memory
            val mem = NpcMemoryStore.getOrCreate(live.asker)
            mem.topics.remove("errand")
            mem.topics.add("errand")
            while (mem.topics.size > 6) mem.topics.removeAt(0)
        }
        val thanksLine = fill(
            (THANKS[live.type] ?: THANKS.getValue("FETCH"))[h % 3], live)
        state.lifeText["errandThanks:${live.asker}"] = thanksLine
        state.agents[live.asker]?.memories?.add("📋 The new kid ran an errand for me.")
        // ---- records ----
        val before = state.errandsDoneCount
        state.errandsDoneCount++
        // v0.5.0e: the asker will text about it in the morning.
        // v0.6.0b: a chain link stays quiet — the chain's
        // origin asker texts when the FINAL link lands, and
        // the chain block below makes that note itself.
        if (!isChainLink) Phone.noteErrand(state, live.asker)
        state.errandsRecent.add(
            ErrandDoneNote(live.id, state.todayKey,
                if (isChainLink)
                    "✔ 🧵 ${objective(live)} " +
                        "(favor chain for $chainOrigin, " +
                        "link ${live.chainLink}/${live.chainLen})"
                else "✔ ${objective(live)}"))
        while (state.errandsRecent.size > STATE_RECENT_CAP)
            state.errandsRecent.removeAt(0)
        state.events.add("📋 Errand done — ${objective(live)} " +
            "(+$fr 💛 ${live.asker}" +
            (if (coins > 0) ", +$coins 🪙" else "") + ")")
        while (state.events.size > 40) state.events.removeAt(0)
        // ---- morning beat (House.morningReport reads it;
        // v0.8.0c: a rescue keeps its own 🚨 beat instead —
        // Emergencies.settleRescue writes it, so the deed
        // is never reported twice) ----
        if (live.source != EMERG) {
            if (state.life["errandBeatDay"] != state.absDay) {
                state.life["errandBeatDay"] = state.absDay
                state.life["errandBeatCount"] = 1
            } else {
                state.life["errandBeatCount"] =
                    (state.life["errandBeatCount"] ?: 0) + 1
            }
            state.lifeText["errandBeat"] = objective(live)
        }
        // ---- feats ----
        val crossed = ERRAND_FEATS.filter { before < it.at && state.errandsDoneCount >= it.at }
        for (f in crossed) {
            state.events.add("${f.name} — errand feat complete!")
            Phone.noteFeat(state, f.name)
            while (state.events.size > 40) state.events.removeAt(0)
            townLogLine(state, "📋 ${f.title} — ${state.errandsDoneCount} errands run for South Park")
        }
        // ---- v0.8.0c: a rescue landing. The verb's own
        // rates are already paid above; Emergencies pays
        // the rescue bonus on top and keeps its records —
        // the reward returned below reports the whole. ----
        var rescueFr = 0
        var rescueCoins = 0
        if (live.source == EMERG) {
            val b = Emergencies.settleRescue(state, live)
            rescueFr = b.first
            rescueCoins = b.second
        }
        // ---- v0.6.0b: favor chains — a link landing either
        // pulls the next thread or closes the chain. The
        // link's own rates are already paid above; what
        // follows is purely the chain's own bookkeeping. ----
        var chainNext: Errand? = null
        var chainReveal: String? = null
        var chainFinished = false
        var chainBonusCoins = 0
        var chainBonusFr = 0
        if (isChainLink && chainOrigin != null) {
            // the ripple: everyone involved carries the
            // chain in their conversation memory now
            plantTopic(live.asker, CHAIN_TOPIC)
            if (chainOrigin != live.asker) plantTopic(chainOrigin, CHAIN_TOPIC)
            state.life["chainBeatDay"] = state.absDay
            if (live.chainRest.isNotEmpty()) {
                chainNext = revealNext(state, live)
                plantTopic(chainNext.asker, CHAIN_TOPIC)
                chainReveal = revealLine(live, chainNext)
                state.lifeText["chainBeat"] =
                    "the favor chain for $chainOrigin pulled its " +
                        "next thread — link ${live.chainLink} of " +
                        "${live.chainLen} landed; next: " +
                        objective(chainNext)
                state.events.add("🧵 Favor chain for $chainOrigin — " +
                    "link ${live.chainLink} of ${live.chainLen} done. " +
                    "Next: ${objective(chainNext)}")
                while (state.events.size > 40) state.events.removeAt(0)
            } else {
                chainFinished = true
                chainBonusCoins = CHAIN_BONUS_COINS_PER_LINK * live.chainLen
                chainBonusFr = CHAIN_BONUS_FRIENDSHIP_PER_LINK *
                    live.chainLen
                if (chainBonusCoins > 0)
                    Minigames.awardCoins(state, chainBonusCoins)
                if (chainOrigin in state.friendship)
                    Engine().relAdjust(state, chainOrigin, chainBonusFr)
                else {
                    val mem = NpcMemoryStore.getOrCreate(chainOrigin)
                    mem.points = (mem.points + chainBonusFr)
                        .coerceIn(-50, 100)
                }
                val beforeChains = chainsDone(state)
                state.life["chainsDone"] = beforeChains + 1
                // the origin asker's closing thank waits on
                // the owed-lines rail like every thanks —
                // unless an older thank of theirs is still
                // waiting there; that one plays first someday
                // and the chain's rides the events instead.
                if (chainOrigin != live.asker &&
                    state.lifeText["errandThanks:$chainOrigin"] == null)
                    state.lifeText["errandThanks:$chainOrigin"] =
                        chainThanksLine(live)
                // the morning text: the asker, at the end,
                // exactly like a single errand's note
                Phone.noteErrand(state, chainOrigin)
                state.lifeText["chainBeat"] =
                    "the favor chain for $chainOrigin closed — " +
                        "all ${live.chainLen} links landed"
                state.events.add("🧵 Favor chain for $chainOrigin " +
                    "COMPLETE — ${live.chainLen} links, and the " +
                    "town watched every one. " +
                    "(+$chainBonusFr 💛 $chainOrigin, " +
                    "+$chainBonusCoins 🪙 chain bonus)")
                while (state.events.size > 40) state.events.removeAt(0)
                for (f in CHAIN_FEATS.filter {
                    beforeChains < it.at && chainsDone(state) >= it.at }) {
                    state.events.add("${f.name} — chain feat complete!")
                    Phone.noteFeat(state, f.name)
                    while (state.events.size > 40) state.events.removeAt(0)
                    townLogLine(state, "🧵 ${f.title} — " +
                        "${chainsDone(state)} favor chains finished " +
                        "for South Park")
                }
            }
        }
        return ErrandCompletion(live,
            ErrandReward(fr + rescueFr, coins + rescueCoins, thankItem,
                crossed.map { it.id }, thanksLine,
                thankCard, chainNext, chainReveal, chainFinished,
                chainBonusCoins, chainBonusFr))
    }

    // ---------------- the judges (one per verb) ----------------

    /** FETCH, the query half: an active errand this hand-in
     *  would settle. The Give UI routes through handInFetch. */
    fun fetchFor(state: GameState, asker: String, itemId: String): Errand? =
        active(state).firstOrNull {
            it.type == "FETCH" && it.asker == asker && it.itemId == itemId
        }

    /** FETCH judged: [asker] receives [itemId] from your pack
     *  (the pack copy is consumed here, not gifted on). */
    fun handInFetch(state: GameState, asker: String, itemId: String): ErrandCompletion? {
        val e = fetchFor(state, asker, itemId) ?: return null
        val held = state.inventory[itemId] ?: 0
        if (held <= 0) return null
        if (held == 1) state.inventory.remove(itemId)
        else state.inventory[itemId] = held - 1
        return complete(state, e)
    }

    /** DELIVER judged: you talked to the recipient. */
    fun deliverTo(state: GameState, targetName: String): ErrandCompletion? {
        val e = active(state).firstOrNull {
            it.type == "DELIVER" && it.targetName == targetName
        } ?: return null
        return complete(state, e)
    }

    /** FIND judged: a careful Look at the right place. */
    fun recoverFind(state: GameState, locId: String): ErrandCompletion? {
        val e = active(state).firstOrNull {
            it.type == "FIND" && it.targetLoc == locId
        } ?: return null
        return complete(state, e)
    }

    /** FIX judged: a tap on the lamp (anywhere — it moved) or
     *  on the right bench. */
    fun onFixTap(state: GameState, kind: String, locId: String): ErrandCompletion? {
        val e = active(state).firstOrNull {
            it.type == "FIX" && it.fixKind == kind &&
                (it.targetLoc == null || it.targetLoc == locId)
        } ?: return null
        return complete(state, e)
    }

    /** PHOTO judged — called from inside Photos.applyPhoto,
     *  so a shutter click settles photo errands like it
     *  settles PHOTO goals. */
    fun onPhoto(state: GameState, rec: PhotoRecord): List<ErrandCompletion> {
        val names = rec.npcs.map { it.name }.toSet()
        val hits = active(state).filter {
            it.type == "PHOTO" &&
                ((it.targetName != null && it.targetName in names) ||
                    (it.targetLoc != null && it.targetLoc == rec.locId))
        }
        return hits.mapNotNull { complete(state, it) }
    }

    /** PLAY judged: a catch at the pond (Minigames.applyCatch). */
    fun onPlayCatch(state: GameState, speciesId: String): List<ErrandCompletion> {
        val hits = active(state).filter {
            it.type == "PLAY" && it.playKind == "fish:$speciesId"
        }
        return hits.mapNotNull { complete(state, it) }
    }

    /** PLAY judged: a Showdown win (Minigames.applyShowdownResult). */
    fun onPlayShowdown(state: GameState, won: Boolean): List<ErrandCompletion> {
        if (!won) return emptyList()
        val hits = active(state).filter {
            it.type == "PLAY" && it.playKind == "showdown"
        }
        return hits.mapNotNull { complete(state, it) }
    }

    /** PLAY judged: a Taco Catch final score (Minigames.applyTacoResult). */
    fun onPlayTaco(state: GameState, score: Int): List<ErrandCompletion> {
        val hits = active(state).filter {
            it.type == "PLAY" && it.playKind?.startsWith("taco:") == true &&
                score >= (it.playKind.removePrefix("taco:").toIntOrNull() ?: Int.MAX_VALUE)
        }
        return hits.mapNotNull { complete(state, it) }
    }

    // ---------------- daily rollover ----------------

    /** Midnight: finished errands leave the plate (their
     *  history is the count + the recent list), and yesterday's
     *  offers lapse — everyone may ask again tomorrow. The
     *  board needs no pruning: it was never stored. */
    fun dailyRollover(state: GameState) {
        state.errands.removeAll { it.done }
        val it = state.errandAsked.entries.iterator()
        while (it.hasNext()) {
            if (it.next().value != state.absDay) it.remove()
        }
    }

    // ---------------- the Journal page ----------------

    /** v0.6.0b: a live chain as a thread — the links that
     *  landed checked off, the live one lit, the links still
     *  hidden an ellipsis, and the origin asker waiting at
     *  the end of it, where the chain closes. */
    fun chainThread(e: Errand): String {
        val parts = mutableListOf<String>()
        for (p in e.chainPrior) parts += "✓ $p"
        parts += "▶ ${objective(e)}"
        if (e.chainLink < e.chainLen) parts += "…"
        parts += "🏁 ${e.chainFrom ?: e.asker}"
        return parts.joinToString(" → ")
    }

    /** The 📋 ERRANDS page, assembled pure like the mystery
     *  boards: plate, today's board, lately, and the title
     *  the town has given you. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "📋 ERRANDS — ${state.errandsDoneCount} run for this town" +
            (titleFor(state.errandsDoneCount)?.let { " · the town calls you: $it" } ?: "") +
            (chainsDone(state).takeIf { it > 0 }
                ?.let { " · 🧵 $it favor chains finished" } ?: "") +
            (Emergencies.rescuesDone(state).takeIf { it > 0 }
                ?.let { " · 🚨 $it emergencies answered" } ?: "")
        val act = active(state)
        // v0.8.0c: a live emergency rides its own slot and
        // its own line — it is not one of the plate's six,
        // and it answers to a clock, not the board.
        val emerg = act.firstOrNull { it.source == EMERG }
        val plate = act.filter { it.source != EMERG }
        if (emerg != null) {
            out += "🚨 EMERGENCY — ${emerg.asker} needs you NOW: " +
                "${objective(emerg)} — by " +
                "${minutesToTimeStr(emerg.emergDeadline)} " +
                "(${(emerg.emergDeadline - state.timeMin).coerceAtLeast(0)} " +
                "min left) — ${hintFor(state, emerg)}"
        }
        if (plate.isEmpty()) {
            if (emerg == null)
                out += "Your plate is empty. The board at City Hall always has something."
        } else {
            out += "ON YOUR PLATE (${plate.size}/$MAX_ACTIVE) — tap this card to drop one:"
            for (e in plate) {
                if (e.chainLink > 0 && e.chainLen >= 2) {
                    out += "🧵 CHAIN for ${e.chainFrom} — link " +
                        "${e.chainLink} of ${e.chainLen}: " +
                        "${objective(e)} — ${hintFor(state, e)}"
                    out += "   ${chainThread(e)}"
                } else {
                    out += "▶ ${objective(e)} — ${hintFor(state, e)}"
                }
            }
        }
        out += "📌 ON THE BOARD AT CITY HALL (today):"
        val open = boardOpen(state)
        if (open.isEmpty()) {
            out += "· bare — every note's spoken for. Fresh ink tomorrow."
        } else {
            for (e in open) out += "· ${objective(e)} — ${e.asker} posted it" +
                (if (e.chainLen >= 2) " 🧵 (word is, it tangles)" else "")
        }
        if (state.errandsRecent.isNotEmpty()) {
            out += "✅ LATELY:"
            for (n in state.errandsRecent.takeLast(3).reversed()) out += "· ${n.line}"
        }
        return out
    }
}

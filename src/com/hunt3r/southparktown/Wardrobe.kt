package com.hunt3r.southparktown

// ============================================================
// Wardrobe — v0.5.0b (Queue #5 item 2): outfits & unlockables.
//
// The creator's shelf grows by fourteen pieces — hats, tops,
// bottoms and accessories — and every one of them starts
// LOCKED. They are ordinary creator options in the existing
// CREATOR_OPTIONS lists (the same data drives the creator,
// the dice and avatarSpec — there is NO parallel appearance
// system); this file is only the registry of where each one
// comes from and the pure rails that grant, sell and notice
// them:
//
//  - COIN (4): bought with Capsule Coins at a real counter —
//    two at the South Park Mall's clothing rack, two at
//    Jimbo's Guns (the counter that fits the piece).
//  - FEAT (4): one each from the errand / photo / game / walk
//    feat families. Crossing the feat grants the piece with
//    a ticker line and a town-log line; syncUnlocks also
//    grants retroactively, so a save already past the feat
//    is dressed on load.
//  - SEASONAL (3): two for playing a holiday's signature
//    beat (Halloween's Full Route, Christmas's Secret Santa
//    Relay — HolidayBeats.yearsDone is the record), and the
//    Deep Winter Parka from the season itself: its trigger,
//    named in the data, is the in-game calendar standing in
//    winter (seasonFor(gameMonth) == WINTER, Dec–Feb).
//  - SPECIAL (3): a mystery-case retirement mark (any of
//    the three cases retired), the Town Fixture piece (50
//    errands, Errands' own feat), and a collection milestone
//    (15 of the 25 collectibles found).
//
// Ownership lives in ONE additive save key — the
// `unlockedWardrobe` set on GameState, decode-filtered to
// this registry (Prefs). Unlocks are permanent and
// monotonic: you can never wear what you don't own (the
// creator greys locked pieces and the dice never rolls
// them), so "a worn piece was unlocked since your last
// outfit change" and "an NPC notices the piece" are the
// same event — Wardrobe.noticeLine rides the existing
// outfit-comment rail with piece-specific lines, a few of
// them from a named member of the cast.
// ============================================================

data class WardrobePiece(
    val id: String,               // == the creator option id
    val category: String,         // a CREATOR_ORDER category
    val name: String,
    val kind: String,             // COIN | FEAT | SEASONAL | SPECIAL
    val sourceLabel: String,      // the named unlock source, player-facing
    val hint: String,             // how to earn it, player-facing
    val price: Int = 0,           // COIN: capsule coins at [shopLoc]
    val shopLoc: String? = null,  // COIN: the counter that sells it
    val featFamily: String? = null, // FEAT: errand | photo | game | walk
    val featId: String? = null,   // FEAT: the feat in that family's table
    val holidayId: String? = null,  // SEASONAL: the beat's holiday
    val trigger: String? = null,  // SEASONAL/SPECIAL: a TRIGGER_* id
    val noticeLine: String,       // the town notices this piece (generic)
    val noticeNpc: String? = null,  // a named cast member with an opinion
    val noticeNpcLine: String? = null
)

object Wardrobe {

    const val KIND_COIN = "COIN"
    const val KIND_FEAT = "FEAT"
    const val KIND_SEASONAL = "SEASONAL"
    const val KIND_SPECIAL = "SPECIAL"

    // The named non-feat, non-coin triggers (data, not vibes).
    const val TRIGGER_WINTER = "season_winter"        // the calendar stands in winter
    const val TRIGGER_MYSTERY = "mystery_retired"     // any case retired
    const val TRIGGER_FIXTURE = "town_fixture"        // 50 errands (Errands.errand_50)
    const val TRIGGER_COLLECTION = "collection_15"    // 15 of 25 collectibles

    /** The collection milestone, in found collectibles. */
    const val COLLECTION_MILESTONE = 15

    val SHOPS: Set<String> = setOf("Mall", "JimbosGuns")

    // ---------------- the fourteen ----------------

    val PIECES: List<WardrobePiece> = listOf(

        // ---- COIN: the Mall rack (2) + Jimbo's counter (2) ----
        WardrobePiece("mall_rat_cap", "hat", "Mall Rat Cap", KIND_COIN,
            sourceLabel = "the clothing rack inside the South Park Mall",
            hint = "Buy it at the clothing rack inside the South Park Mall — 12 🪙.",
            price = 12, shopLoc = "Mall",
            noticeLine = "A Mall Rat Cap. You smell faintly of food court. Respect."),
        WardrobePiece("varsity_jacket", "top", "Varsity Jacket", KIND_COIN,
            sourceLabel = "the clothing rack inside the South Park Mall",
            hint = "Buy it at the clothing rack inside the South Park Mall — 18 🪙.",
            price = 18, shopLoc = "Mall",
            noticeLine = "A varsity jacket? On a fourth grader? The letter is doing a lot of work."),
        WardrobePiece("rhinestone_cowboy", "hat", "Rhinestone Cowboy Hat", KIND_COIN,
            sourceLabel = "the counter at Jimbo's Guns",
            hint = "Buy it at the counter at Jimbo's Guns — 15 🪙. Jimbo has opinions ready.",
            price = 15, shopLoc = "JimbosGuns",
            noticeLine = "That hat has RHINESTONES. In this town. Brave. Reckless. Brave.",
            noticeNpc = "Jimbo",
            noticeNpcLine = "Rhinestones on a cowboy hat, kid? It's comin' right for us — " +
                "the whole CONCEPT of subtlety, I mean. …It looks good. Don't tell Ned I said so."),
        WardrobePiece("pro_camo", "bottom", "Pro Camo Pants", KIND_COIN,
            sourceLabel = "the counter at Jimbo's Guns",
            hint = "Buy it at the counter at Jimbo's Guns — 10 🪙.",
            price = 10, shopLoc = "JimbosGuns",
            noticeLine = "Pro camo. I can only see you from the waist up. It's unnerving.",
            noticeNpc = "Jimbo",
            noticeNpcLine = "Pro camo pants. Good. If a deer sees your top half, " +
                "that's a YOU problem, but the legs? Ghosts."),

        // ---- FEAT: one per feat family ----
        WardrobePiece("neighbor_scarf", "accessory", "Neighborly Scarf", KIND_FEAT,
            sourceLabel = "the 📋 Neighborly feat (10 errands)",
            hint = "Run 10 errands for the town — the 📋 Neighborly feat knits this for you.",
            featFamily = "errand", featId = "errand_10",
            noticeLine = "That scarf smells like thank-you notes and other people's groceries."),
        WardrobePiece("squad_hoodie", "top", "Squad Hoodie", KIND_FEAT,
            sourceLabel = "the 📸 The Whole Squad photo feat",
            hint = "Get Stan, Kyle, Cartman & Kenny in one photo — the 📸 Whole Squad feat.",
            featFamily = "photo", featId = "squad_shot",
            noticeLine = "Squad hoodie. You're IN the photo now. Permanently. That's how fabric works.",
            noticeNpc = "Cartman",
            noticeNpcLine = "Oh, the squad hoodie. You EARNED that. I was in that photo. " +
                "I made that photo. You're welcome."),
        WardrobePiece("taco_hat", "hat", "Taco Titan Hat", KIND_FEAT,
            sourceLabel = "the 🌮 Taco Titan game feat (500+ in Taco Catch)",
            hint = "Score 500+ in one game of Taco Catch at Freeman's — the 🌮 Taco Titan feat.",
            featFamily = "game", featId = "taco_500",
            noticeLine = "You are wearing a taco. On your head. This town has seen things, and this is one of them.",
            noticeNpc = "Chef",
            noticeNpcLine = "A taco hat, child. A TACO. HAT. I have cooked many things " +
                "in my life, but I never thought to wear the menu."),
        WardrobePiece("road_jeans", "bottom", "Road-Worn Jeans", KIND_FEAT,
            sourceLabel = "the 🚶 First Steps walk feat (10 trips on foot)",
            hint = "Complete 10 trips on foot — the 🚶 First Steps walk feat breaks these in.",
            featFamily = "walk", featId = "walk_10",
            noticeLine = "Those jeans have MILES on them. The slush lines are basically a map."),

        // ---- SEASONAL: two holiday beats + the season itself ----
        WardrobePiece("pumpkin_sweater", "top", "Pumpkin Sweater", KIND_SEASONAL,
            sourceLabel = "Halloween's signature beat, \"The Full Route\"",
            hint = "Play Halloween's signature beat — trick-or-treat the Full Route, all five houses and your own porch.",
            holidayId = "halloween",
            noticeLine = "A pumpkin sweater. It is ALWAYS Halloween in that sweater. Always."),
        WardrobePiece("santa_hat", "hat", "Santa Hat", KIND_SEASONAL,
            sourceLabel = "Christmas's signature beat, \"The Secret Santa Relay\"",
            hint = "Play Christmas's signature beat — carry the Secret Santa Relay all the way home.",
            holidayId = "christmas",
            noticeLine = "A Santa hat. Bold, outside of December. The critters approve, probably.",
            noticeNpc = "Butters",
            noticeNpcLine = "Oh! A Santa hat! I checked — you're not Santa. " +
                "Santa's taller. And he never comes to my house anymore on account of the incident."),
        WardrobePiece("deep_winter_parka", "top", "Deep Winter Parka", KIND_SEASONAL,
            sourceLabel = "the winter season itself",
            hint = "Live in South Park while the in-game calendar stands in winter " +
                "(December–February). The season hands it over — Colorado provides.",
            trigger = TRIGGER_WINTER,
            noticeLine = "That parka has survived things. You can tell. The zippers have seen snow with snow on it."),

        // ---- SPECIAL: the case, the title, the shelf ----
        WardrobePiece("sleuth_cap", "hat", "Sleuth's Cap", KIND_SPECIAL,
            sourceLabel = "retiring any mystery case",
            hint = "Retire any one of the town's mystery cases — the cap is the mark it leaves.",
            trigger = TRIGGER_MYSTERY,
            noticeLine = "A sleuth's cap. You close ONE case and suddenly it's a whole look. Earned, though."),
        WardrobePiece("fixture_vest", "top", "Town Fixture Vest", KIND_SPECIAL,
            sourceLabel = "the 📋 Town Fixture title (50 errands)",
            hint = "Run 50 errands and earn the 📋 Town Fixture title. The vest is part of the uniform nobody issued.",
            trigger = TRIGGER_FIXTURE,
            noticeLine = "The Town Fixture vest. Fifty errands deep and the town just hands you things now.",
            noticeNpc = "McDaniels",
            noticeNpcLine = "The vest. THE vest. Fifty errands, and this town runs " +
                "on you more than it runs on me. I find that restful, honestly."),
        WardrobePiece("collector_scarf", "accessory", "Completionist Scarf", KIND_SPECIAL,
            sourceLabel = "the collection milestone — 15 of 25 collectibles",
            hint = "Find 15 of the town's 25 collectibles. The shelf insists on a scarf ceremony.",
            trigger = TRIGGER_COLLECTION,
            noticeLine = "That scarf is woven from pure shelf energy. Fifteen finds deep, and it shows.")
    )

    val PIECE_IDS: Set<String> = PIECES.map { it.id }.toSet()

    fun pieceById(id: String): WardrobePiece? = PIECES.firstOrNull { it.id == id }

    fun pieceFor(category: String, optionId: String): WardrobePiece? =
        PIECES.firstOrNull { it.category == category && it.id == optionId }

    fun piecesAt(shopLoc: String): List<WardrobePiece> =
        PIECES.filter { it.kind == KIND_COIN && it.shopLoc == shopLoc }

    /** Total capsule-coin sink across the coin shelf. */
    fun coinTotal(): Int = PIECES.filter { it.kind == KIND_COIN }.sumOf { it.price }

    // ---------------- ownership ----------------

    fun owned(state: GameState, piece: WardrobePiece): Boolean =
        piece.id in state.unlockedWardrobe

    fun ownedCount(state: GameState): Int =
        PIECES.count { owned(state, it) }

    /** The creator's lock question, whole: a piece is pickable
     *  when it isn't a wardrobe piece, or it's owned. (The
     *  older collectible shelf — CreatorOption.unlockId — is
     *  judged beside this in the creator, not inside it.) */
    fun isLocked(state: GameState, category: String, optionId: String): Boolean {
        val p = pieceFor(category, optionId) ?: return false
        return !owned(state, p)
    }

    /** Same question for callers holding only id sets (the
     *  creator, before any state exists). */
    fun isLocked(unlocked: Set<String>, category: String, optionId: String): Boolean {
        val p = pieceFor(category, optionId) ?: return false
        return p.id !in unlocked
    }

    /** Strip anything unowned out of an appearance — locked
     *  wardrobe pieces and un-found collectible pieces fall
     *  back to the blank New Kid default for their category.
     *  Unlocks are monotonic (never revoked), so a preset or
     *  a saved look made of owned pieces passes through
     *  untouched; this only ever bites a hand-edited save. */
    fun sanitizeAppearance(appearance: Map<String, String>,
                           collected: Set<String>,
                           unlocked: Set<String>): Map<String, String> {
        val a = validateAppearance(appearance).toMutableMap()
        for (c in CREATOR_ORDER) {
            val opt = creatorOpt(c, a[c]!!)
            if (opt.unlockId != null && opt.unlockId !in collected)
                a[c] = DEFAULT_APPEARANCE[c]!!
            else if (isLocked(unlocked, c, a[c]!!))
                a[c] = DEFAULT_APPEARANCE[c]!!
        }
        return a
    }

    /** A preset may only ever hold owned pieces — by
     *  construction (the creator can't pick a locked piece)
     *  and, belt-and-braces, by this check at save/load. */
    fun presetOk(appearance: Map<String, String>,
                 collected: Set<String>, unlocked: Set<String>): Boolean =
        sanitizeAppearance(appearance, collected, unlocked) ==
            validateAppearance(appearance)

    // ---------------- the auto-grant conditions ----------------

    /** Has [piece]'s feat been crossed? Each family reads its
     *  own table — the same source of truth the Collection
     *  page renders — never a copy of the threshold. */
    fun featCrossed(state: GameState, piece: WardrobePiece): Boolean =
        when (piece.featFamily) {
            "errand" -> Errands.featById(piece.featId ?: "")
                ?.let { Errands.featEarned(state, it) } == true
            "photo" -> piece.featId in state.photoFeats &&
                Photos.featById(piece.featId ?: "") != null
            "game" -> piece.featId in state.gameFeats &&
                Minigames.featById(piece.featId ?: "") != null
            "walk" -> Walk.featById(piece.featId ?: "")
                ?.let { Walk.featEarned(state, it) } == true
            else -> false
        }

    /** Has [piece]'s seasonal/special trigger fired? */
    fun triggerMet(state: GameState, piece: WardrobePiece): Boolean = when {
        piece.holidayId != null ->
            HolidayBeats.yearsDone(state, piece.holidayId) > 0 &&
                HolidayBeats.beatFor(piece.holidayId) != null
        piece.trigger == TRIGGER_WINTER ->
            seasonFor(state.gameMonth) == Season.WINTER
        piece.trigger == TRIGGER_MYSTERY ->
            state.arcRetired || state.arc2Retired || state.arc3Retired ||
                state.arc4Retired
        piece.trigger == TRIGGER_FIXTURE ->
            Errands.featById("errand_50")
                ?.let { Errands.featEarned(state, it) } == true
        piece.trigger == TRIGGER_COLLECTION ->
            state.collected.size >= COLLECTION_MILESTONE
        else -> false
    }

    /** An auto piece (feat / seasonal / special) is DUE when
     *  its condition stands and it isn't owned yet. Coin
     *  pieces are never due — they're bought, not granted. */
    fun due(state: GameState, piece: WardrobePiece): Boolean {
        if (owned(state, piece)) return false
        return when (piece.kind) {
            KIND_FEAT -> featCrossed(state, piece)
            KIND_SEASONAL, KIND_SPECIAL -> triggerMet(state, piece)
            else -> false
        }
    }

    /** Grant [piece] if unowned: the set is the authority,
     *  the ticker and the town log say it happened. Returns
     *  true when this call granted it (idempotent). */
    fun grant(state: GameState, piece: WardrobePiece, why: String): Boolean {
        if (!state.unlockedWardrobe.add(piece.id)) return false
        state.events.add("👕 WARDROBE — ${piece.name} unlocked! ($why)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "👕 The new kid unlocked the ${piece.name} — $why")
        return true
    }

    /** Grant every due auto piece. Called on load (the
     *  retroactive pass for saves already past a feat, a
     *  beat, a retirement) and after the actions that can
     *  cross one. Returns the pieces this call granted. */
    fun syncUnlocks(state: GameState): List<WardrobePiece> {
        val out = mutableListOf<WardrobePiece>()
        for (p in PIECES) {
            if (!due(state, p)) continue
            val why = when (p.kind) {
                KIND_FEAT -> p.sourceLabel
                KIND_SEASONAL -> if (p.holidayId != null)
                    "played ${p.sourceLabel}" else "the season provides"
                else -> p.sourceLabel
            }
            if (grant(state, p, why)) out += p
        }
        return out
    }

    // ---------------- the coin rail ----------------

    /** The counter transaction. Returns "owned" | "bought" |
     *  "poor" | "notsold" (no such piece / not a coin piece /
     *  wrong counter) — the Walk.buyPass vocabulary, kept. */
    fun buy(state: GameState, pieceId: String, atLoc: String): String {
        val p = pieceById(pieceId) ?: return "notsold"
        if (p.kind != KIND_COIN || p.shopLoc != atLoc) return "notsold"
        if (owned(state, p)) return "owned"
        val coins = Minigames.capsuleCoins(state)
        if (coins < p.price) return "poor"
        val left = coins - p.price
        if (left > 0) state.inventory[Minigames.COIN_ITEM] = left
        else state.inventory.remove(Minigames.COIN_ITEM)
        grant(state, p, "bought at ${p.sourceLabel} for ${p.price} 🪙")
        return "bought"
    }

    // ---------------- the town notices ----------------

    /** The outfit-comment rail, piece-aware: when the first
     *  changed category's new option is a wardrobe piece, the
     *  line is that piece's own — from its named cast member
     *  when [npcName] is the one with the opinion, else the
     *  generic piece line. Null = no piece involved; the
     *  caller falls back to the category line. Pure, so the
     *  suite pins the whole rail. */
    fun noticeLine(newApp: Map<String, String>, oldApp: Map<String, String>,
                   npcName: String): String? {
        val changed = newApp.keys.firstOrNull { oldApp[it] != newApp[it] }
            ?: return null
        val p = pieceFor(changed, newApp[changed] ?: "") ?: return null
        return if (p.noticeNpc != null && p.noticeNpc == npcName &&
            p.noticeNpcLine != null) p.noticeNpcLine else p.noticeLine
    }

    // ---------------- the Collection page ----------------

    /** 👕 WARDROBE lines for the Collection surface: owned
     *  pieces named with their source, locked ones as
     *  silhouettes carrying their hint — the collection's
     *  own idiom, applied to your closet. */
    fun collectionLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "👕 WARDROBE — ${ownedCount(state)}/${PIECES.size} pieces owned. " +
            "Unlocked pieces join the character creator (the mirror at your house " +
            "wears your saved presets; locked pieces show there greyed, with their hints)."
        for (p in PIECES) {
            out += if (owned(state, p))
                "✅ ${p.name} (${CREATOR_OPTIONS[p.category]?.first ?: p.category}) — ${p.sourceLabel}"
            else
                "👤 ??? — ${p.hint}"
        }
        if (ownedCount(state) == 0)
            out += "Nothing unlocked yet. The town is full of ways to dress better — " +
                "errands, photos, games, walks, holidays, cases, and two honest counters."
        return out
    }
}

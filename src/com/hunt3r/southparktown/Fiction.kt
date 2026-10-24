package com.hunt3r.southparktown

// ============================================================
// Fiction — v0.16.0e PASS 2: the COSTUME SYSTEM (Lucky's
// docs: town-costume-system-spec.md reframing
// town-scenario-diff-spec.md). The town is a constant;
// the kids' game is a variable; the DIFF is the output.
//
// The composition, bottom to top:
//  0. BASE TOWN — frozen. Locations + street sections
//     are the parcels; runtime code NEVER writes to
//     them (the suite pins baseline drift across a
//     full fiction run). Slots are fixed; only slot
//     CONTENTS vary.
//  1. CIVIC LAYER — the real town (schedules, the life
//     sim) always runs underneath and is NEVER
//     rewritten by this system: the population channel
//     overlays costume states only.
//  2. FICTION LAYER — a fiction is DATA, never code:
//     id, theme, vocabulary, palette, prop kit, rank
//     names, claim/flex bias, escalation curve.
//     Swapping theme is swapping the data row.
//  3. RETHEME — one pure function: dress(). Global,
//     simultaneous, theme-consistent, a pure function
//     of (fiction, startTick, tick) — resolving at an
//     earlier tick IS the revert; there is no undo.
//  4. FABRICATION — new sites only in reserved flex
//     zones (wild/urban anchors + fabricated interior
//     sets behind real doors). Overlay state, never
//     base locations; gone on collapse but for residue.
//  5. DIEGESIS LEDGER — per-adult belief, seeded and
//     stable per fiction: >0.75 full costume, >0.45
//     partial (towel-cloak energy), else unimpressed.
//     Institutions lean in through the signage channel.
//  6. PROP PROVENANCE — every dressing records the
//     household junk it is made from, its attachment
//     and its wear — and coverage < 1.0 ALWAYS (a
//     suite pin): the real object shows through,
//     because that is the joke, enforced structurally.
//  7. TRANSITION DIRECTOR — REAL → DRESSING → LIVE →
//     ESCALATING → COLLAPSING → RESIDUE on the game
//     clock. DRESSING is visible work; RESIDUE is a
//     small explicit carry-over list, CAPPED, and the
//     only fiction state (besides id/start/phase) a
//     save ever stores. Everything else is derived
//     from (seed, fiction_id, parcel_id, tick), so a
//     replay dresses the town identically.
// ============================================================

object Fiction {

    // ---------- the fiction definition: pure data ----------

    data class Def(
        val id: String,
        val theme: String,
        val title: String,
        val factionA: String,
        val factionB: String,
        val playerTitle: String,
        val primaryHex: String,
        val accentHex: String,
        val propKit: List<String>,
        val rankNames: List<String>,
        /** Location ids the fiction claims first, in
         *  order (doc 4's claim_bias, mapped to this
         *  town's ids: city_hall→CityHall, school→
         *  Elementary, church→Church, alley→SoDoSoPa,
         *  junkyard→SoDoSoPa's scrap corner). */
        val claimBias: List<String>,
        /** Reserved flex anchors (wild/urban) +
         *  fabricated-interior section ids. */
        val flexBias: List<String>,
        val escalationCurve: List<Float>,
        val budgetStart: Int
    )

    /** The ONE complete playable fiction shipped in
     *  this build — doc 4's fantasy YAML, verbatim in
     *  spirit: Kingdom of Kupa Keep vs the Elves, the
     *  player is the New Kid, and the prop kit is
     *  cardboard, bedsheets, brooms, lids and sticks. */
    val FANTASY = Def(
        id = "stick_of_truth",
        theme = "fantasy",
        title = "The Stick of Truth",
        factionA = "Kingdom of Kupa Keep",
        factionB = "The Elves",
        playerTitle = "The New Kid",
        primaryHex = "#7a4b2a",
        accentHex = "#c9a227",
        propKit = listOf("cardboard_wall", "bedsheet_banner",
            "broom_lance", "lid_shield", "stick_sword"),
        rankNames = listOf("squire", "knight", "paladin"),
        claimBias = listOf("CityHall", "Elementary",
            "Church", "SoDoSoPa"),
        flexBias = listOf("StarksPond", "TegridyFarms",
            "fiction:dungeon", "fiction:lair"),
        escalationCurve = listOf(0.0f, 0.2f, 0.5f, 0.8f, 1.0f),
        budgetStart = 46
    )

    val ALL: List<Def> = listOf(FANTASY)

    fun defById(id: String?): Def? =
        ALL.firstOrNull { it.id == id }

    /** What each fiction prop is MADE FROM — the
     *  provenance chain (doc 4, system 2). */
    val MADE_FROM: Map<String, String> = mapOf(
        "cardboard_wall" to "refrigerator_box",
        "bedsheet_banner" to "bedsheet",
        "broom_lance" to "broom",
        "lid_shield" to "trash_can_lid",
        "stick_sword" to "stick"
    )

    /** The fiction name a claimed site wears. */
    fun claimName(def: Def, locId: String): String? =
        when (locId) {
            "CityHall" -> "The King's Keep"
            "Elementary" -> "The Training Grounds"
            "Church" -> "The Chapel of Holding"
            "SoDoSoPa" -> "The Bandit Camp"
            else -> null
        }

    /** Institution signage (the signage channel):
     *  City Hall posts a themed notice, the church
     *  renames its bake sale, the school changes its
     *  mascot — system behaviour, not one-off text. */
    fun institutionSign(def: Def, locId: String): String? =
        when (locId) {
            "CityHall" -> "BY ORDER OF THE KING — " +
                "COURT IS IN SESSION"
            "Church" -> "YE OLDE BAKE SALE — " +
                "TITHES OF CAKE ACCEPTED"
            "Elementary" -> "HOME OF THE KNIGHTS — " +
                "GO KNIGHTS"
            "MainStreet" -> "KUPA KEEP HIGH ROAD"
            else -> null
        }

    // ---------- layer 0: the frozen base, as parcels ----------

    data class Parcel(
        val id: String,          // locId, or locId#i for houses
        val locId: String,
        val zoning: String,      // residential|commercial|civic|wild
        val landmark: Boolean    // protected: dress, never remove
    )

    /** The base parcel model over the existing town:
     *  one parcel per location, plus one parcel per
     *  house slot on each residential street section
     *  (the re-dress's facades ARE the base contents of
     *  those slots). Derived fresh from the frozen
     *  data on every call — nothing here is stored. */
    fun parcels(): List<Parcel> {
        val out = mutableListOf<Parcel>()
        for ((id, loc) in LOCATIONS) {
            val zoning = when (loc.kind) {
                "house" -> "residential"
                "shop", "hub" -> "commercial"
                "school", "civic" -> "civic"
                else -> "wild"
            }
            out += Parcel(id, id, zoning,
                landmark = id in setOf("CityHall",
                    "Elementary", "Church", "BusStop"))
        }
        // one parcel per house on every residential
        // street (the neighbor row's slots): the same
        // StreetHouse identities the painter and the
        // door registry share, keyed by street + slot
        for (loc in LOCATIONS.values) {
            if (loc.kind != "house") continue
            for (house in ResidentialStreets.doorHouses(loc)) {
                if (house.seqIndex < 0) continue  // the facade itself
                out += Parcel("house:${loc.id}:${house.seqIndex}",
                    loc.id, "residential", landmark = false)
            }
        }
        return out
    }

    /** The dressing cost of one parcel, by zoning. */
    fun costOf(p: Parcel): Int = when (p.zoning) {
        "civic" -> 3
        "commercial" -> 2
        else -> 1
    }

    // ---------- determinism ----------

    /** The one hash every derived value hangs off:
     *  (seed, fiction, parcel) — replays dress
     *  identically, always. */
    fun seedOf(fictionId: String, key: String): Int {
        var h = 2166136261u
        val s = "fiction:$fictionId:$key"
        for (ch in s) {
            h = h xor ch.code.toUInt()
            h *= 16777619u
        }
        return (h and 0x7fffffffu).toInt()
    }

    private fun frac(seed: Int, salt: Int): Float {
        var h = (seed * 31 + salt * 1013904223).toUInt()
        h = h xor (h shr 13)
        h *= 0x5bd1e995u
        h = h xor (h shr 15)
        return (h and 0xffffffu).toFloat() / 16777216f
    }

    // ---------- layer 5: the diegesis ledger ----------

    enum class Costume { NONE, PARTIAL, FULL }

    /** An adult's belief in the current fiction:
     *  seeded per (fiction, name), stable for the
     *  whole run — Randy commits on day one and never
     *  wavers. */
    fun belief(fictionId: String, adultName: String): Float =
        frac(seedOf(fictionId, "belief:$adultName"), 7)

    fun costumeOf(fictionId: String, name: String,
                  isKid: Boolean): Costume {
        if (isKid) return Costume.FULL   // the kids ARE the game
        val b = belief(fictionId, name)
        return when {
            b > 0.75f -> Costume.FULL
            b > 0.45f -> Costume.PARTIAL
            else -> Costume.NONE
        }
    }

    // ---------- layer 6: prop provenance ----------

    data class Dressing(
        val fictionProp: String,
        val madeFrom: String,
        /** STRUCTURAL RULE: always < 1.0 — the real
         *  object shows through the costume. */
        val coverage: Float,
        val attachment: String,  // tape|string|nail|held|leaned
        val wear: Float
    ) {
        /** v0.17.0a: the paper region this dressing's stock
         *  belongs to (spgen §3, region-aware synthesis) —
         *  the shield is METAL (a trash-can lid, sheen and
         *  all), the banner is CLOTH (a woven bedsheet), the
         *  rest is paper SURFACE. One registry for figures
         *  and set pieces alike (SetPieces reads the same
         *  regions). */
        val region: PaperRegion
            get() = when (fictionProp) {
                "lid_shield" -> PaperRegion.METAL
                "bedsheet_banner" -> PaperRegion.CLOTH
                else -> PaperRegion.SURFACE
            }

        /** v0.17.0a: this prop's size in kid units when it
         *  is WORN or CARRIED — from ScaleContract's
         *  prop-relative table (sized from the wearer,
         *  never absolute). */
        fun wornSizeU(): Float =
            ScaleContract.PROP_RELATIVE[fictionProp] ?: 0.5f
    }

    private fun dressingFor(def: Def, parcel: Parcel,
                            slot: Int): Dressing {
        val seed = seedOf(def.id, parcel.id)
        val prop = def.propKit[
            (seed / 7 + slot) % def.propKit.size]
        val attach = listOf("tape", "string", "nail",
            "leaned")[((seed shr 5) + slot) % 4]
        return Dressing(
            fictionProp = prop,
            madeFrom = MADE_FROM[prop] ?: "cardboard",
            coverage = 0.45f + frac(seed, slot * 3 + 1) * 0.35f,
            attachment = attach,
            wear = frac(seed, slot * 5 + 2) * 0.5f)
    }

    /** The prop kit a parcel wears, by zoning — a
     *  house gets a banner over its facade and a
     *  cardboard wall in its yard; a wild flex anchor
     *  gets the fabricated fort. */
    fun propsFor(def: Def, parcel: Parcel): List<Dressing> =
        when {
            parcel.locId == "StarksPond" &&
                parcel.id == parcel.locId -> listOf(
                    dressingFor(def, parcel, 0),
                    dressingFor(def, parcel, 1))
            parcel.zoning == "residential" -> listOf(
                dressingFor(def, parcel, 0),
                dressingFor(def, parcel, 1))
            parcel.zoning == "commercial" -> listOf(
                dressingFor(def, parcel, 0))
            parcel.zoning == "civic" -> listOf(
                dressingFor(def, parcel, 0),
                dressingFor(def, parcel, 1))
            else -> listOf(dressingFor(def, parcel, 0))
        }

    // ---------- layer 7: the transition director ----------

    enum class Phase { REAL, DRESSING, LIVE, ESCALATING,
        COLLAPSING, RESIDUE }

    /** Fiction ticks are the game's 15-minute ticks. */
    fun tickOf(state: GameState): Int =
        state.absDay * 96 + state.timeMin / 15

    const val DRESS_TICKS = 8        // two hours of taping
    const val LIVE_TICKS = 96        // a full day live
    const val ESC_TICKS = 96         // a day escalating
    const val COLLAPSE_TICKS = 8

    /** The phase machine, a pure function of the
     *  timeline. [endTick] is the tick the player
     *  ended the game early (Int.MAX_VALUE = never);
     *  collapse takes COLLAPSE_TICKS from either the
     *  natural end or the early end, whichever first. */
    fun phaseAt(startTick: Int, nowTick: Int,
                endTick: Int): Phase {
        val e = nowTick - startTick
        if (e < 0) return Phase.REAL
        val naturalEnd = DRESS_TICKS + LIVE_TICKS + ESC_TICKS
        val collapseStart = minOf(naturalEnd,
            if (endTick == Int.MAX_VALUE) Int.MAX_VALUE
            else endTick - startTick)
        return when {
            collapseStart <= e &&
                e < collapseStart + COLLAPSE_TICKS ->
                Phase.COLLAPSING
            e >= collapseStart + COLLAPSE_TICKS ->
                Phase.RESIDUE
            e < DRESS_TICKS -> Phase.DRESSING
            e < DRESS_TICKS + LIVE_TICKS -> Phase.LIVE
            else -> Phase.ESCALATING
        }
    }

    /** The dress order: claims first (the fiction
     *  takes its strongholds), then by seeded hash —
     *  deterministic, and the tail of the order is
     *  the "far side of town" the budget runs dry on. */
    fun dressOrder(def: Def): List<Parcel> {
        val all = parcels()
        val claims = def.claimBias.mapNotNull { cid ->
            all.firstOrNull { it.id == cid } }
        // the flex anchors dress next — fabrication
        // sites are load-bearing for the fiction, so
        // the budget reaches them before the long tail
        val flex = def.flexBias.mapNotNull { fid ->
            all.firstOrNull { it.id == fid } }
        val rest = all.filter { it !in claims && it !in flex }
            .sortedBy { seedOf(def.id, it.id) }
        return claims + flex + rest
    }

    /** The tick at which a parcel's dressing STARTS:
     *  spread across the DRESSING window by order. */
    fun dressTickOf(def: Def, orderIdx: Int, n: Int): Int =
        if (n <= 1) 0
        else orderIdx * DRESS_TICKS / n

    /** Craft budget available at an elapsed tick:
     *  the start purse plus escalation refills along
     *  the fiction's curve (each phase boundary pays
     *  out the curve's step × the start purse). */
    fun budgetAt(def: Def, elapsed: Int): Int {
        var b = def.budgetStart
        val step = def.budgetStart / 2
        if (elapsed >= DRESS_TICKS) b += (step *
            (def.escalationCurve.getOrElse(1) { 0f } * 2)).toInt()
        if (elapsed >= DRESS_TICKS + LIVE_TICKS) b += (step *
            (def.escalationCurve.getOrElse(3) { 0f } * 2)).toInt()
        return b
    }

    // ---------- layer 3: the retheme pass ----------

    data class ParcelDress(
        val parcelId: String,
        val paletteHex: String?,
        val signText: String?,
        val claimName: String?,
        val props: List<Dressing>,
        /** 0..1 — DRESSING parcels are mid-tape. */
        val progress: Float
    )

    const val RESIDUE_CAP = 5

    /** dress(base, civic, fiction, ledger, budget):
     *  the single pure retheme. Inputs: the fiction
     *  def, the start tick, the current tick, an
     *  optional early-end tick, and the persisted
     *  residue (only read in RESIDUE). Output: the
     *  dressing set for THIS tick. Resolving at the
     *  start tick yields the bare town; resolving
     *  later and then earlier again yields it too —
     *  revert is resolution, not undo. */
    fun dress(def: Def, startTick: Int, nowTick: Int,
              endTick: Int,
              residue: List<String>): Map<String, ParcelDress> {
        val phase = phaseAt(startTick, nowTick, endTick)
        if (phase == Phase.REAL) return emptyMap()
        if (phase == Phase.RESIDUE) {
            // only the explicit carry-over survives
            val out = mutableMapOf<String, ParcelDress>()
            for (entry in residue.take(RESIDUE_CAP)) {
                val parts = entry.split(":")
                if (parts.size != 2) continue
                val (pid, prop) = parts
                val parcel = parcels().firstOrNull {
                    it.id == pid } ?: continue
                out[pid] = ParcelDress(pid, null, null, null,
                    listOf(Dressing(prop,
                        MADE_FROM[prop] ?: "cardboard",
                        coverage = 0.5f, attachment = "leaned",
                        wear = 0.4f)),
                    progress = 1f)
            }
            return out
        }
        val rawElapsed = nowTick - startTick
        // COLLAPSING replays the settled set with the
        // progress draining back out — the overlay
        // visibly comes DOWN, parcel by parcel
        val collapseStart = minOf(DRESS_TICKS + LIVE_TICKS +
            ESC_TICKS,
            if (endTick == Int.MAX_VALUE) Int.MAX_VALUE
            else endTick - startTick)
        val elapsed = if (phase == Phase.COLLAPSING)
            collapseStart else rawElapsed
        val strip = if (phase == Phase.COLLAPSING)
            1f - (rawElapsed - collapseStart).toFloat() /
                COLLAPSE_TICKS else 1f
        val order = dressOrder(def)
        var spent = 0
        val budget = budgetAt(def, elapsed)
        val out = mutableMapOf<String, ParcelDress>()
        for ((idx, parcel) in order.withIndex()) {
            val dt = dressTickOf(def, idx, order.size)
            if (elapsed < dt) continue        // not started
            val cost = costOf(parcel)
            if (spent + cost > budget) continue // budget dry
            spent += cost
            val progress = when (phase) {
                Phase.DRESSING -> ((elapsed - dt) / 2f)
                    .coerceIn(0.15f, 1f)
                Phase.COLLAPSING -> strip
                else -> 1f
            }
            val isClaim = parcel.id in def.claimBias
            out[parcel.id] = ParcelDress(
                parcelId = parcel.id,
                paletteHex = if (parcel.zoning ==
                    "residential") def.primaryHex else null,
                signText = institutionSign(def, parcel.locId)
                    ?.takeIf { parcel.id == parcel.locId },
                claimName = if (isClaim)
                    claimName(def, parcel.locId) else null,
                props = propsFor(def, parcel),
                progress = progress)
        }
        return out
    }

    /** The residue a collapse leaves behind: the
     *  first RESIDUE_CAP dressed parcels in dress
     *  order, one prop each — a cardboard SOMETHING
     *  still standing in a yard, scenarios later. */
    fun residueFor(def: Def, startTick: Int,
                   collapseTick: Int,
                   endTick: Int): List<String> {
        val dressed = dress(def, startTick,
            collapseTick - 1, endTick, emptyList())
        return dressOrder(def)
            .filter { dressed.containsKey(it.id) }
            .take(RESIDUE_CAP)
            .map { "${it.id}:${propsFor(def, it)[0].fictionProp}" }
    }

    // ---------- save state: ONLY these keys ----------

    const val KEY_ID = "fiction:id"
    const val KEY_START = "fiction:start"
    const val KEY_END = "fiction:end"
    const val KEY_RESIDUE = "fiction:residue"

    fun activeDef(state: GameState): Def? =
        defById(state.lifeText[KEY_ID])

    fun startTickOf(state: GameState): Int =
        state.life[KEY_START] ?: 0

    fun endTickOf(state: GameState): Int =
        state.life[KEY_END] ?: Int.MAX_VALUE

    fun residueOf(state: GameState): List<String> =
        state.lifeText[KEY_RESIDUE]
            ?.split(";")?.filter { it.isNotBlank() }
            ?: emptyList()

    fun phaseOf(state: GameState): Phase {
        activeDef(state) ?: return Phase.REAL
        return phaseAt(startTickOf(state), tickOf(state),
            endTickOf(state))
    }

    /** What the renderer consumes for THIS state:
     *  base + active diffs, nothing else. */
    fun dressNow(state: GameState):
        Map<String, ParcelDress> {
        val def = activeDef(state) ?: return emptyMap()
        return dress(def, startTickOf(state), tickOf(state),
            endTickOf(state), residueOf(state))
    }

    fun start(state: GameState, def: Def) {
        state.lifeText[KEY_ID] = def.id
        state.life[KEY_START] = tickOf(state)
        state.life.remove(KEY_END)
        state.lifeText.remove(KEY_RESIDUE)
    }

    fun requestEnd(state: GameState) {
        if (activeDef(state) == null) return
        if (state.life[KEY_END] == null)
            state.life[KEY_END] = tickOf(state)
    }

    /** Called by the tick hook when the machine
     *  reaches RESIDUE: stamp the carry-over list
     *  (capped) once, then the run is just residue
     *  until a new game starts. */
    fun settleResidue(state: GameState) {
        val def = activeDef(state) ?: return
        if (state.lifeText.containsKey(KEY_RESIDUE)) return
        val end = endTickOf(state)
        val collapseTick = minOf(
            startTickOf(state) + DRESS_TICKS + LIVE_TICKS +
                ESC_TICKS,
            if (end == Int.MAX_VALUE) Int.MAX_VALUE else end)
        state.lifeText[KEY_RESIDUE] = residueFor(def,
            startTickOf(state), collapseTick, end)
            .joinToString(";")
    }
}

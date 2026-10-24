package com.hunt3r.southparktown

import kotlin.random.Random

/**
 * v0.10.0e: YOUR STAND — a kid business, run honestly.
 *
 * The new kid files a permit at City Hall, buys a stand
 * kit, and sets up a snack stand at one of the town's real
 * outdoor pitches. Stock is bought from the town's real
 * shops at the pantry's real prices (Fair.BOOTH is the
 * town's price list; the suite pins the two together),
 * priced by the player inside a stated band, and sold ONLY
 * while the stand is staffed — by you standing at it, or by
 * a crew member who walked there on the real Walk graph
 * under the crew's own occupied rules. Customers are the
 * townsfolk physically present at the pitch on their real
 * agendas; nobody is conjured. Perishables spoil on their
 * stated shelf lives and the spoilage is written into the
 * books, never quietly vanished. After enough staffed
 * days, the rival's crew reviews the stand off its real
 * books — markup, sell-through, spoilage — and an
 * honestly-good review lifts foot traffic for a stated
 * window. Every number on the Journal's 💰 YOUR STAND page
 * is derived from the recorded transactions: bought ==
 * sold + spoiled + on hand, always.
 *
 * All state rides the established life/lifeText maps —
 * no new save keys; a pre-0.10.0e save simply has no stand.
 */
object Stand {

    // ---------------- the setup ----------------

    /** City Hall's one-time blessing, in capsule coins. */
    const val PERMIT_COST = 10
    /** The kit itself: table, crate, hand-painted sign. */
    const val KIT_COST = 15
    /** Moving the pitch later costs a fresh pitch fee —
     *  allowed, stated, and booked as a cost like the rest. */
    const val MOVE_COST = 5

    /** The pitches: outdoor spots where townsfolk genuinely
     *  pass on their real agendas — the strip, the park,
     *  the pond path, the bus stop. */
    val STAND_LOCS: List<String> =
        listOf("MainStreet", "KidsPark", "StarksPond", "BusStop")

    /** The stand's hours: set up after breakfast, packed
     *  away before dark. Sales only happen inside them. */
    const val OPEN_FROM = 8 * 60
    const val OPEN_TO = 20 * 60

    // ---------------- stock & prices ----------------

    /** One stockable item: where it comes from, what the
     *  town's shops charge for it (the pantry price), and
     *  how many days it keeps before it spoils. */
    data class StockDef(
        val itemId: String, val shopPrice: Int,
        val shelfDays: Int, val supplier: String)

    /** The stock list. Prices are Fair.BOOTH's — the
     *  town's own pantry prices, charged by its real shops
     *  at restock. Shelf lives are stated per item: fresh
     *  lemonade turns fast, a thermos of cocoa mix holds
     *  a few days, sealed snacks keep the week. */
    val STOCK: List<StockDef> = listOf(
        StockDef("lemonade", 1, 2, "Tweek Bros."),
        StockDef("hot_chocolate", 1, 3, "Tweek Bros."),
        StockDef("cheesy_poof", 2, 6, "the Mall snack shop"),
        StockDef("snack_cake", 2, 6, "the Mall snack shop"))

    /** v0.11.0b: garden produce — the crate's second
     *  source. Not orderable (no shop sells it; you GROW
     *  it): the shopPrice here is the stated REFERENCE
     *  price the player band is measured against, and the
     *  supplier line names the only supplier there is.
     *  Shelf lives are a garden's: potatoes keep, tomatoes
     *  don't. */
    val PRODUCE: List<StockDef> = listOf(
        StockDef("pumpkin", 3, 10, "your garden"),
        StockDef("corn", 1, 4, "your garden"),
        StockDef("sunflower", 2, 6, "your garden"),
        StockDef("tomato", 1, 3, "your garden"),
        StockDef("potato", 1, 12, "your garden"))

    /** Everything the crate can hold: shop stock, then
     *  garden produce. STOCK stays the shops' list (the
     *  paper's classifieds print it, the order dialog
     *  reads it); ALL_DEFS is the crate's universe. */
    val ALL_DEFS: List<StockDef> = STOCK + PRODUCE

    fun isProduce(itemId: String): Boolean =
        PRODUCE.any { it.itemId == itemId }

    fun stockDef(itemId: String): StockDef? =
        ALL_DEFS.firstOrNull { it.itemId == itemId }

    /** The price band: never below shop price (no dumping
     *  the town's own goods back at it) and never above
     *  PRICE_MAX_MULT times shop (the gouge ceiling). */
    const val PRICE_MAX_MULT = 3

    fun minPrice(itemId: String): Int =
        stockDef(itemId)?.shopPrice ?: 0

    fun maxPrice(itemId: String): Int =
        (stockDef(itemId)?.shopPrice ?: 0) * PRICE_MAX_MULT

    /** The shops will only sell you so much of a thing in
     *  a day — a kid restocking is not a wholesale account.
     *  This cap is one leg of the economy bound. */
    const val ORDER_CAP = 12

    // ---------------- the sale model ----------------

    /** Base chance per present person per 15-minute tick,
     *  before price, weather fit, loves and friendship
     *  have their say. */
    const val BASE_CHANCE = 0.30

    /** A crewmate works the stand at this fraction of your
     *  own selling — they're good, they're not you. */
    const val CREW_EFFICIENCY = 0.7

    /** Nobody buys more than this many stand snacks a
     *  day, however charming the pitch. */
    const val BUY_CAP_DAY = 2

    /** A staffed day earns the staffer this much
     *  friendship at the wrap — the crew works for the
     *  crew; there is no coin cut. */
    const val STAFF_FRIENDSHIP = 1

    // ---------------- the review ----------------

    /** The rival's crew reviews a stand after this many
     *  staffed days, then again every REVIEW_EVERY more. */
    const val REVIEW_DAYS = 5
    const val REVIEW_EVERY = 10

    /** An honestly-good review lifts foot traffic by this
     *  factor (more of the passing townsfolk stop) for
     *  this many days. The lift derives from the books'
     *  verdict, never from the review's tone alone. */
    const val REVIEW_LIFT = 1.5
    const val REVIEW_LIFT_DAYS = 7

    /** The thread the review leaves in conversation. */
    const val TOPIC = "your stand"

    // ---------------- recorded state ----------------
    // life/lifeText keys, all prefixed "stand". Per-day
    // stamps are single values carrying their day, in the
    // crew's stamped style — nothing accumulates.

    private const val K_LOC = "standLoc"
    private const val K_SETUP_DAY = "standSetupDay"
    private const val K_CRATE = "standCrate"
    private const val K_OPEN_DAY = "standOpenDay"
    private const val K_STAFF = "standStaff"
    private const val K_STAFF_FROM = "standStaffFrom"
    private const val K_WORKED = "standWorked"
    private const val K_FEES = "standFees"
    private const val K_PERMIT_PAID = "standPermitPaid"
    private const val K_STOCK_COST = "standStockCost"
    private const val K_TAKINGS = "standTakings"
    private const val K_DAYS_OPEN = "standDaysOpen"
    private const val K_STAFFED_DAYS = "standStaffedDays"
    private const val K_NEXT_REVIEW = "standNextReview"
    private const val K_LIFT_TO = "standLiftTo"
    private const val K_REVIEW = "standReview"
    private const val K_REVIEWER = "standReviewer"
    private const val K_REVIEW_DAY = "standReviewDay"
    private const val K_VERDICT = "standVerdict"

    private fun kPrice(itemId: String) = "standPrice:$itemId"
    private fun kBought(itemId: String) = "standBought:$itemId"
    private fun kGrown(itemId: String) = "standGrown:$itemId"
    private fun kSold(itemId: String) = "standSold:$itemId"
    private fun kSpoiled(itemId: String) = "standSpoiled:$itemId"
    private fun kOrdered(itemId: String) = "standOrd:$itemId"
    private fun kBuyCap(name: String) = "standBuy:$name"

    fun hasStand(state: GameState): Boolean =
        state.lifeText[K_LOC] != null

    fun locOf(state: GameState): String? =
        state.lifeText[K_LOC]

    private fun nowAbsMin(state: GameState): Int =
        state.absDay * 1440 + state.timeMin

    private fun playerName(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.name
            ?: "New Kid"

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    private fun spendCoins(state: GameState, n: Int): Boolean {
        if (Minigames.capsuleCoins(state) < n) return false
        val left = Minigames.capsuleCoins(state) - n
        if (left <= 0) state.inventory.remove(Minigames.COIN_ITEM)
        else state.inventory[Minigames.COIN_ITEM] = left
        return true
    }

    // ---------------- the crate ----------------

    /** One crate batch: bought on a day, spoils after its
     *  shelf life. Batches are sold oldest-first, so the
     *  age on the shelf is always honest. */
    data class Batch(val itemId: String, val day: Int, val count: Int)

    fun crate(state: GameState): List<Batch> {
        val raw = state.lifeText[K_CRATE] ?: return emptyList()
        return raw.split(";").mapNotNull { part ->
            val bits = part.split(":")
            if (bits.size != 3) null
            else Batch(bits[0], bits[1].toIntOrNull() ?: 0,
                bits[2].toIntOrNull() ?: 0)
        }.filter { it.count > 0 }
    }

    private fun putCrate(state: GameState, batches: List<Batch>) {
        val live = batches.filter { it.count > 0 }
        if (live.isEmpty()) state.lifeText.remove(K_CRATE)
        else state.lifeText[K_CRATE] = live.joinToString(";") {
            "${it.itemId}:${it.day}:${it.count}"
        }
    }

    fun onHand(state: GameState, itemId: String): Int =
        crate(state).filter { it.itemId == itemId }
            .sumOf { it.count }

    fun crateTotal(state: GameState): Int =
        crate(state).sumOf { it.count }

    /** Days of shelf left for the oldest batch of an item
     *  (negative once spoiled — rollover clears those). */
    fun oldestShelfLeft(state: GameState, itemId: String): Int? {
        val def = stockDef(itemId) ?: return null
        val oldest = crate(state).filter { it.itemId == itemId }
            .minByOrNull { it.day } ?: return null
        return def.shelfDays - (state.absDay - oldest.day)
    }

    // ---------------- setup & moving ----------------

    /** File the permit, buy the kit, set up at [locId].
     *  Returns the welcome line, or a plain reason why not. */
    fun setup(state: GameState, locId: String): String {
        if (hasStand(state)) return "The stand's already up at " +
            "${LOCATIONS[locOf(state)]?.name}. Moving it is the " +
            "pitch fee — setting up twice isn't a thing."
        if (locId !in STAND_LOCS) return "No pitch there — the " +
            "permit covers ${STAND_LOCS.joinToString(", ") {
                LOCATIONS[it]?.name ?: it }}. Outdoor spots, " +
            "where the town actually walks."
        val total = PERMIT_COST + KIT_COST
        if (!spendCoins(state, total)) return "The permit is " +
            "$PERMIT_COST 🪙 at City Hall and the kit is " +
            "$KIT_COST 🪙 — $total 🪙 all in, and the capsule " +
            "coins aren't there yet. The town pays its games " +
            "and its jobs; the stand waits."
        state.lifeText[K_LOC] = locId
        state.life[K_SETUP_DAY] = state.absDay
        state.life[K_PERMIT_PAID] = total
        state.life[K_FEES] = (state.life[K_FEES] ?: 0) + total
        for (def in STOCK) {
            if (state.life[kPrice(def.itemId)] == null)
                state.life[kPrice(def.itemId)] = def.shopPrice + 1
        }
        note(state, "💰 The new kid's stand is UP at " +
            "${LOCATIONS[locId]?.name} — City Hall permit " +
            "filed (Mayor McDaniels signed it herself), kit " +
            "and all. The town is about to have opinions.")
        return "Permit filed at City Hall ($PERMIT_COST 🪙 — " +
            "the Mayor signed it with a flourish), kit bought " +
            "($KIT_COST 🪙), stand up at " +
            "${LOCATIONS[locId]?.name}. Order stock, set " +
            "your prices, and open up — an unstaffed stand " +
            "sells nothing, and this one says so on the sign."
    }

    /** Move the pitch for the stated fee. Stock travels
     *  with the crate; the books carry the fee. */
    fun move(state: GameState, locId: String): String {
        if (!hasStand(state)) return "There's no stand to " +
            "move yet."
        if (locId == locOf(state)) return "It's already there."
        if (locId !in STAND_LOCS) return "No pitch there — " +
            "the permit covers the outdoor spots where the " +
            "town actually walks."
        if (!spendCoins(state, MOVE_COST)) return "Moving " +
            "costs the $MOVE_COST 🪙 pitch fee, and the " +
            "coins aren't there."
        state.lifeText[K_LOC] = locId
        state.life[K_FEES] = (state.life[K_FEES] ?: 0) + MOVE_COST
        closeStand(state)
        note(state, "💰 The stand moved to " +
            "${LOCATIONS[locId]?.name} (pitch fee " +
            "$MOVE_COST 🪙). Same crate, new sidewalk.")
        return "Moved — pitch fee $MOVE_COST 🪙, crate and " +
            "all, now at ${LOCATIONS[locId]?.name}. It opens " +
            "fresh tomorrow's-hours; a closed stand sells " +
            "nothing until you open it."
    }

    // ---------------- prices ----------------

    fun priceOf(state: GameState, itemId: String): Int {
        val def = stockDef(itemId) ?: return 0
        val p = state.life[kPrice(itemId)] ?: (def.shopPrice + 1)
        return p.coerceIn(minPrice(itemId), maxPrice(itemId))
    }

    /** Set a price inside the band; outside it the stand
     *  refuses, plainly. Returns true when the price stuck. */
    fun setPrice(state: GameState, itemId: String,
                 price: Int): Boolean {
        if (stockDef(itemId) == null) return false
        if (price !in minPrice(itemId)..maxPrice(itemId))
            return false
        state.life[kPrice(itemId)] = price
        return true
    }

    // ---------------- restocking ----------------

    /** Per-day stamps ride one Int key each (life is an
     *  Int map): day * 100 + count, decoded against today. */
    private fun dayStamped(state: GameState, key: String): Int {
        val v = state.life[key] ?: return 0
        return if (v / 100 == state.absDay) v % 100 else 0
    }

    fun orderedToday(state: GameState, itemId: String): Int =
        dayStamped(state, kOrdered(itemId))

    /** Buy stock from the town's real shops at their real
     *  (pantry) prices, into the crate — never into your
     *  pockets, so crate stock can never be laundered into
     *  gifts or resale elsewhere. Returns a line, or a
     *  plain reason why not. */
    fun order(state: GameState, itemId: String, n: Int): String {
        val def = stockDef(itemId)
            ?: return "The stand doesn't stock that."
        // v0.11.0b: produce is never ordered — no shop
        // sells it. It enters the crate by transfer from
        // the garden's harvest (stockFromGarden), against
        // the garden's provenance count.
        if (isProduce(itemId)) return "You don't order " +
            "those — you GROW them. Harvest at the plot, " +
            "then stock the crate from the garden's picking."
        if (!hasStand(state)) return "No stand, no stock."
        if (n <= 0) return "Order at least one."
        if (orderedToday(state, itemId) + n > ORDER_CAP)
            return "${def.supplier} will sell you $ORDER_CAP " +
                "a day, total — you've had " +
                "${orderedToday(state, itemId)}. Wholesale " +
                "accounts are for grown-ups with warehouses."
        val cost = def.shopPrice * n
        val item = itemById(itemId)
        if (!spendCoins(state, cost)) return "That's $cost 🪙 " +
            "at ${def.supplier}'s price, and the coins " +
            "aren't there."
        val batches = crate(state).toMutableList()
        val i = batches.indexOfFirst {
            it.itemId == itemId && it.day == state.absDay }
        if (i >= 0) batches[i] =
            batches[i].copy(count = batches[i].count + n)
        else batches.add(Batch(itemId, state.absDay, n))
        putCrate(state, batches)
        state.life[kOrdered(itemId)] =
            state.absDay * 100 + orderedToday(state, itemId) + n
        state.life[kBought(itemId)] =
            (state.life[kBought(itemId)] ?: 0) + n
        state.life[K_STOCK_COST] =
            (state.life[K_STOCK_COST] ?: 0) + cost
        return "${item?.icon ?: "📦"} $n × ${item?.name} into " +
            "the crate from ${def.supplier} at " +
            "${def.shopPrice} 🪙 each ($cost 🪙). Keeps " +
            "${def.shelfDays} days — the books are watching."
    }

    /** v0.11.0b: garden → crate. A transfer of REAL
     *  items: [n] of [itemId] leave the player's pack and
     *  enter the crate as a batch dated today — but only
     *  as many as the garden provably grew (Garden's
     *  provenance count), so a tomato found in a dumpster
     *  is lunch, never inventory. No coins move: you can't
     *  buy from yourself. The books carry the grown tally,
     *  and the identity holds per item:
     *  grown == sold + spoiled + onHand. */
    fun stockFromGarden(state: GameState, itemId: String,
                        n: Int): String {
        val def = stockDef(itemId)
        if (def == null || !isProduce(itemId))
            return "The crate takes garden produce only " +
                "this way — shop stock is ordered."
        if (!hasStand(state)) return "No stand, no crate."
        if (n <= 0) return "Stock at least one."
        val grown = Garden.grownAvailable(state, itemId)
        val held = state.inventory[itemId] ?: 0
        val move = minOf(n, grown, held)
        val item = itemById(itemId)
        if (move <= 0) return if (held > 0)
            "Those ${item?.name?.lowercase() ?: itemId} " +
                "didn't come from your garden — the crate " +
                "only takes homegrown, and the garden's " +
                "record is the receipt."
        else "Nothing to stock — harvest at the plot " +
            "first, then bring the picking here."
        if (held - move <= 0) state.inventory.remove(itemId)
        else state.inventory[itemId] = held - move
        Garden.noteStocked(state, itemId, move)
        val batches = crate(state).toMutableList()
        val i = batches.indexOfFirst {
            it.itemId == itemId && it.day == state.absDay }
        if (i >= 0) batches[i] =
            batches[i].copy(count = batches[i].count + move)
        else batches.add(Batch(itemId, state.absDay, move))
        putCrate(state, batches)
        state.life[kGrown(itemId)] =
            (state.life[kGrown(itemId)] ?: 0) + move
        return "${item?.icon ?: "🌱"} $move × ${item?.name} " +
            "into the crate from your garden — priced " +
            "against the ${def.shopPrice} 🪙 reference, " +
            "keeps ${def.shelfDays} days."
    }

    // ---------------- opening & staffing ----------------

    fun isOpen(state: GameState): Boolean =
        hasStand(state) &&
            (state.life[K_OPEN_DAY] ?: -1) == state.absDay

    /** Who the stand is assigned to today: "you", a crew
     *  member's name, or null when closed. */
    fun assignedStaff(state: GameState): String? =
        if (isOpen(state)) state.lifeText[K_STAFF] else null

    fun openDays(state: GameState): Int =
        state.life[K_DAYS_OPEN] ?: 0

    fun staffedDays(state: GameState): Int =
        state.life[K_STAFFED_DAYS] ?: 0

    /** Open the stand for today. [staff] null = you work
     *  it yourself (you sell only while standing at it);
     *  a crew member's name = they walk there on the real
     *  Walk pricing and work it under the crew's occupied
     *  rules. Returns the line, or a plain reason why not. */
    fun openStand(state: GameState, staff: String?): String {
        if (!hasStand(state)) return "No stand to open."
        if (isOpen(state)) return "It's already open today — " +
            "close up first if the staffing changes."
        if (state.timeMin >= OPEN_TO) return "Too late to " +
            "open — the stand's hours end at 8 PM, and a " +
            "stand opened into the dark sells to nobody."
        val loc = locOf(state)!!
        if (staff != null) {
            if (!Crew.isMember(state, staff))
                return "$staff isn't in your crew — the stand " +
                    "is staffed by your crew or by you."
            val busy = Crew.busyReason(state, staff)
            if (busy != null) return "$staff can't — their " +
                "life has them right now ($busy). The crew's " +
                    "rules are the crew's rules."
            val from = state.agents[staff]?.location
                ?: return "$staff isn't anywhere to walk from."
            val mins = Walk.routeMinutes(state, from, loc)
            state.life[K_STAFF_FROM] =
                nowAbsMin(state) + mins
            state.lifeText[K_STAFF] = staff
        } else {
            state.lifeText[K_STAFF] = "you"
            state.life.remove(K_STAFF_FROM)
        }
        state.life[K_OPEN_DAY] = state.absDay
        state.life[K_DAYS_OPEN] = openDays(state) + 1
        val where = LOCATIONS[loc]?.name ?: loc
        return if (staff == null)
            "Open at $where — you're working it. It sells " +
                "while YOU stand at it; walk away and it " +
                "sells nothing, same as any honest stand."
        else
            "Open at $where — $staff is walking over on " +
                "the real route and works it at " +
                "${(CREW_EFFICIENCY * 100).toInt()}% of your " +
                "selling, for friendship, no cut. If their " +
                "life calls them away, the stand just stops " +
                "selling — it says so on the page."
    }

    /** Close up. The assignment ends; the crate keeps. */
    fun closeStand(state: GameState) {
        state.life.remove(K_OPEN_DAY)
        state.lifeText.remove(K_STAFF)
        state.life.remove(K_STAFF_FROM)
    }

    fun closeStandLine(state: GameState): String {
        closeStand(state)
        return "Closed up. The crate's packed, the sign's " +
            "down, the books keep what the day earned."
    }

    /** The agenda claim — seated in Engine's stack after
     *  the school play's claim and before your crew's own
     *  hook, self-guarded by the crew's occupied oracle:
     *  if any bigger claim holds the staffer (Crew's
     *  busyReason says so), this returns null and their
     *  life wins. Your crew's word, later in the stack,
     *  still outranks the shift. */
    fun gatheringBlock(state: GameState, name: String,
                       timeMin: Int): AgendaBlock? {
        if (!isOpen(state)) return null
        if (state.lifeText[K_STAFF] != name) return null
        if (timeMin !in OPEN_FROM until OPEN_TO) return null
        if (Crew.busyReason(state, name) != null) return null
        val loc = locOf(state) ?: return null
        return AgendaBlock(loc,
            "Working ${playerName(state)}'s stand — snacks, " +
                "drinks, and total professionalism.")
    }

    /** Who is actually selling right now — presence is the
     *  claim. Null when nobody is: closed, out of hours,
     *  you walked away, or your staffer hasn't arrived /
     *  got called away. The stand sells only when this
     *  returns a name. */
    fun staffedBy(state: GameState): String? {
        if (!isOpen(state)) return null
        if (state.timeMin !in OPEN_FROM until OPEN_TO)
            return null
        val loc = locOf(state) ?: return null
        val staff = state.lifeText[K_STAFF] ?: return null
        if (staff == "you") {
            val me = state.agents.values
                .firstOrNull { it.isPlayer } ?: return null
            return if (!me.asleep && me.location == loc &&
                me.waypoints.isEmpty()) "you" else null
        }
        if (!Crew.isMember(state, staff)) return null
        if (Crew.busyReason(state, staff) != null) return null
        if (nowAbsMin(state) <
            (state.life[K_STAFF_FROM] ?: Int.MAX_VALUE))
            return null
        val a = state.agents[staff] ?: return null
        return if (!a.asleep && a.location == loc &&
            a.waypoints.isEmpty()) staff else null
    }

    /** The plain words for why the stand isn't selling,
     *  for the page and the dialog — an unstaffed stand
     *  says so, never just silently sits there. */
    fun closedReason(state: GameState): String = when {
        !hasStand(state) -> "No stand yet."
        !isOpen(state) -> "Closed — an unstaffed stand " +
            "sells nothing. Open it and stand at it (or " +
            "send a crewmate) and the town can buy."
        state.timeMin !in OPEN_FROM until OPEN_TO ->
            "Outside the stand's hours (8 AM – 8 PM)."
        assignedStaff(state) == "you" ->
            "Open, but you're not at it — it sells while " +
                "YOU stand at the pitch. Walk away and it " +
                "sells nothing."
        else -> "Open, but ${assignedStaff(state)} isn't on " +
            "the pitch right now — still walking over, or " +
            "their own life called them away. It sells " +
            "when they're standing at it."
    }

    // ---------------- the purchase model ----------------

    /** How much the day itself wants this item: cold
     *  drinks on bright days, hot chocolate in the snow —
     *  the same (weather, season) read SkyState takes of
     *  the town. Around 1.0, stated per weather. */
    fun weatherFit(itemId: String, weatherId: String,
                   season: Season): Double = when (itemId) {
        "lemonade" -> when (weatherId) {
            "sunny" -> if (season == Season.SUMMER) 1.6 else 1.4
            "cloudy" -> 1.0
            "wind" -> 0.9
            "fog" -> 0.8
            "rain" -> 0.6
            "light_snow" -> 0.55
            else -> 0.45
        }
        "hot_chocolate" -> when (weatherId) {
            "heavy_snow", "blizzard" -> 1.6
            "light_snow" -> 1.5
            "rain" -> 1.2
            "fog" -> 1.2
            "cloudy" -> 1.1
            "wind" -> 1.1
            "sunny" -> if (season == Season.SUMMER) 0.55 else 0.85
            else -> 1.0
        }
        "snack_cake" -> when (weatherId) {
            "heavy_snow", "blizzard", "light_snow" -> 1.1
            else -> 1.0
        }
        "cheesy_poof" -> when (weatherId) {
            "sunny" -> 1.1
            else -> 1.0
        }
        // v0.11.0b: garden produce sells like what it is —
        // garden food. Sun and growing weather move it;
        // a pumpkin is a fall purchase whatever the sky.
        "corn" -> when (weatherId) {
            "sunny" -> if (season == Season.SUMMER) 1.3 else 1.2
            "rain" -> 0.7
            else -> 1.0
        }
        "tomato" -> when (weatherId) {
            "sunny" -> 1.25
            "rain" -> 0.7
            else -> 1.0
        }
        "sunflower" -> when (weatherId) {
            "sunny" -> 1.3
            "rain", "fog" -> 0.8
            else -> 1.0
        }
        "pumpkin" -> when (season) {
            Season.FALL -> 1.25
            else -> 1.0
        }
        "potato" -> 1.0
        else -> 1.0
    }

    /** Fair sells, gouging mostly doesn't: the price
     *  factor off the ratio to the shop price. 1× → 1.0,
     *  2× → 0.62, 3× (the band's ceiling) → 0.24. */
    fun priceFactor(ratio: Double): Double =
        (1.0 - 0.38 * (ratio - 1.0)).coerceAtLeast(0.05)

    /** Does this person love this item — the items' own
     *  authored read, the same one gifts are judged by. */
    fun loves(state: GameState, npcName: String,
              itemId: String): Boolean {
        val item = itemById(itemId) ?: return false
        return giftValue(item, npcName, emptyList()).second
    }

    /** The review's foot-traffic lift, live right now. */
    fun liftActive(state: GameState): Boolean =
        state.lifeText[K_VERDICT] == "GOOD" &&
            state.absDay <= (state.life[K_LIFT_TO] ?: -1)

    fun trafficFactor(state: GameState): Double =
        if (liftActive(state)) REVIEW_LIFT else 1.0

    /** The full purchase chance for one present person,
     *  one tick, one item — every factor a real read:
     *  price vs shop, weather/season fit, their authored
     *  loves, your friendship, the review lift, and who's
     *  working the stand. */
    fun purchaseChance(state: GameState, npcName: String,
                       itemId: String,
                       sellerIsCrew: Boolean): Double {
        val def = stockDef(itemId) ?: return 0.0
        val ratio = priceOf(state, itemId)
            .toDouble() / def.shopPrice
        val fit = weatherFit(itemId, state.weatherId,
            seasonFor(state.gameMonth))
        val love = if (loves(state, npcName, itemId))
            1.5 else 1.0
        val friend = 1.0 + (state.friendship[npcName] ?: 0)
            .coerceIn(0, 100) / 200.0
        val eff = if (sellerIsCrew) CREW_EFFICIENCY else 1.0
        // v0.11.0e: the winter carnival's kettle rush —
        // hot chocolate at a pond pitch in carnival week
        // buys at Carnival.COCOA_RUSH × (Carnival owns the
        // rule; every other factor is unchanged).
        return BASE_CHANCE * priceFactor(ratio) * fit *
            love * friend * trafficFactor(state) * eff *
            Carnival.cocoaRush(state, itemId)
    }

    /** The item a present person would reach for: the
     *  best weather fit among what's stocked, weighted by
     *  love — or null when the crate's empty for them. */
    fun pickItemFor(state: GameState,
                    npcName: String): String? =
        ALL_DEFS.map { it.itemId }
            .filter { onHand(state, it) > 0 }
            .maxByOrNull { itemId ->
                weatherFit(itemId, state.weatherId,
                    seasonFor(state.gameMonth)) *
                    (if (loves(state, npcName, itemId))
                        1.5 else 1.0)
            }

    private fun boughtToday(state: GameState,
                            name: String): Int =
        dayStamped(state, kBuyCap(name))

    private val LOVE_LINES = listOf(
        "Okay, the {item} from the stand? Actually good. " +
            "Write that down.",
        "A {item} at the pitch is the best part of my " +
            "route now. Don't let it go to your head.",
        "I dream about this {item}. The stand dream. " +
            "Again.",
        "You sell {item} now?? This changes my whole " +
            "afternoon route.")

    /** One sale attempt for one present person. On a sale:
     *  the oldest batch decrements, real coins move at the
     *  set price, the books record it, and a regular who
     *  loves the item may say so on the town feed. */
    fun trySale(state: GameState, npcName: String,
                rnd: Random, sellerIsCrew: Boolean): Boolean {
        if (boughtToday(state, npcName) >= BUY_CAP_DAY)
            return false
        val itemId = pickItemFor(state, npcName)
            ?: return false
        val chance = purchaseChance(state, npcName, itemId,
            sellerIsCrew)
        if (rnd.nextDouble() >= chance) return false
        // the sale: oldest batch first, always
        val batches = crate(state).toMutableList()
        val i = batches.indexOfFirst {
            it.itemId == itemId && it.count > 0 }
        if (i < 0) return false
        batches[i] = batches[i].copy(count =
            batches[i].count - 1)
        putCrate(state, batches)
        val price = priceOf(state, itemId)
        Minigames.awardCoins(state, price)
        state.life[K_TAKINGS] =
            (state.life[K_TAKINGS] ?: 0) + price
        state.life[kSold(itemId)] =
            (state.life[kSold(itemId)] ?: 0) + 1
        state.life[kBuyCap(npcName)] =
            state.absDay * 100 + boughtToday(state, npcName) + 1
        val item = itemById(itemId)
        if (loves(state, npcName, itemId) &&
            rnd.nextDouble() < 0.5) {
            val line = LOVE_LINES[
                (state.absDay + npcName.hashCode())
                    .mod(LOVE_LINES.size)]
                .replace("{item}", item?.name ?: itemId)
            note(state, "🧾 $npcName, at the stand: " +
                "“$line”")
        }
        return true
    }

    // ---------------- the tick ----------------

    /** Engine.advanceTime's hook (after the crew's own
     *  tick, so a staffer's arrangements settle first):
     *  if a seller is standing at the pitch, the townsfolk
     *  physically present get their honest chance to buy.
     *  Nobody present, nobody buys — no conjured crowds. */
    fun tick(state: GameState) {
        val seller = staffedBy(state) ?: return
        state.life[K_WORKED] = state.absDay
        val loc = locOf(state) ?: return
        val sellerIsCrew = seller != "you"
        val present = state.agents.values.filter { a ->
            !a.isPlayer && !a.asleep &&
                a.location == loc && a.waypoints.isEmpty() &&
                a.name != seller
        }
        for (a in present) {
            val rnd = Random(state.absDay * 1_000_003 +
                state.timeMin * 131 + a.name.hashCode())
            trySale(state, a.name, rnd, sellerIsCrew)
        }
    }

    // ---------------- the books ----------------

    /** The stand's real accounts, derived from the
     *  recorded transactions. bought == sold + spoiled +
     *  onHand holds per item and in total — and since
     *  v0.11.0b the garden's produce keeps its own column:
     *  grown == sold + spoiled + onHand per produce item.
     *  The suite recomputes both from the crate and the
     *  counters. */
    data class Books(
        val daysOpen: Int, val staffedDays: Int,
        val bought: Map<String, Int>,
        val sold: Map<String, Int>,
        val spoiled: Map<String, Int>,
        val onHand: Map<String, Int>,
        val takings: Int, val stockCost: Int, val fees: Int,
        val spoilValue: Int,
        // v0.11.0b: garden-grown units transferred in —
        // last, defaulted, so the books' old shape is a
        // prefix of the new one.
        val grown: Map<String, Int> = emptyMap()) {
        val totalBought get() = bought.values.sum()
        val totalGrown get() = grown.values.sum()
        val totalSold get() = sold.values.sum()
        val totalSpoiled get() = spoiled.values.sum()
        val totalOnHand get() = onHand.values.sum()
        val costs get() = stockCost + fees
        val profit get() = takings - costs
    }

    fun books(state: GameState): Books {
        val bought = ALL_DEFS.associate { it.itemId to
            (state.life[kBought(it.itemId)] ?: 0) }
        val grown = PRODUCE.associate { it.itemId to
            (state.life[kGrown(it.itemId)] ?: 0) }
        val sold = ALL_DEFS.associate { it.itemId to
            (state.life[kSold(it.itemId)] ?: 0) }
        val spoiled = ALL_DEFS.associate { it.itemId to
            (state.life[kSpoiled(it.itemId)] ?: 0) }
        val onHand = ALL_DEFS.associate { it.itemId to
            onHand(state, it.itemId) }
        val spoilValue = ALL_DEFS.sumOf {
            (state.life[kSpoiled(it.itemId)] ?: 0) *
                it.shopPrice }
        return Books(
            daysOpen = openDays(state),
            staffedDays = staffedDays(state),
            bought = bought, sold = sold,
            spoiled = spoiled,
            onHand = onHand,
            takings = state.life[K_TAKINGS] ?: 0,
            stockCost = state.life[K_STOCK_COST] ?: 0,
            fees = state.life[K_FEES] ?: 0,
            spoilValue = spoilValue,
            grown = grown)
    }

    // ---------------- the review ----------------

    /** The facts a review is allowed to use — derived
     *  from the books, nothing else. */
    data class ReviewStats(
        val markupPct: Int, val sellThroughPct: Int,
        val spoilPct: Int, val staffedDays: Int,
        val profit: Int)

    fun reviewStats(state: GameState): ReviewStats {
        val b = books(state)
        // v0.11.0b: the mix runs over ALL_DEFS — produce
        // sold counts at its reference price, like shop
        // stock counts at shop price. With no produce
        // traded the numbers are the v0.10.0e numbers.
        val soldMix = ALL_DEFS.sumOf {
            (b.sold[it.itemId] ?: 0) * it.shopPrice }
        val stockedTotal = b.totalBought + b.totalGrown
        val markup = when {
            b.totalSold > 0 -> {
                val avgSell = b.takings.toDouble() /
                    b.totalSold
                val avgShop = soldMix.toDouble() /
                    b.totalSold
                ((avgSell / avgShop - 1.0) * 100)
                    .toInt()
            }
            b.totalOnHand > 0 -> {
                val stocked = ALL_DEFS
                    .filter { (b.onHand[it.itemId] ?: 0) > 0 }
                if (stocked.isEmpty()) 0
                else (stocked.map {
                    priceOf(state, it.itemId).toDouble() /
                        it.shopPrice }.average()
                    .minus(1.0) * 100).toInt()
            }
            else -> 0
        }
        val sell = if (stockedTotal == 0) 0
            else b.totalSold * 100 / stockedTotal
        val spoil = if (stockedTotal == 0) 0
            else b.totalSpoiled * 100 / stockedTotal
        return ReviewStats(markup, sell, spoil,
            b.staffedDays, b.profit)
    }

    /** The verdict the books earn: kind or cutting as
     *  the numbers say, never as the tone wishes. */
    fun verdictOf(stats: ReviewStats): String = when {
        stats.sellThroughPct >= 60 && stats.spoilPct <= 25 ->
            "GOOD"
        stats.sellThroughPct < 30 || stats.spoilPct > 45 ->
            "ROUGH"
        else -> "MIXED"
    }

    private val REVIEW_GOOD = listOf(
        "OFFICIAL CREW REVIEW of the new kid's stand: " +
            "{days} staffed days, {sell}% of stock actually " +
            "sold, prices {markup}% over shop and worth it, " +
            "only {spoil}% wasted. Verdict: legit. We'd " +
            "queue. Tell everyone.",
        "We audited the stand. {sell}% sell-through, " +
            "{spoil}% spoilage, {markup}% markup — those " +
            "are GROWN-UP numbers. The stand is good. We " +
            "hate admitting it. It's good.")
    private val REVIEW_MIXED = listOf(
        "OFFICIAL CREW REVIEW of the new kid's stand: " +
            "{days} staffed days, {sell}% sold, {spoil}% " +
            "spoiled, {markup}% over shop. Verdict: a real " +
            "stand having a real season. Could be great. " +
            "Isn't yet.",
        "The stand, reviewed: it sells ({sell}%), it " +
            "spoils ({spoil}%), it charges {markup}% over " +
            "the shops. It's FINE. Fine is a review. " +
            "You're welcome.")
    private val REVIEW_ROUGH = listOf(
        "OFFICIAL CREW REVIEW of the new kid's stand: " +
            "{sell}% of the stock sold. {spoil}% of it " +
            "SPOILED. At {markup}% over shop prices. " +
            "Verdict: the raccoons eat better than the " +
            "customers. Tragic. Fixable, but tragic.",
        "We reviewed the stand so you don't have to: " +
            "{days} staffed days to sell {sell}% and " +
            "spoil {spoil}%. The sign is nice. The sign " +
            "is doing a LOT of the work here.")

    fun reviewLine(stats: ReviewStats, verdict: String,
                   reviewNo: Int): String {
        val bank = when (verdict) {
            "GOOD" -> REVIEW_GOOD
            "ROUGH" -> REVIEW_ROUGH
            else -> REVIEW_MIXED
        }
        return bank[reviewNo.mod(bank.size)]
            .replace("{days}", stats.staffedDays.toString())
            .replace("{sell}", stats.sellThroughPct.toString())
            .replace("{spoil}", stats.spoilPct.toString())
            .replace("{markup}", stats.markupPct.toString())
    }

    /** The current review's quoted line, if one stands. */
    fun reviewText(state: GameState): String? =
        state.lifeText[K_REVIEW]

    fun reviewBy(state: GameState): String? =
        state.lifeText[K_REVIEWER]

    /** The standing review's verdict (GOOD / MIXED /
     *  ROUGH), as the books earned it, if one stands.
     *  (v0.12.0a: the paper's review tier reads it.) */
    fun reviewVerdict(state: GameState): String? =
        state.lifeText[K_VERDICT]

    /** The day the standing review posted, if one stands.
     *  (v0.12.0a: the paper's review tier reads it.) */
    fun reviewDay(state: GameState): Int? =
        state.life[K_REVIEW_DAY]

    private fun plantTopic(state: GameState, name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** Post the review if it's due: enough staffed days,
     *  and the rival's crew exists to write it. Derives
     *  everything from the books; a GOOD verdict opens
     *  the traffic-lift window. Returns true if it posted. */
    fun maybeReview(state: GameState): Boolean {
        if (!hasStand(state)) return false
        val due = state.life[K_NEXT_REVIEW] ?: REVIEW_DAYS
        if (staffedDays(state) < due) return false
        val crew = RivalCrew.theirCrew(state)
        if (crew.isEmpty()) return false
        val stats = reviewStats(state)
        val verdict = verdictOf(stats)
        val reviewNo = state.life["standReviews"] ?: 0
        val line = reviewLine(stats, verdict, reviewNo)
        val speaker = crew.first()
        state.lifeText[K_REVIEW] = line
        state.lifeText[K_REVIEWER] = speaker
        state.lifeText[K_VERDICT] = verdict
        state.life[K_REVIEW_DAY] = state.absDay
        state.life["standReviews"] = reviewNo + 1
        state.life[K_NEXT_REVIEW] =
            staffedDays(state) + REVIEW_EVERY
        if (verdict == "GOOD")
            state.life[K_LIFT_TO] =
                state.absDay + REVIEW_LIFT_DAYS - 1
        else state.life.remove(K_LIFT_TO)
        for (n in crew + listOfNotNull(Rival.rivalOf(state)))
            plantTopic(state, n)
        note(state, "💰 $speaker and the rival's crew " +
            "posted a review of the new kid's stand — " +
            "it's on the Journal's 💰 YOUR STAND page, " +
            "numbers and all.")
        return true
    }

    // ---------------- the day's wrap ----------------

    /** Engine.onNewDay's hook, before the phone harvest:
     *  yesterday's unsold perishables spoil INTO the
     *  books, a staffed day counts (and pays its
     *  friendship), the review comes due, and the stand
     *  closes with the night. */
    fun dailyRollover(state: GameState) {
        if (!hasStand(state)) return
        // spoilage: age past the shelf life, written off
        // at cost in the books — never silently vanished.
        val batches = crate(state)
        val kept = mutableListOf<Batch>()
        var spoiledUnits = 0
        for (b in batches) {
            val def = stockDef(b.itemId) ?: continue
            if (state.absDay - b.day > def.shelfDays) {
                state.life[kSpoiled(b.itemId)] =
                    (state.life[kSpoiled(b.itemId)] ?: 0) +
                        b.count
                spoiledUnits += b.count
            } else kept.add(b)
        }
        if (spoiledUnits > 0) {
            putCrate(state, kept)
            note(state, "💰 $spoiledUnits stand snacks " +
                "spoiled overnight — written into the " +
                "books at cost, like an honest business.")
        }
        // a staffed day counts once, at the wrap
        if ((state.life[K_WORKED] ?: -1) == state.absDay - 1) {
            state.life[K_STAFFED_DAYS] = staffedDays(state) + 1
            val staff = state.lifeText[K_STAFF]
            if (staff != null && staff != "you" &&
                Crew.isMember(state, staff))
                Engine().relAdjust(state, staff,
                    STAFF_FRIENDSHIP)
        }
        state.life.remove(K_WORKED)
        closeStand(state)
        maybeReview(state)
    }

    // ---------------- the page ----------------

    /** Journal 💰 YOUR STAND: the pitch, the crate, the
     *  prices, the books, the review — every number the
     *  recorded transactions derive. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        if (!hasStand(state)) {
            out += "💰 YOUR STAND"
            out += "No stand yet — and yet the town walks " +
                "past the good pitches every day, thirsty."
            out += "The setup: a ONE-TIME City Hall permit " +
                "($PERMIT_COST 🪙 — the Mayor signs it " +
                "herself) plus the stand kit ($KIT_COST 🪙). " +
                "Pitches: ${STAND_LOCS.joinToString(", ") {
                    LOCATIONS[it]?.name ?: it }} — outdoor " +
                "spots where townsfolk genuinely pass."
            out += "Stock comes from the town's real shops " +
                "at their real prices; you set yours inside " +
                "the band (shop price to ${PRICE_MAX_MULT}×). " +
                "It sells only while staffed — by you " +
                "standing at it, or by a crewmate who walked " +
                "there. Unstaffed, it sells nothing, and " +
                "this page will say so."
            return out
        }
        val loc = locOf(state)!!
        val b = books(state)
        out += "💰 YOUR STAND — ${LOCATIONS[loc]?.name}"
        out += when {
            staffedBy(state) == "you" -> "Open now — YOU " +
                "are working it. It sells while you stand " +
                "at the pitch."
            staffedBy(state) != null -> "Open now — " +
                "${staffedBy(state)} is working it, at " +
                "${(CREW_EFFICIENCY * 100).toInt()}% of your " +
                "selling, for friendship, no cut."
            else -> closedReason(state)
        }
        out += "Days open: ${b.daysOpen} · staffed days: " +
            "${b.staffedDays}"
        // the crate, with its ages showing
        if (crateTotal(state) == 0)
            out += "The crate is empty — order stock from " +
                "the shops (their prices, your markup)."
        for (def in ALL_DEFS) {
            val n = onHand(state, def.itemId)
            if (n <= 0) continue
            val item = itemById(def.itemId)
            val left = oldestShelfLeft(state, def.itemId)
            // v0.11.0b: produce prices read against the
            // REFERENCE, not a shop price — no shop sells it.
            val vs = if (isProduce(def.itemId))
                "(garden reference ${def.shopPrice} 🪙)"
            else "(shop ${def.shopPrice} 🪙)"
            out += "${item?.icon ?: "📦"} ${item?.name} " +
                "×$n — sells at ${priceOf(state, def.itemId)} " +
                "🪙 $vs" +
                (if (left != null)
                    " · oldest keeps $left more day(s)"
                else "")
        }
        // the books
        out += "— THE BOOKS —"
        out += "Bought ${b.totalBought}" +
            (if (b.totalGrown > 0)
                " · grown ${b.totalGrown} (from your garden)"
             else "") +
            " · sold " +
            "${b.totalSold} · spoiled ${b.totalSpoiled} · " +
            "on hand ${b.totalOnHand}"
        out += "Takings: ${b.takings} 🪙"
        out += "Costs: ${b.costs} 🪙 (stock " +
            "${b.stockCost} 🪙 · permit, kit & pitch fees " +
            "${b.fees} 🪙)"
        if (b.totalSpoiled > 0)
            out += "Spoilage written off: ${b.spoilValue} " +
                "🪙 of stock — it's in the books, not " +
                "under the rug."
        out += if (b.profit >= 0)
            "Profit so far: ${b.profit} 🪙 — in the black."
        else
            "Loss so far: ${-b.profit} 🪙 — in the red, " +
                "and the books say so plainly."
        // the review
        val review = reviewText(state)
        if (review != null) {
            out += "— THE RIVAL CREW'S REVIEW " +
                "(${reviewBy(state)}, for the crew) —"
            out += "“$review”"
            if (liftActive(state))
                out += "The good word is working: foot " +
                    "traffic is up (×$REVIEW_LIFT) for " +
                    "${(state.life[K_LIFT_TO] ?: 0) -
                        state.absDay + 1} more day(s), the " +
                    "stated window for an honestly-good " +
                    "review."
            else if (state.lifeText[K_VERDICT] == "GOOD")
                out += "The review's traffic lift has run " +
                    "its stated days."
        } else {
            out += "No review yet — the rival's crew " +
                "reviews a stand after $REVIEW_DAYS staffed " +
                "days, off the real books: markup, " +
                "sell-through, spoilage."
        }
        return out
    }
}

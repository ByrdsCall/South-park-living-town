package com.hunt3r.southparktown

/**
 * 🧺 THE MALL COUNTERS (v0.12.0b, Queue #12 item 2).
 *
 * The walk-to buy surface the garden named as missing in
 * v0.11.0b ("no walk-to-a-shop-counter flow exists in
 * this game"): two counters inside the South Park Mall,
 * built in the Mall's own established counter pattern —
 * the Good Shelf's clothing rack, the Pocket Pals
 * machine and the transit kiosk already sell on this
 * floor with no keeper gate (Wardrobe.buy's counter
 * vocabulary, kept: the LOCATION is the counter, and a
 * sale settles only at its own counter — anywhere else
 * is "notsold"). This module owns both tills; GameView
 * draws the fixtures, GameActivity plays the dialogs.
 *
 * 🌱 THE GARDEN CENTRE (left wall, second row, under
 * the clothes rack): the seed rack Garden has named
 * since v0.11.0b — Garden.SEED_SUPPLIER, "the Mall
 * garden centre". Seed packets are REAL inventory now:
 * buying here stocks a packet (Garden.packetId) at the
 * crop's exact stated price, and Garden.plant spends a
 * held packet before it ever orders at planting. The
 * prices are DERIVED from Garden.CROPS — never
 * re-typed — so the rack, the packet and the planting
 * order can never drift apart on price.
 *
 * 🧺 THE SNACK SHOP (right wall, second row, under the
 * transit kiosk): the general-goods counter.
 * Stand.STOCK has named "the Mall snack shop" as the
 * town's supplier of cheesy poofs and snack cakes all
 * along; this is that shop's counter, selling the stand
 * list's four pantry staples into the player's real
 * pantry — the same inventory the dinners' spread, the
 * sleepover table and the gift flow read, untouched.
 * Prices are DERIVED from Stand.STOCK's shop prices,
 * the same numbers Fair.BOOTH charges in fair week, so
 * the counter opens no spread anywhere in town.
 *
 * THE LOOP WALLS (suite-proven in sectionV0120b):
 * counter goods land in the pantry and stop there.
 * Nothing in town buys from the player — there is no
 * sell path, here or anywhere — and the stand's crate
 * takes shop stock only through Stand.order (coins in,
 * never inventory; the suite proves an order leaves
 * every pantry count untouched) and garden produce
 * only against the garden's provenance count, so a
 * counter-bought lemonade can never be laundered into
 * crate stock and resold at the band. Seed packets
 * carry no ItemDef at all — the bus pass's own pattern
 * (Walk.PASS_ITEM) — which keeps them out of the gift
 * flow, the errand fetch pool and the litter tables by
 * construction: a packet is good for planting, only.
 *
 * THE BOOKS: every purchase is a real coin sink. Seed
 * money feeds the garden's own lifetime seed-spent
 * counter (Garden.noteSeedSpend), so the garden page's
 * total tells the truth across both doors; snack money
 * accrues in K_GOODS_SPENT, printed on the counter's
 * dialog as the till's book. No daily cap and no
 * per-visit cap — the purse is the cap, and the
 * counter sells one unit a tap, like the fair booth.
 * All state rides the established maps (inventory
 * counts, one life key) — a pre-0.12.0b save simply
 * holds no packets and plants by supplier, as before.
 */
object Shop {

    /** Both counters live in the Mall — the town's one
     *  retail floor, where every walk-up counter the
     *  game has ever had already stands. */
    const val LOC = "Mall"

    private const val K_GOODS_SPENT = "shopGoodsSpent"

    /** A settled counter transaction: whether anything
     *  moved, and the line the counter says about it. */
    data class BuyResult(val ok: Boolean, val line: String)

    // ---------------- the snack shop ----------------

    /** The general-goods stock: exactly the stand's
     *  shop list — the four pantry staples the town
     *  already prices. Derived, never re-typed. */
    val STAPLE_IDS: List<String> = Stand.STOCK.map { it.itemId }

    /** A staple's counter price: the shops' own price,
     *  read off Stand.STOCK (Fair.BOOTH agrees — the
     *  suite pins all three tables to the same number,
     *  so no buy-low/sell-high circuit can open between
     *  the counter, the stand band and the fair booth). */
    fun staplePrice(itemId: String): Int? =
        Stand.STOCK.firstOrNull { it.itemId == itemId }?.shopPrice

    /** Lifetime snack-counter spend — the till's book. */
    fun goodsSpent(state: GameState): Int =
        state.life[K_GOODS_SPENT] ?: 0

    // ---------------- the seed rack ----------------

    /** A packet's counter price: the crop's stated
     *  seed price, read off Garden.CROPS — the same
     *  number Garden.plant charges on its
     *  order-at-planting path. */
    fun seedPrice(cropId: String): Int? =
        Garden.cropById(cropId)?.seedPrice

    // ---------------- the tills ----------------

    private fun spendCoins(state: GameState, n: Int): Boolean {
        if (Minigames.capsuleCoins(state) < n) return false
        val left = Minigames.capsuleCoins(state) - n
        if (left <= 0) state.inventory.remove(Minigames.COIN_ITEM)
        else state.inventory[Minigames.COIN_ITEM] = left
        return true
    }

    /** Buy one seed packet at the garden centre
     *  counter. On success the packet is in the pack
     *  (Garden.addPacket) and the garden's seed-spent
     *  book grows by the price — the spend is real
     *  whichever of the garden's two doors it walks
     *  through, so the garden page's lifetime total
     *  stays honest. */
    fun buySeed(state: GameState, cropId: String,
                atLoc: String): BuyResult {
        val crop = Garden.cropById(cropId)
        if (atLoc != LOC || crop == null)
            return BuyResult(false,
                "Not sold at this counter.")
        if (!spendCoins(state, crop.seedPrice))
            return BuyResult(false,
                "A packet of ${crop.name.lowercase()} seed " +
                    "is ${crop.seedPrice} 🪙 at the Mall " +
                    "garden centre, and the capsule coins " +
                    "aren't there.")
        Garden.addPacket(state, crop.id)
        Garden.noteSeedSpend(state, crop.seedPrice)
        return BuyResult(true,
            "${crop.icon} One packet of " +
                "${crop.name.lowercase()} seed — " +
                "${crop.seedPrice} 🪙. It keeps forever, " +
                "and at the plot it plants free. (No " +
                "packet? Planting orders from this same " +
                "rack at the same price — the counter is " +
                "the in-person option, not a tax.)")
    }

    /** Buy one pantry staple at the snack shop
     *  counter, into the real pantry — where the
     *  dinners, the sleepovers and the gift flow will
     *  find it under their own unchanged disciplines. */
    fun buyStaple(state: GameState, itemId: String,
                  atLoc: String): BuyResult {
        val price = staplePrice(itemId)
        val item = itemById(itemId)
        if (atLoc != LOC || price == null || item == null)
            return BuyResult(false,
                "Not sold at this counter.")
        if (!spendCoins(state, price))
            return BuyResult(false,
                "One ${item.name.lowercase()} is $price 🪙 " +
                    "at the snack shop, and the capsule " +
                    "coins aren't there.")
        state.inventory[itemId] =
            (state.inventory[itemId] ?: 0) + 1
        state.life[K_GOODS_SPENT] = goodsSpent(state) + price
        return BuyResult(true,
            "${item.icon} One ${item.name.lowercase()} — " +
                "$price 🪙, into the pantry. The snack " +
                "shop wraps it like it's evidence.")
    }
}

package com.hunt3r.southparktown

import kotlin.math.roundToInt

/**
 * 🌱 YOUR GARDEN (v0.11.0b, Queue #11 item 2).
 *
 * Four beds in the yard at Your House, planted from seed
 * packets bought at the Mall garden centre's seed rack at
 * stated prices (the Stand's supplier-stock pattern: real
 * stock, real prices, pinned in the suite). A crop's life
 * is read off the town's REAL sky and nothing else: the
 * rollover advances each bed by the ENDED day's weather —
 * the same midnight read the newspaper's sky log takes —
 * so rain waters for free, a dry spell stalls growth until
 * the player's WATER beat, a summer sun scorches an
 * unwatered bed, a blizzard kills tender crops outright,
 * and the turn of winter takes every tender bed still
 * standing. Weeds creep a level a week and cut the yield
 * until the CLEAR beat. Nothing dies silently: the bed
 * record says why, and the 🌱 YOUR GARDEN Journal page
 * shows every bed's whole state.
 *
 * Growth is STORED per bed (planted day, effective growth
 * days, watered days, dry streak, scorch, weed base) and
 * advanced one honest day at a time by the rollover —
 * stated plainly: the weather itself is never stored by
 * the garden, only its effects on the beds, each traceable
 * to the day the sky actually did it.
 *
 * The harvest is REAL items in the real pantry (the item
 * registry's gift discipline, untouched), and the garden
 * feeds the town's other tables: hosted and family
 * dinners serve a homegrown dish when the pantry holds
 * produce (Dinners/Family read Garden.homegrownItem), the
 * stand's crate takes homegrown stock only from what the
 * garden actually grew (a provenance counter — a found
 * tomato is lunch, not inventory), and the season's
 * biggest pumpkin is kept for the county fair's produce
 * weigh-in, judged by Fair off the growth record itself
 * (days grown, days watered) — never off a claim.
 *
 * All state rides the established life/lifeText maps —
 * no new save keys; a pre-0.11.0b save simply has bare
 * beds.
 *
 * v0.12.0b: seed packets are REAL inventory. The Mall
 * garden centre's rack (Shop.buySeed owns its till)
 * sells a packet at the crop's stated price, held in
 * the pack until planting spends it; planting without
 * one still orders at planting at the same price, as
 * below. Both doors feed the one seed-spent book.
 */
object Garden {

    // ---------------- the plot ----------------

    /** Beds in the yard. Four: enough to plan a season
     *  around, few enough that neglecting them is a
     *  choice you made on purpose. */
    const val BEDS = 4

    /** The beats' prices in minutes, on the real clock —
     *  the darkroom's discipline: home work costs the
     *  time it takes. */
    const val PLANT_MINUTES = 15
    const val WATER_MINUTES = 15
    const val CLEAR_MINUTES = 15
    const val HARVEST_MINUTES = 15

    /** Weeds gain a level every this many days since the
     *  bed was last cleared (or planted), capped. Each
     *  level costs one unit of yield (and a tenth of a
     *  show pumpkin's weight). */
    const val WEED_EVERY_DAYS = 7
    const val WEED_CAP = 3

    // ---------------- crops & seeds ----------------

    /** One crop: its pantry item, its planting window
     *  (calendar months), its growth span in EFFECTIVE
     *  (watered) days, its base yield, whether frost and
     *  winter kill it, and its seed packet — real stock
     *  at a stated price from a stated supplier. */
    data class CropDef(
        val id: String, val name: String, val icon: String,
        val itemId: String,
        val months: Set<Int>, val windowLabel: String,
        val growDays: Int, val yield: Int,
        val tender: Boolean,
        val seedPrice: Int, val seedSupplier: String)

    /** The seed rack: the Mall garden centre sells every
     *  packet (it's the one shop in town with a garden
     *  centre — seeds beside the planters, priced like
     *  the town prices everything: in capsule coins). */
    const val SEED_SUPPLIER = "the Mall garden centre"

    val CROPS: List<CropDef> = listOf(
        CropDef("pumpkin", "Pumpkin", "🎃", "pumpkin",
            setOf(3, 4, 5), "Mar–May", 55, 1,
            tender = true, seedPrice = 3,
            seedSupplier = SEED_SUPPLIER),
        CropDef("corn", "Corn", "🌽", "corn",
            setOf(4, 5, 6, 7), "Apr–Jul", 40, 3,
            tender = true, seedPrice = 2,
            seedSupplier = SEED_SUPPLIER),
        CropDef("sunflower", "Sunflower", "🌻", "sunflower",
            setOf(3, 4, 5), "Mar–May", 35, 2,
            tender = true, seedPrice = 2,
            seedSupplier = SEED_SUPPLIER),
        CropDef("tomato", "Tomato", "🍅", "tomato",
            setOf(5, 6), "May–Jun", 30, 4,
            tender = true, seedPrice = 2,
            seedSupplier = SEED_SUPPLIER),
        // the fifth crop earns its place the Colorado way:
        // potatoes forgive. Underground, they shrug off the
        // blizzard and the winter that take everything tender.
        CropDef("potato", "Potato", "🥔", "potato",
            setOf(3, 4, 8, 9), "Mar–Apr & Aug–Sep", 45, 4,
            tender = false, seedPrice = 2,
            seedSupplier = SEED_SUPPLIER))

    fun cropById(id: String): CropDef? =
        CROPS.firstOrNull { it.id == id }

    fun cropForItem(itemId: String): CropDef? =
        CROPS.firstOrNull { it.itemId == itemId }

    /** The produce item ids, in crop order — the tables
     *  (Dinners, Family, Stand) read this one list. */
    val PRODUCE_IDS: List<String> = CROPS.map { it.itemId }

    fun isProduce(itemId: String): Boolean =
        itemId in PRODUCE_IDS

    // ---------------- the bed record ----------------

    /** One bed's whole truth, stored in lifeText and
     *  advanced a day at a time by the rollover. */
    data class Bed(
        val cropId: String, val plantedDay: Int,
        val growth: Int, val wateredDays: Int,
        val dryStreak: Int, val scorchDays: Int,
        val weedBase: Int, val lastWaterDay: Int,
        val ripe: Boolean, val dead: Boolean,
        val deadWhy: String)          // "" | "frost" | "winter"

    private fun kBed(i: Int) = "gardenBed:$i"
    private fun kWater(i: Int) = "gardenWater:$i"
    private fun kGrown(itemId: String) = "gardenGrown:$itemId"
    private fun kHarvest(itemId: String) = "gardenHarvest:$itemId"
    private fun kFair(year: Int) = "gardenFair:$year"
    private const val K_LOG = "gardenLog"          // lifeText: "m/d: n × item;…"
    private const val K_SEED_SPENT = "gardenSeedSpent"

    fun bed(state: GameState, i: Int): Bed? {
        val raw = state.lifeText[kBed(i)] ?: return null
        val p = raw.split("|")
        if (p.size != 11) return null
        return Bed(p[0],
            p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0,
            p[3].toIntOrNull() ?: 0, p[4].toIntOrNull() ?: 0,
            p[5].toIntOrNull() ?: 0, p[6].toIntOrNull() ?: 0,
            p[7].toIntOrNull() ?: -1,
            p[8] == "1", p[9] == "1", p[10])
    }

    private fun putBed(state: GameState, i: Int, b: Bed?) {
        if (b == null) state.lifeText.remove(kBed(i))
        else state.lifeText[kBed(i)] = listOf(
            b.cropId, b.plantedDay, b.growth, b.wateredDays,
            b.dryStreak, b.scorchDays, b.weedBase,
            b.lastWaterDay, if (b.ripe) 1 else 0,
            if (b.dead) 1 else 0, b.deadWhy)
            .joinToString("|")
    }

    /** Weed level right now: a level a week since the
     *  bed was last cleared (or planted), capped. */
    fun weedLevel(state: GameState, b: Bed): Int =
        ((state.absDay - b.weedBase) / WEED_EVERY_DAYS)
            .coerceIn(0, WEED_CAP)

    fun anyPlanted(state: GameState): Boolean =
        (0 until BEDS).any { bed(state, it) != null }

    // ---------------- coins ----------------

    private fun spendCoins(state: GameState, n: Int): Boolean {
        if (Minigames.capsuleCoins(state) < n) return false
        val left = Minigames.capsuleCoins(state) - n
        if (left <= 0) state.inventory.remove(Minigames.COIN_ITEM)
        else state.inventory[Minigames.COIN_ITEM] = left
        return true
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** The lifetime seed-spend book. The garden page's
     *  "seed spent so far" line reads this one counter,
     *  fed by BOTH doors: packets bought at the garden
     *  centre's rack (Shop.buySeed) and packets ordered
     *  at planting (plant, below). Planting FROM a held
     *  packet adds nothing — the spend already happened
     *  at the rack. */
    fun noteSeedSpend(state: GameState, n: Int) {
        state.life[K_SEED_SPENT] =
            (state.life[K_SEED_SPENT] ?: 0) + n
    }

    // ---------------- seed packets ----------------

    /** A crop's seed packet, as it rides the pack: an
     *  inventory count under its own id, deliberately
     *  carrying NO ItemDef — the bus pass's pattern
     *  (Walk.PASS_ITEM). That one choice keeps packets
     *  out of everything an item can wander into: the
     *  gift flow and the inventory's item list read
     *  ItemDefs, the errand fetch pool and the litter
     *  tables are built from ITEMS, and the stand's
     *  crate reads its own stock defs — a packet shows
     *  only where seed is the subject: this page, the
     *  planting picker, and the garden centre's rack.
     *  Packets stack per crop and never expire. */
    fun packetId(cropId: String): String = "seed_$cropId"

    val PACKET_IDS: List<String> = CROPS.map { packetId(it.id) }

    fun packetsHeld(state: GameState, cropId: String): Int =
        state.inventory[packetId(cropId)] ?: 0

    fun packetsHeldTotal(state: GameState): Int =
        CROPS.sumOf { packetsHeld(state, it.id) }

    /** The garden centre's half of a packet sale
     *  (Shop.buySeed owns the till and the book). */
    fun addPacket(state: GameState, cropId: String) {
        val k = packetId(cropId)
        state.inventory[k] = (state.inventory[k] ?: 0) + 1
    }

    private fun consumePacket(state: GameState, cropId: String) {
        val k = packetId(cropId)
        val left = (state.inventory[k] ?: 0) - 1
        if (left <= 0) state.inventory.remove(k)
        else state.inventory[k] = left
    }

    // ---------------- planting ----------------

    fun plantableNow(state: GameState,
                     crop: CropDef): Boolean =
        state.gameMonth in crop.months

    /** Plant [cropId] in bed [i]. Two doors, one price:
     *  a seed packet held in the pack is spent (it was
     *  paid for at the garden centre's rack — Shop's
     *  counter); without one, the packet is ordered at
     *  planting from the crop's supplier at the same
     *  stated price (the Stand's order-at-the-counter
     *  pattern), as this beat has always worked.
     *  Planting over a dead bed clears it in the same
     *  beat. Returns the line, or a plain reason why
     *  not — and a refused planting never spends a
     *  packet. */
    fun plant(state: GameState, i: Int, cropId: String): String {
        val crop = cropById(cropId)
            ?: return "The seed rack doesn't carry that."
        if (i !in 0 until BEDS) return "There's no bed $i."
        val cur = bed(state, i)
        if (cur != null && !cur.dead) {
            val cc = cropById(cur.cropId)
            return "Bed ${i + 1} is already growing " +
                "${cc?.name?.lowercase() ?: cur.cropId} — " +
                "one crop per bed, that's the whole rule."
        }
        if (!plantableNow(state, crop)) {
            return "${crop.name} goes in ${crop.windowLabel} — " +
                "the packet says so, the soil agrees, and " +
                "planting out of window is just burying " +
                "money. (${crop.seedSupplier} keeps the " +
                "rack stocked year-round; the season does " +
                "the refusing.)"
        }
        // v0.12.0b: a packet in the pack plants free —
        // the spend already happened at the rack. Without
        // one, order at planting at the same price.
        val fromPack = packetsHeld(state, crop.id) > 0
        if (fromPack) {
            consumePacket(state, crop.id)
        } else {
            if (!spendCoins(state, crop.seedPrice)) {
                return "A packet of ${crop.name.lowercase()} " +
                    "seed is ${crop.seedPrice} 🪙 at " +
                    "${crop.seedSupplier}, and the capsule " +
                    "coins aren't there."
            }
            noteSeedSpend(state, crop.seedPrice)
        }
        putBed(state, i, Bed(
            cropId = crop.id, plantedDay = state.absDay,
            growth = 0, wateredDays = 0, dryStreak = 0,
            scorchDays = 0, weedBase = state.absDay,
            lastWaterDay = -1, ripe = false, dead = false,
            deadWhy = ""))
        note(state, "🌱 Planted: ${crop.name.lowercase()} in " +
            "bed ${i + 1} — ${crop.growDays} good watered " +
            "days to ripe, weather permitting. The weather " +
            "always has opinions.")
        return if (fromPack)
            "${crop.icon} ${crop.name} seed in bed " +
                "${i + 1} — from a packet out of your " +
                "pack (${crop.seedSupplier}'s rack, " +
                "${crop.seedPrice} 🪙, already paid). It " +
                "needs water on dry days, weeds cleared " +
                "weekly, and ${crop.growDays} growing " +
                "days. The garden page keeps the record."
        else
            "${crop.icon} ${crop.name} seed in bed " +
                "${i + 1} (${crop.seedPrice} 🪙 from " +
                "${crop.seedSupplier}). It needs water on dry " +
                "days, weeds cleared weekly, and " +
                "${crop.growDays} growing days. The garden " +
                "page keeps the record."
    }

    // ---------------- the beats ----------------

    /** Water the whole plot by hand: tonight's rollover
     *  will count today as watered for every planted bed,
     *  the way a rain day counts. */
    fun water(state: GameState): String {
        var n = 0
        for (i in 0 until BEDS) {
            val b = bed(state, i) ?: continue
            if (b.dead) continue
            state.life[kWater(i)] = state.absDay
            n++
        }
        return if (n == 0) "Nothing planted to water — " +
            "the hose waters the fence for a while, " +
            "optimistically."
        else "💧 Watered — $n bed(s) drink deep. On a dry " +
            "day, this is the day's growth; on a wet one, " +
            "the garden just enjoys the attention."
    }

    /** Clear the week's weeds from every planted bed:
     *  every bed's yield clock resets to clean. */
    fun clearWeeds(state: GameState): String {
        var worst = 0
        var n = 0
        for (i in 0 until BEDS) {
            val b = bed(state, i) ?: continue
            if (b.dead) continue
            worst = maxOf(worst, weedLevel(state, b))
            putBed(state, i, b.copy(weedBase = state.absDay))
            n++
        }
        return if (n == 0) "No beds to weed. The garden is " +
            "all potential and no weeds, which is to say: " +
            "dirt."
        else if (worst == 0) "🧹 The beds were already " +
            "clean — a tidy garden is its own small boast."
        else "🧹 Weeds cleared (they'd reached level " +
            "$worst — a level a week is the creep, and " +
            "every level costs yield). The beds can breathe."
    }

    // ---------------- the harvest ----------------

    /** A pumpkin's weight, from its growth record alone:
     *  a base, plus what its grown days and watered days
     *  honestly earned, less a tenth per weed level. */
    fun pumpkinWeight(state: GameState, b: Bed): Int {
        val base = 8.0 + b.growth * 0.40 + b.wateredDays * 0.30
        val cut = 1.0 - 0.10 * weedLevel(state, b)
        return (base * cut).roundToInt().coerceAtLeast(5)
    }

    /** The units a ripe bed yields: the crop's base,
     *  less a unit per weed level — never less than one
     *  for the count crops; a pumpkin is always exactly
     *  one fruit (its yield is its weight). */
    fun yieldOf(state: GameState, b: Bed): Int {
        val crop = cropById(b.cropId) ?: return 0
        if (crop.id == "pumpkin") return 1
        return maxOf(1, crop.yield - weedLevel(state, b))
    }

    /** The season's fair specimen: the biggest pumpkin
     *  harvested in [year], kept whole for the fair's
     *  produce weigh-in, with the record it was judged
     *  from. */
    data class Specimen(val weight: Int, val growthDays: Int,
                        val wateredDays: Int)

    fun fairSpecimen(state: GameState, year: Int): Specimen? {
        val raw = state.lifeText[kFair(year)] ?: return null
        val p = raw.split(":")
        if (p.size != 3) return null
        return Specimen(p[0].toIntOrNull() ?: return null,
            p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0)
    }

    /** Provenance: how many of [itemId] in the world are
     *  garden-grown and not yet stocked at the stand. The
     *  stand's crate takes homegrown only against this
     *  count — found produce is food, never stock. */
    fun grownAvailable(state: GameState, itemId: String): Int =
        state.life[kGrown(itemId)] ?: 0

    /** Stand.stockFromGarden's other half: the garden
     *  notes that [n] of its grown [itemId] left for the
     *  crate. */
    fun noteStocked(state: GameState, itemId: String, n: Int) {
        state.life[kGrown(itemId)] =
            ((state.life[kGrown(itemId)] ?: 0) - n)
                .coerceAtLeast(0)
    }

    /** The homegrown dish candidate: the first produce
     *  the pantry actually holds, or null — Dinners and
     *  Family serve off this one read. */
    fun homegrownItem(state: GameState): String? =
        PRODUCE_IDS.firstOrNull {
            (state.inventory[it] ?: 0) > 0 }

    fun harvest(state: GameState, i: Int): String {
        val b = bed(state, i)
            ?: return "Bed ${i + 1} is bare dirt."
        val crop = cropById(b.cropId)
            ?: return "Whatever that was, it's compost now."
        if (b.dead) return "Bed ${i + 1} is dead (" +
            (if (b.deadWhy == "frost") "the blizzard took it"
             else "winter took it") + "). Plant over it — " +
            "the planting beat clears the bed."
        if (!b.ripe) return "Not ripe — ${crop.name.lowercase()} " +
            "has ${b.growth}/${crop.growDays} growing days. " +
            "Picking early is just weeding with ambition."
        val n = yieldOf(state, b)
        val item = itemById(crop.itemId)
        state.inventory[crop.itemId] =
            (state.inventory[crop.itemId] ?: 0) + n
        state.life[kGrown(crop.itemId)] =
            (state.life[kGrown(crop.itemId)] ?: 0) + n
        state.life[kHarvest(crop.itemId)] =
            (state.life[kHarvest(crop.itemId)] ?: 0) + n
        // the season's record: last few harvests, dated
        val key = Commissions.keyForAbsDay(state, state.absDay)
        val label = "${key.substring(4, 6).toInt()}/" +
            key.substring(6, 8).toInt()
        val log = ((state.lifeText[K_LOG] ?: "")
            .split(";").filter { it.isNotEmpty() } +
            "$label: $n × ${crop.itemId}")
            .takeLast(6)
        state.lifeText[K_LOG] = log.joinToString(";")
        var extra = ""
        if (crop.id == "pumpkin") {
            val w = pumpkinWeight(state, b)
            val cur = fairSpecimen(state, state.gameYear)
            if (cur == null || w > cur.weight) {
                state.lifeText[kFair(state.gameYear)] =
                    "$w:${b.growth}:${b.wateredDays}"
                extra = " It weighs $w lb — the biggest " +
                    "this season, so THIS one is kept whole " +
                    "on the porch for the county fair's " +
                    "produce weigh-in."
            } else {
                extra = " It weighs $w lb; the porch " +
                    "already keeps a ${cur.weight} lb one " +
                    "for the fair."
            }
        }
        putBed(state, i, null)
        note(state, "🧺 Harvested: $n × " +
            "${item?.name?.lowercase() ?: crop.itemId} from " +
            "bed ${i + 1} — ${b.growth} days grown, " +
            "${b.wateredDays} of them watered.")
        return "🧺 $n × ${item?.name ?: crop.name} — into " +
            "the pantry, real as rent.$extra"
    }

    // ---------------- the rollover ----------------

    /**
     * Engine.onNewDay's hook (after the stand's wrap,
     * before the paper): each planted bed lives the ENDED
     * day (absDay − 1) under the weather that day actually
     * had — state.weatherId is still the ended day's here,
     * the read every midnight module takes. Rain waters;
     * a hand-watering stamp waters; anything else is a dry
     * day, and a dry sunny summer day scorches. A blizzard
     * kills a tender crop where it stands, and the first
     * rollover into winter takes every tender bed left.
     * Growth lands only on watered days; at growDays the
     * bed is ripe, and says so once.
     */
    fun dailyRollover(state: GameState) {
        if (!anyPlanted(state)) return
        val endedDay = state.absDay - 1
        if (endedDay < 0) return
        val w = state.weatherId
        val key = Commissions.keyForAbsDay(state, endedDay)
        val endedMonth = key.substring(4, 6).toIntOrNull()
            ?: state.gameMonth
        val endedSeason = seasonFor(endedMonth)
        val newSeason = seasonFor(state.gameMonth)
        for (i in 0 until BEDS) {
            val b = bed(state, i) ?: continue
            if (b.dead || b.plantedDay > endedDay) continue
            val crop = cropById(b.cropId) ?: continue
            // winter's turn takes the tender, ripe or not
            if (crop.tender && endedSeason != Season.WINTER &&
                newSeason == Season.WINTER) {
                putBed(state, i, b.copy(dead = true,
                    deadWhy = "winter"))
                note(state, "🌱 Winter took the " +
                    "${crop.name.lowercase()} in bed ${i + 1} — " +
                    "tender crops don't see spring from " +
                    "inside a snowbank. The garden page " +
                    "keeps the record.")
                continue
            }
            // a blizzard kills tender crops where they stand
            if (crop.tender && w == "blizzard") {
                putBed(state, i, b.copy(dead = true,
                    deadWhy = "frost"))
                note(state, "🌱 The blizzard froze the " +
                    "${crop.name.lowercase()} in bed ${i + 1} " +
                    "through — tender means tender. Nothing " +
                    "about it was silent; the record says so.")
                continue
            }
            if (b.ripe) continue
            val watered = w == "rain" ||
                state.life[kWater(i)] == endedDay
            var nb = if (watered) {
                b.copy(
                    growth = minOf(crop.growDays, b.growth + 1),
                    wateredDays = b.wateredDays + 1,
                    dryStreak = 0, lastWaterDay = endedDay)
            } else {
                b.copy(dryStreak = b.dryStreak + 1,
                    scorchDays = b.scorchDays +
                        (if (w == "sunny" &&
                            endedSeason == Season.SUMMER) 1
                         else 0))
            }
            if (nb.growth >= crop.growDays && !nb.ripe) {
                nb = nb.copy(ripe = true)
                note(state, "🌱 RIPE: the " +
                    "${crop.name.lowercase()} in bed ${i + 1} " +
                    "is ready — ${nb.wateredDays} watered " +
                    "days out of ${nb.growth} grown. Pick it " +
                    "at the plot.")
            }
            putBed(state, i, nb)
        }
    }

    // ---------------- the page ----------------

    private fun dateLabel(state: GameState, absDay: Int): String {
        val key = Commissions.keyForAbsDay(state, absDay)
        return "${key.substring(4, 6).toInt()}/" +
            key.substring(6, 8).toInt()
    }

    private fun waterText(state: GameState, b: Bed): String =
        when {
            b.lastWaterDay == state.absDay ->
                "watered today"
            b.lastWaterDay == state.absDay - 1 ->
                "watered last night"
            b.lastWaterDay < 0 -> "never watered"
            else -> "last watered " +
                "${state.absDay - b.lastWaterDay} day(s) ago"
        }

    private fun weedText(state: GameState, b: Bed): String =
        when (val lv = weedLevel(state, b)) {
            0 -> "weeds: none"
            1 -> "weeds: light (−1 yield)"
            2 -> "weeds: thick (−2 yield)"
            else -> "weeds: a jungle (−3 yield)"
        }

    fun bedLine(state: GameState, i: Int): String {
        val b = bed(state, i)
            ?: return "Bed ${i + 1} — bare dirt, waiting on a season."
        val crop = cropById(b.cropId)
        val name = crop?.name ?: b.cropId
        val icon = crop?.icon ?: "🌱"
        return when {
            b.dead -> "Bed ${i + 1} — $icon $name: DEAD (" +
                (if (b.deadWhy == "frost")
                    "a blizzard froze it through"
                 else "winter took it") +
                "), planted ${dateLabel(state, b.plantedDay)}. " +
                "Plant over it when its season comes."
            b.ripe -> "Bed ${i + 1} — $icon $name: RIPE 🧺 " +
                "(${b.growth} days grown, ${b.wateredDays} " +
                "watered" +
                (if (b.cropId == "pumpkin")
                    ", ≈${pumpkinWeight(state, b)} lb" else "") +
                "). Pick it at the plot."
            else -> "Bed ${i + 1} — $icon $name: " +
                "${b.growth}/${crop?.growDays ?: 0} growing " +
                "days (${(crop?.growDays ?: 0) - b.growth} to " +
                "ripe) · ${waterText(state, b)}" +
                (if (b.dryStreak >= 2)
                    " · DRY ${b.dryStreak} days — growth is " +
                        "paused until water" else "") +
                (if (b.scorchDays > 0)
                    " · scorched ${b.scorchDays} day(s)" else "") +
                " · ${weedText(state, b)}"
        }
    }

    /** The pack's seed shelf, as one page line — null
     *  when the pack holds no packets, so a save that
     *  never met the counter reads exactly as before. */
    private fun packLine(state: GameState): String? {
        val held = CROPS.filter { packetsHeld(state, it.id) > 0 }
        if (held.isEmpty()) return null
        return "🎒 Seed packets in your pack: " +
            held.joinToString(" · ") {
                "${it.icon} ${it.name.lowercase()} ×" +
                    packetsHeld(state, it.id) } +
            " — bought at ${SEED_SUPPLIER}; each plants " +
            "one bed, free at the plot."
    }

    /** Journal 🌱 YOUR GARDEN: every bed's whole state,
     *  the season's harvest record, and the fair pumpkin
     *  on the porch — every claim traced to the recorded
     *  sky, like the paper prints it. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🌱 YOUR GARDEN"
        val everPlanted = CROPS.any {
            (state.life[kHarvest(it.itemId)] ?: 0) > 0 } ||
            anyPlanted(state)
        if (!everPlanted) {
            out += "Four beds of good dirt in the yard, and " +
                "nothing in them yet. Gardening here runs on " +
                "the town's real sky: rain waters for free, " +
                "dry days stall a crop until you water it " +
                "yourself, weeds creep a level a week and " +
                "cost yield, a blizzard kills anything " +
                "tender, and winter takes the tender beds " +
                "whole. Potatoes forgive all of it."
            out += "Seeds come from ${SEED_SUPPLIER}'s rack " +
                "— ordered at planting, or carried as " +
                "packets from the counter there: " +
                CROPS.joinToString(" · ") {
                    "${it.icon} ${it.name.lowercase()} " +
                        "${it.seedPrice} 🪙 (${it.windowLabel}, " +
                        "${it.growDays} days)" } + "."
            out += "The harvest is real food: dinners serve " +
                "it, the stand can sell it (grown stock " +
                "only), and the season's biggest pumpkin " +
                "goes to the county fair's weigh-in."
            packLine(state)?.let { out += it }
            return out
        }
        for (i in 0 until BEDS) out += bedLine(state, i)
        packLine(state)?.let { out += it }
        val totals = CROPS.map { it to
            (state.life[kHarvest(it.itemId)] ?: 0) }
            .filter { it.second > 0 }
        if (totals.isNotEmpty()) {
            out += "— THE HARVEST RECORD —"
            out += "Lifetime picked: " + totals.joinToString(" · ") {
                "${it.first.icon} ${it.second} " +
                    it.first.itemId } +
                " · seed spent so far: " +
                "${state.life[K_SEED_SPENT] ?: 0} 🪙"
            val log = (state.lifeText[K_LOG] ?: "")
                .split(";").filter { it.isNotEmpty() }
            for (e in log) out += "   🧺 $e"
        }
        val spec = fairSpecimen(state, state.gameYear)
        if (spec != null) {
            out += "🎃 On the porch: this season's biggest " +
                "pumpkin — ${spec.weight} lb, ${spec.growthDays} " +
                "days grown, ${spec.wateredDays} of them " +
                "watered. It's the fair's produce weigh-in " +
                "entry, judged off exactly that record."
        }
        return out
    }
}

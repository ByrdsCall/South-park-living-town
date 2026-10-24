package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Items — the v0.1.5 inventory system (data-driven).
//
// One ItemDef list feeds everything: the classic daily
// COLLECTIBLES fold in as inventory items (collecting one still
// fills the Collection screen exactly as before), trash loot
// tables draw from it, ground litter is made of it, and gifts
// are picked from it. Tiers: junk | gift | useful | rare | lore.
// ============================================================

data class ItemDef(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val tier: String,               // junk | gift | useful | rare | lore
    val giftPoints: Int = 4,        // relationship points when gifted
    /** NPC names or trait words that LOVE this item (gift bonus x2). */
    val lovedBy: Set<String> = emptySet()
)

val ITEMS: List<ItemDef> = run {
    val list = mutableListOf<ItemDef>()
    // Fold the collectibles in as inventory items (same ids!).
    for (c in COLLECTIBLES) {
        list.add(ItemDef(
            id = c.id, name = c.name, icon = c.icon, desc = c.flavor,
            tier = if (c.rare) "rare" else "gift",
            giftPoints = if (c.rare) 10 else 6,
            lovedBy = when (c.id) {
                "cheesy_poof", "kupa_coin" -> setOf("Cartman")
                "tweek_coffee" -> setOf("Tweek", "coffee")
                "towelie_scrap", "tegridy_nugget" -> setOf("Towelie", "Randy")
                "mr_hat" -> setOf("Garrison")
                "craig_foam_finger" -> setOf("Craig")
                "gobbles_feather" -> setOf("Timmy")
                "woodsy_poster" -> setOf("Mackey")
                "member_berry" -> setOf("Randy")
                else -> emptySet()
            }
        ))
    }
    list.addAll(listOf(
        // ---- giftable finds (shops of the heart) ----
        ItemDef("snack_cake", "Snacky Cake", "🍰",
            "A gas-station snack cake. The universal peace offering.", "gift", 6,
            setOf("Butters", "Cartman")),
        ItemDef("hot_chocolate", "Hot Chocolate", "☕",
            "Still warm. Somehow. Colorado magic.", "gift", 6,
            setOf("Stan", "Kyle", "Tweek")),
        ItemDef("flower", "Slightly Frozen Flower", "🌷",
            "Picked from someone's yard. It's the thought that counts.", "gift", 7,
            setOf("Wendy", "Bebe", "Sharon", "romantic")),
        ItemDef("comic_book", "Old Comic Book", "📚",
            "Issue #47. The cover's half torn off but the inside slaps.", "gift", 7,
            setOf("Jimmy", "Clyde", "geek")),
        ItemDef("coffee_beans", "Bag of Coffee Beans", "🫘",
            "Tweek Bros. reject beans. Still extremely caffeinated.", "gift", 6,
            setOf("Tweek", "coffee")),
        ItemDef("shiny_rock", "Shiny Rock", "💎",
            "It's just a rock. But it's SO shiny.", "gift", 5,
            setOf("Kenny", "curious")),
        // ---- useful ----
        ItemDef("umbrella", "Leaky Umbrella", "☂️",
            "Keeps off at least half the snow. A South Park treasure.", "useful", 3),
        ItemDef("hand_warmer", "Hand Warmer", "🔥",
            "One (1) warm hand. Choose wisely.", "useful", 3),
        // ---- trash-tier junk (still giftable — weirdos exist) ----
        ItemDef("crumpled_wrapper", "Crumpled Wrapper", "🍬",
            "Smells faintly of cheesy poofs and regret.", "junk", 2,
            setOf("Cartman")),
        ItemDef("empty_cup", "Empty Coffee Cup", "🥤",
            "Tweek Bros. cup. Still trembling a little.", "junk", 2,
            setOf("Tweek")),
        ItemDef("tin_can", "Dented Tin Can", "🥫",
            "Label's gone. The mystery is the meal.", "junk", 2,
            setOf("Kenny", "Stuart")),
        ItemDef("old_sock", "Single Old Sock", "🧦",
            "Its partner is in a better place now.", "junk", 1),
        // v0.1.9a #93: turns up as winter litter; the mitten tree
        // at Kid's Park collects them, one by one.
        ItemDef("lost_mitten", "Lost Mitten", "🧤",
            "One mitten, no partner, lightly frostbitten. The mitten tree at the park collects these.",
            "junk", 2, setOf("Kenny")),
        ItemDef("bottle_cap", "Bent Bottle Cap", "🔘",
            "Skeeter's brand. Worth exactly one memory.", "junk", 2,
            setOf("Jimbo", "Randy")),
        ItemDef("dead_battery", "Dead Battery", "🔋",
            "Phase 2: ??? Phase 3: profit.", "junk", 1),
        ItemDef("mystery_fuzz", "Mystery Fuzz", "🌀",
            "Nobody knows. Nobody wants to know.", "junk", 1,
            setOf("curious")),
        // ---- rare lore-flavored treasures (paraphrased flavor only) ----
        ItemDef("golden_ticket_stub", "Golden Ticket Stub", "🎫",
            "From the chocolate factory tour that went... the way tours go here.", "lore", 12),
        ItemDef("velcro_wallet", "Velcro Wallet (Empty)", "👛",
            "Smells like a kid who once believed in allowances.", "lore", 10,
            setOf("Kenny")),
        ItemDef("broken_sled", "Splintered Sled Runner", "🛷",
            "It saw the big hill. It saw too much.", "lore", 10),
        ItemDef("cassette_single", "Water-Damaged Cassette Single", "📼",
            "Side A warbles a boy-band demo. Side B is just screaming.", "lore", 11,
            setOf("music"))
    ))
    // v0.1.8 #75: the Main Street lemonade stand's whole stock
    list.add(ItemDef("lemonade", "Lemonade", "🍋",
        "Twenty-five cents of pure summer. The kid running the stand drives a hard bargain.",
        "gift", 6, setOf("Butters", "Stan", "sweet")))
    // v0.2.0e: the minigame economy. The capsule coin is an
    // INVENTORY ITEM, not a currency system — the Showdown and
    // the Taco Catch cabinet pay them, kids gift and hoard
    // them, and the Journal 🎮 GAMES page counts your stack.
    list.add(ItemDef("capsule_coin", "Capsule Coin", "🪙",
        "From the capsule machine by the taco arcade. Legal tender in exactly " +
            "one economy: bragging.",
        "gift", 3, setOf("Clyde", "geek")))
    // v0.5.0a: the bus pass — a KEY item (its own tier, so no
    // loot table, gift pool or fetch list can ever deal it).
    // Ownership lives in GameState.busPass (Walk owns the
    // rules); this card is the physical thing in your pack.
    list.add(ItemDef("bus_pass", "South Park Bus Pass", "🚌",
        "Laminated. Unlimited rides on the one bus. The driver knows your " +
            "name now — if you give the card away he'll still wave you on. " +
            "He KNOWS you.",
        "key", 0))
    // v0.2.0e: what the pond gives up. Every catch lands in
    // the pack as one of these, so a rare fish is a gift an
    // NPC can have opinions about (giftValue/lovedBy rules).
    list.add(ItemDef("fish_smelt", "Pond Smelt", "🐟",
        "Caught at Stark's Pond. Small, silver, extremely caught.", "gift", 5,
        setOf("Jimbo")))
    list.add(ItemDef("fish_minnow", "Mud Minnow", "🐟",
        "Barely a fish. More of a fish rumor. Yours now.", "gift", 4))
    list.add(ItemDef("fish_trout", "Speckled Trout", "🐠",
        "A proper Stark's Pond trout. Chef could do things with this.", "gift", 7,
        setOf("Chef")))
    list.add(ItemDef("fish_rainbow", "Rainbow Trout", "🌈",
        "Shimmers in seven colors and one emotion: smug.", "rare", 10,
        setOf("Butters")))
    list.add(ItemDef("fish_carp", "Golden Carp", "🏆",
        "THE golden carp of Stark's Pond. Trophy-grade. Jimbo tells stories about this one.",
        "rare", 12, setOf("Jimbo")))
    list.add(ItemDef("fish_three_eyed", "Three-Eyed Fish", "☢️",
        "It sees you. It sees you. It also sees you. Handle with gloves and questions.",
        "lore", 14, setOf("Randy")))
    list.add(ItemDef("old_boot", "Old Boot", "🥾",
        "Size 9. Waterlogged. The pond's longest-running joke, and you caught it.",
        "junk", 1))
    // v0.11.0b: garden produce — grown at Your House, never
    // stocked by any shop. Real pantry food under the same
    // gift/eat discipline as everything above; the stand's
    // crate is the only shelf it can be sold from, and only
    // what the garden provably grew (Garden keeps the
    // provenance count).
    list.add(ItemDef("pumpkin", "Pumpkin", "🎃",
        "A real pumpkin, grown at your house. Fair-grade, if you grew it right.",
        "gift", 7, setOf("Randy", "Chef")))
    list.add(ItemDef("corn", "Corn on the Cob", "🌽",
        "Sweet corn off your own stalk. Butter is a rumor it believes in.",
        "gift", 5, setOf("Randy", "Jimbo")))
    list.add(ItemDef("sunflower", "Sunflower", "🌻",
        "A sunflower the size of a dinner plate. Basically a flower with ambitions.",
        "gift", 7, setOf("Wendy", "Bebe")))
    list.add(ItemDef("tomato", "Tomato", "🍅",
        "A tomato that tastes like the month it grew in. All of them do. That's the point.",
        "gift", 6, setOf("Chef", "Sharon")))
    list.add(ItemDef("potato", "Potato", "🥔",
        "A potato. It survived everything the mountain winter said to it, underground, unbothered.",
        "gift", 5, setOf("Chef", "Stuart")))
    list
}

val ITEM_BY_ID: Map<String, ItemDef> = ITEMS.associateBy { it.id }

fun itemById(id: String): ItemDef? = ITEM_BY_ID[id]

/** Gift outcome: relationship points + whether the NPC loved it. */
fun giftValue(item: ItemDef, npcName: String, npcTraits: List<String>): Pair<Int, Boolean> {
    val loved = item.lovedBy.any { tag ->
        tag.equals(npcName, ignoreCase = true) || npcTraits.any { it.contains(tag, ignoreCase = true) }
    }
    return (if (loved) item.giftPoints * 2 else item.giftPoints) to loved
}

// ---------------- Trash containers (loot chests of the people) ----------------

data class TrashContainerDef(
    val id: String,
    val locId: String,
    val inside: Boolean,
    val kind: String, // can | dumpster | pile | bags
    val name: String
)

val TRASH_CONTAINERS: List<TrashContainerDef> = listOf(
    TrashContainerDef("tc_main_dumpster", "MainStreet", false, "dumpster", "Dumpster behind Main Street"),
    TrashContainerDef("tc_main_can", "MainStreet", false, "can", "Main Street trash can"),
    TrashContainerDef("tc_bus_can", "BusStop", false, "can", "Bus stop trash can"),
    TrashContainerDef("tc_park_pile", "KidsPark", false, "pile", "Trash pile by the court"),
    TrashContainerDef("tc_park_can", "KidsPark", false, "can", "Park trash can"),
    TrashContainerDef("tc_pond_bags", "StarksPond", false, "bags", "Trash bags by the pond"),
    TrashContainerDef("tc_farm_pile", "TegridyFarms", false, "pile", "Barn-side junk pile"),
    TrashContainerDef("tc_sodo_dumpster", "SoDoSoPa", false, "dumpster", "SoDoSoPa dumpster"),
    TrashContainerDef("tc_mall_dumpster", "Mall", false, "dumpster", "Mall loading-dock dumpster"),
    TrashContainerDef("tc_school_dumpster", "Elementary", false, "dumpster", "School dumpster (behind)"),
    TrashContainerDef("tc_kenny_pile", "KennysHouse", false, "pile", "McCormick yard pile"),
    TrashContainerDef("tc_cartman_bags", "CartmanHouse", false, "bags", "Bags by Cartman's garage"),
    TrashContainerDef("tc_skeeter_dumpster", "SkeetersBar", false, "dumpster", "Skeeter's back dumpster"),
    TrashContainerDef("tc_citywok_dumpster", "CityWok", false, "dumpster", "City Wok dumpster"),
    TrashContainerDef("tc_church_can", "Church", false, "can", "Church trash can"),
    TrashContainerDef("tc_tweek_can_in", "TweekBros", true, "can", "Tweek Bros. trash can"),
    TrashContainerDef("tc_bijou_can_in", "Bijou", true, "can", "Bijou lobby trash can"),
    TrashContainerDef("tc_cafeteria_can", "Elementary", true, "can", "Cafeteria trash can"),
    TrashContainerDef("tc_hall_can", "CityHall", true, "can", "City Hall trash can"),
    // v0.1.8 #39: coverage — every enterable interior gets a can
    TrashContainerDef("tc_bank_can", "Bank", true, "can", "Bank trash can"),
    TrashContainerDef("tc_po_can", "PostOffice", true, "can", "Post office trash can"),
    TrashContainerDef("tc_raisins_can", "Raisins", true, "can", "Raisins trash can"),
    TrashContainerDef("tc_rhino_can", "TomsRhino", true, "can", "Rhinoplasty trash can"),
    TrashContainerDef("tc_tacos_can", "FreemansTacos", true, "can", "Taco joint trash can"),
    TrashContainerDef("tc_jimbo_can", "JimbosGuns", true, "can", "Jimbo's trash can"),
    TrashContainerDef("tc_wok_can", "CityWok", true, "can", "City Wok trash can"),
    TrashContainerDef("tc_skeeter_can", "SkeetersBar", true, "can", "Skeeter's trash can"),
    TrashContainerDef("tc_church_can_in", "Church", true, "can", "Church hall trash can"),
    TrashContainerDef("tc_mall_can", "Mall", true, "can", "Mall trash can"),
    TrashContainerDef("tc_sodo_can", "SoDoSoPa", true, "can", "SoDoSoPa trash can"),
    TrashContainerDef("tc_stan_can", "StanHouse", true, "can", "Marsh kitchen trash can"),
    TrashContainerDef("tc_kyle_can", "KyleHouse", true, "can", "Broflovski kitchen trash can"),
    TrashContainerDef("tc_cartman_can", "CartmanHouse", true, "can", "Cartman kitchen trash can"),
    TrashContainerDef("tc_kenny_can", "KennysHouse", true, "can", "McCormick trash can"),
    TrashContainerDef("tc_butters_can", "ButtersHouse", true, "can", "Stotch kitchen trash can"),
    TrashContainerDef("tc_tolkien_can", "TolkienHouse", true, "can", "Black family trash can"),
    // v0.3.0d: your house joins the coverage — even the new kid's
    // trash is searchable (mostly his own junk mail, honestly)
    TrashContainerDef("tc_newkid_can", "YourHouse", true, "can", "Your kitchen trash can")
)

fun containersAt(locId: String, inside: Boolean): List<TrashContainerDef> =
    TRASH_CONTAINERS.filter { it.locId == locId && it.inside == inside }

/** v0.1.8 #5: faction-flavored finds — on a faction's turf the loot
 *  table bends toward their flavor (goth corners yield goth finds,
 *  Tegridy turf yields farm goods, and so on). Ids are ITEMS ids. */
val TURF_LOOT: Map<String, List<String>> = mapOf(
    "boys_crew" to listOf("cheesy_poof", "kupa_coin", "comic_book"),
    "goth_kids" to listOf("cassette_single", "mystery_fuzz"),
    "girls_circle" to listOf("flower", "snack_cake"),
    "craigs_gang" to listOf("craig_foam_finger", "comic_book"),
    "parents" to listOf("bottle_cap", "hot_chocolate"),
    "school_staff" to listOf("woodsy_poster", "coffee_beans"),
    "church_faithful" to listOf("flower", "golden_ticket_stub"),
    "tegridy_crew" to listOf("tegridy_nugget", "member_berry")
)

// ---------------- Loot tables ----------------

private val JUNK_IDS = ITEMS.filter { it.tier == "junk" }.map { it.id }
private val GIFT_IDS = ITEMS.filter { it.tier == "gift" }.map { it.id }
private val USEFUL_IDS = ITEMS.filter { it.tier == "useful" }.map { it.id }
private val LORE_IDS = ITEMS.filter { it.tier == "lore" }.map { it.id }

/** Roll one container search. Dumpsters/piles can yield lore treasures. */
fun rollContainerLoot(rand: Random, bigContainer: Boolean, eventBoost: Boolean): String? {
    val r = rand.nextDouble()
    val loreChance = when {
        !bigContainer -> 0.0
        eventBoost -> 0.10
        else -> 0.05
    }
    return when {
        r < 0.10 -> null // sometimes it's just... empty
        r < 0.10 + loreChance && LORE_IDS.isNotEmpty() -> LORE_IDS[rand.nextInt(LORE_IDS.size)]
        r < 0.72 -> JUNK_IDS[rand.nextInt(JUNK_IDS.size)]
        r < 0.93 -> GIFT_IDS[rand.nextInt(GIFT_IDS.size)]
        else -> USEFUL_IDS[rand.nextInt(USEFUL_IDS.size)]
    }
}

/** Ground litter items — what NPCs drop. Mostly honest junk. */
val LITTER_ITEM_IDS: List<String> = listOf(
    "crumpled_wrapper", "empty_cup", "tin_can", "bottle_cap", "old_sock", "dead_battery"
)

/** Picking up litter: 92% it IS the junk you saw; 8% something worthwhile was under it. */
fun rollLitterFind(rand: Random, litterItemId: String): String =
    if (rand.nextDouble() < 0.08) GIFT_IDS[rand.nextInt(GIFT_IDS.size)] else litterItemId

/** A dropped piece of ground litter (position is derived deterministically at render). */
data class LitterDrop(
    val id: String,
    val locId: String,
    val inside: Boolean,
    val itemId: String,
    val dayKey: String,
    val droppedBy: String = ""
)

/** Storm debris / event mess: junk items scattered after chaos. */
fun debrisItem(rand: Random): String = JUNK_IDS[rand.nextInt(JUNK_IDS.size)]

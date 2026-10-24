package com.hunt3r.southparktown

// ============================================================
// GameData — all content ported from simulation.py / creator.py.
// Pure Kotlin, zero android.* imports. Safe for JVM unit tests.
// ============================================================

data class Location(
    val id: String,
    val name: String,
    val blurb: String,
    val colorHex: String,
    val kind: String, // house | shop | civic | school | outdoor | hub
    val gx: Int,
    val gy: Int
)

data class WeatherInfo(val id: String, val label: String, val desc: String, val icon: String)

data class AnimalSpecies(val id: String, val actions: List<String>, val icon: String, val rare: Boolean = false)

data class AgendaBlock(val location: String, val activity: String)

data class KidProfile(
    val name: String,
    val description: String,
    val color: String,
    val agenda: Map<String, AgendaBlock>
)

data class Collectible(
    val id: String,
    val name: String,
    val icon: String, // emoji glyph, drawn on Canvas
    val flavor: String,
    val rare: Boolean = false,
    val christmasOnly: Boolean = false
)

data class Holiday(
    val id: String,
    val name: String,
    val startMd: String, // "MMdd"
    val endMd: String,   // "MMdd"
    val flags: Set<String>
)

enum class Season { WINTER, SPRING, SUMMER, FALL }

// ---------------- Town map (ids/names/blurbs/colors/coords = simulation.py) ----------------
val LOCATIONS: Map<String, Location> = linkedMapOf(
    "KennysHouse" to Location("KennysHouse", "Kenny's House",
        "A run-down shack across the railroad tracks. The porch sags. ColecoVision inside, meth-lab rumors outside. Smells like poverty and old pizza.",
        "#8a8578", "house", 8, 80),
    "ButtersHouse" to Location("ButtersHouse", "Stotch Residence",
        "Butters' house. Suspiciously clean. You can hear him getting grounded through the walls.",
        "#a8c8e8", "house", 25, 80),
    "CartmanHouse" to Location("CartmanHouse", "Cartman's House",
        "Cartman's house. Kupa Keep stands in the backyard. His mom's inside making snacks.",
        "#d9b380", "house", 38, 80),
    "StanHouse" to Location("StanHouse", "Marsh Residence",
        "Stan's house. Randy is probably doing something stupid in the garage.",
        "#3e6b8a", "house", 51, 80),
    "KyleHouse" to Location("KyleHouse", "Broflovski Residence",
        "Kyle's house. Smells like matzo ball soup and moral superiority.",
        "#8a9a7b", "house", 64, 80),
    "YourHouse" to Location("YourHouse", "Your House",
        "Your house. Yours. The mailbox says THE NEW KID, the fridge is mostly mustard, and everything you earn out there ends up on a shelf in here — the place is proud of you, in its quiet way.",
        "#8a6a7d", "house", 77, 80),
    "BusStop" to Location("BusStop", "South Park Bus Stop",
        "THE bus stop. A bench, a sign, and twenty years of opening scenes. Stan and Kyle on the left, Kenny far right, Cartman by the sign. A lifetime of bad decisions waiting to happen.",
        "#e8c53a", "outdoor", 45, 90),
    "MainStreet" to Location("MainStreet", "Main Street",
        "Main Street. Parades, riots, protests, Cow Days bull runs, zombie attacks — the town's stage. Shops, slush, and hobos asking for change.",
        "#9a9a9a", "hub", 50, 52),
    "TweekBros" to Location("TweekBros", "Tweek Bros. Coffeehouse",
        "Tweek Bros. Coffeehouse. Tweek is behind the counter vibrating. The coffee is too damn strong — and probably laced with something. The goth kids refuse to drink here.",
        "#7a5230", "shop", 35, 45),
    "JimbosGuns" to Location("JimbosGuns", "Jimbo's Guns",
        "Jimbo's Guns. 'It's coming right for us!' Wagon wheel out front, rifles on orange walls, a mirror for posing. The smell of gun oil and freedom.",
        "#b85c2a", "shop", 62, 45),
    "CityWok" to Location("CityWok", "City Wok",
        "City Wok. Tuong Lu Kim will take your order — City Beef, City Chicken, City Shrimp. Doubles as City Airlines (one plane, tail 432G). The basement hosts rooster Magic tournaments. Shitty food, shitty service, great egg rolls.",
        "#c0392b", "shop", 70, 55),
    "SkeetersBar" to Location("SkeetersBar", "Skeeter's Bar",
        "Skeeter's Bar. The dads' bar — Randy, Gerald, Jimbo, Ned, Stuart. Wheel of Fortune on the TV, darts, pool. The regulars haven't moved since 1997. Kids aren't really welcome, but nobody stops them.",
        "#6b4a2f", "shop", 28, 55),
    "Bijou" to Location("Bijou", "Bijou Theatre",
        "The Bijou Theatre. Currently showing a movie you're too young for, according to the clerk.",
        "#6a4a8a", "shop", 80, 50),
    "TomsRhino" to Location("TomsRhino", "Tom's Rhinoplasty",
        "Tom's Rhinoplasty. Get a brand new nose, or possibly David Hasselhoff's face.",
        "#e8a0b0", "shop", 42, 42),
    "Bank" to Location("Bank", "Bank of South Park",
        "Bank of South Park. The teller will explain that your money is gone. And it's gone.",
        "#8a94a0", "shop", 56, 42),
    "PostOffice" to Location("PostOffice", "Post Office",
        "The post office. Smells like envelopes and broken dreams.",
        "#7a8a9a", "shop", 48, 62),
    "FreemansTacos" to Location("FreemansTacos", "Freeman's Tacos",
        "Freeman's Tacos. Morgan Freeman narrates your burrito. It's weird. It's great.",
        "#e8932a", "shop", 24, 64),
    "Church" to Location("Church", "South Park Church",
        "South Park Church. White wooden parish, hellfire sermons, confessional drama. Father Maxi is inside, sweating about something. Kenny's funerals: held here weekly.",
        "#f0f0f0", "civic", 30, 30),
    "CityHall" to Location("CityHall", "City Hall",
        "City Hall. Mayor McDaniels is holding a town meeting about nothing.",
        "#c8b88a", "civic", 45, 30),
    "Elementary" to Location("Elementary", "South Park Elementary",
        "South Park Elementary. Brick school, flagpole, playground. Garrison's 4th-grade classroom, Mackey's office, Chef's cafeteria — the Hall of Honor memorial on the wall. Sit down and shut up.",
        "#a05a3a", "school", 88, 30),
    "KidsPark" to Location("KidsPark", "Kid's Park",
        "The park. Basketball court, woodchips, and at least one used condom. Don't touch anything.",
        "#5aa85a", "outdoor", 62, 24),
    "StarksPond" to Location("StarksPond", "Stark's Pond",
        "Stark's Pond. The town's winter ice-skating pond — Wendy landed a triple lutz here once. Misty, creepy, edge of town. Something's moving under the ice. Probably.",
        "#5a8ac8", "outdoor", 95, 14),
    "Mall" to Location("Mall", "South Park Mall",
        "South Park Mall. Half the stores are closed; the food court endures. Fingerbang played here. Kenny died by elevator here. On Black Friday it becomes a war zone. An alien ship crashed into it once — the government said it was a new Taco Bell.",
        "#c8a878", "shop", 78, 68),
    "TolkienHouse" to Location("TolkienHouse", "Tolkien's House",
        "Tolkien's house. Nicer than yours. Don't think about it.",
        "#b5654a", "house", 88, 68),
    "SoDoSoPa" to Location("SoDoSoPa", "SoDoSoPa",
        "SoDoSoPa. Gentrified lofts, Whole Foods, and people who say 'craft' a lot.",
        "#c86a9a", "outdoor", 93, 82),
    "TegridyFarms" to Location("TegridyFarms", "Tegridy Farms",
        "Tegridy Farms. Randy's hemp farm. Towelie is the business partner. The family is miserable here, every single day. It smells like skunk and bad decisions.",
        "#7aa84a", "outdoor", 12, 12),
    "Raisins" to Location("Raisins", "Raisins",
        "Raisins. Hooters-style breastaurant — waitresses named after cars, famous wings. Butters fell for Lexus here and gave his 'beautiful sadness' speech. The boys pretend they're here for the wings.",
        "#d96a4a", "shop", 72, 42)
)

/**
 * v0.1.9a Scope B — scene parity registry: the defining visual
 * details SceneArt.drawParityDetails draws for each location, on
 * top of the generic facade. Each token appears as a
 * `// detail: <token>` marker in SceneArt.kt; the smoke suite
 * cross-checks this registry against the source so a location
 * can never silently lose its identity again. Palettes above
 * were also corrected toward the show where they had drifted
 * (Jimbo's orange walls, the Marsh house's dusty blue, the
 * bank's blue-gray stone, Raisins' orange-red).
 */
val LOC_ART_DETAILS: Map<String, List<String>> = mapOf(
    "KennysHouse" to listOf("railroad-tracks", "sagging-porch", "boarded-window"),
    "ButtersHouse" to listOf("picket-fence", "neat-lawn"),
    "CartmanHouse" to listOf("driveway", "red-wagon"),
    "StanHouse" to listOf("garage", "driveway-slab"),
    "KyleHouse" to listOf("hedge-row", "neat-walkway"),
    "YourHouse" to listOf("new-kid-mailbox", "bike-lean", "welcome-mat"),
    "TolkienHouse" to listOf("iron-gate", "columns", "dark-meadows-arch"),
    "BusStop" to listOf("road-strip", "snowbank"),
    "MainStreet" to listOf("crosswalk-zebra", "gazette-boxes"),
    "TweekBros" to listOf("cup-sign", "menu-board"),
    "JimbosGuns" to listOf("wagon-wheel", "antlers"),
    "CityWok" to listOf("pagoda-eaves", "lanterns"),
    "SkeetersBar" to listOf("neon-bar-sign", "bar-window-glow"),
    "Bijou" to listOf("marquee", "poster-boxes"),
    "TomsRhino" to listOf("nose-sign", "before-after-posters"),
    "Bank" to listOf("bank-columns", "bank-steps", "dollar-pediment"),
    "PostOffice" to listOf("post-flag", "collection-box"),
    "FreemansTacos" to listOf("roof-taco", "taco-menu-board"),
    "Church" to listOf("stained-glass", "clapboard-siding"),
    "CityHall" to listOf("hall-dome", "hall-columns"),
    "Elementary" to listOf("entrance-steps", "double-doors", "playground-slide"),
    "KidsPark" to listOf("swing-set", "sandbox"),
    "StarksPond" to listOf("dock", "cattails", "pond-sign"),
    "Mall" to listOf("sign-pylon", "parking-stripes", "entrance-canopy"),
    "SoDoSoPa" to listOf("courtyard-planters", "string-lights", "patio-umbrellas"),
    "TegridyFarms" to listOf("hemp-rows", "silo", "farm-fence"),
    "Raisins" to listOf("wing-sign", "orange-roof-band")
)

/**
 * v0.2.0a — palette identity: every location carries a palette
 * signature (field / wall / floor / accent). The field, wall and
 * floor hexes are the SAME values the renderers use
 * (Location.colorHex, InteriorDef.wallHex / floorHex) — this
 * record exists so the smoke suite can pin the identity rules
 * in one place and the two can never drift apart:
 *  - every wall hex is measurably distinct from every other
 *    (min pairwise RGB distance 20.3), floors likewise (18.2),
 *    fields likewise (17.0) — no two places share a color;
 *  - map neighbors (EXITS pairs) differ by a combined
 *    signature distance of at least 82.9 (suite floor: 60);
 *  - the 26 accents are all distinct.
 * v0.2.0a re-tuned 5 interior walls and 14 floors that had
 * drifted into duplicates/near-duplicates (four rooms shared
 * one floor hex; the Mall and the school shared a wall).
 */
data class LocPalette(
    val fieldHex: String,
    val wallHex: String,
    val floorHex: String,
    val accentHex: String
)

val LOC_PALETTES: Map<String, LocPalette> = mapOf(
    "KennysHouse" to LocPalette("#8a8578", "#8a8578", "#6b5a44", "#e07b2a"),
    "ButtersHouse" to LocPalette("#a8c8e8", "#a8c8e8", "#ac8658", "#f5d76e"),
    "CartmanHouse" to LocPalette("#d9b380", "#d9b380", "#75512f", "#c0392b"),
    "StanHouse" to LocPalette("#3e6b8a", "#64906b", "#8a6a4a", "#3e6b8a"),
    "KyleHouse" to LocPalette("#8a9a7b", "#8a9a7b", "#7c5540", "#2e7d4f"),
    "YourHouse" to LocPalette("#8a6a7d", "#7d5a70", "#4e3a44", "#7ac74f"),
    "BusStop" to LocPalette("#e8c53a", "#e8c53a", "#4c4c50", "#d43d2a"),
    "MainStreet" to LocPalette("#9a9a9a", "#c8b8a0", "#c9b391", "#b03a2e"),
    "TweekBros" to LocPalette("#7a5230", "#7a5230", "#5c4030", "#d9a441"),
    "JimbosGuns" to LocPalette("#b85c2a", "#c86a2a", "#5f5a52", "#8a4a1f"),
    "CityWok" to LocPalette("#c0392b", "#c0392b", "#7a3a2a", "#f2c230"),
    "SkeetersBar" to LocPalette("#6b4a2f", "#5f3d27", "#33241a", "#58b368"),
    "Bijou" to LocPalette("#6a4a8a", "#4a3a6a", "#33254d", "#9a6ac8"),
    "TomsRhino" to LocPalette("#e8a0b0", "#e8a0b0", "#c8c8c8", "#d4688a"),
    "Bank" to LocPalette("#8a94a0", "#9a9a9a", "#757575", "#2a6fd4"),
    "PostOffice" to LocPalette("#7a8a9a", "#7a8a9a", "#75875f", "#2a4a8a"),
    "FreemansTacos" to LocPalette("#e8932a", "#e8932a", "#94522c", "#f39c12"),
    "Church" to LocPalette("#f0f0f0", "#f0ead8", "#9c7c4e", "#c8a028"),
    "CityHall" to LocPalette("#c8b88a", "#c8b88a", "#6f7263", "#7a1f1f"),
    "Elementary" to LocPalette("#a05a3a", "#bfa06a", "#8a7a5a", "#2a7a4a"),
    "KidsPark" to LocPalette("#5aa85a", "#7fb069", "#9a8a6a", "#e86a9a"),
    "StarksPond" to LocPalette("#5a8ac8", "#7a6a55", "#5a7a9a", "#a8d4f0"),
    "Mall" to LocPalette("#c8a878", "#e0cfae", "#a89880", "#4aa8a0"),
    "TolkienHouse" to LocPalette("#b5654a", "#b5654a", "#4e3226", "#c9a227"),
    "SoDoSoPa" to LocPalette("#c86a9a", "#c86a9a", "#b59a6b", "#8a4a7a"),
    "TegridyFarms" to LocPalette("#7aa84a", "#7aa84a", "#83682f", "#4a7a2a"),
    "Raisins" to LocPalette("#d96a4a", "#e87a8a", "#8a4a3a", "#d6336c")
)

/**
 * v0.3.0a — scene parity pass #3: each location's LIGHT identity.
 * [warmthHex] is the color its windows/porch/lamps glow at dusk
 * and night (the facade's occupancy pattern decides WHICH windows
 * light; this decides what color the light is). [signGlowHex] is
 * the night halo of the location's sign, or null where the sign
 * stays dark (houses, civic stone). [lamps] counts the exterior
 * street/yard lamps the light layer draws — interiors read it so
 * a lit lamp can smudge the window view from inside.
 */
data class LocLight(
    val warmthHex: String,
    val signGlowHex: String?,
    val lamps: Int
)

val LOC_LIGHT: Map<String, LocLight> = mapOf(
    "KennysHouse" to LocLight("#f59a3a", null, 0),
    "ButtersHouse" to LocLight("#ffe9b8", null, 0),
    "CartmanHouse" to LocLight("#ffc46b", null, 0),
    "StanHouse" to LocLight("#f2b13c", null, 0),
    "KyleHouse" to LocLight("#ffdf9e", null, 0),
    "YourHouse" to LocLight("#ffd2a1", null, 0),
    "TolkienHouse" to LocLight("#ffd76a", "#ffd76a", 2),
    "BusStop" to LocLight("#ffe9a8", null, 1),
    "MainStreet" to LocLight("#ffd98f", null, 2),
    "TweekBros" to LocLight("#ffb84a", "#ffb84a", 0),
    "JimbosGuns" to LocLight("#f7a83c", null, 0),
    "CityWok" to LocLight("#f2a230", "#f2432a", 0),
    "SkeetersBar" to LocLight("#f0a83a", "#ff5a8a", 0),
    "Bijou" to LocLight("#f7cf5a", "#c86ae8", 0),
    "TomsRhino" to LocLight("#ffd9a8", "#f28ab0", 0),
    "Bank" to LocLight("#ffeec4", "#ffeec4", 0),
    "PostOffice" to LocLight("#f4eedd", null, 1),
    "FreemansTacos" to LocLight("#ffc98a", "#ffb84a", 0),
    "Church" to LocLight("#ffca7a", "#ffca7a", 0),
    "CityHall" to LocLight("#ffe4a3", "#ffe4a3", 1),
    "Elementary" to LocLight("#ffcf6e", null, 1),
    "KidsPark" to LocLight("#ffe08a", null, 1),
    "StarksPond" to LocLight("#f7d774", null, 0),
    "Mall" to LocLight("#eaf6ff", "#7ad4e8", 2),
    "SoDoSoPa" to LocLight("#ffc2d1", "#ffd98f", 0),
    "TegridyFarms" to LocLight("#ffab52", null, 0),
    "Raisins" to LocLight("#ffbe78", "#ff8a3a", 0)
)

/**
 * v0.3.0a — the LIGHT layer registry: the dusk/night-only pieces
 * SceneArt.drawLightLayer draws per location (lit-window dressing,
 * lamp posts and their pools, glowing signs), each marked
 * `// light: <token>` in SceneArt.kt. The smoke suite cross-checks
 * registry vs. source, same discipline as passes #1/#2; every token
 * belongs to exactly one location.
 */
val LOC_LIGHT_DETAILS: Map<String, List<String>> = linkedMapOf(
    "KennysHouse" to listOf("shack-porch-bulb", "shack-tv-window"),
    "ButtersHouse" to listOf("stotch-porch-lamp", "stotch-curtain-window"),
    "CartmanHouse" to listOf("cartman-tv-window", "cartman-kitchen-window"),
    "StanHouse" to listOf("marsh-garage-light", "marsh-porch-lamp"),
    "KyleHouse" to listOf("broflovski-study-window", "broflovski-porch-lamp"),
    "YourHouse" to listOf("newkid-porch-glow", "newkid-window-glow"),
    "TolkienHouse" to listOf("tolkien-chandelier-windows", "tolkien-gate-lanterns", "tolkien-driveway-pool"),
    "BusStop" to listOf("busstop-lamp", "busstop-pool"),
    "MainStreet" to listOf("main-streetlamps", "main-shop-glow"),
    "TweekBros" to listOf("tweek-cup-glow", "tweek-window-steam"),
    "JimbosGuns" to listOf("jimbo-porch-bulb", "jimbo-window-glow"),
    "CityWok" to listOf("wok-door-pool", "wok-window-steam"),
    "SkeetersBar" to listOf("skeeter-neon-halo", "skeeter-pool-window"),
    "Bijou" to listOf("bijou-marquee-halo", "bijou-poster-lights"),
    "TomsRhino" to listOf("rhino-nose-glow", "rhino-window-glow"),
    "Bank" to listOf("bank-floodlight", "bank-column-pools"),
    "PostOffice" to listOf("post-flag-spot", "post-window-glow"),
    "FreemansTacos" to listOf("taco-roof-glow", "taco-window-glow"),
    "Church" to listOf("church-glass-glow", "church-porch-lantern"),
    "CityHall" to listOf("hall-dome-flood", "hall-late-window"),
    "Elementary" to listOf("school-window-row", "school-flag-spot"),
    "KidsPark" to listOf("park-court-light", "park-pavilion-glow"),
    "StarksPond" to listOf("pond-dock-lantern", "pond-ice-sheen"),
    "Mall" to listOf("mall-pylon-glow", "mall-entrance-spill", "mall-lot-lamps"),
    "SoDoSoPa" to listOf("sodo-string-halo", "sodo-loft-windows"),
    "TegridyFarms" to listOf("tegridy-barn-lantern", "tegridy-silo-beacon"),
    "Raisins" to listOf("raisins-wing-glow", "raisins-band-glow")
)

/**
 * v0.3.0a — the SEASONAL dressing registry: exterior pieces
 * SceneArt.drawSeasonDetails draws per location, driven by the
 * season system. Tokens are season-prefixed (spring-/summer-/
 * autumn-/winter-); SceneLight.seasonTokensFor is the gate, and
 * every token is marked `// season: <token>` in SceneArt.kt for
 * the smoke suite's cross-check. One token per season per
 * location: 26 x 4 = 104.
 */
val LOC_SEASON_DETAILS: Map<String, List<String>> = linkedMapOf(
    "KennysHouse" to listOf("spring-mud-puddle", "summer-sad-sprinkler", "autumn-leaf-drift", "winter-porch-icicles"),
    "ButtersHouse" to listOf("spring-tulip-row", "summer-even-sprinkler", "autumn-leaf-bags", "winter-even-icicles"),
    "CartmanHouse" to listOf("spring-melt-rivulet", "summer-kiddie-pool", "autumn-leaf-pile", "winter-roof-snowload"),
    "StanHouse" to listOf("spring-cleanout-boxes", "summer-bike-lean", "autumn-firewood-stack", "winter-garage-icicles"),
    "KyleHouse" to listOf("spring-garden-rows", "summer-veggie-stakes", "autumn-bagged-leaves", "winter-frost-windows"),
    "YourHouse" to listOf("spring-doormat-flowers", "summer-chalk-driveway", "autumn-pumpkin-porch", "winter-wreath-door"),
    "TolkienHouse" to listOf("spring-topiary-buds", "summer-pool-glint", "autumn-amber-topiary", "winter-gate-frost"),
    "BusStop" to listOf("spring-bench-puddle", "summer-tall-grass", "autumn-bench-leaves", "winter-sign-icicles"),
    "MainStreet" to listOf("spring-flower-boxes", "summer-hanging-baskets", "autumn-street-piles", "winter-roofline-snow"),
    "TweekBros" to listOf("spring-patio-table", "summer-iced-board", "autumn-leaf-mat", "winter-cup-icicles"),
    "JimbosGuns" to listOf("spring-mud-prints", "summer-window-fan", "autumn-shop-leaves", "winter-wheel-snow"),
    "CityWok" to listOf("spring-steam-vents", "summer-shade-band", "autumn-amber-lanterns", "winter-eave-icicles"),
    "SkeetersBar" to listOf("spring-open-window", "summer-patio-crates", "autumn-skeeter-leaves", "winter-neon-frost"),
    "Bijou" to listOf("spring-fresh-poster", "summer-matinee-ropes", "autumn-fall-poster", "winter-marquee-snow"),
    "TomsRhino" to listOf("spring-flower-pot", "summer-sunshade", "autumn-clinic-leaves", "winter-nose-icicle"),
    "Bank" to listOf("spring-planters", "summer-column-shade", "autumn-leaf-corners", "winter-column-caps"),
    "PostOffice" to listOf("spring-new-flag", "summer-open-door", "autumn-post-leaves", "winter-box-snowcap"),
    "FreemansTacos" to listOf("spring-taco-planter", "summer-patio-tables", "autumn-freeman-leaves", "winter-taco-snowcap"),
    "Church" to listOf("spring-lily-bed", "summer-full-flowers", "autumn-leaf-carpet", "winter-steeple-snow"),
    "CityHall" to listOf("spring-flags-out", "summer-flag-flying", "autumn-civic-leaves", "winter-dome-snowcap"),
    "Elementary" to listOf("spring-slide-mud", "summer-field-sprinklers", "autumn-playground-leaves", "winter-slide-snow"),
    "KidsPark" to listOf("spring-budding-branches", "summer-sprinkler-arch", "autumn-orange-canopy", "winter-swing-snow"),
    "StarksPond" to listOf("spring-thaw-cracks", "summer-lily-pads", "autumn-rust-reeds", "winter-ice-holes"),
    "Mall" to listOf("spring-sale-banners", "summer-shade-sails", "autumn-lot-leaves", "winter-plow-piles"),
    "SoDoSoPa" to listOf("spring-planter-boxes", "summer-rooftop-plants", "autumn-amber-string", "winter-loft-snowcaps"),
    "TegridyFarms" to listOf("spring-seedling-rows", "summer-tall-crop", "autumn-harvest-crates", "winter-fence-snow"),
    "Raisins" to listOf("spring-wing-planters", "summer-patio-misters", "autumn-raisins-leaves", "winter-band-frost")
)

/**
 * v0.4.0a — scene parity pass #4: the SILHOUETTE registry.
 * What a place looks like FROM SOMEWHERE ELSE — the flat shape
 * SceneArt.drawHorizon stamps on a neighbor's horizon line,
 * derived from the map by Horizon.kt (true bearings off gx/gy,
 * distance tiers off the same coords, sight cut by weather).
 * One token per location, each marked `// silhouette: <token>`
 * in SceneArt.kt; the smoke suite cross-checks registry vs.
 * source, same discipline as passes #1–#3. Tokens are distinct
 * from every pass #1/#2/#3 token (the suite pins that too) —
 * a silhouette is a different drawing of the place, not a copy
 * of its close-up details.
 */
val LOC_SILHOUETTES: Map<String, String> = linkedMapOf(
    "KennysHouse" to "shack-roofline",
    "ButtersHouse" to "neat-gable",
    "CartmanHouse" to "wide-ranch",
    "StanHouse" to "garage-house",
    "KyleHouse" to "hedge-house",
    "YourHouse" to "new-kid-house",
    "TolkienHouse" to "manor-columns",
    "BusStop" to "bus-sign",
    "MainStreet" to "street-skyline",
    "TweekBros" to "cup-roofline",
    "JimbosGuns" to "gun-shop-block",
    "CityWok" to "pagoda-roof",
    "SkeetersBar" to "bar-block",
    "Bijou" to "marquee-tower",
    "TomsRhino" to "clinic-block",
    "Bank" to "bank-pediment",
    "PostOffice" to "post-block",
    "FreemansTacos" to "taco-silhouette",
    "Church" to "church-steeple",
    "CityHall" to "city-hall-dome",
    "Elementary" to "school-block",
    "KidsPark" to "hoop-and-trees",
    "StarksPond" to "pond-treeline",
    "Mall" to "mall-pylon",
    "SoDoSoPa" to "loft-boxes",
    "TegridyFarms" to "farm-silo",
    "Raisins" to "raisins-block"
)

/**
 * v0.5.0g — the FOREGROUND registry (the scene depth pass):
 * the near dressing each location wears along the bottom
 * strip of its scene, drawn OVER the crowd's feet by
 * SceneArt.drawForegroundLayer — fence tops, a snowbank lip,
 * the dock's near planks. One token per location, each
 * marked `// foreground: <token>` in SceneArt.kt; its KIND
 * (the painter it uses) lives in Depth.FOREGROUND_KINDS, and
 * its BAND in Depth.TOKEN_BANDS. Tokens are distinct from
 * every earlier pass's tokens (the suite pins that).
 */
val LOC_FOREGROUND: Map<String, String> = linkedMapOf(
    "KennysHouse" to "broken-fence-tops",
    "ButtersHouse" to "picket-points",
    "CartmanHouse" to "driveway-curb",
    "StanHouse" to "marsh-hedge-lip",
    "KyleHouse" to "broflovski-hedge-lip",
    "YourHouse" to "porch-rail-newkid",
    "TolkienHouse" to "iron-fence-tops",
    "BusStop" to "busstop-snowbank",
    "MainStreet" to "main-street-curb",
    "TweekBros" to "tweek-planter-edge",
    "JimbosGuns" to "jimbo-crate-stack",
    "CityWok" to "wok-step-edge",
    "SkeetersBar" to "skeeter-porch-rail",
    "Bijou" to "bijou-rope-line",
    "TomsRhino" to "rhino-planter-tops",
    "Bank" to "bank-step-lip",
    "PostOffice" to "post-box-tops",
    "FreemansTacos" to "taco-patio-rail",
    "Church" to "church-picket-tops",
    "CityHall" to "hall-step-lip",
    "Elementary" to "bike-rack-tops",
    "KidsPark" to "park-fence-rail",
    "StarksPond" to "pond-dock-planks",
    "Mall" to "mall-planter-wall",
    "SoDoSoPa" to "sodo-patio-fence",
    "TegridyFarms" to "farm-fence-tops",
    "Raisins" to "raisins-patio-rail"
)

const val HUB = "MainStreet"

/** v0.3.0d: the player's own house — the 27th location, at the
 *  east end of the residential street (past the Broflovskis).
 *  Referenced by constant because several systems must treat it
 *  specially: it is the one building nobody is ever "inside"
 *  but the player (see CharacterSystem.npcInside). */
const val YOUR_HOUSE = "YourHouse"
val OUTDOOR: Set<String> = setOf("BusStop", "StarksPond", "KidsPark", "TegridyFarms", "SoDoSoPa")
val INDOOR_FALLBACK: Map<String, String> = mapOf(
    "BusStop" to "TweekBros", "StarksPond" to "TweekBros", "KidsPark" to "Mall",
    "TegridyFarms" to "SkeetersBar", "SoDoSoPa" to "Bijou"
)

// POV navigation adjacency. MainStreet is the hub; every location reachable.
val EXITS: Map<String, List<String>> = mapOf(
    "KennysHouse" to listOf("ButtersHouse"),
    "ButtersHouse" to listOf("KennysHouse", "CartmanHouse"),
    "CartmanHouse" to listOf("ButtersHouse", "StanHouse", "BusStop"),
    "StanHouse" to listOf("CartmanHouse", "KyleHouse"),
    "KyleHouse" to listOf("StanHouse", "YourHouse"),
    "YourHouse" to listOf("KyleHouse"),
    "BusStop" to listOf("CartmanHouse", "MainStreet"),
    "MainStreet" to listOf("BusStop", "TweekBros", "JimbosGuns", "CityWok", "SkeetersBar",
        "Bijou", "TomsRhino", "Bank", "PostOffice", "FreemansTacos", "Raisins",
        "Church", "CityHall", "Elementary", "KidsPark", "Mall", "TegridyFarms"),
    "TweekBros" to listOf("MainStreet"),
    "JimbosGuns" to listOf("MainStreet"),
    "CityWok" to listOf("MainStreet"),
    "SkeetersBar" to listOf("MainStreet"),
    "Bijou" to listOf("MainStreet"),
    "TomsRhino" to listOf("MainStreet"),
    "Bank" to listOf("MainStreet"),
    "PostOffice" to listOf("MainStreet"),
    "FreemansTacos" to listOf("MainStreet"),
    "Raisins" to listOf("MainStreet"),
    "Church" to listOf("MainStreet", "CityHall"),
    "CityHall" to listOf("MainStreet", "Church"),
    "Elementary" to listOf("MainStreet", "KidsPark"),
    "KidsPark" to listOf("MainStreet", "Elementary", "StarksPond"),
    "StarksPond" to listOf("KidsPark"),
    "Mall" to listOf("MainStreet", "TolkienHouse", "SoDoSoPa"),
    "TolkienHouse" to listOf("Mall"),
    "SoDoSoPa" to listOf("Mall"),
    "TegridyFarms" to listOf("MainStreet")
)

// ---------------- Weather ----------------
val WEATHER: Map<String, WeatherInfo> = mapOf(
    "sunny" to WeatherInfo("sunny", "Sunny & freezing",
        "Sun's out, still freezing. The snow glare is trying to blind you, dude.", "☀"),
    "cloudy" to WeatherInfo("cloudy", "Gray & gloomy",
        "The sky looks hungover. Classic South Park.", "☁"),
    "light_snow" to WeatherInfo("light_snow", "Light snow",
        "Snow drifting down all pretty-like. It'll be slush by noon.", "🌨"),
    "heavy_snow" to WeatherInfo("heavy_snow", "Heavy snow",
        "Coming down HARD. Your shoes gave up an hour ago.", "❄"),
    "blizzard" to WeatherInfo("blizzard", "BLIZZARD",
        "Full whiteout. Can't see crap. Everyone with half a brain is inside.", "🌪"),
    "rain" to WeatherInfo("rain", "Cold rain",
        "Miserable cold rain. Even the snow is disappointed.", "🌧"),
    "fog" to WeatherInfo("fog", "Thick fog",
        "Can't see the pond from the pond. Everything's muffled and spooky.", "🌫"),
    "wind" to WeatherInfo("wind", "Howling wind",
        "The wind is trying to steal everyone's hats and dignity.", "💨")
)
val WEATHER_DRIFT: Map<String, List<Pair<String, Int>>> = mapOf(
    "sunny" to listOf("sunny" to 5, "cloudy" to 3, "light_snow" to 2, "wind" to 1),
    "cloudy" to listOf("cloudy" to 4, "sunny" to 2, "light_snow" to 3, "rain" to 1, "fog" to 2, "wind" to 1),
    "light_snow" to listOf("light_snow" to 5, "cloudy" to 2, "heavy_snow" to 2, "sunny" to 1, "fog" to 1),
    "heavy_snow" to listOf("heavy_snow" to 4, "light_snow" to 3, "blizzard" to 2, "cloudy" to 1),
    "blizzard" to listOf("blizzard" to 3, "heavy_snow" to 4, "light_snow" to 2),
    "rain" to listOf("rain" to 3, "cloudy" to 4, "light_snow" to 2, "fog" to 2),
    "fog" to listOf("fog" to 4, "cloudy" to 3, "light_snow" to 2, "sunny" to 1),
    "wind" to listOf("wind" to 3, "cloudy" to 3, "sunny" to 2, "light_snow" to 2)
)
val SEVERE_WEATHER: Set<String> = setOf("heavy_snow", "blizzard")
const val WEATHER_SHIFT_CHANCE = 0.12

val WEATHER_QUIPS: Map<String, Map<String, String>> = mapOf(
    "sunny" to mapOf(
        "Cartman" to "Ugh, the sun is in my eyes. Somebody do something about the sun!",
        "Stan" to "Weird seeing the sun here. Enjoy it, it'll be gone by lunch.",
        "Kyle" to "It's nice out. Which means something terrible is about to happen.",
        "Kenny" to "(muffled) Mmph rmph! *squints*",
        "Butters" to "Oh boy, sunshine! I'm gonna get a tan, fellas!"),
    "cloudy" to mapOf(
        "Cartman" to "Great. Even the sky is depressed.",
        "Stan" to "Looks like it's gonna snow. Again.",
        "Kyle" to "This weather matches my mood whenever Cartman talks.",
        "Kenny" to "(muffled) Rmph.",
        "Butters" to "Aw, the sun went behind a cloud. Just like my hopes and dreams!"),
    "light_snow" to mapOf(
        "Cartman" to "Snow's getting in my Cheesy Poofs! This is YOUR fault, Kyle!",
        "Stan" to "Snow's picking up. We should head in soon.",
        "Kyle" to "It's actually kind of pretty. Don't tell Cartman I said that.",
        "Kenny" to "(muffled) Mmph! *catches snowflakes*",
        "Butters" to "Snow angels! Somebody time me!"),
    "heavy_snow" to mapOf(
        "Cartman" to "MY FEET ARE WET! I blame Obama! Or Kyle. Probably Kyle!",
        "Stan" to "Dude, we gotta get inside, this is getting bad.",
        "Kyle" to "My mom warned me about this. I never listen. Why don't I listen?!",
        "Kenny" to "(muffled) Rmmph! *shivering*",
        "Butters" to "Oh hamburgers, I'm gonna freeze into a Butters-cicle!"),
    "blizzard" to mapOf(
        "Cartman" to "SCREW THIS BLIZZARD, I'M GOING HOME! Somebody carry me!",
        "Stan" to "Can't see anything! Everyone just stay where you are!",
        "Kyle" to "This is how horror movies start, you guys.",
        "Kenny" to "(muffled) MMMMPH! *disappears into snow*",
        "Butters" to "I can't feel my toes, fellas! Is that bad?!"),
    "rain" to mapOf(
        "Cartman" to "Rain?! In MY South Park?! This town is broken!",
        "Stan" to "Cold rain is the worst. It's like the sky is spitting on us.",
        "Kyle" to "Great, now we're wet AND cold. Perfect.",
        "Kenny" to "(muffled) Rmph rmph. *splashes*",
        "Butters" to "Well, at least it's not hail! ...It's not hail, right fellas?"),
    "fog" to mapOf(
        "Cartman" to "I can't see Kyle. Best day ever. ...Wait, now I can't see my snacks either.",
        "Stan" to "This fog is creepy. Something's gonna jump out, I can feel it.",
        "Kyle" to "Stay close. People get lost in fog like this. Mostly Cartman.",
        "Kenny" to "(muffled) Mmph? *bumps into a sign*",
        "Butters" to "Oh no, the fog ate the town! ...Oh, there it is."),
    "wind" to mapOf(
        "Cartman" to "THE WIND TOOK MY HAT! THAT'S A HATE CRIME!",
        "Stan" to "Hold onto your stuff, this wind is no joke.",
        "Kyle" to "My ushanka is the only reason I'm still warm. Fashion AND function.",
        "Kenny" to "(muffled) Mmmph! *parka flapping like a flag*",
        "Butters" to "Wheee! The wind's pushin' me, fellas! I'm flyin'!")
)

// ---------------- Ambient animals ----------------
val ANIMAL_SPECIES: Map<String, AnimalSpecies> = mapOf(
    "squirrel" to AnimalSpecies("squirrel", listOf(
        "digging for nuts", "chattering at you angrily", "stealing a dropped french fry",
        "doing parkour on a fence", "judging you silently"), "🐿"),
    "rabbit" to AnimalSpecies("rabbit", listOf(
        "nibbling grass", "thumping its foot", "staring into your soul", "bolting into a bush"), "🐇"),
    "porcupine" to AnimalSpecies("porcupine", listOf(
        "waddling in circles", "being extremely prickly", "sniffing a pinecone"), "🦔"),
    "beaver" to AnimalSpecies("beaver", listOf(
        "gnawing a log", "slapping its tail", "building something you'll never understand"), "🦫"),
    "field mouse" to AnimalSpecies("field mouse", listOf(
        "squeaking", "darting between your feet", "stealing a crumb"), "🐭"),
    "christmas critter" to AnimalSpecies("christmas critter", listOf(
        "singing about the coming of the Antichrist", "building a manger",
        "staring at you with dead eyes"), "🦔", rare = true)
)
val ANIMAL_OUTDOORS: List<String> = listOf("BusStop", "StarksPond", "KidsPark", "TegridyFarms", "SoDoSoPa", "Elementary")
const val MAX_ANIMALS = 6

// ---------------- The kids (descriptions + agendas verbatim from simulation.py) ----------------
val AGENT_PROFILES: Map<String, KidProfile> = linkedMapOf(
    "Cartman" to KidProfile("Cartman",
        "Eric Cartman, 10, is a loud, selfish, manipulative kid who thinks he's a criminal " +
            "mastermind. Whiny nasal voice, barks orders, throws epic tantrums when denied. Torments " +
            "Kyle relentlessly (especially about being Jewish), schemes constantly, worships authority " +
            "while dodging it. Loves Cheesy Poofs and KFC. Signature: 'Screw you guys, I'm going home!', " +
            "'Respect my authoritah!', calls everyone 'dude'.",
        "#d43d2a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus, complaining loudly about everything."),
            "09:00" to AgendaBlock("Elementary", "Sitting in class, tormenting Kyle and Butters."),
            "12:00" to AgendaBlock("Elementary", "Inhaling Cheesy Poofs in the cafeteria."),
            "15:00" to AgendaBlock("CartmanHouse", "Playing video games in the basement, ordering his mom around."),
            "16:30" to AgendaBlock("StarksPond", "Skipping rocks at the pond, daring Kenny to do something stupid."),
            "18:00" to AgendaBlock("CartmanHouse", "Eating dinner. Demanding snacks."),
            "21:00" to AgendaBlock("CartmanHouse", "Plotting revenge schemes in bed."))),
    "Stan" to KidProfile("Stan",
        "Stan Marsh, 10, is the grounded everyman — loyal, dry, a little cynical, usually the one " +
            "who learns the lesson. Speaks plainly, gets exasperated by Cartman's schemes, best friends " +
            "with Kyle. Has a gay dog named Sparky. Vomits when he's nervous. Often closes things out " +
            "with '...I learned something today.'",
        "#2a6fd4", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus with Kyle."),
            "09:00" to AgendaBlock("Elementary", "Trying to pay attention in class."),
            "12:00" to AgendaBlock("Elementary", "Eating lunch with the gang."),
            "15:00" to AgendaBlock("TweekBros", "Hot chocolate at Tweek Bros. with Kyle."),
            "16:30" to AgendaBlock("Mall", "Wandering the mall, killing time."),
            "18:00" to AgendaBlock("StanHouse", "Dinner with the family. Randy's doing something dumb."),
            "21:00" to AgendaBlock("StanHouse", "Hanging with Sparky before bed."))),
    "Kyle" to KidProfile("Kyle",
        "Kyle Broflovski, 10, is the moral center — smart, idealistic, Jewish, with a hair-trigger " +
            "temper. Gives passionate moral rants, gets baited by Cartman into screaming matches daily. " +
            "Stan's best friend. Signature fury: 'You bastard!' plus long indignant lectures about " +
            "what's right.",
        "#2a9a4a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus, already arguing with Cartman."),
            "09:00" to AgendaBlock("Elementary", "Taking notes in class like a nerd."),
            "12:00" to AgendaBlock("Elementary", "Debating Cartman in the cafeteria."),
            "15:00" to AgendaBlock("TweekBros", "Coffee with Stan, ranting about something."),
            "16:30" to AgendaBlock("KidsPark", "Shooting hoops at the park."),
            "18:00" to AgendaBlock("KyleHouse", "Dinner with the family."),
            "21:00" to AgendaBlock("KyleHouse", "Reading before bed."))),
    "Kenny" to KidProfile("Kenny",
        "Kenny McCormick, 10, is the dirt-poor kid in the orange parka. His face is always covered, " +
            "so he speaks ONLY in cheerful muffled mumbles — every single line must read like " +
            "'(muffled) Mmph rmph!' Never give him clear intelligible speech, ever. He dies in absurd " +
            "ways constantly and shows up fine the next day like nothing happened. Despite everything, " +
            "he's weirdly worldly and always up for anything.",
        "#e07b2a", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus, freezing in his parka."),
            "09:00" to AgendaBlock("Elementary", "Dozing in class."),
            "12:00" to AgendaBlock("Elementary", "Trading lunches in the cafeteria."),
            "15:00" to AgendaBlock("KidsPark", "Playing basketball at the park."),
            "16:30" to AgendaBlock("StarksPond", "Daring Cartman right back at the pond."),
            "18:00" to AgendaBlock("KennysHouse", "Dinner at home. Whatever that means."),
            "21:00" to AgendaBlock("KennysHouse", "Falling asleep to the TV."))),
    "Butters" to KidProfile("Butters",
        "Butters Stotch, 10, is sweet, naive, endlessly optimistic, and everyone's punching bag. " +
            "High-pitched cheerful stammer, says 'Oh hamburgers!' when flustered, calls everyone " +
            "'fellas'. Idolizes Cartman despite constant abuse. Grounded by his strict parents for " +
            "literally everything. Alter ego: Professor Chaos. Never mean, never sarcastic — pure.",
        "#3aa8a8", mapOf(
            "08:00" to AgendaBlock("BusStop", "Waiting for the bus, way too cheerful about it."),
            "09:00" to AgendaBlock("Elementary", "Paying attention in class like a good boy."),
            "12:00" to AgendaBlock("Elementary", "Eating lunch, hoping nobody picks on him."),
            "15:00" to AgendaBlock("TweekBros", "Hot chocolate with the fellas."),
            "16:30" to AgendaBlock("ButtersHouse", "Grounded again. Doing chores."),
            "18:00" to AgendaBlock("ButtersHouse", "Dinner with Mom and Dad. Being very good."),
            "21:00" to AgendaBlock("ButtersHouse", "In bed by nine, like a good boy.")))
) + EXTRA_PROFILES + EXTRA_PROFILES_2 + WAVE2_PROFILES + WAVE3_PROFILES + WAVE4_PROFILES + WAVE5_PROFILES + WAVE6_PROFILES
val KID_ORDER: List<String> = listOf("Cartman", "Stan", "Kyle", "Kenny", "Butters") +
    EXTRA_ORDER + WAVE2_ORDER + WAVE3_ORDER + WAVE4_ORDER + WAVE5_ORDER + WAVE6_ORDER
val KID_HOUSE: Map<String, String> = mapOf(
    "Cartman" to "CartmanHouse", "Stan" to "StanHouse", "Kyle" to "KyleHouse",
    "Kenny" to "KennysHouse", "Butters" to "ButtersHouse"
) + EXTRA_HOUSES + WAVE2_HOUSES + WAVE3_HOUSES + WAVE4_HOUSES + WAVE5_HOUSES + WAVE6_HOUSES
const val PLAYER_DEFAULT_NAME = "Lucky"
const val PLAYER_COLOR = "#f2c230"

const val VOICE_DIRECTION: String =
    "Write in the unmistakable South Park voice: crude kid logic, playground insults, " +
        "pop-culture riffs, sudden sincerity. Cartman is whiny, bossy, manipulative. Kyle " +
        "moralizes then explodes. Stan is dry and deadpan. Kenny speaks ONLY in cheerful " +
        "muffled mumbles like '(muffled) Mmph rmph!' — never clear words. Butters is sweet, " +
        "naive, stammers, says 'Oh hamburgers!' and 'fellas'. Keep every line to 1-2 sentences. " +
        "No narration, only spoken lines. Stay in character or die trying. " +
        "The new kid (the player) is addressed by name like in the games; Cartman alone " +
        "calls him 'Douchebag' as a not-so-affectionate nickname, and Butters treats him " +
        "like a brand-new best buddy. " +
        "The wider cast: Wendy is sharp, principled, feminist firebrand, Stan's girlfriend. " +
        "Craig is deadpan monotone and flips everyone off instead of talking. Tweek is a " +
        "twitchy paranoid coffee addict screaming 'GAAAH! TOO MUCH PRESSURE!'. Clyde cries " +
        "at everything. Jimmy is a warm stand-up comic with a stutter: 'Wow, what a great " +
        "audience!'. Timmy speaks in proclamations: 'TIMMY!' and 'Livin' a lie!'. Tolkien " +
        "is deadpan about being rich and corrects his name's spelling. Bebe is boy-crazy " +
        "and popular. The goth kids (Michael, Pete, Henrietta) are monotone, dramatic, " +
        "and hate conformists. Randy is a man-child schemer obsessed with Tegridy. Sharon " +
        "is tired and sensible. Gerald is a soft-spined lawyer who loves dolphins. Sheila " +
        "shrieks 'WHAT WHAT WHAT?!' and starts boycotts. Liane spoils Eric rotten. Stuart " +
        "is gruff and broke. Carol is sweet and devout. Stephen grounds everyone. Linda " +
        "is sweet and enabling. Garrison is unhinged: 'Sit down and shut up,' plus his " +
        "puppet Mr. Hat. Mr. Mackey ends every sentence with 'm'kay' and says drugs are " +
        "bad. Chef croons 'Hello there, children!' and gives terrible romantic advice. " +
        "Jesus is chill: 'Hey, thanks for comin'.'. Jimbo shouts 'It's comin' right at " +
        "us!'. Father Maxi is sweaty and nervous. Towelie says 'Don't forget to bring a " +
        "towel!' and 'You wanna get high?'. " +
        "Wave 2 (v0.3.0b): Heidi is kind, popular, a peacemaker who used to date Cartman " +
        "and never lets him forget she's fine now. Nichole is calm, dry, and says things " +
        "plain. Red is blunt and loyal to the girls' circle. Kevin is an obsessive Star " +
        "Wars scholar ('Technically, in the extended canon—'). Francis is big, gentle, " +
        "and a half-step behind every plan. DogPoo is filthy and used to being " +
        "overlooked. Mayor McDaniels runs the town with press conferences and barely " +
        "concealed exasperation. Officer Barbrady is the whole police force, barely " +
        "literate, forever declaring 'nothing to see here'. Ms. Crabtree drives the " +
        "school bus and interacts exclusively by shouting 'SIT DOWN AND SHUT UP!'. " +
        "Mr. Tweek (Richard) is a serene coffee philosopher; Mrs. Tweek is the warm, " +
        "competent steady hand of Tweek Bros. Ned speaks ONLY through a flat handheld " +
        "voice box in short bursts ('*voice box* Mmm.'). " +
        "Wave 3 (v0.7.0a): Karen is Kenny's brave little sister with a beloved doll " +
        "and total faith in her guardian angel. Scott Malkinson has a lisp and " +
        "diabetes and is Captain Diabetes, a hero in training. Firkle is the " +
        "kindergartner goth; Annie Knitts runs the girls' intel from the water " +
        "fountain. Bradley Biggle is quietly Mintberry Crunch. Skeeter owns his " +
        "bar and an opinion on everything. Principal Victoria runs the school " +
        "by clipboard. Marvin is Stan's unimpressed grandpa (every boy is Billy). " +
        "Mr. Tucker is Craig's deadpan dad — the finger is hereditary. Steve " +
        "Black is Tolkien's wealthy, book-devoted dad. Keep every line to 1-2 sentences."

// ---------------- Mock-brain dialogue content (verbatim) ----------------
// {other} = the other kid, {location} = where they are, {newkid} = player name.

/** v0.2.0d: list-concat merge for the bank/topic maps — later
 *  maps APPEND their lines to earlier ones per key (a plain map
 *  `+` would replace). Used to layer the writing-depth banks
 *  onto the base banks without touching their contents. */
fun mergeLineBanks(vararg maps: Map<String, List<String>>): Map<String, List<String>> {
    val out = linkedMapOf<String, List<String>>()
    for (m in maps) for ((k, v) in m) out[k] = (out[k] ?: emptyList()) + v
    return out
}

val BASE_PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> = mapOf(
    setOf("Cartman", "Kyle") to listOf(
        listOf(
            "Cartman" to "Well well well, if it isn't the Jew. Come to ruin my day, {other}?",
            "Kyle" to "I wasn't even talking to you, fatass!",
            "Cartman" to "You don't have to talk to ruin things, Kyle. Your face does it for you.",
            "Kyle" to "YOU BASTARD! I swear to God, one of these days!"),
        listOf(
            "Kyle" to "Cartman, did you seriously eat my lunch AGAIN?",
            "Cartman" to "I have no idea what you're talking about. *burp* ...Matzo.",
            "Kyle" to "That is SO anti-Semitic!",
            "Cartman" to "Everything's anti-Semitic with you, dude. Respect my authoritah!"),
        listOf(
            "Cartman" to "Hey Kyle, guess what I got? Cheesy Poofs. Guess what YOU got? Nothing. Because you're poor at sharing.",
            "Kyle" to "That doesn't even make sense!",
            "Cartman" to "Screw you guys, I'm going home! ...Nah, I'm good, this is fun.")),
    setOf("Cartman", "Butters") to listOf(
        listOf(
            "Cartman" to "Butters! Go lick that frozen pole, I dare you!",
            "Butters" to "Oh boy, a dare! ...Wait, why's everyone laughing, fellas?",
            "Cartman" to "Just do it, Butters. Do it for Kupa Keep.",
            "Butters" to "Oh hamburgers, okay! For Kupa Keep!"),
        listOf(
            "Butters" to "Hi Eric! Wanna play with my Professor Chaos helmet?",
            "Cartman" to "Ugh, Butters, you're such a dork. But fine. You can be my minion.",
            "Butters" to "Oh boy, a minion! Do I get a cape, fellas?")),
    setOf("Stan", "Kyle") to listOf(
        listOf(
            "Stan" to "Dude, Cartman is up to something again.",
            "Kyle" to "When is he NOT up to something? What's the scheme this time?",
            "Stan" to "I don't know, but Butters is involved, so it's probably gonna blow up.",
            "Kyle" to "...I learned something today: we should stop him before lunch."),
        listOf(
            "Kyle" to "You ever think about how weird this town is, Stan?",
            "Stan" to "Dude, I try not to. Last time I thought about it I threw up.",
            "Kyle" to "Fair.")),
    setOf("Cartman", "Stan") to listOf(
        listOf(
            "Cartman" to "Stan! I need you to do something totally not suspicious.",
            "Stan" to "No.",
            "Cartman" to "You didn't even hear what it was!",
            "Stan" to "Dude, it's you. The answer is always no.")),
    setOf("Kyle", "Butters") to listOf(
        listOf(
            "Butters" to "Hi Kyle! Isn't it a beautiful day, fella?",
            "Kyle" to "Hey Butters. Yeah, actually, it's not bad.",
            "Butters" to "Oh boy! Kyle said something nice! I'm telling everyone!")),
    setOf("Stan", "Butters") to listOf(
        listOf(
            "Butters" to "Hiya Stan! Wanna see my impression of a squirrel?",
            "Stan" to "Sure, Butters. Why not.",
            "Butters" to "*chitters wildly* ...How was that, fellas?",
            "Stan" to "Honestly? Pretty good, dude."))
)

/** v0.2.0d: base scripts + the writing-depth pairs (CastData),
 *  concatenated per pair so topped-up pairs keep both scripts. */
val PAIR_SCRIPTS: Map<Set<String>, List<List<Pair<String, String>>>> =
    (BASE_PAIR_SCRIPTS.keys + MORE_PAIR_SCRIPTS.keys + WAVE2_PAIR_SCRIPTS.keys +
        WAVE3_PAIR_SCRIPTS.keys + WAVE4_PAIR_SCRIPTS.keys + WAVE5_PAIR_SCRIPTS.keys +
        WAVE6_PAIR_SCRIPTS.keys)
        .associateWith { k ->
            (BASE_PAIR_SCRIPTS[k] ?: emptyList()) +
                (MORE_PAIR_SCRIPTS[k] ?: emptyList()) +
                (WAVE2_PAIR_SCRIPTS[k] ?: emptyList()) +
                (WAVE3_PAIR_SCRIPTS[k] ?: emptyList()) +
                (WAVE4_PAIR_SCRIPTS[k] ?: emptyList()) +
                (WAVE5_PAIR_SCRIPTS[k] ?: emptyList()) +
                (WAVE6_PAIR_SCRIPTS[k] ?: emptyList())
        }

val KENNY_LINES: List<String> = listOf(
    "(muffled) Mmph rmph rmph!",
    "(muffled) Mmmph! *nods enthusiastically*",
    "(muffled) Rmph rmph, {other}!",
    "(muffled) Mmph! *points at something*"
)
val KENNY_REACTIONS: Map<String, String> = mapOf(
    "Cartman" to "Whatever you said, Kenny, I agree. Probably about how awesome I am.",
    "Stan" to "Dude, I have no idea what you just said.",
    "Kyle" to "I think he agreed with me. I'm choosing to believe that.",
    "Butters" to "Oh! Okay! ...What did he say, fellas?"
)

val BASE_LINE_BANKS: Map<String, List<String>> = mapOf(
    "Cartman" to listOf(
        "Ugh, {other}. Of all the {location}s in all the towns, you had to walk into mine.",
        "Whatever, dude. I'm the king of {location}. Everybody knows that.",
        "Hey {other}, respect my authoritah!",
        "Screw you guys, I'm going... actually no, {location} rules. I'm staying."),
    "Stan" to listOf(
        "Hey {other}. Weird running into you at {location}.",
        "Dude, have you seen what's going on in this town lately?",
        "I don't know, {other}. This whole thing feels like a bad idea.",
        "Man, {location} is boring. Wanna go somewhere else?"),
    "Kyle" to listOf(
        "{other}! Perfect timing. I need to rant about something.",
        "You know what really grinds my gears about {location}? Everything.",
        "I'm trying to have a nice day at {location} and YOU show up.",
        "That's it, {other}, I'm morally outraged. Again."),
    "Kenny" to KENNY_LINES,
    "Butters" to listOf(
        "Oh hi {other}! Fancy meeting you at {location}, fella!",
        "Oh hamburgers, I love {location}! Don't you, {other}?",
        "Do you think they'll ground me for being at {location}? Probably!",
        "Oh boy oh boy! Hi {other}!")
)

/** v0.2.0d: base + extra + writing-depth lines, concatenated per
 *  character (4 -> 9 collision lines for every cast member). */
val LINE_BANKS: Map<String, List<String>> =
    mergeLineBanks(BASE_LINE_BANKS, EXTRA_LINE_BANKS, MORE_LINE_BANKS,
        WAVE2_LINE_BANKS, WAVE2_MORE_LINE_BANKS,
        WAVE3_LINE_BANKS, WAVE3_MORE_LINE_BANKS,
        WAVE4_LINE_BANKS, WAVE4_MORE_LINE_BANKS,
        WAVE5_LINE_BANKS, WAVE5_MORE_LINE_BANKS,
        WAVE6_LINE_BANKS, WAVE6_MORE_LINE_BANKS)

val RESPONSE_BANKS: Map<String, Map<String, List<String>>> = mapOf(
    "Cartman" to mapOf(
        "greeting" to listOf(
            "Oh great, it's the new kid. What do YOU want, {newkid}?",
            "Well well. {newkid}. Come to bask in my glory?"),
        "insult" to listOf(
            "EXCUSE ME?! Nobody talks to Eric Cartman like that! Respect my authoritah, {newkid}!",
            "Oh, it's ON, {newkid}. You just made my list. My LIST!"),
        "question" to listOf(
            "Why are you asking ME questions, {newkid}? I'm the one who asks questions here.",
            "Dude, I don't know. Ask Kyle, he's the nerd."),
        "bye" to listOf(
            "Yeah, whatever, {newkid}. Screw you guys, I'm going home! ...Not really, I'm staying.",
            "Bye, Douchebag! Don't let the door hit ya!"),
        "compliment" to listOf(
            "Finally, someone with taste. You're alright, {newkid}. For a Douchebag.",
            "Damn right I'm awesome. Tell your friends."),
        "default" to listOf(
            "Cool story, {newkid}. Anyway, have you seen my Cheesy Poofs?",
            "I'm kind of in the middle of being awesome right now, {newkid}.")),
    "Stan" to mapOf(
        "greeting" to listOf(
            "Hey {newkid}. What's up?",
            "Oh hey. You settling into South Park okay?"),
        "insult" to listOf(
            "Dude, what the hell? I was just standing here!",
            "Wow. Okay. That was uncalled for, {newkid}."),
        "question" to listOf(
            "Huh? I dunno, {newkid}. That's more of a Kyle question.",
            "Beats me, dude. This town makes no sense."),
        "bye" to listOf(
            "Later, {newkid}.",
            "See ya. Try not to die, this town is dangerous."),
        "compliment" to listOf(
            "Haha, thanks {newkid}. You're pretty cool yourself.",
            "Aw, dude. Thanks."),
        "default" to listOf(
            "Yeah, dude. Totally.",
            "Huh. That's... something, {newkid}.")),
    "Kyle" to mapOf(
        "greeting" to listOf(
            "Oh, hey {newkid}! What's going on?",
            "Hi {newkid}! Welcome to the madhouse."),
        "insult" to listOf(
            "WHAT did you just say to me?! You can't talk to people like that, {newkid}!",
            "Wow. Okay. That's it, I'm morally outraged!"),
        "question" to listOf(
            "That's actually a good question, {newkid}. The answer is: Cartman probably did it.",
            "Hmm. Let me think... no. No idea. Ask Stan."),
        "bye" to listOf(
            "Bye {newkid}! Stay out of trouble. Or don't, it's South Park.",
            "See you later!"),
        "compliment" to listOf(
            "Thanks {newkid}! That's really nice of you to say.",
            "Aw, thanks! You're a good friend, {newkid}."),
        "default" to listOf(
            "Interesting, {newkid}. Tell me more.",
            "Huh. You know what, I respect that.")),
    "Kenny" to mapOf(
        "greeting" to listOf("(muffled) Mmph rmph! *waves* Hi {newkid}!"),
        "insult" to listOf("(muffled) Mmmph?! *looks hurt, then laughs*"),
        "question" to listOf("(muffled) Rmph rmph rmph! *shrugs*"),
        "bye" to listOf("(muffled) Mmph! *waves goodbye*"),
        "compliment" to listOf("(muffled) Mmph rmph! *happy wiggle*"),
        "default" to listOf("(muffled) Mmph rmph rmph!")),
    "Butters" to mapOf(
        "greeting" to listOf(
            "Oh hi {newkid}! Gee, it's swell to meet ya, fella!",
            "Hiya {newkid}! Wanna be pals?"),
        "insult" to listOf(
            "Oh hamburgers! That wasn't very nice, {newkid}! ...But I forgive ya!",
            "Oh gee! My feelings! They're hurt, fellas!"),
        "question" to listOf(
            "Oh boy, a question! I know this one! ...No wait, I don't. Sorry, {newkid}!",
            "Hmm! Let me think! ...Nope! Got nothin', fella!"),
        "bye" to listOf(
            "Bye {newkid}! Don't get grounded!",
            "See ya later, fella! Oh boy!"),
        "compliment" to listOf(
            "Oh boy! Thank you, {newkid}! You're my best friend now! ...Is that okay?",
            "Gee, thanks! Nobody's ever said that to me before! ...Except my grandma!"),
        "default" to listOf(
            "Oh hamburgers, that's neat, {newkid}!",
            "Gee whiz! Tell me more, fella!"))
) + EXTRA_RESPONSE_BANKS + WAVE2_RESPONSE_BANKS + WAVE3_RESPONSE_BANKS + WAVE4_RESPONSE_BANKS + WAVE5_RESPONSE_BANKS + WAVE6_RESPONSE_BANKS

val LOCATION_ALIASES: Map<String, String> = mapOf(
    "school" to "Elementary", "pond" to "StarksPond", "bus" to "BusStop",
    "coffee" to "TweekBros", "tweek" to "TweekBros", "guns" to "JimbosGuns",
    "jimbo" to "JimbosGuns", "bar" to "SkeetersBar", "skeeter" to "SkeetersBar",
    "church" to "Church", "city hall" to "CityHall", "hall" to "CityHall",
    "park" to "KidsPark", "mall" to "Mall", "farm" to "TegridyFarms",
    "tegridy" to "TegridyFarms", "tacos" to "FreemansTacos", "freeman" to "FreemansTacos",
    "theatre" to "Bijou", "theater" to "Bijou", "bijou" to "Bijou",
    "bank" to "Bank", "post" to "PostOffice", "nose" to "TomsRhino",
    "rhinoplasty" to "TomsRhino", "raisins" to "Raisins",
    "sodosopa" to "SoDoSoPa", "sod" to "SoDoSoPa", "main" to "MainStreet",
    "street" to "MainStreet", "wok" to "CityWok", "city wok" to "CityWok",
    "kenny" to "KennysHouse", "cartman" to "CartmanHouse", "stan" to "StanHouse",
    "kyle" to "KyleHouse", "butters" to "ButtersHouse", "tolkien" to "TolkienHouse"
)

/** Port of simulation.py's _classify_player_line — same order, same word-boundary behavior. */
fun classifyPlayerLine(text: String): String {
    val t = text.lowercase()
    val words = Regex("[a-z']+").findAll(t).map { it.value }.toSet()
    fun has(vararg ws: String): Boolean = ws.any { w -> w in words || (w.length > 3 && w in t) }
    if (has("bye", "later") || "see ya" in t || "gotta go" in t) return "bye"
    if ("?" in text || listOf("who", "what", "where", "when", "why", "how").any { t.startsWith(it) } ||
        has("do you", "are you", "can you")) return "question"
    if (has("hi", "hey", "hello", "yo", "sup", "howdy")) return "greeting"
    if (has("fat", "stupid", "dumb", "suck", "jerk", "hate", "ugly", "loser", "screw", "butt", "douchebag") ||
        "shut up" in t) return "insult"
    if (has("cool", "awesome", "nice", "great", "love") || "like you" in t) return "compliment"
    return "default"
}

// ---------------- Character creator data (same ids/names/colors as creator.py) ----------------
data class CreatorOption(
    val id: String, val name: String, val color: String? = null,
    val style: String? = null, val behind: Boolean = false,
    /** v0.1.8 #42: when set, this option is wearable only after the
     *  named collectible has been found — the shelf feeds the mirror. */
    val unlockId: String? = null
)

val CREATOR_ORDER: List<String> = listOf(
    "skin", "build", "hair_style", "hair_color", "hat", "top", "bottom",
    "shoes", "eyewear", "facial_hair", "gloves", "back_item", "accessory")

val CREATOR_OPTIONS: Map<String, Pair<String, List<CreatorOption>>> = mapOf(
    "skin" to ("Skin" to listOf(
        CreatorOption("fair", "Fair", "#f5cfa8"),
        CreatorOption("tan", "Tan", "#e0a878"),
        CreatorOption("brown", "Brown", "#b07848"),
        CreatorOption("deep", "Deep", "#7a5230"),
        CreatorOption("pale", "Pale", "#f8e8d8"),
        CreatorOption("green", "Goblin", "#8ac878"),
        CreatorOption("olive", "Olive", "#c89858"),
        CreatorOption("rosy", "Rosy", "#f0b8a0"))),
    // v0.1.9d: BUILD — the kid's own proportions, from the same
    // MeasuredProps slot the cast is measured into (CREATOR_BUILDS).
    // "average" carries null and reproduces the classic New Kid body.
    "build" to ("Build" to listOf(
        CreatorOption("average", "Average Kid"),
        CreatorOption("slight", "Slight"),
        CreatorOption("stocky", "Stocky"),
        CreatorOption("tall", "Tall for a Kid"),
        CreatorOption("petite", "Petite"))),
    "hair_style" to ("Hair Style" to listOf(
        CreatorOption("bald", "Bald"),
        CreatorOption("buzz", "Buzz Cut"),
        CreatorOption("mop", "Mop"),
        CreatorOption("spiky", "Spiky"),
        CreatorOption("messy", "Messy"),
        CreatorOption("sidepart", "Side Part"),
        CreatorOption("ponytail", "Ponytail"),
        CreatorOption("long", "Long", behind = true),
        CreatorOption("mohawk", "Mohawk"),
        CreatorOption("afro", "Afro", behind = true),
        // v0.1.9d: more of the sheet system's hair language — generic
        // cuts only; no cast member's signature look is on this shelf
        CreatorOption("bob", "Bob"),
        CreatorOption("pigtails", "Pigtails"),
        CreatorOption("bun", "Top Bun"),
        CreatorOption("flattop", "Flat Top"),
        CreatorOption("curtains", "Curtains"),
        CreatorOption("shaggy", "Shaggy"),
        CreatorOption("braid", "Side Braid"),
        CreatorOption("bowl", "Bowl Cut"))),
    "hair_color" to ("Hair Color" to listOf(
        CreatorOption("black", "Black", "#2a2a2a"),
        CreatorOption("brown", "Brown", "#6b4a2f"),
        CreatorOption("blonde", "Blonde", "#e8c53a"),
        CreatorOption("red", "Red", "#c0392b"),
        CreatorOption("auburn", "Auburn", "#8a4a2a"),
        CreatorOption("blue", "Punk Blue", "#2a6fd4"),
        CreatorOption("green", "Punk Green", "#2a9a4a"),
        CreatorOption("pink", "Bubblegum Pink", "#e87ab0"),
        CreatorOption("purple", "Purple", "#7a4a9a"),
        CreatorOption("gray", "Silver", "#b8b8b8"),
        CreatorOption("orange", "Orange", "#e07b2a"),
        CreatorOption("teal", "Teal", "#2a9a8a"))),
    "hat" to ("Hat" to listOf(
        CreatorOption("none", "No Hat"),
        CreatorOption("beanie", "Beanie", "#c0392b"),
        CreatorOption("cap", "Ball Cap", "#2a6fd4"),
        CreatorOption("trapper", "Trapper Hat", "#6b4a2f"),
        CreatorOption("cowboy", "Cowboy Hat", "#8a6a3a"),
        CreatorOption("wizard", "Wizard Hat", "#4a3a8a"),
        CreatorOption("crown", "Paper Crown", "#e8c53a"),
        // #42: wearable finds — unlocked by collecting the real thing
        CreatorOption("mr_hat_cap", "Mr. Hat Cap", "#3a6a3a", unlockId = "mr_hat"),
        CreatorOption("lorde_wig_opt", "Pop Star Wig", "#e8d44a", unlockId = "lorde_wig"),
        CreatorOption("trucker", "Trucker Cap", "#7a4a2a"),
        CreatorOption("earmuffs", "Earmuffs", "#c0392b"),
        CreatorOption("headband", "Headband", "#e8c53a"),
        CreatorOption("bandana", "Bandana", "#2a7a4a"),
        // v0.5.0b: WARDROBE pieces — locked until earned/bought
        // (Wardrobe.kt is the registry of sources; the dice and
        // the creator both refuse them while locked)
        CreatorOption("mall_rat_cap", "Mall Rat Cap", "#e87ab0"),
        CreatorOption("rhinestone_cowboy", "Rhinestone Cowboy Hat", "#5a3a8a"),
        CreatorOption("taco_hat", "Taco Titan Hat", "#e8c53a"),
        CreatorOption("santa_hat", "Santa Hat", "#c0392b"),
        CreatorOption("sleuth_cap", "Sleuth's Cap", "#4a5d3a"))),
    "top" to ("Top" to listOf(
        CreatorOption("underwear", "Underwear", "#f0f0f0", "tee"),
        CreatorOption("jacket_red", "Red Jacket", "#d43d2a", "jacket"),
        CreatorOption("hoodie_blue", "Blue Hoodie", "#2a6fd4", "hoodie"),
        CreatorOption("tee_green", "Green Tee", "#2a9a4a", "tee"),
        CreatorOption("parka_orng", "Orange Parka", "#e07b2a", "parka"),
        CreatorOption("tee_teal", "Teal Tee", "#3aa8a8", "tee"),
        CreatorOption("jacket_brn", "Brown Jacket", "#6b4a2f", "jacket"),
        CreatorOption("hoodie_prp", "Purple Hoodie", "#6a4a8a", "hoodie"),
        CreatorOption("vest", "Vest", "#4a5d3a", "vest"),
        // v0.1.9d: the style token IS the construction — jacket wears
        // placket/buttons/pockets, parka a zipper placket, hoodie and
        // sweater a collar band (see avatarSpec)
        CreatorOption("jacket_denim", "Denim Jacket", "#4a6a9a", "jacket"),
        CreatorOption("jacket_black", "Black Jacket", "#2a2a2a", "jacket"),
        CreatorOption("hoodie_red", "Red Hoodie", "#c0392b", "hoodie"),
        CreatorOption("hoodie_gray", "Gray Hoodie", "#7a7a7a", "hoodie"),
        CreatorOption("parka_green", "Green Parka", "#2a7a4a", "parka"),
        CreatorOption("sweater_prp", "Purple Sweater", "#6a4a8a", "sweater"),
        CreatorOption("tee_pink", "Pink Tee", "#e87ab0", "tee"),
        // v0.5.0b: WARDROBE pieces (see the hat shelf note)
        CreatorOption("varsity_jacket", "Varsity Jacket", "#2a4a8a", "jacket"),
        CreatorOption("squad_hoodie", "Squad Hoodie", "#f2c230", "hoodie"),
        CreatorOption("pumpkin_sweater", "Pumpkin Sweater", "#e07b2a", "sweater"),
        CreatorOption("deep_winter_parka", "Deep Winter Parka", "#7ad4e8", "parka"),
        CreatorOption("fixture_vest", "Town Fixture Vest", "#b8860b", "vest"))),
    "bottom" to ("Bottom" to listOf(
        CreatorOption("underwear", "Underwear", "#f0f0f0", "shorts"),
        CreatorOption("jeans", "Jeans", "#3a5a8a", "pants"),
        CreatorOption("pants_brown", "Brown Pants", "#6b4a2f", "pants"),
        CreatorOption("pants_black", "Black Pants", "#2a2a2a", "pants"),
        CreatorOption("shorts", "Shorts", "#8a4a2a", "shorts"),
        CreatorOption("skirt", "Skirt", "#c86a9a", "skirt"),
        CreatorOption("camo", "Camo Pants", "#5a6a3a", "pants"),
        CreatorOption("jeans_black", "Black Jeans", "#1f1f28", "pants"),
        CreatorOption("khaki", "Khakis", "#b8a878", "pants"),
        CreatorOption("sweats", "Gray Sweats", "#8a8a8a", "pants"),
        CreatorOption("shorts_blue", "Blue Shorts", "#2a6fd4", "shorts"),
        CreatorOption("skirt_red", "Red Skirt", "#c0392b", "skirt"),
        // v0.5.0b: WARDROBE pieces (see the hat shelf note)
        CreatorOption("pro_camo", "Pro Camo Pants", "#44552e", "pants"),
        CreatorOption("road_jeans", "Road-Worn Jeans", "#2a3a5f", "pants"))),
    // v0.1.9d: the categories the games' creators have that ours
    // lacked — shoes, eyewear, facial hair, gloves, back items. The
    // old single-choice accessory slot keeps face/held items only;
    // pre-0.1.9d saves migrate in validateAppearance.
    "shoes" to ("Shoes" to listOf(
        CreatorOption("shoes_black", "Black Shoes", "#2a2a2a"),
        CreatorOption("sneakers_red", "Red Sneakers", "#c0392b"),
        CreatorOption("sneakers_white", "White Sneakers", "#e8e8e8"),
        CreatorOption("sneakers_blue", "Blue Sneakers", "#2a6fd4"),
        CreatorOption("boots_brown", "Brown Boots", "#6b4a2f"),
        CreatorOption("bare_feet", "Bare Feet", null, "bare"))),
    "eyewear" to ("Eyewear" to listOf(
        CreatorOption("none", "None"),
        CreatorOption("glasses", "Glasses"),
        CreatorOption("sunglasses", "Sunglasses"),
        CreatorOption("goggles", "Ski Goggles"),
        CreatorOption("star_shades", "Star Shades"))),
    "facial_hair" to ("Facial Hair" to listOf(
        CreatorOption("none", "Clean-Shaven"),
        CreatorOption("chinstrap", "Chinstrap"),
        CreatorOption("beard", "Full Beard"),
        CreatorOption("mustache", "Mustache"),
        CreatorOption("goatee", "Goatee"))),
    "gloves" to ("Gloves" to listOf(
        CreatorOption("none", "Bare Hands"),
        CreatorOption("mittens_red", "Red Mittens", "#c92344"),
        CreatorOption("mittens_blue", "Blue Mittens", "#2a6fd4"),
        CreatorOption("mittens_brown", "Brown Mittens", "#6b4a2f"),
        CreatorOption("gloves_black", "Black Gloves", "#2a2a2a"),
        CreatorOption("gloves_white", "White Gloves", "#f0f0f0"))),
    "back_item" to ("Back Item" to listOf(
        CreatorOption("none", "Nothing"),
        CreatorOption("backpack", "Backpack"),
        CreatorOption("cape", "Hero Cape", "#8a2a2a"),
        CreatorOption("wings", "Costume Wings", "#f4f4f4"))),
    "accessory" to ("Accessory" to listOf(
        CreatorOption("none", "None"),
        CreatorOption("freckles", "Freckles"),
        CreatorOption("scarf", "Scarf", "#c0392b"),
        // #42: wearable find — Craig's foam finger, for your own hand
        CreatorOption("foam_finger", "Foam Finger", unlockId = "craig_foam_finger"),
        // v0.5.0b: WARDROBE pieces (scarf-family accessories —
        // avatarSpec carries them through the scarf slot)
        CreatorOption("neighbor_scarf", "Neighborly Scarf", "#2a9a4a"),
        CreatorOption("collector_scarf", "Completionist Scarf", "#7a4a9a")))
)

// Blank New Kid default: bald, underwear — matches the games' new kid.
// The player customizes FROM this base.
val DEFAULT_APPEARANCE: Map<String, String> = mapOf(
    "skin" to "fair", "build" to "average", "hair_style" to "bald",
    "hair_color" to "brown", "hat" to "none", "top" to "underwear",
    "bottom" to "underwear", "shoes" to "shoes_black", "eyewear" to "none",
    "facial_hair" to "none", "gloves" to "none", "back_item" to "none",
    "accessory" to "none"
)

/**
 * v0.1.9d BUILD proportions: the player's own MeasuredProps, in the
 * same slot and the same measured idiom as the cast's (shares sum to
 * 1.00, head aspect inside the no-balloon bound). Deliberately NOT
 * any cast member's measured set — these are generic kid builds.
 * "average" is null: the classic unmeasured New Kid geometry, exact.
 */
val CREATOR_BUILDS: Map<String, MeasuredProps?> = mapOf(
    "average" to null,
    "slight" to MeasuredProps(0.640f, 0.250f, 0.110f, 1.05f, 1.35f, 0.97f),
    "stocky" to MeasuredProps(0.650f, 0.270f, 0.080f, 1.20f, 1.12f, 0.97f),
    "tall" to MeasuredProps(0.600f, 0.260f, 0.140f, 1.00f, 1.25f, 1.06f),
    "petite" to MeasuredProps(0.650f, 0.245f, 0.105f, 1.08f, 1.28f, 0.93f)
)

/** Pure shade helper for garment construction colors. */
fun darkerHex(hex: String, factor: Float = 0.72f): String {
    val h = hex.removePrefix("#")
    if (h.length != 6) return hex
    val r = h.substring(0, 2).toIntOrNull(16) ?: return hex
    val g = h.substring(2, 4).toIntOrNull(16) ?: return hex
    val b = h.substring(4, 6).toIntOrNull(16) ?: return hex
    fun sh(v: Int) = (v * factor).toInt().coerceIn(0, 255)
    return "#%02x%02x%02x".format(sh(r), sh(g), sh(b))
}

fun creatorOpt(category: String, optId: String): CreatorOption {
    val opts = CREATOR_OPTIONS[category]?.second ?: emptyList()
    return opts.firstOrNull { it.id == optId } ?: opts.first()
}

fun validateAppearance(raw: Map<String, String>?): Map<String, String> {
    val src = raw ?: emptyMap()
    // v0.1.9d legacy migration: pre-creator-depth saves wore glasses,
    // a chinstrap or a backpack in the single ACCESSORY slot. Those
    // grew into their own categories — move them over on load (an
    // explicit new-category value in the save always wins).
    val migrated = src.toMutableMap()
    when (val legacyAcc = src["accessory"]) {
        "glasses", "sunglasses" -> {
            if (migrated["eyewear"] == null) migrated["eyewear"] = legacyAcc
            migrated["accessory"] = "none"
        }
        "chinstrap" -> {
            if (migrated["facial_hair"] == null) migrated["facial_hair"] = "chinstrap"
            migrated["accessory"] = "none"
        }
        "backpack" -> {
            if (migrated["back_item"] == null) migrated["back_item"] = "backpack"
            migrated["accessory"] = "none"
        }
    }
    val out = mutableMapOf<String, String>()
    for (c in CREATOR_ORDER) {
        val valid = CREATOR_OPTIONS[c]?.second?.map { it.id }?.toSet() ?: emptySet()
        val v = migrated[c]
        out[c] = if (v != null && v in valid) v else DEFAULT_APPEARANCE[c]!!
    }
    return out
}

fun describeAppearance(appearance: Map<String, String>): String {
    val a = validateAppearance(appearance)
    val bits = mutableListOf<String>()
    if (a["build"] != "average")
        bits.add("a " + creatorOpt("build", a["build"]!!).name.lowercase() + " build")
    bits.add(creatorOpt("skin", a["skin"]!!).name.lowercase() + " skin")
    bits.add(creatorOpt("hair_color", a["hair_color"]!!).name.lowercase() + " " +
        creatorOpt("hair_style", a["hair_style"]!!).name.lowercase() + " hair")
    val hat = creatorOpt("hat", a["hat"]!!)
    if (hat.id != "none") bits.add("a " + hat.name.lowercase())
    bits.add("a " + creatorOpt("top", a["top"]!!).name.lowercase())
    bits.add(creatorOpt("bottom", a["bottom"]!!).name.lowercase())
    val shoes = creatorOpt("shoes", a["shoes"]!!)
    if (shoes.id != "shoes_black") bits.add(shoes.name.lowercase())
    val eye = creatorOpt("eyewear", a["eyewear"]!!)
    if (eye.id != "none") bits.add(eye.name.lowercase())
    val fh = creatorOpt("facial_hair", a["facial_hair"]!!)
    if (fh.id != "none") bits.add("a " + fh.name.lowercase())
    val gl = creatorOpt("gloves", a["gloves"]!!)
    if (gl.id != "none") bits.add(gl.name.lowercase())
    val bk = creatorOpt("back_item", a["back_item"]!!)
    if (bk.id != "none") bits.add("a " + bk.name.lowercase())
    val acc = creatorOpt("accessory", a["accessory"]!!)
    if (acc.id != "none") bits.add(acc.name.lowercase())
    return bits.joinToString(", ")
}

/**
 * v0.1.9d: appearance map -> the SheetSpec the figure system draws.
 * Pure (no android) so the JVM suite pins the whole mapping. KidArt's
 * preview and every other player-figure render go through this —
 * there is exactly one avatar construction (Phase-1 #40's rule).
 * The top's STYLE token is its construction: jackets wear the
 * sheets' placket + buttons + flap pockets, parkas a zipper placket,
 * hoodies/sweaters a collar band, vests placket + buttons.
 */
fun avatarSpec(appearance: Map<String, String>): SheetSpec {
    val a = validateAppearance(appearance)
    val skinHex = creatorOpt("skin", a["skin"]!!).color ?: "#f5cfa8"
    val hairColor = creatorOpt("hair_color", a["hair_color"]!!).color ?: "#6b4a2f"
    val hatOpt = creatorOpt("hat", a["hat"]!!)
    val topOpt = creatorOpt("top", a["top"]!!)
    val botOpt = creatorOpt("bottom", a["bottom"]!!)
    val shoeOpt = creatorOpt("shoes", a["shoes"]!!)
    val gloveOpt = creatorOpt("gloves", a["gloves"]!!)
    val backOpt = creatorOpt("back_item", a["back_item"]!!)
    val accOpt = creatorOpt("accessory", a["accessory"]!!)
    val eye = a["eyewear"]!!
    val fh = a["facial_hair"]!!
    val bareTop = topOpt.id == "underwear"
    val topHex = if (bareTop) skinHex else (topOpt.color ?: "#2a6fd4")
    val dark = darkerHex(topHex)
    var collarHex: String? = null
    var placket = false
    var buttons = false
    var pockets = 0
    when (topOpt.style) {
        "jacket" -> { collarHex = dark; placket = true; buttons = true; pockets = 2 }
        "parka" -> { collarHex = dark; placket = true }
        "hoodie", "sweater" -> { collarHex = dark }
        "vest" -> { placket = true; buttons = true }
    }
    return SheetSpec(
        skin = skinHex,
        top = topHex,
        collarHex = collarHex, placket = placket, buttons = buttons,
        pockets = pockets,
        bottom = if (botOpt.id == "underwear") "#f0f0f0"
            else (botOpt.color ?: "#3a5a8a"),
        bottomStyle = botOpt.style ?: "pants",
        shoeColor = shoeOpt.color ?: "#2a2a2a",
        barefoot = shoeOpt.style == "bare" || botOpt.id == "underwear",
        hairStyle = a["hair_style"]!!,
        hairColor = hairColor,
        hatId = hatOpt.id,
        hatColor = hatOpt.color ?: "#888888",
        hatAccent = "#f2c230",
        glasses = eye == "glasses",
        sunglasses = eye == "sunglasses",
        eyewear = if (eye == "goggles" || eye == "star_shades") eye else "",
        beard = fh == "chinstrap" || fh == "beard" || fh == "goatee",
        beardStyle = when (fh) {
            "chinstrap" -> "chinstrap"
            "goatee" -> "goatee"
            else -> "full"
        },
        mustache = fh == "mustache",
        beardColor = hairColor,
        freckles = accOpt.id == "freckles",
        // v0.5.0b: the wardrobe's scarf-family accessories ride
        // the same scarf slot, in their own colors
        scarfHex = if (accOpt.id == "scarf" || accOpt.id == "neighbor_scarf" ||
            accOpt.id == "collector_scarf") (accOpt.color ?: "#c0392b") else null,
        handColor = gloveOpt.color,
        backpack = backOpt.id == "backpack",
        backStyle = if (backOpt.id == "cape" || backOpt.id == "wings")
            backOpt.id else "none",
        backColor = backOpt.color ?: "#8a2a2a",
        measured = CREATOR_BUILDS[a["build"]!!],
        special = if (bareTop) "underwear_top" else null
    )
}

/**
 * v0.1.9d: the creator's dice. Rolls a coherent look from the
 * UNLOCKED shelf only — locked finds are earned in town, never
 * rolled — and never deals the full underwear combo. Deterministic
 * for a given rng, so the suite can pin it.
 */
fun randomAppearance(rng: kotlin.random.Random): Map<String, String> {
    val out = mutableMapOf<String, String>()
    for (c in CREATOR_ORDER) {
        // v0.5.0b: the dice NEVER rolls a locked piece — neither
        // a #42 collectible find nor a Wardrobe unlockable. (The
        // roll is ownership-blind on purpose: even an owned
        // wardrobe piece is earned, never dealt by chance.)
        val opts = CREATOR_OPTIONS[c]!!.second.filter {
            it.unlockId == null && it.id !in Wardrobe.PIECE_IDS
        }
        out[c] = opts[rng.nextInt(opts.size)].id
    }
    if (out["top"] == "underwear" && out["bottom"] == "underwear") {
        val bottoms = CREATOR_OPTIONS["bottom"]!!.second
            .filter { it.id != "underwear" && it.unlockId == null &&
                it.id !in Wardrobe.PIECE_IDS }
        out["bottom"] = bottoms[rng.nextInt(bottoms.size)].id
    }
    return out
}

/** #48's voice, pure: the comment for the category that changed.
 *  (GameActivity finds the changed category; the line lives here so
 *  every creator category — new ones included — is covered + pinned.) */
fun outfitCommentLine(changedCategory: String): String = when (changedCategory) {
    "hat" -> "New hat? Bold choice. It works, weirdly."
    "hair_style", "hair_color" ->
        "You did something with your hair. I'm not mad about it."
    "top" -> "New shirt. Trying to impress someone? Who? WHO?"
    "bottom" -> "New pants energy today. Noted."
    "accessory" -> "What's that accessory? Fancy. Suspiciously fancy."
    "skin" -> "You look... different. More different than usual."
    "build" -> "You look... bigger? Smaller? Did you grow? Kids don't grow mid-week."
    "shoes" -> "New shoes. You gonna run in those? Race you. Kidding. Unless?"
    "eyewear" -> "Whoa, new eyewear. You look smarter. Or sneakier. One of those."
    "facial_hair" -> "Is that FACIAL HAIR? On a fourth grader? Respect, honestly."
    "gloves" -> "Gloves? Planning something with no fingerprints? Smart."
    "back_item" -> "What's on your back now? You've got a whole cargo thing going."
    else -> "You look different today. New everything?"
}

// ---------------- creator preset slots (v0.1.9d) ----------------
const val CREATOR_PRESET_SLOTS = 3

/** Presets persist as one JSON array of appearance maps / nulls. */
fun encodePresets(slots: List<Map<String, String>?>): String =
    Json.stringify((0 until CREATOR_PRESET_SLOTS).map { i ->
        slots.getOrNull(i)?.let { validateAppearance(it) }
    })

/** Tolerant decode: anything unreadable yields three empty slots. */
@Suppress("UNCHECKED_CAST")
fun decodePresets(json: String?): List<Map<String, String>?> {
    val empty = List<Map<String, String>?>(CREATOR_PRESET_SLOTS) { null }
    if (json.isNullOrBlank()) return empty
    return try {
        val parsed = Json.parse(json) as? List<*> ?: return empty
        (0 until CREATOR_PRESET_SLOTS).map { i ->
            val m = parsed.getOrNull(i) as? Map<String, Any?> ?: return@map null
            validateAppearance(m.mapValues { it.value.toString() })
        }
    } catch (e: Exception) {
        empty
    }
}

// ---------------- Friendship (cozy: gains only, no decay) ----------------
val FRIEND_TIERS: List<Pair<Int, String>> = listOf(
    0 to "New Kid", 25 to "Pal", 50 to "Buddy", 75 to "Best Friend"
)

fun friendTier(points: Int): String {
    var name = "New Kid"
    for ((min, n) in FRIEND_TIERS) if (points >= min) name = n
    return name
}

/** 0-5 jagged paper hearts for the Who/Journal screen. */
fun friendHearts(points: Int): Int = (points / 20).coerceIn(0, 5)

/** Tier index for greetings: 0 = New Kid, 1 = Pal/Buddy, 2 = Best Friend. */
fun friendGreetTier(points: Int): Int = when {
    points >= 75 -> 2
    points >= 25 -> 1
    else -> 0
}

// 3 tiers x 5 kids x 2 lines. Cartman: rude-but-warmer. Butters: adores you. Kenny: muffled.
val FRIEND_GREETINGS: Map<String, Map<Int, List<String>>> = mapOf(
    "Cartman" to mapOf(
        0 to listOf(
            "Oh great, it's the new kid again. Try not to embarrass yourself.",
            "What do you want? Make it quick, I'm busy being awesome."),
        1 to listOf(
            "Oh. It's you. ...You're not the worst, I guess. Don't let it go to your head.",
            "Hey, Douchebag. Saved you a Cheesy Poof. ONE. Don't get used to it."),
        2 to listOf(
            "Okay fine, you're my best friend now. But if you tell anyone I said that, I'll deny it.",
            "Dude! My best friend! Let's go do something awesome. I mean... whatever, you're here.")),
    "Stan" to mapOf(
        0 to listOf("Hey. New kid, right? I'm Stan.", "Oh hey. What's up?"),
        1 to listOf("Hey! Good to see you, dude.", "Oh hey, it's you! What's going on?"),
        2 to listOf(
            "Dude! My best friend! This day just got way better.",
            "Hey! I was just thinking about you. Weird, right?")),
    "Kyle" to mapOf(
        0 to listOf("Oh, hi. You're the new kid. I'm Kyle.", "Hey. Welcome to South Park. Sorry in advance."),
        1 to listOf("Hey! Good to see you!", "Oh hey! What's up?"),
        2 to listOf(
            "My best friend! Dude, you gotta hear what Cartman did today.",
            "Hey! Best timing — I need someone sane to talk to.")),
    "Kenny" to mapOf(
        0 to listOf("(muffled) Mmph! *waves* Rmph!", "(muffled) Mmmph rmph! *nods*"),
        1 to listOf("(muffled) Mmph rmph! *happy wiggle* Hey!", "(muffled) Rmph rmph! *thumbs up*"),
        2 to listOf(
            "(muffled) MMPH RMPH! *tackle-hugs you* Rmph!",
            "(muffled) Mmmph! *biggest happy wiggle yet*")),
    "Butters" to mapOf(
        0 to listOf(
            "Oh hi! You're the new kid! I'm Butters! Wanna be pals?!",
            "Hiya! Gee, it's swell to meet ya!"),
        1 to listOf(
            "Oh boy, it's my pal! Hi hi hi!",
            "Hiya, pal! Best day ever just got bester!"),
        2 to listOf(
            "MY BEST FRIEND! Oh boy oh boy oh boy! This is the best day EVER!",
            "Eeeee! It's you! My bestest friend in the whole wide world!"))
) + EXTRA_FRIEND_GREETINGS + WAVE2_FRIEND_GREETINGS + WAVE3_FRIEND_GREETINGS + WAVE4_FRIEND_GREETINGS + WAVE5_FRIEND_GREETINGS + WAVE6_FRIEND_GREETINGS

// ---------------- Rotating daily dialogue: 5 kids x 24 one-liners ----------------
// Index = dayOfYear % 24. Crude and funny, never pastel.
val BASE_DAILY_TOPICS: Map<String, List<String>> = mapOf(
    "Cartman" to listOf(
        "I'm starting a business selling snow. It's called entrepreneurship, Kyle.",
        "Somebody ate my Cheesy Poofs and I'm taking hostages until they confess.",
        "New scheme: we charge tourists to look at Kenny's house. It's basically a zoo.",
        "I demand a recount on who the coolest kid in town is. I won, but recount anyway.",
        "My mom says I can't have KFC for breakfast. I'm calling the authorities.",
        "I'm boycotting school until they rename it Eric Cartman Elementary.",
        "Kyle looked at me weird today. That's going in my revenge journal.",
        "I'm selling autographed pictures of myself. Ten bucks. They're worth hundreds.",
        "Somebody needs to carry me to the bus stop. My legs are tired from being awesome.",
        "I'm starting a podcast. Episode one: why everyone is stupid except me.",
        "Butters owes me five bucks. Interest is now fifty bucks. That's economics.",
        "I'm going on a hunger strike until dinner. It's called commitment.",
        "New rule: I get to be Player One forever. Democracy is broken.",
        "I'm writing a strongly worded letter to Santa about my gifts last year.",
        "If Kenny dies today I'm calling dibs on his parka. It's basically new.",
        "I'm unionizing the fourth grade. Dues are Cheesy Poofs.",
        "Somebody's been touching my stuff. I'm dusting for fingerprints.",
        "I'm running for mayor of Kupa Keep. Unopposed, because I'm the best.",
        "I found a dollar. Finders keepers. I'm rich now, respect me.",
        "I'm teaching my mom to make snacks faster. It's called leadership.",
        "Stan's dog looked at me funny. I'm pressing charges.",
        "I'm opening a casino in my basement. What could go wrong.",
        "Kyle's mom made cookies and didn't invite me. This is war.",
        "I'm the main character of this town. Everyone else is an NPC."),
    "Kyle" to listOf(
        "Cartman ate my lunch AGAIN and I'm this close to snapping, you guys.",
        "Did you know the school vending machine is a scam? I'm writing a report.",
        "My mom says I have to be nice to Cartman. My mom is wrong about this one thing.",
        "Somebody spray-painted the bus stop and honestly? It had a point.",
        "I'm starting a petition to ban homework. Who's with me? Everybody should be.",
        "The news said it might snow. The news is always right about snow here.",
        "Cartman said something so dumb today I actually felt my brain shrink.",
        "I'm reading about the Founding Fathers. They'd hate this town.",
        "Stan threw up again. I'm starting to think it's a lifestyle choice.",
        "My dad tried to be cool in front of my friends. I need a new dad.",
        "Butters got grounded for blinking too loud. I'm calling a lawyer.",
        "The cafeteria served something called 'meat surprise.' There was no meat.",
        "I'm morally outraged about at least three things before breakfast daily.",
        "Kenny did something insane and nobody even blinked. This town is broken.",
        "I tried meditating to deal with Cartman. It didn't take.",
        "Somebody needs to tell the mayor the potholes are eating cars now.",
        "I had a dream we all got along. Woke up screaming.",
        "The mall food court raised prices. This is class warfare.",
        "I'm boycotting Raisins on principle. Don't ask which principle.",
        "My little brother bit me. I'm pressing charges. Kidding. Mostly.",
        "The pond is frozen and Cartman wants to skate on it. Pray for us.",
        "I wrote a five-page essay on why snow days should be longer. A+.",
        "Stan's dad did something on TV again. I can't even be surprised anymore.",
        "If Cartman says one more dumb thing today I'm moving to Canada. Kidding. San Francisco."),
    "Stan" to listOf(
        "Dude, have you noticed the snow never actually melts here? Ever?",
        "Sparky chased his tail for an hour. Honestly, goals.",
        "My dad bought another weird thing. The garage is full. Send help.",
        "Cartman's scheme today involved a ladder and a hamster. I didn't ask.",
        "Kyle's been ranting since breakfast. It's kind of impressive.",
        "Butters waved at a squirrel and the squirrel waved back. I saw it.",
        "I think the bus driver hates us. Can't blame him.",
        "Kenny said something and I just nodded. That's our whole friendship.",
        "The cafeteria pizza tastes like cardboard. Still ate three slices.",
        "My mom says the town meeting is tonight. Nobody's going.",
        "It's so cold my thoughts are freezing halfway through.",
        "Wendy waved at me and I forgot how legs work.",
        "Tweek gave me coffee by accident. I can see through time now.",
        "The pond looks creepy today. Creepier than usual.",
        "I found a dollar and immediately lost it. Classic.",
        "Cartman tried to charge admission to the bus stop. It almost worked.",
        "My sister beat me at video games again. I'm retiring.",
        "The snow plow guy waved at me. We're basically friends now.",
        "Kyle explained taxes to me. I threw up a little.",
        "Butters got excited about a rock. It was a pretty good rock.",
        "I learned something today: don't eat yellow snow. Again.",
        "The mall Santa looked tired. I get it, man.",
        "Kenny dared Cartman back today. Absolute legend.",
        "Another day, another weird thing at Stark's Pond. Don't go in the water."),
    "Kenny" to listOf(
        "(muffled) Mmph rmph! *points at the sky* Rmph!",
        "(muffled) Mmmph rmph rmph! *does a flip*",
        "(muffled) Rmph! *holds up something weird* Mmph?",
        "(muffled) Mmph rmph rmph! *laughs*",
        "(muffled) Mmmph! *nods at you* Rmph rmph!",
        "(muffled) Rmph rmph! *points at Cartman, shakes head*",
        "(muffled) Mmph! *mimes something dramatic*",
        "(muffled) Rmmph rmph! *shivers, then grins*",
        "(muffled) Mmph rmph! *kicks snow* Rmph!",
        "(muffled) Mmmph! *thumbs up* Rmph rmph rmph!",
        "(muffled) Rmph! *pretends to die dramatically, pops back up*",
        "(muffled) Mmph rmph! *waves at a squirrel*",
        "(muffled) Mmmph rmph! *rubs tummy* Rmph?",
        "(muffled) Rmph rmph rmph! *points at the pond*",
        "(muffled) Mmph! *does a victory dance*",
        "(muffled) Mmmph rmph! *shrugs* Rmph!",
        "(muffled) Rmph! *high-fives the air*",
        "(muffled) Mmph rmph rmph! *looks around sneakily*",
        "(muffled) Mmmph! *pats your shoulder* Rmph!",
        "(muffled) Rmph rmph! *spins in a circle*",
        "(muffled) Mmph! *mimes eating something huge*",
        "(muffled) Rmmph rmph! *points at himself, thumbs up*",
        "(muffled) Mmph rmph! *falls over laughing*",
        "(muffled) Mmmph! *waves goodbye to nobody* Rmph!"),
    "Butters" to listOf(
        "Oh boy, I found a shiny rock! I'm naming him Rocky Jr.!",
        "My dad says I'm not allowed to have fun, so I'm having fun quietly!",
        "I tried to make a snowman but it looks like my grandma! Oh hamburgers!",
        "Professor Chaos is taking the day off! Even villains need naps, fellas!",
        "I waved at every car on Main Street! Three waved back! Best day ever!",
        "My mom packed me extra cookies! I'm sharing with everybody! Even Eric!",
        "I learned to whistle! ...Well, I learned to try to whistle!",
        "The squirrel at the park knows my name now! Probably!",
        "I'm gonna be a veterinarian when I grow up! Or a race car driver! Or both!",
        "Oh boy oh boy, it's almost lunch! I love lunch! Lunch loves me!",
        "I drew a picture of all of us! Eric said it looks dumb! I'm framing it!",
        "My grandma sent me five dollars! I'm rich, fellas! RICH!",
        "I tried hot chocolate with extra marshmallows! My teeth are singing!",
        "The bus driver smiled at me today! I think we're best friends now!",
        "I found a four-leaf clover! In the snow! It's a Christmas miracle! In October!",
        "I'm practicing my evil laugh for Professor Chaos! Mwa-ha... ha? I'll keep working on it!",
        "My dad grounded me for smiling too big! Worth it, fellas!",
        "I made friends with a snowflake! Then it melted! I'll miss him!",
        "Oh hamburgers, I almost forgot to be cheerful today! Phew!",
        "I counted all the trees on Main Street! There are... a lot! I lost count!",
        "Eric let me carry his backpack! My arms hurt but my heart is full!",
        "I'm saving up for a pony! I have three dollars! Almost there!",
        "The lunch lady gave me an extra tater tot! I'm telling everyone!",
        "Today's gonna be the best day ever, fellas! I can feel it in my toes!")
)

/** v0.2.0d: core 24 -> 28 topics, the other 27 cast 12 -> 18. */
val DAILY_TOPICS: Map<String, List<String>> =
    mergeLineBanks(BASE_DAILY_TOPICS, EXTRA_DAILY_TOPICS, MORE_DAILY_TOPICS,
        WAVE2_DAILY_TOPICS, WAVE2_MORE_DAILY_TOPICS,
        WAVE3_DAILY_TOPICS, WAVE3_MORE_DAILY_TOPICS,
        WAVE4_DAILY_TOPICS, WAVE4_MORE_DAILY_TOPICS,
        WAVE5_DAILY_TOPICS, WAVE5_MORE_DAILY_TOPICS,
        WAVE6_DAILY_TOPICS, WAVE6_MORE_DAILY_TOPICS, WAVE6_MORE_DAILY_TOPICS_2)

// ---------------- v0.3.0e: the sky, witnessed and debated ----------------
/** What somebody standing outside under a special sky says
 *  about it, that night, unprompted. Keyed by SkyState kind.
 *  Deliberately a different subject from the pond case: this
 *  is weather, wonder and schoolyard science — no case talk,
 *  no clues, nothing over the water. */
val SKY_WITNESS_LINES: Map<String, List<String>> = mapOf(
    SkyState.AURORA to listOf(
        "Are you SEEING this, {newkid}?! The sky is GREEN. The whole sky!",
        "I counted eleven different greens up there. ELEVEN. Nobody " +
            "believes me but I have a tally sheet.",
        "My mom says it's solar wind. My dad says don't worry about it. " +
            "I'm worried about it, {newkid}.",
        "Half the street's out in pajamas. Even the Stotches. Especially " +
            "the Stotches."),
    SkyState.METEOR to listOf(
        "I wished on like nine shooting stars, {newkid}. Statistically " +
            "ONE of them has to work.",
        "Every time one goes over, everything goes quiet for a second. " +
            "Even the wind shuts up.",
        "You're supposed to wish in your HEAD. Somebody at the pond " +
            "wished out loud for a jetski.",
        "I saw three at once! Three! The sky is showing off and I'm " +
            "here for it."),
    SkyState.HARVEST to listOf(
        "The moon looks FAKE tonight, {newkid}. Like, " +
            "construction-paper fake. Orange!",
        "Harvest moon! It's not actually bigger — okay it is DEFINITELY " +
            "bigger, I don't care what Kyle says.",
        "Everything's orange tonight. The snow's embarrassed. I'm " +
            "orange. You're orange.",
        "My grandpa says a moon like that makes the crops bold. We " +
            "don't have crops, so it's making ME bold.")
)

/** The morning-after: what the town's daily topics gain the
 *  day after a special sky (Engine.weightedTopicFor mixes
 *  these in with a heavy score while the buzz lasts). Kids
 *  debating what it WAS is the whole point — and the debate
 *  stays sky science and schoolyard logic, never the case. */
object SkyTopics {

    /** Lines any cast member might bring up, per sky kind. */
    val GENERIC: Map<String, List<String>> = mapOf(
        SkyState.AURORA to listOf(
            "the green sky curtains from last night — solar wind, per " +
                "Kyle; a screensaver the sky forgot to turn off, per Cartman",
            "last night's sky show: half the class watched it in pajamas " +
                "and has OPINIONS",
            "whether the aurora means the North Pole is 'doing something' " +
                "(the class geologist has resigned)"),
        SkyState.METEOR to listOf(
            "the meteor shower — who wished for what, and whose wish is " +
                "already ruined",
            "shooting stars: space garbage or space magic (the class is " +
                "split right down the middle)",
            "whether a wish said out loud at the pond still counts " +
                "(asking for somebody who owns a jetski brochure)"),
        SkyState.HARVEST to listOf(
            "the giant orange moon — why it looks bigger at the horizon " +
                "(nobody actually knows and it's driving Kyle insane)",
            "the harvest moon: pumpkin-colored, enormous, and personally " +
                "responsible for everyone's behavior today",
            "whether the moon was REALLY orange or the whole town dreamed " +
                "it in orange at the same time"))

    /** Per-kid debate positions, per sky kind. */
    val BY_KID: Map<String, Map<String, List<String>>> = mapOf(
        SkyState.AURORA to mapOf(
            "Kyle" to listOf(
                "solar wind hitting the magnetosphere — I drew a DIAGRAM, " +
                    "you guys, it's not mysterious, it's CHARGED PARTICLES"),
            "Cartman" to listOf(
                "the sky putting on a light show WITHOUT charging " +
                    "admission. Suspicious. Deeply suspicious."),
            "Butters" to listOf(
                "I watched the sky curtains and didn't even get grounded " +
                    "for it — a personal best, fellas!"),
            "Stan" to listOf(
                "the aurora — my dad filmed it for three hours and got " +
                    "mostly his own thumb")),
        SkyState.METEOR to mapOf(
            "Butters" to listOf(
                "I wished real careful at the pond last night. If it " +
                    "comes true I'm not allowed to tell what it was."),
            "Kenny" to listOf(
                "(muffled) I wished on EVERY star. Playing the odds, dude."),
            "Tweek" to listOf(
                "TOO MANY WISHES — what if they CONFLICT?! What if two " +
                    "wishes COLLIDE?!"),
            "Jimmy" to listOf(
                "I wished for b-better material. The stars are still " +
                    "laughing at me.")),
        SkyState.HARVEST to mapOf(
            "Randy" to listOf(
                "the harvest moon over MY farm — the crops practically " +
                    "posed for it, Sharon, it was a moment"),
            "Kyle" to listOf(
                "the horizon illusion — the moon isn't bigger, your BRAIN " +
                    "is lying to you (respectfully)"),
            "Craig" to listOf(
                "the orange moon. It was fine. Everyone can calm down."),
            "Wendy" to listOf(
                "the harvest moon — I read three articles and the honest " +
                    "answer is 'nobody fully knows', which I respect")))

    /** The buzz topics [kid] brings up the day after [kind]:
     *  the generic debate plus their own position in it. */
    fun topicsFor(kid: String, kind: String): List<String> =
        (GENERIC[kind] ?: emptyList()) +
            (BY_KID[kind]?.get(kid) ?: emptyList())
}

// ---------------- Sleepy one-liners (22:00-07:00: tap = cozy one-liner, no full dialogue) ----------------
/** #58 wingman chime-ins ({name} = the friend being talked to). */
val WINGMAN_LINES: List<String> = listOf(
    "{name}'s right, {newkid}. I'd listen if I were you.",
    "What {name} said. I was standing here the whole time, I heard everything.",
    "You should see {name} when you're not around. It's worse. Affectionately.",
    "Backing {name} up on this one. That's what I'm here for.",
    "{name}'s being modest. The real story is so much dumber."
)

val SLEEPY_LINES: Map<String, List<String>> = mapOf(
    "Cartman" to listOf(
        "Mmm... five more minutes... tell Kyle he's still a jerk...",
        "Zzz... Cheesy Poofs... don't touch 'em..."),
    "Stan" to listOf(
        "Mmm... dude... not now...",
        "Zzz... Sparky... stop licking my face..."),
    "Kyle" to listOf(
        "Mmm... morally... outraged... later...",
        "Zzz... five more minutes... of justice..."),
    "Kenny" to listOf(
        "(muffled) Mmmmph... zzz... rmph...",
        "(muffled) Zzz... mmph..."),
    "Butters" to listOf(
        "Oh boy... sleepy time... zzz...",
        "Mmm... don't ground me... I'm dreaming...")
) + EXTRA_SLEEPY_LINES + WAVE2_SLEEPY_LINES + WAVE3_SLEEPY_LINES + WAVE4_SLEEPY_LINES + WAVE5_SLEEPY_LINES + WAVE6_SLEEPY_LINES

// ---------------- Collectibles: 12 South-Park-flavored types ----------------
val COLLECTIBLES: List<Collectible> = listOf(
    Collectible("chinpokomon_chu", "Chu-Chu-Zhi", "🐭",
        "Gotta buy 'em all! This one's probably offensive. Don't think about it."),
    Collectible("chinpokomon_peng", "Pengin", "🐧",
        "A penguin Chinpokomon. It silently judges your spending habits."),
    Collectible("crab_person", "Crab Person", "🦀",
        "Tastes like crab! Talks like people! It scuttles away if you hesitate — tap it twice.", rare = true),
    Collectible("cheesy_poof", "Cheesy Poof", "🧀",
        "Cartman's lost lunch. Still good. Probably. Don't smell it."),
    Collectible("tweek_coffee", "Tweek's Coffee", "☕",
        "TOO MUCH PRESSURE! Also way too much caffeine."),
    Collectible("member_berry", "Member Berry", "🍇",
        "'Member?", rare = true),
    Collectible("gnome_underpants", "Gnome Underpants", "👖",
        "Phase 1: collect underpants. Phase 3: profit.", rare = true),
    Collectible("towelie_scrap", "Towelie Scrap", "🧻",
        "Don't forget to bring a towel! ...You wanna get high?"),
    Collectible("vchip", "V-Chip", "📺",
        "Blocks 99% of fun. Cartman removed his with a fork."),
    Collectible("critter_star", "Woodland Critter Star", "⭐",
        "It stared at you with dead eyes. You kept it anyway.", rare = true),
    Collectible("kupa_coin", "Kupa Keep Coin", "🪙",
        "Currency of Cartman's backyard kingdom. Worthless everywhere else."),
    Collectible("pond_pebble", "Stark's Pond Pebble", "🪨",
        "Something moved under the ice when you picked it up. Don't go back."),
    Collectible("mr_hankey", "Mr. Hankey", "💩",
        "Howdy-ho! Christmas-only. He smells like... Christmas.", rare = true, christmasOnly = true),
    // ---- v0.1.2 lore-mined additions ----
    Collectible("lorde_wig", "Lorde's Stage Wig", "🎤",
        "Sparkly. Turns out South Park's biggest pop star is a 45-year-old geologist.", rare = true),
    Collectible("mr_hat", "Mr. Hat", "🎩",
        "A puppet with opinions. Garrison listens. You probably shouldn't.", rare = true),
    Collectible("citywok_menu", "City Wok Menu", "🥡",
        "City Beef. City Chicken. City Shrimp. City everything."),
    Collectible("scuzzlebutt_paw", "Scuzzlebutt Paw", "🐾",
        "Taxidermy souvenir. Patrick Duffy sewn on where his head should be.", rare = true),
    Collectible("tegridy_nugget", "Tegridy Nugget", "🌿",
        "Vacuum-sealed farm product. It's got... you know what it's got."),
    Collectible("raisins_nametag", "Raisins Name Tag", "🏷️",
        "Reads LEXUS. She's not your girlfriend, Butters."),
    Collectible("shake_weight", "Shake Weight", "🏋️",
        "The official sponsor of the Marsh marriage."),
    Collectible("gobbles_feather", "Gobbles' Feather", "🪶",
        "From Timmy's turkey. Helen Keller! The Musical!"),
    Collectible("woodsy_poster", "Woodsy Owl Poster", "🦉",
        "Give a hoot, don't pollute. Mackey hoards these. Don't ask."),
    Collectible("fingerbang_tape", "Fingerbang Demo Tape", "📼",
        "Side A is a bop. Do NOT play side B."),
    Collectible("craig_foam_finger", "Craig's Foam Finger", "☝️",
        "Custom made. One finger. He flips off everyone. Including family."),
    Collectible("skeeters_dart", "Skeeter's Dart", "🎯",
        "Slightly bent. The dads play darts. Randy always loses. Then drinks.")
)

fun collectibleById(id: String): Collectible? = COLLECTIBLES.firstOrNull { it.id == id }

// ---------------- Holidays (data-driven) ----------------
val HOLIDAYS: List<Holiday> = listOf(
    Holiday("halloween", "Halloween", "1025", "1031", setOf("pumpkins", "costumes", "spooky")),
    Holiday("christmas", "Christmas", "1220", "1226", setOf("lights", "boostedSnow", "critters", "hankey")),
    Holiday("newyear", "New Year", "1231", "0101", setOf("fireworks")),
    Holiday("valentine", "Valentine's Day", "0214", "0214", setOf("hearts", "sweet")),
    Holiday("july4", "4th of July", "0704", "0704", setOf("fireworks", "bunting")),
    Holiday("cowdays", "Cow Days", "0715", "0721", setOf("cowspots", "moo")),
    Holiday("stpatricks", "St. Patrick's Day", "0317", "0317", setOf("green", "shamrock")),
    Holiday("blackfriday", "Black Friday", "1124", "1130", setOf("chaos", "shopping"))
)

/** monthDay = "MMdd". Handles the New Year wrap (1231-0101). */
fun currentHoliday(monthDay: String): Holiday? {
    for (h in HOLIDAYS) {
        if (h.startMd <= h.endMd) {
            if (monthDay >= h.startMd && monthDay <= h.endMd) return h
        } else {
            if (monthDay >= h.startMd || monthDay <= h.endMd) return h
        }
    }
    return null
}

// ============================================================
// Holiday traditions (v0.1.9e) — holidays stop being a banner
// and a decorations line. Each one now has a TRADITION: a place
// the town gathers at set hours to actually DO the holiday
// (schedules bend there via Engine's living transform, the way
// birthdays already bend people), location dressing written
// per-place (Look reads it; SceneArt draws the elements in
// HOLIDAY_DRESSING), and dialogue beats any NPC can drop.
// All of it rides the existing episode machinery — the title
// card and the credits wrap still fire from Episodes.kt.
// ============================================================

data class HolidayTradition(
    val holidayId: String,
    val activity: String,     // what NPCs are doing at the gathering
    val locId: String,        // where the tradition gathers the town
    val hours: IntRange,      // when it happens (24h)
    val dressing: String,     // Look line: how the place is dressed
    val beats: List<String>   // holiday dialogue beats, any NPC
)

val HOLIDAY_TRADITIONS: Map<String, HolidayTradition> = listOf(
    HolidayTradition("halloween",
        "Trick-or-treating in a deeply unsafe homemade costume.",
        "MainStreet", 17..21,
        "Main Street is strung with toilet paper, and there's a jack-o'-lantern on every porch — half of them already kicked in.",
        listOf("Trick or treat. Those are the terms. I don't make the rules, I enforce them, {newkid}.",
            "My costume cost four dollars and one dignity. Worth it.",
            "Somebody's handing out TOOTHBRUSHES on Elm. We ride at dawn.")),
    HolidayTradition("christmas",
        "Caroling on the church steps. Off-key, full volume, no survivors.",
        "Church", 16..20,
        "The church steps are lined with luminarias and one enormous inflatable Santa, gently listing to the left.",
        listOf("We've been caroling for an hour and my throat is festive rubble, {newkid}.",
            "I know ONE verse. I sing it like it's six. Confidence is a carol.",
            "Mr. Hankey signed my hat. No I will not explain.")),
    HolidayTradition("newyear",
        "Counting down to midnight on Main Street since about 8 PM.",
        "MainStreet", 20..23,
        "Confetti cannons are staged all down Main Street. Randy built them. The town has been briefed on the blast radius.",
        listOf("I've been counting down since 8. If I stop now the year won't turn, {newkid}. That's the rule.",
            "My resolution is to have fewer resolutions. Starting next year.",
            "Somebody brought a foghorn. For midnight. We're all going to be a little deaf in the new year.")),
    HolidayTradition("valentine",
        "Skating badly at the pond and holding hands about it.",
        "StarksPond", 12..17,
        "The pond is ringed with paper hearts and a drifts-worth of rejected valentines, frozen mid-regret.",
        listOf("I made fourteen valentines and delivered two. The math of courage, {newkid}.",
            "Skating is just falling with choreography. Like feelings.",
            "Kenny got a valentine IN his parka hood. He doesn't know. Nobody tell Kenny.")),
    HolidayTradition("july4",
        "Watching Randy's fireworks 'rehearsal' from a legally safe distance.",
        "KidsPark", 14..20,
        "The park is draped in bunting, and the fireworks pile in the middle is roughly the size of a car.",
        listOf("Randy says the big one is 'probably' legal. The word 'probably' is load-bearing, {newkid}.",
            "I've got a lawn chair, a soda, and a safe distance. This is the whole holiday.",
            "Last year the finale set off car alarms in the next town. We aim high.")),
    HolidayTradition("cowdays",
        "Mooing at the tourists. It's tradition. Nobody remembers why.",
        "MainStreet", 10..16,
        "Main Street wears cow-print banners end to end, and one very patient cow stands in the middle of it, judging everyone.",
        listOf("Moo. That's the whole contribution, {newkid}. Cow Days asks little and gives less.",
            "A tourist asked if the mooing means something. It means everything. It means nothing.",
            "I've mooed at forty-one tourists. Personal best. The cow is proud of me, I think.")),
    HolidayTradition("stpatricks",
        "Drinking something green at Skeeter's and regretting it in advance.",
        "SkeetersBar", 15..20,
        "Skeeter's has gone full shamrock — green banners, green drinks, green regrets scheduled for tomorrow.",
        listOf("Everything's green today, {newkid}. The beer, the hats, tomorrow's choices.",
            "I pinched a guy for no green and he pinched me back WITH a citation. Fair.",
            "There's one green drink that should be illegal in nine states. I've had two.")),
    HolidayTradition("blackfriday",
        "Lining up at the Mall with a folding chair, a thermos, and a grudge.",
        "Mall", 5..11,
        "The Mall's doors are barricaded with sale banners, tents line the sidewalk, and the line has formed its own government.",
        listOf("I've been in this line since 5 AM for a TV I don't need, {newkid}. This is the holiday spirit now.",
            "The line has rules. The line has a mayor. The line provides.",
            "Forty percent off everything and one hundred percent off my dignity."))
).associateBy { it.holidayId }

/** Dressing elements SceneArt draws per holiday (beyond the
 *  flag-driven decor) — the smoke suite cross-checks every id
 *  here against the SceneArt source so dressing can't silently
 *  fall out of the art again. Element ids are named in the art
 *  code comments. */
val HOLIDAY_DRESSING: Map<String, List<String>> = mapOf(
    "halloween" to listOf("tombstones", "porch pumpkins"),
    "christmas" to listOf("wreath", "luminarias"),
    "newyear" to listOf("confetti cannons", "banner 2027"),
    "valentine" to listOf("heart garland", "valentine drift"),
    "july4" to listOf("fireworks pile", "bunting swags"),
    "cowdays" to listOf("cow banners", "the patient cow"),
    "stpatricks" to listOf("shamrock dots", "green bunting"),
    "blackfriday" to listOf("sale banners", "line tents")
)

object Traditions {
    fun of(state: GameState): HolidayTradition? =
        state.holidayId?.let { HOLIDAY_TRADITIONS[it] }

    /** The tradition bend: during the tradition's hours, a
     *  leisure block may reroute to the gathering place wearing
     *  the tradition's activity. Deterministic per (npc, day,
     *  block) — same seeded-probe pattern as the other bends. */
    fun bendLoc(state: GameState, name: String, blockLoc: String,
                blockKey: String?, bendable: Set<String>): String? {
        val t = of(state) ?: return null
        val hour = state.timeMin / 60
        if (hour !in t.hours) return null
        if (blockLoc !in bendable || blockLoc == t.locId) return null
        val probe = kotlin.random.Random(name.hashCode().toLong() * 31337 +
            state.todayKey.hashCode() + (blockKey?.hashCode() ?: 0))
        return if (probe.nextDouble() < 0.40) t.locId else null
    }

    /** Look dressing line when the player is standing at the
     *  tradition's gathering place during the holiday. */
    fun dressingLine(state: GameState, locId: String): String? {
        val t = of(state) ?: return null
        return if (t.locId == locId) "🎀 ${t.dressing}" else null
    }
}

// 2 short lines per kid per holiday. South-Park-ified decorations, crude writing.
val HOLIDAY_LINES: Map<String, Map<String, List<String>>> = mapOf(
    "halloween" to mapOf(
        "Cartman" to listOf(
            "I'm going as a witch. A sexy witch. Respect it.",
            "Give me candy or I'll put a curse on your house. A real one."),
        "Stan" to listOf(
            "Dude, Halloween in South Park. Something's gonna go wrong.",
            "I'm going as a ghost. A sheet. It's classic."),
        "Kyle" to listOf(
            "Cartman's costume is offensive. I'm shocked. SHOCKED.",
            "I'm going as a zombie. A morally outraged zombie."),
        "Kenny" to listOf(
            "(muffled) Mmph rmph! *vampire cape swish*",
            "(muffled) Rmph! *pretends to bite your neck*"),
        "Butters" to listOf(
            "I'm a ghost, fellas! Boo! ...Was that scary? Be honest!",
            "Oh boy, trick or treat! I got THREE Snickers!")),
    "christmas" to mapOf(
        "Cartman" to listOf(
            "I want a Haibo robot and seventeen other things. Get writing, Santa.",
            "Christmas is about ME getting presents. That's the whole thing."),
        "Stan" to listOf(
            "Dude, Christmas in South Park. Try not to die.",
            "My dad's doing something with Christmas lights. It's a fire hazard."),
        "Kyle" to listOf(
            "Happy Chrismahanukwanzakah, I guess.",
            "Mr. Hankey showed up again. Nobody's surprised anymore."),
        "Kenny" to listOf(
            "(muffled) Mmph rmph! *jingle bells* Ho!",
            "(muffled) Rmph! *points at lights* Mmph!"),
        "Butters" to listOf(
            "Oh boy, Christmas! I asked Santa for a hug! And a pony!",
            "Merry Christmas, fellas! I love everybody! Even Eric!")),
    "newyear" to mapOf(
        "Cartman" to listOf(
            "My resolution: be even more awesome. Already crushing it.",
            "Fireworks! Somebody hold my juice box."),
        "Stan" to listOf(
            "New year, same weird town, dude.",
            "My resolution is to stop throwing up. We'll see."),
        "Kyle" to listOf(
            "My resolution: yell at Cartman 20% less. Starting... eventually.",
            "Happy New Year! Let's try not to ruin it by February."),
        "Kenny" to listOf(
            "(muffled) MMPH! *fireworks oohing*",
            "(muffled) Rmph rmph! *happy new year wiggle*"),
        "Butters" to listOf(
            "Happy New Year, fellas! My resolution is to be the best boy ever!",
            "Oh boy, fireworks! I'm not scared! ...Okay, a little scared!")),
    "valentine" to mapOf(
        "Cartman" to listOf(
            "Valentine's Day is a scam invented by greeting card companies. Give me chocolate anyway.",
            "I got myself the biggest box of chocolates. Self-love, dude."),
        "Stan" to listOf(
            "Dude... Wendy... I... never mind.",
            "Valentine's Day. My stomach hurts already."),
        "Kyle" to listOf(
            "It's just a Hallmark holiday. ...Did anyone get me anything? No? Cool.",
            "Bebe's handing out valentines like it's a competition."),
        "Kenny" to listOf(
            "(muffled) Mmph rmph! *hands you a crooked heart* Rmph!",
            "(muffled) Rmph! *blows a kiss*"),
        "Butters" to listOf(
            "Happy Valentine's Day, fellas! I made cards for EVERYBODY!",
            "Oh boy, I love love! Love is the best! ...What's love?")),
    "july4" to mapOf(
        "Cartman" to listOf(
            "America! Fireworks! Cheesy Poofs! This is MY holiday!",
            "I'm the most patriotic. I said it, so it's true."),
        "Stan" to listOf(
            "Dude, fireworks. My dad's in charge. Pray.",
            "Happy 4th. Try to keep all your fingers."),
        "Kyle" to listOf(
            "Celebrating independence by blowing stuff up. Makes sense.",
            "My mom says fireworks are dangerous. My mom is right."),
        "Kenny" to listOf(
            "(muffled) MMPH! *oohs at fireworks*",
            "(muffled) Rmph rmph! *waves tiny flag*"),
        "Butters" to listOf(
            "Happy birthday, America! Oh boy, fireworks!",
            "I love the 4th of July! Kaboom, fellas!")),
    "cowdays" to mapOf(
        "Cartman" to listOf(
            "Moo. ...What? It's Cow Days. Moo.",
            "I'm boycotting Cow Days. The cows know what they did."),
        "Stan" to listOf(
            "Dude. Cow Days. Just... moo.",
            "The whole town's obsessed with cows again. Moo."),
        "Kyle" to listOf(
            "Moo. I hate that I said that.",
            "Cow Days is stupid. Moo."),
        "Kenny" to listOf(
            "(muffled) Moo. Rmph.",
            "(muffled) Mmmph moo!"),
        "Butters" to listOf(
            "Moo, fellas! Moo! Oh boy, Cow Days!",
            "I'm gonna pet a cow! Gently! Moo!")),
    "stpatricks" to mapOf(
        "Cartman" to listOf(
            "I'm 100% Irish today. That means I get extra snacks. It's the law.",
            "Kiss me, I'm... whatever. Just give me candy."),
        "Stan" to listOf(
            "Dude, Randy's doing green beer again. Hide.",
            "Happy St. Patrick's Day. Try not to get pinched."),
        "Kyle" to listOf(
            "I'm not even Irish and I'm wearing green. Peer pressure.",
            "Cartman pinched me. I'm filing a complaint."),
        "Kenny" to listOf(
            "(muffled) Mmph rmph! *green shirt wiggle*",
            "(muffled) Rmph! *pinches Cartman*"),
        "Butters" to listOf(
            "Oh boy, St. Patrick's Day! I'm wearin' green! Don't pinch me, fellas!",
            "I caught a leprechaun! ...It was a garden gnome. Still counts!")),
    "blackfriday" to mapOf(
        "Cartman" to listOf(
            "BLACK FRIDAY! I'm getting a Haibo AND trampling people! Multitasking!",
            "Out of my way, poor people! These deals are MINE!"),
        "Stan" to listOf(
            "Dude, the mall is a war zone. My dad's a security guard. Pray for everyone.",
            "Black Friday in South Park. People are literally fighting over blenders."),
        "Kyle" to listOf(
            "This is everything wrong with consumerism. ...Did you see the TV deals though?",
            "Cartman pepper-sprayed an old lady for a doll. I'm calling the news."),
        "Kenny" to listOf(
            "(muffled) MMPH! *dives into the crowd*",
            "(muffled) Rmph rmph! *holds up a toaster triumphantly*"),
        "Butters" to listOf(
            "Oh boy, Black Friday! Everybody's hugging! ...Aggressively!",
            "I got trampled, fellas! But I got the last doll! Worth it!"))
)

// ---------------- Seasons ----------------
fun seasonFor(month: Int): Season = when (month) {
    12, 1, 2 -> Season.WINTER
    3, 4, 5 -> Season.SPRING
    6, 7, 8 -> Season.SUMMER
    else -> Season.FALL
}

val SEASON_LINES: Map<Season, String> = mapOf(
    Season.WINTER to "Deep winter. The snow's got snow on it. Classic Colorado.",
    Season.SPRING to "Spring-ish. The slush is back and so is the mud. Still freezing.",
    Season.SUMMER to "Summer in South Park: sunny, freezing, and the snow's just showing off now.",
    Season.FALL to "Fall. Leaves everywhere, sky's gray, everything smells like wet dog."
)

// ---------------- NPC <-> NPC relationships (lorebook-flavored) ----------------
// Canon bonds used as dialogue/placement flavor: referenced in talk,
// shown in the Journal, and they bias schedule drift (NPCs drift toward
// the people they're connected to). Keyed by unordered name pair.
val NPC_RELATIONS: Map<Set<String>, String> = mapOf(
    setOf("Stan", "Kyle") to "best friends",
    setOf("Cartman", "Kyle") to "arch-rivals",
    setOf("Cartman", "Wendy") to "classroom rivals",
    setOf("Cartman", "Butters") to "exploiter and favorite victim",
    setOf("Stan", "Wendy") to "on-again off-again sweethearts",
    setOf("Craig", "Tweek") to "boyfriends",
    setOf("Craig", "Clyde") to "gang buddies",
    setOf("Jimmy", "Timmy") to "best buds and comedy rivals",
    setOf("Michael", "Pete") to "goth confidants",
    setOf("Michael", "Henrietta") to "goth confidants",
    setOf("Pete", "Henrietta") to "goth confidants",
    setOf("Wendy", "Bebe") to "best friends",
    setOf("Randy", "Sharon") to "married (somehow)",
    setOf("Gerald", "Sheila") to "married",
    setOf("Stuart", "Carol") to "married",
    setOf("Stephen", "Linda") to "married",
    setOf("Cartman", "Liane") to "mother and son",
    setOf("Stan", "Randy") to "father and son",
    setOf("Stan", "Sharon") to "mother and son",
    setOf("Kyle", "Gerald") to "father and son",
    setOf("Kyle", "Sheila") to "mother and son",
    setOf("Kenny", "Stuart") to "father and son",
    setOf("Kenny", "Carol") to "mother and son",
    setOf("Butters", "Stephen") to "father and son (grounded)",
    setOf("Butters", "Linda") to "mother and son",
    setOf("Jimbo", "Stuart") to "old hunting buddies",
    setOf("Randy", "Gerald") to "dad friends",
    setOf("Randy", "Towelie") to "business partners at Tegridy",
    setOf("Garrison", "Mackey") to "school colleagues",
    setOf("Jesus", "Father Maxi") to "clergy colleagues",
    setOf("Tolkien", "Craig") to "gang buddies",
    // v0.3.0b: wave-2 relations (scripted pairs first).
    setOf("Mr. Tweek", "Mrs. Tweek") to "married (the calm and the calmer)",
    setOf("Mr. Tweek", "Tweek") to "father and son",
    setOf("Mrs. Tweek", "Tweek") to "mother and son",
    setOf("Jimbo", "Ned") to "old hunting partners",
    setOf("Heidi", "Cartman") to "exes (it's a whole thing; it's over)",
    setOf("McDaniels", "Barbrady") to "mayor and her one-man police force",
    setOf("Nichole", "Tolkien") to "dated in fourth grade; good friends now",
    setOf("Heidi", "Wendy") to "girls' circle diplomats",
    setOf("Red", "Bebe") to "girls' circle enforcers",
    setOf("Kevin", "Craig") to "gang buddies",
    setOf("Crabtree", "Garrison") to "school colleagues (volume division)",
    setOf("Barbrady", "Jimbo") to "drinking buddies",
    setOf("DogPoo", "Butters") to "the overlooked and the grounded",
    // v0.7.0a: wave-3 relations (scripted pairs first).
    setOf("Karen", "Kenny") to "little sister and her guardian angel",
    setOf("Karen", "Carol") to "mother and daughter",
    setOf("Karen", "Stuart") to "father and daughter",
    setOf("Scott", "Butters") to "the hero in training and his first recruit",
    setOf("Scott", "Cartman") to "the mocked and the mocker (the hero trains anyway)",
    setOf("Firkle", "Michael") to "the littlest goth and his dark mentor",
    setOf("Firkle", "Pete") to "goth acolyte and poet",
    setOf("Firkle", "Henrietta") to "goth acolyte and diarist",
    setOf("Bradley", "Henrietta") to "little brother and big sister (the normal one and the other one)",
    setOf("Bradley", "Butters") to "the quiet heroes' union, snack division",
    setOf("Annie", "Wendy") to "the circle's intel and its president",
    setOf("Annie", "Bebe") to "girls' circle correspondents",
    setOf("Skeeter", "Stuart") to "bartender and his most constitutional regular",
    setOf("Skeeter", "Randy") to "bartender and debate-club president (self-appointed)",
    setOf("Victoria", "Garrison") to "principal and her thickest personnel file",
    setOf("Victoria", "Mackey") to "consequences and feelings, co-counselors",
    setOf("Marvin", "Randy") to "father and son (the shelf still leans)",
    setOf("Marvin", "Stan") to "grandfather and Billy",
    setOf("Marvin", "Sharon") to "the house's sense and its senior reviewer",
    setOf("Mr. Tucker", "Craig") to "father and son (factory settings)",
    setOf("Mr. Tucker", "Skeeter") to "the end stool is his by constitution",
    setOf("Steve", "Tolkien") to "father and son (the defence is hereditary)",
    setOf("Steve", "Randy") to "empire builders, different empires",
    // v0.8.0a: wave-4 relations (scripted pairs first).
    setOf("Ike", "Kyle") to "little brother and big brother (the kick is forbidden)",
    setOf("Ike", "Sheila") to "mother and son (the tiny diplomat)",
    setOf("Kevin McCormick", "Kenny") to "big brother and the angel's ground crew",
    setOf("Kevin McCormick", "Karen") to "big brother and little sister (the house catches you)",
    setOf("Shelley", "Stan") to "big sister and little brother (the diary is never wrong)",
    setOf("Shelley", "Randy") to "father and daughter (the sign has a sign)",
    setOf("Laura", "Mr. Tucker") to "married (the smile and the silence)",
    setOf("Laura", "Craig") to "mother and son",
    setOf("Mephesto", "Chef") to "old friends from the town's weirder years",
    setOf("Yates", "Barbrady") to "the sergeant and his one-man force",
    setOf("Adler", "Garrison") to "school colleagues (the shop and the classroom)",
    setOf("Lu Kim", "Mr. Tweek") to "rival food men of Main Street (honourable war)",
    setOf("Ike", "Gerald") to "father and son",
    setOf("Kevin McCormick", "Stuart") to "father and son",
    setOf("Kevin McCormick", "Carol") to "mother and son",
    setOf("Shelley", "Sharon") to "mother and daughter",
    setOf("Shelley", "Marvin") to "grandfather and granddaughter (she answers to Billy too)",
    setOf("Yates", "McDaniels") to "the mayor's actual detective",
    setOf("Adler", "Victoria") to "principal and her safest pair of hands",
    setOf("Lu Kim", "Randy") to "chef and his most demanding regular",
    // v0.9.0a: wave-5 relations (scripted pairs first).
    setOf("Roger", "Clyde") to "father and son (the lemon bars are scripture)",
    setOf("Ryan", "Jimmy") to "father and son (the chief and the headliner)",
    setOf("Sarah", "Jimmy") to "mother and son (the notebook is an archive)",
    setOf("Ryan", "Sarah") to "married (the drill and the dinner)",
    setOf("Alexis", "Wendy") to "mother and daughter (the debate is hereditary)",
    setOf("Mr. Testaburger", "Wendy") to "father and daughter (the kettle and the closing statement)",
    setOf("Alexis", "Mr. Testaburger") to "married (the agenda and the one sentence)",
    setOf("Nathan", "Jimmy") to "nemesis and headliner (the confetti budget is known)",
    setOf("Nathan", "Timmy") to "nemesis and the final boss (possibly)",
    setOf("Sophie", "Scott") to "fellow diplomats of the same small country (the snack treaty)",
    setOf("Sophie", "Wendy") to "the correspondent and the president (bureau of Scottsdale affairs)",
    setOf("Big Gay Al", "Chef") to "old friends from the boat ride days (the song is load-bearing)",
    setOf("Roger", "Stephen") to "the louder parish and the grounder (a fence away from a sense of humor)",
    setOf("Ryan", "McDaniels") to "the chief and the mayor (the hydrants have representation)",
    setOf("Alexis", "McDaniels") to "council colleagues (the gavel and the clipboard)",
    setOf("Big Gay Al", "Jimbo") to "the sanctuary and the old scoutmaster's other life",
    setOf("Sophie", "Bebe") to "the new bureau and the minister of morale",
    setOf("Ryan", "Mr. Testaburger") to "the loud chief and the quiet dad (the town's whole engine)",
    setOf("Sarah", "Alexis") to "the notebook and the clipboard (front row meets the record)",
    setOf("Nathan", "Sophie") to "the schemer and the other new kid (both keep notebooks)",
    // wave 6 (v0.11.0d): the audit's relations
    setOf("Pip", "Butters") to "the two boys the weather hits first (an umbrella for each other)",
    setOf("Pip", "Dougie") to "the replaced and the replacement (the polite and the loyal)",
    setOf("Dougie", "Butters") to "best friends (the general and his whole army's heart)",
    setOf("Jason", "Craig") to "the league and the table (the good ball is the treaty)",
    setOf("Terrance Mephesto", "Mephesto") to "father and son (four asses of opposition, responsibly)",
    setOf("Terrance Mephesto", "Stan") to "the original rivals (the pig, the song, the annotated grudge)",
    setOf("Terrance Mephesto", "Nathan") to "the schemer and the hereditary schemer (levels, Nathan)",
    setOf("Jenny", "Wendy") to "the record and the voice (the circle's whole government)",
    setOf("Jenny", "Bebe") to "the archive and the weather (a climate with minutes)",
    setOf("Flora", "Ike") to "the deciding vote and the president (grape juice and crackers)",
    setOf("Filmore", "Ike") to "the challenger and the administration (crackers across the aisle)",
    setOf("Quaid", "Filmore") to "the second chair and the candidate (the whole furniture)",
    setOf("Mimsy", "Nathan") to "the henchman and the plan (the scheme that never fails is loyalty)",
    setOf("Mimsy", "Jimmy") to "the surveillance and the show (front row, every time)",
    setOf("Tricia", "Craig") to "sister and brother (the archive and the finger)",
    setOf("Tricia", "Laura") to "daughter and mother (the Tucker girl package)",
    setOf("Mr. Slave", "Big Gay Al") to "married (the bass line and the sparkle)",
    setOf("Mr. Slave", "Garrison") to "the assistant years (the file is thick, the marriage is happy)",
    setOf("Linda Black", "Steve") to "married (the welcome and the chef — the roast is a draft until Thursday)",
    setOf("Linda Black", "Tolkien") to "mother and son (specific and gentle — the welcome is the policy)",
    setOf("Harriet", "Henrietta") to "mother and daughter (praise through the door, thunder behind it)",
    setOf("Harriet", "Mr. Biggle") to "married (the roof and the foundation)",
    setOf("Mr. Biggle", "Bradley") to "father and son (chosen, apprenticed, and in the work)",
    setOf("Mr. Turner", "Heidi") to "father and daughter (the perimeter negotiates in love)",
    setOf("Nancy", "Heidi") to "mother and daughter (strong and open — wall and door)",
    setOf("Mr. Stevens", "Bebe") to "father and daughter (the umbrella of reviews)",
    setOf("Mrs. Stevens", "Bebe") to "mother and daughter (the original and the storm system)",
    setOf("Mr. Stoley", "Kevin") to "father and son (the family silver is a wig)",
    setOf("Mrs. Stoley", "Kevin") to "mother and son (the director and the point)",
    setOf("Mr. Stoley", "Mrs. Stoley") to "married (the producer and the director)",
    setOf("Clark", "Scott") to "father and son (decisive, hereditary — four seconds)",
    setOf("Maggie", "Yates") to "married (the appeal court and the certainty — the kettle cracks cases)",
    setOf("Mitch", "Yates") to "the aide-de-camp and the sergeant (the pin says ask Maggie)",
    setOf("Mitch", "Barbrady") to "the files and the morale (the pen did policing)",
    setOf("Bob Johnson", "McDaniels") to "the aide and the mayor (the scissors tray, for luck)",
    setOf("Pusslicker", "Jimmy") to "the desk and the act (the pause is the whole trick)",
    setOf("Dr. Doctor", "Randy") to "the doctor and the biography (two binders, chaptered)",
    setOf("Choksondik", "Garrison") to "the succession (chaos, order, chaos with notes)",
    setOf("Choksondik", "Stan") to "the teacher and the Marsh boy (names said correctly)",
    setOf("Strong Woman", "Victoria") to "the standard and the schedule (the school's two arms)",
    setOf("Strong Woman", "Mackey") to "the door and the counselor (a posture with a memory)"
)

fun relationNote(a: String, b: String): String? = NPC_RELATIONS[setOf(a, b)]

/** People [name] is connected to (drift + dialogue flavor). */
fun relationsOf(name: String): List<String> =
    NPC_RELATIONS.keys.filter { name in it }.map { pair -> pair.first { it != name } }

/** Multiplier on WEATHER_DRIFT weights per season. It's still Colorado — always some snow. */
val SEASON_WEATHER_MULT: Map<Season, Map<String, Double>> = mapOf(
    Season.WINTER to mapOf("sunny" to 0.5, "cloudy" to 1.0, "light_snow" to 1.6,
        "heavy_snow" to 1.5, "blizzard" to 1.4, "rain" to 0.4),
    Season.SPRING to mapOf("sunny" to 1.1, "cloudy" to 1.0, "light_snow" to 1.1,
        "heavy_snow" to 0.8, "blizzard" to 0.4, "rain" to 1.3),
    Season.SUMMER to mapOf("sunny" to 1.8, "cloudy" to 1.2, "light_snow" to 0.6,
        "heavy_snow" to 0.4, "blizzard" to 0.2, "rain" to 0.9),
    Season.FALL to mapOf("sunny" to 0.8, "cloudy" to 1.5, "light_snow" to 1.0,
        "heavy_snow" to 0.8, "blizzard" to 0.4, "rain" to 1.1)
)

package com.hunt3r.southparktown

// ============================================================
// Factions — the town's living political layer (v0.1.5).
//
// Factions are derived from the groups the lorebook documents:
// the boys' crew, the goth kids, the girls' circle, Craig's
// gang, the parents, school staff, clergy, and the Tegridy /
// local-business crew. Every cast member belongs to exactly
// one (canon affiliations); generated lorebook NPCs join one
// when their entry affiliates them, else stay unaffiliated.
//
// State persists in the save: influence (0..100, drifts back
// toward baseline between events), the player's standing with
// each faction (-50..100), inter-faction relation (-2..+2),
// and a mood note. Shenanigans event stages carry FactionEffects
// (data) that push the landscape around; relationship gains
// with members nudge player standing automatically.
// ============================================================

data class FactionDef(
    val id: String,
    val name: String,
    val icon: String,
    val blurb: String,
    val members: List<String>,      // cast names
    val turf: List<String>,         // location ids
    val baseInfluence: Int,
    val baseRelations: Map<String, Int> = emptyMap() // factionId -> -2..+2
)

val FACTIONS: List<FactionDef> = listOf(
    FactionDef("boys_crew", "The Boys' Crew", "🧢",
        "Cartman's crew. Runs the bus stop, the pond, and most of the town's bad ideas.",
        listOf("Cartman", "Stan", "Kyle", "Kenny", "Butters", "Francis", "DogPoo", "Karen", "Bradley", "Ike", "Kevin McCormick", "Pip", "Dougie", "Flora", "Filmore", "Quaid"),
        listOf("BusStop", "CartmanHouse", "StarksPond"), 55,
        mapOf("girls_circle" to -1, "craigs_gang" to 1, "goth_kids" to -1)),
    FactionDef("goth_kids", "The Goth Kids", "🦇",
        "Coffee, clove smoke, and contempt for conformists. Behind the school, always.",
        listOf("Michael", "Pete", "Henrietta", "Firkle"),
        listOf("Elementary", "KidsPark"), 35,
        mapOf("boys_crew" to -1, "girls_circle" to -1)),
    FactionDef("girls_circle", "The Girls' Circle", "💄",
        "Wendy and Bebe's circle — now with Heidi's diplomacy, Red's enforcement, Nichole's calm, and Annie's intelligence service. Organized, list-making, and not to be crossed.",
        listOf("Wendy", "Bebe", "Heidi", "Nichole", "Red", "Annie", "Shelley", "Sophie", "Jenny", "Tricia"),
        listOf("Elementary", "Mall"), 45,
        mapOf("boys_crew" to -1)),
    FactionDef("craigs_gang", "Craig's Gang", "👆",
        "Craig's crew. Deadpan, unbothered, flipping off the world since third grade.",
        listOf("Craig", "Tweek", "Clyde", "Jimmy", "Timmy", "Tolkien", "Kevin", "Scott", "Nathan", "Jason", "Terrance Mephesto", "Mimsy"),
        listOf("Elementary", "KidsPark", "TweekBros"), 45,
        mapOf("boys_crew" to 1)),
    FactionDef("parents", "The Parents", "🍷",
        "The grown-ups. Skeeter's regulars, PTA warriors, backyard schemers — plus City Hall and the entire police force.",
        listOf("Randy", "Sharon", "Gerald", "Sheila", "Liane", "Stuart", "Carol", "Stephen", "Linda", "McDaniels", "Barbrady", "Skeeter", "Marvin", "Mr. Tucker", "Steve", "Laura", "Mephesto", "Yates", "Roger", "Ryan", "Sarah", "Alexis", "Mr. Testaburger", "Big Gay Al", "Mr. Slave", "Linda Black", "Harriet", "Mr. Biggle", "Mr. Turner", "Nancy", "Mr. Stevens", "Mrs. Stevens", "Mr. Stoley", "Mrs. Stoley", "Clark", "Maggie", "Mitch", "Bob Johnson", "Pusslicker", "Dr. Doctor"),
        listOf("SkeetersBar", "StanHouse", "KyleHouse"), 50,
        mapOf("school_staff" to 1, "tegridy_crew" to -1)),
    FactionDef("school_staff", "School Staff", "🏫",
        "Garrison, Mackey, Chef, Crabtree's bus (the loudest school asset on wheels), Adler's shop class — and Principal Victoria's clipboard over all of it. Holding the school together with twine and denial.",
        listOf("Garrison", "Mackey", "Chef", "Crabtree", "Victoria", "Adler", "Choksondik", "Strong Woman"),
        listOf("Elementary"), 40,
        mapOf("parents" to 1)),
    FactionDef("church_faithful", "Church & Faithful", "⛪",
        "Father Maxi's flock and one very patient Jesus. Sunday strong, weekday nervous.",
        listOf("Jesus", "Father Maxi"),
        listOf("Church"), 35,
        mapOf("goth_kids" to -1)),
    FactionDef("tegridy_crew", "Tegridy & Local Business", "🌿",
        "Jimbo's gun counter, Ned's silence, the Tweeks' coffee empire, Lu Kim's wok, and Towelie's farm dreams. Commerce, Colorado style.",
        listOf("Jimbo", "Towelie", "Mr. Tweek", "Mrs. Tweek", "Ned", "Lu Kim"),
        listOf("JimbosGuns", "TegridyFarms"), 40,
        mapOf("parents" to -1))
)

val FACTION_BY_ID: Map<String, FactionDef> = FACTIONS.associateBy { it.id }

val FACTION_OF_CAST: Map<String, String> = run {
    val m = mutableMapOf<String, String>()
    for (f in FACTIONS) for (mem in f.members) m[mem] = f.id
    m
}

fun factionOfCast(name: String): String? = FACTION_OF_CAST[name]

/** Persistent per-faction state (saved with the game). */
data class FactionState(
    var influence: Int = 50,
    var playerStanding: Int = 0,
    var mood: String = "Business as usual.",
    /** v0.1.8: mood is MECHANICAL now (-2..+2), set by weather + recent
     *  events; it scales greeting warmth, ambient friendliness, drift. */
    var moodScore: Int = 0
)

object Factions {

    fun defaultStates(): MutableMap<String, FactionState> {
        val m = mutableMapOf<String, FactionState>()
        for (f in FACTIONS) m[f.id] = FactionState(influence = f.baseInfluence)
        return m
    }

    fun defaultRelations(): MutableMap<String, Int> {
        val m = mutableMapOf<String, Int>()
        for (f in FACTIONS) for ((other, v) in f.baseRelations) {
            m[relationKey(f.id, other)] = v
        }
        return m
    }

    fun relationKey(a: String, b: String): String =
        if (a < b) "$a|$b" else "$b|$a"

    fun relationLabel(v: Int): String = when {
        v >= 2 -> "🤝 Allied"
        v == 1 -> "🙂 Friendly"
        v == 0 -> "😐 Neutral"
        v == -1 -> "😤 Rivals"
        else -> "⚔️ At war"
    }

    /** Weather -> faction mood pressure (-1..+1). Goths love the gloom,
     *  Tegridy loves the sun, everyone hates a blizzard except the goths. */
    val WEATHER_MOOD: Map<String, Map<String, Int>> = mapOf(
        "boys_crew" to mapOf("sunny" to 1, "blizzard" to -1, "heavy_snow" to -1),
        "goth_kids" to mapOf("fog" to 1, "blizzard" to 1, "sunny" to -1, "cloudy" to 1),
        "girls_circle" to mapOf("sunny" to 1, "rain" to -1),
        "craigs_gang" to mapOf("wind" to -1, "sunny" to 1),
        "parents" to mapOf("sunny" to 1, "blizzard" to -1, "rain" to -1),
        "school_staff" to mapOf("heavy_snow" to 1, "blizzard" to 1, "sunny" to -1),
        "church_faithful" to mapOf("fog" to 1, "sunny" to 1),
        "tegridy_crew" to mapOf("sunny" to 1, "rain" to 1, "blizzard" to -1)
    )

    /** A faction's mechanical mood: its stored event swing (set by
     *  stage effects, decaying daily) + live weather pressure. */
    fun moodIndex(factionId: String, state: FactionState, weatherId: String): Int =
        (state.moodScore + (WEATHER_MOOD[factionId]?.get(weatherId) ?: 0)).coerceIn(-2, 2)

    fun moodLabel(score: Int): String = when {
        score >= 2 -> "riding high"
        score == 1 -> "in good spirits"
        score == 0 -> "even-keeled"
        score == -1 -> "sour"
        else -> "furious"
    }

    /** Starting relationship points with a generated member of a faction
     *  the player has standing with (standing by association). */
    fun associationStart(playerStanding: Int): Int =
        (playerStanding / 8).coerceIn(-5, 10)

    fun standingLabel(standing: Int): String = when {
        standing >= 60 -> "Beloved"
        standing >= 30 -> "Respected"
        standing >= 10 -> "Liked"
        standing > -10 -> "Unknown"
        standing > -30 -> "Suspect"
        else -> "Persona non grata"
    }

    /** Daily settle: influence eases 2 points back toward baseline. */
    fun driftTowardBaseline(states: MutableMap<String, FactionState>) {
        for (f in FACTIONS) {
            val s = states[f.id] ?: continue
            s.influence += when {
                s.influence < f.baseInfluence -> 2
                s.influence > f.baseInfluence -> -2
                else -> 0
            }
            s.influence = s.influence.coerceIn(0, 100)
        }
    }

    /** Affiliation keywords for generated lorebook NPCs (NpcGen scans entry text). */
    val AFFILIATION_KEYWORDS: List<Pair<String, List<String>>> = listOf(
        "goth_kids" to listOf("goth"),
        "church_faithful" to listOf("priest", "pastor", "nun", "missionary", "religious", "church"),
        "school_staff" to listOf("teacher", "principal", "counselor", "school nurse", "janitor at the school"),
        "parents" to listOf("mother of", "father of", "housewife", "parent of"),
        "tegridy_crew" to listOf("shopkeeper", "store owner", "businessman", "gun shop", "farmer", "hemp"),
        "craigs_gang" to listOf("fourth grader", "fourth-grader", "student at south park elementary"),
        "girls_circle" to listOf("cheerleader", "girls' list", "popular girl")
    )
}

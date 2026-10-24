package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Heroes — v0.10.0b (Queue #10, item 2): HERO NIGHTS.
//
// After dark, the kids put on capes. Twice a week the town's
// kid cast patrols its streets in costume — every persona
// ORIGINAL: invented names, invented powers, costume specs
// derived from each kid's own measured palette (cape + mask
// + emblem over their own colors). Nothing here borrows a
// show hero's name, suit, or signature; the suite guards
// the roster against franchise terms the way Pocket Pals'
// guard watches the card binder.
//
// The layer is honest about what it is: costumes. Under the
// mask everyone is still themselves — agendas, friendships,
// talk and cases read the real kid. The patrol is an agenda
// claim in the established stack (placed after the rival
// crew's turf claim, before your crew's hook) and it yields
// like the lowest-stakes claims do: to every town-story
// moment (the momentOwned set less a Bijou screening —
// a film owns its cinema, not the streets, and Saturday
// is a screening night), the crew build's busyReason
// wholesale (dinner, work,
// movie, ceremony, finale, a live climax, an emergency), the
// fair's claim, and your family's dinner-guest claim. Your
// parents' table is never eaten by a patrol: patrol hours
// start at 19:30 but a family-dinner GUEST is claimed by
// Family first, and Mom and Dad have opinions about the
// whole arrangement (a morning-after text; a remark in
// person the next day).
//
// Beats ride the existing verbs: a Look at a patrol spot
// finds the night's small "crime" (kid-scale, settled in the
// telling, like the lost-and-found), Talk to someone out
// after dark offers an escort home priced by the Walk graph,
// and the camera tells the truth — costumed figures are
// costumed in the photo record itself, so the album, the
// darkroom and the yearbook all see the capes.
//
// THE CAPER: a season-long original caper in the StoryArcs
// pattern — five movements on patrol nights, chapters gated
// on patrol nights attended, a finale set piece with a
// stance choice, persistent marks, ambient callbacks. It is
// a STORY, not a case: no evidence machinery, no accusation
// — the chapters carry it. Its secret is a story secret and
// stays out of docs and player-facing text until the finale
// itself speaks it.
//
// State (all additive, riding life/lifeText):
//   heroMade (absDay), heroName, heroPower — your identity
//   heroPatrolNights, heroPatrolDay (last night counted),
//   heroOutDay (last day you did any patrol beat),
//   heroLastPatrol (the patrol day the phone reads)
//   heroAssists, heroIncidents, heroEscorts — the tallies
//   heroInc:<loc> = absDay an incident settled there
//   heroEscort:<name>, heroTalk:<name>, heroRemark:<parent>
//     = absDay stamps (bounded: one key per spot/name)
//   caperStage (0 unbegun, 1..4 chapter open, 5 finale due,
//     6 done), caperHelped, caperBeatDay, caperStance,
//     caperDoneDay
// ============================================================

/** One kid's hero persona: an original name and power, and
 *  a costume read off their own measured colors — [cape]
 *  and [mask] are hex accents, [emblem] a shape token the
 *  renderer knows (star | bolt | moon | shield | paw | sun |
 *  wave | crown | eye | gear | comet | drop). */
data class HeroPersona(
    val kid: String,
    val heroName: String,
    val power: String,
    val cape: String,
    val mask: String,
    val emblem: String
)

object Heroes {

    // ---------------- the cadence ----------------

    /** Patrol nights: Wednesday and Saturday (Jobs.weekdayOf:
     *  0 = Sunday). Midweek for the thrill of a school night,
     *  Saturday for the big one; movie nights keep Friday. */
    val PATROL_WEEKDAYS: Set<Int> = setOf(3, 6)

    /** Patrol hours: 19:30 until the 22:00 sleep hour takes
     *  everyone home. Late enough to feel like night work,
     *  early enough that nobody's agenda breaks. */
    const val PATROL_FROM = 19 * 60 + 30
    const val PATROL_TO = 22 * 60

    /** The patrol spots — outdoor, lamplit, defensible. */
    val PATROL_SPOTS: List<String> = listOf(
        "KidsPark", "MainStreet", "StarksPond", "Elementary", "Mall")

    // ---------------- the roster ----------------
    // Sixteen personas: the core gang, Craig's gang, the
    // girls' circle, and the two kids who were never going
    // to be left out of a costume night. Every name, power
    // and colorway invented for this game.

    val ROSTER: List<HeroPersona> = listOf(
        HeroPersona("Stan", "The Standard",
            "THE TRUTH WHISTLE — one blast and anyone lying " +
                "gets uncontrollably itchy",
            "#d43d2a", "#2a4a8a", "star"),
        HeroPersona("Kyle", "The Verdict",
            "THE UNANSWERABLE ARGUMENT — no villain has " +
                "ever won the rebuttal",
            "#e07b2a", "#2a9a4a", "shield"),
        HeroPersona("Cartman", "The Heavyweight",
            "THE COMMAND VOICE — everyone in earshot does " +
                "one thing he says (usually snack-related)",
            "#7a4a9a", "#2aa8a8", "crown"),
        HeroPersona("Kenny", "Second Verse",
            "ALWAYS BACK TOMORROW — whatever happens " +
                "tonight, he's fine by morning",
            "#8a2a2a", "#e07b2a", "moon"),
        HeroPersona("Butters", "The Good Sport",
            "WEAPONIZED MANNERS — please, thank you, and a " +
                "grounding you'll never see coming",
            "#7ac8e8", "#e8c53a", "sun"),
        HeroPersona("Craig", "Flatline",
            "THE SHRUG — panic cannot spread past him; it " +
                "arrives, looks at him, and gives up",
            "#2a2a4a", "#2a6fd4", "wave"),
        HeroPersona("Tweek", "The Jitterbug",
            "CAFFEINE OVERDRIVE — at full jitter he is " +
                "effectively in two places at once",
            "#4a7a3a", "#e8c53a", "bolt"),
        HeroPersona("Clyde", "Floodgate",
            "THE FEELINGS FLOOD — cries on command, and " +
                "everybody in the splash zone feels things",
            "#8a2a4a", "#a83a5a", "drop"),
        HeroPersona("Tolkien", "The Upgrade",
            "A GADGET FOR EVERYTHING — allowance-funded, " +
                "warranty-free, mostly load-bearing",
            "#5a3a8a", "#c8a878", "gear"),
        HeroPersona("Jimmy", "The Punchline",
            "FLAWLESS AT HERO SPEED — the delivery is the " +
                "superpower; the jokes are a bonus",
            "#c83a2a", "#2a6fd4", "star"),
        HeroPersona("Timmy", "The Comet",
            "TURBO WHEELS — and the loudest battle cry in " +
                "Colorado (it's his name, at volume)",
            "#d43d2a", "#3a3a3a", "comet"),
        HeroPersona("Wendy", "The Brief",
            "A FORTY-PAGE REPORT ON ANY VILLAIN — " +
                "researched, cited, delivered overnight",
            "#8a2a6a", "#5a2a8a", "eye"),
        HeroPersona("Bebe", "The Look",
            "ONE LOOK ENDS ANY ARGUMENT — scientists are " +
                "still studying how",
            "#c82a5a", "#e8c53a", "eye"),
        HeroPersona("Heidi", "The Signal",
            "HEARS EVERY GROUP CHAT AT ONCE — no thread " +
                "in town is secret from her",
            "#2a8a7a", "#e87a9a", "wave"),
        HeroPersona("Red", "The Spark",
            "PERFECT TIMING — shows up the exact second " +
                "things are about to pop off",
            "#a82a2a", "#3a3a3a", "bolt"),
        HeroPersona("Scott", "The Meter",
            "NEVER PANICS — blood-sugar discipline is a " +
                "way of life, and fear is off the meal plan",
            "#2a7a4a", "#7a4a2a", "shield")
    )

    private val BY_KID: Map<String, HeroPersona> =
        ROSTER.associateBy { it.kid }

    /** The originality guard, Pocket Pals-style: terms from
     *  the hero franchises this game deliberately does NOT
     *  borrow — no name, power, chapter or line in this
     *  module may contain one. The suite scans every
     *  player-facing string here against it. (Whole phrases,
     *  so a trash-can raccoon stays legal.) */
    val DENYLIST: List<String> = listOf(
        "mysterion", "the coon", "coon and friends",
        "coon & friends", "mintberry", "mint berry",
        "toolshed", "tool shed", "tupperware", "human kite",
        "fastpass", "fast pass", "mosquito", "professor chaos",
        "general disarray", "wonder tweek", "super craig",
        "captain diabetes", "call girl", "freedom pals",
        "batman", "superman", "spider-man", "spiderman",
        "iron man", "wolverine", "deadpool", "the avengers",
        "justice league")

    fun persona(name: String): HeroPersona? = BY_KID[name]

    fun isRoster(name: String): Boolean = name in BY_KID

    // ---------------- your hero ----------------

    /** The name list the creation dialog offers — genre
     *  style, all invented here. */
    val NAME_OPTIONS: List<String> = listOf(
        "Captain Curfew", "The Porchlight", "Kid Comet",
        "The Night Shift", "The Snow Day", "Captain Recess")

    /** Power picks (id -> label). The emblem rides the id. */
    val POWERS: List<Pair<String, String>> = listOf(
        "speed" to "SUPER SPEED — there before the story " +
            "changes its mind",
        "strength" to "SUPER STRENGTH — can carry ALL the " +
            "groceries in one trip",
        "nightsight" to "NIGHT SIGHT — sees in the dark " +
            "like the dark owes him money",
        "snacksense" to "SNACK SENSE — always knows where " +
            "the nearest snack is, to the inch",
        "animals" to "ANIMAL TALKER — the raccoons respect " +
            "him, and the raccoons respect nobody",
        "weather" to "WEATHER CALLER — points at the sky " +
            "and the sky at least considers it")

    val POWER_EMBLEMS: Map<String, String> = mapOf(
        "speed" to "bolt", "strength" to "shield",
        "nightsight" to "moon", "snacksense" to "star",
        "animals" to "paw", "weather" to "sun")

    fun heroCreated(state: GameState): Boolean =
        (state.life["heroMade"] ?: 0) > 0 ||
            state.lifeText.containsKey("heroName")

    /** The creation beat is due: the town has had a day to
     *  be a town, and you have no cape yet. The dialog rides
     *  the safe-moment chain (GameActivity), below every
     *  climax and the family's moving day. */
    fun creationDue(state: GameState): Boolean =
        !heroCreated(state) && state.absDay >= 2

    fun createHero(state: GameState, heroName: String,
                   powerId: String): Boolean {
        if (heroCreated(state)) return false
        if (powerId !in POWER_EMBLEMS) return false
        if (heroName.isBlank()) return false
        state.lifeText["heroName"] = heroName
        state.lifeText["heroPower"] = powerId
        state.life["heroMade"] = state.absDay
        return true
    }

    /** The mirror route: re-pick name and power any time
     *  after the first creation (the identity is yours; the
     *  town adjusts). */
    fun changeHero(state: GameState, heroName: String,
                   powerId: String): Boolean {
        if (!heroCreated(state)) return false
        if (powerId !in POWER_EMBLEMS) return false
        if (heroName.isBlank()) return false
        state.lifeText["heroName"] = heroName
        state.lifeText["heroPower"] = powerId
        return true
    }

    fun heroName(state: GameState): String =
        state.lifeText["heroName"] ?: ""

    fun powerLabel(state: GameState): String =
        POWERS.firstOrNull { it.first == state.lifeText["heroPower"] }
            ?.second ?: ""

    /** Your costume, as a code ("cape|mask|emblem") frozen
     *  onto photo figures at shutter time. The cape wears
     *  your worn top's color and the mask its darker shade —
     *  your palette, like everyone else's. */
    fun playerCostumeCode(state: GameState): String {
        val me = state.agents.values.firstOrNull { it.isPlayer }
        return playerCostumeCodeFor(state,
            me?.appearance ?: emptyMap())
    }

    /** v0.15.0a: the same costume derivation read off an
     *  explicit [appearance] — the creator preview's door:
     *  while you edit a look on a patrol night, the preview
     *  capes the look UNDER EDIT, not the one still saved.
     *  One derivation: [playerCostumeCode] is this function
     *  read off the worn appearance, and LiveLook's preview
     *  reads it too, so the creator, the mirror, the street
     *  and the shutter can never cape two different palettes. */
    fun playerCostumeCodeFor(state: GameState,
                             appearance: Map<String, String>): String {
        val spec = avatarSpec(appearance)
        val cape = spec.top
        val mask = darkerHex(cape)
        val emblem = POWER_EMBLEMS[state.lifeText["heroPower"]]
            ?: "star"
        return "$cape|$mask|$emblem"
    }

    /** A roster kid's costume code, from their persona. */
    fun costumeCodeFor(name: String): String {
        val p = persona(name) ?: return ""
        return "${p.cape}|${p.mask}|${p.emblem}"
    }

    /** Parse a costume code; null when it isn't one. */
    fun parseCode(code: String): Triple<String, String, String>? {
        val parts = code.split("|")
        if (parts.size != 3) return null
        if (!parts[0].startsWith("#") || !parts[1].startsWith("#"))
            return null
        return Triple(parts[0], parts[1], parts[2])
    }

    /** THE COSTUME SPEC: the figure's own sheet, caped —
     *  the cape in the persona's color over the kid's own
     *  clothes (their colors ARE the costume; hats and hair
     *  rules untouched, specials kept). The mask and emblem
     *  are drawn over this by the renderer's hero pass. */
    fun costumeSpec(base: SheetSpec, code: String): SheetSpec {
        val (cape, _, _) = parseCode(code) ?: return base
        return base.copy(backStyle = "cape", backColor = cape)
    }

    // ---------------- cadence & the claim ----------------

    fun isPatrolNight(absDay: Int): Boolean =
        Jobs.weekdayOf(absDay) in PATROL_WEEKDAYS

    fun inWindow(timeMin: Int): Boolean =
        timeMin in PATROL_FROM until PATROL_TO

    /** Where [name] patrols tonight — seeded by the night,
     *  spread across the spots in roster order, so the whole
     *  patrol is a pure function of the day. */
    fun patrolSpotFor(state: GameState, name: String): String {
        val idx = ROSTER.indexOfFirst { it.kid == name }
        if (idx < 0) return PATROL_SPOTS[0]
        val order = PATROL_SPOTS.shuffled(
            Random(state.absDay * 7919L + 13))
        return order[idx % order.size]
    }

    /** The town-moment guard for the patrol: every part of
     *  Emergencies.momentOwned that owns the TOWN'S STORY —
     *  a case at its critical hour, a finale window, a
     *  holiday, the bee hours, a capstone ceremony — less
     *  one deliberate exception: a live Bijou screening.
     *  A film owns its cinema, not the streets; the patrol
     *  works through a screening (Saturday is a screening
     *  night — wholesale deference would gut half the
     *  cadence), and the screening's own audience is still
     *  claimed individually by the movie claim inside
     *  Crew.busyReason, which the patrol honors in full. */
    fun townMomentOwned(state: GameState): Boolean {
        if (Walk.caseCritical(state)) return true
        if (Finales.windowAt(state) != null) return true
        if (HolidayBeats.holidayOf(state) != null) return true
        if (School.isBeeDay(state.absDay) &&
            state.timeMin in School.BEE_FROM until School.BEE_TO)
            return true
        if (state.timeMin in Capstones.CEREMONY_FROM until
            Capstones.CEREMONY_TO &&
            Capstones.CHAINS.any {
                Capstones.activeBeat(state, it.factionId)?.beat == 3
            }) return true
        return false
    }

    /** THE PATROL CLAIM. Engine seats it after the rival
     *  crew's turf claim and before your crew's hook — and
     *  it self-guards like the turf claim does, because the
     *  stack seats by override order, not by importance:
     *  a bigger town moment (townMomentOwned), any
     *  occupation the crew build reads (busyReason: asleep,
     *  school, dinner, work, movie, ceremony, finale, a
     *  live climax's causers, a live emergency), the fair's
     *  claim, your family's dinner-guest claim, and a
     *  crewmate standing with you or walking to you all
     *  take the kid first. */
    fun gatheringBlock(state: GameState, name: String,
                       timeMin: Int): AgendaBlock? {
        val p = persona(name) ?: return null
        if (!isPatrolNight(state.absDay)) return null
        if (!inWindow(timeMin)) return null
        if (townMomentOwned(state)) return null
        if (Crew.busyReason(state, name) != null) return null
        if (Fair.gatheringBlock(state, name, timeMin) != null)
            return null
        if (Family.gatheringBlock(state, name, timeMin) != null)
            return null
        if (Crew.isMember(state, name) &&
            Crew.modeOf(state, name).let {
                it == "with" || it == "go" })
            return null
        return AgendaBlock(patrolSpotFor(state, name),
            "On patrol as ${p.heroName}.")
    }

    /** In costume RIGHT NOW: a roster kid standing at a
     *  patrol spot during patrol hours on a patrol night.
     *  Rendering and photos read this — the camera and the
     *  street agree. */
    fun inCostume(state: GameState, name: String): Boolean {
        if (persona(name) == null) return false
        if (!isPatrolNight(state.absDay)) return false
        if (!inWindow(state.timeMin)) return false
        val a = state.agents[name] ?: return false
        if (a.asleep) return false
        return a.location in PATROL_SPOTS
    }

    /** You, in costume: identity made, patrol night, patrol
     *  hours, standing at a patrol spot. */
    fun playerInCostume(state: GameState): Boolean {
        if (!heroCreated(state)) return false
        if (!isPatrolNight(state.absDay)) return false
        if (!inWindow(state.timeMin)) return false
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return false
        return me.location in PATROL_SPOTS && !state.playerInside
    }

    /** The costume code a photo figure carries at shutter
     *  time ("" = street clothes). */
    fun figureCode(state: GameState, fig: PhotoFigure): String {
        if (fig.kind == "player")
            return if (playerInCostume(state))
                playerCostumeCode(state) else ""
        if (fig.kind == "cast" && inCostume(state, fig.name))
            return costumeCodeFor(fig.name)
        return ""
    }

    /** v0.14.0a: YOUR COSTUME ON THE STREET — the code the
     *  street scene renders you in: your costume code
     *  while you stand a patrol spot in costume (exactly
     *  playerInCostume's moment — identity made, patrol
     *  night, patrol hours, outdoors at a spot), "" in
     *  street clothes, when the street stays first-person
     *  and you stay behind the camera. The code is
     *  figureCode's player branch read live, so the
     *  street wears the same derivation the camera
     *  freezes: one persona, one palette, every surface.
     *  Pure: rendering reads; it never writes. */
    fun streetCostumeCode(state: GameState): String =
        if (playerInCostume(state)) playerCostumeCode(state)
        else ""

    // ---------------- the tick ----------------

    /** Runs on Engine.advanceTime beside Crew.tick: the
     *  patrol's start is announced once a night, and
     *  standing a patrol spot in costume stamps the night
     *  as one you were OUT (the rollover counts it, the
     *  morning-after phone text reads it). */
    fun tick(state: GameState) {
        if (!isPatrolNight(state.absDay)) return
        if (!inWindow(state.timeMin)) return
        if (state.life["heroTickDay"] != state.absDay) {
            state.life["heroTickDay"] = state.absDay
            state.events.add("🦸 Patrol night — the town's " +
                "smallest heroes take the streets.")
            while (state.events.size > 40) state.events.removeAt(0)
        }
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return
        if (playerInCostume(state) ||
            state.agents.values.any {
                !it.isPlayer && inCostume(state, it.name) &&
                    it.location == me.location
            })
            state.life["heroOutDay"] = state.absDay
    }

    // ---------------- beats: the night's small crimes ----

    /** One incident per spot per night: what the Look finds,
     *  and the telling of its settling (kid-scale crime,
     *  hero-scale response). */
    data class Incident(val scene: String, val settled: String)

    fun incidentsFor(locId: String): List<Incident> = when (locId) {
        "KidsPark" -> listOf(
            Incident("🦝 Something is IN the park trash can. " +
                "The lid rattles. Whatever it is, it has " +
                "opinions and a wrapper stuck to its head.",
                "You lift the lid with full ceremony. A raccoon " +
                    "the size of a house cat accepts your help " +
                    "like a tiny king and waddles off into the " +
                    "dark. Case closed."),
            Incident("🧻 The big tree by the swings is wearing " +
                "toilet paper like it lost a bet.",
                "Twenty minutes, one trash bag, zero dignity " +
                    "lost — the tree is a tree again. Somewhere, " +
                    "a prankster feels a disturbance."))
        "StarksPond" -> listOf(
            Incident("🐸 The pond lantern is dark and a frog " +
                "is sitting on the post like it pays rent.",
                "Loose bracket, tightened. The frog supervises " +
                    "the whole repair and approves the light. " +
                    "The pond glows again."))
        "MainStreet" -> listOf(
            Incident("💡 One streetlamp is out, and its bulb is " +
                "sitting on the pavement below in a neat " +
                "little pile of exactly one bulb. Suspicious.",
                "You stand on the bench, screw the bulb back " +
                    "in, and Main Street is lit end to end " +
                    "again. The pile was VERY neat, though."))
        "Elementary" -> listOf(
            Incident("✏️ Chalk arrows on the school sidewalk — " +
                "every single one pointing the WRONG way, " +
                "toward the mud.",
                "You chalk them back, twice as big, with a " +
                    "small star at the turn. If anyone asks, " +
                    "the star was always there."))
        "Mall" -> listOf(
            Incident("🛒 A shopping cart is making a break for " +
                "it down the mall lot, rolling free toward " +
                "the road like it has somewhere to be.",
                "A running catch at the curb. The cart is " +
                    "returned to the corral with a warning. It " +
                    "does not look sorry."))
        else -> emptyList()
    }

    /** The incident waiting at [locId] right now, or null:
     *  patrol hours, seeded most nights, once per spot. */
    fun incidentAt(state: GameState, locId: String): Incident? {
        if (!isPatrolNight(state.absDay)) return null
        if (!inWindow(state.timeMin)) return null
        if (locId !in PATROL_SPOTS) return null
        if (state.life["heroInc:$locId"] == state.absDay)
            return null
        val pool = incidentsFor(locId)
        if (pool.isEmpty()) return null
        val rnd = Random(state.absDay * 131L + locId.hashCode())
        if (rnd.nextFloat() >= 0.75f) return null
        return pool[rnd.nextInt(pool.size)]
    }

    /** The Look hook: a patrol spot on a patrol night may
     *  hold the night's small crime — reading it settles it
     *  (the lost-and-found discipline: the careful Look IS
     *  the hero work). Also the post-caper legend's ambient
     *  trace, when the town kept its poltergeist. */
    fun lookText(state: GameState, locId: String): String? {
        val inc = incidentAt(state, locId)
        if (inc != null) {
            state.life["heroInc:$locId"] = state.absDay
            state.life["heroIncidents"] =
                (state.life["heroIncidents"] ?: 0) + 1
            state.life["heroAssists"] =
                (state.life["heroAssists"] ?: 0) + 1
            state.life["heroOutDay"] = state.absDay
            return "🦸 HERO WORK — ${inc.scene}\n${inc.settled}"
        }
        if (caperDone(state) &&
            state.lifeText["caperStance"] == "silence" &&
            locId in PATROL_SPOTS && inWindow(state.timeMin)) {
            val rnd = Random(state.absDay * 17L + locId.hashCode())
            if (rnd.nextFloat() < 0.2f)
                return "🦸 Something here is still, quietly, " +
                    "six inches left of where it was. The town " +
                    "keeps its legend."
        }
        return null
    }

    // ---------------- beats: the escort ----------------

    /** The escort offer on the person menu: on a patrol
     *  night, anyone out after dark who isn't in costume
     *  can be walked home — a hero's bread and butter. */
    fun escortOption(state: GameState, name: String): String? {
        if (!heroCreated(state)) return null
        if (!isPatrolNight(state.absDay)) return null
        if (!inWindow(state.timeMin)) return null
        if (inCostume(state, name)) return null
        if (state.life["heroEscort:$name"] == state.absDay)
            return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        if (me.location !in PATROL_SPOTS) return null
        val a = state.agents[name] ?: return null
        if (a.location != me.location || a.asleep) return null
        if (escortHome(state, name) == null) return null
        return "🦸 Escort $name home (hero work)"
    }

    /** Where home is for an escort: the household map for
     *  the cast; your own house is not an escort. */
    fun escortHome(state: GameState, name: String): String? =
        KID_HOUSE[name]?.takeIf { it != "YourHouse" }

    /** The honest price of the walk, in minutes: the Walk
     *  graph's own route, rounded up to a tick, floored at
     *  one tick — heroes don't teleport either. */
    fun escortMinutes(state: GameState, name: String): Int {
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return 15
        val home = escortHome(state, name) ?: return 15
        val raw = Walk.routeMinutes(state, me.location, home)
        return maxOf(15, ((raw + 14) / 15) * 15)
    }

    /** Settle an escort (the minutes are spent by the
     *  caller on the real clock): the tally moves, the
     *  night is stamped, and the thanks are specific. */
    fun escort(state: GameState, name: String): String? {
        if (escortOption(state, name) == null) return null
        val home = escortHome(state, name) ?: return null
        state.life["heroEscort:$name"] = state.absDay
        state.life["heroEscorts"] =
            (state.life["heroEscorts"] ?: 0) + 1
        state.life["heroAssists"] =
            (state.life["heroAssists"] ?: 0) + 1
        state.life["heroOutDay"] = state.absDay
        val place = LOCATIONS[home]?.name ?: "home"
        return "🦸 You walk $name home to $place, cape and " +
            "all. Their verdict, at the door: \"Thanks. The " +
            "dark's less dark with a hero in it. Don't " +
            "quote me.\""
    }

    // ---------------- talk: the hero register -------------

    /** In-costume talk (TalkGate.owedLines' hook): a roster
     *  kid in costume speaks hero first, once a night each —
     *  in character, in their own voice, with a crew note
     *  when they're YOUR crew, and a caper callback once the
     *  caper is done. */
    fun costumeLine(state: GameState, npcName: String): Line? {
        val p = persona(npcName) ?: return null
        if (!inCostume(state, npcName)) return null
        if (state.life["heroTalk:$npcName"] == state.absDay)
            return null
        state.life["heroTalk:$npcName"] = state.absDay
        state.life["heroOutDay"] = state.absDay
        if (caperDone(state)) {
            val rnd = Random(state.absDay * 31L +
                npcName.hashCode())
            if (rnd.nextInt(3) == 0) {
                return Line(npcName, caperCallback(state, npcName))
            }
        }
        val base = heroLineFor(npcName)
        val text = if (Crew.isMember(state, npcName))
            base + " (Same crew, in AND out of costume. " +
                "That's the whole deal.)"
        else base
        return Line(npcName, text)
    }

    fun heroLineFor(name: String): String = when (name) {
        "Stan" -> "The Standard does NOT take nights off. " +
            "The whistle is decorative until it isn't."
        "Kyle" -> "I have a rebuttal prepared for the dark. " +
            "It's forty minutes long. The dark has been warned."
        "Cartman" -> "The Heavyweight accepts your gratitude " +
            "in advance. Snacks are also accepted. Mostly snacks."
        "Kenny" -> "Mmph mmph! (Second Verse fears NOTHING. " +
            "Second Verse has a very good track record.)"
        "Butters" -> "Oh, hamburgers — I mean — CITIZEN. The " +
            "Good Sport is on it. Please and thank you for " +
            "your cooperation."
        "Craig" -> "Flatline. Whatever's out here, it can " +
            "wait. I'll be over here. Being flat."
        "Tweek" -> "TOO MUCH COFFEE IS MY ORIGIN STORY. " +
            "The Jitterbug sees EVERYTHING. Twice. At once."
        "Clyde" -> "Floodgate is FINE. These are patrol " +
            "tears. Tactical. The feelings are PART of it."
        "Tolkien" -> "The Upgrade's visor has a setting for " +
            "'suspicious.' It's the default setting, actually."
        "Jimmy" -> "The P-Punchline is — wait. At hero speed " +
            "it's just: The Punchline is here. You're welcome."
        "Timmy" -> "TIMMY! (Translation: The Comet has " +
            "arrived, and justice has wheels.)"
        "Wendy" -> "The Brief has already documented this " +
            "entire street. Cross-referenced. With an index."
        "Bebe" -> "The Look is watching this block. The block " +
            "knows. The block is BEHAVING."
        "Heidi" -> "The Signal heard three group chats " +
            "mention a noise by the pond. Stay sharp."
        "Red" -> "The Spark arrives exactly on time. It's " +
            "kind of the whole thing."
        else -> "The Meter's levels are steady, the cape " +
            "is on, and this street is covered."
    }

    fun caperCallback(state: GameState, name: String): String =
        when (state.lifeText["caperStance"]) {
            "name" -> "$name, in a whisper: \"Our licensed " +
                "menace re-shelved the library cart wrong " +
                "again. Six inches. I measured. I'm SO proud.\""
            "join" -> "$name, in a whisper: \"Co-conspirator. " +
                "The park STILL looks perfect. Nobody suspects " +
                "a thing.\""
            else -> "$name, in a whisper: \"Something moved " +
                "the bench again last night. Six inches. The " +
                "legend provides.\""
        }

    /** The parents' in-person opinion, the day after a
     *  patrol night you were out on — once per parent per
     *  day, warm under the exasperation. */
    fun parentRemark(state: GameState, parent: String): Line? {
        if (!Family.arrived(state)) return null
        if ((state.life["heroLastPatrol"] ?: -1) !=
            state.absDay - 1) return null
        if (state.life["heroRemark:$parent"] == state.absDay)
            return null
        state.life["heroRemark:$parent"] = state.absDay
        return Line(parent, when (parent) {
            Family.MOM -> "You came in at ten with grass on " +
                "your cape. I'm not even mad, honey. I'm " +
                "CURIOUS. There's soup if the hero is hungry."
            else -> "Saw a cape in the laundry pile. We now " +
                "have a cape protocol in this house: it goes " +
                "on the hook, hero. The hook by the door."
        })
    }

    // ---------------- the caper ----------------

    data class CaperChapter(
        val title: String,
        val locId: String,
        val lines: List<Line>,
        val helpLabel: String,
        val watchLabel: String,
        val helpedText: String,
        val watchedText: String
    )

    /** The five movements. The first four play at their
     *  spots; the fifth (the finale) is built by
     *  [finaleLines] at KidsPark. Titles and scenes are the
     *  town's public story — what the movements MEAN is the
     *  finale's business, and it stays there. */
    val CHAPTERS: List<CaperChapter> = listOf(
        CaperChapter(
            "Six Inches Left", "KidsPark",
            listOf(
                Line("📓 Hero Log",
                    "PATROL FINDING: the bench, the birdhouses " +
                        "and the trash cans of Kids Park have " +
                        "all been moved. Exactly six inches. " +
                        "To the left."),
                Line("The Standard",
                    "(group chat) I measured twice. SIX. " +
                        "INCHES. Who measures a crime??"),
                Line("The Brief",
                    "(group chat) Whoever it is, they brought " +
                        "a ruler, gloves, and OPINIONS. Log " +
                        "everything.")),
            "🦸 Help measure the whole park",
            "📓 Just log it and patrol on",
            "You measure the whole park with the patrol. " +
                "Every moved thing: six inches, left, not a " +
                "scuff anywhere. It's not vandalism. It's " +
                "EDITING.",
            "You log it in the hero log and patrol on. Six " +
                "inches. Left. The night keeps its secret " +
                "another week."),
        CaperChapter(
            "The Wrong-Way Arrows", "Elementary",
            listOf(
                Line("📓 Hero Log",
                    "PATROL FINDING: every chalk arrow on the " +
                        "school sidewalk has been redrawn — " +
                        "perfect copies, all pointing the wrong " +
                        "way. Toward the mud."),
                Line("Flatline",
                    "(group chat) The mud is fine. Whatever. " +
                        "But the arrows are PERFECT. Down to " +
                        "the little star I — someone — drew."),
                Line("The Signal",
                    "(group chat) Three chats are blaming " +
                        "three different kids. Nobody's blaming " +
                        "the right one. I can FEEL it.")),
            "🦸 Help chalk them back, bigger",
            "📓 Just log it and patrol on",
            "You and the patrol chalk the arrows back, twice " +
                "as big, with stars. By morning the school's " +
                "sidewalk is a masterpiece of correct " +
                "directions. Somewhere, an editor is furious.",
            "You log the arrows and patrol on. The mud " +
                "claims one kindergartner's shoe by Friday. " +
                "The log notes it without comment."),
        CaperChapter(
            "The Lamp That Wouldn't Stay Lit", "StarksPond",
            listOf(
                Line("📓 Hero Log",
                    "PATROL FINDING: the pond lantern is dark " +
                        "again. The bulb is unscrewed and laid " +
                        "on a leaf beside the post, carefully, " +
                        "like a place setting."),
                Line("The Good Sport",
                    "(group chat) Gosh. It's not even broken. " +
                        "It's just... put away. Who puts a " +
                        "lightbulb AWAY?"),
                Line("The Verdict",
                    "(group chat) Somebody wants the pond " +
                        "DARK but undamaged. That's not a crime " +
                        "spree. That's a preference with a " +
                        "ladder.")),
            "🦸 Help re-light it and stake out the post",
            "📓 Just log it and patrol on",
            "You re-light the lantern and stake out the " +
                "post with the patrol until the sleep hour. " +
                "Nothing comes. But the leaf is still there, " +
                "folded. Deliberately folded.",
            "You log the lantern and patrol on. The frog " +
                "gets its dark pond back. The log underlines " +
                "the word 'carefully' three times."),
        CaperChapter(
            "The Mannequin Meeting", "Mall",
            listOf(
                Line("📓 Hero Log",
                    "PATROL FINDING: the mall's window " +
                        "mannequins have all been turned — every " +
                        "one of them facing the doors, posed mid-" +
                        "conversation, like a meeting was called."),
                Line("The Look",
                    "(group chat) The posing is GOOD. Like, " +
                        "compositionally. I hate that I respect " +
                        "it."),
                Line("The Comet",
                    "(group chat) TIMMY. (Translation: " +
                        "whoever did this had to move SIX of " +
                        "them. Alone. At night. For art.)")),
            "🦸 Help pose them back (matching energy)",
            "📓 Just log it and patrol on",
            "You pose them back — but the patrol matches " +
                "the energy, and by the sleep hour the window " +
                "is a better scene than the store ever set. " +
                "The editor has competition now.",
            "You log the meeting and patrol on. Shoppers " +
                "the next day keep stopping at the window, " +
                "thoughtful, unsure why."))


    /** 0 = unbegun; 1..4 = that chapter is open; 5 = the
     *  finale is due; 6 = done. */
    fun caperStage(state: GameState): Int =
        state.life["caperStage"] ?: 0

    fun caperDone(state: GameState): Boolean = caperStage(state) >= 6

    fun caperHelped(state: GameState): Int =
        state.life["caperHelped"] ?: 0

    /** The caper's ending in the town's PUBLIC words — the
     *  one phrase the Hero Log itself prints once the caper
     *  is closed, and the only shape of the ending any
     *  other record may repeat (v0.12.0a: the town paper
     *  quotes it whole and adds nothing). It names no one
     *  and tells nothing: who the Curator was and what the
     *  movements were stay inside the finale's own scene,
     *  where they have always lived. Null until it closes. */
    fun caperPublicEnding(state: GameState): String? {
        if (!caperDone(state)) return null
        return when (state.lifeText["caperStance"]) {
            "name" -> if (caperHelped(state) >= 2)
                "the Curator, named and licensed"
            else "the Curator, named and gone"
            "join" -> "one perfect park, no witnesses"
            else -> "a legend the town gets to keep"
        }
    }

    /** A chapter beat is due: the caper is open, the hero
     *  exists, it's a patrol night, you're standing at the
     *  open chapter's spot, and no caper beat has played
     *  tonight (one movement a night — anticipation is a
     *  feature). */
    fun caperBeatDue(state: GameState): CaperChapter? {
        if (!heroCreated(state)) return null
        val stage = caperStage(state)
        if (stage !in 1..4) return null
        if (!isPatrolNight(state.absDay)) return null
        if (!inWindow(state.timeMin)) return null
        if (state.life["caperBeatDay"] == state.absDay)
            return null
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return null
        val ch = CHAPTERS[stage - 1]
        return if (me.location == ch.locId && !state.playerInside)
            ch else null
    }

    /** The chapter turns: helped or witnessed, it advances —
     *  the StoryArcs shape (a stage you touched turns over;
     *  the helped count writes the ending's weather). */
    fun playCaperBeat(state: GameState, helped: Boolean): String? {
        val ch = caperBeatDue(state) ?: return null
        state.life["caperBeatDay"] = state.absDay
        state.life["heroOutDay"] = state.absDay
        if (helped) state.life["caperHelped"] =
            caperHelped(state) + 1
        state.life["caperStage"] = caperStage(state) + 1
        return if (helped) ch.helpedText else ch.watchedText
    }

    /** The finale is due at the park on a patrol night,
     *  once every chapter has turned. */
    fun finaleDue(state: GameState): Boolean {
        if (!heroCreated(state)) return false
        if (caperStage(state) != 5) return false
        if (!isPatrolNight(state.absDay)) return false
        if (!inWindow(state.timeMin)) return false
        if (state.life["caperBeatDay"] == state.absDay)
            return false
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return false
        return me.location == "KidsPark" && !state.playerInside
    }

    /** The finale's scene — played in the safe-moment
     *  dialog. This is where the caper's story lands; the
     *  scene itself is the only place it is told. */
    fun finaleLines(state: GameState): List<Line> = listOf(
        Line("📓 Hero Log",
            "PATROL FINDING: the whole park. Every bench, " +
                "every bin, every birdhouse — six inches left, " +
                "all of it, in one night. And by the mitten " +
                "tree, mid-shift, holding a tape measure: the " +
                "one kid nobody ever sees."),
        Line("DogPoo",
            "...Oh. Hi. You found me. Nobody FINDS me. " +
                "That's — huh. That's actually kind of the " +
                "whole point."),
        Line("DogPoo",
            "You all walk past me every day. Every day! So I " +
                "moved your town six inches, and you STILL " +
                "almost didn't look. I just wanted somebody to " +
                "look at ONE thing carefully. One!"),
        Line("The Standard",
            "(group chat) Heroes. We have eyes on the " +
                "Curator. Awaiting the call."))

    val FINALE_STANCES: List<Pair<String, String>> = listOf(
        "name" to "📢 Name them — call it out, in front of " +
            "the whole patrol",
        "silence" to "🤫 Say nothing — let the legend keep " +
            "its secret",
        "join" to "🤝 Lend a hand — help finish the park, " +
            "perfectly")

    /** The stance lands: marks, callbacks and the town's
     *  memory all read what was chosen here. */
    fun playFinale(state: GameState, stance: String): String? {
        if (!finaleDue(state)) return null
        if (stance !in FINALE_STANCES.map { it.first }) return null
        state.life["caperStage"] = 6
        state.life["caperDoneDay"] = state.absDay
        state.life["caperBeatDay"] = state.absDay
        state.lifeText["caperStance"] = stance
        state.life["heroOutDay"] = state.absDay
        state.lifeText["mark:hero_caper"] = stance
        return when (stance) {
            "name" -> if (caperHelped(state) >= 2)
                "📢 You name the Curator to the whole patrol — " +
                    "and the patrol, having read the log, votes " +
                    "on the spot: DogPoo is hereby the crew's " +
                    "OFFICIAL LICENSED MENACE (probationary). " +
                    "He cries a little. Then he measures the " +
                    "bench. Some things don't change; some " +
                    "things get a clipboard."
            else
                "📢 You call it out — and in the shuffle of " +
                    "capes and flashlights, the Curator is " +
                    "simply not there anymore. \"You didn't do " +
                    "the reading,\" says the empty air where he " +
                    "stood. The shifting stops. Being seen was " +
                    "the whole crime; being named ends it."
            "join" -> "🤝 You pick up the other end of the " +
                "bench. It takes the whole patrol and most of " +
                "the night, and by the sleep hour Kids Park is " +
                "the most perfectly arranged place in " +
                "Colorado. Nobody will ever prove who did it. " +
                "The Curator bumps your fist on his way into " +
                "the dark. Six inches, no more, no less."
            else -> "🤫 You close the hero log and say nothing " +
                "at all. The Curator looks at you for a long " +
                "moment — the first time anyone has ever looked " +
                "BACK — and nods, once, like a colleague. The " +
                "town keeps its legend. So does he."
        }
    }

    // ---------------- the person menu ----------------

    /** The hero option on the person menu is the escort —
     *  the one piece of hero work you offer a specific
     *  person to their face. */
    fun menuOption(state: GameState, name: String): String? =
        escortOption(state, name)

    // ---------------- the journal ----------------

    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        if (!heroCreated(state)) {
            out += "No cape yet."
            out += "Word is the kids patrol on Wednesday and " +
                "Saturday nights. Somebody should found a " +
                "hero identity. Somebody with a mirror and " +
                "ten free minutes."
            return out
        }
        out += "🦸 ${heroName(state)} — ${powerLabel(state)}"
        out += "Patrols stood: ${state.life["heroPatrolNights"] ?: 0}" +
            " · Assists: ${state.life["heroAssists"] ?: 0}" +
            " (incidents ${state.life["heroIncidents"] ?: 0}," +
            " escorts ${state.life["heroEscorts"] ?: 0})"
        val shots = state.photos.count { rec ->
            rec.figures.any { it.hero.isNotEmpty() } }
        if (shots > 0) out += "Costumed photos in the album: $shots"
        val stage = caperStage(state)
        when {
            stage == 0 -> out += "The caper: the town is quiet. " +
                "Too quiet, some nights."
            stage in 1..4 -> {
                val ch = CHAPTERS[stage - 1]
                out += "The caper, chapter $stage: " +
                    "\"${ch.title}\" — something is afoot at " +
                    "${LOCATIONS[ch.locId]?.name ?: ch.locId}. " +
                    "Patrol nights will tell."
            }
            stage == 5 -> out += "The caper: every movement " +
                "logged. Whatever it is, it ends at Kids Park " +
                "on a patrol night."
            else -> {
                out += "The caper: CLOSED — " +
                    "${caperPublicEnding(state)}. " +
                    "(Helped on ${caperHelped(state)} of 4 " +
                    "movements.)"
            }
        }
        out += "The patrol: " + ROSTER.joinToString(" · ") {
            "${it.heroName} (${it.kid})" }
        return out
    }

    /** The 🪪 YOU card line, once there's a hero to name. */
    fun youCardLine(state: GameState): String? {
        if (!heroCreated(state)) return null
        return "🦸 Hero: ${heroName(state)} — " +
            powerLabel(state).substringBefore(" — ")
    }

    // ---------------- the rollover ----------------

    /** Midnight bookkeeping, in Engine.onNewDay's order
     *  beside the other systems': a patrol night you were
     *  out on counts exactly once, and yesterday's patrol
     *  becomes the morning-after fact the phone reads. The
     *  caper's first chapter opens on the first midnight
     *  after the hero exists. */
    fun dailyRollover(state: GameState) {
        val yesterday = state.absDay - 1
        if (yesterday >= 0 &&
            state.life["heroOutDay"] == yesterday &&
            state.life["heroPatrolDay"] != yesterday) {
            state.life["heroPatrolDay"] = yesterday
            state.life["heroPatrolNights"] =
                (state.life["heroPatrolNights"] ?: 0) + 1
            state.life["heroLastPatrol"] = yesterday
        }
        if (heroCreated(state) && caperStage(state) == 0)
            state.life["caperStage"] = 1
    }
}

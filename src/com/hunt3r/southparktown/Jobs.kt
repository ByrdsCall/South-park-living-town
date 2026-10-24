package com.hunt3r.southparktown

// ============================================================
// Jobs — v0.6.0e (Queue #6 item 5): town jobs.
//
// Errands are favors — one-off, whoever asks. A JOB is a
// standing commitment: a boss who hired you, a schedule
// that comes around every week whether you're ready or
// not, shifts you clock into and out of, and a reputation
// that accrues to steady hands. Three jobs, deep over
// broad:
//
//   PAPER ROUTE  mornings at City Hall for Mayor
//                McDaniels — pick up the bundle, walk the
//                route (four houses a day, off the Walk's
//                own streets), bring the empties back.
//   FARMHAND     mornings at Tegridy for Randy — tend
//                the rows, mend the fence, survive Randy's
//                inspection. Sharon counts the wages and
//                keeps her own opinion of your work, which
//                lives in her talks like everything else
//                true in this town.
//   SHOP HELP    afternoons on the shops' shared rota —
//                Tweek Bros, Jimbo's Guns and Freeman's
//                Tacos share one help pool (small town,
//                one economy); the week's shop is a pure
//                function of the week, and its keeper is
//                your boss that week.
//
// A shift is played live: clock in with the boss (Talk,
// on the gate's WORK rail), land the shift's tasks —
// judged by the verbs the game already has: Talk to the
// regular, Look at the rows, the shelves, the mailbox —
// and clock out with the boss. Pay accrues through the
// week and lands on payday (the first morning of a new
// game week) in the morning report. Reliability is a
// streak; a streak is a pay tier; missing shifts walks a
// kind ladder — reminder, warning, let go at three in a
// row — and a shift missed because the town was having
// one of its days (a case climax due, a finale week, a
// family table you actually sat at) is excused before
// it's counted. The set-pieces outrank work; every boss
// in town knows it.
//
// State rides the existing `life` / `lifeText` maps (the
// employment record, the day's shift record, the week's
// accrued pay), every read clamped against the job
// registry — no new save keys. Pure Kotlin, like its
// siblings.
// ============================================================

/** One of the town's three jobs. [boss]/[workplace] are
 *  the defaults; the shop job's boss and counter rotate
 *  weekly — Jobs.bossFor / workplaceFor / windowFor read
 *  the SHOPS rota for it. [days] are weekdays, 0 = Sunday
 *  through 6 = Saturday, on the game's real calendar. */
data class JobDef(
    val id: String,
    val title: String,
    val emoji: String,
    val boss: String,
    val workplace: String,       // location id
    val factionId: String,       // the boss's circle (application gate)
    val days: Set<Int>,
    val daysLabel: String,
    val inFrom: Int,             // clock-in window opens (minutes)
    val inTo: Int,               // clock-in window closes
    val shiftEnd: Int,           // the shift's day-part ends
    val basePay: Int,            // capsule coins, a completed shift
    val perTask: Int,            // capsule coins per task landed
    val blurb: String,           // the Journal's one-liner
    val applyText: String,       // the hiring, in the boss's voice
    val refuseText: String,      // the door, when the circle is hostile
    val clockInText: String,     // clocking in, in the boss's voice
    val clockOutText: String,    // clocking out, in the boss's voice
    val quitText: String         // the notice, received
)

/** One shop on the help rota: its keeper, its counter,
 *  its own hours (each shop keeps its keeper's hours). */
data class ShopDef(
    val locId: String,
    val keeper: String,
    val inFrom: Int,
    val inTo: Int,
    val shiftEnd: Int,
    val stockText: String,       // LOOK task: stock the shelves
    val tillText: String         // LOOK task: count the till
)

/** One judged task inside a shift. [kind] is TALK (a
 *  person) or LOOK (a place); [target] is the NPC's name
 *  or the location id. Tasks land in any order — a LOOK
 *  fires the first unlanded LOOK task at its place. */
data class JobTask(
    val kind: String,            // "TALK" | "LOOK"
    val target: String,
    val title: String,
    val text: String
)

/** A step of the job a Talk would take — what the gate
 *  hands the activity. [kind]: APPLY | CLOCK_IN | TASK |
 *  CLOCK_OUT | QUIT. [taskIndex] is set on TASK hits. */
data class JobHit(
    val def: JobDef,
    val kind: String,
    val taskIndex: Int = -1,
    val refused: Boolean = false,
    val text: String = ""
)

object Jobs {

    // ---------------- the dials (ledger-named) ----------------

    /** The application gate: the boss's circle must not
     *  count you Suspect-or-worse (Factions.standingLabel's
     *  line is -10) — standing strictly above it hires. */
    const val HIRE_STANDING_FLOOR = -10

    /** Consecutive unexcused misses that end a job. The
     *  ladder is kind on purpose: 1 = a reminder, 2 = a
     *  warning, 3 = let go. Any attended shift (complete
     *  or partial) resets the count to zero. */
    const val MISS_LIMIT = 3

    /** Streak tiers (ledger-named): at STEADY_STREAK
     *  consecutive completed shifts the town calls you a
     *  steady hand (+STEADY_BONUS a shift); at
     *  TRUSTED_STREAK, a trusted hand (+TRUSTED_BONUS). */
    const val STEADY_STREAK = 5
    const val TRUSTED_STREAK = 12
    const val STEADY_BONUS = 1
    const val TRUSTED_BONUS = 2

    /** Papers delivered per route morning. */
    const val ROUTE_SIZE = 4

    /** The topic a worked shift plants in the boss's (and
     *  a coworker's) memory — Threads' bank follows up on
     *  it like any other thread in the town's talks. */
    const val TOPIC = "work"

    // ---------------- the registry ----------------

    val SHOPS: List<ShopDef> = listOf(
        ShopDef("TweekBros", "Tweek", 15 * 60, 16 * 60, 18 * 60,
            "You haul the bean sacks up from the back and line the " +
                "shelves the way the sign says: alphabetically, then " +
                "by fear level. Tweek nods at the fear-level section. " +
                "It is the biggest section.",
            "You count the till twice, the way you were shown: once " +
                "for the money, once for the gnomes. The counts agree. " +
                "Nobody is more surprised than the till."),
        ShopDef("JimbosGuns", "Jimbo", 16 * 60 + 30, 17 * 60 + 30, 19 * 60 + 30,
            "You stock the shelves: boxes squared, labels out, the " +
                "good camo where the light hits it. Jimbo inspects " +
                "with one eye closed, which is either marksmanship " +
                "or approval. Possibly both.",
            "You count the till under the counter's glass eye. Every " +
                "coin accounted for, every receipt spiked in order. " +
                "Jimbo calls that 'a clean sweep' and means it as " +
                "praise and as inventory."),
        ShopDef("FreemansTacos", "Chef", 15 * 60, 16 * 60, 18 * 60,
            "You stack the shells in their towers and line the " +
                "filling trays the way Chef showed you: with respect, " +
                "with rhythm, with a little song you are not allowed " +
                "to repeat outside this kitchen.",
            "You count the till while the last tacos cool. Chef " +
                "checks your math by smell alone and pronounces it " +
                "correct, which the health inspector has never once " +
                "managed."))

    val JOBS: List<JobDef> = listOf(
        JobDef(
            id = "paper", title = "Paper Route", emoji = "🗞",
            boss = "McDaniels", workplace = "CityHall",
            factionId = "parents",
            days = setOf(1, 2, 3, 4, 5), daysLabel = "Mon–Fri",
            inFrom = 8 * 60, inTo = 10 * 60, shiftEnd = 12 * 60,
            basePay = 2, perTask = 1,
            blurb = "The South Park Gazette, delivered by hand, " +
                "four houses a morning, before the town's coffee cools.",
            applyText = "McDaniels looks you over the way she looks " +
                "over parade routes: for liabilities. “The Gazette " +
                "needs a route walker. Four houses a morning, " +
                "Monday to Friday, my office at eight. The town " +
                "reads, new kid. See that it reads on time.” She " +
                "hands you a route card and, after a moment, a " +
                "second one. “For when you lose the first. Everyone " +
                "loses the first.”",
            refuseText = "McDaniels folds her hands. “City Hall " +
                "positions require... standing, with the town's " +
                "families. Yours is not, at present, what the " +
                "brochure would call strong. Mend fences first — " +
                "the route will keep.”",
            clockInText = "McDaniels slides the bundle across the " +
                "counter without looking up from the headlines. " +
                "“Four houses. Before noon. The Marsh place takes " +
                "theirs in the box, the Broflovski box sticks — " +
                "everyone's box sticks. Walk it like you mean it.”",
            clockOutText = "McDaniels takes the empty strap and " +
                "checks the route card off with a pencil she keeps " +
                "for exactly this. “Every house. On time. The " +
                "Gazette thanks you; the town thanks you; I will " +
                "deny thanking you in public.”",
            quitText = "McDaniels takes the route card back and " +
                "files it under R, for Route, Retired, Reasonable. " +
                "“No hard feelings, new kid. The papers will find " +
                "another pair of legs. They always do. It's the " +
                "headlines I'm worried about.”"),
        JobDef(
            id = "farm", title = "Farmhand", emoji = "🌾",
            boss = "Randy", workplace = "TegridyFarms",
            factionId = "tegridy_crew",
            days = setOf(2, 4, 6), daysLabel = "Tue · Thu · Sat",
            inFrom = 8 * 60, inTo = 9 * 60 + 30, shiftEnd = 12 * 60,
            basePay = 3, perTask = 2,
            blurb = "Tegridy's morning crew: the rows, the fence, " +
                "and Randy's inspection. Sharon counts the wages.",
            applyText = "Randy sizes you up like a fence post. " +
                "“Farm needs hands. Tuesday, Thursday, Saturday, " +
                "eight o'clock, rows don't tend themselves. Sharon " +
                "does the wages — Sharon does everything, I do the " +
                "vision.” He spits, misses the bucket, and hires " +
                "you anyway.",
            refuseText = "Randy shakes his head slowly. “Nothing " +
                "personal, kid. The crew's got a... standing " +
                "situation with you right now, and a farm runs on " +
                "trust and nitrogen. Mostly trust. Come back when " +
                "it's mended.”",
            clockInText = "Randy hands you gloves that have " +
                "clearly raised other, lesser employees. “Rows, " +
                "fence, then my inspection. In that order or any " +
                "order, I'm not a tyrant. Sharon's counting this " +
                "one, so make it count.”",
            clockOutText = "Randy walks the rows with his hands " +
                "behind his back like a general of agriculture. " +
                "“Good work. Honest work. Tegridy work.” He nods " +
                "at the house. “Sharon'll see your wages right. " +
                "She keeps the book. The book is never wrong.”",
            quitText = "Randy takes the gloves back and holds them " +
                "a moment. “Farm'll miss the hands, kid. Rows " +
                "remember who tended 'em.” He hangs the gloves on " +
                "their nail like a ceremony he's done before and " +
                "hopes is rare."),
        JobDef(
            id = "shop", title = "Shop Help", emoji = "🛎",
            boss = "Tweek", workplace = "TweekBros",
            factionId = "tegridy_crew",
            days = setOf(1, 3, 5), daysLabel = "Mon · Wed · Fri",
            inFrom = 15 * 60, inTo = 16 * 60, shiftEnd = 18 * 60,
            basePay = 3, perTask = 2,
            blurb = "The shops' shared help pool — one counter a " +
                "week on the rota: coffee, guns, tacos.",
            applyText = "“The pool! You're IN the pool!” It's " +
                "explained, at speed: the town's shops share one " +
                "help rota — a week at a counter, then the next " +
                "shop takes you. “Stock, serve, count the till. " +
                "Three afternoons a week. The keepers compare " +
                "notes, so no favorites. GAAAH. I mean — welcome!”",
            refuseText = "“We — we CAN'T. The keepers take a vote " +
                "and the vote was... mentioned. Your standing with " +
                "the business crowd is TOO MUCH PRESSURE right " +
                "now. Fix it and ask again. Please. The shelves " +
                "are SO unstocked.”",
            clockInText = "Your boss-for-the-week checks the rota " +
                "sheet and points you at the counter like a " +
                "conductor with one musician. “Stock, the regular, " +
                "the till. You've got this. Probably. GO.”",
            clockOutText = "The rota sheet gets its checkmark, in " +
                "the good pen. “Counter's cleaner than you found " +
                "it. Till's honest. The regular left HAPPY, which " +
                "the review board will hear about. Same time next " +
                "shift.”",
            quitText = "The rota sheet is amended in pencil, which " +
                "in shop law means it's real but sad. “The pool " +
                "will miss you. The shelves will miss you MORE. " +
                "If the standing ever settles, the apron's still " +
                "here. It is ALWAYS here.”"))

    fun defById(id: String?): JobDef? = JOBS.firstOrNull { it.id == id }

    fun shopByLoc(locId: String?): ShopDef? =
        SHOPS.firstOrNull { it.locId == locId }

    // ---------------- the calendar (pure) ----------------

    /** The game's weekday for [absDay]: 0 = Sunday …
     *  6 = Saturday. Day 0 is Thursday, October 1, 2026 —
     *  the anchor is the real calendar, and the suite pins
     *  it against known dates. */
    fun weekdayOf(absDay: Int): Int =
        (((absDay % 7) + 7) % 7 + 4) % 7

    /** The game's week (payday rhythm): weeks start on the
     *  calendar's first day, Thursday. */
    fun weekIndex(absDay: Int): Int =
        if (absDay >= 0) absDay / 7 else (absDay - 6) / 7

    val WEEKDAY_NAMES = listOf("Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday")

    fun isShiftDay(absDay: Int, def: JobDef): Boolean =
        weekdayOf(absDay) in def.days

    /** This week's shop on the rota — a pure function of
     *  the week, so the boss, the counter and the hours all
     *  turn together at the payday boundary. */
    fun shopFor(absDay: Int): ShopDef =
        SHOPS[((weekIndex(absDay) % SHOPS.size) + SHOPS.size) % SHOPS.size]

    fun bossFor(state: GameState, def: JobDef): String =
        if (def.id == "shop") shopFor(state.absDay).keeper else def.boss

    fun workplaceFor(state: GameState, def: JobDef): String =
        if (def.id == "shop") shopFor(state.absDay).locId else def.workplace

    /** The shift's hours as (clock-in opens, clock-in
     *  closes, shift ends) — the shop job reads the week's
     *  counter's own hours. */
    fun windowFor(state: GameState, def: JobDef): Triple<Int, Int, Int> =
        windowForDay(state.absDay, def)

    fun windowForDay(absDay: Int, def: JobDef): Triple<Int, Int, Int> =
        if (def.id == "shop") shopFor(absDay).let {
            Triple(it.inFrom, it.inTo, it.shiftEnd)
        } else Triple(def.inFrom, def.inTo, def.shiftEnd)

    fun windowLabel(from: Int, to: Int): String =
        "${minutesToTimeStr(from)}–${minutesToTimeStr(to)}"

    // ---------------- the paper route (pure) ----------------

    /** The route's houses, as (location, the house's name,
     *  the kid of the house). Suite-pinned against the
     *  cast data — a paper can only be owed to a real
     *  house with a real family in it. */
    val ROUTE_HOUSES: List<Triple<String, String, String>> = listOf(
        Triple("StanHouse", "the Marsh house", "Stan"),
        Triple("KyleHouse", "the Broflovski house", "Kyle"),
        Triple("CartmanHouse", "the Cartman house", "Cartman"),
        Triple("KennysHouse", "the McCormick house", "Kenny"),
        Triple("ButtersHouse", "the Stotch house", "Butters"),
        Triple("TolkienHouse", "the Black house", "Tolkien"))

    /** Today's route: ROUTE_SIZE houses of the six, a
     *  rotating window over the ring — a pure function of
     *  the day, so the card McDaniels hands you at eight
     *  is the card the street agrees with all morning. */
    fun routeFor(absDay: Int): List<Triple<String, String, String>> {
        val start = ((absDay % ROUTE_HOUSES.size) + ROUTE_HOUSES.size) %
            ROUTE_HOUSES.size
        return (0 until ROUTE_SIZE).map {
            ROUTE_HOUSES[(start + it) % ROUTE_HOUSES.size]
        }
    }

    private val PAPER_TEXTS = listOf(
        "You slide the Gazette into the mailbox at {house}. It lands " +
            "with the soft thunk of a town being informed.",
        "The {house} box sticks, exactly as advertised. You apply " +
            "the technique (jiggle, lift, faith) and the Gazette " +
            "goes home.",
        "Paper delivered at {house}. Somewhere inside, {resident} " +
            "is about to have opinions about the funnies.")

    // ---------------- the shift's tasks (pure per day) ----------------

    /** The regular your shop shift serves — one per shop,
     *  chosen by the day from the counter's own crowd. */
    val CUSTOMERS: Map<String, List<String>> = mapOf(
        "TweekBros" to listOf("Stan", "Kyle", "Craig"),
        "JimbosGuns" to listOf("Ned", "Stuart", "Barbrady"),
        "FreemansTacos" to listOf("Butters", "Kenny", "Cartman"))

    fun customerFor(state: GameState, def: JobDef): String? {
        if (def.id != "shop") return null
        val shop = shopFor(state.absDay)
        val crowd = CUSTOMERS[shop.locId] ?: return null
        return crowd[((state.absDay % crowd.size) + crowd.size) % crowd.size]
    }

    private fun serveText(shop: ShopDef, customer: String): String =
        when (shop.locId) {
            "TweekBros" -> when (customer) {
                "Stan" -> "Stan's regular order arrives at his table " +
                    "before his rant about the school board finishes " +
                    "loading. He tips you a nod, which at this counter " +
                    "is legal tender."
                "Kyle" -> "Kyle orders with footnotes and pays exact " +
                    "change, counted twice, correctly. Serving him " +
                    "feels like passing a very small audit."
                else -> "Craig orders by pointing. You point back, " +
                    "professionally, and bring the coffee. It is the " +
                    "smoothest transaction in the shop's history."
            }
            "JimbosGuns" -> when (customer) {
                "Ned" -> "Ned's voice box chirps his order — two " +
                    "boxes, the usual shelf. You bag them carefully. " +
                    "The box adds, “THANK. YOU.” Jimbo pretends not " +
                    "to be moved."
                "Stuart" -> "Stuart needs the size that fits the thing " +
                    "he won't describe. You find it on the second try. " +
                    "He leaves like a man whose afternoon just got " +
                    "classified."
                else -> "Barbrady is on duty, which means his coffee " +
                    "break is official business. You serve it with the " +
                    "gravity the badge demands. He salutes the counter."
            }
            else -> when (customer) {
                "Butters" -> "Butters orders one taco, politely, then " +
                    "apologizes to it. You serve it on the good tray. " +
                    "He says you're 'real good at this,' and means it " +
                    "with his whole chest."
                "Kenny" -> "Kenny's order comes through the parka, " +
                    "muffled but unmistakable — the usual, extra " +
                    "napkins. You slide it over. A thumbs-up emerges " +
                    "from the orange."
                else -> "Cartman inspects his order like a health " +
                    "inspector with a grudge and finds, to his visible " +
                    "annoyance, nothing wrong. “Acceptable,” he says, " +
                    "which is a five-star review."
            }
        }

    /** The tasks of [def]'s shift on [state]'s day, in
     *  card order. Pure per (job, day) — the card the boss
     *  hands you is the card the suite reads. */
    fun tasksFor(state: GameState, def: JobDef): List<JobTask> =
        when (def.id) {
            "paper" -> routeFor(state.absDay).mapIndexed { i, (loc, house, resident) ->
                JobTask("LOOK", loc, "Deliver to $house",
                    PAPER_TEXTS[(state.absDay + i) % PAPER_TEXTS.size]
                        .replace("{house}", house)
                        .replace("{resident}", resident))
            }
            "farm" -> listOf(
                JobTask("LOOK", "TegridyFarms", "Tend the rows",
                    "You work the rows the way Sharon showed you in " +
                        "one devastating eyebrow raise: water, weed, " +
                        "no shortcuts through the crop. The rows stand " +
                        "a little straighter when you finish, the way " +
                        "good work makes things stand."),
                JobTask("LOOK", "TegridyFarms", "Mend the fence line",
                    "Two posts re-seated, one rail replaced, a " +
                        "hammered thumb kept between you and the " +
                        "fence. The line runs true again — the cows of " +
                        "the mind stay where they're put."),
                JobTask("TALK", "Randy", "Randy's inspection",
                    "Randy walks your work with a clipboard he is " +
                        "not writing on. “Rows: tended. Fence: " +
                        "standing. Employee: ...” He writes something " +
                        "at last. “…keeping.” It's the highest " +
                        "grade the farm gives out."))
            else -> { // shop
                val shop = shopFor(state.absDay)
                val customer = customerFor(state, def) ?: "Stan"
                listOf(
                    JobTask("LOOK", shop.locId, "Stock the shelves",
                        shop.stockText),
                    JobTask("TALK", customer, "Serve the regular",
                        serveText(shop, customer)),
                    JobTask("LOOK", shop.locId, "Count the till",
                        shop.tillText))
            }
        }

    // ---------------- state keys (life / lifeText) ----------------
    // One employment record, one shift record for the day,
    // one pay accumulator for the week. Every read clamps
    // against the registry — a hostile or stale save reads
    // as "unemployed, no shift", never as a phantom job.

    private const val K_ID = "jobId"               // lifeText: current job id ("" = none)
    private const val K_SINCE = "jobSince"          // life: absDay hired
    private const val K_SHIFTS = "jobShifts"        // life: shifts completed at this job
    private const val K_STREAK = "jobStreak"        // life: consecutive completed shifts
    private const val K_MISS = "jobMissStreak"      // life: consecutive unexcused misses
    private const val K_WEEKPAY = "jobWeekPay"      // life: coins accrued, awaiting payday
    private const val K_EARNED = "jobEarned"        // life: lifetime wages received
    private const val K_PAYLINE = "jobPaydayLine"   // lifeText: the last payday, as the report told it
    private const val K_SHIFT_DAY = "jobShiftDay"   // life: absDay the shift record belongs to (-1 = none)
    private const val K_CLOCKED = "jobClocked"      // life: 1 while a shift is live
    private const val K_DONE = "jobShiftDone"       // life: 1 once today's shift clocked out
    private const val K_MASK = "jobMask"            // life: today's task bits
    private const val K_SETTLED = "jobSettledDay"   // life: last absDay the rollover settled
    private const val K_QUIT_DAY = "jobQuitDay"     // life: absDay you last gave notice

    private fun kTotal(id: String) = "jobTotal:$id"
    private fun kFired(id: String) = "jobFired:$id"
    private fun kRefused(id: String) = "jobRefused:$id"

    // ---------------- records (all reads clamped) ----------------

    fun currentJob(state: GameState): JobDef? =
        defById(state.lifeText[K_ID])

    fun sinceDay(state: GameState): Int = state.life[K_SINCE] ?: -1

    fun shiftsAtJob(state: GameState): Int =
        (state.life[K_SHIFTS] ?: 0).coerceIn(0, 99999)

    fun streak(state: GameState): Int =
        (state.life[K_STREAK] ?: 0).coerceIn(0, 99999)

    fun missStreak(state: GameState): Int =
        (state.life[K_MISS] ?: 0).coerceIn(0, MISS_LIMIT)

    fun weekPay(state: GameState): Int =
        (state.life[K_WEEKPAY] ?: 0).coerceIn(0, 999999)

    fun lifetimeEarned(state: GameState): Int =
        (state.life[K_EARNED] ?: 0).coerceIn(0, 9999999)

    fun totalShifts(state: GameState, def: JobDef): Int =
        (state.life[kTotal(def.id)] ?: 0).coerceIn(0, 99999)

    fun firedCount(state: GameState, def: JobDef): Int =
        (state.life[kFired(def.id)] ?: 0).coerceIn(0, 999)

    fun paydayLine(state: GameState): String? =
        state.lifeText[K_PAYLINE]?.takeIf { it.isNotEmpty() }

    /** The reputation the town has filed you under, by
     *  streak — the tiers the pay bonus rides on. */
    fun tierName(streak: Int): String = when {
        streak >= TRUSTED_STREAK -> "Trusted hand"
        streak >= STEADY_STREAK -> "Steady hand"
        else -> "New hand"
    }

    fun tierBonus(streak: Int): Int = when {
        streak >= TRUSTED_STREAK -> TRUSTED_BONUS
        streak >= STEADY_STREAK -> STEADY_BONUS
        else -> 0
    }

    /** The hiring gate: the boss's circle must not count
     *  you Suspect-or-worse (Factions' own line). */
    fun standingOk(state: GameState, def: JobDef): Boolean =
        (state.factionStates[def.factionId]?.playerStanding ?: 0) >
            HIRE_STANDING_FLOOR

    // ---------------- the day's shift record ----------------

    fun shiftDay(state: GameState): Int = state.life[K_SHIFT_DAY] ?: -1

    fun clockedIn(state: GameState): Boolean =
        shiftDay(state) == state.absDay && (state.life[K_CLOCKED] ?: 0) == 1

    fun shiftDoneToday(state: GameState): Boolean =
        shiftDay(state) == state.absDay && (state.life[K_DONE] ?: 0) == 1

    fun taskMask(state: GameState, def: JobDef): Int {
        if (shiftDay(state) != state.absDay) return 0
        val n = tasksFor(state, def).size
        return (state.life[K_MASK] ?: 0) and ((1 shl n) - 1)
    }

    fun tasksDone(state: GameState, def: JobDef): Int =
        Integer.bitCount(taskMask(state, def))

    fun allTasksDone(state: GameState, def: JobDef): Boolean =
        tasksDone(state, def) >= tasksFor(state, def).size

    // ---------------- the gate (pure queries) ----------------

    /** The job step a Talk with [npc] would take right
     *  now, or null. Pure query (the gate calls this);
     *  judging is judgeTalk's. Employment first, then the
     *  applications — you can only be hired into one job,
     *  and only at the boss's counter, in the boss's
     *  hours. */
    fun talkStepFor(state: GameState, npc: String,
                    playerLocId: String): JobHit? {
        val job = currentJob(state)
        if (job != null) {
            val boss = bossFor(state, job)
            val place = workplaceFor(state, job)
            if (clockedIn(state)) {
                // the regular's serve beat, mid-shift
                val tasks = tasksFor(state, job)
                val mask = taskMask(state, job)
                val talkIdx = tasks.indices.firstOrNull { i ->
                    tasks[i].kind == "TALK" && tasks[i].target == npc &&
                        mask and (1 shl i) == 0
                } ?: -1
                if (talkIdx >= 0 && npc != boss)
                    return JobHit(job, "TASK", talkIdx)
                if (npc == boss && playerLocId == place) {
                    // Randy's inspection is a TASK beat before
                    // it's a clock-out (he's both boss and task)
                    if (talkIdx >= 0) return JobHit(job, "TASK", talkIdx)
                    if (allTasksDone(state, job))
                        return JobHit(job, "CLOCK_OUT")
                }
                return null
            }
            if (shiftDoneToday(state)) return null
            // clock-in: a scheduled day, the window open, the
            // boss at their counter with you
            val (inFrom, inTo, end) = windowFor(state, job)
            if (npc == boss && playerLocId == place &&
                isShiftDay(state.absDay, job) &&
                state.timeMin in inFrom until inTo &&
                shiftDay(state) != state.absDay)
                return JobHit(job, "CLOCK_IN")
            // the notice: at the workplace, in work hours —
            // outside them, a boss is just a neighbor again
            if (npc == boss && playerLocId == place &&
                state.timeMin in inFrom until end)
                return JobHit(job, "QUIT")
            return null
        }
        // applications: unemployed, at a boss's counter,
        // in that job's hours
        for (def in JOBS) {
            val (boss, place, inFrom, inTo, end) =
                applicationFrame(state, def, npc, playerLocId) ?: continue
            if (npc != boss || playerLocId != place) continue
            if (state.timeMin !in inFrom until end) continue
            if ((state.life[kRefused(def.id)] ?: -1) == state.absDay) continue
            return JobHit(def, "APPLY",
                refused = !standingOk(state, def))
        }
        return null
    }

    /** Where an application for [def] can be made to
     *  [npc]: the fixed boss/counter for paper and farm;
     *  for the shop pool, ANY counter whose keeper you're
     *  talking to — that's the on-ramp (the rota itself
     *  only ever sends you to the week's shop). Returns
     *  (boss, place, hoursFrom, hoursTo, shiftEnd). */
    private fun applicationFrame(state: GameState, def: JobDef,
        npc: String, playerLocId: String):
        Quintuple? {
        if (def.id == "shop") {
            val shop = shopByLoc(playerLocId) ?: return null
            if (shop.keeper != npc) return null
            return Quintuple(shop.keeper, shop.locId,
                shop.inFrom, shop.inTo, shop.shiftEnd)
        }
        return Quintuple(def.boss, def.workplace,
            def.inFrom, def.inTo, def.shiftEnd)
    }

    /** A poor man's 5-tuple (the codebase has none). */
    private data class Quintuple(val a: String, val b: String,
        val c: Int, val d: Int, val e: Int)

    // ---------------- judging ----------------

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
    }

    /** Plant the work thread in a head: a memory line and
     *  the Threads topic the follow-up banks speak. */
    private fun plantWorkThread(state: GameState, name: String,
                                memory: String) {
        state.agents[name]?.memories?.add(memory)
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** TALK judged (the Talk gate calls this on the WORK
     *  rail). Fires exactly the step talkStepFor named. */
    fun judgeTalk(state: GameState, npc: String,
                  playerLocId: String): JobHit? {
        val hit = talkStepFor(state, npc, playerLocId) ?: return null
        val def = hit.def
        val boss = bossFor(state, def)
        return when (hit.kind) {
            "APPLY" -> {
                if (hit.refused) {
                    state.life[kRefused(def.id)] = state.absDay
                    hit.copy(text = def.refuseText)
                } else {
                    state.lifeText[K_ID] = def.id
                    state.life[K_SINCE] = state.absDay
                    state.life[K_SHIFTS] = 0
                    state.life[K_STREAK] = 0
                    state.life[K_MISS] = 0
                    note(state, "${def.emoji} You're hired — ${def.title} " +
                        "(${def.daysLabel}). First shift: see the Journal's " +
                        "💼 WORK page.")
                    townLogLine(state, "${def.emoji} The new kid took the " +
                        "${def.title.lowercase()} job")
                    hit.copy(text = def.applyText)
                }
            }
            "CLOCK_IN" -> {
                state.life[K_SHIFT_DAY] = state.absDay
                state.life[K_CLOCKED] = 1
                state.life[K_DONE] = 0
                state.life[K_MASK] = 0
                val tasks = tasksFor(state, def)
                val card = tasks.joinToString(" · ") { it.title }
                hit.copy(text = def.clockInText +
                    "\n\n(⏰ On the clock — ${def.title}: $card. " +
                    "Clock out with $boss when they're all in.)")
            }
            "TASK" -> {
                val tasks = tasksFor(state, def)
                val mask = taskMask(state, def) or (1 shl hit.taskIndex)
                state.life[K_MASK] = mask
                val left = tasks.size - Integer.bitCount(mask)
                hit.copy(text = tasks[hit.taskIndex].text +
                    (if (left == 0)
                        "\n\n(That's the whole card — clock out with " +
                            "$boss.)"
                    else
                        "\n\n($left of ${tasks.size} tasks still on " +
                            "the card.)"))
            }
            "CLOCK_OUT" -> {
                val tasks = tasksFor(state, def)
                val newStreak = streak(state) + 1
                val bonus = tierBonus(newStreak)
                // v0.15.0h: a mastered site pays its master
                // the master's rate inside this same line —
                // Apprenticeships owns the mark; the pay is
                // the house's, like the tier bonus above it.
                val master = Apprenticeships.masterBonus(state, def)
                val pay = def.basePay + def.perTask * tasks.size +
                    bonus + master
                state.life[K_WEEKPAY] = weekPay(state) + pay
                state.life[K_SHIFTS] = shiftsAtJob(state) + 1
                state.life[kTotal(def.id)] = totalShifts(state, def) + 1
                state.life[K_STREAK] = newStreak
                state.life[K_MISS] = 0
                state.life[K_CLOCKED] = 0
                state.life[K_DONE] = 1
                // the shift sticks in the boss's head — and in
                // a coworker's, where the job has one the town
                // would name (Sharon keeps the farm's book)
                plantWorkThread(state, boss,
                    "${def.emoji} The new kid works the ${def.title} " +
                        "for me — shift ${shiftsAtJob(state)}, done right.")
                if (def.id == "farm") plantWorkThread(state, "Sharon",
                    "🌾 The new kid's farm work is in the book. " +
                        "The book approves so far.")
                if (def.id == "shop") customerFor(state, def)?.let { c ->
                    plantWorkThread(state, c,
                        "${def.emoji} The new kid served me at the " +
                            "counter. Proper service.")
                }
                note(state, "${def.emoji} Shift done — ${def.title}: " +
                    "$pay 🪙 earned (payday: the week's first morning)." +
                    (if (bonus > 0) " Includes the ${tierName(newStreak)} " +
                        "bonus." else "") +
                    (if (master > 0) " Includes the master's " +
                        "rate (+$master 🪙)." else ""))
                // v0.8.0b: a settled shift is also the record a
                // capstone's JOB beat reads — showing up, on
                // paper, is the whole testimony. Its line rides
                // this clock-out text so the player sees it land.
                val capHits = Capstones.onShiftCompleted(state)
                // v0.15.0h: the shift also lands on the
                // site's apprenticeship count — the rota's
                // own record of work actually finished.
                Apprenticeships.onShiftCompleted(state, def)
                hit.copy(text = def.clockOutText +
                    "\n\n(+$pay 🪙 on the week's tab — payday is the " +
                    "first morning of a new week.)" +
                    capHits.joinToString("") { capHit ->
                        "\n\n🏛 ${capHit.text}" +
                            (capHit.completionText?.let { "\n\n$it" } ?: "")
                    })
            }
            "QUIT" -> {
                quit(state)
                hit.copy(text = def.quitText)
            }
            else -> hit
        }
    }

    /** Give notice: the job ends, the record clears, the
     *  week's accrued pay stays yours (payday still
     *  comes). Totals and the fired count are history and
     *  keep. */
    fun quit(state: GameState) {
        val def = currentJob(state) ?: return
        state.lifeText[K_ID] = ""
        state.life[K_SINCE] = -1
        state.life[K_SHIFTS] = 0
        state.life[K_STREAK] = 0
        state.life[K_MISS] = 0
        state.life[K_SHIFT_DAY] = -1
        state.life[K_CLOCKED] = 0
        state.life[K_DONE] = 0
        state.life[K_MASK] = 0
        state.life[K_QUIT_DAY] = state.absDay
        note(state, "${def.emoji} You gave notice at the ${def.title}. " +
            "No hard feelings — the week's tab still pays out on payday.")
    }

    /** LOOK judged: the first unlanded LOOK task at this
     *  place, while a shift is live. Returns its text, or
     *  null. (The Look chain in GameActivity asks here
     *  after Dinners, like every participation beat.) */
    fun lookText(state: GameState, locId: String): String? {
        val def = currentJob(state) ?: return null
        if (!clockedIn(state) || shiftDoneToday(state)) return null
        val tasks = tasksFor(state, def)
        val mask = taskMask(state, def)
        val idx = tasks.indexOfFirst {
            it.kind == "LOOK" && it.target == locId &&
                mask and (1 shl tasks.indexOf(it)) == 0
        }
        if (idx < 0) return null
        val newMask = mask or (1 shl idx)
        state.life[K_MASK] = newMask
        val left = tasks.size - Integer.bitCount(newMask)
        return "💼 ${tasks[idx].title} — ${tasks[idx].text}" +
            (if (left == 0)
                "\n(That's the whole card — clock out with " +
                    "${bossFor(state, def)}.)"
            else
                "\n($left of ${tasks.size} tasks still on the card.)")
    }

    // ---------------- the boss comes to work ----------------

    /** The Engine's agenda lookup asks here (after the
     *  dinner gathering, before the finale's — set-pieces
     *  still win the person): while you hold the shop job,
     *  the week's keeper is at their counter for your
     *  shift, and your regular is there while you're
     *  clocked in. Work expects its people; that's what
     *  makes Freeman's weeks playable at all — Chef's own
     *  agenda never takes him there. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        val def = currentJob(state) ?: return null
        if (def.id != "shop" || !isShiftDay(state.absDay, def)) return null
        val shop = shopFor(state.absDay)
        if (timeMin !in shop.inFrom until shop.shiftEnd) return null
        if (npcName == shop.keeper)
            return AgendaBlock(shop.locId,
                "Behind the counter — the help's shift is today, " +
                    "and the rota is being honored.")
        if (clockedIn(state) && npcName == customerFor(state, def))
            return AgendaBlock(shop.locId,
                "In for their usual — your shift's regular.")
        return null
    }

    // ---------------- the midnight settlement ----------------

    /** Was yesterday's missed shift the town's fault, not
     *  yours? A case climax due at midnight, a finale week
     *  live yesterday, or a family table you actually sat
     *  at last night (evenings only excuse evening work —
     *  the dinner window has to overlap the shift). */
    fun excusedYesterday(state: GameState, def: JobDef,
                         endedAbsDay: Int): Boolean {
        if (Finales.climaxPending(state)) return true
        val yKey = keyFromOrdinal(ordinalOfKey(state.todayKey) - 1)
        if (yKey.isNotEmpty()) {
            val probe = state.copy(
                todayKey = yKey,
                gameYear = yKey.substring(0, 4).toIntOrNull()
                    ?: state.gameYear)
            if (Finales.windowAt(probe) != null) return true
        }
        val (_, _, end) = windowForDay(endedAbsDay, def)
        if (end > Dinners.DINNER_FROM &&
            Dinners.eveningSpent(state, endedAbsDay)) return true
        return false
    }

    /** Midnight: payday lands when the week turns, and
     *  yesterday's shift settles — a finished shift was
     *  already paid into the tab at clock-out; a partial
     *  one pays its tasks and keeps the streak; an
     *  unworked one is excused or counted. Called by
     *  Engine.onNewDay AFTER Dinners (its wraps are the
     *  dinner excuse's evidence) and BEFORE Phone. */
    fun dailyRollover(state: GameState) {
        if ((state.life[K_SETTLED] ?: -1) == state.absDay) return
        state.life[K_SETTLED] = state.absDay
        val ended = state.absDay - 1
        if (ended < 0) return
        val def = currentJob(state)
        // ---- yesterday's shift record settles first, so a
        // partial shift on the week's last day still makes
        // that week's payday ----
        val hadRecord = shiftDay(state) == ended
        if (hadRecord) {
            if ((state.life[K_DONE] ?: 0) == 1) {
                // clocked out yesterday — settled at the counter
            } else if ((state.life[K_CLOCKED] ?: 0) == 1 && def != null) {
                // a partial shift: the tasks pay into the tab,
                // the streak holds (you showed up), no miss.
                // Counted against YESTERDAY's card and mask —
                // tasksDone() reads the live day's record,
                // which has already rolled over
                val yProbe = state.copy(absDay = ended)
                val nTasks = tasksFor(yProbe, def).size
                val done = Integer.bitCount(
                    (state.life[K_MASK] ?: 0) and ((1 shl nTasks) - 1))
                val pay = def.perTask * done
                if (pay > 0) state.life[K_WEEKPAY] = weekPay(state) + pay
                note(state, "${def.emoji} Yesterday's ${def.title} " +
                    "shift ran out of day — $done task(s) on the " +
                    "card paid into the tab. ${bossFor(yProbe, def)} " +
                    "noted that you showed.")
            }
            state.life[K_SHIFT_DAY] = -1
            state.life[K_CLOCKED] = 0
            state.life[K_DONE] = 0
            state.life[K_MASK] = 0
        } else if (shiftDay(state) in 0 until ended) {
            // a stale record from a skipped settlement —
            // clear it without pay or blame
            state.life[K_SHIFT_DAY] = -1
            state.life[K_CLOCKED] = 0
            state.life[K_DONE] = 0
            state.life[K_MASK] = 0
        }
        // ---- payday: the week's tab, counted out ----
        if (weekIndex(state.absDay) != weekIndex(ended)) {
            val pay = weekPay(state)
            if (pay > 0) {
                Minigames.awardCoins(state, pay)
                state.life[K_EARNED] = lifetimeEarned(state) + pay
                state.life[K_WEEKPAY] = 0
                val line = "🪙 Payday — $pay 🪙 for last week's work, " +
                    "counted out and correct."
                state.lifeText[K_PAYLINE] = line
                note(state, line)
                townLogLine(state, "🪙 Payday — $pay 🪙 in wages")
            }
        }
        if (def == null) return
        // the boss of the day that ENDED (the shop rota may
        // have turned at midnight — yesterday's keeper is
        // the one who was stood up)
        val boss = bossFor(state.copy(absDay = ended), def)
        // ---- a scheduled shift that never started ----
        if (!isShiftDay(ended, def)) return
        if (hadRecord) return
        if (sinceDay(state) >= ended) return      // hired that day or later
        if ((state.life[K_QUIT_DAY] ?: -1) == ended) return  // notice given that day
        if (excusedYesterday(state, def, ended)) {
            note(state, "${def.emoji} $boss covered your ${def.title} " +
                "shift — the town was having one of its days, and " +
                "the rota knows it. No harm done.")
            return
        }
        val misses = missStreak(state) + 1
        state.life[K_MISS] = misses
        state.life[K_STREAK] = 0
        when {
            misses >= MISS_LIMIT -> {
                state.life[kFired(def.id)] = firedCount(state, def) + 1
                val pay = weekPay(state)
                state.lifeText[K_ID] = ""
                state.life[K_SINCE] = -1
                state.life[K_SHIFTS] = 0
                state.life[K_MISS] = 0
                note(state, "${def.emoji} $boss lets you go from the " +
                    "${def.title} — $MISS_LIMIT missed shifts in a " +
                    "row is a pattern, and the rota has filled your " +
                    "line." + (if (pay > 0)
                        " The week's tab ($pay 🪙) still pays out " +
                            "on payday." else ""))
                townLogLine(state, "${def.emoji} Let go from the " +
                    "${def.title} — three shifts missed")
            }
            misses == 2 ->
                note(state, "${def.emoji} ⚠️ $boss's second word " +
                    "about the missed ${def.title} shift is shorter " +
                    "than the first: one more no-show in a row and " +
                    "the rota fills your line.")
            else ->
                note(state, "${def.emoji} $boss mentions, casually " +
                    "and on the record, that yesterday's ${def.title} " +
                    "shift went unworked. A reminder — the kind one.")
        }
    }

    // ---------------- the Journal page ----------------

    /** The 💼 WORK page, assembled pure like its siblings:
     *  the job (or the three doors), the schedule, the
     *  streak and what it's worth, the tab and payday, and
     *  the record so far. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val def = currentJob(state)
        if (def == null) {
            out += "💼 WORK — no job right now. The town hires at " +
                "three counters:"
            for (j in JOBS) {
                val boss = if (j.id == "shop")
                    "the week's keeper (rota: ${SHOPS.joinToString(", ") {
                        LOCATIONS[it.locId]?.name ?: it.locId }})"
                else "${j.boss} at ${LOCATIONS[j.workplace]?.name ?: j.workplace}"
                out += "${j.emoji} ${j.title} — ${j.blurb} Apply: talk " +
                    "to $boss, ${j.daysLabel}, in work hours." +
                    if (!standingOk(state, j))
                        " (Their circle's standing with you is cold — " +
                            "mend it first.)" else ""
            }
            val anyTotal = JOBS.sumOf { totalShifts(state, it) }
            if (anyTotal > 0 || lifetimeEarned(state) > 0)
                out += "Lifetime: $anyTotal shift(s) worked, " +
                    "${lifetimeEarned(state)} 🪙 earned."
            paydayLine(state)?.let { out += it }
            return out
        }
        val boss = bossFor(state, def)
        val place = workplaceFor(state, def)
        val (inFrom, inTo, end) = windowFor(state, def)
        out += "${def.emoji} WORK — ${def.title} at " +
            "${LOCATIONS[place]?.name ?: place} (boss: $boss)"
        out += "Shifts ${def.daysLabel} — clock in " +
            "${windowLabel(inFrom, inTo)} at " +
            "${LOCATIONS[place]?.name ?: place}; the shift's day " +
            "ends ${minutesToTimeStr(end)}."
        if (def.id == "shop")
            out += "This week's counter: " +
                "${LOCATIONS[place]?.name ?: place} — the rota turns " +
                "with the week."
        out += "Record here: ${shiftsAtJob(state)} shift(s) done, " +
            "streak ${streak(state)} — ${tierName(streak(state))}" +
            (if (tierBonus(streak(state)) > 0)
                " (+${tierBonus(streak(state))} 🪙 a shift)" else "") +
            ". Lifetime at this work: ${totalShifts(state, def)} shift(s)."
        out += "On the week's tab: ${weekPay(state)} 🪙 — payday is " +
            "the first morning of a new week. Lifetime wages: " +
            "${lifetimeEarned(state)} 🪙."
        paydayLine(state)?.let { out += "Last payday: $it" }
        when {
            clockedIn(state) -> {
                val tasks = tasksFor(state, def)
                val mask = taskMask(state, def)
                out += "⏰ TODAY: clocked in — " +
                    "${Integer.bitCount(mask)} of ${tasks.size} tasks in:"
                for ((i, t) in tasks.withIndex())
                    out += if (mask and (1 shl i) != 0) "✔ ${t.title}"
                        else "▶ ${t.title}"
            }
            shiftDoneToday(state) ->
                out += "⏰ TODAY: shift done and clocked out. " +
                    "The card's clean."
            isShiftDay(state.absDay, def) ->
                out += "⏰ TODAY: a shift day — clock in with $boss " +
                    "between ${windowLabel(inFrom, inTo)}."
            else ->
                out += "⏰ TODAY: no shift — the rota rests."
        }
        val misses = missStreak(state)
        if (misses > 0)
            out += "⚠️ $misses missed shift(s) in a row on the " +
                "record — at $MISS_LIMIT in a row, $boss has to " +
                "let you go. (A shift missed to a finale, a case " +
                "climax or a family table is excused before it " +
                "counts.)"
        return out
    }
}

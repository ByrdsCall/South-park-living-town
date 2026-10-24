package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Town votes — v0.6.0f (Queue #6 item 6): the town decides
// small things, together, every week.
//
// Once a week (the game's Thursday-anchored week, Jobs'
// rhythm), City Hall posts ONE small civic question from the
// bank below: name the dock, pick the matinee, word the
// plaque. The player votes at the ballot box inside City
// Hall (by the request board); the vote is changeable until
// the count and retractable, and it weighs more once the
// town knows your name (THE FIXTURE RULE: your errand title
// is your civic weight — a Town Fixture's ballot counts
// double, an Indispensable's triple).
//
// The cast votes too, and their ballots are AUTHORED, not
// rolled: every option declares a champion, the factions it
// flatters and the Pocket Pal school it smells like, and a
// voter's lean is champion + faction + obsession over the
// same persona data that drives trades and crowd clusters —
// so Cartman votes Cartman-ishly every single week, proven
// in the suite. A whisper of seeded jitter keeps exact ties
// from feeling machined; the tally is a pure function of
// (week, question), identical on every screen.
//
// At the week's turn the count lands: the winner becomes a
// STANDING OUTCOME the town lives with — its trace line
// shows when you Look at the place it belongs to — for
// OUTCOME_WEEKS weeks or until the question is asked and
// counted again. Losing champions keep a gracious epitaph
// on the Journal's 🗳 TOWN VOTES page, and the champions of
// the top two finishers carry the result into their talks
// (the "vote" thread). No coins move. Nothing is gated.
// It's citizenship, not an economy.
//
// State: no new save keys — the ballot, the count and the
// standings ride the existing `life`/`lifeText` maps, every
// read clamped against the bank (a choice that isn't on this
// week's paper is no choice at all). The question itself is
// never stored: it is a pure function of the week.
// ============================================================

object Votes {

    // ---------------- the dials (all suite-pinned) ----------------

    /** A question never repeats within this many weeks. The
     *  rotation is least-recently-used over the bank, so with
     *  a 12-question bank the true spacing is wider — this is
     *  the floor the suite holds it to. */
    const val NO_REPEAT_WEEKS = 6

    /** A standing outcome lives this many weeks from its
     *  count, unless its question is recounted first (the new
     *  winner takes the trace over, win or lose). */
    const val OUTCOME_WEEKS = 8

    /** Past results kept on the Journal page. */
    const val HISTORY_CAP = 8

    /** Lean weights: a champion backs their own option hard;
     *  a flattered faction leans; an obsession is a nudge. */
    const val CHAMPION_LEAN = 6
    const val FACTION_LEAN = 2
    const val SCHOOL_LEAN = 1

    // ---------------- the bank ----------------

    data class VoteOption(
        val id: String,
        val label: String,
        val champion: String,             // cast name, in KID_ORDER
        val factions: Set<String>,        // faction ids this option flatters
        val school: String?,              // PocketPals school it smells like, or null
        val trace: String,                // the Look line at the place while it stands
        val epitaph: String               // the champion's gracious loser line
    )

    data class VoteQuestion(
        val id: String,
        val title: String,
        val ask: String,                  // the question as posted at City Hall
        val locId: String,                // where the winner leaves its trace
        val options: List<VoteOption>
    )

    val QUESTIONS: List<VoteQuestion> = listOf(
        VoteQuestion("dock_name", "Name the Pond's New Dock",
            "Stark's Pond has a new dock and the old sign rotted off. What does the new one say?",
            "StarksPond", listOf(
                VoteOption("wobble", "The Wobble", "Jimbo",
                    setOf("tegridy_crew"), "Grit",
                    "The dock's new sign reads THE WOBBLE, carved slightly crooked. Jimbo insists the crooked part is load-bearing.",
                    "Jimbo is still calling it the Wobble. The sign is wrong, not him."),
                VoteOption("landing", "Stark's Landing", "Stan",
                    setOf("boys_crew"), "Leaf",
                    "A neat new sign on the dock reads STARK'S LANDING, leveled twice and painted once. The pond approves.",
                    "Stan measured the write-in campaign twice. It still wasn't a write-in campaign."),
                VoteOption("planks", "Sir Planksalot", "Butters",
                    setOf("boys_crew", "parents"), "Spark",
                    "The dock's sign reads SIR PLANKSALOT, with a small painted sword. Somebody salutes it most mornings. It's Butters.",
                    "Butters still salutes the dock. Sir Planksalot lives on in his heart."))),
        VoteQuestion("bijou_matinee", "Pick the Bijou's Saturday Matinee",
            "The Bijou can only afford one print for Saturday. The town picks the movie.",
            "Bijou", listOf(
                VoteOption("pancakes", "The Last Pancake", "Chef",
                    setOf("school_staff", "parents"), "Grit",
                    "The Bijou marquee reads THE LAST PANCAKE — SAT MATINEE. Chef has reviewed it four times. All raves.",
                    "Chef maintains the town chose spectacle over breakfast, and history will judge it."),
                VoteOption("moo", "Moo: A Cow's Journey", "Stuart",
                    setOf("parents", "tegridy_crew"), "Leaf",
                    "The Bijou marquee reads MOO: A COW'S JOURNEY. Stuart cried at the trailer. Twice. It's a poster.",
                    "Stuart says the cow deserved the big screen. The cow, reached for comment, said moo."),
                VoteOption("space", "Space Campers III", "Kevin",
                    setOf("boys_crew"), "Spark",
                    "The Bijou marquee reads SPACE CAMPERS III: HOMEWORK IN ORBIT. Kevin has explained the first two to the usher. At length.",
                    "Kevin has begun a letter-writing campaign for a recount. In space. Where no one can hear the recount."))),
        VoteQuestion("bench_plaque", "Words for the Park Bench Plaque",
            "The park bench by the sandbox is getting a brass plaque. The town writes the words.",
            "KidsPark", listOf(
                VoteOption("earned", "“Sit. You've Earned It.”", "Liane",
                    setOf("parents"), "Frost",
                    "The bench's brass plaque reads: SIT. YOU'VE EARNED IT. Sitters sit a little taller now.",
                    "Liane still thinks a bench should feel like a compliment. The bench says nothing."),
                VoteOption("nobody", "“For Nobody, Who Loved This Bench.”", "Henrietta",
                    setOf("goth_kids"), "Frost",
                    "The bench's plaque reads: FOR NOBODY, WHO LOVED THIS BENCH. The goths approve. Quietly. From a distance.",
                    "Henrietta says Nobody would have hated winning anyway. Nobody agrees."),
                VoteOption("wobbly", "“This Bench Is Intentionally Wobbly.”", "Craig",
                    setOf("craigs_gang"), "Grit",
                    "The bench's plaque reads: THIS BENCH IS INTENTIONALLY WOBBLY. Craig nodded once. It was enough.",
                    "Craig has accepted the result with his usual statement: a single, level stare."))),
        VoteQuestion("mascot_hat", "A New Hat for the School Mascot",
            "The elementary mascot's hat disintegrated at the last pep rally. The town picks its replacement.",
            "Elementary", listOf(
                VoteOption("wizard", "a wizard hat", "Cartman",
                    setOf("boys_crew"), "Spark",
                    "The school mascot now wears a tall wizard hat. Cartman takes full credit and partial questions.",
                    "Cartman insists the mascot looks unmagical and the election was rigged by elves."),
                VoteOption("cone", "a party cone", "Butters",
                    setOf("boys_crew", "girls_circle"), "Spark",
                    "The school mascot now wears a party cone, slightly crooked, permanently festive. Morale is up 4%.",
                    "Butters says every day can still be a party. The cone is in his locker, just in case."),
                VoteOption("visor", "a sensible visor", "Garrison",
                    setOf("school_staff"), "Grit",
                    "The school mascot now wears a sensible visor. Garrison calls it 'the only adult decision this town has made.'",
                    "Garrison has filed the result under 'democracy's little failures' and moved on."))),
        VoteQuestion("story_hour", "Story-Hour Theme at the Elementary",
            "Story hour needs a theme for the season. The town picks what the little kids hear.",
            "Elementary", listOf(
                VoteOption("pirates", "Pirates, Obviously", "Kenny",
                    setOf("boys_crew"), "Spark",
                    "Story hour is pirates this season. The reading corner has a paper parrot and a strict no-plank policy.",
                    "Kenny's review of the winning theme, delivered through the parka, was muffled but devastating."),
                VoteOption("feelings", "Feelings, But Funny", "Wendy",
                    setOf("girls_circle"), "Frost",
                    "Story hour is feelings this season, but funny. The reading corner has a feelings chart with googly eyes.",
                    "Wendy has donated three books about sharing to the pirate corner. It's called balance."),
                VoteOption("trucks", "Trucks: A Love Story", "Jimmy",
                    setOf("craigs_gang"), "Grit",
                    "Story hour is trucks this season. The reading corner's rug now has a road on it. Jimmy narrates the vrooms.",
                    "Jimmy says the town voted against literature, and the bit killed at recess anyway."))),
        VoteQuestion("tower_paint", "Paint the Water Tower",
            "The water tower needs paint before spring. The town picks what goes up there.",
            "MainStreet", listOf(
                VoteOption("seal", "the town seal, but bigger", "McDaniels",
                    setOf("parents", "school_staff"), null,
                    "The water tower wears the town seal, but bigger. McDaniels calls it 'appropriately municipal.' Pilots can read it.",
                    "McDaniels has reminded everyone that the seal remains available for other surfaces."),
                VoteOption("sun", "a smiling sun", "Red",
                    setOf("girls_circle"), "Spark",
                    "The water tower wears a huge smiling sun. On cloudy days it does the sky's job for it, free of charge.",
                    "Red maintains the tower is now 30% less cheerful than democracy intended."),
                VoteOption("beige", "fresh beige, professionally applied", "Mackey",
                    setOf("school_staff"), "Grit",
                    "The water tower is fresh beige, professionally applied. Mackey finds it soothing, m'kay. Nobody else has an opinion. That's the point.",
                    "Mackey has accepted the result, m'kay, and scheduled his feelings about it for never."))),
        VoteQuestion("crosswalk", "The Main Street Crosswalk Mural",
            "Main Street's crosswalk is being repainted anyway. The town picks the pattern.",
            "MainStreet", listOf(
                VoteOption("keys", "piano keys", "Tolkien",
                    setOf("boys_crew", "girls_circle"), "Frost",
                    "The Main Street crosswalk is painted as piano keys. Pedestrians keep humming. Tolkien keeps conducting.",
                    "Tolkien has composed a short piece about the result. It is in a minor key. On purpose."),
                VoteOption("cowspots", "cow spots", "Randy",
                    setOf("tegridy_crew"), "Leaf",
                    "The Main Street crosswalk is painted in cow spots. Randy calls it 'agricultural crosswalk technology.' Cars slow down out of respect.",
                    "Randy says the town voted against agriculture and he will be taking it up with the concept of roads."),
                VoteOption("stripes", "leave it stripes — it is a crosswalk", "Barbrady",
                    emptySet(), "Grit",
                    "The Main Street crosswalk is stripes. Just stripes. Barbrady salutes it every morning. Nothing to see here.",
                    "Barbrady has no statement on the result, which for him is a filibuster."))),
        VoteQuestion("fountain_coins", "Where the Fountain Coins Go",
            "The park fountain is full of wished-on coins. Once a season, the town empties it — and picks where they go.",
            "KidsPark", listOf(
                VoteOption("shelter", "the animal shelter", "Wendy",
                    setOf("girls_circle"), "Leaf",
                    "A small brass tag on the fountain reads: THIS SEASON'S COINS GO TO THE ANIMAL SHELTER. The wishes, presumably, approve.",
                    "Wendy has written the shelter a letter anyway. It contains one (1) coin and a lot of feelings."),
                VoteOption("benchfund", "the Permanent Bench Repair Fund", "Clyde",
                    setOf("craigs_gang"), "Grit",
                    "A small brass tag on the fountain reads: THIS SEASON'S COINS GO TO THE PERMANENT BENCH REPAIR FUND. The bench has never been safer.",
                    "Clyde says the bench will remember this betrayal. The bench, notably, is a bench."),
                VoteOption("wishes", "back into wishes", "Kenny",
                    setOf("boys_crew"), "Spark",
                    "The fountain's coins were thrown back in, every one, for luck. The fountain is now technically a savings account.",
                    "Kenny threw his own quarter in as a protest and immediately wished for it back."))),
        VoteQuestion("winter_lights", "Main Street's Winter Lights",
            "It's that time: Main Street's winter lights go up soon. The town picks the look.",
            "MainStreet", listOf(
                VoteOption("warm", "warm white, all of it", "Sharon",
                    setOf("parents"), "Frost",
                    "Main Street's winter lights are warm white, all of it. Sharon calls it 'the street, but remembered fondly.'",
                    "Sharon says the street will simply have to glow on the inside this year."),
                VoteOption("rainbow", "every color, twice", "Cartman",
                    setOf("boys_crew"), "Spark",
                    "Main Street's winter lights are every color, twice. Cartman calls it 'finally, enough light.' Aircraft have adjusted.",
                    "Cartman insists the street looks underdressed and has begun pricing his own bulbs."),
                VoteOption("bulb", "one (1) tasteful bulb", "Gerald",
                    setOf("parents"), "Grit",
                    "Main Street's winter display is one (1) tasteful bulb. Gerald drafted the motion. It is, legally, perfect.",
                    "Gerald has billed the result for one (1) hour of emotional labor. The invoice is tasteful."))),
        VoteQuestion("friday_lunch", "Friday's School Lunch",
            "The elementary cafeteria is changing Friday's menu. The town picks what Fridays taste like.",
            "Elementary", listOf(
                VoteOption("taco", "Taco Friday", "Chef",
                    setOf("school_staff"), "Grit",
                    "Friday at the elementary is Taco Friday now. Chef sings the menu. The children sing the chorus.",
                    "Chef maintains the people were denied their birthright, and has written a song about it. It slaps."),
                VoteOption("pizza", "Pizza Friday", "Clyde",
                    setOf("craigs_gang"), "Spark",
                    "Friday at the elementary is Pizza Friday now. Clyde has opinions about crust and a podium to share them from.",
                    "Clyde says pizza will be back. Pizza always comes back. This is a threat and a promise."),
                VoteOption("salad", "Salad Surprise Friday", "Mrs. Tweek",
                    setOf("parents"), "Leaf",
                    "Friday at the elementary is Salad Surprise Friday. The surprise is that it's salad. Mrs. Tweek winks like it's more.",
                    "Mrs. Tweek says surprise was the whole point, and the surprise is that you're all still surprised."))),
        VoteQuestion("garden_name", "Name the Park's Garden Corner",
            "The garden corner at Kid's Park finally has more flowers than weeds. It has earned a name.",
            "KidsPark", listOf(
                VoteOption("victory", "Victory Garden", "Stephen",
                    setOf("parents"), "Leaf",
                    "The garden corner's little sign reads VICTORY GARDEN. Stephen grounds any weed that disputes it.",
                    "Stephen has grounded the result for one week. The result does not live at his house, but still."),
                VoteOption("annex", "The Tegridy Annex (Unaffiliated)", "Randy",
                    setOf("tegridy_crew"), "Leaf",
                    "The garden corner's sign reads THE TEGRIDY ANNEX (UNAFFILIATED). The parentheses were Randy's one condition. He checks on it. Often.",
                    "Randy says the garden knows what it is, whatever the sign claims, and so does he."),
                VoteOption("kennys", "Kenny's Corner", "Stan",
                    setOf("boys_crew"), "Frost",
                    "The garden corner's little sign reads KENNY'S CORNER. The flowers there are the hardiest in the park. Nobody is surprised.",
                    "Stan says the corner is still Kenny's in spirit. The flowers, being hardy, have not commented."))),
        VoteQuestion("bus_poster", "The Bus Stop Poster",
            "The bus stop's poster case is empty and the town keeps looking at it. It needs a poster.",
            "BusStop", listOf(
                VoteOption("safety", "a safety poster, Barbrady pointing", "Barbrady",
                    emptySet(), null,
                    "The bus stop poster shows a very serious officer pointing at the words LOOK BOTH WAYS. Barbrady points back, daily, for accuracy.",
                    "Barbrady has begun pointing at the poster on duty anyway. Morale is unaffected."),
                VoteOption("coming", "“BUSES: THEY'RE COMING.”", "Crabtree",
                    emptySet(), "Grit",
                    "The bus stop poster reads, in letters a block high: BUSES: THEY'RE COMING. Crabtree nods at it like a colleague.",
                    "Crabtree maintains the winning poster lacks urgency, and has offered to yell at the stop hourly."),
                VoteOption("art", "a rotating kid-art poster", "Tolkien",
                    setOf("boys_crew"), "Frost",
                    "The bus stop poster case holds kid art now, rotating weekly. Tolkien curates. The current piece is mostly a sun. It's a good sun.",
                    "Tolkien has hung his runner-up poster at home, where it receives the foot traffic it deserves.")))
    )

    fun questionById(id: String?): VoteQuestion? =
        QUESTIONS.firstOrNull { it.id == id }

    // ---------------- the rotation ----------------
    // Least-recently-used over the bank, tie-broken by a
    // seeded hash: a pure function of the week, recomputed
    // from week 0 whenever asked (the game asks rarely; the
    // memo below makes it free after the first pass). With
    // 12 questions and LRU, spacing runs far past the pinned
    // NO_REPEAT_WEEKS floor, and no question can starve —
    // the longest-unused always wins its week eventually.

    private val rotationMemo = mutableListOf<Int>()
    private val lastUsedScratch = IntArray(QUESTIONS.size)
    private val rotationLock = Any()

    private fun jitter(week: Int, questionId: String): Int =
        (Random(week * 7919L + questionId.hashCode().toLong() * 131L)
            .nextInt(1000))

    fun questionIndexForWeek(week: Int): Int {
        if (week < 0) return ((week % QUESTIONS.size) + QUESTIONS.size) % QUESTIONS.size
        synchronized(rotationLock) {
        while (rotationMemo.size <= week) {
            val w = rotationMemo.size
            if (w == 0) {
                for (i in lastUsedScratch.indices) lastUsedScratch[i] = -1000 - i * 7
            }
            var best = 0
            var bestScore = Long.MIN_VALUE
            for (i in QUESTIONS.indices) {
                val gap = w - lastUsedScratch[i]
                if (w > 0 && gap < NO_REPEAT_WEEKS) continue
                val score = gap * 1000L + jitter(w, QUESTIONS[i].id)
                if (score > bestScore) { bestScore = score; best = i }
            }
            rotationMemo.add(best)
            lastUsedScratch[best] = w
        }
        return rotationMemo[week]
        }
    }

    fun questionForWeek(week: Int): VoteQuestion =
        QUESTIONS[questionIndexForWeek(week)]

    fun currentQuestion(state: GameState): VoteQuestion =
        questionForWeek(Jobs.weekIndex(state.absDay))

    // ---------------- the cast ballots ----------------

    /** How much [name] leans toward [opt] in question [q]
     *  on [week]: the champion's conviction, their faction's
     *  flattery and their obsession's nudge, over a whisper
     *  of seeded jitter. Pure — the same week always leans
     *  the same way, which is what makes the tally authored. */
    fun lean(name: String, q: VoteQuestion, opt: VoteOption, week: Int): Int {
        var v = jitter(week * 3 + name.hashCode(), q.id + ":" + opt.id) / 500  // 0..1
        if (name == opt.champion) v += CHAMPION_LEAN
        val f = factionOfCast(name)
        if (f != null && f in opt.factions) v += FACTION_LEAN
        if (opt.school != null && PocketPals.obsessionOf(name) == opt.school)
            v += SCHOOL_LEAN
        return v
    }

    /** The option [name] actually votes for. */
    fun ballotOf(name: String, q: VoteQuestion, week: Int): String {
        var best = q.options[0]
        var bestLean = lean(name, q, best, week)
        for (opt in q.options.drop(1)) {
            val l = lean(name, q, opt, week)
            if (l > bestLean) { best = opt; bestLean = l }
        }
        return best.id
    }

    /** The whole town's ballots for [week]'s question —
     *  cast only, before the player's ballot is laid on top. */
    fun castTally(q: VoteQuestion, week: Int): Map<String, Int> {
        val out = LinkedHashMap<String, Int>()
        for (opt in q.options) out[opt.id] = 0
        for (name in KID_ORDER) {
            val b = ballotOf(name, q, week)
            out[b] = (out[b] ?: 0) + 1
        }
        return out
    }

    /** THE FIXTURE RULE: the town weighs the ballot of a kid
     *  it knows. Your errand title is your civic weight —
     *  Neighborly still counts once; Town Fixture counts
     *  double; Indispensable, triple. (Jobs pay coins. Errands
     *  pay citizenship.) */
    fun playerWeight(state: GameState): Int =
        when (Errands.titleFor(state.errandsDoneCount)) {
            "Indispensable" -> 3
            "Town Fixture" -> 2
            else -> 1
        }

    // ---------------- state (life / lifeText, clamped) ----------------

    private const val K_VWEEK = "voteWeek"        // life: week of your recorded ballot (-1 = none)
    private const val K_VCHOICE = "voteChoice"    // lifeText: its option id
    private const val K_COUNTED = "voteCountedWeek"  // life: last week counted
    private const val K_RESULT = "voteLastResult" // lifeText: the last count, piped
    private const val K_HISTORY = "voteHistory"   // lifeText: ";" entries, newest first
    private fun kStand(qid: String) = "voteStand:$qid"       // lifeText: standing option id
    private fun kStandDay(qid: String) = "voteStandDay:$qid" // life: absDay it was counted

    data class CountResult(
        val questionId: String, val optionId: String,
        val margin: Int, val winnerVotes: Int, val totalVotes: Int,
        val countedDay: Int, val week: Int,
        val playerVoted: Boolean, val playerWon: Boolean,
        val runnerId: String = ""
    ) {
        fun question(): VoteQuestion? = questionById(questionId)
        fun option(): VoteOption? =
            question()?.options?.firstOrNull { it.id == optionId }
        fun winnerLabel(): String = option()?.label ?: optionId
    }

    /** Your ballot for THIS week, if you have one on record —
     *  clamped: a stored choice that isn't on this week's
     *  paper (or is filed under another week) is no ballot. */
    fun playerVote(state: GameState): String? {
        val wk = Jobs.weekIndex(state.absDay)
        if ((state.life[K_VWEEK] ?: -1) != wk) return null
        val choice = state.lifeText[K_VCHOICE] ?: return null
        val q = questionForWeek(wk)
        return choice.takeIf { c -> q.options.any { it.id == c } }
    }

    /** Cast (or change) your ballot. False if the option is
     *  not on this week's paper. */
    fun castVote(state: GameState, optionId: String): Boolean {
        val wk = Jobs.weekIndex(state.absDay)
        val q = questionForWeek(wk)
        if (q.options.none { it.id == optionId }) return false
        state.life[K_VWEEK] = wk
        state.lifeText[K_VCHOICE] = optionId
        return true
    }

    /** Take your ballot back. No hard feelings; the box has
     *  seen worse. */
    fun retractVote(state: GameState) {
        if (playerVote(state) == null) return
        state.life[K_VWEEK] = -1
        state.lifeText.remove(K_VCHOICE)
    }

    /** The last count, parsed — null before the first one, or
     *  if the record is malformed in any way. */
    fun lastResult(state: GameState): CountResult? {
        val raw = state.lifeText[K_RESULT] ?: return null
        val p = raw.split("|")
        if (p.size != 10) return null
        val q = questionById(p[0]) ?: return null
        if (q.options.none { it.id == p[1] }) return null
        if (p[9].isNotEmpty() && q.options.none { it.id == p[9] }) return null
        return CountResult(
            questionId = p[0], optionId = p[1],
            margin = p[2].toIntOrNull() ?: return null,
            winnerVotes = p[3].toIntOrNull() ?: return null,
            totalVotes = p[4].toIntOrNull() ?: return null,
            countedDay = p[5].toIntOrNull() ?: return null,
            week = p[6].toIntOrNull() ?: return null,
            playerVoted = p[7] == "1", playerWon = p[8] == "1",
            runnerId = p[9])
    }

    /** A result counted at THIS morning's rollover — the
     *  phone's cue to text it. */
    fun freshResult(state: GameState): CountResult? =
        lastResult(state)?.takeIf { it.countedDay == state.absDay }

    data class HistoryEntry(val week: Int, val questionId: String,
                            val optionId: String, val margin: Int)

    fun history(state: GameState): List<HistoryEntry> {
        val raw = state.lifeText[K_HISTORY] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            val p = e.split(":")
            if (p.size != 4) return@mapNotNull null
            val q = questionById(p[1]) ?: return@mapNotNull null
            if (q.options.none { it.id == p[2] }) return@mapNotNull null
            HistoryEntry(p[0].toIntOrNull() ?: return@mapNotNull null,
                p[1], p[2], p[3].toIntOrNull() ?: return@mapNotNull null)
        }.take(HISTORY_CAP)
    }

    /** The option currently standing for [q] — the count's
     *  winner, alive for OUTCOME_WEEKS weeks from its count
     *  (a recount of the same question replaces it outright,
     *  win or lose, because the new count rewrites the day). */
    fun standingOption(state: GameState, q: VoteQuestion): VoteOption? {
        val oid = state.lifeText[kStand(q.id)] ?: return null
        val opt = q.options.firstOrNull { it.id == oid } ?: return null
        val day = state.life[kStandDay(q.id)] ?: return null
        val age = state.absDay - day
        return if (age in 0 until OUTCOME_WEEKS * 7) opt else null
    }

    // ---------------- the count ----------------

    private fun plantVoteTopic(name: String) {
        if (name !in KID_ORDER) return
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove("vote")
        mem.topics.add("vote")
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** Count one finished [week] over the state as it stands
     *  (the player's ballot counts if it was filed under that
     *  week). Writes the result, the history, the standing
     *  outcome, the announcement, and the champions' memory. */
    fun countWeek(state: GameState, week: Int) {
        val q = questionForWeek(week)
        val tally = LinkedHashMap(castTally(q, week))
        var playerVoted = false
        var playerChoice: String? = null
        if ((state.life[K_VWEEK] ?: -1) == week) {
            val choice = state.lifeText[K_VCHOICE]
            if (choice != null && q.options.any { it.id == choice }) {
                tally[choice] = (tally[choice] ?: 0) + playerWeight(state)
                playerVoted = true
                playerChoice = choice
            }
        }
        // winner: bank order, strict majority of leans — a tie
        // keeps the earlier option (the paper's own order is
        // the town's tiebreak, and it is deterministic).
        var win = q.options[0]
        var winVotes = tally[win.id] ?: 0
        for (opt in q.options.drop(1)) {
            val v = tally[opt.id] ?: 0
            if (v > winVotes) { win = opt; winVotes = v }
        }
        val runnerVotes = q.options.filter { it.id != win.id }
            .maxOfOrNull { tally[it.id] ?: 0 } ?: 0
        val runner = q.options.firstOrNull {
            it.id != win.id && (tally[it.id] ?: 0) == runnerVotes }
        val margin = winVotes - runnerVotes
        val total = tally.values.sum()
        // ---- records ----
        state.lifeText[kStand(q.id)] = win.id
        state.life[kStandDay(q.id)] = state.absDay
        state.lifeText[K_RESULT] = listOf(
            q.id, win.id, margin, winVotes, total, state.absDay, week,
            if (playerVoted) "1" else "0",
            if (playerVoted && playerChoice == win.id) "1" else "0",
            runner?.id ?: ""
        ).joinToString("|")
        val entry = "$week:${q.id}:${win.id}:$margin"
        val hist = (listOf(entry) + history(state).map {
            "${it.week}:${it.questionId}:${it.optionId}:${it.margin}"
        }).take(HISTORY_CAP)
        state.lifeText[K_HISTORY] = hist.joinToString(";")
        // ---- the town hears it ----
        state.events.add("🗳 THE COUNT IS IN: '${win.label}' carries " +
            "'${q.title}' by $margin. The town lives with it now.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🗳 ${q.title} — ${win.label} (by $margin)")
        plantVoteTopic(win.champion)
        runner?.let { plantVoteTopic(it.champion) }
        // v0.8.0b: the count itself can land a capstone's
        // ratification beat — if the player's ballot was in
        // this week's count, the Circle's charter is touched
        // by the town (Capstones owns the beat; the line
        // rides the ticker, as rollover news does).
        Capstones.onVoteCounted(state, playerVoted)
    }

    /** Midnight: every finished, uncounted week is counted
     *  (normally exactly one — the one that just turned), and
     *  a ballot filed under a finished week is shed. Runs
     *  after Jobs and before the Phone in Engine.onNewDay:
     *  the count is a morning fact like any other. */
    fun dailyRollover(state: GameState) {
        val wk = Jobs.weekIndex(state.absDay)
        var counted = state.life[K_COUNTED] ?: -1
        var w = counted + 1
        while (w < wk) {
            countWeek(state, w)
            counted = w
            w++
        }
        if (counted >= 0) state.life[K_COUNTED] = counted
        if ((state.life[K_VWEEK] ?: -1) in 0 until wk) {
            state.life[K_VWEEK] = -1
            state.lifeText.remove(K_VCHOICE)
        }
    }

    // ---------------- surfaces ----------------

    /** The morning report's line: the fresh count on its
     *  morning; on a week's last day, a one-time nudge if
     *  your ballot is still in your pocket. Otherwise null —
     *  City Hall business doesn't nag. */
    fun morningNote(state: GameState): String? {
        freshResult(state)?.let { res ->
            val q = res.question()
            return "🗳 The count is in at City Hall: '${res.winnerLabel()}' " +
                "carries '${q?.title ?: res.questionId}' by ${res.margin}. " +
                (if (res.playerWon) "Your pick — democracy looks good on you."
                else if (res.playerVoted) "Not your pick, this time."
                else "The town decided without you, this time.")
        }
        val wk = Jobs.weekIndex(state.absDay)
        if (Jobs.weekIndex(state.absDay + 1) != wk && playerVote(state) == null) {
            val q = questionForWeek(wk)
            return "🗳 Last day to vote at City Hall: '${q.title}'. " +
                "The ballot box stands inside, by the request board."
        }
        return null
    }

    /** The Look text where outcomes live: at a standing
     *  outcome's place, its trace (the freshest count wins
     *  the line); at City Hall, this week's question and
     *  your ballot's state. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId == "CityHall") {
            val q = currentQuestion(state)
            val mine = playerVote(state)
            return "🗳 The ballot box stands by the request board. " +
                "This week's question: '${q.title}' — ${q.ask}" +
                (if (mine != null)
                    " Your ballot is in: '${q.options.firstOrNull { it.id == mine }?.label}'."
                else " You haven't voted yet.")
        }
        var best: VoteOption? = null
        var bestDay = Int.MIN_VALUE
        for (q in QUESTIONS) {
            if (q.locId != locId) continue
            val opt = standingOption(state, q) ?: continue
            val day = state.life[kStandDay(q.id)] ?: 0
            if (day > bestDay) { bestDay = day; best = opt }
        }
        return best?.trace
    }

    /** The 🗳 TOWN VOTES Journal page, assembled pure like
     *  the other boards: this week's paper, the standings
     *  in force, and the counts the town remembers. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val q = currentQuestion(state)
        val wk = Jobs.weekIndex(state.absDay)
        out += "🗳 TOWN VOTES — week ${wk + 1} at City Hall"
        out += "❓ ${q.title}: ${q.ask}"
        val mine = playerVote(state)
        for (opt in q.options) {
            out += (if (mine == opt.id) "✔ YOUR BALLOT: " else "· ") +
                "${opt.label} — championed by ${opt.champion}"
        }
        if (mine == null) {
            out += "You haven't voted. The ballot box stands inside " +
                "City Hall, by the request board — one ballot, " +
                "changeable until the week turns (Thursday)."
        } else {
            val w = playerWeight(state)
            out += "Your ballot is in" +
                (if (w > 1) " — and it counts ×$w. The town knows " +
                    "${Errands.titleFor(state.errandsDoneCount)} when it sees one."
                else ".") +
                " You can change it at the box until the count."
        }
        val standing = QUESTIONS.mapNotNull { qq ->
            standingOption(state, qq)?.let { qq to it }
        }
        if (standing.isNotEmpty()) {
            out += "🏛 STANDING IN TOWN:"
            for ((qq, opt) in standing) {
                val left = OUTCOME_WEEKS * 7 -
                    (state.absDay - (state.life[kStandDay(qq.id)] ?: state.absDay))
                out += "· ${qq.title}: '${opt.label}' " +
                    "(${LOCATIONS[qq.locId]?.name ?: qq.locId}, ${maxOf(left, 0)} days left)"
            }
        }
        val hist = history(state)
        if (hist.isNotEmpty()) {
            out += "📜 PAST COUNTS:"
            for (h in hist) {
                val hq = questionById(h.questionId)
                val ho = hq?.options?.firstOrNull { it.id == h.optionId }
                out += "· ${hq?.title ?: h.questionId}: '${ho?.label ?: h.optionId}' " +
                    "by ${h.margin}"
            }
            lastResult(state)?.let { res ->
                val runner = res.question()?.options
                    ?.firstOrNull { it.id == res.runnerId }
                if (runner != null) {
                    out += "· ${runner.champion}, graciously: " +
                        "“${runner.epitaph}”"
                }
            }
        }
        return out
    }
}

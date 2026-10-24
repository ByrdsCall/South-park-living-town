package com.hunt3r.southparktown

// ============================================================
// House — v0.3.0d: YOUR house (the 27th location, YOUR_HOUSE).
//
// Everything about the player's home that can be derived, is:
// the trophy shelf reads the collection + feat sets, the decor
// reads the town's permanent marks (episodes finished, feats
// earned, the mystery's ending), the mail is a seeded function
// of the day. NO new save keys — an old save walks into a house
// that already knows what its owner has done.
//
// The bed's sleep + the morning report live here too (pure, so
// the suite can prove the bed is exactly the natural midnight
// path wearing pajamas). Pure Kotlin, zero android.* imports.
// ============================================================

/** One earned object for the room. [token] is the marker
 *  InteriorArt draws (`// decor: <token>`), gated at render time
 *  by TownInfo.houseDecor — the room literally fills as you play. */
data class HouseDecor(
    val id: String,
    val token: String,
    val name: String,
    val hint: String
)

object House {

    /** When the bed lets go of you: 7:00 AM. */
    const val WAKE_MIN = 7 * 60

    /** Trophy-shelf capacity for collectibles (COLLECTIBLES order). */
    const val SHELF_SLOTS = 12

    /** Feat trophies shown as cups before they become a count. */
    const val MAX_CUPS = 6

    /** The decor table: every piece is EARNED from an existing
     *  achievement record — nothing here is bought or found. */
    val DECOR: List<HouseDecor> = listOf(
        HouseDecor("episode_keepsake", "episode-poster",
            "Episode Poster",
            "Finish an episode of the town's ongoing TV."),
        HouseDecor("photo_strip", "photo-strip",
            "Photo Strip",
            "Earn a photo feat — the strip goes up by the cork board."),
        HouseDecor("game_trophy", "game-trophy",
            "Game Trophy",
            "Earn a minigame feat at the pond, the snow or the taco stand."),
        HouseDecor("mystery_truth", "foil-star",
            "Foil Star",
            "Close the pond case by telling what you know."),
        HouseDecor("mystery_silent", "sealed-envelope",
            "Sealed Envelope",
            "Close the pond case by keeping what you know."),
        HouseDecor("mystery_town", "tiny-gavel",
            "Tiny Gavel",
            "Let the town close the pond case itself."),
        // v0.4.0d: the holidays move in too — a beat played
        // is a year lived, and the room keeps the receipts.
        HouseDecor("holiday_garland", "holiday-garland",
            "Holiday Garland",
            "Play a holiday's own beat — any holiday, any year."),
        HouseDecor("full_calendar", "full-calendar",
            "The Full Calendar",
            "Play all eight holidays' beats, across the years."),
        // v0.5.0h: the season finales hang here too — one
        // piece per season, earned by taking part in that
        // season's finale (a beat caught, a call made).
        HouseDecor("finale_garden", "garden-ribbon",
            "Garden Verdict Ribbon",
            "Take part in the spring finale — the Great Garden Verdict."),
        HouseDecor("finale_regatta", "regatta-pennant",
            "Regatta Pennant",
            "Take part in the summer finale — the Pond Days Regatta."),
        HouseDecor("finale_harvest", "harvest-plaque",
            "The Weigh-In Plaque",
            "Take part in the fall finale — the Harvest Weigh-In."),
        HouseDecor("finale_lantern", "lantern-glow",
            "Longest Night Lantern",
            "Take part in the winter finale — the Longest Night Lantern Walk."),
        // v0.7.0b: the secrets shelf — when all twelve are
        // found, the town fits on one hand-drawn page, and
        // the page goes up by the cork board.
        HouseDecor("secret_map", "secret-map",
            "The Town, Mapped",
            "Find every one of the town's secrets — the map draws itself."),
        // v0.7.0d: the bee trophy — a small gold cup that
        // goes up the first time you out-spell the class on
        // a Friday (School owns the record it derives from).
        HouseDecor("bee_trophy", "bee-trophy",
            "The Bee Trophy",
            "Win the Friday spelling bee at the Elementary."),
        // v0.8.0d: the yearbook shelf — the first published
        // volume puts the book by the cork board; the shelf
        // (and the Journal's page) notes it growing a spine
        // a year after that.
        HouseDecor("yearbook_shelf", "yearbook-shelf",
            "The Yearbook Shelf",
            "The Elementary prints your album every spring — " +
                "the first volume starts the shelf."),
        // v0.9.0c: the ribbon display — the county fair's
        // one derived house piece. The first blue ribbon
        // hangs it beside the yearbook shelf, where the
        // light (and every guest) can see it.
        HouseDecor("fair_ribbons", "fair-ribbons",
            "The Ribbon Display",
            "Take a blue ribbon at the county fair — it goes " +
                "up beside the yearbook shelf, where the " +
                "light's good."),
        // v0.10.0b: the hero's mask, retired to a hook by
        // the door — the caper's one derived house piece.
        // Closing the caper hangs it: proof, in felt and
        // elastic, that the town's nights have a keeper.
        HouseDecor("hero_mask", "hero-mask",
            "The Hero's Mask",
            "Close the caper and your mask retires to a hook " +
                "by the door — on display, because heroes " +
                "label their gear.")
    )

    fun decorById(id: String): HouseDecor? = DECOR.firstOrNull { it.id == id }

    /** The decor ids this save has earned — a pure derivation
     *  from records that already exist (episodes, feat sets, the
     *  mystery's permanent marks). Exactly one mystery piece can
     *  ever unlock: the branch the finale actually took. */
    fun decorFor(state: GameState): Set<String> {
        val out = mutableSetOf<String>()
        if (state.episodes.any { it.finished }) out += "episode_keepsake"
        if (state.photoFeats.isNotEmpty()) out += "photo_strip"
        if (state.gameFeats.isNotEmpty()) out += "game_trophy"
        if (state.arcRetired) {
            when (state.arcBranch) {
                "truth" -> out += "mystery_truth"
                "silent" -> out += "mystery_silent"
                "town" -> out += "mystery_town"
            }
        }
        // v0.4.0d: the garland goes up with the first holiday
        // beat ever played; the Full Calendar is the long haul.
        if (HolidayBeats.doneTotal(state) > 0) out += "holiday_garland"
        if (HolidayBeats.distinctDone(state) >= HOLIDAYS.size)
            out += "full_calendar"
        // v0.5.0h: a season's finale piece goes up the first
        // year you actually take part in that finale — the
        // derivation reads Finales' own played counts.
        for (def in Finales.DEFS) {
            if (Finales.playedCount(state, def) > 0) out += def.decorId
        }
        // v0.7.0b: the whole set of secrets, found, draws
        // the map — the derivation reads Secrets' own count
        // over the same life/lifeText records the finds wrote.
        if (Secrets.foundCount(state) >= Secrets.SECRETS.size) {
            out += "secret_map"
        }
        // v0.7.0d: one bee win, one gold cup on the wall —
        // the derivation reads School's own win counter.
        if (School.beeWon(state) >= 1) {
            out += "bee_trophy"
        }
        // v0.8.0d: one published yearbook, one shelf — the
        // derivation reads Yearbook's own volume list; the
        // shelf holds every volume, reduced or whole.
        if (Yearbook.volumes(state).isNotEmpty()) {
            out += "yearbook_shelf"
        }
        // v0.9.0c: one blue ribbon, one display — the
        // derivation reads Fair's own records; any blue
        // in any contest of any fair hangs it.
        if (Fair.blueRibbons(state) >= 1) {
            out += "fair_ribbons"
        }
        // v0.10.0b: the mask hangs when the caper closes —
        // derived from the caper's own record, like every
        // piece on this wall.
        if (Heroes.caperDone(state)) {
            out += "hero_mask"
        }
        return out
    }

    /** The InteriorArt tokens for [decorFor] — what the render
     *  bridge (TownInfo.houseDecor) carries into the room. */
    fun decorTokensFor(state: GameState): Set<String> {
        val ids = decorFor(state)
        return DECOR.filter { it.id in ids }.map { it.token }.toSet()
    }

    /** The trophy shelf: SHELF_SLOTS slots in COLLECTIBLES
     *  order; a slot holds the collectible's icon once found,
     *  and stays honest empty shelf space until then. */
    fun shelfSlots(state: GameState): List<String?> =
        COLLECTIBLES.take(SHELF_SLOTS).map {
            if (it.id in state.collected) it.icon else null
        }

    /** Feat trophies (photo feats + game feats) as shelf cups. */
    fun featCups(state: GameState): Int =
        minOf(MAX_CUPS, state.photoFeats.size + state.gameFeats.size)

    /** Cups that didn't fit, reported as a number instead. */
    fun featOverflow(state: GameState): Int =
        (state.photoFeats.size + state.gameFeats.size - MAX_CUPS)
            .coerceAtLeast(0)

    /** This morning's mail, a pure function of the day and who
     *  likes you: a note from a friend some days, junk some days,
     *  nothing at all the rest. Null = an empty box. */
    fun mailLine(state: GameState): String? {
        val friends = state.friendship.filter { it.value >= 40 }
            .keys.sorted()
        val roll = ((state.absDay * 31 + 7) % 10 + 10) % 10
        return when {
            friends.isNotEmpty() && roll in 0..2 ->
                "📬 A note in the mailbox from " +
                    "${friends[state.absDay % friends.size]}: \"Saw your " +
                    "light on last night. Town's better with you in it. " +
                    "Don't tell anyone I wrote this.\""
            roll == 5 ->
                "📬 Junk mail: one City Wok coupon (expired), a timeshare " +
                    "flyer for Fairplay, and a very official envelope " +
                    "containing absolutely nothing."
            else -> null
        }
    }

    /** The events appended to [after] that [before] never saw.
     *  The events list is append-only with front-trimming at its
     *  cap, so the new entries are whatever follows the longest
     *  surviving tail of [before] at the head of [after]. */
    fun newEvents(before: List<String>, after: List<String>): List<String> {
        var j = minOf(before.size, after.size)
        while (j > 0 &&
            before.subList(before.size - j, before.size) != after.subList(0, j)) {
            j--
        }
        return after.drop(j)
    }

    /** The morning report the bed shows: what the night did to
     *  the town while you were in it. [arcsBefore] snapshots each
     *  open arc's (stage, done) before the sleep; [chapterBefore]
     *  and [retiredBefore] do the same for the mystery. Every
     *  line is spoiler-safe — the case is reported, never told. */
    fun morningReport(state: GameState, fresh: List<String>,
                      arcsBefore: Map<String, Pair<Int, Boolean>>,
                      chapterBefore: Int, retiredBefore: Boolean,
                      mailed: String?): List<String> {
        val lines = mutableListOf<String>()
        lines += "🌅 ${state.gameMonth}/${state.gameDay} — you wake up in " +
            "your own bed. Best house in town. (Don't tell Tolkien.)"
        // v0.3.0g audit — no beat reported twice, by
        // construction: the arc section below OWNS the story
        // transitions, so the ticker versions of the same
        // beats (the rollover appends NEW STORY / stage
        // banners / STORY'S END to the events feed as well)
        // are filtered out of the overnight-events block, and
        // the whole report is distinct()ed at the end, so no
        // contributor — present or future — can repeat a line.
        val storyOwned = HashSet<String>()
        for (def in StoryArcs.ARCS) {
            storyOwned += "📖 NEW STORY: '${def.title}' — ${def.blurb}"
            storyOwned += "📖 STORY'S END: '${def.title}' — ${def.warm.title}."
            storyOwned += "📖 STORY'S END: '${def.title}' — ${def.solo.title}."
            for (st in def.stages) storyOwned += st.banner
        }
        fresh.filter { !it.startsWith("🌅") && it !in storyOwned }
            .distinct().take(6).forEach {
                lines += "· $it"
            }
        for (def in StoryArcs.ARCS) {
            val now = state.storyArcs[def.id]
            val was = arcsBefore[def.id]
            when {
                now == null -> Unit
                was == null ->
                    lines += "📖 A new story opened in town: \"${def.title}\"."
                now.done && !was.second ->
                    lines += "📖 \"${def.title}\" resolved overnight — " +
                        if (now.outcomeId.endsWith("_warm"))
                            "the warm ending. The town's heart grew a size."
                        else
                            "the town sorted it out its own way, eventually."
                !now.done && now.stage > was.first ->
                    lines += "📖 \"${def.title}\" turned a page overnight " +
                        "(stage ${now.stage + 1} of ${def.stages.size})."
            }
        }
        if (state.arcRetired && !retiredBefore) {
            lines += "🔍 The pond case is closed. Whatever the town " +
                "believes now, it believes it together."
        } else if (state.arcChapter > chapterBefore) {
            lines += "🔍 Word is the pond case moved while you slept — " +
                "chapter ${minOf(state.arcChapter, 5)} of 5 has aired."
        }
        // v0.3.0e: the sky you slept through gets its line too
        // — the rollover recorded last night's special against
        // this morning (SkyState.buzzKind reads the same
        // record the day's topics buzz about).
        if (SkyState.buzzKind(state) != null) {
            lines += when (state.lifeText[SkyState.LAST_SKY_KEY]) {
                SkyState.AURORA ->
                    "🌌 While you slept: the sky rippled green over " +
                        "town — an aurora. Half of South Park watched " +
                        "from their yards in pajamas."
                SkyState.METEOR ->
                    "☄️ While you slept: meteors tore the sky up over " +
                        "town. Wishes were made at the pond. The pond " +
                        "isn't telling."
                SkyState.HARVEST ->
                    "🌕 While you slept: a huge orange harvest moon " +
                        "hung over town all night. Everything looked " +
                        "dipped in pumpkin."
                else ->
                    "🌌 While you slept: the sky did something " +
                        "beautiful. Ask anyone — they'll tell you."
            }
        }
        // v0.4.0b: yesterday's good deeds get their beat —
        // the rollover kept the latest completion's objective
        // against the day it happened (life/lifeText, the
        // errand system's own record, no new keys here).
        if ((state.life["errandBeatDay"] ?: -999) == state.absDay - 1) {
            state.lifeText["errandBeat"]?.let { beat ->
                val n = state.life["errandBeatCount"] ?: 1
                lines += if (n > 1)
                    "📋 Yesterday: $n errands run for this town — the latest: " +
                        beat.replaceFirstChar { it.lowercase() } + "."
                else
                    "📋 Yesterday's good deed: " +
                        beat.replaceFirstChar { it.lowercase() } + "."
            }
        }
        // v0.6.0b: yesterday's favor-chain movement gets a
        // line of its own — a thread pulled, or a chain
        // closed for good (Errands.complete keeps the record
        // in life/lifeText, its own keys, nothing new here).
        if ((state.life["chainBeatDay"] ?: -999) == state.absDay - 1) {
            state.lifeText["chainBeat"]?.let { beat ->
                lines += "🧵 Word around town: " +
                    beat.replaceFirstChar { it.lowercase() } + "."
            }
        }
        // v0.8.0c: yesterday's emergency gets its line too —
        // a rescue the town saw, or a moment that passed and
        // the town coped (Emergencies keeps the record in
        // life/lifeText, its own keys, nothing new here).
        if ((state.life["emergBeatDay"] ?: -999) == state.absDay - 1) {
            state.lifeText["emergBeat"]?.let { beat ->
                lines += "🚨 Yesterday: $beat."
            }
        }
        // v0.5.0a: yesterday's road gets its line too — the
        // walk system keeps the latest road beat against the
        // day it happened (life/lifeText, its own record, no
        // new keys here). Beat shorts are written spoiler-free
        // by construction (weather, dogs, directions — never
        // anything a case would mind being overheard).
        if ((state.life["roadBeatDay"] ?: -999) == state.absDay - 1) {
            state.lifeText["roadBeat"]?.let { beat ->
                val n = state.life["roadBeatCount"] ?: 1
                lines += if (n > 1)
                    "🚶 On the road yesterday: $n small moments — the latest: " +
                        beat.replaceFirstChar { it.lowercase() } + "."
                else
                    "🚶 On the road yesterday: " +
                        beat.replaceFirstChar { it.lowercase() } + "."
            }
        }
        // v0.4.0d: a holiday that STARTS today gets one line
        // — its beat exists, it's playable in the window, and
        // HolidayBeats.morningNote returns it only on the
        // window's first morning, so it can't nag twice.
        HolidayBeats.morningNote(state)?.let { lines += it }
        // v0.5.0h: a season finale gets the same courtesy —
        // one line the morning it opens, one summons on its
        // closing morning, and nothing in between.
        Finales.morningNote(state)?.let { lines += it }
        // v0.6.0d: tonight's dinner invitation rides the
        // report too — it names the house, the window, and
        // the dish while there's still a day to plan around.
        Dinners.morningNote(state)?.let { lines += it }
        // v0.10.0a: and your own family's news — the arrival
        // morning, tonight's family table, allowance morning —
        // rides right under the invited tables.
        Family.morningNote(state)?.let { lines += it }
        // v0.11.0c: last night's sleepover gets its morning
        // line right under the family's — pancakes when
        // Mom's home to make them, the plain version when
        // she isn't.
        Sleepovers.morningNote(state)?.let { lines += it }
        // v0.12.0c: birthdays ride under the sleepovers —
        // last night's party result when it's fresh, or
        // tonight's party announced (bring a cake), or the
        // yield stated plainly when the evening's spoken
        // for.
        Birthdays.morningNote(state)?.let { lines += it }
        // v0.14.0d: and YOUR birthday rides under the
        // cast's — last night's party result when it's
        // fresh, or the birthday itself: the gift, the
        // card, and the evening stated plainly (the
        // party, the yield, or the household's day).
        PlayerBirthday.morningNote(state)?.let { lines += it }
        // v0.14.0e: and yesterday's clubhouse afternoon,
        // when one wrapped — the one line: the spot, who
        // came, the beats. (No phone text, no paper line
        // for the clubhouse, by design — a daily afternoon
        // is carried by this line and the Journal page.)
        Clubhouse.morningNote(state)?.let { lines += it }
        // v0.6.0f: the count, when it landed at this
        // morning's rollover — City Hall business rides
        // below the finales and the family tables by design.
        Votes.morningNote(state)?.let { lines += it }
        // v0.7.0b: yesterday's discovery gets one line — the
        // find is named (it's the player's own, and it's on
        // the Journal page besides); the unfound stay uncounted.
        Secrets.morningNote(state)?.let { lines += it }
        // v0.8.0b: yesterday's capstone gets its one line —
        // the title the town is now saying, and where its
        // trace stands (the player's own; the Journal holds
        // the whole page besides).
        Capstones.morningNote(state)?.let { lines += it }
        // v0.8.0e: yesterday's rivalry beat gets its line —
        // a result, the crowning, or the lock, in the beat's
        // own recorded words (Rival keeps the record; the
        // Journal's 🆚 page holds the whole board besides).
        Rival.morningNote(state)?.let { lines += it }
        // v0.7.0c: tonight's screening, announced while
        // there's still a day to plan around — the feature,
        // its stars, and the free door; or the dark reason.
        MovieNights.morningNote(state)?.let { lines += it }
        // v0.14.0c: the festival's mornings ride under
        // the screenings' — the poster's line on
        // announcement morning, tonight's entry on a
        // festival day, and the winner on the wrap's
        // morning (Festival owns the facts; this renders).
        Festival.morningNote(state)?.let { lines += it }
        // the gallery show's mornings (v0.15.0f): the
        // poster's first day, hanging day, and the wrap's
        // answer — the same derived-note discipline
        Gallery.morningNote(state)?.let { lines += it }
        // v0.7.0d: school days get their line — the day's
        // teacher and topic, the bee on Fridays, or the
        // quiet day's honest reason.
        School.morningNote(state)?.let { lines += it }
        mailed?.let { lines += it }
        if (lines.size == 1) {
            lines += "Quiet night. The town mostly behaved itself, " +
                "which is honestly suspicious."
        }
        return lines.distinct()
    }

    /** The knock, when someone is standing at your doorstep and
     *  you answer it (once per visitor per day, caller's job). */
    fun knockLine(name: String): String =
        "🚪 *knock knock* — $name was waiting at your door."
}

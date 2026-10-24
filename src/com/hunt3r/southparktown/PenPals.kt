package com.hunt3r.southparktown

import kotlin.random.Random

/**
 * PenPals — ✉️ PEN PALS (v0.13.0e, Queue #13 item 5): a
 * friend you've never met, in a town that doesn't exist
 * anywhere else.
 *
 * THE PAL IS NOT CAST — the parents' precedent exactly
 * (v0.10.0a): WREN HALLOWAY of PINEWOOD FLATS is a
 * VisitorPersona outside the 98, with her own authored
 * voice (a mini speech card + letter banks below), her own
 * authored SHEETS-style spec, and a warmth lane of her own
 * (life["palPts"], the famPts pattern) that NEVER touches
 * state.friendship — the census and the lane are pinned
 * from both sides in the suite.
 *
 * The program: the Elementary's pen-pal program. The signup
 * box sits by the classroom board every October 1–21 (the
 * program starts late — the box was in a closet; Wren's
 * first letter says so); late joiners wait for next year's
 * pairing, stated plainly. Letters ride the mail: Dad's
 * Post Office is the route (one authored line at signup),
 * arrivals land in the Your House mailbox and fold into the
 * morning report through the ticker.
 *
 * LETTERS IN are composed from (a) Wren's authored banks —
 * her town, her family, her weather — and (b) FACT SLOTS:
 * follow-ups on facts YOU actually sent her. Your replies
 * are composed in front of you from your real records
 * (latest photo, a closed case's PUBLIC shape — title +
 * closed, via the attic's boxes and nothing else — the
 * stand, the garden, the fair/carnival/dance/talent/parade
 * you were in), under one of three tones you pick; the sent
 * text is kept verbatim, so every fact Wren ever mentions
 * traces to a letter you can still read. A letter with no
 * fact to follow up on runs on Pinewood Flats news — it
 * never invents a fact about you.
 *
 * Cadence: a letter every 3–5 days (seeded) once paired.
 * Unanswered letters stretch the gap and draw kind notes;
 * three unanswered and the correspondence goes dormant —
 * Wren stops filling your mailbox until you write (one
 * revival letter reopens it). Warmth bands — stranger →
 * writing-regular → real friend — flavor her greetings and
 * sign-offs, and at the top band on May 31 she VISITS: four
 * days in June as a resident-class guest at your house and
 * the park, talk in her own voice, an album photo through
 * the ordinary camera, and a goodbye beat. Below the band,
 * the year ends with a "maybe next summer" letter.
 *
 * Records: the Journal's 📬 PEN PALS page keeps the pairing,
 * the standing, and the letters — the newest 16 in full,
 * older ones folded to one line apiece from the summaries
 * written when they were composed. The paper does NOT
 * cover the letters (private correspondence); the phone
 * gets nothing (letters are letters). No TalkGate rail;
 * the visit claims no agenda seat — presence only, on the
 * Family whereNow pattern ([blockAt] / [presentAt]).
 *
 * SEASON TWO (v0.15.0b, Queue #15 item 2) — THE VISIT,
 * RETURNED: the program alternates. The first top-band
 * settlement brings her to you; the NEXT top-band
 * settlement — she has visited, and no return trip stands
 * on the books — sends YOU to Pinewood Flats. Her letters
 * that season carry the plan (the JAR II fund, the route
 * committee), so the invitation is built over days, and
 * at the June 1 settlement the invitation itself lands
 * as a HOME letter (the polish #14 rule rides whole: it
 * can be answered any day, in any season). The trip
 * starts June 16 — fifteen days after the settlement,
 * because the program's travel days ARE the seam between
 * school years, stated in the letters rather than
 * shortened by fiat. Four days, four hosted beats, one
 * a day — the Turnaround (arrival), the Tour, Counter
 * Duty at the bait-and-gas, the Goodbye — hosted by Wren
 * in her own voice through her sheet machinery, anchored
 * at the mailbox, the correspondence's front door; the
 * town map is fixed and gains no Pinewood locations, and
 * the album's camera, which composes from where the
 * player actually stands in town, takes no Pinewood
 * photo (stated in the README). Each kept beat pays in
 * her lane only. The morning after the trip, her
 * thank-you rides home mail — the exact symmetry of
 * season one's. Below the band in a return season, the
 * maybe-next letter is reworded for a second year: the
 * jar stays on the counter, filling.
 */
object PenPals {

    // ================= identity (wholly invented) =================

    const val PAL = "Wren Halloway"
    const val PAL_FIRST = "Wren"
    const val PAL_TOWN = "Pinewood Flats"

    fun isPal(name: String): Boolean = name == PAL

    /** The franchise/real-world denylist over every authored
     *  surface of this module (the play/heroes discipline):
     *  no franchise material, no real places. The suite scans
     *  the banks AND every composed letter against it. */
    val DENYLIST: List<String> = listOf(
        "mysterion", "the coon", "toolshed", "human kite",
        "professor chaos", "captain diabetes", "call girl",
        "wonder tweek", "super craig", "mintberry",
        "colorado", "denver", "aspen", "vail", "boulder",
        "real world", "dear diary")

    /** Wren's mini speech card, in the cast cards' house
     *  shape (CHARACTER_SPEECH): how she writes, so the
     *  banks below stay in one voice. */
    val SPEECH_CARD: Map<String, String> = linkedMapOf(
        "Tics" to "Opens reports with \"Report:\". Rates " +
            "things out of five. Signs off with a weather " +
            "report whether or not weather happened.",
        "Habits" to "A P.S. in every letter, sometimes a " +
            "P.P.S. Numbered lists when nervous. Underlines " +
            "by writing the word twice.",
        "Never" to "Never brags, never asks for anything, " +
            "never signs \"love\". Feelings arrive disguised " +
            "as weather.",
        "Deflects" to "Big news gets stated flat and plain, " +
            "then immediately followed by something small.")

    /** Her authored SHEETS-style spec — a fan-construction
     *  kid spec (rust ponytail, pine-green top, plum pants,
     *  freckles), derived through the same KidArt geometry
     *  as any cast kid. NOT a cast slot: no sheet joins the
     *  98 in the goal's sheet set (stated in the README). */
    val SPEC: SheetSpec = SheetSpec(
        skin = "#f2c79b", hairStyle = "ponytail",
        hairColor = "#8a4b2a", hatId = "none",
        face = "normal", freckles = true,
        top = "#2e7a5f", collarHex = "#f2e3c9",
        bottom = "#54436b", shoeColor = "#33262b")

    /** The persona the visit stands in the world with:
     *  resident-class, the VisitorPersona pattern whole. */
    fun persona(state: GameState): VisitorPersona = VisitorPersona(
        loreName = PAL, spec = SPEC, band = AgeBand.KID,
        traits = listOf("dry", "observant", "loyal"),
        role = "your pen pal, visiting from $PAL_TOWN",
        bio = "Eleven years old, of $PAL_TOWN: one street, " +
            "one blinking light, one water tower with a fish " +
            "painted on it. Writes letters the way other " +
            "kids keep score.",
        facts = listOf(
            "$PAL_TOWN has the flattest hill in the county. " +
                "It has been measured twice. Still flat.",
            "Her mom runs the bait-and-gas: one counter, " +
                "two jobs, no bait questions before coffee.",
            "Her dad drives the school bus and the snowplow. " +
                "Same route either way.",
            "Her little brother Kip is seven and trades " +
                "teeth at a rate he sets himself."),
        resident = false, factionId = null,
        home = YOUR_HOUSE, workLoc = null, hangout = "KidsPark",
        greetingLines = VISIT_GREETINGS,
        factLines = VISIT_FACTS,
        deepLines = VISIT_DEEP,
        byeLines = VISIT_BYES,
        ambientLines = VISIT_AMBIENT,
        loves = setOf("music", "food", "art"))

    // ================= the dials =================

    const val SIGNUP_MONTH = 10          // October
    const val SIGNUP_FROM = 1
    const val SIGNUP_TO = 21
    const val GAP_MIN = 3                // a letter every
    const val GAP_SPAN = 3               // 3–5 days, seeded
    const val QUIET_AFTER = 2            // unanswered letters
    const val QUIET_STRETCH = 3          // that stretch the gap
    const val DORMANT_AT = 3             // unanswered → dormant
    const val PTS_PER_REPLY = 3
    const val PTS_REVIVAL = 1
    const val BAND_REGULAR = 15
    const val BAND_FRIEND = 45
    const val KEEP_FULL = 16             // newest full letters
    const val FOLDED_CAP = 12            // folded one-liners kept
    const val VISIT_MONTH = 6            // June
    const val VISIT_DAY = 10
    const val VISIT_DAYS = 4             // June 10–13
    const val REPLY_MINUTES = 15
    const val TALK_PTS_PER_DAY = 1       // visit days, first talk

    // season two: the return trip (v0.15.0b)
    const val TRIP_MONTH = 6             // June
    const val TRIP_DAY = 16              // settlement + 15:
                                         // the travel days
                                         // ARE the seam
    const val TRIP_DAYS = 4              // June 16–19
    const val TRIP_BEAT_PTS = 2          // per hosted beat

    // the trip's beats, one per day, in order
    const val BEAT_ARRIVE = "arrive"
    const val BEAT_TOUR = "tour"
    const val BEAT_COUNTER = "counter"
    const val BEAT_BYE = "bye"
    val TRIP_BEATS: List<String> = listOf(BEAT_ARRIVE,
        BEAT_TOUR, BEAT_COUNTER, BEAT_BYE)

    fun tripBeatLabel(beat: String): String = when (beat) {
        BEAT_ARRIVE -> "The Turnaround — arrival"
        BEAT_TOUR -> "The Tour — her town, on foot"
        BEAT_COUNTER -> "Counter Duty — the bait-and-gas"
        else -> "The Goodbye — dusk at the turnaround"
    }

    // tones (the player's pick at the mailbox)
    const val TONE_BIG = "bignews"
    const val TONE_EVERYDAY = "everyday"
    const val TONE_FUNNY = "funny"
    val TONES: List<String> = listOf(TONE_BIG, TONE_EVERYDAY, TONE_FUNNY)
    fun toneLabel(tone: String): String = when (tone) {
        TONE_BIG -> "🗞 Big news first"
        TONE_FUNNY -> "🤪 The funny version"
        else -> "🌤 Just the everyday"
    }

    // warmth bands
    const val BAND_STRANGER = 0
    const val BAND_REGULAR_I = 1
    const val BAND_FRIEND_I = 2

    // ================= state (pal… keys, additive) =================

    fun signedYear(state: GameState): Int = state.life["palYear"] ?: 0
    fun isSignedUp(state: GameState): Boolean = signedYear(state) > 0
    fun points(state: GameState): Int = state.life["palPts"] ?: 0

    fun adjustPoints(state: GameState, delta: Int) {
        state.life["palPts"] = (points(state) + delta)
            .coerceIn(-50, 100)
    }

    fun bandOf(state: GameState): Int = when {
        points(state) >= BAND_FRIEND -> BAND_FRIEND_I
        points(state) >= BAND_REGULAR -> BAND_REGULAR_I
        else -> BAND_STRANGER
    }

    fun bandWord(state: GameState): String = when (bandOf(state)) {
        BAND_FRIEND_I -> "a real friend"
        BAND_REGULAR_I -> "a writing-regular"
        else -> "a name on a school list"
    }

    fun lettersIn(state: GameState): Int = state.life["palIn"] ?: 0
    fun lettersOut(state: GameState): Int = state.life["palOut"] ?: 0
    fun unanswered(state: GameState): Int =
        state.life["palUnanswered"] ?: 0
    fun isDormant(state: GameState): Boolean =
        (state.life["palDormant"] ?: 0) == 1

    /** HOME MAIL (polish #14's disposition): the special
     *  letters — the settlement's word, the thank-you
     *  after the visit — arrive at your HOME mailbox,
     *  not the school box, so a reply to one rides home
     *  mail and never needed the program's year. This
     *  stamp stands while such a letter waits to be
     *  answered: set by [deliverSpecial], cleared by the
     *  send that answers it, and folded away by a fresh
     *  October signup (a new program year starts clean).
     *  The dormancy rules ride along whole — a home-mail
     *  reply sent to a dormant route IS the revival
     *  letter. Before this fix a thank-you landing after
     *  the June settlement could not be answered until
     *  October's box returned (polish #13 measured its
     *  soak closing 61 of 62 for exactly this reason). */
    private const val K_HOME_MAIL = "palHomeMail"

    fun homeMailPending(state: GameState): Boolean =
        (state.life[K_HOME_MAIL] ?: 0) == 1

    fun unread(state: GameState): Boolean =
        lettersIn(state) > (state.life["palReadUpTo"] ?: 0)

    fun markRead(state: GameState) {
        state.life["palReadUpTo"] = lettersIn(state)
    }

    fun latestLetter(state: GameState): String? =
        if (lettersIn(state) == 0) null
        else state.lifeText["palLetter:${lettersIn(state)}"]

    /** The correspondence is live while the pairing year is
     *  the school year the town is living, and the year has
     *  not been settled. */
    fun live(state: GameState): Boolean =
        isSignedUp(state) &&
            signedYear(state) == Yearbook.currentYear(state) &&
            (state.life["palSettledYear"] ?: 0) != signedYear(state)

    fun signupOpen(state: GameState): Boolean =
        state.gameMonth == SIGNUP_MONTH &&
            state.gameDay in SIGNUP_FROM..SIGNUP_TO &&
            signedYear(state) != Yearbook.currentYear(state)

    /** The program's signup, through the classroom board.
     *  Pairing is immediate — both schools ran the same box
     *  the same week — and the first letter is scheduled on
     *  the ordinary cadence. Re-joining a later year keeps
     *  the warmth and the same pal (she re-signs too). */
    fun signUp(state: GameState): String? {
        if (!signupOpen(state)) return null
        val year = Yearbook.currentYear(state)
        val returning = isSignedUp(state)
        state.life["palYear"] = year
        state.life["palDormant"] = 0
        state.life["palUnanswered"] = 0
        // a new program year folds any home letter still
        // waiting into the archive, unanswered — home
        // mail was last year's route, and it closes
        // clean when the box comes back out
        state.life.remove(K_HOME_MAIL)
        state.life["palNextDue"] = state.absDay +
            gapDays(state, lettersIn(state) + lettersOut(state))
        return if (returning)
            "✉️ Back in the box for ${Yearbook.labelOf(year)}. " +
                "The school in $PAL_TOWN ran the same box the " +
                "same week, and one name was already in it, " +
                "in handwriting you know: $PAL. Same pal. " +
                "Same stamp money. The correspondence " +
                "resumes.\n\nYour dad stamps the signup card " +
                "himself, down at the Post Office: “First " +
                "envelope's on the house, kiddo. Route rule. " +
                "I just made it up, but it's a rule now.”"
        else
            "✉️ Signed up for ${Yearbook.labelOf(year)}. " +
                "The pairing comes back from the box the same " +
                "afternoon — the school in $PAL_TOWN ran the " +
                "same box the same week. Your pen pal: " +
                "$PAL, eleven, of $PAL_TOWN (one street, one " +
                "blinking light, one water tower with a fish " +
                "on it; she will explain).\n\nYour dad stamps " +
                "the signup card himself, down at the Post " +
                "Office: “First envelope's on the house, " +
                "kiddo. Route rule. I just made it up, but " +
                "it's a rule now.”\n\nHer first letter rides " +
                "the mail from here — watch the mailbox."
    }

    /** The seeded 3–5 day gap after event [n]. Pure. */
    fun gapDays(state: GameState, n: Int): Int =
        GAP_MIN + Random("palgap:$n".hashCode()).nextInt(GAP_SPAN)

    // ================= facts (your real records) =================

    /** A fact about YOUR life, drawn from a real record —
     *  the only things your letters may say about you, and
     *  therefore the only things Wren's letters may ever
     *  follow up on. [id] is the provenance key: once a
     *  fact has ridden a sent letter it is marked used and
     *  never offered again (the garden re-offers weekly —
     *  gardens keep happening). */
    data class Fact(val id: String, val kind: String,
                    val text: String)

    private fun usedFacts(state: GameState): Set<String> =
        (state.lifeText["palUsedFacts"] ?: "")
            .split(",").filter { it.isNotBlank() }.toSet()

    fun markFactsUsed(state: GameState, facts: List<Fact>) {
        if (facts.isEmpty()) return
        val now = usedFacts(state) + facts.map { it.id }
        state.lifeText["palUsedFacts"] = now.joinToString(",")
    }

    /** Every fact your records currently offer, freshest
     *  first by a stated order. Pure reads over the town's
     *  own record-keepers — PhotoRecord, the attic's public
     *  case boxes (the ONLY case source: title + closed,
     *  never more), Stand, Garden, and the seasonal
     *  records' own participation fields. */
    fun factsAvailable(state: GameState): List<Fact> {
        val used = usedFacts(state)
        val out = mutableListOf<Fact>()
        // a case's public shape — the attic's boxes only
        Attic.caseBoxes(state).lastOrNull {
            "case:${it.number}" !in used }?.let { box ->
            out += Fact("case:${box.number}", "case",
                "the case of ${box.title} — it's closed now")
        }
        // your latest photo
        state.photos.lastOrNull {
            "photo:${it.id}" !in used }?.let { p ->
            val what = p.caption.ifBlank {
                "a photo at " + (LOCATIONS[p.locId]?.name ?: "town") }
            out += Fact("photo:${p.id}", "photo",
                "$what (${p.dateLabel})")
        }
        // the parade you were in
        for (y in listOf(state.gameYear, state.gameYear - 1)) {
            val rec = Parade.record(state, y)
            if (rec != null && rec.mode.isNotEmpty() &&
                "parade:$y" !in used) {
                out += Fact("parade:$y", "parade",
                    if (rec.mode == "march")
                        "the Founders' Day parade — you marched in it"
                    else "the Founders' Day parade — you watched " +
                        "the whole thing go by")
                break
            }
        }
        // the talent show
        Talent.currentRecord(state)?.let { rec ->
            if (rec.night == "played" &&
                "talent:${rec.year}" !in used)
                out += Fact("talent:${rec.year}", "talent",
                    if (rec.playerAct.isNotEmpty())
                        "the talent show — your act was " +
                            (Talent.playerAct(rec.playerAct)?.label ?: "your act")
                    else "the talent show — you were in the crowd")
        }
        // the dance
        Dance.currentRecord(state)?.let { rec ->
            if (rec.night == "played" &&
                "dance:${rec.year}" !in used)
                out += Fact("dance:${rec.year}", "dance",
                    if (rec.date.isNotEmpty())
                        "the Sweetheart Dance — you went with ${rec.date}"
                    else "the Sweetheart Dance — you went " +
                        "on your own and it was fine")
        }
        // the fair / the carnival
        for (y in listOf(state.gameYear, state.gameYear - 1)) {
            val rec = Fair.recordFor(state, y)
            if (rec != null && rec.attended && "fair:$y" !in used) {
                out += Fact("fair:$y", "fair",
                    "the county fair — you were there")
                break
            }
        }
        for (y in listOf(state.gameYear, state.gameYear - 1)) {
            val rec = Carnival.recordFor(state, y)
            if (rec != null && rec.attended &&
                "carnival:$y" !in used) {
                out += Fact("carnival:$y", "carnival",
                    "the winter carnival — you were there")
                break
            }
        }
        // the garden, this week
        val ripe = (0 until Garden.BEDS)
            .mapNotNull { Garden.bed(state, it) }
            .firstOrNull { it.ripe && !it.dead }
        if (ripe != null) {
            val gid = "garden:${state.absDay / 7}"
            if (gid !in used)
                out += Fact(gid, "garden",
                    "your garden — the " +
                        (Garden.cropById(ripe.cropId)?.name
                            ?: "crop") + " came ripe")
        }
        // the stand
        if (Stand.isOpen(state) && "stand:open" !in used)
            out += Fact("stand:open", "stand",
                "your stand — open for business")
        // the folks
        if (Family.arrived(state) && "family:move" !in used)
            out += Fact("family:move", "family",
                "your folks — they live with you here now")
        return out
    }

    // ================= Wren's banks (her voice) =================

    val GREETINGS: Map<Int, List<String>> = mapOf(
        BAND_STRANGER to listOf(
            "Dear New Kid (the school list says your name is " +
                "longer; the list can write its own letters),",
            "Dear Pen Pal (assigned),",
            "Dear Kid From The Other School List,"),
        BAND_REGULAR_I to listOf(
            "Dear New Kid,",
            "Dear Regular Correspondent,",
            "Dear New Kid (still the best mailbox on my route),"),
        BAND_FRIEND_I to listOf(
            "Dear New Kid,",
            "Dear Actual Friend (by post),",
            "Dear New Kid — my favorite stamp expenditure,"))

    val SIGNOFFS: Map<Int, List<String>> = mapOf(
        BAND_STRANGER to listOf(
            "Report back if you want. — Wren",
            "That's the news from here. — Wren"),
        BAND_REGULAR_I to listOf(
            "Write when you can. — Wren",
            "Your turn. The mailbox here is rooting for you. — Wren"),
        BAND_FRIEND_I to listOf(
            "Write soon. Or don't; I'll assume weather. — Wren",
            "Yours by post, — Wren"))

    /** Pinewood Flats news: (short title for the folded
     *  summary, the lines that ride the letter). */
    val NEWS: List<Pair<String, String>> = listOf(
        "the fish" to "Report: the water tower fish got " +
            "repainted. He is orange now. He was orange " +
            "before, but now he is orange on purpose.",
        "the hill" to "The county came out and measured " +
            "Mount Flatitude again, in case it had grown. " +
            "It has not. Zero feet of growth. The sign has " +
            "been updated to say so.",
        "the light" to "Our traffic light blinked yellow " +
            "for the 400th day in a row. There was a small " +
            "ceremony. Kip brought a flag he made. The flag " +
            "also blinks, because he waves it the whole time.",
        "lake season" to "Lake season started early this " +
            "year, which is what we call it when the Flats " +
            "flood. The ducks are thrilled. The ducks are " +
            "always thrilled. Four out of five, if you like " +
            "ducks.",
        "the bus" to "Dad drove the bus route in the plow " +
            "truck by mistake on Tuesday, because the keys " +
            "look the same in the dark. Nobody complained. " +
            "The plow truck has better heat.",
        "the tooth market" to "Kip has cornered the tooth " +
            "market. He pays himself a quarter per tooth " +
            "and reports the earnings at dinner like a " +
            "weather report. Mostly his own teeth. Business " +
            "is slowing down for obvious reasons.",
        "the library fish" to "The library got a fish tank. " +
            "The fish has a library card now. It was Kip's " +
            "idea and the librarian said yes before he " +
            "finished asking, which tells you everything " +
            "about our library.",
        "the counter" to "Mom has put a bell on the counter " +
            "at the bait-and-gas. Ring for bait, ring twice " +
            "for gas, ring three times if you are Kip and " +
            "want attention. Kip rings three times.",
        "the rain gauge" to "I keep the rain gauge on our " +
            "porch. This week's reading: some. The official " +
            "record says some too. Science agrees with me.",
        "the turnaround" to "They repaved the bus turnaround, " +
            "which is also Main Street's far end and the " +
            "whole parade route. The parade this year will " +
            "be the smoothest four minutes on record.")

    /** Follow-ups, per fact kind — the {} slot takes the
     *  fact text from YOUR sent letter, verbatim-shaped.
     *  This is the only door through which facts about you
     *  enter her letters. */
    val FOLLOWUPS: Map<String, List<String>> = mapOf(
        "photo" to listOf(
            "You wrote about {}. Did it come out the way " +
                "you saw it? Rate it out of five. I rate my " +
                "last drawing of the water tower a three. " +
                "The fish looks worried.",
            "Still thinking about {}. Keep it somewhere " +
                "safe. Paper lasts if you let it."),
        "case" to listOf(
            "You buried the headline, by the way: {}. " +
                "Closed is closed. Our town paper would " +
                "print that above the fold, if we had a fold. " +
                "We have a sheet.",
            "{}. Around here the biggest case is whose dog " +
                "is in whose yard. Yours sounds heavier. " +
                "Glad it's shut."),
        "stand" to listOf(
            "Report received on {}. A whole business. What " +
                "sells best? Asking for my savings plan, " +
                "which is currently a jar that says JAR.",
            "{} — congratulations. The bait-and-gas has " +
                "been open forty years and Mom still calls " +
                "every spring day one a grand opening."),
        "garden" to listOf(
            "You wrote {}. Ours is mud with ambitions. " +
                "The ambitions are also mud. Rate your soil " +
                "out of five.",
            "{} — noted in the Flats almanac, which is me, " +
                "writing it down."),
        "fair" to listOf(
            "You went to {}. Did you win anything? The " +
                "Flats fair is a card table at the turnaround. " +
                "I have won the card table twice.",
            "{} — a real fair. With rides that go somewhere? " +
                "Ours go around, which is honest work too."),
        "carnival" to listOf(
            "A winter carnival — {}. We have ice too, but " +
                "nobody organizes ours. It just shows up and " +
                "so do we.",
            "{} sounds cold in the good way. Rate the cold " +
                "out of five. Ours was a four."),
        "dance" to listOf(
            "You mentioned {}. A whole dance, at a school. " +
                "Our school dance is the bus turnaround with " +
                "a speaker. You win this round.",
            "Report noted: {}. Was the music good? Music is " +
                "half a dance. The other half is standing " +
                "near it correctly."),
        "talent" to listOf(
            "You wrote about {}. A talent show. My talent " +
                "is this: letters. Kip's is the tooth market. " +
                "We would not place, but we'd be remembered.",
            "{} — did the crowd stay awake? Staying awake " +
                "is the highest honor a Flats audience gives."),
        "parade" to listOf(
            "A parade — {}. Ours is four minutes and " +
                "includes the bus doing the route twice so " +
                "it feels longer. Yours sounds official.",
            "You wrote {}. Candy report requested: how " +
                "much, and did any of it survive the day?"),
        "family" to listOf(
            "You said {}. Family in the same house is a " +
                "whole weather system. Ours forecasts loud " +
                "with a chance of pancakes.",
            "{} — noted. Tell them the Flats say hi. The " +
                "Flats are me and Kip. Kip says hi twice."))

    /** Rotating questions she asks; your reply answers the
     *  one from her latest letter (answer bank below). */
    val ASKS: List<String> = listOf(
        "Your turn. Question from the Flats: what's the sky " +
            "doing over your town this week?",
        "Your turn. Survey: what did you eat today that " +
            "you'd eat again? The Flats are compiling data.",
        "Your turn. If your town had a water tower, what " +
            "would be painted on it? Choose wisely. It's " +
            "up there a while.")

    val ASK_ANSWERS: Map<String, List<String>> = mapOf(
        TONE_BIG to listOf(
            "Headline answer to your survey: the sky here is " +
                "doing its job. Full report at eleven.",
            "Breaking: lunch was eaten and would be eaten " +
                "again. Sources confirm.",
            "Tower report: if we had one, something honest " +
                "would go on it. A fish has seniority, " +
                "apparently."),
        TONE_EVERYDAY to listOf(
            "To your question: the sky here did a bit of " +
                "everything this week. Normal sky.",
            "Answering your survey: yes, I'd eat it again. " +
                "Most days are like that here.",
            "If we had a tower, I'd vote for something " +
                "plain that makes people nod."),
        TONE_FUNNY to listOf(
            "Answer to your sky question: it's still up " +
                "there. I checked twice. Professional work.",
            "Survey answer: I ate it, I'd eat it again, and " +
                "I'd deny everything if asked.",
            "Tower answer: a picture of the tower, so it's " +
                "accurate from every angle."))

    /** The kind notes when letters go unanswered. */
    val QUIET_NOTES: List<String> = listOf(
        "No reply needed to the last one, by the way. Mail " +
            "is weather. It comes when it comes.",
        "The mailbox here is patient. It has out-waited " +
            "the light, and the light has blinked for 400 " +
            "days.")

    val DORMANT_NOTE: String =
        "This is the third letter in the pile, so here's " +
            "the deal, flat and plain: I'll stop filling " +
            "your mailbox. No hard feelings — mail is " +
            "weather. When you write, I'll write. The route " +
            "stays open."

    val PS_BANK: List<String> = listOf(
        "P.S. The fish says nothing. He is a tower fish.",
        "P.S. Kip says hi. He says it at full volume, so " +
            "consider this letter loud at the end.",
        "P.S. Rate this letter out of five when you write. " +
            "Be honest. I can take a three.",
        "P.S. If this letter is damp, that's lake season, " +
            "not feelings.",
        "P.P.S. The stamp had a boat on it. You'd have " +
            "liked the stamp.")

    /** The first letter — fully authored, all introduction,
     *  no facts about you (she knows none yet). */
    val FIRST_LETTER: String =
        "Dear New Kid (the school list says your name is " +
            "longer; the list can write its own letters),\n\n" +
            "Report: you have been assigned a pen pal. It is " +
            "me. Wren Halloway, eleven, of Pinewood Flats — " +
            "one street, one blinking light, one water tower " +
            "with a fish painted on it. Our school started " +
            "this program in September. Yours started in " +
            "October, apparently. The box was in a closet. " +
            "These things happen.\n\n" +
            "Facts about here, so we're even: the Flats flood " +
            "every spring and we call it lake season. Our " +
            "hill is the flattest in the county. It has been " +
            "measured twice. My mom runs the bait-and-gas " +
            "(one counter, two jobs). My dad drives the bus " +
            "and the plow. My brother Kip is seven and trades " +
            "teeth.\n\n" +
            "Facts about you: none yet. The list only had " +
            "your town. So: your turn. Write back and tell " +
            "me one true thing about your town, and I'll " +
            "tell you if ours can beat it. (It can't. Ours " +
            "is flat. But I'll write it down like it could.)\n\n" +
            "Report back if you want. — Wren\n\n" +
            "P.S. The fish says nothing. He is a tower fish."

    /** Below the band at year's end: the honest letter. */
    val MAYBE_NEXT_LETTER: String =
        "Dear New Kid,\n\n" +
            "Report: the school year is done, the boxes are " +
            "packed, and the summer question has an answer. " +
            "The visit isn't this summer. That's the flat " +
            "fact of it — the program saves the trip for " +
            "the pairs who wrote their way all the way in, " +
            "and we wrote most of the way, which is not the " +
            "same thing.\n\n" +
            "No hard feelings on this end. Mail is weather, " +
            "and ours was mostly good weather. The box comes " +
            "back out in October. If your name goes in " +
            "again, mine will already be in it. Maybe next " +
            "summer.\n\n" +
            "Yours by post, — Wren\n\n" +
            "P.S. The fish got a summer coat of paint. He " +
            "remains orange. Some things hold."

    /** The morning after the visit: she writes from home. */
    val THANKS_LETTER: String =
        "Dear New Kid,\n\n" +
            "Report: home. The bus got in after dark and " +
            "Kip was asleep against the window of the " +
            "turnaround like a small customs officer. Mom " +
            "left the counter bell for me to ring once. I " +
            "rang it once.\n\n" +
            "Your town is not flat. I keep saying it like " +
            "news. Your park, your street, the hill that is " +
            "an actual hill — I rated the whole visit a " +
            "five and I do not give fives. Keep the photo " +
            "somewhere safe. Paper lasts if you let it.\n\n" +
            "The box comes out again in October. You know " +
            "where my name will be.\n\n" +
            "Yours by post, — Wren\n\n" +
            "P.S. The fish looks smaller from here now. " +
            "Don't tell him."

    // ================= your banks (reply frames) =================

    val REPLY_OPENERS: Map<String, List<String>> = mapOf(
        TONE_BIG to listOf(
            "Dear Wren,\n\nSTOP THE PRESSES (theirs, ours, " +
                "anybody's):",
            "Dear Wren,\n\nBig week here. In order of size:"),
        TONE_EVERYDAY to listOf(
            "Dear Wren,\n\nNot a huge week here, which is " +
                "its own kind of news:",
            "Dear Wren,\n\nReport from the everyday:"),
        TONE_FUNNY to listOf(
            "Dear Wren,\n\nYou are not ready for this letter. " +
                "Nobody was. Anyway:",
            "Dear Wren,\n\nHold onto your stamp. Here's the " +
                "week:"))

    val REPLY_FACT_LINE: Map<String, String> = mapOf(
        TONE_BIG to "Headline: {}.",
        TONE_EVERYDAY to "Also: {}.",
        TONE_FUNNY to "Incredible update: {}. (Hold for applause.)")

    val REPLY_NO_FACT: Map<String, List<String>> = mapOf(
        TONE_BIG to listOf(
            "No headlines this week. The town held still " +
                "and looked innocent."),
        TONE_EVERYDAY to listOf(
            "Nothing huge to report. The ordinary days are " +
                "most of them."),
        TONE_FUNNY to listOf(
            "News: none. I investigated nothing and found " +
                "it everywhere."))

    val REPLY_CLOSERS: Map<String, List<String>> = mapOf(
        TONE_BIG to listOf(
            "Full story as it develops. — the New Kid",
            "More when there's more. There's usually more. " +
                "— the New Kid"),
        TONE_EVERYDAY to listOf(
            "That's the week. — the New Kid",
            "Write when you can. — the New Kid"),
        TONE_FUNNY to listOf(
            "I remain, dramatically, — the New Kid",
            "Send a cooler town next time. Kidding. Mostly. " +
                "— the New Kid"))

    val REPLY_PS: Map<String, List<String>> = mapOf(
        TONE_BIG to listOf(
            "P.S. This letter is officially a five. I checked " +
                "with myself."),
        TONE_EVERYDAY to listOf(
            "P.S. The stamp here had a mountain on it. " +
                "Seemed fair to send one back."),
        TONE_FUNNY to listOf(
            "P.S. I rated your last letter a five. Don't " +
                "let it go to your tower."))

    // ================= the visit banks =================

    val VISIT_GREETINGS: List<String> = listOf(
        "You look exactly like your handwriting. Dry. " +
            "Reliable. Slightly stamp-flavored.",
        "So this is the famous mailbox. It's smaller in " +
            "person. Everything is. Except your hill.",
        "Report: arrived. The Flats are flat and I am " +
            "not there. Four days. Spend them wisely.")

    val VISIT_TALK: List<String> = listOf(
        "Your street has a whole name and everything. " +
            "Ours is just Street. Capital S. It answers to it.",
        "I keep looking for the flat part. There isn't " +
            "one. Your town is showing off.",
        "Back home the evening report is: some weather, " +
            "some ducks. Here it's an event schedule.",
        "Kip made me promise to bring back one (1) " +
            "souvenir and one (1) story. The story is winning.",
        "Mom says the counter basically runs itself for " +
            "four days. This is the nicest lie she's told " +
            "all year.",
        "If anyone asks, the Flats sent their best. If " +
            "nobody asks, same.",
        "I rated your town's sky a four. It loses one " +
            "point for effort. Ours doesn't try at all.",
        "Letters are better, mostly. But this — this is " +
            "a strong second place.")

    val VISIT_FACTS: List<String> = listOf(
        "The fish on the tower was painted by a man named " +
            "Gus who only paints fish. One fish, yearly.",
        "Mount Flatitude has a sign, a bench, and a view " +
            "of the turnaround. Tourists: two, ever.",
        "When the Flats flood, school moves to the library " +
            "upstairs and the fish tank judges us.",
        "The light has blinked since before I was born. " +
            "It's not broken. It's committed.")

    val VISIT_DEEP: List<String> = listOf(
        "Writing to somebody is easy. You fold the " +
            "feelings up small. Standing here is the " +
            "unfolded version. It's fine. It's good, even.",
        "Most people in the Flats I'll know forever. " +
            "You're the first person I picked from a whole " +
            "box. Chosen beats assigned.")

    val VISIT_BYES: List<String> = listOf(
        "Back to the mailbox with you. It's a good system. " +
            "We made it one.",
        "If you write, I'll write. That's the whole " +
            "constitution of the Flats–Here Alliance.")

    val VISIT_AMBIENT: List<String> = listOf(
        "Rates things here out of five, quietly.",
        "Looking for the flattest spot in town, out of " +
            "professional respect.",
        "Taking notes for the Flats almanac. It is still " +
            "just her, writing things down.",
        "Watching the sky do its job.")

    val GOODBYE_LINE: String =
        "Last day, so the flat fact first: this was a " +
            "five. I don't give fives, so keep it somewhere " +
            "safe, same as a photo. The bus does the route " +
            "at dusk. I'll write from home — mail is " +
            "weather, and ours has been clear all week. " +
            "You take care of this town, New Kid. I'll take " +
            "care of the flat one."

    // ================= season two: the return trip =================

    /** The plan, seeded through a return season's ordinary
     *  letters — one line at a time, over days, so the
     *  invitation is built before it lands. */
    val PLAN_LINES: List<String> = listOf(
        "Second-year business, stated early so it can " +
            "be ignored properly: the program alternates. " +
            "Year one, the pal rides the long road. Year " +
            "two, it reverses. If this year ends like " +
            "last year did, the backwards-facing seat on " +
            "the bus has your name on it. Save snack money.",
        "The Flats chapter has opened the Return Trip " +
            "Fund. It is a jar. It says JAR II. Inside " +
            "it: bus fare, and a note that reads THE " +
            "OTHER DIRECTION THIS TIME.",
        "Report from the route committee (me): the road " +
            "between the last bell and my front step is " +
            "fifteen days by the program's count. Travel " +
            "days, they call them. If the writing holds, " +
            "this year they're yours to spend.")

    /** The settlement's word in a return season, at the
     *  band: an invitation, landed as home mail. */
    val INVITE_LETTER: String =
        "Dear New Kid,\n\n" +
            "Report: the school year is done, the board " +
            "is read, and the summer question has a new " +
            "answer. This year the trip runs the other " +
            "way. YOU come HERE.\n\n" +
            "The program alternates — nobody told us in " +
            "year one, because in year one nobody had " +
            "seniority. The pal with the longer route " +
            "rides first; the return trip is saved for a " +
            "pair that writes its way in twice. We wrote " +
            "our way in twice.\n\n" +
            "So: June 16, the bus does the long road. " +
            "Fifteen days from the last bell to my front " +
            "step — the program calls them travel days, " +
            "and that is exactly what they are: two " +
            "buses, one transfer, and the flattest road " +
            "in the county unspooling the whole way. The " +
            "road is the seam between one school year " +
            "and the next. Last year I crossed it. This " +
            "year you do.\n\n" +
            "I'll be at the turnaround. I'll be the one " +
            "holding the flag. Kip insists a flag is " +
            "required. His flag blinks.\n\n" +
            "Yours by post, — Wren\n\n" +
            "P.S. Pack for weather rated 'some'. It is " +
            "the Flats' whole forecast and it has never " +
            "once been wrong."

    /** Below the band in a return season: the honest
     *  letter, reworded for a second year. */
    val MAYBE_NEXT2_LETTER: String =
        "Dear New Kid,\n\n" +
            "Report: the school year is done, and the " +
            "summer question has last year's shape after " +
            "all. The return trip is not this summer. " +
            "The fund is a jar that says JAR II, and the " +
            "jar is not full, and the program is strict " +
            "about jars.\n\n" +
            "The flat fact: the trip saves itself for " +
            "the pairs who wrote their way all the way " +
            "in — twice. We wrote most of the way. Most " +
            "is not twice.\n\n" +
            "No hard feelings. Mail is weather, and ours " +
            "was mostly good weather. The box comes back " +
            "out in October, and the jar stays on the " +
            "counter, filling. If your name goes in " +
            "again, mine will already be in it.\n\n" +
            "Yours by post, — Wren\n\n" +
            "P.S. Kip asked if the bus could come " +
            "anyway, empty, for the ceremony of it. The " +
            "bus respects the jar. We all respect the jar."

    /** The morning after your trip: she writes from home,
     *  the mirror of the letter she sent after hers. */
    val TRIP_THANKS_LETTER: String =
        "Dear New Kid,\n\n" +
            "Report: you, home again. The bus did the " +
            "route at dusk and the road unspooled " +
            "backwards — transfer, flat, flatter, then " +
            "your hills coming up like a rumor.\n\n" +
            "You saw the whole operation from the inside. " +
            "The tower. The hill that isn't. The counter, " +
            "worked by professional hands (yours, " +
            "briefly). I have rated the visit, returned, " +
            "a five. I do not give fives. It is becoming " +
            "a habit, and I blame the correspondence.\n\n" +
            "The box comes out again in October. You " +
            "know where my name will be. The jar on the " +
            "counter says JAR II still. Some funds you " +
            "keep.\n\n" +
            "Yours by post, — Wren\n\n" +
            "P.S. Kip says the flag is yours to keep. It " +
            "blinks. He made it blink for you specifically."

    /** The journey itself, narrated on the arrival beat —
     *  the seam, crossed. */
    val JOURNEY_LINE: String =
        "Two buses and one transfer. The hills give up " +
            "one at a time; the road stops apologizing " +
            "and goes flat, flatter, flattest. Fifteen " +
            "days the program counted for this — the seam " +
            "between school years — and you're the one " +
            "crossing it. Then: a turnaround, a blinking " +
            "light, and a flag that blinks back."

    /** The hosted beats' scripts, in her host voice. */
    val TRIP_SCRIPTS: Map<String, List<String>> = mapOf(
        BEAT_ARRIVE to listOf(
            "Report: landed. You look exactly like your " +
                "spelling. Neat. Slightly stamp-flavored.",
            "That's Kip. The flag is load-bearing; do " +
                "not question the flag. Behind him: the " +
                "tower, with the fish. The light, blinking. " +
                "My house is at the end of Street — capital " +
                "S, it answers to it. This way.",
            "Four days. I have a plan, and the plan has " +
                "a plan. Today is only arriving. Tomorrow " +
                "I narrate and you rate. Those are the " +
                "tour rules and they are final."),
        BEAT_TOUR to listOf(
            "Tour rules, as stated: I narrate, you rate. " +
                "Everything is out of five, including the " +
                "tour. Disputes are settled by me.",
            "Street — the whole of it. Near end: the " +
                "bait-and-gas, Mom's counter, the bell. " +
                "Far end: the library, where the fish has " +
                "a card and seniority. Between them: " +
                "everywhere else. There is no other between.",
            "And this— (she gestures at a view of the " +
                "turnaround) —is Mount Flatitude. Measured " +
                "twice. The bench is for sitting with the " +
                "achievement. Tourists: three, ever, " +
                "counting you. You are the record, New Kid."),
        BEAT_COUNTER to listOf(
            "Counter duty, Saturday rules: ring once for " +
                "bait, twice for gas, three times if " +
                "you're Kip and want attention. You're on " +
                "the bell and the refills. I supervise. " +
                "Mom supervises me. It is supervision all " +
                "the way up.",
            "That rush was a four. You were a four and " +
                "a half. The extra half is for not ringing " +
                "three times when Kip came in, which " +
                "showed restraint beyond your years."),
        BEAT_BYE to listOf(
            "Last day, so the flat fact first: the " +
                "visit, returned, was a five. The jar is " +
                "empty and the road is full. That is the " +
                "correct arrangement of jar and road.",
            "The bus does the route at dusk. Write when " +
                "you're home — no. Write because you're " +
                "home. That's article one of the alliance, " +
                "and the alliance holds."))

    /** Her talk pool as host — the visit's pool, answered
     *  from the other side of the counter. */
    val HOST_TALK: List<String> = listOf(
        "Everything here is exactly as described in " +
            "the letters. I checked before you came. It " +
            "would have been embarrassing otherwise.",
        "That's the rain gauge. This week's reading: " +
            "some. I have logged your visit under " +
            "notable weather.",
        "Kip has questions for you at dinner. He " +
            "prepared them in writing. There are nine. " +
            "Eight are about teeth.",
        "Mom says a guest who works the counter counts " +
            "as family by closing time. Policy. She just " +
            "made it up, but it's a policy now.",
        "I keep looking at you standing in actual " +
            "space. Letters fold a person small. You're " +
            "taller unfolded.",
        "The Flats are showing off for you. The light " +
            "blinked on schedule and everything.")

    val HOST_FACTS: List<String> = listOf(
        "The turnaround is also the parade route, the " +
            "dance floor, and the emergency muster point. " +
            "One patch of pavement, four jobs.",
        "Gus paints the tower fish once a year whether " +
            "it needs it or not. It always needs it. " +
            "He says.",
        "When lake season floods the Flats, school " +
            "moves upstairs at the library and the fish " +
            "judges us from the tank. It has tenure.",
        "The counter bell is older than me. It has " +
            "rung for bait, for gas, and for Kip, in " +
            "that order of frequency, reversed.")

    // ================= the archive =================
    // One meta record per letter, written when the letter
    // is composed — the summary and the fact provenance
    // live here, so a folded letter still testifies to what
    // it carried. Full texts ride palLetter:<n> / palSent:<n>
    // until the fold takes them.

    private const val FS = "\u0001"   // meta field separator
    private const val IS = "\u0002"   // meta inner separator

    data class Incoming(val text: String, val summary: String,
                        val fuKind: String, val fuText: String,
                        val askIdx: Int)

    private fun metaRecords(state: GameState): List<List<String>> =
        (state.lifeText["palMeta"] ?: "").split("\n")
            .filter { it.isNotBlank() }
            .map { it.split(FS) }

    private fun appendMeta(state: GameState, fields: List<String>) {
        val cur = state.lifeText["palMeta"] ?: ""
        state.lifeText["palMeta"] =
            (if (cur.isBlank()) "" else cur + "\n") +
                fields.joinToString(FS)
    }

    private fun dateLabelOf(dayKey: String): String {
        if (dayKey.length < 8) return dayKey
        val m = dayKey.substring(4, 6).toIntOrNull() ?: return dayKey
        val d = dayKey.substring(6, 8).toIntOrNull() ?: return dayKey
        return "$m/$d"
    }

    private fun storeIncoming(state: GameState, inc: Incoming,
                              counts: Boolean) {
        val n = lettersIn(state) + 1
        state.life["palIn"] = n
        state.lifeText["palLetter:$n"] = inc.text
        if (counts) state.life["palUnanswered"] =
            unanswered(state) + 1
        appendMeta(state, listOf("i$n", state.todayKey, inc.summary,
            inc.fuKind, inc.fuText, inc.askIdx.toString()))
        foldIfNeeded(state)
    }

    private fun storeOutgoing(state: GameState, text: String,
                              tone: String, facts: List<Fact>) {
        val n = lettersOut(state) + 1
        state.life["palOut"] = n
        state.lifeText["palSent:$n"] = text
        val summary = "You wrote (" + when (tone) {
            TONE_BIG -> "big news"
            TONE_FUNNY -> "the funny version"
            else -> "everyday" } + "): " +
            (facts.firstOrNull()?.text ?: "the everyday, plain")
        appendMeta(state, listOf("o$n", state.todayKey, summary,
            tone, facts.joinToString("+") { it.id },
            facts.joinToString(IS) { it.text }))
        foldIfNeeded(state)
    }

    private fun fullTextCount(state: GameState): Int =
        (1..lettersIn(state)).count {
            state.lifeText["palLetter:$it"] != null } +
            (1..lettersOut(state)).count {
                state.lifeText["palSent:$it"] != null }

    fun foldedLines(state: GameState): List<String> =
        (state.lifeText["palFolded"] ?: "").split("\n")
            .filter { it.isNotBlank() }

    /** The cap, enforced on write only (reads stay pure):
     *  past [KEEP_FULL] full letters, the oldest folds to
     *  the one-line summary its meta record has carried
     *  since the day it was composed. */
    private fun foldIfNeeded(state: GameState) {
        while (fullTextCount(state) > KEEP_FULL) {
            val rec = metaRecords(state).firstOrNull { r ->
                val key = if (r[0].startsWith("i"))
                    "palLetter:${r[0].drop(1)}"
                else "palSent:${r[0].drop(1)}"
                state.lifeText[key] != null } ?: return
            val key = if (rec[0].startsWith("i"))
                "palLetter:${rec[0].drop(1)}"
            else "palSent:${rec[0].drop(1)}"
            val line = "${dateLabelOf(rec[1])} — ${rec[2]}"
            val folded = foldedLines(state) + line
            val kept = if (folded.size > FOLDED_CAP) {
                state.life["palFoldedDropped"] =
                    (state.life["palFoldedDropped"] ?: 0) +
                        (folded.size - FOLDED_CAP)
                folded.takeLast(FOLDED_CAP)
            } else folded
            state.lifeText["palFolded"] = kept.joinToString("\n")
            state.lifeText.remove(key)
        }
    }

    private fun lastSentFacts(state: GameState): List<Pair<String, String>> {
        val rec = metaRecords(state).lastOrNull {
            it[0].startsWith("o") } ?: return emptyList()
        if (rec.size < 6 || rec[4].isBlank()) return emptyList()
        val ids = rec[4].split("+")
        val texts = rec[5].split(IS)
        return ids.mapIndexed { i, id ->
            (id.substringBefore(":") to (texts.getOrNull(i) ?: ""))
        }.filter { it.second.isNotBlank() }
    }

    private fun lastAskIdx(state: GameState): Int =
        metaRecords(state).lastOrNull { it[0].startsWith("i") }
            ?.getOrNull(5)?.toIntOrNull() ?: -1

    // ================= composition =================

    /** Her next letter, composed whole from the banks and
     *  the follow-up slot. The follow-up is the ONLY place
     *  a fact about you can appear, and it is filled from
     *  your last sent letter's own meta record — provenance
     *  by construction. With no sent fact to follow, the
     *  letter runs on Pinewood Flats news alone. */
    fun composeIncoming(state: GameState): Incoming {
        val n = lettersIn(state) + 1
        if (lettersIn(state) == 0) return Incoming(FIRST_LETTER,
            "Wren's first letter — Pinewood Flats, introduced",
            "", "", 0)
        val band = bandOf(state)
        val greet = GREETINGS.getValue(band)[
            Random("palgreet:$n".hashCode()).nextInt(
                GREETINGS.getValue(band).size)]
        val news = NEWS[(7 * n + 2) % NEWS.size]
        val parts = mutableListOf(greet, "", news.second)
        var summary = "Wren: ${news.first}"
        var fuKind = ""; var fuText = ""
        if ((state.life["palRevived"] ?: 0) == 1)
            parts += "Your letter got here. The route lives. " +
                "Report follows as usual."
        if (tripPending(state))
            parts += PLAN_LINES[(n - 1) % PLAN_LINES.size]
        val sent = lastSentFacts(state)
        if (sent.isNotEmpty()) {
            val (kind, text) = sent[(n - 1) % sent.size]
            val bank = FOLLOWUPS[kind]
            if (bank != null) {
                parts += bank[Random("palfu:$n".hashCode())
                    .nextInt(bank.size)].replace("{}", text)
                fuKind = kind; fuText = text
                summary = "Wren asked about $text"
            }
        }
        val un = unanswered(state) + 1
        when {
            un >= DORMANT_AT -> parts += DORMANT_NOTE
            un == QUIET_AFTER -> parts += QUIET_NOTES[
                Random("palquiet:$n".hashCode()).nextInt(QUIET_NOTES.size)]
        }
        val askIdx = n % ASKS.size
        parts += ASKS[askIdx]
        val sign = SIGNOFFS.getValue(band)[
            Random("palsign:$n".hashCode()).nextInt(
                SIGNOFFS.getValue(band).size)]
        val ps = PS_BANK[Random("palps:$n".hashCode()).nextInt(PS_BANK.size)]
        val text = parts.joinToString("\n\n") + "\n\n" + sign +
            "\n\n" + ps
        return Incoming(text, summary, fuKind, fuText, askIdx)
    }

    /** Your reply, composed in front of you: the tone frames
     *  it; the facts come from your records (up to two,
     *  freshest first) and are IDENTICAL under every tone —
     *  the tone colors the frame, never the facts. Pure:
     *  [sendReply] recomposes and keeps exactly this text. */
    fun composeReply(state: GameState, tone: String):
        Pair<String, List<Fact>> {
        val t = if (tone in TONES) tone else TONE_EVERYDAY
        val facts = factsAvailable(state).take(2)
        val n = lettersOut(state) + 1
        val opener = REPLY_OPENERS.getValue(t)[
            Random("palopen:$t:$n".hashCode()).nextInt(
                REPLY_OPENERS.getValue(t).size)]
        val body = if (facts.isEmpty())
            REPLY_NO_FACT.getValue(t)[Random("palnf:$t:$n".hashCode())
                .nextInt(REPLY_NO_FACT.getValue(t).size)]
        else facts.joinToString(" ") {
            REPLY_FACT_LINE.getValue(t).replace("{}", it.text) }
        val parts = mutableListOf(opener, body)
        val ask = lastAskIdx(state)
        if (ask in ASKS.indices)
            ASK_ANSWERS.getValue(t).getOrNull(ask)
                ?.let { parts += it }
        val closer = REPLY_CLOSERS.getValue(t)[
            Random("palclose:$t:$n".hashCode()).nextInt(
                REPLY_CLOSERS.getValue(t).size)]
        val ps = REPLY_PS.getValue(t)[
            Random("palrps:$t:$n".hashCode()).nextInt(
                REPLY_PS.getValue(t).size)]
        return (parts.joinToString("\n\n") + "\n\n" + closer +
            "\n\n" + ps) to facts
    }

    /** A reply may ride when a letter waits unanswered, or
     *  as the one revival letter that reopens a dormant
     *  correspondence. One letter in flight each way — the
     *  program's stated rule. A special letter waiting in
     *  the HOME mailbox (the settlement's word, the
     *  thank-you) may be answered any day of the year:
     *  home mail is not the school box. */
    fun canReply(state: GameState): Boolean {
        if (!isSignedUp(state)) return false
        if (homeMailPending(state)) return true
        return live(state) &&
            (unanswered(state) > 0 || isDormant(state))
    }

    fun sendReply(state: GameState, tone: String): String? {
        if (!canReply(state)) return null
        val wasDormant = isDormant(state)
        val (text, facts) = composeReply(state, tone)
        storeOutgoing(state, text, tone, facts)
        markFactsUsed(state, facts)
        adjustPoints(state,
            if (wasDormant) PTS_REVIVAL else PTS_PER_REPLY)
        if (wasDormant) {
            state.life["palDormant"] = 0
            state.life["palRevived"] = 1
        }
        // the home letter, if that is what waited, is
        // answered now — the mailbox is clear
        state.life.remove(K_HOME_MAIL)
        state.life["palUnanswered"] = 0
        state.life["palLastOutDay"] = state.absDay
        state.life["palNextDue"] = state.absDay +
            gapDays(state, lettersIn(state) + lettersOut(state))
        return text
    }

    // ================= the year's rhythm =================

    private fun deliverSpecial(state: GameState, text: String,
                               summary: String) {
        storeIncoming(state, Incoming(text, summary, "", "", -1),
            counts = false)
        // the specials land outside the school-year box —
        // at the home mailbox — so they stay answerable
        // there whatever the program's calendar says
        state.life[K_HOME_MAIL] = 1
    }

    /** Engine.onNewDay hook, in the rollover chain after
     *  the parade: the program's October note, the June 1
     *  settlement (visit or the maybe-next-summer letter),
     *  the post-visit thank-you, and letter arrivals on the
     *  cadence — stretched by silence, stopped by dormancy,
     *  paused while she's actually here. */
    fun dailyRollover(state: GameState) {
        val year = Yearbook.currentYear(state)
        // the box comes out — one note per program year
        if (state.gameMonth == SIGNUP_MONTH &&
            state.gameDay == SIGNUP_FROM &&
            signedYear(state) != year &&
            (state.life["palBoxNote"] ?: 0) != year) {
            state.life["palBoxNote"] = year
            state.events.add("✉️ The pen-pal program box is " +
                "out at the Elementary through Oct " +
                "$SIGNUP_TO — the ${Yearbook.labelOf(year)} " +
                "pairings. Late joiners wait for next year.")
        }
        // June 1: the school year just ended — settle it
        val ended = Yearbook.mostRecentEndedYear(state)
        if (state.gameMonth == 6 && state.gameDay == 1 &&
            signedYear(state) == ended &&
            (state.life["palSettledYear"] ?: 0) != ended) {
            state.life["palSettledYear"] = ended
            // the return season's shape (v0.15.0b): she has
            // visited, and no return trip stands on the
            // books — the program alternates
            val returnSeason =
                (state.life["palVisitYear"] ?: 0) > 0 &&
                    (state.life["palTripYear"] ?: 0) == 0
            if (bandOf(state) == BAND_FRIEND_I) {
                if (returnSeason) {
                    state.life["palTripStart"] =
                        FairCalendar.absDayOfOrdinal(
                            ordinalOfKey(
                                "%04d%02d%02d".format(ended,
                                    TRIP_MONTH, TRIP_DAY)))
                    state.life["palTripYear"] = ended
                    state.lifeText.remove("palTripDone")
                    deliverSpecial(state, INVITE_LETTER,
                        "Wren: the summer answer — the " +
                            "return trip is yours")
                    state.events.add("✉️ A letter from $PAL " +
                        "is waiting in your HOME mailbox: " +
                        "the year ended at the top of the " +
                        "board again, and this time the trip " +
                        "runs the other way. YOU go to " +
                        "$PAL_TOWN — the bus does the long " +
                        "road June $TRIP_DAY.")
                } else {
                    state.life["palVisitStart"] =
                        FairCalendar.absDayOfOrdinal(
                            ordinalOfKey(
                                "%04d%02d%02d".format(ended,
                                    VISIT_MONTH, VISIT_DAY)))
                    state.life["palVisitYear"] = ended
                    state.events.add("✉️ A letter from $PAL: the " +
                        "year ended at the top of the program's " +
                        "board — SHE'S COMING. $PAL_TOWN's best " +
                        "arrives June $VISIT_DAY for " +
                        "$VISIT_DAYS days. Watch your own front " +
                        "door for once.")
                }
            } else {
                deliverSpecial(state,
                    if (returnSeason) MAYBE_NEXT2_LETTER
                    else MAYBE_NEXT_LETTER,
                    if (returnSeason)
                        "Wren: the summer answer — the jar " +
                            "is not full (year two)"
                    else "Wren: the summer answer — maybe " +
                        "next summer")
                state.events.add("📬 A letter from $PAL is " +
                    "waiting in your mailbox.")
            }
        }
        // the morning after the visit: her thank-you
        val vs = state.life["palVisitStart"] ?: -1
        val vy = state.life["palVisitYear"] ?: 0
        if (vs >= 0 && vy > 0 && state.absDay == vs + VISIT_DAYS &&
            (state.life["palThanksYear"] ?: 0) != vy) {
            state.life["palThanksYear"] = vy
            deliverSpecial(state, THANKS_LETTER,
                "Wren: home again — the visit, rated")
            state.events.add("📬 A letter from $PAL is " +
                "waiting in your mailbox.")
        }
        // the morning after the return trip: her thank-you
        // rides home mail — the mirror of season one's
        val tStart = state.life["palTripStart"] ?: -1
        val tYear = state.life["palTripYear"] ?: 0
        if (tStart >= 0 && tYear > 0 &&
            state.absDay == tStart + TRIP_DAYS &&
            (state.life["palTripThanksYear"] ?: 0) != tYear) {
            state.life["palTripThanksYear"] = tYear
            deliverSpecial(state, TRIP_THANKS_LETTER,
                "Wren: you came — the visit, returned " +
                    "and rated")
            state.events.add("📬 A letter from $PAL is " +
                "waiting in your mailbox.")
        }
        // the trip's own mornings: the day's shape, noted
        // once a day on the ticker
        if (tripLive(state) &&
            (state.life["palTripNoteDay"] ?: -1) !=
                state.absDay) {
            state.life["palTripNoteDay"] = state.absDay
            val beat = tripBeatToday(state)
            state.events.add(
                if (beat != null)
                    "✈️ $PAL_TOWN, day " +
                        "${tripDayIndex(state) + 1} of " +
                        "$TRIP_DAYS: ${tripBeatLabel(beat)}" +
                        " — Wren has it planned. The trip " +
                        "runs through your mailbox, the " +
                        "way the whole correspondence does."
                else
                    "✈️ $PAL_TOWN, day " +
                        "${tripDayIndex(state) + 1} of " +
                        "$TRIP_DAYS: the planned beat is " +
                        "kept. The rest of the day is " +
                        "unscripted — hers and yours.")
        }
        // arrivals
        if (!live(state) || isDormant(state)) return
        if (visitLive(state)) return
        val due = state.life["palNextDue"] ?: return
        if (state.absDay < due) return
        val inc = composeIncoming(state)
        storeIncoming(state, inc, counts = true)
        if ((state.life["palRevived"] ?: 0) == 1)
            state.life["palRevived"] = 0
        if (unanswered(state) >= DORMANT_AT)
            state.life["palDormant"] = 1
        val stretch = if (unanswered(state) >= QUIET_AFTER)
            QUIET_STRETCH else 0
        state.life["palNextDue"] = state.absDay +
            gapDays(state, lettersIn(state) + lettersOut(state)) +
            stretch
        state.events.add("📬 A letter from $PAL ($PAL_TOWN) " +
            "is waiting in your mailbox.")
    }

    // ================= the visit =================

    fun visitStart(state: GameState): Int =
        state.life["palVisitStart"] ?: -1

    fun visitLive(state: GameState): Boolean {
        val vs = visitStart(state)
        return vs >= 0 && state.absDay in
            vs until vs + VISIT_DAYS
    }

    fun visitDayIndex(state: GameState): Int =
        state.absDay - visitStart(state)

    fun isLastVisitDay(state: GameState): Boolean =
        visitLive(state) && visitDayIndex(state) == VISIT_DAYS - 1

    fun goodbyeDue(state: GameState): Boolean =
        isLastVisitDay(state) &&
            (state.life["palGoodbye"] ?: 0) !=
                (state.life["palVisitYear"] ?: -1)

    /** Her day, on the Family whereNow pattern: mornings
     *  and evenings she's your house guest (indoors), the
     *  middle of the day she's at the park seeing the town
     *  she only knows from letters. Pure. */
    fun blockAt(state: GameState, timeMin: Int): Pair<String, Boolean>? {
        if (!visitLive(state)) return null
        return when {
            timeMin < 11 * 60 -> YOUR_HOUSE to true
            timeMin < 16 * 60 -> "KidsPark" to false
            timeMin < 21 * 60 + 30 -> YOUR_HOUSE to true
            else -> null
        }
    }

    fun presentAt(state: GameState, locId: String,
                  inside: Boolean): List<ActiveNpc> {
        if (CharacterSystem.isBuilding(locId) != inside)
            return emptyList()
        val block = blockAt(state, state.timeMin)
            ?: return emptyList()
        if (block.first != locId || block.second != inside)
            return emptyList()
        return listOf(ActiveNpc(persona(state), locId, inside))
    }

    /** The sheet's greeting: the goodbye owns the last
     *  day's first hello; the pool owns the rest. */
    fun greeting(state: GameState): String =
        if (goodbyeDue(state)) GOODBYE_LINE
        else VISIT_GREETINGS[
            (visitDayIndex(state) + lettersIn(state)) %
                VISIT_GREETINGS.size]

    fun talkLine(n: Int): String =
        VISIT_TALK[((n % VISIT_TALK.size) + VISIT_TALK.size) %
            VISIT_TALK.size]

    /** Once per sheet-open: the day's first talk adds its
     *  point, and a goodbye shown is a goodbye said. */
    fun noteTalk(state: GameState) {
        if (!visitLive(state)) return
        if ((state.life["palTalkDay"] ?: -1) != state.absDay) {
            state.life["palTalkDay"] = state.absDay
            adjustPoints(state, TALK_PTS_PER_DAY)
        }
        if (goodbyeDue(state))
            state.life["palGoodbye"] =
                state.life["palVisitYear"] ?: 0
    }

    // ================= the return trip (season two) =================

    fun tripYear(state: GameState): Int =
        state.life["palTripYear"] ?: 0

    fun tripStart(state: GameState): Int =
        state.life["palTripStart"] ?: -1

    /** The return trip is the live season's shape while
     *  she has visited you and no return trip stands on
     *  the books. Pure. */
    fun tripPending(state: GameState): Boolean =
        live(state) && (state.life["palVisitYear"] ?: 0) > 0 &&
            tripYear(state) == 0

    fun tripLive(state: GameState): Boolean {
        val ts = tripStart(state)
        return ts >= 0 && state.absDay in
            ts until ts + TRIP_DAYS
    }

    fun tripDayIndex(state: GameState): Int =
        state.absDay - tripStart(state)

    /** The beats this trip has kept, in the trip's own
     *  order. A hostile or stale stamp degrades to the
     *  beats it can name; unknown words are ignored by
     *  every reader that matters. */
    fun tripDone(state: GameState): Set<String> =
        (state.lifeText["palTripDone"] ?: "")
            .split(",").filter { it.isNotBlank() }.toSet()

    /** The beat today offers, if it offers one: the day's
     *  own beat, until it is kept. A day that goes by
     *  unkept is simply unkept — the trip does not punish.
     *  Pure. */
    fun tripBeatToday(state: GameState): String? {
        if (!tripLive(state)) return null
        val beat = TRIP_BEATS.getOrNull(tripDayIndex(state))
            ?: return null
        return if (beat in tripDone(state)) null else beat
    }

    fun tripScript(beat: String): List<String> =
        TRIP_SCRIPTS[beat] ?: emptyList()

    fun hostLine(n: Int): String =
        HOST_TALK[((n % HOST_TALK.size) + HOST_TALK.size) %
            HOST_TALK.size]

    fun hostFact(n: Int): String =
        HOST_FACTS[((n % HOST_FACTS.size) + HOST_FACTS.size) %
            HOST_FACTS.size]

    /** Once per beat: a kept beat is stamped in the
     *  trip's own order and pays its warmth in her lane —
     *  and only her lane. */
    fun noteTripBeat(state: GameState, beat: String): Boolean {
        if (beat !in TRIP_BEATS) return false
        if (beat in tripDone(state)) return false
        val done = tripDone(state) + beat
        state.lifeText["palTripDone"] =
            TRIP_BEATS.filter { it in done }.joinToString(",")
        adjustPoints(state, TRIP_BEAT_PTS)
        return true
    }

    // ================= the Journal page =================

    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        if (!isSignedUp(state)) {
            out += "📬 The Pen-Pal Program"
            out += "The Elementary runs a pen-pal program " +
                "with a school in $PAL_TOWN — a town with one " +
                "street, one blinking light, and a water " +
                "tower with a fish on it. You write real " +
                "letters, on paper, through the mail. Your " +
                "dad's Post Office is the route."
            out += "The signup box sits by the classroom " +
                "board every October $SIGNUP_FROM–$SIGNUP_TO. " +
                "Miss the window and the pairing waits for " +
                "next year — that's the program's stated rule."
            if (signupOpen(state))
                out += "The box is OUT right now, through " +
                    "October $SIGNUP_TO."
            out += "The letters are private: the Gazette " +
                "doesn't cover them, and the phone stays out " +
                "of it. Letters are letters."
            return out
        }
        out += "📬 $PAL — $PAL_TOWN"
        out += "Your pal: $PAL, eleven, of $PAL_TOWN — one " +
            "street, one blinking light, one water tower " +
            "with a fish on it. Paired through the " +
            "Elementary's program; the mail rides your " +
            "dad's Post Office."
        out += "Standing: ${bandWord(state)} — letters " +
            "in: ${lettersIn(state)}, letters out: " +
            "${lettersOut(state)}. The warmth lives in its " +
            "own book; the town's friendship ledger never " +
            "sees her name."
        when {
            tripLive(state) -> out += "✈️ YOU ARE THERE — " +
                "day ${tripDayIndex(state) + 1} of " +
                "$TRIP_DAYS in $PAL_TOWN. Wren hosts; the " +
                "beats run one a day — arrival, the tour, " +
                "counter duty, the goodbye — and they run " +
                "through your mailbox, the way everything " +
                "in this correspondence does."
            tripYear(state) > 0 &&
                state.absDay < tripStart(state) ->
                out += "✈️ THE RETURN TRIP IS STAMPED — " +
                    "you ride for $PAL_TOWN on June " +
                    "$TRIP_DAY: fifteen days after the " +
                    "settlement, because the road IS the " +
                    "seam. The invitation sits in your home " +
                    "mailbox; home mail can be answered " +
                    "any day."
            visitLive(state) -> out += "✈️ SHE IS HERE — " +
                "day ${visitDayIndex(state) + 1} of " +
                "$VISIT_DAYS. Mornings and evenings she's " +
                "your house guest; middays she's at the " +
                "park, seeing the town from your letters."
            homeMailPending(state) -> out += "A letter " +
                "from Wren sits in your HOME mailbox — it " +
                "came outside the school-year box, so home " +
                "mail rules apply: it can be answered any " +
                "day, at your mailbox, dormant route or " +
                "not. (A fresh October signup starts a new " +
                "year and folds it away unanswered.)"
            isDormant(state) -> out += "The correspondence " +
                "is dormant: three letters went unanswered, " +
                "and Wren has stopped filling your mailbox, " +
                "kindly and on the record. One letter from " +
                "you reopens the route."
            live(state) && unanswered(state) > 0 ->
                out += "A letter from Wren is waiting " +
                    "(${unanswered(state)} unanswered). " +
                    "Reply at your mailbox — it takes " +
                    "$REPLY_MINUTES minutes and your real week."
            live(state) -> out += "The route is open; the " +
                "next letter rides the cadence (every " +
                "$GAP_MIN–${GAP_MIN + GAP_SPAN - 1} days)."
            else -> out += "The school year is settled. " +
                "The box comes back out in October; if your " +
                "name goes in again, hers will already be " +
                "in it."
        }
        if (tripPending(state))
            out += "This year's shape, if the writing " +
                "holds: the RETURN trip. The program " +
                "alternates — last year she rode the long " +
                "road to you; this year the seat is yours. " +
                "Her letters keep circling it."
        val vy = state.life["palVisitYear"] ?: 0
        if (vy > 0 && !visitLive(state)) {
            val kept = state.photos.any { p ->
                p.figures.any { it.name == PAL } }
            out += "The visit, June $VISIT_DAY–" +
                "${VISIT_DAY + VISIT_DAYS - 1}, $vy: four " +
                "days, in person at last." +
                if (kept) " The album keeps her — she's in " +
                    "a photo here."
                else " No photo caught her; the camera " +
                    "missed its one assignment."
        }
        val ty = tripYear(state)
        if (ty > 0 && !tripLive(state) &&
            state.absDay >= tripStart(state)) {
            val kept = TRIP_BEATS.filter {
                it in tripDone(state) }
            out += "The return trip, June $TRIP_DAY–" +
                "${TRIP_DAY + TRIP_DAYS - 1}, $ty: you " +
                "went to HER this time." +
                if (kept.isEmpty())
                    " The days went by unkept — the road " +
                        "ran on schedule and the record " +
                        "says the visit was mostly weather."
                else " Kept: " + kept.joinToString(", ") {
                    tripBeatLabel(it) } + "."
        }
        for (line in foldedLines(state)) out += "✉ $line"
        val dropped = state.life["palFoldedDropped"] ?: 0
        if (dropped > 0) out += "…and $dropped earlier " +
            "folds are gone entirely. Paper is patient, not " +
            "infinite."
        val recs = metaRecords(state)
        for (rec in recs.asReversed()) {
            val isIn = rec[0].startsWith("i")
            val key = if (isIn) "palLetter:${rec[0].drop(1)}"
            else "palSent:${rec[0].drop(1)}"
            val text = state.lifeText[key] ?: continue
            out += if (isIn)
                "✉️ ${dateLabelOf(rec[1])} — Wren wrote:"
            else "✍️ ${dateLabelOf(rec[1])} — you wrote:"
            out += text
        }
        return out
    }
}

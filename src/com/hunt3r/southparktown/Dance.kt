package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// THE SCHOOL DANCE (v0.13.0b) — once a year, on the second
// Friday of February, South Park Elementary dresses its gym
// for the SWEETHEART DANCE, and the bravest thing a fourth
// grader can do all winter is ask somebody to go.
//
// The model, in order:
//   - THE ANCHOR is pure calendar: the second Friday of
//     February (DanceCalendar), announced a week ahead.
//     The date is proven disjoint from the play's opening
//     window, the fair, the carnival and every finale
//     window by a multi-decade sweep in one lived time
//     space — February's second Friday (the 8th–14th)
//     clears the winter finale (Feb 22+) by a week and
//     the carnival's latest close (Jan 27) by twelve days.
//     The ONE thing that can own the Friday is Valentine's
//     Day itself: when Feb 14 IS the second Friday, the
//     holiday beat has the day and the dance yields the
//     whole year, with the reason printed on the poster.
//     A live case climax or a finale window reaching the
//     day (School.quietReason, the town's standing
//     definition) yields it the same way — a yielded year
//     is a skipped year, honestly, never a postponed one.
//   - THE ASK is the heart, and it rides the tap-person
//     menu like the crew invite — the player's initiative,
//     never a TalkGate rail. Asking costs nothing. A YES
//     needs the Friend tier (30+): below it, the menu says
//     "not yet" and no ask is spent (the Crew discipline).
//     Above it, each kid's answer is AUTHORED (the
//     DISPOSITION table): the glad say yes; the shy say
//     yes once you're Close (55+) and "ask me again when
//     we're closer" below that — a soft no that spends
//     nothing; the rest give a kind, in-voice NO that is
//     FINAL for that dance. One date at a time: a second
//     YES stands the first kid up — allowed, authored,
//     remembered (a small friendship cost at the switch,
//     their line in the gym that night, the fact kept on
//     the Journal page), and a stood-up kid's answer to a
//     re-ask is final too. Going STAG is a first-class
//     plan, stated on the poster, the page and the menu:
//     every beat of the night is reachable stag.
//   - THE EVENING (18:00–21:30, the Elementary gym) is an
//     agenda claim seated with the set pieces — after the
//     play, before sleepovers, so the dance outranks
//     dinners, shifts and sleeping bags for its people on
//     the one night, and yields to everything bigger. It
//     seats your date, a seeded share of the kid cast, and
//     Principal Victoria, who chaperones. Four beats:
//     ARRIVAL (the gym dressed — banner, punch table, DJ
//     booth), the SLOW DANCE (played on the showdown bar:
//     hold your nerve through the song; a steady partner's
//     nerve literally widens your margin — stag, the
//     spotlight variant is yours alone), REFRESHMENTS
//     (the punch table serves real pantry items — your
//     lemonade poured as punch, your snack cakes —
//     consumed exactly when taken; the water cooler is
//     free and always there), and THE LAST SONG (the
//     night read back, and ONE group photo minted by the
//     photo system's own composition, captioned with the
//     night). Beats stamp as played; a partial night
//     wraps as the night it was, an untouched one as
//     missed, a yielded one as yielded — the record never
//     flatters.
//   - THE RECORDS: a Journal 💃 THE DANCE page, one
//     TOWN NEWS line in the paper's next edition (names
//     and the photograph's existence, no invented color —
//     the dance is section copy, never a headline tier),
//     and ONE morning-after phone text through the
//     existing harvest — from your date if you had one,
//     else a friend who was there; fact-only, moves
//     nothing.
//   - TONE (the standing wall): these are elementary
//     kids. The date is courage-and-friendship framing
//     throughout — asking takes nerve, the slow song is
//     played for comedy and heart, and every outcome is
//     friendship and standing ONLY. No romance mechanics,
//     no kiss beats, no couple systems; the one shadow is
//     the honest stood-up consequence, because promises
//     are real even in fourth grade.
//   - STATE: everything rides life/lifeText under
//     "dance…" keys — zero new save keys, so pre-0.13.0b
//     saves load untouched and join the next dance.
// ============================================================

/** The dance's pure calendar: a function of the Gregorian
 *  calendar alone (the PlayCalendar pattern). */
object DanceCalendar {

    /** [year]'s dance: the second Friday of February.
     *  Feb 1's ordinal, forward to the first Friday, +7. */
    fun danceOrdinal(year: Int): Int {
        val feb1 = ordinalOfKey("%04d0201".format(year))
        val toFriday = (5 - FairCalendar.dowOfOrdinal(feb1) + 7) % 7
        return feb1 + toFriday + 7
    }

    fun danceDay(year: Int): Int =
        FairCalendar.absDayOfOrdinal(danceOrdinal(year))

    /** The poster goes up a week before the dance. */
    fun announceDay(year: Int): Int = danceDay(year) - Dance.ANN_BACK

    /** Feb 14's ordinal in [year] — the yield coincidence. */
    fun valentineOrdinal(year: Int): Int =
        ordinalOfKey("%04d0214".format(year))
}

object Dance {

    // ---------------- the dials (all suite-pinned) --------

    const val SCHOOL = "Elementary"
    const val NAME = "The Sweetheart Dance"
    const val CHAPERONE = "Victoria"
    const val TOPIC = "the dance"

    /** The evening's window: doors at 6, last song by 9:30
     *  (a school night, even in February). */
    const val EVE_FROM = 18 * 60
    const val EVE_TO = 21 * 60 + 30

    /** Days before the dance the poster goes up and the
     *  asking opens. */
    const val ANN_BACK = 7

    /** Days after the dance a wrapped night stays "this
     *  year's" on the Journal page. */
    const val CURRENT_TAIL = 30

    /** The ask's floor: the Friend tier, the Crew ask's
     *  own number (Crew.INVITE_MIN_POINTS). */
    const val ASK_MIN_POINTS = 30

    /** The shy kids' floor: the Close-friend tier — they
     *  say yes once they really know you. */
    const val SHY_MIN_POINTS = 55

    /** The friendship cost of standing a kid up, applied
     *  once at the switch — small, real, and above the
     *  faction cold-shoulder threshold, so it's a hurt,
     *  not a feud. */
    const val STOOD_UP_COST = 3

    /** Friendship paid to your date at the slow dance:
     *  a steady song pays full, a sideways one still pays
     *  a little — they laughed WITH you. */
    const val DANCE_PAY_GOOD = 2
    const val DANCE_PAY_SIDEWAYS = 1

    /** The seeded share of the kid cast in the gym, in
     *  percent. The dance is the whole grade's night, but
     *  not literally the whole grade. */
    const val ATTEND_PCT = 65

    // beat bits in the record
    const val BEAT_ARRIVAL = 1
    const val BEAT_DANCE = 2
    const val BEAT_PUNCH = 4
    const val BEAT_LAST = 8

    // ---------------- authored dispositions ----------------
    // Every kid's ask, authored: GLAD say yes at the Friend
    // tier; SHY say yes at Close and "ask me again" below
    // it; NO give the kind, final no. The tables are the
    // town's social truth for this one night — the suite
    // pins the census (every kid exactly once).

    const val GLAD = "glad"
    const val SHY = "shy"
    const val NO = "no"

    val DISPOSITION: Map<String, String> = mapOf(
        "Stan" to GLAD, "Kyle" to GLAD, "Kenny" to GLAD,
        "Wendy" to GLAD, "Clyde" to GLAD, "Jimmy" to GLAD,
        "Timmy" to GLAD, "Tolkien" to GLAD, "Bebe" to GLAD,
        "Heidi" to GLAD, "Nichole" to GLAD, "Red" to GLAD,
        "Kevin" to GLAD, "Francis" to GLAD, "Scott" to GLAD,
        "Annie" to GLAD, "Ike" to GLAD,
        "Kevin McCormick" to GLAD, "Sophie" to GLAD,
        "Terrance Mephesto" to GLAD, "Jenny" to GLAD,
        "Tricia" to GLAD,
        "Butters" to SHY, "Karen" to SHY, "Bradley" to SHY,
        "Pip" to SHY, "Dougie" to SHY, "Jason" to SHY,
        "Flora" to SHY, "Filmore" to SHY, "Quaid" to SHY,
        "Mimsy" to SHY,
        "Cartman" to NO, "Craig" to NO, "Tweek" to NO,
        "Michael" to NO, "Pete" to NO, "Henrietta" to NO,
        "Firkle" to NO, "Nathan" to NO, "Shelley" to NO,
        "DogPoo" to NO)

    fun dispositionOf(kid: String): String =
        DISPOSITION[kid] ?: GLAD

    /** The YES lines, one per kid, in their voice — the
     *  ask's whole payoff. Courage-and-friendship framing,
     *  every one. */
    val YES_LINES: Map<String, String> = mapOf(
        "Stan" to "Yeah. Yeah, okay — I'll go with you. " +
            "If my dad tries to chaperone-dance I will " +
            "simply leave the state, but otherwise: yeah.",
        "Kyle" to "You want to go with me? ...Okay. Yes. " +
            "But if Cartman says ONE thing, we leave and " +
            "start a better dance in the parking lot.",
        "Kenny" to "(muffled) Mmph mmph! — yes. Kenny " +
            "will be there, and Kenny will be wearing the " +
            "good parka.",
        "Butters" to "W-well gee, you mean it? You asked " +
            "ME? ...Okay! I'll wear my nicest shirt and " +
            "I'll practice not steppin' on your feet!",
        "Wendy" to "Yes. And for the record, I already " +
            "know which song I'm requesting, I've made a " +
            "small schedule, and no, you don't get to see " +
            "it. It's called having a plan.",
        "Clyde" to "Yeah! Cool. Cool cool cool. I'll " +
            "bring extra punch money and only cry a normal " +
            "amount if a slow song is embarrassing.",
        "Jimmy" to "W-wow, really? I would be " +
            "th-thrilled. I've been working on my dance " +
            "material — my opening line kills. It's " +
            "'hello'. It kills because of the delivery.",
        "Timmy" to "TIMMY! — (he nods so hard his whole " +
            "chair approves) — Timmy!!",
        "Tolkien" to "Yeah, I'm in. Fair warning: my mom " +
            "already bought me a jacket for this in, like, " +
            "October. So the bar is set.",
        "Bebe" to "Obviously yes. I'll handle the hair " +
            "situation, you handle showing up. This is " +
            "going to be the best-documented dance in " +
            "school history.",
        "Heidi" to "Yes! I was hoping somebody'd ask. " +
            "I'll bring my good hair clips and my " +
            "zero-expectations attitude. It'll be great.",
        "Nichole" to "Yes — okay! But I get to pick one " +
            "song too. I have taste and I'm not afraid to " +
            "use it.",
        "Red" to "Yeah, alright. If anyone steps on my " +
            "shoes I'm simply leaving, but until then: " +
            "yes, let's do this.",
        "Kevin" to "Yes! I have prepared a ranked list " +
            "of slow songs from every major sci-fi " +
            "soundtrack. You're welcome. Number one will " +
            "make you cry.",
        "Francis" to "Yeah! I mean — yeah. Cool. I " +
            "already told my mom. She cried a little. In " +
            "a good way. Moms are weird.",
        "Scott" to "Yes!! I have been preparing a dance " +
            "entrance for YEARS. There's a cape involved. " +
            "Not a big cape. A respectful cape.",
        "Annie" to "Yes! Finally, a school event where " +
            "the dress code does something. I'm in. I " +
            "call dibs on requesting the fast songs.",
        "Ike" to "Ike go dance! Ike step on FEET! — " +
            "(he is already dancing, by himself, at full " +
            "speed)",
        "Kevin McCormick" to "Yeah, sure. Stuart's boy " +
            "has a reputation as a dancer in this family " +
            "and I intend to protect it. Karen's coming " +
            "too — she's my backup dancer.",
        "Sophie" to "Yes! I already have a dress with " +
            "pockets, which means I can carry TWO snack " +
            "cakes. We're going to be unstoppable.",
        "Terrance Mephesto" to "Yes. A social ritual " +
            "with measurable variables — attendance, " +
            "tempo, punch viscosity. I shall attend, take " +
            "notes, and presumably dance.",
        "Jenny" to "Yes! I call the good side of the " +
            "photo. I know my angles and I am not sharing " +
            "them, but I'll stand near you so you benefit.",
        "Tricia" to "Yes. I did a whole report on " +
            "whether to say yes and the findings were " +
            "yes. Craig's sister has STANDARDS though — " +
            "be on time.",
        // the shy kids' yeses, at Close tier
        "Karen" to "Me? Really me? ...Okay. Yes. Kevin " +
            "says I have to be home by nine, but the dance " +
            "ENDS at nine-thirty, so I'll deal with Kevin.",
        "Bradley" to "I — yes. Okay. I checked and " +
            "re-checked and the answer keeps being yes. " +
            "Please don't let me overthink the punch table.",
        "Pip" to "Oh! Oh my. Me? Truly? How terribly " +
            "kind. Yes — yes please. I shall wear my very " +
            "best and be stepped on only politely.",
        "Dougie" to "Yeah okay!! I practiced dancing in " +
            "my room. My lamp is my witness. I am " +
            "READY-ish.",
        "Jason" to "Yeah... yeah okay. Cool. I'll be " +
            "cool about it. Starting now. Was that cool? " +
            "Don't answer.",
        "Flora" to "Yes... I'll bring flowers for the " +
            "punch table. Flowers make any gym thirty " +
            "percent fancier. That's science I made up.",
        "Filmore" to "Okay, yes. But if there's a vote " +
            "for best dancer I'm running a fair campaign. " +
            "Posters. Slogans. The works.",
        "Quaid" to "Yeah. Okay. I did the math on " +
            "asking somebody myself and the numbers were " +
            "scary, so — yes. Thank you for doing the " +
            "math part.",
        "Mimsy" to "Doy — I mean yes! Boss— I mean... " +
            "yes! I'll be there and I'll only fall over " +
            "the regular amount!")

    /** The NO lines: kind, in-voice, and final for that
     *  dance. Nobody is cruel; the answer is just the
     *  answer. */
    val NO_LINES: Map<String, String> = mapOf(
        "Cartman" to "Pff. No. I'm not going to the " +
            "dance WITH somebody, like some kind of date " +
            "accessory. People attend the dance to be in " +
            "the room with ME. I might go. Alone. " +
            "Majestically.",
        "Craig" to "No. Nothing against you. It's loud, " +
            "the punch is warm, and slow songs make me " +
            "feel things in public. No.",
        "Tweek" to "A DANCE?! With MUSIC and PEOPLE and " +
            "a SLOW SONG where everyone WATCHES?! That's " +
            "too much pressure!! I can't — I can't — " +
            "gah! Hide in the library with me instead. " +
            "That's my kind of night.",
        "Michael" to "No. A school dance is a " +
            "fluorescent ritual for people afraid of the " +
            "dark. I'll be at the cemetery gate, being " +
            "interesting.",
        "Pete" to "No. I don't dance. I brood at a " +
            "professional level, and the gym has terrible " +
            "brooding acoustics. All echo. No.",
        "Henrietta" to "No. I wrote a poem about the " +
            "dance instead. It's called 'No.' It's my " +
            "shortest poem and my favorite.",
        "Firkle" to "No. I'm the smallest goth and even " +
            "I can't goth hard enough to survive a conga " +
            "line. There WILL be a conga line. There " +
            "always is.",
        "Nathan" to "No. And before you ask — it's not " +
            "a strategy thing or a scheme thing. I just " +
            "don't want to. A man can simply not want " +
            "to. Put THAT in the yearbook.",
        "Shelley" to "No. I'm in middle-school energy " +
            "now, turd. A fourth-grade gym dance is " +
            "beneath me. ...Have fun or whatever. Tell " +
            "them the punch needs ice.",
        "DogPoo" to "No. I'd get dirt on the gym floor " +
            "and Principal Victoria would just KNOW it " +
            "was me, and she'd be right. It is always me.")

    /** The shy kids' soft no — spends nothing, final
     *  about nothing: "ask me when we're closer". */
    val SOFT_LINES: List<String> = listOf(
        "I — I want to say yes. Can you ask me again " +
            "when we're better friends? I just need to " +
            "be sure. Please?",
        "Not yet. Not a no! I just... need to know you " +
            "better first. Ask me again before Friday, " +
            "okay?",
        "G-gee. Um. Ask me again when we're closer, " +
            "alright? I mean it — ask me again.",
        "I might say yes if I knew you better. That's " +
            "not a no. That's a 'come back'.")

    /** Your date, asked again — you're already going. */
    val AGAIN_LINES: List<String> = listOf(
        "We're already going together, remember? You " +
            "asked, I said yes, there's a whole plan. " +
            "See you at the gym.",
        "Still yes. It was yes this morning, it's yes " +
            "now. Save the nerves for the slow song.")

    /** A stood-up kid, asked again — final, and they
     *  have earned the last word on it. */
    val STOOD_LINES: List<String> = listOf(
        "You already asked me. And then you asked " +
            "somebody else. I did the math at home, " +
            "alone, with snacks. No.",
        "I said yes once. Once was the whole supply. " +
            "Ask your new plan how THEIR night's going.")

    // ---------------- the record ----------------

    /** One year's dance. [date] is the kid you're going
     *  with ("" = stag, or not yet asked); [stood] the
     *  kids you stood up on the way (in order); [noList]
     *  the final NOs; [softList] the shy kids who said
     *  "ask me again". [night]: "" | played | missed |
     *  yielded. Everything serializes into one lifeText
     *  record (the Play pattern). */
    data class DanceRecord(
        val year: Int,
        val date: String = "",
        val stood: List<String> = emptyList(),
        val noList: List<String> = emptyList(),
        val softList: List<String> = emptyList(),
        val beats: Int = 0,
        val danceResult: String = "",   // steady|swayed|glorious
        val punchLem: Int = 0,
        val punchCake: Int = 0,
        val night: String = "",
        val yieldWhy: String = "",      // valentine|case|finale|holiday|play
        val photoId: String = "",
        val nightDay: Int = -1
    ) {
        fun beatDone(bit: Int): Boolean = beats and bit != 0
    }

    private fun recKey(year: Int) = "danceRec:$year"

    fun record(state: GameState, year: Int): DanceRecord? {
        val raw = state.lifeText[recKey(year)] ?: return null
        val f = raw.split("|")
        if (f.size < 13 || f[0] != "1") return null
        fun names(s: String): List<String> =
            if (s.isEmpty()) emptyList() else s.split(",")
        return DanceRecord(year,
            date = f[1], stood = names(f[2]),
            noList = names(f[3]), softList = names(f[4]),
            beats = f[5].toIntOrNull() ?: 0,
            danceResult = f[6],
            punchLem = f[7].toIntOrNull() ?: 0,
            punchCake = f[8].toIntOrNull() ?: 0,
            night = f[9], yieldWhy = f[10],
            photoId = f[11],
            nightDay = f[12].toIntOrNull() ?: -1)
    }

    private fun saveRecord(state: GameState,
                           rec: DanceRecord) {
        val raw = listOf("1", rec.date,
            rec.stood.joinToString(","),
            rec.noList.joinToString(","),
            rec.softList.joinToString(","),
            rec.beats.toString(), rec.danceResult,
            rec.punchLem.toString(), rec.punchCake.toString(),
            rec.night, rec.yieldWhy, rec.photoId,
            rec.nightDay.toString()).joinToString("|")
        state.lifeText[recKey(rec.year)] = raw
    }

    /** A year's record, created empty on first write —
     *  reads never create (the Journal's purity rule). */
    private fun recordOrNew(state: GameState,
                            year: Int): DanceRecord =
        record(state, year) ?: DanceRecord(year)

    /** Every dance this save has on record, oldest first. */
    fun allRecords(state: GameState): List<DanceRecord> =
        state.lifeText.keys
            .filter { it.startsWith("danceRec:") }
            .mapNotNull { it.removePrefix("danceRec:")
                .toIntOrNull() }
            .sorted()
            .mapNotNull { record(state, it) }

    // ---------------- which dance is "this year's" -------

    /** The dance this save is living through: the first
     *  whose living window — up to its night, and
     *  CURRENT_TAIL days past it — hasn't closed. */
    fun currentYear(state: GameState): Int {
        for (y in (state.gameYear - 1)..(state.gameYear + 1)) {
            if (DanceCalendar.danceDay(y) + CURRENT_TAIL >=
                state.absDay) return y
        }
        return state.gameYear + 1
    }

    /** The dance is on the town's mind once its poster
     *  week is near (the Play's cycleLive pattern). */
    fun cycleLive(state: GameState): Boolean =
        state.absDay >= DanceCalendar.announceDay(
            currentYear(state)) - 3

    fun currentRecord(state: GameState): DanceRecord? =
        record(state, currentYear(state))

    // ---------------- the yield ----------------

    /** The calendar-known yield: Valentine's Day IS the
     *  second Friday — the holiday beat owns the day and
     *  the gym stays dark all year. Known at the poster,
     *  printed on it. */
    fun posterYield(year: Int): Boolean =
        DanceCalendar.danceOrdinal(year) ==
            DanceCalendar.valentineOrdinal(year)

    /** Why tonight's dance does not happen, or null when
     *  the night is free to happen: the standing owners,
     *  read through the town's own definitions (School's
     *  quiet day — a case climax due, a finale window, a
     *  holiday beat — plus the play's opening night,
     *  which the calendar already keeps in October; the
     *  check stands anyway, belt and braces, per the
     *  brief). */
    fun yieldReason(state: GameState): String? {
        val year = currentYear(state)
        if (state.absDay != DanceCalendar.danceDay(year))
            return null
        if (posterYield(year)) return "valentine"
        return when (School.quietReason(state)) {
            School.QUIET_CASE -> "case"
            School.QUIET_FINALE -> "finale"
            School.QUIET_HOLIDAY -> "holiday"
            else -> if (Play.nightCanHappen(state)) "play"
                else null
        }
    }

    /** The yield reasons in the town's words — the
     *  poster, the page, the paper and the morning report
     *  all quote the same sentence. */
    fun yieldText(why: String): String = when (why) {
        "valentine" -> "Valentine's Day has the Friday — " +
            "the whole school is doing the hearts thing, " +
            "and the gym is full of construction paper"
        "case" -> "there's case business in the air, and " +
            "even a dance steps around it"
        "finale" -> "it's finale week — the whole town " +
            "is holding its breath, gym included"
        "holiday" -> "the holiday has the day, and the " +
            "gym keeps with the day"
        "play" -> "the school play owns the stage that " +
            "night — one set piece per evening is the rule"
        else -> "the town had bigger business that night"
    }

    /** The evening can happen: it's the day, in the
     *  window's reach, and no owner holds it. The first
     *  consult on the day that finds an owner stamps the
     *  reason (the Family arrival pattern: the fact is
     *  recorded where it's learned) so the morning wrap
     *  can say why, even after the owner has moved on. */
    fun eveningLive(state: GameState): Boolean {
        val year = currentYear(state)
        if (state.absDay != DanceCalendar.danceDay(year))
            return false
        val rec = record(state, year)
        if (rec != null && rec.night.isNotEmpty()) return false
        val why = yieldReason(state)
        if (why != null) {
            if (state.lifeText["danceYield:$year"] == null)
                state.lifeText["danceYield:$year"] = why
            return false
        }
        return true
    }

    /** Who's in the gym: a seeded share of the kid cast
     *  (the same roll every read — attendance is a fact
     *  about the year, not a per-tick coin), plus your
     *  date whether the roll took them or not. */
    fun attendees(year: Int): List<String> =
        CharacterSystem.CAST_KIDS.filter { kid ->
            Random(("danceatt:$year:$kid").hashCode()
                .toLong()).nextInt(100) < ATTEND_PCT
        }.sorted()

    fun attends(year: Int, kid: String): Boolean =
        kid in attendees(year)

    // ---------------- the ask ----------------

    /** Why [name] can't be asked right now, or null when
     *  the ask is live: the window, the cast wall, and
     *  the record's own facts. Codes: window | adult |
     *  date (already yours) | no (final) | stood (final) |
     *  tier (not yet). */
    fun askBlock(state: GameState, name: String): String? {
        if (!askWindowOpen(state)) return "window"
        val rec = currentRecord(state)
        if (rec != null && rec.night.isNotEmpty())
            return "window"
        if (name !in CharacterSystem.CAST &&
            name !in CharacterSystem.CAST_KIDS) return "adult"
        if (CharacterSystem.isAdult(name)) return "adult"
        if (rec != null) {
            if (rec.date == name) return "date"
            if (name in rec.stood) return "stood"
            if (name in rec.noList) return "no"
        }
        if ((state.friendship[name] ?: 0) < ASK_MIN_POINTS)
            return "tier"
        return null
    }

    /** The asking is open from the poster morning to the
     *  doors on the night — never in a poster-yielded
     *  year, when the poster itself says why. */
    fun askWindowOpen(state: GameState): Boolean {
        if (!cycleLive(state)) return false
        val year = currentYear(state)
        if (posterYield(year)) return false
        val d = DanceCalendar.danceDay(year)
        if (state.absDay < DanceCalendar.announceDay(year) ||
            state.absDay > d) return false
        if (state.absDay == d && state.timeMin >= EVE_FROM)
            return false
        return true
    }

    /** The tap-menu's dance option for [name], or null
     *  when no option belongs there (the window's shut,
     *  or their answer is already final history). The
     *  label states the case in the menu's own voice,
     *  like the crew option does. */
    fun menuOption(state: GameState, name: String): String? {
        if (!askWindowOpen(state)) return null
        val rec = currentRecord(state)
        if (rec != null && rec.night.isNotEmpty()) return null
        return when (askBlock(state, name)) {
            null -> "💃 Ask $name to the dance"
            "date" -> "💃 You're going with $name 💃"
            "tier" -> "💃 Ask $name to the dance " +
                "(you're not close yet)"
            "adult" -> "💃 Ask $name to the dance " +
                "(they're a grown-up)"
            else -> null   // no / stood: final — no option
        }
    }

    /** The not-yet line for a below-tier ask (the Crew
     *  pattern: the ask isn't spent, the friendship just
     *  isn't under it yet). */
    fun notYetLine(state: GameState, name: String): String =
        "You start to ask $name about the dance and " +
            "realize, kindly, that you barely know each " +
            "other yet. Some asks need a friendship under " +
            "them first. (Get to know them — Friend tier, " +
            "$ASK_MIN_POINTS — and ask for real.)"

    /** A grown-up, asked: flattered, final, and entirely
     *  outside the record. */
    fun adultLine(name: String): String =
        "$name considers it with enormous seriousness. " +
            "“I'm a grown-up,” they conclude. “The school " +
            "dance is a kids' night. But — flattered. " +
            "Genuinely. Now go ask somebody your own age, " +
            "and stand up straight when you do it.”"

    data class AskResult(val kind: String, val text: String)
    // kind: yes | no | soft | date | stood | tier | adult | window

    /** Ask [name] to the dance. The full discipline:
     *  a yes at their disposition's tier, the switch that
     *  stands a kid up (cost paid here, once), the final
     *  no recorded, the soft no remembered kindly. */
    fun ask(state: GameState, name: String): AskResult {
        when (askBlock(state, name)) {
            "window" -> return AskResult("window",
                "The asking isn't open — the poster's week " +
                    "is the asking week. (Or the dance is " +
                    "already a memory.)")
            "adult" -> return AskResult("adult", adultLine(name))
            "date" -> return AskResult("date",
                pickSeeded("danceagain:${currentYear(state)}:$name",
                    AGAIN_LINES))
            "stood" -> return AskResult("stood",
                pickSeeded("dancestood:${currentYear(state)}:$name",
                    STOOD_LINES))
            "no" -> return AskResult("no",
                "Already asked, already answered — a no " +
                    "is a whole answer, and theirs stands.")
            "tier" -> return AskResult("tier",
                notYetLine(state, name))
        }
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        val pts = state.friendship[name] ?: 0
        return when (dispositionOf(name)) {
            NO -> {
                saveRecord(state, rec.copy(
                    noList = rec.noList + name))
                AskResult("no", NO_LINES[name] ?:
                    "$name shakes their head, kindly and " +
                        "completely. “No. But thanks for " +
                        "asking properly.”")
            }
            SHY -> if (pts < SHY_MIN_POINTS) {
                if (name !in rec.softList) saveRecord(state,
                    rec.copy(softList = rec.softList + name))
                AskResult("soft", pickSeeded(
                    "dancesoft:$year:$name", SOFT_LINES))
            } else yes(state, rec, name)
            else -> yes(state, rec, name)
        }
    }

    /** A YES lands: the date is set, and any previous
     *  date is stood up — the cost paid at the switch,
     *  the fact kept, in order. */
    private fun yes(state: GameState, rec: DanceRecord,
                    name: String): AskResult {
        var r = rec.copy(softList = rec.softList - name)
        val prev = r.date
        var stoodNote = ""
        if (prev.isNotEmpty() && prev != name) {
            Engine().relAdjust(state, prev, -STOOD_UP_COST)
            r = r.copy(stood = r.stood + prev)
            stoodNote = "\n\n($prev was your plan until " +
                "this minute. That's standing somebody up, " +
                "and the town — and $prev — will remember " +
                "it Friday. The Journal keeps the fact.)"
        }
        r = r.copy(date = name)
        saveRecord(state, r)
        plantDanceTopic(state, name)
        return AskResult("yes", (YES_LINES[name] ?:
            "“Yes. Okay. Yes.”") + stoodNote)
    }

    private fun pickSeeded(seed: String,
                           lines: List<String>): String =
        lines[Random(seed.hashCode().toLong())
            .nextInt(lines.size)]

    /** Plant "the dance" in a kid's Threads topics (the
     *  Play planter's mirror). */
    fun plantDanceTopic(state: GameState, kid: String) {
        val mem = NpcMemoryStore.getOrCreate(kid)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    // ---------------- the agenda claim ----------------

    /** The dance's agenda claim, seated in Engine's stack
     *  after the play and before sleepovers: on the one
     *  night, the gym holds your date, the seeded room and
     *  the chaperone — outranking the dinner claims and
     *  the sleeping bags for its people, and never
     *  standing at all in a yielded year. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin < EVE_FROM || timeMin >= EVE_TO)
            return null
        if (!eveningLive(state)) return null
        val year = currentYear(state)
        val rec = record(state, year)
        if (rec != null && rec.night.isNotEmpty()) return null
        val inRoom = npcName == CHAPERONE ||
            (rec != null && rec.date == npcName) ||
            attends(year, npcName)
        if (!inRoom) return null
        return AgendaBlock(SCHOOL,
            if (npcName == CHAPERONE)
                "💃 Chaperoning the dance"
            else "💃 The school dance")
    }

    // ---------------- the evening's beats ----------------

    /** The player is standing in the gym while the night
     *  is live (the beats' shared gate). */
    fun beatsHere(state: GameState): Boolean {
        if (!eveningLive(state)) return false
        if (state.timeMin !in EVE_FROM until EVE_TO)
            return false
        val me = state.agents.values
            .firstOrNull { it.isPlayer } ?: return false
        return me.location == SCHOOL && state.playerInside
    }

    /** The beat buttons the gym offers right now, in the
     *  night's own order. */
    fun beatLabels(state: GameState): List<String> {
        if (!beatsHere(state)) return emptyList()
        val rec = currentRecord(state) ?: DanceRecord(
            currentYear(state))
        val out = mutableListOf<String>()
        if (!rec.beatDone(BEAT_ARRIVAL))
            out += "💃 Step into the dance"
        if (rec.beatDone(BEAT_ARRIVAL) &&
            !rec.beatDone(BEAT_DANCE))
            out += "🎵 The slow dance — hold your nerve"
        if (rec.beatDone(BEAT_ARRIVAL) &&
            !rec.beatDone(BEAT_PUNCH))
            out += "🥤 The punch table"
        if (rec.beatDone(BEAT_ARRIVAL) &&
            rec.beatDone(BEAT_DANCE) &&
            !rec.beatDone(BEAT_LAST))
            out += "🌙 Stay for the last song"
        return out
    }

    /** BEAT 1 — ARRIVAL: the gym dressed, the room as it
     *  stands, your date if you brought one — and the
     *  stood-up kid's toast-that-isn't, once, if they're
     *  in the room to raise it. */
    fun playArrival(state: GameState): String? {
        if (!beatsHere(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (rec.beatDone(BEAT_ARRIVAL)) return null
        saveRecord(state, rec.copy(
            beats = rec.beats or BEAT_ARRIVAL))
        val present = School.presentAt(state)
        val out = StringBuilder()
        out.append("💃 THE GYM, DRESSED — a paper banner " +
            "(“$NAME”, letters the art class is quietly " +
            "proud of) over the court, the punch table " +
            "along the wall, and the DJ booth: a card " +
            "table, two speakers, and a laptop glowing " +
            "like a control room. $CHAPERONE is by the " +
            "door with a clipboard and the calm of a " +
            "woman who has chaperoned weather.")
        if (rec.date.isNotEmpty()) {
            out.append("\n\n")
            if (rec.date in present) {
                out.append("${rec.date} is here — dance " +
                    "best on, doing the thing where they " +
                    "pretend they weren't waiting by the " +
                    "punch table for you specifically. " +
                    "They were. It's fine. It's good.")
            } else {
                out.append("${rec.date} — your date — " +
                    "isn't in the room yet. The night is " +
                    "young and the door is watched, mostly " +
                    "by you.")
            }
        } else {
            out.append("\n\nYou came stag, which the " +
                "poster listed as a whole plan in the same " +
                "size letters, and the room receives it " +
                "that way: a kid with nobody to impress " +
                "and the whole floor to do it on.")
        }
        val stoodHere = rec.stood.lastOrNull {
            it in present }
        if (stoodHere != null) {
            out.append("\n\nAcross the gym, $stoodHere " +
                "sees you" +
                (if (rec.date.isNotEmpty())
                    " come in with ${rec.date}" else "") +
                ". They lift their punch cup, just a " +
                "little, like a toast that isn't one. " +
                "They don't come over. They don't have " +
                "to. You know.")
        }
        out.append("\n\n${present.size} kids are in the " +
            "room. The fast songs are doing their work. " +
            "The slow one is coming — it always is.")
        return out.toString()
    }

    // ---------------- the slow dance (played) ------------

    /** The slow song as a session on the showdown bar:
     *  four phrases; each phrase you tap to step, and the
     *  step holds if your nerve — widened by your
     *  partner's composure, Play's own STAGE_NERVE table
     *  — carries the zone you released in. Stag, the
     *  partner is the spotlight and your nerve is the new
     *  kid's honest 5. */
    class DanceSession(seed: Long, val partner: String) {
        private val rnd = Random(seed)
        var steps = 0; private set
        var held = 0; private set
        val over: Boolean get() = steps >= PHRASES

        /** Your partner's composure: their authored nerve
         *  (the play's table — one nerve table for the
         *  town, wearing two hats), or the new kid's 5
         *  under the spotlight alone. */
        val nerve: Int get() =
            if (partner.isEmpty()) 5
            else Play.stageNerve(partner)

        /** Step at release position 0..1: the zone's own
         *  power, steadied by the nerve multiplier.
         *  Returns the steadiness that landed. */
        fun step(release01: Double): Double {
            if (over) return 0.0
            steps++
            val zone = Showdown.zoneOf(release01.toFloat())
            val jitter = (rnd.nextDouble() * 0.06 - 0.03)
                .coerceIn(-0.05, 0.05)
            val steady = Showdown.zonePower(zone) *
                (0.72 + 0.05 * nerve) + jitter
            if (steady >= Feel.POWER_GOOD) held++
            return steady
        }

        /** steady: clean through · swayed: held on ·
         *  glorious: sideways, and the room loved it. */
        val result: String get() = when {
            held >= PHRASES -> "steady"
            held >= 2 -> "swayed"
            else -> "glorious"
        }

        companion object { const val PHRASES = 4 }
    }

    fun danceSeed(state: GameState): Long =
        ("dance:${state.absDay}").hashCode().toLong()

    /** Judge the played slow dance: lands beat 2, pays
     *  your date in friendship (standing only, ever),
     *  and tells it back. */
    fun judgeSlowDance(state: GameState,
                       session: DanceSession): String? {
        if (!beatsHere(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (rec.beatDone(BEAT_DANCE)) return null
        if (!session.over) return null
        if (session.partner != rec.date) return null
        val result = session.result
        saveRecord(state, rec.copy(
            beats = rec.beats or BEAT_DANCE,
            danceResult = result))
        val p = session.partner
        if (p.isNotEmpty()) {
            Engine().relAdjust(state, p,
                if (result == "glorious") DANCE_PAY_SIDEWAYS
                else DANCE_PAY_GOOD)
            plantDanceTopic(state, p)
        }
        return when {
            p.isNotEmpty() && result == "steady" ->
                "🎵 THE SLOW DANCE — four phrases, and " +
                    "you and $p hold your nerve clean " +
                    "through all of them: no stepped feet, " +
                    "no panic, just the whole gym watching " +
                    "two kids be braver than the song. " +
                    "$CHAPERONE nods once, like a judge " +
                    "passing sentence on a job well done."
            p.isNotEmpty() && result == "swayed" ->
                "🎵 THE SLOW DANCE — you and $p sway, " +
                    "wobble, recover, and hold on through " +
                    "the end of the song, which is the " +
                    "whole skill. Somewhere in phrase three " +
                    "there was a foot situation. It is " +
                    "behind you both now. Everything is."
            p.isNotEmpty() ->
                "🎵 THE SLOW DANCE — it goes gloriously " +
                    "sideways inside the first phrase: a " +
                    "turn becomes a tour, the tour becomes " +
                    "a near-collision with the punch table, " +
                    "and $p is laughing too hard to be " +
                    "embarrassed, which saves everything. " +
                    "The room applauds the recovery. It is " +
                    "the laugh people will describe at " +
                    "breakfast."
            result == "steady" ->
                "🎵 THE SLOW DANCE — stag, spotlight, " +
                    "four phrases, and you dance the whole " +
                    "slow song alone like the floor was " +
                    "booked in your name. Unhurried. " +
                    "Unbothered. Somewhere, a kid with a " +
                    "date is jealous of the peace of it."
            result == "swayed" ->
                "🎵 THE SLOW DANCE — alone under the " +
                    "spotlight, you sway through it with " +
                    "one wobble and a recovery so smooth " +
                    "the gym decides it was choreography. " +
                    "Stag is a whole plan. Tonight it's a " +
                    "good one."
            else ->
                "🎵 THE SLOW DANCE — the spotlight finds " +
                    "you, the song finds your feet, and " +
                    "your feet find each other, repeatedly, " +
                    "in public. It is glorious. A kid by " +
                    "the wall starts a slow clap. You bow. " +
                    "Stag, sideways, and somehow the moment " +
                    "of the night."
        }
    }

    // ---------------- refreshments ----------------

    /** The punch table's stock: your own pantry, brought
     *  to share like the poster asked. Lemonade pours as
     *  punch; snack cakes are snack cakes. */
    val PUNCH_ITEMS = listOf("lemonade", "snack_cake")

    fun punchStock(state: GameState, itemId: String): Int =
        state.inventory[itemId] ?: 0

    /** Serve one: consumes exactly one of the item, the
     *  first serve of anything lands beat 3. "water" is
     *  the cooler — free, endless, and nobody's favorite,
     *  which is why it's still there at 9 PM. */
    fun servePunch(state: GameState,
                   itemId: String): String? {
        if (!beatsHere(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (!rec.beatDone(BEAT_ARRIVAL)) return null
        if (itemId != "water" && itemId !in PUNCH_ITEMS)
            return null
        if (itemId != "water" &&
            punchStock(state, itemId) <= 0) return null
        var r = rec
        if (itemId != "water") {
            val n = punchStock(state, itemId) - 1
            if (n <= 0) state.inventory.remove(itemId)
            else state.inventory[itemId] = n
            r = if (itemId == "lemonade")
                r.copy(punchLem = r.punchLem + 1)
            else r.copy(punchCake = r.punchCake + 1)
        }
        if (!r.beatDone(BEAT_PUNCH))
            r = r.copy(beats = r.beats or BEAT_PUNCH)
        saveRecord(state, r)
        val forDate = rec.date.isNotEmpty() &&
            (r.punchLem + r.punchCake) % 2 == 0 &&
            itemId != "water"
        return when (itemId) {
            "lemonade" ->
                "🥤 PUNCH — the good stuff, because it's " +
                    "yours: one lemonade, poured into a " +
                    "paper cup and promoted to punch by " +
                    "the setting." +
                    if (forDate) " This one's for " +
                        "${rec.date} — delivered with a " +
                        "steadiness the slow dance would " +
                        "envy." else ""
            "snack_cake" ->
                "🧁 SNACK CAKE — broken out at the punch " +
                    "table like it's a ceremony, because " +
                    "tonight it is." +
                    if (forDate) " Half goes to " +
                        "${rec.date}, by the ancient law " +
                        "of the snack cake." else ""
            else ->
                "💧 THE COOLER — water, cold and honest. " +
                    "The DJ points at you in respect. The " +
                    "cooler has outlasted every dance this " +
                    "gym has ever held."
        }
    }

    // ---------------- the last song ----------------

    /** BEAT 4 — THE LAST SONG: the night read back from
     *  the record alone, and the one photograph minted
     *  from the room as it stands. Wraps the night:
     *  the claim releases, the record closes as played. */
    fun playLastSong(state: GameState): String? {
        if (!beatsHere(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (!rec.beatDone(BEAT_ARRIVAL) ||
            !rec.beatDone(BEAT_DANCE)) return null
        if (rec.beatDone(BEAT_LAST)) return null
        var r = rec.copy(beats = rec.beats or BEAT_LAST,
            night = "played", nightDay = state.absDay)
        saveRecord(state, r)
        val photoId = mintPhoto(state, r)
        if (photoId.isNotEmpty()) {
            r = r.copy(photoId = photoId)
            saveRecord(state, r)
        }
        val resultLine = when (r.danceResult) {
            "steady" -> "The slow dance: held, clean " +
                "through, four phrases of nerve."
            "swayed" -> "The slow dance: swayed, wobbled, " +
                "recovered — held on, which is the skill."
            "glorious" -> "The slow dance: gloriously " +
                "sideways, and the laugh of the night."
            else -> "The slow dance: danced."
        }
        val punchLine = when {
            r.punchLem + r.punchCake > 0 ->
                "The punch table saw ${r.punchLem + r.punchCake} " +
                    "of your own stock poured and shared."
            r.beatDone(BEAT_PUNCH) ->
                "The punch table: the cooler, bravely."
            else -> "The punch table went unvisited — " +
                "some nights are like that."
        }
        return buildString {
            append("🌙 THE LAST SONG — $CHAPERONE kills " +
                "the fast playlist and the gym goes quiet " +
                "in the way gyms do when a night has gone " +
                "well. " +
                if (r.date.isNotEmpty())
                    "You came with ${r.date}, and " +
                        "${r.date} is still here at the " +
                        "end of it, which was the whole " +
                        "assignment."
                else
                    "You came stag and you leave stag, " +
                        "and somewhere between those two " +
                        "facts a whole night happened on " +
                        "your own terms.")
            append("\n\n$resultLine $punchLine")
            if (photoId.isNotEmpty()) append("\n\n📸 One " +
                "photograph, taken by the door as the " +
                "coats came out — the room, the banner, " +
                "the night. It's in your album; the " +
                "darkroom can print it.")
            append("\n\n📰 The paper prints the night in " +
                "its next edition, and the Journal keeps " +
                "it (💃 THE DANCE).")
        }
    }

    /** The night's ONE photograph: the photo system's
     *  own group composition from the people actually in
     *  the room, captioned with the night — who you came
     *  with, or stag, in words. Returns the record id, or
     *  "" if the camera stayed home (the Play rule). */
    fun mintPhoto(state: GameState,
                  rec: DanceRecord): String {
        val present = School.presentAt(state)
        if (present.isEmpty()) return ""
        val me = state.agents.values
            .firstOrNull { it.isPlayer }
        val shot = Photos.composeGroup(state, present,
            emptyList(), me?.name ?: "New Kid",
            me?.appearance ?: emptyMap())
        val withLine = if (rec.date.isNotEmpty())
            "You came with ${rec.date}."
        else "You came stag."
        val poster = shot.copy(caption =
            "💃 $NAME — ${dateLabel(state.absDay)}, " +
                "South Park Elementary gym. $withLine " +
                "The banner, the punch table, the whole " +
                "loud room.")
        Photos.applyPhoto(state, poster)
        return poster.id
    }

    // ---------------- mornings ----------------

    /** The dance's mornings, called by Engine.onNewDay
     *  AFTER Play and BEFORE the paper and the phone (its
     *  texts and its edition line derive from these
     *  facts): the poster's ticker on announcement
     *  morning, and the wrap on the morning after —
     *  played if you were in it at all, yielded with its
     *  reason if an owner held the night, missed if the
     *  gym simply went on without you. */
    fun dailyRollover(state: GameState) {
        if (!cycleLive(state)) return
        val year = currentYear(state)
        val d = DanceCalendar.danceDay(year)
        // announcement morning: the poster's ticker line
        if (state.absDay == DanceCalendar.announceDay(year) &&
            state.life["danceAnn:$year"] == null) {
            state.life["danceAnn:$year"] = 1
            if (posterYield(year)) {
                note(state,
                    "💃 The $NAME poster went up with an " +
                        "apology stapled to it: no dance " +
                        "this year — ${yieldText("valentine")}. " +
                        "The gym stays dark that Friday.")
            } else {
                note(state,
                    "💃 POSTER UP at the Elementary: THE " +
                        "SWEETHEART DANCE — ${dateLabel(d)}, " +
                        "6 PM in the gym. Ask a kid this " +
                        "week (tap them, and ask), or come " +
                        "stag: stag is a whole plan, not a " +
                        "consolation. $CHAPERONE chaperones.")
            }
        }
        // the morning after: wrap the night
        if (state.absDay == d + 1) {
            val rec = record(state, year)
            if (rec == null || rec.night.isEmpty()) {
                val base = rec ?: DanceRecord(year)
                val yieldWhy = when {
                    posterYield(year) -> "valentine"
                    base.beats > 0 -> ""
                    else -> state.lifeText["danceYield:$year"]
                        ?: ""
                }
                val night = when {
                    yieldWhy.isNotEmpty() -> "yielded"
                    base.beats > 0 -> "played"
                    else -> "missed"
                }
                saveRecord(state, base.copy(night = night,
                    yieldWhy = yieldWhy, nightDay = d))
                val line = when (night) {
                    "played" -> "💃 $NAME ($year) — the " +
                        "gym was loud till 9:30, and you " +
                        "were in it."
                    "yielded" -> "💃 $NAME ($year) — no " +
                        "dance this year: ${yieldText(yieldWhy)}."
                    else -> "💃 $NAME ($year) — the dance " +
                        "went on without you. The gym was " +
                        "loud anyway."
                }
                state.townLog.add(line)
                while (state.townLog.size > 120)
                    state.townLog.removeAt(0)
            }
        }
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40)
            state.events.removeAt(0)
    }

    /** "Fri, Feb 13"-style labels from the calendar math
     *  (the Play helper's twin). */
    fun dateLabel(absDay: Int): String {
        val key = keyFromOrdinal(FairCalendar
            .ordinalOfAbsDay(absDay))
        val m = key.substring(4, 6).toIntOrNull() ?: 1
        val dd = key.substring(6, 8).toIntOrNull() ?: 1
        val months = listOf("Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec")
        val dow = Jobs.WEEKDAY_NAMES[
            Jobs.weekdayOf(absDay)].take(3)
        return "$dow ${months[m - 1]} $dd"
    }

    // ---------------- look & board ----------------

    /** The Elementary's dance business, for Look (the
     *  School/Play board seam): the marked wall, the
     *  poster, the asking week, the dressed gym on the
     *  night, the aftermath. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != SCHOOL) return null
        if (!cycleLive(state)) return null
        val year = currentYear(state)
        val d = DanceCalendar.danceDay(year)
        val rec = record(state, year)
        val today = state.absDay
        if (rec != null && rec.night.isNotEmpty()) {
            if (today - d in 0..10) {
                return when (rec.night) {
                    "yielded" -> "💃 The poster is still " +
                        "up, with the apology stapled to " +
                        "it: no dance this year — " +
                        yieldText(rec.yieldWhy) + ". " +
                        "Somebody has drawn a small, " +
                        "respectful frowny face in the " +
                        "corner. It might have been " +
                        "$CHAPERONE."
                    "missed" -> "💃 The banner is down. " +
                        "The dance happened — the floor " +
                        "still has the scuffs of it — and " +
                        "you weren't in the room. The " +
                        "Journal (💃 THE DANCE) keeps what " +
                        "there is to keep."
                    else -> "💃 The banner is down; one " +
                        "paper heart is still taped to the " +
                        "gym wall where nobody has the " +
                        "heart to peel it. The night is " +
                        "in the Journal (💃 THE DANCE)" +
                        if (rec.photoId.isNotEmpty())
                            ", and its photograph is in " +
                                "your album."
                        else "."
                }
            }
            return null
        }
        return when {
            today < DanceCalendar.announceDay(year) ->
                "💃 A rectangle of gym wall has been " +
                    "measured and marked in $CHAPERONE's " +
                    "handwriting: “DANCE — WALL.” " +
                    "Underneath: “the banner will fit. " +
                    "it always fits.”"
            today < d ->
                "💃 POSTER: THE SWEETHEART DANCE — " +
                    "${dateLabel(d)}, 6–9:30 PM, this " +
                    "gym. Ask somebody this week, or come " +
                    "stag — the poster lists both in the " +
                    "same size letters, on purpose. Punch " +
                    "table, DJ booth, one (1) slow song, " +
                    "no exceptions." +
                    if (posterYield(year))
                        " Stapled to it, an apology: no " +
                            "dance this year — " +
                            yieldText("valentine") + "."
                    else ""
            eveningLive(state) ->
                "💃 THE GYM, DRESSED: a paper banner " +
                    "(“$NAME”) over the court, the punch " +
                    "table under the wall, and the DJ " +
                    "booth — a card table, two speakers, " +
                    "and a laptop glowing like a control " +
                    "room. The floor is everybody's. (The " +
                    "classroom board runs the night's " +
                    "beats.)"
            else ->
                "💃 The gym is set for a dance that isn't " +
                    "happening tonight — the banner's up, " +
                    "the chairs are stacked, and the night " +
                    "belongs to somebody bigger" +
                    (yieldReason(state)?.let {
                        ": ${yieldText(it)}" } ?: "") + "."
        }
    }

    // ---------------- the Journal page ----------------

    /** The 💃 THE DANCE page: this year's dance whole
     *  while it's live (the poster, your plan, the ask's
     *  whole honest state), the wrapped night read back
     *  from the record after, and past years reduced to
     *  history lines. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val year = currentYear(state)
        val d = DanceCalendar.danceDay(year)
        val rec = record(state, year)
        out += "$NAME — South Park Elementary's February " +
            "dance, ${dateLabel(d)}"
        if (rec == null || rec.night.isEmpty()) {
            out += "One Friday a year the gym becomes a " +
                "ballroom: a paper banner, a punch table, " +
                "a DJ booth made of a card table and " +
                "confidence, and one slow song that " +
                "everybody has to survive somehow. " +
                "Principal $CHAPERONE chaperones."
            when {
                posterYield(year) ->
                    out += "💃 This year: NO DANCE — " +
                        yieldText("valentine") + ". The " +
                        "poster went up with the apology " +
                        "stapled to it."
                state.absDay < DanceCalendar.announceDay(year) ->
                    out += "📋 The poster goes up " +
                        "${dateLabel(DanceCalendar.announceDay(year))}. " +
                        "The asking opens with it."
                state.absDay <= d -> {
                    out += "💃 The asking is open: tap a " +
                        "kid and ask. A yes needs a real " +
                        "friendship under it (Friend tier); " +
                        "the shy ones need to know you " +
                        "better; a no is kind and final. " +
                        "Or come stag — stag is a whole " +
                        "plan."
                    out += when {
                        rec != null && rec.date.isNotEmpty() ->
                            "Your plan: you're going with " +
                                "${rec.date}."
                        else -> "Your plan: stag, unless " +
                            "the week changes your mind."
                    }
                    if (rec != null && rec.stood.isNotEmpty())
                        out += "Stood up along the way: " +
                            rec.stood.joinToString(", ") +
                            ". (You switched. It cost " +
                            "what it cost, and the gym " +
                            "remembers.)"
                    if (rec != null && rec.noList.isNotEmpty())
                        out += "Asked and answered no, " +
                            "kindly and for keeps: " +
                            rec.noList.joinToString(", ") + "."
                    if (rec != null && rec.softList.isNotEmpty())
                        out += "Said “ask me when we're " +
                            "closer”: " +
                            rec.softList.joinToString(", ") + "."
                }
                else -> out += "💃 This year's dance is " +
                    "behind you — the wrap is writing " +
                    "itself into the record."
            }
        } else {
            out += when (rec.night) {
                "yielded" -> "💃 NO DANCE THIS YEAR — " +
                    yieldText(rec.yieldWhy) + ". A " +
                    "skipped year, honestly: some Fridays " +
                    "belong to bigger things."
                "missed" -> "💃 THE DANCE HAPPENED — and " +
                    "you weren't in the room. The gym was " +
                    "loud till 9:30 anyway. The record " +
                    "says what happened: nothing, on your " +
                    "part, and that's a fact too."
                else -> "💃 THE NIGHT: " +
                    if (rec.date.isNotEmpty())
                        "you came with ${rec.date}."
                    else "you came stag."
            }
            if (rec.night == "played") {
                out += "Beats: " +
                    (if (rec.beatDone(BEAT_ARRIVAL))
                        "you stepped in" else "—") + " · " +
                    when (rec.danceResult) {
                        "steady" -> "the slow dance held, " +
                            "clean through"
                        "swayed" -> "the slow dance " +
                            "swayed and held on"
                        "glorious" -> "the slow dance " +
                            "went gloriously sideways"
                        else -> "the slow song went " +
                            "undanced"
                    } + " · " +
                    (if (rec.beatDone(BEAT_PUNCH))
                        "the punch table, visited"
                    else "the punch table, unvisited") +
                    " · " +
                    (if (rec.beatDone(BEAT_LAST))
                        "you stayed for the last song"
                    else "you slipped out before the " +
                        "last song") + "."
                if (rec.stood.isNotEmpty())
                    out += "Stood up along the way: " +
                        rec.stood.joinToString(", ") + "."
                if (rec.photoId.isNotEmpty())
                    out += "📸 The night's photograph is " +
                        "in your album (the gym, " +
                        "${dateLabel(rec.nightDay)}) — the " +
                        "darkroom can print it."
                else
                    out += "📸 No photograph of the night " +
                        "is on record — nobody's camera " +
                        "was there at the end."
            }
            if (rec.night == "missed" &&
                rec.date.isNotEmpty())
                out += "You'd asked ${rec.date}, and they'd " +
                    "said yes. They went. You didn't. The " +
                    "record keeps that too — gently, and " +
                    "in full."
        }
        // history: elder dances, one line each
        val elder = allRecords(state)
            .filter { it.year != year &&
                it.night.isNotEmpty() }
            .sortedByDescending { it.year }.take(3)
        for (old in elder) {
            out += "📜 ${old.year}: " + when (old.night) {
                "yielded" -> "no dance — " +
                    yieldText(old.yieldWhy) + "."
                "missed" -> "the dance went on without you."
                else -> (if (old.date.isNotEmpty())
                    "you went with ${old.date}"
                else "you went stag") +
                    (if (old.stood.isNotEmpty())
                        "; stood up: " +
                            old.stood.joinToString(", ")
                    else "") +
                    (if (old.photoId.isNotEmpty())
                        "; the photograph's in the album"
                    else "") + "."
            }
        }
        return out
    }

    // ---------------- the paper ----------------

    /** The dance's ONE line in the paper's TOWN NEWS,
     *  for the edition of the week the night landed in:
     *  names and the photograph's existence — the facts
     *  on the record, no invented color. Null in every
     *  other week. */
    fun paperLine(state: GameState, week: Int): String? {
        val rec = allRecords(state).firstOrNull {
            it.night.isNotEmpty() && it.nightDay >= 0 &&
                Jobs.weekIndex(it.nightDay) == week
        } ?: return null
        val label = dateLabel(rec.nightDay)
        return when (rec.night) {
            "yielded" -> "💃 The $NAME did not happen " +
                "this year — ${yieldText(rec.yieldWhy)}. " +
                "The gym stayed dark; the poster says so."
            "missed" -> "💃 The $NAME — the Elementary " +
                "gym, $label. The new kid wasn't in the " +
                "room. The night is on the school's " +
                "record, not the album's."
            else -> "💃 The $NAME — the Elementary gym, " +
                "$label. The new kid " +
                (if (rec.date.isNotEmpty())
                    "came with ${rec.date}"
                else "came stag") + ". " +
                (if (rec.photoId.isNotEmpty())
                    "A photograph of the night is on " +
                        "record (the album has it)."
                else "No photograph of the night is on " +
                    "record.")
        }
    }

    // ---------------- the phone ----------------

    /** The dance's ONE morning-after text, in the
     *  harvest's fact discipline: from your date if you
     *  had one, else a friend who was in the room —
     *  fact-only, from the record, moving nothing. A
     *  yielded year texts nobody: nothing happened. */
    fun phoneBeat(state: GameState):
        Triple<String, String, String>? {
        val year = currentYear(state)
        val rec = record(state, year) ?: return null
        if (rec.night.isEmpty()) return null
        if (state.absDay != DanceCalendar.danceDay(year) + 1)
            return null
        if (rec.night == "yielded") return null
        val photoBit = if (rec.photoId.isNotEmpty())
            " the photo came out — it's in your album."
        else ""
        if (rec.night == "missed") {
            if (rec.date.isNotEmpty()) {
                return Triple(rec.date, "stood-date",
                    "you didn't come friday. i waited by " +
                        "the door with two cups of punch " +
                        "like an idiot. it's fine. it's " +
                        "not fine. monday, i guess.")
            }
            val sender = friendThere(state, rec) ?: return null
            return Triple(sender, "missed",
                "you missed the dance friday. the gym " +
                    "was loud, the punch was warm. the " +
                    "journal has the night, what there " +
                    "was of it.")
        }
        // played
        if (rec.date.isNotEmpty()) {
            val resultBit = when (rec.danceResult) {
                "steady" -> "we actually held it together " +
                    "through the slow song."
                "swayed" -> "the slow song wobbled and we " +
                    "held on anyway."
                "glorious" -> "the slow song went sideways " +
                    "and i am STILL laughing."
                else -> "a whole dance happened."
            }
            return Triple(rec.date, "date",
                "last night was the dance. $resultBit" +
                    "$photoBit monday, then.")
        }
        val sender = friendThere(state, rec) ?: return null
        return Triple(sender, "stag",
            "the dance, in review: you came stag and the " +
                "slow song did not defeat you.$photoBit " +
                "respect.")
    }

    /** A friend who was in the room: the highest-
     *  friendship attendee of the year (name order breaks
     *  ties), at the Friend tier or better — if the room
     *  held nobody who knows you, the morning is quiet. */
    private fun friendThere(state: GameState,
                            rec: DanceRecord): String? =
        attendees(rec.year)
            .filter { it != rec.date &&
                (state.friendship[it] ?: 0) >= ASK_MIN_POINTS }
            .maxWithOrNull(compareBy<String> {
                state.friendship[it] ?: 0 }.thenBy { it })

    // ---------------- fitting in ----------------

    /** The gym's story for Look at the school on the
     *  night itself is [lookText]; this is the board's
     *  one-line version for the classroom board dialog. */
    fun boardLine(state: GameState): String? {
        if (!cycleLive(state)) return null
        val year = currentYear(state)
        val rec = record(state, year)
        if (rec != null && rec.night.isNotEmpty()) return null
        val d = DanceCalendar.danceDay(year)
        return when {
            posterYield(year) -> "💃 The dance poster, " +
                "with its apology: no dance this year."
            state.absDay == d && eveningLive(state) ->
                "💃 TONIGHT: the dance, 6–9:30, in the " +
                    "gym. The beats are on this board."
            state.absDay in DanceCalendar.announceDay(year)..d ->
                "💃 The dance is ${dateLabel(d)} — the " +
                    "asking is open, and stag is a whole " +
                    "plan."
            else -> null
        }
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// School days (v0.7.0d, Queue #7 item 4) — South Park
// Elementary stops being a facade on school days.
//
// On weekdays the fourth grade actually MEETS:
//
//   - THE LESSON: during class hours (9:00–12:00 and
//     13:00–15:00) the classroom board offers the day's
//     lesson — a deterministic topic from a written bank,
//     taught by Garrison (Principal Victoria covers
//     Wednesdays), in two short beats, with the kids
//     ACTUALLY PRESENT reacting in character, and ONE
//     participation moment: answer right-ish (the lesson
//     itself taught it), answer funny, or pass — passing
//     is always safe, and every ending is judged kindly.
//     Sitting one pays a small friendship sprinkle to the
//     classmates in the room and plants the "school"
//     thread in them. One lesson per school day.
//   - RECESS: the midday block (12:00–13:00) holds one
//     rotating micro-beat in the yard — a four-square
//     ruling, a snowball truce (snow days), a note relay —
//     refereed between two kids who are actually there.
//     Flavor and a little friendship; no new economy.
//   - THE SPELLING BEE: Fridays, 13:00–15:00, Principal
//     Victoria presiding. Eight kid spellers drawn
//     deterministically from the class, eliminated round
//     by round on their own data (the sharp kids run
//     deep; Cartman cheats and gets caught; Kenny's
//     muffled spelling is accepted by the judges, somehow).
//     The player spells letter by letter off the choice
//     row — winnable and losable on play quality, settled
//     exactly once like every other game in town: capsule
//     coins, a game feat, and the bee trophy on the wall.
//
// Records live in the Journal's 🎓 SCHOOL page. No grades,
// no stats — attendance is texture, not a school sim.
// DEFERENCE (the standing town grammar): a due case
// climax, a finale week, or a holiday with its own beat
// owns the day outright — the classroom just has a quiet
// day, and says so honestly.
//
// Pure logic over GameState, MovieNights-shaped: all state
// rides the existing life/lifeText maps, day-keyed and
// pruned at rollover. ZERO new save keys — a v0.7.0c save
// loads with an empty school record.
// ============================================================

object School {

    // ---------------- the dials (README tuning ledger) ----------------

    /** Which weekdays are school days (Jobs.weekdayOf: 0 = Sunday).
     *  Monday through Friday — the fourth grade rests on weekends,
     *  like it owes the weekend money. */
    val SCHOOL_WEEKDAYS: Set<Int> = setOf(1, 2, 3, 4, 5)

    /** Class hours: first bell. The 9:00 agenda block seats the
     *  class at the Elementary; the lesson runs from here. */
    const val CLASS_FROM: Int = 9 * 60

    /** The midday block: recess (and lunch, in the fiction —
     *  the yard gets the beat, the cafeteria gets the noise). */
    const val RECESS_FROM: Int = 12 * 60
    const val RECESS_TO: Int = 13 * 60

    /** Last bell: the 15:00 agenda block scatters the class to
     *  bus stops and driveways; class is done for the day. */
    const val CLASS_TO: Int = 15 * 60

    /** Principal Victoria covers class one named day a week —
     *  Wednesday, clipboard and all. Garrison teaches the rest. */
    const val VICTORIA_WEEKDAY: Int = 3

    /** The friendship a sat lesson pays each classmate actually
     *  in the room. Small on purpose: attendance is texture. */
    const val LESSON_PAY: Int = 1

    /** ... and the teacher's own bump for a right-ish answer:
     *  the lesson taught it; the teacher noticed you listened. */
    const val TEACHER_PAY: Int = 1

    /** The sprinkle's cap: the nearest few desks, not the whole
     *  class farmed at once. */
    const val SPRINKLE_CAP: Int = 10

    /** The bee's day: Friday. (Jobs.weekdayOf: 5 = Friday.) */
    const val BEE_WEEKDAY: Int = 5

    /** The bee's entry window: set up after lunch, done by
     *  last bell. */
    const val BEE_FROM: Int = 13 * 60
    const val BEE_TO: Int = 15 * 60

    /** Kid spellers facing the player. */
    const val FIELD_SIZE: Int = 8

    /** The round Cartman is disqualified in if he's still
     *  standing — sleeve spelling has a shelf life. */
    const val CHEAT_ROUND: Int = 3

    /** The bell: no bee runs past this many rounds; a lone
     *  survivor at the bell takes the showdown. */
    const val MAX_ROUNDS: Int = 8

    /** The winner's purse, in capsule coins — the town's one
     *  hard currency, paid like every other game's. */
    const val BEE_WIN_COINS: Int = 5

    /** The friendship the bee's runner-up takes home — losing
     *  a final in front of everyone deserves something. */
    const val FINALIST_PAY: Int = 2

    /** Journal history caps (lessons/bees/recess remembered). */
    const val HISTORY_CAP: Int = 20
    const val BEE_HISTORY_CAP: Int = 12
    const val RECESS_HISTORY_CAP: Int = 12

    /** The memory thread a school day plants. */
    const val TOPIC: String = "school"

    const val QUIET_CASE: String = "case"
    const val QUIET_FINALE: String = "finale"
    const val QUIET_HOLIDAY: String = "holiday"
    const val QUIET_TRIP: String = "trip"

    private const val K_LESSONS = "school:lessons"
    private const val K_RECESS = "school:recess"
    private const val K_BEE_IN = "school:beeIn"
    private const val K_BEE_WON = "school:beeWon"
    private const val K_BEE_BEST = "school:beeBest"
    private const val HIST_LESSONS = "schoolHist"
    private const val HIST_BEES = "beeHist"
    private const val HIST_RECESS = "recessHist"

    private val SNOW_WEATHERS = setOf("light_snow", "heavy_snow", "blizzard")

    // ---------------- the calendar ----------------

    fun isSchoolDay(absDay: Int): Boolean =
        Jobs.weekdayOf(absDay) in SCHOOL_WEEKDAYS

    /** How many school days the year has had up to and including
     *  [absDay] — the rotation's clock (weekends don't advance
     *  the curriculum). */
    fun schoolDayIndex(absDay: Int): Int {
        val weeks = absDay / 7
        var count = weeks * 5
        for (d in weeks * 7..absDay) if (isSchoolDay(d)) count++
        return count
    }

    /** The next school day strictly after [absDay]. */
    fun nextSchoolDay(absDay: Int): Int {
        var d = absDay + 1
        while (!isSchoolDay(d)) d++
        return d
    }

    /** Who teaches on [absDay]: the principal covers Wednesdays;
     *  the rest of the week is Garrison's room, hat and all. */
    fun teacherFor(absDay: Int): String =
        if (Jobs.weekdayOf(absDay) == VICTORIA_WEEKDAY) "Victoria" else "Garrison"

    // ---------------- deference ----------------

    /** True when any case's climax is due on [state]'s day —
     *  judged at noon on a copy, the way the Bijou judges its
     *  doors: all four cases, one scan. (Solution-free: this
     *  reads due-ness only, never case content.) */
    fun anyClimaxDueToday(state: GameState): Boolean {
        val noon = state.copy(timeMin = 12 * 60)
        return Mystery.finaleChoiceDue(noon) ||
            Mystery2.accusationDue(noon) ||
            Mystery3.dispositionDue(noon) ||
            Mystery4.vigilDue(noon) ||
            Mystery5.confrontationDue(noon) ||
            Mystery6.meetingDue(noon) ||
            Mystery7.restageDue(noon) ||
            Mystery8.readingDue(noon) ||
            Mystery9.auditDue(noon) ||
            Mystery10.hangingDue(noon) ||
            Mystery11.voicingDue(noon) ||
            Mystery12.returnsDue(noon) ||
            Mystery13.verdictDue(noon)
    }

    /** Why the classroom is quiet today, or null when school
     *  runs — the standing priority: a case owns the day, then
     *  a finale, then a holiday's own beat. Last and lowest,
     *  v0.14.0b: a field trip day — the class is out of the
     *  building on the trip's own business (FieldTrip owns
     *  the day; if a bigger owner also holds it, the trip
     *  yields and the bigger reason is the one that prints). */
    fun quietReason(state: GameState): String? = when {
        anyClimaxDueToday(state) -> QUIET_CASE
        Finales.windowAt(state) != null -> QUIET_FINALE
        state.holidayId?.let { HolidayBeats.beatFor(it) } != null ->
            QUIET_HOLIDAY
        FieldTrip.isTripDay(state) -> QUIET_TRIP
        else -> null
    }

    /** The honest line for a quiet day. */
    fun quietLine(state: GameState): String? = when (quietReason(state)) {
        QUIET_CASE -> "🎓 Quiet day at the Elementary — there's " +
            "case business in the air, and even the fourth grade " +
            "can smell it. Lessons resume when the town settles."
        QUIET_FINALE -> "🎓 Quiet day at the Elementary — it's " +
            "finale week, and the whole town (this school " +
            "included) is holding its breath."
        QUIET_HOLIDAY -> "🎓 Quiet day at the Elementary — " +
            "${HolidayBeats.beatFor(state.holidayId ?: "")?.title ?: "the holiday"} " +
            "has the day. School business keeps."
        QUIET_TRIP -> "🎓 Field trip day at the Elementary — " +
            "the fourth grade is out of the building on a " +
            "bus, and the day in here is a quiet study day " +
            "for anyone who stayed. Lessons and the bee " +
            "keep till the next school day."
        else -> null
    }

    // ---------------- presence ----------------

    /** Who is actually in the room: cast standing at the
     *  Elementary on the player's side of the door, awake and
     *  settled — the same truthfulness the movie house reads.
     *  Reactions, sprinkles and recess parties are drawn from
     *  THIS list and no other. */
    fun presentAt(state: GameState): List<String> =
        state.agents.values
            .filter {
                !it.isPlayer && it.location == "Elementary" &&
                    it.waypoints.isEmpty() && !it.asleep &&
                    CharacterSystem.npcInside(it.location) ==
                        state.playerInside
            }
            .map { it.name }
            .sorted()

    /** The kids in the room (the class itself). */
    fun presentKids(state: GameState): List<String> =
        presentAt(state).filter { it in CharacterSystem.CAST_KIDS }

    // ---------------- little shared helpers ----------------

    private fun lcg(seed: Long): Long {
        var x = seed * 6364136223846793005L + 1442695040888963407L
        x = x xor (x ushr 33)
        return x and Long.MAX_VALUE
    }

    private fun lcgPick(seed: Long, n: Int): Int =
        if (n <= 0) 0 else (lcg(seed) % n).toInt()

    private fun bumpFriendship(state: GameState, name: String, delta: Int) {
        if (delta == 0) return
        val cur = state.friendship[name] ?: 0
        state.friendship[name] = (cur + delta).coerceIn(-50, 100)
    }

    private fun note(state: GameState, line: String) {
        state.events.add(line)
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, line)
    }

    /** Plant the "school" thread — the memory the town already
     *  knows how to follow up (Threads has the bank). */
    private fun plantSchoolTopic(name: String) {
        val mem = NpcMemoryStore.getOrCreate(name)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** This school day's short seed material, shared by the
     *  room's reactions so a day has ONE set of moments. */
    private fun daySeed(absDay: Int): Long = absDay * 1000003L + 77L

    // ================= THE LESSON =================

    /** One day's lesson: an opening beat, a development beat,
     *  the participation question — and the three kindly
     *  outcomes. The right-ish answer is always something the
     *  develop beat just taught; the funny one is wrong on
     *  purpose; the pass is always safe. */
    data class Lesson(
        val id: String, val title: String,
        val open: String, val develop: String,
        val question: String,
        val rightLabel: String, val rightOutcome: String,
        val funnyLabel: String, val funnyOutcome: String,
        val passOutcome: String
    )

    val TOPICS: List<Lesson> = listOf(
        Lesson(
            "water_cycle", "The Water Cycle (Condensed)",
            "opens with a drawing of a cloud that took " +
                "fourteen tries. \"Class. The water cycle. You've " +
                "had this lesson before. You will have it again. " +
                "That IS the lesson, in a way.\"",
            "walks it through with the pointer: the sun " +
                "lifts the water up, the cloud carries it around " +
                "town like it owns the place, and then the cloud " +
                "lets go. \"Clouds are just the sky holding its " +
                "drink. Rain is the sky setting it down.\"",
            "\"So. Where does rain come from? Hands. Actual hands, " +
                "not whatever Craig is doing.\"",
            "Clouds letting go of the water they picked up.",
            "\"Correct. Somebody write that down — oh. YOU just " +
                "did, by saying it. Class dismissed on the subject " +
                "of rain.\" Pride, from the front of the room, is a " +
                "rare and beautiful weather event.",
            "The sky is crying because of something YOU did.",
            "The class loses it completely. \"Incorrect — and yet, " +
                "the most memorable answer of the school year. It's " +
                "going on the board. It's STAYING on the board.\" " +
                "It stays on the board.",
            "\"Noted, and noted respectfully. Taking the " +
                "question under advisement is a life skill this " +
                "town runs on.\" A small, approving nod — from the " +
                "teacher, and weirdly, from Mr. Hat's direction " +
                "too."
        ),
        Lesson(
            "fractions", "Fractions, or: Pizza Math",
            "begins by confiscating an imaginary pizza. " +
                "\"Fractions are just pizza with consequences. " +
                "Everyone understands pizza. Therefore everyone " +
                "understands fractions.\"",
            "cuts the imaginary pizza into smaller and " +
                "smaller pieces with enormous ceremony. \"A half of " +
                "a half. Pay attention, this is the whole exam: a " +
                "quarter. A quarter of a pizza is a tragedy at a " +
                "party and a triumph alone.\"",
            "\"Pop quiz, no stakes, dignity only: what is half " +
                "of a half?\"",
            "A quarter.",
            "\"A quarter. Correct. You may now do fractions at " +
                "restaurants, which is where fractions actually " +
                "live.\" The imaginary pizza is, briefly, yours.",
            "A snack, if you're fast.",
            "The room HOWLS. \"Wrong — halves aren't speed-based, " +
                "we've BEEN over this—\" but there's respect in it. " +
                "Half the class is now saying 'a snack, if you're " +
                "fast' like it's scripture.",
            "\"The dignity-preserving pass. History will record " +
                "that you were offered pizza math and chose peace.\" " +
                "Your slice of the imaginary pizza remains, forever, " +
                "unfractioned."
        ),
        Lesson(
            "volcano", "Volcanoes: The Earth's Pimples",
            "produces a volcano model from under the desk " +
                "with the air of a magician who has done this too " +
                "many times. \"Science. Watch closely. Nobody " +
                "touch anything.\"",
            "loads the model with the traditional " +
                "ingredients. It erupts — mildly. Extremely mildly. " +
                "\"Pressure, class. Underneath, it builds and " +
                "builds, until it can't. That's the whole volcano. " +
                "That's also most of this town's problems, frankly.\"",
            "\"So why does it erupt? Short answers. The model is " +
                "still warm.\"",
            "Pressure builds underneath until it can't.",
            "\"Pressure. Correct. Science works, class — put " +
                "THAT in the newsletter.\" The volcano is " +
                "ceremonially retired to the windowsill, a hero.",
            "It's mad. It saw the report card.",
            "A beat of silence, then the room commits to the " +
                "bit: two kids start slow-clapping. \"False — and " +
                "anthropologically fascinating. The volcano does " +
                "NOT grade you. I grade you. Worse for everyone.\"",
            "\"A scientist's silence. The eruption respects it. " +
                "I respect it.\" The lesson moves on with the " +
                "dignity of a man who has survived forty of these."
        ),
        Lesson(
            "current_events", "Current Events: The Town Edition",
            "slaps the morning paper onto the desk. " +
                "\"Current events. Everything in this paper " +
                "happened within walking distance of this room, " +
                "which is the South Park difference.\"",
            "reads three headlines aloud and dissects " +
                "them like frogs. \"Every story has a teller. " +
                "Every teller wants something. Rule one of current " +
                "events: ask who's telling it, and what they'd " +
                "like you to feel about it.\"",
            "\"Rule one. Somebody give me rule one back.\"",
            "Ask who's telling it, and what they want.",
            "\"Rule one, word perfect. You're all dismissed " +
                "before the paper can age into history class.\" " +
                "Somewhere, a journalist feels a sudden chill.",
            "Panic first. Verify later. In that order.",
            "The class approves audibly. \"That is, tragically, " +
                "how most adults do it — which is why it's the " +
                "FUNNY answer and not the right one. I'm putting " +
                "it in the newsletter anyway.\"",
            "\"No comment — the official position of every " +
                "public figure in this paper. A natural. You'll " +
                "go far, carefully.\" The paper is folded away " +
                "with something like reverence."
        ),
        Lesson(
            "spelling_list", "The Spelling List (Hard Words Edition)",
            "writes a word on the board in letters two " +
                "feet tall. \"Spelling. The bee is Friday, and " +
                "some of you are going to learn this list the " +
                "easy way or the public way.\"",
            "underlines the hard one twice. \"'Necessary.' " +
                "One collar, two socks — one C, two S's. A shirt " +
                "has one collar and two socks go in the wash. " +
                "Nobody forgets a shirt. Nobody who's been told " +
                "about the shirt forgets.\"",
            "\"Necessary. The S count. How many, and show your " +
                "shirt-based working.\"",
            "Two — one collar, two socks.",
            "\"Two S's, shirt-certified. The bee judges accept " +
                "shirt-based working; I've checked, twice, with " +
                "visible reluctance on their part.\"",
            "All of them. Every S. Deploy all the S's.",
            "\"MAXIMUM S's\" gets written on the board in the " +
                "big letters, because some lessons are about " +
                "spelling and some are about commitment. \"Wrong. " +
                "Magnificently, memorably wrong.\"",
            "\"The pass, taken like a professional. The word " +
                "keeps its secrets; so do you. A fair trade, " +
                "honestly negotiated.\" The shirt mnemonic stays " +
                "on the board for the next class."
        ),
        Lesson(
            "gravity", "Gravity: It Isn't Personal",
            "holds a book at arm's length and drops it. " +
                "It lands like it has somewhere to be. \"Gravity. " +
                "Not personal. Not negotiable. Ask the book.\"",
            "drops two things at once — heavy, light — " +
                "and they land TOGETHER, to genuine classroom " +
                "murmurs. \"Same pull. Big, small, rich, famous — " +
                "gravity pulls everything the same. It's the only " +
                "fully fair system in this entire town.\"",
            "\"The bowling ball and the marble, dropped " +
                "together. Does the ball fall faster?\"",
            "No — gravity pulls them the same.",
            "\"The same. Correct. You've just agreed with " +
                "physics, which is more than most town meetings " +
                "manage.\" The book is dropped once more, for the " +
                "road, and lands correctly.",
            "Yes. The ball has more confidence.",
            "\"CONFIDENCE-BASED GRAVITY\" ends up on the board " +
                "in full caps. \"False, obviously — but if physics " +
                "worked that way, this class would be untouchable.\" " +
                "A small bow from the front of the room.",
            "\"Silent on gravity. Gravity respects that. It's " +
                "the only way anyone has ever beaten it: not " +
                "rising to it.\" Somewhere in the back, a kid " +
                "writes that down completely straight-faced."
        ),
        Lesson(
            "maps", "Maps: You Are Here, Unfortunately",
            "unrolls a town map held together by hope " +
                "and two magnets. \"Geography. The blue squiggle " +
                "is the river. The river has been on every exam " +
                "since 1987.\"",
            "points out the landmarks with the pointer: " +
                "the pond, the school, the whole town laid out " +
                "flat and innocent. \"North is up ON THE MAP. In " +
                "real life, north is wherever it wants — the map " +
                "is a polite fiction, but a consistent one.\"",
            "\"The blue squiggle crossing the whole town. " +
                "Name it. Name what's on every map ever.\"",
            "The river. It's on every map ever.",
            "\"The river. Correct, obviously correct, and yet " +
                "someone fails it every year and it ruins my " +
                "whole Sept— my whole week. Not you. Today, not " +
                "you.\"",
            "A snake that pays taxes.",
            "The pointer comes down. The room goes up. \"A " +
                "TAXPAYING SNAKE is joining the board of wrong " +
                "answers, which, I want it noted, is becoming " +
                "the best part of my curriculum.\"",
            "\"Declining to navigate. Half this town navigates " +
                "exactly that way and functions beautifully. " +
                "Mostly. Eventually.\" The map is rolled up like " +
                "a treaty."
        ),
        Lesson(
            "dinosaurs", "Dinosaurs: Ruled, Then Didn't",
            "produces a plastic dinosaur from the desk " +
                "drawer, where it lives, and sets it before the " +
                "class like a tiny king. \"History. Deep history. " +
                "The deepest.\"",
            "mimes the rock. You had to be there: the " +
                "hand comes down out of the sky and the tiny king " +
                "falls over with enormous dignity. \"Dinosaurs " +
                "ran the world. Then a rock from space ended the " +
                "meeting. Nobody saw it coming. Nobody ever does.\"",
            "\"So what ended the dinosaurs? Respectful tones " +
                "for the fallen.\"",
            "An asteroid — a rock from space.",
            "\"An asteroid. Correct. The king is stood back " +
                "up, because in this class the dinosaurs get a " +
                "rematch: they're the best part of the museum " +
                "and everyone knows it.\"",
            "They unionized, and the sky objected.",
            "\"THE SKY OBJECTED\" — onto the board it goes, " +
                "right under the tiny king. \"False, and if the " +
                "museum offered me that exhibit I would simply " +
                "retire. Retire happy, but retire.\"",
            "\"Some silences are respect for the extinct. " +
                "Noted, class — the fossil record of this lesson " +
                "will show a respectful quiet where an answer " +
                "might have been.\""
        ),
        Lesson(
            "plants", "Plants Eat Sunlight (Rude)",
            "sets a plant on the desk. The plant has " +
                "clearly been through some things. \"Biology. " +
                "This plant has survived three classes, two " +
                "holidays, and Randy's cousin's wedding somehow.\"",
            "turns the plant toward the window like it's " +
                "television. \"Leaves. Solar panels that grew " +
                "themselves. The leaf takes the sunlight and " +
                "makes the plant's food out of it. Photosynthesis " +
                "— a big Greek word for doing lunch properly.\"",
            "\"The leaves. The sunlight. What's the leaf " +
                "DOING with it?\"",
            "Making the plant's food from it.",
            "\"Food. Correct. While you were learning that, " +
                "the plant did it forty times. Show-off. Class — " +
                "respect the hustle.\" The plant is turned back " +
                "around, unbothered, lunching.",
            "Tanning. Plants are just out there tanning.",
            "\"PLANTS IS TANNING\" — the board of wrong " +
                "answers gains its most confident entry yet. " +
                "\"Wrong, and I will be thinking about it during " +
                "my entire commute, so thank you for that.\"",
            "\"A quiet photosynthesis of an answer: present, " +
                "unspoken, doing the work anyway. The plant " +
                "approves. The plant approves of everything; " +
                "it's having lunch.\""
        ),
        Lesson(
            "civics", "Civics: How a Town Decides",
            "wheels in on the wheeled chair for the " +
                "duration of exactly one sentence. \"Civics. The " +
                "town votes on things. The voting is the boring " +
                "part. The LIVING WITH IT is the curriculum.\"",
            "walks the class through a vote like a " +
                "weather report: the question, the arguing, the " +
                "counting, and then — the long part. \"After the " +
                "town votes, the winner STANDS. For weeks. On the " +
                "walls, in the yards, at the pond. Democracy has " +
                "a long tail, class.\"",
            "\"After the count, then. What happens to the " +
                "result?\"",
            "It stands, and the town lives with it.",
            "\"Stands. Correct. You may have noticed the " +
                "town's yards lately — that's civics, growing " +
                "in rows.\" A nod to the window, where the " +
                "democracy is visibly visible.",
            "We vote again until Cartman wins.",
            "The class, to a kid, repeats it back like a " +
                "pledge. \"That is NOT the system — that is a " +
                "simulation of the system run by one boy — ON " +
                "THE BOARD. Wrong answer of the month. Easily.\"",
            "\"Abstaining: the load-bearing wall of " +
                "democracy. The town thanks you for holding the " +
                "building up by standing still in it.\" The " +
                "lesson is adjourned constitutionally."
        ),
        Lesson(
            "teeth", "Your Body: The Teeth Keep Score",
            "holds up a dental model the size of a " +
                "grapefruit, which raises more questions than " +
                "any lesson should. \"Health. Open wide. " +
                "Metaphorically.\"",
            "brushes the enormous teeth with an enormous " +
                "toothbrush. \"Twice a day. Morning, night. The " +
                "teeth keep score, class — they keep score like " +
                " grandmas, forever, and they TELL. Brush twice " +
                "and the score stays friendly.\"",
            "\"How often do the teeth want brushing? Asking " +
                "for the grapefruit.\"",
            "Twice a day — and they keep score.",
            "\"Twice a day. Correct. The grapefruit is " +
                "pleased. The grapefruit's pleasure is the " +
                "highest honor this class awards.\" The dental " +
                "model is retired with full honors.",
            "Once. Really well. In 2019.",
            "\"2019\" is written on the board and circled " +
                "three times. \"A vintage year for dental " +
                "confidence. False — but somewhere, a dentist " +
                "just felt a disturbance and smiled.\"",
            "\"The teeth will never know you were asked. " +
                "That's the beautiful thing about the pass: the " +
                "score remains, between you and the grapefruit, " +
                "perfectly even.\""
        ),
        Lesson(
            "the_moon", "The Moon Is Not Following You",
            "draws a moon on the board with the confidence " +
                "of a man who has won this argument against a car " +
                "full of children. \"Astronomy. Settle an old " +
                "score for me.\"",
            "explains it with the globe and a flashlight " +
                "and the patience of centuries: the moon is FAR. " +
                "\"So far that driving across town barely changes " +
                "the angle. It doesn't follow the car. It just " +
                "looks like it does, because far things cheat.\"",
            "\"Last chance to get this right before you all " +
                "have cars: why does the moon follow them?\"",
            "It doesn't — it's just very far away.",
            "\"It doesn't. Correct. One day, driving at " +
                "night, you'll remember this lesson and feel " +
                "briefly superior to the sky. That's the whole " +
                "point of education.\"",
            "It has questions. About the car.",
            "\"THE MOON HAS QUESTIONS\" joins the board, " +
                "where it will outlive us all. \"False — the " +
                "moon has ANSWERS and keeps them to itself, " +
                "like a professional. Unlike some classes.\"",
            "\"Quiet under the moon. The moon, which is not " +
                "following you, respects the silence. The class " +
                "is dismissed into the daylight, moonless and " +
                "free.\""
        ),
        Lesson(
            "money", "Money: Grown-Up Chore Points",
            "empties an entirely empty wallet onto the " +
                "desk with theatrical gravity. \"Economics. This " +
                "is a teaching wallet. The teaching wallet is " +
                "empty, which is the first lesson.\"",
            "writes BUDGET on the board and underlines " +
                "it like a warning. \"A budget is a promise you " +
                "make to yourself on a Monday. What breaks it " +
                "isn't the big stuff — it's the little stuff " +
                "nobody wrote down, spending itself quietly, " +
                "like mice.\"",
            "\"What breaks a budget first? Real answers, " +
                "scarce in economics.\"",
            "The little stuff nobody wrote down.",
            "\"The little stuff. Correct. Somewhere out " +
                "there are grown-ups paying full price to learn " +
                "this exact lesson. You got it free, before " +
                "lunch.\" The teaching wallet is closed, poorer " +
                "in nothing.",
            "Shoes. It's always shoes.",
            "\"IT IS ALWAYS SHOES\" — the board accepts its " +
                "most economically literate wrong answer. \"False " +
                "in general. Devastatingly true in several " +
                "specific households I could name and won't.\"",
            "\"No position on money. In this town, that is " +
                "either great wisdom or a great disguise, and " +
                "either way the budget survives you.\" The " +
                "wallet returns to its pocket, undefeated."
        ),
        Lesson(
            "poetry", "Poetry: Meaning It Is the Rhyme",
            "dims nothing — the lights don't dim — but " +
                "speaks like they have. \"Poetry. Groan audibly " +
                "if you must. Get it out of your systems. " +
                "There. Poetry.\"",
            "reads one short poem aloud, all the way " +
                "through, without a single joke, and lets the " +
                "quiet sit. \"A poem doesn't have to rhyme, " +
                "class. Rhyme is shoes. A poem has to MEAN it — " +
                "that's the whole skeleton. Everything else is " +
                "tailoring.\"",
            "\"So what does a poem actually NEED? And no, " +
                "before anyone starts: not darkness. Not glue.\"",
            "To mean it. Rhyme's just shoes.",
            "\"To mean it. Correct — and somebody in the " +
                "corner there just felt something and will deny " +
                "it beautifully, in verse, one day. That's the " +
                "subject working as intended.\"",
            "A rhyme for 'orange'. Especially 'orange'.",
            "A hush. Then: \"I have waited nine years for a " +
                "student brave enough to say that out loud. " +
                "WRONG — and the wrongness is load-bearing. " +
                "Board. Full caps. Forever.\"",
            "\"A poem-shaped silence, offered instead of a " +
                "poem. The goth kids in the back are writing " +
                "that down. That, class, is what influence " +
                "looks like.\""
        )
    )

    /** The topic rotation: a stride over the bank by school
     *  days — every school day a new topic, no repeat inside
     *  any two school weeks, the whole bank inside three. */
    fun topicFor(absDay: Int): Lesson {
        val idx = schoolDayIndex(absDay)
        val stride = 5 // gcd(5, 14) = 1: the bank fully cycles
        return TOPICS[(idx * stride + 3) % TOPICS.size]
    }

    fun topicById(id: String): Lesson? = TOPICS.firstOrNull { it.id == id }

    // ---------------- lesson presence reactions ----------------

    /** In-character one-beat reactions, signature first — and
     *  ONLY ever from kids actually in the room. Craig's
     *  finger, Tweek's panic, Jimmy's act: the canon beats,
     *  keyed to the kids who own them. */
    private val SIGNATURE_REACTIONS: List<Pair<String, String>> =
        listOf(
            "Craig" to "Craig raises one finger. It is THE finger. " +
                "It is also, technically, a raised hand, and the " +
                "room treats it with the ceremony of both.",
            "Tweek" to "Tweek vibrates at a frequency only dogs " +
                "and teachers can grade. \"TOO MUCH CURRICULUM!\" " +
                "he reports, to no one, at volume.",
            "Jimmy" to "Jimmy, from the back: \"Wh-what a great— " +
                "great audie— CLASS. Great class.\" He bows to the " +
                "desks. The desks allow it.",
            "Cartman" to "Cartman is taking notes. The notes say " +
                "'screw this' in twelve fonts, three of them " +
                "calligraphy. He shows them to Kyle on principle.",
            "Wendy" to "Wendy's hand was up before the question " +
                "existed. It is up now. It will remain up until " +
                "the curriculum respects it.",
            "Kyle" to "Kyle is fact-checking the lesson against " +
                "a book he brought from home for this exact " +
                "purpose. So far the lesson is holding. Barely.",
            "Kenny" to "(muffled) Kenny contributes at length. " +
                "The class writes it down. It will be on the " +
                "test. Nobody knows why. It always is.",
            "Butters" to "Butters nods along like the lesson is " +
                "a lullaby his grandma sings him. He whispers " +
                "\"oh boy\" at the key point, correctly.",
            "Stan" to "Stan stares out the window with the focus " +
                "of a kid in a painting about staring out windows. " +
                "Then he writes something down, decisively.",
            "Clyde" to "Clyde gasps at the lesson's mildest " +
                "revelation like it's a season finale. Two kids " +
                "shush him. He gasps quieter, no less sincere.",
            "Tolkien" to "Tolkien raises his hand with the calm " +
                "of a kid who read the chapter AND the footnotes, " +
                "and is choosing mercy.",
            "Bebe" to "Bebe is running a full annotation system " +
                "in the margins — stars, underlines, one doodle " +
                "that will be explained to NO ONE.",
            "Heidi" to "Heidi takes notes in actual shorthand " +
                "she invented last week. Future Heidi will either " +
                "be delighted or betrayed; it's fifty-fifty.",
            "Red" to "Red has a question face so powerful it " +
                "functions as a question. The lesson adjusts for " +
                "it mid-sentence.",
            "Karen" to "Karen is copying everything down twice: " +
                "once for her, once for later, in handwriting " +
                "neater than the board's."
        )

    private val GENERIC_REACTIONS: List<String> = listOf(
        "{name} takes notes so hard the pencil files a " +
            "workplace complaint.",
        "{name} mouths the key word along with the teacher, " +
            "the way people do at concerts and referendums.",
        "{name}'s hand hovers, commits, hovers again — a " +
            "full parliamentary process in one arm.",
        "{name} whispers \"oh, that's actually true\" in the " +
            "tone of a kid betrayed by a fact.",
        "{name} is quietly blown away and attempting, with " +
            "total failure, to play it cool."
    )

    /** Up to two reaction lines for a lesson beat, drawn ONLY
     *  from the kids present — signature kids first when
     *  they're in the room. Empty when the room is empty (a
     *  fabricated state, never a real class hour). */
    fun reactionLines(state: GameState): List<String> {
        val kids = presentKids(state)
        if (kids.isEmpty()) return emptyList()
        val seed = daySeed(state.absDay)
        val out = mutableListOf<String>()
        val sig = SIGNATURE_REACTIONS.filter { it.first in kids }
        if (sig.isNotEmpty()) {
            out += sig[lcgPick(seed + 5, sig.size)].second
        }
        val rest = kids.filter { name ->
            SIGNATURE_REACTIONS.none { it.first == name }
        }
        if (rest.isNotEmpty() && (lcg(seed + 9) % 2L == 0L || out.isEmpty())) {
            val who = rest[lcgPick(seed + 13, rest.size)]
            out += GENERIC_REACTIONS[lcgPick(seed + 17,
                GENERIC_REACTIONS.size)].replace("{name}", who)
        }
        return out
    }

    // ---------------- the lesson session ----------------

    const val LS_NOT_SCHOOL_DAY: String = "NOT_SCHOOL_DAY"
    const val LS_QUIET: String = "QUIET"
    const val LS_BEFORE: String = "BEFORE"
    const val LS_RECESS: String = "RECESS"
    const val LS_AVAILABLE: String = "AVAILABLE"
    const val LS_IN_SESSION: String = "IN_SESSION"
    const val LS_DONE: String = "DONE"
    const val LS_AFTER: String = "AFTER"

    data class LessonStatus(
        val kind: String, val topic: Lesson, val teacher: String)

    fun lessonStatus(state: GameState): LessonStatus {
        val topic = topicFor(state.absDay)
        val teacher = teacherFor(state.absDay)
        val kind = when {
            !isSchoolDay(state.absDay) -> LS_NOT_SCHOOL_DAY
            quietReason(state) != null -> LS_QUIET
            state.life["schoolSeat:${state.absDay}"] == 1 -> LS_IN_SESSION
            recordFor(state, state.absDay) != null -> LS_DONE
            state.timeMin in RECESS_FROM until RECESS_TO -> LS_RECESS
            state.timeMin < CLASS_FROM -> LS_BEFORE
            state.timeMin >= CLASS_TO -> LS_AFTER
            else -> LS_AVAILABLE
        }
        return LessonStatus(kind, topic, teacher)
    }

    /** Take the seat. Gates exactly like the status reads;
     *  resume-safe (a session already open just continues). */
    fun startLesson(state: GameState): Boolean {
        val st = lessonStatus(state)
        if (st.kind == LS_IN_SESSION) return true
        if (st.kind != LS_AVAILABLE) return false
        state.life["schoolSeat:${state.absDay}"] = 1
        state.life["schoolStep:${state.absDay}"] = 0
        return true
    }

    data class LessonBeat(
        val kind: String, // "text" | "prompt" | "outcome"
        val speaker: String, val text: String)

    /** The current beat of the open lesson, or null when no
     *  session is open. Steps: 0 open, 1 develop + the room's
     *  reactions, 2 the participation prompt, 3 the outcome
     *  (answer* writes it; advancing past it finishes). */
    fun lessonBeat(state: GameState): LessonBeat? {
        val day = state.absDay
        if (state.life["schoolSeat:$day"] != 1) return null
        val topic = topicFor(day)
        val teacher = teacherFor(day)
        return when (state.life["schoolStep:$day"] ?: 0) {
            0 -> LessonBeat("text", teacher,
                "$teacher ${topic.open}")
            1 -> {
                val reacts = reactionLines(state)
                val room = if (reacts.isEmpty()) ""
                    else "\n\n" + reacts.joinToString("\n")
                LessonBeat("text", teacher,
                    "$teacher ${topic.develop}" + room)
            }
            2 -> LessonBeat("prompt", teacher,
                "$teacher ${topic.question}")
            else -> {
                val choice = state.lifeText["schoolChoice:$day"]
                LessonBeat("outcome", teacher, when (choice) {
                    "right" -> topic.rightOutcome
                    "funny" -> topic.funnyOutcome
                    else -> topic.passOutcome
                })
            }
        }
    }

    /** The participation row: the right-ish answer, the funny
     *  wrong one, and the pass — order rotated by day so the
     *  right one isn't always sitting in the same seat. */
    fun lessonOptions(state: GameState): List<Pair<String, String>> {
        val topic = topicFor(state.absDay)
        val all = listOf(
            "right" to topic.rightLabel,
            "funny" to topic.funnyLabel,
            "pass" to "✋ Pass — just take notes")
        val rot = state.absDay % all.size
        return all.drop(rot) + all.take(rot)
    }

    /** Judge the participation, kindly: the sprinkle lands
     *  for everyone in the room whatever you chose; a
     *  right-ish answer earns the teacher's own bump too.
     *  Passing is always safe. Plants the "school" thread in
     *  the teacher and a few of the room. */
    fun answerLesson(state: GameState, choiceId: String): Boolean {
        val day = state.absDay
        if (state.life["schoolSeat:$day"] != 1) return false
        if ((state.life["schoolStep:$day"] ?: 0) != 2) return false
        if (choiceId !in setOf("right", "funny", "pass")) return false
        state.lifeText["schoolChoice:$day"] = choiceId
        state.life["schoolStep:$day"] = 3
        // the sprinkle: the classmates actually in the room
        val kids = presentKids(state).take(SPRINKLE_CAP)
        for (kid in kids) bumpFriendship(state, kid, LESSON_PAY)
        val teacher = teacherFor(day)
        if (choiceId == "right") bumpFriendship(state, teacher, TEACHER_PAY)
        plantSchoolTopic(teacher)
        for (kid in kids.take(3)) plantSchoolTopic(kid)
        return true
    }

    /** Step forward; advancing past the outcome finishes the
     *  lesson and writes the record. Exactly once per day. */
    fun advanceLesson(state: GameState) {
        val day = state.absDay
        if (state.life["schoolSeat:$day"] != 1) return
        val step = state.life["schoolStep:$day"] ?: 0
        if (step < 3) {
            state.life["schoolStep:$day"] = step + 1
            return
        }
        // finish: the record, the counter, the ticker
        val topic = topicFor(day)
        val choice = state.lifeText["schoolChoice:$day"] ?: "pass"
        writeLessonRecord(state, LessonRecord(day, topic.id, choice))
        state.life[K_LESSONS] = lessonsSat(state) + 1
        note(state, "🎓 You sat ${teacherFor(day)}'s lesson: " +
            "“${topic.title}”.")
        state.life.remove("schoolSeat:$day")
        state.life.remove("schoolStep:$day")
        state.lifeText.remove("schoolChoice:$day")
    }

    // ---------------- lesson records ----------------

    data class LessonRecord(val day: Int, val topicId: String,
                            val choice: String)

    private fun writeLessonRecord(state: GameState, rec: LessonRecord) {
        val kept = lessonHistory(state).filter { it.day != rec.day } + rec
        state.lifeText[HIST_LESSONS] = kept.takeLast(HISTORY_CAP)
            .joinToString(";") { "${it.day}|${it.topicId}|${it.choice}" }
    }

    fun lessonHistory(state: GameState): List<LessonRecord> {
        val raw = state.lifeText[HIST_LESSONS] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            val p = e.split("|")
            if (p.size != 3) null
            else LessonRecord(p[0].toIntOrNull() ?: return@mapNotNull null,
                p[1], p[2])
        }
    }

    fun recordFor(state: GameState, day: Int): LessonRecord? =
        lessonHistory(state).firstOrNull { it.day == day }

    fun lessonsSat(state: GameState): Int = state.life[K_LESSONS] ?: 0

    /** The choice mark the Journal prints: ✓ sat it and knew
     *  it, 🎭 the funny answer, ✋ the honorable pass. */
    fun choiceMark(choice: String): String = when (choice) {
        "right" -> "✓"; "funny" -> "🎭"; else -> "✋"
    }

    // ================= RECESS =================

    /** One yard dispute, refereed on the existing verbs. The
     *  parties ({a} and {b}) are filled from the kids actually
     *  in the school at midday; the fair call pays both, a
     *  sided call pays its side. Kindly judged, like class. */
    data class RecessOption(
        val id: String, val label: String, val outcome: String,
        val payA: Int, val payB: Int)

    data class RecessBeat(
        val id: String, val title: String, val setup: String,
        val snowOnly: Boolean, val options: List<RecessOption>)

    val RECESS_BEATS: List<RecessBeat> = listOf(
        RecessBeat(
            "four_square", "The Four-Square Ruling",
            "Four-square, recess rules, king's court: {a} " +
                "swears the ball was OUT. {b} swears it was IN, " +
                "and {b}'s whole bloodline is apparently riding " +
                "on it. A crowd has formed the way crowds do: " +
                "instantly, and with charts.",
            false,
            listOf(
                RecessOption("replay",
                    "🏐 Replay the point — new ball, no kings",
                    "You deliver the only verdict recess has " +
                        "respected since records began: DO-OVER. " +
                        "New ball. No kings. The crowd accepts it " +
                        "with the gravity of a supreme court and " +
                        "twice the legitimacy. {a} and {b} shake " +
                        "on it like nations.",
                    2, 2),
                RecessOption("side_a",
                    "📣 It was OUT — {a} is right",
                    "You call it out, with authority and a " +
                        "pointed finger. {a} ascends to the king's " +
                        "court like royalty. {b} accepts the ruling " +
                        "the way nations do: publicly, completely, " +
                        "and with a grudge scheduled for Thursday.",
                    2, 0),
                RecessOption("side_b",
                    "📣 It was IN — {b} is right",
                    "You call it in. {b} is vindicated so hard it " +
                        "echoes. {a} studies the line in the snow-" +
                        "dust like it personally betrayed the crown, " +
                        "which, in four-square terms, it absolutely " +
                        "did.",
                    0, 2))),
        RecessBeat(
            "snowball_truce", "The Snowball Truce",
            "At the fence line, {a} and {b} are mid-snowball-" +
                "war: two forts, one contested mitten, and a " +
                "ceasefire nobody with cold hands has the " +
                "authority to call. The next volley is already " +
                "being packed.",
            true,
            listOf(
                RecessOption("truce",
                    "☕ Broker a truce — hot-chocolate terms",
                    "You broker it like a tiny diplomat: forts " +
                        "stay, the mitten is shared on a rotating " +
                        "schedule with forms in triplicate, and hostilities " +
                        "convert to a snowman contest judged by " +
                        "no one. Both sides claim victory. Both " +
                        "sides are, somehow, right.",
                    2, 2),
                RecessOption("join_a",
                    "❄️ Join {a}'s side",
                    "You enlist. Your first snowball is a work " +
                        "of art; your second starts a legend; the " +
                        "third gets you remembered at this fence " +
                        "for years. {b} retreats in good order, " +
                        "already drafting the rematch treaty.",
                    2, 0),
                RecessOption("join_b",
                    "❄️ Join {b}'s side",
                    "You enlist. The fort accepts you after a " +
                        "rigorous interview (\"can you throw?\" " +
                        "\"yes\") and the tide turns within a " +
                        "minute. {a} withdraws with honors and a " +
                        "head full of snow and plans.",
                    0, 2))),
        RecessBeat(
            "note_passing", "The Note Relay",
            "A folded note — folded to within an inch of its " +
                "life, the way important documents are — needs " +
                "to cross the yard from {a} to {b} without " +
                "attracting teacher-grade attention. {a} looks " +
                "at you. The note looks at you. Metaphorically.",
            false,
            listOf(
                RecessOption("carry",
                    "✉️ Carry it yourself",
                    "You run the relay solo, casual as weather. " +
                        "The note arrives; {b} unfolds it with " +
                        "ceremony, reads it twice, and looks up " +
                        "with an expression that could power the " +
                        "school. Mission: invisible. Status: legend.",
                    1, 2),
                RecessOption("line",
                    "🤫 Pass it down the line",
                    "The note travels hand to hand like a rumor " +
                        "with a destination. Four kids deep, it " +
                        "arrives unopened — recess honors the " +
                        "seal — and {b} mouths 'thank you' across " +
                        "the yard at the entire postal service.",
                    1, 1),
                RecessOption("read",
                    "👀 Read it first (you monster)",
                    "You read it. It is a drawing of a horse. " +
                        "The horse is magnificent. The horse has " +
                        "taken three recesses. You deliver the " +
                        "note AND a full art review, and {b} " +
                        "accepts both as the tribute they are.",
                    0, 2)))
    )

    /** Today's recess beat, pinned on first read so the yard
     *  doesn't reshuffle mid-lunch. The snowball truce only
     *  rotates in when there's snow to truce over. */
    fun recessBeatFor(state: GameState): RecessBeat {
        val day = state.absDay
        state.lifeText["recessBeat:$day"]?.let { id ->
            RECESS_BEATS.firstOrNull { it.id == id }?.let { return it }
        }
        val available = RECESS_BEATS.filter {
            !it.snowOnly || state.weatherId in SNOW_WEATHERS
        }
        val beat = available[lcgPick(daySeed(day) + 41, available.size)]
        state.lifeText["recessBeat:$day"] = beat.id
        return beat
    }

    /** The two kids the yard produces for today's beat —
     *  drawn deterministically from the kids actually in the
     *  school at midday. Null when the yard can't field two
     *  (fabricated states; a real midday never runs dry). */
    fun recessParties(state: GameState): Pair<String, String>? {
        val kids = presentKids(state)
        if (kids.size < 2) return null
        val seed = daySeed(state.absDay)
        val i = lcgPick(seed + 43, kids.size)
        var j = lcgPick(seed + 47, kids.size - 1)
        if (j >= i) j++
        return kids[i] to kids[j]
    }

    const val RS_NOT_SCHOOL_DAY: String = "NOT_SCHOOL_DAY"
    const val RS_QUIET: String = "QUIET"
    const val RS_NOT_MIDDAY: String = "NOT_MIDDAY"
    const val RS_DONE: String = "DONE"
    const val RS_EMPTY_YARD: String = "EMPTY_YARD"
    const val RS_AVAILABLE: String = "AVAILABLE"

    data class RecessStatus(
        val kind: String, val beat: RecessBeat?,
        val a: String?, val b: String?)

    fun recessStatus(state: GameState): RecessStatus {
        val beat = recessBeatFor(state)
        return when {
            !isSchoolDay(state.absDay) ->
                RecessStatus(RS_NOT_SCHOOL_DAY, beat, null, null)
            quietReason(state) != null ->
                RecessStatus(RS_QUIET, beat, null, null)
            state.timeMin !in RECESS_FROM until RECESS_TO ->
                RecessStatus(RS_NOT_MIDDAY, beat, null, null)
            state.life["recessDone:${state.absDay}"] == 1 ->
                RecessStatus(RS_DONE, beat, null, null)
            else -> {
                val (a, b) = recessParties(state)
                    ?: return RecessStatus(RS_EMPTY_YARD, beat, null, null)
                RecessStatus(RS_AVAILABLE, beat, a, b)
            }
        }
    }

    fun fillBeat(template: String, a: String, b: String): String =
        template.replace("{a}", a).replace("{b}", b)

    /** Referee the beat: applies the option's friendship to
     *  the two parties (who were read from real presence when
     *  the status was drawn), writes the day's record, plants
     *  the thread in both kids. Settles exactly once a day.
     *  Returns the outcome text, or null when not playable. */
    fun playRecess(state: GameState, optionId: String,
                   a: String, b: String): String? {
        val st = recessStatus(state)
        if (st.kind != RS_AVAILABLE) return null
        val beat = st.beat ?: return null
        val opt = beat.options.firstOrNull { it.id == optionId }
            ?: return null
        state.life["recessDone:${state.absDay}"] = 1
        state.life[K_RECESS] = recessCount(state) + 1
        bumpFriendship(state, a, opt.payA)
        bumpFriendship(state, b, opt.payB)
        plantSchoolTopic(a); plantSchoolTopic(b)
        val kept = recessHistory(state).filter {
            it.day != state.absDay } +
            RecessRecord(state.absDay, beat.id)
        state.lifeText[HIST_RECESS] = kept.takeLast(RECESS_HISTORY_CAP)
            .joinToString(";") { "${it.day}|${it.beatId}" }
        note(state, "🍎 Recess: ${beat.title} — refereed " +
            "between $a and $b.")
        return fillBeat(opt.outcome, a, b)
    }

    data class RecessRecord(val day: Int, val beatId: String)

    fun recessHistory(state: GameState): List<RecessRecord> {
        val raw = state.lifeText[HIST_RECESS] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            val p = e.split("|")
            if (p.size != 2) null
            else RecessRecord(p[0].toIntOrNull()
                ?: return@mapNotNull null, p[1])
        }
    }

    fun recessCount(state: GameState): Int = state.life[K_RECESS] ?: 0

    fun recessBeatById(id: String): RecessBeat? =
        RECESS_BEATS.firstOrNull { it.id == id }

    // ================= THE SPELLING BEE =================

    /** Bee day is Friday — Victoria's bee, Garrison's day off
     *  from judging (he judges the science fair; it's fair). */
    fun isBeeDay(absDay: Int): Boolean =
        Jobs.weekdayOf(absDay) == BEE_WEEKDAY

    /** The next bee day on or after [absDay]. */
    fun nextBeeDay(absDay: Int): Int {
        var d = absDay
        while (!isBeeDay(d)) d++
        return d
    }

    /** Each speller's data — the number the bracket rolls
     *  against. The sharp kids run deep because their number
     *  says so; the floor exists so nobody is immortal. */
    private val SKILLS: Map<String, Int> = mapOf(
        "Wendy" to 9, "Kyle" to 9, "Tolkien" to 8, "Nichole" to 8,
        "Stan" to 7, "Craig" to 6, "Kenny" to 6, "Bebe" to 6,
        "Annie" to 6, "Heidi" to 6, "Red" to 6, "Jimmy" to 5,
        "Clyde" to 5, "Kevin" to 5, "Scott" to 5, "Bradley" to 5,
        "Karen" to 5, "Michael" to 5, "Henrietta" to 5,
        "Cartman" to 4, "Butters" to 4, "Firkle" to 4,
        "DogPoo" to 4, "Francis" to 4, "Timmy" to 4, "Pete" to 4,
        // wave 4 (v0.8.0a): the teen reads out of spite and
        // wins rounds on it; her McCormick counterpart left
        // school behind years ago; Ike is three
        "Shelley" to 6, "Kevin McCormick" to 4, "Ike" to 2,
        // wave 5 (v0.9.0a): the schemer reads ahead and spells
        // like a threat; the new girl is bright and careful
        "Nathan" to 7, "Sophie" to 6,
        // wave 6 (v0.11.0d): the audit's kids — Pip reads
        // Dickens for pleasure; Jenny runs a committee like a
        // parliament; Terrance spells like a legacy; Jason is
        // quicker on the buzzer than the book; the kindergarten
        // trio spell at knee height; Mimsy spells like he
        // carries; Tricia like a Tucker who talks
        "Pip" to 6, "Dougie" to 4, "Jason" to 5,
        "Terrance Mephesto" to 6, "Jenny" to 6, "Flora" to 3,
        "Filmore" to 4, "Quaid" to 4, "Mimsy" to 3, "Tricia" to 5)

    fun skillOf(name: String): Int = SKILLS[name] ?: 5

    /** The word bank, tiered the way the bee tiers them: the
     *  early rounds are warm-ups; the late ones have teeth. */
    val WORD_TIERS: List<List<String>> = listOf(
        listOf("pond", "bell", "snow", "desk", "book",
            "lunch", "chalk", "ruler", "globe", "apple"),
        listOf("recess", "pencil", "winter", "monkey",
            "purple", "magnet", "rocket", "valley"),
        listOf("captain", "pyramid", "blanket", "thunder",
            "bicycle", "lantern", "harvest"),
        listOf("calendar", "umbrella", "dinosaur", "kangaroo",
            "mountain", "question", "asteroid", "skeleton"),
        listOf("necessary", "satellite", "avalanche",
            "nightmare", "porcupine", "chocolate", "crocodile",
            "librarian"))

    /** Round → word tier: a new length class about every
     *  round, capped at the monsters. */
    fun tierFor(round: Int): Int = (round - 1).coerceIn(0, WORD_TIERS.size - 1)

    /** Today's eight spellers: a seeded draw from the whole
     *  class — same Friday, same field, forever. */
    fun beeField(absDay: Int): List<String> {
        val rnd = Random(absDay * 7919L + 131L)
        return CharacterSystem.CAST_KIDS.sorted()
            .shuffled(rnd).take(FIELD_SIZE)
    }

    private fun beeSeed(absDay: Int, round: Int, name: String): Long =
        absDay * 1000003L + round * 10007L +
            (name.hashCode() and 0x7fffffff)

    /** Does this speller's word come apart in this round?
     *  Pure and seeded per (day, round, speller): the same
     *  bee always eliminates the same kids in the same
     *  rounds, whoever is watching. */
    fun kidFalls(absDay: Int, round: Int, name: String): Boolean {
        val p = (0.14 + 0.08 * (round - 1) -
            0.04 * (skillOf(name) - 5)).coerceIn(0.05, 0.80)
        return Random(beeSeed(absDay, round, name)).nextDouble() < p
    }

    fun kidWord(absDay: Int, round: Int, name: String): String {
        val bank = WORD_TIERS[tierFor(round)]
        return bank[Random(beeSeed(absDay, round, name) + 5)
            .nextInt(bank.size)]
    }

    /** The word the player draws in [round] — seeded by day
     *  and round alone, so a save-scummed bee deals the same
     *  words. Them's the rules, says the clipboard. */
    fun playerWord(absDay: Int, round: Int): String {
        val bank = WORD_TIERS[tierFor(round)]
        return bank[Random(absDay * 31L + round * 7L + 3L)
            .nextInt(bank.size)]
    }

    /** A plausible misspelling, for the narration: two
     *  letters swapped (or one dropped, on doubles). */
    fun misspell(word: String, seed: Long): String {
        if (word.length < 3) return word.dropLast(1)
        val i = (lcg(seed) % (word.length - 1)).toInt()
        val swapped = word.substring(0, i) + word[i + 1] + word[i] +
            word.substring(i + 2)
        return if (swapped != word) swapped
        else word.removeRange(i, i + 1)
    }

    data class BeeRound(
        val round: Int, val falls: List<String>,
        val dq: String?, val remainingAfter: List<String>)

    data class Bracket(
        val field: List<String>, val rounds: List<BeeRound>,
        val champion: String?)

    /** The whole kid bracket, simulated from the data alone:
     *  rounds until one speller (or none) remains, Cartman's
     *  disqualification in CHEAT_ROUND if he's still standing
     *  (he is eliminated OFF the rolls — the sleeve does not
     *  get a spelling attempt), and a judges' pick at the
     *  bell if MAX_ROUNDS runs out with a crowd still up. */
    fun beeBracket(absDay: Int): Bracket {
        val field = beeField(absDay)
        var remaining = field
        val rounds = mutableListOf<BeeRound>()
        var r = 1
        while (remaining.size > 1 && r <= MAX_ROUNDS) {
            val dq = if ("Cartman" in remaining && r == CHEAT_ROUND)
                "Cartman" else null
            val falls = remaining.filter {
                it != dq && kidFalls(absDay, r, it)
            }
            var next = remaining - falls - listOfNotNull(dq)
            var stuck = falls
            if (next.isEmpty()) {
                // Real bee rules: a round that would empty the
                // field eliminates nobody — the judges reinstate
                // the fallen and the bee spells on (a cheat's
                // disqualification still stands). Before v0.8.0a
                // a wipe round crowned nobody; at 27 kids the
                // seeded lottery hid it, at 30 it crowned nobody
                // a third of all Fridays.
                next = remaining - listOfNotNull(dq)
                stuck = emptyList()
            }
            remaining = next
            rounds += BeeRound(r, stuck, dq, remaining)
            r++
        }
        val champion = when {
            remaining.size == 1 -> remaining[0]
            remaining.isEmpty() -> null
            else -> remaining.maxWithOrNull(
                compareBy({ skillOf(it) },
                    { it.hashCode() and 0x7fffffff }))
        }
        return Bracket(field, rounds, champion)
    }

    /** Kids still standing after [round]'s eliminations. */
    fun kidsStandingAfter(absDay: Int, round: Int): List<String> {
        val b = beeBracket(absDay)
        if (round <= 0) return b.field
        return b.rounds.getOrNull(round - 1)?.remainingAfter
            ?: (b.rounds.lastOrNull()?.remainingAfter ?: b.field)
    }

    /** Who takes the trophy when the player goes out in
     *  [atRound]: the bracket's own champion when the bracket
     *  produced one; otherwise the judges reinstate the
     *  strongest speller of the most recent fall. */
    fun winnerIfOut(absDay: Int, atRound: Int): String {
        val b = beeBracket(absDay)
        b.champion?.let { return it }
        for (r in minOf(atRound, b.rounds.size) downTo 1) {
            val falls = b.rounds[r - 1].falls
            if (falls.isNotEmpty()) return falls.maxWithOrNull(
                compareBy({ skillOf(it) },
                    { it.hashCode() and 0x7fffffff }))!!
        }
        return b.field.maxWithOrNull(
            compareBy({ skillOf(it) },
                { it.hashCode() and 0x7fffffff }))!!
    }

    /** The runner-up a player win leaves standing: the
     *  bracket champion if there is one, else the last faller
     *  of the final round. */
    fun runnerUpOf(absDay: Int): String {
        val b = beeBracket(absDay)
        b.champion?.let { return it }
        return b.rounds.lastOrNull()?.falls?.maxWithOrNull(
            compareBy({ skillOf(it) },
                { it.hashCode() and 0x7fffffff }))
            ?: b.field.first()
    }

    // ---------------- the bee session ----------------

    const val BS_NOT_BEE_DAY: String = "NOT_BEE_DAY"
    const val BS_QUIET: String = "QUIET"
    const val BS_BEFORE: String = "BEFORE"
    const val BS_OPEN: String = "OPEN"
    const val BS_IN_SESSION: String = "IN_SESSION"
    const val BS_DONE: String = "DONE"
    const val BS_AFTER: String = "AFTER"

    fun beeStatus(state: GameState): String = when {
        state.life["beeSeat:${state.absDay}"] == 1 -> BS_IN_SESSION
        beeRecordFor(state, state.absDay) != null -> BS_DONE
        !isBeeDay(state.absDay) -> BS_NOT_BEE_DAY
        quietReason(state) != null -> BS_QUIET
        state.timeMin < BEE_FROM -> BS_BEFORE
        state.timeMin >= BEE_TO -> BS_AFTER
        else -> BS_OPEN
    }

    /** Take a speller's chair. Counted once per bee (entered
     *  is entered — walking away still entered, and midnight
     *  settles the walk-away like any other game in town). */
    fun enterBee(state: GameState): Boolean {
        val st = beeStatus(state)
        if (st == BS_IN_SESSION) return true
        if (st != BS_OPEN) return false
        val day = state.absDay
        state.life["beeSeat:$day"] = 1
        state.life["beeStage:$day"] = 0
        state.life["beeRound:$day"] = 1
        state.life["beeLetter:$day"] = 0
        state.life["beeDone:$day"] = 0
        state.life[K_BEE_IN] = beeEntered(state) + 1
        return true
    }

    data class BeeBeat(val kind: String, // "text" | "letter" | "done"
                       val speaker: String, val text: String)

    private fun roundOf(state: GameState): Int =
        state.life["beeRound:${state.absDay}"] ?: 1

    private fun stageOf(state: GameState): Int =
        state.life["beeStage:${state.absDay}"] ?: 0

    /** The word on the table right now (the showdown deals
     *  from the round past the bracket's end). */
    fun currentWord(state: GameState): String =
        playerWord(state.absDay, roundOf(state))

    /** The current beat of the entered bee, or null when no
     *  bee is on. Stages: 0 the host's intro, 1 the field's
     *  round (narrated), 2 your word — letter by letter,
     *  3 the clean-word note between rounds. A finished bee
     *  reads its record as a "done" beat. */
    fun beeBeat(state: GameState): BeeBeat? {
        val day = state.absDay
        beeRecordFor(state, day)?.let { rec ->
            return BeeBeat("done", "Victoria", doneText(state, rec))
        }
        if (state.life["beeSeat:$day"] != 1) return null
        val bracket = beeBracket(day)
        val round = roundOf(state)
        return when (stageOf(state)) {
            0 -> BeeBeat("text", "Victoria",
                "Principal Victoria takes the microphone with " +
                    "the ease of a woman who has ended debates " +
                    "with it. \"The South Park Elementary Spelling " +
                    "Bee. Eight spellers drew chairs; one trophy " +
                    "leaves. Words get longer. Sleeves stay DOWN " +
                    "— that rule is new, and it is load-bearing.\" " +
                    "The field: ${bracket.field.joinToString(", ")}. " +
                    "And you.")
            1 -> BeeBeat("text", "Victoria",
                fieldNarration(state, bracket, round))
            2 -> {
                val word = currentWord(state)
                val idx = state.life["beeLetter:$day"] ?: 0
                val shown = word.indices.joinToString(" ") {
                    if (it < idx) word[it].uppercaseChar().toString()
                    else "_"
                }
                BeeBeat("letter", "Victoria",
                    "Your word is “$word”.\n$shown\n" +
                        "Letter ${idx + 1} of ${word.length} — " +
                        "the room is holding its breath and " +
                        "Craig is holding something else.")
            }
            else -> {
                val word = playerWord(day, round)
                val standing = kidsStandingAfter(day, round).size
                val tail = if (round >= bracket.rounds.size &&
                    bracket.champion != null)
                    "One speller stands between you and " +
                        "the trophy: ${bracket.champion}. " +
                        "Final word territory."
                else "$standing kid speller(s) still " +
                    "standing. The words get longer."
                BeeBeat("text", "Victoria",
                    "Clean. “${word.uppercase()}” goes in the " +
                        "book, letter-perfect. " + tail)
            }
        }
    }

    /** One round of the field, as the room saw it: who fell
     *  and on what word, the sleeve incident if it's that
     *  round, Kenny's accepted mumbling when he's up. */
    private fun fieldNarration(state: GameState,
                               bracket: Bracket, round: Int): String {
        val day = state.absDay
        if (round > bracket.rounds.size) {
            // the showdown: the last kid spells first
            val champ = bracket.champion ?: return "The field " +
                "is empty. The trophy waits on your word alone."
            val word = kidWord(day, round, champ)
            return if (kidFalls(day, round, champ))
                "$champ steps up for the final word — “$word” — " +
                    "and it comes apart in the middle like a " +
                    "cheap zipper. The room gasps in cursive. " +
                    "The trophy now runs entirely through you."
            else "$champ takes the final-tier word “$word” and " +
                "lays it down letter-perfect, no blink, no " +
                "mercy. Co-champion or nothing: the trophy now " +
                "runs entirely through you."
        }
        val br = bracket.rounds[round - 1]
        val parts = mutableListOf<String>()
        if (br.dq != null) {
            parts += "Before the round even starts, Victoria " +
                "stops the bee. \"Mr. Cartman. Your sleeve.\" The " +
                "sleeve, unfolded, contains the entire word list " +
                "in handwriting experts later call 'confident.' " +
                "Cartman is DISQUALIFIED. The sleeve has denied " +
                "everything. The sleeve is also disqualified."
        }
        for (fall in br.falls) {
            val w = kidWord(day, round, fall)
            parts += if (fall == "Kenny")
                "(muffled) Kenny's spelling is read back off " +
                    "the judges' card as pure consonants, which " +
                    "helps no one. The judges accept the attempt " +
                    "warmly — and eliminate the spelling. Kenny " +
                    "shrugs inside the parka. Kenny always knew."
            else "$fall is out on “$w” — spelled " +
                "“${misspell(w, beeSeed(day, round, fall))}”, " +
                "close enough to hurt, which is the official " +
                "distance."
        }
        if ("Kenny" in br.remainingAfter &&
            (round + day) % 2 == 0) {
            parts += "(muffled) Kenny spells his word into the " +
                "parka. A pause. The judges nod slowly, the way " +
                "people do at weather they can't argue with. " +
                "Accepted. It is always accepted."
        }
        if (br.falls.isEmpty() && br.dq == null) {
            parts += "Nobody falls. The judges look almost " +
                "disappointed. The words get longer."
        }
        val left = br.remainingAfter.size
        parts += when {
            left == 0 -> "And with that, the field is EMPTY. " +
                "It all comes down to your word."
            left == 1 -> "One kid speller still standing: " +
                "${br.remainingAfter[0]}."
            else -> "$left kid spellers still standing."
        }
        return parts.joinToString(" ")
    }

    /** The word so far, as the room sees it: "N E C _ _ _".
     *  (The letter beat and the GameActivity title read it.) */
    fun spellingProgress(state: GameState): String {
        val word = currentWord(state)
        val idx = state.life["beeLetter:${state.absDay}"] ?: 0
        return word.indices.joinToString(" ") {
            if (it < idx) word[it].uppercaseChar().toString() else "_"
        }
    }

    /** The letter row for the current position: the right
     *  letter and two honest distractors, seeded — same
     *  word, same position, same row, every attempt. */
    fun letterOptions(state: GameState): List<Char> {
        val day = state.absDay
        val word = currentWord(state)
        val idx = state.life["beeLetter:$day"] ?: 0
        if (idx >= word.length) return emptyList()
        val right = word[idx]
        val rnd = Random(day * 131L + roundOf(state) * 17L + idx * 7L + 3L)
        val pool = ('a'..'z').filter { it != right }.shuffled(rnd)
        return (listOf(right) + pool.take(2)).shuffled(rnd)
    }

    /** Spell a letter. A right one advances (and finishes
     *  the word — and maybe the bee); a wrong one ends your
     *  bee on the spot, the way bees end. Settle-once lives
     *  in finishBee; this just plays. */
    fun pickLetter(state: GameState, ch: Char): Boolean {
        val day = state.absDay
        if (state.life["beeSeat:$day"] != 1) return false
        if (stageOf(state) != 2) return false
        val word = currentWord(state)
        val idx = state.life["beeLetter:$day"] ?: 0
        if (idx >= word.length) return false
        if (ch.lowercaseChar() != word[idx]) {
            finishBee(state, won = false)
            return false
        }
        val next = idx + 1
        if (next < word.length) {
            state.life["beeLetter:$day"] = next
            return true
        }
        // the word is complete
        state.life["beeDone:$day"] = (state.life["beeDone:$day"] ?: 0) + 1
        val bracket = beeBracket(day)
        val round = roundOf(state)
        when {
            round > bracket.rounds.size -> // showdown word, clean
                finishBee(state, won = true)
            round == bracket.rounds.size &&
                bracket.champion == null ->
                finishBee(state, won = true)
            else -> state.life["beeStage:$day"] = 3
        }
        return true
    }

    /** Step the session: intro → the field's round → your
     *  word → the note → the next round. Advancing a done
     *  beat clears the chair. */
    fun advanceBee(state: GameState) {
        val day = state.absDay
        if (beeRecordFor(state, day) != null) {
            state.life.remove("beeSeat:$day")
            state.life.remove("beeStage:$day")
            state.life.remove("beeRound:$day")
            state.life.remove("beeLetter:$day")
            state.life.remove("beeDone:$day")
            return
        }
        if (state.life["beeSeat:$day"] != 1) return
        when (stageOf(state)) {
            0 -> state.life["beeStage:$day"] = 1
            1 -> {
                state.life["beeStage:$day"] = 2
                state.life["beeLetter:$day"] = 0
            }
            3 -> {
                state.life["beeRound:$day"] = roundOf(state) + 1
                state.life["beeStage:$day"] = 1
            }
        }
    }

    /** Settle the bee — exactly once (the record is the
     *  guard): the counters, the best round, the purse and
     *  the feat on a win, the finalists' friendship either
     *  way, the thread planted, the ticker told. */
    fun finishBee(state: GameState, won: Boolean) {
        val day = state.absDay
        if (beeRecordFor(state, day) != null) return
        val bracket = beeBracket(day)
        val rounds = state.life["beeDone:$day"] ?: 0
        val other = if (won) runnerUpOf(day)
            else winnerIfOut(day, roundOf(state))
        val rec = BeeRecord(day, won, rounds, other)
        writeBeeRecord(state, rec)
        if (won) {
            state.life[K_BEE_WON] = beeWon(state) + 1
            Minigames.awardCoins(state, BEE_WIN_COINS)
            Minigames.awardFeat(state, "bee_champion")
            bumpFriendship(state, other, FINALIST_PAY)
            bumpFriendship(state, "Victoria", 1)
            note(state, "🐝 You WON the spelling bee — the " +
                "trophy, $BEE_WIN_COINS 🪙, and a final against " +
                "$other the school will dine out on.")
        } else {
            bumpFriendship(state, other, 1)
            note(state, "🐝 The spelling bee goes to $other " +
                "in round ${roundOf(state)}. The trophy is " +
                "theirs; the rematch is a school year long.")
        }
        if (rounds > beeBestRound(state)) {
            state.life[K_BEE_BEST] = rounds
        }
        plantSchoolTopic(other)
        // v0.8.0e: the rivalry keeps score at the bee too —
        // if THE RIVAL was in today's field, the best finish
        // takes a standing contest (Rival owns the guards;
        // the bracket's own math grades them both)
        Rival.onBeeSettled(state, rec)
    }

    private fun doneText(state: GameState, rec: BeeRecord): String =
        if (rec.won) "🏆 THE TROPHY IS YOURS. Victoria " +
            "presents it with both hands and a suspicion she " +
            "keeps to herself. +$BEE_WIN_COINS 🪙, the Bee " +
            "Champion feat on your Games page, and — check your " +
            "house — a small gold cup now lives there. " +
            "${rec.other} gets a handshake and the school's " +
            "respect, in that order."
        else "The bee is over: ${rec.other} takes the " +
            "trophy after your round-${rec.rounds + 1} word " +
            "came apart. Victoria, kindly: \"The bee keeps " +
            "its champions humble and its losers literate. " +
            "Same time next Friday — the list only gets longer.\""

    // ---------------- bee records ----------------

    data class BeeRecord(val day: Int, val won: Boolean,
                         val rounds: Int, val other: String)

    private fun writeBeeRecord(state: GameState, rec: BeeRecord) {
        val kept = beeHistory(state).filter { it.day != rec.day } + rec
        state.lifeText[HIST_BEES] = kept.takeLast(BEE_HISTORY_CAP)
            .joinToString(";") {
                "${it.day}|${if (it.won) 1 else 0}|${it.rounds}|${it.other}"
            }
    }

    fun beeHistory(state: GameState): List<BeeRecord> {
        val raw = state.lifeText[HIST_BEES] ?: return emptyList()
        return raw.split(";").mapNotNull { e ->
            val p = e.split("|")
            if (p.size != 4) null
            else BeeRecord(p[0].toIntOrNull()
                ?: return@mapNotNull null,
                p[1] == "1", p[2].toIntOrNull() ?: 0, p[3])
        }
    }

    fun beeRecordFor(state: GameState, day: Int): BeeRecord? =
        beeHistory(state).firstOrNull { it.day == day }

    fun beeEntered(state: GameState): Int = state.life[K_BEE_IN] ?: 0
    fun beeWon(state: GameState): Int = state.life[K_BEE_WON] ?: 0
    fun beeBestRound(state: GameState): Int = state.life[K_BEE_BEST] ?: 0

    // ---------------- the Journal's school page ----------------

    /** The 🎓 SCHOOL page's lines — the pure assembly the
     *  Journal renders (line 0 is the header the suite pins). */
    fun pageLines(state: GameState): List<String> {
        val lines = mutableListOf<String>()
        lines += "🎓 SCHOOL — South Park Elementary (lessons " +
            "sat ×${lessonsSat(state)})"
        val today = state.absDay
        if (!isSchoolDay(today)) {
            lines += "Weekend: the school sleeps. The board " +
                "says Monday's teacher is ${teacherFor(nextSchoolDay(today))}."
        } else when (val quiet = quietReason(state)) {
            null -> {
                lines += "Today's teacher: ${teacherFor(today)} — " +
                    "“${topicFor(today).title}”."
                if (isBeeDay(today)) lines += "🐝 Bee day: " +
                    "the chairs go out at 1 PM, Victoria presiding."
            }
            else -> lines += quietLine(state) ?: ""
        }
        val lessons = lessonHistory(state)
        if (lessons.isEmpty()) {
            lines += "No lessons sat yet — the classroom board " +
                "inside the Elementary seats you during class " +
                "hours (9–3) on school days. Principal Victoria " +
                "keeps an attendance file. It is currently one " +
                "page long and mostly question marks."
        } else {
            val recent = lessons.takeLast(6).reversed()
            lines += "Lessons sat: " + recent.joinToString("; ") {
                "“${topicById(it.topicId)?.title ?: it.topicId}” " +
                    choiceMark(it.choice)
            }
        }
        val bees = beeHistory(state)
        lines += if (bees.isEmpty())
            "🐝 The bee: not entered yet — Fridays, 1–3 PM, " +
                "eight spellers, one trophy."
        else "🐝 The bee: entered ×${beeEntered(state)} · won " +
            "×${beeWon(state)} · best ${beeBestRound(state)} " +
            "word(s) — " + bees.takeLast(3).reversed().joinToString("; ") {
            (if (it.won) "🏆" else "🌾") + " vs ${it.other}, " +
                "${it.rounds} word(s)"
        }
        val recs = recessHistory(state)
        lines += if (recs.isEmpty())
            "🍎 Recess: no beats refereed yet — the yard, " +
                "midday, school days."
        else "🍎 Recess beats refereed ×${recessCount(state)}: " +
            recs.takeLast(4).reversed().joinToString("; ") {
                recessBeatById(it.beatId)?.title ?: it.beatId
            }
        return lines
    }

    // ---------------- the morning report ----------------

    /** School days get one line in the morning report: the
     *  day's teacher and topic, the bee's setup on Fridays —
     *  or the quiet day's honest reason. */
    fun morningNote(state: GameState): String? {
        if (!isSchoolDay(state.absDay)) return null
        val quiet = quietLine(state)
        if (quiet != null) return quiet
        val day = state.absDay
        var note = "🎓 School today: ${teacherFor(day)} teaches " +
            "“${topicFor(day).title}” at the Elementary — sit " +
            "in during class hours (9–3). Recess referee " +
            "duty at midday."
        if (isBeeDay(day)) note += " 🐝 The spelling bee sets " +
            "up at 1 PM — Victoria presiding, eight chairs, one " +
            "trophy."
        return note
    }

    // ---------------- the Look line ----------------

    /** The school's own Look text: inside, the board tells
     *  the day's business; outside, the yard tells its own. */
    fun lookText(state: GameState, locId: String): String? {
        if (locId != "Elementary") return null
        return if (state.playerInside) {
            val quiet = quietLine(state)
            if (quiet != null) quiet
            else if (!isSchoolDay(state.absDay))
                "🎓 The classroom board, in weekend script: " +
                    "“See you Monday — ${teacherFor(nextSchoolDay(state.absDay))}.” " +
                    "The room is empty and peaceful in a way " +
                    "that won't survive 9 AM."
            else "🎓 The classroom board reads: “${topicFor(state.absDay).title} — " +
                "${teacherFor(state.absDay)}”." +
                if (isBeeDay(state.absDay) && state.timeMin < BEE_TO)
                    " Under it, in Victoria's hand: “🐝 THE BEE, " +
                        "1 PM, MY GYM, MY RULES.”"
                else ""
        } else {
            val rs = recessStatus(state)
            if (rs.kind == RS_AVAILABLE)
                "🍎 The yard at recess: ${rs.beat!!.title} — " +
                    "${rs.a} and ${rs.b} are into it, and a " +
                    "referee would be received like rain. " +
                    "(The classroom board inside runs it.)"
            else "🍎 The schoolyard, midday-empty: chalk " +
                "squares fading in the sun, one heroic " +
                "snowball that outlasted the war. School days " +
                "it runs hot from bell to bell."
        }
    }

    // ---------------- the classroom board (GameView) ----------------

    /** Two lines for the painted board: headline + sub. */
    fun boardLines(state: GameState): Pair<String, String> {
        val quiet = quietLine(state)
        return when {
            quiet != null -> "🎓 QUIET DAY" to
                (quiet.removePrefix("🎓 ").take(52) + "…")
            !isSchoolDay(state.absDay) -> "🎓 WEEKEND" to
                "Class resumes Monday — ${teacherFor(nextSchoolDay(state.absDay))}."
            isBeeDay(state.absDay) && state.timeMin >= BEE_FROM &&
                state.timeMin < BEE_TO -> "🐝 BEE DAY" to
                "Victoria's gym. 1 PM. Sleeves DOWN."
            else -> "🎓 ${topicFor(state.absDay).title.uppercase()}" to
                "${teacherFor(state.absDay)} — class 9–3, recess at noon."
        }
    }

    // ---------------- Talk: the teachers remark ----------------

    /** Garrison and Victoria remark on your attendance record
     *  in Talk — Victoria's approval is hard-won and funny.
     *  Once per teacher per day, and only once there's a
     *  record to remark on. (TalkGate calls this at the end
     *  of the owed lines, after the errand thank-you.) */
    fun ambientLine(state: GameState, npcName: String): Line? {
        if (npcName != "Garrison" && npcName != "Victoria") return null
        if (lessonsSat(state) == 0 && beeEntered(state) == 0 &&
            recessCount(state) == 0) return null
        if (state.life["schoolAmb:$npcName"] == state.absDay) return null
        state.life["schoolAmb:$npcName"] = state.absDay
        val seed = state.absDay * 41L + (npcName.hashCode() and 0x7fffffff)
        return if (npcName == "Victoria") {
            Line("Victoria", when {
                lessonsSat(state) >= 12 -> "I will say this " +
                    "once, and I will deny it in the minutes: " +
                    "your attendance is exemplary. There is a " +
                    "gold star in your file. Do NOT touch the star."
                lessonsSat(state) >= 6 -> "Your attendance " +
                    "remains… adequate. I have updated your file " +
                    "from 'concerning' to 'noted.' The clipboard " +
                    "has been informed."
                beeWon(state) >= 1 -> "A bee champion walks my " +
                    "halls. I did not write that rule into the " +
                    "handbook, but I am considering an appendix."
                lessonsSat(state) >= 1 -> "Attendance: noted. " +
                    "One lesson, one sitting, one file entry. " +
                    "Most files grow slowly. Yours is growing " +
                    "slower. Coincidence, I'm sure."
                else -> "Your file is thin. Your presence at " +
                    "my bee was NOT thin. I keep both facts in " +
                    "the same folder, like a professional."
            })
        } else {
            val lastChoice = lessonHistory(state)
                .lastOrNull()?.choice
            Line("Garrison", when (lastChoice) {
                "right" -> "You — you actually listened. In " +
                    "MY class. I had to sit down. Don't make a " +
                    "habit of it; habits become expectations."
                "funny" -> "You still owe the class an apology " +
                    "for the funniest wrong answer I have ever " +
                    "graded. I laughed for a full minute. A full " +
                    "minute. The board still says it. IT STAYS."
                else -> listOf(
                    "A sit-in student. In MY classroom. The " +
                        "curriculum didn't know what to do, so it " +
                        "did its job. Come back. Bring pencils.",
                    "You sat my class and the room survived. " +
                        "Some of my best lessons have survived " +
                        "worse. You were better than the worse.",
                    "Class clown is a competitive position in " +
                        "my room — you're not in it, but you're " +
                        "in the room, which is the first cut."
                )[lcgPick(seed, 3)]
            })
        }
    }

    // ---------------- midnight ----------------

    /** Day-keyed prefixes this module owns (the pruning list
     *  for midnight). */
    private val DAY_PREFIXES = listOf(
        "schoolSeat:", "schoolStep:", "beeSeat:", "beeStage:",
        "beeRound:", "beeLetter:", "beeDone:", "recessDone:")
    private val DAY_TEXT_PREFIXES = listOf("schoolChoice:", "recessBeat:")

    /** Rollover, called from Engine.onNewDay right after the
     *  Bijou's: a walked-away bee settles at the round it
     *  reached (entered is entered), an open lesson just
     *  evaporates (no credit without the participation
     *  moment), and yesterday's day keys are pruned. */
    fun dailyRollover(state: GameState) {
        val yday = state.absDay - 1
        // settle an abandoned bee at its round, no purse,
        // no feat — like every other game, but honest about
        // the walk-away
        if (state.life["beeSeat:$yday"] == 1 &&
            beeRecordFor(state, yday) == null) {
            val rounds = state.life["beeDone:$yday"] ?: 0
            val other = winnerIfOut(yday,
                state.life["beeRound:$yday"] ?: 1)
            writeBeeRecord(state, BeeRecord(yday, false, rounds, other))
            if (rounds > beeBestRound(state)) {
                state.life[K_BEE_BEST] = rounds
            }
            note(state, "🐝 Yesterday's bee settled itself " +
                "when you left: $other takes it. (You were " +
                "entered; entered counts.)")
        }
        // prune yesterday's day keys — the record histories
        // carry the permanent marks
        val dead = DAY_PREFIXES.flatMap { p ->
            state.life.keys.filter { it == "$p$yday" }
        }
        for (k in dead) state.life.remove(k)
        val deadText = DAY_TEXT_PREFIXES.flatMap { p ->
            state.lifeText.keys.filter { it == "$p$yday" }
        }
        for (k in deadText) state.lifeText.remove(k)
    }
}

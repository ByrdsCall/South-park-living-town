package com.hunt3r.southparktown

import kotlin.math.roundToInt
import kotlin.random.Random

// ============================================================
// TALENT NIGHT AT THE BIJOU (v0.13.0c) — once a year, on
// the second Saturday of April, the Bijou turns its stage
// over to the town: ten kid acts, one applause meter, and
// a winner the room itself derives.
//
// The model, in order:
//   - THE ANCHOR is pure calendar: the second Saturday
//     of April (TalentCalendar), the poster up a week
//     ahead. The date is proven disjoint from the dance
//     (February), the play's openings (fall), the fair's
//     week, the carnival's week, every finale window and
//     every holiday window by a 2026–2100 sweep in one
//     lived time space — April's second Saturday (the
//     8th–14th) sits in the town's one quiet spring
//     pocket: six weeks clear of St. Patrick's, six
//     clear of the spring finale's last week of May.
//     The night belongs to the Bijou, so it also stands
//     in the theater's own line: the Saturday screening
//     that would own the stage stands down through
//     MovieNights' own deference (a dark marquee with
//     the reason named), and a bigger town moment — a
//     case climax, a finale window, a holiday beat (the
//     town's standing quiet-day definition), or the
//     play's opening night reaching the date — yields
//     the show the whole year, reason printed. Never
//     postponed into another event.
//   - THE FIELD is derived, never cast by hand: every
//     kid's stage appetite is the play's own data —
//     STAGE_NERVE and the bee's line skill (School's
//     SKILLS, wearing its third hat) — plus a seeded
//     year's appetite for the stage. The top ten make
//     the bill, in a seeded running order. Every kid's
//     act is AUTHORED (the ACTS table — one original
//     line each, denylist-guarded against real songs,
//     real musicals and franchise acts, exactly like
//     the play's and the heroes' guards). The field is
//     posted on the poster a week ahead; entrants talk
//     about it in the run-up, in the play's gossip
//     pattern.
//   - YOUR ENTRY: sign up at the Bijou in the poster
//     week — four authored act kinds (sing / jokes /
//     magic / dance), each with its own played shape on
//     the showdown bar (phrases, sweep, shake and hold
//     threshold all differ). One rehearsal, once, in
//     the week: a real 30-minute beat on the Bijou's
//     stage that raises your score by a stated bonus —
//     the play's attendance honesty. Or just watch: the
//     audience track is complete — reaction prompts in
//     MovieNights' own register, scored by MovieNights'
//     own tables, kept as the night's house.
//   - THE NIGHT (Saturday 18:30–21:30, the Bijou) is an
//     agenda claim seated after the dance and before
//     sleepovers in Engine's stack: the field, the host,
//     and a seeded house. Acts play in posted order;
//     every NPC score is DERIVED live — nerve, voice,
//     chemistry with the room of fellow entrants, and a
//     seeded wobble, all stated here — and the winner
//     is derived from the landed scores at the wrap,
//     ties broken by the room: chemistry, then nerve,
//     then the alphabet. Prizes: a modest coin envelope
//     (15) to the winner, the winner's framed line on
//     the Journal page, a yearbook line for the school
//     year, and ONE winner's photograph through the
//     photo system's own composition. The night's
//     friendship pays follow the movie pattern: paid
//     when the night is played through, never for a
//     night the room finished without you.
//   - THE RECORDS: a Journal 🎤 TALENT SHOW page (the
//     field and the scores as played, your act and
//     placing stated honestly), one TOWN NEWS line in
//     the paper's next edition (winner and act, no
//     invented color — the headline ladder stands at
//     ten), ONE morning-after phone text through the
//     existing harvest (the winner, or the host, or a
//     friend who was there — fact-only, moving nothing
//     beyond the night's own pays), and the yearbook
//     card's line for the year.
//   - THE ROOM IS KIND: the judging never mocks a kid
//     act. Scores are honest — a low score is a quiet
//     room, not a cruel one — and every write-up follows
//     the play review's kind-truths register.
//   - STATE: everything rides life/lifeText under
//     "talent…" keys — zero new save keys, so pre-
//     0.13.0c saves load untouched and join the next
//     show.
// ============================================================

/** The show's pure calendar: a function of the Gregorian
 *  calendar alone (the DanceCalendar pattern). */
object TalentCalendar {

    /** [year]'s show: the second Saturday of April.
     *  Apr 1's ordinal, forward to the first Saturday,
     *  +7. */
    fun showOrdinal(year: Int): Int {
        val apr1 = ordinalOfKey("%04d0401".format(year))
        val toSaturday = (6 - FairCalendar.dowOfOrdinal(apr1) + 7) % 7
        return apr1 + toSaturday + 7
    }

    fun showDay(year: Int): Int =
        FairCalendar.absDayOfOrdinal(showOrdinal(year))

    /** The poster goes up a week before the show. */
    fun announceDay(year: Int): Int = showDay(year) - Talent.ANN_BACK
}

object Talent {

    // ---------------- the dials (all suite-pinned) --------

    const val THEATER = "Bijou"
    const val NAME = "Talent Night at the Bijou"
    const val TOPIC = "talent show"

    /** The evening's window: doors at 6:30, house lights
     *  by 9:30 — the Bijou's own hours, a school night
     *  even in April. */
    const val EVE_FROM = 18 * 60 + 30
    const val EVE_TO = 21 * 60 + 30

    /** Days before the show the poster goes up and the
     *  signups (and the rehearsal) open. */
    const val ANN_BACK = 7

    /** Days after the show a wrapped night stays "this
     *  year's" on the Journal page. */
    const val CURRENT_TAIL = 30

    /** The bill: the ten highest stage appetites in the
     *  kid cast, every year. */
    const val FIELD_SIZE = 10

    /** The seeded share of the non-entrant kids in the
     *  house, in percent — a real crowd, not the whole
     *  grade (the theater has seats, and some are the
     *  acts' families). */
    const val AUDIENCE_PCT = 45

    /** The winner's envelope, in capsule coins: modest,
     *  real, and printed on the poster (City Hall's arts
     *  line — the same money that buys the movie print). */
    const val WINNER_COINS = 15

    /** What the rehearsal is worth on your score card:
     *  a stated, earned bump — the play's rehearsal
     *  honesty priced into the meter. */
    const val REHEARSE_BONUS = 6

    /** The rehearsal's hours on a signup day: the stage
     *  is free while the sun's up and the screenings
     *  haven't taken the evening. */
    const val REH_FROM = 8 * 60
    const val REH_TO = 18 * 60

    /** Friendship pays, at a played-through wrap only
     *  (the movie pattern: an abandoned night pays
     *  nobody): every entrant takes the room's regard;
     *  the winner takes a little more; a legendary house
     *  adds one to the winner again. */
    const val ENTER_PAY = 1
    const val WINNER_PAY = 2
    const val LEGENDARY_BONUS = 1

    /** Score bounds: the applause meter's whole range. */
    const val SCORE_MIN = 5
    const val SCORE_MAX = 99

    /** The score's base — the room starts kind. */
    const val SCORE_BASE = 34

    /** Reaction prompts land after these many acts have
     *  played (the movie's three-prompt shape). */
    val REACT_AFTER: List<Int> = listOf(2, 5, 8)

    // ---------------- act kinds ----------------
    // kind id -> (its shelf label, the MovieNights beat
    // the room judges it by). The beat mapping is the
    // audience track's whole grammar: comedy is laughed
    // at, heart is applauded, spectacle is gasped at,
    // and the stunt is the scare.

    val KIND_BEAT: Map<String, String> = mapOf(
        "jokes" to MovieNights.BEAT_COMEDY,
        "impressions" to MovieNights.BEAT_COMEDY,
        "ventriloquism" to MovieNights.BEAT_COMEDY,
        "song" to MovieNights.BEAT_HEART,
        "poem" to MovieNights.BEAT_HEART,
        "instrument" to MovieNights.BEAT_HEART,
        "magic" to MovieNights.BEAT_SPECTACLE,
        "dance" to MovieNights.BEAT_SPECTACLE,
        "juggle" to MovieNights.BEAT_SPECTACLE,
        "stunt" to MovieNights.BEAT_SCARE)

    fun kindLabel(kind: String): String = when (kind) {
        "jokes" -> "a joke set"
        "impressions" -> "impressions"
        "ventriloquism" -> "a ventriloquism act"
        "song" -> "an original song"
        "poem" -> "an original poem"
        "instrument" -> "an instrumental"
        "magic" -> "a magic act"
        "dance" -> "a dance number"
        "juggle" -> "a juggling act"
        "stunt" -> "a stunt"
        else -> "an act"
    }

    fun beatOfKind(kind: String): String =
        KIND_BEAT[kind] ?: MovieNights.BEAT_SPECTACLE

    // ---------------- the authored acts ----------------
    // Every kid's act, authored: an original one-liner
    // for whatever year the appetite derivation puts
    // them on the bill. Wholly invented acts — the
    // suite denylist-scans every line against real
    // songs, real musicals and franchise acts, and the
    // room's kindness is in the writing, not the scores.

    data class Act(val kind: String, val line: String)

    val ACTS: Map<String, Act> = mapOf(
        "Stan" to Act("instrument",
            "plays the recorder with a concert soloist's " +
                "confidence — both songs he knows, back " +
                "to back, no encore spared"),
        "Kyle" to Act("poem",
            "recites an original poem about homework, " +
                "justice, and his little brother, in that " +
                "order, with sources"),
        "Cartman" to Act("song",
            "sings an original song, written by him, " +
                "about how excellent he is at singing — " +
                "three verses, all verses about that"),
        "Kenny" to Act("stunt",
            "performs the muffle-defying stunt: three " +
                "somersaults off the stage steps, parka " +
                "and all, landing like a rumor"),
        "Butters" to Act("dance",
            "tap-dances in his Sunday shoes, counting " +
                "out loud so the steps behave, smiling " +
                "like the numbers owe him money"),
        "Wendy" to Act("song",
            "sings an original ballad she wrote about " +
                "the town's water tower — researched, " +
                "rhymed, and quietly devastating"),
        "Craig" to Act("magic",
            "performs a card trick in total silence; " +
                "the card appears so unimpressed that " +
                "the room loses it completely"),
        "Tweek" to Act("juggle",
            "juggles three coffee cups — empty, the " +
                "usher insisted — without spilling a " +
                "drop of the panic"),
        "Clyde" to Act("impressions",
            "does impressions of every teacher in the " +
                "building, saving the principal for " +
                "last and bravest"),
        "Jimmy" to Act("jokes",
            "a stand-up set: nine jokes, professionally " +
                "timed, with a closer he has been saving " +
                "since October"),
        "Timmy" to Act("instrument",
            "plays the drums with his whole heart and " +
                "most of the chair — the kit has never " +
                "been hit with this much meaning"),
        "Tolkien" to Act("instrument",
            "plays bass: two notes, perfect posture, " +
                "total commitment — the coolest four " +
                "minutes in the building"),
        "Bebe" to Act("dance",
            "performs a dance routine choreographed to " +
                "the second, with a hair flip the front " +
                "row will be describing for weeks"),
        "Heidi" to Act("song",
            "sings an original song about her garden " +
                "and harmonizes with herself on the " +
                "chorus, because she planned it that way"),
        "Nichole" to Act("dance",
            "performs a routine she choreographed at " +
                "her kitchen table, upgraded for a real " +
                "stage and a real curtain"),
        "Red" to Act("juggle",
            "juggles four scarves and one attitude, " +
                "and only the attitude ever gets dropped"),
        "Kevin" to Act("magic",
            "performs a space-magic routine with a " +
                "flashlight, a mirror, and the total " +
                "seriousness of a moon landing"),
        "Francis" to Act("jokes",
            "tells the jokes his dad taught him, with " +
                "the exact delivery his dad warned him " +
                "about, as a tribute"),
        "DogPoo" to Act("stunt",
            "balances six library books on his head and " +
                "walks the whole stage without dropping " +
                "a single one, or any of the dirt"),
        "Karen" to Act("dance",
            "performs a routine she learned from the " +
                "television and finished herself — the " +
                "ending is hers, and it shows"),
        "Scott" to Act("ventriloquism",
            "performs with a sock-puppet sidekick who " +
                "says all the brave lines, while Scott " +
                "stands there looking surprised"),
        "Firkle" to Act("poem",
            "recites an original poem, “Ode to Not " +
                "Clapping,” and formally forbids applause " +
                "afterwards — the room claps anyway"),
        "Annie" to Act("song",
            "sings an original song she wrote across " +
                "the last three pages of her notebook, " +
                "where the important things live"),
        "Bradley" to Act("magic",
            "performs a disappearing-coin trick and " +
                "checks three times that it really " +
                "disappeared — it did; the checking is " +
                "the encore"),
        "Ike" to Act("stunt",
            "stacks three blocks into a tower, knocks " +
                "them down entirely on purpose, and " +
                "takes the deepest bow of the night"),
        "Kevin McCormick" to Act("juggle",
            "juggles two tangerines and a potato, " +
                "because the third tangerine was lunch " +
                "and he has no regrets"),
        "Shelley" to Act("instrument",
            "plays clarinet with middle-school precision " +
                "and fourth-grade mercy — a harder " +
                "combination than it sounds"),
        "Sophie" to Act("jokes",
            "tells a set about being the new kid in " +
                "town, which the room agrees is expert " +
                "material from the only expert present"),
        "Michael" to Act("poem",
            "reads an original poem about a crow that " +
                "judges the whole town, kindly, one " +
                "rooftop at a time"),
        "Pete" to Act("song",
            "performs an original dirge on a borrowed " +
                "guitar — tempo: funeral; mood: quietly " +
                "pleased with himself"),
        "Henrietta" to Act("poem",
            "recites her shortest epic: four lines, " +
                "all of them about the moon, each one " +
                "heavier than the last"),
        "Nathan" to Act("magic",
            "performs a mind-reading act and somehow " +
                "names the exact card you were thinking " +
                "of — he will not be taking questions"),
        "Pip" to Act("ventriloquism",
            "performs a proper ventriloquism act with " +
                "a puppet whose manners are better than " +
                "everyone's, including Pip's"),
        "Dougie" to Act("dance",
            "performs the dance he practiced in his " +
                "room all spring, with choreography " +
                "notes from his lamp, who attended every " +
                "rehearsal"),
        "Jason" to Act("jokes",
            "tells cool jokes at a cool volume, and " +
                "checks after each one, coolly, that it " +
                "landed cool"),
        "Terrance Mephesto" to Act("magic",
            "performs a science-magic demonstration: a " +
                "paper volcano that erupts exactly on " +
                "cue, with hypotheses distributed after"),
        "Jenny" to Act("impressions",
            "does impressions of the lunch line, the " +
                "bus stop, and the principal's intercom " +
                "voice — the intercom one is uncanny"),
        "Flora" to Act("song",
            "sings an original song about the flowers " +
                "coming back, with a kazoo bridge she " +
                "wrote on a seed packet"),
        "Filmore" to Act("jokes",
            "runs a campaign-style joke set, complete " +
                "with a slogan, a platform, and exit " +
                "polling at the doors"),
        "Quaid" to Act("instrument",
            "plays the triangle with statistical " +
                "precision: one ding, perfectly placed, " +
                "never repeated, never improved upon"),
        "Mimsy" to Act("stunt",
            "performs a strongman act: lifts a chair, " +
                "a full backpack, and very nearly the " +
                "piano bench, to tremendous support"),
        "Tricia" to Act("impressions",
            "delivers a full report of impressions — " +
                "the weather, the news, and her brother — " +
                "all findings verified twice"))

    fun actOf(kid: String): Act? = ACTS[kid]

    /** The room's read of an act, by kind — seeded per
     *  (year, kid) so the same performance reads the
     *  same on every surface. Kind truths, kindly told. */
    val VIGNETTES: Map<String, List<String>> = mapOf(
        "jokes" to listOf(
            "The room laughs in the right places and " +
                "two wrong ones, which counts as a bonus.",
            "Somebody in the third row laughs so hard " +
                "the usher has to check on them. They're " +
                "fine. They're still laughing.",
            "The applause meter wobbles like it isn't " +
                "sure it is allowed to enjoy itself. It " +
                "is. It does."),
        "impressions" to listOf(
            "The room gasps-laughs at the accuracy and " +
                "checks the doors, in case the original " +
                "walks in.",
            "Three separate adults in the audience turn " +
                "around slowly, recognized. The kid " +
                "bows to each.",
            "The meter climbs on recognition alone — " +
                "the highest compliment a copy can earn."),
        "ventriloquism" to listOf(
            "Half the room spends the act watching the " +
                "puppet and half watching the kid, and " +
                "both halves applaud.",
            "The puppet gets its own ovation. The kid " +
                "accepts it on the puppet's behalf, with " +
                "dignity.",
            "Somebody whispers that the puppet should " +
                "run for mayor. The puppet does not " +
                "decline."),
        "song" to listOf(
            "The room goes quiet in the good way — the " +
                "way where even the folding chairs stop " +
                "creaking out of respect.",
            "By the last chorus, the front row is " +
                "humming along to a song that did not " +
                "exist yesterday.",
            "The applause afterwards has the long tail " +
                "of a room that felt something and is " +
                "being normal about it."),
        "poem" to listOf(
            "Silence, then the kind of applause that " +
                "starts with one person who means it and " +
                "spreads honestly.",
            "The usher is seen wiping an eye and " +
                "denying it to the aisle.",
            "The meter takes a second to climb — poems " +
                "land late, and land whole."),
        "instrument" to listOf(
            "The room listens with its whole face. " +
                "Somewhere, a music teacher is sitting " +
                "up straighter.",
            "Not one person checks the program during " +
                "it. For a school audience, that is a " +
                "standing ovation with its shoes on.",
            "The last note rings, and the room lets it " +
                "finish ringing before it applauds. " +
                "Class."),
        "magic" to listOf(
            "A gasp rolls down the rows in sections, " +
                "like weather. Nobody saw anything. " +
                "Everybody saw everything.",
            "The front row leans in so far the usher " +
                "has to wave them back to their own " +
                "postal codes.",
            "The meter spikes; two kids immediately " +
                "announce they know how it was done, and " +
                "are wrong in opposite directions."),
        "dance" to listOf(
            "The room's feet are moving in their seats " +
                "by the halfway mark — the dance has " +
                "escaped the stage.",
            "A missed step gets covered so smoothly " +
                "the room applauds what it thinks was " +
                "the hard part. It was.",
            "The finish lands and the meter jumps like " +
                "it was pulled. Somebody's program goes " +
                "in the air."),
        "juggle" to listOf(
            "The whole room holds one shared breath " +
                "for the whole act and lets it out in " +
                "one piece at the catch.",
            "A drop never comes, which the room " +
                "celebrates like a save in a final.",
            "The applause has a rhythm to it, set by " +
                "ten pairs of hands that were recently " +
                "covering eyes."),
        "stunt" to listOf(
            "The gasp is genuine and enormous, and so " +
                "is the applause — the usher ages a year " +
                "and applauds anyway.",
            "The room cannot decide whether to watch " +
                "or cover its eyes, and settles on " +
                "watching through fingers, applauding " +
                "through gloves.",
            "It lands. The meter lands with it. The " +
                "front row needs a moment."))

    fun vignetteFor(year: Int, kid: String): String {
        val act = ACTS[kid] ?: return ""
        val bank = VIGNETTES[act.kind] ?: return ""
        return bank[Random(("talentvig:$year:$kid")
            .hashCode().toLong()).nextInt(bank.size)]
    }

    // ---------------- your act ----------------
    // Four authored kinds, each with its own played
    // shape: phrases, the bar's sweep, the hands' shake,
    // and what "held" asks of a release. Singing is the
    // dance's honest bar; jokes are faster and land or
    // die; magic is slower and shakier; the dance number
    // is the bar with stage nerves.

    data class PlayerAct(
        val id: String, val label: String,
        val blurb: String, val kind: String,
        val phrases: Int, val sweepMs: Long,
        val jitter: Double, val holdAt: Double)

    val PLAYER_ACTS: List<PlayerAct> = listOf(
        PlayerAct("sing", "🎤 Sing",
            "an original song of your own — three " +
                "verses, one chorus, zero borrowed tunes",
            "song", phrases = 4, sweepMs = 1150,
            jitter = 0.03, holdAt = Feel.POWER_GOOD),
        PlayerAct("jokes", "🤣 Tell jokes",
            "a joke set you wrote yourself — every " +
                "punchline lands or dies in public",
            "jokes", phrases = 3, sweepMs = 900,
            jitter = 0.03, holdAt = 0.85),
        PlayerAct("magic", "🎩 Do magic",
            "a magic act of five moves and one " +
                "borrowed coin, performed with total " +
                "confidence",
            "magic", phrases = 5, sweepMs = 1300,
            jitter = 0.05, holdAt = Feel.POWER_GOOD),
        PlayerAct("dance", "💃 Dance",
            "a dance number in four phrases of your " +
                "own choreography, on a real stage",
            "dance", phrases = 4, sweepMs = 1150,
            jitter = 0.04, holdAt = Feel.POWER_GOOD))

    fun playerAct(id: String): PlayerAct? =
        PLAYER_ACTS.firstOrNull { it.id == id }

    /** Your composure under the lights: the new kid's
     *  honest 5 (the slow dance's spotlight number). */
    const val PLAYER_NERVE = 5

    /** One performance on the showdown bar: [phrases]
     *  releases, each steadied by your nerve and shaken
     *  by your act's own jitter; a release holds at your
     *  act's threshold. Deterministic in the seed. */
    class TalentSession(val act: PlayerAct, seed: Long) {
        private val rnd = Random(seed)
        var steps = 0; private set
        var held = 0; private set
        private var steadySum = 0.0
        val over: Boolean get() = steps >= act.phrases

        fun step(release01: Double): Double {
            if (over) return 0.0
            steps++
            val zone = Showdown.zoneOf(release01.toFloat())
            val jitter = (rnd.nextDouble() * 2 - 1) *
                act.jitter
            val steady = Showdown.zonePower(zone) *
                (0.72 + 0.05 * PLAYER_NERVE) + jitter
            steadySum += steady
            if (steady >= act.holdAt) held++
            return steady
        }

        /** How the room took it, 0..1: half the phrases
         *  you held, half how steady the holding was. */
        val quality: Double get() =
            if (steps == 0) 0.0 else
                (0.5 * held.toDouble() / act.phrases +
                    0.5 * (steadySum / steps)
                        .coerceIn(0.0, 1.0))

        /** nailed: the room will retell it · landed: a
         *  real act, honestly played · sideways: the
         *  laugh of the night, and still an act. */
        val result: String get() = when {
            quality >= 0.80 -> "nailed"
            quality >= 0.45 -> "landed"
            else -> "sideways"
        }
    }

    fun sessionSeed(state: GameState): Long =
        ("talentact:${state.absDay}").hashCode().toLong()

    /** Your score from a played session: the meter's
     *  read of the quality, plus the rehearsal's stated
     *  bonus when you earned it. */
    fun playerScore(quality: Double,
                    rehearsed: Boolean): Int =
        (42 + (50 * quality).roundToInt() +
            (if (rehearsed) REHEARSE_BONUS else 0))
            .coerceIn(SCORE_MIN, SCORE_MAX)

    // ---------------- the field, derived ----------------

    /** The host: the stage desk of the year's judging
     *  rotation — the same chair the play's critic sits
     *  in (Yearbook's own rotation, column 5). */
    fun hostOf(year: Int): String =
        Yearbook.judgeFor(year, 5).name

    /** A kid's appetite for the stage this year: the
     *  play's nerve and the bee's voice, weighted, plus
     *  the year's seeded want. Pure data — the same
     *  number from any save, any day. */
    fun appetite(year: Int, kid: String): Int =
        Play.stageNerve(kid) * 3 + School.skillOf(kid) * 2 +
            Random(("talentapp:$year:$kid").hashCode()
                .toLong()).nextInt(10)

    /** The year's bill: the [FIELD_SIZE] highest
     *  appetites; appetite ties break by name, A–Z. */
    fun field(year: Int): List<String> =
        CharacterSystem.CAST_KIDS.sortedWith(
            compareByDescending<String> {
                appetite(year, it) }.thenBy { it })
            .take(FIELD_SIZE)

    /** The posted running order: the field shuffled by
     *  the year's seed — deterministic, so the poster,
     *  the night and the record all read the same order. */
    fun runningOrder(year: Int): List<String> =
        field(year).sortedBy { kid ->
            Random(("talentord:$year:$kid").hashCode()
                .toLong()).nextInt(1_000_000) }

    /** Your slot in the running order when you enter:
     *  seeded by the year, anywhere from opener to
     *  closer. */
    fun playerSlot(year: Int): Int =
        Random(("talentslot:$year").hashCode().toLong())
            .nextInt(FIELD_SIZE + 1)

    /** The house beyond the bill: the year's seeded
     *  share of the other kids, in the seats. */
    fun audience(year: Int): List<String> =
        (CharacterSystem.CAST_KIDS - field(year).toSet())
            .filter { kid ->
                Random(("talentatt:$year:$kid").hashCode()
                    .toLong()).nextInt(100) < AUDIENCE_PCT }
            .sorted()

    /** An entrant's chemistry with the room: their
     *  average bond with the other entrants, normalized
     *  ((avg + 2) / 8) — the play's own cast-chemistry
     *  measure, read across the bill. */
    fun chemRoom(state: GameState, year: Int,
                 entrant: String): Double {
        val others = field(year) - entrant
        if (others.isEmpty()) return 0.25
        val avg = others.map {
            Photos.chemistryBetween(state, entrant, it) }
            .average()
        return ((avg + 2.0) / 8.0).coerceIn(0.0, 1.0)
    }

    /** The applause meter's read of an entrant, derived
     *  live and stated whole: nerve's reach, voice's
     *  weight, the room's chemistry, and the night's own
     *  seeded wobble — a great pairing can have an off
     *  night, and a dark horse can land. */
    fun actScore(state: GameState, year: Int,
                 entrant: String): Int {
        val nerve = Play.stageNerve(entrant)
        val voice = School.skillOf(entrant)
        val chem = chemRoom(state, year, entrant)
        val wobble = Random(("talentwob:$year:$entrant")
            .hashCode().toLong()).nextInt(11) - 5
        return (SCORE_BASE + nerve * 4 + voice * 2 +
            (chem * 14).roundToInt() + wobble)
            .coerceIn(SCORE_MIN, SCORE_MAX)
    }

    // ---------------- the record ----------------
    // One record per year, in lifeText["talentRec:<year>"],
    // pipe-serialized with a version head — reads never
    // create one; every write goes through recordOrNew.

    data class TalentRecord(
        val year: Int,
        var playerAct: String = "",
        var playerOrder: Int = -1,
        var rehearsed: Boolean = false,
        var arrived: Boolean = false,
        var step: Int = 0,
        var buzz: Int = 0,
        var reacts: Int = 0,
        val scores: MutableMap<String, Int> =
            mutableMapOf(),
        var playerScore: Int = -1,
        var winner: String = "",
        var winScore: Int = 0,
        var night: String = "",
        var yieldWhy: String = "",
        var photoId: String = "",
        var nightDay: Int = 0)

    fun parseRecord(year: Int,
                    raw: String?): TalentRecord? {
        if (raw == null) return null
        val p = raw.split("|")
        if (p.size < 15 || p[0] != "1" ||
            p[1].toIntOrNull() != year) return null
        val rec = TalentRecord(year)
        rec.playerAct = p[2]
        rec.playerOrder = p[3].toIntOrNull() ?: -1
        rec.rehearsed = p[4] == "1"
        rec.arrived = p[5] == "1"
        rec.step = p[6].toIntOrNull() ?: 0
        rec.buzz = p[7].toIntOrNull() ?: 0
        rec.reacts = p[8].toIntOrNull() ?: 0
        if (p[9].isNotEmpty()) {
            for (pair in p[9].split(";")) {
                val kv = pair.split("=")
                val v = kv.getOrNull(1)?.toIntOrNull()
                if (kv.size == 2 && v != null) {
                    rec.scores[kv[0]] = v
                }
            }
        }
        rec.playerScore = p[10].toIntOrNull() ?: -1
        rec.winner = p[11]
        rec.winScore = p[12].toIntOrNull() ?: 0
        rec.night = p[13]
        rec.yieldWhy = p[14]
        if (p.size > 15) rec.photoId = p[15]
        if (p.size > 16) rec.nightDay = p[16].toIntOrNull() ?: 0
        return rec
    }

    fun serializeRecord(r: TalentRecord): String {
        val scores = r.scores.entries
            .joinToString(";") { "${it.key}=${it.value}" }
        return listOf("1", r.year.toString(), r.playerAct,
            r.playerOrder.toString(),
            if (r.rehearsed) "1" else "0",
            if (r.arrived) "1" else "0",
            r.step.toString(), r.buzz.toString(),
            r.reacts.toString(), scores,
            r.playerScore.toString(), r.winner,
            r.winScore.toString(), r.night, r.yieldWhy,
            r.photoId, r.nightDay.toString())
            .joinToString("|")
    }

    fun record(state: GameState,
               year: Int): TalentRecord? =
        parseRecord(year, state.lifeText["talentRec:$year"])

    fun recordOrNew(state: GameState,
                    year: Int): TalentRecord =
        record(state, year) ?: TalentRecord(year)

    fun saveRecord(state: GameState, r: TalentRecord) {
        state.lifeText["talentRec:${r.year}"] =
            serializeRecord(r)
    }

    fun allRecords(state: GameState): List<TalentRecord> =
        state.lifeText.keys
            .filter { it.startsWith("talentRec:") }
            .mapNotNull { key ->
                key.removePrefix("talentRec:").toIntOrNull()
                    ?.let { record(state, it) } }
            .sortedByDescending { it.year }

    /** The year's night is wrapped: played, partial,
     *  missed or yielded. */
    fun isWrapped(state: GameState, year: Int): Boolean =
        record(state, year)?.night?.isNotEmpty() == true

    /** The show year the town is currently in: this
     *  calendar year's show until CURRENT_TAIL days past
     *  it, then next year's. */
    fun currentYear(state: GameState): Int {
        val y = state.gameYear
        val tail = TalentCalendar.showDay(y) + CURRENT_TAIL
        return if (state.absDay > tail) y + 1 else y
    }

    fun cycleLive(state: GameState): Boolean =
        state.absDay >= TalentCalendar.announceDay(
            currentYear(state)) - 3

    fun currentRecord(state: GameState): TalentRecord? =
        record(state, currentYear(state))

    /** The performing bill on the night, with you in it
     *  at your slot when you entered. */
    fun nightOrder(rec: TalentRecord): List<String> {
        val order = runningOrder(rec.year).toMutableList()
        if (rec.playerOrder >= 0) {
            order.add(rec.playerOrder.coerceIn(0,
                order.size), "You")
        }
        return order
    }

    // ---------------- yields ----------------

    /** Why the show can't be held this year, when a
     *  bigger town moment owns the date — the town's
     *  standing quiet-day definition plus the play's
     *  live check, the dance's exact machinery, only
     *  consulted on the show day itself. */
    fun yieldReason(state: GameState): String? {
        val year = currentYear(state)
        if (state.absDay != TalentCalendar.showDay(year)) {
            return null
        }
        School.quietReason(state)?.let { return it }
        // the play's live check, consulted the dance's
        // way: nightCanHappen answers "the play's opening
        // night can happen TONIGHT", so it is only a yield
        // within reach of an opening — anywhere else the
        // play simply isn't on
        val opening = PlayCalendar.scheduleFor(
            Play.currentYear(state)).openingDay
        if (!Play.nightCanHappen(state) &&
            state.absDay in (opening - 2)..(opening + 2)) {
            return "play"
        }
        return null
    }

    fun yieldText(why: String): String = when (why) {
        School.QUIET_CASE -> "a case climax owns the day"
        School.QUIET_FINALE -> "a season finale owns the week"
        School.QUIET_HOLIDAY -> "a holiday owns the day"
        "play" -> "the play's stage owns the night"
        else -> "the town had a bigger night"
    }

    /** The show can be staged tonight: the calendar's
     *  day, no yield, the night not yet wrapped. On a
     *  yielded night the reason is stamped where it is
     *  learned, so the poster can keep saying it after
     *  the moment passes. */
    fun eveningLive(state: GameState): Boolean {
        val year = currentYear(state)
        if (state.absDay != TalentCalendar.showDay(year)) {
            return false
        }
        if (isWrapped(state, year)) return false
        val why = yieldReason(state)
        if (why != null) {
            val rec = recordOrNew(state, year)
            if (rec.yieldWhy.isEmpty()) {
                rec.yieldWhy = why
                saveRecord(state, rec)
            }
            return false
        }
        return true
    }

    /** The Bijou is the show's tonight — MovieNights'
     *  stand-down reads this. A pure read: it never
     *  stamps (the module's own eveningLive stamps). */
    fun nightClaimed(state: GameState): Boolean {
        val year = currentYear(state)
        if (state.absDay != TalentCalendar.showDay(year)) {
            return false
        }
        if (isWrapped(state, year)) return false
        return yieldReason(state) == null
    }

    // ---------------- the claim ----------------

    /** The night's agenda claim — seated in Engine after
     *  the dance and before sleepovers, so a formal
     *  evening outranks the rehearsal room and the
     *  sleepover planner but never a fair, a play night
     *  or a dance: the bill, the host and the house, at
     *  the Bijou for the evening's window. */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (!eveningLive(state)) return null
        if (timeMin !in EVE_FROM..EVE_TO) return null
        val year = currentYear(state)
        return when {
            npcName in field(year) -> AgendaBlock(THEATER,
                "🎤 Talent night — performing at the Bijou")
            npcName == hostOf(year) -> AgendaBlock(THEATER,
                "🎤 Hosting talent night at the Bijou")
            npcName in audience(year) -> AgendaBlock(THEATER,
                "🎤 Talent night at the Bijou")
            else -> null
        }
    }

    // ---------------- the run-up ----------------

    fun signupWindowOpen(state: GameState): Boolean {
        if (!cycleLive(state)) return false
        val year = currentYear(state)
        if (isWrapped(state, year)) return false
        val show = TalentCalendar.showDay(year)
        if (state.absDay !in
            TalentCalendar.announceDay(year)..show) {
            return false
        }
        if (state.absDay == show &&
            state.timeMin >= EVE_FROM) return false
        return true
    }

    fun entered(state: GameState): Boolean =
        currentRecord(state)?.playerAct?.isNotEmpty() == true

    /** Sign up (or change your act) at the Bijou. The
     *  slot is the year's seed, set at the first signup
     *  and kept through a change — the rehearsal stays
     *  banked too: prep is stage prep, whichever act it
     *  ends up serving. */
    fun signUp(state: GameState,
               actId: String): Pair<String, String> {
        val act = playerAct(actId)
            ?: return "bad" to "That's not an act on the " +
                "signup sheet."
        if (!signupWindowOpen(state)) {
            return "window" to "The signup sheet is at " +
                "the Bijou during poster week — the week " +
                "before the show."
        }
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        val changing = rec.playerAct.isNotEmpty()
        rec.playerAct = actId
        if (rec.playerOrder < 0) {
            rec.playerOrder = playerSlot(year)
        }
        saveRecord(state, rec)
        return (if (changing) "changed" else "entered") to
            "✍️ You're on the bill: ${act.blurb}. " +
                "You perform ${ordinalWord(rec.playerOrder + 1)} " +
                "of ${FIELD_SIZE + 1} — the order's on the " +
                "poster. Rehearse once this week if you " +
                "want the stage to feel smaller."
    }

    fun ordinalWord(n: Int): String = when (n) {
        1 -> "first"; 2 -> "second"; 3 -> "third"
        4 -> "fourth"; 5 -> "fifth"; 6 -> "sixth"
        7 -> "seventh"; 8 -> "eighth"; 9 -> "ninth"
        10 -> "tenth"; 11 -> "eleventh"
        else -> "${n}th"
    }

    fun rehearseOpen(state: GameState): Boolean {
        if (!entered(state)) return false
        if (currentRecord(state)?.rehearsed == true) {
            return false
        }
        val year = currentYear(state)
        if (isWrapped(state, year)) return false
        val show = TalentCalendar.showDay(year)
        if (state.absDay !in
            TalentCalendar.announceDay(year)..show) {
            return false
        }
        return state.timeMin in REH_FROM..REH_TO
    }

    /** The rehearsal: thirty real minutes on the Bijou's
     *  stage, once, worth a stated bonus on your score.
     *  The clock is the judge — the beat advances it two
     *  ticks, exactly like the darkroom's developing. */
    fun rehearse(state: GameState): String? {
        if (!rehearseOpen(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (rec.rehearsed) return null
        val eng = Engine()
        repeat(30 / 15) { eng.advanceTime(state) }
        rec.rehearsed = true
        saveRecord(state, rec)
        val actLabel = playerAct(rec.playerAct)?.label
            ?.lowercase() ?: "act"
        return "🎤 Half an hour on the Bijou's stage: " +
            "the marks, the lights, the exact creak of " +
            "the third board. Your $actLabel act runs " +
            "cleaner for it — the rehearsal is worth " +
            "+$REHEARSE_BONUS on the applause meter, " +
            "banked."
    }

    fun posterText(state: GameState): String {
        val year = currentYear(state)
        val show = TalentCalendar.showDay(year)
        val date = Dance.dateLabel(show)
        val rec = record(state, year)
        val sb = StringBuilder()
        sb.append("🎤 TALENT NIGHT AT THE BIJOU — $date.\n")
        sb.append("Hosted by ${hostOf(year)}. Ten acts, " +
            "one applause meter, and a $WINNER_COINS-coin " +
            "envelope from City Hall's arts line for the " +
            "winner.\n\nThe bill, in running order:\n")
        val order = rec?.let { nightOrder(it) }
            ?: runningOrder(year)
        order.forEachIndexed { i, name ->
            if (name == "You") {
                val act = rec?.playerAct?.let {
                    playerAct(it) }
                sb.append("${i + 1}. You — " +
                    "${act?.blurb ?: "your act"}.\n")
            } else {
                val act = ACTS[name]
                sb.append("${i + 1}. $name — " +
                    "${act?.line ?: "an act"}.\n")
            }
        }
        when {
            rec != null && rec.night == "yielded" ->
                sb.append("\nThe show was not held this " +
                    "year — ${yieldText(rec.yieldWhy)}.")
            signupWindowOpen(state) &&
                rec?.playerAct?.isNotEmpty() != true ->
                sb.append("\nOne slot's still open on " +
                    "the signup sheet — yours, if you " +
                    "want it.")
            rec != null && rec.playerAct.isNotEmpty() ->
                sb.append("\nYou're on the bill" +
                    (if (rec.rehearsed) ", rehearsed and " +
                        "ready." else ". The stage is " +
                        "free for one rehearsal this week."))
        }
        return sb.toString().trimEnd()
    }

    /** The screen fixture's routing: the Bijou's screen
     *  is the show's on show day, and the poster board
     *  during signup week — except while a screening is
     *  actually holding its evening. */
    fun screenOpen(state: GameState): Boolean {
        if (!cycleLive(state)) return false
        val year = currentYear(state)
        if (state.absDay == TalentCalendar.showDay(year) &&
            !isWrapped(state, year)) return true
        if (!signupWindowOpen(state)) return false
        if (MovieNights.isScreeningDay(state.absDay) &&
            state.timeMin >= MovieNights.DOORS_FROM) {
            return false
        }
        return true
    }

    /** The dialog's posture at the Bijou screen.
     *  kind: poster / entered / night / yielded / done */
    data class ScreenStatus(val kind: String,
                            val text: String)

    fun screenStatus(state: GameState): ScreenStatus {
        val year = currentYear(state)
        val rec = record(state, year)
        val show = TalentCalendar.showDay(year)
        return when {
            rec != null && rec.night == "yielded" -> ScreenStatus(
                "yielded",
                "🎤 The poster, crossed out in pencil: " +
                    "talent night was not held this year " +
                    "— ${yieldText(rec.yieldWhy)}.")
            state.absDay == show && rec != null &&
                rec.night.isNotEmpty() -> ScreenStatus(
                "done",
                "🎤 The stage is struck and the meter's " +
                    "asleep. ${rec.winner} took the " +
                    "night — the Journal page has the " +
                    "whole story.")
            state.absDay == show && eveningLive(state) &&
                state.timeMin >= EVE_FROM - 30 ->
                ScreenStatus("night",
                    "🎤 Talent night. The house is " +
                        "filling, the meter's warming up, " +
                        "and ${hostOf(year)} is testing " +
                        "the microphone like it owes " +
                        "anybody money.")
            rec != null && rec.night.isNotEmpty() ->
                ScreenStatus("done",
                    "🎤 Last show: ${rec.winner} won " +
                        "with ${actNameOf(state, rec, rec.winner)}.")
            else -> ScreenStatus(
                if (entered(state)) "entered" else "poster",
                posterText(state))
        }
    }

    fun actNameOf(state: GameState, rec: TalentRecord,
                  name: String): String {
        if (name == "You") {
            val act = playerAct(rec.playerAct)
            return "your ${act?.kind ?: "act"} act"
        }
        val act = ACTS[name] ?: return "an act"
        return "${name.split(" ").first()}'s " +
            "${kindLabel(act.kind)}"
    }

    // ---------------- the night ----------------

    /** The night's beats can land: you're at the Bijou,
     *  inside, on show night — the evening's window, or
     *  a started session finishing past it (a show, once
     *  started, plays out). */
    fun sessionOpen(state: GameState): Boolean {
        val year = currentYear(state)
        if (state.absDay != TalentCalendar.showDay(year)) {
            return false
        }
        val me = state.agents.values.firstOrNull {
            it.isPlayer } ?: return false
        if (me.location != THEATER || !state.playerInside) {
            return false
        }
        val rec = record(state, year)
            ?: return eveningLive(state) &&
                state.timeMin >= EVE_FROM - 30
        if (rec.night.isNotEmpty()) return false
        if (rec.arrived) return true
        return eveningLive(state) &&
            state.timeMin >= EVE_FROM - 30
    }

    fun reactsDue(rec: TalentRecord): Boolean =
        rec.reacts < REACT_AFTER.count { it <= rec.step }

    /** The room prompt owed right now: the movie beat of
     *  the act that just played. */
    fun reactBeatKind(state: GameState,
                      rec: TalentRecord): String {
        val order = nightOrder(rec)
        val last = order.getOrNull(rec.step - 1)
            ?: return MovieNights.BEAT_SPECTACLE
        if (last == "You") {
            return beatOfKind(playerAct(rec.playerAct)
                ?.kind ?: "song")
        }
        return beatOfKind(ACTS[last]?.kind ?: "song")
    }

    /** What lands next: arrive / act / react / youract /
     *  results — or null when the night isn't yours to
     *  move from here. */
    fun nextBeatKind(state: GameState): String? {
        if (!sessionOpen(state)) return null
        val rec = currentRecord(state) ?: return "arrive"
        if (!rec.arrived) return "arrive"
        if (rec.playerScore < 0 && rec.playerOrder >= 0 &&
            nightOrder(rec).getOrNull(rec.step) == "You") {
            return "youract"
        }
        if (reactsDue(rec)) return "react"
        val order = nightOrder(rec)
        if (rec.step >= order.size) return "results"
        return if (order[rec.step] == "You") "youract"
        else "act"
    }

    fun playArrival(state: GameState): String? {
        if (!sessionOpen(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (rec.arrived || rec.night.isNotEmpty()) {
            return null
        }
        rec.arrived = true
        rec.nightDay = state.absDay
        saveRecord(state, rec)
        Engine().advanceTime(state)
        val order = nightOrder(rec)
        val bill = order.joinToString(", ") { name ->
            if (name == "You") "you" else name }
        val yours = if (rec.playerOrder >= 0)
            " You perform " +
                "${ordinalWord(rec.playerOrder + 1)}." else ""
        return "🎤 The Bijou, dressed for talent night: " +
            "a paper banner, the applause meter glowing " +
            "over the stage, and ${hostOf(year)} at the " +
            "microphone in a borrowed bow tie.\n\n" +
            "The running order: $bill.$yours\n\n" +
            "The house fills in behind you. House " +
            "lights down."
    }

    /** One NPC act: performed, read by the room, scored
     *  and kept. The act costs no ticks — stage time,
     *  like the movie's feature. */
    fun playAct(state: GameState): String? {
        if (!sessionOpen(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (!rec.arrived || rec.night.isNotEmpty()) {
            return null
        }
        val order = nightOrder(rec)
        val name = order.getOrNull(rec.step) ?: return null
        if (name == "You") return null
        val act = ACTS[name] ?: return null
        val score = actScore(state, year, name)
        rec.scores[name] = score
        rec.step++
        saveRecord(state, rec)
        return "🎤 ${ordinalWord(rec.step)}: $name — " +
            "${act.line}.\n\n${vignetteFor(year, name)}\n\n" +
            "The applause meter settles on $score."
    }

    /** The room, in MovieNights' own register: your
     *  reaction to the act just played, scored by its
     *  tables, kept as the night's house. */
    fun react(state: GameState,
              reactionId: String): String? {
        if (!sessionOpen(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (!rec.arrived || !reactsDue(rec)) return null
        if (reactionId !in MovieNights.REACTIONS
                .map { it.first }) return null
        val beat = reactBeatKind(state, rec)
        rec.buzz += MovieNights.scoreFor(beat, reactionId)
        rec.reacts++
        saveRecord(state, rec)
        val verb = when (reactionId) {
            MovieNights.REACT_LAUGH -> "laugh along"
            MovieNights.REACT_GASP -> "gasp"
            MovieNights.REACT_APPLAUD -> "applaud"
            MovieNights.REACT_HECKLE -> "heckle, once, fondly"
            else -> "react"
        }
        val color = when (reactionId) {
            MovieNights.rightFor(beat) ->
                " The room is with you — the acts can " +
                    "feel it from the stage."
            in MovieNights.closeFor(beat) ->
                " A fair read; the room hums."
            else -> " The room wanted something else " +
                "from you there."
        }
        return "🎤 You $verb.$color"
    }

    fun yourActDue(state: GameState): Boolean =
        nextBeatKind(state) == "youract"

    /** The played act, judged: the session's quality on
     *  the meter, the rehearsal's bonus in, the score
     *  kept. The room is kind in the copy and honest in
     *  the number. */
    fun judgePlayerAct(state: GameState,
                       session: TalentSession): List<String>? {
        if (!sessionOpen(state)) return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        if (!rec.arrived || rec.playerScore >= 0) {
            return null
        }
        if (nightOrder(rec).getOrNull(rec.step) != "You") {
            return null
        }
        if (session.act.id != rec.playerAct) return null
        val score = playerScore(session.quality,
            rec.rehearsed)
        rec.playerScore = score
        rec.step++
        saveRecord(state, rec)
        val band = when (session.result) {
            "nailed" -> "The room comes UP. Somebody's " +
                "program goes in the air, the meter " +
                "pegs, and ${hostOf(year)} has to wait " +
                "to introduce the next act."
            "landed" -> "A real act, honestly played — " +
                "the applause is the warm, full kind, " +
                "and the meter climbs like it means it."
            else -> "It goes sideways in the way that " +
                "becomes the story of the night: the " +
                "room laughs WITH you, loudly, and the " +
                "meter is kinder than the act was. " +
                "You were up there. That counts double."
        }
        return listOf(band,
            "The applause meter settles on $score." +
                (if (rec.rehearsed) " (The rehearsal's " +
                    "+$REHEARSE_BONUS, banked and spent.)"
                    else ""))
    }

    /** The winner, derived at the wrap: highest score;
     *  ties go to the room — highest chemistry with the
     *  room, then highest nerve, then the alphabet.
     *  Your tie-break room is your standing with the
     *  entrants themselves. */
    fun winnerOf(state: GameState,
                 rec: TalentRecord): Pair<String, Int>? {
        val cands = mutableListOf<Triple<String, Int, Double>>()
        for ((name, score) in rec.scores) {
            cands.add(Triple(name, score,
                chemRoom(state, rec.year, name)))
        }
        if (rec.playerOrder >= 0) {
            val eff = if (rec.playerScore >= 0)
                rec.playerScore else 0
            val avgFriend = if (rec.scores.isEmpty()) 0.0
                else rec.scores.keys.map {
                    state.friendship[it] ?: 0 }
                    .average()
            cands.add(Triple("You", eff,
                (avgFriend / 100.0).coerceIn(0.0, 1.0)))
        }
        if (cands.isEmpty()) return null
        val nerveOf = { name: String ->
            if (name == "You") PLAYER_NERVE
            else Play.stageNerve(name) }
        return cands.sortedWith(
            compareByDescending<Triple<String, Int, Double>> {
                it.second }
                .thenByDescending { it.third }
                .thenByDescending { nerveOf(it.first) }
                .thenBy { it.first })
            .first().let { it.first to it.second }
    }

    /** Settle the night once: every unplayed score
     *  derived, the winner read off the landed table.
     *  The friendship pays and the photograph belong to
     *  the played-through wrap alone; the winner's coin
     *  envelope is the one prize that settles whenever
     *  the winner is you and you actually performed. */
    fun settle(state: GameState, rec: TalentRecord) {
        if (rec.winner.isNotEmpty()) return
        for (kid in field(rec.year)) {
            if (kid !in rec.scores) {
                rec.scores[kid] =
                    actScore(state, rec.year, kid)
            }
        }
        val w = winnerOf(state, rec) ?: return
        rec.winner = w.first
        rec.winScore = w.second
        saveRecord(state, rec)
        if (w.first == "You" && rec.playerScore >= 0) {
            Minigames.awardCoins(state, WINNER_COINS)
        }
    }

    private fun plantTopic(kid: String) {
        val mem = NpcMemoryStore.getOrCreate(kid)
        mem.topics.remove(TOPIC)
        mem.topics.add(TOPIC)
        while (mem.topics.size > 6) mem.topics.removeAt(0)
    }

    /** THE RESULTS: the wrap as played — the table read
     *  out, the winner derived, the pays landed, ONE
     *  photograph minted of the room as it stands. */
    fun finishNight(state: GameState): String? {
        if (nextBeatKind(state) != "results") return null
        val year = currentYear(state)
        val rec = recordOrNew(state, year)
        settle(state, rec)
        rec.night = "played"
        val tier = MovieNights.tierFor(rec.buzz)
        // the pays (a played-through night only)
        for (kid in field(year)) {
            Engine().relAdjust(state, kid, ENTER_PAY)
            plantTopic(kid)
        }
        if (rec.winner != "You" &&
            rec.winner.isNotEmpty()) {
            Engine().relAdjust(state, rec.winner,
                WINNER_PAY)
            if (tier == "legendary") {
                Engine().relAdjust(state, rec.winner,
                    LEGENDARY_BONUS)
            }
        }
        plantTopic(rec.winner)
        // the winner's photograph
        val present = MovieNights.presentCast(state)
        if (present.isNotEmpty()) {
            val me = state.agents.values.first {
                it.isPlayer }
            val winLine = if (rec.winner == "You")
                "you won with your " +
                    "${playerAct(rec.playerAct)?.kind ?: "act"} act"
                else "${rec.winner} won with " +
                    actNameOf(state, rec, rec.winner)
            val cap = "🎤 Talent night at the Bijou, " +
                "${Dance.dateLabel(rec.nightDay)} — $winLine."
            val photo = Photos.composeGroup(state,
                present, emptyList(), me.name,
                me.appearance
            )?.copy(caption = cap)
            if (photo != null) {
                Photos.applyPhoto(state, photo)
                rec.photoId = photo.id
            }
        }
        saveRecord(state, rec)
        Engine().advanceTime(state)
        val table = placingLines(state, rec)
            .joinToString("\n")
        val winLine = if (rec.winner == "You")
            "🏆 You won talent night — the " +
                "$WINNER_COINS-coin envelope is yours, " +
                "and the room is still talking about it."
            else "🏆 ${rec.winner} wins talent night " +
                "with ${actNameOf(state, rec, rec.winner)} " +
                "— the $WINNER_COINS-coin envelope goes " +
                "home with them."
        return "🎤 The results, read by ${hostOf(year)} " +
            "off the applause meter:\n\n$table\n\n" +
            "$winLine\n\nThe house: " +
            "${MovieNights.tierLabel(tier)} " +
            "(${if (rec.buzz >= 0) "+" else ""}${rec.buzz})." +
            (if (rec.photoId.isNotEmpty())
                "\n📷 One photograph of the winner's " +
                    "room, for the wall." else "")
    }

    /** The night's table as played: placing order —
     *  score, then the room, then the alphabet (the
     *  winner derivation's own order, minus nerve,
     *  which the page doesn't need to print). */
    fun placingLines(state: GameState,
                     rec: TalentRecord): List<String> {
        val rows = mutableListOf<Triple<String, Int, Double>>()
        for ((name, score) in rec.scores) {
            rows.add(Triple(name, score,
                chemRoom(state, rec.year, name)))
        }
        if (rec.playerOrder >= 0) {
            val eff = if (rec.playerScore >= 0)
                rec.playerScore else 0
            rows.add(Triple("You", eff, 0.5))
        }
        return rows.sortedWith(
            compareByDescending<Triple<String, Int, Double>> {
                it.second }
                .thenByDescending { it.third }
                .thenBy { it.first })
            .mapIndexed { i, row ->
                val actName = actNameOf(state, rec,
                    row.first)
                "${i + 1}. ${row.first} — $actName — " +
                    "${row.second}" +
                    (if (row.first == rec.winner)
                        " 🏆" else "")
            }
    }

    // ---------------- rollover ----------------

    private fun note(state: GameState, text: String) {
        state.events.add(0, text)
        while (state.events.size > 40) {
            state.events.removeAt(state.events.lastIndex)
        }
    }

    /** The town log, on Engine's own discipline: new
     *  lines land at the end and the log is trimmed to
     *  its standing cap of 30, oldest first — a module
     *  that appends past the cap breaks the lifetime
     *  soak, so the cap is the town's, not the module's. */
    private fun townNote(state: GameState, text: String) {
        state.townLog.add(text)
        while (state.townLog.size > 30) {
            state.townLog.removeAt(0)
        }
    }

    fun dailyRollover(state: GameState) {
        // the poster morning
        for (y in (state.gameYear - 1)..(state.gameYear + 1)) {
            if (state.absDay ==
                TalentCalendar.announceDay(y)) {
                note(state, "🎤 Poster up at the Bijou: " +
                    "talent night is " +
                    "${Dance.dateLabel(TalentCalendar.showDay(y))} " +
                    "— signups are open at the screen, " +
                    "and this year's bill is posted.")
            }
            // the morning after: wrap what the night was.
            // The show is a town event — the record opens
            // here even for a year the player never
            // touched, so the paper and the yearbook can
            // carry it.
            if (state.absDay ==
                TalentCalendar.showDay(y) + 1) {
                val rec = recordOrNew(state, y)
                if (rec.night.isNotEmpty()) continue
                if (rec.yieldWhy.isNotEmpty()) {
                    rec.night = "yielded"
                    saveRecord(state, rec)
                    townNote(state, "🎤 Talent night was " +
                        "not held this year — " +
                        yieldText(rec.yieldWhy) + ".")
                    continue
                }
                settle(state, rec)
                rec.nightDay = TalentCalendar.showDay(y)
                rec.night = if (rec.arrived ||
                    rec.step > 0) "partial" else "missed"
                saveRecord(state, rec)
                townNote(state, "🎤 Talent night at the " +
                    "Bijou: ${rec.winner} won with " +
                    actNameOf(state, rec, rec.winner) +
                    if (rec.night == "missed")
                        " — you never made it." else ".")
            }
        }
    }

    // ---------------- the surfaces ----------------

    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val year = currentYear(state)
        val rec = record(state, year)
        val show = TalentCalendar.showDay(year)
        out.add("Talent night at the Bijou — the second " +
            "Saturday of April, every year. Ten acts off " +
            "the poster, one applause meter, and a winner " +
            "the room derives itself.")
        if (cycleLive(state)) {
            when {
                rec != null && rec.night == "yielded" -> {
                    out.add("📅 $year · " +
                        "${Dance.dateLabel(show)} — not held")
                    out.add("The show yielded this year: " +
                        yieldText(rec.yieldWhy) + ". The " +
                        "meter stays dark; the poster " +
                        "stays up, crossed out in pencil.")
                }
                rec != null && rec.night.isNotEmpty() -> {
                    out.add("📅 $year · " +
                        "${Dance.dateLabel(show)} — " +
                        "the night as played")
                    out.addAll(placingLines(state, rec))
                    if (rec.playerOrder >= 0) {
                        val placing = placingLines(state,
                            rec).indexOfFirst {
                            it.contains(". You —") } + 1
                        out.add("Your act: " +
                            (playerAct(rec.playerAct)
                                ?.blurb ?: "your act") +
                            " — " +
                            (if (rec.playerScore >= 0)
                                "scored ${rec.playerScore}, " +
                                    "${ordinalWord(placing)} " +
                                    "of ${rec.scores.size + 1}."
                                else "you never took the " +
                                    "stage — the slot stayed " +
                                    "empty and the meter " +
                                    "read it as a zero.") +
                            (if (rec.rehearsed)
                                " The rehearsal paid its " +
                                    "+$REHEARSE_BONUS."
                                else ""))
                    } else {
                        out.add("You watched from the " +
                            "house — the house was " +
                            MovieNights.tierLabel(
                                MovieNights.tierFor(rec.buzz)) +
                            ".")
                    }
                    out.add("🏆 ${if (rec.winner == "You")
                        "You" else rec.winner} won with " +
                        actNameOf(state, rec, rec.winner) +
                        " (${rec.winScore}) — the framed " +
                        "line, the yearbook line, and the " +
                        "$WINNER_COINS-coin envelope.")
                    if (rec.night != "played") {
                        out.add("The night wrapped as " +
                            rec.night + " — the pays belong " +
                            "to nights played through, so " +
                            "none landed.")
                    }
                    if (rec.photoId.isNotEmpty()) {
                        out.add("📷 The winner's photo " +
                            "was taken in the room.")
                    }
                }
                state.absDay < TalentCalendar
                    .announceDay(year) ->
                    out.add("📅 $year · the poster goes up " +
                        "${Dance.dateLabel(TalentCalendar.announceDay(year))}, " +
                        "a week before the show.")
                else -> {
                    out.add("📅 $year · " +
                        "${Dance.dateLabel(show)} — " +
                        "the bill")
                    for (name in nightOrder(
                        rec ?: TalentRecord(year))) {
                        if (name == "You") {
                            out.add("• You — " +
                                (playerAct(rec?.playerAct
                                    ?: "")?.blurb
                                    ?: "your act"))
                        } else {
                            out.add("• $name — " +
                                (ACTS[name]?.line ?: "an act"))
                        }
                    }
                    when {
                        rec != null && rec.playerAct.isNotEmpty() ->
                            out.add("You're on the bill" +
                                (if (rec.rehearsed)
                                    ", rehearsed (+$REHEARSE_BONUS banked)."
                                    else ". One rehearsal is " +
                                        "yours at the Bijou " +
                                        "this week, 8 to 6."))
                        signupWindowOpen(state) ->
                            out.add("The signup sheet is " +
                                "open at the Bijou — four " +
                                "acts to choose from, one " +
                                "stage, one week.")
                        else ->
                            out.add("Signups have closed. " +
                                "The house is still a fine " +
                                "place to be Saturday.")
                    }
                    val why = rec?.yieldWhy
                        ?.ifEmpty { null }
                        ?: if (state.absDay == show)
                            yieldReason(state) else null
                    if (why != null) {
                        out.add("Tonight the show yields: " +
                            yieldText(why) + ".")
                    }
                }
            }
        } else {
            out.add("This year's show opens its poster " +
                "week on ${Dance.dateLabel(
                    TalentCalendar.announceDay(year))} — " +
                "the second Saturday of April is " +
                "${Dance.dateLabel(show)}.")
        }
        // past years, one line each (newest three)
        for (r in allRecords(state)) {
            if (r.year == year) continue
            if (out.size > 40) break
            val when_ = Dance.dateLabel(
                TalentCalendar.showDay(r.year))
            out.add(when {
                r.night == "yielded" ->
                    "📅 ${r.year} · $when_ — not held: " +
                        yieldText(r.yieldWhy) + "."
                r.winner.isNotEmpty() ->
                    "📅 ${r.year} · $when_ — " +
                        "${r.winner} won with " +
                        actNameOf(state, r, r.winner) +
                        " (${r.winScore})."
                else -> "📅 ${r.year} · $when_ — the " +
                    "show was held."
            })
        }
        return out
    }

    /** The paper's discipline: one TOWN NEWS line for
     *  the show week's edition — the winner and the act,
     *  or the yielded fact; never a headline (the ladder
     *  stands at ten). */
    fun paperLine(state: GameState, week: Int): String? {
        for (r in allRecords(state)) {
            if (r.night.isEmpty()) continue
            if (Jobs.weekIndex(TalentCalendar
                    .showDay(r.year)) != week) continue
            val date = Dance.dateLabel(
                TalentCalendar.showDay(r.year))
            return when {
                r.night == "yielded" ->
                    "🎤 Talent night at the Bijou, $date: " +
                        "not held this year — " +
                        yieldText(r.yieldWhy) + "."
                r.winner == "You" ->
                    "🎤 Talent night at the Bijou, $date: " +
                        "the new kid won with their " +
                        "${playerAct(r.playerAct)?.kind ?: "act"} " +
                        "act (${r.winScore} on the applause " +
                        "meter)" +
                        (if (r.photoId.isNotEmpty())
                            "; a photograph was taken." else ".")
                r.winner.isNotEmpty() ->
                    "🎤 Talent night at the Bijou, $date: " +
                        "${r.winner} won with " +
                        actNameOf(state, r, r.winner) +
                        " (${r.winScore} on the applause " +
                        "meter)" +
                        (if (r.photoId.isNotEmpty())
                            "; a photograph was taken." else ".")
                else -> null
            }
        }
        return null
    }

    /** The yearbook card's line for a school year: the
     *  talent night of that volume's April. */
    fun yearbookLine(state: GameState,
                     year: Int): String? {
        val r = record(state, year) ?: return null
        if (r.night.isEmpty() || r.night == "yielded" ||
            r.winner.isEmpty()) return null
        val winner = if (r.winner == "You") "You"
            else r.winner
        return "🎤 Talent night, April $year " +
            "(${Yearbook.labelOf(year)}): $winner won " +
            "with ${actNameOf(state, r, r.winner)} — " +
            "${r.winScore} on the applause meter."
    }

    /** The Bijou's Look text across the cycle: the
     *  poster, the night, the echo. */
    fun lookText(state: GameState,
                 locId: String): String? {
        if (locId != THEATER) return null
        val year = currentYear(state)
        val rec = record(state, year)
        val show = TalentCalendar.showDay(year)
        return when {
            state.absDay == show &&
                rec != null && rec.night == "yielded" ->
                "🎤 The talent-night poster is crossed " +
                    "out in pencil: not this year — " +
                    yieldText(rec.yieldWhy) + "."
            eveningLive(state) &&
                state.timeMin >= EVE_FROM - 30 ->
                "🎤 Talent night: the house lights are " +
                    "warm, the meter glows over the " +
                    "stage, and the screen's staying dark " +
                    "in honor of the living acts."
            state.absDay in TalentCalendar
                .announceDay(year)..show &&
                signupWindowOpen(state) ->
                "🎤 A hand-lettered poster by the screen: " +
                    "TALENT NIGHT — " +
                    "${Dance.dateLabel(show)}. This " +
                    "year's bill: " +
                    field(year).joinToString(", ") +
                    ". The signup sheet hangs underneath" +
                    if (entered(state))
                        " — your name's already on it."
                    else ", with one slot's worth of " +
                        "room left."
            rec != null && rec.night.isNotEmpty() &&
                rec.night != "yielded" &&
                state.absDay in (show + 1)..(show + 10) ->
                "🎤 The poster, annotated in " +
                    "${hostOf(year)}'s hand: " +
                    "“${if (rec.winner == "You") "YOU"
                        else rec.winner.uppercase()} — " +
                    "${rec.winScore}.” The meter's still " +
                    "warm."
            rec != null && rec.night == "yielded" &&
                state.absDay in (show + 1)..(show + 10) ->
                "🎤 The crossed-out poster is still up. " +
                    "The Bijou keeps its apologies posted " +
                    "for a week and a half."
            else -> null
        }
    }

    /** Entrants talk in the run-up (the play's gossip
     *  pattern: seeded days, quoted acts); after the
     *  night, the winner and the bill remember it out
     *  loud. */
    fun gossipLine(state: GameState,
                   npcName: String): Line? {
        val year = currentYear(state)
        val rec = record(state, year)
        val show = TalentCalendar.showDay(year)
        // after the night — once ever each (v0.13.0g:
        // the play's stamp — the hearing is the
        // stamping, so the winner's sock drawer and
        // the bill's scores are said once, not daily
        // for ten days)
        if (rec != null && rec.night.isNotEmpty() &&
            rec.night != "yielded" &&
            state.absDay in (show + 1)..(show + 10)) {
            val key = "talentPost:$year:$npcName"
            if (state.life[key] != null) return null
            if (npcName == rec.winner) {
                state.life[key] = 1
                return Line(npcName,
                    "I won talent night. The meter " +
                        "said ${rec.winScore}. I have the " +
                        "envelope money in a safe place, " +
                        "which is my sock drawer, which is " +
                        "where the glory lives now.")
            }
            if (npcName in field(year)) {
                val s = rec.scores[npcName]
                if (s != null) {
                    state.life[key] = 1
                    return Line(npcName,
                        "Talent night: the meter gave " +
                            "me $s. ${if (rec.winner == "You")
                                "The new kid won it — " +
                                    "fair and loud."
                                else "${rec.winner} won " +
                                    "it. I've already " +
                                    "started on next " +
                                    "year's act."}")
                }
            }
            return null
        }
        // the run-up
        if (!signupWindowOpen(state)) return null
        if (npcName !in field(year)) return null
        if ((state.absDay + npcName.hashCode()) % 3 != 0) {
            return null
        }
        val act = ACTS[npcName] ?: return null
        val lines = listOf(
            "Saturday's the talent show. I'm doing " +
                "${kindLabel(act.kind)} — " +
                "${act.line.split("—").firstOrNull()
                    ?.trim()?.lowercase() ?: "my act"}. " +
                "The meter doesn't scare me. It scares " +
                "me a regular amount.",
            "You're coming Saturday, right? Talent " +
                "night. I need the house full — the " +
                "meter reads a full room better. Also " +
                "I need witnesses.")
        return Line(npcName, lines[state.absDay % 2])
    }

    /** The morning-after text: the winner, or the host
     *  when it's you, or a friend who was there when you
     *  missed it. Fact-only; moves nothing. */
    fun phoneBeat(state: GameState):
        Triple<String, String, String>? {
        val year = currentYear(state)
        val rec = record(state, year) ?: return null
        if (state.absDay !=
            TalentCalendar.showDay(year) + 1) {
            return null
        }
        if (rec.night.isEmpty() ||
            rec.night == "yielded") return null
        val winner = rec.winner
        if (winner.isEmpty()) return null
        if (winner == "You") {
            val host = hostOf(year)
            return Triple(host, "talent",
                "You WON talent night. The meter said " +
                    "${rec.winScore} and I read it out " +
                    "loud twice because the first time " +
                    "the room was still cheering. — " +
                    "$host, your host")
        }
        if (rec.night == "missed") {
            val friend = (audience(year) + field(year))
                .filter { it != winner }
                .maxByOrNull { state.friendship[it] ?: 0 }
                ?.takeIf {
                    (state.friendship[it] ?: 0) >= 30 }
                ?: return null
            return Triple(friend, "talent",
                "You missed talent night! $winner won " +
                    "with ${actNameOf(state, rec, winner)} " +
                    "— ${rec.winScore} on the meter. " +
                    "I saved you the program.")
        }
        return Triple(winner, "talent",
            "I won talent night at the Bijou — " +
                "${actNameOf(state, rec, winner)}, " +
                "${rec.winScore} on the applause meter. " +
                "The room was kind and the needle was " +
                "honest.")
    }
}

package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Phone — v0.5.0e: the New Kid's phone. Pure Kotlin (the hub
// routes + the Messages store + generation), rendered by
// PhoneActivity. The phone is a HUB, not a rewrite: its tiles
// open the surfaces that already exist (Journal, its album and
// games sections, the town's Go Somewhere picker, Settings).
// Messages is the one new room: ambient texts from the cast
// reacting to YOUR day.
//
// How texts are born (determinism is the whole contract):
//  · EVENT NOTES — the day's happenings are noted at the
//    moment they happen, by the systems that own them
//    (Photos.checkFeats / applyPhoto, Minigames.awardFeat,
//    Errands.complete, PocketPals.pull / grantCard), as bare
//    facts in state.phoneNotes: "absDay|KIND|a|b".
//  · ROLLOVER — Phone.dailyRollover (Engine.onNewDay calls it
//    after the story arcs tick) turns yesterday's notes plus
//    state-derived triggers (a story beat landing, a mystery
//    chapter opening, a birthday, a holiday, complaint-worthy
//    weather, a close friend saying hi) into texts. Sender
//    and wording are chosen by a Random seeded ONLY from the
//    fact itself (note day + kind + subjects, or the trigger
//    day + kind), so a save/reload can never reroll a text:
//    the same fact always writes the same words.
//  · CAPS — the inbox is finite on purpose: at most
//    MAX_NEW_PER_DAY new texts a morning, MAX_UNREAD unread
//    before the town stops texting into the void, MAX_TEXTS
//    stored (oldest read shed first), MAX_PER_THREAD per
//    sender, MAX_NOTES pending facts. Everything decode-
//    clamped in Prefs, like every store before it.
// Spoiler hygiene: mystery/story texts are premise-level
// vague BY TEMPLATE — no case content, no arc outcomes, no
// solutions. The suite scans every bank for the tell words.
// ============================================================

/** One message in a thread. [thread] is the cast member the
 *  conversation belongs to — or, since v0.10.0d, a group
 *  room id (GroupChats owns those); [sender] is that name
 *  for their texts, the actual speaker inside a group, or
 *  "you" for the player's quick replies. */
data class PhoneText(
    val id: String,
    val thread: String,
    val sender: String,
    val trigger: String,
    val absDay: Int,
    val dayKey: String,
    val text: String,
    var read: Boolean = false,
    val emoji: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "thread" to thread, "sender" to sender,
        "trigger" to trigger, "absDay" to absDay, "day" to dayKey,
        "text" to text, "read" to read, "emoji" to emoji)

    companion object {
        /** Decode one record; anything malformed (unknown
         *  thread/sender/trigger, empty or absurd text) is
         *  dropped, never repaired into a fake text. */
        fun fromMap(m: Map<String, Any?>): PhoneText? {
            val thread = m["thread"]?.toString() ?: return null
            // A thread is a cast member's name, a parent's
            // solo thread (v0.10.0a — the identity check
            // admits Mom/Dad explicitly; before v0.10.0d a
            // family text decoded as an unknown thread and
            // was silently dropped on every reload), or a
            // group room (v0.10.0d). The set stays closed:
            // anything else is malformed and dropped.
            if (thread !in KID_ORDER &&
                thread !in Family.PARENTS &&
                thread !in GroupChats.GROUP_IDS) return null
            val sender = m["sender"]?.toString() ?: return null
            if (sender != "you" && sender !in KID_ORDER &&
                sender !in Family.PARENTS) return null
            val trigger = m["trigger"]?.toString() ?: return null
            if (trigger !in Phone.TRIGGERS) return null
            val text = m["text"]?.toString()?.takeIf { it.isNotEmpty() }
                ?: return null
            return PhoneText(
                id = m["id"]?.toString() ?: "",
                thread = thread, sender = sender, trigger = trigger,
                absDay = ((m["absDay"] as? Number)?.toInt() ?: 0).coerceAtLeast(0),
                dayKey = m["day"]?.toString() ?: "",
                text = text.take(Phone.MAX_TEXT_CHARS),
                read = m["read"] as? Boolean ?: false,
                emoji = m["emoji"]?.toString() ?: "")
        }
    }
}

/** One row of the Messages list: a conversation, newest first. */
data class PhoneThread(
    val name: String,
    val last: PhoneText,
    val unread: Int,
    val count: Int
)

/** The phone home screen's status strip, as data. */
data class HubStatus(
    val time: String,
    val weatherIcon: String,
    val weatherLabel: String,
    val coins: Int,
    val unread: Int
)

object Phone {

    // ---------------- triggers ----------------
    const val FEAT = "feat"
    const val PHOTO = "photo"
    const val ERRAND = "errand"
    const val COMMISSION = "commission"
    const val PULL = "pull"
    const val STORY = "story"
    const val MYSTERY = "mystery"
    const val DINNER = "dinner"
    const val DINNER_THANKS = "dinnerthanks"
    const val VOTE = "vote"
    const val MOVIE = "movie"
    const val RIVAL = "rival"
    /** v0.9.0c: the county fair's announcement text — the
     *  Mayor herself, on the announcement morning. */
    const val FAIR = "fair"
    /** v0.11.0e: the winter carnival's announcement text —
     *  the Mayor again, on the announcement morning (her
     *  office handles both festivals; the thread is the
     *  same McDaniels thread the fair uses). */
    const val CARNIVAL = "carnival"
    /** v0.12.0a: the paper's publication-morning summary —
     *  one text carrying the Gazette's headline verbatim,
     *  from a rotating cast subscriber on their own thread
     *  (Newspaper owns the rotation and the debt stamp).
     *  No new thread: the inbox math is the carnival's —
     *  (98 cast + 2 parents + 3 group rooms) × 2 = 206 ≤
     *  MAX_TEXTS 220 stands and the dial does not move. */
    const val PAPER = "paper"
    /** v0.10.0a: your family's texts — dinner calls, the
     *  allowance note, the morning-after fridge report. The
     *  threads are "Mom" and "Dad"; quick replies stay
     *  cast-only (quickReply gates on KID_ORDER), so family
     *  threads are receive-only, like a real parent's texts
     *  deserve. */
    const val FAMILY = "family"
    const val HERO = "hero"
    /** v0.10.0c: the school play's texts — the director's
     *  announcement, the cast-list morning, opening night,
     *  and the critic's review note. Play.phoneBeat owns
     *  the facts; the harvest only delivers. */
    const val PLAY = "play"
    /** v0.13.0b: the school dance's one morning-after
     *  text — from your date if you had one, else a friend
     *  who was there. Dance.phoneBeat owns the facts; the
     *  harvest only delivers. */
    const val DANCE = "dance"
    /** v0.13.0c: the talent show's one morning-after
     *  text — from the winner, or the host when the
     *  winner is you, or a friend who was there when you
     *  missed it. Talent.phoneBeat owns the facts; the
     *  harvest only delivers. */
    const val TALENT = "talent"
    /** v0.13.0d: the parade's one morning-after text —
     *  from a crewmate who marched with you, a curb
     *  neighbor you stood with, or a friend who was there
     *  when you missed it. Parade.phoneBeat owns the
     *  facts; the harvest only delivers. */
    const val PARADE = "parade"
    /** v0.14.0b: the field trip's one morning-after
     *  text — from the classmate closest to you who
     *  rode. FieldTrip.phoneBeat owns the facts; the
     *  harvest only delivers. */
    const val FIELDTRIP = "fieldtrip"
    /** v0.14.0c: the film festival's one morning-after
     *  text — from the winning film's first star, on the
     *  wrap's own morning. Festival.phoneBeat owns the
     *  facts; the harvest only delivers. */
    const val FESTIVAL = "festival"
    /** v0.14.0d: YOUR birthday's one text — on the
     *  birthday morning itself (not the morning after:
     *  the day is the fact), from your closest friend or
     *  from Mom. PlayerBirthday.phoneBeat owns the facts;
     *  the harvest only delivers. */
    const val PLAYERBDAY = "playerbday"
    /** v0.15.0f: the gallery show's judging — one text
     *  on the wrap's own morning, from the winner (a
     *  cast member by construction) or the jury's chair
     *  when the winner is you; an empty show sends
     *  nothing. Gallery.phoneBeat owns the facts; the
     *  harvest only delivers. */
    const val GALLERY = "gallery"
    /** v0.10.0d: a post inside a GROUP thread (the crew's
     *  chat, the family thread, the production's channel).
     *  The thread id names the room; the sender names the
     *  speaker. GroupChats owns the facts and the banks;
     *  the harvest only delivers. */
    const val GROUP = "group"
    const val WEATHER = "weather"
    const val HOLIDAY = "holiday"
    const val BIRTHDAY = "birthday"
    const val HELLO = "hello"
    const val REPLY = "reply"
    const val ACK = "ack"

    val TRIGGERS: Set<String> = setOf(FEAT, PHOTO, ERRAND, COMMISSION, PULL,
        STORY, MYSTERY, WEATHER, HOLIDAY, BIRTHDAY, HELLO, REPLY, ACK,
        DINNER, DINNER_THANKS, VOTE, MOVIE, RIVAL, FAIR, FAMILY, HERO, PLAY,
        GROUP, CARNIVAL, PAPER, DANCE, TALENT, PARADE, FIELDTRIP,
        FESTIVAL, PLAYERBDAY, GALLERY)

    /** The note kinds the event systems may record. */
    val NOTE_KINDS: Set<String> = setOf(FEAT, PHOTO, ERRAND, COMMISSION, PULL)

    // ---------------- caps (the tuning ledger names these) ----
    const val MAX_TEXTS = 220          // whole inbox, stored (120 through
                                       // v0.7.0g; wave 4's 62 townsfolk
                                       // overflowed a full-cast inbox at
                                       // 124, so 140; wave 5's 70 would
                                       // sit exactly AT 140 (70 threads
                                       // × 2) with zero headroom — 160
                                       // keeps eight full threads of
                                       // MAX_PER_THREAD. v0.10.0a: the
                                       // family's two threads make it
                                       // 72 × 2 = 144 — still inside
                                       // 160 with headroom, so the dial
                                       // does NOT move. v0.10.0d: group
                                       // chats add three threads (the
                                       // crew, the family, the play) —
                                       // 75 × 2 = 150 ≤ 160, and the
                                       // dial STILL does not move.
                                       // v0.11.0d: wave 6's audit takes
                                       // the cast to 98, and the worst
                                       // case is honest arithmetic now:
                                       // (98 cast + 2 parents + 3 group
                                       // rooms) × 2 = 206 > 160 — the
                                       // dial is FORCED, and moves to
                                       // 220: fourteen texts of headroom
                                       // over the worst case, and still
                                       // a whole number of MAX_PER_THREAD
                                       // threads)
    const val MAX_PER_THREAD = 20      // per conversation
    const val MAX_UNREAD = 30          // the town stops at a full inbox
    const val MAX_NEW_PER_DAY = 4      // one morning's new texts
    const val MAX_NOTES = 24           // pending noted facts
    const val MAX_TEXT_CHARS = 280     // one text, hard ceiling
    const val REPLY_DAILY_CAP = 3      // friendship bumps per day from replies
    const val REPLY_BONUS = 1          // friendship per capped reply
    const val HELLO_MIN_FRIENDSHIP = 30   // FRIEND tier says hi unprompted
    const val REACT_MIN_FRIENDSHIP = 10   // ACQUAINTANCE tier reacts to your day
    const val HELLO_CHANCE = 0.4       // seeded daily chance of a hello
    const val HELLO_SENDER_GAP_DAYS = 3   // a friend waits before hello-ing again
    const val HELLO_EVERY_DAYS = 2        // at most one hello per two days

    /** Weather worth a complaint text (yesterday's sky, judged
     *  in the morning — the texts are past-tense on purpose). */
    val COMPLAINT_WEATHER: Set<String> = setOf("blizzard", "heavy_snow", "storm")

    /** phoneMeta keys (all ints; the whole store rides the one
     *  additive "phone" save key). */
    private const val META_LAST_ROLL = "lastRoll"
    private const val META_HELLO_DAY = "helloDay"
    private const val META_REPLY_DAY = "replyDay"
    private const val META_REPLIES = "replies"
    private const val META_SNAP_ARC1 = "snapArc1"
    private const val META_SNAP_ARC2 = "snapArc2"
    private const val META_SNAP_ARC3 = "snapArc3"
    private const val META_SNAP_ARC4 = "snapArc4"
    private const val META_SEQ = "seq"

    // ---------------- the hub ----------------

    data class PhoneApp(val id: String, val label: String, val target: String)

    /** The home screen's tiles, in order. [target] names the
     *  REAL surface each tile opens — the suite proves every
     *  one resolves (an activity class, a Journal section, or
     *  the GameActivity picker route). */
    val APPS: List<PhoneApp> = listOf(
        PhoneApp("journal", "📖 Journal", "JournalActivity"),
        PhoneApp("album", "📸 Album", "JournalActivity#album"),
        PhoneApp("map", "🗺 Map", "GameActivity:GoSomewhere"),
        PhoneApp("messages", "💬 Messages", "PhoneActivity:messages"),
        PhoneApp("settings", "⚙️ Settings", "SettingsActivity"),
        PhoneApp("games", "🎮 Games", "JournalActivity#games"),
        // v0.16.0e pass 2: the costume system's door —
        // the kids' game is started/ended from the phone
        PhoneApp("play", "🎭 Play", "FictionActivity")
    )

    fun appById(id: String): PhoneApp? = APPS.firstOrNull { it.id == id }

    /** The status strip over the home screen: the town's time,
     *  its sky, your coin purse, and the badge that matters. */
    fun hubStatus(state: GameState): HubStatus {
        // NB: the trigger const WEATHER shadows GameData's sky
        // map inside this object — qualify the map explicitly.
        val w = com.hunt3r.southparktown.WEATHER[state.weatherId]
        return HubStatus(
            time = minutesToTimeStr(state.timeMin),
            weatherIcon = w?.icon ?: "",
            weatherLabel = w?.label?.substringBefore(" &") ?: "",
            coins = Minigames.capsuleCoins(state),
            unread = unreadCount(state))
    }

    // ---------------- event notes (the hooks) ----------------

    /** Record a bare fact for tomorrow morning's texts.
     *  Notes are facts, not rolls: capped, deduped, and they
     *  convert oldest-first (a busy morning's leftovers wait
     *  for the next one). */
    fun note(state: GameState, kind: String, a: String, b: String = "") {
        if (kind !in NOTE_KINDS) return
        val line = "${state.absDay}|$kind|$a|$b"
        if (line in state.phoneNotes) return
        state.phoneNotes.add(line)
        while (state.phoneNotes.size > MAX_NOTES) state.phoneNotes.removeAt(0)
    }

    /** A feat landed (photo / game / errand / Pocket Pal feat
     *  families all funnel through their own award sites,
     *  which note here). [label] is the feat's display name. */
    fun noteFeat(state: GameState, label: String) = note(state, FEAT, label)

    /** A photo was taken; [who] is a cast member in frame
     *  ("" for a solo selfie), [locId] where. */
    fun notePhoto(state: GameState, who: String, locId: String) =
        note(state, PHOTO, who, locId)

    /** An errand was completed; the asker gets the text. */
    fun noteErrand(state: GameState, asker: String) = note(state, ERRAND, asker)

    /** v0.6.0a: a commissioned print landed; the asker
     *  texts about their photo in the morning. */
    fun noteCommission(state: GameState, asker: String) =
        note(state, COMMISSION, asker)

    /** A capsule pull worth envying (uncommon or rare — the
     *  caller judges; commons are beneath the town's notice). */
    fun notePull(state: GameState, cardName: String) = note(state, PULL, cardName)

    // ---------------- the store ----------------

    fun unreadCount(state: GameState): Int =
        state.phoneTexts.count { !it.read && it.sender != "you" }

    /** Conversations, most recent activity first. */
    fun threads(state: GameState): List<PhoneThread> {
        val byThread = state.phoneTexts.groupBy { it.thread }
        return byThread.entries.map { (name, texts) ->
            PhoneThread(name, texts.last(),
                texts.count { !it.read && it.sender != "you" }, texts.size)
        }.sortedWith(compareByDescending<PhoneThread> { it.last.absDay }
            .thenByDescending { it.last.id })
    }

    /** One conversation, chronological (oldest first). */
    fun threadTexts(state: GameState, name: String): List<PhoneText> =
        state.phoneTexts.filter { it.thread == name }

    fun markThreadRead(state: GameState, name: String) {
        for (t in state.phoneTexts) if (t.thread == name) t.read = true
    }

    /** The one-line status under a name in the Messages list:
     *  pure derivation from live state — where they are, what
     *  their current goal smells like. No new sim. */
    fun statusLine(state: GameState, name: String): String {
        val agent = state.agents[name] ?: return ""
        if (agent.asleep) return "💤 fast asleep"
        val loc = LOCATIONS[agent.location]?.name ?: agent.location
        val goal = state.npcGoals[name]
        val flavor = if (goal != null && !goal.done) when (goal.kind) {
            "SCHEME" -> " · probably scheming"
            "GRUDGE" -> " · holding a grudge"
            "CRAVING" -> " · on a snack mission"
            "MEETUP" -> " · looking for someone"
            "PROJECT" -> " · mid-project"
            "ERRAND" -> " · running an errand"
            "FACTION" -> " · on faction business"
            "WORK" -> " · at work"
            else -> ""
        } else ""
        return "at $loc$flavor"
    }

    // ---------------- Kenny ----------------

    /** Kenny types like he talks: muffled, but every word is
     *  still THERE — a substitution, never a deletion. The
     *  suite pins the recovery rate over every bank. */
    val KENNY_SUBS: List<Pair<String, String>> = listOf(
        "you're" to "ur", "your" to "yr", "you" to "yu",
        "thanks" to "thx", "okay" to "ok", "really" to "rlly",
        "because" to "cuz", "about" to "abt", "people" to "ppl",
        "everybody" to "every1")

    fun kennyfy(text: String): String {
        var t = text.lowercase()
        for ((a, b) in KENNY_SUBS) {
            t = t.replace(Regex("\\b${Regex.escape(a)}\\b"), b)
        }
        return "mmph. $t"
    }

    // ---------------- text banks (all original) ----------------
    // {you} = your name. {x} = the trigger's subject (a feat
    // name, a place, a card, a holiday). Premise-level only:
    // the mystery/story banks CANNOT name outcomes — see the
    // hygiene scan in the suite.

    val FEAT_LINES: List<String> = listOf(
        "ok so. {x}. everybody's talking about it. mostly me. i'm the one talking",
        "heard about {x}?? casual. just casually out there being a legend now",
        "you did {x} and didn't text me first?? rude. proud of you, but rude",
        "{x}!! i already told three people it was partly my idea. you're welcome",
        "so are you famous now or what. {x}. i'm telling everyone i knew you when")

    val PHOTO_LINES_WITH: List<String> = listOf(
        "that photo of us at {x}?? i look amazing. you look… also present. 10/10",
        "saw the selfie from {x}. my hair did not agree to that angle but okay",
        "you kept the {x} one?? i've shown it to two people and a dog already",
        "the {x} photo. iconic. frame it. put it in a museum. a small one")

    val PHOTO_LINES_SOLO: List<String> = listOf(
        "saw your selfie from {x}. bold of you to go solo. respected",
        "the {x} selfie is doing numbers. the number is small but it's doing it",
        "you, alone, at {x}, taking a selfie. cinema. absolute cinema")

    val ERRAND_LINES: List<String> = listOf(
        "thanks again for today. you didn't have to. you did anyway. that's the whole text",
        "still thinking about how you just… did the thing. no complaints, no drama. wild",
        "you're good people, {you}. don't let it go to your head. (it's going to your head)",
        "told everyone you helped me. everyone's impressed. i'm as surprised as you")

    /** v0.6.0a: the morning-after text from a commission's
     *  asker — the print is on their fridge and they have
     *  feelings about it. */
    val COMMISSION_LINES: List<String> = listOf(
        "the photo. it's up. it's ON THE FRIDGE, {you}. that's the highest honor this house has",
        "showed the commissioned photo to three people. three! all agreed: cinema",
        "my picture came out perfect and i'm not even going to be weird about it. (i'm being weird about it)",
        "you actually got the shot i asked for. exactly that one. the town has a photographer now")

    val PULL_LINES: List<String> = listOf(
        "YOU PULLED {x}??? i've opened a million capsules. a MILLION",
        "{x}. from the machine. i saw it happen and i will never recover",
        "trade you for the {x}. i'm serious. name your price. (my price is low. i'm begging here)",
        "the machine gave YOU {x}?? it gives me crumbs and lint. crumbs and lint")

    val STORY_LINES: List<String> = listOf(
        "hey. no details over text. just… thanks for being around lately. it helps",
        "something shifted today. can't explain it in a text. just wanted you to know",
        "you ever feel like a whole chapter of your life just turned a page? anyway. hi")

    val STORY_DONE_LINES: List<String> = listOf(
        "well. that's settled, whatever it was. you'd know. you were basically in it",
        "chapter closed. don't ask me to explain it over text. just… thanks")

    val MYSTERY_LINES: List<String> = listOf(
        "everyone's whispering again. something's up with the case. i know nothing. i never know anything",
        "town's got that look again. the mystery look. keep your eyes open, that's all i'm saying",
        "no details but the vibe right now is extremely investigation. stay sharp, {you}",
        "people keep glancing at the notice board like it owes them money. something's brewing")

    // v0.6.0d: family evenings. The invite is a FACT text —
    // {x} carries dish · place · window, sent by the host
    // on the morning of the dinner (derived, like the rest).
    val DINNER_INVITE_LINES: List<String> = listOf(
        "dinner at ours tonight? i'm making {x}. no occasion. bring an appetite and absolutely no agenda",
        "you're invited. {x}. the table has a chair with your name on it (figuratively. the chairs are unlabeled)",
        "family dinner tonight and you're family-adjacent enough. {x}. come hungry, leave full, that's the whole contract")

    /** The morning of a count (v0.6.0f): City Hall itself
     *  texts the result — McDaniels' office, in its official
     *  capacity as the town's loudest megaphone. {x} = the
     *  winning option's label. */
    val VOTE_RESULT_LINES: List<String> = listOf(
        "official result from city hall: {x} carries it. the people have spoken and the paperwork agrees",
        "the count is in — {x} wins the week. thank you for your service to democracy, however small",
        "ballot update: {x}. the box has been emptied, counted, and returned to its closet with honors",
        "people of south park: {x}. that is all. that is the announcement. — the mayor's office")

    /** Screening morning (v0.7.0c): one of tonight's stars
     *  texts the show. {x} = the feature's title. Stars text
     *  about their own premieres; it's the law of the marquee. */
    val MOVIE_LINES: List<String> = listOf(
        "movie night tonight. {x}. i'm in it. i'm not nervous. i have been nervous since tuesday",
        "they're showing {x} at the bijou tonight and i am somehow the star of it. doors at 7. free. bring opinions",
        "{x} tonight. if you sit near me i will explain the themes. loudly. during. that's the deal")

    /** The morning after a rivalry result (v0.8.0e): THE
     *  RIVAL texts it themselves — a taunt when they took it,
     *  grudging respect when you did, outrage at arithmetic
     *  when it drew. {x} = the contest's label. The sender is
     *  always the rival; the bank follows the recorded winner,
     *  so the text can never claim a result the board denies. */
    val RIVAL_AHEAD_LINES: List<String> = listOf(
        "scoreboard. that's the whole text. ({x}: mine)",
        "{x} was mine and the town knows it. rematch when you're ready to lose again",
        "i've told four people about {x} already. telling you was the victory lap",
        "you up? you looked up during {x}. at me. winning")
    val RIVAL_BEHIND_LINES: List<String> = listOf(
        "{x} was a fluke and history will correct it. rematch",
        "enjoy {x}. i'm framing the rematch as we speak",
        "okay. OKAY. {x} counts. this time. the board and i have notes",
        "i let you have {x}. (i did not let you have {x}.) rematch")
    val RIVAL_DRAW_LINES: List<String> = listOf(
        "{x}: a draw. A DRAW. nobody eats. rematch",
        "we tied at {x}. i hate this town's math and so should you",
        "{x} settled nothing and we both know it. rematch. obviously")

    /** v0.9.0c: the Mayor announces the county fair, three
     *  mornings out ({x} = the fair's date range). One text,
     *  on the announcement morning, in her official voice. */
    val FAIR_LINES: List<String> = listOf(
        "OFFICIAL TOWN BUSINESS: the county fair returns to Kid's Park {x}. booths, contests, a dunk tank, fireworks on closing night. the ribbon-cutting is opening morning and yes, there will be enormous scissors",
        "the fair is coming, {x}, Kid's Park. photo contest, fishing at the pond, someone important in a dunk tank. plan accordingly, citizen",
        "fair week is nearly upon us: {x}. I expect full attendance, full snacking, and at least one (1) memorable dunking. — the Mayor's office")
    val FAIR_SHIFTED_LINES: List<String> = listOf(
        "SCHEDULING NOTICE: the county fair moves UP a week this year — {x}, Kid's Park — so it doesn't fight the Regatta for the pond's week. the fair yields. the fair is gracious. tell everyone",
        "the fair comes early this year: {x}. one week up, by order of not-fighting-the-Regatta. same booths, same tank, same fireworks. — the Mayor's office")

    /** v0.11.0e: the Mayor announces the winter carnival,
     *  three mornings out ({x} = the carnival's date range). */
    val CARNIVAL_LINES: List<String> = listOf(
        "OFFICIAL TOWN BUSINESS: the winter carnival returns to Stark's Pond {x}. ice fishing, snow sculptures, lanterns on the ice. jimbo drills the first hole opening morning and yes, the auger has a name",
        "the carnival is coming, {x}, Stark's Pond — on the ice itself. carve something, catch something, drink something hot. plan accordingly, citizen",
        "carnival week is nearly upon us: {x}. I expect full attendance, full mittens, and at least one (1) sculpture the town argues about until spring. — the Mayor's office")

    /** v0.12.0a: publication morning — a cast subscriber
     *  texts the Gazette's headline ({x} = the headline,
     *  verbatim). The sender rotates by week through the
     *  cast (Newspaper.summarySender owns the rotation);
     *  the lines may react to the headline but state no
     *  fact the edition doesn't — the headline itself and
     *  the porch copy are the whole record here. */
    val PAPER_LINES: List<String> = listOf(
        "paper's out. \"{x}\" — front page. there's a copy on your porch, go get it",
        "gazette day. \"{x}\". that's the headline. yours is waiting on the porch",
        "did you see the paper? \"{x}\". everybody's reading it. porch copy has your name on it")

    /** The morning after an invited dinner you attended:
     *  the host, remembering it fondly ({x} = the family). */
    val DINNER_THANKS_LINES: List<String> = listOf(
        "last night was a good one. {x} do not eat like that every night, but we ate like that last night. come again soon",
        "still thinking about dinner. the table felt complete. thank you for the chair-filling, kid",
        "leftovers report: gone by noon. that's a review, by the way. five stars")

    /** v0.10.0a: tonight's family-dinner call ({x} = the dish,
     *  the house and the window). From whichever parent is on
     *  dinner duty — they alternate, like the cooking. */
    val FAMILY_DINNER_LINES: List<String> = listOf(
        "family dinner tonight: {x}. be there, be hungry, bring one (1) story from your day",
        "dinner at home tonight — {x}. your chair misses you. it told me so",
        "tonight, {x}. family rules: sit, eat, tell us everything, in that order")

    /** v0.10.0a: the morning after a family dinner you sat at. */
    val FAMILY_THANKS_LINES: List<String> = listOf(
        "last night's dinner is still being talked about at breakfast. that's how you know",
        "good table last night. full chairs, empty plates. the system works",
        "your father is still describing the dinner to people at work. it has a fan club now")

    /** v0.10.0a: the morning after a family dinner nobody sat at. */
    val FAMILY_MISSED_LINES: List<String> = listOf(
        "you missed family dinner. there's a plate in the fridge with your name on it. literally. i wrote it on the foil",
        "dinner happened without you. the chair was very brave about it. plate's in the fridge",
        "we saved you a plate. it's in the fridge, under a towel, like a tiny delicious secret")

    /** v0.10.0b: the morning after a patrol night you were
     *  out on — a parent, on the record, about the cape.
     *  The curfew talk is affection in a trench coat. */
    val HERO_PARENT_LINES: List<String> = listOf(
        "you were out until ten. in a cape. the cape has been NOTED in the family record",
        "a neighbor saw 'a small hero' by the pond. a small hero with our whole last-name energy. home by ten means home by ten, hero",
        "your father's verdict on the patrol: 'the cape is growing on me.' that's dad for proud. don't let it go to your head",
        "grass stains on a cape. GRASS STAINS. there is a story and i expect to hear it at dinner",
        "if you're going to be the neighborhood watch, the neighborhood watch eats a vegetable first. those are the terms")

    /** v0.10.0a: allowance morning ({x} = the coins). */
    val FAMILY_ALLOWANCE_LINES: List<String> = listOf(
        "allowance day: {x} 🪙 on the kitchen counter, under the fruit bowl. dinner attendance noted and appreciated, as always",
        "{x} 🪙 — allowance, on the counter. you showed up at the table this week, so the table provides")

    /** The morning after a dinner YOU hosted: the guests,
     *  still talking about it ({x} = the family). */
    val DINNER_HOSTED_THANKS_LINES: List<String> = listOf(
        "your house. your table. your rules — and somehow we all behaved. thank you for having {x}",
        "we talked about your dinner all the way home. ALL the way. that's not a short walk for some of us",
        "a hosted kid is a raised-right kid. the table was lovely. the company was lovelier. — {x}")

    val WEATHER_LINES: List<String> = listOf(
        "this weather is a personal attack and i'll be filing a complaint with the sky",
        "woke up and the snow was up to my everything. colorado is a prank and we're the punchline",
        "the storm last night relocated my trash cans to another area code. anyway. morning")

    val WEATHER_NICE_LINES: List<String> = listOf(
        "don't tell anyone i said this but today is actually… nice?? suspicious",
        "sun's out. everyone's acting weird about it. by weird i mean happy")

    val HOLIDAY_TODAY_LINES: List<String> = listOf(
        "IT'S {x}!! this is not a drill. this is the actual day. act accordingly",
        "happy {x}!! i've been ready since yesterday. i was born ready. for this specifically")

    val HOLIDAY_SOON_LINES: List<String> = listOf(
        "{x} tomorrow. i have plans. they involve snacks and a respectable amount of chaos",
        "psst. {x} is tomorrow. prepare yourself. i've already prepared snacks about it")

    val BIRTHDAY_LINES: List<String> = listOf(
        "it's my birthday today. this text is legally a hint",
        "another year older, another year of you owing me cake. (this is about cake)")

    val HELLO_LINES: List<String> = listOf(
        "no reason. just texting my favorite person in this town. don't make it weird",
        "you alive? you haven't caused a single incident today and it's making me nervous",
        "thinking about you. not in a weird way. in a 'we should get snacks' way",
        "roll call. favorite person: present? reply so i can check the box")

    /** The sender's answer to a quick reply — canned, warm,
     *  and never a conversation tree. */
    val ACK_LINES: Map<String, List<String>> = mapOf(
        "👍" to listOf("👍", "knew you'd get it", "see? this is why we're friends"),
        "😂" to listOf("don't laugh, it's serious (it's not serious)",
            "😂 right??", "i'm here all week. tip your texter"),
        "❤️" to listOf("❤️", "okay that's actually sweet. shutting up now",
            "noted. filed under: reasons you're my favorite"))

    val QUICK_REPLIES: List<String> = listOf("👍", "😂", "❤️")

    // ---------------- generation ----------------

    private fun playerName(state: GameState): String =
        state.agents.values.firstOrNull { it.isPlayer }?.name ?: "new kid"

    /** A friend who might plausibly text about your day:
     *  deterministic under [rnd] — acquaintances or better,
     *  else the closest thing you have to a friend, else
     *  Butters (Butters texts everybody; it's canon to him). */
    fun pickFriend(state: GameState, rnd: Random,
                   minFriendship: Int = REACT_MIN_FRIENDSHIP,
                   exclude: Set<String> = emptySet()): String {
        val elig = KID_ORDER.filter {
            it !in exclude && (state.friendship[it] ?: 0) >= minFriendship
        }
        if (elig.isNotEmpty()) return elig[rnd.nextInt(elig.size)]
        val best = KID_ORDER.filter { it !in exclude }
            .maxByOrNull { state.friendship[it] ?: 0 }
        return best ?: "Butters"
    }

    private fun render(state: GameState, sender: String, template: String,
                       subject: String): String {
        var t = template.replace("{you}", playerName(state))
            .replace("{x}", subject)
        if (sender == "Kenny") t = kennyfy(t)
        return t.take(MAX_TEXT_CHARS)
    }

    private fun pick(bank: List<String>, rnd: Random): String =
        bank[rnd.nextInt(bank.size)]

    /** Append a text to the store, stamped with the
     *  store's own sequence (the id is the seq's, so ids
     *  never collide across mornings). Solo generation
     *  threads by sender; group posts (GroupChats) thread
     *  by room and name their speaker. */
    fun appendText(state: GameState, thread: String, sender: String,
                   trigger: String, text: String,
                   emoji: String = ""): PhoneText {
        val seq = (state.phoneMeta[META_SEQ] ?: 0) + 1
        state.phoneMeta[META_SEQ] = seq
        val t = PhoneText(
            id = "${state.absDay}:$trigger:$seq",
            thread = thread, sender = sender, trigger = trigger,
            absDay = state.absDay, dayKey = state.todayKey,
            text = text, read = false, emoji = emoji)
        state.phoneTexts.add(t)
        return t
    }

    /** Append a generated solo text (arriving this morning). */
    private fun deliver(state: GameState, sender: String, trigger: String,
                        text: String) {
        appendText(state, sender, sender, trigger, text)
    }

    /** The store's caps, enforced after every generation and
     *  on decode: per-thread first (shed that thread's oldest
     *  READ texts, then its oldest), then the whole inbox. */
    fun enforceCaps(state: GameState) {
        for (name in state.phoneTexts.map { it.thread }.distinct()) {
            val texts = state.phoneTexts.filter { it.thread == name }
            var excess = texts.size - MAX_PER_THREAD
            if (excess <= 0) continue
            // shed this thread's oldest READ texts first,
            // then its oldest unread — history before news.
            val readOld = texts.filter { it.read }
            val dropRead = readOld.take(excess)
            state.phoneTexts.removeAll(dropRead)
            excess -= dropRead.size
            if (excess > 0) {
                val rest = state.phoneTexts.filter { it.thread == name }
                state.phoneTexts.removeAll(rest.take(excess))
            }
        }
        while (state.phoneTexts.size > MAX_TEXTS) {
            val oldestRead = state.phoneTexts.firstOrNull { it.read }
            state.phoneTexts.remove(oldestRead ?: state.phoneTexts.first())
        }
    }

    /** Tomorrow's holiday, if one STARTS tomorrow ("MMdd"
     *  math on the in-game calendar). */
    private fun holidayStartingTomorrow(state: GameState): Holiday? {
        var m = state.gameMonth; var d = state.gameDay + 1
        if (d > daysInMonth(state.gameYear, m)) {
            d = 1; m++
            if (m > 12) m = 1
        }
        val md = "%02d%02d".format(m, d)
        return HOLIDAYS.firstOrNull { it.startMd == md }
    }

    /** The morning's harvest. Called by Engine.onNewDay AFTER
     *  the date has advanced and the story arcs have ticked:
     *  yesterday's noted facts + today's derived triggers
     *  become texts, deterministically. Idempotent per day. */
    fun dailyRollover(state: GameState) {
        if (state.phoneMeta[META_LAST_ROLL] == state.absDay) return
        state.phoneMeta[META_LAST_ROLL] = state.absDay
        // snapshots advance even on a full inbox — the town's
        // moments pass whether or not you're reading.
        val storyHits = mutableListOf<Pair<String, Boolean>>() // (sender, finished)
        for ((arcId, rec) in state.storyArcs) {
            val def = StoryArcs.ARCS.firstOrNull { it.id == arcId } ?: continue
            val sender = def.parties.firstOrNull() ?: continue
            if (rec.finishedAbsDay == state.absDay) storyHits.add(sender to true)
            else if (rec.stageStartAbsDay == state.absDay && rec.stage > 0)
                storyHits.add(sender to false)
        }
        val arcSnap = (state.arcChapter * 100) +
            (if (state.arcStarted) 1 else 0)
        val arc2Snap = (if (state.arc2Started) 2 else 0) +
            (if (state.arc2AccusationAired) 1 else 0)
        val arc3Snap = (state.arc3Chapter * 100) +
            (if (state.arc3Started) 1 else 0)
        val arc4Snap = (state.arc4Chapter * 100) +
            (if (state.arc4Started) 1 else 0)
        val mysteryMoved = (state.phoneMeta[META_SNAP_ARC1] ?: arcSnap) != arcSnap ||
            (state.phoneMeta[META_SNAP_ARC2] ?: arc2Snap) != arc2Snap ||
            (state.phoneMeta[META_SNAP_ARC3] ?: arc3Snap) != arc3Snap ||
            (state.phoneMeta[META_SNAP_ARC4] ?: arc4Snap) != arc4Snap
        state.phoneMeta[META_SNAP_ARC1] = arcSnap
        state.phoneMeta[META_SNAP_ARC2] = arc2Snap
        state.phoneMeta[META_SNAP_ARC3] = arc3Snap
        state.phoneMeta[META_SNAP_ARC4] = arc4Snap

        if (unreadCount(state) >= MAX_UNREAD) {
            // a full inbox: notes wait (facts keep), the town
            // does not shout into the void.
            enforceCaps(state)
            return
        }

        var budget = MAX_NEW_PER_DAY
        val usedSenders = mutableSetOf<String>()
        fun send(sender: String, trigger: String, text: String): Boolean {
            if (budget <= 0 || sender in usedSenders) return false
            if (unreadCount(state) >= MAX_UNREAD) return false
            deliver(state, sender, trigger, text)
            usedSenders.add(sender)
            budget--
            return true
        }

        // ---- story beats (derived, highest priority) ----
        for ((sender, finished) in storyHits) {
            val rnd = Random(seedFor(state.absDay, STORY, sender))
            val bank = if (finished) STORY_DONE_LINES else STORY_LINES
            send(sender, STORY, render(state, sender, pick(bank, rnd), ""))
        }
        // ---- a mystery moved (derived, vague by template) ----
        if (mysteryMoved) {
            val rnd = Random(seedFor(state.absDay, MYSTERY, "town"))
            val sender = pickFriend(state, rnd)
            send(sender, MYSTERY, render(state, sender, pick(MYSTERY_LINES, rnd), ""))
        }
        // ---- family evenings (v0.6.0d, derived) ----
        // Tonight's invitation, from the host's own phone —
        // and last night's thanks, from whichever table you
        // sat at. Both anchor on the records
        // Dinners.dailyRollover wrote just before this
        // harvest ran (Engine.onNewDay's order is pinned).
        Dinners.anchoredInvite(state)?.let { inv ->
            val rnd = Random(seedFor(state.absDay, DINNER, inv.id))
            val place = LOCATIONS[inv.house]?.name ?: inv.house
            val x = "${inv.dish} · $place · ${Dinners.windowLabel()}"
            send(inv.host, DINNER,
                render(state, inv.host, pick(DINNER_INVITE_LINES, rnd), x))
        }
        Dinners.wrapFor(state, state.absDay - 1)?.let { (wDef, wKind) ->
            val rnd = Random(seedFor(state.absDay, DINNER_THANKS, wDef.id))
            val bank = if (wKind == "hosted") DINNER_HOSTED_THANKS_LINES
                else DINNER_THANKS_LINES
            send(wDef.host, DINNER_THANKS,
                render(state, wDef.host, pick(bank, rnd), wDef.family))
        }
        // ---- your own family's table (v0.10.0a, derived) ----
        // Family.dailyRollover ran just before this harvest
        // (Engine.onNewDay's order is pinned), so yesterday's
        // wrap and this morning's allowance are facts already.
        // The dinner-duty parent alternates by the day, like
        // the cooking does.
        if (Family.arrived(state)) {
            val duty = if (state.absDay % 2 == 0) Family.MOM
                else Family.DAD
            val other = if (duty == Family.MOM) Family.DAD
                else Family.MOM
            Family.wrapFor(state, state.absDay - 1)?.let { (attended, _) ->
                val rnd = Random(seedFor(state.absDay, FAMILY, "wrap"))
                if (attended) send(other, FAMILY,
                    render(state, other, pick(FAMILY_THANKS_LINES, rnd), ""))
                else send(duty, FAMILY,
                    render(state, duty, pick(FAMILY_MISSED_LINES, rnd), ""))
            }
            if ((state.life["famAllowanceDay"] ?: -1) == state.absDay) {
                val rnd = Random(seedFor(state.absDay, FAMILY, "allowance"))
                send(Family.MOM, FAMILY,
                    render(state, Family.MOM,
                        pick(FAMILY_ALLOWANCE_LINES, rnd),
                        "${Family.ALLOWANCE_COINS}"))
            }
            if (Family.dinnerTonight(state)) {
                val rnd = Random(seedFor(state.absDay, FAMILY, "dinner"))
                val x = "${Family.dishOf(state.absDay)} · Your House · " +
                    Dinners.windowLabel()
                send(duty, FAMILY,
                    render(state, duty, pick(FAMILY_DINNER_LINES, rnd), x))
            }
        }
        // ---- hero nights (v0.10.0b, derived) ----
        // Yesterday was a patrol night and you were OUT on
        // it: Heroes.dailyRollover recorded it before this
        // harvest (Engine.onNewDay's order), and a parent
        // has opinions about the cape. The sender alternates
        // by the day, like the dinner duty does.
        if (Family.arrived(state) && Heroes.heroCreated(state) &&
            (state.life["heroLastPatrol"] ?: -1) == state.absDay - 1) {
            val sender = if (state.absDay % 2 == 0) Family.MOM
                else Family.DAD
            val rnd = Random(seedFor(state.absDay, HERO, "patrol"))
            send(sender, HERO,
                render(state, sender, pick(HERO_PARENT_LINES, rnd), ""))
        }
        // ---- the count (v0.6.0f, derived) ----
        // A result counted at this morning's rollover is
        // texted by City Hall itself: Votes.dailyRollover ran
        // just before this harvest (Engine.onNewDay's order
        // is pinned), so a fresh result is this morning's.
        Votes.freshResult(state)?.let { res ->
            val rnd = Random(seedFor(state.absDay, VOTE, res.questionId))
            send("McDaniels", VOTE,
                render(state, "McDaniels", pick(VOTE_RESULT_LINES, rnd),
                    res.winnerLabel()))
        }
        // ---- the county fair announced (v0.9.0c, derived) ----
        // Three mornings before opening, the Mayor texts
        // the fair herself. Fair.dailyRollover ran just
        // before this harvest (Engine.onNewDay's order is
        // pinned); the phone only marks it phoned when the
        // text actually lands — a full inbox means the
        // Mayor's office waits like everyone else.
        if (Fair.announceDue(state)) {
            val w = FairCalendar.nextWindowAfter(Fair.todayOrdinal(state))
            val rnd = Random(seedFor(state.absDay, FAIR, "fair${w.year}"))
            val bank = if (w.shifted) FAIR_SHIFTED_LINES else FAIR_LINES
            val range = "${Fair.dateLabelOf(w.startOrd)}–" +
                Fair.dateLabelOf(w.endOrd)
            if (send("McDaniels", FAIR,
                    render(state, "McDaniels", pick(bank, rnd), range))) {
                Fair.markPhoned(state, w.year)
            }
        }
        // ---- the winter carnival announced (v0.11.0e, derived) ----
        // Three mornings before opening, the Mayor texts
        // the carnival herself, on her own thread — the
        // inbox math is the fair's: an existing thread, so
        // (98 cast + 2 parents + 3 rooms) × 2 = 206 ≤
        // MAX_TEXTS 220 stands and the dial does not move.
        if (Carnival.announceDue(state)) {
            val w = CarnivalCalendar.nextWindowAfter(Carnival.todayOrdinal(state))
            val rnd = Random(seedFor(state.absDay, CARNIVAL, "carn${w.year}"))
            val range = "${Carnival.dateLabelOf(w.startOrd)}–" +
                Carnival.dateLabelOf(w.endOrd)
            if (send("McDaniels", CARNIVAL,
                    render(state, "McDaniels", pick(CARNIVAL_LINES, rnd), range))) {
                Carnival.markPhoned(state, w.year)
            }
        }
        // ---- the school play (v0.10.0c, derived) ----
        // Play.dailyRollover ran just before this harvest
        // (Engine.onNewDay's order is pinned), so this
        // morning's play fact — the poster, the cast list,
        // opening night, the review — is textable now.
        // Play.phoneBeat returns the one beat due today,
        // fully composed from the record; the harvest just
        // delivers it under the PLAY trigger.
        Play.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, PLAY, text)
        }
        // ---- the school dance (v0.13.0b, derived) ----
        // Dance.dailyRollover ran just before this harvest
        // and wrapped the night, so the morning-after fact
        // — you were there with somebody, you were there
        // stag, or you weren't there at all — is textable
        // now. Dance.phoneBeat returns the one beat due,
        // fully composed from the record; the harvest just
        // delivers it under the DANCE trigger.
        Dance.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, DANCE, text)
        }
        // ---- the talent show (v0.13.0c, derived) ----
        // Talent.dailyRollover ran just before this
        // harvest and wrapped the night, so the morning-
        // after fact — the winner, the act, the meter —
        // is textable now. Talent.phoneBeat returns the
        // one beat due, fully composed from the record;
        // the harvest just delivers it under the TALENT
        // trigger.
        Talent.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, TALENT, text)
        }
        // ---- the parade (v0.13.0d, derived) ----
        // Parade.dailyRollover ran just before this
        // harvest and wrapped the day, so the morning-
        // after fact — who led, the mayor's close, your
        // part — is textable now. Parade.phoneBeat returns
        // the one beat due, fully composed from the
        // record; the harvest just delivers it under the
        // PARADE trigger.
        Parade.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, PARADE, text)
        }
        // ---- the field trip (v0.14.0b, derived) ----
        // FieldTrip.dailyRollover ran just before this
        // harvest and wrapped the day, so the morning-
        // after fact — you rode and how the beat went,
        // or the bus went without you — is textable now.
        // FieldTrip.phoneBeat returns the one beat due,
        // fully composed from the record; the harvest
        // just delivers it under the FIELDTRIP trigger.
        FieldTrip.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, FIELDTRIP, text)
        }
        // ---- the film festival (v0.14.0c, derived) ----
        // Festival.dailyRollover ran just before this
        // harvest and wrapped the year, so the morning-
        // after fact — who took the Golden Popcorn, and
        // whether you were in the house for it — is
        // textable now. Festival.phoneBeat returns the
        // one beat due, fully composed from the record;
        // the harvest just delivers it under the
        // FESTIVAL trigger.
        Festival.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, FESTIVAL, text)
        }
        // ---- your birthday (v0.14.0d, derived) ----
        // PlayerBirthday.dailyRollover ran just before
        // this harvest and landed the gift, the card and
        // the yield stamp, so the birthday itself — the
        // day it IS — is textable now, this morning, not
        // the morning after. PlayerBirthday.phoneBeat
        // returns the one beat due, fully composed; the
        // harvest just delivers it under the PLAYERBDAY
        // trigger. The sender is your closest friend or
        // Mom: the closed admission set either way.
        PlayerBirthday.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, PLAYERBDAY, text)
        }
        // ---- the gallery show (v0.15.0f, derived) ----
        // Gallery.dailyRollover ran earlier in this same
        // morning chain and landed the wrap, so the
        // judging is textable now, on the wrap's own
        // morning — exactly one text, sender in the
        // closed set either way (the winner is a cast
        // member; the chair is one of six named jurors).
        // An empty show sends nothing: nothing happened
        // to anyone.
        Gallery.phoneBeat(state)?.let { (sender, _, text) ->
            send(sender, GALLERY, text)
        }
        // ---- tonight's screening (v0.7.0c, derived) ----
        // On a live screening morning, one of the feature's
        // stars texts the show (the sender is tonight's
        // feature's first star — the premiere's own herald,
        // charged against the same per-sender/day budget as
        // every other derived send).
        if (budget > 0) MovieNights.textSender(state)?.let { star ->
            val f = MovieNights.featureFor(state)
            val rnd = Random(seedFor(state.absDay, MOVIE, f.key))
            send(star, MOVIE,
                render(state, star, pick(MOVIE_LINES, rnd), f.title))
        }
        // ---- the rivalry (v0.8.0e, derived) ----
        // The morning after a result, THE RIVAL texts it —
        // the bank follows the recorded winner (taunt /
        // grudging / draw), and the stamp lands only when
        // the send actually happens, so a crowded inbox
        // defers the grudge rather than dropping it.
        if (budget > 0) Rival.textDue(state)?.let { due ->
            val rnd = Random(seedFor(state.absDay, RIVAL, due.rival))
            val bank = when (due.winner) {
                "them" -> RIVAL_AHEAD_LINES
                "you" -> RIVAL_BEHIND_LINES
                else -> RIVAL_DRAW_LINES
            }
            if (send(due.rival, RIVAL,
                    render(state, due.rival, pick(bank, rnd), due.kindLabel)))
                Rival.markTexted(state)
        }
        // ---- yesterday's noted facts, oldest first ----
        val due = state.phoneNotes.filter {
            (it.split("|").getOrNull(0)?.toIntOrNull() ?: state.absDay) < state.absDay
        }
        val consumed = mutableListOf<String>()
        for (raw in due) {
            if (budget <= 0) break   // the rest wait for tomorrow
            val parts = raw.split("|")
            val noteDay = parts.getOrNull(0)?.toIntOrNull()
            val kind = parts.getOrNull(1) ?: ""
            if (noteDay == null || kind !in NOTE_KINDS) {
                consumed.add(raw)   // a malformed note is shed, never retried
                continue
            }
            val a = parts.getOrNull(2) ?: ""
            val b = parts.getOrNull(3) ?: ""
            val rnd = Random(seedFor(noteDay, kind, "$a|$b"))
            when (kind) {
                FEAT -> {
                    val sender = pickFriend(state, rnd)
                    send(sender, FEAT, render(state, sender, pick(FEAT_LINES, rnd), a))
                }
                PHOTO -> {
                    val place = LOCATIONS[b]?.name ?: b
                    if (a.isNotEmpty() && a in KID_ORDER) {
                        send(a, PHOTO, render(state, a, pick(PHOTO_LINES_WITH, rnd), place))
                    } else {
                        val sender = pickFriend(state, rnd)
                        send(sender, PHOTO, render(state, sender, pick(PHOTO_LINES_SOLO, rnd), place))
                    }
                }
                ERRAND -> if (a in KID_ORDER) {
                    send(a, ERRAND, render(state, a, pick(ERRAND_LINES, rnd), ""))
                }
                COMMISSION -> if (a in KID_ORDER) {
                    send(a, COMMISSION,
                        render(state, a, pick(COMMISSION_LINES, rnd), ""))
                }
                PULL -> {
                    val sender = pickFriend(state, rnd)
                    send(sender, PULL, render(state, sender, pick(PULL_LINES, rnd), a))
                }
            }
            // an ATTEMPTED note is spent — delivered or crowded
            // out by its sender's other text that morning, the
            // fact never converts twice (never a reroll).
            consumed.add(raw)
        }
        state.phoneNotes.removeAll(consumed)

        // ---- birthdays & holidays (derived) ----
        // v0.15.0i: EVERY birthday today gets its beat —
        // on a shared day, both (or all) land. Each
        // birthday person is their own sender, so the
        // per-sender rule never eats the second name.
        // THE SHEDDING ORDER, stated whole: story,
        // mystery, family evenings, the composed event
        // beats and yesterday's noted facts stand ahead
        // of a birthday (on a morning those fill, a
        // birthday can still be the shed — in the open,
        // by the same rule a single birthday has always
        // lived under); the birthday stands ahead of the
        // group chats and the paper's summary below —
        // the two beats that defer whole by their own
        // design, since a birthday is bound to its day
        // and they are not — and ahead of the
        // holiday/weather/hello chatter, which a crowded
        // morning sheds first of all; and the stored-
        // inbox cap sheds oldest read texts, never a
        // birthday for a second name's sake.
        for (who in Birthdays.birthdaysToday(state)) {
            if (who in KID_ORDER) {
                val rnd = Random(seedFor(state.absDay, BIRTHDAY, who))
                send(who, BIRTHDAY, render(state, who, pick(BIRTHDAY_LINES, rnd), ""))
            }
        }
        // ---- group chats (v0.10.0d, derived) ----
        // (Seated after the birthdays since v0.15.0i:
        // a birthday is bound to its day — unsent, it
        // is gone — while these posts defer whole by
        // their own design, so on a crowded morning
        // the deferrable beats are what shed.)
        // The crew's chat, the family thread and the
        // production's channel post in this same harvest.
        // GroupChats.duePosts derives every post from a
        // recorded fact (a fact is stamped only when its
        // post lands, so a crowded morning defers it
        // whole), and the posts spend this harvest's own
        // currency: the shared daily budget and the unread
        // wall — plus a group ceiling of their own, so a
        // loud room can never eat the town's morning.
        if (budget > 0) {
            var groupSent = 0
            for (post in GroupChats.duePosts(state)) {
                if (budget <= 0 ||
                    groupSent >= GroupChats.MAX_GROUP_PER_DAY) break
                if (unreadCount(state) >= MAX_UNREAD) break
                GroupChats.commit(state, post)
                budget--; groupSent++
            }
        }
        // ---- the paper's summary (v0.12.0a, derived) ----
        // The Gazette published (Newspaper.dailyRollover ran
        // earlier in this same onNewDay and stamped the
        // debt): a rotating cast subscriber texts the
        // headline verbatim on their own thread. The stamp
        // clears only when the text actually lands — a
        // crowded morning defers it whole, like the fair's
        // announcement — and the send spends the shared
        // daily budget like every other derived beat.
        if (Newspaper.summaryDue(state)) {
            val headline = Newspaper.latestHeadline(state)
            if (headline == null) {
                // an edition with no readable headline is
                // not a text waiting to happen — clear the
                // debt rather than retry it forever.
                Newspaper.markSummarySent(state)
            } else {
                val sender = Newspaper.summarySender(state)
                val rnd = Random(seedFor(state.absDay, PAPER,
                    "week${Newspaper.publishedWeek(state)}"))
                if (send(sender, PAPER,
                        render(state, sender,
                            pick(PAPER_LINES, rnd), headline)))
                    Newspaper.markSummarySent(state)
            }
        }
        val holiday = state.holidayId?.let { id -> HOLIDAYS.firstOrNull { it.id == id } }
        if (holiday != null) {
            val rnd = Random(seedFor(state.absDay, HOLIDAY, holiday.id))
            val sender = pickFriend(state, rnd)
            send(sender, HOLIDAY, render(state, sender, pick(HOLIDAY_TODAY_LINES, rnd), holiday.name))
        } else {
            holidayStartingTomorrow(state)?.let { soon ->
                val rnd = Random(seedFor(state.absDay, HOLIDAY, soon.id))
                val sender = pickFriend(state, rnd)
                send(sender, HOLIDAY, render(state, sender, pick(HOLIDAY_SOON_LINES, rnd), soon.name))
            }
        }
        // ---- the sky, complained about (derived) ----
        if (state.weatherId in COMPLAINT_WEATHER) {
            val rnd = Random(seedFor(state.absDay, WEATHER, state.weatherId))
            val sender = pickFriend(state, rnd)
            send(sender, WEATHER, render(state, sender, pick(WEATHER_LINES, rnd), ""))
        } else if (state.weatherId == "sunny") {
            val rnd = Random(seedFor(state.absDay, WEATHER, "sunny"))
            if (rnd.nextDouble() < 0.3) {
                val sender = pickFriend(state, rnd)
                send(sender, WEATHER, render(state, sender, pick(WEATHER_NICE_LINES, rnd), ""))
            }
        }
        // ---- a close friend just saying hi (derived, paced) ----
        val lastHello = state.phoneMeta[META_HELLO_DAY] ?: -100
        if (state.absDay - lastHello >= HELLO_EVERY_DAYS) {
            val rnd = Random(seedFor(state.absDay, HELLO, "town"))
            if (rnd.nextDouble() < HELLO_CHANCE) {
                val candidates = KID_ORDER.filter { name ->
                    (state.friendship[name] ?: 0) >= HELLO_MIN_FRIENDSHIP &&
                        state.absDay - lastTextDay(state, name) >= HELLO_SENDER_GAP_DAYS
                }
                if (candidates.isNotEmpty()) {
                    val sender = candidates[rnd.nextInt(candidates.size)]
                    if (send(sender, HELLO, render(state, sender, pick(HELLO_LINES, rnd), ""))) {
                        state.phoneMeta[META_HELLO_DAY] = state.absDay
                    }
                }
            }
        }
        enforceCaps(state)
    }

    /** The absDay of [name]'s most recent text (-100 = never). */
    private fun lastTextDay(state: GameState, name: String): Int =
        state.phoneTexts.filter { it.thread == name }
            .maxOfOrNull { it.absDay } ?: -100

    /** The one seed rule: a fact's words depend on the fact,
     *  never on when anyone got around to reading it. */
    private fun seedFor(day: Int, kind: String, subject: String): Long =
        (day * 1_000_003L) + (kind.hashCode() * 31L) + subject.hashCode()

    // ---------------- quick replies (the event hook) ---------

    data class ReplyResult(val bonusApplied: Boolean, val ack: String?)

    /** Tap a canned reply in [thread]: your bubble lands, the
     *  friendship warms by +1 (REPLY_DAILY_CAP bonuses a day,
     *  through the town's own relAdjust so every clamp and
     *  tier rule still applies), a "texting" topic thread
     *  plants in their memory of you (the Threads banks pick
     *  it up like any topic), and half the time — seeded by
     *  the conversation itself — they text right back. */
    fun quickReply(state: GameState, thread: String, emoji: String): ReplyResult {
        if (thread !in KID_ORDER || emoji !in QUICK_REPLIES) return ReplyResult(false, null)
        val seq = (state.phoneMeta[META_SEQ] ?: 0) + 1
        state.phoneMeta[META_SEQ] = seq
        state.phoneTexts.add(PhoneText(
            id = "${state.absDay}:$REPLY:$seq",
            thread = thread, sender = "you", trigger = REPLY,
            absDay = state.absDay, dayKey = state.todayKey,
            text = emoji, read = true, emoji = emoji))
        // the friendship bump, capped per day
        var bonus = false
        if (state.phoneMeta[META_REPLY_DAY] != state.absDay) {
            state.phoneMeta[META_REPLY_DAY] = state.absDay
            state.phoneMeta[META_REPLIES] = 0
        }
        if ((state.phoneMeta[META_REPLIES] ?: 0) < REPLY_DAILY_CAP) {
            state.phoneMeta[META_REPLIES] = (state.phoneMeta[META_REPLIES] ?: 0) + 1
            Engine().relAdjust(state, thread, REPLY_BONUS)
            bonus = true
        }
        // plant the thread topic ("texting" — the follow-up
        // banks treat it like any shared topic)
        val mem = NpcMemoryStore.getOrCreate(thread)
        mem.topics.remove("texting")
        mem.topics.add("texting")
        while (mem.topics.size > 6) mem.topics.removeAt(0)
        // the reply back, seeded by the conversation's own size
        var ack: String? = null
        val rnd = Random(seedFor(state.absDay, ACK, "$thread:${state.phoneTexts.size}"))
        if (rnd.nextDouble() < 0.5) {
            val bank = ACK_LINES[emoji] ?: emptyList()
            if (bank.isNotEmpty()) {
                ack = render(state, thread, pick(bank, rnd), "")
                val ackSeq = (state.phoneMeta[META_SEQ] ?: 0) + 1
                state.phoneMeta[META_SEQ] = ackSeq
                state.phoneTexts.add(PhoneText(
                    id = "${state.absDay}:$ACK:$ackSeq",
                    thread = thread, sender = thread, trigger = ACK,
                    absDay = state.absDay, dayKey = state.todayKey,
                    text = ack, read = true))
            }
        }
        enforceCaps(state)
        return ReplyResult(bonus, ack)
    }
}

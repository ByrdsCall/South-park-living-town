package com.hunt3r.southparktown

// ============================================================
// BIRTHDAYS (v0.12.0c, Queue #12 item 3) — the town's
// birthdays, on the record.
//
// Until now the town kept twelve birthdays (#55, TownLife) —
// a banner, a text, a little free-time gravity. This build
// puts ALL 98 cast members on the calendar and gives the
// day four honest beats:
//
//   THE RECORD — one authored month/day per cast member in
//   DATES below. SOURCING, kept straight forever: the
//   lorebook itself states exactly ONE cast birthday —
//   Clyde Donovan, "born April 10 (per his Facebook profile
//   in 'You Have 0 Friends')" — so LOREBOOK_SOURCED holds
//   Clyde alone, pinned in the suite against the lorebook
//   file read directly. The other eleven #55 dates shipped
//   earlier and stand unchanged (PRE_EXISTING); the
//   remaining 86 are seeded-authored here (seed 12003: a
//   shuffled draw of days-of-year in roster order, 6–11
//   birthdays a month, four days shared — real towns share
//   birthdays, and every system below handles a shared day
//   instead of pretending otherwise). An authored date is
//   NEVER canon; the split lives in code and docs/TUNING.md so no
//   future build mistakes one for the other.
//
//   THE PAPER — the week's edition lists the week's
//   birthdays in TOWN NEWS (Newspaper calls birthdaysLine),
//   names + dates only, derived off the roster + calendar.
//
//   THE PHONE — the one fact-only birthday text beat #55
//   already ran (`Phone` BIRTHDAY, the birthday kid's own
//   thread) now covers the whole roster, because it reads
//   the same registry through TownLife.birthdayOf.
//
//   THE GIFT — a gift handed to the birthday person ON
//   their day lands warmer: +GIFT_BONUS friendship inside
//   Engine.giveGift's existing math, plus the recipient's
//   authored birthday reaction line (REACTIONS, one per
//   cast member, in GameActivity's conversation). Off-day
//   gifts are untouched, pinned both ways. The gift itself
//   is the real item, consumed as gifts are.
//
//   THE PARTY — when the birthday person belongs to one of
//   the seven dinner households (Dinners), a small birthday
//   gathering can stand at that house that evening, riding
//   DINNERS' rails wholesale: the same 17:30–20:30 window,
//   the same agenda seat (Engine calls gatheringBlock
//   immediately after Dinners'), beats on the same DINNER
//   TalkGate rail (a PartyHit payload beside Sleepovers'
//   SleepHit — no new rail), and Dinners' deference
//   discipline: the party YIELDS to an invited dinner
//   at that same table, a hosted dinner, a family dinner, a household
//   collision, any live finale week, any holiday beat, a
//   case climax due, opening night, fair week, carnival
//   week, or a planned sleepover (partyBlock names it; the
//   day still counts through paper/phone/gift, and the
//   yield is stated wherever the player would look — the
//   morning report and the Journal's 🎂 page). Three beats,
//   the invited dinner's shape: 0 the Welcome (the host),
//   1 the Toast (any member; the guest of honor, talked to
//   directly, makes the wish), 2 the Cake — a Look at the
//   house, which serves ONE real snack_cake out of the
//   player's own pack when one is held (the Mall's snack
//   counter sells them; the synergy is intended) and says
//   so honestly when none is: the household improvises,
//   proudly. The wrap lands at rollover on Dinners'
//   discipline (Birthdays.dailyRollover sits beside
//   Dinners' in Engine.onNewDay) and pays by the invited
//   table's attendance scale, the guest of honor a little
//   extra — it is, after all, their day.
//
// The PLAYER's character gets NO birthday from any of
// this — the roster is the cast, and no real-world date
// is wired into the game. State is life/lifeText counters
// only (nothing new to migrate: a pre-0.12.0c save simply
// gains the dates, which are authored data, and meets its
// first birthdays as the calendar turns).
//
//   SHARED DAYS, WHOLE (v0.15.0i, Queue #15 item 6) — the
//   registry holds four shared days (July 1: Cartman and
//   Sheila; May 26: Kyle and Gerald; September 11: Butters
//   and DogPoo; June 20: Tolkien and Michael), and until
//   now the town handled them the way the v0.12.0c header
//   SAID it didn't: the paper's weekly list and the gift
//   bonus were per-person and whole, but everything keyed
//   on TownLife.birthdayOf — the FIRST name in roster
//   order — took that name alone. The phone's birthday
//   text went to the first name only; the morning banner,
//   the location flavor and the free-time buzz named and
//   orbited the first only; the birthday greeting in
//   conversation answered only the first; and the party
//   went to the first household in roster order while the
//   second birthday person's evening was mentioned
//   NOWHERE — no stamp, no sentence, no observance. The
//   second name on a shared day silently yielded, in
//   every system that announced or gathered. That yield
//   is the retired asset of this build, removed whole:
//
//   THE PHONE — every birthday person today gets their
//   own BIRTHDAY text (each is their own sender, so the
//   per-sender rule never eats the second). The
//   shedding order is stated at the harvest itself:
//   story, noted facts and the event beats stand ahead
//   of a birthday; the birthday stands ahead of the
//   group chats and the paper's summary — the two
//   beats that defer whole by their own design, since
//   a birthday is bound to its day and they are not —
//   and ahead of the weather/hello chatter; and the
//   inbox cap sheds oldest read texts, never a
//   birthday for a second name's sake.
//
//   THE PARTY — one evening still holds one party, but
//   the arbitration is explicit now and STAMPED
//   (bdayPlan, written at rollover, printed on the
//   Journal's 🎂 page): everyone at the chosen table is
//   a guest of honor (two members of one household
//   sharing a day get a JOINT party — both names on the
//   welcome, the toast, the cake and the wrap) whenever
//   the evening is free. On the shipped calendar the
//   one such pair — Kyle and Gerald, May 26 — always
//   lands inside the spring season-finale week
//   (May 25–31), the week every evening rail yields
//   to, single birthdays included: their party yields
//   for BOTH names at once, with the reason, each
//   man's standing printed below. The joint rule is
//   the rule for any freer date; the whole-stamped
//   yield is May 26's lived case. And
//   every OTHER household holding a birthday member
//   marks the day at ITS OWN table that evening: its
//   members gather there (the same gathering claim),
//   its venue line says so, the morning report names
//   it, and its night line is written at rollover
//   (bdayObs* — the family marked it themselves, so no
//   friendship moves: the town pays only at tables the
//   player sits at, Dinners' discipline). A birthday
//   with no dinner table (DogPoo, Michael) counts in
//   the paper, on the phone and in a gift — and now the
//   record SAYS so, per person, on the Journal page
//   (standingLine), instead of saying nothing.
//
//   THE PLAYER — when the player's own (day-counted)
//   birthday lands on a cast birthday — on a fresh save
//   it meets Stephen's October 3 in years two through
//   five — both observances stand: the crew's party at
//   your house and the cast party at its table, both
//   morning notes naming the doubling, and the sharing
//   stamped (bdayShare) in the same words the Journal's
//   YOUR BIRTHDAY page prints. New state is stamps only:
//   bdayPlanDay/bdayPlan, bdayObsDay:<household>,
//   bdayObs:<household>, bdayObsWrapDay:<household>,
//   bdayObsNight:<household>, bdayObservances,
//   bdayShareDay/bdayShare — all life/lifeText.
// ============================================================

object Birthdays {

    // ---------------- the registry ----------------

    /** THE table: every cast member's birthday, "MMDD".
     *  Roster order (KID_ORDER) — on a shared day the first
     *  name in this order is the one TownLife.birthdayOf
     *  announces, exactly as #55 behaved for its twelve. */
    val DATES: Map<String, String> = linkedMapOf(
        "Cartman" to "0701", "Stan" to "1019", "Kyle" to "0526", "Kenny" to "0322",
        "Butters" to "0911", "Wendy" to "1207", "Craig" to "0125", "Tweek" to "0817",
        "Clyde" to "0410", "Jimmy" to "0206", "Timmy" to "0308", "Tolkien" to "0620",
        "Bebe" to "1103", "Michael" to "0620", "Pete" to "0928", "Henrietta" to "0710",
        "Randy" to "0107", "Sharon" to "0408", "Gerald" to "0526", "Sheila" to "0701",
        "Liane" to "0630", "Stuart" to "0128", "Carol" to "1204", "Stephen" to "1003",
        "Linda" to "1231", "Garrison" to "0317", "Mackey" to "1114", "Chef" to "1213",
        "Jesus" to "0418", "Jimbo" to "0426", "Father Maxi" to "0225", "Towelie" to "0112",
        "Heidi" to "0829", "Nichole" to "1025", "Red" to "0919", "Kevin" to "0929",
        "Francis" to "1127", "DogPoo" to "0911", "McDaniels" to "1210", "Barbrady" to "0223",
        "Crabtree" to "0719", "Mr. Tweek" to "0705", "Mrs. Tweek" to "0517", "Ned" to "0409",
        "Karen" to "1129", "Scott" to "0101", "Firkle" to "0602", "Annie" to "0625",
        "Bradley" to "1216", "Skeeter" to "0124", "Victoria" to "1224", "Marvin" to "0615",
        "Mr. Tucker" to "0604", "Steve" to "0627", "Ike" to "0725", "Kevin McCormick" to "0126",
        "Shelley" to "0516", "Laura" to "0913", "Mephesto" to "0211", "Yates" to "0731",
        "Adler" to "1030", "Lu Kim" to "0718", "Roger" to "0113", "Ryan" to "1117",
        "Sarah" to "1225", "Alexis" to "0626", "Mr. Testaburger" to "0706", "Nathan" to "0219",
        "Sophie" to "1221", "Big Gay Al" to "0419", "Pip" to "0824", "Dougie" to "0131",
        "Jason" to "0111", "Terrance Mephesto" to "1015", "Jenny" to "1111", "Flora" to "0603",
        "Filmore" to "0203", "Quaid" to "0316", "Mimsy" to "0914", "Tricia" to "0324",
        "Mr. Slave" to "0704", "Linda Black" to "0417", "Harriet" to "1011", "Mr. Biggle" to "1026",
        "Mr. Turner" to "0527", "Nancy" to "0903", "Mr. Stevens" to "0611", "Mrs. Stevens" to "0905",
        "Mr. Stoley" to "0826", "Mrs. Stoley" to "0521", "Clark" to "0808", "Maggie" to "0329",
        "Mitch" to "0720", "Bob Johnson" to "0122", "Pusslicker" to "0318", "Dr. Doctor" to "1118",
        "Choksondik" to "0917", "Strong Woman" to "0810"
    )

    /** The ONE date the lorebook itself states (Clyde
     *  Donovan — "born April 10", his own lorebook entry).
     *  Pinned in the suite against the lorebook file. */
    val LOREBOOK_SOURCED: Set<String> = setOf("Clyde")

    /** The twelve dates #55 shipped with, unchanged. They
     *  are authored too — all but Clyde's — and the class
     *  is named so nobody upgrades them by accident. */
    val PRE_EXISTING: Set<String> = setOf(
        "Stan", "Kyle", "Cartman", "Kenny", "Butters", "Wendy",
        "Craig", "Tweek", "Jimmy", "Tolkien", "Clyde", "Bebe")

    private val MONTH_LABELS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November",
        "December")

    /** "October 19", for cards and pages. */
    fun dateLabel(name: String): String {
        val md = DATES[name] ?: return ""
        val m = md.substring(0, 2).toIntOrNull() ?: return ""
        val d = md.substring(2, 4).toIntOrNull() ?: return ""
        if (m !in 1..12) return ""
        return "${MONTH_LABELS[m - 1]} $d"
    }

    /** "10/19", the paper's shape. */
    fun shortLabel(name: String): String {
        val md = DATES[name] ?: return ""
        val m = md.substring(0, 2).toIntOrNull() ?: return ""
        val d = md.substring(2, 4).toIntOrNull() ?: return ""
        return "$m/$d"
    }

    /** Is it [name]'s birthday on the state's current day? */
    fun isBirthday(state: GameState, name: String): Boolean =
        DATES[name] != null &&
            DATES[name] == state.todayKey.substring(4)

    /** Everyone whose birthday is the state's current day,
     *  in roster order. */
    fun birthdaysToday(state: GameState): List<String> {
        val md = state.todayKey.substring(4)
        return KID_ORDER.filter { DATES[it] == md }
    }

    /** Everyone whose birthday is the given "YYYYMMDD" key,
     *  in roster order (the sweep's and the paper's reader). */
    fun birthdaysOn(key: String): List<String> {
        val md = key.substring(4)
        return KID_ORDER.filter { DATES[it] == md }
    }

    /** The first of today's birthdays in roster order —
     *  #55's announcer, kept identical for its twelve. */
    fun birthdayOf(state: GameState): String? =
        birthdaysToday(state).firstOrNull()

    /** The next [count] birthdays strictly after today,
     *  as (name, daysAway) in the order they'll arrive.
     *  Pure calendar math off the roster — year-agnostic
     *  (a date 364 days out wraps the new year). */
    fun upcoming(state: GameState, count: Int): List<Pair<String, Int>> {
        val todayMd = state.todayKey.substring(4)
        val todayDoy = doyOf(todayMd)
        val rows = ArrayList<Pair<String, Int>>()
        for (name in KID_ORDER) {
            val md = DATES[name] ?: continue
            var away = doyOf(md) - todayDoy
            if (away <= 0) away += 365
            rows += name to away
        }
        return rows.sortedWith(compareBy({ it.second },
            { KID_ORDER.indexOf(it.first) })).take(count)
    }

    private fun doyOf(md: String): Int {
        val days = intArrayOf(31, 28, 31, 30, 31, 30,
            31, 31, 30, 31, 30, 31)
        val m = md.substring(0, 2).toInt()
        val d = md.substring(2, 4).toInt()
        var out = d
        for (i in 0 until m - 1) out += days[i]
        return out
    }

    // ---------------- the gift beat ----------------

    /** THE DIAL: a gift handed to the birthday person on
     *  their day earns this much extra friendship inside
     *  Engine.giveGift's math — flat, stated, capped by
     *  construction (once per gift, only on the day). */
    const val GIFT_BONUS = 3

    /** One authored birthday reaction per cast member —
     *  what they say when a gift lands on their day. Read
     *  by GameActivity's conversation; never by the math. */
    val REACTIONS: Map<String, String> = mapOf(
        "Cartman" to "A birthday present? For ME? Finally, the respect I deserve. …It's not socks, is it?",
        "Stan" to "Oh — wow, thanks. You actually remembered. People keep saying this town's going weird, but… thanks.",
        "Kyle" to "You got me something? On my actual birthday? Okay, that's — thank you. Genuinely.",
        "Kenny" to "(muffled) Mmph mmph mmph! (It's the best birthday ever, apparently.)",
        "Butters" to "Oh boy oh boy oh boy! A present! For me! I ain't even grounded today — this is the best day ever, fellas!",
        "Wendy" to "You remembered my birthday AND brought a gift? Organized AND thoughtful. Thank you.",
        "Craig" to "…You got me a birthday present. Whatever. (He takes it very carefully.)",
        "Tweek" to "GAAAH! You remembered?! That's — that's so much PRESSURE to be happy about! …I AM happy! GAAAH!",
        "Clyde" to "You remembered my birthday? *sniff* I'm not crying. You're crying. …Okay, I'm crying a little.",
        "Jimmy" to "A b-b-birthday gift! Wow, what a great audience! I mean — what a great friend!",
        "Timmy" to "TIMMY! (He means thank you. He means it a lot.)",
        "Tolkien" to "A birthday gift, hand-delivered. Classy. Thank you — and I mean that without a single footnote.",
        "Bebe" to "Oh my GOD, you remembered! That's, like, the most attractive thing a person can do. Thank you!",
        "Michael" to "A gift. For my birthday. …Don't tell the others I said thanks. It'd ruin everything.",
        "Pete" to "Whatever. Birthdays are just conformity with cake. …Thanks, I guess. The cake part's fine.",
        "Henrietta" to "You remembered the day of my arrival into this pointless world. …The gift is acceptable. So are you.",
        "Randy" to "A BIRTHDAY PRESENT?! For me?! Sharon, are you seeing this?! The kid gets it! THE KID GETS IT!",
        "Sharon" to "Oh, honey, you didn't have to. …Thank you. Truly. It's nice to be remembered by someone in this house.",
        "Gerald" to "A birthday gift! Well — I shall accept this on behalf of myself, with full deliberation. Thank you, kid.",
        "Sheila" to "WHAT WHAT WH— a present?! For ME?! Oh, bubbe, you are a GOOD kid. A good, thoughtful kid!",
        "Liane" to "For me? Oh, sweetie. You are just the sweetest thing. Eric, look — someone remembered Mommy's birthday!",
        "Stuart" to "Huh. A present. …Thanks, kid. Don't get many of those that ain't bills.",
        "Carol" to "Oh, baby, you remembered! Come here — no, stay there, you'll get me cryin' into the waffles. Thank you.",
        "Stephen" to "A gift. For my birthday. …This is acceptable. More than acceptable. Thank you. (Butters, take notes.)",
        "Linda" to "Oh my goodness — for me? You sweet thing. Stephen, look! Somebody remembered!",
        "Garrison" to "A present? For ME? Sit down — no, stand up, it's my birthday, I make the rules. …Thank you.",
        "Mackey" to "A birthday gift, m'kay. That's… that's real nice, m'kay. Drugs are bad, but this? This is good, m'kay.",
        "Chef" to "Well hello there, birthday present! Thank you, child. Chef appreciates being remembered, yes he does.",
        "Jesus" to "Hey, thanks for comin' — and for the gift. Being remembered on your birthday never gets old. Trust me.",
        "Jimbo" to "A present?! IT'S COMIN' RIGHT AT ME! …Thanks, kid. Best birthday ambush ever.",
        "Father Maxi" to "A gift? For my birthday? Oh — oh my. The Lord works through the new kid, apparently. Thank you.",
        "Towelie" to "You brought me a birthday present?? You're the best. …You got a towel? No? This is great too, man.",
        "Heidi" to "You remembered! That's so sweet. Thank you — really. This is what a good one's like, huh?",
        "Nichole" to "A birthday gift. Plain and simple and exactly right. Thank you.",
        "Red" to "You remembered my birthday AND showed up with a gift? Bold. Appreciated. Thanks.",
        "Kevin" to "Technically, in the extended canon, a birthday gift is the highest form of tribute. …Thanks. That was a compliment.",
        "Francis" to "For me? Aw, gee. Thanks! I was just gonna have a regular day, but this is way better.",
        "DogPoo" to "…You remembered MY birthday? Nobody remembers anything about me. Thanks. Seriously.",
        "McDaniels" to "A gift, delivered without a press release. Efficient and kind. Thank you — the city thanks you.",
        "Barbrady" to "A present for my birthday?! Well! Everything's under control! Nothing to see here except my gratitude!",
        "Crabtree" to "YOU REMEMBERED MY BIRTHDAY?! SIT DOWN— I mean. …Thanks, kid. That means somethin'.",
        "Mr. Tweek" to "A birthday gift! You know, a present is just coffee for the soul — warm, dark, and— thank you. Truly.",
        "Mrs. Tweek" to "Oh, hon, for me? You come in here on my birthday with a gift and expect me not to tear up? Thank you.",
        "Ned" to "(through the voice box) THANK — YOU. BEST — BIRTH — DAY — EVER.",
        "Karen" to "For my birthday? Really? …Kenny's friends are the best. Thank you!",
        "Scott" to "A birthday present?! For Scott Malkinson?! I have diabetes, but today I also have THIS. Thank you!",
        "Firkle" to "You got the smallest goth a birthday gift. …Conformist. (He's keeping it forever.)",
        "Annie" to "Oh! You remembered! Thank you — that's so nice of you!",
        "Bradley" to "A birthday gift! Mintberry THANKS you! …Bradley does too. Both of us. Thank you.",
        "Skeeter" to "Well now. A birthday present at my own bar. Thanks, kid — we don't need to take this outside.",
        "Victoria" to "A gift, on my birthday, delivered to the principal. Correct behavior. Thank you, dear.",
        "Marvin" to "At my age a birthday gift is mostly a reminder you're still here. …I'm still here. Thank you, kid.",
        "Mr. Tucker" to "A birthday present. Hm. …Craig gets his manners from somewhere, I suppose. Thank you.",
        "Steve" to "A birthday gift, hand-delivered to the Black house. Appreciated — and noted in the family record.",
        "Ike" to "Tank you! Kick da pwesent! (He means he loves it. He really does.)",
        "Kevin McCormick" to "A present? For my birthday? …Kenny said you were solid. He undersold it. Thanks.",
        "Shelley" to "You got ME a birthday present? …Don't tell Stan I smiled. Thanks. Whatever.",
        "Laura" to "A birthday gift — how thoughtful. Craig, take notes. …Thank you, dear.",
        "Mephesto" to "A birthday specimen— I mean, present! For me! Science is grateful. I am grateful. Thank you.",
        "Yates" to "A birthday gift. I suppose next you'll tell me there's no catch. …Thanks, kid. Case closed.",
        "Adler" to "A present? For my birthday? Don't screw it up by making me emotional. …Too late. Thanks.",
        "Lu Kim" to "A birthday gift! Herro, gratitude! Thank you very much — city price, city thanks.",
        "Roger" to "A birthday present? Well, aren't you a good kid. Clyde's lucky to have a friend like you. Thank you.",
        "Ryan" to "For my birthday? Oh, that's — thank you. Jimmy's friends are good people, I keep saying it.",
        "Sarah" to "Oh, how lovely — a birthday gift! Thank you, dear. Jimmy talks about you all the time.",
        "Alexis" to "A birthday gift! Wendy said you were one of the good ones. She undersold it. Thank you!",
        "Mr. Testaburger" to "A present, on my birthday, from one of Wendy's friends. The paperwork checks out. Thank you.",
        "Nathan" to "A birthday gift? For me? …That was a trick question, I obviously deserve it. Thanks.",
        "Sophie" to "You remembered my birthday? I just moved here and you REMEMBERED? …Okay, this town can stay.",
        "Big Gay Al" to "A birthday gift?! Oh, that is just SUPER! Thank you, thank you — I'm feeling super about this!",
        "Pip" to "Oh, I say! A birthday present, for me! How terribly kind! Thank you ever so much!",
        "Dougie" to "For my birthday?! Wowee! Thanks! I'm gonna tell everyone — I'm gonna tell Butters first!",
        "Jason" to "A birthday gift? Huh. Cool. …Thanks. That was almost a whole emotion.",
        "Terrance Mephesto" to "A present! For science's birthday! I mean MY birthday! Thank you — father will hear of this!",
        "Jenny" to "You remembered my birthday AND brought a gift? Okay, that's actually really sweet. Thanks.",
        "Flora" to "A birthday present! For me! Oh boy — thank you! This is the best recess ever and we're not even at recess!",
        "Filmore" to "A present?! Mine?! Quaid, look! …Thanks! We're gonna play with it at recess. You're invited.",
        "Quaid" to "A birthday gift! Filmore, he got me— okay. Thank you. That was cool of you.",
        "Mimsy" to "Duhhh — a present! For Mimsy?! Nathan, look! …Thanks, boss— I mean, thanks, friend!",
        "Tricia" to "A birthday gift?? Craig never remembers anything and YOU remembered? Thank you! I'm telling Mom.",
        "Mr. Slave" to "Oh my — a birthday present! Why, thank you, sweetie. That's just lovely.",
        "Linda Black" to "A birthday gift, delivered properly. Thank you — the Black family appreciates being remembered.",
        "Harriet" to "For my birthday? Well! Henrietta never brings anyone home worth knowing — present company excepted. Thank you.",
        "Mr. Biggle" to "A gift for my birthday. Hm. …The Biggles accept. Thank you, young person.",
        "Mr. Turner" to "A birthday present? Heidi's friend, right? She's got good taste in people. Thank you.",
        "Nancy" to "Oh! A birthday gift — for me? Cartman's friends are full of surprises. Thank you, sweetie.",
        "Mr. Stevens" to "A present. For my birthday. …Butters speaks highly of you. I see why. Thank you.",
        "Mrs. Stevens" to "Oh my — a birthday gift! Butters said you were a good friend. He was right. Thank you!",
        "Mr. Stoley" to "A birthday gift. Logged, appreciated, and — Kevin will explain the lore significance. Thank you.",
        "Mrs. Stoley" to "For me? Oh, that's so kind. Kevin's friends are always welcome here. Thank you!",
        "Clark" to "A birthday present? Huh. Alright. …Thanks. That was decent of you.",
        "Maggie" to "A birthday gift?! For me?! Oh wow — thank you! Today's officially a good day!",
        "Mitch" to "A present, on my birthday. …Yates said you were alright. For once, the man's evidence holds up. Thanks.",
        "Bob Johnson" to "A birthday gift! Well, I'll be. Thank you — folks don't usually remember the Johnsons.",
        "Pusslicker" to "A present?! For MY birthday?! Hot damn — I mean — thank you kindly!",
        "Dr. Doctor" to "A birthday gift. Prognosis: excellent friend. Thank you — and I don't say that clinically.",
        "Choksondik" to "A present? For my birthday? …Class dismissed — I mean, thank you. That was very kind.",
        "Strong Woman" to "A birthday gift. Strong. Thoughtful. Appreciated. Thank you — genuinely."
    )

    /** [name]'s birthday reaction line, or a plain warm
     *  fallback (the table is complete; the fallback is
     *  belt-and-braces for a future cast wave). */
    fun reactionFor(name: String): String =
        REACTIONS[name] ?: "You remembered my birthday? …Thank you. Really."

    // ---------------- the party ----------------

    /** Beats in a party evening, the invited dinner's
     *  count and shape: welcome, toast, cake. */
    const val STEP_COUNT = 3
    val STEP_TITLES = listOf("The Welcome", "The Toast", "The Cake")

    /** Wrap dials — Dinners' attendance scale, the guest
     *  of honor a little extra on top of their seat. */
    const val WRAP_HOST_FULL = 3
    const val WRAP_MEMBER_FULL = 2
    const val WRAP_HONOR_FULL = 2
    const val WRAP_HOST_PART = 2
    const val WRAP_MEMBER_PART = 1
    const val WRAP_HONOR_PART = 1

    /** The cake the cake beat serves, when the player
     *  brought one: the shop counter's own staple. */
    const val CAKE_ITEM = "snack_cake"

    /** One household's party clothing: the welcome at the
     *  door, the toast, the wish (the toast beat, talked to
     *  the guest of honor directly), and the cake beat both
     *  ways — brought, and honestly improvised. {P} in the
     *  raw strings is the guest of honor's name. */
    data class PartyDef(
        val householdId: String,
        val welcome: String,
        val toast: String,
        val wish: String,
        val cakeBrought: String,
        val cakeBare: String
    )

    val PARTY_DEFS: List<PartyDef> = listOf(
        PartyDef("marsh",
            "Randy bursts the door open with the party energy already at a ten. “THERE you are! It's {P}'s birthday and this house is doing it RIGHT. Cake status: pending. Vibes: immaculate. Get in here.”",
            "Randy raises a juice box like it's a Senate hearing. “To {P}! Another year in this town and still standing! That's the whole speech — that's enough — I'm emotional.”",
            "{P} looks at the candle arrangement like it owes them an answer. “Okay. One wish. Not telling. …It's a good one, though.”",
            "You produce the Snacky Cake from the shop counter and the table goes quiet in the good way. “STORE-BOUGHT?” Randy says, reverent. “We're eatin' like KINGS.” It is, by acclamation, the centerpiece.",
            "No cake rides in your pack — and Sharon, who plans for exactly this, produces a candle and sets it in a stack of pancakes with total confidence. “It's a cake if we say it's a cake,” she says. Nobody argues. It counts."),
        PartyDef("broflovski",
            "Sheila opens the door already mid-sentence. “—and I said to Gerald, if the new kid comes to ONE more thing in this house— oh! You're HERE! Come in, come in, it's {P}'s birthday and we are doing it PROPERLY.”",
            "Sheila taps a glass until the room behaves. “To {P}! WHAT— WHAT— what a year. What a family. Everybody eat something, you're all too thin.”",
            "{P} stares down the candles. “Statistically,” {P} says, “wishes are unproven. …I'm making one anyway. For the data.”",
            "“A cake! Brought! By a guest!” Sheila announces it to the room like a verdict. The Snacky Cake takes the center of the table and {P} gets the first slice, as is right and proper.",
            "Your pack holds no cake, and Gerald — a man prepared — produces a candle and sets it into the noodle kugel without breaking stride. “In this house,” he says, “we improvise with dignity.” It works. It completely works."),
        PartyDef("cartman",
            "Liane opens the door with a party hat already on. “Oh, sweetie, you came! It's a birthday in this house and we are doing cake about it. Come in, come in — there's a hat with your name on it. Not literally. Eric labeled them all 'Eric'.”",
            "Liane lifts her glass. “To {P}! The light of this house — and to whoever raised them, who is also doing great.” Eric demands a second toast about himself. He does not get one.",
            "{P} regards the candles like a contract. “I know exactly what I'm wishing for,” {P} says. “And no, you can't have it. It's mine. It's always mine.”",
            "A Snacky Cake, delivered by a guest, placed at the center of the Cartman table. “RESPECT,” Eric declares. “My AUTHORITAH has been acknowledged.” The birthday person gets the first slice. House rules.",
            "No cake in your pack. Liane does not miss a beat: a candle goes into a small mountain of Cheesy Poofs in a party bowl. “Birthday!” she announces. It's structurally a cake. Legally a cake. Gone in four minutes."),
        PartyDef("mccormick",
            "Carol opens the door one-handed, party in the other. “Well hey there, baby. You came for {P}'s birthday — get on in here. We don't do fancy, we do fed. There's a difference and ours is better.”",
            "Carol raises a mug. “To {P}! Another year of the McCormicks, and we're still the toughest thing on this block. Somebody write that down.” Kenny writes it down.",
            "{P} leans over the candles and wishes in total, professional silence. “Out loud is how they get you,” {P} says. “It's in. It's done.”",
            "You set the Snacky Cake down and the McCormick table treats it like a visiting dignitary. “Store-bought!” Carol says, delighted. “Well, aren't we fancy tonight.” {P} gets the first slice.",
            "No cake in your pack, and Stuart solves it the McCormick way: a candle in the waffle stack, no apology attached. “It's got layers,” he says. “It's got a candle. It's a cake.” He's right. It's a cake."),
        PartyDef("stotch",
            "Linda opens the door beaming, with Stephen right behind her holding streamers like evidence. “Oh my goodness, you're here! It's {P}'s birthday and we are SO pleased. Shoes off, spirits up.”",
            "Stephen raises his glass with great ceremony. “To {P}. Another year in this house, and the house is proud. That is the official statement.” Linda cries a little. Everyone pretends not to notice, kindly.",
            "{P} faces the candles. “Okay,” {P} says. “One wish. No take-backs, no grounding clauses, just the wish.” A breath. “Done. It's a good one.”",
            "A real Snacky Cake, carried in by a guest, set in the center of the Stotch table. “Well!” Stephen says. “Guest-brought. Centerpiece-grade.” Linda gives {P} the first slice and a look that says this will be remembered fondly at review time.",
            "Your pack is cakeless, and Linda — who has clearly rehearsed this — sets a candle into a neat stack of sandwich cookies. “A cake is a promise,” she says, “and this is a promise.” Stephen nods like it's in the handbook. It might be."),
        PartyDef("black",
            "Tolkien opens the door in a paper crown worn with total commitment. “Welcome. It's {P}'s birthday, the house is hosting, and the hosting is being done CORRECTLY. Come in — you're on the list. There's a list.”",
            "Tolkien raises a glass at exactly the right moment. “To {P}. The Black family keeps two sets of books: the real ones, and the birthday ones. This entry is all credit.” Steve laughs into his drink.",
            "{P} looks over the candles and the good tableware. “A wish,” {P} says, “is just a plan with better PR. …Mine's already in motion.”",
            "The Snacky Cake arrives like it was catered. Tolkien inspects it, nods once, and gives it the good plate. “Guest-brought, shop-fresh, centerpiece.” {P} takes the first slice to applause that is frankly well-organized.",
            "No cake in the pack — and Linda Black produces a candle and sets it into a truly architectural fruit plate without a flicker of doubt. “We adapt,” she says. “We do not announce that we adapted.” Noted. It counts."),
        PartyDef("tweek",
            "Mr. Tweek opens the door with ground-level calm stretched over visible, vibrating effort. “Ah — a guest! For {P}'s birthday! We've prepared — EVERYTHING is prepared. The coffee is— I mean, the party is— come IN.”",
            "Mr. Tweek lifts a mug with both hands. “To {P}! May your year be smooth, your grounds fresh, and your— this is a TOAST, Richard, keep it together— HAPPY BIRTHDAY!”",
            "{P} stares at the candles like they're a deadline. “One wish,” {P} says. “No pressure. Except all of it.” A breath. “…Done. It was a good one.”",
            "The Snacky Cake lands on the Tweek table and the whole room's shoulders drop an inch. “SHOP cake,” Mrs. Tweek says, like a weather report clearing. “Nobody had to BAKE under these conditions.” {P} gets the first slice.",
            "No cake in your pack. Mrs. Tweek sets a candle into a coffee cake the size of a hubcap. “It was ALWAYS going to be a coffee cake,” she says. “The candle just makes it official.” It is, officially, a great cake.")
    )

    private val DEFS_BY_ID: Map<String, PartyDef> =
        PARTY_DEFS.associateBy { it.householdId }

    fun defFor(householdId: String): PartyDef? =
        DEFS_BY_ID[householdId]

    /** "Kyle", "Kyle and Gerald", "A, B and C" — the
     *  town's list voice for names sharing one breath. */
    fun joinNames(names: List<String>): String = when {
        names.isEmpty() -> ""
        names.size == 1 -> names[0]
        names.size == 2 -> "${names[0]} and ${names[1]}"
        else -> names.dropLast(1).joinToString(", ") +
            " and " + names.last()
    }

    /** A party that can stand tonight, resolved: whose
     *  (EVERY guest of honor — v0.15.0i: a day two members
     *  of one household share makes a JOINT party), and at
     *  which household's table. */
    data class Party(val def: PartyDef, val honorees: List<String>) {
        /** The first guest of honor, roster order — the
         *  name single-birthday code has always read. */
        val person: String get() = honorees.first()
        val household: HouseholdDef
            get() = Dinners.HOUSEHOLDS.first { it.id == def.householdId }
    }

    private fun kSteps(hid: String) = "bdaySteps:$hid"
    private fun kStepDay(hid: String) = "bdayStepDay:$hid"
    private fun kPerson(hid: String) = "bdayPerson:$hid"
    private fun kCake(hid: String) = "bdayCake:$hid"
    private fun kNight(hid: String) = "bdayNight:$hid"
    private fun kWrapDay(hid: String) = "bdayWrapDay:$hid"

    /** Why [def]'s house holds no birthday party tonight,
     *  or null when the evening is free for one. Dinners'
     *  deference discipline, read whole: anything that
     *  owns an evening, a week, or this table outranks a
     *  birthday — and says so, in words the morning report
     *  and the Journal page can repeat verbatim. */
    fun partyBlock(state: GameState, def: PartyDef): String? {
        val house = Dinners.HOUSEHOLDS.first { it.id == def.householdId }
        if (Finales.windowAt(state) != null)
            return "a season finale owns this week — parties wait"
        if (HolidayBeats.activeBeat(state) != null)
            return "a holiday beat owns tonight"
        if (MovieNights.anyClimaxDueTonight(state))
            return "a case comes to a head tonight"
        if (Dinners.beatsDeferred(state))
            return "a climax is on the table tonight"
        if (Play.nightCanHappen(state))
            return "it's opening night at the Elementary"
        if (Fair.windowToday(state) != null)
            return "the county fair owns this week"
        if (Carnival.windowToday(state) != null)
            return "the winter carnival owns this week"
        if (Dinners.collides(state, house))
            return "the ${house.family} table is spoken for tonight"
        // an invitation to THIS table yields the party —
        // the family is already gathering here tonight for
        // the regular dinner, and one table holds one
        // evening. An invitation ELSEWHERE does not: the
        // rotation stands somewhere every Friday (the
        // hosted table's precedent — an outstanding
        // invitation never barred hosting your own night),
        // and two invitations are the player's to choose
        // between; both are named (Dinners' morning note
        // announces the invite in the same report, and the
        // 🎂 page names it beside the party).
        Dinners.todaysInvite(state)?.let { inv ->
            if (inv.id == def.householdId)
                return "the dinner rotation has this very " +
                    "table tonight — the regular dinner " +
                    "claimed it first"
        }
        if (Dinners.hostingTonight(state) != null)
            return "you're hosting dinner at your own table tonight"
        if (Family.dinnerTonight(state))
            return "it's family dinner night at your house"
        if (Sleepovers.plannedTonight(state))
            return "there's a sleepover planned at your house tonight"
        return null
    }

    /** Tonight's party, if one stands: the first dinner
     *  household (by its earliest birthday member's place
     *  in roster order) whose evening is free — and EVERY
     *  birthday person at that table is a guest of
     *  honor, so a day two members of one household share
     *  resolves as a joint party by construction. Pure. */
    fun partyTonight(state: GameState): Party? {
        val today = birthdaysToday(state)
        for (p in today) {
            val hh = Dinners.householdOf(p) ?: continue
            val def = defFor(hh.id) ?: continue
            if (partyBlock(state, def) == null)
                return Party(def, today.filter {
                    Dinners.householdOf(it)?.id == hh.id })
        }
        return null
    }

    /** Why the first household-eligible birthday person
     *  today gets no party, when none stands — or null
     *  when a party stands or no household member has a
     *  birthday at all. The Journal's and the morning
     *  report's honest sentence. */
    fun partyYield(state: GameState): Pair<String, String>? {
        if (partyTonight(state) != null) return null
        for (p in birthdaysToday(state)) {
            val hh = Dinners.householdOf(p) ?: continue
            val def = defFor(hh.id) ?: continue
            val why = partyBlock(state, def) ?: continue
            return p to why
        }
        return null
    }

    // ---------------- shared days: the arbitration ----------------

    /** A household marking its birthday member's day at
     *  its OWN table tonight: considered by the gathering
     *  rail, named in the morning report and the Journal,
     *  and written into the night's record at rollover —
     *  the honest observance for the birthday the one
     *  party slot didn't take. */
    data class Observance(val def: PartyDef,
                          val honorees: List<String>) {
        val household: HouseholdDef
            get() = Dinners.HOUSEHOLDS.first { it.id == def.householdId }
    }

    /** Tonight's family-table observances: every dinner
     *  household besides the party's own that holds a
     *  birthday member today and whose evening is free.
     *  Only a shared day grows them. Pure. */
    fun observancesTonight(state: GameState): List<Observance> {
        val today = birthdaysToday(state)
        if (today.size < 2) return emptyList()
        val partyHid = partyTonight(state)?.def?.householdId
        val out = ArrayList<Observance>()
        val seen = HashSet<String>()
        for (p in today) {
            val hh = Dinners.householdOf(p) ?: continue
            if (hh.id == partyHid || hh.id in seen) continue
            val def = defFor(hh.id) ?: continue
            if (partyBlock(state, def) != null) continue
            seen += hh.id
            out += Observance(def, today.filter {
                Dinners.householdOf(it)?.id == hh.id })
        }
        return out
    }

    /** The short arbitration clause for a shared day:
     *  why the evening fell out the way it did. */
    fun arbitrationShort(state: GameState): String {
        val party = partyTonight(state)
        return when {
            party != null && party.honorees.size > 1 ->
                "one table, two birthdays — the party is joint"
            party != null ->
                "two birthdays share the day and one evening " +
                    "holds one party: it stands with " +
                    "${party.person}, first on the day's record"
            else -> "the day is shared; the evening went " +
                "to the town"
        }
    }

    /** Where [person]'s birthday evening stands, in one
     *  sentence the Journal can print: the party (joint
     *  honors named), their own table's observance, the
     *  yield with its reason, or the plain truth for a
     *  birthday with no dinner-table house. Pure. */
    fun standingLine(state: GameState, person: String): String {
        val party = partyTonight(state)
        if (party != null && person in party.honorees) {
            return if (party.honorees.size > 1)
                "a joint party at ${party.household.family}'s " +
                    "tonight — ${joinNames(party.honorees)} share " +
                    "the guest-of-honor seat"
            else
                "the party at ${party.household.family}'s tonight"
        }
        observancesTonight(state).firstOrNull {
            person in it.honorees }?.let {
            return "their own table at ${it.household.family}'s " +
                "marks it tonight — ${arbitrationShort(state)}"
        }
        val hh = Dinners.householdOf(person)
        val def = hh?.let { defFor(it.id) }
        if (def != null) {
            val why = partyBlock(state, def)
            if (why != null) return "no party tonight: $why"
        }
        return "no dinner-table house — the day counts in the " +
            "paper, on the phone, and in a gift given today"
    }

    /** The day's arbitration, written whole: who shares
     *  today, where the party stands, whose table marks
     *  its own, whose yields and why, and whose day counts
     *  without a table — a clause for EVERY birthday
     *  person, nobody dropped. The sentence the rollover
     *  stamps (bdayPlan) and the Journal prints. Null
     *  when today is not a shared day. Pure. */
    fun arbitrationLine(state: GameState): String? {
        val today = birthdaysToday(state)
        if (today.size < 2) return null
        val party = partyTonight(state)
        val obs = observancesTonight(state)
        val bits = ArrayList<String>()
        bits += "${joinNames(today)} share today"
        when {
            party != null && party.honorees.size > 1 ->
                bits += "the party at " +
                    "${party.household.family}'s is JOINT — " +
                    "${joinNames(party.honorees)} are both " +
                    "guests of honor at their own table"
            party != null ->
                bits += "the party stands at " +
                    "${party.household.family}'s for " +
                    "${joinNames(party.honorees)}, first on " +
                    "the day's record"
            else -> partyYield(state)?.let { (p, why) ->
                bits += "no party stands — $why (the first " +
                    "name on the record was $p)"
            } ?: bits.add("no party stands tonight")
        }
        for (o in obs) {
            bits += "${o.household.family} mark " +
                "${joinNames(o.honorees)}'s at their own table"
        }
        for (p in today) {
            val hh = Dinners.householdOf(p) ?: continue
            if (party != null && p in party.honorees) continue
            if (obs.any { p in it.honorees }) continue
            val def = defFor(hh.id) ?: continue
            val why = partyBlock(state, def) ?: continue
            bits += "${hh.family} table yields for $p: $why"
        }
        for (p in today) {
            if (Dinners.householdOf(p) == null)
                bits += "$p has no dinner-table house — the " +
                    "day counts in the paper, on the phone, " +
                    "and in a gift"
        }
        return bits.joinToString("; ") + "."
    }

    /** The sentence for a day that is BOTH the player's
     *  birthday and a cast birthday day: both observances
     *  stand, and the record says how the evening is
     *  shared. Null when the day isn't shared with the
     *  player. Pure. */
    fun playerShareLine(state: GameState): String? {
        if (!PlayerBirthday.isBirthday(state)) return null
        val today = birthdaysToday(state)
        if (today.isEmpty()) return null
        val party = partyTonight(state)
        val names = joinNames(today)
        return when {
            party != null && PlayerBirthday.partyStands(state) ->
                "Your birthday and $names's fell on the same " +
                    "day. Both tables stand tonight — your " +
                    "crew at your house, " +
                    "${party.household.family} at theirs for " +
                    "${joinNames(party.honorees)} — and the " +
                    "evening is yours to split; a guest wanted " +
                    "at both tables goes where the night takes " +
                    "them, and both doors stay open."
            party != null ->
                "Your birthday and $names's fell on the same " +
                    "day. Their table stands tonight — a " +
                    "party at ${party.household.family}'s " +
                    "for ${joinNames(party.honorees)} — while " +
                    "your own party yields: " +
                    PlayerBirthday.yieldReason(state)
                        .ifEmpty { "your crew is empty — a " +
                            "party needs a crew" } +
                    ". The day counts in full for you both: " +
                    "yours at home, theirs at their table."
            else -> partyYield(state)?.let { (_, why) ->
                "Your birthday and $names's fell on the same " +
                    "day. Their table's party yields tonight — " +
                    "$why — so the evening is yours, and their " +
                    "day still counts in the paper, on the " +
                    "phone, and in a gift."
            } ?: "Your birthday and $names's fell on the " +
                "same day. No dinner table holds theirs — " +
                "the day counts for them in the paper and " +
                "on the phone, and for you at home."
        }
    }

    // ---------------- the stamps ----------------

    private fun kObsDay(hid: String) = "bdayObsDay:$hid"
    private fun kObs(hid: String) = "bdayObs:$hid"
    private fun kObsNight(hid: String) = "bdayObsNight:$hid"
    private fun kObsWrapDay(hid: String) = "bdayObsWrapDay:$hid"

    /** Today's stamped arbitration, when the stamp is
     *  today's — the record the rollover wrote this
     *  morning (Dance's discipline: derived on the
     *  morning, kept verbatim). */
    fun planLine(state: GameState): String? =
        if ((state.life["bdayPlanDay"] ?: -1) == state.absDay)
            state.lifeText["bdayPlan"]?.takeIf { it.isNotEmpty() }
        else null

    /** The morning-after line for the household whose
     *  observance wrapped, the party night-line's
     *  discipline: kept per household, shown the one
     *  morning after. */
    fun obsNightLine(state: GameState, hid: String): String? =
        if ((state.life[kObsWrapDay(hid)] ?: -1) == state.absDay)
            state.lifeText[kObsNight(hid)] else null

    /** The stamped player-sharing record for today, when
     *  the rollover wrote one; else the live derivation. */
    fun shareLine(state: GameState): String? =
        if ((state.life["bdayShareDay"] ?: -1) == state.absDay)
            state.lifeText["bdayShare"]?.takeIf { it.isNotEmpty() }
                ?: playerShareLine(state)
        else playerShareLine(state)

    // ---------------- steps ----------------

    private fun stepsMask(state: GameState, hid: String): Int =
        if ((state.life[kStepDay(hid)] ?: -1) == state.absDay)
            state.life[kSteps(hid)] ?: 0 else 0

    fun stepsDone(state: GameState, hid: String): Int =
        Integer.bitCount(stepsMask(state, hid))

    private fun currentStep(state: GameState, hid: String): Int {
        val mask = stepsMask(state, hid)
        var s = 0
        while (s < STEP_COUNT && (mask and (1 shl s)) != 0) s++
        return s
    }

    private fun fire(state: GameState, party: Party, step: Int) {
        val hid = party.def.householdId
        state.life[kStepDay(hid)] = state.absDay
        state.life[kSteps(hid)] =
            stepsMask(state, hid) or (1 shl step)
        state.lifeText[kPerson(hid)] =
            party.honorees.joinToString(",")
        if (step == 2) {
            // the cake beat: ONE real cake out of the
            // player's pack when one is held — exactly one,
            // and the record says which way it went.
            val held = state.inventory[CAKE_ITEM] ?: 0
            if (held > 0) {
                if (held == 1) state.inventory.remove(CAKE_ITEM)
                else state.inventory[CAKE_ITEM] = held - 1
                state.life[kCake(hid)] = 1
            } else {
                state.life[kCake(hid)] = 0
            }
        }
    }

    /** TalkGate's payload for a party beat (steps 0–1;
     *  the cake is a Look and never answers here). */
    data class PartyHit(
        val stepIndex: Int,
        val text: String,
        val allLanded: Boolean,
        val family: String,
        val person: String,
        val householdId: String
    )

    private fun inWindow(state: GameState): Boolean =
        state.timeMin in Dinners.DINNER_FROM..Dinners.DINNER_TO

    /** A Talk with [npcName] right now would land party
     *  beat [PartyHit.stepIndex], or null. Pure. The
     *  welcome is the host's to give (Dinners' rule); the
     *  toast is any member's — and talked to the guest of
     *  honor directly, it becomes the wish. */
    fun talkStepFor(state: GameState, npcName: String): PartyHit? {
        val party = partyTonight(state) ?: return null
        if (!inWindow(state)) return null
        val house = party.household
        if (npcName !in house.members) return null
        if (Dinners.beatsDeferred(state)) return null
        val a = state.agents[npcName] ?: return null
        if (a.asleep || a.location != house.house) return null
        val step = currentStep(state, party.def.householdId)
        if (step >= 2) return null
        if (step == 0 && npcName != house.host) return null
        val text = when (step) {
            0 -> party.def.welcome.replace("{P}",
                joinNames(party.honorees))
            else -> if (npcName in party.honorees)
                party.def.wish.replace("{P}", npcName)
            else party.def.toast.replace("{P}",
                joinNames(party.honorees))
        }
        return PartyHit(step, text,
            stepsDone(state, party.def.householdId) + 1 >= STEP_COUNT,
            house.family, party.person, party.def.householdId)
    }

    /** Judge a Talk party beat — the TalkGate caller in
     *  GameActivity shows its text like a dinner beat. */
    fun judgeTalk(state: GameState, npcName: String): PartyHit? {
        val hit = talkStepFor(state, npcName) ?: return null
        val party = partyTonight(state) ?: return null
        fire(state, party, hit.stepIndex)
        return hit.copy(allLanded =
            stepsDone(state, party.def.householdId) >= STEP_COUNT)
    }

    /** The cake beat hides in the Look at the host house
     *  (the Dinners pattern): while the evening waits on
     *  step 2, a careful Look at the party house IS the
     *  cake. Fires the beat — and the cake, when one's
     *  held — exactly like Dinners' table moment. */
    fun lookText(state: GameState, locId: String): String? {
        val party = partyTonight(state) ?: return null
        if (locId != party.household.house) return null
        if (!inWindow(state)) return null
        if (currentStep(state, party.def.householdId) != 2)
            return null
        if (Dinners.beatsDeferred(state)) return null
        val anyHere = party.household.members.any { m ->
            val a = state.agents[m]
            a != null && !a.asleep && a.location == locId
        }
        if (!anyHere) return null
        val brought = (state.inventory[CAKE_ITEM] ?: 0) > 0
        val text = (if (brought) party.def.cakeBrought
            else party.def.cakeBare).replace("{P}",
                joinNames(party.honorees))
        fire(state, party, 2)
        return text
    }

    /** What a Look at the host house says while a party
     *  stands but its cake beat isn't up — the venue line,
     *  so the house reads as dressed for the night. Used
     *  by GameActivity only when the beat look is null;
     *  never fires anything. (v0.15.0i: an observance
     *  house reads as marking its own, the same way.) */
    fun venueText(state: GameState, locId: String): String? {
        val party = partyTonight(state)
        if (party != null && locId == party.household.house) {
            if (!inWindow(state)) return null
            return "🎂 ${party.household.family} place is dressed " +
                "for a birthday — it's " +
                "${joinNames(party.honorees)}'s day, and " +
                "the household is doing it properly."
        }
        val obs = observancesTonight(state)
            .firstOrNull { it.household.house == locId }
            ?: return null
        if (!inWindow(state)) return null
        return "🎂 ${obs.household.family} place is marking " +
            "${joinNames(obs.honorees)}'s birthday at its own " +
            "table tonight — the day is shared, and this " +
            "house does its own celebrating."
    }

    // ---------------- the claim ----------------

    /** The Engine's gathering hook, seated immediately
     *  after Dinners' own: while a party stands, its
     *  household is at its own table — and every claim
     *  seated later in the stack (jobs, screenings,
     *  finales, the fair) still wins them, exactly as a
     *  dinner's guests are won. (v0.15.0i: the same hook
     *  also gathers an observance household at ITS table —
     *  one claim, two honest uses; the stack's count and
     *  order are untouched.) */
    fun gatheringBlock(state: GameState, npcName: String,
                       timeMin: Int): AgendaBlock? {
        if (timeMin !in Dinners.DINNER_FROM..Dinners.DINNER_TO)
            return null
        val party = partyTonight(state)
        if (party != null && npcName in party.household.members) {
            return AgendaBlock(party.household.house,
                "Birthday party at ${party.household.family}'s — " +
                    if (party.honorees.size > 1)
                        "${joinNames(party.honorees)} are the " +
                            "guests of honor."
                    else "${party.person} is the guest of honor.")
        }
        val obs = observancesTonight(state).firstOrNull {
            npcName in it.household.members } ?: return null
        return AgendaBlock(obs.household.house,
            "Marking ${joinNames(obs.honorees)}'s birthday at " +
                "${obs.household.family}'s own table — " +
                "the family holds the day themselves tonight.")
    }

    // ---------------- the wrap ----------------

    /** The morning-after line for the household whose
     *  party wrapped, Dinners' night-line discipline:
     *  kept per household, shown the one morning after. */
    fun nightLine(state: GameState, hid: String): String? =
        if ((state.life[kWrapDay(hid)] ?: -1) == state.absDay)
            state.lifeText[kNight(hid)] else null

    /** Rollover (Engine.onNewDay, beside Dinners'): a
     *  party whose beats landed yesterday pays its table
     *  and writes its night line. A party nobody reached
     *  leaves no trace at all — like an invitation never
     *  taken up, the evening simply went elsewhere. */
    fun dailyRollover(state: GameState) {
        for (def in PARTY_DEFS) {
            val hid = def.householdId
            if ((state.life[kStepDay(hid)] ?: -2) != state.absDay - 1)
                continue
            val mask = state.life[kSteps(hid)] ?: 0
            val beats = Integer.bitCount(mask)
            if (beats == 0) continue
            val person = state.lifeText[kPerson(hid)] ?: continue
            val honorees = person.split(",").map { it.trim() }
                .filter { it.isNotEmpty() }
            val house = Dinners.HOUSEHOLDS.first { it.id == hid }
            val full = beats >= STEP_COUNT
            val hostGain = if (full) WRAP_HOST_FULL else WRAP_HOST_PART
            val memberGain = if (full) WRAP_MEMBER_FULL else WRAP_MEMBER_PART
            val honorGain = if (full) WRAP_HONOR_FULL else WRAP_HONOR_PART
            for (m in house.members) {
                var gain = if (m == house.host) hostGain else memberGain
                if (m in honorees) gain += honorGain
                state.friendship[m] =
                    minOf(100, (state.friendship[m] ?: 0) + gain)
            }
            val cakeBit = when {
                beats < STEP_COUNT -> "You caught part of it"
                (state.life[kCake(hid)] ?: 0) == 1 ->
                    "The cake you brought was the centerpiece"
                else -> "No cake was brought; the household " +
                    "improvised, and it counted anyway"
            }
            state.lifeText[kNight(hid)] =
                "🎂 ${joinNames(honorees)}'s birthday party at " +
                    "${house.family}'s — $cakeBit. " +
                    if (full) "All three beats of the evening, in."
                    else "The evening counted."
            state.life[kWrapDay(hid)] = state.absDay
            state.life["bdayParties"] =
                (state.life["bdayParties"] ?: 0) + 1
            state.lifeText["bdayLast"] =
                "${joinNames(honorees)} at the ${house.family}'s"
        }
        // ---- v0.15.0i: yesterday's observances wrap ----
        // The family marked the day themselves: the
        // record is written, and NO friendship moves —
        // the town pays only at tables the player sits
        // at (Dinners' discipline, kept whole).
        for (def in PARTY_DEFS) {
            val hid = def.householdId
            if ((state.life[kObsDay(hid)] ?: -2) != state.absDay - 1)
                continue
            val honorees = (state.lifeText[kObs(hid)] ?: "")
                .split(",").map { it.trim() }
                .filter { it.isNotEmpty() }
            if (honorees.isEmpty()) continue
            val house = Dinners.HOUSEHOLDS.first { it.id == hid }
            state.lifeText[kObsNight(hid)] =
                "🎂 ${joinNames(honorees)}'s birthday was " +
                    "marked at ${house.family}'s own table " +
                    "last night — family-style: their table, " +
                    "their cake, their day. It counted."
            state.life[kObsWrapDay(hid)] = state.absDay
            state.life["bdayObservances"] =
                (state.life["bdayObservances"] ?: 0) + 1
        }
        // ---- v0.15.0i: today's arbitration, stamped ----
        // Derived while the morning still knows the day
        // (Dance's guarded pattern, PlayerBirthday's
        // yield stamp): the record is written once, and
        // the Journal and the morning report repeat it.
        if (birthdaysToday(state).size >= 2) {
            state.life["bdayPlanDay"] = state.absDay
            state.lifeText["bdayPlan"] =
                arbitrationLine(state) ?: ""
            for (o in observancesTonight(state)) {
                val hid = o.def.householdId
                state.life[kObsDay(hid)] = state.absDay
                state.lifeText[kObs(hid)] =
                    o.honorees.joinToString(",")
            }
        }
        playerShareLine(state)?.let { line ->
            state.life["bdayShareDay"] = state.absDay
            state.lifeText["bdayShare"] = line
        }
    }

    /** The House morning report's line: last night's
     *  party result when it's fresh; otherwise tonight's
     *  party announced while there's a day to plan around
     *  (bring a cake), or the yield stated plainly. */
    fun morningNote(state: GameState): String? {
        for (def in PARTY_DEFS) {
            nightLine(state, def.householdId)?.let { return it }
        }
        // v0.15.0i: last night's family-table observance
        // stands in the report like a party result does.
        for (def in PARTY_DEFS) {
            obsNightLine(state, def.householdId)?.let { return it }
        }
        partyTonight(state)?.let { p ->
            val obs = observancesTonight(state)
            val obsBit = if (obs.isNotEmpty())
                " It's also " +
                    "${joinNames(obs.flatMap { it.honorees })}'s " +
                    "birthday — ${arbitrationShort(state)}; " +
                    "${obs.first().household.family} mark " +
                    "it at their own table tonight."
            else ""
            // v0.15.0i: a player birthday sharing the day
            // is named here too — both observances stand.
            val youBit = if (PlayerBirthday.isBirthday(state))
                " And it's your birthday too — your crew " +
                    "has your own table tonight; the evening " +
                    "is yours to split."
            else ""
            return "🎂 Birthday party tonight: it's " +
                "${joinNames(p.honorees)}'s birthday, and the " +
                "${p.household.family} are having a small party " +
                "at their place (5:30–8:30). You're expected — " +
                "and a Snacky Cake in your pack would make it." +
                obsBit + youBit
        }
        partyYield(state)?.let { (person, why) ->
            return "🎂 It's $person's birthday today — no " +
                "party tonight: $why. The day still counts: " +
                "the paper, the texts, and a gift today all " +
                "land extra."
        }
        return null
    }

    // ---------------- the paper ----------------

    /** The week's birthdays for the paper: names + dates
     *  only, derived off the roster and the week's seven
     *  calendar keys. Null when the week holds none. */
    fun birthdaysLine(state: GameState, week: Int): String? {
        val names = ArrayList<String>()
        for (d in 0..6) {
            val key = Commissions.keyForAbsDay(state, week * 7 + d)
            for (name in birthdaysOn(key)) {
                names += "$name (${shortLabel(name)})"
            }
        }
        if (names.isEmpty()) return null
        return "🎂 Birthdays on the record this week: " +
            names.joinToString(", ") + "."
    }

    /** Shared days in the paper's week, named AS shared:
     *  a line per shared date the week holds, every name
     *  on it — the weekly list names both either way;
     *  this says the day itself is doubled. Null when
     *  the week holds no shared day. */
    fun sharedDayLine(state: GameState, week: Int): String? {
        val lines = ArrayList<String>()
        for (d in 0..6) {
            val key = Commissions.keyForAbsDay(state, week * 7 + d)
            val names = birthdaysOn(key)
            if (names.size >= 2) {
                lines += "🎂 One day, ${names.size} birthdays: " +
                    "${joinNames(names)} share " +
                    "${dateLabel(names.first())} — the town " +
                    "does double cake duty."
            }
        }
        return if (lines.isEmpty()) null
            else lines.joinToString(" ")
    }

    // ---------------- the Journal page ----------------

    /** The 🎂 BIRTHDAYS page: today (and tonight, stated
     *  plainly either way), what's coming, and the running
     *  record of parties. */
    fun journalLines(state: GameState): List<String> {
        val out = ArrayList<String>()
        val today = birthdaysToday(state)
        out += if (today.isEmpty())
            "· No birthdays today. The calendar breathes."
        else
            "🎂 Today: " + today.joinToString(", ") +
                " — wish them well; a gift today lands extra."
        val party = partyTonight(state)
        val yield = if (party == null) partyYield(state) else null
        when {
            party != null -> out +=
                "🎉 Tonight: a small party for " +
                    "${joinNames(party.honorees)} " +
                    "at the ${party.household.family}'s " +
                    "(${LOCATIONS[party.household.house]?.name ?: party.household.house}, 5:30–8:30). " +
                    "You're expected — and a Snacky Cake from " +
                    "the Mall's snack counter would make it." +
                    (Dinners.todaysInvite(state)
                        ?.takeIf { it.id != party.def.householdId }
                        ?.let { " The dinner rotation also has a " +
                            "table at the ${it.family}'s " +
                            "tonight — the evening is yours to " +
                            "choose." } ?: "")
            yield != null -> out +=
                "🎉 No party for ${yield.first} tonight: " +
                    "${yield.second}. The day still counts — " +
                    "the paper has it, their phone will buzz, " +
                    "and a gift today lands extra."
            today.isNotEmpty() -> out +=
                "· No party tonight — today's birthdays " +
                    "aren't at a dinner table's house."
            else -> out += "· No party tonight."
        }
        // v0.15.0i: a shared day is arbitrated in the
        // open — every birthday person's standing, named,
        // and the day's arbitration on the record.
        if (today.size >= 2) {
            for (p in today) {
                out += "· $p: ${standingLine(state, p)}."
            }
            val plan = planLine(state) ?: arbitrationLine(state)
            if (plan != null)
                out += "📋 On the record: $plan"
        }
        if (today.isNotEmpty() && PlayerBirthday.isBirthday(state)) {
            out += "🎂 And it's YOUR birthday today too — " +
                "both observances stand. Your own page " +
                "keeps that record."
        }
        out += "· Coming up: " + upcoming(state, 5).joinToString(", ") {
            "${it.first} (${shortLabel(it.first)})"
        } + "."
        val parties = state.life["bdayParties"] ?: 0
        out += if (parties > 0)
            "🎉 Birthday parties you've been part of: " +
                "$parties — the latest: " +
                (state.lifeText["bdayLast"] ?: "") + "."
        else
            "· No birthday parties yet. They stand on their " +
                "evenings; show up and be counted."
        val observances = state.life["bdayObservances"] ?: 0
        if (observances > 0)
            out += "🏠 Family-table observances marked on " +
                "shared days: $observances — a household " +
                "doing its own celebrating counts too."
        out += "The town keeps these dates the way towns " +
            "do. One of them is straight off the record — " +
            "Clyde Donovan, April 10, per his own profile. " +
            "The rest are simply when everybody's always " +
            "had them."
        return out
    }
}

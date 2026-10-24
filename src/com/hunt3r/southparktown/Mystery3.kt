package com.hunt3r.southparktown

import kotlin.random.Random

// ============================================================
// Mystery3 — the third case (v0.4.0f, Queue #4 #6):
// "What's Buried".
//
// Where the pond case (Mystery.kt) was witness-driven chapters
// spread over a season, and the booth case (Mystery2.kt) was a
// compressed single-room whodunit, this one is OBJECT-driven
// and slow: an excavation. Tegridy breaks ground on a new
// field and the diggers keep pulling up things that shouldn't
// be there — traces of a commune that farmed that valley in
// the seventies, in layers that make no sense. Over four
// weekly chapters, dig sites open across the farm's edge and
// two neighboring spots, and what's down there becomes the
// town's obsession.
//
// The solution, in one breath: the last site holds a
// strongbox with the land's original paperwork. The field
// was COMMUNAL land; the commune never legally dissolved,
// and Randy's purchase papers have a hole exactly where the
// commune's claim sits. Randy has suspected since the day he
// bought the place — a told-no-one fear that explains his
// chapter-1 nerves. Sharon does not know. There is no thief
// and no crime. The culprit is a deed. (This paragraph is
// the ONLY place outside the finale's own payoff text where
// the shape of it is written down; public surfaces — the
// board, the chapters, the changelog — carry the premise
// and never the answer.)
//
// Structure:
//  - CHAPTERS3: four full ShenaniganDefs on the same episode
//    machinery. They live OUTSIDE the random pool but resolve
//    in SHENANIGAN_BY_ID, exactly like chapters and beats
//    before them.
//  - Gating: the case queues into the mystery slot TEN days
//    after the booth case retires. While either earlier case
//    is open this one does not even schedule — the slot has
//    one case at a time, always.
//  - Clues: 12, object-forward — 6 LOOK finds (the artifacts
//    themselves, planted at the sites as chapters open them),
//    3 WITNESS talks, 2 PARTICIPATION earns, 1 PAPER clue
//    (a gap in a file at City Hall).
//  - The sites change the map: each opened site draws dig
//    dressing (DIG_SITES' tokens) at its location for the
//    arc's duration, via TownInfo -> SceneArt. After
//    retirement the sites grass over; one ending keeps a
//    small permanent marker stone at the farm.
//  - The finale (chapter 4's climax): not an accusation — a
//    DISPOSITION. Holding at least DISPOSITION_MIN_CLUES of
//    the case, the player decides what happens to the box:
//    publish it, bury it again, or give it to Sharon (whose
//    decision lands a day later, flavored by however her own
//    storyline with Randy resolved). No choice at all and
//    the county takes the box away to be catalogued.
//  - Retirement: every landing retires the arc permanently.
//
// PURE Kotlin (no android imports): the engine and the
// Activities call in; the JVM suite pins the behavior.
// All text newly written; the premise is ours.
// ============================================================

object Mystery3 {

    const val CASE_TITLE = "What's Buried"
    const val FINALE3_ID = "arc3_ch4_buried"
    const val CHAPTER_GAP_DAYS = 7
    const val OPEN_DELAY_DAYS = 10     // after the booth case retires
    const val DISPOSITION_MIN_CLUES = 8
    const val SHARON_DECISION_DELAY_DAYS = 1

    /** The Marsh marriage storyline whose ending colors the
     *  day Sharon gets the box (Stories.kt, v0.3.0c). */
    const val MARSH_ARC_ID = "randy_sharon_line"
    const val MARSH_ARC_WARM = "randy_sharon_warm"

    /** The dispositions that can be chosen at the climax,
     *  and the landing an unchosen finale falls to. */
    val CHOICE_KEYS = listOf("publish", "bury", "sharon")
    const val DEFAULT_BRANCH = "county"
    val BRANCH_KEYS = listOf("publish", "bury", "sharon", "county")

    // ---------------- the four chapters ----------------

    val CHAPTERS3: List<ShenaniganDef> = listOf(
        ShenaniganDef(
            id = "arc3_ch1_groundbreaking",
            title = "Groundbreaking",
            cause = "Tegridy is breaking ground on a new field out past the barn. " +
                "The backhoe had been at it an hour when the diggers stopped digging " +
                "and started gathering around the trench, looking in.",
            blurb = "A new field, an old valley, and a trench full of things that " +
                "shouldn't be there. The whole town will have an opinion by lunch.",
            theme = QuestTheme("⛏️", "#c8a05a", "GROUND BROKEN"),
            tintHex = "#2e2a1c",
            stages = listOf(
                ShenaniganStage("Kickoff: The Backhoe Stops",
                    "⛏️ GROUNDBREAKING: The Tegridy diggers have stopped digging. " +
                        "They're standing around the new trench, looking in, not talking.",
                    "The first trench on the new field turned up more than dirt: " +
                        "worked things, lived-in things, old things — in a field Randy " +
                        "has farmed for years without turning up so much as a button.",
                    "Head out to Tegridy Farms and see the trench.",
                    routine = "cluster", clusterLoc = "TegridyFarms",
                    clusterActivity = "Standing at the trench edge. Looking in. Not talking.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 2,
                            note = "The dig is Tegridy's now. The farm has a trench and a crowd."))),
                ShenaniganStage("Escalation: The Sift Line",
                    "⛏️ A sift line has formed at the trench. Everything that comes " +
                        "out of the ground goes through it, and everything that goes " +
                        "through it gets quieter.",
                    "Beads. A strap buckle. Half a plate with flowers on it. The " +
                        "layers don't sit right either — the diggers keep trading " +
                        "looks about which layer was under which.",
                    "Work the sift line at Tegridy Farms.",
                    routine = "cluster", clusterLoc = "TegridyFarms",
                    clusterActivity = "Working the sift. Holding up finds. Going quiet.",
                    tone = "whisper", litterBurst = 1,
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 2,
                            note = "The Parents remember things about that valley. Carefully."))),
                ShenaniganStage("Climax: A Layer That Isn't",
                    "⛏️ CLIMAX: The trench has hit a layer that shouldn't exist — flat, " +
                        "laid, deliberate — down where a field should just be field.",
                    "It's not a floor, exactly, and it's not a road. It's a flat, " +
                        "careful layer of laid stone and packed earth, running under " +
                        "the field like a sentence somebody stopped writing.",
                    "Get to the trench at Tegridy Farms. Look at the layer.",
                    routine = "cluster", clusterLoc = "TegridyFarms",
                    clusterActivity = "Staring at the layer. Somebody's brought a measuring tape.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 3,
                            note = "The Boys' Crew has opened an investigation. Again. Obviously."))),
                ShenaniganStage("Resolution: Roped Off",
                    "⛏️ The trench is roped off for the night. The county's been " +
                        "called — 'for the paperwork'. Digging continues next week, " +
                        "carefully, and not just in the field.",
                    "Randy walked the rope line twice before supper. The diggers " +
                        "say there's the same layer running toward the pond, and " +
                        "the county man said a word nobody liked: 'survey'.",
                    "Watch them rope off the trench.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Randy", "Stan"),
            episodeTitles = listOf("The Field Keeps Its Counsel", "Do Not Dig Past The Rope"),
            coldOpen = listOf(
                Line("Randy", "New field. Clean field. Nothing in that field but dirt " +
                    "and future profits. DIG, gentlemen."),
                Line("Stan", "Dad's been staring at the trench for ten minutes. " +
                    "The trench hasn't done anything. Yet."))),
        ShenaniganDef(
            id = "arc3_ch2_layers",
            title = "The Layers",
            cause = "The layer under the Tegridy field runs downhill, the way water " +
                "and old intentions do. The county survey puts its far edge at the " +
                "pond — so this week, the digging moves to Stark's Pond's far bank.",
            blurb = "Second site: the pond's far bank. The artifacts get older. " +
                "The layers get wronger. The old-timers start saying a word: commune.",
            theme = QuestTheme("🏺", "#8ab8c8", "THE LAYERS"),
            tintHex = "#1e2c30",
            stages = listOf(
                ShenaniganStage("Kickoff: The Bank Opens",
                    "🏺 THE LAYERS: A second dig has opened on the pond's far bank. " +
                        "New trench, same layer, older dirt.",
                    "The bank dig is smaller and slower — brushes, not backhoes. " +
                        "The layer's here too, running under the pond path like it " +
                        "was laid by people who expected to stay.",
                    "Visit the far-bank dig at Stark's Pond.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Brushing dirt off history at the far bank.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("boys_crew", influenceDelta = 2,
                            note = "The Crew has a site map now. It has string on it. It's official."))),
                ShenaniganStage("Escalation: Wrong Order",
                    "🏺 The bank's layers are coming up in the wrong order — newer " +
                        "things UNDER older things, neat as shelves in a pantry.",
                    "Buried fast and tidied after: that's the shape of it. Whoever " +
                        "lived on this layer packed it down like a bed they meant " +
                        "to come back to.",
                    "Examine the bank's layers at Stark's Pond.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Comparing layers. Arguing about which century is up.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: The Tin Box",
                    "🏺 CLIMAX: A tin box came out of the bank today, sealed with " +
                        "wax and optimism. The whole dig gathered around it the way " +
                        "you gather around a phone that's about to buzz.",
                    "Printed matter, mostly — they say. Kept dry fifty years by " +
                        "wax and stubbornness. The diggers logged it, photographed " +
                        "it, and argued about who it belongs to now. Nobody won.",
                    "Be at Stark's Pond for the tin box.",
                    routine = "cluster", clusterLoc = "StarksPond",
                    clusterActivity = "Watching the tin box like it might ring.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("parents", influenceDelta = 3,
                            note = "A word is going around the Parents: 'commune'. " +
                                "Said fondly. Mostly."))),
                ShenaniganStage("Resolution: The Word Going Around",
                    "🏺 The bank dig closes for the week. The layer keeps running — " +
                        "toward the school. Toward the old oak behind it, specifically.",
                    "There's a map now, drawn on the back of a feed sack: the " +
                        "field, the bank, and a third patch of ground under the old " +
                        "oak, where the school lets nobody dig. Yet.",
                    "Look at the feed-sack map. Rest your arms.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Jimbo", "Stuart"),
            episodeTitles = listOf("Newer Under Older", "The Word Going Around"),
            coldOpen = listOf(
                Line("Jimbo", "Layers don't lie, boy. People lie. Dirt just... " +
                    "arranges the truth funny."),
                Line("Stuart", "My daddy used to say don't dig by the pond. " +
                    "Never said why. Just — don't."))),
        ShenaniganDef(
            id = "arc3_ch3_oaklot",
            title = "The Oak Lot",
            cause = "The school board voted, 3–2, to let the dig onto the old oak " +
                "lot behind the Elementary — one week, supervised, with the fence " +
                "kept and the oak itself strictly off-limits.",
            blurb = "Third site: the old oak lot. The town picnics at the fence " +
                "line. Sharon has started asking a question she hasn't finished.",
            theme = QuestTheme("🌳", "#7ab86a", "THE OAK LOT"),
            tintHex = "#22301e",
            stages = listOf(
                ShenaniganStage("Kickoff: The Fence Goes Up",
                    "🌳 THE OAK LOT: The fence is up around the old oak lot and the " +
                        "third dig is open. The oak gets a ribbon: DO NOT TOUCH THE TREE.",
                    "Smallest site, oldest shade. The diggers work the lot's edges " +
                        "first, the way you circle a sleeping animal.",
                    "Visit the oak lot dig at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Digging the lot's edges. Not looking at the tree. Looking at the tree.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("school_staff", influenceDelta = 2,
                            note = "The school hosts the dig now; the Staff walk the fence at lunch."))),
                ShenaniganStage("Escalation: The Fence-Line Picnic",
                    "🌳 THE FENCE-LINE PICNIC: Half the town has brought lunch to " +
                        "the fence, the way you do. Blankets, thermoses, opinions.",
                    "It's a dig, a picnic, and a reunion at once: people are " +
                        "recognizing things — in the sift, in old photographs, in " +
                        "each other. The oak shades all of it.",
                    "Eat at the fence line. Listen.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Picnicking at the fence line. Remembering out loud.",
                    tone = "hype", litterBurst = 2,
                    factionEffects = listOf(
                        FactionEffect("girls_circle", influenceDelta = 2,
                            note = "The Girls' Circle is keeping the picnic's guest book. It matters."))),
                ShenaniganStage("Climax: The Sift Provides",
                    "🌳 CLIMAX: The oak lot's sift is giving up the personal stuff " +
                        "now — faces, handwriting, a table's worth of a life — and " +
                        "the dig has gone very careful, and very quiet.",
                    "Whatever the last site is, the lot is pointing at it: the " +
                        "layer, the finds, and the feed-sack map all agree there's " +
                        "one more patch of ground, back where this started. The deep " +
                        "corner of the first field.",
                    "Work the oak lot sift at the Elementary.",
                    routine = "cluster", clusterLoc = "Elementary",
                    clusterActivity = "Sifting slowly. Reading handwriting fifty years old.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: One Site Left",
                    "🌳 The oak lot closes with a vote of thanks and a longer fence. " +
                        "One site left: the deep corner of the first field, next week.",
                    "Sharon drove Stan home the long way, past the farm. At the " +
                        "field she slowed the car down to nearly nothing, and then " +
                        "sped up again, like the field was a sentence she didn't " +
                        "want to finish reading.",
                    "Rest up. Next week: the deep corner.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Sharon", "Wendy"),
            episodeTitles = listOf("Do Not Touch The Tree", "The Meeting Tree"),
            coldOpen = listOf(
                Line("Sharon", "It's good the school's allowing the dig. Randy " +
                    "offered to pay for the whole fence. Randy doesn't offer to " +
                    "pay for fences."),
                Line("Wendy", "I'm keeping the finds list. Every object, every " +
                    "layer. If this is a story, we're going to be able to prove it."))),
        ShenaniganDef(
            id = FINALE3_ID,
            title = "What's Buried",
            cause = "One site left: the deep corner of the first field, where the " +
                "layer goes down instead of across. The county wants it surveyed, " +
                "the town wants it watched, and Randy wants it over.",
            blurb = "The last pit. Whatever the valley has been keeping, it has " +
                "been keeping it here.",
            theme = QuestTheme("🔒", "#e8c53a", "THE LAST PIT"),
            tintHex = "#26221a",
            stages = listOf(
                ShenaniganStage("Kickoff: By Hand Only",
                    "🔒 THE LAST PIT: No backhoe at the deep corner. Brushes and " +
                        "trowels and a town holding its breath, by hand only.",
                    "The deep corner digs slow. The layer here dives — whatever " +
                        "was laid, it was laid deepest where it mattered most.",
                    "Take your place at the deep corner, Tegridy Farms.",
                    routine = "cluster", clusterLoc = "TegridyFarms",
                    clusterActivity = "Digging by hand. Nobody jokes at the deep corner.",
                    tone = "whisper",
                    factionEffects = listOf(
                        FactionEffect("tegridy_crew", influenceDelta = 2,
                            note = "The whole farm walks softer this week."))),
                ShenaniganStage("Escalation: Something Down There",
                    "🔒 The trowels have found an edge. Not stone. Not root. " +
                        "Something straight, and made, and waiting.",
                    "Work has stopped twice for people to just look at each other. " +
                        "Randy has been at the fence since dawn. He hasn't gone " +
                        "in. He hasn't left, either.",
                    "Stay at the deep corner. Watch the edge come clear.",
                    routine = "cluster", clusterLoc = "TegridyFarms",
                    clusterActivity = "Watching an edge come clear out of fifty years of dirt.",
                    tone = "whisper", litterBurst = 1),
                ShenaniganStage("Climax: It Comes Up",
                    "🔒 CLIMAX: It's up. It's out of the ground, it's on the " +
                        "tailgate, it's locked, and it's heavy — and every question " +
                        "this town has asked for a month is suddenly a question " +
                        "about what anybody DOES with it.",
                    "It sits on the tailgate in the sun like it owns the hour. " +
                        "The county man is driving over. The town is gathering. " +
                        "And there's exactly one person in this crowd who has " +
                        "done the whole case — every find, every layer, every word.",
                    "Stand by the tailgate. Decide what you know.",
                    routine = "cluster", clusterLoc = "TegridyFarms",
                    clusterActivity = "Standing by the tailgate. The hour belongs to the box.",
                    tone = "whisper"),
                ShenaniganStage("Resolution: The Morning After the Box",
                    "🔒 By morning, the box will have its answer — the town's, " +
                        "the dark's, Sharon's, or the county's. The field gets " +
                        "its grass back either way.",
                    "Whatever happens to it, the digging is done. The valley " +
                        "goes back to being a valley: field, bank, oak, and one " +
                        "corner where the grass will always grow a little different.",
                    "See what the town makes of it.",
                    routine = "normal", tone = "none")
            ),
            causers = listOf("Randy", "Sharon", "Stan"),
            episodeTitles = listOf("What The Ground Gives Back", "What The Valley Kept"),
            coldOpen = listOf(
                Line("Randy", "Last pit. Then it's all just farming again. " +
                    "Normal farming. Say it's normal farming, Stan."),
                Line("Sharon", "Whatever comes out of that ground, Randy — it " +
                    "comes out in front of everybody. Remember that.")))
    )

    val CHAPTER3_BY_ID: Map<String, ShenaniganDef> = CHAPTERS3.associateBy { it.id }

    /** 0-based chapter index of a def id, or -1 when it's not ours. */
    fun chapterIndexOf3(defId: String?): Int =
        CHAPTERS3.indexOfFirst { it.id == defId }

    fun isChapter3(defId: String?): Boolean = chapterIndexOf3(defId) >= 0

    // ---------------- the dig sites ----------------

    data class DigSite(
        val id: String,
        /** 0-based chapter whose opening opens this site. */
        val chapter: Int,
        val locId: String,
        val name: String,
        val tokens: List<String>
    )

    /** The permanent marker an ending may leave at the farm —
     *  a flat stone by the gate. Drawn by the same SceneArt
     *  pass as the dig tokens (`// dig: marker-stone`). */
    const val MARKER_TOKEN = "marker-stone"
    const val MARKER_LOC = "TegridyFarms"

    val DIG_SITES: List<DigSite> = listOf(
        DigSite("field", 0, "TegridyFarms", "The new field",
            listOf("field-pit", "field-spoil", "field-grid", "field-tent")),
        DigSite("pondbank", 1, "StarksPond", "The pond's far bank",
            listOf("bank-pit", "bank-spoil", "bank-grid")),
        DigSite("oaklot", 2, "Elementary", "The old oak lot",
            listOf("oak-pit", "oak-spoil", "oak-grid")),
        DigSite("deepfield", 3, "TegridyFarms", "The deep corner",
            listOf("deep-pit", "deep-spoil", "deep-grid", "deep-tent"))
    )

    val DIG_TOKENS: Set<String> =
        DIG_SITES.flatMap { it.tokens }.toSet() + MARKER_TOKEN

    /** Does this save's ending keep the marker stone? Public
     *  (publish) keeps it the day the case closes; Sharon's
     *  goes up when her decision lands. Buried-again and
     *  county endings leave nothing showing — that's rather
     *  the point of both. */
    fun keepsMarker(state: GameState): Boolean = when (state.arc3Branch) {
        "publish" -> true
        "sharon" -> state.arc3SharonText.isNotEmpty()
        else -> false
    }

    /** The dig dressing live right now, by location — what
     *  TownInfo carries into SceneArt. A site shows from the
     *  chapter that opens it until the case retires; after
     *  retirement the sites grass over and only the marker
     *  (where the ending keeps one) remains. */
    fun digTokensByLoc(state: GameState): Map<String, List<String>> {
        if (state.arc3Retired) {
            return if (keepsMarker(state)) mapOf(MARKER_LOC to listOf(MARKER_TOKEN))
            else emptyMap()
        }
        if (!state.arc3Started) return emptyMap()
        val out = linkedMapOf<String, MutableList<String>>()
        for (site in DIG_SITES) {
            if (site.chapter < state.arc3Chapter) {
                out.getOrPut(site.locId) { mutableListOf() }.addAll(site.tokens)
            }
        }
        return out
    }

    // ---------------- the clues ----------------

    enum class Kind3 { WITNESS, LOOK, PARTICIPATION, PAPER }

    data class Clue3(
        val id: String,
        val kind: Kind3,
        /** 0-based chapter that must have BEGUN for this clue to exist. */
        val minChapter: Int,
        val title: String,
        val detail: String,
        /** WITNESS: the cast member who tells you. */
        val npc: String? = null,
        /** WITNESS: the line they say (the telling IS the discovery). */
        val spoken: String? = null,
        /** LOOK / PAPER: the location to Look at. */
        val locId: String? = null,
        /** LOOK / PAPER: the sentence the Look reports. */
        val foundText: String? = null,
        /** PARTICIPATION: the chapter def whose climax turf grants it. */
        val chapterDefId: String? = null
    )

    val CLUES3: List<Clue3> = listOf(
        Clue3("bead_strand", Kind3.LOOK, 0,
            "A strand of beads",
            "Out of the first pit's sift: a strand of glass beads on a rotted " +
                "string, the colors still bright. Somebody wore these to work " +
                "this field, a long time before it was this field.",
            locId = "TegridyFarms",
            foundText = "🔍 In the sifter, kept back for you: a strand of glass " +
                "beads on a string that's mostly memory now. Fifty years in the " +
                "ground and the blue ones are still blue."),
        Clue3("waterbed_key", Kind3.LOOK, 1,
            "A waterbed key",
            "In the bank mud: a flat plastic key, the kind that winds a waterbed " +
                "frame tight. Nothing says 'seventies' like sleeping on a bag of " +
                "water and needing a key for it.",
            locId = "StarksPond",
            foundText = "🔍 Stuck flat in the bank mud, right at the layer: a " +
                "small plastic key — a waterbed key, the diggers say, grinning. " +
                "The pond keeps what the valley drops."),
        Clue3("commune_newsletter", Kind3.LOOK, 1,
            "A commune newsletter",
            "From the tin box in the bank: 'THE VALLEY VOICE', four pages, " +
                "hand-duplicated. Work rosters, a bean report, a birthday list — " +
                "and a masthead of names. Half the surnames on it are still on " +
                "mailboxes in this town.",
            locId = "StarksPond",
            foundText = "🔍 They let you read it under supervision: THE VALLEY " +
                "VOICE, vol. 4 — a commune newsletter, mimeographed purple, " +
                "mostly beans and birthdays. The names, though. The names are " +
                "this town's names, fifty years younger."),
        Clue3("photo_strip", Kind3.LOOK, 2,
            "A strip of photo-booth photos",
            "Four frames, glue-backed, from the oak lot's sift: young faces " +
                "mugging for a booth camera. One of them — the grin, the jaw — " +
                "is unmistakably a Marsh. The older kind of Marsh.",
            locId = "Elementary",
            foundText = "🔍 Out of the oak lot's sift, in a twist of rotted " +
                "cloth: a photo-booth strip. Four frames of kids who are " +
                "somebody's grandparents now, mugging like the camera owed them."),
        Clue3("seed_jar", Kind3.LOOK, 2,
            "A mason jar of seeds",
            "A mason jar, sealed, full of seeds, with a paper label in a " +
                "careful hand: 'FOR WHOEVER FARMS HERE NEXT.' Somebody planted " +
                "for a farmer they would never meet.",
            locId = "Elementary",
            foundText = "🔍 The oak lot gives up a mason jar, still sealed, " +
                "still full of seeds. The label's in pencil, in handwriting " +
                "a teacher would approve of: 'FOR WHOEVER FARMS HERE NEXT.'"),
        Clue3("strongbox", Kind3.LOOK, 3,
            "The strongbox",
            "It comes up at the deep corner: iron-banded, locked, heavy in " +
                "the way of things never meant to be carried twice. Whatever " +
                "the valley has been keeping, it has been keeping it in this.",
            locId = "TegridyFarms",
            foundText = "🔍 Before the crowd gets to it, you get one close " +
                "look: iron bands, a lock that means it, and a weight that " +
                "says paper — a lot of paper, kept on purpose. The valley's " +
                "whole month narrows to one locked box."),
        Clue3("oldtimer_memory", Kind3.WITNESS, 1,
            "Stuart remembers the commune",
            "Stuart was a kid when they farmed the valley. Vans, gardens, a " +
                "long table outside — and then, one year, nobody. Nobody left. " +
                "They were just… not there anymore.",
            npc = "Stuart",
            spoken = "That valley? Shoot. When I was a kid there was FOLKS " +
                "livin' out there. Vans, gardens, a big ol' table outside ate " +
                "forty people. My daddy said don't go past their fence — " +
                "commune folks. Nice enough. Just, y'know. Communal. Then one " +
                "year they was just gone. House empty, gardens gone wild. " +
                "Nobody in this town ever said where, and I mean EVER."),
        Clue3("night_cart", Kind3.WITNESS, 1,
            "Jimbo and the night cart",
            "The old story says the commune carted everything away in one " +
                "night. Jimbo, who was hunting that valley all that fall, " +
                "calls it exactly what it is: nobody carted anything anywhere.",
            npc = "Jimbo",
            spoken = "Story goes them commune folks carted everything out in " +
                "one night, loaded a truck, gone by dawn. Son, I was huntin' " +
                "that valley near every night that whole fall, and I am here " +
                "to tell you: nobody carted NOTHIN' outta there. No truck, " +
                "no lights, no ruts. Whatever they left behind, they left " +
                "it IN that valley. Deep, is my guess. People bury what " +
                "they mean to come back for."),
        Clue3("sharon_suspicion", Kind3.WITNESS, 2,
            "Sharon's suspicion",
            "Something she has never said out loud: Randy has been strange " +
                "about that field since the day the farm was bought. Not " +
                "money-strange. Older than that.",
            npc = "Sharon",
            spoken = "Can I tell you something I haven't said out loud? " +
                "Randy has been strange about that field since the DAY we " +
                "bought this farm. And I know Randy's stranges — there's " +
                "money-strange, there's scheme-strange. This one is older. " +
                "He walks that field like it might talk. He has always, " +
                "always walked it like that. And now it's talking."),
        Clue3("dig_shift", Kind3.PARTICIPATION, 0,
            "A shift on the sift",
            "You worked a shift at the first pit's sift line. The layers " +
                "come up wrong there: newer things UNDER older things, neat " +
                "as shelves. Whatever happened in that field happened in a " +
                "hurry — and then somebody tidied.",
            chapterDefId = "arc3_ch1_groundbreaking"),
        Clue3("fence_picnic", Kind3.PARTICIPATION, 2,
            "The fence-line picnic",
            "At the picnic you heard it from three different blankets: the " +
                "old oak was 'the meeting tree', the table under it seated " +
                "forty, and the valley fed itself. Nobody says the word " +
                "commune out loud. Everybody describes one.",
            chapterDefId = "arc3_ch3_oaklot"),
        Clue3("purchase_gap", Kind3.PAPER, 1,
            "A gap in the purchase file",
            "The Tegridy purchase file at City Hall is fat and complete — " +
                "surveys, signatures, a photo of Randy with a giant check. " +
                "But its parcel index skips a page: 6, then 8. And a clerk's " +
                "sticky note, dated the year it was scanned, says: 'page " +
                "already missing when filed.'",
            locId = "CityHall",
            foundText = "🔍 The clerk lets you see the Tegridy purchase file — " +
                "the whole farm, bought fair and filed thick. Except: the " +
                "parcel index runs 6, then 8. There's a sticky note on the " +
                "gap, in a clerk's hand, years old: 'page already missing " +
                "when filed.' One parcel of that farm has no page at all.")
    )

    val CLUE3_BY_ID: Map<String, Clue3> = CLUES3.associateBy { it.id }
    val CLUE3_IDS: Set<String> = CLUES3.map { it.id }.toSet()

    fun clueCount3(state: GameState): Int = state.mystery3Clues.size

    /** A clue exists in the world once its chapter has begun
     *  and the case is still open. */
    fun clueAvailable3(state: GameState, clue: Clue3): Boolean =
        !state.arc3Retired && state.arc3Chapter > clue.minChapter

    /** Grant a clue exactly once. The ticker + the town log
     *  hear about it; the Journal board keeps it forever. */
    fun grantClue3(state: GameState, clueId: String): Boolean {
        val clue = CLUE3_BY_ID[clueId] ?: return false
        if (clueId in state.mystery3Clues) return false
        state.mystery3Clues.add(clueId)
        state.events.add("🔍 CLUE ${state.mystery3Clues.size}/${CLUES3.size}: ${clue.title}. " +
            "(Journal → 🔍 MYSTERY)")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🔍 Clue found: ${clue.title}")
        return true
    }

    /** Talk-to discovery: if [npcName] is a live witness right
     *  now, their line — and the clue it grants. Null otherwise. */
    fun witnessLine3(state: GameState, npcName: String): Line? {
        val clue = CLUES3.firstOrNull {
            it.kind == Kind3.WITNESS && it.npc == npcName
        } ?: return null
        if (!clueAvailable3(state, clue)) return null
        if (!grantClue3(state, clue.id)) return null
        return Line(npcName, clue.spoken ?: "")
    }

    /** Look discovery: the sentence a Look at [locId] reports
     *  the first time it finds each thing buried there — a
     *  location with several finds (the farm, the pond, the
     *  school) gives them up one at a time, in the order the
     *  case has opened them. The City Hall file (PAPER) rides
     *  the same rail. */
    fun lookClueText3(state: GameState, locId: String): String? {
        val clue = CLUES3.firstOrNull {
            (it.kind == Kind3.LOOK || it.kind == Kind3.PAPER) &&
                it.locId == locId && it.id !in state.mystery3Clues &&
                clueAvailable3(state, it)
        } ?: return null
        if (!grantClue3(state, clue.id)) return null
        return clue.foundText
    }

    /** Hands-dirty discovery: called from Engine.bumpParticipation
     *  whenever a participation point lands during a chapter. */
    fun noteParticipation3(state: GameState, defId: String) {
        val clue = CLUES3.firstOrNull {
            it.kind == Kind3.PARTICIPATION && it.chapterDefId == defId
        } ?: return
        if (!clueAvailable3(state, clue)) return
        if (grantClue3(state, clue.id)) {
            state.events.add("🔍 Up close, something clicks: ${clue.detail}")
            while (state.events.size > 40) state.events.removeAt(0)
        }
    }

    // ---------------- scheduling ----------------

    /** The chapter that should take today's event slot, if
     *  any. Also SCHEDULES the case: the first ask after the
     *  booth case retires books chapter 1 ten days out from
     *  that retirement. While either earlier case is open
     *  this never fires — and never even books the date:
     *  the mystery slot has one case at a time. Chapters
     *  defer, day by day, past holiday episodes, like every
     *  case before them. */
    fun dueChapter3(state: GameState): ShenaniganDef? {
        if (state.arc3Retired) return null
        if (!state.arc2Retired) return null
        if (state.arc3Chapter >= CHAPTERS3.size) return null
        if (state.arc3DueAbsDay < 0) {
            state.arc3DueAbsDay = if (state.arc3Chapter == 0) {
                val anchor = if (state.arc2RetiredDay >= 0)
                    state.arc2RetiredDay else state.absDay
                maxOf(state.absDay, anchor + OPEN_DELAY_DAYS)
            } else state.absDay
        }
        if (state.absDay < state.arc3DueAbsDay) return null
        if (state.holidayId != null) return null // defer — holidays keep their day
        return CHAPTERS3[state.arc3Chapter]
    }

    /** Engine hook: a chapter just took the slot. Books it
     *  and sets the next chapter's due day a week out. */
    fun noteChapterStarted3(state: GameState, def: ShenaniganDef) {
        val idx = chapterIndexOf3(def.id)
        if (idx < 0) return
        state.arc3Started = true
        state.arc3Chapter = idx + 1
        state.arc3DueAbsDay = state.absDay + CHAPTER_GAP_DAYS
    }

    // ---------------- the finale: the disposition ----------------

    /** The choice is on the table: the last chapter is
     *  airing, its climax has begun, no disposition is locked
     *  yet, and the player holds enough of the case to decide
     *  what happens to the box. */
    fun dispositionDue(state: GameState): Boolean {
        if (state.arc3Retired || state.arc3Choice.isNotEmpty()) return false
        if (state.shenaniganId != FINALE3_ID) return false
        if (Shenanigans.stageFor(state.timeMin) < 2) return false
        return clueCount3(state) >= DISPOSITION_MIN_CLUES
    }

    /** Lock the player's disposition in at the climax. */
    fun chooseDisposition(state: GameState, choice: String): Boolean {
        if (choice !in CHOICE_KEYS) return false
        if (!dispositionDue(state)) return false
        state.arc3Choice = choice
        state.events.add(when (choice) {
            "publish" ->
                "🔍 You look at the box, and at the town around it. This " +
                    "belongs to everybody. You're taking it to the meeting."
            "bury" ->
                "🔍 You look at the box. You look at Randy, at the fence, " +
                    "not meeting anyone's eye. Some things the ground can keep."
            else ->
                "🔍 You pick up the box. It's heavier than it looks. " +
                    "There's exactly one person who should decide this."
        })
        while (state.events.size > 40) state.events.removeAt(0)
        return true
    }

    data class DispositionOutcome(
        val branch: String,          // "publish" | "bury" | "sharon" | "county"
        val outcome: String,         // the wrap's outcome line
        val recapLines: List<String> // extra credits lines for the episode page
    )

    /** Resolve the finale at wrap time: pick the landing,
     *  stamp the arc RETIRED forever, and hand the wrap its
     *  words. A disposition locked without the case behind it
     *  (impossible through the UI — the choice never appears)
     *  degrades to the county's ending rather than a free
     *  decide, exactly like the accusation degrades cold. */
    fun resolveDisposition(state: GameState): DispositionOutcome {
        val chosen = state.arc3Choice
        val backed = clueCount3(state) >= DISPOSITION_MIN_CLUES
        val branch = if (chosen in CHOICE_KEYS && backed) chosen else DEFAULT_BRANCH
        state.arc3Branch = branch
        state.arc3Retired = true
        state.arc3RetiredDay = state.absDay
        if (branch == "sharon") {
            state.arc3SharonPending = true
            state.arc3SharonDay = state.absDay + SHARON_DECISION_DELAY_DAYS
        }
        return when (branch) {
            "publish" -> DispositionOutcome(branch,
                "You set the box on the meeting table and the town opened its " +
                    "own month in front of itself. It was a long meeting. It was " +
                    "the best one this town has ever had.",
                listOf(
                    "🔍 THE DEED: The box held the land's original paperwork. " +
                        "That field was communal land — the commune's own " +
                        "charter, signed in the seventies, never dissolved, " +
                        "never sold. Randy's purchase covered the rest of the " +
                        "farm. It never covered that field, and some part of " +
                        "him had been waiting thirty years to hear it said.",
                    "🔍 THE VOTE: The field goes into a community trust. " +
                        "Tegridy farms it under lease, Randy signed first, " +
                        "and Sharon held the pen while he did it. The commune's " +
                        "names go on a stone by the gate — all of them, spelled " +
                        "the way they spelled them."))
            "bury" -> DispositionOutcome(branch,
                "That night, the deep corner got its box back, and two people " +
                    "in South Park know where it is, and one of them isn't " +
                    "telling, and the other one owes you. Forever.",
                listOf(
                    "🔍 THE WEIGHT: You never opened it. You didn't have to. " +
                        "A man doesn't white-knuckle an empty box, and whatever " +
                        "paper slept in that dirt is sleeping there still — " +
                        "but now it has a bodyguard, and the bodyguard is you.",
                    "🔍 THE FAVOR: At the fence, in the dark, Randy Marsh " +
                        "said the words 'anything, ever, you just say it' — " +
                        "and a Marsh in your debt is a currency this town " +
                        "doesn't print. Spend it well. Or never. He'll keep."))
            "sharon" -> DispositionOutcome(branch,
                "You gave the box to Sharon. No speech, no meeting, no " +
                    "crowd — a woman, a box, and a hall closet. What she " +
                    "does with it will take a day. Sharon Marsh does not " +
                    "rush a decision that can't be un-made.",
                listOf(
                    "🔍 THE HANDOFF: She didn't ask what was in it. She asked " +
                        "if Randy knew it existed — and her face, when you " +
                        "told her, was a whole meeting all by itself.",
                    "🔍 HER CALL: It lands tomorrow. The board will keep the " +
                        "case open one more day — some things are worth " +
                        "staying open for."))
            else -> DispositionOutcome(branch,
                "Nobody decided, so the county did: the box went into a " +
                    "truck, then a warehouse, then a catalogue. The digs " +
                    "grassed over. The field went back to being a field, " +
                    "and the farm went back to watching it.",
                listOf(
                    "🔍 THE CATALOGUE: The box has a number now. Numbers are " +
                        "where mysteries go to be very safe and very, very " +
                        "far away. The county will catalogue it thoroughly, " +
                        "in order, forever.",
                    "🔍 Randy farms that field like a man mowing around " +
                        "something. If you ask him what, he says 'rocks'. " +
                        "There are no rocks. There were never any rocks."))
        }
    }

    /** The landing's ripples + its permanent marks — called
     *  by the wrap BEFORE the episode's credits are built, so
     *  the recap's faction-shift diff includes them. */
    fun applyDispositionEffects(state: GameState, branch: String) {
        fun ripple(factionId: String, delta: Int, note: String) {
            val fs = state.factionStates[factionId] ?: return
            fs.influence = (fs.influence + delta).coerceIn(0, 100)
            if (delta != 0) {
                fs.moodScore = (fs.moodScore + if (delta > 0) 1 else -1).coerceIn(-2, 2)
            }
            if (note.isNotEmpty()) {
                fs.mood = note
                state.events.add("⚔️ FACTION SHIFT: $note")
                while (state.events.size > 40) state.events.removeAt(0)
            }
        }
        fun befriend(name: String, delta: Int) {
            state.friendship[name] =
                ((state.friendship[name] ?: 0) + delta).coerceAtLeast(0)
        }
        when (branch) {
            "publish" -> {
                ripple("tegridy_crew", -4,
                    "The farm is a trust now. Randy signed first, which helped, eventually.")
                ripple("parents", 3,
                    "The Parents approve of a field that belongs to everybody, with paperwork.")
                ripple("boys_crew", 2,
                    "The Crew dug the truth up and the town kept it. A good month for the Crew.")
                befriend("Randy", -2)
                befriend("Sharon", 2)
                state.lifeText["mark:TegridyFarms"] =
                    "A flat stone by the gate, set the week of the meeting: " +
                        "'THIS FIELD IS EVERYBODY'S. FARMED BY TEGRIDY, UNDER " +
                        "LEASE, SINCE THE MEETING.' Randy reads it every " +
                        "morning. Every morning."
                townLogLine(state, "🔍 SETTLED IN PUBLIC: the field is a " +
                    "community trust, and the stone by the gate has names on it.")
            }
            "bury" -> {
                state.arc3Favor = true
                ripple("tegridy_crew", 3,
                    "Randy's in a suspiciously good mood — the digs are done and the field is his to farm.")
                befriend("Randy", 4)
                state.lifeText["mark:TegridyFarms"] =
                    "The deep corner is just a corner again. The grass came " +
                        "back fast. Too fast, if you think about it. Don't."
                townLogLine(state, "🔍 CASE CLOSED QUIETLY: the box went back " +
                    "into the ground at night. One man in town owes you, forever.")
            }
            "sharon" -> {
                ripple("parents", 2,
                    "The Parents are watching the Marsh house the way you watch weather coming.")
                befriend("Sharon", 2)
                state.lifeText["mark:TegridyFarms"] =
                    "The deep corner is grassing over. The box is not in it. " +
                        "The box is in a hall closet, waiting on a decision " +
                        "with a whole day left to run."
                townLogLine(state, "🔍 THE BOX IS WITH SHARON. Her call lands tomorrow.")
            }
            else -> {
                ripple("tegridy_crew", 1,
                    "The digs are done and the field is quiet. The farm exhales, on paper.")
                state.lifeText["mark:TegridyFarms"] =
                    "A flat county tag by the gate: 'SITE 4 — CATALOGUED.' " +
                        "The grass is already eating it."
                townLogLine(state, "🔍 CASE CLOSED BY CATALOGUE: the county " +
                    "has the box, and the valley has its grass back.")
            }
        }
    }

    // ---------------- Sharon's decision (the day after) ----------------

    /** What Sharon does with the box, in her own terms — the
     *  full text the board keeps. In every flavor the shape is
     *  hers: the commune's history honored in a redacted public
     *  record, the farm's tenure on the field secured in
     *  writing, on her terms. HOW she got there — and whether
     *  Randy was beside her or briefed at breakfast — is
     *  colored by how the Marshes' own storyline resolved. */
    fun sharonDecisionText(state: GameState): String {
        val rec = StoryArcs.record(state, MARSH_ARC_ID)
        return when {
            rec != null && rec.done && rec.outcomeId == MARSH_ARC_WARM ->
                "She read everything twice. Then she woke Randy, told him " +
                    "what it meant — all of it, including the part he'd been " +
                    "carrying alone since the day they bought the farm — and " +
                    "the two of them walked it to City Hall together. The " +
                    "commune's history goes on the public record, names " +
                    "honored, a few pages redacted for the living; the farm's " +
                    "lease on the field is secured in writing, on her terms. " +
                    "'He told me the truth when it cost him,' she said. " +
                    "'That's the man I keep.'"
            rec != null && rec.done ->
                "She read everything twice. She did not wake Randy. By " +
                    "morning the paperwork was done the way she does " +
                    "paperwork — the commune's history honored in a redacted " +
                    "public record, the farm's lease on the field secured in " +
                    "writing, every clause hers. Randy learned what had " +
                    "happened from the clerk, holding a coffee he'd made for " +
                    "two. He has been very quiet since, and very helpful, " +
                    "and Sharon has been almost kind about both."
            else ->
                "She read everything twice, slept on it once, and then did " +
                    "the paperwork like a woman defusing something: the " +
                    "commune's history honored in a redacted public record, " +
                    "the farm's lease on the field secured in writing, on " +
                    "her terms, every signature where it belonged. Randy got " +
                    "the terms with his coffee. He read them twice too."
        }
    }

    /** The delayed landing of the "sharon" disposition:
     *  called daily by the engine; fires exactly once, the
     *  day after the case closed — her call arrives as an
     *  event the town hears about, the board's final entry,
     *  and the marker stone the ending promised. */
    fun noteSharonDecision(state: GameState): Boolean {
        if (!state.arc3SharonPending) return false
        if (state.absDay < state.arc3SharonDay) return false
        state.arc3SharonPending = false
        state.arc3SharonText = sharonDecisionText(state)
        state.events.add("🔍 SHARON'S DECISION: The Marsh paperwork landed at " +
            "City Hall this morning — a public record with the commune's " +
            "names honored, a few pages redacted, and the farm's hold on " +
            "its field secured in writing. On her terms. All of it, her terms.")
        while (state.events.size > 40) state.events.removeAt(0)
        townLogLine(state, "🔍 Sharon Marsh settled the box her way: the " +
            "history honored, the farm secured, the terms hers.")
        state.lifeText["mark:TegridyFarms"] =
            "A flat stone by the gate: 'THE VALLEY'S FIRST FARMERS — " +
                "REMEMBERED HERE.' Set by S. Marsh. Randy tends the " +
                "grass around it himself."
        return true
    }

    // ---------------- the favor (the "bury" landing's long tail) ----------------

    private val FAVOR_LINES = listOf(
        "Anything you need. Ever. You just say the word. Because of " +
            "the — you know. The night. With the — you know what " +
            "carried what. I would simply like it noted that I remain " +
            "a man of my word about the thing I am not mentioning.",
        "You sleep good these days, kid? I sleep GREAT. On account " +
            "of a certain somebody doing a certain something at a " +
            "certain fence line. …I made Sharon a smoothie this " +
            "morning. Just felt like a smoothie morning. Unrelated.",
        "Stan asked why I keep doing whatever you say. I told him " +
            "it's called MENTORSHIP. He didn't buy it. Kids can smell " +
            "a debt, apparently. It has a smell. Who knew."
    )

    /** The standing favor: in the ending where Randy owes
     *  you, he says so — a rotating line in his talks, forever.
     *  Null for everyone else, and every other ending. */
    fun favorLine3(state: GameState, npcName: String): Line? {
        if (npcName != "Randy" || !state.arc3Favor) return null
        if (!state.arc3Retired || state.arc3Branch != "bury") return null
        val idx = ((state.absDay % FAVOR_LINES.size) + FAVOR_LINES.size) %
            FAVOR_LINES.size
        return Line("Randy", FAVOR_LINES[idx])
    }

    // ---------------- the Journal board ----------------

    /** The third case shows once it exists: chapter 1 has
     *  aired, a clue is in hand, or the case is closed. */
    fun boardVisible3(state: GameState): Boolean =
        state.arc3Started || state.mystery3Clues.isNotEmpty() || state.arc3Retired

    fun statusStamp3(state: GameState): String = when {
        state.arc3Retired && state.arc3Branch == "publish" ->
            "✅ SETTLED IN PUBLIC — the field is everybody's"
        state.arc3Retired && state.arc3Branch == "bury" ->
            "🤝 CLOSED QUIETLY — a debt stands"
        state.arc3Retired && state.arc3Branch == "sharon" &&
            state.arc3SharonPending -> "📬 WITH SHARON — her call lands tomorrow"
        state.arc3Retired && state.arc3Branch == "sharon" ->
            "✅ SETTLED — on Sharon's terms"
        state.arc3Retired -> "🧊 CLOSED — the county has it"
        state.shenaniganId == FINALE3_ID -> "🔒 THE LAST PIT"
        state.arc3Started ->
            "⛏️ DIGGING — chapter ${state.arc3Chapter} of ${CHAPTERS3.size} aired"
        else -> "🔍 A CASE IS COMING"
    }

    /** What the town currently believes, by where the case is. */
    fun currentTheory3(state: GameState): String = when {
        state.arc3Retired && state.arc3Branch == "publish" ->
            "CLOSED, and in the minutes. The field belongs to everybody, " +
                "the names are on the stone, and Randy farms it like a man " +
                "paying rent to his own town — cheerfully, mostly."
        state.arc3Retired && state.arc3Branch == "bury" ->
            "CLOSED. The box went 'to the county', far as the town knows. " +
                "The town knows wrong. Two people know right."
        state.arc3Retired && state.arc3Branch == "sharon" ->
            "CLOSED, on Sharon's terms. The town doesn't know the terms. " +
                "The town has decided not to want to know the terms."
        state.arc3Retired ->
            "CLOSED. The county has the box, the catalogue has the box's " +
                "number, and the field has its grass back. Case closed. " +
                "(The case is not closed. The case is in a warehouse.)"
        !state.arc3Started ->
            "Nothing yet. The farm is just a farm. Probably."
        state.arc3Chapter <= 1 ->
            "Old junk, says Randy. Old junk in LAYERS, says everybody " +
                "else, and the sift line keeps proving everybody else right."
        state.arc3Chapter == 2 ->
            "A commune, the old-timers say. Here. In the seventies. With " +
                "a newsletter, a long table, and views on property."
        state.arc3Chapter == 3 ->
            "The town has decided it's buried treasure. The town has " +
                "decided this before, about other things, and been wrong " +
                "in both directions."
        else ->
            "Whatever's at the bottom of that last pit, the whole town " +
                "is coming to watch it come up — and somebody's going to " +
                "have to decide what happens next."
    }

    /** The third case's board as plain lines (the Journal
     *  renders them below the booth case's; the suite reads
     *  them). */
    fun boardLines3(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out.add("🔍 $CASE_TITLE")
        out.add(statusStamp3(state))
        out.add("📖 THE DIGS")
        CHAPTERS3.forEachIndexed { i, def ->
            val airing = state.shenaniganId == def.id
            val aired = state.arc3Chapter > i
            val mark = when { airing -> "▶"; aired -> "✓"; else -> "·" }
            val label = when { airing -> "DIGGING NOW"; aired -> "dug"; else -> "not yet" }
            out.add("$mark Chapter ${i + 1}: ${def.title} — $label")
        }
        out.add("⛏️ THE SITES")
        val live = digTokensByLoc(state)
        for (site in DIG_SITES) {
            val status = when {
                state.arc3Retired -> "grassed over"
                site.tokens.any { it in (live[site.locId] ?: emptyList()) } -> "OPEN"
                else -> "undug"
            }
            out.add("• ${site.name} — $status")
        }
        if (state.arc3Retired && keepsMarker(state)) {
            out.add("• A flat stone stands by the Tegridy gate, and " +
                "stays standing.")
        }
        out.add("🧩 CLUES — ${clueCount3(state)}/${CLUES3.size}" +
            if (!state.arc3Retired && clueCount3(state) < DISPOSITION_MIN_CLUES)
                " (deciding the box's fate takes $DISPOSITION_MIN_CLUES)" else "")
        for (clue in CLUES3) {
            if (clue.id in state.mystery3Clues) {
                out.add("• ${clue.title} — ${clue.detail}")
            }
        }
        val missing = CLUES3.size - clueCount3(state)
        if (missing > 0 && !state.arc3Retired) {
            out.add("• ??? — $missing still in the ground. Talk to the " +
                "old-timers. Look where they dig. Work a shift. Read a file.")
        }
        out.add("🗣️ THE TOWN'S THEORY: ${currentTheory3(state)}")
        if (state.arc3Retired) {
            out.add(when (state.arc3Branch) {
                "publish" -> "🏁 You took it to the meeting and the town " +
                    "opened its own month in front of itself. The field is " +
                    "a trust now, and the stone by the gate has names on it."
                "bury" -> "🏁 You gave the ground its box back, at night, " +
                    "with a witness of one. The case is closed. The debt " +
                    "is open. Forever."
                "sharon" -> "🏁 You gave the box to the one person who " +
                    "never got to not-know about that field. The case is " +
                    "closed the Marsh way: correctly, and in writing."
                else -> "🏁 Nobody decided, so the county did. The case " +
                    "is closed the catalogue's way: numbered, shelved, " +
                    "and very far away."
            })
            if (state.arc3Branch == "sharon") {
                if (state.arc3SharonPending) {
                    out.add("📬 Sharon has the box. Her call lands " +
                        "tomorrow — the board stays open one more day.")
                } else if (state.arc3SharonText.isNotEmpty()) {
                    out.add("📬 SHARON'S DECISION: ${state.arc3SharonText}")
                }
            }
            if (state.arc3Branch == "bury") {
                out.add("🤝 Randy owes you one. He knows it. You know " +
                    "it. It comes up in his conversation now — it will " +
                    "come up forever.")
            }
        }
        return out
    }

    // ---------------- the long tail (ambient callbacks) ----------------

    private val CALLBACKS3 = mapOf(
        "publish" to listOf(
            "You been out to the trust field? Randy was there at dawn, " +
                "working it like it owes HIM. The town can watch any " +
                "time, says the lease. The town watches.",
            "My grandma's name's on that stone. She never told us about " +
                "the commune years. Fifty years, and she let a ROCK " +
                "mention it first."),
        "bury" to listOf(
            "Randy's been weirdly generous lately. Gave the boys the " +
                "good tour, whistling. Randy doesn't whistle. Randy has " +
                "NEVER whistled.",
            "Whatever that box was, it's the county's problem now. " +
                "That's the official story and Randy tells it beautifully. " +
                "Too beautifully, my dad says."),
        "sharon" to listOf(
            "Sharon Marsh got that whole field's paperwork redone in a " +
                "week. A WEEK. Randy just nodded at everything. It was " +
                "beautiful to watch.",
            "There's a stone by the Tegridy gate now. 'First farmers.' " +
                "Randy weeded around it on Sunday. Randy does not weed " +
                "on Sundays."),
        "county" to listOf(
            "The county still has that box, far as I know. Cataloguing. " +
                "It's been a while. Cataloguing takes as long as it " +
                "takes, apparently.",
            "Randy farms that field like he's mowing around something. " +
                "Ask him what and he says 'rocks'. There are no rocks. " +
                "You've SEEN the sift line. No rocks.")
    )

    /** A post-case ambient aside in the town's own voice — the
     *  ending the town lives with is the one it keeps telling.
     *  Null until the case is closed. */
    fun ambientCallback3(state: GameState, rnd: Random): String? {
        if (!state.arc3Retired) return null
        val bank = CALLBACKS3[state.arc3Branch] ?: return null
        return bank[rnd.nextInt(bank.size)]
    }
}

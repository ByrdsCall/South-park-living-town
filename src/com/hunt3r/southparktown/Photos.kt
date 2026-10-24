package com.hunt3r.southparktown

import kotlin.math.roundToInt
import kotlin.random.Random

// ============================================================
// Photos — the selfie system (v0.2.0c, Queue #2 item 3).
//
// The Fractured But Whole's signature social mechanic, rebuilt
// in this game's own terms: point the camera at whoever is
// standing with you, and the town poses — or doesn't.
//
// PHOTOS ARE DATA, NOT PIXELS. A photo is a record (who, where,
// when, what everyone wore and how they posed); PhotoView
// re-renders it deterministically from the record with the
// game's own figure renderer whenever the Journal album opens
// it. Nothing is stored as a bitmap and no image file ships.
//
// Reactions follow the relationship tiers (Relationships):
//   CLOSE FRIEND  -> GOOFY  (bunny ears, tongues, hat swaps)
//   FRIEND        -> HAPPY
//   ACQUAINTANCE / STRANGER -> NEUTRAL
//   RIVAL         -> REFUSE (mostly) or PHOTOBOMB (1 in 4,
//                    deterministic — and a bomb still counts
//                    as being IN the photo)
// Followers are the FBW joke with our numbers: a first photo
// with someone pays out by their town prominence (cast >
// residents > visitors) scaled by how much they like you;
// re-photos trickle +1. Milestones get ticker celebrations
// and a Journal badge line. Tie-ins: an NPC goal can BE
// "get a photo with the new kid" (Goals PHOTO kind), a selfie
// at a shenanigan cluster counts toward participation ONCE
// per event, and photo FEATS live on the Collection screen
// as additions — they never gate existing content.
//
// v0.4.0c — GROUP PHOTOS (Queue #4 item 3). The selfie grows
// up: with 2+ townsfolk present, the camera offers a posed
// GROUP frame alongside the classic pile-in selfie. A group
// record carries a [PhotoRecord.composition] (lineup |
// squad | stacked) chosen by group size + chemistry, and
// its figure list is written in FRAME ORDER — composeGroup
// arranges it from the town's own pair ledgers (npcBonds,
// NPC_RELATIONS, the PAIR_SCRIPTS registry): high-chemistry
// pairs stand adjacent, rivals keep a gap, and a mutually
// rival pair who both agreed gets the backToBack bit (arms
// crossed, backs turned, secretly pleased). Followers pay
// per person at the group rate (x0.6) plus a chemistry
// bonus for recognized pairs making their frame debut.
// Same record, same album, same judges — no parallel
// pipeline: PHOTO goals, PHOTO errands, feats and the
// once-per-event participation hook all read group records
// exactly as they read selfies.
// ============================================================

/** One figure in a photo. [kind] = player | cast | resident |
 *  visitor. [pose] = none | bunny | tongue | swap | bomb |
 *  backToBack (v0.4.0c: half of a rival pair posing
 *  back-to-back in a group frame).
 *  [points] snapshots the relationship points at shutter time
 *  so follower math stays reproducible from the record. */
data class PhotoFigure(
    val name: String,
    val kind: String,
    val mood: String,          // the KidArt mood the figure wears
    val pose: String = "none",
    val swapWith: String? = null,
    val reaction: String = "neutral",  // goofy|happy|neutral|bomb
    val points: Int = 0,
    /** v0.10.0b: the hero-costume code ("cape|mask|emblem")
     *  this figure wore at shutter time, "" for street
     *  clothes. Frozen on the figure like the appearance
     *  map is frozen on the record: a patrol-night photo
     *  remembers the capes honestly forever, and the album,
     *  the darkroom and the yearbook all render from it.
     *  Additive: every figure from before this field loads
     *  "" and renders exactly as it always did. */
    val hero: String = "",
    /** v0.12.0d: the sleepwear code ("top|bottom|cap")
     *  this figure wore at shutter time, "" for street
     *  clothes. Frozen exactly like [hero], one field over:
     *  a photo composed at Your House while a sleepover
     *  night is live remembers the pajamas honestly
     *  forever, and the album, the street re-render and
     *  the darkroom all render from it. Additive: every
     *  figure from before this field loads "" and renders
     *  exactly as it always did — never retroactive. */
    val sleep: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name, "kind" to kind, "mood" to mood, "pose" to pose,
        "swapWith" to swapWith, "reaction" to reaction, "points" to points,
        "hero" to hero, "sleep" to sleep
    )

    companion object {
        fun fromMap(m: Map<String, Any?>): PhotoFigure? {
            val name = m["name"]?.toString() ?: return null
            return PhotoFigure(
                name = name,
                kind = m["kind"]?.toString() ?: "cast",
                mood = m["mood"]?.toString() ?: "idle",
                pose = m["pose"]?.toString() ?: "none",
                swapWith = m["swapWith"]?.toString(),
                reaction = m["reaction"]?.toString() ?: "neutral",
                points = (m["points"] as? Number)?.toInt() ?: 0,
                hero = m["hero"]?.toString() ?: "",
                sleep = m["sleep"]?.toString() ?: ""
            )
        }
    }
}

/** A stored photo. [followersGained] is filled by applyPhoto
 *  (it depends on who was already photographed before). */
data class PhotoRecord(
    val id: String,
    val dayKey: String,
    val dateLabel: String,     // "10/24"
    val timeMin: Int,
    val locId: String,
    val inside: Boolean,
    val weatherId: String,
    val appearance: Map<String, String>,
    val figures: List<PhotoFigure>,   // player first, then posers, bombers last
    val refused: List<String> = emptyList(),
    val caption: String = "",
    var followersGained: Int = 0,
    val seed: Long = 0L,
    /** v0.3.0e: the absDay the shutter fired. The album
     *  re-renders photos from records, and the moon over a
     *  photo is a function of the day it was TAKEN — without
     *  the ordinal, an old photo can't remember its own sky.
     *  Additive: records from before this field load with -1
     *  and PhotoView falls back to a plain moon for them. */
    val absDay: Int = -1,
    /** v0.4.0c: the group composition ("" for a classic
     *  selfie; lineup | squad | stacked for a posed group
     *  frame). Additive like [absDay]: every record from
     *  before this field loads "" and renders exactly as it
     *  always did. Group records write their figures in
     *  frame order — the player stands in their slot, not
     *  necessarily first. */
    val composition: String = "",
    /** v0.6.0a: the commissioned-print stamp — the asker
     *  and the ask this photo landed, written once by
     *  Commissions.complete at shutter time. Additive:
     *  uncommissioned photos (and every record from before
     *  this field) carry "" and render exactly as before.
     *  A stamped print never fades from the album (see
     *  applyPhoto's cap). Vars, like [followersGained]:
     *  the stamp lands after the record is composed. */
    var commissionAsker: String = "",
    var commissionText: String = "",
    /** v0.8.0d: the player's yearbook star — a per-record
     *  curation marker set from the Album. Starred records
     *  earn Yearbook.STAR_BONUS in every category they enter
     *  and never fade from the album (see applyPhoto's cap).
     *  Additive like the stamp: every record from before
     *  this field loads unstarred. A var, like the stamp:
     *  the player sets it after the record exists. */
    var starred: Boolean = false
) {
    /** True for a commissioned print (v0.6.0a). */
    val isCommissioned: Boolean get() = commissionAsker.isNotEmpty()
    /** Everyone in the frame except you (bombers included). */
    val npcs: List<PhotoFigure> get() = figures.filter { it.kind != "player" }

    /** True for a posed group frame (v0.4.0c). */
    val isGroup: Boolean get() = composition.isNotEmpty()

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "dayKey" to dayKey, "dateLabel" to dateLabel,
        "timeMin" to timeMin, "locId" to locId, "inside" to inside,
        "weatherId" to weatherId, "appearance" to appearance,
        "figures" to figures.map { it.toMap() }, "refused" to refused,
        "caption" to caption, "followersGained" to followersGained,
        "seed" to seed, "absDay" to absDay, "composition" to composition,
        "commissionAsker" to commissionAsker,
        "commissionText" to commissionText,
        "starred" to starred
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(m: Map<String, Any?>): PhotoRecord? {
            val id = m["id"]?.toString() ?: return null
            val app = (m["appearance"] as? Map<String, Any?>)
                ?.mapValues { it.value.toString() } ?: emptyMap()
            val figs = (m["figures"] as? List<Any?>)?.mapNotNull {
                (it as? Map<String, Any?>)?.let { fm -> PhotoFigure.fromMap(fm) }
            } ?: emptyList()
            return PhotoRecord(
                id = id,
                dayKey = m["dayKey"]?.toString() ?: "",
                dateLabel = m["dateLabel"]?.toString() ?: "",
                timeMin = (m["timeMin"] as? Number)?.toInt() ?: 480,
                locId = m["locId"]?.toString() ?: "BusStop",
                inside = m["inside"] as? Boolean ?: false,
                weatherId = m["weatherId"]?.toString() ?: "light_snow",
                appearance = validateAppearance(app),
                figures = figs,
                refused = (m["refused"] as? List<Any?>)?.map { it.toString() }
                    ?: emptyList(),
                caption = m["caption"]?.toString() ?: "",
                followersGained = (m["followersGained"] as? Number)?.toInt() ?: 0,
                seed = (m["seed"] as? Number)?.toLong() ?: 0L,
                absDay = (m["absDay"] as? Number)?.toInt() ?: -1,
                composition = m["composition"]?.toString() ?: "",
                commissionAsker = m["commissionAsker"]?.toString() ?: "",
                commissionText = m["commissionText"]?.toString() ?: "",
                starred = m["starred"] as? Boolean ?: false
            )
        }
    }
}

/** What applying a photo to the town produced — the Activity
 *  turns this into toasts and ticker lines. */
data class PhotoResult(
    val gained: Int,
    val milestone: Int?,
    val newFeats: List<String>,       // feat ids completed by this photo
    val goalsDone: List<String>,      // NPC names whose PHOTO goal this completed
    val participationCounted: Boolean,
    /** v0.4.0c: the chemistry bonus inside [gained] (group
     *  frames only) — split out so the UI can brag about it. */
    val chemistryBonus: Int = 0
)

object Photos {

    /** Album cap: the Journal keeps the newest [ALBUM_CAP]
     *  photos; older ones fade out of the album (the follower
     *  count and photographed tallies they earned stay). */
    const val ALBUM_CAP = 60

    val MILESTONES = listOf(10, 25, 50, 100, 250, 500, 1000, 2500)

    // reaction names
    const val GOOFY = "goofy"
    const val HAPPY = "happy"
    const val NEUTRAL = "neutral"
    const val REFUSE = "refuse"
    const val PHOTOBOMB = "bomb"

    /** Follower base by town prominence: cast > residents >
     *  visitors. A re-photo of the same person trickles +1. */
    fun baseFor(kind: String): Int = when (kind) {
        "cast" -> 12
        "resident" -> 6
        else -> 4
    }

    /** First-photo payout for one figure, from the record's
     *  own snapshot: base x tier scale. Bombers pay half —
     *  they crashed the shot, but they ARE in it. */
    fun uniqueGain(fig: PhotoFigure): Int {
        val base = baseFor(fig.kind)
        if (fig.reaction == PHOTOBOMB) return (base * 0.5f).roundToInt()
        val mult = when (Relationships.tierOf(fig.points)) {
            Relationships.CLOSE_FRIEND -> 2.0f
            Relationships.FRIEND -> 1.5f
            Relationships.ACQUAINTANCE -> 1.0f
            Relationships.STRANGER -> 0.75f
            else -> 0.5f
        }
        return (base * mult).roundToInt()
    }

    const val REPHOTO_GAIN = 1

    // ---------------- group frames (v0.4.0c) ----------------

    /** Compositions a group record can carry. "" (no
     *  composition) is a classic selfie, forever. */
    const val COMP_LINEUP = "lineup"     // everybody, one front row
    const val COMP_SQUAD = "squad"       // 2 back / 2 front
    const val COMP_STACKED = "stacked"   // the pile (kids, high chemistry)

    /** A posed frame holds at most this many townsfolk —
     *  plus you, plus a crasher who wasn't invited. The one
     *  exception: the core four (see SQUAD) get all four
     *  slots when the frame is exactly them. */
    const val GROUP_MAX_NPCS = 3

    /** Group rate: a first photo in a group frame pays the
     *  per-person solo gain x this. Re-photos trickle +1
     *  as ever, group or not. */
    const val GROUP_RATE = 0.6f

    /** Followers bonus per recognized pair (PAIR_SCRIPTS
     *  registry) whose BOTH members make their photo debut
     *  in this frame — capped so a debutante ball of famous
     *  couples can't mint fame out of proportion. */
    const val CHEMISTRY_PAIR_BONUS = 10
    const val CHEMISTRY_PAIRS_CAPPED = 2

    /** Pair chemistry = the pair's npcBonds value, plus a
     *  recognition bump for pairs the town already knows
     *  as a unit. HIGH_CHEMISTRY is the "these two are a
     *  THING" line — the same bond >= 4 the scene renderer
     *  leaks hearts at. RIVAL_BOND mirrors its storm line. */
    const val RECOGNIZED_CHEMISTRY = 2
    const val HIGH_CHEMISTRY = 4
    const val RIVAL_BOND = -3

    /** Lifetime group shots the SAY CHEESE feat asks for. */
    const val GROUP_SHOTS_FEAT = 25

    // ---------------- group layout geometry (v0.4.0g) ----------------

    /** A figure's ink half-width as a fraction of its drawn
     *  height, from the SAME derived geometry KidArt draws:
     *  the wider of the head ellipse's right edge and the
     *  silhouette's half span, measured from the center,
     *  times the spec's silhouette width scale, over the
     *  body plan's unit box height. Conservative by
     *  construction — arm capsules and the ground shadow
     *  live inside the silhouette span, hair and hats
     *  inside the head's span. */
    fun halfWidthFrac(spec: SheetSpec): Float {
        val d = FigureGeometry.derive(spec)
        val edge = maxOf(d.hx + d.hrx, 50f + d.silhouetteW / 2f)
        return (edge - 50f) * spec.width / d.plan.boxH
    }

    /** The group layouts' geometry, extracted from PhotoView
     *  (v0.4.0g) so the suite can MEASURE it instead of
     *  trusting the paper. All fractions are of the photo
     *  rect: x in width-fractions, y and heights in
     *  height-fractions. A row's height is capped three
     *  ways: the composition's paper cap, the slot cap (a
     *  figure is at most SLOT_H_FACTOR tall per unit of
     *  slot width), and the HEADROOM cap — the row's
     *  tallest member (by height scale), plus INK_OVERHEAD
     *  for the bunny ears and hat crowns that ride above
     *  the nominal head top, must still fit under the
     *  frame's top edge with HEAD_MARGIN to spare. On the
     *  portrait rects the game mostly draws, the slot cap
     *  binds first and headroom changes nothing; on wide
     *  album cells it is the difference between a head and
     *  a clipped head. (v0.4.0c shipped the first two caps
     *  only — wide cells could shave a tall back-row head.) */
    object GroupGeom {
        const val GROUND = 0.955f          // feet line (photo bottom - 0.045)
        const val FRONT_MAX_H = 0.80f      // front rows, all compositions
        const val SQUAD_BACK_MAX_H = 0.60f
        const val STACKED_BACK_MAX_H = 0.66f
        const val SQUAD_LIFT = 0.17f       // back feet above the ground line
        const val STACKED_LIFT = 0.30f
        const val EDGE = 0.03f             // full rows span [EDGE, 1-EDGE]
        const val SQUAD_INSET = 0.10f      // squad back row span inset
        const val STACKED_INSET = 0.19f    // stacked back row span inset
        const val SLOT_H_FACTOR = 1.18f    // height <= slot width x this
        const val INK_OVERHEAD = 1.05f     // ears/crowns above the head top
        const val HEAD_MARGIN = 0.005f     // daylight under the frame top

        /** One figure's layout inputs: its on-screen height
         *  scale (PhotoView's figScale) and its ink
         *  half-width as a fraction of its drawn height
         *  ([halfWidthFrac]), plus whether it poses
         *  backToBack. */
        data class Fig(val scale: Float, val halfWFrac: Float,
                       val backToBack: Boolean = false)

        /** Where one figure stands: [index] back into the
         *  input list, center x (width-fraction), feet line
         *  and row height (height-fractions — the figure's
         *  own height is h x its scale), and whether it is
         *  the mirrored half of a back-to-back pair. The
         *  list comes back in DRAW order (back rows first),
         *  exactly as PhotoView paints it. */
        data class Placed(val index: Int, val cx: Float, val feet: Float,
                          val h: Float, val flipped: Boolean)

        private fun row(figs: List<Fig>, from: Int, count: Int,
                        x0: Float, x1: Float, feet: Float, maxH: Float,
                        aspect: Float): List<Placed> {
            if (count <= 0) return emptyList()
            val slotW = (x1 - x0) / count
            var h = minOf(maxH, slotW * SLOT_H_FACTOR * aspect)
            val maxScale = (from until from + count).maxOf { figs[it].scale }
            h = minOf(h, (feet - HEAD_MARGIN) / (INK_OVERHEAD * maxScale))
            return (0 until count).map { i ->
                val fig = figs[from + i]
                val flipped = fig.backToBack && i + 1 < count &&
                    figs[from + i + 1].backToBack
                Placed(from + i, x0 + slotW * (i + 0.5f), feet, h, flipped)
            }
        }

        /** The full layout for [composition] over [figs] in
         *  frame order (crasher excluded — it leans in over
         *  the frame by design and stays PhotoView's own
         *  business), for a photo rect whose width/height =
         *  [aspect]. Row-for-row PhotoView's group layouts:
         *  SQUAD's back row is the FIRST two in frame order;
         *  STACKED's back row is the pile's back half (frame
         *  order minus the front two). */
        fun layout(composition: String, figs: List<Fig>,
                   aspect: Float): List<Placed> {
            if (figs.isEmpty()) return emptyList()
            val a = aspect.coerceAtLeast(0.05f)
            return when (composition) {
                COMP_SQUAD -> {
                    val back = minOf(2, figs.size)
                    row(figs, 0, back, SQUAD_INSET, 1f - SQUAD_INSET,
                        GROUND - SQUAD_LIFT, SQUAD_BACK_MAX_H, a) +
                        row(figs, back, figs.size - back, EDGE, 1f - EDGE,
                            GROUND, FRONT_MAX_H, a)
                }
                COMP_STACKED -> {
                    val front = minOf(2, figs.size)
                    row(figs, front, figs.size - front,
                        STACKED_INSET, 1f - STACKED_INSET,
                        GROUND - STACKED_LIFT, STACKED_BACK_MAX_H, a) +
                        row(figs, 0, front, EDGE, 1f - EDGE,
                            GROUND, FRONT_MAX_H, a)
                }
                else -> row(figs, 0, figs.size, EDGE, 1f - EDGE,
                    GROUND, FRONT_MAX_H, a)
            }
        }
    }

    /** The highest milestone crossed going [old] -> [new]. */
    fun crossedMilestone(old: Int, new: Int): Int? =
        MILESTONES.filter { it > old && it <= new }.maxOrNull()

    /** The badge the Journal wears at [followers]: highest
     *  milestone reached, or null before the first. */
    fun badgeFor(followers: Int): Int? =
        MILESTONES.filter { it <= followers }.maxOrNull()

    fun nextMilestone(followers: Int): Int? =
        MILESTONES.firstOrNull { it > followers }

    // ---------------- reactions & poses ----------------

    /** Deterministic reaction for one NPC: tier decides, and
     *  for rivals a stable hash decides refuse vs photobomb
     *  (1 in 4 bomb). Same (name, day, count) = same answer,
     *  on every screen and every run. */
    fun reactionFor(name: String, points: Int, seedKey: String): String {
        return when (Relationships.tierOf(points)) {
            Relationships.CLOSE_FRIEND -> GOOFY
            Relationships.FRIEND -> HAPPY
            Relationships.ACQUAINTANCE, Relationships.STRANGER -> NEUTRAL
            else -> { // RIVAL
                val h = (name.hashCode().toLong() * 31 + seedKey.hashCode()) and 0x7fffffff
                if (h % 4L == 0L) PHOTOBOMB else REFUSE
            }
        }
    }

    private val GOOFY_POSES = listOf("bunny", "tongue", "swap")

    /** The goofy pose a close friend strikes. "swap" is only
     *  provisional here — compose() pairs swappers up and an
     *  unpaired swap falls back to bunny ears. */
    fun goofyPoseFor(name: String, seed: Long): String {
        val idx = ((name.hashCode().toLong() + seed) and 0x7fffffff) % GOOFY_POSES.size
        return GOOFY_POSES[idx.toInt()]
    }

    fun moodFor(reaction: String, pose: String): String = when {
        reaction == PHOTOBOMB -> "happy"
        pose == "tongue" -> "goofy"
        reaction == GOOFY || reaction == HAPPY -> "happy"
        else -> "idle"
    }

    // ---------------- compose ----------------

    /** The relationship points the selfie math reads: cast
     *  live in the friendship map, generated folks in their
     *  memory doc — the same split as everywhere else. */
    fun pointsFor(state: GameState, name: String): Int =
        state.friendship[name] ?: NpcMemoryStore.get(name)?.points ?: 0

    // ---------------- pair chemistry (v0.4.0c) ----------------
    // One read of the town's pair ledgers, shared by the
    // group composer, the payout math and the feats: bonds
    // live in state.npcBonds (Threads.pairKey), canon
    // relations in NPC_RELATIONS, and the recognized-pair
    // registry is PAIR_SCRIPTS — the same sources Stories
    // and the scene renderer read. No second chemistry.

    /** The pair's recorded bond (-5..10), 0 if they've
     *  never moved it. */
    fun bondBetween(state: GameState, a: String, b: String): Int =
        state.npcBonds[Threads.pairKey(a, b)] ?: 0

    /** A recognized pair: the town has pair scripts for
     *  them — Craig+Tweek, Stan+Wendy, Jimbo+Ned... */
    fun isRecognizedPair(a: String, b: String): Boolean =
        setOf(a, b) in PAIR_SCRIPTS

    /** Every recognized pair fully inside [names], in a
     *  stable order (by pair key) so payouts and captions
     *  are reproducible from the record's names alone. */
    fun recognizedPairsIn(names: Collection<String>): List<Pair<String, String>> {
        val have = names.toSet()
        return PAIR_SCRIPTS.keys
            .filter { it.size == 2 && it.all { n -> n in have } }
            .map { val s = it.sorted(); s[0] to s[1] }
            .sortedBy { Threads.pairKey(it.first, it.second) }
    }

    /** Mutual rivals: canon arch-rivals, or a bond at the
     *  storm line the scene renderer uses (<= -3). */
    fun isRivalPair(state: GameState, a: String, b: String): Boolean =
        relationNote(a, b) == "arch-rivals" ||
            bondBetween(state, a, b) <= RIVAL_BOND

    /** The one chemistry number: bond + recognition. */
    fun chemistryBetween(state: GameState, a: String, b: String): Int =
        bondBetween(state, a, b) +
            if (isRecognizedPair(a, b)) RECOGNIZED_CHEMISTRY else 0

    /** Any pair in the group over the high-chemistry line. */
    fun hasHighChemistry(state: GameState, names: List<String>): Boolean {
        for (i in names.indices) for (j in i + 1 until names.size)
            if (chemistryBetween(state, names[i], names[j]) >= HIGH_CHEMISTRY)
                return true
        return false
    }

    /** One would-be figure mid-compose: name, prominence
     *  kind, the reaction the shutter will get, and the
     *  relationship points it was computed from. */
    private data class Draft(val name: String, val kind: String,
                             val reaction: String, val points: Int)

    /** The shutter seed for shot #[shotNo]: the lifetime
     *  selfie counter keeps ids/seeds unique even after the
     *  album cap starts fading old photos. */
    private fun shotSeed(state: GameState, shotNo: Int): Long =
        dailySeed(state.todayKey) + state.timeMin * 131L + shotNo * 7919L

    private fun shotSeedKey(state: GameState, shotNo: Int): String =
        "${state.todayKey}:${state.timeMin}:$shotNo"

    private fun dateLabelFor(state: GameState): String =
        if (state.todayKey.length == 8)
            "${state.todayKey.substring(4, 6).toInt()}/${state.todayKey.substring(6, 8).toInt()}"
        else ""

    /** Everyone present, drafted in presence order (cast
     *  first, then generated) with their reactions rolled —
     *  the shared front half of compose and composeGroup. */
    private fun buildDrafts(state: GameState, castPresent: List<String>,
                            genPresent: List<VisitorPersona>,
                            seedKey: String): List<Draft> {
        val drafts = mutableListOf<Draft>()
        for (n in castPresent) {
            val pts = pointsFor(state, n)
            drafts.add(Draft(n, "cast", reactionFor(n, pts, seedKey), pts))
        }
        for (p in genPresent) {
            val pts = pointsFor(state, p.loreName)
            val kind = if (p.resident) "resident" else "visitor"
            drafts.add(Draft(p.loreName, kind,
                reactionFor(p.loreName, pts, seedKey), pts))
        }
        return drafts
    }

    /** Goofy pose assignment over [candidates] (draft
     *  order): bunny/tongue/swap per goofy friend, swappers
     *  paired consecutively, an unpaired swap falls back
     *  to bunny ears. [skip] names abstain entirely (the
     *  back-to-back pair in a group frame). Returns
     *  (poseOf, swapOf). */
    private fun goofyAssignments(candidates: List<Draft>, seed: Long,
                                 skip: Set<String>)
            : Pair<Map<String, String>, Map<String, String>> {
        val poseOf = mutableMapOf<String, String>()
        val swapOf = mutableMapOf<String, String>()
        val goofies = candidates.filter {
            it.reaction == GOOFY && it.name !in skip
        }
        for (d in goofies) poseOf[d.name] = goofyPoseFor(d.name, seed)
        val swappers = goofies.filter { poseOf[it.name] == "swap" }
        var i = 0
        while (i + 1 < swappers.size) {
            swapOf[swappers[i].name] = swappers[i + 1].name
            swapOf[swappers[i + 1].name] = swappers[i].name
            i += 2
        }
        for (d in swappers) {
            if (d.name !in swapOf) poseOf[d.name] = "bunny"
        }
        return poseOf to swapOf
    }

    /** Build the photo record for the shutter click. Pure:
     *  reactions, poses and the caption are all derived here;
     *  follower application is applyPhoto's job. */
    fun compose(state: GameState, castPresent: List<String>,
                genPresent: List<VisitorPersona>, playerName: String,
                appearance: Map<String, String>): PhotoRecord {
        val me = state.agents.values.firstOrNull { it.isPlayer }
        val locId = me?.location ?: "BusStop"
        val shotNo = state.life["selfies"] ?: 0
        val seed = shotSeed(state, shotNo)
        val seedKey = shotSeedKey(state, shotNo)
        val app = validateAppearance(appearance)
        val drafts = buildDrafts(state, castPresent, genPresent, seedKey)
        val refused = drafts.filter { it.reaction == REFUSE }.map { it.name }
        val posers = drafts.filter { it.reaction != REFUSE }
        val (poseOf, swapOf) = goofyAssignments(posers, seed, emptySet())

        val figures = mutableListOf<PhotoFigure>()
        figures.add(PhotoFigure(playerName, "player", "happy",
            reaction = "player", points = 0))
        for (d in posers.filter { it.reaction != PHOTOBOMB }) {
            val pose = poseOf[d.name] ?: "none"
            figures.add(PhotoFigure(d.name, d.kind,
                moodFor(d.reaction, pose), pose,
                swapWith = swapOf[d.name],
                reaction = d.reaction, points = d.points))
        }
        for (d in posers.filter { it.reaction == PHOTOBOMB }) {
            figures.add(PhotoFigure(d.name, d.kind, "happy", "bomb",
                reaction = PHOTOBOMB, points = d.points))
        }

        val record = PhotoRecord(
            id = "p${state.absDay}-${state.timeMin}-$shotNo",
            dayKey = state.todayKey,
            dateLabel = dateLabelFor(state),
            timeMin = state.timeMin,
            locId = locId,
            inside = state.playerInside,
            weatherId = state.weatherId,
            appearance = app,
            figures = stampSleepwear(state, stampHero(state, figures), locId),
            refused = refused,
            seed = seed,
            absDay = state.absDay
        )
        return record.copy(caption = captionFor(record))
    }

    // ---------------- compose: group frames (v0.4.0c) ----------------

    /** All rival pairs inside [names] (canon or bond-made). */
    fun rivalPairsOf(state: GameState, names: List<String>)
            : List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        for (i in names.indices) for (j in i + 1 until names.size)
            if (isRivalPair(state, names[i], names[j]))
                out.add(names[i] to names[j])
        return out
    }

    private fun permutations(items: List<String>): List<List<String>> {
        if (items.size <= 1) return listOf(items)
        val out = mutableListOf<List<String>>()
        for (i in items.indices) {
            val rest = items.toMutableList().also { it.removeAt(i) }
            for (tail in permutations(rest)) out.add(listOf(items[i]) + tail)
        }
        return out
    }

    /** Arrange a group frame: the NPC order + the player's
     *  slot in it. Every permutation of the (<= 4) members
     *  is scored on its adjacencies — chemistry adds, rival
     *  adjacency costs heavily, the back-to-back pair (when
     *  one was chosen) MUST stand together and glues hard —
     *  and ties break to the lexicographically first order,
     *  so the same town moment always frames the same way.
     *  The player then takes the most central slot that
     *  splits neither the back-to-back block nor a
     *  high-chemistry pair: you never stand between a
     *  couple and their moment. */
    fun frameOrder(state: GameState, members: List<String>,
                   backPair: Pair<String, String>?): Pair<List<String>, Int> {
        if (members.isEmpty()) return emptyList<String>() to 0
        val backSet = backPair?.let { setOf(it.first, it.second) }
        fun adjacencyScore(order: List<String>): Int {
            var s = 0
            for (i in 0 until order.size - 1) {
                val a = order[i]; val b = order[i + 1]
                s += when {
                    backSet != null && setOf(a, b) == backSet -> 40
                    isRivalPair(state, a, b) -> -100
                    else -> chemistryBetween(state, a, b)
                }
            }
            return s
        }
        val orders = permutations(members).filter { order ->
            backSet == null || order.zipWithNext().any { (a, b) ->
                setOf(a, b) == backSet
            }
        }
        val best = orders.minWithOrNull(
            compareByDescending<List<String>> { adjacencyScore(it) }
                .thenBy { it.joinToString(",") }) ?: members
        val n = best.size
        val slots = (0..n).filter { pos ->
            pos == 0 || pos == n || run {
                val a = best[pos - 1]; val b = best[pos]
                !(backSet != null && setOf(a, b) == backSet) &&
                    chemistryBetween(state, a, b) < HIGH_CHEMISTRY
            }
        }
        val slot = slots.minWithOrNull(
            compareBy({ kotlin.math.abs(2 * it - n) }, { it })) ?: 0
        return best to slot
    }

    /** The composition a selected group earns: modest
     *  frames and the core-four exception line up; a trio
     *  squads up — unless they're all kids running hot on
     *  chemistry, in which case it's a pile. */
    fun compositionFor(state: GameState, memberNames: List<String>,
                       bandByName: Map<String, AgeBand>): String = when {
        memberNames.size != 3 -> COMP_LINEUP
        memberNames.all {
            bandByName[it] == AgeBand.KID || bandByName[it] == AgeBand.YOUNG_TEEN
        } && hasHighChemistry(state, memberNames) -> COMP_STACKED
        else -> COMP_SQUAD
    }

    /** Build a GROUP record: up to [GROUP_MAX_NPCS] of the
     *  present townsfolk, chosen by how much they like you
     *  (the core four take all four slots when they're
     *  exactly the frame), arranged by chemistry, plus one
     *  crasher if a rival rolled the bomb. Refusers are
     *  noted in the caption, as ever. Pure, like compose. */
    fun composeGroup(state: GameState, castPresent: List<String>,
                     genPresent: List<VisitorPersona>, playerName: String,
                     appearance: Map<String, String>): PhotoRecord {
        val me = state.agents.values.firstOrNull { it.isPlayer }
        val locId = me?.location ?: "BusStop"
        val shotNo = state.life["selfies"] ?: 0
        val seed = shotSeed(state, shotNo)
        val seedKey = shotSeedKey(state, shotNo)
        val app = validateAppearance(appearance)
        val drafts = buildDrafts(state, castPresent, genPresent, seedKey)
        val refused = drafts.filter { it.reaction == REFUSE }.map { it.name }
        // a bomb in a group frame is a CRASHER: exactly one
        // uninvited rival squeezes in; the rest never made it
        val crasher = drafts.firstOrNull { it.reaction == PHOTOBOMB }
        val posers = drafts.filter {
            it.reaction != REFUSE && it.reaction != PHOTOBOMB
        }
        // the frame: the core four take all four slots when
        // all four are here and willing; otherwise the three
        // who like you most (name breaks ties)
        val coreHere = SQUAD.filter { n -> posers.any { it.name == n } }
        val selected: List<Draft> = if (coreHere.size == SQUAD.size)
            posers.filter { it.name in SQUAD }
        else posers.sortedWith(
            compareByDescending<Draft> { it.points }.thenBy { it.name })
            .take(GROUP_MAX_NPCS)
        val memberNames = selected.map { it.name }
        val byName = selected.associateBy { it.name }

        // the back-to-back bit: the strongest mutual rivalry
        // in frame (lowest bond; pair key breaks ties) poses
        // together, backs turned — everyone else keeps gaps
        val backPair = rivalPairsOf(state, memberNames).minWithOrNull(
            compareBy({ bondBetween(state, it.first, it.second) },
                { Threads.pairKey(it.first, it.second) }))
        val backNames = backPair?.let { setOf(it.first, it.second) }
            ?: emptySet()

        val (poseOf, swapOf) = goofyAssignments(
            posers.filter { it.name in memberNames }, seed, backNames)
        val (npcOrder, playerIdx) = frameOrder(state, memberNames, backPair)

        fun npcFigure(d: Draft): PhotoFigure =
            if (d.name in backNames)
                PhotoFigure(d.name, d.kind, "hurt", "backToBack",
                    reaction = d.reaction, points = d.points)
            else {
                val pose = poseOf[d.name] ?: "none"
                PhotoFigure(d.name, d.kind, moodFor(d.reaction, pose), pose,
                    swapWith = swapOf[d.name],
                    reaction = d.reaction, points = d.points)
            }
        val playerFig = PhotoFigure(playerName, "player", "happy",
            reaction = "player", points = 0)
        val figures = mutableListOf<PhotoFigure>()
        npcOrder.forEachIndexed { i, n ->
            if (i == playerIdx) figures.add(playerFig)
            figures.add(npcFigure(byName.getValue(n)))
        }
        if (playerIdx >= npcOrder.size) figures.add(playerFig)
        if (crasher != null) {
            figures.add(PhotoFigure(crasher.name, crasher.kind, "happy",
                "bomb", reaction = PHOTOBOMB, points = crasher.points))
        }

        val bandByName = HashMap<String, AgeBand>()
        for (n in castPresent) bandByName[n] = CharacterSystem.bandOf(n)
        for (p in genPresent) bandByName[p.loreName] = p.band

        val record = PhotoRecord(
            id = "p${state.absDay}-${state.timeMin}-$shotNo",
            dayKey = state.todayKey,
            dateLabel = dateLabelFor(state),
            timeMin = state.timeMin,
            locId = locId,
            inside = state.playerInside,
            weatherId = state.weatherId,
            appearance = app,
            figures = stampSleepwear(state, stampHero(state, figures), locId),
            refused = refused,
            seed = seed,
            absDay = state.absDay,
            composition = compositionFor(state, memberNames, bandByName)
        )
        return record.copy(caption = captionFor(record))
    }

    /** v0.10.0b: stamp hero costumes onto a freshly
     *  composed frame — the shutter catches the capes that
     *  are actually there (Heroes.figureCode reads the live
     *  moment), frozen on the figures forever. */
    private fun stampHero(state: GameState,
                          figs: List<PhotoFigure>): List<PhotoFigure> =
        figs.map { f ->
            val code = Heroes.figureCode(state, f)
            if (code.isEmpty() || code == f.hero) f
            else f.copy(hero = code)
        }

    /** v0.12.0d: stamp sleepwear onto a freshly composed
     *  frame, the stampHero shape pointed at bedtime —
     *  the shutter catches the pajamas that are actually
     *  there (Sleepwear.figureCode reads the live moment:
     *  your house, a sleepover standing, the evening open;
     *  only you and the night's kid guests can freeze),
     *  frozen on the figures forever. Any other frame
     *  freezes nothing. */
    private fun stampSleepwear(state: GameState,
                               figs: List<PhotoFigure>,
                               locId: String): List<PhotoFigure> =
        figs.map { f ->
            val code = Sleepwear.figureCode(state, f, locId)
            if (code.isEmpty() || code == f.sleep) f
            else f.copy(sleep = code)
        }

    /** "A", "A & B", "A, B & C" — the caption's name list. */
    private fun nameList(names: List<String>): String = when (names.size) {
        0 -> ""
        1 -> names[0]
        2 -> "${names[0]} & ${names[1]}"
        else -> "${names.dropLast(1).joinToString(", ")} & ${names.last()}"
    }

    /** The caption under the polaroid: who, where, and the
     *  shot's little story (a bomb, a snub, a goofy crowd).
     *  Group frames (v0.4.0c) get their own grammar — the
     *  caption names the GANG, not just the guest list. */
    fun captionFor(rec: PhotoRecord): String {
        if (rec.isGroup) return groupCaption(rec)
        val locName = LOCATIONS[rec.locId]?.name ?: rec.locId
        val posers = rec.npcs.filter { it.reaction != PHOTOBOMB }
        val bombers = rec.npcs.filter { it.reaction == PHOTOBOMB }
        val sb = StringBuilder()
        if (posers.isEmpty() && bombers.isEmpty()) {
            sb.append("Just you at $locName.")
            sb.append(" The algorithm provides. Probably.")
        } else {
            val names = posers.map { it.name }
            val who = when (names.size) {
                0 -> ""
                1 -> "You & ${names[0]}"
                2 -> "You, ${names[0]} & ${names[1]}"
                else -> "You, ${names.dropLast(1).joinToString(", ")} & ${names.last()}"
            }
            if (who.isNotEmpty()) sb.append("$who at $locName.")
            if (bombers.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append(" ")
                sb.append("${bombers.joinToString(" & ") { it.name }} " +
                    "photobombed it. Classic.")
            }
            if (posers.any { it.reaction == GOOFY }) {
                sb.append(" Everybody say 'cheese'!")
            }
        }
        if (rec.refused.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append(" ")
            sb.append("${rec.refused.joinToString(" & ")} said no. Rude.")
        }
        return sb.toString()
    }

    /** v0.4.0c: the group caption. Deterministic from the
     *  record: the whole gang gets named as the gang, a pile
     *  gets called a pile, the back-to-back pair gets told
     *  on, and a crasher gets told on harder. */
    private fun groupCaption(rec: PhotoRecord): String {
        val locName = LOCATIONS[rec.locId]?.name ?: rec.locId
        val posers = rec.npcs.filter { it.reaction != PHOTOBOMB }
        val crashers = rec.npcs.filter { it.reaction == PHOTOBOMB }
        val names = posers.map { it.name }
        val sb = StringBuilder()
        when {
            names.isEmpty() && crashers.isEmpty() ->
                sb.append("Just you at $locName. The group shot that wasn't.")
            names.isEmpty() ->
                sb.append("Just you at $locName.")
            SQUAD.all { it in names } ->
                sb.append("The whole gang at $locName.")
            rec.composition == COMP_STACKED ->
                sb.append("Dogpile at $locName — you, ${nameList(names)}. " +
                    "Gravity lost.")
            else ->
                sb.append("You & the gang at $locName: ${nameList(names)}.")
        }
        val backPair = posers.filter { it.pose == "backToBack" }
        if (backPair.size == 2) {
            sb.append(" ${backPair[0].name} & ${backPair[1].name} are NOT " +
                "posing together. (They are totally posing together.)")
        }
        if (crashers.isNotEmpty()) {
            sb.append(" ${nameList(crashers.map { it.name })} crashed it. Classic.")
        }
        if (posers.any { it.reaction == GOOFY }) {
            sb.append(" Everybody say 'cheese'!")
        }
        if (rec.refused.isNotEmpty()) {
            sb.append(" ${rec.refused.joinToString(" & ")} said no. Rude.")
        }
        return sb.toString()
    }

    // ---------------- apply ----------------

    /** File the photo: followers paid, tallies updated, album
     *  capped, PHOTO goals completed, feats checked, one
     *  participation point per event, memory traces left.
     *  Returns everything the UI needs to celebrate with. */
    fun applyPhoto(state: GameState, rec: PhotoRecord): PhotoResult {
        // ---- followers ----
        // v0.4.0c: a group frame pays each first-timer at
        // the GROUP RATE (their solo gain x GROUP_RATE); a
        // crasher's half-pay halves first, then rates. The
        // chemistry bonus lands below, once the tallies say
        // who made their debut in this very frame.
        var gained = 0
        for (fig in rec.npcs) {
            val seen = state.photographed[fig.name] ?: 0
            gained += when {
                seen > 0 -> REPHOTO_GAIN
                rec.isGroup -> (uniqueGain(fig) * GROUP_RATE).roundToInt()
                else -> uniqueGain(fig)
            }
            state.photographed[fig.name] = seen + 1
        }
        var chemistryBonus = 0
        if (rec.isGroup) {
            val debutPairs = recognizedPairsIn(rec.npcs.map { it.name })
                .count { (a, b) ->
                    state.photographed[a] == 1 && state.photographed[b] == 1
                }
            chemistryBonus = CHEMISTRY_PAIR_BONUS *
                minOf(CHEMISTRY_PAIRS_CAPPED, debutPairs)
            gained += chemistryBonus
        }
        rec.followersGained = gained
        val oldFollowers = state.followers
        state.followers += gained
        val milestone = crossedMilestone(oldFollowers, state.followers)
        if (milestone != null) {
            state.events.add("📸 $milestone FOLLOWERS! The algorithm provides. " +
                "You are officially A Thing in this town.")
            while (state.events.size > 40) state.events.removeAt(0)
            townLogLine(state, "📸 Hit $milestone selfie followers")
            // v0.8.0e: an armed follower race notes the
            // crossing day here — the settle itself waits
            // for the rollover, so a photo-finish is judged
            // by the day, never by whose code ran first
            Rival.onFollowers(state)
        }

        // ---- album (newest at the end; the cap fades the oldest) ----
        // v0.6.0a: a COMMISSIONED PRINT never fades — the cap
        // sheds the oldest uncommissioned photo first, and a
        // shelf of nothing but prints simply holds them all.
        // v0.8.0d: a STARRED photo never fades either — the
        // player's yearbook candidate is a keeper by the same
        // logic as a print.
        state.photos.add(rec)
        while (state.photos.size > ALBUM_CAP) {
            // never the record being filed (last) — its own
            // stamp lands later in this call
            val fadeIdx = (0 until state.photos.size - 1)
                .firstOrNull { !state.photos[it].isCommissioned &&
                    !state.photos[it].starred } ?: -1
            if (fadeIdx < 0) break
            state.photos.removeAt(fadeIdx)
        }
        state.life["selfies"] = (state.life["selfies"] ?: 0) + 1
        if (rec.isGroup)
            state.life["groupShots"] = (state.life["groupShots"] ?: 0) + 1

        // ---- memory traces ----
        val locName = LOCATIONS[rec.locId]?.name ?: rec.locId
        val whoBit = if (rec.npcs.isEmpty()) "solo"
            else "with ${rec.npcs.joinToString(", ") { it.name }}"
        state.agents.values.firstOrNull { it.isPlayer }?.memories
            ?.add("📸 Took a selfie at $locName, $whoBit.")
        for (fig in rec.npcs) {
            state.agents[fig.name]?.memories?.add("📸 Was in a selfie at $locName.")
            if (fig.name !in state.agents) {
                NpcMemoryStore.getOrCreate(fig.name).eventNote =
                    "Was in a selfie at $locName with the new kid."
            }
        }

        // ---- goal tie-in: a PHOTO goal completes on shutter ----
        val goalsDone = mutableListOf<String>()
        for (fig in rec.npcs) {
            if (NpcGoals.photoTick(state, fig.name)) goalsDone.add(fig.name)
            // v0.3.0c: a selfie with an arc person is a nudge too
            StoryArcs.noteInteraction(state, fig.name, "selfie")
        }

        // ---- v0.4.0b: a shutter click also settles PHOTO
        // errands (a named person in frame / the named place),
        // judged by the same record the feats read ----
        Errands.onPhoto(state, rec)

        // ---- v0.4.0d: and a holiday beat's portrait step (the
        // patient cow's sitting, the Line Feast's fridge shot),
        // judged by the same record ----
        HolidayBeats.onPhoto(state, rec)

        // ---- v0.5.0d: and an armed faction PHOTO beat (a claim
        // photo, a re-hanging, an ad shot), same record ----
        FactionMissions.onPhoto(state, rec)

        // ---- v0.8.0b: and an armed capstone PHOTO beat (the
        // premiere still — which counts only while the house
        // is live; Capstones owns that condition) ----
        Capstones.onPhoto(state, rec)

        // ---- v0.5.0h: and a season finale's portrait beat
        // (the Regatta's official fleet photo), same record ----
        Finales.onPhoto(state, rec)

        // ---- v0.6.0a: and any held photo commissions —
        // judged against this same record (subjects, place,
        // condition), stamped onto it if one lands. Runs
        // before the feats so the print feats below count
        // THIS shutter's landing ----
        Commissions.onPhoto(state, rec)

        // ---- feats (additions only; never gate content) ----
        val newFeats = checkFeats(state, rec)

        // ---- v0.5.0e: the phone hears about the shutter ----
        Phone.notePhoto(state, rec.npcs.firstOrNull()?.name ?: "", rec.locId)

        // ---- v0.9.0b: crew standing with you who are in
        // the frame tally a photo together (the frame's
        // chemistry above was this module's own — the crew
        // only counts) ----
        Crew.onPhoto(state, rec)

        // ---- v0.9.0e: an armed crew photo showdown
        // settles on a qualifying frame — at the turf,
        // group-sized, with fielded crew in it (the module
        // checks all three; every other frame is just a
        // photo) ----
        RivalCrew.onPhoto(state, rec)

        // ---- participation: once per event, at the cluster ----
        val counted = noteSelfieParticipation(state, rec.locId)

        return PhotoResult(gained, milestone, newFeats, goalsDone, counted,
            chemistryBonus)
    }

    /** A selfie at the active event's cluster location scores
     *  participation — once per event run, not per shutter. */
    fun noteSelfieParticipation(state: GameState, locId: String): Boolean {
        val id = state.shenaniganId ?: return false
        val key = "selfiePart:$id@${state.shenaniganDay}"
        if (state.lifeText[key] != null) return false
        val before = state.participation
        Engine().bumpParticipation(state, locId)
        if (state.participation == before) return false
        state.lifeText[key] = "1"
        return true
    }

    // ---------------- photo feats ----------------

    /** Famous pairs for the pair-shot feat (both in frame). */
    val PHOTO_PAIRS: List<Pair<String, String>> = listOf(
        "Stan" to "Kyle", "Craig" to "Tweek",
        "Jimmy" to "Timmy", "Wendy" to "Bebe"
    )

    /** The squad for the squad-shot feat. */
    val SQUAD = listOf("Stan", "Kyle", "Cartman", "Kenny")

    data class PhotoFeat(val id: String, val name: String, val desc: String)

    val PHOTO_FEATS: List<PhotoFeat> = listOf(
        PhotoFeat("squad_shot", "📸 The Whole Squad",
            "Get Stan, Kyle, Cartman & Kenny in one photo."),
        PhotoFeat("pair_shot", "📸 Famous Pair",
            "Photograph both members of a famous pair together."),
        PhotoFeat("full_house", "📸 Full House",
            "Fit 6 or more townsfolk in a single photo."),
        PhotoFeat("bombed", "📸 Photobombed!",
            "Have a photo crashed by someone who hates you."),
        PhotoFeat("holiday_shot", "📸 Holiday Spirit",
            "Take a photo while a holiday is on."),
        PhotoFeat("night_owl", "📸 Night Owls",
            "Take a photo after 9 PM or before 6 AM."),
        // v0.4.0c: the group-frame family — all keyed on the
        // record's composition, so a pile-in selfie, however
        // crowded, never earns them by accident.
        PhotoFeat("first_crowd", "📸 First Crowd",
            "Pose a group shot — 2+ townsfolk in one frame with you."),
        PhotoFeat("gangs_all_here", "📸 The Gang's All Here",
            "Get Stan, Kyle, Cartman & Kenny all in one group frame."),
        PhotoFeat("full_house_group", "📸 Full House!",
            "Squeeze 5+ figures into one group shot — crasher included."),
        PhotoFeat("say_cheese", "📸 Say Cheese",
            "Take 25 group shots."),
        // v0.6.0a: the commission family — keyed on the
        // lifetime print count (Commissions), so they land
        // on the very shutter that completes an ask.
        PhotoFeat("first_print", "📷 First Commission",
            "Land a commissioned print — somebody asked for that shot."),
        PhotoFeat("print_10", "📷 Town Photographer",
            "Land 10 commissioned prints."),
        PhotoFeat("print_25", "📷 The Fridge-Door Gallery",
            "Land 25 commissioned prints. Every fridge in town, covered.")
    )

    fun featById(id: String): PhotoFeat? = PHOTO_FEATS.firstOrNull { it.id == id }

    /** Does this photo complete feat [id] right now? Pure
     *  (the holiday check reads the state's current holiday). */
    fun featMet(state: GameState, rec: PhotoRecord, id: String): Boolean {
        val names = rec.npcs.map { it.name }.toSet()
        return when (id) {
            "squad_shot" -> SQUAD.all { it in names }
            "pair_shot" -> PHOTO_PAIRS.any { (a, b) -> a in names && b in names }
            "full_house" -> rec.npcs.size >= 6
            "bombed" -> rec.npcs.any { it.reaction == PHOTOBOMB }
            "holiday_shot" -> state.holidayId != null
            "night_owl" -> rec.timeMin >= 21 * 60 || rec.timeMin < 6 * 60
            "first_crowd" -> rec.isGroup && rec.npcs.size >= 2
            "gangs_all_here" -> rec.isGroup && SQUAD.all { it in names }
            "full_house_group" -> rec.isGroup && rec.figures.size >= 5 &&
                rec.npcs.any { it.reaction == PHOTOBOMB }
            "say_cheese" -> rec.isGroup &&
                (state.life["groupShots"] ?: 0) >= GROUP_SHOTS_FEAT
            "first_print" -> state.commissionsDone >= 1
            "print_10" -> state.commissionsDone >= 10
            "print_25" -> state.commissionsDone >= 25
            else -> false
        }
    }

    /** Complete any feats this photo earns; returns the newly
     *  completed feat ids (already-done feats stay done). */
    fun checkFeats(state: GameState, rec: PhotoRecord): List<String> {
        val done = mutableListOf<String>()
        for (f in PHOTO_FEATS) {
            if (f.id in state.photoFeats) continue
            if (featMet(state, rec, f.id)) {
                state.photoFeats.add(f.id)
                done.add(f.id)
                state.events.add("${f.name} — photo feat complete!")
                while (state.events.size > 40) state.events.removeAt(0)
                Phone.noteFeat(state, f.name)
            }
        }
        return done
    }

    /** A seeded random for callers that want one — compose is
     *  fully hash-driven, but the suite pins determinism here. */
    fun seedRandom(rec: PhotoRecord): Random = Random(rec.seed)
}

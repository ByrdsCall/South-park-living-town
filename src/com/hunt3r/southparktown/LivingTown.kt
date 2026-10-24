package com.hunt3r.southparktown

// ============================================================
// LivingTown — v0.16.0a, Lucky's BIG UPDATE (items 11–18).
//
// The living layer as DATA, so the suite can pin coverage
// for EVERY character and species instead of sampling:
//
//  - Expressions: a universal facial-expression registry
//    with three authored sets — CHILD, ADULT, ANIMAL —
//    each expression carrying BOTH its front-view geometry
//    and its side-profile geometry (items 14+15).
//  - Motion: per-character authored motion parameters on
//    the rig — gait, stride, bob, arm swing, gesture and
//    expression habits derived from the character's own
//    sheet + band + hand-authored overrides, never one
//    generic cycle recolored (item 16).
//  - Actions: the action-animation registry every feature
//    triggers (trash, phone, bus, conversation, sitting,
//    using objects…), and the player's blanket set
//    (items 16+17).
//  - Wander: deterministic, game-clock-keyed wander so
//    crowds spread out and keep moving instead of piling
//    onto one spot (items 11+12), and the schedule rule:
//    schedules read the GAME clock only (item 13).
//
// Pure Kotlin. The renderers (KidArt / PaperScene /
// GameView) read this; nothing here draws.
// ============================================================

/** Which expression family a face belongs to. Teens ride
 *  the child set while YOUNG_TEEN and the adult set from
 *  TEEN up — the three sets Lucky named stay the three
 *  sets; the band map is the only bridge. */
enum class FaceKind { CHILD, ADULT, ANIMAL }

object Expressions {

    /** One expression, both views. Front: lid closure
     *  (0 = wide open, 1 = shut), pupil offset in figure
     *  units, mouth curve (+ smile / − frown) and openness,
     *  brow tilt (+ raised / − lowered). Profile: the same
     *  read collapsed to one eye — pupil shift along the
     *  facing axis, lid, muzzle/mouth curve and openness,
     *  and the ear/antenna tilt animals and hats read by. */
    data class Expr(
        val id: String, val label: String,
        val lid: Float, val pupilX: Float, val pupilY: Float,
        val mouthCurve: Float, val mouthOpen: Float,
        val brow: Float,
        val profPupil: Float, val profMouthCurve: Float,
        val profMouthOpen: Float, val earTilt: Float
    )

    private fun e(id: String, label: String, lid: Float, px: Float, py: Float,
                  mc: Float, mo: Float, br: Float, pp: Float, pmc: Float,
                  pmo: Float, ear: Float) =
        Expr(id, label, lid, px, py, mc, mo, br, pp, pmc, pmo, ear)

    /** The CHILD set — big eyes, big swings. */
    val CHILD: List<Expr> = listOf(
        e("neutral", "Neutral", 0.05f, 0f, 0f, 0.05f, 0f, 0f, 0f, 0.05f, 0f, 0f),
        e("happy", "Happy", 0.25f, 0f, -0.4f, 0.85f, 0.15f, 0.4f, 0.2f, 0.8f, 0.1f, 0.2f),
        e("excited", "Excited", 0f, 0f, -0.6f, 1f, 0.7f, 0.8f, 0.4f, 1f, 0.6f, 0.5f),
        e("talk", "Talking", 0.1f, 0.3f, 0f, 0.3f, 0.65f, 0.2f, 0.5f, 0.3f, 0.6f, 0.1f),
        e("worried", "Worried", 0.15f, -0.5f, 0.3f, -0.45f, 0.2f, 0.7f, -0.4f, -0.4f, 0.2f, -0.3f),
        e("angry", "Angry", 0.45f, 0f, 0.2f, -0.7f, 0.25f, -0.9f, 0.1f, -0.6f, 0.3f, -0.5f),
        e("sad", "Sad", 0.55f, 0f, 0.6f, -0.8f, 0.05f, 0.5f, -0.2f, -0.7f, 0f, -0.4f),
        e("hurt", "Hurt", 1f, 0f, 0.4f, -0.6f, 0.35f, -0.4f, 0f, -0.5f, 0.3f, -0.6f),
        e("sleepy", "Sleepy", 0.9f, 0f, 0.5f, -0.1f, 0.3f, 0.1f, -0.3f, -0.1f, 0.25f, -0.5f),
        e("surprised", "Surprised", 0f, 0f, -0.8f, 0.1f, 0.9f, 1f, 0.6f, 0.1f, 0.85f, 0.7f),
        e("proud", "Proud", 0.35f, 0.2f, -0.3f, 0.6f, 0.05f, 0.3f, 0.3f, 0.55f, 0f, 0.35f),
        e("silly", "Silly", 0.7f, 0.6f, -0.2f, 0.9f, 0.55f, 0.6f, 0.7f, 0.85f, 0.5f, 0.6f)
    )

    /** The ADULT set — same grammar, smaller swings,
     *  heavier lids, wearier defaults. */
    val ADULT: List<Expr> = listOf(
        e("neutral", "Neutral", 0.12f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        e("pleased", "Pleased", 0.3f, 0f, -0.2f, 0.55f, 0.05f, 0.25f, 0.15f, 0.5f, 0f, 0.15f),
        e("talk", "Talking", 0.15f, 0.25f, 0f, 0.15f, 0.55f, 0.1f, 0.4f, 0.15f, 0.5f, 0.05f),
        e("stern", "Stern", 0.4f, 0f, 0.1f, -0.35f, 0f, -0.8f, 0.05f, -0.3f, 0f, -0.2f),
        e("worried", "Worried", 0.2f, -0.4f, 0.2f, -0.4f, 0.15f, 0.6f, -0.3f, -0.35f, 0.1f, -0.2f),
        e("angry", "Angry", 0.5f, 0f, 0.2f, -0.65f, 0.2f, -1f, 0.1f, -0.55f, 0.2f, -0.4f),
        e("tired", "Tired", 0.75f, 0f, 0.4f, -0.2f, 0.1f, 0.2f, -0.2f, -0.2f, 0.1f, -0.4f),
        e("sad", "Sad", 0.6f, 0f, 0.5f, -0.7f, 0f, 0.4f, -0.15f, -0.6f, 0f, -0.3f),
        e("hurt", "Hurt", 1f, 0f, 0.3f, -0.55f, 0.3f, -0.3f, 0f, -0.45f, 0.25f, -0.5f),
        e("surprised", "Surprised", 0f, 0f, -0.6f, 0f, 0.8f, 0.9f, 0.5f, 0f, 0.75f, 0.6f),
        e("amused", "Amused", 0.45f, 0.3f, -0.2f, 0.65f, 0.2f, 0.4f, 0.35f, 0.6f, 0.15f, 0.3f),
        e("resolute", "Resolute", 0.25f, 0.1f, 0f, 0.25f, 0f, -0.4f, 0.3f, 0.2f, 0f, 0.2f)
    )

    /** The ANIMAL set — read through eyes, ears and
     *  muzzle; no brows, so earTilt carries the emotion. */
    val ANIMAL: List<Expr> = listOf(
        e("calm", "Calm", 0.15f, 0f, 0f, 0.1f, 0f, 0f, 0f, 0.1f, 0f, 0.1f),
        e("alert", "Alert", 0f, 0f, -0.4f, 0f, 0.1f, 0f, 0.3f, 0f, 0.1f, 0.6f),
        e("happy", "Happy", 0.4f, 0f, -0.2f, 0.7f, 0.3f, 0f, 0.2f, 0.65f, 0.25f, 0.4f),
        e("curious", "Curious", 0.05f, 0.5f, -0.2f, 0.2f, 0.15f, 0f, 0.6f, 0.2f, 0.1f, 0.5f),
        e("startled", "Startled", 0f, 0f, -0.7f, -0.1f, 0.6f, 0f, 0.4f, -0.1f, 0.55f, -0.7f),
        e("sleepy", "Sleepy", 0.95f, 0f, 0.4f, 0f, 0.2f, 0f, -0.2f, 0f, 0.15f, -0.6f),
        e("hungry", "Hungry", 0.2f, 0f, 0.3f, 0.3f, 0.5f, 0f, 0.3f, 0.3f, 0.45f, 0.2f),
        e("hurt", "Hurt", 0.85f, 0f, 0.4f, -0.5f, 0.2f, 0f, 0f, -0.45f, 0.15f, -0.8f),
        e("wary", "Wary", 0.35f, -0.5f, 0.1f, -0.3f, 0f, 0f, -0.4f, -0.25f, 0f, -0.4f),
        e("playful", "Playful", 0.3f, 0.4f, -0.3f, 0.8f, 0.4f, 0f, 0.5f, 0.75f, 0.35f, 0.7f),
        e("proud", "Proud", 0.25f, 0.2f, -0.3f, 0.45f, 0f, 0f, 0.25f, 0.4f, 0f, 0.55f),
        e("talk", "Chittering", 0.1f, 0.3f, 0f, 0.2f, 0.7f, 0f, 0.45f, 0.2f, 0.65f, 0.3f)
    )

    fun setFor(kind: FaceKind): List<Expr> = when (kind) {
        FaceKind.CHILD -> CHILD
        FaceKind.ADULT -> ADULT
        FaceKind.ANIMAL -> ANIMAL
    }

    fun kindOf(band: AgeBand): FaceKind = when (band) {
        AgeBand.KID, AgeBand.YOUNG_TEEN -> FaceKind.CHILD
        else -> FaceKind.ADULT
    }

    /** Resolve a renderer mood to an expression in the
     *  face's own set. Moods the set doesn't carry by id
     *  map by family (happy→pleased for adults, etc.) so
     *  every mood every system emits lands somewhere
     *  authored — never a blank face. */
    fun forMood(kind: FaceKind, mood: String): Expr {
        val set = setFor(kind)
        set.firstOrNull { it.id == mood }?.let { return it }
        val alias = when (mood) {
            "happy" -> if (kind == FaceKind.ADULT) "pleased" else "happy"
            "soft" -> if (kind == FaceKind.ADULT) "pleased"
                else if (kind == FaceKind.ANIMAL) "happy" else "happy"
            "goofy" -> if (kind == FaceKind.CHILD) "silly"
                else if (kind == FaceKind.ADULT) "amused" else "playful"
            "sleep" -> "sleepy"
            "idle" -> if (kind == FaceKind.ANIMAL) "calm" else "neutral"
            else -> if (kind == FaceKind.ANIMAL) "calm" else "neutral"
        }
        return set.first { it.id == alias }
    }
}

/** Per-character motion: the authored parameters of one
 *  character's rig. Every field is a real input to the
 *  walk/idle/talk cycles in KidArt + GameView. */
data class MotionSpec(
    val name: String,
    val gaitHz: Float,        // steps per second at full stride
    val strideDeg: Float,     // leg swing amplitude
    val bobU: Float,          // body bob, figure units
    val armSwingDeg: Float,   // arm swing amplitude
    val headBobDeg: Float,    // head nod while walking
    val idleSwayU: Float,     // idle weight shift
    val gestureAmp: Float,    // talk-gesture amplitude 0..1
    val gestureKind: String,  // hands | point | wave | shrug | still | bounce
    val restExpr: String,     // resting expression id in their set
    val phase: Float          // cycle phase offset (their own rhythm)
)

object MotionRegistry {

    /** Hand-authored overrides for the characters whose
     *  movement IS their character. Values are
     *  (gaitHz, stride, bob, armSwing, gesture, restExpr). */
    private val OVERRIDES: Map<String, List<Any>> = mapOf(
        "Cartman" to listOf(1.5f, 15f, 1.1f, 10f, "point", "proud"),
        "Kyle" to listOf(2.1f, 21f, 0.8f, 16f, "hands", "neutral"),
        "Stan" to listOf(2.0f, 20f, 0.8f, 15f, "hands", "neutral"),
        "Kenny" to listOf(1.9f, 17f, 0.7f, 8f, "still", "neutral"),
        "Butters" to listOf(2.3f, 19f, 1.2f, 18f, "bounce", "happy"),
        "Tweek" to listOf(3.1f, 16f, 0.5f, 22f, "hands", "worried"),
        "Craig" to listOf(1.6f, 18f, 0.4f, 7f, "still", "neutral"),
        "Clyde" to listOf(1.9f, 20f, 0.9f, 14f, "wave", "happy"),
        "Jimmy" to listOf(1.7f, 14f, 1.0f, 12f, "hands", "happy"),
        "Timmy" to listOf(1.4f, 6f, 0.5f, 10f, "bounce", "excited"),
        "Token" to listOf(1.9f, 20f, 0.7f, 13f, "hands", "neutral"),
        "Tolkien" to listOf(1.9f, 20f, 0.7f, 13f, "hands", "neutral"),
        "Wendy" to listOf(2.0f, 20f, 0.8f, 15f, "hands", "neutral"),
        "Bebe" to listOf(2.1f, 19f, 0.9f, 17f, "wave", "happy"),
        "Randy" to listOf(1.8f, 24f, 1.0f, 24f, "hands", "pleased"),
        "Sharon" to listOf(1.8f, 21f, 0.7f, 15f, "hands", "neutral"),
        "Gerald" to listOf(1.7f, 20f, 0.6f, 13f, "hands", "neutral"),
        "Sheila" to listOf(1.9f, 21f, 0.8f, 19f, "point", "talk"),
        "Chef" to listOf(1.7f, 22f, 1.1f, 20f, "hands", "pleased"),
        "Mr. Garrison" to listOf(1.8f, 21f, 0.9f, 21f, "point", "talk"),
        "Mr. Mackey" to listOf(1.6f, 19f, 0.6f, 11f, "hands", "neutral"),
        "Principal Victoria" to listOf(1.7f, 20f, 0.6f, 12f, "still", "stern"),
        "Crabtree" to listOf(1.6f, 20f, 0.8f, 14f, "point", "stern"),
        "Barbrady" to listOf(1.7f, 22f, 0.9f, 16f, "wave", "pleased"),
        "Jimbo" to listOf(1.8f, 23f, 0.9f, 18f, "hands", "pleased"),
        "Ned" to listOf(1.7f, 20f, 0.7f, 12f, "still", "neutral"),
        "Towelie" to listOf(1.5f, 18f, 1.4f, 20f, "wave", "pleased"),
        "Jesus" to listOf(1.5f, 19f, 0.5f, 10f, "hands", "pleased"),
        "Satan" to listOf(1.4f, 24f, 0.8f, 18f, "point", "amused"),
        "Mr. Hankey" to listOf(2.6f, 12f, 1.5f, 20f, "bounce", "amused"),
        "Big Gay Al" to listOf(2.0f, 21f, 1.1f, 22f, "wave", "pleased"),
        "Mrs. Crabtree" to listOf(1.6f, 20f, 0.8f, 14f, "point", "stern")
    )

    private val cache = mutableMapOf<String, MotionSpec>()

    /** The character's own motion, built from their sheet
     *  (band, measured proportions, resting face, signature)
     *  with the hand overrides on top. Deterministic — the
     *  same character moves the same way every frame. */
    fun forName(name: String): MotionSpec = cache.getOrPut(name) {
        val spec = KidArt.SHEETS[name]
        val band = CharacterSystem.bandOf(name)
        val idx = CharacterSystem.CAST.keys.indexOf(name)
            .let { if (it >= 0) it else (name.hashCode() and 0xff) }
        val tall = spec?.tall ?: (band == AgeBand.ADULT)
        // band base: kids quick and bouncy, adults longer
        // and heavier, teens between
        var gait = when (band) {
            AgeBand.KID -> 2.05f
            AgeBand.YOUNG_TEEN -> 1.95f
            AgeBand.TEEN -> 1.85f
            AgeBand.OLDER_TEEN -> 1.8f
            AgeBand.ADULT -> 1.75f
        }
        var stride = if (tall) 21f else 19f
        var bob = if (tall) 0.7f else 0.9f
        var arm = if (tall) 14f else 15f
        var gesture = "hands"
        // the resting expression resolves INSIDE the
        // face's own set (a teen on the adult set rests at
        // "pleased", never at a child-set "happy" that
        // their set doesn't carry)
        val faceKind = Expressions.kindOf(band)
        var rest = when (spec?.face) {
            "deadpan" -> "neutral"
            "nervous" -> "worried"
            "worried" -> "worried"
            "smug" -> if (faceKind == FaceKind.ADULT) "amused" else "proud"
            "grin", "braces" ->
                if (faceKind == FaceKind.ADULT) "pleased" else "happy"
            else -> if (faceKind == FaceKind.ANIMAL)
                "calm" else "neutral"
        }
        // the sheet's own measurements bend the base: a
        // wider figure strides shorter and bobs more; a
        // taller-headed kid bounces quicker
        val w = spec?.width ?: 1.0f
        stride *= (1.08f - (w - 1.0f) * 0.35f).coerceIn(0.8f, 1.15f)
        bob *= (0.85f + (w - 1.0f) * 0.5f).coerceIn(0.7f, 1.3f)
        gait *= (1.0f + ((idx % 7) - 3) * 0.022f)
        OVERRIDES[name]?.let { o ->
            gait = o[0] as Float; stride = o[1] as Float
            bob = o[2] as Float; arm = o[3] as Float
            gesture = o[4] as String; rest = o[5] as String
        }
        if (spec?.special == "wheelchair") {
            stride = 5f; bob = 0.45f; gesture = "bounce"
        }
        MotionSpec(
            name = name,
            gaitHz = gait, strideDeg = stride, bobU = bob,
            armSwingDeg = arm,
            headBobDeg = (stride * 0.16f).coerceIn(2f, 5f),
            idleSwayU = 0.35f + (idx % 5) * 0.09f,
            gestureAmp = (arm / 24f).coerceIn(0.25f, 1f),
            gestureKind = gesture,
            restExpr = rest,
            phase = (idx * 0.731f) % 6.283f
        )
    }

    /** Animal motion, per species: a squirrel darts, a
     *  beaver waddles, a porcupine ambles. */
    fun forSpecies(species: String): MotionSpec {
        val gait: Float; val stride: Float; val bob: Float
        val gesture: String
        when (species) {
            "squirrel" -> { gait = 3.4f; stride = 14f; bob = 0.8f; gesture = "bounce" }
            "rabbit" -> { gait = 2.8f; stride = 18f; bob = 1.4f; gesture = "bounce" }
            "porcupine" -> { gait = 1.5f; stride = 9f; bob = 0.4f; gesture = "still" }
            "beaver" -> { gait = 1.7f; stride = 12f; bob = 0.7f; gesture = "hands" }
            "field mouse" -> { gait = 3.8f; stride = 10f; bob = 0.5f; gesture = "bounce" }
            else -> { gait = 2.4f; stride = 13f; bob = 1.0f; gesture = "wave" }
        }
        return MotionSpec(species, gait, stride, bob, 10f, 3f,
            0.4f, 0.6f, gesture, "calm",
            (species.hashCode() and 0xffff) / 10414f)
    }

    /** The player's motion: built from the player's own
     *  created sheet, so the New Kid moves like themselves. */
    fun forPlayer(spec: SheetSpec): MotionSpec {
        val base = forName("Stan")
        return base.copy(
            name = "player",
            gaitHz = if (spec.tall) 1.8f else 2.05f,
            strideDeg = if (spec.tall) 21f else 19f,
            restExpr = when (spec.face) {
                "deadpan" -> "neutral"
                "nervous" -> "worried"
                "smug" -> if (spec.tall) "amused" else "proud"
                "grin", "braces" -> if (spec.tall) "pleased" else "happy"
                else -> "neutral"
            },
            gestureKind = "hands",
            phase = 0.3f
        )
    }
}

/** One action animation: how long it plays, what the body
 *  does, which expression rides it, and whether it reads
 *  in profile as well as front (all of them do — item 14). */
data class ActionAnim(
    val id: String, val label: String,
    val ticks: Int,           // at the scene's ~9 ticks/sec
    val loop: Boolean,
    val bodyDipU: Float,      // downward body travel, units
    val armPose: String,      // reach | both-up | one-up | lap | hold | swing | type
    val legPose: String,      // stand | walk | bent | seated | folded
    val expr: String,         // expression id (per-set alias resolved at draw)
    val sfx: String
)

object ActionAnims {

    val ALL: List<ActionAnim> = listOf(
        ActionAnim("idle", "Idle", 54, true, 0.2f, "swing", "stand", "neutral", ""),
        ActionAnim("walk", "Walk", 18, true, 0.5f, "swing", "walk", "neutral", ""),
        ActionAnim("talk", "Talk", 36, true, 0.3f, "one-up", "stand", "talk", ""),
        ActionAnim("conversation", "Conversation", 48, true, 0.3f, "one-up", "stand", "talk", ""),
        ActionAnim("sit_down", "Sit down", 7, false, 3.2f, "lap", "bent", "neutral", ""),
        ActionAnim("seated", "Seated", 54, true, 3.4f, "lap", "seated", "neutral", ""),
        ActionAnim("stand_up", "Stand up", 7, false, 3.2f, "one-up", "bent", "neutral", ""),
        ActionAnim("use_object", "Use object", 12, false, 0.8f, "reach", "stand", "neutral", "tap"),
        ActionAnim("pickup_trash", "Pick up trash", 11, false, 2.6f, "reach", "bent", "proud", "pickup"),
        ActionAnim("phone", "Use phone", 40, true, 0.2f, "hold", "stand", "talk", ""),
        ActionAnim("board_bus", "Catch the bus", 14, false, 0.6f, "one-up", "walk", "excited", ""),
        ActionAnim("ride_bus", "Ride the bus", 54, true, 3.4f, "lap", "seated", "neutral", ""),
        ActionAnim("eat", "Eat", 30, true, 0.4f, "reach", "seated", "happy", ""),
        ActionAnim("sleep", "Sleep", 72, true, 0.2f, "lap", "folded", "sleepy", ""),
        ActionAnim("work", "Work", 36, true, 0.7f, "type", "stand", "neutral", "tap"),
        ActionAnim("play", "Play", 30, true, 1.0f, "both-up", "walk", "excited", ""),
        ActionAnim("photo", "Photo pose", 20, false, 0.2f, "both-up", "stand", "happy", ""),
        ActionAnim("wave", "Wave", 16, true, 0.3f, "one-up", "stand", "happy", ""),
        ActionAnim("dance", "Dance", 36, true, 1.2f, "both-up", "walk", "excited", ""),
        ActionAnim("hurt", "Hurt", 12, false, 1.2f, "both-up", "bent", "hurt", ""),
        ActionAnim("cheer", "Cheer", 18, false, 0.8f, "both-up", "stand", "excited", "")
    )

    private val BY_ID = ALL.associateBy { it.id }
    fun byId(id: String): ActionAnim? = BY_ID[id]

    /** The player's blanket set (item 17): every action
     *  the player can be seen doing, sitting first. */
    val PLAYER_SET: List<String> = listOf(
        "sit_down", "seated", "stand_up", "use_object",
        "pickup_trash", "phone", "board_bus", "ride_bus",
        "walk", "idle", "talk", "conversation", "eat",
        "sleep", "work", "play", "photo", "wave", "dance",
        "hurt", "cheer"
    )

    /** Which action a feature trigger plays, by trigger
     *  vocabulary — every interactive verb in the game
     *  resolves to an authored animation. */
    fun forTrigger(trigger: String): ActionAnim = when (trigger) {
        "trash", "litter", "cleanup" -> byId("pickup_trash")!!
        "phone", "text", "call" -> byId("phone")!!
        "bus", "schoolbus", "busstation" -> byId("board_bus")!!
        "talk", "chat", "kid", "visitor" -> byId("conversation")!!
        "bench", "chair", "seat", "pew", "sofa" -> byId("sit_down")!!
        "bed", "sleep" -> byId("sleep")!!
        "eat", "food", "counter" -> byId("eat")!!
        "work", "job", "desk" -> byId("work")!!
        "photo", "camera" -> byId("photo")!!
        "play", "toy", "game" -> byId("play")!!
        else -> byId("use_object")!!
    }

    /** Sit-cycle helper: the phase of a sit transition
     *  (0 = standing, 1 = fully seated) [elapsed] ticks
     *  into an animation of [ticks].
     *
     *  v0.16.0e (spec B7 — the stepped-transition audit):
     *  STEPPED, not eased. Item 17's smoothstep made the
     *  sit glide through every in-between; the source
     *  pipeline keys sparsely and CUTS between held poses
     *  — smooth interpolation is the one thing that
     *  actively hurts this look. The phase now holds at
     *  three plateaus (standing → half → seated) and cuts
     *  between them. This was the audit's one true lerp
     *  in figure animation; the survivors (breath and
     *  talk-bob oscillation, the walk bob) are cycle
     *  additivity, not transitions, and the bus's pull-up
     *  ease is a vehicle, not a figure — all logged in
     *  the v0.16.0e README ledger. */
    fun sitPhase(elapsed: Int, ticks: Int, down: Boolean): Float {
        val f = (elapsed.toFloat() / ticks.coerceAtLeast(1)).coerceIn(0f, 1f)
        val q = (kotlin.math.floor(f * SIT_STEPS) / (SIT_STEPS - 1))
            .coerceIn(0f, 1f)
        return if (down) q else 1f - q
    }

    /** Held poses in a stepped transition (B7). */
    const val SIT_STEPS = 3
}

/** Continuous real-time wander (items 11+12): every figure
 *  owns a home slot from the crowd compose; wander slides
 *  them back and forth INSIDE a range capped at a third
 *  of the slot pitch, so two neighbours can never close
 *  to touching, and the pace/phase are the character's
 *  own motion parameters. The TARGETS re-key on the game
 *  clock (item 13): a new game quarter-hour deals every
 *  figure a new wander phase, so the crowd reshuffles
 *  with the town's day, not the device's. */
object Wander {

    /** Max wander reach as a fraction of the slot pitch:
     *  two adjacent figures closing at full reach still
     *  keep a third of a slot between their slots. */
    const val REACH_OF_PITCH = 0.30f

    data class Pose(
        val dxFrac: Float,      // offset, world fraction
        val walking: Boolean,
        val facing: Int,        // -1 left, +1 right, 0 front
        val phase: Float        // gait phase for the legs
    )

    /** The figure's wander pose at [nowMs] (real time —
     *  the motion on screen is continuous) with its game
     *  clock key [gameMin]/[absDay] dealing the phase.
     *
     *  v0.16.0e (spec B3): the gait phase is DISTANCE-
     *  driven, not clock-driven. It used to advance at
     *  gaitHz * 2π per second of wall time — so a figure
     *  crossing a narrow slot on a long period slid its
     *  feet the whole way, and the phase kept turning
     *  through the pause at each end. Now the phase is
     *  the figure's own leg geometry (Gait, at [fhFrac] —
     *  its drawn height as a fraction of the world
     *  width) applied to the CUMULATIVE PATH the
     *  triangle wave has actually walked this period:
     *  out (2 reaches of path), hold (no path, no phase),
     *  back (2 more), hold. Whole periods behind this
     *  one add their full 4-reach path, so the phase is
     *  continuous across period wraps inside a quarter;
     *  the quarter-hour re-key still reshuffles the
     *  crowd wholesale, as designed. gaitHz now only
     *  paces the PERIOD (how briskly the figure crosses
     *  its reach) — the phase follows the feet. */
    fun pose(name: String, slotWFrac: Float, fhFrac: Float,
             legLen: Float, boxH: Float, nowMs: Long,
             gameMin: Int, absDay: Int): Pose {
        val m = MotionRegistry.forName(name)
        val quarter = gameMin / 15
        val keyed = (name.hashCode() * 31 + quarter * 101 +
            absDay * 7).toFloat()
        val periodMs = (5200f + (kotlin.math.abs(keyed) % 2600f)) /
            (m.gaitHz / 2f)
        val t = (nowMs % periodMs.toLong()).toFloat() / periodMs
        // a triangle wave there-and-back, with a pause at
        // each end (walk, stop, look around, walk back)
        val tri = when {
            t < 0.42f -> -1f + (t / 0.42f) * 2f
            t < 0.58f -> 1f
            t < 0.96f -> 1f - ((t - 0.58f) / 0.38f) * 2f
            else -> -1f
        }
        val walking = (t < 0.42f || (t >= 0.58f && t < 0.96f)) &&
            slotWFrac > 0.004f
        val reach = slotWFrac * REACH_OF_PITCH
        val facing = when {
            !walking -> 0
            t < 0.5f -> 1
            else -> -1
        }
        // the path walked within this period, in world
        // fractions — out, (hold), back, (hold) — plus
        // every whole period's full out-and-back before it
        val inPeriod = when {
            t < 0.42f -> (tri + 1f) * reach
            t < 0.58f -> 2f * reach
            t < 0.96f -> 2f * reach + (1f - tri) * reach
            else -> 4f * reach
        }
        val periodsBefore = (nowMs / periodMs.toLong()).toFloat()
        val pathFrac = periodsBefore * 4f * reach + inPeriod
        val phase = m.phase +
            pathFrac * Gait.phasePerFrac(legLen, boxH, fhFrac)
        return Pose(tri * reach, walking, facing, phase)
    }
}

/** Item 13, as a rule object the suite pins: schedules,
 *  agendas, crowds and wander keys read the GAME clock
 *  (GameState.timeMin / absDay) only. Real-time sync is
 *  OFF by standing rule and no schedule path may consult
 *  the device clock. */
object GameClockRule {
    const val SCHEDULES_ON_GAME_CLOCK = true
    const val REALTIME_SYNC_DEFAULT = false

    /** The only clock a schedule may read. */
    fun nowOf(state: GameState): Pair<Int, Int> =
        state.timeMin to state.absDay
}

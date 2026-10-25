package com.hunt3r.southparktown

// ============================================================
// Darkroom — v0.9.0d: your house learns to DEVELOP what
// your camera saw.
//
// A working fixture in Your House (drawn with the bed, the
// shelf, the mirror, the album and the table in GameView's
// house fixtures): a red-lit corner with trays and a line
// of drying prints. Using it opens the album — pick a
// photo, develop it. Developing takes honest game time and
// honest paper money, and the clock that moves is the
// town's own (Engine.advanceTime): the world keeps its
// rules while you work, and a moment that already owns the
// town (Emergencies.momentOwned) won't let you start.
//
// A PRINT is a physical keepsake: it stores its own copy
// of the photo's record, frozen at develop time — the
// yearbook-volume survival pattern. If the source photo
// later fades out of the album cap, the print survives
// whole, caption and all. Developing never touches the
// source record, its star, or any judging: the album's
// record and the print's copy are strangers from the
// moment the tray fills.
//
// Prints live in a print box (box + wall together capped);
// up to WALL_CAP hang on the print wall beside the
// yearbook shelf and the ribbon display. An ENLARGEMENT —
// the long develop — can be gifted from the person menu's
// gift flow: judged by the photo's real fields (in the
// frame pays, chemistry with the frame pays, the followers
// the shot actually earned pay; not in the frame is a
// polite dud, said out loud, and the print comes back to
// you). Once per photo per person — the town remembers
// who holds which print, and the holder's Talk says so,
// once, the next time you speak.
//
// Dinner guests at YOUR table remark on wall prints that
// feature themselves or their household — fact-slot lines
// only (names, place, date from the print's own copy),
// the yearbook judges' discipline. A print of a yearbook
// WINNER wears a small brass plate, derived from the
// volumes on the shelf — never hand-set.
//
// The print wall is a fixture, not a decor piece: House's
// decor is EARNED achievement, derived from records; the
// wall is furniture, and its contents are your own work.
// House.DECOR does not gain a piece for it.
//
// Saves additive-only: one `prints` key (a capped record
// set). Pre-0.9.0d saves simply have an empty print box —
// the darkroom corner has been there all along, patient
// as furniture. Pure Kotlin, zero android.* imports.
// ============================================================

/** One developed print. [record] is the photo's facts,
 *  copied at develop time — later album edits (a star
 *  toggled, the record itself fading) never rewrite it. */
data class DarkroomPrint(
    val id: String,
    /** The album record this print was developed from. */
    val photoId: String,
    val record: PhotoRecord,
    /** Darkroom.SIZE_PRINT | Darkroom.SIZE_ENLARGEMENT. */
    val size: String,
    val madeAbsDay: Int,
    val madeKey: String,
    var onWall: Boolean = false,
    /** "" = it lives with you (box or wall); otherwise the
     *  name of the cast member who holds it now. */
    var owner: String = "",
    var giftedAbsDay: Int = -1,
    /** The holder's Talk line has played (owed-line rule:
     *  the playing consumes it, once per print). */
    var lineSaid: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id, "photoId" to photoId,
        "record" to record.toMap(), "size" to size,
        "madeAbsDay" to madeAbsDay, "madeKey" to madeKey,
        "onWall" to onWall, "owner" to owner,
        "giftedAbsDay" to giftedAbsDay, "lineSaid" to lineSaid
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(m: Map<String, Any?>): DarkroomPrint? {
            val id = m["id"]?.toString() ?: return null
            val rec = (m["record"] as? Map<String, Any?>)
                ?.let { PhotoRecord.fromMap(it) } ?: return null
            return DarkroomPrint(
                id = id,
                photoId = m["photoId"]?.toString() ?: rec.id,
                record = rec,
                size = m["size"]?.toString()
                    ?: Darkroom.SIZE_PRINT,
                madeAbsDay = (m["madeAbsDay"] as? Number)?.toInt() ?: -1,
                madeKey = m["madeKey"]?.toString() ?: "",
                onWall = m["onWall"] as? Boolean ?: false,
                owner = m["owner"]?.toString() ?: "",
                giftedAbsDay =
                    (m["giftedAbsDay"] as? Number)?.toInt() ?: -1,
                lineSaid = m["lineSaid"] as? Boolean ?: false
            )
        }
    }
}

object Darkroom {

    // ---------------- the dials (docs/TUNING.md tuning ledger) ----------------

    const val SIZE_PRINT = "print"
    const val SIZE_ENLARGEMENT = "enlargement"

    /** A standard print: half an hour in the trays. */
    const val PRINT_MINUTES = 30
    /** Paper + chemistry for a standard print. */
    const val PRINT_COST = 2
    /** An enlargement: a full hour, watched the whole way. */
    const val ENLARGE_MINUTES = 60
    /** The big paper, the frame, the patience. */
    const val ENLARGE_COST = 5

    /** Prints you hold at once (box + wall together). */
    const val BOX_CAP = 12
    /** The wall beside the yearbook shelf holds three. */
    const val WALL_CAP = 3
    /** Every print record kept, gifted ones included —
     *  past this, the oldest GIFTED print is forgotten
     *  (its holder's line goes with it; stated, not silent). */
    const val TOTAL_CAP = 24

    /** Gift judging: a framed enlargement of a photo the
     *  recipient POSED in starts here... */
    const val GIFT_BASE_POSER = 4
    /** ...of one they merely BOMBED, here. */
    const val GIFT_BASE_BOMBER = 2
    /** +1 per other poser in real chemistry with the
     *  recipient (Photos.HIGH_CHEMISTRY), capped. */
    const val GIFT_CHEM_CAP = 2
    /** +1 per this many followers the shot actually
     *  earned, capped. */
    const val GIFT_FOLLOWER_STEP = 25
    const val GIFT_FOLLOWER_CAP = 3
    /** +1 when the print's own copy carries the star. */
    const val GIFT_STAR_BONUS = 1
    /** No framed photo, however beloved, pays past this. */
    const val GIFT_CAP = 8

    // ---------------- the collection ----------------

    fun prints(state: GameState): List<DarkroomPrint> = state.prints

    /** Prints that live with you (box or wall). */
    fun held(state: GameState): List<DarkroomPrint> =
        state.prints.filter { it.owner.isEmpty() }

    /** Held and in the box (not on the wall). */
    fun boxed(state: GameState): List<DarkroomPrint> =
        held(state).filter { !it.onWall }

    /** Held and hanging, in hang order. */
    fun wall(state: GameState): List<DarkroomPrint> =
        held(state).filter { it.onWall }

    /** Prints [name] holds (gifted enlargements). */
    fun heldBy(state: GameState, name: String): List<DarkroomPrint> =
        state.prints.filter { it.owner == name }

    /** Everyone who holds a print of this photo, ever
     *  (the once-per-photo-per-person memory). */
    fun holdersOf(state: GameState, photoId: String): Set<String> =
        state.prints.filter { it.photoId == photoId &&
            it.owner.isNotEmpty() }.map { it.owner }.toSet()

    fun printById(state: GameState, printId: String): DarkroomPrint? =
        state.prints.firstOrNull { it.id == printId }

    /** The caption a print answers to: the copy's own
     *  caption, recomputed from its frozen fields — the
     *  album's later opinion of the photo is not consulted. */
    fun captionOf(print: DarkroomPrint): String =
        Photos.captionFor(print.record)

    /** A one-line label for menus: size, who/where, when. */
    fun shortLabel(print: DarkroomPrint): String {
        val loc = LOCATIONS[print.record.locId]?.name
            ?: print.record.locId
        val kind = if (print.size == SIZE_ENLARGEMENT)
            "🖼 Enlargement" else "🖨 Print"
        return "$kind · $loc · ${print.record.dateLabel}"
    }

    /** True once the source has faded out of the album —
     *  the print carries the only whole copy left. */
    fun sourceGone(state: GameState, print: DarkroomPrint): Boolean =
        state.photos.none { it.id == print.photoId }

    // ---------------- developing ----------------

    data class DevelopResult(
        val print: DarkroomPrint,
        val minutes: Int,
        val cost: Int,
        /** The print box's oldest GIFTED record, forgotten
         *  to make room under TOTAL_CAP, if any. */
        val forgotten: DarkroomPrint?
    )

    /** Why a develop can't start, in the house's voice —
     *  or null when the trays are ready. The dialog and the
     *  suite read the same gate. */
    fun developBlock(state: GameState, size: String): String? {
        if (Emergencies.momentOwned(state))
            return "The town has the hour — a bigger moment is " +
                "running, and prints keep. The trays will be " +
                "here when it lets go."
        if (state.photos.isEmpty())
            return "No photos yet — the trays stay dry until " +
                "the camera's seen something."
        if (held(state).size >= BOX_CAP)
            return "The print box is full ($BOX_CAP) — gift an " +
                "enlargement or let the wall be enough."
        val cost = if (size == SIZE_ENLARGEMENT) ENLARGE_COST
            else PRINT_COST
        if (Minigames.capsuleCoins(state) < cost)
            return "Developing takes paper, and paper takes " +
                "coins — $cost 🪙 for this one, and the tray " +
                "won't run on promises."
        return null
    }

    /** Develop [photoId] at [size]. Pays the paper cost out
     *  of the capsule coins, advances the town's own clock
     *  by the honest minutes (the world ticks — weather,
     *  agendas, an emergency's deadline all keep their
     *  rules), and files the print: a full copy of the
     *  record as it stands RIGHT NOW. The source record is
     *  read, never written. Null when [developBlock] has
     *  something to say (or the photo isn't in the album). */
    fun develop(state: GameState, photoId: String,
                size: String): DevelopResult? {
        if (developBlock(state, size) != null) return null
        val src = state.photos.firstOrNull { it.id == photoId }
            ?: return null
        val enlargement = size == SIZE_ENLARGEMENT
        val cost = if (enlargement) ENLARGE_COST else PRINT_COST
        val minutes = if (enlargement) ENLARGE_MINUTES
            else PRINT_MINUTES
        // the paper is real: paid up front, like the booth
        val left = Minigames.capsuleCoins(state) - cost
        if (left > 0) state.inventory[Minigames.COIN_ITEM] = left
        else state.inventory.remove(Minigames.COIN_ITEM)
        // the copy is made BEFORE the clock moves — the
        // print remembers the photo as it was when you
        // chose it, whatever the hour does to the day
        val copy = PhotoRecord.fromMap(src.toMap()) ?: return null
        val serial = (state.life["printsMade"] ?: 0) + 1
        state.life["printsMade"] = serial
        val print = DarkroomPrint(
            id = "pr$serial",
            photoId = src.id,
            record = copy,
            size = if (enlargement) SIZE_ENLARGEMENT
                else SIZE_PRINT,
            madeAbsDay = state.absDay,
            madeKey = state.todayKey
        )
        var forgotten: DarkroomPrint? = null
        if (state.prints.size >= TOTAL_CAP) {
            forgotten = state.prints.firstOrNull {
                it.owner.isNotEmpty() }
            if (forgotten != null) state.prints.remove(forgotten)
        }
        state.prints.add(print)
        // honest time: the town's clock, the town's ticks
        val eng = Engine()
        repeat(minutes / 15) { eng.advanceTime(state) }
        return DevelopResult(print, minutes, cost, forgotten)
    }

    // ---------------- the wall ----------------

    /** Hang a boxed print. Free, instant, reversible —
     *  the wall's only rule is its size. */
    fun hang(state: GameState, printId: String): Boolean {
        val p = printById(state, printId) ?: return false
        if (p.owner.isNotEmpty() || p.onWall) return false
        // v0.15.0f: a print hanging in a live gallery show
        // can't hang at home too — one print, one wall
        if (Gallery.isLent(state, p.id)) return false
        if (wall(state).size >= WALL_CAP) return false
        p.onWall = true
        return true
    }

    /** Take a wall print back down into the box. */
    fun unhang(state: GameState, printId: String): Boolean {
        val p = printById(state, printId) ?: return false
        if (p.owner.isNotEmpty() || !p.onWall) return false
        p.onWall = false
        return true
    }

    /** The wall, read out loud (the dialog's page): every
     *  hung print's full caption off its own copy, the
     *  brass plate where a volume on the shelf says so. */
    fun wallText(state: GameState): String {
        val w = wall(state)
        val sb = StringBuilder()
        sb.append("🖼 THE PRINT WALL\n\n")
        if (w.isEmpty()) {
            sb.append("Three frames' worth of bare wall beside " +
                "the yearbook shelf. The darkroom corner " +
                "glows red and patient — develop something " +
                "worth the light.")
            return sb.toString()
        }
        for (p in w) {
            sb.append("· ${shortLabel(p)}\n")
            sb.append("  ${captionOf(p)}\n")
            if (isYearbookWinner(state, p)) {
                sb.append("  🏅 Under it, a small brass plate: " +
                    "YEARBOOK WINNER — the Elementary printed " +
                    "this one.\n")
            }
            if (sourceGone(state, p)) {
                sb.append("  (The album's copy has faded. This " +
                    "print is the only whole one left.)\n")
            }
        }
        val boxedN = boxed(state).size
        if (boxedN > 0) sb.append("\nIn the box: $boxedN more " +
            (if (boxedN == 1) "print" else "prints") +
            " — swap from the darkroom corner.")
        return sb.toString().trimEnd()
    }

    // ---------------- the brass plate ----------------

    /** True when a published volume's winning entry IS
     *  this print's photo — derived from the yearbooks on
     *  the shelf (their entries carry the winning records;
     *  the ids are the join), never stored on the print. */
    fun isYearbookWinner(state: GameState,
                         print: DarkroomPrint): Boolean =
        state.yearbooks.any { vol ->
            vol.entries.any { it.record?.id == print.photoId }
        }

    /** The winning category a brass plate would name, for
     *  lines that want it. Null for an ordinary print. */
    fun winnerCategory(state: GameState,
                       print: DarkroomPrint): String? =
        state.yearbooks.firstNotNullOfOrNull { vol ->
            vol.entries.firstOrNull {
                it.record?.id == print.photoId }?.categoryId
        }

    // ---------------- gifting ----------------

    /** Enlargements you hold that [npcName] could still
     *  receive — the gift menu's print shelf. Once per
     *  photo per person: a photo they've already been
     *  given never lists for them again. */
    fun giftablePrints(state: GameState,
                       npcName: String): List<DarkroomPrint> =
        held(state).filter {
            it.size == SIZE_ENLARGEMENT &&
                npcName !in holdersOf(state, it.photoId) &&
                // v0.15.0f: a print lent to a live gallery
                // show can't change hands until the show
                // comes down — it is, physically, elsewhere
                !Gallery.isLent(state, it.id)
        }

    data class GiftResult(
        /** Friendship paid (0 for a dud). */
        val delta: Int,
        /** The recipient's line — always said, win or dud. */
        val line: String,
        /** True when the print changed hands. A dud hands
         *  it back: the print stays in your box. */
        val given: Boolean,
        /** A beloved gift (delta at the warm end). */
        val loved: Boolean
    )

    /** The framed print, judged. The recipient's presence
     *  in the frame is read off the print's own copy; the
     *  chemistry is the town's live number between the
     *  recipient and the frame's other posers; the
     *  followers are the ones the shot actually earned
     *  (frozen on the copy, like everything about a print). */
    fun givePrint(state: GameState, npcName: String,
                  printId: String): GiftResult? {
        val p = printById(state, printId) ?: return null
        if (p.owner.isNotEmpty() ||
            p.size != SIZE_ENLARGEMENT) return null
        if (npcName in holdersOf(state, p.photoId)) return null
        val rec = p.record
        val loc = LOCATIONS[rec.locId]?.name ?: rec.locId
        val fig = rec.figures.firstOrNull {
            it.name == npcName && it.kind != "player" }
        if (fig == null) {
            // the dud: named, factual, and the print comes
            // home with you — never a silent success
            val subjects = rec.npcs.filter {
                it.reaction != Photos.PHOTOBOMB }.map { it.name }
            val who = when (subjects.size) {
                0 -> "just you"
                1 -> subjects[0]
                2 -> "${subjects[0]} and ${subjects[1]}"
                else -> "${subjects.dropLast(1).joinToString(", ")} " +
                    "and ${subjects.last()}"
            }
            return GiftResult(0,
                "…It's $who at $loc. From ${rec.dateLabel}. " +
                    "$npcName studies it, then hands it back — " +
                    "gently, the way you hand back a wallet that " +
                    "isn't yours. “It's a good picture. It's just " +
                    "not MY picture. You keep it — it's your " +
                    "memory, not mine.”",
                given = false, loved = false)
        }
        val bombed = fig.reaction == Photos.PHOTOBOMB
        var delta = if (bombed) GIFT_BASE_BOMBER
            else GIFT_BASE_POSER
        if (!bombed) {
            val partners = rec.npcs.count { other ->
                other.name != npcName &&
                    other.reaction != Photos.PHOTOBOMB &&
                    Photos.chemistryBetween(state, npcName,
                        other.name) >= Photos.HIGH_CHEMISTRY
            }
            delta += minOf(GIFT_CHEM_CAP, partners)
        }
        delta += minOf(GIFT_FOLLOWER_CAP,
            rec.followersGained / GIFT_FOLLOWER_STEP)
        if (rec.starred) delta += GIFT_STAR_BONUS
        delta = minOf(GIFT_CAP, delta)
        // v0.10.0a: a print gifted to your own parent pays in
        // family warmth (Family's points), never cast
        // friendship — the judging math above is unchanged
        if (Family.isParent(npcName))
            Family.adjustPoints(state, npcName, delta)
        else Engine().relAdjust(state, npcName, delta)
        p.owner = npcName
        p.onWall = false
        p.giftedAbsDay = state.absDay
        val line = if (bombed)
            "“…Is that ME? In the back? Ruining it??” " +
                "$npcName looks at the frame for a long time. " +
                "“I look amazing. This is going over the " +
                "mantel. Nobody will ever believe I wasn't " +
                "invited.” (+$delta friendship)"
        else
            "“You DEVELOPED this? In your house? That's us — " +
                "$loc, ${rec.dateLabel}.” $npcName holds the " +
                "frame like it might still be wet. “Okay. " +
                "Officially? Best thing anybody's handed me " +
                "all year.” (+$delta friendship)"
        // a parent's copy of the line says what it paid in
        val outLine = if (Family.isParent(npcName))
            line.replace("friendship", "family warmth") else line
        return GiftResult(delta, outLine, given = true,
            loved = delta >= 5)
    }

    /** The holder's owed Talk line: the first print they
     *  hold whose line hasn't played. Playing consumes it
     *  (the removal IS the playing, like every owed line
     *  before it — Crew's regrets ride the same rule). */
    fun owedLine(state: GameState, npcName: String): Line? {
        val p = heldBy(state, npcName).firstOrNull { !it.lineSaid }
            ?: return null
        p.lineSaid = true
        val rec = p.record
        val loc = LOCATIONS[rec.locId]?.name ?: rec.locId
        return Line(npcName,
            "That print you made me — the one from $loc, " +
                "${rec.dateLabel}? It's up at my place. …Don't " +
                "make it weird. It's just a good picture.")
    }

    // ---------------- the dinner table ----------------

    /** What a hosted household's guests say about the wall:
     *  one line per wall print that features a member of
     *  [household] — composed ONLY from the print's own
     *  copy (the featured members' names, the place, the
     *  date), so a line exists only when its fact hangs
     *  in the room. The Dinners host beat reads these;
     *  the suite audits them like the judges' notes. */
    fun guestPrintLines(state: GameState,
                        household: HouseholdDef):
        List<String> {
        val out = mutableListOf<String>()
        for (p in wall(state)) {
            val featured = p.record.npcs
                .filter { it.reaction != Photos.PHOTOBOMB }
                .map { it.name }
                .filter { it in household.members }
            if (featured.isEmpty()) continue
            val loc = LOCATIONS[p.record.locId]?.name
                ?: p.record.locId
            val names = when (featured.size) {
                1 -> featured[0]
                2 -> "${featured[0]} and ${featured[1]}"
                else -> "${featured.dropLast(1).joinToString(", ")} " +
                    "and ${featured.last()}"
            }
            val brass = if (isYearbookWinner(state, p))
                " And the little brass plate — the yearbook " +
                    "printed that one. We saw." else ""
            out += when ((p.id.hashCode() and 0x7fffffff) % 3) {
                0 -> "“Wait — that's $names at $loc, up on " +
                    "YOUR wall. ${p.record.dateLabel}. You " +
                    "made that from a photo? …Don't let it go " +
                    "to your head, but it's good.$brass”"
                1 -> "“The print by the shelf — $names, " +
                    "$loc, ${p.record.dateLabel}. I remember " +
                    "that day. Didn't know anybody had written " +
                    "it down in light.$brass”"
                else -> "“You hang your people on the wall " +
                    "like family. That's $names at $loc, " +
                    "${p.record.dateLabel}, if anyone's " +
                    "asking. We're asking. It's lovely.$brass”"
            }
        }
        return out
    }
}

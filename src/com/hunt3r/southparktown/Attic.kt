package com.hunt3r.southparktown

// ============================================================
// Attic — v0.12.0e (Queue #12 item 5): YOUR HOUSE gains an
// upstairs.
//
// A fixture at Your House (drawn with the house fixtures in
// GameView, opened from GameActivity like the darkroom and
// the porch paper): a narrow door, three steps up, and a
// room the house always had. Everything in it is DERIVED
// from records that already exist, on every read — the
// attic stores nothing of its own:
//
//   1. THE OVERFLOW SHELF — what the house holds beyond
//      what the downstairs display shows. The trophy shelf
//      holds House.SHELF_SLOTS collectibles and
//      House.MAX_CUPS feat cups; everything earned past
//      those two honest caps lives up here. Derived from
//      state.collected and the feat sets alone — when the
//      downstairs display holds everything, the shelf up
//      here is honestly, provably bare.
//   2. THE FAMILY FRAME'S OUTTAKES — the family photos
//      that didn't make the frame. The frame downstairs
//      (Family.latestFamilyPhoto) holds the NEWEST home
//      photo with a parent in it; the outtakes are the
//      OTHER home photos with a parent in them — the
//      album's, plus any a darkroom print still holds
//      after the album's own copy has faded. Public
//      facts only: the record's own caption, its date,
//      its people — nothing a photo doesn't already say
//      out loud downstairs.
//   3. THE STORED CASE BOXES — one box per FINISHED
//      (retired) case, arcs 1–11, in the town-secrets
//      pattern: a labeled box on a shelf, present only
//      because the record says the case closed. PUBLIC
//      SHAPE ONLY, the paper's own discipline: the case
//      number, the title the Journal already publishes,
//      the closed stamp, the closing day where the
//      record keeps one, and how many pieces the board
//      holds — never an answer, a name the board hasn't
//      said in public, a branch word, or a sealed detail.
//      (The module's own text keeps the same wall — the
//      suite scans it.) An unretired case produces NO
//      box: the attic does not store hopes.
//
// No new economy (nothing up here is bought, sold, or
// paid — there is no coin path in this module at all),
// and NO new save shape: zero new save keys, zero new
// GameState fields. Every function is a pure read — call
// the page twice and the save is byte-identical between
// calls. A pre-0.12.0e save walks up the stairs into an
// attic that already knows what its owner has done,
// exactly like the house below it did in v0.3.0d.
//
// Art discipline: the fixture is drawn in solids only —
// plain wood, one brass knob, three steps — no patterns,
// no clustered motifs, nothing repeated but the steps
// themselves, and steps are architecture.
// ============================================================

object Attic {

    /** How many stored case boxes there can ever be:
     *  one per case in the town's lifetime so far. */
    const val CASE_TOTAL = 13

    /** Outtake lines the attic page prints before it
     *  counts the rest honestly (the album holds them
     *  all — the attic is a shelf, not a warehouse). */
    const val OUTTAKE_LINES = 12

    // ---------------- 1. the overflow shelf ----------------

    /** The collectibles the house holds that the trophy
     *  shelf has no slot for: COLLECTIBLES past the
     *  shelf's first [House.SHELF_SLOTS], in registry
     *  order — the same order the shelf itself fills in.
     *  Derived from state.collected alone. */
    fun overflowCollectibles(state: GameState): List<Collectible> =
        COLLECTIBLES.drop(House.SHELF_SLOTS)
            .filter { it.id in state.collected }

    /** Feat cups past the shelf's [House.MAX_CUPS] —
     *  House's own overflow count, read at its source. */
    fun overflowCups(state: GameState): Int = House.featOverflow(state)

    /** True when the downstairs display holds everything
     *  the house has earned and the overflow shelf is bare. */
    fun overflowEmpty(state: GameState): Boolean =
        overflowCollectibles(state).isEmpty() && overflowCups(state) == 0

    fun overflowLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🧺 THE OVERFLOW SHELF"
        if (overflowEmpty(state)) {
            out += "Bare boards. The trophy shelf downstairs holds " +
                "everything the house has earned — ${House.SHELF_SLOTS} " +
                "shelf slots, ${House.MAX_CUPS} cups, and the count " +
                "fits. For now."
            return out
        }
        out += "What the trophy shelf downstairs has no room for, " +
            "kept properly — not lost, just upstairs:"
        for (c in overflowCollectibles(state))
            out += "· ${c.icon} ${c.name} — past the shelf's " +
                "${House.SHELF_SLOTS} slots, so it lives up here."
        val cups = overflowCups(state)
        if (cups > 0)
            out += "· 🏆 +$cups feat ${if (cups == 1) "trophy" else "trophies"} " +
                "past the shelf's ${House.MAX_CUPS} cups — the overflow " +
                "the shelf itself counts, stored where it points."
        return out
    }

    // ---------------- 2. the family frame's outtakes ----------------

    /** Every album photo taken at Your House with a parent
     *  in frame — the family frame's whole candidate pool,
     *  in album order (oldest first, the framed one last). */
    fun familyPhotos(state: GameState): List<PhotoRecord> =
        state.photos.filter {
            it.locId == YOUR_HOUSE && Family.inPhoto(it) }

    /** One outtake: a family photo that is not the one in
     *  the frame. [fromPrint] marks the ones only a
     *  darkroom print still holds — the album's own copy
     *  has faded past the cap and the print is the record. */
    data class Outtake(val record: PhotoRecord, val fromPrint: Boolean)

    /** The outtakes, derived: every family photo except
     *  the framed (newest) one, then the print-held family
     *  photos whose album source is gone entirely. A photo
     *  the album still holds is never listed twice. */
    fun outtakes(state: GameState): List<Outtake> {
        val fam = familyPhotos(state)
        val framed = Family.latestFamilyPhoto(state)
        val out = mutableListOf<Outtake>()
        for (rec in fam) {
            if (framed != null && rec.id == framed.id) continue
            out += Outtake(rec, fromPrint = false)
        }
        val albumIds = state.photos.map { it.id }.toSet()
        val printOnly = Darkroom.held(state)
            .filter { p ->
                p.record.locId == YOUR_HOUSE &&
                    Family.inPhoto(p.record) &&
                    p.photoId !in albumIds
            }
            .sortedBy { it.madeAbsDay }
        for (p in printOnly) out += Outtake(p.record, fromPrint = true)
        return out
    }

    fun outtakeLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🖼 THE FAMILY FRAME'S OUTTAKES"
        val framed = Family.latestFamilyPhoto(state)
        if (framed == null) {
            out += "No family photos yet — the frame downstairs is " +
                "still empty, so there is nothing to out-take. " +
                "Take a photo at home with Mom or Dad in it and " +
                "the frame fills; take another and the first " +
                "one moves up here."
            return out
        }
        out += "In the frame downstairs: ${Photos.captionFor(framed)} " +
            "(${framed.dateLabel}) — the newest. The rest live here:"
        val all = outtakes(state)
        if (all.isEmpty()) {
            out += "Just the one so far. The frame holds it, and " +
                "the attic shelf beside it waits, honestly empty."
            return out
        }
        for (o in all.take(OUTTAKE_LINES)) {
            val line = "· ${Photos.captionFor(o.record)} " +
                "(${o.record.dateLabel})"
            out += if (o.fromPrint)
                "$line — the album's copy has faded; the darkroom " +
                    "print still holds this one whole."
            else line
        }
        if (all.size > OUTTAKE_LINES)
            out += "…and ${all.size - OUTTAKE_LINES} more, every one " +
                "of them in the album where it belongs — the attic " +
                "keeps the shelf, the album keeps the pictures."
        return out
    }

    // ---------------- 3. the stored case boxes ----------------

    /** One stored box: a finished case's PUBLIC shape and
     *  nothing else — its number, the title the Journal
     *  publishes, the day it closed (-1 where the record
     *  keeps no closing day), and the board's piece count
     *  against its whole. No branch, no choice, no name
     *  the board hasn't already said in public. */
    data class CaseBox(
        val number: Int,
        val title: String,
        val closedDay: Int,
        val piecesHeld: Int,
        val piecesTotal: Int
    )

    /** The boxes, derived on every read from the cases'
     *  own retired flags: a case earns its box the day
     *  it retires, and an unretired case produces none.
     *  In case order, always. */
    fun caseBoxes(state: GameState): List<CaseBox> {
        val out = mutableListOf<CaseBox>()
        if (state.arcRetired) out += CaseBox(1, Mystery.ARC_TITLE, -1,
            Mystery.clueCount(state), Mystery.CLUES.size)
        if (state.arc2Retired) out += CaseBox(2, Mystery2.CASE_TITLE,
            state.arc2RetiredDay,
            Mystery2.clueCount2(state), Mystery2.CLUES2.size)
        if (state.arc3Retired) out += CaseBox(3, Mystery3.CASE_TITLE,
            state.arc3RetiredDay,
            Mystery3.clueCount3(state), Mystery3.CLUES3.size)
        if (state.arc4Retired) out += CaseBox(4, Mystery4.CASE_TITLE,
            state.arc4RetiredDay,
            Mystery4.clueCount4(state), Mystery4.CLUES4.size)
        if (state.arc5Retired) out += CaseBox(5, Mystery5.CASE_TITLE,
            state.arc5RetiredDay,
            Mystery5.clueCount5(state), Mystery5.CLUES5.size)
        if (state.arc6Retired) out += CaseBox(6, Mystery6.CASE_TITLE,
            state.arc6RetiredDay,
            Mystery6.clueCount6(state), Mystery6.CLUES6.size)
        if (state.arc7Retired) out += CaseBox(7, Mystery7.CASE_TITLE,
            state.arc7RetiredDay,
            Mystery7.clueCount7(state), Mystery7.CLUES7.size)
        if (state.arc8Retired) out += CaseBox(8, Mystery8.CASE_TITLE,
            state.arc8RetiredDay,
            Mystery8.clueCount8(state), Mystery8.CLUES8.size)
        if (state.arc9Retired) out += CaseBox(9, Mystery9.CASE_TITLE,
            state.arc9RetiredDay,
            Mystery9.clueCount9(state), Mystery9.CLUES9.size)
        if (state.arc10Retired) out += CaseBox(10, Mystery10.CASE_TITLE,
            state.arc10RetiredDay,
            Mystery10.clueCount10(state), Mystery10.CLUES10.size)
        if (state.arc11Retired) out += CaseBox(11, Mystery11.CASE_TITLE,
            state.arc11RetiredDay,
            Mystery11.clueCount11(state), Mystery11.CLUES11.size)
        if (state.arc12Retired) out += CaseBox(12, Mystery12.CASE_TITLE,
            state.arc12RetiredDay,
            Mystery12.clueCount12(state), Mystery12.CLUES12.size)
        if (state.arc13Retired) out += CaseBox(13, Mystery13.CASE_TITLE,
            state.arc13RetiredDay,
            Mystery13.clueCount13(state), Mystery13.CLUES13.size)
        return out
    }

    fun boxLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        val boxes = caseBoxes(state)
        out += "📦 STORED CASE BOXES — ${boxes.size} of $CASE_TOTAL closed"
        if (boxes.isEmpty()) {
            out += "No boxes yet. A case earns its box the day it " +
                "closes — until then there is nothing up here but " +
                "good intentions and a broom."
            return out
        }
        out += "One box per closed case, taped and labeled in the " +
            "town-secrets pattern: the box keeps the SHAPE of the " +
            "case — never the telling of it."
        for (b in boxes) {
            val dayBit = if (b.closedDay >= 0)
                "closed on day ${b.closedDay + 1}"
            else
                "closed (the first case's record keeps no closing " +
                    "day, and the attic doesn't invent one)"
            out += "· 📦 Case ${b.number} — “${b.title}”: $dayBit. " +
                "The board's count stands at ${b.piecesHeld} of " +
                "${b.piecesTotal} pieces. The box stays taped."
        }
        return out
    }

    // ---------------- the page ----------------

    /** True when the attic has nothing to show at all:
     *  no overflow, no family photo, no closed case. */
    fun isBare(state: GameState): Boolean =
        overflowEmpty(state) &&
            Family.latestFamilyPhoto(state) == null &&
            caseBoxes(state).isEmpty()

    /** The whole attic, as the fixture's dialog reads it:
     *  three derived shelves and nothing stored. Pure —
     *  the same save reads the same page forever. */
    fun pageLines(state: GameState): List<String> {
        val out = mutableListOf<String>()
        out += "🪜 THE ATTIC"
        out += "Up the narrow stairs: a low room under the roof " +
            "that keeps what the house below has outgrown. " +
            "Nothing up here was ever stored by hand — every " +
            "shelf is read off the house's own records, fresh, " +
            "every time you climb up."
        out += ""
        out += overflowLines(state)
        out += ""
        out += outtakeLines(state)
        out += ""
        out += boxLines(state)
        return out
    }
}

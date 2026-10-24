package com.hunt3r.southparktown

// ============================================================
// Crowds — v0.5.0g: when a place gets crowded, the crowd
// stops spreading into an even, cluttered row and gathers
// the way people actually do — in little conversational
// clusters (Lucky's correction, with the scene depth pass).
//
// Pure and deterministic: the same (present set, chemistry,
// place) always lays out the same crowd, so nobody reshuffles
// between ticks. Under [CLUSTER_MIN_TOTAL] this returns null
// and GameView's original slot layout runs untouched.
//
// Chemistry is the town's own social data, carried in as a
// [Chem] snapshot so this file stays state-free: pair-script
// pairs pull together hard, bonds pull, same-faction pulls a
// little, and arch-rivals NEVER share a cluster (unless a
// pair script says they're a double act — some rivalries
// ARE the friendship). Complete-linkage merging: a cluster
// only forms when EVERY cross pair is willing, so nobody
// gets chained into a group through a friend.
//
// Clusters are visual grouping only. Every figure keeps its
// own standing spot with a pinned minimum separation
// ([MIN_SEP]), and GameView resolves figure taps to the
// NEAREST center — a huddle never makes anyone untappable.
// ============================================================
object Crowds {

    /** Crowds at or above this many present figures cluster;
     *  below it, the classic spread was never clutter. */
    const val CLUSTER_MIN_TOTAL = 7

    /** A conversation, not a mob: clusters cap at four. */
    const val CLUSTER_MAX = 4

    /** Within a cluster, members stand this fraction of a
     *  normal slot pitch apart — close enough to talk. */
    const val TIGHT = 0.66f

    /** Between two groups in a row, the step from the last
     *  member of one to the first of the next, in slot
     *  pitches — the visible gap that says "different
     *  conversation". */
    const val BETWEEN = 1.08f

    /** The pinned floor on center-to-center distance within
     *  a row (fraction of world width): nearest-center tap
     *  resolution is only honest if centers stay this far
     *  apart. Holds for every crowd up to a full 44-cast
     *  pile-up (slot pitch shrinks with the crowd; the
     *  tightest case is pinned in the suite). */
    const val MIN_SEP = 0.05f

    const val SCRIPT_AFFINITY = 50
    const val FACTION_AFFINITY = 8
    const val RIVAL_BLOCK = -1000

    /** The social snapshot the layout reads. All maps keyed
     *  by the people PRESENT; absent pairs read as neutral.
     *  [crew] (v0.9.0b): the player's crew members present —
     *  a standing cluster: two crew members always count as
     *  willing to stand together (below a pair script's
     *  pull, above a faction's), and the crew bond outranks
     *  even an arch-rival block between two of its own —
     *  you stand with your crew, grudges included.
     *  [rivalCrew] (v0.9.0e): the RIVAL's crew, captain
     *  included — the same standing-cluster treatment on
     *  its OWN affinity (RIVAL_CREW_AFFINITY, deliberately
     *  not the player's 40): two formations can share a
     *  frame without becoming one, and the suite proves
     *  both hold MIN_SEP/span together at full cast. */
    data class Chem(
        val bond: Map<Set<String>, Int> = emptyMap(),
        val scriptPairs: Set<Set<String>> = emptySet(),
        val rivalPairs: Set<Set<String>> = emptySet(),
        val factionOf: Map<String, String> = emptyMap(),
        val crew: Set<String> = emptySet(),
        val rivalCrew: Set<String> = emptySet()
    )

    /** One figure's standing spot: [xFrac] across the world,
     *  [frontness] rows back from the front (0 = front row),
     *  [clusterId] ≥ 0 for cluster members (−1 = loner), and
     *  [mirror] — cluster members right of their group's
     *  center draw mirrored, so the asymmetries of their
     *  sheets turn INWARD, toward the conversation (the same
     *  cheap, honest turn PhotoView's back-to-back pairs use). */
    data class Placed(
        val name: String,
        val xFrac: Float,
        val frontness: Int,
        val clusterId: Int,
        val mirror: Boolean
    )

    data class Layout(val rows: Int, val placed: List<Placed>)

    /** The row count the classic layout uses — shared so a
     *  clustered crowd stands in the same depth of rows it
     *  would have spread across. */
    fun rowsFor(total: Int): Int = when {
        total <= 5 -> 1
        total <= 10 -> 2
        total <= 16 -> 3
        // v0.9.0a: a fifth row above 64. At 70 cast the
        // four-row classic packs 18 to a row, and a cluster
        // crossing the line takes it to 21 — at the MIN_SEP
        // pitch that row spans 1.00 of the world and spills
        // past the classic margins. Five rows pack 14 (17
        // worst case, span 0.80): everyone stays inside.
        // v0.11.0d: the rule above 70 keeps the PROVEN
        // geometry — perRow 14 — and adds a row per fourteen
        // citizens. Measured on the real tables at 98 cast:
        // five rows pack 22 (span 1.05, margins gone), six
        // pack 20 (span 0.95, minx 0.025 — still spilling),
        // seven pack 14–16 (worst span 0.82, everyone
        // inside). The row count is presentation only; no
        // save data rides on it.
        total <= 64 -> 4
        total <= 70 -> 5
        total <= 84 -> 6
        else -> 7
    }

    /** How much two people want to stand together. A rival
     *  pair is a hard block (negative) — unless a pair script
     *  makes them a double act, which outranks the grudge. */
    /** The crew's standing pull (v0.9.0b): under a pair
     *  script's 50, well over a faction's 8 — the crew is
     *  the group you chose, and a crowd reads it as one. */
    const val CREW_AFFINITY = 40

    /** The rival's crew's standing pull (v0.9.0e): its own
     *  number, a shade under your crew's — they're a real
     *  formation, but the crowd's eye (and the merge order)
     *  gives your chosen four the stronger read. Still far
     *  over a faction's 8 and a script pair short of 50. */
    const val RIVAL_CREW_AFFINITY = 34

    fun affinity(a: String, b: String, chem: Chem): Int {
        if (a == b) return 0
        val key = setOf(a, b)
        val script = key in chem.scriptPairs
        val crewed = a in chem.crew && b in chem.crew
        val rcrewed = a in chem.rivalCrew && b in chem.rivalCrew
        if (key in chem.rivalPairs && !script && !crewed &&
            !rcrewed)
            return RIVAL_BLOCK
        var aff = (chem.bond[key] ?: 0) * 3
        if (script) aff += SCRIPT_AFFINITY
        if (crewed) aff = maxOf(aff, CREW_AFFINITY)
        if (rcrewed) aff = maxOf(aff, RIVAL_CREW_AFFINITY)
        val fa = chem.factionOf[a]
        if (fa != null && fa == chem.factionOf[b]) aff += FACTION_AFFINITY
        return aff
    }

    private fun unitKey(a: List<String>, b: List<String>): String =
        (a + b).sorted().joinToString("|")

    /** Greedily merge the most-willing pair of groups while
     *  every cross pair is willing and the cap allows.
     *  Returns the groups (clusters of 2–4 and loners),
     *  ordered by first member name; members sorted by name.
     *  Fully deterministic: ties break on the merged name key. */
    fun clustersFor(names: List<String>, chem: Chem): List<List<String>> {
        var units = names.map { listOf(it) }.toMutableList()
        while (true) {
            var bestI = -1
            var bestJ = -1
            var bestAff = 0
            var bestKey = ""
            for (i in units.indices) {
                for (j in i + 1 until units.size) {
                    if (units[i].size + units[j].size > CLUSTER_MAX) continue
                    var minAff = Int.MAX_VALUE
                    for (a in units[i]) for (b in units[j]) {
                        val af = affinity(a, b, chem)
                        if (af < minAff) minAff = af
                    }
                    if (minAff <= 0) continue
                    val key = unitKey(units[i], units[j])
                    if (minAff > bestAff ||
                        (minAff == bestAff && (bestI < 0 || key < bestKey))) {
                        bestI = i; bestJ = j; bestAff = minAff; bestKey = key
                    }
                }
            }
            if (bestI < 0) break
            val merged = (units[bestI] + units[bestJ]).sorted()
            units = units.filterIndexed { idx, _ -> idx != bestI && idx != bestJ }
                .toMutableList()
            units.add(merged)
        }
        return units.sortedBy { it.first() }
    }

    /** The full layout, or null when the crowd is small
     *  enough that the classic spread applies. Groups are
     *  packed into rows back-to-front and NEVER split across
     *  rows; within a row, members stand at the tight pitch,
     *  groups at the wide step, and the whole row centers. */
    fun layout(names: List<String>, chem: Chem): Layout? {
        val total = names.size
        if (total < CLUSTER_MIN_TOTAL) return null
        val units = clustersFor(names, chem)
        val targetRows = rowsFor(total)
        val perRow = (total + targetRows - 1) / targetRows

        // pack units into rows, back row first. A row closes
        // only once it has REACHED the classic per-row count —
        // the unit that crosses the line still joins this row
        // (a cluster is never split), so the row count can
        // never exceed the classic layout's own row count,
        // while a row overshoots perRow by less than one
        // cluster. (Filling only to "fits" instead would let
        // 4-clusters strand half-empty rows and double the
        // row count on a bonded crowd.)
        val rowUnits = mutableListOf<MutableList<List<String>>>()
        var curRow = mutableListOf<List<String>>()
        var curCount = 0
        for (u in units) {
            if (curRow.isNotEmpty() && curCount >= perRow) {
                rowUnits.add(curRow)
                curRow = mutableListOf()
                curCount = 0
            }
            curRow.add(u)
            curCount += u.size
        }
        if (curRow.isNotEmpty()) rowUnits.add(curRow)
        val rowsUsed = rowUnits.size

        val slot = 0.94f / perRow
        // v0.7.0a: the pitch never drops below MIN_SEP. At 44 cast
        // (perRow 11) slot*TIGHT cleared the floor on its own; at
        // 54 (perRow 14) it fell to 0.044 and cluster members
        // stood closer than the separation the layout promises.
        // The between-group gaps absorb the difference — they
        // already compress toward the pitch, and every reachable
        // row shape still spans <= 0.94 (worst case: a row that
        // overshoots perRow by one cluster, 17 members in >= 5
        // units: intra 0.60 + gaps <= 0.29).
        val pitch = maxOf(slot * TIGHT, MIN_SEP)
        val placed = mutableListOf<Placed>()
        var nextClusterId = 0
        rowUnits.forEachIndexed { rowIdx, row ->
            val frontness = (rowsUsed - 1) - rowIdx
            val intra = row.sumOf { it.size - 1 } * pitch
            var between = slot * BETWEEN
            if (row.size > 1) {
                // compress the gaps before ever squeezing the
                // tight pitch — MIN_SEP rides on the pitch
                val maxBetween = (0.94f - intra) / (row.size - 1)
                if (between > maxBetween) between = maxBetween.coerceAtLeast(pitch)
            }
            val span = intra + between * (row.size - 1)
            var x = 0.03f + (0.94f - span) / 2f
            for (u in row) {
                val cid = if (u.size >= 2) nextClusterId++ else -1
                val centers = u.indices.map { i -> x + i * pitch }
                u.forEachIndexed { i, name ->
                    // right of the group's center BY POSITION
                    // (an index rule, not a float mean — the
                    // middle member of an odd cluster faces
                    // front, exactly, every time)
                    placed.add(Placed(name, centers[i], frontness, cid,
                        mirror = i * 2 > u.size - 1))
                }
                x += (u.size - 1) * pitch + between
            }
        }
        return Layout(rowsUsed, placed)
    }
}

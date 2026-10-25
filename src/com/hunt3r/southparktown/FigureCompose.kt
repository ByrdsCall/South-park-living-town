package com.hunt3r.southparktown

// ============================================================
// FigureCompose — v0.15.0g, THE SCALE & SPACE BUILD
// (Lucky's playtest round 2, Oct 18: the bus-stop crowd
// rendered as "one overlapping blob of heads behind the
// bus, bodies swallowed", and figures must get "bigger
// and smaller with position" — smaller up by the houses,
// bigger down by the street).
//
// The ONE composition pass for everyone standing in a
// scene: GameView collects standing spots (from Crowds'
// clustered layout or the classic chunk spread), and
// this function turns them into composed geometry —
// center x, feet line, on-screen height — in pixels.
// Pure Kotlin: the suite pins the composed geometry
// itself (pairwise spacing, depth pairs, bus clearance),
// not a description of it.
//
// What it enforces, by construction:
//  - THE CROWD WINDOW: a crowd gathers in a window of
//    the (now much wider) world, WINDOW_SCREENS screen
//    widths across, centered on the scene's heart (the
//    facade; the station scenes sit a breath west, where
//    the shelter and the sign stand). Nobody disperses
//    across the whole widened street, and nobody has to
//    share a slot: the window is sized so a full crowd
//    fits it at body pitch.
//  - TRUE 2D SPACING: rows are re-chunked to the row
//    capacity the window affords at body pitch (never
//    more than MAX_ROWS deep), and within a row a sweep
//    holds every pair of centers at least PITCH_SLACK ×
//    the pair's mean body width apart — bodies may
//    brush shoulders (a crowd is a crowd) but never
//    merge into one blob.
//  - DEPTH SCALING: a figure's height is its world-scale
//    height times WalkBand.depthScale at its OWN feet
//    band — back rows compose smaller, front rows
//    bigger, the player included (the player reads the
//    same curve at their own band in GameView). The
//    v0.15.0d ladder survives because depth multiplies
//    every figure at the same band by the same factor.
//  - FORBIDDEN GROUND: caller-supplied forbidden x
//    ranges (the standing bus's body span) push figures
//    clear — a waiting rider never stands inside the
//    bus's footprint, so the traffic plane can never
//    swallow a body again.
// ============================================================

object FigureCompose {

    /** A figure's body width per unit of its height.
     *  The sheets are head-dominated: the drawn extent
     *  (head + shoulders + arms) runs about 0.72 of the
     *  figure's height for a kid, less for an adult;
     *  0.72 is the conservative (widest) read, and the
     *  mobility figures (Timmy's chair, Jimmy's crutches)
     *  pass their own wider ratio through [compose]. */
    const val WIDTH_RATIO = 0.72f

    /** Same-row centers are held at least this × the
     *  pair's mean body width apart: shoulders may
     *  brush (below 1.0 would let bodies overlap), the
     *  blob may not form. */
    const val PITCH_SLACK = 1.02f

    /** v0.17.0b: THE FEET ELLIPSE — the only collision
     *  shape a figure has. A paper kid collides with
     *  what his FEET touch, never with his whole body
     *  box: the ellipse is the ground shadow the painters
     *  already draw under every figure (half-width 0.30
     *  of height), squashed flat in depth — 0.10 of
     *  height is under half the tightest row pitch
     *  [compose] ever deals (0.54 of height), so figures
     *  a row apart can never touch. The v0.17.0a
     *  playtest failure was collision read off the BODY
     *  pitch (0.72-wide bodies at PITCH_SLACK): walking
     *  one row dealt every figure in it sideways. */
    const val FEET_RX_OVER_FH = 0.30f
    const val FEET_RY_OVER_FH = 0.10f

    /** One figure's ground footprint, in screen px:
     *  center x, feet line, on-screen height. */
    data class Feet(val name: String, val cx: Float,
                    val feetY: Float, val fh: Float)

    fun feetRx(fh: Float): Float = fh * FEET_RX_OVER_FH
    fun feetRy(fh: Float): Float = fh * FEET_RY_OVER_FH

    /** The normalized ellipse distance² between the feet
     *  at ([ax], [ay]) for a figure [afh] tall and figure
     *  [b]'s: < 1 inside, 1 exactly on the edge, > 1 clear. */
    fun feetDist2(ax: Float, ay: Float, afh: Float,
                  b: Feet): Float {
        val rx = feetRx(afh) + feetRx(b.fh)
        val ry = feetRy(afh) + feetRy(b.fh)
        if (rx <= 0f || ry <= 0f) return Float.MAX_VALUE
        val dx = (ax - b.cx) / rx
        val dy = (ay - b.feetY) / ry
        return dx * dx + dy * dy
    }

    /** True when the feet ellipse at ([ax], [ay]) for a
     *  figure [afh] tall overlaps figure [b]'s. */
    fun feetTouch(ax: Float, ay: Float, afh: Float,
                  b: Feet): Boolean = feetDist2(ax, ay, afh, b) < 1f

    /** The center x a figure at ([ax], [ay]) must move
     *  to so its feet ellipse rests exactly ON [b]'s
     *  edge, on the side it already stands. Only
     *  meaningful while the two touch (or nearly). */
    fun feetEdgeX(ax: Float, ay: Float, afh: Float,
                  b: Feet): Float {
        val rx = feetRx(afh) + feetRx(b.fh)
        val ry = feetRy(afh) + feetRy(b.fh)
        val dyN = ((ay - b.feetY) / ry).coerceIn(-1f, 1f)
        val half = rx * kotlin.math.sqrt(1f - dyN * dyN)
        return if (ax < b.cx) b.cx - half else b.cx + half
    }

    /** The crowd window's width, in screen widths. The
     *  world is 3.4–5.1 screens wide after the widening;
     *  a crowd gathers in this much of it around the
     *  scene's heart. */
    const val WINDOW_SCREENS = 3.4f

    /** The deepest a composed crowd ever stacks. The
     *  band fits this many rows at the 0.54 figure pitch
     *  the rows have always used. */
    const val MAX_ROWS = 7

    /** One standing spot going in: [fx] across the
     *  CROWD in layout space (0..1, the space Crowds'
     *  layout and the classic spread both speak),
     *  [frontness] rows back from the front (0 = front
     *  row), [mirror] from the cluster layout. */
    data class Spot(
        val name: String, val fx: Float,
        val frontness: Int, val mirror: Boolean
    )

    /** One figure composed: center x (px, world space),
     *  feet line (px), on-screen height (px), its ground
     *  band, its row. */
    data class Placed(
        val name: String, val cx: Float, val feetY: Float,
        val fh: Float, val band: Float, val frontness: Int,
        val mirror: Boolean
    )

    data class Result(
        val placed: List<Placed>,
        /** The crowd's shared base height (the player's
         *  figure composes off the same base in GameView). */
        val fhBase: Float,
        val rows: Int,
        /** The window's per-row slot width (name tags and
         *  stroll amplitudes read it, as they read the old
         *  world slot). */
        val slotW: Float,
        val windowLeft: Float,
        val windowW: Float
    )

    /** The crowd window's width in px for a world
     *  [worldW] px wide on a screen [screenW] px wide. */
    fun windowWidthPx(worldW: Float, screenW: Float): Float =
        minOf(worldW, screenW * WINDOW_SCREENS)

    /** Where a scene's heart sits, as a world fraction:
     *  the facade's center everywhere, except the two
     *  bus scenes, whose heart is the station cluster
     *  (shelter, sign, the standing bus) west of center. */
    fun focalFrac(locId: String): Float =
        if (locId == "BusStop" || locId == "Elementary")
            0.47f else 0.5f

    /** The standing bus's forbidden span, in world
     *  fractions: its body, centered where GameView
     *  stands it ([busCxFrac]), at ladder length. */
    fun busSpanFrac(busCxFrac: Float, worldW: Float, h: Float):
        Pair<Float, Float> {
        val half = WorldScale.units(WorldScale.BUS_LEN_U, h) / 2f / worldW
        return (busCxFrac - half) to (busCxFrac + half)
    }

    /**
     * Compose [spots] into placed figures.
     * [heightRel] is each figure's own world-scale height
     * multiplier (KidArt.heightFor for cast, the band
     * ladder for generated folk); [widthRatio] its body
     * width per height (mobility figures wider).
     * [forbiddenPx] are world-px x ranges no figure's
     * body may intersect (the standing bus).
     */
    fun compose(
        spots: List<Spot>,
        heightRel: (String) -> Float,
        widthRatio: (String) -> Float,
        worldW: Float, screenW: Float, h: Float,
        inside: Boolean, focalFrac: Float,
        forbiddenPx: List<Pair<Float, Float>>
    ): Result {
        val kidPx = WorldScale.kidPx(h)
        if (spots.isEmpty()) {
            return Result(emptyList(), kidPx, 0, 0f, 0f, 0f)
        }
        // the composition window: WINDOW_SCREENS wide —
        // widened by whatever forbidden ground stands
        // in it (the bus eats curb; the queue stretches
        // along the rest of the street to make it back,
        // up to the whole world). A crowd at the stop
        // distributes across the scene's full width,
        // which is what the brief asks of it.
        val forbTotal = forbiddenPx.sumOf {
            (it.second - it.first).toDouble() }.toFloat()
        val winW = minOf(worldW,
            windowWidthPx(worldW, screenW) + forbTotal)
        val winLeft = (focalFrac * worldW - winW / 2f)
            .coerceIn(0f, maxOf(0f, worldW - winW))
        val winR = winLeft + winW
        // ---- clear segments: the window minus the
        // forbidden ground (the standing bus). A crowd
        // composes AROUND the bus: figures are assigned
        // to clear segments by capacity, never placed on
        // the bus and shoved off it (the shove is what
        // piled bodies into each other).
        val segs = mutableListOf<Pair<Float, Float>>()
        run {
            var cur = winLeft
            for ((fl, fr) in forbiddenPx.sortedBy { it.first }) {
                val cl = fl.coerceIn(winLeft, winR)
                val cr = fr.coerceIn(winLeft, winR)
                if (cl > cur) segs += cur to cl
                if (cr > cur) cur = cr
                if (cur >= winR) break
            }
            if (cur < winR) segs += cur to winR
            if (segs.isEmpty()) segs += winLeft to winR
        }
        val clearLen = segs.sumOf { (it.second - it.first).toDouble() }
            .toFloat().coerceAtLeast(1f)
        // ---- row capacity: how many bodies fit the
        // window at body pitch, at the base height the
        // guard below settles on. Start from the slot the
        // layout's own rows imply, apply the standing
        // guard (a packed scene narrows slots at most 8%
        // into figure size — the v0.15.0d rule), then cap
        // rows by what fits at pitch.
        val layoutRows = (spots.maxOf { it.frontness } + 1)
        val perRow0 = spots.groupBy { it.frontness }
            .values.maxOf { it.size }
        val slot0 = winW / perRow0
        val fhBase = minOf(kidPx, slot0 * 1.08f)
            .coerceAtLeast(kidPx * 0.92f)
        // the row budget prices a body at the FRONT depth
        // (the widest a figure in the row can compose)
        // with headroom for a mixed crowd's taller
        // members — a row that fits on paper fits on
        // screen, adults included
        val pitchMin = fhBase * WIDTH_RATIO * PITCH_SLACK *
            1.15f * 1.30f
        // with forbidden ground standing in the window,
        // the row budget is the CLEAR length — a row
        // must fit beside the bus, not across it
        val capPerRow = maxOf(1,
            ((if (forbiddenPx.isEmpty()) winW else clearLen) /
                pitchMin).toInt())
        val rowsWanted = maxOf(layoutRows,
            (spots.size + capPerRow - 1) / capPerRow)
        val rows = minOf(rowsWanted, MAX_ROWS)
        // ---- re-chunk: walk the spots back-to-front,
        // left to right (the layout's own reading order),
        // dealing them into [rows] rows of at most
        // [capPerRow]. Clusters stay adjacent in the
        // sequence, so a conversation survives the move
        // to a deeper row stack intact or nearly so.
        val seq = spots.sortedWith(
            compareByDescending<Spot> { it.frontness }.thenBy { it.fx })
        val chunked = seq.chunked(
            (spots.size + rows - 1) / rows)
        val rowsUsed = chunked.size
        // ---- vertical composition: feet lines pitch up
        // the band from the front edge, compressed when
        // the stack would climb past the band's top —
        // feet never render above it (the v0.15.0d rule,
        // kept word for word).
        val frontFeet = WalkBand.bottom(inside) * h
        val bandTop = WalkBand.top(inside) * h
        val bandDepth = frontFeet - bandTop
        val rowPitch = if (rows > 1)
            minOf(fhBase * 0.54f, bandDepth / rows) else 0f
        fun feetFor(frontness: Int): Float =
            (frontFeet - frontness * rowPitch).coerceAtLeast(bandTop)
        // rows as (spot, frontness): chunk 0 is the BACK
        // row (highest frontness)
        data class Rowed(val spot: Spot, val frontness: Int,
                         var cx: Float, val fh: Float, val w: Float)
        val rowed = mutableListOf<Rowed>()
        chunked.forEachIndexed { ci, chunk ->
            val frontness = (rowsUsed - 1) - ci
            val feet = feetFor(frontness)
            val band = WalkBand.bandForY(feet, h, inside)
            val ds = WalkBand.depthScale(band)
            for (s in chunk) {
                val fh = fhBase * heightRel(s.name) * ds
                rowed += Rowed(s, frontness,
                    winLeft + s.fx * winW, fh,
                    fh * widthRatio(s.name))
            }
        }
        // ---- horizontal passes, per row: hold body
        // pitch between neighbors. With forbidden
        // ground standing, each row is dealt across
        // the clear segments by capacity (wide
        // segments take more), spread evenly inside
        // each segment, and bodies never cross a
        // segment's forbidden edge — the bus's ground
        // is never stood on, whatever the shove.
        fun holdPitch(row: List<Rowed>, hi: Float) {
            val sorted = row.sortedBy { it.cx }
            for (i in 1 until sorted.size) {
                val prev = sorted[i - 1]
                val cur = sorted[i]
                val need = (prev.w + cur.w) / 2f * PITCH_SLACK
                if (cur.cx - prev.cx < need)
                    cur.cx = prev.cx + need
            }
            // a row pushed past its right edge walks
            // back from the edge the other way
            val over = (sorted.lastOrNull()?.let {
                it.cx + it.w / 2f } ?: 0f) - hi
            if (over > 0f) {
                for (r in sorted) r.cx -= over
                for (i in sorted.size - 2 downTo 0) {
                    val next = sorted[i + 1]
                    val cur = sorted[i]
                    val need = (next.w + cur.w) / 2f * PITCH_SLACK
                    if (next.cx - cur.cx < need)
                        cur.cx = next.cx - need
                }
            }
        }
        val byRow = rowed.groupBy { it.frontness }
        for ((_, row) in byRow) {
            if (forbiddenPx.isEmpty()) {
                holdPitch(row, winR)
            } else {
                // deal the row across segments in
                // layout order, by pitch capacity
                val meanNeed = row.sumOf { it.w.toDouble() }
                    .toFloat() / row.size * PITCH_SLACK
                val caps = segs.map { sg ->
                    maxOf(1, ((sg.second - sg.first) /
                        meanNeed).toInt())
                }
                val counts = IntArray(segs.size)
                var rem = row.size
                for (i in segs.indices) {
                    counts[i] = minOf(rem,
                        maxOf(0, ((segs[i].second - segs[i].first) /
                            clearLen * row.size).toInt()))
                    rem -= counts[i]
                }
                var gi = 0
                while (rem > 0 && gi < segs.size) {
                    counts[gi]++; rem--
                    gi = (gi + 1) % segs.size
                }
                var guard = 0
                while (guard++ < 64) {
                    val over = counts.indices.firstOrNull {
                        counts[it] > caps[it] } ?: break
                    val spare = counts.indices.filter {
                        counts[it] < caps[it] }
                    if (spare.isEmpty()) break
                    val to = spare.maxByOrNull {
                        caps[it] - counts[it] }!!
                    counts[over]--; counts[to]++
                }
                val inFxOrder = row.sortedBy { it.spot.fx }
                var idx = 0
                for (si in segs.indices) {
                    val (sl, sr) = segs[si]
                    val g = mutableListOf<Rowed>()
                    repeat(counts[si]) {
                        if (idx < inFxOrder.size)
                            g += inFxOrder[idx++]
                    }
                    if (g.isEmpty()) continue
                    // even spread inside the segment;
                    // then alternate the forbidden-edge
                    // clamp with the pitch sweep — the
                    // clamp first (bodies start clear of
                    // the bus), the sweep re-spreads what
                    // the clamp compressed, twice round
                    // so the two rules settle together.
                    // Only the FORBIDDEN edges clamp —
                    // the window-facing sides are free
                    // (the world clamp below settles
                    // those, as it always has)
                    val segW = sr - sl
                    for ((j, r) in g.withIndex()) {
                        r.cx = sl + segW *
                            (j + 0.5f) / g.size
                    }
                    fun clampBus() {
                        for (r in g) {
                            if (sl > winLeft)
                                r.cx = maxOf(r.cx, sl + r.w / 2f)
                            if (sr < winR)
                                r.cx = minOf(r.cx, sr - r.w / 2f)
                        }
                    }
                    clampBus(); holdPitch(g, sr)
                    clampBus(); holdPitch(g, sr)
                }
            }
            // nobody stands off the world — and if the
            // world clamp ever slides a body onto
            // forbidden ground, the bus wins: nudge to
            // the nearer clear edge, pitch tolerance
            // notwithstanding
            for (r in row) {
                r.cx = r.cx.coerceIn(r.w / 2f + 2f,
                    worldW - r.w / 2f - 2f)
                for ((fl, fr) in forbiddenPx) {
                    if (r.cx - r.w / 2f < fr &&
                        r.cx + r.w / 2f > fl) {
                        val pushLeft = fl - 4f - r.w / 2f
                        val pushRight = fr + 4f + r.w / 2f
                        r.cx = if (r.cx - pushLeft <=
                            pushRight - r.cx) pushLeft
                            else pushRight
                    }
                }
            }
        }
        val placed = rowed.map { r ->
            val feet = feetFor(r.frontness)
            Placed(r.spot.name, r.cx, feet, r.fh,
                WalkBand.bandForY(feet, h, inside),
                r.frontness, r.spot.mirror)
        }
        // output in the caller's own order (names are
        // unique in a crowd — the town never fields two)
        val byName = placed.associateBy { it.name }
        return Result(spots.map { byName.getValue(it.name) },
            fhBase, rowsUsed, winW / maxOf(1,
                (spots.size + rowsUsed - 1) / rowsUsed),
            winLeft, winW)
    }
}

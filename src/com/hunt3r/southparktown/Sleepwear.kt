package com.hunt3r.southparktown

/**
 * v0.12.0d: SLEEPWEAR — sleepover nights LOOK like
 * sleepover nights. Queue #12 item 4, closing the gap
 * v0.11.0c recorded ("Photos render normal clothes; there
 * is no sleepwear pass").
 *
 * The derivation, in Heroes' spirit exactly: a hero
 * costume is the kid's own sheet, caped — a costume code
 * ("cape|mask|emblem") frozen onto the PhotoFigure at
 * shutter time, and one KidArt pass renders it forever.
 * Sleepwear is the same shape, pointed at bedtime:
 *
 *  - THE CODE ("topHex|bottomHex|capFlag"): the pajama
 *    set's two colors and whether a nightcap rides.
 *    Both colors are SOFTENED readings of the figure's
 *    OWN sheet — the pajama top from their shirt color,
 *    the bottoms from their pants color — so every kid
 *    sleeps in their own palette, flattened and warmed
 *    the way night clothes read under a lamp. Nothing is
 *    authored per kid; the derivation is the design.
 *  - THE SPEC (sleepSpec): the figure's own sheet in
 *    pajamas — top and bottoms swapped for the softened
 *    pair, bottoms always pants, the day clothes'
 *    construction stripped (no placket, buttons, pockets,
 *    tie, backpack or scarf; hands bare), ONE trim: the
 *    collar band wears the bottoms' color, the way real
 *    pajama sets trim. Hats come OFF — the spec's own
 *    hair shows — except a NIGHTCAP for the kids whose
 *    day hat is a soft knit dome (see NIGHTCAP_HATS):
 *    their silhouette already carries a soft cap, so
 *    the night keeps one, in the pajama top's color with
 *    the bottoms' color as its single band. Solids only,
 *    everywhere: no dots, no checks, no repeated motif of
 *    any kind, in the art or in these tables — the
 *    trypophobia constraint is hard, and the suite scans
 *    this module for it.
 *  - THE FREEZE: at shutter time, a photo composed at
 *    Your House while a sleepover night is LIVE (the
 *    plan stands for tonight and the evening window has
 *    opened) freezes the code onto every sleepover
 *    figure it seats — the planned kid guests in frame
 *    and the player. Anywhere else, or at the house on
 *    any other terms, nothing freezes. The record is the
 *    truth from then on: album, street re-render and
 *    darkroom prints all draw the same pajamas through
 *    KidArt's sleep pass, and a record from before this
 *    build keeps exactly what it froze (nothing) — the
 *    pass is never retroactive.
 *  - THE WALLS: sleepwear exists only on those frozen
 *    figures — and, since v0.14.0a, in your mirror.
 *    Adults never render it (they are never
 *    sleepover guests — the freeze re-checks the kid
 *    bands itself, so even a tampered plan can't dress
 *    a grown-up); residents and visitors never freeze;
 *    and no other night system (patrols, finales,
 *    movies) borrows the pass. (v0.15.0a widens ONE
 *    wall honestly: on the live night itself, stepping
 *    OUT of the house in the evening window — the
 *    walk-home/escort window, while your guests are
 *    still up — you are out in what you're wearing, so
 *    the street and a street shutter see the pajamas
 *    too; see streetCode. Daytime never, a wardrobe
 *    outfit never: the pass stays derived, unpickable.)
 *  - THE MIRROR (v0.14.0a): the one place you can see
 *    your own night look is the glass at Your House —
 *    mirrorCode below reads the same live moment the
 *    shutter reads, so the reflection and the album
 *    can never disagree about what you wore to bed.
 *
 * Pure Kotlin (state reads only) — the suite pins the
 * whole derivation.
 */
object Sleepwear {

    /** The day hats whose silhouette honestly supports a
     *  nightcap: the soft knit domes. Under the derivation
     *  these kids keep a soft cap at night — floppy, in
     *  pajama colors. Every harder hat (caps, brims,
     *  crowns, toques) and every bare head simply shows
     *  the spec's own hair, hats off like bedtime means. */
    val NIGHTCAP_HATS = setOf(
        "beanie", "pomhat", "ushanka", "chullo", "trapper")

    /** The nightcap's hatId in the sleep spec — drawn by
     *  the ink stack as a perch (it is deliberately NOT a
     *  covering hat: the kid's own hair keeps its full
     *  mass under it, fringe and all). */
    const val NIGHTCAP_ID = "nightcap"

    /** Soften a hex for lamplight: each channel blended
     *  toward a warm pale neutral and lifted a touch —
     *  the same hue family, flattened. Deterministic;
     *  the suite pins provenance channel by channel. */
    fun softenHex(hex: String): String {
        val h = hex.removePrefix("#")
        if (h.length != 6) return hex
        val r = h.substring(0, 2).toIntOrNull(16) ?: return hex
        val g = h.substring(2, 4).toIntOrNull(16) ?: return hex
        val b = h.substring(4, 6).toIntOrNull(16) ?: return hex
        // the warm neutral night clothes fade toward
        fun soft(v: Int, target: Int) =
            (v * 0.62 + target * 0.38).toInt().coerceIn(0, 255)
        return "#%02x%02x%02x".format(
            soft(r, 216), soft(g, 203), soft(b, 188))
    }

    /** The code a spec's pajamas freeze as: its own top
     *  and bottom, softened, and the nightcap flag read
     *  off its day hat. */
    fun codeFromSpec(spec: SheetSpec): String {
        val cap = if (spec.hatId in NIGHTCAP_HATS) "cap" else "-"
        return "${softenHex(spec.top)}|${softenHex(spec.bottom)}|$cap"
    }

    /** A roster kid's sleepwear code, from their sheet. */
    fun codeFor(name: String): String {
        val spec = KidArt.SHEETS[name] ?: return ""
        return codeFromSpec(spec)
    }

    /** Your sleepwear code, from your creator appearance —
     *  your worn palette, softened, the same derivation
     *  (Heroes.playerCostumeCode's pattern). */
    fun playerCode(appearance: Map<String, String>): String =
        codeFromSpec(avatarSpec(appearance))

    /** Parse a sleepwear code; null when it isn't one. */
    fun parseCode(code: String): Triple<String, String, String>? {
        val parts = code.split("|")
        if (parts.size != 3) return null
        if (!parts[0].startsWith("#") || !parts[1].startsWith("#"))
            return null
        if (parts[2] != "cap" && parts[2] != "-") return null
        return Triple(parts[0], parts[1], parts[2])
    }

    /** THE SLEEP SPEC: [base] in its pajamas. The figure
     *  keeps everything that IS them — skin, hair, face,
     *  proportions, specials (a parka hood, crutches, a
     *  wheelchair stay exactly as the hero pass keeps
     *  them) — and changes only what bedtime changes:
     *  the clothes. A bad code changes nothing. */
    fun sleepSpec(base: SheetSpec, code: String): SheetSpec {
        val (top, bottom, cap) = parseCode(code) ?: return base
        return base.copy(
            top = top,
            bottom = bottom,
            bottomStyle = "pants",
            collarHex = bottom,
            placket = false, buttons = false, pockets = 0,
            tie = false, tieColor = bottom, collar = false,
            hatId = if (cap == "cap") NIGHTCAP_ID else "none",
            hatColor = top, hatAccent = bottom,
            backpack = false, backStyle = "none",
            scarfHex = null, handColor = null,
            shoeColor = darkerHex(bottom, 0.8f))
    }

    /** Is a sleepover night LIVE for a shutter at [locId]
     *  right now? The plan must stand for tonight, the
     *  photo must be at your house, and the evening must
     *  have opened — a noon selfie on a planned Friday is
     *  a day photo, pajama-free, however the night ends. */
    fun liveNow(state: GameState, locId: String): Boolean =
        locId == YOUR_HOUSE && eveningLive(state)

    /** v0.15.0a: the evening itself is live — the plan
     *  stands for tonight and the evening has opened.
     *  The window is the shutter's own, deliberately NOT
     *  narrowed: it opens at ARRIVE_FROM and runs to the
     *  day's end (the plan itself dies at rollover), the
     *  exact moment mirrorCode has read since v0.14.0a —
     *  the suite pins the glass still in pajamas at
     *  23:00. liveNow is this moment read at the house;
     *  the mirror, the previews and the street all read
     *  the same evening through here, so no surface can
     *  open the night earlier or close it sooner than
     *  another. */
    fun eveningLive(state: GameState): Boolean =
        Sleepovers.plannedTonight(state) &&
            state.timeMin >= Sleepovers.ARRIVE_FROM

    /** The code [fig] freezes with in a frame composed at
     *  [locId] right now, or "" — the shutter-side reader
     *  (Heroes.figureCode's shape, pointed at bedtime).
     *  Only two kinds of figure can freeze: you, and a
     *  planned guest who is a kid — re-checked here, so
     *  the walls hold however the plan was written. */
    fun figureCode(state: GameState, fig: PhotoFigure,
                   locId: String): String {
        if (fig.kind == "player") {
            // the house shutter (the original freeze) — or,
            // since v0.15.0a, a street shutter while you are
            // legitimately OUT in them (streetCode's window):
            // the camera abroad freezes exactly what the
            // street draws, never more, never less.
            if (liveNow(state, locId))
                return playerCode(state.agents.values
                    .firstOrNull { it.isPlayer }?.appearance
                    ?: emptyMap())
            return streetCode(state)
        }
        if (!liveNow(state, locId)) return ""
        if (fig.kind == "cast" &&
            fig.name in Sleepovers.plannedGuests(state) &&
            Sleepovers.isKidBand(fig.name))
            return codeFor(fig.name)
        return ""
    }

    /** v0.15.0a: YOUR PAJAMAS ON THE STREET — the code
     *  the street scene renders you in when you are
     *  legitimately out in your sleepwear: a sleepover
     *  night is LIVE (eveningLive — the plan standing,
     *  the evening open) and you are OUTDOORS, in the
     *  walk-home/escort window while the night is still
     *  up. Inside the house the mirror owns the look;
     *  indoors anywhere else you are a guest in a house,
     *  not a figure on a street. "" on every other terms:
     *  no plan, before the evening opens, the morning
     *  after, or indoors — and never in daytime, because
     *  the evening cannot open before 18:00. The code is
     *  playerCode read live — the same derivation the
     *  shutter freezes and the mirror shows. Sleepwear
     *  stays unpickable: no wardrobe piece and no creator
     *  option names it; it exists only as this derivation.
     *  Pure: rendering reads; it never writes. */
    fun streetCode(state: GameState): String {
        if (state.playerInside) return ""
        if (!eveningLive(state)) return ""
        val me = state.agents.values.firstOrNull { it.isPlayer }
            ?: return ""
        if (me.asleep) return ""
        return playerCode(me.appearance)
    }

    /** v0.14.0a: THE MIRROR'S CODE — what your reflection
     *  in the Your House mirror wears right now: your
     *  pajama code while a sleepover night is LIVE at
     *  the house, "" on every other terms (the mirror
     *  then shows the ordinary day look). The live
     *  moment is the shutter's own — liveNow over the
     *  house — and the code is figureCode's player
     *  branch by another door, so the glass, the album,
     *  the street re-render and the darkroom prints all
     *  agree figure-for-figure about your night look.
     *  Pure: the mirror renders; it never writes. */
    fun mirrorCode(state: GameState): String {
        if (!liveNow(state, YOUR_HOUSE)) return ""
        val me = state.agents.values.firstOrNull { it.isPlayer }
        return playerCode(me?.appearance ?: emptyMap())
    }
}

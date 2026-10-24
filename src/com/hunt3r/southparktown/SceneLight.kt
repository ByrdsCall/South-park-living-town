package com.hunt3r.southparktown

// ============================================================
// SceneLight — v0.3.0a, scene parity pass #3: exteriors & light.
//
// The town's light state is a PURE function of (timeMin,
// weatherId, season, locId) — no save state, no randomness —
// so the scene renderer (SceneArt), the interior window
// (InteriorArt) and the selfie backdrop (PhotoView) can all
// ask the same questions and never contradict each other:
//
//  - bandOf / levelForBand: when are the lights on, and how
//    far on (day 0, dusk partial, night full)?
//  - PaletteWear / wearHex: weather wear as MEASURED palette
//    transforms on the LOC_PALETTES data — rain darkens a
//    step, fog desaturates toward gray, snow whitens. The
//    base palette is never mutated; the transform recomputes
//    from it every frame, so clearing weather returns the
//    exact base color (the suite pins NO_WEAR identity over
//    every palette hex in the game).
//  - warmthOf / signGlowOf: each location's lit-window warmth
//    and sign glow (LOC_LIGHT in GameData).
//  - seasonTokensFor: the seasonal dressing registry
//    (LOC_SEASON_DETAILS) gated by season prefix.
// ============================================================
object SceneLight {

    // ---------------- day bands ----------------
    // day: 7-17, dusk: 18-20, night: 21-6. SceneArt.timeOfDay
    // delegates here so there is exactly one band table.
    fun bandOf(timeMin: Int): String {
        val h = (timeMin / 60) % 24
        return when (h) {
            in 7..17 -> "day"
            in 18..20 -> "dusk"
            else -> "night"
        }
    }

    /** How far the town's lights have come on: day 0 (off),
     *  dusk 0.65 (coming on), night 1 (full). */
    fun levelForBand(band: String): Float = when (band) {
        "dusk" -> 0.65f
        "night" -> 1f
        else -> 0f
    }

    fun lightLevel(timeMin: Int): Float = levelForBand(bandOf(timeMin))

    fun lightsOn(timeMin: Int): Boolean = lightLevel(timeMin) > 0f

    /** An alpha scaled by the current light level. */
    fun glowAlpha(base: Int, timeMin: Int): Int =
        (base * lightLevel(timeMin)).toInt().coerceIn(0, 255)

    // ---------------- weather wear ----------------
    /** A measured palette transform: desaturate toward the
     *  color's own luminance, darken by a channel multiplier,
     *  then whiten toward white — in that order, always. */
    data class PaletteWear(val darken: Float, val desat: Float, val whiten: Float)

    val NO_WEAR = PaletteWear(0f, 0f, 0f)

    fun wearFor(weatherId: String): PaletteWear = when (weatherId) {
        "rain" -> PaletteWear(darken = 0.14f, desat = 0.12f, whiten = 0f)
        "storm" -> PaletteWear(darken = 0.18f, desat = 0.15f, whiten = 0f)
        "fog" -> PaletteWear(darken = 0.02f, desat = 0.38f, whiten = 0.10f)
        "cloudy" -> PaletteWear(darken = 0.03f, desat = 0.10f, whiten = 0f)
        "wind" -> PaletteWear(darken = 0f, desat = 0.05f, whiten = 0f)
        "light_snow" -> PaletteWear(darken = 0f, desat = 0.03f, whiten = 0.10f)
        "heavy_snow" -> PaletteWear(darken = 0f, desat = 0.06f, whiten = 0.22f)
        "blizzard" -> PaletteWear(darken = 0f, desat = 0.18f, whiten = 0.38f)
        else -> NO_WEAR // sunny, partly_cloudy, anything unknown
    }

    private val wearCache = HashMap<String, String>()

    /** Apply [wear] to a "#rrggbb" hex. Pure and memoized —
     *  the draw loops call this per paint, like Paper's own
     *  caches (v0.1.9f discipline). NO_WEAR returns the input
     *  byte-exact, so unworn rendering is bit-identical. */
    fun applyWear(hex: String, wear: PaletteWear): String {
        if (wear == NO_WEAR) return hex
        val key = "$hex|${wear.darken}|${wear.desat}|${wear.whiten}"
        wearCache[key]?.let { return it }
        val v = hex.removePrefix("#").toInt(16)
        var r = (v shr 16 and 255).toFloat()
        var g = (v shr 8 and 255).toFloat()
        var b = (v and 255).toFloat()
        val gray = 0.299f * r + 0.587f * g + 0.114f * b
        r += (gray - r) * wear.desat
        g += (gray - g) * wear.desat
        b += (gray - b) * wear.desat
        val dk = 1f - wear.darken
        r *= dk; g *= dk; b *= dk
        r += (255f - r) * wear.whiten
        g += (255f - g) * wear.whiten
        b += (255f - b) * wear.whiten
        fun ch(x: Float): Int = (x + 0.5f).toInt().coerceIn(0, 255)
        val out = "#%02x%02x%02x".format(ch(r), ch(g), ch(b))
        wearCache[key] = out
        return out
    }

    fun wearHex(hex: String, weatherId: String): String =
        applyWear(hex, wearFor(weatherId))

    // ---------------- per-location light identity ----------------
    /** The lit-window warmth of a place (LOC_LIGHT). */
    fun warmthOf(locId: String): String = LOC_LIGHT[locId]?.warmthHex ?: "#f2c230"

    /** The night glow color of a place's sign, or null when
     *  the sign stays dark (most houses). */
    fun signGlowOf(locId: String): String? = LOC_LIGHT[locId]?.signGlowHex

    /** Exterior lamp count for a place (street/yard lamps the
     *  light layer draws; interiors read it for the window
     *  smudge — someone is out there under a lamp). */
    fun lampsOf(locId: String): Int = LOC_LIGHT[locId]?.lamps ?: 0

    // ---------------- seasonal dressing ----------------
    fun seasonPrefix(season: Season): String = when (season) {
        Season.SPRING -> "spring-"
        Season.SUMMER -> "summer-"
        Season.FALL -> "autumn-"
        Season.WINTER -> "winter-"
    }

    /** The seasonal dressing tokens a location wears in
     *  [season] — the registry (LOC_SEASON_DETAILS) is flat
     *  and season-prefixed; this filter is the gate the
     *  renderer and the suite share. */
    fun seasonTokensFor(locId: String, season: Season): List<String> {
        val prefix = seasonPrefix(season)
        return (LOC_SEASON_DETAILS[locId] ?: emptyList())
            .filter { it.startsWith(prefix) }
    }
}

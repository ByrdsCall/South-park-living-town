package com.hunt3r.southparktown

// ============================================================
// LoreBook — Lucky's South Park lorebook (3,322 entries,
// paraphrased from southpark.wiki.gg) repackaged as an app
// asset by tools/gen_lorebook.py and parsed here at runtime.
// Pure Kotlin, no android.* imports: the asset text is handed
// in by LoreLoad.ensureLoreLoaded(). Categories:
//   char = characters, loc = locations, ep = episodes,
//   spec = movies/specials, game = the games.
// ============================================================

data class LoreEntry(
    val name: String,
    val cat: String,
    val text: String,
    val keys: List<String>
)

object LoreBook {
    private var entries: List<LoreEntry> = emptyList()
    private var byNormName: Map<String, LoreEntry> = emptyMap()

    val loaded: Boolean get() = entries.isNotEmpty()
    val size: Int get() = entries.size

    /** Category display labels for the lorebook browser. */
    val CAT_LABELS: Map<String, String> = linkedMapOf(
        "char" to "👤 Characters",
        "loc" to "📍 Locations",
        "ep" to "📺 Episodes",
        "spec" to "🎬 Movies & Specials",
        "game" to "🎮 Games"
    )

    @Synchronized
    fun init(json: String) {
        if (entries.isNotEmpty()) return
        val out = mutableListOf<LoreEntry>()
        var i = 0
        while (true) {
            val nIdx = json.indexOf("\"n\":", i)
            if (nIdx < 0) break
            val name = GeminiClient.readJsonString(json, nIdx + 4) ?: break
            val cIdx = json.indexOf("\"c\":", nIdx)
            if (cIdx < 0) break
            val cat = GeminiClient.readJsonString(json, cIdx + 4) ?: break
            val tIdx = json.indexOf("\"t\":", cIdx)
            if (tIdx < 0) break
            val text = GeminiClient.readJsonString(json, tIdx + 4) ?: break
            val keys = mutableListOf<String>()
            val kIdx = json.indexOf("\"k\":[", tIdx)
            if (kIdx >= 0) {
                var j = kIdx + 5
                while (j < json.length && json[j] != ']') {
                    if (json[j] == '"') {
                        val k = GeminiClient.readJsonString(json, j) ?: break
                        keys.add(k)
                        // advance past the closing quote, escape-aware
                        var m = j + 1
                        while (m < json.length) {
                            if (json[m] == '\\') { m += 2; continue }
                            if (json[m] == '"') break
                            m++
                        }
                        j = m + 1
                    } else {
                        j++
                    }
                }
                i = j
            } else {
                i = tIdx + 4
            }
            out.add(LoreEntry(name, cat, text, keys))
        }
        entries = out
        byNormName = out.associateBy { norm(it.name) }
    }

    fun all(): List<LoreEntry> = entries

    fun byCat(cat: String): List<LoreEntry> = entries.filter { it.cat == cat }

    /** Exact entry lookup by display name (case/punctuation-insensitive). */
    fun entry(name: String): LoreEntry? = byNormName[norm(name)]

    /** Lorebook character entry for a game character name, via LoreAliases. */
    fun charLore(gameName: String): LoreEntry? =
        LORE_CHAR_ALIASES[gameName]?.let { entry(it) }

    /** Lorebook location entry for a game location id, via LoreAliases. */
    fun locLore(locId: String): LoreEntry? =
        LORE_LOC_ALIASES[locId]?.let { entry(it) }

    /** Keyword search across names, lorebook keys and text. */
    fun search(query: String, limit: Int = 120): List<LoreEntry> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val nameHits = mutableListOf<LoreEntry>()
        val keyHits = mutableListOf<LoreEntry>()
        val textHits = mutableListOf<LoreEntry>()
        for (e in entries) {
            when {
                e.name.lowercase().contains(q) -> nameHits.add(e)
                e.keys.any { it.contains(q) } -> keyHits.add(e)
                e.text.lowercase().contains(q) -> textHits.add(e)
            }
        }
        return (nameHits + keyHits + textHits).take(limit)
    }

    /** First [max] chars of [text], cut at a word/sentence boundary + "…". */
    fun excerpt(text: String, max: Int): String {
        if (text.length <= max) return text
        var cut = text.substring(0, max)
        val sentEnd = cut.lastIndexOf(". ")
        if (sentEnd > max / 2) return cut.substring(0, sentEnd + 1)
        val sp = cut.lastIndexOf(' ')
        if (sp > 0) cut = cut.substring(0, sp)
        return "$cut…"
    }

    /**
     * Canon-facts block appended to LLM dialogue prompts: lorebook text for
     * the given game characters + location. Empty string when nothing loads.
     */
    fun promptSuffix(gameCharNames: List<String>, locId: String?): String {
        if (!loaded) return ""
        val parts = mutableListOf<String>()
        for (n in gameCharNames.distinct()) {
            val e = charLore(n) ?: continue
            parts.add("${e.name}: ${excerpt(e.text, 480)}")
        }
        if (locId != null) {
            locLore(locId)?.let { parts.add("${it.name}: ${excerpt(it.text, 320)}") }
        }
        if (parts.isEmpty()) return ""
        return "\n\nLOREBOOK (canon facts — weave them in naturally, " +
            "never quote verbatim):\n" + parts.joinToString("\n")
    }

    /** Lorebook footnote appended to the Look view for a location. */
    fun lookSuffix(locId: String): String {
        val e = locLore(locId) ?: return ""
        return "\n\n📖 Lorebook — ${e.name}: ${excerpt(e.text, 420)}"
    }

    private fun norm(s: String): String {
        val sb = StringBuilder()
        for (ch in s.lowercase()) {
            if (ch in 'a'..'z' || ch in '0'..'9' || ch == ' ') sb.append(ch)
        }
        return sb.toString().trim()
    }
}

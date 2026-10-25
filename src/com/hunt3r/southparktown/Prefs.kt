package com.hunt3r.southparktown

// ============================================================
// Prefs — the save codec over SaveStore (the Android store is
// the "sptown" preferences file; see AndroidSaveStore).
// Hand-rolled tiny JSON (no Gson). Keys are stable — do not
// rename. Every key is declared in SaveKeys.kt (the Phase 3
// registry): store keys are referenced by const below, and the
// game_state doc keys are pinned against the registry by the
// suite in both directions.
// ============================================================

/** Minimal JSON writer/parser: maps, lists, strings, numbers, booleans, null. */
object Json {
    fun stringify(v: Any?): String = when (v) {
        null -> "null"
        is String -> quote(v)
        is Boolean -> if (v) "true" else "false"
        is Number -> v.toString()
        is Map<*, *> -> v.entries.joinToString(",", "{", "}") { (k, vv) ->
            quote(k.toString()) + ":" + stringify(vv)
        }
        is Collection<*> -> v.joinToString(",", "[", "]") { stringify(it) }
        else -> quote(v.toString())
    }

    fun quote(s: String): String {
        val sb = StringBuilder(s.length + 2)
        sb.append('"')
        for (ch in s) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (ch < ' ') sb.append("\\u%04x".format(ch.code)) else sb.append(ch)
            }
        }
        sb.append('"')
        return sb.toString()
    }

    fun parse(s: String): Any? {
        val p = Parser(s)
        p.ws()
        return p.value()
    }

    private class Parser(val s: String) {
        var i = 0
        // v0.1.9f robustness: recursion depth cap. Real saves nest
        // ~5 deep; a CORRUPTED value (bit rot, truncated write) can
        // be nothing but "[[[[[…" — unbounded recursion there threw
        // StackOverflowError, an Error the loaders' catch (Exception)
        // guards could NOT catch, i.e. a crash on a damaged save.
        // Past the cap we throw IllegalArgumentException instead,
        // which every caller already handles as "unreadable".
        private var depth = 0
        fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }
        fun expect(lit: String) {
            if (!s.startsWith(lit, i)) throw IllegalArgumentException("bad json at $i")
            i += lit.length
        }
        fun value(): Any? {
            ws()
            if (i >= s.length) return null
            return when (s[i]) {
                '{', '[' -> {
                    if (++depth > MAX_DEPTH)
                        throw IllegalArgumentException("json nested too deep")
                    val v = if (s[i] == '{') obj() else arr()
                    depth--
                    v
                }
                '"' -> str()
                't' -> { expect("true"); true }
                'f' -> { expect("false"); false }
                'n' -> { expect("null"); null }
                else -> num()
            }
        }
        fun obj(): Map<String, Any?> {
            i++ // {
            val m = linkedMapOf<String, Any?>()
            ws()
            if (i < s.length && s[i] == '}') { i++; return m }
            while (true) {
                ws()
                val k = str()
                ws()
                if (i >= s.length || s[i] != ':') throw IllegalArgumentException("expected : at $i")
                i++
                m[k] = value()
                ws()
                if (i >= s.length) throw IllegalArgumentException("unterminated obj")
                if (s[i] == '}') { i++; return m }
                if (s[i] != ',') throw IllegalArgumentException("expected , at $i")
                i++
            }
        }
        fun arr(): List<Any?> {
            i++ // [
            val l = mutableListOf<Any?>()
            ws()
            if (i < s.length && s[i] == ']') { i++; return l }
            while (true) {
                l.add(value())
                ws()
                if (i >= s.length) throw IllegalArgumentException("unterminated arr")
                if (s[i] == ']') { i++; return l }
                if (s[i] != ',') throw IllegalArgumentException("expected , at $i")
                i++
            }
        }
        fun str(): String {
            if (s[i] != '"') throw IllegalArgumentException("expected string at $i")
            i++
            val sb = StringBuilder()
            while (i < s.length) {
                val ch = s[i]
                when (ch) {
                    '"' -> { i++; return sb.toString() }
                    '\\' -> {
                        i++
                        if (i >= s.length) throw IllegalArgumentException("bad escape")
                        when (val e = s[i]) {
                            '"', '\\', '/' -> sb.append(e)
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'b' -> sb.append('\b')
                            'u' -> {
                                if (i + 4 >= s.length) throw IllegalArgumentException("bad \\u")
                                sb.append(s.substring(i + 1, i + 5).toInt(16).toChar())
                                i += 4
                            }
                            else -> sb.append(e)
                        }
                        i++
                    }
                    else -> { sb.append(ch); i++ }
                }
            }
            throw IllegalArgumentException("unterminated string")
        }
        fun num(): Number {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '-' || s[i] == '+' ||
                        s[i] == '.' || s[i] == 'e' || s[i] == 'E')) i++
            val tok = s.substring(start, i)
            return tok.toLongOrNull() ?: tok.toDoubleOrNull() ?: 0L
        }

        companion object { const val MAX_DEPTH = 100 }
    }
}

@Suppress("UNCHECKED_CAST")
class Prefs(private val store: SaveStore) {

    // ---- typed views over the string store (v0.18.0a: the
    // codec split — Prefs used to talk to the platform store
    // directly; every value now rides SaveStore as a string,
    // booleans as "true"/"false", ints as decimal, exactly the
    // shapes AndroidSaveStore stringifies back on read, so a
    // v0.17.0b save loads identically) ----
    private fun getString(key: String): String? = store.read(key)
    private fun getString(key: String, def: String): String = store.read(key) ?: def
    private fun getBoolean(key: String, def: Boolean): Boolean =
        store.read(key)?.toBoolean() ?: def
    private fun getInt(key: String, def: Int): Int =
        store.read(key)?.toIntOrNull() ?: def
    private fun putString(key: String, value: String) = store.write(key, value)
    private fun putBoolean(key: String, value: Boolean) =
        store.write(key, if (value) "true" else "false")
    private fun putInt(key: String, value: Int) = store.write(key, value.toString())
    private fun removeKey(key: String) = store.delete(key)

    // ---- stable keys ----
    fun getPlayerName(): String? = getString(SaveKeys.PLAYER_NAME)
    fun hasPlayer(): Boolean = getPlayerName() != null

    fun savePlayer(name: String, appearance: Map<String, String>) {
        putString(SaveKeys.PLAYER_NAME, name)
        putString(SaveKeys.PLAYER_APPEARANCE, Json.stringify(appearance))
    }

    fun getAppearance(): Map<String, String> {
        val raw = getString(SaveKeys.PLAYER_APPEARANCE) ?: return DEFAULT_APPEARANCE
        return try {
            val m = Json.parse(raw) as? Map<String, Any?> ?: return DEFAULT_APPEARANCE
            validateAppearance(m.mapValues { it.value.toString() })
        } catch (e: Exception) {
            DEFAULT_APPEARANCE
        }
    }

    // ---- creator preset slots (v0.1.9d, additive key) ----
    fun getCreatorPresets(): List<Map<String, String>?> =
        decodePresets(getString(SaveKeys.CREATOR_PRESETS))

    fun saveCreatorPreset(slot: Int, appearance: Map<String, String>) {
        if (slot !in 0 until CREATOR_PRESET_SLOTS) return
        val cur = getCreatorPresets().toMutableList()
        cur[slot] = validateAppearance(appearance)
        putString(SaveKeys.CREATOR_PRESETS, encodePresets(cur))
    }

    // Never logged. Ever.
    fun getGeminiKey(): String = getString(SaveKeys.GEMINI_KEY, "")
    fun setGeminiKey(k: String) {
        putString(SaveKeys.GEMINI_KEY, k.trim())
    }

    // ---- LLM provider selection + per-provider models (stable keys — do not rename) ----
    fun getLlmProvider(): String = getString(SaveKeys.LLM_PROVIDER, "gemini")
    fun setLlmProvider(p: String) {
        val v = if (p == "openrouter") "openrouter" else "gemini"
        putString(SaveKeys.LLM_PROVIDER, v)
    }

    fun getGeminiModel(): String =
        getString(SaveKeys.GEMINI_MODEL, LlmClient.DEFAULT_GEMINI_MODEL)
    fun setGeminiModel(m: String) {
        val v = m.trim().ifBlank { LlmClient.DEFAULT_GEMINI_MODEL }
        putString(SaveKeys.GEMINI_MODEL, v)
    }

    // Never logged. Ever.
    fun getOpenRouterKey(): String = getString(SaveKeys.OPENROUTER_KEY, "")
    fun setOpenRouterKey(k: String) {
        putString(SaveKeys.OPENROUTER_KEY, k.trim())
    }

    fun getOpenRouterModel(): String =
        getString(SaveKeys.OPENROUTER_MODEL, LlmClient.DEFAULT_OPENROUTER_MODEL)
    fun setOpenRouterModel(m: String) {
        val v = m.trim().ifBlank { LlmClient.DEFAULT_OPENROUTER_MODEL }
        putString(SaveKeys.OPENROUTER_MODEL, v)
    }

    /** Active LLM config, or null when the selected provider has no key (mock brain stays). */
    fun activeLlmConfig(): LlmConfig? = when (getLlmProvider()) {
        "openrouter" -> {
            val k = getOpenRouterKey()
            if (k.isBlank()) null
            else LlmConfig(LlmProvider.OPENROUTER, getOpenRouterModel(), k)
        }
        else -> {
            val k = getGeminiKey()
            if (k.isBlank()) null
            else LlmConfig(LlmProvider.GEMINI, getGeminiModel(), k)
        }
    }

    fun getLastPlayedDate(): String = getString(SaveKeys.LAST_PLAYED_DATE, "")

    /** Real-time clock sync. Default OFF = in-game clock (15-min ticks from 8 AM). */
    fun isRealtimeSync(): Boolean = getBoolean(SaveKeys.REALTIME_SYNC, false)
    fun setRealtimeSync(on: Boolean) {
        putBoolean(SaveKeys.REALTIME_SYNC, on)
    }

    // ---- v0.2.0b audio (additive keys — absent in old saves,
    // defaults = ON at modest volume; the sounds themselves are
    // synthesized at runtime, nothing ships in the APK) ----
    fun audioSettings(): AudioSettings = AudioSettings(
        music = getInt(SaveKeys.AUDIO_MUSIC, AudioSettings.DEFAULT.music),
        sfx = getInt(SaveKeys.AUDIO_SFX, AudioSettings.DEFAULT.sfx),
        ambience = getInt(SaveKeys.AUDIO_AMB, AudioSettings.DEFAULT.ambience),
        muted = getBoolean(SaveKeys.AUDIO_MUTED, false)
    )

    fun setAudioSettings(s: AudioSettings) {
        putInt(SaveKeys.AUDIO_MUSIC, s.music.coerceIn(0, 100))
        putInt(SaveKeys.AUDIO_SFX, s.sfx.coerceIn(0, 100))
        putInt(SaveKeys.AUDIO_AMB, s.ambience.coerceIn(0, 100))
        putBoolean(SaveKeys.AUDIO_MUTED, s.muted)
    }

    // ---- v0.4.0e greeting barks (additive key — absent in
    // old saves, defaults ON: townsfolk hail you by voice
    // when you walk into their space) ----
    fun barksEnabled(): Boolean = getBoolean(SaveKeys.AUDIO_BARKS, true)
    fun setBarksEnabled(on: Boolean) {
        putBoolean(SaveKeys.AUDIO_BARKS, on)
    }

    // ---- v0.16.0d diorama post pass (additive key — absent in
    // old saves, defaults ON: the seeded line "boil" of the
    // paper pipeline, a ±1px breathing of the scene's painted
    // backdrop on a 3-frame cycle; faces and UI never boil) ----
    fun boilEnabled(): Boolean = getBoolean(SaveKeys.POST_BOIL, true)
    fun setBoilEnabled(on: Boolean) {
        putBoolean(SaveKeys.POST_BOIL, on)
    }

    fun saveGame(state: GameState) {
        val doc = encodeGameState(state)
        val gameJson = Json.stringify(doc)
        // v0.1.9f perf: saveGame is called from ~45 sites, often
        // back-to-back with an unchanged state (chained dialogs).
        // Every key written here is a function of this one doc, so
        // if the doc is byte-identical to the last write, the whole
        // edit (and its async disk flush of the prefs file) is a
        // no-op worth skipping. Cache is process-wide; clearGame /
        // wipeAll reset it so a later save always lands.
        if (gameJson == lastGameJson) return
        lastGameJson = gameJson
        putString(SaveKeys.GAME_STATE, gameJson)
        putString(SaveKeys.FRIENDSHIP, Json.stringify(state.friendship))
        putString(SaveKeys.COLLECTED, Json.stringify(state.collected.toList()))
        putString(SaveKeys.LAST_PLAYED_DATE, state.lastPlayedDate)
        putString(SaveKeys.SPAWN_DATE, state.spawnDate)
    }

    // ---- v0.1.5 per-NPC character memory: its OWN key, zero risk to game_state ----
    fun saveNpcMemory() {
        try {
            val json = Json.stringify(NpcMemoryStore.serialize())
            if (json == lastMemoryJson) return // v0.1.9f: unchanged, skip the write
            lastMemoryJson = json
            putString(SaveKeys.NPC_MEMORY, json)
        } catch (e: Exception) {
            // memory is flavor; never break saving
        }
    }

    fun loadNpcMemory() {
        if (NpcMemoryStore.isLoaded) return
        try {
            val raw = getString(SaveKeys.NPC_MEMORY)
            if (raw == null) {
                NpcMemoryStore.markLoaded()
                return
            }
            val doc = Json.parse(raw) as? Map<String, Any?> ?: run {
                NpcMemoryStore.markLoaded(); return
            }
            NpcMemoryStore.deserialize(doc)
        } catch (e: Exception) {
            NpcMemoryStore.markLoaded()
        }
    }

    fun loadGame(): GameState? {
        val raw = getString(SaveKeys.GAME_STATE) ?: return null
        val doc = try {
            Json.parse(raw) as? Map<String, Any?>
        } catch (e: Exception) {
            null
        }
        if (doc != null) {
            try {
                return decodeGameState(doc)
            } catch (e: Exception) {
                // damaged mid-document: fall through to salvage
            }
        }
        // v0.1.9f: a save EXISTS but its main doc is unreadable.
        // Returning null here would start a fresh town whose first
        // save overwrites the surviving standalone keys — instead
        // salvage friendship / collection / dates from them.
        return recoverGameState(
            getString(SaveKeys.FRIENDSHIP),
            getString(SaveKeys.COLLECTED),
            getString(SaveKeys.LAST_PLAYED_DATE, ""),
            getString(SaveKeys.SPAWN_DATE, ""))
    }

    fun clearGame() {
        lastGameJson = null
        lastMemoryJson = null
        removeKey(SaveKeys.GAME_STATE)
    }

    fun wipeAll() {
        lastGameJson = null
        lastMemoryJson = null
        store.clear()
    }

    /** The format generation of the save on disk (for the Settings
     *  line): 0 = no save yet, -1 = a save exists but its doc is
     *  unreadable (it will be salvaged on load, not wiped). */
    fun storedSaveFormat(): Int {
        val raw = getString(SaveKeys.GAME_STATE) ?: return 0
        val doc = try {
            Json.parse(raw) as? Map<String, Any?>
        } catch (e: Exception) {
            null
        } ?: return -1
        return inferSaveFormat(doc)
    }

    companion object {
        // v0.1.9f write-dedupe caches (see saveGame).
        @Volatile private var lastGameJson: String? = null
        @Volatile private var lastMemoryJson: String? = null
    }
}


/**
 * v0.1.9f: the save-format generation this build WRITES.
 * 1 = the original doc (v0.1.0–0.1.4, core keys only);
 * 2 = the v0.1.5 additive doc (which kept growing keys through
 * v0.1.9e — episodes, npcGoals — without breaking old readers);
 * 3 = v0.1.9f: the first format that records itself, plus the
 * corrupt-save salvage path. Every format loads in later builds.
 */
const val SAVE_FORMAT_VERSION = 3

/** Which format generation a parsed game_state doc belongs to
 *  (0 = no doc / no save). Pure — the suite pins the eras. */
fun inferSaveFormat(doc: Map<String, Any?>?): Int {
    if (doc == null) return 0
    (doc["saveFormat"] as? Number)?.let { return it.toInt() }
    return if (doc.containsKey("playerInside")) 2 else 1
}

/** A location id from a save is only trusted if it names a real
 *  place; anything else (corruption, a removed location) lands
 *  at the Bus Stop instead of crashing the tick or the renderer
 *  on a `!!` lookup. */
private fun saneLoc(id: String?): String =
    id?.takeIf { it in LOCATIONS } ?: "BusStop"

/**
 * doc -> state conversion, extracted from Prefs.loadGame
 * (v0.1.9f) so it is PURE and the JVM suite can pin every era's
 * save shape through the real code: era docs load with defaults
 * for their missing keys, and corrupt VALUES degrade field by
 * field instead of killing the whole save.
 */

/** v0.2.0g: the exact doc saveGame persists, as a pure
 *  function of the state — extracted so the JVM suite can
 *  measure and round-trip the REAL save path (caps and
 *  all), not a hand-maintained copy of it. */
fun encodeGameState(state: GameState): Map<String, Any?> {
    val agents = linkedMapOf<String, Any?>()
    for ((name, a) in state.agents) {
        agents[name] = mapOf(
            "description" to a.description,
            "color" to a.color,
            "location" to a.location,
            "coords" to listOf(a.coords.first, a.coords.second),
            "waypoints" to a.waypoints.map { listOf(it.first, it.second) },
            "destination" to a.destination,
            "activity" to a.activity,
            "agenda" to a.agenda.mapValues { mapOf("location" to it.value.location, "activity" to it.value.activity) },
            "memories" to a.memories.takeLast(10),
            "chatCooldownUntil" to a.chatCooldownUntil,
            "isPlayer" to a.isPlayer,
            "appearance" to a.appearance,
            "asleep" to a.asleep
        )
    }
    return mapOf(
        // v0.1.9f: the doc records its own format generation
        "saveFormat" to SAVE_FORMAT_VERSION,
        "timeMin" to state.timeMin,
        "gameYear" to state.gameYear,
        "gameMonth" to state.gameMonth,
        "gameDay" to state.gameDay,
        "weatherId" to state.weatherId,
        "todayKey" to state.todayKey,
        "lastPlayedDate" to state.lastPlayedDate,
        "spawnDate" to state.spawnDate,
        "hasPlaced" to state.hasPlaced,
        "agents" to agents,
        "animals" to state.animals.map {
            mapOf("id" to it.id, "species" to it.species, "locationId" to it.locationId)
        },
        "events" to state.events.takeLast(40),
        "friendship" to state.friendship,
        "dailyGain" to state.dailyGain,
        "collected" to state.collected.toList(),
        "spawns" to state.spawns.map {
            mapOf("id" to it.id, "collectibleId" to it.collectibleId,
                "locationId" to it.locationId, "wiggled" to it.wiggled)
        },
        "lastConversation" to state.lastConversation.map {
            mapOf("speaker" to it.speaker, "text" to it.text)
        },
        // ---- v0.1.5 additive keys (absent in old saves -> defaults) ----
        "playerInside" to state.playerInside,
        "shenaniganId" to state.shenaniganId,
        "shenaniganDay" to state.shenaniganDay,
        "lastShenaniganId" to state.lastShenaniganId,
        "lastEventWeek" to state.lastEventWeek,
        "eventMonthKey" to state.eventMonthKey,
        "eventsThisMonth" to state.eventsThisMonth,
        "lastStageSeen" to state.lastStageSeen,
        "factionStates" to state.factionStates.mapValues {
            mapOf("influence" to it.value.influence,
                "playerStanding" to it.value.playerStanding,
                "mood" to it.value.mood,
                "moodScore" to it.value.moodScore)
        },
        "factionRelations" to state.factionRelations,
        "inventory" to state.inventory,
        "trashSearched" to state.trashSearched,
        "litter" to state.litter.map {
            mapOf("id" to it.id, "locId" to it.locId, "inside" to it.inside,
                "itemId" to it.itemId, "dayKey" to it.dayKey, "droppedBy" to it.droppedBy)
        },
        "npcBonds" to state.npcBonds,
        "questCounters" to state.questCounters,
        "scheduleDrift" to state.scheduleDrift.mapValues { it.value.toMap() },
        // ---- v0.1.8 additive keys (absent in old saves -> defaults) ----
        "absDay" to state.absDay,
        "holidayId" to state.holidayId,
        "participation" to state.participation,
        "eventWitness" to state.eventWitness,
        "relationPressure" to state.relationPressure,
        "locVisits" to state.locVisits,
        "convertedResidents" to state.convertedResidents.toList(),
        "townLog" to state.townLog.takeLast(30),
        "life" to state.life,
        "lifeText" to state.lifeText,
        "puddles" to state.puddles,
        "frozenPuddles" to state.frozenPuddles.toList(),
        // ---- v0.1.9c additive key (absent in old saves -> empty) ----
        "episodes" to state.episodes.map { it.toMap() },
        // ---- v0.1.9e additive key (absent in old saves -> empty) ----
        "npcGoals" to state.npcGoals.values.map { it.toMap() },
        // ---- v0.2.0c additive keys (absent in old saves -> empty) ----
        "photos" to state.photos.map { it.toMap() },
        "followers" to state.followers,
        "photographed" to state.photographed,
        "photoFeats" to state.photoFeats.toList(),
        // ---- v0.2.0e additive keys (absent in old saves -> empty) ----
        "fishLog" to state.fishLog.mapValues { it.value.toMap() },
        "fishCasts" to state.fishCasts,
        "showdown" to state.showdown.mapValues { it.value.toMap() },
        "tacoHigh" to state.tacoHigh,
        "tacoPlays" to state.tacoPlays,
        "gameFeats" to state.gameFeats.toList(),
        // ---- v0.2.0f additive keys (absent in old saves -> a fresh case) ----
        "arc" to mapOf(
            "chapter" to state.arcChapter,
            "due" to state.arcDueAbsDay,
            "started" to state.arcStarted,
            "retired" to state.arcRetired,
            "branch" to state.arcBranch,
            "choice" to state.arcChoice),
        "mysteryClues" to state.mysteryClues.toList(),
        // ---- v0.3.0f additive keys (absent in old saves -> the booth case waits for the pond case) ----
        "arc2" to mapOf(
            "due" to state.arc2DueAbsDay,
            "started" to state.arc2Started,
            "marathonDay" to state.arc2MarathonDay,
            "accuseDue" to state.arc2AccuseDueAbsDay,
            "accusationAired" to state.arc2AccusationAired,
            "retired" to state.arc2Retired,
            "branch" to state.arc2Branch,
            "accused" to state.arc2Accused,
            "declined" to state.arc2Declined,
            "usher" to state.arc2Usher,
            "alibiDay" to state.arc2AlibiDay,
            "retiredDay" to state.arc2RetiredDay,
            "reelFound" to state.arc2ReelFound,
            "flavor" to state.arc2FlavorSeen.toList()),
        "mystery2Clues" to state.mystery2Clues.toList(),
        // ---- v0.4.0f additive keys (absent in old saves -> the buried case waits for the booth case) ----
        "arc3" to mapOf(
            "chapter" to state.arc3Chapter,
            "due" to state.arc3DueAbsDay,
            "started" to state.arc3Started,
            "retired" to state.arc3Retired,
            "branch" to state.arc3Branch,
            "choice" to state.arc3Choice,
            "retiredDay" to state.arc3RetiredDay,
            "favor" to state.arc3Favor,
            "sharonPending" to state.arc3SharonPending,
            "sharonDay" to state.arc3SharonDay,
            "sharonText" to state.arc3SharonText),
        "mystery3Clues" to state.mystery3Clues.toList(),
        // ---- v0.6.0c additive keys (absent in old saves -> the gardener case waits for the buried case) ----
        "arc4" to mapOf(
            "chapter" to state.arc4Chapter,
            "due" to state.arc4DueAbsDay,
            "started" to state.arc4Started,
            "retired" to state.arc4Retired,
            "branch" to state.arc4Branch,
            "choice" to state.arc4Choice,
            "retiredDay" to state.arc4RetiredDay,
            "lastFixPending" to state.arc4LastFixPending,
            "lastFixDay" to state.arc4LastFixDay,
            "lastFixLanded" to state.arc4LastFixLanded),
        "mystery4Clues" to state.mystery4Clues.toList(),
        // ---- v0.7.0f additive keys (absent in old saves -> the confession case queues behind the gardener case) ----
        "arc5" to mapOf(
            "chapter" to state.arc5Chapter,
            "due" to state.arc5DueAbsDay,
            "started" to state.arc5Started,
            "retired" to state.arc5Retired,
            "branch" to state.arc5Branch,
            "choice" to state.arc5Choice,
            "retiredDay" to state.arc5RetiredDay,
            "lurePending" to state.arc5LurePending,
            "lureDay" to state.arc5LureDay,
            "lureReturned" to state.arc5LureReturned),
        "mystery5Clues" to state.mystery5Clues.toList(),
        // ---- v0.8.0f additive keys (absent in old saves -> the opinion case queues behind the confession case) ----
        "arc6" to mapOf(
            "chapter" to state.arc6Chapter,
            "due" to state.arc6DueAbsDay,
            "started" to state.arc6Started,
            "retired" to state.arc6Retired,
            "branch" to state.arc6Branch,
            "choice" to state.arc6Choice,
            "retiredDay" to state.arc6RetiredDay,
            "presented" to state.arc6Presented,
            "wallPending" to state.arc6WallPending,
            "wallDay" to state.arc6WallDay,
            "wallRestored" to state.arc6WallRestored),
        "mystery6Clues" to state.mystery6Clues.toList(),
        "arc6Gossip" to state.arc6Gossip.toList(),
        "arc6Voice" to state.arc6Voice.toMap(),
        // ---- v0.9.0f additive keys (absent in old saves -> the backwards case queues behind the opinion case) ----
        "arc7" to mapOf(
            "chapter" to state.arc7Chapter,
            "due" to state.arc7DueAbsDay,
            "started" to state.arc7Started,
            "retired" to state.arc7Retired,
            "branch" to state.arc7Branch,
            "choice" to state.arc7Choice,
            "retiredDay" to state.arc7RetiredDay,
            "casePending" to state.arc7CasePending,
            "caseDay" to state.arc7CaseDay,
            "caseBuilt" to state.arc7CaseBuilt),
        "mystery7Clues" to state.mystery7Clues.toList(),
        "arc7Commits" to state.arc7Commits.toMap(),
        // ---- v0.10.0f additive keys (absent in old saves -> the alibi case queues behind the backwards case) ----
        "arc8" to mapOf(
            "chapter" to state.arc8Chapter,
            "due" to state.arc8DueAbsDay,
            "started" to state.arc8Started,
            "retired" to state.arc8Retired,
            "branch" to state.arc8Branch,
            "choice" to state.arc8Choice,
            "retiredDay" to state.arc8RetiredDay,
            "letterPending" to state.arc8LetterPending,
            "letterDay" to state.arc8LetterDay,
            "letterBack" to state.arc8LetterBack),
        "mystery8Clues" to state.mystery8Clues.toList(),
        "arc8Pairs" to state.arc8Pairs.toMap(),
        // ---- v0.11.0f additive keys (absent in old saves -> the audit case queues behind the alibi case) ----
        "arc9" to mapOf(
            "chapter" to state.arc9Chapter,
            "due" to state.arc9DueAbsDay,
            "started" to state.arc9Started,
            "retired" to state.arc9Retired,
            "branch" to state.arc9Branch,
            "choice" to state.arc9Choice,
            "retiredDay" to state.arc9RetiredDay,
            "listPending" to state.arc9ListPending,
            "listDay" to state.arc9ListDay,
            "listRestored" to state.arc9ListRestored),
        "mystery9Clues" to state.mystery9Clues.toList(),
        "arc9Calls" to state.arc9Calls.toMap(),
        // ---- v0.12.0f additive keys (absent in old saves -> the photo case queues behind the audit case) ----
        "arc10" to mapOf(
            "chapter" to state.arc10Chapter,
            "due" to state.arc10DueAbsDay,
            "started" to state.arc10Started,
            "retired" to state.arc10Retired,
            "branch" to state.arc10Branch,
            "choice" to state.arc10Choice,
            "retiredDay" to state.arc10RetiredDay,
            "labelPending" to state.arc10LabelPending,
            "labelDay" to state.arc10LabelDay,
            "labelDone" to state.arc10LabelDone),
        "mystery10Clues" to state.mystery10Clues.toList(),
        "arc10Calls" to state.arc10Calls.toMap(),
        // ---- v0.13.0f additive keys (absent in old saves -> the voice case queues behind the photo case) ----
        "arc11" to mapOf(
            "chapter" to state.arc11Chapter,
            "due" to state.arc11DueAbsDay,
            "started" to state.arc11Started,
            "retired" to state.arc11Retired,
            "branch" to state.arc11Branch,
            "choice" to state.arc11Choice,
            "retiredDay" to state.arc11RetiredDay,
            "postPending" to state.arc11PostPending,
            "postDay" to state.arc11PostDay,
            "posted" to state.arc11Posted),
        "mystery11Clues" to state.mystery11Clues.toList(),
        "arc11Calls" to state.arc11Calls.toMap(),
        // ---- v0.14.0f additive keys (absent in old saves -> the returns case queues behind the voice case) ----
        "arc12" to mapOf(
            "chapter" to state.arc12Chapter,
            "due" to state.arc12DueAbsDay,
            "started" to state.arc12Started,
            "retired" to state.arc12Retired,
            "branch" to state.arc12Branch,
            "choice" to state.arc12Choice,
            "retiredDay" to state.arc12RetiredDay,
            "postPending" to state.arc12PostPending,
            "postDay" to state.arc12PostDay,
            "posted" to state.arc12Posted),
        "mystery12Clues" to state.mystery12Clues.toList(),
        "arc12Calls" to state.arc12Calls.toMap(),
        // ---- v0.15.0j additive keys (absent in old saves -> the exhibition case queues behind the returns case, and waits for its wall) ----
        "arc13" to mapOf(
            "chapter" to state.arc13Chapter,
            "due" to state.arc13DueAbsDay,
            "started" to state.arc13Started,
            "retired" to state.arc13Retired,
            "branch" to state.arc13Branch,
            "choice" to state.arc13Choice,
            "retiredDay" to state.arc13RetiredDay,
            "postPending" to state.arc13PostPending,
            "postDay" to state.arc13PostDay,
            "posted" to state.arc13Posted,
            "showId" to state.arc13ShowId),
        "mystery13Clues" to state.mystery13Clues.toList(),
        "arc13Calls" to state.arc13Calls.toMap(),
        // ---- v0.3.0c additive key (absent in old saves -> arcs start fresh) ----
        "storyArcs" to state.storyArcs.values.map { it.toMap() },
        // ---- v0.4.0b additive keys (absent in old saves -> an empty plate) ----
        "errands" to state.errands.map { it.toMap() },
        "errandAsked" to state.errandAsked,
        "errandsDone" to state.errandsDoneCount,
        "errandsRecent" to state.errandsRecent.map { it.toMap() },
        // ---- v0.5.0a additive key (absent in old saves -> no trips, no pass) ----
        "walk" to mapOf(
            "dest" to state.walkDest,
            "carry" to state.walkCarryMin,
            "trips" to state.walkTrips,
            "legs" to state.walkLegs,
            "shortcuts" to state.walkShortcuts.toList(),
            "busPass" to state.busPass),
        // ---- v0.5.0b additive key (absent in old saves -> a locked wardrobe) ----
        "wardrobe" to state.unlockedWardrobe.toList(),
        // ---- v0.5.0c additive key (absent in old saves -> an empty binder) ----
        "pocketPals" to state.pocketPals.filterKeys { it in PocketPals.CARD_IDS }
            .mapValues { it.value.coerceIn(0, PocketPals.COUNT_CAP) },
        // ---- v0.5.0e additive key (absent in old saves -> a
        // silent phone: no texts, no noted facts) ----
        "phone" to mapOf(
            "texts" to state.phoneTexts.takeLast(Phone.MAX_TEXTS).map { it.toMap() },
            "notes" to state.phoneNotes.takeLast(Phone.MAX_NOTES),
            "meta" to state.phoneMeta),
        // ---- v0.6.0a additive key (absent in old saves -> an
        // empty plate and no posting memory) ----
        "commissions" to mapOf(
            "plate" to state.commissions.map { it.toMap() },
            "last" to state.commissionLast,
            "done" to state.commissionsDone,
            "recent" to state.commissionRecent.map { it.toMap() }),
        // ---- v0.8.0d additive key (absent in old saves ->
        // no volumes yet; their photos belong to the current
        // school year) ----
        "yearbooks" to state.yearbooks.map { it.toMap() },
        "fairs" to state.fairs.map { it.toMap() },
        // ---- v0.9.0d additive key (absent in old saves ->
        // an empty print box; the darkroom corner was
        // always there, patient as furniture) ----
        "prints" to state.prints.map { it.toMap() },
        // ---- v0.15.0f additive key (absent in old saves ->
        // no shows on the record yet; the hall has hung
        // shows since before you moved in, but the book
        // only keeps what you were here for) ----
        "gallery" to state.galleryShows.map { it.toMap() }
    )
}

@Suppress("UNCHECKED_CAST")
fun decodeGameState(doc: Map<String, Any?>): GameState {
    val state = GameState()
    state.timeMin = ((doc["timeMin"] as? Number)?.toInt() ?: 480).coerceIn(0, 1439)
    state.gameYear = (doc["gameYear"] as? Number)?.toInt() ?: 2026
    state.gameMonth = ((doc["gameMonth"] as? Number)?.toInt() ?: 10).coerceIn(1, 12)
    state.gameDay = ((doc["gameDay"] as? Number)?.toInt() ?: 1).coerceIn(1, 31)
    state.weatherId = doc["weatherId"]?.toString() ?: "light_snow"
    state.todayKey = doc["todayKey"]?.toString() ?: ""
    // v0.1.9f: the engine slices todayKey unguarded (birthdayOf,
    // currentHoliday, onNewDay) — a short/corrupt key would crash
    // the very first tick. Rebuild it from the (already clamped)
    // date fields when it isn't a plausible yyyymmdd; well-formed
    // keys load byte-identical to before.
    val keyOk = state.todayKey.length == 8 && state.todayKey.all { it.isDigit() } &&
        ordinalOfKey(state.todayKey) != 0
    if (!keyOk) state.todayKey =
        "%04d%02d%02d".format(state.gameYear, state.gameMonth, state.gameDay)
    state.lastPlayedDate = doc["lastPlayedDate"]?.toString() ?: ""
    state.spawnDate = doc["spawnDate"]?.toString() ?: ""
    state.hasPlaced = doc["hasPlaced"] as? Boolean ?: false
    val agents = doc["agents"] as? Map<String, Any?> ?: emptyMap()
    for ((name, av) in agents) {
        val m = av as? Map<String, Any?> ?: continue
        val coords = (m["coords"] as? List<Any?>)?.map { ((it as? Number)?.toInt() ?: 0).coerceIn(0, 100) } ?: listOf(50, 52)
        val wps = (m["waypoints"] as? List<Any?>)?.mapNotNull { w ->
            val p = (w as? List<Any?>)?.map { (it as? Number)?.toInt() ?: 0 }
            if (p != null && p.size >= 2) p[0] to p[1] else null
        }?.toMutableList() ?: mutableListOf()
        val agendaRaw = m["agenda"] as? Map<String, Any?> ?: emptyMap()
        val agenda = agendaRaw.mapValues { (_, bv) ->
            val bm = bv as? Map<String, Any?> ?: emptyMap()
            AgendaBlock(saneLoc(bm["location"]?.toString()), bm["activity"]?.toString() ?: "")
        }.toMutableMap()
        val isPlayer = m["isPlayer"] as? Boolean ?: false
        val base = if (!isPlayer) AGENT_PROFILES[name]?.agenda?.toMap() ?: emptyMap() else emptyMap()
        val appRaw = m["appearance"] as? Map<String, Any?> ?: emptyMap()
        val agent = Agent(
            name = name,
            description = m["description"]?.toString() ?: "",
            color = m["color"]?.toString() ?: "#ffffff",
            location = saneLoc(m["location"]?.toString()),
            coords = (coords.getOrElse(0) { 50 }) to (coords.getOrElse(1) { 52 }),
            waypoints = wps,
            destination = saneLoc(m["destination"]?.toString()),
            activity = m["activity"]?.toString() ?: "",
            agenda = agenda,
            agendaBase = base,
            memories = ((m["memories"] as? List<Any?>)?.map { it.toString() } ?: emptyList()).toMutableList(),
            chatCooldownUntil = (m["chatCooldownUntil"] as? Number)?.toInt() ?: 0,
            isPlayer = isPlayer,
            appearance = validateAppearance(appRaw.mapValues { it.value.toString() }),
            asleep = m["asleep"] as? Boolean ?: false
        )
        state.agents[name] = agent
    }
    // Merge in characters added by app updates (e.g. v0.1.2's full cast).
    for ((name, p) in AGENT_PROFILES) {
        if (name in state.agents) continue
        val home = KID_HOUSE[name]?.takeIf { it in LOCATIONS } ?: "BusStop"
        val a = Agent(
            name = name, description = p.description, color = p.color,
            location = home, coords = LOCATIONS[home]!!.let { it.gx to it.gy },
            destination = home, activity = p.agenda["08:00"]?.activity ?: "",
            agenda = p.agenda.toMutableMap(), agendaBase = p.agenda.toMap()
        )
        a.memories.add("Just moved into town. South Park, baby!")
        state.agents[name] = a
        if (name !in state.friendship) state.friendship[name] = 0
    }
    val animals = doc["animals"] as? List<Any?> ?: emptyList()
    for (av in animals) {
        val m = av as? Map<String, Any?> ?: continue
        state.animals.add(Animal(
            m["id"]?.toString() ?: "a0",
            m["species"]?.toString() ?: "squirrel",
            m["locationId"]?.toString() ?: "KidsPark"))
    }
    state.events.addAll((doc["events"] as? List<Any?>)?.map { it.toString() } ?: emptyList())
    // v0.3.0g audit: the feed's cap holds on load too — a
    // hand-inflated doc can't resurrect an unbounded ticker.
    while (state.events.size > 40) state.events.removeAt(0)
    val fr = doc["friendship"] as? Map<String, Any?> ?: emptyMap()
    for ((k, v) in fr) state.friendship[k] = (v as? Number)?.toInt() ?: 0
    val dg = doc["dailyGain"] as? Map<String, Any?> ?: emptyMap()
    for ((k, v) in dg) state.dailyGain[k] = (v as? Number)?.toInt() ?: 0
    state.collected.addAll((doc["collected"] as? List<Any?>)?.map { it.toString() } ?: emptyList())
    val spawns = doc["spawns"] as? List<Any?> ?: emptyList()
    for (sv in spawns) {
        val m = sv as? Map<String, Any?> ?: continue
        state.spawns.add(Spawn(
            m["id"]?.toString() ?: "",
            m["collectibleId"]?.toString() ?: "",
            m["locationId"]?.toString() ?: "",
            m["wiggled"] as? Boolean ?: false))
    }
    state.lastConversation = (doc["lastConversation"] as? List<Any?>)?.mapNotNull { lv ->
        val m = lv as? Map<String, Any?> ?: return@mapNotNull null
        Line(m["speaker"]?.toString() ?: "?", m["text"]?.toString() ?: "")
    } ?: emptyList()
    // ---- v0.1.5 additive fields (all optional; defaults = old behavior) ----
    state.playerInside = doc["playerInside"] as? Boolean ?: false
    state.shenaniganId = doc["shenaniganId"]?.toString()
    state.shenaniganDay = doc["shenaniganDay"]?.toString() ?: ""
    state.lastShenaniganId = doc["lastShenaniganId"]?.toString()
    state.lastEventWeek = (doc["lastEventWeek"] as? Number)?.toInt() ?: -1
    state.eventMonthKey = doc["eventMonthKey"]?.toString() ?: ""
    state.eventsThisMonth = (doc["eventsThisMonth"] as? Number)?.toInt() ?: 0
    state.lastStageSeen = (doc["lastStageSeen"] as? Number)?.toInt() ?: -1
    (doc["factionStates"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val m = v as? Map<String, Any?> ?: return@forEach
        val fs = state.factionStates[k] ?: return@forEach
        fs.influence = (m["influence"] as? Number)?.toInt() ?: fs.influence
        fs.playerStanding = (m["playerStanding"] as? Number)?.toInt() ?: 0
        fs.mood = m["mood"]?.toString() ?: fs.mood
        fs.moodScore = (m["moodScore"] as? Number)?.toInt() ?: 0
    }
    (doc["factionRelations"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.factionRelations[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["inventory"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.inventory[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["trashSearched"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.trashSearched[k] = v?.toString() ?: ""
    }
    (doc["litter"] as? List<Any?>)?.forEach { lv ->
        val m = lv as? Map<String, Any?> ?: return@forEach
        state.litter.add(LitterDrop(
            m["id"]?.toString() ?: "", m["locId"]?.toString() ?: "",
            m["inside"] as? Boolean ?: false, m["itemId"]?.toString() ?: "",
            m["dayKey"]?.toString() ?: "", m["droppedBy"]?.toString() ?: ""))
    }
    (doc["npcBonds"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.npcBonds[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["questCounters"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.questCounters[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["scheduleDrift"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val inner = (v as? Map<String, Any?>)?.mapValues { it.value?.toString() ?: "" }
            ?: return@forEach
        state.scheduleDrift[k] = inner.toMutableMap()
    }
    // ---- v0.1.8 additive fields (all optional; defaults = old behavior) ----
    state.absDay = (doc["absDay"] as? Number)?.toInt() ?: 0
    state.holidayId = doc["holidayId"]?.toString()
    state.participation = (doc["participation"] as? Number)?.toInt() ?: 0
    state.eventWitness = doc["eventWitness"]?.toString()
    (doc["relationPressure"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.relationPressure[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["locVisits"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.locVisits[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["convertedResidents"] as? List<Any?>)?.forEach {
        it?.toString()?.let { n -> state.convertedResidents.add(n) }
    }
    (doc["townLog"] as? List<Any?>)?.forEach {
        it?.toString()?.let { n -> state.townLog.add(n) }
    }
    (doc["life"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.life[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["lifeText"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.lifeText[k] = v?.toString() ?: ""
    }
    (doc["puddles"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.puddles[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["frozenPuddles"] as? List<Any?>)?.forEach {
        it?.toString()?.let { n -> state.frozenPuddles.add(n) }
    }
    // ---- v0.1.9c additive field (absent in old saves -> none) ----
    (doc["episodes"] as? List<Any?>)?.forEach { ev ->
        (ev as? Map<String, Any?>)?.let { m ->
            Episodes.fromMap(m)?.let { state.episodes.add(it) }
        }
    }
    // ---- v0.8.0d additive field (absent in old saves -> none) ----
    (doc["yearbooks"] as? List<Any?>)?.forEach { yv ->
        (yv as? Map<String, Any?>)?.let { m ->
            YearbookVolume.fromMap(m)?.let { state.yearbooks.add(it) }
        }
    }
    // ---- v0.9.0c additive field (absent in old saves -> none) ----
    (doc["fairs"] as? List<Any?>)?.forEach { fv ->
        (fv as? Map<String, Any?>)?.let { m ->
            FairRecord.fromMap(m)?.let { state.fairs.add(it) }
        }
    }
    // ---- v0.9.0d additive field (absent in old saves -> none) ----
    (doc["prints"] as? List<Any?>)?.forEach { pv ->
        (pv as? Map<String, Any?>)?.let { m ->
            DarkroomPrint.fromMap(m)?.let { state.prints.add(it) }
        }
    }
    // ---- v0.15.0f additive field (absent in old saves -> none) ----
    (doc["gallery"] as? List<Any?>)?.forEach { gv ->
        (gv as? Map<String, Any?>)?.let { m ->
            GalleryShow.fromMap(m)?.let { state.galleryShows.add(it) }
        }
    }
    // ---- v0.1.9e additive field (absent in old saves -> none) ----
    (doc["npcGoals"] as? List<Any?>)?.forEach { gv ->
        (gv as? Map<String, Any?>)?.let { m ->
            NpcGoal.fromMap(m)?.let { state.npcGoals[it.npcKey] = it }
        }
    }
    // ---- v0.2.0c additive fields (absent in old saves -> none) ----
    (doc["photos"] as? List<Any?>)?.forEach { pv ->
        (pv as? Map<String, Any?>)?.let { m ->
            PhotoRecord.fromMap(m)?.let { state.photos.add(it) }
        }
    }
    state.followers = (doc["followers"] as? Number)?.toInt() ?: 0
    (doc["photographed"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.photographed[k] = (v as? Number)?.toInt() ?: 0
    }
    (doc["photoFeats"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> state.photoFeats.add(id) }
    }
    // ---- v0.2.0e additive fields (absent in old saves -> none) ----
    (doc["fishLog"] as? Map<String, Any?>)?.forEach { (k, v) ->
        (v as? Map<String, Any?>)?.let { state.fishLog[k] = FishRecord.fromMap(it) }
    }
    state.fishCasts = (doc["fishCasts"] as? Number)?.toInt() ?: 0
    (doc["showdown"] as? Map<String, Any?>)?.forEach { (k, v) ->
        (v as? Map<String, Any?>)?.let { state.showdown[k] = ShowdownRecord.fromMap(it) }
    }
    state.tacoHigh = (doc["tacoHigh"] as? Number)?.toInt() ?: 0
    state.tacoPlays = (doc["tacoPlays"] as? Number)?.toInt() ?: 0
    (doc["gameFeats"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> state.gameFeats.add(id) }
    }
    // ---- v0.2.0f additive fields (absent in old saves -> the case
    // simply hasn't opened; it schedules itself on the next roll) ----
    (doc["arc"] as? Map<String, Any?>)?.let { m ->
        state.arcChapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery.ARC_CHAPTERS.size)
        state.arcDueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arcStarted = m["started"] as? Boolean ?: false
        state.arcRetired = m["retired"] as? Boolean ?: false
        state.arcBranch = m["branch"]?.toString() ?: ""
        state.arcChoice = m["choice"]?.toString() ?: ""
    }
    (doc["mysteryClues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery.CLUE_IDS) state.mysteryClues.add(id) }
    }
    // ---- v0.3.0f additive fields (absent in old saves -> the
    // booth case simply hasn't been scheduled; it queues itself
    // behind the pond case on the next roll) ----
    (doc["arc2"] as? Map<String, Any?>)?.let { m ->
        state.arc2DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc2Started = m["started"] as? Boolean ?: false
        state.arc2MarathonDay = (m["marathonDay"] as? Number)?.toInt() ?: -1
        state.arc2AccuseDueAbsDay = (m["accuseDue"] as? Number)?.toInt() ?: -1
        state.arc2AccusationAired = m["accusationAired"] as? Boolean ?: false
        state.arc2Retired = m["retired"] as? Boolean ?: false
        state.arc2Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in setOf("", "solved", "wrong", "cold") } ?: ""
        state.arc2Accused = (m["accused"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery2.SUSPECT_KEYS } ?: ""
        state.arc2Declined = m["declined"] as? Boolean ?: false
        state.arc2Usher = m["usher"]?.toString() ?: ""
        state.arc2AlibiDay = (m["alibiDay"] as? Number)?.toInt() ?: -1
        state.arc2RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc2ReelFound = m["reelFound"] as? Boolean ?: false
        (m["flavor"] as? List<Any?>)?.forEach {
            it?.toString()?.let { n -> state.arc2FlavorSeen.add(n) }
        }
    }
    (doc["mystery2Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery2.CLUE2_IDS) state.mystery2Clues.add(id) }
    }
    // ---- v0.4.0f additive fields (absent in old saves -> the
    // buried case simply hasn't been scheduled; it queues
    // itself behind the booth case on the next roll) ----
    (doc["arc3"] as? Map<String, Any?>)?.let { m ->
        state.arc3Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery3.CHAPTERS3.size)
        state.arc3DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc3Started = m["started"] as? Boolean ?: false
        state.arc3Retired = m["retired"] as? Boolean ?: false
        state.arc3Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery3.BRANCH_KEYS + "" } ?: ""
        state.arc3Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery3.CHOICE_KEYS } ?: ""
        state.arc3RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc3Favor = m["favor"] as? Boolean ?: false
        state.arc3SharonPending = m["sharonPending"] as? Boolean ?: false
        state.arc3SharonDay = (m["sharonDay"] as? Number)?.toInt() ?: -1
        state.arc3SharonText = m["sharonText"]?.toString() ?: ""
    }
    (doc["mystery3Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery3.CLUE3_IDS) state.mystery3Clues.add(id) }
    }
    // ---- v0.6.0c additive fields (absent in old saves -> the
    // gardener case simply hasn't been scheduled; it queues
    // itself behind the buried case on the next roll) ----
    (doc["arc4"] as? Map<String, Any?>)?.let { m ->
        state.arc4Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery4.CHAPTERS4.size)
        state.arc4DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc4Started = m["started"] as? Boolean ?: false
        state.arc4Retired = m["retired"] as? Boolean ?: false
        state.arc4Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery4.BRANCH_KEYS + "" } ?: ""
        state.arc4Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery4.CHOICE_KEYS } ?: ""
        state.arc4RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc4LastFixPending = m["lastFixPending"] as? Boolean ?: false
        state.arc4LastFixDay = (m["lastFixDay"] as? Number)?.toInt() ?: -1
        state.arc4LastFixLanded = m["lastFixLanded"] as? Boolean ?: false
    }
    (doc["mystery4Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery4.CLUE4_IDS) state.mystery4Clues.add(id) }
    }
    // ---- v0.7.0f additive fields (absent in old saves -> the
    // confession case simply hasn't been scheduled; it queues
    // itself behind the gardener case on the next roll) ----
    (doc["arc5"] as? Map<String, Any?>)?.let { m ->
        state.arc5Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery5.CHAPTERS5.size)
        state.arc5DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc5Started = m["started"] as? Boolean ?: false
        state.arc5Retired = m["retired"] as? Boolean ?: false
        state.arc5Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery5.BRANCH_KEYS + "" } ?: ""
        state.arc5Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery5.CHOICE_KEYS } ?: ""
        state.arc5RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc5LurePending = m["lurePending"] as? Boolean ?: false
        state.arc5LureDay = (m["lureDay"] as? Number)?.toInt() ?: -1
        state.arc5LureReturned = m["lureReturned"] as? Boolean ?: false
    }
    (doc["mystery5Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery5.CLUE5_IDS) state.mystery5Clues.add(id) }
    }
    // ---- v0.8.0f additive fields (absent in old saves -> the
    // opinion case simply hasn't been scheduled; it queues
    // itself behind the confession case on the next roll).
    // The meter is never among them: it is derived from the
    // gossip set, the evidence set and the voice map, here.
    (doc["arc6"] as? Map<String, Any?>)?.let { m ->
        state.arc6Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery6.CHAPTERS6.size)
        state.arc6DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc6Started = m["started"] as? Boolean ?: false
        state.arc6Retired = m["retired"] as? Boolean ?: false
        state.arc6Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery6.BRANCH_KEYS + "" } ?: ""
        state.arc6Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery6.CHOICE_KEYS } ?: ""
        state.arc6RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc6Presented = m["presented"] as? Boolean ?: false
        state.arc6WallPending = m["wallPending"] as? Boolean ?: false
        state.arc6WallDay = (m["wallDay"] as? Number)?.toInt() ?: -1
        state.arc6WallRestored = m["wallRestored"] as? Boolean ?: false
    }
    (doc["mystery6Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery6.CLUE6_IDS) state.mystery6Clues.add(id) }
    }
    (doc["arc6Gossip"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery6.GOSSIP6_BY_ID) state.arc6Gossip.add(id) }
    }
    (doc["arc6Voice"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val stance = v?.toString() ?: ""
        if (k in Mystery6.GOSSIP6_BY_ID && stance in Mystery6.STANCE_KEYS)
            state.arc6Voice[k] = stance
    }
    // ---- v0.9.0f additive fields (absent in old saves -> the
    // seventh case simply hasn't queued yet) ----
    (doc["arc7"] as? Map<String, Any?>)?.let { m ->
        state.arc7Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery7.CHAPTERS7.size)
        state.arc7DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc7Started = m["started"] as? Boolean ?: false
        state.arc7Retired = m["retired"] as? Boolean ?: false
        state.arc7Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery7.BRANCH_KEYS + "" } ?: ""
        state.arc7Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery7.CHOICE_KEYS } ?: ""
        state.arc7RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc7CasePending = m["casePending"] as? Boolean ?: false
        state.arc7CaseDay = (m["caseDay"] as? Number)?.toInt() ?: -1
        state.arc7CaseBuilt = m["caseBuilt"] as? Boolean ?: false
    }
    (doc["mystery7Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery7.CLUE7_IDS) state.mystery7Clues.add(id) }
    }
    (doc["arc7Commits"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val pick = v?.toString() ?: ""
        if (k in Mystery7.MOVEMENT7_BY_ID && pick in Mystery7.PICK_KEYS)
            state.arc7Commits[k] = pick
    }
    // ---- v0.10.0f additive fields (absent in old saves -> the
    // eighth case simply hasn't queued yet) ----
    (doc["arc8"] as? Map<String, Any?>)?.let { m ->
        state.arc8Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery8.CHAPTERS8.size)
        state.arc8DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc8Started = m["started"] as? Boolean ?: false
        state.arc8Retired = m["retired"] as? Boolean ?: false
        state.arc8Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery8.BRANCH_KEYS + "" } ?: ""
        state.arc8Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery8.CHOICE_KEYS } ?: ""
        state.arc8RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc8LetterPending = m["letterPending"] as? Boolean ?: false
        state.arc8LetterDay = (m["letterDay"] as? Number)?.toInt() ?: -1
        state.arc8LetterBack = m["letterBack"] as? Boolean ?: false
    }
    (doc["mystery8Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery8.CLUE8_IDS) state.mystery8Clues.add(id) }
    }
    (doc["arc8Pairs"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val judgment = v?.toString() ?: ""
        if (k in Mystery8.PAIR8_BY_ID && judgment in Mystery8.JUDGE_KEYS)
            state.arc8Pairs[k] = judgment
    }
    // ---- v0.11.0f additive fields (absent in old saves -> the
    // ninth case simply hasn't queued yet) ----
    (doc["arc9"] as? Map<String, Any?>)?.let { m ->
        state.arc9Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery9.CHAPTERS9.size)
        state.arc9DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc9Started = m["started"] as? Boolean ?: false
        state.arc9Retired = m["retired"] as? Boolean ?: false
        state.arc9Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery9.BRANCH_KEYS + "" } ?: ""
        state.arc9Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery9.CHOICE_KEYS } ?: ""
        state.arc9RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc9ListPending = m["listPending"] as? Boolean ?: false
        state.arc9ListDay = (m["listDay"] as? Number)?.toInt() ?: -1
        state.arc9ListRestored = m["listRestored"] as? Boolean ?: false
    }
    (doc["mystery9Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery9.CLUE9_IDS) state.mystery9Clues.add(id) }
    }
    (doc["arc9Calls"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val call = v?.toString() ?: ""
        if (k in Mystery9.SITE9_BY_ID && call in Mystery9.CALL_KEYS)
            state.arc9Calls[k] = call
    }
    // ---- v0.12.0f additive fields (absent in old saves -> the
    // tenth case simply hasn't queued yet) ----
    (doc["arc10"] as? Map<String, Any?>)?.let { m ->
        state.arc10Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery10.CHAPTERS10.size)
        state.arc10DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc10Started = m["started"] as? Boolean ?: false
        state.arc10Retired = m["retired"] as? Boolean ?: false
        state.arc10Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery10.BRANCH_KEYS + "" } ?: ""
        state.arc10Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery10.CHOICE_KEYS } ?: ""
        state.arc10RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc10LabelPending = m["labelPending"] as? Boolean ?: false
        state.arc10LabelDay = (m["labelDay"] as? Number)?.toInt() ?: -1
        state.arc10LabelDone = m["labelDone"] as? Boolean ?: false
    }
    (doc["mystery10Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery10.CLUE10_IDS) state.mystery10Clues.add(id) }
    }
    (doc["arc10Calls"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val call = v?.toString() ?: ""
        if (k in Mystery10.FRAME10_BY_ID && call in Mystery10.CALL_KEYS)
            state.arc10Calls[k] = call
    }
    // ---- v0.13.0f additive fields (absent in old saves -> the
    // eleventh case simply hasn't queued yet) ----
    (doc["arc11"] as? Map<String, Any?>)?.let { m ->
        state.arc11Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery11.CHAPTERS11.size)
        state.arc11DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc11Started = m["started"] as? Boolean ?: false
        state.arc11Retired = m["retired"] as? Boolean ?: false
        state.arc11Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery11.BRANCH_KEYS + "" } ?: ""
        state.arc11Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery11.CHOICE_KEYS } ?: ""
        state.arc11RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc11PostPending = m["postPending"] as? Boolean ?: false
        state.arc11PostDay = (m["postDay"] as? Number)?.toInt() ?: -1
        state.arc11Posted = m["posted"] as? Boolean ?: false
    }
    (doc["mystery11Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery11.CLUE11_IDS) state.mystery11Clues.add(id) }
    }
    (doc["arc11Calls"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val call = v?.toString() ?: ""
        if (k in Mystery11.LETTER11_BY_ID && call in Mystery11.CALL_KEYS)
            state.arc11Calls[k] = call
    }
    // ---- v0.14.0f additive fields (absent in old saves -> the
    // twelfth case simply hasn't queued yet) ----
    (doc["arc12"] as? Map<String, Any?>)?.let { m ->
        state.arc12Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery12.CHAPTERS12.size)
        state.arc12DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc12Started = m["started"] as? Boolean ?: false
        state.arc12Retired = m["retired"] as? Boolean ?: false
        state.arc12Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery12.BRANCH_KEYS + "" } ?: ""
        state.arc12Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery12.CHOICE_KEYS } ?: ""
        state.arc12RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc12PostPending = m["postPending"] as? Boolean ?: false
        state.arc12PostDay = (m["postDay"] as? Number)?.toInt() ?: -1
        state.arc12Posted = m["posted"] as? Boolean ?: false
    }
    (doc["mystery12Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery12.CLUE12_IDS) state.mystery12Clues.add(id) }
    }
    (doc["arc12Calls"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val call = v?.toString() ?: ""
        if (k in Mystery12.ITEM12_BY_ID && call in Mystery12.CALL_KEYS)
            state.arc12Calls[k] = call
    }
    // ---- v0.15.0j additive fields (absent in old saves -> the
    // exhibition case queues behind the returns case) ----
    (doc["arc13"] as? Map<String, Any?>)?.let { m ->
        state.arc13Chapter = ((m["chapter"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Mystery13.CHAPTERS13.size)
        state.arc13DueAbsDay = (m["due"] as? Number)?.toInt() ?: -1
        state.arc13Started = m["started"] as? Boolean ?: false
        state.arc13Retired = m["retired"] as? Boolean ?: false
        state.arc13Branch = (m["branch"]?.toString() ?: "")
            .takeIf { it in Mystery13.BRANCH_KEYS + "" } ?: ""
        state.arc13Choice = (m["choice"]?.toString() ?: "")
            .takeIf { it.isEmpty() || it in Mystery13.CHOICE_KEYS } ?: ""
        state.arc13RetiredDay = (m["retiredDay"] as? Number)?.toInt() ?: -1
        state.arc13PostPending = m["postPending"] as? Boolean ?: false
        state.arc13PostDay = (m["postDay"] as? Number)?.toInt() ?: -1
        state.arc13Posted = m["posted"] as? Boolean ?: false
        state.arc13ShowId = m["showId"]?.toString() ?: ""
    }
    (doc["mystery13Clues"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Mystery13.CLUE13_IDS) state.mystery13Clues.add(id) }
    }
    (doc["arc13Calls"] as? Map<String, Any?>)?.forEach { (k, v) ->
        val call = v?.toString() ?: ""
        if (call in Mystery13.CALL_KEYS &&
            k.matches(Regex("frame_[1-6]")))
            state.arc13Calls[k] = call
    }
    // ---- v0.3.0c additive field (absent in old saves -> no arcs
    // yet; they open on the next midnight, fresh) ----
    (doc["storyArcs"] as? List<Any?>)?.forEach { av ->
        (av as? Map<String, Any?>)?.let { m ->
            StoryArcState.fromMap(m)?.let { state.storyArcs[it.arcId] = it }
        }
    }
    // ---- v0.4.0b additive fields (absent in old saves -> an
    // empty plate; decode holds the caps so a hand-inflated
    // doc can't resurrect an unbounded errand list) ----
    (doc["errands"] as? List<Any?>)?.forEach { ev ->
        (ev as? Map<String, Any?>)?.let { m ->
            Errand.fromMap(m)?.let { state.errands.add(it) }
        }
    }
    while (state.errands.size > Errands.STATE_CAP) {
        // shed completed records first — the plate is what matters
        val doneIdx = state.errands.indexOfFirst { it.done }
        if (doneIdx >= 0) state.errands.removeAt(doneIdx)
        else state.errands.removeAt(0)
    }
    (doc["errandAsked"] as? Map<String, Any?>)?.forEach { (k, v) ->
        state.errandAsked[k] = (v as? Number)?.toInt() ?: 0
    }
    state.errandsDoneCount = (doc["errandsDone"] as? Number)?.toInt() ?: 0
    (doc["errandsRecent"] as? List<Any?>)?.forEach { rv ->
        (rv as? Map<String, Any?>)?.let { m ->
            ErrandDoneNote.fromMap(m)?.let { state.errandsRecent.add(it) }
        }
    }
    while (state.errandsRecent.size > Errands.STATE_RECENT_CAP)
        state.errandsRecent.removeAt(0)
    // ---- v0.5.0a additive fields (absent in old saves -> the
    // town is unwalked and the bus is a stranger; every value
    // is clamped/filtered against the real graph so a
    // hand-inflated doc can't invent streets) ----
    (doc["walk"] as? Map<String, Any?>)?.let { m ->
        state.walkDest = (m["dest"]?.toString() ?: "")
            .takeIf { it in LOCATIONS } ?: ""
        state.walkCarryMin = ((m["carry"] as? Number)?.toInt() ?: 0)
            .coerceIn(0, Walk.TICK_MINUTES - 1)
        state.walkTrips = ((m["trips"] as? Number)?.toInt() ?: 0).coerceAtLeast(0)
        state.walkLegs = ((m["legs"] as? Number)?.toInt() ?: 0).coerceAtLeast(0)
        (m["shortcuts"] as? List<Any?>)?.forEach { raw ->
            val key = raw?.toString() ?: return@forEach
            val parts = key.split(">")
            if (parts.size == 2 && parts[1] in EXITS[parts[0]].orEmpty() &&
                state.walkShortcuts.size < Walk.SHORTCUT_CAP)
                state.walkShortcuts.add(key)
        }
        state.busPass = m["busPass"] as? Boolean ?: false
    }
    // ---- v0.5.0b additive field (absent in old saves -> the
    // wardrobe starts locked; decode-filtered to the Wardrobe
    // registry so a hand-inflated doc can't invent a piece) ----
    (doc["wardrobe"] as? List<Any?>)?.forEach {
        it?.toString()?.let { id -> if (id in Wardrobe.PIECE_IDS) state.unlockedWardrobe.add(id) }
    }
    // ---- v0.5.0c additive field (absent in old saves -> an
    // empty binder; decode-filtered to the PocketPals registry
    // and capped per card so a hand-inflated doc can't mint
    // a thousand dupes of the chase rare) ----
    (doc["pocketPals"] as? Map<String, Any?>)?.forEach { (k, v) ->
        if (k in PocketPals.CARD_IDS) {
            val n = ((v as? Number)?.toInt() ?: 0)
                .coerceIn(0, PocketPals.COUNT_CAP)
            if (n > 0) state.pocketPals[k] = n
        }
    }
    // ---- v0.5.0e additive key (absent in old saves -> a silent
    // phone; every record re-validated on the way in — unknown
    // senders/triggers/kinds are dropped, and the store's caps
    // hold against a hand-inflated doc) ----
    (doc["phone"] as? Map<String, Any?>)?.let { m ->
        (m["texts"] as? List<Any?>)?.forEach { tv ->
            (tv as? Map<String, Any?>)?.let { tm ->
                PhoneText.fromMap(tm)?.let { state.phoneTexts.add(it) }
            }
        }
        (m["notes"] as? List<Any?>)?.forEach { nv ->
            val raw = nv?.toString() ?: return@forEach
            val parts = raw.split("|")
            if (parts.size >= 3 && parts[0].toIntOrNull() != null &&
                parts[1] in Phone.NOTE_KINDS &&
                state.phoneNotes.size < Phone.MAX_NOTES)
                state.phoneNotes.add(raw)
        }
        (m["meta"] as? Map<String, Any?>)?.forEach { (k, v) ->
            state.phoneMeta[k] = ((v as? Number)?.toInt() ?: 0).coerceIn(-1, 1_000_000)
        }
        Phone.enforceCaps(state)
    }
    // ---- v0.6.0a additive key (absent in old saves -> an
    // empty plate; every record re-validated on the way in —
    // unknown askers/subjects/places are dropped by
    // Commission.fromMap, and the caps hold against a
    // hand-inflated doc) ----
    (doc["commissions"] as? Map<String, Any?>)?.let { m ->
        (m["plate"] as? List<Any?>)?.forEach { cv ->
            (cv as? Map<String, Any?>)?.let { cm ->
                Commission.fromMap(cm)?.let { state.commissions.add(it) }
            }
        }
        while (state.commissions.size > Commissions.STATE_CAP) {
            val doneIdx = state.commissions.indexOfFirst { it.done }
            if (doneIdx >= 0) state.commissions.removeAt(doneIdx)
            else state.commissions.removeAt(0)
        }
        (m["last"] as? Map<String, Any?>)?.forEach { (k, v) ->
            if (k in KID_ORDER) v?.toString()?.let { state.commissionLast[k] = it }
        }
        state.commissionsDone =
            ((m["done"] as? Number)?.toInt() ?: 0).coerceAtLeast(0)
        (m["recent"] as? List<Any?>)?.forEach { rv ->
            (rv as? Map<String, Any?>)?.let { rm ->
                CommissionNote.fromMap(rm)?.let { state.commissionRecent.add(it) }
            }
        }
        while (state.commissionRecent.size > Commissions.RECENT_CAP)
            state.commissionRecent.removeAt(0)
    }
    // ensure all kids exist in friendship (forward-compat)
    for (kid in KID_ORDER) state.friendship.putIfAbsent(kid, 0)
    return state
}

/**
 * Salvage path for a save whose main doc is unreadable: a fresh
 * default town (decode of an empty doc — full cast, defaults)
 * carrying the standalone keys that survive independently:
 * friendship, the collection, and the two dates. Each salvaged
 * value is parsed behind its own guard; garbage salvages to the
 * default, never to a crash.
 */
fun recoverGameState(friendshipJson: String?, collectedJson: String?,
                     lastPlayed: String, spawn: String): GameState {
    val state = decodeGameState(emptyMap())
    try {
        (Json.parse(friendshipJson ?: "") as? Map<String, Any?>)?.forEach { (k, v) ->
            state.friendship[k] = (v as? Number)?.toInt() ?: 0
        }
    } catch (e: Exception) { /* keep defaults */ }
    try {
        (Json.parse(collectedJson ?: "") as? List<Any?>)?.forEach {
            it?.toString()?.let { id -> state.collected.add(id) }
        }
    } catch (e: Exception) { /* keep defaults */ }
    state.lastPlayedDate = lastPlayed
    state.spawnDate = spawn
    return state
}

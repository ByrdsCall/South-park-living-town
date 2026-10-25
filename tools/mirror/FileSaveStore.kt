package com.hunt3r.southparktown

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/** The mirror's SaveStore (Restructure Order Part 3.6): persists
 *  to the SAME file the shim's FileSharedPreferences uses —
 *  <dataDir>/prefs/sptown.properties — in the SAME typed-prefix
 *  properties format, so a mirror save written before the codec
 *  split loads byte-identically after it, and the game's own
 *  AndroidSaveStore (running on the shim in the mirror) sees the
 *  same bytes on disk. Values are always written as S: strings
 *  (Prefs above the seam only ever moves strings); reads decode
 *  any typed prefix back to its plain string form. The file is
 *  re-read on every operation and rewritten whole on every
 *  mutation — the mirror's data file is small, and never caching
 *  keeps this store and the shim's store from drifting apart
 *  inside one process. */
class FileSaveStore(dir: File) : SaveStore {
    private val file = File(dir, "sptown.properties")

    private fun loadRaw(): Properties {
        val p = Properties()
        if (file.exists()) {
            try {
                FileInputStream(file).use { p.load(it) }
            } catch (e: Exception) {
                // a damaged mirror save starts fresh, like the shim's
            }
        }
        return p
    }

    private fun decode(enc: String): String? {
        if (enc.length < 2 || enc[1] != ':') return null
        val body = enc.substring(2)
        return when (enc[0]) {
            // the shim's own unescaping, mirrored exactly:
            // "\n" -> newline first, then "\\" -> "\"
            'S' -> body.replace("\\n", "\n").replace("\\\\", "\\")
            'B' -> if (body.equals("true", ignoreCase = true)) "true" else "false"
            else -> body
        }
    }

    private fun encode(value: String): String =
        "S:" + value.replace("\\", "\\\\").replace("\n", "\\n")

    @Synchronized
    override fun read(key: String): String? {
        val enc = loadRaw().getProperty(key) ?: return null
        return decode(enc)
    }

    @Synchronized
    override fun write(key: String, value: String) {
        val p = loadRaw()
        p.setProperty(key, encode(value))
        store(p)
    }

    @Synchronized
    override fun delete(key: String) {
        val p = loadRaw()
        p.remove(key)
        store(p)
    }

    @Synchronized
    override fun clear() {
        store(Properties())
    }

    private fun store(p: Properties) {
        try {
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { p.store(it, "sptown mirror prefs") }
        } catch (e: Exception) {
            System.err.println("prefs store failed: $e")
        }
    }
}

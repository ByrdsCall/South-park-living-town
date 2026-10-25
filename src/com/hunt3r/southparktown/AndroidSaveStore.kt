package com.hunt3r.southparktown

import android.content.Context

/** The Android SaveStore: the game's one SharedPreferences file
 *  ("sptown"), behind the string seam. read() goes through the
 *  untyped map and stringifies, so typed entries written by
 *  builds before the codec split (v0.17.0b and earlier wrote
 *  booleans and ints natively) load as exactly the strings the
 *  new codec writes: Boolean -> "true"/"false", numbers ->
 *  their decimal text. Every write is a plain string write. */
class AndroidSaveStore(context: Context) : SaveStore {
    private val sp = context.getSharedPreferences("sptown", Context.MODE_PRIVATE)

    override fun read(key: String): String? = when (val v = sp.all[key]) {
        null -> null
        is String -> v
        is Boolean -> if (v) "true" else "false"
        else -> v.toString()
    }

    override fun write(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    override fun delete(key: String) {
        sp.edit().remove(key).apply()
    }

    override fun clear() {
        sp.edit().clear().apply()
    }
}

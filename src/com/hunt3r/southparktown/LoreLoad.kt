package com.hunt3r.southparktown

import android.content.Context

// ============================================================
// LoreLoad — reads assets/lorebook.json once and hands it to
// the pure-Kotlin LoreBook. Safe to call from every Activity;
// LoreBook.init() is idempotent. Never crashes the caller.
// ============================================================
fun ensureLoreLoaded(ctx: Context) {
    if (LoreBook.loaded) return
    try {
        val json = ctx.assets.open("lorebook.json").bufferedReader().use { it.readText() }
        LoreBook.init(json)
    } catch (e: Exception) {
        // Lorebook is flavor, not core: play on without it.
    }
}

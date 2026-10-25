package com.hunt3r.southparktown

/** The mobile-platform seam (Restructure Order Part 3.6): the
 *  whole save system rides on four string operations, so Prefs —
 *  the codec above it — never knows which platform store sits
 *  underneath. AndroidSaveStore is the Android implementation;
 *  the desktop mirror's FileSaveStore is the other one, and the
 *  JVM suite drives Prefs through an in-memory fake. Pure
 *  Kotlin: no platform imports, by design and by suite pin. */
interface SaveStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun delete(key: String)
    fun clear()
}

package com.hunt3r.southparktown

/** Single source of version identity (D7). Everything else — the
 *  manifest, build_apk.sh, the suite's pins — reads from here. */
object Version {
    const val VERSION_CODE: Int = 127
    const val VERSION_NAME: String = "0.18.0a"
}

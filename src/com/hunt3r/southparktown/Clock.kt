package com.hunt3r.southparktown

import java.util.Calendar

// UI-layer clock: town time = device time. Engine stays android-free.
class SystemClock : Clock {
    private fun cal(): Calendar = Calendar.getInstance()
    override fun nowMinutes(): Int {
        val c = cal()
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }
    override fun todayKey(): String {
        val c = cal()
        return "%04d%02d%02d".format(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }
    override fun dayOfYear(): Int = cal().get(Calendar.DAY_OF_YEAR)
    override fun month(): Int = cal().get(Calendar.MONTH) + 1
    override fun hour(): Int = cal().get(Calendar.HOUR_OF_DAY)
}

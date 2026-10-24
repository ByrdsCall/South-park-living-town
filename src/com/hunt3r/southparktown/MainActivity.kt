package com.hunt3r.southparktown

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

// ============================================================
// MainActivity — launcher. Brief splash, then route:
// no saved kid -> CreatorActivity, else GameActivity.
// v0.1.9b: the title rides a crooked parchment plaque, like
// the games' title cards (chrome pass — routing unchanged).
// v0.15.0e: the plaque now frames the approved logo art —
// the wide town sign (assets/title_logo.png, drawn for the
// app and approved by Lucky) decoded from the bundled
// asset. The old lettered title (emoji + text) is gone;
// routing unchanged.
// ============================================================
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // v0.2.0b: start warming the synthesized sounds during the
        // splash (background thread — the routing doesn't wait)
        SoundEngine.init(this)
        SoundEngine.applySettings(Prefs(this).audioSettings())

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor(Paper.CHARCOAL))
        }
        val plaque = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = UiKit.paperCardBg(this@MainActivity, Paper.PARCHMENT, 10f)
            rotation = -2f
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        // v0.15.0e: the title IS the approved artwork — the
        // wide town-sign logo bundled at assets/title_logo.png
        // (1200x760), decoded from the asset and framed by the
        // plaque. Never redrawn in code: the suite hash-pins
        // the bundled bytes against the approved file.
        val logoBmp = try {
            assets.open("title_logo.png").use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) { null }
        if (logoBmp != null) {
            plaque.addView(ImageView(this).apply {
                setImageBitmap(logoBmp)
                adjustViewBounds = true
                contentDescription = "South Park Town"
            }, LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.84).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        root.addView(plaque, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(TextView(this).apply {
            text = "a fan-made toy"
            setTextColor(Color.parseColor(Paper.YELLOW))
            textSize = 14f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        })
        setContentView(root)

        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (Prefs(this).hasPlayer()) GameActivity::class.java
                       else CreatorActivity::class.java
            startActivity(Intent(this, next))
            finish()
        }, 800)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}

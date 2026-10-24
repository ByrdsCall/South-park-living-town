package com.hunt3r.southparktown

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

// ============================================================
// UiKit — Android-side builders for the Chrome design system
// (tokens + layout math: Chrome.kt). Everything is a
// code-drawn GradientDrawable on stock views: parchment
// cardstock panels with a darker edge, chunky game buttons,
// red title plaques — the Stick-of-Truth / Fractured But
// Whole menu language in the app's flat paper discipline.
// No assets, no copied artwork.
//
// paperDialog / showItemsDialog / showMessageDialog replace
// the stock AlertDialog look app-wide: same behavior (title,
// items, message, positive/negative), paper presentation.
// ============================================================
object UiKit {

    fun dp(ctx: Context, v: Float): Int =
        (v * ctx.resources.displayMetrics.density).toInt()

    /** Flat panel with rounded corners and a darker edge line. */
    fun panelBg(ctx: Context, fillHex: String, radiusDp: Float,
                strokeHex: String? = null, strokeDp: Float = Chrome.CARD_STROKE_DP,
                alpha: Int = 255): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(ctx, radiusDp).toFloat()
        setColor(Color.parseColor(fillHex))
        this.alpha = alpha
        val strokeColor: Int = when {
            strokeHex != null -> Color.parseColor(strokeHex)
            fillHex.equals(Paper.PARCHMENT, ignoreCase = true) ->
                Color.parseColor(Chrome.PARCHMENT_EDGE)
            else -> Paper.darker(fillHex, 0.62f)
        }
        setStroke(dp(ctx, strokeDp), strokeColor)
    }

    /** Parchment cardstock panel (Journal/Inventory/Lore cards). */
    fun paperCardBg(ctx: Context, fillHex: String = Paper.PARCHMENT,
                    radiusDp: Float = Chrome.CARD_RADIUS_DP): GradientDrawable =
        panelBg(ctx, fillHex, radiusDp)

    /** Themed plaque (quest chip, quest header band, dialog title). */
    fun plaqueBg(ctx: Context, fillHex: String,
                 radiusDp: Float = Chrome.PLAQUE_RADIUS_DP): GradientDrawable =
        panelBg(ctx, fillHex, radiusDp, strokeDp = Chrome.BUTTON_STROKE_DP)

    /** Chunky game-button background. */
    fun buttonBg(ctx: Context, fillHex: String,
                 radiusDp: Float = Chrome.BUTTON_RADIUS_DP): GradientDrawable =
        panelBg(ctx, fillHex, radiusDp, strokeDp = Chrome.BUTTON_STROKE_DP)

    /** Text-field background: cardstock fill, darker edge. */
    fun inputBg(ctx: Context, fillHex: String = Paper.PARCHMENT): GradientDrawable =
        panelBg(ctx, fillHex, Chrome.CARD_RADIUS_DP)

    /** Chunky game button: red primary, parchment secondary. */
    fun gameButton(ctx: Context, text: String, primary: Boolean = true,
                   onClick: () -> Unit): Button = Button(ctx).apply {
        this.text = text
        textSize = 14f
        typeface = android.graphics.Typeface.create(
            "casual", android.graphics.Typeface.BOLD)
        setTextColor(Color.parseColor(if (primary) "#ffffff" else Chrome.INK))
        background = buttonBg(ctx, if (primary) Paper.RED else Paper.PARCHMENT)
        setPadding(dp(ctx, 14f), dp(ctx, 10f), dp(ctx, 14f), dp(ctx, 10f))
        // v0.2.0b: every game button taps (synthesized, see SoundGen)
        setOnClickListener { SoundEngine.tap(); onClick() }
    }

    /** Screen title: big red casual-bold on the charcoal ground. */
    fun screenTitle(ctx: Context, text: String, colorHex: String = Paper.RED,
                    sizeSp: Float = 24f): TextView = TextView(ctx).apply {
        this.text = text
        setTextColor(Color.parseColor(colorHex))
        textSize = sizeSp
        typeface = android.graphics.Typeface.create(
            "casual", android.graphics.Typeface.BOLD)
    }

    // ---------------- paper dialogs ----------------

    /** The paper dialog shell: a parchment panel with a red
     *  title plaque (glued a hair crooked), caller content,
     *  then an optional button row. Dismisses before running a
     *  button action, like the stock dialogs did. */
    fun paperDialog(ctx: Context, title: String, content: View?,
                    positive: Pair<String, () -> Unit>? = null,
                    negative: Pair<String, () -> Unit>? = null): Dialog {
        val dlg = Dialog(ctx)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = paperCardBg(ctx, Paper.PARCHMENT, 12f)
            setPadding(dp(ctx, 16f), dp(ctx, 14f), dp(ctx, 16f), dp(ctx, 14f))
        }
        val plaque = TextView(ctx).apply {
            text = title
            setTextColor(Color.parseColor("#ffffff"))
            textSize = 17f
            typeface = android.graphics.Typeface.create(
                "casual", android.graphics.Typeface.BOLD)
            background = plaqueBg(ctx, Paper.RED, 6f)
            setPadding(dp(ctx, 12f), dp(ctx, 6f), dp(ctx, 12f), dp(ctx, 6f))
            rotation = Chrome.TILT_DEG
        }
        root.addView(plaque, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(ctx, 10f)
        })
        if (content != null) {
            root.addView(content, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        if (positive != null || negative != null) {
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
                setPadding(0, dp(ctx, 12f), 0, 0)
            }
            negative?.let { (label, act) ->
                row.addView(gameButton(ctx, label, primary = false) {
                    dlg.dismiss(); act()
                })
            }
            positive?.let { (label, act) ->
                row.addView(gameButton(ctx, label, primary = true) {
                    dlg.dismiss(); act()
                }, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    if (negative != null) marginStart = dp(ctx, 8f)
                })
            }
            root.addView(row)
        }
        dlg.setContentView(root)
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.window?.setLayout(
            ctx.resources.displayMetrics.widthPixels - dp(ctx, 36f),
            ViewGroup.LayoutParams.WRAP_CONTENT)
        return dlg
    }

    /** Message dialog on the paper kit. [content] wins over
     *  [message] when both are given. */
    fun showMessageDialog(ctx: Context, title: String, message: String? = null,
                          content: View? = null, positiveLabel: String? = "OK",
                          onPositive: (() -> Unit)? = null,
                          negativeLabel: String? = null) {
        val body: View? = content ?: message?.let { msg ->
            TextView(ctx).apply {
                text = msg
                setTextColor(Color.parseColor(Chrome.INK))
                textSize = 15f
            }
        }
        paperDialog(ctx, title, body,
            positive = positiveLabel?.let { it to (onPositive ?: {}) },
            negative = negativeLabel?.let { it to {} }).show()
    }

    /** v0.1.9c: the episode card — a full paper-chrome show card
     *  (title card / act-break sting / end-credits wrap-up /
     *  Journal episode page, all one presentation). The SOUTH
     *  PARK header is drawn text on the red plaque; the page body
     *  scrolls on the parchment; a single chunky red button is
     *  the only way out, so a card is never lost to a stray tap. */
    fun showEpisodeDialog(ctx: Context, page: EpisodePage, buttonLabel: String,
                          onButton: () -> Unit): Dialog {
        val dlg = Dialog(ctx)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setCancelable(false)
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = paperCardBg(ctx, Paper.PARCHMENT, 12f)
            setPadding(dp(ctx, 18f), dp(ctx, 16f), dp(ctx, 18f), dp(ctx, 16f))
        }
        val plaque = TextView(ctx).apply {
            text = "S O U T H   P A R K"
            setTextColor(Color.parseColor("#ffffff"))
            textSize = 15f
            typeface = android.graphics.Typeface.create(
                "casual", android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            background = plaqueBg(ctx, Paper.RED, 6f)
            setPadding(dp(ctx, 12f), dp(ctx, 7f), dp(ctx, 12f), dp(ctx, 7f))
            rotation = Chrome.TILT_DEG
        }
        root.addView(plaque, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(ctx, 12f)
        })
        val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        fun line(text: String, size: Float, colorHex: String, bold: Boolean,
                 center: Boolean, padTop: Float) {
            col.addView(TextView(ctx).apply {
                this.text = text
                setTextColor(Color.parseColor(colorHex))
                textSize = size
                typeface = android.graphics.Typeface.create(
                    "casual",
                    if (bold) android.graphics.Typeface.BOLD
                    else android.graphics.Typeface.NORMAL)
                if (center) gravity = Gravity.CENTER
                setPadding(0, dp(ctx, padTop), 0, 0)
            })
        }
        if (page.subLine.isNotEmpty()) line(page.subLine, 12.5f, Chrome.INK_SOFT, true, true, 0f)
        line(page.titleLine, 23f, Chrome.INK, true, true, 4f)
        if (page.starringLine.isNotEmpty()) {
            line(page.starringLine, 13f, "#a03028", true, true, 8f)
        }
        for (b in page.body) line(b, 14f, Chrome.INK, false, false, 7f)
        if (page.footer.isNotEmpty()) {
            line(page.footer, 13f, Chrome.INK_SOFT, true, true, 12f)
        }
        val scroll = ScrollView(ctx).apply { addView(col) }
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (ctx.resources.displayMetrics.heightPixels * 0.52f).toInt()))
        val btn = gameButton(ctx, buttonLabel, primary = true) {
            dlg.dismiss(); onButton()
        }
        root.addView(btn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(ctx, 14f)
        })
        dlg.setContentView(root)
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.window?.setLayout(
            ctx.resources.displayMetrics.widthPixels - dp(ctx, 28f),
            ViewGroup.LayoutParams.WRAP_CONTENT)
        return dlg
    }

    /** v0.2.0c: the selfie viewer — the photo re-rendered
     *  live from its record by PhotoView on the paper chrome,
     *  caption underneath, and a summary line (followers,
     *  feats, milestones) when the shot was just taken.
     *  Dismissable by tap-outside: it's a photo, not a card
     *  the story machine needs you to acknowledge. */
    fun showPhotoDialog(ctx: Context, record: PhotoRecord,
                        summary: String? = null,
                        buttonLabel: String = "Keep it 📌",
                        onClose: () -> Unit = {}): Dialog {
        val dlg = Dialog(ctx)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setCancelable(true)
        dlg.setCanceledOnTouchOutside(true)
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = paperCardBg(ctx, Paper.PARCHMENT, 12f)
            setPadding(dp(ctx, 14f), dp(ctx, 12f), dp(ctx, 14f), dp(ctx, 12f))
        }
        val w = ctx.resources.displayMetrics.widthPixels - dp(ctx, 56f)
        val photo = PhotoView(ctx).apply { this.record = record }
        root.addView(photo, LinearLayout.LayoutParams(w, (w * 0.92f).toInt()))
        root.addView(TextView(ctx).apply {
            text = record.caption
            setTextColor(Color.parseColor(Chrome.INK))
            textSize = 14.5f
            typeface = android.graphics.Typeface.create(
                "casual", android.graphics.Typeface.BOLD)
            setPadding(0, dp(ctx, 8f), 0, 0)
        })
        if (!summary.isNullOrEmpty()) {
            root.addView(TextView(ctx).apply {
                text = summary
                setTextColor(Color.parseColor("#a03028"))
                textSize = 13.5f
                setPadding(0, dp(ctx, 4f), 0, 0)
            })
        }
        val btn = gameButton(ctx, buttonLabel, primary = true) {
            dlg.dismiss()   // onClose fires via the dismiss listener
        }
        root.addView(btn, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(ctx, 12f)
        })
        dlg.setOnDismissListener { onClose() }
        dlg.setContentView(root)
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.window?.setLayout(
            ctx.resources.displayMetrics.widthPixels - dp(ctx, 28f),
            ViewGroup.LayoutParams.WRAP_CONTENT)
        return dlg
    }

    /** Item-picker dialog on the paper kit: one chunky
     *  parchment button per item (scrolls when the list is
     *  long — gift bags get big). Same contract as the stock
     *  setItems dialogs: tap an item, we dismiss, you get the
     *  index. */
    fun showItemsDialog(ctx: Context, title: String, items: Array<String>,
                        onPick: (Int) -> Unit) {
        val list = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        lateinit var dlg: Dialog
        items.forEachIndexed { i, label ->
            val b = Button(ctx).apply {
                text = label
                textSize = 15f
                gravity = Gravity.CENTER_VERTICAL
                typeface = android.graphics.Typeface.create(
                    "casual", android.graphics.Typeface.BOLD)
                setTextColor(Color.parseColor(Chrome.INK))
                background = buttonBg(ctx, Paper.PARCHMENT)
                setPadding(dp(ctx, 12f), dp(ctx, 10f), dp(ctx, 12f), dp(ctx, 10f))
                setOnClickListener { SoundEngine.select(); dlg.dismiss(); onPick(i) }
            }
            list.addView(b, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                if (i < items.size - 1) bottomMargin = dp(ctx, 8f)
            })
        }
        val body: View = if (items.size > 6) {
            ScrollView(ctx).apply {
                addView(list)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (ctx.resources.displayMetrics.heightPixels * 0.5f).toInt())
            }
        } else list
        dlg = paperDialog(ctx, title, body)
        dlg.show()
    }
}

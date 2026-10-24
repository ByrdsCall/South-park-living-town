package com.hunt3r.southparktown

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.text.StaticLayout
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView

// ============================================================
// DialogueSheet — conversation as stacked South-Park speech
// bubbles. Framework DialogFragment (no Material dependency)
// pinned to the bottom like a bottom sheet.
// ============================================================

/** One paper speech bubble: white fill, 3px black stroke, tail, kid-color name header. */
class BubbleView(
    context: Context,
    private val speaker: String,
    private val text: String,
    private val colorHex: String,
    private val alignRight: Boolean
) : View(context) {
    private val namePaint = Paper.textPaint(colorHex, 15f, bold = true, casual = true)
    private val bodyPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1a1a1a")
        textSize = 15f
    }
    private var bodyLayout: StaticLayout? = null
    private var nameWidth = 0f
    private val pad = 18f
    private val tailH = 16f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val textW = (w - pad * 2 - 16).toInt().coerceAtLeast(50)
        nameWidth = namePaint.measureText("$speaker:")
        bodyLayout = StaticLayout.Builder.obtain(text, 0, text.length, bodyPaint, textW).build()
        val h = (pad + 24 + (bodyLayout?.height ?: 0) + tailH + pad).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val rect = RectF(4f, 4f, w - 4f, h - tailH)
        // shadow
        c.drawRoundRect(RectF(rect.left + 3, rect.top + 4, rect.right + 3, rect.bottom + 4),
            14f, 14f, Paper.fillPaint("#000000", 80))
        // tail
        val tailX = if (alignRight) rect.right - 70 else rect.left + 60
        c.drawPath(Path().apply {
            moveTo(tailX, rect.bottom - 2)
            lineTo(tailX + 20, rect.bottom + tailH)
            lineTo(tailX + 40, rect.bottom - 2)
            close()
        }, Paper.fillPaint(Chrome.PAPER_WHITE))
        // bubble (v0.1.9b: warm paper-white fill)
        c.drawRoundRect(rect, 14f, 14f, Paper.fillPaint(Chrome.PAPER_WHITE))
        c.drawRoundRect(rect, 14f, 14f, Paper.strokePaint("#000000", 3f))
        // name header + the games' speaker color bar under it
        // (bar at +18..+21, body starts at +26 — pinned, no overlap)
        c.drawText("$speaker:", rect.left + pad, rect.top + pad + 14, namePaint)
        c.drawRect(rect.left + pad, rect.top + pad + 18,
            rect.left + pad + nameWidth, rect.top + pad + 21,
            Paper.fillPaint(colorHex))
        // body
        val layout = bodyLayout ?: return
        c.save()
        c.translate(rect.left + pad, rect.top + pad + 26)
        layout.draw(c)
        c.restore()
    }
}

class DialogueSheet : DialogFragment() {

    var onSend: (String) -> Unit = {}
    /** v0.1.9c: the host checks for queued episode cards when a
     *  conversation closes (a named safe moment for stings). */
    var onDismissed: () -> Unit = {}
    var speakerColors: Map<String, String> = emptyMap()
    var playerName: String = PLAYER_DEFAULT_NAME
    var alignPlayerRight: Boolean = true
    /** Lines shown immediately when the sheet opens (e.g. tapped bubble). */
    var initialLines: List<Line> = emptyList()

    private lateinit var bubbleBox: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private var typingBubble: BubbleView? = null

    companion object {
        fun newInstance(title: String): DialogueSheet {
            val f = DialogueSheet()
            val b = Bundle()
            b.putString("title", title)
            f.arguments = b
            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = activity
        val dialog = Dialog(ctx)
        dialog.setCanceledOnTouchOutside(true)

        val res = ctx.resources
        fun dp(v: Float) = Paper.dp(res, v)

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor(Paper.CHARCOAL))
            setPadding(dp(12f).toInt(), dp(12f).toInt(), dp(12f).toInt(), dp(12f).toInt())
        }

        // v0.1.9b: the sheet title rides a crooked parchment plaque
        val titleView = android.widget.TextView(ctx).apply {
            text = arguments?.getString("title") ?: "Yap session"
            setTextColor(android.graphics.Color.parseColor(Paper.RED))
            textSize = 20f
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            background = UiKit.plaqueBg(ctx, Paper.PARCHMENT)
            rotation = Chrome.TILT_DEG
            setPadding(dp(12f).toInt(), dp(6f).toInt(), dp(12f).toInt(), dp(6f).toInt())
        }
        val titleLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        titleLp.bottomMargin = dp(10f).toInt()
        root.addView(titleView, titleLp)

        bubbleBox = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        scroll = ScrollView(ctx).apply {
            addView(bubbleBox, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val scrollLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        root.addView(scroll, scrollLp)

        val inputRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8f).toInt(), 0, 0)
        }
        input = EditText(ctx).apply {
            hint = "Say something, dude..."
            setHintTextColor(android.graphics.Color.parseColor("#8a8a8a"))
            setTextColor(android.graphics.Color.parseColor("#1a1a1a"))
            background = UiKit.inputBg(ctx, Paper.PARCHMENT)
            setPadding(dp(10f).toInt(), dp(10f).toInt(), dp(10f).toInt(), dp(10f).toInt())
        }
        val inputLp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        inputRow.addView(input, inputLp)
        val send = Button(ctx).apply {
            text = "Send"
            setTextColor(android.graphics.Color.parseColor("#ffffff"))
            background = UiKit.buttonBg(ctx, Paper.RED)
            typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.BOLD)
            setOnClickListener {
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) {
                    input.setText("")
                    onSend(t)
                }
            }
        }
        inputRow.addView(send, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(inputRow)

        dialog.setContentView(root)
        for (l in initialLines) addLine(l)
        val win = dialog.window
        if (win != null) {
            win.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                (res.displayMetrics.heightPixels * 0.72).toInt())
            win.setGravity(Gravity.BOTTOM)
            win.setBackgroundDrawableResource(android.R.color.transparent)
        }
        @Suppress("DEPRECATION")
        dialog.setOnShowListener {
            // keep input above keyboard
            win?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        return dialog
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissed()
    }

    fun addLine(line: Line) {
        // v0.2.0b: a voice blip per bubble — pitch family by
        // speaker (Kenny's is muffled through the parka, of course)
        // v0.4.0e: the blip bends with the line's own mood —
        // excited rides higher/faster, sad drags, angry sharpens
        // (SoundGen.moodOfLine reads the line's words as written).
        if (line.speaker != "…") {
            SoundEngine.blip(line.speaker, line.speaker == playerName,
                SoundGen.moodOfLine(line.speaker, line.text))
        }
        val color = speakerColors[line.speaker]
            ?: if (line.speaker == playerName) PLAYER_COLOR else "#1a1a1a"
        val right = alignPlayerRight && line.speaker == playerName
        val bv = BubbleView(activity, line.speaker, line.text, color, right)
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = Paper.dp(activity.resources, 6f).toInt()
        bubbleBox.addView(bv, lp)
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    fun addLines(lines: List<Line>) {
        for (l in lines) addLine(l)
    }

    fun setTyping(show: Boolean) {
        typingBubble?.let {
            bubbleBox.removeView(it)
            typingBubble = null
        }
        if (show) {
            val bv = BubbleView(activity, "…", "…", "#8a8a8a", false)
            bubbleBox.addView(bv)
            typingBubble = bv
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    fun scrollToBottom() {
        if (::scroll.isInitialized) scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }
}

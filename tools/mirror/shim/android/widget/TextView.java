package android.widget;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;
// Desktop-mirror shim (internal QA tooling only). TextView as a state
// holder: the mirror does not run the Android widget layout pass, so a
// TextView's job here is to carry the game's text state faithfully.
public class TextView extends View {
    public enum BufferType { NORMAL, SPANNABLE, EDITABLE }
    private CharSequence text = "";
    private float textSize = 14f;
    private int textColor = 0xFF000000;
    private Typeface typeface = Typeface.DEFAULT;
    private int gravity = 0x03 | 0x10;
    private int maxLines = Integer.MAX_VALUE;
    private int minLines = 0;
    private int lines = 0;
    private float lineSpacingExtra = 0f, lineSpacingMultiplier = 1f;
    private boolean singleLine = false;
    private CharSequence hint = "";
    private boolean includeFontPadding = true;
    private float letterSpacing = 0f;
    public TextView(Context context) { super(context); }
    public TextView(Context context, AttributeSet attrs) { super(context, attrs); }
    public TextView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
    public CharSequence getText() { return text; }
    public void setText(CharSequence text) { this.text = text == null ? "" : text; }
    public void setText(CharSequence text, BufferType type) { setText(text); }
    public void setText(int resId) { setText(getContext().getString(resId)); }
    public void setText(char[] text, int start, int len) { setText(new String(text, start, len)); }
    public void append(CharSequence text) { this.text = String.valueOf(this.text) + text; }
    public int length() { return text.length(); }
    public float getTextSize() { return textSize; }
    public void setTextSize(float size) { textSize = size; }
    public void setTextSize(int unit, float size) { textSize = size; }
    public void setTextColor(int color) { textColor = color; }
    public int getCurrentTextColor() { return textColor; }
    public void setHintTextColor(int color) {}
    public void setLinkTextColor(int color) {}
    public void setTypeface(Typeface tf) { typeface = tf == null ? Typeface.DEFAULT : tf; }
    public void setTypeface(Typeface tf, int style) {
        typeface = tf == null ? Typeface.create((Typeface) null, style) : Typeface.create(tf, style);
    }
    public Typeface getTypeface() { return typeface; }
    public int getGravity() { return gravity; }
    public void setGravity(int gravity) { this.gravity = gravity; }
    public void setMaxLines(int maxLines) { this.maxLines = maxLines; }
    public int getMaxLines() { return maxLines; }
    public void setMinLines(int minLines) { this.minLines = minLines; }
    public void setLines(int lines) { this.lines = lines; }
    public void setSingleLine() { singleLine = true; }
    public void setSingleLine(boolean singleLine) { this.singleLine = singleLine; }
    public void setLineSpacing(float add, float mult) { lineSpacingExtra = add; lineSpacingMultiplier = mult; }
    public float getLineSpacingMultiplier() { return lineSpacingMultiplier; }
    public float getLineSpacingExtra() { return lineSpacingExtra; }
    public CharSequence getHint() { return hint; }
    public void setHint(CharSequence hint) { this.hint = hint == null ? "" : hint; }
    public void setHint(int resId) { setHint(getContext().getString(resId)); }
    public boolean getIncludeFontPadding() { return includeFontPadding; }
    public void setIncludeFontPadding(boolean include) { includeFontPadding = include; }
    public float getLetterSpacing() { return letterSpacing; }
    public void setLetterSpacing(float spacing) { letterSpacing = spacing; }
    public void setEllipsize(android.text.TextUtils.TruncateAt where) {}
    public android.text.TextUtils.TruncateAt getEllipsize() { return null; }
    public static final int AUTO_SIZE_TEXT_TYPE_NONE = 0;
    public static final int AUTO_SIZE_TEXT_TYPE_UNIFORM = 1;
    public void setAutoSizeTextTypeWithDefaults(int autoSizeTextType) {}
    public void setAutoSizeTextTypeUniformWithConfiguration(int autoSizeMinTextSize, int autoSizeMaxTextSize, int autoSizeStepGranularity, int unit) {}
    public void setAutoSizeTextTypeUniformWithPresetSizes(int[] presetSizes, int unit) {}
    public int getAutoSizeTextType() { return AUTO_SIZE_TEXT_TYPE_NONE; }
    public void setTextAppearance(int resId) {}
    public void setTextAppearance(Context context, int resId) {}
    public void setPaintFlags(int flags) {}
    public int getPaintFlags() { return 0; }
    public android.graphics.Paint getPaint() {
        android.graphics.Paint p = new android.graphics.Paint();
        p.setTextSize(textSize);
        p.setTypeface(typeface);
        p.setColor(textColor);
        return p;
    }
    public int getLineCount() { return 1; }
    public int getLineHeight() { return Math.round(textSize * 1.3f); }
    public void setCompoundDrawables(android.graphics.drawable.Drawable l, android.graphics.drawable.Drawable t, android.graphics.drawable.Drawable r, android.graphics.drawable.Drawable b) {}
    public void setCompoundDrawablesWithIntrinsicBounds(android.graphics.drawable.Drawable l, android.graphics.drawable.Drawable t, android.graphics.drawable.Drawable r, android.graphics.drawable.Drawable b) {}
    public void setCompoundDrawablesWithIntrinsicBounds(int l, int t, int r, int b) {}
    public void setCompoundDrawablePadding(int pad) {}
    public void setShadowLayer(float radius, float dx, float dy, int color) {}
    public void setHighlightColor(int color) {}
    public void setSelectAllOnFocus(boolean b) {}
    public void setCursorVisible(boolean b) {}
    public EditableCarrier editableTextHolder = null;
    public static CharSequence coerce(CharSequence cs) { return cs == null ? "" : cs; }
    public static class EditableCarrier {}
}

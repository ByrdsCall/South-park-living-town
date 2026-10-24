package android.text;
import android.graphics.Canvas;
import android.graphics.Paint;
// Desktop-mirror shim (internal QA tooling only).
public abstract class Layout {
    public enum Alignment { ALIGN_NORMAL, ALIGN_OPPOSITE, ALIGN_CENTER, ALIGN_LEFT, ALIGN_RIGHT }
    protected final CharSequence text;
    protected final TextPaint paint;
    protected final int width;
    protected Layout(CharSequence text, TextPaint paint, int width, Alignment alignment, float spacingMult, float spacingAdd) {
        this.text = text; this.paint = paint; this.width = width;
    }
    public final CharSequence getText() { return text; }
    public final TextPaint getPaint() { return paint; }
    public final int getWidth() { return width; }
    public abstract int getHeight();
    public abstract int getLineCount();
    public abstract int getLineTop(int line);
    public abstract int getLineBottom(int line);
    public int getLineStart(int line) { return 0; }
    public int getLineEnd(int line) { return text.length(); }
    public float getLineWidth(int line) { return 0f; }
    public int getLineDescent(int line) { return 0; }
    public int getLineAscent(int line) { return 0; }
    public Alignment getAlignment() { return Alignment.ALIGN_NORMAL; }
    public void draw(Canvas canvas) {}
}

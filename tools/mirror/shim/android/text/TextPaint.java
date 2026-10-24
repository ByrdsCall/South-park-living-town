package android.text;
import android.graphics.Paint;
// Desktop-mirror shim (internal QA tooling only).
public class TextPaint extends Paint {
    public float bgColor;
    public int baselineShift;
    public float density = 1f;
    public TextPaint() { super(); }
    public TextPaint(int flags) { super(flags); }
    public TextPaint(Paint p) { super(); if (p != null) set(p); }
}

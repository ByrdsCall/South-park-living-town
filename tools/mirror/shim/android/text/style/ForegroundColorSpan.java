package android.text.style;
// Desktop-mirror shim (internal QA tooling only).
public class ForegroundColorSpan extends CharacterStyle implements UpdateAppearance {
    private final int color;
    public ForegroundColorSpan(int color) { this.color = color; }
    public int getForegroundColor() { return color; }
    @Override public void updateDrawState(android.text.TextPaint tp) {}
}

package android.text.style;
// Desktop-mirror shim (internal QA tooling only).
public class BackgroundColorSpan extends CharacterStyle implements UpdateAppearance {
    private final int color;
    public BackgroundColorSpan(int color) { this.color = color; }
    public int getBackgroundColor() { return color; }
    @Override public void updateDrawState(android.text.TextPaint tp) {}
}

package android.text.style;
// Desktop-mirror shim (internal QA tooling only).
public class StyleSpan extends CharacterStyle implements UpdateAppearance {
    private final int style;
    public StyleSpan(int style) { this.style = style; }
    public int getStyle() { return style; }
    @Override public void updateDrawState(android.text.TextPaint tp) {}
}

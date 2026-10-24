package android.text.style;
// Desktop-mirror shim (internal QA tooling only).
public class RelativeSizeSpan extends CharacterStyle implements UpdateAppearance {
    private final float proportion;
    public RelativeSizeSpan(float proportion) { this.proportion = proportion; }
    public float getSizeChange() { return proportion; }
    @Override public void updateDrawState(android.text.TextPaint tp) {}
}

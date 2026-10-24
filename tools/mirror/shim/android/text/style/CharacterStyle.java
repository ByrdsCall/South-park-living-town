package android.text.style;
// Desktop-mirror shim (internal QA tooling only).
public abstract class CharacterStyle {
    public abstract void updateDrawState(android.text.TextPaint tp);
    public static CharacterStyle wrap(CharacterStyle cs) { return cs; }
}

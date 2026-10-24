package android.graphics;
// Desktop-mirror shim (internal QA tooling only).
public class PorterDuff {
    public enum Mode { CLEAR, SRC, DST, SRC_OVER, DST_OVER, SRC_IN, DST_IN, SRC_OUT, DST_OUT,
        SRC_ATOP, DST_ATOP, XOR, PLUS, MULTIPLY, SCREEN, OVERLAY, DARKEN, LIGHTEN, ADD }
    public static class Xfermode extends android.graphics.Xfermode {
        public final Mode mode;
        public Xfermode(Mode mode) { this.mode = mode; }
    }
}

package android.graphics;
// Desktop-mirror shim (internal QA tooling only).
public class PorterDuffXfermode extends Xfermode {
    public final PorterDuff.Mode mode;
    public PorterDuffXfermode(PorterDuff.Mode mode) { this.mode = mode; }
}

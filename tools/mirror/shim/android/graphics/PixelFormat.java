package android.graphics;
// Desktop-mirror shim (internal QA tooling only).
public class PixelFormat {
    public static final int UNKNOWN = 0, TRANSPARENT = -3, TRANSLUCENT = -3, OPAQUE = -1, RGBA_8888 = 1, RGB_565 = 4;
    public static boolean formatHasAlpha(int format) { return format == TRANSLUCENT || format == RGBA_8888; }
}

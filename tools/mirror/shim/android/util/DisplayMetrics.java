package android.util;
// Desktop-mirror shim (internal QA tooling only).
public class DisplayMetrics {
    public int widthPixels = 720, heightPixels = 1280;
    public float density = 2.0f;
    public float scaledDensity = 2.0f;
    public float xdpi = 320f, ydpi = 320f;
    public int densityDpi = 320;
    public static final int DENSITY_DEFAULT = 160;
    public static final int DENSITY_LOW = 120;
    public static final int DENSITY_MEDIUM = 160;
    public static final int DENSITY_HIGH = 240;
    public static final int DENSITY_XHIGH = 320;
    public static final int DENSITY_XXHIGH = 480;
    public DisplayMetrics() {}
    public void setTo(DisplayMetrics o) {
        widthPixels = o.widthPixels; heightPixels = o.heightPixels; density = o.density;
        scaledDensity = o.scaledDensity; xdpi = o.xdpi; ydpi = o.ydpi; densityDpi = o.densityDpi;
    }
}

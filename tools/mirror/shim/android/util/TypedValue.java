package android.util;
// Desktop-mirror shim (internal QA tooling only).
public class TypedValue {
    public static final int COMPLEX_UNIT_PX = 0, COMPLEX_UNIT_DIP = 1, COMPLEX_UNIT_SP = 2,
            COMPLEX_UNIT_PT = 3, COMPLEX_UNIT_IN = 4, COMPLEX_UNIT_MM = 5;
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case COMPLEX_UNIT_DIP: return value * metrics.density;
            case COMPLEX_UNIT_SP: return value * metrics.scaledDensity;
            case COMPLEX_UNIT_PT: return value * metrics.xdpi / 72f;
            case COMPLEX_UNIT_IN: return value * metrics.xdpi;
            case COMPLEX_UNIT_MM: return value * metrics.xdpi / 25.4f;
            default: return value;
        }
    }
}

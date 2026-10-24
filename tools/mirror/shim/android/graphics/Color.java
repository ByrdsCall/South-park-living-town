package android.graphics;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// Minimal android.graphics.Color over packed ARGB ints, matching Android semantics.
public class Color {
    public static final int BLACK = 0xFF000000;
    public static final int DKGRAY = 0xFF444444;
    public static final int GRAY = 0xFF888888;
    public static final int LTGRAY = 0xFFCCCCCC;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int RED = 0xFFFF0000;
    public static final int GREEN = 0xFF00FF00;
    public static final int BLUE = 0xFF0000FF;
    public static final int YELLOW = 0xFFFFFF00;
    public static final int CYAN = 0xFF00FFFF;
    public static final int MAGENTA = 0xFFFF00FF;
    public static final int TRANSPARENT = 0;

    public static int alpha(int color) { return color >>> 24; }
    public static int red(int color) { return (color >> 16) & 0xFF; }
    public static int green(int color) { return (color >> 8) & 0xFF; }
    public static int blue(int color) { return color & 0xFF; }
    public static int rgb(int r, int g, int b) { return 0xFF000000 | (r << 16) | (g << 8) | b; }
    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
    public static int parseColor(String s) {
        if (s == null || !s.startsWith("#")) throw new IllegalArgumentException("bad color: " + s);
        long v = Long.parseLong(s.substring(1), 16);
        if (s.length() == 7) v |= 0xFF000000L;
        else if (s.length() != 9) throw new IllegalArgumentException("bad color: " + s);
        return (int) v;
    }
}

package android.content.res;
import java.util.Locale;
// Desktop-mirror shim (internal QA tooling only).
public class Configuration {
    public float fontScale = 1.0f;
    public int orientation = ORIENTATION_PORTRAIT;
    public int screenWidthDp = 412, screenHeightDp = 915;
    public int densityDpi = 420;
    public Locale locale = Locale.getDefault();
    public static final int ORIENTATION_PORTRAIT = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;
    public static final int UI_MODE_NIGHT_MASK = 0x30;
    public static final int UI_MODE_NIGHT_NO = 0x10;
    public static final int UI_MODE_NIGHT_YES = 0x20;
    public int uiMode = UI_MODE_NIGHT_NO;
    public Configuration() {}
    public Configuration(Configuration o) {
        fontScale = o.fontScale; orientation = o.orientation; uiMode = o.uiMode;
        screenWidthDp = o.screenWidthDp; screenHeightDp = o.screenHeightDp; densityDpi = o.densityDpi;
    }
    public void setTo(Configuration o) {
        fontScale = o.fontScale; orientation = o.orientation; uiMode = o.uiMode;
        screenWidthDp = o.screenWidthDp; screenHeightDp = o.screenHeightDp; densityDpi = o.densityDpi;
    }
    public void setLocale(Locale l) { locale = l; }
    public Locale getLocale() { return locale; }
}

package android.content.res;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
// Desktop-mirror shim (internal QA tooling only).
public class Resources {
    private final DisplayMetrics metrics = new DisplayMetrics();
    private final Configuration config = new Configuration();
    public DisplayMetrics getDisplayMetrics() { return metrics; }
    public Configuration getConfiguration() { return config; }
    public Drawable getDrawable(int id) { return null; }
    public Drawable getDrawable(int id, Theme theme) { return null; }
    public int getColor(int id) { return 0; }
    public String getString(int id) { return ""; }
    public float getDimension(int id) { return 0f; }
    public int getDimensionPixelSize(int id) { return 0; }
    public int getInteger(int id) { return 0; }
    public static Resources getSystem() { return new Resources(); }
    public static class Theme {}
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String msg) { super(msg); }
    }
}

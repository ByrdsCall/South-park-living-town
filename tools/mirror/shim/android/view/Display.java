package android.view;
import android.graphics.Point;
import android.util.DisplayMetrics;
// Desktop-mirror shim (internal QA tooling only).
public class Display {
    public static final int DEFAULT_DISPLAY = 0;
    public int getDisplayId() { return DEFAULT_DISPLAY; }
    public void getSize(Point outSize) { outSize.set(720, 1280); }
    public void getRealSize(Point outSize) { outSize.set(720, 1280); }
    public void getMetrics(DisplayMetrics outMetrics) {
        outMetrics.widthPixels = 720; outMetrics.heightPixels = 1280;
    }
    public void getRealMetrics(DisplayMetrics outMetrics) { getMetrics(outMetrics); }
    @Deprecated public int getWidth() { return 720; }
    @Deprecated public int getHeight() { return 1280; }
    public float getRefreshRate() { return 60f; }
    public int getRotation() { return Surface.ROTATION_0; }
    public int getOrientation() { return 0; }
}

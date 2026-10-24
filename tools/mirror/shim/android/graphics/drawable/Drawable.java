package android.graphics.drawable;
import android.graphics.Canvas;
import android.graphics.Rect;
// Desktop-mirror shim (internal QA tooling only).
public abstract class Drawable {
    protected Rect bounds = new Rect();
    public void setBounds(int l, int t, int r, int b) { bounds.set(l, t, r, b); }
    public void setBounds(Rect b) { bounds.set(b); }
    public Rect getBounds() { return bounds; }
    public abstract void draw(Canvas canvas);
    private int alphaValue = 255;
    public void setAlpha(int alpha) { alphaValue = alpha; }
    public int getAlpha() { return alphaValue; }
    public void setColorFilter(android.graphics.ColorFilter cf) {}
    public int getOpacity() { return -3; }
    public int getIntrinsicWidth() { return -1; }
    public int getIntrinsicHeight() { return -1; }
    public boolean setState(int[] state) { return false; }
    public void setCallback(Callback cb) {}
    public interface Callback { void invalidateDrawable(Drawable who); void scheduleDrawable(Drawable who, Runnable what, long when); void unscheduleDrawable(Drawable who, Runnable what); }
}

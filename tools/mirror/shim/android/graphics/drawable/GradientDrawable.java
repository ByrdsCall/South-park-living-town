package android.graphics.drawable;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.RectF;
// Desktop-mirror shim (internal QA tooling only). Solid/gradient fill +
// corner radius + stroke; used by menus the mirror does not drive, but kept
// visually honest if a capture path ever reaches it.
public class GradientDrawable extends Drawable {
    public static final int RECTANGLE = 0, OVAL = 1, LINE = 2, RING = 3;
    public static final int LINEAR_GRADIENT = 0, RADIAL_GRADIENT = 1, SWEEP_GRADIENT = 2;
    public int shape = RECTANGLE;
    public int gradientType = LINEAR_GRADIENT;
    private int color = 0;
    private int[] colors;
    private float cornerRadiusPx = 0;
    private float[] cornerRadiiArr;
    private int strokeWidth = 0;
    private int strokeColor = 0;
    private float width = -1, height = -1;
    public void setShape(int s) { shape = s; }
    public int getShape() { return shape; }
    public void setColor(int c) { color = c; colors = null; }
    public void setColors(int[] cs) { colors = cs; }
    public void setGradientType(int t) { gradientType = t; }
    public void setCornerRadius(float r) { cornerRadiusPx = r; cornerRadiiArr = null; }
    public float getCornerRadius() { return cornerRadiusPx; }
    public void setCornerRadii(float[] radii) { cornerRadiiArr = radii; }
    public float[] getCornerRadii() { return cornerRadiiArr; }
    public void setStroke(int w, int c) { strokeWidth = w; strokeColor = c; }
    public void setSize(float w, float h) { width = w; height = h; }
    public void setAlpha(int a) {}
    @Override public void draw(Canvas canvas) {
        int fill = colors != null && colors.length > 0 ? colors[0] : color;
        RectF b = new RectF(bounds);
        Paint p = new Paint();
        p.setColor(fill);
        if (shape == OVAL) canvas.drawOval(b, p);
        else if (cornerRadiusPx > 0 || cornerRadiiArr != null) {
            float rx = cornerRadiusPx;
            if (cornerRadiiArr != null && cornerRadiiArr.length > 0) rx = cornerRadiiArr[0];
            canvas.drawRoundRect(b, rx, rx, p);
        } else canvas.drawRect(b, p);
        if (strokeWidth > 0) {
            Paint sp = new Paint();
            sp.setStyle(Paint.Style.STROKE);
            sp.setStrokeWidth(strokeWidth);
            sp.setColor(strokeColor);
            if (shape == OVAL) canvas.drawOval(b, sp); else canvas.drawRect(b, sp);
        }
    }
}

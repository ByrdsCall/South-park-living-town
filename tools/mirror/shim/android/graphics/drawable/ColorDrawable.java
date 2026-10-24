package android.graphics.drawable;
import android.graphics.Canvas;
import android.graphics.Paint;
// Desktop-mirror shim (internal QA tooling only).
public class ColorDrawable extends Drawable {
    private int color;
    public ColorDrawable() {}
    public ColorDrawable(int color) { this.color = color; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    @Override public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(color);
        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, p);
    }
}

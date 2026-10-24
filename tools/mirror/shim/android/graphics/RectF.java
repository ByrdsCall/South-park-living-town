package android.graphics;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class RectF {
    public float left, top, right, bottom;
    public RectF() {}
    public RectF(float l, float t, float r, float b) { left = l; top = t; right = r; bottom = b; }
    public RectF(RectF o) { left = o.left; top = o.top; right = o.right; bottom = o.bottom; }
    public RectF(Rect o) { left = o.left; top = o.top; right = o.right; bottom = o.bottom; }
    public final float width() { return right - left; }
    public final float height() { return bottom - top; }
    public final float centerX() { return (left + right) * 0.5f; }
    public final float centerY() { return (top + bottom) * 0.5f; }
    public boolean isEmpty() { return left >= right || top >= bottom; }
    public void set(float l, float t, float r, float b) { left = l; top = t; right = r; bottom = b; }
    public void set(RectF o) { left = o.left; top = o.top; right = o.right; bottom = o.bottom; }
    public void setEmpty() { left = top = right = bottom = 0; }
    public void offset(float dx, float dy) { left += dx; right += dx; top += dy; bottom += dy; }
    public void offsetTo(float nx, float ny) { right += nx - left; bottom += ny - top; left = nx; top = ny; }
    public void inset(float dx, float dy) { left += dx; top += dy; right -= dx; bottom -= dy; }
    public boolean contains(float x, float y) { return x >= left && x < right && y >= top && y < bottom; }
    public boolean contains(RectF r) { return left < right && top < bottom && left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom; }
    public boolean intersect(RectF r) {
        if (left < r.right && r.left < right && top < r.bottom && r.top < bottom) {
            left = Math.max(left, r.left); top = Math.max(top, r.top);
            right = Math.min(right, r.right); bottom = Math.min(bottom, r.bottom);
            return true;
        }
        return false;
    }
    public void round(Rect dst) {
        dst.set(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
    }
    public void sort() {
        if (left > right) { float t = left; left = right; right = t; }
        if (top > bottom) { float t = top; top = bottom; bottom = t; }
    }
    @Override public String toString() { return "RectF(" + left + "," + top + "," + right + "," + bottom + ")"; }
}

package android.graphics;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class Rect {
    public int left, top, right, bottom;
    public Rect() {}
    public Rect(int l, int t, int r, int b) { left = l; top = t; right = r; bottom = b; }
    public Rect(Rect o) { left = o.left; top = o.top; right = o.right; bottom = o.bottom; }
    public final int width() { return right - left; }
    public final int height() { return bottom - top; }
    public final int centerX() { return (left + right) >> 1; }
    public final int centerY() { return (top + bottom) >> 1; }
    public boolean isEmpty() { return left >= right || top >= bottom; }
    public void set(int l, int t, int r, int b) { left = l; top = t; right = r; bottom = b; }
    public void set(Rect o) { left = o.left; top = o.top; right = o.right; bottom = o.bottom; }
    public void setEmpty() { left = top = right = bottom = 0; }
    public void offset(int dx, int dy) { left += dx; right += dx; top += dy; bottom += dy; }
    public void inset(int dx, int dy) { left += dx; top += dy; right -= dx; bottom -= dy; }
    public boolean contains(int x, int y) { return x >= left && x < right && y >= top && y < bottom; }
    public void sort() {
        if (left > right) { int t = left; left = right; right = t; }
        if (top > bottom) { int t = top; top = bottom; bottom = t; }
    }
    @Override public String toString() { return "Rect(" + left + "," + top + "," + right + "," + bottom + ")"; }
}

package android.graphics;
// Desktop-mirror shim (internal QA tooling only).
public class Point {
    public int x, y;
    public Point() {}
    public Point(int x, int y) { this.x = x; this.y = y; }
    public void set(int x, int y) { this.x = x; this.y = y; }
    public void offset(int dx, int dy) { x += dx; y += dy; }
}

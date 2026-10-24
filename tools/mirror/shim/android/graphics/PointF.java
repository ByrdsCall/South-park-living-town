package android.graphics;
// Desktop-mirror shim (internal QA tooling only).
public class PointF {
    public float x, y;
    public PointF() {}
    public PointF(float x, float y) { this.x = x; this.y = y; }
    public PointF(PointF p) { x = p.x; y = p.y; }
    public void set(float x, float y) { this.x = x; this.y = y; }
    public void set(PointF p) { x = p.x; y = p.y; }
    public void offset(float dx, float dy) { x += dx; y += dy; }
    public float length() { return (float) Math.hypot(x, y); }
}

package android.graphics;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// android.graphics.Canvas over Java2D Graphics2D. This is the ONE forced
// platform divergence of the mirror: the final rasterizer is Java2D instead
// of Skia; every draw call, coordinate and paint decision above it is the
// game's own code, identical to Android.
public class Canvas {
    // QA bisection aid (mirror-only): -Dspt.drawlog=1 logs every draw call
    // with its sequence number, geometry and color, so a capture's pixels
    // can be traced to the exact calls (and pass order) that painted them.
    static final boolean DRAWLOG = System.getProperty("spt.drawlog") != null;
    private static long seq = 0;
    static void log(String fmt, Object... args) {
        if (DRAWLOG) System.err.println("[draw " + (seq++) + "] " + String.format(fmt, args));
    }
    static String colHex(Paint p) {
        return p == null ? "null" : String.format("#%08x", p.effectiveColor());
    }

    Graphics2D g;
    private Bitmap target;
    private final Deque<Graphics2D> stack = new ArrayDeque<>();

    public Canvas() {
        target = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        g = target.img.createGraphics();
    }
    public Canvas(Bitmap bitmap) {
        target = bitmap;
        g = bitmap.img.createGraphics();
    }
    Canvas(Graphics2D g, int w, int h) {
        this.g = g;
        this.widthOverride = w; this.heightOverride = h;
    }
    private int widthOverride = -1, heightOverride = -1;

    public void setBitmap(Bitmap bitmap) {
        if (g != null && target != null) g.dispose();
        target = bitmap;
        g = bitmap == null ? null : bitmap.img.createGraphics();
    }
    public int getWidth() { return widthOverride >= 0 ? widthOverride : (target == null ? 0 : target.getWidth()); }
    public int getHeight() { return heightOverride >= 0 ? heightOverride : (target == null ? 0 : target.getHeight()); }
    public boolean isHardwareAccelerated() { return false; }

    public int save() {
        // Graphics2D.create() snapshots transform + clip + paint state,
        // which is exactly Android's save() contract here.
        stack.push((Graphics2D) g.create());
        return stack.size();
    }
    public void restore() {
        if (!stack.isEmpty()) {
            g.dispose();
            g = stack.pop();
        }
    }
    public int getSaveCount() { return stack.size() + 1; }
    public void restoreToCount(int count) { while (stack.size() >= count && !stack.isEmpty()) restore(); }
    public int saveLayer(RectF bounds, Paint paint) { return save(); }
    public int saveLayerAlpha(RectF bounds, int alpha) { return save(); }

    public void translate(float dx, float dy) { g.translate(dx, dy); }
    public void scale(float sx, float sy) { g.scale(sx, sy); }
    public void scale(float sx, float sy, float px, float py) {
        g.translate(px, py); g.scale(sx, sy); g.translate(-px, -py);
    }
    public void rotate(float degrees) { g.rotate(Math.toRadians(degrees)); }
    public void rotate(float degrees, float px, float py) { g.rotate(Math.toRadians(degrees), px, py); }
    public void skew(float sx, float sy) { g.shear(sx, sy); }
    public void concat(Matrix matrix) { g.transform(matrix.impl); }
    public void setMatrix(Matrix matrix) { if (matrix != null) g.setTransform(matrix.impl); }

    public boolean clipRect(float l, float t, float r, float b) {
        g.clip(new Rectangle2D.Float(l, t, r - l, b - t));
        return true;
    }
    public boolean clipRect(RectF rect) { return clipRect(rect.left, rect.top, rect.right, rect.bottom); }
    public boolean clipRect(Rect rect) { return clipRect(rect.left, rect.top, rect.right, rect.bottom); }
    public boolean clipRect(int l, int t, int r, int b) { return clipRect((float) l, (float) t, (float) r, (float) b); }
    public boolean clipPath(Path path) { g.clip(path.impl); return true; }
    public boolean clipOutPath(Path path) { return true; }
    public boolean clipOutRect(float l, float t, float r, float b) { return true; }
    public void getClipBounds(Rect bounds) {
        Shape s = g.getClip();
        if (s == null) { bounds.set(0, 0, getWidth(), getHeight()); return; }
        java.awt.Rectangle r = s.getBounds();
        try {
            java.awt.geom.Rectangle2D rr = g.getTransform().createInverse()
                    .createTransformedShape(s).getBounds2D();
            bounds.set((int) rr.getX(), (int) rr.getY(),
                    (int) (rr.getX() + rr.getWidth()), (int) (rr.getY() + rr.getHeight()));
        } catch (Exception e) {
            bounds.set(r.x, r.y, r.x + r.width, r.y + r.height);
        }
    }

    public void drawColor(int color) {
        java.awt.Color old = g.getColor();
        Shape clip = g.getClip();
        g.setClip(null);
        g.setColor(new java.awt.Color(color, true));
        g.fillRect(-100000, -100000, 200000, 200000);
        g.setClip(clip);
        g.setColor(old);
    }
    public void drawColor(int color, PorterDuff.Mode mode) { drawColor(color); }
    public void drawARGB(int a, int r, int gg, int b) { drawColor(Color.argb(a, r, gg, b)); }
    public void drawRGB(int r, int gg, int b) { drawColor(Color.rgb(r, gg, b)); }
    public void drawPaint(Paint paint) {
        paint.applyHints(g);
        java.awt.Color old = g.getColor();
        Shape clip = g.getClip();
        g.setClip(null);
        g.setColor(paint.awtColor());
        g.fillRect(-100000, -100000, 200000, 200000);
        g.setClip(clip);
        g.setColor(old);
    }

    private void fillOrStroke(Shape shape, Paint paint) {
        if (paint == null) return;
        paint.applyHints(g);
        g.setColor(paint.awtColor());
        switch (paint.getStyle()) {
            case FILL:
                g.fill(shape);
                break;
            case STROKE:
                g.setStroke(paint.awtStroke());
                g.draw(shape);
                break;
            case FILL_AND_STROKE:
                g.fill(shape);
                g.setStroke(paint.awtStroke());
                g.draw(shape);
                break;
        }
    }

    public void drawRect(float l, float t, float r, float b, Paint paint) {
        log("rect %.0f,%.0f -> %.0f,%.0f %s %s", l, t, r, b, colHex(paint),
                paint == null ? "" : paint.getStyle());
        fillOrStroke(new Rectangle2D.Float(l, t, r - l, b - t), paint);
    }
    public void drawRect(RectF rect, Paint paint) { drawRect(rect.left, rect.top, rect.right, rect.bottom, paint); }
    public void drawRect(Rect rect, Paint paint) { drawRect(rect.left, rect.top, rect.right, rect.bottom, paint); }
    public void drawOval(float l, float t, float r, float b, Paint paint) {
        log("oval %.0f,%.0f -> %.0f,%.0f %s %s", l, t, r, b, colHex(paint),
                paint == null ? "" : paint.getStyle());
        fillOrStroke(new Ellipse2D.Float(l, t, r - l, b - t), paint);
    }
    public void drawOval(RectF oval, Paint paint) { drawOval(oval.left, oval.top, oval.right, oval.bottom, paint); }
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        fillOrStroke(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2), paint);
    }
    public void drawLine(float x1, float y1, float x2, float y2, Paint paint) {
        if (paint == null) return;
        paint.applyHints(g);
        g.setColor(paint.awtColor());
        g.setStroke(paint.awtStroke());
        g.draw(new Line2D.Float(x1, y1, x2, y2));
    }
    public void drawLines(float[] pts, int offset, int count, Paint paint) {
        for (int i = offset; i + 3 < offset + count && i + 3 < pts.length; i += 4)
            drawLine(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], paint);
    }
    public void drawLines(float[] pts, Paint paint) { drawLines(pts, 0, pts.length, paint); }
    public void drawPoint(float x, float y, Paint paint) { drawLine(x, y, x, y, paint); }
    public void drawPoints(float[] pts, Paint paint) {
        for (int i = 0; i + 1 < pts.length; i += 2) drawPoint(pts[i], pts[i + 1], paint);
    }
    public void drawPath(Path path, Paint paint) { fillOrStroke(path.impl, paint); }
    public void drawRoundRect(float l, float t, float r, float b, float rx, float ry, Paint paint) {
        log("roundrect %.0f,%.0f -> %.0f,%.0f %s %s", l, t, r, b, colHex(paint),
                paint == null ? "" : paint.getStyle());
        fillOrStroke(new RoundRectangle2D.Float(l, t, r - l, b - t, rx * 2, ry * 2), paint);
    }
    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint);
    }
    public void drawArc(float l, float t, float r, float b, float startAngle, float sweepAngle,
                        boolean useCenter, Paint paint) {
        Arc2D.Float arc = new Arc2D.Float(l, t, r - l, b - t, -startAngle, -sweepAngle,
                useCenter ? Arc2D.PIE : Arc2D.OPEN);
        if (paint != null && paint.getStyle() == Paint.Style.FILL && !useCenter) {
            arc.setArcType(Arc2D.CHORD);
        }
        fillOrStroke(arc, paint);
    }
    public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        drawArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, useCenter, paint);
    }

    public void drawText(String text, float x, float y, Paint paint) {
        if (text == null || text.isEmpty() || paint == null) return;
        log("text '%s' at %.0f,%.0f size %.1f %s", text, x, y,
                paint.getTextSize(), colHex(paint));
        paint.applyHints(g);
        g.setColor(paint.awtColor());
        java.awt.Font f = paint.awtFont();
        g.setFont(f);
        float drawX = x;
        switch (paint.getTextAlign()) {
            case CENTER: drawX = x - paint.measureText(text) / 2f; break;
            case RIGHT: drawX = x - paint.measureText(text); break;
            default: break;
        }
        if (paint.getTextScaleX() != 1f) {
            java.awt.geom.AffineTransform old = g.getTransform();
            g.translate(drawX, 0);
            g.scale(paint.getTextScaleX(), 1);
            g.drawString(text, 0, y);
            g.setTransform(old);
        } else {
            g.drawString(text, drawX, y);
        }
    }
    public void drawText(String text, int start, int end, float x, float y, Paint paint) {
        drawText(text.substring(start, end), x, y, paint);
    }
    public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
        drawText(new String(text, index, count), x, y, paint);
    }
    public void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
        // Rare cosmetic path (none in the game's scene pipeline); draw at path start.
        java.awt.geom.Point2D p = path.impl.getCurrentPoint();
        if (p != null) drawText(text, (float) p.getX() + hOffset, (float) p.getY() + vOffset, paint);
    }

    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        if (bitmap == null) return;
        CompositeGuard cg = new CompositeGuard(g, paint);
        g.drawImage(bitmap.img, Math.round(left), Math.round(top), null);
        cg.restore();
    }
    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        if (bitmap == null || dst == null) return;
        CompositeGuard cg = new CompositeGuard(g, paint);
        if (src == null) {
            g.drawImage(bitmap.img, Math.round(dst.left), Math.round(dst.top),
                    Math.round(dst.width()), Math.round(dst.height()), null);
        } else {
            g.drawImage(bitmap.img, Math.round(dst.left), Math.round(dst.top),
                    Math.round(dst.right), Math.round(dst.bottom),
                    src.left, src.top, src.right, src.bottom, null);
        }
        cg.restore();
    }
    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        if (bitmap == null || dst == null) return;
        drawBitmap(bitmap, src, new RectF(dst), paint);
    }
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        if (bitmap == null) return;
        CompositeGuard cg = new CompositeGuard(g, paint);
        java.awt.geom.AffineTransform old = g.getTransform();
        g.transform(matrix.impl);
        g.drawImage(bitmap.img, 0, 0, null);
        g.setTransform(old);
        cg.restore();
    }

    /** Applies a Paint's alpha as the Graphics2D composite while blitting. */
    private static class CompositeGuard {
        private final Graphics2D g;
        private final java.awt.Composite old;
        CompositeGuard(Graphics2D g, Paint paint) {
            this.g = g;
            old = g.getComposite();
            if (paint != null && paint.getAlpha() < 255) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        paint.getAlpha() / 255f));
            }
        }
        void restore() { g.setComposite(old); }
    }

    BufferedImage targetImage() { return target == null ? null : target.img; }
}

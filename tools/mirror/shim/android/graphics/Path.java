package android.graphics;

import java.awt.geom.Path2D;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// android.graphics.Path over java.awt.geom.Path2D.Float.
public class Path {
    public enum Direction { CW, CCW }
    public enum FillType {
        WINDING, EVEN_ODD, INVERSE_WINDING, INVERSE_EVEN_ODD;
        public FillType getInverse() {
            switch (this) {
                case WINDING: return INVERSE_WINDING;
                case EVEN_ODD: return INVERSE_EVEN_ODD;
                case INVERSE_WINDING: return WINDING;
                default: return EVEN_ODD;
            }
        }
    }

    final Path2D.Float impl = new Path2D.Float();
    private FillType fillType = FillType.WINDING;

    public Path() {}
    public Path(Path src) { if (src != null) impl.append(src.impl, false); }

    public void reset() { impl.reset(); }
    public void rewind() { impl.reset(); }
    public boolean isEmpty() {
        return impl.getCurrentPoint() == null && isRectEmpty();
    }
    private boolean isRectEmpty() {
        java.awt.geom.Rectangle2D b = impl.getBounds2D();
        return b.getWidth() == 0 && b.getHeight() == 0;
    }
    public void set(Path src) { impl.reset(); if (src != null) impl.append(src.impl, false); }
    public FillType getFillType() { return fillType; }
    public void setFillType(FillType ft) {
        fillType = ft;
        impl.setWindingRule(ft == FillType.EVEN_ODD || ft == FillType.INVERSE_EVEN_ODD
                ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
    }
    public boolean isInverseFillType() {
        return fillType == FillType.INVERSE_WINDING || fillType == FillType.INVERSE_EVEN_ODD;
    }
    public void toggleInverseFillType() { setFillType(fillType.getInverse()); }

    public void moveTo(float x, float y) { impl.moveTo(x, y); }
    public void lineTo(float x, float y) {
        if (impl.getCurrentPoint() == null) impl.moveTo(x, y); else impl.lineTo(x, y);
    }
    public void quadTo(float x1, float y1, float x2, float y2) { impl.quadTo(x1, y1, x2, y2); }
    public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        impl.curveTo(x1, y1, x2, y2, x3, y3);
    }
    public void rMoveTo(float dx, float dy) {
        java.awt.geom.Point2D p = impl.getCurrentPoint();
        if (p == null) impl.moveTo(dx, dy); else impl.moveTo((float) p.getX() + dx, (float) p.getY() + dy);
    }
    public void rLineTo(float dx, float dy) {
        java.awt.geom.Point2D p = impl.getCurrentPoint();
        if (p == null) impl.moveTo(dx, dy);
        else impl.lineTo((float) p.getX() + dx, (float) p.getY() + dy);
    }
    public void rQuadTo(float dx1, float dy1, float dx2, float dy2) {
        java.awt.geom.Point2D p = impl.getCurrentPoint();
        float x = p == null ? 0 : (float) p.getX(), y = p == null ? 0 : (float) p.getY();
        impl.quadTo(x + dx1, y + dy1, x + dx2, y + dy2);
    }
    public void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        java.awt.geom.Point2D p = impl.getCurrentPoint();
        float x = p == null ? 0 : (float) p.getX(), y = p == null ? 0 : (float) p.getY();
        impl.curveTo(x + x1, y + y1, x + x2, y + y2, x + x3, y + y3);
    }
    public void close() { impl.closePath(); }

    public void addRect(float l, float t, float r, float b, Direction dir) {
        impl.moveTo(l, t);
        if (dir == Direction.CW) { impl.lineTo(r, t); impl.lineTo(r, b); impl.lineTo(l, b); }
        else { impl.lineTo(l, b); impl.lineTo(r, b); impl.lineTo(r, t); }
        impl.closePath();
    }
    public void addRect(RectF rect, Direction dir) { addRect(rect.left, rect.top, rect.right, rect.bottom, dir); }
    public void addOval(float l, float t, float r, float b, Direction dir) {
        impl.append(new java.awt.geom.Ellipse2D.Float(l, t, r - l, b - t), false);
    }
    public void addOval(RectF oval, Direction dir) { addOval(oval.left, oval.top, oval.right, oval.bottom, dir); }
    public void addCircle(float x, float y, float radius, Direction dir) {
        impl.append(new java.awt.geom.Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2), false);
    }
    public void addRoundRect(float l, float t, float r, float b, float rx, float ry, Direction dir) {
        impl.append(new java.awt.geom.RoundRectangle2D.Float(l, t, r - l, b - t, rx * 2, ry * 2), false);
    }
    public void addRoundRect(RectF rect, float rx, float ry, Direction dir) {
        addRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, dir);
    }
    public void addPath(Path src) { impl.append(src.impl, false); }
    public void addPath(Path src, float dx, float dy) {
        java.awt.geom.AffineTransform at = java.awt.geom.AffineTransform.getTranslateInstance(dx, dy);
        impl.append(at.createTransformedShape(src.impl), false);
    }
    public void arcTo(float l, float t, float r, float b, float startAngle, float sweepAngle, boolean forceMoveTo) {
        java.awt.geom.Arc2D.Float arc = new java.awt.geom.Arc2D.Float(
                l, t, r - l, b - t, -startAngle, -sweepAngle, java.awt.geom.Arc2D.OPEN);
        java.awt.geom.Point2D sp = arc.getStartPoint();
        if (forceMoveTo || impl.getCurrentPoint() == null) impl.moveTo((float) sp.getX(), (float) sp.getY());
        else impl.lineTo((float) sp.getX(), (float) sp.getY());
        impl.append(arc, true);
    }
    public void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) {
        arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo);
    }
    public void arcTo(RectF oval, float startAngle, float sweepAngle) {
        arcTo(oval, startAngle, sweepAngle, false);
    }
    public void offset(float dx, float dy) {
        java.awt.geom.AffineTransform at = java.awt.geom.AffineTransform.getTranslateInstance(dx, dy);
        impl.transform(at);
    }
    public void transform(Matrix matrix) { impl.transform(matrix.impl); }
    public void computeBounds(RectF bounds, boolean exact) {
        java.awt.geom.Rectangle2D b = impl.getBounds2D();
        bounds.set((float) b.getX(), (float) b.getY(),
                (float) (b.getX() + b.getWidth()), (float) (b.getY() + b.getHeight()));
    }
}

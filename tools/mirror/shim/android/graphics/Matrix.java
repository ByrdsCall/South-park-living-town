package android.graphics;

import java.awt.geom.AffineTransform;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class Matrix {
    final AffineTransform impl = new AffineTransform();
    public Matrix() {}
    public Matrix(Matrix src) { if (src != null) impl.setTransform(src.impl); }
    public boolean isIdentity() { return impl.isIdentity(); }
    public void reset() { impl.setToIdentity(); }
    public void set(Matrix src) { impl.setTransform(src.impl); }
    public boolean preTranslate(float dx, float dy) { impl.preConcatenate(AffineTransform.getTranslateInstance(dx, dy)); return true; }
    public boolean postTranslate(float dx, float dy) { impl.concatenate(AffineTransform.getTranslateInstance(dx, dy)); return true; }
    public boolean preScale(float sx, float sy) { impl.preConcatenate(AffineTransform.getScaleInstance(sx, sy)); return true; }
    public boolean postScale(float sx, float sy) { impl.concatenate(AffineTransform.getScaleInstance(sx, sy)); return true; }
    public boolean preRotate(float degrees) { impl.preConcatenate(AffineTransform.getRotateInstance(Math.toRadians(degrees))); return true; }
    public boolean postRotate(float degrees) { impl.concatenate(AffineTransform.getRotateInstance(Math.toRadians(degrees))); return true; }
    public boolean setTranslate(float dx, float dy) { impl.setToTranslation(dx, dy); return true; }
    public boolean setScale(float sx, float sy) { impl.setToScale(sx, sy); return true; }
    public void mapPoints(float[] pts) {
        impl.transform(pts, 0, pts, 0, pts.length / 2);
    }
    public void mapRect(RectF rect) {
        java.awt.geom.Rectangle2D r = impl.createTransformedShape(
                new java.awt.geom.Rectangle2D.Float(rect.left, rect.top, rect.width(), rect.height())).getBounds2D();
        rect.set((float) r.getX(), (float) r.getY(), (float) (r.getX() + r.getWidth()), (float) (r.getY() + r.getHeight()));
    }
}

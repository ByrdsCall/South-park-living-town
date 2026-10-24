package android.graphics;

import java.io.InputStream;
import javax.imageio.ImageIO;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class BitmapFactory {
    public static class Options {
        public boolean inJustDecodeBounds = false;
        public int outWidth = -1, outHeight = -1;
        public Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888;
        public int inSampleSize = 1;
        public boolean inMutable = false;
    }
    public static Bitmap decodeStream(InputStream is) { return decodeStream(is, null, null); }
    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        try {
            java.awt.image.BufferedImage img = ImageIO.read(is);
            if (img == null) return null;
            if (opts != null) { opts.outWidth = img.getWidth(); opts.outHeight = img.getHeight(); }
            if (opts != null && opts.inJustDecodeBounds) return null;
            return new Bitmap(img);
        } catch (Exception e) { return null; }
    }
    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        try {
            java.awt.image.BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(data, offset, length));
            return img == null ? null : new Bitmap(img);
        } catch (Exception e) { return null; }
    }
    public static Bitmap decodeFile(String pathName) {
        try {
            java.awt.image.BufferedImage img = ImageIO.read(new java.io.File(pathName));
            return img == null ? null : new Bitmap(img);
        } catch (Exception e) { return null; }
    }
}

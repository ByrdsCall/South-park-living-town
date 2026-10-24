package android.graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// android.graphics.Bitmap over java.awt.image.BufferedImage.
public class Bitmap {
    public enum Config { ALPHA_8, RGB_565, ARGB_8888, RGBA_F16 }
    public enum CompressFormat { JPEG, PNG, WEBP }

    public final BufferedImage img;
    Bitmap(BufferedImage img) { this.img = img; }

    public static Bitmap createBitmap(int width, int height, Config config) {
        return new Bitmap(new BufferedImage(Math.max(1, width), Math.max(1, height),
                BufferedImage.TYPE_INT_ARGB));
    }
    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        BufferedImage sub = source.img.getSubimage(x, y, width, height);
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(sub, 0, 0, null);
        g.dispose();
        return new Bitmap(copy);
    }
    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        BufferedImage out = new BufferedImage(Math.max(1, dstWidth), Math.max(1, dstHeight),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        if (filter) g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src.img, 0, 0, dstWidth, dstHeight, null);
        g.dispose();
        return new Bitmap(out);
    }
    public int getWidth() { return img.getWidth(); }
    public int getHeight() { return img.getHeight(); }
    public Config getConfig() { return Config.ARGB_8888; }
    public boolean isRecycled() { return false; }
    public void recycle() {}
    public void eraseColor(int c) {
        Graphics2D g = img.createGraphics();
        g.setColor(new java.awt.Color(c, true));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();
    }
    public int getPixel(int x, int y) { return img.getRGB(x, y); }
    public void setPixel(int x, int y, int color) { img.setRGB(x, y, color); }
    public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        int[] raw = img.getRGB(x, y, width, height, null, 0, width);
        for (int row = 0; row < height; row++)
            System.arraycopy(raw, row * width, pixels, offset + row * stride, width);
    }
    public void setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        int[] raw = new int[width * height];
        for (int row = 0; row < height; row++)
            System.arraycopy(pixels, offset + row * stride, raw, row * width, width);
        img.setRGB(x, y, width, height, raw, 0, width);
    }
    public Bitmap copy(Config config, boolean mutable) {
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return new Bitmap(out);
    }
    public boolean compress(CompressFormat format, int quality, java.io.OutputStream stream) {
        try {
            String fmt = format == CompressFormat.PNG ? "png" : "jpg";
            return javax.imageio.ImageIO.write(img, fmt, stream);
        } catch (Exception e) { return false; }
    }
    public Canvas createCanvas() { return new Canvas(this); }
}

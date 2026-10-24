package android.graphics;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// android.graphics.Paint: state holder; Canvas applies it to Graphics2D.
public class Paint {
    public enum Style { FILL, STROKE, FILL_AND_STROKE }
    public enum Align { LEFT, CENTER, RIGHT }
    public enum Cap { BUTT, ROUND, SQUARE }
    public enum Join { MITER, ROUND, BEVEL }

    public static class FontMetrics {
        public float top, ascent, descent, bottom, leading;
    }
    public static class FontMetricsInt {
        public int top, ascent, descent, bottom, leading;
    }

    public static final int ANTI_ALIAS_FLAG = 1;
    public static final int FILTER_BITMAP_FLAG = 2;
    public static final int DITHER_FLAG = 4;
    public static final int UNDERLINE_TEXT_FLAG = 8;
    public static final int STRIKE_THRU_TEXT_FLAG = 16;
    public static final int FAKE_BOLD_TEXT_FLAG = 32;

    int flags;
    private int color = 0xFF000000;
    private int alpha = 255;
    private Style style = Style.FILL;
    private float strokeWidth = 0f;
    private Cap strokeCap = Cap.BUTT;
    private Join strokeJoin = Join.MITER;
    private float strokeMiter = 4f;
    private float textSize = 12f;
    private Align textAlign = Align.LEFT;
    private Typeface typeface = Typeface.DEFAULT;
    private boolean antiAlias = false;
    private boolean fakeBold = false;
    private boolean underline = false;
    private boolean strikeThru = false;
    private PathEffect pathEffect;
    private float shadowRadius, shadowDx, shadowDy;
    private int shadowColor;
    private float textScaleX = 1f;
    private float letterSpacing = 0f;

    public Paint() { this(0); }
    public Paint(int flags) { this.flags = flags; antiAlias = (flags & ANTI_ALIAS_FLAG) != 0; }
    public Paint(Paint src) { set(src); }

    public void reset() {
        color = 0xFF000000; alpha = 255; style = Style.FILL; strokeWidth = 0f;
        strokeCap = Cap.BUTT; strokeJoin = Join.MITER; textSize = 12f;
        textAlign = Align.LEFT; typeface = Typeface.DEFAULT; fakeBold = false;
        pathEffect = null; shadowRadius = 0; textScaleX = 1f; letterSpacing = 0f;
    }
    public void set(Paint src) {
        if (src == null) return;
        flags = src.flags; color = src.color; alpha = src.alpha; style = src.style;
        strokeWidth = src.strokeWidth; strokeCap = src.strokeCap; strokeJoin = src.strokeJoin;
        strokeMiter = src.strokeMiter; textSize = src.textSize; textAlign = src.textAlign;
        typeface = src.typeface; antiAlias = src.antiAlias; fakeBold = src.fakeBold;
        underline = src.underline; strikeThru = src.strikeThru; pathEffect = src.pathEffect;
        shadowRadius = src.shadowRadius; shadowDx = src.shadowDx; shadowDy = src.shadowDy;
        shadowColor = src.shadowColor; textScaleX = src.textScaleX; letterSpacing = src.letterSpacing;
    }
    public void setFlags(int flags) { this.flags = flags; }
    public int getFlags() { return flags; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public int getAlpha() { return alpha; }
    public void setAlpha(int a) { alpha = a & 0xFF; }
    public void setARGB(int a, int r, int g, int b) { color = Color.argb(a, r, g, b); }
    public Style getStyle() { return style; }
    public void setStyle(Style style) { this.style = style; }
    public float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(float w) { strokeWidth = w; }
    public Cap getStrokeCap() { return strokeCap; }
    public void setStrokeCap(Cap cap) { strokeCap = cap; }
    public Join getStrokeJoin() { return strokeJoin; }
    public void setStrokeJoin(Join join) { strokeJoin = join; }
    public float getStrokeMiter() { return strokeMiter; }
    public void setStrokeMiter(float m) { strokeMiter = m; }
    public boolean isAntiAlias() { return antiAlias; }
    public void setAntiAlias(boolean aa) { antiAlias = aa; }
    public boolean isDither() { return false; }
    public void setDither(boolean d) {}
    public boolean isFilterBitmap() { return false; }
    public void setFilterBitmap(boolean b) {}
    public float getTextSize() { return textSize; }
    public void setTextSize(float size) { textSize = size; }
    public Align getTextAlign() { return textAlign; }
    public void setTextAlign(Align align) { textAlign = align; }
    public Typeface getTypeface() { return typeface; }
    public Typeface setTypeface(Typeface tf) { Typeface old = typeface; typeface = tf == null ? Typeface.DEFAULT : tf; return old; }
    public boolean isFakeBoldText() { return fakeBold; }
    public void setFakeBoldText(boolean b) { fakeBold = b; }
    public boolean isUnderlineText() { return underline; }
    public void setUnderlineText(boolean b) { underline = b; }
    public boolean isStrikeThruText() { return strikeThru; }
    public void setStrikeThruText(boolean b) { strikeThru = b; }
    public float getTextScaleX() { return textScaleX; }
    public void setTextScaleX(float s) { textScaleX = s; }
    public float getLetterSpacing() { return letterSpacing; }
    public void setLetterSpacing(float s) { letterSpacing = s; }
    public PathEffect getPathEffect() { return pathEffect; }
    public PathEffect setPathEffect(PathEffect effect) { PathEffect old = pathEffect; pathEffect = effect; return old; }
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        shadowRadius = radius; shadowDx = dx; shadowDy = dy; shadowColor = color;
    }
    public void clearShadowLayer() { shadowRadius = 0; shadowColor = 0; }
    public float getShadowRadius() { return shadowRadius; }
    public Shader getShader() { return null; }
    public Shader setShader(Shader shader) { return null; }
    public ColorFilter getColorFilter() { return null; }
    public ColorFilter setColorFilter(ColorFilter filter) { return null; }
    public Xfermode getXfermode() { return null; }
    public Xfermode setXfermode(Xfermode x) { return null; }

    // ---- text measurement, via AWT ----
    private static final Graphics2D SCRATCH =
            new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB).createGraphics();
    Font awtFont() { return typeface.awtFont(textSize, fakeBold); }

    public float measureText(String text) {
        if (text == null || text.isEmpty()) return 0f;
        Font f = awtFont();
        FontRenderContext frc = SCRATCH.getFontRenderContext();
        Rectangle2D b = f.getStringBounds(text, frc);
        return (float) b.getWidth() * textScaleX;
    }
    public float measureText(String text, int start, int end) {
        return measureText(text.substring(start, end));
    }
    public float measureText(char[] text, int index, int count) {
        return measureText(new String(text, index, count));
    }
    public int getTextWidths(String text, float[] widths) {
        Font f = awtFont();
        FontRenderContext frc = SCRATCH.getFontRenderContext();
        float x = 0;
        for (int i = 0; i < text.length() && i < widths.length; i++) {
            Rectangle2D b = f.getStringBounds(text.substring(i, i + 1), frc);
            float w = (float) b.getWidth();
            x += w; widths[i] = x;
        }
        return Math.min(text.length(), widths.length);
    }
    public float getFontMetrics(FontMetrics metrics) {
        java.awt.FontMetrics fm = SCRATCH.getFontMetrics(awtFont());
        if (metrics != null) {
            metrics.ascent = -fm.getAscent();
            metrics.descent = fm.getDescent();
            metrics.top = -fm.getMaxAscent();
            metrics.bottom = fm.getMaxDescent();
            metrics.leading = fm.getLeading();
        }
        return fm.getLeading();
    }
    public FontMetrics getFontMetrics() {
        FontMetrics m = new FontMetrics();
        getFontMetrics(m);
        return m;
    }
    public int getFontMetricsInt(FontMetricsInt metrics) {
        java.awt.FontMetrics fm = SCRATCH.getFontMetrics(awtFont());
        if (metrics != null) {
            metrics.ascent = -fm.getAscent();
            metrics.descent = fm.getDescent();
            metrics.top = -fm.getMaxAscent();
            metrics.bottom = fm.getMaxDescent();
            metrics.leading = fm.getLeading();
        }
        return fm.getLeading();
    }
    public float ascent() { return -SCRATCH.getFontMetrics(awtFont()).getAscent(); }
    public float descent() { return SCRATCH.getFontMetrics(awtFont()).getDescent(); }
    public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) {
        int n = 0;
        while (n < text.length() && measureText(text.substring(0, n + 1)) <= maxWidth) n++;
        if (measuredWidth != null && measuredWidth.length > 0)
            measuredWidth[0] = measureText(text.substring(0, n));
        return n;
    }

    /** Effective drawing color: Paint alpha folds into the color's own alpha (Android semantics). */
    int effectiveColor() {
        int a = (Color.alpha(color) * alpha) / 255;
        return (color & 0x00FFFFFF) | (a << 24);
    }
    java.awt.Color awtColor() {
        int c = effectiveColor();
        return new java.awt.Color(c, true);
    }
    BasicStroke awtStroke() {
        int cap = strokeCap == Cap.ROUND ? BasicStroke.CAP_ROUND
                : strokeCap == Cap.SQUARE ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_BUTT;
        int join = strokeJoin == Join.ROUND ? BasicStroke.JOIN_ROUND
                : strokeJoin == Join.BEVEL ? BasicStroke.JOIN_BEVEL : BasicStroke.JOIN_MITER;
        return new BasicStroke(Math.max(0f, strokeWidth), cap, join, strokeMiter);
    }
    void applyHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}

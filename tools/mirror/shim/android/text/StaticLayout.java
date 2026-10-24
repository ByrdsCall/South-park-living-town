package android.text;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;
// Desktop-mirror shim (internal QA tooling only). StaticLayout with real
// word-wrap measurement through the shim Paint (AWT metrics): line breaks
// follow the same fit logic the game sees on device, modulo glyph metrics
// of the mirror font — text CONTENT and wrap counts are the game's own.
public class StaticLayout extends Layout {
    private final List<int[]> lines = new ArrayList<>(); // [start, end)
    private final float lineHeight;
    private final Alignment alignment;
    private final float spacingMult, spacingAdd;
    private final boolean includePad;

    public static class Builder {
        private CharSequence text; private int start, end;
        private TextPaint paint; private int width;
        private Alignment alignment = Alignment.ALIGN_NORMAL;
        private float spacingMult = 1f, spacingAdd = 0f;
        private boolean includePad = true;
        public static Builder obtain(CharSequence text, int start, int end, TextPaint paint, int width) {
            Builder b = new Builder();
            b.text = text; b.start = start; b.end = end; b.paint = paint; b.width = width;
            return b;
        }
        public Builder setAlignment(Alignment a) { alignment = a; return this; }
        public Builder setLineSpacing(float add, float mult) { spacingAdd = add; spacingMult = mult; return this; }
        public Builder setIncludePad(boolean includePad) { this.includePad = includePad; return this; }
        public Builder setBreakStrategy(int s) { return this; }
        public Builder setHyphenationFrequency(int f) { return this; }
        public Builder setJustificationMode(int m) { return this; }
        public Builder setEllipsize(android.text.TextUtils.TruncateAt e) { return this; }
        public Builder setMaxLines(int max) { return this; }
        public StaticLayout build() { return new StaticLayout(this); }
    }

    private StaticLayout(Builder b) {
        super(b.text, b.paint, b.width, b.alignment, b.spacingMult, b.spacingAdd);
        alignment = b.alignment; spacingMult = b.spacingMult; spacingAdd = b.spacingAdd; includePad = b.includePad;
        String s = b.text.subSequence(b.start, Math.min(b.end, b.text.length())).toString();
        android.graphics.Paint.FontMetrics fm = b.paint.getFontMetrics();
        lineHeight = (fm.descent - fm.ascent) * spacingMult + spacingAdd;
        // word wrap with hard line breaks
        int pos = 0;
        for (String para : s.split("\n", -1)) {
            int paraStart = pos;
            if (para.isEmpty()) { lines.add(new int[]{paraStart, paraStart}); pos += 1; continue; }
            int cur = 0;
            String[] words = para.split("(?<= )");
            int lineStart = 0;
            float curW = 0;
            int consumed = 0;
            for (String w : words) {
                float ww = b.paint.measureText(w);
                if (curW + ww > b.width && consumed > lineStart) {
                    lines.add(new int[]{paraStart + lineStart, paraStart + consumed});
                    lineStart = consumed;
                    curW = 0;
                }
                curW += ww;
                consumed += w.length();
                cur = consumed;
            }
            lines.add(new int[]{paraStart + lineStart, paraStart + para.length()});
            pos += para.length() + 1;
        }
        if (lines.isEmpty()) lines.add(new int[]{0, 0});
    }
    @Deprecated
    public StaticLayout(CharSequence source, TextPaint paint, int width, Alignment cap, float spacingMult, float spacingAdd) {
        this(Builder.obtain(source, 0, source.length(), paint, width)
                .setAlignment(cap).setLineSpacing(spacingAdd, spacingMult));
    }
    @Override public int getLineCount() { return lines.size(); }
    @Override public int getHeight() { return Math.round(lineHeight * lines.size()); }
    @Override public int getLineTop(int line) { return Math.round(line * lineHeight); }
    @Override public int getLineBottom(int line) { return Math.round((line + 1) * lineHeight); }
    @Override public int getLineStart(int line) { return lines.get(line)[0]; }
    @Override public int getLineEnd(int line) { return lines.get(line)[1]; }
    @Override public float getLineWidth(int line) {
        String t = text.toString();
        int[] r = lines.get(line);
        return paint.measureText(t.substring(Math.min(r[0], t.length()), Math.min(r[1], t.length())));
    }
    @Override public Alignment getAlignment() { return alignment; }
    @Override public void draw(Canvas canvas) {
        String t = text.toString();
        android.graphics.Paint.FontMetrics fm = paint.getFontMetrics();
        for (int i = 0; i < lines.size(); i++) {
            int[] r = lines.get(i);
            String line = t.substring(Math.min(r[0], t.length()), Math.min(r[1], t.length()));
            float x = 0;
            float w = paint.measureText(line);
            if (alignment == Alignment.ALIGN_CENTER) x = (width - w) / 2f;
            else if (alignment == Alignment.ALIGN_OPPOSITE || alignment == Alignment.ALIGN_RIGHT) x = width - w;
            float baseline = getLineTop(i) - fm.ascent;
            android.graphics.Paint p = new android.graphics.Paint(paint);
            p.setTextAlign(android.graphics.Paint.Align.LEFT);
            canvas.drawText(line, x, baseline, (android.graphics.Paint) p);
        }
    }
}

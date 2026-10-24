package android.graphics;

import java.awt.Font;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// Typeface over java.awt.Font families. "casual" (the game's hand font) maps
// to a rounded sans available on stock JREs; metrics come from AWT.
public class Typeface {
    public static final int NORMAL = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int BOLD_ITALIC = 3;

    public final String family;
    public final int style;
    Typeface(String family, int style) { this.family = family; this.style = style; }

    public static final Typeface DEFAULT = new Typeface(Font.SANS_SERIF, NORMAL);
    public static final Typeface DEFAULT_BOLD = new Typeface(Font.SANS_SERIF, BOLD);
    public static final Typeface SANS_SERIF = new Typeface(Font.SANS_SERIF, NORMAL);
    public static final Typeface SERIF = new Typeface(Font.SERIF, NORMAL);
    public static final Typeface MONOSPACE = new Typeface(Font.MONOSPACED, NORMAL);

    static String mapFamily(String name) {
        if (name == null) return Font.SANS_SERIF;
        switch (name) {
            case "casual": return "Comic Sans MS"; // falls back to Dialog if absent
            case "sans-serif": case "sans": return Font.SANS_SERIF;
            case "serif": return Font.SERIF;
            case "monospace": case "mono": return Font.MONOSPACED;
            default: return name;
        }
    }

    public static Typeface create(String familyName, int style) {
        return new Typeface(familyName == null ? Font.SANS_SERIF : familyName, style);
    }
    public static Typeface create(Typeface family, int style) {
        if (family == null) return create((String) null, style);
        return new Typeface(family.family, style);
    }
    public static Typeface defaultFromStyle(int style) { return new Typeface(Font.SANS_SERIF, style); }
    public int getStyle() { return style; }
    public boolean isBold() { return (style & BOLD) != 0; }
    public boolean isItalic() { return (style & ITALIC) != 0; }

    Font awtFont(float size, boolean fakeBold) {
        int st = Font.PLAIN;
        if ((style & BOLD) != 0 || fakeBold) st |= Font.BOLD;
        if ((style & ITALIC) != 0) st |= Font.ITALIC;
        Font f = new Font(mapFamily(family), st, Math.max(1, Math.round(size)));
        // "Comic Sans MS" is absent on most Linux JREs; Font falls back to
        // Dialog automatically for unknown families, which is what we want.
        return f.deriveFont(size);
    }
}

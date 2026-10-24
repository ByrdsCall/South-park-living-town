package android.text;
// Desktop-mirror shim (internal QA tooling only).
public class TextUtils {
    public enum TruncateAt { START, MIDDLE, END, MARQUEE }
    public static boolean isEmpty(CharSequence str) { return str == null || str.length() == 0; }
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        if (a == null || b == null || a.length() != b.length()) return false;
        return a.toString().equals(b.toString());
    }
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object t : tokens) { if (!first) sb.append(delimiter); sb.append(t); first = false; }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object t : tokens) { if (!first) sb.append(delimiter); sb.append(t); first = false; }
        return sb.toString();
    }
    public static CharSequence ellipsize(CharSequence text, android.text.TextPaint paint, float avail, TruncateAt where) {
        if (text == null) return "";
        if (paint.measureText(text.toString()) <= avail) return text;
        String s = text.toString();
        while (s.length() > 0 && paint.measureText(s + "…") > avail) s = s.substring(0, s.length() - 1);
        return s + "…";
    }
    public static String htmlEncode(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

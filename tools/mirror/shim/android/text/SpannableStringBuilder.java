package android.text;
// Desktop-mirror shim (internal QA tooling only). SpannableStringBuilder:
// text faithfully; span bookkeeping minimal (the mirror draws text itself).
public class SpannableStringBuilder implements Editable {
    private final StringBuilder sb = new StringBuilder();
    public SpannableStringBuilder() {}
    public SpannableStringBuilder(CharSequence text) { if (text != null) sb.append(text); }
    public SpannableStringBuilder(CharSequence text, int start, int end) {
        if (text != null) sb.append(text.subSequence(start, end));
    }
    public static SpannableStringBuilder valueOf(CharSequence source) {
        return source instanceof SpannableStringBuilder ? (SpannableStringBuilder) source : new SpannableStringBuilder(source);
    }
    public int length() { return sb.length(); }
    public char charAt(int index) { return sb.charAt(index); }
    public CharSequence subSequence(int start, int end) { return sb.substring(start, end); }
    public void getChars(int start, int end, char[] dest, int destoff) { sb.getChars(start, end, dest, destoff); }
    public Editable replace(int st, int en, CharSequence source, int start, int end) {
        sb.replace(st, en, source.subSequence(start, end).toString()); return this;
    }
    public Editable replace(int st, int en, CharSequence text) { sb.replace(st, en, text.toString()); return this; }
    public Editable insert(int where, CharSequence text, int start, int end) {
        sb.insert(where, text.subSequence(start, end).toString()); return this;
    }
    public Editable insert(int where, CharSequence text) { sb.insert(where, text.toString()); return this; }
    public Editable delete(int st, int en) { sb.delete(st, en); return this; }
    public Editable append(CharSequence text) { sb.append(text); return this; }
    public Editable append(CharSequence text, int start, int end) { sb.append(text.subSequence(start, end)); return this; }
    public Editable append(char text) { sb.append(text); return this; }
    public void clear() { sb.setLength(0); }
    public void clearSpans() {}
    public void setFilters(InputFilter[] filters) {}
    public InputFilter[] getFilters() { return new InputFilter[0]; }
    public void setSpan(Object what, int start, int end, int flags) {}
    public void removeSpan(Object what) {}
    @SuppressWarnings("unchecked")
    public <T> T[] getSpans(int start, int end, Class<T> type) {
        return (T[]) java.lang.reflect.Array.newInstance(type, 0);
    }
    public int getSpanStart(Object tag) { return -1; }
    public int getSpanEnd(Object tag) { return -1; }
    public int getSpanFlags(Object tag) { return 0; }
    public int nextSpanTransition(int start, int limit, Class type) { return limit; }
    @Override public String toString() { return sb.toString(); }
}

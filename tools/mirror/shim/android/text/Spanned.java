package android.text;
// Desktop-mirror shim (internal QA tooling only).
public interface Spanned extends CharSequence {
    int SPAN_EXCLUSIVE_EXCLUSIVE = 33;
    int SPAN_INCLUSIVE_INCLUSIVE = 17;
    <T> T[] getSpans(int start, int end, Class<T> type);
    int getSpanStart(Object tag);
    int getSpanEnd(Object tag);
    int getSpanFlags(Object tag);
    int nextSpanTransition(int start, int limit, Class type);
}

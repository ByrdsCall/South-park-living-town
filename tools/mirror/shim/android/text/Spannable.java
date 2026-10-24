package android.text;
// Desktop-mirror shim (internal QA tooling only).
public interface Spannable extends Spanned {
    void setSpan(Object what, int start, int end, int flags);
    void removeSpan(Object what);
}

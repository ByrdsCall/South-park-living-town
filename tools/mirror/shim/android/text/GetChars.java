package android.text;
// Desktop-mirror shim (internal QA tooling only).
public interface GetChars extends CharSequence {
    void getChars(int start, int end, char[] dest, int destoff);
}

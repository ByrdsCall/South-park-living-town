package android.view;
// Desktop-mirror shim (internal QA tooling only).
public interface Menu {
    int NONE = 0;
    MenuItem add(CharSequence title);
    MenuItem add(int groupId, int itemId, int order, CharSequence title);
    MenuItem add(int groupId, int itemId, int order, int titleRes);
    MenuItem findItem(int id);
    void removeItem(int id);
    void clear();
    int size();
    MenuItem getItem(int index);
    boolean hasVisibleItems();
}

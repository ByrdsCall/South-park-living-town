package android.database;
// Desktop-mirror shim (internal QA tooling only).
public interface Cursor {
    boolean moveToFirst();
    boolean moveToNext();
    int getCount();
    void close();
    boolean isClosed();
    String getString(int columnIndex);
    int getInt(int columnIndex);
    long getLong(int columnIndex);
    int getColumnIndex(String columnName);
    int getColumnIndexOrThrow(String columnName);
}

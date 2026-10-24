package android.content;
import android.net.Uri;
import java.io.InputStream;
// Desktop-mirror shim (internal QA tooling only).
public class ContentResolver {
    public InputStream openInputStream(Uri uri) throws Exception { return null; }
    public android.database.Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }
}

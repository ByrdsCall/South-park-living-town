package android.content.res;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
// Desktop-mirror shim (internal QA tooling only). Assets resolve against a
// real directory (MirrorMain points it at the project's assets/ folder —
// the same files the APK packages).
public class AssetManager {
    public static File baseDir = new File(".");
    public InputStream open(String fileName) throws IOException {
        return new FileInputStream(new File(baseDir, fileName));
    }
    public InputStream open(String fileName, int accessMode) throws IOException { return open(fileName); }
    public String[] list(String path) throws IOException {
        File d = path == null || path.isEmpty() ? baseDir : new File(baseDir, path);
        String[] names = d.list();
        return names == null ? new String[0] : names;
    }
    public static final int ACCESS_UNKNOWN = 0, ACCESS_RANDOM = 1, ACCESS_STREAMING = 2, ACCESS_BUFFER = 3;
}

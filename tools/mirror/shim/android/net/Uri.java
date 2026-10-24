package android.net;
import java.io.File;
// Desktop-mirror shim (internal QA tooling only).
public class Uri {
    private final String s;
    private Uri(String s) { this.s = s; }
    public static Uri parse(String uriString) { return new Uri(uriString); }
    public static Uri fromFile(File file) { return new Uri("file://" + file.getAbsolutePath()); }
    public String getPath() {
        String t = s;
        int i = t.indexOf("://");
        if (i >= 0) { int j = t.indexOf('/', i + 3); return j >= 0 ? t.substring(j) : "/"; }
        return t;
    }
    public String getScheme() { int i = s.indexOf(':'); return i > 0 ? s.substring(0, i) : null; }
    @Override public String toString() { return s; }
    @Override public boolean equals(Object o) { return o instanceof Uri && s.equals(((Uri) o).s); }
    @Override public int hashCode() { return s.hashCode(); }
    public static class Builder {
        private String scheme, authority, path = "";
        public Builder scheme(String s) { scheme = s; return this; }
        public Builder authority(String a) { authority = a; return this; }
        public Builder path(String p) { path = p; return this; }
        public Builder appendPath(String p) { path = path + "/" + p; return this; }
        public Uri build() { return new Uri((scheme == null ? "" : scheme + "://") + (authority == null ? "" : authority) + path); }
    }
}

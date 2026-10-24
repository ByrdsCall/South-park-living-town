package android.view;
import android.content.Context;
import org.xmlpull.v1.XmlPullParser;
// Desktop-mirror shim (internal QA tooling only). The game builds all UI
// in code; inflate exists only to satisfy imports.
public class LayoutInflater {
    public View inflate(int resource, ViewGroup root) { return null; }
    public View inflate(XmlPullParser parser, ViewGroup root) { return null; }
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) { return null; }
    public static LayoutInflater from(Context context) { return new LayoutInflater(); }
}

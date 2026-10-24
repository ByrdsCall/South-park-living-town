package android.content;
import android.net.Uri;
import android.os.Bundle;
import java.io.Serializable;
// Desktop-mirror shim (internal QA tooling only).
public class Intent {
    public static final String ACTION_VIEW = "android.intent.action.VIEW";
    public static final String ACTION_SEND = "android.intent.action.SEND";
    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String EXTRA_TEXT = "android.intent.extra.TEXT";
    public static final String EXTRA_SUBJECT = "android.intent.extra.SUBJECT";
    public static final int FLAG_ACTIVITY_NEW_TASK = 0x10000000;
    public static final int FLAG_ACTIVITY_CLEAR_TOP = 0x04000000;
    public static final int FLAG_ACTIVITY_SINGLE_TOP = 0x20000000;
    private String action;
    private ComponentName component;
    private final Bundle extras = new Bundle();
    private int flags;
    private Uri data;
    private String type;
    public Intent() {}
    public Intent(String action) { this.action = action; }
    public Intent(String action, Uri uri) { this.action = action; this.data = uri; }
    public Intent(Context packageContext, Class<?> cls) {
        component = new ComponentName(packageContext, cls);
    }
    public Intent(Intent o) {
        action = o.action; component = o.component; flags = o.flags; data = o.data; type = o.type;
        extras.putAll(o.extras);
    }
    public String getAction() { return action; }
    public Intent setAction(String a) { action = a; return this; }
    public ComponentName getComponent() { return component; }
    public Intent setComponent(ComponentName c) { component = c; return this; }
    public Intent setClass(Context ctx, Class<?> cls) { component = new ComponentName(ctx, cls); return this; }
    public Class<?> getTargetClass() { return component == null ? null : component.getCls(); }
    public Uri getData() { return data; }
    public Intent setData(Uri d) { data = d; return this; }
    public String getType() { return type; }
    public Intent setType(String t) { type = t; return this; }
    public Intent setDataAndType(Uri d, String t) { data = d; type = t; return this; }
    public int getFlags() { return flags; }
    public Intent setFlags(int f) { flags = f; return this; }
    public Intent addFlags(int f) { flags |= f; return this; }
    public Intent putExtra(String name, String value) { extras.putString(name, value); return this; }
    public Intent putExtra(String name, int value) { extras.putInt(name, value); return this; }
    public Intent putExtra(String name, long value) { extras.putLong(name, value); return this; }
    public Intent putExtra(String name, boolean value) { extras.putBoolean(name, value); return this; }
    public Intent putExtra(String name, float value) { extras.putFloat(name, value); return this; }
    public Intent putExtra(String name, double value) { extras.putDouble(name, value); return this; }
    public Intent putExtra(String name, String[] value) { extras.putStringArray(name, value); return this; }
    public Intent putExtra(String name, Serializable value) { extras.putSerializable(name, value); return this; }
    public Intent putExtra(String name, Bundle value) { extras.putBundle(name, value); return this; }
    public Intent putExtras(Bundle b) { extras.putAll(b); return this; }
    public Bundle getExtras() { return extras; }
    public boolean hasExtra(String name) { return extras.containsKey(name); }
    public String getStringExtra(String name) { return extras.getString(name); }
    public int getIntExtra(String name, int def) { return extras.getInt(name, def); }
    public long getLongExtra(String name, long def) { return extras.getLong(name, def); }
    public boolean getBooleanExtra(String name, boolean def) { return extras.getBoolean(name, def); }
    public float getFloatExtra(String name, float def) { return extras.getFloat(name, def); }
    public double getDoubleExtra(String name, double def) { return extras.getDouble(name, def); }
    public Serializable getSerializableExtra(String name) { return extras.getSerializable(name); }
    public String[] getStringArrayExtra(String name) { return extras.getStringArray(name); }
    public static Intent createChooser(Intent target, CharSequence title) { return target; }
}

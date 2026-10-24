package android.content;
// Desktop-mirror shim (internal QA tooling only).
public class ComponentName {
    private final String pkg;
    private final Class<?> cls;
    public ComponentName(Context ctx, Class<?> cls) {
        this.pkg = ctx == null ? "" : ctx.getPackageName();
        this.cls = cls;
    }
    public ComponentName(String pkg, String clsName) {
        this.pkg = pkg;
        Class<?> c = null;
        try { c = Class.forName(clsName); } catch (Exception e) { /* name-only */ }
        this.cls = c;
    }
    public String getPackageName() { return pkg; }
    public String getClassName() { return cls == null ? "" : cls.getName(); }
    public Class<?> getCls() { return cls; }
}

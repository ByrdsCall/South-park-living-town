package android.content;
import android.content.res.AssetManager;
import android.content.res.Resources;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
// Desktop-mirror shim (internal QA tooling only).
public abstract class Context {
    public static final int MODE_PRIVATE = 0;
    public static final int MODE_WORLD_READABLE = 1;
    public static final int MODE_WORLD_WRITEABLE = 2;
    public static final int MODE_APPEND = 32768;
    public static final int BIND_AUTO_CREATE = 1;
    public static final String WINDOW_SERVICE = "window";
    public static final String AUDIO_SERVICE = "audio";
    public static final String VIBRATOR_SERVICE = "vibrator";
    public static final String POWER_SERVICE = "power";
    public static final String LAYOUT_INFLATER_SERVICE = "layout_inflater";
    public static final String ACTIVITY_SERVICE = "activity";
    public static final String CLIPBOARD_SERVICE = "clipboard";
    public static final String NOTIFICATION_SERVICE = "notification";
    public static final String CONNECTIVITY_SERVICE = "connectivity";
    public static final String INPUT_METHOD_SERVICE = "input_method";
    public static final String SENSOR_SERVICE = "sensor";
    public static final String TELEPHONY_SERVICE = "phone";
    public static final String DOWNLOAD_SERVICE = "download";
    public static final String MEDIA_SESSION_SERVICE = "media_session";

    /** Mirror-wide data dir (saves): set by MirrorMain before any game code runs. */
    public static File dataDir = new File(System.getProperty("user.home"), ".sptown-mirror");
    private static final Map<String, SharedPreferences> PREFS = new HashMap<>();
    private Resources resources;

    public SharedPreferences getSharedPreferences(String name, int mode) {
        synchronized (PREFS) {
            SharedPreferences p = PREFS.get(name);
            if (p == null) {
                p = new FileSharedPreferences(new File(dataDir, "prefs"), name);
                PREFS.put(name, p);
            }
            return p;
        }
    }
    public Resources getResources() {
        if (resources == null) resources = new Resources();
        return resources;
    }
    public AssetManager getAssets() { return new AssetManager(); }
    public String getPackageName() { return "com.hunt3r.southparktown"; }
    public Context getApplicationContext() { return this; }
    public Object getSystemService(String name) {
        if (AUDIO_SERVICE.equals(name)) return new android.media.AudioManager();
        if (WINDOW_SERVICE.equals(name)) return WINDOW_MANAGER_SINGLETON;
        return null;
    }
    private static final android.view.WindowManager WINDOW_MANAGER_SINGLETON = new android.view.WindowManager() {
        public android.view.Display getDefaultDisplay() { return new android.view.Display(); }
        public void removeViewImmediate(android.view.View view) {}
    };
    @SuppressWarnings("unchecked")
    public <T> T getSystemService(Class<T> serviceClass) { return null; }
    public void startActivity(Intent intent) {
        // The mirror host registers a launcher hook (MirrorMain) so a
        // startActivity lands as "open this screen in the mirror window"
        // where supported, else is logged — never silently dropped.
        if (MirrorHooks.activityLauncher != null) MirrorHooks.activityLauncher.accept(intent);
        else System.out.println("[mirror] startActivity: " + (intent == null ? "null" : String.valueOf(intent.getTargetClass())));
    }
    public void startActivity(Intent intent, android.os.Bundle options) { startActivity(intent); }
    public boolean startActivityIfNeeded(Intent intent, int requestCode) { startActivity(intent); return true; }
    public void sendBroadcast(Intent intent) {}
    public File getFilesDir() { File f = new File(dataDir, "files"); f.mkdirs(); return f; }
    public File getCacheDir() { File f = new File(dataDir, "cache"); f.mkdirs(); return f; }
    public File getExternalFilesDir(String type) { return getFilesDir(); }
    public File getDataDir() { return dataDir; }
    public String getString(int resId) { return ""; }
    public String getString(int resId, Object... formatArgs) { return String.format(getString(resId), formatArgs); }
    public CharSequence getText(int resId) { return getString(resId); }
    public android.graphics.drawable.Drawable getDrawable(int id) { return getResources().getDrawable(id); }
    public int getColor(int id) { return getResources().getColor(id); }
    public int checkSelfPermission(String permission) { return 0; }
    public void enforcePermission(String perm, String msg) {}
    public ContentResolver getContentResolver() { return null; }
    public ClassLoader getClassLoader() { return getClass().getClassLoader(); }
    public android.content.pm.ApplicationInfo getApplicationInfo() { return new android.content.pm.ApplicationInfo(); }
    public ContentResolver contentResolver() { return null; }
}

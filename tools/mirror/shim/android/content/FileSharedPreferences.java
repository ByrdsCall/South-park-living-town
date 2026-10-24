package android.content;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
// Desktop-mirror shim (internal QA tooling only). File-backed
// SharedPreferences: values persist as typed properties in the mirror's
// own data dir. The GAME's save format (Prefs' JSON strings) rides inside
// untouched — identical bytes to what Android stores and reads back.
public class FileSharedPreferences implements SharedPreferences {
    private final File file;
    private final Map<String, Object> map = new HashMap<>();
    FileSharedPreferences(File dir, String name) {
        file = new File(dir, name + ".properties");
        load();
    }
    private synchronized void load() {
        map.clear();
        if (!file.exists()) return;
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            p.load(in);
            for (String k : p.stringPropertyNames()) {
                String v = p.getProperty(k);
                if (v == null || v.length() < 2 || v.charAt(1) != ':') continue;
                String body = v.substring(2);
                switch (v.charAt(0)) {
                    case 'S': map.put(k, body.replace("\\n", "\n").replace("\\\\", "\\")); break;
                    case 'I': try { map.put(k, Integer.parseInt(body)); } catch (Exception e) {} break;
                    case 'L': try { map.put(k, Long.parseLong(body)); } catch (Exception e) {} break;
                    case 'F': try { map.put(k, Float.parseFloat(body)); } catch (Exception e) {} break;
                    case 'B': map.put(k, Boolean.parseBoolean(body)); break;
                    default: map.put(k, body);
                }
            }
        } catch (Exception e) { /* fresh start */ }
    }
    private synchronized void store() {
        Properties p = new Properties();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            Object v = e.getValue();
            String enc;
            if (v instanceof Integer) enc = "I:" + v;
            else if (v instanceof Long) enc = "L:" + v;
            else if (v instanceof Float) enc = "F:" + v;
            else if (v instanceof Boolean) enc = "B:" + v;
            else if (v instanceof Set) continue;
            else enc = "S:" + String.valueOf(v).replace("\\", "\\\\").replace("\n", "\\n");
            p.setProperty(e.getKey(), enc);
        }
        try {
            file.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(file)) { p.store(out, "sptown mirror prefs"); }
        } catch (Exception e) { System.err.println("prefs store failed: " + e); }
    }
    public synchronized Map<String, ?> getAll() { return new HashMap<>(map); }
    public synchronized String getString(String key, String def) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : def;
    }
    public synchronized Set<String> getStringSet(String key, Set<String> def) {
        Object v = map.get(key);
        if (v instanceof Set) { @SuppressWarnings("unchecked") Set<String> s = (Set<String>) v; return s; }
        return def;
    }
    public synchronized int getInt(String key, int def) {
        Object v = map.get(key); return v instanceof Number ? ((Number) v).intValue() : def;
    }
    public synchronized long getLong(String key, long def) {
        Object v = map.get(key); return v instanceof Number ? ((Number) v).longValue() : def;
    }
    public synchronized float getFloat(String key, float def) {
        Object v = map.get(key); return v instanceof Number ? ((Number) v).floatValue() : def;
    }
    public synchronized boolean getBoolean(String key, boolean def) {
        Object v = map.get(key); return v instanceof Boolean ? (Boolean) v : def;
    }
    public synchronized boolean contains(String key) { return map.containsKey(key); }
    public Editor edit() { return new Ed(); }
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {}
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) {}
    private class Ed implements Editor {
        private final Map<String, Object> delta = new HashMap<>();
        private final Set<String> removed = new HashSet<>();
        private boolean cleared = false;
        public Editor putString(String k, String v) { delta.put(k, v); removed.remove(k); return this; }
        public Editor putStringSet(String k, Set<String> v) { delta.put(k, v == null ? null : new HashSet<>(v)); removed.remove(k); return this; }
        public Editor putInt(String k, int v) { delta.put(k, v); removed.remove(k); return this; }
        public Editor putLong(String k, long v) { delta.put(k, v); removed.remove(k); return this; }
        public Editor putFloat(String k, float v) { delta.put(k, v); removed.remove(k); return this; }
        public Editor putBoolean(String k, boolean v) { delta.put(k, v); removed.remove(k); return this; }
        public Editor remove(String k) { delta.remove(k); removed.add(k); return this; }
        public Editor clear() { cleared = true; delta.clear(); removed.clear(); return this; }
        public boolean commit() { apply(); return true; }
        public void apply() {
            synchronized (FileSharedPreferences.this) {
                if (cleared) map.clear();
                for (String k : removed) map.remove(k);
                for (Map.Entry<String, Object> e : delta.entrySet()) {
                    if (e.getValue() == null) map.remove(e.getKey()); else map.put(e.getKey(), e.getValue());
                }
                store();
            }
        }
    }
}

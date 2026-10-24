package android.os;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
// Desktop-mirror shim (internal QA tooling only). Bundle over a HashMap.
public class Bundle implements Cloneable {
    private final HashMap<String, Object> map = new HashMap<>();
    public Bundle() {}
    public Bundle(Bundle b) { if (b != null) map.putAll(b.map); }
    public int size() { return map.size(); }
    public boolean isEmpty() { return map.isEmpty(); }
    public void clear() { map.clear(); }
    public boolean containsKey(String key) { return map.containsKey(key); }
    public void remove(String key) { map.remove(key); }
    public Set<String> keySet() { return map.keySet(); }
    public Object get(String key) { return map.get(key); }
    public void putString(String key, String value) { map.put(key, value); }
    public String getString(String key) { Object v = map.get(key); return v instanceof String ? (String) v : null; }
    public String getString(String key, String def) { String v = getString(key); return v == null ? def : v; }
    public void putInt(String key, int value) { map.put(key, value); }
    public int getInt(String key) { return getInt(key, 0); }
    public int getInt(String key, int def) { Object v = map.get(key); return v instanceof Number ? ((Number) v).intValue() : def; }
    public void putLong(String key, long value) { map.put(key, value); }
    public long getLong(String key) { return getLong(key, 0L); }
    public long getLong(String key, long def) { Object v = map.get(key); return v instanceof Number ? ((Number) v).longValue() : def; }
    public void putFloat(String key, float value) { map.put(key, value); }
    public float getFloat(String key) { return getFloat(key, 0f); }
    public float getFloat(String key, float def) { Object v = map.get(key); return v instanceof Number ? ((Number) v).floatValue() : def; }
    public void putDouble(String key, double value) { map.put(key, value); }
    public double getDouble(String key) { return getDouble(key, 0); }
    public double getDouble(String key, double def) { Object v = map.get(key); return v instanceof Number ? ((Number) v).doubleValue() : def; }
    public void putBoolean(String key, boolean value) { map.put(key, value); }
    public boolean getBoolean(String key) { return getBoolean(key, false); }
    public boolean getBoolean(String key, boolean def) { Object v = map.get(key); return v instanceof Boolean ? (Boolean) v : def; }
    public void putSerializable(String key, Serializable value) { map.put(key, value); }
    public Serializable getSerializable(String key) { Object v = map.get(key); return v instanceof Serializable ? (Serializable) v : null; }
    public void putStringArray(String key, String[] value) { map.put(key, value); }
    public String[] getStringArray(String key) { Object v = map.get(key); return v instanceof String[] ? (String[]) v : null; }
    public void putIntArray(String key, int[] value) { map.put(key, value); }
    public int[] getIntArray(String key) { Object v = map.get(key); return v instanceof int[] ? (int[]) v : null; }
    public void putBundle(String key, Bundle value) { map.put(key, value); }
    public Bundle getBundle(String key) { Object v = map.get(key); return v instanceof Bundle ? (Bundle) v : null; }
    public void putParcelable(String key, Object value) { map.put(key, value); }
    @SuppressWarnings("unchecked")
    public <T> T getParcelable(String key) { return (T) map.get(key); }
    public void putAll(Bundle b) { if (b != null) map.putAll(b.map); }
    @Override public Object clone() { return new Bundle(this); }
}

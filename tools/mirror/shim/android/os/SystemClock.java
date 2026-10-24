package android.os;
// Desktop-mirror shim (internal QA tooling only).
public final class SystemClock {
    private SystemClock() {}
    public static long uptimeMillis() { return System.nanoTime() / 1000000L; }
    public static long elapsedRealtime() { return uptimeMillis(); }
    public static long elapsedRealtimeNanos() { return System.nanoTime(); }
    public static long currentThreadTimeMillis() { return System.currentTimeMillis(); }
    public static long currentTimeMillis() { return System.currentTimeMillis(); }
    public static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}

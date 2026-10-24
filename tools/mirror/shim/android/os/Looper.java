package android.os;
import javax.swing.SwingUtilities;
// Desktop-mirror shim (internal QA tooling only). The Android main looper
// maps onto the Swing event-dispatch thread.
public final class Looper {
    private static final Looper MAIN = new Looper();
    private final Thread thread;
    private Looper() { thread = null; }
    private Looper(Thread t) { thread = t; }
    public static Looper getMainLooper() { return MAIN; }
    public static Looper myLooper() { return SwingUtilities.isEventDispatchThread() ? MAIN : null; }
    public Thread getThread() { return thread; }
    public static void prepare() {}
    public static void loop() {}
    public void quit() {}
    public void quitSafely() {}
}

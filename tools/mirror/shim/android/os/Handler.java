package android.os;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;
// Desktop-mirror shim (internal QA tooling only). Handler over the Swing
// EDT: post = invokeLater, postDelayed = javax.swing.Timer one-shot.
public class Handler {
    public interface Callback { boolean handleMessage(Message msg); }
    private final Map<Runnable, javax.swing.Timer> timers = new ConcurrentHashMap<>();
    private final Callback callback;
    public Handler() { callback = null; }
    public Handler(Looper looper) { callback = null; }
    public Handler(Callback cb) { callback = cb; }
    public Handler(Looper looper, Callback cb) { callback = cb; }
    public boolean post(Runnable r) {
        if (r == null) return false;
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
        return true;
    }
    public boolean postDelayed(Runnable r, long delayMillis) {
        if (r == null) return false;
        if (delayMillis <= 0) return post(r);
        javax.swing.Timer t = new javax.swing.Timer((int) Math.min(delayMillis, Integer.MAX_VALUE), e -> {
            timers.remove(r);
            r.run();
        });
        t.setRepeats(false);
        timers.put(r, t);
        t.start();
        return true;
    }
    public boolean postAtFrontOfQueue(Runnable r) { return post(r); }
    public boolean postAtTime(Runnable r, long uptimeMillis) {
        long delay = uptimeMillis - SystemClock.uptimeMillis();
        return delay <= 0 ? post(r) : postDelayed(r, delay);
    }
    public void removeCallbacks(Runnable r) {
        javax.swing.Timer t = timers.remove(r);
        if (t != null) t.stop();
    }
    public void removeCallbacksAndMessages(Object token) {
        for (Runnable r : timers.keySet()) removeCallbacks(r);
    }
    public void removeMessages(int what) {}
    public boolean hasMessages(int what) { return false; }
    public Message obtainMessage(int what, Object obj) {
        Message m = new Message();
        m.what = what; m.obj = obj;
        m.target = this;
        return m;
    }
    public Message obtainMessage() { Message m = new Message(); m.target = this; return m; }
    public boolean sendMessage(Message msg) {
        if (callback != null) callback.handleMessage(msg);
        else handleMessage(msg);
        return true;
    }
    public boolean sendMessageDelayed(Message msg, long delayMillis) {
        postDelayed(() -> sendMessage(msg), delayMillis);
        return true;
    }
    public boolean sendEmptyMessage(int what) {
        Message m = obtainMessage(what, null);
        return sendMessage(m);
    }
    public boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        return sendMessageDelayed(obtainMessage(what, null), delayMillis);
    }
    public void handleMessage(Message msg) {}
    public Looper getLooper() { return Looper.getMainLooper(); }
}

package android.os;
// Desktop-mirror shim (internal QA tooling only).
public class Message {
    public int what, arg1, arg2;
    public Object obj;
    Handler target;
    public Message() {}
    public static Message obtain() { return new Message(); }
    public static Message obtain(Handler h, int what, Object obj) {
        Message m = new Message(); m.target = h; m.what = what; m.obj = obj; return m;
    }
    public void sendToTarget() { if (target != null) target.sendMessage(this); }
}

package android.view;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class MotionEvent {
    public static final int ACTION_MASK = 0xff;
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_CANCEL = 3;
    public static final int ACTION_OUTSIDE = 4;
    public static final int ACTION_POINTER_DOWN = 5;
    public static final int ACTION_POINTER_UP = 6;
    public static final int ACTION_SCROLL = 8;
    public static final int BUTTON_PRIMARY = 1;
    public static final int TOOL_TYPE_FINGER = 1;
    public static final int TOOL_TYPE_MOUSE = 3;

    private final int action;
    private final float x, y;
    private final long downTime, eventTime;
    private final int metaState;
    private final int source;

    private MotionEvent(int action, float x, float y, long downTime, long eventTime, int metaState, int source) {
        this.action = action; this.x = x; this.y = y;
        this.downTime = downTime; this.eventTime = eventTime;
        this.metaState = metaState; this.source = source;
    }
    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        return new MotionEvent(action, x, y, downTime, eventTime, metaState, 0x1002);
    }
    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState, int source) {
        return new MotionEvent(action, x, y, downTime, eventTime, metaState, source);
    }
    public static MotionEvent obtain(MotionEvent o) {
        return new MotionEvent(o.action, o.x, o.y, o.downTime, o.eventTime, o.metaState, o.source);
    }
    public final int getAction() { return action; }
    public final int getActionMasked() { return action & ACTION_MASK; }
    public final int getActionIndex() { return 0; }
    public final float getX() { return x; }
    public final float getY() { return y; }
    public final float getX(int pointerIndex) { return x; }
    public final float getY(int pointerIndex) { return y; }
    public final long getDownTime() { return downTime; }
    public final long getEventTime() { return eventTime; }
    public final int getMetaState() { return metaState; }
    public final int getPointerCount() { return 1; }
    public final int getPointerId(int index) { return 0; }
    public final int findPointerIndex(int pointerId) { return 0; }
    public final float getPressure() { return 1f; }
    public final float getSize() { return 1f; }
    public final int getSource() { return source; }
    public final int getToolType(int pointerIndex) { return TOOL_TYPE_FINGER; }
    public final int getButtonState() { return 0; }
    public final float getAxisValue(int axis) { return 0f; }
    public void setLocation(float x, float y) {}
    public void offsetLocation(float dx, float dy) {}
    public void recycle() {}
    public static String actionToString(int action) { return "ACTION_" + action; }
}

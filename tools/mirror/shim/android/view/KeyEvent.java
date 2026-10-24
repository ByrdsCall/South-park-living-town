package android.view;
// Desktop-mirror shim (internal QA tooling only).
public class KeyEvent {
    public static final int ACTION_DOWN = 0, ACTION_UP = 1;
    public static final int KEYCODE_BACK = 4, KEYCODE_MENU = 82, KEYCODE_ENTER = 66,
            KEYCODE_DPAD_CENTER = 23, KEYCODE_DEL = 67, KEYCODE_ESCAPE = 111;
    private final int action, keyCode;
    public KeyEvent(int action, int code) { this.action = action; this.keyCode = code; }
    public int getAction() { return action; }
    public int getKeyCode() { return keyCode; }
    public boolean isCanceled() { return false; }
}

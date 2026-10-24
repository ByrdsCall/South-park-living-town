package android.view;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class Gravity {
    public static final int NO_GRAVITY = 0x0000;
    public static final int AXIS_SPECIFIED = 0x0001;
    public static final int AXIS_PULL_BEFORE = 0x0002;
    public static final int AXIS_PULL_AFTER = 0x0004;
    public static final int AXIS_X_SHIFT = 0;
    public static final int AXIS_Y_SHIFT = 4;
    public static final int TOP = 0x0030;
    public static final int BOTTOM = 0x0050;
    public static final int LEFT = 0x0003;
    public static final int RIGHT = 0x0005;
    public static final int CENTER_VERTICAL = 0x0010;
    public static final int FILL_VERTICAL = 0x0070;
    public static final int CENTER_HORIZONTAL = 0x0001;
    public static final int FILL_HORIZONTAL = 0x0007;
    public static final int CENTER = CENTER_VERTICAL | CENTER_HORIZONTAL;
    public static final int FILL = FILL_VERTICAL | FILL_HORIZONTAL;
    public static final int CLIP_HORIZONTAL = 0x0008;
    public static final int CLIP_VERTICAL = 0x0080;
    public static final int RELATIVE_LAYOUT_DIRECTION = 0x00800000;
    public static final int START = RELATIVE_LAYOUT_DIRECTION | LEFT;
    public static final int END = RELATIVE_LAYOUT_DIRECTION | RIGHT;
    public static final int HORIZONTAL_GRAVITY_MASK = 0x0007 | RELATIVE_LAYOUT_DIRECTION;
    public static final int VERTICAL_GRAVITY_MASK = 0x0070;
    public static int getAbsoluteGravity(int gravity, int layoutDirection) {
        int g = gravity;
        if ((g & START) == START) g = (g & ~START) | LEFT;
        else if ((g & END) == END) g = (g & ~END) | RIGHT;
        return g & ~RELATIVE_LAYOUT_DIRECTION;
    }
    public static boolean isVertical(int gravity) { return (gravity & VERTICAL_GRAVITY_MASK) != 0; }
    public static boolean isHorizontal(int gravity) { return (gravity & HORIZONTAL_GRAVITY_MASK) != 0; }
}

package android.view;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
// Desktop-mirror shim (internal QA tooling only).
public interface WindowManager {
    Display getDefaultDisplay();
    void removeViewImmediate(View view);
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int type, format, flags, gravity, x, y, token;
        public float alpha = 1f, dimAmount = 0f, screenBrightness = -1f, buttonBrightness = -1f;
        public int softInputMode, layoutInDisplayCutoutMode;
        public CharSequence title;
        public static final int TYPE_BASE_APPLICATION = 1;
        public static final int TYPE_APPLICATION = 2;
        public static final int TYPE_APPLICATION_OVERLAY = 2038;
        public static final int TYPE_PHONE = 2002;
        public static final int TYPE_SYSTEM_ALERT = 2003;
        public static final int FIRST_APPLICATION_WINDOW = 1;
        public static final int FLAG_NOT_FOCUSABLE = 0x00000008;
        public static final int FLAG_NOT_TOUCHABLE = 0x00000010;
        public static final int FLAG_NOT_TOUCH_MODAL = 0x00000020;
        public static final int FLAG_DIM_BEHIND = 0x00000002;
        public static final int FLAG_BLUR_BEHIND = 0x00000004;
        public static final int FLAG_KEEP_SCREEN_ON = 0x00000080;
        public static final int FLAG_LAYOUT_IN_SCREEN = 0x00000100;
        public static final int FLAG_FULLSCREEN = 0x00000400;
        public static final int FLAG_FORCE_NOT_FULLSCREEN = 0x00000800;
        public static final int FLAG_SECURE = 0x00002000;
        public static final int FLAG_TRANSLUCENT_STATUS = 0x04000000;
        public static final int FLAG_TRANSLUCENT_NAVIGATION = 0x08000000;
        public static final int FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS = 0x80000000;
        public static final int SOFT_INPUT_STATE_UNSPECIFIED = 0;
        public static final int SOFT_INPUT_ADJUST_NOTHING = 0x30;
        public static final int SOFT_INPUT_ADJUST_RESIZE = 0x10;
        public static final int SOFT_INPUT_ADJUST_PAN = 0x20;
        public LayoutParams() { super(MATCH_PARENT, MATCH_PARENT); format = PixelFormat.OPAQUE; }
        public LayoutParams(int width, int height) { super(width, height); format = PixelFormat.OPAQUE; }
        public LayoutParams(int width, int height, int type, int flags, int format) {
            super(width, height); this.type = type; this.flags = flags; this.format = format;
        }
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(LayoutParams o) {
            super(o);
            type = o.type; format = o.format; flags = o.flags; gravity = o.gravity;
            x = o.x; y = o.y; alpha = o.alpha; dimAmount = o.dimAmount;
            softInputMode = o.softInputMode; title = o.title;
        }
        public final int copyFrom(LayoutParams o) {
            width = o.width; height = o.height; type = o.type; format = o.format; flags = o.flags;
            gravity = o.gravity; x = o.x; y = o.y; alpha = o.alpha; dimAmount = o.dimAmount;
            softInputMode = o.softInputMode; title = o.title;
            return 0;
        }
    }
}

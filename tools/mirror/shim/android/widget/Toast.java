package android.widget;
import android.content.Context;
import android.content.MirrorHooks;
import android.view.View;
// Desktop-mirror shim (internal QA tooling only). Toasts surface on the
// mirror's status line / console so QA sees the same messages the phone shows.
public class Toast {
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;
    private CharSequence text;
    private int duration;
    private Toast() {}
    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast t = new Toast();
        t.text = text; t.duration = duration;
        return t;
    }
    public static Toast makeText(Context context, int resId, int duration) {
        return makeText(context, context.getString(resId), duration);
    }
    public void show() {
        if (MirrorHooks.toaster != null) MirrorHooks.toaster.accept(String.valueOf(text));
        else System.out.println("[toast] " + text);
    }
    public void cancel() {}
    public void setText(CharSequence s) { text = s; }
    public void setDuration(int d) { duration = d; }
    public void setGravity(int gravity, int xOffset, int yOffset) {}
    public void setMargin(float horizontalMargin, float verticalMargin) {}
    public void setView(View view) {}
    public View getView() { return null; }
    public int getDuration() { return duration; }
}

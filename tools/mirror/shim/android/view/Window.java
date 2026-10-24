package android.view;
import android.content.Context;
import android.graphics.drawable.Drawable;
// Desktop-mirror shim (internal QA tooling only).
public class Window {
    public static final int FEATURE_NO_TITLE = 7;
    public static final int FEATURE_PROGRESS = 2;
    private final Context context;
    private final WindowManager.LayoutParams attrs = new WindowManager.LayoutParams();
    private View decor;
    private int softInputMode;
    public Window(Context context) { this.context = context; }
    public Context getContext() { return context; }
    public boolean requestFeature(int featureId) { return true; }
    public void setFlags(int flags, int mask) { attrs.flags = (attrs.flags & ~mask) | (flags & mask); }
    public void addFlags(int flags) { attrs.flags |= flags; }
    public void clearFlags(int flags) { attrs.flags &= ~flags; }
    public boolean hasFlag(int flag) { return (attrs.flags & flag) != 0; }
    public WindowManager.LayoutParams getAttributes() { return attrs; }
    public void setAttributes(WindowManager.LayoutParams a) { attrs.copyFrom(a); }
    public void setLayout(int width, int height) { attrs.width = width; attrs.height = height; }
    public void setGravity(int gravity) { attrs.gravity = gravity; }
    public void setType(int type) { attrs.type = type; }
    public void setBackgroundDrawable(Drawable drawable) {}
    public void setBackgroundDrawableResource(int resId) {}
    public void setDimAmount(float amount) { attrs.dimAmount = amount; }
    public void setSoftInputMode(int mode) { softInputMode = mode; }
    public int getSoftInputMode() { return softInputMode; }
    public View getDecorView() {
        if (decor == null) decor = new View(context);
        return decor;
    }
    public View peekDecorView() { return decor; }
    public void setContentView(View view) {}
    public void setTitle(CharSequence title) {}
    public void setWindowAnimations(int resId) {}
    public int getForcedWindowFlags() { return 0; }
}

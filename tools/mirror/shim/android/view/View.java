package android.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// android.view.View: state + lifecycle surface the game compiles against.
// The mirror host drives onDraw/onTouchEvent directly and repaints through
// the invalidateHook the host installs on the root view.
public class View {
    public static final int VISIBLE = 0;
    public static final int INVISIBLE = 4;
    public static final int GONE = 8;
    public static final int NO_ID = -1;
    public static final int FOCUS_BACKWARD = 1;
    public static final int FOCUS_FORWARD = 2;
    public static final int FOCUS_LEFT = 17;
    public static final int FOCUS_UP = 33;
    public static final int FOCUS_RIGHT = 66;
    public static final int FOCUS_DOWN = 130;
    public static final int SCROLLBARS_INSIDE_OVERLAY = 0;
    public static final int OVER_SCROLL_ALWAYS = 0;
    public static final int LAYER_TYPE_NONE = 0;
    public static final int LAYER_TYPE_SOFTWARE = 1;
    public static final int LAYER_TYPE_HARDWARE = 2;
    public static final int TEXT_ALIGNMENT_INHERIT = 0;
    public static final int IMPORTANT_FOR_ACCESSIBILITY_AUTO = 0;

    public interface OnClickListener { void onClick(View v); }
    public interface OnTouchListener { boolean onTouch(View v, MotionEvent event); }
    public interface OnLongClickListener { boolean onLongClick(View v); }
    public interface OnFocusChangeListener { void onFocusChange(View v, boolean hasFocus); }
    public interface OnAttachStateChangeListener {
        void onViewAttachedToWindow(View v);
        void onViewDetachedFromWindow(View v);
    }

    /** Host repaint hook: the mirror sets this on the root GameView. */
    public Runnable invalidateHook;

    protected final Context context;
    private int id = NO_ID;
    ViewGroup parent;
    private int visibility = VISIBLE;
    private float alpha = 1f;
    private boolean enabled = true;
    private boolean clickable = false;
    private boolean focusable = false;
    private boolean attached = false;
    int left, top, right, bottom;
    int measuredWidth, measuredHeight;
    private int paddingLeft, paddingTop, paddingRight, paddingBottom;
    private ViewGroup.LayoutParams layoutParams;
    private OnClickListener clickListener;
    private OnTouchListener touchListener;
    private Object tag;
    private CharSequence contentDescription;
    private float translationX, translationY, scaleX = 1f, scaleY = 1f, rotation = 0f;
    private Drawable background;
    private final Handler handler = new Handler();
    private boolean willNotDraw = false;
    private int scrollX, scrollY;
    private int minimumWidth, minimumHeight;

    public View(Context context) { this.context = context; }
    public View(Context context, AttributeSet attrs) { this.context = context; }
    public View(Context context, AttributeSet attrs, int defStyleAttr) { this.context = context; }

    public Context getContext() { return context; }
    public Resources getResources() { return context == null ? new Resources() : context.getResources(); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Object getTag() { return tag; }
    public void setTag(Object tag) { this.tag = tag; }
    public ViewGroup getParent() { return parent; }
    void setParent(ViewGroup p) { parent = p; }

    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }
    public boolean isShown() {
        View v = this;
        while (v != null) {
            if (v.visibility != VISIBLE) return false;
            v = v.parent;
        }
        return true;
    }
    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = alpha; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isClickable() { return clickable; }
    public void setClickable(boolean clickable) { this.clickable = clickable; }
    public boolean isFocusable() { return focusable; }
    public void setFocusable(boolean focusable) { this.focusable = focusable; }
    public void setFocusableInTouchMode(boolean b) {}
    public boolean requestFocus() { return false; }
    public void clearFocus() {}
    public boolean isFocused() { return false; }

    public int getWidth() { return right - left; }
    public int getHeight() { return bottom - top; }
    public int getLeft() { return left; }
    public int getTop() { return top; }
    public int getRight() { return right; }
    public int getBottom() { return bottom; }
    public int getMeasuredWidth() { return measuredWidth; }
    public int getMeasuredHeight() { return measuredHeight; }
    public int getMeasuredWidthAndState() { return measuredWidth; }
    public int getMeasuredHeightAndState() { return measuredHeight; }

    public static class MeasureSpec {
        public static final int UNSPECIFIED = 0;
        public static final int EXACTLY = 0x40000000;
        public static final int AT_MOST = 0x80000000;
        private static final int MODE_MASK = 0xC0000000;
        private static final int SIZE_MASK = 0x3FFFFFFF;
        public static int makeMeasureSpec(int size, int mode) { return (size & SIZE_MASK) | (mode & MODE_MASK); }
        public static int getMode(int spec) { return spec & MODE_MASK; }
        public static int getSize(int spec) { return spec & SIZE_MASK; }
        public static int adjust(int spec, int delta) {
            return makeMeasureSpec(getSize(spec) + delta, getMode(spec));
        }
    }
    public static int getDefaultSize(int size, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        return mode == MeasureSpec.UNSPECIFIED ? size : MeasureSpec.getSize(measureSpec);
    }
    public static int resolveSize(int size, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        if (mode == MeasureSpec.EXACTLY) return MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.AT_MOST) return Math.min(size, MeasureSpec.getSize(measureSpec));
        return size;
    }
    protected int getSuggestedMinimumWidth() { return minimumWidth; }
    protected int getSuggestedMinimumHeight() { return minimumHeight; }
    public void setMinimumWidth(int w) { minimumWidth = w; }
    private int minWidth, minHeight;
    public int getMinWidth() { return minWidth; }
    public void setMinWidth(int w) { minWidth = w; }
    public int getMinHeight() { return minHeight; }
    public void setMinHeight(int h) { minHeight = h; }
    public int getMinimumWidth() { return minimumWidth; }
    public void setMinimumHeight(int h) { minimumHeight = h; }
    public int getMinimumHeight() { return minimumHeight; }
    protected final void setMeasuredDimension(int w, int h) { measuredWidth = w; measuredHeight = h; }
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
    public final void layout(int l, int t, int r, int b) {
        boolean changed = l != left || t != top || r != right || b != bottom;
        int oldW = getWidth(), oldH = getHeight();
        left = l; top = t; right = r; bottom = b;
        onLayout(changed, l, t, r, b);
        if (changed) onSizeChanged(getWidth(), getHeight(), oldW, oldH);
    }
    protected void onLayout(boolean changed, int l, int t, int r, int b) {}
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {}

    public int getPaddingLeft() { return paddingLeft; }
    public int getPaddingTop() { return paddingTop; }
    public int getPaddingRight() { return paddingRight; }
    public int getPaddingBottom() { return paddingBottom; }
    public int getPaddingStart() { return paddingLeft; }
    public int getPaddingEnd() { return paddingRight; }
    public void setPadding(int l, int t, int r, int b) {
        paddingLeft = l; paddingTop = t; paddingRight = r; paddingBottom = b;
    }
    public void setPaddingRelative(int start, int t, int end, int b) { setPadding(start, t, end, b); }

    public ViewGroup.LayoutParams getLayoutParams() { return layoutParams; }
    public void setLayoutParams(ViewGroup.LayoutParams params) { layoutParams = params; }
    public void requestLayout() { if (parent != null) parent.requestLayout(); }

    public void draw(Canvas canvas) {
        if (visibility != VISIBLE) return;
        onDraw(canvas);
    }
    protected void onDraw(Canvas canvas) {}
    public boolean onTouchEvent(MotionEvent event) { return false; }
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (touchListener != null && touchListener.onTouch(this, event)) return true;
        return onTouchEvent(event);
    }
    public void setOnClickListener(OnClickListener l) { clickListener = l; clickable = l != null; }
    public OnClickListener getOnClickListenerForTest() { return clickListener; }
    public boolean hasOnClickListeners() { return clickListener != null; }
    public void setOnTouchListener(OnTouchListener l) { touchListener = l; }
    public void setOnLongClickListener(OnLongClickListener l) {}
    public boolean performClick() {
        if (clickListener != null) { clickListener.onClick(this); return true; }
        return false;
    }
    public boolean callOnClick() { return performClick(); }

    public void invalidate() {
        Runnable hook = findHook();
        if (hook != null) hook.run();
    }
    public void invalidate(int l, int t, int r, int b) { invalidate(); }
    public void invalidate(Rect dirty) { invalidate(); }
    public void postInvalidate() { invalidate(); }
    public void postInvalidateDelayed(long delay) { invalidate(); }
    private Runnable findHook() {
        View v = this;
        while (v != null) {
            if (v.invalidateHook != null) return v.invalidateHook;
            v = v.parent;
        }
        return null;
    }
    public boolean post(Runnable action) { handler.post(action); return true; }
    public boolean postDelayed(Runnable action, long delayMillis) { handler.postDelayed(action, delayMillis); return true; }
    public void removeCallbacks(Runnable action) { handler.removeCallbacks(action); }

    protected void onAttachedToWindow() { attached = true; }
    protected void onDetachedFromWindow() { attached = false; }
    protected void onVisibilityChanged(View changedView, int visibility) {}
    public boolean isAttachedToWindow() { return attached; }
    public void attachForMirror() { onAttachedToWindow(); }
    public void detachForMirror() { onDetachedFromWindow(); }

    public void bringToFront() { if (parent != null) parent.bringChildToFront(this); }
    public void getLocationOnScreen(int[] outLocation) {
        int x = left, y = top;
        ViewGroup p = parent;
        while (p != null) { x += p.left; y += p.top; p = p.parent; }
        outLocation[0] = x; outLocation[1] = y;
    }
    public void getLocationInWindow(int[] outLocation) { getLocationOnScreen(outLocation); }
    public boolean getGlobalVisibleRect(Rect r) {
        r.set(0, 0, getWidth(), getHeight());
        return visibility == VISIBLE;
    }
    public void scrollTo(int x, int y) { scrollX = x; scrollY = y; }
    public void scrollBy(int x, int y) { scrollTo(scrollX + x, scrollY + y); }
    public int getScrollX() { return scrollX; }
    public int getScrollY() { return scrollY; }
    public boolean canScrollVertically(int direction) { return false; }
    public boolean canScrollHorizontally(int direction) { return false; }

    public void setBackgroundColor(int color) {
        background = new android.graphics.drawable.ColorDrawable(color);
    }
    public void setBackground(Drawable background) { this.background = background; }
    public void setBackgroundDrawable(Drawable background) { this.background = background; }
    public void setBackgroundResource(int resId) {}
    public Drawable getBackground() { return background; }
    public void setWillNotDraw(boolean willNotDraw) { this.willNotDraw = willNotDraw; }
    public boolean willNotDraw() { return willNotDraw; }
    public CharSequence getContentDescription() { return contentDescription; }
    public void setContentDescription(CharSequence cd) { contentDescription = cd; }
    public float getTranslationX() { return translationX; }
    public void setTranslationX(float x) { translationX = x; }
    public float getTranslationY() { return translationY; }
    public void setTranslationY(float y) { translationY = y; }
    public float getScaleX() { return scaleX; }
    public void setScaleX(float s) { scaleX = s; }
    public float getScaleY() { return scaleY; }
    public void setScaleY(float s) { scaleY = s; }
    public float getRotation() { return rotation; }
    public void setRotation(float r) { rotation = r; }
    public float getX() { return left + translationX; }
    public void setX(float x) { translationX = x - left; }
    public float getY() { return top + translationY; }
    public void setY(float y) { translationY = y - top; }
    public void setElevation(float e) {}
    public float getElevation() { return 0f; }
    public void setLayerType(int layerType, android.graphics.Paint paint) {}
    public int getLayerType() { return LAYER_TYPE_NONE; }
    public void addOnAttachStateChangeListener(OnAttachStateChangeListener l) {}
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener l) {}
    public boolean isInEditMode() { return false; }
    public void setTextAlignment(int alignment) {}
    public int getTextAlignment() { return TEXT_ALIGNMENT_INHERIT; }
    public int getLayoutDirection() { return 0; }
    public Handler getHandler() { return handler; }
    public WindowInsets getRootWindowInsets() { return null; }
    public static class WindowInsets {}
}

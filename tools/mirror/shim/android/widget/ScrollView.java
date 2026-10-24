package android.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
// Desktop-mirror shim (internal QA tooling only).
public class ScrollView extends FrameLayout {
    private boolean fillViewport = false;
    private boolean smoothScrollingEnabled = true;
    public ScrollView(Context context) { super(context); }
    public ScrollView(Context context, AttributeSet attrs) { super(context, attrs); }
    public void setFillViewport(boolean fill) { fillViewport = fill; }
    public boolean isFillViewport() { return fillViewport; }
    public void setSmoothScrollingEnabled(boolean b) { smoothScrollingEnabled = b; }
    public boolean isSmoothScrollingEnabled() { return smoothScrollingEnabled; }
    public void smoothScrollTo(int x, int y) { scrollTo(x, y); }
    public void smoothScrollBy(int x, int y) { scrollBy(x, y); }
    public boolean fullScroll(int direction) {
        if (direction == View.FOCUS_DOWN) scrollTo(0, Integer.MAX_VALUE / 2);
        else scrollTo(0, 0);
        return false;
    }
    public boolean pageScroll(int direction) { return fullScroll(direction); }
    public boolean arrowScroll(int direction) { return false; }
    @Override public void addView(View child) {
        if (getChildCount() > 0) throw new IllegalStateException("ScrollView can host only one direct child");
        super.addView(child);
    }
}

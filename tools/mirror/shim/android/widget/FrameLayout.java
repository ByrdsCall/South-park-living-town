package android.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
// Desktop-mirror shim (internal QA tooling only).
public class FrameLayout extends ViewGroup {
    private boolean measureAllChildren = false;
    public FrameLayout(Context context) { super(context); }
    public FrameLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) { return new LayoutParams(getContext(), attrs); }
    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }
    @Override protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) { return new LayoutParams(lp); }
    public void setMeasureAllChildren(boolean measureAll) { measureAllChildren = measureAll; }
    public boolean getMeasureAllChildren() { return measureAllChildren; }
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity = -1;
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(int width, int height) { super(width, height); }
        public LayoutParams(int width, int height, int gravity) { super(width, height); this.gravity = gravity; }
        public LayoutParams(ViewGroup.LayoutParams source) { super(source); }
        public LayoutParams(MarginLayoutParams source) { super(source); }
        public LayoutParams(LayoutParams source) { super(source); gravity = source.gravity; }
    }
}

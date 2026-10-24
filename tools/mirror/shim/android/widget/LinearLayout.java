package android.widget;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
// Desktop-mirror shim (internal QA tooling only).
public class LinearLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SHOW_DIVIDER_NONE = 0, SHOW_DIVIDER_BEGINNING = 1, SHOW_DIVIDER_MIDDLE = 2, SHOW_DIVIDER_END = 4;
    private int orientation = HORIZONTAL;
    private int gravity = Gravity.TOP | Gravity.START;
    private float weightSum = -1f;
    private boolean measureWithLargestChild = false;
    private Drawable divider;
    private int dividerPadding = 0;
    public LinearLayout(Context context) { super(context); }
    public LinearLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public LinearLayout(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
    public int getOrientation() { return orientation; }
    public void setOrientation(int orientation) { this.orientation = orientation; }
    public void setGravity(int gravity) { this.gravity = gravity; }
    public int getGravity() { return gravity; }
    public void setWeightSum(float weightSum) { this.weightSum = weightSum; }
    public float getWeightSum() { return weightSum; }
    public void setMeasureWithLargestChildEnabled(boolean b) { measureWithLargestChild = b; }
    public boolean isMeasureWithLargestChildEnabled() { return measureWithLargestChild; }
    public void setDividerDrawable(Drawable divider) { this.divider = divider; }
    public Drawable getDividerDrawable() { return divider; }
    public void setDividerPadding(int padding) { dividerPadding = padding; }
    public int getDividerPadding() { return dividerPadding; }
    public void setShowDividers(int showDividers) {}
    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) { return new LayoutParams(getContext(), attrs); }
    @Override protected LayoutParams generateDefaultLayoutParams() {
        return orientation == VERTICAL
                ? new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                : new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    }
    @Override protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) { return new LayoutParams(p); }
    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) { return p instanceof LayoutParams; }
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public float weight;
        public int gravity = -1;
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(int width, int height) { super(width, height); weight = 0; }
        public LayoutParams(int width, int height, float weight) { super(width, height); this.weight = weight; }
        public LayoutParams(ViewGroup.LayoutParams p) { super(p); }
        public LayoutParams(MarginLayoutParams source) { super(source); }
        public LayoutParams(LayoutParams source) { super(source); weight = source.weight; gravity = source.gravity; }
    }
}

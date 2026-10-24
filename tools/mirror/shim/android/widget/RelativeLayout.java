package android.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
// Desktop-mirror shim (internal QA tooling only).
public class RelativeLayout extends ViewGroup {
    public RelativeLayout(Context context) { super(context); }
    public RelativeLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(int w, int h) { super(w, h); }
        public LayoutParams(ViewGroup.LayoutParams source) { super(source); }
        public LayoutParams(MarginLayoutParams source) { super(source); }
        public void addRule(int verb) {}
        public void addRule(int verb, int anchor) {}
    }
}

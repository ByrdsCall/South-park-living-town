package android.view;

import android.content.Context;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.List;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
public class ViewGroup extends View {
    final List<View> children = new ArrayList<>();
    public static final int FOCUS_BEFORE_DESCENDANTS = 0x20000;
    public static final int FOCUS_AFTER_DESCENDANTS = 0x40000;
    public static final int FOCUS_BLOCK_DESCENDANTS = 0x60000;

    public ViewGroup(Context context) { super(context); }
    public ViewGroup(Context context, AttributeSet attrs) { super(context, attrs); }
    public ViewGroup(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public static class LayoutParams {
        public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        public int width;
        public int height;
        public LayoutParams(int width, int height) { this.width = width; this.height = height; }
        public LayoutParams(Context c, AttributeSet attrs) { width = WRAP_CONTENT; height = WRAP_CONTENT; }
        public LayoutParams(LayoutParams source) { width = source.width; height = source.height; }
    }
    public static class MarginLayoutParams extends LayoutParams {
        public int leftMargin, topMargin, rightMargin, bottomMargin;
        public MarginLayoutParams(int width, int height) { super(width, height); }
        public MarginLayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public MarginLayoutParams(LayoutParams source) { super(source); }
        public MarginLayoutParams(MarginLayoutParams source) {
            super(source);
            leftMargin = source.leftMargin; topMargin = source.topMargin;
            rightMargin = source.rightMargin; bottomMargin = source.bottomMargin;
        }
        public void setMargins(int l, int t, int r, int b) {
            leftMargin = l; topMargin = t; rightMargin = r; bottomMargin = b;
        }
        public void setMarginStart(int start) { leftMargin = start; }
        public void setMarginEnd(int end) { rightMargin = end; }
        public int getMarginStart() { return leftMargin; }
        public int getMarginEnd() { return rightMargin; }
    }

    protected LayoutParams generateDefaultLayoutParams() { return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
    public LayoutParams generateLayoutParams(AttributeSet attrs) { return new LayoutParams(getContext(), attrs); }
    protected LayoutParams generateLayoutParams(LayoutParams p) { return new LayoutParams(p); }
    protected boolean checkLayoutParams(LayoutParams p) { return p != null; }

    public void addView(View child) {
        if (child.getLayoutParams() == null) child.setLayoutParams(generateDefaultLayoutParams());
        addViewInner(child, -1);
    }
    public void addView(View child, int index) {
        if (child.getLayoutParams() == null) child.setLayoutParams(generateDefaultLayoutParams());
        addViewInner(child, index);
    }
    public void addView(View child, LayoutParams params) {
        child.setLayoutParams(params);
        addViewInner(child, -1);
    }
    public void addView(View child, int width, int height) {
        child.setLayoutParams(generateDefaultLayoutParams());
        child.getLayoutParams().width = width;
        child.getLayoutParams().height = height;
        addViewInner(child, -1);
    }
    public void addView(View child, int index, LayoutParams params) {
        child.setLayoutParams(params);
        addViewInner(child, index);
    }
    private void addViewInner(View child, int index) {
        if (child.parent != null) child.parent.removeView(child);
        if (index < 0 || index > children.size()) children.add(child);
        else children.add(index, child);
        child.setParent(this);
    }
    public void removeView(View view) {
        if (children.remove(view)) view.setParent(null);
    }
    public void removeViewAt(int index) {
        View v = children.remove(index);
        if (v != null) v.setParent(null);
    }
    public void removeAllViews() {
        for (View v : children) v.setParent(null);
        children.clear();
    }
    public void removeViews(int start, int count) {
        for (int i = 0; i < count; i++) removeViewAt(start);
    }
    public int getChildCount() { return children.size(); }
    public View getChildAt(int index) { return children.get(index); }
    public int indexOfChild(View child) { return children.indexOf(child); }
    public void bringChildToFront(View child) {
        if (children.remove(child)) children.add(child);
    }
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        if (getId() == id) return (T) this;
        for (View v : children) {
            if (v.getId() == id) return (T) v;
            if (v instanceof ViewGroup) {
                T found = ((ViewGroup) v).findViewById(id);
                if (found != null) return found;
            }
        }
        return null;
    }
    public void setClipChildren(boolean clip) {}
    public void setClipToPadding(boolean clip) {}
    @Override
    public void draw(android.graphics.Canvas canvas) {
        super.draw(canvas);
        for (View v : new ArrayList<>(children)) {
            if (v.getVisibility() != VISIBLE) continue;
            int save = canvas.save();
            canvas.translate(v.getLeft(), v.getTop());
            v.draw(canvas);
            canvas.restoreToCount(save);
        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {}
}

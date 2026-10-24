package android.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
// Desktop-mirror shim (internal QA tooling only).
public class GridLayout extends ViewGroup {
    public static final int HORIZONTAL = 0, VERTICAL = 1;
    public static final int UNDEFINED = Integer.MIN_VALUE;
    public static class Alignment {}
    public static final Alignment BASELINE = new Alignment(), BOTTOM = new Alignment(),
            TOP = new Alignment(), CENTER = new Alignment(), START = new Alignment(),
            END = new Alignment(), LEFT = new Alignment(), RIGHT = new Alignment(),
            FILL = new Alignment();
    public static class Spec {
        public final int start, size;
        public final Alignment alignment;
        Spec(int start, int size, Alignment alignment) { this.start = start; this.size = size; this.alignment = alignment; }
    }
    public static Spec spec(int start) { return new Spec(start, 1, null); }
    public static Spec spec(int start, Alignment alignment) { return new Spec(start, 1, alignment); }
    public static Spec spec(int start, int size) { return new Spec(start, size, null); }
    public static Spec spec(int start, int size, Alignment alignment) { return new Spec(start, size, alignment); }
    public static Spec spec(int start, float weight) { return new Spec(start, 1, null); }
    public static Spec spec(int start, int size, float weight) { return new Spec(start, size, null); }
    private int rowCount = UNDEFINED, columnCount = UNDEFINED;
    private int orientation = HORIZONTAL;
    public GridLayout(Context context) { super(context); }
    public GridLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public int getRowCount() { return rowCount; }
    public void setRowCount(int count) { rowCount = count; }
    public int getColumnCount() { return columnCount; }
    public void setColumnCount(int count) { columnCount = count; }
    public void setOrientation(int orientation) { this.orientation = orientation; }
    public int getOrientation() { return orientation; }
    public void setUseDefaultMargins(boolean b) {}
    public void setAlignmentMode(int mode) {}
    public void setRowOrderPreserved(boolean b) {}
    public void setColumnOrderPreserved(boolean b) {}
    public static final int ALIGN_BOUNDS = 0, ALIGN_MARGINS = 1;
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public Spec rowSpec, columnSpec;
        public LayoutParams() { super(WRAP_CONTENT, WRAP_CONTENT); }
        public LayoutParams(Spec rowSpec, Spec columnSpec) {
            super(WRAP_CONTENT, WRAP_CONTENT); this.rowSpec = rowSpec; this.columnSpec = columnSpec;
        }
        public LayoutParams(Context c, AttributeSet attrs) { super(c, attrs); }
        public LayoutParams(ViewGroup.LayoutParams source) { super(source); }
        public LayoutParams(MarginLayoutParams source) { super(source); }
        public LayoutParams(LayoutParams source) { super(source); rowSpec = source.rowSpec; columnSpec = source.columnSpec; }
        public void setGravity(int gravity) {}
    }
}

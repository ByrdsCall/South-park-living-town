package android.widget;
import android.content.Context;
import android.util.AttributeSet;
// Desktop-mirror shim (internal QA tooling only).
public class Button extends TextView {
    public Button(Context context) { super(context); }
    public Button(Context context, AttributeSet attrs) { super(context, attrs); }
    public Button(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
}

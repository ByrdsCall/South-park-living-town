package android.widget;
import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
// Desktop-mirror shim (internal QA tooling only).
public class EditText extends TextView {
    private int inputType = 1;
    public EditText(Context context) { super(context); }
    public EditText(Context context, AttributeSet attrs) { super(context, attrs); }
    @Override public Editable getText() { return new SpannableStringBuilder(super.getText()); }
    @Override public void setText(CharSequence text, BufferType type) {
        super.setText(text == null ? "" : text.toString());
    }
    public int getInputType() { return inputType; }
    public void setInputType(int type) { inputType = type; }
    public void setSelection(int index) {}
    public void setSelection(int start, int stop) {}
    public int getSelectionStart() { return 0; }
    public int getSelectionEnd() { return length(); }
    public void setError(CharSequence error) {}
}

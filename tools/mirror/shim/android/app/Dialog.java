package android.app;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
// Desktop-mirror shim (internal QA tooling only). Dialog as state: the
// mirror does not pop Swing windows for in-game dialogs; show()/dismiss()
// are recorded and content is reachable via getContentViewForMirror().
public class Dialog implements DialogInterface {
    protected final Context context;
    private final Window window;
    private View contentView;
    private boolean showing = false;
    private boolean cancelable = true;
    private boolean canceledOnTouchOutside = true;
    private DialogInterface.OnDismissListener dismissListener;
    private DialogInterface.OnShowListener showListener;
    private DialogInterface.OnCancelListener cancelListener;
    private CharSequence title;
    public Dialog(Context context) { this.context = context; this.window = new Window(context); }
    public Dialog(Context context, int themeResId) { this.context = context; this.window = new Window(context); }
    protected Dialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        this.context = context; this.window = new Window(context);
        this.cancelable = cancelable; this.cancelListener = cancelListener;
    }
    public Context getContext() { return context; }
    public boolean requestWindowFeature(int featureId) { return window.requestFeature(featureId); }
    public Window getWindow() { return window; }
    public void setContentView(View view) { contentView = view; }
    public void setContentView(int layoutResID) {}
    public void setContentView(View view, android.view.ViewGroup.LayoutParams params) { contentView = view; }
    public View getContentViewForMirror() { return contentView; }
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        if (contentView == null) return null;
        if (contentView.getId() == id) return (T) contentView;
        if (contentView instanceof android.view.ViewGroup) return ((android.view.ViewGroup) contentView).findViewById(id);
        return null;
    }
    public void setTitle(CharSequence title) { this.title = title; }
    public void setTitle(int titleId) { this.title = context.getString(titleId); }
    public CharSequence getTitleForMirror() { return title; }
    public void show() {
        showing = true;
        onShow();
        if (showListener != null) showListener.onShow(this);
    }
    protected void onShow() {}
    public void hide() { showing = false; }
    public void dismiss() {
        if (!showing) return;
        showing = false;
        onDismiss();
        if (dismissListener != null) dismissListener.onDismiss(this);
    }
    protected void onDismiss() {}
    public void cancel() {
        if (cancelListener != null) cancelListener.onCancel(this);
        dismiss();
    }
    public boolean isShowing() { return showing; }
    public void setOnDismissListener(DialogInterface.OnDismissListener listener) { dismissListener = listener; }
    public void setOnShowListener(DialogInterface.OnShowListener listener) { showListener = listener; }
    public void setOnCancelListener(DialogInterface.OnCancelListener listener) { cancelListener = listener; }
    public void setCancelable(boolean flag) { cancelable = flag; }
    public boolean isCancelable() { return cancelable; }
    public void setCanceledOnTouchOutside(boolean cancel) { canceledOnTouchOutside = cancel; }
    public void setOnKeyListener(DialogInterface.OnKeyListener listener) {}
    protected void onCreate(Bundle savedInstanceState) {}
    protected void onStart() {}
    protected void onStop() {}
    public boolean onTouchEvent(MotionEvent event) { return false; }
    public boolean onKeyDown(int keyCode, KeyEvent event) { return false; }
    public boolean onKeyUp(int keyCode, KeyEvent event) { return false; }
    public LayoutInflater getLayoutInflater() { return null; }
    public View getCurrentFocus() { return null; }
}

package android.app;
import android.os.Bundle;
// Desktop-mirror shim (internal QA tooling only).
public class DialogFragment extends Fragment implements android.content.DialogInterface.OnDismissListener {
    public void onDismiss(android.content.DialogInterface dialog) {}
    public static final int STYLE_NORMAL = 0, STYLE_NO_TITLE = 1, STYLE_NO_FRAME = 2, STYLE_NO_INPUT = 3;
    private Dialog dialog;
    public Dialog onCreateDialog(Bundle savedInstanceState) { return null; }
    public Dialog getDialog() { return dialog; }
    public void setDialogForMirror(Dialog d) { dialog = d; }
    public void show(FragmentManager manager, String tag) {
        dialog = onCreateDialog(null);
        if (dialog != null) dialog.show();
    }
    public int show(FragmentTransaction transaction, String tag) {
        show((FragmentManager) null, tag);
        return 0;
    }
    public void dismiss() { if (dialog != null) dialog.dismiss(); }
    public void dismissAllowingStateLoss() { dismiss(); }
    public boolean isCancelable() { return true; }
    public void setCancelable(boolean cancelable) {}
    public void setStyle(int style, int theme) {}
    public boolean isShowing() { return dialog != null && dialog.isShowing(); }
}

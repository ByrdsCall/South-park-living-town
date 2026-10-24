package android.view;
import android.graphics.drawable.Drawable;
// Desktop-mirror shim (internal QA tooling only).
public interface MenuItem {
    int getItemId();
    CharSequence getTitle();
    MenuItem setTitle(CharSequence title);
    MenuItem setIcon(Drawable icon);
    MenuItem setIcon(int iconRes);
    Drawable getIcon();
    MenuItem setVisible(boolean visible);
    boolean isVisible();
    MenuItem setEnabled(boolean enabled);
    boolean isEnabled();
    View getActionView();
    MenuItem setActionView(View view);
    MenuItem setShowAsActionFlags(int actionEnum);
    interface OnMenuItemClickListener { boolean onMenuItemClick(MenuItem item); }
    MenuItem setOnMenuItemClickListener(OnMenuItemClickListener listener);
}

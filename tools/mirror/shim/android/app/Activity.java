package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

// Desktop-mirror shim (internal QA tooling only — never shipped in the APK).
// Activity as a lifecycle-compatible host object. The mirror does NOT run
// the game's Activities as screens (its screens are Android widget trees;
// see README parity account) — this class exists so the full game source
// compiles and shared logic can be constructed where useful.
public class Activity extends Context {
    public static final int RESULT_CANCELED = 0;
    public static final int RESULT_OK = -1;
    public static final int RESULT_FIRST_USER = 1;

    private final Window window = new Window(this);
    private Intent intent;
    private View contentView;
    private boolean finished = false;
    private boolean destroyed = false;
    private int requestedOrientation = -1;
    private int resultCode = RESULT_CANCELED;
    private Intent resultData;

    protected void onCreate(Bundle savedInstanceState) {}
    protected void onStart() {}
    protected void onRestart() {}
    protected void onResume() {}
    protected void onPause() {}
    protected void onStop() {}
    protected void onDestroy() {}
    protected void onSaveInstanceState(Bundle outState) {}
    protected void onRestoreInstanceState(Bundle savedInstanceState) {}
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {}
    protected void onNewIntent(Intent intent) {}
    public void onBackPressed() {}
    protected void onPostCreate(Bundle savedInstanceState) {}
    protected void onPostResume() {}
    protected void onTitleChanged(CharSequence title, int color) {}
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {}
    public void onConfigurationChanged(Configuration newConfig) {}
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}
    public boolean onCreateOptionsMenu(Menu menu) { return false; }
    public boolean onOptionsItemSelected(MenuItem item) { return false; }
    public boolean onTouchEvent(MotionEvent event) { return false; }
    public boolean dispatchTouchEvent(MotionEvent ev) { return false; }
    public void onWindowFocusChanged(boolean hasFocus) {}
    protected void onApplyThemeResource(Object theme, int resId, boolean first) {}

    public Window getWindow() { return window; }
    public WindowManager getWindowManager() { return null; }
    public LayoutInflater getLayoutInflater() { return LayoutInflater.from(this); }
    public void setContentView(View view) { contentView = view; }
    public void setContentView(int layoutResID) {}
    public void setContentView(View view, ViewGroup.LayoutParams params) { contentView = view; }
    public void addContentView(View view, ViewGroup.LayoutParams params) {}
    public View getContentViewForMirror() { return contentView; }
    @SuppressWarnings("unchecked")
    public <T extends View> T findViewById(int id) {
        if (contentView == null) return null;
        if (contentView.getId() == id) return (T) contentView;
        if (contentView instanceof ViewGroup) return ((ViewGroup) contentView).findViewById(id);
        return null;
    }
    public Intent getIntent() { return intent; }
    public void setIntent(Intent newIntent) { intent = newIntent; }
    public void finish() { finished = true; onDestroy(); }
    public boolean isFinishing() { return finished; }
    public boolean isDestroyed() { return destroyed; }
    public void setDestroyedForMirror() { destroyed = true; }
    public Activity getParent() { return null; }
    public void runOnUiThread(Runnable action) {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) action.run();
        else new android.os.Handler().post(action);
    }
    public void startActivityForResult(Intent intent, int requestCode) { startActivity(intent); }
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) { startActivity(intent); }
    public void setResult(int resultCode) { this.resultCode = resultCode; }
    public void setResult(int resultCode, Intent data) { this.resultCode = resultCode; resultData = data; }
    public int getResultCodeForMirror() { return resultCode; }
    public void overridePendingTransition(int enterAnim, int exitAnim) {}
    public void setRequestedOrientation(int requestedOrientation) { this.requestedOrientation = requestedOrientation; }
    public int getRequestedOrientation() { return requestedOrientation; }
    public void setTitle(CharSequence title) {}
    public void setTitle(int titleId) {}
    public void setTitleColor(int textColor) {}
    public CharSequence getTitle() { return ""; }
    public void setProgressBarIndeterminateVisibility(boolean visible) {}
    public void setVisible(boolean visible) {}
    public boolean isChild() { return false; }
    public void recreate() {}
    public FragmentManager getFragmentManager() { return new FragmentManager(); }
    public Object getSystemService(String name) {
        return super.getSystemService(name);
    }
    public String getLocalClassName() { return getClass().getSimpleName(); }
    public android.content.ComponentName getComponentName() {
        return new android.content.ComponentName(this, getClass());
    }
    public void requestPermissions(String[] permissions, int requestCode) {}
    public boolean shouldShowRequestPermissionRationale(String permission) { return false; }
    public void reportFullyDrawn() {}
    public boolean isTaskRoot() { return true; }
    public boolean moveTaskToBack(boolean nonRoot) { return false; }
}

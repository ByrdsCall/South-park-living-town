package android.app;
// Desktop-mirror shim (internal QA tooling only).
public class FragmentTransaction {
    public FragmentTransaction add(int containerViewId, Fragment fragment) { return this; }
    public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) { return this; }
    public FragmentTransaction replace(int containerViewId, Fragment fragment) { return this; }
    public FragmentTransaction remove(Fragment fragment) { return this; }
    public FragmentTransaction show(Fragment fragment) { return this; }
    public FragmentTransaction hide(Fragment fragment) { return this; }
    public FragmentTransaction addToBackStack(String name) { return this; }
    public int commit() { return 0; }
    public int commitAllowingStateLoss() { return 0; }
    public void commitNow() {}
}

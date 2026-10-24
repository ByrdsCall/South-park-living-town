package android.app;
// Desktop-mirror shim (internal QA tooling only).
public class FragmentManager {
    public FragmentTransaction beginTransaction() { return new FragmentTransaction(); }
    public Fragment findFragmentByTag(String tag) { return null; }
    public Fragment findFragmentById(int id) { return null; }
    public boolean executePendingTransactions() { return false; }
    public int getBackStackEntryCount() { return 0; }
    public void popBackStack() {}
}

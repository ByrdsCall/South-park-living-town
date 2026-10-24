package android.app;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Desktop-mirror shim (internal QA tooling only).
public class Fragment {
    public void onAttach(Context context) {}
    public void onCreate(Bundle savedInstanceState) {}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { return null; }
    public void onViewCreated(View view, Bundle savedInstanceState) {}
    public void onActivityCreated(Bundle savedInstanceState) {}
    public void onStart() {}
    public void onResume() {}
    public void onPause() {}
    public void onStop() {}
    public void onDestroyView() {}
    public void onDestroy() {}
    public void onDetach() {}
    public Activity getActivity() { return null; }
    public Context getContext() { return null; }
    public View getView() { return null; }
    public Bundle getArguments() { return null; }
    public void setArguments(Bundle args) {}
    public boolean isAdded() { return false; }
    public boolean isVisible() { return false; }
    public void startActivity(Intent intent) {}
    public String getString(int resId) { return ""; }
}

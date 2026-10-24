package android.media;
import android.content.Context;
// Desktop-mirror shim (internal QA tooling only).
public class SoundPool {
    public interface OnLoadCompleteListener { void onLoadComplete(SoundPool soundPool, int sampleId, int status); }
    private OnLoadCompleteListener listener;
    @Deprecated public SoundPool(int maxStreams, int streamType, int srcQuality) {}
    private SoundPool() {}
    public static class Builder {
        private int maxStreams = 1;
        private AudioAttributes attrs;
        public Builder setMaxStreams(int maxStreams) { this.maxStreams = maxStreams; return this; }
        public Builder setAudioAttributes(AudioAttributes attributes) { attrs = attributes; return this; }
        public SoundPool build() { return new SoundPool(); }
    }
    public int load(Context context, int resId, int priority) { return 1; }
    public int load(String path, int priority) { return 1; }
    public int play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate) { return 1; }
    public void stop(int streamID) {}
    public void pause(int streamID) {}
    public void resume(int streamID) {}
    public void autoPause() {}
    public void autoResume() {}
    public void release() {}
    public boolean unload(int soundID) { return true; }
    public void setVolume(int streamID, float leftVolume, float rightVolume) {}
    public void setOnLoadCompleteListener(OnLoadCompleteListener listener) { this.listener = listener; }
}

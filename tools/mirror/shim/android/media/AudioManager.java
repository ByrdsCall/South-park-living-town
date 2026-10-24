package android.media;
// Desktop-mirror shim (internal QA tooling only).
public class AudioManager {
    public static final int STREAM_VOICE_CALL = 0, STREAM_SYSTEM = 1, STREAM_RING = 2,
            STREAM_MUSIC = 3, STREAM_ALARM = 4, STREAM_NOTIFICATION = 5;
    public static final int MODE_NORMAL = 0, MODE_RINGTONE = 1, MODE_IN_CALL = 2, MODE_IN_COMMUNICATION = 3;
    public int getStreamVolume(int streamType) { return 10; }
    public int getStreamMaxVolume(int streamType) { return 15; }
    public void setStreamVolume(int streamType, int index, int flags) {}
    public static final int RINGER_MODE_SILENT = 0, RINGER_MODE_VIBRATE = 1, RINGER_MODE_NORMAL = 2;
    private int ringerMode = RINGER_MODE_NORMAL;
    public int getRingerMode() { return ringerMode; }
    public void setRingerMode(int mode) { ringerMode = mode; }
    public int getMode() { return MODE_NORMAL; }
    public void setMode(int mode) {}
    public static final int AUDIOFOCUS_REQUEST_GRANTED = 1;
    public int requestAudioFocus(Object listener, int streamType, int durationHint) { return AUDIOFOCUS_REQUEST_GRANTED; }
    public int abandonAudioFocus(Object listener) { return 1; }
}

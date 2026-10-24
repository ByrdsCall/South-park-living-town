package android.media;
// Desktop-mirror shim (internal QA tooling only). Silent stubs: the mirror
// deliberately makes NO sound (it is a visual QA harness); the game's
// sound code paths run and are exercised, their output device is null.
public class AudioAttributes {
    public static final int USAGE_UNKNOWN = 0, USAGE_MEDIA = 1, USAGE_GAME = 14, USAGE_ASSISTANCE_SONIFICATION = 13;
    public static final int CONTENT_TYPE_UNKNOWN = 0, CONTENT_TYPE_SPEECH = 1, CONTENT_TYPE_MUSIC = 2, CONTENT_TYPE_MOVIE = 3, CONTENT_TYPE_SONIFICATION = 4;
    public int usage, contentType;
    public static class Builder {
        private int usage = USAGE_MEDIA, contentType = CONTENT_TYPE_MUSIC;
        public Builder() {}
        public Builder(AudioAttributes aa) {}
        public Builder setUsage(int usage) { this.usage = usage; return this; }
        public Builder setContentType(int type) { contentType = type; return this; }
        public Builder setLegacyStreamType(int streamType) { return this; }
        public Builder setFlags(int flags) { return this; }
        public AudioAttributes build() {
            AudioAttributes a = new AudioAttributes();
            a.usage = usage; a.contentType = contentType; return a;
        }
    }
}

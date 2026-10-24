package android.media;
// Desktop-mirror shim (internal QA tooling only).
public class AudioFormat {
    public static final int ENCODING_INVALID = 0, ENCODING_PCM_16BIT = 2, ENCODING_PCM_8BIT = 3, ENCODING_PCM_FLOAT = 4;
    public static final int CHANNEL_OUT_MONO = 0x4, CHANNEL_OUT_STEREO = 0xC;
    public static final int CHANNEL_IN_MONO = 0x10, CHANNEL_CONFIGURATION_MONO = 1;
    public int encoding, sampleRate, channelMask;
    public static class Builder {
        private int encoding = ENCODING_PCM_16BIT, sampleRate = 44100, channelMask = CHANNEL_OUT_STEREO;
        public Builder() {}
        public Builder(AudioFormat f) {}
        public Builder setEncoding(int e) { encoding = e; return this; }
        public Builder setSampleRate(int r) { sampleRate = r; return this; }
        public Builder setChannelMask(int m) { channelMask = m; return this; }
        public AudioFormat build() {
            AudioFormat f = new AudioFormat();
            f.encoding = encoding; f.sampleRate = sampleRate; f.channelMask = channelMask;
            return f;
        }
    }
    public int getEncoding() { return encoding; }
    public int getSampleRate() { return sampleRate; }
    public int getChannelMask() { return channelMask; }
    public int getChannelCount() { return Integer.bitCount(channelMask); }
}

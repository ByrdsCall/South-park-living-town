package android.media;
// Desktop-mirror shim (internal QA tooling only). Sink: writes are counted
// and discarded; state machine answers like a healthy track.
public class AudioTrack {
    public static final int STATE_UNINITIALIZED = 0, STATE_INITIALIZED = 1, STATE_NO_STATIC_DATA = 2;
    public static final int MODE_STATIC = 0, MODE_STREAM = 1;
    public static final int PLAYSTATE_STOPPED = 1, PLAYSTATE_PAUSED = 2, PLAYSTATE_PLAYING = 3;
    public static final int SUCCESS = 0, ERROR = -1, ERROR_BAD_VALUE = -2, ERROR_INVALID_OPERATION = -3;
    public static final int WRITE_BLOCKING = 0, WRITE_NON_BLOCKING = 1;
    private int playState = PLAYSTATE_STOPPED;
    private final int bufferSize;
    public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode) {
        bufferSize = bufferSizeInBytes;
    }
    public AudioTrack(AudioAttributes attributes, AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) {
        bufferSize = bufferSizeInBytes;
    }
    public static int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        int channels = channelConfig == AudioFormat.CHANNEL_OUT_STEREO ? 2 : 1;
        int bytesPerSample = audioFormat == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2;
        return sampleRateInHz * channels * bytesPerSample / 10;
    }
    public int getState() { return STATE_INITIALIZED; }
    public int getPlayState() { return playState; }
    public void play() { playState = PLAYSTATE_PLAYING; }
    public void pause() { playState = PLAYSTATE_PAUSED; }
    public void stop() { playState = PLAYSTATE_STOPPED; }
    public void flush() {}
    public void release() { playState = PLAYSTATE_STOPPED; }
    public int write(byte[] audioData, int offsetInBytes, int sizeInBytes) { return sizeInBytes; }
    public int write(byte[] audioData, int offsetInBytes, int sizeInBytes, int writeMode) { return sizeInBytes; }
    public int write(short[] audioData, int offsetInShorts, int sizeInShorts) { return sizeInShorts; }
    public int write(short[] audioData, int offsetInShorts, int sizeInShorts, int writeMode) { return sizeInShorts; }
    public int write(float[] audioData, int offsetInFloats, int sizeInFloats, int writeMode) { return sizeInFloats; }
    public int setVolume(float gain) { return SUCCESS; }
    public int setStereoVolume(float leftGain, float rightGain) { return SUCCESS; }
    public int setLoopPoints(int startInFrames, int endInFrames, int loopCount) { return SUCCESS; }
    public int setPlaybackHeadPosition(int positionInFrames) { return SUCCESS; }
    public int setNotificationMarkerPosition(int markerInFrames) { return SUCCESS; }
    public int setPositionNotificationPeriod(int periodInFrames) { return SUCCESS; }
    public int getBufferSizeInFrames() { return bufferSize / 2; }
    public int getSampleRate() { return 22050; }
    public int getPlaybackHeadPosition() { return 0; }
    public static int getNativeOutputSampleRate(int streamType) { return 44100; }
    public static class Builder {
        private AudioAttributes attributes;
        private AudioFormat format;
        private int bufferSize = 8192;
        private int mode = MODE_STREAM;
        private int sessionId = 0;
        public Builder setAudioAttributes(AudioAttributes attributes) { this.attributes = attributes; return this; }
        public Builder setAudioFormat(AudioFormat format) { this.format = format; return this; }
        public Builder setBufferSizeInBytes(int bufferSizeInBytes) { bufferSize = bufferSizeInBytes; return this; }
        public Builder setTransferMode(int transferMode) { mode = transferMode; return this; }
        public Builder setSessionId(int sessionId) { this.sessionId = sessionId; return this; }
        public AudioTrack build() { return new AudioTrack(attributes, format, bufferSize, mode, sessionId); }
    }
}

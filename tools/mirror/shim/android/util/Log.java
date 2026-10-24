package android.util;
// Desktop-mirror shim (internal QA tooling only).
public final class Log {
    public static final int VERBOSE = 2, DEBUG = 3, INFO = 4, WARN = 5, ERROR = 6, ASSERT = 7;
    private Log() {}
    public static int v(String tag, String msg) { return 0; }
    public static int d(String tag, String msg) { return 0; }
    public static int i(String tag, String msg) { System.out.println("I/" + tag + ": " + msg); return 0; }
    public static int w(String tag, String msg) { System.out.println("W/" + tag + ": " + msg); return 0; }
    public static int e(String tag, String msg) { System.err.println("E/" + tag + ": " + msg); return 0; }
    public static int e(String tag, String msg, Throwable tr) { System.err.println("E/" + tag + ": " + msg + " " + tr); return 0; }
    public static int w(String tag, String msg, Throwable tr) { System.out.println("W/" + tag + ": " + msg); return 0; }
    public static int d(String tag, String msg, Throwable tr) { return 0; }
    public static String getStackTraceString(Throwable tr) {
        java.io.StringWriter sw = new java.io.StringWriter();
        tr.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
    public static boolean isLoggable(String tag, int level) { return false; }
}

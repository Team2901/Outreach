package android.app;

/**
 * Virtual Robot's shim for Android's hidden ActivityThread.
 *
 * FTC team code commonly reaches the Robot Controller's app context with
 * {@code Class.forName("android.app.ActivityThread").getMethod("currentApplication")}.
 * Providing this class lets that reflection succeed in the simulator, so the normal
 * code path runs instead of failing - see android.preference.PreferenceManager, which
 * serves the simulator's stand-in Robot Controller settings.
 */
public class ActivityThread {

    private static final Application APPLICATION = new Application();

    /** The simulator's stand-in for the Robot Controller application. */
    public static Application currentApplication() {
        return APPLICATION;
    }
}

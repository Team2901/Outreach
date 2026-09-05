package android.os;

import java.io.File;

/**
 * Virtual Robot's approximation of Android's Environment.
 *
 * OpModes that log to "external storage" on a Control Hub (e.g. WriteObservations
 * writing observations.csv) instead write into the directory the simulator was
 * launched from, so the files are easy to find on a PC.
 */
public class Environment {

    public static final String MEDIA_MOUNTED = "mounted";

    /** The simulator's working directory stands in for the robot's external storage. */
    public static File getExternalStorageDirectory() {
        return new File(System.getProperty("user.dir", "."));
    }

    public static File getExternalStoragePublicDirectory(String type) {
        File dir = new File(getExternalStorageDirectory(), type == null ? "" : type);
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir;
    }

    public static String getExternalStorageState() {
        return MEDIA_MOUNTED;
    }

    public static boolean isExternalStorageEmulated() {
        return true;
    }

    public static boolean isExternalStorageRemovable() {
        return false;
    }

    public static File getDataDirectory() {
        return getExternalStorageDirectory();
    }

    public static File getRootDirectory() {
        return getExternalStorageDirectory();
    }
}

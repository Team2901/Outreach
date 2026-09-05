package android.app;

import android.content.Context;

/**
 * Virtual Robot's shim for Android's Application.
 *
 * Exists so team code that reads the Robot Controller's app context compiles for the
 * simulator. There is no Android application here, so nothing ever constructs one -
 * code that looks one up should fall back gracefully.
 */
public class Application extends Context {
}

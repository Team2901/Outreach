package android.content;

import java.util.Map;
import java.util.Set;

/**
 * Virtual Robot's shim for Android's SharedPreferences.
 *
 * The simulator stores no Robot Controller preferences, so every getter returns the
 * caller's default value. Present so team code that reads RC settings (e.g. the active
 * hardware configuration name) compiles for the simulator.
 */
public interface SharedPreferences {

    String getString(String key, String defValue);

    Set<String> getStringSet(String key, Set<String> defValues);

    int getInt(String key, int defValue);

    long getLong(String key, long defValue);

    float getFloat(String key, float defValue);

    boolean getBoolean(String key, boolean defValue);

    boolean contains(String key);

    Map<String, ?> getAll();
}

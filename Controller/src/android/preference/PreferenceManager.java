package android.preference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Virtual Robot's shim for Android's PreferenceManager.
 *
 * Stands in for the Robot Controller's settings store. The one setting the simulator
 * actually serves is the active hardware configuration
 * ({@value #KEY_HARDWARE_CONFIG}), which it reports as the robot chosen in the
 * simulator's "Configuration" dropdown - e.g. {"name":"Mecanum Bot"}. That means team
 * code which looks up the configuration name gets a meaningful answer in the simulator
 * through its normal code path, with no simulator-specific branches.
 *
 * The simulator publishes the current selection via the
 * {@value #CONFIGURATION_NAME_PROPERTY} system property; see
 * virtual_robot.controller.VirtualRobotController#getVirtualBotInstance.
 *
 * Every other preference returns the caller's default value.
 */
public class PreferenceManager {

    /** Preference key the Robot Controller uses for the active hardware configuration. */
    public static final String KEY_HARDWARE_CONFIG = "pref_hardware_config_filename";

    /** System property through which the simulator publishes the selected robot config. */
    public static final String CONFIGURATION_NAME_PROPERTY = "virtualRobot.robotConfigurationName";

    /** Reported when no robot configuration has been selected yet. */
    public static final String NO_CONFIGURATION = "No Configuration";

    private static final SharedPreferences PREFERENCES = new SharedPreferences() {

        @Override
        public String getString(String key, String defValue) {
            if (KEY_HARDWARE_CONFIG.equals(key)) {
                String name = System.getProperty(CONFIGURATION_NAME_PROPERTY, NO_CONFIGURATION);
                // The Robot Controller stores this setting as a small JSON document.
                return "{\"name\":\"" + name + "\"}";
            }
            return defValue;
        }

        @Override public Set<String> getStringSet(String key, Set<String> defValues) { return defValues; }
        @Override public int getInt(String key, int defValue) { return defValue; }
        @Override public long getLong(String key, long defValue) { return defValue; }
        @Override public float getFloat(String key, float defValue) { return defValue; }
        @Override public boolean getBoolean(String key, boolean defValue) { return defValue; }
        @Override public boolean contains(String key) { return KEY_HARDWARE_CONFIG.equals(key); }
        @Override public Map<String, ?> getAll() { return Collections.emptyMap(); }
    };

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PREFERENCES;
    }
}

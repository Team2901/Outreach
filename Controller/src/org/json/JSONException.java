package org.json;

/**
 * Virtual Robot's shim for org.json.JSONException.
 *
 * On Android, org.json ships with the platform. The simulator provides just enough of
 * it for team code that parses Robot Controller settings to compile and fail cleanly.
 */
public class JSONException extends Exception {

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }
}

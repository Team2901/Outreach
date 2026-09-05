package org.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Virtual Robot's stand-in for org.json.JSONObject.
 *
 * On Android, org.json ships with the platform; the simulator has to supply it. This is
 * a deliberately small implementation: it parses a FLAT JSON object (no nested objects
 * or arrays), which is all the Robot Controller settings that team code reads look like,
 * e.g. {"name":"Mecanum Bot"}. Encountering nesting raises {@link JSONException} rather
 * than silently returning something wrong.
 */
public class JSONObject {

    private final Map<String, String> values = new LinkedHashMap<>();

    public JSONObject() {
    }

    public JSONObject(String source) throws JSONException {
        if (source == null) {
            throw new JSONException("Cannot parse a null JSON document.");
        }
        String text = source.trim();
        if (text.isEmpty()) {
            throw new JSONException("Cannot parse an empty JSON document.");
        }
        if (!text.startsWith("{") || !text.endsWith("}")) {
            throw new JSONException("Not a JSON object: " + source);
        }
        parse(text.substring(1, text.length() - 1));
    }

    private void parse(String body) throws JSONException {
        int i = 0;
        int n = body.length();
        while (i < n) {
            while (i < n && Character.isWhitespace(body.charAt(i))) i++;
            if (i >= n) break;

            if (body.charAt(i) != '"') {
                throw new JSONException("Expected a quoted key at index " + i + " in: " + body);
            }
            StringBuilder key = new StringBuilder();
            i = readQuoted(body, i, key);

            while (i < n && Character.isWhitespace(body.charAt(i))) i++;
            if (i >= n || body.charAt(i) != ':') {
                throw new JSONException("Expected ':' after key \"" + key + "\"");
            }
            i++;
            while (i < n && Character.isWhitespace(body.charAt(i))) i++;
            if (i >= n) {
                throw new JSONException("Missing value for key \"" + key + "\"");
            }

            char c = body.charAt(i);
            if (c == '{' || c == '[') {
                throw new JSONException(
                        "Nested JSON is not supported by the Virtual Robot's JSON stand-in (key \"" + key + "\").");
            }
            StringBuilder value = new StringBuilder();
            if (c == '"') {
                i = readQuoted(body, i, value);
            } else {
                while (i < n && body.charAt(i) != ',') {
                    value.append(body.charAt(i));
                    i++;
                }
            }
            values.put(key.toString(), value.toString().trim());

            while (i < n && Character.isWhitespace(body.charAt(i))) i++;
            if (i < n && body.charAt(i) == ',') i++;
        }
    }

    /** Reads a quoted string starting at {@code start}; appends its content to {@code out}. */
    private int readQuoted(String s, int start, StringBuilder out) throws JSONException {
        int i = start + 1; // skip opening quote
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                if (i + 1 >= s.length()) break;
                char esc = s.charAt(++i);
                switch (esc) {
                    case 'n': out.append('\n'); break;
                    case 't': out.append('\t'); break;
                    case 'r': out.append('\r'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    default:  out.append(esc);  break;
                }
                i++;
            } else if (c == '"') {
                return i + 1; // past the closing quote
            } else {
                out.append(c);
                i++;
            }
        }
        throw new JSONException("Unterminated string in JSON document.");
    }

    private String require(String key) throws JSONException {
        String v = values.get(key);
        if (v == null) {
            throw new JSONException("No value for key \"" + key + "\".");
        }
        return v;
    }

    public String getString(String key) throws JSONException { return require(key); }

    public int getInt(String key) throws JSONException {
        try { return Integer.parseInt(require(key)); }
        catch (NumberFormatException e) { throw new JSONException("Not an int: " + key, e); }
    }

    public long getLong(String key) throws JSONException {
        try { return Long.parseLong(require(key)); }
        catch (NumberFormatException e) { throw new JSONException("Not a long: " + key, e); }
    }

    public double getDouble(String key) throws JSONException {
        try { return Double.parseDouble(require(key)); }
        catch (NumberFormatException e) { throw new JSONException("Not a double: " + key, e); }
    }

    public boolean getBoolean(String key) throws JSONException {
        return Boolean.parseBoolean(require(key));
    }

    public Object get(String key) throws JSONException { return require(key); }

    public String optString(String key, String fallback) {
        String v = values.get(key);
        return v == null ? fallback : v;
    }

    public String optString(String key) { return optString(key, ""); }

    public int optInt(String key, int fallback) {
        try { return getInt(key); } catch (JSONException e) { return fallback; }
    }

    public boolean optBoolean(String key, boolean fallback) {
        String v = values.get(key);
        return v == null ? fallback : Boolean.parseBoolean(v);
    }

    public JSONObject put(String key, String value) {
        values.put(key, value);
        return this;
    }

    public boolean has(String key) { return values.containsKey(key); }

    public int length() { return values.size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (!first) sb.append(',');
            sb.append('"').append(e.getKey()).append("\":\"").append(e.getValue()).append('"');
            first = false;
        }
        return sb.append('}').toString();
    }
}

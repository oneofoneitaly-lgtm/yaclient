package dev.yaclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/** Persistent key/value settings stored in ~/.yaclient/config.properties. */
public final class Config {
    private static final Properties P = new Properties();

    private Config() {}

    private static File file() {
        return new File(System.getProperty("user.home"), ".yaclient" + File.separator + "config.properties");
    }

    public static synchronized void load() {
        try {
            File f = file();
            if (!f.exists()) return;
            Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            try { P.load(r); } finally { r.close(); }
        } catch (Exception ignored) { }
    }

    public static synchronized void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            try { P.store(w, "YA CLIENT"); } finally { w.close(); }
        } catch (Exception ignored) { }
    }

    /** True if at least one stored key starts with the prefix. */
    public static synchronized boolean hasPrefix(String prefix) {
        for (Object k : P.keySet()) if (String.valueOf(k).startsWith(prefix)) return true;
        return false;
    }

    public static synchronized String get(String key, String def) {
        return P.getProperty(key, def);
    }

    public static synchronized void set(String key, String value) {
        P.setProperty(key, value);
    }

    public static boolean getBool(String key, boolean def) {
        String v = get(key, null);
        return v == null ? def : Boolean.parseBoolean(v);
    }

    public static void setBool(String key, boolean v) { set(key, String.valueOf(v)); }

    public static float getFloat(String key, float def) {
        try { return Float.parseFloat(get(key, String.valueOf(def))); } catch (Exception e) { return def; }
    }

    public static void setFloat(String key, float v) { set(key, String.valueOf(v)); }

    public static int getInt(String key, int def) {
        try { return Integer.parseInt(get(key, String.valueOf(def))); } catch (Exception e) { return def; }
    }

    public static void setInt(String key, int v) { set(key, String.valueOf(v)); }
}

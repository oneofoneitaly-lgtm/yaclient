package dev.yaclient.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/** Small key/value config in ~/.yaclient/fabric.properties (separate from the 1.8.9 client config). */
public final class Config {
    private static final Properties P = new Properties();

    private Config() {}

    private static File file() { return new File(System.getProperty("user.home"), ".yaclient" + File.separator + "fabric.properties"); }

    public static synchronized void load() {
        try {
            File f = file();
            if (f.exists()) {
                try (Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8")) { P.load(r); }
            }
        } catch (Exception ignored) { }
        // shared with the launcher and the 1.8.9 client: store backend, account secret, cosmetic selection
        try {
            File sf = new File(System.getProperty("user.home"), ".yaclient" + File.separator + "config.properties");
            if (sf.exists()) {
                Properties s = new Properties();
                try (Reader r = new InputStreamReader(new FileInputStream(sf), "UTF-8")) { s.load(r); }
                for (String k : s.stringPropertyNames()) {
                    if (k.equals("api.base") || k.equals("store.url")) P.setProperty(k, s.getProperty(k));
                    else if ((k.equals("api.secret") || k.startsWith("cos.")) && !P.containsKey(k)) P.setProperty(k, s.getProperty(k));
                }
            }
        } catch (Exception ignored) { }
    }

    public static synchronized void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8")) { P.store(w, "YA CLIENT (Fabric)"); }
        } catch (Exception ignored) { }
    }

    public static synchronized boolean has(String key) { return P.containsKey(key); }

    public static synchronized boolean hasPrefix(String prefix) {
        for (Object k : P.keySet()) if (String.valueOf(k).startsWith(prefix)) return true;
        return false;
    }

    public static synchronized String get(String key, String def) { return P.getProperty(key, def); }

    public static synchronized void set(String key, String value) { P.setProperty(key, value); }

    public static boolean getBool(String key, boolean def) { String v = get(key, null); return v == null ? def : Boolean.parseBoolean(v); }

    public static void setBool(String key, boolean v) { set(key, String.valueOf(v)); }

    public static float getFloat(String key, float def) {
        try { String v = get(key, null); return v == null ? def : Float.parseFloat(v); } catch (NumberFormatException e) { return def; }
    }

    public static int getInt(String key, int def) {
        try { String v = get(key, null); return v == null ? def : Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }

    public static void setInt(String key, int v) { set(key, String.valueOf(v)); }

    public static void setFloat(String key, float v) { set(key, String.valueOf(v)); }
}

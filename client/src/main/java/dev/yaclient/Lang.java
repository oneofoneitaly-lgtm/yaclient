package dev.yaclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Localization. Language files live in resources/lang/&lt;code&gt;.json (flat "key": "value" objects).
 * English is the default and the fallback for missing keys. To add a language, copy en.json,
 * translate the values and add the code and name below.
 */
public final class Lang {
    public static final String[] CODES = {"en", "it", "es", "de"};
    public static final String[] NAMES = {"English", "Italiano", "Espa\u00f1ol", "Deutsch"};

    private static Map<String, String> fallback = new HashMap<String, String>();
    private static Map<String, String> current = new HashMap<String, String>();
    private static String code = "en";

    private Lang() {}

    public static String code() { return code; }

    public static void load(String c) {
        boolean known = false;
        for (String k : CODES) if (k.equals(c)) known = true;
        if (!known) c = "en";
        fallback = read("en");
        current = c.equals("en") ? fallback : read(c);
        code = c;
        Config.set("lang", c);
    }

    public static boolean has(String key) {
        return current.containsKey(key) || fallback.containsKey(key);
    }

    public static String t(String key) {
        String v = current.get(key);
        if (v == null) v = fallback.get(key);
        return v == null ? key : v;
    }

    public static String t(String key, Object... args) {
        try { return String.format(Locale.ROOT, t(key), args); } catch (Exception e) { return t(key); }
    }

    private static Map<String, String> read(String c) {
        String path = "/lang/" + c + ".json";
        InputStream in = Lang.class.getResourceAsStream(path);
        if (in == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) in = cl.getResourceAsStream(path.substring(1));
        }
        if (in == null) return new HashMap<String, String>();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return parse(new String(out.toByteArray(), "UTF-8"));
        } catch (Exception e) {
            return new HashMap<String, String>();
        } finally {
            try { in.close(); } catch (Exception ignored) { }
        }
    }

    /** Minimal parser for flat JSON objects with string values. */
    static Map<String, String> parse(String s) {
        Map<String, String> m = new HashMap<String, String>();
        int i = 0, n = s.length();
        String key = null;
        while (i < n) {
            char c = s.charAt(i);
            if (c != '"') { i++; continue; }
            StringBuilder sb = new StringBuilder();
            i++;
            while (i < n) {
                char d = s.charAt(i++);
                if (d == '"') break;
                if (d == '\\' && i < n) {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (i + 4 <= n) { sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                            break;
                        default: sb.append(e);
                    }
                } else {
                    sb.append(d);
                }
            }
            if (key == null) key = sb.toString();
            else { m.put(key, sb.toString()); key = null; }
        }
        return m;
    }
}

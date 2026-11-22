package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.Session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Talks to the YA CLIENT store backend (see backend/). The base URL is the config key "api.base"
 * (the launcher writes it from content.json). Without it the client runs in "dev mode":
 * every cosmetic is unlocked and nothing is sent anywhere.
 *
 * Identity is proven with Mojang's session server (the same handshake used to join servers), so a
 * player can only equip cosmetics that were bought for their own Minecraft account.
 */
public final class CosmeticsApi {
    private static volatile Set<String> owned = new HashSet<String>();
    private static volatile Map<String, String> catalog = new HashMap<String, String>();
    private static volatile String storeUrl = "";
    private static volatile boolean plus;
    private static volatile boolean staff;
    private static volatile String status = "";
    private static volatile String detail = "";
    private static volatile boolean refreshing;
    private static long lastRefresh;

    private static final Map<String, Map<String, String>> OTHERS = new ConcurrentHashMap<String, Map<String, String>>();
    private static final Map<String, Long> FETCHED = new ConcurrentHashMap<String, Long>();
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    private static String authToken;
    private static long authExpiry;

    private CosmeticsApi() {}

    // ---------- state ----------
    public static String base() {
        String b = Config.get("api.base", "").trim();
        while (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        return b;
    }

    public static boolean enabled() { return !base().isEmpty(); }

    /** True if the item can be equipped (always true in dev mode). */
    public static boolean owned(String item) { return !enabled() || (premium() && owned.contains(item)); }

    /** False for non-premium (SP) players: the launcher starts them with -Dyaclient.offline=true. They see the cosmetics of premium players but cannot own or wear any. */
    public static boolean premium() { return System.getProperty("yaclient.offline") == null; }

    /** YA Plus subscription (animated cape movement). Always true in dev mode. */
    public static boolean plus() { return !enabled() || plus; }

    public static String price(String item) { return catalog.get(item); }

    public static String status() { return status; }

    /** Technical reason of the last failure (shown next to the status to help support). */
    public static String detail() { return detail; }

    private static String enc(String s) {
        try { return URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; }
    }

    // ---------- network ----------
    static String http(String method, String url, String body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(7000);
        c.setRequestMethod(method);
        c.setRequestProperty("User-Agent", "YA-CLIENT");
        if (body != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStream o = c.getOutputStream();
            o.write(body.getBytes("UTF-8"));
            o.close();
        }
        int code = c.getResponseCode();
        if (code >= 400) {
            String msg = "";
            try {
                InputStream es = c.getErrorStream();
                if (es != null) { ByteArrayOutputStream eb = new ByteArrayOutputStream(); byte[] b2 = new byte[512]; int k = es.read(b2); if (k > 0) eb.write(b2, 0, k); msg = new String(eb.toByteArray(), "UTF-8").replaceAll("[\\r\\n{}\"]", " ").trim(); }
            } catch (Exception ignored) { }
            throw new Exception("HTTP " + code + (msg.isEmpty() ? "" : " " + msg));
        }
        InputStream in = c.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        return new String(out.toByteArray(), "UTF-8");
    }

    /** Minecraft's session service (authlib type, found by return type name to avoid obfuscation issues). */
    private static Object sessionService(Minecraft mc) throws Exception {
        for (Method m : Minecraft.class.getDeclaredMethods()) {
            if (m.getParameterTypes().length == 0 && m.getReturnType().getSimpleName().equals("MinecraftSessionService")) {
                m.setAccessible(true);
                return m.invoke(mc);
            }
        }
        throw new Exception("no session service");
    }

    private static UUID parseUuid(String s) {
        String h = s.replace("-", "");
        return UUID.fromString(h.substring(0, 8) + "-" + h.substring(8, 12) + "-" + h.substring(12, 16) + "-"
                + h.substring(16, 20) + "-" + h.substring(20));
    }

    /** Logs in to the store backend (needs a real Microsoft/Mojang account). Returns null if not possible. */
    static synchronized String token() {
        if (authToken != null && System.currentTimeMillis() < authExpiry) return authToken;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            Session s = mc.getSession();
            String[] parts = s.getSessionID().split(":");
            if (parts.length < 3 || s.getPlayerID() == null || s.getPlayerID().length() < 32) { detail = "no MS session (" + parts.length + ")"; return null; }
            String secret = Config.get("api.secret", "");
            if (!secret.matches("[0-9a-f]{32}")) {
                byte[] rb = new byte[16];
                new SecureRandom().nextBytes(rb);
                StringBuilder sb = new StringBuilder();
                for (byte x : rb) sb.append(String.format("%02x", x));
                secret = sb.toString();
                Config.set("api.secret", secret);
                Config.save();
            }
            Map<String, String> r = Lang.parse(http("POST", base() + "/v1/auth",
                    "username=" + enc(s.getUsername()) + "&uuid=" + enc(s.getPlayerID().replace("-", "")) + "&secret=" + enc(secret) + (premium() ? "" : "&offline=1")));
            authToken = r.get("token");
            authExpiry = System.currentTimeMillis() + 50L * 60L * 1000L;
            return authToken;
        } catch (Throwable t) {
            Throwable c = t.getCause() != null ? t.getCause() : t;
            detail = c.getClass().getSimpleName() + ": " + String.valueOf(c.getMessage());
            return null;
        }
    }

    static synchronized void resetToken() { authToken = null; authExpiry = 0; }

    // ---------- public actions ----------
    /** Reloads the catalog and the player's purchases in the background. */
    public static void refreshAsync(boolean force) {
        if (!enabled()) return;
        long now = System.currentTimeMillis();
        if (refreshing || (!force && now - lastRefresh < 10L * 60L * 1000L)) return;
        refreshing = true;
        lastRefresh = now;
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Map<String, String> cat = Lang.parse(http("GET", base() + "/v1/catalog", null));
                    String su = cat.remove("_store");
                    storeUrl = su == null ? "" : su;
                    catalog = cat;

                    String tok = token();
                    if (tok == null) {
                        status = detail != null && detail.contains("linked to another") ? "linked" : "login";
                    } else {
                        detail = "";
                        Map<String, String> me = Lang.parse(http("GET", base() + "/v1/me?token=" + enc(tok), null));
                        String list = me.get("owned");
                        owned = new HashSet<String>(Arrays.asList(list == null ? new String[0] : list.split(",")));
                        plus = "1".equals(me.get("plus"));
                        staff = "1".equals(me.get("staff"));
                        Theme.enforcePlus(plus);
                        for (String slot : Cosmetics.SLOTS) {
                            String v = me.get(slot);
                            if (v != null) Config.set("cos." + slot, v);
                        }
                        Config.save();
                        status = "ok";
                    }
                } catch (Exception e) {
                    status = "offline";
                    detail = String.valueOf(e.getMessage());
                } finally {
                    refreshing = false;
                }
            }
        }, "YA-Cosmetics");
        t.setDaemon(true);
        t.start();
    }

    private static final java.util.concurrent.ExecutorService PUSH = java.util.concurrent.Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
        @Override public Thread newThread(Runnable r) { Thread t = new Thread(r, "YA-CosmeticsPush"); t.setDaemon(true); return t; }
    });

    /** Tells the server what you wear, so other YA CLIENT players can see it (one ordered queue, one request per change). */
    public static void pushEquip(String slot, String item) {
        Map<String, String> m = new java.util.LinkedHashMap<String, String>();
        m.put(slot, item);
        pushEquipAll(m);
    }

    /** Several slots in a single request, so a bundle is applied (or removed) atomically on the server. */
    public static void pushEquipAll(Map<String, String> items) {
        if (!enabled() || !premium() || items.isEmpty()) return;
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : items.entrySet()) { if (sb.length() > 0) sb.append(','); sb.append(e.getKey()).append(':').append(e.getValue()); }
        PUSH.execute(new Runnable() {
            @Override public void run() {
                for (int attempt = 0; attempt < 2; attempt++) {
                    try {
                        String tok = token();
                        if (tok == null) return;
                        http("POST", base() + "/v1/equip", "token=" + enc(tok) + "&set=" + enc(sb.toString()));
                        return;
                    } catch (Exception e) {
                        resetToken();   // expired token: log in again and retry once
                    }
                }
            }
        });
    }

    /** While set, the local player is drawn wearing exactly these cosmetics (used by the locker cards). */
    public static volatile Map<String, String> previewOverride = null;

    private static String siteBase() {
        String url = storeUrl.isEmpty() ? Config.get("store.url", "") : storeUrl;
        if (url == null) return "";
        url = url.trim().replaceAll("/+$", "");
        return url.replaceAll("/store$", "");
    }

    private static void browse(String url) {
        if (url == null || url.isEmpty()) return;
        try { java.awt.Desktop.getDesktop().browse(new java.net.URI(url)); } catch (Throwable ignored) { }
    }

    /** Opens the web page of one cosmetic (id like "sakura_cape"); falls back to the store. */
    public static void openItem(String itemId) {
        String b = siteBase();
        if (b.isEmpty()) { openStore(); return; }
        browse(b + "/item?i=" + itemId);
    }

    /** Opens the web page of a whole bundle (key like "sakura"); falls back to the store. */
    public static void openBundle(String bundleKey) {
        String b = siteBase();
        if (b.isEmpty()) { openStore(); return; }
        browse(b + "/bundle?b=" + bundleKey);
    }

    public static void openStore() {
        String url = storeUrl.isEmpty() ? Config.get("store.url", "") : storeUrl;
        if (url.isEmpty()) return;
        try { java.awt.Desktop.getDesktop().browse(new java.net.URI(url)); } catch (Throwable ignored) { }
    }

    /** Cosmetics worn by a player: your own selection, or what the server says for others. */
    public static Map<String, String> equippedFor(AbstractClientPlayer p) {
        Minecraft mc = Minecraft.getMinecraft();
        if (p == mc.thePlayer) {
            Map<String, String> ov = previewOverride;
            if (ov != null) return ov;
            Map<String, String> m = new HashMap<String, String>();
            if (!premium()) { for (String slot : Cosmetics.SLOTS) m.put(slot, "none"); m.put("plus", "0"); return m; }
            for (String slot : Cosmetics.SLOTS) m.put(slot, Cosmetics.equipped(slot));
            m.put("plus", plus() ? "1" : "0");
            return m;
        }
        if (!enabled()) return null;
        return lookup(p.getUniqueID().toString().replace("-", ""));
    }

    /** True if that player runs YA CLIENT (always true for yourself; others come from the server). */
    public static boolean usesClient(UUID id) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && id.equals(mc.thePlayer.getUniqueID())) return true;
        if (!enabled()) return false;
        Map<String, String> m = lookup(id.toString().replace("-", ""));
        return m != null && "1".equals(m.get("client"));
    }

    /** Your own logo rank: 2 staff, 1 YA Plus, 0 normal. */
    public static int ownRank() { return !enabled() ? 0 : staff ? 2 : plus ? 1 : 0; }

    /** Logo rank of a player, visible to everyone: 2 staff (set from the admin panel), 1 YA Plus, 0 normal. */
    public static int rank(UUID id) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && id.equals(mc.thePlayer.getUniqueID())) return ownRank();
        if (!enabled()) return 0;
        Map<String, String> m = lookup(id.toString().replace("-", ""));
        if (m == null) return 0;
        return "1".equals(m.get("staff")) ? 2 : "1".equals(m.get("plus")) ? 1 : 0;
    }

    /** True if that player has YA Plus (always the real value for yourself; others come from the server). */
    public static boolean isPlus(UUID id) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && id.equals(mc.thePlayer.getUniqueID())) return plus();
        if (!enabled()) return false;
        Map<String, String> m = lookup(id.toString().replace("-", ""));
        return m != null && "1".equals(m.get("plus"));
    }

    private static Map<String, String> lookup(final String uuid) {
        Map<String, String> cached = OTHERS.get(uuid);
        Long at = FETCHED.get(uuid);
        long ttl = cached != null && "1".equals(cached.get("client")) ? 2L * 1000L : 10L * 1000L;   // very short, so equipping or removing a cosmetic shows up for others almost immediately
        boolean stale = at == null || System.currentTimeMillis() - at > ttl;
        if (stale && PENDING.add(uuid)) {
            Thread t = new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        OTHERS.put(uuid, Lang.parse(http("GET", base() + "/v1/players/" + uuid, null)));
                    } catch (Exception ignored) {
                        OTHERS.put(uuid, new HashMap<String, String>());
                    } finally {
                        FETCHED.put(uuid, System.currentTimeMillis());
                        PENDING.remove(uuid);
                    }
                }
            }, "YA-CosmeticsLookup");
            t.setDaemon(true);
            t.start();
        }
        return cached;
    }
}

package dev.yaclient;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/** Friends list, requests and online presence (backend /v1/friends). Polls every 15 seconds, which also marks you online. */
public final class FriendsApi {
    public static final class Friend {
        public final String uuid, name, where;
        public final boolean online;
        Friend(String uuid, String name, boolean online, String where) { this.uuid = uuid; this.name = name; this.online = online; this.where = where; }
    }

    private static volatile List<Friend> friends = new ArrayList<Friend>();
    private static volatile List<Friend> incoming = new ArrayList<Friend>();
    private static volatile List<Friend> outgoing = new ArrayList<Friend>();
    private static volatile String status = "";
    private static volatile boolean loading, loaded;
    private static long lastPoll;
    private static Set<String> prevOnline = new HashSet<String>();
    private static Set<String> prevIncoming = new HashSet<String>();
    private static final List<Runnable> NONE = new CopyOnWriteArrayList<Runnable>();

    private FriendsApi() {}

    public static List<Friend> friends() { return friends; }
    public static List<Friend> incoming() { return incoming; }
    public static List<Friend> outgoing() { return outgoing; }
    public static String status() { return status; }
    public static boolean loaded() { return loaded; }

    public static int onlineCount() {
        int n = 0;
        for (Friend f : friends) if (f.online) n++;
        return n;
    }

    private static String enc(String s) {
        try { return URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; }
    }

    private static List<Friend> parseList(String s) {
        List<Friend> out = new ArrayList<Friend>();
        if (s == null || s.isEmpty()) return out;
        for (String item : s.split(";")) {
            String[] p = item.split(",", -1);
            if (p.length < 4) continue;
            out.add(new Friend(p[0], p[1], "1".equals(p[2]), p[3]));
        }
        return out;
    }

    /** Called every client tick; polls in the background when due. */
    public static void tick() {
        if (!CosmeticsApi.enabled()) return;
        long now = System.currentTimeMillis();
        if (loading || now - lastPoll < 15000L) return;
        poll();
    }

    public static void pollNow() { if (!loading && CosmeticsApi.enabled()) poll(); }

    private static void poll() {
        loading = true;
        lastPoll = System.currentTimeMillis();
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    String tok = CosmeticsApi.token();
                    if (tok == null) { status = "login"; return; }
                    Map<String, String> r = Lang.parse(CosmeticsApi.http("GET", CosmeticsApi.base() + "/v1/friends?where=game&token=" + enc(tok), null));
                    List<Friend> f = parseList(r.get("friends")), in = parseList(r.get("incoming")), out = parseList(r.get("outgoing"));
                    Set<String> nowOnline = new HashSet<String>();
                    for (Friend x : f) if (x.online) nowOnline.add(x.uuid);
                    Set<String> nowIn = new HashSet<String>();
                    for (Friend x : in) nowIn.add(x.uuid);
                    if (loaded) {
                        for (Friend x : f) if (x.online && !prevOnline.contains(x.uuid)) FriendToast.push(x.name, FriendToast.ONLINE);
                        for (Friend x : in) if (!prevIncoming.contains(x.uuid)) FriendToast.push(x.name, FriendToast.REQUEST);
                    }
                    prevOnline = nowOnline;
                    prevIncoming = nowIn;
                    friends = f; incoming = in; outgoing = out;
                    loaded = true;
                    status = "ok";
                } catch (Exception e) {
                    if (String.valueOf(e.getMessage()).contains("401")) CosmeticsApi.resetToken();
                    status = "offline";
                } finally {
                    loading = false;
                }
            }
        }, "YA-Friends");
        t.setDaemon(true);
        t.start();
    }

    /** Result callback gets a language key suffix: ok, sent, accepted, not_found, self, already, pending, full, error. */
    public interface Done { void done(String code); }

    private static void post(final String path, final String form, final Done cb) {
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                String code = "error";
                try {
                    String tok = CosmeticsApi.token();
                    if (tok != null) {
                        Map<String, String> r = Lang.parse(CosmeticsApi.http("POST", CosmeticsApi.base() + "/v1/friends/" + path, "token=" + enc(tok) + "&" + form));
                        code = r.containsKey("status") ? r.get("status") : "ok";
                    }
                } catch (Exception e) {
                    String m = String.valueOf(e.getMessage());
                    if (m.contains("404")) code = "not_found";
                    else if (m.contains("409")) code = "already";
                    else if (m.contains("400")) code = "self";
                    else if (m.contains("401")) CosmeticsApi.resetToken();
                }
                pollNow();
                if (cb != null) cb.done(code);
            }
        }, "YA-FriendsAct");
        t.setDaemon(true);
        t.start();
    }

    public static void add(String name, Done cb) { post("add", "name=" + enc(name), cb); }
    public static void accept(String uuid, Done cb) { post("accept", "uuid=" + enc(uuid), cb); }
    public static void decline(String uuid, Done cb) { post("decline", "uuid=" + enc(uuid), cb); }
    public static void remove(String uuid, Done cb) { post("remove", "uuid=" + enc(uuid), cb); }
}

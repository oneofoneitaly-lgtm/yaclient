package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The last servers you played on, shown on the title screen with their ping; click one to connect. */
public final class RecentServers {
    private RecentServers() {}

    private static final List<String> list = new ArrayList<String>();
    private static boolean loaded;
    private static String cur = "";
    /** The server you are (or were last) connected to, for auto reconnect. */
    public static volatile ServerData lastData;
    private static final Map<String, Long> pings = new ConcurrentHashMap<String, Long>();
    private static long lastPing;
    public static final int RH = 18, W = 128, X = 16;

    private static void load() {
        loaded = true;
        String v = Config.get("recent.servers", "");
        if (v != null) for (String s : v.split(",")) if (!s.trim().isEmpty()) list.add(s.trim());
    }

    public static void tick(Minecraft mc) {
        try {
            if (!loaded) load();
            ServerData sd = mc.getCurrentServerData();
            if (mc.theWorld == null || sd == null || sd.serverIP == null) return;
            lastData = sd;
            if (sd.serverIP.equals(cur)) return;
            cur = sd.serverIP;
            list.remove(cur);
            list.add(0, cur);
            while (list.size() > 3) list.remove(list.size() - 1);
            Config.set("recent.servers", String.join(",", list));
            Config.save();
        } catch (Throwable ignored) { }
    }

    private static void refresh() {
        if (System.currentTimeMillis() - lastPing < 20000) return;
        lastPing = System.currentTimeMillis();
        for (final String ip : new ArrayList<String>(list)) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        ServerData sd = new ServerData("", ip, false);
                        OldServerPinger p = new OldServerPinger();
                        p.ping(sd);
                        for (int i = 0; i < 60 && sd.pingToServer <= 0; i++) { p.pingPendingNetworks(); Thread.sleep(100); }
                        pings.put(ip, sd.pingToServer > 0 ? sd.pingToServer : -1L);
                    } catch (Throwable e) { pings.put(ip, -1L); }
                }
            }, "YA-ServerPing");
            t.setDaemon(true);
            t.start();
        }
    }

    /** The last servers you joined, newest first. */
    public static List<String> recent() { if (!loaded) load(); return new ArrayList<String>(list); }

    /** Height of the whole block (label + rows + gap) so the caller can lay it out; 0 when there is nothing to show. */
    public static int blockH() { if (!loaded) load(); return list.isEmpty() ? 0 : 13 + list.size() * RH + 8; }

    public static void draw(int mx, int my, int y0) {
        if (!loaded) load();
        if (list.isEmpty()) return;
        refresh();
        Gfx.text("RECENT", X + 2, y0, 8, true, MainMenuScreen.INK2);
        int lh = Gfx.lineHeight(8, false);
        for (int i = 0; i < list.size(); i++) {
            String ip = list.get(i);
            int y = y0 + 13 + i * RH;
            boolean over = mx >= X && mx < X + W && my >= y && my < y + RH - 2;
            Gfx.rr(X, y + 1, W, RH - 2, 6, 0x1F101830);
            Gfx.rr(X, y, W, RH - 2, 6, over ? 0xE8242429 : 0xD0101014);
            Gfx.rrOutline(X, y, W, RH - 2, 6, 1, over ? 0x80FFFFFF : 0x40FFFFFF);
            Long p = pings.get(ip);
            String ps = p == null ? "..." : (p < 0 ? "--" : p + "ms");
            int pc = p == null || p < 0 ? Theme.DIM : (p <= 80 ? 0xFF34D27B : (p <= 160 ? 0xFFFFC21A : 0xFFEF4444));
            int ty = y + (RH - 2 - lh) / 2;
            Gfx.text(ps, X + W - 7 - Gfx.width(ps, 8, false), ty, 8, false, pc);
            Gfx.text(Gfx.trim(ip, 8, false, W - 20 - Gfx.width(ps, 8, false)), X + 7, ty, 8, false, Theme.TEXT);
        }
    }

    public static boolean click(int mx, int my, int y0, GuiScreen from) {
        if (list.isEmpty()) return false;
        for (int i = 0; i < list.size(); i++) {
            int y = y0 + 13 + i * RH;
            if (mx >= X && mx < X + W && my >= y && my < y + RH - 2) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.displayGuiScreen(new YAConnecting(from, mc, new ServerData("YA", list.get(i), false)));
                return true;
            }
        }
        return false;
    }
}

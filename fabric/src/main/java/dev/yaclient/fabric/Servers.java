package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.EventLoopGroupHolder;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared server helpers for the menus: pinging, icons, joining and the recent servers list. */
final class Servers {
    private Servers() {}

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "YA-Server-Ping");
        t.setDaemon(true);
        return t;
    });

    /** Starts a ping in the background; the result lands in sd (state, ping, motd, players, icon). */
    static void ping(ServerStatusPinger pinger, ServerData sd, Runnable changed) {
        Minecraft mc = Minecraft.getInstance();
        sd.setState(ServerData.State.PINGING);
        sd.ping = -1L;
        POOL.submit(() -> {
            try {
                pinger.pingServer(sd, () -> mc.execute(changed), () -> { }, EventLoopGroupHolder.remote(mc.options.useNativeTransport()));
            } catch (Throwable e) {
                sd.setState(ServerData.State.UNREACHABLE);
                sd.ping = -1L;
            }
        });
    }

    static boolean online(ServerData sd) { return sd.state() == ServerData.State.SUCCESSFUL || sd.state() == ServerData.State.INCOMPATIBLE; }

    static boolean offline(ServerData sd) { return sd.state() == ServerData.State.UNREACHABLE; }

    static int pingColor(long p) { return p <= 80 ? 0xFF34D27B : (p <= 160 ? 0xFFFFC21A : 0xFFEF4444); }

    static String name(ServerData sd) { return sd.name == null || sd.name.isEmpty() ? sd.ip : sd.name; }

    static String plain(net.minecraft.network.chat.Component c) { return c == null ? "" : c.getString(); }

    static String players(ServerData sd) { return sd.players == null ? "" : sd.players.online() + "/" + sd.players.max(); }

    /** Server favicon, or a letter tile when there is none. */
    static void icon(ServerData sd, int x, int y, int s) {
        byte[] b = sd.getIconBytes();
        if (b != null && b.length > 0) {
            String key = "srv:" + sd.ip + "|" + Arrays.hashCode(b);
            if (Gfx.picture(key, () -> {
                try { return ImageIO.read(new ByteArrayInputStream(b)); } catch (Exception e) { return null; }
            }, x, y, s, s)) return;
        }
        letter(name(sd), x, y, s);
    }

    static void letter(String n, int x, int y, int s) {
        Gfx.rr(x, y, s, s, 4, 0x20FFFFFF);
        String l = n == null || n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
        int fs = Math.max(8, s / 2);
        Gfx.textCentered(l, x + s / 2, y + (s - Gfx.lineHeight(fs, true)) / 2, fs, true, Theme.MUTED);
    }

    static void join(Screen parent, ServerData sd) {
        if (sd == null) return;
        remember(sd.ip);
        Minecraft mc = Minecraft.getInstance();
        ConnectScreen.startConnecting(parent, mc, ServerAddress.parseString(sd.ip), sd, false, null);
    }

    // ---------- recent servers ----------
    static List<String> recent() {
        List<String> out = new ArrayList<String>();
        for (String s : Config.get("menu.recent", "").split(",")) if (!s.trim().isEmpty()) out.add(s.trim());
        return out;
    }

    static void remember(String ip) {
        if (ip == null || ip.trim().isEmpty()) return;
        List<String> l = recent();
        l.remove(ip.trim());
        l.add(0, ip.trim());
        while (l.size() > 8) l.remove(l.size() - 1);
        Config.set("menu.recent", String.join(",", l));
        Config.save();
    }
}

package dev.yaclient.modules;

import dev.yaclient.Config;
import dev.yaclient.Gfx;
import dev.yaclient.Lang;
import dev.yaclient.Theme;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client-side waypoints. Press B to save your position, N to remove the nearest waypoint.
 * The HUD element lists the closest ones with distance and compass direction.
 */
public class WaypointsModule extends HudModule {
    private static final class Waypoint {
        final String name;
        final double x, y, z;
        Waypoint(String name, double x, double y, double z) { this.name = name; this.x = x; this.y = y; this.z = z; }
    }

    private final List<Waypoint> points = new ArrayList<Waypoint>();
    private String loadedFor = null;
    private boolean addWas, removeWas;

    public WaypointsModule() { super("waypoints", Category.UTILITY, false, 0.4f, 0.012f); }

    private String serverKey() {
        if (mc == null || mc.getCurrentServerData() == null) return "local";
        return mc.getCurrentServerData().serverIP.replace(':', '_');
    }

    private void ensureLoaded() {
        String key = serverKey();
        if (key.equals(loadedFor)) return;
        loadedFor = key;
        points.clear();
        String raw = Config.get("wp." + key, "");
        for (String part : raw.split(";")) {
            String[] f = part.split("\\|");
            if (f.length != 4) continue;
            try { points.add(new Waypoint(f[0], Double.parseDouble(f[1]), Double.parseDouble(f[2]), Double.parseDouble(f[3]))); }
            catch (NumberFormatException ignored) { }
        }
    }

    private void persist() {
        StringBuilder sb = new StringBuilder();
        for (Waypoint w : points) {
            if (sb.length() > 0) sb.append(';');
            sb.append(w.name.replace(";", "").replace("|", "")).append('|').append(w.x).append('|').append(w.y).append('|').append(w.z);
        }
        Config.set("wp." + loadedFor, sb.toString());
        Config.save();
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        ensureLoaded();
        boolean add = mc.currentScreen == null && Keyboard.isKeyDown(Keyboard.KEY_B);
        boolean rem = mc.currentScreen == null && Keyboard.isKeyDown(Keyboard.KEY_N);
        if (add && !addWas) {
            points.add(new Waypoint("WP " + (points.size() + 1), mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            persist();
        }
        if (rem && !removeWas && !points.isEmpty()) {
            Waypoint nearest = null;
            double best = Double.MAX_VALUE;
            for (Waypoint w : points) {
                double d = dist(w);
                if (d < best) { best = d; nearest = w; }
            }
            points.remove(nearest);
            persist();
        }
        addWas = add;
        removeWas = rem;
    }

    private double dist(Waypoint w) {
        double dx = w.x - mc.thePlayer.posX, dy = w.y - mc.thePlayer.posY, dz = w.z - mc.thePlayer.posZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private List<String> lines() {
        List<String> out = new ArrayList<String>();
        if (mc != null && mc.thePlayer != null) {
            ensureLoaded();
            List<Waypoint> sorted = new ArrayList<Waypoint>(points);
            final double px0 = mc.thePlayer.posX, pz0 = mc.thePlayer.posZ;
            java.util.Collections.sort(sorted, new java.util.Comparator<Waypoint>() {
                @Override public int compare(Waypoint a, Waypoint b) {
                    return Double.compare(Math.hypot(a.x - px0, a.z - pz0), Math.hypot(b.x - px0, b.z - pz0));
                }
            });
            for (int i = 0; i < Math.min(3, sorted.size()); i++) {
                Waypoint w = sorted.get(i);
                float yawTo = (float) Math.toDegrees(Math.atan2(-(w.x - px0), w.z - pz0));
                out.add(String.format(Locale.ROOT, "%s  %dm %s", w.name, (int) dist(w), DirectionModule.compass(yawTo)));
            }
        }
        if (out.isEmpty() && preview) out.add("Base  120m N");
        return out;
    }

    @Override
    public int width() {
        int w = 0;
        for (String s : lines()) w = Math.max(w, Gfx.width(s, 10, false) + 16);
        return w;
    }

    @Override
    public int height() { return Math.max(1, lines().size()) * 20 - 4; }

    @Override
    protected void draw() {
        int y = 0, w = width();
        for (String s : lines()) {
            Gfx.rr(0, y, w, 16, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, y, w, 16, 6, 1, 0x1AFFFFFF);
            Gfx.text(s, 8, y + (16 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.TEXT);
            y += 20;
        }
    }
}

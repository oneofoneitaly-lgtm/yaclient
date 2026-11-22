package dev.yaclient;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Vector icons drawn on a 24x24 grid (stroke style). Rendered to textures by Gfx.icon. */
final class Icons {
    private Icons() {}

    private static Path2D poly(boolean close, float... p) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(p[0], p[1]);
        for (int i = 2; i < p.length; i += 2) path.lineTo(p[i], p[i + 1]);
        if (close) path.closePath();
        return path;
    }

    // ---- per-module icons, written in a tiny path language (24x24 grid) ----
    // M x y | L x y | B x1 y1 x2 y2 x y | Q x1 y1 x y | Z | C cx cy r | D cx cy r (filled) | E cx cy rx ry | R x y w h r | A cx cy r start extent
    private static final java.util.Map<String, String> DSL = new java.util.HashMap<String, String>();
    static {
        DSL.put("freelook", "M 2 12 B 6 6 18 6 22 12 B 18 18 6 18 2 12 Z C 12 12 3 D 12 12 1 M 4 4 L 2.5 2.5");
        DSL.put("bw_resources", "M 12 3 L 20 9 L 12 21 L 4 9 Z M 4 9 L 20 9 M 9 9 L 12 3 L 15 9 M 9 9 L 12 21 L 15 9");
        DSL.put("bw_blocks", "R 3 12 8 8 1.5 R 13 12 8 8 1.5 R 8 4 8 8 1.5");
        DSL.put("bw_threat", "C 12 12 9 M 12 3 L 12 7 M 12 17 L 12 21 M 3 12 L 7 12 M 17 12 L 21 12 D 12 12 2");
        DSL.put("streamer_mode", "M 2 12 B 6 6 18 6 22 12 B 18 18 6 18 2 12 Z C 12 12 3 M 4 20 L 20 4");
        DSL.put("crit_counter", "M 12 2 L 14.5 9.5 L 22 12 L 14.5 14.5 L 12 22 L 9.5 14.5 L 2 12 L 9.5 9.5 Z");
        DSL.put("hit_ratio", "M 4 20 L 4 12 M 10 20 L 10 6 M 16 20 L 16 10 M 21 20 L 21 15");
        DSL.put("kill_streak", "M 12 3 B 13 7 19 9 19 15 B 19 19 16 21 12 21 B 8 21 5 19 5 15 B 5 12 7 10 8 8 B 10 10 11 9 12 3 Z M 9 15 L 15 15");
        DSL.put("armor_warning", "M 12 3 L 20 6 L 19 14 B 18.5 17 15 19.5 12 21 B 9 19.5 5.5 17 5 14 L 4 6 Z M 12 8 L 12 13 D 12 16 0.9");
        DSL.put("void_alert", "M 12 3 L 12 17 M 6 12 L 12 18 L 18 12 M 4 21 L 20 21");
        DSL.put("gapple_timer", "M 12 8 B 8 5 3 8 4.5 14 B 6 19 9 21 12 20 B 15 21 18 19 19.5 14 B 21 8 16 5 12 8 Z M 12 8 L 13 3 M 9 13 L 11 15 L 15 11");
        DSL.put("potion_alert", "M 10 3 L 14 3 M 10.5 3 L 10.5 9 L 5 19 B 4.5 20.5 5.5 21 7 21 L 17 21 B 18.5 21 19.5 20.5 19 19 L 13.5 9 L 13.5 3 M 12 12 L 12 16 D 12 18.3 0.8");
        DSL.put("friends", "C 9 8 3.5 M 2.5 20 B 2.5 14 6 13.5 9 13.5 B 12 13.5 15.5 14 15.5 20 M 16 4.5 B 19 4.8 19.5 8.5 17.2 10.4 M 18 13.8 B 20.8 14.2 21.8 16.5 21.8 19.5");
        DSL.put("combo", "M 12 2.5 B 13 6 18 8 18 14 B 18 18 15.5 21 12 21 B 8.5 21 6 18.5 6 15 B 6 12 7.5 10.5 9 9 B 9.5 11 10.5 11.5 11.5 11 B 11 8 11 5 12 2.5 Z");
        DSL.put("bow_charge", "M 8 3 B 18 7 18 17 8 21 M 8 3 L 8 21 M 3 12 L 17 12 M 14 9 L 17 12 L 14 15");
        DSL.put("block_info", "M 12 3 L 20 7.5 L 20 16.5 L 12 21 L 4 16.5 L 4 7.5 Z M 4 7.5 L 12 12 L 20 7.5 M 12 12 L 12 21");
        DSL.put("no_hurt_cam", "R 3 7 18 12 3 C 12 13 3 M 4 20 L 20 4");
        DSL.put("bg_fps", "R 3 4 18 13 2 M 8 21 L 16 21 M 12 17 L 12 21 M 10 8 L 10 13 M 14 8 L 14 13");
        DSL.put("low_health", "M 12 20 B 4 14 3 9 3 7 B 3 4.5 5 3 7.2 3 B 9.2 3 11 4.3 12 6 B 13 4.3 14.8 3 16.8 3 B 19 3 21 4.5 21 7 B 21 9 20 14 12 20 Z M 12 8.5 L 12 12.5 D 12 15.3 0.9");
        DSL.put("target_hud", "C 12 12 7.5 C 12 12 3 M 12 2 L 12 5 M 12 19 L 12 22 M 2 12 L 5 12 M 19 12 L 22 12");
        DSL.put("item_counter", "R 3 4 7 7 2 R 3 13 7 7 2 M 13 7.5 L 21 7.5 M 13 16.5 L 21 16.5");
        DSL.put("inventory_hud", "R 3 4 18 16 2 M 9 4 L 9 20 M 15 4 L 15 20 M 3 9.3 L 21 9.3 M 3 14.7 L 21 14.7");
        DSL.put("nearby_players", "C 12 12 9 C 12 12 4.5 D 12 12 1.5 M 12 12 L 18.5 5.5");
        DSL.put("fps", "A 12 15 8 -20 220 M 12 15 L 16.5 9 D 12 15 1.5");
        DSL.put("cps", "R 7 3 10 18 5 M 12 3 L 12 10 M 7 10 L 17 10");
        DSL.put("coords", "M 12 21 B 6 15 5 12 5 9 B 5 5.5 8 3 12 3 B 16 3 19 5.5 19 9 B 19 12 18 15 12 21 Z C 12 9 2.5");
        DSL.put("keystrokes", "R 9 3 6 6 1.5 R 3 11 6 6 1.5 R 9 11 6 6 1.5 R 15 11 6 6 1.5");
        DSL.put("armor", "M 12 3 L 20 6 L 19 14 B 18.5 17 15 19.5 12 21 B 9 19.5 5.5 17 5 14 L 4 6 Z");
        DSL.put("ping", "M 5 19 L 5 15 M 10 19 L 10 11 M 15 19 L 15 7 M 20 19 L 20 3");
        DSL.put("direction", "C 12 12 9 M 15.5 8.5 L 13.5 13.5 L 8.5 15.5 L 10.5 10.5 Z");
        DSL.put("clock", "C 12 12 9 M 12 7 L 12 12 L 15.5 14");
        DSL.put("speed", "M 5 6 L 11 12 L 5 18 M 12 6 L 18 12 L 12 18 M 19 6 L 19 18");
        DSL.put("memory", "R 7 7 10 10 2 M 10 3 L 10 7 M 14 3 L 14 7 M 10 17 L 10 21 M 14 17 L 14 21 M 3 10 L 7 10 M 3 14 L 7 14 M 17 10 L 21 10 M 17 14 L 21 14");
        DSL.put("health", "M 12 20 B 4 14 3 9 3 7 B 3 4.5 5 3 7.2 3 B 9.2 3 11 4.3 12 6 B 13 4.3 14.8 3 16.8 3 B 19 3 21 4.5 21 7 B 21 9 20 14 12 20 Z");
        DSL.put("potions", "M 10 3 L 14 3 M 10.5 3 L 10.5 9 L 5 19 B 4.5 20.5 5.5 21 7 21 L 17 21 B 18.5 21 19.5 20.5 19 19 L 13.5 9 L 13.5 3 M 7.5 15 L 16.5 15");
        DSL.put("reach", "M 3 12 L 21 12 M 17 8 L 21 12 L 17 16 M 3 8 L 3 16");
        DSL.put("spotify", "M 9 17 L 9 5 L 19 3 L 19 15 C 6.8 17.5 2.4 C 16.8 15.5 2.4");
        DSL.put("waypoints", "M 6 21 L 6 3 M 6 4 L 18 4 L 15 8.5 L 18 13 L 6 13");
        DSL.put("days", "R 4 5 16 16 2.5 M 4 10 L 20 10 M 9 3 L 9 7 M 15 3 L 15 7");
        DSL.put("biome", "M 5 19 B 5 9 10 4 20 4 B 20 14 15 19 5 19 Z M 5 19 L 13 11");
        DSL.put("light", "M 9 17 L 15 17 M 10 20.5 L 14 20.5 M 9 17 B 9 14 6 13 6 9.5 B 6 6 8.7 3.5 12 3.5 B 15.3 3.5 18 6 18 9.5 B 18 13 15 14 15 17");
        DSL.put("pack_info", "R 4 4 16 16 3 R 7 7 4 4 1 M 7 17 L 11 13 L 14 16 L 17 12");
        DSL.put("mechvibes", "R 2 7 20 11 2 M 5 10 L 7 10 M 9 10 L 11 10 M 13 10 L 15 10 M 17 10 L 19 10 M 6 14 L 18 14");
        DSL.put("server_ip", "C 12 12 9 E 12 12 4 9 M 3 12 L 21 12");
        DSL.put("session", "M 6 3 L 18 3 M 6 21 L 18 21 M 7 3 B 7 9 11 10 12 12 B 11 14 7 15 7 21 M 17 3 B 17 9 13 10 12 12 B 13 14 17 15 17 21");
        DSL.put("rotation", "A 12 12 8 40 270 M 19 4 L 19 9 L 14 9");
        DSL.put("item_info", "R 4 4 16 16 3 M 12 11 L 12 17 D 12 7.8 1.2");
        DSL.put("players", "C 9 8 3.2 M 3 20 B 3 15 6 13.5 9 13.5 B 12 13.5 15 15 15 20 C 17 9 2.5 M 17 13.8 B 19.5 14 21 15.5 21 19");
        DSL.put("food", "M 12 8 B 8 5 3 8 4.5 14 B 6 19 9 21 12 20 B 15 21 18 19 19.5 14 B 21 8 16 5 12 8 Z M 12 8 L 13 3");
        DSL.put("toggle_sprint", "C 14.5 4.5 2 M 9.5 21 L 11.5 15 L 8.5 12.5 L 11.5 8.5 L 16 10.5 L 19 9 M 11.5 15 L 15 17.5 L 14.5 21 M 8.5 12.5 L 5 14");
        DSL.put("toggle_sneak", "C 8.5 5 2 M 8 8.5 L 11.5 13 L 17 13 L 19 17 M 11.5 13 L 8 18 L 4.5 18 M 8 8.5 L 4 12");
        DSL.put("zoom", "C 10.5 10.5 6.5 M 15.5 15.5 L 21 21 M 8 10.5 L 13 10.5 M 10.5 8 L 10.5 13");
        DSL.put("fullbright", "C 12 12 4 M 12 2 L 12 5 M 12 19 L 12 22 M 2 12 L 5 12 M 19 12 L 22 12 M 4.9 4.9 L 7 7 M 17 17 L 19.1 19.1 M 4.9 19.1 L 7 17 M 17 7 L 19.1 4.9");
        DSL.put("motion_blur", "R 3 8 8 8 2 M 14 8 L 20 8 M 14 12 L 22 12 M 14 16 L 20 16");
        DSL.put("bandana", "M 3 9.5 B 8 6.5 16 6.5 21 9.5 L 21 14 B 16 11 8 11 3 14 Z M 19 13 L 22 19 M 17 12.5 L 18 19");
        DSL.put("sword", "M 14.5 17.5 L 3.5 6.5 L 3.5 3.5 L 6.5 3.5 L 17.5 14.5 M 13 19 L 19 13 M 16 16 L 20 20 M 19 21 L 21 19");
        DSL.put("wrench", "M 14.5 6 B 13 8 13.5 10.5 15.5 11.8 B 17.3 13 19.8 12.5 21 10.5 B 21.5 8 20.3 5.8 18 5 L 16 7 L 14.5 6 Z M 14 12.5 L 5.5 21 B 4.3 21.6 3 20.5 3.5 19 L 12 11");
        DSL.put("arrow_right", "M 9 5 L 16 12 L 9 19");
        DSL.put("crosshair", "C 12 12 7 M 12 2 L 12 8 M 12 16 L 12 22 M 2 12 L 8 12 M 16 12 L 22 12");
        DSL.put("discord_rpc", "M 5 5 L 19 5 B 20.5 5 21 5.5 21 7 L 21 15 B 21 16.5 20.5 17 19 17 L 11 17 L 7 21 L 7 17 L 5 17 B 3.5 17 3 16.5 3 15 L 3 7 B 3 5.5 3.5 5 5 5 Z D 9 11 1.2 D 15 11 1.2");
        DSL.put("time_changer", "M 20 15 B 13 21 3 15 6 6 B 7 4 9 3 10.5 3 B 8 8 12 17 20 15 Z");
        DSL.put("clear_weather", "M 7 18 B 3 18 3 12 7.5 12 B 8 7 16 7 17 12 B 21 12 21 18 17 18 Z M 4 4 L 20 20");
        DSL.put("hitboxes", "M 4 9 L 4 4 L 9 4 M 15 4 L 20 4 L 20 9 M 20 15 L 20 20 L 15 20 M 9 20 L 4 20 L 4 15 C 12 12 3");
        DSL.put("fov", "M 12 20 L 3 6 B 8 3 16 3 21 6 Z");
        DSL.put("cinematic", "R 3 6 18 14 2 M 3 10 L 21 10 M 8 6 L 6 10 M 13 6 L 11 10 M 18 6 L 16 10");
        DSL.put("entities", "R 5 4 14 16 2 R 7.5 8 3 3 0.5 R 13.5 8 3 3 0.5 R 10 13 4 5 0.5");
        DSL.put("fall", "M 12 3 L 12 17 M 6 11 L 12 17 L 18 11 M 5 21 L 19 21");
        DSL.put("xp", "C 12 12 8.5 M 12 6.5 L 13.4 10.6 L 17.5 12 L 13.4 13.4 L 12 17.5 L 10.6 13.4 L 6.5 12 L 10.6 10.6 Z");
        DSL.put("hit_color", "M 12 3 B 12 3 5 11 5 15 B 5 19 8 21 12 21 B 16 21 19 19 19 15 B 19 11 12 3 12 3 Z");
        DSL.put("old_animations", "M 11 6 L 4 12 L 11 18 Z M 20 6 L 13 12 L 20 18 Z");
        DSL.put("autogg", "M 8 4 L 16 4 L 15 11 B 14.5 13 9.5 13 9 11 Z M 8 6 L 4 6 B 4 10 6 11 8.5 11 M 16 6 L 20 6 B 20 10 18 11 15.5 11 M 12 13 L 12 17 M 8 20 L 16 20");
        DSL.put("levelhead", "R 3 6 18 12 3 M 7.5 10.5 L 7.5 13.5 L 10 13.5 M 13 10.5 L 13 13.5 L 16.5 13.5 M 7 18 L 12 21 L 17 18");
        DSL.put("minimap", "M 3 6 L 9 4 L 15 6 L 21 4 L 21 18 L 15 20 L 9 18 L 3 20 Z M 9 4 L 9 18 M 15 6 L 15 20");
        DSL.put("scoreboard", "R 4 3 16 18 2 M 8 8 L 16 8 M 8 12 L 16 12 M 8 16 L 13 16");
        DSL.put("fps_boost", "M 12 3 B 17 6 18 12 16 17 L 8 17 B 6 12 7 6 12 3 Z C 12 10 1.8 M 8 17 L 6 21 M 16 17 L 18 21");
        DSL.put("no_clouds", "M 7 18 B 3 18 3 12 7.5 12 B 8 7 16 7 17 12 B 21 12 21 18 17 18 Z M 3 3 L 21 21");
        DSL.put("block_outline", "R 4 8 12 12 1 M 4 8 L 8 4 L 20 4 L 20 16 L 16 20 M 16 8 L 20 4");
        DSL.put("smooth_shift", "M 5 4 L 19 4 M 12 8 L 12 19 M 8 15 L 12 19 L 16 15");
        DSL.put("chat", "R 3 4 18 13 3 M 8 17 L 8 21 L 12.5 17 M 7.5 9 L 16.5 9 M 7.5 12.5 L 13 12.5");
        DSL.put("auto_reconnect", "A 12 12 8 20 140 M 19 4 L 19 9 L 14 9 A 12 12 8 200 140 M 5 20 L 5 15 L 10 15");
        DSL.put("block_place", "R 3 3 12 12 2 M 18 14 L 18 21 M 14.5 17.5 L 21.5 17.5");
        DSL.put("callouts", "M 3 10 L 3 14 L 14 19 L 14 5 Z M 17 9 B 19.5 11 19.5 13 17 15 M 19.5 6.5 B 23 10.5 23 13.5 19.5 17.5");
        DSL.put("chat_tweaks", "M 4 5 L 20 5 L 20 16 L 11 16 L 6 20 L 6 16 L 4 16 Z M 8 9 L 16 9 M 8 12 L 12 12 D 15 12 1");
        DSL.put("click_counter", "M 8 8 L 19 12.5 L 14.5 14.5 L 12.5 19 Z M 5 5 L 7 7 M 12 3 L 12 6 M 3 12 L 6 12");
        DSL.put("clip", "M 6 3 L 18 3 L 18 21 L 12 17 L 6 21 Z");
        DSL.put("custom_text", "M 5 7 L 5 4 L 19 4 L 19 7 M 12 4 L 12 20 M 9 20 L 15 20");
        DSL.put("facing", "C 12 12 9 M 12 5 L 15 12 L 12 10.5 L 9 12 Z M 12 14.5 L 12 19");
        DSL.put("game_time", "C 12 13.5 7.5 M 12 13.5 L 15 10.5 M 10 3 L 14 3 M 12 3 L 12 6");
        DSL.put("item_lore", "R 5 3 14 18 2 M 8.5 8 L 15.5 8 M 8.5 12 L 15.5 12 M 8.5 16 L 12.5 16");
        DSL.put("item_tracker", "R 3 3 10 10 2 C 15 15 4.5 M 18.3 18.3 L 21.5 21.5");
        DSL.put("mouse_strokes", "R 8 2 8 12 4 M 12 2 L 12 7 M 3 19 B 6 15 8 22 12 18 B 16 14 18 21 21 17");
        DSL.put("perspective_lock", "R 5 11 14 10 2 M 8 11 L 8 8 B 8 2 16 2 16 8 L 16 11");
        DSL.put("player_head", "R 6 4 12 12 2 D 9.5 9.5 0.9 D 14.5 9.5 0.9 M 9.5 13 L 14.5 13 M 8 20 L 16 20");
        DSL.put("player_preview", "M 4 8 L 4 5 L 7 4 M 17 4 L 20 5 L 20 8 M 20 16 L 20 19 L 17 20 M 7 20 L 4 19 L 4 16 C 12 10 2.5 M 7.5 17 B 8 14.5 10 13.5 12 13.5 B 14 13.5 16 14.5 16.5 17");
        DSL.put("replay_buffer", "C 12 12 9 D 12 12 3.5 M 12 1 L 12 3");
        DSL.put("saturation", "C 15 9 5.5 M 11 13 L 4 20 M 3 17 L 7 21");
        DSL.put("shape", "C 8 8 4.5 R 11 11 9 9 1.5");
        DSL.put("team_viewer", "M 5 3 L 19 3 L 12 12 Z M 12 16 B 8.5 16 7 18 7 21 L 17 21 B 17 18 15.5 16 12 16");
        DSL.put("weather", "M 7 15 B 3 15 3 9.5 7.5 9.5 B 8 5 16 5 17 9.5 B 21 9.5 21 15 17 15 Z M 8 18 L 7 21 M 12 18 L 11 21 M 16 18 L 15 21");
    }

    static boolean has(String name) { return DSL.containsKey(name); }

    private static float f(String s) { return Float.parseFloat(s); }

    private static void dsl(Graphics2D g, String spec) {
        String[] t = spec.trim().split("\\s+");
        Path2D.Float path = null;
        int i = 0;
        while (i < t.length) {
            String c = t[i++];
            if (c.equals("M")) {
                if (path != null) g.draw(path);
                path = new Path2D.Float();
                float x = f(t[i++]), y = f(t[i++]);
                path.moveTo(x, y);
            } else if (c.equals("L")) {
                float x = f(t[i++]), y = f(t[i++]);
                path.lineTo(x, y);
            } else if (c.equals("B")) {
                float a = f(t[i++]), b = f(t[i++]), c2 = f(t[i++]), d = f(t[i++]), x = f(t[i++]), y = f(t[i++]);
                path.curveTo(a, b, c2, d, x, y);
            } else if (c.equals("Q")) {
                float a = f(t[i++]), b = f(t[i++]), x = f(t[i++]), y = f(t[i++]);
                path.quadTo(a, b, x, y);
            } else if (c.equals("Z")) {
                path.closePath();
            } else if (c.equals("C") || c.equals("D")) {
                float x = f(t[i++]), y = f(t[i++]), r = f(t[i++]);
                Ellipse2D.Float e = new Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r);
                if (c.equals("D")) g.fill(e); else g.draw(e);
            } else if (c.equals("E")) {
                float x = f(t[i++]), y = f(t[i++]), rx = f(t[i++]), ry = f(t[i++]);
                g.draw(new Ellipse2D.Float(x - rx, y - ry, 2 * rx, 2 * ry));
            } else if (c.equals("R")) {
                float x = f(t[i++]), y = f(t[i++]), w = f(t[i++]), h = f(t[i++]), r = f(t[i++]);
                g.draw(new RoundRectangle2D.Float(x, y, w, h, r * 2, r * 2));
            } else if (c.equals("A")) {
                float x = f(t[i++]), y = f(t[i++]), r = f(t[i++]), st = f(t[i++]), ex = f(t[i++]);
                g.draw(new Arc2D.Float(x - r, y - r, 2 * r, 2 * r, st, ex, Arc2D.OPEN));
            }
        }
        if (path != null) g.draw(path);
    }

    static void draw(Graphics2D g, String name) {
        String spec = DSL.get(name);
        if (spec != null) { dsl(g, spec); return; }
        switch (name) {
            case "grid":
                g.draw(new RoundRectangle2D.Float(3.5f, 3.5f, 7f, 7f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(13.5f, 3.5f, 7f, 7f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(3.5f, 13.5f, 7f, 7f, 2.5f, 2.5f));
                g.draw(new RoundRectangle2D.Float(13.5f, 13.5f, 7f, 7f, 2.5f, 2.5f));
                break;
            case "info":
                g.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                g.draw(new Line2D.Float(12f, 11f, 12f, 17f));
                g.fill(new Ellipse2D.Float(11f, 6.6f, 2f, 2f));
                break;
            case "sword":
                g.draw(new Line2D.Float(5f, 5f, 19f, 19f));
                g.draw(new Line2D.Float(19f, 5f, 5f, 19f));
                g.draw(new Line2D.Float(4f, 9f, 9f, 4f));
                g.draw(new Line2D.Float(20f, 9f, 15f, 4f));
                break;
            case "wrench":
                g.draw(new Arc2D.Float(3f, 3f, 11f, 11f, 160f, 310f, Arc2D.OPEN));
                g.draw(new Line2D.Float(11.5f, 11.5f, 20f, 20f));
                g.draw(new Line2D.Float(5f, 5.5f, 7.5f, 8f));
                break;
            case "shirt":
                g.draw(poly(true, 8f, 3f, 3.5f, 5.5f, 1.8f, 10f, 5.5f, 11.5f, 7f, 10f, 7f, 21f, 17f, 21f, 17f, 10f, 18.5f, 11.5f, 22.2f, 10f, 20.5f, 5.5f, 16f, 3f));
                g.draw(poly(false, 8f, 3f, 12f, 6.5f, 16f, 3f));
                break;
            case "star":
                g.draw(poly(true, 12f, 3f, 14.6f, 9f, 21f, 9.4f, 16f, 13.6f, 17.7f, 20f, 12f, 16.5f, 6.3f, 20f, 8f, 13.6f, 3f, 9.4f, 9.4f, 9f));
                break;
            case "move":
                g.draw(new Line2D.Float(12f, 3f, 12f, 21f));
                g.draw(new Line2D.Float(3f, 12f, 21f, 12f));
                g.draw(poly(false, 9f, 6f, 12f, 3f, 15f, 6f));
                g.draw(poly(false, 9f, 18f, 12f, 21f, 15f, 18f));
                g.draw(poly(false, 6f, 9f, 3f, 12f, 6f, 15f));
                g.draw(poly(false, 18f, 9f, 21f, 12f, 18f, 15f));
                break;
            case "gear": {
                Path2D.Float cog = new Path2D.Float();
                int teeth = 8;
                boolean first = true;
                for (int k = 0; k < teeth; k++) {
                    double c = Math.PI * 2 * k / teeth;
                    double step = Math.PI * 2 / teeth;
                    double[][] pts = {{c - step * 0.32, 7.6}, {c - step * 0.2, 10.6}, {c + step * 0.2, 10.6}, {c + step * 0.32, 7.6}};
                    for (double[] q : pts) {
                        float x = (float) (12 + Math.cos(q[0]) * q[1]), y = (float) (12 + Math.sin(q[0]) * q[1]);
                        if (first) { cog.moveTo(x, y); first = false; } else cog.lineTo(x, y);
                    }
                }
                cog.closePath();
                g.draw(cog);
                g.draw(new Ellipse2D.Float(8.8f, 8.8f, 6.4f, 6.4f));
                break;
            }
            case "sliders":
                g.draw(new Line2D.Float(4f, 7f, 20f, 7f));
                g.draw(new Line2D.Float(4f, 12f, 20f, 12f));
                g.draw(new Line2D.Float(4f, 17f, 20f, 17f));
                g.fill(new Ellipse2D.Float(7f, 4.2f, 5.6f, 5.6f));
                g.fill(new Ellipse2D.Float(13.5f, 9.2f, 5.6f, 5.6f));
                g.fill(new Ellipse2D.Float(6f, 14.2f, 5.6f, 5.6f));
                break;
            case "search":
                g.draw(new Ellipse2D.Float(4f, 4f, 12f, 12f));
                g.draw(new Line2D.Float(14f, 14f, 20f, 20f));
                break;
            case "close":
                g.draw(new Line2D.Float(6f, 6f, 18f, 18f));
                g.draw(new Line2D.Float(18f, 6f, 6f, 18f));
                break;
            case "check":
                g.draw(poly(false, 5f, 12.5f, 10f, 17.5f, 19f, 7f));
                break;
            case "globe":
                g.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                g.draw(new Ellipse2D.Float(8f, 3f, 8f, 18f));
                g.draw(new Line2D.Float(3f, 12f, 21f, 12f));
                break;
            case "palette":
                g.draw(new Ellipse2D.Float(3f, 3f, 18f, 18f));
                g.fill(new Ellipse2D.Float(7f, 9f, 2.4f, 2.4f));
                g.fill(new Ellipse2D.Float(11f, 6.5f, 2.4f, 2.4f));
                g.fill(new Ellipse2D.Float(15f, 9f, 2.4f, 2.4f));
                break;
            case "lock":
                g.draw(new RoundRectangle2D.Float(5f, 10.5f, 14f, 10f, 3f, 3f));
                g.draw(new Arc2D.Float(7.5f, 3.5f, 9f, 11f, 0f, 180f, Arc2D.OPEN));
                g.fill(new Ellipse2D.Float(11f, 14f, 2f, 2f));
                break;
            case "cart":
                g.draw(poly(false, 3f, 4f, 6f, 4f, 8.5f, 15f, 18f, 15f, 20f, 7f, 7f, 7f));
                g.fill(new Ellipse2D.Float(8.5f, 18f, 2.6f, 2.6f));
                g.fill(new Ellipse2D.Float(15f, 18f, 2.6f, 2.6f));
                break;
            case "reset":
                g.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
                g.draw(poly(false, 4f, 4f, 4f, 9f, 9f, 9f));
                break;
            case "cape":
                g.draw(poly(true, 8.5f, 3.5f, 15.5f, 3.5f, 19.5f, 20.5f, 12f, 18f, 4.5f, 20.5f));
                g.draw(new Line2D.Float(8.5f, 3.5f, 12f, 7f));
                g.draw(new Line2D.Float(15.5f, 3.5f, 12f, 7f));
                break;
            case "wings": {
                Path2D.Float w = new Path2D.Float();
                w.moveTo(11f, 12f);
                w.curveTo(9.5f, 7f, 6f, 5f, 2f, 5.5f);
                w.curveTo(2.6f, 11f, 5.5f, 16f, 11f, 19.5f);
                g.draw(w);
                Path2D.Float w2 = new Path2D.Float();
                w2.moveTo(13f, 12f);
                w2.curveTo(14.5f, 7f, 18f, 5f, 22f, 5.5f);
                w2.curveTo(21.4f, 11f, 18.5f, 16f, 13f, 19.5f);
                g.draw(w2);
                g.draw(new Line2D.Float(10f, 13f, 5f, 10f));
                g.draw(new Line2D.Float(10.5f, 15.5f, 5.5f, 14f));
                g.draw(new Line2D.Float(14f, 13f, 19f, 10f));
                g.draw(new Line2D.Float(13.5f, 15.5f, 18.5f, 14f));
                break;
            }
            case "bandana": {
                Path2D.Float b = new Path2D.Float();
                b.moveTo(3.5f, 9.5f);
                b.quadTo(12f, 5f, 20.5f, 9.5f);
                b.lineTo(20.5f, 14.5f);
                b.quadTo(12f, 10f, 3.5f, 14.5f);
                b.closePath();
                g.draw(b);
                g.draw(poly(false, 20.5f, 12f, 22f, 19f));
                g.draw(poly(false, 20f, 13f, 17.5f, 20f));
                break;
            }
            case "pet":
                g.draw(new Ellipse2D.Float(7f, 12f, 10f, 8f));
                g.draw(new Ellipse2D.Float(2.5f, 7f, 4f, 5f));
                g.draw(new Ellipse2D.Float(7.5f, 2.5f, 4f, 5f));
                g.draw(new Ellipse2D.Float(12.5f, 2.5f, 4f, 5f));
                g.draw(new Ellipse2D.Float(17.5f, 7f, 4f, 5f));
                break;
            case "gift":
                g.draw(new RoundRectangle2D.Float(4f, 10f, 16f, 10.5f, 2f, 2f));
                g.draw(new RoundRectangle2D.Float(3f, 7f, 18f, 3.5f, 1.5f, 1.5f));
                g.draw(new Line2D.Float(12f, 7f, 12f, 20.5f));
                g.draw(new Ellipse2D.Float(6.5f, 2.5f, 5.5f, 4.5f));
                g.draw(new Ellipse2D.Float(12f, 2.5f, 5.5f, 4.5f));
                break;
            default:
                g.draw(new Ellipse2D.Float(4f, 4f, 16f, 16f));
        }
    }
}

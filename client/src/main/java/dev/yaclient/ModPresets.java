package dev.yaclient;

import dev.yaclient.modules.Module;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** One-click mod sets: PvP, Building and Streaming. Mods in the set are turned on, all others off. */
public final class ModPresets {
    private ModPresets() {}

    public static final String[] NAMES = {"PvP", "Build", "Stream"};
    private static final String[][] SETS = {
        {"fps", "cps", "keystrokes", "armor", "potions", "combo", "target_hud", "ping", "reach", "hit_color", "old_animations", "fullbright", "no_hurt_cam", "toggle_sprint", "fps_boost", "no_clouds"},
        {"fps", "coords", "direction", "block_info", "light", "biome", "clock", "fullbright", "zoom", "inventory_hud", "item_counter", "days"},
        {"fps", "cps", "keystrokes", "ping", "clock", "streamer_mode", "discord_rpc", "chat_tweaks", "fps_boost", "no_clouds"}
    };
    public static String message = "";
    public static long messageUntil;

    public static void apply(int i) {
        if (i < 0 || i >= SETS.length) return;
        Set<String> on = new HashSet<String>(Arrays.asList(SETS[i]));
        for (Module m : YAClient.MODULES) {
            if (!m.available) continue;
            boolean want = on.contains(m.id);
            if (m.enabled != want) m.toggle();
        }
        Config.save();
        message = NAMES[i] + " preset";
        messageUntil = System.currentTimeMillis() + 2500;
    }
}

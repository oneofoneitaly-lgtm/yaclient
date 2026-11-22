package dev.yaclient.modules;

import dev.yaclient.Setting;

import java.util.regex.Pattern;

/**
 * Streamer mode: hides what you should not show on stream. Other modules ask {@link #hideCoords()},
 * {@link #hideIp()} and {@link #mask(String)}.
 */
public class StreamerModeModule extends Module {
    private final Setting.Bool coords = add(new Setting.Bool("hide_coords", true));
    private final Setting.Bool ip = add(new Setting.Bool("hide_ip", true));
    private final Setting.Bool board = add(new Setting.Bool("hide_board_ip", true));

    private static StreamerModeModule inst;
    private static final Pattern DOMAIN = Pattern.compile("(?i)\\b[a-z0-9-]+(\\.[a-z0-9-]+)*\\.(net|com|org|gg|io|eu|me|xyz|club|pro|tv|fun|lol|co|us|de|it|fr|es|uk)\\b");

    public StreamerModeModule() { super("streamer_mode", Category.UTILITY, false); inst = this; }

    public static boolean hideCoords() { return inst != null && inst.enabled && inst.coords.value; }

    public static boolean hideIp() { return inst != null && inst.enabled && inst.ip.value; }

    /** Replaces server addresses in a scoreboard line. */
    public static String maskBoard(String s) {
        if (inst == null || !inst.enabled || !inst.board.value || s == null) return s;
        return DOMAIN.matcher(s).replaceAll("hidden");
    }
}

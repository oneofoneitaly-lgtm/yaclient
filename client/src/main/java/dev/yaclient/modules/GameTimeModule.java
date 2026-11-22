package dev.yaclient.modules;

import java.util.Locale;

/** The Minecraft clock (06:00 = sunrise), not your computer time. */
public class GameTimeModule extends PillModule {
    public GameTimeModule() { super("game_time", Category.INFO, false, 0.99f, 0.22f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        long t = mc.theWorld.getWorldTime() % 24000L;
        if (t < 0) t += 24000L;
        int h = (int) ((t / 1000L + 6L) % 24L);
        int m = (int) ((t % 1000L) * 60L / 1000L);
        return String.format(Locale.ROOT, "In Game Time: %02d:%02d", h, m);
    }

    @Override
    protected String sample() { return "In Game Time: 14:30"; }
}

package dev.yaclient.modules;

import java.util.Locale;

public class HealthModule extends PillModule {
    public HealthModule() { super("health", Category.INFO, false, 0.99f, 0.168f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        return String.format(Locale.ROOT, "%.1f HP", mc.thePlayer.getHealth());
    }

    @Override
    protected String sample() { return "20.0 HP"; }
}

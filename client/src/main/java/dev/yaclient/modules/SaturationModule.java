package dev.yaclient.modules;

import java.util.Locale;

public class SaturationModule extends PillModule {
    public SaturationModule() { super("saturation", Category.INFO, false, 0.008f, 0.36f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        return String.format(Locale.ROOT, "Saturation: %.2f", mc.thePlayer.getFoodStats().getSaturationLevel());
    }

    @Override
    protected String sample() { return "Saturation: 5.00"; }
}

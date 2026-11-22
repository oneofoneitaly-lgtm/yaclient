package dev.yaclient.modules;

import dev.yaclient.Setting;

import java.util.Locale;

public class CoordsModule extends PillModule {
    private final Setting.Bool decimals = add("general", new Setting.Bool("decimals", false));
    private final Setting.Bool label = add("general", new Setting.Bool("label", true));
    private final Setting.Bool block = add("general", new Setting.Bool("block", false));
    private final Setting.Choice sep = add("general", new Setting.Choice("sep", 0, "space", "comma", "slash"));

    public CoordsModule() { super("coords", Category.INFO, false, 0.008f, 0.116f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        if (StreamerModeModule.hideCoords()) return "XYZ ???";
        String sp = new String[]{" ", ", ", " / "}[sep.index];
        double x = mc.thePlayer.posX, y = mc.thePlayer.posY, z = mc.thePlayer.posZ;
        String pre = label.value ? "XYZ " : "";
        if (decimals.value && !block.value) return pre + String.format(Locale.ROOT, "%.1f", x) + sp + String.format(Locale.ROOT, "%.1f", y) + sp + String.format(Locale.ROOT, "%.1f", z);
        return pre + (int) Math.floor(x) + sp + (int) Math.floor(y) + sp + (int) Math.floor(z);
    }

    @Override
    protected String sample() { return (label.value ? "XYZ " : "") + "128 64 -256"; }
}

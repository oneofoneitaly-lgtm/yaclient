package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingModule extends PillModule {
    private final Setting.Bool label = add("general", new Setting.Bool("label", true));
    private final Setting.Bool colorize = add("general", new Setting.Bool("colorize", true));
    private int last = -1;

    public PingModule() { super("ping", Category.INFO, false, 0.99f, 0.012f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null || mc.getNetHandler() == null) return "";
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        last = info == null ? -1 : info.getResponseTime();
        return (label.value ? "Ping: " : "") + (info == null ? "--" : info.getResponseTime() + "ms");
    }

    @Override
    protected int autoColor() {
        if (!colorize.value || last < 0) return 0;
        return last <= 80 ? 0xFF22C55E : (last <= 160 ? 0xFFFFC21A : 0xFFEF4444);
    }

    @Override
    protected String sample() { return (label.value ? "Ping: " : "") + "42ms"; }
}

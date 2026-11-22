package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;

public class FpsModule extends PillModule {
    private final Setting.Bool label = add("general", new Setting.Bool("label", true));
    private final Setting.Bool colorize = add("general", new Setting.Bool("colorize", false));
    private final Setting.Num warn = add("general", new Setting.Num("warn", 20f, 144f, 5f, 60f, ""));

    public FpsModule() { super("fps", Category.INFO, true, 0.008f, 0.012f); }

    @Override
    protected String text() { return (label.value ? "FPS: " : "") + Minecraft.getDebugFPS(); }

    @Override
    protected int autoColor() {
        if (!colorize.value) return 0;
        int f = Minecraft.getDebugFPS();
        return f >= warn.value ? 0xFF22C55E : (f >= warn.value / 2f ? 0xFFFFC21A : 0xFFEF4444);
    }
}

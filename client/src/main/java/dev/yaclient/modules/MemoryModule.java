package dev.yaclient.modules;

import dev.yaclient.Setting;

public class MemoryModule extends PillModule {
    private final Setting.Choice mode = add("general", new Setting.Choice("mode", 0, "used_max", "used", "percent"));
    private final Setting.Bool label = add("general", new Setting.Bool("label", true));

    public MemoryModule() { super("memory", Category.INFO, false, 0.008f, 0.22f); }

    @Override
    protected String text() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        String pre = label.value ? "Memory: " : "";
        if (mode.index == 1) return pre + used + " MB";
        if (mode.index == 2) return pre + (used * 100L / Math.max(1L, max)) + "%";
        return pre + used + "/" + max + " MB";
    }
}

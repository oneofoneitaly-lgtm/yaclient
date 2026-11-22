package dev.yaclient.modules;

public class DayCounterModule extends PillModule {
    public DayCounterModule() { super("days", Category.INFO, false, 0.008f, 0.16f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        return "Day: " + (mc.theWorld.getWorldTime() / 24000L + 1L);
    }

    @Override
    protected String sample() { return "Day: 12"; }
}

package dev.yaclient.modules;

/** Absorption hearts left after a golden apple. */
public class GappleTimerModule extends PillModule {
    public GappleTimerModule() { super("gapple_timer", Category.PVP, false, 0.45f, 0.38f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        float a = mc.thePlayer.getAbsorptionAmount();
        return a > 0f ? "Absorption " + Math.round(a / 2f * 10f) / 10f : "";
    }

    @Override protected int autoColor() { return 0xFFFFC21A; }

    @Override protected String sample() { return "Absorption 2.0"; }
}

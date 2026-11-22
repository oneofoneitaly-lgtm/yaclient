package dev.yaclient.modules;

public class FallDistanceModule extends PillModule {
    public FallDistanceModule() { super("fall", Category.INFO, false, 0.008f, 0.56f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        return String.format(java.util.Locale.ROOT, "Fall %.1f", mc.thePlayer.fallDistance);
    }

    @Override
    protected String sample() { return "Fall 3.0"; }
}

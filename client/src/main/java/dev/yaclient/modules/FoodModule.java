package dev.yaclient.modules;

public class FoodModule extends PillModule {
    public FoodModule() { super("food", Category.INFO, false, 0.008f, 0.48f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        return String.format(java.util.Locale.ROOT, "Food %d  Sat %.1f", mc.thePlayer.getFoodStats().getFoodLevel(), mc.thePlayer.getFoodStats().getSaturationLevel());
    }

    @Override
    protected String sample() { return "Food 20  Sat 5.0"; }
}

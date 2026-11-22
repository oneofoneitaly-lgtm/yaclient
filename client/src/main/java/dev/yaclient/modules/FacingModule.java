package dev.yaclient.modules;

/** The direction you are looking at, written out ("North East"). */
public class FacingModule extends PillModule {
    private static final String[] NAMES = {"South", "South West", "West", "North West", "North", "North East", "East", "South East"};

    public FacingModule() { super("facing", Category.INFO, false, 0.99f, 0.17f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        float y = ((mc.thePlayer.rotationYaw % 360f) + 360f) % 360f;
        return "Facing: " + NAMES[((int) ((y + 22.5f) / 45f)) % 8];
    }

    @Override
    protected String sample() { return "Facing: North East"; }
}

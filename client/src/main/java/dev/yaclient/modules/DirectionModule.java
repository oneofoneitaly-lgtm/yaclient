package dev.yaclient.modules;

public class DirectionModule extends PillModule {
    static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public DirectionModule() { super("direction", Category.INFO, false, 0.99f, 0.064f); }

    /** Compass letters for a Minecraft yaw angle (0 = south, 90 = west). */
    static String compass(float yaw) {
        float y = ((yaw % 360f) + 360f) % 360f;
        return DIRS[((int) ((y + 22.5f) / 45f)) % 8];
    }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        float yaw = ((mc.thePlayer.rotationYaw % 360f) + 360f) % 360f;
        return compass(yaw) + " " + (int) yaw + "\u00b0";
    }

    @Override
    protected String sample() { return "N 180\u00b0"; }
}

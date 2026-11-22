package dev.yaclient.modules;

public class RotationModule extends PillModule {
    public RotationModule() { super("rotation", Category.INFO, false, 0.008f, 0.36f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        float yaw = net.minecraft.util.MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);
        return String.format(java.util.Locale.ROOT, "Yaw %.1f  Pitch %.1f", yaw, mc.thePlayer.rotationPitch);
    }

    @Override
    protected String sample() { return "Yaw -45.0  Pitch 12.3"; }
}

package dev.yaclient.modules;

/** Smooth, cinematic camera movement (the vanilla smooth camera option). */
public class CinematicCameraModule extends Module {
    private boolean active;

    public CinematicCameraModule() { super("cinematic", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.gameSettings == null) return;
        mc.gameSettings.smoothCamera = true;
        active = true;
    }

    @Override
    public void onAlwaysTick() {
        if (!enabled && active && mc != null && mc.gameSettings != null) {
            mc.gameSettings.smoothCamera = false;
            active = false;
        }
    }
}

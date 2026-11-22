package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Keeps the camera on the view you pick (first person, back or front), whatever you press (F5 included). */
public class PerspectiveLockModule extends Module {
    private final Setting.Choice view = add(new Setting.Choice("view", 0, "first", "back", "front"));

    public PerspectiveLockModule() { super("perspective_lock", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc == null || mc.gameSettings == null || mc.thePlayer == null) return;
        if (mc.gameSettings.thirdPersonView != view.index) mc.gameSettings.thirdPersonView = view.index;
    }
}

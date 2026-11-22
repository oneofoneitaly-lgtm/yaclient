package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Sets your field of view to any value, also outside the normal slider range. */
public class FovModule extends Module {
    private final Setting.Num fov = add(new Setting.Num("fov", 30f, 110f, 1f, 90f, ""));
    private float saved = 70f;
    private boolean active;

    public FovModule() { super("fov", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.gameSettings == null || ZoomModule.active) return;
        if (!active) {
            saved = mc.gameSettings.fovSetting;
            active = true;
        }
        mc.gameSettings.fovSetting = fov.value;
    }

    @Override
    public void onAlwaysTick() {
        if (!enabled && active && mc != null && mc.gameSettings != null) {
            mc.gameSettings.fovSetting = saved;
            active = false;
        }
    }
}

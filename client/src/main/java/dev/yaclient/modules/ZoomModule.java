package dev.yaclient.modules;

import dev.yaclient.Setting;
import org.lwjgl.input.Keyboard;

/**
 * Hold C to zoom, then use the mouse wheel to zoom in or out as much as you want.
 * While zooming, the wheel does not change the selected hotbar slot (the slot is put back).
 */
public class ZoomModule extends Module {
    private final Setting.Num fov = add(new Setting.Num("fov", 1f, 60f, 1f, 30f, ""));
    private final Setting.Bool smooth = add(new Setting.Bool("smooth", true));
    private final Setting.Bool sens = add(new Setting.Bool("sens", true));
    private float saved = -1, savedSens = -1, level;
    /** True while the zoom key is held (the FOV module leaves the field of view alone then). */
    public static volatile boolean active;
    private boolean zooming, savedSmooth;
    private int slot = -1;

    public ZoomModule() { super("zoom", Category.UTILITY, true); }

    @Override
    public void onTick() {
        if (mc.gameSettings == null || mc.thePlayer == null) return;
        boolean down = mc.currentScreen == null && Keyboard.isKeyDown(Keyboard.KEY_C);
        int cur = mc.thePlayer.inventory.currentItem;
        if (down && !zooming) {
            saved = mc.gameSettings.fovSetting;
            savedSens = mc.gameSettings.mouseSensitivity;
            savedSmooth = mc.gameSettings.smoothCamera;
            mc.gameSettings.smoothCamera = smooth.value;
            level = Math.min(fov.value, saved);
            zooming = true;
            active = true;
        } else if (!down && zooming) {
            restore();
        }
        if (zooming) {
            // the wheel moved the hotbar selection: turn that into zoom steps and put the slot back
            if (slot >= 0 && cur != slot) {
                int d = ((cur - slot) % 9 + 9) % 9;
                if (d > 4) d -= 9;                       // negative = wheel up = zoom in
                for (int i = 0; i < Math.abs(d); i++) level *= d < 0 ? 0.85f : 1f / 0.85f;
                level = Math.max(0.5f, Math.min(saved, level));
                mc.thePlayer.inventory.currentItem = slot;
                cur = slot;
            }
            mc.gameSettings.fovSetting = level;
            if (sens.value && saved > 0) mc.gameSettings.mouseSensitivity = Math.max(0.01f, savedSens * Math.max(0.15f, level / saved));
        }
        slot = cur;
    }

    private void restore() {
        mc.gameSettings.fovSetting = saved;
        if (savedSens >= 0) mc.gameSettings.mouseSensitivity = savedSens;
        mc.gameSettings.smoothCamera = savedSmooth;
        zooming = false;
        active = false;
    }

    @Override
    public void onToggle() {
        if (!enabled && zooming && mc != null) restore();
    }
}

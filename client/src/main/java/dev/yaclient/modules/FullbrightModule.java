package dev.yaclient.modules;

import dev.yaclient.Setting;

/**
 * Fullbright / gamma control (client-side only, purely visual). Some servers do not allow it,
 * so it is disabled by default.
 */
public class FullbrightModule extends Module {
    private final Setting.Num gamma = add(new Setting.Num("gamma", 1f, 100f, 1f, 100f, ""));
    private float saved = 1f;
    private boolean active;

    public FullbrightModule() { super("fullbright", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.gameSettings == null) return;
        if (!active) {
            saved = mc.gameSettings.gammaSetting;
            active = true;
        }
        mc.gameSettings.gammaSetting = gamma.value;
    }

    @Override
    public void onAlwaysTick() {
        // if the game was closed while fullbright was on, the value would stay in options.txt
        if (!enabled && !active && mc.gameSettings != null && mc.gameSettings.gammaSetting > 1f) {
            mc.gameSettings.gammaSetting = 1f;
        }
    }

    @Override
    public void onToggle() {
        if (!enabled && active && mc != null && mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = saved > 1f ? 1f : saved;
            active = false;
        }
    }
}

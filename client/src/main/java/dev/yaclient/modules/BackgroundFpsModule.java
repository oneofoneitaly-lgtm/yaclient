package dev.yaclient.modules;

import dev.yaclient.Setting;
import org.lwjgl.opengl.Display;

/** Lowers the frame rate while the game window is in the background (saves CPU/GPU). */
public class BackgroundFpsModule extends Module {
    private final Setting.Num fps = add(new Setting.Num("fps", 5f, 60f, 5f, 15f, ""));
    private boolean applied;
    private int saved;

    public BackgroundFpsModule() { super("bg_fps", Category.UTILITY, false); }

    @Override
    public void onAlwaysTick() {
        if (mc == null || mc.gameSettings == null) return;
        boolean want = enabled && !Display.isActive();
        if (want && !applied) {
            saved = mc.gameSettings.limitFramerate;
            mc.gameSettings.limitFramerate = Math.round(fps.value);
            applied = true;
        } else if (!want && applied) {
            mc.gameSettings.limitFramerate = saved;
            applied = false;
        }
    }
}

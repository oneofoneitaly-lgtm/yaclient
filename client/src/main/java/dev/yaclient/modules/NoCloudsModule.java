package dev.yaclient.modules;

import net.minecraft.client.settings.GameSettings;

/** Removes the clouds (puts your own cloud setting back when it is switched off). */
public class NoCloudsModule extends Module {
    private int saved = -1;
    private boolean applied;

    public NoCloudsModule() { super("no_clouds", Category.UTILITY, false); }

    @Override
    public void onAlwaysTick() {
        if (mc == null || mc.gameSettings == null) return;
        GameSettings gs = mc.gameSettings;
        if (enabled) {
            if (!applied) { saved = gs.clouds; applied = true; }
            if (gs.clouds != 0) gs.clouds = 0;
        } else if (applied) {
            gs.clouds = saved < 0 ? 2 : saved;
            applied = false;
        }
    }
}

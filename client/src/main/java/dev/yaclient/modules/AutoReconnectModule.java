package dev.yaclient.modules;

import dev.yaclient.ReconnectScreen;
import dev.yaclient.RecentServers;
import dev.yaclient.Setting;
import net.minecraft.client.gui.GuiDisconnected;

/** After a kick or a lost connection, reconnects to the same server (a few tries, any click or key cancels). */
public class AutoReconnectModule extends Module {
    private final Setting.Num delay = add(new Setting.Num("delay", 2f, 30f, 1f, 5f, "s"));
    private final Setting.Num maxTries = add(new Setting.Num("tries", 1f, 10f, 1f, 3f, ""));
    public static int tries;

    public AutoReconnectModule() { super("auto_reconnect", Category.UTILITY, false); }

    @Override
    public void onAlwaysTick() {
        if (mc == null) return;
        if (mc.theWorld != null) { tries = 0; return; }
        if (!enabled || RecentServers.lastData == null) return;
        if (mc.currentScreen instanceof GuiDisconnected && tries < Math.round(maxTries.value)) {
            mc.displayGuiScreen(new ReconnectScreen(mc.currentScreen, Math.round(delay.value)));
        }
    }
}

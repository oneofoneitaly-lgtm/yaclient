package dev.yaclient.modules;

import dev.yaclient.Hud;
import net.minecraft.client.settings.KeyBinding;

/** Press the sneak key once to sneak, press again to stop. */
public class ToggleSneakModule extends Module {
    private boolean toggled, was;

    public ToggleSneakModule() { super("toggle_sneak", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        KeyBinding kb = mc.gameSettings.keyBindSneak;
        boolean down = Hud.down(kb);
        if (down && !was) {
            toggled = !toggled;
            if (!toggled) KeyBinding.setKeyBindState(kb.getKeyCode(), false);
        }
        was = down;
        if (toggled) KeyBinding.setKeyBindState(kb.getKeyCode(), true);
    }

    @Override
    public void onToggle() {
        if (!enabled) {
            toggled = false;
            if (mc != null && mc.gameSettings != null) {
                KeyBinding kb = mc.gameSettings.keyBindSneak;
                KeyBinding.setKeyBindState(kb.getKeyCode(), Hud.down(kb));
            }
        }
    }
}

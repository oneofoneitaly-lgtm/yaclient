package dev.yaclient.modules;

import net.minecraft.client.settings.KeyBinding;

/** Keeps sprint pressed while you walk forward. */
public class ToggleSprintModule extends Module {
    public ToggleSprintModule() { super("toggle_sprint", Category.UTILITY, true); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        if (mc.gameSettings.keyBindForward.isKeyDown() && !mc.thePlayer.isSneaking()
                && !mc.thePlayer.isUsingItem() && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}

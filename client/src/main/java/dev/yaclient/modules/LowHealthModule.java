package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

/** Pulsing red screen edges when your health is low. */
public class LowHealthModule extends Module {
    private final Setting.Num hearts = add(new Setting.Num("hearts", 1f, 10f, 1f, 3f, ""));

    public LowHealthModule() { super("low_health", Category.UTILITY, false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null || mc.thePlayer.isDead || mc.currentScreen != null) return;
        float hp = mc.thePlayer.getHealth();
        if (hp <= 0f || hp > hearts.value * 2f) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        float pulse = 0.55f + 0.45f * (float) Math.sin(System.currentTimeMillis() / 220.0);
        float urgency = 1f - Math.max(0f, hp - 2f) / Math.max(1f, hearts.value * 2f - 2f) * 0.5f;
        final int steps = 12;
        for (int i = 0; i < steps; i++) {
            float a = (1f - i / (float) steps) * 0.34f * pulse * urgency;
            int col = (Math.min(255, Math.round(a * 255f)) << 24) | 0xE01010;
            Gui.drawRect(0, i * 2, w, i * 2 + 2, col);
            Gui.drawRect(0, h - i * 2 - 2, w, h - i * 2, col);
            Gui.drawRect(i * 2, i * 2 + 2, i * 2 + 2, h - i * 2 - 2, col);
            Gui.drawRect(w - i * 2 - 2, i * 2 + 2, w - i * 2, h - i * 2 - 2, col);
        }
    }
}

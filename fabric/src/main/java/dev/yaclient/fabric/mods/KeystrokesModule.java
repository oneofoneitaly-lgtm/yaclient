package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Gfx;
import dev.yaclient.fabric.HudModule;
import dev.yaclient.fabric.Setting;
import dev.yaclient.fabric.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** WASD, mouse buttons and space bar, same layout as the 1.8.9 client. */
public class KeystrokesModule extends HudModule {
    private static final int K = 22, G = 3, W = 3 * K + 2 * G;

    private final Setting.Bool showMouse = add("general", new Setting.Bool("mouse", true));
    private final Setting.Bool showSpace = add("general", new Setting.Bool("space", true));

    public KeystrokesModule() { super("keystrokes", "Keystrokes", "WASD, mouse buttons and space bar", Category.PVP, true, 0.008f, 0.3f); }

    @Override public int width(Minecraft mc) { return W; }

    @Override
    public int height(Minecraft mc) {
        int h = 2 * K + G;
        if (showMouse.value) h += G + 20;
        if (showSpace.value) h += G + 16;
        return h;
    }

    private void key(String label, boolean down, int x, int y, int w, int h) {
        Gfx.rr(x, y, w, h, 5, down ? 0xEBF5F5F5 : Theme.HUD_BG);
        if (!down) Gfx.rrOutline(x, y, w, h, 5, 1, 0x1AFFFFFF);
        int lh = Gfx.lineHeight(10, true);
        Gfx.text(label, x + (w - Gfx.width(label, 10, true)) / 2, y + (h - lh) / 2, 10, true, down ? 0xFF101014 : Theme.TEXT);
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        var o = mc.options;
        key("W", o.keyUp.isDown(), K + G, 0, K, K);
        key("A", o.keyLeft.isDown(), 0, K + G, K, K);
        key("S", o.keyDown.isDown(), K + G, K + G, K, K);
        key("D", o.keyRight.isDown(), 2 * (K + G), K + G, K, K);
        int y = 2 * K + 2 * G, half = (W - G) / 2;
        if (showMouse.value) {
            key("LMB", o.keyAttack.isDown(), 0, y, half, 20);
            key("RMB", o.keyUse.isDown(), half + G, y, half, 20);
            y += 20 + G;
        }
        if (showSpace.value) key("SPACE", o.keyJump.isDown(), 0, y, W, 16);
    }
}

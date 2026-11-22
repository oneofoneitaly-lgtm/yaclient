package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Hud;
import dev.yaclient.Lang;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Mouse;

public class KeystrokesModule extends HudModule {
    private static final int K = 22, G = 3, W = 3 * K + 2 * G;
    private final Setting.Bool showMouse = add(new Setting.Bool("mouse", true));
    private final Setting.Bool showSpace = add(new Setting.Bool("space", true));

    public KeystrokesModule() { super("keystrokes", Category.PVP, true, 0.008f, 0.3f); }

    @Override
    public int width() { return W; }

    @Override
    public int height() {
        int h = 2 * K + G;
        if (showMouse.value) h += G + 20;
        if (showSpace.value) h += G + 16;
        return h;
    }

    private void key(String label, boolean down, int x, int y, int w, int h) {
        Gfx.rr(x, y, w, h, 5, down ? 0xEBF5F5F5 : Theme.HUD_BG);
        if (!down) Gfx.rrOutline(x, y, w, h, 5, 1, 0x1AFFFFFF);
        int lh = Gfx.lineHeight(10, true);
        Gfx.text(label, x + (w - Gfx.width(label, 10, true)) / 2, y + (h - lh) / 2, 10, true,
                down ? 0xFF101014 : Theme.TEXT);
    }

    @Override
    protected void draw() {
        GameSettings s = mc.gameSettings;
        key("W", Hud.down(s.keyBindForward), K + G, 0, K, K);
        key("A", Hud.down(s.keyBindLeft), 0, K + G, K, K);
        key("S", Hud.down(s.keyBindBack), K + G, K + G, K, K);
        key("D", Hud.down(s.keyBindRight), 2 * (K + G), K + G, K, K);
        int y = 2 * K + 2 * G;
        if (showMouse.value) {
            int half = (W - G) / 2;
            key("LMB", Mouse.isButtonDown(0), 0, y, half, 20);
            key("RMB", Mouse.isButtonDown(1), half + G, y, half, 20);
            y += 20 + G;
        }
        if (showSpace.value) key(Lang.t("key.space"), Hud.down(s.keyBindJump), 0, y, W, 16);
    }
}

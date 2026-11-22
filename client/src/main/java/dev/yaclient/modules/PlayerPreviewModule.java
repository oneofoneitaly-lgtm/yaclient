package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import net.minecraft.client.gui.inventory.GuiInventory;

/** A small 3D copy of your player. */
public class PlayerPreviewModule extends HudModule {
    private final Setting.Num size = add("layout", new Setting.Num("preview_size", 20f, 80f, 2f, 40f, ""));
    private final Setting.Bool background = add("style", new Setting.Bool("background", true));

    public PlayerPreviewModule() { super("player_preview", Category.INFO, false, 0.90f, 0.40f); }

    @Override
    public int width() { return Math.round(size.value * 1.6f); }

    @Override
    public int height() { return Math.round(size.value * 2.5f); }

    @Override
    protected void draw() {
        int w = width(), h = height();
        if (background.value) {
            Gfx.rr(0, 0, w, h, 6, 0xB8121216);
            Gfx.rrOutline(0, 0, w, h, 6, 1, 0x1AFFFFFF);
        }
        if (mc == null || mc.thePlayer == null) return;
        GuiInventory.drawEntityOnScreen(w / 2, h - 6, Math.round(size.value), -12f, -8f, mc.thePlayer);
    }
}

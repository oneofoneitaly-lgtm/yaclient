package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

/** Your own head (skin face), as big as you like. */
public class PlayerHeadModule extends HudModule {
    private final Setting.Num size = add("layout", new Setting.Num("head_size", 16f, 96f, 2f, 32f, ""));
    private final Setting.Bool hat = add("general", new Setting.Bool("hat_layer", true));
    private final Setting.Bool frame = add("style", new Setting.Bool("frame", false));

    public PlayerHeadModule() { super("player_head", Category.INFO, false, 0.008f, 0.50f); }

    private int s() { return Math.round(size.value); }

    @Override
    public int width() { return s(); }

    @Override
    public int height() { return s(); }

    @Override
    protected void draw() {
        int s = s();
        if (mc == null || mc.thePlayer == null) {
            Gfx.rr(0, 0, s, s, 3, 0x66FFFFFF);
            return;
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, Math.max(0f, Math.min(1f, Gfx.fade)));
        mc.getTextureManager().bindTexture(mc.thePlayer.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect(0, 0, 8f, 8f, 8, 8, s, s, 64f, 64f);
        if (hat.value) Gui.drawScaledCustomSizeModalRect(0, 0, 40f, 8f, 8, 8, s, s, 64f, 64f);
        GlStateManager.color(1f, 1f, 1f, 1f);
        if (frame.value) Gfx.rrOutline(0, 0, s, s, 2, 1, 0x55FFFFFF);
    }
}

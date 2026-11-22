package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ArmorModule extends HudModule {
    private final Setting.Bool held = add(new Setting.Bool("held", true));
    private final Setting.Bool bar = add(new Setting.Bool("bar", true));
    private final Setting.Bool percent = add(new Setting.Bool("percent", false));

    public ArmorModule() { super("armor", Category.PVP, true, 0.99f, 0.4f); }

    @Override
    public int width() { return 56; }

    @Override
    public int height() { return 5 * 26 - 4; }

    @Override
    protected void draw() {
        int lh = Gfx.lineHeight(10, false);
        int y = 0;
        for (int i = 0; i < 5; i++) {
            ItemStack st = null;
            if (mc.thePlayer != null) {
                if (i == 0) st = held.value ? mc.thePlayer.getHeldItem() : null;
                else st = mc.thePlayer.inventory.armorItemInSlot(4 - i);
            }
            if (st == null && !preview) continue;
            Gfx.rr(0, y, 56, 22, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, y, 56, 22, 6, 1, 0x1AFFFFFF);
            if (st != null) {
                String txt = "";
                int tc = Theme.TEXT;
                if (st.isItemStackDamageable()) {
                    int left = st.getMaxDamage() - st.getItemDamage();
                    float f = Math.max(0f, Math.min(1f, left / (float) Math.max(1, st.getMaxDamage())));
                    txt = percent.value ? Math.round(f * 100f) + "%" : String.valueOf(left);
                    int col = f > 0.5f ? 0xFF34D27B : (f > 0.25f ? 0xFFFFC21A : 0xFFEF4444);
                    tc = col;
                    if (bar.value) {
                        Gfx.rr(25, y + 17, 26, 2, 1, 0x33FFFFFF);
                        Gfx.rr(25, y + 17, Math.max(2, Math.round(26 * f)), 2, 1, col);
                    }
                } else if (st.stackSize > 1) txt = String.valueOf(st.stackSize);
                if (!txt.isEmpty()) Gfx.text(txt, 25, y + (bar.value && st.isItemStackDamageable() ? 3 : (22 - lh) / 2), 10, false, tc);

                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(st, 5, y + 3);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                GlStateManager.disableLighting();
            }
            y += 26;
        }
    }
}

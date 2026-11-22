package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** BedWars: iron, gold, diamonds and emeralds in your inventory. */
public class BwResourcesModule extends HudModule {
    public BwResourcesModule() { super("bw_resources", Category.INFO, false, 0.008f, 0.40f); }

    private static final int ROW = 20, W = 56;
    private final int[] n = new int[4];
    private long last;
    private ItemStack[] icons;

    private void refresh() {
        long t = System.currentTimeMillis();
        if (t - last < 250) return;
        last = t;
        n[0] = n[1] = n[2] = n[3] = 0;
        if (mc == null || mc.thePlayer == null) return;
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (s == null) continue;
            Item i = s.getItem();
            if (i == Items.iron_ingot) n[0] += s.stackSize;
            else if (i == Items.gold_ingot) n[1] += s.stackSize;
            else if (i == Items.diamond) n[2] += s.stackSize;
            else if (i == Items.emerald) n[3] += s.stackSize;
        }
    }

    private int rows() {
        refresh();
        int r = 0;
        for (int i = 0; i < 4; i++) if (n[i] > 0) r++;
        return preview && r == 0 ? 4 : r;
    }

    @Override public int width() { return rows() == 0 ? 0 : W; }

    @Override public int height() { return Math.max(ROW, rows() * (ROW + 2) - 2); }

    @Override
    protected void draw() {
        if (icons == null) icons = new ItemStack[]{new ItemStack(Items.iron_ingot), new ItemStack(Items.gold_ingot), new ItemStack(Items.diamond), new ItemStack(Items.emerald)};
        boolean fake = rows() == 4 && n[0] + n[1] + n[2] + n[3] == 0;
        int y = 0;
        for (int i = 0; i < 4; i++) {
            int c = fake ? 16 * (i + 1) : n[i];
            if (c <= 0) continue;
            Gfx.rr(0, y, W, ROW, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, y, W, ROW, 6, 1, 0x1AFFFFFF);
            Gfx.text(String.valueOf(c), 24, y + (ROW - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.TEXT);
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(icons[i], 4, y + 2);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GlStateManager.disableLighting();
            y += ROW + 2;
        }
    }
}

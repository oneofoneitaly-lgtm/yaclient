package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Your inventory on the HUD: the 27 backpack slots and, optionally, the hotbar row. */
public class InventoryHudModule extends HudModule {
    private final Setting.Bool hotbar = add(new Setting.Bool("hotbar", true));
    private final Setting.Bool slots = add(new Setting.Bool("slots", true));

    private ItemStack[] sample;

    public InventoryHudModule() { super("inventory_hud", Category.INFO, false, 0.3f, 0.02f); }

    private int rows() { return hotbar.value ? 4 : 3; }

    @Override
    public int width() { return 9 * 18 + 8; }

    @Override
    public int height() { return rows() * 18 + 8 + (hotbar.value ? 4 : 0); }

    private ItemStack at(int slot) {
        if (mc == null || mc.thePlayer == null) return null;
        return mc.thePlayer.inventory.mainInventory[slot];
    }

    private boolean anyItem() {
        for (int i = 0; i < 36; i++) if (at(i) != null) return true;
        return false;
    }

    private ItemStack[] sampleItems() {
        if (sample == null) {
            try {
                sample = new ItemStack[]{new ItemStack(Items.diamond_sword), new ItemStack(Items.golden_apple, 3), new ItemStack(Items.ender_pearl, 8),
                        new ItemStack(Blocks.planks, 64), new ItemStack(Items.bow), new ItemStack(Items.arrow, 32), new ItemStack(Items.iron_ingot, 12)};
            } catch (Throwable t) { sample = new ItemStack[0]; }
        }
        return sample;
    }

    @Override
    protected void draw() {
        int w = width(), h = height();
        Gfx.rr(0, 0, w, h, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, w, h, 8, 1, 0x1AFFFFFF);
        boolean demo = preview && !anyItem();
        ItemStack[] smp = demo ? sampleItems() : null;
        int n = rows() * 9;
        // slot backgrounds first (2D), items after (3D lighting state)
        for (int i = 0; i < n; i++) {
            int x = 4 + (i % 9) * 18, y = 4 + (i / 9) * 18 + (hotbar.value && i >= 27 ? 4 : 0);
            if (slots.value) Gfx.rr(x + 1, y + 1, 16, 16, 3, 0x14FFFFFF);
        }
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < n; i++) {
            // rows 0-2 = backpack (slots 9..35), row 3 = hotbar (slots 0..8)
            int slot = i < 27 ? 9 + i : i - 27;
            ItemStack s = demo ? (i < smp.length ? smp[i] : null) : at(slot);
            if (s == null) continue;
            int x = 4 + (i % 9) * 18 + 1, y = 4 + (i / 9) * 18 + 1 + (hotbar.value && i >= 27 ? 4 : 0);
            try {
                mc.getRenderItem().renderItemAndEffectIntoGUI(s, x, y);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, s, x, y, null);
            } catch (Throwable ignored) { }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
    }
}

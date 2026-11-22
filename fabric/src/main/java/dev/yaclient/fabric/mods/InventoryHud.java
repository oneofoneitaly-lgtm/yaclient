package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Gfx;
import dev.yaclient.fabric.HudModule;
import dev.yaclient.fabric.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** The 27 inventory slots on the HUD. */
public class InventoryHud extends HudModule {
    public InventoryHud() { super("inventory", "Inventory", "Shows your inventory on the HUD", Category.UTILITY, false, 0.8f, 0.5f); }

    @Override public int width(Minecraft mc) { return 9 * 18 + 8; }

    @Override public int height(Minecraft mc) { return 3 * 18 + 8; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        int w = width(mc), h = height(mc);
        Gfx.rr(0, 0, w, h, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, w, h, 8, 1, 0x1AFFFFFF);
        if (mc.player == null) return;
        for (int i = 0; i < 27; i++) {
            ItemStack st = mc.player.getInventory().getItem(9 + i);
            if (st.isEmpty()) continue;
            int x = 4 + (i % 9) * 18, y = 4 + (i / 9) * 18;
            g.item(st, x + 1, y + 1);
            g.itemDecorations(mc.font, st, x + 1, y + 1);
        }
    }
}

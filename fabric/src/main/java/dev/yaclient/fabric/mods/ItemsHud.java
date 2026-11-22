package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Gfx;
import dev.yaclient.fabric.HudModule;
import dev.yaclient.fabric.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Armor and held items with durability. */
public class ItemsHud extends HudModule {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    public ItemsHud() { super("armor", "Armor", "Armor and held items with durability", Category.PVP, false, 0.008f, 0.62f); }

    @Override public int width(Minecraft mc) { return 66; }

    @Override public int height(Minecraft mc) { return SLOTS.length * 18 + 8; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        int w = width(mc), h = height(mc);
        Gfx.rr(0, 0, w, h, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, w, h, 8, 1, 0x1AFFFFFF);
        if (mc.player == null) return;
        int y = 4;
        for (EquipmentSlot s : SLOTS) {
            ItemStack st = mc.player.getItemBySlot(s);
            if (!st.isEmpty()) {
                g.item(st, 6, y);
                String t = null;
                int col = Theme.TEXT;
                if (st.isDamageableItem()) {
                    int left = st.getMaxDamage() - st.getDamageValue();
                    int pct = left * 100 / Math.max(1, st.getMaxDamage());
                    col = pct > 50 ? 0xFF22C55E : pct > 20 ? 0xFFFFC21A : 0xFFEF4444;
                    t = String.valueOf(left);
                } else if (st.getCount() > 1) t = "x" + st.getCount();
                if (t != null) Gfx.text(t, 28, y + (16 - Gfx.lineHeight(10, true)) / 2, 10, true, col);
            }
            y += 18;
        }
    }
}

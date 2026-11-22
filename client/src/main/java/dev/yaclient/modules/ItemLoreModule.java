package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/** The name and lore lines of the item in your hand. */
public class ItemLoreModule extends HudModule {
    private final Setting.Bool name = add("general", new Setting.Bool("show_name", true));
    private final Setting.Num textSize = add("text", new Setting.Num("text_size", 8f, 14f, 1f, 9f, ""));
    private List<String> cache = new ArrayList<String>();
    private long at;

    public ItemLoreModule() { super("item_lore", Category.INFO, false, 0.008f, 0.74f); }

    private List<String> lines() {
        long now = System.currentTimeMillis();
        if (now - at < 150 && !preview) return cache;
        at = now;
        List<String> out = new ArrayList<String>();
        if (mc != null && mc.thePlayer != null) {
            ItemStack s = mc.thePlayer.getHeldItem();
            if (s != null) {
                List<String> tip = s.getTooltip(mc.thePlayer, false);
                for (int i = name.value ? 0 : 1; i < tip.size() && out.size() < 10; i++) out.add(EnumChatFormatting.getTextWithoutFormattingCodes(tip.get(i)));
            }
        }
        if (out.isEmpty() && preview) { out.add("Item Lore"); out.add("Hold an item with lore"); }
        cache = out;
        return out;
    }

    private int size() { return Math.round(textSize.value); }

    @Override
    public int width() {
        List<String> l = lines();
        if (l.isEmpty()) return 0;
        int w = 0;
        for (String s : l) w = Math.max(w, Gfx.width(s, size(), false));
        return w + 14;
    }

    @Override
    public int height() {
        List<String> l = lines();
        return l.isEmpty() ? 0 : l.size() * (Gfx.lineHeight(size(), false) + 2) + 8;
    }

    @Override
    protected void draw() {
        List<String> l = lines();
        if (l.isEmpty()) return;
        int w = width(), h = height(), lh = Gfx.lineHeight(size(), false) + 2;
        Gfx.rr(0, 0, w, h, 5, 0xB8121216);
        Gfx.rrOutline(0, 0, w, h, 5, 1, 0x1AFFFFFF);
        for (int i = 0; i < l.size(); i++) Gfx.text(l.get(i), 7, 5 + i * lh, size(), false, i == 0 && name.value ? 0xFFF4F4F5 : 0xFFB4B4BE);
    }
}

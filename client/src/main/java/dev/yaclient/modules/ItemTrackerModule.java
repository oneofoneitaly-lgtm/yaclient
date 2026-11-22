package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Shows the items you just picked up or used: "+64 Cobblestone", "-1 Arrow". */
public class ItemTrackerModule extends HudModule {
    private final Setting.Num seconds = add("general", new Setting.Num("show_seconds", 2f, 15f, 1f, 5f, "s"));
    private final Setting.Num rows = add("general", new Setting.Num("max_rows", 1f, 8f, 1f, 4f, ""));

    private static final class Entry {
        ItemStack icon; String name; int delta; long at;
    }

    private final List<Entry> entries = new ArrayList<Entry>();
    private Map<String, Integer> prev = new HashMap<String, Integer>();
    private Map<String, ItemStack> icons = new HashMap<String, ItemStack>();
    private Object lastWorld;

    public ItemTrackerModule() { super("item_tracker", Category.UTILITY, false, 0.008f, 0.86f); }

    @Override
    public void onTick() {
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return;
        Map<String, Integer> now = new HashMap<String, Integer>();
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (s == null) continue;
            String k = s.getDisplayName();
            Integer o = now.get(k);
            now.put(k, (o == null ? 0 : o) + s.stackSize);
            if (!icons.containsKey(k)) { ItemStack c = s.copy(); c.stackSize = 1; icons.put(k, c); }
        }
        if (lastWorld == mc.theWorld) {
            for (Map.Entry<String, Integer> e : now.entrySet()) {
                Integer p = prev.get(e.getKey());
                int d = e.getValue() - (p == null ? 0 : p);
                if (d != 0) note(e.getKey(), d);
            }
            for (Map.Entry<String, Integer> e : prev.entrySet()) {
                if (!now.containsKey(e.getKey())) note(e.getKey(), -e.getValue());
            }
        }
        lastWorld = mc.theWorld;
        prev = now;
        long t = System.currentTimeMillis();
        for (int i = entries.size() - 1; i >= 0; i--) if (t - entries.get(i).at > seconds.value * 1000f) entries.remove(i);
    }

    private void note(String name, int d) {
        long t = System.currentTimeMillis();
        for (Entry e : entries) {
            if (e.name.equals(name) && t - e.at < 1500L && (e.delta > 0) == (d > 0)) { e.delta += d; e.at = t; return; }
        }
        Entry e = new Entry();
        e.name = name; e.delta = d; e.at = t; e.icon = icons.get(name);
        entries.add(0, e);
        while (entries.size() > 8) entries.remove(entries.size() - 1);
    }

    private List<Entry> shown() {
        List<Entry> out = new ArrayList<Entry>();
        if (entries.isEmpty() && preview) {
            Entry a = new Entry(); a.name = "Arrow"; a.delta = -1; a.icon = new ItemStack(Items.arrow);
            Entry b = new Entry(); b.name = "Cobblestone"; b.delta = 64; b.icon = new ItemStack(Item.getItemFromBlock(Blocks.cobblestone));
            Entry c = new Entry(); c.name = "Diamond"; c.delta = 3; c.icon = new ItemStack(Items.diamond);
            out.add(a); out.add(b); out.add(c);
            return out;
        }
        for (int i = 0; i < entries.size() && i < Math.round(rows.value); i++) out.add(entries.get(i));
        return out;
    }

    private static String label(Entry e) { return (e.delta > 0 ? "+" : "") + e.delta + " " + e.name; }

    @Override
    public int width() {
        List<Entry> l = shown();
        if (l.isEmpty()) return 0;
        int w = 0;
        for (Entry e : l) w = Math.max(w, Gfx.width(label(e), 10, false));
        return w + 34;
    }

    @Override
    public int height() {
        int n = shown().size();
        return n == 0 ? 0 : n * 22 + 4;
    }

    @Override
    protected void draw() {
        List<Entry> l = shown();
        if (l.isEmpty()) return;
        Gfx.rr(0, 0, width(), height(), 5, 0xB8121216);
        for (int i = 0; i < l.size(); i++) {
            Entry e = l.get(i);
            int y = 2 + i * 22;
            if (e.icon != null) {
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(e.icon, 5, y + 2);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                GlStateManager.disableLighting();
            }
            Gfx.text(label(e), 27, y + (22 - Gfx.lineHeight(10, false)) / 2, 10, false, e.delta > 0 ? 0xFF4ADE80 : 0xFFF87171);
        }
    }
}

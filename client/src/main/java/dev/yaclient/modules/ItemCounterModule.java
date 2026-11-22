package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

/** How many ender pearls, golden apples, splash potions and arrows you carry. */
public class ItemCounterModule extends HudModule {
    public ItemCounterModule() { super("item_counter", Category.PVP, false, 0.008f, 0.62f); }

    private final Setting.Choice layout = add("general", new Setting.Choice("layout", 0, "vertical", "horizontal"));
    private final Setting.Bool hideZero = add("general", new Setting.Bool("hide_zero", false));
    private final Setting.Bool background = add("style", new Setting.Bool("background", true));
    private final Setting.Bool[] show = {
        add("general", new Setting.Bool("show_pearls", true)), add("general", new Setting.Bool("show_gapples", true)),
        add("general", new Setting.Bool("show_potions", true)), add("general", new Setting.Bool("show_arrows", true))};

    private static final int ROW = 20, W = 52, GAP = 2;
    private ItemStack[] icons;

    private ItemStack[] icons() {
        if (icons == null) icons = new ItemStack[]{new ItemStack(Items.ender_pearl), new ItemStack(Items.golden_apple), new ItemStack(Items.potionitem, 1, 16421), new ItemStack(Items.arrow)};
        return icons;
    }

    private int count(int kind) {
        if (mc == null || mc.thePlayer == null) return 0;
        int n = 0;
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (s == null) continue;
            boolean m;
            switch (kind) {
                case 0: m = s.getItem() == Items.ender_pearl; break;
                case 1: m = s.getItem() == Items.golden_apple; break;
                case 2: m = s.getItem() instanceof ItemPotion && (s.getMetadata() & 16384) != 0; break;
                default: m = s.getItem() == Items.arrow; break;
            }
            if (m) n += s.stackSize;
        }
        return n;
    }

    /** Indices of the entries to draw right now. */
    private int[] entries() {
        int[] out = new int[4];
        int r = 0;
        for (int i = 0; i < 4; i++) {
            if (!show[i].value) continue;
            if (hideZero.value && count(i) <= 0 && !preview) continue;
            out[r++] = i;
        }
        if (r == 0 && preview) { for (int i = 0; i < 4; i++) out[i] = i; r = 4; }
        return java.util.Arrays.copyOf(out, r);
    }

    private boolean horiz() { return layout.index == 1; }

    @Override
    public int width() {
        int r = entries().length;
        if (r == 0) return 0;
        return horiz() ? r * W + (r - 1) * GAP : W;
    }

    @Override
    public int height() {
        int r = entries().length;
        if (r == 0) return 0;
        return horiz() ? ROW : r * (ROW + GAP) - GAP;
    }

    @Override
    protected void draw() {
        ItemStack[] ic = icons();
        int[] es = entries();
        boolean fake = preview && count(0) + count(1) + count(2) + count(3) == 0;
        int x = 0, y = 0;
        for (int k = 0; k < es.length; k++) {
            int i = es[k];
            int n = fake ? 8 + i : count(i);
            if (background.value) {
                Gfx.rr(x, y, W, ROW, 6, Theme.HUD_BG);
                Gfx.rrOutline(x, y, W, ROW, 6, 1, 0x1AFFFFFF);
            }
            Gfx.text(String.valueOf(n), x + 24, y + (ROW - Gfx.lineHeight(10, false)) / 2, 10, false, n > 0 ? Theme.TEXT : 0x80FFFFFF);
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(ic[i], x + 4, y + 2);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GlStateManager.disableLighting();
            if (horiz()) x += W + GAP; else y += ROW + GAP;
        }
    }
}

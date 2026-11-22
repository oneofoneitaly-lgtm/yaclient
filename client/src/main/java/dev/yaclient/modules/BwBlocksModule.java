package dev.yaclient.modules;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** BedWars: how many placeable blocks you carry (wool, clay, wood, end stone, glass, obsidian...). */
public class BwBlocksModule extends PillModule {
    private long last;
    private int count;

    public BwBlocksModule() { super("bw_blocks", Category.INFO, false, 0.008f, 0.36f); }

    @Override
    protected String text() {
        long t = System.currentTimeMillis();
        if (t - last >= 250) {
            last = t;
            count = 0;
            if (mc != null && mc.thePlayer != null) {
                for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
                    if (s != null && s.getItem() instanceof ItemBlock) {
                        Block b = ((ItemBlock) s.getItem()).getBlock();
                        if (b.isFullCube()) count += s.stackSize;
                    }
                }
            }
        }
        return mc == null || mc.thePlayer == null ? "" : "Blocks " + count;
    }

    @Override protected String sample() { return "Blocks 128"; }

    @Override
    protected int autoColor() { return count > 0 && count < 16 ? 0xFFEF4444 : 0; }
}

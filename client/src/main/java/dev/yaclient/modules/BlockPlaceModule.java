package dev.yaclient.modules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.ArrayDeque;

/** Blocks you place per second (bridging speed). Counts the blocks that leave your held stack. */
public class BlockPlaceModule extends PillModule {
    private final ArrayDeque<Long> times = new ArrayDeque<Long>();
    private Item lastItem;
    private int lastSize = -1, lastSlot = -1;

    public BlockPlaceModule() { super("block_place", Category.PVP, false, 0.008f, 0.42f); }

    @Override
    public void onTick() {
        if (mc == null || mc.thePlayer == null) return;
        ItemStack s = mc.thePlayer.getHeldItem();
        int slot = mc.thePlayer.inventory.currentItem;
        if (s != null && s.getItem() instanceof ItemBlock && slot == lastSlot && s.getItem() == lastItem && s.stackSize < lastSize) {
            long n = System.currentTimeMillis();
            for (int i = 0; i < lastSize - s.stackSize && i < 8; i++) times.addLast(n);
        }
        lastSlot = slot;
        lastItem = s == null ? null : s.getItem();
        lastSize = s == null ? -1 : s.stackSize;
    }

    @Override
    protected String text() {
        long now = System.currentTimeMillis();
        while (!times.isEmpty() && now - times.peekFirst() > 1000L) times.pollFirst();
        return "Blocks: " + times.size() + "/s";
    }

    @Override
    protected String sample() { return "Blocks: 7/s"; }
}

package dev.yaclient.modules;

import net.minecraft.item.ItemStack;

public class ItemInfoModule extends PillModule {
    public ItemInfoModule() { super("item_info", Category.INFO, false, 0.008f, 0.4f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        ItemStack st = mc.thePlayer.getHeldItem();
        if (st == null) return "";
        if (st.isItemStackDamageable()) return "Durability " + (st.getMaxDamage() - st.getItemDamage()) + "/" + st.getMaxDamage();
        return st.stackSize > 1 ? st.getDisplayName() + " x" + st.stackSize : st.getDisplayName();
    }

    @Override
    protected String sample() { return "Durability 1500/1561"; }
}

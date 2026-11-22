package dev.yaclient.modules;

import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;

/** Shows how far your bow is drawn. */
public class BowChargeModule extends PillModule {
    public BowChargeModule() { super("bow_charge", Category.PVP, false, 0.45f, 0.56f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null || !mc.thePlayer.isUsingItem()) return "";
        ItemStack st = mc.thePlayer.getItemInUse();
        if (st == null || !(st.getItem() instanceof ItemBow)) return "";
        int pct = Math.min(100, Math.round(mc.thePlayer.getItemInUseDuration() / 20f * 100f));
        return "Bow " + pct + "%";
    }

    @Override
    protected String sample() { return "Bow 100%"; }
}

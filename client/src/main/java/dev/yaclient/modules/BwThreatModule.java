package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;

/** BedWars: the closest enemy with what he holds and how his armor looks (leather / chain / iron / diamond). */
public class BwThreatModule extends PillModule {
    private final Setting.Num range = add("general", new Setting.Num("range", 5f, 40f, 5f, 20f, ""));
    private long last;
    private String txt = "";

    public BwThreatModule() { super("bw_threat", Category.PVP, false, 0.008f, 0.44f); }

    private static String armor(EntityPlayer p) {
        ItemStack b = p.getCurrentArmor(0);
        ItemStack c = p.getCurrentArmor(2);
        ItemStack s = c != null ? c : b;
        if (s == null || !(s.getItem() instanceof ItemArmor)) return "no armor";
        switch (((ItemArmor) s.getItem()).getArmorMaterial()) {
            case LEATHER: return "leather";
            case CHAIN: return "chain";
            case IRON: return "iron";
            case GOLD: return "gold";
            default: return "diamond";
        }
    }

    private static String held(EntityPlayer p) {
        ItemStack h = p.getHeldItem();
        if (h == null) return "hand";
        if (h.getItem() instanceof ItemSword) return "sword";
        if (h.getItem() instanceof ItemAxe) return "axe";
        if (h.getItem() instanceof ItemPickaxe) return "pickaxe";
        return h.getDisplayName();
    }

    @Override
    protected String text() {
        long t = System.currentTimeMillis();
        if (t - last < 200) return txt;
        last = t;
        txt = "";
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return txt;
        EntityPlayer best = null;
        float bd = range.value;
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p == mc.thePlayer || p.isInvisible() || p.isDead) continue;
            float d = mc.thePlayer.getDistanceToEntity(p);
            if (d <= bd) { bd = d; best = p; }
        }
        if (best != null) txt = best.getName() + " " + Math.round(bd) + "m  " + held(best) + "  " + armor(best);
        return txt;
    }

    @Override protected String sample() { return "Player1 8m  sword  iron"; }
}

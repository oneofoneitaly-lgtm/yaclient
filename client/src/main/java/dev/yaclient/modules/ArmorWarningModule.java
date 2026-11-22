package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.item.ItemStack;

/** Warns when a piece of your armor is about to break. */
public class ArmorWarningModule extends PillModule {
    private final Setting.Num limit = add("general", new Setting.Num("limit", 5f, 50f, 5f, 20f, "%"));
    private long last;
    private String txt = "";

    public ArmorWarningModule() { super("armor_warning", Category.PVP, false, 0.45f, 0.30f); }

    @Override
    protected String text() {
        long t = System.currentTimeMillis();
        if (t - last < 500) return txt;
        last = t;
        txt = "";
        if (mc == null || mc.thePlayer == null) return txt;
        int worst = 101;
        for (int i = 0; i < 4; i++) {
            ItemStack s = mc.thePlayer.getCurrentArmor(i);
            if (s == null || s.getMaxDamage() <= 0) continue;
            int pc = 100 - Math.round(100f * s.getItemDamage() / s.getMaxDamage());
            if (pc < worst) worst = pc;
        }
        if (worst <= limit.value) txt = "Armor " + worst + "%";
        return txt;
    }

    @Override protected int autoColor() { return 0xFFEF4444; }

    @Override protected String sample() { return "Armor 12%"; }
}

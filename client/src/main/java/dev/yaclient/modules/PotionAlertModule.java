package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/** Tells you when a potion effect is about to run out, so you can drink the next one in time. */
public class PotionAlertModule extends PillModule {
    private final Setting.Num secs = add("general", new Setting.Num("seconds", 3f, 30f, 1f, 10f, ""));
    private long last;
    private String txt = "";

    public PotionAlertModule() { super("potion_alert", Category.PVP, false, 0.45f, 0.42f); }

    @Override
    protected String text() {
        long t = System.currentTimeMillis();
        if (t - last < 250) return txt;
        last = t;
        txt = "";
        if (mc == null || mc.thePlayer == null) return txt;
        int best = Integer.MAX_VALUE;
        String name = "";
        for (PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
            Potion p = Potion.potionTypes[e.getPotionID()];
            if (p == null || p.isBadEffect() || e.getDuration() > secs.value * 20) continue;
            if (e.getDuration() < best) { best = e.getDuration(); name = I18n.format(p.getName(), new Object[0]); }
        }
        if (best != Integer.MAX_VALUE) txt = name + " " + (best / 20) + "s";
        return txt;
    }

    @Override protected int autoColor() { return 0xFFF97316; }

    @Override protected String sample() { return "Speed 7s"; }
}

package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Theme;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class PotionModule extends HudModule {
    public PotionModule() { super("potions", Category.PVP, false, 0.008f, 0.62f); }

    private List<String> lines() {
        List<String> out = new ArrayList<String>();
        if (mc != null && mc.thePlayer != null) {
            for (PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                Potion p = Potion.potionTypes[e.getPotionID()];
                if (p == null) continue;
                String n = I18n.format(p.getName(), new Object[0]);
                if (e.getAmplifier() > 0) n += " " + (e.getAmplifier() + 1);
                out.add(n + " " + Potion.getDurationString(e));
            }
        }
        if (out.isEmpty() && preview) out.add("Speed II 0:30");
        return out;
    }

    @Override
    public int width() {
        int w = 0;
        for (String s : lines()) w = Math.max(w, Gfx.width(s, 10, false) + 16);
        return w;
    }

    @Override
    public int height() { return Math.max(1, lines().size()) * 20 - 4; }

    @Override
    protected void draw() {
        int y = 0;
        int w = width();
        for (String s : lines()) {
            Gfx.rr(0, y, w, 16, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, y, w, 16, 6, 1, 0x1AFFFFFF);
            Gfx.text(s, 8, y + (16 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.TEXT);
            y += 20;
        }
    }
}

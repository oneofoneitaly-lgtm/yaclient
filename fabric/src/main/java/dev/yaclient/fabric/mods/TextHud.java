package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Function;

/** A HUD box that shows one or more lines of text computed every frame. */
public class TextHud extends HudModule {
    private final Function<Minecraft, String> fn;

    public TextHud(String id, String name, String desc, Category cat, boolean def, float x, float y, Function<Minecraft, String> fn) {
        super(id, name, desc, cat, def, x, y);
        this.fn = fn;
    }

    // the text is rebuilt at most 10 times a second: width, height and draw all share it
    private String[] cache = {""};
    private int cw = 20, ch = 16;
    private long cacheAt;

    private void refresh(Minecraft mc) {
        long now = System.currentTimeMillis();
        if (now - cacheAt < 100L) return;
        cacheAt = now;
        String[] ls;
        try { String s = fn.apply(mc); ls = (s == null ? "" : s).split("\n"); } catch (Exception e) { ls = new String[]{"-"}; }
        cache = ls;
        int w = 20;
        for (String l : ls) w = Math.max(w, mc.font.width(l) + 10);
        cw = w;
        ch = ls.length * 11 + 5;
    }

    @Override public int width(Minecraft mc) { refresh(mc); return cw; }

    @Override public int height(Minecraft mc) { refresh(mc); return ch; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        refresh(mc);
        g.fill(0, 0, cw, ch, 0x90101014);
        int y = 4;
        int i = 0;
        for (String l : cache) { g.text(mc.font, l, 5, y, Extra.rainbow ? Extra.rainbowRgb(i++) : 0xFFFFFFFF, true); y += 11; }
    }
}

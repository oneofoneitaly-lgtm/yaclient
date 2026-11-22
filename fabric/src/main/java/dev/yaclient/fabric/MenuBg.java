package dev.yaclient.fabric;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Same backdrop as the 1.8.9 menus: the game's panorama slowly drifting, a bright frosted veil on top and soft light blobs. */
final class MenuBg {
    private MenuBg() {}

    static final int INK = 0xFF1A1C26, INK2 = 0xFF5B6272;
    private static final Identifier[] PANO = new Identifier[4];
    static { for (int i = 0; i < 4; i++) PANO[i] = Identifier.withDefaultNamespace("textures/gui/title/background/panorama_" + i + ".png"); }

    static void draw(GuiGraphicsExtractor g, int w, int h, float t) {
        Gfx.g = g;
        g.fill(0, 0, w, h, 0xFFE9EEF7);
        // the four side faces of the panorama in a row, scrolling slowly (one full turn every ~4 minutes)
        int fs = Math.max(h, 1);
        float off = (t * 4f * fs / 60f) % (4f * fs);
        int first = (int) Math.floor(off / fs);
        for (int i = 0; i * fs - (off - first * fs) < w; i++) {
            int x = Math.round(i * fs - (off - first * fs));
            Gfx.tex(PANO[(first + i) % 4], x, 0, fs, fs, 0f, 0f, 256, 256, 256, 256, 0xFFFFFFFF);
        }
        // frosted light veil so everything on top reads clean and bright
        g.fillGradient(0, 0, w, h, 0xCCF4F6FC, 0xD2DCE3F0);
        Gfx.glow(w * 0.5f, h * 0.46f, w * 0.36f, h * 0.6f, 0xFFFFFF, 120);
        Gfx.glow(w * 0.18f + (float) Math.sin(t * 0.2f) * 30f, h * 0.3f, w * 0.25f, h * 0.45f, 0xFFFFFF, 70);
        Gfx.glow(w * 0.85f + (float) Math.cos(t * 0.17f) * 30f, h * 0.75f, w * 0.25f, h * 0.45f, 0xFFFFFF, 70);
    }

    /** Soft floor shadow under the player / logo. */
    static void blob(float cx, float cy, float rx, float ry, int rgb, int alpha) {
        Gfx.glow(cx, cy, rx, ry, rgb, alpha);
    }
}

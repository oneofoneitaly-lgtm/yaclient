package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/** The shared look of the loading screens: bright backdrop, bobbing logo, title, subtitle and a sliding progress bar. */
final class LoadingArt {
    private LoadingArt() {}

    private static int logo(int h) { return Math.min(56, h / 5); }

    private static int titleY(int h) { return h / 2 - logo(h) / 2 - 30 + logo(h) + 16; }

    /** Y of the progress bar, so a screen can place its own button under it. */
    static int barY(int h) { return titleY(h) + 34; }

    static void draw(int w, int h, String title, String sub, long opened) {
        long now = System.currentTimeMillis();
        float t = (now - opened) / 1000f;
        MenuBg.draw(w, h, t);
        int gs = Math.max(1, new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
        Gfx.setScale(gs);
        float fade = Ui.ease((now - opened) / 300f);
        Gfx.fade = fade;
        int logo = logo(h), cx = w / 2;
        float lx = cx - logo / 2f, ly = h / 2 - logo / 2 - 30 + (float) Math.sin(t * 1.6f) * 1.5f;
        MenuBg.blob(cx, ly + logo + 4, logo * 0.7f, 4f, 0x1A2238, 60);
        for (int k = 0; k < 8; k++) {
            double ang = k * Math.PI / 4;
            Logo.drawTint(lx + (float) Math.cos(ang) * 1.6f, ly + (float) Math.sin(ang) * 1.6f, logo, logo, 0.92f * fade, 0.10f, 0.11f, 0.15f);
        }
        Logo.draw(lx, ly, logo, logo, fade);
        int ty = titleY(h);
        Gfx.textCentered(title, cx, ty, 11, true, MenuBg.INK);
        if (sub != null && !sub.isEmpty()) Gfx.textCentered(Gfx.trim(sub, 8, false, w - 40), cx, ty + 16, 8, false, MenuBg.INK2);
        int bw = 140, bx = cx - bw / 2, by = barY(h);
        Gfx.rr(bx, by, bw, 4, 2, 0x28101830);
        float p = (t * 0.9f) % 1.6f;
        int seg = 46;
        int sx = bx + Math.round(p / 1.6f * (bw + seg)) - seg;
        int x0 = Math.max(bx, sx), x1 = Math.min(bx + bw, sx + seg);
        if (x1 - x0 >= 4) Gfx.rr(x0, by, x1 - x0, 4, 2, Theme.accent());
        Gfx.fade = 1f;
    }

    /** A button in the menu style, used by the screens that draw their own buttons. */
    static void button(int x, int y, int w, int h, String label, boolean primary, boolean hover, boolean enabled) {
        int fill = !enabled ? 0x60101014 : (primary ? Ui.mix(Theme.accent(), 0xFFFFFFFF, hover ? 0.12f : 0f) : (hover ? 0xE8242429 : 0xD0101014));
        Gfx.rr(x, y + 2, w, h, 8, 0x22101830);
        Gfx.rr(x, y, w, h, 8, fill);
        Gfx.rrOutline(x, y, w, h, 8, 1, primary ? 0x50FFFFFF : (hover ? 0x80FFFFFF : 0x40FFFFFF));
        Gfx.textCentered(label, x + w / 2, y + (h - Gfx.lineHeight(9, true)) / 2, 9, true, enabled ? (primary ? 0xFFFFFFFF : Theme.TEXT) : Theme.DIM);
    }
}

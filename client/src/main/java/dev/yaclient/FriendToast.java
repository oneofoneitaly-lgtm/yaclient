package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.List;

/** Top-right notification: YA logo on the left, the friend's name, and a green dot when they come online. */
public final class FriendToast {
    public static final int ONLINE = 0, REQUEST = 1;
    private static final class T { String name; int kind; long at; }
    private static final List<T> Q = new ArrayList<T>();
    private static final long IN = 320, STAY = 4200, OUT = 320;

    private FriendToast() {}

    static void push(String name, int kind) {
        synchronized (Q) {
            T t = new T();
            t.name = name; t.kind = kind; t.at = 0;
            Q.add(t);
        }
    }

    /** Draws the active toast; safe to call from any screen or from the in-game overlay. */
    public static void render() {
        T t;
        synchronized (Q) {
            if (Q.isEmpty()) return;
            t = Q.get(0);
            long now = System.currentTimeMillis();
            if (t.at == 0) t.at = now;
            if (now - t.at > IN + STAY + OUT) { Q.remove(0); return; }
        }
        long age = System.currentTimeMillis() - t.at;
        float slide = age < IN ? Ui.ease(age / (float) IN) : (age > IN + STAY ? 1f - Ui.ease((age - IN - STAY) / (float) OUT) : 1f);

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        Gfx.setScale(Math.max(1, sr.getScaleFactor()));
        float oldFade = Gfx.fade;
        Gfx.fade = 1f;
        draw(sr.getScaledWidth(), t.name, t.kind, slide);
        Gfx.fade = oldFade;
    }

    /** Draws one toast at the given slide progress (0 = off screen, 1 = shown). Also used by previews. */
    static void draw(int screenW, String name, int kind, float slide) {
        int w = 150, h = 30;
        int x = Math.round(screenW - 8 - (w + 12) * slide);
        int y = 8;
        Gfx.rr(x, y, w, h, 8, 0xE6121216);
        Gfx.rrOutline(x, y, w, h, 8, 1, 0x40FFFFFF);
        Logo.draw(x + 6, y + 5, 20, 20, 1f);
        String line = kind == REQUEST ? Lang.t("friends.toast.request") : Lang.t("friends.toast.online");
        Gfx.text(Gfx.trim(name, 9, true, w - 58), x + 32, y + 5, 9, true, Theme.TEXT);
        Gfx.text(Gfx.trim(line, 7, false, w - 58), x + 32, y + 17, 7, false, Theme.MUTED);
        int dot = 8, dx = x + w - 14, dy = y + (h - dot) / 2;
        if (kind == REQUEST) {
            Gfx.icon("friends", dx - 3, dy - 3, 14, Theme.TEXT);
        } else {
            Gfx.rr(dx - 2, dy - 2, dot + 4, dot + 4, (dot + 4) / 2, 0x4422C55E);
            Gfx.rr(dx, dy, dot, dot, dot / 2, 0xFF22C55E);
        }
    }
}

package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Changelog sheet: banner on top, then what is new / improved / fixed. Scrolls with the wheel; ESC or the X closes it. */
public class NewsScreen extends GuiScreen {
    public static final String VERSION = "1.6";
    public static final String DATE = "2026-11-19";

    private static final String[][] SECTIONS = {
            {"new", "singles", "pixel", "phone"},
            {"improved", "sync", "perf", "photos"},
            {"fixed", "tagfix"}};

    private final GuiScreen parent;
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float scroll, scrollTarget, contentH, closeHover;
    private int px, py, pw, ph, vy, vh;

    public NewsScreen(GuiScreen parent) { this.parent = parent; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 220f);
        drawGradientRect(0, 0, width, height, 0xA0000000, 0xC0000000);

        Minecraft mc = Minecraft.getMinecraft();
        int sf = new ScaledResolution(mc).getScaleFactor();
        Gfx.setScale(Math.max(1, sf));
        Gfx.fade = fade;

        pw = Math.min(width - 20, 400);
        ph = Math.min(height - 16, 280);
        px = (width - pw) / 2;
        py = (height - ph) / 2 + Math.round((1f - fade) * 8f);
        Gfx.rr(px, py, pw, ph, 10, Theme.GLASS);
        Gfx.rrOutline(px, py, pw, ph, 10, 1, Theme.GLASS_LINE);

        // header
        Gfx.text(Lang.t("news.header"), px + 14, py + 10, 12, true, Theme.TEXT);
        int cx = px + pw - 24, cy = py + 7;
        boolean over = mx >= cx && mx < cx + 18 && my >= cy && my < cy + 18;
        closeHover = Ui.approach(closeHover, over ? 1f : 0f, dt, 16f);
        Gfx.rr(cx, cy, 18, 18, 6, Ui.mix(0x20FFFFFF, 0x40FFFFFF, closeHover));
        Gfx.icon("close", cx + 4, cy + 4, 10, Theme.TEXT);

        vy = py + 32;
        vh = ph - 40;
        int ix = px + 14, iw = pw - 28;

        scroll = Ui.approach(scroll, scrollTarget, dt, 16f);
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - vh));

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(px * sf, (height - (vy + vh)) * sf, pw * sf, vh * sf);
        int y = vy - Math.round(scroll);
        int top = y;

        int bh = Math.round(iw / NewsArt.ASPECT);
        NewsArt.draw(ix, y, iw, bh, fade);
        Gfx.rrOutline(ix, y, iw, bh, 4, 1, 0x30FFFFFF);
        y += bh + 10;

        Gfx.text("YA CLIENT " + VERSION, ix, y, 12, true, Theme.TEXT);
        Gfx.text(DATE, ix + iw - Gfx.width(DATE, 9, false), y + 2, 9, false, Theme.MUTED);
        y += 16;
        for (String line : Lang.t("news.intro").split("\n")) {
            Gfx.text(Gfx.trim(line, 9, false, iw), ix, y, 9, false, Theme.MUTED);
            y += 12;
        }
        y += 6;

        for (String[] sec : SECTIONS) {
            String name = Lang.t("news.sec." + sec[0]);
            int tw = Gfx.width(name, 8, true) + 14;
            Gfx.rr(ix, y, tw, 14, 7, 0x26FFFFFF);
            Gfx.text(name, ix + 7, y + 3, 8, true, Theme.TEXT);
            y += 20;
            for (int i = 1; i < sec.length; i++) {
                String k = "news.i." + sec[i];
                Gfx.rr(ix + 2, y + 4, 4, 4, 2, Theme.TEXT);
                Gfx.text(Gfx.trim(Lang.t(k + ".t"), 9, true, iw - 14), ix + 12, y, 9, true, Theme.TEXT);
                y += 12;
                for (String line : wrap(Lang.t(k + ".d"), iw - 14, 8)) {
                    Gfx.text(line, ix + 12, y, 8, false, Theme.MUTED);
                    y += 10;
                }
                y += 6;
            }
            y += 4;
        }
        contentH = y - top + 6;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // scrollbar
        if (contentH > vh) {
            int kh = Math.max(16, Math.round(vh * vh / contentH));
            float ky = vy + (scroll / (contentH - vh)) * (vh - kh);
            Gfx.rr(px + pw - 7, Math.round(ky), 3, kh, 1, 0x50FFFFFF);
        }
        Gfx.fade = 1f;
    }

    private static java.util.List<String> wrap(String s, int maxW, int size) {
        java.util.List<String> out = new java.util.ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        for (String w : s.split(" ")) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (Gfx.width(test, size, false) > maxW && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(w);
            } else cur = new StringBuilder(test);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    @Override
    public void handleMouseInput() {
        try { super.handleMouseInput(); } catch (Exception ignored) { }
        int d = Mouse.getEventDWheel();
        if (d != 0) scrollTarget -= Math.signum(d) * 30f;
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        int cx = px + pw - 24, cy = py + 7;
        boolean outside = mx < px || mx > px + pw || my < py || my > py + ph;
        if (outside || (mx >= cx && mx < cx + 18 && my >= cy && my < cy + 18)) close();
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (key == Keyboard.KEY_ESCAPE) close();
    }

    private void close() { mc.displayGuiScreen(parent); }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

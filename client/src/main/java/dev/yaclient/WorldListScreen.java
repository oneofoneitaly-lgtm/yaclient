package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.SaveFormatComparator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The singleplayer world list, built like the server list: search, filter tabs, a list with world icons and a side
 * panel with the chosen world (play, rename, delete). Replaces the vanilla world selection screen.
 */
public class WorldListScreen extends GuiScreen {
    private static final int ROW = 30;

    /** A tiny text box (the vanilla one cannot be styled). */
    private static final class Field {
        String value = ""; final int max; boolean focus; int x, y, w, h;
        Field(int max) { this.max = max; }
        boolean hit(int mx, int my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
        void type(char c, int key) {
            if (key == 14) { if (!value.isEmpty()) value = value.substring(0, value.length() - 1); return; }
            if (key == 47 && (org.lwjgl.input.Keyboard.isKeyDown(29) || org.lwjgl.input.Keyboard.isKeyDown(157))) {
                String clip = GuiScreen.getClipboardString();
                if (clip != null) for (char ch : clip.toCharArray()) if (ChatAllowedCharacters.isAllowedCharacter(ch) && value.length() < max) value += ch;
                return;
            }
            if (ChatAllowedCharacters.isAllowedCharacter(c) && value.length() < max) value += c;
        }
        void draw(String hint, int size) {
            Gfx.rr(x, y, w, h, 5, focus ? 0x30FFFFFF : 0x18FFFFFF);
            Gfx.rrOutline(x, y, w, h, 5, 1, focus ? 0xCCFFFFFF : 0x30FFFFFF);
            int ty = y + (h - Gfx.lineHeight(size, false)) / 2;
            if (value.isEmpty()) Gfx.text(Gfx.trim(hint, size, false, w - 12), x + 6, ty, size, false, Theme.DIM);
            else {
                String shown = Gfx.trim(value, size, false, w - 14);
                Gfx.text(shown, x + 6, ty, size, false, Theme.TEXT);
                if (focus && (System.currentTimeMillis() / 500) % 2 == 0) Gfx.rr(x + 7 + Gfx.width(shown, size, false), ty, 1, Gfx.lineHeight(size, false), 0, 0xFFFFFFFF);
            }
        }
    }

    private static final class W {
        String folder, name;
        long last, size;
        int mode;
        boolean hard, cheats, conv;
        ResourceLocation icon;
        boolean tried;
    }

    private final GuiScreen parent;
    private final List<W> all = new ArrayList<W>();
    private final Field search = new Field(40);
    private final Field fName = new Field(48);
    private final long opened = System.currentTimeMillis();
    private int tab;               // 0 all, 1 survival, 2 creative, 3 hardcore
    private W selected;
    private float scroll, scrollTarget;
    private long lastClick; private W lastClickRow;
    private int gs = 2;
    private int dlg;               // 0 none, 1 rename, 2 delete
    private int mainX, mainW, sideX, sideW, listY, listH, hdrY, hdrH, addX, addW;
    private int dx, dy, dw, dh;

    public WorldListScreen(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        String keep = selected == null ? null : selected.folder;
        all.clear();
        try {
            mc.getSaveLoader().flushCache();
            List<SaveFormatComparator> l = mc.getSaveLoader().getSaveList();
            Collections.sort(l);
            for (SaveFormatComparator c : l) {
                W w = new W();
                w.folder = c.getFileName();
                String dn = c.getDisplayName();
                w.name = dn == null || dn.trim().isEmpty() ? w.folder : dn;
                w.last = c.getLastTimePlayed();
                w.size = c.getSizeOnDisk();
                w.mode = c.getEnumGameType().getID();
                w.hard = c.isHardcoreModeEnabled();
                w.cheats = c.getCheatsEnabled();
                w.conv = c.requiresConversion();
                all.add(w);
            }
        } catch (Exception ignored) { }
        selected = null;
        for (W w : all) if (w.folder.equals(keep)) selected = w;
        if (selected == null && !all.isEmpty()) selected = all.get(0);
    }

    // ---------- data ----------
    private List<W> rows() {
        String q = search.value.trim().toLowerCase();
        List<W> out = new ArrayList<W>();
        for (W w : all) {
            if (tab == 1 && (w.hard || w.mode != 0)) continue;
            if (tab == 2 && w.mode != 1) continue;
            if (tab == 3 && !w.hard) continue;
            if (!q.isEmpty() && !(w.name + " " + w.folder).toLowerCase().contains(q)) continue;
            out.add(w);
        }
        return out;
    }

    private static String modeName(W w) {
        if (w.hard) return Lang.t("wld.hardcore");
        switch (w.mode) {
            case 1: return Lang.t("wld.creative");
            case 2: return Lang.t("wld.adventure");
            case 3: return Lang.t("wld.spectator");
            default: return Lang.t("wld.survival");
        }
    }

    private static int modeColor(W w) {
        if (w.hard) return 0xFFEF4444;
        switch (w.mode) {
            case 1: return 0xFF5AA9FF;
            case 2: return 0xFFFFC21A;
            case 3: return 0xFFB58CFF;
            default: return 0xFF34D27B;
        }
    }

    private static String date(long t) { return t <= 0 ? "-" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(t)); }

    private static String size(long b) { return b <= 0 ? "-" : (b < 1048576L ? (b / 1024L) + " KB" : (b / 1048576L) + " MB"); }

    private void play(W w) {
        if (w == null) return;
        try {
            if (!mc.getSaveLoader().canLoadWorld(w.folder)) return;
        } catch (Throwable ignored) { }
        mc.displayGuiScreen(null);
        mc.launchIntegratedServer(w.folder, w.name, null);
    }

    private ResourceLocation icon(W w) {
        if (w.icon != null || w.tried) return w.icon;
        w.tried = true;
        try {
            File f = new File(mc.mcDataDir, "saves" + File.separator + w.folder + File.separator + "icon.png");
            if (f.isFile()) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) w.icon = mc.getTextureManager().getDynamicTextureLocation("ya_world_" + Math.abs(w.folder.hashCode()), new DynamicTexture(img));
            }
        } catch (Throwable ignored) { }
        return w.icon;
    }

    private void drawIcon(W w, int x, int y, int s) {
        ResourceLocation rl = icon(w);
        if (rl != null) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(rl);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, s, s, (float) s, (float) s);
            return;
        }
        Gfx.rr(x, y, s, s, 4, 0x20FFFFFF);
        Gfx.textCentered(w.name.substring(0, 1).toUpperCase(), x + s / 2, y + (s - Gfx.lineHeight(s / 2, true)) / 2, Math.max(8, s / 2), true, Theme.MUTED);
    }

    // ---------- drawing ----------
    private boolean over(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private void button(int x, int y, int w, int h, String label, boolean primary, boolean hover) {
        Gfx.rr(x, y, w, h, 6, primary ? (hover ? 0xFFFFFFFF : 0xF0F4F4F5) : (hover ? 0xC0303036 : 0x90202026));
        if (!primary) Gfx.rrOutline(x, y, w, h, 6, 1, hover ? 0x66FFFFFF : 0x2EFFFFFF);
        Gfx.textCentered(label, x + w / 2, y + (h - Gfx.lineHeight(8, true)) / 2, 8, true, primary ? 0xFF101014 : Theme.TEXT);
    }

    private String[] tabNames() { return new String[]{Lang.t("srv.all"), Lang.t("wld.survival"), Lang.t("wld.creative"), Lang.t("wld.hardcore")}; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        gs = Math.max(1, new ScaledResolution(mc).getScaleFactor());
        float fadeIn = Ui.ease((System.currentTimeMillis() - opened) / 350f);
        MenuBg.draw(width, height, (System.currentTimeMillis() - opened) / 1000f);
        Gfx.setScale(gs);
        Gfx.fade = fadeIn;

        int pad = 14;
        sideW = Math.min(118, Math.max(96, width / 4));
        mainX = pad; sideX = width - pad - sideW; mainW = sideX - 10 - mainX;
        hdrY = pad; hdrH = 26;
        int tabsY = hdrY + hdrH + 6;
        listY = tabsY + 18; listH = height - listY - pad;
        boolean blocked = dlg != 0;
        int hx = blocked ? -1 : mx, hy = blocked ? -1 : my;

        Gfx.rr(mainX, hdrY, mainW, hdrH, 8, 0xB0121216);
        Gfx.rrOutline(mainX, hdrY, mainW, hdrH, 8, 1, 0x2EFFFFFF);
        boolean backOver = over(hx, hy, mainX + 4, hdrY + 4, 18, 18);
        Gfx.rr(mainX + 4, hdrY + 4, 18, 18, 5, backOver ? 0x40FFFFFF : 0x18FFFFFF);
        Gfx.textCentered("<", mainX + 13, hdrY + 4 + (18 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
        int tx = mainX + 28;
        if (mainW >= 300) { Gfx.text(Lang.t("wld.title"), tx, hdrY + (hdrH - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.TEXT); tx += Gfx.width(Lang.t("wld.title"), 11, true) + 8; }
        addW = Gfx.width(Lang.t("wld.create"), 8, true) + 22;
        addX = mainX + mainW - 4 - addW;
        search.x = tx; search.y = hdrY + 4; search.w = Math.max(40, addX - 6 - tx); search.h = 18;
        search.draw(Lang.t("wld.search"), 8);
        button(addX, hdrY + 4, addW, 18, "+ " + Lang.t("wld.create"), true, over(hx, hy, addX, hdrY + 4, addW, 18));

        String[] tabs = tabNames();
        int x = mainX + 4;
        for (int i = 0; i < tabs.length; i++) {
            int w = Gfx.width(tabs[i], 8, true) + 12;
            Gfx.text(tabs[i], x + 6, tabsY + 2, 8, true, i == tab ? MenuBg.INK : MenuBg.INK2);
            if (i == tab) Gfx.rr(x + 4, tabsY + 13, w - 8, 1, 0, MenuBg.INK);
            x += w + 4;
        }

        Gfx.rr(mainX, listY, mainW, listH, 8, 0xA0101014);
        Gfx.rrOutline(mainX, listY, mainW, listH, 8, 1, 0x24FFFFFF);
        List<W> rows = rows();
        float maxScroll = Math.max(0, rows.size() * (ROW + 4) + 4 - listH);
        scrollTarget = Ui.clamp(scrollTarget, 0f, maxScroll);
        scroll = Ui.lerp(scroll, scrollTarget, 0.3f);
        if (rows.isEmpty()) {
            Gfx.textCentered(Lang.t("wld.none"), mainX + mainW / 2, listY + listH / 2 - 12, 10, true, Theme.TEXT);
            Gfx.textCentered(Lang.t("wld.none_d"), mainX + mainW / 2, listY + listH / 2 + 2, 8, false, Theme.MUTED);
        } else {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(mainX * gs, mc.displayHeight - (listY + listH) * gs, mainW * gs, listH * gs);
            for (int i = 0; i < rows.size(); i++) {
                int ry = listY + 4 + i * (ROW + 4) - Math.round(scroll);
                if (ry + ROW < listY || ry > listY + listH) continue;
                drawRow(rows.get(i), mainX + 4, ry, mainW - 8, hx, hy);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        drawSide(hx, hy);
        if (dlg != 0) drawDialog(mx, my);
        Gfx.fade = 1f;
    }

    private void drawRow(W w, int x, int y, int rw, int mx, int my) {
        boolean sel = w == selected, hov = over(mx, my, x, y, rw, ROW) && my >= listY && my < listY + listH;
        Gfx.rr(x, y, rw, ROW, 7, sel ? 0x38FFFFFF : (hov ? 0x24FFFFFF : 0x14FFFFFF));
        if (sel) Gfx.rrOutline(x, y, rw, ROW, 7, 1, 0xCCFFFFFF);
        drawIcon(w, x + 4, y + 4, 22);
        int right = x + rw - 6;
        String ms = modeName(w);
        Gfx.text(ms, right - Gfx.width(ms, 8, true), y + 5, 8, true, modeColor(w));
        String sz = size(w.size);
        Gfx.text(sz, right - Gfx.width(sz, 7, false), y + 17, 7, false, Theme.MUTED);
        int tw = rw - 40 - Math.max(Gfx.width(ms, 8, true), Gfx.width(sz, 7, false)) - 8;
        Gfx.text(Gfx.trim(w.name, 9, true, tw), x + 32, y + 4, 9, true, Theme.TEXT);
        Gfx.text(Gfx.trim(date(w.last), 7, false, tw), x + 32, y + 17, 7, false, Theme.MUTED);
    }

    private void drawSide(int mx, int my) {
        Gfx.rr(sideX, hdrY, sideW, height - 2 * hdrY, 8, 0xB0121216);
        Gfx.rrOutline(sideX, hdrY, sideW, height - 2 * hdrY, 8, 1, 0x2EFFFFFF);
        int x = sideX + 10, y = hdrY + 10, w = sideW - 20;
        if (selected == null) {
            Gfx.rr(x, y, 34, 34, 7, 0x18FFFFFF);
            Gfx.textCentered("?", x + 17, y + (34 - Gfx.lineHeight(14, true)) / 2, 14, true, Theme.DIM);
            Gfx.text(Lang.t("wld.none"), x + 40, y + 6, 9, true, Theme.TEXT);
            Gfx.text(Gfx.trim(Lang.t("wld.none_d"), 7, false, w - 40), x + 40, y + 19, 7, false, Theme.MUTED);
        } else {
            drawIcon(selected, x, y, 34);
            Gfx.text(Gfx.trim(selected.name, 10, true, w - 40), x + 40, y + 4, 10, true, Theme.TEXT);
            Gfx.text(Gfx.trim(selected.folder, 7, false, w - 40), x + 40, y + 17, 7, false, Theme.MUTED);
            Gfx.text(modeName(selected), x + 40, y + 27, 7, true, modeColor(selected));
            int ly = y + 44;
            Gfx.text(Lang.t("wld.played") + ": " + date(selected.last), x, ly, 7, false, Theme.TEXT); ly += 11;
            Gfx.text(Lang.t("wld.size") + ": " + size(selected.size), x, ly, 7, false, Theme.MUTED); ly += 11;
            Gfx.text(Lang.t("wld.cheats") + ": " + Lang.t(selected.cheats ? "wld.yes" : "wld.no"), x, ly, 7, false, Theme.MUTED);
        }
        int by = height - hdrY - 10 - 24;
        boolean can = selected != null;
        Gfx.rr(sideX + 10, by, w, 24, 7, can ? (over(mx, my, sideX + 10, by, w, 24) ? 0xFFFFFFFF : 0xF0F4F4F5) : 0x30FFFFFF);
        Gfx.textCentered(can ? Lang.t("srv.play") : Lang.t("wld.choose"), sideX + 10 + w / 2, by + (24 - Gfx.lineHeight(10, true)) / 2, 10, true, can ? 0xFF101014 : Theme.DIM);
        if (can) {
            int hw = (w - 4) / 2, ey = by - 22;
            button(sideX + 10, ey, hw, 18, Lang.t("wld.rename"), false, over(mx, my, sideX + 10, ey, hw, 18));
            button(sideX + 14 + hw, ey, hw, 18, Lang.t("srv.delete"), false, over(mx, my, sideX + 14 + hw, ey, hw, 18));
        }
        Gfx.text(all.size() + " " + Lang.t("wld.worlds"), sideX + 10, by - 34, 7, false, Theme.DIM);
    }

    private void drawDialog(int mx, int my) {
        drawGradientRect(0, 0, width, height, 0x90000000, 0x90000000);
        dw = Math.min(width - 20, 260); dh = dlg == 1 ? 96 : 84; dx = (width - dw) / 2; dy = (height - dh) / 2;
        Gfx.rr(dx, dy, dw, dh, 10, 0xF0141418);
        Gfx.rrOutline(dx, dy, dw, dh, 10, 1, 0x3CFFFFFF);
        Gfx.text(Lang.t(dlg == 1 ? "wld.rename" : "wld.delete_q"), dx + 12, dy + 10, 11, true, Theme.TEXT);
        if (dlg == 1) {
            fName.x = dx + 12; fName.y = dy + 30; fName.w = dw - 24; fName.h = 18; fName.draw(Lang.t("wld.name"), 8);
        } else if (selected != null) {
            Gfx.text(Gfx.trim(selected.name, 9, true, dw - 24), dx + 12, dy + 30, 9, true, Theme.TEXT);
            Gfx.text(Gfx.trim(Lang.t("wld.delete_d"), 7, false, dw - 24), dx + 12, dy + 43, 7, false, Theme.MUTED);
        }
        int by = dy + dh - 28, bw = 64;
        String ok = Lang.t(dlg == 1 ? "srv.save" : "srv.delete");
        button(dx + dw - 12 - bw, by, bw, 18, ok, true, over(mx, my, dx + dw - 12 - bw, by, bw, 18));
        button(dx + dw - 16 - 2 * bw, by, bw, 18, Lang.t("srv.cancel"), false, over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18));
    }

    private void submitDialog() {
        if (selected == null) { dlg = 0; return; }
        try {
            if (dlg == 1) {
                String n = fName.value.trim();
                if (n.isEmpty()) return;
                mc.getSaveLoader().renameWorld(selected.folder, n);
            } else {
                mc.getSaveLoader().flushCache();
                mc.getSaveLoader().deleteWorldDirectory(selected.folder);
            }
        } catch (Throwable ignored) { }
        dlg = 0;
        initGui();
    }

    // ---------- input ----------
    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (dlg != 0) {
            if (button != 0) return;
            fName.focus = dlg == 1 && fName.hit(mx, my);
            int by = dy + dh - 28, bw = 64;
            if (over(mx, my, dx + dw - 12 - bw, by, bw, 18)) submitDialog();
            else if (over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18) || !over(mx, my, dx, dy, dw, dh)) dlg = 0;
            return;
        }
        if (button != 0) return;
        search.focus = search.hit(mx, my);
        if (over(mx, my, mainX + 4, hdrY + 4, 18, 18)) { mc.displayGuiScreen(parent); return; }
        if (over(mx, my, addX, hdrY + 4, addW, 18)) { mc.displayGuiScreen(new GuiCreateWorld(this)); return; }
        int tx = mainX + 4;
        String[] tabs = tabNames();
        for (int i = 0; i < tabs.length; i++) {
            int w = Gfx.width(tabs[i], 8, true) + 12;
            if (over(mx, my, tx, hdrY + hdrH + 4, w, 16)) { tab = i; scrollTarget = 0; scroll = 0; return; }
            tx += w + 4;
        }
        int w = sideW - 20, by = height - hdrY - 10 - 24;
        if (selected != null && over(mx, my, sideX + 10, by, w, 24)) { play(selected); return; }
        if (selected != null) {
            int hw = (w - 4) / 2, ey = by - 22;
            if (over(mx, my, sideX + 10, ey, hw, 18)) { dlg = 1; fName.value = selected.name; fName.focus = true; return; }
            if (over(mx, my, sideX + 14 + hw, ey, hw, 18)) { dlg = 2; return; }
        }
        if (over(mx, my, mainX, listY, mainW, listH)) {
            List<W> rows = rows();
            for (int i = 0; i < rows.size(); i++) {
                int ry = listY + 4 + i * (ROW + 4) - Math.round(scroll);
                if (over(mx, my, mainX + 4, ry, mainW - 8, ROW)) {
                    W sw = rows.get(i);
                    long now = System.currentTimeMillis();
                    if (sw == lastClickRow && now - lastClick < 400) { play(sw); return; }
                    selected = sw; lastClickRow = sw; lastClick = now;
                    return;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (dlg != 0) {
            if (key == 1) { dlg = 0; return; }
            if (key == 28 || key == 156) { submitDialog(); return; }
            if (dlg == 1) fName.type(typed, key);
            return;
        }
        if (key == 1) { mc.displayGuiScreen(parent); return; }
        if ((key == 28 || key == 156) && !search.focus) { play(selected); return; }
        if (search.focus) { search.type(typed, key); scrollTarget = 0; }
    }

    @Override
    public void handleMouseInput() {
        try { super.handleMouseInput(); } catch (Exception ignored) { }
        int d = Mouse.getEventDWheel();
        if (d != 0 && dlg == 0) scrollTarget -= Math.signum((float) d) * (ROW + 4);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

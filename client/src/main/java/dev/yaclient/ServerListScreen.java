package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The server list: search, saved and recent tabs, live ping and icons, a side panel with the chosen server,
 * and an add dialog with a live preview of the server. Replaces the vanilla multiplayer screen.
 */
public class ServerListScreen extends GuiScreen {
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

    private final GuiScreen parent;
    private final ServerList saved;
    private final List<ServerData> recent = new ArrayList<ServerData>();
    private final Map<ServerData, Boolean> pinging = new HashMap<ServerData, Boolean>();
    private final Map<String, ResourceLocation> icons = new HashMap<String, ResourceLocation>();
    private final Map<String, String> iconSrc = new HashMap<String, String>();
    private final Field search = new Field(40);
    private final long opened = System.currentTimeMillis();
    private int tab;               // 0 = all, 1 = recent
    private ServerData selected;
    private float scroll, scrollTarget;
    private long lastClick; private ServerData lastClickRow;
    private int gs = 2;

    // layout (recomputed every frame)
    private int mainX, mainW, sideX, sideW, listY, listH, hdrY, hdrH;
    private int addX, addW, dirX, dirW;

    // dialog: 0 none, 1 add, 2 direct connect, 3 edit
    private int dlg;
    private final Field fName = new Field(32), fAddr = new Field(80);
    private ServerData editing;
    private volatile ServerData prev;
    private long prevAt; private String prevFor = "";
    private volatile boolean prevDone;

    public ServerListScreen(GuiScreen parent) {
        this.parent = parent;
        this.saved = new ServerList(Minecraft.getMinecraft());
        this.saved.loadServerList();
        for (String ip : RecentServers.recent()) {
            ServerData found = null;
            for (int i = 0; i < saved.countServers(); i++) if (ip.equals(saved.getServerData(i).serverIP)) found = saved.getServerData(i);
            recent.add(found != null ? found : new ServerData(ip, ip, false));
        }
        if (saved.countServers() > 0) selected = saved.getServerData(0);
    }

    @Override
    public void initGui() {
        for (int i = 0; i < saved.countServers(); i++) ping(saved.getServerData(i));
        for (ServerData sd : recent) ping(sd);
    }

    // ---------- pinging ----------
    private void ping(final ServerData sd) {
        if (pinging.containsKey(sd)) return;
        pinging.put(sd, Boolean.TRUE);
        Thread t = new Thread(new Runnable() {
            public void run() { pingNow(sd); }
        }, "YA-ServerList-Ping");
        t.setDaemon(true);
        t.start();
    }

    private static void pingNow(ServerData sd) {
        try {
            sd.pingToServer = 0L;
            OldServerPinger p = new OldServerPinger();
            p.ping(sd);
            for (int i = 0; i < 60 && sd.pingToServer <= 0L; i++) { p.pingPendingNetworks(); Thread.sleep(100); }
            if (sd.pingToServer <= 0L) sd.pingToServer = -1L;
        } catch (Throwable e) { sd.pingToServer = -1L; }
    }

    private static boolean online(ServerData sd) { return sd.pingToServer > 0L; }

    private static String clean(String s) { return s == null ? "" : EnumChatFormatting.getTextWithoutFormattingCodes(s); }

    private ResourceLocation icon(ServerData sd) {
        String data = sd.getBase64EncodedIconData();
        if (data == null || data.isEmpty()) return null;
        String key = sd.serverIP + "|" + data.length();
        ResourceLocation rl = icons.get(key);
        if (rl != null) return rl;
        if (icons.containsKey(key)) return null;
        icons.put(key, null);
        try {
            byte[] b = java.util.Base64.getDecoder().decode(data.replace("\n", ""));
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(b));
            if (img != null) {
                rl = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("ya_srv_" + Math.abs(key.hashCode()), new DynamicTexture(img));
                icons.put(key, rl);
            }
        } catch (Throwable ignored) { }
        return rl;
    }

    private void drawIcon(ServerData sd, int x, int y, int s) {
        ResourceLocation rl = icon(sd);
        if (rl != null) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(rl);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, s, s, (float) s, (float) s);
            return;
        }
        Gfx.rr(x, y, s, s, 4, 0x20FFFFFF);
        String n = sd.serverName == null || sd.serverName.isEmpty() ? "?" : sd.serverName.substring(0, 1).toUpperCase();
        Gfx.textCentered(n, x + s / 2, y + (s - Gfx.lineHeight(s / 2, true)) / 2, Math.max(8, s / 2), true, Theme.MUTED);
    }

    private static int pingColor(long p) { return p <= 80 ? 0xFF34D27B : (p <= 160 ? 0xFFFFC21A : 0xFFEF4444); }

    // ---------- data ----------
    private List<ServerData> rows() {
        List<ServerData> src = new ArrayList<ServerData>();
        if (tab == 1) src.addAll(recent);
        else for (int i = 0; i < saved.countServers(); i++) src.add(saved.getServerData(i));
        String q = search.value.trim().toLowerCase();
        if (q.isEmpty()) return src;
        List<ServerData> out = new ArrayList<ServerData>();
        for (ServerData sd : src) if ((sd.serverName + " " + sd.serverIP).toLowerCase().contains(q)) out.add(sd);
        return out;
    }

    private boolean isSaved(ServerData sd) {
        for (int i = 0; i < saved.countServers(); i++) if (saved.getServerData(i) == sd) return true;
        return false;
    }

    private void join(ServerData sd) {
        if (sd == null) return;
        mc.displayGuiScreen(new YAConnecting(this, mc, new ServerData(sd.serverName, sd.serverIP, false)));
    }

    // ---------- drawing ----------
    private boolean over(int mx, int my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private void button(int x, int y, int w, int h, String label, boolean primary, boolean hover) {
        Gfx.rr(x, y, w, h, 6, primary ? (hover ? 0xFFFFFFFF : 0xF0F4F4F5) : (hover ? 0xC0303036 : 0x90202026));
        if (!primary) Gfx.rrOutline(x, y, w, h, 6, 1, hover ? 0x66FFFFFF : 0x2EFFFFFF);
        Gfx.textCentered(label, x + w / 2, y + (h - Gfx.lineHeight(8, true)) / 2, 8, true, primary ? 0xFF101014 : Theme.TEXT);
    }

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

        // header bar
        Gfx.rr(mainX, hdrY, mainW, hdrH, 8, 0xB0121216);
        Gfx.rrOutline(mainX, hdrY, mainW, hdrH, 8, 1, 0x2EFFFFFF);
        boolean backOver = over(hx, hy, mainX + 4, hdrY + 4, 18, 18);
        Gfx.rr(mainX + 4, hdrY + 4, 18, 18, 5, backOver ? 0x40FFFFFF : 0x18FFFFFF);
        Gfx.textCentered("<", mainX + 13, hdrY + 4 + (18 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
        int tx = mainX + 28;
        if (mainW >= 300) { Gfx.text(Lang.t("srv.title"), tx, hdrY + (hdrH - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.TEXT); tx += Gfx.width(Lang.t("srv.title"), 11, true) + 8; }
        dirW = Gfx.width(Lang.t("srv.direct"), 8, true) + 14; addW = Gfx.width(Lang.t("srv.add"), 8, true) + 14;
        dirX = mainX + mainW - 4 - dirW; addX = dirX - 4 - addW;
        search.x = tx; search.y = hdrY + 4; search.w = Math.max(40, addX - 6 - tx); search.h = 18;
        search.draw(Lang.t("srv.search"), 8);
        button(addX, hdrY + 4, addW, 18, "+ " + Lang.t("srv.add"), true, over(hx, hy, addX, hdrY + 4, addW, 18));
        button(dirX, hdrY + 4, dirW, 18, Lang.t("srv.direct"), false, over(hx, hy, dirX, hdrY + 4, dirW, 18));

        // tabs
        String[] tabs = {Lang.t("srv.all"), Lang.t("srv.recent")};
        int x = mainX + 4;
        for (int i = 0; i < 2; i++) {
            int w = Gfx.width(tabs[i], 8, true) + 12;
            Gfx.text(tabs[i], x + 6, tabsY + 2, 8, true, i == tab ? MenuBg.INK : MenuBg.INK2);
            if (i == tab) Gfx.rr(x + 4, tabsY + 13, w - 8, 1, 0, MenuBg.INK);
            x += w + 4;
        }

        // list card
        Gfx.rr(mainX, listY, mainW, listH, 8, 0xA0101014);
        Gfx.rrOutline(mainX, listY, mainW, listH, 8, 1, 0x24FFFFFF);
        List<ServerData> rows = rows();
        float maxScroll = Math.max(0, rows.size() * (ROW + 4) + 4 - listH);
        scrollTarget = Ui.clamp(scrollTarget, 0f, maxScroll);
        scroll = Ui.lerp(scroll, scrollTarget, 0.3f);
        if (rows.isEmpty()) {
            String a = Lang.t(tab == 1 ? "srv.none_recent" : "srv.none"), b = Lang.t("srv.none_d");
            Gfx.textCentered(a, mainX + mainW / 2, listY + listH / 2 - 12, 10, true, Theme.TEXT);
            Gfx.textCentered(b, mainX + mainW / 2, listY + listH / 2 + 2, 8, false, Theme.MUTED);
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

    private void drawRow(ServerData sd, int x, int y, int w, int mx, int my) {
        boolean sel = sd == selected, hov = over(mx, my, x, y, w, ROW) && my >= listY && my < listY + listH;
        Gfx.rr(x, y, w, ROW, 7, sel ? 0x38FFFFFF : (hov ? 0x24FFFFFF : 0x14FFFFFF));
        if (sel) Gfx.rrOutline(x, y, w, ROW, 7, 1, 0xCCFFFFFF);
        drawIcon(sd, x + 4, y + 4, 22);
        int right = x + w - 6;
        String ps;
        int pc;
        if (sd.pingToServer > 0) { ps = sd.pingToServer + "ms"; pc = pingColor(sd.pingToServer); }
        else if (sd.pingToServer < 0) { ps = Lang.t("srv.offline"); pc = 0xFFEF4444; }
        else { ps = "..."; pc = Theme.DIM; }
        Gfx.text(ps, right - Gfx.width(ps, 8, true), y + 5, 8, true, pc);
        String pop = clean(sd.populationInfo);
        if (!pop.isEmpty() && sd.pingToServer > 0) Gfx.text(pop, right - Gfx.width(pop, 7, false), y + 17, 7, false, Theme.MUTED);
        int tw = w - 40 - Math.max(Gfx.width(ps, 8, true), Gfx.width(pop, 7, false)) - 8;
        Gfx.text(Gfx.trim(sd.serverName == null || sd.serverName.isEmpty() ? sd.serverIP : sd.serverName, 9, true, tw), x + 32, y + 4, 9, true, Theme.TEXT);
        String motd = clean(sd.serverMOTD).replace("\n", " ");
        Gfx.text(Gfx.trim(sd.pingToServer > 0 && !motd.isEmpty() ? motd : sd.serverIP, 7, false, tw), x + 32, y + 17, 7, false, Theme.MUTED);
    }

    private void drawSide(int mx, int my) {
        Gfx.rr(sideX, hdrY, sideW, height - 2 * hdrY, 8, 0xB0121216);
        Gfx.rrOutline(sideX, hdrY, sideW, height - 2 * hdrY, 8, 1, 0x2EFFFFFF);
        int x = sideX + 10, y = hdrY + 10, w = sideW - 20;
        if (selected == null) {
            Gfx.rr(x, y, 34, 34, 7, 0x18FFFFFF);
            Gfx.textCentered("?", x + 17, y + (34 - Gfx.lineHeight(14, true)) / 2, 14, true, Theme.DIM);
            Gfx.text(Lang.t("srv.none"), x + 40, y + 6, 9, true, Theme.TEXT);
            Gfx.text(Gfx.trim(Lang.t("srv.none_d"), 7, false, w - 40), x + 40, y + 19, 7, false, Theme.MUTED);
        } else {
            drawIcon(selected, x, y, 34);
            Gfx.text(Gfx.trim(selected.serverName == null || selected.serverName.isEmpty() ? selected.serverIP : selected.serverName, 10, true, w - 40), x + 40, y + 4, 10, true, Theme.TEXT);
            Gfx.text(Gfx.trim(selected.serverIP, 7, false, w - 40), x + 40, y + 17, 7, false, Theme.MUTED);
            String st = selected.pingToServer > 0 ? selected.pingToServer + "ms" : (selected.pingToServer < 0 ? Lang.t("srv.offline") : "...");
            Gfx.text(st, x + 40, y + 27, 7, true, selected.pingToServer > 0 ? pingColor(selected.pingToServer) : Theme.DIM);
            String ver = clean(selected.gameVersion);
            int ly = y + 44;
            if (selected.pingToServer > 0) {
                String pop = clean(selected.populationInfo);
                if (!pop.isEmpty()) { Gfx.text(Lang.t("srv.online") + ": " + pop, x, ly, 8, false, Theme.TEXT); ly += 11; }
                if (!ver.isEmpty()) { Gfx.text(Gfx.trim(ver, 7, false, w), x, ly, 7, false, Theme.MUTED); ly += 11; }
                String[] lines = clean(selected.serverMOTD).split("\n");
                for (int i = 0; i < lines.length && i < 2; i++) { Gfx.text(Gfx.trim(lines[i].trim(), 7, false, w), x, ly, 7, false, Theme.MUTED); ly += 10; }
            }
        }
        int by = height - hdrY - 10 - 24;
        boolean can = selected != null;
        Gfx.rr(sideX + 10, by, w, 24, 7, can ? (over(mx, my, sideX + 10, by, w, 24) ? 0xFFFFFFFF : 0xF0F4F4F5) : 0x30FFFFFF);
        Gfx.textCentered(can ? Lang.t("srv.play") : Lang.t("srv.choose"), sideX + 10 + w / 2, by + (24 - Gfx.lineHeight(10, true)) / 2, 10, true, can ? 0xFF101014 : Theme.DIM);
        if (can && isSaved(selected)) {
            int hw = (w - 4) / 2, ey = by - 22;
            button(sideX + 10, ey, hw, 18, Lang.t("srv.edit"), false, over(mx, my, sideX + 10, ey, hw, 18));
            button(sideX + 14 + hw, ey, hw, 18, Lang.t("srv.delete"), false, over(mx, my, sideX + 14 + hw, ey, hw, 18));
        }
        String cnt = saved.countServers() + " " + Lang.t("srv.saved");
        Gfx.text(cnt, sideX + 10, by - 34, 7, false, Theme.DIM);
    }

    // ---------- dialog ----------
    private void openDialog(int mode, ServerData edit) {
        dlg = mode; editing = edit; prev = null; prevFor = ""; prevDone = false;
        fName.value = edit != null ? (edit.serverName == null ? "" : edit.serverName) : "";
        fAddr.value = edit != null ? edit.serverIP : "";
        fName.focus = false; fAddr.focus = true;
        if (edit != null) startPreview();
    }

    private void startPreview() {
        final String ip = fAddr.value.trim();
        prevFor = ip; prevAt = 0;
        if (ip.isEmpty()) { prev = null; return; }
        final ServerData sd = new ServerData("", ip, false);
        prev = sd; prevDone = false;
        Thread t = new Thread(new Runnable() {
            public void run() { pingNow(sd); if (sd == prev) prevDone = true; }
        }, "YA-ServerList-Preview");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void updateScreen() {
        if (dlg != 0 && !fAddr.value.trim().equals(prevFor)) {
            if (prevAt == 0) prevAt = System.currentTimeMillis();
            else if (System.currentTimeMillis() - prevAt > 500) startPreview();
        }
    }

    private int dx, dy, dw, dh;

    private void drawDialog(int mx, int my) {
        drawGradientRect(0, 0, width, height, 0x90000000, 0x90000000);
        dw = Math.min(width - 20, 300); dh = 134; dx = (width - dw) / 2; dy = (height - dh) / 2;
        Gfx.rr(dx, dy, dw, dh, 10, 0xF0141418);
        Gfx.rrOutline(dx, dy, dw, dh, 10, 1, 0x3CFFFFFF);
        String title = Lang.t(dlg == 2 ? "srv.direct" : (dlg == 3 ? "srv.edit" : "srv.add"));
        Gfx.text(title, dx + 12, dy + 10, 11, true, Theme.TEXT);
        boolean two = dw >= 260 && dlg != 2;
        int fw = two ? (dw - 36) / 2 : dw - 24;
        int fy = dy + 30;
        if (dlg != 2) {
            Gfx.text(Lang.t("srv.name"), dx + 12, fy, 8, true, Theme.MUTED);
            fName.x = dx + 12; fName.y = fy + 11; fName.w = fw; fName.h = 18; fName.draw(Lang.t("srv.default_name"), 8);
            fy += 36;
        }
        Gfx.text(Lang.t("srv.address"), dx + 12, fy, 8, true, Theme.MUTED);
        fAddr.x = dx + 12; fAddr.y = fy + 11; fAddr.w = fw; fAddr.h = 18; fAddr.draw("play.example.net", 8);
        if (two) drawPreview(dx + 24 + fw, dy + 30, fw, dh - 30 - 40);
        else if (dlg == 2) { }
        int by = dy + dh - 28;
        int bw = 64;
        String ok = Lang.t(dlg == 2 ? "srv.connect" : "srv.save");
        button(dx + dw - 12 - bw, by, bw, 18, ok, true, over(mx, my, dx + dw - 12 - bw, by, bw, 18));
        button(dx + dw - 16 - 2 * bw, by, bw, 18, Lang.t("srv.cancel"), false, over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18));
    }

    private void drawPreview(int x, int y, int w, int h) {
        Gfx.rr(x, y, w, h, 7, 0x16FFFFFF);
        Gfx.rrOutline(x, y, w, h, 7, 1, 0x30FFFFFF);
        ServerData p = prev;
        String badge = p == null ? "" : (p.pingToServer > 0 ? Lang.t("srv.online").toUpperCase() : (p.pingToServer < 0 ? Lang.t("srv.offline").toUpperCase() : "..."));
        if (!badge.isEmpty()) {
            int bw = Gfx.width(badge, 6, true) + 8;
            Gfx.rrOutline(x + w - bw - 5, y + 5, bw, 11, 5, 1, 0x80FFFFFF);
            Gfx.text(badge, x + w - bw - 1, y + 7, 6, true, p != null && p.pingToServer < 0 ? 0xFFEF4444 : Theme.TEXT);
        }
        ServerData face = p != null ? p : new ServerData("", "", false);
        drawIcon(face, x + 6, y + 8, 26);
        String nm = fName.value.trim().isEmpty() ? (fAddr.value.trim().isEmpty() ? Lang.t("srv.default_name") : fAddr.value.trim()) : fName.value.trim();
        Gfx.text(Gfx.trim(nm, 9, true, w - 12), x + 6, y + 38, 9, true, Theme.TEXT);
        if (p == null) { Gfx.text(Gfx.trim(Lang.t("srv.hint"), 7, false, w - 12), x + 6, y + 51, 7, false, Theme.DIM); return; }
        if (p.pingToServer > 0) {
            String info = clean(p.populationInfo) + " " + Lang.t("srv.online") + " | " + p.pingToServer + "ms";
            Gfx.text(Gfx.trim(info, 7, false, w - 12), x + 6, y + 51, 7, false, Theme.MUTED);
            String[] lines = clean(p.serverMOTD).split("\n");
            for (int i = 0; i < lines.length && i < 2; i++) Gfx.text(Gfx.trim(lines[i].trim(), 7, false, w - 12), x + 6, y + 64 + i * 10, 7, false, Theme.TEXT);
        }
    }

    private void submitDialog() {
        String ip = fAddr.value.trim();
        if (ip.isEmpty()) return;
        String name = fName.value.trim().isEmpty() ? ip : fName.value.trim();
        if (dlg == 2) { ServerData sd = new ServerData(ip, ip, false); dlg = 0; join(sd); return; }
        if (dlg == 3 && editing != null) { editing.serverName = name; if (!ip.equals(editing.serverIP)) { editing.serverIP = ip; pinging.remove(editing); ping(editing); } }
        else { ServerData sd = new ServerData(name, ip, false); saved.addServerData(sd); selected = sd; ping(sd); }
        saved.saveServerList();
        dlg = 0;
    }

    // ---------- input ----------
    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (dlg != 0) {
            if (button != 0) return;
            fName.focus = dlg != 2 && fName.hit(mx, my);
            fAddr.focus = fAddr.hit(mx, my);
            int by = dy + dh - 28, bw = 64;
            if (over(mx, my, dx + dw - 12 - bw, by, bw, 18)) submitDialog();
            else if (over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18) || !over(mx, my, dx, dy, dw, dh)) dlg = 0;
            return;
        }
        if (button != 0) return;
        search.focus = search.hit(mx, my);
        if (over(mx, my, mainX + 4, hdrY + 4, 18, 18)) { mc.displayGuiScreen(parent); return; }
        if (over(mx, my, addX, hdrY + 4, addW, 18)) { openDialog(1, null); return; }
        if (over(mx, my, dirX, hdrY + 4, dirW, 18)) { openDialog(2, null); return; }
        int tx = mainX + 4;
        String[] tabs = {Lang.t("srv.all"), Lang.t("srv.recent")};
        for (int i = 0; i < 2; i++) {
            int w = Gfx.width(tabs[i], 8, true) + 12;
            if (over(mx, my, tx, hdrY + hdrH + 4, w, 16)) { tab = i; scrollTarget = 0; scroll = 0; return; }
            tx += w + 4;
        }
        // side panel
        int w = sideW - 20, by = height - hdrY - 10 - 24;
        if (selected != null && over(mx, my, sideX + 10, by, w, 24)) { join(selected); return; }
        if (selected != null && isSaved(selected)) {
            int hw = (w - 4) / 2, ey = by - 22;
            if (over(mx, my, sideX + 10, ey, hw, 18)) { openDialog(3, selected); return; }
            if (over(mx, my, sideX + 14 + hw, ey, hw, 18)) {
                for (int i = 0; i < saved.countServers(); i++) if (saved.getServerData(i) == selected) { saved.removeServerData(i); break; }
                saved.saveServerList();
                selected = saved.countServers() > 0 ? saved.getServerData(0) : null;
                return;
            }
        }
        // rows
        if (over(mx, my, mainX, listY, mainW, listH)) {
            List<ServerData> rows = rows();
            for (int i = 0; i < rows.size(); i++) {
                int ry = listY + 4 + i * (ROW + 4) - Math.round(scroll);
                if (over(mx, my, mainX + 4, ry, mainW - 8, ROW)) {
                    ServerData sd = rows.get(i);
                    long now = System.currentTimeMillis();
                    if (sd == lastClickRow && now - lastClick < 400) { join(sd); return; }
                    selected = sd; lastClickRow = sd; lastClick = now;
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
            if (key == 15) { if (dlg != 2) { boolean f = fName.focus; fName.focus = !f; fAddr.focus = f; } return; }
            if (fName.focus) fName.type(typed, key); else if (fAddr.focus) fAddr.type(typed, key);
            return;
        }
        if (key == 1) { mc.displayGuiScreen(parent); return; }
        if ((key == 28 || key == 156) && !search.focus) { join(selected); return; }
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

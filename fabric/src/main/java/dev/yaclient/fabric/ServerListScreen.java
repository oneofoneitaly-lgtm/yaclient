package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The server list, same as the 1.8.9 one: search, saved and recent tabs, live ping and icons, a side panel with the
 * chosen server, and an add / edit / direct connect dialog with a live preview. Replaces the vanilla multiplayer screen.
 */
public class ServerListScreen extends Screen {
    private static final int ROW = 30;

    private final Screen parent;
    private final ServerList saved;
    private final ServerStatusPinger pinger = new ServerStatusPinger();
    private final List<ServerData> recent = new ArrayList<ServerData>();
    private final List<ServerData> pinged = new ArrayList<ServerData>();
    private final Field search = new Field(40);
    private final long opened = System.currentTimeMillis();
    private int tab;               // 0 = all, 1 = recent
    private ServerData selected;
    private float scroll, scrollTarget;
    private long lastClick;
    private ServerData lastClickRow;

    // layout (recomputed every frame)
    private int mainX, mainW, sideX, sideW, listY, listH, hdrY, hdrH, addX, addW, dirX, dirW;

    // dialog: 0 none, 1 add, 2 direct connect, 3 edit
    private int dlg;
    private final Field fName = new Field(32), fAddr = new Field(80);
    private ServerData editing, prev;
    private String prevFor = "";
    private long prevAt;
    private int dx, dy, dw, dh;

    public ServerListScreen(Screen parent) {
        super(Component.literal("Multiplayer"));
        this.parent = parent;
        this.saved = new ServerList(Minecraft.getInstance());
        this.saved.load();
        for (String ip : Servers.recent()) {
            ServerData found = saved.get(ip);
            recent.add(found != null ? found : new ServerData(ip, ip, ServerData.Type.OTHER));
        }
        if (saved.size() > 0) selected = saved.get(0);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) { }

    @Override
    protected void init() {
        for (int i = 0; i < saved.size(); i++) ping(saved.get(i));
        for (ServerData sd : recent) ping(sd);
    }

    private void ping(ServerData sd) {
        if (pinged.contains(sd)) return;
        pinged.add(sd);
        Servers.ping(pinger, sd, () -> { if (isSaved(sd)) saved.save(); });
    }

    @Override
    public void tick() {
        pinger.tick();
        if (dlg != 0 && !fAddr.value.trim().equals(prevFor)) {
            if (prevAt == 0) prevAt = System.currentTimeMillis();
            else if (System.currentTimeMillis() - prevAt > 500) startPreview();
        }
    }

    @Override
    public void removed() { pinger.removeAll(); }

    @Override
    public void onClose() { Minecraft.getInstance().setScreenAndShow(parent); }

    // ---------- data ----------
    private List<ServerData> rows() {
        List<ServerData> src = new ArrayList<ServerData>();
        if (tab == 1) src.addAll(recent);
        else for (int i = 0; i < saved.size(); i++) src.add(saved.get(i));
        String q = search.value.trim().toLowerCase();
        if (q.isEmpty()) return src;
        List<ServerData> out = new ArrayList<ServerData>();
        for (ServerData sd : src) if ((sd.name + " " + sd.ip).toLowerCase().contains(q)) out.add(sd);
        return out;
    }

    private boolean isSaved(ServerData sd) {
        for (int i = 0; i < saved.size(); i++) if (saved.get(i) == sd) return true;
        return false;
    }

    private void join(ServerData sd) {
        if (sd == null) return;
        Servers.join(this, sd);
    }

    // ---------- drawing ----------
    private static boolean over(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private static void button(int x, int y, int w, int h, String label, boolean primary, boolean hover) {
        Gfx.rr(x, y, w, h, 6, primary ? (hover ? 0xFFFFFFFF : 0xF0F4F4F5) : (hover ? 0xC0303036 : 0x90202026));
        if (!primary) Gfx.rrOutline(x, y, w, h, 6, 1, hover ? 0x66FFFFFF : 0x2EFFFFFF);
        Gfx.textCentered(label, x + w / 2, y + (h - Gfx.lineHeight(8, true)) / 2, 8, true, primary ? 0xFF101014 : Theme.TEXT);
    }

    private void layout() {
        int pad = 14;
        sideW = Math.min(118, Math.max(96, width / 4));
        mainX = pad; sideX = width - pad - sideW; mainW = sideX - 10 - mainX;
        hdrY = pad; hdrH = 26;
        listY = hdrY + hdrH + 6 + 18; listH = height - listY - pad;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft mc = Minecraft.getInstance();
        float fadeIn = Ui.ease((System.currentTimeMillis() - opened) / 350f);
        MenuBg.draw(g, width, height, (System.currentTimeMillis() - opened) / 1000f);
        Gfx.g = g;
        Gfx.setScale(Math.max(1, mc.getWindow().getGuiScale()));
        Gfx.fade = fadeIn;
        layout();
        int tabsY = hdrY + hdrH + 6;
        boolean blocked = dlg != 0;
        int hx = blocked ? -1 : mx, hy = blocked ? -1 : my;

        // header bar
        Gfx.rr(mainX, hdrY, mainW, hdrH, 8, 0xB0121216);
        Gfx.rrOutline(mainX, hdrY, mainW, hdrH, 8, 1, 0x2EFFFFFF);
        Gfx.rr(mainX + 4, hdrY + 4, 18, 18, 5, over(hx, hy, mainX + 4, hdrY + 4, 18, 18) ? 0x40FFFFFF : 0x18FFFFFF);
        Gfx.textCentered("<", mainX + 13, hdrY + 4 + (18 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
        int tx = mainX + 28;
        if (mainW >= 300) { Gfx.text("Servers", tx, hdrY + (hdrH - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.TEXT); tx += Gfx.width("Servers", 11, true) + 8; }
        dirW = Gfx.width("Direct", 8, true) + 14; addW = Gfx.width("+ Add", 8, true) + 14;
        dirX = mainX + mainW - 4 - dirW; addX = dirX - 4 - addW;
        search.x = tx; search.y = hdrY + 4; search.w = Math.max(40, addX - 6 - tx); search.h = 18;
        search.draw("Search servers...", 8);
        button(addX, hdrY + 4, addW, 18, "+ Add", true, over(hx, hy, addX, hdrY + 4, addW, 18));
        button(dirX, hdrY + 4, dirW, 18, "Direct", false, over(hx, hy, dirX, hdrY + 4, dirW, 18));

        // tabs
        String[] tabs = {"All", "Recent"};
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
            Gfx.textCentered(tab == 1 ? "No recent servers" : "No servers yet", mainX + mainW / 2, listY + listH / 2 - 12, 10, true, Theme.TEXT);
            Gfx.textCentered("Add one with the + Add button", mainX + mainW / 2, listY + listH / 2 + 2, 8, false, Theme.MUTED);
        } else {
            g.enableScissor(mainX, listY + 1, mainX + mainW, listY + listH - 1);
            for (int i = 0; i < rows.size(); i++) {
                int ry = listY + 4 + i * (ROW + 4) - Math.round(scroll);
                if (ry + ROW < listY || ry > listY + listH) continue;
                drawRow(rows.get(i), mainX + 4, ry, mainW - 8, hx, hy);
            }
            g.disableScissor();
        }

        drawSide(hx, hy);
        if (dlg != 0) drawDialog(g, mx, my);
        Gfx.fade = 1f;
    }

    private String status(ServerData sd) {
        if (Servers.online(sd) && sd.ping >= 0) return sd.ping + "ms";
        if (Servers.offline(sd)) return "Offline";
        return "...";
    }

    private int statusColor(ServerData sd) {
        if (Servers.online(sd) && sd.ping >= 0) return Servers.pingColor(sd.ping);
        if (Servers.offline(sd)) return 0xFFEF4444;
        return Theme.DIM;
    }

    private void drawRow(ServerData sd, int x, int y, int w, int mx, int my) {
        boolean sel = sd == selected, hov = over(mx, my, x, y, w, ROW) && my >= listY && my < listY + listH;
        Gfx.rr(x, y, w, ROW, 7, sel ? 0x38FFFFFF : (hov ? 0x24FFFFFF : 0x14FFFFFF));
        if (sel) Gfx.rrOutline(x, y, w, ROW, 7, 1, 0xCCFFFFFF);
        Servers.icon(sd, x + 4, y + 4, 22);
        int right = x + w - 6;
        String ps = status(sd);
        Gfx.text(ps, right - Gfx.width(ps, 8, true), y + 5, 8, true, statusColor(sd));
        String pop = Servers.online(sd) ? Servers.players(sd) : "";
        if (!pop.isEmpty()) Gfx.text(pop, right - Gfx.width(pop, 7, false), y + 17, 7, false, Theme.MUTED);
        int tw = w - 40 - Math.max(Gfx.width(ps, 8, true), Gfx.width(pop, 7, false)) - 8;
        Gfx.text(Gfx.trim(Servers.name(sd), 9, true, tw), x + 32, y + 4, 9, true, Theme.TEXT);
        String motd = Servers.plain(sd.motd).replace("\n", " ");
        Gfx.text(Gfx.trim(Servers.online(sd) && !motd.isEmpty() ? motd : sd.ip, 7, false, tw), x + 32, y + 17, 7, false, Theme.MUTED);
    }

    private void drawSide(int mx, int my) {
        Gfx.rr(sideX, hdrY, sideW, height - 2 * hdrY, 8, 0xB0121216);
        Gfx.rrOutline(sideX, hdrY, sideW, height - 2 * hdrY, 8, 1, 0x2EFFFFFF);
        int x = sideX + 10, y = hdrY + 10, w = sideW - 20;
        if (selected == null) {
            Gfx.rr(x, y, 34, 34, 7, 0x18FFFFFF);
            Gfx.textCentered("?", x + 17, y + (34 - Gfx.lineHeight(14, true)) / 2, 14, true, Theme.DIM);
            Gfx.text("No server", x + 40, y + 6, 9, true, Theme.TEXT);
            Gfx.text(Gfx.trim("Pick one from the list", 7, false, w - 40), x + 40, y + 19, 7, false, Theme.MUTED);
        } else {
            Servers.icon(selected, x, y, 34);
            Gfx.text(Gfx.trim(Servers.name(selected), 10, true, w - 40), x + 40, y + 4, 10, true, Theme.TEXT);
            Gfx.text(Gfx.trim(selected.ip, 7, false, w - 40), x + 40, y + 17, 7, false, Theme.MUTED);
            Gfx.text(status(selected), x + 40, y + 27, 7, true, statusColor(selected));
            int ly = y + 44;
            if (Servers.online(selected)) {
                String pop = Servers.players(selected);
                if (!pop.isEmpty()) { Gfx.text("Online: " + pop, x, ly, 8, false, Theme.TEXT); ly += 11; }
                String ver = Servers.plain(selected.version);
                if (!ver.isEmpty()) { Gfx.text(Gfx.trim(ver, 7, false, w), x, ly, 7, false, Theme.MUTED); ly += 11; }
                String[] lines = Servers.plain(selected.motd).split("\n");
                for (int i = 0; i < lines.length && i < 2; i++) { Gfx.text(Gfx.trim(lines[i].trim(), 7, false, w), x, ly, 7, false, Theme.MUTED); ly += 10; }
            }
        }
        int by = height - hdrY - 10 - 24;
        boolean can = selected != null;
        Gfx.rr(sideX + 10, by, w, 24, 7, can ? (over(mx, my, sideX + 10, by, w, 24) ? 0xFFFFFFFF : 0xF0F4F4F5) : 0x30FFFFFF);
        Gfx.textCentered(can ? "Play" : "Choose a server", sideX + 10 + w / 2, by + (24 - Gfx.lineHeight(10, true)) / 2, 10, true, can ? 0xFF101014 : Theme.DIM);
        if (can && isSaved(selected)) {
            int hw = (w - 4) / 2, ey = by - 22;
            button(sideX + 10, ey, hw, 18, "Edit", false, over(mx, my, sideX + 10, ey, hw, 18));
            button(sideX + 14 + hw, ey, hw, 18, "Delete", false, over(mx, my, sideX + 14 + hw, ey, hw, 18));
        }
        Gfx.text(saved.size() + " saved", sideX + 10, by - 34, 7, false, Theme.DIM);
    }

    // ---------- dialog ----------
    private void openDialog(int mode, ServerData edit) {
        dlg = mode; editing = edit; prev = null; prevFor = ""; prevAt = 0;
        fName.value = edit != null ? (edit.name == null ? "" : edit.name) : "";
        fAddr.value = edit != null ? edit.ip : "";
        fName.focus = false; fAddr.focus = true;
        search.focus = false;
        if (edit != null) startPreview();
    }

    private void startPreview() {
        String ip = fAddr.value.trim();
        prevFor = ip; prevAt = 0;
        if (ip.isEmpty()) { prev = null; return; }
        ServerData sd = new ServerData("", ip, ServerData.Type.OTHER);
        prev = sd;
        Servers.ping(pinger, sd, () -> { });
    }

    private void drawDialog(GuiGraphicsExtractor g, int mx, int my) {
        g.fill(0, 0, width, height, 0x90000000);
        dw = Math.min(width - 20, 300); dh = 134; dx = (width - dw) / 2; dy = (height - dh) / 2;
        Gfx.rr(dx, dy, dw, dh, 10, 0xF0141418);
        Gfx.rrOutline(dx, dy, dw, dh, 10, 1, 0x3CFFFFFF);
        Gfx.text(dlg == 2 ? "Direct connect" : (dlg == 3 ? "Edit server" : "Add server"), dx + 12, dy + 10, 11, true, Theme.TEXT);
        boolean two = dw >= 260 && dlg != 2;
        int fw = two ? (dw - 36) / 2 : dw - 24;
        int fy = dy + 30;
        if (dlg != 2) {
            Gfx.text("Name", dx + 12, fy, 8, true, Theme.MUTED);
            fName.x = dx + 12; fName.y = fy + 11; fName.w = fw; fName.h = 18; fName.draw("Minecraft Server", 8);
            fy += 36;
        }
        Gfx.text("Address", dx + 12, fy, 8, true, Theme.MUTED);
        fAddr.x = dx + 12; fAddr.y = fy + 11; fAddr.w = fw; fAddr.h = 18; fAddr.draw("play.example.net", 8);
        if (two) drawPreview(dx + 24 + fw, dy + 30, fw, dh - 30 - 40);
        int by = dy + dh - 28, bw = 64;
        button(dx + dw - 12 - bw, by, bw, 18, dlg == 2 ? "Connect" : "Save", true, over(mx, my, dx + dw - 12 - bw, by, bw, 18));
        button(dx + dw - 16 - 2 * bw, by, bw, 18, "Cancel", false, over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18));
    }

    private void drawPreview(int x, int y, int w, int h) {
        Gfx.rr(x, y, w, h, 7, 0x16FFFFFF);
        Gfx.rrOutline(x, y, w, h, 7, 1, 0x30FFFFFF);
        ServerData p = prev;
        String badge = p == null ? "" : (Servers.online(p) ? "ONLINE" : (Servers.offline(p) ? "OFFLINE" : "..."));
        if (!badge.isEmpty()) {
            int bw = Gfx.width(badge, 6, true) + 8;
            Gfx.rrOutline(x + w - bw - 5, y + 5, bw, 11, 5, 1, 0x80FFFFFF);
            Gfx.text(badge, x + w - bw - 1, y + 7, 6, true, p != null && Servers.offline(p) ? 0xFFEF4444 : Theme.TEXT);
        }
        String nm = fName.value.trim().isEmpty() ? (fAddr.value.trim().isEmpty() ? "Minecraft Server" : fAddr.value.trim()) : fName.value.trim();
        if (p != null) Servers.icon(p, x + 6, y + 8, 26); else Servers.letter(nm, x + 6, y + 8, 26);
        Gfx.text(Gfx.trim(nm, 9, true, w - 12), x + 6, y + 38, 9, true, Theme.TEXT);
        if (p == null) { Gfx.text(Gfx.trim("Type an address to see the server", 7, false, w - 12), x + 6, y + 51, 7, false, Theme.DIM); return; }
        if (Servers.online(p)) {
            String info = Servers.players(p) + " online | " + p.ping + "ms";
            Gfx.text(Gfx.trim(info, 7, false, w - 12), x + 6, y + 51, 7, false, Theme.MUTED);
            String[] lines = Servers.plain(p.motd).split("\n");
            for (int i = 0; i < lines.length && i < 2; i++) Gfx.text(Gfx.trim(lines[i].trim(), 7, false, w - 12), x + 6, y + 64 + i * 10, 7, false, Theme.TEXT);
        }
    }

    private void submitDialog() {
        String ip = fAddr.value.trim();
        if (ip.isEmpty()) return;
        String name = fName.value.trim().isEmpty() ? ip : fName.value.trim();
        if (dlg == 2) { dlg = 0; join(new ServerData(ip, ip, ServerData.Type.OTHER)); return; }
        if (dlg == 3 && editing != null) {
            editing.name = name;
            if (!ip.equals(editing.ip)) { editing.ip = ip; pinged.remove(editing); ping(editing); }
        } else {
            ServerData sd = new ServerData(name, ip, ServerData.Type.OTHER);
            saved.add(sd, false);
            selected = sd;
            tab = 0;
            ping(sd);
        }
        saved.save();
        dlg = 0;
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        if (e.button() != 0) return true;
        if (dlg != 0) {
            fName.focus = dlg != 2 && fName.hit(mx, my);
            fAddr.focus = fAddr.hit(mx, my);
            int by = dy + dh - 28, bw = 64;
            if (over(mx, my, dx + dw - 12 - bw, by, bw, 18)) submitDialog();
            else if (over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18) || !over(mx, my, dx, dy, dw, dh)) dlg = 0;
            return true;
        }
        layout();
        search.focus = search.hit(mx, my);
        if (over(mx, my, mainX + 4, hdrY + 4, 18, 18)) { onClose(); return true; }
        if (over(mx, my, addX, hdrY + 4, addW, 18)) { openDialog(1, null); return true; }
        if (over(mx, my, dirX, hdrY + 4, dirW, 18)) { openDialog(2, null); return true; }
        int tx = mainX + 4;
        String[] tabs = {"All", "Recent"};
        for (int i = 0; i < 2; i++) {
            int w = Gfx.width(tabs[i], 8, true) + 12;
            if (over(mx, my, tx, hdrY + hdrH + 4, w, 16)) { tab = i; scrollTarget = 0; scroll = 0; return true; }
            tx += w + 4;
        }
        // side panel
        int w = sideW - 20, by = height - hdrY - 10 - 24;
        if (selected != null && over(mx, my, sideX + 10, by, w, 24)) { join(selected); return true; }
        if (selected != null && isSaved(selected)) {
            int hw = (w - 4) / 2, ey = by - 22;
            if (over(mx, my, sideX + 10, ey, hw, 18)) { openDialog(3, selected); return true; }
            if (over(mx, my, sideX + 14 + hw, ey, hw, 18)) {
                saved.remove(selected);
                saved.save();
                selected = saved.size() > 0 ? saved.get(0) : null;
                return true;
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
                    if (sd == lastClickRow && now - lastClick < 400) { join(sd); return true; }
                    selected = sd; lastClickRow = sd; lastClick = now;
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent e) {
        int cp = e.codepoint();
        if (dlg != 0) {
            if (fName.focus) fName.type(cp); else if (fAddr.focus) fAddr.type(cp);
            return true;
        }
        if (search.focus) { search.type(cp); scrollTarget = 0; return true; }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        int k = e.key();
        if (dlg != 0) {
            if (k == 256) { dlg = 0; return true; }
            if (k == 257 || k == 335) { submitDialog(); return true; }
            if (k == 258) { if (dlg != 2) { boolean f = fName.focus; fName.focus = !f; fAddr.focus = f; } return true; }
            if (fName.focus) fName.key(k, e.modifiers()); else if (fAddr.focus) fAddr.key(k, e.modifiers());
            return true;
        }
        if (k == 256) { if (search.focus) search.focus = false; else onClose(); return true; }
        if ((k == 257 || k == 335) && !search.focus) { join(selected); return true; }
        if (search.focus) { search.key(k, e.modifiers()); scrollTarget = 0; return true; }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (dlg == 0 && sy != 0) scrollTarget -= (float) Math.signum(sy) * (ROW + 4);
        return true;
    }
}

package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * The singleplayer world list, same as the 1.8.9 one: search, filter tabs, a list with world icons and a side panel
 * with the chosen world (play, rename, delete). Replaces the vanilla world selection screen.
 */
public class WorldListScreen extends Screen {
    private static final int ROW = 30;
    private static final Map<String, Long> SIZES = new ConcurrentHashMap<String, Long>();

    private final Screen parent;
    private final List<LevelSummary> all = new ArrayList<LevelSummary>();
    private final Field search = new Field(40);
    private final Field fName = new Field(48);
    private final long opened = System.currentTimeMillis();
    private boolean loading = true;
    private int tab;               // 0 all, 1 survival, 2 creative, 3 hardcore
    private LevelSummary selected;
    private float scroll, scrollTarget;
    private long lastClick;
    private LevelSummary lastClickRow;
    private int dlg;               // 0 none, 1 rename, 2 delete
    private int mainX, mainW, sideX, sideW, listY, listH, hdrY, hdrH, addX, addW;
    private int dx, dy, dw, dh;

    public WorldListScreen(Screen parent) {
        super(Component.literal("Singleplayer"));
        this.parent = parent;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) { }

    @Override
    protected void init() { reload(); }

    @Override
    public void onClose() { Minecraft.getInstance().setScreenAndShow(parent); }

    private void reload() {
        Minecraft mc = Minecraft.getInstance();
        String keep = selected == null ? null : selected.getLevelId();
        loading = true;
        try {
            LevelStorageSource src = mc.getLevelSource();
            src.loadLevelSummaries(src.findLevelCandidates()).thenAccept(list -> mc.execute(() -> {
                all.clear();
                all.addAll(list);
                all.sort(null);
                selected = null;
                for (LevelSummary s : all) if (s.getLevelId().equals(keep)) selected = s;
                if (selected == null && !all.isEmpty()) selected = all.get(0);
                loading = false;
                for (LevelSummary s : all) size(s);
            }));
        } catch (Exception e) {
            loading = false;
        }
    }

    // ---------- data ----------
    private List<LevelSummary> rows() {
        String q = search.value.trim().toLowerCase();
        List<LevelSummary> out = new ArrayList<LevelSummary>();
        for (LevelSummary w : all) {
            if (tab == 1 && (w.isHardcore() || w.getGameMode() != GameType.SURVIVAL)) continue;
            if (tab == 2 && w.getGameMode() != GameType.CREATIVE) continue;
            if (tab == 3 && !w.isHardcore()) continue;
            if (!q.isEmpty() && !(w.getLevelName() + " " + w.getLevelId()).toLowerCase().contains(q)) continue;
            out.add(w);
        }
        return out;
    }

    private static String name(LevelSummary w) {
        String n = w.getLevelName();
        return n == null || n.trim().isEmpty() ? w.getLevelId() : n;
    }

    private static String modeName(LevelSummary w) {
        if (w.isHardcore()) return "Hardcore";
        GameType m = w.getGameMode();
        if (m == GameType.CREATIVE) return "Creative";
        if (m == GameType.ADVENTURE) return "Adventure";
        if (m == GameType.SPECTATOR) return "Spectator";
        return "Survival";
    }

    private static int modeColor(LevelSummary w) {
        if (w.isHardcore()) return 0xFFEF4444;
        GameType m = w.getGameMode();
        if (m == GameType.CREATIVE) return 0xFF5AA9FF;
        if (m == GameType.ADVENTURE) return 0xFFFFC21A;
        if (m == GameType.SPECTATOR) return 0xFFB58CFF;
        return 0xFF34D27B;
    }

    private static String date(long t) { return t <= 0 ? "-" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(t)); }

    /** Folder size, worked out once in the background. */
    private static String size(LevelSummary w) {
        String id = w.getLevelId();
        Long b = SIZES.get(id);
        if (b == null) {
            SIZES.put(id, -1L);
            Path dir = Minecraft.getInstance().getLevelSource().getLevelPath(id);
            Thread t = new Thread(() -> {
                long sum = 0;
                try (Stream<Path> s = Files.walk(dir)) {
                    sum = s.filter(Files::isRegularFile).mapToLong(p -> { try { return Files.size(p); } catch (Exception e) { return 0L; } }).sum();
                } catch (Exception ignored) { }
                SIZES.put(id, sum);
            }, "YA-World-Size");
            t.setDaemon(true);
            t.start();
            return "-";
        }
        long v = b;
        return v <= 0 ? "-" : (v < 1048576L ? (v / 1024L) + " KB" : (v / 1048576L) + " MB");
    }

    private void play(LevelSummary w) {
        if (w == null || !w.primaryActionActive()) return;
        Minecraft mc = Minecraft.getInstance();
        mc.createWorldOpenFlows().openWorld(w.getLevelId(), () -> mc.setScreenAndShow(this));
    }

    private static void icon(LevelSummary w, int x, int y, int s) {
        Path p = w.getIcon();
        if (p != null && Gfx.picture("world:" + w.getLevelId() + "|" + w.getLastPlayed(), () -> {
            try { return Files.isRegularFile(p) ? ImageIO.read(p.toFile()) : null; } catch (Exception e) { return null; }
        }, x, y, s, s)) return;
        Servers.letter(name(w), x, y, s);
    }

    // ---------- drawing ----------
    private static boolean over(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    private static void button(int x, int y, int w, int h, String label, boolean primary, boolean hover) {
        Gfx.rr(x, y, w, h, 6, primary ? (hover ? 0xFFFFFFFF : 0xF0F4F4F5) : (hover ? 0xC0303036 : 0x90202026));
        if (!primary) Gfx.rrOutline(x, y, w, h, 6, 1, hover ? 0x66FFFFFF : 0x2EFFFFFF);
        Gfx.textCentered(label, x + w / 2, y + (h - Gfx.lineHeight(8, true)) / 2, 8, true, primary ? 0xFF101014 : Theme.TEXT);
    }

    private static final String[] TABS = {"All", "Survival", "Creative", "Hardcore"};

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

        Gfx.rr(mainX, hdrY, mainW, hdrH, 8, 0xB0121216);
        Gfx.rrOutline(mainX, hdrY, mainW, hdrH, 8, 1, 0x2EFFFFFF);
        Gfx.rr(mainX + 4, hdrY + 4, 18, 18, 5, over(hx, hy, mainX + 4, hdrY + 4, 18, 18) ? 0x40FFFFFF : 0x18FFFFFF);
        Gfx.textCentered("<", mainX + 13, hdrY + 4 + (18 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
        int tx = mainX + 28;
        if (mainW >= 300) { Gfx.text("Worlds", tx, hdrY + (hdrH - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.TEXT); tx += Gfx.width("Worlds", 11, true) + 8; }
        addW = Gfx.width("+ Create", 8, true) + 14;
        addX = mainX + mainW - 4 - addW;
        search.x = tx; search.y = hdrY + 4; search.w = Math.max(40, addX - 6 - tx); search.h = 18;
        search.draw("Search worlds...", 8);
        button(addX, hdrY + 4, addW, 18, "+ Create", true, over(hx, hy, addX, hdrY + 4, addW, 18));

        int x = mainX + 4;
        for (int i = 0; i < TABS.length; i++) {
            int w = Gfx.width(TABS[i], 8, true) + 12;
            Gfx.text(TABS[i], x + 6, tabsY + 2, 8, true, i == tab ? MenuBg.INK : MenuBg.INK2);
            if (i == tab) Gfx.rr(x + 4, tabsY + 13, w - 8, 1, 0, MenuBg.INK);
            x += w + 4;
        }

        Gfx.rr(mainX, listY, mainW, listH, 8, 0xA0101014);
        Gfx.rrOutline(mainX, listY, mainW, listH, 8, 1, 0x24FFFFFF);
        List<LevelSummary> rows = rows();
        float maxScroll = Math.max(0, rows.size() * (ROW + 4) + 4 - listH);
        scrollTarget = Ui.clamp(scrollTarget, 0f, maxScroll);
        scroll = Ui.lerp(scroll, scrollTarget, 0.3f);
        if (rows.isEmpty()) {
            Gfx.textCentered(loading ? "Loading worlds..." : "No worlds yet", mainX + mainW / 2, listY + listH / 2 - 12, 10, true, Theme.TEXT);
            if (!loading) Gfx.textCentered("Create one with the + Create button", mainX + mainW / 2, listY + listH / 2 + 2, 8, false, Theme.MUTED);
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

    private void drawRow(LevelSummary w, int x, int y, int rw, int mx, int my) {
        boolean sel = w == selected, hov = over(mx, my, x, y, rw, ROW) && my >= listY && my < listY + listH;
        Gfx.rr(x, y, rw, ROW, 7, sel ? 0x38FFFFFF : (hov ? 0x24FFFFFF : 0x14FFFFFF));
        if (sel) Gfx.rrOutline(x, y, rw, ROW, 7, 1, 0xCCFFFFFF);
        icon(w, x + 4, y + 4, 22);
        int right = x + rw - 6;
        String ms = modeName(w);
        Gfx.text(ms, right - Gfx.width(ms, 8, true), y + 5, 8, true, modeColor(w));
        String sz = size(w);
        Gfx.text(sz, right - Gfx.width(sz, 7, false), y + 17, 7, false, Theme.MUTED);
        int tw = rw - 40 - Math.max(Gfx.width(ms, 8, true), Gfx.width(sz, 7, false)) - 8;
        Gfx.text(Gfx.trim(name(w), 9, true, tw), x + 32, y + 4, 9, true, Theme.TEXT);
        Gfx.text(Gfx.trim(date(w.getLastPlayed()), 7, false, tw), x + 32, y + 17, 7, false, Theme.MUTED);
    }

    private void drawSide(int mx, int my) {
        Gfx.rr(sideX, hdrY, sideW, height - 2 * hdrY, 8, 0xB0121216);
        Gfx.rrOutline(sideX, hdrY, sideW, height - 2 * hdrY, 8, 1, 0x2EFFFFFF);
        int x = sideX + 10, y = hdrY + 10, w = sideW - 20;
        if (selected == null) {
            Gfx.rr(x, y, 34, 34, 7, 0x18FFFFFF);
            Gfx.textCentered("?", x + 17, y + (34 - Gfx.lineHeight(14, true)) / 2, 14, true, Theme.DIM);
            Gfx.text("No world", x + 40, y + 6, 9, true, Theme.TEXT);
            Gfx.text(Gfx.trim("Create one to start", 7, false, w - 40), x + 40, y + 19, 7, false, Theme.MUTED);
        } else {
            icon(selected, x, y, 34);
            Gfx.text(Gfx.trim(name(selected), 10, true, w - 40), x + 40, y + 4, 10, true, Theme.TEXT);
            Gfx.text(Gfx.trim(selected.getLevelId(), 7, false, w - 40), x + 40, y + 17, 7, false, Theme.MUTED);
            Gfx.text(modeName(selected), x + 40, y + 27, 7, true, modeColor(selected));
            int ly = y + 44;
            Gfx.text("Played: " + date(selected.getLastPlayed()), x, ly, 7, false, Theme.TEXT); ly += 11;
            Gfx.text("Size: " + size(selected), x, ly, 7, false, Theme.MUTED); ly += 11;
            Gfx.text("Cheats: " + (selected.hasCommands() ? "Yes" : "No"), x, ly, 7, false, Theme.MUTED); ly += 11;
            Gfx.text(Gfx.trim(selected.getWorldVersionName().getString(), 7, false, w), x, ly, 7, false, Theme.DIM);
        }
        int by = height - hdrY - 10 - 24;
        boolean can = selected != null && selected.primaryActionActive();
        Gfx.rr(sideX + 10, by, w, 24, 7, can ? (over(mx, my, sideX + 10, by, w, 24) ? 0xFFFFFFFF : 0xF0F4F4F5) : 0x30FFFFFF);
        Gfx.textCentered(selected == null ? "Choose a world" : (can ? "Play" : "Can't open"), sideX + 10 + w / 2, by + (24 - Gfx.lineHeight(10, true)) / 2, 10, true, can ? 0xFF101014 : Theme.DIM);
        if (selected != null) {
            int hw = (w - 4) / 2, ey = by - 22;
            button(sideX + 10, ey, hw, 18, "Rename", false, over(mx, my, sideX + 10, ey, hw, 18));
            button(sideX + 14 + hw, ey, hw, 18, "Delete", false, over(mx, my, sideX + 14 + hw, ey, hw, 18));
        }
        Gfx.text(all.size() + " worlds", sideX + 10, by - 34, 7, false, Theme.DIM);
    }

    private void drawDialog(GuiGraphicsExtractor g, int mx, int my) {
        g.fill(0, 0, width, height, 0x90000000);
        dw = Math.min(width - 20, 260); dh = dlg == 1 ? 96 : 84; dx = (width - dw) / 2; dy = (height - dh) / 2;
        Gfx.rr(dx, dy, dw, dh, 10, 0xF0141418);
        Gfx.rrOutline(dx, dy, dw, dh, 10, 1, 0x3CFFFFFF);
        Gfx.text(dlg == 1 ? "Rename" : "Delete this world?", dx + 12, dy + 10, 11, true, Theme.TEXT);
        if (dlg == 1) {
            fName.x = dx + 12; fName.y = dy + 30; fName.w = dw - 24; fName.h = 18; fName.draw("World name", 8);
        } else if (selected != null) {
            Gfx.text(Gfx.trim(name(selected), 9, true, dw - 24), dx + 12, dy + 30, 9, true, Theme.TEXT);
            Gfx.text(Gfx.trim("It will be gone forever (a long time!)", 7, false, dw - 24), dx + 12, dy + 43, 7, false, Theme.MUTED);
        }
        int by = dy + dh - 28, bw = 64;
        button(dx + dw - 12 - bw, by, bw, 18, dlg == 1 ? "Save" : "Delete", true, over(mx, my, dx + dw - 12 - bw, by, bw, 18));
        button(dx + dw - 16 - 2 * bw, by, bw, 18, "Cancel", false, over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18));
    }

    private void submitDialog() {
        if (selected == null) { dlg = 0; return; }
        LevelStorageSource src = Minecraft.getInstance().getLevelSource();
        if (dlg == 1) {
            String n = fName.value.trim();
            if (n.isEmpty()) return;
            try (LevelStorageSource.LevelStorageAccess a = src.createAccess(selected.getLevelId())) {
                a.renameLevel(n);
            } catch (Exception ignored) { }
        } else {
            try (LevelStorageSource.LevelStorageAccess a = src.createAccess(selected.getLevelId())) {
                a.deleteLevel();
            } catch (Exception ignored) { }
            SIZES.remove(selected.getLevelId());
            selected = null;
        }
        dlg = 0;
        reload();
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        if (e.button() != 0) return true;
        if (dlg != 0) {
            fName.focus = dlg == 1 && fName.hit(mx, my);
            int by = dy + dh - 28, bw = 64;
            if (over(mx, my, dx + dw - 12 - bw, by, bw, 18)) submitDialog();
            else if (over(mx, my, dx + dw - 16 - 2 * bw, by, bw, 18) || !over(mx, my, dx, dy, dw, dh)) dlg = 0;
            return true;
        }
        layout();
        Minecraft mc = Minecraft.getInstance();
        search.focus = search.hit(mx, my);
        if (over(mx, my, mainX + 4, hdrY + 4, 18, 18)) { onClose(); return true; }
        if (over(mx, my, addX, hdrY + 4, addW, 18)) { CreateWorldScreen.openFresh(mc, () -> mc.setScreenAndShow(this)); return true; }
        int tx = mainX + 4;
        for (int i = 0; i < TABS.length; i++) {
            int w = Gfx.width(TABS[i], 8, true) + 12;
            if (over(mx, my, tx, hdrY + hdrH + 4, w, 16)) { tab = i; scrollTarget = 0; scroll = 0; return true; }
            tx += w + 4;
        }
        int w = sideW - 20, by = height - hdrY - 10 - 24;
        if (selected != null && over(mx, my, sideX + 10, by, w, 24)) { play(selected); return true; }
        if (selected != null) {
            int hw = (w - 4) / 2, ey = by - 22;
            if (over(mx, my, sideX + 10, ey, hw, 18)) { dlg = 1; fName.value = name(selected); fName.focus = true; search.focus = false; return true; }
            if (over(mx, my, sideX + 14 + hw, ey, hw, 18)) { dlg = 2; search.focus = false; return true; }
        }
        if (over(mx, my, mainX, listY, mainW, listH)) {
            List<LevelSummary> rows = rows();
            for (int i = 0; i < rows.size(); i++) {
                int ry = listY + 4 + i * (ROW + 4) - Math.round(scroll);
                if (over(mx, my, mainX + 4, ry, mainW - 8, ROW)) {
                    LevelSummary sw = rows.get(i);
                    long now = System.currentTimeMillis();
                    if (sw == lastClickRow && now - lastClick < 400) { play(sw); return true; }
                    selected = sw; lastClickRow = sw; lastClick = now;
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent e) {
        int cp = e.codepoint();
        if (dlg == 1) { fName.type(cp); return true; }
        if (dlg == 0 && search.focus) { search.type(cp); scrollTarget = 0; }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        int k = e.key();
        if (dlg != 0) {
            if (k == 256) { dlg = 0; return true; }
            if (k == 257 || k == 335) { submitDialog(); return true; }
            if (dlg == 1) fName.key(k, e.modifiers());
            return true;
        }
        if (k == 256) { if (search.focus) search.focus = false; else onClose(); return true; }
        if ((k == 257 || k == 335) && !search.focus) { play(selected); return true; }
        if (search.focus) { search.key(k, e.modifiers()); scrollTarget = 0; }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (dlg == 0 && sy != 0) scrollTarget -= (float) Math.signum(sy) * (ROW + 4);
        return true;
    }
}

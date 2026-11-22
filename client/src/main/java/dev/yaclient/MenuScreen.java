package dev.yaclient;

import dev.yaclient.cosmetics.CosmeticPreview;
import dev.yaclient.cosmetics.Logo;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main client menu: glass panel, animated sidebar with icons, searchable module cards with a
 * per-module settings drawer, cosmetics page and global settings (language, accent color, HUD).
 * Immediate-mode layout: every frame registers its clickable regions in {@code hits}.
 */
public class MenuScreen extends GuiScreen {
    private interface Action { void run(float x, float y); }

    private static final class Hit {
        final int x, y, w, h;
        final Action click, drag, right;

        Hit(int x, int y, int w, int h, Action click, Action drag, Action right) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.click = click; this.drag = drag; this.right = right;
        }

        boolean contains(float px, float py) { return px >= x && px < x + w && py >= y && py < y + h; }
    }

    // ----- layout (GUI units) -----
    private static final int PW = 780, PH = 450;
    private static final int CX = 24, VY = 124, VW = PW - 48, VH = PH - VY - 18;
    private static final String[] TABS = {"all", "info", "pvp", "utility", "cosmetics", "settings"};
    private static final String[] TAB_ICONS = {"grid", "info", "sword", "wrench", "shirt", "gear"};
    // menu palette (same as the design previews): near-black panel, soft grey surfaces, white accents
    private static final int PANEL = 0x700F1012, SURF = 0x781A1B1F, SURF2 = 0x84222328, SURF3 = 0x962D2E34;
    private static final int ACC = 0xFFE8E8E8, ON_ACC = 0xFF101014, OFF = 0xFF3A3B42, GOLD = 0xFFFFC21A;

    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<String, Float> anim = new HashMap<String, Float>();
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float fade = 0.05f, scale = 1f, pageFade = 0f, indY = -1f;
    private float scroll, scrollTarget, contentH, drawerP;
    private int originX, originY, gs = 2;
    private float mxl, myl;
    private int tab = 0;
    /** True when opened from the hub's cosmetics button: only the cosmetics page, no module tabs. */
    private boolean cosMode;

    public MenuScreen() {}

    public MenuScreen(int startTab) { this.tab = startTab; this.cosMode = startTab == 4; if (startTab == 4) CosmeticsApi.refreshAsync(true); }
    private String query = "";
    private boolean searchFocus;
    private Setting.Text focusText;
    private Setting.Key focusKey;
    private boolean paint;
    private Module drawerModule, drawerLast;
    private Action activeDrag;

    // ================= helpers =================
    private float anim(String key, float target, float dt, float speed, float initial) {
        Float cur = anim.get(key);
        float v = Ui.approach(cur == null ? initial : cur, target, dt, speed);
        anim.put(key, v);
        return v;
    }

    private boolean over(int x, int y, int w, int h) { return mxl >= x && mxl < x + w && myl >= y && myl < y + h; }

    private boolean overView(int x, int y, int w, int h) { return over(x, y, w, h) && myl >= VY && myl < VY + VH; }

    private void hit(int x, int y, int w, int h, Action click, Action drag, Action right) {
        hits.add(new Hit(x, y, w, h, click, drag, right));
    }

    /** Hit region for content inside the scrolling viewport (clipped to it). */
    private void hitView(int x, int y, int w, int h, Action click, Action drag, Action right) {
        int y0 = Math.max(y, VY), y1 = Math.min(y + h, VY + VH);
        if (y1 > y0) hits.add(new Hit(x, y0, w, y1 - y0, click, drag, right));
    }

    private void clip(int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getMinecraft();
        int px = Math.round((originX + x * scale) * gs);
        int pw = Math.round(w * scale * gs);
        int ph = Math.round(h * scale * gs);
        int py = Math.round(mc.displayHeight - (originY + (y + h) * scale) * gs);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(px, py, Math.max(0, pw), Math.max(0, ph));
    }

    private void unclip() { GL11.glDisable(GL11.GL_SCISSOR_TEST); }

    private float lx(int mx) { return (mx - originX) / scale; }

    private float ly(int my) { return (my - originY) / scale; }

    private boolean moduleTab() { return tab >= 0 && tab <= 3; }

    private void selectTab(int i) {
        tab = i;
        scroll = 0f;
        scrollTarget = 0f;
        pageFade = 0f;
        drawerModule = null;
        searchFocus = false;
        if (i == 4) CosmeticsApi.refreshAsync(true);
    }

    private void saveAll() {
        for (Module m : YAClient.MODULES) m.saveConfig();
        Config.save();
    }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<Module>();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Module m : YAClient.MODULES) {
            if (tab >= 1 && tab <= 3 && m.category.ordinal() != tab - 1) continue;
            if (!q.isEmpty() && !m.name().toLowerCase(Locale.ROOT).contains(q)
                    && !m.desc().toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    // ================= frame =================
    @Override
    public void drawScreen(int mx, int my, float pt) {
        Minecraft mc = Minecraft.getMinecraft();
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        fade = 0.05f + 0.95f * Ui.ease((now - opened) / 200f);
        float open = Ui.ease((now - opened) / 240f);
        gs = new ScaledResolution(mc).getScaleFactor();
        // same physical size on every GUI scale: about 1 pixel per unit at 1080p
        float phys = Math.max(0.5f, mc.displayHeight / 1080f * 1.3f);
        scale = Math.min(phys / gs, Math.min((height - 16f) / PH, (width - 16f) / PW));
        Gfx.setScale(Math.max(1, Math.round(phys * 2f)));
        originX = Math.round(width / 2f - PW * scale / 2f);
        originY = Math.round(height / 2f - PH * scale / 2f);
        mxl = lx(mx);
        myl = ly(my);

        hits.clear();
        pageFade = Ui.approach(pageFade, 1f, dt, 14f);
        scroll = Ui.approach(scroll, scrollTarget, dt, 16f);
        drawerP = Ui.approach(drawerP, drawerModule != null ? 1f : 0f, dt, 16f);
        if (drawerModule != null) drawerLast = drawerModule;

        Gui.drawRect(0, 0, width, height, Ui.alpha(0x40000000, fade));

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0f);
        float sc = scale * (0.95f + 0.05f * open);
        GlStateManager.scale(sc, sc, 1f);
        GlStateManager.translate(-PW / 2f, -PH / 2f, 0f);
        Gfx.fade = fade;

        drawFrame();
        drawTabs(dt);
        drawHeader();
        drawBody(dt);
        drawDrawer(dt);

        Gfx.fade = 1f;
        GlStateManager.popMatrix();
    }

    private void drawFrame() {
        for (int i = 5; i >= 1; i--) Gfx.rr(-i * 3, -i * 3 + 10, PW + i * 6, PH + i * 6, 18 + i * 3, 0x12000000);
        Gfx.rr(0, 0, PW, PH, 10, PANEL);
        Gfx.rrOutline(0, 0, PW, PH, 10, 1, 0x24FFFFFF);
    }

    // ================= top bar: segmented tabs =================
    private void drawTabs(float dt) {
        final int[] nav = cosMode ? new int[]{4} : new int[]{0, 1, 2, 3, 5};
        int[] ws = new int[nav.length];
        int total = 0;
        for (int i = 0; i < nav.length; i++) { ws[i] = Gfx.width(Lang.t("tab." + TABS[nav[i]]), 12, true) + 46; total += ws[i]; }
        int y = 70, x = CX;
        Gfx.rr(x, y, total + 8, 38, 6, SURF);
        int px = x + 4;
        for (int i = 0; i < nav.length; i++) {
            final int idx = nav[i];
            boolean active = tab == idx;
            float h = anim("nav" + idx, active ? 1f : (over(px, y + 4, ws[i], 30) ? 0.5f : 0f), dt, 16f, active ? 1f : 0f);
            if (h > 0.01f) Gfx.rr(px, y + 4, ws[i], 30, 4, Ui.alpha(active ? SURF3 : SURF2, Math.min(1f, h)));
            int fg = active ? Theme.TEXT : Ui.mix(Theme.MUTED, Theme.TEXT, h);
            Gfx.icon(TAB_ICONS[idx], px + 13, y + 4 + 6, 18, fg);
            Gfx.text(Lang.t("tab." + TABS[idx]), px + 36, y + 4 + (30 - Gfx.lineHeight(12, true)) / 2, 12, true, fg);
            hit(px, y + 4, ws[i], 30, new Action() { @Override public void run(float a, float b) { selectTab(idx); } }, null, null);
            px += ws[i];
        }
        if (!cosMode) {
            String lab = Lang.t("ui.hud_editor");
            int w = Gfx.width(lab, 11, false) + 48, bx = PW - 24 - w;
            boolean ov = over(bx, y, w, 38);
            Gfx.rr(bx, y, w, 38, 6, ov ? SURF2 : SURF);
            Gfx.icon("move", bx + 14, y + 10, 18, ov ? Theme.TEXT : Theme.MUTED);
            Gfx.text(lab, bx + 38, y + (38 - Gfx.lineHeight(11, false)) / 2, 11, false, ov ? Theme.TEXT : Theme.MUTED);
            hit(bx, y, w, 38, new Action() { @Override public void run(float a, float b) { Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen()); } }, null, null);
        }
    }

    // ================= header =================
    private void drawHeader() {
        Logo.draw(24, 14, 38, 38, fade);
        int ls = 18;
        Gfx.text("CLIENT", 70, 16, ls, true, Theme.BRAND_RED);
        String sub;
        if (moduleTab()) {
            int total = visibleModules().size(), on = 0;
            for (Module m : visibleModules()) if (m.enabled) on++;
            sub = Lang.t("ui.count", total, on);
        } else {
            sub = Lang.t("tab." + TABS[tab] + ".sub");
        }
        Gfx.text(sub, 70, 16 + Gfx.lineHeight(ls, true) + 2, 10, false, Theme.MUTED);

        if (!moduleTab()) return;
        final int x = PW - 24 - 230, y = 18, w = 230, h = 34;
        boolean ov = over(x, y, w, h);
        Gfx.rr(x, y, w, h, 6, searchFocus ? SURF3 : (ov ? SURF2 : SURF));
        if (searchFocus) Gfx.rrOutline(x, y, w, h, 6, 1, 0x66FFFFFF);
        Gfx.icon("search", x + 11, y + (h - 16) / 2, 16, Theme.MUTED);
        int ty = y + (h - Gfx.lineHeight(12, false)) / 2;
        if (query.isEmpty()) {
            Gfx.text(Lang.t("ui.search"), x + 34, ty, 12, false, Theme.DIM);
        } else {
            Gfx.text(Gfx.trim(query, 12, false, w - 46), x + 34, ty, 12, false, Theme.TEXT);
        }
        if (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cx = x + 34 + (query.isEmpty() ? 0 : Math.min(w - 46, Gfx.width(query, 12, false)));
            Gui.drawRect(cx + 1, ty, cx + 2, ty + Gfx.lineHeight(12, false), Ui.alpha(0xFFFFFFFF, fade));
        }
        hit(x, y, w, h, new Action() { @Override public void run(float a, float b) { searchFocus = true; } }, null, null);
    }

    // ================= body =================
    private void drawBody(float dt) {
        float cf = Ui.ease(pageFade);
        Gfx.fade = fade * cf;
        int top = VY - Math.round(scroll) + Math.round((1f - cf) * 10f);
        clip(CX - 6, VY - 2, VW + 12, VH + 4);
        if (moduleTab()) contentH = drawModules(top, dt);
        else if (tab == 4) contentH = drawCosmetics();
        else contentH = drawSettings(top, dt);
        unclip();
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - VH));

        if (contentH > VH) {
            float kh = Math.max(24f, VH * VH / contentH);
            float ky = VY + (scroll / (contentH - VH)) * (VH - kh);
            Gfx.rr(PW - 12, Math.round(ky), 4, Math.round(kh), 2, 0x33FFFFFF);
        }
        Gfx.fade = fade;
    }

    // ----- modules -----
    private float drawModules(int top, float dt) {
        List<Module> list = visibleModules();
        if (list.isEmpty()) {
            Gfx.textCentered(Lang.t("ui.no_results"), CX + VW / 2, VY + 70, 13, false, Theme.MUTED);
            return 0f;
        }
        int gap = 12, cw = (VW - 2 * gap) / 3, ch = 120;
        for (int i = 0; i < list.size(); i++) {
            int x = CX + (i % 3) * (cw + gap), y = top + (i / 3) * (ch + gap);
            if (y + ch < VY - 2 || y > VY + VH + 2) continue;
            drawCard(list.get(i), x, y, cw, ch, dt);
        }
        return ((list.size() + 2) / 3) * (ch + gap);
    }

    private void drawCard(final Module m, int x, int y, int cw, int ch, float dt) {
        float h = anim("h" + m.id, overView(x, y, cw, ch) ? 1f : 0f, dt, 16f, 0f);
        float a = anim("e" + m.id, m.enabled ? 1f : 0f, dt, 14f, m.enabled ? 1f : 0f);
        boolean locked = m.locked();

        Gfx.rr(x, y, cw, ch, 6, Ui.mix(SURF, SURF2, h));
        Gfx.rrOutline(x, y, cw, ch, 6, 1, Ui.mix(0x14FFFFFF, 0x30FFFFFF, h));

        int fg = m.available && !locked ? Theme.TEXT : Theme.MUTED;
        // icon tile, name beside it, description under both
        Gfx.rr(x + 14, y + 14, 36, 36, 6, Ui.mix(SURF3, 0xFF3A3B42, Math.max(a * 0.7f, h * 0.5f)));
        Gfx.icon(m.icon(), x + 14 + 8, y + 14 + 8, 20, Ui.mix(Theme.MUTED, Theme.TEXT, Math.max(a, h)));
        Gfx.text(Gfx.trim(m.name(), 14, true, cw - 74), x + 60, y + 14 + (36 - Gfx.lineHeight(14, true)) / 2, 14, true, fg);
        Gfx.text(Gfx.trim(m.desc(), 10, false, cw - 28), x + 14, y + 58, 10, false, Theme.MUTED);

        hitView(x, y, cw, ch, null, null, new Action() {
            @Override public void run(float px, float py) { if (!m.settings.isEmpty() && m.available && !m.locked()) drawerModule = m; }
        });
        boolean gear = m.available && !locked && !m.settings.isEmpty();
        int bx = x + 14, by = y + ch - 14 - 26, bw = Gfx.width(Lang.t("ui.options"), 10, true) + 30;
        boolean oo = gear && overView(bx, by, bw, 26);
        Gfx.rr(bx, by, bw, 26, 4, gear ? (oo ? SURF3 : 0xFF26272C) : 0xFF1F2024);
        Gfx.textCentered(Lang.t("ui.options"), bx + bw / 2, by + (26 - Gfx.lineHeight(10, true)) / 2, 10, true, gear ? Theme.TEXT : Theme.DIM);
        if (gear) hitView(bx, by, bw, 26, new Action() { @Override public void run(float px, float py) { drawerModule = m; } }, null, null);

        if (!m.available) {
            pill(Lang.t("ui.soon"), x + cw - 14, by + 3, 0xFF2D2E34, Theme.MUTED);
        } else if (locked) {
            pill("PLUS", x + cw - 14, by + 3, GOLD, 0xFF101014);
        } else {
            int sx = x + cw - 14 - 38, sy = by + 3;
            Gfx.rr(sx, sy, 38, 20, 6, Ui.mix(OFF, ACC, a));
            Gfx.rr(sx + 2 + Math.round(18 * a), sy + 2, 16, 16, 4, Ui.mix(0xFF9A9BA3, ON_ACC, a));
            hitView(sx - 8, sy - 6, 54, 32, new Action() { @Override public void run(float px, float py) { m.toggle(); } }, null, null);
        }
    }

    /** Small rounded label whose right edge is at rightX. */
    private void pill(String text, int rightX, int y, int bg, int fg) {
        int w = Gfx.width(text, 9, true) + 18;
        Gfx.rr(rightX - w, y, w, 20, 6, bg);
        Gfx.textCentered(text, rightX - w / 2, y + (20 - Gfx.lineHeight(9, true)) / 2, 9, true, fg);
    }

    // ----- module options page: group list on the left, one calm card of rows on the right -----
    private static final String[] GROUP_ORDER = {"general", "style", "colors", "text", "layout"};
    private static final int PG_X = 284, PG_Y = 84, PG_W = PW - 284 - 24, PG_H = PH - 84 - 20, PAD = 20;
    private float pageScroll, pageTarget, pageContentH;
    private Module pageFor;
    private String pageGroup;

    private void pageHit(int x, int y, int w, int h, Action click, Action drag) {
        int y0 = Math.max(y, PG_Y + 4), y1 = Math.min(y + h, PG_Y + PG_H - 4);
        if (y1 > y0) hits.add(new Hit(x, y0, w, y1 - y0, click, drag, null));
    }

    private boolean pageOver(int x, int y, int w, int h) {
        return over(x, y, w, h) && myl >= PG_Y + 4 && myl < PG_Y + PG_H - 4;
    }

    private java.util.List<String> groupsOf(Module m) {
        java.util.List<String> out = new ArrayList<String>();
        for (String g : GROUP_ORDER) for (Setting st : m.settings) if (st.group.equals(g)) { out.add(g); break; }
        for (Setting st : m.settings) if (!out.contains(st.group)) out.add(st.group);
        return out;
    }

    private void ghostButton(int x, int y, int w, String icon, String label, boolean live, Action act) {
        boolean ov = over(x, y, w, 28);
        Gfx.rr(x, y, w, 28, 4, ov ? SURF3 : SURF2);
        Gfx.icon(icon, x + 11, y + 5, 18, ov ? Theme.TEXT : Theme.MUTED);
        Gfx.text(label, x + 36, y + (28 - Gfx.lineHeight(11, false)) / 2, 11, false, ov ? Theme.TEXT : Theme.MUTED);
        if (live) hit(x, y, w, 28, act, null, null);
    }

    private void drawDrawer(float dt) {
        final Module m = drawerModule != null ? drawerModule : drawerLast;
        float p = Ui.ease(drawerP);
        if (m == null || p <= 0.003f) return;
        if (pageFor != m) { pageFor = m; pageScroll = 0f; pageTarget = 0f; pageGroup = null; }
        java.util.List<String> groups = groupsOf(m);
        if (pageGroup == null || !groups.contains(pageGroup)) pageGroup = groups.isEmpty() ? "" : groups.get(0);
        pageScroll = Ui.approach(pageScroll, pageTarget, dt, 16f);
        boolean live = drawerModule != null;
        Gfx.fade = fade * p;

        if (live) hits.add(new Hit(0, 0, PW, PH, null, null, null));   // the page swallows every click behind it
        Gfx.rr(0, 0, PW, PH, 10, 0xFF0F1012);
        Gfx.rrOutline(0, 0, PW, PH, 10, 1, 0x24FFFFFF);

        // ---- header: breadcrumb, title, enable switch
        boolean bo = over(24, 22, 70, 20);
        Gfx.text("<  " + Lang.t("ui.back"), 24, 22, 11, false, bo ? Theme.TEXT : Theme.MUTED);
        if (live) hit(20, 18, 80, 28, new Action() { @Override public void run(float x, float y) { drawerModule = null; } }, null, null);
        Gfx.rr(24, 40, 40, 40, 6, SURF3);
        Gfx.icon(m.icon(), 34, 50, 20, Theme.TEXT);
        Gfx.text(Gfx.trim(m.name(), 20, true, PW - 24 - 90 - 220), 76, 50, 20, true, Theme.TEXT);
        float ea = anim("pg_en" + m.id, m.enabled ? 1f : 0f, dt, 14f, m.enabled ? 1f : 0f);
        String es = Lang.t(m.enabled ? "ui.enabled" : "ui.disabled");
        int sx0 = PW - 24 - 38;
        Gfx.text(es, sx0 - 12 - Gfx.width(es, 11, false), 48, 11, false, Ui.mix(Theme.MUTED, Theme.TEXT, ea));
        Gfx.rr(sx0, 46, 38, 20, 6, Ui.mix(OFF, ACC, ea));
        Gfx.rr(sx0 + 2 + Math.round(18 * ea), 48, 16, 16, 4, Ui.mix(0xFF9A9BA3, ON_ACC, ea));
        if (live) hit(sx0 - 90, 40, 130, 32, new Action() { @Override public void run(float x, float y) { m.toggle(); } }, null, null);

        // ---- left column: preview, group list, actions
        int lx0 = 24, lw = 236;
        Gfx.rr(lx0, PG_Y, lw, 108, 6, SURF);
        Gfx.rrOutline(lx0, PG_Y, lw, 108, 6, 1, 0x14FFFFFF);
        if (m instanceof dev.yaclient.modules.HudModule) {
            dev.yaclient.modules.HudModule hm = (dev.yaclient.modules.HudModule) m;
            int ew0 = Math.max(1, hm.width()), eh0 = Math.max(1, hm.height());
            float ps = Math.min(1.8f, Math.min((lw - 28f) / ew0, 80f / eh0));
            hm.drawPreviewAt(lx0 + Math.round((lw - ew0 * ps) / 2f), PG_Y + Math.round((108 - eh0 * ps) / 2f), ps);
        } else {
            if (m instanceof dev.yaclient.modules.CrosshairModule) ((dev.yaclient.modules.CrosshairModule) m).drawAt(lx0 + lw / 2, PG_Y + 54, 2f);
            else Gfx.icon(m.icon(), lx0 + (lw - 48) / 2, PG_Y + 30, 48, Theme.DIM);
        }
        int gy = PG_Y + 108 + 12;
        for (final String g : groups) {
            int count = 0;
            for (Setting st : m.settings) if (st.group.equals(g)) count++;
            boolean act = g.equals(pageGroup), go = over(lx0, gy, lw, 28);
            float ga = anim("pg_g" + g, act ? 1f : 0f, dt, 16f, act ? 1f : 0f);
            if (ga > 0.01f) Gfx.rr(lx0, gy, lw, 28, 4, Ui.alpha(SURF3, ga));
            else if (go) Gfx.rr(lx0, gy, lw, 28, 4, SURF);
            if (ga > 0.01f) Gfx.rr(lx0 + 2, gy + 6, 3, 16, 2, Ui.alpha(ACC, ga));
            Gfx.text(Lang.t("grp." + g), lx0 + 16, gy + (28 - Gfx.lineHeight(12, act)) / 2, 12, act, act ? Theme.TEXT : Theme.MUTED);
            String cs = String.valueOf(count);
            Gfx.text(cs, lx0 + lw - 12 - Gfx.width(cs, 10, false), gy + (28 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.DIM);
            if (live) hit(lx0, gy, lw, 28, new Action() { @Override public void run(float x, float y) { pageGroup = g; focusText = null; pageScroll = 0f; pageTarget = 0f; } }, null, null);
            gy += 30;
        }
        int by = PG_Y + PG_H - 28;
        ghostButton(lx0, by, lw, "reset", Lang.t("ui.reset_opts"), live, new Action() { @Override public void run(float x, float y) { m.resetSettings(); saveAll(); } });
        if (m instanceof dev.yaclient.modules.HudModule) {
            by -= 32;
            ghostButton(lx0, by, lw, "move", Lang.t("ui.edit_pos"), live, new Action() { @Override public void run(float x, float y) { saveAll(); Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen()); } });
        }

        // ---- right: one card with the options of the chosen group
        clip(PG_X, PG_Y + 2, PG_W, PG_H - 4);
        int y = PG_Y + 6 - Math.round(pageScroll);
        int first = y;
        boolean firstRow = true;
        for (Setting st : m.settings) {
            if (!st.group.equals(pageGroup)) continue;
            firstRow = false;
            y = drawOptionRow(m, st, y, dt, live);
        }
        unclip();
        pageContentH = y - first + 12;
        pageTarget = Ui.clamp(pageTarget, 0f, Math.max(0f, pageContentH - PG_H));
        if (pageContentH > PG_H) {
            float kh = Math.max(24f, PG_H * PG_H / pageContentH);
            float ky = PG_Y + (pageScroll / (pageContentH - PG_H)) * (PG_H - kh);
            Gfx.rr(PG_X + PG_W - 8, Math.round(ky), 3, Math.round(kh), 2, 0x33FFFFFF);
        }
        Gfx.fade = fade;
    }

    private void rowBg(int y, int h) { Gfx.rr(PG_X + 4, y + 1, PG_W - 8, h - 5, 6, SURF); }

    private int drawOptionRow(final Module m, Setting st, int y, float dt, boolean live) {
        final int x = PG_X + PAD, w = PG_W - 2 * PAD;
        if (st instanceof Setting.Bool) {
            final Setting.Bool b = (Setting.Bool) st;
            float a = anim("pg" + m.id + st.id, b.value ? 1f : 0f, dt, 14f, b.value ? 1f : 0f);
            int rh = 46;
            rowBg(y, rh);
            Gfx.text(Gfx.trim(st.label(), 12, false, w - 70), x, y + (rh - Gfx.lineHeight(12, false)) / 2, 12, false, Theme.TEXT);
            int sx = x + w - 36;
            Gfx.rr(sx, y + (rh - 20) / 2, 36, 20, 6, Ui.mix(OFF, ACC, a));
            Gfx.rr(sx + 2 + Math.round(16 * a), y + (rh - 20) / 2 + 2, 16, 16, 4, Ui.mix(0xFF9A9BA3, ON_ACC, a));
            if (live) pageHit(x - 8, y, w + 16, rh, new Action() { @Override public void run(float px, float py) { b.value = !b.value; saveAll(); } }, null);
            return y + rh;
        }
        if (st instanceof Setting.Num) {
            final Setting.Num n = (Setting.Num) st;
            int rh = 62;
            rowBg(y, rh);
            final int trackX = x, trackW = w, ty = y + 38;
            Gfx.text(Gfx.trim(st.label(), 12, false, w - 100), x, y + 12, 12, false, Theme.TEXT);
            String val = n.text();
            int vw = Gfx.width(val, 11, true) + 16;
            Gfx.rr(x + w - vw, y + 8, vw, 22, 7, SURF3);
            Gfx.textCentered(val, x + w - vw / 2, y + 8 + (22 - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.TEXT);
            Gfx.rr(trackX, ty, trackW, 4, 2, OFF);
            int fw = Math.max(4, Math.round(trackW * n.fraction()));
            Gfx.rr(trackX, ty, fw, 4, 2, ACC);
            Gfx.rr(trackX + fw - 7, ty - 5, 14, 14, 7, 0xFFF5F5F5);
            if (live) {
                Action drag = new Action() { @Override public void run(float px, float py) { n.setFraction((px - trackX) / trackW); } };
                pageHit(x - 8, y + 26, w + 16, 30, drag, drag);
            }
            return y + rh;
        }
        if (st instanceof Setting.Choice) {
            final Setting.Choice c = (Setting.Choice) st;
            int total = 0;
            int[] ws = new int[c.options.length];
            for (int i = 0; i < ws.length; i++) { ws[i] = Gfx.width(Lang.t("opt." + c.options[i]), 11, false) + 22; total += ws[i]; }
            int lab = Gfx.width(st.label(), 12, false);
            boolean inline = total + 4 + lab + 24 <= w;
            int rh = inline ? 46 : 80;
            { int cx0 = inline ? x + w - total - 4 : x, extra = 0; for (int i = 0; i < ws.length; i++) { if (cx0 + ws[i] > x + w + 2) { cx0 = x; extra += 32; } cx0 += ws[i]; } rowBg(y, rh + extra); }
            int sy = inline ? y + (rh - 28) / 2 : y + 38, sx = inline ? x + w - total - 4 : x;
            Gfx.text(Gfx.trim(st.label(), 12, false, w), x, inline ? y + (rh - Gfx.lineHeight(12, false)) / 2 : y + 12, 12, false, Theme.TEXT);
            // segmented control (wraps to a second line when it does not fit)
            int cx = sx, cy = sy;
            Gfx.rr(sx - 2, sy - 2, Math.min(total, w) + 4, 32, 5, SURF3);
            for (int i = 0; i < ws.length; i++) {
                final int idx = i;
                if (cx + ws[i] > x + w + 2) { cx = x; cy += 32; Gfx.rr(x - 2, cy - 2, Math.min(total, w) + 4, 32, 5, SURF3); rh += 32; }
                boolean sel = c.index == i, cov = pageOver(cx, cy, ws[i], 28);
                if (sel) Gfx.rr(cx, cy, ws[i], 28, 7, 0xFF4A4B53);
                else if (cov) Gfx.rr(cx, cy, ws[i], 28, 7, 0xFF3A3B42);
                Gfx.textCentered(Lang.t("opt." + c.options[i]), cx + ws[i] / 2, cy + (28 - Gfx.lineHeight(11, sel)) / 2, 11, sel, sel ? Theme.TEXT : Theme.MUTED);
                if (live) pageHit(cx, cy, ws[i], 28, new Action() { @Override public void run(float px, float py) { c.index = idx; saveAll(); } }, null);
                cx += ws[i];
            }
            return y + rh;
        }
        if (st instanceof Setting.Pixels) {
            final Setting.Pixels pg = (Setting.Pixels) st;
            final int n = pg.n, cell = 18, gw = n * cell, gx = x + 6, gy = y + 58;
            int rh = 58 + gw + 24;
            rowBg(y, rh);
            Gfx.text(Gfx.trim(st.label(), 12, false, w), x, y + 12, 12, false, Theme.TEXT);
            Gfx.text(Gfx.trim(Lang.t("set.crosshair.hint"), 10, false, w), x, y + 12 + Gfx.lineHeight(12, false) + 3, 10, false, Theme.MUTED);
            Gfx.rr(gx - 6, gy - 6, gw + 12, gw + 12, 6, 0xFF0F1012);
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
                int cx = gx + j * cell, cy = gy + i * cell;
                boolean on = pg.cells[i * n + j], hv = pageOver(cx, cy, cell, cell), mid = i == n / 2 && j == n / 2;
                Gfx.rr(cx + 1, cy + 1, cell - 2, cell - 2, 3, on ? 0xFFF4F4F5 : (hv ? 0xFF4A4B53 : (mid ? 0xFF3A3B42 : 0xFF26272C)));
                if (mid && !on) Gfx.rr(cx + cell / 2 - 1, cy + cell / 2 - 1, 3, 3, 1, 0xFF6B6C75);
            }
            if (live) {
                final int fgx = gx, fgy = gy;
                Action click = new Action() { @Override public void run(float px, float py) {
                    int j = (int) Math.floor((px - fgx) / cell), i = (int) Math.floor((py - fgy) / cell);
                    if (i < 0 || j < 0 || i >= n || j >= n) return;
                    paint = !pg.cells[i * n + j];
                    pg.cells[i * n + j] = paint; saveAll();
                } };
                Action drag = new Action() { @Override public void run(float px, float py) {
                    int j = (int) Math.floor((px - fgx) / cell), i = (int) Math.floor((py - fgy) / cell);
                    if (i < 0 || j < 0 || i >= n || j >= n) return;
                    if (pg.cells[i * n + j] != paint) { pg.cells[i * n + j] = paint; saveAll(); }
                } };
                pageHit(gx, gy, gw, gw, click, drag);
            }
            // live preview + quick shapes
            int rx = gx + gw + 24, rw = x + w - rx;
            Gfx.text(Lang.t("set.crosshair.preview"), rx, gy - 6, 10, false, Theme.MUTED);
            Gfx.rr(rx, gy + 10, rw, 70, 6, 0xFF0F1012);
            dev.yaclient.modules.CrosshairModule cm = dev.yaclient.modules.CrosshairModule.instance;
            if (cm != null) cm.drawAt(rx + rw / 2, gy + 45, Math.min(3f, 62f / (dev.yaclient.modules.CrosshairModule.N * cm.pixelSize())));
            Gfx.text(Lang.t("set.crosshair.presets"), rx, gy + 92, 10, false, Theme.MUTED);
            String[] ids = {"cross", "plus", "dot", "ring", "square", "x"};
            int bw2 = (rw - 6) / 2;
            for (int k = 0; k < ids.length + 1; k++) {
                final String id = k < ids.length ? ids[k] : "clear";
                int bx = rx + (k % 2) * (bw2 + 6), by = gy + 108 + (k / 2) * 32;
                int bwk = (k == ids.length) ? rw : bw2;
                if (k == ids.length) bx = rx;
                boolean ov = pageOver(bx, by, bwk, 26);
                Gfx.rr(bx, by, bwk, 26, 4, ov ? SURF3 : SURF2);
                Gfx.textCentered(Lang.t("opt.cx_" + id), bx + bwk / 2, by + (26 - Gfx.lineHeight(10, false)) / 2, 10, false, ov ? Theme.TEXT : Theme.MUTED);
                if (live) pageHit(bx, by, bwk, 26, new Action() { @Override public void run(float px, float py) {
                    pg.set(id.equals("clear") ? new String(new char[n * n]).replace('\0', '0') : dev.yaclient.modules.CrosshairModule.preset(id)); saveAll(); } }, null);
            }
            return y + rh;
        }
        if (st instanceof Setting.Key) {
            final Setting.Key kk = (Setting.Key) st;
            int rh = 46;
            rowBg(y, rh);
            boolean cap = focusKey == kk;
            Gfx.text(Gfx.trim(st.label(), 12, false, w - 150), x, y + (rh - Gfx.lineHeight(12, false)) / 2, 12, false, Theme.TEXT);
            String val = cap ? Lang.t("ui.press_key") : kk.text();
            int vw = Math.max(70, Gfx.width(val, 11, true) + 24);
            int bx = x + w - vw, by = y + (rh - 26) / 2;
            Gfx.rr(bx, by, vw, 26, 7, cap ? 0xFF4A4B53 : SURF3);
            Gfx.rrOutline(bx, by, vw, 26, 7, 1, cap ? 0x88FFFFFF : 0x1AFFFFFF);
            Gfx.textCentered(val, bx + vw / 2, by + (26 - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.TEXT);
            if (live) pageHit(bx, by, vw, 26, new Action() { @Override public void run(float px, float py) { focusKey = kk; focusText = null; } }, null);
            return y + rh;
        }
        if (st instanceof Setting.Text) {
            final Setting.Text tx = (Setting.Text) st;
            int rh = 62;
            rowBg(y, rh);
            boolean foc = focusText == tx;
            Gfx.text(Gfx.trim(st.label(), 12, false, w), x, y + 12, 12, false, Theme.TEXT);
            int bx = x, by = y + 32, bw = w, bh = 24;
            Gfx.rr(bx, by, bw, bh, 4, foc ? 0x22FFFFFF : 0x12FFFFFF);
            Gfx.rrOutline(bx, by, bw, bh, 4, 1, foc ? 0x88FFFFFF : 0x1AFFFFFF);
            String shown = tx.value;
            Gfx.text(Gfx.trim(shown, 12, false, bw - 24), bx + 10, by + (bh - Gfx.lineHeight(12, false)) / 2, 12, false, Theme.TEXT);
            if (foc && (System.currentTimeMillis() / 500) % 2 == 0) {
                int cw = Gfx.width(Gfx.trim(shown, 12, false, bw - 24), 12, false);
                Gfx.rr(bx + 11 + cw, by + 5, 1, bh - 10, 0, Theme.TEXT);
            }
            if (live) pageHit(bx, by, bw, bh, new Action() { @Override public void run(float px, float py) { focusText = tx; } }, null);
            return y + rh;
        }
        if (st instanceof Setting.Palette) {
            final Setting.Palette pal = (Setting.Palette) st;
            int rh = 78;
            rowBg(y, rh);
            Gfx.text(Gfx.trim(st.label(), 12, false, w - 120), x, y + 12, 12, false, Theme.TEXT);
            String cn = Lang.t("opt." + Setting.Palette.IDS[pal.index]);
            Gfx.text(cn, x + w - Gfx.width(cn, 11, false), y + 13, 11, false, Theme.MUTED);
            int sy = y + 40, step = w / Setting.Palette.IDS.length;
            for (int i = 0; i < Setting.Palette.IDS.length; i++) {
                final int idx = i;
                int sx = x + i * step + (step - 24) / 2;
                boolean sel = pal.index == i, cov = pageOver(sx, sy, 24, 24);
                if (sel) Gfx.rrOutline(sx - 4, sy - 4, 32, 32, 9, 2, 0xFFF5F5F5);
                else if (cov) Gfx.rrOutline(sx - 3, sy - 3, 30, 30, 8, 1, 0x77FFFFFF);
                Gfx.rr(sx, sy, 24, 24, 7, Setting.Palette.color(i));
                if (live) pageHit(sx - 4, sy - 4, 32, 32, new Action() { @Override public void run(float px, float py) { pal.index = idx; saveAll(); } }, null);
            }
            return y + rh;
        }
        return y;
    }

    // ----- cosmetics (locker) -----
    private static final String[] COS_CATS = {"bundle", "cape", "wings", "bandana", "pet", "partner"};
    private int cosCat = 0;

    private static String cosIcon(String slotOrBundle) {
        if (slotOrBundle.equals("bundle")) return "gift";
        if (slotOrBundle.equals("partner")) return "star";
        return slotOrBundle;
    }

    /** Spinning preview of the player with the equipped cosmetics (the same render layer as in game). */
    private void drawPlayerPreview(int cx, int baseY, int size) {
        drawPlayer(cx, baseY, size, (System.currentTimeMillis() % 11000L) / 11000f * 360f, null);
    }

    /** Draws the local player (own skin) at a fixed yaw, optionally wearing exactly the cosmetics in {@code override}. */
    private void drawPlayer(int cx, int baseY, int size, float spin, java.util.Map<String, String> override) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer ent = mc.thePlayer;
        if (ent == null) return;
        // the menu drawing leaves a tint, blending and depth state behind: start from an opaque, untinted state
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) cx, (float) baseY, 50.0F);
        GlStateManager.scale((float) (-size), (float) size, (float) size);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float ro = ent.renderYawOffset, pro = ent.prevRenderYawOffset, ry = ent.rotationYaw, pry = ent.prevRotationYaw,
                rp = ent.rotationPitch, prh = ent.prevRotationYawHead, rh = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        ent.renderYawOffset = spin; ent.prevRenderYawOffset = spin;
        ent.rotationYaw = spin; ent.prevRotationYaw = spin;
        ent.rotationYawHead = spin; ent.prevRotationYawHead = spin;
        ent.rotationPitch = 0f;
        RenderManager rm = mc.getRenderManager();
        rm.setPlayerViewY(180.0F);
        rm.setRenderShadow(false);
        CosmeticsApi.previewOverride = override;
        try { rm.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F); } finally { CosmeticsApi.previewOverride = null; }
        rm.setRenderShadow(true);
        ent.renderYawOffset = ro; ent.prevRenderYawOffset = pro;
        ent.rotationYaw = ry; ent.prevRotationYaw = pry;
        ent.rotationPitch = rp; ent.prevRotationYawHead = prh; ent.rotationYawHead = rh;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    /** Accent colour of each bundle (same as the website cards). */
    private static final int[] BCOL = {0xFFE10600, 0xFFE4E4E8, 0xFF9FD8FF, 0xFFFF86B6, 0xFF8C6CFF, 0xFFFF9B3D, 0xFFFF4040, 0xFF6EC6FF, 0xFFFFD21F, 0xFF3DDC6A};

    /** One locker card picture: the player (own skin) wearing the item, or the whole bundle, swaying gently. */
    private void drawWornCard(String key, int bi, boolean bundle, int cx, int baseY, int size) { drawWornCard(key, bi, bundle, null, cx, baseY, size); }

    private void drawWornCard(String key, int bi, boolean bundle, String single, int cx, int baseY, int size) {
        java.util.Map<String, String> ov = new java.util.HashMap<String, String>();
        for (String sl : Cosmetics.SLOTS) ov.put(sl, "none");
        if (single != null) ov.put(key, single);
        else if (bundle) { for (String sl : Cosmetics.SLOTS) ov.put(sl, Cosmetics.item(bi, sl)); }
        else ov.put(key, Cosmetics.item(bi, key));
        ov.put("plus", CosmeticsApi.plus() ? "1" : "0");
        boolean front = key.equals("bandana") || key.equals("pet");
        float base = front ? 335f : 150f;
        float sway = (float) Math.sin(System.currentTimeMillis() / 1100.0) * 14f;
        drawPlayer(cx, baseY, size, base + sway, ov);
    }

    private float drawCosmetics() {
        int pw = 176;
        // left: player preview
        Gfx.rr(CX, VY, pw, VH, 6, SURF);
        Gfx.rrOutline(CX, VY, pw, VH, 6, 1, 0x14FFFFFF);
        Gfx.textCentered(Lang.t("cos.loadout"), CX + pw / 2, VY + 10, 11, true, Theme.MUTED);
        boolean plusOn = CosmeticsApi.plus();
        String plusLab = Lang.t(plusOn ? "cos.plus.on" : "cos.plus.off");
        int plw = Gfx.width(plusLab, 9, true) + 18;
        Gfx.rr(CX + (pw - plw) / 2, VY + 28, plw, 16, 4, plusOn ? Theme.accent() : 0x18FFFFFF);
        Gfx.text(plusLab, CX + (pw - plw) / 2 + 9, VY + 28 + (16 - Gfx.lineHeight(9, true)) / 2, 9, true, plusOn ? Theme.onAccent() : Theme.MUTED);
        drawPlayerPreview(CX + pw / 2, VY + VH - 40, 92);

        int x0 = CX + pw + 16, wRight = VW - pw - 16;

        // category pills
        int px = x0;
        for (int i = 0; i < COS_CATS.length; i++) {
            final int idx = i;
            String lab = Lang.t("cos.cat." + COS_CATS[i]);
            int w = Gfx.width(lab, 10, true) + 40;
            boolean on = cosCat == i, ov = over(px, VY, w, 26);
            Gfx.rr(px, VY, w, 26, 4, on ? SURF3 : (ov ? SURF2 : SURF));
            Gfx.icon(cosIcon(COS_CATS[i]), px + 9, VY + 6, 14, on ? Theme.TEXT : Theme.MUTED);
            Gfx.text(lab, px + 28, VY + (26 - Gfx.lineHeight(10, true)) / 2, 10, true, on ? Theme.TEXT : Theme.MUTED);
            hit(px, VY, w, 26, new Action() {
                @Override public void run(float mx, float my) { cosCat = idx; scrollTarget = 0f; scroll = 0f; }
            }, null, null);
            px += w + 6;
        }

        // cards: bundles first, or the items of one slot (from every bundle)
        List<String> entries = new ArrayList<String>();
        boolean partnerTab = COS_CATS[cosCat].equals("partner");
        for (int bi = 0; bi < Cosmetics.BUNDLES.length; bi++) {
            if (partnerTab) { if (Cosmetics.isPartner(bi)) entries.add("bundle:" + bi); continue; }
            if (Cosmetics.isPartner(bi)) continue;          // partner bundles live in their own tab
            if (cosCat == 0) entries.add("bundle:" + bi);
            else entries.add(COS_CATS[cosCat] + ":" + bi);
        }
        if (!partnerTab && cosCat > 0) {          // single items of this slot (not part of any bundle)
            String suffix = "_" + COS_CATS[cosCat];
            for (String ex : Cosmetics.EXTRAS) if (ex.endsWith(suffix)) entries.add("x:" + COS_CATS[cosCat] + ":" + ex);
        }
        if (partnerTab) Gfx.text(Lang.t("cos.partner_hint"), x0, VY + 36, 10, false, Theme.MUTED);
        int gap = 10, cols = 3, cw = (wRight - gap * (cols - 1)) / cols, ch = 206;
        int gy = VY + (partnerTab ? 54 : 40);
        final int footY = VY + VH - 26;
        final int gTop = gy - 2, gBot = footY - 8, gH = gBot - gTop;
        int rows = (entries.size() + cols - 1) / cols;
        int gridH = rows * (ch + gap) - gap;
        int gScroll = Math.round(scroll);
        clip(x0 - 2, gTop, wRight + 4, gH);
        for (int n = 0; n < entries.size(); n++) {
            final String[] kv = entries.get(n).split(":");
            final boolean single = kv[0].equals("x");
            final String key = single ? kv[1] : kv[0];
            final int bi = single ? 0 : Integer.parseInt(kv[1]);
            int cx = x0 + (n % cols) * (cw + gap), cy = gy - gScroll + (n / cols) * (ch + gap);
            if (cy + ch < gTop || cy > gBot) continue;
            final boolean bundle = !single && key.equals("bundle");
            final String id = single ? kv[2] : (bundle ? Cosmetics.BUNDLES[bi] : Cosmetics.item(bi, key));
            boolean has, on;
            if (bundle) {
                has = Cosmetics.ownsBundle(bi);
                on = has && Cosmetics.wearsBundle(bi);
            } else {
                has = CosmeticsApi.owned(id);
                on = has && Cosmetics.selected(key).equals(id);
            }
            boolean ov = over(cx, cy, cw, ch) && myl >= gTop && myl < gBot;
            Gfx.rr(cx, cy, cw, ch, 7, on ? Ui.alpha(Theme.accent(), 0.16f) : (ov ? 0x24FFFFFF : 0x1AFFFFFF));
            Gfx.rrOutline(cx, cy, cw, ch, 7, 1, on ? Theme.accent() : 0x30FFFFFF);
            // tag band (FREE / EXCLUSIVE / lock) above the thumbnail, so nothing is drawn over the preview
            int band = 18, th = 130, ty = cy + band + 4;
            if (bundle) {
                String fr = Cosmetics.isPartner(bi) ? Lang.t("cos.partner_tag") + " " + Cosmetics.PARTNER_NAME[bi] : Lang.t(bi == 0 ? "cos.free" : "cos.exclusive");
                int fw = Gfx.width(fr, 9, true) + 12;
                Gfx.rr(cx + 8, cy + 6, fw, 15, 7, bi == 0 ? Theme.accent() : 0xFFF4F4F5);
                Gfx.text(fr, cx + 14, cy + 6 + (15 - Gfx.lineHeight(9, true)) / 2, 9, true, bi == 0 ? Theme.onAccent() : 0xFF101014);
            }
            if (!has) Gfx.icon("lock", cx + cw - 24, cy + 6, 14, Theme.MUTED);
            // thumbnail (3D preview is clipped to its box)
            Gfx.rr(cx + 6, ty, cw - 12, th, 5, 0x22000000);
            Gfx.rr(cx + 6, ty, cw - 12, th, 5, Ui.alpha(single ? 0xFFB8B8C8 : BCOL[bi], 0.16f));
            float ps = key.equals("cape") ? 56f : key.equals("wings") ? 34f : key.equals("bandana") ? 90f : key.equals("pet") ? 90f : 30f;
            int cy0 = Math.max(ty, gTop), cy1 = Math.min(ty + th, gBot);
            if (cy1 > cy0) {
            clip(cx + 6, cy0, cw - 12, cy1 - cy0);
            if (Minecraft.getMinecraft().thePlayer != null) {
                drawWornCard(key, bi, bundle, single ? id : null, cx + cw / 2, ty + th - 8, 56);
            } else {   // no world yet (title screen): standalone preview of the item
                CosmeticPreview.draw(key, single ? id.substring(0, id.lastIndexOf('_')) : Cosmetics.PREFIX[bi], cx + cw / 2f, ty + th / 2f + 3f, ps, System.currentTimeMillis(), CosmeticsApi.plus());
            }
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            clip(x0 - 2, gTop, wRight + 4, gH);
            }
            String name = Lang.t(bundle ? "cos.bundle." + id : "cos.item." + id);
            Gfx.text(Gfx.trim(name, 11, true, cw - 16), cx + 8, ty + th + 7, 11, true, Theme.TEXT);
            String st = on ? Lang.t("cos.equipped") : (has ? Lang.t("cos.click_equip") : Lang.t(CosmeticsApi.premium() ? "cos.get" : "cos.premium_only"));
            if (on) {
                Gfx.rr(cx + 6, cy + ch - 24, cw - 12, 18, 7, Ui.alpha(Theme.accent(), 0.30f));
                Gfx.icon("check", cx + 10, cy + ch - 23, 16, Theme.TEXT);
                Gfx.text(st, cx + 28, cy + ch - 24 + (18 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
            } else {
                Gfx.text(Gfx.trim(st, 10, false, cw - 16), cx + 8, cy + ch - 24 + (18 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.MUTED);
            }
            final boolean fHas = has;
            int hy0 = Math.max(cy, gTop), hy1 = Math.min(cy + ch, gBot);
            if (hy1 > hy0) hit(cx, hy0, cw, hy1 - hy0, new Action() {
                @Override public void run(float mx, float my) {
                    if (!fHas) { if (!CosmeticsApi.premium()) return; if (bundle) CosmeticsApi.openBundle(Cosmetics.BUNDLES[bi]); else CosmeticsApi.openItem(id); return; }
                    if (bundle) Cosmetics.toggleBundle(bi); else if (single) Cosmetics.toggleItem(key, id); else Cosmetics.toggle(bi, key);
                }
            }, null, null);
        }

        unclip();
        // footer: sync status + store button
        Minecraft mc = Minecraft.getMinecraft();
        int fy = footY;
        String stKey;
        if (!CosmeticsApi.enabled()) stKey = "cos.dev_mode";
        else if (CosmeticsApi.status().isEmpty()) stKey = "cos.syncing";
        else stKey = "cos.status." + CosmeticsApi.status();
        String store = Lang.t("cos.store");
        int sw = Gfx.width(store, 11, true) + 40;
        Gfx.text(Gfx.trim(Lang.t(stKey) + (CosmeticsApi.enabled() && !CosmeticsApi.detail().isEmpty() ? " [" + CosmeticsApi.detail() + "]" : ""), 10, false, wRight - sw - 12), x0, fy + (26 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.MUTED);
        if (CosmeticsApi.enabled()) {
            int bx = x0 + wRight - sw;
            boolean ov = over(bx, fy, sw, 26);
            Gfx.rr(bx, fy, sw, 26, 5, ov ? Ui.mix(Theme.accent(), 0xFFFFFFFF, 0.15f) : Theme.accent());
            Gfx.icon("cart", bx + 11, fy + 5, 16, Theme.onAccent());
            Gfx.text(store, bx + 31, fy + (26 - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.onAccent());
            hit(bx, fy, sw, 26, new Action() {
                @Override public void run(float mx, float my) { CosmeticsApi.openStore(); }
            }, null, null);
        }
        return VH + Math.max(0, gridH - gH);
    }

    // ----- global settings -----
    private int section(String key, int y) {
        Gfx.text(Lang.t(key), CX, y, 13, true, Theme.MUTED);
        return y + 24;
    }

    private float drawSettings(int top, float dt) {
        int y = top;

        // language
        y = section("settings.language", y);
        int cw = (VW - 20) / 3;
        for (int i = 0; i < Lang.CODES.length; i++) {
            final String code = Lang.CODES[i];
            int x = CX + (i % 3) * (cw + 10), yy = y + (i / 3) * 48;
            boolean sel = Lang.code().equals(code);
            boolean ov = overView(x, yy, cw, 40);
            Gfx.rr(x, yy, cw, 40, 6, sel ? Ui.alpha(Theme.accent(), 0.18f) : (ov ? 0x1AFFFFFF : 0x0FFFFFFF));
            Gfx.rrOutline(x, yy, cw, 40, 6, 1, sel ? Theme.accent() : 0x14FFFFFF);
            Gfx.icon("globe", x + 14, yy + 11, 18, sel ? Theme.accent() : Theme.MUTED);
            Gfx.text(Lang.NAMES[i], x + 42, yy + (40 - Gfx.lineHeight(12, true)) / 2, 12, true, sel ? Theme.TEXT : Theme.MUTED);
            if (sel) Gfx.icon("check", x + cw - 30, yy + 11, 18, Theme.accent());
            hitView(x, yy, cw, 40, new Action() {
                @Override public void run(float px, float py) { Lang.load(code); Config.save(); }
            }, null, null);
        }
        y += ((Lang.CODES.length + 2) / 3) * 48 + 14;

        // HUD
        y = section("settings.hud", y);
        Gfx.text(Lang.t("settings.hud_scale"), CX, y, 12, false, Theme.TEXT);
        String val = Math.round(Hud.globalScale() * 100f) + "%";
        Gfx.text(val, CX + VW - Gfx.width(val, 12, true), y, 12, true, Theme.MUTED);
        y += 24;
        final int tx = CX, tw = VW;
        Gfx.rr(tx, y, tw, 6, 3, 0x26FFFFFF);
        float frac = (Hud.globalScale() - 0.5f) / 1.5f;
        int fw = Math.max(6, Math.round(tw * frac));
        Gfx.rr(tx, y, fw, 6, 3, Theme.accent());
        Gfx.rr(tx + fw - 7, y - 4, 14, 14, 7, 0xFFF5F5F5);
        Action drag = new Action() {
            @Override public void run(float px, float py) {
                Hud.setGlobalScale(0.5f + Ui.clamp((px - tx) / (float) tw, 0f, 1f) * 1.5f);
            }
        };
        hitView(tx - 6, y - 10, tw + 12, 26, drag, drag, null);
        y += 30;

        int bw = (VW - 10) / 2;
        button(Lang.t("settings.edit_hud"), "move", CX, y, bw, true, new Action() {
            @Override public void run(float px, float py) { Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen()); }
        });
        button(Lang.t("settings.reset_hud"), "reset", CX + bw + 10, y, bw, false, new Action() {
            @Override public void run(float px, float py) { Hud.resetLayout(); }
        });
        y += 44 + 20;

        // about
        y = section("settings.about", y);
        Gfx.text("CLIENT - Minecraft 1.8.9", CX, y, 12, false, Theme.TEXT);
        y += 18;
        Gfx.text(Gfx.trim(Lang.t("settings.config_hint"), 10, false, VW), CX, y, 10, false, Theme.MUTED);
        y += 30;
        return y - top;
    }

    private void button(String label, String icon, int x, int y, int w, boolean primary, Action act) {
        boolean ov = overView(x, y, w, 36);
        int fill = primary ? (ov ? Ui.mix(Theme.accent(), 0xFFFFFFFF, 0.15f) : Theme.accent()) : (ov ? 0x24FFFFFF : 0x12FFFFFF);
        int fg = primary ? Theme.onAccent() : Theme.TEXT;
        Gfx.rr(x, y, w, 36, 6, fill);
        if (!primary) Gfx.rrOutline(x, y, w, 36, 6, 1, 0x1AFFFFFF);
        int lw = Gfx.width(label, 12, true);
        int ix = x + (w - lw - 26) / 2;
        Gfx.icon(icon, ix, y + 9, 18, fg);
        Gfx.text(label, ix + 26, y + (36 - Gfx.lineHeight(12, true)) / 2, 12, true, fg);
        hitView(x, y, w, 36, act, null, null);
    }

    // ================= input (no "throws": compatible with any mapping) =================
    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (focusKey != null && button >= 2) { focusKey.code = -100 + button; focusKey = null; saveAll(); return; }
        if (button == 0 || button == 1) focusKey = null;
        float x = lx(mx), y = ly(my);
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.contains(x, y)) continue;
            if (button == 0) {
                searchFocus = false;
                if (h.click != null) h.click.run(x, y);
                if (h.drag != null) { activeDrag = h.drag; h.drag.run(x, y); }
            } else if (button == 1 && h.right != null) {
                h.right.run(x, y);
            }
            return;
        }
        searchFocus = false;
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long time) {
        if (activeDrag != null) activeDrag.run(lx(mx), ly(my));
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        if (activeDrag != null) {
            activeDrag = null;
            saveAll();
        }
    }

    @Override
    public void handleMouseInput() {
        int d = Mouse.getEventDWheel();
        if (d != 0 && drawerModule != null) pageTarget = Ui.clamp(pageTarget - Math.signum(d) * 48f, 0f, Math.max(0f, pageContentH - PG_H));
        else if (d != 0) scrollTarget = Ui.clamp(scrollTarget - Math.signum(d) * 48f, 0f, Math.max(0f, contentH - VH));
        try { super.handleMouseInput(); } catch (Exception ignored) { }
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (focusKey != null) {
            if (key != Keyboard.KEY_ESCAPE) { focusKey.code = key == Keyboard.KEY_DELETE ? 0 : key; saveAll(); }
            focusKey = null;
            return;
        }
        if (focusText != null) {
            if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) { focusText = null; saveAll(); return; }
            if (key == Keyboard.KEY_BACK) {
                if (!focusText.value.isEmpty()) focusText.value = focusText.value.substring(0, focusText.value.length() - 1);
            } else if (typed >= 32 && typed != 127 && focusText.value.length() < focusText.maxLen) {
                focusText.value += typed;
            }
            saveAll();
            return;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            if (searchFocus) { searchFocus = false; return; }
            if (drawerModule != null) { drawerModule = null; return; }
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }
        if (searchFocus && moduleTab()) {
            if (key == Keyboard.KEY_BACK) {
                if (!query.isEmpty()) query = query.substring(0, query.length() - 1);
            } else if (typed >= 32 && typed != 127 && query.length() < 24) {
                query += typed;
            }
            scrollTarget = 0f;
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

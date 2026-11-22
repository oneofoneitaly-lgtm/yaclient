package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Mod menu, same look as the 1.8.9 client: glass panel, segmented tabs, search, module cards and a HUD editor. */
public class MenuScreen extends Screen {
    private static final int PW = 780, PH = 450;
    private static final int CX = 24, VY = 124, VW = PW - 48, VH = PH - VY - 18;
    private static final int PANEL = 0x700F1012, SURF = 0x781A1B1F, SURF2 = 0x84222328, SURF3 = 0x962D2E34;
    private static final int ACC = 0xFFE8E8E8, ON_ACC = 0xFF101014, OFF = 0xFF3A3B42;
    private static final String[] TABS = {"All", "Info", "PvP", "Crystal", "Utility", "Cosmetics"};
    private static final int COS = 5;
    private static final String[] TAB_ICONS = {"grid", "info", "sword", "target_hud", "wrench", "shirt"};
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("yaclient", "textures/gui/logo.png");

    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int tab;
    private String query = "";
    private boolean searchFocus, editing;
    private float scroll, scrollTarget, contentH, scale, viewH;
    private int originX, originY;
    private float mxl, myl;
    private final Map<String, Float> anims = new HashMap<String, Float>();
    private HudModule dragging;
    private float dragDX, dragDY;
    private float dtMs;
    private float curSc = 0.6f;

    // options page
    private interface Act { void run(float x, float y); }

    private static final class Hit {
        final int x, y, w, h;
        final Act click, drag;
        Hit(int x, int y, int w, int h, Act click, Act drag) { this.x = x; this.y = y; this.w = w; this.h = h; this.click = click; this.drag = drag; }
    }

    private static final int PG_X = 284, PG_Y = 84, PG_W = PW - 284 - 24, PG_H = PH - 84 - 20, PAD = 20;
    private final List<Hit> hits = new ArrayList<Hit>();
    private Act activeDrag;
    private Module pageFor;
    private String pageGroup;
    private float pageScroll, pageTarget, pageContentH;

    public MenuScreen() { super(Component.literal("YA CLIENT")); }

    public MenuScreen(int startTab, boolean editor) {
        this();
        this.tab = startTab;
        this.editing = editor;
        if (startTab == COS) CosmeticsApi.refreshAsync(false);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) { }

    private float anim(String key, float target, float speed) {
        float v = anims.containsKey(key) ? anims.get(key) : target;
        v = Ui.approach(v, target, dtMs, speed);
        anims.put(key, v);
        return v;
    }

    private boolean over(int x, int y, int w, int h) { return mxl >= x && mxl < x + w && myl >= y && myl < y + h; }

    private List<Module> visible() {
        List<Module> out = new ArrayList<Module>();
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (Module m : Modules.ALL) {
            if (tab == COS) continue;
            if (tab > 0 && m.category.ordinal() != tab - 1) continue;
            if (!q.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(q) && !m.description.toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        dtMs = Math.min(100, now - last);
        last = now;
        Gfx.g = g;
        int gs = Math.max(1, mc.getWindow().getGuiScale());
        if (editing) { renderEditor(g, mc, mx, my, gs); return; }

        float fade = 0.05f + 0.95f * Ui.ease((now - opened) / 200f);
        float open = Ui.ease((now - opened) / 240f);
        // same physical size on every GUI scale
        float phys = Math.max(0.5f, mc.getWindow().getHeight() / 1080f * 1.3f);
        scale = Math.min(phys / gs, Math.min((height - 16f) / PH, (width - 16f) / PW));
        float sc = scale * (0.95f + 0.05f * open);
        curSc = sc;
        Gfx.setScale(Math.max(1, Math.round(2f * gs * sc)));
        originX = Math.round(width / 2f - PW * scale / 2f);
        originY = Math.round(height / 2f - PH * scale / 2f);
        mxl = (mx - width / 2f) / sc + PW / 2f;
        myl = (my - height / 2f) / sc + PH / 2f;
        scroll = Ui.approach(scroll, scrollTarget, dtMs, 16f);

        g.fill(0, 0, width, height, Ui.alpha(0x40000000, fade));
        g.pose().pushMatrix();
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(sc, sc);
        g.pose().translate(-PW / 2f, -PH / 2f);
        Gfx.fade = fade;

        for (int i = 5; i >= 1; i--) Gfx.rr(-i * 3, -i * 3 + 10, PW + i * 6, PH + i * 6, 18 + i * 3, 0x12000000);
        Gfx.rr(0, 0, PW, PH, 10, PANEL);
        Gfx.rrOutline(0, 0, PW, PH, 10, 1, 0x24FFFFFF);
        hits.clear();
        if (pageFor != null) {
            drawPage(g, mc, gs, sc);
        } else {
            drawTabs();
            drawHeader(g);
            if (tab == COS) drawCosmetics(g); else drawBody(g, fade);
        }

        Gfx.fade = 1f;
        g.pose().popMatrix();
    }

    private void drawTabs() {
        int[] ws = new int[TABS.length];
        int total = 0;
        for (int i = 0; i < TABS.length; i++) { ws[i] = Gfx.width(TABS[i], 12, true) + 46; total += ws[i]; }
        int y = 70, x = CX;
        Gfx.rr(x, y, total + 8, 38, 6, SURF);
        int px = x + 4;
        for (int i = 0; i < TABS.length; i++) {
            boolean active = tab == i;
            float h = anim("nav" + i, active ? 1f : (over(px, y + 4, ws[i], 30) ? 0.5f : 0f), 16f);
            if (h > 0.01f) Gfx.rr(px, y + 4, ws[i], 30, 4, Ui.alpha(active ? SURF3 : SURF2, Math.min(1f, h)));
            int fg = active ? Theme.TEXT : Ui.mix(Theme.MUTED, Theme.TEXT, h);
            Gfx.icon(TAB_ICONS[i], px + 13, y + 4 + 6, 18, fg);
            Gfx.text(TABS[i], px + 36, y + 4 + (30 - Gfx.lineHeight(12, true)) / 2, 12, true, fg);
            px += ws[i];
        }
        String lab = "HUD editor";
        int w = Gfx.width(lab, 11, false) + 48, bx = PW - 24 - w;
        boolean ov = over(bx, y, w, 38);
        Gfx.rr(bx, y, w, 38, 6, ov ? SURF2 : SURF);
        Gfx.icon("move", bx + 14, y + 10, 18, ov ? Theme.TEXT : Theme.MUTED);
        Gfx.text(lab, bx + 38, y + (38 - Gfx.lineHeight(11, false)) / 2, 11, false, ov ? Theme.TEXT : Theme.MUTED);
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO, 24, 14, 0f, 0f, 38, 38, 128, 128, 128, 128, Ui.alpha(0xFFFFFFFF, Gfx.fade));
        int ls = 18;
        Gfx.text("CLIENT", 70, 16, ls, true, Theme.BRAND_RED);
        int total = visible().size(), on = 0;
        for (Module m : visible()) if (m.enabled) on++;
        String sub = tab == COS ? "Your cosmetics" : total + " mods  -  " + on + " on";
        Gfx.text(sub, 70, 16 + Gfx.lineHeight(ls, true) + 2, 10, false, Theme.MUTED);

        if (tab == COS) return;
        final int x = PW - 24 - 230, y = 18, w = 230, h = 34;
        boolean ov = over(x, y, w, h);
        Gfx.rr(x, y, w, h, 6, searchFocus ? SURF3 : (ov ? SURF2 : SURF));
        if (searchFocus) Gfx.rrOutline(x, y, w, h, 6, 1, 0x66FFFFFF);
        Gfx.icon("search", x + 11, y + (h - 16) / 2, 16, Theme.MUTED);
        int ty = y + (h - Gfx.lineHeight(12, false)) / 2;
        if (query.isEmpty()) Gfx.text("Search mods...", x + 34, ty, 12, false, Theme.DIM);
        else Gfx.text(Gfx.trim(query, 12, false, w - 46), x + 34, ty, 12, false, Theme.TEXT);
        if (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cx = x + 34 + (query.isEmpty() ? 0 : Math.min(w - 46, Gfx.width(query, 12, false)));
            g.fill(cx + 1, ty, cx + 2, ty + Gfx.lineHeight(12, false), Ui.alpha(0xFFFFFFFF, Gfx.fade));
        }
    }

    private void drawBody(GuiGraphicsExtractor g, float fade) {
        List<Module> list = visible();
        int gap = 12, cw = (VW - 2 * gap) / 3, ch = 120;
        contentH = list.isEmpty() ? 0 : ((list.size() + 2) / 3) * (ch + gap);
        viewH = VH;
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - VH));
        g.enableScissor(CX - 6, VY - 2, CX + VW + 6, VY + VH + 2);
        if (list.isEmpty()) {
            Gfx.textCentered("No mods found", CX + VW / 2, VY + 70, 13, false, Theme.MUTED);
        }
        int top = VY - Math.round(scroll);
        for (int i = 0; i < list.size(); i++) {
            int x = CX + (i % 3) * (cw + gap), y = top + (i / 3) * (ch + gap);
            if (y + ch < VY - 2 || y > VY + VH + 2) continue;
            drawCard(list.get(i), x, y, cw, ch);
        }
        g.disableScissor();
        if (contentH > VH) {
            float kh = Math.max(24f, VH * VH / contentH);
            float ky = VY + (scroll / (contentH - VH)) * (VH - kh);
            Gfx.rr(PW - 12, Math.round(ky), 4, Math.round(kh), 2, 0x33FFFFFF);
        }
    }

    private void drawCard(Module m, int x, int y, int cw, int ch) {
        boolean inView = myl >= VY && myl < VY + VH;
        float h = anim("h" + m.id, inView && over(x, y, cw, ch) ? 1f : 0f, 16f);
        float a = anim("e" + m.id, m.enabled ? 1f : 0f, 14f);
        Gfx.rr(x, y, cw, ch, 6, Ui.mix(SURF, SURF2, h));
        Gfx.rrOutline(x, y, cw, ch, 6, 1, Ui.mix(0x14FFFFFF, 0x30FFFFFF, h));
        Gfx.rr(x + 14, y + 14, 36, 36, 6, Ui.mix(SURF3, 0xFF3A3B42, Math.max(a * 0.7f, h * 0.5f)));
        Gfx.icon(m.icon(), x + 14 + 8, y + 14 + 8, 20, Ui.mix(Theme.MUTED, Theme.TEXT, Math.max(a, h)));
        boolean lockedPlus = m.plusOnly && !CosmeticsApi.plus();
        if (m.plusOnly) {
            int tw = Gfx.width("PLUS", 8, true) + 12;
            Gfx.rr(x + cw - 14 - tw, y + 14, tw, 16, 4, 0xFF3A3220);
            Gfx.textCentered("PLUS", x + cw - 14 - tw / 2, y + 14 + (16 - Gfx.lineHeight(8, true)) / 2, 8, true, 0xFFE0B040);
        }
        Gfx.text(Gfx.trim(m.name, 14, true, cw - 74 - (m.plusOnly ? 44 : 0)), x + 60, y + 14 + (36 - Gfx.lineHeight(14, true)) / 2, 14, true, Theme.TEXT);
        Gfx.text(Gfx.trim(m.description, 10, false, cw - 28), x + 14, y + 58, 10, false, Theme.MUTED);
        int sx = x + cw - 14 - 38, sy = y + ch - 14 - 26 + 3;
        if (lockedPlus) { Gfx.rr(sx - 22, sy, 60, 20, 6, 0xFF3A3220); Gfx.textCentered("Get Plus", sx + 8, sy + (20 - Gfx.lineHeight(9, true)) / 2, 9, true, 0xFFE0B040); }
        else {
            Gfx.rr(sx, sy, 38, 20, 6, Ui.mix(OFF, ACC, a));
            Gfx.rr(sx + 2 + Math.round(18 * a), sy + 2, 16, 16, 4, Ui.mix(0xFF9A9BA3, ON_ACC, a));
        }
        int bx = x + 14, by = y + ch - 14 - 26, bw = Gfx.width("Options", 10, true) + 30;
        boolean gear = !m.settings.isEmpty();
        boolean oo = gear && inView && over(bx, by, bw, 26);
        Gfx.rr(bx, by, bw, 26, 4, gear ? (oo ? SURF3 : 0xFF26272C) : 0xFF1F2024);
        Gfx.textCentered("Options", bx + bw / 2, by + (26 - Gfx.lineHeight(10, true)) / 2, 10, true, gear ? Theme.TEXT : Theme.DIM);
    }


    // ---------- options page ----------
    private List<String> groupsOf(Module m) {
        List<String> out = new ArrayList<String>();
        for (String g : new String[]{"general", "style", "text", "colors", "layout"})
            for (Setting st : m.settings) if (st.group.equals(g)) { out.add(g); break; }
        for (Setting st : m.settings) if (!out.contains(st.group)) out.add(st.group);
        return out;
    }

    private void hit(int x, int y, int w, int h, Act click, Act drag) { hits.add(new Hit(x, y, w, h, click, drag)); }

    private void pageHit(int x, int y, int w, int h, Act click, Act drag) {
        int y0 = Math.max(y, PG_Y + 4), y1 = Math.min(y + h, PG_Y + PG_H - 4);
        if (y1 > y0) hits.add(new Hit(x, y0, w, y1 - y0, click, drag));
    }

    private boolean pageOver(int x, int y, int w, int h) { return over(x, y, w, h) && myl >= PG_Y + 4 && myl < PG_Y + PG_H - 4; }

    private void toggleSwitch(int sx, int sy, float a) {
        Gfx.rr(sx, sy, 38, 20, 6, Ui.mix(OFF, ACC, a));
        Gfx.rr(sx + 2 + Math.round(18 * a), sy + 2, 16, 16, 4, Ui.mix(0xFF9A9BA3, ON_ACC, a));
    }

    private void ghostButton(int x, int y, int w, String icon, String label, Act act) {
        boolean ov = over(x, y, w, 28);
        Gfx.rr(x, y, w, 28, 4, ov ? SURF3 : SURF2);
        Gfx.icon(icon, x + 11, y + 5, 18, ov ? Theme.TEXT : Theme.MUTED);
        Gfx.text(label, x + 36, y + (28 - Gfx.lineHeight(11, false)) / 2, 11, false, ov ? Theme.TEXT : Theme.MUTED);
        hit(x, y, w, 28, act, null);
    }

    private void drawPage(GuiGraphicsExtractor g, Minecraft mc, int gs, float sc) {
        final Module m = pageFor;
        List<String> groups = groupsOf(m);
        if (pageGroup == null || !groups.contains(pageGroup)) pageGroup = groups.isEmpty() ? "general" : groups.get(0);
        Gfx.rr(0, 0, PW, PH, 10, 0xFF0F1012);
        Gfx.rrOutline(0, 0, PW, PH, 10, 1, 0x24FFFFFF);

        boolean bo = over(24, 18, 90, 22);
        Gfx.text("<  Back", 24, 22, 11, false, bo ? Theme.TEXT : Theme.MUTED);
        hit(20, 16, 100, 26, (x, y) -> { saveAll(); pageFor = null; }, null);
        Gfx.rr(24, 40, 40, 40, 6, SURF3);
        Gfx.icon(m.icon(), 34, 50, 20, Theme.TEXT);
        Gfx.text(Gfx.trim(m.name, 20, true, PW - 24 - 90 - 220), 76, 50, 20, true, Theme.TEXT);
        float ea = anim("pe" + m.id, m.enabled ? 1f : 0f, 14f);
        int sx0 = PW - 24 - 38;
        String es = m.enabled ? "Enabled" : "Disabled";
        Gfx.text(es, sx0 - 12 - Gfx.width(es, 11, false), 48, 11, false, Ui.mix(Theme.MUTED, Theme.TEXT, ea));
        toggleSwitch(sx0, 46, ea);
        hit(sx0 - 90, 40, 130, 32, (x, y) -> m.toggle(), null);

        int lx0 = 24, lw = 236;
        Gfx.rr(lx0, PG_Y, lw, 108, 6, SURF);
        Gfx.rrOutline(lx0, PG_Y, lw, 108, 6, 1, 0x14FFFFFF);
        if (m instanceof HudModule hm) {
            hm.measure(mc);
            int ew0 = Math.max(1, hm.width(mc)), eh0 = Math.max(1, hm.height(mc));
            float ps = Math.min(1.8f, Math.min((lw - 28f) / ew0, 80f / eh0));
            Gfx.setScale(Math.max(1, Math.round(2f * gs * sc * ps)));
            g.pose().pushMatrix();
            g.pose().translate((float) (lx0 + Math.round((lw - ew0 * ps) / 2f)), (float) (PG_Y + Math.round((108 - eh0 * ps) / 2f)));
            g.pose().scale(ps, ps);
            float f0 = Gfx.fade;
            Gfx.fade = f0 * hm.opacity.value / 100f;
            hm.draw(g, mc);
            Gfx.fade = f0;
            g.pose().popMatrix();
            Gfx.setScale(Math.max(1, Math.round(2f * gs * sc)));
        } else {
            Gfx.icon(m.icon(), lx0 + (lw - 48) / 2, PG_Y + 30, 48, Theme.DIM);
        }
        int gy = PG_Y + 108 + 12;
        for (final String gr : groups) {
            int count = 0;
            for (Setting st : m.settings) if (st.group.equals(gr)) count++;
            boolean act = gr.equals(pageGroup), go = over(lx0, gy, lw, 28);
            float ga = anim("pg_g" + gr, act ? 1f : 0f, 16f);
            if (ga > 0.01f) Gfx.rr(lx0, gy, lw, 28, 4, Ui.alpha(SURF3, ga));
            else if (go) Gfx.rr(lx0, gy, lw, 28, 4, SURF);
            if (ga > 0.01f) Gfx.rr(lx0 + 2, gy + 6, 3, 16, 2, Ui.alpha(ACC, ga));
            Gfx.text(Setting.pretty(gr), lx0 + 16, gy + (28 - Gfx.lineHeight(12, act)) / 2, 12, act, act ? Theme.TEXT : Theme.MUTED);
            String cs = String.valueOf(count);
            Gfx.text(cs, lx0 + lw - 12 - Gfx.width(cs, 10, false), gy + (28 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.DIM);
            hit(lx0, gy, lw, 28, (x, y) -> { pageGroup = gr; pageScroll = pageTarget = 0f; }, null);
            gy += 30;
        }
        int by = PG_Y + PG_H - 28;
        ghostButton(lx0, by, lw, "reset", "Reset options", (x, y) -> { m.resetSettings(); });
        if (m instanceof HudModule) {
            by -= 32;
            ghostButton(lx0, by, lw, "move", "Edit position", (x, y) -> { saveAll(); pageFor = null; editing = true; });
        }

        pageScroll = Ui.approach(pageScroll, pageTarget, dtMs, 16f);
        g.enableScissor(PG_X, PG_Y + 2, PG_X + PG_W, PG_Y + PG_H - 2);
        int y = PG_Y + 6 - Math.round(pageScroll), first = y;
        for (Setting st : m.settings) {
            if (!st.group.equals(pageGroup)) continue;
            y = drawOptionRow(m, st, y);
        }
        g.disableScissor();
        pageContentH = y - first + 12;
        pageTarget = Ui.clamp(pageTarget, 0f, Math.max(0f, pageContentH - PG_H));
        if (pageContentH > PG_H) {
            float kh = Math.max(24f, PG_H * PG_H / pageContentH);
            float ky = PG_Y + (pageScroll / (pageContentH - PG_H)) * (PG_H - kh);
            Gfx.rr(PG_X + PG_W - 8, Math.round(ky), 3, Math.round(kh), 2, 0x33FFFFFF);
        }
    }

    private void rowBg(int y, int h) { Gfx.rr(PG_X + 4, y + 1, PG_W - 8, h - 5, 6, SURF); }

    private int drawOptionRow(final Module m, final Setting st, int y) {
        final int x = PG_X + PAD, w = PG_W - 2 * PAD;
        if (st instanceof Setting.Bool b) {
            float a = anim("pg" + m.id + st.id, b.value ? 1f : 0f, 14f);
            int rh = 46;
            rowBg(y, rh);
            Gfx.text(Gfx.trim(st.label(), 12, false, w - 70), x, y + (rh - Gfx.lineHeight(12, false)) / 2, 12, false, Theme.TEXT);
            toggleSwitch(x + w - 36, y + (rh - 20) / 2, a);
            pageHit(x - 8, y, w + 16, rh, (px, py) -> { b.value = !b.value; saveAll(); }, null);
            return y + rh;
        }
        if (st instanceof Setting.Num n) {
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
            Act drag = (px, py) -> { n.setFraction((px - trackX) / trackW); };
            pageHit(x - 8, y + 26, w + 16, 30, drag, drag);
            return y + rh;
        }
        if (st instanceof Setting.Choice c) {
            int total = 0;
            int[] ws = new int[c.options.length];
            for (int i = 0; i < ws.length; i++) { ws[i] = Gfx.width(Setting.pretty(c.options[i]), 11, false) + 22; total += ws[i]; }
            int lab = Gfx.width(st.label(), 12, false);
            boolean inline = total + 4 + lab + 24 <= w;
            int rh = inline ? 46 : 80;
            rowBg(y, rh);
            int sy = inline ? y + (rh - 28) / 2 : y + 38, sx = inline ? x + w - total - 4 : x;
            Gfx.text(Gfx.trim(st.label(), 12, false, w), x, inline ? y + (rh - Gfx.lineHeight(12, false)) / 2 : y + 12, 12, false, Theme.TEXT);
            int cx = sx;
            Gfx.rr(sx - 2, sy - 2, Math.min(total, w) + 4, 32, 5, SURF3);
            for (int i = 0; i < ws.length; i++) {
                final int idx = i;
                boolean sel = c.index == i, cov = pageOver(cx, sy, ws[i], 28);
                if (sel) Gfx.rr(cx, sy, ws[i], 28, 7, 0xFF4A4B53);
                else if (cov) Gfx.rr(cx, sy, ws[i], 28, 7, 0xFF3A3B42);
                Gfx.textCentered(Setting.pretty(c.options[i]), cx + ws[i] / 2, sy + (28 - Gfx.lineHeight(11, sel)) / 2, 11, sel, sel ? Theme.TEXT : Theme.MUTED);
                pageHit(cx, sy, ws[i], 28, (px, py) -> { c.index = idx; saveAll(); }, null);
                cx += ws[i];
            }
            return y + rh;
        }
        if (st instanceof Setting.Palette pal) {
            int rh = 78;
            rowBg(y, rh);
            Gfx.text(Gfx.trim(st.label(), 12, false, w - 120), x, y + 12, 12, false, Theme.TEXT);
            String cn = Setting.pretty(Setting.Palette.IDS[pal.index]);
            Gfx.text(cn, x + w - Gfx.width(cn, 11, false), y + 13, 11, false, Theme.MUTED);
            int sy = y + 40, step = w / Setting.Palette.IDS.length;
            for (int i = 0; i < Setting.Palette.IDS.length; i++) {
                final int idx = i;
                int sx = x + i * step + (step - 24) / 2;
                boolean sel = pal.index == i, cov = pageOver(sx, sy, 24, 24);
                if (sel) Gfx.rrOutline(sx - 4, sy - 4, 32, 32, 9, 2, 0xFFF5F5F5);
                else if (cov) Gfx.rrOutline(sx - 3, sy - 3, 30, 30, 8, 1, 0x77FFFFFF);
                Gfx.rr(sx, sy, 24, 24, 7, Setting.Palette.color(i));
                pageHit(sx - 4, sy - 4, 32, 32, (px, py) -> { pal.index = idx; saveAll(); }, null);
            }
            return y + rh;
        }
        return y;
    }


    // ---------- cosmetics ----------
    private static final String[] SLOT_NAMES = {"Cape", "Wings", "Bandana", "Pet"};
    private static final int[] BUNDLE_ACC = {0xFFE10600, 0xFFE4E4E8, 0xFF9FD8FF, 0xFFFF86B6, 0xFF8C6CFF, 0xFFFF9B3D, 0xFFFF4040, 0xFF6EC6FF, 0xFFFFD21F, 0xFF3DDC6A};

    private void bodyHit(int x, int y, int w, int h, Act click) {
        int y0 = Math.max(y, VY), y1 = Math.min(y + h, VY + VH);
        if (y1 > y0) hits.add(new Hit(x, y0, w, y1 - y0, click, null));
    }

    private String cosTab = "bundles";
    private static final String[][] COS_TABS = {{"bundles", "Bundles"}, {"cape", "Cloaks"}, {"wings", "Wings"}, {"bandana", "Bandanas"}, {"pet", "Pets"}, {"partner", "Partner"}};

    private void drawCosmetics(GuiGraphicsExtractor g) {
        boolean plus = CosmeticsApi.plus();
        String st = CosmeticsApi.status();
        String line = (CosmeticsApi.enabled() ? (st.equals("ok") ? "Synced with your account" : st.equals("login") ? "Sign in with Microsoft in the launcher to unlock your items" : st.equals("linked") ? "This account is linked to another device" : st.equals("offline") ? "Store offline" : "Loading...") : "Dev mode: everything is unlocked")
                + (plus ? "   -   YA Plus active" : "");
        // fixed header: status, loadout, tabs
        Gfx.text(line, CX, VY + 2, 11, false, Theme.MUTED);
        int ly = VY + 22;
        Gfx.text("LOADOUT", CX, ly + 14, 10, true, Theme.DIM);
        for (int i = 0; i < 4; i++) {
            int x = CX + 70 + i * 52;
            String it = Cosmetics.equipped(Cosmetics.SLOTS[i]);
            Gfx.rr(x, ly, 44, 44, 6, SURF2);
            if (!it.equals("none")) Gfx.image("/assets/yaclient/cos/" + it + ".jpg", x + 2, ly + 2, 40, 40, 0xFFFFFFFF);
            else Gfx.textCentered(SLOT_NAMES[i].substring(0, 1), x + 22, ly + 14, 14, true, Theme.DIM);
        }
        int ty = ly + 54, tx = CX;
        for (String[] tb : COS_TABS) {
            int tw = Gfx.width(tb[1], 11, true) + 24;
            boolean on = cosTab.equals(tb[0]);
            boolean ov = over(tx, ty, tw, 26);
            Gfx.rr(tx, ty, tw, 26, 13, on ? 0xFFE8E8E8 : (ov ? SURF3 : SURF2));
            Gfx.textCentered(tb[1], tx + tw / 2, ty + (26 - Gfx.lineHeight(11, true)) / 2, 11, true, on ? ON_ACC : Theme.TEXT);
            final String key = tb[0];
            bodyHit(tx, ty, tw, 26, (px, py) -> { cosTab = key; scrollTarget = 0; scroll = 0; });
            tx += tw + 8;
        }
        int gridTop = ty + 38;
        boolean bundles = cosTab.equals("bundles");
        int gap = 12, cols = bundles ? 4 : 5, cw = (VW - (cols - 1) * gap) / cols;
        int imgH = cw - 8, ch = imgH + (bundles ? 86 : 62);
        java.util.List<int[]> cells = new java.util.ArrayList<int[]>();   // {bundle}
        if (bundles) { for (int b = 0; b < Cosmetics.BUNDLES.length; b++) cells.add(new int[] {b}); }
        else if (cosTab.equals("partner")) { for (int b = 0; b < Cosmetics.BUNDLES.length; b++) if (Cosmetics.PARTNER[b]) cells.add(new int[] {b}); }
        else {
            for (int b = 0; b < Cosmetics.BUNDLES.length; b++) cells.add(new int[] {b});
            for (int e = 0; e < Cosmetics.EXTRAS.length; e++) if (Cosmetics.EXTRAS[e].endsWith("_" + cosTab)) cells.add(new int[] {-1, e});   // single items
        }
        int rows = (cells.size() + cols - 1) / cols;
        int areaH = VY + VH - gridTop;
        contentH = rows * (ch + gap) + 6;
        viewH = areaH;
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - areaH));
        g.enableScissor(CX - 6, gridTop, CX + VW + 6, VY + VH + 2);
        int top = gridTop - Math.round(scroll);
        boolean partnerTab = cosTab.equals("partner");
        for (int c = 0; c < cells.size(); c++) {
            final int bi = Math.max(0, cells.get(c)[0]);
            int x = CX + (c % cols) * (cw + gap), y = top + (c / cols) * (ch + gap);
            if (y + ch < gridTop - 2 || y > VY + VH + 2) continue;
            boolean inView = myl >= gridTop && myl < VY + VH;
            boolean hov = inView && over(x, y, cw, ch);
            if (bundles || partnerTab) {
                boolean owned = Cosmetics.ownsBundle(bi), wearing = Cosmetics.wearsBundle(bi);
                float h = anim("cb" + bi + cosTab, hov ? 1f : 0f, 16f);
                Gfx.rr(x, y, cw, ch, 6, Ui.mix(SURF, SURF2, h));
                Gfx.image("/assets/yaclient/cos/b_" + Cosmetics.BUNDLES[bi] + ".jpg", x + 4, y + 4, cw - 8, imgH, 0xFFFFFFFF);
                Gfx.rrOutline(x, y, cw, ch, 6, 1, wearing ? 0x99FFFFFF : Ui.mix(0x14FFFFFF, 0x30FFFFFF, h));
                Gfx.text(Gfx.trim(Cosmetics.NAMES[bi], 13, true, cw - 80), x + 12, y + imgH + 14, 13, true, Theme.TEXT);
                String tag = Cosmetics.PARTNER[bi] ? "PARTNER" : owned ? "OWNED" : "LOCKED";
                int tw = Gfx.width(tag, 8, true) + 14;
                Gfx.rr(x + cw - 12 - tw, y + imgH + 14, tw, 16, 4, owned ? 0xFFE8E8E8 : 0xFF2D2E34);
                Gfx.textCentered(tag, x + cw - 12 - tw / 2, y + imgH + 14 + (16 - Gfx.lineHeight(8, true)) / 2, 8, true, owned ? ON_ACC : Theme.MUTED);
                int fy = y + ch - 12 - 28;
                boolean fo = inView && over(x + 12, fy, cw - 24, 28);
                String fl = owned ? (wearing ? "Remove all" : "Equip all") : (CosmeticsApi.premium() ? "Get in store" : "Premium accounts only");
                Gfx.rr(x + 12, fy, cw - 24, 28, 5, owned ? (fo ? 0xFFF4F4F5 : 0xFFE8E8E8) : (fo ? SURF3 : 0xFF26272C));
                Gfx.textCentered(fl, x + cw / 2, fy + (28 - Gfx.lineHeight(11, true)) / 2, 11, true, owned ? ON_ACC : Theme.TEXT);
                bodyHit(x + 12, fy, cw - 24, 28, (px, py) -> {
                    if (!CosmeticsApi.premium()) return;
                    if (Cosmetics.ownsBundle(bi)) Cosmetics.toggleBundle(bi); else CosmeticsApi.openBundle(Cosmetics.BUNDLES[bi]);
                });
            } else {
                final String slot = cosTab;
                final int ex = cells.get(c).length > 1 ? cells.get(c)[1] : -1;
                final String item = ex >= 0 ? Cosmetics.EXTRAS[ex] : Cosmetics.item(bi, slot);
                boolean has = CosmeticsApi.owned(item), on = Cosmetics.selected(slot).equals(item);
                float h = anim("ci" + item, hov ? 1f : 0f, 16f);
                Gfx.rr(x, y, cw, ch, 6, Ui.mix(SURF, SURF2, h));
                Gfx.image("/assets/yaclient/cos/" + item + ".jpg", x + 4, y + 4, cw - 8, imgH, has ? 0xFFFFFFFF : 0xFF777777);
                Gfx.rrOutline(x, y, cw, ch, 6, 1, on ? 0x99FFFFFF : Ui.mix(0x14FFFFFF, 0x30FFFFFF, h));
                if (!has) Gfx.icon("lock", x + cw - 24, y + 10, 14, Theme.TEXT);
                Gfx.text(Gfx.trim(ex >= 0 ? Cosmetics.EXTRA_NAMES[ex] : Cosmetics.NAMES[bi], 12, true, cw - 20), x + 10, y + imgH + 10, 12, true, Theme.TEXT);
                int fy = y + ch - 8 - 22;
                boolean fo = inView && over(x + 10, fy, cw - 20, 22);
                String fl = has ? (on ? "Remove" : "Equip") : (CosmeticsApi.premium() ? "Get in store" : "Premium only");
                Gfx.rr(x + 10, fy, cw - 20, 22, 4, has ? (fo ? 0xFFF4F4F5 : 0xFFE8E8E8) : (fo ? SURF3 : 0xFF26272C));
                Gfx.textCentered(fl, x + cw / 2, fy + (22 - Gfx.lineHeight(10, true)) / 2, 10, true, has ? ON_ACC : Theme.TEXT);
                bodyHit(x + 10, fy, cw - 20, 22, (px, py) -> {
                    if (!CosmeticsApi.premium()) return;
                    if (CosmeticsApi.owned(item)) { if (ex >= 0) Cosmetics.toggleItem(slot, item); else Cosmetics.toggle(bi, slot); } else CosmeticsApi.openItem(item);
                });
            }
        }
        g.disableScissor();
        if (contentH > areaH) {
            float kh = Math.max(24f, areaH * areaH / contentH);
            float ky = gridTop + (scroll / (contentH - areaH)) * (areaH - kh);
            Gfx.rr(PW - 12, Math.round(ky), 4, Math.round(kh), 2, 0x33FFFFFF);
        }
    }

    // ---------- HUD editor ----------
    private void renderEditor(GuiGraphicsExtractor g, Minecraft mc, int mx, int my, int gs) {
        g.fill(0, 0, width, height, 0x66000000);
        for (Module m : Modules.ALL) {
            if (!m.enabled || !(m instanceof HudModule h)) continue;
            h.measure(mc);
            if (h.width(mc) <= 0) continue;
            h.render(g, mc);
            float x = h.screenX(width, mc), y = h.screenY(height, mc);
            boolean hov = mx >= x && mx <= x + h.screenW(mc) && my >= y && my <= y + h.screenH(mc);
            g.outline(Math.round(x) - 1, Math.round(y) - 1, Math.round(h.screenW(mc)) + 2, Math.round(h.screenH(mc)) + 2, hov || h == dragging ? 0xFFF4F4F5 : 0x66FFFFFF);
        }
        Gfx.g = g;
        Gfx.fade = 1f;
        Gfx.setScale(gs);
        String done = "Done", reset = "Reset", hint = "Drag to move  -  mouse wheel to resize";
        int dw = Gfx.width(done, 11, true) + 40, rw = Gfx.width(reset, 11, false) + 40, y = height - 34;
        int total = dw + rw + 10, x0 = width / 2 - total / 2;
        mxl = mx;
        myl = my;
        boolean od = over(x0, y, dw, 24), or = over(x0 + dw + 10, y, rw, 24);
        Gfx.rr(x0, y, dw, 24, 4, od ? 0xFFF4F4F5 : 0xFFE8E8E8);
        Gfx.textCentered(done, x0 + dw / 2, y + (24 - Gfx.lineHeight(11, true)) / 2, 11, true, ON_ACC);
        Gfx.rr(x0 + dw + 10, y, rw, 24, 4, or ? SURF3 : SURF2);
        Gfx.textCentered(reset, x0 + dw + 10 + rw / 2, y + (24 - Gfx.lineHeight(11, false)) / 2, 11, false, Theme.TEXT);
        Gfx.textCentered(hint, width / 2, y - 18, 10, false, Theme.MUTED);
    }

    private HudModule hudAt(double mx, double my) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = Modules.ALL.size() - 1; i >= 0; i--) {
            Module m = Modules.ALL.get(i);
            if (!m.enabled || !(m instanceof HudModule h)) continue;
            h.measure(mc);
            float x = h.screenX(width, mc), y = h.screenY(height, mc);
            if (h.width(mc) > 0 && mx >= x && mx <= x + h.screenW(mc) && my >= y && my <= y + h.screenH(mc)) return h;
        }
        return null;
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (e.button() != 0) return super.mouseClicked(e, dbl);
        Minecraft mc = Minecraft.getInstance();
        if (editing) {
            mxl = (float) e.x();
            myl = (float) e.y();
            Gfx.setScale(Math.max(1, mc.getWindow().getGuiScale()));
            int dw = Gfx.width("Done", 11, true) + 40, rw = Gfx.width("Reset", 11, false) + 40, y = height - 34;
            int x0 = width / 2 - (dw + rw + 10) / 2;
            if (over(x0, y, dw, 24)) { editing = false; saveAll(); return true; }
            if (over(x0 + dw + 10, y, rw, 24)) { for (Module m : Modules.ALL) if (m instanceof HudModule h) h.resetLayout(); saveAll(); return true; }
            HudModule h = hudAt(e.x(), e.y());
            if (h != null) {
                dragging = h;
                h.measure(mc);
                dragDX = (float) (e.x() - h.screenX(width, mc));
                dragDY = (float) (e.y() - h.screenY(height, mc));
                return true;
            }
            return true;
        }
        if (pageFor != null) {
            toLocal(e.x(), e.y());
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (mxl >= h.x && mxl < h.x + h.w && myl >= h.y && myl < h.y + h.h) {
                    if (h.click != null) h.click.run(mxl, myl);
                    activeDrag = h.drag;
                    return true;
                }
            }
            return true;
        }
        if (tab == COS) {
            toLocal(e.x(), e.y());
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (mxl >= h.x && mxl < h.x + h.w && myl >= h.y && myl < h.y + h.h) { if (h.click != null) h.click.run(mxl, myl); return true; }
            }
        }
        searchFocus = false;
        if (tab != COS && over(PW - 24 - 230, 18, 230, 34)) { searchFocus = true; return true; }
        int[] ws = new int[TABS.length];
        for (int i = 0; i < TABS.length; i++) ws[i] = Gfx.width(TABS[i], 12, true) + 46;
        int px = CX + 4;
        for (int i = 0; i < TABS.length; i++) {
            if (over(px, 74, ws[i], 30)) { tab = i; scroll = scrollTarget = 0; if (i == COS) CosmeticsApi.refreshAsync(false); return true; }
            px += ws[i];
        }
        int w = Gfx.width("HUD editor", 11, false) + 48;
        if (over(PW - 24 - w, 70, w, 38)) { editing = true; return true; }
        if (tab != COS && myl >= VY && myl < VY + VH) {
            List<Module> list = visible();
            int gap = 12, cw = (VW - 2 * gap) / 3, ch = 120, top = VY - Math.round(scroll);
            for (int i = 0; i < list.size(); i++) {
                int x = CX + (i % 3) * (cw + gap), y = top + (i / 3) * (ch + gap);
                if (over(x, y, cw, ch)) {
                    Module m = list.get(i);
                    int sx = x + cw - 14 - 38, sy = y + ch - 14 - 26 + 3;
                    if (m.settings.isEmpty() || over(sx - 8, sy - 6, 54, 32)) m.toggle();
                    else { pageFor = m; pageGroup = null; pageScroll = pageTarget = 0f; }
                    return true;
                }
            }
        }
        return super.mouseClicked(e, dbl);
    }

    private void toLocal(double mx, double my) {
        mxl = (float) ((mx - width / 2f) / curSc + PW / 2f);
        myl = (float) ((my - height / 2f) / curSc + PH / 2f);
    }

    private void saveAll() {
        for (Module m : Modules.ALL) m.save();
        Config.save();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (!editing && activeDrag != null) { toLocal(e.x(), e.y()); activeDrag.run(mxl, myl); return true; }
        if (editing && dragging != null) {
            Minecraft mc = Minecraft.getInstance();
            dragging.measure(mc);
            float x = (float) (e.x() - dragDX), y = (float) (e.y() - dragDY);
            dragging.px = Ui.clamp(x / width, 0f, 1f);
            dragging.py = Ui.clamp(y / height, 0f, 1f);
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (activeDrag != null) { activeDrag = null; saveAll(); return true; }
        if (dragging != null) { dragging = null; saveAll(); return true; }
        return super.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (editing) {
            HudModule h = hudAt(mx, my);
            if (h != null) { h.scale = Ui.clamp(h.scale + (float) sy * 0.05f, HudModule.MIN_SCALE, HudModule.MAX_SCALE); saveAll(); }
            return true;
        }
        if (pageFor != null) { pageTarget = Ui.clamp(pageTarget - (float) sy * 40f, 0f, Math.max(0f, pageContentH - PG_H)); return true; }
        scrollTarget = Ui.clamp(scrollTarget - (float) sy * 60f / Math.max(0.3f, scale) * 0.6f, 0f, Math.max(0f, contentH - (viewH > 0 ? viewH : VH)));
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent e) {
        if (searchFocus && !editing) {
            int cp = e.codepoint();
            if (cp >= 32 && cp != 127 && query.length() < 24) { query += new String(Character.toChars(cp)); scroll = scrollTarget = 0; return true; }
        }
        return super.charTyped(e);
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        if (searchFocus && !editing) {
            if (e.key() == 259) { if (!query.isEmpty()) query = query.substring(0, query.length() - 1); return true; }
            if (e.key() == 256) { searchFocus = false; return true; }
        }
        if (editing && e.key() == 256) { editing = false; saveAll(); return true; }
        if (pageFor != null && e.key() == 256) { saveAll(); pageFor = null; return true; }
        return super.keyPressed(e);
    }
}

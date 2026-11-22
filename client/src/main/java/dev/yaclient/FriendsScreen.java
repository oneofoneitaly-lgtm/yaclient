package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/** Friends list: add by name, accept/decline requests, online friends first with a green dot. */
public class FriendsScreen extends GuiScreen {
    private static final int PW = 250, RH = 24, HH = 16;
    private static final int GREEN = 0xFF22C55E;

    private final GuiScreen parent;
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private final StringBuilder input = new StringBuilder();
    private String msg = "";
    private int msgColor = Theme.MUTED;
    private float scroll, scrollTarget, contentH;
    private int px, py, ph, vy, vh, ix, iw;
    private float closeHover, addHover;
    private final List<Row> rows = new ArrayList<Row>();

    private static final class Row {
        int type; // 0 header, 1 friend, 2 request, 3 sent
        String label;
        FriendsApi.Friend f;
        float hover;
    }

    public FriendsScreen(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        FriendsApi.pollNow();
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    private void rebuild() {
        List<FriendsApi.Friend> fr = FriendsApi.friends();
        List<FriendsApi.Friend> on = new ArrayList<FriendsApi.Friend>(), off = new ArrayList<FriendsApi.Friend>();
        for (FriendsApi.Friend f : fr) (f.online ? on : off).add(f);
        // keep existing hover state per uuid
        List<Row> old = new ArrayList<Row>(rows);
        rows.clear();
        List<FriendsApi.Friend> in = FriendsApi.incoming();
        if (!in.isEmpty()) {
            header(Lang.t("friends.requests") + " · " + in.size());
            for (FriendsApi.Friend f : in) row(2, f, old);
        }
        if (!on.isEmpty()) {
            header(Lang.t("friends.online") + " · " + on.size());
            for (FriendsApi.Friend f : on) row(1, f, old);
        }
        if (!off.isEmpty()) {
            header(Lang.t("friends.offline") + " · " + off.size());
            for (FriendsApi.Friend f : off) row(1, f, old);
        }
        List<FriendsApi.Friend> out = FriendsApi.outgoing();
        if (!out.isEmpty()) {
            header(Lang.t("friends.sent") + " · " + out.size());
            for (FriendsApi.Friend f : out) row(3, f, old);
        }
    }

    private void header(String s) { Row r = new Row(); r.type = 0; r.label = s; rows.add(r); }

    private void row(int type, FriendsApi.Friend f, List<Row> old) {
        Row r = new Row();
        r.type = type; r.f = f;
        for (Row o : old) if (o.f != null && o.f.uuid.equals(f.uuid) && o.type == type) r.hover = o.hover;
        rows.add(r);
    }

    private int rowH(Row r) { return r.type == 0 ? HH : RH; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 200f);
        drawGradientRect(0, 0, width, height, Ui.alpha(0x20000000, fade), Ui.alpha(0x38000000, fade));

        ScaledResolution sr = new ScaledResolution(mc);
        int sf = sr.getScaleFactor();
        Gfx.setScale(Math.max(1, sf));
        Gfx.fade = fade;

        rebuild();
        ph = Math.min(260, height - 24);
        px = (width - PW) / 2;
        py = (height - ph) / 2 + Math.round((1f - fade) * 6);
        ix = px + 14;
        iw = PW - 28;

        Gfx.rr(px, py, PW, ph, 7, 0x66101015);
        Gfx.rrOutline(px, py, PW, ph, 7, 1, Theme.GLASS_LINE);

        Gfx.icon("friends", ix, py + 11, 14, Theme.TEXT);
        Gfx.text(Lang.t("friends.title"), ix + 20, py + 11, 11, true, Theme.TEXT);
        int onN = FriendsApi.onlineCount();
        String cnt = onN + " " + Lang.t("friends.online_l");
        Gfx.rr(px + PW - 24 - Gfx.width(cnt, 8, false) - 22, py + 12, 6, 6, 3, GREEN);
        Gfx.text(cnt, px + PW - 24 - Gfx.width(cnt, 8, false) - 12, py + 11, 8, false, Theme.MUTED);
        int cx = px + PW - 24, cy = py + 8;
        boolean cOver = mx >= cx && mx < cx + 18 && my >= cy && my < cy + 18;
        closeHover = Ui.approach(closeHover, cOver ? 1f : 0f, dt, 16f);
        Gfx.icon("close", cx + 2, cy + 2, 14, Ui.mix(Theme.MUTED, Theme.TEXT, closeHover));

        // add-by-name field
        int fy = py + 34, fw = iw - 52;
        Gfx.rr(ix, fy, fw, 22, 7, 0x22FFFFFF);
        Gfx.rrOutline(ix, fy, fw, 22, 7, 1, (now / 500) % 2 == 0 ? 0x66FFFFFF : 0x44FFFFFF);
        String shown = input.toString();
        if (shown.isEmpty()) Gfx.text(Lang.t("friends.placeholder"), ix + 8, fy + 6, 9, false, Theme.DIM);
        else Gfx.text(Gfx.trim(shown, 9, false, fw - 20), ix + 8, fy + 6, 9, false, Theme.TEXT);
        if ((now / 500) % 2 == 0) {
            int tw = shown.isEmpty() ? 0 : Gfx.width(Gfx.trim(shown, 9, false, fw - 20), 9, false);
            Gfx.rr(ix + 9 + tw, fy + 5, 1, 12, 0, 0xFFE8E8EC);
        }
        int bx = ix + fw + 6;
        boolean aOver = mx >= bx && mx < bx + 46 && my >= fy && my < fy + 22;
        addHover = Ui.approach(addHover, aOver ? 1f : 0f, dt, 16f);
        Gfx.rr(bx, fy, 46, 22, 7, Ui.mix(0xFFEDEDF0, 0xFFFFFFFF, addHover));
        Gfx.textCentered(Lang.t("friends.add"), bx + 23, fy + 6, 9, true, 0xFF0C0C10);

        int my2 = fy + 26;
        if (!msg.isEmpty()) Gfx.text(Gfx.trim(msg, 7, false, iw), ix + 2, my2, 7, false, msgColor);

        vy = py + 68;
        vh = ph - 68 - 10;
        scroll = Ui.approach(scroll, scrollTarget, dt, 16f);
        contentH = 0;
        for (Row r : rows) contentH += rowH(r) + (r.type == 0 ? 0 : 2);
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - vh));

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((px + 2) * sf, (height - (vy + vh)) * sf, (PW - 4) * sf, vh * sf);
        int y = vy - Math.round(scroll);
        if (rows.isEmpty()) {
            String s = !CosmeticsApi.enabled() ? Lang.t("friends.offline_api")
                    : "login".equals(FriendsApi.status()) ? Lang.t("friends.login")
                    : !FriendsApi.loaded() ? Lang.t("friends.loading") : Lang.t("friends.empty");
            Gfx.textCentered(s, px + PW / 2, vy + 30, 8, false, Theme.MUTED);
        }
        for (Row r : rows) {
            int h = rowH(r);
            if (r.type == 0) {
                Gfx.text(r.label.toUpperCase(), ix + 2, y + 4, 7, true, Theme.DIM);
            } else if (y + h > vy - 4 && y < vy + vh + 4) {
                drawRow(r, y, mx, my, dt);
            }
            y += h + (r.type == 0 ? 0 : 2);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (contentH > vh) {
            int kh = Math.max(16, (int) (vh * (vh / contentH)));
            float ky = vy + (scroll / (contentH - vh)) * (vh - kh);
            Gfx.rr(px + PW - 7, Math.round(ky), 3, kh, 1, 0x40FFFFFF);
        }
        Gfx.fade = 1f;
        FriendToast.render();
    }

    private void drawRow(Row r, int y, int mx, int my, float dt) {
        boolean inView = my >= vy && my < vy + vh;
        boolean over = inView && mx >= ix && mx < ix + iw && my >= y && my < y + RH;
        r.hover = Ui.approach(r.hover, over ? 1f : 0f, dt, 16f);
        FriendsApi.Friend f = r.f;
        boolean dim = r.type == 1 && !f.online;
        Gfx.rr(ix, y, iw, RH, 4, Ui.mix(0x14FFFFFF, 0x26FFFFFF, r.hover));
        // avatar (initial in a circle)
        int ax = ix + 6, ay = y + 4, as = 16;
        Gfx.rr(ax, ay, as, as, as / 2, dim ? 0x30FFFFFF : 0x55FFFFFF);
        String ini = f.name.isEmpty() ? "?" : f.name.substring(0, 1).toUpperCase();
        Gfx.textCentered(ini, ax + as / 2, ay + 3, 9, true, dim ? Theme.MUTED : Theme.TEXT);
        if (r.type == 1 && f.online) {
            Gfx.rr(ax + as - 6, ay + as - 6, 8, 8, 4, 0xFF15151A);
            Gfx.rr(ax + as - 5, ay + as - 5, 6, 6, 3, GREEN);
        }
        int nx = ax + as + 7;
        int maxW = iw - (nx - ix) - (r.type == 2 ? 50 : 26);
        Gfx.text(Gfx.trim(f.name, 9, true, maxW), nx, y + 4, 9, true, dim ? Theme.MUTED : Theme.TEXT);
        String sub;
        int subC = Theme.DIM;
        if (r.type == 1) {
            if (f.online) { sub = "game".equals(f.where) ? Lang.t("friends.in_game") : Lang.t("friends.in_launcher"); subC = GREEN; }
            else sub = Lang.t("friends.offline_l");
        } else if (r.type == 2) sub = Lang.t("friends.wants");
        else sub = Lang.t("friends.pending");
        Gfx.text(sub, nx, y + 14, 7, false, subC);

        int by = y + 4;
        if (r.type == 2) {
            int[] a = btn(0, y), d = btn(1, y);
            boolean ao = inView && mx >= a[0] && mx < a[0] + 18 && my >= a[1] && my < a[1] + 16;
            boolean dO = inView && mx >= d[0] && mx < d[0] + 18 && my >= d[1] && my < d[1] + 16;
            Gfx.rr(a[0], a[1], 18, 16, 6, ao ? GREEN : 0xFF1B8F47);
            Gfx.icon("check", a[0] + 3, a[1] + 1, 12, 0xFFFFFFFF);
            Gfx.rr(d[0], d[1], 18, 16, 6, dO ? 0xFF4A4A52 : 0xFF303036);
            Gfx.icon("close", d[0] + 3, d[1] + 1, 12, Theme.TEXT);
        } else if (r.hover > 0.3f) {
            int[] b = btn(1, y);
            boolean bo = inView && mx >= b[0] && mx < b[0] + 18 && my >= b[1] && my < b[1] + 16;
            Gfx.rr(b[0], b[1], 18, 16, 6, bo ? 0xFFB3261E : 0xFF303036);
            Gfx.icon("close", b[0] + 3, b[1] + 1, 12, Theme.TEXT);
        }
    }

    /** Button rect: slot 0 = left (accept), slot 1 = right (decline / remove / cancel). */
    private int[] btn(int slot, int y) {
        return new int[]{slot == 1 ? ix + iw - 24 : ix + iw - 46, y + 4};
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
        int cx = px + PW - 24, cy = py + 8;
        boolean outside = mx < px || mx > px + PW || my < py || my > py + ph;
        if (outside || (mx >= cx && mx < cx + 18 && my >= cy && my < cy + 18)) { close(); return; }
        int fy = py + 34, fw = iw - 52, bx = ix + fw + 6;
        if (mx >= bx && mx < bx + 46 && my >= fy && my < fy + 22) { submit(); return; }
        if (my < vy || my >= vy + vh) return;
        int y = vy - Math.round(scroll);
        for (Row r : rows) {
            int h = rowH(r);
            if (r.type != 0 && my >= y && my < y + RH) {
                final String id = r.f.uuid;
                int[] a = btn(0, y), d = btn(1, y);
                boolean inA = mx >= a[0] && mx < a[0] + 18 && my >= a[1] && my < a[1] + 16;
                boolean inD = mx >= d[0] && mx < d[0] + 18 && my >= d[1] && my < d[1] + 16;
                if (r.type == 2 && inA) FriendsApi.accept(id, null);
                else if (inD && (r.type != 1 || r.hover > 0.3f)) {
                    if (r.type == 1) FriendsApi.remove(id, null); else FriendsApi.decline(id, null);
                }
                return;
            }
            y += h + (r.type == 0 ? 0 : 2);
        }
    }

    private void submit() {
        final String name = input.toString().trim();
        if (name.length() < 3) return;
        msg = Lang.t("friends.sending");
        msgColor = Theme.MUTED;
        FriendsApi.add(name, new FriendsApi.Done() {
            @Override public void done(String code) {
                if ("sent".equals(code)) { msg = Lang.t("friends.r.sent", name); msgColor = GREEN; input.setLength(0); }
                else if ("accepted".equals(code)) { msg = Lang.t("friends.r.accepted", name); msgColor = GREEN; input.setLength(0); }
                else { msg = Lang.t("friends.r." + code); msgColor = 0xFFFF6B63; }
            }
        });
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (key == Keyboard.KEY_ESCAPE) { close(); return; }
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) { submit(); return; }
        if (key == Keyboard.KEY_BACK) { if (input.length() > 0) input.setLength(input.length() - 1); return; }
        if (GuiScreen.isKeyComboCtrlV(key)) {
            String c = GuiScreen.getClipboardString();
            if (c != null) for (char ch : c.toCharArray()) if (ok(ch) && input.length() < 16) input.append(ch);
            return;
        }
        if (ok(typed) && input.length() < 16) input.append(typed);
    }

    private static boolean ok(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_'; }

    private void close() { mc.displayGuiScreen(parent); }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

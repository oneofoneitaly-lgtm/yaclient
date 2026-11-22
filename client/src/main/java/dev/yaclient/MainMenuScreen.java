package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.glu.Project;

/** YA CLIENT title screen: bright backdrop, logo, stacked buttons with a highlighted Play, an icon dock, the spinning own player on a floor ring, and a left column with recent servers and news. */
public class MainMenuScreen extends GuiScreen {
    private static final int BW = 150, BH = 22, PBH = 28, GAP = 6, TW = 34, TH = 25, DG = 6;
    private static final String[] ICONS = {"shirt", "friends", "gear", "close"};

    private final float[] hover = new float[3];
    private final float[] dock = new float[ICONS.length];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int bx, by0, dockX, dockY, recentY;
    private int nx, ny, nw = RecentServers.W, nh = 86;
    private float newsHover;
    private int pillX, pillY, pillW;
    private final int[] qx = new int[11];
    private int qy;

    private int btnH(int i) { return i == 0 ? PBH : BH; }
    private int btnY(int i) { return i == 0 ? by0 : by0 + PBH + GAP + (i - 1) * (BH + GAP); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float t = (now - opened) / 1000f;
        float fadeIn = Ui.ease((now - opened) / 500f);

        MenuBg.draw(width, height, t);

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, gs));
        Gfx.fade = fadeIn;

        int logo = Math.min(50, height / 6), badge = logo + 14;
        boolean wideScr = width >= 520;
        int plW = 112;
        int contentH = badge + 14 + PBH + 2 * BH + 3 * GAP + 12 + TH;
        int top = Math.max(10, (height - contentH) / 2);
        int cx = width / 2;

        // the player: in the free space on the right, toward its right side, with only a soft floor shadow
        if (wideScr) {
            int rightStart = cx + BW / 2 + 20;
            int pcx = rightStart + (width - 16 - rightStart) * 72 / 100;
            int feet = top + contentH - 24;
            int ps = Math.max(30, (contentH - 40) * 10 / 19);
            MenuBg.blob(pcx, feet + 2, 46f, 8f, 0x1A2238, 75);
            MenuPlayer.draw(pcx, feet, ps, ((now - opened) % 9000L) / 9000f * 360f);
            String nm = Minecraft.getMinecraft().getSession().getUsername();
            int pw = Gfx.width(nm, 9, true) + 22;
            int py = feet + 16;
            pillX = pcx - pw / 2; pillY = py; pillW = pw;
            Gfx.rr(pcx - pw / 2, py + 1, pw, 16, 8, 0x22101830);
            Gfx.rr(pcx - pw / 2, py, pw, 16, 8, 0xE0101014);
            Gfx.rrOutline(pcx - pw / 2, py, pw, 16, 8, 1, 0x40FFFFFF);
            Gfx.textCentered(nm, pcx, py + (16 - Gfx.lineHeight(9, true)) / 2, 9, true, Theme.TEXT);
        }

        // logo: outline copies for contrast on the bright screen + a soft shadow
        float bob = (float) Math.sin(t * 1.4f) * 1.2f;
        float lx = cx - logo / 2f, ly = top + bob + 6;
        MenuBg.blob(cx, ly + logo + 4, logo * 0.7f, 4f, 0x1A2238, 60);
        for (int k = 0; k < 8; k++) {
            double ang = k * Math.PI / 4;
            Logo.drawTint(lx + (float) Math.cos(ang) * 1.6f, ly + (float) Math.sin(ang) * 1.6f, logo, logo, 0.92f * fadeIn, 0.10f, 0.11f, 0.15f);
        }
        Logo.draw(lx, ly, logo, logo, fadeIn);

        bx = cx - BW / 2;
        by0 = top + badge + 14;
        String[] labels = {Lang.t("menu.play"), Lang.t("menu.multiplayer"), Lang.t("menu.client_settings")};
        for (int i = 0; i < 3; i++) {
            int x = bx, y = btnY(i), h = btnH(i);
            boolean over = mx >= x && mx < x + BW && my >= y && my < y + h;
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            int lift = Math.round(hover[i]);
            Gfx.rr(x, y + 2, BW, h, 8, i == 0 ? Ui.alpha(Theme.accent(), 0.30f) : 0x26101830);
            if (i == 0) {
                Gfx.rr(x, y - lift, BW, h, 8, Ui.mix(Theme.accent(), 0xFFFFFFFF, 0.12f * hover[i]));
                Gfx.rrOutline(x, y - lift, BW, h, 8, 1, Ui.mix(0x50FFFFFF, 0xA0FFFFFF, hover[i]));
                Gfx.textCentered(labels[i], x + BW / 2, y - lift + (h - Gfx.lineHeight(11, true)) / 2, 11, true, 0xFFFFFFFF);
            } else {
                Gfx.rr(x, y - lift, BW, h, 8, Ui.mix(0xD0101014, 0xE8242429, hover[i]));
                Gfx.rrOutline(x, y - lift, BW, h, 8, 1, Ui.mix(0x40FFFFFF, 0x80FFFFFF, hover[i]));
                Gfx.textCentered(labels[i], x + BW / 2, y - lift + (h - Gfx.lineHeight(9, true)) / 2, 9, true, Theme.TEXT);
            }
        }

        // icon dock
        int dockW = ICONS.length * TW + (ICONS.length - 1) * DG;
        dockX = cx - dockW / 2;
        dockY = btnY(2) + BH + 12;
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (TW + DG);
            boolean over = mx >= x && mx < x + TW && my >= dockY && my < dockY + TH;
            dock[i] = Ui.approach(dock[i], over ? 1f : 0f, dt, 16f);
            int lift = Math.round(dock[i]);
            Gfx.rr(x, dockY + 2, TW, TH, 9, 0x26101830);
            Gfx.rr(x, dockY - lift, TW, TH, 9, Ui.mix(0xD0101014, 0xE8242429, dock[i]));
            Gfx.rrOutline(x, dockY - lift, TW, TH, 9, 1, Ui.mix(0x40FFFFFF, 0x80FFFFFF, dock[i]));
            Gfx.icon(ICONS[i], x + (TW - 16) / 2, dockY - lift + (TH - 16) / 2, 16, Ui.mix(0xFFD8D8DC, Theme.TEXT, dock[i]));
        }

        // left column (recent servers + news), centered vertically as one block
        int rb = RecentServers.blockH();
        int colH = rb + nh;
        int colTop = Math.max(10, (height - colH) / 2);
        recentY = colTop;
        nx = RecentServers.X;
        ny = colTop + rb;
        if (width >= 520) RecentServers.draw(mx, my, recentY);
        boolean nOver = width >= 520 && mx >= nx && mx < nx + nw && my >= ny && my < ny + nh;
        newsHover = Ui.approach(newsHover, nOver ? 1f : 0f, dt, 16f);
        if (width >= 520) {
        Gfx.rr(nx, ny + 2, nw, nh, 9, 0x26101830);
        Gfx.rr(nx, ny, nw, nh, 9, Ui.mix(0xD0101014, 0xE8242429, newsHover));
        int artH = Math.round((nw - 8) / NewsArt.ASPECT);
        NewsArt.draw(nx + 4, ny + 4, nw - 8, artH, fadeIn);
        Gfx.rrOutline(nx, ny, nw, nh, 9, 1, Ui.mix(0x40FFFFFF, 0x80FFFFFF, newsHover));
        Gfx.text(Lang.t("news.card"), nx + 8, ny + 4 + artH + 5, 9, true, Theme.TEXT);
        Gfx.text(Gfx.trim("Update " + NewsScreen.VERSION + " \u00b7 " + Lang.t("news.open"), 7, false, nw - 14), nx + 8, ny + 4 + artH + 18, 7, false, Theme.MUTED);
        }

        // footer
        Gfx.text("CLIENT 1.8.9", 16, height - 14, 8, true, INK2);
        // quick settings (bottom right): language chips
        int n = 0, cxr = width - 16;
        qy = height - 22;
        for (int i = Lang.CODES.length - 1; i >= 0; i--) {
            String c = Lang.CODES[i].toUpperCase();
            int cw = Gfx.width(c, 7, true) + 10;
            cxr -= cw;
            boolean sel = Lang.CODES[i].equals(Lang.code());
            boolean ov = mx >= cxr && mx < cxr + cw && my >= qy && my < qy + 14;
            Gfx.rr(cxr, qy, cw, 14, 7, sel ? 0xE0101014 : (ov ? 0x70101014 : 0x30101014));
            Gfx.textCentered(c, cxr + cw / 2, qy + (14 - Gfx.lineHeight(7, true)) / 2, 7, true, sel ? Theme.TEXT : MenuBg.INK);
            cxr -= 3;
        }
        Gfx.fade = 1f;
        FriendToast.render();
    }

    static final int INK = MenuBg.INK, INK2 = MenuBg.INK2;

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (width >= 520 && RecentServers.click(mx, my, recentY, this)) return;
        if (width >= 520 && mx >= pillX && mx < pillX + pillW && my >= pillY && my < pillY + 16) { mc.displayGuiScreen(new AccountScreen(this)); return; }
        {
            int cxr = width - 16;
            for (int i = Lang.CODES.length - 1; i >= 0; i--) {
                int cw = Gfx.width(Lang.CODES[i].toUpperCase(), 7, true) + 10;
                cxr -= cw;
                if (mx >= cxr && mx < cxr + cw && my >= qy && my < qy + 14) { Lang.load(Lang.CODES[i]); Config.save(); return; }
                cxr -= 3;
            }
        }
        if (width >= 520 && mx >= nx && mx < nx + nw && my >= ny && my < ny + nh) { mc.displayGuiScreen(new NewsScreen(this)); return; }
        for (int i = 0; i < 3; i++) {
            int y = btnY(i);
            if (mx >= bx && mx < bx + BW && my >= y && my < y + btnH(i)) {
                if (i == 0) mc.displayGuiScreen(new WorldListScreen(this));
                else if (i == 1) mc.displayGuiScreen(new ServerListScreen(this));
                else mc.displayGuiScreen(new MenuScreen());
                return;
            }
        }
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (TW + DG);
            if (mx >= x && mx < x + TW && my >= dockY && my < dockY + TH) {
                if (i == 0) mc.displayGuiScreen(new MenuScreen(4));
                else if (i == 1) mc.displayGuiScreen(new FriendsScreen(this));
                else if (i == 2) mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                else mc.shutdown();
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typed, int key) {}

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

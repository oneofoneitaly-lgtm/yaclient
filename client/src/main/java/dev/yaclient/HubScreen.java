package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

/**
 * The small menu in the middle of the screen (Right Shift): one clean panel with the client header,
 * a wide Mods tile and four tiles for cosmetics, friends, HUD editor and settings.
 */
public class HubScreen extends GuiScreen {
    private static final int PW = 320, PH = 202, PAD = 16, GAP = 8, R = 6;
    private static final int PANEL = 0x700F1012, SURF = 0x701A1B1F, SURF2 = 0x80222328, SURF3 = 0x902D2E34;
    private static final String[] ICONS = {"grid", "shirt", "friends", "move", "gear"};
    private static final String[] LABELS = {"hub.mods", "hub.cosmetics", "hub.friends", "hub.hud", "hub.settings"};

    private final float[] hover = new float[5];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int px, py;
    private int nx, ny;
    private static final int NW = 165, NH = 110;
    private float newsHover;
    private float sc = 1f;

    private int[] rect(int i) {
        if (i == 0) return new int[]{px + PAD, py + 60, PW - 2 * PAD, 52};
        int w = (PW - 2 * PAD - 3 * GAP) / 4;
        return new int[]{px + PAD + (i - 1) * (w + GAP), py + 60 + 52 + GAP, w, 60};
    }

    @Override
    public void drawScreen(int gmx, int gmy, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 220f);
        drawGradientRect(0, 0, width, height, Ui.alpha(0x20000000, fade), Ui.alpha(0x38000000, fade));

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        // fixed physical size on every GUI scale: about 1 pixel per unit at 1080p
        float f = Math.max(1.4f, Minecraft.getMinecraft().displayHeight / 1080f * 1.85f);
        sc = f / gs;
        int vw = Math.round(width / sc), vh = Math.round(height / sc);
        int mx = Math.round(gmx / sc), my = Math.round(gmy / sc);
        Gfx.setScale(Math.max(1, Math.round(f)));
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.scale(sc, sc, 1f);
        Gfx.fade = fade;

        px = (vw - PW) / 2;
        py = (vh - PH) / 2 + Math.round((1f - fade) * 6f);

        Gfx.rr(px - 2, py - 2, PW + 4, PH + 4, 9, 0x22000000);
        Gfx.rr(px, py, PW, PH, 8, PANEL);
        Gfx.rrOutline(px, py, PW, PH, 8, 1, 0x24FFFFFF);

        // header
        Logo.draw(px + PAD, py + 14, 34, 34, fade);
        Gfx.text("CLIENT", px + PAD + 42, py + 16, 14, true, Theme.BRAND_RED);
        Gfx.text("Minecraft 1.8.9", px + PAD + 42, py + 16 + Gfx.lineHeight(14, true) + 2, 8, false, Theme.DIM);
        if (CosmeticsApi.enabled() && CosmeticsApi.plus()) {
            int w = Gfx.width("PLUS", 8, true) + 16;
            Gfx.rr(px + PW - PAD - w, py + 20, w, 18, 5, 0xFFFFC21A);
            Gfx.textCentered("PLUS", px + PW - PAD - w / 2, py + 20 + (18 - Gfx.lineHeight(8, true)) / 2, 8, true, 0xFF101014);
        }

        for (int i = 0; i < 5; i++) {
            int[] r = rect(i);
            boolean over = mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            Gfx.rr(r[0], r[1], r[2], r[3], R, Ui.mix(SURF, SURF3, hover[i] * 0.8f));
            Gfx.rrOutline(r[0], r[1], r[2], r[3], R, 1, Ui.mix(0x14FFFFFF, 0x33FFFFFF, hover[i]));
            int fg = Ui.mix(0xFFD8D8DC, Theme.TEXT, hover[i]);
            if (i == 0) {
                Gfx.rr(r[0] + 10, r[1] + 8, 36, 36, 6, Ui.mix(SURF3, 0xFF3A3B42, hover[i]));
                Gfx.icon(ICONS[0], r[0] + 18, r[1] + 16, 20, fg);
                Gfx.text(Lang.t(LABELS[0]), r[0] + 56, r[1] + 11, 12, true, Theme.TEXT);
                int total = 0, on = 0;
                for (Module m : YAClient.MODULES) { total++; if (m.enabled) on++; }
                Gfx.text(Lang.t("ui.count", total, on), r[0] + 56, r[1] + 11 + Gfx.lineHeight(12, true) + 3, 8, false, Theme.MUTED);
                Gfx.icon("arrow_right", r[0] + r[2] - 28, r[1] + (r[3] - 14) / 2, 14, Ui.mix(Theme.DIM, Theme.TEXT, hover[i]));
            } else {
                Gfx.rr(r[0] + (r[2] - 28) / 2, r[1] + 6, 28, 28, 4, Ui.mix(SURF3, 0xFF3A3B42, hover[i]));
                Gfx.icon(ICONS[i], r[0] + (r[2] - 16) / 2, r[1] + 12, 16, fg);
                Gfx.textCentered(Gfx.trim(Lang.t(LABELS[i]), 8, false, r[2] - 6), r[0] + r[2] / 2, r[1] + 40, 8, false, Ui.mix(Theme.MUTED, Theme.TEXT, hover[i]));
            }
        }
        Gfx.textCentered(Lang.t("ui.esc_hint"), px + PW / 2, py + PH - 16, 7, false, Theme.DIM);

        // changelog card (bottom left), same as on the main menu
        nx = 10;
        ny = vh - 20 - 8 - NH;
        boolean nOver = mx >= nx && mx < nx + NW && my >= ny && my < ny + NH;
        newsHover = Ui.approach(newsHover, nOver ? 1f : 0f, dt, 16f);
        Gfx.rr(nx, ny, NW, NH, 4, Ui.mix(0xE0101014, 0xF0242429, newsHover));
        int artH = Math.round((NW - 8) / NewsArt.ASPECT);
        NewsArt.draw(nx + 4, ny + 4, NW - 8, artH, fade);
        Gfx.rrOutline(nx, ny, NW, NH, 4, 1, Ui.mix(0x38FFFFFF, 0x80FFFFFF, newsHover));
        Gfx.text(Lang.t("news.card"), nx + 9, ny + 4 + artH + 6, 11, true, Theme.TEXT);
        Gfx.text(Gfx.trim("Update " + NewsScreen.VERSION + " · " + Lang.t("news.open"), 9, false, NW - 18), nx + 9, ny + 4 + artH + 8 + Gfx.lineHeight(11, true), 9, false, Theme.MUTED);

        Gfx.fade = 1f;
        net.minecraft.client.renderer.GlStateManager.popMatrix();
        FriendToast.render();
    }

    @Override
    protected void mouseClicked(int gmx, int gmy, int button) {
        if (button != 0) return;
        int mx = Math.round(gmx / sc), my = Math.round(gmy / sc);
        Minecraft mc = Minecraft.getMinecraft();
        if (mx >= nx && mx < nx + NW && my >= ny && my < ny + NH) { mc.displayGuiScreen(new NewsScreen(this)); return; }
        for (int i = 0; i < 5; i++) {
            int[] r = rect(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                if (i == 0) mc.displayGuiScreen(new MenuScreen(0));
                else if (i == 1) mc.displayGuiScreen(new MenuScreen(4));
                else if (i == 2) mc.displayGuiScreen(new FriendsScreen(this));
                else if (i == 3) mc.displayGuiScreen(new HudEditorScreen());
                else mc.displayGuiScreen(new MenuScreen(5));
                return;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** The small menu in the middle of the screen (Right Shift): a wide Mods tile, then Cosmetics, HUD editor and Store. Same as 1.8.9. */
public class HubScreen extends Screen {
    private static final int PW = 320, PH = 190, PAD = 16, GAP = 8, R = 6;
    private static final int PANEL = 0x700F1012, SURF = 0x701A1B1F, SURF3 = 0x902D2E34;
    private static final String[] ICONS = {"grid", "shirt", "move", "gear"};
    private static final String[] LABELS = {"Mods", "Cosmetics", "HUD editor", "Store"};
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("yaclient", "textures/gui/logo.png");

    private final float[] hover = new float[4];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int px, py;
    private float sc = 1f;

    public HubScreen() { super(Component.literal("YA CLIENT")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) { }

    private int[] rect(int i) {
        if (i == 0) return new int[]{px + PAD, py + 60, PW - 2 * PAD, 52};
        int w = (PW - 2 * PAD - 2 * GAP) / 3;
        return new int[]{px + PAD + (i - 1) * (w + GAP), py + 60 + 52 + GAP, w, 60};
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 220f);
        Gfx.g = g;
        g.fill(0, 0, width, height, Ui.alpha(0x40000000, fade));
        int gs = Math.max(1, mc.getWindow().getGuiScale());
        float phys = Math.max(1.4f, mc.getWindow().getHeight() / 1080f * 1.85f);   // same physical size on every GUI scale
        sc = Math.min(phys / gs, Math.min((height - 16f) / PH, (width - 16f) / PW));
        Gfx.setScale(Math.max(1, Math.round(phys * 2f)));
        Gfx.fade = fade;
        px = 0;
        py = Math.round((1f - fade) * 6f);
        mx = Math.round((mx - width / 2f) / sc + PW / 2f);
        my = Math.round((my - height / 2f) / sc + PH / 2f);
        g.pose().pushMatrix();
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(sc, sc);
        g.pose().translate(-PW / 2f, -PH / 2f);

        Gfx.rr(px - 2, py - 2, PW + 4, PH + 4, 9, 0x22000000);
        Gfx.rr(px, py, PW, PH, 8, PANEL);
        Gfx.rrOutline(px, py, PW, PH, 8, 1, 0x24FFFFFF);

        g.blit(RenderPipelines.GUI_TEXTURED, LOGO, px + PAD, py + 14, 0f, 0f, 34, 34, 128, 128, 128, 128, Ui.alpha(0xFFFFFFFF, fade));
        Gfx.text("CLIENT", px + PAD + 42, py + 16, 14, true, Theme.BRAND_RED);
        Gfx.text("Minecraft " + Minecraft.getInstance().getLaunchedVersion(), px + PAD + 42, py + 16 + Gfx.lineHeight(14, true) + 2, 8, false, Theme.DIM);
        if (CosmeticsApi.enabled() && CosmeticsApi.plus()) {
            int w = Gfx.width("PLUS", 8, true) + 16;
            Gfx.rr(px + PW - PAD - w, py + 20, w, 18, 5, 0xFFFFC21A);
            Gfx.text("PLUS", px + PW - PAD - w + 8, py + 20 + (18 - Gfx.lineHeight(8, true)) / 2, 8, true, 0xFF101014);
        }

        for (int i = 0; i < 4; i++) {
            int[] r = rect(i);
            boolean over = mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            Gfx.rr(r[0], r[1], r[2], r[3], R, Ui.mix(SURF, SURF3, hover[i] * 0.8f));
            Gfx.rrOutline(r[0], r[1], r[2], r[3], R, 1, Ui.mix(0x14FFFFFF, 0x33FFFFFF, hover[i]));
            int fg = Ui.mix(0xFFD8D8DC, Theme.TEXT, hover[i]);
            String ic = Gfx.hasIcon(ICONS[i]) ? ICONS[i] : "grid";
            if (i == 0) {
                Gfx.rr(r[0] + 10, r[1] + 8, 36, 36, 6, Ui.mix(SURF3, 0xFF3A3B42, hover[i]));
                Gfx.icon(ic, r[0] + 18, r[1] + 16, 20, fg);
                Gfx.text(LABELS[0], r[0] + 56, r[1] + 11, 12, true, Theme.TEXT);
                int total = 0, on = 0;
                for (Module m : Modules.ALL) { total++; if (m.enabled) on++; }
                Gfx.text(total + " mods  -  " + on + " on", r[0] + 56, r[1] + 11 + Gfx.lineHeight(12, true) + 3, 8, false, Theme.MUTED);
                Gfx.icon("arrow_right", r[0] + r[2] - 28, r[1] + (r[3] - 14) / 2, 14, Ui.mix(Theme.DIM, Theme.TEXT, hover[i]));
            } else {
                Gfx.rr(r[0] + (r[2] - 28) / 2, r[1] + 6, 28, 28, 4, Ui.mix(SURF3, 0xFF3A3B42, hover[i]));
                Gfx.icon(ic, r[0] + (r[2] - 16) / 2, r[1] + 12, 16, fg);
                Gfx.textCentered(LABELS[i], r[0] + r[2] / 2, r[1] + 40, 8, false, Ui.mix(Theme.MUTED, Theme.TEXT, hover[i]));
            }
        }
        Gfx.textCentered("Esc to close", px + PW / 2, py + PH - 16, 7, false, Theme.DIM);
        g.pose().popMatrix();
        Gfx.fade = 1f;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        Minecraft mc = Minecraft.getInstance();
        double ex = (e.x() - width / 2f) / sc + PW / 2f, ey = (e.y() - height / 2f) / sc + PH / 2f;
        for (int i = 0; i < 4; i++) {
            int[] r = rect(i);
            if (ex >= r[0] && ex < r[0] + r[2] && ey >= r[1] && ey < r[1] + r[3]) {
                if (i == 0) mc.setScreenAndShow(new MenuScreen(0, false));
                else if (i == 1) mc.setScreenAndShow(new MenuScreen(5, false));
                else if (i == 2) mc.setScreenAndShow(new MenuScreen(0, true));
                else CosmeticsApi.openStore();
                return true;
            }
        }
        return super.mouseClicked(e, dbl);
    }
}

package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * YA CLIENT title screen, same as the 1.8.9 one: bright backdrop, logo, stacked buttons with a highlighted Play,
 * an icon dock, the player's own skin on the right and recent servers on the left. Replaces the vanilla title screen.
 */
public class YaTitleScreen extends Screen {
    private static final int BW = 150, BH = 22, PBH = 28, GAP = 6, TW = 34, TH = 25, DG = 6;
    private static final String[] ICONS = {"shirt", "gear", "close"};
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("yaclient", "textures/gui/logo.png");
    private static final int RW = 118, RH = 26;

    private final float[] hover = new float[3];
    private final float[] dock = new float[ICONS.length];
    private final float[] rhover = new float[3];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int bx, by0, dockX, dockY, top, contentH, rx, ry;
    private PlayerSkinWidget skin;
    private ServerData[] recent = new ServerData[0];

    public YaTitleScreen() { super(Component.literal("YA CLIENT")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) { }

    private int btnH(int i) { return i == 0 ? PBH : BH; }

    private int btnY(int i) { return i == 0 ? by0 : by0 + PBH + GAP + (i - 1) * (BH + GAP); }

    private void layout() {
        int logo = Math.min(50, height / 6), badge = logo + 14;
        contentH = badge + 14 + PBH + 2 * BH + 3 * GAP + 12 + TH;
        top = Math.max(10, (height - contentH) / 2);
        bx = width / 2 - BW / 2;
        by0 = top + badge + 14;
        int dockW = ICONS.length * TW + (ICONS.length - 1) * DG;
        dockX = width / 2 - dockW / 2;
        dockY = btnY(2) + BH + 12;
        rx = 16;
        ry = Math.max(10, (height - (14 + recent.length * (RH + 4))) / 2);
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        // recent servers (names and icons from the saved list when the server is there)
        try {
            ServerList sl = new ServerList(mc);
            sl.load();
            List<String> ips = Servers.recent();
            int n = Math.min(3, ips.size());
            recent = new ServerData[n];
            for (int i = 0; i < n; i++) {
                ServerData sd = sl.get(ips.get(i));
                recent[i] = sd != null ? sd : new ServerData(ips.get(i), ips.get(i), ServerData.Type.OTHER);
            }
        } catch (Throwable e) { recent = new ServerData[0]; }
        layout();
        // the player's own skin, turnable with the mouse
        skin = null;
        if (width >= 520) {
            try {
                int ph = Math.max(60, Math.min(150, contentH - 30));
                skin = new PlayerSkinWidget(ph * 6 / 10, ph, mc.getEntityModels(), mc.getSkinManager().createLookup(mc.getGameProfile(), false));
                addWidget(skin);
            } catch (Throwable e) { skin = null; }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float t = (now - opened) / 1000f;
        float fadeIn = Ui.ease((now - opened) / 500f);

        MenuBg.draw(g, width, height, t);
        Gfx.g = g;
        Gfx.setScale(Math.max(1, mc.getWindow().getGuiScale()));
        Gfx.fade = fadeIn;
        layout();
        int cx = width / 2;
        int logo = Math.min(50, height / 6);

        // the player: in the free space on the right, with a soft floor shadow and the name below
        if (skin != null) {
            int rightStart = cx + BW / 2 + 20;
            int pcx = rightStart + (width - 16 - rightStart) * 72 / 100;
            int feet = top + contentH - 24;
            skin.setX(pcx - skin.getWidth() / 2);
            skin.setY(feet - skin.getHeight());
            MenuBg.blob(pcx, feet + 2, 46f, 8f, 0x1A2238, 75);
            skin.extractRenderState(g, mx, my, delta);
            String nm = mc.getUser().getName();
            int pw = Gfx.width(nm, 9, true) + 22, py = feet + 8;
            Gfx.rr(pcx - pw / 2, py + 1, pw, 16, 8, 0x22101830);
            Gfx.rr(pcx - pw / 2, py, pw, 16, 8, 0xE0101014);
            Gfx.rrOutline(pcx - pw / 2, py, pw, 16, 8, 1, 0x40FFFFFF);
            Gfx.textCentered(nm, pcx, py + (16 - Gfx.lineHeight(9, true)) / 2, 9, true, Theme.TEXT);
        }

        // logo: dark outline copies for contrast on the bright screen + a soft shadow
        float bob = (float) Math.sin(t * 1.4f) * 1.2f;
        int lx = cx - logo / 2, ly = Math.round(top + bob + 6);
        MenuBg.blob(cx, ly + logo + 4, logo * 0.7f, 4f, 0x1A2238, 60);
        int dark = Ui.alpha(0xFF1A1C26, 0.92f * fadeIn);
        for (int k = 0; k < 8; k++) {
            double ang = k * Math.PI / 4;
            int ox = (int) Math.round(Math.cos(ang) * 1.5), oy = (int) Math.round(Math.sin(ang) * 1.5);
            g.blit(RenderPipelines.GUI_TEXTURED, LOGO, lx + ox, ly + oy, 0f, 0f, logo, logo, 128, 128, 128, 128, dark);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO, lx, ly, 0f, 0f, logo, logo, 128, 128, 128, 128, Ui.alpha(0xFFFFFFFF, fadeIn));

        // main buttons
        String[] labels = {"Singleplayer", "Multiplayer", "Client settings"};
        for (int i = 0; i < 3; i++) {
            int x = bx, y = btnY(i), h = btnH(i);
            boolean over = mx >= x && mx < x + BW && my >= y && my < y + h;
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            int lift = Math.round(hover[i]);
            Gfx.rr(x, y + 2, BW, h, 8, i == 0 ? Ui.alpha(Theme.BRAND_RED, 0.30f) : 0x26101830);
            if (i == 0) {
                Gfx.rr(x, y - lift, BW, h, 8, Ui.mix(Theme.BRAND_RED, 0xFFFFFFFF, 0.12f * hover[i]));
                Gfx.rrOutline(x, y - lift, BW, h, 8, 1, Ui.mix(0x50FFFFFF, 0xA0FFFFFF, hover[i]));
                Gfx.textCentered(labels[i], x + BW / 2, y - lift + (h - Gfx.lineHeight(11, true)) / 2, 11, true, 0xFFFFFFFF);
            } else {
                Gfx.rr(x, y - lift, BW, h, 8, Ui.mix(0xD0101014, 0xE8242429, hover[i]));
                Gfx.rrOutline(x, y - lift, BW, h, 8, 1, Ui.mix(0x40FFFFFF, 0x80FFFFFF, hover[i]));
                Gfx.textCentered(labels[i], x + BW / 2, y - lift + (h - Gfx.lineHeight(9, true)) / 2, 9, true, Theme.TEXT);
            }
        }

        // icon dock: cosmetics, Minecraft options, quit
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

        // recent servers on the left
        if (width >= 520 && recent.length > 0) {
            Gfx.text("RECENT SERVERS", rx + 2, ry, 7, true, MenuBg.INK2);
            for (int i = 0; i < recent.length; i++) {
                ServerData sd = recent[i];
                int y = ry + 14 + i * (RH + 4);
                boolean over = mx >= rx && mx < rx + RW && my >= y && my < y + RH;
                rhover[i] = Ui.approach(rhover[i], over ? 1f : 0f, dt, 16f);
                Gfx.rr(rx, y + 2, RW, RH, 7, 0x26101830);
                Gfx.rr(rx, y, RW, RH, 7, Ui.mix(0xD0101014, 0xE8242429, rhover[i]));
                Gfx.rrOutline(rx, y, RW, RH, 7, 1, Ui.mix(0x40FFFFFF, 0x80FFFFFF, rhover[i]));
                Servers.icon(sd, rx + 4, y + 4, 18);
                Gfx.text(Gfx.trim(Servers.name(sd), 8, true, RW - 32), rx + 27, y + 4, 8, true, Theme.TEXT);
                Gfx.text(Gfx.trim(sd.ip, 7, false, RW - 32), rx + 27, y + 14, 7, false, Theme.MUTED);
            }
        }

        // footer
        Gfx.text("CLIENT " + mc.getLaunchedVersion(), 16, height - 14, 8, true, MenuBg.INK2);
        Gfx.fade = 1f;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        Minecraft mc = Minecraft.getInstance();
        if (e.button() == 0) {
            layout();
            for (int i = 0; i < 3; i++) {
                int y = btnY(i);
                if (mx >= bx && mx < bx + BW && my >= y && my < y + btnH(i)) {
                    if (i == 0) mc.setScreenAndShow(new WorldListScreen(this));
                    else if (i == 1) mc.setScreenAndShow(new ServerListScreen(this));
                    else mc.setScreenAndShow(new MenuScreen(0, false));
                    return true;
                }
            }
            for (int i = 0; i < ICONS.length; i++) {
                int x = dockX + i * (TW + DG);
                if (mx >= x && mx < x + TW && my >= dockY && my < dockY + TH) {
                    if (i == 0) mc.setScreenAndShow(new MenuScreen(5, false));
                    else if (i == 1) mc.setScreenAndShow(new OptionsScreen(this, mc.options, false));
                    else mc.stop();
                    return true;
                }
            }
            if (width >= 520) for (int i = 0; i < recent.length; i++) {
                int y = ry + 14 + i * (RH + 4);
                if (mx >= rx && mx < rx + RW && my >= y && my < y + RH) { Servers.join(this, recent[i]); return true; }
            }
        }
        return super.mouseClicked(e, dbl);
    }
}

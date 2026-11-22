package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

/** The game menu (Esc) restyled: the vanilla buttons and actions are kept, only the look changes, plus a dock to the client screens. */
public class PauseScreen extends GuiIngameMenu {
    private static final String[] ICONS = {"gear", "shirt", "friends"};
    private static final int TW = 34, TH = 25, DG = 6;
    private final long opened = System.currentTimeMillis();
    private final float[] dock = new float[ICONS.length];
    private long last = opened;

    private int dockX() { return width / 2 - (ICONS.length * TW + (ICONS.length - 1) * DG) / 2; }
    private int dockY() { return height - 14 - TH; }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 250f);
        drawGradientRect(0, 0, width, height, Ui.alpha(0xD0101820, fade), Ui.alpha(0xE0101820, fade));
        Gfx.setScale(Math.max(1, new ScaledResolution(mc).getScaleFactor()));
        Gfx.fade = fade;
        int logo = Math.min(40, height / 9);
        Logo.draw(width / 2 - logo / 2f, 12, logo, logo, fade);
        Gfx.textCentered(Lang.t("pause.title"), width / 2, 12 + logo + 4, 9, true, Theme.MUTED);
        for (Object o : buttonList) {
            GuiButton b = (GuiButton) o;
            if (!b.visible) continue;
            boolean over = b.enabled && mx >= b.xPosition && mx < b.xPosition + b.getButtonWidth() && my >= b.yPosition && my < b.yPosition + 20;
            LoadingArt.button(b.xPosition, b.yPosition, b.getButtonWidth(), 20, b.displayString, b.id == 4, over, b.enabled);
        }
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX() + i * (TW + DG), y = dockY();
            boolean over = mx >= x && mx < x + TW && my >= y && my < y + TH;
            dock[i] = Ui.approach(dock[i], over ? 1f : 0f, dt, 16f);
            Gfx.rr(x, y + 2, TW, TH, 9, 0x26101830);
            Gfx.rr(x, y, TW, TH, 9, Ui.mix(0xD0101014, 0xE8242429, dock[i]));
            Gfx.rrOutline(x, y, TW, TH, 9, 1, Ui.mix(0x40FFFFFF, 0x80FFFFFF, dock[i]));
            Gfx.icon(ICONS[i], x + (TW - 16) / 2, y + (TH - 16) / 2, 16, Ui.mix(0xFFD8D8DC, Theme.TEXT, dock[i]));
        }
        Gfx.fade = 1f;
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button == 0) {
            for (int i = 0; i < ICONS.length; i++) {
                int x = dockX() + i * (TW + DG), y = dockY();
                if (mx >= x && mx < x + TW && my >= y && my < y + TH) {
                    if (i == 0) mc.displayGuiScreen(new MenuScreen());
                    else if (i == 1) mc.displayGuiScreen(new MenuScreen(4));
                    else mc.displayGuiScreen(new FriendsScreen(this));
                    return;
                }
            }
        }
        super.mouseClicked(mx, my, button);
    }
}

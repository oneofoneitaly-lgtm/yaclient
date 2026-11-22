package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

/** The "connecting to a server" screen in the client's style; the connection itself is vanilla's. */
public class YAConnecting extends GuiConnecting {
    private final long opened = System.currentTimeMillis();
    private final String host;

    public YAConnecting(GuiScreen parent, Minecraft mc, ServerData sd) {
        super(parent, mc, sd);
        this.host = sd == null ? "" : sd.serverIP;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (!buttonList.isEmpty()) {
            GuiButton b = (GuiButton) buttonList.get(0);
            b.xPosition = width / 2 - b.getButtonWidth() / 2;
            b.yPosition = LoadingArt.barY(height) + 20;
        }
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        LoadingArt.draw(width, height, Lang.t("load.connecting"), host, opened);
        if (!buttonList.isEmpty()) {
            GuiButton b = (GuiButton) buttonList.get(0);
            Gfx.setScale(Math.max(1, new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor()));
            boolean over = mx >= b.xPosition && mx < b.xPosition + b.getButtonWidth() && my >= b.yPosition && my < b.yPosition + 20;
            LoadingArt.button(b.xPosition, b.yPosition, b.getButtonWidth(), 20, Lang.t("srv.cancel"), false, over, true);
        }
    }
}

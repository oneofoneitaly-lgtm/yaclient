package dev.yaclient;

import dev.yaclient.modules.AutoReconnectModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/** Wraps the vanilla "Disconnected" screen and reconnects after a few seconds (any click or key cancels). */
public class ReconnectScreen extends GuiScreen {
    private final GuiScreen inner;
    private final long end;
    private boolean cancelled;

    public ReconnectScreen(GuiScreen inner, int seconds) {
        this.inner = inner;
        this.end = System.currentTimeMillis() + seconds * 1000L;
    }

    @Override
    public void initGui() { inner.setWorldAndResolution(mc, width, height); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        inner.drawScreen(mx, my, pt);
        String s;
        if (cancelled) s = "Auto reconnect cancelled";
        else s = "Reconnecting in " + Math.max(0, (end - System.currentTimeMillis() + 999) / 1000) + "s  (click or press a key to cancel)";
        drawCenteredString(fontRendererObj, s, width / 2, height - 24, 0xFFFFFF);
    }

    @Override
    public void updateScreen() {
        inner.updateScreen();
        if (!cancelled && System.currentTimeMillis() >= end) {
            ServerData sd = RecentServers.lastData;
            if (sd == null) { cancelled = true; return; }
            AutoReconnectModule.tries++;
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(new YAConnecting(new ServerListScreen(new MainMenuScreen()), mc, new ServerData(sd.serverName, sd.serverIP, false)));
        }
    }

    @Override
    public void handleMouseInput() {
        if (Mouse.getEventButtonState()) cancelled = true;
        try { inner.handleMouseInput(); } catch (Exception ignored) { }
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.getEventKeyState()) cancelled = true;
        try { inner.handleKeyboardInput(); } catch (Exception ignored) { }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

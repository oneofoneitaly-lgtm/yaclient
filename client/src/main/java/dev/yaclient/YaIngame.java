package dev.yaclient;

import dev.yaclient.modules.CrosshairModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;

/**
 * In-game GUI that skips the vanilla crosshair icon while the YA crosshair module is on
 * (no Mixin: the crosshair is the 16x16 icon at u=0, v=0 centered on the screen).
 */
public class YaIngame extends GuiIngame {
    private final Minecraft game;

    public YaIngame(Minecraft mc) { super(mc); this.game = mc; }

    @Override
    public void drawTexturedModalRect(int x, int y, int u, int v, int w, int h) {
        if (w == 16 && h == 16 && u == 0 && v == 0 && CrosshairModule.hidesVanilla()) {
            ScaledResolution sr = new ScaledResolution(game);
            if (x == sr.getScaledWidth() / 2 - 7 && y == sr.getScaledHeight() / 2 - 7) return;
        }
        super.drawTexturedModalRect(x, y, u, v, w, h);
    }

    /** Swaps the game's GUI for this one (once, at the main menu so no chat history is lost). */
    public static void install() {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            if (mc.ingameGUI != null && !(mc.ingameGUI instanceof YaIngame)) mc.ingameGUI = new YaIngame(mc);
        } catch (Throwable t) {
            System.out.println("[YA CLIENT] crosshair hook not installed: " + t);
        }
    }
}

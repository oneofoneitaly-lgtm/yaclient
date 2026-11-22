package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Shows the YA logo next to the name of every YA CLIENT player in the tab list.
 * The vanilla tab overlay is swapped for a subclass (found by field type, so it works with any mapping) that
 * reserves a little room in front of the name and draws the logo there, at the exact row position.
 */
final class TabBadge {
    private TabBadge() {}

    private static boolean failed;

    /** Called every tick: installs the custom tab overlay once. */
    static void tick() {
        if (failed) return;
        Minecraft mc = Minecraft.getMinecraft();
        GuiIngame gui = mc.ingameGUI;
        if (gui == null) return;
        try {
            if (gui.getTabList() instanceof YaTabList) return;
            Field slot = null;
            for (Field f : GuiIngame.class.getDeclaredFields()) {
                if (f.getType() == GuiPlayerTabOverlay.class && !Modifier.isStatic(f.getModifiers())) { slot = f; break; }
            }
            if (slot == null) { failed = true; return; }
            slot.setAccessible(true);
            GuiPlayerTabOverlay old = (GuiPlayerTabOverlay) slot.get(gui);
            YaTabList neu = new YaTabList(mc, gui);
            for (Field f : GuiPlayerTabOverlay.class.getDeclaredFields()) {   // keep header, footer and timers
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(neu, f.get(old));
            }
            slot.set(gui, neu);
        } catch (Throwable t) {
            failed = true;   // never break the game because of a cosmetic detail
        }
    }

    private static final class YaTabList extends GuiPlayerTabOverlay {
        private static final String RESERVE = "   ";        // 3 spaces = 12px kept free in front of the name

        YaTabList(Minecraft mc, GuiIngame gui) { super(mc, gui); }

        private boolean badge(NetworkPlayerInfo info) {
            return Config.getBool("tab.badge", true) && info != null && info.getGameProfile() != null
                    && info.getGameProfile().getId() != null && CosmeticsApi.usesClient(info.getGameProfile().getId());
        }

        @Override
        public String getPlayerName(NetworkPlayerInfo info) {
            String n = super.getPlayerName(info);
            if (!badge(info)) return n;
            return RESERVE + n;
        }

        @Override
        protected void drawPing(int width, int x, int y, NetworkPlayerInfo info) {
            super.drawPing(width, x, y, info);
            if (!badge(info)) return;
            Minecraft mc = Minecraft.getMinecraft();
            boolean head = mc.isIntegratedServerRunning() || mc.getNetHandler().getNetworkManager().getIsencrypted();
            int bx = x + (head ? 9 : 0);
            Logo.draw(bx + 1, y, 9, 9, 1f, CosmeticsApi.rank(info.getGameProfile().getId()));
        }
    }
}

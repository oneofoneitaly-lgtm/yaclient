package dev.yaclient;

import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.network.NetHandlerPlayClient;

/** "Loading terrain" shown while joining a world, in the client's style. */
public class YADownload extends GuiDownloadTerrain {
    private final long opened = System.currentTimeMillis();

    public YADownload(NetHandlerPlayClient handler) { super(handler); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        LoadingArt.draw(width, height, Lang.t("load.terrain"), Lang.t("load.terrain_d"), opened);
    }
}

package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.multiplayer.ServerData;

/** The address of the server you are on, with the server's own icon next to it. */
public class ServerAddressModule extends PillModule {
    public ServerAddressModule() { super("server_ip", Category.INFO, false, 0.008f, 0.28f); }

    private final Setting.Bool showIcon = add("layout", new Setting.Bool("show_icon", true));
    private final Setting.Bool showName = add("layout", new Setting.Bool("show_name", true));
    private final Setting.Num iconSize = add("layout", new Setting.Num("icon_size", 8f, 28f, 1f, 12f, ""));

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        if (mc.getCurrentServerData() == null) return "Singleplayer";
        if (StreamerModeModule.hideIp()) return "hidden";
        return "IP: " + mc.getCurrentServerData().serverIP;
    }

    @Override
    protected String sample() { return "IP: play.example.net"; }

    @Override protected boolean iconOn() { return showIcon.value; }
    @Override protected int iconPx() { return Math.round(iconSize.value); }
    @Override protected boolean textOn() { return showName.value || !showIcon.value; }

    @Override
    protected void drawIcon(int x, int y, int s) {
        ServerData sd = mc == null ? null : mc.getCurrentServerData();
        boolean hidden = sd != null && StreamerModeModule.hideIp();
        if (sd != null && !hidden && IconDraw.bindServer(sd.getBase64EncodedIconData())) { IconDraw.quad(x, y, s); return; }
        IconDraw.letter(x, y, s, sd == null ? (preview ? "S" : "S") : hidden ? "?" : (sd.serverName != null && !sd.serverName.isEmpty() ? sd.serverName : sd.serverIP));
    }
}

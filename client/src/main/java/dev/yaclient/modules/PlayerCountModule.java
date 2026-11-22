package dev.yaclient.modules;

public class PlayerCountModule extends PillModule {
    public PlayerCountModule() { super("players", Category.INFO, false, 0.008f, 0.44f); }

    @Override
    protected String text() {
        if (mc == null || mc.getNetHandler() == null) return "";
        return "Players " + mc.getNetHandler().getPlayerInfoMap().size();
    }

    @Override
    protected String sample() { return "Players 24"; }
}

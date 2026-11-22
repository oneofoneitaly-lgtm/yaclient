package dev.yaclient.modules;

public class XpLevelModule extends PillModule {
    public XpLevelModule() { super("xp", Category.INFO, false, 0.008f, 0.60f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        return "Level " + mc.thePlayer.experienceLevel;
    }

    @Override
    protected String sample() { return "Level 30"; }
}

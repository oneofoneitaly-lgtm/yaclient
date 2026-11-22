package dev.yaclient.modules;

public class EntityCountModule extends PillModule {
    public EntityCountModule() { super("entities", Category.INFO, false, 0.008f, 0.52f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        return "Entities: " + mc.theWorld.loadedEntityList.size();
    }

    @Override
    protected String sample() { return "Entities: 128"; }
}

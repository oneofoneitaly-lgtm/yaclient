package dev.yaclient.modules;

/** Shows entity hitboxes (same as F3 + B). Re-applied every tick and frame so nothing can switch it off. */
public class HitboxesModule extends Module {
    private boolean active;

    public HitboxesModule() { super("hitboxes", Category.PVP, false); }

    @Override
    public void onTick() { apply(); }

    @Override
    public void onRender() { apply(); }

    private void apply() {
        if (mc == null || mc.getRenderManager() == null) return;
        mc.getRenderManager().setDebugBoundingBox(true);
        active = true;
    }

    @Override
    public void onAlwaysTick() {
        if (!enabled && active && mc != null && mc.getRenderManager() != null) {
            mc.getRenderManager().setDebugBoundingBox(false);
            active = false;
        }
    }
}

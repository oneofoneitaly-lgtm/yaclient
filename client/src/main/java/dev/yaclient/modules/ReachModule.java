package dev.yaclient.modules;

import net.minecraft.util.MovingObjectPosition;

import java.util.Locale;

/** Shows the distance to the entity under the crosshair (information only). */
public class ReachModule extends PillModule {
    private double last;
    private long lastTime;

    public ReachModule() { super("reach", Category.PVP, false, 0.99f, 0.22f); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.hitVec != null) {
            last = mop.hitVec.distanceTo(mc.thePlayer.getPositionEyes(1.0F));
            lastTime = System.currentTimeMillis();
        }
    }

    @Override
    protected String text() {
        if (System.currentTimeMillis() - lastTime < 1500) return String.format(Locale.ROOT, "Reach: %.2f blocks", last);
        return "Reach: --";
    }
}

package dev.yaclient.modules;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

/** White hearts with the target's health on every hit you land. */
public class TargetHudModule extends HudModule {
    private final dev.yaclient.Setting.Bool popup = add(new dev.yaclient.Setting.Bool("hit_popup", true));
    private static TargetHudModule inst;

    public TargetHudModule() { super("target_hud", Category.PVP, false, 0.40f, 0.70f); inst = this; }

    /** White heart with the target's health popping up on every hit you land. */
    public static boolean popupsOn() { return inst != null && inst.enabled && inst.popup.value; }

    @Override
    public int width() { return 1; }

    @Override
    public int height() { return 1; }

    private EntityLivingBase current() {
        if (mc == null || mc.thePlayer == null) return null;
        MovingObjectPosition m = mc.objectMouseOver;
        if (m != null && m.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && m.entityHit instanceof EntityLivingBase) {
            HitTracker.aimed((EntityLivingBase) m.entityHit);
        }
        return HitTracker.target();
    }

    /** No box is drawn any more: the module only keeps track of the target for the hit hearts. */
    @Override
    protected void draw() { current(); }
}

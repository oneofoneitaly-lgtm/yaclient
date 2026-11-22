package dev.yaclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

/** Detects the hits you land (for Combo, Hit Color and Target HUD) and the damage you take. Updated every tick. */
public final class HitTracker {
    private HitTracker() {}

    private static boolean wasDown;
    private static int missTick = -1;
    private static EntityLivingBase clickTarget;
    private static int tick, clickTick;
    private static long lastHit;
    private static int combo;
    private static EntityLivingBase target;
    private static boolean wasHurt, clickCrit;
    private static int hits, crits, taken, kills, streak;
    private static EntityLivingBase lastKilled;

    public static void tick(Minecraft mc) {
        tick++;
        if (mc.thePlayer == null || mc.theWorld == null) { combo = 0; target = null; clickTarget = null; return; }
        boolean down = Mouse.isButtonDown(0) && mc.currentScreen == null;
        if (down && !wasDown) {
            MovingObjectPosition m = mc.objectMouseOver;
            if (m == null || m.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                missTick = tick;
            } else if (m.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                missTick = -1;
            }
            if (m != null && m.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && m.entityHit instanceof EntityLivingBase) {
                clickTarget = (EntityLivingBase) m.entityHit;
                clickTick = tick;
                net.minecraft.entity.player.EntityPlayer pl = mc.thePlayer;
                clickCrit = pl.fallDistance > 0f && !pl.onGround && !pl.isOnLadder() && !pl.isInWater() && pl.ridingEntity == null;
                target = clickTarget;
                lastTargetTime = System.currentTimeMillis();
            } else {
                clickTarget = null;
            }
        }
        wasDown = down;
        if (clickTarget != null) {
            if (tick - clickTick > 12 || clickTarget.isDead) {
                clickTarget = null;
            } else if (clickTarget.hurtTime > 0 && clickTarget.hurtTime >= clickTarget.maxHurtTime - 1) {
                combo++;
                hits++;
                HitPopups.hit(clickTarget);
                if (clickCrit) crits++;
                lastHit = System.currentTimeMillis();
                clickTarget = null;
            }
        }
        int ht = NoHurtCamModule.hurt(mc.thePlayer);
        boolean hurt = ht > 0 && ht >= NoHurtCamModule.realMax(mc.thePlayer) - 1;
        if (hurt && !wasHurt) { combo = 0; taken++; streak = 0; }
        if (target != null && target != lastKilled && (target.isDead || target.getHealth() <= 0f) && System.currentTimeMillis() - lastTargetTime < 4000) {
            lastKilled = target; kills++; streak++;
        }
        wasHurt = hurt;
        if (System.currentTimeMillis() - lastHit > 3000) combo = 0;
    }

    private static long lastTargetTime;

    public static int combo() { return combo; }

    public static int hits() { return hits; }

    public static int crits() { return crits; }

    public static int taken() { return taken; }

    public static int kills() { return kills; }

    public static int streak() { return streak; }

    public static long lastHitTime() { return lastHit; }

    /** The enemy you attacked or aimed at most recently (null after a few seconds). */
    public static EntityLivingBase target() {
        if (target != null && (target.isDead || System.currentTimeMillis() - lastTargetTime > 4000)) target = null;
        return target;
    }

    /** Refresh the "recently aimed at" timer while the crosshair is on a living entity. */
    public static void aimed(EntityLivingBase e) { target = e; lastTargetTime = System.currentTimeMillis(); }
}

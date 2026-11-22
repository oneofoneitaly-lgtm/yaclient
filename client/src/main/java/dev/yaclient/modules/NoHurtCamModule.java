package dev.yaclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Removes the camera shake when you take damage. The shake is computed every frame from your hurtTime, so between
 * two ticks hurtTime is kept at 0 (frames never see it) and the real value is put back at the start of the next tick,
 * so the game, the server and the other modules still see the true hurt time.
 */
public class NoHurtCamModule extends Module {
    private static NoHurtCamModule inst;
    private static int saved;
    private static EntityPlayer owner;

    public NoHurtCamModule() { super("no_hurt_cam", Category.PVP, false); inst = this; }

    /** Real max hurt time (never changed by this module any more). */
    public static int realMax(Entity e) {
        return e instanceof net.minecraft.entity.EntityLivingBase ? Math.min(20, Math.max(1, ((net.minecraft.entity.EntityLivingBase) e).maxHurtTime)) : 10;
    }

    /** The real hurt time of the local player (the field itself reads 0 between ticks while this mod is on). */
    public static int hurt(EntityPlayer p) {
        if (inst != null && inst.enabled && p == owner) return Math.max(p.hurtTime, saved);
        return p.hurtTime;
    }

    /** Start of a client tick: the real hurt time goes back, unless a new hit just set it. */
    public static void pre(Minecraft mc) {
        if (inst == null || !inst.enabled || mc.thePlayer == null) return;
        EntityPlayer p = mc.thePlayer;
        if (p != owner) { owner = p; saved = 0; return; }
        if (p.hurtTime == 0 && saved > 0) p.hurtTime = saved;
        if (p.maxHurtTime > 100) p.maxHurtTime = 10;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer != owner) { owner = mc.thePlayer; saved = 0; }
        saved = mc.thePlayer.hurtTime;
        mc.thePlayer.hurtTime = 0;
    }

    @Override
    public void onToggle() {
        if (!enabled && mc != null && mc.thePlayer != null && mc.thePlayer == owner && mc.thePlayer.hurtTime == 0) mc.thePlayer.hurtTime = saved;
        saved = 0;
    }
}

package dev.yaclient;

import dev.yaclient.modules.FreeLookModule;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Free Look (the FreeLookModule switches it on and off; the key is set in the options of that mod). Hold the key and
 * move the mouse: the camera turns around you while the direction your player faces (and aims) stays frozen. It
 * switches to third person while held and goes back when released.
 *
 * How: the mouse always turns the player entity between two ticks, so that is the camera. At the start of every
 * client tick the real (frozen) angles are put back, so the tick, the packets to the server and the clicks use the
 * frozen direction; at the end of the tick the camera angles are put on the player again.
 */
public final class FreeLook {
    private static boolean active;
    private static float camYaw, camPitch, fYaw, fPitch;
    private static int prevView;

    private FreeLook() {}

    /** Start of a client tick: back to the frozen direction. */
    public static void pre(Minecraft mc) {
        if (!active) return;
        EntityPlayer p = mc.thePlayer;
        if (p == null) { active = false; return; }
        camYaw = p.rotationYaw;
        camPitch = p.rotationPitch;
        p.rotationYaw = p.prevRotationYaw = fYaw;
        p.rotationPitch = p.prevRotationPitch = fPitch;
        try { mc.entityRenderer.getMouseOver(1.0F); } catch (Throwable ignored) { }   // clicks aim where the player faces, not where the camera looks
    }

    /** End of a client tick: start / stop, and the camera angles back on the player for the frames until the next tick. */
    public static void post(Minecraft mc) {
        EntityPlayer p = mc.thePlayer;
        boolean want = FreeLookModule.on() && p != null && !p.isDead && mc.currentScreen == null && FreeLookModule.held();
        if (want && !active) {
            active = true;
            camYaw = fYaw = p.rotationYaw;
            camPitch = fPitch = p.rotationPitch;
            prevView = mc.gameSettings.thirdPersonView;
            if (prevView == 0 && FreeLookModule.thirdPerson()) mc.gameSettings.thirdPersonView = 1;
        } else if (!want && active) {
            active = false;
            if (p != null) { p.rotationYaw = p.prevRotationYaw = fYaw; p.rotationPitch = p.prevRotationPitch = fPitch; }
            mc.gameSettings.thirdPersonView = prevView;
        } else if (active && p != null) {
            p.rotationYaw = p.prevRotationYaw = camYaw;
            p.rotationPitch = p.prevRotationPitch = camPitch;
        }
    }
}

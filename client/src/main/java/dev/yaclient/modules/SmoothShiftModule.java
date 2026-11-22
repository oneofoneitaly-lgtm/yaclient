package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Smooth sneaking: the camera glides down and up when you press or release shift instead of jumping 0.08 blocks.
 * The world renderer is swapped for a subclass (same state copied over) that nudges the camera entity for the
 * duration of one frame, so nothing about your real position or movement changes.
 */
public class SmoothShiftModule extends Module {
    private final Setting.Num speed = add(new Setting.Num("speed", 40f, 400f, 10f, 140f, "ms"));

    private static SmoothShiftModule inst;
    private boolean failed;
    private static float eased;          // how far the eye is currently lowered (0 .. 0.08)
    private static long last;

    public SmoothShiftModule() { super("smooth_shift", Category.UTILITY, false); inst = this; }

    @Override
    public void onAlwaysTick() {
        if (!enabled || failed || mc == null || mc.entityRenderer == null) return;
        try {
            if (mc.entityRenderer instanceof SmoothRenderer) return;
            EntityRenderer old = mc.entityRenderer;
            SmoothRenderer neu = new SmoothRenderer(mc);
            for (Field f : EntityRenderer.class.getDeclaredFields()) {       // keep every bit of state of the old renderer
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(neu, f.get(old));
            }
            mc.entityRenderer = neu;
        } catch (Throwable t) {
            failed = true;      // never break the game because of a camera detail
        }
    }

    private static final class SmoothRenderer extends EntityRenderer {
        SmoothRenderer(Minecraft mc) { super(mc, mc.getResourceManager()); }

        @Override
        public void renderWorld(float partialTicks, long finishTimeNano) {
            Minecraft mc = Minecraft.getMinecraft();
            Entity v = mc.getRenderViewEntity();
            double d = 0;
            if (inst != null && inst.enabled && v != null && v == mc.thePlayer && mc.gameSettings.thirdPersonView == 0
                    && !mc.thePlayer.isPlayerSleeping() && mc.thePlayer.ridingEntity == null) {
                long now = System.nanoTime();
                float dt = last == 0 ? 0.016f : Math.min(0.1f, (now - last) / 1e9f);
                last = now;
                float target = mc.thePlayer.isSneaking() ? 0.08f : 0f;
                float tau = Math.max(0.01f, inst.speed.value / 1000f / 3f);
                eased += (target - eased) * (1f - (float) Math.exp(-dt / tau));
                d = target - eased;                 // vanilla already moved the eye by "target": add the difference back
            } else {
                last = 0;
                eased = (v != null && mc.thePlayer != null && mc.thePlayer.isSneaking()) ? 0.08f : 0f;
            }
            if (d == 0 || v == null) { super.renderWorld(partialTicks, finishTimeNano); return; }
            v.posY += d;
            v.prevPosY += d;
            v.lastTickPosY += d;
            try {
                super.renderWorld(partialTicks, finishTimeNano);
            } finally {
                v.posY -= d;
                v.prevPosY -= d;
                v.lastTickPosY -= d;
            }
        }
    }
}

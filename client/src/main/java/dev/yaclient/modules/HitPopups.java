package dev.yaclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.weavemc.api.event.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Part of Target HUD: every hit you land makes a white heart with the target's health (in hearts) pop up at a random
 * point of the target's hitbox, rise a little and fade.
 */
public final class HitPopups {
    private HitPopups() {}

    private static final class Pop {
        EntityLivingBase e;
        double dx, dy, dz;
        long born;
    }

    private static final List<Pop> pops = new ArrayList<Pop>();
    private static final Random rnd = new Random();
    private static final long LIFE = 1100L;
    private static boolean registered;

    /** Called by HitTracker when a hit lands. */
    public static void hit(EntityLivingBase e) {
        if (e == null || !TargetHudModule.popupsOn()) return;
        AxisAlignedBB bb = e.getEntityBoundingBox();
        Pop p = new Pop();
        p.e = e;
        p.dx = bb.minX + rnd.nextDouble() * (bb.maxX - bb.minX) - e.posX;
        p.dy = bb.minY + (0.5 + rnd.nextDouble() * 0.45) * (bb.maxY - bb.minY) - e.posY;
        p.dz = bb.minZ + rnd.nextDouble() * (bb.maxZ - bb.minZ) - e.posZ;
        p.born = System.currentTimeMillis();
        synchronized (pops) {
            pops.add(p);
            while (pops.size() > 14) pops.remove(0);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        if (registered) return;
        String[] names = {"net.weavemc.api.event.RenderWorldEvent$Post", "net.weavemc.api.event.RenderWorldEvent"};
        for (String n : names) {
            try {
                Class c = Class.forName(n);
                EventBus.subscribe(c, (Consumer) new Consumer<Object>() {
                    @Override public void accept(Object ev) { draw(ev); }
                });
                registered = true;
                return;
            } catch (Throwable ignored) { }
        }
        System.out.println("[YA CLIENT] hit popups: world render event not found");
    }

    private static float partial(Object e) {
        try { return ((Number) e.getClass().getMethod("getPartialTicks").invoke(e)).floatValue(); } catch (Throwable t) { return 1f; }
    }

    private static void draw(Object event) {
        synchronized (pops) {
            if (pops.isEmpty()) return;
            try {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc == null || mc.theWorld == null || mc.getRenderViewEntity() == null || mc.gameSettings.hideGUI) { pops.clear(); return; }
                float pt = partial(event);
                Entity v = mc.getRenderViewEntity();
                double cx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
                double cy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
                double cz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;
                FontRenderer fr = mc.fontRendererObj;
                long now = System.currentTimeMillis();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableTexture2D();
                for (int i = pops.size() - 1; i >= 0; i--) {
                    Pop p = pops.get(i);
                    long age = now - p.born;
                    if (age > LIFE || p.e == null || p.e.isDead && p.e.getHealth() > 0f) { pops.remove(i); continue; }
                    double ex = p.e.lastTickPosX + (p.e.posX - p.e.lastTickPosX) * pt;
                    double ey = p.e.lastTickPosY + (p.e.posY - p.e.lastTickPosY) * pt;
                    double ez = p.e.lastTickPosZ + (p.e.posZ - p.e.lastTickPosZ) * pt;
                    float k = age / (float) LIFE;
                    double x = ex + p.dx - cx, y = ey + p.dy + 0.35 * k - cy, z = ez + p.dz - cz;
                    double dist = Math.sqrt(x * x + y * y + z * z);
                    int alpha = (int) (255 * (k < 0.6f ? 1f : 1f - (k - 0.6f) / 0.4f));
                    if (alpha < 6) continue;
                    String s = String.format(Locale.ROOT, "%.1f", Math.max(0f, p.e.getHealth()) / 2f) + " ♥";
                    float sc = (float) (0.032 * Math.max(1.0, Math.min(3.0, dist / 5.0)));
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((float) x, (float) y, (float) z);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
                    GlStateManager.rotate((mc.gameSettings.thirdPersonView == 2 ? -1f : 1f) * mc.getRenderManager().playerViewX, 1f, 0f, 0f);
                    GlStateManager.scale(-sc, -sc, sc);
                    fr.drawStringWithShadow(s, -fr.getStringWidth(s) / 2f, 0f, (alpha << 24) | 0xFFFFFF);
                    GlStateManager.popMatrix();
                }
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
                GlStateManager.color(1f, 1f, 1f, 1f);
            } catch (Throwable ignored) { }
        }
    }
}

package dev.yaclient.fabric;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** 1.21.1 has no tinted blit: apply the colour through setColor around the plain one. */
public final class Blit {
    private Blit() {}

    public static int abgr(int argb) {
        return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
    }

    public static void blit(GuiGraphics g, ResourceLocation id, int x, int y, float u, float v, int w, int h, int uw, int vh, int tw, int th, int color) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.setColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, ((color >>> 24) & 255) / 255f);
        g.blit(id, x, y, w, h, u, v, uw, vh, tw, th);
        g.setColor(1f, 1f, 1f, 1f);
    }

    /** Before 1.21.6 the scissor ignores the pose: transform the rectangle by hand. */
    public static void scissor(GuiGraphics g, int x1, int y1, int x2, int y2) {
        org.joml.Matrix4f m = g.pose().last().pose();
        org.joml.Vector4f a = new org.joml.Vector4f(x1, y1, 0f, 1f).mul(m);
        org.joml.Vector4f b = new org.joml.Vector4f(x2, y2, 0f, 1f).mul(m);
        g.enableScissor((int) Math.floor(Math.min(a.x, b.x)), (int) Math.floor(Math.min(a.y, b.y)), (int) Math.ceil(Math.max(a.x, b.x)), (int) Math.ceil(Math.max(a.y, b.y)));
    }
}

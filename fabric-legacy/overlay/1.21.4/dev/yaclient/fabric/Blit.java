package dev.yaclient.fabric;

import net.minecraft.client.gui.GuiGraphics;

/** Before 1.21.6 the scissor ignores the pose: transform the rectangle by hand. */
public final class Blit {
    private Blit() {}

    /** Before 1.21.6 the scissor ignores the pose: transform the rectangle by hand. */
    public static void scissor(GuiGraphics g, int x1, int y1, int x2, int y2) {
        org.joml.Matrix4f m = g.pose().last().pose();
        org.joml.Vector4f a = new org.joml.Vector4f(x1, y1, 0f, 1f).mul(m);
        org.joml.Vector4f b = new org.joml.Vector4f(x2, y2, 0f, 1f).mul(m);
        g.enableScissor((int) Math.floor(Math.min(a.x, b.x)), (int) Math.floor(Math.min(a.y, b.y)), (int) Math.ceil(Math.max(a.x, b.x)), (int) Math.ceil(Math.max(a.y, b.y)));
    }
}

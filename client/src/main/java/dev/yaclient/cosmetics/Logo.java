package dev.yaclient.cosmetics;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/** Draws the YA logo as a smooth GUI quad (used by the menus). */
public final class Logo {
    private Logo() {}

    public static void draw(float x, float y, float w, float h, float alpha) { draw(x, y, w, h, alpha, 0); }

    /** The logo flattened to one color (used for a dark outline on bright screens). */
    public static void drawTint(float x, float y, float w, float h, float alpha, float r, float g, float b) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(r, g, b, alpha);
        LogoTexture.bind(0);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0).tex(0, 1).endVertex();
        wr.pos(x + w, y + h, 0).tex(1, 1).endVertex();
        wr.pos(x + w, y, 0).tex(1, 0).endVertex();
        wr.pos(x, y, 0).tex(0, 0).endVertex();
        t.draw();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableDepth();
    }

    /** rank: 0 normal, 1 YA Plus, 2 staff. */
    public static void draw(float x, float y, float w, float h, float alpha, int rank) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1f, 1f, 1f, alpha);
        LogoTexture.bind(rank);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0).tex(0, 1).endVertex();
        wr.pos(x + w, y + h, 0).tex(1, 1).endVertex();
        wr.pos(x + w, y, 0).tex(1, 0).endVertex();
        wr.pos(x, y, 0).tex(0, 0).endVertex();
        t.draw();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableDepth();
    }
}

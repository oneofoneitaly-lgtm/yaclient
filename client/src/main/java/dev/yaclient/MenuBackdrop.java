package dev.yaclient;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/** Still photo backdrop for the title screen (assets/yaclient/menu_bg.png, drawn at full resolution, never downscaled). */
final class MenuBackdrop {
    private static DynamicTexture tex;
    private static boolean failed;
    private static final float AR = 16f / 9f;

    private MenuBackdrop() {}

    private static volatile BufferedImage decoded;
    private static boolean preloading;

    /** Decodes the picture on a background thread at startup, so the title screen never stalls while it loads. */
    static synchronized void preload() {
        if (preloading || tex != null) return;
        preloading = true;
        Thread th = new Thread(new Runnable() {
            public void run() {
                try {
                    InputStream in = MenuBackdrop.class.getResourceAsStream("/assets/yaclient/menu_bg.png");
                    if (in != null) { try { decoded = ImageIO.read(in); } finally { in.close(); } }
                } catch (Throwable ignored) { }
            }
        }, "YA-MenuBg");
        th.setDaemon(true);
        th.start();
    }

    /** @return false if the image is missing so the caller can fall back to the panorama. */
    static boolean draw(int w, int h, float t) {
        if (tex == null && !failed) {
            try {
                BufferedImage img = decoded;
                if (img == null) {
                    InputStream in = MenuBackdrop.class.getResourceAsStream("/assets/yaclient/menu_bg.png");
                    if (in != null) { try { img = ImageIO.read(in); } finally { in.close(); } }
                }
                if (img == null) failed = true;
                else {
                    tex = new DynamicTexture(img);
                    decoded = null;
                    GlStateManager.bindTexture(tex.getGlTextureId());
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                }
            } catch (Throwable e) { failed = true; }
        }
        if (tex == null) return false;

        float sa = (float) w / h;
        float uR = sa > AR ? 1f : sa / AR, vR = sa > AR ? AR / sa : 1f;

        GlStateManager.shadeModel(7425);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.bindTexture(tex.getGlTextureId());

        // the picture stays still: fills the screen, centered
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        quad(w, h, uR, vR, 1f, 0f, 0f);
        GlStateManager.enableBlend();
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // readability gradient (top/bottom)
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(w, 0, 0).color(0, 0, 0, 90).endVertex();
        wr.pos(0, 0, 0).color(0, 0, 0, 90).endVertex();
        wr.pos(0, h * 0.35, 0).color(0, 0, 0, 0).endVertex();
        wr.pos(w, h * 0.35, 0).color(0, 0, 0, 0).endVertex();
        ts.draw();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(w, h * 0.55, 0).color(0, 0, 0, 0).endVertex();
        wr.pos(0, h * 0.55, 0).color(0, 0, 0, 0).endVertex();
        wr.pos(0, h, 0).color(0, 0, 0, 105).endVertex();
        wr.pos(w, h, 0).color(0, 0, 0, 105).endVertex();
        ts.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.color(1f, 1f, 1f, 1f);
        return true;
    }

    private static void quad(int w, int h, float uR, float vR, float z, float px, float py) {
        float uw = uR / z, vh = vR / z;
        float u0 = (1 - uw) * (0.5f + px), v0 = (1 - vh) * (0.5f + py);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0, h, 0).tex(u0, v0 + vh).endVertex();
        wr.pos(w, h, 0).tex(u0 + uw, v0 + vh).endVertex();
        wr.pos(w, 0, 0).tex(u0 + uw, v0).endVertex();
        wr.pos(0, 0, 0).tex(u0, v0).endVertex();
        t.draw();
    }
}

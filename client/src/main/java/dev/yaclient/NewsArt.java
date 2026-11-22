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

/** The changelog banner (assets/yaclient/news_update.png), drawn as a smooth GUI quad. */
final class NewsArt {
    private static DynamicTexture tex;
    private static boolean failed;

    private NewsArt() {}

    /** Aspect ratio (width / height) of the banner. */
    static final float ASPECT = 960f / 400f;

    static void draw(float x, float y, float w, float h, float alpha) {
        if (tex == null && !failed) {
            try {
                InputStream in = NewsArt.class.getResourceAsStream("/assets/yaclient/news_update.png");
                if (in != null) {
                    try {
                        BufferedImage img = ImageIO.read(in);
                        tex = new DynamicTexture(img);
                        GlStateManager.bindTexture(tex.getGlTextureId());
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                    } finally { in.close(); }
                } else failed = true;
            } catch (Throwable t) { failed = true; }
        }
        if (tex == null) return;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1f, 1f, 1f, alpha);
        GlStateManager.bindTexture(tex.getGlTextureId());
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

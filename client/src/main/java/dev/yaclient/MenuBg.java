package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.glu.Project;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/** Bright, soft backdrop shared by the title screen and the server list: a gradient with slowly drifting color blobs. */
final class MenuBg {
    private MenuBg() {}

    private static final ResourceLocation[] PANO = new ResourceLocation[6];
    static { for (int i = 0; i < 6; i++) PANO[i] = new ResourceLocation("textures/gui/title/background/panorama_" + i + ".png"); }

    static final int INK = 0xFF1A1C26, INK2 = 0xFF5B6272;

    static void draw(int w, int h, float t) {
        // the game's own panorama, slowly turning, then a frosted light veil on top so it reads clean and bright
        panorama(w, h, t * 50f);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(w, 0, 0).color(0xF2, 0xF5, 0xFB, 200).endVertex();
        wr.pos(0, 0, 0).color(0xFA, 0xFB, 0xFE, 215).endVertex();
        wr.pos(0, h, 0).color(0xE9, 0xEE, 0xF7, 200).endVertex();
        wr.pos(w, h, 0).color(0xDC, 0xE3, 0xF0, 215).endVertex();
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        blob(w * 0.5f, h * 0.46f, w * 0.36f, h * 0.6f, 0xFFFFFF, 120);
    }

    /** A translucent white diagonal panel: top edge x0..x1, bottom edge x3..x2. */
    static void band(float x0, float x1, float x2, float x3, int w, int h, int alpha) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x1, 0, 0).color(255, 255, 255, alpha).endVertex();
        wr.pos(x0, 0, 0).color(255, 255, 255, alpha).endVertex();
        wr.pos(x3, h, 0).color(255, 255, 255, 0).endVertex();
        wr.pos(x2, h, 0).color(255, 255, 255, 0).endVertex();
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** A faint dot grid over the backdrop: gives the flat gradient some structure. */
    static void dots(int w, int h) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int y = 9; y < h; y += 14) {
            for (int x = 9; x < w; x += 14) {
                wr.pos(x, y, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
                wr.pos(x, y + 1, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
                wr.pos(x + 1, y + 1, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
                wr.pos(x + 1, y, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
            }
        }
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** Flat translucent disc. */
    static void disc(float cx, float cy, float r, int rgb, int alpha) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        int rr = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(6, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0).color(rr, g, b, alpha).endVertex();
        for (int i = 0; i <= 40; i++) {
            double a = i * Math.PI * 2 / 40;
            wr.pos(cx + Math.cos(a) * r, cy + Math.sin(a) * r, 0).color(rr, g, b, (int) (alpha * 0.45f)).endVertex();
        }
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** Thin elliptical ring (a floor marker under the player). */
    static void ring(float cx, float cy, float rx, float ry, float th, int rgb, int alpha) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        int n = 64;
        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2 / n, a1 = (i + 1) * Math.PI * 2 / n;
            wr.pos(cx + Math.cos(a0) * rx, cy + Math.sin(a0) * ry, 0).color(r, g, b, alpha).endVertex();
            wr.pos(cx + Math.cos(a0) * (rx - th), cy + Math.sin(a0) * (ry - th * ry / rx), 0).color(r, g, b, alpha).endVertex();
            wr.pos(cx + Math.cos(a1) * (rx - th), cy + Math.sin(a1) * (ry - th * ry / rx), 0).color(r, g, b, alpha).endVertex();
            wr.pos(cx + Math.cos(a1) * rx, cy + Math.sin(a1) * ry, 0).color(r, g, b, alpha).endVertex();
        }
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** Soft radial blob (an ellipse of color fading to transparent), alpha 0..255 at the center. */
    static void blob(float cx, float cy, float rx, float ry, int rgb, int alpha) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(6, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0).color(r, g, b, alpha).endVertex();
        for (int i = 0; i <= 32; i++) {
            double a = i * Math.PI * 2 / 32;
            wr.pos(cx + Math.cos(a) * rx, cy + Math.sin(a) * ry, 0).color(r, g, b, 0).endVertex();
        }
        t.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }



    /** The vanilla title panorama (same technique as the original menu). */
    static void panorama(int width, int height, float timer) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(85.0F, (float) width / (float) height, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        final int n = 4;
        for (int j = 0; j < n * n; j++) {
            GlStateManager.pushMatrix();
            float dx = ((float) (j % n) / n - 0.5F) / 64.0F, dy = ((float) (j / n) / n - 0.5F) / 64.0F;
            GlStateManager.translate(dx, dy, 0.0F);
            GlStateManager.rotate((float) Math.sin(timer / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-timer * 0.1F, 0.0F, 1.0F, 0.0F);
            for (int k = 0; k < 6; k++) {
                GlStateManager.pushMatrix();
                if (k == 1) GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                if (k == 2) GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                if (k == 3) GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                if (k == 4) GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                if (k == 5) GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                Minecraft.getMinecraft().getTextureManager().bindTexture(PANO[k]);
                wr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int a = 255 / (j + 1);
                wr.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, a).endVertex();
                wr.pos(-1.0D, 1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, a).endVertex();
                wr.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, a).endVertex();
                wr.pos(1.0D, -1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, a).endVertex();
                t.draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }
}

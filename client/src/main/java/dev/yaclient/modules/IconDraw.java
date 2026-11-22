package dev.yaclient.modules;

import dev.yaclient.Gfx;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/** Small helpers to draw a picture (server icon, pack icon) inside a HUD element. */
final class IconDraw {
    private IconDraw() {}

    private static String lastB64;
    private static DynamicTexture serverTex;
    private static boolean serverBad;

    /** Binds the server's icon (base64 PNG from the server list) and returns true, or false if the server has none. */
    static boolean bindServer(String b64) {
        if (b64 == null || b64.isEmpty()) return false;
        if (b64.startsWith("data:image/png;base64,")) b64 = b64.substring(22);
        if (!b64.equals(lastB64)) {
            lastB64 = b64;
            serverBad = false;
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(java.util.Base64.getMimeDecoder().decode(b64)));
                if (img == null) serverBad = true;
                else {
                    if (serverTex != null) { try { serverTex.deleteGlTexture(); } catch (Throwable ignored) { } }
                    serverTex = new DynamicTexture(img);
                }
            } catch (Throwable t) { serverBad = true; }
        }
        if (serverBad || serverTex == null) return false;
        GlStateManager.bindTexture(serverTex.getGlTextureId());
        return true;
    }

    /** Draws the currently bound texture in a square. */
    static void quad(float x, float y, float s) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1f, 1f, 1f, Gfx.fade);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + s, 0).tex(0, 1).endVertex();
        wr.pos(x + s, y + s, 0).tex(1, 1).endVertex();
        wr.pos(x + s, y, 0).tex(1, 0).endVertex();
        wr.pos(x, y, 0).tex(0, 0).endVertex();
        t.draw();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /** A rounded dark tile with a letter, used when there is no picture. */
    static void letter(int x, int y, int s, String text) {
        Gfx.rr(x, y, s, s, Math.max(2, s / 5), 0xFF2A2A33);
        String l = text == null || text.isEmpty() ? "?" : text.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        int fs = Math.max(8, s - 4);
        Gfx.text(l, x + (s - Gfx.width(l, fs, true)) / 2, y + (s - Gfx.lineHeight(fs, true)) / 2, fs, true, 0xFFF4F4F5);
    }
}

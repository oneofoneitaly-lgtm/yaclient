package dev.yaclient.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Cosmetic textures are generated in code (no image files needed). Each item gets one 64x64
 * texture, laid out for the box UV mapping used by CosmeticModels.
 */
public final class CosmeticTextures {
    private static final Map<String, ResourceLocation> CACHE = new HashMap<String, ResourceLocation>();

    private CosmeticTextures() {}

    public static ResourceLocation get(String item) {
        ResourceLocation r = CACHE.get(item);
        if (r == null) {
            r = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("yaclient_cos_" + item, new DynamicTexture(paint(item)));
            CACHE.put(item, r);
        }
        return r;
    }

    // ---------- painting ----------
    private static Graphics2D g(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    /** Fills the whole UV area of a box (w x h x d) whose texture offset is (u, v). */
    private static void box(BufferedImage img, int u, int v, int w, int h, int d, int argb) {
        Graphics2D g = g(img);
        g.setColor(new Color(argb, true));
        g.fillRect(u, v, 2 * (d + w), d + h);
        g.dispose();
    }

    private static BufferedImage paint(String item) {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        switch (item) {
            case "angel": wings(img, 0xFFFFFFFF, 0xFFDCD6FF, 0xFFE9C46A, false); break;
            case "dragon": wings(img, 0xFF8A1111, 0xFF4A0707, 0xFF140202, true); break;
            case "classic": cape(img, 0xFF16161A, 0xFF0A0A0C, 0xFFF4F4F5); break;
            case "crimson": cape(img, 0xFFC4120A, 0xFF4A0502, 0xFFFFFFFF); break;
            case "midnight": cape(img, 0xFF1B2A63, 0xFF07091C, 0xFF9DB4FF); break;
            case "tophat":
                box(img, 0, 0, 12, 1, 12, 0xFF15151A);
                box(img, 0, 16, 8, 8, 8, 0xFF15151A);
                box(img, 0, 34, 9, 2, 9, 0xFFC4120A);
                break;
            case "crown":
                box(img, 0, 0, 9, 3, 9, 0xFFE6B422);
                box(img, 0, 14, 2, 3, 2, 0xFFFFD866);
                break;
            case "red":
                box(img, 0, 0, 9, 2, 9, 0xFFD11A1A);
                box(img, 0, 12, 3, 3, 2, 0xFFB01414);
                break;
            case "black":
                box(img, 0, 0, 9, 2, 9, 0xFF18181C);
                box(img, 0, 12, 3, 3, 2, 0xFF0C0C0E);
                break;
            default:
                box(img, 0, 0, 64, 64, 0, 0xFFFF00FF);
        }
        return img;
    }

    /** Wing silhouette in "outward" coordinates: x 0 (body) .. 12 (tip), y 0 (top) .. 18. */
    private static Path2D wingShape(int u0, int v0, boolean mirrorX) {
        float[][] pts = {{0, 0}, {6, 0}, {11, 2}, {12, 6}, {10, 7}, {12, 10}, {9, 11}, {10, 15}, {7, 14}, {6, 18}, {4, 13}, {2, 14}, {0, 9}};
        Path2D.Float p = new Path2D.Float();
        for (int i = 0; i < pts.length; i++) {
            float x = mirrorX ? 12f - pts[i][0] : pts[i][0];
            float px = u0 + x, py = v0 + pts[i][1];
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    /** Right wing at (0,0), left wing at (0,24); front and back faces share the same design. */
    private static void wings(BufferedImage img, int top, int bottom, int edge, boolean dark) {
        Graphics2D g = g(img);
        int[][] regions = {{1, 1, 1}, {14, 1, 1}, {1, 25, 0}, {14, 25, 0}}; // u0, v0, mirror(1=right wing)
        for (int[] r : regions) {
            Path2D shape = wingShape(r[0], r[1], r[2] == 1);
            g.setPaint(new GradientPaint(r[0], r[1], new Color(top, true), r[0], r[1] + 18, new Color(bottom, true)));
            g.fill(shape);
            g.setColor(new Color(edge, true));
            g.draw(shape);
            if (dark) { // bone lines for the dragon wings
                g.setColor(new Color(0xFF0A0101, true));
                g.drawLine(r[0] + (r[2] == 1 ? 12 : 0), r[1], r[0] + (r[2] == 1 ? 1 : 11), r[1] + 16);
                g.drawLine(r[0] + (r[2] == 1 ? 12 : 0), r[1] + 2, r[0] + (r[2] == 1 ? 6 : 6), r[1] + 15);
            }
        }
        g.dispose();
    }

    /** Cape box 10x16x1 at (0,0): front face at x 1..11, back (outside) face at x 12..22. */
    private static void cape(BufferedImage img, int top, int bottom, int trim) {
        Graphics2D g = g(img);
        g.setColor(new Color(0xFF0A0A0C, true));
        g.fillRect(0, 0, 24, 18);
        g.setPaint(new GradientPaint(12, 1, new Color(top, true), 12, 17, new Color(bottom, true)));
        g.fillRect(12, 1, 10, 16);
        g.setColor(new Color(trim, true));
        g.drawRect(12, 1, 9, 15);
        g.setFont(new Font("SansSerif", Font.BOLD, 8));
        g.drawString("YA", 14, 11);
        g.dispose();
    }
}

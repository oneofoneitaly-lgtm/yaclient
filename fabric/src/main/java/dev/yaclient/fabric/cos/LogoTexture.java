package dev.yaclient.fabric.cos;


import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * The YA logo. Put your own image at src/main/resources/assets/yaclient/logo.png (square, transparent
 * background, 256x256 or bigger). If the file is missing a simple "YA" mark is drawn instead.
 */
final class LogoTexture {
    private static BufferedImage image;
    private static DynamicTexture texture;

    private LogoTexture() {}

    static BufferedImage image() {
        if (image == null) {
            try {
                InputStream in = LogoTexture.class.getResourceAsStream("/assets/yaclient/logo.png");
                if (in != null) {
                    try { image = ImageIO.read(in); } finally { in.close(); }
                }
            } catch (Throwable ignored) { }
            if (image == null) image = fallback();
        }
        return image;
    }

    /** Binds the logo as the current OpenGL texture (smooth filtering). */
    static void bind() {
        if (texture == null) {
            texture = new DynamicTexture(image());
            GlStateManager.bindTexture(texture.getGlTextureId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        GlStateManager.bindTexture(texture.getGlTextureId());
    }

    private static BufferedImage fallback() {
        int n = 256;
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 150));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth("YA");
        g.setColor(Color.WHITE);
        g.drawString("YA", (n - w) / 2, n / 2 + fm.getAscent() / 2 - 14);
        g.setColor(new Color(0xE10600));
        g.fillRoundRect(n / 2 - 70, n - 62, 140, 14, 14, 14);
        g.dispose();
        return img;
    }
}

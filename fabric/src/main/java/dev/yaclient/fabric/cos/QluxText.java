package dev.yaclient.fabric.cos;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * The "Qlux" wordmark for the cape: a custom geometric rounded lettering drawn with vector strokes into a
 * large, smoothly filtered texture (no pixel look, no font needed). It is white on transparent; the cape
 * code tints it per vertex, which is what makes the shimmer animation.
 */
final class QluxText {
    /** Texture size; the wordmark is 1024 x 384. */
    static final int W = 1024, H = 384;
    private static DynamicTexture texture;

    private QluxText() {}

    static void bind() {
        if (texture == null) {
            texture = new DynamicTexture(paint());
            GlStateManager.bindTexture(texture.getGlTextureId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        GlStateManager.bindTexture(texture.getGlTextureId());
    }

    static BufferedImage paint() {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(46f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        final double base = 300;                         // baseline
        // Q: ring with a slanted tail
        g.draw(new Ellipse2D.Double(60, 96, 214, 204 + 0));
        g.draw(new Line2D.Double(196, 232, 276, 322));
        // l: tall rounded stem
        g.draw(new Line2D.Double(372, 52, 372, base));
        // u: two stems joined by a round bottom
        Path2D.Double u = new Path2D.Double();
        u.moveTo(480, 120);
        u.lineTo(480, 222);
        u.append(new Arc2D.Double(480, 190, 112, 110, 180, 180, Arc2D.OPEN), true);
        u.moveTo(592, 120);
        u.lineTo(592, base);
        g.draw(u);
        // x: two crossing strokes
        g.draw(new Line2D.Double(676, 122, 800, base));
        g.draw(new Line2D.Double(800, 122, 676, base));
        // trailing underscore: "Qlux_"
        g.draw(new Line2D.Double(850, base + 22, 972, base + 22));
        g.dispose();
        return img;
    }
}

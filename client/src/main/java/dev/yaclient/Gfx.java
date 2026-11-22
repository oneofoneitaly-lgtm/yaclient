package dev.yaclient;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Grafica nitida: forme arrotondate e testo anti-aliased disegnati con Java2D
 * e usati come texture (1 pixel della texture = 1 pixel dello schermo).
 */
public final class Gfx {
    private static final class FontInfo {
        final Font font;
        final FontMetrics fm;
        FontInfo(Font font, FontMetrics fm) { this.font = font; this.fm = fm; }
    }

    private static final int MAX_TEX = 600;
    /** Cache LRU: le texture meno usate vengono eliminate (utile per testi che cambiano, es. coordinate). */
    private static final Map<String, DynamicTexture> TEX = new LinkedHashMap<String, DynamicTexture>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, DynamicTexture> eldest) {
            if (size() > MAX_TEX) {
                eldest.getValue().deleteGlTexture();
                return true;
            }
            return false;
        }
    };
    private static final Map<String, FontInfo> FONTS = new HashMap<String, FontInfo>();
    private static final String FAMILY = pickFamily();
    private static int ss = 2;
    /** Trasparenza globale (animazione di apertura). */
    public static float fade = 1f;

    private Gfx() {}

    private static String pickFamily() {
        try {
            String[] want = {"Segoe UI", "Inter", "Helvetica Neue", "Arial"};
            String[] have = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String w : want) for (String h : have) if (h.equals(w)) return w;
        } catch (Throwable ignored) { }
        return Font.SANS_SERIF;
    }

    /** Pixel reali per unita' GUI (le texture in cache hanno la scala nella chiave). */
    public static void setScale(int s) {
        ss = Math.max(1, s);
    }

    /** Font family used for all client text (also for the in-world nametag). */
    public static String family() { return FAMILY; }

    /** True if the module id has its own custom icon. */
    public static boolean hasIcon(String name) { return Icons.has(name); }

    private static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static FontInfo info(int size, boolean bold) {
        String key = size + "|" + bold + "|" + ss;
        FontInfo fi = FONTS.get(key);
        if (fi == null) {
            Font f = new Font(FAMILY, bold ? Font.BOLD : Font.PLAIN, size * ss);
            BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            hints(g);
            FontMetrics fm = g.getFontMetrics(f);
            g.dispose();
            fi = new FontInfo(f, fm);
            FONTS.put(key, fi);
        }
        return fi;
    }

    private static DynamicTexture upload(String key, BufferedImage img) {
        DynamicTexture t = new DynamicTexture(img);
        GlStateManager.bindTexture(t.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        TEX.put(key, t);
        return t;
    }

    private static void draw(DynamicTexture t, int x, int y, int w, int h, int color) {
        float a = ((color >>> 24) & 255) / 255f * fade;
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, a);
        GlStateManager.bindTexture(t.getGlTextureId());
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, w, h, (float) w, (float) h);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }


    /** Color with the global fade applied (for the solid parts of the nine-slice shapes). */
    private static int faded(int color) {
        int a = Math.round(((color >>> 24) & 255) * fade);
        return (Math.max(0, Math.min(255, a)) << 24) | (color & 0xFFFFFF);
    }

    /** One corner quarter (cs x cs GUI units) of a rounded shape texture of size 2cs x 2cs. */
    private static void corner(DynamicTexture t, int x, int y, int cs, int qx, int qy, int color) {
        float a = ((color >>> 24) & 255) / 255f * fade;
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, a);
        GlStateManager.bindTexture(t.getGlTextureId());
        Gui.drawScaledCustomSizeModalRect(x, y, (float) (qx * cs), (float) (qy * cs), cs, cs, cs, cs, (float) (2 * cs), (float) (2 * cs));
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static DynamicTexture cornerTex(String key, int cs, int r, boolean outline, int thick) {
        DynamicTexture t = TEX.get(key);
        if (t == null) {
            int s = 2 * cs;
            BufferedImage img = new BufferedImage(s * ss, s * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            g.setColor(Color.WHITE);
            if (outline) {
                float th = thick * ss;
                g.setStroke(new BasicStroke(th));
                g.draw(new RoundRectangle2D.Float(th / 2f, th / 2f, s * ss - th, s * ss - th, 2f * r * ss, 2f * r * ss));
            } else {
                g.fill(new RoundRectangle2D.Float(0f, 0f, s * ss, s * ss, 2f * r * ss, 2f * r * ss));
            }
            g.dispose();
            t = upload(key, img);
        }
        return t;
    }

    /** Rettangolo con angoli arrotondati (anti-aliased). */
    public static void rr(int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        // big shapes: 4 small cached corners + solid rectangles (no new full-size texture for every size, so no stutter)
        int cs = r + 1;
        if (r > 0 && w > 2 * cs + 8 && h > 2 * cs + 8) {
            DynamicTexture ct = cornerTex("rc|" + r + "|" + ss, cs, r, false, 0);
            int c2 = faded(color);
            corner(ct, x, y, cs, 0, 0, color);
            corner(ct, x + w - cs, y, cs, 1, 0, color);
            corner(ct, x, y + h - cs, cs, 0, 1, color);
            corner(ct, x + w - cs, y + h - cs, cs, 1, 1, color);
            Gui.drawRect(x + cs, y, x + w - cs, y + cs, c2);
            Gui.drawRect(x, y + cs, x + w, y + h - cs, c2);
            Gui.drawRect(x + cs, y + h - cs, x + w - cs, y + h, c2);
            return;
        }
        String key = "r|" + w + "|" + h + "|" + r + "|" + ss;
        DynamicTexture t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(w * ss, h * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Float(0f, 0f, w * ss, h * ss, 2f * r * ss, 2f * r * ss));
            g.dispose();
            t = upload(key, img);
        }
        draw(t, x, y, w, h, color);
    }

    /** Rettangolo arrotondato con bordo di 1 pixel. */
    public static void rrb(int x, int y, int w, int h, int r, int fill, int border) {
        rr(x, y, w, h, r, border);
        rr(x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    private static int ceilTo(int v, int m) { return ((v + m - 1) / m) * m; }

    /** Larghezza del testo in unita' GUI. */
    public static int width(String s, int size, boolean bold) {
        if (s == null || s.isEmpty()) return 0;
        return (info(size, bold).fm.stringWidth(s) + ss - 1) / ss;
    }

    /** Altezza di una riga in unita' GUI. */
    public static int lineHeight(int size, boolean bold) {
        return (info(size, bold).fm.getHeight() + ss - 1) / ss;
    }

    /** Accorcia il testo con "..." se supera la larghezza massima. */
    public static String trim(String s, int size, boolean bold, int maxW) {
        if (width(s, size, bold) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && width(t + "...", size, bold) > maxW) t = t.substring(0, t.length() - 1);
        return t + "...";
    }

    /** Testo anti-aliased. (x, y) = angolo in alto a sinistra. */
    public static void text(String s, int x, int y, int size, boolean bold, int color) {
        if (s == null || s.isEmpty()) return;
        FontInfo fi = info(size, bold);
        int pad = ss;
        int pw = ceilTo(fi.fm.stringWidth(s) + 2 * pad, ss);
        int ph = ceilTo(fi.fm.getHeight() + pad, ss);
        String key = "t|" + s + "|" + size + "|" + bold + "|" + ss;
        DynamicTexture t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            g.setFont(fi.font);
            g.setColor(Color.WHITE);
            g.drawString(s, pad, fi.fm.getAscent());
            g.dispose();
            t = upload(key, img);
        }
        draw(t, x - 1, y, pw / ss, ph / ss, color);
    }

    /** Rounded outline (stroke only) with the given thickness in GUI units. */
    public static void rrOutline(int x, int y, int w, int h, int r, int thick, int color) {
        if (w <= 0 || h <= 0) return;
        int cs = r + thick;
        if (r > 0 && w > 2 * cs + 8 && h > 2 * cs + 8) {
            DynamicTexture ct = cornerTex("oc|" + r + "|" + thick + "|" + ss, cs, r, true, thick);
            int c2 = faded(color);
            corner(ct, x, y, cs, 0, 0, color);
            corner(ct, x + w - cs, y, cs, 1, 0, color);
            corner(ct, x, y + h - cs, cs, 0, 1, color);
            corner(ct, x + w - cs, y + h - cs, cs, 1, 1, color);
            Gui.drawRect(x + cs, y, x + w - cs, y + thick, c2);
            Gui.drawRect(x + cs, y + h - thick, x + w - cs, y + h, c2);
            Gui.drawRect(x, y + cs, x + thick, y + h - cs, c2);
            Gui.drawRect(x + w - thick, y + cs, x + w, y + h - cs, c2);
            return;
        }
        String key = "o|" + w + "|" + h + "|" + r + "|" + thick + "|" + ss;
        DynamicTexture t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(w * ss, h * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            g.setColor(Color.WHITE);
            float th = thick * ss;
            g.setStroke(new BasicStroke(th));
            g.draw(new RoundRectangle2D.Float(th / 2f, th / 2f, w * ss - th, h * ss - th, 2f * r * ss, 2f * r * ss));
            g.dispose();
            t = upload(key, img);
        }
        draw(t, x, y, w, h, color);
    }

    /** Vector icon from Icons (square, size in GUI units). */
    public static void icon(String name, int x, int y, int size, int color) {
        String key = "i|" + name + "|" + size + "|" + ss;
        DynamicTexture t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(size * ss, size * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            hints(g);
            g.setColor(Color.WHITE);
            double k = size * ss / 24.0;
            g.scale(k, k);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Icons.draw(g, name);
            g.dispose();
            t = upload(key, img);
        }
        draw(t, x, y, size, size, color);
    }

    /** Text centered horizontally around cx. */
    public static void textCentered(String s, int cx, int y, int size, boolean bold, int color) {
        text(s, cx - width(s, size, bold) / 2, y, size, bold, color);
    }
}

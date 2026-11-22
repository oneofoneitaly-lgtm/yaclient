package dev.yaclient.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

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
 * Same crisp graphics as the 1.8.9 client: rounded shapes and anti-aliased text drawn with Java2D,
 * uploaded as textures (1 texel = 1 screen pixel) and blitted through the GUI pipeline.
 */
public final class Gfx {
    private static final class FontInfo {
        final Font font;
        final FontMetrics fm;
        FontInfo(Font font, FontMetrics fm) { this.font = font; this.fm = fm; }
    }

    private static final class Tex {
        final Identifier id;
        final DynamicTexture tex;
        final int w, h;
        Tex(Identifier id, DynamicTexture tex, int w, int h) { this.id = id; this.tex = tex; this.w = w; this.h = h; }
    }

    private static final int MAX_TEX = 500;
    private static int counter;
    private static final Map<String, Tex> TEX = new LinkedHashMap<String, Tex>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Tex> e) {
            if (size() > MAX_TEX) {
                try { Minecraft.getInstance().getTextureManager().release(e.getValue().id); } catch (Throwable ignored) { }
                return true;
            }
            return false;
        }
    };
    private static final Map<String, FontInfo> FONTS = new HashMap<String, FontInfo>();
    private static final String FAMILY = pickFamily();
    private static int ss = 2;
    /** The extractor everything is drawn on (set at the start of every draw). */
    public static GuiGraphicsExtractor g;
    /** Global transparency (open animation). */
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

    /** Real pixels per GUI unit. */
    public static void setScale(int s) { ss = Math.max(1, s); }

    public static boolean hasIcon(String name) { return Icons.has(name); }

    private static void hints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static FontInfo info(int size, boolean bold) {
        String key = size + "|" + bold + "|" + ss;
        FontInfo fi = FONTS.get(key);
        if (fi == null) {
            Font f = new Font(FAMILY, bold ? Font.BOLD : Font.PLAIN, size * ss);
            BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            hints(g2);
            FontMetrics fm = g2.getFontMetrics(f);
            g2.dispose();
            fi = new FontInfo(f, fm);
            FONTS.put(key, fi);
        }
        return fi;
    }

    private static Tex upload(String key, BufferedImage img) {
        try {
            int w = img.getWidth(), h = img.getHeight();
            NativeImage ni = new NativeImage(w, h, false);
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) ni.setPixel(x, y, img.getRGB(x, y));
            final String label = "yaclient/" + key;
            DynamicTexture dt = new DynamicTexture(() -> label, ni);
            Identifier id = Identifier.fromNamespaceAndPath("yaclient", "g/" + (counter++));
            Minecraft.getInstance().getTextureManager().register(id, dt);
            Tex t = new Tex(id, dt, w, h);
            TEX.put(key, t);
            return t;
        } catch (Throwable e) {
            return null;
        }
    }

    private static final java.util.Set<String> MISSING = new java.util.HashSet<String>();

    /** Bundled picture (classpath resource) covering a w x h box, cropped to fit. */
    public static void image(String res, int x, int y, int w, int h, int color) {
        if (g == null || MISSING.contains(res)) return;
        String key = "img:" + res;
        Tex t = TEX.get(key);
        if (t == null) {
            try (java.io.InputStream in = Gfx.class.getResourceAsStream(res)) {
                if (in == null) { MISSING.add(res); return; }
                t = upload(key, javax.imageio.ImageIO.read(in));
            } catch (Throwable e) { MISSING.add(res); return; }
            if (t == null) { MISSING.add(res); return; }
        }
        float ar = (float) w / h, tr = (float) t.w / t.h;
        int uw = t.w, vh = t.h, u = 0, v = 0;
        if (tr > ar) { uw = Math.round(t.h * ar); u = (t.w - uw) / 2; } else { vh = Math.round(t.w / ar); v = (t.h - vh) / 2; }
        g.blit(RenderPipelines.GUI_TEXTURED, t.id, x, y, (float) u, (float) v, w, h, uw, vh, t.w, t.h, faded(color));
    }

    private static int faded(int color) {
        int a = Math.round(((color >>> 24) & 255) * fade);
        return (Math.max(0, Math.min(255, a)) << 24) | (color & 0xFFFFFF);
    }

    /** Whole texture into a w x h box. */
    private static void draw(Tex t, int x, int y, int w, int h, int color) {
        if (t == null || g == null) return;
        g.blit(RenderPipelines.GUI_TEXTURED, t.id, x, y, 0f, 0f, w, h, t.w, t.h, t.w, t.h, faded(color));
    }

    /** One quarter (cs x cs GUI units) of a corner texture of 2cs x 2cs. */
    private static void corner(Tex t, int x, int y, int cs, int qx, int qy, int color) {
        if (t == null || g == null) return;
        int q = cs * ss;
        g.blit(RenderPipelines.GUI_TEXTURED, t.id, x, y, (float) (qx * q), (float) (qy * q), cs, cs, q, q, t.w, t.h, faded(color));
    }

    private static void rect(int x1, int y1, int x2, int y2, int color) {
        if (g != null && x2 > x1 && y2 > y1) g.fill(x1, y1, x2, y2, faded(color));
    }

    private static Tex cornerTex(String key, int cs, int r, boolean outline, int thick) {
        Tex t = TEX.get(key);
        if (t == null) {
            int s = 2 * cs;
            BufferedImage img = new BufferedImage(s * ss, s * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            hints(g2);
            g2.setColor(Color.WHITE);
            if (outline) {
                float th = thick * ss;
                g2.setStroke(new BasicStroke(th));
                g2.draw(new RoundRectangle2D.Float(th / 2f, th / 2f, s * ss - th, s * ss - th, 2f * r * ss, 2f * r * ss));
            } else {
                g2.fill(new RoundRectangle2D.Float(0f, 0f, s * ss, s * ss, 2f * r * ss, 2f * r * ss));
            }
            g2.dispose();
            t = upload(key, img);
        }
        return t;
    }

    /** Rounded rectangle (anti-aliased). */
    public static void rr(int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        int cs = r + 1;
        if (r > 0 && w > 2 * cs + 8 && h > 2 * cs + 8) {
            Tex ct = cornerTex("rc|" + r + "|" + ss, cs, r, false, 0);
            corner(ct, x, y, cs, 0, 0, color);
            corner(ct, x + w - cs, y, cs, 1, 0, color);
            corner(ct, x, y + h - cs, cs, 0, 1, color);
            corner(ct, x + w - cs, y + h - cs, cs, 1, 1, color);
            rect(x + cs, y, x + w - cs, y + cs, color);
            rect(x, y + cs, x + w, y + h - cs, color);
            rect(x + cs, y + h - cs, x + w - cs, y + h, color);
            return;
        }
        String key = "r|" + w + "|" + h + "|" + r + "|" + ss;
        Tex t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(w * ss, h * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            hints(g2);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0f, 0f, w * ss, h * ss, 2f * r * ss, 2f * r * ss));
            g2.dispose();
            t = upload(key, img);
        }
        draw(t, x, y, w, h, color);
    }

    public static void rrb(int x, int y, int w, int h, int r, int fill, int border) {
        rr(x, y, w, h, r, border);
        rr(x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    private static int ceilTo(int v, int m) { return ((v + m - 1) / m) * m; }

    public static int width(String s, int size, boolean bold) {
        if (s == null || s.isEmpty()) return 0;
        return (info(size, bold).fm.stringWidth(s) + ss - 1) / ss;
    }

    public static int lineHeight(int size, boolean bold) { return (info(size, bold).fm.getHeight() + ss - 1) / ss; }

    public static String trim(String s, int size, boolean bold, int maxW) {
        if (width(s, size, bold) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && width(t + "...", size, bold) > maxW) t = t.substring(0, t.length() - 1);
        return t + "...";
    }

    /** Anti-aliased text; (x, y) = top left corner. */
    public static void text(String s, int x, int y, int size, boolean bold, int color) {
        if (s == null || s.isEmpty()) return;
        FontInfo fi = info(size, bold);
        int pad = ss;
        int pw = ceilTo(fi.fm.stringWidth(s) + 2 * pad, ss);
        int ph = ceilTo(fi.fm.getHeight() + pad, ss);
        String key = "t|" + s + "|" + size + "|" + bold + "|" + ss;
        Tex t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            hints(g2);
            g2.setFont(fi.font);
            g2.setColor(Color.WHITE);
            g2.drawString(s, pad, fi.fm.getAscent());
            g2.dispose();
            t = upload(key, img);
        }
        draw(t, x - 1, y, pw / ss, ph / ss, color);
    }

    public static void textCentered(String s, int cx, int y, int size, boolean bold, int color) {
        text(s, cx - width(s, size, bold) / 2, y, size, bold, color);
    }

    /** Rounded outline with the given thickness in GUI units. */
    public static void rrOutline(int x, int y, int w, int h, int r, int thick, int color) {
        if (w <= 0 || h <= 0) return;
        int cs = r + thick;
        if (r > 0 && w > 2 * cs + 8 && h > 2 * cs + 8) {
            Tex ct = cornerTex("oc|" + r + "|" + thick + "|" + ss, cs, r, true, thick);
            corner(ct, x, y, cs, 0, 0, color);
            corner(ct, x + w - cs, y, cs, 1, 0, color);
            corner(ct, x, y + h - cs, cs, 0, 1, color);
            corner(ct, x + w - cs, y + h - cs, cs, 1, 1, color);
            rect(x + cs, y, x + w - cs, y + thick, color);
            rect(x + cs, y + h - thick, x + w - cs, y + h, color);
            rect(x, y + cs, x + thick, y + h - cs, color);
            rect(x + w - thick, y + cs, x + w, y + h - cs, color);
            return;
        }
        String key = "o|" + w + "|" + h + "|" + r + "|" + thick + "|" + ss;
        Tex t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(w * ss, h * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            hints(g2);
            g2.setColor(Color.WHITE);
            float th = thick * ss;
            g2.setStroke(new BasicStroke(th));
            g2.draw(new RoundRectangle2D.Float(th / 2f, th / 2f, w * ss - th, h * ss - th, 2f * r * ss, 2f * r * ss));
            g2.dispose();
            t = upload(key, img);
        }
        draw(t, x, y, w, h, color);
    }

    public static void icon(String name, int x, int y, int size, int color) {
        if (!Icons.has(name) && !isBuiltin(name)) name = "grid";
        String key = "i|" + name + "|" + size + "|" + ss;
        Tex t = TEX.get(key);
        if (t == null) {
            BufferedImage img = new BufferedImage(size * ss, size * ss, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            hints(g2);
            g2.setColor(Color.WHITE);
            double k = size * ss / 24.0;
            g2.scale(k, k);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Icons.draw(g2, name);
            g2.dispose();
            t = upload(key, img);
        }
        draw(t, x, y, size, size, color);
    }

    private static boolean isBuiltin(String n) {
        switch (n) {
            case "grid": case "info": case "sword": case "wrench": case "move": case "gear": case "search": case "close": case "check": case "reset": return true;
            default: return false;
        }
    }
}

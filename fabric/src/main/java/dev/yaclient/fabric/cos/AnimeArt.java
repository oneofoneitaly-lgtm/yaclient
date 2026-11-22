package dev.yaclient.fabric.cos;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/** Cape emblems of the anime bundles, drawn with vector strokes into a 512x512 texture (pure Java2D, no Minecraft deps). */
final class AnimeArt {
    private AnimeArt() {}
    static final int S = 512;

    /** Minimalist Minecraft-style pixel art: a 16x16 grid scaled up with hard pixels (use GL_NEAREST). */
    static final int G = 16, SCALE = 8;

    static BufferedImage paint(int theme) {
        if (theme == 0) return sakuraVector();          // the smooth blossom branch
        int[][] px = new int[G][G];
        switch (theme) {
            case 0: break;
            case 1: shinobi(px); break;
            case 2: neon(px); break;
            case 4: gift(px); break;
            case 5: snowflake(px); break;
            case 6: clown(px); break;
            case 7: break;
            default: kitsune(px); break;
        }
        BufferedImage img = new BufferedImage(G * SCALE, G * SCALE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++)
            for (int j = 0; j < SCALE; j++) for (int i = 0; i < SCALE; i++) img.setRGB(x * SCALE + i, y * SCALE + j, px[y][x]);
        return img;
    }

    private static void shinobi(int[][] p) {
        int silver = 0xFFDDE0F5, violet = 0xFF8C6BFF;
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            double dx = x + 0.5 - 7.6, dy = y + 0.5 - 8.0, ex = x + 0.5 - 10.0, ey = y + 0.5 - 6.6;
            if (dx * dx + dy * dy <= 6.3 * 6.3 && ex * ex + ey * ey > 5.6 * 5.6) p[y][x] = silver;
        }
        p[3][12] = violet; p[2][12] = violet; p[3][11] = violet; p[3][13] = violet; p[4][12] = violet;   // star
        p[12][12] = silver; p[13][13] = violet; p[10][13] = violet;
    }

    private static void neon(int[][] p) {
        int cyan = 0xFF00F0FF, mag = 0xFFFF2FBF, white = 0xFFFFFFFF;
        boolean[][] in = new boolean[G][G];
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            double u = (x + 0.5 - 8.0) / 5.9, v = -(y + 0.5 - 7.9) / 5.9;
            double q = u * u + v * v - 1;
            in[y][x] = q * q * q - u * u * v * v * v <= 0;
        }
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            if (!in[y][x]) continue;
            boolean edge = y == 0 || x == 0 || y == G - 1 || x == G - 1 || !in[y - 1][x] || !in[y + 1][x] || !in[y][x - 1] || !in[y][x + 1];
            p[y][x] = edge ? cyan : mag;
        }
        p[4][4] = white; p[4][5] = white; p[5][4] = white;
        p[1][14] = cyan; p[2][13] = cyan; p[2][14] = white; p[2][15] = cyan; p[3][14] = cyan;      // sparkle
        p[13][1] = mag; p[14][0] = mag; p[14][1] = white; p[14][2] = mag; p[15][1] = mag;
    }

    private static void rect(int[][] p, int x, int y, int w, int h, int col) {
        for (int j = y; j < y + h; j++) for (int i = x; i < x + w; i++) if (i >= 0 && j >= 0 && i < G && j < G) p[j][i] = col;
    }

    /** Christmas gift with a golden bow. */
    private static void gift(int[][] p) {
        int red = 0xFFD62A2A, dred = 0xFFB81E1E, white = 0xFFFFFFFF, gold = 0xFFFFC21A, green = 0xFF2E9E4B;
        rect(p, 3, 7, 10, 7, red); rect(p, 2, 5, 12, 3, dred);
        rect(p, 7, 5, 2, 9, gold); rect(p, 3, 9, 10, 1, dred);
        rect(p, 5, 3, 2, 2, gold); rect(p, 9, 3, 2, 2, gold); rect(p, 7, 4, 2, 1, gold);
        rect(p, 4, 2, 1, 1, gold); rect(p, 11, 2, 1, 1, gold);
        rect(p, 3, 13, 10, 1, dred);
        p[1][1] = white; p[2][14] = white; p[12][1] = white; p[14][13] = white; p[0][8] = white;
        p[11][14] = green; p[12][14] = green; p[12][13] = green;
    }

    /** Clown face: rainbow hair, white face, red nose, smile and a little party hat. */
    private static void clown(int[][] p) {
        int white = 0xFFFFF1DC, red = 0xFFE8262B, yel = 0xFFFFD21F, blue = 0xFF2F6FE8, grn = 0xFF2FB34A, ink = 0xFF20203A, pink = 0xFFFF8FB0;
        rect(p, 4, 4, 8, 9, white); rect(p, 5, 13, 6, 1, white); rect(p, 3, 6, 1, 5, white); rect(p, 12, 6, 1, 5, white);
        rect(p, 2, 4, 2, 3, red); rect(p, 1, 6, 2, 3, yel); rect(p, 12, 4, 2, 3, blue); rect(p, 13, 6, 2, 3, grn);      // hair tufts
        rect(p, 6, 3, 4, 1, yel); rect(p, 7, 2, 2, 1, blue); p[1][7] = red; p[1][8] = red; p[0][7] = red; p[0][8] = red;   // hat + pompom
        p[7][5] = ink; p[7][6] = ink; p[7][9] = ink; p[7][10] = ink; p[8][5] = blue; p[8][10] = blue;                    // eyes
        p[6][5] = ink; p[6][10] = ink;
        rect(p, 7, 9, 2, 2, red);                                                                                         // nose
        p[10][5] = pink; p[10][10] = pink;
        p[12][5] = red; p[12][10] = red; rect(p, 6, 13, 4, 1, red);                                                        // smile
        p[11][6] = red; p[11][9] = red;
    }

    /** Snowflake. */
    private static void snowflake(int[][] p) {
        int ice = 0xFF7CCBF5, white = 0xFFFFFFFF;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
        for (int[] d : dirs) {
            for (int k = 1; k <= 6; k++) {
                int x = 7 + d[0] * k + (d[0] >= 0 ? 1 : 0) * 0, y = 7 + d[1] * k;
                int px = 7 + d[0] * k, py = 7 + d[1] * k;
                set(p, px, py, (d[0] == 0 || d[1] == 0) ? white : ice);
                if (k == 4 && (d[0] == 0 || d[1] == 0)) { set(p, px + d[1], py + d[0], ice); set(p, px - d[1], py - d[0], ice); }
            }
        }
        rect(p, 7, 7, 2, 2, white);
        set(p, 8, 7, white);
    }

    private static void set(int[][] p, int x, int y, int col) { if (x >= 0 && y >= 0 && x < G && y < G) p[y][x] = col; }

    private static void kitsune(int[][] p) {
        String[] half = {
                "........",
                ".RR.....",
                ".RWR....",
                ".RWWRRRR",
                ".RWWWWWR",
                ".RWKKWWR",
                ".RWWWWWW",
                "..RWWWWW",
                "..RWWWWW",
                "...RWWWW",
                "...RWWWW",
                "....RWWK",
                ".....RWW",
                "......RR",
                ".......R",
                "........"};
        for (int y = 0; y < G; y++) for (int x = 0; x < 8; x++) {
            int c = 0;
            switch (half[y].charAt(x)) {
                case 'R': c = 0xFFE02B1A; break;
                case 'W': c = 0xFFFFFFFF; break;
                case 'K': c = 0xFF2A1215; break;
                default: c = 0;
            }
            p[y][x] = c; p[y][G - 1 - x] = c;
        }
    }

    /** Smooth vector sakura branch (the one the Sakura cape keeps), 512x512, use GL_LINEAR. */
    static BufferedImage sakuraVector() {
        BufferedImage img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        sakura(g);
        g.dispose();
        return img;
    }

    private static Color a(int rgb, int alpha) { return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha); }

    private static void glow(Graphics2D g, Shape s, int rgb, float w) {
        for (int i = 4; i >= 1; i--) {
            g.setStroke(new BasicStroke(w + i * 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(a(rgb, 22));
            g.draw(s);
        }
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(a(rgb, 255));
        g.draw(s);
        g.setStroke(new BasicStroke(Math.max(2f, w * 0.35f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 230));
        g.draw(s);
    }

    private static void flower(Graphics2D g, double cx, double cy, double r, double rot) {
        for (int i = 0; i < 5; i++) {
            AffineTransform t = g.getTransform();
            g.translate(cx, cy);
            g.rotate(rot + i * Math.PI * 2 / 5);
            Path2D.Double petal = new Path2D.Double();
            petal.moveTo(0, 0);
            petal.curveTo(-r * 0.75, -r * 0.35, -r * 0.55, -r * 1.05, -r * 0.14, -r * 1.00);   // notched petal tip
            petal.lineTo(0, -r * 0.86);
            petal.lineTo(r * 0.14, -r * 1.00);
            petal.curveTo(r * 0.55, -r * 1.05, r * 0.75, -r * 0.35, 0, 0);
            g.setPaint(new GradientPaint(0, 0, a(0xFFFFFF, 255), 0, (float) -r, a(0xF7A8C4, 255)));
            g.fill(petal);
            g.setColor(a(0xE0709A, 255));
            g.setStroke(new BasicStroke(3f));
            g.draw(petal);
            g.setTransform(t);
        }
        g.setColor(a(0xF2C94C, 255));
        g.fill(new Ellipse2D.Double(cx - r * 0.12, cy - r * 0.12, r * 0.24, r * 0.24));
        for (int i = 0; i < 6; i++) {
            double an = rot + i * Math.PI / 3;
            g.setColor(a(0xE0709A, 255));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Line2D.Double(cx, cy, cx + Math.cos(an) * r * 0.32, cy + Math.sin(an) * r * 0.32));
            g.fill(new Ellipse2D.Double(cx + Math.cos(an) * r * 0.32 - 4, cy + Math.sin(an) * r * 0.32 - 4, 8, 8));
        }
    }

    private static void sakura(Graphics2D g) {
        // a blossoming branch
        Path2D.Double br = new Path2D.Double();
        br.moveTo(40, 470); br.curveTo(140, 400, 190, 330, 260, 280); br.curveTo(330, 230, 380, 200, 470, 70);
        g.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(a(0x7A4A52, 255)); g.draw(br);
        Path2D.Double tw = new Path2D.Double();
        tw.moveTo(190, 335); tw.curveTo(190, 290, 160, 250, 120, 220);
        tw.moveTo(330, 232); tw.curveTo(350, 280, 390, 310, 440, 320);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(tw);
        flower(g, 120, 214, 52, 0.2); flower(g, 262, 278, 64, 0.7); flower(g, 445, 322, 50, 1.2);
        flower(g, 400, 160, 56, 0.4); flower(g, 90, 410, 44, 1.0); flower(g, 340, 100, 36, 0.1);
        // falling petals
        double[][] pt = {{60, 120, 0.8}, {200, 60, 2.2}, {440, 240, 1.4}, {330, 420, 0.3}, {170, 450, 2.6}, {470, 400, 1.9}, {40, 290, 0.5}, {230, 180, 1.2}};
        for (double[] p : pt) {
            AffineTransform t = g.getTransform();
            g.translate(p[0], p[1]); g.rotate(p[2]);
            g.setPaint(new GradientPaint(-12, 0, a(0xFFFFFF, 240), 12, 0, a(0xF7A8C4, 240)));
            g.fill(new Ellipse2D.Double(-14, -8, 28, 16));
            g.setTransform(t);
        }
    }
}

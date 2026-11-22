package dev.yaclient.cosmetics;

import java.util.LinkedHashMap;
import java.util.Map;

/** Clown bundle, fully pixelated: balloon wings, harlequin cape with a ruffle, striped pompom band and Bozo the juggling pet. */
final class ClownPix {
    private ClownPix() {}

    private static final double CELL = 0.022;

    private static final int RED = 0xE8262B, DRED = 0xA81A20, YEL = 0xFFD21F, DYEL = 0xD9A400, BLUE = 0x2F6FE8, DBLUE = 0x1E4DB0,
            GRN = 0x2FB34A, DGRN = 0x1E7F33, ORG = 0xFF8A1F, MAG = 0xE83FA8, DMAG = 0xA82A78, WHITE = 0xFFFFFF, INK = 0x20203A, CREAM = 0xFFF1DC;

    private static float[] c(int hex) { return Vox.rgb(hex, 0.25f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }
    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    // ------------------------------------------------------------------ wings: a bouquet of balloons
    private static final Map<Long, float[]> W = new LinkedHashMap<Long, float[]>();
    private static long key(int ix, int iy) { return ((long) (ix + 1000) << 20) | (iy + 1000); }
    private static void put(int ix, int iy, float[] col) { W.put(key(ix, iy), col); }

    private static void seg(double x0, double y0, double x1, double y1, double w, float[] col) {
        double len = Math.hypot(x1 - x0, y1 - y0);
        int steps = Math.max(2, (int) (len / (CELL * 0.4)));
        for (int k = 0; k <= steps; k++) {
            double t = k / (double) steps, x = x0 + (x1 - x0) * t, y = y0 + (y1 - y0) * t;
            int ax = (int) Math.floor((x - w) / CELL), bx = (int) Math.floor((x + w) / CELL);
            int ay = (int) Math.floor((y - w) / CELL), by = (int) Math.floor((y + w) / CELL);
            for (int ix = ax; ix <= bx; ix++) for (int iy = ay; iy <= by; iy++) {
                double px = (ix + 0.5) * CELL - x, py = (iy + 0.5) * CELL - y;
                if (px * px + py * py <= w * w) put(ix, iy, col);
            }
        }
    }

    /** One balloon: dark rim, body, a white shine and a small knot. */
    private static void balloon(double cx, double cy, double r, int body, int rim, int shine) {
        double rx = r, ry = r * 1.22;
        int span = (int) Math.ceil(ry / CELL) + 2, cix = (int) Math.floor(cx / CELL), ciy = (int) Math.floor(cy / CELL);
        for (int ix = cix - span; ix <= cix + span; ix++) for (int iy = ciy - span; iy <= ciy + span; iy++) {
            double px = (ix + 0.5) * CELL - cx, py = (iy + 0.5) * CELL - cy;
            // slightly egg shaped: narrower at the bottom
            double nar = py > 0 ? 1.0 - 0.18 * (py / ry) : 1.0;
            double u = px / (rx * nar), v = py / ry, d = u * u + v * v;
            if (d > 1.0) continue;
            double edge = 1.0 - Math.sqrt(d);
            float[] col;
            if (edge * rx < 0.040) col = c(rim);
            else if (u < -0.18 && v < -0.12 && u > -0.62 && v > -0.62 && (u + v) < -0.45) col = c(shine);   // shine
            else col = c(body);
            put(ix, iy, col);
        }
        // knot
        int kx = (int) Math.floor(cx / CELL), ky = (int) Math.floor((cy + ry) / CELL);
        put(kx, ky, c(rim)); put(kx - 1, ky + 1, c(rim)); put(kx, ky + 1, c(rim)); put(kx + 1, ky + 1, c(rim));
    }

    private static void balloons() {
        float[] str = c(0xF4F4F4);
        double rx = 0.03, ry = 0.0;                                             // where all strings meet (wing root)
        double[][] b = {{0.17, 0.50, 0.125}, {0.40, 0.47, 0.13}, {0.60, 0.30, 0.12}, {0.30, 0.27, 0.12}, {0.52, 0.05, 0.11}, {0.20, 0.06, 0.10}};
        int[][] pal = {{RED, DRED, 0xFFB0A8}, {YEL, DYEL, 0xFFF3A6}, {BLUE, DBLUE, 0xA9C8FF}, {GRN, DGRN, 0xA8F0B8}, {MAG, DMAG, 0xFFB8E4}, {ORG, 0xB85A0A, 0xFFD2A0}};
        for (double[] bl : b) {                                                  // strings first, balloons on top
            double mx = (bl[0] + rx) / 2 + Math.sin(bl[0] * 21) * 0.03;
            seg(bl[0], bl[1] + bl[2] * 1.22, mx, (bl[1] + ry) / 2, 0.011, str);
            seg(mx, (bl[1] + ry) / 2, rx, ry, 0.011, str);
        }
        for (int i = 0; i < b.length; i++) balloon(b[i][0], b[i][1], b[i][2], pal[i][0], pal[i][1], pal[i][2]);
        // ribbon bow where the strings are tied
        for (int ix = -1; ix <= 2; ix++) for (int iy = -1; iy <= 1; iy++) put(ix + 1, iy, c(RED));
        put(0, 0, c(WHITE));
    }

    static void wing(Anime.Th t, float age) {
        W.clear();
        balloons();
        final double hz = 0.013;
        for (Map.Entry<Long, float[]> e : W.entrySet()) {
            long k = e.getKey();
            int ix = (int) (k >> 20) - 1000, iy = (int) (k & 0xFFFFF) - 1000;
            float[] col = e.getValue();
            float sh = 0.97f + 0.05f * noise(iy, ix);
            float[] cc = {Math.min(1f, col[0] * sh), Math.min(1f, col[1] * sh), Math.min(1f, col[2] * sh), col[3]};
            double x0 = ix * CELL, x1 = x0 + CELL, y0 = iy * CELL, y1 = y0 + CELL;
            double[] p000 = Poly.p(x0, y0, -hz), p100 = Poly.p(x1, y0, -hz), p110 = Poly.p(x1, y1, -hz), p010 = Poly.p(x0, y1, -hz);
            double[] p001 = Poly.p(x0, y0, hz), p101 = Poly.p(x1, y0, hz), p111 = Poly.p(x1, y1, hz), p011 = Poly.p(x0, y1, hz);
            Poly.quad(p000, p100, p110, p010, cc, cc, cc, cc, 1f);
            Poly.quad(p001, p011, p111, p101, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix - 1, iy))) Poly.quad(p000, p010, p011, p001, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix + 1, iy))) Poly.quad(p100, p101, p111, p110, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix, iy - 1))) Poly.quad(p000, p001, p101, p100, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix, iy + 1))) Poly.quad(p010, p110, p111, p011, cc, cc, cc, cc, 1f);
        }
    }

    // ------------------------------------------------------------------ cape: harlequin diamonds with a ruffle
    static void capeBase(Anime.Th t, float age, boolean plus) {
        final int NU = 22, NV = 32;
        for (int i = 0; i < NU; i++) for (int j = 0; j < NV; j++) {
            float[] col;
            // pixel white: a few close shades of white, grouped in small blotches like Minecraft wool
            float n = noise(i, j) * 0.6f + noise(i / 3 + 40, j / 3 + 7) * 0.4f;
            int[] shades = {0xFFFFFF, 0xF6F6F8, 0xEDEDF1, 0xF9F9FB, 0xE4E4EA};
            col = c(shades[Math.min(shades.length - 1, (int) (n * shades.length))]);
            float k = 1f;
            float[] cc = {Math.min(1f, col[0] * k), Math.min(1f, col[1] * k), Math.min(1f, col[2] * k), col[3]};
            double u0 = i / (double) NU, u1 = (i + 1) / (double) NU, v0 = j / (double) NV, v1 = (j + 1) / (double) NV;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), cc, cc, cc, cc, 1f);
        }
    }

    // ------------------------------------------------------------------ band: candy stripes, pompoms, rainbow tails
    static void band(Anime.Th t, float age, float limbSwing) {
        Vox.TEXEL = 0.025; Vox.yo = 0; Vox.reset();
        final int[] stripe = {RED, YEL, BLUE, GRN};
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                double along = a == 2 ? rx * 0.27 : rz * 0.27;                  // front/back run along x, the sides along z
                if (a == 0) along = rz * 0.27;
                int k = (int) Math.floor((along + 0.6) / 0.055);
                return c(stripe[((k % 4) + 4) % 4]);
            }
        };
        float[] top = c(RED), bot = c(DRED);
        double y = -0.37, hy = 0.06;
        Vox.box(0, y, -0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(0, y, 0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(-0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.box(0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.marker = null;
        // a big pompom on each side
        double sway = Math.sin(age * 0.12) * 0.012;
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.31, y - 0.01 + sway, 0, 0.040, 0.040, 0.040, c(s < 0 ? BLUE : RED), c(s < 0 ? DBLUE : DRED), 1);
            Vox.box(s * 0.31, y - 0.045 + sway, 0, 0.022, 0.022, 0.022, c(YEL), c(DYEL), 1);
        }
        // rainbow tails at the back
        int[] tail = {RED, YEL, GRN, BLUE};
        for (int i = 0; i < 4; i++) {
            double x = (i - 1.5) * 0.045;
            for (int s = 0; s < 4; s++)
                Vox.box(x + Math.sin(age * 0.13 + s * 0.5 + i) * 0.012 * s * 0.4, y + 0.03 + s * 0.05, 0.30 + s * 0.012, 0.022, 0.026, 0.018, c(tail[i]), c(tail[(i + 1) % 4]), 1);
        }
        Vox.TEXEL = 0.0125;
    }

    // ------------------------------------------------------------------ pet: Bozo, a tiny juggling clown
    static void pet(Anime.Th t, float age) {
        Vox.TEXEL = 0.0185; Vox.reset();
        Vox.marker = null;
        double ph = age * 0.30;
        double hop = Math.abs(Math.sin(ph)) * 0.020 + Math.sin(age * 0.07) * 0.004;
        Vox.yo = -0.05 - hop;
        double bob = Math.sin(ph * 2) * 0.004;
        // big shoes
        for (int s = -1; s <= 1; s += 2) {
            double lift = Math.max(0, Math.sin(ph + (s > 0 ? Math.PI : 0))) * 0.012;
            Vox.box(s * 0.034, 0.118 - lift, -0.012, 0.030, 0.016, 0.052, c(ORG), c(0xB85A0A), 3);
            Vox.box(s * 0.034, 0.100 - lift, 0.020, 0.018, 0.020, 0.018, c(YEL), c(DYEL), 3);          // striped sock
        }
        // body: polka-dot suit
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                int k = (int) Math.floor(rx * 2.2) + (int) Math.floor(ry * 2.2) + (int) Math.floor(rz * 2.2);
                return (k & 1) == 0 ? c(YEL) : null;
            }
        };
        Vox.box(0, 0.050, 0, 0.056, 0.046, 0.042, c(RED), c(DRED), 1);
        Vox.marker = null;
        // ruffle collar and pompom buttons
        Vox.box(0, 0.002, 0, 0.074, 0.012, 0.060, c(WHITE), c(CREAM), 1);
        Vox.box(0, 0.016, -0.045, 0.012, 0.012, 0.006, c(BLUE), c(DBLUE), 0);
        Vox.box(0, 0.052, -0.045, 0.012, 0.012, 0.006, c(GRN), c(DGRN), 0);
        // arms: one up, one out, both in the suit pattern
        Vox.box(-0.075, 0.020 - Math.max(0, Math.sin(ph)) * 0.02, -0.002, 0.016, 0.034, 0.016, c(RED), c(DRED), 2);
        Vox.box(0.075, 0.030 + Math.max(0, -Math.sin(ph)) * 0.02, -0.002, 0.016, 0.034, 0.016, c(RED), c(DRED), 2);
        Vox.box(-0.075, -0.018 - Math.max(0, Math.sin(ph)) * 0.02, -0.002, 0.018, 0.016, 0.018, c(WHITE), c(CREAM), 2);   // gloves
        Vox.box(0.075, 0.064 + Math.max(0, -Math.sin(ph)) * 0.02, -0.002, 0.018, 0.016, 0.018, c(WHITE), c(CREAM), 2);
        // head
        double hy = -0.058 + bob, hz = -0.004;
        Vox.box(0, hy, hz, 0.064, 0.058, 0.056, c(CREAM), c(CREAM), 0);
        // rainbow hair tufts
        int[] hair = {RED, YEL, GRN, BLUE, MAG};
        for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 3; i++)
            Vox.box(s * (0.085 + i * 0.018), hy - 0.022 + i * 0.020 - 0.012, hz + 0.004, 0.024, 0.024, 0.030, c(hair[(i + (s > 0 ? 2 : 0)) % 5]), c(hair[(i + (s > 0 ? 3 : 1)) % 5]), 0);
        // face: eyes with brows, nose, cheeks, smile
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.030, hy - 0.010, hz - 0.058, 0.016, 0.014, 0.004, c(WHITE), c(WHITE), 0);
            Vox.box(s * 0.030, hy - 0.008, hz - 0.062, 0.008, 0.010, 0.002, c(BLUE), c(DBLUE), 0);
            Vox.box(s * 0.030, hy - 0.034, hz - 0.058, 0.018, 0.005, 0.004, c(INK), c(INK), 0);          // eyebrow
            Vox.box(s * 0.044, hy + 0.018, hz - 0.058, 0.010, 0.008, 0.003, g(0xFF8FB0, 0.5f), g(0xFF8FB0, 0.5f), 0);   // cheek
        }
        Vox.box(0, hy + 0.010, hz - 0.070, 0.020, 0.020, 0.016, g(0xFF2A2A, 0.95f), g(0xFF6A5A, 0.95f), 0);   // glowing red nose
        Vox.box(0, hy + 0.036, hz - 0.058, 0.030, 0.005, 0.003, c(RED), c(RED), 0);                          // smile
        Vox.box(-0.034, hy + 0.030, hz - 0.058, 0.006, 0.006, 0.003, c(RED), c(RED), 0);
        Vox.box(0.034, hy + 0.030, hz - 0.058, 0.006, 0.006, 0.003, c(RED), c(RED), 0);
        // party hat: yellow cone with red dots and a pompom
        Vox.box(0.006, hy - 0.072, hz, 0.050, 0.012, 0.050, c(YEL), c(DYEL), 0);
        Vox.box(0.010, hy - 0.098, hz, 0.036, 0.014, 0.036, c(BLUE), c(DBLUE), 0);
        Vox.box(0.014, hy - 0.120, hz, 0.022, 0.012, 0.022, c(YEL), c(DYEL), 0);
        Vox.box(0.016, hy - 0.140, hz, 0.014, 0.014, 0.014, c(RED), c(DRED), 0);
        // juggling balls on an arc above the head
        int[] ball = {RED, GRN, BLUE};
        for (int i = 0; i < 3; i++) {
            double a = age * 0.11 + i * 2.094;
            double bx = Math.sin(a) * 0.105, by = hy - 0.150 - Math.abs(Math.cos(a)) * 0.055;
            Vox.box(bx, by, hz, 0.016, 0.016, 0.016, g(ball[i], 0.5f), g(ball[i], 0.3f), 0);
        }
        Vox.TEXEL = 0.0125;
    }
}

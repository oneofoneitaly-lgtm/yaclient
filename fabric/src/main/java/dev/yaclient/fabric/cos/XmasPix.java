package dev.yaclient.fabric.cos;

import java.util.LinkedHashMap;
import java.util.Map;

/** Christmas bundles, fully pixelated (voxel wings, pixel cape, blocky band and pet): Santa and Frost. */
final class XmasPix {
    private XmasPix() {}

    private static final double CELL = 0.022;               // size of one wing "pixel" (finer than the pets)

    private static float[] c(int hex) { return Vox.rgb(hex, 0.25f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }
    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    // ------------------------------------------------------------------ wings: bow loops (Santa) and snow-crystal branches (Frost)
    private static final java.util.Map<Long, float[]> W = new LinkedHashMap<Long, float[]>();

    private static long key(int ix, int iy) { return ((long) (ix + 1000) << 20) | (iy + 1000); }

    private static void put(int ix, int iy, float[] col) { W.put(key(ix, iy), col); }

    /** Fills every cell whose centre lies within {@code w} of the segment. */
    private static void seg(double x0, double y0, double x1, double y1, double w, float[] col0, float[] col1) {
        double len = Math.hypot(x1 - x0, y1 - y0);
        int steps = Math.max(2, (int) (len / (CELL * 0.4)));
        for (int k = 0; k <= steps; k++) {
            double t = k / (double) steps, x = x0 + (x1 - x0) * t, y = y0 + (y1 - y0) * t;
            float[] col = Poly.mix(col0, col1, (float) t);
            int ax = (int) Math.floor((x - w) / CELL), bx = (int) Math.floor((x + w) / CELL);
            int ay = (int) Math.floor((y - w) / CELL), by = (int) Math.floor((y + w) / CELL);
            for (int ix = ax; ix <= bx; ix++) for (int iy = ay; iy <= by; iy++) {
                double px = (ix + 0.5) * CELL - x, py = (iy + 0.5) * CELL - y;
                if (px * px + py * py <= w * w) put(ix, iy, col);
            }
        }
    }

    /** A bow loop: a rotated ellipse ring with a lighter fill. */
    private static void loop(double cx, double cy, double rx, double ry, double rot, double ring, int edge, int fill, int stripe) {
        double ca = Math.cos(rot), sa = Math.sin(rot);
        int span = (int) Math.ceil(Math.max(rx, ry) / CELL) + 2;
        int cix = (int) Math.floor(cx / CELL), ciy = (int) Math.floor(cy / CELL);
        for (int ix = cix - span; ix <= cix + span; ix++) for (int iy = ciy - span; iy <= ciy + span; iy++) {
            double px = (ix + 0.5) * CELL - cx, py = (iy + 0.5) * CELL - cy;
            double u = (px * ca + py * sa) / rx, v = (-px * sa + py * ca) / ry;
            double d = u * u + v * v;
            if (d > 1.0) continue;
            double rim = 1.0 - Math.sqrt(d);                          // 0 at the edge
            if (rim * Math.min(rx, ry) < ring) put(ix, iy, c(edge));
            else put(ix, iy, ((ix + iy) % 4 == 0 && noise(ix, iy) > 0.3f) ? c(stripe) : c(fill));
        }
    }

    private static void santaWing() {
        loop(0.34, 0.30, 0.30, 0.17, 0.42, 0.045, 0xB81E1E, 0xE04040, 0xFFFFFF);     // upper loop
        loop(0.30, -0.06, 0.27, 0.15, -0.38, 0.045, 0xB81E1E, 0xE04040, 0xFFFFFF);  // lower loop
        seg(0.10, 0.10, 0.46, -0.52, 0.045, c(0xD62A2A), c(0xB81E1E));              // ribbon tails
        seg(0.08, 0.06, 0.20, -0.58, 0.040, c(0xD62A2A), c(0xB81E1E));
        seg(0.46, -0.52, 0.50, -0.60, 0.03, c(0xFFC21A), c(0xFFC21A));               // gold ends
        seg(0.20, -0.58, 0.20, -0.66, 0.03, c(0xFFC21A), c(0xFFC21A));
        for (int ix = -2; ix <= 2; ix++) for (int iy = 2; iy <= 6; iy++) put(ix + 3, iy, c(0xFFC21A));  // golden knot
    }

    private static void frostWing() {
        float[] base = c(0x4FA8E8), tip = c(0xFFFFFF);
        double[][] arms = {{36, 0.74}, {-8, 0.60}, {72, 0.50}, {-50, 0.44}};
        for (double[] arm : arms) {
            double an = Math.toRadians(arm[0]), L = arm[1], dx = Math.cos(an), dy = Math.sin(an);
            seg(0.03, 0.0, 0.03 + dx * L, dy * L, 0.036, base, tip);
            double[] ts = {0.38, 0.62, 0.84};
            for (int bi = 0; bi < ts.length; bi++) {
                double bx = 0.03 + dx * L * ts[bi], by = dy * L * ts[bi], bl = L * (0.34 - 0.08 * bi);
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    double ba = an + sgn * Math.toRadians(52);
                    seg(bx, by, bx + Math.cos(ba) * bl, by + Math.sin(ba) * bl, 0.024, Poly.mix(base, tip, (float) ts[bi]), tip);
                }
            }
        }
        for (int ix = 0; ix <= 3; ix++) for (int iy = -2; iy <= 2; iy++) put(ix, iy, c(0xFFFFFF));   // bright core
    }

    static void wing(Anime.Th t, float age) {
        if (t.index == 6) { ClownPix.wing(t, age); return; }
        W.clear();
        if (t.index == 4) santaWing(); else frostWing();
        final double hz = 0.013;
        int n = 0;
        for (java.util.Map.Entry<Long, float[]> e : W.entrySet()) {
            long k = e.getKey();
            int ix = (int) (k >> 20) - 1000, iy = (int) (k & 0xFFFFF) - 1000;
            float[] col = e.getValue();
            float sparkle = (t.index == 5 && noise(ix, iy) > 0.94f) ? 1f : 0f;
            float sh = 0.96f + 0.06f * noise(iy, ix);
            float[] cc = sparkle > 0 ? g(0xFFFFFF, 0.9f) : new float[]{Math.min(1f, col[0] * sh), Math.min(1f, col[1] * sh), Math.min(1f, col[2] * sh), col[3]};
            double x0 = ix * CELL, x1 = x0 + CELL, y0 = iy * CELL, y1 = y0 + CELL;
            double[] p000 = Poly.p(x0, y0, -hz), p100 = Poly.p(x1, y0, -hz), p110 = Poly.p(x1, y1, -hz), p010 = Poly.p(x0, y1, -hz);
            double[] p001 = Poly.p(x0, y0, hz), p101 = Poly.p(x1, y0, hz), p111 = Poly.p(x1, y1, hz), p011 = Poly.p(x0, y1, hz);
            Poly.quad(p000, p100, p110, p010, cc, cc, cc, cc, 1f);      // front
            Poly.quad(p001, p011, p111, p101, cc, cc, cc, cc, 1f);      // back
            if (!W.containsKey(key(ix - 1, iy))) Poly.quad(p000, p010, p011, p001, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix + 1, iy))) Poly.quad(p100, p101, p111, p110, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix, iy - 1))) Poly.quad(p000, p001, p101, p100, cc, cc, cc, cc, 1f);
            if (!W.containsKey(key(ix, iy + 1))) Poly.quad(p010, p110, p111, p011, cc, cc, cc, cc, 1f);
        }
    }

    // ------------------------------------------------------------------ cape: pixel grid with a fur border
    static void capeBase(Anime.Th t, float age, boolean plus) {
        if (t.index == 6) { ClownPix.capeBase(t, age, plus); return; }
        final int NU = 11, NV = 16;
        boolean santa = t.index == 4;
        for (int i = 0; i < NU; i++) for (int j = 0; j < NV; j++) {
            float[] col;
            boolean border = i == 0 || i == NU - 1 || j == 0 || j == NV - 1;
            float n = noise(i, j);
            if (santa) {
                if (border) col = c(0xFFFFFF);                                            // white fur outline
                else if (j >= NV - 3) col = ((i + j) % 2 == 0) ? c(0xFFFFFF) : c(0xF0E8E8);   // fluffy hem
                else if (i == 1 || i == NU - 2) col = c(0xB81E1E);
                else col = n > 0.85f ? c(0xE04040) : c(0xD62A2A);
            } else {
                if (border) col = c(0xFFFFFF);
                else if (j >= NV - 3) col = ((i + j) % 2 == 0) ? c(0xA8DCFA) : c(0xE6F6FF);   // icicle hem
                else if (i == 1 || i == NU - 2) col = c(0x7CCBF5);
                else col = n > 0.88f ? c(0xFFFFFF) : (n < 0.12f ? c(0xA8DCFA) : c(0xBFE6FF));
            }
            float k = 0.96f + 0.04f * noise(j, i);
            float[] cc = {Math.min(1f, col[0] * k), Math.min(1f, col[1] * k), Math.min(1f, col[2] * k), col[3]};
            double u0 = i / (double) NU, u1 = (i + 1) / (double) NU, v0 = j / (double) NV, v1 = (j + 1) / (double) NV;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), cc, cc, cc, cc, 1f);
        }
    }

    // ------------------------------------------------------------------ band (head space: y down, front -z)
    static void band(Anime.Th t, float age, float limbSwing) {
        if (t.index == 6) { ClownPix.band(t, age, limbSwing); return; }
        final boolean santa = t.index == 4;
        Vox.TEXEL = 0.025; Vox.yo = 0; Vox.reset();
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                if (santa) {
                    if (ry < -0.45) return c(0xFFFFFF);                                  // fur trim along the top
                    return null;
                }
                if (part == 0 && a == 2 && sa < 0 && Math.abs(rx) < 0.22 && ry > -0.5 && ry < 0.7) {   // snowflake on the front
                    int ix = (int) Math.round(rx * 8), iy = (int) Math.round(ry * 3);
                    if (ix == 0 || iy == 0 || Math.abs(ix) == Math.abs(iy)) return c(0xFFFFFF);
                }
                return null;
            }
        };
        float[] top = santa ? c(0xD62A2A) : c(0x7CCBF5), bot = santa ? c(0xB81E1E) : c(0x5DB4EC);
        double y = -0.37, hy = 0.06;
        Vox.box(0, y, -0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(0, y, 0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(-0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.box(0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.marker = null;
        // pompom / tails at the back
        double sway = Math.sin(age * 0.12) * 0.02;
        if (santa) {
            for (int i = 0; i < 4; i++) Vox.box(sway * i * 0.4, y + 0.03 + i * 0.05, 0.30 + i * 0.012, 0.03, 0.026, 0.02, c(0xD62A2A), c(0xB81E1E), 1);
            Vox.box(sway * 1.6, y + 0.25, 0.35, 0.045, 0.045, 0.045, c(0xFFFFFF), c(0xF0E8E8), 1);
        } else {
            for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 5; i++)
                Vox.box(s * 0.05 + Math.sin(age * 0.14 + i * 0.5 + s) * 0.02 * i * 0.3, y + 0.03 + i * 0.05, 0.30 + i * 0.015, 0.025, 0.026, 0.018, i % 2 == 0 ? c(0xFFFFFF) : c(0x7CCBF5), c(0x5DB4EC), 1);
        }
        Vox.TEXEL = 0.0125;
    }

    // ------------------------------------------------------------------ pets
    static void pet(Anime.Th t, float age) {
        if (t.index == 6) { ClownPix.pet(t, age); return; }
        Vox.TEXEL = 0.0185; Vox.reset();
        Vox.marker = null;
        if (t.index == 4) reindeer(age); else snowman(age);
        Vox.TEXEL = 0.0125;
    }

    private static void box(double cx, double cy, double cz, double hx, double hy, double hz, float[] top, float[] bot) {
        Vox.box(cx, cy, cz, hx, hy, hz, top, bot, 0);
    }

    private static void reindeer(float age) {
        float[] brown = c(0x9A6535), dbrown = c(0x6E4423), cream = c(0xFFF1DC);
        double ph = age * 0.32;
        double hop = Math.abs(Math.sin(ph)) * 0.016 + Math.sin(age * 0.07) * 0.006;
        Vox.yo = -0.045 - hop;
        double bob = Math.sin(ph * 2) * 0.004;
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                if (part == 1 && a == 0 && ry > 0.3) return c(0xFFF1DC);                  // cream belly
                if (part == 2 && a == 2 && sa < 0 && ry > 0.1) return c(0xFFF1DC);        // muzzle
                if (part == 3 && ry > 0.35) return c(0x2A1A14);                           // hooves
                return null;
            }
        };
        Vox.box(0, 0.02, 0.045, 0.068, 0.062, 0.115, brown, dbrown, 1);
        for (int s = -1; s <= 1; s += 2) for (int f = 0; f < 2; f++) {
            double lp = ph + (f + (s > 0 ? 1 : 0)) * Math.PI;
            double z = f == 0 ? -0.045 : 0.135, step = Math.sin(lp) * 0.026, lift = Math.max(0, Math.cos(lp)) * 0.016;
            Vox.box(s * 0.046, 0.108 - lift, z + step, 0.024, 0.036, 0.024, brown, dbrown, 3);
        }
        double hz = -0.115, hy = -0.072 + bob;
        Vox.box(0, hy, hz, 0.072, 0.064, 0.064, brown, brown, 0);
        Vox.box(0, hy + 0.030, hz - 0.074, 0.036, 0.026, 0.026, cream, cream, 2);
        Vox.box(0, hy + 0.012, hz - 0.101, 0.020, 0.018, 0.010, g(0xFF2A2A, 0.95f), g(0xFF6A5A, 0.95f), 0);   // glowing red nose
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.040, hy - 0.006, hz - 0.066, 0.014, 0.014, 0.004, c(0x1A0E0A), c(0x1A0E0A), 0);
            Vox.box(s * 0.045, hy - 0.010, hz - 0.069, 0.005, 0.005, 0.002, c(0xFFFFFF), c(0xFFFFFF), 0);
            Vox.box(s * 0.092, hy - 0.020, hz + 0.01, 0.026, 0.012, 0.012, brown, dbrown, 0);                 // ears
            // antlers: a trunk with two branches
            Vox.box(s * 0.040, hy - 0.100, hz + 0.02, 0.008, 0.040, 0.008, c(0xD8B56A), c(0xB89348), 0);
            Vox.box(s * 0.060, hy - 0.120, hz + 0.02, 0.020, 0.008, 0.008, c(0xD8B56A), c(0xB89348), 0);
            Vox.box(s * 0.052, hy - 0.092, hz + 0.02, 0.020, 0.007, 0.007, c(0xD8B56A), c(0xB89348), 0);
        }
        // Santa hat on top: red cap, white fur band, pompom
        Vox.box(0, hy - 0.072, hz + 0.01, 0.060, 0.012, 0.060, c(0xFFFFFF), c(0xF0E8E8), 0);
        Vox.box(0, hy - 0.100, hz + 0.02, 0.046, 0.018, 0.046, c(0xD62A2A), c(0xB81E1E), 0);
        Vox.box(0.012, hy - 0.128, hz + 0.03, 0.030, 0.012, 0.030, c(0xD62A2A), c(0xB81E1E), 0);
        Vox.box(0.032, hy - 0.140, hz + 0.034, 0.014, 0.014, 0.014, c(0xFFFFFF), c(0xF0E8E8), 0);
        // collar with a golden bell
        Vox.box(0, -0.012, -0.060, 0.070, 0.012, 0.018, g(0xE02B1A, 0.4f), g(0xB01E10, 0.3f), 0);
        Vox.box(0, 0.016, -0.082, 0.014, 0.014, 0.012, g(0xFFC21A, 0.9f), g(0xFFE27A, 0.9f), 0);
        // little white tail
        Vox.box(0, 0.0, 0.17, 0.026, 0.026, 0.026, c(0xFFFFFF), c(0xF0E8E8), 0);
        Vox.marker = null;
    }

    private static void snowman(float age) {
        double ph = age * 0.22;
        double hop = Math.abs(Math.sin(ph)) * 0.030 + Math.sin(age * 0.07) * 0.006;
        double sq = 1.0 - 0.05 * Math.max(0, -Math.sin(ph + 0.3));                // squash on landing
        Vox.yo = -0.02 - hop;
        float[] snow = c(0xFFFFFF), shade = c(0xDDEBF7);
        Vox.box(0, 0.075, 0.0, 0.080, 0.070 * sq, 0.080, snow, shade, 0);                // body ball (cube)
        Vox.box(0, -0.045, 0.0, 0.064, 0.056 * sq, 0.064, snow, shade, 0);
        double hy = -0.140;
        Vox.box(0, hy, 0.0, 0.054, 0.052, 0.054, snow, shade, 0);                        // head
        // face
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.022, hy - 0.012, -0.056, 0.008, 0.008, 0.003, c(0x1A1A22), c(0x1A1A22), 0);
            Vox.box(s * 0.030 + s * 0.012, hy + 0.020, -0.056, 0.005, 0.005, 0.003, c(0x1A1A22), c(0x1A1A22), 0);   // smile ends
        }
        Vox.box(0, hy + 0.026, -0.056, 0.020, 0.005, 0.003, c(0x1A1A22), c(0x1A1A22), 0);
        Vox.box(0, hy + 0.004, -0.078, 0.010, 0.010, 0.028, c(0xFF8A1E), c(0xE2560F), 0);   // carrot nose
        // buttons
        for (int i = 0; i < 3; i++) Vox.box(0, -0.075 + i * 0.045, -0.066 + (i == 2 ? -0.014 : 0), 0.008, 0.008, 0.003, c(0x1A1A22), c(0x1A1A22), 0);
        // top hat
        Vox.box(0, hy - 0.058, 0, 0.072, 0.006, 0.072, c(0x23232B), c(0x15151B), 0);
        Vox.box(0, hy - 0.100, 0, 0.044, 0.038, 0.044, c(0x2A2A33), c(0x15151B), 0);
        Vox.box(0, hy - 0.072, 0, 0.046, 0.010, 0.046, c(0xD62A2A), c(0xB81E1E), 0);
        // scarf with a tail that swings
        double sway = Math.sin(age * 0.18) * 0.02;
        Vox.box(0, hy + 0.062, 0, 0.062, 0.012, 0.062, c(0x2E9E4B), c(0x1F7A38), 0);
        Vox.box(0.044, hy + 0.098, -0.060 + sway, 0.016, 0.034, 0.008, c(0x2E9E4B), c(0xD62A2A), 0);
        // stick arms
        for (int s = -1; s <= 1; s += 2) {
            double lift = Math.sin(age * 0.18 + s) * 0.012;
            Vox.box(s * 0.114, -0.060 - lift, 0.0, 0.034, 0.006, 0.006, c(0x7A4A22), c(0x5A3416), 0);
            Vox.box(s * 0.150, -0.080 - lift, 0.0, 0.006, 0.020, 0.006, c(0x7A4A22), c(0x5A3416), 0);
        }
        // snow sparkle floating next to it
        Poly.ellipsoid(0.12, -0.20 + Vox.yo, 0.04, 0.016, 0.016, 0.016, g(0xFFFFFF, 1f), g(0xBFE6FF, 1f), 1f);
    }
}

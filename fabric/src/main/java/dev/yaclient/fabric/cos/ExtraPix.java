package dev.yaclient.fabric.cos;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single cosmetics (sold one by one, not in bundles): five capes, five wings, five bands and five pets.
 * Capes, wings and bands are pixel art; pets mix pixel (voxel) and smooth models.
 */
final class ExtraPix {
    private ExtraPix() {}

    interface Fn { float[] at(double x, double y); }

    private static float[] c(int hex) { return Vox.rgb(hex, 0.25f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }
    private static float[] k(int hex, float e, float m) { float[] a = Vox.rgb(hex, e); return new float[]{Math.min(1f, a[0] * m), Math.min(1f, a[1] * m), Math.min(1f, a[2] * m), a[3]}; }
    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }
    private static double sm(double t) { t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }

    // ================================================================== CAPES (pixel grid 22 x 32)
    static boolean has(String slot, String style) {
        String[] a = slot.equals("cape") ? CAPES : slot.equals("wings") ? WINGS : slot.equals("bandana") ? BANDS : slot.equals("pet") ? PETS : null;
        if (a == null || style == null) return false;
        for (String x : a) if (x.equals(style)) return true;
        return false;
    }

    static final String[] CAPES = capeList();
    private static String[] capeList() { String[] a = {"sky", "sunset", "forest", "ocean", "space", "storm"}, b = CapeArt.names(), r = new String[a.length + b.length]; System.arraycopy(a, 0, r, 0, a.length); System.arraycopy(b, 0, r, a.length, b.length); return r; }

    static void cape(String id, float age, boolean plus) { if (CapeArt.has(id)) CapeArt.draw(id, age, plus); else ExtraCape.draw(id, age, plus); }

    // ================================================================== WINGS (pixel cells in the wing plane)
    static final String[] WINGS = {"dragon", "butterfly", "azure", "venom", "dragonfly", "vampire", "vamp_crimson", "vamp_scarlet", "vamp_orange", "vamp_amber", "vamp_gold", "vamp_lime", "vamp_green", "vamp_emerald", "vamp_teal", "vamp_cyan", "vamp_sky", "vamp_blue", "vamp_indigo", "vamp_violet", "vamp_purple", "vamp_magenta", "vamp_pink", "vamp_rose", "vamp_white", "vamp_silver"};

    private static double CELL = 0.020, HZ = 0.011;
    private static final Map<Long, float[]> W = new LinkedHashMap<Long, float[]>();
    private static long key(int ix, int iy) { return ((long) (ix + 1000) << 20) | (iy + 1000); }

    private static void fill(double x0, double y0, double x1, double y1, Fn f) {
        for (int ix = (int) Math.floor(x0 / CELL); ix <= (int) Math.ceil(x1 / CELL); ix++)
            for (int iy = (int) Math.floor(y0 / CELL); iy <= (int) Math.ceil(y1 / CELL); iy++) {
                float[] col = f.at((ix + 0.5) * CELL, (iy + 0.5) * CELL);
                if (col != null) W.put(key(ix, iy), col);
            }
    }

    private static void seg(double x0, double y0, double x1, double y1, double w, float[] col) {
        double len = Math.hypot(x1 - x0, y1 - y0);
        int steps = Math.max(2, (int) (len / (CELL * 0.4)));
        for (int s = 0; s <= steps; s++) {
            double t = s / (double) steps, x = x0 + (x1 - x0) * t, y = y0 + (y1 - y0) * t;
            for (int ix = (int) Math.floor((x - w) / CELL); ix <= (int) Math.floor((x + w) / CELL); ix++)
                for (int iy = (int) Math.floor((y - w) / CELL); iy <= (int) Math.floor((y + w) / CELL); iy++) {
                    double px = (ix + 0.5) * CELL - x, py = (iy + 0.5) * CELL - y;
                    if (px * px + py * py <= w * w) W.put(key(ix, iy), col);
                }
        }
    }

    private static boolean inPoly(double x, double y, double[][] p) {
        boolean in = false;
        for (int i = 0, j = p.length - 1; i < p.length; j = i++)
            if ((p[i][1] > y) != (p[j][1] > y) && x < (p[j][0] - p[i][0]) * (y - p[i][1]) / (p[j][1] - p[i][1]) + p[i][0]) in = !in;
        return in;
    }

    /** A tapered feather/tongue: from (bx,by) at angle ang (deg), length len, half width w, curl bends it along its length. */
    private static void blade(double bx, double by, double deg, double len, double w, double curl, Fn colAtS) {
        double ang = Math.toRadians(deg);
        int steps = Math.max(6, (int) (len / (CELL * 0.35)));
        for (int s = 0; s <= steps; s++) {
            double t = s / (double) steps, a = ang + curl * t * t;
            double x = bx, y = by;
            // integrate the curved centre line
            double xx = bx, yy = by;
            for (int q = 1; q <= s; q++) { double tq = q / (double) steps, aq = ang + curl * tq * tq; xx += Math.cos(aq) * len / steps; yy += Math.sin(aq) * len / steps; }
            double ww = w * Math.pow(Math.sin(Math.PI * Math.min(1.0, 0.18 + 0.82 * t)), 0.55);
            float[] col = colAtS.at(t, 0);
            double nx = -Math.sin(a), ny = Math.cos(a);
            for (double o = -ww; o <= ww; o += CELL * 0.45) {
                int ix = (int) Math.floor((xx + nx * o) / CELL), iy = (int) Math.floor((yy + ny * o) / CELL);
                float[] cc = col;
                if (colAtS != null) { float[] e = colAtS.at(t, o / Math.max(1e-6, ww)); if (e != null) cc = e; }
                W.put(key(ix, iy), cc);
            }
        }
    }

    private static float WA = 1f;
    private static double SC = 1.0;
    private static boolean DEF = false;
    private static float AGE = 0f;
    private static boolean outline = true;

    static void wing(String id, float age) {
        CELL = 0.020; HZ = 0.011; W.clear(); WA = 1f; outline = true; SC = (id.equals("dragon") || id.equals("azure") || id.equals("venom")) ? 1.32 : (id.equals("vampire") ? 1.0 : 1.0); DEF = id.equals("azure") || id.equals("venom"); AGE = age; if (DEF) { CELL = id.equals("azure") ? 0.034 : 0.045; HZ = 0.022; SC = 1.0; }
        if (id.equals("dragon")) dragonWing(age);
        else if (id.equals("butterfly")) butterflyWing(age);
        else if (id.equals("azure")) bigDragon(age, true);
        else if (id.equals("venom")) bigDragon(age, false);
        else if (id.equals("vampire") || id.startsWith("vamp_")) { vcolors(id); vampireSmooth(age); return; }
        else dragonflyWing(age);
        emitWing();
    }

    private static void emitWing() {
        final double hz = HZ;
        for (Map.Entry<Long, float[]> e : W.entrySet()) {
            long kk = e.getKey();
            int ix = (int) (kk >> 20) - 1000, iy = (int) (kk & 0xFFFFF) - 1000;
            float[] col = e.getValue();
            boolean edge = !W.containsKey(key(ix - 1, iy)) || !W.containsKey(key(ix + 1, iy)) || !W.containsKey(key(ix, iy - 1)) || !W.containsKey(key(ix, iy + 1));
            float m = 0.97f + 0.05f * noise(iy, ix);
            if (outline && edge) m *= 0.62f;
            float[] cc = {Math.min(1f, col[0] * m), Math.min(1f, col[1] * m), Math.min(1f, col[2] * m), edge && outline ? col[3] * 0.4f : col[3]};
            double x0 = ix * CELL * SC, x1 = x0 + CELL * SC, y0 = iy * CELL * SC, y1 = y0 + CELL * SC;
            double dzz = 0, dyy = 0;
            if (DEF) {                                                    // wing folds and trails like a real wing: the tip lags behind the arm
                double xc = (x0 + x1) * 0.5, ph = AGE * 0.12 - xc * 1.9;
                dzz = 0.10 * xc * xc * Math.sin(ph) - 0.06 * xc;
                dyy = -0.07 * xc * xc * (0.5 + 0.5 * Math.sin(ph)) * 1.0;
                y0 += dyy; y1 += dyy;
            }
            double[] a = Poly.p(x0, y0, -hz + dzz), b = Poly.p(x1, y0, -hz + dzz), cq = Poly.p(x1, y1, -hz + dzz), d = Poly.p(x0, y1, -hz + dzz);
            double[] a2 = Poly.p(x0, y0, hz + dzz), b2 = Poly.p(x1, y0, hz + dzz), c2 = Poly.p(x1, y1, hz + dzz), d2 = Poly.p(x0, y1, hz + dzz);
            Poly.quad(a, b, cq, d, cc, cc, cc, cc, WA);
            Poly.quad(a2, d2, c2, b2, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix - 1, iy))) Poly.quad(a, d, d2, a2, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix + 1, iy))) Poly.quad(b, b2, c2, cq, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix, iy - 1))) Poly.quad(a, a2, b2, b, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix, iy + 1))) Poly.quad(d, cq, c2, d2, cc, cc, cc, cc, WA);
        }
    }

    // --- dragon: leathery bat wing with finger bones and a scalloped edge
    private static void dragonWing(float age) {
        final double fl = Math.sin(age * 0.2) * 0.012;
        final double[][] mem = {{0.02, 0.00}, {0.12, 0.30}, {0.36, 0.62}, {0.74, 0.54}, {0.92, 0.10 + fl}, {0.80, 0.17}, {0.73, -0.14 + fl}, {0.57, 0.05},
                {0.45, -0.20 + fl}, {0.31, 0.00}, {0.17, -0.14}};
        fill(0, -0.3, 1.0, 0.7, new Fn() { public float[] at(double x, double y) {
            if (!inPoly(x, y, mem)) return null;
            float t = (float) sm((0.62 - y) / 0.8);
            float[] col = Poly.mix(c(0xC2253A), c(0x6E0F1E), t);
            if (noise((int) (x / CELL), (int) (y / CELL)) > 0.9) col = Poly.mix(col, c(0xE0485A), 0.5f);
            return col; } });
        float[] bone = c(0x3A0A12), horn = c(0xF2D9A0);
        double[][] tips = {{0.92, 0.10 + fl}, {0.73, -0.14 + fl}, {0.45, -0.20 + fl}, {0.17, -0.14}};
        for (double[] t : tips) seg(0.74, 0.54, t[0], t[1], 0.011, bone);
        seg(0.02, 0.00, 0.12, 0.30, 0.020, bone); seg(0.12, 0.30, 0.36, 0.62, 0.018, bone); seg(0.36, 0.62, 0.74, 0.54, 0.016, bone);
        seg(0.74, 0.54, 0.80, 0.68, 0.012, horn); seg(0.36, 0.62, 0.34, 0.72, 0.010, horn);      // claws
        double glow = 0.65 + 0.35 * Math.sin(age * 0.12);
        float[] ember = g(0xFF8A1F, (float) (0.7 + 0.3 * glow));
        for (double[] t : tips) seg(0.74, 0.54, t[0] * 0.97 + 0.02, t[1] * 0.97 + 0.01, 0.0055, ember);       // glowing veins
        seg(0.12, 0.30, 0.36, 0.62, 0.006, ember); seg(0.36, 0.62, 0.74, 0.54, 0.006, ember);
    }


    // --- vampire: smooth 3D dragon wing, small: thick bone arm with a short spike, three fingers and a bowed violet-black membrane
    private static double[] vp(double x, double y, double z, float age) {
        return Poly.p(x, y, z);                                                          // membrane follows the bones, no ripple
    }

    // colour variants of the smooth vampire wings: {name, membrane, bones}
    private static final Object[][] VCOLS = {
        {"vamp_crimson", 0x8E1420, 0x2A0508},
        {"vamp_scarlet", 0xC42A1E, 0x3A0E08},
        {"vamp_orange", 0xD9701A, 0x3D1D06},
        {"vamp_amber", 0xE0A020, 0x40300A},
        {"vamp_gold", 0xE8C84A, 0x4A3C10},
        {"vamp_lime", 0x8CC63A, 0x24380C},
        {"vamp_green", 0x2E9B4A, 0x0A2A14},
        {"vamp_emerald", 0x12805E, 0x05281E},
        {"vamp_teal", 0x1FA3A0, 0x073130},
        {"vamp_cyan", 0x38C6E0, 0x0C3640},
        {"vamp_sky", 0x5AA8F0, 0x14304F},
        {"vamp_blue", 0x2F5BD6, 0x0A1840},
        {"vamp_indigo", 0x4A3AB8, 0x120E3A},
        {"vamp_violet", 0x7B3FCF, 0x22103F},
        {"vamp_purple", 0x5B2A8A, 0x180A2A},
        {"vamp_magenta", 0xC0359E, 0x3A0F30},
        {"vamp_pink", 0xF07AB8, 0x4A1A38},
        {"vamp_rose", 0xE0507A, 0x40101F},
        {"vamp_white", 0xE8E8F0, 0x5A5A66},
        {"vamp_silver", 0x9AA0AE, 0x2C2F38}
    };
    private static float[] vMem = g(0x121214, 0.95f), vBone = c(0x030304);
    private static void vcolors(String id) {
        vMem = g(0x121214, 0.95f); vBone = c(0x030304);
        for (Object[] r : VCOLS) if (r[0].equals(id)) { vMem = g((Integer) r[1], 0.95f); vBone = c((Integer) r[2]); }
    }

    private static float[] vcol(double x, double y, double jx, double jy) {
        float t = (float) sm(Math.hypot(x - jx, y - jy) / 0.46);
        return vMem;                                                                   // one flat colour
    }

    private static void vmem(double[] a, double[] b, double[] c, double[] j, float age) {
        final int n = 8;
        for (int i = 0; i < n; i++) for (int k = 0; k < n - i; k++) {
            double[][] P = new double[4][]; float[][] C = new float[4][];
            int[][] ij = {{i, k}, {i + 1, k}, {i, k + 1}, {i + 1, k + 1}};
            for (int q = 0; q < 4; q++) {
                double u = ij[q][0] / (double) n, v = ij[q][1] / (double) n, w = 1 - u - v;
                double x = a[0] * w + b[0] * u + c[0] * v, y = a[1] * w + b[1] * u + c[1] * v;
                double billow = 0.030 * 27.0 * u * v * w;                                     // bows backwards between the bones
                double z0 = a[2] * w + b[2] * u + c[2] * v;                                          // carry the bones' swing into the membrane
                P[q] = vp(x, y, z0 + billow, age); C[q] = vcol(x, y, j[0], j[1]);
            }
            Poly.tri(P[0], P[1], P[2], C[0], C[1], C[2], 1f);
            if (k < n - i - 1) Poly.tri(P[1], P[3], P[2], C[1], C[3], C[2], 1f);
        }
    }

    private static double[] vsub(double[] a, double[] b) { return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    private static double[] vcross(double[] a, double[] b) { return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }
    private static double[] vnorm(double[] a) { double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]); return new double[]{a[0] / l, a[1] / l, a[2] / l}; }
    private static double[] voff(double[] p, double[] n, double kn, double[] m, double km) {
        return Poly.p(p[0] + n[0] * kn + m[0] * km, p[1] + n[1] * kn + m[1] * km, p[2] + n[2] * kn + m[2] * km);
    }

    /** A bone: tapered 4-sided prism from p to q (real thickness). */
    private static void vbone(double[] p, double[] q, double w0, double w1, float[] col, float[] edge) {
        double[] d = vsub(q, p);
        double[] n = vnorm(vcross(d, new double[]{0, 0, 1}));
        double[] m = vnorm(vcross(d, n));
        double d0 = w0, d1 = w1;                                                           // square cross-section
        double[][] P = {voff(p, n, w0, m, d0), voff(p, n, -w0, m, d0), voff(p, n, -w0, m, -d0), voff(p, n, w0, m, -d0)};
        double[][] Q = {voff(q, n, w1, m, d1), voff(q, n, -w1, m, d1), voff(q, n, -w1, m, -d1), voff(q, n, w1, m, -d1)};
        for (int i = 0; i < 4; i++) {
            int k = (i + 1) % 4;
            float[] cc = (i == 0 || i == 2) ? edge : col;
            Poly.quad(P[i], P[k], Q[k], Q[i], cc, cc, cc, cc, 1f);
        }
        Poly.quad(P[0], P[1], P[2], P[3], col, col, col, col, 1f);
    }

    private static double[] lerp(double[] a, double[] b, double t) { return Poly.p(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t); }

    private static void vcube(double[] c, double h, float[] col) {
        double[][] P = new double[8][];
        for (int i = 0; i < 8; i++) P[i] = Poly.p(c[0] + ((i & 1) == 0 ? -h : h), c[1] + ((i & 2) == 0 ? -h : h), c[2] + ((i & 4) == 0 ? -h : h));
        int[][] f = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
        for (int[] q : f) Poly.quad(P[q[0]], P[q[1]], P[q[2]], P[q[3]], col, col, col, col, 1f);
    }

    private static void vampireSmooth(float age) {
        double sw = Math.sin(age * 0.10);                                                                              // slow, in step with the flap
        double[] A = Poly.p(0.02, 0.0, 0), J = Poly.p(0.30, 0.20, 0);                                                 // arm + joint stay still
        double[] T = Poly.p(0.70, 0.24, sw * 0.07);                                                                    // leading edge: only this swings back and forth
        double th = 0;                                                                                         // the three fingers + membrane move up/down together, hinged at the joint
        double cs = Math.cos(th), sn = Math.sin(th);
        double[][] base = {{0.65, -0.15}, {0.43, -0.41}, {0.155, -0.152}};
        double[][] F = new double[3][];
        for (int i = 0; i < 3; i++) {
            double dx = base[i][0] - J[0], dy = base[i][1] - J[1];
            F[i] = Poly.p(J[0] + dx * cs - dy * sn, J[1] + dx * sn + dy * cs, i == 2 ? 0 : sw * 0.09);   // follows the leading edge, a hair behind; the small inner finger (next to the body) stays still
        }
        double[][] Fm = new double[3][];
        for (int i = 0; i < 3; i++) Fm[i] = lerp(J, F[i], 0.99);
        double[] Tm = lerp(J, T, 0.99);
        double[][] M = new double[2][];
        for (int i = 0; i < 2; i++) { double[] mid = lerp(Fm[i], Fm[i + 1], 0.5); M[i] = lerp(mid, J, 0.10); }
        double[] M0 = lerp(lerp(Tm, Fm[0], 0.5), J, 0.10);
        vmem(J, Tm, M0, J, age); vmem(J, M0, Fm[0], J, age);
        vmem(J, Fm[0], M[0], J, age); vmem(J, M[0], Fm[1], J, age);
        vmem(J, Fm[1], M[1], J, age); vmem(J, M[1], Fm[2], J, age);
        vmem(J, Fm[2], A, J, age);
        float[] bone = vBone, edge = vBone;
        vbone(A, J, 0.040, 0.032, bone, edge);
        vbone(J, T, 0.018, 0.011, bone, edge);                                                                        // leading edge
        vcube(T, 0.011, bone);
        double[] w = {0.021, 0.024, 0.016};
        for (int i = 0; i < 3; i++) {
            vbone(J, F[i], w[i], w[i] * 0.62, bone, edge);
            vcube(F[i], w[i] * 0.62, bone);
        }
    }

    // --- butterfly: two rounded lobes with a dark border, white dots and blue eyespots
    private static void butterflyWing(float age) {
        final double fl = Math.sin(age * 0.16) * 0.02;
        fill(0, -0.3, 0.9, 0.8, new Fn() { public float[] at(double x, double y) {
            double ca = Math.cos(0.45), sa = Math.sin(0.45);
            double ux = ((x - 0.40) * ca + (y - 0.38 - fl) * sa) / 0.40, uy = (-(x - 0.40) * sa + (y - 0.38 - fl) * ca) / 0.29;
            double d = Math.sqrt(ux * ux + uy * uy);
            if (d <= 1) return butter(d, Math.atan2(uy, ux), x, y);
            double cb = Math.cos(-0.35), sb = Math.sin(-0.35);
            double vx = ((x - 0.27) * cb + (y + 0.10) * sb) / 0.27, vy = (-(x - 0.27) * sb + (y + 0.10) * cb) / 0.20;
            double e = Math.sqrt(vx * vx + vy * vy);
            if (e <= 1) return butter(e, Math.atan2(vy, vx) + 2, x, y);
            return null; } });
        seg(0.02, 0.02, 0.60, 0.50, 0.007, c(0x3A1A06)); seg(0.02, 0.02, 0.45, 0.05, 0.007, c(0x3A1A06)); seg(0.02, 0.02, 0.34, 0.30, 0.007, c(0x3A1A06));
    }
    private static float[] butter(double d, double ang, double x, double y) {
        if (d > 0.88) {
            int dots = (int) Math.floor(ang * 5 / Math.PI);
            return (d > 0.90 && d < 0.97 && dots % 2 == 0) ? c(0xFFFFFF) : c(0x2A1608);
        }
        if (d > 0.74) return c(0xFFD21F);
        if (d < 0.30) return c(0x2F6FE8);
        if (d < 0.42) return c(0x8CC0FF);
        return Poly.mix(c(0xFF8A1E), c(0xE8501A), (float) sm((d - 0.4) / 0.4));
    }

    // --- Bedrock-style chunky wings (big blocks, thick). azure = rounded feathered arch, white to blue. venom = swept-back dragon wing, purple/green/gold.
    private static void bigDragon(final float age, final boolean azure) {
        if (azure) {
            final double[] lens = {0.58, 0.76, 0.92, 1.02, 1.04, 0.98, 0.86, 0.68};
            fill(-0.1, -0.7, 1.2, 1.0, new Fn() { public float[] at(double x, double y) {
                double dx = x - 0.03, dy = y - 0.06, r = Math.hypot(dx, dy), th = Math.toDegrees(Math.atan2(dy, dx));
                double s = (112 - th) / 15.0;                               // feather index along the fan
                if (s < 0 || s >= lens.length || r < 0.04) return null;
                int k = (int) Math.floor(s); double frac = s - k;
                double len = lens[k] * (1 - 0.10 * Math.abs(2 * frac - 1) * 1.0) - (frac > 0.5 ? 0 : 0.0);
                if (r > len) return null;
                double t = r / len;
                float[] col = t < 0.30 ? c(0xFFFFFF) : t < 0.55 ? c(0xCFE8FF) : t < 0.80 ? c(0x86BBF7) : c(0x4A84E0);
                if (frac < 0.09 || frac > 0.93) col = Poly.mix(col, c(0x2F63BE), 0.55f);                             // dark gap between feathers
                if (k < 2 && t < 0.35) col = c(0xF2F8FF);
                if (r > len - CELL * 1.1) col = Poly.mix(col, c(0x2F63BE), 0.35f);
                return col; } });
        } else {
            // stepped valkyrie wing: four long pointed blades stacked like stairs, gold on top, green, violet, deep purple underneath
            final double[] ang = {-8, 14, 36, 60}, len = {0.70, 0.88, 1.02, 0.86}, wd = {0.16, 0.17, 0.18, 0.15};
            final int[][] pal = {{0x5A22A8, 0x3A1480}, {0x9A55EA, 0x6E2FC8}, {0x3FDB6E, 0x20A84A}, {0xFFD21F, 0xE0A000}};
            for (int rowk = 0; rowk < 4; rowk++) {
                final int kk = rowk;
                final double a0 = Math.toRadians(ang[kk]), ca = Math.cos(a0), sa = Math.sin(a0);
                final double bx = 0.04, by = 0.02 + kk * 0.07;
                fill(-0.1, -0.6, 1.2, 1.0, new Fn() { public float[] at(double x, double y) {
                    double dx = x - bx, dy = y - by, along = dx * ca + dy * sa, across = -dx * sa + dy * ca;
                    if (along < 0 || along > len[kk]) return null;
                    double t = along / len[kk], w = wd[kk] * (1 - 0.92 * Math.pow(t, 2.2));
                    if (Math.abs(across) > w) return null;
                    float[] col = c(t < 0.5 ? pal[kk][0] : pal[kk][1]);
                    if (across > w * 0.45) col = Poly.mix(col, c(0x000000), 0.18f);                                   // lower half in shade
                    if (across < -w * 0.55 && t > 0.1) col = Poly.mix(col, c(0xFFFFFF), 0.22f);
                    if (((int) Math.floor(along / 0.09)) % 3 == 0 && Math.abs(across) < w * 0.25) col = Poly.mix(col, c(0xFFFFFF), 0.25f);   // chunky feather stripe
                    return col; } });
            }
            // gold shoulder plate
            fill(-0.05, -0.1, 0.26, 0.3, new Fn() { public float[] at(double x, double y) {
                double dx = x - 0.08, dy = y - 0.09;
                return dx * dx / 0.011 + dy * dy / 0.016 < 1 ? c(dx + dy > 0 ? 0xE0A000 : 0xFFE46A) : null; } });
        }
    }

    // --- dragonfly: slim translucent wings with veins and a dark tip mark
    private static void dragonflyWing(float age) {
        WA = 0.72f; outline = false;
        final double fl = Math.sin(age * 0.3) * 0.02;
        double[][] ws = {{0.40, 0.30 + fl, 0.85, 0.44, 0.095}, {0.38, 0.12 + fl * 0.6, 0.55, 0.40, 0.085}, {0.34, -0.03, 0.15, 0.36, 0.08}};
        for (final double[] w : ws) {
            fill(0, -0.4, 0.95, 0.8, new Fn() { public float[] at(double x, double y) {
                double ca = Math.cos(w[2]), sa = Math.sin(w[2]);
                double ux = ((x - w[0]) * ca + (y - w[1]) * sa) / w[3], uy = (-(x - w[0]) * sa + (y - w[1]) * ca) / w[4];
                double d = ux * ux + uy * uy;
                if (d > 1) return null;
                if (d > 0.78) return g(0x3FB7C9, 0.5f);                                                  // edge vein
                if (Math.abs(uy) < 0.12) return g(0x3FB7C9, 0.5f);                                       // main vein
                if (ux > 0.80 && Math.abs(uy) < 0.5) return g(0x1E4A5A, 0.4f);                           // dark tip mark
                if (((int) Math.floor((ux + 1) * 5.5)) % 2 == 0 && Math.abs(uy) < 0.85 && ((int) Math.floor((uy + 1) * 3)) % 3 == 1) return g(0x7ED8E8, 0.5f);
                return g(0xCFF6FF, 0.6f); } });
        }
    }

    // ================================================================== BANDS (voxel rings around the head)
    static final String[] BANDS = {"ninja", "crown", "halo", "cat", "rainbow"};
    private static final double BY = -0.37;


    /** Point on the halo torus: ring angle a, tube angle t. y points down, so up is negative. */
    private static double[] hp(double a, double t, double R, double rw, double rh, double bob, double tx, double tz) {
        double r = R + rw * Math.cos(t), x = Math.sin(a) * r, z = Math.cos(a) * r;
        double y = BY - 0.33 + bob + tx * z + tz * x - rh * Math.sin(t);
        return Poly.p(x, y, z);
    }

    private static float[] hc(double a, double t, float age) {
        double run = Math.max(0, Math.cos(a - age * 0.12));                         // bright spot travelling around the ring
        double top = 0.5 - 0.5 * Math.sin(t);                                       // 0 on top, 1 underneath
        float[] hi = g(0xFFF2A8, (float) (0.8 + 0.2 * run)), lo = g(0xD99A00, 0.75f);
        return Poly.mix(Poly.mix(hi, lo, (float) top), g(0xFFFFFF, 1f), (float) (run * run * 0.35 * (1 - top)));
    }

    private static void ring(double y, double hy, float[] top, float[] bot) {
        Vox.box(0, y, -0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(0, y, 0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(-0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.box(0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
    }

    static void band(String id, float age, float limb) {
        Vox.TEXEL = 0.025; Vox.yo = 0; Vox.reset(); Vox.marker = null;
        if (id.equals("ninja")) {
            ring(BY, 0.05, c(0x1C1C26), c(0x0E0E14));
            Vox.box(0, BY, -0.285, 0.10, 0.045, 0.012, c(0xC9CEDB), c(0x8A90A4), 1);                 // steel plate
            Vox.box(0, BY, -0.298, 0.045, 0.020, 0.004, c(0x1C1C26), c(0x1C1C26), 1);               // emblem
            Vox.box(0, BY, -0.299, 0.012, 0.045, 0.004, c(0x1C1C26), c(0x1C1C26), 1);
            for (int t = 0; t < 2; t++) for (int s = 0; s < 6; s++) {
                double sw = Math.sin(age * 0.14 + s * 0.6 + t * 1.5) * 0.02 * s * 0.3;
                Vox.box(-0.04 + t * 0.08 + sw, BY + 0.03 + s * 0.055, 0.30 + s * 0.02, 0.026, 0.030, 0.012, c(0x1C1C26), c(0x2E2E3C), 1);
            }
        } else if (id.equals("crown")) {
            ring(BY - 0.01, 0.045, c(0xFFC21F), c(0xC98A00));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4, x = Math.sin(a) * 0.268, z = -Math.cos(a) * 0.268;
                double h = (i % 2 == 0) ? 0.130 : 0.075;
                Vox.box(x, BY - 0.055 - h, z, 0.030, h, 0.030, c(0xFFD84A), c(0xD9A200), 1);
                Vox.box(x, BY - 0.055 - 2 * h - 0.020, z, 0.024, 0.024, 0.024, (i % 2 == 0) ? g(0xFF3B5C, 0.7f) : g(0x4FC3F7, 0.7f), g(0xFFFFFF, 0.5f), 1);
            }
            Vox.box(0, BY, -0.282, 0.032, 0.028, 0.010, g(0xFF3B5C, 0.7f), g(0xFF9AAE, 0.7f), 1);   // front ruby
        } else if (id.equals("halo")) {
            // thick golden halo floating above the head (smooth 3D ring): it bobs, tilts and drifts slowly, with a light running around it
            double bob = Math.sin(age * 0.10) * 0.020, tx = Math.sin(age * 0.07) * 0.13, tz = Math.cos(age * 0.055) * 0.13;
            final int nr = 44, nt = 12;
            final double R = 0.22, rw = 0.058, rh = 0.042;
            for (int i = 0; i < nr; i++) {
                double a0 = i * 2 * Math.PI / nr, a1 = (i + 1) * 2 * Math.PI / nr;
                for (int j = 0; j < nt; j++) {
                    double t0 = j * 2 * Math.PI / nt, t1 = (j + 1) * 2 * Math.PI / nt;
                    double[] p00 = hp(a0, t0, R, rw, rh, bob, tx, tz), p10 = hp(a1, t0, R, rw, rh, bob, tx, tz);
                    double[] p11 = hp(a1, t1, R, rw, rh, bob, tx, tz), p01 = hp(a0, t1, R, rw, rh, bob, tx, tz);
                    float[] c00 = hc(a0, t0, age), c10 = hc(a1, t0, age), c11 = hc(a1, t1, age), c01 = hc(a0, t1, age);
                    Poly.quad(p00, p10, p11, p01, c00, c10, c11, c01, 1f);
                }
            }
        } else if (id.equals("cat")) {
            ring(BY, 0.04, c(0xFF8FB8), c(0xE0608F));
            for (int s = -1; s <= 1; s += 2) {
                double x = s * 0.17;
                Vox.box(x, BY - 0.110, -0.10, 0.085, 0.065, 0.040, c(0xFF8FB8), c(0xE0608F), 1);
                Vox.box(x + s * 0.014, BY - 0.205, -0.10, 0.060, 0.034, 0.040, c(0xFF8FB8), c(0xE0608F), 1);
                Vox.box(x + s * 0.026, BY - 0.270, -0.10, 0.034, 0.034, 0.040, c(0xFF8FB8), c(0xE0608F), 1);
                Vox.box(x, BY - 0.110, -0.145, 0.052, 0.046, 0.006, c(0xFFD3E4), c(0xFFB0CC), 1);
                Vox.box(x + s * 0.010, BY - 0.195, -0.145, 0.032, 0.028, 0.006, c(0xFFD3E4), c(0xFFB0CC), 1);
            }
            Vox.box(0, BY + 0.02, -0.285, 0.020, 0.020, 0.012, g(0xFFD21F, 0.6f), g(0xC98A00, 0.4f), 1);   // bell
            Vox.box(0, BY + 0.005, -0.290, 0.030, 0.006, 0.006, c(0xE0608F), c(0xE0608F), 1);
        } else {
            int[] rb = {0xE8262B, 0xFF8A1F, 0xFFD21F, 0x2FB34A, 0x2F6FE8, 0x8A3FD8};
            for (int i = 0; i < 6; i++) { double y = BY - 0.055 + i * 0.022; ring(y, 0.011, c(rb[i]), c(rb[i])); }
            for (int i = 0; i < 6; i++) for (int s = 0; s < 5; s++) {
                double sw = Math.sin(age * 0.13 + s * 0.5 + i) * 0.018 * s * 0.3;
                Vox.box((i - 2.5) * 0.034 + sw, BY + 0.05 + s * 0.05, 0.30 + s * 0.012, 0.016, 0.026, 0.010, c(rb[i]), c(rb[i]), 1);
            }
        }
        Vox.TEXEL = 0.0125;
    }

    // ================================================================== PETS
    static final String[] PETS = {"drake", "bunny", "penguin", "panda", "axolotl", "puppy"};

    static void pet(String id, float age) {
        Vox.TEXEL = 0.0185; Vox.reset(); Vox.marker = null; Vox.yo = -0.05;
        if (id.equals("drake")) drake(age);
        else if (id.equals("bunny")) bunny(age);
        else if (id.equals("penguin")) penguin(age);
        else if (id.equals("panda")) panda(age);
        else if (id.equals("puppy")) puppy(age);
        else axolotl(age);
        Vox.TEXEL = 0.0125; Vox.yo = 0;
    }

    private static void drake(float age) {
        double ph = age * 0.28, hop = Math.abs(Math.sin(ph)) * 0.016;
        Vox.yo = -0.05 - hop;
        float[] gr = c(0x39B54A), dg = c(0x1E7F33), cr = c(0xFFF0C0);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.034, 0.112, -0.006, 0.026, 0.020, 0.036, gr, dg, 3);
        Vox.box(0, 0.050, 0.004, 0.056, 0.050, 0.048, gr, dg, 1);
        Vox.box(0, 0.058, -0.040, 0.034, 0.040, 0.010, cr, c(0xE8D090), 1);                         // belly
        for (int i = 0; i < 4; i++) {                                                                // tail with a spade tip
            double sw = Math.sin(age * 0.18 - i * 0.7) * 0.012 * (i + 1) * 0.5;
            Vox.box(sw, 0.075 - i * 0.012, 0.062 + i * 0.034, 0.024 - i * 0.003, 0.020 - i * 0.003, 0.018, gr, dg, 4);
        }
        Vox.box(Math.sin(age * 0.18 - 2.8) * 0.02, 0.040, 0.205, 0.020, 0.020, 0.012, c(0xE8262B), c(0xA81A20), 4);
        double fl = Math.sin(age * 0.5) * 0.022;
        for (int s = -1; s <= 1; s += 2) {                                                           // wings
            Vox.box(s * 0.092, 0.010 - fl, 0.020, 0.040, 0.008, 0.034, c(0x2E8F42), dg, 5);
            Vox.box(s * 0.130, -0.004 - fl * 1.5, 0.028, 0.030, 0.008, 0.026, c(0xE8262B), c(0xA81A20), 5);
        }
        double hy = -0.058;
        Vox.box(0, hy, -0.016, 0.054, 0.046, 0.046, gr, dg, 0);
        Vox.box(0, hy + 0.026, -0.070, 0.032, 0.022, 0.022, gr, dg, 2);                              // snout
        Vox.box(-0.012, hy + 0.022, -0.094, 0.005, 0.005, 0.003, c(0x1B1B22), c(0x1B1B22), 2);
        Vox.box(0.012, hy + 0.022, -0.094, 0.005, 0.005, 0.003, c(0x1B1B22), c(0x1B1B22), 2);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.030, hy - 0.010, -0.058, 0.014, 0.014, 0.004, g(0xFFE24A, 0.7f), g(0xFFE24A, 0.7f), 0);
            Vox.box(s * 0.030, hy - 0.010, -0.062, 0.006, 0.010, 0.002, c(0x1B1B22), c(0x1B1B22), 0);
            Vox.box(s * 0.038, hy - 0.056, -0.020, 0.010, 0.026, 0.010, cr, c(0xE8D090), 0);          // horns
            Vox.box(s * 0.042, hy - 0.090, -0.018, 0.006, 0.014, 0.006, cr, c(0xE8D090), 0);
        }
        Vox.box(0, hy - 0.052, 0.020, 0.010, 0.010, 0.026, c(0x2E8F42), dg, 0);                      // back spikes
    }

    private static void bunny(float age) {
        double hop = Math.max(0, Math.sin(age * 0.12)) * 0.012, tw = Math.sin(age * 0.3) * 0.006;
        Vox.yo = -0.05 - hop;
        float[] wh = c(0xFFFFFF), sh = c(0xE4E8F2), pk = c(0xFFA6C4), bk = c(0x2A2430);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.040, 0.108, -0.016, 0.022, 0.016, 0.040, sh, sh, 3);        // big feet
        Vox.box(0, 0.056, 0.012, 0.056, 0.048, 0.056, wh, sh, 1);                                                    // body
        Vox.box(0, 0.060, -0.044, 0.030, 0.034, 0.010, sh, sh, 1);                                                   // belly
        Vox.box(0, 0.066, 0.078, 0.026, 0.026, 0.020, wh, wh, 4);                                                    // fluffy tail
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.060, 0.050, -0.010, 0.012, 0.030, 0.020, sh, sh, 2);          // arms
        double hy = -0.050;
        Vox.box(0, hy, -0.014, 0.056, 0.046, 0.048, wh, sh, 0);                                                      // head
        for (int s = -1; s <= 1; s += 2) {
            double lean = s * 0.008 + tw * s;
            Vox.box(s * 0.034 + lean, hy - 0.090, -0.004, 0.018, 0.050, 0.012, wh, sh, 0);                          // tall ears
            Vox.box(s * 0.034 + lean, hy - 0.088, -0.016, 0.009, 0.040, 0.003, pk, pk, 0);                         // pink inside
            Vox.box(s * 0.028, hy - 0.004, -0.064, 0.009, 0.012, 0.003, bk, bk, 0);                                // eyes
            Vox.box(s * 0.044, hy + 0.014, -0.062, 0.010, 0.007, 0.003, pk, pk, 0);                                // blush
        }
        Vox.box(0, hy + 0.012, -0.066, 0.009, 0.007, 0.004, pk, pk, 2);                                             // nose
        Vox.box(0, hy + 0.026, -0.064, 0.003, 0.008, 0.003, bk, bk, 2);                                             // mouth
    }

    private static void penguin(float age) {
        double ph = age * 0.22, wad = Math.sin(ph) * 0.012;
        Vox.yo = -0.05;
        float[] bk = c(0x23262E), wh = c(0xFFFFFF), or = c(0xFF9A1F);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.036 + wad * s * 0.5, 0.112, -0.030, 0.026, 0.014, 0.040, or, or, 3);   // orange feet
        Vox.box(wad, 0.050, 0.010, 0.060, 0.056, 0.050, bk, bk, 1);
        Vox.box(wad, 0.056, -0.042, 0.040, 0.046, 0.010, wh, wh, 1);                                                // white belly
        double fl = Math.sin(age * 0.35) * 0.014;
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.076 + wad, 0.044 - fl * s * 0.3, 0.004, 0.012, 0.040, 0.024, bk, bk, 2);   // flippers
        Vox.box(wad, -0.002, -0.010, 0.050, 0.006, 0.044, c(0x2F9BE8), c(0x1E6DB0), 1);                             // blue scarf
        Vox.box(wad + 0.036, 0.016, -0.042, 0.010, 0.026, 0.008, c(0x2F9BE8), c(0x1E6DB0), 1);
        double hy = -0.050;
        Vox.box(wad, hy, -0.010, 0.054, 0.044, 0.046, bk, bk, 0);
        Vox.box(wad, hy + 0.004, -0.048, 0.036, 0.034, 0.006, wh, wh, 0);                                           // white face
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(wad + s * 0.022, hy - 0.006, -0.056, 0.012, 0.014, 0.003, Poly.INK, Poly.INK, 0);
            Vox.box(wad + s * 0.022, hy - 0.010, -0.060, 0.004, 0.005, 0.002, wh, wh, 0);
            Vox.box(wad + s * 0.040, hy + 0.014, -0.054, 0.010, 0.007, 0.003, c(0xFFB0B0), c(0xFFB0B0), 0);        // blush
        }
        Vox.box(wad, hy + 0.016, -0.068, 0.014, 0.008, 0.012, or, or, 2);                                           // beak
    }

    private static void panda(float age) {
        double bob = Math.sin(age * 0.10) * 0.006;
        Vox.yo = -0.05 + bob;
        float[] w = c(0xFFFFFF), wd = c(0xE4E4EC), bl = c(0x23232B);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.050, 0.108, -0.030, 0.030, 0.022, 0.040, bl, bl, 3);     // feet
        Vox.box(0, 0.050, 0.004, 0.066, 0.050, 0.052, w, wd, 1);
        Vox.box(0, 0.040, 0.004, 0.068, 0.020, 0.054, bl, bl, 1);                                                // black belt across the chest
        double eat = Math.sin(age * 0.3) * 0.012;
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.086, 0.040, -0.030, 0.024, 0.034, 0.024, bl, bl, 2);
        Vox.box(0.062, 0.020 + eat, -0.072, 0.012, 0.050, 0.012, c(0x6BCB4A), c(0x3E9A2A), 2);                       // bamboo
        Vox.box(0.062, 0.020 + eat, -0.072, 0.014, 0.004, 0.014, c(0x2E7A22), c(0x2E7A22), 2);
        Vox.box(0.062, -0.014 + eat, -0.072, 0.014, 0.004, 0.014, c(0x2E7A22), c(0x2E7A22), 2);
        Vox.box(0.072, -0.050 + eat, -0.072, 0.022, 0.010, 0.010, c(0x8EE070), c(0x6BCB4A), 2);                  // leaf
        double hy = -0.062;
        Vox.box(0, hy, -0.010, 0.064, 0.054, 0.052, w, wd, 0);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.052, hy - 0.058, -0.006, 0.022, 0.022, 0.016, bl, bl, 0);                              // ears
            Vox.box(s * 0.030, hy - 0.004, -0.060, 0.022, 0.030, 0.006, bl, bl, 0);                              // eye patches
            Vox.box(s * 0.032, hy - 0.008, -0.067, 0.006, 0.008, 0.002, w, w, 0);
        }
        Vox.box(0, hy + 0.020, -0.062, 0.016, 0.012, 0.008, bl, bl, 0);                                          // nose
        Vox.box(0, hy + 0.040, -0.058, 0.014, 0.004, 0.004, bl, bl, 0);
    }


    private static void puppy(float age) {
        double bob = Math.sin(age * 0.10) * 0.006;
        Vox.yo = -0.05 + bob;
        float[] gd = c(0xE6B25E), gl = c(0xB5803A), cr = c(0xFBE6B8), cd = c(0xE8CC96), ear = c(0xA8702C), dk = c(0x2A1A10), pk = c(0xFF7A9A);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.040, 0.098, 0.036, 0.022, 0.028, 0.030, gd, gl, 3);                                    // sitting back legs
            Vox.box(s * 0.036, 0.094, -0.040, 0.016, 0.034, 0.016, cr, cd, 2);                                   // front legs
            Vox.box(s * 0.036, 0.124, -0.044, 0.018, 0.006, 0.022, cr, cd, 3);                                   // paws
        }
        Vox.box(0, 0.050, 0.014, 0.052, 0.042, 0.062, gd, gl, 1);                                                // body
        Vox.box(0, 0.062, -0.034, 0.040, 0.034, 0.020, cr, cd, 1);                                               // cream chest
        double wag = Math.sin(age * 0.55);
        for (int i = 0; i < 4; i++) {                                                                            // wagging tail
            Vox.box(wag * 0.022 * (i + 1) * 0.5, 0.040 - i * 0.026, 0.086 + i * 0.010, 0.012, 0.016, 0.012, i == 3 ? cr : gd, i == 3 ? cd : gl, 4);
        }
        double hy = -0.030 + Math.sin(age * 0.10 + 0.8) * 0.004;
        Vox.box(0, hy, -0.052, 0.050, 0.040, 0.042, gd, gl, 0);                                                  // head
        Vox.box(0, hy + 0.024, -0.098, 0.027, 0.018, 0.022, cr, cd, 0);                                          // muzzle
        Vox.box(0, hy + 0.010, -0.122, 0.013, 0.008, 0.004, dk, dk, 0);                                          // nose
        double pant = (Math.sin(age * 0.45) * 0.5 + 0.5) * 0.008;
        Vox.box(0, hy + 0.046 + pant * 0.5, -0.102, 0.009, 0.006 + pant, 0.006, pk, pk, 0);                      // tongue
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.025, hy - 0.006, -0.096, 0.007, 0.010, 0.002, dk, dk, 0);                              // eyes
            Vox.box(s * 0.027, hy - 0.010, -0.098, 0.003, 0.003, 0.001, c(0xFFFFFF), c(0xFFFFFF), 0);
            double sw = Math.sin(age * 0.18 + s) * 0.006;
            Vox.box(s * 0.060 + sw, hy + 0.004, -0.046, 0.010, 0.036, 0.020, ear, ear, 0);                       // floppy ears
        }
    }

    private static void axolotl(float age) {
        double ph = age * 0.22, hop = Math.abs(Math.sin(ph)) * 0.012;
        Vox.yo = -0.05 - hop;
        float[] pk = c(0xFFB3C8), dp = c(0xF08AA8), rd = c(0xE8527A);
        for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 2; i++)
            Vox.box(s * 0.062, 0.106, -0.040 + i * 0.080, 0.018, 0.016, 0.022, pk, dp, 3);                       // four little legs
        Vox.box(0, 0.064, 0.020, 0.058, 0.040, 0.092, pk, dp, 1);
        for (int i = 0; i < 5; i++) {                                                                            // flat tail fin
            double sw = Math.sin(age * 0.2 - i * 0.6) * 0.010 * (i + 1) * 0.5;
            Vox.box(sw, 0.072 + 0.004 * i, 0.128 + i * 0.030, 0.012, 0.030 - i * 0.003, 0.016, pk, dp, 4);
        }
        Vox.box(0, 0.050, 0.020, 0.010, 0.040, 0.080, rd, rd, 1);                                                // dorsal stripe
        double hy = 0.040;
        Vox.box(0, hy, -0.090, 0.074, 0.040, 0.044, pk, dp, 0);
        for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 3; i++) {
            double sw = Math.sin(age * 0.25 + i * 1.1 + s) * 0.010;
            Vox.box(s * (0.090 + i * 0.012), hy - 0.030 + i * 0.026 + sw, -0.086, 0.010 + 0.002 * (2 - i), 0.010, 0.008, rd, c(0xFF8FB0), 0);    // gill stalks
            Vox.box(s * (0.108 + i * 0.012), hy - 0.030 + i * 0.026 + sw, -0.086, 0.008, 0.008, 0.008, c(0xFF8FB0), c(0xFF8FB0), 0);
        }
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.038, hy - 0.010, -0.138, 0.010, 0.010, 0.003, Poly.INK, Poly.INK, 0);
        Vox.box(0, hy + 0.022, -0.136, 0.022, 0.004, 0.003, rd, rd, 0);
        Vox.box(-0.026, hy + 0.016, -0.136, 0.004, 0.004, 0.003, rd, rd, 0);
        Vox.box(0.026, hy + 0.016, -0.136, 0.004, 0.004, 0.003, rd, rd, 0);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.054, hy + 0.012, -0.134, 0.010, 0.006, 0.003, c(0xFF8FA8), c(0xFF8FA8), 0);   // cheeks
    }
}

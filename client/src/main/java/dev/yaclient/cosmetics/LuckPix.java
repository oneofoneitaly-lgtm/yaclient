package dev.yaclient.cosmetics;

/** Luck bundle: a plain emerald cape with a single animated four-leaf clover, plus the Lucky ladybug pet. */
final class LuckPix {
    private LuckPix() {}

    private static final int GN = 56;                       // clover grid resolution (smooth, not chunky)
    private static final double CX = 0.5, CY = 0.40, R = 0.27;   // clover centre (cape u,v) and half size in cape units

    private static float[] c(int hex, float e) { return Vox.rgb(hex, e); }

    /** Heart implicit value: <= 0 inside. Point is at the bottom (Y = -1), lobes on top. */
    private static double heart(double x, double y) {
        double a = x * x + y * y - 1;
        return a * a * a - x * x * y * y * y;
    }

    /** Colour of the clover at local point (x, y) in [-1, 1], or null outside. */
    private static float[] clover(double x, double y, float age) {
        double sway = Math.sin(age * 0.05) * 0.10;                           // gentle rocking
        double sc = 1.0 + 0.035 * Math.sin(age * 0.09);                      // breathing
        double cs = Math.cos(sway), sn = Math.sin(sway);
        double px = (x * cs + y * sn) / sc, py = (-x * sn + y * cs) / sc;
        // stem
        double st = Math.max(0, Math.min(1, (-py + 0.02) / 0.95));
        if (py < 0.02 && py > -0.98) {
            double sx = 0.10 * Math.sin(st * 2.6) + 0.02;
            if (Math.abs(px - sx) < 0.045 - st * 0.012) return Poly.mix(c(0x2FA84F, 0.2f), c(0x12602B, 0.1f), (float) st);
        }
        double best = -1e9; int bi = -1; double bx = 0, by = 0;
        for (int k = 0; k < 4; k++) {
            double ang = Math.PI / 4 + k * Math.PI / 2;
            double ca = Math.cos(ang), sa = Math.sin(ang);
            double along = px * ca + py * sa, across = -px * sa + py * ca;     // along: outward from the centre
            double X = across / 0.40, Y = (along - 0.52) / 0.40;
            double h = heart(X, Y);
            if (h <= 0 && -h > best) { best = -h; bi = k; bx = along; by = across; }
        }
        if (bi < 0) return null;
        // leaf shading: deeper emerald at the centre, bright mint outwards, dark midrib, gold rim
        float t = (float) Math.min(1.0, Math.max(0.0, (bx - 0.08) / 1.0));
        float[] col = Poly.mix(c(0x0F7A3A, 0.25f), c(0x6FE08A, 0.40f), t * 0.85f);
        if (Math.abs(by) < 0.028 + 0.02 * (1 - t) && bx > 0.1) col = Poly.mix(col, c(0x0A5A2A, 0.2f), 0.75f);   // midrib
        if (Math.cbrt(best) < 0.15) col = Poly.mix(col, c(0xFFD34A, 0.85f), 0.9f);                                        // gold rim
        // a light sweep running over the leaves
        double sweep = (px * 0.8 + py * 0.6) * 1.4 - ((age * 0.045) % 4.0 - 1.2) * 1.6;
        float sh = (float) Math.max(0, 1 - Math.abs(sweep) * 2.4);
        col = Poly.mix(col, c(0xF4FFE0, 0.95f), sh * 0.55f);
        return col;
    }

    private static void sparkle(double u0, double v0, double s, float[] col, float age, boolean plus) {
        for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++) {
            if (Math.abs(i) + Math.abs(j) > (s > 0.5 ? 1 : 0)) continue;
            double w = s * 0.013, ux = u0 + i * w / 0.66, vy = v0 + j * w;
            Poly.quad(CosmeticRender.capePt(ux - w / 1.32, vy - w / 2, age, plus), CosmeticRender.capePt(ux + w / 1.32, vy - w / 2, age, plus),
                    CosmeticRender.capePt(ux + w / 1.32, vy + w / 2, age, plus), CosmeticRender.capePt(ux - w / 1.32, vy + w / 2, age, plus), col, col, col, col, 1f);
        }
    }

    static void capeBase(Anime.Th t, float age, boolean plus) {
        float[] top = c(0xFFFFFF, 0.35f), bot = c(0xEEF1F0, 0.30f);          // plain white cape
        for (int i = 0; i < CosmeticRender.CAPE_C; i++) for (int j = 0; j < CosmeticRender.CAPE_R; j++) {
            double u0 = i / (double) CosmeticRender.CAPE_C, u1 = (i + 1) / (double) CosmeticRender.CAPE_C;
            double v0 = j / (double) CosmeticRender.CAPE_R, v1 = (j + 1) / (double) CosmeticRender.CAPE_R;
            float[] a = Poly.mix(top, bot, (float) v0), b = Poly.mix(top, bot, (float) v1);
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), a, a, b, b, 1f);
        }
        // the animated clover, drawn as a fine grid of small quads lifted a hair above the cape
        double hv = R * CosmeticRender.CAPE_W / CosmeticRender.CAPE_H;      // half height in v for a square clover
        for (int i = 0; i < GN; i++) for (int j = 0; j < GN; j++) {
            double xa = -1 + 2.0 * i / GN, xb = -1 + 2.0 * (i + 1) / GN, ya = 1 - 2.0 * j / GN, yb = 1 - 2.0 * (j + 1) / GN;
            float[] col = clover((xa + xb) / 2, (ya + yb) / 2, age);
            if (col == null) continue;
            double[][] q = new double[4][];
            double[][] xy = {{xa, ya}, {xb, ya}, {xb, yb}, {xa, yb}};
            for (int n = 0; n < 4; n++) {
                double[] p = CosmeticRender.capePt(Math.max(0, Math.min(1, CX + xy[n][0] * R / CosmeticRender.CAPE_W)),
                        Math.max(0, Math.min(1, CY - xy[n][1] * hv)), age, plus);
                p[2] += 0.004;
                q[n] = p;
            }
            Poly.quad(q[0], q[1], q[2], q[3], col, col, col, col, 1f);
        }
        // a few gold twinkles around it, each on its own beat
        float[] gold = c(0xFFE680, 1f);
        for (int k = 0; k < 4; k++) {
            double ph = (age * 0.04 + k * 0.25) % 1.0, s = Math.sin(ph * Math.PI);
            if (s < 0.25) continue;
            double a = k * 1.9 + 0.6;
            sparkle(CX + Math.cos(a) * 0.30, CY + Math.sin(a) * 0.20 * 0.66, s, gold, age, plus);
        }
    }

    // ------------------------------------------------------------------ wings: a fan of four clover leaves per wing
    private static void leaf(double rx, double ry, double deg, double len, double z, float age, int k) {
        double ang = Math.toRadians(deg + Math.sin(age * 0.16 - k * 0.7) * 3.0);
        double ca = Math.cos(ang), sa = Math.sin(ang);
        final int G = 44;
        double ext = len * 1.0;                                  // leaf spans 0..len along its axis, ~0.9 len wide
        for (int i = 0; i < G; i++) for (int j = 0; j < G; j++) {
            double[] u0 = {-0.5 + i / (double) G, (j) / (double) G}, u1 = {-0.5 + (i + 1) / (double) G, (j + 1) / (double) G};
            double ma = (u0[1] + u1[1]) / 2, mc = (u0[0] + u1[0]) / 2;       // along 0..1, across -0.5..0.5 (cell centre)
            double X = mc * ext / (0.31 * len), Y = (ma * ext - 0.52 * len) / (0.40 * len);
            double h = heart(X, Y);
            if (h > 0) continue;
            float t = (float) ma;
            float[] col = Poly.mix(c(0x0F7A3A, 0.25f), c(0x6FE08A, 0.40f), t * 0.85f);
            if (Math.abs(mc) * ext < 0.012 + 0.012 * (1 - t) && ma > 0.1) col = Poly.mix(col, c(0x0A5A2A, 0.2f), 0.75f);      // midrib
            if (Math.cbrt(-h) < 0.17) col = Poly.mix(col, c(0xFFD34A, 0.85f), 0.85f);                                          // gold rim
            double[][] q = new double[4][];
            double[][] cs = {{u0[0], u0[1]}, {u1[0], u0[1]}, {u1[0], u1[1]}, {u0[0], u1[1]}};
            for (int n = 0; n < 4; n++) {
                double al = cs[n][1] * ext, ac = cs[n][0] * ext;
                q[n] = Poly.p(rx + ca * al - sa * ac, ry + sa * al + ca * ac, z);
            }
            Poly.quad(q[0], q[1], q[2], q[3], col, col, col, col, 1f);
        }
    }

    static void wing(Anime.Th t, float age) {
        // far leaves first so the near, bigger ones sit on top
        leaf(0.03, 0.04, -34, 0.36, 0.000, age, 0);
        leaf(0.03, 0.04, -4, 0.56, 0.003, age, 1);
        leaf(0.03, 0.04, 26, 0.64, 0.006, age, 2);
        leaf(0.03, 0.04, 56, 0.58, 0.009, age, 3);
        leaf(0.03, 0.04, 86, 0.42, 0.012, age, 4);
    }

    // ------------------------------------------------------------------ pet: Lucky the ladybug
    static void pet(Anime.Th t, float age) {
        Vox.TEXEL = 0.0185; Vox.reset();
        double ph = age * 0.26;
        double hop = Math.abs(Math.sin(ph)) * 0.014 + Math.sin(age * 0.07) * 0.004;
        Vox.yo = -0.05 - hop;
        final float[] red = c(0xE0262B, 0f), dred = c(0xA81A20, 0f), blk = c(0x1B1B22, 0f);
        // legs
        for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 3; i++) {
            double lift = Math.max(0, Math.sin(ph + i * 2.1 + (s > 0 ? 1.5 : 0))) * 0.012;
            Vox.box(s * 0.082, 0.096 - lift, -0.040 + i * 0.040, 0.020, 0.006, 0.008, blk, blk, 3);
            Vox.box(s * 0.100, 0.108 - lift, -0.040 + i * 0.040, 0.007, 0.014, 0.008, blk, blk, 3);
        }
        // shell with spots and the centre line; slightly opened wing covers flutter
        final double flut = Math.sin(age * 0.5) * 0.004;
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                if (Math.abs(rx) < 0.07) return blk;                                    // centre line
                int sx = rx < 0 ? -1 : 1;
                double[][] sp = {{0.52, -0.38}, {0.52, 0.34}, {0.20, -0.02}};          // spot positions (rx, rz)
                for (double[] q : sp) { double dx = Math.abs(rx) - q[0], dz = rz - q[1]; if (dx * dx + dz * dz < 0.075) return blk; }
                return null;
            }
        };
        Vox.box(0, 0.060, 0.012, 0.080, 0.040, 0.072, red, dred, 1);
        Vox.marker = null;
        Vox.box(-0.050, 0.020 - flut, 0.012, 0.038, 0.008, 0.078, c(0xFF5A5A, 0.15f), red, 1);   // lifted cover highlight
        Vox.box(0.050, 0.020 + flut, 0.012, 0.038, 0.008, 0.078, c(0xFF5A5A, 0.15f), red, 1);
        // head: black with white eyes, a tiny gold clover dot between the antennae
        Vox.box(0, 0.052, -0.090, 0.056, 0.034, 0.030, blk, blk, 0);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.030, 0.042, -0.122, 0.014, 0.014, 0.004, c(0xFFFFFF, 0.6f), c(0xFFFFFF, 0.6f), 0);
            Vox.box(s * 0.030, 0.044, -0.127, 0.006, 0.008, 0.002, blk, blk, 0);
            double sw = Math.sin(age * 0.18 + s) * 0.008;
            Vox.box(s * 0.030 + sw, 0.010, -0.108, 0.005, 0.026, 0.005, blk, blk, 0);
            Vox.box(s * 0.034 + sw * 1.4, -0.020, -0.112, 0.011, 0.011, 0.011, c(0xFFD34A, 0.8f), c(0xFFD34A, 0.8f), 0);
        }
        Vox.TEXEL = 0.0125;
    }
}

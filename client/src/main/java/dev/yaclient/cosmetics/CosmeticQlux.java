package dev.yaclient.cosmetics;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Geometry of the "Qlux" partner bundle: white and pearl, ice-blue light. Smooth 3D shapes only (no pixel
 * textures): white feather wings (CosmeticRender with the qlux palette), a satin cape with an animated
 * wordmark, a white band and a floating light spirit.
 */
final class CosmeticQlux {
    private CosmeticQlux() {}

    private static float smooth(float t) { t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }

    // ------------------------------------------------------------------ cape
    private static float[] capeColor(double u, double v, float age) {
        float[] c = Poly.mix(Poly.SNOW, Poly.PEARL, (float) (v * 0.55));
        // soft vertical satin folds
        double fold = 0.5 + 0.5 * Math.sin(u * Math.PI * 6 + v * 1.5);
        c = Poly.mix(c, Poly.SNOW, (float) (fold * 0.25));
        // silver trim along the edge and an ice-blue glow running along the hem
        double edge = Math.min(Math.min(u, 1 - u) * CosmeticRender.CAPE_W / 0.028, (1 - v) / 0.034);
        float trim = smooth((float) (1.0 - edge));
        c = Poly.mix(c, Poly.SILVER, trim * 0.9f);
        double hem = Math.sin(u * 9.0 - age * 0.07) * 0.5 + 0.5;
        float glow = smooth((float) ((v - 0.90) / 0.10)) * (0.35f + 0.65f * (float) hem);
        c = Poly.mix(c, Poly.ICE, glow * 0.75f);
        c[3] = Math.max(c[3], 0.25f + glow * 0.6f);
        return c;
    }

    static void capeBase(float age, boolean plus) {
        for (int i = 0; i < CosmeticRender.CAPE_C; i++) {
            for (int j = 0; j < CosmeticRender.CAPE_R; j++) {
                double u0 = i / (double) CosmeticRender.CAPE_C, u1 = (i + 1) / (double) CosmeticRender.CAPE_C;
                double v0 = j / (double) CosmeticRender.CAPE_R, v1 = (j + 1) / (double) CosmeticRender.CAPE_R;
                Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                        CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus),
                        capeColor(u0, v0, age), capeColor(u1, v0, age), capeColor(u1, v1, age), capeColor(u0, v1, age), 1f);
            }
        }
    }

    // wordmark placement on the cape
    private static final double TU0 = 0.14, TU1 = 0.86;
    private static final double TH = (TU1 - TU0) * CosmeticRender.CAPE_W / CosmeticRender.CAPE_H * QluxText.H / (double) QluxText.W;
    private static final double TV0 = 0.5 - TH / 2, TV1 = 0.5 + TH / 2;   // centered on the cape

    /** The wordmark: black letters that gently wave, with a soft grey sheen sweeping through them. */
    static void text(float age, boolean plus) {
        final int NX = 28, NY = 10;
        double sweep = ((age * 0.018) % 1.6) - 0.3;                 // light position in u-space, wraps around
        for (int i = 0; i < NX; i++) {
            for (int j = 0; j < NY; j++) {
                double a0 = i / (double) NX, a1 = (i + 1) / (double) NX, b0 = j / (double) NY, b1 = (j + 1) / (double) NY;
                tv(a0, b0, age, plus, sweep);
                tv(a1, b0, age, plus, sweep);
                tv(a1, b1, age, plus, sweep);
                tv(a0, b1, age, plus, sweep);
            }
        }
    }

    private static void tv(double a, double b, float age, boolean plus, double sweep) {
        double wave = Math.sin(age * 0.09 + a * 7.0) * 0.006 * (1.0 - b * 0.4);   // letters bob like a ribbon
        double u = TU0 + (TU1 - TU0) * a, v = TV0 + (TV1 - TV0) * b + wave;
        double[] p = CosmeticRender.capePt(Math.max(0, Math.min(1, u)), Math.max(0, Math.min(1, v)), age, plus);
        float d = (float) Math.abs(a - sweep);
        float lit = (float) Math.exp(-d * d * 60.0);                 // soft light band
        float pulse = 0.5f + 0.5f * (float) Math.sin(age * 0.05);
        float r = 0.01f + 0.20f * lit + 0.01f * pulse;
        float g = 0.01f + 0.20f * lit + 0.01f * pulse;
        float bl = 0.01f + 0.22f * lit + 0.01f * pulse;
        Poly.tex(p[0], p[1], p[2] + 0.005, (float) a, (float) b, r, g, bl, 1f);
    }

    /** Whole cape: satin base plus the animated wordmark (own textured pass). */
    static void drawCape(float age, boolean plus) {
        Poly.begin();
        capeBase(age, plus);
        Poly.end();
        GlStateManager.enableTexture2D();
        QluxText.bind();
        Poly.beginTex();
        text(age, plus);
        Poly.end();
        GlStateManager.disableTexture2D();
    }

    // ------------------------------------------------------------------ pet: "Lumi", a little light fox-cat
    private static final double YO0 = -0.045;     // lifts the whole pet so the paws hover at the right height
    private static double YO = YO0;                // YO0 plus the current hop

    private static final double TEXEL = 0.0125;   // one "pixel" of the pet's fur texture
    private static int boxSeed = 0;
    private static int PART = 0;   // 0 body, 1 head, 2 snout, 3 leg, 4 tail, 5 ear: picks the pixel-art markings

    /** Hand-placed pixel markings (fur patches, face mask, socks) by part; coordinates are relative to the box (-1..1). */
    private static float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
        double ax = Math.abs(rx), ay = Math.abs(ry);
        switch (part) {
            case 1:   // head
                if (a == 2 && sa < 0) {
                    if (ry < -0.55 && ax < 0.16) return Poly.ICE;                                // forehead mark
                    if (ry < -0.40 && ax < 0.34 && ax > 0.20) return Poly.SILVER;               // brow
                    if (ry > 0.05 && ry < 0.50 && ax > 0.50 && ax < 0.66) return Poly.SILVER;   // tear streaks
                    if (ry > 0.40 && ax > 0.70) return Poly.PEARL;                              // cheek fluff
                    if (ry > 0.62) return Poly.PEARL;
                }
                if (a == 1 && sa < 0 && ax < 0.13) return Poly.SILVER;                           // top stripe
                if (a == 1 && sa < 0 && ax < 0.30 && rz > 0.2) return Poly.PEARL;
                if (a == 0 && rz > 0.35 && ry > -0.3) return Poly.PEARL;                         // side fluff
                return null;
            case 0:   // body
                if (a == 1 && sa < 0 && ax < 0.22) return ((int) Math.round(rz * 5)) % 2 == 0 ? Poly.SILVER : Poly.PEARL;
                if (a == 0 && ry > 0.35) return Poly.PEARL;                                      // belly fade
                if (a == 0 && ry > -0.5 && ry < 0.2 && (((int) Math.round(rz * 4)) + ((int) Math.round(ry * 4))) % 3 == 0) return Poly.PEARL;
                return null;
            case 3:   // leg: pearl socks
                if (ry > 0.30) return Poly.PEARL;
                return null;
            case 4:   // tail rings
                if (Math.abs(rz) > 0.45) return Poly.ICE;
                return null;
            case 5:   // ear
                if (ry < -0.55) return Poly.PEARL;
                return null;
            default: return null;
        }
    }

    private static float hash(int a, int b, int c, int d) {
        long x = (a * 73856093L) ^ (b * 19349663L) ^ (c * 83492791L) ^ (d * 374761393L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    /** Minecraft-style box whose faces are tiled with pixel cells (each a flat colour with a little fur noise). */
    private static void box(double cx, double cy, double cz, double hx, double hy, double hz, double bev, float[] top, float[] bot) {
        cy += YO;
        int id = ++boxSeed;
        double[] h = {hx, hy, hz}, c = {cx, cy, cz};
        for (int a = 0; a < 3; a++) for (int sa = -1; sa <= 1; sa += 2) {
            int b = (a + 1) % 3, d = (a + 2) % 3;
            int nb = Math.max(1, (int) Math.round(2 * h[b] / TEXEL)), nd = Math.max(1, (int) Math.round(2 * h[d] / TEXEL));
            for (int i = 0; i < nb; i++) for (int j = 0; j < nd; j++) {
                double[][] q = new double[4][];
                int[][] uv = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
                for (int n = 0; n < 4; n++) {
                    double[] p = new double[3];
                    p[a] = c[a] + sa * h[a];
                    p[b] = c[b] - h[b] + 2 * h[b] * (i + uv[n][0]) / nb;
                    p[d] = c[d] - h[d] + 2 * h[d] * (j + uv[n][1]) / nd;
                    q[n] = p;
                }
                double ym = (q[0][1] + q[2][1]) * 0.5;
                float[] base = Poly.mix(top, bot, (float) Math.max(0, Math.min(1, (ym - (cy - h[1])) / (2 * h[1]))));
                double[] cc = {(q[0][0] + q[2][0]) * 0.5, (q[0][1] + q[2][1]) * 0.5, (q[0][2] + q[2][2]) * 0.5};
                float[] mk = nb * nd > 6 ? mark(PART, a, sa, (cc[0] - cx) / hx, (cc[1] - cy) / hy, (cc[2] - cz) / hz) : null;
                if (mk != null) base = Poly.mix(base, mk, mk == Poly.ICE ? 0.85f : 0.8f);
                float r = hash(id, a * 2 + (sa > 0 ? 1 : 0), i, j);
                float k = 0.94f + 0.08f * r;                                   // fur noise
                float[] col = {Math.min(1f, base[0] * k), Math.min(1f, base[1] * k), Math.min(1f, base[2] * k), base[3]};
                if (r > 0.985f && nb * nd > 6) col = new float[]{Poly.ICE[0] * 0.9f + 0.1f, Poly.ICE[1] * 0.9f + 0.1f, Poly.ICE[2], 0.5f};   // sparkle pixel
                else if (r < 0.04f && nb * nd > 6) col = new float[]{base[0] * 0.80f, base[1] * 0.82f, base[2] * 0.90f, base[3]};          // shadow pixel
                if (sa < 0) Poly.quad(q[0], q[1], q[2], q[3], col, col, col, col, 1f);
                else Poly.quad(q[3], q[2], q[1], q[0], col, col, col, col, 1f);
            }
        }
    }

    static void pet(float age) {
        float pulse = 0.85f + 0.15f * (float) Math.sin(age * 0.2);
        double ph = age * 0.32;                                   // trot cycle
        double hop = Math.abs(Math.sin(ph)) * 0.016 + Math.sin(age * 0.07) * 0.006;   // bounces up and down
        YO = YO0 - hop;
        double bob = Math.sin(ph * 2) * 0.004;
        // body, with a pearl belly/chest and a silver stripe along the back
        PART = 0;
        box(0, 0.02, 0.045, 0.068, 0.062, 0.115, 0.016, Poly.SNOW, Poly.PEARL);
        PART = 2;
        box(0, 0.045, -0.052, 0.042, 0.036, 0.020, 0.008, Poly.PEARL, Poly.PEARL);
        box(0, -0.046, 0.05, 0.026, 0.006, 0.095, 0.004, Poly.SILVER, Poly.SILVER);
        // legs with pearl paws (small idle step)
        PART = 3;
        for (int s = -1; s <= 1; s += 2) {
            for (int f = 0; f < 2; f++) {
                double lp = ph + (f + (s > 0 ? 1 : 0)) * Math.PI;           // diagonal pairs move together
                double z = f == 0 ? -0.045 : 0.135, step = Math.sin(lp) * 0.026, lift = Math.max(0, Math.cos(lp)) * 0.016;
                box(s * 0.046, 0.108 - lift, z + step, 0.024, 0.034, 0.024, 0.008, Poly.SNOW, Poly.PEARL);
                box(s * 0.046, 0.140 - lift, z + step - 0.003, 0.026, 0.008, 0.028, 0.004, Poly.PEARL, Poly.SILVER);
            }
        }
        // head (cube) with snout, nose, ears, eyes
        double hz = -0.115, hy = -0.072 + bob;
        PART = 1;
        box(0, hy, hz, 0.076, 0.066, 0.066, 0.016, Poly.SNOW, Poly.SNOW);
        PART = 2;
        box(0, hy + 0.030, hz - 0.074, 0.036, 0.026, 0.026, 0.008, Poly.PEARL, Poly.PEARL);
        box(0, hy + 0.012, hz - 0.101, 0.012, 0.008, 0.004, 0.003, Poly.INK, Poly.INK);
        for (int s = -1; s <= 1; s += 2) {
            PART = 5;
            box(s * 0.052, hy - 0.090, hz + 0.010, 0.022, 0.032, 0.014, 0.006, Poly.SNOW, Poly.PEARL);            // ear
            PART = 2;
            box(s * 0.052, hy - 0.086, hz - 0.003, 0.012, 0.020, 0.003, 0.002, Poly.ICE, Poly.ICE);               // inner ear
            box(s * 0.040, hy - 0.006, hz - 0.068, 0.014, 0.016, 0.004, 0.003, Poly.ICE, Poly.ICE);               // glowing eyes
            box(s * 0.040, hy - 0.006, hz - 0.071, 0.007, 0.010, 0.002, 0.001, Poly.INK, Poly.INK);              // pupils
            box(s * 0.060, hy + 0.030, hz - 0.060, 0.010, 0.006, 0.006, 0.002, Poly.ICE, Poly.ICE);               // cheek glow
        }
        // collar with a star charm
        box(0, -0.012, -0.060, 0.074, 0.014, 0.020, 0.006, Poly.ICE, Poly.ICE);
        Poly.ellipsoid(0, 0.016 + YO, -0.082, 0.012 * pulse, 0.012 * pulse, 0.006, Poly.SNOW, Poly.ICE, 1f);
        // halo ring above the head
        final int n = 28;
        double haloY = -0.205 + YO + Math.sin(age * 0.12) * 0.006;
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            double r0 = 0.072, r1 = 0.088;
            double[] p0 = Poly.p(Math.cos(a0) * r0, haloY, hz + Math.sin(a0) * r0), p1 = Poly.p(Math.cos(a1) * r0, haloY, hz + Math.sin(a1) * r0);
            double[] q1 = Poly.p(Math.cos(a1) * r1, haloY, hz + Math.sin(a1) * r1), q0 = Poly.p(Math.cos(a0) * r1, haloY, hz + Math.sin(a0) * r1);
            float[] hc = Poly.mix(Poly.ICE, Poly.SNOW, (float) (0.5 + 0.5 * Math.sin(a0 * 2 - age * 0.25)));
            Poly.quad(p0, p1, q1, q0, hc, hc, hc, hc, 1f);
        }
        // fluffy tail: stacked cubes curving up, ending in a glowing ice-blue star
        final int T = 5;
        for (int i = 0; i < T; i++) {
            double f = i / (double) (T - 1);
            double x = Math.sin(age * 0.14 - i * 0.7) * 0.035 * f * 2.0;
            double y = 0.0 - 0.11 * f * f - 0.02 * f;
            double z = 0.17 + 0.14 * f;
            double r = 0.034 * (1 - f * 0.45);
            PART = 4;
            if (i < T - 1) box(x, y, z, r, r, r * 1.35, 0.009, Poly.SNOW, i == T - 2 ? Poly.ICE : Poly.PEARL);
            else Poly.ellipsoid(x, y + YO - 0.012, z, 0.030 * pulse, 0.030 * pulse, 0.030 * pulse, Poly.ICE, Poly.SNOW, 1f);
        }
        // tiny feathered wings: three overlapping feathers each, flapping
        double flap = Math.sin(age * 0.40) * 0.12;
        for (int s = -1; s <= 1; s += 2) {
            for (int k = 0; k < 3; k++) {
                double ang = -0.55 + k * 0.42 - flap;                 // fan angle, radians
                double len = 0.17 - k * 0.03;
                double rx = s * 0.062, ry = -0.025 + YO + k * 0.012, rz = 0.06 + k * 0.012;
                double tx = rx + s * Math.cos(ang) * len, ty = ry + Math.sin(ang) * len * 1.0, tz = rz + 0.03;
                double w = 0.075;
                float[] tip = k == 0 ? Poly.ICE : Poly.PEARL;
                Poly.quad(Poly.p(rx, ry - w * 0.4, rz), Poly.p(rx, ry + w * 0.4, rz), Poly.p(tx, ty + w * 0.30, tz), Poly.p(tx, ty - w * 0.30, tz),
                          Poly.SNOW, Poly.SNOW, tip, tip, 1f);
                Poly.tri(Poly.p(tx, ty - w * 0.25, tz), Poly.p(tx, ty + w * 0.25, tz), Poly.p(tx + s * 0.03, ty + Math.sin(ang) * 0.03, tz + 0.01), tip, tip, Poly.ICE, 1f);
            }
        }
    }
}

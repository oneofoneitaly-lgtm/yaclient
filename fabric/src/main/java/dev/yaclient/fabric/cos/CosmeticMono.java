package dev.yaclient.fabric.cos;

/**
 * Geometry of the "Monochrome" bundle: classic black feather wings, a black cape with an infinity symbol that
 * draws itself in white, and a spinning yin-yang pet. Same conventions as CosmeticRender
 * (model space, y points DOWN, back of the player is +z; wings are built in a plane: x out, y up).
 */
final class CosmeticMono {
    private CosmeticMono() {}

    // ------------------------------------------------------------------ wings
    // Black dragon wings: a scaled membrane stretched between finger bones. Built in a plane (x out, y up).
    private static double[] pt(double x, double y, double z) { return Poly.p(x, y, z); }

    /** Membrane triangle, subdivided so every little cell can carry a scale tint. */
    private static void scaled(double[] a, double[] b, double[] c, int seed) {
        final int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - i; j++) {
                double[] p00 = bary(a, b, c, i, j, n), p10 = bary(a, b, c, i + 1, j, n), p01 = bary(a, b, c, i, j + 1, n);
                Poly.tri(p00, p10, p01, scale(i, j, seed), scale(i + 1, j, seed), scale(i, j + 1, seed), 1f);
                if (j < n - i - 1) {
                    double[] p11 = bary(a, b, c, i + 1, j + 1, n);
                    Poly.tri(p10, p11, p01, scale(i + 1, j, seed), scale(i + 1, j + 1, seed), scale(i, j + 1, seed), 1f);
                }
            }
        }
    }

    private static double[] bary(double[] a, double[] b, double[] c, int i, int j, int n) {
        double u = i / (double) n, v = j / (double) n, w = 1 - u - v;
        double billow = 0.02 * 27.0 * u * v * w;     // the membrane bows backwards between the bones (3D volume)
        return pt(a[0] * w + b[0] * u + c[0] * v, a[1] * w + b[1] * u + c[1] * v, a[2] * w + b[2] * u + c[2] * v + billow);
    }

    private static float[] scale(int i, int j, int seed) {
        float k = ((i * 3 + j * 5 + seed) % 4) / 3f;          // staggered scale pattern
        return Poly.mix(Poly.INK, Poly.CHAR, 0.20f + 0.55f * k);
    }

    private static double[] sub(double[] a, double[] b) { return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    private static double[] cross(double[] a, double[] b) { return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }
    private static double[] norm(double[] a) { double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]); return new double[]{a[0] / l, a[1] / l, a[2] / l}; }
    private static double[] off(double[] p, double[] n, double kn, double[] m, double km) {
        return pt(p[0] + n[0] * kn + m[0] * km, p[1] + n[1] * kn + m[1] * km, p[2] + n[2] * kn + m[2] * km);
    }

    /** A bone: a tapered 4-sided prism from p to q (real thickness, so the wing reads as a 3D object). */
    private static void bone(double[] p, double[] q, double w0, double w1, float[] col, float[] edge) {
        double[] d = sub(q, p);
        double[] n = norm(cross(d, new double[]{0, 0, 1}));
        double[] m = norm(cross(d, n));
        double d0 = w0 * 0.8, d1 = w1 * 0.8;
        double[][] P = {off(p, n, w0, m, d0), off(p, n, -w0, m, d0), off(p, n, -w0, m, -d0), off(p, n, w0, m, -d0)};
        double[][] Q = {off(q, n, w1, m, d1), off(q, n, -w1, m, d1), off(q, n, -w1, m, -d1), off(q, n, w1, m, -d1)};
        for (int i = 0; i < 4; i++) {
            int k = (i + 1) % 4;
            float[] c = (i == 0 || i == 2) ? edge : col;
            Poly.quad(P[i], P[k], Q[k], Q[i], c, c, c, c, 1f);
        }
        Poly.quad(P[0], P[1], P[2], P[3], col, col, col, col, 1f);   // base cap (hides the joint seams)
    }

    /** One wing in its own plane (x out, y up). The caller mirrors it for the other side.
     *  Shape (from the reference model): a thick forearm from the body up and out to the joint, a long spike
     *  rising from the joint, and a membrane hanging below with three bone fingers and a scalloped edge. */
    static void wing(float age) {
        double fl = Math.sin(age * 0.10) * 0.02;
        double[] A = pt(0.02, 0.0, 0), J = pt(0.52, 0.32, 0);
        double[] K = pt(0.68 + fl, 0.90, 0);                       // spike tip
        double[] F1 = pt(0.58 + fl, -0.46, 0), F2 = pt(0.33 + fl * 0.5, -0.44, 0), F3 = pt(0.08, -0.30, 0);
        double[] M1 = pt((F1[0] + F2[0]) / 2, (F1[1] + F2[1]) / 2 + 0.07, (F1[2] + F2[2]) / 2);
        double[] M2 = pt((F2[0] + F3[0]) / 2, (F2[1] + F3[1]) / 2 + 0.06, (F2[2] + F3[2]) / 2);
        double[] Mo = pt(J[0] + 0.14, J[1] - 0.44, 0);              // outer edge between spike and first finger
        scaled(J, F1, M1, 0); scaled(J, M1, F2, 1);
        scaled(J, F2, M2, 2); scaled(J, M2, F3, 3);
        scaled(J, F3, A, 0);
        scaled(J, K, Mo, 1);
        scaled(J, Mo, F1, 2);
        float[] bone = Poly.INK, edge = Poly.CHAR;
        bone(A, J, 0.050, 0.040, bone, edge);                       // forearm
        bone(J, K, 0.040, 0.012, bone, edge);                       // spike
        bone(J, F1, 0.032, 0.012, bone, edge);
        bone(J, F2, 0.026, 0.010, bone, edge);
        bone(J, F3, 0.020, 0.010, bone, edge);
    }

    // ------------------------------------------------------------------ cape
    private static float[] capeColor(double u, double v) {
        float[] c = Poly.mix(Poly.INK, new float[]{0.055f, 0.055f, 0.07f, 0f}, (float) (v * 0.7));
        double edge = Math.min(Math.min(u, 1 - u) * CosmeticRender.CAPE_W / 0.022, (1 - v) / 0.026);
        float trim = 0f;   // cape stays fully black; only the infinity symbol is white
        trim = trim * trim * (3 - 2 * trim);
        float[] o = Poly.mix(c, Poly.WHITE, trim * 0.85f);
        o[3] = Math.max(o[3], trim * 0.7f);
        return o;
    }

    static void capeBase(float age, boolean plus) {
        for (int i = 0; i < CosmeticRender.CAPE_C; i++) {
            for (int j = 0; j < CosmeticRender.CAPE_R; j++) {
                double u0 = i / (double) CosmeticRender.CAPE_C, u1 = (i + 1) / (double) CosmeticRender.CAPE_C;
                double v0 = j / (double) CosmeticRender.CAPE_R, v1 = (j + 1) / (double) CosmeticRender.CAPE_R;
                Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                        CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus),
                        capeColor(u0, v0), capeColor(u1, v0), capeColor(u1, v1), capeColor(u0, v1), 1f);
            }
        }
    }

    private static double[] onCape(double x, double y, float age, boolean plus) {
        double v = 0.40 + y / CosmeticRender.CAPE_H;
        double u = 0.5 + x / (CosmeticRender.CAPE_W * (1 + 0.06 * v));
        double[] p = CosmeticRender.capePt(u, v, age, plus);
        p[2] += 0.004;
        return p;
    }

    /** A white infinity symbol (lemniscate); a bright comet runs along it, so it keeps drawing itself. */
    static void emblem(float age, boolean plus) {
        final int n = 96;
        final double a = 0.30, tau = Math.PI * 2;
        double head = (age * 0.045) % tau;
        double[] x = new double[n + 1], y = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            double t = tau * i / n, s = Math.sin(t), c = Math.cos(t), d = 1 + s * s;
            x[i] = a * c / d;
            y[i] = a * 1.15 * s * c / d;
        }
        for (int i = 0; i < n; i++) {
            double t = tau * i / n;
            double dt = ((head - t) % tau + tau) % tau;
            float b = (float) (0.24 + 0.76 * Math.exp(-dt * 1.0));
            double w = 0.021 * (1.0 + 0.9 * Math.exp(-dt * 7.0));
            double[][] l = new double[2][], r = new double[2][];
            for (int k = 0; k < 2; k++) {
                int j = i + k, j0 = Math.max(0, j - 1), j1 = Math.min(n, j + 1);
                double tx = x[j1] - x[j0], ty = y[j1] - y[j0], tl = Math.hypot(tx, ty);
                double nx = -ty / tl, ny = tx / tl;
                l[k] = onCape(x[j] + nx * w, y[j] + ny * w, age, plus);
                r[k] = onCape(x[j] - nx * w, y[j] - ny * w, age, plus);
            }
            float[] col = {b, b, b, 1f};
            Poly.quad(l[0], r[0], r[1], l[1], col, col, col, col, 1f);
        }
    }

    // ------------------------------------------------------------------ pet
    private static void fan(double cx, double cy, double r, double a0, double a1, float[] col, double z) {
        final int n = Math.max(6, (int) Math.round(48 * Math.abs(a1 - a0) / (Math.PI * 2)));
        double[] c = Poly.p(cx, cy, z);
        for (int i = 0; i < n; i++) {
            double s0 = a0 + (a1 - a0) * i / n, s1 = a0 + (a1 - a0) * (i + 1) / n;
            Poly.tri(c, Poly.p(cx + Math.cos(s0) * r, cy + Math.sin(s0) * r, z), Poly.p(cx + Math.cos(s1) * r, cy + Math.sin(s1) * r, z), col, col, col, 1f);
        }
    }

    private static final float[] PW = {0.96f, 0.96f, 0.98f, 0.8f}, PB = {0.025f, 0.025f, 0.03f, 0.8f};

    /** The yin-yang (symbol of opposites) as a flat disc; the caller spins it around the vertical axis. */
    static void pet(float age) {
        final double r = 0.16, e = 0.0016;
        for (int side = -1; side <= 1; side += 2) {
            if (side == 1) fan(0, 0, r, 0, Math.PI * 2, PW, 0);
            fan(0, 0, r, -Math.PI / 2, Math.PI / 2, PB, side * e);
            fan(0, -r / 2, r / 2, 0, Math.PI * 2, PB, side * e * 2);
            fan(0, r / 2, r / 2, 0, Math.PI * 2, PW, side * e * 2);
            fan(0, -r / 2, r * 0.14, 0, Math.PI * 2, PW, side * e * 3);
            fan(0, r / 2, r * 0.14, 0, Math.PI * 2, PB, side * e * 3);
        }
        // thin rim so it reads on any background
        final int n = 48;
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            for (int side = -1; side <= 1; side += 2) {
                double z = side * e * 4, r1 = r + 0.007;
                Poly.quad(Poly.p(Math.cos(a0) * r, Math.sin(a0) * r, z), Poly.p(Math.cos(a1) * r, Math.sin(a1) * r, z),
                        Poly.p(Math.cos(a1) * r1, Math.sin(a1) * r1, z), Poly.p(Math.cos(a0) * r1, Math.sin(a0) * r1, z), PW, PW, PW, PW, 1f);
            }
        }
    }
}

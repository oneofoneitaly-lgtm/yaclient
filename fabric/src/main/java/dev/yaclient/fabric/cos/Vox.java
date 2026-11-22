package dev.yaclient.fabric.cos;

/** Minecraft-style pixel boxes: every face is tiled with texel-sized cells (flat colour + fur noise + hand-placed markings). */
final class Vox {
    private Vox() {}

    interface Marker { float[] mark(int part, int axis, int sign, double rx, double ry, double rz); }

    static double TEXEL = 0.0125;
    static double yo = 0;          // vertical offset applied to every box
    static Marker marker = null;
    private static int seed = 0;

    private static float hash(int a, int b, int c, int d) {
        long x = (a * 73856093L) ^ (b * 19349663L) ^ (c * 83492791L) ^ (d * 374761393L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    static float[] rgb(int hex, float emissive) {
        return new float[]{((hex >> 16) & 255) / 255f, ((hex >> 8) & 255) / 255f, (hex & 255) / 255f, emissive};
    }

    static void reset() { seed = 0; }

    static void box(double cx, double cy, double cz, double hx, double hy, double hz, float[] top, float[] bot, int part) {
        cy += yo;
        int id = ++seed;
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
                double mx = (q[0][0] + q[2][0]) * 0.5, mz = (q[0][2] + q[2][2]) * 0.5;
                boolean big = nb * nd > 6;
                float[] mk = big && marker != null ? marker.mark(part, a, sa, (mx - cx) / hx, (ym - cy) / hy, (mz - cz) / hz) : null;
                if (mk != null) base = Poly.mix(base, mk, 0.85f);
                float r = hash(id, a * 2 + (sa > 0 ? 1 : 0), i, j);
                float k = 0.94f + 0.08f * r;
                float[] col = {Math.min(1f, base[0] * k), Math.min(1f, base[1] * k), Math.min(1f, base[2] * k), base[3]};
                if (big && mk == null && r < 0.04f) col = new float[]{base[0] * 0.8f, base[1] * 0.82f, base[2] * 0.9f, base[3]};
                if (sa < 0) Poly.quad(q[0], q[1], q[2], q[3], col, col, col, col, 1f);
                else Poly.quad(q[3], q[2], q[1], q[0], col, col, col, col, 1f);
            }
        }
    }
}

package dev.yaclient.fabric.cos;


/**
 * Small smooth-shaded geometry helpers (vertex colors, no texture) used by the cosmetics.
 * Colors are float[]{r, g, b, emissive}: emissive 0 = fully affected by the light shading, 1 = glows.
 */
final class Poly {
    private Poly() {}

    // light direction in model space (y points down), from above/front
    private static final double LX = 0.30, LY = -0.78, LZ = -0.55;
    private static final double LL = Math.sqrt(LX * LX + LY * LY + LZ * LZ);

    static final float[] BLACK = {0.045f, 0.045f, 0.06f, 0f};
    static final float[] DARK = {0.10f, 0.03f, 0.04f, 0f};
    static final float[] DEEP = {0.45f, 0.02f, 0.02f, 0f};
    static final float[] RED = {0.88f, 0.03f, 0.02f, 0.25f};
    static final float[] HOT = {1.00f, 0.28f, 0.16f, 0.8f};
    // black & white bundle
    static final float[] INK = {0.015f, 0.015f, 0.020f, 0f};
    static final float[] CHAR = {0.17f, 0.17f, 0.20f, 0.15f};
    static final float[] WHITE = {0.96f, 0.96f, 0.98f, 0.75f};
    // Qlux (white) bundle
    static final float[] SNOW = {0.98f, 0.98f, 1.00f, 0.40f};
    static final float[] PEARL = {0.80f, 0.83f, 0.92f, 0.10f};
    static final float[] ICE = {0.52f, 0.80f, 1.00f, 0.85f};
    static final float[] SILVER = {0.70f, 0.74f, 0.84f, 0.30f};
    static final float[] GREY = {0.52f, 0.52f, 0.56f, 0.4f};

    static float[] mix(float[] a, float[] b, float t) {
        t = t < 0 ? 0 : (t > 1 ? 1 : t);
        return new float[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t, a[3] + (b[3] - a[3]) * t};
    }

    private static final java.util.ArrayList<float[]> BUF = new java.util.ArrayList<float[]>();
    private static boolean textured;
    private static int texId;

    static void begin() { BUF.clear(); textured = false; }

    static void beginTex() { BUF.clear(); textured = true; texId = GlStateManager.boundTexture(); }

    /** Flushes the recorded quads through the current render context, using the current matrix. */
    static void end() {
        Gl.flush(BUF, textured ? texId : -1);
        BUF.clear();
    }

    static double shade(double nx, double ny, double nz) {
        double l = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (l < 1e-9) return 0.8;
        double d = Math.abs((nx * LX + ny * LY + nz * LZ) / (l * LL));
        return 0.74 + 0.26 * d;
    }

    private static void vertex(double[] p, float[] c, double sh, float a) {
        float k = (float) (sh + (1.0 - sh) * c[3]);
        BUF.add(new float[]{(float) p[0], (float) p[1], (float) p[2], c[0] * k, c[1] * k, c[2] * k, a, 0f, 0f});
    }

    /** A quad with per-vertex colors; the light shading comes from its face normal. */
    static void quad(double[] a, double[] b, double[] c, double[] d, float[] ca, float[] cb, float[] cc, float[] cd, float alpha) {
        double ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
        double vx = d[0] - a[0], vy = d[1] - a[1], vz = d[2] - a[2];
        double sh = shade(uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx);
        vertex(a, ca, sh, alpha);
        vertex(b, cb, sh, alpha);
        vertex(c, cc, sh, alpha);
        vertex(d, cd, sh, alpha);
    }

    static void tri(double[] a, double[] b, double[] c, float[] ca, float[] cb, float[] cc, float alpha) {
        quad(a, b, c, c, ca, cb, cc, cc, alpha);
    }

    /** Textured vertex with a flat tint (used by the emblem). */
    static void tex(double x, double y, double z, float u, float v, float r, float g, float b, float a) {
        BUF.add(new float[]{(float) x, (float) y, (float) z, r, g, b, a, u, v});
    }

    static double[] p(double x, double y, double z) { return new double[]{x, y, z}; }

    /** Smooth ellipsoid, colored from top to bottom, with a per-vertex shading from its true normals. */
    static void ellipsoid(double cx, double cy, double cz, double rx, double ry, double rz,
                          float[] top, float[] bottom, float emissive) {
        final int lat = 9, lon = 14;
        for (int i = 0; i < lat; i++) {
            double a0 = Math.PI * i / lat - Math.PI / 2, a1 = Math.PI * (i + 1) / lat - Math.PI / 2;
            for (int j = 0; j < lon; j++) {
                double b0 = 2 * Math.PI * j / lon, b1 = 2 * Math.PI * (j + 1) / lon;
                ev(cx, cy, cz, rx, ry, rz, a0, b0, top, bottom, emissive);
                ev(cx, cy, cz, rx, ry, rz, a0, b1, top, bottom, emissive);
                ev(cx, cy, cz, rx, ry, rz, a1, b1, top, bottom, emissive);
                ev(cx, cy, cz, rx, ry, rz, a1, b0, top, bottom, emissive);
            }
        }
    }

    private static void ev(double cx, double cy, double cz, double rx, double ry, double rz, double a, double b,
                           float[] top, float[] bottom, float emissive) {
        double nx = Math.cos(a) * Math.cos(b), ny = Math.sin(a), nz = Math.cos(a) * Math.sin(b);
        float[] c = mix(top, bottom, (float) (ny * 0.5 + 0.5));
        float[] col = {c[0], c[1], c[2], Math.max(c[3], emissive)};
        vertex(new double[]{cx + nx * rx, cy + ny * ry, cz + nz * rz}, col, shade(nx / rx, ny / ry, nz / rz), 1f);
    }

    /** Cone (ears, spikes): base center, tip, base radius. */
    static void cone(double bx, double by, double bz, double tx, double ty, double tz, double r, float[] base, float[] tip) {
        final int n = 8;
        double dx = tx - bx, dy = ty - by, dz = tz - bz;
        double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= l; dy /= l; dz /= l;
        // two vectors perpendicular to the axis
        double ax = Math.abs(dx) < 0.9 ? 1 : 0, ay = ax == 1 ? 0 : 1;
        double ux = dy * 0 - dz * ay, uy = dz * ax - dx * 0, uz = dx * ay - dy * ax;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;
        double vx = dy * uz - dz * uy, vy = dz * ux - dx * uz, vz = dx * uy - dy * ux;
        double[] tipP = p(tx, ty, tz);
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n;
            double[] p0 = p(bx + (Math.cos(a0) * ux + Math.sin(a0) * vx) * r, by + (Math.cos(a0) * uy + Math.sin(a0) * vy) * r, bz + (Math.cos(a0) * uz + Math.sin(a0) * vz) * r);
            double[] p1 = p(bx + (Math.cos(a1) * ux + Math.sin(a1) * vx) * r, by + (Math.cos(a1) * uy + Math.sin(a1) * vy) * r, bz + (Math.cos(a1) * uz + Math.sin(a1) * vz) * r);
            tri(p0, p1, tipP, base, base, tip, 1f);
        }
    }
}

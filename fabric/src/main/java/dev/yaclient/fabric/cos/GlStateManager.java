package dev.yaclient.fabric.cos;

import java.util.ArrayDeque;

/** Shim for the few GlStateManager calls the cosmetic code uses: a tiny matrix stack, everything else is a no-op. */
final class GlStateManager {
    private static double[] m = identity();
    private static final ArrayDeque<double[]> STACK = new ArrayDeque<double[]>();
    private static int bound = -1;

    private GlStateManager() {}

    private static double[] identity() { return new double[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}; }

    static void reset() { m = identity(); STACK.clear(); bound = -1; }

    static double[] matrix() { return m; }

    static void pushMatrix() { STACK.push(m.clone()); }

    static void popMatrix() { if (!STACK.isEmpty()) m = STACK.pop(); }

    private static void mul(double[] b) {
        double[] r = new double[16];
        for (int i = 0; i < 4; i++) for (int j = 0; j < 4; j++) {
            double s = 0;
            for (int k = 0; k < 4; k++) s += m[i * 4 + k] * b[k * 4 + j];
            r[i * 4 + j] = s;
        }
        m = r;
    }

    static void translate(float x, float y, float z) {
        double[] t = identity();
        t[3] = x; t[7] = y; t[11] = z;
        mul(t);
    }

    static void scale(float x, float y, float z) {
        double[] t = identity();
        t[0] = x; t[5] = y; t[10] = z;
        mul(t);
    }

    static void rotate(float angle, float x, float y, float z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 1e-9) return;
        double ux = x / l, uy = y / l, uz = z / l, a = Math.toRadians(angle), c = Math.cos(a), s = Math.sin(a), t = 1 - c;
        double[] r = identity();
        r[0] = t * ux * ux + c;       r[1] = t * ux * uy - s * uz;  r[2] = t * ux * uz + s * uy;
        r[4] = t * ux * uy + s * uz;  r[5] = t * uy * uy + c;       r[6] = t * uy * uz - s * ux;
        r[8] = t * ux * uz - s * uy;  r[9] = t * uy * uz + s * ux;  r[10] = t * uz * uz + c;
        mul(r);
    }

    static void bindTexture(int id) { bound = id; }

    static int boundTexture() { return bound; }

    static void enableTexture2D() {}
    static void disableTexture2D() {}
    static void enableBlend() {}
    static void disableBlend() {}
    static void enableLighting() {}
    static void disableLighting() {}
    static void enableCull() {}
    static void disableCull() {}
    static void enableDepth() {}
    static void disableDepth() {}
    static void depthMask(boolean b) {}
    static void depthFunc(int f) {}
    static void color(float r, float g, float b, float a) {}
    static void tryBlendFuncSeparate(int a, int b, int c, int d) {}
}

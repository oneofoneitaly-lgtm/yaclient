package dev.yaclient;

/** Small math and color helpers used by the UI (animations, blending). */
public final class Ui {
    private Ui() {}

    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    public static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }

    public static int clamp(int v, int lo, int hi) { return v < lo ? lo : (v > hi ? hi : v); }

    /** Ease-out cubic, t in [0, 1]. */
    public static float ease(float t) {
        t = clamp(t, 0f, 1f);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    /** Frame-rate independent smoothing towards a target. dtMs = frame time, speed ~ 8..20. */
    public static float approach(float value, float target, float dtMs, float speed) {
        float k = 1f - (float) Math.exp(-speed * dtMs / 1000f);
        float n = value + (target - value) * k;
        return Math.abs(target - n) < 0.002f ? target : n;
    }

    /** Linear blend of two ARGB colors. */
    public static int mix(int c1, int c2, float t) {
        t = clamp(t, 0f, 1f);
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Multiplies the alpha channel of an ARGB color. */
    public static int alpha(int argb, float mul) {
        int a = (int) (((argb >>> 24) & 255) * clamp(mul, 0f, 1f));
        return (a << 24) | (argb & 0xFFFFFF);
    }
}

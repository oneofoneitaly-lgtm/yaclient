package dev.yaclient.fabric.cos;

/** Pixel pets of the anime bundles: Mochi (bunny), Kage (ninja cat), Byte (robot cat) and Inari (two-tailed fox). */
final class AnimePets {
    private AnimePets() {}

    private static final int BODY = 0, HEAD = 1, SNOUT = 2, LEG = 3, TAIL = 4, EAR = 5, EXTRA = 6;
    private static final double Y0 = -0.045;
    private static int T;

    private static float[] c(int hex) { return Vox.rgb(hex, 0f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }

    private static final Vox.Marker MARK = new Vox.Marker() {
        public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
            double ax = Math.abs(rx);
            switch (T) {
                case 0:   // Mochi: pink cheeks, pink socks, pink belly
                    if (part == HEAD && a == 2 && sa < 0) { if (ry > 0.1 && ry < 0.5 && ax > 0.55 && ax < 0.85) return g(0xFF8FB5, 0.2f); if (ry < -0.6 && ax < 0.2) return c(0xFFB7CF); }
                    if (part == HEAD && a == 1 && sa < 0 && ax < 0.15) return c(0xFFC9DA);
                    if (part == LEG && ry > 0.3) return c(0xFFB7CF);
                    if (part == BODY && a == 0 && ry > 0.3) return c(0xFFC9DA);
                    if (part == BODY && a == 1 && sa < 0 && ax < 0.2 && ((int) Math.round(rz * 5)) % 2 == 0) return c(0xFFC9DA);
                    return null;
                case 1:   // Kage: violet mouth cloth, wrappings, stripe
                    if (part == HEAD && a == 2 && sa < 0 && ry > 0.15) return g(0x6B4FD6, 0.25f);
                    if (part == HEAD && a == 0 && ry > 0.25 && rz < -0.1) return g(0x6B4FD6, 0.2f);
                    if (part == LEG && ((int) Math.round(ry * 4)) % 2 == 0 && ry > 0.0) return c(0x3A3B63);
                    if (part == BODY && a == 1 && sa < 0 && ax < 0.2) return g(0x8C6BFF, 0.35f);
                    if (part == BODY && a == 0 && ry > -0.1 && ry < 0.1) return g(0x6B4FD6, 0.25f);
                    return null;
                case 2:   // Byte: chunky pixels, dark screen face with a pixel smile, neon strips, glitch noise
                    if (part == HEAD && a == 2 && sa < 0) {
                        if (ry > 0.18 && ry < 0.42 && ((ax > 0.14 && ax < 0.30) || (ry > 0.30 && ax < 0.30))) return g(0x00F0FF, 0.95f);    // pixel smile
                        if (ry > -0.62 && ry < 0.62 && ax < 0.82) return c(0x05050E);
                    }
                    if ((part == BODY || part == HEAD || part == LEG) && ((int) Math.floor(rx * 6) * 31 + (int) Math.floor(ry * 5) * 17 + (int) Math.floor(rz * 7) * 13 + a * 7) % 11 == 0)
                        return ((int) Math.floor(rx * 6) + (int) Math.floor(rz * 7)) % 2 == 0 ? g(0x00F0FF, 0.9f) : g(0xFF2FBF, 0.9f);
                    if (part == HEAD && a == 2 && sa < 0 && ry > -0.62 && ry < 0.62 && ax < 0.82) return c(0x05050E);
                    if (part == HEAD && a == 1 && sa < 0 && ax < 0.12) return g(0xFF2FBF, 0.9f);
                    if (part == BODY && a == 0 && ry > -0.15 && ry < 0.08) return g(0x00F0FF, 0.95f);
                    if (part == BODY && a == 1 && sa < 0 && ax < 0.18) return ((int) Math.round(rz * 5)) % 2 == 0 ? g(0xFF2FBF, 0.9f) : g(0x00F0FF, 0.9f);
                    if (part == LEG && ry > 0.4) return g(0x00F0FF, 0.9f);
                    if (part == TAIL && (int) Math.round(rz * 3) % 2 == 0) return g(0x00F0FF, 0.9f);
                    return null;
                default:  // Inari: white chest, cheeks and tail tips, dark socks and ear tips
                    if (part == HEAD && a == 2 && sa < 0 && ry > 0.2 && ax > 0.15) return c(0xFFFFFF);
                    if (part == HEAD && a == 0 && ry > 0.15) return c(0xFFFFFF);
                    if (part == HEAD && a == 2 && sa < 0 && ry < -0.5 && ax < 0.15) return c(0xFFFFFF);
                    if (part == SNOUT) return c(0xFFF3E8);
                    if (part == BODY && a == 2 && sa < 0) return c(0xFFFFFF);
                    if (part == BODY && a == 0 && ry > 0.35) return c(0xFFE7D0);
                    if (part == LEG && ry > 0.15) return c(0x3A2418);
                    if (part == EAR && ry < -0.3) return c(0x2A1A14);
                    if (part == TAIL && rz > 0.45) return c(0xFFFFFF);
                    return null;
            }
        }
    };

    static void pet(Anime.Th th, float age) {
        if (th.index == 7) { LuckPix.pet(th, age); return; }
        if (th.pixel) { XmasPix.pet(th, age); return; }
        T = th.index;
        Vox.marker = MARK;
        Vox.TEXEL = T == 2 ? 0.0185 : 0.0125;      // Byte is the chunky one
        Vox.reset();
        float[] bt, bb, ht;       // body top/bottom, head
        switch (T) {
            case 0: bt = c(0xFFF4F8); bb = c(0xFFD3E2); break;
            case 1: bt = c(0x23264A); bb = c(0x0B0C1E); break;
            case 2: bt = c(0x2A1850); bb = c(0x120A28); break;
            default: bt = c(0xFB8A24); bb = c(0xE0620E); break;
        }
        double ph = age * (T == 0 ? 0.40 : 0.32);
        double hop = Math.abs(Math.sin(ph)) * (T == 0 ? 0.030 : 0.016) + Math.sin(age * 0.07) * 0.006;
        Vox.yo = Y0 - hop;
        double bob = Math.sin(ph * 2) * 0.004;
        // body and legs
        box(0, 0.02, 0.045, 0.068, 0.062, 0.115, bt, bb, BODY);
        for (int s = -1; s <= 1; s += 2) for (int f = 0; f < 2; f++) {
            double lp = ph + (f + (s > 0 ? 1 : 0)) * Math.PI;
            double z = f == 0 ? -0.045 : 0.135, step = Math.sin(lp) * 0.026, lift = Math.max(0, Math.cos(lp)) * 0.016;
            box(s * 0.046, 0.108 - lift, z + step, 0.024, 0.036, 0.024, bb, bb, LEG);
        }
        // head
        double hz = -0.115, hy = -0.072 + bob;
        float[] hc = T == 2 ? c(0x2F1C5A) : bt;
        box(0, hy, hz, 0.076, 0.066, 0.066, hc, T == 0 ? c(0xFFE3ED) : (T == 3 ? c(0xF27A1A) : (T == 1 ? c(0x15172E) : c(0x1D1040))), HEAD);
        box(0, hy + 0.030, hz - 0.074, 0.036, 0.026, 0.026, T == 3 ? c(0xFFF3E8) : (T == 0 ? c(0xFFE3ED) : (T == 1 ? c(0x15172E) : c(0x120A28))), T == 1 ? c(0x15172E) : (T == 3 ? c(0xFFF3E8) : bb), SNOUT);
        // nose
        box(0, hy + 0.012, hz - 0.101, 0.012, 0.008, 0.004, T == 0 ? c(0xFF7FA8) : c(0x1A0E14), T == 0 ? c(0xFF7FA8) : c(0x1A0E14), EXTRA);
        for (int s = -1; s <= 1; s += 2) {
            switch (T) {
                case 0:   // tall bunny ears with pink insides, one flops
                    double sway = Math.sin(age * 0.15 + s) * 0.008;
                    box(s * 0.040 + sway, hy - 0.140, hz + 0.020, 0.020, 0.074, 0.012, c(0xFFF4F8), c(0xFFD3E2), EAR);
                    box(s * 0.040 + sway, hy - 0.140, hz + 0.008, 0.011, 0.060, 0.003, c(0xFF9CBF), c(0xFFB7CF), EXTRA);
                    box(s * 0.040, hy - 0.004, hz - 0.068, 0.017, 0.020, 0.004, c(0x2A1A22), c(0x2A1A22), EXTRA);        // big round eyes
                    box(s * 0.045, hy - 0.012, hz - 0.071, 0.006, 0.007, 0.002, c(0xFFFFFF), c(0xFFFFFF), EXTRA);       // eye shine
                    break;
                case 1:   // pointed ears, slit eyes
                    box(s * 0.052, hy - 0.080, hz + 0.010, 0.020, 0.030, 0.014, c(0x23264A), c(0x15172E), EAR);
                    box(s * 0.040, hy - 0.008, hz - 0.068, 0.018, 0.007, 0.004, c(0xEFF1FF), g(0xB8BCFF, 0.8f), EXTRA);
                    box(s * 0.040, hy - 0.008, hz - 0.071, 0.006, 0.007, 0.002, c(0x6B4FD6), c(0x6B4FD6), EXTRA);
                    break;
                case 2:   // headphones + pixel screen eyes
                    box(s * 0.084, hy - 0.004, hz, 0.014, 0.030, 0.030, g(0xFF2FBF, 0.8f), g(0xC01A90, 0.6f), EXTRA);
                    box(s * 0.052, hy - 0.090, hz + 0.004, 0.020, 0.026, 0.014, c(0x2F1C5A), c(0x120A28), EAR);
                    box(s * 0.040, hy - 0.010, hz - 0.068, 0.016, 0.018, 0.004, g(0x00F0FF, 1f), g(0x00B8C8, 0.9f), EXTRA);
                    break;
                default:  // tall fox ears with dark tips
                    box(s * 0.050, hy - 0.100, hz + 0.014, 0.022, 0.040, 0.014, c(0xFB8A24), c(0xE0620E), EAR);
                    box(s * 0.050, hy - 0.098, hz + 0.001, 0.011, 0.028, 0.003, c(0xFFE7D0), c(0xFFE7D0), EXTRA);
                    box(s * 0.040, hy - 0.008, hz - 0.068, 0.016, 0.009, 0.004, c(0x2A1A14), c(0x2A1A14), EXTRA);
                    box(s * 0.040, hy - 0.008, hz - 0.071, 0.007, 0.007, 0.002, c(0xFFC21A), g(0xFFC21A, 0.8f), EXTRA);
                    break;
            }
        }
        if (T == 0) {
            // cherry blossom on the left ear
            double fx = 0.075, fy = hy - 0.168, fz = hz - 0.002;
            for (int k = 0; k < 5; k++) {
                double an = k * Math.PI * 2 / 5;
                box(fx + Math.cos(an) * 0.020, fy + Math.sin(an) * 0.020, fz, 0.011, 0.011, 0.007, c(0xFF9CBF), c(0xFFD3E2), EXTRA);
            }
            box(fx, fy, fz - 0.007, 0.008, 0.008, 0.004, c(0xFFD54A), c(0xFFD54A), EXTRA);
        }
        if (T == 1) {
            // headband with a metal plate, and a long scarf that trails behind
            box(0, hy - 0.036, hz, 0.080, 0.012, 0.070, g(0x4B4F8A, 0.2f), g(0x333665, 0.2f), EXTRA);
            box(0, hy - 0.036, hz - 0.071, 0.030, 0.013, 0.004, c(0xD7DBF0), c(0xB8BCD8), EXTRA);
            box(0, hy - 0.036, hz - 0.075, 0.010, 0.006, 0.002, c(0x6B4FD6), c(0x6B4FD6), EXTRA);
            for (int i = 0; i < 4; i++) {
                double f = i / 3.0;
                box(Math.sin(age * 0.18 - i * 0.8) * 0.02, -0.010 + f * 0.012, -0.060 + 0.07 + f * 0.17, 0.034, 0.008, 0.032, g(0x8C6BFF, 0.5f), g(0x6B4FD6, 0.4f), EXTRA);
            }
        }
        if (T == 2) {
            // antenna with a glowing orb
            box(0.0, hy - 0.086, hz + 0.01, 0.005, 0.024, 0.005, c(0xB8BCD8), c(0x8A8FB0), EXTRA);
            double pulse = 0.85 + 0.15 * Math.sin(age * 0.3);
            Poly.ellipsoid(0, hy - 0.126 + Vox.yo, hz + 0.01, 0.020 * pulse, 0.020 * pulse, 0.020 * pulse, g(0xFF2FBF, 1f), g(0xFF7FDC, 1f), 1f);
            box(0, hy - 0.071, hz, 0.082, 0.008, 0.012, g(0xFF2FBF, 0.8f), g(0xC01A90, 0.6f), EXTRA);   // headphone band
        }
        if (T == 3) {
            // red ribbon with a golden bell
            box(0, -0.012, -0.060, 0.074, 0.013, 0.020, g(0xE02B1A, 0.4f), g(0xB01E10, 0.3f), EXTRA);
            Poly.ellipsoid(0, 0.016 + Vox.yo, -0.082, 0.014, 0.014, 0.012, g(0xFFC21A, 0.9f), g(0xFFE27A, 0.9f), 1f);
        }
        // tail(s)
        int tails = T == 3 ? 2 : 1;
        for (int tl = 0; tl < tails; tl++) {
            double side = tails == 2 ? (tl == 0 ? -1 : 1) : 0;
            int n = T == 0 ? 1 : (T == 3 ? 4 : 5);
            for (int i = 0; i < n; i++) {
                double f = n == 1 ? 0 : i / (double) (n - 1);
                double x = side * (0.03 + 0.05 * f) + Math.sin(age * 0.14 - i * 0.7 + tl * 2) * 0.035 * f * 2.0;
                if (T == 2) x = ((i % 2 == 0) ? 1 : -1) * 0.03 * f;            // zig-zag cable
                double y = 0.0 - 0.10 * f * f - 0.02 * f;
                double z = 0.17 + 0.14 * f;
                double r = 0.034 * (1 - f * 0.4);
                if (T == 0) { box(0, 0.03, 0.18, 0.040, 0.040, 0.040, c(0xFFFFFF), c(0xFFE3ED), TAIL); }
                else if (T == 3) { double rr = 0.040 * (1 - f * 0.25); box(x, y - 0.01, z, rr, rr, rr * 1.4, c(0xFB8A24), c(0xE0620E), TAIL); }
                else if (T == 2) box(x, y, z, 0.022, 0.022, 0.034, c(0x2A1850), c(0x120A28), TAIL);
                else box(x, y, z, r, r, r * 1.35, c(0x23264A), i >= n - 2 ? g(0x8C6BFF, 0.5f) : c(0x0B0C1E), TAIL);
            }
        }
        // sparkle on top of the pet (like Lumi's halo)
        double pulse = 0.8 + 0.2 * Math.sin(age * 0.2);
        float[] sp = T == 0 ? g(0xFFB7CF, 0.9f) : T == 1 ? g(0x8C6BFF, 0.9f) : T == 2 ? g(0x00F0FF, 1f) : g(0xFFC21A, 0.9f);
        Poly.ellipsoid(0.11, -0.12 + Vox.yo, 0.06, 0.016 * pulse, 0.016 * pulse, 0.016 * pulse, sp, Poly.SNOW, 1f);
        Vox.marker = null;
        Vox.TEXEL = 0.0125;
    }

    private static void box(double cx, double cy, double cz, double hx, double hy, double hz, float[] top, float[] bot, int part) {
        Vox.box(cx, cy, cz, hx, hy, hz, top, bot, part);
    }
}

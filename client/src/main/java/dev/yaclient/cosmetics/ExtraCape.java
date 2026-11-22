package dev.yaclient.cosmetics;

/** The five single capes: chunky pixel-art scenes on a 22 x 32 grid, full surface, with a little animation. */
final class ExtraCape {
    private ExtraCape() {}

    static final int NU = 22, NV = 32;

    private static float[] c(int hex) { return Vox.rgb(hex, 0.25f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }
    private static float[] mix(float[] a, float[] b, double t) { return Poly.mix(a, b, (float) t); }
    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    static void draw(String id, float age, boolean plus) {
        for (int i = 0; i < NU; i++) for (int j = 0; j < NV; j++) {
            float[] col;
            if (id.equals("sky")) col = sky(i, j, age);
            else if (id.equals("sunset")) col = sunset(i, j, age);
            else if (id.equals("forest")) col = forest(i, j, age);
            else if (id.equals("ocean")) col = ocean(i, j, age);
            else if (id.equals("storm")) col = storm(i, j, age);
            else col = space(i, j, age);
            double u0 = i / (double) NU, u1 = (i + 1) / (double) NU, v0 = j / (double) NV, v1 = (j + 1) / (double) NV;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), col, col, col, col, 1f);
        }
    }

    private static boolean disc(int i, int j, double cx, double cy, double r) { double dx = i - cx, dy = j - cy; return dx * dx + dy * dy <= r * r; }
    private static int mod(int a, int m) { return ((a % m) + m) % m; }

    // ---- sky: blue sky, pastel rainbow, a smiling cloud, a sun and drifting clouds
    private static float[] sky(int i, int j, float age) {
        float[] col = mix(c(0x62BDFF), c(0xBFEAFF), j / 31.0);
        int[] rb = {0xFF4B4B, 0xFF9A3C, 0xFFE45E, 0x5BDB6E, 0x3FA9FF, 0x9A6BFF};
        double d = Math.hypot(i - 10.5, (j - 31) * 1.0);
        int k = (int) Math.floor((d - 8.5) / 1.5);
        if (k >= 0 && k < 6 && j < 30) col = c(rb[k]);
        if (disc(i, j, 17, 5, 2.4)) col = g(0xFFE45E, 0.8f);
        if (disc(i, j, 17, 5, 3.6) && !disc(i, j, 17, 5, 2.4) && ((i + j + (int) (age * 0.05)) & 1) == 0 && Math.abs(i - 17) + Math.abs(j - 5) < 5) col = g(0xFFF2A0, 0.7f);
        int cx = mod((int) (age * 0.03) + 14, 34) - 6;                                                       // drifting cloud
        if (disc(i, j, cx, 10, 1.6) || disc(i, j, cx + 2, 9, 2.0) || disc(i, j, cx + 4, 10, 1.6) || (j == 11 && i >= cx - 1 && i <= cx + 5)) col = c(0xFFFFFF);
        // big cute cloud with a face, bottom-left
        if (disc(i, j, 5, 24, 2.6) || disc(i, j, 8, 23, 3.2) || disc(i, j, 11, 24, 2.6) || (j >= 24 && j <= 26 && i >= 3 && i <= 13)) {
            col = j >= 26 ? c(0xDCE9F5) : c(0xFFFFFF);
            if ((i == 6 || i == 10) && j == 24) col = c(0x2A2D3A);
            if ((i == 5 || i == 11) && j == 25) col = c(0xFFB0C8);
            if (i == 8 && j == 25) col = c(0xFF8FA8);
        }
        return col;
    }

    // ---- sunset: striped sun over a calm sea, palm tree, birds
    private static float[] sunset(int i, int j, float age) {
        int[] sk = {0x3A2B7A, 0x5A3A8F, 0x8A4A9E, 0xC2559A, 0xEE6A8A, 0xFF8A70, 0xFFAE5A, 0xFFD060};
        float[] col = c(sk[Math.min(7, j * 8 / 20)]);
        if (j < 14 && noise(i, j) > 0.95) col = g(0xFFFFFF, 0.8f);
        boolean sun = disc(i, j, 12, 19, 6.2) && j < 20;
        if (sun && !(j > 13 && (j % 3 == 0))) col = mix(g(0xFFF08A, 0.9f), g(0xFF7A4A, 0.9f), (j - 13) / 7.0);
        if (j >= 20) {
            col = mix(c(0x3C2A7A), c(0x1A1450), (j - 20) / 11.0);
            int w = mod(i + (int) (age * 0.06) + j * 3, 8);
            if (Math.abs(i - 12) < 5 - (j - 20) / 3 && w < 3 && j % 2 == 0) col = g(0xFFC070, 0.7f);              // sun glitter
            else if (w == 0 && j % 3 == 1) col = c(0x6A52B8);
        }
        // palm tree
        int[][] trunk = {{3, 31}, {3, 29}, {3, 27}, {4, 25}, {4, 23}, {4, 21}, {5, 19}, {5, 17}, {5, 15}};
        for (int[] t : trunk) if ((i == t[0] || i == t[0] + 1) && (j == t[1] || j == t[1] - 1)) col = c(0x120A22);
        int[][] frond = {{5, 14}, {6, 13}, {7, 13}, {8, 14}, {4, 13}, {3, 13}, {2, 14}, {1, 15}, {6, 15}, {7, 16}, {4, 15}, {3, 16}, {5, 12}, {5, 11}, {6, 12}, {4, 12}};
        for (int[] f : frond) if (i == f[0] && j == f[1]) col = c(0x120A22);
        if (j >= 28 && i < 8) col = c(0x120A22);
        int bx = mod((int) (age * 0.05) + 16, 30) - 4;                                                         // seagull
        if (j == 7 && (i == bx || i == bx + 2)) col = c(0x2A1850);
        if (j == 8 && i == bx + 1) col = c(0x2A1850);
        return col;
    }

    // ---- forest: night pines, moon, mushrooms and fireflies
    private static float[] forest(int i, int j, float age) {
        float[] col = mix(c(0x0B1632), c(0x24456A), j / 31.0);
        if (j < 12 && noise(i, j) > 0.96) col = g(0xFFFFFF, 0.8f);
        if (disc(i, j, 16, 6, 3.0)) { col = g(0xFFF6C0, 0.85f); if (disc(i, j, 15, 5, 0.9) || disc(i, j, 17, 8, 0.7)) col = c(0xE0D49A); }
        for (int t = 0; t < 4; t++) {                                                                            // back pines
            int px = 2 + t * 6, ph = 16 + (t * 5) % 4;
            if (j >= ph && Math.abs(i - px) <= (j - ph) / 2 + (j > ph ? 0 : 0) && j < 29) col = c(0x123A3A);
        }
        for (int t = 0; t < 3; t++) {                                                                            // front pines
            int px = 5 + t * 6, ph = 20 + (t * 3) % 4;
            if (j >= ph && Math.abs(i - px) <= (j - ph) / 2 && j < 30) col = c(t == 1 ? 0x0F4A2C : 0x0A2F1E);
            if (j >= 28 && j < 31 && Math.abs(i - px) <= 0) col = c(0x3A2412);
        }
        if (j >= 29) col = c(0x0A2418);
        if (j == 28 && (i == 1 || i == 2 || i == 3)) col = c(0xE04848);                                       // mushroom cap
        if (j == 27 && i == 2) col = c(0xE04848);
        if (j == 28 && i == 2) col = c(0xFFFFFF);
        if (j == 29 && i == 2) col = c(0xEADFC8);
        for (int f = 0; f < 7; f++) {                                                                            // fireflies
            int fx = mod((int) (5 + f * 3 + Math.sin(age * 0.04 + f * 2) * 3), 22), fy = (int) (14 + (f * 5) % 12 + Math.sin(age * 0.05 + f) * 2);
            if (i == fx && j == fy) col = g(0xFFF27A, 0.5f + 0.5f * (float) Math.max(0, Math.sin(age * 0.12 + f * 1.7)));
        }
        return col;
    }

    // ---- ocean: sunlit water, a little orange fish, bubbles, seaweed, starfish
    private static float[] ocean(int i, int j, float age) {
        float[] col = mix(c(0x35C0EC), c(0x0A3F7E), j / 31.0);
        int ray = mod(i - j / 2 + (int) (age * 0.02), 9);
        if (ray < 2 && j < 22) col = mix(col, g(0xBFF3FF, 0.4f), 0.28);
        if (j >= 29) col = c(0xE6CE8C);
        if (j >= 29 && noise(i, j) > 0.8) col = c(0xCFB674);
        for (int s = 0; s < 4; s++) {                                                                            // seaweed
            int sx = 2 + s * 6, sh = 5 + (s * 3) % 4;
            for (int q = 0; q < sh; q++) { int sw = (int) Math.round(Math.sin(age * 0.07 + q * 0.7 + s) * 1.0); if (i == sx + sw && j == 28 - q) col = c(q % 2 == 0 ? 0x2FBF5A : 0x1F8F45); }
        }
        if (disc(i, j, 12, 30, 1.6) && j >= 28 ) col = c(0xFF86A8);                                             // starfish
        int fx = mod((int) (age * 0.06), 34) - 6, fy = 14 + (int) Math.round(Math.sin(age * 0.05) * 2);
        for (int[] p : new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}, {1, -1}, {2, -1}, {1, 1}, {2, 1}, {-1, -1}, {-1, 1}}) if (i == fx + p[0] && j == fy + p[1]) col = c(p[0] == -1 ? 0xFF7A1F : 0xFF9A3C);
        if (i == fx + 3 && j == fy - 0) col = c(0xFF9A3C);
        if (i == fx + 3 && j == fy - 1) col = c(0x15151A);
        for (int b = 0; b < 5; b++) {                                                                            // bubbles
            int bx = 3 + b * 4 + (int) Math.round(Math.sin(age * 0.05 + b)), by = mod((int) (30 - age * (0.05 + b * 0.01) + b * 7), 30);
            if (i == bx && j == by) col = c(0xE8FAFF);
        }
        return col;
    }


    // ---- storm: night clouds with a pixel lightning bolt that strikes, flashes and fades, then strikes somewhere else
    private static int boltEpoch = -1;
    private static final int[] BX = new int[NV], BR = new int[NV];

    private static void makeBolt(int epoch) {
        boltEpoch = epoch;
        java.util.Arrays.fill(BX, -99); java.util.Arrays.fill(BR, -99);
        int x = 4 + (int) (noise(epoch, 7) * 14), br = 9 + (int) (noise(epoch, 3) * 6), bx = x;
        int dir = noise(epoch, 5) > 0.5 ? 1 : -1;
        for (int j = 0; j < NV; j++) {
            BX[j] = Math.max(1, Math.min(NU - 2, x));
            float r = noise(epoch * 31 + j, 11);
            x += r < 0.33 ? -1 : (r < 0.66 ? 0 : 1);
            if (j % 5 == 2) x += dir;                                                    // diagonal jags
            if (j == br) { bx = x; }
            if (j > br && j < br + 9) { bx += (j % 2 == 0) ? -dir : 0; BR[j] = Math.max(0, Math.min(NU - 1, bx)); bx += -dir; }
        }
    }

    private static float[] storm(int i, int j, float age) {
        int epoch = (int) (age / 48f);
        if (epoch != boltEpoch) makeBolt(epoch);
        float ph = age - epoch * 48f;
        float flash = ph < 3 ? 1f : (ph < 14 ? 1f - (ph - 3) / 11f * 0.8f : Math.max(0f, 0.2f - (ph - 14) * 0.006f));
        float n = noise((i + (int) (age * 0.03)) / 2, j / 2);
        float[] col = mix(c(0x07070D), c(0x181A30), 0.25 + 0.55 * n * (0.4 + j / 31.0));
        if (j < 8 && noise(i, j + 40) > 0.95) col = c(0x2B2F55);
        col = mix(col, c(0x3A4A8A), flash * 0.22);                                       // the whole sky flashes
        boolean core = BX[j] == i || (BR[j] == i) || (j > 0 && BX[j - 1] == i && flash > 0.05f && ((i + j) & 1) == 0);
        boolean glow = Math.abs(BX[j] - i) == 1 || (BR[j] >= 0 && Math.abs(BR[j] - i) == 1);
        if (flash > 0.01f) {
            if (core) col = mix(col, g(0xFFFFFF, 1f), Math.min(1f, flash * 1.6f));
            else if (glow) col = mix(col, g(0x9FB4FF, 0.9f), flash * 0.7f);
        }
        if (j >= 30) col = mix(col, c(0x3A4A8A), 0.12 + 0.2 * flash);                      // little rim at the hem
        return col;
    }

    // ---- space: deep sky, a ringed planet, a small moon and a shooting star
    private static float[] space(int i, int j, float age) {
        float[] col = mix(c(0x0A0820), c(0x1A1240), j / 31.0);
        if (noise(i, j) > 0.93) col = g(0xFFFFFF, 0.4f + 0.5f * (float) Math.max(0, Math.sin(age * 0.08 + noise(j, i) * 20)));
        double dx = i - 10.5, dy = j - 15.5;
        boolean ringBack = Math.abs(dy + dx * 0.28) < 1.0 && Math.abs(dx) < 9.5 && Math.abs(dx) > 5.4 && dx > -9.5;
        if (ringBack && dy + dx * 0.28 < 0) col = c(0xFFD9A0);
        if (disc(i, j, 10.5, 15.5, 5.2)) {
            int band = (j - 10) / 2;
            col = c(new int[]{0xFFB06A, 0xFF8A5A, 0xFFC27A, 0xE8704F, 0xFFAA66}[Math.max(0, Math.min(4, band))]);
            if (dx + dy * 0.5 > 2.5) col = mix(col, c(0x3A1A3A), 0.45);
        }
        boolean ring = Math.abs(dy + dx * 0.28) < 1.0 && Math.abs(dx) < 9.5 && Math.abs(dx) > 5.4;
        if (ring && dy + dx * 0.28 >= 0) col = c(0xFFE0B0);
        if (ring && dy + dx * 0.28 < 0 && Math.hypot(dx, dy) > 5.2) col = c(0xFFD9A0);
        if (disc(i, j, 3, 26, 2.2)) col = mix(c(0xB9A8FF), c(0x5A4AB0), (i - 1) / 4.0);
        if (disc(i, j, 4, 25, 0.7)) col = c(0xD8CCFF);
        int sx = mod((int) (age * 0.12), 60) - 14, sy = sx / 2;
        if (sy >= 0 && sy < 14 && i == 21 - sx + 0 && j == sy + 2) col = g(0xFFFFFF, 1f);
        if (sy >= 1 && sy < 15 && i == 22 - sx && j == sy + 1) col = g(0xB0C8FF, 0.8f);
        return col;
    }
}

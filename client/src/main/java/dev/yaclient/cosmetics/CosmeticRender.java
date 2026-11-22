package dev.yaclient.cosmetics;

/**
 * Geometry of the YA CLIENT Beta Tester bundle: wings, cape, bandana and the Ember pet.
 * Everything is smooth-shaded vertex geometry (no low resolution textures), red and black.
 * Coordinates are in blocks, in Minecraft model space (y points DOWN, the back of the player is +z).
 */
final class CosmeticRender {
    private CosmeticRender() {}

    private static float[] c3(float[] c, float emissive) { return new float[]{c[0], c[1], c[2], Math.max(c[3], emissive)}; }

    private static float smooth(float t) { t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }

    // =====================================================================================
    // WINGS
    // =====================================================================================
    // row: count, tStart, tEnd, angleStart, angleEnd (deg), lenBase, lenAmp (len = base + amp*sin(pi*f)), width, zOffset
    static final double[][] ROWS = {
            {9, 0.25, 1.00, -84, -14, 0.34, 0.40, 0.070, 0.000},
            {8, 0.12, 0.90, -88, -30, 0.26, 0.20, 0.062, 0.006},
            {8, 0.05, 0.95, -84, -20, 0.14, 0.10, 0.052, 0.012}
    };

    /** Arm of the wing (leading edge), cubic bezier in the wing plane: x outwards, y up. */
    static double[] arm(double t) {
        double[] p0 = {0.02, 0.0}, p1 = {0.04, 0.26}, p2 = {0.30, 0.38}, p3 = {0.62, 0.28};
        double u = 1 - t;
        double x = u * u * u * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t * t * t * p3[0];
        double y = u * u * u * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t * t * t * p3[1];
        return new double[]{x, y};
    }

    /** One wing in its own plane (x out, y up, z thickness). The caller mirrors it for the other side. */
    /** Set while a single (non-bundle) item is drawn: its style id (e.g. "dragon"). */
    static String extra;

    static void wing(float age) {
        if (extra != null) { ExtraPix.wing(extra, age); return; }
        if (anime != null && anime.pixel) { XmasPix.wing(anime, age); return; }
        if (anime != null && anime.index == 7) { LuckPix.wing(anime, age); return; }
        int k = 0;
        for (int row = 2; row >= 0; row--) {          // long feathers first, short covert feathers on top
            double[] r = (anime != null ? anime.rows : ROWS)[row];
            int n = (int) r[0];
            for (int i = 0; i < n; i++, k++) {
                double f = n == 1 ? 0 : i / (double) (n - 1);
                double t = r[1] + (r[2] - r[1]) * f;
                double ang = Math.toRadians(r[3] + (r[4] - r[3]) * f + Math.sin(age * 0.16 - i * 0.45) * 3.0);
                double len = r[5] + r[6] * Math.sin(Math.PI * f);
                double[] b = arm(t);
                feather(b[0], b[1], r[8] + i * 0.0004, Math.cos(ang), Math.sin(ang), len, r[7], row, (float) f, age, i);
            }
        }
    }

    private static void feather(double bx, double by, double z, double dx, double dy, double len, double w,
                                int row, float rowF, float age, int idx) {
        final int N = 6;
        double nx = -dy, ny = dx;                       // perpendicular
        double[][] left = new double[N + 1][], mid = new double[N + 1][], right = new double[N + 1][];
        float[][] cl = new float[N + 1][], cm = new float[N + 1][], cr = new float[N + 1][];
        float glow = 0.85f + 0.15f * (float) Math.sin(age * 0.10 + idx);
        for (int j = 0; j <= N; j++) {
            double s = j / (double) N;
            double bend = 0.14 * len * s * s;           // feathers curve gently
            double cx = bx + dx * len * s + nx * bend, cy = by + dy * len * s + ny * bend;
            double wp = w * Math.sin(Math.PI * (0.12 + 0.88 * s));
            left[j] = Poly.p(cx + nx * wp, cy + ny * wp, z);
            mid[j] = Poly.p(cx, cy, z + 0.0015);
            right[j] = Poly.p(cx - nx * wp, cy - ny * wp, z);
            cl[j] = featherColor(row, (float) s, 1f, glow, rowF);
            cm[j] = featherColor(row, (float) s, 0f, glow, rowF);
            cr[j] = cl[j];
        }
        for (int j = 0; j < N; j++) {
            Poly.quad(left[j], mid[j], mid[j + 1], left[j + 1], cl[j], cm[j], cm[j + 1], cl[j + 1], 1f);
            Poly.quad(mid[j], right[j], right[j + 1], mid[j + 1], cm[j], cr[j], cr[j + 1], cm[j + 1], 1f);
        }
    }

    /** True while the white Qlux bundle is being drawn (wing and bandana palette). */
    static boolean qlux;
    /** Set while an anime bundle (Sakura, Shinobi, Neon, Kitsune) is being drawn: wing and band palette. */
    static Anime.Th anime;

    private static float[] featherColor(int row, float s, float edge, float glow, float rowF) {
        if (anime != null) return Anime.feather(anime, row, s, edge, glow, rowF);
        if (qlux) {
            float[] w = row == 0 ? Poly.mix(Poly.SNOW, Poly.PEARL, smooth(s * 1.1f) * 0.9f)
                    : row == 1 ? Poly.mix(Poly.SNOW, Poly.PEARL, s * 0.6f) : Poly.mix(Poly.SNOW, Poly.SNOW, 0f);
            w = Poly.mix(w, Poly.ICE, Math.max(0f, (s - 0.80f) / 0.20f) * (row == 0 ? 0.85f : 0.5f) * glow);
            float[] o = Poly.mix(w, Poly.SILVER, edge * edge * 0.55f);
            o[3] = Math.max(o[3], row == 0 ? 0.30f : 0.18f);
            return o;
        }
        float[] c;
        if (row == 0) {
            c = Poly.mix(Poly.BLACK, Poly.DEEP, smooth(s * 1.25f));
            c = Poly.mix(c, Poly.RED, Math.max(0f, (s - 0.62f) / 0.38f) * 0.9f * glow);
            c = Poly.mix(c, Poly.HOT, Math.max(0f, (s - 0.92f) / 0.08f) * 0.8f);
        } else if (row == 1) {
            c = Poly.mix(Poly.BLACK, Poly.DEEP, s * 0.85f);
            c = Poly.mix(c, Poly.RED, Math.max(0f, (s - 0.75f) / 0.25f) * 0.7f);
        } else {
            c = Poly.mix(Poly.DARK, Poly.BLACK, s);
        }
        float e = edge * edge * (0.25f + 0.55f * s);
        float[] out = Poly.mix(c, Poly.RED, e);
        out[3] = Math.max(out[3], e * 0.8f);
        return out;
    }

    // =====================================================================================
    // CAPE: static black cloth with scanlines and a red trim. The cloth only waves for YA Plus
    // members; the logo emblem always glitches.
    // =====================================================================================
    static final double CAPE_W = 0.66, CAPE_H = 1.0, THK = 0.055;   // THK: cloth thickness (like the vanilla cape's 1 px)
    static final int CAPE_C = 14, CAPE_R = 40;

    static double[] capePt(double u, double v, float age, boolean plus) {
        double x = (u - 0.5) * CAPE_W * (1 + 0.06 * v);
        double y = v * CAPE_H;
        double z = 0.012 + THK + 0.02 * v;
        if (plus) {
            z += Math.sin(age * 0.10 + v * 5.5 + u * 2.2) * 0.026 * v
                    + Math.sin(age * 0.06 + u * 7.0 - v * 2.0) * 0.011 * v
                    + Math.sin(u * Math.PI * 5) * 0.010 * v;
        }
        return Poly.p(x, y, z);
    }


    /** Side colour of the cape cloth (what you see on its edges), by style. */
    static int capeEdgeColor(String style) {
        int cb = CapeArt.bg(style);
        if (cb >= 0) return cb;
        switch (style) {
            case "ya": return 0x5A0A0A;
            case "mono": return 0x0C0C0E;
            case "qlux": return 0xC9CFDD;
            case "sakura": return 0xE79AB4;
            case "shinobi": return 0x1A1A22;
            case "kitsune": return 0xD9692A;
            case "santa": return 0xB01E1E;
            case "frost": return 0x9CCCE8;
            case "clown": return 0xE0E0E8;
            case "luck": return 0x2E8F48;
            case "sky": return 0x62BDFF;
            case "sunset": return 0x3C2A7A;
            case "forest": return 0x1E5A2A;
            case "ocean": return 0x1A4A7A;
            case "space": return 0x120A2A;
            case "storm": return 0x1A1C2A;
            default: return 0x16161C;
        }
    }

    /** Thickness of the cape: the inner face and the left, right and bottom sides (the top is hidden by the shoulders). */
    static void capeThickness(float age, boolean plus, int edgeRgb) {
        float[] e = Vox.rgb(edgeRgb, 0f);
        float[] in = {e[0] * 0.7f, e[1] * 0.7f, e[2] * 0.7f, 0f};
        for (int i = 0; i < CAPE_C; i++) for (int j = 0; j < CAPE_R; j++) {
            double u0 = i / (double) CAPE_C, u1 = (i + 1) / (double) CAPE_C, v0 = j / (double) CAPE_R, v1 = (j + 1) / (double) CAPE_R;
            Poly.quad(capeIn(u0, v0, age, plus), capeIn(u1, v0, age, plus), capeIn(u1, v1, age, plus), capeIn(u0, v1, age, plus), in, in, in, in, 1f);
        }
        for (int j = 0; j < CAPE_R; j++) {
            double v0 = j / (double) CAPE_R, v1 = (j + 1) / (double) CAPE_R;
            for (int sd = 0; sd <= 1; sd++)
                Poly.quad(capePt(sd, v0, age, plus), capePt(sd, v1, age, plus), capeIn(sd, v1, age, plus), capeIn(sd, v0, age, plus), e, e, e, e, 1f);
        }
        for (int i = 0; i < CAPE_C; i++) {
            double u0 = i / (double) CAPE_C, u1 = (i + 1) / (double) CAPE_C;
            Poly.quad(capePt(u0, 1, age, plus), capePt(u1, 1, age, plus), capeIn(u1, 1, age, plus), capeIn(u0, 1, age, plus), e, e, e, e, 1f);
        }
    }

    private static double[] capeIn(double u, double v, float age, boolean plus) { double[] p = capePt(u, v, age, plus); p[2] -= THK; return p; }

    private static float[] capeColor(double u, double v, float age, boolean plus, int row) {
        float[] base = Poly.mix(Poly.BLACK, Poly.DARK, (float) (v * 0.6));
        base = Poly.mix(base, Poly.DEEP, (float) (v * v * 0.55));
        if ((row & 1) == 1) { base[0] *= 0.82f; base[1] *= 0.82f; base[2] *= 0.82f; }   // scanlines
        double edge = Math.min(Math.min(u, 1 - u) * 0.66 / 0.030, (1 - v) / 0.035);
        float trim = smooth((float) (1.0 - edge));
        float[] c = Poly.mix(base, Poly.RED, trim);
        float inner = smooth((float) (1.0 - Math.abs(Math.min(Math.min(u, 1 - u) * 0.66, (1 - v) * 1.0) - 0.055) / 0.008));
        c = Poly.mix(c, Poly.DEEP, inner * 0.8f);
        c[3] = Math.max(c[3], Math.max(trim * 0.9f, inner * 0.3f));
        if (plus) {
            double s = Math.sin((u * 0.9 + v * 1.5) * 7.0 - age * 0.09);
            float band = (float) Math.pow(Math.max(0.0, s), 10) * (float) (0.35 + 0.65 * v);
            c = Poly.mix(c, Poly.HOT, band * 0.5f);
            c[3] = Math.max(c[3], band * 0.9f);
        }
        return c;
    }

    static void capeBase(float age, boolean plus) {
        for (int i = 0; i < CAPE_C; i++) {
            for (int j = 0; j < CAPE_R; j++) {
                double u0 = i / (double) CAPE_C, u1 = (i + 1) / (double) CAPE_C, v0 = j / (double) CAPE_R, v1 = (j + 1) / (double) CAPE_R;
                Poly.quad(capePt(u0, v0, age, plus), capePt(u1, v0, age, plus), capePt(u1, v1, age, plus), capePt(u0, v1, age, plus),
                        capeColor(u0, v0, age, plus, j), capeColor(u1, v0, age, plus, j), capeColor(u1, v1, age, plus, j), capeColor(u0, v1, age, plus, j), 1f);
            }
        }
    }

    // emblem placement on the cape (the logo is square)
    static final double EM_U0 = 0.17, EM_U1 = 0.83, EM_V0 = 0.10;
    static final double EM_V1 = EM_V0 + (EM_U1 - EM_U0) * CAPE_W / CAPE_H;

    private static double hash(long a, long b) {
        long x = a * 374761393L + b * 668265263L;
        x = (x ^ (x >> 13)) * 1274126177L;
        x ^= x >> 16;
        return (x & 0xFFFF) / 65535.0;
    }

    /** The logo, drawn as a glitching hologram: bursts of sliced rows and red/white ghost copies. */
    static void capeEmblem(float age, boolean plus) {
        double t = age / 20.0;
        long frame = (long) Math.floor(t * 16.0);
        boolean burst = (t % 2.4) < 0.38 || hash(frame, 7) < 0.035;
        final int S = 14;
        double[] off = new double[S];
        if (burst) for (int i = 0; i < S; i++) off[i] = hash(i, frame) > 0.45 ? (hash(i + 31, frame) - 0.5) * 0.20 : 0.0;
        if (burst) {
            emblemPass(age, plus, off, 0.045, 1f, 0.12f, 0.10f, 0.60f);
            emblemPass(age, plus, off, -0.045, 1f, 0.92f, 0.92f, 0.45f);
        }
        float flicker = burst ? 0.85f + 0.15f * (float) hash(frame, 3) : 1f;
        emblemPass(age, plus, off, 0.0, 1f, 1f, 1f, 0.97f * flicker);
    }

    private static void emblemPass(float age, boolean plus, double[] off, double shift, float r, float g, float b, float a) {
        final int S = off.length, N = 4;
        for (int s = 0; s < S; s++) {
            double v0 = s / (double) S, v1 = (s + 1) / (double) S;
            for (int i = 0; i < N; i++) {
                double a0 = i / (double) N, a1 = (i + 1) / (double) N;
                emb(a0, v0, off[s] + shift, age, plus, r, g, b, a);
                emb(a1, v0, off[s] + shift, age, plus, r, g, b, a);
                emb(a1, v1, off[s] + shift, age, plus, r, g, b, a);
                emb(a0, v1, off[s] + shift, age, plus, r, g, b, a);
            }
        }
    }

    private static void emb(double a, double b, double du, float age, boolean plus, float r, float g, float bl, float al) {
        double u = EM_U0 + (EM_U1 - EM_U0) * (a + du), v = EM_V0 + (EM_V1 - EM_V0) * b;
        u = Math.max(0.0, Math.min(1.0, u));
        double[] p = capePt(u, v, age, plus);
        Poly.tex(p[0], p[1], p[2] + 0.004, (float) (1.0 - a), (float) b, r, g, bl, al);
    }

    // =====================================================================================
    // BANDANA (head space: y points down, head box from y=-0.5 to 0, back is +z)
    // =====================================================================================
    private static double[] ring(double theta, double y, double a) {
        double c = Math.cos(theta), s = Math.sin(theta);
        double e = 2.0 / 6.0;
        return Poly.p(a * Math.signum(c) * Math.pow(Math.abs(c), e), y, a * Math.signum(s) * Math.pow(Math.abs(s), e));
    }

    /** True while the black & white bundle is being drawn (bandana palette). */
    static boolean mono;
    private static float monoAge;

    private static float[] bandColor(double vv, double theta) {
        if (anime != null) return Anime.band(anime, vv, theta, monoAge);
        if (qlux) {
            float[] c = Poly.mix(Poly.SNOW, Poly.PEARL, (float) (0.25 + 0.25 * Math.sin(theta * 3.0)));
            if (vv < 0.14 || vv > 0.86) return Poly.mix(Poly.SILVER, Poly.SNOW, 0.5f);
            double st = Math.sin(theta * 3.0 - monoAge * 0.10 + vv * 2.0);
            return Poly.mix(c, Poly.ICE, (float) Math.pow(Math.max(0.0, st), 8.0) * 0.8f);
        }
        if (mono) {
            double st = Math.sin(theta * 4.0 - monoAge * 0.12 + vv * 3.0);
            float w = (float) Math.pow(Math.max(0.0, st), 6.0);
            float[] c = Poly.mix(Poly.INK, Poly.CHAR, (float) (0.3 + 0.3 * Math.sin(theta * 2.0)));
            if (vv < 0.12 || vv > 0.88) c = Poly.mix(Poly.WHITE, Poly.GREY, 0.2f);
            return Poly.mix(c, Poly.WHITE, w * (vv < 0.12 || vv > 0.88 ? 0f : 0.95f));
        }
        float[] c;
        if (vv < 0.16 || vv > 0.84) c = Poly.mix(Poly.BLACK, Poly.DARK, 0.3f);
        else c = Poly.mix(Poly.DEEP, Poly.RED, (float) (0.35 + 0.35 * Math.sin(theta * 3.0)));
        if (Math.abs(vv - 0.5) < 0.05) c = Poly.mix(c, Poly.HOT, 0.4f);
        return c;
    }

    static void bandana(float age, float limbSwing) {
        if (extra != null) { ExtraPix.band(extra, age, limbSwing); return; }
        if (anime != null && anime.pixel) { XmasPix.band(anime, age, limbSwing); return; }
        monoAge = age;
        final int N = 56;
        final double a = 0.298, yTop = -0.43, yBot = -0.31;
        for (int i = 0; i < N; i++) {
            double t0 = 2 * Math.PI * i / N, t1 = 2 * Math.PI * (i + 1) / N;
            for (int k = 0; k < 4; k++) {
                double v0 = k / 4.0, v1 = (k + 1) / 4.0;
                double y0 = yTop + (yBot - yTop) * v0, y1 = yTop + (yBot - yTop) * v1;
                Poly.quad(ring(t0, y0, a), ring(t1, y0, a), ring(t1, y1, a), ring(t0, y1, a),
                        bandColor(v0, t0), bandColor(v0, t1), bandColor(v1, t1), bandColor(v1, t0), 1f);
            }
        }
        // knot
        Poly.ellipsoid(0, -0.37, a + 0.015, 0.045, 0.05, 0.035, anime != null ? anime.alt : qlux ? Poly.ICE : (mono ? Poly.WHITE : Poly.RED), anime != null ? anime.accent : qlux ? Poly.SNOW : (mono ? Poly.GREY : Poly.DEEP), 0.15f);
        // two waving tails
        for (int side = -1; side <= 1; side += 2) {
            final int S = 9;
            double len = 0.30;
            double[] prevL = null, prevR = null;
            float[] prevC = null;
            for (int j = 0; j <= S; j++) {
                double v = j / (double) S;
                double w = 0.028 + 0.030 * v;
                if (v > 0.78) w *= 1.0 - (v - 0.78) / 0.22 * 0.9;
                double cx = side * (0.018 + 0.05 * v) + Math.sin(age * 0.12 + v * 3.0 + side) * 0.022 * v;
                double cz = a + 0.035 + 0.03 * v + Math.sin(age * 0.10 + v * 2.5) * 0.028 * v + 0.14 * v * Math.min(1f, limbSwing);
                double cy = -0.36 + v * len;
                double[] l = Poly.p(cx - w, cy, cz), r = Poly.p(cx + w, cy, cz);
                float[] col = anime != null ? Anime.bandTail(anime, v, age) : qlux ? Poly.mix(Poly.SNOW, Poly.ICE, (float) Math.min(1.0, v * 0.9)) : mono ? Poly.mix(Poly.WHITE, Poly.INK, (float) Math.min(1.0, v * 1.1 + 0.15 * Math.sin(age * 0.2 + v * 6.0))) : Poly.mix(Poly.RED, Poly.DEEP, (float) v);
                if (v > 0.95) col = Poly.mix(col, Poly.BLACK, 0.7f);
                if (prevL != null) Poly.quad(prevL, prevR, r, l, prevC, prevC, col, col, 1f);
                prevL = l; prevR = r; prevC = col;
            }
        }
    }

    // =====================================================================================
    // PET "Ember": a small floating fox-dragon (faces -z, ~0.4 blocks long)
    // =====================================================================================
    static void pet(float age) {
        final float[] BELLY = Poly.DEEP;
        // body and head
        Poly.ellipsoid(0, 0, 0.02, 0.085, 0.075, 0.12, Poly.BLACK, BELLY, 0f);
        Poly.ellipsoid(0, -0.03, -0.15, 0.075, 0.068, 0.07, Poly.BLACK, Poly.DARK, 0f);
        Poly.ellipsoid(0, -0.015, -0.215, 0.034, 0.027, 0.045, Poly.DARK, Poly.BLACK, 0f);
        Poly.ellipsoid(0, -0.022, -0.255, 0.012, 0.010, 0.010, Poly.RED, Poly.RED, 0.6f);   // nose
        // ears
        for (int s = -1; s <= 1; s += 2) {
            Poly.cone(s * 0.045, -0.085, -0.15, s * 0.06, -0.20, -0.17, 0.034, Poly.BLACK, Poly.DEEP);
            Poly.cone(s * 0.045, -0.088, -0.152, s * 0.06, -0.185, -0.171, 0.020, Poly.RED, Poly.HOT);
            // glowing eyes
            Poly.ellipsoid(s * 0.040, -0.045, -0.208, 0.015, 0.015, 0.012, Poly.HOT, Poly.RED, 1f);
        }
        // tail: chain of spheres ending in a flame
        final int T = 7;
        for (int i = 0; i < T; i++) {
            double f = i / (double) (T - 1);
            double x = Math.sin(age * 0.14 - i * 0.7) * 0.035 * f * 2.0;
            double y = 0.02 - 0.17 * f * f - 0.02 * f;
            double z = 0.12 + 0.30 * f;
            double r = 0.052 * (1 - f * 0.65);
            if (i < T - 1) {
                Poly.ellipsoid(x, y, z, r, r, r * 1.15, Poly.BLACK, Poly.DEEP, 0f);
            } else {
                float pulse = 0.8f + 0.2f * (float) Math.sin(age * 0.5);
                Poly.ellipsoid(x, y - 0.01, z, r * 1.5 * pulse, r * 2.0 * pulse, r * 1.4 * pulse, Poly.HOT, Poly.RED, 1f);
            }
        }
        // small flapping wings
        double flap = Math.sin(age * 0.45) * 0.09;
        for (int s = -1; s <= 1; s += 2) {
            double[] root = Poly.p(s * 0.06, -0.06, 0.0);
            double[] t1 = Poly.p(s * 0.32, -0.20 - flap, 0.04);
            double[] t2 = Poly.p(s * 0.30, -0.04 - flap * 0.7, 0.14);
            double[] t3 = Poly.p(s * 0.16, 0.03 - flap * 0.2, 0.15);
            double[] back = Poly.p(s * 0.06, -0.02, 0.10);
            Poly.quad(root, t1, t2, back, Poly.DEEP, Poly.RED, Poly.RED, Poly.DEEP, 1f);
            Poly.quad(back, t2, t3, back, Poly.DEEP, Poly.RED, Poly.HOT, Poly.DEEP, 1f);
        }
    }
}

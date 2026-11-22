package dev.yaclient.fabric.cos;


import java.awt.image.BufferedImage;

/**
 * The anime bundles: Sakura (pink blossoms), Shinobi (night ninja), Neon (cyber idol) and Kitsune (shrine fox).
 * Smooth feather wings, a satin cape with an emblem, a band and a pixel pet each; palettes live here.
 */
final class Anime {
    private Anime() {}

    static final class Th {
        final String id; final int index;
        final float[] main, light, accent, alt, deep;
        final double[][] rows;     // wing feather rows (see CosmeticRender.ROWS)
        final boolean pixel;       // fully pixelated (Christmas bundles)
        Th(String id, int index, float[] main, float[] light, float[] accent, float[] alt, float[] deep, double[][] rows) {
            this(id, index, main, light, accent, alt, deep, rows, false);
        }
        Th(String id, int index, float[] main, float[] light, float[] accent, float[] alt, float[] deep, double[][] rows, boolean pixel) {
            this.id = id; this.index = index; this.main = main; this.light = light; this.accent = accent; this.alt = alt; this.deep = deep; this.rows = rows; this.pixel = pixel;
        }
    }

    // row: count, tStart, tEnd, angleStart, angleEnd, lenBase, lenAmp, width, zOffset
    private static final double[][] PETAL = {      // short, wide, rounded feathers like petals
            {7, 0.22, 1.00, -84, -8, 0.24, 0.26, 0.120, 0.000},
            {7, 0.12, 0.90, -88, -26, 0.19, 0.15, 0.105, 0.006},
            {6, 0.05, 0.92, -84, -16, 0.12, 0.08, 0.088, 0.012}};
    private static final double[][] BLADE = {      // long thin blades
            {8, 0.22, 1.00, -76, -4, 0.40, 0.52, 0.044, 0.000},
            {7, 0.12, 0.90, -82, -22, 0.30, 0.26, 0.040, 0.006},
            {6, 0.05, 0.95, -80, -14, 0.16, 0.12, 0.036, 0.012}};
    private static final double[][] CRYSTAL = {    // few wide angular shards
            {5, 0.25, 1.00, -80, -8, 0.36, 0.44, 0.100, 0.000},
            {5, 0.12, 0.90, -86, -24, 0.28, 0.22, 0.090, 0.006},
            {4, 0.05, 0.95, -82, -14, 0.14, 0.10, 0.075, 0.012}};
    private static final double[][] FLAME = {      // flame-like tongues
            {9, 0.22, 1.00, -90, -2, 0.30, 0.46, 0.062, 0.000},
            {8, 0.12, 0.92, -92, -26, 0.22, 0.24, 0.058, 0.006},
            {7, 0.05, 0.95, -86, -14, 0.12, 0.10, 0.050, 0.012}};

    static final Th SAKURA = new Th("sakura", 0,
            new float[]{1.00f, 0.82f, 0.89f, 0.25f}, new float[]{1.00f, 0.95f, 0.97f, 0.40f}, new float[]{0.97f, 0.50f, 0.70f, 0.40f},
            new float[]{1.00f, 0.72f, 0.84f, 0.70f}, new float[]{0.66f, 0.28f, 0.44f, 0.05f}, PETAL);
    static final Th SHINOBI = new Th("shinobi", 1,
            new float[]{0.09f, 0.10f, 0.19f, 0.05f}, new float[]{0.34f, 0.34f, 0.55f, 0.15f}, new float[]{0.55f, 0.40f, 0.95f, 0.70f},
            new float[]{0.84f, 0.86f, 1.00f, 0.85f}, new float[]{0.02f, 0.02f, 0.06f, 0.00f}, BLADE);
    static final Th NEON = new Th("neon", 2,
            new float[]{0.10f, 0.06f, 0.20f, 0.05f}, new float[]{0.28f, 0.12f, 0.45f, 0.15f}, new float[]{0.00f, 0.94f, 1.00f, 0.95f},
            new float[]{1.00f, 0.18f, 0.75f, 0.95f}, new float[]{0.02f, 0.01f, 0.06f, 0.00f}, CRYSTAL);
    static final Th KITSUNE = new Th("kitsune", 3,
            new float[]{0.98f, 0.95f, 0.91f, 0.30f}, new float[]{1.00f, 1.00f, 1.00f, 0.40f}, new float[]{0.88f, 0.17f, 0.10f, 0.35f},
            new float[]{1.00f, 0.78f, 0.25f, 0.70f}, new float[]{0.58f, 0.08f, 0.05f, 0.00f}, FLAME);
    static final Th SANTA = new Th("santa", 4,
            new float[]{0.84f, 0.16f, 0.16f, 0.25f}, new float[]{1f, 1f, 1f, 0.4f}, new float[]{1.00f, 0.82f, 0.30f, 0.7f},
            new float[]{0.18f, 0.62f, 0.29f, 0.3f}, new float[]{0.72f, 0.12f, 0.12f, 0f}, PETAL, true);
    static final Th FROST = new Th("frost", 5,
            new float[]{0.74f, 0.90f, 1.00f, 0.3f}, new float[]{1f, 1f, 1f, 0.4f}, new float[]{0.36f, 0.70f, 0.96f, 0.6f},
            new float[]{0.90f, 0.97f, 1.00f, 0.7f}, new float[]{0.30f, 0.55f, 0.85f, 0f}, PETAL, true);
    static final Th CLOWN = new Th("clown", 6,
            new float[]{0.91f, 0.15f, 0.17f, 0.25f}, new float[]{1f, 0.82f, 0.12f, 0.4f}, new float[]{0.18f, 0.44f, 0.91f, 0.6f},
            new float[]{0.18f, 0.70f, 0.29f, 0.3f}, new float[]{0.66f, 0.10f, 0.13f, 0f}, PETAL, true);
    private static final double[][] LEAF = {       // broad, rounded leaf-like feathers, finer than the petals
            {10, 0.20, 1.00, -86, -6, 0.26, 0.34, 0.082, 0.000},
            {9, 0.12, 0.92, -90, -24, 0.20, 0.20, 0.074, 0.006},
            {8, 0.05, 0.94, -86, -14, 0.12, 0.10, 0.064, 0.012}};
    static final Th LUCK = new Th("luck", 7,
            new float[]{0.05f, 0.45f, 0.20f, 0.25f}, new float[]{0.55f, 0.92f, 0.60f, 0.40f}, new float[]{1.00f, 0.83f, 0.29f, 0.70f},
            new float[]{1.00f, 0.95f, 0.65f, 0.80f}, new float[]{0.02f, 0.22f, 0.10f, 0.00f}, LEAF);
    static final Th[] ALL = {SAKURA, SHINOBI, KITSUNE, SANTA, FROST, CLOWN, LUCK};

    static Th byStyle(String id) { for (Th t : ALL) if (t.id.equals(id)) return t; return null; }

    private static float smooth(float t) { t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }

    // ------------------------------------------------------------------ wings and band colours
    static float[] feather(Th t, int row, float s, float edge, float glow, float rowF) {
        float[] c = row == 0 ? Poly.mix(t.main, t.accent, smooth(s * 1.05f) * 0.85f)
                : row == 1 ? Poly.mix(t.main, t.light, s * 0.75f) : Poly.mix(t.deep, t.main, 0.4f + 0.6f * s);
        float[] tip = Poly.mix(t.accent, t.alt, rowF);
        c = Poly.mix(c, tip, Math.max(0f, (s - 0.72f) / 0.28f) * (row == 0 ? 0.95f : 0.55f) * glow);
        float[] o = Poly.mix(c, t.light, edge * edge * 0.35f);
        o[3] = Math.max(o[3], (row == 0 ? 0.30f : 0.15f) + (s > 0.8f ? 0.35f * glow : 0f));
        return o;
    }

    static float[] band(Th t, double vv, double theta, float age) {
        if (vv < 0.14 || vv > 0.86) return Poly.mix(t.accent, t.alt, 0.35f);
        float[] c = Poly.mix(t.main, t.light, (float) (0.25 + 0.2 * Math.sin(theta * 3.0)));
        double st = Math.sin(theta * 3.0 - age * 0.10 + vv * 2.0);
        return Poly.mix(c, t.alt, (float) Math.pow(Math.max(0.0, st), 8.0) * 0.7f);
    }

    static float[] bandTail(Th t, double v, float age) {
        float[] c = Poly.mix(t.accent, t.main, (float) Math.min(1.0, v * 0.9));
        if (v > 0.85) c = Poly.mix(c, t.alt, 0.7f);
        return c;
    }

    // ------------------------------------------------------------------ cape
    private static float[] capeColor(Th t, double u, double v, float age) {
        float[] c;
        switch (t.index) {
            case 0: c = Poly.mix(t.main, t.light, (float) (v * 0.7)); break;
            case 1: c = Poly.mix(t.main, t.deep, (float) (v * 0.8)); break;
            case 2: c = Poly.mix(t.main, t.deep, (float) (v * 0.6)); break;
            default: c = Poly.mix(t.main, t.light, (float) (v * 0.5)); break;
        }
        double fold = 0.5 + 0.5 * Math.sin(u * Math.PI * 6 + v * 1.5);
        c = Poly.mix(c, t.index == 1 || t.index == 2 ? t.light : t.light, (float) (fold * (t.index == 1 || t.index == 2 ? 0.10 : 0.22)));
        double edge = Math.min(Math.min(u, 1 - u) * CosmeticRender.CAPE_W / 0.028, (1 - v) / 0.034);
        float trim = smooth((float) (1.0 - edge));
        c = Poly.mix(c, t.index == 3 ? t.alt : t.accent, trim * 0.9f);
        double hem = Math.sin(u * 9.0 - age * 0.07) * 0.5 + 0.5;
        float hg = smooth((float) ((v - 0.88) / 0.12));
        if (t.index == 3) {                       // kitsune: vermilion hem band with a gold line
            c = Poly.mix(c, t.accent, hg * 0.92f);
            c = Poly.mix(c, t.alt, smooth((float) (1.0 - Math.abs(v - 0.875) / 0.010)) * 0.9f);
        } else if (t.index == 2) {                // neon: glowing grid lines
            double gl = Math.max(Math.pow(Math.max(0, Math.sin(v * Math.PI * 14)), 40), Math.pow(Math.max(0, Math.sin(u * Math.PI * 7)), 60));
            c = Poly.mix(c, t.accent, (float) gl * 0.55f * (float) (0.4 + 0.6 * v));
            c = Poly.mix(c, t.alt, hg * (0.4f + 0.6f * (float) hem));
        } else {
            c = Poly.mix(c, t.alt, hg * (0.25f + 0.55f * (float) hem));
        }
        c[3] = Math.max(c[3], (t.index == 2 ? 0.12f : 0.25f) + hg * 0.5f);
        return c;
    }

    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    /** Kitsune: the whole cape is a Minecraft-style pixel grid with an orange outline and a zig-zag hem. */
    private static void pixelCape(float age, boolean plus) {
        final int NU = 11, NV = 16;
        final float[] orange = Vox.rgb(0xFF8A1E, 0.30f), deepOr = Vox.rgb(0xE2560F, 0.20f), cream = Vox.rgb(0xFFF1DC, 0.40f), white = Vox.rgb(0xFFFFFF, 0.40f);
        for (int i = 0; i < NU; i++) for (int j = 0; j < NV; j++) {
            float[] c;
            boolean border = i == 0 || i == NU - 1 || j == 0 || j == NV - 1;
            if (border) c = orange;
            else if (j >= NV - 3) c = ((i + j) % 2 == 0) ? deepOr : orange;          // zig-zag hem
            else if (i == 1 || i == NU - 2 || j == 1) c = cream;                       // inner line
            else c = noise(i, j) > 0.86f ? cream : white;
            float k = 0.95f + 0.05f * noise(j, i);
            float[] col = {Math.min(1f, c[0] * k), Math.min(1f, c[1] * k), Math.min(1f, c[2] * k), c[3]};
            double u0 = i / (double) NU, u1 = (i + 1) / (double) NU, v0 = j / (double) NV, v1 = (j + 1) / (double) NV;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), col, col, col, col, 1f);
        }
    }

    static void capeBase(Th t, float age, boolean plus) {
        if (t.pixel) { XmasPix.capeBase(t, age, plus); return; }
        if (t.index == 7) { LuckPix.capeBase(t, age, plus); return; }
        if (t.index == 3) { pixelCape(age, plus); return; }
        for (int i = 0; i < CosmeticRender.CAPE_C; i++) {
            for (int j = 0; j < CosmeticRender.CAPE_R; j++) {
                double u0 = i / (double) CosmeticRender.CAPE_C, u1 = (i + 1) / (double) CosmeticRender.CAPE_C;
                double v0 = j / (double) CosmeticRender.CAPE_R, v1 = (j + 1) / (double) CosmeticRender.CAPE_R;
                Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                        CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus),
                        capeColor(t, u0, v0, age), capeColor(t, u1, v0, age), capeColor(t, u1, v1, age), capeColor(t, u0, v1, age), 1f);
            }
        }
    }

    private static final double EU0 = 0.14, EU1 = 0.86;
    private static final double EH = (EU1 - EU0) * CosmeticRender.CAPE_W / CosmeticRender.CAPE_H;
    private static final double EV0 = 0.46 - EH / 2;     // slightly above the middle

    /** The emblem (textured square) with a soft pulse. */
    static void emblem(Th t, float age, boolean plus) {
        if (t.index == 7) return;      // Luck: the clover is part of the cape itself
        final int N = 12;
        float pulse = 0.90f + 0.10f * (float) Math.sin(age * 0.08);
        float tr = 1f, tg = 1f, tb = 1f;
        for (int i = 0; i < N; i++) for (int j = 0; j < N; j++) {
            double a0 = i / (double) N, a1 = (i + 1) / (double) N, b0 = j / (double) N, b1 = (j + 1) / (double) N;
            ev(a0, b0, age, plus, pulse); ev(a1, b0, age, plus, pulse); ev(a1, b1, age, plus, pulse); ev(a0, b1, age, plus, pulse);
        }
    }

    private static void ev(double a, double b, float age, boolean plus, float pulse) {
        double wave = Math.sin(age * 0.07 + a * 5.0) * 0.004;
        double u = EU0 + (EU1 - EU0) * a, v = EV0 + EH * b + wave;
        double[] p = CosmeticRender.capePt(Math.max(0, Math.min(1, u)), Math.max(0, Math.min(1, v)), age, plus);
        Poly.tex(p[0], p[1], p[2] + 0.005, (float) a, (float) b, pulse, pulse, pulse, 1f);
    }

    static void drawCape(Th t, float age, boolean plus) {
        Poly.begin();
        capeBase(t, age, plus);
        Poly.end();
        GlStateManager.enableTexture2D();
        bind(t.index);
        Poly.beginTex();
        emblem(t, age, plus);
        Poly.end();
        GlStateManager.disableTexture2D();
    }

    // ------------------------------------------------------------------ emblem textures
    private static final DynamicTexture[] TEX = new DynamicTexture[8];

    static void bind(int i) {
        if (TEX[i] == null) {
            BufferedImage im = AnimeArt.paint(i);
            TEX[i] = new DynamicTexture(im);
            GlStateManager.bindTexture(TEX[i].getGlTextureId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, i == 0 ? GL11.GL_LINEAR : GL11.GL_NEAREST);   // hard pixels (Sakura keeps its smooth art)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, i == 0 ? GL11.GL_LINEAR : GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        GlStateManager.bindTexture(TEX[i].getGlTextureId());
    }
}

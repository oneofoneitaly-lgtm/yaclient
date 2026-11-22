package dev.yaclient.cosmetics;

import java.util.ArrayList;
import java.util.List;

/**
 * Capes matching the 21 Vampire Wings colours: one flat-colour cape (never animated) with a drawing on it that
 * draws itself, holds, fades away and starts again. Drawn smooth (quads on the cape surface), 3 colours per drawing.
 */
final class CapeArt {
    private CapeArt() {}

    // name, background, colour 1, colour 2, colour 3
    private static final Object[][] T = {
        {"vampire", 0x0A0A0D, 0xC9C9D6, 0xE0203F, 0xFFFFFF}, {"vamp_crimson", 0x2A0509, 0xFF3B4E, 0xFFC2C8, 0xFFFFFF},
        {"vamp_scarlet", 0x3A0D08, 0xFF6A3D, 0xFFC24A, 0xFFFFFF}, {"vamp_orange", 0x3D1C06, 0xFFA02E, 0xFFE08A, 0xFFFFFF},
        {"vamp_amber", 0x3B2A06, 0xFFC93A, 0xFFF0A8, 0xFFFFFF}, {"vamp_gold", 0x40320C, 0xFFD84A, 0xFFF4C0, 0xFF5A5A},
        {"vamp_lime", 0x1E300A, 0xB6F04A, 0xE8FFB0, 0x7AD03A}, {"vamp_green", 0x0B2814, 0x4AE07A, 0xB8FFD0, 0xFFD84A},
        {"vamp_emerald", 0x052820, 0x2EE0A8, 0xB5FFEA, 0xFFFFFF}, {"vamp_teal", 0x072E2E, 0x35E0DC, 0xB5FFFC, 0xFFFFFF},
        {"vamp_cyan", 0x0A3340, 0x5FE6FF, 0xD0F8FF, 0xFFFFFF}, {"vamp_sky", 0x12304E, 0x7FC4FF, 0xE0F2FF, 0xFFFFFF},
        {"vamp_blue", 0x0C1A44, 0x5C8CFF, 0xC8D8FF, 0xFFD84A}, {"vamp_indigo", 0x120F3C, 0x8F7CFF, 0xFFE9A0, 0xFFFFFF},
        {"vamp_violet", 0x22103F, 0xB67CFF, 0xF0DCFF, 0xFFFFFF}, {"vamp_purple", 0x1A0B2E, 0xA45CE8, 0xEBD0FF, 0xFFD84A},
        {"vamp_magenta", 0x3A0F30, 0xFF5CD0, 0xFFC8F0, 0xFFFFFF}, {"vamp_pink", 0x4A1A38, 0xFF8CC8, 0xFFE0F0, 0xFFE070},
        {"vamp_rose", 0x40101F, 0xFF6A8E, 0xFFD0DC, 0xFFFFFF}, {"vamp_white", 0xE4E6EE, 0x7A86A8, 0xFFC93A, 0x4A5170},
        {"vamp_silver", 0x2C2F38, 0xC8D0E0, 0x7FA0FF, 0xFFFFFF}
    };

    private static final String[] TITLES = {"Bat", "Heart", "Flame", "Sun", "Bolt", "Crown", "Clover", "Pine", "Gem", "Waves", "Snowflake", "Cloud", "Compass", "Moon", "Eye", "Hexagram", "Butterfly", "Flower", "Paw", "Angel", "Sword"};
    /** Shop name of the cape: "Geometric <drawing> Cape". */
    static String title(String id) { int i = idx(id); return i < 0 ? id : "Geometric " + TITLES[i] + " Cape"; }
    static String[] names() { String[] n = new String[T.length]; for (int i = 0; i < T.length; i++) n[i] = (String) T[i][0]; return n; }
    /** Background colour of a Geometric cape, or -1. */
    static int bg(String id) { int i = idx(id); return i < 0 ? -1 : (Integer) T[i][1]; }
    static boolean has(String id) { return idx(id) >= 0; }
    private static int idx(String id) { for (int i = 0; i < T.length; i++) if (T[i][0].equals(id)) return i; return -1; }

    // ------------------------------------------------------------------ strokes
    private static final class S {
        double[] x, y; int col; double delay, w, r; int kind; boolean closed;   // kind 0 line, 1 disc, 2 spark
    }
    private static final List<S> L = new ArrayList<S>();
    private static double nextDelay;

    private static void pl(int col, double d, double w, boolean closed, double... xy) {
        S s = new S(); s.kind = 0; s.col = col; s.delay = d; s.w = w; s.closed = closed;
        int n = xy.length / 2; s.x = new double[n]; s.y = new double[n];
        for (int i = 0; i < n; i++) { s.x[i] = xy[2 * i]; s.y[i] = xy[2 * i + 1]; }
        L.add(s);
    }
    private static void line(int col, double d, double w, double x0, double y0, double x1, double y1) { pl(col, d, w, false, x0, y0, x1, y1); }
    private static void pts(int col, double d, double w, boolean closed, List<double[]> p) {
        double[] xy = new double[p.size() * 2];
        for (int i = 0; i < p.size(); i++) { xy[2 * i] = p.get(i)[0]; xy[2 * i + 1] = p.get(i)[1]; }
        pl(col, d, w, closed, xy);
    }
    private static void disc(int col, double d, double cx, double cy, double r) { S s = new S(); s.kind = 1; s.col = col; s.delay = d; s.x = new double[]{cx}; s.y = new double[]{cy}; s.r = r; L.add(s); }
    private static void spark(int col, double d, double cx, double cy, double r) { S s = new S(); s.kind = 2; s.col = col; s.delay = d; s.x = new double[]{cx}; s.y = new double[]{cy}; s.r = r; L.add(s); }
    private static List<double[]> arcP(double cx, double cy, double rx, double ry, double a0, double a1, int n, double rot) {
        List<double[]> p = new ArrayList<double[]>();
        for (int i = 0; i <= n; i++) {
            double a = Math.toRadians(a0 + (a1 - a0) * i / n), ex = Math.cos(a) * rx, ey = Math.sin(a) * ry;
            p.add(new double[]{cx + ex * Math.cos(rot) - ey * Math.sin(rot), cy + ex * Math.sin(rot) + ey * Math.cos(rot)});
        }
        return p;
    }
    private static void ring(int col, double d, double w, double cx, double cy, double r) { pts(col, d, w, true, arcP(cx, cy, r, r, 0, 360 - 360.0 / 40, 39, 0)); }
    private static void ell(int col, double d, double w, double cx, double cy, double rx, double ry, double rotDeg) { pts(col, d, w, true, arcP(cx, cy, rx, ry, 0, 360 - 360.0 / 36, 35, Math.toRadians(rotDeg))); }
    private static void tear(int col, double d, double w, double cx, double cy, double h, double wd) {
        List<double[]> p = new ArrayList<double[]>(); int n = 14;
        for (int k = 0; k <= n; k++) { double s = k / (double) n; p.add(new double[]{cx - wd * Math.pow(Math.sin(Math.PI * s), 0.75) * (0.4 + 0.6 * s), cy + h - s * 2 * h}); }
        for (int k = n - 1; k >= 1; k--) { double s = k / (double) n; p.add(new double[]{cx + wd * Math.pow(Math.sin(Math.PI * s), 0.75) * (0.4 + 0.6 * s), cy + h - s * 2 * h}); }
        pts(col, d, w, true, p);
    }

    // ------------------------------------------------------------------ the 21 drawings (x right, y UP, ~ +-0.27)
    private static void emblem(String id) {
        L.clear();
        final double W = 0.015;
        if (id.equals("vampire")) {                                           // bat and blood drops
            disc(0, 10, 0, 0.03, 0.04);
            pl(0, 14, W, false, -0.03, 0.06, -0.025, 0.12, -0.002, 0.07); pl(0, 14, W, false, 0.03, 0.06, 0.025, 0.12, 0.002, 0.07);
            for (int sd = -1; sd <= 1; sd += 2)
                pl(0, 16, W, true, sd * 0.04, 0.05, sd * 0.11, 0.13, sd * 0.20, 0.10, sd * 0.27, 0.16, sd * 0.25, 0.0, sd * 0.20, -0.05, sd * 0.15, -0.01, sd * 0.10, -0.08, sd * 0.05, -0.04);
            tear(1, 40, 0.01, -0.06, -0.19, 0.04, 0.022); tear(1, 52, 0.01, 0, -0.24, 0.05, 0.026); tear(1, 64, 0.01, 0.06, -0.19, 0.04, 0.022);
            spark(2, 70, -0.20, 0.20, 0.035); spark(2, 85, 0.22, -0.12, 0.03);
            disc(1, 36, -0.012, 0.01, 0.007); disc(1, 36, 0.012, 0.01, 0.007);
        } else if (id.equals("vamp_crimson")) {                               // heart + heartbeat line
            for (int k = 0; k < 2; k++) {
                List<double[]> p = new ArrayList<double[]>(); double sc = k == 0 ? 0.0105 : 0.0062;
                for (int i = 0; i < 48; i++) { double t = Math.PI * 2 * i / 48; p.add(new double[]{16 * Math.pow(Math.sin(t), 3) * sc, (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) * sc + 0.07 - (k == 1 ? 0.004 : 0)}); }
                pts(k == 0 ? 0 : 1, 10 + k * 16, W, true, p);
            }
            pl(2, 55, 0.012, false, -0.25, -0.17, -0.12, -0.17, -0.08, -0.10, -0.03, -0.26, 0.02, -0.06, 0.06, -0.17, 0.25, -0.17);
            spark(2, 80, 0.15, 0.2, 0.035); spark(2, 92, -0.17, 0.12, 0.03);
        } else if (id.equals("vamp_scarlet")) {                               // flame
            tear(0, 8, W, 0, 0.0, 0.22, 0.12); tear(1, 22, W, -0.12, -0.05, 0.14, 0.07); tear(1, 30, W, 0.12, -0.05, 0.14, 0.07); tear(2, 40, W, 0, -0.07, 0.12, 0.06);
            spark(2, 70, -0.18, 0.18, 0.03); spark(2, 85, 0.18, 0.2, 0.025); spark(2, 98, 0.0, 0.27, 0.03);
        } else if (id.equals("vamp_orange")) {                                // sun
            disc(1, 6, 0, 0.02, 0.07); ring(0, 12, W, 0, 0.02, 0.115);
            for (int k = 0; k < 12; k++) { double a = Math.PI * 2 * k / 12, r0 = 0.15, r1 = k % 2 == 0 ? 0.24 : 0.2; line(k % 2 == 0 ? 0 : 1, 24 + k * 3, W, Math.cos(a) * r0, 0.02 + Math.sin(a) * r0, Math.cos(a) * r1, 0.02 + Math.sin(a) * r1); }
            spark(2, 80, 0, 0.02, 0.05);
        } else if (id.equals("vamp_amber")) {                                 // lightning bolt
            pl(0, 6, W, true, 0.06, 0.25, -0.10, 0.0, 0.0, 0.0, -0.07, -0.25, 0.11, 0.04, 0.01, 0.04);
            pl(1, 36, 0.01, true, 0.045, 0.19, -0.065, 0.0, 0.01, 0.0, -0.04, -0.17, 0.07, 0.04, 0.0, 0.04);
            spark(2, 60, -0.17, 0.1, 0.04); spark(2, 72, 0.18, -0.1, 0.035); spark(2, 84, -0.13, -0.17, 0.03); spark(2, 94, 0.16, 0.18, 0.03);
        } else if (id.equals("vamp_gold")) {                                  // crown
            pl(0, 6, W, true, -0.20, -0.10, -0.22, 0.10, -0.10, 0.0, 0.0, 0.16, 0.10, 0.0, 0.22, 0.10, 0.20, -0.10);
            line(0, 36, W, -0.20, -0.16, 0.20, -0.16); line(1, 44, 0.01, -0.21, -0.10, 0.21, -0.10);
            disc(2, 50, -0.22, 0.12, 0.028); disc(2, 56, 0.0, 0.18, 0.028); disc(2, 62, 0.22, 0.12, 0.028);
            disc(1, 68, -0.07, -0.13, 0.016); disc(1, 72, 0.0, -0.13, 0.016); disc(1, 76, 0.07, -0.13, 0.016);
            spark(2, 84, -0.2, 0.22, 0.035); spark(2, 94, 0.2, -0.24, 0.03);
        } else if (id.equals("vamp_lime")) {                                  // four-leaf clover
            double[][] c = {{-0.075, 0.075}, {0.075, 0.075}, {-0.075, -0.075}, {0.075, -0.075}};
            for (int k = 0; k < 4; k++) { ring(0, 8 + k * 8, W, c[k][0], c[k][1], 0.075); ring(1, 40 + k * 4, 0.01, c[k][0] * 1.1, c[k][1] * 1.1, 0.035); }
            pl(2, 56, W, false, 0.0, -0.03, 0.02, -0.14, 0.06, -0.25);
            spark(2, 80, -0.2, 0.2, 0.03); spark(2, 92, 0.2, -0.05, 0.03);
        } else if (id.equals("vamp_green")) {                                 // pine tree
            pl(0, 6, W, true, 0.0, 0.2, -0.09, 0.09, 0.09, 0.09); pl(0, 16, W, true, 0.0, 0.11, -0.13, -0.04, 0.13, -0.04); pl(0, 26, W, true, 0.0, 0.01, -0.17, -0.16, 0.17, -0.16);
            pl(2, 40, W, true, -0.03, -0.16, -0.03, -0.25, 0.03, -0.25, 0.03, -0.16);
            disc(2, 50, 0.0, 0.25, 0.025); spark(1, 60, 0, 0.25, 0.06); disc(1, 66, -0.05, 0.05, 0.014); disc(1, 72, 0.07, -0.08, 0.014); disc(1, 78, -0.09, -0.1, 0.014);
            spark(2, 90, -0.2, 0.18, 0.03); spark(2, 98, 0.2, 0.12, 0.03);
        } else if (id.equals("vamp_emerald")) {                               // gem
            pl(0, 6, W, true, -0.16, 0.08, -0.08, 0.18, 0.08, 0.18, 0.16, 0.08, 0.0, -0.2);
            line(0, 28, W, -0.16, 0.08, 0.16, 0.08); pl(1, 36, 0.011, false, -0.08, 0.18, -0.04, 0.08, 0.0, -0.2); pl(1, 42, 0.011, false, 0.08, 0.18, 0.04, 0.08, 0.0, -0.2);
            line(1, 48, 0.011, -0.04, 0.08, 0.0, 0.18); line(1, 52, 0.011, 0.04, 0.08, 0.0, 0.18);
            spark(2, 64, -0.08, 0.14, 0.05); spark(2, 76, 0.18, -0.12, 0.035); spark(2, 88, -0.2, -0.1, 0.03);
        } else if (id.equals("vamp_teal")) {                                  // waves + bubbles
            for (int k = 0; k < 3; k++) {
                List<double[]> p = new ArrayList<double[]>(); for (int i = 0; i <= 24; i++) { double x = -0.22 + 0.44 * i / 24; p.add(new double[]{x, 0.11 - 0.11 * k + Math.sin(x * 34 + k) * 0.032}); }
                pts(k == 1 ? 1 : 0, 6 + k * 14, W, false, p);
            }
            ring(2, 54, 0.01, -0.1, -0.17, 0.03); ring(2, 62, 0.01, 0.05, -0.21, 0.04); ring(2, 70, 0.01, 0.14, -0.16, 0.025); ring(2, 78, 0.01, -0.03, 0.2, 0.03); ring(2, 84, 0.01, 0.12, 0.22, 0.02);
        } else if (id.equals("vamp_cyan")) {                                  // snowflake
            for (int k = 0; k < 6; k++) {
                double a = Math.PI / 6 + Math.PI * 2 * k / 6, ca = Math.cos(a), sa = Math.sin(a), d = 6 + k * 5;
                line(0, d, W, 0, 0, ca * 0.23, sa * 0.23);
                for (double rr : new double[]{0.12, 0.18}) {
                    double bx = ca * rr, by = sa * rr, l = rr == 0.12 ? 0.06 : 0.045;
                    line(1, d + 14, 0.011, bx, by, bx + Math.cos(a + 0.9) * l, by + Math.sin(a + 0.9) * l); line(1, d + 14, 0.011, bx, by, bx + Math.cos(a - 0.9) * l, by + Math.sin(a - 0.9) * l);
                }
            }
            ring(2, 50, 0.011, 0, 0, 0.035); disc(2, 58, 0, 0, 0.015); spark(2, 80, 0.2, 0.2, 0.03); spark(2, 92, -0.2, -0.2, 0.03);
        } else if (id.equals("vamp_sky")) {                                   // cloud + rain
            ring(0, 6, W, -0.09, 0.04, 0.07); ring(0, 14, W, 0.0, 0.09, 0.09); ring(0, 22, W, 0.1, 0.04, 0.07); line(0, 30, W, -0.15, -0.03, 0.17, -0.03);
            for (int k = 0; k < 5; k++) { double x = -0.13 + 0.065 * k; line(1, 44 + k * 7, 0.012, x, -0.09, x - 0.02, -0.15 - (k % 2) * 0.04); disc(2, 52 + k * 7, x - 0.025, -0.2 - (k % 2) * 0.04, 0.012); }
            spark(2, 90, 0.2, 0.2, 0.03);
        } else if (id.equals("vamp_blue")) {                                  // compass
            ring(0, 6, W, 0, 0, 0.23); ring(1, 18, 0.01, 0, 0, 0.195);
            pl(2, 28, 0.013, true, 0, 0.19, 0.035, 0.035, 0.19, 0, 0.035, -0.035, 0, -0.19, -0.035, -0.035, -0.19, 0, -0.035, 0.035);
            pl(0, 44, 0.011, true, 0.11, 0.11, 0.02, 0.0, 0.11, -0.11, 0.0, -0.02, -0.11, -0.11, -0.02, 0, -0.11, 0.11, 0, 0.02);
            disc(1, 56, 0, 0, 0.018); spark(1, 76, 0, 0.19, 0.04);
        } else if (id.equals("vamp_indigo")) {                                // crescent moon + stars
            List<double[]> o = arcP(-0.02, 0.0, 0.17, 0.17, 50, 310, 28, 0), in = arcP(0.05, 0.0, 0.136, 0.136, 287, 73 + 360, 28, 0);
            o.addAll(in); pts(0, 6, W, true, o);
            for (int k = 0; k < 4; k++) { double[][] sp = {{0.17, 0.15}, {0.2, -0.02}, {0.12, -0.2}, {-0.15, -0.22}}; double s = k % 2 == 0 ? 0.04 : 0.03; disc(1, 30 + k * 10, sp[k][0], sp[k][1], 0.012); spark(1, 36 + k * 10, sp[k][0], sp[k][1], s); }
            spark(2, 84, -0.05, 0.0, 0.03);
        } else if (id.equals("vamp_violet")) {                                // eye
            List<double[]> a = new ArrayList<double[]>();
            for (int i = 0; i <= 20; i++) { double x = -0.23 + 0.46 * i / 20; a.add(new double[]{x, 0.12 * (1 - Math.pow(x / 0.23, 2))}); }
            for (int i = 19; i >= 1; i--) { double x = -0.23 + 0.46 * i / 20; a.add(new double[]{x, -0.12 * (1 - Math.pow(x / 0.23, 2))}); }
            pts(0, 6, W, true, a); ring(1, 30, W, 0, 0, 0.075); disc(2, 44, 0, 0, 0.035); disc(1, 54, 0.02, 0.025, 0.012);
            for (int k = -2; k <= 2; k++) line(0, 60 + (k + 2) * 5, 0.011, k * 0.07, 0.12 * (1 - Math.pow(k * 0.07 / 0.23, 2)) + 0.01, k * 0.085, 0.12 * (1 - Math.pow(k * 0.07 / 0.23, 2)) + 0.07);
            spark(2, 90, -0.2, -0.17, 0.03);
        } else if (id.equals("vamp_purple")) {                                // hexagram in a ring
            ring(0, 6, W, 0, 0, 0.23);
            pl(1, 20, W, true, 0, 0.18, -0.156, -0.09, 0.156, -0.09); pl(2, 34, W, true, 0, -0.18, -0.156, 0.09, 0.156, 0.09);
            disc(3, 50, 0, 0, 0.022); spark(2, 70, 0, 0.18, 0.04); spark(2, 78, 0, -0.18, 0.04); spark(2, 90, 0.2, 0.0, 0.03);
        } else if (id.equals("vamp_magenta")) {                               // butterfly
            for (int sd = -1; sd <= 1; sd += 2) {
                ell(0, 8, W, sd * 0.11, 0.07, 0.11, 0.08, sd * 30); ell(1, 20, W, sd * 0.085, -0.09, 0.08, 0.06, -sd * 25);
                disc(2, 40, sd * 0.13, 0.08, 0.02); disc(2, 46, sd * 0.09, -0.09, 0.014);
            }
            line(2, 30, W, 0, 0.13, 0, -0.16); line(2, 52, 0.01, 0, 0.13, -0.05, 0.21); line(2, 52, 0.01, 0, 0.13, 0.05, 0.21);
            spark(1, 80, 0.2, 0.2, 0.03); spark(1, 92, -0.2, -0.15, 0.03);
        } else if (id.equals("vamp_pink")) {                                  // flower
            for (int k = 0; k < 5; k++) { double a = Math.PI / 2 + Math.PI * 2 * k / 5; ell(0, 8 + k * 6, W, Math.cos(a) * 0.1, 0.07 + Math.sin(a) * 0.1, 0.09, 0.05, Math.toDegrees(a)); }
            disc(2, 46, 0, 0.07, 0.045); ring(1, 52, 0.01, 0, 0.07, 0.06);
            line(3, 58, W, 0, -0.04, 0.0, -0.25); ell(3, 66, 0.011, 0.07, -0.17, 0.07, 0.03, 30); ell(3, 72, 0.011, -0.07, -0.12, 0.06, 0.028, -30);
            spark(1, 88, 0.2, 0.2, 0.03);
        } else if (id.equals("vamp_rose")) {                                  // paw print
            ell(0, 6, W, 0, -0.07, 0.11, 0.09, 0);
            double[][] t = {{-0.15, 0.05}, {-0.06, 0.12}, {0.06, 0.12}, {0.15, 0.05}};
            for (int k = 0; k < 4; k++) { ell(0, 18 + k * 8, W, t[k][0], t[k][1], 0.045, 0.055, k < 2 ? 20 : -20); disc(1, 50 + k * 6, t[k][0], t[k][1], 0.028); }
            disc(1, 46, 0, -0.07, 0.07); spark(2, 80, -0.2, -0.2, 0.03); spark(2, 92, 0.2, 0.22, 0.03);
        } else if (id.equals("vamp_white")) {                                 // halo + angel wings
            ell(1, 6, W, 0, 0.2, 0.09, 0.03, 0);
            for (int sd = -1; sd <= 1; sd += 2) {
                pl(0, 16, W, false, sd * 0.03, 0.0, sd * 0.12, 0.07, sd * 0.22, 0.06, sd * 0.27, 0.0);
                line(0, 30, W, sd * 0.08, 0.055, sd * 0.12, -0.08); line(0, 36, W, sd * 0.15, 0.07, sd * 0.2, -0.09); line(0, 42, W, sd * 0.22, 0.055, sd * 0.27, -0.06);
            }
            disc(2, 52, 0, 0.06, 0.02); spark(1, 80, 0.2, 0.2, 0.035); spark(1, 92, -0.18, -0.18, 0.03);
        } else {                                                              // silver: sword
            pl(0, 6, W, true, 0, 0.27, 0.03, 0.21, 0.03, -0.05, -0.03, -0.05, -0.03, 0.21);
            line(1, 26, 0.011, 0, 0.22, 0, -0.04); pl(0, 34, W, true, -0.12, -0.05, 0.12, -0.05, 0.12, -0.08, -0.12, -0.08);
            line(2, 44, W, 0, -0.08, 0, -0.17); disc(1, 52, 0, -0.2, 0.032);
            spark(2, 64, 0.03, 0.14, 0.04); spark(2, 74, -0.03, 0.04, 0.03); spark(2, 86, 0.0, 0.24, 0.035);
        }
    }

    // ------------------------------------------------------------------ rendering
    private static double[] onCape(double x, double y, float age, boolean plus, double lift) {
        double v = 0.40 - y / CosmeticRender.CAPE_H;
        double u = 0.5 + x / (CosmeticRender.CAPE_W * (1 + 0.06 * v));
        double[] p = CosmeticRender.capePt(u, v, age, plus);
        p[2] += lift;
        return p;
    }
    private static double sat(double v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    private static float[] col(int hex) { return Vox.rgb(hex, 0.55f); }

    static void draw(String id, float age, boolean plus) {
        int ti = idx(id); if (ti < 0) return;
        float[] bg = Vox.rgb((Integer) T[ti][1], 0.45f);
        for (int i = 0; i < CosmeticRender.CAPE_C; i++) for (int j = 0; j < CosmeticRender.CAPE_R; j++) {
            double u0 = i / (double) CosmeticRender.CAPE_C, u1 = (i + 1) / (double) CosmeticRender.CAPE_C;
            double v0 = j / (double) CosmeticRender.CAPE_R, v1 = (j + 1) / (double) CosmeticRender.CAPE_R;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), bg, bg, bg, bg, 1f);
        }
        emblem(id);
        for (S q : L) { for (int i = 0; i < q.x.length; i++) { q.x[i] *= 1.1; q.y[i] *= 1.1; } q.w *= 1.1; q.r *= 1.1; }
        double t = age % 150.0;
        double fade = 1 - sat((t - 120) / 22.0);
        int layer = 0;
        for (S s : L) {
            double lt = t - s.delay; if (lt <= 0) continue;
            float[] c = col(s.col >= 3 ? (id.equals("vamp_pink") ? 0x6AD88A : 0xFFFFFF) : (Integer) T[ti][2 + s.col]);
            double lift = 0.004 + (layer++) * 0.00035;
            if (s.kind == 2) {                                                              // sparkle: pops and vanishes
                if (lt > 34) continue;
                double k = Math.sin(Math.PI * lt / 34.0) * s.r, th = k * 0.28;
                double cx = s.x[0], cy = s.y[0];
                quad(c, lift, age, plus, cx - k, cy, cx, cy + th, cx + k, cy, cx, cy - th);
                quad(c, lift, age, plus, cx, cy - k, cx + th, cy, cx, cy + k, cx - th, cy);
                continue;
            }
            if (fade <= 0.01) continue;
            float[] cc = Poly.mix(bg, c, (float) fade);
            if (s.kind == 1) {                                                              // filled disc pops in
                double r = s.r * sat(lt / 12.0);
                for (int k = 0; k < 14; k++) {
                    double a0 = Math.PI * 2 * k / 14, a1 = Math.PI * 2 * (k + 1) / 14;
                    Poly.tri(onCape(s.x[0], s.y[0], age, plus, lift), onCape(s.x[0] + Math.cos(a0) * r, s.y[0] + Math.sin(a0) * r, age, plus, lift),
                            onCape(s.x[0] + Math.cos(a1) * r, s.y[0] + Math.sin(a1) * r, age, plus, lift), cc, cc, cc, 1f);
                }
                continue;
            }
            int n = s.x.length, segs = s.closed ? n : n - 1;
            double total = 0; double[] sl = new double[segs];
            for (int i = 0; i < segs; i++) { int j = (i + 1) % n; sl[i] = Math.hypot(s.x[j] - s.x[i], s.y[j] - s.y[i]); total += sl[i]; }
            double shown = total * sat(lt / (14 + total * 60));
            double acc = 0;
            for (int i = 0; i < segs && acc < shown; i++) {
                int j = (i + 1) % n; double f1 = Math.min(1, (shown - acc) / Math.max(sl[i], 1e-9));
                double x0 = s.x[i], y0 = s.y[i], x1 = x0 + (s.x[j] - x0) * f1, y1 = y0 + (s.y[j] - y0) * f1;
                seg(cc, lift, age, plus, x0, y0, x1, y1, s.w * 0.5);
                acc += sl[i];
            }
        }
    }

    private static void quad(float[] c, double lift, float age, boolean plus, double... p) {
        Poly.quad(onCape(p[0], p[1], age, plus, lift), onCape(p[2], p[3], age, plus, lift), onCape(p[4], p[5], age, plus, lift), onCape(p[6], p[7], age, plus, lift), c, c, c, c, 1f);
    }

    /** One stroke segment (a ribbon), split so it follows the cape's curvature. */
    private static void seg(float[] c, double lift, float age, boolean plus, double x0, double y0, double x1, double y1, double hw) {
        double dx = x1 - x0, dy = y1 - y0, l = Math.hypot(dx, dy); if (l < 1e-6) return;
        double nx = -dy / l * hw, ny = dx / l * hw;
        int n = Math.max(1, (int) Math.ceil(l / 0.04));
        for (int k = 0; k < n; k++) {
            double a = k / (double) n, b = (k + 1) / (double) n;
            double ax = x0 + dx * a, ay = y0 + dy * a, bx = x0 + dx * b, by = y0 + dy * b;
            // slightly overlap ends so corners stay closed
            quad(c, lift, age, plus, ax + nx, ay + ny, bx + nx, by + ny, bx - nx, by - ny, ax - nx, ay - ny);
        }
        // round-ish joint cap
        quad(c, lift, age, plus, x1 + hw, y1, x1, y1 + hw, x1 - hw, y1, x1, y1 - hw);
    }
}

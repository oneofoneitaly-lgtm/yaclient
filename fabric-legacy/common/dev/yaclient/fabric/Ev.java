package dev.yaclient.fabric;

/** Input event shim: lets code written for the 1.21.9+ event objects run on older versions. */
public final class Ev {
    private final double x, y; private final int b, k, sc, m, cp;
    public Ev(double x, double y, int b, int k, int sc, int m, int cp) { this.x = x; this.y = y; this.b = b; this.k = k; this.sc = sc; this.m = m; this.cp = cp; }
    public double x() { return x; }
    public double y() { return y; }
    public int button() { return b; }
    public int key() { return k; }
    public int scancode() { return sc; }
    public int modifiers() { return m; }
    public int codepoint() { return cp; }
}

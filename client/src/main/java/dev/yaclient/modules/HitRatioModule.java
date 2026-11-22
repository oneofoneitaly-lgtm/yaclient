package dev.yaclient.modules;

/** Hits you landed against hits you took, and the ratio: who is winning the fight. */
public class HitRatioModule extends PillModule {
    public HitRatioModule() { super("hit_ratio", Category.PVP, false, 0.45f, 0.70f); }

    @Override
    protected String text() {
        int h = HitTracker.hits(), t = HitTracker.taken();
        if (h + t == 0) return "";
        return h + " : " + t;
    }

    @Override
    protected int autoColor() { return HitTracker.hits() >= HitTracker.taken() ? 0xFF22C55E : 0xFFEF4444; }

    @Override protected String sample() { return "12 : 8"; }
}

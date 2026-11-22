package dev.yaclient.modules;

/** How many of your hits were critical hits (falling) and the percentage. */
public class CritCounterModule extends PillModule {
    public CritCounterModule() { super("crit_counter", Category.PVP, false, 0.45f, 0.66f); }

    @Override
    protected String text() {
        int h = HitTracker.hits();
        return h == 0 ? "" : "Crits " + HitTracker.crits() + " (" + Math.round(100f * HitTracker.crits() / h) + "%)";
    }

    @Override protected String sample() { return "Crits 6 (40%)"; }
}

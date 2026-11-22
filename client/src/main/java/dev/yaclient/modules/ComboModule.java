package dev.yaclient.modules;

/** Counts the hits in a row you land before being hit back. */
public class ComboModule extends PillModule {
    public ComboModule() { super("combo", Category.PVP, false, 0.45f, 0.62f); }

    @Override
    protected String text() {
        int c = HitTracker.combo();
        return c > 0 ? "Combo: " + c + (c == 1 ? " hit" : " hits") : "";
    }

    @Override
    protected String sample() { return "Combo: 4 hits"; }
}

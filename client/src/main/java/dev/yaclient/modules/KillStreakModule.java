package dev.yaclient.modules;

/** Kills this session and your current streak (resets when you get hit). */
public class KillStreakModule extends PillModule {
    public KillStreakModule() { super("kill_streak", Category.PVP, false, 0.45f, 0.74f); }

    @Override
    protected String text() {
        int k = HitTracker.kills();
        return k == 0 ? "" : "Kills " + k + "  Streak " + HitTracker.streak();
    }

    @Override protected String sample() { return "Kills 5  Streak 2"; }
}

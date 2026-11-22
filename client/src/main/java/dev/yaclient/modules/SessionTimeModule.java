package dev.yaclient.modules;

public class SessionTimeModule extends PillModule {
    private static final long START = System.currentTimeMillis();

    public SessionTimeModule() { super("session", Category.INFO, false, 0.008f, 0.32f); }

    @Override
    protected String text() {
        long s = (System.currentTimeMillis() - START) / 1000L;
        return String.format(java.util.Locale.ROOT, "Time Played: %02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
    }

    @Override
    protected String sample() { return "Time Played: 00:42:10"; }
}

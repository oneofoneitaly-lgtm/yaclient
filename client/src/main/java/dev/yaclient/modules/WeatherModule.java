package dev.yaclient.modules;

public class WeatherModule extends PillModule {
    public WeatherModule() { super("weather", Category.INFO, false, 0.99f, 0.28f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        if (mc.theWorld.isThundering()) return "Weather: Thunder";
        if (mc.theWorld.isRaining()) return "Weather: Rain";
        return "Weather: Clear";
    }

    @Override
    protected String sample() { return "Weather: Clear"; }
}

package dev.yaclient.modules;

/** Hides rain and thunder on your screen (client-side only). */
public class ClearWeatherModule extends Module {
    public ClearWeatherModule() { super("clear_weather", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.theWorld == null) return;
        mc.theWorld.setRainStrength(0f);
        mc.theWorld.setThunderStrength(0f);
    }
}

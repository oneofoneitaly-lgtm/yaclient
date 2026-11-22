package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Forces the time of day on your screen (client-side only, purely visual). */
public class TimeChangerModule extends Module {
    private final Setting.Num time = add(new Setting.Num("time", 0f, 24000f, 500f, 6000f, ""));

    public TimeChangerModule() { super("time_changer", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.theWorld != null) mc.theWorld.setWorldTime(Math.round(time.value));
    }
}

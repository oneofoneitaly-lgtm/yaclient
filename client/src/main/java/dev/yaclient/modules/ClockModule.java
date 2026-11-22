package dev.yaclient.modules;

import dev.yaclient.Setting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockModule extends PillModule {
    private final Setting.Bool h24 = add("general", new Setting.Bool("h24", true));
    private final Setting.Bool seconds = add("general", new Setting.Bool("seconds", false));
    private final Setting.Bool blink = add("general", new Setting.Bool("blink", false));
    private String text = "";

    public ClockModule() { super("clock", Category.INFO, false, 0.99f, 0.116f); }

    @Override
    protected String text() {
        String pat = (h24.value ? "HH" : "hh") + (blink.value && (System.currentTimeMillis() / 500) % 2 == 1 ? " " : ":") + "mm"
                + (seconds.value ? ":ss" : "") + (h24.value ? "" : " a");
        text = new SimpleDateFormat(pat).format(new Date());
        return text;
    }
}

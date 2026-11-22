package dev.yaclient.modules;

import dev.yaclient.Setting;
import org.lwjgl.input.Mouse;

/** Total mouse clicks since the game started. */
public class ClickCounterModule extends PillModule {
    private final Setting.Bool both = add("general", new Setting.Bool("both_buttons", true));
    private boolean lastL, lastR;
    private int total;

    public ClickCounterModule() { super("click_counter", Category.PVP, false, 0.008f, 0.30f); }

    @Override
    public void onRender() {
        if (mc == null || mc.currentScreen != null) { lastL = false; lastR = false; return; }
        boolean l = Mouse.isButtonDown(0), r = Mouse.isButtonDown(1);
        if (l && !lastL) total++;
        if (both.value && r && !lastR) total++;
        lastL = l;
        lastR = r;
    }

    @Override
    protected String text() { return "Clicks: " + total; }

    @Override
    protected String sample() { return "Clicks: 187"; }
}

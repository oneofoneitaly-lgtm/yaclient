package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Any text you want on screen. */
public class CustomTextModule extends PillModule {
    private final Setting.Text content = add("general", new Setting.Text("text", "Custom Text", 48));

    public CustomTextModule() { super("custom_text", Category.UTILITY, false, 0.40f, 0.012f); }

    @Override
    protected String text() { return content.value == null ? "" : content.value; }

    @Override
    protected String sample() { return "Custom Text"; }
}

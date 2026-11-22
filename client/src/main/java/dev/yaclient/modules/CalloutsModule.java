package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Keys that send a ready-made short message to your team chat ("low", "pot", "gapple", "help"). {hp} and {xyz} are filled in. */
public class CalloutsModule extends Module {
    private final Setting.Text prefix = add(new Setting.Text("prefix", "", 12));
    private final Setting.Key kLow = add(new Setting.Key("key_low", org.lwjgl.input.Keyboard.KEY_NUMPAD1));
    private final Setting.Text mLow = add(new Setting.Text("msg_low", "Low! {hp} hp", 60));
    private final Setting.Key kPot = add(new Setting.Key("key_pot", org.lwjgl.input.Keyboard.KEY_NUMPAD2));
    private final Setting.Text mPot = add(new Setting.Text("msg_pot", "Need pots", 60));
    private final Setting.Key kGap = add(new Setting.Key("key_gapple", org.lwjgl.input.Keyboard.KEY_NUMPAD3));
    private final Setting.Text mGap = add(new Setting.Text("msg_gapple", "Eating gapple", 60));
    private final Setting.Key kHelp = add(new Setting.Key("key_help", org.lwjgl.input.Keyboard.KEY_NUMPAD4));
    private final Setting.Text mHelp = add(new Setting.Text("msg_help", "Help at {xyz}", 60));
    private final boolean[] was = new boolean[4];
    private long lastSend;

    public CalloutsModule() { super("callouts", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc == null || mc.thePlayer == null || mc.currentScreen != null) return;
        Setting.Key[] k = {kLow, kPot, kGap, kHelp};
        Setting.Text[] m = {mLow, mPot, mGap, mHelp};
        for (int i = 0; i < 4; i++) {
            boolean d = k[i].code != 0 && k[i].down();
            if (d && !was[i] && System.currentTimeMillis() - lastSend > 1500) {
                String s = m[i].value == null ? "" : m[i].value;
                s = s.replace("{hp}", String.valueOf(Math.round(mc.thePlayer.getHealth() / 2f * 10f) / 10f))
                     .replace("{xyz}", (int) mc.thePlayer.posX + " " + (int) mc.thePlayer.posY + " " + (int) mc.thePlayer.posZ);
                String p = prefix.value == null ? "" : prefix.value.trim();
                if (!s.trim().isEmpty()) { mc.thePlayer.sendChatMessage((p.isEmpty() ? "" : p + " ") + s); lastSend = System.currentTimeMillis(); }
            }
            was[i] = d;
        }
    }
}

package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ScreenShotHelper;

import java.io.File;
import java.io.FileWriter;

/** Press the key to mark a moment: takes a screenshot and writes the time to yaclient-clips.txt (so you find the clip in your recording). */
public class ClipModule extends Module {
    private final Setting.Key key = add(new Setting.Key("key", org.lwjgl.input.Keyboard.KEY_F8));
    private final Setting.Bool shot = add(new Setting.Bool("screenshot", true));
    private boolean was;
    private final long started = System.currentTimeMillis();

    public ClipModule() { super("clip", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc == null || mc.thePlayer == null) return;
        boolean d = mc.currentScreen == null && key.down();
        if (d && !was) mark(mc);
        was = d;
    }

    private void mark(Minecraft mc) {
        try {
            long ms = System.currentTimeMillis() - started;
            String t = String.format("%02d:%02d:%02d", ms / 3600000, (ms / 60000) % 60, (ms / 1000) % 60);
            File f = new File(mc.mcDataDir, "yaclient-clips.txt");
            FileWriter w = new FileWriter(f, true);
            w.write(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "  +" + t + " into the session\n");
            w.close();
            if (shot.value) ScreenShotHelper.saveScreenshot(mc.mcDataDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cYA §7Clip marked at §f+" + t));
        } catch (Throwable ignored) { }
    }
}

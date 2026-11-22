package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

/** Chat helpers: fold repeated messages (x2, x3), hide messages with words you choose, show a nickname instead of your own name. */
public class ChatTweaksModule extends Module {
    private final Setting.Bool spam = add(new Setting.Bool("antispam", true));
    private final Setting.Bool filter = add(new Setting.Bool("filter", false));
    private final Setting.Text words = add(new Setting.Text("words", "", 120));
    private final Setting.Bool hide = add(new Setting.Bool("nickhider", false));
    private final Setting.Text nick = add(new Setting.Text("nick", "Player", 16));

    private static ChatTweaksModule inst;
    private static String lastText = "";
    private static int lastId = -1, repeats, nextId = 880000;

    public ChatTweaksModule() { super("chat_tweaks", Category.UTILITY, false); inst = this; }

    /** Called for every incoming chat line. Returns what to print, or null to drop it. */
    public static IChatComponent process(IChatComponent c, net.minecraft.client.gui.GuiNewChat chat, int[] idOut) {
        if (inst == null || !inst.enabled || c == null) return c;
        try {
            String plain = c.getUnformattedText();
            if (inst.filter.value && inst.words.value != null && !inst.words.value.trim().isEmpty()) {
                String low = plain.toLowerCase();
                for (String w : inst.words.value.split(",")) { w = w.trim().toLowerCase(); if (!w.isEmpty() && low.contains(w)) return null; }
            }
            if (inst.hide.value && Minecraft.getMinecraft().thePlayer != null) {
                String me = Minecraft.getMinecraft().thePlayer.getName();
                String f = c.getFormattedText();
                if (me != null && !me.isEmpty() && f.contains(me)) c = new ChatComponentText(f.replace(me, inst.nick.value == null || inst.nick.value.isEmpty() ? "Player" : inst.nick.value));
            }
            if (inst.spam.value && !plain.trim().isEmpty()) {
                if (plain.equals(lastText) && lastId >= 0) {
                    repeats++;
                    chat.deleteChatLine(lastId);
                    c = new ChatComponentText(c.getFormattedText() + " §7(x" + (repeats + 1) + ")");
                } else { lastText = plain; repeats = 0; }
                lastId = nextId++;
                if (nextId > 889999) nextId = 880000;
                idOut[0] = lastId;
            }
        } catch (Throwable ignored) { }
        return c;
    }
}

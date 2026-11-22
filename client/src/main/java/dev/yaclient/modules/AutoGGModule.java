package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ChatLine;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

/** Says "gg" in chat when a game ends (it reads the game-end messages the server sends; never spams). */
public class AutoGGModule extends Module {
    private final Setting.Choice message = add(new Setting.Choice("message", 0, "gg", "gg_wp", "good_game"));
    private final Setting.Num delay = add(new Setting.Num("delay", 0f, 5f, 1f, 1f, "s"));

    private static final String[] TRIGGERS = {
            "you won", "has won the game", "game over", "victory!", "winner!", "winners:", "1st killer", "1st place",
            "ha vinto", "hai vinto", "vittoria", "partita terminata", "fine partita", "ganador", "has ganado", "sieger", "gewonnen"};

    private Field linesField;
    private boolean searched;
    private ChatLine lastSeen;
    private int pending = -1;
    private long lastSent;

    public AutoGGModule() { super("autogg", Category.UTILITY, false); }

    @SuppressWarnings("unchecked")
    private List<ChatLine> lines() {
        try {
            GuiNewChat chat = mc.ingameGUI.getChatGUI();
            if (!searched) {
                searched = true;
                for (Field f : GuiNewChat.class.getDeclaredFields()) {
                    if (f.getType() != List.class) continue;
                    f.setAccessible(true);
                    List<?> l = (List<?>) f.get(chat);
                    if (l.isEmpty() || l.get(0) instanceof ChatLine) { if (linesField == null) linesField = f; }
                }
            }
            if (linesField == null) return null;
            List<?> l = (List<?>) linesField.get(chat);
            if (!l.isEmpty() && !(l.get(0) instanceof ChatLine)) { searched = false; linesField = null; return null; }
            return (List<ChatLine>) l;
        } catch (Throwable t) {
            return null;
        }
    }

    private String gg() {
        switch (message.current()) {
            case "gg_wp": return "gg wp";
            case "good_game": return "good game";
            default: return "gg";
        }
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) { lastSeen = null; pending = -1; return; }
        if (pending > 0) { pending--; }
        else if (pending == 0) {
            pending = -1;
            mc.thePlayer.sendChatMessage(gg());
            lastSent = System.currentTimeMillis();
        }
        List<ChatLine> l = lines();
        if (l == null || l.isEmpty()) return;
        ChatLine newest = l.get(0);
        if (lastSeen == null) { lastSeen = newest; return; }
        if (newest == lastSeen) return;
        for (int i = 0; i < l.size() && i < 20; i++) {
            ChatLine cl = l.get(i);
            if (cl == lastSeen) break;
            String t = cl.getChatComponent().getUnformattedText().toLowerCase(Locale.ROOT);
            if (t.startsWith("<") || t.contains("[gg]")) continue;     // normal player chat
            for (String trig : TRIGGERS) {
                if (t.contains(trig) && pending < 0 && System.currentTimeMillis() - lastSent > 30000L) {
                    pending = Math.round(delay.value * 20f);
                    break;
                }
            }
        }
        lastSeen = newest;
    }
}

package dev.yaclient.modules;

import dev.yaclient.Setting;
import dev.yaclient.YaChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Chat options: remove the dark background behind the lines and make the whole chat bigger or smaller. */
public class ChatModule extends HudModule {
    private final Setting.Bool hideBg = add(new Setting.Bool("hide_bg", true));
    private final Setting.Num size = add(new Setting.Num("scale", 30f, 250f, 5f, 100f, "%"));

    private static ChatModule inst;
    private boolean failed;

    public ChatModule() { super("chat", Category.UTILITY, false, -1f, -1f); inst = this; }

    // The chat is a movable HUD element: px/py < 0 means "where the game puts it" (bottom left).
    @Override public int width() { return 320; }

    @Override public int height() { return 90; }

    @Override
    public void loadConfig() {
        super.loadConfig();
        scale = dev.yaclient.Ui.clamp(dev.yaclient.Config.getFloat("hud.chat.s", 1f), MIN_SCALE, MAX_SCALE);
    }

    @Override
    public void resetLayout() { super.resetLayout(); scale = 1f; }

    @Override
    public float screenX(int sw) { return px < 0f ? 2f : super.screenX(sw); }

    @Override
    public float screenY(int sh) { return py < 0f ? sh - 28f - height() * screenScale() : super.screenY(sh); }

    /** Preview only: the real chat is drawn by the game / YaChat. */
    @Override
    protected void draw() {
        if (!preview) return;
        dev.yaclient.Gfx.rr(0, 0, width(), height(), 6, 0x66000000);
        dev.yaclient.Gfx.rrOutline(0, 0, width(), height(), 6, 1, 0x33FFFFFF);
        String[] t = {"<Player1> hello", "<Player2> gg", "[YA] chat position", "<Player1> nice"};
        for (int i = 0; i < t.length; i++) dev.yaclient.Gfx.text(t[i], 6, 8 + i * 14, 10, false, 0xFFFFFFFF);
    }

    public static boolean bgHidden() { return inst != null && inst.enabled && inst.hideBg.value; }

    /** How many GUI pixels the chat is lifted above its normal place (negative = lower). */
    public static int lift(int sh) {
        if (inst == null || !inst.enabled || inst.py < 0f) return 0;
        return Math.round((sh - 28f) - (inst.screenY(sh) + inst.height() * inst.screenScale()));
    }

    /** How many GUI pixels the chat is moved to the right. */
    public static int shiftX(int sw) { return inst == null || !inst.enabled || inst.px < 0f ? 0 : Math.round(inst.screenX(sw) - 2f); }

    public static float userScale() { return inst != null && inst.enabled ? inst.size.value / 100f * inst.screenScale() : 1f; }

    /** Swaps the game's chat for the YA chat (same history), once. Runs every tick so it survives a GUI swap. */
    @Override
    public void onAlwaysTick() {
        if (failed || mc == null || mc.ingameGUI == null) return;
        GuiIngame gui = mc.ingameGUI;
        try {
            if (gui.getChatGUI() instanceof YaChat) return;
            Field slot = null;
            for (Field f : GuiIngame.class.getDeclaredFields()) {
                if (f.getType() == GuiNewChat.class && !Modifier.isStatic(f.getModifiers())) { slot = f; break; }
            }
            if (slot == null) { failed = true; return; }
            slot.setAccessible(true);
            GuiNewChat old = (GuiNewChat) slot.get(gui);
            YaChat neu = new YaChat(mc);
            for (Field f : GuiNewChat.class.getDeclaredFields()) {      // keep the lines, the history and the scroll position
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(neu, f.get(old));
            }
            slot.set(gui, neu);
        } catch (Throwable t) {
            failed = true;     // never break the game because of the chat
        }
    }
}

package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Keeps the movement keys in sync with the real keyboard. Minecraft only updates a key when it gets a press or
 * release event, so a missed event (menu opened or closed while a key was held, alt-tab, lag spike, another
 * module forcing a key) leaves the game thinking a key is still pressed (the player walks by itself) or not
 * pressed (the player does not jump until the key is pressed again). Once per tick, while no screen is open,
 * every movement key gets the state the keyboard really has.
 */
public final class KeyGuard {
    private KeyGuard() {}

    private static void sync(KeyBinding kb) {
        if (kb == null) return;
        int code = kb.getKeyCode();
        boolean real;
        try {
            real = code < 0 ? Mouse.isButtonDown(code + 100) : (code > 0 && Keyboard.isKeyDown(code));
        } catch (Throwable t) { return; }
        if (kb.isKeyDown() != real) KeyBinding.setKeyBindState(code, real);
    }

    /** Call once per client tick, before the modules that may force a key (toggle sprint / sneak). */
    public static void tick(Minecraft mc) {
        if (mc == null || mc.gameSettings == null || mc.thePlayer == null || mc.currentScreen != null) return;
        GameSettings gs = mc.gameSettings;
        sync(gs.keyBindForward);
        sync(gs.keyBindBack);
        sync(gs.keyBindLeft);
        sync(gs.keyBindRight);
        sync(gs.keyBindJump);
        sync(gs.keyBindSneak);
        sync(gs.keyBindSprint);
    }
}

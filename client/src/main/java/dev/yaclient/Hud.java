package dev.yaclient;

import dev.yaclient.modules.HudModule;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

/** Draws the HUD elements every frame and holds the global HUD settings. */
public final class Hud {
    private static float globalScale = 1f;

    private Hud() {}

    public static float globalScale() { return globalScale; }

    public static void setGlobalScale(float s) {
        globalScale = Ui.clamp(s, 0.5f, 2f);
        Config.setFloat("hud.scale", globalScale);
    }

    public static void loadConfig() { globalScale = Ui.clamp(Config.getFloat("hud.scale", 1f), 0.5f, 2f); }

    /** Enabled HUD elements, in draw order. */
    public static List<HudModule> elements() {
        List<HudModule> out = new ArrayList<HudModule>();
        for (Module m : YAClient.MODULES) if (m.enabled && m instanceof HudModule) out.add((HudModule) m);
        return out;
    }

    public static void resetLayout() {
        for (Module m : YAClient.MODULES) {
            if (m instanceof HudModule) {
                ((HudModule) m).resetLayout();
                m.saveConfig();
            }
        }
        setGlobalScale(1f);
        Config.save();
    }

    public static void saveLayout() {
        for (Module m : YAClient.MODULES) if (m instanceof HudModule) m.saveConfig();
        Config.save();
    }

    /** Called every frame after the vanilla overlay. */
    public static void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int gs = sr.getScaleFactor();
        Gfx.fade = 1f;
        for (Module m : YAClient.MODULES) {
            if (!m.enabled) continue;
            final Module mm = m;
            m.bind();
            Gfx.setScale(gs);
            m.guard(new Runnable() { public void run() { mm.onRender(); } });
        }
        int sw = sr.getScaledWidth(), sh = sr.getScaledHeight();
        for (int i = 0, n = YAClient.MODULES.size(); i < n; i++) {
            Module m = YAClient.MODULES.get(i);
            if (m.enabled && m instanceof HudModule) { final HudModule hm = (HudModule) m; final int fsw = sw, fsh = sh, fgs = gs; m.guard(new Runnable() { public void run() { hm.renderHud(false, fsw, fsh, fgs); } }); }
        }
    }

    /** Key or mouse button of a key binding is currently held. */
    public static boolean down(KeyBinding kb) {
        int code = kb.getKeyCode();
        if (code < 0) return Mouse.isButtonDown(code + 100);
        return Keyboard.isKeyDown(code);
    }
}

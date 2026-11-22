package dev.yaclient.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

/** Draws every enabled HUD module on top of the game HUD (older Fabric API: HudRenderCallback). */
public final class Hud {
    private Hud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((g, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            for (Module m : Modules.ALL) {
                if (m.active() && m instanceof HudModule h) h.render(g, mc);
            }
        });
    }
}

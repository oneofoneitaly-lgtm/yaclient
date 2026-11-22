package dev.yaclient.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/** Draws every enabled HUD module on top of the game HUD. */
public final class Hud {
    private Hud() {}

    public static void register() {
        // our crosshair replaces the vanilla one instead of drawing next to it
        HudElementRegistry.replaceElement(net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CROSSHAIR, prev -> (g, delta) -> {
            Module c = Modules.byId("crosshair");
            if (c == null || !c.enabled) prev.extractRenderState(g, delta);
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("yaclient", "hud"), (g, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            for (Module m : Modules.ALL) {
                if (m.active() && m instanceof HudModule h) { try { h.render(g, mc); } catch (Throwable t) { Modules.fail(m, t); } }
            }
        });
    }
}

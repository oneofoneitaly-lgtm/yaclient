package dev.yaclient.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** YA CLIENT for the latest Minecraft (Fabric). */
public class YaFabricClient implements ClientModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final String VERSION = "1.0.0";

    @Override
    public void onInitializeClient() {
        Config.load();
        Modules.init();
        // new installs: only FPS, CPS, FPS Boost and Discord RPC are on, everything else starts off (switches the player already set stay as they are)
        if (!Config.getBool("perf.v2", false)) {
            java.util.List<String> base = java.util.Arrays.asList("fps", "cps", "fpsboost", "discord_rpc");
            for (Module m : Modules.ALL) {
                if (!Config.has("mod." + m.id + ".enabled")) { m.enabled = base.contains(m.id); m.save(); }
            }
            Config.setBool("perf.v2", true);
            Config.save();
        }
        Hud.register();
        Hits.init();
        dev.yaclient.fabric.cos.CosmeticsLayer.register();
        KeyMapping menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.yaclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("yaclient", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) CosmeticsApi.refreshAsync(false);
            Modules.tick();
            TabBadge.tick(client);
            while (menuKey.consumeClick()) client.setScreenAndShow(new HubScreen());
        });
        System.out.println("[" + NAME + "] " + VERSION + " loaded (Fabric), " + Modules.ALL.size() + " modules.");
    }
}

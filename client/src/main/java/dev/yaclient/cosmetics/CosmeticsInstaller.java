package dev.yaclient.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Adds CosmeticLayer to the player renderers without Mixin: the renderer keeps its layers in a
 * List field, which is found by type (so it works whatever the runtime field names are).
 */
public final class CosmeticsInstaller {
    private static boolean done, failed;

    private CosmeticsInstaller() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void tryInstall() {
        if (done || failed) return;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getRenderManager() == null) return;
            // RenderManager has no public getter for the player renderers: collect them by reflection
            Set<RenderPlayer> players = new LinkedHashSet<RenderPlayer>();
            Object rm = mc.getRenderManager();
            for (Field f : rm.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(rm);
                if (v instanceof Map) {
                    for (Object o : ((Map) v).values()) if (o instanceof RenderPlayer) players.add((RenderPlayer) o);
                } else if (v instanceof RenderPlayer) {
                    players.add((RenderPlayer) v);
                }
            }
            if (players.isEmpty()) return;

            Field layers = null;
            for (Field f : RendererLivingEntity.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) { layers = f; break; }
            }
            if (layers == null) { failed = true; return; }
            layers.setAccessible(true);
            for (RenderPlayer rp : players) {
                List list = (List) layers.get(rp);
                for (int k = 0; k < list.size(); k++) {
                    if (list.get(k) instanceof LayerCape) list.set(k, new CapeGuard((LayerRenderer) list.get(k)));
                }
                list.add(new CosmeticLayer(rp));
                list.add(new dev.yaclient.modules.HitTintLayer(rp));
            }
            done = true;
        } catch (Throwable t) {
            failed = true;
            System.out.println("[YA CLIENT] cosmetics layer not installed: " + t);
        }
    }
}

package dev.yaclient.modules;

import dev.yaclient.Config;
import dev.yaclient.Setting;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.opengl.Display;

/**
 * Raises FPS by lowering the heaviest video settings while it is on, and puts your own settings back
 * when it is switched off (the originals are stored in the config, so this also works across restarts).
 */
public class FpsBoostModule extends Module {
    private final Setting.Bool particles = add(new Setting.Bool("particles", true));
    private final Setting.Bool shadows = add(new Setting.Bool("shadows", true));
    private final Setting.Bool graphics = add(new Setting.Bool("graphics", true));
    private final Setting.Bool smooth = add(new Setting.Bool("smooth", true));
    private final Setting.Bool unlock = add(new Setting.Bool("unlock", true));
    private final Setting.Bool entities = add(new Setting.Bool("entities", true));
    private final Setting.Bool vbo = add(new Setting.Bool("vbo", true));
    private final Setting.Bool clouds = add(new Setting.Bool("clouds", true));
    private final Setting.Num distance = add(new Setting.Num("distance", 2f, 16f, 1f, 8f, ""));

    private boolean applied;
    private int ticks;

    public FpsBoostModule() { super("fps_boost", Category.UTILITY, true); }

    private static final String K = "fpsboost.orig.";

    private void remember(GameSettings gs) {
        if (Config.getBool(K + "saved", false)) return;   // keep the very first (real) values
        Config.setInt(K + "particles", gs.particleSetting);
        Config.setBool(K + "shadows", gs.entityShadows);
        Config.setBool(K + "fancy", gs.fancyGraphics);
        Config.setInt(K + "ao", gs.ambientOcclusion);
        Config.setInt(K + "dist", gs.renderDistanceChunks);
        Config.setInt(K + "fps", gs.limitFramerate);
        Config.setInt(K + "clouds", gs.clouds);
        Config.setBool(K + "vbo", gs.useVbo);
        Config.setBool(K + "vsync", gs.enableVsync);
        Config.setBool(K + "saved", true);
        Config.save();
    }

    private void apply(GameSettings gs) {
        if (particles.value) gs.particleSetting = 2;
        if (shadows.value) gs.entityShadows = false;
        if (graphics.value) gs.fancyGraphics = false;
        if (smooth.value) gs.ambientOcclusion = 0;
        int orig = Config.getInt(K + "dist", gs.renderDistanceChunks);
        int cap = Math.min(orig, Math.round(distance.value));
        if (gs.renderDistanceChunks != cap) gs.renderDistanceChunks = cap;
        if (clouds.value && gs.clouds != 0) gs.clouds = 0;
        if (vbo.value && !gs.useVbo) {
            gs.useVbo = true;                 // VBO chunk rendering: fewer draw calls, a real FPS gain
            try { if (mc.renderGlobal != null) mc.renderGlobal.loadRenderers(); } catch (Throwable ignored) { }
        }
        if (unlock.value) {
            gs.limitFramerate = 260;
            gs.enableVsync = false;
            try { Display.setVSyncEnabled(false); } catch (Throwable ignored) { }
        }
    }

    private void restore(GameSettings gs) {
        if (!Config.getBool(K + "saved", false)) return;
        gs.particleSetting = Config.getInt(K + "particles", gs.particleSetting);
        gs.entityShadows = Config.getBool(K + "shadows", gs.entityShadows);
        gs.fancyGraphics = Config.getBool(K + "fancy", gs.fancyGraphics);
        gs.ambientOcclusion = Config.getInt(K + "ao", gs.ambientOcclusion);
        gs.renderDistanceChunks = Config.getInt(K + "dist", gs.renderDistanceChunks);
        gs.limitFramerate = Config.getInt(K + "fps", gs.limitFramerate);
        gs.clouds = Config.getInt(K + "clouds", gs.clouds);
        boolean ov = Config.getBool(K + "vbo", gs.useVbo);
        if (gs.useVbo != ov) { gs.useVbo = ov; try { if (mc.renderGlobal != null) mc.renderGlobal.loadRenderers(); } catch (Throwable ignored) { } }
        cullEntities(false);
        gs.enableVsync = Config.getBool(K + "vsync", gs.enableVsync);
        try { Display.setVSyncEnabled(gs.enableVsync); } catch (Throwable ignored) { }
        Config.setBool(K + "saved", false);
        Config.save();
    }

    /** Original render-distance factor of every entity we shortened (restored when the module is switched off). */
    private final java.util.WeakHashMap<net.minecraft.entity.Entity, Double> orig = new java.util.WeakHashMap<net.minecraft.entity.Entity, Double>();

    /**
     * Real saving: items, mobs, armor stands, xp orbs and so on are not drawn from far away (players always are).
     * Fewer entities to draw each frame means a higher frame rate in busy places.
     */
    private void cullEntities(boolean on) {
        try {
            if (mc.theWorld == null) { orig.clear(); return; }
            java.util.List<net.minecraft.entity.Entity> list = mc.theWorld.loadedEntityList;
            for (int i = 0; i < list.size(); i++) {
                net.minecraft.entity.Entity e = list.get(i);
                if (e == null || e instanceof net.minecraft.entity.player.EntityPlayer) continue;
                if (on) {
                    if (!orig.containsKey(e)) orig.put(e, e.renderDistanceWeight);
                    e.renderDistanceWeight = Math.min(orig.get(e), 0.45);
                } else {
                    Double o = orig.remove(e);
                    if (o != null) e.renderDistanceWeight = o;
                }
            }
            if (!on) orig.clear();
        } catch (Throwable ignored) { }
    }

    @Override
    public void onAlwaysTick() {
        if (mc == null || mc.gameSettings == null) return;
        GameSettings gs = mc.gameSettings;
        if (enabled) {
            if (!applied) {
                remember(gs);
                applied = true;
                apply(gs);
            } else if (++ticks % 40 == 0) {
                apply(gs);     // keeps working if a setting was changed meanwhile
            }
            if (entities.value && ticks % 20 == 0) cullEntities(true);
            else if (!entities.value && !orig.isEmpty()) cullEntities(false);
        } else if (applied) {
            restore(gs);
            applied = false;
        }
    }
}

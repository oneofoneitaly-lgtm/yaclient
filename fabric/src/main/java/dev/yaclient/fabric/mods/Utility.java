package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Module;
import dev.yaclient.fabric.Setting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Modules without a HUD box. */
public final class Utility {
    private Utility() {}

    public static class Fullbright extends Module {
        public Fullbright() { super("fullbright", "Fullbright", "See in the dark (client side night vision)", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.player != null) mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
        }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (!enabled && mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static class NoClouds extends Module {
        public NoClouds() { super("noclouds", "No Clouds", "Removes the clouds from the sky", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.options.cloudStatus().get() != CloudStatus.OFF) mc.options.cloudStatus().set(CloudStatus.OFF);
        }
    }

    public static class ToggleSprint extends Module {
        public ToggleSprint() { super("togglesprint", "Toggle Sprint", "Sprints automatically while you walk forward", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.player == null || !mc.mouseHandler.isMouseGrabbed()) return;
            if (mc.options.keyUp.isDown() && !mc.player.isShiftKeyDown() && !mc.player.isUsingItem()) mc.player.setSprinting(true);
        }
    }

    /** Hold the key to zoom in (FOV 30), release to restore. */
    public static class Zoom extends Module {
        private int saved = -1;
        static volatile boolean holding;
        private final Setting.Num fov = add("general", new Setting.Num("zoom_fov", 10f, 60f, 1f, 30f, ""));

        public Zoom() { super("zoom", "Zoom", "Hold C to zoom in", Category.PVP, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            boolean down = mc.mouseHandler.isMouseGrabbed() && org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().handle(), org.lwjgl.glfw.GLFW.GLFW_KEY_C) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            holding = down;
            if (down && saved < 0) { saved = mc.options.fov().get(); mc.options.fov().set(Math.round(fov.value)); }
            else if (!down && saved >= 0) { mc.options.fov().set(saved); saved = -1; }
        }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (!enabled && saved >= 0) { mc.options.fov().set(saved); saved = -1; }
        }
    }

    /** Keeps the field of view steady while sprinting or under speed effects. */
    public static class NoFovEffects extends Module {
        private Double saved;
        public NoFovEffects() { super("nofoveffects", "No FOV Effects", "[PvP] The view does not stretch when you sprint or have speed", Category.UTILITY, false); }
        @Override public void onTick() { var o = mc().options.fovEffectScale(); if (saved == null) saved = o.get(); if (o.get() != 0.0) o.set(0.0); }
        @Override public void onToggle() { if (!enabled && saved != null) { mc().options.fovEffectScale().set(saved); saved = null; } }
    }

    /** Removes the wobble of nausea / portal effects on screen. */
    public static class NoScreenEffects extends Module {
        private Double saved;
        public NoScreenEffects() { super("noscreeneffects", "No Screen Effects", "Less screen wobble from nausea and portals", Category.UTILITY, false); }
        @Override public void onTick() { var o = mc().options.screenEffectScale(); if (saved == null) saved = o.get(); if (o.get() != 0.0) o.set(0.0); }
        @Override public void onToggle() { if (!enabled && saved != null) { mc().options.screenEffectScale().set(saved); saved = null; } }
    }

    /** Turns off view bobbing while walking. */
    public static class NoBobbing extends Module {
        private Boolean saved;
        public NoBobbing() { super("nobobbing", "No Bobbing", "Steady camera while walking", Category.UTILITY, false); }
        @Override public void onTick() { var o = mc().options.bobView(); if (saved == null) saved = o.get(); if (o.get()) o.set(false); }
        @Override public void onToggle() { if (!enabled && saved != null) { mc().options.bobView().set(saved); saved = null; } }
    }

    /** Fixed field of view, independent of the vanilla slider (zoom still works on top). */
    public static class Fov extends Module {
        private int saved = -1;
        private final Setting.Num fov = add("general", new Setting.Num("fov_value", 30f, 110f, 1f, 90f, ""));
        public Fov() { super("fov", "FOV", "Set your field of view quickly (30 - 110)", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (Zoom.holding) return;
            if (saved < 0) saved = mc.options.fov().get();
            int v = Math.round(fov.value);
            if (mc.options.fov().get() != v) mc.options.fov().set(v);
        }

        @Override
        public void onToggle() {
            if (!enabled && saved >= 0 && !Zoom.holding) { mc().options.fov().set(saved); }
            if (!enabled) saved = -1;
        }
    }

    /** Lowers the heaviest video options while on and restores them when switched off. */
    public static class FpsBoost extends Module {
        private final java.util.Map<String, Object> saved = new java.util.HashMap<String, Object>();
        private int t;
        public FpsBoost() { super("fpsboost", "FPS Boost", "Fewer particles, no clouds, no shadows and less entity detail for more FPS", Category.UTILITY, false); }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void put(String key, OptionInstance opt, Object value) {
            if (!saved.containsKey(key)) saved.put(key, opt.get());
            if (!opt.get().equals(value)) opt.set(value);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void restore(String key, OptionInstance opt) {
            if (saved.containsKey(key)) opt.set(saved.get(key));
        }

        private void apply() {
            Minecraft mc = mc();
            put("particles", mc.options.particles(), ParticleStatus.MINIMAL);
            put("clouds", mc.options.cloudStatus(), CloudStatus.OFF);
            put("shadows", mc.options.entityShadows(), Boolean.FALSE);
            put("entdist", mc.options.entityDistanceScaling(), Double.valueOf(0.5));
            put("biome", mc.options.biomeBlendRadius(), Integer.valueOf(0));
            put("bob", mc.options.bobView(), Boolean.FALSE);
            put("ao", mc.options.ambientOcclusion(), Boolean.FALSE);
            put("rd", mc.options.renderDistance(), Integer.valueOf(Math.min(12, mc.options.renderDistance().get())));
            put("sd", mc.options.simulationDistance(), Integer.valueOf(Math.min(8, mc.options.simulationDistance().get())));
        }

        @Override public void onTick() { if (t++ % 100 == 0) apply(); }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (enabled) { t = 0; apply(); return; }
            restore("particles", mc.options.particles());
            restore("clouds", mc.options.cloudStatus());
            restore("ao", mc.options.ambientOcclusion());
            restore("rd", mc.options.renderDistance());
            restore("sd", mc.options.simulationDistance());
            restore("shadows", mc.options.entityShadows());
            restore("entdist", mc.options.entityDistanceScaling());
            restore("biome", mc.options.biomeBlendRadius());
            restore("bob", mc.options.bobView());
            saved.clear();
        }
    }
}

package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Motion blur using Minecraft's built-in "phosphor" post-processing shader. The shader keeps a fixed fraction of the
 * previous frame, so at a high frame rate the trail was far too long. The fraction is now recomputed from the real
 * frame rate so the blur always lasts the same few milliseconds, whatever the FPS.
 */
public class MotionBlurModule extends Module {
    /** 1 = barely visible, 100 = strong. */
    private final Setting.Num strength = add(new Setting.Num("strength", 1f, 100f, 1f, 20f, "%"));

    private static Method loader;
    private static Field groupShaders;
    private static boolean failed, shadersFailed;
    private boolean applied;

    public MotionBlurModule() { super("motion_blur", Category.UTILITY, false); }

    /**
     * EntityRenderer.loadShader(ResourceLocation) is private. The method is found by its signature
     * (void, one ResourceLocation parameter) so this works whatever the runtime method names are.
     */
    private static void loadShader(EntityRenderer renderer, ResourceLocation location) {
        if (failed) return;
        try {
            if (loader == null) {
                for (Method m : EntityRenderer.class.getDeclaredMethods()) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length == 1 && p[0] == ResourceLocation.class && m.getReturnType() == void.class) {
                        m.setAccessible(true);
                        loader = m;
                        break;
                    }
                }
            }
            if (loader == null) { failed = true; return; }
            loader.invoke(renderer, location);
        } catch (Exception e) {
            failed = true;
        }
    }

    /** Sets the "Phosphor" uniform (share of the previous frame that stays) of every pass of the active shader. */
    @SuppressWarnings("unchecked")
    private void setKeep(float keep) {
        if (shadersFailed) return;
        try {
            ShaderGroup g = mc.entityRenderer.getShaderGroup();
            if (g == null) return;
            if (groupShaders == null) {
                for (Field f : ShaderGroup.class.getDeclaredFields()) {
                    if (f.getType() == List.class) {
                        f.setAccessible(true);
                        List<?> l = (List<?>) f.get(g);
                        if (l.isEmpty() || l.get(0) instanceof Shader) { groupShaders = f; break; }
                    }
                }
            }
            if (groupShaders == null) { shadersFailed = true; return; }
            for (Object o : (List<Object>) groupShaders.get(g)) {
                ShaderUniform u = ((Shader) o).getShaderManager().getShaderUniform("Phosphor");
                if (u != null) u.set(keep, keep, keep);
            }
        } catch (Throwable t) {
            shadersFailed = true;
        }
    }

    @Override
    public void onTick() {
        if (mc.theWorld == null || mc.entityRenderer == null) return;
        // Blur only the world: with a menu open (inventory, chat, mod menu...) the shader also smeared the player
        // preview in the inventory, so the skin looked bent. It is switched off while a screen is open and loaded
        // again, with a clean previous frame, when the screen closes.
        if (mc.currentScreen != null) {
            if (applied) {
                if (mc.entityRenderer.isShaderActive()) mc.entityRenderer.stopUseShader();
                applied = false;
            }
            return;
        }
        if (!mc.entityRenderer.isShaderActive()) {
            loadShader(mc.entityRenderer, new ResourceLocation("shaders/post/phosphor.json"));
            applied = true;
        }
        // the blur lasts about 3 ms (weak) to 21 ms (strong); per frame that is half-life / frame time
        float fps = Math.max(20f, Minecraft.getDebugFPS());
        float halfLife = 0.003f + 0.00018f * strength.value;
        float keep = (float) Math.pow(0.5, (1.0 / fps) / halfLife);
        setKeep(Math.max(0f, Math.min(0.9f, keep)));
    }

    @Override
    public void onToggle() {
        if (!enabled && applied && mc != null && mc.entityRenderer != null) {
            mc.entityRenderer.stopUseShader();
            applied = false;
        }
    }
}

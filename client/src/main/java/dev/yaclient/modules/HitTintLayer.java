package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/** Draws the player model again in a flat color while the player is hurt (fades with the hurt time). */
public class HitTintLayer implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer renderer;

    public HitTintLayer(RenderPlayer renderer) { this.renderer = renderer; }

    @Override
    public void doRenderLayer(AbstractClientPlayer p, float limbSwing, float limbSwingAmount, float partialTicks,
                              float age, float netHeadYaw, float headPitch, float scale) {
        HitColorModule m = HitColorModule.inst;
        int ht = p == Minecraft.getMinecraft().thePlayer ? NoHurtCamModule.hurt(p) : p.hurtTime;
        if (m == null || !m.enabled || ht <= 0 || p.isInvisible()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (p == mc.thePlayer && !m.self.value) return;
        float f = Math.max(0f, Math.min(1f, (ht - partialTicks) / Math.max(1f, (float) NoHurtCamModule.realMax(p))));
        float a = m.intensity.value / 100f * (0.35f + 0.65f * f);
        int rgb = Setting.Palette.color(m.color.index);
        float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
        ModelPlayer model = renderer.getMainModel();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.depthFunc(515);
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1f, -1f);
        GlStateManager.color(r, g, b, a);
        model.render(p, limbSwing, limbSwingAmount, age, netHeadYaw, headPitch, scale);
        GlStateManager.disablePolygonOffset();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}

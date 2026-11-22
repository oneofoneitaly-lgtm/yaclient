package dev.yaclient.cosmetics;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/** Wraps the vanilla cape layer and hides it while a YA cape is worn. */
final class CapeGuard implements LayerRenderer<AbstractClientPlayer> {
    private final LayerRenderer<AbstractClientPlayer> inner;

    CapeGuard(LayerRenderer<AbstractClientPlayer> inner) { this.inner = inner; }

    @Override
    public void doRenderLayer(AbstractClientPlayer p, float limbSwing, float limbSwingAmount, float partialTicks,
                              float age, float netHeadYaw, float headPitch, float scale) {
        if (CosmeticLayer.wearsYaCape(p)) return;
        inner.doRenderLayer(p, limbSwing, limbSwingAmount, partialTicks, age, netHeadYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() { return inner.shouldCombineTextures(); }
}

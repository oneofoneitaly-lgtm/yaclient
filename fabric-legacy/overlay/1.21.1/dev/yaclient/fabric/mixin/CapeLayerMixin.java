package dev.yaclient.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yaclient.fabric.Badges;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the normal Minecraft cape while a YA cape is equipped, so the two do not overlap. */
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void yaclient$hideVanillaCape(PoseStack ps, MultiBufferSource c, int light, AbstractClientPlayer p, float a, float b, float d, float e, float f, float g, CallbackInfo ci) {
        if (Badges.hasYaCape(p)) ci.cancel();
    }
}

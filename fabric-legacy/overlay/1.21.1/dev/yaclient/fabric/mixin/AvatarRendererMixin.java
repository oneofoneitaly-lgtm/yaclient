package dev.yaclient.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yaclient.fabric.Badges;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Puts the YA logo (and the gold +) in front of the name above the head of YA CLIENT players. */
@Mixin(PlayerRenderer.class)
public abstract class AvatarRendererMixin {
    private static final String M = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V";

    @Inject(method = M, at = @At("HEAD"))
    private void yaclient$who(AbstractClientPlayer p, Component name, PoseStack ps, MultiBufferSource b, int light, float pt, CallbackInfo ci) {
        Badges.current = p;
    }

    @ModifyVariable(method = M, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component yaclient$nametag(Component name) {
        AbstractClientPlayer p = Badges.current;
        return p == null ? name : Badges.nametag(p, name);
    }
}

package dev.yaclient.fabric.mixin;

import dev.yaclient.fabric.Badges;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Puts the YA logo (and the gold +) in front of the name above the head of YA CLIENT players. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void yaclient$nametag(Avatar avatar, AvatarRenderState state, float partial, CallbackInfo ci) {
        Badges.applyNametag(avatar, state);
    }
}

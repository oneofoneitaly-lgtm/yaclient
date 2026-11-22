package dev.yaclient.fabric.mixin;

import dev.yaclient.fabric.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shows your own name tag (with the YA logo) in third person, like the newer versions do. */
@Mixin(LivingEntityRenderer.class)
public abstract class SelfNameMixin {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true)
    private void yaclient$self(LivingEntity e, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !(e instanceof AbstractClientPlayer p)) return;
        Minecraft mc = Minecraft.getInstance();
        if (p == mc.player && !mc.options.getCameraType().isFirstPerson() && Config.getBool("nametag.badge", true)) cir.setReturnValue(true);
    }
}

package dev.yaclient.fabric.mixin;

import dev.yaclient.fabric.Module;
import dev.yaclient.fabric.Modules;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Older versions (1.21.1 / 1.21.4): hide the vanilla crosshair while the YA crosshair is on. Ignored where the method does not exist. */
@Mixin(Gui.class)
public abstract class GuiCrosshairMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void yaclient$crosshair(CallbackInfo ci) {
        Module c = Modules.byId("crosshair");
        if (c != null && c.enabled) ci.cancel();
    }
}

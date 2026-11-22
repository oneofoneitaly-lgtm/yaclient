package dev.yaclient.fabric.mixin;

import dev.yaclient.fabric.ServerListScreen;
import dev.yaclient.fabric.WorldListScreen;
import dev.yaclient.fabric.YaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Swaps the vanilla title, multiplayer and singleplayer screens for the YA CLIENT ones (same as 1.8.9). */
@Mixin({TitleScreen.class, JoinMultiplayerScreen.class, SelectWorldScreen.class})
public abstract class MenuReplaceMixin {
    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private void yaclient$replace(CallbackInfo ci) {
        if (!dev.yaclient.fabric.Config.getBool("menu.custom", true)) return;
        Object self = this;
        Screen next;
        if (self instanceof TitleScreen) next = new YaTitleScreen();
        else if (self instanceof JoinMultiplayerScreen) next = new ServerListScreen(new YaTitleScreen());
        else if (self instanceof SelectWorldScreen) next = new WorldListScreen(new YaTitleScreen());
        else return;
        ci.cancel();
        Minecraft.getInstance().setScreenAndShow(next);
    }
}

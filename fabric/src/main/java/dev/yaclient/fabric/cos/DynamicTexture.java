package dev.yaclient.fabric.cos;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

/** Shim: uploads a BufferedImage as a Minecraft texture; getGlTextureId() is a handle for GlStateManager.bindTexture. */
final class DynamicTexture {
    private static int counter;
    private static final ConcurrentHashMap<Integer, Identifier> IDS = new ConcurrentHashMap<Integer, Identifier>();

    private final int handle;
    private final Identifier id;
    private final net.minecraft.client.renderer.texture.DynamicTexture tex;

    DynamicTexture(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        NativeImage ni = new NativeImage(w, h, false);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) ni.setPixel(x, y, img.getRGB(x, y));
        final String label = "yaclient/cos";
        tex = new net.minecraft.client.renderer.texture.DynamicTexture(() -> label, ni);
        synchronized (DynamicTexture.class) { handle = counter++; }
        id = Identifier.fromNamespaceAndPath("yaclient", "cos/t" + handle);
        Minecraft.getInstance().getTextureManager().register(id, tex);
        IDS.put(handle, id);
    }

    int getGlTextureId() { return handle; }

    void deleteGlTexture() {
        try { Minecraft.getInstance().getTextureManager().release(id); } catch (Throwable ignored) { }
        IDS.remove(handle);
    }

    static Identifier identifier(int handle) { return IDS.get(handle); }
}

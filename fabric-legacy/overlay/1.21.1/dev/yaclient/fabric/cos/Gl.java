package dev.yaclient.fabric.cos;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Render context of the cosmetic layer (1.21.2 - 1.21.8: immediate buffers). */
final class Gl {
    private static PoseStack stack;
    private static MultiBufferSource collector;
    private static DynamicTexture white;

    private Gl() {}

    static void begin(PoseStack ps, MultiBufferSource c) {
        stack = ps;
        collector = c;
        GlStateManager.reset();
        if (white == null) {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, 0xFFFFFFFF);
            white = new DynamicTexture(img);
        }
    }

    static void end() { stack = null; collector = null; }

    static void flush(List<float[]> src, int texHandle) {
        if (stack == null || collector == null || src.isEmpty()) return;
        ResourceLocation id = texHandle >= 0 ? DynamicTexture.identifier(texHandle) : null;
        if (id == null) id = DynamicTexture.identifier(white.getGlTextureId());
        final double[] m = GlStateManager.matrix();
        int n = src.size() - src.size() % 4;
        VertexConsumer vc = collector.getBuffer(RenderType.entityTranslucentEmissive(id));
        PoseStack.Pose pose = stack.last();
        for (int i = 0; i < n; i++) {
            float[] v = src.get(i);
            double x = v[0], y = v[1], z = v[2];
            float ox = (float) (m[0] * x + m[1] * y + m[2] * z + m[3]);
            float oy = (float) (m[4] * x + m[5] * y + m[6] * z + m[7]);
            float oz = (float) (m[8] * x + m[9] * y + m[10] * z + m[11]);
            vc.addVertex(pose, ox, oy, oz)
              .setColor(clamp(v[3]), clamp(v[4]), clamp(v[5]), clamp(v[6]))
              .setUv(v[7], v[8])
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(LightTexture.FULL_BRIGHT)
              .setNormal(0f, 1f, 0f);
        }
    }

    private static int clamp(float f) { return Math.max(0, Math.min(255, Math.round(f * 255f))); }
}

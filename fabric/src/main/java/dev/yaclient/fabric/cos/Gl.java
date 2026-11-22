package dev.yaclient.fabric.cos;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Render context of the cosmetic layer: vertices recorded by Poly are transformed by the shim matrix and submitted. */
final class Gl {
    private static PoseStack stack;
    private static SubmitNodeCollector collector;
    private static DynamicTexture white;

    private Gl() {}

    static void begin(PoseStack ps, SubmitNodeCollector c) {
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
        Identifier id = texHandle >= 0 ? DynamicTexture.identifier(texHandle) : null;
        if (id == null) id = DynamicTexture.identifier(white.getGlTextureId());
        final double[] m = GlStateManager.matrix();
        final ArrayList<float[]> out = new ArrayList<float[]>(src.size());
        int n = src.size() - src.size() % 4;
        for (int i = 0; i < n; i++) {
            float[] v = src.get(i);
            double x = v[0], y = v[1], z = v[2];
            float[] o = v.clone();
            o[0] = (float) (m[0] * x + m[1] * y + m[2] * z + m[3]);
            o[1] = (float) (m[4] * x + m[5] * y + m[6] * z + m[7]);
            o[2] = (float) (m[8] * x + m[9] * y + m[10] * z + m[11]);
            out.add(o);
        }
        RenderType rt = RenderTypes.entityTranslucentEmissive(id);
        collector.submitCustomGeometry(stack, rt, (pose, vc) -> {
            for (float[] v : out) {
                vc.addVertex(pose, v[0], v[1], v[2])
                  .setColor(clamp(v[3]), clamp(v[4]), clamp(v[5]), clamp(v[6]))
                  .setUv(v[7], v[8])
                  .setOverlay(OverlayTexture.NO_OVERLAY)
                  .setLight(LightCoordsUtil.FULL_BRIGHT)
                  .setNormal(0f, 1f, 0f);
            }
        });
    }

    private static int clamp(float f) { return Math.max(0, Math.min(255, Math.round(f * 255f))); }
}

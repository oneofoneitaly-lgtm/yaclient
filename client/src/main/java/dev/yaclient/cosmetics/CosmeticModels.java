package dev.yaclient.cosmetics;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/** Small box models for each cosmetic slot. Coordinates are relative to the body or head part. */
public final class CosmeticModels {
    private CosmeticModels() {}

    private static ModelRenderer part(ModelBase m, int u, int v, float x, float y, float z, int w, int h, int d) {
        ModelRenderer r = new ModelRenderer(m, u, v);
        r.addBox(x, y, z, w, h, d);
        return r;
    }

    /** Two flapping wings, attached to the back of the body. */
    public static final class Wings extends ModelBase {
        private final ModelRenderer right, left;

        public Wings() {
            textureWidth = 64;
            textureHeight = 64;
            right = part(this, 0, 0, -12f, 0f, 0f, 12, 18, 1);
            right.setRotationPoint(-1.5f, 1f, 2.5f);
            left = part(this, 0, 24, 0f, 0f, 0f, 12, 18, 1);
            left.setRotationPoint(1.5f, 1f, 2.5f);
        }

        public void render(float time, float scale) {
            float flap = (float) Math.sin(time * 0.12f) * 0.18f;
            right.rotateAngleY = 0.55f + flap;
            left.rotateAngleY = -0.55f - flap;
            right.render(scale);
            left.render(scale);
        }
    }

    /** A cape hanging from the back of the body. */
    public static final class Cape extends ModelBase {
        private final ModelRenderer cape;

        public Cape() {
            textureWidth = 64;
            textureHeight = 64;
            cape = part(this, 0, 0, -5f, 0f, 0f, 10, 16, 1);
        }

        /** angle in radians, backOffset moves the cape away from the body (armor). */
        public void render(float angle, float backOffset, float scale) {
            cape.setRotationPoint(0f, 0f, 2.2f + backOffset);
            cape.rotateAngleX = angle;
            cape.render(scale);
        }
    }

    /** Top hat: brim, tube and a colored band. Attached to the head. */
    public static final class TopHat extends ModelBase {
        private final ModelRenderer brim, tube, band;

        public TopHat() {
            textureWidth = 64;
            textureHeight = 64;
            brim = part(this, 0, 0, -6f, -9f, -6f, 12, 1, 12);
            tube = part(this, 0, 16, -4f, -17f, -4f, 8, 8, 8);
            band = part(this, 0, 34, -4.5f, -11f, -4.5f, 9, 2, 9);
        }

        public void render(float scale) {
            brim.render(scale);
            tube.render(scale);
            band.render(scale);
        }
    }

    /** Gold crown with four spikes. Attached to the head. */
    public static final class Crown extends ModelBase {
        private final ModelRenderer base;
        private final ModelRenderer[] spikes = new ModelRenderer[4];

        public Crown() {
            textureWidth = 64;
            textureHeight = 64;
            base = part(this, 0, 0, -4.5f, -10.5f, -4.5f, 9, 3, 9);
            float[][] at = {{-4.5f, -4.5f}, {2.5f, -4.5f}, {-4.5f, 2.5f}, {2.5f, 2.5f}};
            for (int i = 0; i < 4; i++) spikes[i] = part(this, 0, 14, at[i][0], -13.5f, at[i][1], 2, 3, 2);
        }

        public void render(float scale) {
            base.render(scale);
            for (ModelRenderer s : spikes) s.render(scale);
        }
    }

    /** Bandana: a band around the head with a knot at the back. */
    public static final class Bandana extends ModelBase {
        private final ModelRenderer band, knot;

        public Bandana() {
            textureWidth = 64;
            textureHeight = 64;
            band = part(this, 0, 0, -4.5f, -6f, -4.5f, 9, 2, 9);
            knot = part(this, 0, 12, -1.5f, -6.5f, 4.4f, 3, 3, 2);
        }

        public void render(float scale) {
            band.render(scale);
            knot.render(scale);
        }
    }
}

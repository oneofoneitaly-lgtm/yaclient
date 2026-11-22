package dev.yaclient.cosmetics;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Draws a spinning 3D preview of a cosmetic inside a GUI (used by the locker cards). */
public final class CosmeticPreview {
    private CosmeticPreview() {}

    /**
     * @param key   cape, wings, bandana, pet or bundle
     * @param scale pixels per block
     */
    public static void draw(String key, float cx, float cy, float scale, long timeMs, boolean plus) {
        draw(key, "ya", cx, cy, scale, timeMs, plus);
    }

    /** @param variant "ya" (Beta Tester bundle) or "mono" (Monochrome bundle) */
    public static void draw(String key, String variant, float cx, float cy, float scale, long timeMs, boolean plus) {
        final boolean mono = "mono".equals(variant), qlux = "qlux".equals(variant);
        final Anime.Th an = Anime.byStyle(variant);
        XV = ExtraPix.has(key, variant) ? variant : null;
        float age = timeMs / 50f;
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, 100f);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1f, 1f, 1f, 1f);
        float sway = (float) Math.sin(age * 0.06) * 18f;

        if (key.equals("cape")) {
            GlStateManager.translate(0f, -0.5f, 0f);
            GlStateManager.rotate(sway * 0.7f, 0f, 1f, 0f);
            cape(age, plus, mono, qlux, an);
        } else if (key.equals("wings")) {
            GlStateManager.translate(0f, 0.05f, 0f);
            GlStateManager.rotate(sway * 0.5f, 0f, 1f, 0f);
            wings(age, mono, qlux, an);
        } else if (key.equals("bandana")) {
            GlStateManager.translate(0f, 0.37f, 0f);
            GlStateManager.rotate(18f, 1f, 0f, 0f);
            GlStateManager.rotate(age * 2.5f, 0f, 1f, 0f);
            CosmeticRender.mono = mono;
            CosmeticRender.qlux = qlux;
            CosmeticRender.anime = an;
            CosmeticRender.extra = XV;
            Poly.begin();
            CosmeticRender.bandana(age, 0f);
            Poly.end();
            CosmeticRender.extra = null;
            CosmeticRender.mono = false;
            CosmeticRender.qlux = false;
            CosmeticRender.anime = null;
        } else if (key.equals("pet")) {
            GlStateManager.translate(0f, 0.02f, 0f);
            GlStateManager.rotate(mono ? age * 6f : ((qlux || an != null || XV != null) ? age * 2.5f : 180f + sway * 1.6f), 0f, 1f, 0f);
            Poly.begin();
            if (XV != null) ExtraPix.pet(XV, age); else if (an != null) AnimePets.pet(an, age); else if (qlux) CosmeticQlux.pet(age); else if (mono) CosmeticMono.pet(age); else CosmeticRender.pet(age);
            Poly.end();
        } else {   // bundle
            GlStateManager.translate(0f, -0.25f, 0f);
            GlStateManager.rotate(sway * 0.5f, 0f, 1f, 0f);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0f, -0.1f, 0.1f);
            wings(age, mono, qlux, an);
            GlStateManager.popMatrix();
            cape(age, plus, mono, qlux, an);
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.95f, 0.1f, 0.25f);
            GlStateManager.scale(0.8f, 0.8f, 0.8f);
            GlStateManager.rotate(mono ? age * 6f : ((qlux || an != null || XV != null) ? age * 2.5f : 180f), 0f, 1f, 0f);
            Poly.begin();
            if (XV != null) ExtraPix.pet(XV, age); else if (an != null) AnimePets.pet(an, age); else if (qlux) CosmeticQlux.pet(age); else if (mono) CosmeticMono.pet(age); else CosmeticRender.pet(age);
            Poly.end();
            GlStateManager.popMatrix();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
        if (!depth) GlStateManager.disableDepth();
    }

    private static String XV;

    private static void cape(float age, boolean plus, boolean mono, boolean qlux, Anime.Th an) {
        capeBody(age, plus, mono, qlux, an);
        String st = XV != null ? XV : an != null ? an.id : qlux ? "qlux" : mono ? "mono" : "ya";
        Poly.begin();
        CosmeticRender.capeThickness(age, plus, CosmeticRender.capeEdgeColor(st));
        Poly.end();
    }

    private static void capeBody(float age, boolean plus, boolean mono, boolean qlux, Anime.Th an) {
        if (XV != null) { Poly.begin(); ExtraPix.cape(XV, age, plus); Poly.end(); return; }
        if (an != null) { Anime.drawCape(an, age, plus); return; }
        if (qlux) { CosmeticQlux.drawCape(age, plus); return; }
        if (mono) {
            Poly.begin();
            CosmeticMono.capeBase(age, plus);
            CosmeticMono.emblem(age, plus);
            Poly.end();
            return;
        }
        Poly.begin();
        CosmeticRender.capeBase(age, plus);
        Poly.end();
        GlStateManager.enableTexture2D();
        LogoTexture.bind();
        Poly.beginTex();
        CosmeticRender.capeEmblem(age, plus);
        Poly.end();
        GlStateManager.disableTexture2D();
    }

    private static void wings(float age, boolean mono, boolean qlux, Anime.Th an) {
        float flap = (float) Math.sin(age * 0.10) * 7f;
        for (int side = -1; side <= 1; side += 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * 0.07f, 0f, 0f);
            GlStateManager.rotate(-side * 26f, 0f, 1f, 0f);
            GlStateManager.rotate(-side * (-2f + flap), 0f, 0f, 1f);
            GlStateManager.scale((float) side, -1f, 1f);
            Poly.begin();
            CosmeticRender.qlux = qlux;
            CosmeticRender.anime = an;
            CosmeticRender.extra = XV;
            if (mono) CosmeticMono.wing(age); else CosmeticRender.wing(age);
            CosmeticRender.extra = null;
            CosmeticRender.qlux = false;
            CosmeticRender.anime = null;
            Poly.end();
            GlStateManager.popMatrix();
        }
    }
}

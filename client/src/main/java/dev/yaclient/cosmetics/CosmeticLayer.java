package dev.yaclient.cosmetics;

import dev.yaclient.Config;
import dev.yaclient.CosmeticsApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import org.lwjgl.opengl.GL11;

import java.util.Map;

/** Render layer that draws the equipped cosmetics (cape, wings, bandana, pet) and the own nametag. */
public class CosmeticLayer implements LayerRenderer<AbstractClientPlayer> {
    private static final float S = 0.0625f;

    private final RenderPlayer renderer;

    public CosmeticLayer(RenderPlayer renderer) { this.renderer = renderer; }

    @Override
    public void doRenderLayer(AbstractClientPlayer p, float limbSwing, float limbSwingAmount, float partialTicks,
                              float age, float netHeadYaw, float headPitch, float scale) {
        Minecraft mc = Minecraft.getMinecraft();
        age = clock();      // own continuous clock: walking, jumping or lag can never reset the animations
        if (p == mc.thePlayer && mc.gameSettings.thirdPersonView != 0 && mc.currentScreen == null
                && Config.getBool("nametag.self", true)) {
            Nametag.render(p, partialTicks);
        }
        if (p != mc.thePlayer && Config.getBool("nametag.others", true) && CosmeticsApi.usesClient(p.getUniqueID())) {
            Nametag.renderOther(p, partialTicks, CosmeticsApi.rank(p.getUniqueID()));
        }
        if (p.isInvisible()) return;
        // other players further than 40 blocks: the cosmetics are a few pixels, skip the heavy geometry
        if (p != mc.thePlayer && p.getDistanceSqToEntity(mc.thePlayer) > 1600.0) return;
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return;
        ModelPlayer model = renderer.getMainModel();

        boolean cape = on(eq, "cape"), wings = on(eq, "wings"), bandana = on(eq, "bandana"), pet = on(eq, "pet");
        if (!cape && !wings && !bandana && !pet) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1f, 1f, 1f, 1f);

        if (wings) drawWings(p, model, age, style(eq, "wings"));
        if (cape) drawCape(p, partialTicks, age, "1".equals(eq.get("plus")), style(eq, "cape"));
        if (bandana) {
            GlStateManager.pushMatrix();
            model.bipedHead.postRender(S);
            String bs = style(eq, "bandana");
            CosmeticRender.mono = bs.equals("mono");
            CosmeticRender.qlux = bs.equals("qlux");
            CosmeticRender.anime = Anime.byStyle(bs);
            CosmeticRender.extra = ExtraPix.has("bandana", bs) ? bs : null;
            Poly.begin();
            CosmeticRender.bandana(age, limbSwingAmount);
            Poly.end();
            CosmeticRender.extra = null;
            CosmeticRender.mono = false;
            CosmeticRender.qlux = false;
            CosmeticRender.anime = null;
            GlStateManager.popMatrix();
        }
        if (pet) drawPet(p, age, style(eq, "pet"));

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    /** Continuous animation time in ticks (20 per second), independent of the entity state. */
    private static float clock() { return (float) ((System.nanoTime() / 1000000L) % 36000000L) / 50f; }

    /** Smoothed "is in the air" factor per player, so the wing motion blends instead of jumping. */
    private static final class AirState { float v; long t; }
    private static final java.util.Map<Integer, AirState> AIR = new java.util.HashMap<Integer, AirState>();

    private static float airFactor(AbstractClientPlayer p) {
        AirState st = AIR.get(p.getEntityId());
        long now = System.nanoTime();
        if (st == null) { st = new AirState(); st.v = p.onGround ? 0f : 1f; st.t = now; AIR.put(p.getEntityId(), st); return st.v; }
        float dt = Math.min(0.1f, (now - st.t) / 1e9f);
        st.t = now;
        st.v += ((p.onGround ? 0f : 1f) - st.v) * Math.min(1f, dt * 8f);
        return st.v;
    }

    /** True if the player wears the YA cape (then the vanilla cape must be hidden). */
    static boolean wearsYaCape(AbstractClientPlayer p) {
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        return eq != null && on(eq, "cape");
    }

    /** Look of the worn item: "ya", "mono" or "qlux" (from the item id prefix). */
    static String style(Map<String, String> eq, String slot) {
        String v = eq.get(slot);
        if (v == null) return "ya";
        { int u = v.lastIndexOf('_'); if (u > 0 && ExtraPix.has(slot, v.substring(0, u))) return v.substring(0, u); }
        if (v.startsWith("mono_")) return "mono";
        if (v.startsWith("qlux_")) return "qlux";
        for (Anime.Th t : Anime.ALL) if (v.startsWith(t.id + "_")) return t.id;
        return "ya";
    }

    private static boolean on(Map<String, String> eq, String slot) {
        String v = eq.get(slot);
        return v != null && !v.equals("none");
    }

    private void drawWings(AbstractClientPlayer p, ModelPlayer model, final float age, final String style) {
        final boolean mono = style.equals("mono");
        float air = airFactor(p);
        float flap = (float) Math.sin(age * 0.10f) * (mono ? 17f : 7f) * (1f - air)
                + (float) Math.sin(age * 0.32f) * (mono ? 34f : 22f) * air;
        float raise = (mono ? -6f : -2f) + flap;
        float spread = 40f + 6f * air;
        GlStateManager.pushMatrix();
        model.bipedBody.postRender(S);
        for (int side = -1; side <= 1; side += 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * 0.07f, 0.08f, 0.19f);
            GlStateManager.rotate(-side * raise, 0f, 0f, 1f);     // flap: pure up/down around the body's front-back axis
            GlStateManager.rotate(-side * spread, 0f, 1f, 0f);    // fixed outward lean
            GlStateManager.scale((float) side, -1f, 1f);
            GeomCache.draw("w:" + style, GeomCache.bucket(age), new Runnable() { public void run() {
                Poly.begin();
                CosmeticRender.qlux = style.equals("qlux");
                CosmeticRender.anime = Anime.byStyle(style);
                CosmeticRender.extra = ExtraPix.has("wings", style) ? style : null;
                if (mono) CosmeticMono.wing(age); else CosmeticRender.wing(age);
                CosmeticRender.extra = null;
                CosmeticRender.qlux = false;
                CosmeticRender.anime = null;
                Poly.end();
            } });
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void drawCape(AbstractClientPlayer p, float pt, final float age, final boolean plus, final String style) {
        boolean mono = style.equals("mono");
        double d0 = p.prevChasingPosX + (p.chasingPosX - p.prevChasingPosX) * pt - (p.prevPosX + (p.posX - p.prevPosX) * pt);
        double d1 = p.prevChasingPosY + (p.chasingPosY - p.prevChasingPosY) * pt - (p.prevPosY + (p.posY - p.prevPosY) * pt);
        double d2 = p.prevChasingPosZ + (p.chasingPosZ - p.prevChasingPosZ) * pt - (p.prevPosZ + (p.posZ - p.prevPosZ) * pt);
        float yaw = p.prevRenderYawOffset + (p.renderYawOffset - p.prevRenderYawOffset) * pt;
        double sin = Math.sin(yaw * Math.PI / 180.0), cos = -Math.cos(yaw * Math.PI / 180.0);
        float f1 = (float) d1 * 10f;
        f1 = Math.max(-6f, Math.min(32f, f1));
        float f2 = Math.max(0f, (float) (d0 * sin + d2 * cos) * 100f);
        float f3 = (float) (d0 * cos - d2 * sin) * 100f;
        float cam = p.prevCameraYaw + (p.cameraYaw - p.prevCameraYaw) * pt;
        f1 += (float) Math.sin((p.prevDistanceWalkedModified + (p.distanceWalkedModified - p.prevDistanceWalkedModified) * pt) * 6f) * 32f * cam;
        float back = p.inventory.armorItemInSlot(2) != null ? 0.0625f : 0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 0f, 0.125f + back);
        if (p.isSneaking()) {
            f1 += 25f;
            GlStateManager.translate(0f, 0.142f, -0.0178f);
        }
        GlStateManager.rotate(6f + f2 / 2f + f1, 1f, 0f, 0f);
        GlStateManager.rotate(f3 / 2f, 0f, 0f, 1f);
        GlStateManager.rotate(-f3 / 2f, 0f, 1f, 0f);
        if (ExtraPix.has("cape", style)) {
            GeomCache.draw("c:" + style + plus, GeomCache.bucket(age), new Runnable() { public void run() {
                Poly.begin();
                ExtraPix.cape(style, age, plus);
                Poly.end();
            } });
        } else if (Anime.byStyle(style) != null) {
            Anime.drawCape(Anime.byStyle(style), age, plus);
        } else if (style.equals("qlux")) {
            CosmeticQlux.drawCape(age, plus);
        } else if (mono) {
            Poly.begin();
            CosmeticMono.capeBase(age, plus);
            CosmeticMono.emblem(age, plus);
            Poly.end();
        } else {
            Poly.begin();
            CosmeticRender.capeBase(age, plus);
            Poly.end();
            // logo emblem (textured)
            GlStateManager.enableTexture2D();
            LogoTexture.bind();
            Poly.beginTex();
            CosmeticRender.capeEmblem(age, plus);
            Poly.end();
            GlStateManager.disableTexture2D();
        }
        final int edge = CosmeticRender.capeEdgeColor(style);
        GeomCache.draw("t:" + style + plus, GeomCache.bucket(age), new Runnable() { public void run() {
            Poly.begin();
            CosmeticRender.capeThickness(age, plus, edge);
            Poly.end();
        } });
        GlStateManager.popMatrix();
    }

    private void drawPet(AbstractClientPlayer p, final float age, final String style) {
        final boolean mono = style.equals("mono"), qlux = style.equals("qlux");
        GlStateManager.pushMatrix();
        double bob = Math.sin(age * 0.10) * 0.035;
        double swayX = Math.sin(age * 0.05) * 0.04, swayZ = Math.cos(age * 0.04) * 0.05;
        GlStateManager.translate((float) (-0.64 + swayX), (float) (-0.20 + bob), (float) (0.04 + swayZ));
        if (mono) GlStateManager.rotate(age * 6f, 0f, 1f, 0f);
        else GlStateManager.rotate((float) (Math.sin(age * 0.05) * 12.0), 0f, 1f, 0f);
        GeomCache.draw("p:" + style, GeomCache.bucket(age), new Runnable() { public void run() {
            Poly.begin();
            if (ExtraPix.has("pet", style)) ExtraPix.pet(style, age); else if (Anime.byStyle(style) != null) AnimePets.pet(Anime.byStyle(style), age); else if (qlux) CosmeticQlux.pet(age); else if (mono) CosmeticMono.pet(age); else CosmeticRender.pet(age);
            Poly.end();
        } });
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}

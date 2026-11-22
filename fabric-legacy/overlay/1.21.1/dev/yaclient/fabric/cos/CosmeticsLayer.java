package dev.yaclient.fabric.cos;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yaclient.fabric.CosmeticsApi;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/** Draws the equipped cosmetics (cape, wings, bandana, pet) on players: same geometry as the 1.8.9 client. */
public class CosmeticsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public CosmeticsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) { super(parent); }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof PlayerRenderer ar) helper.register(new CosmeticsLayer((RenderLayerParent) ar));
        });
    }

    private static float clock() { return (float) ((System.nanoTime() / 1000000L) % 36000000L) / 50f; }

    private static final Map<Integer, float[]> AIR = new HashMap<Integer, float[]>();

    private static float airFactor(Entity p) {
        float[] st = AIR.get(p.getId());
        long now = System.nanoTime();
        float t = (now / 1000L) / 1e6f;
        if (st == null) { st = new float[]{p.onGround() ? 0f : 1f, t}; AIR.put(p.getId(), st); return st[0]; }
        float dt = Math.min(0.1f, t - st[1]);
        st[1] = t;
        st[0] += ((p.onGround() ? 0f : 1f) - st[0]) * Math.min(1f, dt * 8f);
        return st[0];
    }

    private static boolean on(Map<String, String> eq, String slot) {
        String v = eq.get(slot);
        return v != null && !v.equals("none");
    }

    private static String style(Map<String, String> eq, String slot) {
        String v = eq.get(slot);
        if (v == null) return "ya";
        if (v.startsWith("mono_")) return "mono";
        if (v.startsWith("qlux_")) return "qlux";
        for (Anime.Th t : Anime.ALL) if (v.startsWith(t.id + "_")) return t.id;
        return "ya";
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource c, int light, AbstractClientPlayer p, float limbSwing, float limbSwingAmount, float partial, float ageInTicks, float netHeadYaw, float headPitch) {
        if (p.isInvisible()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (p != mc.player && mc.player != null && p.distanceToSqr(mc.player) > 1600.0) return;
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return;
        boolean cape = on(eq, "cape"), wings = on(eq, "wings"), bandana = on(eq, "bandana"), pet = on(eq, "pet");
        if (!cape && !wings && !bandana && !pet) return;
        float age = clock();
        PlayerModel<AbstractClientPlayer> model = getParentModel();
        Gl.begin(ps, c);
        try {
            if (wings) {
                ps.pushPose();
                model.body.translateAndRotate(ps);
                drawWings(p, age, style(eq, "wings"));
                ps.popPose();
            }
            if (cape) drawCape(ps, p, partial, age, "1".equals(eq.get("plus")), style(eq, "cape"));
            if (bandana) {
                ps.pushPose();
                model.head.translateAndRotate(ps);
                GlStateManager.reset();
                String bs = style(eq, "bandana");
                CosmeticRender.mono = bs.equals("mono");
                CosmeticRender.qlux = bs.equals("qlux");
                CosmeticRender.anime = Anime.byStyle(bs);
                Poly.begin();
                CosmeticRender.bandana(age, 0f);
                Poly.end();
                CosmeticRender.mono = false;
                CosmeticRender.qlux = false;
                CosmeticRender.anime = null;
                ps.popPose();
            }
            if (pet) drawPet(age, style(eq, "pet"));
        } finally {
            Gl.end();
        }
    }

    private void drawWings(Player p, float age, String style) {
        boolean mono = style.equals("mono");
        float air = airFactor(p);
        float flap = (float) Math.sin(age * 0.10f) * (mono ? 17f : 7f) * (1f - air) + (float) Math.sin(age * 0.32f) * (mono ? 34f : 22f) * air;
        float raise = (mono ? -6f : -2f) + flap;
        float spread = 40f + 6f * air;
        GlStateManager.reset();
        for (int side = -1; side <= 1; side += 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * 0.07f, 0.08f, 0.19f);
            GlStateManager.rotate(-side * raise, 0f, 0f, 1f);
            GlStateManager.rotate(-side * spread, 0f, 1f, 0f);
            GlStateManager.scale((float) side, -1f, 1f);
            Poly.begin();
            CosmeticRender.qlux = style.equals("qlux");
            CosmeticRender.anime = Anime.byStyle(style);
            if (mono) CosmeticMono.wing(age); else CosmeticRender.wing(age);
            CosmeticRender.qlux = false;
            CosmeticRender.anime = null;
            Poly.end();
            GlStateManager.popMatrix();
        }
    }

    private void drawCape(PoseStack ps, AbstractClientPlayer p, float pt, float age, boolean plus, String style) {
        boolean mono = style.equals("mono");
        double d0 = Mth.lerp(pt, p.xCloakO, p.xCloak) - Mth.lerp(pt, p.xo, p.getX());
        double d1 = Mth.lerp(pt, p.yCloakO, p.yCloak) - Mth.lerp(pt, p.yo, p.getY());
        double d2 = Mth.lerp(pt, p.zCloakO, p.zCloak) - Mth.lerp(pt, p.zo, p.getZ());
        float yb = p.yBodyRotO + (p.yBodyRot - p.yBodyRotO) * pt;
        double sn = Mth.sin(yb * 0.017453292f), cs = -Mth.cos(yb * 0.017453292f);
        float f1 = Mth.clamp((float) d1 * 10f, -6f, 32f);
        float f2 = Mth.clamp((float) (d0 * sn + d2 * cs) * 100f, 0f, 150f);
        float f3 = Mth.clamp((float) (d0 * cs - d2 * sn) * 100f, -20f, 20f);
        f1 += Mth.sin(Mth.lerp(pt, p.walkDistO, p.walkDist) * 6f) * 32f * Mth.lerp(pt, p.oBob, p.bob);
        float back = !p.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? 0.0625f : 0f;
        GlStateManager.reset();
        GlStateManager.scale(-1f, 1f, 1f);
        GlStateManager.translate(0f, 0f, 0.125f + back);
        if (p.isCrouching()) {
            f1 += 25f;
            GlStateManager.translate(0f, 0.142f, -0.0178f);
        }
        GlStateManager.rotate(6f + f2 / 2f + f1, 1f, 0f, 0f);
        GlStateManager.rotate(f3 / 2f, 0f, 0f, 1f);
        GlStateManager.rotate(-f3 / 2f, 0f, 1f, 0f);
        if (Anime.byStyle(style) != null) {
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
            GlStateManager.enableTexture2D();
            LogoTexture.bind();
            Poly.beginTex();
            CosmeticRender.capeEmblem(age, plus);
            Poly.end();
            GlStateManager.disableTexture2D();
        }
    }

    private void drawPet(float age, String style) {
        boolean mono = style.equals("mono"), qlux = style.equals("qlux");
        GlStateManager.reset();
        double bob = Math.sin(age * 0.10) * 0.035;
        double swayX = Math.sin(age * 0.05) * 0.04, swayZ = Math.cos(age * 0.04) * 0.05;
        GlStateManager.translate((float) (-0.64 + swayX), (float) (-0.20 + bob), (float) (0.04 + swayZ));
        if (mono) GlStateManager.rotate(age * 6f, 0f, 1f, 0f);
        else GlStateManager.rotate((float) (Math.sin(age * 0.05) * 12.0), 0f, 1f, 0f);
        Poly.begin();
        if (Anime.byStyle(style) != null) AnimePets.pet(Anime.byStyle(style), age); else if (qlux) CosmeticQlux.pet(age); else if (mono) CosmeticMono.pet(age); else CosmeticRender.pet(age);
        Poly.end();
    }
}

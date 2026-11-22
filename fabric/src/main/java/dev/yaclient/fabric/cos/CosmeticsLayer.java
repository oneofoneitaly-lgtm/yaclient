package dev.yaclient.fabric.cos;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yaclient.fabric.CosmeticsApi;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/** Draws the equipped cosmetics (cape, wings, bandana, pet) on players: same geometry as the 1.8.9 client. */
public class CosmeticsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public CosmeticsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof AvatarRenderer ar) helper.register(new CosmeticsLayer((RenderLayerParent) ar));
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
        { int u = v.lastIndexOf('_'); if (u > 0 && ExtraPix.has(slot, v.substring(0, u))) return v.substring(0, u); }
        if (v.startsWith("mono_")) return "mono";
        if (v.startsWith("qlux_")) return "qlux";
        for (Anime.Th t : Anime.ALL) if (v.startsWith(t.id + "_")) return t.id;
        return "ya";
    }

    @Override
    public void submit(PoseStack ps, SubmitNodeCollector c, int light, AvatarRenderState s, float yRot, float xRot) {
        if (s.isInvisible) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(s.id);
        if (!(e instanceof Player p)) return;
        if (p != mc.player && s.distanceToCameraSq > 1600.0) return;
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return;
        boolean cape = on(eq, "cape"), wings = on(eq, "wings"), bandana = on(eq, "bandana"), pet = on(eq, "pet");
        if (!cape && !wings && !bandana && !pet) return;
        float age = clock();
        PlayerModel model = getParentModel();
        Gl.begin(ps, c);
        try {
            if (wings) {
                ps.pushPose();
                model.body.translateAndRotate(ps);
                drawWings(p, age, style(eq, "wings"));
                ps.popPose();
            }
            if (cape) drawCape(ps, s, age, "1".equals(eq.get("plus")), style(eq, "cape"));
            if (bandana) {
                ps.pushPose();
                model.head.translateAndRotate(ps);
                GlStateManager.reset();
                String bs = style(eq, "bandana");
                CosmeticRender.mono = bs.equals("mono");
                CosmeticRender.qlux = bs.equals("qlux");
                CosmeticRender.anime = Anime.byStyle(bs);
                CosmeticRender.extra = ExtraPix.has("bandana", bs) ? bs : null;
                Poly.begin();
                CosmeticRender.bandana(age, 0f);
                Poly.end();
                CosmeticRender.extra = null;
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
            CosmeticRender.extra = ExtraPix.has("wings", style) ? style : null;
            if (mono) CosmeticMono.wing(age); else CosmeticRender.wing(age);
            CosmeticRender.extra = null;
            CosmeticRender.qlux = false;
            CosmeticRender.anime = null;
            Poly.end();
            GlStateManager.popMatrix();
        }
    }

    private void drawCape(PoseStack ps, AvatarRenderState s, float age, boolean plus, String style) {
        boolean mono = style.equals("mono");
        float f1 = s.capeFlap, f2 = s.capeLean, f3 = s.capeLean2;
        float back = s.chestEquipment != null && !s.chestEquipment.isEmpty() ? 0.0625f : 0f;
        GlStateManager.reset();
        GlStateManager.scale(-1f, 1f, 1f);   // this render space is mirrored compared to 1.8.9: keeps cape text readable
        GlStateManager.translate(0f, 0f, 0.125f + back);
        if (s.isCrouching) {
            f1 += 25f;
            GlStateManager.translate(0f, 0.142f, -0.0178f);
        }
        GlStateManager.rotate(6f + f2 / 2f + f1, 1f, 0f, 0f);
        GlStateManager.rotate(f3 / 2f, 0f, 0f, 1f);
        GlStateManager.rotate(-f3 / 2f, 0f, 1f, 0f);
        if (ExtraPix.has("cape", style)) {
            Poly.begin();
            ExtraPix.cape(style, age, plus);
            Poly.end();
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
            GlStateManager.enableTexture2D();
            LogoTexture.bind();
            Poly.beginTex();
            CosmeticRender.capeEmblem(age, plus);
            Poly.end();
            GlStateManager.disableTexture2D();
        }
        Poly.begin();
        CosmeticRender.capeThickness(age, plus, CosmeticRender.capeEdgeColor(style));
        Poly.end();
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
        if (ExtraPix.has("pet", style)) ExtraPix.pet(style, age); else if (Anime.byStyle(style) != null) AnimePets.pet(Anime.byStyle(style), age); else if (qlux) CosmeticQlux.pet(age); else if (mono) CosmeticMono.pet(age); else CosmeticRender.pet(age);
        Poly.end();
    }
}

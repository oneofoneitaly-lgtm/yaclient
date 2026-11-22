package dev.yaclient;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

/**
 * The player's own model on the title screen, where there is no world and so no player entity:
 * the skin is fetched with the session profile and the body parts are drawn straight from the model.
 */
final class MenuPlayer {
    private MenuPlayer() {}

    private static ModelPlayer wide, slim;
    private static volatile ResourceLocation skin;
    private static volatile boolean slimArms;
    private static boolean started;

    private static void start() {
        started = true;
        final Minecraft mc = Minecraft.getMinecraft();
        final GameProfile gp = mc.getSession().getProfile();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try { mc.getSessionService().fillProfileProperties(gp, true); } catch (Throwable ignored) { }
                try {
                    mc.getSkinManager().loadProfileTextures(gp, new SkinManager.SkinAvailableCallback() {
                        public void skinAvailable(MinecraftProfileTexture.Type type, ResourceLocation loc, MinecraftProfileTexture tex) {
                            if (type == MinecraftProfileTexture.Type.SKIN) {
                                slimArms = "slim".equals(tex.getMetadata("model"));
                                skin = loc;
                            }
                        }
                    }, true);
                } catch (Throwable ignored) { }
            }
        }, "YA-MenuSkin");
        t.setDaemon(true);
        t.start();
    }

    private static void pose(ModelPlayer m) {
        m.bipedRightArm.rotateAngleZ = 0.08f; m.bipedRightArmwear.rotateAngleZ = 0.08f;
        m.bipedLeftArm.rotateAngleZ = -0.08f; m.bipedLeftArmwear.rotateAngleZ = -0.08f;
    }

    /** Draws the model standing on (cx, baseY), size = pixels per model unit, spin in degrees. */
    static void draw(int cx, int baseY, int size, float spin) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!started) start();
        if (wide == null) { wide = new ModelPlayer(0.0F, false); slim = new ModelPlayer(0.0F, true); pose(wide); pose(slim); }
        ResourceLocation loc = skin;
        boolean sl = slimArms;
        if (loc == null) {
            java.util.UUID id = mc.getSession().getProfile().getId();
            if (id == null) id = java.util.UUID.nameUUIDFromBytes("YA".getBytes());
            loc = DefaultPlayerSkin.getDefaultSkin(id);
            sl = "slim".equals(DefaultPlayerSkin.getSkinType(id));
        }
        ModelPlayer m = sl ? slim : wide;
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) cx, (float) baseY, 50.0F);
        GlStateManager.scale((float) (-size), (float) size, (float) size);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F - spin, 0.0F, 1.0F, 0.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
        GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
        GlStateManager.disableCull();
        mc.getTextureManager().bindTexture(loc);
        float s = 0.0625F;
        // the outer layers keep a constructor offset until the renderer copies the base parts onto them
        net.minecraft.client.model.ModelBase.copyModelAngles(m.bipedHead, m.bipedHeadwear);
        net.minecraft.client.model.ModelBase.copyModelAngles(m.bipedBody, m.bipedBodyWear);
        net.minecraft.client.model.ModelBase.copyModelAngles(m.bipedRightArm, m.bipedRightArmwear);
        net.minecraft.client.model.ModelBase.copyModelAngles(m.bipedLeftArm, m.bipedLeftArmwear);
        net.minecraft.client.model.ModelBase.copyModelAngles(m.bipedRightLeg, m.bipedRightLegwear);
        net.minecraft.client.model.ModelBase.copyModelAngles(m.bipedLeftLeg, m.bipedLeftLegwear);
        m.bipedHead.render(s);
        m.bipedHeadwear.render(s);
        m.bipedBody.render(s);
        m.bipedBodyWear.render(s);
        m.bipedRightArm.render(s);
        m.bipedRightArmwear.render(s);
        m.bipedLeftArm.render(s);
        m.bipedLeftArmwear.render(s);
        m.bipedRightLeg.render(s);
        m.bipedRightLegwear.render(s);
        m.bipedLeftLeg.render(s);
        m.bipedLeftLegwear.render(s);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
    }
}

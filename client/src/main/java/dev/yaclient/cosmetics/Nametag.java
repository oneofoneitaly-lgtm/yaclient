package dev.yaclient.cosmetics;

import net.minecraft.client.Minecraft;
import dev.yaclient.CosmeticsApi;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Vanilla-style nametag above your own head in third person (F5), with the YA logo next to the name. */
final class Nametag {
    private static final float LOGO = 9f, GAP = 3f;   // 9 = exactly the height of the vanilla name box

    private static final float PLUS = 8f;

    private Nametag() {}

    /** The same translucent dark box vanilla puts behind a name, so the logo and the + sit inside one label. */
    private static void box(float x0, float x1) {
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GL11.glColor4f(0f, 0f, 0f, 0.25f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x0, 8f); GL11.glVertex2f(x1, 8f); GL11.glVertex2f(x1, -1f); GL11.glVertex2f(x0, -1f);
        GL11.glEnd();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    /** Vanilla raises the name by 1.15 line heights while it also shows the below-name score (within 10 blocks). */
    private static float scoreLift(AbstractClientPlayer p, Minecraft mc) {
        net.minecraft.entity.Entity view = mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.thePlayer;   // vanilla measures from the camera entity (spectating!)
        if (view == null || p.getDistanceSqToEntity(view) >= 100.0) return 0f;
        net.minecraft.scoreboard.Scoreboard sb = p.getWorldScoreboard();
        if (sb == null || sb.getObjectiveInDisplaySlot(2) == null) return 0f;
        return -mc.fontRendererObj.FONT_HEIGHT * 1.15f;
    }

    private static float lerpYaw(float a, float b, float t) {
        float d = b - a;
        while (d < -180f) d += 360f;
        while (d >= 180f) d -= 360f;
        return a + d * t;
    }

    /**
     * Called from the player's render layer. The current matrix is the player's model matrix, so it is
     * first undone back to "entity position, no rotation" and then the label is billboarded.
     */
    static void render(AbstractClientPlayer p, float pt) {
        Minecraft mc = Minecraft.getMinecraft();
        String name = p.getName();
        if (name == null || name.isEmpty()) return;
        FontRenderer fr = mc.fontRendererObj;
        float textW = fr.getStringWidth(name);
        int rank = CosmeticsApi.ownRank();
        float badgeW = LOGO;
        float groupW = badgeW + GAP + textW;
        float x0 = -groupW / 2f;

        float bodyYaw = lerpYaw(p.prevRenderYawOffset, p.renderYawOffset, pt);
        GlStateManager.pushMatrix();
        // undo RendererLivingEntity: translate, rotate(180-yaw), scale(-1,-1,1), scale(0.9375), translate(0,-1.5078,0)
        GlStateManager.translate(0f, 1.5078125f, 0f);
        GlStateManager.scale(1f / 0.9375f, 1f / 0.9375f, 1f / 0.9375f);
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.rotate(-(180f - bodyYaw), 0f, 1f, 0f);
        // billboard above the head, same height and size as a vanilla nametag
        GlStateManager.translate(0f, p.height + (p.isSneaking() ? 0.25f : 0.5f), 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate((mc.gameSettings.thirdPersonView == 2 ? -1f : 1f) * mc.getRenderManager().playerViewX, 1f, 0f, 0f);   // same as vanilla in front view
        float s = 0.016666668f * 1.6f;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        box(x0 - 1f, x0 + groupW + 1f);
        // logo
        LogoTexture.bind(rank);
        Poly.beginTex();
        Poly.tex(x0, -1f, 0, 0f, 0f, 1f, 1f, 1f, 1f);
        Poly.tex(x0, -1f + LOGO, 0, 0f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f + LOGO, 0, 1f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f, 0, 1f, 0f, 1f, 1f, 1f, 1f);
        Poly.end();
        // name, normal Minecraft font
        fr.drawStringWithShadow(name, x0 + badgeW + GAP, 0f, 0xFFFFFFFF);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /** Other YA CLIENT players: the logo (and the gold plus for YA Plus) just left of their vanilla nametag. */
    static void renderOther(AbstractClientPlayer p, float pt, int rank) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.hideGUI || p.isInvisible() || p.isSneaking() || p.getDistanceSqToEntity(mc.getRenderViewEntity() != null ? mc.getRenderViewEntity() : mc.thePlayer) > 48 * 48) return;
        FontRenderer fr = mc.fontRendererObj;
        float textW = fr.getStringWidth(p.getDisplayName().getFormattedText());
        float groupW = LOGO;
        float x0 = -textW / 2f - GAP - groupW;
        float rx = textW / 2f + GAP;
        float bodyYaw = lerpYaw(p.prevRenderYawOffset, p.renderYawOffset, pt);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 1.5078125f, 0f);
        GlStateManager.scale(1f / 0.9375f, 1f / 0.9375f, 1f / 0.9375f);
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.rotate(-(180f - bodyYaw), 0f, 1f, 0f);
        GlStateManager.translate(0f, p.height + 0.5f, 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate((mc.gameSettings.thirdPersonView == 2 ? -1f : 1f) * mc.getRenderManager().playerViewX, 1f, 0f, 0f);   // same as vanilla in front view
        float s = 0.016666668f * 1.6f;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.translate(0f, scoreLift(p, mc), 0f);
        box(x0 - 1f, -textW / 2f - 1f);                 // joins the vanilla box on the left
        LogoTexture.bind(rank);
        Poly.beginTex();
        Poly.tex(x0, -1f, 0, 0f, 0f, 1f, 1f, 1f, 1f);
        Poly.tex(x0, -1f + LOGO, 0, 0f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f + LOGO, 0, 1f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f, 0, 1f, 0f, 1f, 1f, 1f, 1f);
        Poly.end();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}

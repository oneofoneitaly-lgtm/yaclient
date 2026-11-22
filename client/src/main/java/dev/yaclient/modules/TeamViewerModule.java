package dev.yaclient.modules;

import dev.yaclient.FriendsApi;
import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.weavemc.api.event.EventBus;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A green arrow above your teammates' heads. Teammates = same scoreboard team, your online YA friends, and names you list. */
public class TeamViewerModule extends Module {
    private final Setting.Bool team = add(new Setting.Bool("team", true));
    private final Setting.Bool friends = add(new Setting.Bool("friends", true));
    private final Setting.Text names = add(new Setting.Text("names", "", 80));
    private final Setting.Num range = add(new Setting.Num("range", 16f, 256f, 16f, 128f, ""));

    private static TeamViewerModule inst;
    private static Method pt;
    private static boolean ptTried;

    public TeamViewerModule() { super("team_viewer", Category.INFO, false); inst = this; }

    /** Green arrow above each teammate's head (world render event, looked up by name). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        String[] names = {"net.weavemc.api.event.RenderWorldEvent", "net.weavemc.api.event.RenderWorldEvent$Post"};
        for (String n : names) {
            try {
                Class c = Class.forName(n);
                EventBus.subscribe(c, (Consumer) new Consumer<Object>() {
                    @Override public void accept(Object e) { if (inst != null) inst.drawArrows(e); }
                });
                return;
            } catch (Throwable ignored) { }
        }
    }

    private static float partial(Object e) {
        try {
            if (!ptTried) { ptTried = true; pt = e.getClass().getMethod("getPartialTicks"); }
            if (pt != null) return ((Number) pt.invoke(e)).floatValue();
        } catch (Throwable ignored) { }
        return 1f;
    }

    // World pass: only remember where the arrows go and the camera matrices. The arrows themselves are drawn later,
    // on top of the finished frame (sky, clouds and weather are drawn after the entities and used to hide them).
    private final java.nio.FloatBuffer mv = org.lwjgl.BufferUtils.createFloatBuffer(16), pr = org.lwjgl.BufferUtils.createFloatBuffer(16), win = org.lwjgl.BufferUtils.createFloatBuffer(3);
    private final java.nio.IntBuffer vp = org.lwjgl.BufferUtils.createIntBuffer(16);
    private final List<double[]> spots = new ArrayList<double[]>();
    private long spotsAt;

    private void drawArrows(Object event) {
        if (!enabled) return;
        Minecraft m = Minecraft.getMinecraft();
        spots.clear();
        if (m == null || m.theWorld == null || m.thePlayer == null || m.gameSettings.hideGUI) return;
        Entity v = m.getRenderViewEntity();
        if (v == null) return;
        List<EntityPlayer> l = members();
        if (l.isEmpty()) return;
        try {
            float pt = partial(event);
            double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
            double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
            double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;
            for (EntityPlayer p : l) {
                if (p.isInvisible() || p.isDead) continue;
                double x = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt - vx;
                double y = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt - vy + p.height + 0.75 + Math.sin(System.currentTimeMillis() / 260.0) * 0.06;
                double z = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt - vz;
                spots.add(new double[] { x, y, z });
            }
            mv.clear(); pr.clear(); vp.clear();
            org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX, mv);
            org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX, pr);
            org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_VIEWPORT, vp);
            spotsAt = System.currentTimeMillis();
        } catch (Throwable t) { spots.clear(); }
    }

    /** Draws the arrows flat on the screen, after the whole world (so nothing can cover them). */
    @Override
    public void onRender() {
        if (spots.isEmpty() || System.currentTimeMillis() - spotsAt > 200 || mc.currentScreen != null) return;
        int gs = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        int ph = mc.displayHeight;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        try {
            // camera up vector in world space = second row of the model view matrix
            float ux = mv.get(1), uy = mv.get(5), uz = mv.get(9);
            for (double[] s : new ArrayList<double[]>(spots)) {
                mv.rewind(); pr.rewind(); vp.rewind(); win.clear();
                if (!org.lwjgl.util.glu.GLU.gluProject((float) s[0], (float) s[1], (float) s[2], mv, pr, vp, win)) continue;
                float sx = win.get(0), sy = win.get(1), sz = win.get(2);
                if (sz <= 0f || sz >= 1f) continue;   // behind the camera
                win.clear(); mv.rewind(); pr.rewind(); vp.rewind();
                if (!org.lwjgl.util.glu.GLU.gluProject((float) s[0] + ux, (float) s[1] + uy, (float) s[2] + uz, mv, pr, vp, win)) continue;
                float ppb = (float) Math.hypot(win.get(0) - sx, win.get(1) - sy);   // pixels per block at that distance
                double dist = Math.sqrt(s[0] * s[0] + s[1] * s[1] + s[2] * s[2]);
                float sc = 0.42f * Math.max(1f, (float) dist / 8f);
                float u = sc * ppb / gs;   // size of one arrow unit in GUI pixels
                if (u < 2f) u = 2f;
                downArrow2d(sx / gs, (ph - sy) / gs, u, 0x34D27B, 235);
            }
        } catch (Throwable ignored) { }
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    /** The same arrow as before, flat: shaft on top, head below. (ax, ay) is its base point, u the size of one unit. */
    private static void downArrow2d(float ax, float ay, float u, int rgb, int a) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        for (int pass = 0; pass < 2; pass++) {
            float o = pass == 0 ? 0.08f : 0f;
            int cr = pass == 0 ? 0 : r, cg = pass == 0 ? 0 : g, cb = pass == 0 ? 0 : b, ca = pass == 0 ? a / 2 : a;
            w.begin(7, DefaultVertexFormats.POSITION_COLOR);
            w.pos(ax + (-0.16 - o) * u, ay - (1.0 + o) * u, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(ax + (-0.16 - o) * u, ay - 0.45 * u, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(ax + (0.16 + o) * u, ay - 0.45 * u, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(ax + (0.16 + o) * u, ay - (1.0 + o) * u, 0).color(cr, cg, cb, ca).endVertex();
            t.draw();
            w.begin(7, DefaultVertexFormats.POSITION_COLOR);
            w.pos(ax + (-0.42 - o) * u, ay - 0.45 * u, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(ax, ay + (0.05 + o) * u, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(ax, ay + (0.05 + o) * u, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(ax + (0.42 + o) * u, ay - 0.45 * u, 0).color(cr, cg, cb, ca).endVertex();
            t.draw();
        }
    }

    private final List<EntityPlayer> rows = new ArrayList<EntityPlayer>();
    private long built;

    /** Same scoreboard team: the same team object, the same team name, or the same colored prefix (servers that build teams per viewer). */
    private boolean sameTeam(EntityPlayer p) {
        try {
            net.minecraft.scoreboard.Team a = mc.thePlayer.getTeam(), b = p.getTeam();
            if (a == null || b == null) return false;
            if (a == b || a.getRegisteredName().equals(b.getRegisteredName())) return true;
            if (a instanceof net.minecraft.scoreboard.ScorePlayerTeam && b instanceof net.minecraft.scoreboard.ScorePlayerTeam) {
                String pa = ((net.minecraft.scoreboard.ScorePlayerTeam) a).getColorPrefix(), pb = ((net.minecraft.scoreboard.ScorePlayerTeam) b).getColorPrefix();
                return pa != null && !pa.trim().isEmpty() && pa.equals(pb);
            }
        } catch (Throwable ignored) { }
        return false;
    }

    private List<EntityPlayer> members() {
        long now = System.currentTimeMillis();
        if (now - built < 250) return rows;
        built = now;
        rows.clear();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return rows;
        Map<String, Boolean> manual = new LinkedHashMap<String, Boolean>();
        if (names.value != null) for (String s : names.value.split(",")) if (!s.trim().isEmpty()) manual.put(s.trim().toLowerCase(Locale.ROOT), true);
        if (friends.value) {
            try { for (FriendsApi.Friend f : FriendsApi.friends()) if (f.online && f.name != null) manual.put(f.name.toLowerCase(Locale.ROOT), true); } catch (Throwable ignored) { }
        }
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p == mc.thePlayer || rows.size() >= 6) continue;
            if (mc.thePlayer.getDistanceToEntity(p) > range.value) continue;
            boolean mate = (team.value && sameTeam(p))
                    || manual.containsKey(p.getName().toLowerCase(Locale.ROOT));
            if (mate) rows.add(p);
        }
        return rows;
    }
}

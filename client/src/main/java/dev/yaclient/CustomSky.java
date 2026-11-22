package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.weavemc.api.event.EventBus;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Custom sky from the active resource pack, in the OptiFine / MCPatcher format, so a pack with a custom sky (space, aurora, ...)
 * looks right without OptiFine.
 *
 * Reads assets/minecraft/optifine/sky/world0|world-1|world1/skyN.properties (and the older mcpatcher/sky/...):
 * source, startFadeIn, endFadeIn, startFadeOut, endFadeOut, blend, rotate, speed, axis, days, daysLoop, weather.
 * The source image is a 3x2 cube map. Layers are drawn over the sky only where there is no terrain.
 * Switch: Config "customsky" (default on).
 */
public final class CustomSky {
    private CustomSky() {}

    private static final class Layer {
        ResourceLocation tex;
        ResourceLocation dyn;   // the image decoded by us (avoids missing-texture problems with big or odd images)
        int sIn = -1, eIn = -1, sOut = -1, eOut = -1;
        boolean always;
        String blend = "alpha";
        boolean rotate;
        float speed = 1f;
        int w, h;             // size of the decoded image, to keep sampling inside each face
        float[] axis = {1f, 0f, 0f};
        boolean[] days;       // null = every day
        int daysLoop = 8;
        boolean clear = true, rain = true, thunder = true;
    }

    private static final List<Layer> layers = new ArrayList<Layer>();
    private static String sig = "";
    private static int checkTick;
    private static int tickCount;

    /** Subscribes to the world render event (looked up by name so a missing event can never stop the client). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        String[] names = {"net.weavemc.api.event.RenderWorldEvent$Post", "net.weavemc.api.event.RenderWorldEvent"};
        for (String n : names) {
            try {
                Class c = Class.forName(n);
                EventBus.subscribe(c, (Consumer) new Consumer<Object>() {
                    @Override public void accept(Object e) { draw(e); }
                });
                return;
            } catch (Throwable ignored) { }
        }
        System.out.println("[YA CLIENT] custom sky: world render event not found");
    }

    private static float partial(Object e) {
        try { return ((Number) e.getClass().getMethod("getPartialTicks").invoke(e)).floatValue(); } catch (Throwable t) { return 1f; }
    }

    private static String dimName(World w) {
        int d = w.provider.getDimensionId();
        return d == -1 ? "world-1" : d == 1 ? "world1" : "world0";
    }

    private static boolean exists(Minecraft mc, ResourceLocation rl) {
        try { IResource r = mc.getResourceManager().getResource(rl); r.getInputStream().close(); return true; } catch (Throwable t) { return false; }
    }

    private static int parseTime(String s, int def) {
        if (s == null) return def;
        try {
            String[] p = s.trim().split(":");
            int h = Integer.parseInt(p[0].trim()), m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
            h -= 6; if (h < 0) h += 24;
            return (int) (h * 1000 + m / 0.06) % 24000;
        } catch (Throwable t) { return def; }
    }

    private static ResourceLocation resolve(String src, String dir) {
        src = src.trim();
        if (src.startsWith("assets/minecraft/")) src = src.substring("assets/minecraft/".length());
        if (src.startsWith("minecraft:")) return new ResourceLocation("minecraft", src.substring(10));
        int c = src.indexOf(':');
        if (c > 0) return new ResourceLocation(src.substring(0, c), src.substring(c + 1));
        if (src.startsWith("./")) return new ResourceLocation("minecraft", dir + "/" + src.substring(2));
        if (src.startsWith("~/")) return new ResourceLocation("minecraft", "optifine/" + src.substring(2));
        if (src.indexOf('/') >= 0) return new ResourceLocation("minecraft", src);
        return new ResourceLocation("minecraft", dir + "/" + src);
    }

    private static void load(Minecraft mc, String dim) {
        layers.clear();
        String[] roots = {"optifine/sky/" + dim, "mcpatcher/sky/" + dim};
        for (String root : roots) {
            int miss = 0;
            for (int i = 1; i < 200 && miss < 4; i++) {
                ResourceLocation pr = new ResourceLocation("minecraft", root + "/sky" + i + ".properties");
                InputStream in = null;
                try {
                    in = mc.getResourceManager().getResource(pr).getInputStream();
                    Properties p = new Properties();
                    p.load(in);
                    miss = 0;
                    Layer l = new Layer();
                    String src = p.getProperty("source");
                    if (src == null) src = root + "/sky" + i + ".png";
                    l.tex = resolve(src, root);
                    if (!exists(mc, l.tex)) { System.out.println("[YA CLIENT] custom sky: image not found " + l.tex); continue; }
                    try {
                        InputStream ti = mc.getResourceManager().getResource(l.tex).getInputStream();
                        java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(ti);
                        ti.close();
                        if (bi != null) { l.w = bi.getWidth(); l.h = bi.getHeight(); }
                        if (bi != null) l.dyn = mc.getTextureManager().getDynamicTextureLocation("yasky" + dim + i, new net.minecraft.client.renderer.texture.DynamicTexture(bi));
                    } catch (Throwable t) { System.out.println("[YA CLIENT] custom sky: could not decode " + l.tex + ": " + t); }
                    String a = p.getProperty("startFadeIn"), b = p.getProperty("endFadeIn"), c = p.getProperty("startFadeOut"), d = p.getProperty("endFadeOut");
                    if (a == null && b == null && c == null && d == null) l.always = true;
                    else {
                        l.sIn = parseTime(a != null ? a : b, 0);
                        l.eIn = parseTime(b != null ? b : a, l.sIn);
                        l.eOut = parseTime(d != null ? d : c, -1);
                        l.sOut = parseTime(c != null ? c : d, -1);
                        if (l.sOut < 0 && l.eOut < 0) { l.sOut = l.eOut = l.sIn - 1; l.sOut = -2; }   // never fades out
                    }
                    String bl = p.getProperty("blend");
                    if (bl != null) l.blend = bl.trim().toLowerCase();
                    l.rotate = "true".equalsIgnoreCase(p.getProperty("rotate", "true").trim());
                    try { l.speed = Float.parseFloat(p.getProperty("speed", "1").trim()); } catch (Throwable t) { }
                    String ax = p.getProperty("axis");
                    if (ax != null) {
                        String[] q = ax.trim().split("\\s+");
                        if (q.length >= 3) l.axis = new float[]{Float.parseFloat(q[0]), Float.parseFloat(q[1]), Float.parseFloat(q[2])};
                    }
                    try { l.daysLoop = Math.max(1, Integer.parseInt(p.getProperty("daysLoop", "8").trim())); } catch (Throwable t) { }
                    String dy = p.getProperty("days");
                    if (dy != null) {
                        l.days = new boolean[l.daysLoop];
                        for (String part : dy.trim().split("\\s+")) {
                            String[] r = part.split("-");
                            int lo = Integer.parseInt(r[0]), hi = r.length > 1 ? Integer.parseInt(r[1]) : lo;
                            for (int k = lo; k <= hi; k++) if (k >= 0 && k < l.daysLoop) l.days[k] = true;
                        }
                    }
                    String w = p.getProperty("weather");
                    if (w != null) {
                        w = w.toLowerCase();
                        l.clear = w.contains("clear"); l.rain = w.contains("rain"); l.thunder = w.contains("thunder");
                    }
                    layers.add(l);
                    System.out.println("[YA CLIENT] custom sky: layer " + pr + " source " + l.tex + " blend " + l.blend + " rotate " + l.rotate + " decoded " + (l.dyn != null));
                } catch (Throwable t) {
                    miss++;
                } finally {
                    try { if (in != null) in.close(); } catch (Throwable ignored) { }
                }
            }
            if (!layers.isEmpty()) return;   // optifine/ wins over the older mcpatcher/ folder
        }
    }

    /** Position of t inside [s, e] going forward around the 24000-tick day, as 0..1, or -1 when outside. */
    private static float frac(float t, int s, int e) {
        float len = ((e - s) % 24000 + 24000) % 24000;
        float off = ((t - s) % 24000 + 24000) % 24000;
        if (len == 0) return off < 1 ? 1f : -1f;
        return off <= len ? off / len : -1f;
    }

    private static float brightness(Layer l, float t) {
        if (l.always) return 1f;
        float f = frac(t, l.sIn, l.eIn);
        if (f >= 0) return f;
        if (l.sOut == -2) return 1f;                      // no fade out given: stays on
        float on = frac(t, l.eIn, l.sOut);
        if (on >= 0) return 1f;
        f = frac(t, l.sOut, l.eOut);
        if (f >= 0) return 1f - f;
        return 0f;
    }

    private static void blend(String mode) {
        switch (mode) {
            case "add": GlStateManager.enableBlend(); GlStateManager.blendFunc(770, 1); break;
            case "subtract": GlStateManager.enableBlend(); GlStateManager.blendFunc(775, 0); break;
            case "multiply": GlStateManager.enableBlend(); GlStateManager.blendFunc(0, 768); break;
            case "dodge": GlStateManager.enableBlend(); GlStateManager.blendFunc(1, 1); break;
            case "burn": GlStateManager.enableBlend(); GlStateManager.blendFunc(0, 769); break;
            case "screen": GlStateManager.enableBlend(); GlStateManager.blendFunc(1, 769); break;
            case "replace": GlStateManager.disableBlend(); break;
            default: GlStateManager.enableBlend(); GlStateManager.blendFunc(770, 771);
        }
    }

    /**
     * Sky box in world axes (+X east, +Z south, +Y up), seen from inside. Image layout, worked out from how the edges of
     * real packs join up: tile 0 = down, 1 = up, 2 = east, 3 = south, 4 = west, 5 = north (the four sides turn clockwise).
     */
    private static void cube(float h, float iu, float iv) {
        // corners TL, TR, BL, BR of every tile as x,y,z signs (times h)
        final int[][] c = {
            { -1, -1,  1,   -1, -1, -1,    1, -1,  1,    1, -1, -1 },   // 0 down
            {  1,  1,  1,    1,  1, -1,   -1,  1,  1,   -1,  1, -1 },   // 1 up
            {  1,  1, -1,    1,  1,  1,    1, -1, -1,    1, -1,  1 },   // 2 east
            {  1,  1,  1,   -1,  1,  1,    1, -1,  1,   -1, -1,  1 },   // 3 south
            { -1,  1,  1,   -1,  1, -1,   -1, -1,  1,   -1, -1, -1 },   // 4 west
            { -1,  1, -1,    1,  1, -1,   -1, -1, -1,    1, -1, -1 }    // 5 north
        };
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        for (int i = 0; i < 6; i++) {
            float u0 = (i % 3) / 3f + iu, u1 = (i % 3 + 1) / 3f - iu;
            float v0 = (i / 3) / 2f + iv, v1 = (i / 3 + 1) / 2f - iv;
            int[] k = c[i];
            w.begin(7, DefaultVertexFormats.POSITION_TEX);
            w.pos(k[0] * h, k[1] * h, k[2] * h).tex(u0, v0).endVertex();     // top left
            w.pos(k[6] * h, k[7] * h, k[8] * h).tex(u0, v1).endVertex();     // bottom left
            w.pos(k[9] * h, k[10] * h, k[11] * h).tex(u1, v1).endVertex();   // bottom right
            w.pos(k[3] * h, k[4] * h, k[5] * h).tex(u1, v0).endVertex();     // top right
            t.draw();
        }
    }

    private static void draw(Object event) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.theWorld == null || !Config.getBool("customsky", true)) return;
            World w = mc.theWorld;
            String dim = dimName(w);
            if (++checkTick >= 40 || sig.isEmpty()) {
                checkTick = 0;
                String s = dim + "|" + mc.getResourcePackRepository().getRepositoryEntries();
                if (!s.equals(sig)) { sig = s; load(mc, dim); }
            }
            if (layers.isEmpty()) return;
            float pt = partial(event);
            float t = (float) (w.getWorldTime() % 24000L) + pt;
            int day = (int) ((w.getWorldTime() / 24000L) % 100000L);
            boolean thunder = w.isThundering(), rain = w.isRaining() && !thunder, clear = !w.isRaining();
            float rainDim = 1f - w.getRainStrength(pt);

            boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
            GlStateManager.disableFog();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.enableDepth();
            GlStateManager.depthFunc(515);               // LEQUAL
            GL11.glDepthRange(1.0, 1.0);                 // far plane: only pixels with no terrain are painted
            GlStateManager.pushMatrix();
            for (Layer l : layers) {
                if (l.days != null && !l.days[day % l.daysLoop]) continue;
                if (!((clear && l.clear) || (rain && l.rain) || (thunder && l.thunder))) continue;
                float b = brightness(l, t);
                if (l.clear && l.rain && l.thunder) b *= rainDim;
                if (b <= 0.001f) continue;
                GlStateManager.pushMatrix();
                if (l.rotate) {
                    // same turn as the sun: axis given in the sun's frame, 0 at sunrise (celestial angle 0.75), box kept in world axes
                    GlStateManager.rotate(-90f, 0f, 1f, 0f);
                    if (l.axis[0] != 0f || l.axis[1] != 0f || l.axis[2] != 0f)
                        GlStateManager.rotate((w.getCelestialAngle(pt) - 0.75f) * 360f * l.speed, l.axis[0], l.axis[1], l.axis[2]);
                    GlStateManager.rotate(90f, 0f, 1f, 0f);
                }
                blend(l.blend);
                GlStateManager.color(1f, 1f, 1f, b);
                mc.getTextureManager().bindTexture(l.dyn != null ? l.dyn : l.tex);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 33071);   // clamp to edge: no seams between faces
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 33071);
                cube(30f, l.w > 0 ? 0.5f / l.w : 0f, l.h > 0 ? 0.5f / l.h : 0f);
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GL11.glDepthRange(0.0, 1.0);
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.enableAlpha();
            GlStateManager.depthMask(true);
            GlStateManager.depthFunc(515);
            if (fog) GlStateManager.enableFog();
        } catch (Throwable ignored) {
            try { GL11.glDepthRange(0.0, 1.0); } catch (Throwable t2) { }
        }
    }
}

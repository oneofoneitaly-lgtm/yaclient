package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.weavemc.api.event.EventBus;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/** Draws your own outline (color, thickness, opacity, optional fill) around the block you are looking at. */
public class BlockOutlineModule extends Module {
    private final Setting.Palette color = add("outline", new Setting.Palette("color", 2));
    private final Setting.Num thickness = add("outline", new Setting.Num("thickness", 1f, 10f, 0.5f, 3f, ""));
    private final Setting.Num opacity = add("outline", new Setting.Num("opacity", 10f, 100f, 5f, 100f, "%"));
    private final Setting.Bool fill = add("fill", new Setting.Bool("fill", false));
    private final Setting.Num fillOpacity = add("fill", new Setting.Num("fill_opacity", 5f, 80f, 5f, 20f, "%"));
    private final Setting.Bool walls = add("outline", new Setting.Bool("through_walls", false));

    private static BlockOutlineModule inst;
    private static Method pt;
    private static boolean ptTried;

    public BlockOutlineModule() { super("block_outline", Category.PVP, false); inst = this; }

    /** Subscribes to the world render event (looked up by name so a missing event can never stop the client). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        String[] names = {"net.weavemc.api.event.RenderWorldEvent", "net.weavemc.api.event.RenderWorldEvent$Post"};
        for (String n : names) {
            try {
                Class c = Class.forName(n);
                EventBus.subscribe(c, (Consumer) new Consumer<Object>() {
                    @Override public void accept(Object e) { if (inst != null) inst.draw(e); }
                });
                return;
            } catch (Throwable ignored) { }
        }
        System.out.println("[YA CLIENT] block outline: world render event not found");
    }

    private static float partial(Object e) {
        try {
            if (!ptTried) { ptTried = true; pt = e.getClass().getMethod("getPartialTicks"); }
            if (pt != null) return ((Number) pt.invoke(e)).floatValue();
        } catch (Throwable ignored) { }
        return 1f;
    }

    private void draw(Object event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.gameSettings.hideGUI) return;
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        Entity v = mc.getRenderViewEntity();
        if (v == null) return;
        try {
            BlockPos pos = mop.getBlockPos();
            Block b = mc.theWorld.getBlockState(pos).getBlock();
            if (b.getMaterial() == Material.air || !mc.theWorld.getWorldBorder().contains(pos)) return;
            b.setBlockBoundsBasedOnState(mc.theWorld, pos);
            float p = partial(event);
            double x = v.lastTickPosX + (v.posX - v.lastTickPosX) * p;
            double y = v.lastTickPosY + (v.posY - v.lastTickPosY) * p;
            double z = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * p;
            AxisAlignedBB bb = b.getSelectedBoundingBox(mc.theWorld, pos).expand(0.002, 0.002, 0.002).offset(-x, -y, -z);

            int rgb = Setting.Palette.color(color.index);
            int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, bl = rgb & 255;
            int a = Math.round(255f * opacity.value / 100f);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            if (walls.value) GlStateManager.disableDepth();
            if (fill.value) {
                GlStateManager.disableCull();
                faces(bb, r, g, bl, Math.round(255f * fillOpacity.value / 100f));
                GlStateManager.enableCull();
            }
            GL11.glLineWidth(thickness.value);
            RenderGlobal.drawOutlinedBoundingBox(bb, r, g, bl, a);
            GL11.glLineWidth(2.0f);
            if (walls.value) GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        } catch (Throwable ignored) { }
    }

    private static void faces(AxisAlignedBB b, int r, int g, int bl, int a) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        w.begin(7, DefaultVertexFormats.POSITION_COLOR);
        double x0 = b.minX, y0 = b.minY, z0 = b.minZ, x1 = b.maxX, y1 = b.maxY, z1 = b.maxZ;
        double[][] q = {
                {x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1},   // bottom
                {x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0},   // top
                {x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0},   // north
                {x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1},   // south
                {x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0},   // west
                {x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1}    // east
        };
        for (double[] f : q)
            for (int i = 0; i < 4; i++) w.pos(f[i * 3], f[i * 3 + 1], f[i * 3 + 2]).color(r, g, bl, a).endVertex();
        t.draw();
    }
}

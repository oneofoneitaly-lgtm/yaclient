package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Top-down map of the terrain around you (colors from the vanilla map palette), north is up. */
public class MinimapModule extends HudModule {
    private static final int MAX = 80;           // texture size (cells)
    private static final int SIZE = 96;          // element size

    private final Setting.Num range = add(new Setting.Num("range", 12f, 40f, 4f, 24f, ""));

    private DynamicTexture tex;
    private ResourceLocation loc;
    private final int[] heights = new int[MAX * MAX];
    private int row, originX, originZ, cells = 48;
    private boolean ready;

    public MinimapModule() { super("minimap", Category.INFO, false, 0.008f, 0.05f); }

    @Override
    public int width() { return SIZE; }

    @Override
    public int height() { return SIZE; }

    @Override
    public void onTick() {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (tex == null) {
            tex = new DynamicTexture(MAX, MAX);
            loc = mc.getTextureManager().getDynamicTextureLocation("yaminimap", tex);
        }
        int c = Math.round(range.value) * 2;
        if (row == 0 || c != cells) {
            cells = Math.min(MAX, c);
            row = 0;
            originX = (int) Math.floor(mc.thePlayer.posX) - cells / 2;
            originZ = (int) Math.floor(mc.thePlayer.posZ) - cells / 2;
        }
        int[] px = tex.getTextureData();
        int perTick = 10;
        for (int n = 0; n < perTick && row < cells; n++, row++) {
            for (int col = 0; col < cells; col++) {
                int x = originX + col, z = originZ + row;
                BlockPos top = new BlockPos(x, 0, z);
                int color = 0xFF101014, h = 0;
                if (mc.theWorld.isBlockLoaded(top)) {
                    BlockPos p = mc.theWorld.getTopSolidOrLiquidBlock(top);
                    IBlockState st = mc.theWorld.getBlockState(p.down());
                    Block b = st.getBlock();
                    h = p.getY();
                    int mc1 = b.getMapColor(st).colorValue;
                    color = 0xFF000000 | mc1;
                    if (mc1 == 0) color = 0xFF101014;
                }
                heights[row * MAX + col] = h;
                px[row * MAX + col] = color;
            }
        }
        if (row >= cells) {
            // second pass: simple relief shading from the height difference with the north-west neighbour
            for (int r = 1; r < cells; r++) {
                for (int col = 1; col < cells; col++) {
                    int d = heights[r * MAX + col] - heights[(r - 1) * MAX + (col - 1)];
                    float k = d > 0 ? 1.12f : (d < 0 ? 0.82f : 1f);
                    int argb = px[r * MAX + col];
                    int rr = Math.min(255, Math.round(((argb >> 16) & 255) * k));
                    int gg = Math.min(255, Math.round(((argb >> 8) & 255) * k));
                    int bb = Math.min(255, Math.round((argb & 255) * k));
                    px[r * MAX + col] = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
                }
            }
            tex.updateDynamicTexture();
            ready = true;
            row = 0;
        }
    }

    @Override
    protected void draw() {
        Gfx.rr(-2, -2, SIZE + 4, SIZE + 4, 8, Theme.HUD_BG);
        Gfx.rrOutline(-2, -2, SIZE + 4, SIZE + 4, 8, 1, 0x30FFFFFF);
        if (mc == null || mc.thePlayer == null) return;
        if (ready && loc != null) {
            float u = cells / (float) MAX;
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(loc);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            Tessellator t = Tessellator.getInstance();
            WorldRenderer wr = t.getWorldRenderer();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            wr.pos(0, SIZE, 0).tex(0, u).endVertex();
            wr.pos(SIZE, SIZE, 0).tex(u, u).endVertex();
            wr.pos(SIZE, 0, 0).tex(u, 0).endVertex();
            wr.pos(0, 0, 0).tex(0, 0).endVertex();
            t.draw();
            // player marker, positioned inside the map and pointing where you look
            float fx = (float) ((mc.thePlayer.posX - originX) / cells), fz = (float) ((mc.thePlayer.posZ - originZ) / cells);
            float mx = Math.max(0.03f, Math.min(0.97f, fx)) * SIZE, my = Math.max(0.03f, Math.min(0.97f, fz)) * SIZE;
            GlStateManager.disableTexture2D();
            GlStateManager.pushMatrix();
            GlStateManager.translate(mx, my, 0f);
            GlStateManager.rotate(mc.thePlayer.rotationYaw + 180f, 0f, 0f, 1f);
            wr.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            wr.pos(0, -5, 0).color(1f, 1f, 1f, 1f).endVertex();
            wr.pos(-3.5, 4, 0).color(0.9f, 0.1f, 0.1f, 1f).endVertex();
            wr.pos(3.5, 4, 0).color(0.9f, 0.1f, 0.1f, 1f).endVertex();
            t.draw();
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
        } else {
            Gfx.text("...", SIZE / 2 - 4, SIZE / 2 - 6, 10, false, Theme.MUTED);
        }
    }
}

package dev.yaclient.modules;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;

/** Shows VOID when there is nothing under your feet (BedWars, SkyWars, bridges). */
public class VoidAlertModule extends PillModule {
    private long last;
    private boolean void_;

    public VoidAlertModule() { super("void_alert", Category.PVP, false, 0.45f, 0.34f); }

    @Override
    protected String text() {
        long t = System.currentTimeMillis();
        if (t - last >= 150) {
            last = t;
            void_ = false;
            if (mc != null && mc.thePlayer != null && mc.theWorld != null && !mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying) {
                int x = (int) Math.floor(mc.thePlayer.posX), z = (int) Math.floor(mc.thePlayer.posZ);
                int y = (int) Math.floor(mc.thePlayer.posY) - 1;
                void_ = true;
                for (int i = 0; i < 40 && y - i >= 0; i++) {
                    if (mc.theWorld.getBlockState(new BlockPos(x, y - i, z)).getBlock().getMaterial() != Material.air) { void_ = false; break; }
                }
            }
        }
        return void_ ? "VOID" : "";
    }

    @Override protected int autoColor() { return 0xFFEF4444; }

    @Override protected String sample() { return "VOID"; }
}

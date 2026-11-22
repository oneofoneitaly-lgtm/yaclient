package dev.yaclient.modules;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

/** Name and position of the block under your crosshair. */
public class BlockInfoModule extends PillModule {
    public BlockInfoModule() { super("block_info", Category.INFO, false, 0.45f, 0.5f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        MovingObjectPosition m = mc.objectMouseOver;
        if (m == null || m.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || m.getBlockPos() == null) return "";
        BlockPos p = m.getBlockPos();
        IBlockState s = mc.theWorld.getBlockState(p);
        return s.getBlock().getLocalizedName() + "  (" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")";
    }

    @Override
    protected String sample() { return "Stone  (12, 64, -5)"; }
}

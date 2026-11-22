package dev.yaclient.modules;

import net.minecraft.util.BlockPos;

public class LightLevelModule extends PillModule {
    public LightLevelModule() { super("light", Category.INFO, false, 0.008f, 0.24f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return "";
        BlockPos pos = new BlockPos(mc.thePlayer);
        return "Light " + mc.theWorld.getLightFromNeighbors(pos);
    }

    @Override
    protected String sample() { return "Light 15"; }
}

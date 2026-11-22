package dev.yaclient.modules;

import net.minecraft.util.BlockPos;

public class BiomeModule extends PillModule {
    public BiomeModule() { super("biome", Category.INFO, false, 0.008f, 0.2f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return "";
        BlockPos pos = new BlockPos((int) Math.floor(mc.thePlayer.posX), 64, (int) Math.floor(mc.thePlayer.posZ));
        return "Biome: " + mc.theWorld.getBiomeGenForCoords(pos).biomeName;
    }

    @Override
    protected String sample() { return "Biome: Plains"; }
}

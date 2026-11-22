package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Module;
import dev.yaclient.fabric.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;

/** Modules that only YA Plus members can switch on. */
public final class Plus {
    private Plus() {}

    /** A soft white trail of sparkles behind you while you move. */
    public static class Trail extends Module {
        private final Setting.Num density = add("general", new Setting.Num("trail_density", 1f, 6f, 1f, 2f, ""));
        private double lx, lz;
        public Trail() { super("platrail", "YA Trail", "[Plus] A sparkle trail behind you while you move", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.player == null || mc.level == null) return;
            double x = mc.player.getX(), z = mc.player.getZ();
            boolean moved = (x - lx) * (x - lx) + (z - lz) * (z - lz) > 0.0004;
            lx = x; lz = z;
            if (!moved || (mc.options.getCameraType().isFirstPerson() && mc.player.getDeltaMovement().lengthSqr() < 0.001)) return;
            int n = Math.round(density.value);
            for (int i = 0; i < n; i++) {
                double ox = (Math.random() - 0.5) * 0.5, oz = (Math.random() - 0.5) * 0.5;
                mc.level.addParticle(ParticleTypes.END_ROD, x + ox, mc.player.getY() + 0.1 + Math.random() * 0.3, z + oz, 0.0, 0.01, 0.0);
            }
        }
    }

    /** Extra crit sparks on the player you hit. */
    public static class HitSparks extends Module {
        public HitSparks() { super("plushit", "Hit Sparks", "[Plus] Crit sparks on every player or mob you hit", Category.PVP, false); }

        public static void spark(Entity target) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            for (int i = 0; i < 14; i++) {
                mc.level.addParticle(ParticleTypes.CRIT, target.getX() + (Math.random() - 0.5) * 0.8, target.getY() + target.getBbHeight() * Math.random(), target.getZ() + (Math.random() - 0.5) * 0.8,
                    (Math.random() - 0.5) * 0.4, 0.2 + Math.random() * 0.3, (Math.random() - 0.5) * 0.4);
            }
        }
    }

    /** The text of all HUD boxes cycles through soft colours. Handled in Extra.rainbow. */
    public static class RainbowHud extends Module {
        public RainbowHud() { super("rainbowhud", "Rainbow HUD", "[Plus] HUD text slowly cycles through soft colours", Category.UTILITY, false); }
    }
}

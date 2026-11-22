package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import dev.yaclient.fabric.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Full screen things: custom crosshair and the low health vignette. They have no draggable box. */
public final class Overlays {
    private Overlays() {}

    public static class Crosshair extends HudModule {
        private final Setting.Palette color = add("colors", new Setting.Palette("color", 0));
        private final Setting.Num size = add("general", new Setting.Num("size", 2f, 10f, 1f, 5f, ""));
        private final Setting.Num gap = add("general", new Setting.Num("gap", 0f, 8f, 1f, 2f, ""));
        public Crosshair() { super("crosshair", "Crosshair", "Small white cross in the middle (hides nothing else)", Category.PVP, false, 0.5f, 0.5f); }

        private int span() { return Math.round(gap.value) * 2 + Math.round(size.value) * 2 + 1; }

        @Override public int width(Minecraft mc) { return span(); }

        @Override public float screenX(int sw, Minecraft mc) { return (float) Math.floor((sw - screenW(mc)) / 2f); }

        @Override public float screenY(int sh, Minecraft mc) { return (float) Math.floor((sh - screenH(mc)) / 2f); }

        @Override public int height(Minecraft mc) { return span(); }

        @Override
        public void draw(GuiGraphicsExtractor g, Minecraft mc) {
            int s = Math.round(size.value), gp = Math.round(gap.value), c = color.rgb(), m = s + gp;
            g.fill(m, 0, m + 1, s, c);
            g.fill(m, m + 1 + gp, m + 1, 2 * m + 1, c);
            g.fill(0, m, s, m + 1, c);
            g.fill(m + 1 + gp, m, 2 * m + 1, m + 1, c);
        }
    }

    public static class LowHealth extends HudModule {
        private final Setting.Num limit = add("general", new Setting.Num("health_limit", 2f, 12f, 1f, 6f, ""));
        private final Setting.Palette color = add("colors", new Setting.Palette("color", 2));
        public LowHealth() { super("lowhealth", "Low Health", "Red border when your health is low", Category.PVP, false, 0f, 0f); }

        @Override public int width(Minecraft mc) { return 1; }

        @Override public int height(Minecraft mc) { return 1; }

        @Override
        public void draw(GuiGraphicsExtractor g, Minecraft mc) {
            if (mc.player == null || mc.player.getHealth() > limit.value) return;
            int w = g.guiWidth(), h = g.guiHeight(), t = 8, c = (0x66 << 24) | (color.rgb() & 0xFFFFFF);
            g.pose().pushMatrix();
            g.pose().scale(1f / scale, 1f / scale);
            g.pose().translate(-Math.round(px * w), -Math.round(py * h));
            g.fill(0, 0, w, t, c);
            g.fill(0, h - t, w, h, c);
            g.fill(0, t, t, h - t, c);
            g.fill(w - t, t, w, h - t, c);
            g.pose().popMatrix();
        }
    }
}

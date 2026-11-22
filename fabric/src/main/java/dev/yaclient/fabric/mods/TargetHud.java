package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Gfx;
import dev.yaclient.fabric.HudModule;
import dev.yaclient.fabric.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;

/** Name and health of the entity you aim at (stays for 3 seconds after you look away). */
public class TargetHud extends HudModule {
    private LivingEntity last;
    private long seen;

    public TargetHud() { super("targethud", "Target HUD", "Health of the player you are aiming at", Category.CRYSTAL, false, 0.42f, 0.62f); }

    private LivingEntity target(Minecraft mc) {
        if (mc.crosshairPickEntity instanceof LivingEntity le) { last = le; seen = System.currentTimeMillis(); }
        if (last != null && (last.isRemoved() || System.currentTimeMillis() - seen > 3000)) last = null;
        return last;
    }

    @Override public int width(Minecraft mc) { return target(mc) == null ? 0 : 130; }

    @Override public int height(Minecraft mc) { return 36; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        LivingEntity t = target(mc);
        if (t == null) return;
        int w = 130, h = 36;
        Gfx.rr(0, 0, w, h, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, w, h, 8, 1, 0x1AFFFFFF);
        Gfx.text(Gfx.trim(t.getName().getString(), 11, true, w - 60), 10, 7, 11, true, Theme.TEXT);
        float hp = t.getHealth(), max = Math.max(1f, t.getMaxHealth());
        float f = Math.max(0f, Math.min(1f, hp / max));
        String s = String.format("%.1f", hp);
        Gfx.text(s, w - 10 - Gfx.width(s, 10, true), 8, 10, true, Theme.MUTED);
        Gfx.rr(10, 25, w - 20, 5, 2, 0xFF2D2E34);
        Gfx.rr(10, 25, Math.max(3, Math.round((w - 20) * f)), 5, 2, f > 0.5f ? 0xFF22C55E : f > 0.25f ? 0xFFFFC21A : 0xFFEF4444);
    }
}

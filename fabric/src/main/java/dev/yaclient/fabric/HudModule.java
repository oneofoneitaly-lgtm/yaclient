package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A module with a movable, resizable HUD element. Position is a fraction of the screen,
 * draw() always paints at (0, 0) with Gfx (same look as the 1.8.9 client).
 */
public abstract class HudModule extends Module {
    public static final float MIN_SCALE = 0.4f, MAX_SCALE = 3f, DEF_SCALE = 0.8f;
    public float px, py, scale = DEF_SCALE;
    private final float defX, defY;
    /** Shared by every HUD element: how see-through the whole element is. */
    public final Setting.Num opacity = add("style", new Setting.Num("opacity", 20f, 100f, 5f, 100f, "%"));

    protected HudModule(String id, String name, String description, Category category, boolean enabledByDefault, float defX, float defY) {
        super(id, name, description, category, enabledByDefault);
        this.defX = defX;
        this.defY = defY;
        this.px = defX;
        this.py = defY;
    }

    public abstract int width(Minecraft mc);

    public abstract int height(Minecraft mc);

    /** Paints the element with its top left corner at (0, 0) using Gfx. */
    public abstract void draw(GuiGraphicsExtractor g, Minecraft mc);

    @Override
    public void load() {
        super.load();
        px = Config.getFloat("hud." + id + ".x", defX);
        py = Config.getFloat("hud." + id + ".y", defY);
        scale = Ui.clamp(Config.getFloat("hud." + id + ".s", DEF_SCALE), MIN_SCALE, MAX_SCALE);
    }

    @Override
    public void save() {
        super.save();
        Config.setFloat("hud." + id + ".x", px);
        Config.setFloat("hud." + id + ".y", py);
        Config.setFloat("hud." + id + ".s", scale);
    }

    public void resetLayout() { px = defX; py = defY; scale = DEF_SCALE; }

    /** Sets the Gfx pixel scale for this element and returns its measured size on screen (GUI units). */
    private void prepare(GuiGraphicsExtractor g, Minecraft mc) {
        Gfx.g = g;
        Gfx.fade = opacity.value / 100f;
        Gfx.setScale(Math.max(1, Math.round(mc.getWindow().getGuiScale() * scale)));
    }

    public float screenW(Minecraft mc) { return width(mc) * scale; }

    public float screenH(Minecraft mc) { return height(mc) * scale; }

    public float screenX(int sw, Minecraft mc) { return Ui.clamp(px * sw, 0f, Math.max(0f, sw - screenW(mc))); }

    public float screenY(int sh, Minecraft mc) { return Ui.clamp(py * sh, 0f, Math.max(0f, sh - screenH(mc))); }

    public final void render(GuiGraphicsExtractor g, Minecraft mc) {
        prepare(g, mc);
        if (width(mc) <= 0) return;
        int gs = Math.max(1, mc.getWindow().getGuiScale());
        float x = Math.round(screenX(g.guiWidth(), mc) * gs) / (float) gs;
        float y = Math.round(screenY(g.guiHeight(), mc) * gs) / (float) gs;
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        draw(g, mc);
        g.pose().popMatrix();
        Gfx.fade = 1f;
    }

    /** Measures with the right pixel scale (menu hit testing). */
    public final void measure(Minecraft mc) { Gfx.setScale(Math.max(1, Math.round(mc.getWindow().getGuiScale() * scale))); }
}

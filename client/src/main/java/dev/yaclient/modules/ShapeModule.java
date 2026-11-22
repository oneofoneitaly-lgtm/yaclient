package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;

/** A plain coloured shape: a divider, a frame, a banner behind other HUD elements. */
public class ShapeModule extends HudModule {
    private final Setting.Num w = add("layout", new Setting.Num("shape_w", 4f, 400f, 2f, 60f, ""));
    private final Setting.Num h = add("layout", new Setting.Num("shape_h", 2f, 300f, 2f, 20f, ""));
    private final Setting.Num rad = add("style", new Setting.Num("radius", 0f, 40f, 1f, 6f, ""));
    private final Setting.Palette color = add("colors", new Setting.Palette("shape_color", 0));
    private final Setting.Bool outline = add("style", new Setting.Bool("outline_only", false));

    public ShapeModule() { super("shape", Category.UTILITY, false, 0.45f, 0.45f); }

    @Override
    public int width() { return Math.round(w.value); }

    @Override
    public int height() { return Math.round(h.value); }

    @Override
    protected void draw() {
        int ww = width(), hh = height(), r = Math.min(Math.round(rad.value), Math.min(ww, hh) / 2);
        int c = 0xFF000000 | (color.rgb() & 0xFFFFFF);
        if (outline.value) Gfx.rrOutline(0, 0, ww, hh, r, 2, c);
        else Gfx.rr(0, 0, ww, hh, r, c);
    }
}

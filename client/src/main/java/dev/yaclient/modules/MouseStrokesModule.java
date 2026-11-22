package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Ui;

/** A small square where a dot follows your mouse movement (aim visualiser). */
public class MouseStrokesModule extends HudModule {
    private final Setting.Num size = add("layout", new Setting.Num("box_size", 32f, 96f, 2f, 48f, ""));
    private final Setting.Num sens = add("general", new Setting.Num("sensitivity", 1f, 10f, 1f, 4f, ""));
    private float ox, oy, tx, ty;

    public MouseStrokesModule() { super("mouse_strokes", Category.PVP, false, 0.008f, 0.62f); }

    @Override
    public void onRender() {
        if (mc == null || mc.mouseHelper == null || mc.currentScreen != null) return;
        float k = sens.value * 0.15f;
        ox += mc.mouseHelper.deltaX * k;
        oy -= mc.mouseHelper.deltaY * k;
        float lim = size.value / 2f - 6f;
        ox = Ui.clamp(ox * 0.9f, -lim, lim);
        oy = Ui.clamp(oy * 0.9f, -lim, lim);
        tx += (ox - tx) * 0.25f;
        ty += (oy - ty) * 0.25f;
    }

    @Override
    public int width() { return Math.round(size.value); }

    @Override
    public int height() { return Math.round(size.value); }

    @Override
    protected void draw() {
        int s = Math.round(size.value), c = s / 2;
        Gfx.rr(0, 0, s, s, 5, 0xB8121216);
        Gfx.rrOutline(0, 0, s, s, 5, 1, 0x1FFFFFFF);
        double a = preview ? System.currentTimeMillis() / 600.0 : 0;
        int px = c + Math.round(preview ? (float) Math.cos(a) * (s / 4f) : tx);
        int py = c + Math.round(preview ? (float) Math.sin(a) * (s / 4f) : ty);
        Gfx.rr(c - 2 + Math.round((px - c) * 0.6f), c - 2 + Math.round((py - c) * 0.6f), 5, 5, 2, 0xFFEF4444);
        Gfx.rr(px - 3, py - 3, 7, 7, 3, 0xFFFFFFFF);
    }
}

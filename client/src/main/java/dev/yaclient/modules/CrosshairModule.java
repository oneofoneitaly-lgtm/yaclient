package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Crosshair you draw yourself on a pixel grid (see the options page). While the module is enabled the
 * vanilla crosshair is not drawn (see YaIngame).
 */
public class CrosshairModule extends Module {
    public static final int N = 11;
    public static CrosshairModule instance;

    public final Setting.Pixels grid = add("draw", new Setting.Pixels("grid", N, preset("cross")));
    private final Setting.Palette color = add("draw", new Setting.Palette("color", 0));
    private final Setting.Num size = add("draw", new Setting.Num("pixel", 1f, 4f, 1f, 2f, "px"));
    private final Setting.Bool outline = add("style", new Setting.Bool("outline", true));
    private final Setting.Num opacity = add("style", new Setting.Num("opacity", 20f, 100f, 5f, 100f, "%"));

    public CrosshairModule() { super("crosshair", Category.PVP, false); instance = this; }

    public int pixelSize() { return Math.max(1, Math.round(size.value)); }

    /** True while the vanilla crosshair must be hidden. */
    public static boolean hidesVanilla() { return instance != null && instance.enabled; }

    /** Ready-made patterns for the editor's quick buttons. */
    public static String preset(String name) {
        char[] c = new char[N * N];
        java.util.Arrays.fill(c, '0');
        int m = N / 2;
        for (int i = 0; i < N; i++) for (int j = 0; j < N; j++) {
            int dx = j - m, dy = i - m, ax = Math.abs(dx), ay = Math.abs(dy);
            boolean on;
            switch (name) {
                case "plus": on = (dx == 0 || dy == 0) && Math.max(ax, ay) <= 4; break;
                case "dot": on = ax <= 1 && ay <= 1; break;
                case "ring": { double r = Math.sqrt(dx * dx + dy * dy); on = r >= 3.6 && r <= 4.9; break; }
                case "square": on = Math.max(ax, ay) == 4; break;
                case "x": on = ax == ay && ax <= 4 && ax >= 1; break;
                default: on = (dx == 0 || dy == 0) && Math.max(ax, ay) >= 2 && Math.max(ax, ay) <= 5; // cross with a gap
            }
            if (on) c[i * N + j] = '1';
        }
        return new String(c);
    }

    private int argb() {
        int a = Math.round(2.55f * opacity.value);
        return (a << 24) | (Setting.Palette.color(color.index) & 0xFFFFFF);
    }

    @Override
    public void onRender() {
        if (mc.gameSettings.thirdPersonView != 0 || mc.currentScreen != null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        Gfx.setScale(sr.getScaleFactor());
        Gfx.fade = 1f;
        drawAt(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 1f);
    }

    /** Draws the drawing centered on (cx, cy); used by the game and by the options preview. */
    public void drawAt(int cx, int cy, float zoom) {
        int px = Math.max(1, Math.round(size.value * zoom));
        int x0 = cx - (N * px) / 2, y0 = cy - (N * px) / 2;
        int c = argb(), o = c & 0xFF000000;
        boolean[] g = grid.cells;
        if (outline.value) {
            for (int i = 0; i < N; i++) for (int j = 0; j < N; j++)
                if (g[i * N + j]) Gfx.rr(x0 + j * px - 1, y0 + i * px - 1, px + 2, px + 2, 0, o);
        }
        for (int i = 0; i < N; i++) for (int j = 0; j < N; j++)
            if (g[i * N + j]) Gfx.rr(x0 + j * px, y0 + i * px, px, px, 0, c);
    }
}

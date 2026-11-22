package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Locale;
import java.util.function.Function;

/** One line in a rounded pill, or several lines in a card, with the same look options as the 1.8.9 pills. */
public class TextHud extends HudModule {
    private final Function<Minecraft, String> fn;
    /** Optional color chosen by the module (e.g. FPS colorize). Return 0 to use the chosen text color. */
    public Function<Minecraft, Integer> autoColor;

    public final Setting.Bool background = add("style", new Setting.Bool("bg", true));
    public final Setting.Num bgOpacity = add("style", new Setting.Num("bg_opacity", 0f, 100f, 5f, 72f, "%"));
    public final Setting.Choice bgColor = add("style", new Setting.Choice("bg_color", 0, "dark", "black", "navy", "wine"));
    public final Setting.Num radius = add("style", new Setting.Num("radius", 0f, 10f, 1f, 6f, ""));
    public final Setting.Bool border = add("style", new Setting.Bool("border", true));
    public final Setting.Palette borderColor = add("style", new Setting.Palette("border_color", 0));
    public final Setting.Palette textColor = add("colors", new Setting.Palette("text_color", 0));
    public final Setting.Bool shadow = add("text", new Setting.Bool("shadow", false));
    public final Setting.Bool bold = add("text", new Setting.Bool("bold", false));
    public final Setting.Bool upper = add("text", new Setting.Bool("upper", false));
    public final Setting.Num textSize = add("text", new Setting.Num("text_size", 8f, 16f, 1f, 10f, ""));
    public final Setting.Num padding = add("layout", new Setting.Num("padding", 3f, 16f, 1f, 8f, ""));
    public final Setting.Num boxHeight = add("layout", new Setting.Num("box_height", 12f, 30f, 1f, 16f, ""));

    private static final int[] BG = {0x121216, 0x000000, 0x0B1530, 0x2A0C10};

    public TextHud(String id, String name, String desc, Category cat, boolean def, float x, float y, Function<Minecraft, String> fn) {
        super(id, name, desc, cat, def, x, y);
        this.fn = fn;
    }

    private String[] lines(Minecraft mc) {
        String s;
        try { s = fn.apply(mc); } catch (Exception e) { s = ""; }
        if (s == null || s.isEmpty()) return new String[0];
        if (upper.value) s = s.toUpperCase(Locale.ROOT);
        return s.split("\n");
    }

    private int size() { return Math.round(textSize.value); }

    @Override
    public int width(Minecraft mc) {
        String[] l = lines(mc);
        if (l.length == 0) return 0;
        int w = 0;
        for (String s : l) w = Math.max(w, Gfx.width(s, size(), bold.value));
        return w + 2 * Math.round(padding.value);
    }

    @Override
    public int height(Minecraft mc) {
        int n = lines(mc).length;
        if (n == 0) return 0;
        return n == 1 ? Math.round(boxHeight.value) : 10 + n * (Gfx.lineHeight(size(), bold.value) + 3) - 3;
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        String[] l = lines(mc);
        if (l.length == 0) return;
        int w = width(mc), h = height(mc), pad = Math.round(padding.value);
        int r = l.length == 1 ? Math.max(1, Math.round(radius.value)) : Math.max(1, Math.round(radius.value) + 2);
        if (background.value) {
            int a = Math.round(255f * bgOpacity.value / 100f);
            Gfx.rr(0, 0, w, h, r, (a << 24) | BG[Ui.clamp(bgColor.index, 0, BG.length - 1)]);
        }
        if (border.value) {
            int bc = borderColor.rgb() & 0xFFFFFF;
            Gfx.rrOutline(0, 0, w, h, r, 1, (borderColor.index == 0 ? 0x1A : 0x99) << 24 | bc);
        }
        int col = textColor.rgb();
        if (autoColor != null) { int c = autoColor.apply(mc); if (c != 0) col = c; }
        int lh = Gfx.lineHeight(size(), bold.value);
        int y = l.length == 1 ? (h - lh) / 2 : 5;
        for (String s : l) {
            if (shadow.value) Gfx.text(s, pad + 1, y + 1, size(), bold.value, 0x99000000);
            Gfx.text(s, pad, y, size(), bold.value, col);
            y += lh + 3;
        }
    }
}

package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import dev.yaclient.Ui;

/** HUD element that shows one line of text in a rounded pill, with a full set of look options. */
public abstract class PillModule extends HudModule {
    protected final Setting.Bool background = add("style", new Setting.Bool("bg", true));
    protected final Setting.Num bgOpacity = add("style", new Setting.Num("bg_opacity", 0f, 100f, 5f, 62f, "%"));
    protected final Setting.Choice bgColor = add("style", new Setting.Choice("bg_color", 1, "dark", "black", "navy", "wine", "accent"));
    protected final Setting.Num radius = add("style", new Setting.Num("radius", 0f, 10f, 1f, 4f, ""));
    protected final Setting.Bool border = add("style", new Setting.Bool("border", false));
    protected final Setting.Palette borderColor = add("style", new Setting.Palette("border_color", 0));
    protected final Setting.Palette textColor = add("colors", new Setting.Palette("text_color", 0));
    protected final Setting.Bool shadow = add("text", new Setting.Bool("shadow", false));
    protected final Setting.Bool bold = add("text", new Setting.Bool("bold", false));
    protected final Setting.Bool upper = add("text", new Setting.Bool("upper", false));
    protected final Setting.Num textSize = add("text", new Setting.Num("text_size", 8f, 16f, 1f, 10f, ""));
    protected final Setting.Num padding = add("layout", new Setting.Num("padding", 3f, 16f, 1f, 8f, ""));
    protected final Setting.Num boxHeight = add("layout", new Setting.Num("box_height", 12f, 30f, 1f, 16f, ""));

    private static final int[] BG = {0x121216, 0x000000, 0x0B1530, 0x2A0C10, -1};

    protected PillModule(String id, Category category, boolean enabledByDefault, float defX, float defY) {
        super(id, category, enabledByDefault, defX, defY);
    }

    /** Optional picture at the left of the text (server icon, pack icon). Subclasses opt in. */
    protected boolean iconOn() { return false; }
    protected int iconPx() { return 12; }
    protected void drawIcon(int x, int y, int size) { }
    /** False = icon only (the text is still what hides the element when empty). */
    protected boolean textOn() { return true; }

    /** Current text. Return an empty string to hide the element. */
    protected abstract String text();

    /** Text shown in the HUD editor when text() is empty. */
    protected String sample() { return name(); }

    /** Modules can override the text color (e.g. green/red by value). Return 0 to use the chosen color. */
    protected int autoColor() { return 0; }

    private String cached = "";
    private long cachedAt;

    /** text() at most 10 times a second: width(), height() and draw() would otherwise rebuild it several times per frame. */
    private String cachedText() {
        long now = System.currentTimeMillis();
        if (now - cachedAt >= 100 || now < cachedAt) { cachedAt = now; cached = text(); }
        return cached;
    }

    private String shown() {
        String t = cachedText();
        if ((t == null || t.isEmpty()) && preview) t = sample();
        if (t == null) return "";
        return upper.value ? t.toUpperCase(java.util.Locale.ROOT) : t;
    }

    private int size() { return Math.round(textSize.value); }

    private int contentW(String t) {
        boolean tx = textOn(), ic = iconOn();
        return (tx ? Gfx.width(t, size(), bold.value) : 0) + (ic ? iconPx() + (tx ? 4 : 0) : 0);
    }

    @Override
    public int width() {
        String t = shown();
        return t.isEmpty() ? 0 : contentW(t) + 2 * Math.round(padding.value);
    }

    @Override
    public int height() { return Math.max(Math.round(boxHeight.value), iconOn() ? iconPx() + 4 : 0); }

    @Override
    protected void draw() {
        String t = shown();
        if (t.isEmpty()) return;
        int pad = Math.round(padding.value), h = height(), r = Math.max(1, Math.round(radius.value));
        int w = contentW(t) + 2 * pad;
        if (background.value) {
            int rgb = BG[Ui.clamp(bgColor.index, 0, BG.length - 1)];
            int c = (rgb < 0 ? Theme.accent() : (0xFF000000 | rgb)) & 0xFFFFFF;
            int a = Math.round(255f * bgOpacity.value / 100f);
            Gfx.rr(0, 0, w, h, r, (a << 24) | c);
        }
        if (border.value && (background.value || true)) {
            int bc = borderColor.rgb() & 0xFFFFFF;
            Gfx.rrOutline(0, 0, w, h, r, 1, (borderColor.index == 0 ? 0x1A : 0x99) << 24 | bc);
        }
        int col = autoColor();
        if (col == 0) col = textColor.rgb();
        int ty = (h - Gfx.lineHeight(size(), bold.value)) / 2;
        int tx0 = pad;
        if (iconOn()) { drawIcon(pad, (h - iconPx()) / 2, iconPx()); tx0 += iconPx() + 4; }
        if (!textOn()) return;
        if (shadow.value) Gfx.text(t, tx0 + 1, ty + 1, size(), bold.value, 0x99000000);
        Gfx.text(t, tx0, ty, size(), bold.value, col);
    }
}

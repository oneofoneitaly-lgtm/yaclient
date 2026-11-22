package dev.yaclient;

/** Colors and accent presets. The brand red is only used for the "CLIENT" wordmark. */
public final class Theme {
    public static final int BRAND_RED = 0xFFE10600;
    public static final int TEXT = 0xFFF4F4F5;
    public static final int MUTED = 0xFF9A9AA5;
    public static final int DIM = 0xFF6B6B76;

    public static final int GLASS = 0xE6101015;      // main panel (translucent)
    public static final int GLASS_LINE = 0x24FFFFFF; // thin highlight outline
    public static final int CARD = 0x0EFFFFFF;
    public static final int CARD_HOVER = 0x1AFFFFFF;
    public static final int HUD_BG = 0xB8121216;

    public static final String[] ACCENT_IDS = {"crimson", "azure", "violet", "emerald", "amber", "mono", "gold"};
    private static final int[] ACCENTS = {0xFFE10600, 0xFF3B82F6, 0xFF8B5CF6, 0xFF10B981, 0xFFF59E0B, 0xFFF5F5F5, 0xFFFFC21A};
    private static int index = 0;

    private Theme() {}

    public static void load() { setAccent(0); }

    /** Falls back to the default accent when Plus is not active. */
    public static void enforcePlus(boolean plus) { if (!plus && plusOnly(index)) { index = 0; Config.setInt("accent", 0); } }

    public static int accentIndex() { return index; }

    /** The last accent (gold) belongs to YA Plus. */
    public static boolean plusOnly(int i) { return i == ACCENTS.length - 1; }

    /** The accent is always the brand red; the picker was removed. */
    public static void setAccent(int i) {
        index = 0;
        Config.setInt("accent", 0);
    }

    public static int accent() { return ACCENTS[index]; }

    public static int accentOf(int i) { return ACCENTS[Ui.clamp(i, 0, ACCENTS.length - 1)]; }

    /** Text color that stays readable on top of the accent color. */
    public static int onAccent() {
        int c = ACCENTS[index];
        int lum = (((c >> 16) & 255) * 299 + ((c >> 8) & 255) * 587 + (c & 255) * 114) / 1000;
        return lum > 150 ? 0xFF101014 : 0xFFFFFFFF;
    }
}

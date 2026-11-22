package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;

/** A tiny styled text box for the menus (same look as the 1.8.9 one). */
final class Field {
    String value = "";
    final int max;
    boolean focus;
    int x, y, w, h;

    Field(int max) { this.max = max; }

    boolean hit(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }

    /** A typed character (codepoint). */
    void type(int cp) {
        if (cp < 32 || cp == 127 || cp == 167) return;
        String s = new String(Character.toChars(cp));
        if (value.length() + s.length() <= max) value += s;
    }

    /** A pressed key: backspace and Ctrl+V. True if handled. */
    boolean key(int key, int mods) {
        if (key == 259) { if (!value.isEmpty()) value = value.substring(0, value.offsetByCodePoints(value.length(), -1)); return true; }
        if (key == 86 && (mods & 2) != 0) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clip != null) clip.codePoints().forEach(this::type);
            return true;
        }
        return false;
    }

    void draw(String hint, int size) {
        Gfx.rr(x, y, w, h, 5, focus ? 0x30FFFFFF : 0x18FFFFFF);
        Gfx.rrOutline(x, y, w, h, 5, 1, focus ? 0xCCFFFFFF : 0x30FFFFFF);
        int ty = y + (h - Gfx.lineHeight(size, false)) / 2;
        if (value.isEmpty()) Gfx.text(Gfx.trim(hint, size, false, w - 12), x + 6, ty, size, false, Theme.DIM);
        else {
            String shown = Gfx.trim(value, size, false, w - 14);
            Gfx.text(shown, x + 6, ty, size, false, Theme.TEXT);
            if (focus && (System.currentTimeMillis() / 500) % 2 == 0) Gfx.rr(x + 7 + Gfx.width(shown, size, false), ty, 1, Gfx.lineHeight(size, false), 0, 0xFFFFFFFF);
        }
        if (focus && value.isEmpty() && (System.currentTimeMillis() / 500) % 2 == 0) Gfx.rr(x + 6, ty, 1, Gfx.lineHeight(size, false), 0, 0xFFFFFFFF);
    }
}

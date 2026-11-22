package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

/** Your account in one place: spinning skin, name, id, and quick actions (copy, NameMC, cosmetics, friends). */
public class AccountScreen extends GuiScreen {
    private final GuiScreen parent;
    private final long opened = System.currentTimeMillis();
    private final float[] hov = new float[5];
    private long last = opened;
    private int px, py, pw, ph, bx, bw, by0;
    private String note = "";
    private long noteAt;

    public AccountScreen(GuiScreen parent) { this.parent = parent; }

    private String name() { return Minecraft.getMinecraft().getSession().getUsername(); }

    private String id() {
        String s = Minecraft.getMinecraft().getSession().getPlayerID();
        return s == null ? "" : s;
    }

    private String dashed() {
        String s = id();
        if (s.length() != 32) return s;
        return s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20);
    }

    private String kind() {
        String s = id();
        return s.length() == 32 && !s.equals("0") && !s.startsWith("0000") ? Lang.t("acc.premium") : Lang.t("acc.offline");
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 350f);
        MenuBg.draw(width, height, (now - opened) / 1000f);
        Gfx.setScale(Math.max(1, new ScaledResolution(mc).getScaleFactor()));
        Gfx.fade = fade;

        // back chip
        boolean backOver = mx >= 14 && mx < 44 && my >= 14 && my < 34;
        Gfx.rr(14, 14, 30, 20, 8, backOver ? 0xE8242429 : 0xD0101014);
        Gfx.rrOutline(14, 14, 30, 20, 8, 1, 0x40FFFFFF);
        Gfx.textCentered("<", 29, 14 + (20 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);

        // skin on the left half, card on the right
        int half = width / 2;
        int feet = height / 2 + Math.min(90, height / 4);
        int size = Math.max(40, Math.min(height - 80, 220) * 10 / 19);
        int scx = half / 2 + 10;
        MenuBg.blob(scx, feet + 2, 50f, 8f, 0x1A2238, 75);
        MenuPlayer.draw(scx, feet, size, ((now - opened) % 9000L) / 9000f * 360f);

        pw = Math.min(210, half - 30); ph = 150;
        px = half + (half - pw) / 2 - 10; py = (height - ph) / 2;
        Gfx.rr(px, py + 2, pw, ph, 12, 0x26101830);
        Gfx.rr(px, py, pw, ph, 12, 0xD0101014);
        Gfx.rrOutline(px, py, pw, ph, 12, 1, 0x40FFFFFF);
        Gfx.text(Gfx.trim(name(), 14, true, pw - 24), px + 12, py + 12, 14, true, Theme.TEXT);
        int bw2 = Gfx.width(kind(), 7, true) + 10;
        Gfx.rr(px + 12, py + 32, bw2, 12, 6, Ui.alpha(Theme.accent(), 0.35f));
        Gfx.text(kind(), px + 17, py + 34, 7, true, Theme.TEXT);
        Gfx.text("UUID", px + 12, py + 52, 7, true, Theme.DIM);
        Gfx.text(Gfx.trim(dashed(), 7, false, pw - 24), px + 12, py + 62, 7, false, Theme.MUTED);

        bx = px + 12; bw = (pw - 28) / 2; by0 = py + 82;
        String[] labels = {Lang.t("acc.copy_name"), Lang.t("acc.copy_uuid"), "NameMC", Lang.t("acc.cosmetics")};
        for (int i = 0; i < 4; i++) {
            int x = bx + (i % 2) * (bw + 4), y = by0 + (i / 2) * 24;
            boolean over = mx >= x && mx < x + bw && my >= y && my < y + 20;
            hov[i] = Ui.approach(hov[i], over ? 1f : 0f, dt, 16f);
            LoadingArt.button(x, y, bw, 20, Gfx.trim(labels[i], 9, true, bw - 8), false, over, true);
        }
        boolean fo = mx >= px + 12 && mx < px + pw - 12 && my >= py + ph - 26 && my < py + ph - 8;
        LoadingArt.button(px + 12, py + ph - 26, pw - 24, 18, Lang.t("acc.friends"), true, fo, true);
        if (!note.isEmpty() && now - noteAt < 1600) Gfx.textCentered(note, px + pw / 2, py + ph + 8, 8, true, MenuBg.INK2);
        Gfx.fade = 1f;
    }

    private void say(String s) { note = s; noteAt = System.currentTimeMillis(); }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        if (mx >= 14 && mx < 44 && my >= 14 && my < 34) { mc.displayGuiScreen(parent); return; }
        for (int i = 0; i < 4; i++) {
            int x = bx + (i % 2) * (bw + 4), y = by0 + (i / 2) * 24;
            if (mx >= x && mx < x + bw && my >= y && my < y + 20) {
                if (i == 0) { setClipboardString(name()); say(Lang.t("acc.copied")); }
                else if (i == 1) { setClipboardString(dashed()); say(Lang.t("acc.copied")); }
                else if (i == 2) { try { org.lwjgl.Sys.openURL("https://namemc.com/profile/" + name()); } catch (Throwable ignored) { } }
                else mc.displayGuiScreen(new MenuScreen(4));
                return;
            }
        }
        if (mx >= px + 12 && mx < px + pw - 12 && my >= py + ph - 26 && my < py + ph - 8) mc.displayGuiScreen(new FriendsScreen(this));
    }

    @Override
    protected void keyTyped(char c, int key) { if (key == 1) mc.displayGuiScreen(parent); }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}

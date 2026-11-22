package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Built-in hotbar protection (not a module, always on).
 *
 * Slow switch: the hotbar moves at most one slot per 2 ticks, so a fast flick of the mouse wheel cannot jump over the
 * item you wanted.
 * Item switch prevention: while you are using an item (eating, drawing a bow, blocking with a sword) the hotbar does
 * not change, so a stray scroll cannot make you drop the sword or cancel the bow.
 *
 * It runs at the end of every client tick, before the game tells the server about a new slot, so the server never
 * sees the blocked switch.
 */
public final class HotbarGuard {
    private static int prev = -1;
    private static int tick;
    private static int lastChange = -100;

    private HotbarGuard() {}

    /** When a key or mouse button bound to a hotbar slot last went down (a thread watches them, so a quick tap is never missed). */
    private static volatile long lastKeyNs = -1;
    /** Slot picked with a key or mouse button bound to a hotbar slot, applied on the next tick even if the game missed it. */
    private static volatile int pending = -1;
    private static Thread watcher;

    private static void startWatcher(final Minecraft mc) {
        if (watcher != null) return;
        watcher = new Thread(new Runnable() {
            public void run() {
                boolean[] down = new boolean[9];
                while (true) {
                    try {
                        Thread.sleep(4);
                        if (mc.gameSettings == null) continue;
                        for (int i = 0; i < 9; i++) {
                            int code = mc.gameSettings.keyBindsHotbar[i].getKeyCode();
                            boolean d = code < 0 ? org.lwjgl.input.Mouse.isButtonDown(code + 100)
                                    : code > 0 && code < 256 && org.lwjgl.input.Keyboard.isKeyDown(code);
                            if (d && !down[i]) { lastKeyNs = System.nanoTime(); pending = i; }   // any key or button bound to a slot
                            down[i] = d;
                        }
                    } catch (InterruptedException e) { return; } catch (Throwable ignored) { }
                }
            }
        }, "YA-HotbarKeys");
        watcher.setDaemon(true);
        watcher.start();
    }

    /** A switch the client itself made on purpose (mouse side buttons): never revert it. */
    public static void accept(int slot) { prev = slot; lastChange = tick; }

    // ---- inventory: the key / mouse button bound to a hotbar slot swaps the hovered item into that slot ----
    private static ItemStack[] snap;
    private static int armed = -1, armedTick;
    private static boolean seenChange;
    private static java.lang.reflect.Method mSlotAt, mClick;

    private static ItemStack[] refs(Container c) {
        ItemStack[] a = new ItemStack[c.inventorySlots.size()];
        for (int i = 0; i < a.length; i++) a[i] = c.inventorySlots.get(i).getStack();
        return a;
    }

    /** Start of a tick: remember the inventory, to know afterwards whether the game already did the swap itself. */
    public static void pre(Minecraft mc) {
        try {
            EntityPlayer p = mc == null ? null : mc.thePlayer;
            snap = p != null && p.openContainer != null && mc.currentScreen instanceof GuiContainer ? refs(p.openContainer) : null;
        } catch (Throwable t) { snap = null; }
    }

    private static void inventory(Minecraft mc, EntityPlayer p) {
        int want = pending;
        pending = -1;
        try {
            Container c = p.openContainer;
            if (c == null) return;
            ItemStack[] now = refs(c);
            boolean changed = false;
            if (snap != null && snap.length == now.length) for (int i = 0; i < now.length; i++) if (snap[i] != now[i]) { changed = true; break; }
            snap = null;
            if (changed) seenChange = true;
            if (want >= 0) {
                int code = mc.gameSettings.keyBindsHotbar[want].getKeyCode();
                if (code <= -98) { swap(mc, p, want); armed = -1; return; }            // mouse side buttons: the game ignores them in inventories
                if (code > 0) { armed = want; armedTick = tick; seenChange = changed; }  // keyboard (CTRL, ALT, ...): wait one tick to see if the game did it
            }
            if (armed >= 0 && tick > armedTick) {
                int a = armed; armed = -1;
                if (!seenChange) swap(mc, p, a);
            }
        } catch (Throwable t) { armed = -1; }
    }

    private static void swap(Minecraft mc, EntityPlayer p, int hot) throws Exception {
        if (mc.currentScreen == null || !(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiContainerCreative) return;
        if (p.inventory.getItemStack() != null) return;
        if (mSlotAt == null || mClick == null) {
            for (java.lang.reflect.Method m : GuiContainer.class.getDeclaredMethods()) {
                Class<?>[] a = m.getParameterTypes();
                if (a.length == 2 && a[0] == int.class && a[1] == int.class && m.getReturnType() == Slot.class) { m.setAccessible(true); mSlotAt = m; }
                if (a.length == 4 && a[0] == Slot.class && a[1] == int.class && a[2] == int.class && a[3] == int.class && m.getReturnType() == void.class) { m.setAccessible(true); mClick = m; }
            }
            if (mSlotAt == null || mClick == null) return;
        }
        GuiContainer g = (GuiContainer) mc.currentScreen;
        int mx = org.lwjgl.input.Mouse.getX() * g.width / mc.displayWidth;
        int my = g.height - org.lwjgl.input.Mouse.getY() * g.height / mc.displayHeight - 1;
        Slot s = (Slot) mSlotAt.invoke(g, mx, my);
        if (s == null) return;
        mClick.invoke(g, s, s.slotNumber, hot, 2);   // click type 2 = swap with hotbar slot
    }

    public static void tick(Minecraft mc) {
        tick++;
        if (mc != null && mc.thePlayer != null && mc.currentScreen instanceof GuiContainer) { startWatcher(mc); inventory(mc, mc.thePlayer); } else armed = -1;
        if (mc != null) startWatcher(mc);
        EntityPlayer p = mc == null ? null : mc.thePlayer;
        if (p == null || p.isDead || !Config.getBool("hotbar.guard", true)) { prev = -1; return; }
        int cur = p.inventory.currentItem;
        int want = pending;
        pending = -1;
        if (want >= 0 && mc.currentScreen == null) {   // a slot bound to ANY key or mouse button: always switch, also while attacking, eating or blocking
            p.inventory.currentItem = want;
            prev = want; lastChange = tick;
            return;
        }
        if (prev < 0 || mc.currentScreen != null) { prev = cur; return; }
        if (cur != prev) {
            // a slot key or mouse button (side buttons too) was pressed just now: that is on purpose, whatever you are doing
            // (attacking, blocking, eating) and whichever slot it picks, so it always goes through
            if (lastKeyNs > 0 && System.nanoTime() - lastKeyNs < 600000000L) { prev = cur; lastChange = tick; return; }
            boolean busy = p.isUsingItem();
            boolean tooFast = tick - lastChange < 2;
            if (busy || tooFast) {
                p.inventory.currentItem = prev;   // refuse the switch
                return;
            }
            // one slot at a time for the wheel; number keys jump on purpose
            boolean key = false;
            try { for (int i = 0; i < 9; i++) if (org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindsHotbar[i].getKeyCode())) key = true; } catch (Throwable ignored) { }
            int d = ((cur - prev) % 9 + 9) % 9;      // 0..8 steps forward
            int step = d <= 4 ? 1 : -1;
            if (!key && d != 1 && d != 8) {
                if (d <= 3 || d >= 6) { cur = ((prev + step) % 9 + 9) % 9; p.inventory.currentItem = cur; }
            }
            lastChange = tick;
        }
        prev = cur;
    }
}

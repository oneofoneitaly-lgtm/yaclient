package dev.yaclient.fabric;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** Combat statistics shared by the combo, reach and totem pop modules. */
public final class Hits {
    public static int combo;
    public static double reach;
    private static int pops, lastTotems = -1, lastHurt;

    private Hits() {}

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide()) {
                combo++; reach = player.distanceTo(entity);
                Module hs = Modules.byId("plushit");
                if (hs != null && hs.active()) dev.yaclient.fabric.mods.Plus.HitSparks.spark(entity);
            }
            return InteractionResult.PASS;
        });
    }

    public static int pops(Minecraft mc) { return pops; }

    private static final java.util.ArrayDeque<Long> LEFT = new java.util.ArrayDeque<Long>(), RIGHT = new java.util.ArrayDeque<Long>();
    private static boolean wasL, wasR;

    private static int count(java.util.ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        return q.size();
    }

    /** "left | right CPS", sampled every time the HUD asks for it. */
    public static String cpsText(Minecraft mc, boolean label) {
        long now = System.currentTimeMillis();
        boolean l = mc.options.keyAttack.isDown(), r = mc.options.keyUse.isDown();
        if (l && !wasL) LEFT.addLast(now);
        if (r && !wasR) RIGHT.addLast(now);
        wasL = l;
        wasR = r;
        return count(LEFT, now) + " | " + count(RIGHT, now) + (label ? " CPS" : "");
    }

    public static void tick(Minecraft mc) {
        Player p = mc.player;
        if (p == null) { lastTotems = -1; return; }
        if (p.hurtTime > 0 && lastHurt == 0) combo = 0;
        lastHurt = p.hurtTime;
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(Items.TOTEM_OF_UNDYING)) n += s.getCount();
        }
        if (lastTotems >= 0 && n < lastTotems && p.hurtTime > 0) pops += lastTotems - n;
        lastTotems = n;
    }
}

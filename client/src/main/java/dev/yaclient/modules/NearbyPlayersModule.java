package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** The closest players around you with their distance. */
public class NearbyPlayersModule extends HudModule {
    private final Setting.Num count = add(new Setting.Num("count", 1f, 8f, 1f, 5f, ""));
    private final Setting.Num range = add(new Setting.Num("range", 16f, 128f, 16f, 64f, ""));

    public NearbyPlayersModule() { super("nearby_players", Category.INFO, false, 0.008f, 0.30f); }

    private List<String> lines() {
        List<String> out = new ArrayList<String>();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return out;
        List<EntityPlayer> ps = new ArrayList<EntityPlayer>();
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p != mc.thePlayer && !p.isInvisible() && mc.thePlayer.getDistanceToEntity(p) <= range.value) ps.add(p);
        }
        Collections.sort(ps, new Comparator<EntityPlayer>() {
            @Override public int compare(EntityPlayer a, EntityPlayer b) {
                return Float.compare(mc.thePlayer.getDistanceToEntity(a), mc.thePlayer.getDistanceToEntity(b));
            }
        });
        for (int i = 0; i < ps.size() && i < Math.round(count.value); i++) {
            out.add(ps.get(i).getName() + "  " + Math.round(mc.thePlayer.getDistanceToEntity(ps.get(i))) + "m");
        }
        if (out.isEmpty() && preview) { out.add("Player1  8m"); out.add("Player2  21m"); }
        return out;
    }

    @Override
    public int width() {
        List<String> l = lines();
        if (l.isEmpty()) return 0;
        int w = 60;
        for (String s : l) w = Math.max(w, Gfx.width(s, 10, false) + 16);
        return w;
    }

    @Override
    public int height() { return Math.max(16, lines().size() * 14 + 6); }

    @Override
    protected void draw() {
        List<String> l = lines();
        if (l.isEmpty()) return;
        Gfx.rr(0, 0, width(), height(), 7, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, width(), height(), 7, 1, 0x1AFFFFFF);
        for (int i = 0; i < l.size(); i++) Gfx.text(l.get(i), 8, 5 + i * 14, 10, false, Theme.TEXT);
    }
}

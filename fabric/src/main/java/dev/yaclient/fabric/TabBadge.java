package dev.yaclient.fabric;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.PlayerTeam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Puts the YA logo (and the gold + for Plus) in front of YA CLIENT players in the tab list, using a bitmap font glyph. */
public final class TabBadge {
    private TabBadge() {}

    private static final Style LOGO = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("yaclient", "badge"))).withColor(0xFFFFFF);
    private static final Map<UUID, Component> APPLIED = new HashMap<UUID, Component>();
    private static int tick;

    public static void tick(Minecraft mc) {
        if (mc.getConnection() == null || ++tick % 10 != 0) return;
        boolean on = Config.getBool("tab.badge", true);
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            UUID id = info.getProfile().id();
            Component cur = info.getTabListDisplayName();
            Component mine = APPLIED.get(id);
            boolean want = on && CosmeticsApi.usesClient(id);
            if (want) {
                if (mine != null && cur == mine) continue;
                Component base = cur != null && cur != mine ? cur : PlayerTeam.formatNameForTeam(info.getTeam(), Component.literal(info.getProfile().name()));
                MutableComponent out = Badges.decorate(id, base);
                APPLIED.put(id, out);
                info.setTabListDisplayName(out);
            } else if (mine != null) {
                if (cur == mine) info.setTabListDisplayName(null);
                APPLIED.remove(id);
            }
        }
    }
}

package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

/** Logo / Plus badges drawn with a bitmap font glyph (1.21.1: entity based, no render state). */
public final class Badges {
    private Badges() {}

    /** Player whose name tag is being rendered right now (set by the mixin). */
    public static AbstractClientPlayer current;

    public static final Style LOGO = Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath("yaclient", "badge")).withColor(0xFFFFFF);

    public static MutableComponent decorate(java.util.UUID id, Component base) {
        MutableComponent out = Component.literal("");
        out.append(Component.literal("").withStyle(LOGO));
        out.append(Component.literal(" "));
        out.append(base);
        if (CosmeticsApi.isPlus(id)) { out.append(Component.literal(" ")); out.append(Component.literal("").withStyle(LOGO)); }
        return out;
    }

    public static Component nametag(AbstractClientPlayer p, Component base) {
        if (!Config.getBool("nametag.badge", true) || base == null) return base;
        if (!CosmeticsApi.usesClient(p.getUUID())) return base;
        Minecraft mc = Minecraft.getInstance();
        if (p == mc.player && mc.options.getCameraType().isFirstPerson()) return base;
        return decorate(p.getUUID(), base);
    }

    public static boolean hasYaCape(Player p) {
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return false;
        String cape = eq.get("cape");
        return cape != null && !cape.isEmpty() && !"none".equals(cape);
    }
}

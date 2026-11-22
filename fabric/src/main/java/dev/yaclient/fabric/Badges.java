package dev.yaclient.fabric;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/** Logo / Plus badges drawn with a bitmap font glyph, so Minecraft itself handles the billboard, distance and depth. */
public final class Badges {
    private Badges() {}

    public static final Style LOGO = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("yaclient", "badge"))).withColor(0xFFFFFF);

    /** Logo glyph, then the name, then the gold plus glyph for Plus members. */
    public static MutableComponent decorate(java.util.UUID id, Component base) {
        MutableComponent out = Component.literal("");
        int rank = CosmeticsApi.rank(id);   // 0 normal, 1 YA Plus, 2 staff (set from the admin panel)
        out.append(Component.literal(rank == 2 ? "\uE003" : rank == 1 ? "\uE002" : "\uE000").withStyle(LOGO));
        out.append(Component.literal(" "));
        out.append(base);
        return out;
    }

    public static void applyNametag(Avatar avatar, AvatarRenderState state) {
        if (!Config.getBool("nametag.badge", true) || !(avatar instanceof Player p)) return;
        if (!CosmeticsApi.usesClient(p.getUUID())) return;
        Minecraft mc = Minecraft.getInstance();
        boolean self = p == mc.player;
        if (self && mc.options.getCameraType().isFirstPerson()) return;
        Component base = state.nameTag != null ? state.nameTag : p.getDisplayName();
        if (base == null) return;
        state.nameTag = decorate(p.getUUID(), base);
        if (state.nameTagAttachment == null) state.nameTagAttachment = new Vec3(0.0, state.boundingBoxHeight + 0.5, 0.0);
    }

    public static boolean hasYaCape(AvatarRenderState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        Entity e = mc.level.getEntity(state.id);
        if (!(e instanceof Player p)) return false;
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return false;
        String cape = eq.get("cape");
        return cape != null && !cape.isEmpty() && !"none".equals(cape);
    }
}

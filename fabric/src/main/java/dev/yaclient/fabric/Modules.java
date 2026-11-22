package dev.yaclient.fabric;

import dev.yaclient.fabric.mods.*;
import dev.yaclient.fabric.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** All modules of the Fabric client. New installs start with FPS, CPS and Keystrokes on, everything else off. */
public final class Modules {
    public static final List<Module> ALL = new ArrayList<Module>();
    private static final long START = System.currentTimeMillis();

    private Modules() {}

    private static TextHud text(String id, String name, String desc, Category c, float x, float y, java.util.function.Function<Minecraft, String> f) {
        TextHud t = new TextHud(id, name, desc, c, false, x, y, f);
        ALL.add(t);
        return t;
    }

    private static TextHud textOn(String id, String name, String desc, Category c, float x, float y, java.util.function.Function<Minecraft, String> f) {
        TextHud t = new TextHud(id, name, desc, c, true, x, y, f);
        ALL.add(t);
        return t;
    }

    public static void init() {
        final Setting.Bool[] fl = new Setting.Bool[1], fc = new Setting.Bool[1];
        final Setting.Num[] fw = new Setting.Num[1];
        TextHud fps = textOn("fps", "FPS", "Shows your frames per second", Category.INFO, 0.008f, 0.012f, mc -> mc.getFps() + (fl[0].value ? " FPS" : ""));
        fl[0] = fps.add("general", new Setting.Bool("label", true));
        fc[0] = fps.add("general", new Setting.Bool("colorize", false));
        fw[0] = fps.add("general", new Setting.Num("warn", 20f, 144f, 5f, 60f, ""));
        fps.autoColor = mc -> {
            if (!fc[0].value) return 0;
            int f = mc.getFps();
            return f >= fw[0].value ? 0xFF22C55E : (f >= fw[0].value / 2f ? 0xFFFFC21A : 0xFFEF4444);
        };
        final Setting.Bool[] cl = new Setting.Bool[1];
        TextHud cps = textOn("cps", "CPS", "Shows your clicks per second", Category.PVP, 0.008f, 0.064f, mc -> Hits.cpsText(mc, cl[0].value));
        cl[0] = cps.add("general", new Setting.Bool("label", true));
        ALL.add(new KeystrokesModule());
        ALL.add(new DiscordRpcModule());
        text("coords", "Coordinates", "Shows your XYZ position", Category.INFO, 0.008f, 0.116f, mc -> mc.player == null ? "" :
            "XYZ " + (int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY()) + " " + (int) Math.floor(mc.player.getZ()));
        text("ping", "Ping", "Your latency to the server", Category.INFO, 0.008f, 0.17f, mc -> {
            if (mc.player == null || mc.getConnection() == null) return "Ping -";
            PlayerInfo pi = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            return "Ping " + (pi == null ? 0 : pi.getLatency()) + " ms";
        });
        text("speed", "Speed", "Your speed in blocks per second", Category.INFO, 0.008f, 0.22f, mc -> {
            if (mc.player == null) return "";
            double dx = mc.player.getX() - mc.player.xo, dz = mc.player.getZ() - mc.player.zo;
            return String.format("%.2f b/s", Math.sqrt(dx * dx + dz * dz) * 20);
        });
        text("direction", "Direction", "Compass direction you face", Category.INFO, 0.008f, 0.27f, mc -> {
            if (mc.player == null) return "";
            float yaw = ((mc.player.getYRot(1f) % 360f) + 360f) % 360f;
            String[] d = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
            return d[Math.round(yaw / 45f) % 8] + " (" + Math.round(yaw) + ")";
        });
        final Setting.Bool[] h24 = new Setting.Bool[1];
        TextHud clk = text("clock", "Clock", "Real time", Category.INFO, 0.9f, 0.012f, mc -> h24[0].value ? String.format("%tR", Calendar.getInstance()) : String.format("%tl:%tM %tp", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
        h24[0] = clk.add("general", new Setting.Bool("h24", true));
        text("session", "Session Time", "How long you have been playing", Category.INFO, 0.9f, 0.06f, mc -> {
            long s = (System.currentTimeMillis() - START) / 1000;
            return String.format("%d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
        });
        text("memory", "Memory", "Java memory in use", Category.INFO, 0.9f, 0.11f, mc -> {
            Runtime r = Runtime.getRuntime();
            return ((r.totalMemory() - r.freeMemory()) >> 20) + " / " + (r.maxMemory() >> 20) + " MB";
        });
        text("playercount", "Player Count", "Players online on the server", Category.INFO, 0.9f, 0.16f, mc ->
            mc.getConnection() == null ? "" : mc.getConnection().getOnlinePlayers().size() + " online");
        text("server", "Server Address", "The server you are on", Category.INFO, 0.9f, 0.21f, mc ->
            mc.getCurrentServer() == null ? "Singleplayer" : mc.getCurrentServer().ip);
        text("entities", "Entity Count", "Entities around you", Category.INFO, 0.9f, 0.26f, mc -> {
            if (mc.level == null) return "";
            int n = 0;
            for (Entity e : mc.level.entitiesForRendering()) n++;
            return n + " entities";
        });
        text("falldistance", "Fall Distance", "Blocks you have fallen", Category.INFO, 0.9f, 0.31f, mc ->
            mc.player == null ? "" : String.format("Fall %.1f", mc.player.fallDistance));
        text("health", "Health", "Your health in hearts", Category.PVP, 0.008f, 0.8f, mc -> {
            if (mc.player == null) return "";
            float a = mc.player.getAbsorptionAmount();
            return String.format("%.1f HP", mc.player.getHealth()) + (a > 0 ? String.format(" +%.0f", a) : "");
        });
        text("food", "Food", "Hunger and saturation", Category.INFO, 0.008f, 0.85f, mc ->
            mc.player == null ? "" : "Food " + mc.player.getFoodData().getFoodLevel() + " (" + String.format("%.1f", mc.player.getFoodData().getSaturationLevel()) + ")");
        text("xp", "XP Level", "Your experience level", Category.INFO, 0.008f, 0.9f, mc ->
            mc.player == null ? "" : "Level " + mc.player.experienceLevel);
        text("potions", "Potion Effects", "Active effects and time left", Category.PVP, 0.9f, 0.4f, mc -> {
            if (mc.player == null) return "";
            StringBuilder b = new StringBuilder();
            for (MobEffectInstance e : mc.player.getActiveEffects()) {
                int s = e.getDuration() / 20;
                b.append(e.getEffect().value().getDisplayName().getString()).append(' ').append(e.getAmplifier() + 1)
                 .append(e.isInfiniteDuration() ? " **" : String.format(" %d:%02d", s / 60, s % 60)).append('\n');
            }
            return b.length() == 0 ? "No effects" : b.substring(0, b.length() - 1);
        });
        text("blockinfo", "Block Info", "Name of the block you look at", Category.INFO, 0.42f, 0.012f, mc -> {
            if (mc.level == null || !(mc.hitResult instanceof BlockHitResult bh) || bh.getType() != HitResult.Type.BLOCK) return "";
            return mc.level.getBlockState(bh.getBlockPos()).getBlock().getName().getString();
        });
        text("iteminfo", "Item Info", "Held item and durability", Category.INFO, 0.42f, 0.9f, mc -> {
            if (mc.player == null) return "";
            var st = mc.player.getMainHandItem();
            if (st.isEmpty()) return "";
            String s = st.getHoverName().getString();
            if (st.isDamageableItem()) s += " " + (st.getMaxDamage() - st.getDamageValue()) + "/" + st.getMaxDamage();
            return s;
        });
        text("bowcharge", "Bow Charge", "How charged your bow is", Category.PVP, 0.42f, 0.55f, mc -> {
            if (mc.player == null || !mc.player.isUsingItem()) return "";
            return "Charge " + Math.min(100, mc.player.getTicksUsingItem() * 5) + "%";
        });
        text("nearby", "Nearby Players", "Players within 64 blocks", Category.PVP, 0.9f, 0.6f, mc -> {
            if (mc.level == null || mc.player == null) return "";
            StringBuilder b = new StringBuilder();
            int n = 0;
            for (Player p : mc.level.players()) {
                if (p == mc.player || n >= 8) continue;
                float d = mc.player.distanceTo(p);
                if (d > 64) continue;
                b.append(p.getName().getString()).append("  ").append(Math.round(d)).append("m\n");
                n++;
            }
            return n == 0 ? "No players nearby" : b.substring(0, b.length() - 1);
        });
        text("combo", "Combo", "Consecutive hits without being hit", Category.PVP, 0.42f, 0.7f, mc -> "Combo " + Hits.combo);
        text("reach", "Reach", "Distance of your last hit", Category.PVP, 0.42f, 0.75f, mc -> String.format("Reach %.2f", Hits.reach));
        text("totempops", "Totem Pops", "Totems you have popped this session (counted from your inventory)", Category.CRYSTAL, 0.008f, 0.45f, mc -> "Pops " + Hits.pops(mc));
        // ---- SMP / BoxPvP / SkyBlock helpers ----
        text("netherxyz", "Nether Coords", "[SMP] Matching Nether / Overworld coordinates for portals", Category.INFO, 0.008f, 0.32f, Extra::netherCoords);
        text("deathpos", "Death Location", "[SMP] Where you died last, so you can go back for your items", Category.INFO, 0.008f, 0.37f, mc -> Extra.deathText());
        text("daycount", "Day Counter", "[SMP] World day and in-game time", Category.INFO, 0.9f, 0.36f, Extra::day);
        text("lightlevel", "Light Level", "[SMP / SkyBlock] Light at your feet, below 8 mobs can spawn", Category.INFO, 0.008f, 0.42f, Extra::light);
        text("freeslots", "Free Slots", "[SkyBlock / SMP] Empty inventory slots left", Category.INFO, 0.9f, 0.46f, mc -> mc.player == null ? "" : Extra.freeSlots(mc) + " free slots");
        text("potcount", "Potion Count", "[BoxPvP] Splash potions in your inventory", Category.PVP, 0.008f, 0.55f, mc -> mc.player == null ? "" : "Pots " + Extra.count(mc, Items.SPLASH_POTION));
        text("gapcount", "Gapple Count", "[BoxPvP] Golden apples in your inventory", Category.PVP, 0.008f, 0.6f, mc -> mc.player == null ? "" : "Gapples " + Extra.count(mc, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        text("pearlcount", "Pearl Count", "[BoxPvP] Ender pearls in your inventory", Category.PVP, 0.008f, 0.65f, mc -> mc.player == null ? "" : "Pearls " + Extra.count(mc, Extra.PEARL));
        text("xpbottles", "XP Bottles", "[SkyBlock / SMP] Bottles o' enchanting in your inventory", Category.INFO, 0.9f, 0.51f, mc -> mc.player == null ? "" : "XP bottles " + Extra.count(mc, Items.EXPERIENCE_BOTTLE));
        text("handcount", "Held Item Count", "[SkyBlock] How many of the item in your hand you carry in total", Category.INFO, 0.42f, 0.84f, mc -> {
            if (mc.player == null) return "";
            var st = mc.player.getMainHandItem();
            if (st.isEmpty()) return "";
            return st.getHoverName().getString() + " x" + Extra.count(mc, st.getItem());
        });
        text("armorwarn", "Armor Warning", "[BoxPvP] Warns when a piece of armor is almost broken", Category.PVP, 0.42f, 0.3f, mc -> {
            if (mc.player == null) return "";
            StringBuilder b = new StringBuilder();
            for (var st : new net.minecraft.world.item.ItemStack[] {mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST), mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS), mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)}) {
                if (st.isEmpty() || !st.isDamageableItem()) continue;
                int left = st.getMaxDamage() - st.getDamageValue();
                if (left * 10 < st.getMaxDamage()) b.append(st.getHoverName().getString()).append(" low (").append(left).append(")\n");
            }
            return b.length() == 0 ? "" : "! " + b.substring(0, b.length() - 1);
        });
        text("chunkpos", "Chunk Position", "Chunk you are in and your place inside it", Category.INFO, 0.008f, 0.47f, mc -> mc.player == null ? "" :
            "Chunk " + ((int) Math.floor(mc.player.getX()) >> 4) + " " + ((int) Math.floor(mc.player.getZ()) >> 4) + "  (" + ((int) Math.floor(mc.player.getX()) & 15) + ", " + ((int) Math.floor(mc.player.getZ()) & 15) + ")");
        text("targetdist", "Target Distance", "[SMP / PvP] Distance to the block or entity you look at", Category.INFO, 0.42f, 0.06f, Extra::targetDistance);
        text("arrowcount", "Arrow Count", "[BoxPvP] Arrows in your inventory", Category.PVP, 0.008f, 0.7f, mc -> mc.player == null ? "" : "Arrows " + Extra.count(mc, Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW));
        text("totemwarn", "Totem Warning", "[Crystal / BoxPvP] Warns when you hold no totem in your off hand", Category.CRYSTAL, 0.42f, 0.2f, mc ->
            mc.player == null || mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING) ? "" : "NO TOTEM IN OFF HAND");
        text("mobcount", "Mob Counter", "[SkyBlock] Hostile mobs within 32 blocks, handy for mob farms", Category.INFO, 0.9f, 0.56f, Extra::mobCount);
        text("dropcount", "Dropped Items", "[SkyBlock] Item entities lying around you, for farms and drops", Category.INFO, 0.9f, 0.61f, Extra::dropCount);
        text("sprintstate", "Sprint State", "Shows when you are sprinting or sneaking", Category.INFO, 0.008f, 0.75f, mc ->
            mc.player == null ? "" : (mc.player.isSprinting() ? "Sprinting" : mc.player.isShiftKeyDown() ? "Sneaking" : "Walking"));
        // ---- Crystal PvP ----
        text("crystalsnear", "Crystals Nearby", "[Crystal] End crystals within 12 blocks", Category.CRYSTAL, 0.75f, 0.05f, mc -> Extra.crystals(mc, false));
        text("crystaldist", "Crystal Distance", "[Crystal] Distance to the nearest end crystal", Category.CRYSTAL, 0.75f, 0.10f, mc -> Extra.crystals(mc, true));
        text("obsidiancount", "Obsidian Count", "[Crystal] Obsidian in your inventory", Category.CRYSTAL, 0.75f, 0.15f, mc -> mc.player == null ? "" : "Obsidian " + Extra.count(mc, Items.OBSIDIAN));
        text("anchorcount", "Anchor Count", "[Crystal] Respawn anchors and glowstone", Category.CRYSTAL, 0.75f, 0.20f, mc -> mc.player == null ? "" : "Anchors " + Extra.count(mc, Items.RESPAWN_ANCHOR) + "  Glow " + Extra.count(mc, Items.GLOWSTONE));
        text("totemcount", "Totem Count", "[Crystal] Totems of undying in your inventory", Category.CRYSTAL, 0.75f, 0.25f, mc -> mc.player == null ? "" : "Totems " + Extra.count(mc, Items.TOTEM_OF_UNDYING));
        text("offhandinfo", "Offhand Item", "[Crystal] What you hold in your off hand", Category.CRYSTAL, 0.75f, 0.30f, mc -> {
            if (mc.player == null) return "";
            var st = mc.player.getOffhandItem();
            return st.isEmpty() ? "Offhand empty" : st.getHoverName().getString() + " x" + st.getCount();
        });
        text("hitcooldown", "Hit Cooldown", "[PvP] How charged your next attack is", Category.PVP, 0.75f, 0.35f, mc -> mc.player == null ? "" : "Attack " + Math.round(mc.player.getAttackStrengthScale(0f) * 100f) + "%");
        text("armorvalue", "Armor Value", "[PvP] Your total armor points", Category.PVP, 0.75f, 0.40f, mc -> mc.player == null ? "" : "Armor " + mc.player.getArmorValue());
        text("lastdamage", "Last Damage", "[PvP] Damage of the last hit you took", Category.PVP, 0.75f, 0.45f, mc -> Extra.lastDamage());
        text("shieldstate", "Shield State", "[PvP] Shows when you are blocking", Category.PVP, 0.75f, 0.50f, mc -> mc.player != null && mc.player.isBlocking() ? "Blocking" : "");
        text("burning", "Burning Alert", "[PvP / SMP] Warns while you are on fire", Category.PVP, 0.42f, 0.25f, mc -> mc.player != null && mc.player.isOnFire() ? "BURNING!" : "");
        // ---- BedWars / SkyWars ----
        text("blocksleft", "Blocks Left", "[BedWars / SkyWars] How many of the block in your hand you carry", Category.PVP, 0.75f, 0.55f, mc -> {
            int n = Extra.countAll(mc, true);
            return n < 0 ? "" : "Blocks " + n;
        });
        text("resources", "Resources", "[BedWars] Iron, gold, diamonds and emeralds you carry", Category.PVP, 0.75f, 0.60f, mc -> mc.player == null ? "" :
            "Iron " + Extra.count(mc, Items.IRON_INGOT) + "\nGold " + Extra.count(mc, Items.GOLD_INGOT) + "\nDiamond " + Extra.count(mc, Items.DIAMOND) + "\nEmerald " + Extra.count(mc, Items.EMERALD));
        text("bwitems", "BedWars Items", "[BedWars] Fireballs, TNT and water buckets", Category.PVP, 0.75f, 0.68f, mc -> mc.player == null ? "" :
            "Fireballs " + Extra.count(mc, Items.FIRE_CHARGE) + "\nTNT " + Extra.count(mc, Items.TNT) + "\nWater " + Extra.count(mc, Items.WATER_BUCKET));
        text("buckets", "Bucket Count", "[SkyBlock / SkyWars] Water and lava buckets", Category.INFO, 0.75f, 0.75f, mc -> mc.player == null ? "" : "Water " + Extra.count(mc, Items.WATER_BUCKET) + "  Lava " + Extra.count(mc, Items.LAVA_BUCKET));
        text("lookangles", "Look Angles", "[BedWars / Bridging] Your exact yaw and pitch", Category.INFO, 0.42f, 0.45f, mc -> mc.player == null ? "" :
            String.format("Yaw %.1f  Pitch %.1f", mc.player.getViewYRot(1f), mc.player.getViewXRot(1f)));
        text("pingwarn", "Lag Warning", "Warns when your ping is high", Category.INFO, 0.42f, 0.5f, mc -> {
            if (mc.player == null || mc.getConnection() == null) return "";
            PlayerInfo pi = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            return pi != null && pi.getLatency() > 150 ? "HIGH PING " + pi.getLatency() + " ms" : "";
        });
        // ---- Vanilla / SMP ----
        text("mountspeed", "Mount Speed", "[SMP] Speed of your horse, boat or minecart", Category.INFO, 0.75f, 0.80f, Extra::mountSpeed);
        text("elytrainfo", "Elytra Info", "[SMP] Elytra durability and flight speed", Category.INFO, 0.75f, 0.85f, Extra::elytra);
        text("fireworks", "Firework Count", "[SMP] Firework rockets in your inventory", Category.INFO, 0.75f, 0.90f, mc -> mc.player == null ? "" : "Rockets " + Extra.count(mc, Items.FIREWORK_ROCKET));
        text("breath", "Breath", "[SMP] Air left while underwater", Category.INFO, 0.42f, 0.55f, mc -> mc.player == null || mc.player.getAirSupply() >= mc.player.getMaxAirSupply() ? "" : "Air " + Math.max(0, mc.player.getAirSupply() / 20) + "s");
        text("xpprogress", "XP Progress", "[SMP] Percent to the next level", Category.INFO, 0.42f, 0.6f, mc -> mc.player == null ? "" : "XP " + Math.round(mc.player.experienceProgress * 100f) + "% to " + (mc.player.experienceLevel + 1));
        text("gamemodeinfo", "Game Mode", "Creative, Spectator or Survival", Category.INFO, 0.42f, 0.65f, mc -> mc.player == null ? "" : (mc.player.isCreative() ? "Creative" : mc.player.isSpectator() ? "Spectator" : "Survival"));
        text("walked", "Distance Walked", "[SMP] Blocks walked this session", Category.INFO, 0.42f, 0.7f, mc -> Extra.walkedText());
        text("hungerwarn", "Hunger Warning", "[SMP] Warns when your food is low", Category.INFO, 0.42f, 0.75f, mc -> mc.player != null && mc.player.getFoodData().getFoodLevel() <= 6 ? "HUNGRY (" + mc.player.getFoodData().getFoodLevel() + ")" : "");
        text("toolwarn", "Tool Warning", "[SMP / SkyBlock] Warns when your held tool is about to break", Category.INFO, 0.42f, 0.8f, Extra::toolWarn);
        ALL.add(new Tweaks.Opt<Boolean>("togglesneak", "Toggle Sneak", "[SMP / BedWars] Press sneak once to stay crouched", Category.UTILITY, o -> o.toggleCrouch(), Boolean.TRUE));
        ALL.add(new Tweaks.Opt<Boolean>("noautojump", "No Auto Jump", "Turns off auto jump", Category.UTILITY, o -> o.autoJump(), Boolean.FALSE));
        ALL.add(new Tweaks.Opt<Integer>("unlimitedfps", "Unlimited FPS", "Removes the FPS limit", Category.UTILITY, o -> o.framerateLimit(), Integer.valueOf(260)));
        ALL.add(new Tweaks.Opt<Boolean>("vsyncoff", "VSync Off", "Turns VSync off for lower input delay", Category.UTILITY, o -> o.enableVsync(), Boolean.FALSE));
        // ---- YA Plus only ----
        ALL.add(new Plus.Trail().plus());
        ALL.add(new Plus.HitSparks().plus());
        ALL.add(new Plus.RainbowHud().plus());
        text("fallalert", "Fall Alert", "Big warning when a fall would hurt", Category.INFO, 0.42f, 0.4f, mc ->
            mc.player != null && mc.player.fallDistance > 3.5f ? String.format("FALL %.0f blocks!", mc.player.fallDistance) : "").plus();
        text("plusstats", "Session Stats", "Hits, combo and best combo this session", Category.PVP, 0.9f, 0.7f, mc -> Extra.stats()).plus();
        ALL.add(new Utility.NoFovEffects());
        ALL.add(new Utility.NoScreenEffects());
        ALL.add(new Utility.NoBobbing());
        ALL.add(new CrystalCounterModule());
        ALL.add(new TargetHud());
        ALL.add(new ItemsHud());
        ALL.add(new InventoryHud());
        ALL.add(new Overlays.Crosshair());
        ALL.add(new Overlays.LowHealth());
        ALL.add(new Utility.Fullbright());
        ALL.add(new Utility.NoClouds());
        ALL.add(new Utility.FpsBoost());
        ALL.add(new Utility.ToggleSprint());
        ALL.add(new Utility.Zoom());
        ALL.add(new Utility.Fov());
        for (Module m : ALL) m.load();
    }

    public static Module byId(String id) {
        for (Module m : ALL) if (m.id.equals(id)) return m;
        return null;
    }

    private static final java.util.Map<String, Integer> FAILS = new java.util.HashMap<String, Integer>();

    /** A bug in one module must never break the game or the other modules: log once, switch it off after repeated errors. */
    public static void fail(Module m, Throwable t) {
        int n = FAILS.merge(m.id, 1, Integer::sum);
        if (n == 1) System.out.println("[YA CLIENT] module " + m.id + " error: " + t);
        if (n >= 200) { m.enabled = false; FAILS.put(m.id, 0); System.out.println("[YA CLIENT] module " + m.id + " disabled after repeated errors"); }
    }

    public static void tick() {
        for (Module m : ALL) if (m.active()) { try { m.onTick(); } catch (Throwable t) { Modules.fail(m, t); } }
        Hits.tick(Minecraft.getInstance());
        Extra.tick(Minecraft.getInstance());
    }
}

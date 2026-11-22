package dev.yaclient.fabric;

/**
 * Cosmetics catalog and the player's selection (same ids as the 1.8.9 client). Items are unlocked on the
 * website, the selection is synced to the store API (see CosmeticsApi).
 */
public final class Cosmetics {
    public static final String[] SLOTS = {"cape", "wings", "bandana", "pet"};
    public static final String[] BUNDLES = {"beta", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost", "clown", "luck"};
    public static final String[] PREFIX = {"ya", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost", "clown", "luck"};
    public static final String[] NAMES = {"Beta Tester", "Monochrome", "Qlux", "Sakura", "Shinobi", "Kitsune", "Santa", "Frost", "Clown", "Luck"};
    public static final boolean[] PARTNER = {false, false, true, false, false, false, false, false, false, false};

    private Cosmetics() {}

    /** Single items (sold one by one, outside the bundles): "<style>_<slot>". */
    public static final String[] EXTRAS = {
            "sky_cape", "ocean_cape", "sunset_cape", "forest_cape", "space_cape",
            "dragon_wings", "butterfly_wings", "azure_wings", "venom_wings", "dragonfly_wings",
            "ninja_bandana", "crown_bandana", "halo_bandana", "cat_bandana", "rainbow_bandana",
            "drake_pet", "bunny_pet", "penguin_pet", "panda_pet", "axolotl_pet",
            "vampire_wings", "vamp_crimson_wings", "vamp_scarlet_wings", "vamp_orange_wings", "vamp_amber_wings", "vamp_gold_wings",
            "vamp_lime_wings", "vamp_green_wings", "vamp_emerald_wings", "vamp_teal_wings", "vamp_cyan_wings", "vamp_sky_wings",
            "vamp_blue_wings", "vamp_indigo_wings", "vamp_violet_wings", "vamp_purple_wings", "vamp_magenta_wings",
            "vamp_pink_wings", "vamp_rose_wings", "vamp_white_wings", "vamp_silver_wings", "vampire_cape", "vamp_crimson_cape",
            "vamp_scarlet_cape", "vamp_orange_cape", "vamp_amber_cape", "vamp_gold_cape", "vamp_lime_cape", "vamp_green_cape",
            "vamp_emerald_cape", "vamp_teal_cape", "vamp_cyan_cape", "vamp_sky_cape", "vamp_blue_cape", "vamp_indigo_cape",
            "vamp_violet_cape", "vamp_purple_cape", "vamp_magenta_cape", "vamp_pink_cape", "vamp_rose_cape", "vamp_white_cape",
            "vamp_silver_cape", "storm_cape", "puppy_pet"};

    public static final String[] EXTRA_NAMES = {
            "Sky Cape", "Ocean Cape", "Sunset Cape", "Forest Cape", "Space Cape",
            "Dragon Wings", "Butterfly Wings", "Azure Wings", "Valkyrie Wings", "Dragonfly Wings",
            "Ninja Headband", "Golden Crown", "Angel Halo", "Cat Ears", "Rainbow Band",
            "Drake (pet)", "Bunny (pet)", "Penguin (pet)", "Panda (pet)", "Axolotl (pet)"};

    public static boolean isExtra(String item) { for (String e : EXTRAS) if (e.equals(item)) return true; return false; }

    /** Equips (or removes when it is already worn) a single item. */
    public static void toggleItem(String slot, String item) { equip(slot, selected(slot).equals(item) ? "none" : item); }

    public static String item(int bundle, String slot) { return PREFIX[bundle] + "_" + slot; }

    public static boolean ownsBundle(int bundle) {
        for (String s : SLOTS) if (!CosmeticsApi.owned(item(bundle, s))) return false;
        return true;
    }

    public static String selected(String slot) { return Config.get("cos." + slot, "none"); }

    /** What is actually worn: the selection, but only if it is owned. */
    public static String equipped(String slot) {
        String s = selected(slot);
        return (s.equals("none") || !CosmeticsApi.owned(s)) ? "none" : s;
    }

    public static void equip(String slot, String item) {
        Config.set("cos." + slot, item);
        Config.save();
        CosmeticsApi.pushEquip(slot, item);
    }

    public static void toggle(int bundle, String slot) {
        String item = item(bundle, slot);
        equip(slot, selected(slot).equals(item) ? "none" : item);
    }

    public static boolean wearsBundle(int bundle) {
        for (String s : SLOTS) if (!selected(s).equals(item(bundle, s))) return false;
        return true;
    }

    public static void toggleBundle(int bundle) {
        boolean all = wearsBundle(bundle);
        java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
        for (String s : SLOTS) { String v = all ? "none" : item(bundle, s); Config.set("cos." + s, v); m.put(s, v); }
        Config.save();
        CosmeticsApi.pushEquipAll(m);
    }
}

package dev.yaclient;

/**
 * Cosmetics catalog and the player's selection. Items are unlocked on the website; the selection is
 * saved locally and synced to the store API (see CosmeticsApi). Rendering is done by cosmetics/CosmeticLayer.
 * Item ids are "<bundle prefix>_<slot>", e.g. ya_cape or mono_wings.
 */
public final class Cosmetics {
    /** One item per slot in every bundle. Order = order in the locker. */
    public static final String[] SLOTS = {"cape", "wings", "bandana", "pet"};
    /** Store ids of the bundles and the prefix of their items. */
    public static final String[] BUNDLES = {"beta", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost", "clown", "luck"};
    public static final String[] PREFIX = {"ya", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost", "clown", "luck"};
    /** Partner (streamer) bundles are listed in their own "Partner" section. */
    public static final boolean[] PARTNER = {false, false, true, false, false, false, false, false, false, false};
    /** Streamer name shown on a partner bundle. */
    public static final String[] PARTNER_NAME = {"", "", "Qlux", "", "", "", "", "", "", ""};

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

    public static boolean isExtra(String item) { for (String e : EXTRAS) if (e.equals(item)) return true; return false; }

    /** Equips (or removes when it is already worn) a single item. */
    public static void toggleItem(String slot, String item) { equip(slot, selected(slot).equals(item) ? "none" : item); }

    public static boolean isPartner(int bundle) { return PARTNER[bundle]; }

    private Cosmetics() {}

    public static String item(int bundle, String slot) { return PREFIX[bundle] + "_" + slot; }

    public static String slotOf(String item) {
        int i = item == null ? -1 : item.lastIndexOf('_');
        return i < 0 ? null : item.substring(i + 1);
    }

    public static boolean ownsBundle(int bundle) {
        for (String s : SLOTS) if (!CosmeticsApi.owned(item(bundle, s))) return false;
        return true;
    }

    /** What the player chose for the slot ("none" if nothing). */
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

    /** Equips (or removes when it is already worn) one item of a bundle. */
    public static void toggle(int bundle, String slot) {
        String item = item(bundle, slot);
        equip(slot, selected(slot).equals(item) ? "none" : item);
    }

    public static boolean wearsBundle(int bundle) {
        for (String s : SLOTS) if (!selected(s).equals(item(bundle, s))) return false;
        return true;
    }

    /** Equips the whole bundle, or removes everything when all of it is already worn. */
    public static void toggleBundle(int bundle) {
        boolean all = wearsBundle(bundle);
        java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
        for (String s : SLOTS) { String v = all ? "none" : item(bundle, s); Config.set("cos." + s, v); m.put(s, v); }
        Config.save();
        CosmeticsApi.pushEquipAll(m);
    }
}

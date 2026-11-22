package dev.yaclient.modules;

import dev.yaclient.Lang;

/** Module categories shown as tabs in the menu (the "All" tab is virtual). */
public enum Category {
    INFO("info"),
    PVP("sword"),
    UTILITY("wrench");

    public final String icon;

    Category(String icon) { this.icon = icon; }

    public String label() { return Lang.t("cat." + name().toLowerCase(java.util.Locale.ROOT)); }
}

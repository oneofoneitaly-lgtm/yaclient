package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Tints a player in your color while they are hurt (the layer is HitTintLayer, installed on the player renderers). */
public class HitColorModule extends Module {
    final Setting.Palette color = add(new Setting.Palette("color", 2));
    final Setting.Num intensity = add(new Setting.Num("intensity", 10f, 100f, 5f, 60f, "%"));
    final Setting.Bool self = add(new Setting.Bool("self", false));

    static HitColorModule inst;

    public HitColorModule() { super("hit_color", Category.PVP, false); inst = this; }

    public static boolean active() { return inst != null && inst.enabled; }
}

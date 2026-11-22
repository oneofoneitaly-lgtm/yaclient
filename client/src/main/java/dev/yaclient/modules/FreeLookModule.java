package dev.yaclient.modules;

import dev.yaclient.Setting;

/**
 * Free Look: hold the key and the camera turns around you while your player keeps facing (and aiming) the same way.
 * The key is chosen in the options of this mod. The work is done by {@link dev.yaclient.FreeLook}.
 */
public class FreeLookModule extends Module {
    private final Setting.Bool third = add(new Setting.Bool("third_person", true));
    private final Setting.Key key = add(new Setting.Key("key", org.lwjgl.input.Keyboard.KEY_V));
    private static FreeLookModule inst;

    public FreeLookModule() { super("freelook", Category.UTILITY, true); inst = this; }

    public static boolean on() { return inst != null && inst.enabled && !inst.locked(); }

    /** True while the chosen key (or mouse button) is held. */
    public static boolean held() { return inst != null && inst.key.down(); }

    public static boolean thirdPerson() { return inst == null || inst.third.value; }
}

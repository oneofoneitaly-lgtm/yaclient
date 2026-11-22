package dev.yaclient.modules;

import dev.yaclient.Config;
import dev.yaclient.Lang;
import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of every client feature. Ids are stable (used for config and translations):
 * display name = lang key "mod.&lt;id&gt;.name", description = "mod.&lt;id&gt;.desc".
 */
public abstract class Module {
    public final String id;
    public final Category category;
    /** False for planned features that are listed in the menu but cannot be enabled yet. */
    public final boolean available;
    public boolean enabled;
    /** Only YA Plus members can use it. */
    public boolean plusOnly;
    public final List<Setting> settings = new ArrayList<Setting>();
    protected Minecraft mc;

    protected Module(String id, Category category, boolean enabledByDefault) {
        this(id, category, enabledByDefault, true);
    }

    protected Module(String id, Category category, boolean enabledByDefault, boolean available) {
        this.id = id;
        this.category = category;
        this.available = available;
        this.enabled = available && enabledByDefault;
    }

    /** Icon name: the module's own icon when it has one, otherwise its category icon. */
    public String icon() { return dev.yaclient.Gfx.hasIcon(id) ? id : category.icon; }

    public String name() { return Lang.t("mod." + id + ".name"); }

    public String desc() { return Lang.t("mod." + id + ".desc"); }

    protected <T extends Setting> T add(T s) {
        s.owner = id;
        settings.add(s);
        return s;
    }

    protected <T extends Setting> T add(String group, T s) {
        s.group = group;
        return add(s);
    }

    /** Every option back to its default value. */
    public void resetSettings() {
        for (Setting s : settings) s.reset();
        saveConfig();
    }

    public void loadConfig() {
        enabled = available && Config.getBool("mod." + id + ".enabled", enabled);
        for (Setting s : settings) s.load();
    }

    public void saveConfig() {
        Config.setBool("mod." + id + ".enabled", enabled);
        for (Setting s : settings) s.save();
    }

    /** Refreshes the Minecraft reference (it does not exist yet when modules are created). */
    public final void bind() { mc = Minecraft.getMinecraft(); }

    /** True when this is a Plus mod and the player has no Plus. */
    public boolean locked() { return plusOnly && !dev.yaclient.CosmeticsApi.plus(); }

    public void toggle() {
        if (!available || locked()) return;
        enabled = !enabled;
        onToggle();
        saveConfig();
        Config.save();
    }

    public void onToggle() {}

    private int failures;

    /** Runs one of this module's callbacks so a bug in one mod can never break the game or the other mods. */
    public final void guard(Runnable r) {
        try { r.run(); failures = 0; }
        catch (Throwable t) {
            if (failures == 0) System.out.println("[YA CLIENT] module " + id + " error: " + t);
            if (++failures >= 200) { enabled = false; failures = 0; System.out.println("[YA CLIENT] module " + id + " disabled after repeated errors"); }
        }
    }

    /** Called every tick while the module is enabled. */
    public void onTick() {}

    /** Called every tick even when the module is disabled (cleanup). */
    public void onAlwaysTick() {}

    /** Called every frame while enabled, before the HUD elements are drawn. */
    public void onRender() {}
}

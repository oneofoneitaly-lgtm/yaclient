package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

import java.util.function.Function;

/** Modules that hold one video / control option at a value while they are on, and give it back when switched off. */
public final class Tweaks {
    private Tweaks() {}

    public static class Opt<T> extends Module {
        private final Function<Options, OptionInstance<T>> opt;
        private final T value;
        private T saved;
        private boolean has;

        public Opt(String id, String name, String desc, Category c, Function<Options, OptionInstance<T>> opt, T value) {
            super(id, name, desc, c, false);
            this.opt = opt;
            this.value = value;
        }

        @Override
        public void onTick() {
            OptionInstance<T> o = opt.apply(Minecraft.getInstance().options);
            if (!has) { saved = o.get(); has = true; }
            if (!o.get().equals(value)) o.set(value);
        }

        @Override
        public void onToggle() {
            if (!enabled && has) { opt.apply(Minecraft.getInstance().options).set(saved); has = false; }
        }
    }
}

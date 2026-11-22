package dev.yaclient.fabric;

import java.util.Locale;

/** A configurable option of a module, saved in the config file (same kinds as the 1.8.9 client). */
public abstract class Setting {
    public final String id;
    public String owner = "";
    public String group = "general";

    protected Setting(String id) { this.id = id; }

    public abstract void reset();

    public abstract void load();

    public abstract void save();

    protected String key() { return "set." + owner + "." + id; }

    public static String pretty(String s) {
        if (s == null || s.isEmpty()) return "";
        String t = s.replace('_', ' ');
        return t.substring(0, 1).toUpperCase(Locale.ROOT) + t.substring(1);
    }

    public String label() {
        switch (id) {
            case "bg": return "Background";
            case "bg_opacity": return "Background opacity";
            case "bg_color": return "Background color";
            case "text_size": return "Text size";
            case "box_height": return "Box height";
            case "opacity": return "Opacity";
            default: return pretty(id);
        }
    }

    public static final class Bool extends Setting {
        public boolean value;
        private final boolean def;

        public Bool(String id, boolean def) { super(id); this.def = def; this.value = def; }

        @Override public void load() { value = Config.getBool(key(), def); }

        @Override public void reset() { value = def; }

        @Override public void save() { Config.setBool(key(), value); }
    }

    public static final class Num extends Setting {
        public float value;
        public final float min, max, step, def;
        public final String unit;

        public Num(String id, float min, float max, float step, float def, String unit) {
            super(id);
            this.min = min; this.max = max; this.step = step; this.def = def; this.unit = unit;
            this.value = def;
        }

        public void setFraction(float f) {
            float v = min + Ui.clamp(f, 0f, 1f) * (max - min);
            if (step > 0f) v = Math.round(v / step) * step;
            value = Ui.clamp(v, min, max);
        }

        public float fraction() { return max == min ? 0f : (value - min) / (max - min); }

        public String text() {
            String n = (step >= 1f) ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
            return n + unit;
        }

        @Override public void load() { value = Ui.clamp(Config.getFloat(key(), def), min, max); }

        @Override public void reset() { value = def; }

        @Override public void save() { Config.setFloat(key(), value); }
    }

    public static final class Choice extends Setting {
        public int index;
        public final String[] options;
        private final int def;

        public Choice(String id, int def, String... options) { super(id); this.options = options; this.def = def; this.index = def; }

        public String current() { return options[Ui.clamp(index, 0, options.length - 1)]; }

        @Override public void load() { index = Ui.clamp(Config.getInt(key(), def), 0, options.length - 1); }

        @Override public void reset() { index = def; }

        @Override public void save() { Config.setInt(key(), index); }
    }

    public static final class Palette extends Setting {
        public static final String[] IDS = {"white", "silver", "red", "orange", "gold", "green", "cyan", "blue", "violet", "pink", "accent", "rainbow"};
        private static final int[] RGB = {0xFFF4F4F5, 0xFF9A9AA5, 0xFFEF4444, 0xFFF97316, 0xFFFFC21A, 0xFF22C55E, 0xFF22D3EE, 0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0, 0};
        public int index;
        private final int def;

        public Palette(String id, int def) { super(id); this.def = def; this.index = def; }

        public static int color(int i) {
            i = Ui.clamp(i, 0, IDS.length - 1);
            if (IDS[i].equals("accent")) return Theme.accent();
            if (IDS[i].equals("rainbow")) return 0xFF000000 | (java.awt.Color.HSBtoRGB((System.currentTimeMillis() % 4000L) / 4000f, 0.65f, 1f) & 0xFFFFFF);
            return RGB[i];
        }

        public int rgb() { return color(index); }

        @Override public void load() { index = Ui.clamp(Config.getInt(key(), def), 0, IDS.length - 1); }

        @Override public void save() { Config.setInt(key(), index); }

        @Override public void reset() { index = def; }
    }
}

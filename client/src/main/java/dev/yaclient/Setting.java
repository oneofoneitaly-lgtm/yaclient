package dev.yaclient;

/** A configurable option of a module. Values are persisted in the config file. */
public abstract class Setting {
    public final String id;
    /** Id of the module that owns this setting (set by Module.add). */
    public String owner = "";
    /** Section of the options page this setting is shown in (lang key "grp.&lt;group&gt;"). */
    public String group = "general";

    protected Setting(String id) { this.id = id; }

    public Setting in(String g) { group = g; return this; }

    /** Back to the default value (the "Reset options" button). */
    public abstract void reset();

    protected String key() { return "set." + owner + "." + id; }

    /** Localized label: "set.<module>.<id>" first, then the shared "set.<id>". */
    public String label() {
        String specific = "set." + owner + "." + id;
        return Lang.has(specific) ? Lang.t(specific) : Lang.t("set." + id);
    }

    public abstract void load();

    public abstract void save();

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
        /** Optional suffix shown after the value, e.g. "%" or "x". */
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
            String n = (step >= 1f) ? String.valueOf(Math.round(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
            return n + unit;
        }

        @Override public void load() { value = Ui.clamp(Config.getFloat(key(), def), min, max); }

        @Override public void reset() { value = def; }

        @Override public void save() { Config.setFloat(key(), value); }
    }

    public static final class Choice extends Setting {
        public int index;
        public final String[] options; // option ids; label key is "opt.<id>"
        private final int def;

        public Choice(String id, int def, String... options) {
            super(id);
            this.options = options; this.def = def; this.index = def;
        }

        public String current() { return options[Ui.clamp(index, 0, options.length - 1)]; }

        @Override public void load() { index = Ui.clamp(Config.getInt(key(), def), 0, options.length - 1); }

        @Override public void reset() { index = def; }

        @Override public void save() { Config.setInt(key(), index); }
    }

    /** A color picked from a fixed palette (shown as round swatches). */
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

    /** Free text typed by the player (e.g. the crosshair shape or text). */
    public static final class Text extends Setting {
        public String value;
        public final int maxLen;
        private final String def;

        public Text(String id, String def, int maxLen) { super(id); this.def = def; this.value = def; this.maxLen = maxLen; }

        @Override public void load() { String v = Config.get(key(), def); value = v == null ? def : v; }

        @Override public void reset() { value = def; }

        @Override public void save() { Config.set(key(), value); }
    }

    /** A small bitmap the player draws (the crosshair): n x n cells, true = lit. */
    public static final class Pixels extends Setting {
        public final int n;
        public boolean[] cells;
        private final String def;

        public Pixels(String id, int n, String def) { super(id); this.n = n; this.def = def; this.cells = parse(def); }

        private boolean[] parse(String s) {
            boolean[] c = new boolean[n * n];
            if (s != null && s.length() == n * n) for (int i = 0; i < c.length; i++) c[i] = s.charAt(i) == '1';
            return c;
        }

        public String encode() {
            StringBuilder b = new StringBuilder();
            for (boolean c : cells) b.append(c ? '1' : '0');
            return b.toString();
        }

        public void set(String pattern) { cells = parse(pattern); }

        @Override public void load() { cells = parse(Config.get(key(), def)); }

        @Override public void reset() { cells = parse(def); }

        @Override public void save() { Config.set(key(), encode()); }
    }

    /** A key or mouse button picked by the player (LWJGL key code; mouse buttons are stored as -100 + button). */
    public static final class Key extends Setting {
        public int code;
        private final int def;

        public Key(String id, int def) { super(id); this.def = def; this.code = def; }

        public boolean down() {
            try { return code < 0 ? org.lwjgl.input.Mouse.isButtonDown(code + 100) : code > 0 && org.lwjgl.input.Keyboard.isKeyDown(code); } catch (Throwable t) { return false; }
        }

        public String text() {
            if (code == 0) return "-";
            if (code < 0) { int b = code + 100; return b == 2 ? "Mouse 3 (wheel)" : "Mouse " + (b + 1); }
            String n = org.lwjgl.input.Keyboard.getKeyName(code);
            return n == null ? "?" : n;
        }

        @Override public void load() { code = Config.getInt(key(), def); }

        @Override public void reset() { code = def; }

        @Override public void save() { Config.setInt(key(), code); }
    }
}

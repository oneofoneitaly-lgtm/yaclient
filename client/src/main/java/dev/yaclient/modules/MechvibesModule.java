package dev.yaclient.modules;

import dev.yaclient.Setting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Mechvibes: real keyboard sounds (Cherry MX Black ABS pack, one sound per key) and a mouse click sound (Razer mouse pack).
 * The sounds are mixed on one audio line and played from a small background thread that watches the keys and mouse buttons,
 * so it works in game, in the chat and in menus and never touches the render thread.
 */
public class MechvibesModule extends Module {
    private final Setting.Num volume = add("general", new Setting.Num("volume", 0f, 100f, 5f, 70f, "%"));
    private final Setting.Choice keyboard = add("general", new Setting.Choice("mv_keyboard", 0, "mv_cherry", "mv_orange"));
    private final Setting.Choice mousePack = add("general", new Setting.Choice("mv_mouse", 0, "mv_razer", "mv_logitech"));
    private final Setting.Bool release = add("general", new Setting.Bool("key_release", true));
    private final Setting.Bool mouse = add("general", new Setting.Bool("mouse_clicks", true));
    private final Setting.Bool inMenus = add("general", new Setting.Bool("in_menus", true));

    public MechvibesModule() { super("mechvibes", Category.UTILITY, false); }

    private static final int RATE = 44100;
    private static final String BASE = "/assets/yaclient/mv/";
    private static final String[] KB = { "cherry", "orange" };
    private static final String[] MS = { "razer", "logitech" };
    private Thread thread;

    @Override
    public void onAlwaysTick() {
        if (thread == null && enabled) {
            thread = new Thread(this::loop, "YA-Mechvibes");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private static final class Voice {
        final short[] d; int pos; final float gain;
        Voice(short[] d, float gain) { this.d = d; this.gain = gain; }
    }

    /** Reads a 44.1 kHz mono 16-bit WAV from the jar. */
    private static short[] wav(String dir, String name) {
        try {
            InputStream in = MechvibesModule.class.getResourceAsStream(BASE + dir + "/" + name);
            if (in == null) return null;
            AudioInputStream a = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = a.read(buf)) > 0) bo.write(buf, 0, n);
            a.close();
            byte[] b = bo.toByteArray();
            short[] out = new short[b.length / 2];
            for (int i = 0; i < out.length; i++) out[i] = (short) ((b[2 * i] & 255) | (b[2 * i + 1] << 8));
            return out;
        } catch (Throwable t) { return null; }
    }

    /** Reads a pack table: "code file [releaseFile]" lines, returned as {code, file, dir[, releaseFile]}. */
    private static List<String[]> table(String dir, String file) {
        List<String[]> r = new ArrayList<String[]>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(MechvibesModule.class.getResourceAsStream(BASE + dir + "/" + file), "UTF-8"));
            String ln;
            while ((ln = in.readLine()) != null) {
                String[] p = ln.trim().split(" ");
                if (p.length < 2) continue;
                String[] e = new String[p.length + 1];
                e[0] = p[0]; e[1] = p[1]; e[2] = dir;
                if (p.length > 2) e[3] = p[2];
                r.add(e);
            }
            in.close();
        } catch (Throwable ignored) { }
        return r;
    }

    // ---- audio thread ----
    private void loop() {
        SourceDataLine line;
        try {
            line = AudioSystem.getSourceDataLine(new AudioFormat(RATE, 16, 1, true, false));
            line.open(new AudioFormat(RATE, 16, 1, true, false), 4096);
            line.start();
        } catch (Throwable t) { return; }   // no audio device: the mod simply stays silent
        // [code][0] = press sound, [code][1] = release sound (when the pack has one)
        short[][][] byKey = new short[256][][];
        short[][][] byBtn = new short[5][][];
        short[] any = null;
        int kbLoaded = -1, msLoaded = -1;
        final List<Voice> voices = new ArrayList<Voice>();
        final boolean[] prev = new boolean[256];
        final boolean[] prevM = new boolean[5];
        final int CH = 128;
        final byte[] out = new byte[CH * 2];
        final float[] mix = new float[CH];
        while (true) {
            try {
                if (kbLoaded != keyboard.index) {
                    kbLoaded = keyboard.index;
                    byKey = new short[256][][]; any = null;
                    for (String[] e : table(KB[Math.min(kbLoaded, KB.length - 1)], "keys.txt")) {
                        int code = Integer.parseInt(e[0]);
                        short[][] pair = { wav(e[2], e[1]), e.length > 3 ? wav(e[2], e[3]) : null };
                        if (pair[0] != null && code > 0 && code < 256) { byKey[code] = pair; if (any == null) any = pair[0]; }
                    }
                }
                if (msLoaded != mousePack.index) {
                    msLoaded = mousePack.index;
                    byBtn = new short[5][][];
                    for (String[] e : table(MS[Math.min(msLoaded, MS.length - 1)], "mouse.txt")) {
                        int b = Integer.parseInt(e[0]);
                        if (b >= 0 && b < 5) byBtn[b] = new short[][] { wav(e[2], e[1]), e.length > 3 ? wav(e[2], e[3]) : null };
                    }
                }
                boolean active = enabled && mc != null && Display.isActive() && Keyboard.isCreated();
                if (active && (inMenus.value || mc.currentScreen == null)) {
                    float g = Math.max(0f, Math.min(1f, volume.value / 100f)) * 0.9f;
                    for (int k = 1; k < 256; k++) {
                        boolean d = Keyboard.isKeyDown(k);
                        if (d != prev[k]) {
                            prev[k] = d;
                            short[] s = d ? (byKey[k] != null ? byKey[k][0] : any)
                                    : (release.value && byKey[k] != null ? byKey[k][1] : null);
                            if (s != null && voices.size() < 24) voices.add(new Voice(s, g));
                        }
                    }
                    if (mouse.value && Mouse.isCreated()) {
                        for (int b = 0; b < 5; b++) {
                            boolean d = b < Mouse.getButtonCount() && Mouse.isButtonDown(b);
                            if (d != prevM[b]) {
                                prevM[b] = d;
                                short[] s = byBtn[b] == null ? null : byBtn[b][d ? 0 : 1];
                                if (s != null && voices.size() < 24) voices.add(new Voice(s, g));
                            }
                        }
                    }
                } else {
                    java.util.Arrays.fill(prev, false);
                    java.util.Arrays.fill(prevM, false);
                    if (!enabled) { voices.clear(); }
                }
                java.util.Arrays.fill(mix, 0f);
                for (int i = voices.size() - 1; i >= 0; i--) {
                    Voice v = voices.get(i);
                    for (int n = 0; n < CH && v.pos < v.d.length; n++, v.pos++) mix[n] += v.d[v.pos] * v.gain;
                    if (v.pos >= v.d.length) voices.remove(i);
                }
                for (int n = 0; n < CH; n++) {
                    int s = Math.max(-32768, Math.min(32767, Math.round(mix[n])));
                    out[2 * n] = (byte) s; out[2 * n + 1] = (byte) (s >> 8);
                }
                line.write(out, 0, out.length);   // blocks until the line has room: this is the pacing
            } catch (Throwable t) {
                try { Thread.sleep(50); } catch (InterruptedException e) { return; }
            }
        }
    }
}

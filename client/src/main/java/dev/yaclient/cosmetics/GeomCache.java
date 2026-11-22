package dev.yaclient.cosmetics;

import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * Display-list cache for cosmetic geometry. The geometry only depends on the style and on the animation time,
 * so it is built once per ~30 Hz time step and replayed for every player / side / frame in between.
 */
final class GeomCache {
    private static final class E { int list; int bucket = Integer.MIN_VALUE; }
    private static final Map<String, E> MAP = new HashMap<String, E>();

    /** Time step of the animation in ticks (1.67 ticks = 30 Hz). */
    static int bucket(float age) { return (int) (age / 1.67f); }

    /** Runs build (a Poly.begin ... Poly.end block) at most once per bucket and replays the recorded list otherwise. */
    static void draw(String key, int bucket, Runnable build) {
        E e = MAP.get(key);
        if (e == null) { e = new E(); MAP.put(key, e); }
        if (e.list != 0 && e.bucket == bucket) { GL11.glCallList(e.list); return; }
        if (e.list == 0) e.list = GL11.glGenLists(1);
        if (e.list == 0) { build.run(); return; }
        e.bucket = bucket;
        GL11.glNewList(e.list, GL11.GL_COMPILE_AND_EXECUTE);
        try { build.run(); } finally { GL11.glEndList(); }
    }
}

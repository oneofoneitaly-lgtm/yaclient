package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Keeps the last seconds of your game as small pictures (2 per second, in memory) and writes them to a folder when you press the key:
 * yaclient-replays/<date>/frame_001.jpg ... A "photo replay" of the last moments of a fight, not a video.
 */
public class ReplayBufferModule extends Module {
    private final Setting.Key key = add(new Setting.Key("key", org.lwjgl.input.Keyboard.KEY_F9));
    private final Setting.Num seconds = add(new Setting.Num("seconds", 10f, 60f, 5f, 30f, "s"));

    private final ArrayDeque<byte[]> ring = new ArrayDeque<byte[]>();
    private final ExecutorService ex = Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
        public Thread newThread(Runnable r) { Thread t = new Thread(r, "YA-Replay"); t.setDaemon(true); return t; }
    });
    private long last;
    private boolean was;
    private volatile boolean busy;

    public ReplayBufferModule() { super("replay_buffer", Category.UTILITY, false); }

    @Override
    public void onRender() {
        if (mc == null || mc.theWorld == null || busy) return;
        long now = System.currentTimeMillis();
        if (now - last < 500) return;
        last = now;
        try {
            final int w = mc.displayWidth, h = mc.displayHeight;
            final ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 3);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glReadPixels(0, 0, w, h, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf);
            busy = true;
            ex.submit(new Runnable() {
                public void run() {
                    try {
                        int ow = 640, oh = Math.max(1, h * ow / w);
                        BufferedImage img = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB);
                        for (int y = 0; y < oh; y++) {
                            int sy = h - 1 - (y * h / oh);
                            for (int x = 0; x < ow; x++) {
                                int i = (sy * w + (x * w / ow)) * 3;
                                img.setRGB(x, y, ((buf.get(i) & 255) << 16) | ((buf.get(i + 1) & 255) << 8) | (buf.get(i + 2) & 255));
                            }
                        }
                        ByteArrayOutputStream bo = new ByteArrayOutputStream();
                        javax.imageio.ImageIO.write(img, "jpg", bo);
                        synchronized (ring) {
                            ring.addLast(bo.toByteArray());
                            int max = Math.round(seconds.value * 2f);
                            while (ring.size() > max) ring.removeFirst();
                        }
                    } catch (Throwable ignored) { }
                    busy = false;
                }
            });
        } catch (Throwable t) { busy = false; }
    }

    @Override
    public void onTick() {
        if (mc == null || mc.thePlayer == null) return;
        boolean d = mc.currentScreen == null && key.down();
        if (d && !was) save();
        was = d;
    }

    private void save() {
        final java.util.List<byte[]> frames;
        synchronized (ring) { frames = new java.util.ArrayList<byte[]>(ring); }
        if (frames.isEmpty()) return;
        final File dir = new File(mc.mcDataDir, "yaclient-replays/" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()));
        ex.submit(new Runnable() {
            public void run() {
                try {
                    dir.mkdirs();
                    for (int i = 0; i < frames.size(); i++) {
                        FileOutputStream o = new FileOutputStream(new File(dir, String.format("frame_%03d.jpg", i + 1)));
                        o.write(frames.get(i));
                        o.close();
                    }
                } catch (Throwable ignored) { }
            }
        });
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\u00a7cYA \u00a77Replay saved: \u00a7f" + frames.size() + " frames \u00a77in yaclient-replays"));
    }
}

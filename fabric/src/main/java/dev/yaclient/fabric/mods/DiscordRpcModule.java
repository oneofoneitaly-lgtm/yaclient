package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Config;
import dev.yaclient.fabric.Module;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/** Discord Rich Presence through the local Discord IPC pipe (Windows), no native library needed. */
public class DiscordRpcModule extends Module {
    /** The application's NAME is what Discord shows after "Playing". */
    private static final String DEFAULT_ID = "1562827651100581970";
    private volatile String state = "In menus";
    private final String details;
    private Thread thread;
    private final long startedAt = System.currentTimeMillis() / 1000L;

    public DiscordRpcModule() {
        super("discord_rpc", "Discord RPC", "Shows YA Client on your Discord profile", Category.UTILITY, true);
        String v = "";
        try { v = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString(); } catch (Throwable ignored) { }
        details = "YA Client " + v;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) state = "In menus";
        else if (mc.getCurrentServer() == null) state = "Singleplayer";
        else state = "Playing on " + mc.getCurrentServer().ip;
        if (thread == null) {
            thread = new Thread(this::loop, "YA-DiscordRPC");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void loop() {
        while (true) {
            String id = Config.get("discord.clientId", DEFAULT_ID).trim();
            RandomAccessFile pipe = null;
            try {
                if (enabled && !id.isEmpty()) pipe = open();
                if (pipe != null) {
                    send(pipe, 0, "{\"v\":1,\"client_id\":\"" + esc(id) + "\"}");
                    read(pipe);
                    while (enabled) {
                        send(pipe, 1, activity());
                        read(pipe);
                        Thread.sleep(15000);
                    }
                }
            } catch (InterruptedException e) {
                return;
            } catch (Throwable ignored) {
            } finally {
                try { if (pipe != null) pipe.close(); } catch (Exception ignored) { }
            }
            try { Thread.sleep(10000); } catch (InterruptedException e) { return; }
        }
    }

    private String activity() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        return "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + pid + ",\"activity\":{"
                + "\"details\":\"" + esc(details) + "\","
                + "\"state\":\"" + esc(state) + "\","
                + "\"timestamps\":{\"start\":" + startedAt + "}}},"
                + "\"nonce\":\"" + UUID.randomUUID() + "\"}";
    }

    private static RandomAccessFile open() {
        for (int i = 0; i < 10; i++) {
            try { return new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw"); }
            catch (Exception ignored) { }
        }
        return null;
    }

    private static void send(RandomAccessFile f, int op, String json) throws Exception {
        byte[] b = json.getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(8 + b.length).order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(op);
        bb.putInt(b.length);
        bb.put(b);
        f.write(bb.array());
    }

    private static void read(RandomAccessFile f) throws Exception {
        byte[] head = new byte[8];
        f.readFully(head);
        int len = ByteBuffer.wrap(head).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
        if (len < 0 || len > 1 << 20) throw new Exception("bad frame");
        f.readFully(new byte[len]);
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

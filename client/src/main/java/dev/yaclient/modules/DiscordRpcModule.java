package dev.yaclient.modules;

import dev.yaclient.Config;

import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Discord Rich Presence through the local Discord IPC pipe (Windows), no native library needed.
 * Create an application at https://discord.com/developers/applications and put its Application ID
 * in ~/.yaclient/config.properties as: discord.clientId=123456789012345678
 */
public class DiscordRpcModule extends Module {
    /** Discord application whose NAME is shown as "Playing ...": set it to "YA Client 1.8.9" in the developer portal. */
    private static final String DEFAULT_ID = "1562827651100581970";
    private volatile String state = "In menus";
    private Thread thread;
    private final long startedAt = System.currentTimeMillis() / 1000L;

    public DiscordRpcModule() { super("discord_rpc", Category.UTILITY, true); }

    @Override
    public void loadConfig() {
        super.loadConfig();
        // one-time migration: the module used to be off by default
        if (!Config.getBool("discord.rpc.migrated", false)) {
            enabled = true;
            Config.setBool("discord.rpc.migrated", true);
            saveConfig();
            Config.save();
        }
    }

    private static void log(String s) {
        try {
            java.io.File f = new java.io.File(System.getProperty("user.home"), ".yaclient/discord.log");
            f.getParentFile().mkdirs();
            java.io.FileWriter w = new java.io.FileWriter(f, true);
            w.write(new java.util.Date() + " " + s + "\n");
            w.close();
        } catch (Exception ignored) { }
    }

    @Override
    public void onTick() {
        if (mc.theWorld == null) state = "In menus";
        else if (mc.isSingleplayer()) state = "Singleplayer";
        else if (mc.getCurrentServerData() != null) state = "Playing on " + mc.getCurrentServerData().serverIP;
        else state = "In game";
    }

    @Override
    public void onAlwaysTick() {
        if (enabled && thread == null) start();
    }

    private void start() {
        thread = new Thread(new Runnable() {
            @Override public void run() { loop(); }
        }, "YA-DiscordRPC");
        thread.setDaemon(true);
        thread.start();
    }

    private void loop() {
        while (true) {
            String id = Config.get("discord.clientId", DEFAULT_ID).trim();
            RandomAccessFile pipe = null;
            try {
                if (enabled && !id.isEmpty()) pipe = open();
                if (pipe == null) log("pipe discord-ipc non trovata (Discord aperto?)");
                if (pipe != null) {
                    log("connesso a Discord, id " + id);
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
            } catch (Exception e) {
                log("errore: " + e);
            } finally {
                try { if (pipe != null) pipe.close(); } catch (Exception ignored) { }
            }
            try { Thread.sleep(10000); } catch (InterruptedException e) { return; }
        }
    }

    private String activity() {
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        return "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + pid + ",\"activity\":{"
                + "\"details\":\"YA Client 1.8.9\","
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

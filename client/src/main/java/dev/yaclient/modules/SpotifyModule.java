package dev.yaclient.modules;

import dev.yaclient.Gfx;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Shows the song currently playing in the Spotify desktop app (Windows). It reads the window
 * title of Spotify.exe in a background thread - no account or API key needed.
 */
public class SpotifyModule extends PillModule {
    private volatile String track = "";
    private Thread thread;

    public SpotifyModule() { super("spotify", Category.INFO, false, 0.008f, 0.93f); }

    @Override
    public void onAlwaysTick() {
        if (enabled && thread == null) start();
    }

    private void start() {
        thread = new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    if (enabled) track = query();
                    try { Thread.sleep(4000); } catch (InterruptedException e) { return; }
                }
            }
        }, "YA-Spotify");
        thread.setDaemon(true);
        thread.start();
    }

    private static String query() {
        try {
            Process p = new ProcessBuilder("tasklist", "/v", "/fo", "csv", "/nh", "/fi", "imagename eq Spotify.exe")
                    .redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            String found = "";
            while ((line = r.readLine()) != null) {
                int i = line.lastIndexOf("\",\"");
                if (i < 0) continue;
                String title = line.substring(i + 3).replace("\"", "").trim();
                if (title.contains(" - ")) found = title;
            }
            r.close();
            return found;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected String text() {
        String t = track;
        return t.isEmpty() ? "" : Gfx.trim(t, 10, false, 220);
    }

    @Override
    protected String sample() { return "Artist - Song"; }
}

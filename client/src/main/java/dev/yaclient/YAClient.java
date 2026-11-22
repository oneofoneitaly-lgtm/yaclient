package dev.yaclient;

import dev.yaclient.cosmetics.CosmeticsInstaller;
import dev.yaclient.modules.*;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiSelectWorld;
import net.weavemc.api.ModInitializer;
import net.weavemc.api.event.EventBus;
import net.weavemc.api.event.RenderGameOverlayEvent;
import net.weavemc.api.event.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/** YA CLIENT entry point (Weave loader, no Forge). Only legitimate features: no cheats. */
public class YAClient implements ModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final String VERSION = "1.1.0";
    /** Key that opens the client menu (close it with ESC). */
    public static final int MENU_KEY = Keyboard.KEY_RSHIFT;

    public static final List<Module> MODULES = new ArrayList<Module>();
    private boolean menuKeyWasDown;

    @Override
    public void init() {
        Config.load();
        Lang.load(Config.get("lang", "en"));
        Theme.load();
        Hud.loadConfig();

        // HUD elements
        MODULES.add(new FpsModule());
        MODULES.add(new CpsModule());
        MODULES.add(new CoordsModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new ArmorModule());
        MODULES.add(new PingModule());
        MODULES.add(new DirectionModule());
        MODULES.add(new ClockModule());
        MODULES.add(new SpeedModule());
        MODULES.add(new MemoryModule());
        MODULES.add(new HealthModule());
        MODULES.add(new PotionModule());
        MODULES.add(new ReachModule());
        MODULES.add(new SpotifyModule());
        MODULES.add(new WaypointsModule());
        // utilities
        MODULES.add(new DayCounterModule());
        MODULES.add(new BiomeModule());
        MODULES.add(new LightLevelModule());
        MODULES.add(new ServerAddressModule());
        MODULES.add(new PackInfoModule());
        MODULES.add(new MechvibesModule());
        MODULES.add(new SessionTimeModule());
        MODULES.add(new RotationModule());
        MODULES.add(new ItemInfoModule());
        MODULES.add(new PlayerCountModule());
        MODULES.add(new FoodModule());
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new ToggleSneakModule());
        MODULES.add(new ZoomModule());
        MODULES.add(new ChatModule());
        MODULES.add(new ClipModule());
        MODULES.add(new PerspectiveLockModule());
        MODULES.add(new ChatTweaksModule());
        MODULES.add(new AutoReconnectModule());
        MODULES.add(new ReplayBufferModule());
        MODULES.add(new CalloutsModule());
        MODULES.add(new TeamViewerModule());
        MODULES.add(new FreeLookModule());
        MODULES.add(new NoCloudsModule());
        MODULES.add(new SmoothShiftModule());
        MODULES.add(new BlockOutlineModule());
        BlockOutlineModule.register();
        TeamViewerModule.register();
        CustomSky.register();
        dev.yaclient.modules.HitPopups.register();
        MODULES.add(new FullbrightModule());
        MODULES.add(new MotionBlurModule());
        MODULES.add(new CrosshairModule());
        MODULES.add(new DiscordRpcModule());
        MODULES.add(new TimeChangerModule());
        MODULES.add(new ClearWeatherModule());
        MODULES.add(new HitboxesModule());
        MODULES.add(new FovModule());
        MODULES.add(new CinematicCameraModule());
        MODULES.add(new EntityCountModule());
        MODULES.add(new FallDistanceModule());
        MODULES.add(new XpLevelModule());
        MODULES.add(new ScoreboardModule());
        MODULES.add(new FpsBoostModule());
        // planned (need the Mixin phase, see docs/PHASE2.md)
        MODULES.add(new SoonModule("levelhead", Category.INFO));
        MODULES.add(new HitColorModule());
        MODULES.add(new OldAnimationsModule());
        MODULES.add(new AutoGGModule());
        MODULES.add(new MinimapModule());
        MODULES.add(new ComboModule());
        MODULES.add(new BowChargeModule());
        MODULES.add(new BlockInfoModule());
        MODULES.add(new NoHurtCamModule());
        MODULES.add(new BackgroundFpsModule());
        MODULES.add(new LowHealthModule());
        MODULES.add(new TargetHudModule());
        MODULES.add(new ItemCounterModule());
        MODULES.add(new InventoryHudModule());
        MODULES.add(new NearbyPlayersModule());
        MODULES.add(new BwResourcesModule());
        MODULES.add(new BwBlocksModule());
        MODULES.add(new BwThreatModule());
        MODULES.add(new StreamerModeModule());
        MODULES.add(new CritCounterModule());
        MODULES.add(new HitRatioModule());
        MODULES.add(new KillStreakModule());
        MODULES.add(new ArmorWarningModule());
        MODULES.add(new VoidAlertModule());
        MODULES.add(new GappleTimerModule());
        MODULES.add(new PotionAlertModule());
        MODULES.add(new ClickCounterModule());
        MODULES.add(new CustomTextModule());
        MODULES.add(new FacingModule());
        MODULES.add(new GameTimeModule());
        MODULES.add(new SaturationModule());
        MODULES.add(new WeatherModule());
        MODULES.add(new BlockPlaceModule());
        MODULES.add(new PlayerHeadModule());
        MODULES.add(new MouseStrokesModule());
        MODULES.add(new ShapeModule());
        MODULES.add(new ItemLoreModule());
        MODULES.add(new ItemTrackerModule());
        MODULES.add(new PlayerPreviewModule());

        // no Plus-only mods any more (kept empty on purpose)
        java.util.Set<String> plusMods = new java.util.HashSet<String>(java.util.Arrays.asList(
                ));   // every mod is free for everyone now
        for (Module m : MODULES) { if (plusMods.contains(m.id)) m.plusOnly = true; }
        // new installs: only FPS, CPS, FPS Boost and Discord RPC are on; every other mod starts off (the player turns on what they want)
        if (!Config.getBool("perf.v2", false)) {   // first start of this install (also when the launcher already wrote a few mod switches: only those stay as the player set them)
            java.util.Set<String> base = new java.util.HashSet<String>(java.util.Arrays.asList("fps", "cps", "fps_boost", "discord_rpc"));
            for (Module m : MODULES) if (Config.get("mod." + m.id + ".enabled", null) == null) Config.setBool("mod." + m.id + ".enabled", m.available && base.contains(m.id));
            Config.setBool("perf.v2", true);
            Config.save();
        }
        // existing installs, one time: FPS Boost (unlimited frame rate, light effects) on
        if (!Config.getBool("perf.v2", false)) { Config.setBool("mod.fps_boost.enabled", true); Config.setBool("perf.v2", true); Config.save(); }
        for (Module m : MODULES) m.loadConfig();

        MenuBackdrop.preload();
        // The vanilla title screen would be drawn for a few frames (the game timer runs no tick on its first loop, so the
        // tick swap comes too late). A watcher notices it within a millisecond and swaps it straight away, from its own
        // thread: the title screen needs no OpenGL to be replaced. If that fails, the swap is queued for the next frame.
        Thread tw = new Thread(() -> {
            final Minecraft m1 = Minecraft.getMinecraft();
            while (true) {
                try {
                    Thread.sleep(1);
                    net.minecraft.client.gui.GuiScreen cs = m1.currentScreen;
                    if (cs != null && cs.getClass() == GuiMainMenu.class) {
                        try {
                            m1.displayGuiScreen(new MainMenuScreen());
                        } catch (Throwable direct) {
                            m1.addScheduledTask(() -> {
                                if (m1.currentScreen != null && m1.currentScreen.getClass() == GuiMainMenu.class) m1.displayGuiScreen(new MainMenuScreen());
                            });
                        }
                        Thread.sleep(30);
                    }
                } catch (InterruptedException ie) { return; } catch (Throwable ignored) { }
            }
        }, "YA-TitleSwap");
        tw.setDaemon(true);
        tw.start();
        EventBus.subscribe(TickEvent.Pre.class, e -> {
            Minecraft m0 = Minecraft.getMinecraft();
            if (m0.currentScreen != null && m0.currentScreen.getClass() == GuiMainMenu.class) m0.displayGuiScreen(new MainMenuScreen());
            if (m0.currentScreen != null && m0.currentScreen.getClass() == GuiMultiplayer.class) m0.displayGuiScreen(new ServerListScreen(new MainMenuScreen()));
            if (m0.currentScreen != null && m0.currentScreen.getClass() == GuiSelectWorld.class) m0.displayGuiScreen(new WorldListScreen(new MainMenuScreen()));
            if (m0.currentScreen != null && m0.currentScreen.getClass() == GuiIngameMenu.class) m0.displayGuiScreen(new PauseScreen());
            if (m0.currentScreen != null && m0.currentScreen.getClass() == GuiDownloadTerrain.class && m0.getNetHandler() != null) m0.displayGuiScreen(new YADownload(m0.getNetHandler()));
            NoHurtCamModule.pre(m0);
            FreeLook.pre(m0);
            HotbarGuard.pre(m0);
        });
        EventBus.subscribe(TickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getMinecraft();

            // cosmetics: add the render layer once the renderers exist, keep purchases in sync
            CosmeticsInstaller.tryInstall();
            if (!(mc.ingameGUI instanceof YaIngame) && !ingameFailed) { YaIngame.install(); if (!(mc.ingameGUI instanceof YaIngame)) ingameFailed = true; }
            CosmeticsApi.refreshAsync(false);

            // our title screen replaces the vanilla one
            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMainMenu.class) {
                mc.displayGuiScreen(new MainMenuScreen());
            }
            // leaving a server drops you on our server list, not on the vanilla multiplayer screen
            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMultiplayer.class) {
                mc.displayGuiScreen(new ServerListScreen(new MainMenuScreen()));
            }

            ScoreboardModule.afterOverlay();
            TabBadge.tick();
            FriendsApi.tick();
            HitTracker.tick(mc);
            KeyGuard.tick(mc);
            HotbarGuard.tick(mc);
            RecentServers.tick(mc);
            FreeLook.post(mc);
            for (Module m : MODULES) {
                final Module mm = m;
                m.bind();
                m.guard(new Runnable() { public void run() {
                    mm.onAlwaysTick();
                    if (mm.enabled && mm.locked()) { mm.enabled = false; mm.onToggle(); }
                    if (mm.enabled) mm.onTick();
                } });
            }

            boolean down = Keyboard.isKeyDown(MENU_KEY);
            if (down && !menuKeyWasDown && mc.currentScreen == null) {
                mc.displayGuiScreen(new HubScreen());
            }
            menuKeyWasDown = down;
        });

        EventBus.subscribe(RenderGameOverlayEvent.Pre.class, e -> ScoreboardModule.beforeOverlay());
        EventBus.subscribe(RenderGameOverlayEvent.Post.class, e -> {
            ScoreboardModule.afterOverlay();
            Hud.render();
            FriendToast.render();
        });

        System.out.println("[" + NAME + "] " + VERSION + " loaded. Press Right Shift for the menu.");
    }

    private static boolean ingameFailed;

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(Instrumentation instrumentation) {}
}

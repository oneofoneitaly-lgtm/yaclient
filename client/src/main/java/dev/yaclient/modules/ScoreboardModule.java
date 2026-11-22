package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The sidebar scoreboard as a movable, resizable HUD element. The vanilla one is always taken off the
 * vanilla overlay (see {@link #beforeOverlay()}); this module draws it, so switching the module off
 * hides the scoreboard completely.
 */
public class ScoreboardModule extends HudModule {
    private static ScoreObjective current;

    private final Setting.Bool numbers = add(new Setting.Bool("numbers", true));
    private final Setting.Bool background = add(new Setting.Bool("bg", true));

    public ScoreboardModule() { super("scoreboard", Category.INFO, true, 0.99f, 0.28f); }

    /** Called right before the vanilla overlay: lifts the sidebar objective out of the vanilla HUD. */
    public static void beforeOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        current = null;
        if (mc.theWorld == null) return;
        Scoreboard sb = mc.theWorld.getScoreboard();
        ScoreObjective o = sb.getObjectiveInDisplaySlot(1);
        if (o != null) {
            current = o;
            sb.setObjectiveInDisplaySlot(1, null);
        }
    }

    /** Puts the objective back (idempotent) so the game state is never changed for good. */
    public static void afterOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        if (current != null && mc.theWorld != null && current.getScoreboard() == mc.theWorld.getScoreboard()) {
            mc.theWorld.getScoreboard().setObjectiveInDisplaySlot(1, current);
        }
    }

    private String title() {
        if (current != null) return current.getDisplayName();
        return preview ? "Scoreboard" : "";
    }

    /** Rows from top to bottom: {name, score}. */
    private List<String[]> rows() {
        List<String[]> out = new ArrayList<String[]>();
        if (current == null || mc == null || mc.theWorld == null) {
            if (preview) {
                out.add(new String[]{"Player 1", "12"});
                out.add(new String[]{"Player 2", "8"});
                out.add(new String[]{"Player 3", "3"});
            }
            return out;
        }
        Scoreboard sb = current.getScoreboard();
        Collection<Score> all = sb.getSortedScores(current);
        List<Score> list = new ArrayList<Score>();
        for (Score s : all) {
            String n = s.getPlayerName();
            if (n != null && !n.startsWith("#")) list.add(s);
        }
        if (list.size() > 15) list = list.subList(list.size() - 15, list.size());
        for (int i = list.size() - 1; i >= 0; i--) {
            Score s = list.get(i);
            ScorePlayerTeam team = sb.getPlayersTeam(s.getPlayerName());
            out.add(new String[]{StreamerModeModule.maskBoard(ScorePlayerTeam.formatPlayerName(team, s.getPlayerName())), String.valueOf(s.getScorePoints())});
        }
        return out;
    }

    @Override
    public int width() {
        if (mc == null) return 0;
        FontRenderer fr = mc.fontRendererObj;
        String t = title();
        List<String[]> rows = rows();
        if (t.isEmpty() && rows.isEmpty()) return 0;
        int w = fr.getStringWidth(t);
        for (String[] r : rows) {
            int rw = fr.getStringWidth(r[0]) + (numbers.value ? 10 + fr.getStringWidth(r[1]) : 0);
            if (rw > w) w = rw;
        }
        return Math.max(90, w + 12);
    }

    @Override
    public int height() { return 16 + rows().size() * 10 + 4; }

    @Override
    protected void draw() {
        FontRenderer fr = mc.fontRendererObj;
        int w = width(), h = height();
        if (w <= 0) return;
        if (background.value) {
            Gfx.rr(0, 0, w, h, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, 0, w, h, 6, 1, 0x1AFFFFFF);
        }
        String t = title();
        fr.drawStringWithShadow(t, (w - fr.getStringWidth(t)) / 2f, 4f, 0xFFFFFF);
        int y = 17;
        for (String[] r : rows()) {
            fr.drawStringWithShadow(r[0], 5f, y, 0xFFFFFF);
            if (numbers.value) fr.drawStringWithShadow(r[1], w - 5f - fr.getStringWidth(r[1]), y, 0xFF5555);
            y += 10;
        }
    }
}

package dev.yaclient.modules;

import dev.yaclient.Setting;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/** Shows the resource pack you are using: its name and its logo. */
public class PackInfoModule extends PillModule {
    public PackInfoModule() { super("pack_info", Category.INFO, false, 0.008f, 0.34f); }

    private final Setting.Bool showIcon = add("layout", new Setting.Bool("show_icon", true));
    private final Setting.Bool showName = add("layout", new Setting.Bool("show_name", true));
    private final Setting.Num iconSize = add("layout", new Setting.Num("icon_size", 8f, 28f, 1f, 14f, ""));
    private final Setting.Choice which = add("general", new Setting.Choice("pack_which", 0, "top", "all_packs"));
    private final Setting.Bool hideDefault = add("general", new Setting.Bool("hide_default", false));

    private ResourcePackRepository.Entry top() {
        if (mc == null) return null;
        List<ResourcePackRepository.Entry> l = mc.getResourcePackRepository().getRepositoryEntries();
        return l == null || l.isEmpty() ? null : l.get(l.size() - 1);   // the last one in the list wins
    }

    private static String clean(String n) {
        if (n == null) return "";
        n = EnumChatFormatting.getTextWithoutFormattingCodes(n);
        if (n.toLowerCase(java.util.Locale.ROOT).endsWith(".zip")) n = n.substring(0, n.length() - 4);
        return n;
    }

    @Override
    protected String text() {
        if (mc == null) return "";
        List<ResourcePackRepository.Entry> l = mc.getResourcePackRepository().getRepositoryEntries();
        if (l == null || l.isEmpty()) return hideDefault.value ? "" : "Default";
        if (which.index == 1 && l.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = l.size() - 1; i >= 0; i--) { if (sb.length() > 0) sb.append(" + "); sb.append(clean(l.get(i).getResourcePackName())); }
            return sb.toString();
        }
        return clean(l.get(l.size() - 1).getResourcePackName());
    }

    @Override protected String sample() { return "My Pack 16x"; }

    @Override protected boolean iconOn() { return showIcon.value; }
    @Override protected int iconPx() { return Math.round(iconSize.value); }
    @Override protected boolean textOn() { return showName.value || !showIcon.value; }

    @Override
    protected void drawIcon(int x, int y, int s) {
        ResourcePackRepository.Entry e = top();
        if (e == null) { IconDraw.letter(x, y, s, "D"); return; }
        e.bindTexturePackIcon(mc.getTextureManager());
        IconDraw.quad(x, y, s);
    }
}

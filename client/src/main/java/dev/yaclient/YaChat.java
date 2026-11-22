package dev.yaclient;

import dev.yaclient.modules.ChatModule;
import dev.yaclient.modules.ChatTweaksModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Chat that can be scaled and drawn without the dark background (see ChatModule). Falls back to vanilla if anything is missing. */
public class YaChat extends GuiNewChat {
    private static Field drawnField, scrollField;
    private static boolean searched, ok;

    public YaChat(Minecraft mc) { super(mc); }

    @Override
    public void printChatMessageWithOptionalDeletion(net.minecraft.util.IChatComponent c, int id) {
        int[] out = {id};
        net.minecraft.util.IChatComponent r = ChatTweaksModule.process(c, this, out);
        if (r == null) return;
        super.printChatMessageWithOptionalDeletion(r, out[0]);
    }

    @Override
    public float getChatScale() { return super.getChatScale() * ChatModule.userScale(); }

    @Override
    public int getChatWidth() { return Math.round(super.getChatWidth() * ChatModule.userScale()); }

    private static void find() {
        searched = true;
        try {
            List<Field> lists = new ArrayList<Field>();
            for (Field f : GuiNewChat.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.getType() == List.class) {
                    Type g = f.getGenericType();
                    if (g instanceof ParameterizedType) {
                        Type[] a = ((ParameterizedType) g).getActualTypeArguments();
                        if (a.length == 1 && a[0] == ChatLine.class) lists.add(f);
                    }
                } else if (f.getType() == int.class) {
                    scrollField = f;
                }
            }
            // declaration order: chatLines (all lines), then drawnChatLines (the wrapped lines that are drawn)
            if (lists.size() == 2 && scrollField != null) {
                drawnField = lists.get(1);
                drawnField.setAccessible(true);
                scrollField.setAccessible(true);
                ok = true;
            }
        } catch (Throwable t) { ok = false; }
    }

    @Override
    public void drawChat(int updateCounter) {
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
        int lift = ChatModule.lift(sr.getScaledHeight()), dx = ChatModule.shiftX(sr.getScaledWidth());
        if (lift == 0 && dx == 0) { draw(updateCounter); return; }
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) dx, (float) -lift, 0f);
        draw(updateCounter);
        GlStateManager.popMatrix();
    }

    @Override
    public net.minecraft.util.IChatComponent getChatComponent(int mouseX, int mouseY) {
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
        return super.getChatComponent(mouseX - ChatModule.shiftX(sr.getScaledWidth()), mouseY + ChatModule.lift(sr.getScaledHeight()));
    }

    @SuppressWarnings("unchecked")
    private void draw(int updateCounter) {
        if (!ChatModule.bgHidden()) { super.drawChat(updateCounter); return; }
        if (!searched) find();
        if (!ok) { super.drawChat(updateCounter); return; }
        Minecraft mc = Minecraft.getMinecraft();
        List<ChatLine> lines;
        int scroll;
        try {
            lines = (List<ChatLine>) drawnField.get(this);
            scroll = scrollField.getInt(this);
        } catch (Throwable t) { super.drawChat(updateCounter); return; }
        if (mc.gameSettings.chatVisibility == EntityPlayer.EnumChatVisibility.HIDDEN) return;
        int max = getLineCount();
        int total = lines.size();
        if (total <= 0) return;
        boolean open = getChatOpen();
        float opacity = mc.gameSettings.chatOpacity * 0.9f + 0.1f;
        float f1 = getChatScale();
        GlStateManager.pushMatrix();
        GlStateManager.translate(2.0f, 20.0f, 0.0f);
        GlStateManager.scale(f1, f1, 1.0f);
        for (int i = 0; i + scroll < total && i < max; ++i) {
            ChatLine line = lines.get(i + scroll);
            if (line == null) continue;
            int age = updateCounter - line.getUpdatedCounter();
            if (age >= 200 && !open) continue;
            double d = (double) age / 200.0;
            d = 1.0 - d;
            d = MathHelper.clamp_double(d * 10.0, 0.0, 1.0);
            d = d * d;
            int alpha = open ? 255 : (int) (255.0 * d);
            alpha = (int) ((float) alpha * opacity);
            if (alpha <= 3) continue;
            String s = line.getChatComponent().getFormattedText();
            GlStateManager.enableBlend();
            mc.fontRendererObj.drawStringWithShadow(s, 0.0f, (float) (-i * 9 - 8), 16777215 + (alpha << 24));
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }
        GlStateManager.popMatrix();
    }
}

package dev.yaclient.modules;

import dev.yaclient.Hud;
import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 1.7 animations: the sword swings while you block (block-hit) and sits a little lower, like in 1.7.
 * The first-person item renderer is swapped for a subclass (found by field type, so it works with any mapping).
 */
public class OldAnimationsModule extends Module {
    static final Setting.Bool BLOCKHIT = new Setting.Bool("blockhit", true);
    static final Setting.Bool LOW = new Setting.Bool("low_sword", true);
    static final Setting.Num SWORD_Y = new Setting.Num("sword_height", -20f, 20f, 1f, 0f, "");
    static final Setting.Num SWORD_X = new Setting.Num("sword_side", -20f, 20f, 1f, 0f, "");
    static volatile boolean ACTIVE;

    private boolean tried;

    public OldAnimationsModule() {
        super("old_animations", Category.PVP, false);
        add(BLOCKHIT);
        add(LOW);
        add(SWORD_Y);
        add(SWORD_X);
    }

    @Override
    public void onAlwaysTick() {
        ACTIVE = enabled;
        if (tried || !enabled || mc == null || mc.entityRenderer == null) return;
        tried = true;
        try {
            EntityRenderer er = mc.entityRenderer;
            Field slot = null;
            for (Field f : EntityRenderer.class.getDeclaredFields()) {
                if (f.getType() == ItemRenderer.class && !Modifier.isStatic(f.getModifiers())) { slot = f; break; }
            }
            if (slot == null) return;
            slot.setAccessible(true);
            ItemRenderer old = (ItemRenderer) slot.get(er);
            if (old instanceof OldItemRenderer) return;
            OldItemRenderer neu = new OldItemRenderer(mc);
            for (Field f : ItemRenderer.class.getDeclaredFields()) {     // keep the equipped item and its progress
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(neu, f.get(old));
            }
            slot.set(er, neu);
        } catch (Throwable ignored) { }
    }

    /**
     * 1.7: with the sword up you can keep swinging while you hit the ground or the air (hold left click, right click held).
     * 1.8 only swings on the first click there. Swinging is rate limited by the game itself.
     */
    private boolean attackWas;

    @Override
    public void onTick() {
        if (mc == null || mc.thePlayer == null || mc.currentScreen != null) { attackWas = false; return; }
        EntityPlayerSP p = mc.thePlayer;
        ItemStack held = p.getHeldItem();
        // 1.7: left click swings the arm while you eat an apple or drink a potion (1.8 ignores the click)
        boolean atk = Hud.down(mc.gameSettings.keyBindAttack);
        if (atk && !attackWas && held != null && p.isUsingItem()) {
            EnumAction a = held.getItemUseAction();
            if (a == EnumAction.EAT || a == EnumAction.DRINK) localSwing(p);
        }
        attackWas = atk;
        if (held == null || !(held.getItem() instanceof ItemSword)) return;
        if (!Hud.down(mc.gameSettings.keyBindUseItem) || !Hud.down(mc.gameSettings.keyBindAttack)) return;
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) return;   // hitting a player already swings
        localSwing(p);
    }

    /** Visual arm swing only: nothing is sent to the server (EntityPlayerSP.swingItem would send an animation packet). */
    private static void localSwing(EntityPlayerSP p) {
        if (!p.isSwingInProgress || p.swingProgressInt >= 3 || p.swingProgressInt < 0) { p.swingProgressInt = -1; p.isSwingInProgress = true; }
    }

    private static final class OldItemRenderer extends ItemRenderer {
        // own swing clock: the game's swing progress only moves on ticks, this one moves every frame
        private boolean swingWas;
        private int lastInt;
        private long startNs;

        OldItemRenderer(Minecraft mc) { super(mc); }

        /** Frame-accurate swing progress 0..1 (0 when not swinging). */
        private float smoothSwing(EntityPlayerSP p, float pt) {
            if (!p.isSwingInProgress) { swingWas = false; lastInt = 0; return 0f; }
            long now = System.nanoTime();
            if (!swingWas || p.swingProgressInt < lastInt) startNs = now;
            swingWas = true;
            lastInt = p.swingProgressInt;
            int end = 6;
            if (p.isPotionActive(Potion.digSpeed)) end -= 1 + p.getActivePotionEffect(Potion.digSpeed).getAmplifier();
            else if (p.isPotionActive(Potion.digSlowdown)) end += (1 + p.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2;
            if (end < 2) end = 2;
            float t = (now - startNs) / (end * 50_000_000f);
            return t < 0f ? 0f : (t > 1f ? 1f : t);
        }

        @Override
        public void renderItemInFirstPerson(float pt) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP p = mc.thePlayer;
            ItemStack held = p == null ? null : p.getHeldItem();
            boolean fp = ACTIVE && held != null && mc.gameSettings.thirdPersonView == 0 && mc.currentScreen == null;
            // right click held counts as blocking too (the game stops "using" the sword while you hit a block)
            boolean block = fp && held.getItem() instanceof ItemSword
                    && held.getItemUseAction() == EnumAction.BLOCK
                    && (p.isUsingItem() || Hud.down(mc.gameSettings.keyBindUseItem));
            EnumAction act = fp ? held.getItemUseAction() : EnumAction.NONE;
            boolean eat = fp && p.isUsingItem() && (act == EnumAction.EAT || act == EnumAction.DRINK);
            if (!block && !eat) { super.renderItemInFirstPerson(pt); return; }
            float sw = smoothSwing(p, pt);
            if (eat && sw <= 0f) { super.renderItemInFirstPerson(pt); return; }
            GlStateManager.pushMatrix();
            // everything below happens "inside" the item's own position, exactly where vanilla does its swing
            GlStateManager.translate(0.56f, -0.52f, -0.72f);
            if (block && LOW.value) GlStateManager.translate(0f, -0.06f, 0f);
            if (block || eat) GlStateManager.translate(SWORD_X.value * 0.01f, SWORD_Y.value * 0.01f, 0f);
            if ((eat || BLOCKHIT.value) && sw > 0f) {
                float s1 = MathHelper.sin(sw * sw * (float) Math.PI);
                float s2 = MathHelper.sin(MathHelper.sqrt_float(sw) * (float) Math.PI);
                GlStateManager.translate(-s2 * 0.4f, MathHelper.sin(MathHelper.sqrt_float(sw) * (float) Math.PI * 2f) * 0.2f,
                        -MathHelper.sin(sw * (float) Math.PI) * 0.2f);
                GlStateManager.rotate(45f, 0f, 1f, 0f);
                GlStateManager.rotate(s1 * -20f, 0f, 1f, 0f);
                GlStateManager.rotate(s2 * -20f, 0f, 0f, 1f);
                GlStateManager.rotate(s2 * -80f, 1f, 0f, 0f);
                GlStateManager.rotate(-45f, 0f, 1f, 0f);
            }
            GlStateManager.translate(-0.56f, 0.52f, 0.72f);
            super.renderItemInFirstPerson(pt);
            GlStateManager.popMatrix();
        }
    }
}

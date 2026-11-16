package dev.nexus.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();
    public static KeyBinding openKey, zoomKey;
    static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("nexusclient", "main"));

    public static void init() {
        modules.addAll(Modules.create());
        Config.load();
        for (Module m : modules) {
            Boolean e = Config.enabled.get(m.name);
            if (e != null) m.enabled = e;
            float[] p = Config.hud.get(m.name);
            if (p != null && p.length == 3) { m.x = p[0]; m.y = p[1]; m.scale = p[2]; }
        }
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nexus.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nexus.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(ModuleManager::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(Optimizer::restoreAll);
        HudRenderCallback.EVENT.register((ctx, tc) -> renderHud(ctx));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient()) Modules.onAttack(player, entity);
            return ActionResult.PASS;
        });
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> Modules.onChat(msg.getString()));
    }

    public static void save() {
        for (Module m : modules) {
            Config.enabled.put(m.name, m.enabled);
            Config.hud.put(m.name, new float[]{m.x, m.y, m.scale});
        }
        Config.save();
    }

    static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        while (openKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new ClickGuiScreen());
        }
        Modules.pollClicks(mc);
        for (Module m : modules) if (m.enabled) m.tick(mc);
        Cosmetics.tick(mc);
    }

    static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;
        if (mc.currentScreen instanceof HudEditorScreen) return;
        drawAll(ctx, mc);
    }

    public static void drawAll(DrawContext ctx, MinecraftClient mc) {
        for (Module m : modules) {
            if (!m.enabled) continue;
            if (m.hud) {
                Matrix3x2fStack ms = ctx.getMatrices();
                ms.pushMatrix();
                ms.translate(m.x, m.y);
                ms.scale(m.scale, m.scale);
                m.render(ctx, mc);
                ms.popMatrix();
            } else {
                m.render(ctx, mc);
            }
        }
    }
}

package dev.nexus.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Tüm dahili modüller. */
public class Modules {
    // ---- paylaşılan istatistikler ----
    static final ArrayDeque<Long> leftClicks = new ArrayDeque<>(), rightClicks = new ArrayDeque<>();
    static boolean lastL, lastR;
    static int combo;
    static double lastReach;
    static long lastGG;

    static void pollClicks(MinecraftClient mc) {
        if (mc.currentScreen != null) { lastL = lastR = false; return; }
        long h = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();
        boolean l = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !lastL) leftClicks.add(now);
        if (r && !lastR) rightClicks.add(now);
        lastL = l; lastR = r;
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000) rightClicks.pollFirst();
    }

    static void onAttack(PlayerEntity p, Entity e) {
        combo++;
        Vec3d eye = p.getEyePos();
        Box b = e.getBoundingBox();
        Vec3d c = new Vec3d(MathHelper.clamp(eye.x, b.minX, b.maxX),
                            MathHelper.clamp(eye.y, b.minY, b.maxY),
                            MathHelper.clamp(eye.z, b.minZ, b.maxZ));
        lastReach = eye.distanceTo(c);
    }

    static void onChat(String s) {
        Module m = find("AutoGG");
        if (m == null || !m.enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        String l = s.toLowerCase();
        if ((l.contains("winner") || l.contains("victory") || l.contains("won the game")) &&
            System.currentTimeMillis() - lastGG > 10000 && mc.player != null) {
            lastGG = System.currentTimeMillis();
            mc.player.networkHandler.sendChatMessage("gg");
        }
    }

    static Module find(String n) {
        for (Module m : ModuleManager.modules) if (m.name.equals(n)) return m;
        return null;
    }

    // ---- taban sınıflar ----
    static abstract class Text extends Module {
        Text(String n) { super(n, "HUD", true); }
        abstract String text(MinecraftClient mc);
        @Override public void render(DrawContext c, MinecraftClient mc) {
            String s = text(mc);
            Theme t = Theme.cur();
            w = mc.textRenderer.getWidth(s) + 10; h = 14;
            c.fill(0, 0, w, h, t.bg);
            c.fill(0, 0, 2, h, t.accent);
            c.drawText(mc.textRenderer, s, 6, 3, t.text, true);
        }
    }

    static abstract class Util extends Module {
        Util(String n, String cat) { super(n, cat, false); }
    }

    // ---- modül listesi ----
    static List<Module> create() {
        List<Module> l = new ArrayList<>();

        l.add(new Text("FPS") { String text(MinecraftClient mc) { return mc.getCurrentFps() + " FPS"; } });

        l.add(new Text("CPS") { String text(MinecraftClient mc) {
            return leftClicks.size() + " | " + rightClicks.size() + " CPS"; } });

        l.add(new Text("Ping") { String text(MinecraftClient mc) {
            if (mc.getNetworkHandler() == null || mc.player == null) return "- ms";
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return (e == null ? 0 : e.getLatency()) + " ms"; } });

        l.add(new Text("Koordinat") { String text(MinecraftClient mc) {
            String biome = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                .map(k -> k.getValue().getPath()).orElse("?");
            return String.format("XYZ %d %d %d  (%s)", (int) Math.floor(mc.player.getX()),
                (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()), biome); } });

        l.add(new Text("Pusula") { String text(MinecraftClient mc) {
            String[] n = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
            float yaw = ((mc.player.getYaw() % 360) + 360) % 360;
            int i = ((int) Math.floor(yaw / 45.0 + 0.5)) % 8;
            return "Yön: " + n[i] + " (" + (int) yaw + ")"; } });

        l.add(new Text("Saat") { String text(MinecraftClient mc) {
            return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); } });

        l.add(new Text("Sunucu IP") { String text(MinecraftClient mc) {
            return mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Tek oyunculu"; } });

        l.add(new Text("Combo") { String text(MinecraftClient mc) { return "Combo: " + combo; }
            int prev;
            @Override public void tick(MinecraftClient mc) {
                int ht = mc.player.hurtTime;
                if (ht > 0 && prev == 0) combo = 0;
                prev = ht;
            } });

        l.add(new Text("Reach") { String text(MinecraftClient mc) {
            return String.format("Reach: %.2f", lastReach); } });

        l.add(new Text("Potion Sayacı") { String text(MinecraftClient mc) {
            int n = 0;
            var inv = mc.player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack s = inv.getStack(i);
                if (s.isOf(Items.SPLASH_POTION)) n += s.getCount();
            }
            return "Potion: " + n; } });

        l.add(new Text("TNT Timer") { String text(MinecraftClient mc) {
            int min = Integer.MAX_VALUE;
            for (Entity e : mc.world.getEntities())
                if (e instanceof TntEntity t) min = Math.min(min, t.getFuse());
            return min == Integer.MAX_VALUE ? "TNT: -" : String.format("TNT: %.1fs", min / 20.0); } });

        // Keystrokes
        l.add(new Module("Keystrokes", "HUD", true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                var o = mc.options;
                w = 66; h = 64;
                key(c, mc, 22, 0, 20, 20, "W", o.forwardKey.isPressed());
                key(c, mc, 0, 22, 20, 20, "A", o.leftKey.isPressed());
                key(c, mc, 22, 22, 20, 20, "S", o.backKey.isPressed());
                key(c, mc, 44, 22, 20, 20, "D", o.rightKey.isPressed());
                key(c, mc, 0, 44, 31, 20, "LMB", lastL);
                key(c, mc, 33, 44, 31, 20, "RMB", lastR);
            }
            void key(DrawContext c, MinecraftClient mc, int x, int y, int kw, int kh, String s, boolean on) {
                Theme t = Theme.cur();
                c.fill(x, y, x + kw, y + kh, on ? t.accent : t.bg);
                int tw = mc.textRenderer.getWidth(s);
                c.drawText(mc.textRenderer, s, x + (kw - tw) / 2, y + (kh - 8) / 2, on ? 0xFF000000 : t.text, false);
            }
        });

        // Zırh durumu
        l.add(new Module("Zırh Durumu", "HUD", true) {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                Theme t = Theme.cur();
                w = 56; h = 72;
                for (int i = 0; i < 4; i++) {
                    ItemStack s = mc.player.getEquippedStack(slots[i]);
                    c.fill(0, i * 18, w, i * 18 + 17, t.bg);
                    if (s.isEmpty()) continue;
                    c.drawItem(s, 0, i * 18);
                    if (s.isDamageable()) {
                        int left = s.getMaxDamage() - s.getDamage();
                        c.drawText(mc.textRenderer, String.valueOf(left), 20, i * 18 + 5, t.text, true);
                    }
                }
            }
        });

        // Nişangah
        l.add(new Util("Crosshair", "PvP") {
            @Override public void render(DrawContext c, MinecraftClient mc) {
                int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
                int col = Theme.cur().accent;
                c.fill(cx - 6, cy - 1, cx - 2, cy + 1, col);
                c.fill(cx + 2, cy - 1, cx + 6, cy + 1, col);
                c.fill(cx - 1, cy - 6, cx + 1, cy - 2, col);
                c.fill(cx - 1, cy + 2, cx + 1, cy + 6, col);
            }
        });

        // Fullbright
        l.add(new Util("Fullbright", "Dünya") {
            @Override public void tick(MinecraftClient mc) {
                if (mc.player.age % 40 == 0 || !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
                    mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
            }
            @Override public void onDisable(MinecraftClient mc) {
                if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        });

        // Zoom (C tuşu basılı tut)
        l.add(new Util("Zoom", "Dünya") {
            boolean zoomed; int old = 70;
            @Override public void tick(MinecraftClient mc) {
                boolean down = ModuleManager.zoomKey.isPressed() && mc.currentScreen == null;
                if (down && !zoomed) { old = mc.options.getFov().getValue(); zoomed = true; }
                if (down) mc.options.getFov().setValue(30);
                if (!down && zoomed) { mc.options.getFov().setValue(old); zoomed = false; }
            }
            @Override public void onDisable(MinecraftClient mc) {
                if (zoomed) { mc.options.getFov().setValue(old); zoomed = false; }
            }
        });

        l.add(new Util("Weather Changer", "Dünya") {
            @Override public void tick(MinecraftClient mc) {
                mc.world.setRainGradient(0f);
                mc.world.setThunderGradient(0f);
            }
        });

        l.add(new Util("Toggle Sprint", "Dünya") {
            @Override public void tick(MinecraftClient mc) {
                if (mc.options.forwardKey.isPressed() && !mc.player.isSneaking() && mc.currentScreen == null)
                    mc.options.sprintKey.setPressed(true);
            }
        });

        l.add(new Util("AutoGG", "Dünya") {});

        l.add(new Util("Projectile Trail", "PvP") {
            @Override public void tick(MinecraftClient mc) {
                for (Entity e : mc.world.getEntities()) {
                    boolean proj = (e instanceof PersistentProjectileEntity) || (e instanceof SnowballEntity);
                    if (proj && e.getVelocity().lengthSquared() > 0.01)
                        mc.world.addParticle(ParticleTypes.CRIT, e.getX(), e.getY(), e.getZ(), 0, 0, 0);
                }
            }
        });

        l.add(new Optimizer());

        // varsayılan konumlar + açık gelenler
        int y = 4;
        for (Module m : l) {
            if (m.hud && !(m.name.equals("Keystrokes") || m.name.equals("Zırh Durumu"))) { m.x = 4; m.y = y; y += 18; }
        }
        for (Module m : l) {
            if (m.name.equals("Keystrokes")) { m.x = 4; m.y = 190; }
            if (m.name.equals("Zırh Durumu")) { m.x = 80; m.y = 190; }
            if (m.name.equals("FPS") || m.name.equals("CPS") || m.name.equals("Koordinat")
                || m.name.equals("Keystrokes") || m.name.equals("Zırh Durumu") || m.name.equals("Optimizer")) m.enabled = true;
        }
        return l;
    }
}

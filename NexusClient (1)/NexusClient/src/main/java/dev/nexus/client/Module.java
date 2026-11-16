package dev.nexus.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Module {
    public final String name, category;
    public final boolean hud;
    public boolean enabled;
    public float x = 4, y = 4, scale = 1f;
    public int w = 80, h = 14;

    protected Module(String name, String category, boolean hud) {
        this.name = name; this.category = category; this.hud = hud;
    }

    public void tick(MinecraftClient mc) {}
    public void render(DrawContext c, MinecraftClient mc) {}
    public void onEnable(MinecraftClient mc) {}
    public void onDisable(MinecraftClient mc) {}

    public void setEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (v) onEnable(mc); else onDisable(mc);
    }

    public void toggle() { setEnabled(!enabled); }

    public boolean hit(double mx, double my) {
        return mx >= x && my >= y && mx <= x + w * scale && my <= y + h * scale;
    }
}

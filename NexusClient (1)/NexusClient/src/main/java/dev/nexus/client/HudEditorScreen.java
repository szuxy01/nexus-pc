package dev.nexus.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class HudEditorScreen extends Screen {
    Module drag;
    double ox, oy;

    public HudEditorScreen() { super(Text.literal("HUD Düzenleyici")); }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fill(0, 0, width, height, 0x80000000);
        ModuleManager.drawAll(c, client);
        for (Module m : ModuleManager.modules) {
            if (!m.enabled || !m.hud) continue;
            int col = m.hit(mx, my) ? Theme.cur().accent : 0x80FFFFFF;
            c.drawBorder((int) m.x - 1, (int) m.y - 1, (int) (m.w * m.scale) + 2, (int) (m.h * m.scale) + 2, col);
        }
        c.drawText(textRenderer, "Sürükle: taşı  |  Tekerlek: boyut  |  ESC: kaydet ve çık", 6, height - 12, 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        List<Module> ms = ModuleManager.modules;
        for (int i = ms.size() - 1; i >= 0; i--) {
            Module m = ms.get(i);
            if (m.enabled && m.hud && m.hit(mx, my)) { drag = m; ox = mx - m.x; oy = my - m.y; return true; }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        double mx = click.x(), my = click.y();
        if (drag != null) {
            drag.x = (float) MathHelper.clamp(mx - ox, 0, width - 10);
            drag.y = (float) MathHelper.clamp(my - oy, 0, height - 10);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        drag = null;
        ModuleManager.save();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        for (Module m : ModuleManager.modules) {
            if (m.enabled && m.hud && m.hit(mx, my)) {
                m.scale = MathHelper.clamp(m.scale + (float) Math.signum(vAmt) * 0.1f, 0.5f, 3f);
                ModuleManager.save();
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hAmt, vAmt);
    }

    @Override public void close() { ModuleManager.save(); super.close(); }
    @Override public boolean shouldPause() { return false; }
}

package dev.nexus.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    record Hit(int x, int y, int w, int h, Runnable r) {}

    final List<Hit> hits = new ArrayList<>();
    int tab = 0, cat = 0, page = 0;

    public ClickGuiScreen() { super(Text.literal("Nexus Client")); }

    void btn(DrawContext c, int x, int y, int w, int h, String s, boolean on, Runnable r) {
        Theme t = Theme.cur();
        c.fill(x, y, x + w, y + h, on ? t.accent : t.bg);
        String lbl = textRenderer.trimToWidth(s, w - 8);
        c.drawText(textRenderer, lbl, x + 4, y + (h - 8) / 2, on ? 0xFF000000 : t.text, false);
        hits.add(new Hit(x, y, w, h, r));
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        hits.clear();
        Theme t = Theme.cur();
        int pw = 400, ph = 232, x0 = width / 2 - pw / 2, y0 = height / 2 - ph / 2;
        c.fill(0, 0, width, height, 0x60000000);
        c.fill(x0, y0, x0 + pw, y0 + ph, t.panel);
        c.fill(x0, y0, x0 + pw, y0 + 2, t.accent);

        String[] tabs = {"Modüller", "Temalar", "Kozmetik", "Performans"};
        for (int i = 0; i < tabs.length; i++) {
            final int ii = i;
            btn(c, x0 + 6 + i * 68, y0 + 8, 66, 16, tabs[i], tab == i, () -> { tab = ii; page = 0; });
        }
        btn(c, x0 + pw - 106, y0 + 8, 100, 16, "HUD Düzenleyici", false,
            () -> client.setScreen(new HudEditorScreen()));

        if (tab == 0) {
            List<Module> ms = ModuleManager.modules;
            for (int i = 0; i < ms.size(); i++) {
                Module m = ms.get(i);
                btn(c, x0 + 8 + (i % 3) * 128, y0 + 34 + (i / 3) * 19, 124, 16, m.name, m.enabled, m::toggle);
            }
        } else if (tab == 1) {
            for (int i = 0; i < Theme.ALL.length; i++) {
                final int ii = i;
                btn(c, x0 + 8 + (i % 3) * 128, y0 + 34 + (i / 3) * 19, 124, 16,
                    (i + 1) + ". " + Theme.ALL[i].name, Config.theme == i, () -> Config.theme = ii);
            }
        } else if (tab == 3) {
            Module opt = Modules.find("Optimizer");
            btn(c, x0 + 8, y0 + 34, 250, 16, "Optimizer: " + (opt.enabled ? "AÇIK" : "KAPALI"), opt.enabled, opt::toggle);
            for (int i = 0; i < Optimizer.KEYS.length; i++) {
                final int ii = i;
                btn(c, x0 + 8 + (i % 2) * 192, y0 + 58 + (i / 2) * 19, 188, 16,
                    Optimizer.LABELS[i], Optimizer.on(i), () -> Optimizer.set(ii, !Optimizer.on(ii)));
            }
            int ty = y0 + 58 + 4 * 19 + 8;
            c.drawText(textRenderer, "Hedef FPS: " + Config.targetFps, x0 + 8, ty + 4, t.text, false);
            btn(c, x0 + 100, ty, 24, 16, "-", false, () -> { Config.targetFps = Math.max(30, Config.targetFps - 10); });
            btn(c, x0 + 128, ty, 24, 16, "+", false, () -> { Config.targetFps = Math.min(360, Config.targetFps + 10); });
            c.drawText(textRenderer, "FPS hedefin altına düşerse render mesafesi 6'ya kadar kademeli azalır,", x0 + 8, ty + 26, t.text, false);
            c.drawText(textRenderer, "yükselince orijinal değerine döner. Kapatınca tüm ayarlar geri yüklenir.", x0 + 8, ty + 38, t.text, false);
        } else {
            for (int i = 0; i < Cosmetics.CATS.length; i++) {
                final int ii = i;
                btn(c, x0 + 8 + i * 77, y0 + 30, 74, 16, Cosmetics.CATS[i], cat == i, () -> { cat = ii; page = 0; });
            }
            String key = Cosmetics.CATS[cat];
            List<Cosmetics.Item> items = Cosmetics.ALL.get(key);
            int per = 32, start = page * per;
            for (int k = 0; k < per && start + k < items.size(); k++) {
                final int idx = start + k;
                Cosmetics.Item it = items.get(idx);
                int bx = x0 + 8 + (k % 4) * 97, by = y0 + 52 + (k / 4) * 18;
                boolean sel = Cosmetics.selected(key) == idx;
                btn(c, bx + 10, by, 85, 16, it.name(), sel, () -> {
                    if (Cosmetics.selected(key) == idx) Config.cosmetics.remove(key);
                    else Config.cosmetics.put(key, idx);
                });
                c.fill(bx, by + 2, bx + 8, by + 14, 0xFF000000 | it.color());
            }
            int pages = (items.size() + per - 1) / per;
            btn(c, x0 + 8, y0 + 208, 24, 16, "<", false, () -> page = Math.max(0, page - 1));
            btn(c, x0 + 36, y0 + 208, 24, 16, ">", false, () -> page = Math.min(pages - 1, page + 1));
            c.drawText(textRenderer, (page + 1) + "/" + pages + "  (" + items.size() + " adet)", x0 + 66, y0 + 212, t.text, false);
            if (cat != 4) c.drawText(textRenderer, "3D model: faz 2", x0 + 300, y0 + 212, 0xFFFFAA00, false);
        }
        c.drawText(textRenderer, "Sağ Shift: menü  |  C: zoom", x0 + 8, y0 + ph + 4, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (mx >= h.x && mx <= h.x + h.w && my >= h.y && my <= h.y + h.h) {
                h.r.run();
                ModuleManager.save();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean shouldPause() { return false; }
}

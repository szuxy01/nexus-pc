package dev.nexus.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/**
 * Performans modülü. Mixin gerektirmeyen, oyun ayarlarını FPS lehine çeken ve
 * FPS düşünce render mesafesini otomatik ayarlayan bir optimizer.
 * Kapatınca veya oyundan çıkınca orijinal ayarlar geri yüklenir.
 */
public class Optimizer extends Module {
    public static final String[] KEYS = {"particles", "clouds", "shadows", "biome", "entities", "graphics", "bobbing", "adaptive"};
    public static final String[] LABELS = {
        "Parçacıkları azalt", "Bulutları kapat", "Entity gölgelerini kapat", "Biyom karışımını kapat",
        "Entity mesafesini düşür", "Hızlı grafik modu", "Kamera sallanmasını kapat", "Uyarlanabilir render mesafesi"};
    static final boolean[] DEF = {true, true, true, true, true, true, false, true};

    static Optimizer INSTANCE;

    boolean saved, applied;
    int timer;
    ParticlesMode bParticles;
    CloudRenderMode bClouds;
    boolean bShadows, bBob;
    int bBiome, bView;
    double bEntity;
    GraphicsMode bGraphics;

    public Optimizer() {
        super("Optimizer", "Performans", false);
        INSTANCE = this;
    }

    public static boolean on(int i) {
        return Config.enabled.getOrDefault("opt." + KEYS[i], DEF[i]);
    }

    public static void set(int i, boolean v) {
        Config.enabled.put("opt." + KEYS[i], v);
        refresh();
    }

    /** Ayar değişince (veya hedef FPS değişince) yeniden uygula. */
    public static void refresh() {
        if (INSTANCE != null && INSTANCE.enabled && INSTANCE.saved)
            INSTANCE.apply(MinecraftClient.getInstance());
    }

    public static void restoreAll(MinecraftClient mc) {
        if (INSTANCE != null) INSTANCE.restore(mc);
    }

    void apply(MinecraftClient mc) {
        GameOptions o = mc.options;
        if (!saved) {
            bParticles = o.getParticles().getValue();
            bClouds = o.getCloudRenderMode().getValue();
            bShadows = o.getEntityShadows().getValue();
            bBiome = o.getBiomeBlendRadius().getValue();
            bEntity = o.getEntityDistanceScaling().getValue();
            bGraphics = o.getGraphicsMode().getValue();
            bBob = o.getBobView().getValue();
            bView = o.getViewDistance().getValue();
            saved = true;
        }
        o.getParticles().setValue(on(0) ? ParticlesMode.DECREASED : bParticles);
        o.getCloudRenderMode().setValue(on(1) ? CloudRenderMode.OFF : bClouds);
        o.getEntityShadows().setValue(on(2) ? Boolean.FALSE : Boolean.valueOf(bShadows));
        o.getBiomeBlendRadius().setValue(on(3) ? 0 : bBiome);
        o.getEntityDistanceScaling().setValue(on(4) ? 0.75 : bEntity);
        o.getGraphicsMode().setValue(on(5) ? GraphicsMode.FAST : bGraphics);
        o.getBobView().setValue(on(6) ? Boolean.FALSE : Boolean.valueOf(bBob));
        if (!on(7)) o.getViewDistance().setValue(bView);
        applied = true;
    }

    void restore(MinecraftClient mc) {
        if (!saved) return;
        GameOptions o = mc.options;
        o.getParticles().setValue(bParticles);
        o.getCloudRenderMode().setValue(bClouds);
        o.getEntityShadows().setValue(bShadows);
        o.getBiomeBlendRadius().setValue(bBiome);
        o.getEntityDistanceScaling().setValue(bEntity);
        o.getGraphicsMode().setValue(bGraphics);
        o.getBobView().setValue(bBob);
        o.getViewDistance().setValue(bView);
        saved = false;
        applied = false;
    }

    @Override public void onDisable(MinecraftClient mc) { restore(mc); }

    @Override
    public void tick(MinecraftClient mc) {
        if (!applied) apply(mc);
        if (!on(7)) return;
        if (++timer < 200) return;      // ~10 saniyede bir
        timer = 0;
        int fps = mc.getCurrentFps();
        int vd = mc.options.getViewDistance().getValue();
        if (fps < Config.targetFps && vd > 6) mc.options.getViewDistance().setValue(vd - 1);
        else if (fps > Config.targetFps * 1.4 && vd < bView) mc.options.getViewDistance().setValue(vd + 1);
    }
}

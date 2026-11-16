package dev.nexus.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.DustParticleEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 300 kozmetik kaydı (60 kanat, 80 pelerin, 60 şapka, 50 pet, 50 iz/aura). */
public class Cosmetics {
    public record Item(String cat, String name, int color) {}

    public static final String[] CATS = {"Kanatlar", "Pelerinler", "Şapkalar", "Evcil Hayvanlar", "İz & Aura"};
    public static final Map<String, List<Item>> ALL = new LinkedHashMap<>();

    static final String[] CN = {"Kırmızı", "Mavi", "Yeşil", "Sarı", "Mor", "Turuncu", "Pembe", "Camgöbeği", "Beyaz", "Siyah"};
    static final int[] CC = {0xE53935, 0x1E88E5, 0x43A047, 0xFDD835, 0x8E24AA, 0xFB8C00, 0xEC407A, 0x00ACC1, 0xFAFAFA, 0x212121};

    static void gen(String cat, String[] types, int colors) {
        List<Item> l = new ArrayList<>();
        for (String t : types) for (int i = 0; i < colors; i++) l.add(new Item(cat, t + " " + CN[i], CC[i]));
        ALL.put(cat, l);
    }

    static {
        gen(CATS[0], new String[]{"Ejderha", "Melek", "Şeytan", "Kelebek", "Meka", "Ateş", "Buz", "Galaksi", "Kristal", "Yarasa"}, 6);
        gen(CATS[1], new String[]{"Düz", "Gradyan", "Animasyonlu", "OptiFine", "Minecon", "Çizgili", "Damalı", "Yıldızlı"}, 10);
        gen(CATS[2], new String[]{"Melek Halesi", "Kral Tacı", "Şeytan Boynuzu", "Kedi Kulağı", "Büyücü Şapkası", "Astronot Kaskı", "Silindir", "Korsan", "Kovboy", "Prenses Tacı"}, 6);
        gen(CATS[3], new String[]{"Mini Ejderha", "Hayalet", "Mini Golem", "Uçan Küre", "Baykuş"}, 10);
        gen(CATS[4], new String[]{"İz", "Aura", "Spiral", "Hale", "Yörünge"}, 10);
    }

    public static int selected(String cat) { return Config.cosmetics.getOrDefault(cat, -1); }

    /** Şu an parçacık tabanlı olarak çalışan kategori: İz & Aura. */
    static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        int idx = selected(CATS[4]);
        if (idx < 0) return;
        int shape = idx / 10;
        int color = CC[idx % 10];
        var fx = new DustParticleEffect(color, 1.0f);
        double t = p.age;
        double x = p.getX(), y = p.getY(), z = p.getZ();
        switch (shape) {
            case 0 -> {
                if (p.getVelocity().horizontalLengthSquared() > 0.003)
                    mc.world.addParticle(fx, x, y + 0.1, z, 0, 0, 0);
            }
            case 1 -> {
                for (int i = 0; i < 3; i++) {
                    double a = Math.random() * Math.PI * 2;
                    mc.world.addParticle(fx, x + Math.cos(a) * 0.7, y + Math.random() * 1.8, z + Math.sin(a) * 0.7, 0, 0, 0);
                }
            }
            case 2 -> {
                double a = t * 0.35, hy = (t % 40) / 20.0;
                mc.world.addParticle(fx, x + Math.cos(a) * 0.6, y + hy, z + Math.sin(a) * 0.6, 0, 0, 0);
            }
            case 3 -> {
                double a = t * 0.5;
                mc.world.addParticle(fx, x + Math.cos(a) * 0.3, y + p.getHeight() + 0.35, z + Math.sin(a) * 0.3, 0, 0, 0);
            }
            case 4 -> {
                for (int i = 0; i < 2; i++) {
                    double a = t * 0.25 + i * Math.PI;
                    mc.world.addParticle(fx, x + Math.cos(a) * 0.8, y + 1.0, z + Math.sin(a) * 0.8, 0, 0, 0);
                }
            }
        }
    }
}

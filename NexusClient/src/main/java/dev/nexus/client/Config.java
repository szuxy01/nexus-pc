package dev.nexus.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static int theme = 0;
    public static int targetFps = 90;
    public static Map<String, Boolean> enabled = new HashMap<>();
    public static Map<String, float[]> hud = new HashMap<>();
    public static Map<String, Integer> cosmetics = new HashMap<>();

    static class Data {
        int theme;
        int targetFps;
        Map<String, Boolean> enabled;
        Map<String, float[]> hud;
        Map<String, Integer> cosmetics;
    }

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("nexusclient.json");
    }

    public static void load() {
        try {
            Path f = file();
            if (!Files.exists(f)) return;
            Data d = GSON.fromJson(Files.readString(f), Data.class);
            if (d == null) return;
            if (d.targetFps >= 30) targetFps = d.targetFps;
            theme = Math.max(0, Math.min(Theme.ALL.length - 1, d.theme));
            if (d.enabled != null) enabled = d.enabled;
            if (d.hud != null) hud = d.hud;
            if (d.cosmetics != null) cosmetics = d.cosmetics;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.theme = theme;
            d.targetFps = targetFps;
            d.enabled = enabled;
            d.hud = hud;
            d.cosmetics = cosmetics;
            Files.writeString(file(), GSON.toJson(d));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package net.creater120.ghostblock.client.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ConfigManager {
    private static ConfigManager instance;
    private final Map<String, Object> config = new HashMap<>();
    private final Path configPath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private ConfigManager() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("ghostblock.json");
        load();
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    public void load() {
        if (configPath.toFile().exists()) {
            try (Reader reader = new FileReader(configPath.toFile())) {
                Map<String, Object> loaded = gson.fromJson(reader, Map.class);
                config.putAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setDefaults();
    }
    
    private void setDefaults() {
        config.putIfAbsent("placeMode", 0);
        config.putIfAbsent("bridgeWidth", 1);
        config.putIfAbsent("opacity", 0.5f);
        config.putIfAbsent("showOutline", true);
        config.putIfAbsent("showParticles", true);
        config.putIfAbsent("maxUndo", 50);
        config.putIfAbsent("requireServerOptIn", false);
        config.putIfAbsent("trustedServers", new ArrayList<String>());
    }
    
    public void save() {
        try (Writer writer = new FileWriter(configPath.toFile())) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }
    
    public float getFloat(String key, float defaultValue) {
        Object value = config.get(key);
        return value instanceof Number ? ((Number) value).floatValue() : defaultValue;
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        return value instanceof Boolean ? (boolean) value : defaultValue;
    }
    
    public void set(String key, Object value) {
        config.put(key, value);
    }
}
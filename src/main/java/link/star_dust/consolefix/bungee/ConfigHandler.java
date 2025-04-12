package link.star_dust.consolefix.bungee;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    private final BungeeCSF plugin;
    private Configuration config;

    public ConfigHandler(BungeeCSF plugin) {
        this.plugin = plugin;
    }

    public boolean loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().info("No config file found! Copying default config from JAR...");
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResourceAsStream("config-bungee.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to copy default config file!");
                e.printStackTrace();
                return false;
            }
        }

        try {
            plugin.getLogger().info("Loading the config file...");
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            plugin.getLogger().info("Config file loaded successfully!");
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load config file!");
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getStringList(String key) {
        if (!config.contains(key)) {
            throw new RuntimeException("Missing required key in config.yml: " + key);
        }
        return config.getStringList(key);
    }

    public String getString(String key) {
        if (!config.contains(key)) {
            throw new RuntimeException("Missing required key in config.yml: " + key);
        }
        return config.getString(key);
    }
}
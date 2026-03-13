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
        } else {
            // 检查配置文件版本
            checkConfigVersion(configFile);
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

    private void checkConfigVersion(File configFile) {
        try {
            // 加载现有配置文件
            Configuration existingConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            
            // 获取现有配置文件的版本
            int existingVersion = existingConfig.getInt("_config-version", 0);
            
            // 从 JAR 中读取默认配置文件
            try (InputStream inputStream = plugin.getResourceAsStream("config-bungee.yml")) {
                if (inputStream == null) {
                    plugin.getLogger().warning("Default config file 'config-bungee.yml' is missing from the JAR!");
                    return;
                }
                
                // 读取默认配置文件的版本
                Configuration defaultConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(inputStream);
                int defaultVersion = defaultConfig.getInt("_config-version", 0);
                
                // 如果现有版本低于默认版本，备份并更新
                if (existingVersion < defaultVersion) {
                    plugin.getLogger().info("Config version outdated (current: " + existingVersion + ", latest: " + defaultVersion + "). Backing up and updating...");
                    
                    // 备份当前配置文件
                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    File backupFile = new File(configFile.getParentFile(), "config-" + timestamp + ".yml.bak");
                    
                    try {
                        Files.copy(configFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        plugin.getLogger().info("Backup created: " + backupFile.getName());
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to create backup: " + e.getMessage());
                        return;
                    }
                    
                    // 删除旧配置文件
                    if (!configFile.delete()) {
                        plugin.getLogger().warning("Failed to delete old config file!");
                        return;
                    }
                    
                    // 复制新的默认配置文件
                    plugin.getDataFolder().mkdirs();
                    try (InputStream in = plugin.getResourceAsStream("config-bungee.yml")) {
                        Files.copy(in, configFile.toPath());
                        plugin.getLogger().info("Config file updated to version " + defaultVersion);
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to copy default config file!");
                        e.printStackTrace();
                    }
                } else if (existingVersion > defaultVersion) {
                    plugin.getLogger().warning("Config version (" + existingVersion + ") is higher than default version (" + defaultVersion + "). This may cause compatibility issues.");
                } else {
                    plugin.getLogger().info("Config version is up to date (" + existingVersion + ")");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check config version: " + e.getMessage());
            e.printStackTrace();
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
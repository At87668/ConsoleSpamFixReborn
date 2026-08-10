package link.star_dust.consolefix.bukkit;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

/**
 * Bukkit config handler backed by the Spigot {@code YamlConfiguration}.
 */
public class ConfigHandler {
    private CSF csf;

    public ConfigHandler(CSF csf) {
        this.csf = csf;
        this.loadConfig();
    }

    public boolean loadConfig() {
        File configFile;
        File pluginFolder = new File("plugins" + System.getProperty("file.separator") + CSF.pluginName);
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        configFile = new File("plugins" + System.getProperty("file.separator") + CSF.pluginName + System.getProperty("file.separator") + "config.yml");

        if (!configFile.exists()) {
            CSF.log.info("No config file found! Creating new one...");
            this.csf.saveDefaultConfig();
        } else {
            // Check the config file version and migrate if needed.
            checkConfigVersion(configFile);
        }

        try {
            CSF.log.info("Loading the config file...");
            this.csf.getConfig().load(configFile);
            CSF.log.info("Config file loaded!");
            return true;
        }
        catch (Exception e) {
            CSF.log.info("Could not load config file! You need to regenerate the config! Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void checkConfigVersion(File configFile) {
        try {
            // Load the existing config file.
            org.bukkit.configuration.file.YamlConfiguration existingConfig = new org.bukkit.configuration.file.YamlConfiguration();
            existingConfig.load(configFile);

            // Read the version of the existing config.
            int existingVersion = existingConfig.getInt("_config-version", 0);

            // Read the default config file from the JAR.
            try (InputStream inputStream = csf.getResource("config.yml")) {
                if (inputStream == null) {
                    CSF.log.warning("Default config file 'config.yml' is missing from the JAR!");
                    return;
                }

                // Read the version of the default config.
                org.bukkit.configuration.file.YamlConfiguration defaultConfig = new org.bukkit.configuration.file.YamlConfiguration();
                defaultConfig.loadFromString(new java.util.Scanner(inputStream).useDelimiter("\\A").next());
                int defaultVersion = defaultConfig.getInt("_config-version", 0);

                // If the existing version is older, back up and update.
                if (existingVersion < defaultVersion) {
                    CSF.log.info("Config version outdated (current: " + existingVersion + ", latest: " + defaultVersion + "). Backing up and updating...");

                    // Back up the current config file.
                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    File backupFile = new File(configFile.getParentFile(), "config-" + timestamp + ".yml.bak");

                    try {
                        java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        CSF.log.info("Backup created: " + backupFile.getName());
                    } catch (IOException e) {
                        CSF.log.warning("Failed to create backup: " + e.getMessage());
                        return;
                    }

                    // Delete the old config file.
                    if (!configFile.delete()) {
                        CSF.log.warning("Failed to delete old config file!");
                        return;
                    }

                    // Save the new default config file.
                    csf.saveDefaultConfig();
                    CSF.log.info("Config file updated to version " + defaultVersion);
                } else if (existingVersion > defaultVersion) {
                    CSF.log.warning("Config version (" + existingVersion + ") is higher than default version (" + defaultVersion + "). This may cause compatibility issues.");
                } else {
                    CSF.log.info("Config version is up to date (" + existingVersion + ")");
                }
            }
        } catch (Exception e) {
            CSF.log.warning("Failed to check config version: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getStringList(String key) {
        if (!this.csf.getConfig().contains(key)) {
            this.csf.getLogger().severe("Could not locate '" + key + "' in the config.yml inside of the " + CSF.pluginName + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return this.csf.getConfig().getStringList(key);
    }

    public String getString(String key) {
        if (!this.csf.getConfig().contains(key)) {
            this.csf.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CSF.pluginName + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key;
        }
        return this.csf.getConfig().getString(key);
    }

    public String getStringWithColor(String key) {
        if (!this.csf.getConfig().contains(key)) {
            this.csf.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CSF.pluginName + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key;
        }
        return this.csf.getConfig().getString(key).replaceAll("&", "§");
    }
}

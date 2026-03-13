package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;

public class ConfigHandler {
    private final Logger logger;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private CommentedConfigurationNode configNode;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public ConfigHandler(VelocityCSF velocityCSF) {
        this.logger = velocityCSF.getLogger();
        this.dataDirectory = velocityCSF.getDataDirectory();
        this.pluginContainer = velocityCSF.getPluginContainer();
    }

    public boolean loadConfig() {
        File pluginFolder = dataDirectory.toFile();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        File configFile = new File(dataDirectory.toFile(), "config.yml");
        loader = YamlConfigurationLoader.builder()
                .path(configFile.toPath())
                .build();

        if (!configFile.exists()) {
            logger.info("No config file found! Copying default config from JAR...");
            copyDefaultConfigFromJar(configFile);
        } else {
            // 检查配置文件版本
            checkConfigVersion(configFile);
        }

        try {
            logger.info("Loading the config file...");
            configNode = loader.load();
            logger.info("Config file loaded successfully!");
            return true;
        } catch (ConfigurateException e) {
            logger.error("Could not load config file! Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void copyDefaultConfigFromJar(File configFile) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config-velocity.yml")) {
            if (inputStream == null) {
                logger.error("Default config file 'config-velocity.yml' is missing from the JAR!");
                return;
            }

            try (OutputStream outputStream = new FileOutputStream(configFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            logger.info("Default config file has been copied successfully.");
        } catch (IOException e) {
            logger.error("Failed to copy default config file! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkConfigVersion(File configFile) {
        try {
            // 加载现有配置文件
            ConfigurationLoader<CommentedConfigurationNode> existingLoader = YamlConfigurationLoader.builder()
                    .path(configFile.toPath())
                    .build();
            CommentedConfigurationNode existingConfig = existingLoader.load();
            
            // 获取现有配置文件的版本
            int existingVersion = existingConfig.node("_config-version").getInt(0);
            
            // 从 JAR 中读取默认配置文件
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config-velocity.yml")) {
                if (inputStream == null) {
                    logger.error("Default config file 'config-velocity.yml' is missing from the JAR!");
                    return;
                }
                
                // 读取默认配置文件的版本
                ConfigurationLoader<CommentedConfigurationNode> defaultLoader = YamlConfigurationLoader.builder()
                        .source(() -> new BufferedReader(new InputStreamReader(inputStream)))
                        .build();
                CommentedConfigurationNode defaultConfig = defaultLoader.load();
                int defaultVersion = defaultConfig.node("_config-version").getInt(0);
                
                // 如果现有版本低于默认版本，备份并更新
                if (existingVersion < defaultVersion) {
                    logger.info("Config version outdated (current: " + existingVersion + ", latest: " + defaultVersion + "). Backing up and updating...");
                    
                    // 备份当前配置文件
                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    File backupFile = new File(configFile.getParentFile(), "config-" + timestamp + ".yml.bak");
                    
                    try {
                        java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        logger.info("Backup created: " + backupFile.getName());
                    } catch (IOException e) {
                        logger.error("Failed to create backup: " + e.getMessage());
                        return;
                    }
                    
                    // 删除旧配置文件
                    if (!configFile.delete()) {
                        logger.error("Failed to delete old config file!");
                        return;
                    }
                    
                    // 复制新的默认配置文件
                    copyDefaultConfigFromJar(configFile);
                    logger.info("Config file updated to version " + defaultVersion);
                } else if (existingVersion > defaultVersion) {
                    logger.warn("Config version (" + existingVersion + ") is higher than default version (" + defaultVersion + "). This may cause compatibility issues.");
                } else {
                    logger.info("Config version is up to date (" + existingVersion + ")");
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check config version: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getStringList(String key) throws SerializationException {
        if (configNode.node(key).virtual()) {
            throw new RuntimeException("Missing required key in config.yml: " + key);
        }
        return configNode.node(key).getList(String.class);
    }

    public String getString(String key) {
        if (configNode.node(key).virtual()) {
            throw new RuntimeException("Missing required key in config.yml: " + key);
        }
        return configNode.node(key).getString();
    }

    public String getChatMessage(String key) {
        String message = getString("ChatMessages." + key);
        if (message == null) {
            throw new RuntimeException("Missing required chat message in config.yml: ChatMessages." + key);
        }
        return message.replaceAll("&", "§"); // Replace & with § for color codes
    }
}
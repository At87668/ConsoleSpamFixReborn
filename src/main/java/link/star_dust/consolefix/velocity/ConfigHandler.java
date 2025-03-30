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
        this.loadConfig();
    }

    public boolean loadConfig() {
        // Ensure the plugin's data directory exists
        File pluginFolder = dataDirectory.toFile();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        // Define the config file path
        File configFile = new File(dataDirectory.toFile(), "config.yml");

        // Initialize the YAML loader
        loader = YamlConfigurationLoader.builder()
                .path(configFile.toPath())
                .build();

        if (!configFile.exists()) {
            logger.info("No config file found! Copying default config from JAR...");
            copyDefaultConfigFromJar(configFile);
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

            // Copy the default config file to the plugin's data directory
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
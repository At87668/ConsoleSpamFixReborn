package link.star_dust.consolefix.core;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Simple YAML config backed by configurate (bundled into the JAR).
 *
 * <p>Used by the mod platforms (Fabric / Forge / NeoForge) to read the
 * filter lists and chat messages. A default config is copied from the
 * bundled resource on first run.
 */
public class ConfigStore {

    private final Path configFile;
    private final String defaultResource;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode node;

    public ConfigStore(Path configFile, String defaultResource) {
        this.configFile = configFile;
        this.defaultResource = defaultResource;
    }

    /**
     * Load (or create) the config file.
     *
     * @return {@code true} when the config was loaded successfully
     */
    public boolean load() {
        try {
            if (!Files.exists(configFile)) {
                Path parent = configFile.getParent();
                if (parent != null) Files.createDirectories(parent);
                try (InputStream in = getClass().getClassLoader().getResourceAsStream(defaultResource)) {
                    if (in == null) return false;
                    Files.copy(in, configFile);
                }
            }
            loader = YamlConfigurationLoader.builder().path(configFile).build();
            node = loader.load();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Read a string list, returning an empty list when the key is missing.
     *
     * @param path dotted config path, e.g. {@code Messages-To-Hide-Filter.contains}
     */
    public List<String> getStringList(String path) {
        if (node == null) return Collections.emptyList();
        try {
            List<String> list = node.node((Object[]) path.split("\\.")).getList(String.class);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Read a single string, returning {@code null} when the key is missing.
     */
    public String getString(String path) {
        if (node == null) return null;
        try {
            return node.node((Object[]) path.split("\\.")).getString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reload the config from disk.
     */
    public void reload() {
        load();
    }
}

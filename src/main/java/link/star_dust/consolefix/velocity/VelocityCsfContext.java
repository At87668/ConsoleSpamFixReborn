package link.star_dust.consolefix.velocity;

import link.star_dust.consolefix.common.CsfContext;

import java.util.Collections;
import java.util.List;

/**
 * Velocity implementation of the platform-agnostic {@link CsfContext}.
 *
 * <p>The Velocity config handler throws on missing keys, so every read is
 * wrapped and normalised (empty list / {@code null}) to keep the shared
 * core safe.
 */
public class VelocityCsfContext implements CsfContext {

    private final VelocityCSF plugin;
    private final ConfigHandler configHandler;

    public VelocityCsfContext(VelocityCSF plugin, ConfigHandler configHandler) {
        this.plugin = plugin;
        this.configHandler = configHandler;
    }

    @Override
    public List<String> getStringList(String path) {
        try {
            return configHandler.getStringList(path);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String getString(String path) {
        try {
            return configHandler.getString(path);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getStringWithColor(String path) {
        String s = getString(path);
        return s == null ? null : s.replaceAll("&", "§");
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warn(String message) {
        plugin.getLogger().warn(message);
    }

    @Override
    public void error(String message) {
        plugin.getLogger().error(message);
    }

    @Override
    public void reloadConfig() {
        configHandler.loadConfig();
    }

    @Override
    public void logFilteredMessage(String message) {
        configHandler.logFilteredMessage(message);
    }
}

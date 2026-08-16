package link.star_dust.consolefix.bukkit;

import link.star_dust.consolefix.common.CsfContext;

import java.util.Collections;
import java.util.List;

/**
 * Bukkit implementation of the platform-agnostic {@link CsfContext}.
 * Delegates config access to the Bukkit {@link ConfigHandler} and logging
 * to the plugin logger. Missing config keys are normalised to empty lists.
 */
public class BukkitCsfContext implements CsfContext {

    private final CSF plugin;

    public BukkitCsfContext(CSF plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> getStringList(String path) {
        List<String> list = plugin.getConfigHandler().getStringList(path);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    public String getString(String path) {
        return plugin.getConfigHandler().getString(path);
    }

    @Override
    public String getStringWithColor(String path) {
        return plugin.getConfigHandler().getStringWithColor(path);
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warn(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void error(String message) {
        plugin.getLogger().severe(message);
    }

    @Override
    public void reloadConfig() {
        plugin.getConfigHandler().loadConfig();
    }

    @Override
    public void logFilteredMessage(String message) {
        plugin.getConfigHandler().logFilteredMessage(message);
    }
}

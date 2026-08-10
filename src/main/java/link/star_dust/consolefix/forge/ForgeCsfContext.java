package link.star_dust.consolefix.forge;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.core.ConfigStore;

import org.apache.logging.log4j.LogManager;

import java.util.List;

/**
 * Forge implementation of the platform-agnostic {@link CsfContext}.
 * Config comes from the shared {@link ConfigStore}; logging uses log4j
 * (present on every Minecraft server) so no platform logger API is needed.
 */
final class ForgeCsfContext implements CsfContext {

    private final ConfigStore configStore;
    private final org.apache.logging.log4j.Logger logger;

    ForgeCsfContext(ConfigStore configStore) {
        this.configStore = configStore;
        this.logger = LogManager.getLogger("ConsoleSpamFixReborn");
    }

    @Override
    public List<String> getStringList(String path) {
        return configStore.getStringList(path);
    }

    @Override
    public String getString(String path) {
        return configStore.getString(path);
    }

    @Override
    public String getStringWithColor(String path) {
        String s = getString(path);
        return s == null ? null : s.replaceAll("&", "§");
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void reloadConfig() {
        configStore.reload();
    }
}

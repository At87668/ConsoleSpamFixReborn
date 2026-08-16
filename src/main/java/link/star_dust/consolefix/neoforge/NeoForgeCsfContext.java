package link.star_dust.consolefix.neoforge;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.core.ConfigStore;
import link.star_dust.consolefix.core.FilteredLogWriter;

import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * NeoForge implementation of the platform-agnostic {@link CsfContext}.
 * Config comes from the shared {@link ConfigStore}; logging uses log4j
 * (present on every Minecraft server) so no platform logger API is needed.
 */
final class NeoForgeCsfContext implements CsfContext {

    private final ConfigStore configStore;
    private final org.apache.logging.log4j.Logger logger;
    private boolean logFilteredMessages;
    private FilteredLogWriter filteredLogWriter;

    NeoForgeCsfContext(ConfigStore configStore) {
        this.configStore = configStore;
        this.logger = LogManager.getLogger("ConsoleSpamFixReborn");
        initFilteredLog();
    }

    private void initFilteredLog() {
        this.logFilteredMessages = configStore.getBoolean("Log-Filtered-Messages", false);
        if (this.logFilteredMessages && this.filteredLogWriter == null) {
            try {
                this.filteredLogWriter = new FilteredLogWriter(new File("logs"));
                logger.info("Filtered message logging enabled. Logging to logs/yyyy-MM-dd-N-filtered.log");
            } catch (IOException e) {
                logger.error("Failed to initialize filtered log file: " + e.getMessage());
            }
        }
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
        initFilteredLog();
    }

    @Override
    public void logFilteredMessage(String message) {
        if (this.logFilteredMessages && this.filteredLogWriter != null) {
            this.filteredLogWriter.write(message);
        }
    }
}

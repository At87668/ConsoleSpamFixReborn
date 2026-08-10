package link.star_dust.consolefix.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import java.nio.file.Path;

/**
 * BungeeCord entry point. Platform wiring only — the actual log filtering
 * is delegated to the shared {@code core} layer through the
 * {@link CsfContext} / {@link EngineInterface} contracts.
 */
public class BungeeCSF extends Plugin {
    public static final String PLUGIN_NAME = "ConsoleSpamFixReborn";
    private ConfigHandler configHandler;
    private CsfContext csfContext;
    private EngineInterface engine;
    private LogFilterManager logFilterManager;
    private Metrics metrics;

    @Override
    public void onEnable() {
        // Initialise the config handler.
        this.configHandler = new ConfigHandler(this);
        if (!this.configHandler.loadConfig()) {
            getLogger().warning("Failed to load configuration. The plugin may not function correctly.");
        }

        // Initialise the platform-agnostic core.
        this.csfContext = new BungeeCsfContext(this, configHandler);
        this.engine = new NewEngine(csfContext);
        this.logFilterManager = new LogFilterManager(csfContext, engine);

        // Initialise bStats metrics.
        int pluginId = 25292;
        new Metrics(this, pluginId);

        // Register the command.
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeeCommandHandler(configHandler, this));

        // Update the log filter.
        updateLogFilter();

        getLogger().info(PLUGIN_NAME + " " + "v" + getDescription().getVersion() + " loaded successfully!");
    }

    public void updateLogFilter() {
        if (this.engine == null || this.logFilterManager == null) {
            getLogger().warning("Cannot update log filter: Engine or LogFilterManager is not initialized yet!");
            return;
        }
        this.logFilterManager.updateFilter();
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public EngineInterface getEngine() {
        return engine;
    }

    public CsfContext getCsfContext() {
        return csfContext;
    }

    public Path getDataDirectory() {
        return getDataFolder().toPath();
    }
}
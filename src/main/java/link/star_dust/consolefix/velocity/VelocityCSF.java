package link.star_dust.consolefix.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import java.nio.file.Path;

@SuppressWarnings("unused")
@Plugin(id = "consolefixreborn", name = "ConsoleSpamFixReborn", version = "1.0.0", description = "Fixes console spam", authors = {"CraftersLand", "Author87668"})
public class VelocityCSF {
    public static final String PLUGIN_NAME = "ConsoleSpamFixReborn";
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private ConfigHandler configHandler;
    private CsfContext csfContext;
    private EngineInterface engine;
    private LogFilterManager logFilterManager;
    private Metrics metrics;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityCSF(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory, PluginContainer pluginContainer, Metrics.Factory metricsFactory) {
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
        this.metricsFactory = metricsFactory;
    }
    
    public void updateLogFilter() {
        if (this.engine == null || this.logFilterManager == null) {
            logger.error("Cannot update log filter: Engine or LogFilterManager is not initialized yet!");
            return;
        }

        this.logFilterManager.updateFilter();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Initialise the config handler.
        this.configHandler = new ConfigHandler(this);
        if (!this.configHandler.loadConfig()) {
            logger.error("Failed to load configuration. The plugin may not function correctly.");
        }

        // Initialise the platform-agnostic core.
        this.csfContext = new VelocityCsfContext(this, configHandler);
        this.engine = new NewEngine(csfContext);
        this.logFilterManager = new LogFilterManager(csfContext, engine);

        // Initialise bStats metrics.
        int pluginId = 25291;
        metricsFactory.make(this, pluginId);

        // Register the command.
        this.server.getCommandManager().register("csfv", new VelocityCommandHandler(this, this.logFilterManager));

        // Update the log filter.
        updateLogFilter();

        // Log successful initialization.
        logger.info("{} v{} loaded successfully!", PLUGIN_NAME, pluginContainer.getDescription().getVersion().orElse("Unknown"));
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public EngineInterface getEngine() {
        return engine;
    }

    public LogFilterManager getLogFilterManager() {
        return logFilterManager;
    }

    public CsfContext getCsfContext() {
        return csfContext;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }
}
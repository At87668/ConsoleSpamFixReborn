package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

public class VelocityCSF {
    public static final String PLUGIN_NAME = "ConsoleSpamFixReborn";
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;
    private LogFilter logFilter;
    private final LoggerContext loggerContext;
    private final Configuration config;
    private final LogFilterManager logFilterManager;

    private ConfigHandler configHandler;
    private EngineInterface engine;
    private Metrics metrics;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityCSF(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory, PluginContainer pluginContainer, Metrics.Factory metricsFactory) {
        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.pluginContainer = pluginContainer;
        this.metricsFactory = metricsFactory;
        this.loggerContext = (LoggerContext) LogManager.getContext(false);
        this.config = loggerContext.getConfiguration();
        this.logFilterManager = new LogFilterManager(this);
    }
    
    public void updateLogFilter() {
        if (this.engine == null || this.configHandler == null) {
            logger.error("Cannot update log filter: Engine or ConfigHandler is not initialized yet!");
            return;
        }

        this.engine.hideConsoleMessages();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) throws SerializationException {
        // Initialize the configuration handler
        this.configHandler = new ConfigHandler(this);
        if (!this.configHandler.loadConfig()) {
            logger.error("Failed to load configuration. The plugin may not function correctly.");
        }

        // Initialize the engine and log filter
        this.engine = new NewEngine(this);
        this.engine.hideConsoleMessages(); // Ensure logFilter is initialized here

        // Initialize bStats metrics
        int pluginId = 25291; // Replace with your actual plugin ID
        metricsFactory.make(this, pluginId);

        // Register the command after ensuring logFilter is initialized
        ProxyServer proxyServer = this.server;
        proxyServer.getCommandManager().register("csfv", new VelocityCommandHandler(this.configHandler, this.engine, this, this.logFilter));

        // Update log filter
        updateLogFilter();

        // Log successful initialization
        logger.info("{} v{} loaded successfully!", PLUGIN_NAME, pluginContainer.getDescription().getVersion().orElse("Unknown"));
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public EngineInterface getEngine() {
        return engine;
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
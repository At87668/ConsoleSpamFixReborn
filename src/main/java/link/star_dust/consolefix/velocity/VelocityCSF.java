package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

import javax.inject.Inject;

public class VelocityCSF {
    public static final String PLUGIN_NAME = "ConsoleSpamFixReborn";
    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private final PluginContainer pluginContainer;

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
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Initialize the configuration handler
        this.configHandler = new ConfigHandler(this);
        if (!this.configHandler.loadConfig()) {
            logger.error("Failed to load configuration. The plugin may not function correctly.");
        }

        // Initialize the engine
        this.engine = new NewEngine(this);
        this.engine.hideConsoleMessages();
        
        ProxyServer proxyServer = this.server;
        proxyServer.getCommandManager().register("csfv", new VelocityCommandHandler(this.configHandler, this.engine));

        // Initialize bStats metrics
        int pluginId = 25291; // Replace with your actual plugin ID
        metricsFactory.make(this, pluginId);

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
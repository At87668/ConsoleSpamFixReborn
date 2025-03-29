package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;
import org.bstats.velocity.Metrics;

@Plugin(id = "consolespamfix", name = "ConsoleSpamFixReborn", version = "1.11.3")
public class VelocityCSF {
    private final Logger logger;
    private ConfigHandler configHandler;
    private EngineInterface engine;
    
    int pluginId = 25291;

    metricsFactory.make(this, pluginId);

    public VelocityCSF(Logger logger) {
        this.logger = logger;
        this.configHandler = new ConfigHandler(this);
        this.engine = new NewEngine(this);
        engine.hideConsoleMessages();
        logger.info("ConsoleSpamFixReborn loaded successfully!");
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public EngineInterface getEngine() {
        return engine;
    }
}
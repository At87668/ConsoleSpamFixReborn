package link.star_dust.consolefix.velocity;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.spongepowered.configurate.serialize.SerializationException;

public class LogFilterManager {
    private final VelocityCSF plugin;
    private LogFilter activeFilter;

    public LogFilterManager(VelocityCSF plugin) {
        this.plugin = plugin;
    }

    public void updateFilter() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        if (loggerConfig == null) return;

        try {
            // Remove previous filter if exists
            if (activeFilter != null) {
                loggerConfig.removeFilter(activeFilter);
            }

            // Create new filter and load config safely
            LogFilter newFilter = new LogFilter(plugin);
            newFilter.reloadFilters();
            activeFilter = newFilter;

            // Attach to logger
            loggerConfig.addFilter(activeFilter);
            context.updateLoggers();

        } catch (SerializationException e) {
            plugin.getLogger().error("[ConsoleSpamFix] Failed to load Messages-To-Hide-Filter", e);
        }
    }

}
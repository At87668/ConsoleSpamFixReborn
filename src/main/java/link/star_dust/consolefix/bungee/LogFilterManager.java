package link.star_dust.consolefix.bungee;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.spongepowered.configurate.serialize.SerializationException;

public class LogFilterManager {
    private final BungeeCSF plugin;
    private LogFilter activeFilter;

    public LogFilterManager(BungeeCSF plugin) {
        this.plugin = plugin;
    }

    public void updateFilter() {
        List<String> contains = plugin.getConfigHandler()
                                      .getStringList("Messages-To-Hide-Filter.contains");
        List<String> regexStrings = plugin.getConfigHandler()
                                          .getStringList("Messages-To-Hide-Filter.regex");

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        if (loggerConfig == null) return;

        // Remove previous filter if exists
        if (activeFilter != null) {
            loggerConfig.removeFilter(activeFilter);
        }

        // Create new filter and set lists
        LogFilter newFilter = new LogFilter(plugin);

        // Override the cached filters directly
        newFilter.reloadFilters();
        activeFilter = newFilter;

        // Attach to logger
        loggerConfig.addFilter(activeFilter);
        context.updateLoggers();
    }
}
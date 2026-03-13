package link.star_dust.consolefix;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LogFilterManager {
    private final CSF plugin;
    private LogFilter activeFilter;

    public LogFilterManager(CSF plugin) {
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
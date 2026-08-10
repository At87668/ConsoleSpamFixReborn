package link.star_dust.consolefix.core;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.List;

/**
 * Manages the lifecycle of the active {@link LogFilter}: on each update the
 * previous filter is removed from the root logger config and a fresh one is
 * attached, so reloads always reflect the latest config without duplicating
 * filters. Shared by every platform.
 */
public class LogFilterManager {

    private final CsfContext ctx;
    private final EngineInterface engine;
    private LogFilter activeFilter;

    public LogFilterManager(CsfContext ctx, EngineInterface engine) {
        this.ctx = ctx;
        this.engine = engine;
    }

    /**
     * Read the current filter lists from config and (re)attach the filter to
     * the log4j root logger. Safe to call repeatedly (e.g. on reload).
     */
    public void updateFilter() {
        List<String> contains = ctx.getStringList("Messages-To-Hide-Filter.contains");
        List<String> regexStrings = ctx.getStringList("Messages-To-Hide-Filter.regex");

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        if (loggerConfig == null) return;

        // Remove previous filter if exists
        if (activeFilter != null) {
            loggerConfig.removeFilter(activeFilter);
        }

        // Create new filter and set lists
        LogFilter newFilter = new LogFilter(ctx, engine);
        newFilter.reloadFilters();
        activeFilter = newFilter;

        // Attach to logger
        loggerConfig.addFilter(activeFilter);
        context.updateLoggers();
    }
}

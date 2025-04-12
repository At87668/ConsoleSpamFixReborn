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

    public LogFilterManager(BungeeCSF plugin) {
        this.plugin = plugin;
    }

    public void updateFilter(List<String> newMessagesToHide) throws SerializationException {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // 获取根日志记录器
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        // 移除所有现有过滤器
        if (loggerConfig != null) {
        	loggerConfig.removeFilter(loggerConfig.getFilter());
        }

        // 创建新的过滤器
        LogFilter newFilter = new LogFilter(plugin);
        newFilter.refreshMessagesToHide(newMessagesToHide); // 更新过滤规则

        // 如果有新的过滤规则，则添加到 CompositeFilter
        if (!newMessagesToHide.isEmpty()) {
            CompositeFilter compositeFilter = CompositeFilter.createFilters(new Filter[]{newFilter});
            loggerConfig.addFilter(compositeFilter); // 添加新的过滤器
        }

        // 应用更新后的配置
        context.updateLoggers();
    }
}
package link.star_dust.consolefix.velocity;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.spongepowered.configurate.serialize.SerializationException;

public class LogFilterManager {
    private final VelocityCSF plugin;
    private CompositeFilter dynamicFilter;

    public LogFilterManager(VelocityCSF plugin) {
        this.plugin = plugin;
    }

    public void updateFilter(List<String> newMessagesToHide) throws SerializationException {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // 创建新的过滤器
        LogFilter newFilter = new LogFilter(plugin);
        newFilter.refreshMessagesToHide(newMessagesToHide); // 传递新配置

        // 将新过滤器添加到 CompositeFilter 中
        if (dynamicFilter == null) {
            dynamicFilter = CompositeFilter.createFilters(new Filter[]{newFilter});
        } else {
            dynamicFilter = dynamicFilter.addFilter(newFilter);
        }

        // 更新根日志记录器的过滤器
        config.getRootLogger().addFilter(dynamicFilter);
        context.updateLoggers(); // 应用配置 [[3]]
    }
}
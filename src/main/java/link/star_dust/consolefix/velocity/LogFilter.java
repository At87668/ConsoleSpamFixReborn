package link.star_dust.consolefix.velocity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LogFilter implements Filter {
    private final VelocityCSF plugin;

    private volatile List<String> containsFilters = Collections.emptyList();
    private volatile List<Pattern> regexFilters = Collections.emptyList();

    public LogFilter(VelocityCSF plugin) throws SerializationException {
        this.plugin = plugin;
        reloadFilters();
    }

    /**
     * 刷新需要隐藏的消息列表
     */
    public void reloadFilters() throws SerializationException {
        this.containsFilters =
                plugin.getConfigHandler().getStringList("Messages-To-Hide-Filter.contains");

        this.regexFilters =
                plugin.getConfigHandler()
                        .getStringList("Messages-To-Hide-Filter.regex")
                        .stream()
                        .map(this::compileRegexSafe)
                        .filter(p -> p != null)
                        .collect(java.util.stream.Collectors.toList());
    }

    private Pattern compileRegexSafe(String raw) {
        try {
            return Pattern.compile(raw);
        } catch (PatternSyntaxException e) {
            plugin.getLogger().warn("Invalid regex ignored: " + raw);
            return null;
        }
    }

    @Override
    public Filter.Result filter(LogEvent event) {
        return checkMessage(event.getMessage().getFormattedMessage());
    }

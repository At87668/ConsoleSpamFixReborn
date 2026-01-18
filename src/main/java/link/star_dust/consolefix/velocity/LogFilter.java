package link.star_dust.consolefix.velocity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.spongepowered.configurate.serialize.SerializationException;

import link.star_dust.consolefix.bungee.BungeeCSF;

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
                        .toList();
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

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Object message, Throwable t) {
        return checkMessage(message.toString());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable t) {
        return checkMessage(message.getFormattedMessage());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return checkMessage(message);
    }

    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10) {
        return checkMessage(message);
    }

    private Filter.Result checkMessage(String message) {
        for (String s : containsFilters) {
            if (message.contains(s)) {
                plugin.getEngine().addHiddenMsg();
                return Filter.Result.DENY;
            }
        }

        for (Pattern p : regexFilters) {
            if (p.matcher(message).find()) {
                plugin.getEngine().addHiddenMsg();
                return Filter.Result.DENY;
            }
        }

        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public State getState() {
        return State.STARTED;
    }

    @Override
    public void initialize() {}

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}
}
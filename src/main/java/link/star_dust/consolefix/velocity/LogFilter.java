package link.star_dust.consolefix.velocity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.List;

public class LogFilter implements Filter {
    private final VelocityCSF plugin;
    private List<String> messagesToHide;

    public LogFilter(VelocityCSF plugin) {
        this.plugin = plugin;
        refreshMessagesToHide(); // 初始化时加载过滤列表
    }

    /**
     * 刷新需要隐藏的消息列表
     */
    public void refreshMessagesToHide() {
        try {
            this.messagesToHide = plugin.getConfigHandler().getStringList("Messages-To-Hide-Filter");
        } catch (Exception e) {
            plugin.getLogger().error("Failed to reload messages to hide from config", e);
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
        if (messagesToHide == null || messagesToHide.isEmpty()) {
            return Filter.Result.NEUTRAL;
        }

        for (String s : messagesToHide) {
            if (message.contains(s)) {
                plugin.getEngine().addHiddenMsg();
                return Filter.Result.DENY; // 阻止该日志事件
            }
        }
        return Filter.Result.NEUTRAL; // 允许该日志事件
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
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
        // 缓存需要隐藏的消息列表
        try {
            this.messagesToHide = plugin.getConfigHandler().getStringList("Messages-To-Hide-Filter");
        } catch (Exception e) {
            plugin.getLogger().error("Failed to load messages to hide from config", e);
        }
    }

    @Override
    public Result filter(LogEvent event) {
        if (messagesToHide == null || messagesToHide.isEmpty()) {
            return Result.NEUTRAL;
        }

        String message = event.getMessage().getFormattedMessage();
        for (String s : messagesToHide) {
            if (message.contains(s)) {
                plugin.getEngine().addHiddenMsg();
                return Result.DENY; // 阻止该日志事件
            }
        }
        return Result.NEUTRAL; // 允许该日志事件
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
        return checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable t) {
        return checkMessage(message.toString());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable t) {
        return checkMessage(message.getFormattedMessage());
    }

    private Result checkMessage(String message) {
        if (messagesToHide == null || messagesToHide.isEmpty()) {
            return Result.NEUTRAL;
        }

        for (String s : messagesToHide) {
            if (message.contains(s)) {
                plugin.getEngine().addHiddenMsg();
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
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

    @Override
    public Result getOnMatch() {
        return Result.DENY; // 如果匹配到，则阻止日志事件
    }

    @Override
    public Result getOnMismatch() {
        return Result.NEUTRAL; // 如果未匹配到，则允许日志事件
    }

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3, Object p4) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3, Object p4, Object p5) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3, Object p4, Object p5, Object p6) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3, Object p4, Object p5, Object p6, Object p7) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
		
		return this.checkMessage(message);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2,
			Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
		
		return this.checkMessage(message);
	}
}
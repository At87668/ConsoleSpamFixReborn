package link.star_dust.consolefix.core;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Log4j {@link Filter} that hides configured messages from the console
 * and logs. Shared by every platform — the core only depends on log4j and
 * the platform-agnostic {@link CsfContext} / {@link EngineInterface}.
 */
public class LogFilter implements Filter {

    private final CsfContext ctx;
    private final EngineInterface engine;

    private volatile List<String> containsFilters = Collections.emptyList();
    private volatile List<Pattern> regexFilters = Collections.emptyList();

    public LogFilter(CsfContext ctx, EngineInterface engine) {
        this.ctx = ctx;
        this.engine = engine;
        reloadFilters();
    }

    /**
     * Re-read the configured filter lists from the platform config.
     * Missing keys and invalid regexes are handled gracefully (never crash).
     */
    public void reloadFilters() {
        List<String> contains = ctx.getStringList("Messages-To-Hide-Filter.contains");
        this.containsFilters = contains == null ? Collections.emptyList() : contains;

        List<String> regexStrings = ctx.getStringList("Messages-To-Hide-Filter.regex");
        this.regexFilters = regexStrings == null
                ? Collections.emptyList()
                : regexStrings.stream()
                        .map(this::compileRegexSafe)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
    }

    private Pattern compileRegexSafe(String raw) {
        try {
            return Pattern.compile(raw);
        } catch (PatternSyntaxException e) {
            ctx.warn("Invalid regex ignored: " + raw);
            return null;
        }
    }

    private Filter.Result checkMessage(String message) {
        if (message == null) return Result.NEUTRAL;

        for (String s : containsFilters) {
            if (message.contains(s)) {
                engine.addHiddenMsg();
                return Result.DENY;
            }
        }

        for (Pattern pattern : regexFilters) {
            if (pattern.matcher(message).find()) {
                engine.addHiddenMsg();
                return Result.DENY;
            }
        }

        return Result.NEUTRAL;
    }

    @Override
    public LifeCycle.State getState() {
        return LifeCycle.State.STARTED;
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean stop(long timeout, TimeUnit timeUnit) {
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public Filter.Result filter(LogEvent event) {
        return this.checkMessage(event.getMessage().getFormattedMessage());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Object message, Throwable t) {
        return this.checkMessage(message == null ? null : message.toString());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable t) {
        return this.checkMessage(message == null ? null : message.getFormattedMessage());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object p11, Object p12) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }
}

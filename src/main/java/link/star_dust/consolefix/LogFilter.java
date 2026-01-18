package link.star_dust.consolefix;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LogFilter implements Filter {
    private CSF pl;

    private volatile List<String> containsFilters = Collections.emptyList();
    private volatile List<Pattern> regexFilters = Collections.emptyList();

    public LogFilter(CSF plugin) {
        this.pl = plugin;
        reloadFilters();
    }

    public void reloadFilters() {
        this.containsFilters =
                pl.getConfigHandler().getStringList("Messages-To-Hide-Filter.contains");

        this.regexFilters =
                pl.getConfigHandler()
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
            pl.getLogger().warning("[ConsoleSpamFix] Invalid regex ignored: " + raw);
            return null;
        }
    }

    private Filter.Result checkMessage(String message) {

        for (String s : containsFilters) {
            if (message.contains(s)) {
                pl.getEngine().addHiddenMsg();
                return Result.DENY;
            }
        }

        for (Pattern pattern : regexFilters) {
            if (pattern.matcher(message).find()) {
                pl.getEngine().addHiddenMsg();
                return Result.DENY;
            }
        }

        return Result.NEUTRAL;
    }

    public LifeCycle.State getState() {
        try {
            return LifeCycle.State.STARTED;
        }
        catch (Exception exception) {
            return null;
        }
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
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object ... arg4) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        return this.checkMessage(message.toString());
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        return this.checkMessage(message.getFormattedMessage());
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) {
        return this.checkMessage(message);
    }

    @Override
    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) {
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
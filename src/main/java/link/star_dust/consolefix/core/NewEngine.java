package link.star_dust.consolefix.core;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

/**
 * Default engine shared by every platform: attaches a {@link LogFilter} to
 * the log4j root logger and owns the hidden-message counter.
 */
public class NewEngine implements EngineInterface {

    private final CsfContext ctx;
    private int msgHidden = 0;

    public NewEngine(CsfContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getHiddenMessagesCount() {
        return this.msgHidden;
    }

    @Override
    public void addHiddenMsg() {
        ++this.msgHidden;
    }

    @Override
    public void hideConsoleMessages() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addFilter(new LogFilter(this.ctx, this));
    }
}

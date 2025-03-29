package link.star_dust.consolefix.velocity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class NewEngine implements EngineInterface {
    private VelocityCSF csf;
    private int msgHidden = 0;

    public NewEngine(VelocityCSF csf) {
        this.csf = csf;
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
        rootLogger.addFilter(new LogFilter(this.csf));
    }
}

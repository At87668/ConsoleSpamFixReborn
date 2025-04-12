package link.star_dust.consolefix.bungee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class NewEngine implements EngineInterface {
    private final BungeeCSF csf;
    private int msgHidden = 0;

    public NewEngine(BungeeCSF csf) {
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
        if (csf.getConfigHandler() == null) {
            csf.getLogger().warning("ConfigHandler is not initialized yet!");
            return;
        }

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addFilter(new LogFilter(csf));
    }
}

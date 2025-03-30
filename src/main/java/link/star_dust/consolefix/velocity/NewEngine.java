package link.star_dust.consolefix.velocity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.spongepowered.configurate.serialize.SerializationException;

public class NewEngine implements EngineInterface {
    private VelocityCSF csf;
    private LogFilter logFilter;
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
        // 确保 configHandler 已初始化
        if (csf.getConfigHandler() == null) {
            csf.getLogger().error("ConfigHandler is not initialized yet!");
            return;
        }
        // 创建并注册过滤器
        try {
            this.logFilter = new LogFilter(csf); // 确保 logFilter 被正确初始化
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        rootLogger.addFilter(logFilter);
    }
}

package link.star_dust.consolefix.common;

/**
 * Platform-agnostic engine contract shared by every platform.
 *
 * <p>The engine owns the hidden-message counter and knows how to attach
 * the log4j {@code LogFilter} that performs the actual hiding.
 */
public interface EngineInterface {

    /**
     * Attach the console-message filter to the log4j root logger.
     */
    void hideConsoleMessages();

    /**
     * @return the number of messages hidden since startup
     */
    int getHiddenMessagesCount();

    /**
     * Increment the hidden-message counter (called by the active filter).
     */
    void addHiddenMsg();
}

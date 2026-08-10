package link.star_dust.consolefix.common;

/**
 * Platform-agnostic command operations used by the mod platforms
 * (Fabric / Forge / NeoForge).
 *
 * <p>Follows the LuckPerms {@code Sender} abstraction pattern: the core
 * command layer never touches Minecraft types. Each platform implements
 * this interface using reflection so {@code CommandSourceStack} is never
 * referenced at compile time.
 */
public interface CommandBridge {

    /**
     * Send an informational message to the command source.
     */
    void sendMessage(String message);

    /**
     * Send a success-style message (green/white chat text where supported).
     */
    default void sendSuccess(String message) {
        sendMessage(message);
    }

    /**
     * Send a failure/error-style message (red chat text where supported).
     */
    default void sendFailure(String message) {
        sendMessage(message);
    }

    /**
     * Check whether the source has the given permission node.
     */
    boolean hasPermission(String node);

    boolean isPlayer();

    boolean isConsole();

    /**
     * @return the raw platform command source ({@code Object} to avoid a
     *         compile-time dependency on Minecraft classes)
     */
    Object getSender();
}

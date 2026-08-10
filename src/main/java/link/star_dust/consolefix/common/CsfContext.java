package link.star_dust.consolefix.common;

import java.util.List;

/**
 * Platform-agnostic context contract.
 *
 * <p>The shared {@code core} layer (log filter, filter manager, engine)
 * depends only on this interface — never on Bukkit / BungeeCord /
 * Velocity / Fabric / Forge / NeoForge types. Each platform provides its
 * own implementation wiring the contract to its native config + logger.
 */
public interface CsfContext {

    /**
     * Read a string list from the platform config.
     *
     * @param path dotted config path, e.g. {@code Messages-To-Hide-Filter.contains}
     * @return the list, or an empty list when the key is missing or null
     */
    List<String> getStringList(String path);

    /**
     * Read a single string from the platform config.
     *
     * @param path dotted config path
     * @return the value, or {@code null} when the key is missing
     */
    String getString(String path);

    /**
     * Read a single string from the platform config with colour codes applied.
     *
     * @param path dotted config path
     * @return the colourised value, or {@code null} when the key is missing
     */
    String getStringWithColor(String path);

    void info(String message);

    void warn(String message);

    void error(String message);

    /**
     * Reload the platform config from disk.
     */
    void reloadConfig();
}

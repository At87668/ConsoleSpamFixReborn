package link.star_dust.consolefix.bukkit;

/**
 * Capability probe for the Folia scheduler API.
 *
 * <p>Reflective detection: when the Folia {@code RegionScheduler} class is
 * present the server runs Folia, and the global scheduler must not be used.
 */
public class FoliaCheck {
    private static Boolean isFolia = null;

    public static boolean isFolia() {
        if (isFolia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }
}

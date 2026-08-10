package link.star_dust.consolefix.bukkit;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Bukkit / Spigot / Paper / Folia entry point.
 *
 * <p>Only the platform-specific wiring lives here; the actual log filtering
 * is delegated to the shared {@code core} layer through the
 * {@link CsfContext} / {@link EngineInterface} contracts.
 */
public final class CSF extends JavaPlugin {
    public static Logger log;
    public static String pluginName;
    private ConfigHandler cH;
    private EngineInterface eng;
    private LogFilterManager logFilterManager;
    private CsfContext csfContext;

    static {
        pluginName = "ConsoleSpamFixReborn";
    }

    @Override
    public void onEnable() {
        log = this.getLogger();
        log.info("Initializing " + pluginName);

        cH = new ConfigHandler(this);
        csfContext = new BukkitCsfContext(this);
        eng = new NewEngine(csfContext);
        logFilterManager = new LogFilterManager(csfContext, eng);
        CommandHandler cmd = new CommandHandler(this);

        int pluginId = 24348;
        new Metrics(this, pluginId);

        if (this.getCommand("csf") == null) {
            log.severe("Command 'csf' could not be found! Make sure it is defined in plugin.yml.");
        } else {
            Objects.requireNonNull(this.getCommand("csf"), "Command 'csf' not found in plugin.yml").setExecutor(cmd);
        }

        // Initialise the log filter from the config.
        this.getLogFilterManager().updateFilter();
        log.info(pluginName + " loaded successfully!");
    }

    @Override
    public void onDisable() {
        if (!FoliaCheck.isFolia()) {
            Bukkit.getScheduler().cancelTasks(this);
        }
        HandlerList.unregisterAll(this);
        log.info("Messages hidden since the server started: " + this.getEngine().getHiddenMessagesCount());
        log.info(pluginName + " is disabled!");
    }

    public ConfigHandler getConfigHandler() {
        return cH;
    }

    public EngineInterface getEngine() {
        return eng;
    }

    public LogFilterManager getLogFilterManager() {
        return logFilterManager;
    }

    public CsfContext getCsfContext() {
        return csfContext;
    }
}

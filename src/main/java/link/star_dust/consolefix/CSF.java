package link.star_dust.consolefix;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import link.star_dust.consolefix.FoliaCheck;

import java.util.Objects;
import java.util.logging.Logger;

public final class CSF extends JavaPlugin {
    public static Logger log;
    public static String pluginName;
    public boolean is19Server = true;
    public boolean is13Server = false;
    public boolean oldEngine = false;
    private static ConfigHandler cH;
    private static EngineInterface eng;

    static {
        pluginName = "ConsoleSpamFixReborn";
    }

    @Override
    public void onEnable() {
        log = this.getLogger();
        log.info("Initializing " + pluginName);

        this.getMcVersion();
        cH = new ConfigHandler(this);
        eng = this.oldEngine ? new OldEngine(this) : new NewEngine(this);
        CommandHandler cmd = new CommandHandler(this);
        
        int pluginId = 24348;
        new Metrics(this, pluginId);

        // Debugging start
        log.info("Attempting to register command executor for 'csf'");
        if (this.getCommand("csf") == null) {
            log.severe("Command 'csf' could not be found! Make sure it is defined in plugin.yml.");
        } else {
            log.info("Command 'csf' found. Setting executor...");
            Objects.requireNonNull(this.getCommand("csf"), "Command 'csf' not found in plugin.yml").setExecutor(cmd);
            log.info("Command executor for 'csf' set successfully.");
        }
        // Debugging end

        this.getEngine().hideConsoleMessages();
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

    private void getMcVersion() {
        String[] serverVersion = Bukkit.getBukkitVersion().split("-");
        String version = serverVersion[0];
        log.info("Server version detected: " + version);
        if (version.matches("1.7.10") || version.matches("1.7.9") || version.matches("1.7.5") || version.matches("1.7.2") || version.matches("1.8.8") || version.matches("1.8.3") || version.matches("1.8.4") || version.matches("1.8")) {
            this.is19Server = false;
            this.is13Server = false;
            this.oldEngine = true;
        } else if (version.matches("1.9") || version.matches("1.9.1") || version.matches("1.9.2") || version.matches("1.9.3") || version.matches("1.9.4") || version.matches("1.10") || version.matches("1.10.1") || version.matches("1.10.2") || version.matches("1.11") || version.matches("1.11.1") || version.matches("1.11.2")) {
            this.oldEngine = true;
            this.is19Server = true;
            this.is13Server = false;
        } else {
        	this.is13Server = true;
            this.is19Server = true;
            this.oldEngine = false;
        }
    }

    public ConfigHandler getConfigHandler() {
        return cH;
    }

    public EngineInterface getEngine() {
        return eng;
    }
}

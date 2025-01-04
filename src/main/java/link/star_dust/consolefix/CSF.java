package link.star_dust.consolefix;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

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
        
        int pluginId = 23790;
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
        Bukkit.getScheduler().cancelTasks(this);
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
        } else if (version.matches("1.13") || version.matches("1.13.1") || version.matches("1.13.2")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.14") || version.matches("1.14.1") || version.matches("1.14.2") || version.matches("1.14.3") || version.matches("1.14.4")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.15") || version.matches("1.15.1") || version.matches("1.15.2")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.16") || version.matches("1.16.1") || version.matches("1.16.2") || version.matches("1.16.3") || version.matches("1.16.4") || version.matches("1.16.5")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.17") || version.matches("1.17.1")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.18") || version.matches("1.18.1") || version.matches("1.18.2")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.19") || version.matches("1.19.1") || version.matches("1.19.2") || version.matches("1.19.3") || version.matches("1.19.4")) {
            this.oldEngine = false;
            this.is19Server = true;
            this.is13Server = true;
        } else if (version.matches("1.20") || version.matches("1.20.1") || version.matches("1.20.2") || version.matches("1.20.3") || version.matches("1.20.4") || version.matches("1.20.5") || version.matches("1.20.6")) {
            this.is13Server = true;
            this.is19Server = true;
            this.oldEngine = false;
        } else if (version.matches("1.21") || version.matches("1.21.1") || version.matches("1.21.2") || version.matches("1.21.3") || version.matches("1.21.4")) {
            this.is13Server = true;
            this.is19Server = true;
            this.oldEngine = false;
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

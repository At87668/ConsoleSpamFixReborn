package link.star_dust.consolefix.velocity;

import java.io.File;
import java.util.List;

public class ConfigHandler {
    private VelocityCSF csf;

    public ConfigHandler(VelocityCSF csf) {
        this.csf = csf;
        this.loadConfig();
    }

    public boolean loadConfig() {
        File configFile;
        File pluginFolder = new File("plugins" + System.getProperty("file.separator") + CSF.pluginName);
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        if (!(configFile = new File("plugins" + System.getProperty("file.separator") + CSF.pluginName + System.getProperty("file.separator") + "config.yml")).exists()) {
        	VelocityCSF.log.info("No config file found! Creating new one...");
            this.csf.saveDefaultConfig();
        }
        try {
        	VelocityCSF.log.info("Loading the config file...");
            this.csf.getConfig().load(configFile);
            CSF.log.info("Config file loaded!");
            return true;
        }
        catch (Exception e) {
        	VelocityCSF.log.info("Could not load config file! You need to regenerate the config! Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getStringList(String key) {
        if (!this.csf.getConfig().contains(key)) {
            this.csf.getLogger().severe("Could not locate '" + key + "' in the config.yml inside of the " + CSF.pluginName + " folder! (Try generating a new one by deleting the current)");
            return null;
        }
        return this.csf.getConfig().getStringList(key);
    }

    public String getString(String key) {
        if (!this.csf.getConfig().contains(key)) {
            this.csf.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CSF.pluginName + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key;
        }
        return this.csf.getConfig().getString(key);
    }

    public String getStringWithColor(String key) {
        if (!this.csf.getConfig().contains(key)) {
            this.csf.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + CSF.pluginName + " folder! (Try generating a new one by deleting the current)");
            return "errorCouldNotLocateInConfigYml:" + key;
        }
        return this.csf.getConfig().getString(key).replaceAll("&", "§");
    }
}

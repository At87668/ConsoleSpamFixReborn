package link.star_dust.consolefix.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ProxyServer;
import org.bstats.bungeecord.Metrics;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.List;

public class BungeeCSF extends Plugin {
    public static final String PLUGIN_NAME = "ConsoleSpamFixReborn";
    private ConfigHandler configHandler;
    private EngineInterface engine;
    private Metrics metrics;

    @Override
    public void onEnable() {
        // 初始化配置处理器
        this.configHandler = new ConfigHandler(this);
        if (!this.configHandler.loadConfig()) {
            getLogger().warning("Failed to load configuration. The plugin may not function correctly.");
        }

        // 初始化引擎
        this.engine = new NewEngine(this);

        // 初始化 bStats metrics
        int pluginId = 25292;
        metrics = new Metrics(this, pluginId);

        // 注册命令
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeeCommandHandler(configHandler, engine, this));

        // 更新日志过滤器
        updateLogFilter();

        getLogger().info(PLUGIN_NAME + " " + "v" + getDescription().getVersion() + " loaded successfully!");
    }

    public void updateLogFilter() {
        if (this.engine == null || this.configHandler == null) {
            getLogger().warning("Cannot update log filter: Engine or ConfigHandler is not initialized yet!");
            return;
        }
        this.engine.hideConsoleMessages();
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public EngineInterface getEngine() {
        return engine;
    }

    public Path getDataDirectory() {
        return getDataFolder().toPath();
    }
}
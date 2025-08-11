package link.star_dust.consolefix.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeCommandHandler extends Command {
    private final ConfigHandler configHandler;
    private final EngineInterface engine;
    private final BungeeCSF plugin;

    public BungeeCommandHandler(ConfigHandler configHandler, EngineInterface engine, BungeeCSF plugin) {
        super("csfb");
        this.configHandler = configHandler;
        this.engine = engine;
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("csf.admin")) {
            sender.sendMessage("§cYou don't have permission to do that.");
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            boolean success = configHandler.loadConfig();
            if (success) {
                plugin.updateLogFilter();
                sender.sendMessage("Reload successful!");
            } else {
                sender.sendMessage("Failed to reload the config. Check the console for errors.");
            }
        } else {
            sender.sendMessage("Usage: /csfb reload");
        }
    }
}
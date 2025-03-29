package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class VelocityCommandHandler implements SimpleCommand {
    private final ConfigHandler configHandler;

    public VelocityCommandHandler(ConfigHandler configHandler, EngineInterface engine) {
        this.configHandler = configHandler;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // Check if the user has permission
        if (!hasPermission(invocation)) {
            source.sendMessage(MiniMessage.miniMessage().deserialize(
                    configHandler.getChatMessage("NoPermission")
            ));
            return;
        }

        // Parse command arguments
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // Reload the configuration
            boolean success = configHandler.loadConfig();
            if (success) {
                source.sendMessage(MiniMessage.miniMessage().deserialize(
                        configHandler.getChatMessage("CmdReload")
                ));
            } else {
                source.sendMessage(MiniMessage.miniMessage().deserialize("<red>Failed to reload the config. Check the console for errors.</red>"));
            }
        } else {
            // Display usage message
            source.sendMessage(MiniMessage.miniMessage().deserialize(
                    configHandler.getChatMessage("CmdHelp")
            ));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Check if the command source has the required permission
        return invocation.source().hasPermission("csf.admin");
    }
}
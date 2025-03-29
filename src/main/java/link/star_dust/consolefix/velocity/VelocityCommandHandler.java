package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.command.SimpleCommand;

public class VelocityCommandHandler implements SimpleCommand {
    private final VelocityCSF plugin;

    public VelocityCommandHandler(VelocityCSF plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length == 1 && invocation.arguments()[0].equalsIgnoreCase("reload")) {
            invocation.source().sendMessage("Config reloaded!");
            plugin.getConfigHandler().loadConfig();
        } else {
            invocation.source().sendMessage("Usage: /csfv reload");
        }
    }
}
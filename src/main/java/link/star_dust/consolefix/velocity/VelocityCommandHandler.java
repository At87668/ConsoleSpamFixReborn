package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import net.kyori.adventure.text.Component;

import link.star_dust.consolefix.core.LogFilterManager;

public class VelocityCommandHandler implements SimpleCommand {
    private final VelocityCSF velocityCSF;
    private final LogFilterManager logFilterManager;

    public VelocityCommandHandler(VelocityCSF velocityCSF, LogFilterManager logFilterManager) {
        this.velocityCSF = velocityCSF;
        this.logFilterManager = logFilterManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!hasPermission(invocation)) {
            source.sendMessage(Component.text("You don't have permission to do that."));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            boolean success = velocityCSF.getConfigHandler().loadConfig();
            if (success) {
                logFilterManager.updateFilter();
                source.sendMessage(Component.text("Reload successful!"));
            } else {
                source.sendMessage(Component.text("Failed to reload the config. Check the console for errors."));
            }
        } else {
            source.sendMessage(Component.text("Reload Config: /csfv reload"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("csf.admin");
    }
}
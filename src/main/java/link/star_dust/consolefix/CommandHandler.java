package link.star_dust.consolefix;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
    private final CSF csf;

    public CommandHandler(CSF csf) {
        this.csf = csf;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmdlabel, String[] args) {
        if (cmdlabel.equalsIgnoreCase("csf")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        if (!sender.hasPermission("csf.admin")) {
                            sender.sendMessage(this.csf.getConfigHandler().getStringWithColor("ChatMessages.NoPermission"));
                            return true;
                        }
                        sender.sendMessage(this.csf.getConfigHandler().getStringWithColor("ChatMessages.CmdReload"));
                    }
                    this.csf.getConfigHandler().loadConfig();
                    this.csf.getLogFilterManager().updateFilter();
                }
            } else {
                sender.sendMessage(this.csf.getConfigHandler().getStringWithColor("ChatMessages.CmdHelp"));
            }
        }
        return true;
    }
}

package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.command.SimpleCommand;

import org.spongepowered.configurate.serialize.SerializationException;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import link.star_dust.consolefix.velocity.LogFilter;

public class VelocityCommandHandler implements SimpleCommand {
    private final ConfigHandler configHandler;
    private final VelocityCSF velocityCSF;
	private final LogFilter logFilter;

    public VelocityCommandHandler(ConfigHandler configHandler, EngineInterface enginem, VelocityCSF velocityCSF, LogFilter logFilter) {
    	this.velocityCSF = velocityCSF;
        this.configHandler = configHandler;
        this.logFilter = logFilter;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // 检查权限
        if (!hasPermission(invocation)) {
            source.sendMessage(MiniMessage.miniMessage().deserialize("<red>You don't have permission to do that.</red>"));
            return;
        }

        // 处理参数
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // 重新加载配置
            boolean success = configHandler.loadConfig();
            if (success) {
                if (logFilter != null) { // 添加空值检查
                    try {
                        logFilter.refreshMessagesToHide(velocityCSF.getConfigHandler().getStringList("Messages-To-Hide-Filter"));
                    } catch (SerializationException e) {
                        e.printStackTrace();
                    }
                    source.sendMessage(MiniMessage.miniMessage().deserialize("<green>Reload successful!</green>"));
                } else {
                    source.sendMessage(MiniMessage.miniMessage().deserialize("<red>LogFilter is not initialized. Reload failed.</red>"));
                }
            } else {
                source.sendMessage(MiniMessage.miniMessage().deserialize("<red>Failed to reload the config. Check the console for errors.</red>"));
            }
        } else {
            source.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Reload Config: /csfv reload</yellow>"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Check if the command source has the required permission
        return invocation.source().hasPermission("csf.admin");
    }
}
package link.star_dust.consolefix.velocity;

import com.velocitypowered.api.command.SimpleCommand;

import org.spongepowered.configurate.serialize.SerializationException;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import link.star_dust.consolefix.velocity.LogFilter;

public class VelocityCommandHandler implements SimpleCommand {
    private final ConfigHandler configHandler;
    private final VelocityCSF velocityCSF;
	private final LogFilter logFilter;
	private final LogFilterManager logFilterManager;

    public VelocityCommandHandler(ConfigHandler configHandler, EngineInterface enginem, VelocityCSF velocityCSF, LogFilter logFilter, LogFilterManager logFilterManager) throws SerializationException {
    	this.velocityCSF = velocityCSF;
        this.configHandler = configHandler;
        this.logFilterManager = new LogFilterManager(velocityCSF);
        this.logFilter = new LogFilter(velocityCSF);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // 检查权限
        if (!hasPermission(invocation)) {
            source.sendMessage(Component.text("You don't have permission to do that."));
            return;
        }

        // 处理参数
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // 重新加载配置
            boolean success = configHandler.loadConfig();
            if (success) {
                if (logFilter != null) { // 添加空值检查
                    logFilterManager.updateFilter();
                    source.sendMessage(Component.text("Reload successful!"));
                } else {
                    source.sendMessage(Component.text("LogFilter is not initialized. Reload failed."));
                }
            } else {
                source.sendMessage(Component.text("Failed to reload the config. Check the console for errors."));
            }
        } else {
            source.sendMessage(Component.text("Reload Config: /csfv reload"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        // Check if the command source has the required permission
        return invocation.source().hasPermission("csf.admin");
    }
}
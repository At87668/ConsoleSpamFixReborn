package link.star_dust.consolefix.forge;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.core.LogFilterManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/**
 * Registers the server-side {@code /csf reload} and client-side
 * {@code /csfc reload} brigadier commands. The command source
 * ({@code CommandSourceStack}) is held as {@code Object} and permission
 * checks go through {@link ForgeReflection}.
 */
final class ForgeCommandHandler {

    private final CsfContext ctx;
    private final LogFilterManager filterManager;

    ForgeCommandHandler(CsfContext ctx, LogFilterManager filterManager) {
        this.ctx = ctx;
        this.filterManager = filterManager;
    }

    /** Server-side registration (with permission check). */
    @SuppressWarnings({"rawtypes", "unchecked"})
    void register(CommandDispatcher dispatcher) {
        register(dispatcher, "csf", true);
    }

    /** Client-side registration (no permission check needed). */
    @SuppressWarnings({"rawtypes", "unchecked"})
    void registerClient(CommandDispatcher dispatcher) {
        register(dispatcher, "csfc", false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void register(CommandDispatcher dispatcher, String name, boolean checkPermission) {
        LiteralArgumentBuilder node = LiteralArgumentBuilder.literal(name);
        if (checkPermission) {
            node.requires(source -> {
                try {
                    return new ForgeCommandBridge(source).hasPermission("csf.admin");
                } catch (Throwable t) {
                    return false;
                }
            });
        }
        node.executes(ctxCmd -> {
            new ForgeCommandBridge(ctxCmd.getSource()).sendMessage("Reload Config: /" + name + " reload");
            return 1;
        });

        LiteralArgumentBuilder reload = LiteralArgumentBuilder.literal("reload");
        reload.executes(ctxCmd -> {
            Object source = ctxCmd.getSource();
            ForgeCommandBridge bridge = new ForgeCommandBridge(source);
            ctx.reloadConfig();
            filterManager.updateFilter();
            bridge.sendSuccess("Reload successful!");
            return 1;
        });

        node.then(reload);
        dispatcher.register(node);
    }
}

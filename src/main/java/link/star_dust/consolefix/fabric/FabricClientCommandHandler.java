package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.core.LogFilterManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

/**
 * Registers the client-side {@code /csfc reload} brigadier command.
 *
 * <p>The command source is a {@code FabricClientCommandSource} (held as
 * {@code Object}); no permission check is needed on the client.
 */
final class FabricClientCommandHandler {

    private final CsfContext ctx;
    private final LogFilterManager filterManager;

    FabricClientCommandHandler(CsfContext ctx, LogFilterManager filterManager) {
        this.ctx = ctx;
        this.filterManager = filterManager;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void register(CommandDispatcher dispatcher) {
        LiteralArgumentBuilder csfc = LiteralArgumentBuilder.literal("csfc");
        csfc.executes(ctxCmd -> {
            new FabricClientCommandBridge(ctxCmd.getSource()).failure("Reload Config: /csfc reload");
            return 1;
        });

        LiteralArgumentBuilder reload = LiteralArgumentBuilder.literal("reload");
        reload.executes(ctxCmd -> {
            FabricClientCommandBridge bridge = new FabricClientCommandBridge(ctxCmd.getSource());
            ctx.reloadConfig();
            filterManager.updateFilter();
            bridge.success("Reload successful!");
            return 1;
        });

        csfc.then(reload);
        dispatcher.register(csfc);
    }
}

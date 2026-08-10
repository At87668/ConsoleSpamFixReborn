package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.core.LogFilterManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.function.Predicate;

/**
 * Registers the {@code /csf reload} brigadier command and executes it.
 * The command source ({@code CommandSourceStack}) is held as {@code Object}
 * and permission checks go through {@link FabricReflection}.
 */
final class FabricCommandHandler {

    private final CsfContext ctx;
    private final LogFilterManager filterManager;

    FabricCommandHandler(CsfContext ctx, LogFilterManager filterManager) {
        this.ctx = ctx;
        this.filterManager = filterManager;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void register(CommandDispatcher dispatcher) {
        Predicate requires = source -> {
            try {
                Object r = FabricReflection.callAny(source, "hasPermission",
                        new Class<?>[]{int.class}, new Object[]{2});
                return r instanceof Boolean && (Boolean) r;
            } catch (Throwable t) {
                return false;
            }
        };

        LiteralArgumentBuilder csf = LiteralArgumentBuilder.literal("csf");
        csf.requires(requires);
        csf.executes(ctxCmd -> {
            new FabricCommandBridge(ctxCmd.getSource()).sendMessage("Reload Config: /csf reload");
            return 1;
        });

        LiteralArgumentBuilder reload = LiteralArgumentBuilder.literal("reload");
        reload.executes(ctxCmd -> {
            Object source = ctxCmd.getSource();
            FabricCommandBridge bridge = new FabricCommandBridge(source);
            ctx.reloadConfig();
            filterManager.updateFilter();
            bridge.sendSuccess("Reload successful!");
            return 1;
        });

        csf.then(reload);
        dispatcher.register(csf);
    }
}

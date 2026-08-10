package link.star_dust.consolefix.neoforge;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.core.LogFilterManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import java.util.function.Predicate;

/**
 * Registers the {@code /csf reload} brigadier command and executes it.
 * The command source ({@code CommandSourceStack}) is held as {@code Object}
 * and permission checks go through {@link NeoForgeReflection}.
 */
final class NeoForgeCommandHandler {

    private final CsfContext ctx;
    private final LogFilterManager filterManager;

    NeoForgeCommandHandler(CsfContext ctx, LogFilterManager filterManager) {
        this.ctx = ctx;
        this.filterManager = filterManager;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void register(CommandDispatcher dispatcher) {
        Predicate requires = source -> {
            try {
                return new NeoForgeCommandBridge(source).hasPermission("csf.admin");
            } catch (Throwable t) {
                return false;
            }
        };

        LiteralArgumentBuilder csf = LiteralArgumentBuilder.literal("csf");
        csf.requires(requires);
        csf.executes(ctxCmd -> {
            new NeoForgeCommandBridge(ctxCmd.getSource()).sendMessage("Reload Config: /csf reload");
            return 1;
        });

        LiteralArgumentBuilder reload = LiteralArgumentBuilder.literal("reload");
        reload.executes(ctxCmd -> {
            Object source = ctxCmd.getSource();
            NeoForgeCommandBridge bridge = new NeoForgeCommandBridge(source);
            ctx.reloadConfig();
            filterManager.updateFilter();
            bridge.sendSuccess("Reload successful!");
            return 1;
        });

        csf.then(reload);
        dispatcher.register(csf);
    }
}

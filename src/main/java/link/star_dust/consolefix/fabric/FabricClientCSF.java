package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.core.ConfigStore;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric client entry point. Loads the config, attaches the log4j filter to
 * the client logger (hiding spam from the client console and latest.log) and
 * registers the client-side {@code /csfc reload} command.
 */
public class FabricClientCSF implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve("ConsoleSpamFixReborn.yml");

        ConfigStore configStore = new ConfigStore(configFile, "config.yml");
        configStore.load();

        CsfContext ctx = new FabricCsfContext(configStore);
        EngineInterface engine = new NewEngine(ctx);
        LogFilterManager filterManager = new LogFilterManager(ctx, engine);

        // Attach the filter (client console/log spam).
        filterManager.updateFilter();

        // Register /csfc reload via a dynamic-proxy ClientCommandRegistrationCallback.
        FabricEventBus.registerClientCommandRegistration(dispatcherObj ->
                new FabricClientCommandHandler(ctx, filterManager)
                        .register((com.mojang.brigadier.CommandDispatcher<?>) dispatcherObj));

        ctx.info("ConsoleSpamFixReborn (Fabric) client enabled.");
    }
}

package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.core.ConfigStore;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric entry point (server-side). Loads the config, attaches the log4j
 * filter via the shared core and registers the {@code /csfm reload} command
 * through the reflection-based event bus.
 */
public class FabricCSF implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve("ConsoleSpamFixReborn.yml");

        ConfigStore configStore = new ConfigStore(configFile, "config.yml");
        configStore.load();

        CsfContext ctx = new FabricCsfContext(configStore);
        EngineInterface engine = new NewEngine(ctx);
        LogFilterManager filterManager = new LogFilterManager(ctx, engine);

        // Attach the filter (single mechanism, also used on reload).
        filterManager.updateFilter();

        // Register /csfm reload via a dynamic-proxy CommandRegistrationCallback.
        FabricEventBus.registerCommandRegistration(dispatcherObj ->
                new FabricCommandHandler(ctx, filterManager)
                        .register((com.mojang.brigadier.CommandDispatcher<?>) dispatcherObj));

        ctx.info("ConsoleSpamFixReborn (Fabric) enabled.");
    }
}

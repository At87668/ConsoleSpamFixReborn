package link.star_dust.consolefix.neoforge;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.common.FastStatsCompat;
import link.star_dust.consolefix.core.ConfigStore;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import java.nio.file.Path;

/**
 * NeoForge entry point. The {@code @Mod} annotation is provided by a
 * compile-time stub (same FQN as the real NeoForge annotation) so no
 * NeoForge JAR is needed at compile time; at runtime the server provides
 * the real class.
 */
@net.neoforged.fml.common.Mod("consolespamfixreborn")
public class NeoForgeCSF {

    public NeoForgeCSF() {
        Path configFile = NeoForgeReflection.getConfigDir().resolve("ConsoleSpamFixReborn.yml");

        ConfigStore configStore = new ConfigStore(configFile, "config.yml");
        configStore.load();

        CsfContext ctx = new NeoForgeCsfContext(configStore);
        EngineInterface engine = new NewEngine(ctx);
        LogFilterManager filterManager = new LogFilterManager(ctx, engine);

        // Attach the filter (single mechanism, also used on reload).
        filterManager.updateFilter();

        NeoForgeCommandHandler commandHandler = new NeoForgeCommandHandler(ctx, filterManager);

        // Register the csf.admin permission node (fires during server construction).
        NeoForgePermissionRegistry.registerGatherListener();

        Object eventBus = NeoForgeReflection.getMainEventBus();
        if (eventBus != null) {
            // Cache the server for telemetry once it starts.
            Class<?> serverStarting = NeoForgeReflection.forgeClass(
                    "net.neoforged.neoforge.event.server.ServerStartingEvent");
            if (serverStarting == null) {
                serverStarting = NeoForgeReflection.forgeClass(
                        "net.minecraftforge.event.server.ServerStartingEvent");
            }
            final Class<?> startingEvent = serverStarting;
            if (startingEvent != null) {
                NeoForgeReflection.registerEventListener(eventBus, startingEvent,
                        event -> {
                            Object server = NeoForgeReflection.callAny(event, "getServer",
                                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
                            if (server != null) NeoForgeReflection.setCachedServer(server);
                        });
            }

            // RegisterCommandsEvent fires during server construction — register early.
            Class<?> eventClass = NeoForgeReflection.forgeClass(
                    "net.neoforged.neoforge.event.RegisterCommandsEvent");
            if (eventClass == null) {
                // NeoForge 1.20.1 used the net.minecraftforge package.
                eventClass = NeoForgeReflection.forgeClass(
                        "net.minecraftforge.event.RegisterCommandsEvent");
            }
            final Class<?> resolvedEvent = eventClass;
            if (resolvedEvent != null) {
                NeoForgeReflection.registerEventListener(eventBus, resolvedEvent,
                        event -> {
                            Object dispatcher = NeoForgeReflection.callAny(event, "getDispatcher",
                                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
                            if (dispatcher != null) {
                                commandHandler.register((com.mojang.brigadier.CommandDispatcher<?>) dispatcher);
                            }
                        });
            }

            // RegisterClientCommandsEvent fires on the client — register /csfc.
            Class<?> clientCmd = NeoForgeReflection.forgeClass(
                    "net.neoforged.neoforge.event.RegisterClientCommandsEvent");
            if (clientCmd == null) {
                // NeoForge 1.20.1 used the net.minecraftforge package.
                clientCmd = NeoForgeReflection.forgeClass(
                        "net.minecraftforge.event.RegisterClientCommandsEvent");
            }
            final Class<?> resolvedClientCmd = clientCmd;
            if (resolvedClientCmd != null) {
                NeoForgeReflection.registerEventListener(eventBus, resolvedClientCmd,
                        event -> {
                            Object dispatcher = NeoForgeReflection.callAny(event, "getDispatcher",
                                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
                            if (dispatcher != null) {
                                commandHandler.registerClient((com.mojang.brigadier.CommandDispatcher<?>) dispatcher);
                            }
                        });
            }
        }

        // FastStats telemetry (dedicated server only, non-fatal).
        if (NeoForgeReflection.isDedicatedServer()) {
            try {
                FastStatsCompat.create(ctx, NeoForgeReflection.getConfigDir(), "1.12.0",
                        new NeoForgeFastStatsData(), "neoforge", FastStatsCompat.FASTSTATS_TOKEN)
                        .ready();
            } catch (Throwable t) {
                ctx.warn("Failed to initialise FastStats metrics: " + t.getMessage());
            }
        }

        ctx.info("ConsoleSpamFixReborn (NeoForge) enabled.");
    }
}

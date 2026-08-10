package link.star_dust.consolefix.forge;

import link.star_dust.consolefix.common.CsfContext;
import link.star_dust.consolefix.common.EngineInterface;
import link.star_dust.consolefix.core.ConfigStore;
import link.star_dust.consolefix.core.LogFilterManager;
import link.star_dust.consolefix.core.NewEngine;

import java.nio.file.Path;

/**
 * Forge entry point. The {@code @Mod} annotation is provided by a
 * compile-time stub (same FQN as the real Forge annotation) so no Forge JAR
 * is needed at compile time; at runtime the server provides the real class.
 */
@net.minecraftforge.fml.common.Mod("consolespamfixreborn")
public class ForgeCSF {

    public ForgeCSF() {
        Path configFile = ForgeReflection.getConfigDir().resolve("ConsoleSpamFixReborn.yml");

        ConfigStore configStore = new ConfigStore(configFile, "config.yml");
        configStore.load();

        CsfContext ctx = new ForgeCsfContext(configStore);
        EngineInterface engine = new NewEngine(ctx);
        LogFilterManager filterManager = new LogFilterManager(ctx, engine);

        // Attach the filter (single mechanism, also used on reload).
        filterManager.updateFilter();

        ForgeCommandHandler commandHandler = new ForgeCommandHandler(ctx, filterManager);

        Object eventBus = ForgeReflection.getMainEventBus();
        if (eventBus != null) {
            // RegisterCommandsEvent fires during server construction — register early.
            ForgeReflection.registerEventListener(eventBus,
                    ForgeReflection.forgeClass("net.minecraftforge.event.RegisterCommandsEvent"),
                    event -> {
                        Object dispatcher = ForgeReflection.callAny(event, "getDispatcher",
                                ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
                        if (dispatcher != null) {
                            commandHandler.register((com.mojang.brigadier.CommandDispatcher<?>) dispatcher);
                        }
                    });
        }

        ctx.info("ConsoleSpamFixReborn (Forge) enabled.");
    }
}

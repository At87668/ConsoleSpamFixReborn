package link.star_dust.consolefix.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

/**
 * Centralized runtime-name constants for the Minecraft classes, methods and
 * fields accessed via reflection by the Fabric platform.
 *
 * <p>Fabric remaps the Minecraft JAR to <b>intermediary</b> names in
 * production (1.18–1.21.x); on MC 26+ / dev the named/mojang name is used
 * directly. Each constant is resolved at class-load time via the
 * {@link MappingResolver} (named→intermediary), falling back to hardcoded
 * intermediary names cross-referenced from the Fabric intermediary mappings.
 *
 * <p>Verified names are taken from the Fabric intermediary mapping set
 * (via {@code mappings.dev} / FabricMC intermediary, see the source notes).
 */
final class FabricReflectionConstants {

    private FabricReflectionConstants() {}

    /* ---- empty arrays shared with FabricReflection ---- */

    static final Class<?>[] NO_PARAMS = new Class<?>[0];
    static final Object[]   NO_ARGS   = new Object[0];

    /* ---- mapping resolver ---- */

    private static final MappingResolver MR;
    private static final boolean IS_DEV;
    /** True when the runtime namespace is named/mojang (MC 26+), false when
     *  it is intermediary (MC 1.18–1.21). */
    private static final boolean IS_NAMED_RUNTIME;
    static {
        FabricLoader fl = FabricLoader.getInstance();
        MR = fl.getMappingResolver();
        IS_DEV = fl.isDevelopmentEnvironment();
        boolean namedRt = false;
        try {
            // On MC 26+ named classes are the runtime; on 1.18–1.21 only
            // intermediary class_NNNN names exist at runtime.
            Class.forName("net.minecraft.world.level.Level");
            namedRt = true;
        } catch (ClassNotFoundException ignored) {
        }
        IS_NAMED_RUNTIME = namedRt;
    }

    // ==================================================================
    // Resolution helpers
    // ==================================================================

    /**
     * Resolve a method name to its runtime form.
     *
     * <ol>
     *   <li>Try {@code named→intermediary} via {@link MappingResolver#mapMethodName}
     *       (works in dev / MC 26+ where "named" namespace is available).</li>
     *   <li>Hardcoded intermediary fallback for production 1.18–1.21.</li>
     *   <li>Return the named name unchanged (last resort).</li>
     * </ol>
     */
    private static String im(String namedOwner, String named,
                             String desc, String interFallback) {
        try {
            String r = MR.mapMethodName("named", namedOwner, named, desc);
            if (r != null && !r.equals(named)) return r;
        } catch (Throwable ignore) {
        }
        if (!IS_DEV && !IS_NAMED_RUNTIME && interFallback != null) return interFallback;
        return named;
    }

    /** Same as {@link #im} but for fields. */
    private static String ifd(String namedOwner, String named,
                              String desc, String interFallback) {
        try {
            String r = MR.mapFieldName("named", namedOwner, named, desc);
            if (r != null && !r.equals(named)) return r;
        } catch (Throwable ignore) {
        }
        if (!IS_DEV && !IS_NAMED_RUNTIME && interFallback != null) return interFallback;
        return named;
    }

    // ==================================================================
    // CLASS NAMES  (mojang/named — forName handles intermediary fallback)
    // ==================================================================

    static final String CLS_MINECRAFT_SERVER    = "net.minecraft.server.MinecraftServer";
    static final String CLS_PLAYER_LIST         = "net.minecraft.server.players.PlayerList";
    static final String CLS_COMMAND_SOURCE_STACK = "net.minecraft.commands.CommandSourceStack";
    static final String CLS_SERVER_PLAYER       = "net.minecraft.server.level.ServerPlayer";
    static final String CLS_ENTITY              = "net.minecraft.world.entity.Entity";
    static final String CLS_COMPONENT           = "net.minecraft.network.chat.Component";
    static final String CLS_MUTABLE_COMPONENT   = "net.minecraft.network.chat.MutableComponent";
    static final String CLS_TEXT_COMPONENT      = "net.minecraft.network.chat.TextComponent";

    // ==================================================================
    // METHOD NAMES
    //   im(namedOwner, namedName, descriptor, intermediaryFallback)
    // ==================================================================

    // -- CommandSourceStack ----------------------------------------------
    static final String M_GET_SERVER      = im("net.minecraft.commands.CommandSourceStack", "getServer",   "()Lnet/minecraft/server/MinecraftServer;", "method_9211");
    static final String M_IS_PLAYER       = im("net.minecraft.commands.CommandSourceStack", "isPlayer",    "()Z",                                      "method_43737");
    static final String M_GET_ENTITY      = im("net.minecraft.commands.CommandSourceStack", "getEntity",   "()Lnet/minecraft/world/entity/Entity;",     "method_9228");
    static final String M_SEND_SUCCESS    = im("net.minecraft.commands.CommandSourceStack", "sendSuccess", "(Lnet/minecraft/network/chat/Component;Z)V", "method_9226");
    static final String M_SEND_FAILURE    = im("net.minecraft.commands.CommandSourceStack", "sendFailure", "(Lnet/minecraft/network/chat/Component;)V",  "method_9213");
    static final String M_HAS_PERMISSION  = im("net.minecraft.commands.CommandSourceStack", "hasPermission","(I)Z",                                    "method_9259");

    // -- MinecraftServer / PlayerList (telemetry) ------------------------
    static final String M_GET_PLAYER_LIST = im("net.minecraft.server.MinecraftServer", "getPlayerList", "()Lnet/minecraft/server/players/PlayerList;", "method_3760");
    static final String M_GET_PLAYERS     = im("net.minecraft.server.players.PlayerList", "getPlayers", "()Ljava/util/List;", "method_14571");
    static final String M_IS_ONLINE_MODE  = im("net.minecraft.server.MinecraftServer", "isOnlineMode", "()Z", "method_3828");
    static final String M_GET_GAME_PROFILE = im("net.minecraft.world.entity.player.Player", "getGameProfile", "()Lcom/mojang/authlib/GameProfile;", "method_7334");
    static final String M_IS_OP           = im("net.minecraft.server.players.PlayerList", "isOp", "(Lcom/mojang/authlib/GameProfile;)Z", "method_14569");

    // -- Component -------------------------------------------------------
    // Component.literal(String) — named only; resolved by signature scan on
    // production 1.18–1.21 (intermediary has no stable name for this).
    static final String M_COMPONENT_LITERAL = "literal";

    // ==================================================================
    // Runtime method name redirector
    //
    // On production servers callers may pass mojang method names but the
    // runtime class only knows the intermediary name. This map bridges the
    // gap at lookup time.
    // ==================================================================

    private static final java.util.Map<String, String> METHOD_REDIRECT = new java.util.HashMap<>();

    static {
        putM("getServer", M_GET_SERVER);
        putM("isPlayer", M_IS_PLAYER);
        putM("isExecutedByPlayer", M_IS_PLAYER);          // 1.18 alias
        putM("getEntity", M_GET_ENTITY);
        putM("sendSuccess", M_SEND_SUCCESS);
        putM("sendFailure", M_SEND_FAILURE);
        putM("hasPermission", M_HAS_PERMISSION);
        putM("hasPermissionLevel", M_HAS_PERMISSION);     // 1.18 alias
        putM("getPlayerList", M_GET_PLAYER_LIST);
        putM("getPlayers", M_GET_PLAYERS);
        putM("isOnlineMode", M_IS_ONLINE_MODE);
        putM("getGameProfile", M_GET_GAME_PROFILE);
        putM("isOp", M_IS_OP);
        putM("literal", M_COMPONENT_LITERAL);
    }

    private static void putM(String mojangName, String resolved) {
        METHOD_REDIRECT.put(mojangName, resolved);
    }

    /** Redirect a bare mojang method name to its resolved runtime form. */
    static String redirectMethod(String bareName) {
        String r = METHOD_REDIRECT.get(bareName);
        return r != null ? r : bareName;
    }

    // ==================================================================
    // Class-name fallback  (named → intermediary for production servers)
    // ==================================================================

    private static final java.util.Map<String, String> NAMED_TO_INTER = new java.util.HashMap<>();

    static {
        NAMED_TO_INTER.put("net.minecraft.server.MinecraftServer", "net.minecraft.server.MinecraftServer");
        NAMED_TO_INTER.put("net.minecraft.commands.CommandSourceStack", "net.minecraft.class_2168");
        NAMED_TO_INTER.put("net.minecraft.server.players.PlayerList", "net.minecraft.class_3324");
        NAMED_TO_INTER.put("net.minecraft.server.level.ServerPlayer", "net.minecraft.class_3222");
        NAMED_TO_INTER.put("net.minecraft.world.entity.Entity", "net.minecraft.class_1297");
        NAMED_TO_INTER.put("net.minecraft.network.chat.Component", "net.minecraft.class_2561");
        NAMED_TO_INTER.put("net.minecraft.network.chat.MutableComponent", "net.minecraft.class_5250");
        NAMED_TO_INTER.put("net.minecraft.network.chat.TextComponent", "net.minecraft.class_2585");
    }

    static String toIntermediaryClass(String named) {
        return NAMED_TO_INTER.get(named);
    }
}

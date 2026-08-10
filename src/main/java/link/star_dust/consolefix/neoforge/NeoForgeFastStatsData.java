package link.star_dust.consolefix.neoforge;

import link.star_dust.consolefix.common.FastStatsCompat;

import java.util.Collection;

/**
 * FastStats telemetry provider for the NeoForge runtime (defensive reads).
 * Uses the cached server instance resolved during {@code ServerStartingEvent}.
 */
final class NeoForgeFastStatsData implements FastStatsCompat.Data {

    @Override
    public int playerAmount() {
        try {
            Object server = NeoForgeReflection.getServer();
            if (server == null) return 0;
            Object pm = NeoForgeReflection.callAny(server, "getPlayerList",
                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
            if (pm == null) return 0;
            Object list = NeoForgeReflection.callAny(pm, "getPlayers",
                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
            if (list instanceof Collection) return ((Collection<?>) list).size();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    @Override
    public int onlineMode() {
        try {
            Object server = NeoForgeReflection.getServer();
            if (server == null) return -1;
            Object v = NeoForgeReflection.callAny(server, "isOnlineMode",
                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
            if (v instanceof Boolean) return ((Boolean) v) ? 1 : 0;
        } catch (Throwable ignored) {
        }
        return -1;
    }

    @Override
    public String serverSoftware() {
        return "NeoForge";
    }

    @Override
    public String platformTag() {
        return "neoforge";
    }

    @Override
    public String serverVersion() {
        try {
            // Stable FML API — avoids an SRG method-name lookup on MinecraftServer.
            Object info = NeoForgeReflection.callStatic("net.neoforged.fml.loading.FMLLoader",
                    "versionInfo", NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
            if (info == null) return null;
            Object v = NeoForgeReflection.callAny(info, "mcVersion",
                    NeoForgeReflectionConstants.NO_PARAMS, NeoForgeReflectionConstants.NO_ARGS);
            return v == null ? null : v.toString();
        } catch (Throwable ignored) {
            return null;
        }
    }
}

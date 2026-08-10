package link.star_dust.consolefix.forge;

import link.star_dust.consolefix.common.FastStatsCompat;

import java.util.Collection;

/**
 * FastStats telemetry provider for the Forge runtime (defensive reads).
 * Uses the cached server instance resolved during {@code ServerStartingEvent}.
 */
final class ForgeFastStatsData implements FastStatsCompat.Data {

    @Override
    public int playerAmount() {
        try {
            Object server = ForgeReflection.getServer();
            if (server == null) return 0;
            Object pm = ForgeReflection.callAny(server, "getPlayerList",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (pm == null) return 0;
            Object list = ForgeReflection.callAny(pm, "getPlayers",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (list instanceof Collection) return ((Collection<?>) list).size();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    @Override
    public int onlineMode() {
        try {
            Object server = ForgeReflection.getServer();
            if (server == null) return -1;
            Object v = ForgeReflection.callAny(server, "isOnlineMode",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (v instanceof Boolean) return ((Boolean) v) ? 1 : 0;
        } catch (Throwable ignored) {
        }
        return -1;
    }

    @Override
    public String serverSoftware() {
        return "Forge";
    }

    @Override
    public String platformTag() {
        return "forge";
    }

    @Override
    public String serverVersion() {
        try {
            // Stable FML API — avoids an SRG method-name lookup on MinecraftServer.
            Object info = ForgeReflection.callStatic("net.minecraftforge.fml.loading.FMLLoader",
                    "versionInfo", ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (info == null) return null;
            Object v = ForgeReflection.callAny(info, "mcVersion",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            return v == null ? null : v.toString();
        } catch (Throwable ignored) {
            return null;
        }
    }
}

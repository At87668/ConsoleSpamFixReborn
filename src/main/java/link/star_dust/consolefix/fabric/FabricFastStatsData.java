package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.FastStatsCompat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Collection;

/**
 * FastStats telemetry provider for the Fabric runtime (defensive reads).
 * Uses the cached server instance resolved during {@link ServerLifecycleEvents}.
 */
final class FabricFastStatsData implements FastStatsCompat.Data {

    @Override
    public int playerAmount() {
        try {
            Object server = FabricReflection.getServer();
            if (server == null) return 0;
            Object pm = FabricReflection.callAny(server, "getPlayerList",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (pm == null) return 0;
            Object list = FabricReflection.callAny(pm, "getPlayers",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (list instanceof Collection) return ((Collection<?>) list).size();
        } catch (Throwable ignored) {
        }
        return 0;
    }

    @Override
    public int onlineMode() {
        try {
            Object server = FabricReflection.getServer();
            if (server == null) return -1;
            Object v = FabricReflection.callAny(server, "isOnlineMode",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (v instanceof Boolean) return ((Boolean) v) ? 1 : 0;
        } catch (Throwable ignored) {
        }
        return -1;
    }

    @Override
    public String serverSoftware() {
        return "Fabric";
    }

    @Override
    public String platformTag() {
        return "fabric";
    }

    @Override
    public String serverVersion() {
        try {
            return FabricLoader.getInstance().getModContainer("minecraft")
                    .map(mc -> mc.getMetadata().getVersion().getFriendlyString())
                    .orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

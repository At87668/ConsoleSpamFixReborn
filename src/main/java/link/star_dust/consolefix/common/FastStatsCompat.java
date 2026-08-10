/*
 * Adapted from MinerTrack (GNU General Public License v3.0), Copyright (c)
 * At87668 (Author87668) <https://github.com/At87668>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */

package link.star_dust.consolefix.common;

import com.google.gson.JsonObject;
import dev.faststats.Metrics;
import dev.faststats.SimpleContext;
import dev.faststats.SimpleMetrics;
import dev.faststats.config.SimpleConfig;
import dev.faststats.internal.Logger;
import dev.faststats.internal.PlatformLoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Platform-agnostic FastStats (faststats.dev) bridge for the mod platforms
 * (Fabric / Forge / NeoForge).
 *
 * <p>The official FastStats SDK has no Forge module, and its Fabric/NeoForge
 * modules require the platform APIs on the compile classpath — incompatible
 * with ConsoleSpamFixReborn's "compile against server platforms only, reflect
 * the rest" constraint. This class drives the SDK's platform-agnostic
 * {@code core} / {@code config} modules directly: it extends
 * {@link SimpleContext}, schedules submissions on its own daemon thread, and
 * reports the standard fields from a {@link Data} provider that each mod
 * platform implements via its existing reflection helpers.
 *
 * <p>{@link SimpleContext}, {@link SimpleMetrics} and {@link SimpleConfig} are
 * marked {@code @ApiStatus.Internal} by FastStats — the official platform
 * modules build on them the same way, so this is an accepted, version-pinned
 * dependency (pinned to the {@code 0.29.4} artifact in {@code build.gradle}).
 *
 * <p>The submission endpoint is {@code https://metrics.faststats.dev/v1/collect}
 * (JDK {@code java.net.http}, gzip, {@code Authorization: Bearer <token>}), the
 * initial delay is 30 s and the period is 30 min. All telemetry failure is
 * non-fatal: construction is wrapped by the callers so a transport error never
 * aborts server startup.
 */
public final class FastStatsCompat extends SimpleContext {

    /**
     * The FastStats project token (see {@code dev.faststats.Token.PATTERN}).
     */
    public static final String FASTSTATS_TOKEN = "785504187c466ce349c9c4d4278cf4f7";

    /** Live telemetry supplier implemented by each mod platform. */
    public interface Data {
        /** Current online player count; negative values are coerced to 0. */
        int playerAmount();

        /** 1 = online mode, 0 = offline mode, -1 = unknown (field omitted). */
        int onlineMode();

        /** Server software label, e.g. "Fabric". */
        String serverSoftware();

        /** Minecraft version, e.g. "1.21.1"; may be null/empty (field omitted). */
        String serverVersion();

        /** Lowercase platform tag appended to the plugin version, e.g. "fabric". */
        String platformTag();
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "consolespamfixreborn-faststats");
        t.setDaemon(true);
        return t;
    });
    private final Set<Future<?>> tasks = new CopyOnWriteArraySet<>();
    private final Data data;
    private final String version;

    private FastStatsCompat(Factory factory, CsfContext ctx, Path dataFolder, String version,
                            Data data, String platform, String token, PlatformLoggerFactory loggerFactory) {
        super(factory, loggerFactory, SimpleConfig.read(configPath(dataFolder), loggerFactory), platform, token);
        this.data = data;
        this.version = version;
        initializeServices(factory);
    }

    /** Convenience factory wiring the default metrics service. */
    public static FastStatsCompat create(CsfContext ctx, Path dataFolder, String version,
                                         Data data, String platform, String token) {
        return new Factory(ctx, dataFolder, version, data, platform, token)
                .metrics(Metrics.Factory::create)
                .create();
    }

    @Override
    protected boolean preSubmissionStart() {
        // First run: prints the opt-out notice and defers submission until the
        // next restart (FastStats compliance — users must be able to opt out).
        return ((SimpleConfig) getConfig()).preSubmissionStart(this);
    }

    @Override
    public String getProjectName() {
        return "consolespamfixreborn";
    }

    @Override
    protected Metrics.Factory metricsFactory() {
        return new SimpleMetrics.Factory(this) {
            @Override
            public Metrics create() {
                return new ModMetricsImpl(this, data, version);
            }
        };
    }

    @Override
    protected void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        tasks.add(executor.scheduleAtFixedRate(task, initialDelay, period, unit));
    }

    @Override
    public void shutdown() {
        tasks.forEach(future -> future.cancel(false));
        super.shutdown();
        executor.shutdownNow();
    }

    private static PlatformLoggerFactory loggerFactory(CsfContext ctx) {
        return new PlatformLoggerFactory((level, throwable, message) -> {
            if (level == Logger.LogLevel.ERROR || level == Logger.LogLevel.WARN) {
                String suffix = throwable != null && throwable.getMessage() != null
                        ? " — " + throwable.getMessage() : "";
                ctx.warn("[FastStats] " + message + suffix);
            } else {
                ctx.info("[FastStats] " + message);
            }
        });
    }

    private static Path configPath(Path dataFolder) {
        return new File(dataFolder.toFile(), "faststats" + File.separator + "config.properties").toPath();
    }

    /** Standard mod-platform metrics payload (plus FastStats' own internal data). */
    private static final class ModMetricsImpl extends SimpleMetrics {
        private final Data data;
        private final String version;

        ModMetricsImpl(Factory factory, Data data, String version) {
            super(factory);
            this.data = data;
            this.version = version;
        }

        @Override
        protected void appendDefaultData(JsonObject metrics) {
            try {
                metrics.addProperty("server_type", data.serverSoftware());
            } catch (Throwable ignored) {
            }
            try {
                String v = data.serverVersion();
                if (v != null && !v.isEmpty()) metrics.addProperty("platform_version", v);
            } catch (Throwable ignored) {
            }
            try {
                int om = data.onlineMode();
                if (om >= 0) metrics.addProperty("online_mode", om == 1);
            } catch (Throwable ignored) {
            }
            try {
                int pc = data.playerAmount();
                if (pc >= 0) metrics.addProperty("player_count", pc);
            } catch (Throwable ignored) {
            }
            try {
                String pv = version;
                String tag = data.platformTag();
                if (pv != null && !pv.isEmpty()) {
                    if (tag != null && !tag.isEmpty() && !pv.endsWith("+" + tag)) pv = pv + "+" + tag;
                    metrics.addProperty("plugin_version", pv);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    /** Builder for {@link FastStatsCompat}. */
    public static final class Factory extends SimpleContext.Factory<FastStatsCompat, Factory> {
        private final CsfContext ctx;
        private final Path dataFolder;
        private final String version;
        private final Data data;
        private final String platform;
        private final String token;

        public Factory(CsfContext ctx, Path dataFolder, String version, Data data, String platform, String token) {
            this.ctx = ctx;
            this.dataFolder = dataFolder;
            this.version = version;
            this.data = data;
            this.platform = platform;
            this.token = token;
        }

        @Override
        public FastStatsCompat create() {
            return new FastStatsCompat(this, ctx, dataFolder, version, data, platform, token, loggerFactory(ctx));
        }
    }
}

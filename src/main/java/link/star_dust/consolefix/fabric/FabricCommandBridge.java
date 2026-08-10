package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.CommandBridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Fabric {@link CommandBridge} wrapping {@code CommandSourceStack} (held as
 * {@code Object} to avoid a compile-time dependency on Minecraft classes).
 *
 * <p>Follows the MinerTrack reference implementation: message routing uses
 * a multi-version fallback chain — {@code sendSuccess(Supplier, boolean)}
 * (1.20.2+), {@code sendSuccess(Component, boolean)} (1.18.2–1.20.1),
 * {@code sendFailure(Component)}, {@code sendSystemMessage(Component)},
 * {@code sendMessage(Component, UUID)} and legacy {@code sendMessage(...)}.
 */
final class FabricCommandBridge implements CommandBridge {

    private final Object source;

    FabricCommandBridge(Object source) {
        this.source = source;
    }

    private static Object createText(String message) {
        return FabricReflection.createText(message);
    }

    @Override
    public Object getSender() {
        return source;
    }

    @Override
    public boolean isPlayer() {
        try {
            Object r = FabricReflection.callAny(source, "isPlayer",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (r instanceof Boolean) return (Boolean) r;
        } catch (Throwable t) {
            // fall through
        }
        return false;
    }

    @Override
    public boolean isConsole() {
        return !isPlayer();
    }

    @Override
    public boolean hasPermission(String node) {
        if (source == null) return false;
        // 1) Try LuckPerms via fabric-permissions-api (reflection, isInstance-matched).
        if (checkLPPermission(source, node, 2)) return true;
        // 2) Fall back to vanilla op-level / operator check.
        return checkVanillaOpLevel(source, 2);
    }

    // ── Permission checks ────────────────────────────────────────────
    //
    // Use Lucko's fabric-permissions-api (me.lucko.fabric.api.permissions.v0.Permissions)
    // via reflection — its check() overloads reference Minecraft types not on our
    // compile classpath. fabric-permissions-api is NOT bundled; LuckPerms ships its
    // own copy at runtime. When LP is absent, we fall back to the vanilla op level.

    /** Cached list of all Permissions.check() overloads matching signature (?, String, int). */
    private static volatile List<Method> allCheckMethods;

    private static List<Method> findAllCheckMethods() {
        if (allCheckMethods != null) return allCheckMethods;
        List<Method> result = new ArrayList<>();
        try {
            Class<?> permsCls = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            for (Method m : permsCls.getMethods()) {
                if (m.getName().equals("check") && m.getParameterCount() == 3) {
                    Class<?>[] pts = m.getParameterTypes();
                    if (pts[1] == String.class && (pts[2] == int.class || pts[2] == Integer.class)) {
                        result.add(m);
                    }
                }
            }
        } catch (Throwable t) {
            result = Collections.emptyList();
        }
        allCheckMethods = result;
        return result;
    }

    /**
     * Call {@code Permissions.check(source, node, defaultOpLevel)} via reflection,
     * trying each cached overload until {@code param[0].isInstance(source)} matches.
     */
    private static boolean checkLPPermission(Object source, String node, int defaultOpLevel) {
        for (Method m : findAllCheckMethods()) {
            Class<?> sourceType = m.getParameterTypes()[0];
            if (!sourceType.isInstance(source)) continue;
            try {
                Object result = m.invoke(null, source, node, defaultOpLevel);
                return result instanceof Boolean && (Boolean) result;
            } catch (Throwable t) {
                return false;
            }
        }
        return false;
    }

    /**
     * Vanilla op-level / operator check — the catch-all fallback. Console /
     * non-player sources are always allowed.
     */
    private static boolean checkVanillaOpLevel(Object source, int minLevel) {
        if (source == null) return false;
        Object player = resolvePlayerEntity(source);
        if (player == null) return true; // not a player → console → allowed
        Object r = FabricReflection.callAny(source, "hasPermission",
                new Class<?>[]{int.class}, new Object[]{minLevel});
        if (r instanceof Boolean) return (Boolean) r;
        return isPlayerOperator(player);
    }

    private static Object resolvePlayerEntity(Object source) {
        if (source == null) return null;
        try {
            Object entity = FabricReflection.callAny(source, "getEntity",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (entity == null) return null;
            Class<?> serverPlayer = FabricReflection.forName(FabricReflectionConstants.CLS_SERVER_PLAYER);
            if (serverPlayer != null && serverPlayer.isInstance(entity)) return entity;
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isPlayerOperator(Object player) {
        try {
            Object server = FabricReflection.getServer();
            if (server == null) return false;
            Object pm = FabricReflection.callAny(server, "getPlayerList",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (pm == null) return false;
            Object profile = FabricReflection.callAny(player, "getGameProfile",
                    FabricReflectionConstants.NO_PARAMS, FabricReflectionConstants.NO_ARGS);
            if (profile == null) return false;
            Object result = FabricReflection.callAny(pm, "isOp",
                    new Class<?>[]{profile.getClass()}, new Object[]{profile});
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void sendMessage(String message) {
        if (source == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        Object text = createText(message);
        if (text == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        if (!sendFeedback(source, text, true))
            System.out.println("[ConsoleSpamFixReborn] " + message);
    }

    @Override
    public void sendSuccess(String message) {
        if (source == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        Object text = createText(message);
        if (text == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        if (!sendFeedback(source, text, true))
            System.out.println("[ConsoleSpamFixReborn] " + message);
    }

    @Override
    public void sendFailure(String message) {
        if (source == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        Object text = createText(message);
        if (text == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        if (!sendFeedback(source, text, false))
            System.out.println("[ConsoleSpamFixReborn] " + message);
    }

    /**
     * Multi-version fallback chain for sending a component to a
     * {@code CommandSourceStack}. Returns {@code true} when delivered.
     */
    private static boolean sendFeedback(Object target, Object text, boolean isSuccess) {
        if (text == null || target == null) return false;
        Class<?> textCls = FabricReflection.resolveTextComponentClass();
        if (textCls == null) return false;
        Class<?> targetCls = target.getClass();

        // sendSuccess(Supplier<Component>, boolean) — 1.20.2+
        // sendSuccess(Component, boolean) — 1.18.2–1.20.1
        if (isSuccess) {
            boolean oldDebug = FabricReflection.DEBUG_REFLECTION;
            FabricReflection.DEBUG_REFLECTION = false;
            try {
                try {
                    Method m = FabricReflection.findMethod(targetCls, "sendSuccess",
                            new Class<?>[]{Supplier.class, boolean.class});
                    if (m != null) {
                        final Object t = text;
                        m.invoke(target, (Supplier<?>) () -> t, false);
                        return true;
                    }
                } catch (Throwable t) {
                    // fall through
                }
                try {
                    Method m = FabricReflection.findMethod(targetCls, "sendSuccess",
                            new Class<?>[]{textCls, boolean.class});
                    if (m != null) {
                        m.invoke(target, text, false);
                        return true;
                    }
                } catch (Throwable t) {
                    // fall through
                }
            } finally {
                FabricReflection.DEBUG_REFLECTION = oldDebug;
            }
        }

        // sendFailure(Component)
        if (!isSuccess) {
            try {
                Method m = FabricReflection.findMethod(targetCls, "sendFailure",
                        new Class<?>[]{textCls});
                if (m != null) {
                    m.invoke(target, text);
                    return true;
                }
            } catch (Throwable t) {
                // fall through
            }
        }

        // sendSystemMessage(Component) — 1.19.3+
        try {
            Method m = FabricReflection.findMethod(targetCls, "sendSystemMessage",
                    new Class<?>[]{textCls});
            if (m != null) {
                m.invoke(target, text);
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        // sendMessage(Component, UUID) — 1.18.2
        try {
            Method m = FabricReflection.findMethod(targetCls, "sendMessage",
                    new Class<?>[]{textCls, UUID.class});
            if (m != null) {
                m.invoke(target, text, UUID.randomUUID());
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        // sendMessage(Component, boolean) — player entity fallback
        try {
            Method m = FabricReflection.findMethod(targetCls, "sendMessage",
                    new Class<?>[]{textCls, boolean.class});
            if (m != null) {
                m.invoke(target, text, false);
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        // sendMessage(Component) — last resort
        try {
            Method m = FabricReflection.findMethod(targetCls, "sendMessage",
                    new Class<?>[]{textCls});
            if (m != null) {
                m.invoke(target, text);
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        return false;
    }
}

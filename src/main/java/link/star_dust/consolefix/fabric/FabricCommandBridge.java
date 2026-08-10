package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.common.CommandBridge;

import java.lang.reflect.Method;
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
        // csf.admin defaults to op → permission level 2.
        try {
            Object r = FabricReflection.callAny(source, "hasPermission",
                    new Class<?>[]{int.class}, new Object[]{2});
            return r instanceof Boolean && (Boolean) r;
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

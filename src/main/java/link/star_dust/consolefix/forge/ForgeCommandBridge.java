package link.star_dust.consolefix.forge;

import link.star_dust.consolefix.common.CommandBridge;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Forge {@link CommandBridge} wrapping {@code CommandSourceStack} (held as
 * {@code Object}). Mirrors the Fabric implementation — same multi-version
 * fallback chain for message routing, all via reflection.
 */
final class ForgeCommandBridge implements CommandBridge {

    private final Object source;

    ForgeCommandBridge(Object source) {
        this.source = source;
    }

    private static Object createText(String message) {
        return ForgeReflection.createText(message);
    }

    @Override
    public Object getSender() {
        return source;
    }

    @Override
    public boolean isPlayer() {
        try {
            Object r = ForgeReflection.callAny(source, "isPlayer",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
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
            Object r = ForgeReflection.callAny(source, "hasPermission",
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
        Class<?> textCls = ForgeReflection.resolveTextComponentClass();
        if (textCls == null) return false;
        Class<?> targetCls = target.getClass();

        if (isSuccess) {
            boolean oldDebug = ForgeReflection.DEBUG_REFLECTION;
            ForgeReflection.DEBUG_REFLECTION = false;
            try {
                try {
                    Method m = ForgeReflection.findMethod(targetCls, "sendSuccess",
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
                    Method m = ForgeReflection.findMethod(targetCls, "sendSuccess",
                            new Class<?>[]{textCls, boolean.class});
                    if (m != null) {
                        m.invoke(target, text, false);
                        return true;
                    }
                } catch (Throwable t) {
                    // fall through
                }
            } finally {
                ForgeReflection.DEBUG_REFLECTION = oldDebug;
            }
        }

        if (!isSuccess) {
            try {
                Method m = ForgeReflection.findMethod(targetCls, "sendFailure",
                        new Class<?>[]{textCls});
                if (m != null) {
                    m.invoke(target, text);
                    return true;
                }
            } catch (Throwable t) {
                // fall through
            }
        }

        try {
            Method m = ForgeReflection.findMethod(targetCls, "sendSystemMessage",
                    new Class<?>[]{textCls});
            if (m != null) {
                m.invoke(target, text);
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        try {
            Method m = ForgeReflection.findMethod(targetCls, "sendMessage",
                    new Class<?>[]{textCls, UUID.class});
            if (m != null) {
                m.invoke(target, text, UUID.randomUUID());
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        try {
            Method m = ForgeReflection.findMethod(targetCls, "sendMessage",
                    new Class<?>[]{textCls, boolean.class});
            if (m != null) {
                m.invoke(target, text, false);
                return true;
            }
        } catch (Throwable t) {
            // fall through
        }

        try {
            Method m = ForgeReflection.findMethod(targetCls, "sendMessage",
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

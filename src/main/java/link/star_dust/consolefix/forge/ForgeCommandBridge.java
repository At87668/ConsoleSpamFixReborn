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
        if (source == null) return false;
        // Try the native permission API on the underlying player entity.
        UUID playerId = extractPlayerUuid(source);
        if (playerId != null) {
            Object player = resolvePlayer(playerId);
            if (player != null) return checkForgePermission(player, node);
        }
        // Console / non-player source → vanilla op-level (console is allowed).
        return checkVanillaOpLevel(source, 2);
    }

    /**
     * Query a permission node for a player via LuckPerms, then the native Forge
     * {@code PermissionAPI}, falling back to the vanilla op status. Mirrors the
     * MinerTrack implementation.
     */
    private static boolean checkForgePermission(Object player, String node) {
        // 1) LuckPerms direct — works on hybrid servers (Arclight/Mohist) where
        //    the active Forge permission handler forwards to Bukkit.
        Boolean lp = checkForgeLuckPerms(player, node);
        if (lp != null) return lp;
        // 2) Native PermissionAPI using a registered PermissionNode (1.19+).
        Object cachedNode = ForgePermissionRegistry.getNode(node);
        if (cachedNode != null) {
            try {
                Class<?> apiCls = Class.forName("net.minecraftforge.server.permission.PermissionAPI");
                Class<?> playerCls = Class.forName("net.minecraft.server.level.ServerPlayer");
                java.lang.reflect.Method m = apiCls.getMethod("getPermission", playerCls, cachedNode.getClass());
                Object result = m.invoke(null, player, cachedNode);
                if (result instanceof Boolean) return (Boolean) result;
                if (result != null) {
                    String ts = result.toString();
                    if ("TRUE".equals(ts)) return true;
                    if ("FALSE".equals(ts)) return false;
                }
            } catch (Throwable t) {
                // PermissionAPI not present
            }
        }
        // 3) Legacy String overload (Forge 1.18.x) → Boolean / Tristate.
        try {
            Class<?> apiCls = Class.forName("net.minecraftforge.server.permission.PermissionAPI");
            java.lang.reflect.Method m = apiCls.getMethod("getPermission",
                    Class.forName("net.minecraft.server.level.ServerPlayer"), String.class);
            Object result = m.invoke(null, player, node);
            if (result instanceof Boolean) return (Boolean) result;
            if (result != null) {
                String ts = result.toString();
                if ("TRUE".equals(ts)) return true;
                if ("FALSE".equals(ts)) return false;
                // DEFAULT → fall through to op-level
            }
        } catch (Throwable t) {
            // PermissionAPI not present / no String overload
        }
        return isPlayerOperator(player);
    }

    /**
     * Query LuckPerms directly for a node and player. Returns {@code null} when
     * LuckPerms is absent or the result is undefined — the caller falls through
     * to the native API / op level.
     */
    private static Boolean checkForgeLuckPerms(Object player, String node) {
        try {
            UUID uuid = playerUuid(player);
            if (uuid == null) return null;
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = provider.getMethod("get").invoke(null);
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cached = user.getClass().getMethod("getCachedData").invoke(user);
            Object permData = cached.getClass().getMethod("getPermissionData").invoke(cached);
            Object tristate = permData.getClass().getMethod("checkPermission", String.class).invoke(permData, node);
            if (tristate == null) return null;
            try {
                Object asBool = tristate.getClass().getMethod("asBoolean").invoke(tristate);
                if (asBool instanceof Boolean) return (Boolean) asBool;
            } catch (Throwable t) {
                // no asBoolean
            }
            String ts = tristate.toString();
            if (ts != null && ts.contains("TRUE")) return true;
            if (ts != null && ts.contains("FALSE")) return false;
            return null; // DEFAULT / undefined → fall through
        } catch (Throwable t) {
            return null; // LuckPerms not present
        }
    }

    private static UUID playerUuid(Object player) {
        try {
            Object uid = ForgeReflection.callAny(player, "getUUID",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            return uid instanceof UUID ? (UUID) uid : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static UUID extractPlayerUuid(Object css) {
        if (css == null) return null;
        try {
            Object entity = ForgeReflection.callAny(css, "getEntity",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (entity != null) {
                Object uid = ForgeReflection.callAny(entity, "getUUID",
                        ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
                if (uid instanceof UUID) return (UUID) uid;
            }
        } catch (Throwable t) {
            // fall through
        }
        return null;
    }

    private Object resolvePlayer(UUID playerId) {
        try {
            Object server = ForgeReflection.getServer();
            if (server == null) return null;
            Object pm = ForgeReflection.callAny(server, "getPlayerList",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (pm == null) return null;
            return ForgeReflection.callAny(pm, "getPlayer",
                    new Class<?>[]{UUID.class}, new Object[]{playerId});
        } catch (Throwable t) {
            return null;
        }
    }

    /** Whether the player is a vanilla operator (op list). */
    static boolean isPlayerOperator(Object player) {
        try {
            Object server = ForgeReflection.getServer();
            if (server == null) return false;
            Object pm = ForgeReflection.callAny(server, "getPlayerList",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (pm == null) return false;
            Object profile = ForgeReflection.callAny(player, "getGameProfile",
                    ForgeReflectionConstants.NO_PARAMS, ForgeReflectionConstants.NO_ARGS);
            if (profile == null) return false;
            Object result = ForgeReflection.callAny(pm, "isOp",
                    new Class<?>[]{profile.getClass()}, new Object[]{profile});
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean checkVanillaOpLevel(Object source, int requiredLevel) {
        try {
            if (isPlayer()) {
                UUID id = extractPlayerUuid(source);
                if (id != null) {
                    Object player = resolvePlayer(id);
                    if (player != null) return isPlayerOperator(player);
                }
            }
            return isConsole();
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

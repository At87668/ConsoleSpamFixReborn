package link.star_dust.consolefix.forge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers the {@code csf.admin} permission node with the Forge native
 * permission system (Forge 1.19+).
 *
 * <p>Forge's {@code PermissionAPI.getPermission(player, node)} throws
 * {@code UnregisteredPermissionException} for any node that was never
 * registered via {@code PermissionGatherEvent.Nodes}. This registry creates a
 * {@code PermissionNode} (BOOLEAN type) whose default resolver falls back to
 * the vanilla op status — so when no third-party permission handler is active,
 * an op player is granted and everyone else is denied, matching the Bukkit
 * {@code csf.admin = op} default.
 *
 * <p>Called reflectively to avoid compile-time coupling with the Forge classes.
 * The gather event fires during {@code MinecraftServer} construction, before
 * {@code ServerStartingEvent} — so {@link #registerGatherListener()} must be
 * invoked from the mod constructor, exactly like command registration.
 */
final class ForgePermissionRegistry {

    /** The single permission node, registered as BOOLEAN (default = op). */
    private static final String NODE = "csf.admin";

    /** nodeName -> created PermissionNode instance (kept for direct queries). */
    private static final Map<String, Object> NODE_CACHE = new ConcurrentHashMap<>();
    private static volatile boolean listenerRegistered = false;

    private ForgePermissionRegistry() {}

    /**
     * Register the {@code PermissionGatherEvent.Nodes} listener. Must be called
     * from the ForgeCSF constructor.
     */
    static void registerGatherListener() {
        if (listenerRegistered) return;
        listenerRegistered = true;
        ForgeReflection.registerEventListener(
                ForgeReflection.getMainEventBus(),
                ForgeReflection.forgeClass("net.minecraftforge.server.permission.events.PermissionGatherEvent$Nodes"),
                ForgePermissionRegistry::onGatherNodes);
    }

    /** Return the registered PermissionNode instance for a node name, or null. */
    static Object getNode(String nodeName) {
        return NODE_CACHE.get(nodeName);
    }

    private static void onGatherNodes(Object event) {
        try {
            Class<?> nodeCls = ForgeReflection.forgeClass("net.minecraftforge.server.permission.nodes.PermissionNode");
            Class<?> typesCls = ForgeReflection.forgeClass("net.minecraftforge.server.permission.nodes.PermissionTypes");
            Class<?> resolverCls = ForgeReflection.forgeClass("net.minecraftforge.server.permission.nodes.PermissionNode$PermissionResolver");
            Class<?> dynKeyCls = ForgeReflection.forgeClass("net.minecraftforge.server.permission.nodes.PermissionDynamicContextKey");
            if (nodeCls == null || typesCls == null || resolverCls == null || dynKeyCls == null) return;

            Object booleanType = typesCls.getField("BOOLEAN").get(null);

            // Default resolver: an op player is granted, everyone else denied.
            // A real permission handler (e.g. LuckPerms-Forge) ignores this and
            // uses its own data.
            Object resolver = java.lang.reflect.Proxy.newProxyInstance(
                    resolverCls.getClassLoader(),
                    new Class<?>[]{resolverCls},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "resolve":
                                Object player = (args != null && args.length > 0) ? args[0] : null;
                                return player != null && ForgeCommandBridge.isPlayerOperator(player);
                            case "toString":
                                return "ConsoleSpamFixDefaultResolver";
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            default:
                                return null;
                        }
                    });

            Object emptyDyns = java.lang.reflect.Array.newInstance(dynKeyCls, 0);
            // PermissionNode(String modID, String nodeName, PermissionType,
            //                PermissionResolver, PermissionDynamicContextKey...)
            Constructor<?> ctor = nodeCls.getConstructor(String.class, String.class,
                    booleanType.getClass(), resolverCls, emptyDyns.getClass());

            int dot = NODE.indexOf('.');
            String modId = NODE.substring(0, dot);
            String name = NODE.substring(dot + 1);
            Object node = ctor.newInstance(modId, name, booleanType, resolver, emptyDyns);
            NODE_CACHE.put(NODE, node);

            // Build a real PermissionNode[] (NOT Object[]) so getMethod finds
            // addNodes(PermissionNode<?>...) via an exact array-type match.
            Object nodes = java.lang.reflect.Array.newInstance(nodeCls, 1);
            java.lang.reflect.Array.set(nodes, 0, node);

            // event.addNodes(PermissionNode<?>... nodes)
            Method addNodes = event.getClass().getMethod("addNodes", nodes.getClass());
            addNodes.invoke(event, nodes);
        } catch (Throwable t) {
            // Nodes are best-effort; on failure the bridge falls back to op-level.
        }
    }
}

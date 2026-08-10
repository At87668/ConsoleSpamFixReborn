package link.star_dust.consolefix.fabric;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Fabric event-listener registration via dynamic {@link Proxy}.
 *
 * <p>Minecraft types are not on the compile classpath, so Fabric API event
 * callbacks (which take {@code net.minecraft.*} parameters) are proxied.
 * The handler receives the raw {@code com.mojang.brigadier.CommandDispatcher}
 * as {@code Object}.
 */
final class FabricEventBus {
    private FabricEventBus() {}

    /**
     * Register a command registration callback. Supports v2 (1.19+) with
     * v1 (1.18.x) fallback.
     */
    static void registerCommandRegistration(Consumer<Object> handler) {
        if (tryRegisterCommandV2(handler)) return;
        tryRegisterCommandV1(handler);
    }

    private static boolean tryRegisterCommandV2(Consumer<Object> handler) {
        try {
            Class<?> callbackCls = FabricReflection.forName(
                    "net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback");
            if (callbackCls == null) return false;

            java.lang.reflect.Field eventField = callbackCls.getField("EVENT");
            Object event = eventField.get(null);

            java.lang.reflect.Method abstractMethod = findAbstractMethod(callbackCls);
            if (abstractMethod == null) return false;

            Class<?> eventIface = FabricReflection.forName("net.fabricmc.fabric.api.event.Event");
            java.lang.reflect.Method registerMethod = eventIface != null
                    ? eventIface.getMethod("register", Object.class)
                    : event.getClass().getMethod("register", Object.class);

            Object proxy = Proxy.newProxyInstance(
                    callbackCls.getClassLoader(),
                    new Class<?>[]{callbackCls},
                    (proxyObj, method, args) -> {
                        if (!method.equals(abstractMethod)) return handleObjectMethods(proxyObj, method, args);
                        // args[0] is the CommandDispatcher
                        handler.accept(args[0]);
                        return null;
                    });

            registerMethod.invoke(event, proxy);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean tryRegisterCommandV1(Consumer<Object> handler) {
        try {
            Class<?> callbackCls = FabricReflection.forName(
                    "net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback");
            if (callbackCls == null) return false;

            java.lang.reflect.Field eventField = callbackCls.getField("EVENT");
            Object event = eventField.get(null);

            java.lang.reflect.Method abstractMethod = findAbstractMethod(callbackCls);
            if (abstractMethod == null) return false;

            Class<?> eventIface = FabricReflection.forName("net.fabricmc.fabric.api.event.Event");
            java.lang.reflect.Method registerMethod = eventIface != null
                    ? eventIface.getMethod("register", Object.class)
                    : event.getClass().getMethod("register", Object.class);

            Object proxy = Proxy.newProxyInstance(
                    callbackCls.getClassLoader(),
                    new Class<?>[]{callbackCls},
                    (proxyObj, method, args) -> {
                        if (!method.equals(abstractMethod)) return handleObjectMethods(proxyObj, method, args);
                        handler.accept(args[0]);
                        return null;
                    });

            registerMethod.invoke(event, proxy);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Handle equals/hashCode/toString for dynamic proxies. */
    private static Object handleObjectMethods(Object proxy, Method method, Object[] args) {
        if ("equals".equals(method.getName()) && method.getParameterCount() == 1
                && method.getParameterTypes()[0] == Object.class) {
            return proxy == args[0];
        }
        if ("hashCode".equals(method.getName()) && method.getParameterCount() == 0) {
            return System.identityHashCode(proxy);
        }
        if ("toString".equals(method.getName()) && method.getParameterCount() == 0) {
            return "FabricEventBus$Proxy@" + System.identityHashCode(proxy);
        }
        return null;
    }

    /** Returns the single abstract method if the interface is functional, else null. */
    private static Method findAbstractMethod(Class<?> iface) {
        if (iface == null) return null;
        Method found = null;
        int count = 0;
        for (Method m : iface.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) {
                found = m;
                count++;
                if (count > 1) return null;
            }
        }
        return found;
    }
}

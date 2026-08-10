package link.star_dust.consolefix.neoforge;

import link.star_dust.consolefix.core.FastReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Reflection helpers for accessing Minecraft / NeoForge internals.
 *
 * <p>NeoForge 1.20.2+ uses Mojang names at runtime and the
 * {@code net.neoforged.*} packages. The main event bus is
 * {@code NeoForge.EVENT_BUS} (with a {@code net.minecraftforge.*} fallback
 * for 1.20.1). Event registration uses the 4-arg {@code addListener}
 * overload to avoid generic-signature resolution problems.
 */
final class NeoForgeReflection {

    private NeoForgeReflection() {}

    static boolean DEBUG_REFLECTION = false;

    private static volatile Object cachedServer;

    /** Cache the dedicated-server instance resolved during lifecycle init. */
    static void setCachedServer(Object server) {
        cachedServer = server;
    }

    /** Return the cached server instance (may be null before server start). */
    static Object getServer() {
        return cachedServer;
    }

    private static void log(String msg) {
        System.out.println("[ConsoleSpamFixReborn:NeoForgeReflection] " + msg);
    }

    private static final Object NOT_FOUND = new Object();

    private static final ConcurrentHashMap<String, Object> classCache = new ConcurrentHashMap<>(64);
    private static final ConcurrentHashMap<MethodKey, Object> methodCache = new ConcurrentHashMap<>(128);
    private static final ConcurrentHashMap<StaticMethodKey, Object> staticMethodCache = new ConcurrentHashMap<>(64);
    private static final ConcurrentHashMap<FieldKey, Object> fieldCache = new ConcurrentHashMap<>(64);

    private static final class MethodKey {
        final Class<?> cls;
        final String name;
        final Class<?>[] paramTypes;

        MethodKey(Class<?> cls, String name, Class<?>[] paramTypes) {
            this.cls = cls;
            this.name = name;
            this.paramTypes = paramTypes;
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof MethodKey)) return false;
            MethodKey k = (MethodKey) o;
            return cls == k.cls && name.equals(k.name) && Arrays.equals(paramTypes, k.paramTypes);
        }

        @Override public int hashCode() {
            return cls.hashCode() * 31 + name.hashCode() + Arrays.hashCode(paramTypes);
        }
    }

    private static final class StaticMethodKey {
        final String className;
        final String methodName;
        final Class<?>[] paramTypes;

        StaticMethodKey(String className, String methodName, Class<?>[] paramTypes) {
            this.className = className;
            this.methodName = methodName;
            this.paramTypes = paramTypes;
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof StaticMethodKey)) return false;
            StaticMethodKey k = (StaticMethodKey) o;
            return className.equals(k.className) && methodName.equals(k.methodName)
                    && Arrays.equals(paramTypes, k.paramTypes);
        }

        @Override public int hashCode() {
            return className.hashCode() * 31 + methodName.hashCode() + Arrays.hashCode(paramTypes);
        }
    }

    private static final class FieldKey {
        final Class<?> cls;
        final String name;

        FieldKey(Class<?> cls, String name) {
            this.cls = cls;
            this.name = name;
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof FieldKey)) return false;
            FieldKey k = (FieldKey) o;
            return cls == k.cls && name.equals(k.name);
        }

        @Override public int hashCode() {
            return cls.hashCode() * 31 + name.hashCode();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T unwrap(Object cached) {
        return (cached == NOT_FOUND) ? null : (T) cached;
    }

    // ==================================================================
    // Class loading
    // ==================================================================

    static Class<?> forName(String className) {
        if (className == null) return null;
        Object cached = classCache.get(className);
        if (cached != null) return unwrap(cached);
        Class<?> cls = tryLoad(className);
        if (cls != null) {
            classCache.put(className, cls);
            return cls;
        }
        if (DEBUG_REFLECTION) log("CLS-MISS " + className);
        classCache.put(className, NOT_FOUND);
        return null;
    }

    /**
     * Load a class visible to the NeoForge mod-loading environment. Tries
     * the calling classloader, the thread context classloader, and the
     * classloader of the main event bus.
     */
    static Class<?> forgeClass(String name) {
        if (name == null) return null;
        Class<?> cls = tryLoad(name);
        if (cls != null) return cls;
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        if (ctx != null) {
            cls = tryLoadWith(ctx, name);
            if (cls != null) return cls;
        }
        Object bus = getMainEventBus();
        if (bus != null) {
            ClassLoader bl = bus.getClass().getClassLoader();
            if (bl != null) {
                cls = tryLoadWith(bl, name);
                if (cls != null) return cls;
            }
        }
        return null;
    }

    private static Class<?> tryLoad(String name) {
        if (name == null) return null;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Class<?> tryLoadWith(ClassLoader cl, String name) {
        try {
            return Class.forName(name, false, cl);
        } catch (Throwable t) {
            return null;
        }
    }

    // ==================================================================
    // Text/Component creation
    // ==================================================================

    private static volatile Class<?> cachedTextClass;
    private static volatile boolean cachedTextClassResolved;

    static Class<?> resolveTextComponentClass() {
        if (cachedTextClassResolved) return cachedTextClass;
        cachedTextClass = forName(NeoForgeReflectionConstants.CLS_COMPONENT);
        cachedTextClassResolved = true;
        return cachedTextClass;
    }

    static Object createText(String message) {
        if (message == null) return null;
        Object r = callStatic(NeoForgeReflectionConstants.CLS_COMPONENT,
                NeoForgeReflectionConstants.M_COMPONENT_LITERAL,
                new Class<?>[]{String.class}, new Object[]{message});
        if (r != null) return r;
        Class<?> tcCls = forName(NeoForgeReflectionConstants.CLS_TEXT_COMPONENT);
        if (tcCls != null) {
            try {
                Constructor<?> ctor = tcCls.getDeclaredConstructor(String.class);
                ctor.setAccessible(true);
                return ctor.newInstance(message);
            } catch (Throwable t) {
                // fall through
            }
        }
        return null;
    }

    // ==================================================================
    // Method invocation
    // ==================================================================

    static Object callStatic(String className, String methodName,
                             Class<?>[] paramTypes, Object[] args) {
        StaticMethodKey key = new StaticMethodKey(className, methodName, paramTypes);
        Object cached = staticMethodCache.get(key);
        if (cached != null) {
            Method m = unwrap(cached);
            if (m == null) return null;
            return FastReflection.invokeStatic(m, args);
        }
        Class<?> cls = forName(className);
        if (cls == null) {
            staticMethodCache.put(key, NOT_FOUND);
            return null;
        }
        String r = NeoForgeReflectionConstants.redirectMethod(methodName);

        try {
            Method m = cls.getDeclaredMethod(r, paramTypes);
            m.setAccessible(true);
            Object result = FastReflection.invokeStatic(m, args);
            staticMethodCache.put(key, m);
            return result;
        } catch (NoSuchMethodException e) {
            try {
                Method m = cls.getMethod(r, paramTypes);
                Object result = FastReflection.invokeStatic(m, args);
                staticMethodCache.put(key, m);
                return result;
            } catch (Throwable t2) {
                // fall through
            }
        } catch (Throwable e) {
            staticMethodCache.put(key, NOT_FOUND);
            return null;
        }

        // Parameter-type scan fallback.
        for (Method candidate : cls.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(candidate.getModifiers())) continue;
            if (!paramsMatch(candidate, paramTypes)) continue;
            try {
                candidate.setAccessible(true);
                Object result = FastReflection.invokeStatic(candidate, args);
                staticMethodCache.put(key, candidate);
                return result;
            } catch (Throwable ignored) {
            }
        }

        if (DEBUG_REFLECTION) log("STATIC-MISS " + className + "." + methodName);
        staticMethodCache.put(key, NOT_FOUND);
        return null;
    }

    static Object call(Object target, String methodName,
                       Class<?>[] paramTypes, Object[] args) {
        if (target == null) return null;
        Method m = findMethodImpl(target.getClass(), methodName, paramTypes);
        if (m == null) return null;
        return FastReflection.invoke(m, target, args);
    }

    static Object callAny(Object target, String methodName,
                          Class<?>[] paramTypes, Object[] args) {
        return call(target, methodName, paramTypes, args);
    }

    static Method findMethod(Class<?> cls, String name, Class<?>[] paramTypes) {
        return findMethodImpl(cls, name, paramTypes);
    }

    private static Method findMethodImpl(Class<?> cls, String name, Class<?>[] paramTypes) {
        if (cls == null || name == null) return null;
        MethodKey key = new MethodKey(cls, name, paramTypes);
        Object cached = methodCache.get(key);
        if (cached != null) return unwrap(cached);

        Method m = tryMethod(cls, name, paramTypes);
        if (m != null) {
            methodCache.put(key, m);
            return m;
        }
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method candidate : c.getDeclaredMethods()) {
                if (!paramsMatch(candidate, paramTypes)) continue;
                try {
                    candidate.setAccessible(true);
                    methodCache.put(key, candidate);
                    return candidate;
                } catch (Throwable ignored) {
                }
            }
        }

        if (DEBUG_REFLECTION) log("M-MISS " + cls.getName() + "." + name);
        methodCache.put(key, NOT_FOUND);
        return null;
    }

    private static Method tryMethod(Class<?> cls, String name, Class<?>[] paramTypes) {
        try {
            Method m = cls.getDeclaredMethod(name, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            try {
                return cls.getMethod(name, paramTypes);
            } catch (NoSuchMethodException e2) {
                return null;
            }
        }
    }

    private static boolean paramsMatch(Method m, Class<?>[] paramTypes) {
        Class<?>[] pts = m.getParameterTypes();
        if (pts.length != paramTypes.length) return false;
        for (int i = 0; i < pts.length; i++) {
            if (!pts[i].isAssignableFrom(paramTypes[i])) return false;
        }
        return true;
    }

    // ==================================================================
    // Field access
    // ==================================================================

    @SuppressWarnings("unchecked")
    static <T> T getField(Object target, String fieldName) {
        Class<?> cls = (target instanceof Class) ? (Class<?>) target : target.getClass();
        FieldKey key = new FieldKey(cls, fieldName);
        Object cached = fieldCache.get(key);
        Field f = cached != null ? unwrap(cached) : null;
        if (cached == null) {
            for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    f = c.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    // keep walking
                }
            }
            fieldCache.put(key, f == null ? NOT_FOUND : f);
        }
        if (f == null) return null;
        f.setAccessible(true);
        Object owner = (target instanceof Class) ? null : target;
        return (T) FastReflection.get(f, owner);
    }

    // ==================================================================
    // NeoForge-specific
    // ==================================================================

    /** Resolve the NeoForge config directory ({@code FMLPaths.CONFIGDIR}). */
    static Path getConfigDir() {
        try {
            Class<?> fmlPaths = forgeClass("net.neoforged.fml.loading.FMLPaths");
            if (fmlPaths == null) return Path.of("config");
            Field f = fmlPaths.getField("CONFIGDIR");
            Object v = f.get(null);
            return v instanceof Path ? (Path) v : Path.of("config");
        } catch (Throwable t) {
            return Path.of("config");
        }
    }

    /**
     * Resolve the NeoForge main event bus: {@code NeoForge.EVENT_BUS}
     * (1.20.2+), falling back to {@code MinecraftForge.EVENT_BUS} (1.20.1).
     */
    static Object getMainEventBus() {
        try {
            Class<?> neoForge = forgeClass("net.neoforged.neoforge.common.NeoForge");
            if (neoForge != null) {
                try {
                    Field eb = neoForge.getField("EVENT_BUS");
                    return eb.get(null);
                } catch (NoSuchFieldException ignored) {
                    // fall back
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            Class<?> mcForge = forgeClass("net.minecraftforge.common.MinecraftForge");
            if (mcForge == null) return null;
            Field eb = mcForge.getField("EVENT_BUS");
            return eb.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Register a {@code Consumer<Object>} listener for {@code eventClass} on the
     * given event bus. Uses the 4-arg {@code addListener(EventPriority, boolean,
     * Class, Consumer)} overload so the event type does not need to be resolved
     * from the consumer's generic signature.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void registerEventListener(Object eventBus, Class<?> eventClass, Consumer<Object> handler) {
        if (eventBus == null || eventClass == null) return;
        try {
            Class<?> priorityCls = Class.forName("net.neoforged.bus.api.EventPriority");
            if (priorityCls == null) return;
            Object normal = priorityCls.getField("NORMAL").get(null);
            Method al = findAddListener(eventBus.getClass(), 4);
            if (al != null) {
                al.invoke(eventBus, normal, false, eventClass, handler);
                return;
            }
            log("4-arg addListener not found on " + eventBus.getClass().getName());
            Method al2 = eventBus.getClass().getMethod("addListener", java.util.function.Consumer.class);
            Object typed = makeConsumer(eventClass, handler);
            al2.invoke(eventBus, typed);
        } catch (Throwable t) {
            log("Failed to register listener: " + t);
        }
    }

    private static Method findAddListener(Class<?> busClass, int paramCount) {
        for (Method m : busClass.getMethods()) {
            if (!"addListener".equals(m.getName())) continue;
            if (m.getParameterCount() != paramCount) continue;
            if (paramCount == 4) {
                Class<?>[] pts = m.getParameterTypes();
                if (pts[2] != Class.class || pts[3] != java.util.function.Consumer.class) continue;
            }
            return m;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> java.util.function.Consumer<T> makeConsumer(Class<T> eventClass, Consumer<Object> handler) {
        return new java.util.function.Consumer<T>() {
            @Override public void accept(T e) {
                handler.accept(e);
            }
        };
    }
}

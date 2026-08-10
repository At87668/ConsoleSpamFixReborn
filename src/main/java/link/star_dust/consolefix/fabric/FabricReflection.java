package link.star_dust.consolefix.fabric;

import link.star_dust.consolefix.core.FastReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight reflection helpers for accessing Minecraft internals on the
 * Fabric platform.
 *
 * <p>Uses {@link FabricReflectionConstants} to redirect bare mojang method
 * names to their runtime form (intermediary on 1.18–1.21.x, named on
 * MC 26+ / dev).
 */
final class FabricReflection {

    private static volatile Object cachedServer;
    static boolean DEBUG_REFLECTION = false;

    private FabricReflection() {}

    static void setDebugReflection(boolean on) {
        DEBUG_REFLECTION = on;
    }

    static void setCachedServer(Object server) {
        cachedServer = server;
    }

    static Object getServer() {
        return cachedServer;
    }

    private static void log(String msg) {
        System.out.println("[ConsoleSpamFixReborn:Reflection] " + msg);
    }

    // ==================================================================
    // Reflection caches
    // ==================================================================

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

    /**
     * Load a Minecraft class by its mojang/named name, falling back to the
     * intermediary class name on production servers. Results are cached.
     */
    static Class<?> forName(String namedClassName) {
        if (namedClassName == null) return null;
        Object cached = classCache.get(namedClassName);
        if (cached != null) return unwrap(cached);

        Class<?> cls = tryLoad(namedClassName);
        if (cls != null) {
            classCache.put(namedClassName, cls);
            return cls;
        }
        String inter = FabricReflectionConstants.toIntermediaryClass(namedClassName);
        if (inter != null && !inter.equals(namedClassName)) {
            cls = tryLoad(inter);
            if (cls != null) {
                classCache.put(namedClassName, cls);
                return cls;
            }
        }
        if (DEBUG_REFLECTION) log("CLS-MISS " + namedClassName);
        classCache.put(namedClassName, NOT_FOUND);
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

    // ==================================================================
    // Text/Component creation
    // ==================================================================

    private static volatile Class<?> cachedTextClass;
    private static volatile boolean cachedTextClassResolved;

    /** Resolve the text component class ({@code Component} / {@code class_2561}). */
    static Class<?> resolveTextComponentClass() {
        if (cachedTextClassResolved) return cachedTextClass;
        cachedTextClass = forName(FabricReflectionConstants.CLS_COMPONENT);
        cachedTextClassResolved = true;
        return cachedTextClass;
    }

    /**
     * Create a Minecraft text component from a plain string.
     *
     * <ol>
     *   <li>{@code Component.literal(String)} — 1.19.3+ / MC 26.1+</li>
     *   <li>{@code new TextComponent(String)} — 1.18–1.19.2</li>
     * </ol>
     */
    static Object createText(String message) {
        if (message == null) return null;
        Object r = callStatic(FabricReflectionConstants.CLS_COMPONENT,
                FabricReflectionConstants.M_COMPONENT_LITERAL,
                new Class<?>[]{String.class}, new Object[]{message});
        if (r != null) return r;

        Class<?> tcCls = forName(FabricReflectionConstants.CLS_TEXT_COMPONENT);
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
        String r = FabricReflectionConstants.redirectMethod(methodName);

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

        if (!r.equals(methodName)) {
            try {
                Method m = cls.getDeclaredMethod(methodName, paramTypes);
                m.setAccessible(true);
                Object result = FastReflection.invokeStatic(m, args);
                staticMethodCache.put(key, m);
                return result;
            } catch (Throwable ignored) {
            }
            try {
                Method m = cls.getMethod(methodName, paramTypes);
                m.setAccessible(true);
                Object result = FastReflection.invokeStatic(m, args);
                staticMethodCache.put(key, m);
                return result;
            } catch (Throwable ignored) {
            }
        }

        // Parameter-type scan — match any static method with compatible params.
        for (Method candidate : cls.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(candidate.getModifiers())) continue;
            Class<?>[] pts = candidate.getParameterTypes();
            if (pts.length != paramTypes.length) continue;
            boolean match = true;
            for (int i = 0; i < pts.length; i++) {
                if (!pts[i].isAssignableFrom(paramTypes[i])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                try {
                    candidate.setAccessible(true);
                    Object result = FastReflection.invokeStatic(candidate, args);
                    staticMethodCache.put(key, candidate);
                    return result;
                } catch (Throwable ignored) {
                }
            }
        }

        if (DEBUG_REFLECTION) log("STATIC-MISS " + className + "." + methodName + " (resolved=" + r + ")");
        staticMethodCache.put(key, NOT_FOUND);
        return null;
    }

    /** Call an instance method; bare mojang name is redirected automatically. */
    static Object call(Object target, String methodName,
                       Class<?>[] paramTypes, Object[] args) {
        if (target == null) return null;
        Method m = findMethodImpl(target.getClass(), methodName, paramTypes);
        if (m == null) return null;
        return FastReflection.invoke(m, target, args);
    }

    /** Alias for {@link #call}. */
    static Object callAny(Object target, String methodName,
                          Class<?>[] paramTypes, Object[] args) {
        return call(target, methodName, paramTypes, args);
    }

    // ==================================================================
    // Method lookup
    // ==================================================================

    static Method findMethod(Class<?> cls, String name, Class<?>[] paramTypes) {
        return findMethodImpl(cls, name, paramTypes);
    }

    private static Method findMethodImpl(Class<?> cls, String name, Class<?>[] paramTypes) {
        if (cls == null) return null;
        MethodKey key = new MethodKey(cls, name, paramTypes);
        Object cached = methodCache.get(key);
        if (cached != null) return unwrap(cached);

        String r = FabricReflectionConstants.redirectMethod(name);
        Method found = null;

        // 1. Exact match with the resolved name.
        try {
            found = cls.getDeclaredMethod(r, paramTypes);
            found.setAccessible(true);
            methodCache.put(key, found);
            return found;
        } catch (NoSuchMethodException e) {
            try {
                found = cls.getMethod(r, paramTypes);
                methodCache.put(key, found);
                return found;
            } catch (Throwable t2) {
                // fall through
            }
        } catch (Throwable e) {
            methodCache.put(key, NOT_FOUND);
            return null;
        }

        // 2. Try the original (bare mojang) name.
        if (!r.equals(name)) {
            try {
                found = cls.getDeclaredMethod(name, paramTypes);
                found.setAccessible(true);
                methodCache.put(key, found);
                return found;
            } catch (Throwable ignored) {
            }
            try {
                found = cls.getMethod(name, paramTypes);
                methodCache.put(key, found);
                return found;
            } catch (Throwable ignored) {
            }
        }

        // 3. Parameter-type scan across the class hierarchy.
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (Method candidate : c.getDeclaredMethods()) {
                if (candidate.getParameterCount() != paramTypes.length) continue;
                Class<?>[] pts = candidate.getParameterTypes();
                boolean match = true;
                for (int i = 0; i < pts.length; i++) {
                    if (!pts[i].isAssignableFrom(paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    try {
                        candidate.setAccessible(true);
                        methodCache.put(key, candidate);
                        return candidate;
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        if (DEBUG_REFLECTION) log("M-MISS " + cls.getName() + "." + name + " (resolved=" + r + ")");
        methodCache.put(key, NOT_FOUND);
        return null;
    }

    // ==================================================================
    // Field access
    // ==================================================================

    @SuppressWarnings("unchecked")
    static <T> T getField(Object target, String fieldName) {
        Class<?> cls = (target instanceof Class) ? (Class<?>) target : target.getClass();
        Field f = findField(cls, fieldName);
        if (f == null) return null;
        f.setAccessible(true);
        Object owner = (target instanceof Class) ? null : target;
        return (T) FastReflection.get(f, owner);
    }

    private static Field findField(Class<?> cls, String name) {
        FieldKey key = new FieldKey(cls, name);
        Object cached = fieldCache.get(key);
        if (cached != null) return unwrap(cached);
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                fieldCache.put(key, f);
                return f;
            } catch (NoSuchFieldException e) {
                // keep walking up
            }
        }
        fieldCache.put(key, NOT_FOUND);
        return null;
    }
}

package link.star_dust.consolefix.fabric;

import java.lang.reflect.Method;

/**
 * Client-side command feedback for {@code /csfc}.
 *
 * <p>The source is a {@code FabricClientCommandSource} (fabric-api), held as
 * {@code Object}. Feedback goes through {@code sendFeedback(Component)} /
 * {@code sendError(Component)} via reflection, with a console fallback so the
 * message is never lost.
 */
final class FabricClientCommandBridge {

    private final Object source;

    FabricClientCommandBridge(Object source) {
        this.source = source;
    }

    void success(String message) {
        send(message, true);
    }

    void failure(String message) {
        send(message, false);
    }

    private void send(String message, boolean success) {
        if (source == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        Object text = FabricReflection.createText(message);
        if (text == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        Class<?> textCls = FabricReflection.resolveTextComponentClass();
        if (textCls == null) {
            System.out.println("[ConsoleSpamFixReborn] " + message);
            return;
        }
        String methodName = success ? "sendFeedback" : "sendError";
        try {
            Method m = FabricReflection.findMethod(source.getClass(), methodName, new Class<?>[]{textCls});
            if (m != null) {
                m.invoke(source, text);
                return;
            }
        } catch (Throwable t) {
            // fall through
        }
        System.out.println("[ConsoleSpamFixReborn] " + message);
    }
}

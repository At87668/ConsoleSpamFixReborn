package link.star_dust.consolefix.forge;

/**
 * Runtime-name constants for the Forge platform.
 *
 * <p>Forge 1.18+ runs Mojang-mapped classes, so the runtime names ARE the
 * mojang/named names — no intermediary fallback is needed. Method names are
 * passed through unchanged (identity redirect).
 */
final class ForgeReflectionConstants {

    private ForgeReflectionConstants() {}

    static final Class<?>[] NO_PARAMS = new Class<?>[0];
    static final Object[]   NO_ARGS   = new Object[0];

    static final String CLS_COMPONENT      = "net.minecraft.network.chat.Component";
    static final String CLS_TEXT_COMPONENT = "net.minecraft.network.chat.TextComponent";

    // Component.literal(String) — named name; resolved by signature scan if absent.
    static final String M_COMPONENT_LITERAL = "literal";

    /** Forge uses Mojang names at runtime — no redirects required. */
    static String redirectMethod(String bareName) {
        return bareName;
    }
}

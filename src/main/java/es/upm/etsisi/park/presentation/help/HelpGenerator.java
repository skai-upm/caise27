package es.upm.etsisi.park.presentation.help;

import java.util.Map;

import es.upm.etsisi.park.presentation.CommandRegistry;

/**
 * Produces help text for the registered commands. The rendering behaviour is
 * injected as a map of variants to renderers, so new help formats can be
 * added at wiring time without touching command processing.
 */
public class HelpGenerator {

    public static final String BASIC = "";
    public static final String EXTENDED = "EXTENDED";

    private final CommandRegistry registry;
    private final Map<String, HelpRenderer> renderers;

    public HelpGenerator(CommandRegistry registry, Map<String, HelpRenderer> renderers) {
        this.registry = registry;
        this.renderers = Map.copyOf(renderers);
    }

    public String render(String variant) {
        HelpRenderer renderer = renderers.get(variant);
        if (renderer == null) {
            throw new IllegalArgumentException("No help renderer for variant '" + variant + "'");
        }
        return renderer.render(registry.definitions());
    }
}

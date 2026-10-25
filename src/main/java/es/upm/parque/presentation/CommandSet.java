package es.upm.parque.presentation;

/**
 * A group of related commands that registers itself in a {@link CommandRegistry}.
 */
public interface CommandSet {

    void registerInto(CommandRegistry registry);
}

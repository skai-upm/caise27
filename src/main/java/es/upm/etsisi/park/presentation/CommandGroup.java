package es.upm.etsisi.park.presentation;

/** A set of related commands that registers itself in a {@link CommandRegistry}. */
public interface CommandGroup {

    void register(CommandRegistry registry);
}

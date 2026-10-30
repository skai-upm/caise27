package es.upm.etsisi.amusementpark.presentation.command;

/**
 * A set of related commands bound to the controller that executes them.
 */
public interface CommandModule {

    void registerIn(CommandRegistry registry);
}

package es.upm.etsisi.amusementpark.cli;

import java.io.PrintStream;

/**
 * A command that can be executed by the {@link CommandProcessor}.
 */
public interface Command {

    /**
     * Executes the command.
     *
     * @param arguments the fields of the command line after the keyword
     * @param out       destination for the command output
     * @return {@code true} if command processing must stop after this command
     */
    boolean execute(String[] arguments, PrintStream out);
}

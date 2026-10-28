package es.upm.etsisi.amusementpark.presentation;

import java.io.PrintStream;

/**
 * EXIT: terminates command processing in a controlled manner.
 */
public class ExitCommand implements Command {

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        out.println("Exiting application.");
        return true;
    }
}

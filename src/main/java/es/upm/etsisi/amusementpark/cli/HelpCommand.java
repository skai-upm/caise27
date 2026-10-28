package es.upm.etsisi.amusementpark.cli;

import java.io.PrintStream;
import java.util.List;

/**
 * HELP: displays the available commands and their basic syntax.
 */
public class HelpCommand implements Command {

    public static final List<String> SYNTAX = List.of(
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "HELP",
            "EXIT");

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        out.println("Available commands:");
        SYNTAX.forEach(out::println);
        return false;
    }
}

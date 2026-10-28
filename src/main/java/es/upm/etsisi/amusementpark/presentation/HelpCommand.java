package es.upm.etsisi.amusementpark.presentation;

import java.io.PrintStream;
import java.util.List;

/**
 * HELP: displays the available commands and their basic syntax.
 */
public class HelpCommand implements Command {

    public static final List<String> SYNTAX = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP",
            "EXIT");

    @Override
    public boolean execute(String[] arguments, PrintStream out) {
        out.println("Available commands:");
        SYNTAX.forEach(out::println);
        return false;
    }
}

package com.amusementpark.presentation;

import com.amusementpark.business.controller.ParkController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Interprets and executes text commands from a registry shared by command execution and help.
 * Used by both interactive and file modes.
 */
public class CommandProcessor {

    static final String EXIT = "EXIT";

    private final List<CommandDefinition> commands = new ArrayList<>();
    private final PrintStream out;

    public CommandProcessor(ParkController park, HelpGenerator help, HelpGenerator extendedHelp, PrintStream out) {
        this.out = out;
        register("USER CREATE", 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        register("USER DELETE", 2, "USER DELETE;Correo;Contraseña",
                "Deletes the user if the password matches and no ticket is dated today or later.",
                p -> park.deleteUser(p[1], p[2]));
        register("USER UPDATE", 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking the password.",
                p -> park.updateUser(p[1], p[2], p[3], p[4]));
        register("SYSTEM DATE", 1, "SYSTEM DATE;yyyy-MM-dd",
                "Sets the simulated current date used by every date rule.",
                p -> park.setSystemDate(p[1]));
        register("SYSTEM PRICE", 1, "SYSTEM PRICE;Precio",
                "Sets the base price of new individual tickets for this run.",
                p -> park.setBasePrice(p[1]));
        register("GROUP CREATE", 2, "GROUP CREATE;IdGrupo;Nombre",
                "Creates an empty group.",
                p -> park.createGroup(p[1], p[2]));
        register("GROUP ADD", 2, "GROUP ADD;IdGrupo;Correo",
                "Adds a registered user to a group that has no current or future group ticket.",
                p -> park.addToGroup(p[1], p[2]));
        register("GROUP REMOVE", 2, "GROUP REMOVE;IdGrupo;Correo",
                "Removes a member from a group that has no current or future group ticket.",
                p -> park.removeFromGroup(p[1], p[2]));
        register("GROUP DELETE", 1, "GROUP DELETE;IdGrupo",
                "Deletes a group that has no current or future group ticket.",
                p -> park.deleteGroup(p[1]));
        register("TICKET CREATE", 3, "TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd",
                "Buys an individual ticket for a user at the current base price.",
                p -> park.createTicket(p[1], p[2], p[3]));
        register("TICKET DELETE", 1, "TICKET DELETE;IdEntrada",
                "Refunds an individual ticket dated after the system date.",
                p -> park.deleteTicket(p[1]));
        register("GROUPTICKET CREATE", 3, "GROUPTICKET CREATE;IdEntradaGrupo;IdGrupo;yyyy-MM-dd",
                "Buys one ticket per current member with a discount by group size.",
                p -> park.createGroupTicket(p[1], p[2], p[3]));
        register("GROUPTICKET DELETE", 1, "GROUPTICKET DELETE;IdEntradaGrupo",
                "Refunds a group ticket dated after the system date with all its tickets.",
                p -> park.deleteGroupTicket(p[1]));
        register("ATTRACTION CREATE", 3, "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
                "Creates an attraction with a maximum capacity.",
                p -> park.createAttraction(p[1], p[2], p[3]));
        register("ATTRACTION ENTER", 2, "ATTRACTION ENTER;IdAtraccion;IdEntrada",
                "Admits all holders of a ticket valid today if there is room for all of them.",
                p -> park.enterAttraction(p[1], p[2]));
        register("ATTRACTION EXIT", 2, "ATTRACTION EXIT;IdAtraccion;IdEntrada|Correo",
                "Lets the holders of a ticket, or the user with that email, leave the attraction.",
                p -> park.exitAttraction(p[1], p[2]));
        register("ATTRACTION STATUS", 1, "ATTRACTION STATUS;IdAtraccion",
                "Shows capacity, occupancy, free places and users present.",
                p -> park.attractionStatus(p[1]));
        register("INVOICE CREATE", 2, "INVOICE CREATE;USER|GROUP;Correo|IdGrupo",
                "Shows an invoice of a user's tickets or of a group's group tickets.",
                p -> park.createInvoice(p[1], p[2]));
        register("HELP", 0, "HELP",
                "Lists the syntax of every command.",
                p -> help.generate(getCommands()));
        register("HELP EXTENDED", 0, "HELP EXTENDED",
                "Lists every command with its syntax and a short description.",
                p -> extendedHelp.generate(getCommands()));
        register(EXIT, 0, EXIT,
                "Ends the session; later commands in a file are not executed.",
                p -> "OK: Bye.");
    }

    private void register(String name, int parameters, String syntax, String description,
                          Function<String[], String> action) {
        commands.add(new CommandDefinition(name, parameters, syntax, description, action));
    }

    public List<CommandDefinition> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (!process(line)) {
                return;
            }
        }
    }

    /**
     * Processes a single command line.
     *
     * @return {@code false} if the command was EXIT, {@code true} otherwise
     */
    public boolean process(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        String[] parts = command.split(";", -1);
        String name = parts[0].trim();
        CommandDefinition definition = find(name);
        if (definition == null) {
            out.println("ERROR: Unknown command: " + name + " (type HELP for the list)");
            return true;
        }
        int parameters = parts.length - 1;
        if (!definition.accepts(parameters)) {
            out.println("ERROR: " + name + " expects " + expected(definition)
                    + " parameter(s) but got " + parameters + ".");
            return true;
        }
        out.println(definition.action().apply(parts));
        return !EXIT.equals(name);
    }

    private CommandDefinition find(String name) {
        for (CommandDefinition c : commands) {
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }

    private static String expected(CommandDefinition c) {
        return c.minParameters() == c.maxParameters() ? Integer.toString(c.minParameters())
                : c.minParameters() + " to " + c.maxParameters();
    }
}

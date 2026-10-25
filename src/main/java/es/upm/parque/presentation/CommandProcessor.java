package es.upm.parque.presentation;

import es.upm.parque.business.controller.ParkController;
import es.upm.parque.presentation.help.HelpGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets text commands (components separated by ';') and executes the registered command definitions
 * through the park controller. Shared by interactive and file execution modes.
 */
public class CommandProcessor {

    public static final String HELP_BASIC = "BASIC";
    public static final String HELP_EXTENDED = "EXTENDED";

    private final ParkController controller;
    private final HelpGenerator helpGenerator;
    private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();
    private boolean exitRequested;

    public CommandProcessor(ParkController controller, HelpGenerator helpGenerator) {
        this.controller = controller;
        this.helpGenerator = helpGenerator;
        registerCommands();
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public List<CommandDefinition> getCommands() {
        return new ArrayList<>(commands.values());
    }

    private void register(String name, List<String> parameters, String description, CommandAction action) {
        commands.put(name, new CommandDefinition(name, parameters, description, action));
    }

    private void registerCommands() {
        ParkController c = controller;
        register("USER CREATE", List.of("Nombre", "Apellidos", "DNI", "Correo", "Contraseña", "Telefono", "Tarjeta"),
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> c.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6]));
        register("USER DELETE", List.of("Correo", "Contraseña"),
                "Deletes a user given the correct password, unless the user has tickets dated today or later.",
                p -> c.deleteUser(p[0], p[1]));
        register("USER UPDATE", List.of("Correo", "Contraseña", "Campo", "NuevoValor"),
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user after checking the password.",
                p -> c.updateUser(p[0], p[1], p[2], p[3]));
        register("SYSTEM DATE", List.of("yyyy-MM-dd"),
                "Sets the simulated current date used by every date-dependent rule.",
                p -> c.setDate(p[0]));
        register("SYSTEM PRICE", List.of("Precio"),
                "Sets the individual ticket price used for new purchases (40.00 at startup).",
                p -> c.setPrice(p[0]));
        register("GROUP CREATE", List.of("IdGrupo", "Nombre"),
                "Creates an empty group with a unique identifier.",
                p -> c.createGroup(p[0], p[1]));
        register("GROUP ADD", List.of("IdGrupo", "Correo"),
                "Adds a registered user to a group (not allowed while the group has current or future group tickets).",
                p -> c.addToGroup(p[0], p[1]));
        register("GROUP REMOVE", List.of("IdGrupo", "Correo"),
                "Removes a user from a group (not allowed while the group has current or future group tickets).",
                p -> c.removeFromGroup(p[0], p[1]));
        register("GROUP DELETE", List.of("IdGrupo"),
                "Deletes a group without current or future group tickets; its users are kept.",
                p -> c.deleteGroup(p[0]));
        register("TICKET CREATE", List.of("IdEntrada", "Correo", "yyyy-MM-dd"),
                "Buys an individual ticket for a user and date at the current price.",
                p -> c.createTicket(p[0], p[1], p[2]));
        register("TICKET DELETE", List.of("IdEntrada"),
                "Deletes an individual ticket dated after the current date, together with its passes.",
                p -> c.deleteTicket(p[0]));
        register("GROUPTICKET CREATE", List.of("IdEntrada", "IdGrupo", "yyyy-MM-dd"),
                "Buys a group ticket: one individual ticket per member, with a discount by group size.",
                p -> c.createGroupTicket(p[0], p[1], p[2]));
        register("GROUPTICKET DELETE", List.of("IdEntrada"),
                "Deletes a group ticket dated after the current date, with its internal tickets and their passes.",
                p -> c.deleteGroupTicket(p[0]));
        register("ATTRACTION CREATE", List.of("IdAtraccion", "Nombre", "Capacidad", "[Tipo]"),
                "Creates an attraction with a maximum capacity; Tipo is GENERAL (default), INDIVIDUAL, GROUP or SPECIAL.",
                p -> c.createAttraction(p[0], p[1], p[2], p.length > 3 ? p[3] : null));
        register("ATTRACTION DELETE", List.of("IdAtraccion"),
                "Deletes an empty attraction.",
                p -> c.deleteAttraction(p[0]));
        register("ATTRACTION ENTER", List.of("IdAtraccion", "IdEntrada", "[SPEED]"),
                "Lets in the users of a ticket valid today if they all fit and the attraction type admits it; "
                        + "SPEED consumes one SpeedPass per ticket; SPECIAL attractions consume the Special Pass.",
                p -> c.enterAttraction(p[0], p[1], p.length > 2 ? p[2] : null));
        register("ATTRACTION EXIT", List.of("IdAtraccion", "IdEntrada"),
                "Lets out the users of a ticket, freeing their places.",
                p -> c.exitAttraction(p[0], p[1]));
        register("ATTRACTION STATUS", List.of("IdAtraccion"),
                "Shows capacity, occupancy, available places and users present.",
                p -> c.attractionStatus(p[0]));
        register("INVOICE CREATE", List.of("USER|GROUP", "Correo|IdGrupo"),
                "Shows an invoice: for a user, direct tickets and personal passes; for a group, its group tickets.",
                p -> c.createInvoice(p[0], p[1]));
        register("SPEEDPASS CREATE", List.of("IdEntrada", "Cantidad"),
                "Buys 1 or 10 SpeedPasses for an individual ticket, each costing 20% of the ticket price.",
                p -> c.createSpeedPasses(p[0], p[1]));
        register("SPEEDPASS DELETE", List.of("IdPase"),
                "Refunds an unused SpeedPass of a ticket dated after the current date.",
                p -> c.deleteSpeedPass(p[0]));
        register("SPECIALPASS CREATE", List.of("IdEntrada"),
                "Buys the Special Pass of an individual ticket (one per ticket) at the ticket price.",
                p -> c.createSpecialPass(p[0]));
        register("SPECIALPASS DELETE", List.of("IdPase"),
                "Refunds an unused Special Pass of a ticket dated after the current date.",
                p -> c.deleteSpecialPass(p[0]));
        register("HELP", List.of(),
                "Shows the syntax of every command.",
                p -> helpGenerator.generate(HELP_BASIC, getCommands()));
        register("HELP EXTENDED", List.of(),
                "Shows the syntax and a description of every command.",
                p -> helpGenerator.generate(HELP_EXTENDED, getCommands()));
        register("EXIT", List.of(),
                "Ends the application (commands after EXIT are not executed).",
                p -> {
                    exitRequested = true;
                    return "OK BYE";
                });
    }

    /**
     * Executes one command line and returns the response to show to the user.
     */
    public String process(String line) {
        String[] parts = line.trim().split(";", -1);
        String operation = parts[0].trim().toUpperCase();
        CommandDefinition command = commands.get(operation);
        if (command == null) {
            return "ERROR unknown command: " + parts[0].trim() + " (type HELP)";
        }
        int count = parts.length - 1;
        if (count < command.minParameters() || count > command.maxParameters()) {
            return "ERROR wrong number of parameters. Usage: " + command.syntax();
        }
        String[] parameters = new String[count];
        System.arraycopy(parts, 1, parameters, 0, count);
        try {
            return command.action().execute(parameters);
        } catch (Exception e) {
            return "ERROR could not execute command: " + e.getMessage();
        }
    }
}

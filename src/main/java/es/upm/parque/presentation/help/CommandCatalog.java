package es.upm.parque.presentation.help;

import java.util.List;

/**
 * Registry of the commands the application accepts, in the order they are presented.
 */
public class CommandCatalog {

    private final List<CommandInfo> commands;

    public CommandCatalog(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    /**
     * The catalog of all commands currently supported by the application.
     */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                new CommandInfo("USER CREATE", "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
                new CommandInfo("USER DELETE", "USER DELETE;Correo;Contraseña",
                        "Deletes the user if the password matches and no ticket is dated on or after the system date."),
                new CommandInfo("USER UPDATE", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the current credentials."),
                new CommandInfo("GROUP CREATE", "GROUP CREATE;IdGrupo;Nombre",
                        "Creates an empty group with a unique identifier."),
                new CommandInfo("GROUP ADD", "GROUP ADD;IdGrupo;Correo",
                        "Adds a registered user to the group, unless already a member or the group is locked."),
                new CommandInfo("GROUP REMOVE", "GROUP REMOVE;IdGrupo;Correo",
                        "Removes a member from the group, unless the group is locked by a current or future group ticket."),
                new CommandInfo("GROUP DELETE", "GROUP DELETE;IdGrupo",
                        "Deletes the group (its users stay registered), unless it is locked."),
                new CommandInfo("TICKET CREATE", "TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for a user and date at the current price."),
                new CommandInfo("TICKET DELETE", "TICKET DELETE;IdEntrada",
                        "Deletes a standalone individual ticket dated after the system date."),
                new CommandInfo("GROUPTICKET CREATE", "GROUPTICKET CREATE;IdEntradaGrupo;IdGrupo;yyyy-MM-dd",
                        "Buys one ticket per group member with a discount of 0/10/20/50% by group size."),
                new CommandInfo("GROUPTICKET DELETE", "GROUPTICKET DELETE;IdEntradaGrupo",
                        "Deletes a group ticket dated after the system date together with its members' tickets."),
                new CommandInfo("ATTRACTION CREATE", "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;GENERAL|INDIVIDUAL|GROUP|SPECIAL]",
                        "Creates an attraction with a positive capacity; GENERAL (any ticket) unless another type is given."),
                new CommandInfo("ATTRACTION DELETE", "ATTRACTION DELETE;IdAtraccion",
                        "Deletes an attraction with nobody inside."),
                new CommandInfo("ATTRACTION ENTER", "ATTRACTION ENTER;IdAtraccion;IdEntrada",
                        "Admits all users of a ticket valid today if there is room for all of them."),
                new CommandInfo("ATTRACTION EXIT", "ATTRACTION EXIT;IdAtraccion;IdEntrada",
                        "Lets the ticket's users leave the attraction, freeing their places."),
                new CommandInfo("ATTRACTION STATUS", "ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, available places and the users inside."),
                new CommandInfo("SPEEDPASS CREATE", "SPEEDPASS CREATE;IdEntrada;1|10",
                        "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price."),
                new CommandInfo("SPEEDPASS DELETE", "SPEEDPASS DELETE;IdPase",
                        "Refunds an unused SpeedPass if its ticket is dated after the system date."),
                new CommandInfo("SPECIALPASS CREATE", "SPECIALPASS CREATE;IdEntrada",
                        "Buys the ticket's single Special Pass at the ticket price."),
                new CommandInfo("SPECIALPASS DELETE", "SPECIALPASS DELETE;IdEntrada",
                        "Refunds the ticket's unused Special Pass if the ticket is dated after the system date."),
                new CommandInfo("INVOICE CREATE", "INVOICE CREATE;USER|GROUP;Correo|IdGrupo",
                        "Prints an invoice of a user's direct purchases or a group's group tickets."),
                new CommandInfo("SYSTEM DATE", "SYSTEM DATE;yyyy-MM-dd",
                        "Sets the application's current date used by every date rule."),
                new CommandInfo("SYSTEM PRICE", "SYSTEM PRICE;Precio",
                        "Sets the individual ticket price for the rest of the execution."),
                new CommandInfo("HELP", "HELP",
                        "Lists the syntax of every command."),
                new CommandInfo("HELP EXTENDED", "HELP EXTENDED",
                        "Lists the syntax and a description of every command."),
                new CommandInfo("EXIT", "EXIT",
                        "Ends the application; no later command is executed.")));
    }

    public List<CommandInfo> getCommands() {
        return commands;
    }

    /**
     * Returns the syntax of the given operation, or the operation itself if it is not registered.
     */
    public String syntaxOf(String operation) {
        return commands.stream()
                .filter(command -> command.operation().equals(operation))
                .map(CommandInfo::syntax)
                .findFirst()
                .orElse(operation);
    }
}

package com.parque.presentation;

import java.util.List;

/**
 * Registry of the commands supported by the application, used by every help format.
 */
public class CommandCatalog {
    private final List<CommandInfo> commands;

    public CommandCatalog(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    /** Registered commands in display order (read-only). */
    public List<CommandInfo> commands() {
        return commands;
    }

    /** The commands of the application. Add new commands here. */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
                new CommandInfo("USER DELETE;Correo;Contraseña",
                        "Deletes the authenticated user unless they own a ticket dated on or after the system date."),
                new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of the authenticated user."),
                new CommandInfo("GROUP CREATE;Identificador;Nombre", "Creates an empty group with a unique identifier."),
                new CommandInfo("GROUP ADD;Identificador;Correo",
                        "Adds a registered user to the group (not twice, not while the group is locked)."),
                new CommandInfo("GROUP REMOVE;Identificador;Correo",
                        "Removes a member from the group (not while the group is locked)."),
                new CommandInfo("GROUP DELETE;Identificador",
                        "Deletes the group, keeping its users (not while the group is locked)."),
                new CommandInfo("TICKET CREATE;Identificador;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for a user and a date not before the system date, at the current price."),
                new CommandInfo("TICKET DELETE;Identificador",
                        "Cancels an individual ticket dated after the system date."),
                new CommandInfo("GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd",
                        "Buys a ticket for every current group member with a discount by group size; locks the group."),
                new CommandInfo("GROUPTICKET DELETE;Identificador",
                        "Cancels a group ticket dated after the system date together with its member tickets."),
                new CommandInfo("SPEEDPASS CREATE;Identificador;Ticket;Cantidad",
                        "Buys 1 or 10 SpeedPasses (20% of the ticket's price each) for an individual ticket."),
                new CommandInfo("SPEEDPASS DELETE;Identificador",
                        "Refunds an unused SpeedPass whose ticket is dated after the system date."),
                new CommandInfo("SPECIALPASS CREATE;Identificador;Ticket",
                        "Buys the only Special Pass of an individual ticket (100% of the ticket's price)."),
                new CommandInfo("SPECIALPASS DELETE;Identificador",
                        "Refunds an unused Special Pass whose ticket is dated after the system date."),
                new CommandInfo("ATTRACTION CREATE;Identificador;Nombre;Aforo[;Tipo]",
                        "Creates an attraction with a maximum capacity and type GENERAL (default), INDIVIDUAL, GROUP or SPECIAL."),
                new CommandInfo("ATTRACTION ENTER;Identificador;Ticket",
                        "Admits the visitors of a ticket valid today if they all fit in the attraction."),
                new CommandInfo("ATTRACTION EXIT;Identificador;Ticket",
                        "Lets the visitors of a ticket leave the attraction, freeing their places."),
                new CommandInfo("ATTRACTION STATUS;Identificador",
                        "Shows capacity, occupancy, available places and the users present."),
                new CommandInfo("INVOICE CREATE;USER;Correo",
                        "Shows the invoice of the tickets bought directly by a user."),
                new CommandInfo("INVOICE CREATE;GROUP;Identificador",
                        "Shows the invoice of the group tickets bought by a group."),
                new CommandInfo("SYSTEM DATE;yyyy-MM-dd", "Sets the current date used by every date-dependent rule."),
                new CommandInfo("SYSTEM PRICE;Precio", "Sets the price of individual tickets bought from now on."),
                new CommandInfo("HELP", "Lists the syntax of the available commands."),
                new CommandInfo("HELP EXTENDED", "Lists the available commands with a short explanation of each."),
                new CommandInfo("EXIT", "Ends the execution; no further commands are processed.")));
    }
}
